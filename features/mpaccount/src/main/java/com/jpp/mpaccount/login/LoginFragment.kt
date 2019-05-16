package com.jpp.mpaccount.login

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.jpp.mpaccount.R
import com.jpp.mpdesign.ext.getViewModel
import com.jpp.mpdesign.ext.setInvisible
import com.jpp.mpdesign.ext.setVisible
import com.jpp.mpdesign.ext.snackBar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

/**
 * Fragment used to provide the user a login experience.
 * The login model supported by the application is based on Oauth2 (because the API supports that model).
 * Following that model, this Fragment provides a WebView that renders the web content needed to perform
 * the login and captures any redirection performed by the web site, delegating the responsibility
 * of performing the actual login to a ViewModel that supports this Fragment.
 */
class LoginFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        withViewModel {
            viewStates.observe(this@LoginFragment.viewLifecycleOwner, Observer { viewState -> viewState.actionIfNotHandled { renderViewState(it) } })
            navEvents.observe(this@LoginFragment.viewLifecycleOwner, Observer { navEvent ->  reactToNavEvent(navEvent)})
            onInit()
        }
    }

    /**
     * Helper function to execute methods over the [LoginViewModel].
     */
    private fun withViewModel(action: LoginViewModel.() -> Unit) = getViewModel<LoginViewModel>(viewModelFactory).action()

    /**
     * Performs the branching to render the proper views given then [viewState].
     */
    private fun renderViewState(viewState: LoginViewState) {
        when (viewState) {
            is LoginViewState.NotConnected -> { renderNotConnectedToNetwork() }
            is LoginViewState.Loading -> { renderLoading() }
            is LoginViewState.UnableToLogin -> { renderUnableToLogin() }
            is LoginViewState.ShowOauth -> { renderOauthState(viewState) }
        }
    }

    /**
     * Reacts to the navigation event provided.
     */
    private fun reactToNavEvent(navEvent: LoginNavigationEvent) {
        when (navEvent) {
            is LoginNavigationEvent.BackToPrevious -> findNavController().popBackStack()
        }
    }

    private fun renderOauthState(oauthState: LoginViewState.ShowOauth) {
        accountWebView.apply {
            settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
                webViewClient = LoginWebViewClient(oauthState.interceptUrl) { redirectedUrl -> withViewModel { onUserRedirectedToUrl(redirectedUrl, oauthState.accessToken) } }
            }
            webChromeClient = LoginWebChromeClient(accountWebPg)
            loadUrl(oauthState.url)
        }

        if (oauthState.reminder) {
            snackBar(loginContent, R.string.account_approve_reminder, R.string.error_retry) {
                withViewModel { onUserRetry() }
            }
        }

        loginErrorView.setInvisible()
        loginLoadingView.setInvisible()
        accountWebPg.setVisible()
        accountWebView.setVisible()
    }

    private fun renderNotConnectedToNetwork() {
        accountWebPg.setInvisible()
        accountWebView.setInvisible()
        loginLoadingView.setInvisible()

        loginErrorView.asNoConnectivityError { withViewModel { onUserRetry() } }
        loginErrorView.setVisible()
    }

    private fun renderUnableToLogin() {
        accountWebPg.setInvisible()
        accountWebView.setInvisible()
        loginLoadingView.setInvisible()

        loginErrorView.asUnknownError { withViewModel { onUserRetry() } }
        loginErrorView.setVisible()
    }

    private fun renderLoading() {
        loginErrorView.setInvisible()
        accountWebPg.setInvisible()
        accountWebView.setInvisible()

        loginLoadingView.setVisible()
    }


    /**
     * A [WebViewClient] used to listen for changes in the WebView
     * being used to load the login page used in the Oauth flow.
     */
    private class LoginWebViewClient(private val redirectUrl: String,
                                     private val callback: (String) -> Unit) : WebViewClient() {

        @SuppressWarnings("deprecation")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleUrl(url)
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
            return handleUrl(request.url.toString())
        }

        private fun handleUrl(url: String): Boolean {
            if (url.startsWith(redirectUrl)) {
                callback(url)
                return true
            }
            return false
        }
    }

    /**
     * A [WebChromeClient] implementation used to listen for process
     * updates when the WebView is loading a URL.
     */
    private class LoginWebChromeClient(private val pgBar: ProgressBar) : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            when (newProgress) {
                100 -> pgBar.setInvisible()
                else -> pgBar.progress = newProgress;
            }
        }
    }
}
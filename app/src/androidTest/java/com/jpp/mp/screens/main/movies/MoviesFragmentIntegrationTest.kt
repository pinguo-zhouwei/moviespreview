package com.jpp.mp.screens.main.movies

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.jpp.mp.R
import com.jpp.mp.assertions.*
import com.jpp.mp.di.TestMPViewModelFactory
import com.jpp.mp.extras.launch
import com.jpp.mp.extras.moviesPages
import com.jpp.mp.screens.main.movies.fragments.PlayingMoviesFragment
import com.jpp.mp.testutils.FragmentTestActivity
import com.jpp.mpdomain.MovieSection
import com.jpp.mpdomain.usecase.movies.ConfigMovieUseCase
import com.jpp.mpdomain.usecase.movies.GetMoviesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.jpp.mpdomain.usecase.movies.GetMoviesUseCase.GetMoviesResult.*
import com.jpp.mptestutils.CurrentThreadExecutorService

/**
 * Tests the interaction between the [MoviesFragment] and the [MoviesFragmentViewModel].
 * It is extremely hard to unit-test a PagedList and the scrolling behavior in the list
 * of movies - therefore, this integration tests are exercising that code in order to put
 * coverage on it and coverage on the [MoviesFragment] itself.
 *
 * We actually test [PlayingMoviesFragment], but all fragments are the same here.
 */
@RunWith(AndroidJUnit4::class)
class MoviesFragmentIntegrationTest {

    @get:Rule
    val activityTestRule = object : ActivityTestRule<FragmentTestActivity>(FragmentTestActivity::class.java, true, false) {
    }

    private fun launchAndInjectFragment() {
        activityTestRule.activity.startFragment(PlayingMoviesFragment(), this@MoviesFragmentIntegrationTest::inject)
    }

    private fun inject(fragment: PlayingMoviesFragment) {
        // inject the factory and the ViewModel
        fragment.viewModelFactory = TestMPViewModelFactory().apply {
            addVm(viewModel)
        }
    }

    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var configMovieUseCase: ConfigMovieUseCase
    private lateinit var viewModel: PlayingMoviesFragment.PlayingMoviesFragmentViewModel

    @Before
    fun setUp() {
        getMoviesUseCase = mockk()
        configMovieUseCase = mockk()

        // real ViewModel
        viewModel = PlayingMoviesFragment.PlayingMoviesFragmentViewModel(
                getMoviesUseCase = getMoviesUseCase,
                configMovieUseCase = configMovieUseCase,
                networkExecutor = CurrentThreadExecutorService()
        )

        activityTestRule.launch()
    }

    @Test
    fun shouldFetchNewMoviesPageOnAttachedToActivity() {
        val pages = moviesPages(10)

        every { getMoviesUseCase.getMoviePageForSection(any(), MovieSection.Playing) } answers { Success(pages[arg((0))]) }
        every { configMovieUseCase.configure(any(), any(), any()) } answers { arg(2) }

        launchAndInjectFragment()

        waitForDoneLoading()

        onMoviesLoadingView().assertNotDisplayed()
        onMoviesErrorView().assertNotDisplayed()
        onMoviesList().assertDisplayed()
        onMoviesList().assertItemCount(pages[0].results.size)

        /*
         * Here we verify that the MoviesFragmentViewModel is properly mapping the model classes to
         * UI classes by matching each item in the recycler view with the expected value.
         */
        onView(withViewInRecyclerView(R.id.moviesList, 0, R.id.movieListItemTitle))
                .check(ViewAssertions.matches(ViewMatchers.withText(pages[0].results[0].title)))

        onView(withViewInRecyclerView(R.id.moviesList, 1, R.id.movieListItemTitle))
                .check(ViewAssertions.matches(ViewMatchers.withText(pages[0].results[1].title)))

        verify { getMoviesUseCase.getMoviePageForSection(1, MovieSection.Playing) }
    }

    @Test
    fun shouldShowUnknownError() {
        every { getMoviesUseCase.getMoviePageForSection(any(), MovieSection.Playing) } answers { ErrorUnknown }

        launchAndInjectFragment()

        waitForViewState(MoviesViewState.ErrorUnknown)

        onMoviesErrorView().assertDisplayed()
        onView(withId(R.id.errorTitleTextView)).assertWithText(R.string.error_unexpected_error_message)

        onMoviesLoadingView().assertNotDisplayed()
        onMoviesList().assertNotDisplayed()
    }

    @Test
    fun shouldShowConnectivityError() {
        every { getMoviesUseCase.getMoviePageForSection(any(), MovieSection.Playing) } answers { ErrorNoConnectivity }

        launchAndInjectFragment()

        waitForViewState(MoviesViewState.ErrorNoConnectivity)

        onMoviesErrorView().assertDisplayed()
        onView(withId(R.id.errorTitleTextView)).assertWithText(R.string.error_no_network_connection_message)

        onMoviesLoadingView().assertNotDisplayed()
        onMoviesList().assertNotDisplayed()
    }


    private fun waitForDoneLoading() {
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String = "Waiting for items in list"

            override fun checkCondition(): Boolean {
                return viewModel.viewState().value is MoviesViewState.InitialPageLoaded
            }
        })
    }

    private fun waitForViewState(viewState: MoviesViewState) {
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String = "Waiting for items in list"

            override fun checkCondition(): Boolean {
                return viewModel.viewState().value == viewState
            }
        })
    }


    private fun onMoviesList() = onView(withId(R.id.moviesList))
    private fun onMoviesLoadingView() = onView(withId(R.id.moviesLoadingView))
    private fun onMoviesErrorView() = onView(withId(R.id.moviesErrorView))
}
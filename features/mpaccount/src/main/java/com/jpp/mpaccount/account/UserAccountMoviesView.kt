package com.jpp.mpaccount.account

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jpp.mpaccount.R
import com.jpp.mpdesign.ext.inflate
import com.jpp.mpdesign.ext.loadImageUrl
import com.jpp.mpdesign.ext.setInvisible
import com.jpp.mpdesign.ext.setVisible
import kotlinx.android.synthetic.main.layout_user_account_movies.view.*
import kotlinx.android.synthetic.main.list_item_user_account_movies.view.*

class UserAccountMoviesView : ConstraintLayout {

    /**
     * Generic interface definition to load the list of items.
     */
    interface UserAccountMovieItem {
        fun getImageUrl(): String
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UserAccountMoviesView)
        try {
            setTitle(typedArray.getText(R.styleable.UserAccountMoviesView_accountMoviesTitleText))
        } finally {
            typedArray.recycle()
        }
    }

    private fun init() {
        inflate(context, R.layout.layout_user_account_movies, this)
    }

    fun setTitle(title: CharSequence) {
        userAccountMoviesTitle.text = title
    }

    fun showMovies(movies: List<UserAccountMovieItem>, moreAction: () -> Unit) {
        userAccountMoviesMoreIv.apply {
            setVisible()
            setOnClickListener { moreAction.invoke() }
        }

        userAccountMoviesList.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = AccountMovieAdapter(movies)
            setVisible()
        }

        userAccountMoviesError.setInvisible()
    }

    fun showErrorMessage(message: String) {
        userAccountMoviesMoreIv.setInvisible()
        userAccountMoviesList.setInvisible()

        userAccountMoviesError.apply {
            text = message
            setVisible()
        }
    }


    class AccountMovieAdapter(private val items: List<UserAccountMovieItem>) : RecyclerView.Adapter<AccountMovieAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.list_item_user_account_movies))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(item: UserAccountMovieItem) {
                itemView.userAccountMoviesItemIv.loadImageUrl(item.getImageUrl())
            }
        }
    }
}
package com.jpp.moviespreview.screens.main.movies.fragments

import com.jpp.moviespreview.domainlayer.MovieSection
import com.jpp.moviespreview.screens.main.movies.MoviesFragment

class PlayingMoviesFragment : MoviesFragment() {

    override fun getMoviesSection(): MovieSection = MovieSection.Playing

}
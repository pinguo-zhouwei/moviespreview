package com.jpp.moviespreview.screens.main.details

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.jpp.moviespreview.screens.main.TestCoroutineDispatchers
import com.jpp.moviespreview.utiltest.InstantTaskExecutorExtension
import com.jpp.mpdomain.MovieDetail
import com.jpp.mpdomain.MovieGenre
import com.jpp.mpdomain.repository.details.MovieDetailsRepository
import com.jpp.mpdomain.repository.details.MovieDetailsRepositoryState
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, InstantTaskExecutorExtension::class)
class MovieDetailsViewModelTest {


    @MockK
    private lateinit var detailsRepository: MovieDetailsRepository

    private lateinit var subject: MovieDetailsViewModel

    @BeforeEach
    fun setUp() {
        subject = MovieDetailsViewModel(TestCoroutineDispatchers(), detailsRepository)
    }


    @Test
    fun `Should fetch movie detail from repository, adapt to UI model and post value on init`() {
        val movieDetailId = 121.toDouble()
        val expectedTitle = "aTitle"
        val expectedOverview = "anOverview"
        val expectedReleaseDate = "aReleaseDat"
        val expectedVoteCount = 12.toDouble()
        val expectedVoteAverage = 15F
        val expectedPopularity = 178F
        val expectedMovieGenres = listOf(
                MovieGenreItem.Action,
                MovieGenreItem.Horror
        )

        val movieDetail = MovieDetail(
                id = movieDetailId,
                title = expectedTitle,
                overview = expectedOverview,
                release_date = expectedReleaseDate,
                vote_count = expectedVoteCount,
                vote_average = expectedVoteAverage,
                popularity = expectedPopularity,
                poster_path = null,
                genres = listOf(
                        MovieGenre(28, "Action"),
                        MovieGenre(27, "Horror")
                )
        )

        every { detailsRepository.getDetail(movieDetailId) } returns MovieDetailsRepositoryState.Success(movieDetail)

        executeInitTest(movieDetailId) {
            assertTrue(it is MovieDetailsFragmentViewState.ShowDetail)

            with((it as MovieDetailsFragmentViewState.ShowDetail).detail) {
                assertEquals(expectedTitle, title)
                assertEquals(expectedOverview, overview)
                assertEquals(expectedReleaseDate, releaseDate)
                assertEquals(expectedVoteCount, voteCount)
                assertEquals(expectedVoteAverage, voteAverage)
                assertEquals(expectedPopularity, popularity)
                assertEquals(expectedMovieGenres, genres)
            }
        }
        verify(exactly = 1) { detailsRepository.getDetail(movieDetailId) }
    }

    @Test
    fun `Should fetch movie detail from repository and show connectivity error`() {
        val movieDetailId = 121.toDouble()

        every { detailsRepository.getDetail(movieDetailId) } returns MovieDetailsRepositoryState.ErrorNoConnectivity

        executeInitTest(movieDetailId) {
            assertTrue(it is MovieDetailsFragmentViewState.ErrorNoConnectivity)
        }
    }

    @Test
    fun `Should fetch movie detail from repository and show unknown error`() {
        val movieDetailId = 121.toDouble()

        every { detailsRepository.getDetail(movieDetailId) } returns MovieDetailsRepositoryState.ErrorUnknown

        executeInitTest(movieDetailId) {
            assertTrue(it is MovieDetailsFragmentViewState.ErrorUnknown)
        }
    }

    @Test
    fun `Should not re-fetch movie detail when init is called twice with the same id`() {
        val movieDetailId = 121.toDouble()

        every { detailsRepository.getDetail(movieDetailId) } returns MovieDetailsRepositoryState.Success(mockk(relaxed = true))

        // first call
        subject.init(movieDetailId)

        // second call
        subject.init(movieDetailId)

        verify(exactly = 1) { detailsRepository.getDetail(movieDetailId) }
    }


    private fun executeInitTest(movieId: Double, verification: (MovieDetailsFragmentViewState) -> Unit) {
        val lifecycleOwner: LifecycleOwner = mockk()
        val lifecycle = LifecycleRegistry(lifecycleOwner)

        every { lifecycleOwner.lifecycle } returns lifecycle

        subject.init(movieId)

        subject.viewState().observe(lifecycleOwner, Observer {
            verification.invoke(it)
        })

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

}
package com.jpp.moviespreview.datalayer

import com.jpp.moviespreview.domainlayer.ImagesConfiguration as DomainImagesConfiguration
import com.jpp.moviespreview.datalayer.ImagesConfiguration as DataImagesConfiguration
import com.jpp.moviespreview.datalayer.AppConfiguration as DataAppConfiguration
import com.jpp.moviespreview.domainlayer.Movie as DomainMovie
import com.jpp.moviespreview.datalayer.Movie as DataMovie
import com.jpp.moviespreview.domainlayer.MoviePage as DomainMoviePage
import com.jpp.moviespreview.datalayer.MoviePage as DataMoviePage

/**
 * Maps all domain classes into data module classes.
 */
class DataModelMapper {

    /**
     * Maps a [DomainImagesConfiguration] into a [DataAppConfiguration].
     */
    fun mapDomainImagesConfiguration(domainImagesConfiguration: DomainImagesConfiguration): DataAppConfiguration =
        with(domainImagesConfiguration) {
            DataAppConfiguration(
                    DataImagesConfiguration(
                            base_url = baseUrl,
                            poster_sizes = posterSizes,
                            profile_sizes = profileSizes,
                            backdrop_sizes = backdropSizes
                    )
            )
        }


    /**
     * Maps a [DataAppConfiguration] into a [DomainImagesConfiguration].
     */
    fun mapDataAppConfiguration(dataAppConfiguration: DataAppConfiguration): DomainImagesConfiguration =
        with(dataAppConfiguration.images) {
            DomainImagesConfiguration(
                    baseUrl = base_url,
                    posterSizes = poster_sizes,
                    profileSizes = profile_sizes,
                    backdropSizes = backdrop_sizes
            )
        }

    /**
     * Maps a [DataMoviePage] into a [DomainMoviePage].
     */
    fun mapDataMoviePage(dataMoviePage: DataMoviePage): DomainMoviePage =
        with(dataMoviePage) {
            DomainMoviePage(
                    pageNumber = page,
                    movies = results.map { mapDataMovie(it) },
                    totalPages = total_pages
            )
        }

    /**
     * Maps a [DataMovie] into a [DomainMovie].
     */
    fun mapDataMovie(dataMovie: DataMovie): DomainMovie =
        with(dataMovie) {
            DomainMovie(
                    id = id,
                    title = title,
                    originalTitle = original_title,
                    overview = overview,
                    releaseDate = release_date,
                    originalLanguage = original_language,
                    posterPath = poster_path,
                    backdropPath = backdrop_path,
                    voteCount = vote_count,
                    voteAverage = vote_average,
                    popularity = popularity
            )
        }


    /**
     * Maps a [DomainMoviePage] into a [DataMoviePage].
     */
    fun mapDomainMoviePage(domainMoviePage: DomainMoviePage): DataMoviePage =
        with(domainMoviePage) {
            DataMoviePage(
                    page = pageNumber,
                    results = movies.map { mapDomainMovie(it) },
                    total_pages = totalPages,
                    total_results = 0 // for the moment this parameter is not important
            )
        }

    /**
     * Maps a [DomainMovie] into a [DataMovie].
     */
    fun mapDomainMovie(domainMovie: DomainMovie): DataMovie =
        with(domainMovie) {
            DataMovie(
                    id = id,
                    title = title,
                    original_title = originalTitle,
                    overview = overview,
                    release_date = releaseDate,
                    original_language = originalLanguage,
                    poster_path = posterPath,
                    backdrop_path = backdropPath,
                    vote_count = voteCount,
                    vote_average = voteAverage,
                    popularity = popularity
            )
        }

}
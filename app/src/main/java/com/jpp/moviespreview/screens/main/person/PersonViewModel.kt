package com.jpp.moviespreview.screens.main.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jpp.moviespreview.screens.CoroutineDispatchers
import com.jpp.moviespreview.screens.MPScopedViewModel
import com.jpp.mpdomain.Person
import com.jpp.mpdomain.usecase.person.GetPersonUseCase
import com.jpp.mpdomain.usecase.person.GetPersonUseCaseResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * TODO JPP verifica si te conviene tener un UseCase para crear el image path de person.
 */
class PersonViewModel @Inject constructor(dispatchers: CoroutineDispatchers,
                                          private val getPersonUseCase: GetPersonUseCase)
    : MPScopedViewModel(dispatchers) {

    private val viewStateLiveData by lazy { MutableLiveData<PersonViewState>() }
    private var currentPersonId: Double = INVALID_PERSON_ID


    fun init(personId: Double) {
        if (currentPersonId == personId) {
            return
        }

        viewStateLiveData.value = PersonViewState.Loading
        launch {
            /*
             * This work is being executed in the default dispatcher, which indicates that is
             * running in a different thread that the UI thread.
             * Since the default context in ViewModel is the main context (UI thread), once
             * that withContext returns is value, we're back in the main context.
             */
            viewStateLiveData.value = withContext(dispatchers.default()) { fetchPerson(personId) }
        }
    }

    /**
     * Subscribe to this [LiveData] in order to get updates of the [PersonViewState].
     */
    fun viewState(): LiveData<PersonViewState> = viewStateLiveData

    override fun onCleared() {
        currentPersonId = INVALID_PERSON_ID
        super.onCleared()
    }


    /**
     * Fetches the person that is identified by [personId]
     * @return the [PersonViewState] that will be rendered as result of the
     * use case execution.
     */
    private fun fetchPerson(personId: Double): PersonViewState =
            getPersonUseCase
                    .getPerson(personId)
                    .also { currentPersonId = personId }
                    .let { ucResult ->
                        when (ucResult) {
                            is GetPersonUseCaseResult.ErrorNoConnectivity -> PersonViewState.ErrorNoConnectivity
                            is GetPersonUseCaseResult.ErrorUnknown -> PersonViewState.ErrorUnknown
                            is GetPersonUseCaseResult.Success -> {
                                PersonViewState.Loaded(
                                        person = mapPersonToUiPerson(ucResult.person),
                                        showBirthday = ucResult.person.birthday != null,
                                        showDeathDay = ucResult.person.deathday != null,
                                        showPlaceOfBirth = ucResult.person.place_of_birth != null
                                )
                            }
                        }
                    }


    /**
     * Maps a domain [Person] into a [UiPerson] ready to be rendered.
     */
    private fun mapPersonToUiPerson(domainPerson: Person): UiPerson =
            with(domainPerson) {
                UiPerson(
                        name = name,
                        biography = biography,
                        birthday = birthday ?: "",
                        deathday = deathday ?: "",
                        placeOfBirth = place_of_birth ?: ""
                )
            }


    private companion object {
        const val INVALID_PERSON_ID = (-1).toDouble()
    }
}
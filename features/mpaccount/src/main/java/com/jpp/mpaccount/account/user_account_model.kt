package com.jpp.mpaccount.account

/*
 * Contains the definitions for the entire model used in the user account feature.
 */

/**
 * Represents the view states that the user account view can assume.
 */
sealed class UserAccountViewState {
    /*
     * Shows the not connected to network state
     */
    object ShowNotConnected : UserAccountViewState()
    /*
     * Shows when the VM indicates that a work is in progress.
     */
    object Loading : UserAccountViewState()

    /*
     * Shows the generic error screen.
     */
    object ShowError : UserAccountViewState()

    /*
     * Shows the user account data.
     */
    data class ShowUserAccountData(
            val avatarUrl: String,
            val userName: String,
            val accountName: String,
            val defaultLetter: Char
    ) : UserAccountViewState()
}

/**
 * Represents all the navigation events that the user account view will response to.
 */
sealed class UserAccountNavigationEvent {
    /*
     * Used when the VM detects that the user is not logged in.
     */
    object GoToLogin : UserAccountNavigationEvent()
}
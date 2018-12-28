package com.jpp.mpdomain.repository
/**
 * Represents the state of an operation that is executed.
 */
sealed class OperationState {
    object Loaded : OperationState()
    object Loading : OperationState()
    object Error : OperationState()
}
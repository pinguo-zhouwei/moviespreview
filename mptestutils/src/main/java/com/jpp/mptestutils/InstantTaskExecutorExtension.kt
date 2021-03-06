package com.jpp.mptestutils

import android.annotation.SuppressLint
import androidx.annotation.RestrictTo
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension to swap the background executor used by the Architecture Components
 * with a different one which executes each task synchronously.
 *
 * By default, LiveData uses an asynchronous notification mechanism backed by a task executor.
 * This extension takes care of changing that task executor that is running in a background
 * thread for a new one that runs in the same thread that the test is being executed.
 *
 * Based on https://developer.android.com/reference/androidx/arch/core/executor/testing/InstantTaskExecutorRule
 * Source https://github.com/mannodermaus/android-junit5/issues/66
 */
@RestrictTo(RestrictTo.Scope.TESTS)
class InstantTaskExecutorExtension : BeforeEachCallback, AfterEachCallback {

    @SuppressLint("RestrictedApi")
    override fun beforeEach(context: ExtensionContext) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean {
                return true
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}

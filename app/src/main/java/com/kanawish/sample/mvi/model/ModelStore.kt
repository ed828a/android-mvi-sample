package com.kanawish.sample.mvi.model

import com.jakewharton.rxrelay2.PublishRelay
import com.kanawish.sample.mvi.intent.Intent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

open class ModelStore<S> (startingState: S) : Model<S> {
    private val intents = PublishRelay.create<Intent<S>>()

    private val store = intents
            .observeOn(AndroidSchedulers.mainThread())
            .scan(startingState){oldState, intent -> intent.reduce(oldState) }
            .replay(1)
            .apply { connect() }

    /**
     * ModelState is immutable, Processed intents triger modelState _transition_.
     */
    override fun process(intent: Intent<S>) = intents.accept(intent)

    /**
     * new modelState instances are emitted every time when a processed intent triggers a _transition_.
     */
    override fun modelState(): Observable<S> = store

    /**
     * allows us to react to problems within the ModelStore
     */
    private val internalDisposable = store.subscribe(::internalLogger, ::crashHandler)

    private fun internalLogger(state: S) = Timber.i("$state")

    private fun crashHandler(throwable: Throwable): Unit = throw throwable
}


package com.kanawish.sample.mvi.model

import com.jakewharton.rxrelay2.PublishRelay
import com.kanawish.sample.mvi.intent.Intent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * this class is used to store Model instance with current ModelState, which must have a ModelState instance for the current State
 *
 */
open class ModelStore<S> (startingState: S /* a copy of given StateType */ ) : Model<S> {
    private val intents = PublishRelay.create<Intent<S>>()

    private val store = intents
            .observeOn(AndroidSchedulers.mainThread())
            .scan(startingState){oldState, intent ->
                intent.reduce(oldState)  // transits oldState to a newState
            }
            .replay(1)  // convert Observable to Connectable Observable, reply(1) let the subscribers to receive current state and afterwards
            .apply { connect() }  // using connect() to activate the Observable

    /**
     * ModelState is immutable, Processed intents trigger modelState _transition_:
     * which is to apply the intent to current state to create a new state.
     */
    override fun process(intent: Intent<S>) = intents.accept(intent)

    /**
     * new modelState instances are emitted every time when a processed intent triggers a _transition_.
     * modelState(): to broadcast the new states, which means the receiver can be more than one.
     */
    override fun modelState(): Observable<S> = store


    /**
     * allows us to react to problems within the ModelStore
     * to Log each state
     */
    private val internalDisposable = store.subscribe(::internalLogger, ::crashHandler)

    private fun internalLogger(state: S) = Timber.i("$state")

    private fun crashHandler(throwable: Throwable): Unit = throw throwable





}


package com.kanawish.sample.mvi.model

import com.kanawish.sample.mvi.intent.Intent
import io.reactivex.Observable
import io.reactivex.observables.ConnectableObservable


interface Model<S> {
    /**
     * Model will receive intents to be processed via this function
     *
     * ModelState is immutable. Processed intents will work much like 'copy()'
     * and modify some properties of the old state to form a new state.
     */
    fun process(intent: Intent<S>)  // process Intents -- [Model: process(intent)]

    /**
     * Observable stream of changes to ModelState
     *
     * Every time when a modelState is replaced by a new one, this observable will fire.
     * and This is what views will subscribe to.
     */
    fun modelState(): Observable<S> // emit States -- [Model -down-> View: state]
}

/*

    @startuml
     Model: process(intent)
     View: render(state)
     Intent: map(event)

     Model -down-> View: state
     View -right-> Intent: events
     Intent -up-> Model: intents
    @enduml


*/
package com.kanawish.sample.mvi.intent

import com.kanawish.sample.mvi.model.TaskEditorState

interface Intent<T> {
    fun reduce(oldState: T): T
}

/**
 * DSL function to help build intents from code blocks
 *
 * Note: Magic of extension functions: (T) -> T can be changed to T.() -> T
 */
fun <T> intent(block: T.() -> T): Intent<T> = object: Intent<T> {
    override fun reduce(oldState: T): T = block(oldState)
     // as the test example
    // reduce(oldState: TaskEditorState): TaskEditorState = (this as? TaskEditorState.Closed)?.addTask(taskToBeAdded) ?: throw IllegalStateException("Something went wrong")

}

/**
 * By delegating work to other models, repos or services,
 * we end up with situations where we don't need to update our ModelStore state
 * until the delegated work completes
 */
fun <T> sideEffect(block: T.() -> Unit): Intent<T> = object: Intent<T> {
    override fun reduce(oldState: T): T = oldState.apply(block)
}

fun main(){
    // Without DSL function, creating an intent like this
    val stringIntentLong = object: Intent<String>{
        override fun reduce(oldState: String): String {
            return oldState.plus("bar")
        }
    }

    println(stringIntentLong.reduce("foo"))

    // With DSL, creating an intent like this
    val stringIntentFoo = intent<String> { plus("bar") }

    println(stringIntentFoo.reduce("foo"))
}
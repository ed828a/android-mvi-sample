package com.kanawish.sample.mvi.intent

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
    val stringIntentLong = object: Intent<String>{
        override fun reduce(oldState: String): String {
            return oldState.plus("bar")
        }
    }
    println(stringIntentLong.reduce("foo"))

    val stringIntentFoo = intent<String> { plus("bar") }
    println(stringIntentFoo.reduce("foo"))
}
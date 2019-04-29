package com.kanawish.sample.mvi.intent

interface Intent<T> {
    fun reduce(oldstate: T): T
}
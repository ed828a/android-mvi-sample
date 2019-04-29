package com.kanawish.sample.mvi.model

import java.util.*

/**
 * this main like a unit test
 */
fun main(){
    val task = Task(title = "milk", completed = false)
    val updatedTask = task.copy(completed = true)
    println("final result: $updatedTask")
}
/**
 * Task Model
 */
data class Task(
        val id: String = UUID.randomUUID().toString(),
        val lastUpdate: Long = -1,
        val title: String = "New Task",
        val description: String = "",
        val completed: Boolean = false
)

/**
 * Used with the filter selector in the tasks list
 */
enum class FilterType {
    ANY,
    ACTIVE,
    COMPLETE;

    fun filter(task: Task): Boolean {
        return when(this){
            ANY -> true
            ACTIVE -> !task.completed
            COMPLETE -> task.completed
        }
    }
}
/**
 * Tasks Sync State
 */
sealed class SyncState{
    object IDLE: SyncState(){
        override fun toString(): String = "IDLE"
    }

    data class PROCESS(val type: Type): SyncState(){
        enum class Type { REFRESH, CREATE, UPDATE}
    }

    data class ERROR(val throwable: Throwable): SyncState()

}

/**
 * ModelState holds all the states we track in the Model
 */
data class TasksState(
        val tasks: List<Task>,
        val filter: FilterType,
        val syncState: SyncState
){
    fun filteredTasks(): List<Task> = tasks.filter{
        filter.filter(it)
    }

}

/*
    State Diagram for SyncState
    @startuml
    [*] --> IDLE
    IDLE -> PROCESS: refresh

    PROCESS -> IDLE: success
    PROCESS -> ERROR: Failed

    ERROR -> IDLE:reset
    ERROR:throwable
    @enduml


    Class diagram for Tasks
    @startuml
    class Task {
        id: String
        lastUpdate: Long
        title: String
        description: String
        completed: String
    }

    enum SyncState << (S, #FF7700) sealed class >> {
        Idle()
        Process(type)
        Error(details)
    }

    class TasksModelState <<(D, orchild) data class>>{
        tasks:List<Task>
        filter: FilterType
        syncState: SyncState
    }

    enum FilterType {
        ANY
        ACTIVE
        COMPLETE
    }

    TasksModelState*--Task:tasks
    TasksModelState *-- FilterType:filter
    TasksModelState *-- SyncState:syncState

    @enduml

 */
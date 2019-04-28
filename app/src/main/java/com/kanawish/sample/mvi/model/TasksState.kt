package com.kanawish.sample.mvi.model

import java.util.*


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

/*

    Class diagram for Tasks
    @startuml
    class Task {
        id: String
        lastUpdate: Long
        title: String
        description: String
        completed: String
    }

    class TasksModelState <<(D, orchild) data class>>{
        tasks:List<Task>
    }

    TasksModelState*--Task:tasks
    @enduml

 */
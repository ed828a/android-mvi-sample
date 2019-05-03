package com.kanawish.sample.mvi.model

/**
 * This class implements the State Machine
 *
 * elements of the sealed class are the states of the State Machine
 *
 * each element contains its transition functions with the return value is the destination state.
 */
sealed class TaskEditorState{
    object Closed: TaskEditorState(){
        fun addTask(task: Task) = Editing(task, true)
        fun editTask(task: Task) = Editing(task)
    }

    data class Editing(val task: Task, val adding: Boolean = false): TaskEditorState() {
        fun edit(block: Task.() -> Task) = copy(task = task.block())
        fun save() = Saving(task)
        fun delete() = Deleting(task.id)
        fun cancel() = Closed
    }

    data class Deleting(val taskId: String): TaskEditorState() {
        fun deleted() = Closed

    }

    data class Saving(val task: Task): TaskEditorState() {
        fun saved() = Closed
    }

}


/*
    What would be a full State Machine with error handling?

    @startuml
    [*] --> CLOSED
    CLOSED --> EDITING: addTask()
    CLOSED --> EDITING: editTask()

    EDITING: task
    EDITING --> EDITING: edit()

    EDITING -down-> SAVING: save()
    EDITING -down-> DELETING: delete()
    EDITING -left-> CLOSED: cancel()

    DELETING -up-> CLOSED: deleted()
    DELETING --> EDITING: error(msg)
    DELETING: taskId

    SAVING -up-> CLOSED: saved()
    SAVING --> EDITING: error(msg)
    SAVING: task
    @enduml

 */

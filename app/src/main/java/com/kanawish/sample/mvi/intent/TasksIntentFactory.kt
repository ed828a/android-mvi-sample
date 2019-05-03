package com.kanawish.sample.mvi.intent

import com.kanawish.sample.mvi.model.*
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TasksIntentFactory @Inject constructor(
        private val tasksModelStore: TasksModelStore,
        private val taskEditorModelStore: TaskEditorModelStore
) {
    fun process(event: TasksViewEvent){
        tasksModelStore.process(toIntent(event))
    }

    private fun toIntent(viewEvent: TasksViewEvent): Intent<TasksState> {
        return when(viewEvent){
            TasksViewEvent.ClearCompletedClick -> buildClearCompletedIntent()
            TasksViewEvent.FilterTypeClick -> buildCycleFilterIntent()
            TasksViewEvent.NewTaskClick -> buildNewTaskIntent()
            TasksViewEvent.RefreshTasksClick -> TODO()
            TasksViewEvent.RefreshTasksSwipe -> TODO()
            is TasksViewEvent.CompleteTaskClick -> buildCompletetTaskClick(viewEvent)
            is TasksViewEvent.EditTaskClick -> buildEditTaskIntent(viewEvent)
        }
    }

    private fun buildEditTaskIntent(viewEvent: TasksViewEvent.EditTaskClick): Intent<TasksState> {
        // we use 'sideEffect()' here since we're entirely delegating the work
        return sideEffect {
            // we can assert things about the TasksStore state
            assert(tasks.contains(viewEvent.task))

            // Editing a tasj then only involves opening it.
            val intent = AddEditTaskIntentFactory.buildEditTaskIntent(viewEvent.task)
            taskEditorModelStore.process(intent)
        }
    }

    private fun buildCompletetTaskClick(viewEvent: TasksViewEvent.CompleteTaskClick): Intent<TasksState> {
        return intent {
            // We need to operate on the tasks list here
            val mutableList = tasks.toMutableList()
            // Replaces old task in the list with a new updated copy
            mutableList[tasks.indexOf(viewEvent.task)] = viewEvent.task.copy(completed = viewEvent.checked)

            // take the modified list, and create a new copy of tasksState with it
            copy(tasks = mutableList)
        }
    }

    private fun buildCycleFilterIntent(): Intent<TasksState> {
        return intent {
            copy( filter = when (filter){
                FilterType.ANY -> FilterType.ACTIVE
                FilterType.ACTIVE -> FilterType.COMPLETE
                FilterType.COMPLETE -> FilterType.ANY
            })
        }
    }

    private fun buildClearCompletedIntent(): Intent<TasksState> {
        return intent {
            copy(tasks = tasks.filter { !it.completed }.toList())
        }
    }

    private fun buildNewTaskIntent(): Intent<TasksState> = sideEffect {
        val addIntent = AddEditTaskIntentFactory.buildAddTaskIntent(Task())
        taskEditorModelStore.process(addIntent)
    }

    // Getting comfortable with simple DSL-style-builders is valuable in MVI,
    // DSL: Domain Specific Language
    private fun chainedIntent(block: TasksState.() -> TasksState) = tasksModelStore.process(intent(block))

    companion object {
        // Allows an external Model to save a task
        fun buildAddOrUpdateTaskIntent(task: Task): Intent<TasksState> = intent {
            tasks.toMutableList().let{newList ->
                newList.find { task.id == it.id }?.let {
                    newList[newList.indexOf(it)] = task
                } ?: newList.add(task)
                copy (tasks = newList)
            }
        }

        // Allows an external model to delete a task
        fun buildDeleteTaskIntent(taskId: String): Intent<TasksState> = intent {
            copy(tasks = tasks.toMutableList().apply {
                find { it.id == taskId } ?.also { remove(it) }
            })
        }
    }

}
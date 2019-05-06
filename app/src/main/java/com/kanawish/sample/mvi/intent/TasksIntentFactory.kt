package com.kanawish.sample.mvi.intent

import com.kanawish.sample.mvi.model.*
import com.kanawish.sample.mvi.model.backend.TasksRestApi
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.ClearCompletedClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.CompleteTaskClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.EditTaskClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.FilterTypeClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.NewTaskClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.RefreshTasksClick
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent.RefreshTasksSwipe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TasksIntentFactory is responsible for turning TasksViewEvent into
 * Intent<TasksState>, and coordinates with any other dependencies such as
 * ModelStores, Repositories or Services.
 *
 * @see AddEditTaskIntentFactory for state machine safety example.
 */
@Singleton
class TasksIntentFactory @Inject constructor(
        private val tasksModelStore: TasksModelStore,
        private val taskEditorModelStore: TaskEditorModelStore,
        private val tasksRestApi: TasksRestApi
) {
    fun process(event: TasksViewEvent) {
        tasksModelStore.process(toIntent(event))
    }

    private fun toIntent(viewEvent: TasksViewEvent): Intent<TasksState> {
        return when (viewEvent) {
            ClearCompletedClick -> buildClearCompletedIntent()
            FilterTypeClick -> buildCycleFilterIntent()
            RefreshTasksSwipe, RefreshTasksClick -> buildReloadTasksIntent()
            NewTaskClick -> buildNewTaskIntent()
            is CompleteTaskClick -> buildCompleteTaskClick(viewEvent)
            is EditTaskClick -> buildEditTaskIntent(viewEvent)
        }
    }

    // Getting comfortable with simple DSL-style-builders is valuable in MVI,
    // DSL: Domain Specific Language
    private fun chainedIntent(block: TasksState.() -> TasksState) = tasksModelStore.process(intent(block))

    private fun buildReloadTasksIntent(): Intent<TasksState> {
        return intent {
            assert(syncState == SyncState.IDLE)

            fun retrofitSuccess(loadedTasks: List<Task>) = chainedIntent {
                assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)

                copy(tasks = loadedTasks, syncState = (syncState as SyncState.PROCESS).success())
//                copy(tasks = loadedTasks, syncState = SyncState.IDLE)
            }


            fun retrofitError(throwable: Throwable) = chainedIntent {
                assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)
//                copy(syncState = SyncState.ERROR(throwable))
                copy(syncState = (syncState as SyncState.PROCESS).failed())
            }


            val disposable = tasksRestApi.getTasks()
                    .map { it.values.toList() }
                    .subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)

            copy(syncState = SyncState.PROCESS(SyncState.PROCESS.Type.REFRESH, disposable::dispose))
        }

    }

    private fun buildEditTaskIntent(viewEvent: TasksViewEvent.EditTaskClick): Intent<TasksState> {
        // we use 'sideEffect()' here since we're entirely delegating the work
        // the taskListStore won't be changing.
        return sideEffect {
            // we can assert things about the TasksStore state
            assert(tasks.contains(viewEvent.task))

            // Editing a task then only involves opening it.
            val intent = AddEditTaskIntentFactory.buildEditTaskIntent(viewEvent.task)
            taskEditorModelStore.process(intent)
        }
    }

    private fun buildCompleteTaskClick(viewEvent: TasksViewEvent.CompleteTaskClick): Intent<TasksState> {
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
            copy(filter = when (filter) {
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


    companion object {
        // Allows an external Model to save a task
        fun buildAddOrUpdateTaskIntent(task: Task): Intent<TasksState> = intent {
            tasks.toMutableList().let { newList ->
                newList.find { task.id == it.id }?.let {
                    newList[newList.indexOf(it)] = task
                } ?: newList.add(task)
                copy(tasks = newList)
            }
        }

        /** Allows an external model to delete a task. */
        fun buildDeleteTaskIntent(taskId: String): Intent<TasksState> = intent {
            copy(tasks = tasks.toMutableList().apply {
                find { it.id == taskId }?.also { remove(it) }
            })
        }
    }
}

/*
           fun retrofitSuccess(loadedTasks: List<Task>) = tasksModelStore.process(
               intent {
                   assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)
                   copy(tasks = loadedTasks, syncState = SyncState.IDLE)
               }
           )


           fun retrofitError(throwable: Throwable) = tasksModelStore.process(
                   intent {
                       assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)
                       copy(syncState = SyncState.ERROR(throwable))
                   }
           )
           */
package com.kanawish.sample.mvi.intent

import com.kanawish.sample.mvi.model.*
import com.kanawish.sample.mvi.model.TaskEditorState.Closed.editTask
import com.kanawish.sample.mvi.view.addedittask.AddEditTaskViewEvent
import com.kanawish.sample.mvi.view.addedittask.AddEditTaskViewEvent.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AddEditTaskIntentFactory is responsible for turning AddEditTaskViewEvents into Intent<TaskEditorState>,
 * and coordinates with any other dependencies, such as ModelStores, Repositories or Services.
 */

@Singleton
class AddEditTaskIntentFactory @Inject constructor(
        private val taskEditorModelStore: TaskEditorModelStore,
        private val tasksModelStore: TasksModelStore){

    fun process(viewEvent: AddEditTaskViewEvent){
        taskEditorModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: AddEditTaskViewEvent): Intent<TaskEditorState> {
        return when (viewEvent){
            is TitleChange -> buildEditTitleIntent(viewEvent)
            is DescriptionChanged -> buildEditDescriptionIntent(viewEvent)
            SaveTaskClick -> buildSaveIntent()
            DeleteTaskClick -> buildDeleteIntent()
            CancelTaskClick -> buildCancelIntent()
        }
    }

    private fun buildDeleteIntent(): Intent<TaskEditorState> = editorIntent<TaskEditorState.Editing> {
        delete().apply {
            // 'TasksStore' deletes this task from its internal list.
            val intent = TasksIntentFactory.buildDeleteTaskIntent(taskId)
            tasksModelStore.process(intent)
            deleted()
        }
    }

    /**
     * an example of delegation work to an external dependency.
     */
    private fun buildSaveIntent(): Intent<TaskEditorState> = editorIntent<TaskEditorState.Editing> {
        // This triggers a state change in another ModelStore
        save().run {
            // Note: When we do this with a real backend + retrofit, it will become asynchronous
            val intent = TasksIntentFactory.buildAddOrUpdateTaskIntent(task)
            tasksModelStore.process(intent)
            saved()
        }

    }


    companion object {
        /**
         * Creates an intent for the TaskEditor State Machine
         */
        inline fun <reified S: TaskEditorState> editorIntent (
                crossinline block: S.() -> TaskEditorState
        ): Intent<TaskEditorState>{
            return intent {
                (this as? S)?.block()
                        ?: throw IllegalStateException("editorIntent encountered an inconsistent State. $this")
            }
        }

        private fun buildEditTitleIntent(viewEvent: TitleChange): Intent<TaskEditorState> =
                editorIntent<TaskEditorState.Editing> { edit { copy(title = viewEvent.title) } }

        private fun buildEditDescriptionIntent(viewEvent: DescriptionChanged): Intent<TaskEditorState> =
                editorIntent<TaskEditorState.Editing> { edit { copy(description = viewEvent.description) } }

        private fun buildCancelIntent() = editorIntent<TaskEditorState.Editing> { cancel() }

        fun buildAddTaskIntent(task: Task): Intent<TaskEditorState> = editorIntent<TaskEditorState.Closed> { addTask(task) }

        fun buildEditTaskIntent(task: Task): Intent<TaskEditorState> = editorIntent<TaskEditorState.Editing> { editTask(task) }
    }
}
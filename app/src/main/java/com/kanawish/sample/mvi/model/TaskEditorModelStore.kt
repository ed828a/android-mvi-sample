package com.kanawish.sample.mvi.model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskEditorModelStore @Inject constructor(): ModelStore<TaskEditorState>(
        // Todo: change back initial state to CLOSED for final version. right now just for testing
//        TaskEditorState.Editing(Task(title = "Edward", description = "At Home"), true)
                TaskEditorState.Closed
)
package com.kanawish.sample.mvi.model

import javax.inject.Inject
import javax.inject.Singleton

/**
 * to store TasksState Model
 */
@Singleton
class TasksModelStore @Inject constructor() : ModelStore<TasksState>(
        TasksState(
                listOf(Task(title= "One"), Task(title = "Two"), Task(title = "Three")),
                FilterType.ANY,
                SyncState.IDLE
        )
)
package com.kanawish.sample.mvi.view.addedittask

sealed class AddEditTaskViewEvent {
    data class TitleChange(val title: String): AddEditTaskViewEvent()
    data class DescriptionChanged(val description: String): AddEditTaskViewEvent()
    // because below events don't carry data, we use object to make those events as singletons
    object SaveTaskClick: AddEditTaskViewEvent()
    object DeleteTaskClick: AddEditTaskViewEvent()
    object CancelTaskClick: AddEditTaskViewEvent()
}
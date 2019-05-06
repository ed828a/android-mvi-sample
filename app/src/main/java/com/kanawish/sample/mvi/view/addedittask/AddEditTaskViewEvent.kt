package com.kanawish.sample.mvi.view.addedittask


/**
 * Events can be defined in a sealed class.
 * for events without data, we can define them as object for each.
 * for events with data, we define them as data class.
 * it's same rules for State Machine definition
 *
 * to generate those events, go to respective Activity and Fragment, and us RxBinding, merge, map to generate them
 */
sealed class AddEditTaskViewEvent {
    data class TitleChange(val title: String): AddEditTaskViewEvent()
    data class DescriptionChange(val description: String): AddEditTaskViewEvent()
    // because below events don't carry data, we use object to make those events as singletons
    object SaveTaskClick: AddEditTaskViewEvent()
    object DeleteTaskClick: AddEditTaskViewEvent()
    object CancelTaskClick: AddEditTaskViewEvent()
}
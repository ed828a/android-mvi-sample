package com.kanawish.sample.mvi.view.tasks

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.kanawish.sample.mvi.model.Task
import com.kanawish.sample.mvi.view.EventObservable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.task_item.view.*
import timber.log.Timber


/**
 * Note: viewContract is really more a 'documenting' interface in this app
 *
 * In the interest of simplicity, we drop the 'subscriptionView()' side,
 * and only bind on the tasks list updates
 *
 * you could imagine scenarios where a ViewHolder points to a "live" source of data.
 * In those cases, binding an Observable would be a good approach.
 */
class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), EventObservable<TasksViewEvent> {

    private lateinit var currentTask: Task

    fun bind(task: Task){
        currentTask = task
        itemView.title.text = task.title
        itemView.completeCheckBox.isChecked = task.completed
    }

    override fun events(): Observable<TasksViewEvent> {
        return Observable.merge(
                itemView.completeCheckBox.checkedChanges()
                        .skipInitialValue()
                        .map { checked ->
                            TasksViewEvent.CompleteTaskClick(currentTask, checked)
                        },

                itemView.title.clicks().map {
                    Timber.i("${itemView.title.text} clicked")
                    TasksViewEvent.EditTaskClick(currentTask)
                }
        )
    }
}
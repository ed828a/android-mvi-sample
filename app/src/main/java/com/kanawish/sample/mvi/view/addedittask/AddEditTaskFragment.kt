package com.kanawish.sample.mvi.view.addedittask

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.widget.textChanges
import com.kanawish.sample.mvi.R
import com.kanawish.sample.mvi.intent.AddEditTaskIntentFactory
import com.kanawish.sample.mvi.model.TaskEditorModelStore
import com.kanawish.sample.mvi.model.TaskEditorState
import com.kanawish.sample.mvi.view.EventObservable
import com.kanawish.sample.mvi.view.StateSubscriber
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.addtask_frag.*
import javax.inject.Inject

/**
 * Fragment for adding/editing tasks.
 */
class AddEditTaskFragment : Fragment(),
        StateSubscriber<TaskEditorState>,
        EventObservable<AddEditTaskViewEvent> {

    @Inject lateinit var editorModelStore: TaskEditorModelStore
    @Inject lateinit var intentFactory: AddEditTaskIntentFactory

    private val disposables = CompositeDisposable()

    override fun events(): Observable<AddEditTaskViewEvent> {
        return Observable.merge(
                add_task_title.textChanges().map { AddEditTaskViewEvent.TitleChange(it.toString()) },
                add_task_description.textChanges().map { AddEditTaskViewEvent.DescriptionChange(it.toString()) }
        )
    }

    override fun Observable<TaskEditorState>.subscribeToState(): Disposable {
        // return ofType(TaskEditorState.Editing::class.java).firstElement().subscribe {editing
        return ofType<TaskEditorState.Editing>().firstElement().subscribe {editing ->
            add_task_title.setText(editing.task.title)
            add_task_description.setText(editing.task.description)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.addtask_frag, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.addtask_fragment_menu, menu)
    }

    override fun onResume() {
        super.onResume()
        disposables += editorModelStore.modelState().subscribeToState()
        disposables += events().subscribe(intentFactory::process)
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}
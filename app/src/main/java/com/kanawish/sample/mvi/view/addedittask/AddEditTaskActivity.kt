package com.kanawish.sample.mvi.view.addedittask

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.support.v7.widget.itemClicks
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import com.kanawish.sample.mvi.R
import com.kanawish.sample.mvi.intent.AddEditTaskIntentFactory
import com.kanawish.sample.mvi.model.TaskEditorModelStore
import com.kanawish.sample.mvi.model.TaskEditorState
import com.kanawish.sample.mvi.util.replaceFragmentInActivity
import com.kanawish.sample.mvi.util.setupActionBar
import com.kanawish.sample.mvi.view.EventObservable
import com.kanawish.sample.mvi.view.StateSubscriber
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.addtask_act.*
import timber.log.Timber
import javax.inject.Inject


/**
 * Activity houses the Toolbar, a FAB and the fragment for adding/editing tasks.
 */
class AddEditTaskActivity : AppCompatActivity(),
        StateSubscriber<TaskEditorState>,
        EventObservable<AddEditTaskViewEvent> {

    @Inject lateinit var editorModelStore: TaskEditorModelStore
    @Inject lateinit var intentFactory: AddEditTaskIntentFactory

    /**
     * NOTE: Relays are useful when RxBinding doesn't expose a binding for a widget
     */
    private val navigateUpRelay = PublishRelay.create<AddEditTaskViewEvent.CancelTaskClick>()

    private val disposables = CompositeDisposable()

    override fun Observable<TaskEditorState>.subscribeToState(): Disposable {
        return CompositeDisposable().also { innerdisposables ->
            // logging
            innerdisposables += subscribe { Timber.i("$it")}
            // Reactive UX
            innerdisposables += subscribe {
                when(it){
                    is TaskEditorState.Editing -> {
                        fab_edit_task_done.isEnabled = true
                        busy.visibility = View.GONE
                    }

                    is TaskEditorState.Saving, is TaskEditorState.Deleting -> {
                        invalidateOptionsMenu()
                        fab_edit_task_done.isEnabled = true
                        busy.visibility = View.VISIBLE
                    }

                    TaskEditorState.Closed -> { onBackPressed() }
                }
            }

        }
    }

    /**
     * On android, not everything is a straightforward click listener.
     */
    override fun events(): Observable<AddEditTaskViewEvent> {
        return Observable.merge(
                toolbar.itemClicks()
                        .filter { it.itemId == R.id.menu_delete }
                        .map { AddEditTaskViewEvent.DeleteTaskClick },
                fab_edit_task_done.clicks().map { AddEditTaskViewEvent.SaveTaskClick },
                navigateUpRelay
        )
    }

    /**
     * Note: when dealing with menus and such, we need "lifecycle flexibility".
     *
     * Here, 'invalidateOptionsMenu()' is triggered from out long-lived subscription above
     * and we are a one-time trigger since we use the 'firstElement()' operator below.
     *
     * If you breakpoint throught the code below, you should see that the subscription resolves synchronously, not asynchronously.
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        disposables += editorModelStore.modelState()
                .firstElement()
                .subscribe { state ->
                    menu?.let {
                        it.findItem(R.id.menu_delete).apply {
                            val itemEnabled = state is TaskEditorState.Editing && !state.adding
                            isVisible = itemEnabled
                            isEnabled = itemEnabled
                        }
                    }
                }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        fab_edit_task_done?.apply {
            setImageResource(R.drawable.ic_done)
        }

        supportFragmentManager.findFragmentById(R.id.contentFrame) as AddEditTaskFragment?
                ?: AddEditTaskFragment().also {
                    replaceFragmentInActivity(it, R.id.contentFrame)
                }

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

    /**
     * this function called in AppCompatActivity.java, on onMenuSelected(int featureId, android.view.MenuItem item)
     * and itemId = android.R.id.home
     */
    override fun onSupportNavigateUp(): Boolean {
        // it was onBackPressed() before
        navigateUpRelay.accept(AddEditTaskViewEvent.CancelTaskClick)
        return true
    }
}
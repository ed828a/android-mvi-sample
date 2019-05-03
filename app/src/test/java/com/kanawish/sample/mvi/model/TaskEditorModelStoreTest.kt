package com.kanawish.sample.mvi.model

import com.kanawish.sample.mvi.intent.AddEditTaskIntentFactory.Companion.editorIntent
import com.kanawish.sample.mvi.util.ReplaceMainThreadSchedulerRule
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import toothpick.testing.ToothPickRule
import javax.inject.Inject

class TaskEditorModelStoreTest {
    // swaps out AndroidSchedulers.mainThread() for trampoline scheduler
    @get:Rule val schedulerRule = ReplaceMainThreadSchedulerRule()

    // Injects any @Mock reference properties in this test class
    @get:Rule val mockitoRule: MockitoRule = MockitoJUnit.rule()

    // Uses @Mock as dependencies for injections, and resets ToothPick at the end of each test
    @get:Rule val toothPickRule = ToothPickRule(this, this)

    // Instance under test
    @Inject
    lateinit var taskEditorModelStore: TaskEditorModelStore

    @Before
    fun setup() {
        toothPickRule.inject(this)
    }

    @Test
    fun startingState(){
        val testObserver = TestObserver<TaskEditorState>()

        taskEditorModelStore.modelState().subscribe(testObserver)

        testObserver.assertValue(TaskEditorState.Closed)
        testObserver.assertNoErrors() // Checks that the observable hasn't raised exceptions/errors
    }

    @Test
    fun validTransitions(){
        val testObserver = TestObserver<TaskEditorState>()
        val taskToBeAdded = Task(id = "TEST_SUCCESS")
//        val addIntent: Intent<TaskEditorState> = intent {
//            (this as? TaskEditorState.Closed)?.addTask(taskToBeAdded) ?: throw IllegalStateException("Something went wrong")
//        }

        val addIntent = editorIntent<TaskEditorState.Closed> { addTask(taskToBeAdded) }

        taskEditorModelStore.process(addIntent)
        taskEditorModelStore.modelState().subscribe(testObserver)

        testObserver.assertValueCount(1)
        testObserver.values().last().let {
            when(it){
                is TaskEditorState.Editing -> assert(it.task == taskToBeAdded){
                    "Edited task doesn't match what was added in."
                }

                else -> assert(false){
                    "Expected type 'Editing', was $it"
                }
            }
        }

        val cancelIntent = editorIntent<TaskEditorState.Editing> { cancel() }
        taskEditorModelStore.process(cancelIntent)

        testObserver.assertValueCount(2)
        testObserver.values().last().let {
            assert(it is TaskEditorState.Closed) { "Expected type 'Closed', was $it" }
        }

        testObserver.assertNoErrors()
    }

    @Test
    fun invalidTransitions(){
        val testObserver = TestObserver<TaskEditorState>()
        val taskToBeAdded = Task(id = "TEST_SUCCESS")
//        val invalidIntent: Intent<TaskEditorState> = intent {
//            (this as? TaskEditorState.Editing)?.edit {
//                copy(title = "This intent is invalid and won't reduce.")
//            } ?: throw IllegalStateException("Something happened.")
//        }

        val invalidIntent = editorIntent<TaskEditorState.Editing>{
            edit {
                copy(title = "This is invalid and won't reduce.")
            }
        }

        taskEditorModelStore.modelState().subscribe(testObserver)
        taskEditorModelStore.process(invalidIntent)

        testObserver.assertValueCount(1)
        testObserver.assertValue(TaskEditorState.Closed)
        testObserver.assertError(IllegalStateException::class.java)
    }
}
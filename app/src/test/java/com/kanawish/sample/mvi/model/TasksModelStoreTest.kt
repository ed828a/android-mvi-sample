package com.kanawish.sample.mvi.model

import com.kanawish.sample.mvi.intent.intent
import com.kanawish.sample.mvi.util.ReplaceMainThreadSchedulerRule
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import toothpick.testing.ToothPickRule
import javax.inject.Inject

class TasksModelStoreTest {

    // swap out AndroidSchedulers.mainThread() for trampoline scheduler
    @get:Rule val schedulerRule = ReplaceMainThreadSchedulerRule()

    // Injects any @Mock reference properties in this test class.
    @get:Rule val mockitoRule:MockitoRule = MockitoJUnit.rule()

    // use @Mock as dependencies for injection, and resets Toothpick at the end of each test.
    @get:Rule val toothPickRule = ToothPickRule(this, this)

    // Instance under test.
    @Inject lateinit var tasksModelStore: TasksModelStore

    @Before
    fun setup(){
        toothPickRule.inject(this)  //  to build all annotated @Inject variables' instance, for example, tasksModelStore.
    }

    @Test
    fun startingState(){
        val testObserver = TestObserver<TasksState>()

        tasksModelStore.modelState().subscribe(testObserver)

        testObserver.assertValue(
                TasksState(
                        emptyList(),
                        FilterType.ANY,
                        SyncState.IDLE
                )
        )
    }

    @Test
    fun changes() {
        val testObserver = TestObserver<TasksState>()

        // Process a mock intent
        tasksModelStore.process(intent {
            // simulates a pull to refresh
            copy(syncState = SyncState.PROCESS(SyncState.PROCESS.Type.REFRESH) {})
        })

        //we subscribe after this to validate replay works correctly.
        tasksModelStore.modelState().subscribe(testObserver)

        // expected stated when refresh is running after mock intent above
        testObserver.assertValueCount(1)
        testObserver.values().last().let {
            assert(it.filter == FilterType.ANY)
            assert(it.syncState == SyncState.PROCESS(SyncState.PROCESS.Type.REFRESH) {})
        }

        // simulate a successful refresh call, that return 1 task.
        tasksModelStore.process(intent {
            copy(tasks = listOf(Task()), syncState = SyncState.IDLE)
        })
        // validate expectations
        testObserver.assertValueCount(2)
        testObserver.values().last().let {
            assert(it.tasks.size == 1)
            assert(it.filter == FilterType.ANY)
            assert(it.syncState == SyncState.IDLE)
        }

    }
}
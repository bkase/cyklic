package com.bkase.cyklic.example

import android.os.Parcelable
import com.bkase.cyklic.RamState
import com.bkase.cyklic.components.Component
import com.bkase.cyklic.components.StartStopComponent
import com.bkase.cyklic.drivers.ViewDriver
import rx.Observable

/**
 * Created by bkase on 3/26/16.
 */

object SimpleCounter {
  data class ViewIntentions(
      val increment: Observable<Unit>,
      val decrement: Observable<Unit>
  )

  object Model {
    data class State(override val ram: Int): RamState<Any, Int, Parcelable>

    val initialState = State(0)
    val createState: (Any?, Int?, Parcelable?) -> State = { a, b, c -> State(b!!) }
    val model: (ViewIntentions) -> Observable<(State) -> State> = { intentions ->
      val ups: Observable<(State) -> State> = intentions.increment.map{ unit ->
        { state: State -> State(state.ram+1) }
      }
      val downs: Observable<(State) -> State> = intentions.decrement.map{ unit ->
        { state: State -> State(state.ram-1) }
      }

      Observable.merge(ups, downs)
    }
  }

  object ViewModel {
    data class State(val counterCount: Int)

    // trivial viewmodel
    val viewModel: (Observable<Model.State>) -> Observable<ViewModel.State> = { stateStream ->
      stateStream.map{ modelState -> State(modelState.ram) }
    }
  }

  interface Viewish {
    fun setCount(count: Int)
  }

  class SimpleCounterComponent(
      viewIntentions: ViewIntentions,
      val viewish: Viewish
  ): StartStopComponent by Component(
      driver = ViewDriver<ViewIntentions, ViewModel.State>(
          intention = viewIntentions,
          onViewState = { old, curr ->
            if (old?.counterCount != curr.counterCount) {
              viewish.setCount(curr.counterCount)
            }
          }
      ),
      model = ViewDriver.makeModel(
          initialState = Model.initialState,
          createState = Model.createState,
          model = Model.model,
          viewModel = ViewModel.viewModel
      )
  )
}

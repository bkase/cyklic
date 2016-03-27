package com.bkase.cyklic.drivers

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.bkase.cyklic.ModelState
import com.bkase.cyklic.slidingPair
import rx.Observable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by bkase on 3/26/16.
 */

data class WithKey<T>(val data: T, val key: String)

class ViewDriver<ViewIntention, ViewState: Any>(
    private val intention: ViewIntention,
    // TODO: Figure out how to make this not a var
    var onViewState: (ViewState?, ViewState) -> Unit
): Driver<ViewIntention, Pair<ViewState?, ViewState>> {
  companion object {
    fun <ViewIntention, ViewState: Any, State: ModelState<N, P, C>, N: Any, P: Any, C: Parcelable> makeModel(
        initialState: State,
        createState: (N?, P?, C?) -> State,
        model: (ViewIntention) -> Observable<(State)->State>,
        viewModel: (Observable<State>) -> Observable<ViewState>
    ): (ViewIntention) -> Observable<Pair<ViewState?, ViewState>> =
        makeSavedInstanceModel(initialState, createState, model, viewModel, null, Observable.never())

    // TODO: How to not leak onSaveInstanceState subscription
    fun <ViewIntention, ViewState: Any, State: ModelState<N,P,C>, N: Any, P: Any, C: Parcelable> makeSavedInstanceModel(
        initialState: State,
        createState: (N?, P?, C?) -> State,
        model: (ViewIntention) -> Observable<(State)->State>,
        viewModel: (Observable<State>) -> Observable<ViewState>,
        savedInstanceState: WithKey<Bundle>?,
        onSaveInstanceState: Observable<Bundle>
    ): (ViewIntention) -> Observable<Pair<ViewState?, ViewState>> =
        object: Function1<ViewIntention, Observable<Pair<ViewState?, ViewState>>> {
          // I think there isn't a race here since we only READ when we're not writing
          // but can Java re-order this?
          private var currentRam: P? = null

          private val lock = ReentrantLock()
          public var diskState: WithKey<C>? = null
            get() = lock.withLock { field }
            set(newValue) { lock.withLock { field = newValue } }

          override fun invoke(v: ViewIntention): Observable<Pair<ViewState?, ViewState>> {
            if (savedInstanceState != null) {
              onSaveInstanceState.subscribe{ b ->
                diskState?.let{ b.putParcelable(it.key, it.data) }
              }
            }

            val startState = createState(
                initialState.volatile,
                currentRam ?: initialState.ram,
                diskState?.data ?: savedInstanceState?.let {
                  it.data.getParcelable<C>(it.key)
                } ?: initialState.disk
            )

            val state =
                model(v).scan(startState, { currState: State, t ->
                  t(currState) }).distinctUntilChanged()
            return viewModel(
                state.doOnNext { s ->
                  savedInstanceState?.let { savedInstanceState ->
                    s.disk?.let { disk ->
                      diskState = WithKey(disk, savedInstanceState.key)
                    }
                  }
                  currentRam = s.ram
                }
            )
                .distinctUntilChanged()
                .slidingPair()
          }
        }
  }

  override val input: ViewIntention = intention

  override fun output(o: Observable<Pair<ViewState?, ViewState>>) =
      o.subscribe{ onViewState(it.first, it.second) }
}

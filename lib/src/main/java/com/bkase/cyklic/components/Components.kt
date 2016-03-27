package com.bkase.cyklic.components

import com.bkase.cyklic.drivers.Driver
import rx.Observable
import rx.subscriptions.CompositeSubscription

/**
 * Created by bkase on 3/26/16.
 */

interface StartStopComponent {
  fun start()
  fun stop()
}

class Component<T, U, D1: Driver<T, U>>(
    val driver: D1,
    private val model: (T) -> Observable<U>
): StartStopComponent {
  private var subs = CompositeSubscription()

  override fun start() {
    if (subs.hasSubscriptions()) {
      //      Log.w(this.javaClass.getName(), "You're trying to start me, but I already have subs!")
      return
    }
    subs.add(driver.output(model(driver.input)))
  }

  override fun stop() {
    subs.unsubscribe()
    subs = CompositeSubscription()
  }
}

// TODO: This design doesn't scale to components that depend on N drivers
data class ObservablePair<U1, U2>(val out1: Observable<U1>, val out2: Observable<U2>)
class Component2<T1, U1, D1: Driver<T1, U1>, T2, U2, D2: Driver<T2, U2>>(
    val driver1: D1,
    val driver2: D2,
    private val model: (T1, T2) -> ObservablePair<U1, U2>
): StartStopComponent {
  private var subs = CompositeSubscription()

  override fun start() {
    val (out1, out2) = model(driver1.input, driver2.input)
    subs.add(driver1.output(out1))
    subs.add(driver2.output(out2))
  }

  override fun stop() {
    subs.unsubscribe()
    subs = CompositeSubscription()
  }
}

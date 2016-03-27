package com.bkase.cyklic.components

import com.bkase.cyklic.LifecycleEvent
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * Created by bkase on 3/26/16.
 */

class LifeCycleComponent<C: StartStopComponent>(
    private val pauseResumeCycle: Observable<LifecycleEvent>,
    val inner: C
): StartStopComponent {
  val isStarted: Boolean
    get() = _isStarted

  private var _isStarted: Boolean = false
  private var didPause: Boolean = false

  private var sub: Subscription? = null

  private fun restartLifecycle() {
    sub = pauseResumeCycle.observeOn(AndroidSchedulers.mainThread()).subscribe{
      if (it == LifecycleEvent.PAUSE) {
        didPause = true
        //        Log.d(javaClass<LifeCycleComponent<C>>().getName(),"$num: Pause hit and taking")
        stop_()
      } else if (it == LifecycleEvent.RESUME && didPause) {
        didPause = false
        //        Log.d(javaClass<LifeCycleComponent<C>>().getName(), "$num: Resume hit and taking")
        start_()
      }
    }
  }

  private fun start_() {
    _isStarted = true
    // Log.d(javaClass<LifeCycleComponent<C>>().getName(), "$num Starting... [${Thread.currentThread()}]")
    inner.start()
  }

  override fun start() {
    start_()
    restartLifecycle()
  }

  private fun stop_() {
    inner.stop()
    // Log.d(javaClass<LifeCycleComponent<C>>().getName(), "$num Stopping... [${Thread.currentThread()}]")
    _isStarted = false
  }

  override fun stop() {
    stop_()
    sub?.unsubscribe()
    sub = null
  }
}


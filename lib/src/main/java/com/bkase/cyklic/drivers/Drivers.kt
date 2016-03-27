package com.bkase.cyklic.drivers

import android.content.Context
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject

/**
 * Created by bkase on 3/26/16.
 */

interface Driver<T, U> {
  val input: T
  fun output(o: Observable<U>): Subscription
}

object Drivers {
  fun <T> patch(o: Observable<T>, subj: Observer<T>): Subscription =
      o.subscribe{ subj.onNext(it) }
}


package com.bkase.cyklic.drivers

import android.content.Context
import android.preference.PreferenceManager
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject

/**
 * Created by bkase on 3/26/16.
 */

abstract class PrefDriver<V>(
    private val ctx: Context,
    private val name: String
): Driver<Observable<V>, V> {
  private val subj = PublishSubject.create<V>()

  abstract fun getValue(ctx: Context, name: String): V
  abstract fun putValue(ctx: Context, name: String, value: V): Unit

  override val input: Observable<V> =
      subj.startWith(getValue(ctx, name))

  override fun output(o: Observable<V>): Subscription =
      Drivers.patch(o.doOnNext{ putValue(ctx, name, it) }, subj)
}

class LongPrefDriver(
    private val ctx: Context,
    private val name: String
): PrefDriver<Long>(ctx, name) {
  override fun getValue(ctx: Context, name: String): Long =
    PreferenceManager.getDefaultSharedPreferences(ctx).getLong(name, 0L)

  override fun putValue(ctx: Context, name: String, value: Long) {
    PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong(name, value).apply()
  }
}


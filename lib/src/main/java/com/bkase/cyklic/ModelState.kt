package com.bkase.cyklic

import android.os.Parcelable

/**
 * Created by bkase on 3/26/16.
 */

// TODO: Is (null, null, null) a valid initial state? It means NO state
interface ModelState<N: Any, P: Any, C: Parcelable> {
  val volatile: N?
    get() = null
  val ram: P?
    get() = null
  val disk: C?
    get() = null
}

interface VolatileState<N: Any, P: Any, C: Parcelable>: ModelState<N, P, C> {
  override val volatile: N
}

interface RamState<N: Any, P: Any, C: Parcelable>: ModelState<N, P, C> {
  override val ram: P
}

interface DiskState<N: Any, P: Any, C: Parcelable>: ModelState<N, P, C> {
  override val disk: C
}

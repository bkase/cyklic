package com.bkase.cyklic

/**
 * Created by bkase on 3/26/16.
 */

/**
 * For use in an observable stream
 *
 * Fire X when activity/fragment/etc onX() is called
 *
 * ex.)
 *      onResume() { subject.onNext(LifecycleEvent.RESUME) }
 */
enum class LifecycleEvent {
    RESUME,
    PAUSE,
    STOP,
    DESTROY
}


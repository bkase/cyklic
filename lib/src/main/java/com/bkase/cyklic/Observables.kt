package com.bkase.cyklic

import rx.Observable

/**
 * Created by bkase on 3/26/16.
 */

fun <T: Any> Observable<T>.slidingPair(): Observable<Pair<T?, T>> =
        this.scan(null, { b: Pair<T?, T>?, elem: T -> b?.second to elem })
                .filter{ it != null }
                .map{ it!! }

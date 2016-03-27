package com.bkase.cyklic

import android.test.InstrumentationTestCase
import com.bkase.cyklic.example.SimpleCounter
import rx.subjects.PublishSubject

/**
 * Created by bkase on 3/26/16.
 */

class TestView: SimpleCounter.Viewish {
  private var _count: Int = 0

  val count: Int
    get() = _count

  override fun setCount(count: Int) {
    _count = count
  }
}

class ComponentTest: InstrumentationTestCase() {
  enum class Direction {
    INC, DEC
  }
  data class Command(val direction: Direction, val amount: Int)

  fun testTest() {
  }

  private fun tapNTimes(n: Int, where: PublishSubject<Unit>) {
    0.until(n).forEach { where.onNext(Unit) }
  }

  private fun exec(
      startCount: Int,
      commands: List<Command>,
      tapUps: PublishSubject<Unit>,
      tapDowns: PublishSubject<Unit>,
      view: TestView
  ): Int {
    var currCount = startCount
    commands.forEach {
      when (it.direction) {
        Direction.INC -> {
          tapNTimes(it.amount, tapUps)
          currCount += it.amount
          assertEquals(currCount, view.count)
        }
        Direction.DEC -> {
          tapNTimes(it.amount, tapDowns)
          currCount -= it.amount
          assertEquals(currCount, view.count)
        }
      }
    }
    return currCount
  }

  fun testComponent() {
    val fakeIncrementTaps = PublishSubject.create<Unit>()
    val fakeDecrementTaps = PublishSubject.create<Unit>()
    val view = TestView()
    val component = SimpleCounter.SimpleCounterComponent(SimpleCounter.ViewIntentions(
        increment = fakeIncrementTaps,
        decrement = fakeDecrementTaps
    ), view)

    component.start()
    // zero at start
    assertEquals(0, view.count)

    // tap on a bunch of increments and decrements
    // and make sure the component updates the underlying view
    val newCount = exec(0, listOf(
        Command(Direction.INC, 4),
        Command(Direction.DEC, 2),
        Command(Direction.INC, 3)
    ), fakeIncrementTaps, fakeDecrementTaps, view)

    component.stop()

    // when the component is stopped, view updates don't occur
    assertEquals(newCount, view.count)
    tapNTimes(2, fakeIncrementTaps)
    assertEquals(newCount, view.count)

    component.start()

    // when the component is restarted, since we used Ram state
    // the state should be there
    exec(newCount, listOf(
        Command(Direction.INC, 3),
        Command(Direction.DEC, 1)
    ), fakeIncrementTaps, fakeDecrementTaps, view)

    component.stop()
  }
}

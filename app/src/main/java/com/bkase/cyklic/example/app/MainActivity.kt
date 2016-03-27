package com.bkase.cyklic.example.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.bkase.cyklic.example.SimpleCounter
import com.bkase.cyklic.example.app.R
import com.jakewharton.rxbinding.view.clicks

class MainActivity : AppCompatActivity() {

  val incButton by lazy {
    findViewById(R.id.increment) as Button
  }

  val decButton by lazy {
    findViewById(R.id.decrement) as Button
  }

  val countView by lazy {
    findViewById(R.id.count) as TextView
  }

  val component by lazy {
    val viewish = object: SimpleCounter.Viewish {
      override fun setCount(count: Int) {
        countView.text = count.toString()
      }
    }

    SimpleCounter.SimpleCounterComponent(
        SimpleCounter.ViewIntentions(
            incButton.clicks(),
            decButton.clicks()
        ), viewish
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar = findViewById(R.id.toolbar) as Toolbar?
    setSupportActionBar(toolbar)
  }

  override fun onResume() {
    super.onResume()

    component.start()
  }

  override fun onPause() {
    component.stop()

    super.onPause()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }
}

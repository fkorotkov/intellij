package test

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class MainTest {

  @Test
  fun testGetNoGreetings() = runBlocking {
    val num = 2

    for (i in 0 until num) {
      launch {
        delay(1000L * (i + 1))
        println("Hello from Coroutine $i")
      }
    }

    // this should be printed before any of the coroutines output their messages
    println("Hello from Main coroutine")
  }
}

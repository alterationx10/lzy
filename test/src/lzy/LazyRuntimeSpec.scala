package lzy

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.language.postfixOps
import scala.util.*

import munit.*

class LazyRuntimeSpec extends FunSuite {
  given ExecutionContext = LazyRuntime.executionContext

  test("LazyRuntime.runSync() captures the Future failure in the Try") {
    val lazyFail = Lazy.fail(new ArithmeticException("bad math"))
    assert(
      LazyRuntime.runSync(lazyFail) match {
        case Failure(_: ArithmeticException) => true
        case _                               => false
      }
    )
    val lazyBoom = Lazy.fn(throw new ArithmeticException("bad math"))
    assert(
      LazyRuntime.runSync(lazyBoom) match {
        case Failure(_: ArithmeticException) => true
        case _                               => false
      }
    )
  }

  test(
    "LazyRuntime.runSync() times out when appropriate"
  ) {
    assert(
      LazyRuntime.runSync(
        Lazy.sleep(100 milliseconds),
        1 milliseconds
      ) match {
        case Failure(_: TimeoutException) => true
        case _                            => false
      }
    )
  }

  test(
    "LazyRuntime.runSync() doesn't time out when appropriate"
  ) {
    assert(
      LazyRuntime
        .runSync(
          Lazy.sleep(5 milliseconds),
          10 milliseconds
        )
        .isSuccess
    )
  }
}

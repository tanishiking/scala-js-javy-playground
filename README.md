# scala-js-javy-playground

- [bytecodealliance/javy: JS to WebAssembly toolchain](https://github.com/bytecodealliance/javy)
- [scala-js/scala-js: Scala.js, the Scala to JavaScript compiler](https://github.com/scala-js/scala-js)

Scala --(scala.js)--> JS --(javy)--> WASM

## Hello World

```scala
// Hello.scala
import scala.scalajs.js
import java.lang.Throwable

object Hello:
  def main(args: Array[String]): Unit =
    val console = js.Dynamic.global.console
    try
      throw new Error("test")
    catch
      case e: Throwable => console.log(e.getMessage)
```

```sh
$ scala-cli package --js Hello.scala -o build/hello.js --force

$ javy compile build/hello.js -o destination/hello.wasm

$ wasmtime destination/hello.wasm
Hello World from Scala.js
java.lang.Error: test
```


## Event loop is not supported
[The event loop - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Event_loop)

> Similarly, we haven’t enabled the event loop in the QuickJS instance that Javy uses. That means that async/await, Promises, and functions like setTimeout are syntactically available but never trigger their callbacks. We want to enable this functionality in Javy, but have to clear up a couple of open questions, like if and how to integrate the event loop with the host system or how to implement setTimeout() from inside a WASI environment.

> With all of this in place, we’ve built a JavaScript runtime that behaves very similarly to other runtimes out there. We can generate WASI-compatible Wasm modules from JavaScript and even split them into a static engine part and a dynamic user code part. This forms the foundation of how we brought JavaScript to Shopify Functions. As I said, we want Javy to be a general-purpose tool for JavaScript-in-Wasm, so everything that is specific to Shopify Functions needs to be built on top of Javy. 
[Bringing Javascript to WebAssembly for Shopify Functions (2023)](https://shopify.engineering/javascript-in-webassembly-for-shopify-functions)

```js
// index.js
function fetchData() {
  return Promise.resolve()
    .then(_ => "some data!")
}
const f = fetchData()
f.then(data =>
  console.log(data)
)
```

```sh
$ javy compile index.js -o destination/index.wasm

$ wasmtime destination/index.wasm
Error while running JS: Adding tasks to the event queue is not supported
Error: failed to run main module `destination/index.wasm`

Caused by:
    0: failed to invoke command default
    1: error while executing at wasm backtrace:
           0: 0x5cfa1 - <unknown>!<wasm function 104>
           1: 0x6f59c - <unknown>!<wasm function 165>
           2: 0xb6630 - <unknown>!<wasm function 1005>
    2: wasm trap: wasm `unreachable` instruction executed
```

which means, we cannot use `Future` in Scala...

```scala
// Promise.scala
import scala.concurrent._
import scala.util.Success
import concurrent.ExecutionContext.Implicits.global

object Promise:
  def fetchData(): Future[String] = Future { "some data!" }
  def main(args: Array[String]): Unit =
    val f = fetchData()
    f.onComplete:
      case Success(data) => println(data)
```

```sh
$ scala-cli package --js Promise.scala -o build/promise.js --force
$ javy compile build/promise.js -o destination/promise.wasm
$ wasmtime destination/promise.wasm
Error while running JS: Adding tasks to the event queue is not supported
Error: failed to run main module `destination/promise.wasm`

Caused by:
    0: failed to invoke command default
    1: error while executing at wasm backtrace:
           0: 0x5cfa1 - <unknown>!<wasm function 104>
           1: 0x6f59c - <unknown>!<wasm function 165>
           2: 0xb6630 - <unknown>!<wasm function 1005>
    2: wasm trap: wasm `unreachable` instruction executed
```

because Scala's `Future` would be translated into `Promise` (or `setTimeout`) in JS.

## experimental_event_loop
[Support for async / await · Issue #387 · bytecodealliance/javy](https://github.com/bytecodealliance/javy/issues/387)

Build `javy` with an `experimental_event_loop` feature flag, and install `javy-cli` globally.

```sh
$ cargo build --features experimental_event_loop -p javy-core --target=wasm32-wasi -r
$ cargo install --path crates/cli

$ javy compile build/promise.js -o destination/promise.wasm
$ wasmtime destination/promise.wasm
some data!
```

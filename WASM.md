# WebAssembly → ksplang
This is a translation layer from wasm to ksplang. Allows to call wasm functions from ksplang. Very useful, really.

It uses [Chicory](https://github.com/dylibso/chicory) (a JVM WASM runtime) for the WASM parsing,
all instructions are then [translated](/wasm2ksplang/src/main/kotlin/cz/sejsel/ksplang/wasm/WasmFunctionScope.kt) into ksplang equivalents.

The main challenge with ksplang is that all overflows (except bitshift, i.e. `<<`) cause fatal errors in ksplang,
and WASM generally has wrapping logic for its arithmetic. Performance matters quite a bit, so there
has been an attempt at finding efficient translations.

Additionally, we define some runtime data layouts on
the stack to store globals, the function table (maybe avoidable), and most importantly, the memory.
The original input to the ksplang program is also stored.

> [!WARNING]
> Float (f32, f64) instructions are not yet implemented. Everything else from the MVP is.
> See below for supported proposals.
>
> Will happen when I feel like implementing some more instructions (it's suprisingly fun!)

## Simple example
Compile the following Rust code with `RUSTFLAGS=-Ctarget-cpu=mvp cargo +nightly build --release -Zbuild-std=panic_abort,std --target wasm32-unknown-unknown`:

```rust
#[link(wasm_import_module = "env")]
extern "C" {
    #[link_name = "input_size"]
    fn input_size() -> i32;
    #[link_name = "read_input"]
    fn read_input(index: i32) -> i64;
}

#[export_name = "sum_input"]
pub extern "C" fn sum_input() -> i64 {
    let size = unsafe { input_size() };
    let mut sum = 0;
    for i in 0..size {
        let value = unsafe { read_input(i) };
        sum += value;
    }
    sum
}
```

This results in `sum_input.wasm` (or, rather, your package name, but you get the idea).
Now you can create a single-module (WASM module, that is) ksplang program:

```kt
val builder = KsplangBuilder()
val translator = KsplangWasmModuleTranslator()

val store = Store()
val module = instantiateModuleFromPath(translator, Path("wasm/sum_input.wasm"), "sum_input", store)
val program = buildSingleModuleProgram(module) {
    val sum = getExportedFunction("sum_input") as ProgramFunction0To1

    body {
        call(sum)
        leaveTop() // destroys runtime layout, leaves just the result of sum_input
    }
}

val ksplang = builder.buildAnnotated(program).toRunnableProgram()
File("sum_input.ksplang").writeText(ksplang)
```

## Supported proposals

| Proposal                                 | Supported                     |
|------------------------------------------|-------------------------------|
| MVP                                      | 🚧 Almost (no f32, f64)       |
| Import/Export of Mutable Globals         | ❌ Not tested, maybe           |
| Non-trapping float-to-int conversions    | ❌ Nah                         |
| Sign-extension operators                 | ✅ Yes                         |
| Multi-value                              | ❌ Nope                        |
| JS BigInt to WebAssembly i64 integration | ❌ we don't need no JavaScript |
| Reference Types                          | ❌ Nah                         |
| Bulk memory operations                   | ❌ Nope                        |
| Fixed-width SIMD                         | ❌ Nay                         |
| Tail call                                | ❌ not at all                  |
| Extended Constant Expressions            | not applicable, I think?      |
| Typed Function References                | ❌ Nuh-uh                      |
| Garbage collection                       | ❌ Uh                          |
| Multiple memories                        | ❌ Maybe one day               |
| Relaxed SIMD                             | ❌ No way                      |
| Branch Hinting                           | ❌ No                          |
| Exception handling                       | ❌ Nope                        |
| JS String Builtins                       | ❌ ksplang > JS                |
| Memory64                                 | ❌ No (would be slow, too)     |


## Tests

Basically everything is covered by tests, mainly by property tests which compare
whether Chicory produces the same result for a program as the translated ksplang program.

This approach worked quite well, there were only two bugs to solve when running the first large programs:
- missing support for offset in load/store instructions (oops)
- `i32.const` violating our invariant that top 32 bits should be zero, when used with negative numbers (a missing MOD)

Eventually more bugs were found:
- `if` did not support missing `else` (not produced when compiling rust to wasm, but wasm-opt might produce it)
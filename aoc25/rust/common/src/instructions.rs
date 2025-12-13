// ksplang instructions exported as functions, these should be super fast
#[link(wasm_import_module = "ksplang")]
unsafe extern "C" {
    #[link_name = "max"]
    fn _max(a: i64, b: i64) -> i64;
    // We cannot provide the raw u instruction because it has a variable number of stack arguments
    #[link_name = "u_add"]
    fn _u_add(a: i64, b: i64) -> i64;
    #[link_name = "u_subabs"]
    fn _u_subabs(a: i64, b: i64) -> i64;
    #[link_name = "u_mul"]
    fn _u_mul(a: i64, b: i64) -> i64;
    #[link_name = "u_curseddiv"]
    fn _u_curseddiv(a: i64, b: i64) -> i64;
    #[link_name = "u_factorial"]
    fn _u_factorial(a: i64) -> i64;
    #[link_name = "u_sgn"]
    fn _u_sgn(a: i64) -> i32;
    #[link_name = "rem"]
    fn _rem(a: i64, b: i64) -> i64;
    #[link_name = "mod"]
    fn _mod(a: i64, b: i64) -> i64;
    #[link_name = "tetr"]
    fn _tetr(a: i64, b: i64) -> i64;
    #[link_name = "cs"]
    fn _cs(a: i64) -> u32;
    #[link_name = "lensum"]
    fn _lensum(a: i64, b: i64) -> u32;
    #[link_name = "bitshift"]
    fn _bitshift(num: i64, by: i64) -> i64;
    #[link_name = "and"]
    fn _and(a: i64, b: i64) -> i64;
    #[link_name = "gcd"]
    fn _gcd(a: i64, b: i64) -> i64;
    #[link_name = "funkcia"]
    fn _funkcia(a: i64, b: i64) -> u32;
    #[link_name = "spanek"]
    fn _spanek();
    // Most efficient negation is done through qeq, which has a variable number of stack results,
    // it's a bit too risky to expose it directly.
    #[link_name = "negate"]
    fn _negate(a: i64) -> i64;
    // Division is also not native
    #[link_name = "div"]
    fn _div(a: i64, b: i64) -> i64;
}

/// Calls the ksplang funkcia instruction, this is safe for all inputs.
/// Returns the result of funkcia(a, b).
pub fn funkcia(a: i64, b: i64) -> u32 {
    unsafe { _funkcia(a, b) }
}

/// Calls the ksplang max instruction, this is safe for all inputs.
/// Returns the maximum of a and b.
pub fn max(a: i64, b: i64) -> i64 {
    unsafe { _max(a, b) }
}

/// Calls the ksplang u instruction with param 0 (addition).
/// Result: `a + b`.
///
/// # Safety
/// This function leads to a program crash if the result overflows i64 (in either direction).
pub unsafe fn add_unchecked(a: i64, b: i64) -> i64 {
    unsafe { _u_add(a, b) }
}

/// Calls the ksplang u instruction with param 1 (absolute value of subtraction).
/// Result: `|a - b|`.
///
/// # Safety
/// This function leads to a program crash if the result overflows i64.
pub unsafe fn subabs_unchecked(a: i64, b: i64) -> i64 {
    unsafe { _u_subabs(a, b) }
}

/// Calls the ksplang u instruction with param 2 (multiplication).
/// Result: `a * b`.
///
/// # Safety
/// This function leads to a program crash if the result overflows i64.
pub unsafe fn mul_unchecked(a: i64, b: i64) -> i64 {
    unsafe { _u_mul(a, b) }
}

/// Calls the ksplang u instruction with param 3 (cursed division).
/// Result: `a / b` or `a % b` if a % b != 0.
///
/// # Safety
/// This function leads to a program crash if the result does not fit into i64.
pub unsafe fn curseddiv_unchecked(dividend: i64, divisor: i64) -> i64 {
    // Note the swapped parameters
    unsafe { _u_curseddiv(divisor, dividend) }
}

/// Calls the ksplang u instruction with param 4 (factorial).
/// Result: `|a|!`.
///
/// # Safety
/// This function leads to a program crash if the result overflows i64.
pub unsafe fn factorial_unchecked(a: i64) -> i64 {
    unsafe { _u_factorial(a) }
}

/// Calls the ksplang u instruction with param 5 (sgn).
/// Result: `sgn(a)`.
pub fn sgn(a: i64) -> i32 {
    unsafe { _u_sgn(a) }
}

/// Calls the ksplang REM instruction.
/// Result: `a rem b`. (like C %)
/// # Safety
/// This function leads to a program crash if b == 0.
pub unsafe fn rem(dividend: i64, divisor: i64) -> i64 {
    // Note the swapped parameters
    unsafe { _rem(divisor, dividend) }
}

/// Calls the ksplang % instruction.
/// Result: `a % b`. (like Rust euclid_mod)
///
/// # Safety
/// This function leads to a program crash if b == 0.
pub unsafe fn euclid_mod(dividend: i64, divisor: i64) -> i64 {
    // Note the swapped parameters
    unsafe { _rem(divisor, dividend) }
}

/// Calls the ksplang tetr instruction.
/// Result: `tetr(a, b)`.
///
/// # Safety
/// This function leads to a program crash if the result does not fit into i64.
pub unsafe fn tetr(a: i64, b: i64) -> i64 {
    unsafe { _tetr(a, b) }
}

/// Calls the ksplang cs instruction, this is safe for all inputs.
/// Result: digit sum of |a|.
pub fn cs(a: i64) -> u32 {
    unsafe { _cs(a) }
}

/// Calls the ksplang lensum instruction, this is safe for all inputs.
/// Result: ilog10(a)+1 + ilog10(b)+1
pub fn lensum(a: i64, b: i64) -> u32 {
    unsafe { _lensum(a, b) }
}

/// Calls the ksplang bitshift instruction.
/// Result: `num << by`
///
/// # Safety
/// This crashes the program with negative `by` values.
pub unsafe fn bitshift(num: i64, by: i64) -> i64 {
    unsafe { _bitshift(num, by) }
}

/// Calls the ksplang and instruction to calculate bitwise and, this is safe for all inputs.
/// Result: `a & b`.
pub fn and(a: i64, b: i64) -> i64 {
    unsafe { _and(a, b) }
}

/// Calls the ksplang gcd instruction.
/// Result: `gcd(a, b)`.
///
/// # Safety
/// This function leads to a program crash if the result does not fit into i64.
pub unsafe fn gcd_unchecked(a: i64, b: i64) -> i64 {
    unsafe { _gcd(a, b) }
}


/// Negates a number, composite of multiple instructions.
///
/// # Safety
/// This function leads to a program crash if a is [i64::MIN].
pub unsafe fn negate_unchecked(a: i64) -> i64 {
    unsafe { _negate(a) }
}

/// Divides two numbers, composite of multiple instructions.
///
/// # Safety
/// This function leads to a program crash if b == 0 or the result does not fit into i64.
pub unsafe fn div_unchecked(a: i64, b: i64) -> i64 {
    // Note the swapped parameters
    unsafe { _div(b, a) }
}
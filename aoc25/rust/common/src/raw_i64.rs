use std::ops;
use crate::instructions::{add_unchecked, and, div_unchecked, lensum, mul_unchecked, negate_unchecked, rem};

/// A wrapper around raw ksplang i64 values, providing fast but **UNSAFE** arithmetic operations which invoke ksplang instructions directly.
/// Any overflow except for bitshifts causes a program crash.
///
/// For example the following WILL crash the program:
/// - negating i64::MIN
/// - adding two values that exceed i64::MAX or go below i64::MIN
/// - multiplying two values that exceed i64::MAX or go below i64::MIN
/// - etc.
#[derive(Copy, Clone, Debug, PartialEq, Eq, PartialOrd, Ord)]
#[repr(transparent)]
pub struct RawI64(i64);

impl RawI64 {
    pub fn new(value: i64) -> Self {
        RawI64(value)
    }

    pub fn get(&self) -> i64 {
        self.0
    }

    /// Returns the number of decimal digits in the value ([i64::ilog10] + 1).
    /// # Examples:
    /// - 0 is 0
    /// - 1-9 is 1
    /// - 10-99 is 2
    pub fn digit_len(&self) -> u32 {
        lensum(self.0, 0)
    }
}

impl From<i64> for RawI64 {
    fn from(value: i64) -> Self {
        RawI64::new(value)
    }
}

impl From<RawI64> for i64 {
    fn from(value: RawI64) -> Self {
        value.get()
    }
}

impl ops::Add for RawI64 {
    type Output = RawI64;

    fn add(self, other: RawI64) -> RawI64 {
        unsafe { add_unchecked(self.into(), other.into()).into() }
    }
}

impl ops::Add<i64> for RawI64 {
    type Output = RawI64;

    fn add(self, other: i64) -> RawI64 {
        unsafe { add_unchecked(self.into(), other).into() }
    }
}

impl ops::Neg for RawI64 {
    type Output = RawI64;

    fn neg(self) -> RawI64 {
        RawI64(unsafe { negate_unchecked(self.0) })
    }
}

impl ops::Sub for RawI64 {
    type Output = RawI64;

    fn sub(self, other: RawI64) -> RawI64 {
        self + (-other)
    }
}

impl ops::Mul for RawI64 {
    type Output = RawI64;

    fn mul(self, other: RawI64) -> RawI64 {
        unsafe { mul_unchecked(self.into(), other.into()).into() }
    }
}

impl ops::Div for RawI64 {
    type Output = RawI64;

    fn div(self, other: RawI64) -> RawI64 {
        unsafe { div_unchecked(self.0, other.0).into() }
    }
}

impl ops::BitAnd for RawI64 {
    type Output = RawI64;

    fn bitand(self, other: RawI64) -> RawI64 {
        // This one should actually be the translation for standard i64 bitwise and anyway, but might as well be explicit.
        and(self.into(), other.into()).into()
    }
}

impl ops::Rem for RawI64 {
    type Output = RawI64;

    fn rem(self, other: RawI64) -> RawI64 {
        unsafe { rem(self.into(), other.into()).into() }
    }
}

impl ops::AddAssign for RawI64 {
    fn add_assign(&mut self, other: RawI64) {
        *self = *self + other;
    }
}

impl ops::SubAssign for RawI64 {
    fn sub_assign(&mut self, other: RawI64) {
        *self = *self - other;
    }
}

impl ops::MulAssign for RawI64 {
    fn mul_assign(&mut self, other: RawI64) {
        *self = *self * other;
    }
}

impl ops::DivAssign for RawI64 {
    fn div_assign(&mut self, other: RawI64) {
        *self = *self / other;
    }
}

impl ops::RemAssign for RawI64 {
    fn rem_assign(&mut self, other: RawI64) {
        *self = *self % other;
    }
}

impl ops::BitAndAssign for RawI64 {
    fn bitand_assign(&mut self, other: RawI64) {
        *self = *self & other;
    }
}
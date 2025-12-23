use crate::_read_raw_i64;

/// A cursed structure which efficiently stores i64 values.
///
/// The ksplang host environment stores each byte of memory into an i64 value,
/// so we can use this to store i64 values in u8 values. Magic.
pub struct RawFastArray {
    data: *mut u8,
    size: usize,
}

impl RawFastArray {
    /// Creates a new RawFastArray of the given size.
    ///
    /// Note that this memory is leaked and will not be freed.
    /// We would have to zero all memory on drop or we would break the memory
    /// representation invariant if the underlying memory was reused.
    pub fn new_leaked(size: usize) -> Self {
        let layout = std::alloc::Layout::from_size_align(size, 1).unwrap();
        let data = unsafe { std::alloc::alloc(layout) };
        RawFastArray { data, size }
    }

    /// Gets the i64 value at the given index.
    ///
    /// SAFETY:
    /// No index checks are performed, anything might happen if out of bounds.
    pub fn get(&self, index: usize) -> i64 {
        unsafe { _read_raw_i64(self.data as usize + index) }
    }

    /// Sets the i64 value at the given index.
    ///
    /// SAFETY:
    /// No index checks are performed, anything might happen if out of bounds.
    pub fn set(&mut self, index: usize, value: i64) {
        unsafe { crate::_save_raw_i64(value, self.data as usize + index) }
    }

    /// Returns the size of the array.
    pub fn size(&self) -> usize {
        self.size
    }
}
pub mod raw_input;

use std::alloc::{Layout, alloc};

#[link(wasm_import_module = "env")]
unsafe extern "C" {
    #[link_name = "input_size"]
    fn _input_size() -> i32;
    #[link_name = "read_input"]
    fn _read_input(index: i32) -> i64;
    /// Method to save raw i64 data into memory (normally, it would be individual bytes).
    /// SAFETY: never deallocate memory manipulated by this to contain values
    ///         which do not fit into a byte or you will break invariants.
    #[link_name = "save_raw_i64"]
    fn _save_raw_i64(value: i64, index: usize);
    #[link_name = "set_input"]
    fn _set_input(value: i64, index: i32);
}

pub fn input_size() -> i32 {
    unsafe { _input_size() }
}

pub fn read_input(index: i32) -> i64 {
    unsafe { _read_input(index) }
}

/** Replaces value in input with a new value. Mainly useful as an optimization. */
pub fn set_input(index: i32, value: i64) {
    unsafe { _set_input(value, index) }
}

pub fn read_input_to_string() -> String {
    let size = input_size();
    let mut input = String::new();
    for i in 0..size {
        let value = read_input(i);
        let c = std::char::from_u32(value as u32).unwrap();
        input.push(c);
    }
    input
}

pub fn read_input_to_lines() -> Vec<String> {
    let mut lines: Vec<String> = Vec::new();
    let size = input_size();
    let mut current_line = String::new();
    for i in 0..size {
        let value = read_input(i);
        let c = std::char::from_u32(value as u32).unwrap();
        if c == '\n' {
            lines.push(current_line);
            current_line = String::new();
        } else {
            current_line.push(c);
        }
    }
    if !current_line.is_empty() {
        lines.push(current_line);
    }
    lines
}

// We know this is backed by i64 memory fields
#[repr(C)]
pub struct KsplangOutput(*mut u8);

// Exports an array of i64 to the host environment.
// Layout: [first element is length, followed by elements]
pub fn export(data: &[i64]) -> KsplangOutput {
    let pointer = unsafe { alloc(Layout::from_size_align_unchecked(data.len() + 1, 1)) };
    // So we do some cursed crimes here, we grab a i8 pointer, but we know it's backed by i64 data
    unsafe {
        _save_raw_i64(data.len() as i64, pointer as usize);
    }
    for (i, &value) in data.iter().enumerate() {
        unsafe {
            _save_raw_i64(value, pointer as usize + i + 1);
        }
    }

    KsplangOutput(pointer)
}

use crate::raw_input::is_char;
use crate::read_input_raw;

pub fn count_chars(from: u32, to: u32, char: char) -> u32 {
    let mut range_count = 0;
    for i in from..to {
        if is_char(read_input_raw(i), char) {
            range_count += 1;
        }
    }
    range_count
}

pub fn count_other_chars(from: u32, to: u32, char: char) -> u32 {
    let mut range_count = 0;
    for i in from..to {
        if !is_char(read_input_raw(i), char) {
            range_count += 1;
        }
    }
    range_count
}

pub fn find_first_other_char(from: u32, to: u32, char: char) -> u32 {
    for i in from..to {
        if !is_char(read_input_raw(i), char) {
            return i;
        }
    }
    to
}

pub fn find_first_char(from: u32, to: u32, char: char) -> u32 {
    for i in from..to {
        if is_char(read_input_raw(i), char) {
            return i;
        }
    }
    to
}

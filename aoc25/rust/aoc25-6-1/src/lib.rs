use common::raw_input::{is_char, parse_u32_unchecked};
use common::{input_size, read_input_raw, set_input};
use common::raw_i64::RawI64;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut result: RawI64 = 0.into();
    let input_size = input_size();

    let rows = count_chars(0, input_size, '\n');

    let row_len = input_size / rows;
    let last_row_start = (rows - 1) * row_len;

    // replace newline with space so we don't need to special case reading chars in the last column
    for row in 0..rows {
        set_input(row * row_len + row_len - 1, ' ' as i64);
    }

    let columns = count_other_chars(last_row_start, input_size - 1, ' '); // -1 to ignore last '\n'

    let mut op_pos = last_row_start;
    for column in 0..columns {
        let next_pos = find_first_other_char(op_pos + 1, input_size, ' ');
        let is_mul = is_char(read_input_raw(op_pos), '*'); // otherwise '+' - addition
        let mut column_result: RawI64 = if is_mul { 1.into() } else { 0.into() };

        for row in 0..(rows-1) {
            let start = row * row_len + op_pos - last_row_start;
            let mut first_char_index = find_first_other_char(start, input_size, ' ');
            let num = unsafe { parse_u32_unchecked(&mut first_char_index, ' ') };

            if is_mul {
                column_result *= (num as i64).into();
            } else {
                column_result += (num as i64).into();
            }
        }

        result += column_result;
        op_pos = next_pos
    }

    result
}

fn count_chars(from: u32, to: u32, char: char) -> u32 {
    let mut range_count = 0;
    for i in from..to {
        if is_char(read_input_raw(i), char) {
            range_count += 1;
        }
    }
    range_count
}

fn count_other_chars(from: u32, to: u32, char: char) -> u32 {
    let mut range_count = 0;
    for i in from..to {
        if !is_char(read_input_raw(i), char) {
            range_count += 1;
        }
    }
    range_count
}

fn find_first_other_char(from: u32, to: u32, char: char) -> u32 {
    for i in from..to {
        if !is_char(read_input_raw(i), char) {
            return i;
        }
    }
    to
}

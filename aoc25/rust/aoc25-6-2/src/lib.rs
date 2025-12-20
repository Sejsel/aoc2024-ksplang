use common::raw_input::{is_char};
use common::{input_size, read_input, read_input_raw, set_input};
use common::input_utils::{count_chars, count_other_chars, find_first_other_char};
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
    for _ in 0..columns {
        let next_pos = find_first_other_char(op_pos + 1, input_size, ' ');
        let column_width = next_pos - op_pos - 1;
        let is_mul = is_char(read_input_raw(op_pos), '*'); // otherwise '+' - addition
        let mut column_result: RawI64 = if is_mul { 1.into() } else { 0.into() };

        for num_i in 0..column_width {
            let num = parse_u32_vertical(0, rows - 1, op_pos - last_row_start + num_i, row_len);
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

fn parse_u32_vertical(from_y: u32, to_y: u32, x: u32, row_len: u32) -> u32 {
    let mut result = 0;
    for y in from_y..to_y {
        let input_pos = y * row_len + x;
        let c = unsafe { std::char::from_u32_unchecked(read_input(input_pos) as u32) };
        if c >= '0' && c <= '9' {
            result = result * 10 + (c as u32 - '0' as u32);
        }
    }
    result
}


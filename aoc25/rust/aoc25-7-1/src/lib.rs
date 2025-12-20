use common::input_utils::{count_chars, find_first_char};
use common::raw_i64::RawI64;
use common::raw_input::{is_char};
use common::{input_size, read_input_raw, set_input};

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut result: RawI64 = 0.into();
    let input_size = input_size();

    let rows = count_chars(0, input_size, '\n');

    let row_len = input_size / rows;

    let start = find_first_char(0, input_size, 'S');
    set_input(start, '|' as i64); // no special case for S, just treat it as a |

    // we do not care about bottom row
    for row in 1..rows - 1 {
        // could optimize here: increase x range by 1 to each side with each row, start with just one column under S
        for x in 0..(row_len - 1) {
            // -1 for newline
            let char = read_input_xy(x, row, row_len);
            let light_above = is_char(read_input_xy(x, row - 1, row_len), '|');
            if is_char(char, '^') {
                if light_above {
                    // this is a split
                    result += 1.into();
                }
                set_input_xy(x - 1, row, row_len, '|');
                set_input_xy(x + 1, row, row_len, '|');
            } else if light_above {
                set_input_xy(x, row, row_len, '|');
            }
        }
    }

    result
}

fn read_input_xy(x: u32, y: u32, row_len: u32) -> RawI64 {
    let index = y * row_len + x;
    read_input_raw(index)
}

fn set_input_xy(x: u32, y: u32, row_len: u32, char: char) {
    let index = y * row_len + x;
    set_input(index, char as i64)
}

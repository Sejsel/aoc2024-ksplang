use common::input_utils::{count_chars, find_first_char};
use common::raw_i64::RawI64;
use common::raw_input::{is_char};
use common::{input_size, read_input_raw, set_input};

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let input_size = input_size();

    let rows = count_chars(0, input_size, '\n');

    let row_len = input_size / rows;

    let start = find_first_char(0, input_size, 'S');
    set_input(start, 1); // 1 beam

    // replace all dots with zeroes - we will use it to track beams on each location
    for i in 0..input_size {
        let char = read_input_raw(i);
        if is_char(char, '.') {
            set_input(i, 0);
        } else if is_char(char, '^') {
            set_input(i, -1); // splits are -1
        }
    }

    // we do not care about bottom row
    for row in 1..rows - 1 {
        // could optimize here: increase x range by 1 to each side with each row, start with just one column under S
        for x in 0..(row_len - 1) {
            // -1 for newline
            let char = read_input_xy(x, row, row_len);
            let num_above = read_input_xy(x, row - 1, row_len);

            if char.subabs((-1).into()) == 0.into() { // char == -1
                // this is a splitter

                // add number from above to tiles left and right
                let left = read_input_xy(x - 1, row, row_len);
                set_input_xy(x - 1, row, row_len, left + num_above);
                let right = read_input_xy(x + 1, row, row_len);
                set_input_xy(x + 1, row, row_len, right + num_above);
            } else {
                // not a splitter
                // copy from above
                if num_above.subabs((-1).into()) != 0.into() { // char == -1 // char == -1
                    // number above is not a splitter, we can copy
                    let num = read_input_xy(x, row, row_len);
                    set_input_xy(x, row, row_len, num + num_above);
                }
            }
        }
    }

    // sum up bottom row
    let mut total_beams = RawI64::from(0);
    for x in 0..(row_len - 1) {
        let num = read_input_xy(x, rows - 2, row_len);
        if num.subabs((-1).into()) == 0.into() {
            // ignore this splitter
            continue;
        }
        total_beams += num;
    }
    total_beams
}

fn read_input_xy(x: u32, y: u32, row_len: u32) -> RawI64 {
    let index = y * row_len + x;
    read_input_raw(index)
}

fn set_input_xy(x: u32, y: u32, row_len: u32, num: RawI64) {
    let index = y * row_len + x;
    set_input(index, num.into())
}

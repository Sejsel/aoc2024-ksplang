use common::instructions::subabs_unchecked;
use common::raw_i64::RawI64;
use common::{input_size, read_input, read_input_raw};

const DIGITS: u32 = 12;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut input_pos = 0;
    let mut result: RawI64 = RawI64::new(0);
    let input_size = input_size();

    loop {
        if input_pos >= input_size {
            break;
        }

        let newline_pos = find_next_unsafe(input_pos, '\n');

        let mut row_result = RawI64::new(0);
        for digit_index in 0..DIGITS {
            let last_char_pos = newline_pos - (DIGITS - 1) + digit_index;
            row_result *= 10.into();

            'num: for num in (1..=9).rev() {
                let char = num + '0' as u32;
                for pos in input_pos..last_char_pos {
                    let input_char = read_input_raw(pos);
                    if input_char.subabs((char as i64).into()) == 0.into() {
                        input_pos = pos + 1;
                        row_result += (num as i64).into();
                        break 'num;
                    }
                }
            }
        }
        result += row_result;

        input_pos = newline_pos + 1;
    }

    result
}

// Will go out of input bounds if char is not in input on from_index or after
fn find_next_unsafe(mut from_index: u32, char: char) -> u32 {
    loop {
        let input = read_input(from_index);
        if subabs_unchecked(input, char as i64) == 0 {
            return from_index;
        }
        from_index += 1;
    }
}

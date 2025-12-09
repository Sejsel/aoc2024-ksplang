use common::{input_size, read_input, };
use common::raw_input::parse_u32_unchecked;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> i32 {
    let mut input_pos = 0;
    let mut result = 0;
    let mut position: i32 = 50;
    let input_size = input_size();
    loop {
        if input_pos >= input_size {
            break;
        }
        let negative = read_input(input_pos) as u32 == 'L' as u32;
        input_pos += 1;
        let by = unsafe { parse_u32_unchecked(&mut input_pos, '\n') } as i32;

        if negative {
            position -= by;
        } else {
            position += by;
        }
        position = position.rem_euclid(100);
        if position == 0 {
            result += 1;
        }
    }

    result
}

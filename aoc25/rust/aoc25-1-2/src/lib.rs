use common::raw_input::parse_u32_unchecked;
use common::{input_size, read_input};

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

        let new_position = if negative {
            position - by
        } else {
            position + by
        };

        let rotations = if new_position > 0 {
            new_position / 100
        } else {
            if position == 0 {
                new_position.abs() / 100
            } else {
                new_position.abs() / 100 + 1
            }
        };

        position = new_position.rem_euclid(100);
        result += rotations
    }

    result
}

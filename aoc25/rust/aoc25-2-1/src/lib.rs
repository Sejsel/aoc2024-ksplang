use common::raw_input::{parse_u64_unchecked};
use common::{input_size, set_input};
use common::instructions::lensum;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> u64 {
    let mut input_pos = 0;
    let mut result = 0;
    let input_size = input_size();
    // replace final line break with comma so we don't need to special case that
    set_input(input_size - 1, ',' as i64);

    loop {
        if input_pos >= input_size {
            break;
        }
        let from = unsafe { parse_u64_unchecked(&mut input_pos, '-') };
        let to = unsafe { parse_u64_unchecked(&mut input_pos, ',') };
        for value in from..=to {
            if is_invalid(value) {
                result += value
            }
        }
    }

    result
}

fn is_invalid(value: u64) -> bool {
    let len = lensum(value as i64, 0);
    if len % 2 != 0 {
        return false
    }

    let power_of_ten = 10u64.pow(len / 2);
    let bottom_half = value % power_of_ten;
    let top_half = value / power_of_ten;

    top_half == bottom_half
}
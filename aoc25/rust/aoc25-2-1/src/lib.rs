use common::raw_input::{parse_u64_unchecked};
use common::{input_size, set_input};
use common::raw_i64::RawI64;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut input_pos = 0;
    let mut result = RawI64::new(0);
    let input_size = input_size();
    // replace final line break with comma so we don't need to special case that
    set_input(input_size - 1, ',' as i64);

    loop {
        if input_pos >= input_size {
            break;
        }
        let from: RawI64 = (unsafe { parse_u64_unchecked(&mut input_pos, '-') } as i64).into();
        let to: RawI64 = (unsafe { parse_u64_unchecked(&mut input_pos, ',') } as i64).into();

        let mut value: RawI64 = from;
        loop {
            if is_invalid(value) {
                result += value;
            }

            value += 1.into();
            if (to - value).sgn() == 0 {
                break;
            }
        }
    }

    result
}

const POWERS_OF_TEN: [i64; 15] = [
    1,
    10,
    100,
    1_000,
    10_000,
    100_000,
    1_000_000,
    10_000_000,
    100_000_000,
    1_000_000_000,
    10_000_000_000,
    100_000_000_000,
    1_000_000_000_000,
    10_000_000_000_000,
    100_000_000_000_000,
];

fn is_invalid(value: RawI64) -> bool {
    let len = value.digit_len();
    if len % 2 != 0 {
        return false
    }

    let power_of_ten = POWERS_OF_TEN[(len / 2) as usize];
    let bottom_half = (value % power_of_ten.into()).to_i64() as u32;
    let top_half = (value / power_of_ten.into()).to_i64() as u32;

    top_half == bottom_half
}
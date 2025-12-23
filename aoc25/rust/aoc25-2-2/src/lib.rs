use common::raw_input::{parse_u64_unchecked};
use common::{input_size, set_input};
use common::raw_array::RawFastArray;
use common::raw_i64::{iter_non_empty_range_inclusive, RawI64};

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

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut input_pos = 0;
    let mut result = RawI64::new(0);
    let input_size = input_size();
    // replace final line break with comma so we don't need to special case that
    set_input(input_size - 1, ',' as i64);

    let mut lookup_table = RawFastArray::new_leaked(POWERS_OF_TEN.len());
    for (i, power) in POWERS_OF_TEN.iter().enumerate() {
        lookup_table.set(i, *power);
    }

    loop {
        if input_pos >= input_size {
            break;
        }
        let from: RawI64 = (unsafe { parse_u64_unchecked(&mut input_pos, '-') } as i64).into();
        let to: RawI64 = (unsafe { parse_u64_unchecked(&mut input_pos, ',') } as i64).into();
        for value in iter_non_empty_range_inclusive(from, to) {
            if is_invalid(value, &lookup_table) {
                result += value
            }
        }
    }

    result
}

fn is_invalid(value: RawI64, powers_of_ten: &RawFastArray) -> bool {
    let len = value.digit_len();
    'outer: for part_len in 1..=(len / 2) {
        if len % part_len != 0 { continue }
        let part_count = len / part_len;
        let power_of_ten = powers_of_ten.get(part_len as usize);

        let prev_part = value % power_of_ten.into();
        let mut remaining = value / power_of_ten.into();
        for _ in 0..(part_count - 1) {
            let current_part = remaining % power_of_ten.into();
            if current_part != prev_part {
                continue 'outer
            }
            remaining = remaining / power_of_ten.into();
        }
        return true
    }
    false
}
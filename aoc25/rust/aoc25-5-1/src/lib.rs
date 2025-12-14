use common::{input_size, read_input_raw};
use common::raw_input::{is_char, parse_u64_unchecked};

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> u32 {
    let mut input_pos: u32 = 0;
    let mut result: u32 = 0;
    let input_size = input_size();

    let range_count = count_ranges(input_size);
    let mut ranges: Vec<(i64, i64)> = Vec::with_capacity(range_count as usize);
    for _ in 0..range_count {
        let from = unsafe { parse_u64_unchecked(&mut input_pos, '-') } as i64;
        let to = unsafe { parse_u64_unchecked(&mut input_pos, '\n') } as i64;
        ranges.push((from, to));
    }
    // now there is an empty line
    input_pos += 1;

    loop {
        if input_pos >= input_size {
            break;
        }
        let id = unsafe { parse_u64_unchecked(&mut input_pos, '\n') } as i64;
        if is_fresh(id, &ranges) {
            result += 1;
        }
    }

    result
}

fn is_fresh(id: i64, ranges: &[(i64, i64)]) -> bool {
    for (from, to) in ranges.iter() {
        if id >= *from && id <= *to {
            return true;
        }
    }
    false
}

fn count_ranges(input_size: u32) -> u32 {
    let mut range_count = 0;
    for i in 0..input_size {
        if is_char(read_input_raw(i), '-') {
            range_count += 1;
        }
    }
    range_count
}

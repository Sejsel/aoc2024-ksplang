use common::{input_size, read_input_raw};
use common::raw_input::{is_char, parse_u64_unchecked};

#[derive(Clone)]
struct RangePart {
    id: i64,
    is_end: bool
}

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> i64 {
    let mut input_pos: u32 = 0;
    let mut result: i64 = 0;
    let input_size = input_size();

    let range_count = count_ranges(input_size);
    let mut ranges: Vec<RangePart> = Vec::with_capacity((range_count * 2) as usize);
    for _ in 0..range_count {
        let from = unsafe { parse_u64_unchecked(&mut input_pos, '-') } as i64;
        ranges.push(RangePart{ id: from, is_end: false });
        let to = unsafe { parse_u64_unchecked(&mut input_pos, '\n') } as i64;
        ranges.push(RangePart{ id: to, is_end: true });
    }

    // we need to sort starts before ends if there is a tie or we would double-count the id if that is where two ranges meet.
    ranges.sort_by_key(|x| (x.id, x.is_end));

    let mut depth: u32 = 0;
    let mut active_from: Option<i64> = None;
    for part in ranges {
        if part.is_end {
            depth -= 1;
        } else {
            depth += 1;
        }

        if let Some(from) = active_from {
            if depth == 0 {
                // No more ranges active
                let to = part.id;
                result += to - from + 1;
                active_from = None;
            }
        } else {
            active_from = Some(part.id);
        }
    }

    result
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

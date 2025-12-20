use common::raw_input::{is_char, parse_u32_unchecked};
use common::{input_size, read_input_raw, set_input};
use common::input_utils::count_chars;
use common::raw_i64::RawI64;

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> RawI64 {
    let mut result: RawI64 = 0.into();
    let input_size = input_size();

    let rows = count_chars(0, input_size, '\n');

    let row_len = input_size / rows;
    
    todo!()
}

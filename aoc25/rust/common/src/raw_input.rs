use crate::read_input;

#[inline]
pub unsafe fn parse_u32_unchecked(input_pos: &mut i32, terminator: char) -> u32 {
    let mut result = 0;
    loop {
        let c = unsafe { std::char::from_u32_unchecked(read_input(*input_pos) as u32) };
        *input_pos += 1;
        if c == terminator {
            break;
        }
        result = result * 10 + (c as u32 - '0' as u32);
    }
    result
}

#[inline]
pub unsafe fn parse_u64_unchecked(input_pos: &mut i32, terminator: char) -> u64 {
    let mut result = 0u64;
    loop {
        let c = unsafe { std::char::from_u32_unchecked(read_input(*input_pos) as u32) };
        *input_pos += 1;
        if c == terminator {
            break;
        }
        result = result * 10 + (c as u64 - '0' as u64);
    }
    result
}

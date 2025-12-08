use common::{input_size, read_input};

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> i32 {
    let input = read_input_to_string();
    let mut result = 0;
    let mut position = 50;
    for line in input.lines() {
        let negative = line.starts_with("L");
        let by = line[1..].parse::<i32>().unwrap();
        if negative {
            position -= by;
        } else {
            position += by;
        }
        position %= 100;
        if position == 0 {
            result += 1;
        }
    }

    result
}


fn read_input_to_string() -> String {
    let size = input_size();
    let mut string = String::new();
    string.reserve(size as usize);
    for i in 0..size {
        let value = read_input(i);
        string.push(char::from_u32(value as u32).unwrap());
    }
    string
}
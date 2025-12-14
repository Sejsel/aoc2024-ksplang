use common::{input_size, read_input_raw};
use common::raw_i64::RawI64;
use common::raw_input::{find_next_unsafe, is_char};

const AROUND: [(i32, i32); 8] = [
    (-1, -1),
    (-1, 0),
    (-1, 1),
    (0, -1),
    (0, 1),
    (1, -1),
    (1, 0),
    (1, 1),
];

#[unsafe(no_mangle)]
pub extern "C" fn solve() -> u32 {
    let mut result: u32 = 0;
    let input_size = input_size();

    // input is a grid
    let width = find_next_unsafe(0, '\n');
    let height = input_size / (width + 1); // +1 for newline

    for y in 0..height {
        for x in 0..width {
            if !is_char(read_cell(y, x, width), '@') {
                continue;
            }

            let mut nearby_paper_rolls = 0;
            for (dx, dy) in AROUND {
                let cell_x = (x as i32 + dx) as u32;
                let cell_y = (y as i32 + dy) as u32;
                if is_in_bounds(cell_y, cell_x, width, height) {
                    let cell = read_cell(cell_y, cell_x, width);
                    if is_char(cell, '@') {
                        nearby_paper_rolls += 1;
                    }
                    if nearby_paper_rolls >= 4 {
                        break;
                    }
                }
            }

            if nearby_paper_rolls < 4 {
                result += 1;
            }
        }
    }

    result
}

fn is_in_bounds(y: u32, x: u32, width: u32, height: u32) -> bool {
    // We use the fact that u32 underflows to a large number subtracting to -1
    y < height && x < width
}

fn read_cell(y: u32, x: u32, width: u32) -> RawI64 {
    let index = y * (width + 1) + x; // +1 for newline
    read_input_raw(index)
}

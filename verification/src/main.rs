use easy_parallel::Parallel;
use ksplang::vm::VMOptions;

fn main() -> Result<(), ()>{
    let mut programs = vec![];
    for line in std::io::stdin().lines() {
        let line = line.unwrap();
        if line.trim().starts_with('#') {
            continue;
        }
        let (num, program) = line.trim().split_once(char::is_whitespace).expect("Failed to find whitespace for num-program split");
        let num: i64 = num.parse().expect("Failed to parse number");
        programs.push((num, program.to_string()));
    }

    let threads = 12;

    let ok: Vec<bool> = Parallel::new().each(programs.chunks(programs.len() / threads), |chunk| {
        let mut ok = true;

        for (num, program) in chunk {
            let ops = ksplang::parser::parse_program(program).expect("Failed to parse program");
            for digit_sum in VALUES_PER_DIGIT_SUM {

                let result = ksplang::vm::run(&ops, VMOptions::new(
                    &[digit_sum],
                    1000,
                    &PI_TEST_VALUES,
                    5000,
                    u64::MAX,
                ));

                let stack = result.expect("Failed to extract result stack").stack;
                if stack.len() != 2 {
                    eprintln!("{num} on top of {digit_sum}: Expected 2 values on the stack, got {}", stack.len());
                    ok = false;
                    break;
                }
                if stack[1] != *num {
                    eprintln!("{num} on top of {digit_sum}: Expected {num}, got {}", stack[1]);
                    ok = false;
                    break;
                }
            }
        }

        ok
    }).run();

    if ok.iter().all(|x| *x) {
        Ok(())
    } else {
        Err(())
    }
}

const PI_TEST_VALUES: [i8; 42] = [
    3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3, 2, 3, 8, 4, 6, 2, 6, 4, 3, 3, 8, 3, 2, 7, 9, 5,
    0, 2, 8, 8, 4, 1, 9, 7, 1, 6,
];

const VALUES_PER_DIGIT_SUM: [i64; 171] = [
    0,
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    8,
    9,
    19,
    29,
    39,
    49,
    59,
    69,
    79,
    89,
    99,
    199,
    299,
    399,
    499,
    599,
    699,
    799,
    899,
    999,
    1999,
    2999,
    3999,
    4999,
    5999,
    6999,
    7999,
    8999,
    9999,
    19999,
    29999,
    39999,
    49999,
    59999,
    69999,
    79999,
    89999,
    99999,
    199999,
    299999,
    399999,
    499999,
    599999,
    699999,
    799999,
    899999,
    999999,
    1999999,
    2999999,
    3999999,
    4999999,
    5999999,
    6999999,
    7999999,
    8999999,
    9999999,
    19999999,
    29999999,
    39999999,
    49999999,
    59999999,
    69999999,
    79999999,
    89999999,
    99999999,
    199999999,
    299999999,
    399999999,
    499999999,
    599999999,
    699999999,
    799999999,
    899999999,
    999999999,
    1999999999,
    2999999999,
    3999999999,
    4999999999,
    5999999999,
    6999999999,
    7999999999,
    8999999999,
    9999999999,
    19999999999,
    29999999999,
    39999999999,
    49999999999,
    59999999999,
    69999999999,
    79999999999,
    89999999999,
    99999999999,
    199999999999,
    299999999999,
    399999999999,
    499999999999,
    599999999999,
    699999999999,
    799999999999,
    899999999999,
    999999999999,
    1999999999999,
    2999999999999,
    3999999999999,
    4999999999999,
    5999999999999,
    6999999999999,
    7999999999999,
    8999999999999,
    9999999999999,
    19999999999999,
    29999999999999,
    39999999999999,
    49999999999999,
    59999999999999,
    69999999999999,
    79999999999999,
    89999999999999,
    99999999999999,
    199999999999999,
    299999999999999,
    399999999999999,
    499999999999999,
    599999999999999,
    699999999999999,
    799999999999999,
    899999999999999,
    999999999999999,
    1999999999999999,
    2999999999999999,
    3999999999999999,
    4999999999999999,
    5999999999999999,
    6999999999999999,
    7999999999999999,
    8999999999999999,
    9999999999999999,
    19999999999999999,
    29999999999999999,
    39999999999999999,
    49999999999999999,
    59999999999999999,
    69999999999999999,
    79999999999999999,
    89999999999999999,
    99999999999999999,
    199999999999999999,
    299999999999999999,
    399999999999999999,
    499999999999999999,
    599999999999999999,
    699999999999999999,
    799999999999999999,
    899999999999999999,
    999999999999999999,
    1999999999999999999,
    2999999999999999999,
    3999999999999999999,
    4999999999999999999,
    5999999999999999999,
    6999999999999999999,
    7999999999999999999,
    8999999999999999999,
];

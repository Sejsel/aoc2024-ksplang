#!/usr/bin/env python3
"""
Script to remove unused memory from WebAssembly files.
Takes two arguments: input.wasm and output.wasm
Converts to WAT, removes memory declaration, converts back to WASM.
"""

import sys
import subprocess
import re


def run_command(cmd, error_msg):
    """Run a command and return its output, or exit with error on failure."""
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            check=True
        )
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"Error: {error_msg}", file=sys.stderr)
        print(f"Command failed: {' '.join(cmd)}", file=sys.stderr)
        print(f"stderr: {e.stderr}", file=sys.stderr)
        sys.exit(1)
    except FileNotFoundError:
        print(f"Error: Command not found: {cmd[0]}", file=sys.stderr)
        print("Make sure wasm2wat and wat2wasm are installed (from WABT toolkit)", file=sys.stderr)
        sys.exit(1)


def check_memory_instructions(wat_content):
    """Check if WAT contains any memory instructions."""
    # Memory instructions to check for
    memory_instructions = [
        r'\bmemory\.size\b',
        r'\bmemory\.grow\b',
        r'\bi32\.load\b',
        r'\bi64\.load\b',
        r'\bf32\.load\b',
        r'\bf64\.load\b',
        r'\bi32\.load8_s\b',
        r'\bi32\.load8_u\b',
        r'\bi32\.load16_s\b',
        r'\bi32\.load16_u\b',
        r'\bi64\.load8_s\b',
        r'\bi64\.load8_u\b',
        r'\bi64\.load16_s\b',
        r'\bi64\.load16_u\b',
        r'\bi64\.load32_s\b',
        r'\bi64\.load32_u\b',
        r'\bi32\.store\b',
        r'\bi64\.store\b',
        r'\bf32\.store\b',
        r'\bf64\.store\b',
        r'\bi32\.store8\b',
        r'\bi32\.store16\b',
        r'\bi64\.store8\b',
        r'\bi64\.store16\b',
        r'\bi64\.store32\b',
    ]
    
    for pattern in memory_instructions:
        if re.search(pattern, wat_content):
            # Find the instruction for better error message
            match = re.search(pattern, wat_content)
            instruction = match.group(0)
            print(f"Error: Memory instruction '{instruction}' found in WAT file.", file=sys.stderr)
            print("Cannot remove memory declaration while memory instructions are present.", file=sys.stderr)
            sys.exit(1)


def remove_memory_declaration(wat_content):
    """Remove memory declaration and memory export from WAT content."""
    # Check if memory declaration exists
    memory_pattern = r'^\s*\(memory.*?\).*$'
    
    if not re.search(memory_pattern, wat_content, re.MULTILINE):
        print("Error: No memory declaration found in WAT file.", file=sys.stderr)
        sys.exit(1)
    
    # First remove memory exports (do this before removing memory declaration)
    # Pattern matches lines like: (export "memory" (memory 0))
    # Use a more precise pattern that matches the quoted string
    memory_export_pattern = r'^\s*\(export\s+"[^"]+"\s+\(memory\s+\d+\)\s*\)\s*$'
    modified_wat = re.sub(memory_export_pattern, '', wat_content, flags=re.MULTILINE)

    # Remove the memory declaration
    modified_wat = re.sub(memory_pattern, '', modified_wat, flags=re.MULTILINE)
    
    return modified_wat


def main():
    if len(sys.argv) != 3:
        print("Usage: python wasm_remove_unused_memory.py <input.wasm> <output.wasm>", file=sys.stderr)
        sys.exit(1)
    
    input_wasm = sys.argv[1]
    output_wasm = sys.argv[2]
    
    # Step 1: Convert WASM to WAT
    print(f"Converting {input_wasm} to WAT format...")
    wat_content = run_command(
        ['wasm2wat', input_wasm],
        f"Failed to convert {input_wasm} to WAT format"
    )
    
    # Step 2: Check for memory instructions
    print("Checking for memory instructions...")
    check_memory_instructions(wat_content)
    
    # Step 3: Remove memory declaration and export
    print("Removing memory declaration and export...")
    modified_wat = remove_memory_declaration(wat_content)
    
    # Step 4: Convert WAT back to WASM
    print(f"Converting modified WAT to {output_wasm}...")
    process = subprocess.Popen(
        ['wat2wasm', '-', '-o', output_wasm],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    stdout, stderr = process.communicate(input=modified_wat)
    
    if process.returncode != 0:
        print("Error: Failed to convert modified WAT back to WASM", file=sys.stderr)
        print(f"stderr: {stderr}", file=sys.stderr)
        sys.exit(1)
    
    print(f"Success! Memory declaration and export removed from {input_wasm} and saved to {output_wasm}")


if __name__ == '__main__':
    main()

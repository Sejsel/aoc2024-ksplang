// WebSocket message types based on AsyncAPI

export interface FrontendRequest {
  type: 'set_program' | 'set_stack' | 'step_to' | 'run_to_end' | 'run_to_instruction' | 'run_to_instruction_backwards' | 'run_to_next_breakpoint' | 'run_to_previous_breakpoint' | 'add_breakpoint' | 'remove_breakpoint' | 'clear_breakpoints';
}

export interface SetProgram extends FrontendRequest {
  type: 'set_program';
  program: AnnotatedKsplangTree;
}

export interface SetStack extends FrontendRequest {
  type: 'set_stack';
  stack: bigint[];
}

export interface StepTo extends FrontendRequest {
  type: 'step_to';
  executedInstructions: bigint;
}

export interface AddBreakpoint extends FrontendRequest {
  type: 'add_breakpoint';
  instructionIndex: number;
}

export interface RemoveBreakpoint extends FrontendRequest {
  type: 'remove_breakpoint';
  instructionIndex: number;
}

export interface RunToNextBreakpoint extends FrontendRequest {
  type: 'run_to_next_breakpoint';
}

export interface RunToPreviousBreakpoint extends FrontendRequest {
  type: 'run_to_previous_breakpoint';
}

export interface ClearBreakpoints extends FrontendRequest {
  type: 'clear_breakpoints';
}

export interface StateMessage {
  type: 'state';
  program: AnnotatedKsplangTree;
  ip: number;
  step: bigint;
  stack: bigint[];
  reversed: boolean;
  error: string | null;
  breakpoints: number[];
}

// Program tree types
export type AnnotatedKsplangTree = KsplangOp | KsplangBlock | KsplangRoot;

export interface KsplangOp {
  type: 'op';
  instruction: string;
}

export interface KsplangBlock {
  type: 'block';
  name?: string | null;
  blockType: BlockType;
  children: AnnotatedKsplangTree[];
}

export interface KsplangRoot {
  type: 'root';
  children: AnnotatedKsplangTree[];
}

export type BlockType = InlinedFunction | FunctionCall;

export interface InlinedFunction {
  type: 'inlined_function';
}

export interface FunctionCall {
  type: 'function_call';
}

// UI state types
export interface DebuggerState {
  currentState: StateMessage | null;
  connected: boolean;
  connecting: boolean;
}

import type { KsplangOp } from '../types/debugger';

interface InstructionProps {
  instruction: KsplangOp;
  isCurrentInstruction?: boolean;
}

export function Instruction({ instruction, isCurrentInstruction = false }: InstructionProps) {
  const className = isCurrentInstruction 
    ? "px-2 py-1 font-mono text-sm bg-yellow-200 border-l-4 border-yellow-500"
    : "px-2 py-1 font-mono text-sm";

  return (
    <div className={className}>
      {instruction.instruction}
    </div>
  );
}

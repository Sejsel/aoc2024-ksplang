import { useState } from 'react';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Input } from './ui/input';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from './ui/dialog';
import type { StateMessage } from '../types/debugger';

interface StepControlsProps {
  currentState: StateMessage | null;
  connected: boolean;
  onStepTo: (instructionCount: bigint) => void;
  onRunToEnd: () => void;
  onRunToInstruction: (fromStep: bigint, instructionIndex: number) => void;
  onRunToInstructionBackwards: (fromStep: bigint, instructionIndex: number) => void;
  onRunToNextBreakpoint: () => void;
  onRunToPreviousBreakpoint: () => void;
  onClearBreakpoints: () => void;
  onLoadProgramFromClipboard: () => Promise<void>;
}

export function StepControls({ 
  currentState, 
  connected, 
  onStepTo, 
  onRunToEnd, 
  onRunToInstruction: _onRunToInstruction, 
  onRunToInstructionBackwards: _onRunToInstructionBackwards, 
  onRunToNextBreakpoint, 
  onRunToPreviousBreakpoint, 
  onClearBreakpoints, 
  onLoadProgramFromClipboard 
}: StepControlsProps) {
  const [confirmClearOpen, setConfirmClearOpen] = useState(false);

  const handleConfirmClear = () => {
    onClearBreakpoints();
    setConfirmClearOpen(false);
  };
  const currentStep = currentState?.step ?? BigInt(0);
  const breakpointCount = currentState?.breakpoints?.length ?? 0;
  const hasBreakpoints = breakpointCount > 0;

  const handleStepTo = (step: bigint) => {
    onStepTo(step);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = parseInt(e.target.value, 10);
    if (!isNaN(target) && target >= 0) {
      onStepTo(BigInt(target));
    }
  };

  return (
    <div className="border rounded-lg bg-card p-4">
      <h3 className="text-lg font-semibold mb-4">Step Controls</h3>
      
      {/* Program Loading */}
      <div className="flex items-center gap-2 mb-4 pb-4 border-b">
        <Button 
          onClick={onLoadProgramFromClipboard}
          disabled={!connected}
          variant="outline"
          size="sm"
          title="Load program from clipboard (JSON)"
          className="text-slate-600 hover:text-slate-700 hover:border-slate-300 dark:text-slate-400 dark:hover:text-slate-300"
        >
          📋 Load Program
        </Button>
        <span className="text-xs text-muted-foreground">Load program from clipboard JSON</span>
      </div>
      
      {/* Control Buttons */}
      <div className="flex items-center gap-2 mb-4">
        <Button 
          onClick={() => handleStepTo(BigInt(0))}
          disabled={!connected}
          variant="outline"
          size="sm"
          title="To start"
        >
          ↺
        </Button>
        
        <Button 
          onClick={() => handleStepTo(currentStep >= 10n ? currentStep - 10n : BigInt(0))}
          disabled={!connected || !currentState}
          variant="outline"
          size="sm"
          title="10 steps back"
        >
          ←←
        </Button>
        
        <Button 
          onClick={() => handleStepTo(currentStep >= 1n ? currentStep - 1n : BigInt(0))}
          disabled={!connected || !currentState}
          variant="outline"
          size="sm"
          title="1 step back"
        >
          ←
        </Button>

        <Input
          type="number"
          min="0"
          value={currentStep.toString()}
          onChange={handleInputChange}
          className="w-20 text-center [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none [-moz-appearance:textfield]"
          disabled={!connected}
        />
        
        <Button 
          onClick={() => handleStepTo(currentStep + 1n)}
          disabled={!connected || !currentState}
          variant="outline"
          size="sm"
          title="1 step forward"
        >
          →
        </Button>
        
        <Button 
          onClick={() => handleStepTo(currentStep + 10n)}
          disabled={!connected || !currentState}
          variant="outline"
          size="sm"
          title="10 steps forward"
        >
          →→
        </Button>

        <Button 
          onClick={onRunToEnd}
          disabled={!connected}
          variant="outline"
          size="sm"
          title="Run to end"
        >
          🏁
        </Button>
      </div>

      {/* Breakpoint Navigation Controls */}
      <div className="flex items-center gap-2 mb-4">
        <span className="text-sm font-medium text-muted-foreground">Breakpoints:</span>
        <Badge variant="secondary" className="text-xs">
          {breakpointCount} set
        </Badge>
        
        <Button 
          onClick={onRunToPreviousBreakpoint}
          disabled={!connected || !hasBreakpoints}
          variant="outline"
          size="sm"
          title="Run to previous breakpoint"
          className="text-rose-600 hover:text-rose-700 hover:border-rose-300 disabled:text-gray-400 dark:text-rose-400 dark:hover:text-rose-300"
        >
          ◀●
        </Button>
        
        <Button 
          onClick={onRunToNextBreakpoint}
          disabled={!connected || !hasBreakpoints}
          variant="outline"
          size="sm"
          title="Run to next breakpoint"
          className="text-rose-600 hover:text-rose-700 hover:border-rose-300 disabled:text-gray-400 dark:text-rose-400 dark:hover:text-rose-300"
        >
          ●▶
        </Button>
        
        <Dialog open={confirmClearOpen} onOpenChange={setConfirmClearOpen}>
          <DialogTrigger asChild>
            <Button 
              disabled={!connected || !hasBreakpoints}
              variant="outline"
              size="sm"
              title="Clear all breakpoints"
              className="text-rose-600 hover:text-rose-700 hover:border-rose-300 disabled:text-gray-400 dark:text-rose-400 dark:hover:text-rose-300"
            >
              ✕
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Clear All Breakpoints</DialogTitle>
              <DialogDescription>
                Are you sure you want to clear all {breakpointCount} breakpoint{breakpointCount !== 1 ? 's' : ''}? This action cannot be undone.
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button 
                variant="outline" 
                onClick={() => setConfirmClearOpen(false)}
              >
                Cancel
              </Button>
              <Button 
                variant="destructive"
                onClick={handleConfirmClear}
              >
                Clear All
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Current State Display */}
      <div className="grid grid-cols-2 gap-4 text-sm mb-4">
        <div className="flex items-center gap-2">
          <span className="font-medium">IP:</span>
          <Badge variant="outline" className="font-mono">
            {currentState?.ip ?? 'N/A'}
          </Badge>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-medium">Reversed:</span>
          <Badge variant={currentState?.reversed ? "default" : "secondary"}>
            {currentState?.reversed ? 'Yes' : 'No'}
          </Badge>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-medium">Connection:</span>
          <Badge variant={connected ? "default" : "destructive"} className={connected ? "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950 dark:text-emerald-300 dark:border-emerald-800" : ""}>
            {connected ? 'Connected' : 'Disconnected'}
          </Badge>
        </div>
      </div>

      {/* Error Display */}
      {currentState?.error && (
        <div className="p-2 bg-rose-50 border border-rose-200 rounded text-rose-700 text-sm dark:bg-rose-950 dark:border-rose-800 dark:text-rose-300">
          <span className="font-medium">Error:</span> {currentState.error}
        </div>
      )}
    </div>
  );
}

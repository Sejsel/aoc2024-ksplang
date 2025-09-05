import { useState } from 'react';
import type { StateMessage } from '../types/debugger';
import { Badge } from './ui/badge';
import { Checkbox } from './ui/checkbox';
import { Button } from './ui/button';
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

interface StackDisplayProps {
  currentState: StateMessage | null;
  onSetStack?: (stack: bigint[]) => void;
}

// Special values for 64-bit signed integers
const MIN_I64 = -9223372036854775808n; // -2^63
const MAX_I64 = 9223372036854775807n;  // 2^63 - 1

type DisplayMode = 'full' | 'compact' | 'binary';

function formatStackValue(value: bigint, displayMode: DisplayMode): string {
  switch (displayMode) {
    case 'binary':
      // Convert to 64-bit two's complement binary representation
      const buffer = new ArrayBuffer(8);
      const view = new DataView(buffer);
      view.setBigInt64(0, value, false); // false = big-endian
      
      let binary = '';
      for (let i = 0; i < 8; i++) {
        const byte = view.getUint8(i);
        binary += byte.toString(2).padStart(8, '0');
      }
      
      // Add spacing for readability (every 8 bits)
      return binary.replace(/(.{8})/g, '$1 ').trim();
      
    case 'compact':
      if (value === MIN_I64) {
        return '-2^63';
      } else if (value === MIN_I64 + 1n) {
        return '-2^63+1';
      } else if (value === MAX_I64) {
        return '2^63-1';
      } else {
        return value.toString();
      }
      
    case 'full':
    default:
      return value.toString();
  }
}

export function StackDisplay({ currentState, onSetStack }: StackDisplayProps) {
  const [displayMode, setDisplayMode] = useState<DisplayMode>('compact');
  const [alignNumbers, setAlignNumbers] = useState(true);
  const [stackInput, setStackInput] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const parseStackInput = (input: string): bigint[] => {
    // Split by any non-digit characters except minus sign
    // This allows separation by whitespace, commas, or any other non-numeric characters
    // Examples: "1 2 3", "1,2,3", "1;2;3", "1|2|3", "1    2    3" all work
    return input
      .split(/[^\d-]+/)
      .map(s => s.trim())
      .filter(s => s.length > 0 && s !== '-') // Filter out empty strings and standalone minus signs
      .map(s => BigInt(s));
  };

  const handleSetStack = () => {
    if (!onSetStack) return;
    
    try {
      const values = parseStackInput(stackInput);
      onSetStack(values);
      setStackInput(''); // Clear input after successful set
      setDialogOpen(false); // Close dialog
    } catch (error) {
      alert('Invalid stack values. Please enter space or comma-separated integers.');
    }
  };
  
  return (
    <div className="border rounded-lg bg-white p-4 flex flex-col h-full">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-lg font-semibold">Stack</h3>
        <div className="flex items-center gap-2">
          <Badge variant="secondary">
            {currentState?.stack.length || 0} items
          </Badge>
          {/* Set Stack Button */}
          {onSetStack && (
            <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
              <DialogTrigger asChild>
                <Button variant="outline" size="sm" className="text-xs">
                  Set
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>Set Initial Stack</DialogTitle>
                  <DialogDescription>
                    Enter integers separated by spaces, commas, or any other characters.
                    Example: "1 2 3" or "1, 2, 3" or "1;2;3"
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <Input
                    value={stackInput}
                    onChange={(e) => setStackInput(e.target.value)}
                    placeholder="1 2 3 4 -5 9223372036854775807"
                    className="font-mono"
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        handleSetStack();
                      }
                    }}
                  />
                </div>
                <DialogFooter>
                  <Button 
                    onClick={handleSetStack}
                    disabled={!stackInput.trim()}
                  >
                    Set Stack
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          )}
          <select 
            value={displayMode}
            onChange={(e) => setDisplayMode(e.target.value as DisplayMode)}
            className="text-xs border rounded px-2 py-1 bg-white text-gray-700"
          >
            <option value="full">Full</option>
            <option value="compact">Compact</option>
            <option value="binary">Binary</option>
          </select>
          <label className="flex items-center cursor-pointer gap-1">
            <Checkbox 
              checked={alignNumbers}
              onCheckedChange={(checked) => setAlignNumbers(checked === true)}
              className="h-4 w-4"
            />
            <span className="text-xs text-gray-600">Align</span>
          </label>
        </div>
      </div>
      
      <div className="flex-1 overflow-hidden">
        {currentState?.stack.length ? (
          <div className="h-full overflow-y-auto font-mono text-sm">
            {alignNumbers ? (
              // Aligned grid layout (adjust columns based on display mode)
              Array.from({ length: Math.ceil(currentState.stack.length / (displayMode === 'binary' ? 2 : 8)) }, (_, rowIndex) => {
                const itemsPerRow = displayMode === 'binary' ? 2 : 8;
                const startIndex = rowIndex * itemsPerRow;
                const rowItems = currentState.stack.slice(startIndex, startIndex + itemsPerRow);
                
                return (
                  <div key={rowIndex} className="flex items-center mb-2">
                    <div className="w-8 text-gray-500 text-right mr-2 text-xs">
                      {startIndex}:
                    </div>
                    <div className={`flex-1 grid gap-1 ${displayMode === 'binary' ? 'grid-cols-2' : 'grid-cols-8'}`}>
                      {rowItems.map((value, colIndex) => {
                        const formattedValue = formatStackValue(value, displayMode);
                        return (
                          <div 
                            key={startIndex + colIndex} 
                            className={`text-center text-xs px-1 py-1 bg-gray-50 rounded overflow-hidden ${displayMode === 'binary' ? 'font-mono text-[10px]' : ''}`}
                            title={`${value.toString()} (${displayMode === 'binary' ? 'binary' : 'decimal'})`}
                          >
                            <div className="truncate">
                              {formattedValue}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })
            ) : (
              // Unaligned flow layout
              <div className="flex flex-wrap gap-1">
                {currentState.stack.map((value, index) => {
                  const formattedValue = formatStackValue(value, displayMode);
                  return (
                    <div 
                      key={index} 
                      className={`text-xs px-2 py-1 bg-gray-50 rounded border ${displayMode === 'binary' ? 'font-mono text-[10px]' : ''}`}
                      title={`${index}: ${value.toString()} (${displayMode === 'binary' ? 'binary' : 'decimal'})`}
                    >
                      {formattedValue}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        ) : (
          <div className="flex items-center justify-center h-full text-gray-500">
            Empty Stack
          </div>
        )}
      </div>
    </div>
  );
}

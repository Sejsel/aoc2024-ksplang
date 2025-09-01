import { useState } from 'react';
import type { StateMessage } from '../types/debugger';
import { Badge } from './ui/badge';
import { Checkbox } from './ui/checkbox';

interface StackDisplayProps {
  currentState: StateMessage | null;
}

// Special values for 64-bit signed integers
const MIN_I64 = -9223372036854775808n; // -2^63
const MAX_I64 = 9223372036854775807n;  // 2^63 - 1

function formatStackValue(value: bigint, useCompactNotation: boolean): string {
  if (!useCompactNotation) {
    return value.toString();
  }
  
  if (value === MIN_I64) {
    return '-2^63';
  } else if (value === MIN_I64 + 1n) {
    return '-2^63+1';
  } else if (value === MAX_I64) {
    return '2^63-1';
  } else {
    return value.toString();
  }
}

export function StackDisplay({ currentState }: StackDisplayProps) {
  const [useCompactNotation, setUseCompactNotation] = useState(true);
  const [alignNumbers, setAlignNumbers] = useState(true);
  
  return (
    <div className="border rounded-lg bg-white p-4 flex flex-col h-full">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-lg font-semibold">Stack</h3>
        <div className="flex items-center gap-2">
          <Badge variant="secondary">
            {currentState?.stack.length || 0} items
          </Badge>
          <label className="flex items-center cursor-pointer gap-1">
            <Checkbox 
              checked={useCompactNotation}
              onCheckedChange={(checked) => setUseCompactNotation(checked === true)}
              className="h-4 w-4"
            />
            <span className="text-xs text-gray-600">Compact</span>
          </label>
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
              // Aligned grid layout (8 per row)
              Array.from({ length: Math.ceil(currentState.stack.length / 8) }, (_, rowIndex) => {
                const startIndex = rowIndex * 8;
                const rowItems = currentState.stack.slice(startIndex, startIndex + 8);
                
                return (
                  <div key={rowIndex} className="flex items-center mb-2">
                    <div className="w-8 text-gray-500 text-right mr-2 text-xs">
                      {startIndex}:
                    </div>
                    <div className="flex-1 grid grid-cols-8 gap-1">
                      {rowItems.map((value, colIndex) => {
                        const formattedValue = formatStackValue(value, useCompactNotation);
                        return (
                          <div 
                            key={startIndex + colIndex} 
                            className="text-center text-xs px-1 py-1 bg-gray-50 rounded overflow-hidden"
                            title={value.toString()}
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
                  const formattedValue = formatStackValue(value, useCompactNotation);
                  return (
                    <div 
                      key={index} 
                      className="text-xs px-2 py-1 bg-gray-50 rounded border"
                      title={`${index}: ${value.toString()}`}
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

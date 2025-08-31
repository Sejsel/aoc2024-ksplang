import type { StateMessage } from '../types/debugger';
import { Badge } from './ui/badge';

interface StackDisplayProps {
  currentState: StateMessage | null;
}

export function StackDisplay({ currentState }: StackDisplayProps) {
  return (
    <div className="border rounded-lg bg-white p-4 flex flex-col h-full">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-lg font-semibold">Stack</h3>
        <Badge variant="secondary">
          {currentState?.stack.length || 0} items
        </Badge>
      </div>
      
      <div className="flex-1 overflow-hidden">
        {currentState?.stack.length ? (
          <div className="h-full overflow-y-auto font-mono text-sm">
            {Array.from({ length: Math.ceil(currentState.stack.length / 8) }, (_, rowIndex) => {
              const startIndex = rowIndex * 8;
              const rowItems = currentState.stack.slice(startIndex, startIndex + 8);
              
              return (
                <div key={rowIndex} className="flex items-center mb-2">
                  <div className="w-8 text-gray-500 text-right mr-2 text-xs">
                    {startIndex}:
                  </div>
                  <div className="flex-1 grid grid-cols-8 gap-1">
                    {rowItems.map((value, colIndex) => (
                      <div 
                        key={startIndex + colIndex} 
                        className="text-center text-xs px-1 py-1 bg-gray-50 rounded overflow-hidden"
                        title={value.toString()}
                      >
                        <div className="truncate">
                          {value.toString()}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
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

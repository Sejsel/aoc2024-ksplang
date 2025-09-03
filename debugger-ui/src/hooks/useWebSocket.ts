import { useState, useEffect, useCallback, useRef } from 'react';
import type { StateMessage, DebuggerState, StepTo } from '../types/debugger';

export function useWebSocket(url: string) {
  const [state, setState] = useState<DebuggerState>({
    currentState: null,
    connected: false,
    connecting: false,
  });
  
  const ws = useRef<WebSocket | null>(null);
  const reconnectTimeout = useRef<NodeJS.Timeout | null>(null);

  // Mock state for testing UI (remove this when real backend is available)
  const mockState = {
    type: 'state' as const,
    program: {
      type: 'root' as const,
      children: [
        {
          type: 'block' as const,
          name: 'factorial',
          blockType: { type: 'inlined_function' as const },
          children: [
            { type: 'op' as const, instruction: 'CS' },
            { type: 'op' as const, instruction: '++' },
            { type: 'op' as const, instruction: 'lensum' },
          ]
        },
        {
          type: 'block' as const,
          name: null,
          blockType: { type: 'function_call' as const },
          children: [
            { type: 'op' as const, instruction: 'pop2' },
            { type: 'op' as const, instruction: 'lroll' },
            { type: 'op' as const, instruction: 'swap' },
          ]
        }
      ]
    },
    ip: 1,
    step: BigInt(3),
    stack: [BigInt(1), BigInt(2), BigInt(42)],
    reversed: false,
    error: null,
    breakpoints: [2, 5] // Mock breakpoints at instruction indices 2 and 5
  };

  const connect = useCallback(() => {
    if (ws.current?.readyState === WebSocket.CONNECTING || ws.current?.readyState === WebSocket.OPEN) {
      return;
    }

    setState(prev => ({ ...prev, connecting: true }));

    try {
      ws.current = new WebSocket(url);

      ws.current.onopen = () => {
        setState(prev => ({ 
          ...prev, 
          connected: true, 
          connecting: false,
          currentState: mockState
        }));
      };

      ws.current.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as StateMessage;
          
          if (message.type === 'state') {
            const stateWithBigInt = {
              ...message,
              step: BigInt(message.step),
              stack: message.stack.map(val => BigInt(val))
            };
            
            setState(prev => ({ 
              ...prev, 
              currentState: stateWithBigInt 
            }));
          }
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      ws.current.onclose = () => {
        setState(prev => ({ 
          ...prev, 
          connected: false, 
          connecting: false 
        }));
        
        // Only reconnect if we don't already have a timeout scheduled
        if (!reconnectTimeout.current) {
          reconnectTimeout.current = setTimeout(() => {
            reconnectTimeout.current = null;
            connect();
          }, 1000);
        }
      };

      ws.current.onerror = () => {
        // If connection fails, use mock data for testing
        setState(prev => ({ 
          ...prev, 
          connected: false, 
          connecting: false,
          currentState: mockState
        }));
      };

    } catch (error) {
      // Fallback to mock data for testing
      setState(prev => ({ 
        ...prev, 
        connecting: false,
        currentState: mockState
      }));
    }
  }, [url]);

  const disconnect = useCallback(() => {
    if (reconnectTimeout.current) {
      clearTimeout(reconnectTimeout.current);
      reconnectTimeout.current = null;
    }
    
    if (ws.current) {
      ws.current.close();
      ws.current = null;
    }
  }, []);

  const sendMessage = useCallback((message: any) => {
    if (ws.current?.readyState === WebSocket.OPEN) {
      const serializedMessage = JSON.stringify(message, (_, value) =>
        typeof value === 'bigint' ? value.toString() : value
      );
      ws.current.send(serializedMessage);
    }
  }, []);

  const stepTo = useCallback((instructionCount: bigint) => {
    sendMessage({
      type: 'step_to',
      executedInstructions: instructionCount,
    } as StepTo);
  }, [sendMessage]);

  const runToEnd = useCallback(() => {
    sendMessage({
      type: 'run_to_end',
    });
  }, [sendMessage]);

  const runToInstruction = useCallback((fromStep: bigint, instructionIndex: number) => {
    sendMessage({
      type: 'run_to_instruction',
      fromStep: fromStep,
      instructionIndex: instructionIndex,
    });
  }, [sendMessage]);

  const runToInstructionBackwards = useCallback((fromStep: bigint, instructionIndex: number) => {
    sendMessage({
      type: 'run_to_instruction_backwards',
      fromStep: fromStep,
      instructionIndex: instructionIndex,
    });
  }, [sendMessage]);

  const addBreakpoint = useCallback((instructionIndex: number) => {
    sendMessage({
      type: 'add_breakpoint',
      instructionIndex: instructionIndex,
    });
  }, [sendMessage]);

  const removeBreakpoint = useCallback((instructionIndex: number) => {
    sendMessage({
      type: 'remove_breakpoint',
      instructionIndex: instructionIndex,
    });
  }, [sendMessage]);

  const toggleBreakpoint = useCallback((instructionIndex: number) => {
    const currentBreakpoints = state.currentState?.breakpoints || [];
    if (currentBreakpoints.includes(instructionIndex)) {
      removeBreakpoint(instructionIndex);
    } else {
      addBreakpoint(instructionIndex);
    }
  }, [state.currentState?.breakpoints, addBreakpoint, removeBreakpoint]);

  const runToNextBreakpoint = useCallback(() => {
    sendMessage({
      type: 'run_to_next_breakpoint',
    });
  }, [sendMessage]);

  const runToPreviousBreakpoint = useCallback(() => {
    sendMessage({
      type: 'run_to_previous_breakpoint',
    });
  }, [sendMessage]);

  const clearBreakpoints = useCallback(() => {
    sendMessage({
      type: 'clear_breakpoints',
    });
  }, [sendMessage]);

  useEffect(() => {
    connect();
    
    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  return {
    ...state,
    connect,
    disconnect,
    sendMessage,
    stepTo,
    runToEnd,
    runToInstruction,
    runToInstructionBackwards,
    addBreakpoint,
    removeBreakpoint,
    toggleBreakpoint,
    runToNextBreakpoint,
    runToPreviousBreakpoint,
    clearBreakpoints,
  };
}

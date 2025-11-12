import { useWebSocket } from './hooks/useWebSocket';
import { CodeDisplay } from './components/CodeDisplay';
import { StepControls } from './components/StepControls';
import { StackDisplay } from './components/StackDisplay';
import { ModeToggle } from './components/mode-toggle';
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels';
import './App.css';

function App() {
  const { 
    currentState, 
    connected, 
    connecting,
    stepTo,
    runToEnd,
    runToInstruction,
    runToInstructionBackwards,
    toggleBreakpoint,
    runToNextBreakpoint,
    runToPreviousBreakpoint,
    clearBreakpoints,
    setStack,
    loadProgramFromClipboard,
    loadProgramFromFile
  } = useWebSocket('ws://localhost:8080/ws');

  return (
    <div className="h-screen bg-background p-4 flex flex-col">
      {/* Header with theme toggle */}
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold text-foreground">KSP Debugger</h1>
        <ModeToggle />
      </div>
      
      <div className="flex flex-col h-full">
        <PanelGroup direction="horizontal" className="flex-1 min-h-0">
          {/* Code Display Panel */}
          <Panel defaultSize={67} minSize={30}>
            <CodeDisplay 
              program={currentState?.program || null}
              currentState={currentState}
              onRunToInstruction={runToInstruction}
              onRunToInstructionBackwards={runToInstructionBackwards}
              onToggleBreakpoint={toggleBreakpoint}
            />
          </Panel>
          
          <PanelResizeHandle className="w-1 bg-border hover:bg-muted transition-colors duration-200 cursor-col-resize" />
          
          {/* Controls and Stack Panel */}
          <Panel defaultSize={33} minSize={25}>
            <div className="flex flex-col gap-4 h-full">
              <div>
                              <StepControls
                currentState={currentState}
                connected={connected}
                onStepTo={stepTo}
                onRunToEnd={runToEnd}
                onRunToInstruction={runToInstruction}
                onRunToInstructionBackwards={runToInstructionBackwards}
                onRunToNextBreakpoint={runToNextBreakpoint}
                onRunToPreviousBreakpoint={runToPreviousBreakpoint}
                onClearBreakpoints={clearBreakpoints}
                onLoadProgramFromClipboard={loadProgramFromClipboard}
                onLoadProgramFromFile={loadProgramFromFile}
              />
              </div>
              <div className="flex-1 min-h-0">
                <StackDisplay
                  currentState={currentState}
                  onSetStack={setStack}
                />
              </div>
            </div>
          </Panel>
        </PanelGroup>

        {/* Connection Status Bar */}
        <div className="mt-6 text-center">
          {connecting && (
            <p className="text-blue-600">Connecting to debugger...</p>
          )}
          {!connected && !connecting && (
            <p className="text-red-600">
              Failed to connect to debugger at ws://localhost:8080/ws
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

export default App

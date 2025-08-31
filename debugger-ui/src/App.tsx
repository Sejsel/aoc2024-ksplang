import { useWebSocket } from './hooks/useWebSocket';
import { CodeDisplay } from './components/CodeDisplay';
import { StepControls } from './components/StepControls';
import { StackDisplay } from './components/StackDisplay';
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels';
import './App.css';

function App() {
  const { 
    currentState, 
    connected, 
    connecting,
    stepTo 
  } = useWebSocket('ws://localhost:8080/ws');

  return (
    <div className="h-screen bg-gray-50 p-4 flex flex-col">
      <div className="flex flex-col h-full">
        <header className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">
            ksplang Debugger
          </h1>
          <p className="text-gray-600 mt-2">
            Stack-based instruction set debugger
          </p>
        </header>

        <PanelGroup direction="horizontal" className="flex-1 min-h-0">
          {/* Code Display Panel */}
          <Panel defaultSize={67} minSize={30}>
            <CodeDisplay 
              program={currentState?.program || null}
              currentState={currentState}
            />
          </Panel>
          
          <PanelResizeHandle className="w-1 bg-gray-100 hover:bg-gray-200 transition-colors duration-200 cursor-col-resize" />
          
          {/* Controls and Stack Panel */}
          <Panel defaultSize={33} minSize={25}>
            <div className="flex flex-col gap-4 h-full">
              <div>
                <StepControls
                  currentState={currentState}
                  connected={connected}
                  onStepTo={stepTo}
                />
              </div>
              <div className="flex-1 min-h-0">
                <StackDisplay
                  currentState={currentState}
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

import { useWebSocket } from './hooks/useWebSocket';
import { CodeDisplay } from './components/CodeDisplay';
import { StepControls } from './components/StepControls';
import './App.css';

function App() {
  const { 
    currentState, 
    connected, 
    connecting,
    stepTo 
  } = useWebSocket('ws://localhost:8080/ws');

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-6xl mx-auto">
        <header className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">
            ksplang Debugger
          </h1>
          <p className="text-gray-600 mt-2">
            Stack-based instruction set debugger
          </p>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Code Display - Takes up 2/3 of the width on large screens */}
          <div className="lg:col-span-2">
            <CodeDisplay 
              program={currentState?.program || null}
              currentState={currentState}
            />
          </div>

          {/* Step Controls - Takes up 1/3 of the width on large screens */}
          <div>
            <StepControls
              currentState={currentState}
              connected={connected}
              onStepTo={stepTo}
            />
          </div>
        </div>

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

# AI Agent Development Instructions

## Project Overview
This is a React + TypeScript frontend for a debugger UI for "ksplang", a stack-based instruction set language. The frontend communicates with a websocket-based backend where the session lifecycle is tied to the websocket connection.

## Technology Stack
- **Build Tool**: Vite
- **Package Manager**: NPM
- **Frontend Framework**: React
- **Type System**: TypeScript
- **UI Components**: Shadcn UI
- **Communication**: WebSocket

## Core Development Principles

### KISS (Keep It Simple, Stupid)
- Avoid unnecessary complexity and over-engineering
- Choose simple solutions over clever ones
- Minimize abstractions unless they provide clear value

### Minimalism
- Do not add features, dependencies, or code that are not strictly needed
- Question every addition: "Is this absolutely necessary?"
- Remove unused code, imports, and dependencies

### Code Organization
- Use reusable custom hooks for shared logic
- Follow React best practices and patterns
- Organize components and utilities logically
- Keep components focused and single-purpose

### Performance First
- UI should never be wasteful with space or resources
- Optimize for rendering performance
- Use efficient data structures and algorithms
- Minimize re-renders and unnecessary computations

## Technical Requirements

### Stack Handling
- The ksplang stack uses **64-bit signed integers**
- **Always use `BigInt`** for stack operations and values
- Handle BigInt serialization/deserialization carefully

### WebSocket Communication
- All frontend-backend communication is via WebSocket
- Session ends when WebSocket connection closes
- Handle connection states gracefully (connecting, open, closed, error)
- Implement proper error handling and reconnection logic

### UI/UX Guidelines
- **Space Efficiency**: Use space wisely, avoid unnecessary padding/margins
- **Clarity**: Design for immediate understanding of debugger state
- **Compactness**: Information-dense layouts where appropriate
- **Responsiveness**: Handle different screen sizes appropriately
- Use Shadcn UI components as the foundation

## Implementation Guidelines

### Code Quality
- Use TypeScript for all code with strict type checking
- Write self-documenting code with clear variable/function names
- Add brief documentation for custom hooks and complex components
- Handle edge cases and error states

### Backend Integration
- Refer to the AsyncAPI document for websocket message formats
- Implement proper message typing based on the protocol
- Handle all message types defined in the AsyncAPI spec
- Validate incoming messages when necessary

### Development Workflow
- Use Vite for development server and builds
- Leverage TypeScript for catching errors early
- Test websocket integration thoroughly
- Optimize bundle size and loading performance

## Debugger-Specific Considerations
- Display stack state clearly and efficiently
- Show instruction execution flow
- Provide step-by-step debugging capabilities
- Handle large stack sizes gracefully
- Display 64-bit integers in user-friendly formats
- Implement efficient state management for debugging sessions

## What NOT to Do
- Don't add unnecessary animations or visual effects
- Don't create abstractions without clear benefits
- Don't ignore performance implications
- Don't add dependencies for simple functionality
- Don't create overly complex component hierarchies
- Don't waste screen real estate on decorative elements

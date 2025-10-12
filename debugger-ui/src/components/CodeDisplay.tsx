import { useState, useRef, useEffect, useMemo, memo, useCallback } from 'react';
import type { AnnotatedKsplangTree, StateMessage } from '../types/debugger';
import { Checkbox } from './ui/checkbox';

interface CodeDisplayProps {
  program: AnnotatedKsplangTree | null;
  currentState: StateMessage | null;
  onRunToInstruction: (fromStep: bigint, instructionIndex: number) => void;
  onRunToInstructionBackwards: (fromStep: bigint, instructionIndex: number) => void;
  onToggleBreakpoint: (instructionIndex: number) => void;
}

interface RenderNodeProps {
  node: AnnotatedKsplangTree;
  depth: number;
  currentIp: number;
  instructionMap: Map<AnnotatedKsplangTree, number>;
  showNumbers: boolean;
  autoScroll: boolean;
  previousIpRef: React.MutableRefObject<number>;
  scrollContainerRef: React.RefObject<HTMLDivElement | null>;
  onRunToInstruction: (fromStep: bigint, instructionIndex: number) => void;
  onRunToInstructionBackwards: (fromStep: bigint, instructionIndex: number) => void;
  onToggleBreakpoint: (instructionIndex: number) => void;
  currentStep: bigint;
  isCtrlPressed: boolean;
  breakpoints: number[];
}

function buildInstructionMap(node: AnnotatedKsplangTree, map: Map<AnnotatedKsplangTree, number>, counter: { current: number }): void {
  if (node.type === 'op') {
    map.set(node, counter.current++);
  } else if (node.type === 'block' || node.type === 'root') {
    for (let i = 0; i < node.children.length; i++) {
      buildInstructionMap(node.children[i], map, counter);
    }
  }
}

// Memoized function to build groups from children
function buildChildrenGroups(children: AnnotatedKsplangTree[]): Array<{ type: 'ops'; items: AnnotatedKsplangTree[] } | { type: 'block'; item: AnnotatedKsplangTree }> {
  const groups: Array<{ type: 'ops'; items: AnnotatedKsplangTree[] } | { type: 'block'; item: AnnotatedKsplangTree }> = [];
  let currentGroup: AnnotatedKsplangTree[] = [];
  
  for (const child of children) {
    if (child.type === 'op') {
      currentGroup.push(child);
    } else {
      if (currentGroup.length > 0) {
        groups.push({ type: 'ops', items: currentGroup });
        currentGroup = [];
      }
      groups.push({ type: 'block', item: child });
    }
  }
  
  if (currentGroup.length > 0) {
    groups.push({ type: 'ops', items: currentGroup });
  }
  
  return groups;
}

// Rainbow colors for light mode, subtle grays for dark mode
const RAINBOW_COLORS = [
  'border-red-200 dark:border-gray-700',
  'border-orange-200 dark:border-slate-700', 
  'border-yellow-200 dark:border-zinc-700',
  'border-green-200 dark:border-neutral-700',
  'border-blue-200 dark:border-stone-700',
  'border-indigo-200 dark:border-gray-600',
  'border-purple-200 dark:border-slate-600',
  'border-pink-200 dark:border-zinc-600'
];

// Memoized RenderNode component to prevent unnecessary re-renders
const RenderNode = memo(function RenderNode({ node, depth, currentIp, instructionMap, showNumbers, autoScroll, previousIpRef, scrollContainerRef, onRunToInstruction, onRunToInstructionBackwards, onToggleBreakpoint, currentStep, isCtrlPressed, breakpoints }: RenderNodeProps) {
  const borderColor = RAINBOW_COLORS[depth % RAINBOW_COLORS.length];

  // Move all hooks to the top - they must be called unconditionally
  const instructionIndex = useMemo(() => instructionMap.get(node) ?? -1, [instructionMap, node]);
  const isCurrentInstruction = currentIp === instructionIndex;
  const isBreakpoint = useMemo(() => breakpoints.length > 0 ? breakpoints.includes(instructionIndex) : false, [breakpoints, instructionIndex]);
  
  // Memoize groups building for block nodes (will be undefined for non-block nodes)
  const groups = useMemo(() => {
    return node.type === 'block' ? buildChildrenGroups(node.children) : undefined;
  }, [node]);
  
  // Memoize click handler to prevent recreation on every render
  const handleClick = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    if (e.shiftKey) {
      e.stopPropagation();
      window.getSelection()?.removeAllRanges();
      onToggleBreakpoint(instructionIndex);
    } else if (e.ctrlKey) {
      onRunToInstructionBackwards(currentStep, instructionIndex);
    } else {
      onRunToInstruction(currentStep, instructionIndex);
    }
  }, [instructionIndex, currentStep, onToggleBreakpoint, onRunToInstructionBackwards, onRunToInstruction]);

  if (node.type === 'op') {
    return (
      <span
        data-instruction-idx={instructionIndex}
        ref={isCurrentInstruction ? (el) => {
          if (el && autoScroll && scrollContainerRef?.current && previousIpRef.current !== currentIp) {
            const container = scrollContainerRef.current;
            const containerRect = container.getBoundingClientRect();
            const elementRect = el.getBoundingClientRect();
            const elementTop = elementRect.top - containerRect.top + container.scrollTop;
            const elementCenter = elementTop - container.clientHeight / 2 + el.clientHeight / 2;
            container.scrollTo({ top: elementCenter, behavior: 'smooth' });
            previousIpRef.current = currentIp;
          }
        } : undefined}
        className={`font-mono text-xs mr-1 px-1 py-0.5 rounded border cursor-pointer transition-colors ${
          isCurrentInstruction
            ? "bg-amber-50 border-amber-300 font-semibold text-amber-800 dark:bg-amber-950 dark:border-amber-700 dark:text-amber-200"
            : "text-foreground border-transparent hover:bg-slate-50 hover:border-slate-200 dark:hover:bg-slate-950 dark:hover:border-slate-800"
        } ${isCtrlPressed ? "hover:bg-rose-50 hover:border-rose-200 dark:hover:bg-rose-950 dark:hover:border-rose-800" : ""}`}
        title={`${instructionIndex}`}
        onClick={handleClick}
      >
        {isBreakpoint && (
          <span className="inline-block w-2 h-2 bg-red-300 rounded-full mr-1" title="Breakpoint"></span>
        )}
        {showNumbers && (
          <span className="text-muted-foreground text-xs mr-1">
            {instructionIndex.toString().padStart(2, '0')}
          </span>
        )}
        {node.instruction}
      </span>
    );
  }
  
  if (node.type === 'block') {
    const displayName = node.name || `[${node.blockType.type}]`;
    const isFunction = node.blockType.type === 'function_call';
    const emoji = isFunction ? ' 📞' : '';
    
    return (
      <div className="mb-2 ml-2">
        <div className="text-xs text-blue-600 dark:text-blue-400 mb-1 font-semibold bg-blue-50 dark:bg-blue-950/30 px-2 py-1 rounded">
          {displayName}{emoji}
        </div>
        <div className={`border-l-2 ${borderColor} pl-2`}>
          {groups!.map((group, groupIndex) => {
            if (group.type === 'ops') {
              return (
                <div key={`ops-${groupIndex}`} className="flex flex-wrap gap-1 items-center mb-1">
                  {group.items.map((opNode, opIndex) => (
                    <RenderNode 
                      key={`${groupIndex}-${opIndex}`}
                      node={opNode} 
                      depth={0} 
                      currentIp={currentIp}
                      instructionMap={instructionMap}
                      showNumbers={showNumbers}
                      autoScroll={autoScroll}
                      previousIpRef={previousIpRef}
                      scrollContainerRef={scrollContainerRef}
                      onRunToInstruction={onRunToInstruction}
                      onRunToInstructionBackwards={onRunToInstructionBackwards}
                      onToggleBreakpoint={onToggleBreakpoint}
                      currentStep={currentStep}
                      isCtrlPressed={isCtrlPressed}
                      breakpoints={breakpoints}
                    />
                  ))}
                </div>
              );
            } else {
              return (
                <div key={`block-${groupIndex}`} className="mb-1">
                  <RenderNode 
                    node={group.item} 
                    depth={depth + 1} 
                    currentIp={currentIp}
                    instructionMap={instructionMap}
                    showNumbers={showNumbers}
                    autoScroll={autoScroll}
                    previousIpRef={previousIpRef}
                    scrollContainerRef={scrollContainerRef}
                    onRunToInstruction={onRunToInstruction}
                    onRunToInstructionBackwards={onRunToInstructionBackwards}
                    onToggleBreakpoint={onToggleBreakpoint}
                    currentStep={currentStep}
                    isCtrlPressed={isCtrlPressed}
                    breakpoints={breakpoints}
                  />
                </div>
              );
            }
          })}
        </div>
      </div>
    );
  }
  
  if (node.type === 'root') {
    return (
      <div>
        {node.children.map((child, index) => (
          <RenderNode 
            key={index}
            node={child} 
            depth={depth} 
            currentIp={currentIp}
            instructionMap={instructionMap}
            showNumbers={showNumbers}
            autoScroll={autoScroll}
            previousIpRef={previousIpRef}
            scrollContainerRef={scrollContainerRef}
            onRunToInstruction={onRunToInstruction}
            onRunToInstructionBackwards={onRunToInstructionBackwards}
            onToggleBreakpoint={onToggleBreakpoint}
            currentStep={currentStep}
            isCtrlPressed={isCtrlPressed}
            breakpoints={breakpoints}
          />
        ))}
      </div>
    );
  }
  
  return null;
});

export function CodeDisplay({ program, currentState, onRunToInstruction, onRunToInstructionBackwards, onToggleBreakpoint }: CodeDisplayProps) {
  const [showNumbers, setShowNumbers] = useState(false);
  const [autoScroll, setAutoScroll] = useState(true);
  const [isCtrlPressed, setIsCtrlPressed] = useState(false);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const previousIpRef = useRef<number>(-1);
  
  // Memoize instruction map to avoid rebuilding it on every render
  const instructionMap = useMemo(() => {
    if (!program) return new Map<AnnotatedKsplangTree, number>();
    const map = new Map<AnnotatedKsplangTree, number>();
    const counter = { current: 0 };
    buildInstructionMap(program, map, counter);
    return map;
  }, [program]);

  // Memoize callback functions to prevent unnecessary re-renders
  const memoizedOnRunToInstruction = useCallback(onRunToInstruction, [onRunToInstruction]);
  const memoizedOnRunToInstructionBackwards = useCallback(onRunToInstructionBackwards, [onRunToInstructionBackwards]);
  const memoizedOnToggleBreakpoint = useCallback(onToggleBreakpoint, [onToggleBreakpoint]);

  // Memoize breakpoints array to prevent unnecessary re-renders
  const breakpoints = useMemo(() => currentState?.breakpoints ?? [], [currentState?.breakpoints]);
  
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.metaKey) {
        setIsCtrlPressed(true);
      }
    };
    
    const handleKeyUp = (e: KeyboardEvent) => {
      if (!e.ctrlKey && !e.metaKey) {
        setIsCtrlPressed(false);
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, []);
  
  if (!program) {
    return (
      <div className="p-4 border rounded-lg bg-muted">
        <p className="text-muted-foreground">Start by connecting to the server</p>
      </div>
    );
  }

  const currentIp = currentState?.ip ?? -1;
  const currentStep = currentState?.step ?? BigInt(0);

  return (
    <div ref={scrollContainerRef} className="border rounded-lg bg-card overflow-auto h-full">
      <div className="p-4 pb-8">
        <div className="flex items-center justify-between mb-4 text-foreground border-b pb-2">
          <h3 className="text-lg font-semibold">Program</h3>
          <div className="flex items-center gap-4">
            <label className="flex items-center text-sm text-muted-foreground cursor-pointer gap-2">
              <Checkbox 
                checked={autoScroll}
                onCheckedChange={(checked) => setAutoScroll(checked === true)}
              />
              Auto-scroll
            </label>
            <label className="flex items-center text-sm text-muted-foreground cursor-pointer gap-2">
              <Checkbox 
                checked={showNumbers}
                onCheckedChange={(checked) => setShowNumbers(checked === true)}
              />
              Instruction numbers
            </label>
          </div>
        </div>
        <div className="space-y-1">
          <RenderNode 
            node={program} 
            depth={0} 
            currentIp={currentIp}
            instructionMap={instructionMap}
            showNumbers={showNumbers}
            autoScroll={autoScroll}
            previousIpRef={previousIpRef}
            scrollContainerRef={scrollContainerRef}
            onRunToInstruction={memoizedOnRunToInstruction}
            onRunToInstructionBackwards={memoizedOnRunToInstructionBackwards}
            onToggleBreakpoint={memoizedOnToggleBreakpoint}
            currentStep={currentStep}
            isCtrlPressed={isCtrlPressed}
            breakpoints={breakpoints}
          />
        </div>
      </div>
    </div>
  );
}

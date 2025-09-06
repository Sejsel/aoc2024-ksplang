import { useState, useRef, useEffect } from 'react';
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
    map.set(node, counter.current);
    counter.current++;
  } else if (node.type === 'block' || node.type === 'root') {
    for (const child of node.children) {
      buildInstructionMap(child, map, counter);
    }
  }
}

function RenderNode({ node, depth, currentIp, instructionMap, showNumbers, autoScroll, previousIpRef, scrollContainerRef, onRunToInstruction, onRunToInstructionBackwards, onToggleBreakpoint, currentStep, isCtrlPressed, breakpoints, hoveredInstruction, setHoveredInstruction }: RenderNodeProps & { hoveredInstruction: number | null, setHoveredInstruction: (idx: number | null) => void }) {
  // Subtle colors for block hierarchy (very faint, theme-aware)
  const rainbowColors = [
    'border-gray-200 dark:border-gray-700',
    'border-slate-200 dark:border-slate-700', 
    'border-zinc-200 dark:border-zinc-700',
    'border-neutral-200 dark:border-neutral-700',
    'border-stone-200 dark:border-stone-700',
    'border-gray-300 dark:border-gray-600',
    'border-slate-300 dark:border-slate-600',
    'border-zinc-300 dark:border-zinc-600'
  ];
  
  const borderColor = rainbowColors[depth % rainbowColors.length];

  if (node.type === 'op') {
    const instructionIndex = instructionMap.get(node) ?? -1;
    const isCurrentInstruction = instructionIndex === currentIp;
    const isHovered = hoveredInstruction === instructionIndex;
    const isBreakpoint = breakpoints.includes(instructionIndex);

    const handleClick = (e: React.MouseEvent) => {
      e.preventDefault();
      if (e.shiftKey) {
        e.stopPropagation();
        // Prevent text selection on shift+click
        window.getSelection()?.removeAllRanges();
        onToggleBreakpoint(instructionIndex);
      } else if (e.ctrlKey) {
        onRunToInstructionBackwards(currentStep, instructionIndex);
      } else {
        onRunToInstruction(currentStep, instructionIndex);
      }
    };

    useEffect(() => {
      if (!isCurrentInstruction && isHovered) {
        const el = document.querySelector(`[data-instruction-idx='${instructionIndex}']`);
        if (el) {
          if (isCtrlPressed) {
            el.classList.add('bg-rose-50', 'border-rose-200', 'dark:bg-rose-950', 'dark:border-rose-800');
            el.classList.remove('bg-slate-50', 'border-slate-200', 'dark:bg-slate-950', 'dark:border-slate-800');
          } else {
            el.classList.add('bg-slate-50', 'border-slate-200', 'dark:bg-slate-950', 'dark:border-slate-800');
            el.classList.remove('bg-rose-50', 'border-rose-200', 'dark:bg-rose-950', 'dark:border-rose-800');
          }
        }
      }
    }, [isCtrlPressed, isHovered, isCurrentInstruction, instructionIndex]);

    return (
      <span
        data-instruction-idx={instructionIndex}
        ref={isCurrentInstruction ? (el) => {
          // Only scroll if auto-scroll is enabled and the instruction actually changed
          if (el && autoScroll && scrollContainerRef && scrollContainerRef.current && previousIpRef.current !== currentIp) {
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
            : isHovered
              ? isCtrlPressed
                ? "bg-rose-50 border-rose-200 dark:bg-rose-950 dark:border-rose-800"
                : "bg-slate-50 border-slate-200 dark:bg-slate-950 dark:border-slate-800"
              : "text-foreground border-transparent"
        }`}
        title={`Instruction ${instructionIndex}: ${node.instruction} | Click to run to here | Ctrl+Click to run backwards to here | Shift+Click to toggle breakpoint`}
        onClick={handleClick}
        onMouseEnter={() => !isCurrentInstruction && setHoveredInstruction(instructionIndex)}
        onMouseLeave={() => !isCurrentInstruction && setHoveredInstruction(null)}
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
        <div className="text-xs text-muted-foreground mb-1 font-medium">
          {displayName}{emoji}
        </div>
        <div className={`border-l-2 ${borderColor} pl-2`}>
          {/* Group consecutive operations and preserve order */}
          {(() => {
            const groups: Array<{ type: 'ops'; items: AnnotatedKsplangTree[] } | { type: 'block'; item: AnnotatedKsplangTree }> = [];
            let currentGroup: AnnotatedKsplangTree[] = [];
            
            for (const child of node.children) {
              if (child.type === 'op') {
                currentGroup.push(child);
              } else {
                // Non-op node encountered, finalize current group
                if (currentGroup.length > 0) {
                  groups.push({ type: 'ops', items: currentGroup });
                  currentGroup = [];
                }
                groups.push({ type: 'block', item: child });
              }
            }
            
            // Don't forget the last group
            if (currentGroup.length > 0) {
              groups.push({ type: 'ops', items: currentGroup });
            }
            
            return groups.map((group, groupIndex) => {
              if (group.type === 'ops') {
                return (
                  <div key={`ops-${groupIndex}`} className="flex flex-wrap gap-1 items-center mb-1">
                    {group.items.map((opNode, opIndex) => (
                      <RenderNode 
                        key={`op-${groupIndex}-${opIndex}`}
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
                        hoveredInstruction={hoveredInstruction}
                        setHoveredInstruction={setHoveredInstruction}
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
                      hoveredInstruction={hoveredInstruction}
                      setHoveredInstruction={setHoveredInstruction}
                    />
                  </div>
                );
              }
            });
          })()}
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
            hoveredInstruction={hoveredInstruction}
            setHoveredInstruction={setHoveredInstruction}
          />
        ))}
      </div>
    );
  }
  
  return null;
}

export function CodeDisplay({ program, currentState, onRunToInstruction, onRunToInstructionBackwards, onToggleBreakpoint }: CodeDisplayProps) {
  const [showNumbers, setShowNumbers] = useState(false);
  const [autoScroll, setAutoScroll] = useState(true);
  const [isCtrlPressed, setIsCtrlPressed] = useState(false);
  const [hoveredInstruction, setHoveredInstruction] = useState<number | null>(null);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const previousIpRef = useRef<number>(-1);
  
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
  const breakpoints = currentState?.breakpoints ?? [];
  
  // Build instruction map once
  const instructionMap = new Map<AnnotatedKsplangTree, number>();
  const counter = { current: 0 };
  buildInstructionMap(program, instructionMap, counter);

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
            onRunToInstruction={onRunToInstruction}
            onRunToInstructionBackwards={onRunToInstructionBackwards}
            onToggleBreakpoint={onToggleBreakpoint}
            currentStep={currentStep}
            isCtrlPressed={isCtrlPressed}
            breakpoints={breakpoints}
            hoveredInstruction={hoveredInstruction}
            setHoveredInstruction={setHoveredInstruction}
          />
        </div>
      </div>
    </div>
  );
}

import { useState, useRef, useEffect, useMemo, memo, useCallback } from 'react';
import { List, type ListImperativeAPI } from 'react-window';
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
  maxOpsRowWidth: number;
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

// Types for flattened rendering
type FlatItem = 
  | { type: 'block-header'; node: AnnotatedKsplangTree; depth: number }
  | { type: 'ops-row'; nodes: AnnotatedKsplangTree[]; depth: number }
  | { type: 'block-end'; depth: number };

// Flatten the tree structure for virtualized rendering
function flattenTree(node: AnnotatedKsplangTree, depth: number = 0): FlatItem[] {
  const items: FlatItem[] = [];
  
  if (node.type === 'op') {
    // Single op - should be part of an ops-row
    items.push({ type: 'ops-row', nodes: [node], depth });
  } else if (node.type === 'block') {
    // Block header
    items.push({ type: 'block-header', node, depth });
    
    // Group children into ops and blocks
    const groups = buildChildrenGroups(node.children);
    for (const group of groups) {
      if (group.type === 'ops') {
        items.push({ type: 'ops-row', nodes: group.items, depth: depth + 1 });
      } else {
        items.push(...flattenTree(group.item, depth + 1));
      }
    }
  } else if (node.type === 'root') {
    // Process root children
    for (const child of node.children) {
      items.push(...flattenTree(child, depth));
    }
  }
  
  return items;
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

// Actual color values for inline styles (virtualized rendering)
const BORDER_COLOR_VALUES = [
  '#fecaca', // red-200
  '#fed7aa', // orange-200
  '#fef08a', // yellow-200
  '#bbf7d0', // green-200
  '#bfdbfe', // blue-200
  '#c7d2fe', // indigo-200
  '#e9d5ff', // purple-200
  '#fbcfe8'  // pink-200
];

// Memoized RenderNode component to prevent unnecessary re-renders
const RenderNode = memo(function RenderNode({ node, depth, currentIp, instructionMap, showNumbers, autoScroll, previousIpRef, scrollContainerRef, onRunToInstruction, onRunToInstructionBackwards, onToggleBreakpoint, currentStep, isCtrlPressed, breakpoints, maxOpsRowWidth }: RenderNodeProps) {
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
        style={{ width: '80px', minWidth: '80px', maxWidth: '80px' }}
        className={`font-mono text-xs px-1 py-0.5 rounded border cursor-pointer transition-colors inline-flex items-center justify-center overflow-hidden text-ellipsis whitespace-nowrap ${
          isCurrentInstruction
            ? "bg-amber-50 border-amber-300 font-semibold text-amber-800 dark:bg-amber-950 dark:border-amber-700 dark:text-amber-200"
            : "text-foreground border-transparent hover:bg-slate-50 hover:border-slate-200 dark:hover:bg-slate-950 dark:hover:border-slate-800"
        } ${isCtrlPressed ? "hover:bg-rose-50 hover:border-rose-200 dark:hover:bg-rose-950 dark:hover:border-rose-800" : ""}`}
        title={`${instructionIndex}: ${node.instruction}`}
        onClick={handleClick}
      >
        {isBreakpoint && (
          <span className="inline-block w-2 h-2 bg-red-300 rounded-full mr-1 flex-shrink-0" title="Breakpoint"></span>
        )}
        {showNumbers && (
          <span className="text-muted-foreground text-xs mr-1 flex-shrink-0">
            {instructionIndex.toString().padStart(2, '0')}
          </span>
        )}
        <span className="truncate">{node.instruction}</span>
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
                <div key={`ops-${groupIndex}`} className="flex flex-wrap gap-1 items-center mb-1" style={{ maxWidth: `${maxOpsRowWidth}px` }}>
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
                      maxOpsRowWidth={maxOpsRowWidth}
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
                    maxOpsRowWidth={maxOpsRowWidth}
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
            maxOpsRowWidth={maxOpsRowWidth}
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
  const [useVirtualization, setUseVirtualization] = useState(true);
  const [containerWidth, setContainerWidth] = useState(0);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const listRef = useRef<ListImperativeAPI | null>(null);
  const previousIpRef = useRef<number>(-1);
  const codeContainerRef = useRef<HTMLDivElement | null>(null);
  
  // Memoize instruction map to avoid rebuilding it on every render
  const instructionMap = useMemo(() => {
    if (!program) return new Map<AnnotatedKsplangTree, number>();
    const map = new Map<AnnotatedKsplangTree, number>();
    const counter = { current: 0 };
    buildInstructionMap(program, map, counter);
    return map;
  }, [program]);

  // Flatten the tree for virtualized rendering
  const flatItems = useMemo(() => {
    if (!program) return [];
    return flattenTree(program);
  }, [program]);

  // Build a map from instruction index to flatItem index for fast lookup
  const instructionToFlatItemMap = useMemo(() => {
    const map = new Map<number, number>();
    for (let i = 0; i < flatItems.length; i++) {
      const item = flatItems[i];
      if (item.type === 'ops-row') {
        for (const node of item.nodes) {
          if (node.type === 'op') {
            const instructionIdx = instructionMap.get(node);
            if (instructionIdx !== undefined) {
              map.set(instructionIdx, i);
            }
          }
        }
      }
    }
    return map;
  }, [flatItems, instructionMap]);

  // Memoize callback functions to prevent unnecessary re-renders
  const memoizedOnRunToInstruction = useCallback(onRunToInstruction, [onRunToInstruction]);
  const memoizedOnRunToInstructionBackwards = useCallback(onRunToInstructionBackwards, [onRunToInstructionBackwards]);
  const memoizedOnToggleBreakpoint = useCallback(onToggleBreakpoint, [onToggleBreakpoint]);

  // Memoize breakpoints array to prevent unnecessary re-renders
  const breakpoints = useMemo(() => currentState?.breakpoints ?? [], [currentState?.breakpoints]);
  
  // Calculate ops per line based on container width
  const opsPerLine = useMemo(() => {
    if (containerWidth === 0) return 18; // Default
    // Each op is 80px wide, gap is 4px
    // Available width = containerWidth - padding (32px for px-4 on both sides)
    const availableWidth = containerWidth - 32;
    const opWidth = 80;
    const gapWidth = 4;
    // Calculate how many ops fit: width = n * opWidth + (n - 1) * gapWidth
    // Solving for n: width = n * (opWidth + gapWidth) - gapWidth
    // n = (width + gapWidth) / (opWidth + gapWidth)
    const calculated = Math.floor((availableWidth + gapWidth) / (opWidth + gapWidth));
    return Math.max(1, Math.min(calculated, 18)); // At least 1, at most 18
  }, [containerWidth]);

  // Calculate max width for ops-row based on opsPerLine
  const maxOpsRowWidth = useMemo(() => {
    return opsPerLine * 80 + (opsPerLine - 1) * 4; // ops * width + gaps * width
  }, [opsPerLine]);

  // Calculate actual container height for the list
  const [containerHeight, setContainerHeight] = useState(0);

  // Observe container width and height changes
  useEffect(() => {
    const container = codeContainerRef.current;
    if (!container) return;

    const resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const newWidth = entry.contentRect.width;
        const newHeight = entry.contentRect.height;
        
        // Only update if width actually changed
        setContainerWidth(prevWidth => {
          if (newWidth !== prevWidth) {
            return newWidth;
          }
          return prevWidth;
        });
        
        // Update height for list rendering
        setContainerHeight(prevHeight => {
          if (newHeight !== prevHeight) {
            return newHeight;
          }
          return prevHeight;
        });
      }
    });

    resizeObserver.observe(container);
    
    // Initial measurement - force it synchronously
    const rect = container.getBoundingClientRect();
    if (rect.width > 0) {
      setContainerWidth(rect.width);
    }
    if (rect.height > 0) {
      setContainerHeight(rect.height);
    }

    return () => {
      resizeObserver.disconnect();
    };
  }, [program]); // Re-run when program changes to ensure measurement after load
  
  const currentIp = currentState?.ip ?? -1;
  const currentStep = currentState?.step ?? BigInt(0);

  // Find which flat item contains the current instruction
  const currentItemIndex = useMemo(() => {
    if (currentIp === -1 || !useVirtualization) return -1;
    return instructionToFlatItemMap.get(currentIp) ?? -1;
  }, [currentIp, instructionToFlatItemMap, useVirtualization]);

  // Auto-scroll to current instruction in virtualized list
  useEffect(() => {
    if (autoScroll && useVirtualization && currentItemIndex !== -1 && listRef.current && previousIpRef.current !== currentIp) {
      listRef.current.scrollToRow({ index: currentItemIndex, align: 'center' });
      previousIpRef.current = currentIp;
    }
  }, [currentItemIndex, autoScroll, useVirtualization, currentIp]);

  // Calculate item height for virtualized list with accurate variable heights
  const getItemSize = useCallback((index: number, _rowProps: Record<string, never>) => {
    const item = flatItems[index];
    if (item.type === 'block-header') {
      // mb-2 (8px) + text-xs (12px) + mb-1 (4px) + px-2 py-1 (padding 8px+4px) = ~36px
      return 36;
    } else if (item.type === 'ops-row') {
      // Each op is 80px wide + 4px gap between items
      // Calculate how many lines this row needs based on wrapping
      const lines = Math.max(1, Math.ceil(item.nodes.length / opsPerLine));
      // Each line: py-0.5 (4px) + text (12-14px) + mb-1 (4px) = ~24px per line
      return lines * 24;
    }
    return 24; // Default for unknown types
  }, [flatItems, opsPerLine]);

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

  // Row renderer for virtualized list
  const Row = ({ index, style, ariaAttributes }: { index: number; style: React.CSSProperties; ariaAttributes: { 'aria-posinset': number; 'aria-setsize': number; role: 'listitem' } }) => {
    const item = flatItems[index];
    
    if (item.type === 'block-header') {
      // Type guard: ensure we have a block node
      const node = item.node;
      if (node.type !== 'block') return <div style={style} {...ariaAttributes}></div>;
      
      const displayName = node.name || `[${node.blockType.type}]`;
      const isFunction = node.blockType.type === 'function_call';
      const emoji = isFunction ? ' 📞' : '';
      
      return (
        <div style={{ ...style, marginLeft: `${item.depth * 8}px` }} {...ariaAttributes}>
          <div className="mb-2">
            <div className="text-xs text-blue-600 dark:text-blue-400 mb-1 font-semibold bg-blue-50 dark:bg-blue-950/30 px-2 py-1 rounded inline-block">
              {displayName}{emoji}
            </div>
          </div>
        </div>
      );
    } else if (item.type === 'ops-row') {
      const borderColorValue = BORDER_COLOR_VALUES[item.depth % BORDER_COLOR_VALUES.length];
      const marginLeft = item.depth * 8; // 8px per depth level
      
      return (
        <div 
          style={{ 
            ...style, 
            marginLeft: `${marginLeft}px`,
            borderLeft: item.depth > 0 ? `2px solid ${borderColorValue}` : 'none',
            paddingLeft: item.depth > 0 ? '8px' : '0',
            maxWidth: `${maxOpsRowWidth}px`
          }} 
          className="flex flex-wrap gap-1 items-center mb-1 overflow-x-auto"
          {...ariaAttributes}
        >
          {item.nodes.map((opNode, opIndex) => {
            // Type guard: ensure we have an op node
            if (opNode.type !== 'op') return null;
            
            const instructionIndex = instructionMap.get(opNode) ?? -1;
            const isCurrentInstruction = currentIp === instructionIndex;
            const isBreakpoint = breakpoints.includes(instructionIndex);
            
            const handleClick = (e: React.MouseEvent) => {
              e.preventDefault();
              if (e.shiftKey) {
                e.stopPropagation();
                window.getSelection()?.removeAllRanges();
                memoizedOnToggleBreakpoint(instructionIndex);
              } else if (e.ctrlKey) {
                memoizedOnRunToInstructionBackwards(currentStep, instructionIndex);
              } else {
                memoizedOnRunToInstruction(currentStep, instructionIndex);
              }
            };
            
            return (
              <span
                key={opIndex}
                data-instruction-idx={instructionIndex}
                style={{ width: '80px', minWidth: '80px', maxWidth: '80px' }}
                className={`font-mono text-xs px-1 py-0.5 rounded border cursor-pointer transition-colors inline-flex items-center justify-center overflow-hidden text-ellipsis whitespace-nowrap ${
                  isCurrentInstruction
                    ? "bg-amber-50 border-amber-300 font-semibold text-amber-800 dark:bg-amber-950 dark:border-amber-700 dark:text-amber-200"
                    : "text-foreground border-transparent hover:bg-slate-50 hover:border-slate-200 dark:hover:bg-slate-950 dark:hover:border-slate-800"
                } ${isCtrlPressed ? "hover:bg-rose-50 hover:border-rose-200 dark:hover:bg-rose-950 dark:hover:border-rose-800" : ""}`}
                title={`${instructionIndex}: ${opNode.instruction}`}
                onClick={handleClick}
              >
                {isBreakpoint && (
                  <span className="inline-block w-2 h-2 bg-red-300 rounded-full mr-1 flex-shrink-0" title="Breakpoint"></span>
                )}
                {showNumbers && (
                  <span className="text-muted-foreground text-xs mr-1 flex-shrink-0">
                    {instructionIndex.toString().padStart(2, '0')}
                  </span>
                )}
                <span className="truncate">{opNode.instruction}</span>
              </span>
            );
          })}
        </div>
      );
    }
    
    return <div style={style} {...ariaAttributes}></div>;
  };

  return (
    <div className="border rounded-lg bg-card flex flex-col h-full">
      <div className="p-4 pb-2 flex-shrink-0">
        <div className="flex items-center justify-between mb-4 text-foreground border-b pb-2">
          <h3 className="text-lg font-semibold">Program</h3>
          <div className="flex items-center gap-4">
            <label className="flex items-center text-sm text-muted-foreground cursor-pointer gap-2">
              <Checkbox 
                checked={useVirtualization}
                onCheckedChange={(checked) => setUseVirtualization(checked === true)}
              />
              Virtual scroll
            </label>
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
      </div>
      
      <div className="flex-1 min-h-0 px-4 pb-4" ref={codeContainerRef}>
        {useVirtualization ? (
          containerHeight > 0 && (
            <List
              key={`list-${opsPerLine}-${flatItems.length}`}
              listRef={listRef}
              defaultHeight={containerHeight}
              style={{ height: containerHeight, width: '100%' }}
              rowCount={flatItems.length}
              rowHeight={getItemSize}
              rowComponent={Row}
              rowProps={{}}
              overscanCount={100}
            />
          )
        ) : (
          <div ref={scrollContainerRef} className="overflow-auto h-full">
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
                maxOpsRowWidth={maxOpsRowWidth}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
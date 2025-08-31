import { useState } from 'react';
import type { AnnotatedKsplangTree, StateMessage } from '../types/debugger';
import { Checkbox } from './ui/checkbox';

interface CodeDisplayProps {
  program: AnnotatedKsplangTree | null;
  currentState: StateMessage | null;
}

interface RenderNodeProps {
  node: AnnotatedKsplangTree;
  depth: number;
  currentIp: number;
  instructionMap: Map<AnnotatedKsplangTree, number>;
  showNumbers: boolean;
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

function RenderNode({ node, depth, currentIp, instructionMap, showNumbers }: RenderNodeProps) {
  // Rainbow colors for block hierarchy (faint versions)
  const rainbowColors = [
    'border-red-200',
    'border-orange-200', 
    'border-yellow-200',
    'border-green-200',
    'border-blue-200',
    'border-indigo-200',
    'border-purple-200',
    'border-pink-200'
  ];
  
  const borderColor = rainbowColors[depth % rainbowColors.length];

  if (node.type === 'op') {
    const instructionIndex = instructionMap.get(node) ?? -1;
    const isCurrentInstruction = instructionIndex === currentIp;
    
    return (
      <span 
        className={`font-mono text-xs mr-1 px-1 py-0.5 rounded ${
          isCurrentInstruction 
            ? "bg-yellow-100 border border-yellow-500 font-semibold text-yellow-800" 
            : "hover:bg-gray-50 text-gray-800"
        }`}
        title={`Instruction ${instructionIndex}: ${node.instruction}`}
      >
        {showNumbers && (
          <span className="text-gray-400 text-xs mr-1">
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
        <div className="text-xs text-gray-500 mb-1 font-medium">
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
          />
        ))}
      </div>
    );
  }
  
  return null;
}

export function CodeDisplay({ program, currentState }: CodeDisplayProps) {
  const [showNumbers, setShowNumbers] = useState(false);
  
  if (!program) {
    return (
      <div className="p-4 border rounded-lg bg-gray-50">
        <p className="text-gray-500">No program loaded</p>
      </div>
    );
  }

  const currentIp = currentState?.ip ?? -1;
  
  // Build instruction map once
  const instructionMap = new Map<AnnotatedKsplangTree, number>();
  const counter = { current: 0 };
  buildInstructionMap(program, instructionMap, counter);

  return (
    <div className="border rounded-lg bg-white overflow-auto h-screen">
      <div className="p-4">
        <div className="flex items-center justify-between mb-4 text-gray-800 border-b pb-2">
          <h3 className="text-lg font-semibold">Program</h3>
          <label className="flex items-center text-sm text-gray-600 cursor-pointer gap-2">
            <Checkbox 
              checked={showNumbers}
              onCheckedChange={(checked) => setShowNumbers(checked === true)}
            />
            Show numbers
          </label>
        </div>
        <div className="space-y-1">
          <RenderNode 
            node={program} 
            depth={0} 
            currentIp={currentIp}
            instructionMap={instructionMap}
            showNumbers={showNumbers}
          />
        </div>
      </div>
    </div>
  );
}

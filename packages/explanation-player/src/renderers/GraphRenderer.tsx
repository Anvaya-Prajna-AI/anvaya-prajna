import React from 'react';
import { ReasoningStep, ExplanationIR, ReasoningGraphNode } from '../types/ir';
import { RenderContext } from './RendererRegistry';

export const GraphRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ explanation, context }) => {
  const graph = explanation.reasoningGraph;
  if (!graph || !graph.nodes || graph.nodes.length === 0) {
    return null;
  }

  const nodes = graph.nodes;
  const edges = graph.edges || [];

  // Arrange nodes in a horizontal DAG flow
  const nodeWidth = 110;
  const nodeHeight = 44;
  const startX = 30;
  const startY = 60;
  const colSpacing = 140;

  const nodePositions = new Map<string, { x: number; y: number }>();
  nodes.forEach((node, i) => {
    const col = i % 4;
    const row = Math.floor(i / 4);
    nodePositions.set(node.id, {
      x: startX + col * colSpacing,
      y: startY + row * 70,
    });
  });

  const getNodeColor = (type: ReasoningGraphNode['type']) => {
    switch (type) {
      case 'FACT':
        return { fill: '#dbeafe', stroke: '#3b82f6', text: '#1e40af' };
      case 'RULE':
        return { fill: '#fef3c7', stroke: '#f59e0b', text: '#92400e' };
      case 'RESULT':
        return { fill: '#e0e7ff', stroke: '#6366f1', text: '#3730a3' };
      case 'CONCLUSION':
        return { fill: '#dcfce7', stroke: '#10b981', text: '#065f46' };
      case 'VERIFICATION':
        return { fill: '#f1f5f9', stroke: '#64748b', text: '#334155' };
      default:
        return { fill: '#f3f4f6', stroke: '#9ca3af', text: '#1f2937' };
    }
  };

  return (
    <div className="ep-graph-container">
      <h4 className="ep-graph-title">Reasoning Dependency Graph (DAG)</h4>
      <svg
        className="ep-graph-svg"
        viewBox="0 0 580 180"
        xmlns="http://www.w3.org/2000/svg"
      >
        <defs>
          <marker
            id="dag-arrow"
            viewBox="0 0 10 10"
            refX="9"
            refY="5"
            markerWidth="6"
            markerHeight="6"
            orient="auto-start-reverse"
          >
            <path d="M 0 0 L 10 5 L 0 10 z" fill="#94a3b8" />
          </marker>
        </defs>

        {/* Edges */}
        {edges.map((edge, i) => {
          const fromPos = nodePositions.get(edge.from);
          const toPos = nodePositions.get(edge.to);
          if (!fromPos || !toPos) return null;

          const x1 = fromPos.x + nodeWidth;
          const y1 = fromPos.y + nodeHeight / 2;
          const x2 = toPos.x;
          const y2 = toPos.y + nodeHeight / 2;

          return (
            <line
              key={i}
              x1={x1}
              y1={y1}
              x2={x2}
              y2={y2}
              stroke="#94a3b8"
              strokeWidth="1.5"
              strokeDasharray={edge.relationship === 'SUPPORTS' ? '4 2' : undefined}
              markerEnd="url(#dag-arrow)"
            />
          );
        })}

        {/* Nodes */}
        {nodes.map((node) => {
          const pos = nodePositions.get(node.id);
          if (!pos) return null;
          const colors = getNodeColor(node.type);
          const isCurrentStepNode = node.id === `s${context.stepIndex + 1}`;

          return (
            <g
              key={node.id}
              className={`ep-graph-node ${isCurrentStepNode ? 'ep-graph-node-active' : ''}`}
            >
              <rect
                x={pos.x}
                y={pos.y}
                width={nodeWidth}
                height={nodeHeight}
                rx="6"
                fill={colors.fill}
                stroke={isCurrentStepNode ? '#4f46e5' : colors.stroke}
                strokeWidth={isCurrentStepNode ? '2.5' : '1.5'}
              />
              <text
                x={pos.x + 8}
                y={pos.y + 16}
                fontSize="9"
                fontWeight="700"
                fill={colors.text}
              >
                {node.type}
              </text>
              <text
                x={pos.x + 8}
                y={pos.y + 32}
                fontSize="10"
                fill="#0f172a"
                fontWeight="500"
              >
                {node.id}: {node.content.length > 14 ? node.content.slice(0, 12) + '...' : node.content}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
};
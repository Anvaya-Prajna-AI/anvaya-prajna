import React from 'react';
import { ReasoningStep, ExplanationIR, DiagramIR } from '../types/ir';
import { RenderContext } from './RendererRegistry';

export const DiagramRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ explanation }) => {
  const diagrams: DiagramIR[] = explanation.diagrams || [];

  if (diagrams.length === 0) {
    return null;
  }

  return (
    <div className="ep-diagram-panel">
      {diagrams.map((diag) => (
        <div key={diag.id} className="ep-diagram-card">
          <h4 className="ep-diagram-title">{diag.title || 'Diagram'}</h4>
          <svg
            className="ep-diagram-svg"
            viewBox="0 0 500 220"
            xmlns="http://www.w3.org/2000/svg"
          >
            {/* Background Grid */}
            <defs>
              <pattern
                id={`grid-${diag.id}`}
                width="20"
                height="20"
                patternUnits="userSpaceOnUse"
              >
                <path
                  d="M 20 0 L 0 0 0 20"
                  fill="none"
                  stroke="#e2e8f0"
                  strokeWidth="0.5"
                />
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill={`url(#grid-${diag.id})`} rx="8" />

            {/* Rendering based on diagram type */}
            {diag.type === 'bar' && renderBarChart(diag)}
            {diag.type === 'number-line' && renderNumberLine(diag)}
            {diag.type !== 'bar' && diag.type !== 'number-line' && renderGenericDiagram(diag)}
          </svg>
        </div>
      ))}
    </div>
  );
};

function renderBarChart(diag: DiagramIR) {
  const objects = diag.objects || [];
  const barWidth = 40;
  const startX = 80;
  const gap = 110;
  const baseY = 180;

  return (
    <g>
      {/* Base line */}
      <line x1="40" y1={baseY} x2="460" y2={baseY} stroke="#94a3b8" strokeWidth="2" />

      {objects.map((obj, i) => {
        const x = startX + i * gap;
        const rank = obj.properties?.rank ?? obj.properties?.height ?? 2;
        const height = rank * 35;
        const y = baseY - height;

        return (
          <g key={obj.id} className="ep-svg-bar-group">
            <rect
              x={x}
              y={y}
              width={barWidth}
              height={height}
              fill="#6366f1"
              rx="4"
              className="ep-svg-bar"
            />
            <text
              x={x + barWidth / 2}
              y={y - 8}
              textAnchor="middle"
              fill="#1e293b"
              fontSize="12"
              fontWeight="600"
            >
              {obj.label || obj.id}
            </text>
            <text
              x={x + barWidth / 2}
              y={baseY + 18}
              textAnchor="middle"
              fill="#64748b"
              fontSize="11"
            >
              Rank {rank}
            </text>
          </g>
        );
      })}

      {/* Relationships */}
      {diag.relationships?.map((rel, i) => {
        const fromIdx = objects.findIndex((o) => o.id === rel.from);
        const toIdx = objects.findIndex((o) => o.id === rel.to);
        if (fromIdx >= 0 && toIdx >= 0) {
          const x1 = startX + fromIdx * gap + barWidth / 2;
          const x2 = startX + toIdx * gap + barWidth / 2;
          const midX = (x1 + x2) / 2;
          return (
            <g key={i}>
              <path
                d={`M ${x1} 50 Q ${midX} 35 ${x2} 50`}
                fill="none"
                stroke="#10b981"
                strokeWidth="1.5"
                strokeDasharray="4 3"
              />
              <text x={midX} y="32" textAnchor="middle" fill="#059669" fontSize="11" fontWeight="bold">
                {rel.label || '>'}
              </text>
            </g>
          );
        }
        return null;
      })}
    </g>
  );
}

function renderNumberLine(diag: DiagramIR) {
  const baseY = 110;
  return (
    <g>
      {/* Number line */}
      <line x1="60" y1={baseY} x2="440" y2={baseY} stroke="#475569" strokeWidth="2.5" />
      <polygon points="440,105 455,110 440,115" fill="#475569" />

      {/* Points */}
      {diag.objects?.map((obj, i) => {
        const x = i === 0 ? 80 : 380;
        return (
          <g key={obj.id}>
            <circle cx={x} cy={baseY} r="7" fill="#3b82f6" stroke="#ffffff" strokeWidth="2" />
            <text x={x} y={baseY + 25} textAnchor="middle" fill="#1e293b" fontSize="12" fontWeight="600">
              {obj.label || obj.id}
            </text>
          </g>
        );
      })}

      {/* Relationship path */}
      {diag.relationships?.[0] && (
        <g>
          <path d="M 90 95 Q 230 65 370 95" fill="none" stroke="#f59e0b" strokeWidth="2" markerEnd="url(#arrow)" />
          <text x="230" y="60" textAnchor="middle" fill="#b45309" fontSize="12" fontWeight="600">
            {diag.relationships[0].label}
          </text>
        </g>
      )}
    </g>
  );
}

function renderGenericDiagram(diag: DiagramIR) {
  return (
    <g>
      <text x="250" y="110" textAnchor="middle" fill="#64748b" fontSize="14">
        {diag.title || 'Diagram Visualization'}
      </text>
    </g>
  );
}
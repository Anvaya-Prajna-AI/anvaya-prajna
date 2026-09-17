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
            {/* Background Grid and Markers */}
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
              <marker
                id={`arrow-${diag.id}`}
                viewBox="0 0 10 10"
                refX="7"
                refY="5"
                markerWidth="6"
                markerHeight="6"
                orient="auto-start-reverse"
              >
                <path d="M 0 1 L 10 5 L 0 9 z" fill="#6366f1" />
              </marker>
              <marker
                id={`arrow-green-${diag.id}`}
                viewBox="0 0 10 10"
                refX="7"
                refY="5"
                markerWidth="6"
                markerHeight="6"
                orient="auto-start-reverse"
              >
                <path d="M 0 1 L 10 5 L 0 9 z" fill="#10b981" />
              </marker>
              <marker
                id={`arrow-red-${diag.id}`}
                viewBox="0 0 10 10"
                refX="7"
                refY="5"
                markerWidth="6"
                markerHeight="6"
                orient="auto-start-reverse"
              >
                <path d="M 0 1 L 10 5 L 0 9 z" fill="#ef4444" />
              </marker>
            </defs>
            <rect width="100%" height="100%" fill={`url(#grid-${diag.id})`} rx="8" />

            {/* Rendering based on diagram type */}
            {diag.type === 'bar' && renderBarChart(diag)}
            {diag.type === 'number-line' && renderNumberLine(diag)}
            {diag.type === 'free-body' && renderFreeBodyDiagram(diag)}
            {diag.type === 'reaction-energy' && renderReactionEnergyProfile(diag)}
            {diag.type !== 'bar' && diag.type !== 'number-line' && diag.type !== 'free-body' && diag.type !== 'reaction-energy' && renderGenericDiagram(diag)}
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
          <path d="M 90 95 Q 230 65 370 95" fill="none" stroke="#f59e0b" strokeWidth="2" />
          <text x="230" y="60" textAnchor="middle" fill="#b45309" fontSize="12" fontWeight="600">
            {diag.relationships[0].label}
          </text>
        </g>
      )}
    </g>
  );
}

function renderFreeBodyDiagram(diag: DiagramIR) {
  const bodyObj = diag.objects?.find((o) => o.id === 'body') || diag.objects?.[0];
  const massLabel = bodyObj?.label || 'Mass m';
  const groundY = 165;
  const boxWidth = 90;
  const boxHeight = 60;
  const boxX = 250 - boxWidth / 2;
  const boxY = groundY - boxHeight;
  const centerX = 250;
  const centerY = boxY + boxHeight / 2;

  const fnRel = diag.relationships?.find((r) => r.to === 'Fn' || r.label?.includes('Normal'));
  const fgRel = diag.relationships?.find((r) => r.to === 'Fg' || r.label?.includes('Gravity') || r.label?.includes('W ='));
  const fappRel = diag.relationships?.find((r) => r.to === 'Fapp' || r.label?.includes('Applied') || r.label?.includes('F_app'));
  const fkRel = diag.relationships?.find((r) => r.to === 'fk' || r.label?.includes('Friction'));

  return (
    <g>
      {/* Ground surface */}
      <line x1="60" y1={groundY} x2="440" y2={groundY} stroke="#64748b" strokeWidth="2.5" />
      {/* Floor hatch marks */}
      {[80, 120, 160, 200, 240, 280, 320, 360, 400].map((hx) => (
        <line key={hx} x1={hx} y1={groundY} x2={hx - 12} y2={groundY + 12} stroke="#94a3b8" strokeWidth="1.5" />
      ))}
      <text x="70" y={groundY + 22} fill="#64748b" fontSize="11" fontWeight="500">
        Frictionless Surface
      </text>

      {/* Mass Body */}
      <rect
        x={boxX}
        y={boxY}
        width={boxWidth}
        height={boxHeight}
        fill="#e0f2fe"
        stroke="#0284c7"
        strokeWidth="2.5"
        rx="6"
      />
      {/* Center of Mass Dot */}
      <circle cx={centerX} cy={centerY} r="4" fill="#0369a1" />
      <text
        x={centerX}
        y={centerY + 4}
        textAnchor="middle"
        fill="#0c4a6e"
        fontSize="11"
        fontWeight="700"
      >
        {massLabel}
      </text>

      {/* Normal Force Vector (UP) */}
      <line
        x1={centerX}
        y1={boxY}
        x2={centerX}
        y2={30}
        stroke="#10b981"
        strokeWidth="2.5"
        markerEnd={`url(#arrow-green-${diag.id})`}
      />
      <text x={centerX + 8} y="40" fill="#047857" fontSize="11" fontWeight="700">
        {fnRel?.label || 'F_N (Normal)'}
      </text>

      {/* Gravity Vector (DOWN) */}
      <line
        x1={centerX}
        y1={groundY}
        x2={centerX}
        y2={210}
        stroke="#ef4444"
        strokeWidth="2.5"
        markerEnd={`url(#arrow-red-${diag.id})`}
      />
      <text x={centerX + 8} y="205" fill="#b91c1c" fontSize="11" fontWeight="700">
        {fgRel?.label || 'W = mg (Weight)'}
      </text>

      {/* Applied Force Vector (RIGHT) */}
      <line
        x1={boxX + boxWidth}
        y1={centerY}
        x2={380}
        y2={centerY}
        stroke="#6366f1"
        strokeWidth="2.5"
        markerEnd={`url(#arrow-${diag.id})`}
      />
      <text x="385" y={centerY - 6} fill="#4338ca" fontSize="11" fontWeight="700">
        {fappRel?.label || 'F_app'}
      </text>

      {/* Friction Vector (LEFT) if present */}
      {fkRel && (
        <g>
          <line
            x1={boxX}
            y1={centerY}
            x2={120}
            y2={centerY}
            stroke="#f59e0b"
            strokeWidth="2.5"
            markerEnd={`url(#arrow-${diag.id})`}
          />
          <text x="110" y={centerY - 6} textAnchor="end" fill="#b45309" fontSize="11" fontWeight="700">
            {fkRel.label || 'f_k'}
          </text>
        </g>
      )}
    </g>
  );
}

function renderReactionEnergyProfile(diag: DiagramIR) {
  const eaRel = diag.relationships?.find((r) => r.type === 'ACTIVATION_ENERGY' || r.label?.includes('E_a'));
  const dhRel = diag.relationships?.find((r) => r.type === 'ENTHALPY_CHANGE' || r.label?.includes('ΔH') || r.label?.includes('deltaH'));

  return (
    <g>
      {/* Axes */}
      <line x1="50" y1="200" x2="50" y2="25" stroke="#475569" strokeWidth="2" markerEnd={`url(#arrow-${diag.id})`} />
      <line x1="50" y1="200" x2="470" y2="200" stroke="#475569" strokeWidth="2" markerEnd={`url(#arrow-${diag.id})`} />
      <text x="55" y="32" fill="#334155" fontSize="10" fontWeight="700">
        Potential Energy (kJ/mol)
      </text>
      <text x="465" y="194" textAnchor="end" fill="#334155" fontSize="10" fontWeight="700">
        Reaction Coordinate →
      </text>

      {/* Reaction Energy Curve */}
      <path
        d="M 65 135 L 130 135 C 180 135, 200 45, 245 45 C 290 45, 310 170, 360 170 L 435 170"
        fill="none"
        stroke="#8b5cf6"
        strokeWidth="3.5"
        strokeLinecap="round"
      />

      {/* Reactants Plateau & Label */}
      <line x1="65" y1="135" x2="130" y2="135" stroke="#3b82f6" strokeWidth="3" />
      <text x="75" y="122" fill="#1d4ed8" fontSize="11" fontWeight="700">
        Reactants (2H₂ + O₂)
      </text>

      {/* Transition State Peak */}
      <circle cx="245" cy="45" r="5" fill="#f43f5e" stroke="#fff" strokeWidth="2" />
      <text x="245" y="32" textAnchor="middle" fill="#be123c" fontSize="11" fontWeight="700">
        Transition State (Activated Complex)
      </text>

      {/* Products Plateau & Label */}
      <line x1="360" y1="170" x2="435" y2="170" stroke="#10b981" strokeWidth="3" />
      <text x="375" y="160" fill="#047857" fontSize="11" fontWeight="700">
        Products (2H₂O)
      </text>

      {/* Activation Energy (Ea) indicator line */}
      <line x1="130" y1="135" x2="245" y2="135" stroke="#cbd5e1" strokeWidth="1.5" strokeDasharray="3 3" />
      <line x1="245" y1="135" x2="245" y2="52" stroke="#f59e0b" strokeWidth="2" markerEnd={`url(#arrow-${diag.id})`} />
      <text x="252" y="95" fill="#b45309" fontSize="11" fontWeight="700">
        {eaRel?.label || 'E_a (+90 kJ/mol)'}
      </text>

      {/* Enthalpy Change (ΔH) indicator line */}
      <line x1="360" y1="135" x2="435" y2="135" stroke="#cbd5e1" strokeWidth="1.5" strokeDasharray="3 3" />
      <line x1="420" y1="135" x2="420" y2="164" stroke="#ef4444" strokeWidth="2" markerEnd={`url(#arrow-red-${diag.id})`} />
      <text x="426" y="152" fill="#b91c1c" fontSize="11" fontWeight="700">
        {dhRel?.label || 'ΔH = -40 kJ/mol (Exothermic)'}
      </text>
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

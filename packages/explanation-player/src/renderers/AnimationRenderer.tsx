import React from 'react';
import { ReasoningStep, ExplanationIR } from '../types/ir';
import { RenderContext } from './RendererRegistry';

export const AnimationRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ step }) => {
  const op = step.operation;
  if (!op && step.type !== 'TRANSFORM' && step.type !== 'CALCULATE') {
    return null;
  }

  return (
    <div className="ep-animation-stage">
      <div className="ep-anim-header">
        <span className="ep-anim-tag">Transformation Action</span>
        {op?.name && <span className="ep-op-badge">{op.name} {op.value ?? ''}</span>}
      </div>

      <div className="ep-anim-flow">
        {step.before && (
          <div className="ep-anim-box ep-anim-before">
            <span className="ep-anim-box-label">Before </span>
            <code>{step.before}</code>
          </div>
        )}

        <div className="ep-anim-arrow">
          <span>➔</span>
          {op && <small className="ep-op-label">{op.name} ({op.target || 'both sides'})</small>}
        </div>

        {step.after && (
          <div className="ep-anim-box ep-anim-after ep-anim-pulse">
            <span className="ep-anim-box-label">After </span>
            <code>{step.after}</code>
          </div>
        )}
      </div>
    </div>
  );
};

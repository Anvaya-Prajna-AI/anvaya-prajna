import React from 'react';
import { ReasoningStep, ExplanationIR } from '../types/ir';
import { RenderContext } from './RendererRegistry';

export const TextRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ step, context }) => {
  return (
    <div className={`ep-step-text ${context.isCurrent ? 'ep-step-active' : ''}`}>
      <div className="ep-step-badge-row">
        <span className={`ep-badge ep-badge-${step.type.toLowerCase()}`}>
          {step.type.replace('_', ' ')}
        </span>
        <span className="ep-step-sequence">Step {step.sequence}</span>
      </div>

      {step.before && step.before !== step.after && (
        <div className="ep-step-before">
          <span className="ep-sublabel">From:</span>
          <code>{step.before}</code>
        </div>
      )}

      {step.after && (
        <div className="ep-step-after">
          <span className="ep-sublabel">{step.before ? 'Result:' : 'Content:'}</span>
          <strong>{step.after}</strong>
        </div>
      )}

      {step.justification && (
        <p className="ep-step-justification">
          <span className="ep-info-icon">💡</span> {step.justification.text}
        </p>
      )}
    </div>
  );
};
import React from 'react';
import { ReasoningStep, ExplanationIR } from '../types/ir';
import { RendererRegistry, RenderContext } from '../renderers/RendererRegistry';

export const StepRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ step, explanation, context }) => {
  const representations = step.representations && step.representations.length > 0
    ? step.representations
    : ['TEXT'];

  return (
    <div className="ep-step-container">
      {representations.map((repType, idx) => {
        const Component = RendererRegistry.get(repType);
        if (!Component) {
          // Fallback to text if renderer not explicitly registered
          const TextComp = RendererRegistry.get('TEXT');
          return TextComp ? (
            <TextComp key={idx} step={step} explanation={explanation} context={context} />
          ) : null;
        }

        return (
          <div key={`${repType}-${idx}`} className={`ep-representation ep-rep-${repType.toLowerCase()}`}>
            <Component step={step} explanation={explanation} context={context} />
          </div>
        );
      })}
    </div>
  );
};
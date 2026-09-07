import React, { useMemo } from 'react';
import katex from 'katex';
import { ReasoningStep, ExplanationIR } from '../types/ir';
import { RenderContext } from './RendererRegistry';

export const MathRenderer: React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}> = ({ step }) => {
  const latexStr = step.math?.latex || step.after || '';

  const renderedHtml = useMemo(() => {
    if (!latexStr) return '';
    try {
      return katex.renderToString(latexStr, {
        displayMode: true,
        throwOnError: false,
      });
    } catch {
      return '';
    }
  }, [latexStr]);

  return (
    <div className="ep-math-container">
      {renderedHtml ? (
        <div
          className="ep-math-render"
          dangerouslySetInnerHTML={{ __html: renderedHtml }}
        />
      ) : (
        <div className="ep-math-fallback">
          <code>{latexStr}</code>
        </div>
      )}
    </div>
  );
};
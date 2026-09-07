import React, { useState } from 'react';
import { ReasoningStep } from '../types/ir';

export const WhyPanel: React.FC<{
  currentStep?: ReasoningStep;
}> = ({ currentStep }) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const justification = currentStep?.justification;

  if (!justification) {
    return null;
  }

  return (
    <div className="ep-panel ep-panel-why">
      <button
        className="ep-why-toggle"
        onClick={() => setIsOpen(!isOpen)}
        aria-expanded={isOpen}
      >
        <span>❓ Why this step?</span>
        <span>{isOpen ? '▲ Hide' : '▼ Explain Why'}</span>
      </button>

      {isOpen && (
        <div className="ep-why-content">
          <p className="ep-why-text">{justification.text}</p>
          {justification.depth !== undefined && (
            <span className="ep-why-depth">Explanation Depth: {justification.depth}</span>
          )}
        </div>
      )}
    </div>
  );
};
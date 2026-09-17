import React from 'react';
import { ExplanationIR } from '../types/ir';

export const ExplanationHeader: React.FC<{
  explanation: ExplanationIR;
}> = ({ explanation }) => {
  const meta = explanation.metadata;
  const status = explanation.status;
  const domain = explanation.problem.domain || 'GENERAL';

  return (
    <header className="ep-header">
      <div className="ep-header-main">
        <div className="ep-brand">
          <span className="ep-logo">अन्वय-प्रज्ञा</span>
          <span className="ep-version-pill">v{explanation.schemaVersion}</span>
        </div>
        <div className="ep-header-badges">
          <span className="ep-badge ep-badge-domain">{domain}</span>
          {meta?.studentLevel && (
            <span className="ep-badge ep-badge-level">{meta.studentLevel}</span>
          )}
          <span className={`ep-badge ep-badge-status ep-badge-${status.toLowerCase()}`}>
            {status}
          </span>
          {meta?.qualityScore !== undefined && (
            <span className="ep-badge ep-badge-score">
              EQS: {Math.round(meta.qualityScore * 100)}%
            </span>
          )}
        </div>
      </div>
      <div className="ep-header-sub">
        <span className="ep-meta-id">QID: {explanation.questionId}</span>
        <span className="ep-meta-id">ExpID: {explanation.explanationId}</span>
      </div>
    </header>
  );
};
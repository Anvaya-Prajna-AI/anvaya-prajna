import React from 'react';
import { VerificationResult } from '../types/ir';

export const VerificationPanel: React.FC<{
  verification?: VerificationResult;
}> = ({ verification }) => {
  if (!verification) {
    return null;
  }

  return (
    <div className={`ep-panel ep-panel-verification ${verification.passed ? 'ep-verify-passed' : 'ep-verify-failed'}`}>
      <div className="ep-verify-badge-row">
        <span className="ep-verify-icon">{verification.passed ? '✅' : '❌'}</span>
        <h4 className="ep-verify-title">
          {verification.passed ? 'Mathematically Verified' : 'Verification Inconsistency'}
        </h4>
        <span className="ep-verify-type">{verification.type || 'SUBSTITUTION'}</span>
      </div>
      <div className="ep-verify-expr">
        <code>{verification.expression}</code>
      </div>
      {verification.details && (
        <p className="ep-verify-details">{verification.details}</p>
      )}
    </div>
  );
};
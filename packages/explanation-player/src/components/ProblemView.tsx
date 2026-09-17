import React from 'react';
import { Problem } from '../types/ir';

export const ProblemView: React.FC<{
  problem: Problem;
}> = ({ problem }) => {
  return (
    <section className="ep-problem-card">
      <div className="ep-section-tag">Question Statement</div>
      <h2 className="ep-problem-statement">{problem.statement}</h2>
      {problem.authoritativeAnswer && (
        <div className="ep-authoritative-box">
          <span className="ep-sublabel">Verified Final Answer:</span>
          <strong>{problem.authoritativeAnswer}</strong>
        </div>
      )}
    </section>
  );
};
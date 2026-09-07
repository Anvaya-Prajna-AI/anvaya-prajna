import React from 'react';
import { Misconception } from '../types/ir';

export const MisconceptionPanel: React.FC<{
  misconceptions?: Misconception[];
}> = ({ misconceptions = [] }) => {
  if (misconceptions.length === 0) {
    return null;
  }

  return (
    <div className="ep-panel ep-panel-misconception">
      <h4 className="ep-panel-title">
        <span>⚠️ Common Misconceptions & Pitfalls</span>
      </h4>
      <div className="ep-misconception-list">
        {misconceptions.map((item) => (
          <div key={item.id} className="ep-misconception-card">
            <div className="ep-misc-row ep-misc-incorrect">
              <span className="ep-misc-label">Common Mistake:</span>
              <p>{item.incorrectReasoning}</p>
            </div>
            <div className="ep-misc-row ep-misc-why">
              <span className="ep-misc-label">Why It's Wrong:</span>
              <p>{item.whyWrong}</p>
            </div>
            {item.correctedReasoning && (
              <div className="ep-misc-row ep-misc-correct">
                <span className="ep-misc-label">Correct Thinking:</span>
                <p>{item.correctedReasoning}</p>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
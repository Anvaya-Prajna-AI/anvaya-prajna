import React, { useState } from 'react';
import { Hint } from '../types/ir';

export const HintPanel: React.FC<{
  hints?: Hint[];
}> = ({ hints = [] }) => {
  const [revealedLevel, setRevealedLevel] = useState<number>(0);

  if (hints.length === 0) {
    return null;
  }

  const sortedHints = [...hints].sort((a, b) => (a.level || 0) - (b.level || 0));

  return (
    <div className="ep-panel ep-panel-hints">
      <div className="ep-panel-header">
        <h4 className="ep-panel-title">
          <span>💡 Progressive Hints</span>
          <span className="ep-hint-counter">
            {revealedLevel} of {sortedHints.length} unlocked
          </span>
        </h4>
        {revealedLevel < sortedHints.length && (
          <button
            className="ep-btn-pill"
            onClick={() => setRevealedLevel((prev) => prev + 1)}
          >
            + Reveal Hint {revealedLevel + 1}
          </button>
        )}
      </div>

      <div className="ep-hints-list">
        {sortedHints.slice(0, revealedLevel).map((hint) => (
          <div key={hint.id} className="ep-hint-item ep-hint-revealed">
            <span className="ep-hint-level">Level {hint.level}</span>
            <p className="ep-hint-text">{hint.text}</p>
          </div>
        ))}

        {revealedLevel === 0 && (
          <p className="ep-hint-placeholder">
            Stuck on this question? Click "Reveal Hint" to get a gentle conceptual nudge without spoiling the full solution.
          </p>
        )}
      </div>
    </div>
  );
};
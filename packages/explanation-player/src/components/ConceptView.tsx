import React from 'react';

export const ConceptView: React.FC<{
  concepts?: string[];
  facts?: string[];
}> = ({ concepts = [], facts = [] }) => {
  if (concepts.length === 0 && facts.length === 0) {
    return null;
  }

  return (
    <div className="ep-concept-row">
      {concepts.length > 0 && (
        <div className="ep-concept-group">
          <span className="ep-concept-label">Concepts:</span>
          {concepts.map((concept, i) => (
            <span key={i} className="ep-concept-chip">
              #{concept}
            </span>
          ))}
        </div>
      )}
      {facts.length > 0 && (
        <div className="ep-facts-group">
          <span className="ep-concept-label">Given Facts:</span>
          {facts.map((fact, i) => (
            <span key={i} className="ep-fact-chip">
              ✓ {fact}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};
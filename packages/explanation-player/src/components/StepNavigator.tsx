import React from 'react';
import { ReasoningStep } from '../types/ir';

export const StepNavigator: React.FC<{
  steps: ReasoningStep[];
  currentIndex: number;
  onSelectIndex: (index: number) => void;
  isPlaying: boolean;
  onTogglePlay: () => void;
  speed: number;
  onChangeSpeed: (speed: number) => void;
}> = ({
  steps,
  currentIndex,
  onSelectIndex,
  isPlaying,
  onTogglePlay,
  speed,
  onChangeSpeed,
}) => {
  const total = steps.length;
  const canPrev = currentIndex > 0;
  const canNext = currentIndex < total - 1;

  return (
    <div className="ep-navigator">
      <div className="ep-nav-controls">
        <button
          className="ep-btn ep-btn-secondary"
          onClick={() => onSelectIndex(currentIndex - 1)}
          disabled={!canPrev}
          title="Previous Step"
        >
          ◀ Prev
        </button>

        <button
          className={`ep-btn ${isPlaying ? 'ep-btn-playing' : 'ep-btn-primary'}`}
          onClick={onTogglePlay}
          title={isPlaying ? 'Pause Auto-play' : 'Play Step-by-Step'}
        >
          {isPlaying ? '⏸ Pause' : '▶ Play'}
        </button>

        <button
          className="ep-btn ep-btn-secondary"
          onClick={() => onSelectIndex(currentIndex + 1)}
          disabled={!canNext}
          title="Next Step"
        >
          Next ▶
        </button>

        <button
          className="ep-btn ep-btn-ghost"
          onClick={() => onSelectIndex(0)}
          title="Restart from beginning"
        >
          ↺ Reset
        </button>

        {/* Speed Selector */}
        <div className="ep-speed-group">
          {[0.5, 1, 1.5, 2].map((s) => (
            <button
              key={s}
              className={`ep-speed-btn ${speed === s ? 'ep-speed-active' : ''}`}
              onClick={() => onChangeSpeed(s)}
            >
              {s}×
            </button>
          ))}
        </div>
      </div>

      {/* Step Dots Indicator */}
      <div className="ep-step-dots">
        {steps.map((step, idx) => (
          <button
            key={step.id || idx}
            className={`ep-step-dot ${idx === currentIndex ? 'ep-dot-active' : ''} ${
              idx < currentIndex ? 'ep-dot-completed' : ''
            }`}
            onClick={() => onSelectIndex(idx)}
            title={`Step ${idx + 1}: ${step.type}`}
          >
            {idx + 1}
          </button>
        ))}
      </div>
    </div>
  );
};
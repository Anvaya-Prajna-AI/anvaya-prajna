import React, { useState } from 'react';
import { ExplanationIR } from '../types/ir';
import { ExplanationHeader } from './ExplanationHeader';
import { ProblemView } from './ProblemView';
import { ConceptView } from './ConceptView';
import { StepNavigator } from './StepNavigator';
import { StepRenderer } from './StepRenderer';
import { HintPanel } from './HintPanel';
import { WhyPanel } from './WhyPanel';
import { VerificationPanel } from './VerificationPanel';
import { MisconceptionPanel } from './MisconceptionPanel';
import { useAnimationPlayer } from '../hooks/useAnimationPlayer';
import '../renderers'; // Auto-register built-in renderers

export interface ExplanationPlayerProps {
  explanation: ExplanationIR;
  initialStepIndex?: number;
  onStepChange?: (stepIndex: number) => void;
  className?: string;
}

export const ExplanationPlayer: React.FC<ExplanationPlayerProps> = ({
  explanation,
  initialStepIndex = 0,
  onStepChange,
  className = '',
}) => {
  const steps = explanation.steps || [];
  const [activeStepIndex, setActiveStepIndex] = useState<number>(initialStepIndex);

  const {
    goToStep,
    isPlaying,
    togglePlay,
    speed,
    setSpeed,
    reducedMotion,
  } = useAnimationPlayer(steps.length, (idx) => {
    setActiveStepIndex(idx);
    onStepChange?.(idx);
  });

  const handleSelectStep = (idx: number) => {
    goToStep(idx);
    setActiveStepIndex(idx);
    onStepChange?.(idx);
  };

  const currentStep = steps[activeStepIndex] || steps[0];

  return (
    <article className={`anvaya-explanation-player ${className}`}>
      <ExplanationHeader explanation={explanation} />

      <main className="ep-main-body">
        <ProblemView problem={explanation.problem} />

        <ConceptView
          concepts={explanation.concepts}
          facts={explanation.facts}
        />

        {steps.length > 0 && (
          <section className="ep-interactive-stage">
            <StepNavigator
              steps={steps}
              currentIndex={activeStepIndex}
              onSelectIndex={handleSelectStep}
              isPlaying={isPlaying}
              onTogglePlay={togglePlay}
              speed={speed}
              onChangeSpeed={setSpeed}
            />

            <div className="ep-active-step-view">
              <StepRenderer
                step={currentStep}
                explanation={explanation}
                context={{
                  stepIndex: activeStepIndex,
                  totalSteps: steps.length,
                  isCurrent: true,
                  reducedMotion,
                }}
              />
            </div>

            <WhyPanel currentStep={currentStep} />
          </section>
        )}

        <footer className="ep-side-panels">
          <HintPanel hints={explanation.hints} />
          <VerificationPanel verification={explanation.verification} />
          <MisconceptionPanel misconceptions={explanation.misconceptions} />
        </footer>
      </main>
    </article>
  );
};
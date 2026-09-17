import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { StepNavigator } from '../StepNavigator';
import { ReasoningStep } from '../../types/ir';

describe('StepNavigator Component', () => {
  const mockSteps: ReasoningStep[] = [
    {
      id: 'step-1',
      sequence: 1,
      type: 'GIVEN',
      after: '3x + 5 = 20',
    },
    {
      id: 'step-2',
      sequence: 2,
      type: 'TRANSFORM',
      before: '3x + 5 = 20',
      after: '3x = 15',
    },
  ];

  it('renders step navigation controls and steps', () => {
    const onSelectIndex = vi.fn();
    const onTogglePlay = vi.fn();
    const onChangeSpeed = vi.fn();

    render(
      <StepNavigator
        steps={mockSteps}
        currentIndex={0}
        onSelectIndex={onSelectIndex}
        isPlaying={false}
        onTogglePlay={onTogglePlay}
        speed={1}
        onChangeSpeed={onChangeSpeed}
      />
    );

    expect(screen.getByText(/Play/i)).toBeInTheDocument();
    expect(screen.getByText(/Next/i)).toBeInTheDocument();
    
    // Click step 2 dot
    const step2Dot = screen.getByTitle('Step 2: TRANSFORM');
    fireEvent.click(step2Dot);
    expect(onSelectIndex).toHaveBeenCalledWith(1);
  });
});

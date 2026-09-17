import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { HintPanel } from '../HintPanel';
import { Hint } from '../../types/ir';

describe('HintPanel Component', () => {
  const mockHints: Hint[] = [
    { id: 'h1', level: 1, text: 'Remember the order of operations (PEMDAS).' },
    { id: 'h2', level: 2, text: 'Subtract 5 from both sides of the equation.' },
  ];

  it('renders nothing when hints array is empty', () => {
    const { container } = render(<HintPanel hints={[]} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders hint placeholder and reveal button initially', () => {
    render(<HintPanel hints={mockHints} />);
    expect(screen.getByText(/Progressive Hints/i)).toBeInTheDocument();
    expect(screen.getByText(/0 of 2 unlocked/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /\+ Reveal Hint 1/i })).toBeInTheDocument();
  });

  it('reveals hints progressively upon button click', () => {
    render(<HintPanel hints={mockHints} />);
    const revealBtn = screen.getByRole('button', { name: /\+ Reveal Hint 1/i });

    fireEvent.click(revealBtn);
    expect(screen.getByText('Remember the order of operations (PEMDAS).')).toBeInTheDocument();
    expect(screen.getByText(/1 of 2 unlocked/i)).toBeInTheDocument();

    const revealBtn2 = screen.getByRole('button', { name: /\+ Reveal Hint 2/i });
    fireEvent.click(revealBtn2);
    expect(screen.getByText('Subtract 5 from both sides of the equation.')).toBeInTheDocument();
    expect(screen.getByText(/2 of 2 unlocked/i)).toBeInTheDocument();
  });
});

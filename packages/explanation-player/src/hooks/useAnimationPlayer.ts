import { useState, useEffect, useRef } from 'react';

export function useAnimationPlayer(
  totalSteps: number,
  onStepChange: (index: number) => void
) {
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [speed, setSpeed] = useState<number>(1);
  const [reducedMotion, setReducedMotion] = useState<boolean>(false);

  const timerRef = useRef<number | null>(null);

  const goToStep = (index: number) => {
    const clamped = Math.max(0, Math.min(index, totalSteps - 1));
    setCurrentIndex(clamped);
    onStepChange(clamped);
  };

  const togglePlay = () => {
    if (isPlaying) {
      setIsPlaying(false);
    } else {
      if (currentIndex >= totalSteps - 1) {
        goToStep(0);
      }
      setIsPlaying(true);
    }
  };

  useEffect(() => {
    if (!isPlaying) {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      return;
    }

    const intervalMs = Math.round(2000 / speed);
    timerRef.current = window.setInterval(() => {
      setCurrentIndex((prev) => {
        if (prev >= totalSteps - 1) {
          setIsPlaying(false);
          return prev;
        }
        const next = prev + 1;
        onStepChange(next);
        return next;
      });
    }, intervalMs);

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };
  }, [isPlaying, totalSteps, speed, onStepChange]);

  return {
    currentIndex,
    goToStep,
    isPlaying,
    togglePlay,
    speed,
    setSpeed,
    reducedMotion,
    setReducedMotion,
  };
}
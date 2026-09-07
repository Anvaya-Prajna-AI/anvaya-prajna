import React from 'react';
import { ReasoningStep, RepresentationType, ExplanationIR } from '../types/ir';

export interface RenderContext {
  stepIndex: number;
  totalSteps: number;
  isCurrent: boolean;
  highlighted?: boolean;
  reducedMotion?: boolean;
}

export type StepRendererComponent = React.FC<{
  step: ReasoningStep;
  explanation: ExplanationIR;
  context: RenderContext;
}>;

class Registry {
  private renderers: Map<string, StepRendererComponent> = new Map();

  register(type: RepresentationType | string, renderer: StepRendererComponent): void {
    this.renderers.set(type.toUpperCase(), renderer);
  }

  get(type: RepresentationType | string): StepRendererComponent | undefined {
    return this.renderers.get(type.toUpperCase());
  }

  has(type: RepresentationType | string): boolean {
    return this.renderers.has(type.toUpperCase());
  }
}

export const RendererRegistry = new Registry();
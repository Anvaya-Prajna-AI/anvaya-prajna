import { RendererRegistry } from './RendererRegistry';
import { TextRenderer } from './TextRenderer';
import { MathRenderer } from './MathRenderer';
import { DiagramRenderer } from './DiagramRenderer';
import { GraphRenderer } from './GraphRenderer';
import { AnimationRenderer } from './AnimationRenderer';

// Auto-register built-in renderers per Design §24
RendererRegistry.register('TEXT', TextRenderer);
RendererRegistry.register('MATH', MathRenderer);
RendererRegistry.register('DIAGRAM', DiagramRenderer);
RendererRegistry.register('SVG', DiagramRenderer);
RendererRegistry.register('REASONING_GRAPH', GraphRenderer);
RendererRegistry.register('ANIMATION', AnimationRenderer);
RendererRegistry.register('HIGHLIGHT', AnimationRenderer);

export {
  RendererRegistry,
  TextRenderer,
  MathRenderer,
  DiagramRenderer,
  GraphRenderer,
  AnimationRenderer,
};
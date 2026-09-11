# Physics & Chemistry Explanation Engine Specification & Design

## 1. Executive Summary

This document specifies the architecture, data models, domain plugins, and multi-modal visualization strategies for expanding **Anvaya-Prajna-AI** into the physical sciences:
- **Physics**: Classical Mechanics, Kinematics, Newton's Laws of Motion, Work-Energy Theorem, and Free-Body Vector Diagrams (FBD).
- **Chemistry**: Chemical Equation Balancing, Stoichiometry, Molar Conversions, Limiting Reagents, and Reaction Energy Profiles ($E_a$, $\Delta H$).

The design strictly maintains the core philosophy of Anvaya-Prajna:
> **One verified reasoning model → canonical Explanation IR → multi-modal, synchronized educational representations.**

---

## 2. Architectural Blueprint

```
+-----------------------------------------------------------------------------------------+
|                                    Assessment Question                                   |
|   Physics: "A 10 kg block is pulled with 50 N force on frictionless surface. Find a."    |
|   Chemistry: "Balance H2 + O2 -> H2O and find grams of H2O from 4g of H2."              |
+-----------------------------------------------------------------------------------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                  Explanation Domain Plugins (libraries:explain-core)                     |
|                                                                                         |
|   +---------------------------------------+   +-------------------------------------+   |
|   |         PhysicsMechanicsDomain        |   |      ChemistryStoichiometryDomain   |   |
|   | - Kinematics ($v = u + at$)           |   | - Atom Conservation Matrix          |   |
|   | - Dynamics ($\Sigma F = ma$)          |   | - Stoichiometric Ratio Evaluator    |   |
|   | - Work & Energy ($W = Fd$)            |   | - Molar Mass & Yield Computation    |   |
|   | - Dimensional / Unit Verifier         |   | - Exothermic/Endothermic Classifier |   |
|   +---------------------------------------+   +-------------------------------------+   |
+-----------------------------------------------------------------------------------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                           Explanation IR (Canonical Contract)                           |
|   - ReasoningGraphIR: Law Dependencies & Inferences                                     |
|   - ReasoningStep[]: GIVEN -> APPLY_RULE -> SUBSTITUTE -> CALCULATE -> VERIFY          |
|   - Diagrams: DiagramIR ("free-body", "reaction-energy", "chemical-equation")           |
|   - Misconceptions: Domain-specific cognitive pitfall catalog                          |
+-----------------------------------------------------------------------------------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                       Explain Player UI (packages:explanation-player)                   |
|   - KaTeX Math typesetting with physical units & chemical formulas                      |
|   - SVG Free-Body Diagram Renderer (vector arrows, center of mass, normal/gravity)       |
|   - SVG Reaction Energy Profile Renderer (reactants, Ea hump, products, delta H)        |
+-----------------------------------------------------------------------------------------+
```

---

## 3. DiagramIR Semantic Specification

To faithfully communicate physical forces and chemical energy pathways, [`schemas/diagram-ir.schema.json`](file:///C:/Users/sheel/IdeaProjects/anvaya-prajna/schemas/diagram-ir.schema.json) is extended with specialized declarative diagram types:

### 3.1 Free-Body Diagram (`free-body`)
Declarative representation of forces acting upon an isolated body.
- **Objects**:
  - `body`: Central mass node (`position: [x, y]`, `mass`, `label`).
  - `surface`: Optional ground plane or inclined plane angle $\theta$.
- **Relationships (Forces / Vectors)**:
  - `type: "VECTOR"`
  - `from`: `"body"`
  - `to`: Vector label/target (e.g., `"Fn"`, `"Fg"`, `"Fapp"`, `"fk"`)
  - `properties`: `{ "magnitude": "50 N", "direction": "RIGHT", "angle": 0, "color": "#10b981" }`

### 3.2 Reaction Energy Profile (`reaction-energy`)
Declarative potential energy curve along the reaction coordinate.
- **Objects**:
  - `reactants`: Energy level of reactants (e.g., `energy: 80 kJ/mol`, `label: "2H2 + O2"`).
  - `transition_state`: Peak activation energy (e.g., `energy: 180 kJ/mol`, `label: "Activated Complex"`).
  - `products`: Energy level of products (e.g., `energy: 30 kJ/mol`, `label: "2H2O"`).
- **Relationships**:
  - `Ea` (Activation energy: Reactants $\rightarrow$ Transition state).
  - `deltaH` (Enthalpy change: Products - Reactants, marked exothermic if $< 0$).

### 3.3 Chemical Equation Tiles (`chemical-equation`)
Declarative breakdown of reactant and product molecules showing atom conservation:
- **Objects**:
  - Reactant species and product species with stoichiometric coefficients and atom counts.

---

## 4. Physics Domain Design (`PhysicsMechanicsDomain`)

### 4.1 Supported Question Categories
1. **Newton's Second Law**: $\Sigma F = ma$, solving for $F$, $m$, or $a$.
2. **Weight & Gravitational Acceleration**: $W = mg$, where $g = 9.8\text{ m/s}^2$ (or $10\text{ m/s}^2$ in simplified curriculum problems).
3. **Friction**: Normal force $F_N = mg$ and kinetic friction $f_k = \mu_k F_N$.
4. **1D Kinematics**:
   - $v = u + at$
   - $s = ut + \frac{1}{2}at^2$
   - $v^2 = u^2 + 2as$
5. **Work & Kinetic Energy**:
   - $W = F \cdot d$
   - $KE = \frac{1}{2}mv^2$

### 4.2 Step Execution Pipeline
1. `StepType.GIVEN`: Identify given scalar quantities with units ($m = 10\text{ kg}$, $F = 50\text{ N}$).
2. `StepType.APPLY_RULE`: Cite the governing law (e.g., Newton's Second Law: $a = \frac{F_{net}}{m}$).
3. `StepType.SUBSTITUTE`: Substitute scalar values into the equation ($a = \frac{50}{10}$).
4. `StepType.CALCULATE`: Evaluate expression via safe AST evaluator ($a = 5\text{ m/s}^2$).
5. `StepType.VERIFY`: Dimensional consistency check ($[N] / [kg] = [kg \cdot m/s^2] / [kg] = [m/s^2]$).

### 4.3 Pedagogical Misconceptions
- **FBD Direction**: Thinking friction acts in the direction of motion rather than opposing relative slip.
- **Normal Force Myth**: Believing normal force is an action-reaction pair with gravity ($F_N$ and $W$ act on the *same* body).
- **Inertia vs Force**: Believing a continuous forward force is required to sustain constant velocity.

---

## 5. Chemistry Domain Design (`ChemistryStoichiometryDomain`)

### 5.1 Supported Question Categories
1. **Chemical Equation Balancing**:
   - Methane combustion: $\text{CH}_4 + 2\text{O}_2 \rightarrow \text{CO}_2 + 2\text{H}_2\text{O}$
   - Hydrogen-Oxygen synthesis: $2\text{H}_2 + \text{O}_2 \rightarrow 2\text{H}_2\text{O}$
   - Haber-Bosch ammonia synthesis: $\text{N}_2 + 3\text{H}_2 \rightarrow 2\text{NH}_3$
2. **Stoichiometric Mole & Mass Conversions**:
   - Grams to Moles: $n = \frac{m}{M}$
   - Molar Mass lookup ($H = 1.008$, $O = 16.0$, $C = 12.011$, $N = 14.007$)
   - Theoretical product yield calculation.
3. **Reaction Energetics ($\Delta H$ & $E_a$)**:
   - Classifying exothermic ($\Delta H < 0$) vs endothermic ($\Delta H > 0$).

### 5.2 Step Execution Pipeline
1. `StepType.GIVEN`: State the initial unbalanced reaction or given masses.
2. `StepType.APPLY_RULE`: Conservation of Mass (Lavoisier's Principle: atom counts must balance).
3. `StepType.TRANSFORM`: Set integer stoichiometric coefficients.
4. `StepType.CALCULATE`: Compute moles and target product mass.
5. `StepType.VERIFY`: Total mass of reactants = Total mass of products.

### 5.3 Pedagogical Misconceptions
- **Subscript Tampering**: Changing chemical subscripts (e.g., writing $\text{H}_2\text{O}_2$ instead of $2\text{H}_2\text{O}$) to balance equations.
- **Mole vs Mass Confusion**: Applying stoichiometric ratios directly to grams rather than moles.
- **Exothermic Myth**: Believing exothermic reactions occur without an initial activation energy hurdle.

---

## 6. Security & Safe Execution (OWASP Top 10 for Agentic AI)

- **ASI01 (Prompt Hijacking)**: Physics & chemistry questions are filtered through `OwaspAgentSecurityGuard` for adversarial overrides.
- **ASI05 (No RCE / Safe Expression Parsing)**: All numerical substitutions ($F/m$, $m/M$) are verified using deterministic AST evaluators (`math-core` exp4j AST) with zero dynamic shell or JavaScript execution.
- **ASI03 (Decoupled IAM)**: Physics and Chemistry reasoning proposals conform to `libraries:explain-core`'s `EngineSecurityAuthorizer`.

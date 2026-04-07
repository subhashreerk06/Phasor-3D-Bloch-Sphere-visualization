# Phasor-3D-Bloch-Sphere-visualization

---

# Phasor — Quantum Circuit Visualiser

A gate-based quantum circuit simulator with real-time visuals, built from scratch in Java with Swing. Supports 1 and 2 qubit systems with animated phasor wheels, an interactive 3D Bloch sphere, and a live circuit diagram.

---

## Features

**Amplitude Phasor Wheels**  
Each basis state gets its own unit circle with a glowing arrow (phasor) whose angle encodes the complex phase and whose length encodes the probability amplitude. When a gate is applied, the arrows animate smoothly to their new positions rather than snapping.

**Interactive 3D Bloch Sphere**  
A full orthographic 3D projection of the Bloch sphere with latitude/longitude wireframe grid, depth-shaded front and back hemispheres, and labelled axes (|0⟩, |1⟩, |+⟩, |−⟩, |i⟩, |−i⟩). The state vector is drawn with a glow effect and a dashed drop-shadow onto the equatorial plane. Drag with the mouse to rotate freely. The sphere auto-rotates gently on startup until you interact with it. The purity indicator turns amber when a qubit becomes entangled or mixed.

**Live Circuit Diagram**  
Gates are drawn as boxes on qubit wires as you apply them, using standard notation — rounded gate boxes for single-qubit gates and circle-plus for CNOT targets.

**Gate Palette**  
H, X, Y, Z, S, T, Rz (with adjustable θ slider), CNOT q0→q1, CNOT q1→q0.

---

## Requirements

- Java 11 or later
- No external libraries — standard Java SE only

---

## Building and Running

Clone or download the project, then from the `Phasor/` root directory:

```bash
mkdir -p out
javac -d out src/*.java
java -cp out Phasor
```

On startup you will be asked whether to simulate 1 or 2 qubits. Choose 2 qubits to access CNOT and explore entanglement.

---

## Project Structure

```
Phasor/
├── src/
│   ├── Complex.java          # Immutable complex number with lerp for animation
│   ├── QuantumGate.java      # Gate matrix definitions (H, X, Y, Z, S, T, Rz)
│   ├── QuantumState.java     # Statevector, gate application, partial trace, Bloch vector
│   ├── PhasorPanel.java      # Animated phasor wheel visualisation
│   ├── BlochSpherePanel.java # Interactive 3D Bloch sphere with mouse rotation
│   ├── CircuitPanel.java     # Live circuit diagram
│   ├── ControlPanel.java     # Gate buttons, qubit selector, Rz slider
│   └── Phasor.java           # Main window and entry point
└── README.md
```

---

## How It Works

The simulator represents the quantum state as a statevector of 2ⁿ complex amplitudes (one per basis state). Applying a single-qubit gate iterates over all basis states, extracts the relevant qubit's value, routes the amplitude through the 2×2 gate matrix, and accumulates into the new statevector. CNOT is implemented directly by flipping the target qubit index whenever the control qubit is |1⟩.

The Bloch vector for each qubit is computed via partial trace — the reduced density matrix is constructed by summing over all other qubits, and the Bloch coordinates follow from the off-diagonal and diagonal elements. This gives the correct mixed-state Bloch vector even when the qubit is entangled, which is why the purity indicator |r| drops below 1 after a Bell state is created.

The 3D sphere uses orthographic projection with two Euler rotation matrices applied in sequence (Y-axis spin, then X-axis tilt). Front and back hemisphere segments are separated by checking the depth coordinate after rotation, which is what gives the wireframe its shaded appearance.

---

## Things to Try

**Bell state** — select 2 qubits, apply H to q0, then CNOT q0→q1. The phasor wheels will show equal amplitudes on |00⟩ and |11⟩ with zero on |01⟩ and |10⟩. The Bloch sphere for each individual qubit will collapse to the origin, showing that neither qubit has a well-defined state on its own.

**Phase kickback** — apply X to q1, then H to q1, then CNOT q0→q1. Watch the phase on q0's Bloch sphere shift even though you only applied gates to q1.

**Rz sweep** — apply H to put a qubit on the equator, then drag the θ slider and repeatedly apply Rz. The phasor arrow rotates around the unit circle and the Bloch sphere vector traces the equator.

---

## Implementation Notes

- All gate matrices are exact — no floating point approximation in the definitions
- The animation uses an ease-in-out curve so gate transitions feel physical rather than mechanical
- The 3D sphere redraws at ~30fps during auto-rotation and on every repaint triggered by gate application
- Fonts use `Monospaced` throughout for a consistent technical aesthetic

---

## Possible Extensions

- 3-qubit support (8 basis states, 2×4 phasor grid)
- Measurement simulation with probabilistic collapse
- Drag-and-drop circuit builder
- Export circuit to Qiskit or OpenQASM
- Noise channels (depolarising, dephasing) to show Bloch vector shrinkage

import javax.swing.*;
import java.awt.*;

// Sidebar: gate buttons, qubit selector, Rz slider, CNOT options, and reset.
public class ControlPanel extends JPanel {
    private final QuantumState state;
    private final Phasor mainWindow;
    private JComboBox<String> qubitSelector;
    private JSlider rzSlider;
    private JLabel rzLabel;

    private static final Color BG         = new Color(10, 10, 20);
    private static final Color SECTION    = new Color(90, 110, 170);
    private static final Color BTN_BG     = new Color(20, 35, 70);
    private static final Color BTN_BORDER = new Color(80, 130, 220);
    private static final Color BTN_PRESS  = new Color(50, 90, 180);
    private static final Color BTN_TEXT   = new Color(200, 215, 255);

    public ControlPanel(QuantumState state, Phasor mainWindow) {
        this.state = state;
        this.mainWindow = mainWindow;
        setBackground(BG);
        setPreferredSize(new Dimension(185, 500));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        section("Target Qubit");
        String[] qLabels = new String[state.getNumQubits()];
        for (int i = 0; i < qLabels.length; i++) qLabels[i] = "qubit " + i;
        qubitSelector = new JComboBox<>(qLabels);
        styleCombo(qubitSelector);
        add(qubitSelector);
        gap(16);

        section("Single-Qubit Gates");
        for (String name : new String[]{"H", "X", "Y", "Z", "S", "T"})
            gateBtn(name, () -> applySingle(name));
        gap(12);

        section("Rz Rotation");
        rzSlider = new JSlider(-314, 314, 157);
        rzSlider.setBackground(BG);
        rzSlider.setMaximumSize(new Dimension(162, 28));
        rzSlider.setAlignmentX(CENTER_ALIGNMENT);
        rzLabel = new JLabel("\u03b8 = \u03c0/2", SwingConstants.CENTER);
        rzLabel.setForeground(BTN_TEXT);
        rzLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        rzLabel.setAlignmentX(CENTER_ALIGNMENT);
        rzSlider.addChangeListener(e ->
            rzLabel.setText(String.format("\u03b8 = %.2f\u03c0", rzSlider.getValue() / 100.0 / Math.PI)));
        add(rzSlider);
        add(rzLabel);
        gap(5);
        gateBtn("Apply Rz", () -> {
            state.applySingleQubitGate(QuantumGate.Rz(rzSlider.getValue() / 100.0),
                                       qubitSelector.getSelectedIndex());
            mainWindow.refresh();
        });
        gap(12);

        if (state.getNumQubits() >= 2) {
            section("Two-Qubit Gates");
            gateBtn("CNOT q0\u2192q1", () -> { state.applyCNOT(0, 1); mainWindow.refresh(); });
            gateBtn("CNOT q1\u2192q0", () -> { state.applyCNOT(1, 0); mainWindow.refresh(); });
            gap(12);
        }

        section("Circuit");
        gateBtn("Reset |0\u27E9", () -> { state.reset(); mainWindow.refresh(); });
    }

    private void applySingle(String name) {
        int q = qubitSelector.getSelectedIndex();
        QuantumGate gate;
        if      (name.equals("H")) gate = QuantumGate.H();
        else if (name.equals("X")) gate = QuantumGate.X();
        else if (name.equals("Y")) gate = QuantumGate.Y();
        else if (name.equals("Z")) gate = QuantumGate.Z();
        else if (name.equals("S")) gate = QuantumGate.S();
        else if (name.equals("T")) gate = QuantumGate.T();
        else return;
        state.applySingleQubitGate(gate, q);
        mainWindow.refresh();
    }

    private void section(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setForeground(SECTION);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        add(lbl);
        gap(6);
    }

    private void gap(int size) { add(Box.createVerticalStrut(size)); }

    private void gateBtn(String label, Runnable action) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BTN_PRESS : BTN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getModel().isRollover() ? BTN_TEXT : BTN_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setForeground(BTN_TEXT);
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(162, 33));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        add(btn);
        gap(5);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(BTN_BG);
        combo.setForeground(BTN_TEXT);
        combo.setFont(new Font("Monospaced", Font.PLAIN, 13));
        combo.setMaximumSize(new Dimension(162, 30));
        combo.setAlignmentX(CENTER_ALIGNMENT);
    }
}

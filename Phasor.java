import javax.swing.*;
import java.awt.*;

// Entry point and main window.
public class Phasor extends JFrame {
    private final PhasorPanel      phasorPanel;
    private final BlochSpherePanel blochPanel;
    private final CircuitPanel     circuitPanel;

    public Phasor(int numQubits) {
        super("Phasor \u2014 Quantum Circuit Visualiser");

        QuantumState state = new QuantumState(numQubits);

        phasorPanel   = new PhasorPanel(state);
        blochPanel    = new BlochSpherePanel(state);
        circuitPanel  = new CircuitPanel(state);
        ControlPanel controlPanel = new ControlPanel(state, this);

        // Header bar
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 9));
        header.setBackground(new Color(7, 7, 16));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(28, 28, 55)));

        JLabel appName = new JLabel("  \u03a8  PHASOR");
        appName.setForeground(new Color(100, 175, 255));
        appName.setFont(new Font("Monospaced", Font.BOLD, 20));

        JLabel subtitle = new JLabel("    Quantum Circuit Visualiser  \u2014  "
                + numQubits + " qubit" + (numQubits > 1 ? "s" : ""));
        subtitle.setForeground(new Color(85, 95, 150));
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 13));

        header.add(appName);
        header.add(subtitle);

        // Centre panel: phasor wheels left, 3D Bloch sphere right
        JPanel centre = new JPanel(new GridLayout(1, 2, 8, 0));
        centre.setBackground(new Color(10, 10, 20));
        centre.add(phasorPanel);
        centre.add(blochPanel);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(10, 10, 20));
        root.add(header,       BorderLayout.NORTH);
        root.add(centre,       BorderLayout.CENTER);
        root.add(circuitPanel, BorderLayout.SOUTH);
        root.add(controlPanel, BorderLayout.EAST);

        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(1020, 740));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Called by ControlPanel after every gate application
    public void refresh() {
        phasorPanel.update();
        blochPanel.update();
        circuitPanel.update();
    }

    public static void main(String[] args) {
        String[] opts = {"1 Qubit", "2 Qubits"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "How many qubits would you like to simulate?",
            "Phasor \u2014 Setup",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, opts, opts[1]
        );
        int n = (choice == 0) ? 1 : 2;
        SwingUtilities.invokeLater(() -> new Phasor(n));
    }
}

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Live circuit diagram — draws qubit wires and gate boxes as you apply gates.
public class CircuitPanel extends JPanel {
    private final QuantumState state;

    private static final Color BG          = new Color(10, 10, 20);
    private static final Color WIRE_COLOR  = new Color(70, 70, 120);
    private static final Color GATE_FILL   = new Color(22, 38, 75);
    private static final Color GATE_BORDER = new Color(90, 150, 240);
    private static final Color GATE_TEXT   = new Color(210, 225, 255);
    private static final Color CTRL_COLOR  = new Color(90, 150, 240);

    public CircuitPanel(QuantumState state) {
        this.state = state;
        setBackground(BG);
        setPreferredSize(new Dimension(920, 125));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(28, 28, 55)));
    }

    public void update() { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int numQubits   = state.getNumQubits();
        List<String> history = state.getHistory();
        int startX      = 80;
        int gateW       = 50;
        int gateH       = 30;
        int gateGap     = 8;
        int topPad      = 16;
        int wireSpacing = (getHeight() - topPad - 10) / (numQubits + 1);

        // Qubit labels and wires
        for (int q = 0; q < numQubits; q++) {
            int wireY = topPad + (q + 1) * wireSpacing;
            g2.setColor(new Color(140, 150, 205));
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.drawString("q" + q + " |0\u27E9", 6, wireY + 5);
            g2.setColor(WIRE_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(startX, wireY, getWidth() - 10, wireY);
        }

        // Gate boxes from history
        int x = startX + 8;
        for (String entry : history) {
            String[] parts = entry.split(" \u2192 ");
            if (parts.length < 2) continue;
            String gateName = parts[0];
            String target   = parts[1];

            if (target.startsWith("ctrl=")) {
                int ctrl = 0, tgt = 1;
                try {
                    ctrl = Integer.parseInt(target.split(",")[0].replace("ctrl=q", "").trim());
                    tgt  = Integer.parseInt(target.split(",")[1].replace(" tgt=q", "").trim());
                } catch (NumberFormatException ignored) {}

                int ctrlY = topPad + (ctrl + 1) * wireSpacing;
                int tgtY  = topPad + (tgt  + 1) * wireSpacing;
                int midX  = x + gateW / 2;

                g2.setColor(CTRL_COLOR);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawLine(midX, ctrlY, midX, tgtY);
                g2.fillOval(midX - 5, ctrlY - 5, 10, 10);

                g2.setColor(GATE_FILL);
                g2.fillOval(midX - 14, tgtY - 14, 28, 28);
                g2.setColor(CTRL_COLOR);
                g2.drawOval(midX - 14, tgtY - 14, 28, 28);
                g2.drawLine(midX - 14, tgtY, midX + 14, tgtY);
                g2.drawLine(midX, tgtY - 14, midX, tgtY + 14);

            } else {
                int q = 0;
                try { q = Integer.parseInt(target.replace("q", "")); }
                catch (NumberFormatException ignored) {}
                int wireY = topPad + (q + 1) * wireSpacing;

                g2.setColor(GATE_FILL);
                g2.fillRoundRect(x, wireY - gateH/2, gateW, gateH, 8, 8);
                g2.setColor(GATE_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, wireY - gateH/2, gateW, gateH, 8, 8);

                String label = gateName.length() > 5 ? gateName.substring(0, 5) : gateName;
                g2.setColor(GATE_TEXT);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x + (gateW - fm.stringWidth(label)) / 2, wireY + 5);
            }

            x += gateW + gateGap;
            if (x + gateW > getWidth() - 10) break;
        }
    }
}

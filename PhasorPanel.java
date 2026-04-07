import javax.swing.*;
import java.awt.*;

// Each basis state gets a phasor wheel: a unit circle with an arrow whose
// angle = complex phase and length = amplitude magnitude.
// When a gate fires, the arrows animate smoothly to their new positions.
public class PhasorPanel extends JPanel {
    private final QuantumState state;
    private Timer animTimer;
    private double animT = 1.0;
    private Complex[] displayAmplitudes;

    private static final Color BG           = new Color(10, 10, 20);
    private static final Color CIRCLE_COLOR = new Color(45, 45, 75);
    private static final Color GRID_COLOR   = new Color(28, 28, 48);
    private static final Color PHASOR_COLOR = new Color(100, 180, 255);
    private static final Color FILL_COLOR   = new Color(80, 60, 180, 60);
    private static final Color PROB_COLOR   = new Color(100, 255, 180);
    private static final Color TEXT_COLOR   = new Color(190, 200, 230);

    public PhasorPanel(QuantumState state) {
        this.state = state;
        setBackground(BG);
        setPreferredSize(new Dimension(520, 460));
        displayAmplitudes = state.getAmplitudes().clone();

        // ~60fps; animT goes 0→1 over ~400ms
        animTimer = new Timer(16, e -> {
            animT = Math.min(1.0, animT + 0.04);
            displayAmplitudes = state.getInterpolated(easeInOut(animT));
            repaint();
            if (animT >= 1.0) animTimer.stop();
        });
    }

    public void update() {
        animT = 0.0;
        animTimer.restart();
    }

    private double easeInOut(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int dim  = displayAmplitudes.length;
        int cols = (int) Math.ceil(Math.sqrt(dim));
        int rows = (int) Math.ceil((double) dim / cols);

        int cellW  = getWidth() / cols;
        int cellH  = (getHeight() - 38) / rows;

        g2.setColor(new Color(120, 140, 200));
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("Amplitude Phasors", 14, 26);

        for (int i = 0; i < dim; i++) {
            int col = i % cols;
            int row = i / cols;
            int cx  = col * cellW + cellW / 2;
            int cy  = 38 + row * cellH + cellH / 2;
            int r   = Math.min(cellW, cellH) / 2 - 26;
            drawPhasor(g2, displayAmplitudes[i], cx, cy, r, i);
        }
    }

    private void drawPhasor(Graphics2D g2, Complex amp, int cx, int cy, int r, int index) {
        String label = "|" + toBinary(index, state.getNumQubits()) + "\u27E9";
        double prob  = amp.magnitudeSquared();
        double mag   = amp.magnitude();
        double phase = amp.phase();

        // Probability fill arc
        g2.setColor(FILL_COLOR);
        g2.fillArc(cx - r, cy - r, 2*r, 2*r, 90, -(int)(prob * 360));

        // Unit circle and crosshairs
        g2.setColor(CIRCLE_COLOR);
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(cx - r, cy - r, 2*r, 2*r);
        g2.setColor(GRID_COLOR);
        g2.drawLine(cx - r, cy, cx + r, cy);
        g2.drawLine(cx, cy - r, cx, cy + r);

        // Phasor tip position (y flipped: positive phase goes up on screen)
        int px = (int)(cx + r * mag * Math.cos(phase));
        int py = (int)(cy - r * mag * Math.sin(phase));

        // Glow layers for neon look
        for (int glow = 5; glow >= 1; glow--) {
            float alpha = (5.0f - glow) / 5.0f * 0.6f;
            g2.setColor(new Color(100/255f, 180/255f, 255/255f, alpha));
            g2.setStroke(new BasicStroke(glow * 1.5f));
            g2.drawLine(cx, cy, px, py);
        }

        // Solid arrow and arrowhead
        g2.setColor(PHASOR_COLOR);
        g2.setStroke(new BasicStroke(2.3f));
        g2.drawLine(cx, cy, px, py);
        drawArrowHead(g2, cx, cy, px, py, 9);

        // Bright tip dot and phase ring
        g2.setColor(Color.WHITE);
        g2.fillOval(px - 3, py - 3, 6, 6);

        g2.setColor(new Color(255, 200, 80, 150));
        g2.setStroke(new BasicStroke(1.5f));
        int ringR = 15;
        g2.drawArc(cx - ringR, cy - ringR, 2*ringR, 2*ringR, 0, -(int)Math.toDegrees(phase));

        // Labels
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(TEXT_COLOR);
        g2.drawString(label, cx - r + 2, cy + r + 17);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(PROB_COLOR);
        g2.drawString(String.format("p=%.3f", prob), cx - r + 2, cy + r + 32);

        g2.setColor(new Color(155, 165, 195));
        g2.drawString(amp.toString(), cx - r + 2, cy + r + 46);
    }

    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2, int size) {
        if (x1 == x2 && y1 == y2) return;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int x3 = (int)(x2 - size * Math.cos(angle - Math.PI/7));
        int y3 = (int)(y2 - size * Math.sin(angle - Math.PI/7));
        int x4 = (int)(x2 - size * Math.cos(angle + Math.PI/7));
        int y4 = (int)(y2 - size * Math.sin(angle + Math.PI/7));
        g2.fillPolygon(new int[]{x2, x3, x4}, new int[]{y2, y3, y4}, 3);
    }

    private String toBinary(int n, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = bits - 1; i >= 0; i--) sb.append((n >> i) & 1);
        return sb.toString();
    }
}

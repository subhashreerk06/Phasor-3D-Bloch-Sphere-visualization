import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// A proper 3D Bloch sphere rendered with orthographic projection.
// Latitude and longitude grid lines are depth-shaded (bright front, dim back).
// Drag with the mouse to spin it. It auto-rotates gently until you touch it.
public class BlochSpherePanel extends JPanel implements MouseListener, MouseMotionListener {
    private QuantumState state;
    private int selectedQubit = 0;

    // Euler angles controlling the view — mouse drag updates these
    private double angleY = 0.5;
    private double angleX = -0.25;
    private int lastMouseX, lastMouseY;
    private boolean userTouched = false;
    private Timer autoRotate;

    private static final Color BG         = new Color(10, 10, 20);
    private static final Color GRID_FRONT = new Color(55, 55, 105);
    private static final Color GRID_BACK  = new Color(28, 28, 52);
    private static final Color VEC_COLOR  = new Color(255, 110, 70);
    private static final Color AXIS_Z     = new Color(90, 220, 120);
    private static final Color AXIS_X     = new Color(90, 160, 255);
    private static final Color AXIS_Y     = new Color(200, 100, 255);

    public BlochSpherePanel(QuantumState state) {
        this.state = state;
        setBackground(BG);
        setPreferredSize(new Dimension(400, 460));
        addMouseListener(this);
        addMouseMotionListener(this);

        autoRotate = new Timer(30, e -> {
            if (!userTouched) { angleY += 0.007; repaint(); }
        });
        autoRotate.start();
    }

    // Project a 3D world point to 2D screen coordinates.
    // wx=right, wy=up, wz=toward viewer (before rotation).
    private int[] project(double wx, double wy, double wz, int cx, int cy, int scale) {
        double x1 =  wx * Math.cos(angleY) + wz * Math.sin(angleY);
        double z1 = -wx * Math.sin(angleY) + wz * Math.cos(angleY);
        double y2 =  wy * Math.cos(angleX) - z1 * Math.sin(angleX);
        double x2 = x1;
        return new int[]{ cx + (int)(x2 * scale), cy - (int)(y2 * scale) };
    }

    // Depth after rotation: positive means facing the viewer (front hemisphere).
    private double depth(double wx, double wy, double wz) {
        double z1 = -wx * Math.sin(angleY) + wz * Math.cos(angleY);
        return wy * Math.sin(angleX) + z1 * Math.cos(angleX);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx    = getWidth() / 2;
        int cy    = getHeight() / 2 + 15;
        int scale = Math.min(getWidth(), getHeight()) / 2 - 65;

        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(120, 140, 200));
        g2.drawString("Bloch Sphere \u2014 q" + selectedQubit, 14, 28);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(new Color(65, 75, 125));
        g2.drawString("drag to rotate", 14, 46);

        // Dark sphere fill
        g2.setColor(new Color(14, 14, 26));
        g2.fillOval(cx - scale, cy - scale, 2 * scale, 2 * scale);

        // Grid: back hemisphere first, then front so front lines always appear on top
        drawGrid(g2, cx, cy, scale, false);
        drawGrid(g2, cx, cy, scale, true);

        // Sphere rim
        g2.setColor(new Color(50, 50, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - scale, cy - scale, 2 * scale, 2 * scale);

        // Axes — Bloch convention: z up = |0>, x right = |+>, y depth = |i>
        drawAxis(g2, cx, cy, scale,  0,  1,  0,  0, -1,  0, AXIS_Z, "|0\u27E9", "|1\u27E9");
        drawAxis(g2, cx, cy, scale,  1,  0,  0, -1,  0,  0, AXIS_X, "|+\u27E9", "|-\u27E9");
        drawAxis(g2, cx, cy, scale,  0,  0,  1,  0,  0, -1, AXIS_Y, "|i\u27E9", "|-i\u27E9");

        int q = Math.min(selectedQubit, state.getNumQubits() - 1);
        double[] b = state.getBlochVector(q);
        // b[0]=x, b[1]=y, b[2]=z in Bloch coords → map to world: x→wx, z→wy(up), y→wz(depth)
        drawBlochVector(g2, b[0], b[2], b[1], cx, cy, scale);
        drawReadout(g2, b);
    }

    private void drawGrid(Graphics2D g2, int cx, int cy, int scale, boolean frontOnly) {
        int steps = 80;

        // Latitude circles
        int[] lats = {-60, -30, 0, 30, 60};
        for (int lat : lats) {
            double elev = Math.toRadians(lat);
            double r  = Math.cos(elev);
            double wy = Math.sin(elev);
            boolean equator = (lat == 0);
            int[] prev = null;
            for (int i = 0; i <= steps; i++) {
                double theta = 2 * Math.PI * i / steps;
                double wx = r * Math.cos(theta);
                double wz = r * Math.sin(theta);
                boolean front = depth(wx, wy, wz) > 0;
                if (front != frontOnly) { prev = null; continue; }
                int[] pt = project(wx, wy, wz, cx, cy, scale);
                if (prev != null) {
                    g2.setColor(equator ? new Color(70, 70, 125) : (front ? GRID_FRONT : GRID_BACK));
                    g2.setStroke(new BasicStroke(equator ? 1.3f : 0.8f));
                    g2.drawLine(prev[0], prev[1], pt[0], pt[1]);
                }
                prev = pt;
            }
        }

        // Longitude great circles
        for (int lon = 0; lon < 180; lon += 30) {
            double theta = Math.toRadians(lon);
            int[] prev = null;
            for (int i = 0; i <= steps; i++) {
                double phi = 2 * Math.PI * i / steps;
                double wx = Math.sin(phi) * Math.cos(theta);
                double wy = Math.cos(phi);
                double wz = Math.sin(phi) * Math.sin(theta);
                boolean front = depth(wx, wy, wz) > 0;
                if (front != frontOnly) { prev = null; continue; }
                int[] pt = project(wx, wy, wz, cx, cy, scale);
                if (prev != null) {
                    g2.setColor(front ? GRID_FRONT : GRID_BACK);
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawLine(prev[0], prev[1], pt[0], pt[1]);
                }
                prev = pt;
            }
        }
    }

    private void drawAxis(Graphics2D g2, int cx, int cy, int scale,
                          double ax, double ay, double az,
                          double bx, double by, double bz,
                          Color col, String labelPos, String labelNeg) {
        double ext = 1.22;
        int[] pos = project(ax*ext, ay*ext, az*ext, cx, cy, scale);
        int[] neg = project(bx*ext, by*ext, bz*ext, cx, cy, scale);

        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 170));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(cx, cy, pos[0], pos[1]);

        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 80));
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                     0, new float[]{5, 4}, 0));
        g2.drawLine(cx, cy, neg[0], neg[1]);

        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(col);
        g2.drawString(labelPos, pos[0] + 5, pos[1] + 5);
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 150));
        g2.drawString(labelNeg, neg[0] + 5, neg[1] + 5);
    }

    private void drawBlochVector(Graphics2D g2, double wx, double wy, double wz,
                                 int cx, int cy, int scale) {
        int[] tip    = project(wx, wy, wz, cx, cy, scale);
        int[] shadow = project(wx,  0, wz, cx, cy, scale);

        // Dashed drop-shadow to equatorial plane
        g2.setColor(new Color(255, 110, 70, 55));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                     0, new float[]{4, 3}, 0));
        g2.drawLine(tip[0], tip[1], shadow[0], shadow[1]);
        g2.fillOval(shadow[0] - 3, shadow[1] - 3, 6, 6);

        // Glow layers
        for (int glow = 7; glow >= 1; glow--) {
            float alpha = (7.0f - glow) / 7.0f * 0.45f;
            g2.setColor(new Color(1.0f, 0.43f, 0.27f, alpha));
            g2.setStroke(new BasicStroke(glow + 1.5f));
            g2.drawLine(cx, cy, tip[0], tip[1]);
        }

        g2.setColor(VEC_COLOR);
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(cx, cy, tip[0], tip[1]);
        g2.fillOval(tip[0] - 6, tip[1] - 6, 12, 12);

        g2.setColor(new Color(90, 90, 140));
        g2.fillOval(cx - 3, cy - 3, 6, 6);
    }

    private void drawReadout(Graphics2D g2, double[] b) {
        double purity = Math.sqrt(b[0]*b[0] + b[1]*b[1] + b[2]*b[2]);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        int rx = 14, ry = getHeight() - 66;
        g2.setColor(new Color(150, 210, 155));
        g2.drawString(String.format("x = %.4f", b[0]), rx, ry);
        g2.drawString(String.format("y = %.4f", b[1]), rx, ry + 17);
        g2.drawString(String.format("z = %.4f", b[2]), rx, ry + 34);
        g2.setColor(purity > 0.99 ? new Color(90, 255, 170) : new Color(255, 175, 75));
        g2.drawString(String.format("|r| = %.4f  %s", purity,
            purity > 0.99 ? "(pure)" : "(mixed/entangled)"), rx, ry + 51);
    }

    @Override public void mousePressed(MouseEvent e)  { lastMouseX = e.getX(); lastMouseY = e.getY(); userTouched = true; }
    @Override public void mouseDragged(MouseEvent e)  {
        angleY += (e.getX() - lastMouseX) * 0.012;
        angleX += (e.getY() - lastMouseY) * 0.012;
        angleX = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, angleX));
        lastMouseX = e.getX(); lastMouseY = e.getY();
        repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}

    public void setSelectedQubit(int q) { this.selectedQubit = q; repaint(); }
    public void update() { repaint(); }
}

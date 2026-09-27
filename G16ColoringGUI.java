import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.RenderingHints;
import java.util.*;
import java.util.List;

/**
 * G16 Triangle-Free 3-Edge-Coloring Visualizer
 * Greenwood-Gleason (1955) construction via GF(16)
 *
 * Compile:  javac G16ColoringGUI.java
 * Run:      java G16ColoringGUI
 */
public class G16ColoringGUI extends JFrame {

    //  GF(16) construction 
    static final int N = 16;
    static final int[] POW = new int[15];
    static final int[] LOG = new int[16];
    static final int[][] adj = new int[N][N];

    static final Color COL_RED   = new Color(220, 60,  60);
    static final Color COL_GREEN = new Color(60,  170, 60);
    static final Color COL_BLUE  = new Color(50,  120, 220);
    static final Color[] COLORS  = {COL_RED, COL_GREEN, COL_BLUE};
    static final String[] CNAMES = {"Red", "Green", "Blue"};

    static int gfMul(int a, int b) {
        int p = 0;
        for (int i = 0; i < 4; i++) {
            if ((b & 1) != 0) p ^= a;
            int hb = a & 8;
            a = (a << 1) & 0xF;
            if (hb != 0) a ^= 0x3;
            b >>= 1;
        }
        return p;
    }

    static void buildTables() {
        POW[0] = 1;
        for (int i = 1; i < 15; i++) POW[i] = gfMul(POW[i - 1], 2);
        Arrays.fill(LOG, -1);
        for (int i = 0; i < 15; i++) LOG[POW[i]] = i;
        for (int i = 0; i < N; i++) Arrays.fill(adj[i], -1);
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++) {
                int c = LOG[i ^ j] % 3;
                adj[i][j] = c;
                adj[j][i] = c;
            }
    }

    //  GUI components 
    GraphPanel graphPanel;
    JLabel statusLabel;
    JCheckBox[] colorToggle = new JCheckBox[3];
    JSlider alphaSlider;
    JList<String> neighborList;
    DefaultListModel<String> listModel = new DefaultListModel<>();

    public G16ColoringGUI() {
        super("G16 Coloring  -  Greenwood-Gleason (1955)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(28, 28, 32));

        // Graph panel
        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(700, 700));
        add(graphPanel, BorderLayout.CENTER);

        // Right panel
        JPanel right = buildRightPanel();
        add(right, BorderLayout.EAST);

        // Bottom status bar
        statusLabel = new JLabel("  Click a vertex to inspect its neighbors");
        statusLabel.setForeground(new Color(180, 180, 180));
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(20, 20, 24));
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(28, 28, 32));
        p.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));
        p.setPreferredSize(new Dimension(220, 700));

        // Title
        JLabel title = new JLabel("G16 Coloring");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);

        JLabel subtitle = new JLabel("K16 - 3 colors - no mono triangle");
        subtitle.setForeground(new Color(140, 140, 150));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(subtitle);
        p.add(Box.createVerticalStrut(16));

        // Color toggles
        JLabel toggleLabel = styledLabel("Show / Hide Colors", true);
        p.add(toggleLabel);
        p.add(Box.createVerticalStrut(6));

        String[] labels = {"Red edges (40)", "Green edges (40)", "Blue edges (40)"};
        for (int c = 0; c < 3; c++) {
            colorToggle[c] = new JCheckBox(labels[c], true);
            colorToggle[c].setForeground(COLORS[c].brighter());
            colorToggle[c].setBackground(new Color(28, 28, 32));
            colorToggle[c].setFont(new Font("SansSerif", Font.PLAIN, 12));
            colorToggle[c].setAlignmentX(Component.LEFT_ALIGNMENT);
            final int fc = c;
            colorToggle[c].addActionListener(e -> graphPanel.repaint());
            p.add(colorToggle[c]);
        }
        p.add(Box.createVerticalStrut(14));

        // Edge opacity slider
        p.add(styledLabel("Edge opacity", true));
        p.add(Box.createVerticalStrut(4));
        alphaSlider = new JSlider(10, 100, 55);
        alphaSlider.setBackground(new Color(28, 28, 32));
        alphaSlider.setForeground(new Color(180, 180, 180));
        alphaSlider.setMaximumSize(new Dimension(200, 30));
        alphaSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        alphaSlider.addChangeListener(e -> graphPanel.repaint());
        p.add(alphaSlider);
        p.add(Box.createVerticalStrut(14));

        // Layout selector
        p.add(styledLabel("Layout", true));
        p.add(Box.createVerticalStrut(4));
        String[] layouts = {"Circle", "Two rings", "Grid 4x4"};
        JComboBox<String> layoutBox = new JComboBox<>(layouts);
        layoutBox.setBackground(new Color(40, 40, 48));
        layoutBox.setForeground(Color.WHITE);
        layoutBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        layoutBox.setMaximumSize(new Dimension(200, 28));
        layoutBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        layoutBox.addActionListener(e -> {
            graphPanel.layout = layoutBox.getSelectedIndex();
            graphPanel.computePositions();
            graphPanel.repaint();
        });
        p.add(layoutBox);
        p.add(Box.createVerticalStrut(16));

        // Reset highlight button
        JButton resetBtn = new JButton("Clear selection");
        resetBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resetBtn.setBackground(new Color(50, 50, 60));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            graphPanel.selected = -1;
            listModel.clear();
            statusLabel.setText("  Click a vertex to inspect its neighbors");
            graphPanel.repaint();
        });
        p.add(resetBtn);
        p.add(Box.createVerticalStrut(14));

        // Neighbor list
        p.add(styledLabel("Neighbors of selected vertex", true));
        p.add(Box.createVerticalStrut(4));
        neighborList = new JList<>(listModel);
        neighborList.setBackground(new Color(20, 20, 28));
        neighborList.setForeground(new Color(200, 200, 200));
        neighborList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        neighborList.setSelectionBackground(new Color(60, 60, 80));
        JScrollPane scroll = new JScrollPane(neighborList);
        scroll.setPreferredSize(new Dimension(200, 180));
        scroll.setMaximumSize(new Dimension(200, 220));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        p.add(scroll);
        p.add(Box.createVerticalStrut(14));

        // Stats
        p.add(styledLabel("Stats", true));
        p.add(Box.createVerticalStrut(4));
        String[] stats = {
            "Vertices : 16",
            "Edges    : 120",
            "Per color: 40",
            "Degree   : 15 (5+5+5)",
            "R(3,3,3) = 17"
        };
        for (String s : stats) {
            JLabel l = new JLabel(s);
            l.setForeground(new Color(160, 200, 160));
            l.setFont(new Font("Monospaced", Font.PLAIN, 11));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l);
        }

        p.add(Box.createVerticalGlue());
        return p;
    }

    JLabel styledLabel(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(200, 200, 210));
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    //  Graph drawing panel 
    class GraphPanel extends JPanel {
        double[] vx = new double[N];
        double[] vy = new double[N];
        int selected = -1;
        int hovered  = -1;
        int layout   = 0; // 0=circle, 1=two rings, 2=grid

        GraphPanel() {
            setBackground(new Color(18, 18, 22));
            computePositions();

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    int h = nearestVertex(e.getX(), e.getY(), 20);
                    if (h != hovered) { hovered = h; repaint(); }
                }
            });

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int v = nearestVertex(e.getX(), e.getY(), 20);
                    selected = (v == selected) ? -1 : v;
                    updateNeighborList();
                    repaint();
                }
                public void mouseExited(MouseEvent e) {
                    hovered = -1; repaint();
                }
            });
        }

        void computePositions() {
            int W = getPreferredSize().width;
            int H = getPreferredSize().height;
            double cx = W / 2.0, cy = H / 2.0;
            if (layout == 0) {
                // Single circle
                double r = Math.min(cx, cy) * 0.80;
                for (int i = 0; i < N; i++) {
                    double a = 2 * Math.PI * i / N - Math.PI / 2;
                    vx[i] = cx + r * Math.cos(a);
                    vy[i] = cy + r * Math.sin(a);
                }
            } else if (layout == 1) {
                // Two rings: inner 6, outer 10
                double r1 = Math.min(cx, cy) * 0.38;
                double r2 = Math.min(cx, cy) * 0.78;
                for (int i = 0; i < 6; i++) {
                    double a = 2 * Math.PI * i / 6 - Math.PI / 2;
                    vx[i] = cx + r1 * Math.cos(a);
                    vy[i] = cy + r1 * Math.sin(a);
                }
                for (int i = 0; i < 10; i++) {
                    double a = 2 * Math.PI * i / 10 - Math.PI / 2;
                    vx[6 + i] = cx + r2 * Math.cos(a);
                    vy[6 + i] = cy + r2 * Math.sin(a);
                }
            } else {
                // 4x4 grid
                double margin = 80;
                double step = (Math.min(W, H) - 2 * margin) / 3.0;
                for (int i = 0; i < N; i++) {
                    int row = i / 4, col = i % 4;
                    vx[i] = margin + col * step;
                    vy[i] = margin + row * step;
                }
            }
        }

        int nearestVertex(int mx, int my, int threshold) {
            int best = -1; double bestD = threshold;
            for (int i = 0; i < N; i++) {
                double d = Math.hypot(vx[i] - mx, vy[i] - my);
                if (d < bestD) { bestD = d; best = i; }
            }
            return best;
        }

        void updateNeighborList() {
            listModel.clear();
            if (selected < 0) {
                statusLabel.setText("  Click a vertex to inspect its neighbors");
                return;
            }
            int[] cnt = new int[3];
            for (int j = 0; j < N; j++) if (adj[selected][j] >= 0) cnt[adj[selected][j]]++;
            statusLabel.setText(String.format(
                "  Vertex %d  |  Red: %d  Green: %d  Blue: %d",
                selected, cnt[0], cnt[1], cnt[2]));
            for (int c = 0; c < 3; c++) {
                List<Integer> nb = new ArrayList<>();
                for (int j = 0; j < N; j++) if (adj[selected][j] == c) nb.add(j);
                listModel.addElement(CNAMES[c] + ": " + nb);
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int W = getWidth(), H = getHeight();
            double cx = W / 2.0, cy = H / 2.0;
            double scale = Math.min(W, H) / (double) getPreferredSize().width;

            // Recompute positions for actual size
            double[] rx = new double[N], ry = new double[N];
            if (layout == 0) {
                double r = Math.min(cx, cy) * 0.80;
                for (int i = 0; i < N; i++) {
                    double a = 2 * Math.PI * i / N - Math.PI / 2;
                    rx[i] = cx + r * Math.cos(a);
                    ry[i] = cy + r * Math.sin(a);
                }
            } else if (layout == 1) {
                double r1 = Math.min(cx, cy) * 0.38;
                double r2 = Math.min(cx, cy) * 0.78;
                for (int i = 0; i < 6; i++) {
                    double a = 2 * Math.PI * i / 6 - Math.PI / 2;
                    rx[i] = cx + r1 * Math.cos(a);
                    ry[i] = cy + r1 * Math.sin(a);
                }
                for (int i = 0; i < 10; i++) {
                    double a = 2 * Math.PI * i / 10 - Math.PI / 2;
                    rx[6 + i] = cx + r2 * Math.cos(a);
                    ry[6 + i] = cy + r2 * Math.sin(a);
                }
            } else {
                double margin = 80 * scale;
                double step = (Math.min(W, H) - 2 * margin) / 3.0;
                for (int i = 0; i < N; i++) {
                    rx[i] = margin + (i % 4) * step;
                    ry[i] = margin + (i / 4) * step;
                }
            }

            float alpha = alphaSlider.getValue() / 100f;

            // Draw edges
            for (int i = 0; i < N; i++) {
                for (int j = i + 1; j < N; j++) {
                    int c = adj[i][j];
                    if (!colorToggle[c].isSelected()) continue;

                    boolean selEdge = (selected == i || selected == j);
                    boolean hovEdge = (hovered  == i || hovered  == j);

                    float a;
                    float width;
                    if (selected >= 0) {
                        a     = selEdge ? Math.min(alpha + 0.3f, 1f) : alpha * 0.12f;
                        width = selEdge ? 2.2f : 0.7f;
                    } else if (hovered >= 0) {
                        a     = hovEdge ? Math.min(alpha + 0.2f, 1f) : alpha * 0.2f;
                        width = hovEdge ? 1.8f : 0.7f;
                    } else {
                        a     = alpha * 0.65f;
                        width = 1.0f;
                    }

                    Color base = COLORS[c];
                    g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(),
                            Math.min(255, (int)(a * 255))));
                    g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine((int) rx[i], (int) ry[i], (int) rx[j], (int) ry[j]);
                }
            }

            // Draw vertices
            int vr = (int)(10 * Math.sqrt(scale) + 4);
            for (int i = 0; i < N; i++) {
                int x = (int) rx[i], y = (int) ry[i];
                boolean isSel = (i == selected);
                boolean isHov = (i == hovered);

                // Glow ring for selected/hovered
                if (isSel) {
                    g.setColor(new Color(255, 255, 100, 60));
                    g.fillOval(x - vr - 5, y - vr - 5, (vr + 5) * 2, (vr + 5) * 2);
                } else if (isHov) {
                    g.setColor(new Color(200, 200, 255, 40));
                    g.fillOval(x - vr - 3, y - vr - 3, (vr + 3) * 2, (vr + 3) * 2);
                }

                // Vertex fill
                Color fill = isSel ? new Color(255, 240, 80)
                           : isHov ? new Color(180, 180, 255)
                                   : new Color(220, 220, 230);
                g.setColor(fill);
                g.fillOval(x - vr, y - vr, vr * 2, vr * 2);

                // Vertex border
                g.setColor(new Color(80, 80, 100));
                g.setStroke(new BasicStroke(1.2f));
                g.drawOval(x - vr, y - vr, vr * 2, vr * 2);

                // Vertex label
                g.setFont(new Font("SansSerif", Font.BOLD, (int)(10 * Math.sqrt(scale) + 2)));
                g.setColor(new Color(20, 20, 30));
                FontMetrics fm = g.getFontMetrics();
                String lbl = String.valueOf(i);
                g.drawString(lbl, x - fm.stringWidth(lbl) / 2, y + fm.getAscent() / 2 - 1);
            }

            // Legend overlay (top-left)
            drawLegend(g);
        }

        void drawLegend(Graphics2D g) {
            int lx = 14, ly = 14;
            int lw = 150, lh = 90;
            g.setColor(new Color(10, 10, 15, 180));
            g.fillRoundRect(lx, ly, lw, lh, 10, 10);
            g.setColor(new Color(80, 80, 100));
            g.setStroke(new BasicStroke(0.8f));
            g.drawRoundRect(lx, ly, lw, lh, 10, 10);

            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.setColor(new Color(200, 200, 210));
            g.drawString("Edge colors", lx + 10, ly + 18);

            String[] desc = {"Red  (Clebsch graph 1)",
                             "Green (Clebsch graph 2)",
                             "Blue  (Clebsch graph 3)"};
            for (int c = 0; c < 3; c++) {
                int y = ly + 34 + c * 18;
                g.setColor(COLORS[c]);
                g.setStroke(new BasicStroke(3f));
                g.drawLine(lx + 10, y, lx + 28, y);
                g.setColor(new Color(180, 180, 185));
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g.drawString(desc[c], lx + 34, y + 4);
            }
        }
    }

    //  Entry point 
    public static void main(String[] args) {
        buildTables();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new G16ColoringGUI();
        });
    }
}

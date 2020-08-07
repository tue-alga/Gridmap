package gui.panels;

import java.awt.*;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import Utils.Utils;
import algorithms.honors.Tree;

/**
 * Panel on which you can see a tree. The tree is drawn using the
 * {@link Tree#xCoordinate} and {@link Tree#yCoordinate} of each node. No
 * layouting whatsoever is done in that regard.
 */
public class TreePanel extends JPanel {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8136535847888459785L;
    /**
     * If the subtrees should be coloured to indicate if it is subTree1 or
     * subTree2.
     */
    public boolean drawColouredSubtrees = false;

    private Tree tree;

    public static final float SCALE = 20;

    /**
     * Construct a new TreePanel that does not show a tree.
     */
    public TreePanel() {
        this(null);
    }

    /**
     * Construct a new TreePanel that shows the given tree.
     *
     * @param tree Tree to show. If {@code tree == null}, nothing will be shown.
     */
    public TreePanel(Tree tree) {
        this.tree = tree;

        setBorder(BorderFactory.createLoweredBevelBorder());
        setOpaque(true);
    }

    /**
     * Changes the tree to be drawn to the given one.
     *
     * @param tree Tree to be drawn.
     */
    public void setTree(Tree tree) {
        this.tree = tree;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tree == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        AffineTransform previousTransform = g2.getTransform();

        g2.setColor(Color.BLACK);
        g2.scale(SCALE, SCALE);
        g2.translate(1, 1);
        g2.setStroke(new BasicStroke(1f / SCALE));
        drawTree(g2, tree);

        g2.setTransform(previousTransform);

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    private void drawTree(Graphics2D g2, Tree t) {

        // draw line to subTree1 and the tree itself
        if (t.subTree1 != null) {
            g2.setColor(!drawColouredSubtrees ? t.subTree1.isReal
                    ? Color.BLACK : Color.GRAY : Color.BLUE);
            g2.draw(new Line2D.Double(t.xCoordinate, t.yCoordinate,
                    t.subTree1.xCoordinate, t.subTree1.yCoordinate));
            drawTree(g2, t.subTree1);
        }

        // draw line to subTree2 and the tree itself
        if (t.subTree2 != null) {
            g2.setColor(!drawColouredSubtrees ? t.subTree2.isReal
                    ? Color.BLACK : Color.GRAY : Color.RED);
            g2.draw(new Line2D.Double(t.xCoordinate, t.yCoordinate,
                    t.subTree2.xCoordinate, t.subTree2.yCoordinate));
            drawTree(g2, t.subTree2);
        }

        // draw line to subTree3 and the tree itself
        if (t.subTree3 != null) {
            g2.setColor(!drawColouredSubtrees ? t.subTree3.isReal
                    ? Color.BLACK : Color.GRAY : Color.GREEN);
            g2.draw(new Line2D.Double(t.xCoordinate, t.yCoordinate,
                    t.subTree3.xCoordinate, t.subTree3.yCoordinate));
            drawTree(g2, t.subTree3);
        }

        // draw tree as a node
        Color color = t.isReal ? t.face == null ? Color.DARK_GRAY
                : Utils.averageVertexColor(t.face.vertices) : Color.LIGHT_GRAY;
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Float(t.xCoordinate, t.yCoordinate),
                10 / SCALE,
                new Point2D.Float(t.xCoordinate - 2.5f / SCALE, t.yCoordinate - 2.5f / SCALE),
                new float[]{0.0f, 0.4f, 1.0f},
                new Color[]{color.brighter(), color, color.darker()},
                CycleMethod.NO_CYCLE);
        g2.setPaint(gradient);
        g2.fill(new Ellipse2D.Double(
                t.xCoordinate - 5 / SCALE,
                t.yCoordinate - 5 / SCALE,
                10 / SCALE,
                10 / SCALE));

        g2.setColor(t.isReal ? Color.BLACK : Color.GRAY);
        g2.draw(new Ellipse2D.Double(
                t.xCoordinate - 5 / SCALE,
                t.yCoordinate - 5 / SCALE,
                10 / SCALE,
                10 / SCALE));
    }
}

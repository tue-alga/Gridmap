package gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import Utils.Utils;
import gui.listeners.NetworkChangedListener;
import model.Network;
import model.Network.Edge;
import model.Network.Vertex;
import model.subdivision.PlanarSubdivision;
import model.graph.PlanarStraightLineGraph;
import model.util.Vector2D;

/**
 * A NetworkPanel is a {@link JPanel} that can display a {@link Network}. The
 * vertices in the network can be dragged around and a force-based layouting
 * algorithm is used to layout the network continuously. Nodes can be added to
 * the network and delete from it, as can be done with edges.
 */
@SuppressWarnings("serial")
public class NetworkPanel extends JPanel implements MouseListener, MouseMotionListener {

    /**
     * Diameter of the drawn vertices.
     */
    public static final int VERTEX_SIZE = 20;
    /**
     * Diameter of the drawn vertices of the dual network.
     */
    public static final int VERTEX_SIZE_DUAL = 10;
    /**
     * Diameter of the "glow" around selected/hovered/dragged vertices.
     */
    public static final int VERTEX_SIZE_GLOW = VERTEX_SIZE + 10;
    /**
     * Network that is displayed.
     */
    private final Network network;
    /**
     * Planar subdivision induced by the network.
     */
    private PlanarSubdivision subdivision;
    /**
     * Dual of {@link #network}.
     */
    private PlanarStraightLineGraph dualNetwork;
    /**
     * The hovered vertex. Can be {@code null} if no vertex is hovered.
     */
    private Vertex hovered;
    /**
     * The selected vertex. Can be {@code null} if no vertex is selected.
     */
    private Vertex selected;
    /**
     * The dragged vertex. Can be {@code null} if no vertex is dragged.
     */
    private Vertex dragged;
    /**
     * X-coordinate of current mouse position.
     */
    private int mouseX;
    /**
     * Y-coordinate of current mouse position.
     */
    private int mouseY;
    /**
     * List of listeners that should be fired when the dual changes.
     */
    private ArrayList<NetworkChangedListener> dualChangedListeners = new ArrayList<>();
    /**
     * If the dual of the graph should be shown or not.
     */
    public boolean drawDual = false;
    /**
     * If the ID of faces should be shown or not. Can be useful for debugging
     * purposes.
     */
    public boolean drawFaceIDs = false;
    /**
     * Padding of background behind face IDs.
     */
    public static final double FACE_ID_PADDING = 2;
    /**
     * The timer that manages the layout.
     */
    Timer layoutTimer = new Timer(10, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            double totalKineticEnergy = network.doLayoutStep(50, dragged, getWidth(), getHeight());
            if (totalKineticEnergy < 0.1f) {
                layoutTimer.stop();
            }
            repaint();
        }
    });

    /**
     * Construct a new NetworkPanel that displays a network with a single vertex
     * in it.
     */
    public NetworkPanel() {
        this.network = new Network(1);
        network.getVertex(0).getPosition().setX(getWidth() / 2);
        network.getVertex(0).getPosition().setY(getHeight() / 2);
        computeInternalData();

        setBorder(BorderFactory.createLoweredBevelBorder());

        setOpaque(true);

        addMouseListener(this);
        addMouseMotionListener(this);
        setKeyBindings();

        layoutTimer.start();
    }

    public Network getNetwork() {
        return network;
    }

    public PlanarStraightLineGraph getDualNetwork() {
        return dualNetwork;
    }

    public PlanarSubdivision getPlanarSubdivision() {
        return subdivision;
    }

    /**
     * Registers the given listener for notifications of changes in the dual.
     *
     * @param listener Listener to be registered.
     */
    public void addDualChangedListener(NetworkChangedListener listener) {
        if (listener != null) {
            dualChangedListeners.add(listener);
            listener.networkChanged();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (network == null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            g2.setFont(g2.getFont().deriveFont(48f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(0, 0, 0, 64));
            String text = "No network yet...";
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent() / 2) / 2);
            return;
        }

        if (selected != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2.setStroke(new BasicStroke(4f));
            if (hovered != null && hovered != selected) {
                if (network.hasEdge(selected, hovered)) {
                    g2.setColor(new Color(210, 40, 20));
                } else {
                    g2.setColor(new Color(20, 210, 40));
                }
                double fromX = xWorldToScreen(selected.getPosition().getX());
                double fromY = yWorldToScreen(selected.getPosition().getY());
                double toX = xWorldToScreen(hovered.getPosition().getX());
                double toY = yWorldToScreen(hovered.getPosition().getY());
                g2.draw(new Line2D.Double(fromX, fromY, toX, toY));
            } else {
                g2.setColor(new Color(240, 210, 150));
                double fromX = xWorldToScreen(selected.getPosition().getX());
                double fromY = yWorldToScreen(selected.getPosition().getY());
                g2.draw(new Line2D.Double(fromX, fromY, mouseX, mouseY));
            }
            g2.setStroke(new BasicStroke(2f));
        }
        drawGraph(g2);
        if (drawDual && dualNetwork != null) {
            drawDualGraph(g2);
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    /**
     * Draws the given graph to an image that is saved.
     *
     * @param filename The file name of the image to write to.
     * @param g The graph to draw.
     */
    public void toImage(String filename) {
        BufferedImage i = new BufferedImage(800, 600, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g2 = (Graphics2D) i.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 800, 600);
        drawGraph(g2);

        File file = new File(filename);

        try {
            ImageIO.write(i, "png", file);
        } catch (IOException e) {
        }
    }

    /**
     * Draws the given graph onto a graphics object.
     *
     * @param g2 The graphics object to draw with. This may, for example, be a
     * graphics object of a GUI container, or a graphics object from an image.
     * @param g The graph to draw.
     */
    private void drawGraph(Graphics2D g2) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        synchronized (network) {

            // draw the "glow" around the hovered node
            if (hovered != null && hovered != selected) {
                if (selected != null) {
                    if (network.hasEdge(selected, hovered)) {
                        g2.setColor(new Color(210, 40, 20));
                    } else {
                        g2.setColor(new Color(20, 210, 40));
                    }
                } else {
                    g2.setColor(new Color(100, 140, 255));
                }
                double fromX = xWorldToScreen(hovered.getPosition().getX());
                double fromY = yWorldToScreen(hovered.getPosition().getY());
                g2.fill(new Ellipse2D.Double(fromX - VERTEX_SIZE_GLOW / 2,
                        fromY - VERTEX_SIZE_GLOW / 2, VERTEX_SIZE_GLOW,
                        VERTEX_SIZE_GLOW));
            }

            // draw the "glow" around the selected node
            if (selected != null) {
                if (hovered != null && hovered != selected) {
                    if (network.hasEdge(selected, hovered)) {
                        g2.setColor(new Color(210, 40, 20));
                    } else {
                        g2.setColor(new Color(20, 210, 40));
                    }
                } else {
                    g2.setColor(new Color(240, 210, 150));
                }
                double fromX = xWorldToScreen(selected.getPosition().getX());
                double fromY = yWorldToScreen(selected.getPosition().getY());
                g2.fill(new Ellipse2D.Double(fromX - VERTEX_SIZE_GLOW / 2,
                        fromY - VERTEX_SIZE_GLOW / 2, VERTEX_SIZE_GLOW,
                        VERTEX_SIZE_GLOW));
            }

            // draw the edges
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            for (Edge e : network.edges()) {
                Vertex source = e.getSource();
                Vertex target = e.getTarget();
                double fromX = xWorldToScreen(source.getPosition().getX());
                double fromY = yWorldToScreen(source.getPosition().getY());
                double toX = xWorldToScreen(target.getPosition().getX());
                double toY = yWorldToScreen(target.getPosition().getY());
                g2.draw(new Line2D.Double(fromX, fromY, toX, toY));
            }

            // draw the vertices
            g2.setStroke(new BasicStroke(2f));
            for (Vertex v : network.vertices()) {

                double vx = xWorldToScreen(v.getPosition().getX());
                double vy = yWorldToScreen(v.getPosition().getY());
                RadialGradientPaint gradient = new RadialGradientPaint(
                        new Point2D.Double(vx, vy),
                        VERTEX_SIZE,
                        new Point2D.Double(vx - 5, vy - 5),
                        new float[]{0.0f, 0.4f, 1.0f},
                        new Color[]{v.getColor().brighter(), v.getColor(), v.getColor().darker()},
                        CycleMethod.NO_CYCLE);

                g2.setPaint(gradient);
                g2.fill(new Ellipse2D.Double(vx - VERTEX_SIZE / 2,
                        vy - VERTEX_SIZE / 2, VERTEX_SIZE, VERTEX_SIZE));

                g2.setColor(Color.BLACK);
                g2.draw(new Ellipse2D.Double(vx - VERTEX_SIZE / 2,
                        vy - VERTEX_SIZE / 2, VERTEX_SIZE, VERTEX_SIZE));
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    /**
     * Draws the given tree onto a graphics object, where all nodes of the tree
     * are positioned at the center of the face they lie in.
     *
     * @param g2 The graphics object to draw in.
     * @param dualNetwork The tree to draw.
     */
    private void drawDualGraph(Graphics2D g2) {
        updateDualNetworkPositions();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Draw edges
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        for (PlanarStraightLineGraph.Edge e : dualNetwork.edges()) {
            double fromX = xWorldToScreen(e.getSource().getPosition().getX());
            double fromY = yWorldToScreen(e.getSource().getPosition().getY());
            double toX = xWorldToScreen(e.getTarget().getPosition().getX());
            double toY = yWorldToScreen(e.getTarget().getPosition().getY());
            g2.draw(new Line2D.Double(fromX, fromY, toX, toY));
        }

        // Draw vertices
        int numVertices = dualNetwork.numberOfVertices();
        for (int i = 0; i < numVertices; i++) {
            PlanarStraightLineGraph.Vertex dualVertex = dualNetwork.getVertex(i);
            PlanarSubdivision.Face face = subdivision.getFace(i);
            Vector2D pos = dualVertex.getPosition();
            // draw just the ID if requested, full node otherwise
            if (drawFaceIDs) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String str = Integer.toString(face.getId());

                g2.setColor(Color.WHITE);
                Rectangle2D rect = fm.getStringBounds(str, g2);
                double px = xWorldToScreen(pos.getX());
                double py = yWorldToScreen(pos.getY());
                g2.fill(new Rectangle2D.Double(rect.getMinX() + px
                        - fm.stringWidth(str) / 2f - FACE_ID_PADDING,
                        rect.getMinY() + py + fm.getAscent() / 2f - FACE_ID_PADDING,
                        rect.getWidth() + 2 * FACE_ID_PADDING,
                        rect.getHeight() + 2 * FACE_ID_PADDING));

                g2.setColor(Color.RED);
                g2.drawString(str, (float) px - fm.stringWidth(str) / 2f, (float) py + fm.getAscent() / 2f);
            } else {
                int numBoundaryVertices = face.getBoundaryVertices().size();
                ArrayList<Vertex> originalVertices = new ArrayList<>(numBoundaryVertices);
                for (PlanarSubdivision.Vertex v : face.getBoundaryVertices()) {
                    originalVertices.add(network.getVertex(v.getId()));
                }
                Color color = Utils.averageVertexColor(originalVertices);

                double px = xWorldToScreen(pos.getX());
                double py = yWorldToScreen(pos.getY());
                RadialGradientPaint gradient = new RadialGradientPaint(
                        new Point2D.Double(px, py),
                        VERTEX_SIZE_DUAL,
                        new Point2D.Double(px - 5, py - 5),
                        new float[]{0.0f, 0.4f, 1.0f},
                        new Color[]{color.brighter(), color, color.darker()},
                        CycleMethod.NO_CYCLE);

                g2.setPaint(gradient);
                g2.fill(new Ellipse2D.Double(px - VERTEX_SIZE_DUAL / 2,
                        py - VERTEX_SIZE_DUAL / 2, VERTEX_SIZE_DUAL, VERTEX_SIZE_DUAL));

                g2.setColor(Color.BLACK);
                g2.draw(new Ellipse2D.Double(px - VERTEX_SIZE_DUAL / 2,
                        py - VERTEX_SIZE_DUAL / 2, VERTEX_SIZE_DUAL, VERTEX_SIZE_DUAL));
            }
        }
    }

    /**
     * Starts the layout procedure if it is not already running. The layout
     * automatically stops when the algorithm is finished. This algorithm should
     * be called when the network to be displayed is changed.
     */
    public void triggerLayout() {
        if (!layoutTimer.isRunning()) {
            layoutTimer.start();
        }
    }

    // methods for the mouse handlers
    private Vertex getVertexOnPoint(int x, int y) {

        if (network == null) {
            return null;
        }

        for (Vertex v : network.vertices()) {
            double px = xWorldToScreen(v.getPosition().getX());
            double py = yWorldToScreen(v.getPosition().getY());
            if ((x - px) * (x - px) + (y - py) * (y - py) <= (VERTEX_SIZE / 2) * (VERTEX_SIZE / 2)) {
                return v;
            }
        }

        return null;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        hovered = getVertexOnPoint(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        selected = null;

        if (dragged != null) {
            dragged.getPosition().setX(xScreenToWorld(e.getX()));
            dragged.getPosition().setY(yScreenToWorld(e.getY()));
        }

        triggerLayout();

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (selected == null) {
            selected = getVertexOnPoint(e.getX(), e.getY());
        } else {
            Vertex clicked = getVertexOnPoint(e.getX(), e.getY());
            if (clicked != selected) {
                if (clicked == null) {
                    double vx = xScreenToWorld(e.getX());
                    double vy = yScreenToWorld(e.getY());
                    clicked = network.addVertex(new Vector2D(vx, vy));
                }
                Edge edge = network.getEdge(selected, clicked);
                if (edge != null) {
                    network.removeEdge(edge);
                } else {
                    network.addEdge(selected, clicked);
                }
                selected = clicked;

                computeInternalData();
                for (NetworkChangedListener dcl : dualChangedListeners) {
                    dcl.networkChanged();
                }

                triggerLayout();
            } else {
                selected = null;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragged = getVertexOnPoint(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragged = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // ignored
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // ignored
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 450);
    }

    /**
     * Initialize some key bindings for this panel.
     */
    private void setKeyBindings() {
        ActionMap actionMap = getActionMap();
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        String vkDelete = "VK_DELETE";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), vkDelete);

        actionMap.put(vkDelete, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null && dragged == null
                        && network.numberOfVertices() > 1) {
                    network.removeVertex(selected);
                    computeInternalData();
                    for (NetworkChangedListener dcl : dualChangedListeners) {
                        dcl.networkChanged();
                    }
                    if (hovered == selected) {
                        hovered = null;
                    }
                    selected = null;
                    triggerLayout();
                }
            }
        });
    }

    /**
     * Update internal data after a change in the network.
     */
    private void computeInternalData() {
        subdivision = new PlanarSubdivision(network);
        dualNetwork = new PlanarStraightLineGraph();
        subdivision.computeWeakDual(dualNetwork);
    }

    /**
     * Update the position of the vertices in the dual network after a layout
     * step has been executed.
     */
    private void updateDualNetworkPositions() {
        int numVertices = dualNetwork.numberOfVertices();
        for (int i = 0; i < numVertices; i++) {
            PlanarStraightLineGraph.Vertex v = dualNetwork.getVertex(i);
            PlanarSubdivision.Face f = subdivision.getFace(i);
            ArrayList<Vertex> originalVertices = new ArrayList<>(f.getBoundaryVertices().size());
            for (PlanarSubdivision.Vertex subdivisionVertex : f.getBoundaryVertices()) {
                originalVertices.add(network.getVertex(subdivisionVertex.getId()));
            }
            Vector2D position = Utils.meanPosition(originalVertices);
            v.setPosition(position);
        }
    }

    private double xScreenToWorld(int x) {
        return x;
    }

    private double yScreenToWorld(int y) {
        return getHeight() - y;
    }

    private double xWorldToScreen(double x) {
        return x;
    }

    private double yWorldToScreen(double y) {
        return getHeight() - y;
    }
}

package gui.panels;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * A class to simplify the creation of panels that allow for zooming and
 * panning. Coordinate conversion can be done in two ways:
 * <p>1. Using the functions xToScreen(double) and yToScreen(double) to convert
 * every coordinate to be drawn. This also implies that things like (but not
 * limited to) line thickness and string font are independent of zooming.</p>
 * <p>2. Applying the affine transform returned by getTransform() to the
 * Graphics2D object. The resulting code is shorter, but line thickness varies
 * with different zoom levels. If this is undesirable, the first method should
 * be used. Also, this cannot be used with strings, otherwise they will be drawn
 * upside down.
 *
 * @author Rafael Cano <rgcano at gmail.com>
 */
public class InteractivePanel extends JPanel {

    private int panX = 0;
    private int panY = 0;
    private double zoomFactor = 1;
    private double zoomMultiplier = 1.1;
    private boolean zoomingEnabled = true;
    private boolean panningEnabled = true;
    private AffineTransform transform = new AffineTransform();
    private AffineTransform inverseTransform = new AffineTransform();
    private HorizontalStatusBar statusBar = null;
    private MessagePosition coordinatesPosition = MessagePosition.RIGHT;
    private static final int MARGIN = 20;

    /**
     * Creates a new InteractivePanel with a double buffer and a flow layout.
     */
    public InteractivePanel() {
        super();
        initialize();
    }

    /**
     * Creates a new InteractivePanel with FlowLayout and the specified
     * buffering strategy.
     */
    public InteractivePanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        initialize();
    }

    /**
     * Creates a new buffered InteractivePanel with the specified layout
     * manager.
     */
    public InteractivePanel(LayoutManager layout) {
        super(layout);
        initialize();
    }

    /**
     * Creates a new InteractivePanel with the specified layout manager and
     * buffering strategy.
     */
    public InteractivePanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        initialize();
    }

    /**
     * Returns the x-coordinate of the current pan offset of this panel.
     */
    public int getPanX() {
        return panX;
    }

    /**
     * Returns the y-coordinate of the current pan offset of this panel.
     */
    public int getPanY() {
        return panY;
    }

    /**
     * Sets the pan offset for this panel and repaints.
     */
    public void setPan(int panX, int panY) {
        this.panX = panX;
        this.panY = panY;
        updateTransforms();
    }

    /**
     * Returns the current zoom factor of this panel.
     */
    public final double getZoomFactor() {
        return zoomFactor;
    }

    /**
     * Sets the zoom level of this panel and repaints.
     */
    public final void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        updateTransforms();
    }

    /**
     * Returns the multiplier applied to the zoom factor when the mouse wheel is
     * turned.
     */
    public final double getZoomMultiplier() {
        return zoomMultiplier;
    }

    /**
     * Sets the multiplier applied to the zoom factor when the mouse wheel is
     * turned.
     */
    public final void setZoomMultiplier(double zoomMultiplier) {
        this.zoomMultiplier = zoomMultiplier;
    }

    /**
     * Sets the zooming control switch for this panel.
     */
    public final void setZoomingEnabled(boolean zoomingEnabled) {
        this.zoomingEnabled = zoomingEnabled;
    }

    /**
     * Returns true is zooming is enabled for this panel, false otherwise.
     */
    public final boolean zoomingEnabled() {
        return zoomingEnabled;
    }

    /**
     * Sets the panning control switch for this panel..
     */
    public final void setPanningEnabled(boolean panningEnabled) {
        this.panningEnabled = panningEnabled;
    }

    /**
     * Returns true if panning is enabled for this panel, false otherwise.
     */
    public final boolean panningEnabled() {
        return panningEnabled;
    }

    /**
     * Returns the current status bar.
     */
    public HorizontalStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Sets the status bar for this panel.
     */
    public void setStatusBar(HorizontalStatusBar statusBar) {
        this.statusBar = statusBar;
    }

    /**
     * Returns the position where the coordinates are being displayed in the
     * status bar.
     */
    public MessagePosition getCoordinatesDisplayPosition() {
        return coordinatesPosition;
    }

    /**
     * Sets the position where the coordinates are being displayed in the status
     * bar.
     */
    public void setCoordinateDisplayPosition(MessagePosition coordinatesPosition) {
        this.coordinatesPosition = coordinatesPosition;
    }

    /**
     * Sets the zoom and pan attributes so that the rectangle defined by the
     * given (user) coordinates is centralized on the screen.
     */
    public final void changeFocus(double minX, double minY, double maxX, double maxY) {
        int panelWidth;
        int panelHeight;
        do {
            panelWidth = getWidth();
            panelHeight = getHeight();
        } while (panelWidth == 0 || panelHeight == 0);
        double width = maxX - minX;
        double height = maxY - minY;
        int desiredWidth = panelWidth - 2 * MARGIN;
        int desiredHeight = panelHeight - 2 * MARGIN;
        double horizontalFactor = desiredWidth / width;
        double verticalFactor = desiredHeight / height;
        zoomFactor = Math.min(horizontalFactor, verticalFactor);
        double actualWidth = zoomFactor * width;
        double actualHeight = zoomFactor * height;
        panX = (int) Math.round((panelWidth - actualWidth) / 2 - zoomFactor * minX);
        panY = (int) Math.round(zoomFactor * minY - (panelHeight - actualHeight) / 2);
        updateTransforms();
        repaint();
    }

    /**
     * Exports the elements on this panel to a PNG image file.
     */
    public final void toPNGImage(String fileName) {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paint(g);
        g.dispose();
        try {
            ImageIO.write(bi, "png", new File(fileName));
        } catch (Exception e) {
        }
    }

    /**
     * Returns the affine transform used to convert coordinates given by the
     * user to screen coordinates.
     */
    protected final AffineTransform getTransform() {
        return transform;
    }

    /**
     * Returns the affine transform used to convert from screen coordinates to
     * the user's coordinates.
     */
    protected final AffineTransform getInverseTransform() {
        return inverseTransform;
    }

    /**
     * Converts an x-coordinate from the screen's to the user's coordinate
     * system.
     */
    protected final double xToUser(int x) {
        return x * inverseTransform.getScaleX() + inverseTransform.getTranslateX();
    }

    /**
     * Converts a y-coordinate from the screen's to the user's coordinate
     * system.
     */
    protected final double yToUser(int y) {
        return y * inverseTransform.getScaleY() + inverseTransform.getTranslateY();
    }

    /**
     * Converts an x-coordinate from the user's to the screen's coordinate
     * system.
     */
    protected final int xToScreen(double x) {
        return (int) Math.round(x * transform.getScaleX() + transform.getTranslateX());
    }

    /**
     * Converts a y-coordinate from the user's to the screen's coordinate
     * system.
     */
    protected final int yToScreen(double y) {
        return (int) Math.round(y * transform.getScaleY() + transform.getTranslateY());
    }

    /**
     * Sets the left status message if statusBar is not null.
     */
    protected void setLeftStatusMessage(String message) {
        if (statusBar != null) {
            statusBar.setLeftMessage(message);
        }
    }

    /**
     * Sets the center status message if statusBar is not null.
     */
    protected void setCenterStatusMessage(String message) {
        if (statusBar != null) {
            statusBar.setCenterMessage(message);
        }
    }

    /**
     * Sets the right status message if statusBar is not null.
     */
    protected void setRightStatusMessage(String message) {
        if (statusBar != null) {
            statusBar.setRightMessage(message);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateTransforms();
    }

    private void initialize() {
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addMouseWheelListener(listener);

//        JLabel label = new JLabel();
//        label.setBackground(Color.RED);
//        label.setOpaque(true);
//        label.setBorder(BorderFactory.createLoweredBevelBorder());
//        label.setText("uhauahuhauha");
//        this.setLayout(new FlowLayout(FlowLayout.LEFT));
//        this.add(label);
    }

    private void updateTransforms() {
        transform.setTransform(zoomFactor, 0, 0, -zoomFactor, panX, getHeight() + panY);
        try {
            inverseTransform = transform.createInverse();
        } catch (NoninvertibleTransformException ex) {
        }
    }

    public enum MessagePosition {

        NONE, LEFT, CENTER, RIGHT;
    }

    private class Listener implements MouseListener, MouseMotionListener, MouseWheelListener {

        private int clickX = 0;
        private int clickY = 0;

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            clickX = e.getX();
            clickY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (panningEnabled) {
                int currentX = e.getX();
                int currentY = e.getY();
                panX += currentX - clickX;
                panY += currentY - clickY;
                clickX = currentX;
                clickY = currentY;
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            double xWorld = xToUser(e.getX());
            double yWorld = yToUser(e.getY());
            switch (coordinatesPosition) {
                case LEFT:
                    setLeftStatusMessage(String.format("(%.2f, %.2f)", xWorld, yWorld));
                    break;
                case CENTER:
                    setCenterStatusMessage(String.format("(%.2f, %.2f)", xWorld, yWorld));
                    break;
                case RIGHT:
                    setRightStatusMessage(String.format("(%.2f, %.2f)", xWorld, yWorld));
                    break;
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (zoomingEnabled) {
                int rotation = e.getWheelRotation();
                double oldScale = zoomFactor;
                if (rotation > 0) {
                    zoomFactor /= zoomMultiplier;
                } else {
                    zoomFactor *= zoomMultiplier;
                }
                panX += (int) Math.round(xToUser(e.getX()) * (oldScale - zoomFactor));
                panY += (int) Math.round(yToUser(e.getY()) * (zoomFactor - oldScale));
                repaint();
            }
        }
    }
}

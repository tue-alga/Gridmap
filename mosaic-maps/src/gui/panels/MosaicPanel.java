package gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import Utils.Utils;
import model.Cartogram.MosaicCartogram;
import model.Cartogram.MosaicCartogram.MosaicRegion;
import model.Cartogram.MosaicCartogram.Cell;
import model.Cartogram.MosaicCartogram.Coordinate;
import model.Network;
import model.subdivision.Map;
import model.util.Vector2D;

/**
 * A MosaicPanel is a panel that can show mosaics on a grid.
 */
public class MosaicPanel extends InteractivePanel {

    private MosaicCartogram cartogram;
    private MosaicRegion selected = null;
    private boolean useTransparency = false;
    private static final int TRANSPARENCY = 80;

    public MosaicPanel() {
        this(null);
    }

    public MosaicPanel(MosaicCartogram mosaic) {
        super();
        if (mosaic != null) {
            this.cartogram = mosaic.duplicate();
        } else {
            this.cartogram = null;
        }

        setBorder(BorderFactory.createLoweredBevelBorder());
        setOpaque(true);

        //setBackground(new Color(154, 181, 219));
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        this.addKeyListener(listener);
    }

    public void setCartogram(MosaicCartogram mosaic, boolean centralize) {
        this.cartogram = mosaic.duplicate();
        if (centralize) {
            centralizeView();
        }
        repaint();
    }

    public void setCartogram(MosaicCartogram grid) {
        setCartogram(grid, true);
    }

    public MosaicCartogram getCartogram() {
        return cartogram;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (cartogram == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (Cell cell : cartogram.cells()) {
            drawCell(g2, cell);
        }

        if (useTransparency) {
            for (MosaicRegion region : cartogram.regions()) {
                drawGuidingShapeOutline(g2, region);
            }
        } else {
            for (MosaicRegion region : cartogram.regions()) {
                drawRegionOutline(g2, region);
            }
        }

        if (selected != null) {
            paintHighlightedCells(g2);
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    private void drawCell(Graphics2D g2, Cell cell) {

        if (cell.getVertex() == null) {
            System.out.println("Error: cell with null vertex");
            return;
        }

        Point2D hCenter = cell.getCenter();
        Point2D[] points = cell.getBoundaryPoints();
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < points.length; i++) {
            Point2D p = points[i];
            if (i == 0) {
                path.moveTo(xToScreen(p.getX()), yToScreen(p.getY()));
            } else {
                path.lineTo(xToScreen(p.getX()), yToScreen(p.getY()));
            }
        }
        path.closePath();

        Color c1, c2, c3;
        if (useTransparency) {
            c1 = createTransparentColor(cell.getVertex().getColor().brighter());
            c2 = createTransparentColor(cell.getVertex().getColor());
            c3 = createTransparentColor(cell.getVertex().getColor().darker());
        } else {
            c1 = cell.getVertex().getColor().brighter();
            c2 = cell.getVertex().getColor();
            c3 = cell.getVertex().getColor().darker();
        }

        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Double(xToScreen(hCenter.getX()), yToScreen(hCenter.getY())),
                (float) (cartogram.getCellSide() * getZoomFactor()),
                new Point2D.Double(xToScreen(hCenter.getX() - 5), yToScreen(hCenter.getY() + 5)),
                new float[]{0.0f, 0.4f, 1.0f},
                new Color[]{c1, c2, c3},
                CycleMethod.NO_CYCLE);

        //g2.setPaint(h.getVertex().getColor());
        g2.setPaint(gradient);
        g2.fill(path);
        g2.setStroke(new BasicStroke(0.2f));
        if (useTransparency) {
            g2.setColor(createTransparentColor(Color.BLACK));
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.draw(path);
    }

    private Color createTransparentColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return new Color(r, g, b, TRANSPARENCY);
    }

    private void drawRegionOutline(Graphics2D g2, MosaicRegion region) {
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(Color.BLACK);
        ArrayList<Point2D> outline = region.computeOutlinePoints();
        Path2D path = new Path2D.Double();
        Point2D first = outline.get(0);
        path.moveTo(xToScreen(first.getX()), yToScreen(first.getY()));
        for (int i = 1; i < outline.size(); i++) {
            Point2D point = outline.get(i);
            path.lineTo(xToScreen(point.getX()), yToScreen(point.getY()));
        }
        path.closePath();
        g2.draw(path);
    }

    private void drawGuidingShapeOutline(Graphics2D g2, MosaicRegion region) {
        g2.setStroke(new BasicStroke(3.5f));
        g2.setColor(region.getVertex().getColor().darker());
        ArrayList<Point2D> outline = region.getGuidingShape().computeOutlinePoints();
        Path2D path = new Path2D.Double();
        Point2D first = outline.get(0);
        path.moveTo(xToScreen(first.getX()), yToScreen(first.getY()));
        for (int i = 1; i < outline.size(); i++) {
            Point2D point = outline.get(i);
            path.lineTo(xToScreen(point.getX()), yToScreen(point.getY()));
        }
        path.closePath();
        g2.draw(path);
    }

    private void paintHighlightedCells(Graphics2D g2) {
        for (Coordinate c : selected.getGuidingShape()) {
            Cell cell = cartogram.getCell(c);
            Point2D[] points = cell.getBoundaryPoints();
            Path2D.Double path = new Path2D.Double();
            for (int i = 0; i < points.length; i++) {
                Point2D p = points[i];
                if (i == 0) {
                    path.moveTo(xToScreen(p.getX()), yToScreen(p.getY()));
                } else {
                    path.lineTo(xToScreen(p.getX()), yToScreen(p.getY()));
                }
            }
            path.closePath();

            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.MAGENTA);
            g2.draw(path);
        }
    }

    private void centralizeView() {
        if (cartogram != null) {
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            // Find center of the drawing
            for (Cell cell : cartogram.cells()) {
                Point2D p = cell.getCenter();
                if (p.getX() < minX) {
                    minX = p.getX();
                }
                if (p.getY() < minY) {
                    minY = p.getY();
                }
                if (p.getX() > maxX) {
                    maxX = p.getX();
                }
                if (p.getY() > maxY) {
                    maxY = p.getY();
                }
            }
            Vector2D center = new Vector2D((maxX + minX) / 2, (maxY + minY) / 2);
            // Set zoom level to the expected numberOfCells of the final cartogram
            int finalSize = 0;
            double mapArea = 0.0;
            Map.Vertex leftmost = Utils.leftmost(cartogram.getMap().vertices());
            Map.Vertex rightmost = Utils.rightmost(cartogram.getMap().vertices());
            Map.Vertex topmost = Utils.topmost(cartogram.getMap().vertices());
            Map.Vertex bottommost = Utils.bottommost(cartogram.getMap().vertices());
            double width = rightmost.getPosition().getX() - leftmost.getPosition().getX();
            double height = topmost.getPosition().getY() - bottommost.getPosition().getY();
            for (MosaicRegion region : cartogram.regions()) {
                finalSize += region.getGuidingShape().size();
                mapArea += region.getMapFace().getArea();
            }
            // Compute the desired final area and try to predict the grid numberOfCells
            double cartogramArea = finalSize * cartogram.getCellArea();
            double factor = cartogramArea / mapArea;
            width = width * factor / 2;
            height = height * factor / 2;
            minX = Math.min(minX, center.getX() - width);
            minY = Math.min(minY, center.getY() - height);
            maxX = Math.max(maxX, center.getX() + width);
            maxY = Math.max(maxY, center.getY() + height);
            changeFocus(minX, minY, maxX, maxY);
            repaint();
        }
    }

    private class Listener implements MouseListener, MouseMotionListener, KeyListener {

        private Coordinate currentCoordinate = null;

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isControlDown()) {
                setPanningEnabled(false);
                if (selected != null) {
                    double xWorld = xToUser(e.getX());
                    double yWorld = yToUser(e.getY());
                    currentCoordinate = cartogram.getContainingCell(xWorld, yWorld);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            currentCoordinate = null;
            setPanningEnabled(true);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!e.isControlDown()) {
                currentCoordinate = null;
            }
            double xWorld = xToUser(e.getX());
            double yWorld = yToUser(e.getY());
            if (cartogram != null) {
                Coordinate c = cartogram.getContainingCell(xWorld, yWorld);
                if (currentCoordinate != null) {
                    if (selected != null && !c.equals(currentCoordinate)) {
                        selected.translateGuidingShape(c.minus(currentCoordinate));
                        currentCoordinate = c;
                        repaint();
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!e.isControlDown()) {
                currentCoordinate = null;
            }
            double xWorld = xToUser(e.getX());
            double yWorld = yToUser(e.getY());
            if (cartogram != null) {
                Coordinate c = cartogram.getContainingCell(xWorld, yWorld);
                setCenterStatusMessage(c.toString());
                Network.Vertex v = cartogram.getVertex(c);
                if (v == null) {
                    if (selected != null) {
                        selected = null;
                        repaint();
                    }
                } else {
                    MosaicRegion newSelected = cartogram.getRegion(v.getId());
                    if (newSelected != selected) {
                        selected = newSelected;
                        repaint();
                    }
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//                MosaicHeuristic heuristic = new MosaicHeuristic(mosaic.getMap(), mosaic.getDual(), mosaic);
//                heuristic.execute(MosaicPanel.this);
            } else if (e.getKeyCode() == KeyEvent.VK_T) {
                useTransparency = !useTransparency;
                repaint();
            }
        }
    }
}

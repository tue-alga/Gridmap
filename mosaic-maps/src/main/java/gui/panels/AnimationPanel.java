package gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.Timer;
import model.Cartogram.MosaicCartogram;
import model.Cartogram.MosaicCartogram.Cell;
import model.Cartogram.MosaicCartogram.MosaicRegion;
import model.Network;
import model.util.ElementList;
import model.util.Random;
import model.util.Vector2D;

/**
 *
 * @author Rafael Cano <rgcano at gmail.com>
 */
public class AnimationPanel extends InteractivePanel {

    private MosaicCartogram cartogram;
    ElementList<ArrayList<Tile>> tiles1;
    ArrayList<Tile> tiles2;
    Timer timer;
    Vector2D center;
    int state;
    MosaicRegion current;
    ElementList<Boolean> doneRegions;

    public AnimationPanel() {
        this(null);
    }

    public AnimationPanel(MosaicCartogram cartogram) {
        this.cartogram = cartogram;
    }

    public MosaicCartogram getCartogram() {
        return cartogram;
    }

    public void setCartogram(MosaicCartogram cartogram) {
        this.cartogram = cartogram;
        centralizeView();
    }

    public void animate() {
        tiles1 = new ElementList<>();
        tiles2 = new ArrayList<>();
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!step()) {
                    timer.stop();
                }
            }
        });
        center = getCenter();
        state = 0;
        for (int i = 0; i < cartogram.numberOfRegions(); i++) {
            tiles1.add(new ArrayList<Tile>());
        }

        for (Cell cell : cartogram.cells()) {
            Tile t = new Tile();
            t.cell = cell;
            t.actual = new Vector2D(center);
            t.translation = Vector2D.difference(center, cell.getCoordinate().toVector2D());
            double vx = Random.nextDouble() * 2 - 1;
            double vy = Random.nextDouble() * 2 - 1;
            t.velocity = new Vector2D(vx, vy);
            tiles1.get(cell.getVertex()).add(t);
            tiles2.add(t);
        }
        timer.start();
    }

    private boolean step() {
        // Run stuff!!
        if (state == 0) {
            double done = 0;
            for (Tile t : tiles2) {
                t.translation.add(t.velocity);
                t.actual.add(t.velocity);
                final int DMAX = 120;
                double d = Math.min(Vector2D.norm(Vector2D.difference(center, t.actual)), DMAX);
                t.velocity.multiply(1 - d * d / (DMAX * DMAX));
                if (t.velocity.norm() < 0.001) {
                    done += 1;
                }
            }
            done /= tiles2.size();
            if (done > 0.9) {
                for (Tile t : tiles2) {
                    t.velocity = new Vector2D(Vector2D.product(t.translation, -1)).normalize().multiply(1.5);
                }
                state = 1;
//                int maxSize = 0;
//                for (MosaicRegion region : cartogram.regions()) {
//                    if (region.size() > maxSize) {
//                        maxSize = region.size();
//                        current = region;
//                    }
//                }
//                doneRegions = new ElementList<>(cartogram.numberOfRegions(), false);
//                for (Tile t : tiles1.get(current)) {
//                    t.velocity = new Vector2D(Vector2D.product(t.translation, -1)).normalize().multiply(2);
//                }
            }
        } else if (state == 1) {
            int done = 0;
            for (Tile t : tiles2) {
                t.translation.add(t.velocity);
                t.actual.add(t.velocity);
                if (t.translation.norm() < 10) {
                    t.translation = new Vector2D(0, 0);
                    t.velocity = new Vector2D(0, 0);
                    done++;
                }
            }
            if (done == tiles2.size()) {
                state = 2;
            }
//            if (done == current.size()) {
//                doneRegions.set(current, Boolean.TRUE);
//                int count = 0;
//                for (int i = 0; i < doneRegions.size(); i++) {
//                    if (!doneRegions.get(i)) {
//                        count++;
//                    }
//                }
//                if (count == 0) {
//                    return false;
//                }
//                int maxSize = 0;
//                for (MosaicRegion region : cartogram.regions()) {
//                    if (canCurrent(region) && region.size() > maxSize) {
//                        maxSize = region.size();
//                        current = region;
//                    }
//                }
//                if (maxSize == 0) {
//                    for (MosaicRegion region : cartogram.regions()) {
//                    }
//                }
//                for (Tile t : tiles1.get(current)) {
//                    t.velocity = new Vector2D(Vector2D.product(t.translation, -1)).normalize().multiply(2);
//                }
//            }
        }
        repaint();
        return true;
    }

    private boolean canCurrent(MosaicRegion region) {
        if (doneRegions.get(region)) {
            return false;
        }
        Network.Vertex v = region.getVertex();
        Network dual = cartogram.getDual();
        for (int i = 0; i < dual.getDegree(v); i++) {
            Network.Vertex u = dual.getNeighbour(v, i);
            if (doneRegions.get(u)) {
                return true;
            }
        }
        return false;
    }

    private void centralizeView() {
        if (cartogram != null) {
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            // Find center of the drawing
            for (MosaicCartogram.Cell cell : cartogram.cells()) {
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
            changeFocus(minX, minY, maxX, maxY);
            repaint();
        }
    }

    private Vector2D getCenter() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        // Find center of the drawing
        for (MosaicCartogram.Cell cell : cartogram.cells()) {
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
        return new Vector2D((maxX + minX) / 2, (maxY + minY) / 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (cartogram == null || tiles2.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

//        for (MosaicCartogram.Cell cell : cartogram.cells()) {
//            drawCell(g2, cell);
//        }

        if (state == 2) {
            for (Tile t : tiles2) {
                drawCell(g2, t.cell, new Vector2D(0, 0));
            }
            for (MosaicCartogram.MosaicRegion region : cartogram.regions()) {
                drawRegionOutline(g2, region);
            }
        } else {
            for (Tile t : tiles2) {
                drawCell(g2, t.cell, t.translation);
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    private void drawCell(Graphics2D g2, MosaicCartogram.Cell cell, Vector2D t) {

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
                path.moveTo(xToScreen(p.getX() + t.getX()), yToScreen(p.getY() + t.getY()));
            } else {
                path.lineTo(xToScreen(p.getX() + t.getX()), yToScreen(p.getY() + t.getY()));
            }
        }
        path.closePath();

        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Double(xToScreen(hCenter.getX() + t.getX()), yToScreen(hCenter.getY() + t.getY())),
                (float) (cartogram.getCellSide() * getZoomFactor()),
                new Point2D.Double(xToScreen(hCenter.getX() + t.getX() - 5), yToScreen(hCenter.getY() + t.getY() + 5)),
                new float[]{0.0f, 0.4f, 1.0f},
                new Color[]{cell.getVertex().getColor().brighter(), cell.getVertex().getColor(), cell.getVertex().getColor().darker()},
                MultipleGradientPaint.CycleMethod.NO_CYCLE);

        //g2.setPaint(h.getVertex().getColor());
        g2.setPaint(gradient);
        g2.fill(path);
        g2.setStroke(new BasicStroke(0.2f));
        g2.setColor(Color.BLACK);
        g2.draw(path);
    }

    private void drawRegionOutline(Graphics2D g2, MosaicCartogram.MosaicRegion region) {
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

    private static class Tile {

        Cell cell;
        Vector2D actual;
        Vector2D translation;
        Vector2D velocity;
        double rotation;
        double rotationSpeed;
    }
}

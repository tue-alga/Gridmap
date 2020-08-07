package gui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.BorderFactory;
import Utils.Utils;
import model.subdivision.Label;
import model.subdivision.Map;
import model.subdivision.PlanarSubdivision;
import model.util.Vector2D;

/**
 *
 * @author Rafael Cano <rgcano at gmail.com>
 */
public class MapPanel extends InteractivePanel {

    private Map map = null;
    private boolean centralize = true;

    public MapPanel() {
        super();
        setBorder(BorderFactory.createLoweredBevelBorder());
        setOpaque(true);
    }

    public void setMap(Map map) {
        this.map = map;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (centralize) {
            centralizeView();
            centralize = false;
        }

        drawMap(g2);

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    private void drawMap(Graphics2D g2) {
        for (Map.Face f : map.boundedFaces()) {
            Path2D path = new Path2D.Double();
            List<? extends PlanarSubdivision.Vertex> boundary = f.getBoundaryVertices();
            for (int i = 0; i < boundary.size(); i++) {
                Vector2D pos = boundary.get(i).getPosition();
                if (i == 0) {
                    path.moveTo(xToScreen(pos.getX()), yToScreen(pos.getY()));
                } else {
                    path.lineTo(xToScreen(pos.getX()), yToScreen(pos.getY()));
                }
            }
            path.closePath();

            // Draw coloured face
            g2.setPaint(f.getColor());
            g2.fill(path);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            g2.draw(path);
        }

        // Draw labels
        for (Map.Face f : map.boundedFaces()) {
            Label l = f.getLabel();
            if (l != null) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.BLACK);
                String text = l.getText();
                Vector2D position = l.getPosition();
                float x = (float) xToScreen(position.getX()) - fm.stringWidth(text) / 2f;
                float y = (float) yToScreen(position.getY()) + fm.getAscent() / 2f;
//                float x = xWorldToScreen(position.getX());
//                float y = yWorldToScreen(position.getY());
                g2.drawString(text, x, y);
            }
        }
    }

    private void centralizeView() {
        if (map != null) {
            double minX = Utils.leftmost(map.vertices()).getPosition().getX();
            double maxX = Utils.rightmost(map.vertices()).getPosition().getX();
            double minY = Utils.bottommost(map.vertices()).getPosition().getY();
            double maxY = Utils.topmost(map.vertices()).getPosition().getY();
            changeFocus(minX, minY, maxX, maxY);
        }
    }
}

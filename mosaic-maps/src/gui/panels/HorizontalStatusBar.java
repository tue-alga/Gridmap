package gui.panels;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Rafael Cano <rgcano at gmail.com>
 */
public class HorizontalStatusBar extends JPanel {

    private JLabel left;
    private JLabel center;
    private JLabel right;

    public HorizontalStatusBar() {
        super();
        GridLayout layout = new GridLayout(1, 3);
        this.setLayout(layout);
        left = new JLabel();
        center = new JLabel();
        right = new JLabel();
        left.setText(" ");
        left.setHorizontalAlignment(JLabel.LEFT);
        center.setText(" ");
        center.setHorizontalAlignment(JLabel.CENTER);
        right.setText(" ");
        right.setHorizontalAlignment(JLabel.RIGHT);
        add(left);
        add(center);
        add(right);
    }
    
    public String getLeftMessage() {
        return left.getText();
    }

    public void setLeftMessage(String message) {
        left.setText(message);
        repaint();
    }

    public String getCenterMessage() {
        return center.getText();
    }

    public void setCenterMessage(String message) {
        center.setText(message);
        repaint();
    }

    public String getRightMessage() {
        return right.getText();
    }

    public void setRightMessage(String message) {
        right.setText(message);
        repaint();
    }
    
    public void resetMessages() {
        left.setText(" ");
        center.setText(" ");
        right.setText(" ");
    }
}

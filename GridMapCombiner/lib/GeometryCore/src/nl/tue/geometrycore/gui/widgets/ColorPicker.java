/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Button that colors with an objects color and allows for configuring the
 * objects color.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ColorPicker extends JButton {

    /**
     * Interface for an object that can be colored with the
     * {@link nl.tue.geometrycore.gui.widgets.ColorPicker} class.
     *
     */
    public static interface ColorableInterface {

        /**
         * Sets the colors of the object to the given color.
         *
         * @param color
         */
        void setColor(Color color);

        /**
         * Gets the current color of the object.
         *
         * @return
         */
        Color getColor();

        /**
         * Attaches a color picker to this object.
         *
         * @param picker
         */
        void addColorPicker(ColorPicker picker);

        /**
         * Detaches a color picker from this object.
         *
         * @param picker
         */
        void removeColorPicker(ColorPicker picker);
    }

    /**
     * Basic implementation of a
     * {@link nl.tue.geometrycore.gui.widgets.ColorPicker.ColorableInterface}.
     * It automatically keeps the attached color pickers in sync.
     */
    public static class ColorableObject implements ColorableInterface {

        private Color color = null;
        private final List<ColorPicker> pickers = new ArrayList();

        /**
         * Default constructor, where the color is set to null.
         */
        public ColorableObject() {
        }

        /**
         * Constructor to initialize to the given color.
         *
         * @param color
         */
        public ColorableObject(Color color) {
            this.color = color;
        }

        /**
         * Updates attached color pickers to the new color value of the object.
         */
        private void colorChanged() {
            for (ColorPicker picker : pickers) {
                picker.setFromObject();
            }
        }

        @Override
        public void setColor(Color color) {
            this.color = color;
            colorChanged();
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public void addColorPicker(ColorPicker picker) {
            pickers.add(picker);
        }

        @Override
        public void removeColorPicker(ColorPicker picker) {
            pickers.remove(picker);
        }
    }

    private final ColorableInterface coloredobject;
    private final ActionListener onComplete;

    /**
     * Creates a color picker for the given object. This automatically attaches
     * the color picker to the object.
     *
     * @param coloredobject
     */
    public ColorPicker(ColorableInterface coloredobject) {
        this(coloredobject, null);
    }

    /**
     * Creates a color picker for the given object. This automatically attaches
     * the color picker to the object. The given action is executed after the
     * object's color has been changed with this color picker.
     *
     * @param coloredobject
     * @param onComplete
     */
    public ColorPicker(ColorableInterface coloredobject, ActionListener onComplete) {
        super("");
        this.coloredobject = coloredobject;
        coloredobject.addColorPicker(this);
        this.addActionListener((ActionEvent e) -> {
            trigger();
        });
        this.onComplete = onComplete;
        setFromObject();
    }

    /**
     * Deactivates this color picker. It will no longer be kept in sync with the
     * object.
     */
    public void deactivate() {
        coloredobject.removeColorPicker(this);
    }

    /**
     * Re-activates this color picker. Its color is set from the object and will
     * be kept in sync with the object.
     */
    public void activate() {
        coloredobject.addColorPicker(this);
        setFromObject();
    }

    /**
     * Triggers the color picker interface to set the color of the associated
     * object.
     */
    public void trigger() {
        Color result = JColorChooser.showDialog(this, "Pick a color", coloredobject.getColor());
        if (result != null) {
            coloredobject.setColor(result);
            if (onComplete != null) {
                onComplete.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    /**
     * Sets the background color of this button based on the object's color.
     */
    private void setFromObject() {
        if (coloredobject.getColor() != null) {
            setBackground(coloredobject.getColor());
            repaint();
        }
    }
}

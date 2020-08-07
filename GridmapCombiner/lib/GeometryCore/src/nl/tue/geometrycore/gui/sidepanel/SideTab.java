/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

/**
 * A tab in the {@link TabbedSidePanel}. It is based on providing a vertical
 * layout of the provided components, with default sizing attributes. However,
 * horizontal splits and custom-sized components are also supported. The
 * addition of a scrollbar to the tab is done automatically.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SideTab {

    //<editor-fold defaultstate="collapsed" desc="PRIVATE STATIC">
    /**
     * Number of pixels needed for a vertical scrollbar. This is likely to be
     * very platform dependent. Currently, it is based on JRE 7 on Windows 8.
     */
    private static final int scrollNarrowing = 22;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private TabbedSidePanel _partof;
    String _name;
    JPanel _panel;
    private int _maxElementWidth;
    private int _y;
    private Stack<Addition> _additions;
    private LinkedList<SplitSpec> _splits;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">    
    SideTab(String name, TabbedSidePanel partof) {
        _partof = partof;

        _name = name;
        _panel = new JPanel();
        _panel.setLayout(null);
        _y = _partof._tabMargin;
        _maxElementWidth = _partof.getWidth() - 2 * _partof._tabMargin - scrollNarrowing;

        _additions = new Stack();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TAB METHODS">
    public void invalidate() {
        _panel.invalidate();
        _panel.repaint();
    }

    public void setEnabled(boolean enabled) {
        _partof.setTabEnabled(_name, enabled);
    }

    public void resizeTab() {
        int newEltWidth = _partof.getWidth() - 2 * _partof._tabMargin - scrollNarrowing;
        if (newEltWidth != _maxElementWidth) {
            _maxElementWidth = newEltWidth;
            for (Addition a : _additions) {
                a.place();
            }
            updatePreferredSize();
            invalidate();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GENERIC COMPONENT CONTROL">
    public void makeSplit(int space, int number) {
        double f = 1.0 / (double) number;
        double[] fs = new double[number];
        for (int i = 0; i < number; i++) {
            fs[i] = f;
        }
        makeCustomSplit(space, fs);
    }

    public void makeCustomSplit(int space, double... fractions) {
        this._splits = new LinkedList();

        double soFar = 0;

        for (int i = 0; i < fractions.length - 1; i++) {
            double f = fractions[i];
            _splits.add(new SplitSpec(i, fractions.length, space, soFar, f));
            soFar += f;
        }
        _splits.addLast(new SplitSpec(fractions.length - 1, fractions.length, space, soFar, 1 - soFar));
    }

    public void addComponent(JComponent comp, int height) {
        assert _splits == null;

        _panel.add(comp);
        Addition a = new StandardAddition(comp, _y, height);
        _additions.add(a);
        a.place();

        _y += height + _partof._elementSpacing;

        updatePreferredSize();
    }

    public void addComponent(JComponent comp) {
        if (comp != null) {
            _panel.add(comp);
        }
        Addition a;

        if (_splits != null) {
            a = new SplitAddition(comp, _y, _splits.removeFirst());
            if (_splits.isEmpty()) {
                _splits = null;
            }
        } else {
            a = new StandardAddition(comp, _y);
        }

        _additions.add(a);
        a.place();
        if (_splits == null) {
            _y += _partof._elementHeight + _partof._elementSpacing;
        }

        updatePreferredSize();
    }

    public void addComponent(JComponent comp, int eltWidth, int eltHeight) {

        assert _splits == null;

        _panel.add(comp);
        Addition a = new FixedAddition(comp, _y, eltWidth, eltHeight);
        a.place();
        _additions.add(a);

        _y += eltHeight + _partof._elementSpacing;

        updatePreferredSize();
    }

    public void addSpace() {
        addSpace(1);
    }

    public void addSpace(int factor) {
        _additions.add(new StandardAddition(null, _y));
        _y += factor * _partof._elementSpacing;
    }

    public void revertUntil(JComponent comp) {
        while (!_additions.isEmpty() && _additions.peek()._comp != comp) {
            revertLastAddition();
        }
    }

    public void revertLastAddition() {
        if (_additions.isEmpty()) {
            return;
        }

        Addition revert = _additions.pop();

        _y = revert._yCoord;
        if (revert._comp != null) {
            _panel.remove(revert._comp);
            invalidate();
        }

        if (revert instanceof SplitAddition) {
            SplitSpec spec = ((SplitAddition) revert)._split;
            if (_splits == null) {
                _splits = new LinkedList();
            }
            _splits.addFirst(spec);
            if (_splits.size() == spec._total) {
                _splits = null;
            }
        }

        updatePreferredSize();
    }

    public void clearTab() {
        while (!_additions.isEmpty()) {
            revertLastAddition();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="SPECIFIC COMPONENTS">
    public JSeparator addSeparator() {
        return addSeparator(0);
    }

    public JSeparator addSeparator(int spacefactor) {
        addSpace(spacefactor);

        JSeparator rule = new JSeparator(JSeparator.HORIZONTAL);
        addComponent(rule, _maxElementWidth, 2);

        addSpace(spacefactor);
        return rule;
    }

    public JLabel addLabel(String text) {
        JLabel label = new JLabel(text);
        addComponent(label);
        return label;
    }

    public JTextField addTextField(String text) {
        JTextField field = new JTextField(text);
        addComponent(field);
        return field;
    }

    public JButton addButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        addComponent(button);

        if (action != null) {
            button.addActionListener(action);
        }
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    public JCheckBox addCheckbox(String text, boolean checked, NewValueListener<ActionEvent,Boolean> action) {
        JCheckBox check = new JCheckBox(text, checked);
        addComponent(check);
        if (action != null) {
            check.addActionListener((e) -> {
                action.update(e, check.isSelected());
            });
        }
        return check;
    }

    public ButtonGroup addButtonGroup() {
        ButtonGroup bgroup = new ButtonGroup();
        return bgroup;
    }

    public JRadioButton addRadioButton(String text, boolean checked, ButtonGroup group, ActionListener action) {
        JRadioButton rbtn = new JRadioButton(text, checked);
        group.add(rbtn);
        addComponent(rbtn);
        if (action != null) {
            rbtn.addActionListener(action);
        }
        return rbtn;
    }

    public JSpinner addIntegerSpinner(int init, int min, int max, int step, NewValueListener<ChangeEvent, Integer> list) {
        JSpinner spin = new JSpinner(new SpinnerNumberModel(init, min, max, step));
        addComponent(spin);
        if (list != null) {
            spin.addChangeListener((e) -> {
                list.update(e, (int) spin.getValue());
            });
        }
        return spin;
    }

    public JSpinner addDoubleSpinner(double init, double min, double max, double step, NewValueListener<ChangeEvent, Double> list) {
        JSpinner spin = new JSpinner(new SpinnerNumberModel(init, min, max, step));
        addComponent(spin);
        if (list != null) {
            spin.addChangeListener((e) -> {
                list.update(e, (double) spin.getValue());
            });
        }
        return spin;
    }

    public JSlider addIntegerSlider(int init, int min, int max, NewValueListener<ChangeEvent, Integer> list) {
        JSlider slide = new JSlider(min, max, init);
        addComponent(slide);
        if (list != null) {
            slide.addChangeListener((e) -> {
                list.update(e, (int) slide.getValue());
            });
        }
        return slide;
    }

    public <T> JComboBox addComboBox(T[] objects, NewValueListener<ItemEvent, T> list) {
        JComboBox box = new JComboBox(objects);
        addComponent(box);
        if (list != null) {
            box.addItemListener((e) -> {
                list.update(e, (T) box.getSelectedItem());
            });
        }
        return box;
    }

    public <T> JComboBox addComboBox(T[] objects, T selected, NewValueListener<ItemEvent, T> list) {
        JComboBox box = new JComboBox(objects);
        box.setSelectedItem(selected);
        addComponent(box);
        if (list != null) {
            box.addItemListener((e) -> {
                list.update(e, (T) box.getSelectedItem());
            });
        }
        return box;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void updatePreferredSize() {
        _panel.setPreferredSize(new Dimension(_partof._tabMargin + _maxElementWidth, _y));
    }

    private class SplitSpec {

        final int _index;
        final int _total;
        final int _splitSpace;
        final double _fracStart;
        final double _fracWidth;

        SplitSpec(int index, int total, int splitSpace, double fracStart, double fracWidth) {
            _index = index;
            _total = total;
            _splitSpace = splitSpace;
            _fracStart = fracStart;
            _fracWidth = fracWidth;
        }
    }

    private abstract class Addition {

        final JComponent _comp;
        final int _yCoord;

        Addition(JComponent comp, int yCoord) {
            _comp = comp;
            _yCoord = yCoord;
        }

        abstract void place();
    }

    private class StandardAddition extends Addition {

        final int _height;

        StandardAddition(JComponent comp, int yCoord) {
            super(comp, yCoord);
            _height = _partof._elementHeight;
        }

        StandardAddition(JComponent comp, int yCoord, int height) {
            super(comp, yCoord);
            _height = height;
        }

        @Override
        void place() {
            if (_comp != null) {
                _comp.setBounds(_partof._tabMargin, _yCoord, _maxElementWidth, _height);
            }
        }
    }

    private class SplitAddition extends Addition {

        final SplitSpec _split;

        SplitAddition(JComponent comp, int yCoord, SplitSpec split) {
            super(comp, yCoord);
            _split = split;
        }

        @Override
        void place() {
            if (_comp != null) {
                int totW = _maxElementWidth - _split._splitSpace * (_split._total - 1);
                int left = _partof._tabMargin
                        + _split._splitSpace * _split._index
                        + (int) Math.round(totW * _split._fracStart);
                int width = (int) Math.round(totW * _split._fracWidth);
                _comp.setBounds(left, _yCoord, width, _partof._elementHeight);
            }
        }
    }

    private class FixedAddition extends Addition {

        final int _width;
        final int _height;

        FixedAddition(JComponent comp, int yCoord, int width, int height) {
            super(comp, yCoord);
            _width = width;
            _height = height;
        }

        @Override
        void place() {
            if (_comp != null) {
                int customMargin = _partof._tabMargin + (_maxElementWidth - _width) / 2;
                _comp.setBounds(customMargin, _yCoord, _width, _height);
            }
        }
    }
    //</editor-fold>
}

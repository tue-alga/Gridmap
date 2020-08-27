/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.streams;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * Simple panel that can be used to capture text that is sent to the System.out
 * and System.err writers. It replaces these writers, with or without forward to
 * the old ones.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class StreamsPanel extends JPanel {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final JTabbedPane _tabs;
    private final Redirect _out, _err;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Creates a streams panel to replace the given streams, allowing both the
     * old console streams as well as streaming to the new panel.
     *
     * @param streams streams to be replaced
     * @param out_default default place to write for System.out
     * @param err_default default place to write for System.err
     */
    public StreamsPanel(SelectedStreams streams, StreamSetting out_default, StreamSetting err_default) {
        this(streams, StreamSetting.BOTH, out_default, StreamSetting.BOTH, err_default);
    }

    /**
     * Creates a streams panel to replace the given streams, with configurable
     * settings the places where writing to the System streams ends up.
     *
     * @param streams streams to be replaced
     * @param out_allowed allowed places to write for System.out
     * @param out_default default place to write for System.out
     * @param err_allowed allowed places to write for System.err
     * @param err_default default place to write for System.err
     */
    public StreamsPanel(SelectedStreams streams, StreamSetting out_allowed, StreamSetting out_default, StreamSetting err_allowed, StreamSetting err_default) {

        setLayout(new BorderLayout());

        switch (streams) {
            case ERR:
                _tabs = null;
                _out = null;

                _err = new Redirect(System.err, err_allowed, err_default);
                System.setErr(new PrintStream(_err, true));

                if (_err._panel != null) {
                    add(_err._panel, BorderLayout.CENTER);
                }
                break;
            case OUT:
                _tabs = null;
                _err = null;

                _out = new Redirect(System.out, out_allowed, out_default);
                System.setOut(new PrintStream(_out, true));

                if (_out._panel != null) {
                    add(_out._panel, BorderLayout.CENTER);
                }
                break;
            default:
            case BOTH:
                _err = new Redirect(System.err, err_allowed, err_default);
                System.setErr(new PrintStream(_err, true));

                _out = new Redirect(System.out, out_allowed, out_default);
                System.setOut(new PrintStream(_out, true));

                if (_out._panel != null && _err._panel != null) {
                    _tabs = new JTabbedPane();

                    _tabs.addTab("Output", _out._panel);
                    _tabs.addTab("Error", _err._panel);

                    add(_tabs, BorderLayout.CENTER);
                } else if (_out._panel != null) {
                    _tabs = null;
                    add(_out._panel, BorderLayout.CENTER);
                } else if (_err._panel != null) {
                    _tabs = null;
                    add(_err._panel, BorderLayout.CENTER);
                } else {
                    _tabs = null;
                }
                break;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REPLACEMENT STREAM">
    /**
     * Class used as an intermediary to write to the old system stream and/or
     * the GUI.
     */
    private class Redirect extends OutputStream {

        // original ssytem stream
        final OutputStream _sys;
        // GUI elements
        final JPanel _panel;
        final JScrollPane _scroll;
        final JTextArea _text;
        // settings
        final StreamSetting _allowed;
        StreamSetting _current;

        private Redirect(OutputStream sys, StreamSetting allowed, StreamSetting current) {
            _allowed = allowed;
            _current = current;
            _sys = sys;

            // ensure current is subset of allowed
            if (!_allowed.containsGUI()) {
                _current = _current.changeGUI(false);
            }
            if (!_allowed.containsSystem()) {
                _current = _current.changeSystem(false);
            }

            // initialize GUI as needed
            if (_allowed == StreamSetting.NONE) {
                _current = _allowed;

                _panel = null;
                _text = null;
                _scroll = null;
            } else {

                _panel = new JPanel(new BorderLayout());

                JPanel controls = new JPanel(new BorderLayout());
                JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEADING));
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
                controls.add(checks, BorderLayout.WEST);
                controls.add(buttons, BorderLayout.EAST);

                controls.setMinimumSize(new Dimension(10, 20));

                if (_allowed.containsGUI()) {
                    _panel.add(controls, BorderLayout.SOUTH);
                } else {
                    _panel.add(controls, BorderLayout.CENTER);
                }

                if (_allowed.containsSystem()) {
                    JCheckBox checksys = new JCheckBox("Enable system", _current.containsSystem());
                    checksys.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            _current = _current.changeSystem(e.getStateChange() == ItemEvent.SELECTED);
                        }
                    });

                    checks.add(checksys);
                }

                if (_allowed.containsGUI()) {
                    _text = new JTextArea();
                    _scroll = new JScrollPane(_text, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    _panel.add(_scroll, BorderLayout.CENTER);

                    JCheckBox checkgui = new JCheckBox("Enable GUI", _current.containsGUI());
                    checkgui.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            _current = _current.changeGUI(e.getStateChange() == ItemEvent.SELECTED);
                        }
                    });

                    JButton btncopy = new JButton("Copy contents");
                    btncopy.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ClipboardUtil.setClipboardContents(_text.getText());
                        }
                    });
                    JButton btnclear = new JButton("Clear");
                    btnclear.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _text.setText("");
                        }
                    });

                    checks.add(checkgui);
                    buttons.add(btncopy);
                    buttons.add(btnclear);
                } else {
                    _text = null;
                    _scroll = null;
                }
            }
        }

        private void writeStringToGUI(final String msg) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _text.append(msg);
                    JScrollBar vsb = _scroll.getVerticalScrollBar();
                    if (vsb != null) {
                        vsb.setValue(vsb.getMaximum());
                    }
                }
            });
        }

        @Override
        public void write(int b) throws IOException {
            switch (_current) {
                case NONE:
                    break;
                case GUI:
                    writeStringToGUI(String.valueOf((char) b));
                    break;
                case CONSOLE:
                    _sys.write(b);
                    break;
                case BOTH:
                    _sys.write(b);
                    writeStringToGUI(String.valueOf((char) b));
                    break;
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            switch (_current) {
                case NONE:
                    break;
                case GUI:
                    writeStringToGUI(new String(b, off, len));
                    break;
                case CONSOLE:
                    _sys.write(b, off, len);
                    break;
                case BOTH:
                    _sys.write(b, off, len);
                    writeStringToGUI(new String(b, off, len));
                    break;
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            switch (_current) {
                case NONE:
                    break;
                case GUI:
                    writeStringToGUI(new String(b, 0, b.length));
                    break;
                case CONSOLE:
                    _sys.write(b);
                    break;
                case BOTH:
                    _sys.write(b);
                    writeStringToGUI(new String(b, 0, b.length));
                    break;
            }
        }
    }
    //</editor-fold>
}

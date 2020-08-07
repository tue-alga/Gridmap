/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

/**
 * Panel that allows for easy programmatic construction of a basic GUI
 * interface. It focuses on building SideTabs, each of which is a simple panel
 * in a tabbed panel. Scrolling is handled automatically and the basic settings
 * are configured at the time of constructing the TabbedSidePanel.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class TabbedSidePanel extends JPanel {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private List<SideTab> _tabs;
    private JTabbedPane _tabpane;
    int _tabMargin;
    int _elementHeight;
    int _elementSpacing;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a tabbed side panel with default settings. The defaults are
     * panel width 250, margin 5, element height 20 and spacing 3.
     */
    public TabbedSidePanel() {
        this(250, 5, 20, 3);
    }

    /**
     * Constructs a tabbed side panel with the provided settings.
     *
     * @param sidePanelWidth controls the width of the tabbed side panel
     * @param tabMargin controls the margins for the side tabs
     * @param elementHeight controls the default element height for components
     * on a side tab
     * @param elementSpacing controls the default spacing between two components
     * on a side tab
     */
    public TabbedSidePanel(int sidePanelWidth, int tabMargin, int elementHeight, int elementSpacing) {
        super();
        _tabMargin = tabMargin;
        _elementHeight = elementHeight;
        _elementSpacing = elementSpacing;

        Dimension D = new Dimension(sidePanelWidth, 100);
        setMinimumSize(D);
        setPreferredSize(D);
        setSize(D);

        setLayout(new BorderLayout());
        _tabpane = new JTabbedPane(JTabbedPane.TOP);
        add(_tabpane, BorderLayout.CENTER);
        _tabs = new ArrayList();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTabs();
            }
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Finds a side tab by name. This is case sensitive. If the side tab is not
     * found, null is returned.
     *
     * @param name case-sensitive name of the tab
     * @return the side tab with the given name, if it exists
     */
    public SideTab getTab(String name) {
        for (SideTab tab : _tabs) {
            if (tab._name.equals(name)) {
                return tab;
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Toggles the activity of the tab with the provided name.
     *
     * @param name name of the tab
     * @param enabled new state of the tab
     */
    public void setTabEnabled(String name, boolean enabled) {

        int index = _tabpane.indexOfTab(name);
        _tabpane.setEnabledAt(index, enabled);

        if (!enabled && _tabpane.getSelectedIndex() == index) {
            for (int i = 0; i < _tabpane.getTabCount(); i++) {
                if (_tabpane.getTabComponentAt(i).isEnabled()) {
                    _tabpane.setSelectedIndex(index);
                    break;
                }
            }
        }
    }

    /**
     * Adds a new tab to the panel.
     *
     * @param name name of the new tab
     * @return a new empty side tab
     */
    public SideTab addTab(String name) {
        SideTab tab = new SideTab(name, this);

        JScrollPane scroll = new JScrollPane(tab._panel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        _tabpane.add(name, scroll);
        _tabs.add(tab);
        return tab;
    }
    
    public void resizeTabs() {
        for (SideTab tab : _tabs) {
            tab.resizeTab();
        }
    }
    //</editor-fold>
}

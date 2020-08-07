/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.parameterconfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ConfigurableEnumParameter<TEnum extends Enum<TEnum>> extends ConfigurableParameter<TEnum> {

    private final TEnum[] _enumvalues;
    
    public ConfigurableEnumParameter(String label, TEnum defaultvalue, TEnum... enumvalues) {
        super(label, defaultvalue);
        _enumvalues = enumvalues;
    }

    @Override
    public boolean setValueFromString(String valuestring) {
        Logger.getLogger(ConfigurableEnumParameter.class.getName()).log(Level.SEVERE, "Cannot set enum parameter from String type");
        return false;
    }

    @Override
    public void promptForNewValue(JComponent parent) {
        
        Object result = JOptionPane.showInputDialog(parent, getLabel(), getLabel(), JOptionPane.QUESTION_MESSAGE, null, _enumvalues, getValue());
        if (result != null) {
            setValue((TEnum) result);
        }
    }

    @Override
    public void updateInterface() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}

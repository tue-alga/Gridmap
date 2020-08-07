/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

import java.util.EventObject;

/**
 * Convenience interface to make handling updates based on setting new values in
 * the interface easier. Internally, it'll wrap the traditional event handlers,
 * forwarding their event, but explicitly include the newly set value of the
 * interface item.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TEvent> Event class belonging to the wrapped event handler
 * @param <TValue> Class of values that are to be configured
 */
public interface NewValueListener<TEvent extends EventObject, TValue> {

    void update(TEvent e, TValue value);
}

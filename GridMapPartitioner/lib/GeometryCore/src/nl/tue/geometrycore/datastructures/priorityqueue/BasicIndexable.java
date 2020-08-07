/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.priorityqueue;

/**
 * Provides a basic implementation of the {@link Indexable} interface.
 * 
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class BasicIndexable implements Indexable {

    private int _index = -1;

    @Override
    public void setIndex(int index) {
        _index = index;
    }

    @Override
    public int getIndex() {
        return _index;
    }
}
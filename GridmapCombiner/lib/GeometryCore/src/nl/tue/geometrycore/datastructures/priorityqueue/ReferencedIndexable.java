/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.priorityqueue;

/**
 * Provides a simple wrapper around objects that cannot implement Indexable or
 * that need to be in multiple data structures.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public final class ReferencedIndexable<T> extends BasicIndexable {

    private final T _object;

    public ReferencedIndexable(T object) {
        _object = object;
    }

    public T getObject() {
        return _object;
    }
}

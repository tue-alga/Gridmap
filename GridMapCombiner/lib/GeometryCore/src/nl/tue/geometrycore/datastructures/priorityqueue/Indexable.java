/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.priorityqueue;

/**
 * An indexable object maintains its index in a data structure (often an array
 * or list). This allows quicker look-ups for the element, as well as a simple
 * way of "hashing", by creating an equally sized array and using the index as a
 * key.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface Indexable {

    /**
     * Sets the index of the object. This method is typically only used by the
     * data structure itself
     *
     * @param index the new index of the object
     */
    public void setIndex(int index);

    /**
     * Gets the index of the object in the data structure in which it is stored.
     *
     * @return index of the object
     */
    public int getIndex();
}

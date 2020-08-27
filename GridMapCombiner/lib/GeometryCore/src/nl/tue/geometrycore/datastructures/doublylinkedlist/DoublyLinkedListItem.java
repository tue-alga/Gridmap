/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.doublylinkedlist;

/**
 * Base class for storing objects in a {@link DoublyLinkedList}. Note that every
 * object can be stored in at most one list. We do not keep track of which list 
 * an object is stored in, for sake of efficiency.
 *
 * @param <TItem> class of items for the list, typically, the inheriting class
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DoublyLinkedListItem<TItem extends DoublyLinkedListItem> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private TItem _previous, _next;
    private boolean _containedInList;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Basic constructor, for an item not stored in a list.
     */
    public DoublyLinkedListItem() {
        _previous = null;
        _next = null;
        _containedInList = false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    /**
     * Returns whether this object is stored in a list. Note that we do not
     * store the containing list, to allow efficient merging of lists.
     *
     * @return whether object is in a list
     */
    public boolean isContainedInList() {
        return _containedInList;
    }

    /**
     * Returns the next item in the list. Returns null if this item is the last 
     * in the list, or not contained in a list.
     * 
     * @return the next item
     */
    public TItem getNext() {
        return _next;
    }

    /**
     * Returns the previous item in the list. Returns null if this item is the 
     * first in the list, or not contained in a list.
     * 
     * @return the next item
     */
    public TItem getPrevious() {
        return _previous;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="INTERNAL">
    void setContainedInList(boolean contained) {
        _containedInList = contained;
    }
    
    void setNext(TItem next) {
        this._next = next;
    }
    
    void setPrevious(TItem prev) {
        this._previous = prev;
    }
    //</editor-fold>
}

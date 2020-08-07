/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

/**
 * Simple class to represent a tuple/pair of two objects.
 *
 * @param <T1> type of the first object in the pair
 * @param <T2> type of the second object in the pair
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Pair<T1, T2> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private T1 _first;
    private T2 _second;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty pair.
     */
    public Pair() {
        _first = null;
        _second = null;
    }

    /**
     * Constructs a pair from the given objects.
     *
     * @param first first object of the pair
     * @param second second object of the pair
     */
    public Pair(T1 first, T2 second) {
        _first = first;
        _second = second;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">    
    public T1 getFirst() {
        return _first;
    }

    public void setFirst(T1 first) {
        _first = first;
    }

    public T2 getSecond() {
        return _second;
    }

    public void setSecond(T2 second) {
        _second = second;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ " + _first.toString() + " , " + _second.toString() + " ]";
    }
    //</editor-fold>
}

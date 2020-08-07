/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import java.util.Comparator;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class LexicographicOrder<TVector extends Vector> implements Comparator<TVector> {

    public enum Order {
        X_INC_Y_INC,
        X_INC_Y_DEC,
        X_DEC_Y_INC,
        X_DEC_Y_DEC,
        Y_INC_X_INC,
        Y_INC_X_DEC,
        Y_DEC_X_INC,
        Y_DEC_X_DEC
    }

    private final Order _order;

    public LexicographicOrder() {
        this(Order.X_INC_Y_INC);
    }

    public LexicographicOrder(Order order) {
        _order = order;
    }

    @Override
    public int compare(TVector o1, TVector o2) {
        switch (_order) {
            default:
            case X_INC_Y_INC: {
                int x = Double.compare(o1.getX(), o2.getX());
                if (x == 0) {
                    return Double.compare(o1.getY(), o2.getY());
                } else {
                    return x;
                }
            }
            case X_INC_Y_DEC: {
                int x = Double.compare(o1.getX(), o2.getX());
                if (x == 0) {
                    return -Double.compare(o1.getY(), o2.getY());
                } else {
                    return x;
                }
            }
            case X_DEC_Y_INC: {
                int x = Double.compare(o1.getX(), o2.getX());
                if (x == 0) {
                    return Double.compare(o1.getY(), o2.getY());
                } else {
                    return -x;
                }
            }
            case X_DEC_Y_DEC: {
                int x = Double.compare(o1.getX(), o2.getX());
                if (x == 0) {
                    return -Double.compare(o1.getY(), o2.getY());
                } else {
                    return -x;
                }
            }
            case Y_INC_X_INC: {
                int y = Double.compare(o1.getY(), o2.getY());
                if (y == 0) {
                    return Double.compare(o1.getX(), o2.getX());
                } else {
                    return y;
                }
            }
            case Y_INC_X_DEC: {
                int y = Double.compare(o1.getY(), o2.getY());
                if (y == 0) {
                    return -Double.compare(o1.getX(), o2.getX());
                } else {
                    return y;
                }
            }
            case Y_DEC_X_INC: {
                int y = Double.compare(o1.getY(), o2.getY());
                if (y == 0) {
                    return Double.compare(o1.getX(), o2.getX());
                } else {
                    return -y;
                }
            }
            case Y_DEC_X_DEC: {
                int y = Double.compare(o1.getY(), o2.getY());
                if (y == 0) {
                    return -Double.compare(o1.getX(), o2.getX());
                } else {
                    return -y;
                }
            }
        }

    }
}

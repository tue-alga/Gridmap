/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.hulls;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.LexicographicOrder;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TVector>
 */
public class ConvexHull<TVector extends Vector> {

    public enum ListMaintenance {
        MAINTAIN_ORDER,
        MAINTAIN_ELEMENTS,
        REMOVE_CH;
    }

    public List<TVector> computeHull(List<TVector> points, ListMaintenance maintain) {
        // TODO: we can be smarter if ALL_ELEMENTS is specified...
        if (maintain != ListMaintenance.REMOVE_CH) {
            points = new ArrayList(points);
        }

        List<TVector> upper = computeHalfHullWithOrder(points, LexicographicOrder.Order.X_INC_Y_INC);
        // TODO: this can be done faster by using the sorted order
        points.removeAll(upper);
        points.add(upper.get(0));
        points.add(upper.get(upper.size() - 1));
        List<TVector> lower = computeHalfHullWithOrder(points, LexicographicOrder.Order.X_DEC_Y_DEC);

        // NB: exclude first and last, these are same as upper hull
        for (int i = 1; i < lower.size() - 1; i++) {
            upper.add(lower.get(i));
        }

        if (maintain == ListMaintenance.REMOVE_CH) {
            // irrelevant if we made a new list earlier
            // TODO: this can be done faster by using the sorted order
            points.removeAll(lower);
        }
        return upper;
    }

    public List<TVector> computeUpperHull(List<TVector> points, ListMaintenance maintain) {
        return computeHalfHullWithOrder(points, maintain, LexicographicOrder.Order.X_INC_Y_INC);
    }

    public List<TVector> computeLowerHull(List<TVector> points, ListMaintenance maintain) {
        return computeHalfHullWithOrder(points, maintain, LexicographicOrder.Order.X_DEC_Y_DEC);
    }

    public List<TVector> computeLeftHull(List<TVector> points, ListMaintenance maintain) {
        return computeHalfHullWithOrder(points, maintain, LexicographicOrder.Order.Y_INC_X_INC);
    }

    public List<TVector> computeRightHull(List<TVector> points, ListMaintenance maintain) {
        return computeHalfHullWithOrder(points, maintain, LexicographicOrder.Order.Y_DEC_X_DEC);
    }

    private List<TVector> computeHalfHullWithOrder(List<TVector> points, ListMaintenance maintain, LexicographicOrder.Order order) {
        // TODO: we can be smarter if ALL_ELEMENTS is specified...
        if (maintain != ListMaintenance.REMOVE_CH) {
            points = new ArrayList(points);
        }

        List<TVector> hull = computeHalfHullWithOrder(points, order);

        if (maintain == ListMaintenance.REMOVE_CH) {
            // irrelevant if we made a new list earlier
            // TODO: this can be done faster by using the sorted order
            points.removeAll(hull);
        }
        return hull;
    }

    private List<TVector> computeHalfHullWithOrder(List<TVector> points, LexicographicOrder.Order order) {
        points.sort(new LexicographicOrder(order));
        List<TVector> hull = new ArrayList();
        TVector pp = points.get(0);
        TVector p = points.get(1);
        hull.add(pp);
        hull.add(p);
        for (int i = 2; i < points.size(); i++) {
            TVector c = points.get(i);
            while (pp != null && Vector.crossProduct(Vector.subtract(p, pp), Vector.subtract(c, p)) > 0) {
                p = pp;
                hull.remove(hull.size() - 1);
                pp = hull.size() >= 2 ? hull.get(hull.size() - 2) : null;
            }
            hull.add(c);
            pp = p;
            p = c;
        }
        return hull;
    }
}

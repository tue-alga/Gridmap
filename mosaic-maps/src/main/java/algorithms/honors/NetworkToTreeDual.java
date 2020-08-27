package algorithms.honors;

import model.subdivision.PlanarSubdivision;
import java.util.ArrayList;

import model.*;
import model.Network.Vertex;
import model.graph.PlanarStraightLineGraph;

/**
 * This class just contains static methods that can convert a network into its
 * weak dual. The weak dual is again a network, but may be represented as a tree
 * so that it is possible to link vertices to faces in the primal network.
 */
public class NetworkToTreeDual {

    /**
     * Convert a triangulation to its weak dual. Probably not very efficient.
     *
     * @param n The network. Should be an outerplanar triangulation. If not,
     * null is returned.
     * @return The weak dual, or null if the given network is not a
     * triangulation.
     */
    public static Tree triangulationToTreeDual(Network n) {
        PlanarSubdivision subdivision = new PlanarSubdivision(n);
        PlanarStraightLineGraph dual = new PlanarStraightLineGraph();
        subdivision.computeWeakDual(dual);
        Tree d = null;
        ArrayList<Tree> toConnectEndpoints = new ArrayList<>();
        ArrayList<Tree> pathsToAdd = new ArrayList<>();
        Network c = new Network(n);
        for (int i = 0; i < n.numberOfVertices(); i++) {
            c.getVertex(i).setPosition(n.getVertex(i).getPosition());
        }
        Vertex ear;

        do {
            // remove an ear from the network
            ear = getDegreeTwoVertex(c);
            if (ear == null) {
                break;
            }
            c.removeVertex(ear);

            // construct a path as long as possible
            Face f = new Face(ear, n.getNeighbour(ear, 0), n.getNeighbour(ear, 1));
            Tree pathStart = new Tree(null, null, f);
            Tree path = pathStart;
            while (isTriangleCorner(n, n.getNeighbour(ear, 0))
                    || isTriangleCorner(n, n.getNeighbour(ear, 1))) {
                ear = (isTriangleCorner(n, n.getNeighbour(ear, 0)) ? n.getNeighbour(ear, 0)
                        : n.getNeighbour(ear, 1));
                c.removeVertex(ear);
                f = new Face(ear, n.getNeighbour(ear, 0), n.getNeighbour(ear, 1));
                path = new Tree(path, null, f);
            }

            // add the constructed path to d if possible, otherwise store it for later
            if (d == null) {
                d = path;
            } else {
                pathsToAdd.add(path);
            }
            // possibly, we ran into a split in the tree
            if (n.getDegree(n.getNeighbour(ear, 0)) > 2
                    && n.getDegree(n.getNeighbour(ear, 1)) > 2) {
                toConnectEndpoints.add(path);
            }
        } while (c.numberOfVertices() > 0 && ear != null);

        // make sure we have a path and we got all of the network
        if (d == null || c.numberOfVertices() > 2) {
            return null;
        }

//		System.out.println("Endpoints: " + toConnectEndpoints);
//		System.out.println("Paths to add: " + pathsToAdd);

        // add paths we could not figure out before
        for (Tree pathToAdd : pathsToAdd) {
//			System.out.println("ADDING PATH" + pathToAdd);
            walkThePath:
            do {
                ArrayList<Tree> connectedConnectPoints = new ArrayList<>();
                for (Tree connectPoint : toConnectEndpoints) {
                    if (pathToAdd.face.sharesPointsWith(connectPoint.face, 2)
                            && !pathToAdd.face.equals(connectPoint.face)
                            && !(pathToAdd.parentTree != null
                            && pathToAdd.parentTree.face.equals(connectPoint.face))
                            && !(pathToAdd.subTree1 != null
                            && pathToAdd.subTree1.face.equals(connectPoint.face))
                            && !(pathToAdd.subTree2 != null
                            && pathToAdd.subTree2.face.equals(connectPoint.face))
                            && pathToAdd.face.ID > connectPoint.face.ID) {
                        if (connectPoint.subTree1 == null) {
                            connectPoint.subTree1 = pathToAdd;
                        } else {
                            connectPoint.subTree2 = pathToAdd;
                        }
                        if (pathToAdd.subTree2 == null) {
                            pathToAdd.subTree2 = pathToAdd.parentTree;
                            pathToAdd.parentTree = connectPoint;
                            swapPath(pathToAdd.subTree2);
                        } else {
                            pathToAdd.subTree1 = pathToAdd.parentTree;
                            pathToAdd.parentTree = connectPoint;
                            swapPath(pathToAdd.subTree1);
                        }
//						System.out.println("SET PARENT OF" + pathToAdd + "TO" + connectPoint);
                        // Add connected connectpoint to list of connectpoints that
                        // can be removed later. We cannot do that now, since that
                        // would violate the foreach loop we are in :-)
                        connectedConnectPoints.add(connectPoint);
                        // We cannot stop the outer loop here, as it may be the case
                        // that one path needs to connect to multiple endpoints!
                        // We can however stop the inner loop, as one face can never connect
                        // to two connect points, then it would have been a path...
                        continue walkThePath;
                    }
                }
                pathToAdd = pathToAdd.subTree1;
                // remove connectpoints that we connected to
                for (Tree connectedConnectPoint : connectedConnectPoints) {
                    toConnectEndpoints.remove(connectedConnectPoint);
//					System.out.println("REMOVING " + connectedConnectPoint);
                }
            } while (pathToAdd != null);
        }

        while (d.parentTree != null) {
            d = d.parentTree;
//			System.out.println("D =" + d);
        }
        return d;
    }

    /**
     * Return a vertex with degree 2 from the given network.
     *
     * @param n Network to look in.
     * @return A vertex with degree 2, or null if no such vertex was found.
     */
    private static Vertex getDegreeTwoVertex(Network n) {
        for (Vertex v : n.vertices()) {
            if (isTriangleCorner(n, v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Returns if a vertex has degree 2 and its two neighbours are at least
     * connected to eachother (they may be connected to other vertices as well).
     * This is useful to check if the vertex is the start of a path.
     *
     * @param v Vertex to consider.
     * @return See above.
     */
    private static boolean isTriangleCorner(Network n, Vertex v) {
        return (n.getDegree(v) == 2
                && n.getEdge(n.getNeighbour(v, 0), n.getNeighbour(v, 1)) != null);
    }

    /**
     * Swaps the subTree1 and parentTree references of this tree, recursively.
     * That is, this function will call {@code swapPath(tree.parentTree)} with
     * the original parentTree for as long as that reference exists.
     *
     * <p>If a tree has no subTree1, but a subTree2 reference, then subTree1
     * will become subTree2 and subTree2 will become null before the swap.
     *
     * @param tree Tree to swap references of.
     */
    private static void swapPath(Tree tree) {
        if (tree == null) {
            return;
        }

        // if the parent has a subTree2 pointer to this tree, then we must fix that
        Tree tmp;
        if (tree.parentTree != null && tree.parentTree.subTree2 != null
                && tree.parentTree.subTree2.face.equals(tree.face)) {
            tmp = tree.parentTree.subTree1;
            tree.parentTree.subTree1 = tree.parentTree.subTree2;
            tree.parentTree.subTree2 = tmp;
        }
        tmp = tree.subTree1;
        tree.subTree1 = tree.parentTree;
        tree.parentTree = tmp;
        swapPath(tree.subTree1);
    }
}

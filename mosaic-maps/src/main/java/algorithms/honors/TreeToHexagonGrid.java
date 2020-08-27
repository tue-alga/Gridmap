package algorithms.honors;

import model.HexagonalMap;
import model.HexagonalMap.BarycentricCoordinate;
import model.Network.Vertex;

public class TreeToHexagonGrid {

    public static HexagonalMap computeHexagonGrid(Tree dual) {
        HexagonalMap h = new HexagonalMap();

        addTree(h, dual, null, null);

        return h;
    }

    private static void addTree(HexagonalMap h, Tree t, Tree leftParentTree, Tree topParentTree) {

        if (t.face == null) {
            return;
        }

        // find the neighbouring trees
        Tree bottomSubTree = null, rightSubTree = null;
        if (t.subTree1 != null && t.xCoordinate == t.subTree1.xCoordinate) {
            bottomSubTree = t.subTree1;
            rightSubTree = t.subTree2;
        }

        if (t.subTree2 != null && t.xCoordinate == t.subTree2.xCoordinate) {
            if (bottomSubTree != null) {
                throw new RuntimeException("Something is really wrong with the hv-drawing: "
                        + "both subtrees are at the same x-coordinate as their parent node:\n"
                        + t.toString());
            }
            bottomSubTree = t.subTree2;
            rightSubTree = t.subTree1;
        }

        /*// color type-1 vertices
         if (bottomSubTree != null && rightSubTree != null) {
         Vertex bottomRight = findCommonVertex(bottomSubTree, rightSubTree);
         setVertex(h, 3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate - 1, bottomRight);
         }
         if (topParentTree != null && rightSubTree != null) {
         Vertex topRight = findCommonVertex(topParentTree, rightSubTree);
         setVertex(h, 3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate, topRight);
         }
        
         if (bottomSubTree != null && leftParentTree != null) {
         Vertex bottomLeft = findCommonVertex(bottomSubTree, leftParentTree);
         setVertex(h, 3 * t.xCoordinate, 0, 3 * -t.yCoordinate - 1, bottomLeft);
         }
         if (topParentTree != null && leftParentTree != null) {
         Vertex topLeft = findCommonVertex(topParentTree, leftParentTree);
         setVertex(h, 3 * t.xCoordinate, 0, 3 * -t.yCoordinate, topLeft);
         }*/

        tryToColor(h, t, topParentTree, rightSubTree, bottomSubTree, leftParentTree);

        // augment the hexagons of this tree
        // that is: if we can infer one of the colors, fill that one in
        // note: this is now commented out for debugging... it shouldn't hurt to
        // augment, but it should also work without it
        //augmentColoring(h, t);

        // handle subtrees
        if (t.subTree1 != null) {
            addSubtree(h, t, t.subTree1);
        }
        if (t.subTree2 != null) {
            addSubtree(h, t, t.subTree2);
        }

        tryToColor(h, t, topParentTree, rightSubTree, bottomSubTree, leftParentTree);

        //augmentColoring(h, t);
    }

    private static void tryToColor(HexagonalMap h, Tree t, Tree topParentTree,
            Tree rightSubTree, Tree bottomSubTree, Tree leftParentTree) {

        // je mag alleen spieken bij je ouders :D

        int caseId =
                (topParentTree != null ? 0b1000 : 0b0000)
                + (rightSubTree != null ? 0b0100 : 0b0000)
                + (bottomSubTree != null ? 0b0010 : 0b0000)
                + (leftParentTree != null ? 0b0001 : 0b0000);

        switch (caseId) {
            case 0b0000:
                // lonely node
                setTLVertex(h, t, t.face.vertices.get(0));
                setTRVertex(h, t, t.face.vertices.get(0));
                setBLVertex(h, t, t.face.vertices.get(1));
                setBRVertex(h, t, t.face.vertices.get(2));
                break;
            case 0b0001:
                // only left
                setTLVertex(h, t, getTRVertex(h, leftParentTree));
                setTRVertex(h, t, findOtherVertex(t, leftParentTree));
                setBLVertex(h, t, getBRVertex(h, leftParentTree));
                setBRVertex(h, t, getTRVertex(h, t));

                if (getTLVertex(h, t) == null && getBLVertex(h, t) == null) {
                    setTLVertex(h, t, findCommonVertex(t, leftParentTree));
                }
                augmentColoring(h, t);
                break;
            case 0b0010:
                // only bottom
                setTLVertex(h, t, findOtherVertex(t, bottomSubTree));
                setTRVertex(h, t, getTLVertex(h, t));
                setBLVertex(h, t, getTLVertex(h, bottomSubTree)); // spiek (maar dat is onvermijdelijk)
                setBRVertex(h, t, getTRVertex(h, bottomSubTree)); // spiek (maar dat is onvermijdelijk)
                break;
            case 0b0011:
                // bottom and left
                setTLVertex(h, t, findCommonVertexExcept(t, leftParentTree, getBLVertex(h, t)));
                setTRVertex(h, t, getTLVertex(h, t));
                setBLVertex(h, t, findCommonVertex(bottomSubTree, leftParentTree));
                setBRVertex(h, t, findCommonVertexExcept(t, bottomSubTree, getBLVertex(h, t)));
                break;
            case 0b0100:
                // only right
                setTLVertex(h, t, findOtherVertex(t, rightSubTree));
                setTRVertex(h, t, getTLVertex(h, rightSubTree)); // spiek (maar dat is onvermijdelijk)
                setBLVertex(h, t, getTLVertex(h, t));
                setBRVertex(h, t, getBLVertex(h, rightSubTree)); // spiek (maar dat is onvermijdelijk)
                break;
            case 0b0101:
                // right and left
                setTLVertex(h, t, getTRVertex(h, leftParentTree));
                setBLVertex(h, t, getBRVertex(h, leftParentTree));

                if (getTLVertex(h, t) == findCommonVertex(leftParentTree, rightSubTree)) {
                    setTRVertex(h, t, getTLVertex(h, t));
                    setBRVertex(h, t, findCommonVertexExcept(t, rightSubTree, getTRVertex(h, t)));
                } else {
                    setBRVertex(h, t, getBLVertex(h, t));
                    setTRVertex(h, t, findCommonVertexExcept(t, rightSubTree, getBRVertex(h, t)));
                }
                break;
            case 0b0110:
                // right and bottom
                setBRVertex(h, t, findCommonVertex(bottomSubTree, rightSubTree));
                setTRVertex(h, t, findCommonVertexExcept(t, rightSubTree, getBRVertex(h, t)));
                setTLVertex(h, t, getTRVertex(h, t));
                setBLVertex(h, t, findCommonVertexExcept(t, bottomSubTree, getBRVertex(h, t)));
                break;
            case 0b0111:
                // right, bottom and left
                setTLVertex(h, t, findCommonVertex(leftParentTree, rightSubTree));
                setTRVertex(h, t, findCommonVertex(leftParentTree, rightSubTree));
                setBLVertex(h, t, findCommonVertex(leftParentTree, bottomSubTree));
                setBRVertex(h, t, findCommonVertex(rightSubTree, bottomSubTree));
                break;
            case 0b1000:
                // only top
                setTLVertex(h, t, getBLVertex(h, topParentTree));
                setTRVertex(h, t, getBRVertex(h, topParentTree));
                setBLVertex(h, t, findOtherVertex(t, topParentTree));
                setBRVertex(h, t, getBLVertex(h, t));

                if (getTLVertex(h, t) == null && getTRVertex(h, t) == null) {
                    setTLVertex(h, t, findCommonVertex(t, topParentTree));
                }
                augmentColoring(h, t);
                break;
            case 0b1001:
                // top and left
                throw new RuntimeException("OH NEEEEE");
            case 0b1010:
                // top and bottom
                setTLVertex(h, t, getBLVertex(h, topParentTree));
                setTRVertex(h, t, getBRVertex(h, topParentTree));
                setBLVertex(h, t, getTLVertex(h, bottomSubTree));
                setBRVertex(h, t, getTRVertex(h, bottomSubTree));

                if (getTLVertex(h, t) == findCommonVertex(topParentTree, bottomSubTree)
                        || getBLVertex(h, t) == findCommonVertex(topParentTree, bottomSubTree)) {
                    setTLVertex(h, t, findCommonVertex(topParentTree, bottomSubTree));
                    setTRVertex(h, t, findCommonVertexExcept(t, topParentTree, getTLVertex(h, t)));
                    setBLVertex(h, t, getTLVertex(h, t));
                    setBRVertex(h, t, findCommonVertexExcept(t, bottomSubTree, getBLVertex(h, t)));
                } else {
                    setTRVertex(h, t, findCommonVertex(topParentTree, bottomSubTree));
                    setTLVertex(h, t, findCommonVertexExcept(t, topParentTree, getTRVertex(h, t)));
                    setBRVertex(h, t, getTRVertex(h, t));
                    setBLVertex(h, t, findCommonVertexExcept(t, bottomSubTree, getBRVertex(h, t)));
                }


                break;
            case 0b1011:
                // top, bottom and left
                throw new RuntimeException("OEIIIII");
            case 0b1100:
                // top and right
                setTRVertex(h, t, findCommonVertex(topParentTree, rightSubTree));
                setTLVertex(h, t, findCommonVertexExcept(t, topParentTree, getTRVertex(h, t)));
                setBRVertex(h, t, findCommonVertexExcept(t, rightSubTree, getTRVertex(h, t)));
                setBLVertex(h, t, getBRVertex(h, t));
                break;
            case 0b1101:
                // top, right and left
                throw new RuntimeException("STOPPPP");
            case 0b1110:
                // top, right and bottom
                setTLVertex(h, t, findCommonVertex(topParentTree, bottomSubTree));
                setTRVertex(h, t, findCommonVertex(topParentTree, rightSubTree));
                setBLVertex(h, t, findCommonVertex(topParentTree, bottomSubTree));
                setBRVertex(h, t, findCommonVertex(rightSubTree, bottomSubTree));
                break;
            case 0b1111:
                // everything
                throw new RuntimeException("OEPSSSS");
        }
    }

    private static Vertex both(Vertex v1, Vertex v2) {
        if (v1 == null && v2 == null) {
            return null;
        }
        if (v1 == null && v2 != null) {
            return v2;
        }
        if (v2 != null && v1 != v2) {
            throw new RuntimeException("HELP HELP");
        }
        return v1;
    }

    /**
     * Sets the vertex for the TL hexagon of the given tree.
     */
    private static void setTLVertex(HexagonalMap h, Tree t, Vertex v) {
        h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, 3 * -t.yCoordinate), v);
    }

    /**
     * Gets the vertex for the TL hexagon of the given tree.
     */
    private static Vertex getTLVertex(HexagonalMap h, Tree t) {
        return h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, 3 * -t.yCoordinate));
    }

    /**
     * Sets the vertex for the TR hexagon of the given tree.
     */
    private static void setTRVertex(HexagonalMap h, Tree t, Vertex v) {
        h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate), v);
    }

    /**
     * Gets the vertex for the TR hexagon of the given tree.
     */
    private static Vertex getTRVertex(HexagonalMap h, Tree t) {
        return h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate));
    }

    /**
     * Sets the vertex for the BL hexagon of the given tree.
     */
    private static void setBLVertex(HexagonalMap h, Tree t, Vertex v) {
        h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, 3 * -t.yCoordinate - 1), v);
    }

    /**
     * Gets the vertex for the BL hexagon of the given tree.
     */
    private static Vertex getBLVertex(HexagonalMap h, Tree t) {
        return h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, 3 * -t.yCoordinate - 1));
    }

    /**
     * Sets the vertex for the BR hexagon of the given tree.
     */
    private static void setBRVertex(HexagonalMap h, Tree t, Vertex v) {
        h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate - 1), v);
    }

    /**
     * Gets the vertex for the BR hexagon of the given tree.
     */
    private static Vertex getBRVertex(HexagonalMap h, Tree t) {
        return h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, 3 * -t.yCoordinate - 1));
    }

    /**
     * Finds a vertex (color) that is shared by the faces of the given trees.
     *
     * @param t1 The first tree.
     * @param t2 The second tree.
     * @return A vertex <code>v</code> with      <pre>
     * t1.face.vertices.contains(v) && t2.face.vertices.contains(v)
     * </pre> If one doesn't exist, <code>null</code> is returned.
     */
    private static Vertex findCommonVertex(Tree t1, Tree t2) {
        return findCommonVertexExcept(t1, t2, null);
    }

    /**
     * Finds a vertex (color) that is shared by the faces of the given trees.
     * Ignores the vertex given.
     *
     * @param t1 The first tree.
     * @param t2 The second tree.
     * @param except The vertex to ignore.
     * @return A vertex <code>v != except</code> with      <pre>
     * t1.face.vertices.contains(v) && t2.face.vertices.contains(v)
     * </pre> If one doesn't exist, <code>null</code> is returned.
     */
    private static Vertex findCommonVertexExcept(Tree t1, Tree t2, Vertex except) {
        if (t2.face.vertices.get(0) != except
                && t1.face.vertices.contains(t2.face.vertices.get(0))) {
            return t2.face.vertices.get(0);
        }
        if (t2.face.vertices.get(1) != except
                && t1.face.vertices.contains(t2.face.vertices.get(1))) {
            return t2.face.vertices.get(1);
        }
        if (t2.face.vertices.get(2) != except
                && t1.face.vertices.contains(t2.face.vertices.get(2))) {
            return t2.face.vertices.get(2);
        }
        return null;
    }

    /**
     * Finds a vertex (color) that is in the face of one tree, and not in the
     * other.
     *
     * @param t1 The first tree.
     * @param t2 The second tree.
     * @return A vertex <code>v</code> with      <pre>
     * t1.face.vertices.contains(v) && !t2.face.vertices.contains(v)
     * </pre> If one doesn't exist, <code>null</code> is returned.
     */
    private static Vertex findOtherVertex(Tree t1, Tree t2) {
        if (!t2.face.vertices.contains(t1.face.vertices.get(0))) {
            return t1.face.vertices.get(0);
        }
        if (!t2.face.vertices.contains(t1.face.vertices.get(1))) {
            return t1.face.vertices.get(1);
        }
        if (!t2.face.vertices.contains(t1.face.vertices.get(2))) {
            return t1.face.vertices.get(2);
        }
        return null;
    }

    private static void augmentColoring(HexagonalMap h, Tree t) {
        Vertex[] vertices = new Vertex[]{
            getTLVertex(h, t), getTRVertex(h, t), getBLVertex(h, t), getBRVertex(h, t)
        };
        Vertex first = null;
        Vertex second = null;
        Vertex third = null;
        int numNulls = 0;
        for (Vertex v : vertices) {
            if (v == null) {
                numNulls++;
                continue;
            }
            if (first == null) {
                first = v;
            } else if (first != v) {
                if (second == null) {
                    second = v;
                } else if (second != v) {
                    third = v;
                }
            }
        }

        // only continue if we have exactly two colors and 1 unknown
        if (numNulls != 1 || first == null || second == null || third != null) {
            return;
        }

        for (Vertex v : t.face.vertices) {
            if (v != first && v != second) {
                third = v;
                if (v == null) {
                    throw new StackOverflowError("WHAA");
                }
                break;
            }
        }

        if (getTLVertex(h, t) == null) {
            setTLVertex(h, t, third);
        }
        if (getTRVertex(h, t) == null) {
            setTRVertex(h, t, third);
        }
        if (getBLVertex(h, t) == null) {
            setBLVertex(h, t, third);
        }
        if (getBRVertex(h, t) == null) {
            setBRVertex(h, t, third);
        }
    }

    private static void addSubtree(HexagonalMap h, Tree origin, Tree t) {

        // the edge to the subtree
        if (origin.xCoordinate == t.xCoordinate) {

            // the actual subtree: recursive call to addTree()
            addTree(h, t, null, origin);

            // it is a vertical edge
            for (int z = -3 * t.yCoordinate + 1; z <= -3 * origin.yCoordinate - 2; z++) {
                h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, z),
                        h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate, 0, z - 1)));

                h.setVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, z),
                        h.getVertex(new BarycentricCoordinate(3 * t.xCoordinate + 1, 0, z - 1)));
            }

        } else {

            // the actual subtree: recursive call to addTree()
            addTree(h, t, origin, null);

            // it is a horizontal edge
            for (int x = 3 * t.xCoordinate - 1; x >= 3 * origin.xCoordinate + 2; x--) {
                h.setVertex(new BarycentricCoordinate(x, 0, -3 * t.yCoordinate),
                        h.getVertex(new BarycentricCoordinate(x + 1, 0, -3 * t.yCoordinate)));

                h.setVertex(new BarycentricCoordinate(x, 0, -3 * t.yCoordinate - 1),
                        h.getVertex(new BarycentricCoordinate(x + 1, 0, -3 * t.yCoordinate - 1)));
            }
        }
    }
}

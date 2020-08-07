package algorithms.honors;

/**
 * A recursive data structure for a tree.
 */
public class Tree {

    /**
     * If the Tree was added to make the tree complete, or if it was there in
     * the first place. Used by h-v drawing algorithm only.
     */
    public boolean isReal = true;
    /**
     * Maximum levels of recursion for printing a tree.
     */
    public static final int MAX_RECURSION_DEPTH = 5;
    /**
     * Can store the height of this node, counted from the leaves up (longest
     * path to leaf).
     */
    public int height;
    /**
     * The third subtree.
     */
    public Tree subTree3;
    /**
     * The first subtree,
     * <code>null</code> if not existing.
     */
    public Tree subTree1;
    /**
     * The second subtree,
     * <code>null</code> if not existing.
     */
    public Tree subTree2;
    /**
     * The parent tree.
     * {@code null} if not existing or not specified.
     */
    public Tree parentTree;
    /**
     * The face corresponding to this node.
     */
    public Face face;
    /**
     * The eventual x-coordinate.
     */
    public int xCoordinate;
    /**
     * The resulting y-coordinate.
     */
    public int yCoordinate;
    /**
     * Coordinates of O_{h-1} as described in the paper.
     */
    private int oXOld;
    private int oYOld;
    /**
     * Coordinates of O_{h} as described in the paper.
     */
    private int oXNew;
    private int oYNew;
    /**
     * Coordinates of U_{h-1} and U_{h}.
     */
    private int uXOld;
    private int uYOld;
    private int uXNew;
    private int uYNew;

    /**
     * Copy constructor. Does not copy h-v drawing related stuff. Faces are not
     * copied either, but the reference is copied. Coordinates are copied.
     * Subtrees are copied and their parentTree pointer is set to the newly
     * created copy. The parentTree pointer of this Tree is set to the same
     * value as the pointer of the copied Tree: this is hence not copied.
     *
     * <p>Passing the root of a tree will result in a fully copied Tree, as
     * expected. All child nodes are copied as well.
     *
     * @param toCopy Tree to make a copy of.
     */
    public Tree(Tree toCopy) {
        this.xCoordinate = toCopy.xCoordinate;
        this.yCoordinate = toCopy.yCoordinate;

        this.face = toCopy.face;
        this.parentTree = toCopy.parentTree;
        if (toCopy.subTree1 == null) {
            this.subTree1 = null;
        } else {
            this.subTree1 = new Tree(toCopy.subTree1);
            this.subTree1.parentTree = this;
        }
        if (toCopy.subTree2 == null) {
            this.subTree2 = null;
        } else {
            this.subTree2 = new Tree(toCopy.subTree2);
            this.subTree2.parentTree = this;
        }
        if (toCopy.subTree3 == null) {
            this.subTree3 = null;
        } else {
            this.subTree3 = new Tree(toCopy.subTree3);
            this.subTree3.parentTree = this;
        }
    }

    /**
     * Creates a new compound tree.
     *
     * @param subTree1 The first subtree.
     * @param subTree2 The second subtree.
     * @param face The face that is linked to this tree.
     */
    public Tree(Tree subTree1, Tree subTree2, Face face) {
        this.subTree1 = subTree1;
        if (subTree1 != null) {
            subTree1.parentTree = this;
        }
        if (subTree2 != null) {
            subTree2.parentTree = this;
        }
        this.subTree2 = subTree2;
        this.face = face;
    }

    /**
     * Creates a new compound tree.
     *
     * @param subTree1 The first subtree.
     * @param subTree2 The second subtree.
     * @param subTree3 The third subtree - only allowed for the root node!
     */
    public Tree(Tree subTree1, Tree subTree2, Tree subTree3, Face face) {
        this.subTree1 = subTree1;
        this.subTree2 = subTree2;
        this.subTree3 = subTree3;
        if (subTree1 != null) {
            subTree1.parentTree = this;
        }
        if (subTree2 != null) {
            subTree2.parentTree = this;
        }
        if (subTree3 != null) {
            subTree3.parentTree = this;
        }
        this.face = face;
    }

    public void completeTree() {
        // setRealNodes();
        computeHeights();
        completeThisNode(height);
    }

    /**
     * To be called on the root. Computes height of the tree.
     */
    public void computeHeights() {
        if (subTree1 == null && subTree2 == null) {
            height = 0;
        } else {

            if (subTree1 != null) {
                subTree1.computeHeights();
                height = subTree1.height + 1;
            }
            if (subTree2 != null) {
                subTree2.computeHeights();
                if (subTree2.height + 1 > height) {
                    height = subTree2.height + 1;
                }
            }

            if (subTree3 != null) {
                subTree3.computeHeights();
                if (subTree3.height + 1 > height) {
                    height = subTree3.height + 1;
                }
            }
        }
    }

    public static void main(String args[]) {
        Tree tree = new Tree(
                new Tree(new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Face()),
                new Tree(new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Face()),
                new Tree(new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Tree(new Tree(null, null, new Face()), new Tree(null, null, new Face()), new Face()), new Face()),
                new Face());
        //Tree tree = new Tree(new Tree(null, null, new Face()) ,new Tree(null, null, new Face()), new Face());
        tree.computeHeights();
        tree.computeHvDrawing();
        tree.print();
    }

    public void computeHvDrawing() {
        computeHeights();
        // completeTree();

        // computeHeights();
        hvDraw();
        updateCoordinates();
    }

    /**
     * Computes a h-v-drawing for this tree. The result will be stored in the
     * <code>xCoordinate</code> and
     * <code>yCoordinate</code> of the nodes of this tree. For every (sub)tree,
     * except the one for which this algorithm is called, we need
     * <code>tree.subTree3 == null</code>
     */
    public void computeCompleteHvDrawing() {
        completeTree();
        computeHeights();
        hvDrawComplete();
        if (subTree3 != null) {
            subTree3.hvDrawComplete();
            subTree3.mirrorInYAndShiftLeft();
        }
        updateCoordinates();
    }

    private void shift(int x, int y) {
        oXNew = oXOld + x;
        oYNew = oYOld + y;
        if (subTree1 != null) {
            subTree1.shift(x, y);
        }
        if (subTree2 != null) {
            subTree2.shift(x, y);
        }
    }

    /**
     * Returns [width, height] of a drawn subtree.
     */
    private int[] hvDraw() {
        oXNew = 0;
        oXOld = 0;
        oYOld = 0;
        oYNew = 0;

        if (subTree1 != null && subTree2 != null) {
            int[] dim1 = subTree1.hvDraw();
            int[] dim2 = subTree2.hvDraw();

            //[o width, o height, u width, u height]
            // Please note dim2 may be needed later, when we actually CARE about
            // the order we insert the subtrees!
            if (dim1[0] >= dim2[0]) {
                subTree2.shift(1, 0);
                subTree1.shift(0, dim2[1] + 1);

                setToOld();
                return new int[]{Math.max(dim2[0] + 1, dim1[0]), dim2[1] + dim1[1] + 1};
            } else {
                subTree1.shift(1, 0);
                subTree2.shift(0, dim1[1] + 1);

                setToOld();
                return new int[]{Math.max(dim1[0] + 1, dim2[0]), dim1[1] + dim2[1] + 1};

            }
        } else if (subTree1 != null) {//subTree2 = null
            int[] dim1 = subTree1.hvDraw();
            subTree1.shift(0, 1);
            setToOld();
            return new int[]{dim1[0], dim1[1] + 1};


        } else if (subTree2 != null) {
            int[] dim2 = subTree2.hvDraw();
            subTree2.shift(0, 1);
            setToOld();
            return new int[]{dim2[0], dim2[1] + 1};
        } else {
            return new int[]{0, 0};
        }

    }

    /**
     * Will give a h-v-drawing of this tree, if called on the root node. Returns
     * width and height of previous drawing, as [o width, o height, u width, u
     * height]. Ignores
     * <code>subTree3</code>. Implemented as in "minimum area hv drawings"
     */
    private int[] hvDrawComplete() {
        oXNew = 0;
        oXOld = 0;
        oYOld = 0;
        oYNew = 0;
        uYOld = 0;
        uYNew = 0;
        uXOld = 0;
        uXNew = 0;

        if (height <= 2) {
            // Oh = delta 3
            // Uh = delta 3 rotated
            if (subTree1 != null) {
                subTree1.oXNew = 0;
                subTree1.oXOld = 0;
                subTree1.oYOld = 1;
                subTree1.oYNew = 1;

                subTree1.uXNew = 0;
                subTree1.uXOld = 0;
                subTree1.uYOld = 2;
                subTree1.uYNew = 2;

                if (subTree1.subTree1 != null) {
                    subTree1.subTree1.oXNew = 0;
                    subTree1.subTree1.oXOld = 0;
                    subTree1.subTree1.oYOld = 2;
                    subTree1.subTree1.oYNew = 2;

                    subTree1.subTree1.uXNew = 0;
                    subTree1.subTree1.uXOld = 0;
                    subTree1.subTree1.uYOld = 3;
                    subTree1.subTree1.uYNew = 3;

                }
                if (subTree1.subTree2 != null) {
                    subTree1.subTree2.oXNew = 1;
                    subTree1.subTree2.oXOld = 1;
                    subTree1.subTree2.oYOld = 1;
                    subTree1.subTree2.oYNew = 1;

                    subTree1.subTree2.uXNew = 1;
                    subTree1.subTree2.uXOld = 1;
                    subTree1.subTree2.uYOld = 2;
                    subTree1.subTree2.uYNew = 2;
                }

            }
            // Many cases..
            if (subTree2 != null) {
                subTree2.oXNew = 2;
                subTree2.oXOld = 2;
                subTree2.oYOld = 0;
                subTree2.oYNew = 0;

                subTree2.uXNew = 1;
                subTree2.uXOld = 1;
                subTree2.uYOld = 0;
                subTree2.uYNew = 0;
                if (subTree2.subTree2 != null) {
                    subTree2.subTree2.oXNew = 3;
                    subTree2.subTree2.oXOld = 3;
                    subTree2.subTree2.oYOld = 0;
                    subTree2.subTree2.oYNew = 0;

                    subTree2.subTree2.uXNew = 2;
                    subTree2.subTree2.uXOld = 2;
                    subTree2.subTree2.uYOld = 0;
                    subTree2.subTree2.uYNew = 0;
                }
                if (subTree2.subTree1 != null) {
                    subTree2.subTree1.oXNew = 2;
                    subTree2.subTree1.oXOld = 2;
                    subTree2.subTree1.oYOld = 1;
                    subTree2.subTree1.oYNew = 1;

                    subTree2.subTree1.uXNew = 1;
                    subTree2.subTree1.uXOld = 1;
                    subTree2.subTree1.uYOld = 1;
                    subTree2.subTree1.uYNew = 1;
                }
            }

            return new int[]{3, 2, 2, 3};
        } else {
            if (subTree1 != null && subTree2 != null) {
                int[] dim1 = subTree1.hvDrawComplete();
                int[] dim2 = subTree2.hvDrawComplete();

                //[o width, o height, u width, u height]
                // Please note dim2 may be needed later, when we actually CARE about
                // the order we insert the subtrees!
                if (!vertical()) {
                    subTree1.shiftAndMirrorUtoO(0, 1);
                    subTree2.shiftAndMirrorOtoO(dim1[3] + 1, 0);

                    subTree1.shiftAndMirrorOtoU(0, 1);
                    subTree2.shiftAndMirrorOtoU(dim1[1] + 1, 0);
                    setToOld();
                    return new int[]{dim1[3] + dim2[1] + 1, Math.max(dim1[2] + 1, dim2[0]), dim1[1] + 1 + dim2[1], Math.max(dim1[0] + 1, dim2[0])};


                } else {
                    subTree2.shiftAndMirrorUtoO(0, 1);
                    subTree1.shiftAndMirrorOtoO(dim2[3] + 1, 0);
                    subTree2.shiftAndMirrorOtoU(0, 1);
                    subTree1.shiftAndMirrorOtoU(dim2[1] + 1, 0);
                    setToOld();
                    return new int[]{dim2[3] + dim1[1] + 1, Math.max(dim2[2] + 1, dim1[0]), dim2[1] + 1 + dim1[1], Math.max(dim2[0] + 1, dim1[0])};

                }


                // return the new dimensions

            } /*
             * else if (subTree1 != null && subTree2 == null) { int[] dim1 =
             * subTree1.hvDraw(); subTree1.shiftAndMirrorUtoO(0, 1);
             * subTree1.shiftAndMirrorOtoU(0, 1); //set old values to newly
             * computed ones setToOld(); //return new dimension. return new
             * int[]{dim1[3] + 1, dim1[2] + 1, dim1[1] + 1, dim1[0] + 1}; } else
             * { //if (subTree1 == null && subTree2 != null) { int[] dim2 =
             * subTree2.hvDraw(); subTree1.shiftAndMirrorUtoO(1, 0);
             * subTree1.shiftAndMirrorOtoU(1, 0);
             *
             * setToOld(); return new int[]{dim2[1] + 1, dim2[0] + 1, dim2[1] +
             * 1, dim2[0] + 1}; }
             */
        }
        return null;
    }

    /**
     * Prints some representation of this tree.
     */
    public void print() {
        System.out.println("{" + height + ": " + face.toString() + "(" + xCoordinate + "," + yCoordinate + ")");
        if (subTree1 != null) {
            subTree1.print();
        }
        if (subTree2 != null) {
            subTree2.print();
        }
        if (subTree3 != null) {
            subTree3.print();
        }
        System.out.println("}");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String prefix = "  ";
        sb.append(newLine);
        toStringHelper(sb, prefix, newLine, 0);
        sb.append(newLine);
        return sb.toString();
    }

    private void toStringHelper(StringBuilder sb, String prefix, String newLine, int recursionDepth) {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            sb.append(prefix + "[ Tree ... ]" + newLine);
            return;
        }

        sb.append(prefix + "[ Tree");
        sb.append(newLine);

        sb.append(prefix + "  Face (" + this.face.ID + ") : ");
        sb.append(this.face.toString());
        sb.append(newLine);

        sb.append(prefix + "  Parent Face (" + (this.parentTree == null ? "-" : this.parentTree.face.ID) + ") : ");
        if (this.parentTree == null) {
            sb.append("null");
        } else {
            sb.append(this.parentTree.face.toString());
        }
        sb.append(newLine);

        sb.append(prefix + "  SubTree 1 : ");
        if (this.subTree1 == null) {
            sb.append("null");
        } else {
            sb.append(newLine);
            this.subTree1.toStringHelper(sb, prefix + "  ", newLine, recursionDepth + 1);
        }
        sb.append(newLine);

        sb.append(prefix + "  SubTree 2 : ");
        if (this.subTree2 == null) {
            sb.append("null");
        } else {
            sb.append(newLine);
            this.subTree2.toStringHelper(sb, prefix + "  ", newLine, recursionDepth + 1);
        }
        sb.append(newLine);

        sb.append(prefix + "  SubTree 3 : ");
        if (this.subTree3 == null) {
            sb.append("null");
        } else {
            sb.append(newLine);
            this.subTree3.toStringHelper(sb, prefix + "  ", newLine, recursionDepth + 1);
        }
        sb.append(newLine);

        sb.append(prefix + "]");
    }

    /**
     * For hvdraw(). Updates from step h-1 to step h.
     */
    private void setToOld() {
        oXOld = oXNew;
        oYOld = oYNew;
        uXOld = uXNew;
        uYOld = uYNew;
        if (subTree1 != null) {
            subTree1.setToOld();
        }
        if (subTree2 != null) {
            subTree2.setToOld();
        }

    }

    /**
     * Update O coordinates by previous U-coordinates (mirrored) and shifts by
     * (x,y)
     *
     * @param x Shift in x direction
     * @param y Shift in y direction.
     */
    private void shiftAndMirrorUtoO(int x, int y) {
        oXNew = uYOld + x;
        oYNew = uXOld + y;
        if (subTree1 != null) {
            subTree1.shiftAndMirrorUtoO(x, y);
        }
        if (subTree2 != null) {
            subTree2.shiftAndMirrorUtoO(x, y);
        }
    }

    /**
     * Help function for hvdraw. Copies old O-coordinates to new O-coordinates,
     * then mirrors and then shifts.
     *
     * @param x
     * @param y
     */
    private void shiftAndMirrorOtoO(int x, int y) {
        oXNew = oYOld + x;
        oYNew = oXOld + y;
        if (subTree1 != null) {
            subTree1.shiftAndMirrorOtoO(x, y);
        }
        if (subTree2 != null) {
            subTree2.shiftAndMirrorOtoO(x, y);
        }
    }

    /**
     * Help function for hvdraw. Copies old O-coordinates to new U-coordinates,
     * then mirrors and then shifts.
     *
     * @param x
     * @param y
     */
    private void shiftAndMirrorOtoU(int x, int y) {
        uXNew = oYOld + x;
        uYNew = oXOld + y;
        if (subTree1 != null) {
            subTree1.shiftAndMirrorOtoU(x, y);
        }
        if (subTree2 != null) {
            subTree2.shiftAndMirrorOtoU(x, y);
        }
    }

    /**
     * Sets computed coordinates to the x and y coordinate.
     */
    private void updateCoordinates() {
        xCoordinate = oXNew;
        yCoordinate = oYNew;
        oXNew = 0;
        oXOld = 0;
        oYOld = 0;
        oYNew = 0;
        uYOld = 0;
        uYNew = 0;
        uXOld = 0;
        uXNew = 0;
        if (subTree1 != null) {
            subTree1.updateCoordinates();
        }
        if (subTree2 != null) {
            subTree2.updateCoordinates();
        }

        if (subTree3 != null) {
            subTree3.updateCoordinates();
        }
    }

    /**
     * Used to put the third subtree left of the current drawing.
     */
    private void mirrorInYAndShiftLeft() {
        oXNew = -oXNew - 1;
        if (subTree1 != null) {
            subTree1.mirrorInYAndShiftLeft();
        }
        if (subTree2 != null) {
            subTree2.mirrorInYAndShiftLeft();
        }
    }

    private boolean vertical() {
        if (subTree1 != null && subTree1.subTree1 != null) {
            return subTree1.oXOld == subTree1.subTree1.oXOld;
        }
        if (subTree1 != null && subTree1.subTree2 != null) {
            return subTree1.oYOld == subTree1.subTree2.oYOld;
        }
        if (subTree2 != null && subTree2.subTree1 != null) {
            return subTree2.oXOld == subTree2.subTree1.oXOld;
        }
        if (subTree2 != null && subTree2.subTree2 != null) {
            return subTree2.oYOld == subTree2.subTree2.oYOld;
        }

        //cannot happen
        return false;
    }

    private void completeThisNode(int h) {
        if (h == 0) {
            return;
        }
        if (subTree1 == null) {
            subTree1 = new Tree(null, null, null);
            subTree1.isReal = false;
        }
        //if (subTree1.height < h - 1) {
        subTree1.completeThisNode(h - 1);
        //}
        if (subTree2 == null) {
            subTree2 = new Tree(null, null, null);
            subTree2.isReal = false;
        }
        subTree2.completeThisNode(h - 1);


    }
}

package algorithms.honors;

/**
 * This class contains static methods to layout {@link Tree Trees} in a special
 * way, namely as an h-v drawing. The algorithms are taken from "A note on
 * optimal area algorithms for upward drawings of binary trees" from Crescenzi
 * et al., from 1992.
 * 
 * <p>Note: in their paper, the authors suggest that it would be "trivial" to
 * implement the algorithm in linear time and space. I as an implementer was
 * not sure how to do that, so the implementation given here is quadratic. I
 * do not see how one would be able to do the layout and moving of subtrees to
 * the correct position in one go...
 */
public class TreeToHVDrawing {

    /**
     * Compute an h-v drawing of O(n log n) area of the given tree. This is the
     * Algorithm BT from the paper.
     * 
     * @param t Tree to compute the h-v drawing of.
     * @return The width and height of the computed drawing, in that order.
     */
    public static int[] layoutBinaryTree(Tree t) {
        if (t == null) {
            return new int[] {0, 0};
        }
        
        // position root
        t.xCoordinate = 0;
        t.yCoordinate = 0;
        
        // leaf special case
        if (t.subTree1 == null && t.subTree2 == null) {
            return new int[] {1, 1}; 
        }
        
        // position subtrees
        int[] tree1Size = layoutBinaryTree(t.subTree1);
        int[] tree2Size = layoutBinaryTree(t.subTree2);
        return composeVertical(t.subTree1, t.subTree2, tree1Size, tree2Size);
    }
    
    /**
     * Compose two trees horizontally, so put one next to the other. The tallest
     * subtree is put at the right.
     * 
     * @param subTree1 First subtree.
     * @param subTree2 Second subtree.
     * @param tree1Size Size of first subtree.
     * @param tree2Size Size of second subtree.
     * @return The width and height of the computed composition, in that order.
     */
    private static int[] composeHorizontal(Tree subTree1, Tree subTree2,
            int[] tree1Size, int[] tree2Size) {
        if (tree1Size[1] >= tree2Size[1]) {
            // horizontal layout, tree2 on the left
            int tree2Width = Math.max(tree2Size[0], 1);
            shiftTree(subTree2, 0, 1);
            shiftTree(subTree1, tree2Width, 0);
            return new int[] {tree1Size[0] + tree2Width, (tree1Size[1] >
                    tree2Size[1] ? tree1Size[1] : tree1Size[1] + 1)};
        }
        // horizontal layout, tree1 on the left
        int tree1Width = Math.max(tree1Size[0], 1);
        shiftTree(subTree1, 0, 1);
        shiftTree(subTree2, tree1Width, 0);
        return new int[] {tree1Width + tree2Size[0], tree2Size[1]};
    }
    
    /**
     * Compose two trees vertically, so put one above the other. The widest
     * subtree is put at the bottom.
     * 
     * @param subTree1 First subtree.
     * @param subTree2 Second subtree.
     * @param tree1Size Size of first subtree.
     * @param tree2Size Size of second subtree.
     * @return The width and height of the computed composition, in that order.
     */
    private static int[] composeVertical(Tree subTree1, Tree subTree2,
            int[] tree1Size, int[] tree2Size) {
        if (tree1Size[0] >= tree2Size[0]) {
            // vertical layout, tree2 on top
            int tree2Height = Math.max(tree2Size[1], 1);
            shiftTree(subTree2, 1, 0);
            shiftTree(subTree1, 0, tree2Height);
            return new int[] {(tree1Size[0] > tree2Size[0] ? tree1Size[0]
                    : tree1Size[0] + 1), tree1Size[1] + tree2Height};
        }
        // vertical layout, tree1 on top
        int tree1Height = Math.max(tree1Size[1], 1);
        shiftTree(subTree1, 1, 0);
        shiftTree(subTree2, 0, tree1Height);
        return new int[] {tree2Size[0], tree1Height + tree2Size[1]};
    }

    /**
     * Compute an h-v drawing of O(n) area of the given complete tree. This is
     * the Algorithm CT from the paper.
     * 
     * @param t Tree to compute the h-v drawing of.
     * @return The width and height of the computed drawing, in that order.
     */
    public static int[] layoutCompleteBinaryTree(Tree t) {
        return layoutCompleteBinaryTree(t, 0);
    }
    
    /**
     * Compute an h-v drawing of O(n) area of the given complete tree. This is
     * the Algorithm CT from the paper. The index {@code i} is the recursion
     * depth and should initally be {@code 0}. The order of putting together
     * the subtrees depends on it.
     * 
     * @param t Tree to layout.
     * @param i Recursion depth.
     * @return The width and height of the computed drawing, in that order.
     */
    private static int[] layoutCompleteBinaryTree(Tree t, int i) {
        if (t == null) {
            return new int[] {0, 0};
        }
        
        // position root
        t.xCoordinate = 0;
        t.yCoordinate = 0;
        
        // leaf special case
        if (t.subTree1 == null && t.subTree2 == null) {
            return new int[] {1, 1}; 
        }
        
        // position subtrees
        int[] tree1Size = layoutCompleteBinaryTree(t.subTree1, i + 1);
        int[] tree2Size = layoutCompleteBinaryTree(t.subTree2, i + 1);
        if (i % 2 == 0) {
            return composeHorizontal(t.subTree1, t.subTree2, tree1Size, tree2Size);
        }
        return composeVertical(t.subTree1, t.subTree2, tree1Size, tree2Size);
    }
    
    /**
     * Shift all coordinates of the given tree with the given X- and Y-
     * coordinate. That is, the given {@code dx} is added up to the X-coordinate
     * of the given tree and all of its subtrees and similarly, {@code dy} is
     * added up to all Y-coordinates.
     * 
     * @param t Tree to shift.
     * @param dx Value to shift X-coordinate with.
     * @param dy Value to shift Y-coordinate with.
     */
    public static void shiftTree(Tree t, int dx, int dy) {
        if (t == null) {
            return;
        }
        
        t.xCoordinate += dx;
        t.yCoordinate += dy;
        shiftTree(t.subTree1, dx, dy);
        shiftTree(t.subTree2, dx, dy);
    }
}

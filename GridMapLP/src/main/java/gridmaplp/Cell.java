/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridmaplp;

import java.awt.Color;

/**
 *
 * @author msondag
 */
class Cell {

    String label;
    String province = "Empty";
    Color color;
    MosaicCell mosaicCell = null;

    public Cell() {
        label = "";
        color = Color.WHITE;
    }

    public Cell(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public Cell(MosaicCell c) {
        label = c.label;
        color = Color.WHITE;
        mosaicCell = c;
    }

    public double squaredDistance(Coordinate c) {
        return (mosaicCell.x - c.x) * (mosaicCell.x - c.x) + (mosaicCell.y - c.y) * (mosaicCell.y - c.y);

    }
}

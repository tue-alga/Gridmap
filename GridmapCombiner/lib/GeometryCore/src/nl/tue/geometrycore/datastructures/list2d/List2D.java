/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.list2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class List2D<T> implements Iterable<T> {

    private final List<List<T>> data;
    private final boolean columnMajor;
    private int columns;
    private int rows;

    public List2D() {
        this(0, 0, true);
    }

    public List2D(int columns, int rows) {
        this(columns, rows, true);
    }

    public List2D(int columns, int rows, boolean columnMajor) {
        this.data = new ArrayList();
        this.columnMajor = columnMajor;
        this.columns = columns;
        this.rows = rows;

        if (columnMajor) {
            for (int c = 0; c < columns; c++) {
                data.add(new ArrayList());
                for (int r = 0; r < rows; r++) {
                    data.get(c).add(null);
                }
            }
        } else {
            for (int r = 0; r < rows; r++) {
                data.add(new ArrayList());
                for (int c = 0; c < columns; c++) {
                    data.get(r).add(null);
                }
            }
        }
    }

    public boolean isValidIndex(int column, int row) {
        return 0 <= column && column < columns
                && 0 <= row && row < rows;
    }

    public T get(int column, int row) {
        if (columnMajor) {
            return data.get(column).get(row);
        } else {
            return data.get(row).get(column);
        }
    }

    public void set(int column, int row, T value) {
        if (columnMajor) {
            data.get(column).set(row, value);
        } else {
            data.get(row).set(column, value);
        }
    }

    public void setToNull() {
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                set(c, r, null);
            }
        }
    }

    public void clear() {
        data.clear();
        rows = 0;
        columns = 0;
    }

    public void addColumn() {
        addColumn(columns);
    }

    public void addColumn(int before) {
        if (columnMajor) {
            data.add(before, new ArrayList());
            for (int r = 0; r < rows; r++) {
                data.get(before).add(null);
            }
        } else {
            for (int r = 0; r < rows; r++) {
                data.get(r).add(before, null);
            }
        }
        columns++;
    }

    public void removeColumn() {
        removeColumn(columns - 1);
    }

    public void removeColumn(int column) {
        if (columnMajor) {
            data.remove(column);
        } else {
            for (int r = 0; r < rows; r++) {
                data.get(r).remove(column);
            }
        }
        column--;
    }

    public void addRow() {
        addRow(rows);
    }

    public void addRow(int before) {
        if (columnMajor) {
            for (int c = 0; c < columns; c++) {
                data.get(c).add(before, null);
            }
        } else {
            data.add(before, new ArrayList());
            for (int c = 0; c < columns; c++) {
                data.get(before).add(null);
            }
        }
        rows++;
    }

    public void removeRow() {
        removeRow(rows - 1);
    }

    public void removeRow(int row) {
        if (columnMajor) {
            for (int c = 0; c < columns; c++) {
                data.get(c).remove(row);
            }
        } else {
            data.remove(row);
        }
        rows--;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        while (columns < this.columns) {
            removeColumn();
        }
        while (columns > this.columns) {
            addColumn();
        }
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        while (rows < this.rows) {
            removeRow();
        }
        while (rows > this.rows) {
            addRow();
        }
        this.rows = rows;
    }

    public Iterable<T> perColumn() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return perColumnIterator();
            }
        };
    }

    public Iterable<T> perRow() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return perRowIterator();
            }
        };
    }
    
    public void forEachEntry(EntryAction<T> action) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                action.action(col, row, get(col, row));
            }
        }
    }

    public Iterator<T> perColumnIterator() {
        return new GridIterator(false);
    }

    public Iterator<T> perRowIterator() {
        return new GridIterator(true);
    }

    @Override
    public Iterator<T> iterator() {
        return new GridIterator(columnMajor);
    }

    public class GridIterator implements Iterator<T> {

        private int thisrow = -1;
        private int thiscolumn = -1;
        private int nextrow = 0;
        private int nextcolumn = 0;
        private final boolean perRow;

        public GridIterator(boolean perRow) {
            this.perRow = perRow;
        }

        @Override
        public boolean hasNext() {
            return nextrow < rows && nextcolumn < columns;
        }

        @Override
        public T next() {
            thisrow = nextrow;
            thiscolumn = nextcolumn;
            if (perRow) {
                nextcolumn++;
                if (nextcolumn >= columns) {
                    nextcolumn = 0;
                    nextrow++;
                }
            } else {
                nextrow++;
                if (nextrow >= rows) {
                    nextrow = 0;
                    nextcolumn++;
                }
            }
            return get(thiscolumn, thisrow);
        }

        @Override
        public void remove() {
            set(thiscolumn, thisrow, null);
        }

        public int getCurrentColumn() {
            return thiscolumn;
        }

        public int getCurrentRow() {
            return thisrow;
        }
    }
}

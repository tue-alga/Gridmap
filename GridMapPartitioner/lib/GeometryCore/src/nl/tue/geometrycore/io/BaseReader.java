/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a base interface to read files that somehow store
 * geometric shapes. Derived classes will allow reading specific file formats.
 * Note that instances of the reader must be closed to handle the closing of any
 * underlying files appropriately.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class BaseReader implements AutoCloseable {

//<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Reads all geometry information in this reader and returns these as a
     * list.
     *
     * @return new list with items of the reader
     *
     * @throws IOException
     */
    public List<ReadItem> read() throws IOException {
        List<ReadItem> items = new ArrayList();
        read(items);
        return items;
    }

    /**
     * Reads all geometry information in this reader and adds these to the
     * provided list.
     *
     * @param items list to which any geometric objects should be added
     * @throws IOException
     */
    public abstract void read(List<ReadItem> items) throws IOException;

    /**
     * Closes the writer.
     *
     * @throws IOException
     */
    @Override
    public abstract void close() throws IOException;
    //</editor-fold>

}

/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.priorityqueue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Provides a priority queue on a custom comparator using indexable items. By
 * using indexation, some methods can be done more efficiently. The ordering is
 * based on a comparator. The smallest value according to this comparator is
 * treated as the highest priority.
 * 
 * Implementation partially based on {@link java.util.PriorityQueue}.
 *
 * @param <T> the class of objects stored in the priority queue
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IndexedPriorityQueue<T extends Indexable> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Indexable[] _queue;
    private int _size;
    private final Comparator<? super T> _comparator;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a priority queue for the given objects. Note that this object
     * runs in linear time and is thus faster than creating an empty queue and
     * adding the items individually.
     *
     * @param objects initial objects for the queue
     * @param comparator comparator deciding on the priority order
     */
    public IndexedPriorityQueue(T[] objects, Comparator<? super T> comparator) {
        _queue = objects;
        _comparator = comparator;
        _size = objects.length;
        heapify();
    }

    /**
     * Constructs an empty priority queue.
     *
     * @param initialCapacity number of objects it can store before the internal
     * storage must be extended
     * @param comparator comparator deciding on the priority order
     */
    public IndexedPriorityQueue(int initialCapacity, Comparator<? super T> comparator) {
        _queue = new Indexable[initialCapacity];
        _comparator = comparator;
        _size = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Converts the queue contents to an list.
     *
     * @return new list with the contents of this queue
     */
    public List<T> extractContents() {
        return (List) Arrays.asList(_queue).subList(0, _size);
    }

    /**
     * Returns the first element in the queue but does not remove it.
     *
     * @return the first element in the queue
     */
    public T peek() {
        if (_size == 0) {
            return null;
        }
        return (T) _queue[0];
    }

    /**
     * Checks whether the element is present in the current queue.
     *
     * @param element element to check
     * @return whether the provided element is stored in this queue
     */
    public boolean contains(T element) {
        int index = element.getIndex();
        if (index < 0) {
            return false;
        } else if (index < _size) {
            return _queue[index] == element;
        } else {
            return false;
        }
    }

    /**
     * Returns the number of items in this queue.
     *
     * @return size of the queue
     */
    public int size() {
        return _size;
    }

    /**
     * Checks whether the queue is empty.
     *
     * @return whether the queue size is zero
     */
    public boolean isEmpty() {
        return _size == 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Adds the provided element to the priority queue.
     *
     * @param element the new element
     */
    public void add(T element) {
        if (element == null) {
            throw new NullPointerException();
        }
        int i = _size;
        if (i >= _queue.length) {
            grow(i + 1);
        }
        _size = i + 1;
        if (i == 0) {
            _queue[0] = element;
            element.setIndex(0);
        } else {
            siftUp(i, element);
        }
    }

    /**
     * Notifies the queue of a change in priority of the provided element.
     *
     * @param element element with a changed priority
     */
    public void priorityChanged(T element) {
        siftUp(element.getIndex(), element);
        siftDown(element.getIndex(), element);
    }

    /**
     * Notifies the queue that the priority of the provided element has
     * increased.
     *
     * @param element element with increased priority
     */
    public void priorityIncreased(T element) {
        siftUp(element.getIndex(), element);
    }

    /**
     * Notifies the queue that the priority of the provided element has
     * decreased.
     *
     * @param element element with decreased priority
     */
    public void priorityDecreased(T element) {
        siftDown(element.getIndex(), element);
    }

    /**
     * Removes the provided element from the queue
     *
     * @param element element to be removed
     * @return whether the element was contained in the list
     */
    public boolean remove(T element) {
        if (contains(element)) {
            removeAt(element.getIndex());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all items from this queue.
     */
    public void clear() {
        for (int i = 0; i < _size; i++) {
            _queue[i].setIndex(-1);
            _queue[i] = null;
        }
        _size = 0;
    }

    /**
     * Removes and returns the first item in this queue.
     *
     * @return
     */
    public T poll() {
        if (_size == 0) {
            return null;
        }
        int s = --_size;
        T result = (T) _queue[0];
        result.setIndex(-1);

        T x = (T) _queue[s];
        _queue[s] = null;
        if (s != 0) {
            siftDown(0, x);
        }
        return result;
    }

    /**
     * Removes the item at the provided index
     *
     * @param index index of the item to be removed
     * @return the removed item
     */
    private T removeAt(int index) {
        assert index >= 0 && index < _size;

        ((T) _queue[index]).setIndex(-1);

        int s = --_size;
        if (s == index) // removed last element
        {
            _queue[index] = null;
        } else {
            T moved = (T) _queue[s];
            _queue[s] = null;
            siftDown(index, moved);
            if (_queue[index] == moved) {
                siftUp(index, moved);
                if (_queue[index] != moved) {
                    return moved;
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">    
    private void siftUp(int k, T x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            T e = (T) _queue[parent];
            if (_comparator.compare(x, e) >= 0) {
                break;
            }
            _queue[k] = e;
            e.setIndex(k);
            k = parent;
        }
        _queue[k] = x;
        x.setIndex(k);
    }

    private void siftDown(int k, T x) {
        int half = _size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            T c = (T) _queue[child];
            int right = child + 1;
            if (right < _size && _comparator.compare(c, (T) _queue[right]) > 0) {
                c = (T) _queue[child = right];
            }
            if (_comparator.compare(x, c) <= 0) {
                break;
            }
            _queue[k] = c;
            c.setIndex(k);
            k = child;
        }
        _queue[k] = x;
        x.setIndex(k);
    }

    private void heapify() {
        for (int i = _size - 1; i > (_size >>> 1) - 1; i--) {
            _queue[i].setIndex(i);
        }
        for (int i = (_size >>> 1) - 1; i >= 0; i--) {
            siftDown(i, (T) _queue[i]);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STORAGE MANAGEMENT">
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        int oldCapacity = _queue.length;
        // Double size if small; else grow by 50%
        int newCapacity = oldCapacity + ((oldCapacity < 64)
                ? (oldCapacity + 2)
                : (oldCapacity >> 1));
        // overflow-conscious code
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        _queue = Arrays.copyOf(_queue, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
        {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE)
                ? Integer.MAX_VALUE
                : MAX_ARRAY_SIZE;
    }
    //</editor-fold>
}

/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.doublylinkedlist;

import java.util.Iterator;

/**
 * A true doubly linked list. Note that it requires the stored objects to
 * inherit from {@link DoublyLinkedListItem}. These objects can be stored in at
 * most one list at a time. Calling methods on a list with objects from another
 * list may result incorrect behavior.
 *
 * @param <TItem> class of items that are contained in the list
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DoublyLinkedList<TItem extends DoublyLinkedListItem<TItem>> implements Iterable<TItem> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private TItem _head, _tail;
    private int _size;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty list.
     */
    public DoublyLinkedList() {
        _head = null;
        _tail = null;
        _size = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Returns the number of elements currently in the list.
     *
     * @return size of the list
     */
    public int size() {
        return _size;
    }

    /**
     * Returns the first element in the list. Returns null if the list is empty.
     *
     * @return the first element
     */
    public TItem getFirst() {
        return _head;
    }

    /**
     * Returns the last element in the list. Returns null if the list is empty.
     *
     * @return the last element
     */
    public TItem getLast() {
        return _tail;
    }

    /**
     * Returns an iterator that loops over the list from head to tail.
     *
     * @return iterator for this list
     */
    @Override
    public Iterator<TItem> iterator() {
        final DoublyLinkedList<TItem> thislist = this;
        return new Iterator<TItem>() {

            TItem curr = null;
            TItem next = _head;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public TItem next() {
                curr = next;
                next = next.getNext();
                return curr;
            }

            @Override
            public void remove() {
                assert curr != null;
                thislist.remove(curr);
                curr = null;
            }
        };
    }

    /**
     * Returns an iterator that loops over the list from tail to head.
     *
     * @return backward iterator for this list
     */
    public Iterator<TItem> backwardIterator() {
        final DoublyLinkedList<TItem> thislist = this;
        return new Iterator<TItem>() {

            TItem curr = null;
            TItem next = _tail;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public TItem next() {
                curr = next;
                next = next.getPrevious();
                return curr;
            }

            @Override
            public void remove() {
                assert curr != null;
                thislist.remove(curr);
                curr = null;
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _size + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">  
    /**
     * Adds the given item to the front of the list.
     *
     * @param item item to put at the head of the list
     */
    public void addFirst(TItem item) {
        assert !item.isContainedInList() : "Trying to add item to doubly linked list, but it is already in one.";

        item.setContainedInList(true);
        if (_size == 0) {
            _head = item;
            _tail = item;
        } else {
            _head.setPrevious(item);
            item.setNext(_head);
            _head = item;
        }
        _size++;
    }

    /**
     * Adds the given item to the back of the list.
     *
     * @param item item to put the at the tail of the list
     */
    public void addLast(TItem item) {
        assert !item.isContainedInList() : "Trying to add item to doubly linked list, but it is already in one.";

        item.setContainedInList(true);
        if (_size == 0) {
            _head = item;
            _tail = item;
        } else {
            _tail.setNext(item);
            item.setPrevious(_tail);
            _tail = item;
        }
        _size++;
    }

    /**
     * Adds a new item into the list, before the specified item. If this
     * specified item is null, the new item is placed at the front of the list.
     *
     * @param newitem new item to be added
     * @param before item before which the new one is placed
     */
    public void addBefore(TItem newitem, TItem before) {
        assert !newitem.isContainedInList() : "Trying to add item to doubly linked list, but it is already in one.";

        if (before == null || before.getPrevious() == null) {
            addFirst(newitem);
        } else {
            TItem prev = before.getPrevious();

            prev.setNext(newitem);
            newitem.setNext(before);

            before.setPrevious(newitem);
            newitem.setPrevious(prev);

            _size++;
        }
    }

    /**
     * Adds a new item into the list, after the specified item. If this
     * specified item is null, the new item is placed at the end of the list.
     *
     * @param newitem new item to be added
     * @param after item after which the new one is placed
     */
    public void addAfter(TItem newitem, TItem after) {
        assert !newitem.isContainedInList() : "Trying to add item to doubly linked list, but it is already in one.";

        if (after == null || after.getNext() == null) {
            addLast(newitem);
        } else {
            TItem next = after.getNext();

            after.setNext(newitem);
            newitem.setNext(next);

            next.setPrevious(newitem);
            newitem.setPrevious(after);

            _size++;
        }
    }

    /**
     * Puts all items in the provided list in front of this list. The provided
     * list is cleared in the process. This takes constant time.
     *
     * @param otherlist list to append to this list
     */
    public void addAllFirst(DoublyLinkedList<? extends TItem> otherlist) {
        if (otherlist._size == 0) {
            // nothing to do
        } else {
            if (_size == 0) {
                // this list is empty
                _head = otherlist._head;
            } else {
                _head.setPrevious(otherlist._tail);
                otherlist._tail.setNext(_head);
            }
            _head = otherlist._head;
            _size += otherlist._size;

            otherlist._size = 0;
            otherlist._head = null;
            otherlist._tail = null;
        }
    }

    /**
     * Appends all items in the provided list to this list at the end. The
     * provided list is cleared in the process. This takes constant time.
     *
     * @param otherlist list to append to this list
     */
    public void addAllLast(DoublyLinkedList<? extends TItem> otherlist) {
        if (otherlist._size == 0) {
            // nothing to do
        } else {
            if (_size == 0) {
                // this list is empty
                _head = otherlist._head;
            } else {
                _tail.setNext(otherlist._head);
                otherlist._head.setPrevious(_tail);
            }
            _tail = otherlist._tail;
            _size += otherlist._size;

            otherlist._size = 0;
            otherlist._head = null;
            otherlist._tail = null;
        }
    }

    /**
     * Inserts all items in the provided list into the list, before the
     * specified item. If this specified item is null, all are placed at the
     * front of the list. The provided list is cleared in the process. This
     * takes constant time.
     *
     * @param otherlist list to inserted into this list
     * @param before item before which the new ones are placed
     */
    public void addAllBefore(DoublyLinkedList<? extends TItem> otherlist, TItem before) {
        if (otherlist._size == 0) {
            // nothing to do
        } else if (before == null || before.getPrevious() == null) {
            addAllFirst(otherlist);
        } else {
            TItem prev = before.getPrevious();

            prev.setNext(otherlist._head);
            otherlist._head.setPrevious(prev);

            before.setPrevious(otherlist._tail);
            otherlist._tail.setNext(before);

            _size += otherlist._size;

            otherlist._size = 0;
            otherlist._head = null;
            otherlist._tail = null;
        }
    }

    /**
     * Inserts all items in the provided list into the list, after the specified
     * item. If this specified item is null, all are placed at the end of the
     * list. The provided list is cleared in the process. This takes constant
     * time.
     *
     * @param otherlist list to inserted into this list
     * @param after item after which the new ones are placed
     */
    public void addAllAfter(DoublyLinkedList<? extends TItem> otherlist, TItem after) {
        if (otherlist._size == 0) {
            // nothing to do
        } else if (after == null || after.getNext() == null) {
            addAllLast(otherlist);
        } else {
            TItem next = after.getNext();

            after.setNext(otherlist._head);
            otherlist._head.setPrevious(after);

            next.setPrevious(otherlist._tail);
            otherlist._tail.setNext(next);

            _size += otherlist._size;

            otherlist._size = 0;
            otherlist._head = null;
            otherlist._tail = null;
        }
    }

    /**
     * Empties the list. Note that it clears all the previous and next pointers
     * stored with the elements, and ensures that they are no longer indicating
     * to be part of a list. Thus this takes linear time.
     */
    public void clear() {
        while (_size > 0) {
            removeFirst();
        }
    }

    /**
     * Removes the first element from the list.
     *
     * @return the removed element
     */
    public TItem removeFirst() {
        assert _size > 0;

        TItem removed = _head;

        _head.setContainedInList(false);
        if (_size == 1) {
            _head = null;
            _tail = null;
        } else {
            TItem second = _head.getNext();
            second.setPrevious(null);
            _head.setNext(null);
            _head = second;
        }
        _size--;

        return removed;
    }

    /**
     * Removes the last element from the list.
     *
     * @return the removed element
     */
    public TItem removeLast() {
        assert _size > 0;

        TItem removed = _tail;

        _tail.setContainedInList(false);
        if (_size == 1) {
            _head = null;
            _tail = null;
        } else {
            TItem beforeLast = _tail.getPrevious();
            beforeLast.setNext(null);
            _tail.setPrevious(null);
            _tail = beforeLast;
        }
        _size--;

        return removed;
    }

    /**
     * Removes the specified element from the list.
     *
     * @param elt the element to be removed
     */
    public void remove(TItem elt) {
        if (elt == _head) {
            removeFirst();
        } else if (elt == _tail) {
            removeLast();
        } else {
            elt.setContainedInList(false);

            elt.getPrevious().setNext(elt.getNext());
            elt.getNext().setPrevious(elt.getPrevious());

            elt.setNext(null);
            elt.setPrevious(null);

            _size--;
        }
    }
    //</editor-fold>
}

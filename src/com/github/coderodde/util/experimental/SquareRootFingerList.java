package com.github.coderodde.util.experimental;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * This class implements a doubly-linked list that maintains a set of so called
 * fingers in order to access the target nodes faster.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 13, 2020)
 * @since 1.6
 */
public final class SquareRootFingerList<E> 
        extends AbstractSequentialList<E> 
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    
    private static final class SquareRootFingerListNode<T> {
        private T datum;
        private SquareRootFingerListNode<T> prev;
        private SquareRootFingerListNode<T> next;

        public SquareRootFingerListNode(T datum) {
            this.datum = datum;
        }
        
        T getDatum() {
            return datum;
        }
    }
    
    private static final class Finger<T> {
        private SquareRootFingerListNode<T> node;
        private int index;
    }
    
    private static final class FingerStack<T> {
        private static final int DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY = 8;
        
        private int size;
        private int mask = DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY - 1;
        private int topIndex;
        private Finger<T>[] fingerArray = 
                new Finger[DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY];
        
        void pushFinger(Finger<T> finger) {
            ensureCapacity(++size);
            fingerArray[topIndex] = finger;
            topIndex = (topIndex + 1) & mask;
        }
        
        void popFinger() {
            int nextTailIndex = (topIndex - 1) & mask;
            // Let the garbage collector do its work:
            fingerArray[nextTailIndex] = null;
            topIndex = nextTailIndex;
            size--;
        }
        
        private void ensureCapacity(int requestedCapacity) {
            if (fingerArray.length < requestedCapacity) {
                int nextCapacity = size * 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
                mask = nextCapacity - 1;
            }
        }
    }
    
    private final FingerStack<E> fingerStack = new FingerStack<>();
    private SquareRootFingerListNode<E> headNode;
    private SquareRootFingerListNode<E> tailNode;
    private int size;
    private int modificationCount;
    
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public boolean contains(Object o) {
        for (SquareRootFingerListNode<E> node = headNode; node != null; node = node.next) {
            if (Objects.equals(o, node.getDatum())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] toArray() {
        Object[] resultArray = new Object[size];
        SquareRootFingerListNode<E> node = headNode;
        
        for (int i = 0; i < size; i++) {
            resultArray[i] = node.datum;
            node = node.next;
        }
        
        return resultArray;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(E e) {
        if (size == 0) {
            addToEmptyList(e);
        } else {
            // Append to a non-empty list:
            appendToList(e);
        }
        
        size++;
        
        return true;
    }

    private void fixFingerArrayAfterAddition() {
        int optimalNumberOf
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E getFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E getLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void addToEmptyList(E e) {
        headNode = tailNode = new SquareRootFingerListNode<>(e);
    }
    
    private void appendToList(E e) {
        SquareRootFingerListNode<E> newNode = new SquareRootFingerListNode<>(e);
        tailNode.next = newNode;
        newNode.prev = tailNode;
        tailNode = newNode;
    }
}

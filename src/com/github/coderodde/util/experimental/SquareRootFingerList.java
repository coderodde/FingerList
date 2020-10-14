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
    
    private static final class SquareRootFingerListNode<E> {
        E datum;
        SquareRootFingerListNode<E> prev;
        SquareRootFingerListNode<E> next;

        SquareRootFingerListNode(E datum) {
            this.datum = datum;
        }
    }
    
    private static final class Finger<T> {
        private SquareRootFingerListNode<T> node;
        private int index;
        
        Finger(SquareRootFingerListNode node, int index) {
            this.node = node;
            this.index = index;
        }
    }
    
    private static final class FingerStack<E> {
        private static final int DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY = 8;
        
        private int size;
        private int mask = DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY - 1;
        private int topIndex;
        private Finger<E>[] fingerArray = 
                new Finger[DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY];
        
        public void pushFinger(Finger<E> finger) {
            ensureCapacity(++size);
            fingerArray[topIndex] = finger;
            topIndex = (topIndex + 1) & mask;
        }
        
        public void popFinger() {
            int nextTailIndex = (topIndex - 1) & mask;
            // Let the garbage collector do its work:
            fingerArray[nextTailIndex] = null;
            topIndex = nextTailIndex;
            size--;
        }
        
        public int size() {
            return size;
        }
        
        public Finger<E> get(int i) {
            return fingerArray[topIndex - 1];
        }
        
        public Finger<E> findClosestFinger(int index) {
            int closestDistance = Integer.MAX_VALUE;
            Finger<E> closestFinger = null;
            Finger<E> finger = fingerArray[0];
            
            while (finger != null) {
                int distance = Math.abs(finger.index - index);
                
                if (closestDistance > distance) {
                    closestDistance = distance;
                    closestFinger = finger;
                }
            }
            
            return closestFinger;
        }
        
        public void clear() {
            size = 0;
            topIndex = 0;
            mask = DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY - 1;
            fingerArray = new Finger[DEFAULT_FINGER_QUEUE_ARRAY_CAPACITY];
        }
        
        private void ensureCapacity(int requestedCapacity) {
            if (fingerArray.length < requestedCapacity) {
                int nextCapacity = size * 2;
                fingerArray = Arrays.copyOf(fingerArray, nextCapacity);
                mask = nextCapacity - 1;
            }
        }
    }
    
    private transient final FingerStack<E> fingerStack = new FingerStack<>();
    private transient SquareRootFingerListNode<E> headNode;
    private transient SquareRootFingerListNode<E> tailNode;
    private transient int size;
    private transient int modificationCount;
    
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
            if (Objects.equals(o, node.datum)) {
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
        fixFingerArrayAfterAddition();
        return true;
    }
    
    @Override
    public boolean remove(Object o) {
        int index = 0;
        
        for (SquareRootFingerListNode<E> node = headNode; 
                node != null; 
                node = node.next, index++) {
            if (Objects.equals(o, node.datum)) {
                removeNode(index, node);
                return true;
            }
        }
        
        return false;
    }
    
    private void removeNode(int index, SquareRootFingerListNode<E> node) {
        if (node.prev == null) {
            headNode = node.next;
            headNode.prev = null;
        } else {
            node.prev.next = node.next;
        }
        
        if (node.next == null) {
            tailNode = node.prev;
            tailNode.next = null;
        } else {
            node.next.prev = node.prev;
        }
        
        --size;
        
        fixFingerStackSizeAfterRemoval();
        fixFingerStackFingersAfterRemoval(index);
    }
    
    private void fixFingerStackFingersAfterRemoval(int index) {
        for (int i = 0; i < fingerStack.size(); i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= index) {
                finger.index--;
            }
        }
    }
    
    private void fixFingerStackFingersAfterAddition(int index) {
        for (int i = 0; i < fingerStack.size(); i++) {
            Finger<E> finger = fingerStack.get(i);
            
            if (finger.index >= index) {
                finger.index++;
            }
        }
    }
    
    private void fixFingerStackSizeAfterRemoval() {
        int optimalNumberOfFingers = getOptimalNumberOfFingers();
        
        if (fingerStack.size() > optimalNumberOfFingers) {
            fingerStack.popFinger();
        }
    }
    
    private void fixFingerStackAfterAddition() {
        int optimalNumberOfFingers = getOptimalNumberOfFingers();
        
        while (fingerStack.size() < optimalNumberOfFingers) {
            fingerStack.pushFinger(new Finger<>(tailNode, size - 1));
        }
    }
    
    private int getOptimalNumberOfFingers() {
        return (int) Math.sqrt(size);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        
        return true;
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
        boolean removedAny = false;
    
        for (Object o : c) {
            if (remove(o)) {
                removedAny = true;
            }
        }
        
        return removedAny;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean removedAny = false;
        
        for (Object o : c) {
            if (!contains(o)) {
                remove(o);
            }
        }
        
        return removedAny;
    }

    @Override
    public void clear() {
        fingerStack.clear();
        size = 0;
        headNode = null;
        tailNode = null;
    }

    @Override
    public E get(int index) {
        checkAccessIndex(index);
        Finger<E> finger = fingerStack.findClosestFinger(index);
        SquareRootFingerListNode<E> node = finger.node;
        
        for (int i = 0; i < finger.index - index; i++, node = node.prev) {
            finger.node = finger.node.prev;
        }
        
        for (int i = 0; i < index - finger.index; i++, node = node.next) {
            finger.node = finger.node.next;
        }
        
        return node.datum;
    }

    
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index(" + index + ") < 0");
        }
        
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "index(" + index + ") >= size(" + size + ")");
        }
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

    private void fixFingerArrayAfterAddition() {
        int optimalNumberOfFingers = (int) Math.sqrt(size);
        
        while (fingerStack.size < optimalNumberOfFingers) {
            Finger<E> finger = new Finger<>(tailNode, size - 1);
            fingerStack.pushFinger(finger);
        }
    }
}

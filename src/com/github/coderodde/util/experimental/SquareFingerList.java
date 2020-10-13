package com.github.coderodde.util.experimental;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class implements an experimental linked list data structure that
 * maintains a small set of so called fingers that are just references to the
 * linked list nodes.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 13, 2020)
 */
public final class SquareFingerList<T> {
    
    
    /**
     * This static inner class defines a node in the linked list.
     * 
     * @param <T> the element type.
     */
    private static final class SquareFingerListNode<T> {
        
        private T element;
        private SquareFingerListNode<T> previousNode;
        private SquareFingerListNode<T> nextNode;
        
        SquareFingerListNode(T element) {
            this.element = element;
        }
        
        // Used for debugging.
        @Override
        public String toString() {
            return "[" + Objects.toString(element) + "]";
        }
    }
    
    /**
     * This static inner class defines a finger to a node.
     * 
     * @param <T> the element type.
     */
    private static final class Finger<T> {
        
        private SquareFingerListNode<T> node;
        private int index;
        
        Finger(SquareFingerListNode<T> node) {
            this.node = node;
        }
        
        // Used for debugging.
        @Override
        public String toString() {
            return "[" + index + ", " + Objects.toString(node.element) + "]";
        }
    }
    
    private SquareFingerListNode<T> headNode;
    private SquareFingerListNode<T> tailNode;
    private final FingerDeque<T> fingerDeque = new FingerDeque<>();
    private int shortestFingerDistance;
    private int size;
    
    public void add(int index, T element) {
        checkAddIndex(index);
        
        if (size == 0) {
            // Empty list. Just add the node and set all the fingers point to
            // it:
            addToEmptyList(element);
        } else if (size == index) {
            // Non-empty list. Append the new node as the last one:
            appendToList(element);
        } else if (index == 0) {
            // Non-empty list. Prepend the new node as the first one::
            prependToList(element);
        } else {
            // Non-empty list. Insert between two adjacent nodes:
            insertToList(index, element);
        }
        
        size++;
        fixNumberOfFingersAfterAddition();
    }
    
    private void fixNumberOfFingersAfterAddition() {
        int optimalNumberOfFingers = getFingersArrayOptimalLength(size);
        SquareFingerListNode<T> lastNode = tailNode;
        
        while (fingerDeque.size() < optimalNumberOfFingers) {
            Finger<T> finger = new Finger<>(lastNode);
            finger.index = size - 1;
            fingerDeque.addFinger(finger);
        }
    }
    
    private void fixNumberOfFingersAfterRemoval() {
        int optimalNumberOfFingers = getFingersArrayOptimalLength(size);
        
        while (fingerDeque.size() > optimalNumberOfFingers) {
            fingerDeque.removeFinger();
        }
    }
    
    private void addToEmptyList(T element) {
        headNode = tailNode = new SquareFingerListNode<>(element);
        fingerDeque.addFinger(new Finger<>(headNode));
    }
    
    private void appendToList(T element) {
        SquareFingerListNode<T> node = new SquareFingerListNode<>(element);
        tailNode.nextNode = node;
        node.previousNode = tailNode;
        tailNode = node;
        updateFingerIndicesAfterAddition(size - 1, 1);
    }
    
    private void prependToList(T element) {
        SquareFingerListNode<T> node = new SquareFingerListNode<>(element);
        headNode.previousNode = node;
        node.nextNode = headNode;
        headNode = node;
        updateFingerIndicesAfterAddition(0, 1);
    }
    
    private void insertToList(int index, T element) {
        SquareFingerListNode<T> newNode = new SquareFingerListNode<>(element);
        Finger<T> finger = fingerDeque.getClosestFinger(index);
        SquareFingerListNode<T> targetNode = finger.node;
        
        // Out of two below for loops, only one will iterate at least once:
        for (int i = 0; i < finger.index - index; i++) {
            targetNode = targetNode.previousNode;
        }
        
        for (int i = 0; i < index - finger.index; i++) {
            targetNode = targetNode.nextNode;
        }
        
        // Here, we have found the target node in front of which we need to 
        // insert the new node:
        newNode.nextNode = targetNode;
        newNode.previousNode = targetNode.previousNode;
        targetNode.previousNode.nextNode = newNode;
        targetNode.previousNode = newNode;
        
        // Update the finger indices:
        updateFingerIndicesAfterAddition(index, 1);
        
        finger.index = index;
        finger.node = newNode;
    }
    
    private void updateFingerIndicesAfterAddition(int index, int delta) {
        for (int i = 0; i < fingerDeque.size(); i++) {
            Finger<T> finger = fingerDeque.get(i);
            
            if (finger.index >= index) {
                finger.index += delta;
            }
        }
    }
    
    public T get(int index) {
        checkAccessIndex(index);
        Finger<T> finger = fingerDeque.getClosestFinger(index);
        
        for (int i = 0; i < finger.index - index; i++) {
            finger.index--;
            finger.node = finger.node.previousNode;
        }
        
        for (int i = 0; i < index - finger.index; i++) {
            finger.index++;
            finger.node = finger.node.nextNode;
        }
        
        return finger.node.element;
    }
    
    public void remove(int index) {
        checkAccessIndex(index);
        
        if (size == 1) {
            removeFromListWithOnlyOneElement();
        } else if (index == 0) {
            removeHeadNode();
        } else if (index == size - 1) {
            
        }
        
        size--;
        fixNumberOfFingersAfterRemoval();
    }
    
    private void removeHeadNode() {
        headNode = headNode.nextNode;
        headNode.previousNode = null;
    }
    
    private void removeFromListWithOnlyOneElement() {
        // Only one element in the list. Easy:
        headNode = tailNode = null;
        size = 0;
    }
    
    public int size() {
        return size;
    }
    
    boolean hasCorrectState() {
        if (size == 0) {
            for (Finger<T> finger : fingers) {
                if (finger.node != null) {
                    return false;
                }
            }
        } else {
            int index = 0;
            
            for (SquareFingerListNode<T> node = headNode;
                 node != null;
                 node = node.nextNode, index++) {
                for (Finger<T> finger : fingers) {
                    if (finger.node == node && finger.index != index) {
                        return false;
                    } else if (finger.index < 0) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index(" + index + ") < 0");
        }
        
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "index(" + index + ") >= (" + size + ")");
        }
    }
    
    private void checkAddIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index(" + index + ") < 0");
        }
        
        if (index > size) {
            throw new IndexOutOfBoundsException(
                    "index(" + index + ") > (" + size + ")");
        }
    }
    
    private static int getFingersArrayOptimalLength(int size) {
        return (int) Math.sqrt(size);
    }
    
    private class FingerDeque<T> {
        
        // The default finger array capacity.
        private static final int DEFAULT_FINGER_ARRAY_CAPACITY = 8;
        
        // The ratio by which the finger deque array is expanded when new space
        // is needed.
        private static final float FINGER_ARRAY_EXPANSION_FACTORY = 1.5f;
        
        private Finger<T>[] fingers;
        private int headIndex;
        private int tailIndex;
        private int size;
        
        void addFinger(Finger<T> finger) {
            if (size == fingers.length) {
                expandFingerArray();
            }
            
            fingers[tailIndex] = finger;
            tailIndex = (tailIndex + 1) % fingers.length;
            size++;
        }
        
        Finger get(int index) {
            return fingers[(headIndex + index) % fingers.length];
        }
        
        void removeFinger() {
            fingers[headIndex] = null; // Let the GC do its job.
            headIndex = (headIndex + 1) % fingers.length;
            size--;
        }
        
        Finger getClosestFinger(int index) {
            Finger<T> closestFinger = null;
            int closestDistance = Integer.MAX_VALUE;
            
            for (int i = 0; i < size; i++) {
                int fingerIndex = (headIndex + i) % fingers.length;
                Finger<T> finger = fingers[fingerIndex];
                int distance = Math.abs(index - finger.index);
                
                if (closestDistance > distance) {
                    closestDistance = distance;
                    closestFinger = finger;
                }
            }
            
            SquareFingerList.this.shortestFingerDistance = closestDistance;
            return closestFinger;
        }
        
        int size() {
            return size;
        }
        
        private void expandFingerArray() {
            this.fingers = 
                    Arrays.copyOf(
                            this.fingers, 
                            (int)(this.fingers.length * 
                                  FINGER_ARRAY_EXPANSION_FACTORY));
        }
    }
}

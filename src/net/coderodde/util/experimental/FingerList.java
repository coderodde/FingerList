package net.coderodde.util.experimental;

import java.util.Objects;

/**
 * This class implements an experimental linked list data structure that
 * maintains a small set of so called fingers that are just references to the
 * linked list nodes.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 18, 2018)
 */
public final class FingerList<T> {
    
    /**
     * This static inner class defines a node in the linked list.
     * 
     * @param <T> the element type.
     */
    private static final class FingerListNode<T> {
        
        private T element;
        private FingerListNode<T> previousNode;
        private FingerListNode<T> nextNode;
        
        FingerListNode(T element) {
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
        
        private FingerListNode<T> node;
        private int index;
        
        // Used for debugging.
        @Override
        public String toString() {
            return "[" + index + ", " + Objects.toString(node.element) + "]";
        }
    }
    
    private FingerListNode<T> headNode;
    private FingerListNode<T> tailNode;
    private Finger<T>[] fingers;
    private int size;
    
    public FingerList(int numberOfFingers) {
        numberOfFingers = Math.max(1, numberOfFingers);
        this.fingers = new Finger[numberOfFingers];
        
        for (int i = 0; i < numberOfFingers; i++) {
            this.fingers[i] = new Finger<>();
        }
    }
    
    public FingerList() {
        this(3);
    }
    
    public void add(int index, T element) {
        checkAddIndex(index);
        
        if (size == 0) {
            headNode = new FingerListNode(element);
            tailNode = headNode;
            
            for (Finger<T> finger : fingers) {
                finger.index = 0;
                finger.node = headNode;
            }
        } else {
            FingerListNode<T> newNode = new FingerListNode<>(element);
            int bestFingerDistance = Integer.MAX_VALUE;
            Finger<T> bestFinger = null;
            int distance = 0;
            
            // Find the finger closest to the insertion index:
            for (Finger<T> finger : fingers) {
                distance = Math.abs(index - finger.index);
                
                if (bestFingerDistance > distance) {
                    bestFingerDistance = distance;
                    bestFinger = finger;
                }
            }
            
            if (index < bestFinger.index) {
                // March to the left:
                while (distance-- > 0) {
                    bestFinger.node = bestFinger.node.previousNode;
                }
                
                if (index == 0) {
                    // Insert before headNode:
                    newNode.nextNode = headNode;
                    headNode.previousNode = newNode;
                    headNode = newNode;
                } else {
                    // Insert before node:
                    newNode.nextNode = bestFinger.node;
                    newNode.previousNode = bestFinger.node.previousNode;
                    bestFinger.node.previousNode.nextNode = newNode;
                    bestFinger.node.nextNode.previousNode = newNode;
                }
                
                // Update the finger index:
                for (Finger<T> finger : fingers) {
                    if (finger.index > index) {
                        finger.index++;
                    }
                }
            } else if (index == size) {
                // Append to the end of list:
                tailNode.nextNode = newNode;
                newNode.previousNode = tailNode;
                tailNode = newNode;
                bestFinger.index = size;
                bestFinger.node = newNode;
            } else {
                // March to the right:
                while (distance-- > 0) {
                    bestFinger.node = bestFinger.node.nextNode;
                }
                
                bestFinger.node.previousNode.nextNode = newNode;
                bestFinger.node.nextNode.previousNode = newNode;
                newNode.previousNode = bestFinger.node.previousNode;
                newNode.nextNode = bestFinger.node;
                
                for (Finger<T> finger : fingers) {
                    if (finger.index > index) {
                        finger.index++;
                    }
                }
            }
        }
        
        size++;
    }
    
    public T get(int index) {
        checkAccessIndex(index);
        int bestFingerDistance = Integer.MAX_VALUE;
        Finger<T> bestFinger = null;
        
        for (Finger<T> finger : fingers) {
            int currentDistance = Math.abs(finger.index - index);
            
            if (bestFingerDistance > currentDistance) {
                bestFingerDistance = currentDistance;
                bestFinger = finger;
            }
        }
        
        FingerListNode<T> node = bestFinger.node;
        
        if (index < bestFinger.index) { 
            bestFinger.index -= bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                bestFingerDistance--;
                node = node.previousNode;
            }
            
            bestFinger.node = node;
        } else {
            bestFinger.index += bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                bestFingerDistance--;
                node = node.nextNode;
            }
            
            bestFinger.node = node;
        }
        
        return node.element;
    }
    
    public void remove(int index) {
        checkAccessIndex(index);
        int bestFingerDistance = Integer.MAX_VALUE;
        Finger<T> bestFinger = null;
        
        // Find the closest finger:
        for (Finger<T> finger : fingers) {
            int currentDistance = Math.abs(finger.index - index);
            
            if (bestFingerDistance > currentDistance) {
                bestFingerDistance = currentDistance;
                bestFinger = finger;
            }
        }
        
        FingerListNode<T> node = bestFinger.node;
        
        if (index < bestFinger.index) {
            bestFinger.index -= bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                node = node.previousNode;
            }
        } else {
            bestFingerDistance += bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                node = node.nextNode;
            }
        }
        
        bestFinger.node = node;

        if (node.previousNode != null) {
            bestFinger.node = node.previousNode;
            bestFinger.index--;
        } else if (node.nextNode != null) {
            bestFinger.node = node.nextNode;
            bestFinger.index++;
        }
        
        if (node == headNode) {
            // Unlink the head node:
            headNode = headNode.nextNode;
            
            if (headNode != null) {
                headNode.previousNode = null;
            }
        } else if (node == tailNode) {
            // Unlink the tail node:
            tailNode = tailNode.previousNode;
            
            if (tailNode != null) {
                tailNode.nextNode = null;
            }
        } else {
            // Unlink an inner node:
            node.previousNode.nextNode = node.nextNode;
            node.nextNode.previousNode = node.previousNode;
            node.previousNode = null;
            node.nextNode = null;
        }
        
        for (Finger<T> finger : fingers) {
            if (finger.index > index) {
                finger.index--;
            }
        }
        
        size--;
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
                    "index(" + index + ") >= (" + size + ")");
        }
    }
}

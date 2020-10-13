package com.github.coderodde.util.experimental;

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
            // Empty list. Just add the node and set all the fingers point to
            // it:
            headNode = new FingerListNode(element);
            tailNode = headNode;
            size = 1;
            
            // Set all the fingers to point to the only node:
            for (Finger<T> finger : fingers) {
                finger.index = 0;
                finger.node = headNode;
            }
        } else if (size == index) {
            // Append the input element. Here, this list is not empty so that
            // the tail node exists:
            FingerListNode<T> nodeToAdd = new FingerListNode<>(element);
            tailNode.nextNode = nodeToAdd;
            nodeToAdd.previousNode = tailNode;
            tailNode = nodeToAdd;
            
            // Find the closest finger:
            int shortestFingerDistance = Integer.MAX_VALUE;
            Finger<T> closestFinger = null;
            
            for (Finger<T> finger : fingers) {
                int fingerDistance = Math.abs(index - finger.index);
                
                if (shortestFingerDistance > fingerDistance) {
                    shortestFingerDistance = fingerDistance;
                    closestFinger = finger;
                }
            }
            
            closestFinger.index = size;
            closestFinger.node = tailNode;
            size++;
        } else {
            // The element to add will have both a previous and a next nodes:
            FingerListNode<T> nodeToAdd = new FingerListNode<>(element);
            int shortestFingerDistance = Integer.MAX_VALUE;
            Finger<T> closestFinger = null;
            
            for (Finger<T> finger : fingers) {
                int fingerDistance = Math.abs(index - finger.index);
                
                if (shortestFingerDistance > fingerDistance) {
                    shortestFingerDistance = fingerDistance;
                    closestFinger = finger;
                }
            }
            
            // Closest finger found. Now move it to point to the node in front
            // of which we will insert the new node:
            if (index <= closestFinger.index) {
                closestFinger.index -= shortestFingerDistance;
                
                while (shortestFingerDistance > 0) {
                    shortestFingerDistance--;
                    closestFinger.node = closestFinger.node.previousNode;
                }
            } else {
                closestFinger.index += shortestFingerDistance;
                
                while (shortestFingerDistance >  0) {
                    shortestFingerDistance--;
                    closestFinger.node = closestFinger.node.nextNode;
                }
            }
            
            // Insert the new node:
            if (closestFinger.index == 0) {
                // Set as the head node:
                nodeToAdd.nextNode = headNode;
                headNode.previousNode = nodeToAdd;
                headNode = nodeToAdd;
            } else {
                // Insert a new node before closestFinger.node:
                nodeToAdd.nextNode = closestFinger.node;
                nodeToAdd.previousNode = closestFinger.node.previousNode;
                closestFinger.node.previousNode.nextNode = nodeToAdd;
                closestFinger.node.previousNode = nodeToAdd;
                closestFinger.index = index;
            }
            
            // Because the new node shifts all the fingers on its right side
            // one position to the right, update the relevant finger indices:
            for (Finger<T> finger : fingers) {
                if (finger.index >= index) {
                    finger.index++;
                }
            }
            
            size++;
        }
    }
    
    public T get(int index) {
        checkAccessIndex(index);
        // Find the closest finger:
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
        
        // Update the closest finger by moving it to the target node:
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
        
        FingerListNode<T> removedNode = bestFinger.node;
        
        // Move the closest finger to the node being removed:
        if (index < bestFinger.index) {
            bestFinger.index -= bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                bestFingerDistance--;
                removedNode = removedNode.previousNode;
            }
        } else {
            bestFinger.index += bestFingerDistance;
            
            while (bestFingerDistance > 0) {
                bestFingerDistance--;
                removedNode = removedNode.nextNode;
            }
        }
        
        // Remove the node:
        if (size == 1) {
            headNode = null;
            tailNode = null;
            
            // Set all node references so that the garbage collector can claim
            // them:
            for (Finger<T> finger : fingers) {
                finger.node = null;
            }
        } else if (removedNode.previousNode == null) {
            // Once here, removedNode is the head node.
            for (Finger<T> finger : fingers) {
                if (finger.index > 0) {
                    finger.index--;
                }  else if (finger.node == removedNode) {
                    finger.node = finger.node.nextNode;
                }
            }
            
            // Update the head node:
            headNode = headNode.nextNode;
            
            if (headNode != null) {
                headNode.previousNode = null;
                bestFinger.node = headNode;
                bestFinger.index = 0; 
            }
        } else if (removedNode.nextNode == null) {
            // Once here, removedNode is the tail node:
            tailNode = tailNode.previousNode;
            
            if (tailNode != null) {
                tailNode.nextNode = null;
                bestFinger.node = tailNode;
                bestFinger.index--;
            }
            
            // Move all the fingers referencing the tail one position to the
            // left:
            for (Finger<T> finger : fingers) {
                if (finger.index == index) {
                    finger.index--;
                    finger.node = finger.node.previousNode;
                }
            }
        } else {
            // Once here, removedNode has both previous and next nodes:
            bestFinger.node = removedNode.nextNode;
            
            for (Finger<T> finger : fingers) {
                if (finger.index > index) {
                    finger.index--;
                }
            }
            
            removedNode.nextNode.previousNode = removedNode.previousNode;
            removedNode.previousNode.nextNode = removedNode.nextNode;
        }
        
        size--;
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
            
            for (FingerListNode<T> node = headNode;
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
}

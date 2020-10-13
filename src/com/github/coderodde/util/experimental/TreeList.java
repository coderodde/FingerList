package com.github.coderodde.util.experimental;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 25, 2018)
 */
public final class TreeList<E> {
    
    /**
     * Implements the actual tree node type.
     * 
     * @param <E> the element type. 
     */
    private static final class TreeListNode<E> {
        
        /**
         * The maximum number of elements this node can accommodate.
         */
        private final int capacity;
        
        /**
         * The number of elements actually stored.
         */
        private int size;
        
        /**
         * The index of the element that is a logical beginning of the sublist
         * represented by this node.
         */
        private int headIndex;
        
        /**
         * The height of the node, or in other words, the maximum number of
         * steps towards the lower nodes in order to reach the bottom. This
         * field is solely for the AVL-algorithm.
         */
        private int height;
        
        /**
         * Counts the number of successor nodes of this node, including this 
         * node.
         */
        private int count;
        
        /**
         * The right child of this node.
         */
        private TreeListNode<E> leftChild;

        /**
         * The left child of this node.
         */
        private TreeListNode<E> rightChild;

        /**
         * The parent node of this node.
         */
        private TreeListNode<E> parentNode;
        
        /**
         * The actual array storing the elements.
         */
        private final E[] array;
        
        private int leftSubtreeSize;
        
        TreeListNode(int capacity) {
            checkTreeListNodeCapacity(capacity);
            this.array = (E[]) new Object[capacity];
            this.capacity = this.array.length;
        }
        
        E get(int index) {
            return array[(headIndex + index) % capacity];
        }
        
        /**
         * Adds {@code element} at logical index {@code index}. The value zero
         * of the index points to the {@code array[headIndex]}, the value one of
         * the index points to {@code array[headIndex]}. If, however, 
         * {@code index + headIndex == array.length}, the index "wraps around"
         * and points to the first array component.
         * 
         * @param index   the logical index of the input element.
         * @param element the element to add.
         */
        void add(final int index, final E element) {
            final int elementsOnLeft = index;
            final int elementsOnRight = size - index;
            
            if (elementsOnLeft < elementsOnRight) {
                int counter = 0;
                int sourceIndex = (this.headIndex + index - 1) % this.capacity;
                int targetIndex = (this.headIndex + index - 2) % this.capacity;
                
                while (counter < elementsOnLeft) {
                    counter++;
                    this.array[targetIndex--] = this.array[sourceIndex--];
                    sourceIndex %= this.capacity;
                    targetIndex %= this.capacity;
                    
                    if (sourceIndex < 0) {
                        throw new RuntimeException("oooops!");
                    }
                }
                
                this.array[(this.headIndex + index) % this.capacity] = element;
                this.headIndex = (this.headIndex - 1) % this.capacity;
            } else {
                int counter = 0;
                int sourceIndex = (this.headIndex + size - 1) % this.capacity;
                int targetIndex = (this.headIndex + size)     % this.capacity;
                
                while (counter < elementsOnRight) {
                    counter++;
                    this.array[targetIndex--] = this.array[sourceIndex--];
                    sourceIndex %= this.capacity;
                    targetIndex %= this.capacity;
                    
                    if (targetIndex < 0) {
                        throw new RuntimeException("oook!");
                    }
                } 
                
                this.array[index] = element;
            }
            
            size++;
        }
    }
    
    private static final int DEFAULT_NODE_CAPACITY = 64;
    
    private final int nodeCapacity;
    private TreeListNode<E> root;
    private int size;
    
    public TreeList(int nodeCapacity) {
        checkTreeListNodeCapacity(nodeCapacity);
        this.nodeCapacity = nodeCapacity;
        this.root = new TreeListNode<>(nodeCapacity);
    }
    
    public TreeList() {
        this(DEFAULT_NODE_CAPACITY);
    }
    
    public int size() {
        return size;
    }
    
    public void add(int index, E element) {
        checkAdditionIndex(index);
        TreeListNode<E> node = root;
        
        while (true) {
            if (index > node.count) {
                node = node.rightChild;
            } else if (index >= node.count) {
                node = node.rightChild; 
            }
            if (index > node.leftSubtreeSize + node.size) {
                index -= node.leftSubtreeSize + node.size;
                node = node.rightChild;
            } else if (index < node.leftSubtreeSize) {
                node = node.leftChild;
            } else {
                doAddElement(node, element, index);
                return;
            }
        }
    }
    
    private void doAddElement(TreeListNode<E> node, E element, int index) {
        if (node.size < this.nodeCapacity) {
            node.add(index, element);
            updateRootPathOnInsert(node);
        } else {
            splittingAdd(node, element, index);
        }
        
        size++;
    }
    
    /**
     * Performs a move operation over {@code n} logically first elements from
     * the source array to target array.
     * 
     * @param sourceArray
     * @param targetArray
     * @param n
     * @param sourceArrayHeadIndex 
     */
    void moveElementsToAnotherArray(final E[] sourceArray, 
                                    final E[] targetArray,
                                    final int n,
                                    final int sourceArrayHeadIndex) {
        final int sourceArrayCapacity = sourceArray.length;
        
        int sourceIndex = sourceArrayHeadIndex;
        int targetIndex = 0;
        
        while (targetIndex < n) {
            targetArray[targetIndex++] = sourceArray[sourceIndex];
            sourceArray[sourceIndex++] = null;
            sourceIndex %= sourceArrayCapacity;
        }
    }
    
    /**
     * If {@code subTreeRoot} has no left child, sets {@code newNode} as its
     * left child. Otherwise, goes to the left child and from there goes as far
     * to the right as possible. When a node, that has no right child, adds 
     * {@code newNode} as its right child and exits.
     * 
     * @param newNode     the node to append to {@code subTreeRoot}.
     * @param subTreeRoot the root of the subtree to which to add 
     *                    {@code newNode}.
     */
    private void appendNewNodeToLeftSubtree(final TreeListNode<E> newNode, 
                                            final TreeListNode<E> subTreeRoot) {
        if (subTreeRoot.leftChild == null) {
            subTreeRoot.leftChild = newNode;
            newNode.parentNode = subTreeRoot;
        } else {
            TreeListNode<E> auxNode = subTreeRoot.leftChild;
            
            while (auxNode.rightChild != null) {
                auxNode = auxNode.rightChild;
            }
            
            auxNode.rightChild = newNode;
            newNode.parentNode = auxNode;
        }
    }
    
    private void prependNewNodeToRightSubtree(
            final TreeListNode<E> newNode,
            final TreeListNode<E> subTreeRoot) {
        if (subTreeRoot.rightChild == null) {
            subTreeRoot.rightChild = newNode;
            newNode.parentNode = subTreeRoot;
        } else {
            TreeListNode<E> auxNode = subTreeRoot.rightChild;
            
            while (auxNode.leftChild != null) {
                auxNode = auxNode.leftChild;
            }
            
            auxNode.leftChild = newNode;
            newNode.parentNode = auxNode;
        }
    }
    
    private void splittingAddToLeft(final TreeListNode<E> sourceNode, 
                                    final int elementsOnLeft,
                                    final E element) {
        final TreeListNode<E> newNode = new TreeListNode<>(this.nodeCapacity);
        final E[] sourceArray = sourceNode.array;
        final E[] targetArray = newNode.array;
        final int n = elementsOnLeft;
        final int sourceArrayIndex = sourceNode.headIndex;
        // Copy 'nodeCapacity' first elements from the 'sourceArray'.
        moveElementsToAnotherArray(sourceArray,
                                   targetArray, 
                                   n, 
                                   sourceArrayIndex);
        // Prepend the 'element' to the 'newNode'.
        final int targetArrayIndex = this.nodeCapacity - 1;
        newNode.headIndex = targetArrayIndex;
        newNode.array[targetArrayIndex] = element; 
        appendNewNodeToLeftSubtree(newNode, sourceNode);
        newNode.size = elementsOnLeft + 1;
        sourceNode.size -= elementsOnLeft; // Count the new element also.
        updateRootPathOnInsert(newNode);
        fixAfterInsertion(newNode);
    }
    
    private void splittingAddToRight(final TreeListNode<E> sourceNode,
                                     final int elementsOnRight,
                                     final E element) {
        final TreeListNode<E> newNode = new TreeListNode<>(this.nodeCapacity);
        final E[] sourceArray = sourceNode.array;
        final E[] targetArray = newNode.array;
        final int n = elementsOnRight;
        final int sourceArrayIndex = (sourceNode.headIndex + sourceNode.size
                                                           - elementsOnRight) 
                                    % sourceNode.capacity;
        // TODO: check if startIndex can be negative.
        moveElementsToAnotherArray(sourceArray,
                                   targetArray,
                                   n,
                                   sourceArrayIndex);
        // Append the 'element' to the 'newNode'.
        final int targetArrayIndex = this.nodeCapacity - 1;
        newNode.headIndex = targetArrayIndex;
        newNode.array[targetArrayIndex] = element;
        prependNewNodeToRightSubtree(newNode, sourceNode);
        newNode.size = elementsOnRight + 1;
        sourceNode.size -= elementsOnRight;
        updateRootPathOnInsert(newNode);
        fixAfterInsertion(newNode);
    }
    
    private void splittingAdd(TreeListNode<E> node, E element, int index) {
        final int elementsOnLeft  = index;
        final int elementsOnRight = node.size - elementsOnLeft;
        
        if (elementsOnLeft < elementsOnRight) {
            splittingAddToLeft(node, elementsOnLeft, element);
        } else {
            splittingAddToRight(node, elementsOnRight, element);
        }
    }
    
    private void updateRootPathOnInsert(TreeListNode<E> node) {
        TreeListNode<E> parentNode = node.parentNode;
        
        while (parentNode != null) {
            if (parentNode.leftChild == node) {
                parentNode.leftSubtreeSize++;
            }
            
            node = parentNode;
            parentNode = parentNode.parentNode;
        }
    }
    
    private void updateRootPathOnDelete(TreeListNode<E> node) {
        TreeListNode<E> parentNode = node.parentNode;
        
        while (parentNode != null) {
            if (parentNode.leftChild == node) {
                parentNode.leftSubtreeSize--;
            }
            
            node = parentNode;
            parentNode = parentNode.parentNode;
        }
    }
    
    public E get(int index) {
        checkAccessIndex(index);
        TreeListNode<E> node = root;
        
        while (true) {
            if (index < node.leftSubtreeSize) {
                node = node.leftChild;
            } else if (index < node.size) {
                return node.get(index);
            } else {
                index -= node.size + node.leftSubtreeSize;
                node = node.rightChild;
            }
        }
    }
    
    private TreeListNode<E> getNode(int index) {
        TreeListNode<E> node = root;
        
        while (true) {
            if (index <= node.size) {
                return node;            }
            
            if (index < node.leftSubtreeSize) {
                node = node.leftChild;
            }  else if (index > node.leftSubtreeSize + node.size) {
                index -= node.leftSubtreeSize + node.size;
                node = node.rightChild;
            }
        }
    }
    
    public boolean isHealthy() {
        if (root == null) {
            return size == 0;
        }
        
        return !containsCycles()
                && heightsAreCorrect()
                && isBalanced()
                && isWellIndexed();
    }
    
    //// Input validation ////
    private static void checkTreeListNodeCapacity(int candidateCapacity) {
        if (candidateCapacity < 1) {
            throw new IllegalArgumentException(
                    "Node capacity is too small: " + candidateCapacity);
        }
    }
    
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Access index negative: " + index);
        }
    }
    
    //// AVL-tree related code ////    
    private void fixAfterInsertion(TreeListNode<E> node) {
        // Expects that the left subtree size counters reflect the current 
        // tree structure.
        TreeListNode<E> parent = node.parentNode;
        TreeListNode<E> grandParent;
        TreeListNode<E> subTree;
        
        while (parent != null) {
            if (height(parent.leftChild) == 
                height(parent.rightChild) + 2) {
                grandParent = parent.parentNode;
                
                if (height(parent.leftChild.leftChild) >= 
                    height(parent.leftChild.rightChild)) {
                    subTree = rightRotate(parent);
                } else {
                    subTree = leftRightRotate(parent);
                }
                
                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.leftChild == parent) {
                    grandParent.leftChild = subTree;
                } else {
                    grandParent.rightChild = subTree;
                }
                
                if (grandParent != null) {
                    grandParent.height = Math.max(
                            height(grandParent.leftChild),
                            height(grandParent.rightChild)) + 1;
                }
                
                return;
            } else if (height(parent.leftChild) + 2 ==
                       height(parent.rightChild)) {
                grandParent = parent.parentNode;
                
                if (height(parent.rightChild.rightChild) >= 
                    height(parent.rightChild.leftChild)) {
                    subTree = leftRotate(parent);
                } else {
                    subTree = rightLeftRotate(parent);
                }
                
                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.leftChild == parent) {
                    grandParent.leftChild = subTree;
                } else {
                    grandParent.rightChild = subTree;
                }
                
                if (grandParent != null) {
                    grandParent.height = Math.max(
                            height(grandParent.leftChild),
                            height(grandParent.rightChild)) + 1;
                }
                
                return;
            }
            
            parent.height = Math.max(height(parent.leftChild),
                                     height(parent.rightChild)) + 1;
            parent = parent.parentNode;
        }
    }
    
    private void fixAfterDeletion(TreeListNode<E> node) {
        updateRootPathOnDelete(node);
        TreeListNode<E> parent = node.parentNode;
        TreeListNode<E> grandParent;
        TreeListNode<E> subTree;
        
        while (parent != null) {
            if (height(parent.leftChild) == 
                height(parent.rightChild) + 2) {
                grandParent = parent.parentNode;
                
                if (height(parent.leftChild.leftChild) >= 
                    height(parent.leftChild.rightChild)) {
                    subTree = rightRotate(parent);
                } else {
                    subTree = leftRightRotate(parent);
                }
                
                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.leftChild == parent) {
                    grandParent.leftChild = subTree;
                } else {
                    grandParent.rightChild = subTree;
                }
                
                if (grandParent != null) {
                    grandParent.height = Math.max(
                            height(grandParent.leftChild),
                            height(grandParent.rightChild)) + 1;
                }
            } else if (height(parent.leftChild) + 2 ==
                       height(parent.rightChild)) {
                grandParent = parent.parentNode;
                
                if (height(parent.rightChild.rightChild) >= 
                    height(parent.rightChild.leftChild)) {
                    subTree = leftRotate(parent);
                } else {
                    subTree = rightLeftRotate(parent);
                }
                
                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.leftChild == parent) {
                    grandParent.leftChild = subTree;
                } else {
                    grandParent.rightChild = subTree;
                }
                
                if (grandParent != null) {
                    grandParent.height = Math.max(
                            height(grandParent.leftChild),
                            height(grandParent.rightChild)) + 1;
                }
            }
            
            parent.height = Math.max(height(parent.leftChild),
                                     height(parent.rightChild)) + 1;
            parent = parent.parentNode;
        }
    }
    
    /*    
         x             y
          \           / \
           y    ---> x   u
          / \         \
         z   u         z
     */
    private TreeListNode<E> leftRotate(TreeListNode<E> node) {
        TreeListNode<E> nodeRightChild = node.rightChild;
        nodeRightChild.parentNode = node.parentNode;
        node.parentNode = nodeRightChild;
        node.rightChild = nodeRightChild.leftChild;
        nodeRightChild.leftChild  = node;
        
        if (node.rightChild != null) {
            node.rightChild.parentNode = node;
        }
        
        node.height = Math.max(height(node.leftChild),
                               height(node.rightChild)) + 1;
        
        nodeRightChild.height = Math.max(height(nodeRightChild.leftChild), 
                                         height(nodeRightChild.rightChild)) + 1;
        
        nodeRightChild.leftSubtreeSize += nodeRightChild.leftSubtreeSize 
                                        + nodeRightChild.size;
        return nodeRightChild;
    }
    
    /*    
            x           y
           /           / \
          y     --->  z   x
         / \             /
        z   u           u
    */
    private TreeListNode<E> rightRotate(TreeListNode<E> node) {
        TreeListNode<E> nodeLeftChild = node.leftChild;
        nodeLeftChild.parentNode = node.parentNode;
        node.parentNode = nodeLeftChild;
        node.leftChild  = nodeLeftChild.rightChild;
        nodeLeftChild.rightChild = node;
        
        if (node.leftChild != null) {
            node.leftChild.parentNode = node;
        }
        
        node.height = Math.max(height(node.leftChild), 
                               height(node.rightChild)) + 1;
        
        nodeLeftChild.height = Math.max(height(nodeLeftChild.leftChild), 
                                        height(nodeLeftChild.rightChild)) + 1;
        
        node.leftSubtreeSize -= nodeLeftChild.leftSubtreeSize 
                              + nodeLeftChild.size;
        return nodeLeftChild;
    }
    
    private TreeListNode<E> rightLeftRotate(TreeListNode<E> node1) {
        TreeListNode<E> node2 = node1.rightChild;
        node1.rightChild = rightRotate(node2);
        return leftRotate(node1);
    }
    
    private TreeListNode<E> leftRightRotate(TreeListNode<E> node1) {
        TreeListNode<E> node2 = node1.leftChild;
        node1.leftChild = leftRotate(node2);
        return rightRotate(node1);
    }
    
    private int height(TreeListNode<E> node) {
        return node == null ? -1 : node.height;
    }
    
    //// Checking that the tree abides to the AVL-invariants.
    private boolean containsCycles() {
        Set<TreeListNode<E>> visitedNodes = new HashSet<>();
        return containsCycles(root, visitedNodes);
    }
    
    private boolean containsCycles(TreeListNode<E> current,
                                   Set<TreeListNode<E>> visitedNodes) {
        if (current == null) {
            return false;
        }
        
        if (visitedNodes.contains(current)) {
            return true;
        }
        
        visitedNodes.add(current);
        
        return containsCycles(current.leftChild,  visitedNodes) 
            || containsCycles(current.rightChild, visitedNodes);
    }
    
    private boolean heightsAreCorrect() {
        return getHeight(root) == root.height;
    }
    
    private int getHeight(final TreeListNode<E> node) {
        if (node == null) {
            return -1;
        }
        
        final int leftTreeHeight = getHeight(node.leftChild);
        
        if (leftTreeHeight == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        
        final int rightTreeHeight = getHeight(node.rightChild);
        
        if (rightTreeHeight == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        
        if (node.height == Math.max(leftTreeHeight, rightTreeHeight) + 1) {
            return node.height;
        }
        
        return Integer.MIN_VALUE;
    }
    
    private boolean isBalanced() {
        return isBalanced(root);
    }
    
    private boolean isBalanced(TreeListNode<E> node) {
        if (node == null) {
            return true;
        }
        
        if (!isBalanced(node.leftChild)) {
            return false;
        }
        
        if (!isBalanced(node.rightChild)) {
            return false;
        }
        
        int leftChildHeight  = height(node.leftChild);
        int rightChildHeight = height(node.rightChild);
        
        return Math.abs(leftChildHeight - rightChildHeight) < 2;
    }
    
    private boolean isWellIndexed() {
        return count(root) == size && 
               count(root.leftChild) == root.leftSubtreeSize;
    }
    
    private int count(TreeListNode<E> node) {
        if (node == null) {
            return 0;
        }
        
        int leftTreeSize = count(node.leftChild);
        
        if (leftTreeSize == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        
        if (node.leftSubtreeSize != leftTreeSize) {
            return Integer.MIN_VALUE;
        }
        
        int rightTreeSize = count(node.rightChild);
        
        if (rightTreeSize == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        
        return leftTreeSize + node.size + rightTreeSize;
    }
    
    private void checkAdditionIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Negative addition index: " + index);
        }
        
        if (index > size) {
            throw new IllegalArgumentException(
                    "Addition index is too large: " + index + ". List size: " +
                    size + ".");
        }
    }
}

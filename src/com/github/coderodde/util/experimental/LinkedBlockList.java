package com.github.coderodde.util.experimental;

/**
 * This class implements an experimental linked list data structure that 
 * combines linked list with array-based list.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 22, 2018)
 */
public final class LinkedBlockList<T> {

    private static final int DEFAULT_BLOCK_CAPACITY = 64;
    private static final int MINIMUM_BLOCK_CAPACITY = 4;

    /**
     * This static inner class implements the actual blocks storing the 
     * elements.
     * 
     * @param <T> the element type.
     */
    private static final class Block<T>  {

        /**
         * The length of {@code array}.
         */
        final int capacity;

        /**
         * The mask used for modulo computation.
         */
        final int indexMask;

        /**
         * The number of elements in this block.
         */
        int size;

        /**
         * The index of the very first element in this block.
         */
        int headIndex;

        /**
         * The array holding all the elements belonging to this block.
         */
        T[] array;

        /**
         * The previous block.
         */
        Block<T> previousBlock;

        /**
         * The next block.
         */
        Block<T> nextBlock;

        Block(int capacity) {
            this.capacity = capacity;
            this.indexMask = capacity - 1;
            this.array = (T[]) new Object[capacity];
        }

        T get(int logicalIndex) {
            return array[(headIndex + logicalIndex) & indexMask];
        }

        void setNull(int logicalIndex) {
            array[(headIndex + logicalIndex) & indexMask] = null;
        }
    }

    /**
     * The number of elements in this list.
     */
    private int size;

    /**
     * The number of blocks contained by this list.
     */
    private int blocks;

    /**
     * The first block in the chain.
     */
    private Block<T> headBlock;

    /**
     * The last block in the chain.
     */
    private Block<T> tailBlock;

    /**
     * The block capacity.
     */
    private int blockCapacity;
    /**
     * The mask used for index computation.
     */
    private int indexMask;

    public LinkedBlockList(int blockCapacity) {
        blockCapacity = Math.max(blockCapacity, MINIMUM_BLOCK_CAPACITY);
        blockCapacity = ceilToPowerOfTwo(blockCapacity);
        this.blockCapacity = blockCapacity;
        this.indexMask = blockCapacity - 1;
    }

    public LinkedBlockList() {
        this(DEFAULT_BLOCK_CAPACITY);
    }

    public void add(int index, T element) {
        checkAddIndex(index);

        if (size == 0) {
            headBlock = new Block<>(blockCapacity);
            tailBlock = headBlock;
            headBlock.array[0] = element;
            headBlock.size = 1;
            size = 1;
            return;
        }

        Block<T> block = headBlock;

        while (index > block.size) {
            index -= block.size;
            block = block.nextBlock;
        }

        if (block.size == block.capacity) {
            // Create a new block and move to it as little elements as possible:
            int elementsOnLeft = index;
            int elementsOnRight = block.size - index;
            Block<T> newBlock = new Block<>(blockCapacity);

            if (elementsOnLeft < elementsOnRight) {
                // Add newBlock before block and move to it the prefix of the
                // current block and append the new element:
                for (int newBlockIndex = 0; 
                         newBlockIndex < elementsOnLeft; 
                         newBlockIndex++) {
                    newBlock.array[newBlockIndex] = block.get(newBlockIndex);
                    block.setNull(newBlockIndex);
                }

                newBlock.array[elementsOnLeft] = element;
                newBlock.size = elementsOnLeft + 1;
                newBlock.nextBlock = block;
                newBlock.previousBlock = block.previousBlock;
                block.previousBlock = newBlock;
                block.size -= elementsOnLeft;
                block.headIndex = 
                        (block.headIndex + elementsOnLeft) & indexMask;

                if (newBlock.previousBlock == null) {
                    headBlock = newBlock;
                } else {
                    newBlock.previousBlock.nextBlock = newBlock;
                }
            } else {
                block.size -= elementsOnRight;
                newBlock.array[0] = element;
                int targetIndex = 1;
                
                for (int newBlockIndex = index; 
                         newBlockIndex < elementsOnRight;
                         newBlockIndex++) {
                    newBlock.array[targetIndex] = block.get(newBlockIndex);
                    block.setNull(newBlockIndex);
                    targetIndex++;
                }

                newBlock.size = elementsOnRight + 1;
                newBlock.previousBlock = block;
                newBlock.nextBlock = block.nextBlock;
                block.nextBlock = newBlock;

                if (newBlock.nextBlock == null) {
                    tailBlock = newBlock;
                } else {
                    newBlock.nextBlock.previousBlock = newBlock;
                }
            }
        } else {
            // The current block is not full so insert into it:
            int elementsOnLeft = index;
            int elementsOnRight = block.size - index;

            if (elementsOnLeft < elementsOnRight) {
                // Shift the leftmost elements one position to the left:
                for (int elementIndex = 0; 
                         elementIndex < elementsOnLeft; 
                         elementIndex++) {
                    int sourceIndex = 
                            (block.headIndex + elementIndex) 
                            & indexMask;

                    int targetIndex = 
                            (block.headIndex + elementIndex - 1) 
                            & indexMask;

                    block.array[targetIndex] = block.array[sourceIndex];
                }

                block.array[(block.headIndex + index - 1) & indexMask] =
                        element;
                block.headIndex = (block.headIndex - 1) & indexMask;
                block.size++;
            } else {
                // Shift the rightmost elements one position to the right:
                for (int elementIndex = 0;
                         elementIndex < elementsOnRight; 
                         elementIndex++) {
                    int sourceIndex = 
                            (block.headIndex + block.size - elementIndex - 1) 
                            & indexMask;

                    int targetIndex = 
                            (block.headIndex + block.size - elementIndex) 
                            & indexMask;

                    block.array[targetIndex] = block.array[sourceIndex];
                }

                block.array[(block.headIndex + index) & indexMask] = element;
                block.size++;
            }
        }

        size++;
    }

    public T get(int index) {
        checkAccessIndex(index);
        Block<T> block = headBlock;

        while (index >= block.size) {
            index -= block.size;
            block = block.nextBlock;
        }

        return block.get(index);
    }

    public void remove(int index) {
        checkAccessIndex(index);
        Block<T> targetBlock = headBlock;

        while (index >= targetBlock.size) {
            index -= targetBlock.size;
            targetBlock = targetBlock.nextBlock;
        }

        if (targetBlock.size == 1) {
            // The target block contains only one element. Unlink it from the
            // chain of blocks:
            if (targetBlock == headBlock) {
                headBlock = headBlock.nextBlock;

                if (headBlock != null) {
                    headBlock.previousBlock = null;                    
                }
            } else {
                targetBlock.previousBlock.nextBlock = targetBlock.nextBlock;
            }

            if (targetBlock == tailBlock) {
                tailBlock = tailBlock.previousBlock;

                if (tailBlock != null) {
                    tailBlock.nextBlock = null;
                }
            } else {
                targetBlock.nextBlock.previousBlock = targetBlock.previousBlock;
            }
        } else {
            int elementsOnLeft = index;
            int elementsOnRight = targetBlock.size - index - 1;

            if (elementsOnLeft < elementsOnRight) {
                // Shift the leftmost elements in the target block one position
                // to the right:
                for (int i = index - 1; i >= 0; i--) {
                    int sourceIndex = (targetBlock.headIndex + i) & indexMask;
                    int targetIndex = (targetBlock.headIndex + i + 1) 
                            & indexMask;
                    targetBlock.array[targetIndex] = 
                            targetBlock.array[sourceIndex];
                }

                targetBlock.setNull(0);
                targetBlock.headIndex = (targetBlock.headIndex + 1) & indexMask;
                targetBlock.size--;
            } else {
                // Shift the rightmost elements in the target block one position
                // to the left:
                for (int i = index + 1; i < targetBlock.size; i++) {
                    int sourceIndex = (targetBlock.headIndex + i) & indexMask;
                    int targetIndex = (targetBlock.headIndex + i - 1) 
                            & indexMask;
                    targetBlock.array[targetIndex] = 
                            targetBlock.array[sourceIndex];
                }

                targetBlock.size--;
                targetBlock.setNull(targetBlock.size);
            }
        }

        size--;
    }

    public int size() {
        return size;
    }

    /**
     * Returns a number between zero and one indicating how densely the blocks
     * are.
     * 
     * @return density factor.
     */
    public float getDensityFactor() {
        return ((float) size) / blocks * blockCapacity;
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

    private static int ceilToPowerOfTwo(int number) {
        int ret = 1;

        while (ret < number) {
            ret <<= 1;
        }

        return ret;
    }
}

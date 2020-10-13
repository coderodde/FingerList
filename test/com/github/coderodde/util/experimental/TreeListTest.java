package com.github.coderodde.util.experimental;

import com.github.coderodde.util.experimental.TreeList;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class TreeListTest {
    
    @Test
    public void testMoveElementsToLeft() {
        TreeList<Integer> list = new TreeList<>();
        
        Integer[] sourceArray = new Integer[11];
        
        // sourceArray = [null, 1, 2, 3, 4, 5, 6, 7, 8, 9, null]
        for (int i = 1; i < sourceArray.length - 1; i++) {
            sourceArray[i] = i;
        } 
        
        Integer[] targetArray = new Integer[13];
        list.moveElementsToAnotherArray(sourceArray, targetArray, 7, 6);
        
        for (int i = 0; i < 4; i++) {
            assertEquals(Integer.valueOf(i + 6), targetArray[i]);
        }
        
        for (int i = 4; i <= 5; i++) {
            assertNull(targetArray[i]);
        }
        
        assertEquals(Integer.valueOf(1), targetArray[6]);
    }
    
    @Test
    public void testSmallTreeListWithDegree3() {
        TreeList<Integer> list = new TreeList<>(3);
        
        list.add(0, 1);
        list.add(0, 0);
        list.add(2, 2);
        
        assertTrue(list.isHealthy());
        
        list.add(0, -1);
        
        assertTrue(list.isHealthy());
        
        list.add(1, 13);
        
        assertTrue(list.isHealthy());
    }
    
//    
//    @Test
//    public void testSmallTree() {
//        TreeList<Integer> list = new TreeList<>(3);
//        list.add(0, 1);
//        list.add(0, 0);
//        assertTrue(list.isHealthy());
//        System.out.println("yeah");
//        list.add(2, 2);
//        list.add(1, 3);
//        
//        assertTrue(list.isHealthy());
//    }
    
//    @Test
    public void test2() {
        TreeList<Integer> list = new TreeList<>(1);
        
        assertTrue(list.isHealthy());
        list.add(0, 0);
        assertTrue(list.isHealthy());
        list.add(1, 1);
        assertTrue(list.isHealthy());
        list.add(0, 2);
        boolean b = list.isHealthy();
        assertTrue(b);
        
        System.out.println("Larger test.");
        long seed = 1L; //System.currentTimeMillis();
        int[] array = new int[100];
        Random random = new Random(seed);
        
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(i + 1);
        }
        
        for (int i : array) {
            System.out.println("i = " + i);
            list.add(i, i);
            assertTrue(list.isHealthy());
        }
        
        assertTrue(list.isHealthy());
    }
}

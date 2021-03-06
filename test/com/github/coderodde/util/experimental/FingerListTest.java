package com.github.coderodde.util.experimental;

import com.github.coderodde.util.experimental.FingerList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class FingerListTest {
    
    private FingerList<Integer> list;
    private static final Random random = new Random();
    
    @Before
    public void before() {
        list = new FingerList<>();
    }
    
    @Test
    public void test1() {
        list = new FingerList<>();
        
        for (int i = 0; i < 5; i++) {
            list.add(i, i);
        }
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, (int) list.get(i));
        }
        
        // Remove from tail:
        list.remove(4);
        
        assertEquals(3, (int) list.get(3));
        
        // Remove from middle:
        list.remove(2);
        list.remove(1);
        
        assertEquals(0, (int) list.get(0));
        assertEquals(3, (int) list.get(1));
        
        // Remove from head:
        list.remove(0);
        
        assertEquals(3, (int) list.get(0));
        
        // Remove last:
        list.remove(0);
    }
    
    @Test
    public void testSkewedAddition() {
        list.add(0, 0);
        list.add(1, 1);
        list.add(0, -1);
        list.add(1, 10);
        list.add(4, 100);
        
        // -1, 10, 0, 1, 100
        assertEquals(-1, (int) list.get(0));
        assertEquals(10, (int) list.get(1));
        assertEquals(0,  (int) list.get(2));
        assertEquals(1,  (int) list.get(3));
        assertEquals(100, (int) list.get(4));
        
        list.remove(2);
        
        assertEquals(-1,  (int) list.get(0));
        assertEquals(10,  (int) list.get(1));
        assertEquals(1,   (int) list.get(2));
        assertEquals(100, (int) list.get(3));
        
        //5, -1, 10, 1, 100
        list.add(0, 5);
        
        assertEquals(5, (int) list.get(0));
        
        //5, -1, 10, 1, 100, 55
        list.add(5, 55);
       
        assertEquals(55, (int) list.get(5));
        
        list.remove(4);
        
        assertEquals(5,    (int) list.get(0));
        assertEquals(-1,   (int) list.get(1));
        assertEquals(10,   (int) list.get(2));
        assertEquals(1,    (int) list.get(3));
        assertEquals(55,   (int) list.get(4));
    }
    
    @Test
    public void bruteForceTest() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        
        System.out.println("Seed = " + seed);
        
        List<Integer> javaList = new LinkedList<>();
        FingerList<Integer> fingerList = new FingerList<>();
        
        for (int operationNumber = 0; 
                 operationNumber < 10_000; 
                 operationNumber++) {
            int operationCode = random.nextInt(3);
            System.out.print(operationNumber + " ");
            switch (operationCode) {
                // Remove:
                case 0:
                    if (javaList.size() > 0) {
                        System.out.print("remove ");
                        int index = random.nextInt(javaList.size());
                        javaList.remove(index);
                        
                        try {
                            fingerList.remove(index);
                            System.out.println(fingerList.hasCorrectState());
                        } catch (Exception ex) {
                            System.out.println(
                                    ex.getMessage() + " on " + 
                                    operationNumber + " in remove()");
                        }
                        
                        if (!equals(javaList, fingerList)) {
                            fail("Failed while removing at index " + index);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
                    
                // Add:
                case 1:
                    
                    if (javaList.size() < 10) {
                        System.out.print("add ");
                        // Do not create large lists, max. 10 elements.
                        Integer integer = random.nextInt(1000);
                        int index = random.nextInt(javaList.size() + 1);
                        javaList.add(index, integer);
                        
                        try {
                            fingerList.add(index, integer);
                            System.out.println(fingerList.hasCorrectState());
                        } catch (Exception ex) {
                            System.out.println(
                                    ex.getMessage() + " on " + 
                                    operationNumber + " in add()");
                        }

                        if (!equals(javaList, fingerList)) {
                            fail("Failed while adding at index " + index + 
                                 " value " + integer);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
                    
                // Get:
                case 2:
                    
                    if (javaList.size() > 0) {
                        System.out.print("get "); 
                        int index = random.nextInt(javaList.size());
                        int javaListInt = javaList.get(index);
                        int fingerListInt = fingerList.get(index);
                        System.out.println(fingerList.hasCorrectState());

                        if (javaListInt != fingerListInt) {
                            fail("Failed while getting at index " + index +
                                 ", " + javaListInt + " vs. " + fingerListInt);
                        }
                    } else {
                        System.out.println();
                    }
                    
                    break;
            }
        }
    }
    
    private static boolean equals(List<Integer> javaList,
                                  FingerList<Integer> fingerList) {
        if (javaList.size() != fingerList.size()) {
            return false;
        }
        
        Iterator<Integer> javaListIterator = javaList.iterator();
        int fingerListIndex = 0;
        
        while (javaListIterator.hasNext()) {
            Integer javaListInteger = javaListIterator.next();
            Integer fingerListInteger = fingerList.get(fingerListIndex);
            
            if (!Objects.equals(javaListInteger, fingerListInteger)) {
                return false;
            }
            
            fingerListIndex++;
        }
        
        return true;
    }
}

package net.coderodde.util.experimental;

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
            
            switch (operationCode) {
                // Remove:
                case 0:
                    if (javaList.size() > 0) {
                        int index = random.nextInt(javaList.size() + 1);
                        javaList.remove(index);
                        fingerList.remove(index);
                        
                        if (!equals(javaList, fingerList)) {
                            fail("Failed while removing at index " + index);
                        }
                    }
                    
                    break;
                    
                // Add:
                case 1:
                    Integer integer = random.nextInt(1000);
                    int index = random.nextInt(javaList.size() + 1);
                    javaList.add(index, integer);
                    fingerList.add(index, integer);
                    
                    if (!equals(javaList, fingerList)) {
                        fail("Failed while adding at index " + index + 
                             " value " + integer);
                    }
                    
                    break;
                    
                // Get:
                case 2:
                    if (javaList.size() > 0) {
                        index = random.nextInt(javaList.size());
                        int javaListInt = javaList.get(index);
                        int fingerListInt = fingerList.get(index);

                        if (javaListInt != fingerListInt) {
                            fail("Failed while getting at index " + index +
                                 ", " + javaListInt + " vs. " + fingerListInt);
                        }
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
        int index = 0;
        
        while (javaListIterator.hasNext()) {
            if (Objects.equals(javaListIterator.next(), 
                               fingerList.get(index))) {
                return false;
            }
        }
        
        return true;
    }
}

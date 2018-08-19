package net.coderodde.util.experimental;

import java.util.Random;
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
        
        assertEquals(1, (int) list.get(0));
        
        // Remove last:
        list.remove(0);
    }
}

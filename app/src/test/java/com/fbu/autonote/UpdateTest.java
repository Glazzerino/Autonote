package com.fbu.autonote;

import com.fbu.autonote.utilities.LRUCache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UpdateTest  {
    @Test
    public void updateTest() {
        int limit = 3;
        LRUCache<Integer> test = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            test.update(i);
        }
        test.update(0);
        int back = test.container.getBack().data;
        assertEquals(null, test.container.getFront().prev);
    }
}

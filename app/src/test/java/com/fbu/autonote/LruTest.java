package com.fbu.autonote;

import com.fbu.autonote.models.CustomNode;
import com.fbu.autonote.utilities.LeastRecentlyUsed;

import org.junit.Test;

import static org.junit.Assert.*;

public class LruTest {
    @Test
    public void test_LRU() {
        int limit = 20;
        LeastRecentlyUsed<Integer> lruTest = new LeastRecentlyUsed<>(limit);
        for (int i=0; i<limit; i++) {
            lruTest.update(i);
        }

        int times = 0;
        for (LeastRecentlyUsed<Integer> it = lruTest; it.hasNext(); ) {
            Integer n = it.next();
            System.out.println(String.valueOf(n));
            times++;
        }

        assertEquals(limit-1, times);
    }
}
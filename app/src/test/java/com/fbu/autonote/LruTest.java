package com.fbu.autonote;

import com.fbu.autonote.utilities.LRUCache;

import org.junit.Test;

import static org.junit.Assert.*;

public class LruTest {
    @Test
    public void reinsertion() {
        int limit = 20;
        LRUCache<Integer> lruTest = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            lruTest.update(i);
        }
        for (int i=limit; i>limit/2; i--) {
            lruTest.update(i);
        }
        for (LRUCache<Integer> it = lruTest; it.hasNext(); ) {
            System.out.println(String.valueOf(it.next()));
        }

        assertEquals(limit, lruTest.size());
    }

    @Test
    public void test_LRU() {
        int limit = 20;
        LRUCache<Integer> lruTest = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            lruTest.update(i);
        }

        int times = 0;
        for (LRUCache<Integer> it = lruTest; it.hasNext(); ) {
            System.out.println(String.valueOf(it.next()));
            times++;
        }

        assertEquals(limit, times);
    }
}
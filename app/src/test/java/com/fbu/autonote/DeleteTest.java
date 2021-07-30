package com.fbu.autonote;

import com.fbu.autonote.utilities.LRUCache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteTest {
    @Test
    public void deleteBack() {
        int limit = 100;
        LRUCache<Integer> cache = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            cache.update(i);
        }
        cache.delete(99);
        int back = cache.container.getBack().data;
        assertEquals(98, back);
    }

    @Test
    public void deleteFront() {
        int limit = 100;
        LRUCache<Integer> cache = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            cache.update(i);
        }
        cache.delete(0);
        int front = cache.container.getFront().data;
        assertEquals(1, front);
    }

    @Test
    public void confirmTotalDeletion() {
        int limit = 3;
        LRUCache<Integer> cache = new LRUCache<>(limit);
        for (int i=0; i<limit; i++) {
            cache.update(i);
        }
        cache.delete(0);
        cache.delete(1);
        cache.delete(2);

        for (LRUCache<Integer> it = cache; it.hasNext(); ) {
            System.out.println(String.valueOf(it.next()));
        }
        assertEquals(0, cache.size());
    }
}

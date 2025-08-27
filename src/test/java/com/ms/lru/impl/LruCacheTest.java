package com.ms.lru.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LruCacheTest {

    @Test
    void evictsLeastRecentlyUsedOnOverflow() {
        LruCache<Integer, String> cache = new LruCache<>(3);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");          // full: LRU=1, MRU=3

        cache.get(1);               // touch 1 → LRU=2
        cache.put(4, "D");          // evict 2

        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertEquals(3, cache.size());
    }

    @Test
    void getRefreshesRecency() {
        LruCache<Integer, String> cache = new LruCache<>(2);

        cache.put(10, "X");         // [10]
        cache.put(20, "Y");         // [10,20] LRU=10
        cache.get(10);              // [20,10] LRU=20
        cache.put(30, "Z");         // evict 20

        assertTrue(cache.containsKey(10));
        assertTrue(cache.containsKey(30));
        assertFalse(cache.containsKey(20));
    }

    @Test
    void putExistingKeyReplacesAndRefreshes() {
        LruCache<Integer, String> cache = new LruCache<>(2);

        cache.put(1, "one");
        cache.put(2, "two");        // LRU=1
        cache.put(1, "ONE");        // refresh 1 → LRU=2
        cache.put(3, "three");      // evict 2

        assertEquals("ONE", cache.get(1));
        assertTrue(cache.containsKey(3));
        assertFalse(cache.containsKey(2));
        assertEquals(2, cache.size());
    }

    @Test
    void computeIfAbsentInsertsOnMissAndRefreshesOnHit() {
        LruCache<Integer, String> cache = new LruCache<>(2);

        String v1 = cache.computeIfAbsent(7, k -> "S"); // miss → insert
        assertEquals("S", v1);
        assertEquals(1, cache.size());

        String v2 = cache.computeIfAbsent(7, k -> "T"); // hit → same value, refreshed
        assertEquals("S", v2);
        assertEquals(1, cache.size());

        cache.put(8, "U");          // now full: LRU should be 8 or 7 depending on last access
        cache.put(9, "V");          // one of {7,8} gets evicted; since 7 was just hit, 8 is LRU

        assertFalse(cache.containsKey(7), "hit should have refreshed key 7");
        assertTrue(cache.containsKey(8), "8 should be evicted");
        assertTrue(cache.containsKey(9));
    }

    @Test
    void computeIfAbsentNullDoesNotInsert() {
        LruCache<Integer, String> cache = new LruCache<>(2);

        String v = cache.computeIfAbsent(42, k -> null);
        assertNull(v);
        assertEquals(0, cache.size());
        assertFalse(cache.containsKey(42));
    }

    @Test
    void capacityZeroActsAsNoStore() {
        LruCache<Integer, String> cache = new LruCache<>(0);

        cache.put(1, "A");                  // will be immediately evicted
        assertEquals(0, cache.size());
        assertNull(cache.get(1));

        String r = cache.computeIfAbsent(2, k -> "B"); // return value but don't store
        assertEquals("B", r);
        assertEquals(0, cache.size());
        assertNull(cache.get(2));
    }

    @Test
    void clearResetsState() {
        LruCache<Integer, String> cache = new LruCache<>(2);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.clear();

        assertEquals(0, cache.size());
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));

        cache.put(3, "C");
        cache.put(4, "D");
        cache.put(5, "E"); // evict 3

        assertFalse(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
    }
}

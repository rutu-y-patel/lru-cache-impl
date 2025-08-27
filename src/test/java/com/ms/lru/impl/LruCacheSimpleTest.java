package com.ms.lru.impl;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LruCacheSimpleTest {

    @Test
    void evictsLeastRecentlyUsedOnOverflow() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(3);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");       // full: [1,2,3]  (1 = LRU, 3 = MRU)

        cache.get(1);            // touch 1 → recency: [2,3,1] (2 = LRU)
        cache.put(4, "D");       // overflow → evict 2

        assertFalse(cache.containsKey(2), "2 should be evicted (LRU)");
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertEquals(3, cache.size());
    }

    @Test
    void getUpdatesRecencySoNextOverflowEvictsTheOtherKey() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        cache.put(10, "X");
        cache.put(20, "Y");      // full: [10,20] (10 = LRU)
        cache.get(10);           // touch 10 → [20,10] (20 = LRU)
        cache.put(30, "Z");      // overflow → evict 20

        assertTrue(cache.containsKey(10));
        assertTrue(cache.containsKey(30));
        assertFalse(cache.containsKey(20), "20 should be evicted after 10 was accessed");
    }

    @Test
    void putExistingKeyReplacesValueAndRefreshesRecency() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        cache.put(1, "one");
        cache.put(2, "two");          // full: [1,2] (1 = LRU)
        cache.put(1, "ONE");          // replace & refresh → [2,1] (2 = LRU)
        cache.put(3, "three");        // overflow → evict 2

        assertEquals("ONE", cache.get(1));
        assertTrue(cache.containsKey(3));
        assertFalse(cache.containsKey(2), "2 should be evicted after 1 was refreshed");
        assertEquals(2, cache.size());
    }

    @Test
    void computeIfAbsentInsertsOnlyOnMissAndDoesNotReplaceExisting() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        cache.put(1, "A");
        String r1 = cache.computeIfAbsent(1, k -> "B"); // hit → no replace
        assertEquals("A", r1);
        assertEquals("A", cache.get(1));
        assertEquals(1, cache.size());

        String r2 = cache.computeIfAbsent(2, k -> "B"); // miss → insert
        assertEquals("B", r2);
        assertEquals(2, cache.size());
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
    }

    @Test
    void computeIfAbsentReturnsNullDoesNotInsert() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        String r = cache.computeIfAbsent(99, k -> null); // mapping returns null → no insert
        assertNull(r);
        assertFalse(cache.containsKey(99));
        assertEquals(0, cache.size());
    }

    @Test
    void capacityZeroMeansAlwaysEmpty() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(0);

        cache.put(1, "A");
        cache.put(2, "B");
        assertEquals(0, cache.size());
        assertNull(cache.get(1));
        assertNull(cache.get(2));

        // computeIfAbsent should also end up evicting immediately
        cache.computeIfAbsent(3, k -> "C");
        assertEquals(0, cache.size());
        assertNull(cache.get(3));
    }

    @Test
    void negativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LruCacheSimple<>(-1));
    }

    @Test
    void reinsertSameKeyDoesNotGrowSize() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        cache.put(7, "A");
        cache.put(7, "B"); // replace
        assertEquals(1, cache.size());
        assertEquals("B", cache.get(7));
    }

    @Test
    void clearEmptiesCacheAndRespectsCapacityAfterwards() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(2);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.clear();
        assertEquals(0, cache.size());
        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsKey(2));

        cache.put(3, "C");
        cache.put(4, "D");
        cache.put(5, "E");  // overflow → evict 3
        assertFalse(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
        assertTrue(cache.containsKey(5));
    }

    @Test
    void overflowEvictsTheTrueLruAfterMixedAccess() {
        LruCacheSimple<Integer, String> cache = new LruCacheSimple<>(3);

        cache.put(100, "a");
        cache.put(200, "b");
        cache.put(300, "c");   // [100,200,300]

        cache.get(100);        // [200,300,100]
        cache.get(300);        // [200,100,300]
        cache.put(400, "d");   // overflow → evict 200

        assertFalse(cache.containsKey(200));
        assertTrue(cache.containsKey(100));
        assertTrue(cache.containsKey(300));
        assertTrue(cache.containsKey(400));
        assertEquals(3, cache.size());
    }

}

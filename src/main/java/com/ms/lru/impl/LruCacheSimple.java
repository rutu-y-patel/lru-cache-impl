package com.ms.lru.impl;

import com.ms.lru.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LruCacheSimple<K, V> implements Cache<K, V> {

    private final int capacity;
    private static final float LOAD_FACTOR = 0.80f;
    private final Map<K, V> lruCacheMap;

    public LruCacheSimple(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0");
        this.capacity = capacity;
        int initialCapacity = Math.max(5, (int) Math.ceil((capacity + 1) / LOAD_FACTOR));

        this.lruCacheMap = new LinkedHashMap<>(initialCapacity, LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LruCacheSimple.this.capacity;
            }
        };
    }

    @Override
    public V get(K key) {
        return lruCacheMap.get(key);
    }

    @Override
    public void put(K key, V value) {
        lruCacheMap.put(key, value);

    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V existing = lruCacheMap.get(key);                 // moves to MRU if present
        if (existing != null || lruCacheMap.containsKey(key)) {
            return existing;                       // present (even if null)
        }
        return lruCacheMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return lruCacheMap.containsKey(key);
    }

    @Override
    public int size() {
        return lruCacheMap.size();
    }

    @Override
    public void clear() {
        lruCacheMap.clear();
    }

    @Override
    public void close() {
        //no-op
    }


}

package com.ms.lru.cache;

import java.util.function.Function;

public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    boolean containsKey(K key);

    int size();

    void clear();

    void close();
}

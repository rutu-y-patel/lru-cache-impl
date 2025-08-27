package com.ms.lru.impl;

import com.ms.lru.cache.Cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class LruCache<K, V> implements Cache<K, V>, AutoCloseable {

    private final int capacity;
    private final Map<K, Node<K, V>> map;

    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);

    public LruCache(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0");
        this.capacity = capacity;
        this.map = new HashMap<>(Math.max(16, (int) Math.ceil(capacity / 0.75) + 1));
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if(node == null) return null;
        moveToFront(node);
        return node.value;
    }

    @Override
    public void put(K key, V value) {
        Node<K, V> existingNode = map.get(key);

        if(existingNode != null){
            existingNode.value = value;
            moveToFront(existingNode);
        }
        else {
            Node<K, V> newNode = new Node<>(key, value);
            map.put(key, newNode);
            addFirst(newNode);
            if(map.size() > capacity){
                evictTail();
            }
        }

    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        Node<K, V> node = map.get(key);
        if(node != null){
            moveToFront(node);
            return node.value;
        }
        V computedValue = mappingFunction.apply(key);
        if(computedValue != null){
            put(key, computedValue);
        }
        return computedValue;
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void close() {

    }

    private void moveToFront(Node<K,V> node) {
        remove(node);
        addFirst(node);
    }

    private void addFirst(Node<K,V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void remove(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    private void evictTail() {
        if(tail.prev == head) return;
        Node<K, V> lru = tail.prev;
        remove(lru);
        map.remove(lru.key);
    }


    static final class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }
}

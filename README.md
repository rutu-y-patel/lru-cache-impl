# LRU Cache (Java)

Two implementations:
- `LruCacheSimple` — LinkedHashMap-based (accessOrder=true)
- `LruCache` — HashMap + Doubly-Linked List (O(1) get/put/move)

## Features
- O(1) get/put/computeIfAbsent
- Capacity-bounded eviction (LRU)
- Null-avoidance on compute (optional)
- Easy to extend with stats/eviction callbacks

## Usage (Maven/Java 17+)
```java
Cache<String,String> cache = new LruCache<>(3);
cache.put("a","A"); cache.put("b","B"); cache.put("c","C");
cache.get("a");                 // 'a' becomes MRU
cache.put("d","D");             // evicts LRU ('b')
System.out.println(cache.size()); // 3

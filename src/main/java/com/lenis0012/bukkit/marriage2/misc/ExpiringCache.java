package com.lenis0012.bukkit.marriage2.misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Small time-based cache used instead of Guava.
 * Modern Paper servers do not expose Guava to plugins.
 */
public final class ExpiringCache<K, V> {
    private static final class TimedValue<V> {
        private final V value;
        private final long expiresAt;

        private TimedValue(V value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    private final ConcurrentHashMap<K, TimedValue<V>> entries = new ConcurrentHashMap<K, TimedValue<V>>();
    private final long ttlMillis;

    public ExpiringCache(long duration, TimeUnit unit) {
        this.ttlMillis = Math.max(0L, unit.toMillis(duration));
    }

    public V getIfPresent(K key) {
        TimedValue<V> entry = entries.get(key);
        if(entry == null) {
            return null;
        }
        if(ttlMillis <= 0L || entry.expiresAt <= System.currentTimeMillis()) {
            entries.remove(key, entry);
            return null;
        }
        return entry.value;
    }

    public void put(K key, V value) {
        if(ttlMillis <= 0L) {
            return;
        }
        entries.put(key, new TimedValue<V>(value, System.currentTimeMillis() + ttlMillis));
    }
}

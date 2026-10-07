/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lembergmax.justmath.bignumber.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import lombok.NonNull;

/**
 * A thread-safe cache that holds at most a fixed number of entries and evicts the least recently
 * used entry when it is full.
 *
 * <p>The loader runs outside the lock. Two threads that miss on the same key at the same time may
 * both run the loader, and the first result stays in the cache. Use this cache only for loaders
 * that return equal values for equal keys and have no side effects.</p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class BoundedCache<K, V> {

    private final int maximumEntries;

    private final Map<K, V> entries;

    /**
     * Creates an empty cache.
     *
     * @param maximumEntries the largest number of entries the cache keeps; must be at least 1
     * @throws IllegalArgumentException if {@code maximumEntries} is less than 1
     */
    public BoundedCache(final int maximumEntries) {
        if (maximumEntries < 1) {
            throw new IllegalArgumentException("maximumEntries must be at least 1, was " + maximumEntries);
        }
        this.maximumEntries = maximumEntries;
        this.entries = new LeastRecentlyUsedMap<>(maximumEntries);
    }

    /**
     * Returns the value cached for {@code key}, or loads, caches and returns it. Reading a value marks
     * it as the most recently used.
     *
     * @param key    the key to look up; must not be {@code null}
     * @param loader computes the value for a missing key; must not be {@code null} and must not
     *               return {@code null}
     * @return the cached or newly loaded value; never {@code null}
     * @throws NullPointerException if {@code loader} returns {@code null}
     */
    public V computeIfAbsent(
            @NonNull final K key,
            @NonNull final Function<? super K, ? extends V> loader
    ) {
        synchronized (entries) {
            final V cached = entries.get(key);
            if (cached != null) {
                return cached;
            }
        }

        final V loaded = Objects.requireNonNull(loader.apply(key), "loader must not return null");

        synchronized (entries) {
            final V winner = entries.putIfAbsent(key, loaded);
            return winner != null ? winner : loaded;
        }
    }

    /**
     * Returns the number of entries currently cached.
     *
     * @return the entry count, at most {@link #getMaximumEntries()}
     */
    public int size() {
        synchronized (entries) {
            return entries.size();
        }
    }

    /**
     * Returns the largest number of entries this cache keeps.
     *
     * @return the upper bound given at construction; at least 1
     */
    public int getMaximumEntries() {
        return maximumEntries;
    }

    private static final class LeastRecentlyUsedMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;

        private static final int INITIAL_CAPACITY = 16;

        private static final float LOAD_FACTOR = 0.75f;

        private final int maximumEntries;

        private LeastRecentlyUsedMap(final int maximumEntries) {
            super(INITIAL_CAPACITY, LOAD_FACTOR, true);
            this.maximumEntries = maximumEntries;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
            return size() > maximumEntries;
        }

    }

}

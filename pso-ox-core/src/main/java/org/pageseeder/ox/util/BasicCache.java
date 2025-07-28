/*
 * Copyright 2021 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * A really basic caching implementation backed by a hashtable.
 *
 * @param <T> the type parameter
 * @author Christophe Lauret
 * @since 28 October 2013
 */
public class BasicCache<T> {

  /**
   * Maps XSLT templates to their URL as a string for easy retrieval.
   */
  private final Map<String, CachedItem<T>> cache = new Hashtable<String, CachedItem<T>>();

  /**
   * Initialise a new basic cache.
   */
  public BasicCache() {
  }

  /**
   * Get t.
   *
   * @param key      the key
   * @param modified the modified
   * @return the t
   */
  public T get(String key, long modified) {
    CachedItem<T> cached = this.cache.get(key);
    T item = null;
    // There is an entry in the cache
    if (cached != null) {
      if (cached.timestamp() > modified) {
        item = cached.item();
      } else {
        this.cache.remove(key);
      }
    }
    return item;
  }

  /**
   * Put.
   *
   * @param key  the key
   * @param item the item
   */
  public void put(String key, T item) {
    this.cache.put(key, new CachedItem<T>(item));
  }

  /**
   * Clear.
   */
  public void clear() {
    this.cache.clear();
  }

  /**
   * Templates with a timestamp for caching.
   *
   * @author Christophe Lauret
   * @since  28 October 2013
   */
  private static final class CachedItem<T> implements Serializable {

    /** As per requirement for Serializable. */
    private static final long serialVersionUID = -5296958870819118637L;

    private final long timestamp;
    private final T item;

    /**
     * Create a new cached items setting the timestamp to the current time.
     *
     * @param o the o
     */
    public CachedItem(T o) {
      this.item = o;
      this.timestamp = System.currentTimeMillis();
    }

    /**
     * Timestamp long.
     *
     * @return the modified
     */
    public long timestamp() {
      return this.timestamp;
    }

    /**
     * Item t.
     *
     * @return the cached item
     */
    public T item() {
      return this.item;
    }
  }
}

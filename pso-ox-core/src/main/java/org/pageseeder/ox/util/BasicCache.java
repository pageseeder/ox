/*
 * Copyright (c) 1999-2014 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * A really basic caching implementation backed by a hashtable.
 *
 * @author Christophe Lauret
 * @since  28 October 2013
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

  public void put(String key, T item) {
    this.cache.put(key, new CachedItem<T>(item));
  }

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
     */
    public CachedItem(T o) {
      this.item = o;
      this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return the modified
     */
    public long timestamp() {
      return this.timestamp;
    }

    /**
     * @return the cached item
     */
    public T item() {
      return this.item;
    }
  }
}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The <code>LRUCache</code> is a hashtable that stores a finite number of elements.
 * When an attempt is made to add values to a full cache, the least recently used values
 * in the cache are discarded to make room for the new values as necessary.
 *
 * <p>The data structure is based on the LRU virtual memory paging scheme.
 *
 * <p>Objects can take up a variable amount of cache space by implementing
 * the <code>ILRUCacheable</code> interface.
 *
 * <p>This implementation is NOT thread-safe.  Synchronization wrappers would
 * have to be added to ensure atomic insertions and deletions from the cache.
 *
 * @see org.eclipse.jdt.internal.core.util.ILRUCacheable
 */
public class LRUCache implements Cloneable {

	/**
	 * This type is used internally by the LRUCache to represent entries
	 * stored in the cache.
	 * It is static because it does not require a pointer to the cache
	 * which contains it.
	 *
	 * @see LRUCache
	 */
	protected static class LRUCacheEntry {

		/**
		 * Hash table key
		 */
		public Object key;

		/**
		 * Hash table value (an LRUCacheEntry object)
		 */
		public Object value;

		/**
		 * Time value for queue sorting
		 */
		public int timestamp;

		/**
		 * Cache footprint of this entry
		 */
		public int space;

		/**
		 * Previous entry in queue
		 */
		public LRUCacheEntry previous;

		/**
		 * Next entry in queue
		 */
		public LRUCacheEntry next;

		/**
		 * Creates a new instance of the receiver with the provided values
		 * for key, value, and space.
		 */
		public LRUCacheEntry (Object key, Object value, int space) {
			this.key = key;
			this.value = value;
			this.space = space;
		}

		/**
		 * Returns a String that represents the value of this object.
		 */
		public String toString() {

			return "LRUCacheEntry [" + this.key + "-->" + this.value + "]"; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Amount of cache space used so far
	 */
	protected int currentSpace;

	/**
	 * Maximum space allowed in cache
	 */
	protected int spaceLimit;

	/**
	 * Counter for handing out sequential timestamps
	 */
	protected int timestampCounter;

	/**
	 * Hash table for fast random access to cache entries
	 */
	protected Hashtable entryTable;

	/**
	 * Start of queue (most recently used entry)
	 */
	protected LRUCacheEntry entryQueue;

	/**
	 * End of queue (least recently used entry)
	 */
	protected LRUCacheEntry entryQueueTail;

	/**
	 * Default amount of space in the cache
	 */
	protected static final int DEFAULT_SPACELIMIT = 100;
	/**
	 * Creates a new cache.  Size of cache is defined by
	 * <code>DEFAULT_SPACELIMIT</code>.
	 */
	public LRUCache() {

		this(DEFAULT_SPACELIMIT);
	}
	/**
	 * Creates a new cache.
	 * @param size Size of Cache
	 */
	public LRUCache(int size) {

		this.timestampCounter = this.currentSpace = 0;
		this.entryQueue = this.entryQueueTail = null;
		this.entryTable = new Hashtable(size);
		this.spaceLimit = size;
	}
	/**
	 * Returns a new cache containing the same contents.
	 *
	 * @return New copy of object.
	 */
	public Object clone() {

		LRUCache newCache = newInstance(this.spaceLimit);
		LRUCacheEntry qEntry;

		/* Preserve order of entries by copying from oldest to newest */
		qEntry = this.entryQueueTail;
		while (qEntry != null) {
			newCache.privateAdd (qEntry.key, qEntry.value, qEntry.space);
			qEntry = qEntry.previous;
		}
		return newCache;
	}
	public double fillingRatio() {
		return (this.currentSpace) * 100.0 / this.spaceLimit;
	}
	/**
	 * Flushes all entries from the cache.
	 */
	public void flush() {

		this.currentSpace = 0;
		LRUCacheEntry entry = this.entryQueueTail; // Remember last entry
		this.entryTable = new Hashtable();  // Clear it out
		this.entryQueue = this.entryQueueTail = null;
		while (entry != null) {  // send deletion notifications in LRU order
			entry = entry.previous;
		}
	}
	/**
	 * Flushes the given entry from the cache.  Does nothing if entry does not
	 * exist in cache.
	 *
	 * @param key Key of object to flush
	 */
	public void flush (Object key) {

		LRUCacheEntry entry;

		entry = (LRUCacheEntry) this.entryTable.get(key);

		/* If entry does not exist, return */
		if (entry == null) return;

		privateRemoveEntry (entry, false);
	}
	/*
	 * Answers the existing key that is equals to the given key.
	 * If the key is not in the cache, returns the given key
	 */
	public Object getKey(Object key) {
		LRUCacheEntry entry = (LRUCacheEntry) this.entryTable.get(key);
		if (entry == null) {
			return key;
		}
		return entry.key;
	}
	/**
	 * Answers the value in the cache at the given key.
	 * If the value is not in the cache, returns null
	 *
	 * @param key Hash table key of object to retrieve
	 * @return Retreived object, or null if object does not exist
	 */
	public Object get(Object key) {

		LRUCacheEntry entry = (LRUCacheEntry) this.entryTable.get(key);
		if (entry == null) {
			return null;
		}

		updateTimestamp (entry);
		return entry.value;
	}
	/**
	 * Returns the amount of space that is current used in the cache.
	 */
	public int getCurrentSpace() {
		return this.currentSpace;
	}
	/**
	 * Returns the timestamps of the most recently used element in the cache.
	 */
	public int getNewestTimestamps() {
		return this.entryQueue == null ? 0 : this.entryQueue.timestamp;
	}
	/**
	 * Returns the timestamps of the least recently used element in the cache.
	 */
	public int getOldestTimestamps() {
		return this.entryQueueTail == null ? 0 : this.entryQueueTail.timestamp;
	}
	/**
	 * Returns the lest recently used element in the cache
	 */
	public Object getOldestElement() {
		return this.entryQueueTail == null ? null : this.entryQueueTail.key;
	}
	
	/**
	 * Returns the maximum amount of space available in the cache.
	 */
	public int getSpaceLimit() {
		return this.spaceLimit;
	}
	/**
	 * Returns an Enumeration of the keys currently in the cache.
	 */
	public Enumeration keys() {

		return this.entryTable.keys();
	}
	/**
	 * Returns an enumeration that iterates over all the keys and values
	 * currently in the cache.
	 */
	public ICacheEnumeration keysAndValues() {
		return new ICacheEnumeration() {

			Enumeration values = LRUCache.this.entryTable.elements();
			LRUCacheEntry entry;

			public boolean hasMoreElements() {
				return this.values.hasMoreElements();
			}

			public Object nextElement() {
				this.entry = (LRUCacheEntry) this.values.nextElement();
				return this.entry.key;
			}

			public Object getValue() {
				if (this.entry == null) {
					throw new java.util.NoSuchElementException();
				}
				return this.entry.value;
			}
		};
	}
	/**
	 * Ensures there is the specified amount of free space in the receiver,
	 * by removing old entries if necessary.  Returns true if the requested space was
	 * made available, false otherwise.
	 *
	 * @param space Amount of space to free up
	 */
	protected boolean makeSpace (int space) {

		int limit;

		limit = getSpaceLimit();

		/* if space is already available */
		if (this.currentSpace + space <= limit) {
			return true;
		}

		/* if entry is too big for cache */
		if (space > limit) {
			return false;
		}

		/* Free up space by removing oldest entries */
		while (this.currentSpace + space > limit && this.entryQueueTail != null) {
			privateRemoveEntry (this.entryQueueTail, false);
		}
		return true;
	}
	/**
	 * Returns a new LRUCache instance
	 */
	protected LRUCache newInstance(int size) {
		return new LRUCache(size);
	}
	/**
	 * Answers the value in the cache at the given key.
	 * If the value is not in the cache, returns null
	 *
	 * This function does not modify timestamps.
	 */
	public Object peek(Object key) {

		LRUCacheEntry entry = (LRUCacheEntry) this.entryTable.get(key);
		if (entry == null) {
			return null;
		}
		return entry.value;
	}
	/**
	 * Adds an entry for the given key/value/space.
	 */
	protected void privateAdd (Object key, Object value, int space) {

		LRUCacheEntry entry;

		entry = new LRUCacheEntry(key, value, space);
		privateAddEntry (entry, false);
	}
	/**
	 * Adds the given entry from the receiver.
	 * @param shuffle Indicates whether we are just shuffling the queue
	 * (in which case, the entry table is not modified).
	 */
	protected void privateAddEntry (LRUCacheEntry entry, boolean shuffle) {

		if (!shuffle) {
			this.entryTable.put (entry.key, entry);
			this.currentSpace += entry.space;
		}

		entry.timestamp = this.timestampCounter++;
		entry.next = this.entryQueue;
		entry.previous = null;

		if (this.entryQueue == null) {
			/* this is the first and last entry */
			this.entryQueueTail = entry;
		} else {
			this.entryQueue.previous = entry;
		}

		this.entryQueue = entry;
	}
	/**
	 * Removes the entry from the entry queue.
	 * @param shuffle indicates whether we are just shuffling the queue
	 * (in which case, the entry table is not modified).
	 */
	protected void privateRemoveEntry (LRUCacheEntry entry, boolean shuffle) {

		LRUCacheEntry previous, next;

		previous = entry.previous;
		next = entry.next;

		if (!shuffle) {
			this.entryTable.remove(entry.key);
			this.currentSpace -= entry.space;
		}

		/* if this was the first entry */
		if (previous == null) {
			this.entryQueue = next;
		} else {
			previous.next = next;
		}

		/* if this was the last entry */
		if (next == null) {
			this.entryQueueTail = previous;
		} else {
			next.previous = previous;
		}
	}
	/**
	 * Sets the value in the cache at the given key. Returns the value.
	 *
	 * @param key Key of object to add.
	 * @param value Value of object to add.
	 * @return added value.
	 */
	public Object put(Object key, Object value) {

		int newSpace, oldSpace, newTotal;
		LRUCacheEntry entry;

		/* Check whether there's an entry in the cache */
		newSpace = spaceFor(value);
		entry = (LRUCacheEntry) this.entryTable.get (key);

		if (entry != null) {

			/**
			 * Replace the entry in the cache if it would not overflow
			 * the cache.  Otherwise flush the entry and re-add it so as
			 * to keep cache within budget
			 */
			oldSpace = entry.space;
			newTotal = getCurrentSpace() - oldSpace + newSpace;
			if (newTotal <= getSpaceLimit()) {
				updateTimestamp (entry);
				entry.value = value;
				entry.space = newSpace;
				this.currentSpace = newTotal;
				return value;
			} else {
				privateRemoveEntry (entry, false);
			}
		}
		if (makeSpace(newSpace)) {
			privateAdd (key, value, newSpace);
		}
		return value;
	}
	/**
	 * Removes and returns the value in the cache for the given key.
	 * If the key is not in the cache, returns null.
	 *
	 * @param key Key of object to remove from cache.
	 * @return Value removed from cache.
	 */
	public Object removeKey (Object key) {

		LRUCacheEntry entry = (LRUCacheEntry) this.entryTable.get(key);
		if (entry == null) {
			return null;
		}
		Object value = entry.value;
		privateRemoveEntry (entry, false);
		return value;
	}
	/**
	 * Sets the maximum amount of space that the cache can store
	 *
	 * @param limit Number of units of cache space
	 */
	public void setSpaceLimit(int limit) {
		if (limit < this.spaceLimit) {
			makeSpace(this.spaceLimit - limit);
		}
		this.spaceLimit = limit;
	}
	/**
	 * Returns the space taken by the given value.
	 */
	protected int spaceFor (Object value) {

		if (value instanceof ILRUCacheable) {
			return ((ILRUCacheable) value).getCacheFootprint();
		} else {
			return 1;
		}
	}
	/**
	 * Returns a String that represents the value of this object.  This method
	 * is for debugging purposes only.
	 */
	public String toString() {
		return
			toStringFillingRation("LRUCache") + //$NON-NLS-1$
			toStringContents();
	}

	/**
	 * Returns a String that represents the contents of this object.  This method
	 * is for debugging purposes only.
	 */
	protected String toStringContents() {
		StringBuffer result = new StringBuffer();
		int length = this.entryTable.size();
		Object[] unsortedKeys = new Object[length];
		String[] unsortedToStrings = new String[length];
		Enumeration e = keys();
		for (int i = 0; i < length; i++) {
			Object key = e.nextElement();
			unsortedKeys[i] = key;
			unsortedToStrings[i] =
				(key instanceof org.eclipse.jdt.internal.core.JavaElement) ?
					((org.eclipse.jdt.internal.core.JavaElement)key).getElementName() :
					key.toString();
		}
		ToStringSorter sorter = new ToStringSorter();
		sorter.sort(unsortedKeys, unsortedToStrings);
		for (int i = 0; i < length; i++) {
			String toString = sorter.sortedStrings[i];
			Object value = get(sorter.sortedObjects[i]);
			result.append(toString);
			result.append(" -> "); //$NON-NLS-1$
			result.append(value);
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}

	public String toStringFillingRation(String cacheName) {
		StringBuffer buffer = new StringBuffer(cacheName);
		buffer.append('[');
		buffer.append(getSpaceLimit());
		buffer.append("]: "); //$NON-NLS-1$
		buffer.append(NumberFormat.getInstance().format(fillingRatio()));
		buffer.append("% full"); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Updates the timestamp for the given entry, ensuring that the queue is
	 * kept in correct order.  The entry must exist
	 */
	protected void updateTimestamp (LRUCacheEntry entry) {

		entry.timestamp = this.timestampCounter++;
		if (this.entryQueue != entry) {
			privateRemoveEntry (entry, true);
			privateAddEntry (entry, true);
		}
		return;
	}
}

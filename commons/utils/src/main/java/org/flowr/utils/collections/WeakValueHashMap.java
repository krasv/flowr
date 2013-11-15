/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.flowr.utils.JavaUtils;

/**
 * An {@link Map} implementation that keeps the key and value objects as weak references. Please note that the implementation does not fully support the map backwards change, if you access the map via
 * the {@link #entrySet()} method.
 *
 * @author SvenKrause
 * @param <K>
 *        the maps key type
 * @param <V>
 *        the maps value type
 *
 */
public class WeakValueHashMap<K, V> implements Map<K, V> {

	private WeakHashMap<K, WeakReference<V>> weakMap = new WeakHashMap<K, WeakReference<V>>();

	public WeakValueHashMap() {}

	public WeakValueHashMap(Map<K, V> m) {
		putAll(m);
	}


	public V put(K key, V value) {
		WeakReference<V> oldRef = weakMap.put(key, new WeakReference<V>(value));
		return oldRef != null ? oldRef.get() : null;
	}

	public V get(Object key) {
		WeakReference<V> weakReference = weakMap.get(key);
		return weakReference != null ? weakReference.get() : null;
	}

	public int size() {
		return weakMap.size();
	}

	public boolean isEmpty() {
		return weakMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return weakMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		for (Map.Entry<K, WeakReference<V>> entry : weakMap.entrySet()) {
			WeakReference<V> ref = entry.getValue();
			if (ref != null && JavaUtils.equals(ref.get(), value)) { return true; }
		}
		return false;
	}

	public V remove(Object key) {
		WeakReference<V> oldRef = weakMap.remove(key);
		return oldRef != null ? oldRef.get() : null;
	}

	public void putAll(Map< ? extends K, ? extends V> m) {
		for (Map.Entry< ? extends K, ? extends V> entry : m.entrySet()) {
			weakMap.put(entry.getKey(), new WeakReference<V>(entry.getValue()));
		}
	}

	public void clear() {
		weakMap.clear();
	}

	public Set<K> keySet() {
		return weakMap.keySet();
	}

	public Collection<V> values() {
		Collection<WeakReference<V>> values = weakMap.values();
		List<V> results = new ArrayList<V>(values.size());
		for (WeakReference<V> ref : values) {
			results.add(ref.get());
		}
		return results;
	}

	/**
	 * Gets an <b>unmodifiable</b> set of map entries in contrast to the originally interface contract. The set is <b>not backed</b> by the map. <br><br>
	 * {@inheritDoc}
	 */
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<K> keySet = keySet();
		Set<Entry<K, V>> resultSet = new HashSet<Entry<K, V>>();
		for (K key : keySet) {
			resultSet.add(new WeakEntry(key));
		}
		return Collections.unmodifiableSet(resultSet);
	}

	private class WeakEntry implements Map.Entry<K, V> {

		private K key;

		WeakEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return get(key);
		}

		@Override
		public V setValue(V value) {
			return put(key, value);
		}

	}
}

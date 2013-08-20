package com.testwidget.dataprovider;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TraversableMap<K, V> extends AbstractMap<K, V> {

	private Map<K, V> mapData;
	private int index;
	private Collection<V> values;

	public TraversableMap() {
		mapData = new LinkedHashMap<K, V>();
		values = mapData.values();
	}

	public V current() {
		return get(index);
	}

	public V previous() {
		index--;
		if (index == -1) {
			index = values.size() - 1;
		}
		return get(index);
	}

	public V next() {
		index++;
		if (index == values.size()) {
			index = 0;
		}
		return get(index);
	}

	private V get(int i) {
		if (i < 0 || i > values.size()-1) {
			// TODO throw
		}
		Iterator<V> it = values.iterator();
		V val = null;
		int current = 0;
		while (current++ <= i) {
			val = it.next();
		}
		return val;
	}

	@Override
	public V put(K key, V value) {
		return mapData.put(key, value);
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return mapData.entrySet();
	}

	@Override
	public V remove(Object key) {
		if (containsKey(key)) {
			previous();
			return super.remove(key);
		} else {
			return null;
		}
	}
}
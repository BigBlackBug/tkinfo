package com.testwidget.dataprovider;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.testwidget.dataprovider.DataProvider.NoDataException;

public class TraversableMap<K, V> extends AbstractMap<K, V> {

	private Map<K, V> mapData;
	private int cursor = -1;
	private Set<Map.Entry<K, V>> entries;

	private Map.Entry<K, V> currentEntry;

	public TraversableMap() {
		mapData = new LinkedHashMap<K, V>();
		entries = mapData.entrySet();
	}

	public V current() throws NoDataException {
		return getCurrentEntry().getValue();
	}

	private Map.Entry<K, V> getCurrentEntry() throws NoDataException {
		if (currentEntry == null) {
			throw new NoDataException();
		}
		return currentEntry;
	}

	public V previous() throws NoDataException {
		moveCursor(-1);
		return current();
	}

	public V next() throws NoDataException {
		moveCursor(1);
		return current();
	}

	private void moveCursor(int shift) throws NoDataException {
		if (cursor == -1) {
			throw new NoDataException();
		}
		if (shift < 0) {
			for (int i = shift; i < 0; i++) {
				moveBack();
			}
		} else {
			for (int i = 0; i < shift; i++) {
				moveForward();
			}
		}
		currentEntry = get(cursor);
	}

	private void moveForward() {
		cursor++;
		if (cursor == entries.size()) {
			cursor = 0;
		}
	}

	private void moveBack() {
		cursor--;
		if (cursor == -1) {
			cursor = entries.size() - 1;
		}
	}

	private Map.Entry<K, V> get(int i) {
		Iterator<Map.Entry<K, V>> it = entries.iterator();
		Map.Entry<K, V> val = null;
		int current = 0;
		while (current++ <= i) {
			val = it.next();
		}
		return val;
	}

	@Override
	public V put(K key, V value) {
		V ret = mapData.put(key, value);
		if (mapData.size() == 1) {
			cursor = 0;
			currentEntry = get(cursor);
		}
		return ret;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return entries;
	}

	@Override
	public V remove(Object key) {
		throw new RuntimeException(
				"this operation is currently not supported. " +
				"use removeCurrent method instead");
	}

	public Map.Entry<K, V> removeCurrent() throws NoDataException {
		if (currentEntry == null) {
			throw new NoDataException();
		}
		Map.Entry<K, V> removedEntry = currentEntry;
		entries.remove(currentEntry);
		
		if (entries.isEmpty()) {
			cursor = -1;
			currentEntry = null;
			return removedEntry;
		}
		
		if (cursor == entries.size()) {
			cursor = 0;
		}
		currentEntry = get(cursor);
		return removedEntry;
	}
}
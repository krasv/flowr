/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/

package org.flowr.utils.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.flowr.utils.IFilter;
import org.flowr.utils.ITransformer;
import org.flowr.utils.StringUtils;

/**
 * Helper class for convenient collection operations.
 * 
 * @author krausesv
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> Comparator<T> identityComparator() {
        return new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return System.identityHashCode(o1) - System.identityHashCode(o2);
            }
        };
    }

    /**
     * Returns a set containing these objects from the given source set, which are accepted by the given filter.
     * 
     * @param <T>
     *            sets elements type
     * @param set
     *            source set to filter
     * @param filter
     *            decision maker
     * @return a subset of the accepted elements from the source set.
     */
    public static <T> Set<T> filter(Set<T> set, IFilter<? super T> filter) {
        if (filter == null) {
            return set;
        }
        Set<T> resultSet = new TreeSet<T>(identityComparator());
        for (T t : set) {
            if (filter.accept(t)) {
                resultSet.add(t);
            }
        }
        return resultSet;
    }

    /**
     * Returns a list containing all these objects from the given source list, which are accepted by the given filter.
     */
    public static <T> List<T> filter(List<T> list, IFilter<? super T> filter) {
        if (filter == null) {
            return list;
        }
        List<T> resultList = new ArrayList<T>();
        for (T t : list) {
            if (filter.accept(t)) {
                resultList.add(t);
            }
        }
        return resultList;
    }

    /**
     * Gets all those map keys, where the map value is accepted by the given filter. The method returns all map key, if
     * the filter is <code>null</code>.
     * 
     * @param <K>
     *            map key type
     * @param <V>
     *            map value type
     * @param map
     *            map to inspect. may not be null.
     * @param filter
     *            value filter to consider. might be null.
     * @return a set of type K containing all map keys, which corresponding map value is accepted.
     */
    public static <K, V> Set<K> filter(Map<K, V> map, IFilter<? super V> filter) {
        if (filter == null) {
            return map.keySet();
        }
        Set<K> resultSet = new LinkedHashSet<K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (filter.accept(entry.getValue())) {
                resultSet.add(entry.getKey());
            }
        }
        return resultSet;
    }

    /**
     * creates an compound filter applicable at Map entries.
     * 
     * @param <K>
     *            map key type
     * @param <V>
     *            map value type
     * @param keyFilter
     *            filter inspecting map keys. might be null. Null means accept all.
     * @param valueFilter
     *            filter inspecting map values. might be null. Null means accept all.
     * @param junction
     *            filter result combination. may not be null.
     * @return an filter based on key and value filter junction.
     */
    public static <K, V> IFilter<Map.Entry<K, V>> mapFilter(final IFilter<K> keyFilter, final IFilter<V> valueFilter,
            final IFilter.Junction junction) {
        if (junction == null)
            throw new NullPointerException("junction may not be null");
        return new IFilter<Entry<K, V>>() {

            @Override
            public boolean accept(Entry<K, V> entry) {
                K k = entry.getKey();
                V v = entry.getValue();
                switch (junction) {
                case And:
                    return (keyFilter == null || keyFilter.accept(k)) && (valueFilter == null || valueFilter.accept(v));
                case Or:
                    return (keyFilter == null || keyFilter.accept(k)) || (valueFilter == null || valueFilter.accept(v));
                case Xor:
                    return (keyFilter == null || keyFilter.accept(k)) == (valueFilter == null || valueFilter.accept(v));
                }
                return false;
            }
        };
    }

    /**
     * returns the first matching element of the given iterable.
     */
    public static <T> T first(Iterable<T> iterable, IFilter<? super T> filter) {
        for (T t : iterable) {
            if (filter == null || filter.accept(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * removes all these elements from the set those are accepted by the given filter.
     * 
     *@param <T>
     *            sets elements type
     * @param set
     *            the source set to modify
     * @param filter
     *            decison maker
     * @return a set containing all removed elements, those has been removed from the source set.
     */
    public static <T> Set<T> remove(Set<T> set, IFilter<? super T> filter) {
        Set<T> resultSet = new TreeSet<T>(identityComparator());
        if (filter != null) {
            for (Iterator<T> iterator = set.iterator(); iterator.hasNext();) {
                T t = (T) iterator.next();
                if (filter.accept(t)) {
                    iterator.remove();
                    resultSet.add(t);
                }
            }
        }
        return resultSet;
    }

    /**
     * transforms an source typed set of objects into another set of the target type using the given transformer.
     * 
     * @param <To>
     *            transformation source type
     * @param <From>
     *            transformation target type
     * @param source
     *            the source set
     * @param transformer
     *            the {@link ITransformer} to use
     * @param skipNull
     *            don't process null values into result set
     * @return a set of elements containing element having the target type
     */
    public static <To, From> Set<To> transform(Set<From> source, ITransformer<From, To> transformer, boolean skipNull) {
        Set<To> resultSet = new TreeSet<To>(identityComparator());
        for (From from : source) {
            final To to = transformer.transform(from);
            if (to != null || !skipNull) {
                resultSet.add(to);
            }
        }
        return resultSet;
    }

    /**
     * transforms an source typed list of objects into another list of the target type using the given transformer.
     * 
     * @param <To>
     *            transformation source type
     * @param <From>
     *            transformation target type
     * @param source
     *            the source set
     * @param transformer
     *            the {@link ITransformer} to use
     * @param skipNull
     *            don't process null values into result set
     * @return a set of elements containing element having the target type
     */
    public static <To, From> List<To> transform(List<From> source, ITransformer<From, To> transformer, boolean skipNull) {
        List<To> resultSet = new ArrayList<To>();
        for (From from : source) {
            final To to = transformer.transform(from);
            if (to != null || !skipNull) {
                resultSet.add(to);
            }
        }
        return resultSet;
    }

    /**
     * merges all elements from the given array of sets,which are matching the given filter.
     * 
     * @param <T>
     *            the elements type
     * @param filter
     *            the decision maker
     * @param sets
     *            array of sets containing elements of type T to be merged
     * @return set with elements of type T containing all those elements form the source sets, which are accepted by the
     *         filter.
     */
    public static <T> Set<T> merge(IFilter<? super T> filter, Set<T>... sets) {
        if (sets.length == 1 && sets[0] != null) {
            return filter(sets[0], filter);
        }
        Set<T> resultSet = new TreeSet<T>(identityComparator());
        for (Set<T> set : sets) {
            if (set != null) {
                resultSet.addAll(filter(set, filter));
            }
        }
        return resultSet;
    }

    /**
     * gets all map entries from the given map, where the tail part of the key is matching the given lead condition.
     * 
     * @param <V>
     * @param map
     * @param leadCondition
     * @param separator
     * @return
     */
    public static <V> List<Map.Entry<String, V>> getAll(Map<String, V> map, String leadCondition, String separator) {
        List<Entry<String, V>> resultList = new ArrayList<Entry<String, V>>();
        final Set<Entry<String, V>> entrySet = map.entrySet();
        for (Entry<String, V> entry : entrySet) {
            final String key = entry.getKey();
            if (key.startsWith(leadCondition)) {
                if (key.length() == leadCondition.length()) {
                    resultList.add(entry);
                } else if (StringUtils.subString(key, leadCondition).startsWith(separator)) {
                    resultList.add(entry);
                }
            }
        }
        return resultList;
    }

    @SuppressWarnings("rawtypes")
	public static <E extends Enum> List<E> sort(List<E> source) {
        Collections.sort(source, new Comparator<E>() {

            @Override
            public int compare(E o1, E o2) {
                return o1.ordinal() - o2.ordinal();
            }
        });
        return source;
    }

    public static interface GroupCriteria<K, V> {
        K[] getGroups(V value);
    }
    
    public static interface GroupedComparator<K, V> {
        int compare(K key, V v1, V v2);
    }

    public static <K, V> Map<K, List<V>> groupByKey(Iterable<V> iterable, GroupCriteria<K, V> criteria) {
        return groupByKey(iterable, criteria, (Comparator<V>)null, null);
    }

    public static <K, V> Map<K, List<V>> groupByKey(Iterable<V> iterable, GroupCriteria<K, V> criteria,
            Comparator<V> valueComparator, Comparator<K> keyComparator) {
        Map<K, List<V>> map = createMap(iterable, criteria, keyComparator);
        if (valueComparator != null) {
            for (List<V> list : map.values()) {
                Collections.sort(list, valueComparator);
            }
        }
        return map;
    }

    public static <K, V> Map<K, List<V>> groupByKey(Iterable<V> iterable, GroupCriteria<K, V> criteria,
            final GroupedComparator<K, V> valueComparator, Comparator<K> keyComparator) {
        Map<K, List<V>> map = createMap(iterable, criteria, keyComparator);
        if (valueComparator != null) {
            for (Map.Entry<K, List<V>> e : map.entrySet()) {
                final K key = e.getKey();
                List<V> list = e.getValue();
                Comparator<V> comparator = new Comparator<V>() {
                    
                    @Override
                    public int compare(V v1, V v2) {
                        return valueComparator.compare(key, v1, v2);
                    }
                };
                Collections.sort(list, comparator);
            }
        }
        return map;
    }

    private static <V, K> Map<K, List<V>> createMap(Iterable<V> c, GroupCriteria<K, V> criteria,
            Comparator<K> keyComparator) {
        Map<K, List<V>> map = keyComparator != null ? new TreeMap<K, List<V>>(keyComparator) : new HashMap<K, List<V>>();
        for (V v : c) {
            K[] groups = criteria.getGroups(v);
            if(groups != null) {
                for (K key : groups) {
                    List<V> list = map.get(key);
                    if (list == null) {
                        list = new ArrayList<V>();
                        map.put(key, list);
                    }
                    list.add(v);
                }
            }
        }
        return map;
    }
    
    
    public static interface IContainerHandler<T> {
        boolean isContainer(T t);
        List<T> getChildren(T t);
        T createFilteredContainer(T t, List<T> list);
    }
    
    public static <T> List<T> filter(List<T> source, IFilter<? super T> filter, IContainerHandler<T> handler) {
        if(filter == null) return source;
        if(handler == null) return filter(source, filter);
        List<T> resultList = new ArrayList<T>();
        for(T t: source) {
            if(handler.isContainer(t)) {
                List<T> children = handler.getChildren(t);
                List<T> filteredChildren = filter(children, filter, handler);
                if(!filteredChildren.isEmpty()) {
                    resultList.add(handler.createFilteredContainer(t, filteredChildren));
                }
            } else {
                if(filter.accept(t)) {
                    resultList.add(t);
                }
            }
        }
        return resultList;
    }

}

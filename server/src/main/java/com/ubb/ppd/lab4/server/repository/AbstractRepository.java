package com.ubb.ppd.lab4.server.repository;

import com.ubb.ppd.lab4.server.model.Identifiable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marius Adam
 */
public class AbstractRepository<Id, T extends Identifiable<Id>> {
    private final Map<Id, T> items;

    AbstractRepository(MapFactory<Id, T> mapFactory) {
        items = mapFactory.createNewMapInstance();
    }

    public void save(T item) {
        beforeSave(item);
        items.put(item.getId(), item);
    }

    protected void beforeSave(T item) {

    }

    public Collection<T> findAll() {
        return items.values();
    }

    protected Stream<T> streamAll() {
        return items.values().stream();
    }

    public Collection<T> findBy(Predicate<T> condition) {
        return streamAll()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public T findOne(Predicate<T> condition) {
        Collection<T> matches = findBy(condition);
        if (matches.isEmpty()) {
            return null;
        }

        return matches.iterator().next();
    }

    public long count() {
        return items.size();
    }

    protected interface MapFactory<Id, T> {
        Map<Id, T> createNewMapInstance();
    }
}

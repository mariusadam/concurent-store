package com.ubb.ppd.lab4.server.util;

import com.ubb.ppd.lab4.server.model.Identifiable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Marius Adam
 */
public class LockManager {
    private final Map<String, Map<Object, Lock>> instances;

    public LockManager() {
        instances = new ConcurrentHashMap<>();
    }

    public <Id> Lock getLock(Identifiable<Id> identifiable) {
        Map<Object, Lock> lockMap = getLockMapInstance(identifiable.getClass());
        if (lockMap.get(identifiable.getId()) == null) {
            lockMap.put(identifiable.getId(), new ReentrantLock());
        }

        return lockMap.get(identifiable.getId());
    }

    private Map<Object, Lock> getLockMapInstance(Class<?> tClass) {
        if (instances.get(tClass.getName()) == null) {
            instances.put(tClass.getName(), new ConcurrentHashMap<>());
        }

        return instances.get(tClass.getName());
    }
}

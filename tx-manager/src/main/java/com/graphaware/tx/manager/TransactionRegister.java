package com.graphaware.tx.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionRegister {

    private static final ConcurrentHashMap<UUID, Transaction> register = new ConcurrentHashMap<>();

    public Collection<Transaction> transactions() {
        return Collections.unmodifiableCollection(register.values());
    }

    public void put(Transaction tx) {
        register.put(tx.getUuid(), tx);
    }

    public Transaction get(UUID key) {
        return register.get(key);
    }

    public void remove(UUID key) {
        register.remove(key);
    }

    public int size() {
        return register.size();
    }

    public void clear() {
        register.clear();
    }
}

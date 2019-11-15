package com.gysoft.jdbc.multi;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 周宁
 */
public class RoundbinLoadBalance extends AbstractLoadBalance {

    private static final RoundbinLoadBalance INSTANCE = new RoundbinLoadBalance();

    private Map<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<>();

    private RoundbinLoadBalance() {
    }

    @Override
    protected String doSelect(List<String> keys, String group) {
        AtomicPositiveInteger sequence = sequences.get(group);
        if (sequence == null) {
            sequences.putIfAbsent(group, new AtomicPositiveInteger());
            sequence = sequences.get(group);
        }
        int currentSequence = sequence.getAndIncrement();
        return keys.get(currentSequence % keys.size());
    }

    public static RoundbinLoadBalance getInstance() {
        return INSTANCE;
    }
}

package com.gysoft.jdbc.multi.balance;


import com.gysoft.jdbc.multi.AtomicPositiveInteger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 周宁
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private Map<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<>();

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
}

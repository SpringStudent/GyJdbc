package com.gysoft.jdbc.multi;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 周宁
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private static final RandomLoadBalance INSTANCE = new RandomLoadBalance();

    private RandomLoadBalance() {
    }

    @Override
    protected String doSelect(List<String> keys, String group) {
        int length = keys.size();
        return keys.get(ThreadLocalRandom.current().nextInt(length));
    }

    public static RandomLoadBalance getInstance() {
        return INSTANCE;
    }
}

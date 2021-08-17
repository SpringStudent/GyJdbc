package com.gysoft.jdbc.multi.balance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 周宁
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> keys, String group) {
        int length = keys.size();
        return keys.get(ThreadLocalRandom.current().nextInt(length));
    }
}

package com.gysoft.jdbc.multi.balance;

import java.util.List;

/**
 * @author zhouning
 */
public class SelectLastLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> keys, String group) {
        return keys.get(keys.size() - 1);
    }
}

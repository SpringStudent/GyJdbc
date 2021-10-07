package com.gysoft.jdbc.multi.balance;

import java.util.List;

/**
 * @author zhouning
 */
public class SelectFirstLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doSelect(List<String> keys, String group) {
        return keys.get(0);
    }
}

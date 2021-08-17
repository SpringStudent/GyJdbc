package com.gysoft.jdbc.multi.balance;

import java.util.List;

/**
 * @author zhouning
 * @date 2021/08/17 9:53
 */
public class SelectFirstLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doSelect(List<String> keys, String group) {
        return keys.get(0);
    }
}

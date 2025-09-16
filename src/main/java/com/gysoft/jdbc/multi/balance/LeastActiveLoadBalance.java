package com.gysoft.jdbc.multi.balance;

import com.gysoft.jdbc.multi.DataSourceBindHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 周宁
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> keys, String group) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        if (keys.size() == 1) {
            return keys.get(0);
        }
        // 查找活跃连接数最少的数据源
        List<String> leastActiveKeys = findLeastActiveKeys(keys);
        // 如果只有一个最少活跃的数据源，直接返回
        if (leastActiveKeys.size() == 1) {
            return leastActiveKeys.get(0);
        }
        // 如果有多个活跃连接数相同的数据源，使用权重随机选择
        return selectRandomFromCandidates(leastActiveKeys);
    }

    private List<String> findLeastActiveKeys(List<String> keys) {
        List<String> leastActiveKeys = new ArrayList<>();
        int minActiveCount = Integer.MAX_VALUE;
        for (String key : keys) {
            Integer activeCount = DataSourceBindHolder.getActiveCount(key);
            if (activeCount == null) {
                activeCount = 0;
            }
            if (activeCount < minActiveCount) {
                minActiveCount = activeCount;
                leastActiveKeys.clear();
                leastActiveKeys.add(key);
            } else if (activeCount == minActiveCount) {
                leastActiveKeys.add(key);
            }
        }
        return leastActiveKeys;
    }

    /**
     * 从候选数据源中随机选择一个
     */
    private String selectRandomFromCandidates(List<String> candidates) {
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

}

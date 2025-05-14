package com.nowcoder.community.service;

import java.util.Date;

/**
 * @author yl
 * @date 2025-04-28 21:01
 */
public interface DataService {
    long calculateUV(Date start, Date end);

    void recordUV(String ip);

    void recordDAU(int id);

    long calculateDAU(Date start, Date end);
}

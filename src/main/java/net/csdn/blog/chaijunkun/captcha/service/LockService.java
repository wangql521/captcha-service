package net.csdn.blog.chaijunkun.captcha.service;

import java.util.Date;

/**
 * Created by wangqingli1 on 2016/12/8.
 */
public interface LockService {
    /***
     * 上锁
     * @param lock
     * @param expire
     * @return
     */
    public Boolean lock(final String lock, final int expire);

    /**
     * 解锁
     * @param lock
     * @return
     */
    public Boolean unDieLock(final String lock);

    /***
     * 购买服务
     * @param key
     * @param limitCount
     * @param buyCount
     * @param endDate
     * @param lock
     * @param expire
     * @return
     */
    public boolean checkSoldCountByRedisDate(String key, int limitCount, int buyCount, Date endDate, String lock, int expire);
}

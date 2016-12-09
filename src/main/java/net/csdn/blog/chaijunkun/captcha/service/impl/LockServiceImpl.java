package net.csdn.blog.chaijunkun.captcha.service.impl;

import com.spring.tools.DateUtil;
import net.csdn.blog.chaijunkun.captcha.service.LockService;
import net.csdn.blog.chaijunkun.captcha.util.RedisUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangqingli1 on 2016/12/8.
 */
@Service
public class LockServiceImpl implements LockService {
    private static Logger logger= Logger.getLogger(LockServiceImpl.class);
    @Autowired
    private RedisTemplate redisT;
    private volatile int totalCount=100;
    public RedisUtil getRedisUtil() {
        return redisUtil;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Autowired
    private RedisUtil redisUtil;
    public RedisTemplate getRedisT() {
        return redisT;
    }

    public void setRedisT(RedisTemplate redisT) {
        this.redisT = redisT;
    }

    /**
     *
     * 雷------2016年6月17日
     *
     * @Title: lock
     * @Description: 加锁机制
     * @param @param lock 锁的名称
     * @param @param expire 锁占有的时长（毫秒）
     * @param @return 设定文件
     * @return Boolean 返回类型
     * @throws
     */
    @Override
    @SuppressWarnings("unchecked")
    public Boolean lock(final String lock, final int expire) {
        return (Boolean) redisT.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection
                                             connection) throws DataAccessException {
                boolean locked = false;
                byte[] lockValue = redisT.getValueSerializer().serialize(
                        DateUtil.getDateAddMillSecond(null, expire));
                byte[] lockName = redisT.getStringSerializer().serialize(lock);
                locked = connection.setNX(lockName, lockValue);
                if (locked)
                    connection.expire(lockName, TimeoutUtils.toSeconds(expire, TimeUnit.MILLISECONDS));
                return locked;
            }
        });
    }

    /**
     *
     * 雷------2016年6月17日
     *
     * @Title: unDieLock
     * @Description: 处理发生的死锁
     * @param @param lock 是锁的名称
     * @param @return 设定文件
     * @return Boolean 返回类型
     * @throws
     */
    @Override
    @SuppressWarnings("unchecked")
    public Boolean unDieLock(final String lock) {
        boolean unLock = false;
        Date lockValue = (Date) redisT.opsForValue().get(lock);
        if (lockValue != null && lockValue.getTime() <= (new Date().getTime())) {
            redisT.delete(lock);
            unLock = true;
        }
        return unLock;
    }
    /**
     *
     * 雷------2016年6月17日
     *
     * @Title: checkSoldCountByRedisDate
     * @Description: 抢购的计数处理（用于处理超卖）
     * @param @param key 购买计数的key
     * @param @param limitCount 总的限购数量
     * @param @param buyCount 当前购买数量
     * @param @param endDate 抢购结束时间
     * @param @param lock 锁的名称与unDieLock方法的lock相同
     * @param @param expire 锁占有的时长（毫秒）
     * @param @return 设定文件
     * @return boolean 返回类型
     * @throws
     */
    public boolean checkSoldCountByRedisDate(String key, int limitCount, int buyCount, Date endDate, String lock, int expire) {
        boolean check = false;
        if(endDate.before(new Date())){
            System.out.println( "活动已经截止----");

        }
        if (this.lock(lock, expire)) {
            Integer soldCount = (Integer) redisUtil.get(key);
            Integer totalSoldCount = (soldCount == null ? 0 : soldCount) + buyCount;
            if(totalCount<buyCount||(totalCount-totalSoldCount)<buyCount){
                throw new RuntimeException("库存不足还剩---"+(totalCount-totalSoldCount)+"请稍后重试");
            }else{
                this.unDieLock(lock);
            }
            if (totalSoldCount <= totalCount) {
                redisUtil.set(key, totalSoldCount, DateUtil.diffDateTime(endDate, new Date()));
                System.out.println( "已经卖出多少量！===" + totalSoldCount+"还剩与数量=="+(totalCount-totalSoldCount));
                check = true;
            }else{
                System.out.println( "活动太火爆啦,已经卖完了亲-------");


            }
           //redisUtil.remove(key);
        } else {
            if (this.unDieLock(lock)) {
                logger.info("解决了出现的死锁");
            } else {
                System.out.println( "活动太火爆啦,请稍后重试");

            }
        }
        return check;
    }

}

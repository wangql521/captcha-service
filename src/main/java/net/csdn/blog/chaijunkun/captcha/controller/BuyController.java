package net.csdn.blog.chaijunkun.captcha.controller;

import net.csdn.blog.chaijunkun.captcha.service.LockService;
import net.csdn.blog.chaijunkun.captcha.util.RedisUtil;
import net.csdn.blog.chaijunkun.captcha.util.ResponseUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangqingli1 on 2016/12/9.
 */
@Controller
@RequestMapping(value="/buy")
public class BuyController {
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private LockService lockService;
    @Autowired
    private RedisTemplate redisT;
    private static  final String  lock="order_id_001";
    private static  final String  keyCount="order_id_001_limit";
    /**
     * 购买服务
     * @param request
     * @param response
     * @param key
     * @param buyCount
     * @return
     */
    @RequestMapping(value="/buy.do")
    public String buy(HttpServletRequest request,
                      HttpServletResponse response,
                      String key,int buyCount){
        System.out.println(redisT.opsForSet().members("userList"));
        System.out.println(redisT.opsForSet().isMember("userList", key));
        if (redisT.opsForSet().isMember("userList", key)){
            ResponseUtil.sendMessageNoCache(response, "你已经成功预约了！---");
            return null;
        }
       if(!this.canAccess(1,10000,key)){
           ResponseUtil.sendMessageNoCache(response,"我只能接受你的第一次请求!!!");
           return  null;
       }
        String sEnd="2016-12-09 20:00:00";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean flag= false;
        try {
            flag = lockService.checkSoldCountByRedisDate(keyCount,0,buyCount,dateFormat.parse(sEnd),lock,10000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(flag){
            ResponseUtil.sendMessageNoCache(response, key + "再次购买成功==" + buyCount);
            redisT.opsForSet().add("userList",key);
        }else{
            ResponseUtil.sendMessageNoCache(response,key+"再次购买失败=="+buyCount);

        }


        return null;
    }
    /**
     * 是否可以继续访问某一资源（时间second内只能访问visitTotal次 资源）
     *
     * @param visitTotal 访问次数限制
     * @param second 时间周期
     * @param key 某一资源访问限制的key
     * @return
     * @author xll
     */
    private boolean canAccess(int visitTotal, int second, String key) {

        int total =(Integer)redisT.opsForValue().get (key);

        if (total <= 0)
            redisUtil.remove(key);// 有可能是之前访问留下的痕迹

        if (total < visitTotal) {// 小于限制值,继续计数
            redisT.opsForValue().increment(key, 1);// 增加key的值
            System.out.println("访问次数" + (Integer) redisT.opsForValue().get(key));
            if (total <= 0)
                redisT.expire(key, second, TimeUnit.SECONDS);// 限定时间内的第一次访问设置过期时间
            return true;
        } else {
            System.out.println("超出最大访问次数" + (Integer)redisT.opsForValue().get (key));
            return false;
        }
    }

}

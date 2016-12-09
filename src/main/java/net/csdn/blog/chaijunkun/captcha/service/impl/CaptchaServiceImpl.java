package net.csdn.blog.chaijunkun.captcha.service.impl;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.spring.tools.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import com.google.code.kaptcha.Producer;

import net.csdn.blog.chaijunkun.captcha.params.CaptchaVerifyParam;
import net.csdn.blog.chaijunkun.captcha.service.CaptchaService;
import net.csdn.blog.chaijunkun.captcha.util.XXTEAUtil;

import javax.annotation.Resource;

@Service
public class CaptchaServiceImpl implements CaptchaService {

	private static Logger logger= Logger.getLogger(CaptchaServiceImpl.class);

	private static final String[] codeBase= {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

	private static Random rand= new Random();
	/** XXTEA加密解密的密钥 */
	private String secKey = "captcha";
	
	/** 验证码超时门限(秒) */
	private long expire = 30;
	
	/** 缓存前缀 */
	private String cachePrefix = "captcha";
	
	/** 验证码字符数 */
	private int charCount = 4;
	
	/** 验证码生成器 */
	@Autowired
	private Producer producer;

	/** 缓存操作模板 */
	@Autowired
	private StringRedisTemplate redisTemplate;

	public CaptchaServiceImpl(){
		logger.info("正在构建验证码生成与验证服务");
	}
	
	@Override
	public String getSecKey() {
		return secKey;
	}

	@Override
	public void setSecKey(String secKey) {
		this.secKey = secKey;
	}

	@Override
	public long getExpire() {
		return expire;
	}

	@Override
	public void setExpire(long expire) {
		this.expire = expire;
	}

	@Override
	public String getCachePrefix() {
		return cachePrefix;
	}

	@Override
	public void setCachePrefix(String cachePrefix) {
		this.cachePrefix = cachePrefix;
	}
	
	@Override
	public int getCharCount() {
		return charCount;
	}

	@Override
	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}
	
	/**
	 * 获取验证码生成器
	 * @return 验证码生成器
	 */
	public Producer getProducer() {
		return producer;
	}

	/**
	 * 设置验证码生成器
	 * @param producer 验证码生成器
	 */
	public void setProducer(Producer producer) {
		this.producer = producer;
	}

	/**
	 * 获取缓存操作模板
	 * @return 缓存操作模板
	 */
	public StringRedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	/**
	 * 设置缓存操作模板
	 * @param redisTemplate 缓存操作模板
	 */
	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 生成token黑名单缓存key
	 * @param token
	 * @return
	 */
	private String genTokenBlacklistCacheKey(String token){
		return String.format("%s_token_black_%s", cachePrefix, token);
	}
	
	/**
	 * 生成token已验证key
	 * @param token
	 * @return
	 */
	private String genVerifiedTokenCacheKey(String token){
		return String.format("%s_token_verified_%s", cachePrefix, token);
	}

	@Override
	public String getCaptcha(String token) throws Exception {
		try{
			if (redisTemplate.hasKey(genTokenBlacklistCacheKey(token))){
				throw new IllegalStateException("此token已列入黑名单");
			}
			String plainText= XXTEAUtil.decrypt(token, secKey);
			if (StringUtils.isBlank(plainText)){
				throw new IllegalStateException("解密失败,token可能遭到篡改");
			}
			String[] plainTextArr= plainText.split("_");
			if (plainTextArr.length!=2){
				throw new IllegalStateException("token数据格式错误");
			}
			long timestamp= 0;
			try{
				timestamp= Long.parseLong(plainTextArr[1]);
			}catch(NumberFormatException e){
				throw new IllegalStateException("时间戳无效");
			}
			if ((System.currentTimeMillis() - timestamp)>TimeUnit.MILLISECONDS.convert(expire+5, TimeUnit.SECONDS)){
				throw new IllegalStateException("验证码已过期");
			}
			return plainTextArr[0];
		}catch(Exception e){
			redisTemplate.opsForValue().set(genTokenBlacklistCacheKey(token), Boolean.TRUE.toString(), expire, TimeUnit.SECONDS);
			throw new Exception(e.getMessage());
		}
	}


	@Override
	public String genToken() {
		StringBuffer sb= new StringBuffer();
		for(int i=0; i<charCount; i++){
			int randInt= Math.abs(rand.nextInt());
			sb.append(codeBase[randInt % codeBase.length]);
		}
		long timestamp= System.currentTimeMillis();
		String token= null;
		token= String.format("%s_%d", sb.toString(), timestamp);
		logger.info("未加密的token:"+token);
		token= XXTEAUtil.encrypt(token, secKey);
		return token;
	}

	@Override
	public boolean doVerify(CaptchaVerifyParam param) throws Exception {
		if (StringUtils.isBlank(param.getCaptcha())){
			throw new IllegalArgumentException("未输入验证码");
		}
		if (StringUtils.isBlank(param.getToken())){
			throw new IllegalArgumentException("token缺失");
		}
		//判断缓存中有没有此token
		//如果有说明已经被验证过了

		if (redisTemplate.hasKey(this.genVerifiedTokenCacheKey(param.getToken()))){
			throw new Exception("该验证码已经被验证过");
		}else{
			//如果没有 将其放入到已验证缓存库中 
			redisTemplate.opsForValue().set(this.genVerifiedTokenCacheKey(param.getToken()), Boolean.TRUE.toString(), expire, TimeUnit.SECONDS);
		}
		String verifyCode= getCaptcha(param.getToken());
		verifyCode= verifyCode.toLowerCase();
		String code= param.getCaptcha().toLowerCase();
		if (code.equals(verifyCode)){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public BufferedImage getCaptchaImg(String captcha) {
		return producer.createImage(captcha);
	}

}

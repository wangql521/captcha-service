package net.csdn.blog.chaijunkun.captcha.service;

import java.awt.image.BufferedImage;

import net.csdn.blog.chaijunkun.captcha.params.CaptchaVerifyParam;

public interface CaptchaService {
	
	/**
	 * 获取XXTEA加密解密的密钥
	 * @return XXTEA加密解密的密钥
	 */
	public String getSecKey();

	/**
	 * 设置XXTEA加密解密的密钥
	 * @param secKey XXTEA加密解密的密钥
	 */
	public void setSecKey(String secKey);

	/**
	 * 获取验证码超时门限(秒)
	 * @return 验证码超时门限(秒)
	 */
	public long getExpire();

	/**
	 * 设置验证码超时门限(秒)
	 * @param expire 验证码超时门限(秒)
	 */
	public void setExpire(long expire);

	/**
	 * 获取缓存前缀
	 * @return 缓存前缀
	 */
	public String getCachePrefix();

	/**
	 * 设置缓存前缀
	 * @param cachePrefix 缓存前缀
	 */
	public void setCachePrefix(String cachePrefix);
	
	/**
	 * 获取验证码字符数
	 * @return 验证码字符数
	 */
	public int getCharCount();

	/**
	 * 设置验证码字符数
	 * @param charCount 验证码字符数
	 */
	public void setCharCount(int charCount);
	
	/**
	 * 生成验证码令牌
	 * @return
	 */
	public String genToken();
	
	/**
	 * 校验token
	 * @param token 被校验的token
	 * @return token中的验证码字符
	 * @throws Exception
	 */
	public String getCaptcha(String token) throws Exception;

	/**
	 * 验证输入的验证码与令牌是否匹配
	 * @param param 验证码参数
	 * @return 匹配结果
	 * @throws Exception 匹配时产生的异常
	 */
	public boolean doVerify(CaptchaVerifyParam param) throws Exception;
	
	/**
	 * 根据指定的captcha生成相应的验证码图片
	 * @param captcha
	 * @return
	 */
	public BufferedImage getCaptchaImg(String captcha);
	
}

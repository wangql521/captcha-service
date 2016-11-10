package net.csdn.blog.chaijunkun.captcha.controller;

import java.awt.image.BufferedImage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.csdn.blog.chaijunkun.captcha.params.CaptchaVerifyParam;
import net.csdn.blog.chaijunkun.captcha.params.CaptchaVerifyResp;
import net.csdn.blog.chaijunkun.captcha.service.CaptchaService;
import net.csdn.blog.chaijunkun.captcha.util.ResponseUtil;


@Controller
@RequestMapping(value="/captcha")
public class CaptchaController {

	private static Logger logger= Logger.getLogger(CaptchaController.class);

	@Resource
	private CaptchaService captchaService;

	public CaptchaController(){
		logger.info("正在构造验证码控制器...");
	}

	/**
	 * 验证码请求接口
	 * @param request
	 * @param response
	 * @param token
	 * @return
	 */
	@RequestMapping(value="/captcha.do")
	public String requestCaptcha(HttpServletRequest request,
			HttpServletResponse response,
			String token){
		if (StringUtils.isBlank(token)){
			token= captchaService.genToken();
			ResponseUtil.sendJSON(response, token);
		}else{
			try{
				String captcha= captchaService.getCaptcha(token);
				//根据明文captcha生成图片
				BufferedImage img= captchaService.getCaptchaImg(captcha);
				//发送图片
				ResponseUtil.sendImg(response, img, "image/jpeg", "code", "jpg");
			}catch(Exception e){
				logger.error("生成验证码图片出错", e);
			}
		}
		return null;
	}
	
	/**
	 * 校验验证码接口
	 * @param request
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/verify.do")
	@ResponseBody
	public CaptchaVerifyResp verify(HttpServletRequest request, CaptchaVerifyParam param){
		CaptchaVerifyResp resp = new CaptchaVerifyResp();
		try{
			boolean captchaPass = captchaService.doVerify(param);
			if (!captchaPass){
				throw new IllegalStateException("验证码错误");
			}
			resp.setCode(CaptchaVerifyResp.CODE_OK);
			resp.setMsg("验证成功");
		}catch(Exception e){
			logger.error("验证失败", e);
			resp.setCode(CaptchaVerifyResp.CODE_ERR);
			resp.setMsg(e.getMessage());
		}
		return resp;
	}

}

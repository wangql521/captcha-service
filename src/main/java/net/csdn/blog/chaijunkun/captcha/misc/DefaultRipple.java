package net.csdn.blog.chaijunkun.captcha.misc;

import java.awt.image.BufferedImage;

import com.google.code.kaptcha.GimpyEngine;
import com.google.code.kaptcha.util.Configurable;

public class DefaultRipple extends Configurable implements GimpyEngine {

	@Override
	public BufferedImage getDistortedImage(BufferedImage baseImage) {
		// TODO Auto-generated method stub
		return baseImage;
	}

}

package net.csdn.blog.chaijunkun.captcha.util;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class JSONUtil {
	
	private static Logger logger= Logger.getLogger(JSONUtil.class);
	
	private static volatile ObjectMapper objectMapper;
	
	/**
	 * 懒惰单例模式得到ObjectMapper实例
	 * 此对象为Jackson的核心
	 */
	private static ObjectMapper getMapper(){
		if (objectMapper== null){
			synchronized (JSONUtil.class) {
				objectMapper= new ObjectMapper();
				objectMapper = new ObjectMapper();
				//当找不到对应的序列化器时 忽略此字段
				objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				//支持双引号
				objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);  
				//禁止一个Map中value为null时,对应key参与序列化
				objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
				//未知字段在反序列化时忽略
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				//设置null值不参与序列化(字段不被显示)
				objectMapper.setSerializationInclusion(Include.NON_NULL);
			}
		}
		return objectMapper;
	}
	
	/**
	 * 将生成的JSON字符串流式输出到sw,
	 * 输出完毕后将其关闭, 使用toString()方法得到最终字符串
	 */
	public static StringWriter sw= null;
	
	/**
	 * 创建JSON处理器的静态方法
	 * @param content JSON字符串
	 * @return
	 */
	private static JsonParser getParser(String content){
		try{
			return getMapper().getFactory().createParser(content);
		}catch (IOException ioe){
			return null;
		}
	}
	
	/**
	 * 创建JSON生成器的静态方法, 使用标准输出
	 * @return
	 */
	private static JsonGenerator getGenerator(StringWriter sw){
		try{
			return getMapper().getFactory().createGenerator(sw);
		}catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * JSON对象序列化
	 */
	public static String toJSON(Object obj){
		StringWriter sw= new StringWriter();
		JsonGenerator jsonGen= getGenerator(sw);
		if (jsonGen== null){
			try {
				sw.close();
			} catch (IOException e) {
			}
			return null;
		}		
		try {
			//由于在getGenerator方法中指定了OutputStream为sw
			//因此调用writeObject会将数据输出到sw
			jsonGen.writeObject(obj);
			//由于采用流式输出 在输出完毕后务必清空缓冲区并关闭输出流
			jsonGen.flush();
			jsonGen.close();
			return sw.toString();
		} catch (JsonGenerationException jge) {
			logger.error("JSON生成错误" + jge.getMessage());
		} catch (IOException ioe) {
			logger.error("JSON输入输出错误" + ioe.getMessage());
		}
		return null;		
	}
	
	/**
	 * 生成JSONP
	 * @param callBack 回调函数名
	 * @param obj 要序列化的数据对象
	 * @return JSONP
	 */
	public static String toJSONP(String callBack, Object obj){
		String json= toJSON(obj);
		String retVal= String.format("%s(%s)", callBack, json);
		return retVal;
	}
	
	/**
	 * JSON对象反序列化
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJSON(String json, Class<T> clazz) {
		try {
			JsonParser jp= getParser(json);
			return jp.readValueAs(clazz);
		} catch (JsonParseException jpe){
			logger.error(String.format("反序列化失败, 错误原因:%s", jpe.getMessage()));
		} catch (JsonMappingException jme){
			logger.error(String.format("反序列化失败, 错误原因:%s", jme.getMessage()));	
		} catch (IOException ioe){
			logger.error(String.format("反序列化失败, 错误原因:%s", ioe.getMessage()));
		}
		return null;
	}
	
	/**
	 * 从JSON反序列化为 对象 针对容器对象定制
	 * 例:
	 * JSONUtil.fromJSONtoList(jsonStr, new TypeReference<List<SysSlideAD>>(){});
	 * @param json
	 * @param t
	 * @return
	 */
	public static <T> T fromJSON(String json, TypeReference<T> typeReference){
		try {
			return getMapper().readValue(json, typeReference);
		} catch (JsonParseException jpe){
			logger.error(String.format("反序列化失败, 错误原因:%s", jpe.getMessage()));
		} catch (JsonMappingException jme){
			logger.error(String.format("反序列化失败, 错误原因:%s", jme.getMessage()));	
		} catch (IOException ioe){
			logger.error(String.format("反序列化失败, 错误原因:%s", ioe.getMessage()));
		}
		return null;
	}
}

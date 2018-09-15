package com.cch.common.utils.codec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cch.common.utils.exception.ExceptionUtil;
import com.cch.common.utils.list.ListUtil;
import com.cch.common.utils.string.StringUtil;

public class EncodeUtil {
	private static final Logger logger = LoggerFactory.getLogger(EncodeUtil.class);
	private static final String DEFAULT_URL_ENCODING = "UTF-8";
	private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * Hex编码.
	 */
	public static String encodeHex(byte[] input) {
		return new String(Hex.encodeHex(input));
	}

	/**
	 * Hex解码.
	 */
	public static byte[] decodeHex(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * Base64编码.
	 */
	public static String encodeBase64(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}
	
	/**
	 * Base64编码.
	 */
	public static String encodeBase64(String input) {
		try {
			return new String(Base64.encodeBase64(input.getBytes(DEFAULT_URL_ENCODING)));
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	/**
	 * Base64解码.
	 */
	public static byte[] decodeBase64(String input) {
		return Base64.decodeBase64(input.getBytes());
	}
	
	/**
	 * Base64解码.
	 */
	public static String decodeBase64String(String input) {
		try {
			return new String(Base64.decodeBase64(input.getBytes()), DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	/**
	 * Base62编码。
	 */
	public static String encodeBase62(byte[] input) {
		char[] chars = new char[input.length];
		for (int i = 0; i < input.length; i++) {
			chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
		}
		return new String(chars);
	}

	/**
	 * URL 编码, Encode默认为UTF-8. 
	 */
	public static String encodeUrl(String part) {
		return encodeUrl(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * URL 编码, Encode默认为UTF-8. 
	 */
	public static String encodeUrl(String part, String encoding) {
		if (part == null){
			return null;
		}
		try {
			return URLEncoder.encode(part, encoding);
		} catch (UnsupportedEncodingException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * URL 解码, Encode默认为UTF-8. 
	 */
	public static String decodeUrl(String part) {
		return decodeUrl(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * URL 解码, Encode默认为UTF-8. 
	 */
	public static String decodeUrl(String part, String encoding) {
		if (part == null){
			return null;
		}
		try {
			return URLDecoder.decode(part, encoding);
		} catch (UnsupportedEncodingException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	/**
	 * URL 解码（两次）, Encode默认为UTF-8. 
	 */
	public static String decodeUrl2(String part) {
		return decodeUrl(decodeUrl(part));
	}

	// 预编译XSS过滤正则表达式
	private static List<Pattern> xssPatterns = ListUtil.newArrayList(
			Pattern.compile("(<\\s*(script|link|style|iframe)([\\s\\S]*?)(>|<\\/\\s*\\1\\s*>))|(</\\s*(script|link|style|iframe)\\s*>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\s*(href|src)\\s*=\\s*(\"\\s*(javascript|vbscript):[^\"]+\"|'\\s*(javascript|vbscript):[^']+'|(javascript|vbscript):[^\\s]+)\\s*(?=>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\s*on[a-z]+\\s*=\\s*(\"[^\"]+\"|'[^']+'|[^\\s]+)\\s*(?=>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(eval\\((.|\\n)*\\)|xpression\\((.|\\n)*\\))", Pattern.CASE_INSENSITIVE)
		);
	
	/**
	 * XSS 非法字符过滤，内容以<!--HTML-->开头的用以下规则（保留标签）
	 * @author ThinkGem
	 */
	public static String xssFilter(String text) {
		String oriValue = StringUtil.trim(text);
		if (text != null){
			String value = oriValue;
			for (Pattern pattern : xssPatterns) {
				Matcher matcher = pattern.matcher(value);
				if (matcher.find()) {
					value = matcher.replaceAll(StringUtil.EMPTY);
				}
			}
			// 如果开始不是HTML，XML，JOSN格式，则再进行HTML的 "、<、> 转码。
			if (!StringUtil.startsWithIgnoreCase(value, "<!--HTML-->") 	// HTML
					&& !StringUtil.startsWithIgnoreCase(value, "<?xml ") 	// XML
					&& !StringUtil.contains(value, "id=\"FormHtml\"") 		// JFlow
					&& !(StringUtil.startsWith(value, "{") && StringUtil.endsWith(value, "}")) // JSON Object
					&& !(StringUtil.startsWith(value, "[") && StringUtil.endsWith(value, "]")) // JSON Array
				){
				value = value.replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			}
			if (logger.isInfoEnabled() && !value.equals(oriValue)){
				logger.info("xssFilter: {}   <=<=<=   {}", value, text);
			}
			return value;
		}
		return null;
	}
	
	// 预编译SQL过滤正则表达式
	private static Pattern sqlPattern = Pattern.compile("(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)", Pattern.CASE_INSENSITIVE);
			
	/**
	 * SQL过滤，防止注入，传入参数输入有select相关代码，替换空。
	 * @author ThinkGem
	 */
	public static String sqlFilter(String text){
		if (text != null){
			String value = text;
			Matcher matcher = sqlPattern.matcher(text);
			if (matcher.find()) {
				value = matcher.replaceAll(StringUtil.EMPTY);
			}
			if (logger.isWarnEnabled() && !value.equals(text)){
				logger.info("sqlFilter: {}   <=<=<=   {}", value, text);
				return StringUtil.EMPTY;
			}
			return value;
		}
		return null;
	}
	
//	public static void main(String[] args) {
//		xssFilter("你好，<script>alert(document.cookie)</script>我还在。");
//		xssFilter("你好，<strong>加粗文字</strong>我还在。");
//		xssFilter("<!--HTML-->你好，\"><strong>加粗文字</strong>我还在。");
//		xssFilter("<!--HTML-->你好，<iframe src=\"abcdef\"></iframe><strong>加粗文字</strong>我还在。");
//		xssFilter("<!--HTML-->你好，<iframe src=\"abcdef\"/><strong>加粗文字</strong>我还在。");
//		xssFilter("<!--HTML-->你好，<iframe src=\"abcdef\"><strong>加粗文字</strong>我还在。");
//		xssFilter("<!--HTML-->你好，<script type=\"text/javascript\">alert(document.cookie)</script>我还在。");
//		xssFilter("<!--HTML-->你好，<script\n type=\"text/javascript\">\nalert(document.cookie)\n</script>我还在。");
//		xssFilter("<!--HTML-->你好，<script src='' onerror='alert(document.cookie)'></script>我还在。");
//		xssFilter("<!--HTML-->你好，<script type=text/javascript>alert()我还在。");
//		xssFilter("<!--HTML-->你好，<script>alert(document.cookie)</script>我还在。");
//		xssFilter("<!--HTML-->你好，<script>window.location='url'我还在。");
//		xssFilter("<!--HTML-->你好，</script></iframe>我还在。");
//		xssFilter("<!--HTML-->你好，eval(abc)我还在。");
//		xssFilter("<!--HTML-->你好，xpression(abc)我还在。");
//		xssFilter("<!--HTML-->你好，<img src='abc.jpg' onerror='location='';alert(document.cookie);'></img>我还在。");
//		xssFilter("<!--HTML-->你好，<img src='abc.jpg' onerror='alert(document.cookie);'/>我还在。");
//		xssFilter("<!--HTML-->你好，<img src='abc.jpg' onerror='alert(document.cookie);'>我还在。");
//		xssFilter("<!--HTML-->你好，<a onload='alert(\"abc\")'>hello</a>我还在。");
//		xssFilter("<!--HTML-->你好，<a href=\"/abc\">hello</a>我还在。");
//		xssFilter("<!--HTML-->你好，<a href='/abc'>hello</a>我还在。");
//		xssFilter("<!--HTML-->你好，<a href='vbscript:alert(\"abc\");'>hello</a>我还在。");
//		xssFilter("<!--HTML-->你好，<a href='javascript:alert(\"abc\");'>hello</a>我还在。");
//		xssFilter("<!--HTML-->你好，?abc=def&hello=123&world={\"a\":1}我还在。");
//		sqlFilter("你好，select * from xxx where abc=def and 1=1我还在。");
//		sqlFilter("你好，insert into xxx values(1,2,3,4,5)我还在。");
//		sqlFilter("你好，delete from xxx我还在。");
//	}
}

package com.cch.common.utils.mail;

import java.net.URI;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class MailUtil {

	/**
	 * 发送邮件
	 * 服务器
	 * @param mail
	 * @return
	 */
	public static boolean sendEmail() {
		Boolean flag = false;
		try {
			ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP1); // 新建server版本
			ExchangeCredentials credentials = new WebCredentials("1236856486@qq.com", "xerkfwsmxyoqiidh"); // 用户名，密码
			service.setCredentials(credentials);
			service.setUrl(new URI("https://ex.qq.com/EWS/Exchange.asmx")); // outlook.spacex.com 改为自己的邮箱服务器地址
			EmailMessage msg = new EmailMessage(service);
			msg.setSubject("This is a test!"); // 主题
			msg.setBody(MessageBody.getMessageBodyFromText("this is a test! pls ignore it!")); // 内容
			msg.getToRecipients().add("1126373925@qq.com"); // 收件人
			//msg.getCcRecipients().add("test2@test.com"); //抄送人
			//msg.getAttachments().addFileAttachment("E:\\123.txt"); //附件
			msg.send(); // 发送
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public static void main(String[] args) {
		System.out.println("start");
		sendEmail();
		System.out.println("end");
	}
}

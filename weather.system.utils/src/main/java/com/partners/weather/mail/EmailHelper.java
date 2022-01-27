package com.partners.weather.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.Emalilandsmssettings;
import com.partners.entity.Notification;
import com.partners.weather.common.NotificationType;
import com.partners.weather.encrypt.HexUtil;

public class EmailHelper {
	private static final Logger logger = LoggerFactory.getLogger(EmailHelper.class);
	public static void SendEmail(final Emalilandsmssettings setting,Notification notification,NotificationType notificationType) {
		Transport transport=null;	
        try {
        	if(setting==null || StringUtils.isBlank(setting.getSmtpserver()) || StringUtils.isBlank(setting.getSmtpuserName())
        	  || StringUtils.isBlank(setting.getSmtpuserPwd()) || StringUtils.isBlank(setting.getReceiveEmail())){
        		return;
        	}
        	Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", setting.getSmtpserver());
            props.setProperty("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(setting.getSmtpuserName(),HexUtil.HexToString(setting.getSmtpuserPwd()));
            }});
            session.setDebug(false); 
            transport = session.getTransport();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(setting.getSmtpuserName()));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(setting.getReceiveEmail()));
            message.setSubject(notificationType.getDescription(), "UTF-8");
            message.setContent(notification.getMessage(), "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();       
            transport.connect(setting.getSmtpuserName(),HexUtil.HexToString(setting.getSmtpuserPwd()));
            transport.sendMessage(message, message.getAllRecipients());
		} catch(Exception e){
			logger.error("Error in {}",e);
		}finally {
			if( transport!=null){
				 try {
					transport.close();
				} catch (MessagingException e) {
					logger.error("Error in {}",e);
				}
			}
		}
	}

}

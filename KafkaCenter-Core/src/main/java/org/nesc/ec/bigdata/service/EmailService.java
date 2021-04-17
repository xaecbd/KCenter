package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.model.EmailEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private TemplateEngine templateEngine;
    @Autowired
	InitConfig initConfig;

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	/**
	 * 根据邮件类型不同，渲染不同模板
	 * 
	 * @param emailEntity
	 * @param mailContent
	 * @param mailType
	 * @throws Exception
	 */
	public void renderTemplateAndSend(Object emailEntity, Object mailContent, int mailType) throws Exception {
		try {
			if (initConfig.getMailEnable()) {
				EmailEntity mailEntity = (EmailEntity) emailEntity;
				@SuppressWarnings("unchecked")
				Map<String, Object> mailContentMap = (Map<String, Object>) mailContent;
				Context context = new Context();
				context.setVariables(mailContentMap);
				String emailContent;
				if (mailType == 1) {
					emailContent = templateEngine.process("mail/toAdminApproveMail", context);
				} else if (mailType == 3) {
					emailContent = templateEngine.process("mail/toUserApproveMail", context);
				} else if (mailType == 2) {
					emailContent = templateEngine.process("mail/toUserRejectMail", context);
				} else {
					emailContent = templateEngine.process("mail/toAlertMail", context);
				}

				sendTemplateMail(mailEntity, emailContent);
			}
		} catch (Exception e) {
			logger.error("send email error.",e);
		}
	}

	/**
	 * 实际发邮件方法
	 * 
	 * @param mailEntity
	 * @param emailContent
	 * @throws Exception
	 */
	@Async
	void sendTemplateMail(EmailEntity mailEntity, String emailContent) throws Exception {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
		try {
			messageHelper.setFrom(mailEntity.getEmailFrom());
			String[] emailToArr;
			String[] emailCCArr;
			if (mailEntity.getEmailTo().contains(";")) {
				emailToArr = mailEntity.getEmailTo().split(";");
				messageHelper.setTo(emailToArr);
			} else {
				messageHelper.setTo(mailEntity.getEmailTo());
			}
			if (null != mailEntity.getEmailCC()) {
				if (mailEntity.getEmailCC().contains(";")) {
					emailCCArr = mailEntity.getEmailCC().split(";");
					messageHelper.setCc(emailCCArr);
				} else {
					messageHelper.setCc(mailEntity.getEmailCC());
				}
			}
			messageHelper.setSubject(mailEntity.getEmailSubject());
			messageHelper.setText(emailContent, true);
			javaMailSender.send(mimeMessage);
		} catch (Exception e) {
			logger.warn("Email sending failed, please check your mailbox configuration!",e);
		}
	}
}
package org.nesc.ec.bigdata.common.util;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ay05
 * @create 3/27/2019
 */
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    private String hostname;
    private Integer port;
    private String from;
    private String fromname;

    private EmailUtil(Builder builder) {
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.from = builder.from;
        this.fromname = builder.fromname;
    }

    // 默认String String
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder{
        private String hostname;
        private Integer port;
        private String from;
        private String fromname;

        public Builder hostname(String hostname){
            this.hostname = hostname;
            return this;
        }
        public Builder port(Integer port){
            this.port = port;
            return this;
        }
        public Builder from(String from){
            this.from = from;
            return this;
        }
        public Builder fromname(String fromname){
            this.fromname = fromname;
            return this;
        }
        public EmailUtil build(){
            return new EmailUtil(this);
        }
    }

    public void sendAndCC(String mailAddress, String cc, String title, String content) {
        try {
            Email email = new HtmlEmail();
            email.setHostName(hostname);
            email.setSmtpPort(port);
            email.setFrom(from, fromname);
            email.setSubject(title);
            email.setMsg(content);
            email.addTo(mailAddress.split(";"));
        	String[] ccArr = cc.split(";");
        	for(String ccAddress : ccArr) {
        		email.addCc(ccAddress);
        	}
            email.setCharset("utf-8");
            email.send();
            logger.info("email send done .");
        } catch (Exception e) {
            logger.error("Send Mail Error", e);
        }
    }
    
    public void send(String mailAddress,String title, String content) {
        try {
            Email email = new HtmlEmail();
            email.setHostName(hostname);
            email.setSmtpPort(port);
            email.setFrom(from, fromname);
            email.setSubject(title);
            email.setMsg(content);
            email.addTo(mailAddress.split(";"));
            email.setCharset("utf-8");
            email.send();
            logger.info("email send done .");
        } catch (Exception e) {
            logger.error("Send Mail Error", e);
        }
    }
}

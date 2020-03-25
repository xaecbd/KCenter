package org.nesc.ec.bigdata.email;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.nesc.ec.bigdata.Application;
import org.nesc.ec.bigdata.service.EmailService;

/**
 * @author Reason.H.Duan
 * @version 1.0
 * @date 3/29/2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={Application.class})
public class EmailTest {

    @Autowired
    EmailService emailService;

    /**
     * Test send email.
     * Email服务已升级，需要传入特定的参数<br>
     * 	2020/03/04
     */
    @Test
    public void testSendEmail() {
//        emailService.sendDefaultTitleMail("reason.h.duan@aaa.com", "KafkaCenter test send email.");
    }
}

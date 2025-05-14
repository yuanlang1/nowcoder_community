package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author yl
 * @date 2025-04-22 12:30
 */
@SpringBootTest
public class MailTest {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "袁浪");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println("content = " + content);
        mailClient.sendMail("y_lang111@163.com", "HTML", content);
    }
}

package com.ilya.ivanov.aspect;

import com.ilya.ivanov.aspect.help.ApplicationContextProvider;
import com.ilya.ivanov.security.session.SessionManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by ilya on 5/21/17.
 */
@Aspect
public class MailAspect {
    private static final Logger log = Logger.getLogger(MailAspect.class);
    private JavaMailSender javaMailSender;

    private SessionManager sessionManager;

    @Pointcut("within(com.ilya.ivanov..*)")
    public void withinApplication() {}

    @Pointcut("execution(* com.ilya.ivanov.security.session.SessionManager.newSession(..))")
    public void newSession() {}

    @Pointcut("execution(* com.ilya.ivanov.security.session.SessionManager.invalidateSession(..))")
    public void invalidateSession() {}

    @AfterReturning(pointcut = "newSession() && withinApplication()", returning = "result")
    public void prepareWorkspace(JoinPoint joinPoint, Object result) {
        System.out.println("New session");
    }

    @Before("invalidateSession() && withinApplication()")
    public void sendActivityReport() {
        System.out.println("Sending email!");
        final String email = sessionManager.getSession().getUserEntity().getEmail();
        final InternetAddress address;
        try {
            address = new InternetAddress(email);
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            mimeMessage.addRecipient(Message.RecipientType.TO, address);
            mimeMessage.setSubject("Archive - activity report");
            mimeMessage.setText("Hello, my friend!");
        } catch (AddressException e) {
            log.warn("User's email address is not exists", e);
        } catch (MessagingException e) {
            log.warn("Problems with mail sending", e);
        }
    }

    public void setApplicationContextProvider(ApplicationContextProvider applicationContextProvider) {
        ApplicationContext context = applicationContextProvider.getApplicationContext();
        this.javaMailSender = context.getBean(JavaMailSender.class);
        this.sessionManager = context.getBean(SessionManager.class);
    }
}

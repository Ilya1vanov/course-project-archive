package com.ilya.ivanov.aspect;

import com.ilya.ivanov.aspect.help.ApplicationContextProvider;
import com.ilya.ivanov.security.session.SessionManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ilya on 5/21/17.
 */
@Aspect
public class ActivityTrackingAspect {
    private static final Logger log = Logger.getLogger(ActivityTrackingAspect.class);
    private JavaMailSender javaMailSender;

    private SessionManager sessionManager;

    private ExecutorService executor = Executors.newWorkStealingPool();

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
        log.debug("Sending email!");
        final String email = sessionManager.getSession().getUserEntity().getEmail();
//        executor.submit(() -> javaMailSender.send(mimeMessage -> {
//            InternetAddress address = new InternetAddress(email);
//            mimeMessage.addRecipient(Message.RecipientType.TO, address);
//            mimeMessage.setSubject("Archive - activity report");
//            mimeMessage.setText("Hello, my friend!");
//            log.info("Report was sent");
//        }));
    }

    public void setApplicationContextProvider(ApplicationContextProvider applicationContextProvider) {
        ApplicationContext context = applicationContextProvider.getApplicationContext();
        this.javaMailSender = context.getBean(JavaMailSender.class);
        this.sessionManager = context.getBean(SessionManager.class);
    }
}

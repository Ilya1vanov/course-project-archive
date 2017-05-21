package com.ilya.ivanov.aspect;

import com.ilya.ivanov.aspect.help.ApplicationContextProvider;
import com.ilya.ivanov.controller.MainController;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.security.session.Session;
import javafx.scene.control.TreeItem;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Created by ilya on 5/21/17.
 */
@Aspect
public class SessionAspect {
    private MainController mainController;

    @Pointcut("within(com.ilya.ivanov..*)")
    public void withinApplication() {}

    @Pointcut("execution(* com.ilya.ivanov.security.session.SessionManager.newSession(..))")
    public void newSession() {}

    @Pointcut("execution(* com.ilya.ivanov.security.session.SessionManager.invalidateSession(..))")
    public void invalidateSession() {}

    @AfterReturning(pointcut = "newSession() && withinApplication()", returning = "result")
    public void prepareWorkspace(JoinPoint joinPoint, Object result) {
        if (result != null) {
            Session session = (Session) result;
            UserEntity userEntity = session.getUserEntity();
            this.initializeGUI(userEntity);
        }
    }

    private void initializeGUI(UserEntity userEntity) {
        this.initializeUserLetter(userEntity);
        mainController.getWorkingTreeTable().setRoot(new TreeItem<>(userEntity.getRoot()));
        mainController.getSearchField().setText("");
    }

    private void initializeUserLetter(UserEntity userEntity) {
        String email = userEntity.getEmail();
        String s = email.substring(0, 1).toUpperCase();
        mainController.getUserNameLetter().setText(s);
    }

    @Before("invalidateSession() && withinApplication()")
    public void sendActivityReport() {
        System.out.println("Sending email!");
    }

    public void setApplicationContextProvider(ApplicationContextProvider applicationContextProvider) {
        ApplicationContext context = applicationContextProvider.getApplicationContext();
        this.mainController = context.getBean(MainController.class);
    }
}

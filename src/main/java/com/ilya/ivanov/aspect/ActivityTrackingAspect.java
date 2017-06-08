package com.ilya.ivanov.aspect;

import com.google.common.collect.Lists;
import com.ilya.ivanov.aspect.help.ApplicationContextProvider;
import com.ilya.ivanov.data.model.file.FileDto;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.security.session.ActivityType;
import com.ilya.ivanov.security.session.Session;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.service.user.UserService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/21/17.
 */
@Aspect
public class ActivityTrackingAspect {
    private static final Logger log = Logger.getLogger(ActivityTrackingAspect.class);
    private JavaMailSender javaMailSender;

    private SessionManager sessionManager;

    private UserService userService;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Pointcut(value = "within(com.ilya.ivanov..*)")
    public void withinApplication() {}

    @Pointcut(value = "execution(* com.ilya.ivanov.security.session.SessionManager.newSession(..))")
    public void newSession() {}

    @Pointcut(value = "execution(* com.ilya.ivanov.security.session.SessionManager.invalidateSession(..))")
    public void invalidateSession() {}

    @AfterReturning(pointcut = "newSession() && withinApplication()", returning = "result")
    public void prepareWorkspace(JoinPoint joinPoint, Object result) {
        System.out.println("Args: " + Arrays.toString(joinPoint.getArgs()));
        System.out.println("New session " + result);
    }

    @Pointcut("execution(javafx.concurrent.Task<com.ilya.ivanov.data.model.file.FileEntity> com.ilya.ivanov.service.file.FileService.renameFile(..))")
    public void renameActivity() {}

    @Pointcut("execution(javafx.concurrent.Task<java.util.Collection<com.ilya.ivanov.data.model.file.FileEntity>> com.ilya.ivanov.service.file.FileService.addFiles(..))")
    public void addActivity() {}

    @Pointcut("execution(javafx.concurrent.Task<java.lang.Void> com.ilya.ivanov.service.file.FileService.removeFiles(..))")
    public void removeActivity() {}

    @AfterReturning(value = "renameActivity() && withinApplication() && args(before, filename)", returning = "task", argNames = "before,filename,task")
    public void trackRename(FileEntity before, String filename, Task<FileEntity> task) {
        this.addEventHandler(task, ActivityType.REMOVE, WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> Lists.newArrayList(before));
        this.addEventHandler(task, ActivityType.REMOVE, WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> Lists.newArrayList((FileEntity) e.getSource().getValue()));
    }

    @AfterReturning(value = "addActivity() && withinApplication()", returning = "task")
    public void trackAdd(Task<Collection<FileEntity>> task) {
        this.addEventHandler(task, ActivityType.ADD, WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> (Collection<FileEntity>) e.getSource().getValue());
    }

    @AfterReturning(value = "removeActivity() && withinApplication() && args(files)", returning = "task", argNames = "files,task")
    public void trackRemove(Collection<FileEntity> files, Task<Void> task) {
        this.addEventHandler(task, ActivityType.REMOVE, WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> files);
    }

    private <U, T extends Event> void addEventHandler(Task<U> task, ActivityType activityType, EventType<T> eventType, Function<T, Collection<FileEntity>> filesSupplier) {
        Platform.runLater(() -> task.addEventHandler(eventType, getSucceedEventHandler(activityType, filesSupplier)));
    }

    private <T extends Event> EventHandler<T> getSucceedEventHandler(ActivityType activityType, Function<T, Collection<FileEntity>> filesSupplier) {
        return (e) -> sessionManager.getSession().newActivity(activityType,
                filesSupplier.apply(e).stream().map(FileDto::new).collect(Collectors.toList()));
    }

    @AfterReturning("invalidateSession() && withinApplication()")
    public void sendActivityReport() {
        log.debug("Sending report!");
        final Session session = sessionManager.getSession();
        executor.submit(() -> javaMailSender.send(mimeMessage -> {
            final Collection<? extends Address> recipients = getRecipients(session.getUserEntity());
            mimeMessage.addRecipients(Message.RecipientType.TO, recipients.toArray(new Address[0]));
            mimeMessage.setSubject("Archive - activity report");
            mimeMessage.setContent(createAttachment(session.getActivity(), session));
        }));
    }

    private Collection<? extends Address> getRecipients(UserEntity userEntity) throws AddressException {
        final Set<UserEntity> admins = userService.getAdmins();
        admins.add(userEntity);
        final List<String> recipients = admins.stream().map(UserEntity::getEmail).collect(Collectors.toList());
        Collection<InternetAddress> addresses = new ArrayList<>();
        for (String recipient : recipients) {
            addresses.add(new InternetAddress(recipient));
        }
        return addresses;
    }

    private Multipart createAttachment(Map<ActivityType, Collection<FileDto>> activity, Session session) throws MessagingException, DocumentException {
        final Multipart multipart = new MimeMultipart();
        final byte[] activityAsPdf = getActivityAsPdf(activity, session);
        DataSource source = new ByteArrayDataSource(activityAsPdf, "application/pdf");
        final MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(source));
        multipart.addBodyPart(bodyPart);
        return multipart;
    }

    private byte[] getActivityAsPdf(Map<ActivityType, Collection<FileDto>> activity, Session session) throws DocumentException {
        final UserEntity user = session.getUserEntity();
        final ByteOutputStream byteOutputStream = new ByteOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteOutputStream);

        document.open();
        document.add(createTitle(user));
        document.add(createDurationReport(session));
        for (Paragraph paragraph : createActivityReport(activity)) {
            document.add(paragraph);
        }
        document.close();

        return byteOutputStream.getBytes();
    }

    private Paragraph createTitle(UserEntity user) {
        Font f = new Font();
        f.setStyle(Font.BOLD);
        f.setSize(24);
        Paragraph title = new Paragraph();
        title.setFont(f);
        title.add("Activity report for [" + user.getEmail() + "]");
        title.setAlignment(Element.ALIGN_CENTER);
        return title;
    }

    private Paragraph createDurationReport(Session session) {
        Paragraph duration = new Paragraph();
        duration.add("Session duration: " + session.getStart() + " - " + session.getEnd());
        return duration;
    }

    private Collection<Paragraph> createActivityReport(Map<ActivityType, Collection<FileDto>> activity) {
        Collection<Paragraph> paragraphs = new ArrayList<>();
        activity.forEach((t, act) -> {
            final Paragraph paragraph = new Paragraph();
            final Font font = new Font();
            font.setColor(t.getColor());
            font.setSize(12);
            paragraph.setFont(font);
            paragraph.add(t.getDescription() + "\n");
            act.forEach(a -> paragraph.add(t.getSign() + " " + a.toString() + "\n"));
            paragraphs.add(paragraph);
        });
        return paragraphs;
    }

    public void setApplicationContextProvider(ApplicationContextProvider applicationContextProvider) {
        ApplicationContext context = applicationContextProvider.getApplicationContext();
        this.javaMailSender = context.getBean(JavaMailSender.class);
        this.sessionManager = context.getBean(SessionManager.class);
        this.userService = context.getBean(UserService.class);
    }
}

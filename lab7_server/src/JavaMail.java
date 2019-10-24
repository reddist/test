import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class JavaMail {
    private static final String ENCODING = "UTF-8";

    static void registration(String email, String password, String login) {
        String subject = "Confirm registration";
        String content = "Ваш логин: " + login + "\nВаш пароль: " + password + "\n";
        String smtpHost = "smtp.gmail.com";
        String from = "erddist.supprog@gmail.com";
        String myLogin = "erddist.supprog@gmail.com";
        String myPassword = "DslhfGj101";
        String smtpPort = "587";
        try {
            sendSimpleMessage(myLogin, myPassword, from, email, content, subject, smtpPort, smtpHost);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private static void sendSimpleMessage(String login, String password, String from, String to, String content, String subject, String smtpPort, String smtpHost)
            throws MessagingException {
        Authenticator auth = new MyAuthenticator(login, password);

        Properties props = System.getProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.mime.charset", ENCODING);
        Session session = Session.getDefaultInstance(props,auth);
        ReentrantLock l = new ReentrantLock();
        l.lock();
        l.unlock();

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }
    static class MyAuthenticator extends Authenticator {
        private String user;
        private String password;

        MyAuthenticator(String user, String password) {

            this.user = user;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            String user = this.user;
            String password = this.password;
            return new PasswordAuthentication(user, password);
        }

    }
}

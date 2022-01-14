package org.binatel.bill.controllers.util;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    /**
     * Отправка e-mail 
     *
     * @param toEmail
     * @param subject
     * @param body
     */
    public static void sendEmail(String smtpHostServer, String smtpPort, String smtpSslPort, String emailNotifyFromAddress,
                                 String emailNotifyFromName, String smtpUserName, String smtpPassword,
                                 String toEmail, String subject, String body) {


        Properties props = System.getProperties();
        props.put(smtpHostServer, smtpHostServer);
        Session session;

        if (!smtpUserName.equals("") || !smtpPassword.equals("")) {
            props.put("mail.smtp.socketFactory.port", smtpSslPort); //SSL Port
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory"); //SSL фабрика
            props.put("mail.smtp.auth", "true"); //подключение SMTP Auth
            props.put("mail.smtp.port", smtpPort); //SMTP Port

            Authenticator auth = new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUserName, smtpPassword);
                }
            };
            session = Session.getDefaultInstance(props, auth);
        } else {
            session = Session.getInstance(props, null);
        }
        try {
            MimeMessage msg = new MimeMessage(session);
            //Заголовки сообщения
            msg.addHeader("Content-type", "text/html; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(emailNotifyFromAddress, emailNotifyFromName));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            System.out.println("Message is ready");
            Transport.send(msg);
            System.out.println("EMail Sent Successfully!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


//
// package com.example.majorproject.BackgroundTask;
//
// import java.util.Properties;
//
// public class JavaMailApi {
//     private static final String EMAIL = "your_email@gmail.com"; // Your email address
//     private static final String PASSWORD = "your_password"; // Your email password
//
//     public static void sendEmail(String recipient, String subject, String body) {
//         Properties props = new Properties();
//         props.put("mail.smtp.auth", "true");
//         props.put("mail.smtp.starttls.enable", "true");
//         props.put("mail.smtp.host", "smtp.gmail.com");
//         props.put("mail.smtp.port", "587");
//
//         Session session = Session.getInstance(props,
//                 new javax.mail.Authenticator() {
//                     protected PasswordAuthentication getPasswordAuthentication() {
//                         return new PasswordAuthentication(EMAIL, PASSWORD);
//                     }
//                 });
//
//         try {
//             Message message = new MimeMessage(session);
//             message.setFrom(new InternetAddress(EMAIL));
//             message.setRecipients(Message.RecipientType.TO,
//                     InternetAddress.parse(recipient));
//             message.setSubject(subject);
//             message.setText(body);
//
//             Transport.send(message);
//
//             System.out.println("Email sent successfully.");
//
//         } catch (MessagingException e) {
//             throw new RuntimeException(e);
//         }
//     }
//
//     public static void main(String[] args) {
//         sendEmail("recipient@example.com", "Test Subject", "This is a test email sent by EmailBot.");
//     }
//
// }

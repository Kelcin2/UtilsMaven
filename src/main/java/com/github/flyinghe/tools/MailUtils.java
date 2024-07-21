package com.github.flyinghe.tools;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * 与邮件相关的邮件工具类 。
 *
 * @author Flying
 * @see Email
 * @see AttachmentBean
 */
public class MailUtils {
    /**
     * 获取用户与邮件服务器的连接
     *
     * @param host     邮件主机名
     * @param username 发件人的用户名
     * @param password 发件人的用户名密码
     * @return 返回指定用户与指定邮件服务器绑定的一个连接(会话)
     */
    public static Session getSession(String host, final String username,
                                     final String password) {
        // 设置配置文件，邮件主机和是否认证
        Properties property = new Properties();
        property.put("mail.host", host);
        property.put("mail.smtp.auth", "true");
        // 设置用户名和密码
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        // 获取与邮件主机的连接
        Session session = Session.getInstance(property, auth);
        return session;
    }

    /**
     * 用来发送邮件
     *
     * @param session 与发送邮件的主机的连接(会话)
     * @param email   写好的邮件
     * @throws Exception 出现异常
     */
    public static void sendEmail(Session session, Email email) throws Exception {
        if (StringUtils.isBlank(email.getFrom())) {
            throw new Exception("没有邮件发送者");
        }

        if (StringUtils.isBlank(email.getTo()) && StringUtils.isBlank(email.getBcc()) &&
                StringUtils.isBlank(email.getCc())) {
            throw new Exception("没有邮件接收者");
        }

        // 获取发送邮件的信息类
        MimeMessage message = new MimeMessage(session);

        // 设置发送方邮件地址
        message.setFrom(new InternetAddress(email.getFrom()));
        // 设置发送类型和被发送方的邮件地址
        if (!email.getTo().isEmpty()) {
            message.setRecipients(RecipientType.TO, email.getTo());
        }
        if (!email.getCc().isEmpty()) {
            message.setRecipients(RecipientType.CC, email.getCc());
        }
        if (!email.getBcc().isEmpty()) {
            message.setRecipients(RecipientType.BCC, email.getBcc());
        }
        // 设置邮件主题
        message.setSubject(email.getSubject(), "utf-8");
        // 设置邮件内容
        MimeMultipart content = new MimeMultipart();
        // 邮件正文
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(email.getContent(), email.getType());
        content.addBodyPart(text);
        // 设置附件
        if (email.getAttachments() != null) {
            for (AttachmentBean attachment : email.getAttachments()) {
                MimeBodyPart part = new MimeBodyPart();
                part.attachFile(attachment.getFile());
                part.setFileName(MimeUtility.encodeText(attachment
                        .getFileName()));
                if (attachment.getCid() != null) {
                    part.setContentID(attachment.getCid());
                }
                content.addBodyPart(part);
            }
        }
        // 将邮件内容添加到信息中
        message.setContent(content);
        // 发送邮件
        Transport.send(message);
    }
}

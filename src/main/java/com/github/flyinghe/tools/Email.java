package com.github.flyinghe.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于发送邮件,表示邮件类，你需要设置：发件人，收件人、被抄送人(可选)、被密送人(可选)、主题（默认为空）、内容（默认为空），
 * 内容MIME类型（默认为"text/plain;charset=utf-8"）以及附件(可选)。
 *
 * @author Flying
 * @see AttachmentBean
 * @see MailUtils
 */
public class Email {
    private String from = null;// 发件人
    private StringBuffer to = new StringBuffer();// 收件人
    private StringBuffer cc = new StringBuffer();// 被抄送人
    private StringBuffer bcc = new StringBuffer();// 被密送人
    private String subject = "";
    private String content = "";// 文本正文
    private String type = "text/plain;charset=utf-8";// 文本正文MIME类型
    private List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();// 附件

    public Email(String from) {
        this.from = from;
    }

    public Email(String from, String to) {
        super();
        this.from = from;
        this.to.append(to);
    }

    public Email(String from, String to, String subject, String content) {
        super();
        this.from = from;
        this.to.append(to);
        this.subject = subject;
        this.content = content;
    }

    public Email(String from, String to, String subject, String content,
                 String type) {
        super();
        this.from = from;
        this.to.append(to);
        this.subject = subject;
        this.content = content;
        this.type = type;
    }

    /**
     * 返回发件人
     *
     * @return
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置发件人
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * 返回主题
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置主题
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 获取主题内容
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置主题内容
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取内容MIME类型
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * 设置内容MIME类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取收件人
     *
     * @return
     */
    public String getTo() {
        return this.to.toString();
    }

    /**
     * 添加收件人,可以是多个收件人,若字符串中包含多个收件人，则地址必须用","隔开
     *
     * @param to
     */
    public void addTo(String to) {
        if (this.to.length() > 0) {
            this.to.append(",");
        }
        this.to.append(to);
    }

    /**
     * 获取抄送
     *
     * @return
     */
    public String getCc() {
        return this.cc.toString();
    }

    /**
     * 添加抄送人，可以是多个抄送人，若字符串中包含多个被抄送人，则地址必须用","隔开
     *
     * @param cc
     */
    public void addCc(String cc) {
        if (this.cc.length() > 0) {
            this.cc.append(",");
        }
        this.cc.append(cc);
    }

    /**
     * 获取暗送
     *
     * @return
     */
    public String getBcc() {
        return this.bcc.toString();
    }

    /**
     * 添加被密送人，可以是多个被密送人，若字符串中包含多个被密送人，则地址必须用","隔开
     *
     * @param bcc
     */
    public void addBcc(String bcc) {
        if (this.bcc.length() > 0) {
            this.bcc.append(",");
        }
        this.bcc.append(bcc);
    }

    /**
     * 获取所有附件
     *
     * @return
     */
    public List<AttachmentBean> getAttachments() {
        return attachments;
    }

    /**
     * 添加附件，可以添加多个附件
     *
     * @param attachment
     */
    public void addAttachment(AttachmentBean attachment) {
        this.attachments.add(attachment);
    }

    @Override
    public String toString() {
        return "Email [from=" + from + ", to=" + to + ", cc=" + cc + ", bcc="
                + bcc + ", subject=" + subject + ", content=" + content
                + ", type=" + type + ", attachments=" + attachments + "]";
    }

}

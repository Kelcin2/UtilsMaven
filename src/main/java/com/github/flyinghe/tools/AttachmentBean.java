package com.github.flyinghe.tools;

import java.io.File;

/**
 * 附件类,用于发邮件的附件
 *
 * @author Flying
 * @see Email
 * @see MailUtils
 */
public class AttachmentBean {
    private String cid;
    private File file;
    private String fileName;

    /**
     * @param cid      指定CID（content ID）
     * @param file     指定文件
     * @param fileName 指定带有后缀名的文件名
     */
    public AttachmentBean(String cid, File file, String fileName) {
        super();
        this.cid = cid;
        this.file = file;
        this.fileName = fileName;
    }

    /**
     * @param file     指定文件
     * @param fileName 指定带有后缀名的文件名
     */
    public AttachmentBean(File file, String fileName) {
        this(null, file, fileName);
    }

    public AttachmentBean() {
        this(null, null);
    }

    /**
     * 获取CID
     *
     * @return CID（content ID）
     */
    public String getCid() {
        return cid;
    }

    /**
     * 设置CID（content ID）
     */
    public void setCid(String cid) {
        this.cid = cid;
    }

    /**
     * 获取附件
     *
     * @return 返回附件
     */
    public File getFile() {
        return file;
    }

    /**
     * 设置附件
     *
     * @param file 附件
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * 获取附件名
     *
     * @return 返回指定附件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置附件名
     *
     * @param fileName 指定附件名称
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}

package com.github.flyinghe.depdcy;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by FlyingHe on 2019/10/31.
 */
public class FlyingFilePart extends FilePart {
    private static final Log LOG = LogFactory.getLog(Part.class);
    private String fileNameCharset = "UTF-8";

    public String getFileNameCharset() {
        return fileNameCharset;
    }

    public void setFileNameCharset(String fileNameCharset) {
        this.fileNameCharset = fileNameCharset;
    }

    public FlyingFilePart(String name, PartSource partSource,
                          String contentType, String charset) {
        super(name, partSource, contentType, charset);
    }

    public FlyingFilePart(String name, PartSource partSource) {
        super(name, partSource);
    }

    public FlyingFilePart(String name, File file) throws FileNotFoundException {
        super(name, file);
    }

    public FlyingFilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
        super(name, file, contentType, charset);
    }

    public FlyingFilePart(String name, String fileName, File file) throws FileNotFoundException {
        super(name, fileName, file);
    }

    public FlyingFilePart(String name, String fileName, File file, String contentType, String charset)
            throws FileNotFoundException {
        super(name, fileName, file, contentType, charset);
    }

    @Override
    protected void sendDispositionHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendDispositionHeader(OutputStream out)");
        out.write(CONTENT_DISPOSITION_BYTES);
        out.write(QUOTE_BYTES);
        out.write(EncodingUtil.getAsciiBytes(getName()));
        out.write(QUOTE_BYTES);
        String filename = this.getSource().getFileName();
        if (filename != null) {
            out.write(EncodingUtil.getAsciiBytes(FILE_NAME));
            out.write(QUOTE_BYTES);
            out.write(EncodingUtil.getBytes(filename, this.getFileNameCharset()));
            out.write(QUOTE_BYTES);
        }
    }
}

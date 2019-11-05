package com.github.flyinghe.depdcy;

import org.apache.commons.httpclient.methods.multipart.PartSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by FlyingHe on 2019/11/4.
 */
public class InputStreamPartSource implements PartSource {
    private String fileName;
    private InputStream is;
    private Long fileLength;


    public InputStreamPartSource(String fileName, InputStream is) {
        this(fileName, null, is);
    }

    public InputStreamPartSource(String fileName, Long fileLength, InputStream is) {
        this.fileName = fileName;
        this.is = is;
        try {
            this.fileLength = fileLength != null ? fileLength : null != is ? this.is.available() : 0L;
        } catch (IOException e) {
            this.fileLength = 0L;
        }
    }

    @Override
    public long getLength() {
        return this.fileLength;
    }

    @Override
    public String getFileName() {
        return this.fileName != null ? this.fileName : "noname";
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return null != this.is ? this.is : new ByteArrayInputStream(new byte[]{});
    }
}

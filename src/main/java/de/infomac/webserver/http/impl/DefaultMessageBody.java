package de.infomac.webserver.http.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.Unimplemented;

public class DefaultMessageBody extends MessageBodyBase {
    private final static Category logger = Logger
            .getInstance(DefaultMessageBody.class);

    private String coding;

    private final InputStream is;

    private long length;
    
    

    public DefaultMessageBody(InputStream is, String content_coding, long length) {
        super();
        this.coding = content_coding;
        this.is = is;
        this.length = length;
        
        addDefaultListener();
    }



    public DefaultMessageBody(InputStream is, String coding) {
        this(is, coding, -1);
    }

    public String getContentCoding() {
        return coding;
    }

    public InputStream getInputStream() {
        return is;
    }

    public boolean hasInputStream() {
        return is != null;
    }

    public boolean isReadable() {
        return is != null;
    }

    public int read() throws IOException {
        return is.read();
    }

    public boolean lengthAvailable() {
        return length != -1;
    }

    public long length() {
        return length;
    }

    /**
     * gzip and deflate is supported
     */
    public MessageBody decode(HTTPVersion version,
            boolean content_length_required) throws Unimplemented,
            MessageBodyException, IOException {

        //if encoding is unknown (null), we assume identity
        if ((getContentCoding() == null)
                || getContentCoding().equals("identity"))
            return this;

        MessageBody ret = null;
        if (getContentCoding().equals("gzip") || getContentCoding().equals("x-gzip")) {
            ret = new DefaultMessageBody(new GZIPInputStream(getInputStream()),
                    null);
            logger.info("will decode Content-Encoding: gzip");
            //ret does not know its Message-Length!
        } else if (getContentCoding().equals("deflate") || getContentCoding().equals("x-deflate")) {

            ret = new DefaultMessageBody(new InflaterInputStream(
                    getInputStream()), null);
            logger.info("will decode Content-Encoding: deflate");
            //ret does not know its Message-Length!
        } else {
            throw new Unimplemented("cannot decode Content-Encoding: "
                    + getContentCoding());
        }

        //When sending to HTTP/1.0 clients it's ok not to provide a
        // Content-Length,
        //but in HTTP/1.1 Content-Lenght is required, when no
        // Transfer-Encoding is given, wich is the case.
        if ((version.isVersion1_1() && (ret != null) && !ret.lengthAvailable())
                || content_length_required) {
            //must determine the content-length.
            //I do not have another idea than just deflate it and buffer
            // the data.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            //unpack it into buffer
            ret.copyToOutputStream(buffer);

            //now return the data as ByteArrayMessageBody wich knows its lenght
            return new ByteArrayMessageBody(buffer.toByteArray(), null);

        }

        if (ret != null)
            return ret;

        return super.decode(version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ceruti.http.MessageBody#cleanup()
     */
    public void cleanup() {
        try {
            if (is != null)
                is.close();
        } catch (IOException e) {

        }
    }

}
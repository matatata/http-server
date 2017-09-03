/*
 * Created on 19.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.infomac.webserver.http.impl.ByteArrayMessageBody;


/**
 * @author matteo
 *
 * TODO comment
 */
public class DelegatingMessageBody implements MessageBody {
    private MessageBody delegatee;
    
    public DelegatingMessageBody(MessageBody delegatee){
        this.delegatee = delegatee;
    }
    
   
    /**
     * @param dest
     * @throws MessageBodyException
     */
    public void attachedTo(HTTPMessage dest) throws MessageBodyException {
        delegatee.attachedTo(dest);
    }
    /**
     * @param out
     * @throws IOException
     * @throws MessageBodyException
     */
    public void copyToOutputStream(OutputStream out) throws IOException,
            MessageBodyException {
        delegatee.copyToOutputStream(out);
    }
    /**
     * @param out
     * @param maximumAmount
     * @throws IOException
     * @throws MessageBodyException
     */
    public void copyToOutputStream(OutputStream out, int maximumAmount)
            throws IOException, MessageBodyException {
        delegatee.copyToOutputStream(out, maximumAmount);
    }
    /**
     * @param version
     * @return
     * @throws Unimplemented
     * @throws MessageBodyException
     * @throws IOException
     */
    public MessageBody decode(HTTPVersion version) throws Unimplemented,
            MessageBodyException, IOException {
        return delegatee.decode(version);
    }
    /**
     * @param version
     * @param content_lenght_required
     * @return
     * @throws Unimplemented
     * @throws MessageBodyException
     * @throws IOException
     */
    public MessageBody decode(HTTPVersion version,
            boolean content_lenght_required) throws Unimplemented,
            MessageBodyException, IOException {
        return delegatee.decode(version, content_lenght_required);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return delegatee.equals(arg0);
    }
    /**
     * @return
     */
    public String getContentCoding() {
        return delegatee.getContentCoding();
    }
    /**
     * @return
     */
    public String getContentType() {
        return delegatee.getContentType();
    }
    /**
     * @return
     */
    public InputStream getInputStream() {
        return delegatee.getInputStream();
    }
    /**
     * @return
     */
    public String getTransferEncoding() {
        return delegatee.getTransferEncoding();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegatee.hashCode();
    }
    /**
     * @return
     */
    public boolean hasInputStream() {
        return delegatee.hasInputStream();
    }
    /**
     * @return
     */
    public boolean isReadable() {
        return delegatee.isReadable();
    }
    /**
     * @return
     */
    public long length() {
        return delegatee.length();
    }
    /**
     * @return
     */
    public boolean lengthAvailable() {
        return delegatee.lengthAvailable();
    }
    /**
     * @return
     * @throws MessageBodyException
     * @throws IOException
     */
    public ByteArrayMessageBody preFetchData() throws MessageBodyException,
            IOException {
        return delegatee.preFetchData();
    }
    /**
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        return delegatee.read();
    }
    /**
     * @throws IOException
     */
    public void reset() throws IOException {
        delegatee.reset();
    }
    /**
     * @return
     */
    public boolean resetAvailable() {
        return delegatee.resetAvailable();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegatee.toString();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#addListener(org.ceruti.http.HTTPMessageListener)
     */
    public void addListener(HTTPMessageBodyListener listener) {
        delegatee.addListener(listener);
    }

    /*
     *  (non-Javadoc)
     * @see org.ceruti.http.MessageBody#removeListener(org.ceruti.http.HTTPMessageBodyListener)
     */
    public boolean removeListener(HTTPMessageBodyListener listener) {
        return delegatee.removeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getListeners()
     */
    public java.util.Iterator getListeners() {
        return delegatee.getListeners();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#fireAfterCopyToOutputStream()
     */
    public void fireAfterCopyToOutputStream() {
        delegatee.fireAfterCopyToOutputStream();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#fireAfterExceptionDuringTransfer(java.lang.Exception)
     */
    public void fireAfterExceptionDuringTransfer(Exception e) {
        delegatee.fireAfterExceptionDuringTransfer(e);
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getDefaultListener()
     */
    public HTTPMessageBodyListener getDefaultListener() {
        return delegatee.getDefaultListener();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#addDefaultListener()
     */
    public void addDefaultListener() {
        delegatee.addDefaultListener();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#newDefaultListener()
     */
    public HTTPMessageBodyListener newDefaultListener() {
        return delegatee.newDefaultListener();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#removeDefaultListener()
     */
    public void removeDefaultListener() {
       delegatee.removeDefaultListener();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#ETagAvailable()
     */
    public boolean ETagAvailable() {
        return delegatee.ETagAvailable();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getETag()
     */
    public String getETag() {
        return delegatee.getETag();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#cleanup()
     */
    public void cleanup() {
        delegatee.cleanup();
    }
}

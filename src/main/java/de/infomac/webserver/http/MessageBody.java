/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;





import java.io.IOException;
import java.io.InputStream;

import de.infomac.webserver.http.impl.ByteArrayMessageBody;
import de.infomac.webserver.http.io.Readable;



public interface MessageBody extends Readable{
    boolean hasInputStream();
    InputStream getInputStream();
    
    /**
     * clsoe streams and stuff
     *
     */
    void cleanup();

    String getContentCoding();
    
    boolean isReadable();
   
    boolean lengthAvailable();
    long length();
    
    /**
     * 
     * @return
     */
    boolean resetAvailable();
    /**
     * If you wish to resend the Body you should reset the Body.
     * Check it with resetAvailable() before you call it.
     * @throws IOException
     */
    void reset() throws IOException;
    
    
    /**
     * Transfers its content to out. After that calls afterCopyToOutputStream()
     * @param out
     * @throws IOException
     * @throws MessageBodyException
     */
    public void copyToOutputStream(java.io.OutputStream out) throws IOException, MessageBodyException;
    
    /**
     * Transfers its content to out. After that calls afterCopyToOutputStream()
     * @param out
     * @param maximum maximum bytes to be copied
     * @throws IOException
     * @throws MessageBodyException
     */
    public void copyToOutputStream(java.io.OutputStream out,int maximumAmount) throws IOException, MessageBodyException;
    
    
   
    //public abstract void setListener(HTTPMessageBodyListener listener);
    
    public void addListener(HTTPMessageBodyListener ll);
    
    /**
     * 
     * @param ll can be null, but then nothing happens
     * @return
     */
	public boolean removeListener(HTTPMessageBodyListener ll);
	
	public void fireAfterCopyToOutputStream();
	public void fireAfterExceptionDuringTransfer(Exception e);
	
	
	public java.util.Iterator getListeners();
	
	/**
	 * 
	 * @return null if not available
	 */
	public HTTPMessageBodyListener getDefaultListener();
	
	public void addDefaultListener();
	
	/**
	 * Create a new Instance of the Default-Listener
	 * @return null if not available
	 */
	public HTTPMessageBodyListener newDefaultListener();

	public void removeDefaultListener();

    /**
     * This must me called when a MessageBody gets attached to a Message.
     * the Message can e.g. set the approriate Headers if necessary.
     * @throws MessageBodyException
     */
    public void attachedTo(HTTPMessage dest) throws MessageBodyException;
    
    public boolean ETagAvailable();
    public String getETag();
    public String getContentType();
    
    public String getTransferEncoding();
    
    /**
     * decode body to "identity"-Encoding
     * @param version
     * @param content_lenght_required if the length of the decoded data is required
     * @return
     * @throws Unimplemented
     * @throws MessageBodyException
     */
	public MessageBody decode(HTTPVersion version,boolean content_lenght_required) throws Unimplemented,MessageBodyException,IOException;
	
	  /**
     * decode body to "identity"-Encoding. It depends on the implementation and MessageBody-Type, wether the result knowns its content-length.
     * @param version
     * @return
     * @throws Unimplemented
     * @throws MessageBodyException
     */
	public MessageBody decode(HTTPVersion version) throws Unimplemented,MessageBodyException,IOException;
	
	/**
	 * You should not buffer unlimited amount of data
	 * @return a ByteArrayMessageBody where all bytes are already buffered. The callback(s) should be implemented.
	 * @throws MessageBodyException
	 * @throws IOException
	 */
	public ByteArrayMessageBody preFetchData() throws MessageBodyException, IOException;

}

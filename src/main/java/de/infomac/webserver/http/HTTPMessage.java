/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


import java.io.IOException;
import java.io.OutputStream;



public interface HTTPMessage {
    
    
    public boolean hasContentLength();
    public long getContentLength();
    public boolean hasTransferEncoding();
    
    /**
     * close streams and stuff
     *
     */
    public void cleanup();
    
    /**
     * @return Returns the headers.
     */
    public HTTPHeaders getHeaders();
    
    public void setHeaders(HTTPHeaders h);
   
    
    public Object getHeader(String field_name);
    
    
    
    /**
     * sets a field. If already present it replaces the value.
     * @param field_name
     * @param val
     */
    public void setHeader(String field_name,Object val);
    
    /**
     * sets a field, but preserves previous values by using a ',' separated list
     * @param field_name
     * @param val
     */
    public void setHeaderNonDestructive(String field_name,Object val);
    
    /**
     * 
     * @param field_name
     * @return true if a non empty field-value associated with field_name exists.
     */
    public boolean hasNonEmptyHeader(String field_name);
    
    public boolean hasHeader(String field_name,Object object);
    
    public boolean hasHeaderIgnoreCase(String field_name,String object);
    
    public void copyToOutputStream(OutputStream out) throws IOException, MessageBodyException;

    
    // BODY
    public void setMessageBody(MessageBody m) throws MessageBodyException;
    public MessageBody getMessageBody();
    
    
    
    
	//public HTTPMessageListener getListener(int index);
	public void addListener(HTTPMessageListener ll);
	public boolean removeListener(HTTPMessageListener ll);
	
	public java.util.Iterator getListeners();
	
	public void fireAfterCopyToOutputStream();
	public void fireAfterExceptionDuringTransfer(Exception e);

	/**
	 * @return
	 */
	public HTTPVersion getVersion();
	
}

/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.IOException;
import java.io.OutputStream;

import de.infomac.webserver.http.HTTPMessageListener;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;



public abstract class HTTPMessage implements de.infomac.webserver.http.HTTPMessage {
    
    private de.infomac.webserver.http.HTTPHeaders headers;
    private HTTPVersion version;
    private MessageBody messageBody;

	private java.util.Set listeners = new java.util.HashSet();
    
    public HTTPMessage(HTTPVersion v){
        this(v,new HTTPHeaders());
    }
    
    public HTTPMessage(HTTPVersion v,de.infomac.webserver.http.HTTPHeaders h){
        headers = h;
        version = v;
    }
    
    /**
     * @return Returns the version.
     */
    public HTTPVersion getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(HTTPVersion version) {
        this.version = version;
    }
    
    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPRequest#getHeader(java.lang.String)
     */
    public Object getHeader(String field_name) {
        return headers.get(field_name);
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPRequest#setHeader(java.lang.String, java.lang.Object)
     */
    public void setHeader(String field_name, Object val) {
       
        headers.put(field_name,val,true);
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPRequest#setHeaderNonDestructive(java.lang.String, java.lang.Object)
     */
    public void setHeaderNonDestructive(String field_name, Object val) {
        headers.put(field_name,val,false);
    }


    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPRequest#hasNonEmptyHeader(java.lang.String)
     */
    public boolean hasNonEmptyHeader(String field_name) {
        return headers.hasNonEmptyField(field_name);
    }
    

    public boolean hasHeader(String field_name,Object value) {
        return headers.hasHeader(field_name,value);
    }
    
    public boolean hasHeaderIgnoreCase(String field_name,String value) {
        return headers.hasHeaderIgnoreCase(field_name,value);
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPMessage#getHeaders()
     */
    public de.infomac.webserver.http.HTTPHeaders getHeaders() {
        return headers;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.ceruti.http.HTTPMessage#setHeaders(org.ceruti.http.HTTPHeaders)
     */
    public void setHeaders(de.infomac.webserver.http.HTTPHeaders h) {
        headers = h;
    }

    
    public boolean hasContentLength(){
        try {
            getHeaders().getContentLength();
        }catch (NumberFormatException e){
            return false;
        }
        
        return true;
    }
    
    public long getContentLength(){
        return getHeaders().getContentLength();
    }
    
    public boolean hasTransferEncoding(){
        return hasNonEmptyHeader("Transfer-Encoding");
    }
    
    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPMessage#setMessageBody(cwprx.org.ceruti.http.MessageBody)
     */
    public final void setMessageBody(MessageBody m) throws MessageBodyException {
        this.messageBody = m;
        if(m!=null)
        		m.attachedTo(this);
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.HTTPMessage#getMessageBody()
     */
    public MessageBody getMessageBody() {
        return messageBody;
    }

    
    public final void copyToOutputStream(OutputStream out) throws IOException, MessageBodyException {
		
        //write the message without the body
        try {
            out.write(toString().getBytes());
        }catch(IOException e){
            fireAfterExceptionDuringTransfer(e);
            fireAfterCopyToOutputStream();   
            
            throw e;
        }
		
        //write the message-body
		MessageBody mb = getMessageBody();
		
		try {

			if (mb != null) {
				mb.copyToOutputStream(out); //this will too call the messageBodyListener-Callbacks on Exception etc.
	
				out.flush();
			}
	
			
		
		}

		finally {
		    fireAfterCopyToOutputStream();
		}
	}

    
    public void addListener(HTTPMessageListener ll){
        listeners.add(ll);
    }
    
	public boolean removeListener(HTTPMessageListener ll){
	   return listeners.remove(ll);
	}
	
	public java.util.Iterator getListeners(){
	    return listeners.iterator();
	}
	
	public void fireAfterCopyToOutputStream(){
	    for(java.util.Iterator it = getListeners(); it.hasNext();){
	        HTTPMessageListener l = (HTTPMessageListener)it.next();
	        
	        l.afterCopyToOutputStream(this);
	    }
	}
	public void fireAfterExceptionDuringTransfer(Exception e){
	    for(java.util.Iterator it = getListeners(); it.hasNext();){
	        HTTPMessageListener l = (HTTPMessageListener)it.next();
	        
	        l.afterExceptionDuringTransfer(this,e);
	    }
	}
	
   
	public void cleanup(){
	    if(getMessageBody()!=null)
	        getMessageBody().cleanup();
	}
	
}

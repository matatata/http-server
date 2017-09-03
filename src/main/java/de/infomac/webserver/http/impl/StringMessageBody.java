/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;



import java.io.IOException;
import java.io.StringReader;

import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;



public class StringMessageBody extends MessageBodyBase implements MessageBody {

    private StringReader sr;
    private long length;
    private String s;
    private String contentType;
    
    public StringMessageBody(String s,String contentType){
        sr = new StringReader(s);
        
        length = s.length();
        this.s = s;
        this.contentType = contentType;
    }
    
    public StringMessageBody(String s){
        // I noticed strange behaviour with text/plain, so html is default
        this(s,"text/html");
    }


    public boolean isReadable() {
       return true;
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.MessageBody#read()
     */
    public int read() throws IOException {
        return sr.read();
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.MessageBody#lengthAvailable()
     */
    public boolean lengthAvailable() {
        return true;
    }

    /* (non-Javadoc)
     * @see cwprx.org.ceruti.http.MessageBody#lenght()
     */
    public long length() {
        return length;
    }
    
    public boolean resetAvailable(){
        return true;
    }
    
    public void reset() throws IOException{
        sr = new StringReader(s);
        
        length = s.length();
    }

    public String getContentType(){
        return contentType;
    }

    

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#preFetchData()
     */
    public ByteArrayMessageBody preFetchData() throws MessageBodyException, IOException {
        return new ByteArrayMessageBody(sr.toString().getBytes(),getContentCoding());
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#cleanup()
     */
    public void cleanup() {
        sr.close();
    }

  

 

}

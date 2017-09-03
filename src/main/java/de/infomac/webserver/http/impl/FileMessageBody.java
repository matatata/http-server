/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.http.MessageBody;



public class FileMessageBody extends MessageBodyBase implements MessageBody{

    private FileInputStream fis;
    private File file;
    private long length;
    private static ContentTypeMap contentTypeMap = null;
    private static Category logger = Logger.getInstance(FileMessageBody.class);

   
    
    static {
        try {
            contentTypeMap = new ContentTypeMap("mime.types");
        } catch(FileNotFoundException e){
            logger.error(e);
            contentTypeMap = new ContentTypeMap(true);
        } catch (IOException e) {
            logger.error(e);
            contentTypeMap = new ContentTypeMap(true);
        }
    }
    
    public FileMessageBody(File file) throws FileNotFoundException{
        fis = new FileInputStream(file);
        this.file = file;
        
        addListener(getDefaultListener());
    }
    
    public FileMessageBody(String fileName) throws FileNotFoundException{
        this(new File(fileName));
    }
    
    public boolean hasInputStream() {
        return true;
    }

    public InputStream getInputStream() {
        return fis;
    }

    public boolean isReadable() {
        return true;
    }


    public int read() throws IOException {
        return fis.read();
    }

    public boolean lengthAvailable() {
        return true;
    }

    public long length() {
        return file.length();
    }

    public boolean resetAvailable(){
        return true;
    }
    
    public void reset() throws IOException{
        if(fis!=null)
            fis.close();
        
        fis = new FileInputStream(file);
    }

    public String getContentType(){
        return contentTypeMap.get(file.getName());
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#ETagAvailable()
     */
    public boolean ETagAvailable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getETag()
     */
    public String getETag() {
     return String.valueOf(file.lastModified());   
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#cleanup()
     */
    public void cleanup() {

        try {
            if (fis != null)
                fis.close();
        } catch (IOException e) {

        }
    }
    
    
}

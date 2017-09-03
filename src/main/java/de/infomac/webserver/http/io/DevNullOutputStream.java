/*
 * Created on 23.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.io;

import java.io.IOException;
import java.io.OutputStream;


public class DevNullOutputStream extends OutputStream{

    
    public void close() throws IOException {
        
    }
    
    public boolean equals(Object arg0) {
        return false;
    }
    
    public void flush() throws IOException {
        
    }
    
    public void write(byte[] arg0) throws IOException {
        
    }
   
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
       
    }
   
    public void write(int arg0) throws IOException {
       
    }
}

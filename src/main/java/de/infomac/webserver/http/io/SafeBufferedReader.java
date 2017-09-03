/*
 * @(#)SafePrintWriter.java 1.0 99/07/10
 *
 * Written 1999 by Elliotte Rusty Harold,
 * Explicitly placed in the public domain
 * No rights reserved.
 */
package de.infomac.webserver.http.io;

import java.io.IOException;
import java.io.Reader;



/**
 * @version   1.0, 99/07/10
 * @author  Elliotte Rusty Harold
 * @since Java Network Programming, 2nd edition
 */



public class SafeBufferedReader extends java.io.BufferedReader {
    private Reader in;
    public SafeBufferedReader(Reader in) {
        this(in, 1024);
        
        this.in = in;
    }
    
    /**
     * The original unmodified reader
     * @return
     */
    public Reader getSrcReader(){
        return in;
    }

    public SafeBufferedReader(Reader in, int bufferSize) {
        super(in, bufferSize);
    }

    private boolean lookingForLineFeed = false;
    private boolean dirty = false;


    public String readLine() throws IOException {
        StringBuffer sb = new StringBuffer("");
        while (true) {
            int c = super.read();
            if (c == -1) { // end of stream
                if (sb.equals(""))
                    return null;
                return sb.toString();
            } else if (c == '\n') {
                if (lookingForLineFeed) {
                    lookingForLineFeed = false;
                    continue;
                } else {
                    return sb.toString();
                }
            } else if (c == '\r') {
                lookingForLineFeed = true;
                dirty = true;
                return sb.toString();
            } else {
                lookingForLineFeed = false;
                sb.append((char) c);
            }
        }
    }
    
 
    
}
/*
 * Created on 22.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.http;

import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTPVersion {
    private int maj, min;
    
    public static final HTTPVersion VERSION_1_0= new HTTPVersion(1,0);
    public static final HTTPVersion VERSION_1_1= new HTTPVersion(1,1);
    
    
    public boolean isVersion1_0(){
        return equals(VERSION_1_0);
    }
    
    public boolean isVersion1_1(){
        return equals(VERSION_1_1);
    }
    
    /**
     * HTTP-Version = "HTTP" "/" 1*DIGIT "." 1*DIGIT Leading zeros MUST be
     * ignored by recipients
     */
    private static Pattern httpversion = Pattern
            .compile("HTTP/(0*\\d+)\\.(0*\\d+)");

    public HTTPVersion(int maj, int min) {
        this.maj = maj;
        this.min = min;
    }

    /**
     * 
     * @param s
     *            the input string without leading characters
     * @return instance of HTTPVersion
     * @throws MalformedHTTPVersion
     */
    public static HTTPVersion parseHTTPVersion(String s)
            throws MalformedHTTPVersion {

        CharBuffer chBuf = CharBuffer.wrap(s.toCharArray());

        Matcher matcher = httpversion.matcher(chBuf);
        if (matcher.find()) {
            int maj = Integer.parseInt(matcher.group(1));
            int min = Integer.parseInt(matcher.group(2));
            return new HTTPVersion(maj, min);
        }

        throw new MalformedHTTPVersion(s);
    }

    public int getMajor() {
        return maj;
    }

    public int getMinor() {
        return min;
    }

    public boolean equals(Object o) {
        HTTPVersion test = (HTTPVersion) o;
        return (test.getMajor() == getMajor())
                && (test.getMinor() == getMinor());

    }
    
    public String toString(){
        return "HTTP/" + maj + "." + min; 
    }

}
/*
 * Created on 22.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


public class MalformedHTTPVersion extends Exception {

    /**
     * @param s
     */
    public MalformedHTTPVersion(String s) {
        super(s);
    }

}

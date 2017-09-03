/*
 * Created on 14.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;



/**
 * @author matteo
 *
 * TODO comment
 */
public class IllegalByteRangeSpec extends Exception {

    /**
     * @param string
     */
    public IllegalByteRangeSpec(String string) {
        super(string);
    }

    /**
     * @param e
     */
    public IllegalByteRangeSpec(Exception e) {
        super(e);
    }

}

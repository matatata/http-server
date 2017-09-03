/*
 * Created on 15.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;

/**
 * @author matteo
 *
 * TODO comment
 */
public class RequestedRangeNotSatisfiable extends Exception {

    /**
     * @param string
     */
    public RequestedRangeNotSatisfiable(String string) {
        super(string);
    }
    
    public RequestedRangeNotSatisfiable() {
      
    }

    /**
     * @param e
     */
    public RequestedRangeNotSatisfiable(Exception e) {
        super(e);
    }

}

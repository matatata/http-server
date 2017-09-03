/*
 * Created on 01.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


/**
 * @author matteo
 *
 * TODO comment
 */
public class BadRequest extends Exception {

    /**
     * @param e
     */
    public BadRequest(Exception e) {
        super(e);
    }

}

/*
 * Created on 24.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


public class InvalidMessage extends Exception {
    public static final InvalidMessage HOST_HEADER_REQUIRED = new InvalidMessage("Host-HeaderField required");

    public InvalidMessage(String message){
        super(message);
    }
}

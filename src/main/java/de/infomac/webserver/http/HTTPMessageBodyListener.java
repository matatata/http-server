/*
 * Created on 19.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


/**
 * @author matteo
 *
 * TODO comment
 */
public interface HTTPMessageBodyListener {
    public void afterCopyToOutputStream(MessageBody body);
    public void afterExceptionDuringTransfer(MessageBody body,Exception e);
}

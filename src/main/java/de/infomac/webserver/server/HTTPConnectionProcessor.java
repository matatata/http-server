/*
 * Created on 11.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

import java.net.Socket;

import de.infomac.webserver.commons.Poolable;

/**
 * @author matteo
 *
 * TODO comment
 */
public interface HTTPConnectionProcessor extends Poolable, Runnable{
    long getIdleTime();
    void close();
    /**
     * @param connectionSocket
     * @param context
     */
    void init(Socket connectionSocket, Context context);
}

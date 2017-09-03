/*
 * Created on 13.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

/**
 * @author matteo
 *
 * TODO comment
 */
public class AFactory {

    public static HTTPConnectionProcessor createHTTPConnectionProcessor(){
        return new HTTPConnectionProcessorImpl();
    }
}

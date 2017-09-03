/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.io;

import java.io.IOException;


public interface Readable {

    int read() throws IOException;
}

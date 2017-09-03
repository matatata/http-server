/*
 * Created on 23.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;




public class HTTPParseError extends Exception {

    /**
     * @param string
     */
    public HTTPParseError(String string) {
      super(string);
    }

    /**
     * @param e
     */
    public HTTPParseError(Exception e) {
       super(e);
    }

}

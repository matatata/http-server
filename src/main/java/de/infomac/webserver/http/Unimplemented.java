/*
 * Created on 27.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


public class Unimplemented extends RuntimeException {

    /**
     * @param string
     */
    public Unimplemented(String string) {
       super(string);
    }

    /**
     * 
     */
    public Unimplemented() {
        super();
    }

}

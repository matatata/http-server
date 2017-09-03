/*
 * Created on 27.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.commons;


public class Assert {

    public static void condition(Object obj){
        condition(obj!=null);
    }
    
    public static void condition(boolean bool){
        if(!bool)
            throw new AssertionFailed();
    }
}

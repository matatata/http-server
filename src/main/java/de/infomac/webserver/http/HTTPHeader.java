/*
 * Created on 23.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


public class HTTPHeader {
    public String name;
    public Object value;
    
    public HTTPHeader(String name,Object value){
        this.name = name;
        this.value = value;
    }
    
    public String toString(){
        return name + ": " + value;
    }
    
    public boolean equals(Object o){
        HTTPHeader h = (HTTPHeader)o;
        return name.equalsIgnoreCase(h.name) && value.equals(h.value);
    }
    
   /* public int hashCode(){
        return (name + value).hashCode();
    }*/
}

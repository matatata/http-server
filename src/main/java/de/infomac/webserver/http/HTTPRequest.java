/*
 * Created on 22.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;


import java.net.MalformedURLException;







public interface HTTPRequest extends HTTPMessage {

    
    public HTTPVersion getVersion();
    public void setVersion(HTTPVersion v);
    
    public String getRequestURI();
    public void setRequestURI(String u);

    public String getMethod();

    public void setMethod(String method);
    
    public java.net.URL getURL() throws MalformedURLException;
    
    public boolean isAbsoluteURI();
    
    public boolean isMethodGET();
    public boolean isMethodPOST();
    public boolean isMethodHEAD();
    
    public boolean hasAcceptEncoding(String coding);
	/**
	 * @return null if no Accept-Encoding Headerfield is present
	 */
    public String[] getAcceptEncodings();
    
    
}

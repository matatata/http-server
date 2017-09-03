/*
 * Created on 24.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import de.infomac.webserver.http.InvalidMessage;
import de.infomac.webserver.http.UnsupportedMethod;

/**
 * Validates Basic HTTP/1.0 and HTTP/1.1
 */

public abstract class HTTPResponseValidatorBase extends HTTPMessageValidatorBase {

    
    public void validate(de.infomac.webserver.http.HTTPResponse response) throws InvalidMessage, UnsupportedMethod {
    	    validateVersion(response);
    	    validateHeaders(response); 
    }
 
    public void validateHeaders(de.infomac.webserver.http.HTTPResponse msg) throws InvalidMessage {
    
    		super.validateHeaders(msg);
    	
    		//transfer coding only in HTTP/1.1
    		if(msg.getVersion().isVersion1_0()){
    			if(msg.hasNonEmptyHeader("Transfer-Coding"))
    				throw new InvalidMessage("Transfer-Coding is not allowed in HTTP/1.0");
    		}
    }
    
}
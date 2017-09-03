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

public abstract class HTTPRequestValidatorBase extends HTTPMessageValidatorBase {

    
    public void validate(de.infomac.webserver.http.HTTPRequest request) throws InvalidMessage, UnsupportedMethod {
        // Check HTTP-Version
        validateVersion(request);
        validateMethod(request);
        validateHeaders(request);
        validateRequestURI(request);
    }
    

    /**
     * - version 1.0 or 1.1
     * - if 1.1 check for valid URI (Host-Header is required i.e uri is relative)
     * - 
     * @param request
     * @throws InvalidMessage
     */
    public void validateRequestURI(de.infomac.webserver.http.HTTPRequest request)  throws InvalidMessage{
        if (request.getVersion().isVersion1_1()) {
            //if HTTP/1.1 requires "Host"-Header if the request-uri is relative
            if (!request.isAbsoluteURI()) {//is relative so we require Host-Header
                if (!request.hasNonEmptyHeader("Host"))
                    throw InvalidMessage.HOST_HEADER_REQUIRED;
            }
            
        }
        else {
            // version 1.0 does not have any special restrictions
        }
    }
    
    public abstract void validateMethod(de.infomac.webserver.http.HTTPRequest req) throws UnsupportedMethod;

    public void validateHeaders(de.infomac.webserver.http.HTTPRequest req) throws InvalidMessage {
        super.validateHeaders(req);
    }


    
    
}
package de.infomac.webserver.http.impl;

import de.infomac.webserver.http.InvalidMessage;


public class HTTPMessageValidatorBase {

    public void validateVersion(de.infomac.webserver.http.HTTPMessage msg) throws InvalidMessage {
        if (!msg.getVersion().isVersion1_0()
                && !msg.getVersion().isVersion1_1())
            throw new InvalidMessage(msg.getVersion() + " not supported");

    }
    
    public void validateHeaders(de.infomac.webserver.http.HTTPMessage msg) throws InvalidMessage {
        // Section 4.4
        // 
        try{
            //try to get it. If present we catch the NumberFormatException
        		msg.getHeaders().getContentLength();
        }catch(NumberFormatException e){
            throw new InvalidMessage("Content-Length must be an integer");
        }
    }

}

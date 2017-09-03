package de.infomac.webserver.http;



public abstract class HTTPMessageListener {
	//gets called after HTTPMessage.copyToOutputStream was called
	public abstract void afterCopyToOutputStream(HTTPMessage message);
	
	//If an Exception was encountered during transfer (wich means during the HEADER, then we do the same thing as the Body would have done
	
    public void afterExceptionDuringTransfer(HTTPMessage message,Exception e){
        if(message.getMessageBody() != null)
            message.getMessageBody().fireAfterExceptionDuringTransfer(e);
    }

  
}

package de.infomac.webserver.http.impl;




import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ByteArrayMessageBody extends DefaultMessageBody {
	
	public ByteArrayMessageBody(byte []bytes,String encoding){
		super(new ByteArrayInputStream(bytes),encoding,bytes.length);
		
	}
	
	//ByteArrayInputStream objects are marked at position zero by
	// default when constructed.
	public boolean resetAvailable() {
		return true;
	}

	//reset is available
	public void reset() throws IOException {
		getInputStream().reset();
	}

	
}
/*
 * Created on 26.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.http.HTTPMessage;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.Unimplemented;
import de.infomac.webserver.http.impl.byterange.ByteRangeSpec;
import de.infomac.webserver.http.impl.byterange.SuffixByteRangeSpec;
import de.infomac.webserver.http.io.BufferedInputStream;
import de.infomac.webserver.http.io.Utils;

/**
 * Can decode chunked data
 */
public class ChunkedMessageBody extends MessageBodyBase {

	private BufferedInputStream sbr;
	private static Category logger = Logger
    .getInstance(ChunkedMessageBody.class);
	private String contentCoding;
	private boolean unchunk_onTheFly;
	
	
	
	/**
	 * 
	 * @param uch
	 */
	public void setUnchunkOnTheFly(boolean uch){
	    unchunk_onTheFly = uch;
	    
	}
	
	public boolean hasInputStream() {
		return true;
	}

	public InputStream getInputStream() {
		return sbr;
	}

	public String getContentCoding() {
		return contentCoding;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.ceruti.http.MessageBody#attachedTo(org.ceruti.http.HTTPMessage)
	 */ 
	public void attachedTo(HTTPMessage dest) throws MessageBodyException {
	    //depending on unchunk_onTheFly
	    //we must remove the Transfer-Encoding Header-Field
	    if(unchunk_onTheFly){
	        dest.getHeaders().remove("Transfer-Encoding");
	    }
	    
	    super.attachedTo(dest);
	}
	
	/**
	 * Transfers its content to out. After that calls afterCopyToOutputStream()
	 */
	public void copyToOutputStream(java.io.OutputStream out,int maximum)
			throws IOException, MessageBodyException {
		
	    if(unchunk_onTheFly){
	        try {
	            unchunk(out,(long)maximum);
	        } catch(IOException e){
	            fireAfterExceptionDuringTransfer(e);
	            throw e; 
	        } catch(MessageBodyException e) {
	            fireAfterExceptionDuringTransfer(e);
	            throw e; 
	        }
	        finally{
	            fireAfterCopyToOutputStream();
	        }
	     }
	    else
	        super.copyToOutputStream(out);
	  
	}
	
	/**
	 * Transfers its content to out. After that calls afterCopyToOutputStream()
	 */
	public void copyToOutputStream(java.io.OutputStream out)
			throws IOException, MessageBodyException {
		
	    this.copyToOutputStream(out,-1); //unlimited transfer
	  
	}
	
	/**
	 * 
	 * @param sbr
	 * @param coding
	 */
	public ChunkedMessageBody(BufferedInputStream sbr, String coding) {
	    this(sbr,coding,false);
	}
	
	/**
	 * 
	 * @param sbr
	 * @param coding
	 * @param unchunk_onTheFly if true the data will unchunked when beeing transferred.
	 * 						the Transfer-Coding header will be removed, and a Connection: close header is added.
	 * 						You should close the connection to the client after having sent the response.
	 */
	public ChunkedMessageBody(BufferedInputStream sbr, String coding,boolean unchunk_onTheFly) {
		this.sbr = sbr;
		contentCoding = coding;
		this.unchunk_onTheFly = unchunk_onTheFly;
		addDefaultListener();
	}

	/*
	 * 
	 * 
	 * Chunked-Body   = *chunk
	 * 					last-chunk
	 * 					trailer
	 * 					CRLF
	 * chunk          = chunk-size [ chunk-extension ] CRLF
	 * 				   chunk-data CRLF
	 * chunk-size     = 1*HEX
	 * last-chunk     = 1*("0") [ chunk-extension ] CRLF
	 * chunk-extension= *( ";" chunk-ext-name [ "=" chunk-ext-val ] )
	 * chunk-ext-name = token
	 * chunk-ext-val  = token | quoted-string
	 * chunk-data     = chunk-size(OCTET)
	 * trailer        = *(entity-header CRLF)
	 */
	
	/**
	 * This unchunks the data (trailer-stuff not implemented)
	 * @param unchunked
	 * @param maximum use -1 if you want unlimited amount of data beeing trasferred
	 */
	public void unchunk(java.io.OutputStream unchunked,long maximum) throws IOException,
			MessageBodyException {

		String line;
		long length = 0;
		
		logger.info("unchunking");

		line = sbr.readLine();
		while ((line != null) && (line.length() > 0)) {
			String chunk_size_str = line;
			long chunk_size = 0;
			int ext_pos = chunk_size_str.indexOf(";");

			if (ext_pos != -1)
				chunk_size_str = chunk_size_str.substring(0, ext_pos);

			chunk_size_str = chunk_size_str.replaceAll("^\\s+", "");
			chunk_size_str = chunk_size_str.replaceAll("\\s+$", "");

			try {
				chunk_size = Long.decode("0x" + chunk_size_str).longValue();
			} catch (NumberFormatException e) {
				throw new MessageBodyException("chunk-size (1*HEX) expected");
			}

			if (chunk_size == 0) { //is last-chunk
				break;
			}

			length += chunk_size;

			//transfer chunk-data
			Utils.transferBytes(sbr, unchunked, chunk_size);
			
			if(maximum != -1 && length >= maximum)
			    throw new MessageBodyException("Too many bytes in message");

			line = sbr.readLine();
			if (line.length() != 0)
				throw new MessageBodyException(
						"chunk-data must terminate with CRLF");

			line = sbr.readLine(); //next line
		}

		//trailer
		try {
			HTTPHeaders.parseHeaders(sbr); //parse but ignore them
			//TODO do something with it?
		} catch (HTTPParseError e) {
			throw new MessageBodyException("error in trailer");
		}

	}
	

	
	/**
	 * 
	 */
	public MessageBody decode(HTTPVersion version,boolean content_length_required) throws Unimplemented, MessageBodyException,IOException {

	    if(content_length_required){
	        
	        //if the called requires a content_length, we can unchunk the data
	        //and buffer it and return a ByteArrayMessageBody.
	        logger.info("prefetching and decoding (to max=" + prefetch_size + " bytes)");
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			//try {
				unchunk(data,prefetch_size);
			//} catch (IOException e) {
			//	throw new MessageBodyException(e);
			//}
	
			MessageBody ret = new ByteArrayMessageBody(data.toByteArray(),getContentCoding());
			
			return ret;
	    }
	    else { //if the called does not requires a content_length, we can just unchunk the data on the fly
	        setUnchunkOnTheFly(true);
	    
	        return this;
	    }
	}

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#satisfies(org.ceruti.org.ceruti.http.impl.ByteRangeSpec)
     */
    public int satisfies(ByteRangeSpec sp) {
       return 2; //possibly
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#satisfies(org.ceruti.org.ceruti.http.impl.SuffixByteRangeSpec)
     */
    public int satisfies(SuffixByteRangeSpec sp) {
        return 2; //possibly
    }

    
    

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getTransferEncoding()
     */
    public String getTransferEncoding() {
        if(unchunk_onTheFly)
            return null;
        
        return "chunked";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ceruti.http.MessageBody#cleanup()
     */
    public void cleanup() {
        try {
            if (sbr != null)
                sbr.close();
        } catch (IOException e) {

        }
    }

   

}
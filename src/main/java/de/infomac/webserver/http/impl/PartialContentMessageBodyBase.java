/*
 * Created on 14.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.*;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.commons.Assert;
import de.infomac.webserver.http.HTTPMessage;
import de.infomac.webserver.http.HTTPMessageBodyListener;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.HTTPResponse;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.PartialContentMessageBody;
import de.infomac.webserver.http.Unimplemented;
import de.infomac.webserver.http.impl.byterange.ByteContentRangeSpec;
import de.infomac.webserver.http.impl.byterange.ByteRangeSpecBase;
import de.infomac.webserver.http.impl.byterange.ByteRangesSpecifier;



public abstract class PartialContentMessageBodyBase extends MessageBodyBase
        implements PartialContentMessageBody {
    private static Category logger = Logger.getInstance(PartialContentMessageBodyBase.class);
    private String range_unit = "bytes";

    private MessageBody origBody;

    private ByteContentRangeSpec spec;

    private java.io.OutputStream origOut;

    
    public java.util.Iterator getListeners(){
        return origBody.getListeners();
    }
    
    public void addListener(HTTPMessageBodyListener l){
        origBody.addListener(l);
    }
    
    
    public HTTPMessageBodyListener newDefaultListener(){
        return origBody.getDefaultListener();
    }
    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#fireAfterCopyToOutputStream()
     */
    public void fireAfterCopyToOutputStream() {
        origBody.fireAfterCopyToOutputStream();
    }


    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#fireAfterExceptionDuringTransfer(java.lang.Exception)
     */
    public void fireAfterExceptionDuringTransfer(Exception e) {
        origBody.fireAfterExceptionDuringTransfer(e);
    }
    
   /*
    *  (non-Javadoc)
    * @see org.ceruti.http.PartialContentMessageBody#setOriginalDataStream(java.io.OutputStream)
    */
    public void setOriginalDataStream(java.io.OutputStream dest) {
        origOut = dest;
    }
	/*
	 *  (non-Javadoc)
	 * @see org.ceruti.http.PartialContentMessageBody#getByteContentRangeSpec()
	 */
    public ByteContentRangeSpec getByteContentRangeSpec() {
        return spec;
    }
    /*
     *  (non-Javadoc)
     * @see org.ceruti.http.PartialContentMessageBody#getFullEntityBody()
     */
    public MessageBody getFullEntityBody() {
        return origBody;
    }

    public String getRangeUnit() {
        return range_unit;
    }

    public void copyToOutputStream(java.io.OutputStream out)
            throws IOException, MessageBodyException {
        long sentBytes;
        try {
            sentBytes = spec.transferBytes(origBody, out, origOut);

            out.flush();
            if (origOut != null)
                origOut.flush();
        } catch (RequestedRangeNotSatisfiable e) {
            fireAfterExceptionDuringTransfer(e);
            throw new MessageBodyException(e);
        } catch(IOException e){
            fireAfterExceptionDuringTransfer(e);
            throw e; 
        } catch(MessageBodyException e) {
            fireAfterExceptionDuringTransfer(e);
            throw e; 
        }
        finally {
            fireAfterCopyToOutputStream();
        }

    }
    
    public void copyToOutputStream(java.io.OutputStream out,long max)
            throws IOException, MessageBodyException {
       logger.error("operation not supported");
       Assert.condition(false);
    }

    /**
     * 
     * @param originalBody
     * @param spec
     * @throws RequestedRangeNotSatisfiable
     */
    public PartialContentMessageBodyBase(MessageBody originalBody,
            ByteContentRangeSpec spec) throws RequestedRangeNotSatisfiable {

        if (spec.canBeSatisfiedBy(originalBody) == 0) {
            //cannot be applied
            throw new RequestedRangeNotSatisfiable(spec + " cannot satisfy "
                    + originalBody);
        }

        this.origBody = originalBody;
        this.spec = spec;

    }

    /**
     * 
     * @param dest
     * @throws MessageBodyException
     */
    public void attachedTo(HTTPMessage dest) throws MessageBodyException {

        Assert.condition(spec.instanceLengthAvailable());

        super.attachedTo(dest); //this will set the Content-Length Field

        //override the Content-Length, since this is a partial body of a known length
        dest.setHeader("Content-Range", spec.toString());

    }

    /**
     * @return
     */
    public long length() {
        return spec.length();
    }

    /**
     * @return
     */
    public boolean lengthAvailable() {
        return spec.lengthAvailable();
    }

    /*
     *  (non-Javadoc)
     * @see org.ceruti.http.MessageBody#decode(org.ceruti.http.HTTPVersion, boolean)
     */
    
    public MessageBody decode(HTTPVersion version) throws Unimplemented,
    MessageBodyException , IOException{
        //TODO is this needed?
        throw new Unimplemented("does not make sense!?");
    }
    
    public MessageBody decode(HTTPVersion version,boolean content_length_required) throws Unimplemented,
            MessageBodyException, IOException {
        try {
            return new PartialContentMessageBodyBase(origBody.decode(version,content_length_required),
                    getByteContentRangeSpec()) {
                public boolean fullEntityBodyAvailable() {
                    return true;
                }
            };
        } catch (RequestedRangeNotSatisfiable e) {
            throw new MessageBodyException(e);
        } catch (MessageBodyException e) {
            throw new MessageBodyException(e);
        }
    }

    //Delegate

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return origBody.equals(arg0);
    }

    /**
     * @return
     */
    public String getContentCoding() {
        return origBody.getContentCoding();
    }

    /**
     * @return
     */
    public String getContentType() {
        return origBody.getContentType();
    }

    /**
     * @return
     */
    public InputStream getInputStream() {
        return origBody.getInputStream();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return origBody.hashCode();
    }

    /**
     * @return
     */
    public boolean hasInputStream() {
        return origBody.hasInputStream();
    }

    /**
     * @return
     */
    public boolean isReadable() {
        return origBody.isReadable();
    }

    /**
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        return origBody.read();
    }

    /**
     * @throws IOException
     */
    public void reset() throws IOException {
        origBody.reset();
    }

    /**
     * @return
     */
    public boolean resetAvailable() {
        return origBody.resetAvailable();
    }
    
    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#ETagAvailable()
     */
    public boolean ETagAvailable() {
        return origBody.ETagAvailable();
    }

    /* (non-Javadoc)
     * @see org.ceruti.http.MessageBody#getETag()
     */
    public String getETag() {
     return origBody.getETag();   
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return origBody.toString();
    }

    public static PartialContentMessageBodyBase getPartialContentMessageBody(
            MessageBody original, String range_header_field_value,final boolean fullBodyAvailable)
            throws RequestedRangeNotSatisfiable, IllegalByteRangeSpec {
        try {
            ByteRangesSpecifier specifier = ByteRangeSpecBase
                    .parseByteRangeSpecs(range_header_field_value);
            return getPartialContentMessageBody(original,specifier,fullBodyAvailable);
        } catch (HTTPParseError e) {
            throw new IllegalByteRangeSpec(e);
        }
    }

    public static PartialContentMessageBodyBase getPartialContentMessageBody(
            MessageBody original, ByteRangesSpecifier specifier,final boolean fullBodyAvailable)
            throws RequestedRangeNotSatisfiable {

        return new PartialContentMessageBodyBase(original, ByteContentRangeSpec
                .overlap(original, specifier)) {
            public boolean fullEntityBodyAvailable() {
                return fullBodyAvailable;
            }
        };

    }
    
    /**
     * Modifies a response so that it satisfies the Range-Request. If the
     * range-request is invalid the original response with the unmodified
     * origResponse will be returned. If the Range-Request is not satisfyable,
     * then a 416 "Requested range not satisfiable" is returned.
     * 
     * @param Range-Header-Field
     *            Value (e.g. bytes=500-999)
     * @param origResponse
     *            to be partially sent
     * @return
     */
    public static HTTPResponse processRangeRequest(String range_header_field,
            HTTPResponse origResponse) {
        /*
         * If the byte-range-set is unsatisfiable, the org.ceruti.server SHOULD return a
         * response with a status of 416 (Requested range not satisfiable).
         * Otherwise, the org.ceruti.server SHOULD return a response with a status of 206
         * (Partial Content) containing the satisfiable ranges of the
         * entity-body.
         *  
         */
        try {
            MessageBody origMessageBody = origResponse.getMessageBody();
            
            
            boolean FULLBODYAVAIL = origResponse.getStatusCode() != 206;
            
            //first see, check for validity
            ByteRangesSpecifier specifier;
            try {
                specifier = ByteRangeSpecBase.parseByteRangeSpecs(range_header_field);
            } catch (HTTPParseError e1) {
                throw new IllegalByteRangeSpec(e1);
            }
            
            if (!origMessageBody.lengthAvailable()) {
                //It is likely that the Range cannot be satisfied, since there
                //is no lenght known,
                
                try {
                    
                    //but maybe the Transfer-Encoding can be decoded
                    if(origMessageBody.getTransferEncoding() != null){
                        //TODO do we really want to maybe force the origMessageBody to buffer the whole data?
                        boolean content_length_required = true; //could say false instead,
                        							//but then decode would likely return a message-Body
                        							//that does not have a content-length either.
                        
                        origMessageBody = origMessageBody.decode(origResponse.getVersion(),content_length_required);
                        //seemed to be successfull, so the data is no more transfer-encoded
                        //we do not have to remove the Transfer-Encoding-Header this should be done by the body itself
                        //origResponse.getHeaders().remove("Transfer-Encoding");
                    }
                    else {//there is no transfer-encoding and no legth()... try to prefetch the Data?
                        
                        origMessageBody = origMessageBody.preFetchData();
                    }
                    
                    
                    
                    
                }catch(MessageBodyException me){
                    // could not decode or prefetch.... so the following code probably will throw the 416 Response
                } catch (Unimplemented e) {
                    // could not decode or prefetch.... so the following code probably will throw the 416 Response
                } catch (IOException e) {
                    // could not decode or prefetch.... so the following code probably will throw the 416 Response
                }
            }
            
            
            PartialContentMessageBody partial = PartialContentMessageBodyBase
                    .getPartialContentMessageBody(
                            origMessageBody, specifier,FULLBODYAVAIL);

            origResponse.setMessageBody(partial); //set the new partial body
            origResponse.setStatusCode(206);
            origResponse.setReasonPhrase("Partial Content");
            logger.info("delivering Range: " + partial.getByteContentRangeSpec());
            return origResponse;

        } catch (RequestedRangeNotSatisfiable e2) {
            return de.infomac.webserver.http.impl.HTTPResponse.getResponse(HTTPVersion.VERSION_1_1,
                    416, "Requested range not satisfiable");

        } catch (MessageBodyException e) {
            //TODO do a BooBoo instead?
            return de.infomac.webserver.http.impl.HTTPResponse.getResponse(HTTPVersion.VERSION_1_1,
                    416, "Requested range not satisfiable");
        } catch(IllegalByteRangeSpec e2){
            //if invalid rfc sais we should return ignore the Range-Header-Field
            logger.debug(e2);
            return origResponse;
        }
    }

    

    
    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            if ((line = br.readLine()) != null) {

                ByteRangesSpecifier specifier = ByteRangeSpecBase
                        .parseByteRangeSpecs(line);

                System.out.println(specifier);
                //MessageBody body = new DefaultMessageBody(System.in,null);
                MessageBody body = new FileMessageBody(args[0]);

                PartialContentMessageBodyBase pbody = new PartialContentMessageBodyBase(
                        body, ByteContentRangeSpec.overlap(body, specifier)) {
                    public boolean fullEntityBodyAvailable() {
                        return true;
                    }
                };

                HTTPResponse resp = de.infomac.webserver.http.impl.HTTPResponse.getResponse(
                        HTTPVersion.VERSION_1_1, 206, "Partial Content", pbody);

                resp.copyToOutputStream(System.out);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public void cleanup(){
       origBody.cleanup();
   }

}
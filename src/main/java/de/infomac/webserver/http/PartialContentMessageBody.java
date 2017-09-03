/*
 * Created on 18.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;

import de.infomac.webserver.http.impl.byterange.ByteContentRangeSpec;

/**
 * @author matteo
 *
 * The PartialContentMessageBody encapsulates an other instance of
 * MessageBody, but transfers only a specified range of bytes.
 * let [x,y] be the byte-range, the copyToOutputStream()-Method will
 * skip the first x-1 bytes of the original MessageBody and copy (y - x) + 1 bytes to the output-stream.
 * Since the bytes 0-x and the remaining bytes after y are available you can send the original MessageBody
 * to a stream as well by using setOriginalDataStream()
 */
public interface PartialContentMessageBody extends MessageBody{
    /**
     * Since this is a partial MessageBody 
     * @return
     */
    public boolean fullEntityBodyAvailable();
    
    public ByteContentRangeSpec getByteContentRangeSpec();

    public String getRangeUnit();
    
    public MessageBody getFullEntityBody();
    
    /**
     * 
     * @param dest receives all the available bytes (not only the specified byte-range) of the original MessageBody
     * 			during copyToOutputStream();
     */
    public void setOriginalDataStream(java.io.OutputStream dest);
}

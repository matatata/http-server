/*
 * Created on 14.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl.byterange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.impl.RequestedRangeNotSatisfiable;
import de.infomac.webserver.http.io.DevNullOutputStream;
import de.infomac.webserver.http.io.Readable;
import de.infomac.webserver.http.io.Utils;


/**
 * @author matteo
 *
 * TODO comment
 */

public class SuffixByteRangeSpec extends ByteRangeSpecBase{
    public int suffix_length;
    
 
    
    
    public SuffixByteRangeSpec(int length){
        suffix_length = length;
    }
    /**
     * 
     * @param s including the '-' LWS stripped
     */
    public static SuffixByteRangeSpec parseSuffixByteRangeSpec(String s) throws HTTPParseError{
        
        Pattern range = Pattern
        .compile("-(\\d+)");
        
        CharBuffer chBuf = CharBuffer.wrap(s.toCharArray());

        Matcher matcher = range.matcher(chBuf);
        
        
        if (matcher.find()) {
            try {
                return new SuffixByteRangeSpec(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException e) {
                throw new HTTPParseError("illegal byte-range-spec: " + s);
            }

        }
        
        throw new HTTPParseError("illegal byte-range-spec: " + s);
        
    }
    /*
     *  (non-Javadoc)
     * @see org.ceruti.org.ceruti.http.impl.ByteRangeSpecBase#canBeSatisfiedBy(org.ceruti.http.MessageBody)
     */
    public int canBeSatisfiedBy(MessageBody body){
        if(!body.lengthAvailable())
            return 0;
        else {
            if(suffix_length > 0 )//suffix_length< body.length()
                return 1; //yes
        }
                
        return 0; //no
    }
    
    public String toString(){
        return "-" + suffix_length;
    }
    
    public long transferBytes(MessageBody data, OutputStream out,OutputStream originalStream) throws IOException, RequestedRangeNotSatisfiable {
        if(canBeSatisfiedBy(data)==0)
            throw new RequestedRangeNotSatisfiable();
        
       de.infomac.webserver.commons.Assert.condition(data.lengthAvailable());
       long len = 0;
       if(data.hasInputStream()){
           
           java.io.InputStream is = data.getInputStream();
           
           //skip
           len = Utils.transferBytes(is,originalStream != null ? originalStream : new DevNullOutputStream(),data.length() - suffix_length);
           
           if(len!=data.length() - suffix_length)
               throw new RequestedRangeNotSatisfiable("skip failed");
           
           
           return len + Utils.transferBytes(data.getInputStream(),out,suffix_length);
       }
       else if(data.isReadable()){
           len = Utils.transferBytes((Readable)data,originalStream != null ? originalStream : new DevNullOutputStream(),data.length() - suffix_length);
           
           if(len!=data.length() - suffix_length)
               throw new RequestedRangeNotSatisfiable("skip failed");
           
           return len + Utils.transferBytes((Readable)data,out,suffix_length);
       }
       
       de.infomac.webserver.commons.Assert.condition(false);
       return 0; 
    }
   
    public boolean lengthAvailable() {
        return true;
    }
   
    public int length() {
        return suffix_length;
    }
}

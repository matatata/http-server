/*
 * Created on 14.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl.byterange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.impl.IllegalByteRangeSpec;
import de.infomac.webserver.http.impl.RequestedRangeNotSatisfiable;
import de.infomac.webserver.http.io.DevNullOutputStream;
import de.infomac.webserver.http.io.Readable;
import de.infomac.webserver.http.io.Utils;


/**
 * @author matteo
 *
 * TODO comment
 */
public class ByteRangeSpec extends ByteRangeSpecBase{
    public int first_byte_pos = -1;
    public int last_byte_pos = -1;
    public boolean last_byte_pos_given;
    
    
    /*
     *  (non-Javadoc)
     * @see org.ceruti.org.ceruti.org.ceruti.http.impl.byterange.ByteRangeSpecBase#transferBytes(org.ceruti.http.MessageBody, java.io.OutputStream, java.io.OutputStream)
     */
    public long transferBytes(MessageBody data,OutputStream out,OutputStream originalStream ) throws IOException, RequestedRangeNotSatisfiable, MessageBodyException{
        
        if(canBeSatisfiedBy(data)==0)
            throw new RequestedRangeNotSatisfiable();
        
        if (data.hasInputStream())
            return transferBytes(data.getInputStream(),out,originalStream);
        else if(data.isReadable())
            return transferBytes((Readable)data,out,originalStream);
        
        throw new MessageBodyException("MessageBody can not be transferred");
  	 
    }
    
    public boolean lengthAvailable(){
        return (last_byte_pos_given == true);
    }
    
    public int length(){
       if(last_byte_pos_given)
          return last_byte_pos - first_byte_pos + 1;
       return -1;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.ceruti.org.ceruti.org.ceruti.http.impl.byterange.ByteRangeSpecBase#transferBytes(org.ceruti.http.MessageBody, java.io.OutputStream, java.io.OutputStream)
     */
    private long transferBytes(InputStream in, OutputStream out,OutputStream originalStream) throws IOException, RequestedRangeNotSatisfiable {
        long sent = 0;
        //skip
        sent = Utils.transferBytes(in,originalStream != null ? originalStream : new DevNullOutputStream(),first_byte_pos);
        
        
        if(sent!=first_byte_pos)
            throw new RequestedRangeNotSatisfiable("skip failed");
        
        if(last_byte_pos_given){
            int len = last_byte_pos - first_byte_pos + 1;
            return sent + Utils.transferBytes(in,out,len);
        }
        
        return sent + Utils.transferBytes(in,out);
        
    }
    
    /*
     *  (non-Javadoc)
     * @see org.ceruti.org.ceruti.org.ceruti.http.impl.byterange.ByteRangeSpecBase#transferBytes(org.ceruti.http.MessageBody, java.io.OutputStream, java.io.OutputStream)
     */
    private long transferBytes(Readable in, OutputStream out,OutputStream originalStream) throws IOException, RequestedRangeNotSatisfiable {
        
        int c = 0;
        //skip
        long sent = Utils.transferBytes(in,originalStream != null ? originalStream : new DevNullOutputStream(),first_byte_pos);
        
            
            if(sent != first_byte_pos)//unexpected EOF
                throw new RequestedRangeNotSatisfiable("skip failed");
            
     
        
        if(this.lengthAvailable())
            return sent + Utils.transferBytes(in,out,this.length());
        
        return sent + + Utils.transferBytes(in,out);
    }
    /**
     * 
     * @param first
     * @param last use -1 if unknown
     * @throws IllegalByteRangeSpec
     */
    public ByteRangeSpec(int first,int last) throws IllegalByteRangeSpec{
        first_byte_pos = first;
        last_byte_pos = last;
        
        
        
        if(first_byte_pos < 0)
            throw new IllegalByteRangeSpec("first_byte_pos must not be negative");
        
        if(last_byte_pos != -1 && first_byte_pos > last_byte_pos)
            throw new IllegalByteRangeSpec("first_byte_pos > last_byte_pos");
        
        last_byte_pos_given = last_byte_pos != -1;
    }
    
    public ByteRangeSpec(int first) throws IllegalByteRangeSpec{
        this(first,-1);
        
    }
    
    /**
     * 
     * @param s LWS already stripped
     * @return
     * @throws HTTPParseError
     * @throws IllegalByteRangeSpec
     */
    public static ByteRangeSpec parseByteRangeSpec(String s) throws HTTPParseError, IllegalByteRangeSpec{
        
        
        Pattern range = Pattern
        .compile("(\\d+)-(\\d+)?");
        
        CharBuffer chBuf = CharBuffer.wrap(s.toCharArray());

        Matcher matcher = range.matcher(chBuf);
        
        
        if (matcher.find()) {
            try {
                int first = Integer.parseInt(matcher.group(1));
                int last = -1;

                if (matcher.group(2)!=null) {
                    last = Integer.parseInt(matcher.group(2));
                    return new ByteRangeSpec(first, last);
                }

                return new ByteRangeSpec(first);

            } catch (NumberFormatException e) {
                throw new HTTPParseError("illegal byte-range-spec: " + s);
            }

        }
        
        throw new HTTPParseError("illegal byte-range-spec: " + s);
       
      }
    
  
    public int canBeSatisfiedBy(MessageBody body){
        if(body.lengthAvailable()){
            if(first_byte_pos >= body.length())
                return 0; //no
            return 1; //yes
        }
        
        return 2; 
        
    }
    


    public String toString(){
        StringBuffer buf = new StringBuffer(String.valueOf(first_byte_pos)).append("-");
        
        if(last_byte_pos_given)
            buf.append(last_byte_pos);
        
        return buf.toString();
    }

 

}

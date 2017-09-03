/*
 * Created on 14.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl.byterange;


import java.io.IOException;
import java.io.OutputStream;

import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.impl.IllegalByteRangeSpec;
import de.infomac.webserver.http.impl.RequestedRangeNotSatisfiable;




/**
 * @author matteo
 *
 * TODO comment
 */
public abstract class ByteRangeSpecBase {
    
   /**
    * 
    * @param body
    * @return 0 "no" 1 "yes" 2 "possibly"
    */
    public abstract int canBeSatisfiedBy(MessageBody body);
    
    public abstract boolean lengthAvailable();
    
    public abstract int length();
    
    /**
     * Sends only a range of bytes. Must also be able to copy the original bytes to the originalStream paramter
     * 
     * @param data
     *            original MessageBody
     * @param out
     *            where only the specified byte-range of the original body will
     *            be copied to
     * @param originalStream
     *            where all the available bytes of the original body will be
     *            copied to (can be null, if you are not interested in the full
     *            body)
     * @return @throws
     *         IOException
     * @throws RequestedRangeNotSatisfiable
     * @throws MessageBodyException
     */
    public abstract long transferBytes(MessageBody data,OutputStream out,OutputStream originalStream) throws IOException, RequestedRangeNotSatisfiable, MessageBodyException;

    
    /**
     * 
     * @param ranges_specifier (LWS already stripped) separated by ", " example bytes=0-999, -10
     * @return ByteRangesSpecifier
     * @throws HTTPParseError
     * @throws IllegalByteRangeSpec
     */
    public static ByteRangesSpecifier parseByteRangeSpecs(String ranges_specifier) throws HTTPParseError, IllegalByteRangeSpec{
        /*
        ranges-specifier = byte-ranges-specifier
        byte-ranges-specifier = bytes-unit "=" byte-range-set
        byte-range-set  = 1#( byte-range-spec | suffix-byte-range-spec )
        byte-range-spec = first-byte-pos "-" [last-byte-pos]
        first-byte-pos  = 1*DIGIT
        last-byte-pos   = 1*DIGIT
        suffix-byte-range-spec = "-" suffix-length
        suffix-length = 1*DIGIT 
         */
        int p = ranges_specifier.indexOf("=");
        if(p<2)
            throw new HTTPParseError("illegal ranges_specifier: " + ranges_specifier);
        
        String bunit = ranges_specifier.substring(0,p);
        
        ranges_specifier = ranges_specifier.substring(p+1);
        
        String []specs = ranges_specifier.split(", |,");
        ByteRangeSpecBase [] byte_ranges = new ByteRangeSpecBase[specs.length];
        
        for(int i = 0; i < specs.length ; i++){
            if(specs[i].length()<=1)
                throw new HTTPParseError("illegal ranges_specifier: " + ranges_specifier);
            
            if(specs[i].charAt(0)=='-')
                byte_ranges[i] = SuffixByteRangeSpec.parseSuffixByteRangeSpec(specs[i]);
            else
                byte_ranges[i] = ByteRangeSpec.parseByteRangeSpec(specs[i]);
        }
        
        return new ByteRangesSpecifier(bunit, byte_ranges);
    }
    
}
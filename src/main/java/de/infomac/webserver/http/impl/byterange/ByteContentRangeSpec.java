/*
 * Created on 15.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl.byterange;

import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.impl.IllegalByteRangeSpec;
import de.infomac.webserver.http.impl.RequestedRangeNotSatisfiable;



/**
 * @author matteo
 * 
 * TODO comment
 */
public class ByteContentRangeSpec extends ByteRangeSpec {

    private int instanceLength = -1;

    private String bytes_unit = "bytes";

    public int getInstanceLength(){
        return instanceLength;
    }
    
    public boolean instanceLengthAvailable(){
        return instanceLength != -1;
    }
    
    /**
     * 
     * @param unit
     * @param first
     * @param last use -1 if not defined
     * @throws IllegalByteRangeSpec
     */
    public ByteContentRangeSpec(String unit, int first, int last)
            throws IllegalByteRangeSpec {
        this(unit, first, last, -1); // -1 "*"
    }

    /**
     * 
     * @param unit
     * @param first
     * @param last use -1 if not defined
     * @param instanceLength use -1 if not defined
     * @throws IllegalByteRangeSpec
     */
    public ByteContentRangeSpec(String unit, int first, int last,
            int instanceLength) throws IllegalByteRangeSpec {
        super(first, last);

        bytes_unit = unit;
        
        if(instanceLength!=-1 && instanceLength >= 0){
            this.instanceLength = instanceLength;
    
            if (last_byte_pos_given) {
                if (last_byte_pos > instanceLength - 1){
                    last_byte_pos = instanceLength - 1;
                    last_byte_pos_given = true;
                }
            }
            else {
                last_byte_pos = instanceLength - 1;
                last_byte_pos_given = true;
            }
        }
    }
    
    
    public int canBeSatisfiedBy(MessageBody body){
        if(body.lengthAvailable()){
            if(first_byte_pos >= body.length())
                return 0; //no
            return 1; //yes
        }
        
        //we MUST know the instanceLength
        return 0; 
        
    }
    

    /*
     * 
     * byte-content-range-spec = bytes-unit SP byte-range-resp-spec "/" (
     * instance-length | "*" ) byte-range-resp-spec = (first-byte-pos "-"
     * last-byte-pos)
     */

    public static ByteContentRangeSpec parseByteContentRangeSpec(String s)
            throws HTTPParseError, IllegalByteRangeSpec {

        Pattern range = Pattern.compile("(\\w+) (\\d+)-(\\d+)/(\\d+|\\*)");

        CharBuffer chBuf = CharBuffer.wrap(s.toCharArray());

        Matcher matcher = range.matcher(chBuf);
        if (matcher.find()) {
            try {
                String unit = matcher.group(1);
                int first = Integer.parseInt(matcher.group(2));
                int last = Integer.parseInt(matcher.group(3));
                if (!matcher.group(4).equals("*"))
                    return new ByteContentRangeSpec(unit, first, last, Integer
                            .parseInt(matcher.group(4)));

                return new ByteContentRangeSpec(unit, first, last);

            } catch (NumberFormatException e) {
                throw new HTTPParseError("illegal byte-range-spec: " + s);
            }

        }

        throw new HTTPParseError("illegal byte-content-range-spec: " + s);

    }

    /**
     * 
     * @param body
     * @param specifier
     * @return
     * @throws RequestedRangeNotSatisfiable
     */
    public static ByteContentRangeSpec overlap(MessageBody body,
            ByteRangesSpecifier specifier) throws RequestedRangeNotSatisfiable {
        ByteRangeSpecBase[] specs = specifier.byte_range_set;

        if (specs.length == 0)
            throw new RequestedRangeNotSatisfiable(specifier.toString());

        //first find minimum and max byte-positions
        int min = Integer.MAX_VALUE;
        int max = -1;

        int satisfiable_count = 0;
        for (int i = 0; i < specs.length; i++) {
            //do not care for unsatisfaybe ranges
            if (specs[i].canBeSatisfiedBy(body) == 0)
                continue;

            //there there was at least one spec that was satisfiable
            satisfiable_count++;

            if (specs[i] instanceof ByteRangeSpec) {
                ByteRangeSpec s = (ByteRangeSpec) specs[i];
                if (s.first_byte_pos < min)
                    min = s.first_byte_pos;
                if (s.last_byte_pos_given && s.last_byte_pos > max)
                    max = s.last_byte_pos;
            } else if (specs[i] instanceof SuffixByteRangeSpec) {
                SuffixByteRangeSpec s = (SuffixByteRangeSpec) specs[i];
                if (body.lengthAvailable()) {
                    int first = (int) (body.length() - s.suffix_length);
                    if (first < min)
                        min = first;

                    if (body.length() - 1 > max)
                        max = (int) (body.length() - 1);
                }

            } else {
                throw new RequestedRangeNotSatisfiable(
                        "Unknown ByteRangeSpec-Type "
                                + specs[i].getClass().getName() + " in "
                                + specifier.toString());
            }

        }

        if (satisfiable_count <= 0)
            throw new RequestedRangeNotSatisfiable(specifier.toString());

        if (min == -1)
            throw new RequestedRangeNotSatisfiable(specifier.toString());

        try {
            if (body.lengthAvailable())
                return new ByteContentRangeSpec(specifier.bytes_unit, min, max,
                        (int) body.length());
            else
                return new ByteContentRangeSpec(specifier.bytes_unit, min, max);
        } catch (IllegalByteRangeSpec e) {
            throw new RequestedRangeNotSatisfiable(specifier.toString() + ": "
                    + e);
        }

    }

    public String toString() {
        StringBuffer buf = new StringBuffer(bytes_unit + " " + first_byte_pos);
        buf.append("-");
        if(last_byte_pos==-1)
            buf.append("*");
        else
            buf.append(last_byte_pos);
        
        buf.append("/");
        if (instanceLength == -1)
            buf.append("*");
        else
            buf.append(instanceLength);

        return buf.toString();
    }

   

}
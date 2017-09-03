/*
 * Created on 22.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;

import java.util.regex.Pattern;


public class ParsingHelper {
    public final static String CRLF = "\r\n";	// 13 10
    public final static char HT = '\t'; // Horizontal Tab
    public final static char SP = ' '; //Space
    
    /**
     * token          = 1*<any CHAR except CTLs or separators>
       separators     = "(" | ")" | "<" | ">" | "@"
                      | "," | ";" | ":" | "\" | <">
                      | "/" | "[" | "]" | "?" | "="
                      | "{" | "}" | SP | HT
     */
    private static Pattern token = Pattern
    .compile("(\\w|-)+");
    
    public static boolean isToken(String tok) {
        return token.matcher(tok).matches();
    }
    
    
    public static boolean matches(String pattern,CharSequence seq){
        return Pattern.matches(pattern,seq);
    }
}

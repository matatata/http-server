/*
 * Created on 24.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;



public interface HTTPResponse extends HTTPMessage {
/*
    An Allow header field MUST be
    present in a 405 (Method Not Allowed) response.

        Allow   = "Allow" ":" #Method

    Example of use:

        Allow: GET, HEAD, PUT
*/


    /**
     * @return Returns the reasonPhrase.
     */
    public String getReasonPhrase();
    /**
     * @param reasonPhrase The reasonPhrase to set.
     */
    public void setReasonPhrase(String reasonPhrase);
    /**
     * @return Returns the statusCode.
     */
    public int getStatusCode();
    /**
     * @param statusCode The statusCode to set.
     */
    public void setStatusCode(int statusCode);
    /**
     * @return Returns the version.
     */
    public HTTPVersion getVersion();
    /**
     * @param version The version to set.
     */
    public void setVersion(HTTPVersion version);
}

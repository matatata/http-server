/*
 * Created on 23.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.infomac.webserver.http.HTTPHeader;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.ParsingHelper;
import de.infomac.webserver.http.io.BufferedInputStream;

public class HTTPHeaders extends de.infomac.webserver.http.HTTPHeaders {

	private HashMap map = new HashMap();

	/**
	 * Stores the mappings in the same order in wich they occure as
	 * an array of HTTPHeader isntances
	 */
	private List headersOrderedByOccurence = new ArrayList();

	/* (non-Javadoc)
	 * @see cwprx.org.ceruti.http.HTTPHeaders#iterator()
	 */
	public Iterator iterator() {
		return headersOrderedByOccurence.iterator();
	}

	public Object remove(String key) {
		key = key.toLowerCase();
		headersOrderedByOccurence.remove(getEntry(key));
		return map.remove(key);
	}

	private Object getEntry(String key) {
		key = key.toLowerCase();
		return map.get(key);
	}

	/* (non-Javadoc)
	 * @see cwprx.org.ceruti.http.HTTPHeaders#get(java.lang.String)
	 */
	public Object get(String key) {
		key = key.toLowerCase();

		HTTPHeader hh = (HTTPHeader) map.get(key);
		return (hh != null) ? hh.value : null;
	}
	
	


	/* (non-Javadoc)
	 * @see cwprx.org.ceruti.http.HTTPHeaders#put(java.lang.String, java.lang.Object)
	 */
	public Object put(String o, Object value, boolean replaceIfPresent) {

		o = o.toLowerCase(); //Field names are case-insensitive

		// if already present
		if (map.containsKey(o)) {
			HTTPHeader old = (HTTPHeader) getEntry(o);
			if (!replaceIfPresent)
				old.value = (String) old.value + ", " + value;
			else
				old.value = value;
			return old.value;
		}
		// construct new Header
		HTTPHeader hh = new HTTPHeader((String) o, value);

		headersOrderedByOccurence.add(hh);
		map.put(o, hh);
		return null;
	}

	/**
	 * reads the Headers until the "empty line" is read. returns an instance of HTTPHeaders.
	 * It can handle Multi-Lined field-values (LWS) linear-White-space
	 */
	public static HTTPHeaders parseHeaders(BufferedInputStream sbr)
			throws IOException, HTTPParseError {
		// Get header lines.

		HTTPHeaders headers = new HTTPHeaders();
		int state = 0; // 0 = look for field-name 1 = read field-value 2=expecting LWS or new header-field
		String currentString = currentString = sbr.readLine();
		String currentToken = null;
		StringBuffer fieldContent = new StringBuffer();
		boolean done = false;

		while ((currentString != null) && !done) {

			switch (state) {
			case 0: {
				if (currentString.length() == 0) {
					done = true;
					break;
				}

				//read token ':'
				int c = currentString.indexOf(":"); //TODO Is it ':' or ': ' ???
				if (c != -1) {
					String token = currentString.substring(0, c);
					if (!ParsingHelper.isToken(token))
						throw new HTTPParseError("illegal token '" + token
								+ "'");

					currentToken = token;

					currentString = currentString.substring(c + 1);
					
					currentString = currentString.replaceFirst("^(" + ParsingHelper.SP
						+ "|" + ParsingHelper.HT + ")*","");
					
					state = 1;
				} else
					throw new HTTPParseError("expecting field-name': '. not '"
							+ currentString + "'");
			}
				break;
			case 1: {
				// take care of LWS
				if (currentString.length() == 0) {
					// must have been a CRLF before so possibly this will be a
					// LWS or just field-content
					currentString = sbr.readLine();
					state = 2;
					break;
				}
				//replace any LWS with one SP
				currentString = currentString.replaceAll("(" + ParsingHelper.SP
						+ "|" + ParsingHelper.HT + ")+", " ");
				//field-content
				fieldContent.append(currentString);

				currentString = sbr.readLine();
				state = 2;

			}
				break;
			case 2: {
				// expecting LWS or new Header field otherwise it's end of HEADERS

				if (currentString.length() == 0) {
					// must have been a CRLF before so this is the end of
					// the headers
					// put the previous field
					if (currentToken != null)
						headers.put(currentToken, fieldContent.toString().replaceAll("^(" + ParsingHelper.SP
								+ "|" + ParsingHelper.HT + ")*", ""),
								false);

					done = true;
					break;
				}
				int c = currentString.charAt(0);
				if ((c == ParsingHelper.SP) || (c == ParsingHelper.HT)) {
					//remove trailing whitespace continue reading field-value
					currentString = currentString.replaceAll("("
							+ ParsingHelper.SP + "|" + ParsingHelper.HT + ")*$",
							"");
					state = 1;
					break;
				} else { //A New HEADER-field starts
					//put the previous field
					if (currentToken != null)
						headers.put(currentToken, fieldContent.toString().replaceAll("^(" + ParsingHelper.SP
								+ "|" + ParsingHelper.HT + ")*", ""),
								false);
					//discard the old content
					fieldContent = new StringBuffer();
					state = 0;
				}

			}
				break;
			}
		}

		return headers;

	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		for (Iterator it = iterator(); it.hasNext();) {
			HTTPHeader h = (HTTPHeader) it.next();
			buf.append(h).append(ParsingHelper.CRLF);
		}
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see cwprx.org.ceruti.http.HTTPHeaders#containsKey(java.lang.String)
	 */
	public boolean containsKey(String k) {
		return map.containsKey(k.toLowerCase());
	}

    /* (non-Javadoc)
     * @see org.ceruti.http.HTTPHeaders#newInstance()
     */
    public de.infomac.webserver.http.HTTPHeaders newInstance() {
        return new HTTPHeaders();
    }

}
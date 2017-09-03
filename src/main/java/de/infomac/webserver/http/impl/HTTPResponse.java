/*
 * Created on 26.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.commons.Assert;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MalformedHTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.ParsingHelper;
import de.infomac.webserver.http.io.BufferedInputStream;


//No persistent Connections!

public class HTTPResponse extends HTTPMessage implements
		de.infomac.webserver.http.HTTPResponse {
	private int statusCode;

	private String reasonPhrase;


	private static Category logger = Logger.getInstance(HTTPResponse.class);

	
	public static HTTPResponse message(HTTPVersion version, String message) {
		return message(version,200,"OK",message);
	}
	
	
	public static HTTPResponse message(HTTPVersion version, int status,String phrase,String message) {
		try {
			return de.infomac.webserver.http.impl.HTTPResponse.getResponse(version, status, phrase,
					new StringMessageBody(message + "\n"));
		} catch (MessageBodyException e) {
			//StringMessageBody will not produce such an Exception
			logger.error(e);
			Assert.condition(false);
		}
		return null;
	}
	
	/**
	 * 
	 * @param version
	 * @param code
	 * @param phrase
	 * @param body
	 *            can be null
	 * @return @throws
	 *         MessageBodyException
	 */
	public static HTTPResponse getResponse(HTTPVersion version, int code,
			String phrase, MessageBody body) throws MessageBodyException {
		HTTPResponse response = new HTTPResponse(version, code, phrase);

		if (body != null)
			response.setMessageBody(body);

		return response;
	}
	
	/**
	 * 
	 * @param version
	 * @param code
	 * @param phrase
	 */
	public static HTTPResponse getResponse(HTTPVersion version, int code,
			String phrase)  {
		HTTPResponse response = new HTTPResponse(version, code, phrase);

		return response;
	}

	public static HTTPResponse get404(HTTPVersion version, String request_uri) {
		return message(version,404,"Not Found",request_uri + " not found (404)\n");
	}

	// Bad Request
	public static HTTPResponse get400(HTTPVersion version, String message) {
	    return message(version,400,"Bad Request","<b>Bad Request (400)</b><br>"
				+ message + "\n");
	}

	// 500 Internal Server Error
	public static de.infomac.webserver.http.HTTPResponse get500(HTTPVersion version,
			String detail) {
		
		return message(version,500,"Internal Server Error","<b>Internal Server Error (500)</b><br>"
				+ "The Server made a Boo-Boo<p>\n" + detail);
	}

	// 500 Internal Server Error
	public static de.infomac.webserver.http.HTTPResponse get500(HTTPVersion version) {
		return get500(version, "");
	}

	// 500 Internal Server Error
	public static de.infomac.webserver.http.HTTPResponse get500(HTTPVersion version,
			Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		e.printStackTrace(pw);
		pw.flush();
		return get500(version, sw.toString());
	}

	// Not implemented
	public static HTTPResponse get501(HTTPVersion version, String message) {
		
		return message(version,501,"Not Implemented","<b>Not Implemented (501)</b><br>"
							+ message);
	}

	public HTTPResponse(HTTPVersion vers, int statusCode, String phrase,
			de.infomac.webserver.http.HTTPHeaders headers) {
		super(vers, headers);
		this.statusCode = statusCode;
		this.reasonPhrase = phrase;
	}

	public HTTPResponse(HTTPVersion vers, int statusCode, String phrase) {
		this(vers, statusCode, phrase, new HTTPHeaders());
	}

	public HTTPResponse(HTTPVersion vers, Integer statusCode, String phrase) {
		this(vers, statusCode.intValue(), phrase, new HTTPHeaders());
	}

	public HTTPResponse(HTTPVersion vers, Integer statusCode, String phrase,
			de.infomac.webserver.http.HTTPHeaders headers) {
		this(vers, statusCode.intValue(), phrase, headers);
	}

	/**
	 * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase
	 * 
	 * @param line
	 * @return Object[] = {HTTPVersion, Integer,String}
	 * @throws HTTPParseError
	 */
	private static Object[] parseStatusLine(String line) throws HTTPParseError {

		String parts[] = line.split(String.valueOf(ParsingHelper.SP));
		if (parts.length < 2)
			throw new HTTPParseError("Error in '" + line + "'");

		HTTPVersion vers = null;
		try {
			vers = HTTPVersion.parseHTTPVersion(parts[0]);
		} catch (MalformedHTTPVersion e) {
			throw new HTTPParseError(e);
		}
		int code;
		try {
			code = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			throw new HTTPParseError("Illegal statusCode '" + parts[1] + "'");
		}

		StringBuffer phrase = new StringBuffer();
		for (int i = 2; i < parts.length; i++) {
			phrase.append(parts[i]);
			if (i < parts.length)
				phrase.append(ParsingHelper.SP);
		}

		return new Object[] { vers, new Integer(code), phrase.toString() };

	}

	public static de.infomac.webserver.http.HTTPResponse parseHTTPResponse(
			java.io.InputStream is) throws IOException, HTTPParseError {
		final BufferedInputStream sbr = new BufferedInputStream(is);
		String line = sbr.readLine();
		Object[] stat_line = parseStatusLine(line);
		de.infomac.webserver.http.HTTPHeaders headers = HTTPHeaders.parseHeaders(sbr);

		final HTTPResponse response = new HTTPResponse(
				(HTTPVersion) stat_line[0], (Integer) stat_line[1],
				(String) stat_line[2], headers);

		/*
		 * The presence of a message-body [...] is signaled by the inclusion of
		 * a Content-Length or Transfer-Encoding Header-Field But this is not
		 * always the case. Often there is just the Connection: close Header So
		 * we must read Data until the Connection gets closed.
		 */
		try {
			if (response.getVersion().isVersion1_1() && response.hasNonEmptyHeader("Transfer-Encoding")) {
				if (response.getHeader("Transfer-Encoding").equals("chunked"))
					response.setMessageBody(new ChunkedMessageBody(sbr,(String) response.getHeader("Content-Encoding")));
				else
					throw new HTTPParseError("Unknown Transfer-Encoding "+response.getHeader("Transfer-Encoding"));
			} else if (response.getHeaders().getContentLength() != -1) {
				//This will be the normal case
				
				response.setMessageBody(new DefaultMessageBody(sbr,
						(String) response.getHeader("Content-Encoding"), response
								.getHeaders().getContentLength()));
				
			} else if (response.hasHeaderIgnoreCase("Connection", "close")) {
				// No Content-Length but the 'Connection: close' Header
				response.setMessageBody(new DefaultMessageBody(sbr,
						(String) response.getHeader("Content-Encoding")));
			}
			else {//No Content-Length and no 'Connection: close' Header ...
				//... is ok in HTTP/1.0
				if(response.getVersion().isVersion1_0()){
					//...could be nph-Stuff where "nobody" knows how many bytes follow.
					response.setMessageBody(new DefaultMessageBody(sbr,
						(String) response.getHeader("Content-Encoding")));
					logger.debug("might be nph-response");
				}
				else {
					//We really really do not expect a message-body, since it's not HTTP/1.0
					//if there is one, we ignore it
					logger.debug("Empty-Body");
				}
			}
			
				
		} catch (MessageBodyException e) {
			throw new HTTPParseError(e);
		}
		return response;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		//      VERSIOM SP CODE SP PHRASE CRLF
		buf.append(getVersion()).append(ParsingHelper.SP).append(
				getStatusCode()).append(ParsingHelper.SP).append(
				getReasonPhrase()).append(ParsingHelper.CRLF);

		buf.append(getHeaders());
		buf.append(ParsingHelper.CRLF);

		return buf.toString();
	}

	/**
	 * @return Returns the reasonPhrase.
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * @param reasonPhrase
	 *            The reasonPhrase to set.
	 */
	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * @return Returns the statusCode.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            The statusCode to set.
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	
	

}
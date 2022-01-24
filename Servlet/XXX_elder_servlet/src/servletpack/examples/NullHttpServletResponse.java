// kanji=漢字
/*
 * $Id: 6c335195916aebd503fe00d4df4764e62548eed7 $
 *
 * 作成日: 2006/03/16 13:49:10 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * <<クラスの説明>>。
 * @author tamura
 * @version $Id: 6c335195916aebd503fe00d4df4764e62548eed7 $
 */
public class NullHttpServletResponse implements HttpServletResponse {

    public void addCookie(Cookie cookie) {}

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeURL(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {}

    public void sendError(int sc) throws IOException {}

    public void sendRedirect(String location) throws IOException {}

    public void setDateHeader(String name, long date) {}

    public void addDateHeader(String name, long date) {}

    public void setHeader(String name, String value) {}

    public void addHeader(String name, String value) {}

    public void setIntHeader(String name, int value) {}

    public void addIntHeader(String name, int value) {}

    public void setStatus(int sc) {}

    public void setStatus(int sc, String sm) {}

    public String getCharacterEncoding() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public void setContentLength(int len) {}

    public void setContentType(String type) {}

    public void setBufferSize(int size) {}

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {}

    public boolean isCommitted() {
        return false;
    }

    public void reset() {}

    public void setLocale(Locale loc) {}

    public Locale getLocale() {
        return null;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public void resetBuffer() {
        // TODO Auto-generated method stub
        
    }

    public void setCharacterEncoding(String arg0) {
        // TODO Auto-generated method stub
        
    }

	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

} // NullHttpServletResponse

// eof

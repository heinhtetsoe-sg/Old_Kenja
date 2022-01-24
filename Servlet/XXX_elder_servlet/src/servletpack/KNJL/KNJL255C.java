// kanji=漢字
/*
 * $Id: 32fd29ddcae47d6a63979e1c3af8876fae3ee2e9 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.KNJZ100;

/**
 * 出身学校・塾タックシール
 */
public class KNJL255C {
    private static final Log log = LogFactory.getLog(KNJL255C.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJZ100().svf_out(request, response);
    }
}

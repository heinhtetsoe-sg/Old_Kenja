// kanji=漢字
/*
 * $Id: 75ae01e9880e4b436a944b1df4b4ea45b880c219 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中学校用 特別支援学校
 */

public class KNJA134J {
    private static final Log log = LogFactory.getLog(KNJA134J.class);
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Map paramMap = new HashMap(); // 追加パラメータ用
        paramMap.put("OUTPUT_KIND", "TOKUBETSU_SHIEN");
        new KNJA133J().svf_out_ex(request, response, paramMap);
    }
}

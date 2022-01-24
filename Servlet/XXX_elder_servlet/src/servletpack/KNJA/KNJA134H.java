// kanji=漢字
/*
 * $Id: 1917b39c04b43a99d0825893c328909eda239319 $
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
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録　高校用 特別支援学校
 */

public class KNJA134H {
    private static final Log log = LogFactory.getLog(KNJA134H.class);
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Map paramMap = new HashMap(); // 追加パラメータ用
        paramMap.put("OUTPUT_KIND", "TOKUBETSU_SHIEN");
        new KNJA130().svf_out_ex(request, response, paramMap);
    }
}

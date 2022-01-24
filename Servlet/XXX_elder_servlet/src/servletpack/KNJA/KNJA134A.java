// kanji=漢字
/*
 * $Id: c664115a59e8b9af659b704f867eb8423b83795f $
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
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録　高校専攻用 特別支援学校
 */

public class KNJA134A {
    private static final Log log = LogFactory.getLog(KNJA134A.class);
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Map paramMap = new HashMap(); // 追加パラメータ用
        paramMap.put("OUTPUT_KIND", "TOKUBETSU_SHIEN");
        new KNJA130().svf_out_ex(request, response, paramMap);
    }
}

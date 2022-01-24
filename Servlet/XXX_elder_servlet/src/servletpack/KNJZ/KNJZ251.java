// kanji=漢字
/*
 * $Id: cd75b0f2ccf736754abd95ea65bd771568f4b528 $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJG.KNJG010;
import servletpack.KNJZ.detail.KNJServletUtils;

/*
 *
 *  学校教育システム 賢者 [事務管理] 証明書学校データ
 *
 */
public class KNJZ251 {

	private static final Log log = LogFactory.getLog(KNJZ251.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 76351 $ $Date: 2020-08-31 17:52:56 +0900 (月, 31 8 2020) $"); // CVSキーワードの取り扱いに注意
        
        request.getParameterMap().put("category_name", new String[] {createCategoryName(request)});        //証明書情報
        request.getParameterMap().put("certifSchoolOnly", new String[] {"1"});        //証明書学校データのみ
        
        new KNJG010().svf_out(request, response);
    }

    private String createCategoryName(final HttpServletRequest request) {
    	// KNJG010のcategory_nameパラメータ
    	final Object[] categoryName = {
    		null                                 // 学籍番号
    	  , request.getParameter("CERTIF_KINDCD")    // 年度
    	  , request.getParameter("CTRL_YEAR")    // 年度
    	  , request.getParameter("CTRL_SEMESTER")    // 学期
    	  , null    // 学年
    	  , null    // 職員コード
    	  , null    // 評定
    	  , null    // 漢字
    	  , request.getParameter("CTRL_DATE")    // 日付
    	  , null    // 証明書番号
    	  , null    // コメント (調査書)
    	  , request.getParameter("CTRL_YEAR")    // 年度2
    	};
    	
    	final StringBuffer stb = new StringBuffer();
    	String comma = "";
    	for (final Object o :categoryName) {
    		stb.append(comma).append(null == o ? "" : o.toString());
    		comma = ",";
    	}
    	log.info("category_name = " + stb.toString());
		return stb.toString();
	}

}

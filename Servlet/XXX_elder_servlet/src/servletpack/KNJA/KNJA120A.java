// kanji=漢字
/*
 * $Id: 79791212905b86e1003663922397109d906515f6 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA120A {
    private static final Log log = LogFactory.getLog(KNJA120A.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        
        log.fatal("$Revision: 68385 $ $Date: 2019-07-01 17:47:25 +0900 (月, 01 7 2019) $"); // CVSキーワードの取り扱いに注意

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

        final DB2UDB db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
        }
        
        final String z001Name3 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'Z001' AND NAMECD2 = '" + getSchooldiv(request, db2) + "' "));
        final String z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        if ("1".equals(z001Name3)) { // 通信制
            log.info("call KNJA133M.");
            new KNJA133M().svf_out(setParameterKNJA133M(request), response);
        } else if ("kumamoto".equals(z010Name1)) {
            log.info("call KNJA130B.");
            new KNJA130B().svf_out(setParameterKNJA130B(request), response);
        } else {
            log.info("call KNJA130C.");
            new KNJA130C().svf_out(setParameterKNJA130C(request), response);
        }
        
        sd.closeDb(db2);
    }

	private String getSchooldiv(final HttpServletRequest request, final DB2UDB db2) {
		final StringBuffer schoolMstSql = new StringBuffer();
        final String year = request.getParameter("PRINT_YEAR");
		schoolMstSql.append("SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
        if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
        	 String grade = StringUtils.substring(request.getParameter("GRADE_HR_CLASS"), 0, 2);
			schoolMstSql.append(" AND SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "') ");
        }
        final String schooldiv = KnjDbUtils.getOne(KnjDbUtils.query(db2, schoolMstSql.toString()));
		return schooldiv;
	}

    private HttpServletRequest setParameterKNJA133M(HttpServletRequest request) {
        final Map parameterMap = request.getParameterMap();
        parameterMap.put("OUTPUT", array("1")); // 個人
        parameterMap.put("CATEGORY_SELECTED", array(request.getParameter("SCHREGNO")));
        parameterMap.put("KATSUDO", array("1"));
        parameterMap.put("YEAR", array(request.getParameter("PRINT_YEAR")));
        parameterMap.put("SEMESTER", array(request.getParameter("PRINT_SEMESTER")));
        parameterMap.put("GRADE_HR_CLASS", array(request.getParameter("GRADE_HR_CLASS")));
        parameterMap.put("RADIO", array("1"));
        parameterMap.put("remarkOnly", array("1"));
        return request;
    }

    private HttpServletRequest setParameterKNJA130B(HttpServletRequest request) {
        final Map parameterMap = request.getParameterMap();
        parameterMap.put("OUTPUT", array("1")); // 個人
        parameterMap.put("CATEGORY_SELECTED", array(request.getParameter("SCHREGNO")));
        parameterMap.put("KATSUDO", array("1"));
        parameterMap.put("YEAR", array(request.getParameter("PRINT_YEAR")));
        parameterMap.put("SEMESTER", array(request.getParameter("PRINT_SEMESTER")));
        parameterMap.put("GRADE_HR_CLASS", array(request.getParameter("GRADE_HR_CLASS")));
        parameterMap.put("RADIO", array("1"));
        parameterMap.put("remarkOnly", array("1"));
        return request;
    }

    private HttpServletRequest setParameterKNJA130C(HttpServletRequest request) {
        final Map parameterMap = request.getParameterMap();
        
        parameterMap.put("OUTPUT", array("1")); // 個人
        parameterMap.put("category_selected", array(request.getParameter("SCHREGNO")));
        parameterMap.put("katsudo", array("1"));
        parameterMap.put("YEAR", array(request.getParameter("PRINT_YEAR")));
        parameterMap.put("GAKKI", array(request.getParameter("PRINT_SEMESTER")));
        parameterMap.put("GRADE_HR_CLASS", array(request.getParameter("GRADE_HR_CLASS")));
        parameterMap.put("RADIO", array("1"));
        parameterMap.put("remarkOnly", array("1"));
        return request;
    }

    private static String[] array(final String s) {
        return new String[] {s};
    }

}

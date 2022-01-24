// kanji=漢字
/*
 * $Id: 0e0cd51e02742fd4278c0725ca35e86ff335f95a $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA120B {
    private static final Log log = LogFactory.getLog(KNJA120B.class);
    
//    private String _z010Name1;
//    private boolean _isKNJA130;
//    private boolean _isKNJA130C;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        new KNJA120A().svf_out(request, response);
    }

//    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
//        
//        setDb(request);
//        
//        if (_isKNJA130) {
//            new KNJA130().svf_out(setParameter(request), response);
//        } else if (_isKNJA130C) {
//            new KNJA130C().svf_out(setParameter(request), response);
//        } else {
//            new KNJA130B().svf_out(setParameter(request), response);
//        }
//    }
//
//    private void setDb(final HttpServletRequest request) {
//        DB2UDB db2 = null;
//        try {
//            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
//            db2.open();
//            
//            _z010Name1 = query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
//            _isKNJA130  = 1 <= Integer.parseInt(query(db2, " SELECT COUNT(*) AS COUNT FROM MENU_MST T1 INNER JOIN GROUPAUTH_DAT T2 ON T2.MENUID = T1.MENUID WHERE T1.PROGRAMID IN ('KNJA130', 'KNJA130A') "));
//            _isKNJA130C = 1 <= Integer.parseInt(query(db2, " SELECT COUNT(*) AS COUNT FROM MENU_MST T1 INNER JOIN GROUPAUTH_DAT T2 ON T2.MENUID = T1.MENUID WHERE T1.PROGRAMID IN ('KNJA130C', 'KNJA130D') "));
//
//        } catch (Exception ex) {
//            log.error("db open error:", ex);
//        } finally {
//            if (null != db2) {
//                db2.close();
//            }
//        }
//    }
//
//    private String query(DB2UDB db2, final String sql) throws SQLException {
//        String rtn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        try {
//            ps = db2.prepareStatement(sql);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                rtn = rs.getString(1);
//            }
//        } catch (Exception e) {
//            log.error("exception!", e);
//        } finally {
//            DbUtils.closeQuietly(null, ps, rs);
//        }
//        return rtn;
//    }
//
//    private HttpServletRequest setParameter(HttpServletRequest request) {
//        final Map parameterMap = request.getParameterMap();
//        parameterMap.put("OUTPUT", array("1")); // 個人
//        parameterMap.put("CATEGORY_SELECTED", array(request.getParameter("SCHREGNO")));
//        parameterMap.put("KATSUDO", array("1"));
//        parameterMap.put("YEAR", array(request.getParameter("PRINT_YEAR")));
//        parameterMap.put("SEMESTER", array(request.getParameter("PRINT_SEMESTER")));
//        parameterMap.put("GRADE_HR_CLASS", array(request.getParameter("GRADE_HR_CLASS")));
//        parameterMap.put("RADIO", array("1"));
//        parameterMap.put("remarkOnly", array("1"));
//        return request;
//    }
//
//    private static String[] array(final String s) {
//        return new String[] {s};
//    }
}

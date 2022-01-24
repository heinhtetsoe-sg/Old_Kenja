/*
 * $Id: 4e52922297d071d5030b13bca89e1ce967884332 $
 *
 * 作成日: 2016/11/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL338F {

    private static final Log log = LogFactory.getLog(KNJL338F.class);

    private static final String SCHOOL_J = "1";
    private static final String SCHOOL_H = "2";
    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
    	final String form;
    	if ("2".equals(_param._applicantdiv)) {
    		form = "KNJL338F_H.frm";
    	} else {
     		form = "KNJL338F.frm";
    	}
        svf.VrSetForm(form, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyou = Integer.parseInt(_param._gyou);
            int retu = Integer.parseInt(_param._retu);

            while (rs.next()) {
                if (retu > 3) {
                    retu = 1;
                    gyou++;
                    if (gyou > 8) {
                        gyou = 1;
                        svf.VrEndPage();
                    }
                }
                svf.VrsOutn("EXAM_NO" + retu, gyou, rs.getString("RECEPTNO"));
                final String setName = rs.getString("NAME") + "　様";
                if (_param._printaddress != null && "2".equals(_param._printaddress)) {
                    svf.VrsOutn("COURSE_NAME" + retu, gyou, rs.getString("EXAMCOURSE_NAME"));
                    svf.VrsOutn("ZIP_NO" + retu, gyou, rs.getString("ZIPCD"));
                    final String address1 = rs.getString("ADDRESS1");
                    final String address2 = rs.getString("ADDRESS2");
                    final int addr1len = getMS932Bytecount(address1);
                    final int addr2len = getMS932Bytecount(address2);
                    final String addrfield;
                    if (addr1len <= 40 && addr2len <= 40) {
                    	addrfield = "1";
                    } else if (addr1len <= 50 && addr2len <= 50) {
                    	addrfield = "2";
                    } else {
                    	addrfield = "3";
                    }
                    svf.VrsOutn("ADDRESS" + retu + "_1_" + addrfield, gyou, address1);
                    svf.VrsOutn("ADDRESS" + retu + "_2_" + addrfield, gyou, address2);

                    final String nameField = getMS932Bytecount(setName) > 30 ? "_3" : "_2";
                    svf.VrsOutn("NAME" + retu + nameField, gyou, setName);

                } else {
                	if (SCHOOL_H.equals(_param._applicantdiv)) {
                        final String nameField = getMS932Bytecount(setName) > 30 ? "_3" : "_2";
                        svf.VrsOutn("NAME" + retu + nameField, gyou, setName);
                	} else {
                        final String nameField = getMS932Bytecount(setName) > 30 ? "_3" : getMS932Bytecount(setName) > 20 ? "_2" : "_1";
                        svf.VrsOutn("NAME" + retu + nameField, gyou, setName);
                	}
                }
                retu++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
//        if ("3".equals(_param._passDiv)) { // 志願者指定
//            if ("1".equals(_param._applicantdiv)) {
//                stb.append(" SELECT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     APD012.REMARK1 AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD012 ON BASE.ENTEXAMYEAR = APD012.ENTEXAMYEAR AND BASE.EXAMNO = APD012.EXAMNO AND APD012.SEQ = '012' ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND APD012.REMARK1 BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" UNION ALL ");
//                stb.append(" SELECT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     APD012.REMARK2 AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD012 ON BASE.ENTEXAMYEAR = APD012.ENTEXAMYEAR AND BASE.EXAMNO = APD012.EXAMNO AND APD012.SEQ = '012' ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND APD012.REMARK2 BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" UNION ALL ");
//                stb.append(" SELECT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     APD012.REMARK3 AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD012 ON BASE.ENTEXAMYEAR = APD012.ENTEXAMYEAR AND BASE.EXAMNO = APD012.EXAMNO AND APD012.SEQ = '012' ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND APD012.REMARK3 BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" UNION ALL ");
//                stb.append(" SELECT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     APD012.REMARK4 AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD012 ON BASE.ENTEXAMYEAR = APD012.ENTEXAMYEAR AND BASE.EXAMNO = APD012.EXAMNO AND APD012.SEQ = '012' ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND APD012.REMARK4 BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" UNION ALL ");
//                stb.append(" SELECT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     APD012.REMARK5 AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD012 ON BASE.ENTEXAMYEAR = APD012.ENTEXAMYEAR AND BASE.EXAMNO = APD012.EXAMNO AND APD012.SEQ = '012' ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND APD012.REMARK5 BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" ORDER BY ");
//                stb.append("     RECEPTNO ");
//
//            } else {
//                stb.append(" SELECT DISTINCT ");
//                stb.append("     BASE.ENTEXAMYEAR, BASE.APPLICANTDIV, ");
//                stb.append("     BASE.EXAMNO AS RECEPTNO, ");
//                stb.append("     BASE.NAME, BASE.NAME_KANA, ");
//                stb.append("     COURSE.EXAMCOURSE_NAME ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
//                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON BASE.ENTEXAMYEAR = APD001.ENTEXAMYEAR AND BASE.EXAMNO = APD001.EXAMNO AND APD001.SEQ = '001' ");
//                stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV AND COURSE.TESTDIV = '1' AND APD001.REMARK8 = COURSE.COURSECD AND APD001.REMARK9 = COURSE.MAJORCD AND APD001.REMARK10 = COURSE.EXAMCOURSECD ");
//                stb.append(" WHERE ");
//                stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//                stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//                stb.append("     AND BASE.EXAMNO BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
//                stb.append(" ORDER BY ");
//                stb.append("     BASE.EXAMNO ");
//            }
//        } else {
            stb.append(" WITH BASE_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     BASE.ENTEXAMYEAR, ");
            stb.append("     BASE.APPLICANTDIV, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.SUC_COURSECD, ");
            stb.append("     BASE.SUC_MAJORCD, ");
            stb.append("     BASE.SUC_COURSECODE, ");
            stb.append("     V_BASE.TESTDIV ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
            if ("1".equals(_param._passDiv) || "2".equals(_param._passDiv)) {
                stb.append("           AND BASE.ENTDIV = '1' ");
            }
            stb.append("     INNER JOIN V_ENTEXAM_RECEPT_DAT RECEPT ON V_BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("           AND V_BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("           AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("           AND RECEPT.EXAM_TYPE = '1'  ");
            stb.append("           AND V_BASE.RECEPTNO = RECEPT.RECEPTNO ");
            if (!"5".equals(_param._passDiv)) {
                stb.append("           AND RECEPT.JUDGEDIV = '1' ");
                if ("1".equals(_param._passDiv) || "2".equals(_param._passDiv) || "4".equals(_param._passDiv)) {
                    stb.append("           AND RECEPT.PROCEDUREDIV1 = '1' ");
                }
            }
            if (SCHOOL_J.equals(_param._applicantdiv)) {
                stb.append("           AND VALUE(BASE.GENERAL_FLG, '0') != CASE WHEN RECEPT.TESTDIV = '5' THEN '1' ELSE '9' END ");
            } else {
                stb.append("           AND VALUE(BASE.GENERAL_FLG, '0') != CASE WHEN RECEPT.TESTDIV = '3' THEN '1' ELSE '9' END ");
            }
            stb.append(" WHERE ");
            stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
            stb.append(" ), RECEPT_MIN AS ( ");
            stb.append(" SELECT ");
            stb.append("     BASE_T.EXAMNO, ");
            stb.append("     MIN(RECEPT.RECEPTNO) AS RECEPTNO ");
            stb.append(" FROM ");
            stb.append("     BASE_T, ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE, ");
            stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
            stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     BASE_T.ENTEXAMYEAR = V_BASE.ENTEXAMYEAR ");
            stb.append("     AND BASE_T.APPLICANTDIV = V_BASE.APPLICANTDIV ");
            stb.append("     AND BASE_T.EXAMNO = V_BASE.EXAMNO ");
            stb.append("     AND BASE_T.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("     AND BASE_T.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("     AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("     AND BASE_T.EXAMNO = RECEPT.EXAMNO ");
            if (!"5".equals(_param._passDiv)) {
                stb.append("     AND L013.NAMESPARE1 = '1' ");
            }
            if ("2".equals(_param._passDiv)) {
                stb.append("     AND RECEPT.RECEPTNO = '" + _param._receptNo + "' ");
            } else if ("3".equals(_param._passDiv)) {
                stb.append("     AND RECEPT.RECEPTNO BETWEEN '" + _param._receptNo2s + "' AND '" + _param._receptNo2e + "' ");
            } else if ("4".equals(_param._passDiv)) {
                stb.append("     AND RECEPT.RECEPTNO BETWEEN '" + _param._receptNo4s + "' AND '" + _param._receptNo4e + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     BASE_T.EXAMNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     BASE_T.EXAMNO, ");
            stb.append("     RECEPT_MIN.RECEPTNO, ");
            stb.append("     BASE_T.NAME, ");
            stb.append("     BASE_T.NAME_KANA, ");
            stb.append("     BASE_T.SUC_COURSECD, ");
            stb.append("     BASE_T.SUC_MAJORCD, ");
            stb.append("     BASE_T.SUC_COURSECODE, ");
            stb.append("     COURSE.EXAMCOURSE_NAME, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2 ");
            stb.append(" FROM ");
            stb.append("     BASE_T ");
            stb.append("     INNER JOIN RECEPT_MIN ON BASE_T.EXAMNO = RECEPT_MIN.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE_T.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
            stb.append("          AND BASE_T.APPLICANTDIV = COURSE.APPLICANTDIV ");
            stb.append("          AND COURSE.TESTDIV = '1' ");
            stb.append("          AND BASE_T.SUC_COURSECD = COURSE.COURSECD ");
            stb.append("          AND BASE_T.SUC_MAJORCD = COURSE.MAJORCD ");
            stb.append("          AND BASE_T.SUC_COURSECODE = COURSE.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON BASE_T.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
            stb.append("          AND BASE_T.EXAMNO = ADDR.EXAMNO ");
            stb.append(" ORDER BY ");
            stb.append("     BASE_T.TESTDIV, ");
            stb.append("     RECEPT_MIN.RECEPTNO ");
//        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65428 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _passDiv; // 1:入学者全員 2:入学者指定 3:合格者指定 4:手続者指定、5:志願者全員
        private final String _printaddress; //1:住所を印刷しない 2:住所を印刷する
        private final String _receptNo;
        private final String _receptNo2s;
        private final String _receptNo2e;
        private final String _receptNo4s;
        private final String _receptNo4e;
        private final String _gyou;
        private final String _retu;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
//        private final String _printLogStaffcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _passDiv            = request.getParameter("PASSDIV");
            _receptNo           = request.getParameter("RECEPTNO");
            _receptNo2s         = request.getParameter("RECEPTNO2S");
            _receptNo2e         = request.getParameter("RECEPTNO2E");
            _receptNo4s         = request.getParameter("RECEPTNO4S");
            _receptNo4e         = request.getParameter("RECEPTNO4E");
            _gyou               = request.getParameter("GYOU");
            _retu               = request.getParameter("RETU");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
//            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _printaddress       = request.getParameter("PRINTADDRESS");
        }

    }
}

// eof


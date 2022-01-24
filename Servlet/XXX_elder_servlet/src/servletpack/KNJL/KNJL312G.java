/*
 * $Id: 445fd4effec69788b335d78e3d0f56eb261c709e $
 *
 * 作成日: 2016/12/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL312G {

    private static final Log log = LogFactory.getLog(KNJL312G.class);

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
        svf.VrSetForm("KNJL312G.frm", 1);

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　得点リストデータチェック");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            final String subclassName = (String) _param._subclassMap.get(subclassCd);
            svf.VrsOut("SUBCLASS_NAME" + subclassCd, subclassName);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql;
            if (_param._callchk050gflg) {
            	sql = sql2();
            } else {
            	sql = sql1();
            }
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 50;
            int lineCnt = 1;
            String befCd = "";
            String sExamNo = "";
            String eExamNo = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String hallCd = rs.getString("EXAMHALLCD");
                final String hallName = rs.getString("EXAMHALL_NAME");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String shName = rs.getString("SH_NAME");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String tekiyou = rs.getString("TEKIYOU");
                if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(hallCd))) {
                    svf.VrsOut("PRINT_AREA", sExamNo + "\uFF5E" + eExamNo);
                    svf.VrEndPage();
                    svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　得点リストデータチェック");
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
                    for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
                        final String subclassCd = (String) iterator.next();
                        final String subclassName = (String) _param._subclassMap.get(subclassCd);
                        svf.VrsOut("SUBCLASS_NAME" + subclassCd, subclassName);
                    }
                    lineCnt = 1;
                    sExamNo = "";
                    eExamNo = "";
                }
                if ("".equals(sExamNo)) {
                    sExamNo = examno;
                }
                eExamNo = examno;
                svf.VrsOut("HALL_NAME", hallName);
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                final String kanaField = getMS932Bytecount(nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, nameKana);
                svf.VrsOutn("SEX", lineCnt, sex);
                int totalScore = 0;
                for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
                    final String subclassCd = (String) iterator.next();
                    if (null != rs.getString("SCORE" + subclassCd)) {
                        svf.VrsOutn("SCORE" + subclassCd, lineCnt, rs.getString("SCORE" + subclassCd));
                        totalScore += rs.getInt("SCORE" + subclassCd);
                    }
                }
                svf.VrsOutn("TOTAL_SCORE", lineCnt, String.valueOf(totalScore));
                svf.VrsOutn("HOPE1", lineCnt, course1);
                svf.VrsOutn("HOPE2", lineCnt, course2);
                svf.VrsOutn("SDIV", lineCnt, shName);
                svf.VrsOutn("FINSCHHO_CD", lineCnt, fsCd);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);
                svf.VrsOutn("REMARK", lineCnt, tekiyou);

                befCd = hallCd;
                lineCnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrsOut("PRINT_AREA", sExamNo + "\uFF5E" + eExamNo);
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

    private String sql1() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     HALL.EXAM_TYPE || HALL.EXAMHALLCD AS EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     SCORE" + subclassCd + ".SCORE AS SCORE" + subclassCd + ", ");
        }
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN VBASE.JUDGEMENT = '4' THEN L013.NAME1 ELSE '' END AS TEKIYOU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND VBASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON RECEPT.ENTEXAMYEAR = HALL.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = HALL.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = HALL.TESTDIV ");
        if (_param._isKeiai) {
        	stb.append("          AND VBASE.SHDIV = HALL.EXAM_TYPE ");
        } else {
        	stb.append("          AND HALL.EXAM_TYPE = '1' ");
        }
        stb.append("          AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE" + subclassCd + " ON RECEPT.ENTEXAMYEAR = SCORE" + subclassCd + ".ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = SCORE" + subclassCd + ".APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = SCORE" + subclassCd + ".TESTDIV ");
            stb.append("          AND RECEPT.EXAM_TYPE = SCORE" + subclassCd + ".EXAM_TYPE ");
            stb.append("          AND RECEPT.RECEPTNO = SCORE" + subclassCd + ".RECEPTNO ");
            stb.append("          AND SCORE" + subclassCd + ".TESTSUBCLASSCD = '" + subclassCd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAM_TYPE, ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private String sql2() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     HALL.EXAM_TYPE || HALL.EXAMHALLCD AS EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     SCORE" + subclassCd + ".SCORE AS SCORE" + subclassCd + ", ");
        }
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN VBASE.JUDGEMENT = '4' THEN L013.NAME1 ELSE '' END AS TEKIYOU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND VBASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON RECEPT.ENTEXAMYEAR = HALL.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = HALL.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = HALL.TESTDIV ");
        if (_param._isKeiai) {
        	stb.append("          AND VBASE.SHDIV = HALL.EXAM_TYPE ");
        } else {
        	stb.append("          AND HALL.EXAM_TYPE = '1' ");
        }
        stb.append("          AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append("          AND HALL.EXAMHALLCD       = '" + _param._examhallcd + "' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S2 ");
        stb.append("           ON S2.ENTEXAMYEAR      = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S2.EXAMNO           = VBASE.EXAMNO ");
        stb.append("          AND S2.SEQ              = '001' ");
        stb.append("     INNER JOIN ENTEXAM_PERFECT_MST S4 ");
        stb.append("           ON S4.ENTEXAMYEAR      = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S4.APPLICANTDIV     = VBASE.APPLICANTDIV ");
        stb.append("          AND S4.TESTDIV          = VBASE.TESTDIV ");
        stb.append("          AND S4.COURSECD         = S2.REMARK8 ");
        stb.append("          AND S4.MAJORCD          = S2.REMARK9 ");
        stb.append("          AND S4.EXAMCOURSECD     = S2.REMARK10 ");
        stb.append("          AND S4.TESTSUBCLASSCD   = '" + _param._testsubclasscd + "' ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE" + subclassCd + " ON RECEPT.ENTEXAMYEAR = SCORE" + subclassCd + ".ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = SCORE" + subclassCd + ".APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = SCORE" + subclassCd + ".TESTDIV ");
            stb.append("          AND RECEPT.EXAM_TYPE = SCORE" + subclassCd + ".EXAM_TYPE ");
            stb.append("          AND RECEPT.RECEPTNO = SCORE" + subclassCd + ".RECEPTNO ");
            stb.append("          AND SCORE" + subclassCd + ".TESTSUBCLASSCD = '" + subclassCd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VBASE.EXAMNO BETWEEN '" + _param._sreceptno + "' AND '" + _param._ereceptno + "' ");
        if ("1".equals(_param._specialreasondiv)) {
        	stb.append("         AND VBASE.EXAMNO IN (");
        	stb.append("               SELECT ");
        	stb.append("                   W1.RECEPTNO ");
        	stb.append("               FROM ");
        	stb.append("                   ENTEXAM_RECEPT_DAT W1 ");
        	stb.append("                   INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ");
        	stb.append("                         ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ");
        	stb.append("                        AND W2.EXAMNO = W1.EXAMNO ");
        	stb.append("                        AND W2.SPECIAL_REASON_DIV IS NOT NULL ");
        	stb.append("               WHERE ");
        	stb.append("                       W1.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        	stb.append("                   AND W1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        	stb.append("                   AND W1.TESTDIV      = '" + _param._testDiv + "' ");
        	stb.append("             )");
        }

        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAM_TYPE, ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59140 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;
        final Map _subclassMap;
        final boolean _isKeiai;
    	final boolean _callchk050gflg;
        final String _testsubclasscd;
        final String _examhallcd;
        final String _sreceptno;
        final String _ereceptno;
        final String _specialreasondiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
            _subclassMap = getSubclassList(db2);
            final String z010Name2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z010", "00"));
            _isKeiai = "30270254001".equals(z010Name2);

            _callchk050gflg = "TRUE".equals(request.getParameter("HID_ISCALL050G")) ? true : false;
        	_testsubclasscd = StringUtils.defaultString(request.getParameter("TESTSUBCLASSCD"));
        	_examhallcd = StringUtils.defaultString(request.getParameter("EXAMHALLCD"));
        	_sreceptno = StringUtils.defaultString(request.getParameter("S_RECEPTNO"));
        	_ereceptno = StringUtils.defaultString(request.getParameter("E_RECEPTNO"));
        	_specialreasondiv = StringUtils.defaultString(request.getParameter("SPECIAL_REASON_DIV"));
        }

        private Map getSubclassList(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ORDER BY NAMECD2 ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof


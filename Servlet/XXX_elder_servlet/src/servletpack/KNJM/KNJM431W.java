/*
 * $Id: d1912d326d02f4fe14d6bbfb7506ec0480b06204 $
 *
 * 作成日: 2018/08/08
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJM431W {

    private static final Log log = LogFactory.getLog(KNJM431W.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String formname = "KNJM431W.frm";
    	svf.VrSetForm(formname, 4);

    	PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = db2.prepareStatement(prestatementReportHead());
            rs = ps1.executeQuery();
            int lineCnt = 1;
            int maxLineCnt = 2;
            while (rs.next()) {
            	//出力件数を超えたら、改ページ
                if (lineCnt > maxLineCnt) {
                    svf.VrEndPage();
                	svf.VrSetForm(formname, 4);
                    lineCnt = 1;
                }
                //タイトル出力
                svf.VrsOut("ADDR1", rs.getString("ADDR1"));
                if ("1".equals(rs.getString("ADDR_FLG"))) {
                    svf.VrsOut("ADDR2", rs.getString("ADDR2"));
                }
                svf.VrsOut("ADDR_NAME", rs.getString("NAME_SHOW"));
            	String titlestr = "テスト成績通知表";
            	svf.VrsOut("TITLE", titlestr);

                //学籍番号
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                //年
                svf.VrsOut("GRADE", String.valueOf(rs.getInt("GRADE_CD")));
                //組
                svf.VrsOut("HR_CLASS", rs.getString("HR_NAME"));
                //番
                svf.VrsOut("ATTENDNO", String.valueOf(rs.getInt("ATTENDNO")));

                //名前
                final String namewk = rs.getString("NAME_SHOW");
                svf.VrsOut("NAME", namewk);

                //講座
                svf.VrsOut("CHAIRNAME", rs.getString("CHAIRNAME"));
                //テスト種別
                svf.VrsOut("TESTNAME", (String)_param._testTypeMap.get(_param._testCd));
                //履修期
                svf.VrsOut("TAKESEMES", (String)_param._semesterMap.get(rs.getString("TAKESEMES")));
                //回数
                svf.VrsOut("SEQ", _param._seq);
                //受験日

                svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._testdate, "/", "-")));
                //素点
                svf.VrsOut("SCORE", rs.getString("SCORE"));
                //合否
                svf.VrsOut("GOUHI", rs.getString("RESULT"));
                //連絡事項
                svf.VrsOut("REMARK", rs.getString("REMARK1"));

                svf.VrEndRecord();
                lineCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    }

    /**
     *  SQL-STATEMENT作成 レポート課題集
     *
     */
    private String prestatementReportHead() {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH GETADDR AS (");
        stb.append("  SELECT ");
        stb.append("    T9.SCHREGNO, ");
        stb.append("    MAX(T9.ISSUEDATE) AS MAXDAT ");
        stb.append("  FROM ");
        stb.append("    SCHREG_ADDRESS_DAT T9 ");
        stb.append("  WHERE");
        stb.append("    T9.SCHREGNO = '" + _param._schregNo + "' ");
        stb.append("  GROUP BY ");
        stb.append("    T9.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T9.ZIPCD,");
        stb.append("     T9.ADDR1, ");
        stb.append("     T9.ADDR2, ");
        stb.append("     T9.ADDR_FLG, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T4.GRADE,");
        stb.append("     T5.GRADE_CD,");
        stb.append("     T4.HR_CLASS, ");
        stb.append("     T8.HR_NAME, ");
        stb.append("     T4.ATTENDNO, ");
        stb.append("     T3.NAME_SHOW, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     T6.SCORE, ");
        stb.append("     CASE WHEN T6.SCORE > 30 THEN '合格' ELSE '不合格' END AS RESULT, ");
        stb.append("     T7.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("       AND T4.YEAR = '" + _param._year + "' ");
        stb.append("       AND T4.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T4.YEAR ");
        stb.append("       AND T5.GRADE = T4.GRADE ");
        stb.append("     LEFT JOIN RECORD_SCORE_HIST_DAT T6 ON T6.YEAR = T4.YEAR ");
        stb.append("       AND T6.SCHREGNO = T4.SCHREGNO ");
        stb.append("       AND T6.SEQ = " + _param._seq + " ");
        stb.append("       AND T6.SEMESTER || '-' || T6.TESTKINDCD || '-' || T6.TESTITEMCD || '-' || T6.SCORE_DIV = '" + _param._testCd + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || T6.SUBCLASSCD = '" + _param._subclassCd + "' ");
        } else {
            stb.append("       AND T6.SUBCLASSCD = '" + _param._subclassCd + "' ");
        }
        stb.append("     LEFT JOIN RECORD_SCORE_HIST_DETAIL_DAT T7 ON T7.YEAR = T6.YEAR ");
        stb.append("       AND T7.SCHREGNO   = T6.SCHREGNO ");
        stb.append("       AND T7.SEMESTER   = T6.SEMESTER ");
        stb.append("       AND T7.TESTKINDCD = T6.TESTKINDCD ");
        stb.append("       AND T7.TESTITEMCD = T6.TESTITEMCD ");
        stb.append("       AND T7.SCORE_DIV  = T6.SCORE_DIV ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T7.CLASSCD = T6.CLASSCD ");
            stb.append("       AND T7.SCHOOL_KIND = T6.SCHOOL_KIND ");
            stb.append("       AND T7.CURRICULUM_CD = T6.CURRICULUM_CD ");
        }
        stb.append("       AND T7.SUBCLASSCD = T6.SUBCLASSCD ");
        stb.append("       AND T7.DSEQ = '002' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T8 ON T8.YEAR = T4.YEAR ");
        stb.append("       AND T8.SEMESTER = T4.SEMESTER ");
        stb.append("       AND T8.GRADE = T4.GRADE ");
        stb.append("       AND T8.HR_CLASS = T4.HR_CLASS ");
        stb.append("     LEFT JOIN GETADDR T9F ON T9F.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT T9 ON T9.SCHREGNO = T9F.SCHREGNO ");
        stb.append("       AND T9.ISSUEDATE = T9F.MAXDAT ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR     = T2.YEAR AND ");
        stb.append("     T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = T2.SEMESTER AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T1.CHAIRCD  = T2.CHAIRCD AND ");
        stb.append("     T2.SCHREGNO = " + _param._schregNo + " AND ");
        stb.append("     EXISTS (SELECT ");
        stb.append("                 'X' ");
        stb.append("             FROM ");
        stb.append("                 SUBCLASS_STD_PASS_SDIV_DAT S1 ");
        stb.append("             WHERE ");
        stb.append("                 T1.YEAR         = S1.YEAR AND ");
        stb.append("                 T1.SEMESTER     = S1.SEMESTER AND ");
        stb.append("                 S1.SEMESTER || '-' || S1.TESTKINDCD || '-' ||  S1.TESTITEMCD || '-' ||  S1.SCORE_DIV = '" + _param._testCd + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 T1.CLASSCD       = S1.CLASSCD AND ");
            stb.append("                 T1.SCHOOL_KIND   = S1.SCHOOL_KIND AND ");
            stb.append("                 T1.CURRICULUM_CD = S1.CURRICULUM_CD AND ");
        }
        stb.append("                 T1.SUBCLASSCD   = S1.SUBCLASSCD AND ");
        stb.append("                 T2.SCHREGNO     = S1.SCHREGNO AND ");
        stb.append("                 S1.SEM_PASS_FLG = '1' ) AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
        } else {
            stb.append("     T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.SCHREGNO ");

        log.debug("sql = " + stb.toString());
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61713 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _schregNo;
        private final String _testCd;
        private final String _subclassCd;
        private final String _seq;
        private final String _testdate;

        private final String _useCurriculumcd;
        private String _nendo;

        private Map _semesterMap;
        private Map _testTypeMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _schregNo = request.getParameter("SCHREGNO");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _testCd = request.getParameter("TESTCD");
            _seq = request.getParameter("SEQ");
            _testdate = request.getParameter("TEST_DATE");

            _useCurriculumcd = request.getParameter("USE_CURRICULUMCD");

            _semesterMap = setSemesterMap(db2);
            _testTypeMap = setTestTypeMap(db2);

            //年度
            try{
                //_nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度 (" + _year + ")";
            	_nendo = _year + "年度";
            } catch( Exception ex ){
                log.error("setHead error!" + ex);
            }

            if (_nendo == null) {
                _nendo = "";
            }
        }

        private Map setSemesterMap(final DB2UDB db2) {
        	Map retMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT DISTINCT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' and SEMESTER <> '9' "), "SEMESTER", "SEMESTERNAME");
        	retMap.put("0", "通年");
        	return retMap;
        }
        private Map setTestTypeMap(final DB2UDB db2) {
        	StringBuffer stb = new StringBuffer();

        	stb.append(" SELECT ");
        	stb.append(" T2.SEMESTER || '-' || T2.TESTKINDCD || '-' ||  T2.TESTITEMCD || '-' ||  T2.SCORE_DIV AS Key, ");
        	stb.append(" T2.TESTITEMNAME ");
        	stb.append(" FROM ");
        	stb.append("   NAME_MST T1 ");
        	stb.append("   INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
        	stb.append("     ON T2.YEAR = '" + _year + "' ");
        	stb.append("    AND T2.SEMESTER = '" + _semester + "' ");
        	stb.append("    AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ");
        	stb.append("    AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV  = '" + _testCd + "'");
        	stb.append(" WHERE ");
        	stb.append("   NAMECD1 = 'M002' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ");

        	log.debug("testtypesql = " + stb.toString());
        	Map retMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "Key", "TESTITEMNAME");
        	return retMap;
        }
    }
}

// eof


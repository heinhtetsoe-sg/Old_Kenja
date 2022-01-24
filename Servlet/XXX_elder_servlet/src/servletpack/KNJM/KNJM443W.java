/*
 * $Id: 697ca04eb5e645fd00f37ab018df82fa098ed598 $
 *
 * 作成日: 2020/01/20
 * 作成者: matsushima
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJM443W {

    private static final Log log = LogFactory.getLog(KNJM443W.class);

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
        String formname = "KNJM443W.frm";
    	svf.VrSetForm(formname, 1);

    	PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = db2.prepareStatement(prestatementReportHead());
            rs = ps1.executeQuery();
            int lineCnt = 1;
            int maxLineCnt = 2;
            int copyCnt = 2;
            while (rs.next()) {
                //出力件数を超えたら、改ページ
                if (lineCnt > maxLineCnt) {
                    svf.VrEndPage();
                    svf.VrSetForm(formname, 1);
                    lineCnt = 1;
                }

                for(int idx = 1; idx <= copyCnt; idx++) {
                    //クラス
                    svf.VrsOutn("HR_NAME" + idx, lineCnt, rs.getString("HR_NAME"));

                    //学籍番号
                    svf.VrsOutn("SCHREGNO" + idx, lineCnt, rs.getString("SCHREGNO"));

                    //名前
                    final String namewk = rs.getString("NAME_SHOW");
                    int namelen = KNJ_EditEdit.getMS932ByteLength(namewk);
                    String namefield = namelen > 30 ? "_3" : namelen > 22 ? "_2" : "_1";
                    svf.VrsOutn("NAME" + idx + namefield, lineCnt, namewk);

                    //年度
                    svf.VrsOutn("NENDO" + idx, lineCnt, _param._nendo);

                    //考査名称
                    svf.VrsOutn("TEST_DIV" + idx, lineCnt, (String)_param._testTypeMap.get(_param._testCd));

                    //回数
                    svf.VrsOutn("NUM" + idx, lineCnt, _param._testCount + "回目");

                    //科目
                    final String subclasswk = rs.getString("SUBCLASSNAME");
                    int subclasslen = KNJ_EditEdit.getMS932ByteLength(subclasswk);
                    String subclassfield = "1";
                    if (idx == 1) {
                        subclassfield = subclasslen > 26 ? "2" : "1";
                    }else {
                        subclassfield = subclasslen > 20 ?  "2_3" : subclasslen > 12 ? "2_2" : "2_1";
                    }
                    svf.VrsOutn("SUBCLASS_NAME" + subclassfield, lineCnt, subclasswk);


                    //素点
                    svf.VrsOutn("SCORE" + idx, lineCnt, rs.getString("SCORE"));
                }
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

        stb.append(" SELECT ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T3.NAME_SHOW, ");
        stb.append("     T6.SUBCLASSNAME, ");
        stb.append("     SCORE.SCORE ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T2 ");
        stb.append("            ON T2.YEAR     = T1.YEAR ");
        stb.append("           AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("           AND T2.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     INNER JOIN RECORD_SCORE_HIST_DAT SCORE ");
        stb.append("            ON SCORE.YEAR       = T2.YEAR ");
        stb.append("           AND SCORE.SEMESTER   = T2.SEMESTER ");
        stb.append("           AND SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + _param._testCd + "' ");
        stb.append("           AND SCORE.SCHREGNO   = T2.SCHREGNO ");
        stb.append("           AND SCORE.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND SCORE.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND SCORE.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND SCORE.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND SCORE.SEQ = '" + _param._testCount + "' ");
        stb.append("           AND SCORE.SCORE < " + _param._goukakuten + " "); //不合格者
        stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T4 ");
        stb.append("            ON T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("           AND T4.YEAR     = T2.YEAR ");
        stb.append("           AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("            ON T5.YEAR     = T4.YEAR ");
        stb.append("           AND T5.SEMESTER = T4.SEMESTER ");
        stb.append("           AND T5.GRADE    = T4.GRADE ");
        stb.append("           AND T5.HR_CLASS = T4.HR_CLASS ");
        stb.append("     LEFT JOIN SUBCLASS_MST T6 ");
        stb.append("            ON T6.SUBCLASSCD    = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T6.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND T6.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     EXISTS (SELECT ");
        stb.append("                 'X' ");
        stb.append("             FROM ");
        stb.append("                 SUBCLASS_STD_PASS_SDIV_DAT S1 ");
        stb.append("             WHERE ");
        stb.append("                 T1.YEAR         = S1.YEAR AND ");
        stb.append("                 T1.SEMESTER     = S1.SEMESTER AND ");
        stb.append("                 S1.TESTKINDCD || S1.TESTITEMCD || S1.SCORE_DIV = '" + _param._testCd + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                 T1.CLASSCD       = S1.CLASSCD AND ");
            stb.append("                 T1.SCHOOL_KIND   = S1.SCHOOL_KIND AND ");
            stb.append("                 T1.CURRICULUM_CD = S1.CURRICULUM_CD AND ");
        }
        stb.append("                 T1.SUBCLASSCD   = S1.SUBCLASSCD AND ");
        stb.append("                 T2.SCHREGNO     = S1.SCHREGNO AND ");
        stb.append("                 S1.SEM_PASS_FLG = '1' ) AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclass + "' ");
        } else {
            stb.append("     T1.SUBCLASSCD = '" + _param._subclass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.SCHREGNO ");
        log.debug("sql = " + stb.toString());
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75469 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _testCd;
        private final String _testCount;
        private final String _subclass;
        private final String _useCurriculumcd;
        private final int _goukakuten;

        private String _nendo;
        private Map _semesterMap;
        private Map _testTypeMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _testCount = request.getParameter("TEST_COUNT");
            _subclass = request.getParameter("SUBCLASS");
            _useCurriculumcd = request.getParameter("USE_CURRICULUMCD");
            
            _goukakuten = Integer.parseInt(StringUtils.defaultString(KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' and NAMECD1 = 'M028' ORDER BY NAMECD2 ")), "NAME1"), "30"));

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
        	stb.append(" T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS Key, ");
        	stb.append(" T2.TESTITEMNAME ");
        	stb.append(" FROM ");
        	stb.append("   NAME_MST T1 ");
        	stb.append("   INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
        	stb.append("     ON T2.YEAR = '" + _year + "' ");
        	stb.append("    AND T2.SEMESTER = '" + _semester + "' ");
        	stb.append("    AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ");
        	stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _testCd + "'");
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


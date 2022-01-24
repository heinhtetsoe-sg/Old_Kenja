/*
 * $Id: 14afd760a9de1751608b50052bf3f29d80b31ce2 $
 *
 * 作成日: 2018/08/07
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJM441W {

    private static final Log log = LogFactory.getLog(KNJM441W.class);

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
        String formname = "KNJM441W.frm";
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
                String titlestr = _param._nendo + "　" + (String)_param._testTypeMap.get(_param._testCd);
                svf.VrsOut("TITLE", titlestr);

                //登録番号
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                //名前
                final String namewk = rs.getString("NAME_SHOW");
                int namelen = KNJ_EditEdit.getMS932ByteLength(namewk);
                String namefield = namelen > 30 ? "NAME_2" : "NAME";
                svf.VrsOut(namefield, namewk);
                //クラス
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                //科目
                svf.VrsOut("SUBCLASS_NAME", rs.getString("SUBCLASSNAME"));
//                //講座
//                svf.VrsOut("CHAIRNAME", rs.getString("CHAIRNAME"));
//                //テスト種別
//                svf.VrsOut("TESTNAME", (String)_param._testTypeMap.get(_param._testCd));
//                //履修期
//                svf.VrsOut("TAKESEMES", (String)_param._semesterMap.get(rs.getString("TAKESEMES")));
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
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T3.NAME_SHOW, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     T6.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T2 ");
        stb.append("            ON T2.YEAR     = T1.YEAR ");
        stb.append("           AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("           AND T2.CHAIRCD  = T1.CHAIRCD ");
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
        log.fatal("$Revision: 71878 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _testCd;
        private final String _subclass;
        private final String[] _selectdata;

        private final String _useCurriculumcd;
        private String _nendo;
        String _schregInstate;

        private Map _semesterMap;
        private Map _testTypeMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _subclass = request.getParameter("SUBCLASS");
            _useCurriculumcd = request.getParameter("USE_CURRICULUMCD");
            _selectdata = request.getParameterValues("CATEGORY_SELECTED");

            _schregInstate = "";
            String sep = "";
            for (int i = 0; i < _selectdata.length; i++) {
                final String selectVal = _selectdata[i];
                _schregInstate += sep + "'" + selectVal + "'";
                sep = ",";
            }
            _schregInstate = "(" + _schregInstate + ")";

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


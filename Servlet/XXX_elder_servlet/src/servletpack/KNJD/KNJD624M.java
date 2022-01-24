// kanji=漢字
/*
 * $Id: d2193e8843559395b08775145455876ae83afcf4 $
 *
 * 作成日: 2009/06/18 15:47:43 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: d2193e8843559395b08775145455876ae83afcf4 $
 */
public class KNJD624M {

    private static final Log log = LogFactory.getLog("KNJD624M.class");
    private static final String FORM_NAME  = "KNJD624M.frm";
    private static final String SUBCLASSALL = "999999";

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request);

            _param.load(db2);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    /**
     * 印刷処理（メイン）
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm(FORM_NAME, 1);
        printHeader(svf);
        printScoreCnt(svf);
        svf.VrEndPage();
        _hasData = true;
    }

    /**
     * ヘッダ
     */
    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("GRADE", _param._gradeName);
        svf.VrsOut("PRINTDAY", _param._printDate);
    }

    /**
     * 成績人数
     */
    private void printScoreCnt(final Vrw32alp svf) {
        int gyo = 0;
        String tmpSubcd = "";
        for (final Iterator iter = _param._scoreCntList.iterator(); iter.hasNext();) {
            final ScoreCnt scoreCnt = (ScoreCnt) iter.next();
            if (scoreCnt._subclassCd.endsWith(SUBCLASSALL)) {
                //平均
                svf.VrsOut("TOTAL" + scoreCnt._score,  scoreCnt._cnt);
            } else {
                //成績
                if (!tmpSubcd.equals(scoreCnt._subclassCd)) gyo++;
                svf.VrsOutn(getFieldName(scoreCnt._subclassName),  gyo,  scoreCnt._subclassName);
                svf.VrsOutn("SCORE" + scoreCnt._score,  gyo,  scoreCnt._cnt);
                tmpSubcd = scoreCnt._subclassCd;
            }
        }
    }

    /**
     * 科目名
     * ・１０文字用と２０文字用のフィールドに対応。
     */
    private String getFieldName(final String str) {
        return str != null && 10 < str.length() ? "SUBJECT2" : "SUBJECT1";
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");
        
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
    
    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request) throws Exception {
        final Param param = new Param(request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _grade;
        private final String _testKindCd;
        private final String _semester;
        private final String _ctrlSemester;
        private final String _schregSeme;
        private final String _loginDate;
        private final String _printDate;
        private final String _useCurriculumcd;

        private String _semesterName;
        private String _gradeName;
        
        private List _scoreCntList = new ArrayList();

        Param(final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _testKindCd = request.getParameter("TESTKINDCD");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schregSeme = "9".equals(_semester) ? _ctrlSemester : _semester;
            _loginDate = request.getParameter("LOGIN_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _printDate = KNJ_EditDate.h_format_JP(_loginDate);
        }

        /**
         * ヘッダ情報
         */
        private void load(final DB2UDB db2) throws SQLException, ParseException {
            loadSemesterName(db2);
            loadGradeName(db2);
            setScoreCnt(db2);
        }

        /**
         * 学期名の取得
         */
        private void loadSemesterName(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST " +
                        "WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 学年名の取得
         */
        private void loadGradeName(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT " +
                        "WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 成績人数
         */
        private void setScoreCnt(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getScoreCntSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final ScoreCnt scoreCnt = new ScoreCnt(
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("SCORE"),
                            rs.getString("CNT")
                    );
                    _scoreCntList.add(scoreCnt);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 成績人数
         */
        private String getScoreCntSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T1.SCORE, ");
            stb.append("     COUNT(T1.SCHREGNO) AS CNT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                  AND T2.YEAR = '" + _year + "' ");
            stb.append("                                  AND T2.SEMESTER = '" + _schregSeme + "' ");
            stb.append("                                  AND T2.GRADE = '" + _grade + "' ");
            stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("           AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("           AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' AND ");
            stb.append("     T1.SEMESTER = '" + _semester + "' AND ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD = '" + _testKindCd + "' AND ");
            stb.append("     T1.SUBCLASSCD NOT LIKE '9%' AND ");
            stb.append("     3 <= T1.SCORE AND T1.SCORE <= 10 ");
            stb.append(" GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T1.SCORE ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     '平均' AS SUBCLASSNAME, ");
            stb.append("     case when smallint(T1.AVG) < 6 then 5 ");
            stb.append("          when 9 <= smallint(T1.AVG) then 9 ");
            stb.append("          else smallint(T1.AVG) end AS SCORE, ");
            stb.append("     COUNT(T1.SCHREGNO) AS CNT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                  AND T2.YEAR = '" + _year + "' ");
            stb.append("                                  AND T2.SEMESTER = '" + _schregSeme + "' ");
            stb.append("                                  AND T2.GRADE = '" + _grade + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' AND ");
            stb.append("     T1.SEMESTER = '" + _semester + "' AND ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD = '" + _testKindCd + "' AND ");
            stb.append("     T1.SUBCLASSCD = '" + SUBCLASSALL + "' AND ");
            stb.append("     3 <= T1.AVG AND T1.AVG <= 10 ");
            stb.append(" GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     case when smallint(T1.AVG) < 6 then 5 ");
            stb.append("          when 9 <= smallint(T1.AVG) then 9 ");
            stb.append("          else smallint(T1.AVG) end ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     SCORE ");
            return stb.toString();
        }
    }

    /**
     * 成績人数の内部クラス
     */
    private class ScoreCnt {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _score;
        private final String _cnt;

        private ScoreCnt(
                final String subclassCd,
                final String subclassName,
                final String score,
                final String cnt
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _score = score;
            _cnt = cnt;
        }

        public String toString() {
            return "科目：" + _subclassCd + "：" + _subclassName;
        }
    }
}

// eof

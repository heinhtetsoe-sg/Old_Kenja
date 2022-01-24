// kanji=漢字
/*
 * $Id: 1e1946313c1a6d9738d20dff328e815eddedf31b $
 *
 * 作成日: 2011/11/29 13:30:19 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
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
 * @version $Id: 1e1946313c1a6d9738d20dff328e815eddedf31b $
 */
public class KNJD195 {

    private static final Log log = LogFactory.getLog("KNJD195.class");

    private boolean _hasData;

    Param _param;

    private static final String CHAIRALL = "9999999";

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

            _param = createParam(db2, request);

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

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJD195.frm", 4);
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("TESTNAME", _param._testName);
        svf.VrsOut("DATE", _param.getLoginDateString());
        svf.VrsOut("GRADE", _param._gradename);
        final List recAvgList = getRecordAverageList(db2);
        for (final Iterator it = recAvgList.iterator(); it.hasNext();) {
            final RecordAverage recAvg = (RecordAverage) it.next();

            svf.VrsOut("SUBCLASSCD", recAvg._subclassCd);
            svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteCount(recAvg._subclassName) > 26 ? "2" : ""), recAvg._subclassName);
            svf.VrsOut("CHAIRCD", CHAIRALL.equals(recAvg._chairCd) ? "" : recAvg._chairCd);
            svf.VrsOut("CHAIR_NAME" + (getMS932ByteCount(recAvg._chairName) > 26 ? "2" : ""), CHAIRALL.equals(recAvg._chairCd) ? "計" : recAvg._chairName);
            svf.VrsOut("TEACHER_NAME", CHAIRALL.equals(recAvg._chairCd) ? "" : recAvg._staffName);
            svf.VrsOut("SUM", recAvg._count);
            svf.VrsOut("AVERAGE", recAvg._avg);

            svf.VrEndRecord();
            _hasData = true;
        }
    }
    
    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getRecordAverageList(final DB2UDB db2) throws SQLException  {
        final List rtnList = new ArrayList();
        ResultSet rs = null;
        try {
            final String sql = getRecordAverageSql();
            log.debug("getRecordAverageSql = " + sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                RecordAverage recAvg = new RecordAverage(rs.getString("SUBCLASSCD"),
                                                         rs.getString("SUBCLASSNAME"),
                                                         rs.getString("CHAIRCD"),
                                                         rs.getString("CHAIRNAME"),
                                                         rs.getString("STAFFNAME"),
                                                         rs.getString("COUNT"),
                                                         rs.getString("AVG"));
                rtnList.add(recAvg);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnList;
    }

    private String getRecordAverageSql() {
        final StringBuffer stb = new StringBuffer();
        final String tableName = "2".equals(_param._tableDiv) ? "V_DAT" : "DAT";

        stb.append(" WITH T_REC_AVG AS ( ");
        stb.append("     SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         CHAIRCD, ");
        stb.append("         COUNT, ");
        stb.append("         DECIMAL(ROUND(AVG*10,0)/10,5,1) AS AVG ");
        stb.append("     FROM ");
        stb.append("         RECORD_AVERAGE_CHAIR_" + tableName + " ");
        stb.append("     WHERE ");
        stb.append("             YEAR       = '" + _param._year + "' ");
        stb.append("         AND SEMESTER   = '" + _param._semester + "' ");
        stb.append("         AND TESTKINDCD || TESTITEMCD = '" + _param._kindItem + "' ");
        stb.append("         AND AVG_DIV    = '1' ");
        stb.append("         AND GRADE      = '" + _param._grade + "' ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, ");
        stb.append("         '" + CHAIRALL + "' AS CHAIRCD, ");
        stb.append("         COUNT, ");
        stb.append("         DECIMAL(ROUND(AVG*10,0)/10,5,1) AS AVG ");
        stb.append("     FROM ");
        stb.append("         RECORD_AVERAGE_" + tableName + " ");
        stb.append("     WHERE ");
        stb.append("             YEAR       = '" + _param._year + "' ");
        stb.append("         AND SEMESTER   = '" + _param._semester + "' ");
        stb.append("         AND TESTKINDCD || TESTITEMCD = '" + _param._kindItem + "' ");
        stb.append("         AND AVG_DIV    = '1' ");
        stb.append("         AND GRADE      = '" + _param._grade + "' ");
        stb.append("     ) ");
        stb.append(" , T_CHAIR_STAFF AS ( ");
        stb.append("     SELECT ");
        stb.append("         CHAIRCD, ");
        stb.append("         MIN(STAFFCD) AS STAFFCD ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STF_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("         AND SEMESTER = '" + _param._chairSemester + "' ");
        stb.append("         AND CHARGEDIV = 1 "); //1:正担任
        stb.append("     GROUP BY ");
        stb.append("         CHAIRCD ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L2.CHAIRNAME, ");
        stb.append("     L3.STAFFCD, ");
        stb.append("     L4.STAFFNAME, ");
        stb.append("     T1.COUNT, ");
        stb.append("     T1.AVG ");
        stb.append(" FROM ");
        stb.append("     T_REC_AVG T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                       AND L1.CLASSCD  = T1.CLASSCD ");
            stb.append("                       AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ");
            stb.append("                       AND L1.CURRICULUM_CD  = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN CHAIR_DAT L2 ON L2.YEAR     = '" + _param._year + "' ");
        stb.append("                           AND L2.SEMESTER = '" + _param._chairSemester + "' ");
        stb.append("                           AND L2.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     LEFT JOIN T_CHAIR_STAFF L3 ON L3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = L3.STAFFCD ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD ");

        return stb.toString();
    }

    private class RecordAverage {
        final String _subclassCd;
        final String _subclassName;
        final String _chairCd;
        final String _chairName;
        final String _staffName;
        final String _count;
        final String _avg;

        RecordAverage(
                final String subclassCd,
                final String subclassName,
                final String chairCd,
                final String chairName,
                final String staffName,
                final String count,
                final String avg
                ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _chairCd = chairCd;
            _chairName = chairName;
            _staffName = staffName;
            _count = count;
            _avg = avg;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _kindItem;
        private final String _grade;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _chairSemester;
        private final String _scoreFlg;
        private final String _useCurriculumcd;

        private final boolean _seirekiFlg;
        private final String _testName;
        private final String _semesterName;
        private final String _gradename;
        private String _tableDiv;
        String _z010 = "";
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _kindItem = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _chairSemester = "9".equals(_semester) ? _ctrlSemester : _semester;
            _scoreFlg = request.getParameter("SCORE_FLG");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _seirekiFlg = getSeirekiFlg(db2);
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _gradename = getGradename(db2, _year, _grade);
            _z010 = setNameMst(db2, "Z010", "00");
            setTableDiv();
        }
        
        private String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        String getLoginDateString() {
            return getDateString(_ctrlDate);
        }

        String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat ) ;        
            }
            return null;
        }
        
        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        /**
         * 席次テーブル
         * @return 1:DAT, 2:V_DAT 
         */
        public void setTableDiv() {
            if (isTottori() && (("9900".equals(_kindItem) && "1".equals(_scoreFlg)) || (!"9900".equals(_kindItem) && "2".equals(_scoreFlg)))) {
                _tableDiv = "2";
            } else {
                _tableDiv = "1";
            }
            log.fatal("序列テーブル：" + ("2".equals(_tableDiv) ? "V_DAT" : "DAT"));
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }
        
        private boolean isTottori() {
            return "tottori".equals(_z010);
        }

        private String getGradename(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String gradename = null;
            try {
                String sql = "SELECT GRADE, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradename = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradename;
        }
    }
}

// eof

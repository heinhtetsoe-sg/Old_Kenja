// kanji=漢字
/*
 * $Id: 927deabf7d8cf35d19c4568ff9eaccfd6240dcbc $
 *
 * 作成日: 2012/03/02 16:45:58 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
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
 * @author m-yama
 * @version $Id: 927deabf7d8cf35d19c4568ff9eaccfd6240dcbc $
 */
public class KNJH538B {

    private static final Log log = LogFactory.getLog("KNJH538B.class");

    private static final String FORM_FILE = "KNJH538B.frm";
    private boolean _hasData;
    private static final int MAX_LINE = 50;

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
        final List printList = getPrintList(db2);
        int totalScore = 0;
        int totalCnt = 0;

        svf.VrSetForm(FORM_FILE, 4);
        printHeader(db2, svf);
        int printCnt = 1;
        for (final Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            final PrintData printData = (PrintData) itPrint.next();
            if (null != printData._score && !"*".equals(printData._score)) {
                totalScore += Integer.parseInt(printData._score);
                totalCnt++;
            }

            String fieldNo = "";
            if (printCnt % 5 == 0) {
                fieldNo = "5";
            }
            /* 出席番号 */
            svf.VrsOut("ATTENDNO" + fieldNo, printData._attendNo);
            /* 学生番号 */
            svf.VrsOut("SCHREGNO" + fieldNo, printData._schregNo);
            /* 氏名 */
            svf.VrsOut("NAME" + fieldNo, printData._name);
            /* 得点 */
            svf.VrsOut("SCORE" + fieldNo, printData._setScore);
            /* 席次 */
            svf.VrsOut("RANK" + fieldNo, printData._rank);
            /* 偏差値 */
            svf.VrsOut("DEVIATION" + fieldNo, printData._deviation);

            if (printCnt >= MAX_LINE) {
                printCnt = 1;
            }

            svf.VrEndRecord();
            printCnt++;
            _hasData = true;
        }

        double scale1 = 0;
        if (totalCnt > 0) {
            double avgDb = (double) totalScore / (double) totalCnt;
            BigDecimal avgBd = new BigDecimal(String.valueOf(avgDb));
            scale1 = avgBd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            printFooter(svf, totalScore, totalCnt, scale1, printCnt);
        }
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String printSql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String score = rs.getString("SCORE");
                final String rank = rs.getString("RANK");
                final String deviation = rs.getString("DEVIATION");
                final String scoreDi = rs.getString("SCORE_DI");
                final String setScore = null == score ? "" : score;
                final PrintData printData = new PrintData(schregNo, hrName, attendNo, name, score, rank, deviation, scoreDi, setScore);
                retList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE, ");
        stb.append("     L4.GROUP_CD, ");
        stb.append("     L1.HR_NAME, ");
        stb.append("     L2.NAME, ");
        stb.append("     L3.SCORE, ");
        stb.append("     L3.SCORE_DI, ");
        stb.append("     L3.RANK, ");
        stb.append("     L3.DEVIATION ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN PROFICIENCY_DAT L3 ON L3.YEAR = T1.YEAR ");
        stb.append("           AND L3.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND L3.PROFICIENCYDIV = '" + _param._proficiencyDiv + "' ");
        stb.append("           AND L3.PROFICIENCYCD = '" + _param._proficiencyCd + "' ");
        stb.append("           AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND L3.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("           AND L3.SCORE_DI = '*' ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT L4 ON L4.YEAR = T1.YEAR ");
        stb.append("          AND L4.GRADE = T1.GRADE ");
        stb.append("          AND L4.COURSECD || L4.MAJORCD || L4.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHr + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf) {
        /* 年度 */
        svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度");
        /* テスト名称 */
        svf.VrsOut("MOCKNAME", _param._semesterName + "  " + _param._proficiencyName + "見込点票");
        /* 科目名称 */
        svf.VrsOut("SUBCLASS_ABBV", _param._proficiencySubclassName);
        /* クラス名称 */
        svf.VrsOut("HR_NAME", _param._hrData._hrName);
        /* 担当者名称 */
        String putStaff = "";
        String sep = "";
        for (final Iterator iter = _param._hrData._staffList.iterator(); iter.hasNext();) {
            final String staffName = (String) iter.next();
            putStaff = putStaff + sep + staffName;
            sep = ",";
        }
        svf.VrsOut("STAFF_NAME", putStaff);
        svf.VrsOut("STAFF_NAME2", _param._staffName);
        /* 作成日 */
        svf.VrsOut("DATE",   KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate) );
    }

    private void printFooter(
            final Vrw32alp svf,
            final int totalScore,
            final int totalCnt,
            final double scale1,
            final int printCnt
    ) {

        for (int j = printCnt + 1; j <= MAX_LINE; j++) {
            String fieldNo = "";
            if (j % 5 == 0) {
                fieldNo = "5";
            }
            svf.VrsOut("KARA" + fieldNo, "1");
            svf.VrEndRecord();
        }

        /* 合計 */
        svf.VrsOut("ITEMNAME", "合計");
        svf.VrsOut("ITEM_SCORE", String.valueOf(totalScore));
        svf.VrEndRecord();

        /* 人数 */
        svf.VrsOut("ITEMNAME", "人数");
        svf.VrsOut("ITEM_SCORE", String.valueOf(totalCnt));
        svf.VrEndRecord();

        /* 平均点 */
        svf.VrsOut("ITEMNAME", "平均点");
        svf.VrsOut("ITEM_SCORE", String.valueOf(scale1));
        svf.VrEndRecord();
    }

    private class PrintData {
        final String _schregNo;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _score;
        final String _rank;
        final String _deviation;
        final String _scoreDi;
        final String _setScore;
        public PrintData(
                final String schregNo,
                final String hrName,
                final String attendNo,
                final String name,
                final String score,
                final String rank,
                final String deviation,
                final String scoreDi,
                final String setScore
        ) {
            _schregNo   = schregNo;
            _hrName     = hrName;
            _attendNo   = attendNo;
            _name    = name;
            _score      = score;
            _rank       = rank;
            _deviation  = deviation;
            _scoreDi    = scoreDi;
            _setScore   = setScore;
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
        log.fatal("$Revision: 69424 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _proficiencySubclassCd;
        private final String _proficiencySubclassName;
        private final String _proficiencyDiv;
        private final String _proficiencyCd;
        private final String _proficiencyName;
        private final String _gradeHr;
        private final String _staffCd;
        private final String _staffName;
        private final HrData _hrData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemesterName(db2, _year, _semester);
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _proficiencySubclassCd = request.getParameter("PROFICIENCY_SUBCLASS_CD");
            _proficiencySubclassName = getSubclassName(db2, _proficiencySubclassCd);
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCY_TARGET");
            _proficiencyName = getProficiencyName(db2, _proficiencyDiv, _proficiencyCd);
            _gradeHr = request.getParameter("GRADE_HR_CLASS");
            _hrData = new HrData(db2, _year, _semester, _gradeHr);
            _staffCd = request.getParameter("STAFFCD");
            _staffName = getStaffName(db2, _staffCd);
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            String retSemesterName = "";
            final String subclassSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSemesterName = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemesterName;
        }

        private String getSubclassName(final DB2UDB db2, final String proficiencySubclassCd) throws SQLException {
            String retSubclassName = "";
            final String subclassSql = "SELECT SUBCLASS_NAME FROM PROFICIENCY_SUBCLASS_MST WHERE PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclassCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSubclassName = rs.getString("SUBCLASS_NAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSubclassName;
        }

        
        private String getProficiencyName(final DB2UDB db2, final String proficiencyDiv, final String proficiencyCd) throws SQLException {
            String retProficiencyName = "";
            final String proficiencySql = "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + proficiencyDiv + "' AND PROFICIENCYCD = '" + proficiencyCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(proficiencySql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retProficiencyName = rs.getString("PROFICIENCYNAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retProficiencyName;
        }
        private String getStaffName(final DB2UDB db2, final String staffCd) throws SQLException {
            String retStaff = "";
            final String staffSql = "SELECT STAFFNAME AS  STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + staffCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(staffSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStaff = rs.getString("STAFFNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStaff;
        }

    }

    private class HrData {
        final String _year;
        final String _semester;
        final String _gradeHr;
        final String _hrName;
        final List _staffList;

        public HrData(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String gradeHr
        ) throws SQLException {
            _gradeHr = gradeHr;
            _year = year;
            _semester = semester;
            _hrName = getHrName(db2, _year, _semester, gradeHr);
            _staffList = getHrStaff(db2, _year, _semester, gradeHr);
        }

        private String getHrName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String gradeHr
        ) throws SQLException {
            final String hrNameSql = "SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHr + "' ";
            String retHrName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hrNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retHrName = rs.getString("HR_NAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retHrName;
        }

        private List getHrStaff(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String gradeHr
        ) throws SQLException {
            final String hrStaffSql = getHrStaffSql(year, semester, gradeHr);
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hrStaffSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retList.add(rs.getString("STAFFNAME"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String getHrStaffSql(
                final String year,
                final String semester,
                final String gradeHr
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     '1' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.TR_CD1 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '2' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.TR_CD2 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '3' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.TR_CD3 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '4' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.SUBTR_CD1 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '5' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.SUBTR_CD2 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '6' AS ORDERCD, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN STAFF_MST L1 ON T1.SUBTR_CD3 = L1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
            stb.append(" ORDER BY ");
            stb.append("     ORDERCD ");

            return stb.toString();
        }
    }
}

// eof

// kanji=漢字
/*
 * $Id: 97528ba5eed285ded3649cb3a9def7e029bc41ed $
 *
 * 作成日: 2011/09/30 15:33:05 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 97528ba5eed285ded3649cb3a9def7e029bc41ed $
 */
public class KNJS310Nenkan1 {

    private static final Log log = LogFactory.getLog("KNJS310Nenkan1.class");

    private boolean _hasData;
    private final String SELECT_HR = "1";
    private final String UNIT_L = "1";
    private final String FRMID = "KNJS310_2.frm";

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public boolean svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws Exception {
        
        DB2UDB db2 = null;
        try {

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
        }

        return _hasData;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printList = getPrintList(db2);
        if (SELECT_HR.equals(_param._selectDiv)) {
            printHr(db2, svf, printList);
        } else {
            printStaff(db2, svf, printList);
        }
    }

    private void printHr(final DB2UDB db2, final Vrw32alp svf, final List printList) throws SQLException {
        svf.VrSetForm(FRMID, 4);
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final HrClass hrClass = (HrClass) iter.next();
            svf.VrsOut("NENDO", _param.getYear());
            svf.VrsOut("HR_NAME", hrClass._hrName);
            svf.VrsOut("TEACHER", hrClass._staffName);
            svf.VrsOut("ymd1", _param.getDate());
            for (final Iterator itSem = _param._semesterMap.keySet().iterator(); itSem.hasNext();) {
                final String semester = (String) itSem.next();
                final String semeName = (String) _param._semesterMap.get(semester);
                svf.VrsOut("SEMESTER" + semester, semeName);
            }
            svf.VrsOut("SEMESTER4", "年間");
            svf.VrsOut("SEMESTER5", "配当");
            svf.VrsOut("SEMESTER6", "過不足");
            for (final Iterator itPrint = hrClass._printMap.keySet().iterator(); itPrint.hasNext();) {
                final String subclassCd = (String) itPrint.next();
                final MeisaiMap meisaiMap = (MeisaiMap) hrClass._printMap.get(subclassCd);
                svf.VrsOut("CLASS", meisaiMap._title);

                for (final Iterator itMeisai = meisaiMap._monthData.keySet().iterator(); itMeisai.hasNext();) {
                    final String dataMonth = (String) itMeisai.next();
                    final MonthData monthData = (MonthData) meisaiMap._monthData.get(dataMonth);
                    svf.VrsOut("MONTH" + dataMonth, dataMonth + "月");
                    svf.VrsOut("MONTH" + dataMonth + "_TOTAL", String.valueOf(monthData._monthTotal));
                    int unitCnt = 1;
                    for (final Iterator itMonthData = monthData._unitList.iterator(); itMonthData.hasNext();) {
                        final UnitData unitData = (UnitData) itMonthData.next();
                        svf.VrsOut("MONTH" + dataMonth + "_LUNIT" + unitCnt, unitData._unitname);
                        svf.VrsOut("MONTH" + dataMonth + "_LESSON" + unitCnt, unitData._cnt);
                        unitCnt++;
                    }
                }
                int nenkan = 0;
                for (final Iterator itSemeTotal = meisaiMap._semeTotal.keySet().iterator(); itSemeTotal.hasNext();) {
                    final String semester = (String) itSemeTotal.next();
                    final Integer semeTotal = (Integer) meisaiMap._semeTotal.get(semester);
                    svf.VrsOut("LESSON" + semester, semeTotal.toString());
                    nenkan = nenkan + semeTotal.intValue();
                }
                svf.VrsOut("LESSON4", String.valueOf(nenkan));
                final String standardTime = getStandardTimeSql(db2, subclassCd, hrClass._grade);
                svf.VrsOut("LESSON5", standardTime);
                int sabun = nenkan - Integer.parseInt(standardTime);
                svf.VrsOut("LESSON6", String.valueOf(sabun));
                svf.VrEndRecord();
            }
            _hasData = true;
            svf.VrEndRecord();
        }
    }

    private void printStaff(final DB2UDB db2, final Vrw32alp svf, final List printList) throws SQLException {
        svf.VrSetForm(FRMID, 4);
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final SubclassData subclassData = (SubclassData) iter.next();
            svf.VrsOut("NENDO", _param.getYear());
            svf.VrsOut("HR_NAME", subclassData._subclassAbbv);
            svf.VrsOut("TEACHER", subclassData._staffName);
            svf.VrsOut("ymd1", _param.getDate());
            for (final Iterator itSem = _param._semesterMap.keySet().iterator(); itSem.hasNext();) {
                final String semester = (String) itSem.next();
                final String semeName = (String) _param._semesterMap.get(semester);
                svf.VrsOut("SEMESTER" + semester, semeName);
            }
            svf.VrsOut("SEMESTER4", "年間");
            svf.VrsOut("SEMESTER5", "配当");
            svf.VrsOut("SEMESTER6", "過不足");
            for (final Iterator itPrint = subclassData._printMap.keySet().iterator(); itPrint.hasNext();) {
                final String gradeHr = (String) itPrint.next();
                final MeisaiMap meisaiMap = (MeisaiMap) subclassData._printMap.get(gradeHr);
                svf.VrsOut("CLASS", meisaiMap._title);

                for (final Iterator itMeisai = meisaiMap._monthData.keySet().iterator(); itMeisai.hasNext();) {
                    final String dataMonth = (String) itMeisai.next();
                    final MonthData monthData = (MonthData) meisaiMap._monthData.get(dataMonth);
                    svf.VrsOut("MONTH" + dataMonth, dataMonth + "月");
                    svf.VrsOut("MONTH" + dataMonth + "_TOTAL", String.valueOf(monthData._monthTotal));
                    int unitCnt = 1;
                    for (final Iterator itMonthData = monthData._unitList.iterator(); itMonthData.hasNext();) {
                        final UnitData unitData = (UnitData) itMonthData.next();
                        svf.VrsOut("MONTH" + dataMonth + "_LUNIT" + unitCnt, unitData._unitname);
                        svf.VrsOut("MONTH" + dataMonth + "_LESSON" + unitCnt, unitData._cnt);
                        unitCnt++;
                    }
                }
                int nenkan = 0;
                for (final Iterator itSemeTotal = meisaiMap._semeTotal.keySet().iterator(); itSemeTotal.hasNext();) {
                    final String semester = (String) itSemeTotal.next();
                    final Integer semeTotal = (Integer) meisaiMap._semeTotal.get(semester);
                    svf.VrsOut("LESSON" + semester, semeTotal.toString());
                    nenkan = nenkan + semeTotal.intValue();
                }
                svf.VrsOut("LESSON4", String.valueOf(nenkan));
                final String standardTime = getStandardTimeSql(db2, subclassData._subclassCd, StringUtils.substring(gradeHr, 0, 2));
                svf.VrsOut("LESSON5", standardTime);
                int sabun = nenkan - Integer.parseInt(standardTime);
                svf.VrsOut("LESSON6", String.valueOf(sabun));
                svf.VrEndRecord();
            }
            _hasData = true;
            svf.VrEndRecord();
        }
    }

    private String getStandardTimeSql(final DB2UDB db2, final String subclassCd, final String grade) throws SQLException {
        String retVal = "0";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     STANDARD_TIME ");
        stb.append(" FROM ");
        stb.append("     UNIT_CLASS_LESSON_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclassCd + "' ");
        } else {
            stb.append("     AND CURRICULUM_CD = '1' ");
            stb.append("     AND SUBCLASSCD = '" + subclassCd + "' ");
        }
        stb.append("     AND SEMESTER = '9' ");
        stb.append("     AND GRADE = '" + grade + "' ");
        stb.append("     AND TIME_DIV = '1' ");        

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retVal = rs.getString("STANDARD_TIME");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retVal;
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        List retList = new ArrayList();
        if (SELECT_HR.equals(_param._selectDiv)) {
            retList = getHrData(db2);
        } else {
            retList = getStaffData(db2);
        }
        return retList;
    }

    private List getHrData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getHrSchChrCntSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            HrClass hrClass = null;
            String befSubclassCd = "";
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final String ltitol = rs.getString("L_TITOL");
                final String unitName = rs.getString("UNIT_NAME");
                final String dataMonth = rs.getString("DATA_MONTH");
                final String cnt = rs.getString("CNT");
                if (null == hrClass) {
                    hrClass = new HrClass(db2, semester, subclassCd, subclassAbbv, ltitol, unitName, dataMonth, cnt);
                } else if (!befSubclassCd.equals(subclassCd)) {
                    retList.add(hrClass);
                    hrClass = new HrClass(db2, semester, subclassCd, subclassAbbv, ltitol, unitName, dataMonth, cnt);
                } else {
                    hrClass.setMeisai(semester, subclassCd, subclassAbbv, ltitol, unitName, dataMonth, cnt);
                }
                befSubclassCd = subclassCd;
            }
            if (null != hrClass) {
                retList.add(hrClass);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getHrSchChrCntSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SCH_CDAT.SEMESTER, ");
        stb.append("     SCH_CDAT.EXECUTEDATE, ");
        stb.append("     SCH_CDAT.PERIODCD, ");
        stb.append("     VALUE(UNITD.L_TITOL, 999) AS L_TITOL, ");
        if (UNIT_L.equals(_param._unit)) {
            stb.append("     UNITD.UNIT_L_NAME AS UNIT_NAME, ");
        } else {
            stb.append("     CASE WHEN UNITD.UNIT_S_NAME IS NOT NULL ");
            stb.append("          THEN UNITD.UNIT_S_NAME ");
            stb.append("          ELSE CASE WHEN UNITD.UNIT_M_NAME IS NOT NULL ");
            stb.append("                    THEN UNITD.UNIT_M_NAME ");
            stb.append("                    ELSE UNITD.UNIT_L_NAME ");
            stb.append("               END ");
            stb.append("     END AS UNIT_NAME, ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SUBD.CLASSCD || '-' || SUBD.SCHOOL_KIND || '-' || SUBD.CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBD.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     SUBD.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ");
        stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ");
        stb.append("                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ");
        stb.append("                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ");
        stb.append("     LEFT JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR = CHAIR.YEAR ");
        stb.append("          AND CHAIR_STF.SEMESTER = CHAIR.SEMESTER ");
        stb.append("          AND CHAIR_STF.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHAIR_STF.STAFFCD ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND SUBD.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        }
        stb.append("     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ");
        stb.append("                AND UNIT_TEXT.GRADE = '" + StringUtils.substring(_param._gradeHrClass, 0, 2) + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        }
        stb.append("                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN UNIT_DAT UNITD ON UNITD.YEAR = SCH_CDAT.YEAR ");
        stb.append("          AND UNITD.DATA_DIV = '2' ");
        stb.append("          AND UNITD.GRADE || UNITD.HR_CLASS = '" + _param._gradeHrClass + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND UNITD.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        }
        stb.append("          AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append("          AND UNITD.SEQ = SCH_CDAT.RANK ");
        stb.append("          AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ");
        stb.append("     LEFT JOIN ISSUECOMPANY_MST ISSUEM ON ISSUEM.ISSUECOMPANYCD = UNITD.ISSUECOMPANYCD ");
        stb.append(" WHERE ");
        stb.append("     SCH_CDAT.EXECUTEDATE BETWEEN '" + _param._ctrlYear + "-04-01' AND '" + _param._lastDay + "' ");
        stb.append("     AND SCH_CDAT.CHAIRCD IN ( ");
        stb.append("             SELECT DISTINCT ");
        stb.append("                 CSTD.CHAIRCD ");
        stb.append("             FROM ");
        stb.append("                 SCHREG_REGD_DAT REGD, ");
        stb.append("                 CHAIR_STD_DAT CSTD ");
        stb.append("             WHERE ");
        stb.append("                 REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("             AND REGD.SEMESTER  <= '" + _param._targetSeme + "' ");
        stb.append("             AND REGD.GRADE || REGD.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append("             AND REGD.YEAR = CSTD.YEAR ");
        stb.append("             AND REGD.SEMESTER = CSTD.SEMESTER ");
        stb.append("             AND REGD.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("             AND CSTD.APPDATE <= '" + _param._lastDay + "' ");
        stb.append("             AND CSTD.APPENDDATE >= '" + _param._lastDay + "' ");
        stb.append("     ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUBCLASSABBV, ");
        stb.append("     L_TITOL, ");
        if (UNIT_L.equals(_param._unit)) {
            stb.append("     MAX(UNIT_NAME) AS UNIT_NAME, ");
        } else {
            stb.append("     UNIT_NAME, ");
        }
        stb.append("     MONTH(EXECUTEDATE) AS DATA_MONTH, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     SEMESTER, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUBCLASSABBV, ");
        stb.append("     L_TITOL, ");
        if (!UNIT_L.equals(_param._unit)) {
            stb.append("     UNIT_NAME, ");
        }
        stb.append("     MONTH(EXECUTEDATE) ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SEMESTER, ");
        stb.append("     MONTH(EXECUTEDATE), ");
        stb.append("     L_TITOL ");

        return stb.toString();
    }

    private List getStaffData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStaffSchChrCntSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            SubclassData subclassData = null;
            String befGradeHr = "";
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClassCd = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String semester = rs.getString("SEMESTER");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final String ltitol = rs.getString("L_TITOL");
                final String unitName = rs.getString("UNIT_NAME");
                final String dataMonth = rs.getString("DATA_MONTH");
                final String cnt = rs.getString("CNT");
                if (null == subclassData) {
                    subclassData = new SubclassData(grade, hrClassCd, hrName, staffName, semester, subclassCd, subclassAbbv, ltitol, unitName, dataMonth, cnt);
                } else if (!befGradeHr.equals(grade + hrClassCd)) {
                    retList.add(subclassData);
                    subclassData = new SubclassData(grade, hrClassCd, hrName, staffName, semester, subclassCd, subclassAbbv, ltitol, unitName, dataMonth, cnt);
                } else {
                    subclassData.setMeisai(semester, grade, hrClassCd, hrName, ltitol, unitName, dataMonth, cnt);
                }
                befGradeHr = grade + hrClassCd;
            }
            if (null != subclassData) {
                retList.add(subclassData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStaffSchChrCntSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     REGD_H.GRADE, ");
        stb.append("     REGD_H.HR_CLASS, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     CHAIR_STF.STAFFCD, ");
        stb.append("     STF.STAFFNAME, ");
        stb.append("     SCH_CDAT.SEMESTER, ");
        stb.append("     SCH_CDAT.EXECUTEDATE, ");
        stb.append("     SCH_CDAT.PERIODCD, ");
        stb.append("     VALUE(UNITD.L_TITOL, 999) AS L_TITOL, ");
        if (UNIT_L.equals(_param._unit)) {
            stb.append("     UNITD.UNIT_L_NAME AS UNIT_NAME, ");
        } else {
            stb.append("     CASE WHEN UNITD.UNIT_S_NAME IS NOT NULL ");
            stb.append("          THEN UNITD.UNIT_S_NAME ");
            stb.append("          ELSE CASE WHEN UNITD.UNIT_M_NAME IS NOT NULL ");
            stb.append("                    THEN UNITD.UNIT_M_NAME ");
            stb.append("                    ELSE UNITD.UNIT_L_NAME ");
            stb.append("               END ");
            stb.append("     END AS UNIT_NAME, ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SUBD.CLASSCD || '-' || SUBD.SCHOOL_KIND || '-' || SUBD.CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBD.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     SUBD.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ");
        stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ");
        stb.append("                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ");
        stb.append("                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ");
        stb.append("                AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || ");
        }
        stb.append("                    CHAIR.SUBCLASSCD IN (" + _param._inState + ") ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND SUBD.CLASSCD  || '-' || SUBD.SCHOOL_KIND  || '-' || SUBD.CURRICULUM_CD = ");
            stb.append("         CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD ");
        }
        stb.append("     INNER JOIN CLASS_MST CLASSD ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSD.CLASSCD || '-' || CLASSD.SCHOOL_KIND =");
            stb.append("         SUBD.CLASSCD   || '-' || SUBD.SCHOOL_KIND ");
        } else {
            stb.append("         CLASSD.CLASSCD = SUBSTR(SUBD.SUBCLASSCD, 1, 2) ");
        }
        stb.append("     INNER JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR = CHAIR.YEAR ");
        stb.append("           AND CHAIR_STF.SEMESTER = CHAIR.SEMESTER ");
        stb.append("           AND CHAIR_STF.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("           AND CHAIR_STF.STAFFCD = '" + _param._staff + "' ");
        stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHAIR_STF.STAFFCD ");
        stb.append("     LEFT JOIN ( ");
        stb.append("              SELECT ");
        stb.append("                  CSTD.YEAR, ");
        stb.append("                  CSTD.SEMESTER, ");
        stb.append("                  CSTD.CHAIRCD, ");
        stb.append("                  MAX(REGD.GRADE || '-' || REGD.HR_CLASS) AS MAX_CLASS ");
        stb.append("              FROM ");
        stb.append("                  SCHREG_REGD_DAT REGD, ");
        stb.append("                  CHAIR_STD_DAT CSTD ");
        stb.append("              WHERE ");
        stb.append("                  REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("                  AND REGD.SEMESTER <= '" + _param._targetSeme + "' ");
        stb.append("                  AND REGD.YEAR = CSTD.YEAR ");
        stb.append("                  AND REGD.SEMESTER = CSTD.SEMESTER ");
        stb.append("                  AND REGD.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("                  AND CSTD.APPDATE <= '" + _param._lastDay + "' ");
        stb.append("                  AND CSTD.APPENDDATE >= '" + _param._lastDay + "' ");
        stb.append("              GROUP BY ");
        stb.append("                  CSTD.YEAR, ");
        stb.append("                  CSTD.SEMESTER, ");
        stb.append("                  CSTD.CHAIRCD ");
        stb.append("          ) MAX_REGD ON MAX_REGD.YEAR = CHAIR_STF.YEAR ");
        stb.append("          AND MAX_REGD.SEMESTER = CHAIR_STF.SEMESTER ");
        stb.append("          AND MAX_REGD.CHAIRCD = CHAIR_STF.CHAIRCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = MAX_REGD.YEAR ");
        stb.append("          AND REGD_H.SEMESTER = MAX_REGD.SEMESTER ");
        stb.append("          AND REGD_H.GRADE || '-' || REGD_H.HR_CLASS = MAX_REGD.MAX_CLASS ");
        stb.append("     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ");
        stb.append("                AND UNIT_TEXT.GRADE = REGD_H.GRADE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ");
            stb.append("                AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("                AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        }
        stb.append("                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN UNIT_DAT UNITD ON UNITD.YEAR = SCH_CDAT.YEAR ");
        stb.append("          AND UNITD.DATA_DIV = '2' ");
        stb.append("          AND UNITD.GRADE || UNITD.HR_CLASS = '" + _param._gradeHrClass + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                AND UNITD.CLASSCD = CHAIR.CLASSCD ");
            stb.append("                AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("                AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        }
        stb.append("          AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append("          AND UNITD.SEQ = SCH_CDAT.RANK ");
        stb.append("          AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ");
        stb.append("     LEFT JOIN ISSUECOMPANY_MST ISSUEM ON ISSUEM.ISSUECOMPANYCD = UNITD.ISSUECOMPANYCD ");
        stb.append(" WHERE ");
        stb.append("     SCH_CDAT.EXECUTEDATE BETWEEN '" + _param._ctrlYear + "-04-01' AND '" + _param._lastDay + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME, ");
        stb.append("     STAFFCD, ");
        stb.append("     STAFFNAME, ");
        stb.append("     L_TITOL, ");
        if (UNIT_L.equals(_param._unit)) {
            stb.append("     MAX(UNIT_NAME) AS UNIT_NAME, ");
        } else {
            stb.append("     UNIT_NAME, ");
        }
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUBCLASSABBV, ");
        stb.append("     MONTH(EXECUTEDATE) AS DATA_MONTH, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     SEMESTER, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME, ");
        stb.append("     STAFFCD, ");
        stb.append("     STAFFNAME, ");
        stb.append("     L_TITOL, ");
        if (!UNIT_L.equals(_param._unit)) {
            stb.append("     UNIT_NAME, ");
        }
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUBCLASSABBV, ");
        stb.append("     MONTH(EXECUTEDATE) ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     SEMESTER, ");
        stb.append("     MONTH(EXECUTEDATE), ");
        stb.append("     L_TITOL ");

        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;
        private final Map _printMap = new HashMap();

        HrClass(final DB2UDB db2,
                final String semester,
                final String subclassCd,
                final String subclassAbbv,
                final String ltitol,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     L1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                _grade = rs.getString("GRADE");
                _hrClass = rs.getString("HR_CLASS");
                _hrName = rs.getString("HR_NAME");
                _staffName = rs.getString("STAFFNAME");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final MeisaiMap meisaiMap = new MeisaiMap(subclassAbbv, semester, ltitol, unitName, dataMonth, cnt);
            _printMap.put(subclassCd, meisaiMap);
        }

        private void setMeisai(
                final String semester,
                final String subclassCd,
                final String subclassAbbv,
                final String ltitol,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            MeisaiMap meisaiMap = null;
            if (!_printMap.containsKey(subclassCd)) {
                meisaiMap = new MeisaiMap(subclassAbbv, semester, ltitol, unitName, dataMonth, cnt);
            } else {
                meisaiMap = (MeisaiMap) _printMap.get(subclassCd);
                meisaiMap.setMonthData(semester, unitName, dataMonth, cnt);
            }
            _printMap.put(subclassCd, meisaiMap);
        }

        public String toString() {
            return _hrName;
        }
    }

    private class SubclassData {
        private final String _subclassCd;
        private final String _subclassAbbv;
        private final String _staffName;
        private final Map _printMap = new HashMap();

        SubclassData(
                final String grade,
                final String hrClassCd,
                final String hrName,
                final String staffName,
                final String semester,
                final String subclassCd,
                final String subclassAbbv,
                final String ltitol,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            _staffName = staffName;
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
            final MeisaiMap meisaiMap = new MeisaiMap(hrName, semester, ltitol, unitName, dataMonth, cnt);
            _printMap.put(grade + hrClassCd, meisaiMap);
        }

        private void setMeisai(
                final String semester,
                final String grade,
                final String hrClassCd,
                final String hrName,
                final String ltitol,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            MeisaiMap meisaiMap = null;
            if (!_printMap.containsKey(grade + hrClassCd)) {
                meisaiMap = new MeisaiMap(hrName, semester, ltitol, unitName, dataMonth, cnt);
            } else {
                meisaiMap = (MeisaiMap) _printMap.get(grade + hrClassCd);
                meisaiMap.setMonthData(semester, unitName, dataMonth, cnt);
            }
            _printMap.put(grade + hrClassCd, meisaiMap);
        }

        public String toString() {
            return _subclassAbbv;
        }
    }

    private class MeisaiMap {
        private final String _title;
        private final Map _monthData;
        private final Map _semeTotal;

        MeisaiMap(
                final String title,
                final String semester,
                final String Ltitol,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            _title = title;
            _monthData = new HashMap();
            final MonthData monthData = new MonthData(unitName, dataMonth, cnt);
            _monthData.put(dataMonth, monthData);
            _semeTotal = new HashMap();
            _semeTotal.put(semester, new Integer(cnt));
        }

        private void setMonthData(
                final String semester,
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            MonthData monthData = null;
            if (_monthData.containsKey(dataMonth)) {
                monthData = (MonthData) _monthData.get(dataMonth);
                monthData.setUnit(unitName, dataMonth, cnt);
            } else {
                monthData = new MonthData(unitName, dataMonth, cnt);
            }
            _monthData.put(dataMonth, monthData);
            int semeCnt = Integer.parseInt(cnt);
            if (!_semeTotal.containsKey(semester)) {
                _semeTotal.put(semester, new Integer(cnt));
            } else {
                Integer semeAll = (Integer) _semeTotal.get(semester);
                final int setSemeCnt = semeAll.intValue() + semeCnt;
                _semeTotal.put(semester, new Integer(setSemeCnt));
            }
        }
    }

    private class MonthData {
        private final String _month;
        private final List _unitList;
        int _monthTotal;

        MonthData(
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            _month = dataMonth;
            _unitList = new ArrayList();
            final UnitData unitData = new UnitData(unitName, cnt);
            _unitList.add(unitData);
            _monthTotal = Integer.parseInt(cnt);
        }

        private void setUnit(
                final String unitName,
                final String dataMonth,
                final String cnt
        ) {
            final UnitData unitData = new UnitData(unitName, cnt);
            _unitList.add(unitData);
            _monthTotal = _monthTotal + Integer.parseInt(cnt);
        }
    }

    private class UnitData {
        private final String _unitname;
        private final String _cnt;

        UnitData(
                final String unitName,
                final String cnt
        ) {
            _unitname = unitName;
            _cnt = cnt;
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _selectDiv;
        private final String _gradeHrClass;
        private final String _staff;
        private final String _targetMonth;
        private final String _targetYear;
        private final String _targetSeme;
        private final String[] _categorySelect;
        private final String _useCurriculumcd;
        private final String _inState;
        private final String _unit;
        private final String _lastDay;
        private final boolean _seirekiFlg;
        private final Map _semesterMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _selectDiv = request.getParameter("SELECT_DIV");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _staff = request.getParameter("STAFF");
            final String monthSeme = request.getParameter("TARGET_MONTH");
            final String[] monthSemeArray = StringUtils.split(monthSeme, '-');
            _targetMonth = monthSemeArray[0];
            _targetYear = Integer.parseInt(_targetMonth) > 3 ? _ctrlYear : String.valueOf(Integer.parseInt(_ctrlYear) + 1);
            _targetSeme = monthSemeArray[1];
            _categorySelect = request.getParameterValues("CATEGORY_SELECTED");
            final StringBuffer categoryInState = new StringBuffer();
            String sep = "";
            if (!SELECT_HR.equals(_selectDiv)) {
                for (int i = 0; i < _categorySelect.length; i++) {
                    categoryInState.append(sep + "'" + _categorySelect[i] + "'");
                    sep = ",";
                }
            }
            _inState = categoryInState.toString();
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _lastDay = getLastDay(db2);
            _unit = request.getParameter("UNIT");
            _seirekiFlg = getSeirekiFlg(db2);
            _semesterMap = getSemesMst(db2);
        }

        /**
         * @param db2
         * @return
         */
        private Map getSemesMst(DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER < '9' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semeName = rs.getString("SEMESTERNAME");
                    retMap.put(semester, semeName);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getLastDay(final DB2UDB db2) throws SQLException {
            String retDay = getSemeLastDay(db2);
            Calendar cal = Calendar.getInstance();
            final String monthFirst = _targetYear + "-" + new DecimalFormat("00").format(Integer.parseInt(_targetMonth)) + "-01";
            cal.setTime(Date.valueOf(monthFirst));
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DATE, -1);
            java.util.Date date = cal.getTime();
            final String monthLastDay = new SimpleDateFormat("yyyy-MM-dd").format(date);
            if (Integer.parseInt(StringUtils.replaceChars(monthLastDay, "-", "")) < Integer.parseInt(StringUtils.replaceChars(retDay, "-", ""))) {
                retDay = monthLastDay;
            }
            return retDay;
        }

        private String getSemeLastDay(final DB2UDB db2) throws SQLException {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     EDATE ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _targetSeme + "' ");

            String retDay = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retDay = rs.getString("EDATE");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retDay;
        }
        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return 
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        public String getYear() {
            return _seirekiFlg ? _ctrlYear + "年度": KNJ_EditDate.h_format_JP_N(_ctrlYear + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDate() {
            return _seirekiFlg ?
                    (_ctrlDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_ctrlDate)) : (KNJ_EditDate.h_format_JP(_ctrlDate));
        }

    }
}

// eof

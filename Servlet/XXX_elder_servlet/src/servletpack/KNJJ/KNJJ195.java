/*
 * $Id: f89b5c7592a15d9cc1905bbe36bc81b0401a58c8 $
 *
 * 作成日: 2017/03/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJJ195 {

    private static final Log log = LogFactory.getLog(KNJJ195.class);

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
        svf.VrSetForm("KNJJ195.frm", 4);
        svf.VrsOut("TITLE", _param._ctrlYear + "年度　各クラス支部別人数");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss現在");
        svf.VrsOut("DATE", sdf.format(date));

        int titleCnt = 1;
        for (Iterator iterator = _param._branchMap.keySet().iterator(); iterator.hasNext();) {
            final String branchCd = (String) iterator.next();
            final String branchName = (String) _param._branchMap.get(branchCd);
            svf.VrsOut("BRANCH_NAME" + titleCnt, branchName);
            titleCnt++;
        }
        svf.VrEndRecord();
        final List list = getList(db2);
        String befGrade = "";
        String befGradeName = "";
        String befSchoolKind = "";
        String befSchoolKindName = "";
        Map gradeTotalMap = _param.getTotalMap();
        Map kindTotalMap = _param.getTotalMap();
        Map totalMap = _param.getTotalMap();
        for (int line = 0; line < list.size(); line++) {
            final HrClass hrClass = (HrClass) list.get(line);

            gradeTotalMap = printTotal(svf, befGrade, befGradeName, gradeTotalMap, hrClass._grade);
            kindTotalMap = printTotal(svf, befSchoolKind, befSchoolKindName, kindTotalMap, hrClass._schoolKind);
            if (!"".equals(befSchoolKind) && !befSchoolKind.equals(hrClass._schoolKind)) {
                svf.VrsOut("BRANK", "1");
                svf.VrEndRecord();
            }

            svf.VrsOut("JH_NAME", hrClass._jhName);
            svf.VrsOut("HR_NAME", hrClass._abbv);
            svf.VrsOut("CLASS_NUM", hrClass._hrCnt);

            int fieldCnt = 1;
            for (Iterator iterator = _param._branchMap.keySet().iterator(); iterator.hasNext();) {
                final String branchCd = (String) iterator.next();
                if (null != hrClass._branchCntMap && !hrClass._branchCntMap.containsKey(branchCd)) {
                    fieldCnt++;
                    continue;
                }
                final String branchCnt = (String) hrClass._branchCntMap.get(branchCd);
                svf.VrsOut("BRANCH_NUM" + fieldCnt, branchCnt);
                setTotalMap(gradeTotalMap, branchCd, branchCnt);
                setTotalMap(kindTotalMap, branchCd, branchCnt);
                setTotalMap(totalMap, branchCd, branchCnt);
                fieldCnt++;
            }

            svf.VrEndRecord();
            befGrade = hrClass._grade;
            befGradeName = hrClass._gradeName;
            befSchoolKind = hrClass._schoolKind;
            befSchoolKindName = hrClass._jhName;
            _hasData = true;
        }
        gradeTotalMap = printTotal(svf, befGrade, befGradeName, gradeTotalMap, "");
        kindTotalMap = printTotal(svf, befSchoolKind, befSchoolKindName, kindTotalMap, "");
        svf.VrsOut("BRANK", "1");
        svf.VrEndRecord();
        totalMap = printTotal(svf, befSchoolKind, "中高", totalMap, "");
    }

    private void setTotalMap(Map totalMap, final String branchCd, final String branchCnt) {
        String gradeVal = (String) totalMap.get(branchCd);
        gradeVal = String.valueOf(Integer.parseInt(gradeVal) + Integer.parseInt(branchCnt));
        totalMap.put(branchCd, gradeVal);
    }

    private Map printTotal(final Vrw32alp svf, final String befData, final String befDataName, Map totalMap, final String aftData) {
        if (!"".equals(befData) && !befData.equals(aftData)) {
            svf.VrsOut("TOTAL_NAME", befDataName + "合計");
            int gradeFieldCnt = 1;
            for (Iterator itgrade = totalMap.keySet().iterator(); itgrade.hasNext();) {
                final String setbranchCd = (String) itgrade.next();
                final String setBranchCnt = (String) totalMap.get(setbranchCd);
                svf.VrsOut("TOTAL_BRANCH_NUM" + gradeFieldCnt, setBranchCnt);
                gradeFieldCnt++;
            }
            svf.VrEndRecord();
            totalMap = _param.getTotalMap();
        }
        return totalMap;
    }

	private List getList(final DB2UDB db2) {
		final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befKindGradeHr = "";
            HrClass objHrClass = null;
            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String jhName = rs.getString("JHNAME");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String branchcd = rs.getString("BRANCHCD");
                final String hrCnt = rs.getString("HR_CNT");
                final String abbv = rs.getString("HR_NAMEABBV");
                final String branchCnt = rs.getString("BRANCH_CNT");
                final String kindGradeHr = schoolKind + grade + hrClass;
                if (!befKindGradeHr.equals(kindGradeHr)) {
                    objHrClass = new HrClass(schoolKind, gradeName, jhName, grade, hrClass, abbv, hrCnt);
                    list.add(objHrClass);
                }
                objHrClass._branchCntMap.put(null == branchcd ? "" : branchcd, branchCnt);
                befKindGradeHr = kindGradeHr;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
		return list;
	}

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.SCHOOL_KIND, ");
        stb.append("         S1.GRADE_NAME1, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         S2.HR_NAMEABBV, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT S1 ");
        stb.append("              ON T1.YEAR     = S1.YEAR ");
        stb.append("             AND T1.GRADE    = S1.GRADE ");
        stb.append("             AND S1.SCHOOL_KIND IN ('H','J') ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT S2 ");
        stb.append("              ON T1.YEAR     = S2.YEAR ");
        stb.append("             AND T1.SEMESTER = S2.SEMESTER ");
        stb.append("             AND T1.GRADE    = S2.GRADE ");
        stb.append("             AND T1.HR_CLASS = S2.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_BASE_MST S3 ");
        stb.append("              ON T1.SCHREGNO = S3.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ), MAIN_CNT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     COUNT(T1.SCHREGNO) AS HR_CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE_NAME1, ");
        stb.append("     A023.ABBV1 AS JHNAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     S1.BRANCHCD, ");
        stb.append("     MAX(T2.HR_CNT) AS HR_CNT, ");
        stb.append("     COUNT(T1.SCHREGNO) AS BRANCH_CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     LEFT JOIN SCHREG_BRANCH_DAT S1 ");
        stb.append("          ON S1.SCHOOLCD     = '" + _param._schoolcd + "' ");
        stb.append("         AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ");
        stb.append("         AND S1.YEAR         = '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.SCHREGNO     = S1.SCHREGNO ");
        stb.append("     LEFT JOIN MAIN_CNT T2 ON T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("          AND T1.GRADE = T2.GRADE ");
        stb.append("          AND T1.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("          AND T1.SCHOOL_KIND = A023.NAME1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE_NAME1, ");
        stb.append("     A023.ABBV1, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     S1.BRANCHCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     S1.BRANCHCD ");
        return stb.toString();
    }

    private class HrClass {
        final String _schoolKind;
        final String _gradeName;
        final String _jhName;
        final String _grade;
        final String _hrClass;
        final String _abbv;
        final String _hrCnt;
        final Map _branchCntMap;
        public HrClass(
            final String schoolKind,
            final String gradeName,
            final String jhName,
            final String grade,
            final String hrClass,
            final String abbv,
            final String hrCnt
        ) {
            _schoolKind = schoolKind;
            _gradeName = gradeName;
            _jhName = jhName;
            _grade = grade;
            _hrClass = hrClass;
            _abbv = abbv;
            _hrCnt = hrCnt;
            _branchCntMap = new TreeMap();
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
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _usecurriculumcd;
        final String _useschoolKindfield;
        final String _schoolcd;
        final String _schoolKind;
        final Map _branchMap;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolcd = request.getParameter("LOGIN_SCHOOLCD");
            _schoolKind = request.getParameter("LOGIN_SCHOOL_KIND");
            _branchMap = getBranchMap(db2);
        }
        public Map getTotalMap() {
            final Map retMap = new TreeMap();
            for (Iterator iterator = _branchMap.keySet().iterator(); iterator.hasNext();) {
                final String branchCd = (String) iterator.next();
                retMap.put(branchCd, "0");
            }
            return retMap;
        }
        private Map getBranchMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BRANCHCD, ");
            stb.append("     BRANCHNAME ");
            stb.append(" FROM ");
            stb.append("     BRANCH_MST ");
            stb.append(" ORDER BY ");
            stb.append("     BRANCHCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String branchcd = rs.getString("BRANCHCD");
                    final String branchName = rs.getString("BRANCHNAME");
                    retMap.put(branchcd, branchName);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

    }
}

// eof


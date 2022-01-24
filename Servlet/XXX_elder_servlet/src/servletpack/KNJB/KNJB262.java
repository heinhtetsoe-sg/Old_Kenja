/*
 * $Id: 4b432f4a2f93af86b90525ae74a8f6ae7e69193f $
 *
 * 作成日: 2017/06/02
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class KNJB262 {

    private static final Log log = LogFactory.getLog(KNJB262.class);

    private static final String GRADE_ALL = "99"; // 学年全て

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
        svf.VrSetForm("KNJB262.frm", 4);
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._year + "-01-01") + "度　自習者一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate) + _param._loginHour + "時" + _param._loginMinutes + "分");
        svf.VrsOut("GRADE", _param._gradeName + "　講座基準日(" + _param._appDate.replace('-', '/') + ")　" + _param._rirekiName);

        final DecimalFormat df = new DecimalFormat("0000");

        final Map chairAllMap = getAllMap(getList(db2, sqlChairDat()));
        final List subclassStudentList = groupBySubclass(getList(db2, sqlSubclassStd()));
        for (int i = 0; i < subclassStudentList.size(); i++) {
            final List studentList = (List) subclassStudentList.get(i);
            List chairList = new ArrayList();

            final String subGrp = df.format(i+1);

            final Map subStdMap0 = (Map) studentList.get(0);
            final String setSubclasscd = getString(subStdMap0, "SUBCLASSCD");
            final String setSubclassName = getString(subStdMap0, "SUBCLASSNAME");
            final String setSubclassNum = getString(subStdMap0, "SUBCLASS_NUM");
            if (chairAllMap.containsKey(setSubclasscd)) {
                chairList = (List) chairAllMap.get(setSubclasscd);
            }
            
            int maxLine = studentList.size() < chairList.size() ? chairList.size() : studentList.size();
            if (maxLine < 2) maxLine = 2;

            for (int ii = 0; ii < maxLine; ii++) {
                svf.VrsOut("SUBCLASS_GRP", subGrp);
                if (ii == 0) {
                    svf.VrsOut("SUBCLASS1", setSubclasscd);
                } else if (ii == 1) {
                    svf.VrsOut("SUBCLASS" + (getMS932Bytecount(setSubclassName) <= 14 ? "1" : "2"), setSubclassName);
                }
                svf.VrsOut("SUBCLASS_GRP2", subGrp);
                if (ii == 0) {
                    svf.VrsOut("SUBCLASS_NUM", setSubclassNum);
                }
                if (chairList.size() > ii) {
                    final Map chairMap = (Map) chairList.get(ii);
                    svf.VrsOut("CHAIR_CD", getString(chairMap, "CHAIRCD"));
                    final String setName = getString(chairMap, "CHAIRNAME");
                    svf.VrsOut("CHAIR_NAME" + (getMS932Bytecount(setName) <= 20 ? "1" : getMS932Bytecount(setName) <= 30 ? "2" : "3"), setName);
                    svf.VrsOut("CHAIR_NUM", getString(chairMap, "CHAIR_NUM"));
                }
                if (studentList.size() > ii) {
                    final Map studentMap = (Map) studentList.get(ii);
                    final String setName = getString(studentMap, "HR_NAME") + "-" + getString(studentMap, "ATTENDNO") + "(" + getString(studentMap, "SCHREGNO") + ")" + getString(studentMap, "NAME");
                    svf.VrsOut("NAME" + (getMS932Bytecount(setName) <= 50 ? "1" : "2"), setName);
                }
                svf.VrsOut("NAME_GRP", subGrp);
                svf.VrEndRecord();
                _hasData = true;
            }
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

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static Map getAllMap(final List list) {
        Map allMap = new HashMap();
        for (int ii = 0; ii < list.size(); ii++) {
            final Map m = (Map) list.get(ii);
            final String key = getString(m, "SUBCLASSCD");
            if (!allMap.containsKey(key)) {
                allMap.put(key, new ArrayList());
            }
            final List tmpList = (List) allMap.get(key);
            tmpList.add(m);
        }
        return allMap;
    }

    private static List groupBySubclass(final List list) {
        final List rtn = new ArrayList();
        List current = null;
        String currentCd = null;
        for (int i = 0; i < list.size(); i++) {
            final Map m = (Map) list.get(i);
            final String cd = getString(m, "SUBCLASSCD");
            if (null == currentCd || !currentCd.equals(cd)) {
                current = new ArrayList();
                rtn.add(current);
                currentCd = cd;
            }
            current.add(m);
        }
        return rtn;
    }

    private String sqlChairDat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_REGD AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME, ");
        stb.append("         T3.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T3.GRADE = T1.GRADE ");
        stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        if (!GRADE_ALL.equals(_param._grade)) {
            stb.append("         AND T1.GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ), CHAIR_STD AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         COUNT(T1.SCHREGNO) AS CHAIR_NUM ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND '" + _param._appDate + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");
        stb.append("     GROUP BY ");
        stb.append("         T1.CHAIRCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     L1.CHAIR_NUM ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_STD L1 ON L1.CHAIRCD = T1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    private String sqlSubclassStd() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_REGD AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME, ");
        stb.append("         T3.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T3.GRADE = T1.GRADE ");
        stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        if (!GRADE_ALL.equals(_param._grade)) {
            stb.append("         AND T1.GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ), CHAIR_STD AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         LEFT JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND '" + _param._appDate + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");
        stb.append("     GROUP BY ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), SUBCLASS_STD AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), SUBCLASS_STD_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         COUNT(T1.SCHREGNO) AS SUBCLASS_NUM ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     L2.SUBCLASS_NUM, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD T1 ");
        stb.append("     INNER JOIN SCHREG_REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD ");
        stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_STD_CNT L2 ON L2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             'X' ");
        stb.append("         FROM ");
        stb.append("             CHAIR_STD W1 ");
        stb.append("         WHERE ");
        stb.append("             W1.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND W1.CLASSCD = T1.CLASSCD ");
        stb.append("             AND W1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND W1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND W1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
//        stb.append("     T1.SCHOOL_KIND DESC, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _appDate;
        final String _loginDate;
        final String _rirekiCode;

        /** ログイン時間 */
        final String _loginHour;
        final String _loginMinutes;

        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;
        private final String _use_prg_schoolkind;
        private final String _selectSchoolKind;
        private final String[] _selectSchoolKinds;

        String _gradeName = null;
        String _rirekiName = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _appDate = request.getParameter("DATE").replace('/', '-'); // 講座基準日
            _rirekiCode = request.getParameter("RIREKI_CODE");
            _loginDate = request.getParameter("LOGIN_DATE");

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _selectSchoolKinds = StringUtils.split(_selectSchoolKind, ":");

            final Calendar cal = Calendar.getInstance();
            _loginHour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            _loginMinutes = String.valueOf(cal.get(Calendar.MINUTE));

            setGradeName(db2);
            setRirekiName(db2);
        }

        private String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _selectSchoolKinds.length; i++) {
                stb.append(sep + "'" + _selectSchoolKinds[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

        /** 学年名取得 */
        private void setGradeName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeName = null;
            try {
                String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_selectSchoolKind)) {
                        sql += "   AND SCHOOL_KIND IN (" + getInState() + ") ";
                    }
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += "   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1") != null ? rs.getString("GRADE_NAME1") : "";
                }
                if (GRADE_ALL.equals(_grade)) {
                    _gradeName = "全学年";
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 履修履歴取得 */
        private void setRirekiName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _rirekiName = null;
            try {
                String sql = "";
                sql += " SELECT ";
                sql += "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
                sql += " FROM ";
                sql += "     STUDY_SELECT_DATE_YMST ";
                sql += " WHERE ";
                sql += "     YEAR = '" + _year + "' ";
                sql += "     AND RIREKI_CODE = '" + _rirekiCode + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _rirekiName = rs.getString("LABEL") != null ? rs.getString("LABEL") : "";
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof


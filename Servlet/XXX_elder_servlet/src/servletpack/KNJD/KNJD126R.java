/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 4a5a9821fe922ca016469789f9a921492f37dfc4 $
 *
 * 作成日: 2018/08/30
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD126R {

    private static final Log log = LogFactory.getLog(KNJD126R.class);

    private boolean _hasData;
    private static final int MAX_LINECNT = 50;
    private static final String HYOUTEI = "99";

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
        svf.VrSetForm("KNJD126R.frm", 1);
        printTitle(db2, svf);
        final List printList = getList(db2);
        int lineCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (MAX_LINECNT < lineCnt) {
                lineCnt = 1;
                svf.VrEndPage();
                printTitle(db2, svf);
            }

            svf.VrsOutn("HR_NAME", lineCnt, student._hrName + "-" + student._attendNo);
            svf.VrsOutn("NAME", lineCnt, student._name);
            int statusCnt = 1;
            for (Iterator itPrintView = student._setViewMap.keySet().iterator(); itPrintView.hasNext();) {
                final String viewCd = (String) itPrintView.next();
                final String setScore = (String) student._setViewMap.get(viewCd);
                if (HYOUTEI.equals(viewCd)) {
                    svf.VrsOutn("STATUS9", lineCnt, setScore);
                } else {
                    svf.VrsOutn("STATUS" + statusCnt, lineCnt, setScore);
                    statusCnt++;
                }
            }

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("CLASS", _param._className);
        svf.VrsOut("CHAIRTITLE", "講座");
        svf.VrsOut("CHAIRNAME", _param._chairName);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        int lineCnt = 1;
        for (Iterator itView = _param._viewMap.keySet().iterator(); itView.hasNext();) {
            final String key = (String) itView.next();
            final String viewName = (String) _param._viewMap.get(key);
            svf.VrsOut("VIEWCD1_" + key, key);
            svf.VrsOutn("VIEWCD2", lineCnt, key);
            svf.VrsOutn("VIEWNAME", lineCnt, viewName);
            lineCnt++;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");

                final Map setViewMap = new TreeMap();
                for (int viewCnt = 0; viewCnt < _param._viewData.length; viewCnt++) {
                    final String viewCd = _param._viewData[viewCnt];
                    final String setScore = rs.getString("SCORE" + viewCd);
                    setViewMap.put(viewCd, StringUtils.defaultString(setScore));
                }
                final String viewCd = "99";
                final String setScore = rs.getString("SCORE" + viewCd);
                setViewMap.put(viewCd, StringUtils.defaultString(setScore));

                final Student student = new Student(grade, hrClass, hrName, attendNo, schregNo, name, setViewMap);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("        REGDH.HR_NAME, ");
        stb.append("        REGD.GRADE, ");
        stb.append("        REGD.HR_CLASS, ");
        stb.append("        REGD.ATTENDNO, ");
        stb.append("        REGD.SCHREGNO, ");
        stb.append("        BASE.NAME ");
        stb.append("     FROM ");
        stb.append("        SCHREG_REGD_HDAT REGDH, ");
        stb.append("        SCHREG_REGD_DAT REGD, ");
        stb.append("        SCHREG_BASE_MST BASE, ");
        stb.append("        CHAIR_STD_DAT CSTD ");
        stb.append("     WHERE ");
        stb.append("        REGDH.YEAR      = REGD.YEAR ");
        stb.append("        AND REGDH.SEMESTER  = REGD.SEMESTER ");
        stb.append("        AND REGDH.GRADE     = REGD.GRADE ");
        stb.append("        AND REGDH.HR_CLASS  = REGD.HR_CLASS ");
        stb.append("        AND REGD.YEAR      = '" + _param._ctrlYear + "' ");
        stb.append("        AND REGD.SEMESTER  = '" + _param._semester2 + "' ");
        stb.append("        AND REGD.SCHREGNO  = BASE.SCHREGNO ");
        stb.append("        AND CSTD.YEAR      = REGD.YEAR ");
        stb.append("        AND CSTD.CHAIRCD   = '" + _param._sendChairCd + "' ");
        stb.append("        AND CSTD.SCHREGNO  = REGD.SCHREGNO ");
        stb.append("        AND '" + _param._execute_date + "' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ");
        stb.append(" ), VIEWSTAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO ");
        for (int viewCnt = 0; viewCnt < _param._viewData.length; viewCnt++) {
            final String viewCd = _param._viewData[viewCnt];
            stb.append("    ,MAX(CASE WHEN T2.VIEWCD = '" + viewCd + "' THEN T2.SCORE ELSE NULL END) AS SCORE" + viewCd);
        }
        stb.append("     FROM ");
        stb.append("         SCHINFO T1, ");
        stb.append("         JVIEWSTAT_RECORD_DAT T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR         = '" + _param._ctrlYear + "' AND ");
        stb.append("         T2.SEMESTER     = '" + _param._semester + "' AND ");
        stb.append("         T1.SCHREGNO     = T2.SCHREGNO AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T2.CLASSCD          = '" + _param._sendClassCd + "' AND ");
            stb.append("         T2.SCHOOL_KIND      = '" + _param._sendSchoolKind + "' AND ");
            stb.append("         T2.CURRICULUM_CD    = '" + _param._sendCurriculumCd + "' AND ");
            stb.append("         T2.SUBCLASSCD       = '" + _param._sendSubclasscd + "' AND ");
        } else {
            stb.append("         T2.SUBCLASSCD       = '" + _param._sendSubclasscd + "' AND ");
        }
        if (_param._viewData.length > 0) {
            stb.append("    T2.VIEWCD IN" + SQLUtils.whereIn(true, _param._viewData) + " AND ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        SUBSTR(T2.VIEWCD, 1, 2) = '" + _param._sendClassCd + "' ");
        } else {
            stb.append("        SUBSTR(T2.VIEWCD, 1, 2) = '" + _param._sendSubclasscd.substring(0, 2) + "' ");
        }
        stb.append("    GROUP BY T1.SCHREGNO ");
        stb.append("    ) ");
        stb.append("SELECT ");
        stb.append("    T1.HR_NAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.NAME ");
        for (int viewCnt = 0; viewCnt < _param._viewData.length; viewCnt++) {
            final String viewCd = _param._viewData[viewCnt];
            if (!"99".equals(viewCd.substring(0, 2)) && !"99".equals(viewCd)) {
                stb.append(",T2.SCORE" + viewCd);
            }
        }
        stb.append(",T3.SCORE AS SCORE99");
        stb.append(" FROM ");
        stb.append("     SCHINFO T1 ");
        stb.append("     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb.append("              ON T3.YEAR         = '" + _param._ctrlYear + "' ");
        stb.append("             AND T3.SEMESTER     = '" + _param._semester + "' ");
        stb.append("             AND T3.TESTKINDCD   = '99' ");
        stb.append("             AND T3.TESTITEMCD   = '00' ");
        stb.append("             AND T3.SCORE_DIV    = '09' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND T3.CLASSCD          = '" + _param._sendClassCd + "' ");
            stb.append("             AND T3.SCHOOL_KIND      = '" + _param._sendSchoolKind + "' ");
            stb.append("             AND T3.CURRICULUM_CD    = '" + _param._sendCurriculumCd + "' ");
            stb.append("             AND T3.SUBCLASSCD       = '" + _param._sendSubclasscd + "' ");
        } else {
            stb.append("             AND T3.SUBCLASSCD       = '" + _param._sendSubclasscd + "' ");
        }
        stb.append("             AND T3.SCHREGNO     = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _schregNo;
        final String _name;
        final Map _setViewMap;

        public Student(final String grade, final String hrClass, final String hrName, final String attendNo, final String schregNo, final String name, final Map setViewMap) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _schregNo = schregNo;
            _name = name;
            _setViewMap = setViewMap;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62032 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _semesterName;
        final String _semester2;
        final String _classcd;
        final String _chaircd;
        final String _sendChairCd;
        final String _sendClassCd;
        final String _sendCurriculumCd;
        final String _sendSchoolKind;
        final String _sendSubclasscd;
        final String _sendViewdata;
        final String[] _viewData;
        final String _className;
        final String _chairName;
        final String _grade;
        final String _prgid;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _schoolKind;
        final String _kantenhyouji;
        final String _kantenhyouji_5;
        final String _kantenhyouji_6;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _execute_date;
        final Map _viewMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _semester2 = request.getParameter("SEMESTER2");
            _classcd = request.getParameter("CLASSCD");
            _chaircd = request.getParameter("CHAIRCD");
            _sendChairCd = request.getParameter("SEND_CHAIRCD");
            _sendClassCd = request.getParameter("SEND_CLASSCD");
            _sendCurriculumCd = request.getParameter("SEND_CURRICULUMCD");
            _sendSchoolKind = request.getParameter("SEND_SCHOOL_KIND");
            _sendSubclasscd = request.getParameter("SEND_SUBCLASSCD");
            _sendViewdata = request.getParameter("SEND_VIEWDATA");
            _viewData = StringUtils.split(_sendViewdata, ",");
            _grade = request.getParameter("GRADE");
            _prgid = request.getParameter("PRGID");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _kantenhyouji = request.getParameter("kantenHyouji");
            _kantenhyouji_5 = request.getParameter("kantenHyouji_5");
            _kantenhyouji_6 = request.getParameter("kantenHyouji_6");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _execute_date = request.getParameter("execute_date");
            _semesterName = getSemesterName(db2);
            _className = getClassName(db2);
            _chairName = getChairName(db2);
            _viewMap = getViewMap(db2);
        }

        private String getSemesterName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            String setNameCd = "Z009";
            if ("1".equals(_use_prg_schoolkind)) {
                setNameCd = "Z" + _schoolKind + "09";
            } else if ("1".equals(_useSchool_KindField)) {
                setNameCd = "Z" + _schoolKind + "09";
            }
            stb.append(" SELECT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR    = '" + _ctrlYear + "' ");
            stb.append("     AND NAMECD1 = '" + setNameCd + "' ");
            stb.append("     AND NAMECD2 = '" + _semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("    NAMECD2 ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String getClassName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = null;
            try {
                ps = db2.prepareStatement("SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + _classcd + "'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("CLASSNAME");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String getChairName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = null;
            try {
                ps = db2.prepareStatement("SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester2 + "' AND CHAIRCD = '" + _sendChairCd + "'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("CHAIRNAME");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private Map getViewMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            final String sql = getViewCdSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int setKey = 1;
                while (rs.next()) {
                    final String viewName = rs.getString("VIEWNAME");
                    retMap.put(String.valueOf(setKey), viewName);
                    setKey++;
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getViewCdSql() {
            StringBuffer stb = new StringBuffer();
            try {
                stb.append(" SELECT ");
                stb.append("     T1.VIEWCD, ");
                stb.append("     T2.VIEWNAME ");
                stb.append(" FROM ");
                stb.append("     JVIEWNAME_GRADE_YDAT T1, ");
                stb.append("     JVIEWNAME_GRADE_MST T2 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR  = '" + _ctrlYear + "' AND ");
                stb.append("    T1.GRADE = '" + _grade + "' AND ");
                stb.append("    T1.GRADE = T2.GRADE AND ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("    T1.CLASSCD       = '" + _sendClassCd + "' AND ");
                    stb.append("    T1.SCHOOL_KIND   = '" + _sendSchoolKind + "' AND ");
                    stb.append("    T1.CURRICULUM_CD = '" + _sendCurriculumCd + "' AND ");
                    stb.append("    T1.SUBCLASSCD    = '" + _sendSubclasscd + "' AND ");
                    stb.append("    T1.CLASSCD       = T2.CLASSCD AND ");
                    stb.append("    T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ");
                    stb.append("    T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ");
                    stb.append("    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ");
                } else {
                    stb.append("    T1.SUBCLASSCD    = '" + _sendSubclasscd + "' AND ");
                    stb.append("    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ");
                }
                stb.append("    T1.VIEWCD = T2.VIEWCD AND ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("    SUBSTR(T1.VIEWCD,1,2)    = '" + _sendClassCd + "' ");
                } else {
                    stb.append("    SUBSTR(T1.VIEWCD,1,2)    = '" + _sendSubclasscd.substring(0, 2) + "' ");
                }
                stb.append(" ORDER BY ");
                stb.append("    T1.VIEWCD ");
            } catch (Exception ex) {
                log.warn("statementJviewname error!", ex);
            }
            return stb.toString();
        }
    }
}

// eof

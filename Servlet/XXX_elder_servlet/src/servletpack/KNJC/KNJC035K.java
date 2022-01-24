// kanji=漢字
/*
 * $Id: d4001cf10ca11d79ca79f18b5bc2e43dac3bbfb0 $
 *
 * 作成日: 2013/02/12 16:22:54 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: d4001cf10ca11d79ca79f18b5bc2e43dac3bbfb0 $
 */
public class KNJC035K {

    private static final Log log = LogFactory.getLog("KNJC035K.class");

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJC035K.frm", 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
            svf.VrsOut("SEMESTER", _param._semesterName);
            svf.VrsOut("ITEM1", _param._item1);
            svf.VrsOut("ITEM2", _param._item2);
            svf.VrsOut("ITEM3", _param._item3);
            svf.VrsOut("SUBCLASS", _param._subclassCd + ":" + _param._subclassName);
            svf.VrsOut("CHAIR", _param._chairName);
            
            final String sql = selectAttendQuery();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                svf.VrsOut("ATTENDNO", rs.getString("HR_ATTENDNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("APPOINTED_DAY", getAttendZeroHyoji(rs.getString("APPOINTED_DAY")));
                svf.VrsOut("LESSON", getAttendZeroHyoji(rs.getString("LESSON")));
                svf.VrsOut("OFFDAYS", getAttendZeroHyoji(rs.getString("OFFDAYS")));
                svf.VrsOut("ABROAD", getAttendZeroHyoji(rs.getString("ABROAD")));
                svf.VrsOut("ABSENT", getAttendZeroHyoji(rs.getString("ABSENT")));
                svf.VrsOut("SUSPEND", getAttendZeroHyoji(rs.getString("SUSPEND")));
                svf.VrsOut("MOURNING", getAttendZeroHyoji(rs.getString("MOURNING")));
                svf.VrsOut("ABSENCE1", getAttendZeroHyoji(rs.getString("SICK")));
                svf.VrsOut("ABSENCE2", getAttendZeroHyoji(rs.getString("NOTICE")));
                svf.VrsOut("ABSENCE3", getAttendZeroHyoji(rs.getString("NONOTICE")));
                svf.VrsOut("ABSENCE4", getAttendZeroHyoji(rs.getString("NURSEOFF")));
                svf.VrsOut("LATE", getAttendZeroHyoji(rs.getString("LATE")));
                svf.VrsOut("EARLY", getAttendZeroHyoji(rs.getString("EARLY")));

                svf.VrEndRecord();
                
                _hasData = true;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    //プロパティ「use_Attend_zero_hyoji」= '1'のとき、データの通りにゼロ、NULLを表示
    //それ以外のとき、ゼロは表示しない
    private String getAttendZeroHyoji(final String val) {
        if ("1".equals(_param._use_Attend_zero_hyoji)) return val;
        if ("0".equals(val) || "0.0".equals(val)) return "";
        return val;
    }

    /**
     * @return
     */
    private String selectAttendQuery() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_INFO AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_ATTENDNO, ");
        stb.append("         T2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1, ");
        stb.append("         SCHREG_BASE_MST T2, ");
        stb.append("         SCHREG_REGD_HDAT T3, ");
        stb.append("         CHAIR_STD_DAT T4 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = T3.YEAR AND ");
        stb.append("         T1.YEAR     = T4.YEAR AND ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = T3.SEMESTER AND ");
        stb.append("         T1.SEMESTER = T4.SEMESTER AND ");
        stb.append("         T1.SEMESTER = '" + _param._monthSem + "' AND ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("         T1.SCHREGNO = T4.SCHREGNO AND ");
        stb.append("         T1.GRADE    = T3.GRADE AND ");
        stb.append("         T1.HR_CLASS = T3.HR_CLASS AND ");
        stb.append("         T4.CHAIRCD  = '" + _param._chairCd + "' ");
        if (StringUtils.isNumeric(_param._month)) {
            stb.append("         AND MONTH(T4.APPDATE) <= " + Integer.parseInt(_param._month) + " + CASE WHEN " + Integer.parseInt(_param._month) + " < 4 THEN 12 ELSE 0 END AND ");
            stb.append("         " + Integer.parseInt(_param._month) + " <= MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ");
            stb.append("         AND CASE WHEN " + Integer.parseInt(_param._month) + " < 4 THEN " + Integer.parseInt(_param._month) + "+12 ELSE " + Integer.parseInt(_param._month) + " END ");
            stb.append("         BETWEEN ");
            stb.append("             CASE WHEN MONTH(T4.APPDATE) < 4 THEN MONTH(T4.APPDATE)+12 ELSE MONTH(T4.APPDATE) END ");
            stb.append("         AND ");
            stb.append("             CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN MONTH(T4.APPENDDATE)+12 ELSE MONTH(T4.APPENDDATE) END ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T2.APPOINTED_DAY, ");
        stb.append("     T2.LESSON, ");
        stb.append("     T2.OFFDAYS, ");
        stb.append("     T2.ABSENT, ");
        stb.append("     T2.SUSPEND, ");
        stb.append("     T2.MOURNING, ");
        stb.append("     T2.ABROAD, ");
        stb.append("     T2.SICK, ");
        stb.append("     T2.NOTICE, ");
        stb.append("     T2.NONOTICE, ");
        stb.append("     T2.NURSEOFF, ");
        stb.append("     T2.LATE, ");
        stb.append("     T2.EARLY ");
        stb.append(" FROM ");
        stb.append("     SCH_INFO T1 ");
        stb.append("     LEFT JOIN ATTEND_SUBCLASS_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO AND ");
        stb.append("                                         T2.YEAR         = '" + _param._year + "' AND  ");
        stb.append("                                         T2.SEMESTER     = '" + _param._monthSem + "' AND ");
        stb.append("                                         T2.MONTH        = '" + _param._month + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                                         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD   = '" + _param._subclassCd + "' ");
        } else {
            stb.append("                                         T2.CLASSCD      = '" + _param._subclassCd.substring(0, 2) + "' AND ");
            stb.append("                                         T2.SUBCLASSCD   = '" + _param._subclassCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.SCHREGNO ");

        return stb.toString();
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
        private final String _subclassCd;
        private final String _subclassName;
        private final String _chairCd;
        private final String _chairName;
        private final String _monthCd;
        private final String _month;
        private final String _monthSem;
        private final String _semesterName;
        private final String _ctrlDate;
        private final String _useCurriculumcd;
        private final String _use_Attend_zero_hyoji;
        
        private final KNJSchoolMst _knjSchoolMst;
        private String _item1;
        private String _item2;
        private String _item3;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _subclassName = getSubclassName(db2);
            _chairCd = request.getParameter("CHAIRCD");
            _chairName = getChairName(db2);
            _monthCd = request.getParameter("MONTHCD");
            _month = StringUtils.split(_monthCd, "-")[0];
            _monthSem = StringUtils.split(_monthCd, "-")[1];
            _semesterName = getSemesterName(db2, StringUtils.split(_monthCd, "-")[0], StringUtils.split(_monthCd, "-")[1]);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            setSickDiv(db2);
            _use_Attend_zero_hyoji = request.getParameter("use_Attend_zero_hyoji");
        }

        /**
         * @param db2
         * @param subclassCd
         * @return
         */
        private String getSubclassName(final DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("     SUBCLASSNAME ");
            sql.append(" FROM ");
            sql.append("     SUBCLASS_MST ");
            sql.append(" WHERE ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD   = '" + _subclassCd + "' ");
            } else {
                sql.append("     SUBCLASSCD   = '" + _subclassCd + "' ");
            }
            
            String subclassName = "";
            log.debug(" semester sql = " + sql.toString());
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                subclassName = rs.getString("SUBCLASSNAME");
            }
            return subclassName;
        }

        /**
         * @param db2
         * @param chairCd
         * @return
         */
        private String getChairName(final DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("     CHAIRCD || ':' || VALUE(CHAIRNAME, '') AS CHAIRNAME ");
            sql.append(" FROM ");
            sql.append("     CHAIR_DAT ");
            sql.append(" WHERE ");
            sql.append("     YEAR = '" + _year + "' ");
            sql.append("     AND SEMESTER = '" + _semester + "' ");
            sql.append("     AND CHAIRCD = '" + _chairCd + "' ");
            
            String chairName = "";
            log.debug(" semester sql = " + sql.toString());
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                chairName = rs.getString("CHAIRNAME");
            }
            return chairName;
        }

        private String getSemesterName(DB2UDB db2, String month, String semester) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("     t1.namecd2, t1.name1, t1.namespare1, t2.semestername ");
            sql.append(" FROM ");
            sql.append("     name_mst t1, semester_mst t2");
            sql.append(" WHERE ");
            sql.append("     t1.namecd1 = 'Z005' ");
            sql.append("     AND t1.namecd2 = '" + month + "' ");
            sql.append("     AND t2.year  = '" + _year + "' ");
            sql.append("     AND t2.semester  = '" + semester + "' ");
            sql.append(" ORDER BY ");
            sql.append("     namespare1 ");
            
            String semesterName = null;
            log.debug(" semester sql = " + sql.toString());
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String monthName = rs.getString("NAME1");
                semesterName = rs.getString("SEMESTERNAME");
                if (semesterName != null) {
                    semesterName = semesterName + " " + monthName;
                } else {
                    semesterName = monthName;
                }
            }
            return semesterName;
        }

        private void setSickDiv(DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     NAMECD2, ");
            sql.append("     NAME1 ");
            sql.append(" FROM ");
            sql.append("     V_NAME_MST ");
            sql.append(" WHERE ");
            sql.append("     YEAR = '" + _year + "' ");
            sql.append("     AND NAMECD1 = 'C001' ");
            sql.append("     AND NAMECD2 IN ('4', '5', '6') ");
            sql.append(" ORDER BY ");
            sql.append("     NAMECD2 ");
            
            _item1 = "";
            _item2 = "";
            _item3 = "";
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String cd = rs.getString("NAMECD2");
                if ("4".equals(cd)) _item1 = rs.getString("NAME1");
                if ("5".equals(cd)) _item2 = rs.getString("NAME1");
                if ("6".equals(cd)) _item3 = rs.getString("NAME1");
            }
        }
    }
}

// eof

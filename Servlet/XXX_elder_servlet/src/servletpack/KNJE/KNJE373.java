// kanji=漢字
/*
 * $Id: 72038083b2cffab08934412bcceda022cb5a2aed $
 *
 * 作成日: 2010/06/28 13:39:41 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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

public class KNJE373 {

    private static final Log log = LogFactory.getLog("KNJE373.class");

    private boolean _hasData;

    Param _param;

    private static final String FORM_FILE = "KNJE373.frm";

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
            log.debug("年組：" + _param._classSelectedIn);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List hrclassList = createHrClass(db2);
        for (final Iterator it = hrclassList.iterator(); it.hasNext();) {
            final HrClass hrclass = (HrClass) it.next();

            svf.VrSetForm(FORM_FILE, 1);
            printHeader(svf);
            printHrClass(svf, hrclass);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("NENDO"          , _param._nendo );
        svf.VrsOut("DATE"           , _param.getDateMD() );
        svf.VrsOut("WEEK"           , _param.getWeek() );
        svf.VrsOut("MAIN_COLLEGE"   , _param.getMainCollegeName() );
    }

    private void printHrClass(final Vrw32alp svf, final HrClass hrclass) {
        svf.VrsOut("HR_NAME"    , hrclass.getHrName() );
        svf.VrsOut("CNT2"       , hrclass._cnt2 );
        svf.VrsOut("CNT3"       , hrclass._cnt3 );
        svf.VrsOut("CNT4"       , hrclass._cnt4 );
        svf.VrsOut("CNT5"       , hrclass._cnt5 );
        svf.VrsOut("CNT8"       , hrclass._cnt8 );
        printCount7(svf, hrclass);
    }

    private void printCount7(final Vrw32alp svf, final HrClass hrclass) {
        int cnt6 = 0;
        int gyoCnt = 0;
        int gyo = 0;
        String retu = "";
        for (final Iterator it = hrclass._count7List.iterator(); it.hasNext();) {
            final Count7 count7 = (Count7) it.next();
            gyoCnt++;

            if (gyoCnt <= 8) {
                gyo = gyoCnt;
                retu = "1";
            } else if (gyoCnt <= 16) {
                gyo = gyoCnt - 8;
                retu = "2";
            } else if (gyoCnt <= 24) {
                gyo = gyoCnt - 16;
                retu = "3";
            } else {
                gyo = gyoCnt - 24;
                retu = "4";
            }

            String fieldNameCnt     = "PUBLIC_CNT"      + retu;
            String fieldNameCollege = "PUBLIC_COLLEGE"  + retu + count7.getFieldNo();

            svf.VrsOutn(fieldNameCnt        , gyo  , count7.getCnt7());
            svf.VrsOutn(fieldNameCollege    , gyo  , count7.getSchoolName());
            cnt6 += count7.getCnt7Int();
        }
        //集計６
        svf.VrsOut("CNT6"   , String.valueOf(cnt6) );
    }

    private List createHrClass(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            if (_param._isPrintGoukei && 0 < i) continue;
            final String selected = _param._classSelected[i];
            final String sql = sqlHrClass(selected);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = (_param._isPrintGoukei && _param._isPrintMark) ? "*" + rs.getString("HR_NAME") : rs.getString("HR_NAME");
                    final String cnt2 = rs.getString("CNT2");
                    final String cnt3 = rs.getString("CNT3");
                    final String cnt4 = rs.getString("CNT4");
                    final String cnt5 = rs.getString("CNT5");
                    final String cnt8 = rs.getString("CNT8");

                    final HrClass hrclass = new HrClass(grade, hrClass, hrName, cnt2, cnt3, cnt4, cnt5, cnt8);
                    hrclass.load(db2);
                    rtn.add(hrclass);
                }
            } catch (final Exception ex) {
                log.error("クラスのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        return rtn;
    }

    private String sqlHrClass(final String selected) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                                      AND T3.SEMESTER = T1.SEMESTER ");
        if (_param._isPrintGoukei) {
            stb.append("                                  AND T3.GRADE || T3.HR_CLASS IN " + _param._classSelectedIn + " ");
        } else {
            stb.append("                                  AND T3.GRADE = T1.GRADE ");
            stb.append("                                  AND T3.HR_CLASS = T1.HR_CLASS ");
        }
        stb.append("     WHERE ");
        stb.append("             T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        stb.append("     ) ");
        stb.append(" , T_AFT_GRAD_COURSE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         L1.SEQ, ");
        stb.append("         L1.SENKOU_KIND, ");
        stb.append("         L1.STAT_CD, ");
        stb.append("         L1.DECISION, ");
        stb.append("         L1.PLANSTAT, ");
        stb.append("         L2.SCHOOL_NAME, ");
        stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
        stb.append("         N1.NAMESPARE2 AS KOKKOURITU_FLG, ");
        stb.append("         case when N2.ABBV3 is not null then '1' end AS MAIN_COLLEGE_FLG ");
        stb.append("     FROM ");
        stb.append("         T_SCHREG T1 ");
        stb.append("         LEFT JOIN AFT_GRAD_COURSE_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
        stb.append("                                         AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.STAT_CD ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
        stb.append("                              AND N1.NAMECD2 = L2.SCHOOL_GROUP ");
        stb.append("         LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z010' ");
        stb.append("                              AND N2.NAMECD2 = '00' ");
        stb.append("                              AND N2.ABBV3 = L1.STAT_CD ");
        stb.append("     ) ");
        stb.append(" , T_CNT2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         HR_NAME, ");
        stb.append("         COUNT(SCHREGNO) AS CNT2 ");
        stb.append("     FROM ");
        stb.append("         T_SCHREG ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         HR_NAME ");
        stb.append("     ) ");
        stb.append(" , T_CNT3 AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COUNT(SCHREGNO) AS CNT3 ");
        stb.append("     FROM ");
        stb.append("         T_AFT_GRAD_COURSE ");
        stb.append("     WHERE ");
        stb.append("             SENKOU_KIND = '0' ");
        stb.append("         AND DECISION = '1' ");
        stb.append("         AND TAISHOU_FLG = '1' ");
        stb.append("         AND MAIN_COLLEGE_FLG = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append("     ) ");
        stb.append(" , T_CNT4 AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COUNT(DISTINCT SCHREGNO) AS CNT4 ");
        stb.append("     FROM ");
        stb.append("         T_AFT_GRAD_COURSE ");
        stb.append("     WHERE ");
        stb.append("             SENKOU_KIND = '0' ");
        stb.append("         AND PLANSTAT = '1' ");
        stb.append("         AND TAISHOU_FLG = '1' ");
        stb.append("         AND MAIN_COLLEGE_FLG = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append("     ) ");
        stb.append(" , T_CNT5 AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COUNT(SCHREGNO) AS CNT5 ");
        stb.append("     FROM ");
        stb.append("         T_AFT_GRAD_COURSE ");
        stb.append("     WHERE ");
        stb.append("             SENKOU_KIND = '0' ");
        stb.append("         AND DECISION = '1' ");
        stb.append("         AND TAISHOU_FLG = '1' ");
        stb.append("         AND MAIN_COLLEGE_FLG IS NULL ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append("     ) ");
        stb.append(" , T_CNT8 AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         COUNT(DISTINCT T1.SCHREGNO) AS CNT8 ");
        stb.append("     FROM ");
        stb.append("         T_AFT_GRAD_COURSE T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHREGNO NOT IN (SELECT W1.SCHREGNO ");
        stb.append("                               FROM T_AFT_GRAD_COURSE W1 ");
        stb.append("                              WHERE W1.PLANSTAT = '1' ");
        stb.append("                             GROUP BY W1.SCHREGNO) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.HR_NAME ");
        stb.append("    ,value(T2.CNT2,0) as CNT2 ");
        stb.append("    ,value(T3.CNT3,0) as CNT3 ");
        stb.append("    ,value(T4.CNT4,0) as CNT4 ");
        stb.append("    ,value(T5.CNT5,0) as CNT5 ");
        stb.append("    ,value(T8.CNT8,0) as CNT8 ");
        stb.append(" FROM ");
        stb.append("     T_CNT2 T2 ");
        stb.append("     LEFT JOIN T_CNT3 T3 ON T3.GRADE = T2.GRADE AND T3.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN T_CNT4 T4 ON T4.GRADE = T2.GRADE AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN T_CNT5 T5 ON T5.GRADE = T2.GRADE AND T5.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN T_CNT8 T8 ON T8.GRADE = T2.GRADE AND T8.HR_CLASS = T2.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _cnt2;
        private final String _cnt3;
        private final String _cnt4;
        private final String _cnt5;
        private final String _cnt8;

        private List _count7List;

        public HrClass(
                final String grade, 
                final String hrClass,
                final String hrName,
                final String cnt2,
                final String cnt3,
                final String cnt4,
                final String cnt5,
                final String cnt8
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _cnt2 = cnt2;
            _cnt3 = cnt3;
            _cnt4 = cnt4;
            _cnt5 = cnt5;
            _cnt8 = cnt8;
        }

        private String getHrName() {
            return (null == _hrName) ? "" : _hrName;
        }

        private void load(final DB2UDB db2) throws SQLException {
            _count7List = createCount7(db2);
        }

        private List createCount7(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String selected = _grade + _hrClass;
            final String sql = sqlCount7(selected);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String schoolCode = rs.getString("STAT_CD");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String cnt7 = rs.getString("CNT7");

                    final Count7 count7 = new Count7(grade, hrClass, schoolCode, schoolName, cnt7);
                    rtn.add(count7);
                }
            } catch (final Exception ex) {
                log.error("集計６・７のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlCount7(final String selected) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T3.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.HR_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_HDAT T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("                                      AND T3.SEMESTER = T1.SEMESTER ");
            if (_param._isPrintGoukei) {
                stb.append("                                  AND T3.GRADE || T3.HR_CLASS IN " + _param._classSelectedIn + " ");
            } else {
                stb.append("                                  AND T3.GRADE = T1.GRADE ");
                stb.append("                                  AND T3.HR_CLASS = T1.HR_CLASS ");
            }
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
            stb.append("     ) ");
            stb.append(" , T_AFT_GRAD_COURSE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SENKOU_KIND, ");
            stb.append("         L1.STAT_CD, ");
            stb.append("         L1.DECISION, ");
            stb.append("         L1.PLANSTAT, ");
            stb.append("         L2.SCHOOL_NAME, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS KOKKOURITU_FLG, ");
            stb.append("         case when N2.ABBV3 is not null then '1' end AS MAIN_COLLEGE_FLG ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG T1 ");
            stb.append("         LEFT JOIN AFT_GRAD_COURSE_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
            stb.append("                                         AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("         LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.STAT_CD ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L2.SCHOOL_GROUP ");
            stb.append("         LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z010' ");
            stb.append("                              AND N2.NAMECD2 = '00' ");
            stb.append("                              AND N2.ABBV3 = L1.STAT_CD ");
            stb.append("     ) ");
            stb.append(" , T_CNT7 AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         STAT_CD, ");
            stb.append("         SCHOOL_NAME, ");
            stb.append("         COUNT(SCHREGNO) AS CNT7 ");
            stb.append("     FROM ");
            stb.append("         T_AFT_GRAD_COURSE ");
            stb.append("     WHERE ");
            stb.append("             SENKOU_KIND = '0' ");
            stb.append("         AND DECISION = '1' ");
            stb.append("         AND TAISHOU_FLG = '1' ");
            stb.append("         AND KOKKOURITU_FLG = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         STAT_CD, ");
            stb.append("         SCHOOL_NAME ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     T7.GRADE, ");
            stb.append("     T7.HR_CLASS, ");
            stb.append("     T7.STAT_CD, ");
            stb.append("     T7.SCHOOL_NAME ");
            stb.append("    ,value(T7.CNT7,0) as CNT7 ");
            stb.append(" FROM ");
            stb.append("     T_CNT7 T7 ");
            stb.append(" ORDER BY ");
            stb.append("     value(T7.CNT7,0) desc, ");
            stb.append("     T7.STAT_CD ");
            return stb.toString();
        }

        public String toString() {
            return _grade + _hrClass + ":" + _hrName;
        }
    }

    private class Count7 {
        private final String _grade;
        private final String _hrClass;
        private final String _schoolCode;
        private final String _schoolName;
        private final String _cnt7;

        public Count7(
                final String grade, 
                final String hrClass,
                final String schoolCode,
                final String schoolName,
                final String cnt7
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _schoolCode = schoolCode;
            _schoolName = schoolName;
            _cnt7 = cnt7;
        }

        private String getSchoolName() {
            return (null == _schoolName) ? "" : _schoolName;
        }

        private String getFieldNo() {
            if (22 < getSchoolName().length()) return "_3";
            if (15 < getSchoolName().length()) return "_2";
            return "_1";
        }

        private String getCnt7() {
            return (null == _cnt7) ? "0" : _cnt7;
        }

        private int getCnt7Int() {
            return (null == _cnt7) ? 0 : Integer.parseInt(_cnt7);
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
        private final String _date;
        private final String[] _classSelected;
        private final String _classSelectedIn;
        private final boolean _isPrintGoukei;
        private final String _nendo;

        private boolean _isPrintMark; //複数クラス選択マーク「*」

        //系列大学
        private String _mainCollegeCode;
        private String _mainCollegeName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _classSelectedIn = getClassSelectedIn();
            String outDiv = request.getParameter("OUT_DIV"); // 1:総合計 2:クラス毎
            _isPrintGoukei = "1".equals(outDiv);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _nendo = gengou + "年度";
            setMainCollege(db2);
        }

        private String getClassSelectedIn() {
            StringBuffer stb = new StringBuffer();
            _isPrintMark = false;
            stb.append("(");
            for (int i = 0; i < _classSelected.length; i++) {
                if (0 < i) {
                    _isPrintMark = true;
                    stb.append(",");
                }
                stb.append("'" + _classSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        private String getDateMD() {
            if (null == _date || "".equals(_date)) return "";
            return KNJ_EditDate.h_format_JP_MD(_date);
        }

        private String getWeek() {
            if (null == _date || "".equals(_date)) return "";
            return KNJ_EditDate.h_format_W(_date);
        }

        private String getMainCollegeName() {
            return (null == _mainCollegeName) ? "" : _mainCollegeName;
        }

        private void setMainCollege(final DB2UDB db2) throws SQLException {
            _mainCollegeCode = null;
            _mainCollegeName = null;
            final String sql = sqlMainCollege();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mainCollegeCode = rs.getString("ABBV3");
                    _mainCollegeName = rs.getString("SCHOOL_NAME");
                }
            } catch (final Exception ex) {
                log.error("系列大学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlMainCollege() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.ABBV3, ");
            stb.append("     L2.SCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.ABBV3 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'Z010' AND ");
            stb.append("     L1.NAMECD2 = '00' ");
            return stb.toString();
        }

    }
}

// eof

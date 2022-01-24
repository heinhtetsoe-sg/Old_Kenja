// kanji=漢字
/*
 * $Id: 0d90bc37ca503365127f912c608d69d54619b5ae $
 *
 * 作成日: 2009/01/27
 * 作成者: nakamoto
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 遅刻欠課訂正情報チェックリスト。
 * @author nakamoto
 * @version $Id: 0d90bc37ca503365127f912c608d69d54619b5ae $
 */
public class KNJC036 {

    private static final String FORM_NAME = "KNJC036.frm";

    private static final Log log = LogFactory.getLog(KNJC036.class);

    Param _param;
    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            boolean hasData = false;

            svf.VrSetForm(FORM_NAME, 4);
            log.debug("印刷するフォーム:" + FORM_NAME);

            if (printMain(db2, svf)) hasData = true;

            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnflg = false;

        final List listStudent = createStudent(db2);
        log.debug("生徒数=" + listStudent.size());

        for (final Iterator its = listStudent.iterator(); its.hasNext();) {
            final Student rtnStudent = (Student) its.next();

            //ヘッダ
            printHeader(svf);
            //生徒情報
            printStudent(svf, rtnStudent);
            //遅刻・欠課・訂正情報
            printAttend(db2, svf, rtnStudent);

            svf.VrEndRecord();
            rtnflg = true;
        }

        return rtnflg;
    }

    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("NENDO"      , _param._gengou);
        svf.VrsOut("HR_NAME"    , _param._hrName);
        svf.VrsOut("DATE"       , _param._loginDate);
        svf.VrsOut("SEMESTER1"  , _param._semesterName1);
        svf.VrsOut("SEMESTER2"  , _param._semesterName2);
        svf.VrsOut("SEMESTER3"  , _param._semesterName3);
    }

    private void printStudent(final Vrw32alp svf, final Student rtnStudent) {
        svf.VrsOut("ATTENDNO"   , rtnStudent.getAttendnoInt());
        svf.VrsOut("NAME"       , rtnStudent._name);
    }

    private void printAttend(final DB2UDB db2, final Vrw32alp svf, final Student rtnStudent) throws SQLException {
        final String sql = sqlAttend(rtnStudent._schregno);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            int totalLate   = 0;
            int totalJisuu  = 0;
            int totalKekka  = 0;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String seme = rs.getString("SEMESTER");
                svf.VrsOut("LATE"           + seme , getZeroToBlank(rs.getInt("LATE")) );        //遅刻回数
                svf.VrsOut("LATE_DETAIL"    + seme , getZeroToBlank(rs.getInt("LATE_COR")) );    //遅刻訂正
                svf.VrsOut("LATE_DECISION"  + seme , getZeroToBlank(rs.getInt("LATE_FIX")) );    //遅刻確定
                svf.VrsOut("LACKTIME"       + seme , getZeroToBlank(rs.getInt("JISUU")) );       //欠時数
                svf.VrsOut("KEKKA"          + seme , getZeroToBlank(rs.getInt("KEKKA")) );       //欠課回数
                svf.VrsOut("KEKKA_DETAIL"   + seme , getZeroToBlank(rs.getInt("KEKKA_COR")) );   //欠課訂正
                svf.VrsOut("KEKKA_DECISION" + seme , getZeroToBlank(rs.getInt("KEKKA_FIX")) );   //欠課確定
                totalLate  += rs.getInt("LATE_FIX");
                totalJisuu += rs.getInt("JISUU");
                totalKekka += rs.getInt("KEKKA_FIX");
            }
            svf.VrsOut("TOTAL_LATE"     , getZeroToBlank(totalLate) );  //遅刻（年間）
            svf.VrsOut("TOTAL_LACKTIME" , getZeroToBlank(totalJisuu) ); //欠時数（年間）
            svf.VrsOut("TOTAL_KEKKA"    , getZeroToBlank(totalKekka) ); //欠課回数（年間）
        } catch (final Exception ex) {
            log.error("遅刻・欠課・訂正情報の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String getZeroToBlank(final int value) {
        final String retVal;
        retVal = (value == 0) ? "" : String.valueOf(value) ;
        return retVal;
    }
    
    private String sqlAttend(final String schregno) {
        // 欠課時数。学校マスタを参照し欠課に含める。
        String attendStr = "value(SICK,0) + value(NOTICE,0) + value(NONOTICE,0) + value(NURSEOFF,0)";
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            attendStr += " + value(OFFDAYS,0)";
        }
        if ("1".equals(_param._knjSchoolMst._subAbsent)) {
            attendStr += " + value(ABSENT,0)";
        }
        if ("1".equals(_param._knjSchoolMst._subSuspend)) {
            attendStr += " + value(SUSPEND,0)";
        }
        if ("1".equals(_param._knjSchoolMst._subMourning)) {
            attendStr += " + value(MOURNING,0)";
        }
        if ("1".equals(_param._knjSchoolMst._subVirus)) {
            attendStr += " + value(VIRUS,0)";
        }
        final String sql;
        sql = ""
            + " WITH ATTEND_SEMES AS ( "
            + "     SELECT "
            + "         SCHREGNO, "
            + "         SEMESTER, "
            + "         sum(KEKKA) as KEKKA "
            + "     FROM "
            + "         ATTEND_SEMES_DAT "
            + "     WHERE "
            + "         YEAR='" + _param._year + "' "
            + "         AND SCHREGNO = '" + schregno + "' "
            + "     GROUP BY  "
            + "         SCHREGNO, "
            + "         SEMESTER "
            + "     ) "
            + " , ATTEND_SUBCLASS AS ( "
            + "     SELECT "
            + "         SCHREGNO, "
            + "         SEMESTER, "
            + "         sum(LATE) as LATE, "
            + "         sum(" + attendStr + ") as JISUU "
            + "     FROM "
            + "         ATTEND_SUBCLASS_DAT "
            + "     WHERE "
            + "         YEAR='" + _param._year + "' "
            + "         AND SCHREGNO = '" + schregno + "' "
            + "     GROUP BY  "
            + "         SCHREGNO, "
            + "         SEMESTER "
            + "     ) "
            + " , ATTEND_CORRECTION AS ( "
            + "     SELECT "
            + "         SCHREGNO, "
            + "         SEMESTER, "
            + "         sum(LATEDETAIL) as LATEDETAIL, "
            + "         sum(KEKKADETAIL) as KEKKADETAIL "
            + "     FROM "
            + "         ATTEND_CORRECTION_DAT "
            + "     WHERE "
            + "         YEAR='" + _param._year + "' "
            + "         AND SCHREGNO = '" + schregno + "' "
            + "     GROUP BY  "
            + "         SCHREGNO, "
            + "         SEMESTER "
            + "     ) "

            + " SELECT "
            + "     T1.SCHREGNO, "
            + "     T1.SEMESTER, "
            + "     value(L3.LATE,0) as LATE, "
            + "     value(L4.LATEDETAIL,0) as LATE_COR, "
            + "     value(L3.LATE,0) + value(L4.LATEDETAIL,0) as LATE_FIX, "
            + "     value(L3.JISUU,0) as JISUU, "
            + "     value(L2.KEKKA,0) as KEKKA, "
            + "     value(L4.KEKKADETAIL,0) as KEKKA_COR, "
            + "     value(L2.KEKKA,0) + value(L4.KEKKADETAIL,0) as KEKKA_FIX "
            + " FROM "
            + "     SCHREG_REGD_DAT T1 "
            + "      LEFT JOIN ATTEND_SEMES L2  "
            + "             ON L2.SCHREGNO = T1.SCHREGNO "
            + "            AND L2.SEMESTER = T1.SEMESTER "
            + "      LEFT JOIN ATTEND_SUBCLASS L3  "
            + "             ON L3.SCHREGNO = T1.SCHREGNO "
            + "            AND L3.SEMESTER = T1.SEMESTER "
            + "      LEFT JOIN ATTEND_CORRECTION L4  "
            + "             ON L4.SCHREGNO = T1.SCHREGNO "
            + "            AND L4.SEMESTER = T1.SEMESTER "
            + " WHERE "
            + "     T1.YEAR = '" + _param._year + "' AND "
            + "     T1.SCHREGNO = '" + schregno + "' "
            + " ORDER BY "
            + "     T1.SEMESTER "
            ;
        return sql;
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String hrClass = request.getParameter("HR_CLASS");
        final String loginDate = request.getParameter("CTRL_DATE");

        final Param param = new Param(
                year,
                semester,
                hrClass,
                loginDate
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _hrClass;
        private final String _loginDate;
        private final String _gengou;

        private String _semesterName1;
        private String _semesterName2;
        private String _semesterName3;
        private String _hrName;

        private KNJSchoolMst _knjSchoolMst;

        public Param(
                final String year,
                final String semester,
                final String hrClass,
                final String loginDate
        ) {
            _year = year;
            _semester = semester;
            _hrClass = hrClass;
            _loginDate = KNJ_EditDate.h_format_JP(loginDate);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";
        }

        public void load(final DB2UDB db2) {
            setHrName(db2);
            setSemesterName(db2);
            loadKNJSchoolMst(db2);
        }

        public void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST" +
                               " WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' ORDER BY SEMESTER";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String tmpSeme      = rs.getString("SEMESTER");
                    String tmpSemeName  = rs.getString("SEMESTERNAME");
                    if ("1".equals(tmpSeme)) _semesterName1 = tmpSemeName;
                    if ("2".equals(tmpSeme)) _semesterName2 = tmpSemeName;
                    if ("3".equals(tmpSeme)) _semesterName3 = tmpSemeName;
                }
            } catch (final Exception ex) {
                log.error("学期名の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setHrName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT HR_NAME FROM SCHREG_REGD_HDAT" +
                               " WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'" +
                               "   AND GRADE || HR_CLASS = '" + _hrClass + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrName = rs.getString("HR_NAME");
                }
            } catch (final Exception ex) {
                log.error("クラス名の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void loadKNJSchoolMst(final DB2UDB db2) {
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.error("学校マスタ取得でエラー", e);
            } catch (Exception e) {
                log.error("loadKNJSchoolMst exception", e);
            }
        }
    }

    private List createStudent(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudent();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");

                final Student rtnStudent = new Student(schregno, attendno, name);
                rtn.add(rtnStudent);
            }
        } catch (final Exception ex) {
            log.error("生徒の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlStudent() {
        final String sql;
        sql = ""
            + " SELECT "
            + "     T1.SCHREGNO, "
            + "     T1.ATTENDNO, "
            + "     L1.NAME_SHOW AS NAME "
            + " FROM "
            + "     SCHREG_REGD_DAT T1 "
            + "     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO "
            + " WHERE "
            + "     T1.YEAR = '" + _param._year + "' AND "
            + "     T1.SEMESTER = '" + _param._semester + "' AND "
            + "     T1.GRADE || T1.HR_CLASS = '" + _param._hrClass + "' "
            + " ORDER BY "
            + "     T1.ATTENDNO "
            ;
        return sql;
    }

    private class Student {
        private final String _schregno;
        private final String _attendno;
        private final String _name;

        public Student(
                final String schregno,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
        }

        public String getAttendnoInt() {
            return String.valueOf(Integer.parseInt(_attendno));
        }

        public String toString() {
            return _attendno + ":" + _name;
        }
    }
}

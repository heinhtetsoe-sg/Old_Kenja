// kanji=漢字
/*
 * $Id: 7a8d6fec6d7cc52ff1c5cba0f495fc05442d8852 $
 *
 */
package servletpack.KNJG;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 出欠席の記録
 */

public class KNJG105 {

    private static final Log log = LogFactory.getLog(KNJG105.class);
    private Param _param;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        boolean nonedata = false;
        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = createParam(request, db2);

            // 印刷処理
            nonedata = printMain(db2, svf, _param._gradeHrclass);

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            // 終了処理
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return rtn;
    }

    /**
     *  印刷処理
     */
    private boolean printMain (
            final DB2UDB db2,
            final Vrw32alp svf,
            final String grade
    ) {
        boolean nonedata = false;

        final List studentList = Student.loadStudentList(db2, _param);
        // 生徒がいなければ処理をスキップ
        if (studentList.size() == 0) {
            return false;
        }
        setAttendData(db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            final DayAttendance att = student._daysAttendance;
            final boolean isPrintRemark = null != att && att._sick >= 5; // 5日以上
            final String form;
            if (isPrintRemark) {
                form = "KNJG105_2.frm";
            } else {
                form = "KNJG105.frm";
            }

            svf.VrSetForm(form, 1);

            svf.VrsOut("HR_NAME", student._hrName); // クラス名称
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : "1"), student._name); // 氏名
            if (NumberUtils.isDigits(student._grade)) {
                svf.VrsOut("GRADE", KNJ_EditEdit.convertZenkakuSuuji(String.valueOf(Integer.parseInt(student._grade))) + "年次"); // 学年名称
            }
            svf.VrsOut("PERIOD", _param._attendRange); // 期間
            if (null != att) {
                svf.VrsOut("LESSON", String.valueOf(att._lesson)); // 授業日数
                svf.VrsOut("SUSPEND", String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停忌引日数
                svf.VrsOut("MUST", String.valueOf(att._mlesson)); // 出席しなければならない日数
                svf.VrsOut("NOTICE", String.valueOf(att._sick)); // 欠席日数
                svf.VrsOut("ATTEND", String.valueOf(att._attend)); // 出席日数
                svf.VrsOut("LATE", String.valueOf(att._late)); // 遅刻
                svf.VrsOut("EARLY", String.valueOf(att._early)); // 早退
            }
            if (isPrintRemark) {
                svf.VrsOut("REASON", student._baseRemark1); // 早退
            }
            svf.VrsOut("DATE", KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_SeirekiJP(_param._kisaiDate))); // 日付
            svf.VrsOut("PRINT_PERSON" + (getMS932ByteLength(_param._kisaiStaffname) > 30 ? "2" : "1"), _param._kisaiStaffname); // 記載者名
            svf.VrsOut("SCHOOL_NAME1", StringUtils.defaultString(_param._certifSchoolDatRemark1) + "　" + StringUtils.defaultString(_param._certifSchoolDatRemark2)); // 学校名称 学校法人 武蔵野東学園
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolDatSchoolName); // 学校名称 武蔵野東小学校
            svf.VrsOut("JOB_NAME", _param._certifSchoolDatJobName); // 役職名
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolDatPrincipalName); // 校長名
            nonedata = true;

            svf.VrEndPage();
        }
        return nonedata;
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private void setAttendData(final DB2UDB db2, final Collection studentList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = null;
        try {
            // 出欠の情報
            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, _param._ctrlYear, _param._sdate, _param._date);
            log.debug(" hasuuMap = " + hasuuMap);

            final String semesInState = (String) hasuuMap.get("attendSemesInState");
            // 1日単位
            sql = getAttendSemesDat(semesInState);

            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final int lesson   = rs.getInt("LESSON"); // 授業日数
                    final int sick     = rs.getInt("SICK"); // 病欠日数
                    final int mourning = rs.getInt("MOURNING");
                    final int suspend = rs.getInt("SUSPEND");
                    final int virus = rs.getInt("VIRUS");
                    final int koudome = rs.getInt("KOUDOME");
                    final int mlesson  = lesson - (mourning + suspend + virus + koudome); // 出席すべき日数
                    student._daysAttendance._lesson   += lesson;
                    student._daysAttendance._mourning += mourning;
                    student._daysAttendance._suspend  += suspend;
                    student._daysAttendance._virus    += virus;
                    student._daysAttendance._koudome  += koudome;
                    student._daysAttendance._mlesson  += mlesson;
                    student._daysAttendance._sick     += sick;
                    student._daysAttendance._attend   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
                    student._daysAttendance._late     += rs.getInt("LATE");
                    student._daysAttendance._early    += rs.getInt("EARLY");

                }
                DbUtils.closeQuietly(rs);
            }
            DbUtils.closeQuietly(ps);

            final String aftDayFrom = (String) hasuuMap.get("aftDayFrom");
            final String aftDayTo = (String) hasuuMap.get("aftDayTo");

            if (null != aftDayFrom && null != aftDayTo) {
                String sqlAttend = getAttendDayDatSql(semesInState, _param._semeMonth, aftDayFrom, aftDayTo);
                ps = db2.prepareStatement(sqlAttend);
                ps.setString(1, _param._gradeHrclass.substring(0, 2));
                ps.setString(2, _param._gradeHrclass.substring(2));

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(3, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int lesson   = rs.getInt("LESSON"); // 授業日数
                        final int sick     = rs.getInt("SICK"); // 病欠日数
                        final int mourning = rs.getInt("MOURNING");
                        final int suspend = rs.getInt("SUSPEND");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");
                        final int mlesson  = lesson - (mourning + suspend + virus + koudome); // 出席すべき日数
                        student._daysAttendance._lesson   += lesson;
                        student._daysAttendance._mourning += mourning;
                        student._daysAttendance._suspend  += suspend;
                        student._daysAttendance._virus  += virus;
                        student._daysAttendance._koudome  += koudome;
                        student._daysAttendance._mlesson  += mlesson;
                        student._daysAttendance._sick     += sick;
                        student._daysAttendance._attend   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
                        student._daysAttendance._late     += rs.getInt("LATE");
                        student._daysAttendance._early    += rs.getInt("EARLY");
                    }
                    DbUtils.closeQuietly(rs);
                }
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private String getAttendSemesDat(final String semesInState) {
        //月別集計データから集計した表
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ");
        stb.append("        SCHREGNO, ");
        stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
        if (null != _param._knjSchoolMst._semOffDays && _param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append("           + VALUE(OFFDAYS, 0) ");
        }
        stb.append("        ) AS LESSON, ");
        stb.append("        SUM(MOURNING) AS MOURNING, ");
        stb.append("        SUM(SUSPEND) AS SUSPEND, ");
        stb.append("        SUM(ABSENT) AS ABSENT, ");
        stb.append("        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if (null != _param._knjSchoolMst._semOffDays && _param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append("           + VALUE(OFFDAYS, 0) ");
        }
        stb.append("        ) AS SICK, ");
        stb.append("        SUM(LATE) AS LATE, ");
        stb.append("        SUM(EARLY) AS EARLY, ");
        if ("true".equals(_param._useVirus)) {
            stb.append("        SUM(VIRUS) AS VIRUS, ");
        } else {
            stb.append("        0 AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append("        SUM(KOUDOME) AS KOUDOME, ");
        } else {
            stb.append("        0 AS KOUDOME, ");
        }
        stb.append("        SUM(ABROAD) AS ABROAD, ");
        stb.append("        SUM(OFFDAYS) AS OFFDAYS ");
        stb.append("    FROM ");
        stb.append("        V_ATTEND_SEMES_DAT W1 ");
        stb.append("    WHERE ");
        stb.append("        W1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("        AND W1.SEMESTER || W1.MONTH IN " + semesInState + " ");
        stb.append("        AND W1.SCHREGNO = ? ");
        stb.append("    GROUP BY ");
        stb.append("        W1.SCHREGNO ");
        return stb.toString();
    }

    private String getAttendDayDatSql(final String semesInState, final String semeMonth, final String sdate, final String edate) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHEDULES AS (SELECT ");
        stb.append("     T1.MONTH, ");
        stb.append("     SUM(T1.LESSON) AS LESSON ");
        stb.append("   FROM ");
        stb.append("     ATTEND_SEMES_LESSON_DAT T1 ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER || T1.MONTH NOT IN " + semesInState + " ");
        stb.append("     AND T1.GRADE = ? ");
        stb.append("     AND T1.HR_CLASS = ? ");
        stb.append("     AND (CASE WHEN INT(T1.MONTH) < 4 THEN CAST((INT(T1.SEMESTER) + 1) AS CHAR(1)) ELSE T1.SEMESTER END) || T1.MONTH <= '" + semeMonth + "' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.MONTH ");
        stb.append(" ), ATTEND_SEMES AS (SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MONTH(T1.ATTENDDATE) AS IMONTH, ");
        stb.append("     MAX(VALUE(T4.LESSON,0)) AS LESSON, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
        if ("true".equals(_param._useVirus)) {
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
        } else {
            stb.append("     0 AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ");
        } else {
            stb.append("     0 AS KOUDOME, ");
        }
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('3', '10') THEN 1 ELSE 0 END) AS MOURNING, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('4', '11') THEN 1 ELSE 0 END) AS SICK, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('5', '12') THEN 1 ELSE 0 END) AS NOTICE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('6', '13') THEN 1 ELSE 0 END) AS NONOTICE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('15','23','24') THEN 1 ELSE 0 END) AS LATE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('16') THEN 1 ELSE 0 END) AS EARLY, ");
        stb.append("     0 AS KEKKA_JISU ");
        stb.append("   FROM ");
        stb.append("     SCHEDULES T4 ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T1 ON T1.SCHREGNO = ? ");
        stb.append("         AND INT(T4.MONTH) = MONTH(T1.ATTENDDATE) ");
        stb.append("         AND T1.ATTENDDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO, MONTH(T1.ATTENDDATE) ");
        stb.append(" ) ");
        stb.append("  SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    SUM(T1.LESSON) AS LESSON, ");
        stb.append("    SUM(T1.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(T1.VIRUS) AS VIRUS, ");
        stb.append("    SUM(T1.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(T1.MOURNING) AS MOURNING, ");
        stb.append("    SUM(T1.SICK) AS SICK, ");
        stb.append("    SUM(T1.NOTICE) AS NOTICE, ");
        stb.append("    SUM(T1.NONOTICE) AS NONOTICE, ");
        stb.append("    SUM(T1.LATE) AS LATE, ");
        stb.append("    SUM(T1.EARLY) AS EARLY, ");
        stb.append("    SUM(T1.KEKKA_JISU) AS KEKKA_JISU ");
        stb.append("  FROM ");
        stb.append("    ATTEND_SEMES T1 ");
        stb.append("  GROUP BY ");
        stb.append("    T1.SCHREGNO ");

        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grade;
        final String _hrClass;
        DayAttendance _daysAttendance;
        String _baseRemark1;

        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex,
                final String grade,
                final String hrClass) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _grade = grade;
            _hrClass = hrClass;
            _daysAttendance = new DayAttendance();
        }

        public static List loadStudentList(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List studentList = new ArrayList();

            try {
                // HRの生徒を取得
                final String sql = sqlSchregRegdDat(param);
                //log.debug("schreg_regd_dat sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student st = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("HR_NAME"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("GRADE"),
                            rs.getString("HR_CLASS"));
                    st._baseRemark1 = rs.getString("BASE_REMARK1");
                    studentList.add(st);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info(" studentList size = " + studentList.size());
            return studentList;
        }

        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T4.BASE_REMARK1 ");
            stb.append(" FROM ");
            if ("2".equals(param._hrClassType)) {
                stb.append("     SCHREG_REGD_FI_DAT T1");
            } else {
                stb.append("     SCHREG_REGD_DAT T1");
            }
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            if ("2".equals(param._hrClassType)) {
                stb.append("     INNER JOIN SCHREG_REGD_FI_HDAT T3 ON ");
            } else {
                stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            }
            stb.append("         T1.YEAR = T3.YEAR ");
            stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T1.GRADE = T3.GRADE ");
            stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T4.BASE_SEQ = '005' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            return stb.toString();
        }
    }

    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lesson;
        /** 忌引日数 */
        private int _mourning;
        /** 出停日数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 出席すべき日数 */
        private int _mlesson;
        /** 欠席日数 */
        private int _sick;
        /** 出席日数 */
        private int _attend;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return
            "LESSON=" + df5.format(_lesson)
            + ", MOR=" + df5.format(_mourning)
            + ", SSP=" + df5.format(_suspend)
            + ", MLS=" + df5.format(_mlesson)
            + ", SCK=" + df5.format(_sick)
            + ", ATE=" + df5.format(_attend)
            + ", LAT=" + df5.format(_late)
            + ", EAL=" + df5.format(_early);
        }
    }

    private static Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.debug(" $Id: 7a8d6fec6d7cc52ff1c5cba0f495fc05442d8852 $ ");
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        private static final String FROM_TO_MARK = "\uFF5E";

        final String _ctrlYear;
        final String _semester;
        final String _sdate;
        final String _date;
        final String _kisaiDate;
        final String _kisaiStaff;
        final String _kisaiStaffname;
        final String _dateMonth;
        final String _dateDayOfMonth;
        final String _ctrlDate;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _attendRange;
//        final String _useTestCountflg;

        private KNJSchoolMst _knjSchoolMst;
        final String _hrClassType; // 1:法定クラス 2:FIクラス
        final String _semeMonth;

//        /** 教育課程コードを使用するか */
//        final String _useCurriculumcd;

        final String _useVirus;
        final String _useKoudome;

        final String _certifSchoolDatRemark1;
        final String _certifSchoolDatRemark2;
        final String _certifSchoolDatSchoolName;
        final String _certifSchoolDatJobName;
        final String _certifSchoolDatPrincipalName;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _sdate = getSdate(db2);
            _date = request.getParameter("DATE").replace('/', '-');
            _dateMonth = StringUtils.split(_date, '-')[1];
            _dateDayOfMonth = StringUtils.split(_date, '-')[2];
            _kisaiDate = request.getParameter("KISAI_DATE").replace('/', '-');
            _kisaiStaff = request.getParameter("KISAI_STAFF");
            _kisaiStaffname = getStaffname(db2, _kisaiStaff);
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
//            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
//            _useTestCountflg = request.getParameter("useTestCountflg");
            _certifSchoolDatRemark1 = getCertifSchoolDat(db2, "REMARK1");
            _certifSchoolDatRemark2 = getCertifSchoolDat(db2, "REMARK2");
            _certifSchoolDatSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolDatJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _certifSchoolDatPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _semeMonth = getSemeMonth(db2, _date);
            _attendRange = getAttendRange(_sdate, _date);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
        }

        private String getAttendRange(final String sdate, final String date) {
            final Calendar scal = Calendar.getInstance();
            scal.setTime(Date.valueOf(sdate));
            
            final String syear = String.valueOf(scal.get(Calendar.YEAR)) + "年";
            final String smonth = String.valueOf(1 + scal.get(Calendar.MONTH)) + "月";

            final Calendar cal = Calendar.getInstance();
            cal.setTime(Date.valueOf(date));

            final String emonth = String.valueOf(1 + cal.get(Calendar.MONTH)) + "月";
            return KNJ_EditEdit.convertZenkakuSuuji(syear + smonth + "〜" + emonth);
        }

        private String getSemeMonth(final DB2UDB db2, final String date) {
            final String sql = "SELECT SEMESTER, SDATE, EDATE, CASE WHEN '" + date + "' BETWEEN SDATE AND EDATE THEN '1' END AS HAS_BETWEEN FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER <> '9' ORDER BY SEMESTER ";

            String rsSemester = null;
            boolean hasBetween = false;
            String semFirstSdate = null;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("1".equals(rs.getString("SEMESTER"))) {
                        semFirstSdate = rs.getString("SDATE");
                    }
                    rsSemester = rs.getString("SEMESTER");
                    if ("1".equals(rs.getString("HAS_BETWEEN"))) {
                        hasBetween = true;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            String semester = rsSemester;
            if (!hasBetween && null != semFirstSdate && date.compareTo(semFirstSdate) < 0) { // 1学期開始日より前日を指定した場合、1学期
                semester = "1";
            }
            String semeMonth;
            if (Integer.parseInt(_dateMonth) < 4) {
                semeMonth = String.valueOf(Integer.parseInt(semester) + 1) + _dateMonth;
            } else {
                semeMonth = semester + _dateMonth;
            }
            return semeMonth;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String fieldname) {
            String rtn = null;

            final String certifKindCd = "132";
            final String sql = "SELECT " + fieldname + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKindCd + "'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(fieldname);
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2, final String staffcd) {
            String rtn = null;

            final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + staffcd + "' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSdate(final DB2UDB db2) {
            String rtn = null;

            final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '1' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SDATE");
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// kanji=漢字
/*
 * $Id: 98a252185ecab7fb5c23e5a9bdf5fb881092e912 $
 *
 */
package servletpack.KNJG;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 皆勤賞・精勤賞
 */

public class KNJG102 {

    private static final Log log = LogFactory.getLog(KNJG102.class);
    private Param _param;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        log.debug(" $Id: 98a252185ecab7fb5c23e5a9bdf5fb881092e912 $ ");
        KNJServletUtils.debugParam(request, log);

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
            _param = new Param(request, db2);

            // 印刷処理
            for (int i = 0; i < _param._grades.length; i++) {
                final String grade = _param._grades[i];
                nonedata = printMain(db2, svf, grade) || nonedata;
            }

            for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }

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

        String schoolStampPath = null;
        final File imgfile = new File(_param._documentroot + "/" + _param._imagePath + "SCHOOLSTAMP_" + _param.getSchoolKind(db2, grade) + "." + _param._extension);
        if (imgfile.exists()) {
            schoolStampPath = imgfile.getAbsolutePath();
        } else {
            log.warn("file not found:" + imgfile.getAbsolutePath());
        }

        final List studentList = Student.loadStudentList(db2, _param, grade);
        // 生徒がいなければ処理をスキップ
        if (studentList.size() == 0) {
            return false;
        }
        for (final Iterator it = getHrClassMap(studentList).entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String hrClass = (String) e.getKey();
            final Collection hrClassStudentList = (Collection) e.getValue();
            //log.debug(" set attend " + grade + " : " + hrClass);
            DayAttendance.setAttendData(db2, _param, hrClassStudentList, grade, hrClass);
        }

        final String kisaiDateString = Param.getDateString(_param._kisaiDate);

        int noTargetCount = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (!isTarget(student, _param)) {
                noTargetCount += 1;
                continue;
            }

            svf.VrSetForm("KNJG102.frm", 1);

            nonedata = true;

            svf.VrsOut("DATE", kisaiDateString);
            svf.VrsOut("HR_NAME", student._hrName);

            if ("1".equals(_param._kanaPrint)) {
                final String kanaField = KNJ_EditEdit.getMS932ByteLength(student._kana) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._kana) > 20 ? "2" : "1";
                svf.VrsOut("KANA" + kanaField, student._kana);
            }
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 14 ? "2" : "1"), student._name);
            svf.VrsOut("TEXT1", "本年度" + ("2".equals(_param._kaikinsya) ? "精勤" : "皆勤") + "したことを");
            svf.VrsOut("TEXT2", "賞する");

            svf.VrsOut("CORP_DIV", _param._certifSchoolDatRemark1);
            svf.VrsOut("CORP_NAME", _param._certifSchoolDatRemark2);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName);
            svf.VrsOut("JOB_NAME", _param._certifSchoolDatJobName);
            svf.VrsOut("STAFF_NAME", _param._certifSchoolDatPrincipalName);
            if (null != schoolStampPath) {
                svf.VrsOut("STAMP_BMP", schoolStampPath);
            }

            svf.VrEndPage();
        }
        log.info(" noTargetCount = " + noTargetCount);
        return nonedata;
    }

    private static Map getHrClassMap(final List studentList) {
        final Map map = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == map.get(student._hrClass)) {
                map.put(student._hrClass, new ArrayList());
            }
            ((List) map.get(student._hrClass)).add(student);
        }
        return map;
    }

    private boolean isTarget(final Student student, final Param param) {
        if (null == student._daysAttendance) {
            return false; // 出欠データのない生徒は対象外
        }
        final DayAttendance da = student._daysAttendance;
        if (da._lesson == 0) { // 対象外
            return false;
        }

        final int kansanTikokuSoutai = da._sick * param._kessekiKansan + da._late + da._early;
        //log.debug(" tikoku max (kaikin, seikin = (" + kaikinTikokuMax + ", " + seikinTikokuMax + ")");
        final boolean isTarget;
        if ("2".equals(param._kaikinsya)) {
            isTarget = param._kaikinTikokuMax < kansanTikokuSoutai && kansanTikokuSoutai <= param._seikinTikokuMax;
        } else {
            isTarget = kansanTikokuSoutai <= param._kaikinTikokuMax;
        }
        //log.debug(" student = " + student._schregno + ", (lesson, sick, late + early, kansanTikokuSoutai, limit) = (" + da._lesson + ", " + da._sick + ", " + (da._late + da._early) + ", "+ kansanTikokuSoutai + ", " + limit + ")");
        return isTarget;
    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _kana;
        final String _sex;
        final String _grade;
        final String _hrClass;
        DayAttendance _daysAttendance;

        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name,
                final String kana,
                final String sex,
                final String grade,
                final String hrClass) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _kana = kana;
            _sex = sex;
            _grade = grade;
            _hrClass = hrClass;
            _daysAttendance = new DayAttendance();
        }

        public static List loadStudentList(final DB2UDB db2, final Param param, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List studentList = new ArrayList();

            try {
                // HRの生徒を取得
                final String sql = sqlSchregRegdDat(param, grade);
                //log.debug("schreg_regd_dat sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student st = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("HR_NAME"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("KANA"),
                            rs.getString("SEX"),
                            rs.getString("GRADE"),
                            rs.getString("HR_CLASS"));
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
        private static String sqlSchregRegdDat(final Param param, final String grade) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     VALUE(T2.NAME_KANA, '') AS KANA, ");
            stb.append("     T2.SEX ");
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
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
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

        private static void setAttendData(final DB2UDB db2, final Param param, final Collection studentList, final String grade, final String hrClass) {
            ResultSet rs = null;
            String sql = null;
            try {
                String ATTEND = "ATTEND";
                String ATTEND_DAY = "ATTEND_DAY";
                if (null == param._psMap.get(ATTEND)) {
                    // 出欠の情報
                    final Map attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, param._z010Name1, param._ctrlYear);
                    final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  param._sdate, param._date);
                    log.debug(" hasuuMap = " + hasuuMap);

                    final String semesInState = (String) hasuuMap.get("attendSemesInState");
                    // 1日単位
                    sql = getAttendSemesDat(param, semesInState);

                    param._psMap.put(ATTEND, db2.prepareStatement(sql));

                    final String aftDayFrom = (String) hasuuMap.get("aftDayFrom");
                    final String aftDayTo = (String) hasuuMap.get("aftDayTo");

                    if (null != aftDayFrom && null != aftDayTo) {
                        param._sqlAttend = getAttendDayDatSql(param, semesInState, param._semeMonth, aftDayFrom, aftDayTo);
                        param._psMap.put(ATTEND_DAY, db2.prepareStatement(param._sqlAttend));
                    }
                }
                PreparedStatement ps;
                ps = (PreparedStatement) param._psMap.get(ATTEND);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final int lesson   = rs.getInt("LESSON"); // 授業日数
                        final int sick     = rs.getInt("SICK"); // 病欠日数
                        final int special  = rs.getInt("MOURNING") + rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME"); // 特別欠席
                        final int mlesson  = lesson - special; // 出席すべき日数
                        student._daysAttendance._lesson   += lesson;
                        student._daysAttendance._mourning += rs.getInt("MOURNING");
                        student._daysAttendance._suspend  += rs.getInt("SUSPEND");
                        student._daysAttendance._virus  += rs.getInt("VIRUS");
                        student._daysAttendance._koudome  += rs.getInt("KOUDOME");
                        student._daysAttendance._mlesson  += mlesson;
                        student._daysAttendance._sick     += sick;
                        student._daysAttendance._attend   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
                        student._daysAttendance._late     += rs.getInt("LATE");
                        student._daysAttendance._early    += rs.getInt("EARLY");

                    }
                    DbUtils.closeQuietly(rs);
                }

                if (null != param._psMap.get(ATTEND_DAY)) {
                    ps = (PreparedStatement) param._psMap.get(ATTEND_DAY);
                    ps.setString(1, grade);
                    ps.setString(2, hrClass);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        ps.setString(3, student._schregno);

                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final int lesson   = rs.getInt("LESSON"); // 授業日数
                            final int sick     = rs.getInt("SICK"); // 病欠日数
                            final int special  = rs.getInt("MOURNING") + rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME"); // 特別欠席
                            final int mlesson  = lesson - special; // 出席すべき日数
                            student._daysAttendance._lesson   += lesson;
                            student._daysAttendance._mourning += rs.getInt("MOURNING");
                            student._daysAttendance._suspend  += rs.getInt("SUSPEND");
                            student._daysAttendance._virus  += rs.getInt("VIRUS");
                            student._daysAttendance._koudome  += rs.getInt("KOUDOME");
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
            }
        }

        private static String getAttendSemesDat(final Param param, final String semesInState) {
            //月別集計データから集計した表
            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS LESSON, ");
            stb.append("        SUM(MOURNING) AS MOURNING, ");
            stb.append("        SUM(SUSPEND) AS SUSPEND, ");
            stb.append("        SUM(ABSENT) AS ABSENT, ");
            stb.append("        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS SICK, ");
            stb.append("        SUM(LATE) AS LATE, ");
            stb.append("        SUM(EARLY) AS EARLY, ");
            if ("true".equals(param._useVirus)) {
                stb.append("        SUM(VIRUS) AS VIRUS, ");
            } else {
                stb.append("        0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        SUM(KOUDOME) AS KOUDOME, ");
            } else {
                stb.append("        0 AS KOUDOME, ");
            }
            stb.append("        SUM(ABROAD) AS ABROAD, ");
            stb.append("        SUM(OFFDAYS) AS OFFDAYS ");
            stb.append("    FROM ");
            stb.append("        V_ATTEND_SEMES_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("        AND W1.SEMESTER || W1.MONTH IN " + semesInState + " ");
            stb.append("        AND W1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        W1.SCHREGNO ");
            return stb.toString();
        }

        private static String getAttendDayDatSql(final Param param, final String semesInState, final String semeMonth, final String sdate, final String edate) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHEDULES AS (SELECT ");
            stb.append("     T1.MONTH, ");
            stb.append("     SUM(T1.LESSON) AS LESSON ");
            stb.append("   FROM ");
            stb.append("     ATTEND_SEMES_LESSON_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
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
            if ("true".equals(param._useVirus)) {
                stb.append("     SUM(CASE WHEN T1.DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
            } else {
                stb.append("     0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
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
    }

    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _sdate;
        final String _date;
        final String _kisaiDate;
        final String _dateMonth;
        final String _dateDayOfMonth;
        final String _ctrlDate;
        final String[] _grades;
        final String _useTestCountflg;
        final String _kaikinsya; // 1:皆勤者 2:精勤者
        final String _kanaPrint; //ふりがな出力
        final int _kessekiKansan;
        final int _kaikinKesseki;
        final int _kaikinKaikinTikoku;
        final int _seikinKesseki;
        final int _kaikinSeikinTikoku;
        final int _kaikinTikokuMax;
        final int _seikinTikokuMax;

        private KNJSchoolMst _knjSchoolMst;
        final String _z010Name1;
        final String _hrClassType; // 1:法定クラス 2:FIクラス
        final String _imagePath;
        final String _extension;
        final String _documentroot;
        final String _semeMonth;
        String _sqlAttend;
        final Map _psMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        final String _useVirus;
        final String _useKoudome;

        final String _certifSchoolDatSchoolName;
        final String _certifSchoolDatJobName;
        final String _certifSchoolDatPrincipalName;
        final String _certifSchoolDatRemark1;
        final String _certifSchoolDatRemark2;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester  = request.getParameter("CTRL_SEMESTER");
            _sdate = getSdate(db2);
            _date = request.getParameter("DATE").replace('/', '-');
            _dateMonth = StringUtils.split(_date, '-')[1];
            _dateDayOfMonth = StringUtils.split(_date, '-')[2];
            _kisaiDate = request.getParameter("KISAI_DATE").replace('/', '-');
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');
            _grades = "99".equals(request.getParameter("GRADE")) ? getGdatGrade(db2) : new String[] { request.getParameter("GRADE") };
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _kaikinsya = request.getParameter("KAIKINSYA");
            _kessekiKansan = defval(request.getParameter("KESSEKI_KANSAN"), 3);
            _kaikinKesseki = defval(request.getParameter("KAIKIN_KESSEKI"), 0);
            _kaikinKaikinTikoku = defval(request.getParameter("KAIKIN_KAIKIN_TIKOKU"), -1);
            _seikinKesseki = defval(request.getParameter("SEIKIN_KESSEKI"), 3);
            _kaikinSeikinTikoku = defval(request.getParameter("KAIKIN_SEIKIN_TIKOKU"), -1);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _certifSchoolDatSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolDatJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _certifSchoolDatPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifSchoolDatRemark1 = getCertifSchoolDat(db2, "REMARK1");
            _certifSchoolDatRemark2 = getCertifSchoolDat(db2, "REMARK2");
            _semeMonth = getSemeMonth(db2, _date);
            _kanaPrint = request.getParameter("KANA_PRINT");

            _kaikinTikokuMax = _kaikinKaikinTikoku;
            _seikinTikokuMax = _kaikinSeikinTikoku;
            log.info(" kaikinTikokuMax = " + _kaikinTikokuMax + ", seikinTikokuMax = " + _seikinTikokuMax);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
            _z010Name1 = setZ010Name1(db2);

            final KNJ_Get_Info getinfo = new KNJ_Get_Info();
            final KNJ_Get_Info.ReturnVal returnvalControl = getinfo.Control(db2);
            _imagePath = null == returnvalControl.val4 ? "" : returnvalControl.val4 + "/";
            _extension = returnvalControl.val5;
            _documentroot = request.getParameter("DOCUMENTROOT");
            _psMap = new HashMap();
        }

        private static int defval(final String val, final int def) {
            return NumberUtils.isDigits(val) ? Integer.parseInt(val) : def;
        }

        private String getImagePath() {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLSTAMP_J." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
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

        private String[] getGdatGrade(final DB2UDB db2) {
            List list = new ArrayList();

            final String sql = "SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' ORDER BY GRADE ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(rs.getString("GRADE"));
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            String[] arr = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String) list.get(i);
            }
            return arr;
        }

        private String getSchoolKind(final DB2UDB db2, final String grade) {
            String rtn = "";

            final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + grade + "'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String getDateString(final String date) {
            final String gengo = KNJ_EditDate.h_format_JP_N(date);
            final int month = Integer.parseInt(date.substring(5, 7));
            final int day = Integer.parseInt(date.substring(8, 10));

            final String monthStr = ((month < 10) ? " " : "")  + String.valueOf(month);
            final String dayStr = ((day < 10) ? " " : "")  + String.valueOf(day);

            return  gengo + String.valueOf(monthStr) + "月" + String.valueOf(dayStr) + "日";
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (Exception ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String fieldname) {
            String rtn = null;

            final String certifKindCd = "130";
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

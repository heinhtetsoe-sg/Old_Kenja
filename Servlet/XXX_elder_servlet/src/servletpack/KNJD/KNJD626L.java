/*
 * 作成日: 2021/03/11
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD626L extends HttpServlet {
    private static final Log log = LogFactory.getLog(KNJD626L.class);

    private boolean _hasData;

    private Param _param;

    private static final String SEMEALL = "9";
    private static final String KIMATSU_HYOUKA = "990008";
    private static final String SELECT_CLASSCD_UNDER = "90";
    private static final int BORDER_SCORE_TOUNENDO = 3;
    private static final int BORDER_SCORE_KANENDO = 2;
    private static final int LINE_MAX = 20;
    private static final String UNKNOWN_SCORE_MARK = "×";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJD626L.frm", 1);
        final Map<String, HrClass> hrClassMap = getHrClassMap(db2);
        int pageCnt = 1;
        int lineCnt = 1;
        int classCnt = 1;
        int classPageCnt = 1;

        for (HrClass hrClass : hrClassMap.values()) {
            printTitle(svf, pageCnt, hrClass);

            final int classEndPageCnt = (int)Math.ceil((double)hrClass._recordCnt / (double)LINE_MAX); // 一クラスを印字するのに何頁が必要かを求める
            for (Student student : hrClass._studentMap.values()) {
                if (LINE_MAX < lineCnt) {
                    lineCnt = 1;
                    classPageCnt++;
                    pageCnt++;
                    svf.VrEndPage();
                    printTitle(svf, pageCnt, hrClass);
                }
                printStudent(svf, hrClass._hrName, student, lineCnt, classCnt);

                for (Subclass subclass : student._printSubclassMap.values()) {
                    if (LINE_MAX < lineCnt) {
                        lineCnt = 1;
                        classPageCnt++;
                        pageCnt++;
                        svf.VrEndPage();
                        printTitle(svf, pageCnt, hrClass);
                    }

                    // クラス毎に最終頁に達した場合は男女の統計を印字する
                    if (classEndPageCnt == classPageCnt) {
                        svf.VrsOut("NUM1", String.valueOf(hrClass._mailCnt));
                        svf.VrsOut("NUM2", String.valueOf(hrClass._femailCnt));
                        svf.VrsOut("NUM3", String.valueOf(hrClass._mailCnt + hrClass._femailCnt));
                    }

                    // 同一クラス内で頁跨りが発生した場合は、最初の一行に必ず生徒情報を印字する
                    if (1 < classPageCnt && lineCnt == 1) {
                        printStudent(svf, hrClass._hrName, student, lineCnt, classCnt);
                    }

                    svf.VrsOutn("SELECT", lineCnt, subclass._requireFlg);
                    final int subclassByte = KNJ_EditEdit.getMS932ByteLength(subclass._subclassName);
                    final String subclassNameFieldName = subclassByte > 30 ? "3" : subclassByte > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + subclassNameFieldName, lineCnt, subclass._subclassName);
                    svf.VrsOutn("SCORE", lineCnt, subclass._score);
                    svf.VrsOutn("CREDIT", lineCnt, subclass._credit);

                    lineCnt++;
                    _hasData = true;
                }

                classCnt++;
            }

            lineCnt = 1;
            classPageCnt = 1;
            pageCnt++;
            svf.VrEndPage();
        }
    }

    private void printTitle(final Vrw32alp svf, final int pageNo, final HrClass hrClass) {
        svf.VrsOut("TITLE",    "不合格単位保持者一覧");
        svf.VrsOut("DATE",     KNJ_EditDate.h_format_SeirekiJP(_param._ctrlDate));
        svf.VrsOut("PAGE",     "No." + String.valueOf(pageNo));
        svf.VrsOut("HR_NAME1", hrClass._hrName);
        svf.VrsOut("TR_NAME",  hrClass._staffName);
    }

    private void printStudent(final Vrw32alp svf, final String hrName, final Student student, final int lineCnt, final int classCnt) {
        svf.VrsOutn("SEQ", lineCnt, String.valueOf(classCnt));
        svf.VrsOutn("HR_NAME2", lineCnt, hrName);
        if (!StringUtils.isEmpty(student._attendno)) {
            svf.VrsOutn("NO", lineCnt, String.valueOf(Integer.parseInt(student._attendno)));
        }
        svf.VrsOutn("SEX", lineCnt, student._sex);
        final int nameByte = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameFieldName = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
        svf.VrsOutn("NAME" + nameFieldName, lineCnt, student._name);
        svf.VrsOutn("BASE_REMARK", lineCnt, student._prefName);
        svf.VrsOutn("GRADE", lineCnt, student._proffesional);
    }

    private Map<String, HrClass> getHrClassMap(final DB2UDB db2) {
        final Map<String, HrClass> hrClassMap = new LinkedHashMap<String, HrClass>();
        HrClass hrClassClass = null;
        Student student = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String studentSql = getStudentSql();
        log.debug(" student sql =" + studentSql);

        try {
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String yearTounendo = rs.getString("YEAR_TOUNENDO");
                final String semesterTounendo = rs.getString("SEMESTER_TOUNENDO");
                final String gradeTounendo = rs.getString("GRADE_TOUNENDO");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String sex = rs.getString("SEX");
                final String name = rs.getString("NAME");
                final String prefName = rs.getString("PREF_NAME");
                final String professional = rs.getString("PROFESSIONAL");
                final String sdateTounendo = rs.getString("SDATE_TOUNENDO");
                final String edateTounendo = rs.getString("EDATE_TOUNENDO");

                if (hrClassMap.containsKey(hrClass)) {
                    hrClassClass = hrClassMap.get(hrClass);
                } else {
                    hrClassClass = new HrClass(gradeTounendo, hrClass, hrName, staffName);
                    hrClassMap.put(hrClass, hrClassClass);
                }

                if (hrClassClass._studentMap.containsKey(schregno)) {
                    student = hrClassClass._studentMap.get(schregno);
                } else {
                    student = new Student(yearTounendo, semesterTounendo, gradeTounendo, schregno, attendno, sex, name, prefName, professional, sdateTounendo, edateTounendo);
                    student.setSubclass(db2, semesterTounendo); // 当年度の受講評価を設定する
                    student.setAttendSubclass(db2, semesterTounendo); // 当年度の欠課時数情報を設定する
                    student.setPrintSubclass(); // 当年度の出力対象の科目を設定する
                    hrClassClass._studentMap.put(schregno, student);
                }

                //過年度の在籍情報があれば保持する。
                final String yearKanendo = rs.getString("YEAR_KANENDO");
                final String semesterKanendo = rs.getString("SEMESTER_KANENDO");
                final String gradeKanendo = rs.getString("GRADE_KANENDO");
                final String sdateKanendo = rs.getString("SDATE_KANENDO");
                final String edateKanendo = rs.getString("EDATE_KANENDO");
                if (yearKanendo != null) {
                    final Student kanendoStudent = new Student(yearKanendo, semesterKanendo, gradeKanendo, schregno, null, null, null, null, null, sdateKanendo, edateKanendo);
                    kanendoStudent.setSubclass(db2, SEMEALL); // 過年度の学年末の受講評価を設定する
                    kanendoStudent.setAttendSubclass(db2, SEMEALL); // 過年度の学年末の欠課時数情報を設定する　注：SQLで取得したSEMESTERは学年末ではなく最終学期
                    kanendoStudent.setPrintSubclass(); // 過年度の出力対象の科目を設定する

                    student._printSubclassMap.putAll(kanendoStudent._printSubclassMap); // 過年度の科目情報を当年度にマージする
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        for (final Iterator hrClassIte = hrClassMap.values().iterator(); hrClassIte.hasNext();) {
            final HrClass tmpHrClassClass = (HrClass)hrClassIte.next();

            for (final Iterator studentIte =  tmpHrClassClass._studentMap.values().iterator(); studentIte.hasNext();) {
                final Student tmpStudent = (Student)studentIte.next();
                final int printSubclassCnt = tmpStudent._printSubclassMap.size();
                if (printSubclassCnt == 0) {
                    // 出力する科目がなければマップから生徒情報を削除する
                    studentIte.remove();
                } else {
                    final String subclassCntStr = printSubclassCnt + "科目";
                    int creditTotal = 0;
                    for(Subclass subclass : tmpStudent._printSubclassMap.values()) {
                        final int credit = Integer.parseInt(StringUtils.defaultString(subclass._credit, "0"));
                        creditTotal += credit;
                    }
                    tmpStudent._printSubclassMap.put(null, new Subclass(null, "", subclassCntStr, "", String.valueOf(creditTotal)));

                    tmpHrClassClass._recordCnt += printSubclassCnt + 1;
                    if ("".equals(tmpStudent._sex)) {
                        tmpHrClassClass._femailCnt++;
                    } else {
                        tmpHrClassClass._mailCnt++;
                    }
                }
            }

            if (tmpHrClassClass._studentMap.size() == 0) {
                // 出力する生徒が一人もいなければマップから組情報を削除する
                hrClassIte.remove();
            }
        }

        return hrClassMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PROFFESIONAL0 AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         YEAR, ");
        stb.append("         GRADE, ");
        stb.append("         SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append(" ), ");
        //留年した人は最新の学年を取得する
        stb.append(" PROFFESIONAL1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         MAX(YEAR) AS YEAR, ");
        stb.append("         GRADE, ");
        stb.append("         SCHREGNO, ");
        stb.append("         COUNT(YEAR) AS CNT ");
        stb.append("     FROM ");
        stb.append("         PROFFESIONAL0 ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         SCHREGNO ");
        stb.append(" ), ");
        //当年度の在籍情報を取得
        stb.append(" SCHREG_TOUNENDO AS ( ");
        stb.append("     SELECT ");
        stb.append("         DAT.YEAR, ");
        stb.append("         DAT.SEMESTER, ");
        stb.append("         DAT.GRADE, ");
        stb.append("         DAT.HR_CLASS, ");
        stb.append("         DAT.SCHREGNO, ");
        stb.append("         DAT.ATTENDNO, ");
        stb.append("         DAT.COURSECD, ");
        stb.append("         DAT.MAJORCD, ");
        stb.append("         DAT.COURSECODE, ");
        stb.append("         PROFF.CNT ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT DAT ");
        stb.append("         INNER JOIN PROFFESIONAL1 PROFF ");
        stb.append("                 ON PROFF.YEAR     = DAT.YEAR ");
        stb.append("                AND PROFF.GRADE    = DAT.GRADE ");
        stb.append("                AND PROFF.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         DAT.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND DAT.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND DAT.GRADE    = '" + _param._grade    + "' ");
        stb.append("     AND ");
        stb.append(SQLUtils.whereIn(true, "DAT.HR_CLASS", _param._categorySelected));
        stb.append(" ), ");
        stb.append(" MAX_SEMESTER AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         MAX(SEMESTER) AS SEMESTER ");
        stb.append("     FROM ");
        stb.append("         SEMESTER_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR     < '" + _param._ctrlYear + "' ");
        stb.append("     AND SEMESTER <> '" + SEMEALL +"' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR ");
        stb.append(" ), ");
        //過年度の在籍情報を取得
        stb.append(" SCHREG_KANENDO AS ( ");
        stb.append("     SELECT ");
        stb.append("         DAT.YEAR, ");
        stb.append("         DAT.SEMESTER, ");
        stb.append("         DAT.GRADE, ");
        stb.append("         DAT.SCHREGNO, ");
        stb.append("         DAT.COURSECD, ");
        stb.append("         DAT.MAJORCD, ");
        stb.append("         DAT.COURSECODE, ");
        stb.append("         PROFF.CNT ");
        stb.append("     FROM ");
        stb.append("         SCHREG_TOUNENDO ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT DAT ");
        stb.append("                 ON DAT.SCHREGNO = SCHREG_TOUNENDO.SCHREGNO ");
        stb.append("         INNER JOIN MAX_SEMESTER ");
        stb.append("                 ON MAX_SEMESTER.YEAR     = DAT.YEAR ");
        stb.append("                AND MAX_SEMESTER.SEMESTER = DAT.SEMESTER ");
        stb.append("         INNER JOIN PROFFESIONAL1 PROFF ");
        stb.append("                 ON PROFF.YEAR     = DAT.YEAR ");
        stb.append("                AND PROFF.GRADE    = DAT.GRADE ");
        stb.append("                AND PROFF.SCHREGNO = DAT.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG_TOUNENDO.YEAR AS YEAR_TOUNENDO, ");
        stb.append("     SCHREG_TOUNENDO.SEMESTER AS SEMESTER_TOUNENDO, ");
        stb.append("     SCHREG_TOUNENDO.GRADE AS GRADE_TOUNENDO, ");
        stb.append("     SCHREG_TOUNENDO.HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     SCHREG_TOUNENDO.SCHREGNO, ");
        stb.append("     SCHREG_TOUNENDO.ATTENDNO, ");
        stb.append("     CASE WHEN BASE.SEX = '1' THEN '〇' ELSE '' END AS SEX, ");
        stb.append("     BASE.NAME, ");
        stb.append("     A053.ABBV1 AS PREF_NAME, ");
        stb.append("     CASE WHEN SCHREG_TOUNENDO.CNT = 1 THEN '---' ELSE '〇' END AS PROFESSIONAL, ");
        stb.append("     SEMESTER_TOUNENDO.SDATE AS SDATE_TOUNENDO, ");
        stb.append("     SEMESTER_TOUNENDO.EDATE AS EDATE_TOUNENDO, ");
        stb.append("     SCHREG_KANENDO.YEAR AS YEAR_KANENDO, ");
        stb.append("     SCHREG_KANENDO.SEMESTER AS SEMESTER_KANENDO, ");
        stb.append("     SCHREG_KANENDO.GRADE AS GRADE_KANENDO, ");
        stb.append("     SEMESTER_KANENDO.SDATE AS SDATE_KANENDO, ");
        stb.append("     SEMESTER_KANENDO.EDATE AS EDATE_KANENDO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TOUNENDO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("             ON HDAT.YEAR     = SCHREG_TOUNENDO.YEAR ");
        stb.append("            AND HDAT.SEMESTER = SCHREG_TOUNENDO.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = SCHREG_TOUNENDO.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = SCHREG_TOUNENDO.HR_CLASS ");
        stb.append("     INNER JOIN STAFF_MST STAFF ");
        stb.append("             ON STAFF.STAFFCD = HDAT.TR_CD1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
        stb.append("             ON BASE.SCHREGNO = SCHREG_TOUNENDO.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST D016 ");
        stb.append("            ON D016.SCHREGNO = BASE.SCHREGNO ");
        stb.append("           AND D016.BASE_SEQ = '016' ");
        stb.append("     LEFT JOIN V_NAME_MST A053 ");
        stb.append("            ON A053.YEAR     = SCHREG_TOUNENDO.YEAR ");
        stb.append("           AND A053.NAMECD1  = 'A053' ");
        stb.append("           AND A053.NAMECD2  = D016.BASE_REMARK1 ");
        stb.append("     LEFT JOIN SEMESTER_MST SEMESTER_TOUNENDO ");
        stb.append("            ON SEMESTER_TOUNENDO.YEAR     = SCHREG_TOUNENDO.YEAR ");
        stb.append("           AND SEMESTER_TOUNENDO.SEMESTER = SCHREG_TOUNENDO.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_KANENDO ");
        stb.append("            ON SCHREG_KANENDO.SCHREGNO = SCHREG_TOUNENDO.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST SEMESTER_KANENDO ");
        stb.append("            ON SEMESTER_KANENDO.YEAR     = SCHREG_KANENDO.YEAR ");
        stb.append("           AND SEMESTER_KANENDO.SEMESTER = '" + SEMEALL + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREG_TOUNENDO.GRADE, ");
        stb.append("     SCHREG_TOUNENDO.HR_CLASS, ");
        stb.append("     SCHREG_TOUNENDO.ATTENDNO, ");
        stb.append("     SCHREG_KANENDO.GRADE DESC ");
        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;
        private final Map<String, Student> _studentMap;
        private int _mailCnt   = 0;
        private int _femailCnt = 0;
        private int _recordCnt = 0;

        HrClass (final String grade, final String hrClass, final String hrName, final String staffName) {
            _grade      = grade;
            _hrClass    = hrClass;
            _hrName     = hrName;
            _staffName  = staffName;
            _studentMap = new LinkedHashMap<String, Student>();
        }
    }

    private class Student {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _schregno;
        private final String _attendno;
        private final String _sex;
        private final String _name;
        private final String _prefName;
        private final String _proffesional;
        private final String _sdate;
        private final String _edate;
        private final Map<String, Subclass> _subclassMap;
        private final Map<String, SubclassAttendance> _subclassAttendanceMap;
        private final Map<String, Subclass> _printSubclassMap;

        Student (
            final String year,
            final String semester,
            final String grade,
            final String schregno,
            final String attendno,
            final String sex,
            final String name,
            final String prefName,
            final String proffesional,
            final String sdate,
            final String edate
        ) {
            _year              = year;
            _semester          = semester;
            _grade             = grade;
            _schregno          = schregno;
            _attendno          = attendno;
            _sex               = sex;
            _name              = name;
            _prefName          = prefName;
            _proffesional      = proffesional;
            _sdate             = sdate;
            _edate             = edate;
            _subclassMap       = new LinkedHashMap<String, Subclass>();
            _subclassAttendanceMap = new LinkedHashMap<String, SubclassAttendance>();
            _printSubclassMap  = new LinkedHashMap<String, Subclass>();
        }

        private void setSubclass(final DB2UDB db2, final String semester) throws SQLException {
            final String subclassSql = getSubclassSql(semester);
            log.debug(_grade + ":" + _schregno + " subclass sql = " + subclassSql);

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String ySubclasscd = rs.getString("Y_SUBCLASSCD");
                    final String requireFlg = rs.getString("REQUIRE_FLG");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String score = rs.getString("SCORE");
                    final String credits = rs.getString("CREDITS");

                    if (!_subclassMap.containsKey(ySubclasscd)) {
                        final Subclass subclass = new Subclass(ySubclasscd, requireFlg, subclassName, score, credits);
                        _subclassMap.put(ySubclasscd, subclass);
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setAttendSubclass(final DB2UDB db2, final String semester) throws SQLException {
            // 当年の欠課情報は、指示画面で選択された学期のみ、
            // 過年度の欠課情報は学年末の学期のみ扱う
            final String attendSql = AttendAccumulate.getAttendSubclassSql(
                _year,
                semester,
                _sdate,
                _edate,
                _param._attendParamMap
            );
            log.debug(_grade + ":" + _schregno + " attend sql = " + attendSql);

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(attendSql);
                ps.setString(1, _schregno);
                ps.setString(2, _grade);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String seme = rs.getString("SEMESTER");
                    if (!SEMEALL.equals(seme)) {
                        continue;
                    }

                    final String ySubclass = _year + "-" + rs.getString("SUBCLASSCD");
                    if (!_subclassMap.containsKey(ySubclass)) {
                        log.warn(_grade + ":" + _schregno + ": no subclass : " + ySubclass);
                        continue;
                    }

                    final int lesson = rs.getInt("LESSON");
                    final int absent = rs.getInt("ABSENT");
                    final int mourning = rs.getInt("MOURNING");
                    final double sick = rs.getDouble("SICK_ONLY");
                    final double notice = rs.getDouble("NOTICE_ONLY");
                    final double noNotice = rs.getDouble("NONOTICE_ONLY");

                    final SubclassAttendance subclassAttendance = new SubclassAttendance(ySubclass, lesson, absent, mourning, sick, notice, noNotice);
                    _subclassAttendanceMap.put(ySubclass, subclassAttendance);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setPrintSubclass() {
            for (String ySubclasscd : _subclassMap.keySet()) {
                final Subclass subclass = _subclassMap.get(ySubclasscd);

                if (_subclassAttendanceMap.containsKey(ySubclasscd)) {
                    final SubclassAttendance sa = _subclassAttendanceMap.get(ySubclasscd);
                    final int absentHight = sa.getAbsentHight();
                    final double kekka = sa.getKekka();

                    if (UNKNOWN_SCORE_MARK.equals(subclass._score)) {
                         // SCOREが空白で、欠課時数が1/2を超えていない場合、未入力と判断して出力対象外とする
                         if (kekka < (absentHight / 2.0)) {
                             continue;
                         }
                    } else {
                        final int score = Integer.parseInt(StringUtils.defaultString(subclass._score, "0"));
                        if (_param._ctrlYear.equals(_year)) {
                            // ログイン年度の生徒の場合は、
                            // 10段階評価で３より大きく、保留でも未履修でもない科目は出力対象外とする
                            // ※「10段階評価で３以下、保留、および未履修」科目を出力対象とする
                            if ((BORDER_SCORE_TOUNENDO < score) && (kekka < (absentHight / 3.0))) {
                                continue;
                            }
                        } else {
                            // 過年度の生徒の場合は、
                            // 10段階評価で２より大きく未履修でもない科目は出力対象外とする
                            // ※「10段階評価で１・２・未履修」科目を出力対象とする
                            if ((BORDER_SCORE_KANENDO < score) && (kekka < (absentHight / 2.0))) {
                                continue;
                            }
                        }
                    }

                } else {
                    // SCOREが空白で、欠課時数が1/2を超えていない場合、未入力と判断して出力対象外とする
                    if (UNKNOWN_SCORE_MARK.equals(subclass._score)) {
                        continue;

                    } else {
                        final int score = Integer.parseInt(StringUtils.defaultString(subclass._score, "0"));

                        if  (BORDER_SCORE_TOUNENDO < score) {
                            continue;
                        }
                    }
                }

                _printSubclassMap.put(ySubclasscd, subclass);
            }
        }

        private String getSubclassSql(final String scoreSemester) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         SEMESTER, ");
            stb.append("         GRADE, ");
            stb.append("         SCHREGNO, ");
            stb.append("         COURSECD, ");
            stb.append("         MAJORCD, ");
            stb.append("         COURSECODE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR     = '" + _year     + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" ), ");
            stb.append(" SUBCLASS AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         STD.YEAR, ");
            stb.append("         STD.SEMESTER, ");
            stb.append("         STD.SCHREGNO, ");
            stb.append("         SUBCLASS.CLASSCD, ");
            stb.append("         SUBCLASS.SCHOOL_KIND, ");
            stb.append("         SUBCLASS.CURRICULUM_CD, ");
            stb.append("         SUBCLASS.SUBCLASSCD, ");
            stb.append("         SUBCLASS.SUBCLASSNAME, ");
            stb.append("         SUBCLASS.ELECTDIV ");
            stb.append("     FROM ");
            stb.append("         SCHREG ");
            stb.append("         INNER JOIN CHAIR_STD_DAT STD ");
            stb.append("                 ON STD.YEAR     = SCHREG.YEAR ");
            stb.append("                AND STD.SEMESTER = SCHREG.SEMESTER ");
            stb.append("                AND STD.SCHREGNO = SCHREG.SCHREGNO ");
            stb.append("                AND STD.APPDATE  BETWEEN DATE('" + _sdate + "') AND DATE('" + _edate + "') ");
            stb.append("         INNER JOIN CHAIR_DAT CHAIR ");
            stb.append("                 ON CHAIR.YEAR     = STD.YEAR ");
            stb.append("                AND CHAIR.SEMESTER = STD.SEMESTER ");
            stb.append("                AND CHAIR.CHAIRCD  = STD.CHAIRCD ");
            stb.append("                AND CHAIR.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("         INNER JOIN SUBCLASS_MST SUBCLASS ");
            stb.append("                 ON SUBCLASS.CLASSCD       = CHAIR.CLASSCD ");
            stb.append("                AND SUBCLASS.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
            stb.append("                AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("                AND SUBCLASS.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
            stb.append("         INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("                 ON SUBY.YEAR          = STD.YEAR ");
            stb.append("                AND SUBY.CLASSCD       = SUBCLASS.CLASSCD ");
            stb.append("                AND SUBY.SCHOOL_KIND   = SUBCLASS.SCHOOL_KIND ");
            stb.append("                AND SUBY.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("                AND SUBY.SUBCLASSCD    = SUBCLASS.SUBCLASSCD ");
//            stb.append("         INNER JOIN SEMESTER_MST SEME ");
//            stb.append("                 ON SEME.YEAR     = STD.YEAR ");
//            stb.append("                AND SEME.SEMESTER = '" + scoreSemester + "' ");
//            stb.append("         LEFT  JOIN ATTEND_SUBCLASS_DAT ABSENT_T ");
//            stb.append("                 ON ABSENT_T.YEAR           = STD.YEAR ");
//            stb.append("                AND ABSENT_T.SCHREGNO       = STD.SCHREGNO ");
//            stb.append("                AND ABSENT_T.CLASSCD        = SUBCLASS.CLASSCD ");
//            stb.append("                AND ABSENT_T.SCHOOL_KIND    = SUBCLASS.SCHOOL_KIND ");
//            stb.append("                AND ABSENT_T.CURRICULUM_CD  = SUBCLASS.CURRICULUM_CD ");
//            stb.append("                AND ABSENT_T.SUBCLASSCD     = SUBCLASS.SUBCLASSCD ");
//            stb.append("                AND ABSENT_T.MONTH      <= '' ");
            stb.append(" ), ");
            stb.append(" REP_SUBCLASS AS ( ");
            stb.append("     SELECT ");
            stb.append("         SUBCLASS.YEAR, ");
            stb.append("         SUBCLASS.SEMESTER, ");
            stb.append("         SUBCLASS.SCHREGNO, ");
            stb.append("         SUBCLASS.CLASSCD, ");
            stb.append("         SUBCLASS.SCHOOL_KIND, ");
            stb.append("         SUBCLASS.CURRICULUM_CD, ");
            stb.append("         SUBCLASS.SUBCLASSCD, ");
            stb.append("         SUBCLASS.SUBCLASSNAME, ");
            stb.append("         SUBCLASS.ELECTDIV, ");
            stb.append("         CREDIT_T.CREDITS, ");
            stb.append("         COM_SUBCLASS.CALCULATE_CREDIT_FLG, ");
            stb.append("         SUM(ATT_CREDIT_T.CREDITS) AS ATT_CREDITS ");
            stb.append("     FROM ");
            stb.append("         SCHREG ");
            stb.append("         INNER JOIN SUBCLASS ");
            stb.append("                 ON SUBCLASS.YEAR     = SCHREG.YEAR ");
            stb.append("                AND SUBCLASS.SEMESTER = SCHREG.SEMESTER ");
            stb.append("                AND SUBCLASS.SCHREGNO = SCHREG.SCHREGNO ");
            stb.append("         LEFT JOIN CREDIT_MST CREDIT_T ");
            stb.append("                ON CREDIT_T.YEAR          = SCHREG.YEAR ");
            stb.append("               AND CREDIT_T.COURSECD      = SCHREG.COURSECD ");
            stb.append("               AND CREDIT_T.MAJORCD       = SCHREG.MAJORCD ");
            stb.append("               AND CREDIT_T.GRADE         = SCHREG.GRADE ");
            stb.append("               AND CREDIT_T.COURSECODE    = SCHREG.COURSECODE ");
            stb.append("               AND CREDIT_T.CLASSCD       = SUBCLASS.CLASSCD ");
            stb.append("               AND CREDIT_T.SCHOOL_KIND   = SUBCLASS.SCHOOL_KIND ");
            stb.append("               AND CREDIT_T.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("               AND CREDIT_T.SUBCLASSCD    = SUBCLASS.SUBCLASSCD ");
            stb.append("         LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COM_SUBCLASS ");
            stb.append("                ON COM_SUBCLASS.YEAR                   = SUBCLASS.YEAR ");
            stb.append("               AND COM_SUBCLASS.COMBINED_CLASSCD       = SUBCLASS.CLASSCD ");
            stb.append("               AND COM_SUBCLASS.COMBINED_SCHOOL_KIND   = SUBCLASS.SCHOOL_KIND ");
            stb.append("               AND COM_SUBCLASS.COMBINED_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("               AND COM_SUBCLASS.COMBINED_SUBCLASSCD    = SUBCLASS.SUBCLASSCD ");
            stb.append("         LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT_SUBCLASS ");
            stb.append("                ON ATT_SUBCLASS.YEAR                 = COM_SUBCLASS.YEAR ");
            stb.append("               AND ATT_SUBCLASS.ATTEND_CLASSCD       = COM_SUBCLASS.ATTEND_CLASSCD ");
            stb.append("               AND ATT_SUBCLASS.ATTEND_SCHOOL_KIND   = COM_SUBCLASS.ATTEND_SCHOOL_KIND ");
            stb.append("               AND ATT_SUBCLASS.ATTEND_CURRICULUM_CD = COM_SUBCLASS.ATTEND_CURRICULUM_CD ");
            stb.append("               AND ATT_SUBCLASS.ATTEND_SUBCLASSCD    = COM_SUBCLASS.ATTEND_SUBCLASSCD ");
            stb.append("         LEFT JOIN CREDIT_MST ATT_CREDIT_T ");
            stb.append("                ON ATT_CREDIT_T.YEAR          = SCHREG.YEAR ");
            stb.append("               AND ATT_CREDIT_T.COURSECD      = SCHREG.COURSECD ");
            stb.append("               AND ATT_CREDIT_T.MAJORCD       = SCHREG.MAJORCD ");
            stb.append("               AND ATT_CREDIT_T.GRADE         = SCHREG.GRADE ");
            stb.append("               AND ATT_CREDIT_T.COURSECODE    = SCHREG.COURSECODE ");
            stb.append("               AND ATT_CREDIT_T.CLASSCD       = ATT_SUBCLASS.ATTEND_CLASSCD ");
            stb.append("               AND ATT_CREDIT_T.SCHOOL_KIND   = ATT_SUBCLASS.ATTEND_SCHOOL_KIND ");
            stb.append("               AND ATT_CREDIT_T.CURRICULUM_CD = ATT_SUBCLASS.ATTEND_CURRICULUM_CD ");
            stb.append("               AND ATT_CREDIT_T.SUBCLASSCD    = ATT_SUBCLASS.ATTEND_SUBCLASSCD ");
            stb.append("     WHERE ");
            stb.append("         NOT EXISTS (SELECT ");
            stb.append("                         'X' ");
            stb.append("                     FROM ");
            stb.append("                         SUBCLASS_REPLACE_COMBINED_DAT ATT_SUBCLASS2 ");
            stb.append("                     WHERE ");
            stb.append("                         SUBCLASS.YEAR          = ATT_SUBCLASS2.YEAR ");
            stb.append("                     AND SUBCLASS.CLASSCD       = ATT_SUBCLASS2.ATTEND_CLASSCD ");
            stb.append("                     AND SUBCLASS.SCHOOL_KIND   = ATT_SUBCLASS2.ATTEND_SCHOOL_KIND ");
            stb.append("                     AND SUBCLASS.CURRICULUM_CD = ATT_SUBCLASS2.ATTEND_CURRICULUM_CD ");
            stb.append("                     AND SUBCLASS.SUBCLASSCD    = ATT_SUBCLASS2.ATTEND_SUBCLASSCD ");
            stb.append("                    ) ");
            stb.append("     GROUP BY ");
            stb.append("         SUBCLASS.YEAR, ");
            stb.append("         SUBCLASS.SEMESTER, ");
            stb.append("         SUBCLASS.SCHREGNO, ");
            stb.append("         SUBCLASS.CLASSCD, ");
            stb.append("         SUBCLASS.SCHOOL_KIND, ");
            stb.append("         SUBCLASS.CURRICULUM_CD, ");
            stb.append("         SUBCLASS.SUBCLASSCD, ");
            stb.append("         SUBCLASS.SUBCLASSNAME, ");
            stb.append("         SUBCLASS.ELECTDIV, ");
            stb.append("         COM_SUBCLASS.CALCULATE_CREDIT_FLG, ");
            stb.append("         CREDIT_T.CREDITS ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REP_SUBCLASS.YEAR || '-' || REP_SUBCLASS.CLASSCD || '-' || REP_SUBCLASS.SCHOOL_KIND || '-' || REP_SUBCLASS.CURRICULUM_CD || '-' || REP_SUBCLASS.SUBCLASSCD AS Y_SUBCLASSCD, ");
            stb.append("     CASE WHEN REP_SUBCLASS.ELECTDIV = '0' THEN '必' ELSE '選' END AS REQUIRE_FLG, ");
            stb.append("     REP_SUBCLASS.SUBCLASSNAME, ");
            stb.append("     CASE WHEN SCORE_T.SCORE IS NULL THEN '" + UNKNOWN_SCORE_MARK + "' ELSE TO_CHAR(SCORE_T.SCORE) END AS SCORE, ");
            stb.append("     CASE WHEN REP_SUBCLASS.CALCULATE_CREDIT_FLG = '2' THEN REP_SUBCLASS.ATT_CREDITS ELSE REP_SUBCLASS.CREDITS END AS CREDITS ");
            stb.append(" FROM ");
            stb.append("     REP_SUBCLASS ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE_T ");
            stb.append("            ON SCORE_T.YEAR          = REP_SUBCLASS.YEAR ");
            stb.append("           AND SCORE_T.SEMESTER      = '" + scoreSemester + "' ");
            stb.append("           AND SCORE_T.TESTKINDCD || SCORE_T.TESTITEMCD || SCORE_T.SCORE_DIV = '" + KIMATSU_HYOUKA + "' ");
            stb.append("           AND SCORE_T.CLASSCD       = REP_SUBCLASS.CLASSCD ");
            stb.append("           AND SCORE_T.SCHOOL_KIND   = REP_SUBCLASS.SCHOOL_KIND ");
            stb.append("           AND SCORE_T.CURRICULUM_CD = REP_SUBCLASS.CURRICULUM_CD ");
            stb.append("           AND SCORE_T.SUBCLASSCD    = REP_SUBCLASS.SUBCLASSCD ");
            stb.append("           AND SCORE_T.SCHREGNO      = REP_SUBCLASS.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     REP_SUBCLASS.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private class Subclass {
        private final String _ySubclasscd;
        private final String _requireFlg;
        private final String _subclassName;
        private final String _score;
        private final String _credit;

        Subclass (final String ySubclasscd, final String requireFlg, final String subclassName, final String score, final String credit) {
            _ySubclasscd  = ySubclasscd;
            _requireFlg   = requireFlg;
            _subclassName = subclassName;
            _score        = score;
            _credit       = credit;
        }
    }

    /**
     * 科目ごとの出欠データ
     */
    static class SubclassAttendance {
        final String _ySubclass;
        final int _lesson;
        final int _absent;
        final int _mourning;
        final double _sick;
        final double _notice;
        final double _noNotice;

        public SubclassAttendance(
            final String ySubclass,
            final int lesson,
            final int absent,
            final int mourning,
            final double sick,
            final double notice,
            final double noNotice
        ) {
            _ySubclass = ySubclass;
            _lesson    = lesson;
            _absent    = absent;
            _mourning  = mourning;
            _sick      = sick;
            _notice    = notice;
            _noNotice  = noNotice;
        }

        public int getAbsentHight() {
            return _lesson - _absent - _mourning;
        }

        public double getKekka() {
            return _sick + _notice + _noNotice;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _grade;
        private final String _semester;
        private final String[] _categorySelected;
        private final String _semesterName;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear         = request.getParameter("CTRL_YEAR");
            _ctrlDate         = request.getParameter("CTRL_DATE");
            _grade            = request.getParameter("GRADE");
            _semester         = request.getParameter("SEMESTER");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _semesterName     = getSemesterName(db2);

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("grade", "?");
            _attendParamMap.put("schregno", "?");
        }

        private String getSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }
    }
}

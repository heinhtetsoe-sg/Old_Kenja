// kanji=漢字
/*
 * $Id: a6a28756e85812e916bcbd4856d4e975a46eb0fd $
 *
 * 作成日: 2007/10/26 10:09:43 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 成績判定会議総括資料。
 * @author takaesu
 * @version $Id: a6a28756e85812e916bcbd4856d4e975a46eb0fd $
 */
public class KNJD234 {
    /** 卒業見込み者などの表が存在するフォーム */
    private static final String FORM_NAME = "KNJD234.frm";
    /** 卒業見込み者などの表が存在しないフォーム */
    private static final String FORM_NAME1 = "KNJD234_2.frm";

    /** 最大組数 */
    private static final int MAX_HRCLASS = 10;
    private String FIRST_SEMESTER = "1";    // 「年度当初」の学期

    private Param _param;
    private boolean _hasData;
    private Form _form;

    private KNJSchoolMst _knjSchoolMst;

    /*pkg*/static final Log log = LogFactory.getLog(KNJD234.class);

    /** 対象外の教科コード。このコード以上のデータは対象外 */
    private static final int ILLEGAL_CLASS_CODE_VALUE = 89;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        final String formFile = _param.isLastAnnual() ? FORM_NAME : FORM_NAME1;
        _form = new Form(formFile, response);

        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _param._year);
            } catch (SQLException ex) {
                log.debug("KNJSchoolMst exception!", ex);
            }
            _form.printHeader();

            if (true) {
                _hasData = printMain(db2);
            } else {
                // デバッグ用
                _form.testPrint();
                _hasData = true;
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            // 終了処理
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private boolean printMain(final DB2UDB db2) throws SQLException, IOException, ParseException {

        boolean hasData = false;
        
        final Set records = loadRecord(db2);
        log.debug("読み込んだ " + _param.getRecordTable() + " の総数=" + records.size());
        if (records.size() != 0) {
            // 在籍
            printZaiseki(db2);

            // 成績優良者...
            printGood(db2, records);

            if (_param.isLastAnnual()) {
                // 卒業見込み者...
                printGraduation(db2);
            } else {
                noPrintGraduation(db2);
            }

            /*
             * 評定分布表はレコード(フォーム)なので最後に印字
             */
            // 評定分布表
            printHyoutei(db2, records);

            // 1頁目の終わり
            _form._svf.VrPrint();
            
            hasData = true;
        }

        if (_param._isDetail1) {
            KNJD234DetailAbstract badStudents = new KNJD234GoodStudent(db2, _form._svf, _param);
            badStudents.setPrintData("GOOD", 50, 11);
            hasData = badStudents.printOut() || hasData;
        }

        if (_param._isDetail2) {
            KNJD234DetailAbstract badStudents = new KNJD234BadStudent(db2, _form._svf, _param);
            badStudents.setPrintData("BAD", 50, 30);
            hasData = badStudents.printOut() || hasData;
        }
        return hasData;
    }

    private Set loadRecord(final DB2UDB db2) throws SQLException {
        final Set rtn = new HashSet();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlRecord(_param._year, _param._semester, _param._grade));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subClassCd = rs.getString("SUBCLASSCD");
                final SubClass subClass = (SubClass) _param._subClasses.get(subClassCd);
                if (null == subClass) {
                    continue;
                }
                // 学年末の時は「元」を。学期の時は「先」を無視する
                if (_param._isGakunenMatu) {
                    if (subClass._isMoto) {
                        continue;
                    }
                } else {
                    if (subClass._isSaki) {
                        continue;
                    }
                }
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final HomeRoom hr = _param.getHomeRoom(grade, hrClass);
                if (null == hr) {
                    log.warn("年組が無い。" + grade + ", " + hrClass);
                    continue;
                }

                // Recordを保持する
                final Integer value = KNJServletUtils.getInteger(rs, "VALUE");
                final Integer getCredit = KNJServletUtils.getInteger(rs, "GET_CREDIT");
                final Integer compCredit = KNJServletUtils.getInteger(rs, "COMP_CREDIT");
                final Record rec = new Record(subClass, schregno, hr, value, getCredit, compCredit);
                rtn.add(rec);

                // 生徒にRecord_dat をぶら下げる
                final Student student = hr.getStudent(schregno);
                if (null != student) {
                    student.addRecord(rec);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String sqlRecord(final String year, final String semester, final String grade) {
        final String field;
        if (_param._isGakunenMatu) {
            field = "GRAD_VALUE";
        } else {
            field = "SEM" + _param._semester + "_VALUE";
        }
        final StringBuffer stb = new StringBuffer();
        if (_param.isUseRecordScoreDat()) {
            stb.append(" select ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("  T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("  T1.SUBCLASSCD, ");
            }
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.HR_CLASS, ");
            stb.append("  T1.VALUE, ");
            stb.append("  T1.GET_CREDIT, ");
            stb.append("  T1.COMP_CREDIT ");
            stb.append(" from RECORD_SCORE_DAT T1 inner join SCHREG_REGD_DAT T2 ");
            stb.append("  on T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(" where ");
            stb.append("  T1.YEAR = '" + year + "' and ");
            stb.append("  T1.SEMESTER = '" + _param._combSeme + "' and ");
            stb.append("  T1.TESTKINDCD = '99' and ");
            stb.append("  T1.TESTITEMCD = '00' and ");
            stb.append("  T1.SCORE_DIV = '00' and ");
            stb.append("  T1.YEAR = T2.YEAR and ");
            stb.append("  T2.SEMESTER = '" + semester + "' and ");
            stb.append("  T2.GRADE = '" + grade + "' and ");
            stb.append("  int(substr(T1.SUBCLASSCD, 1, 2)) < " + ILLEGAL_CLASS_CODE_VALUE + "");
            log.debug("RECORD_SCORE_DATのSQL文=" + stb.toString());
        } else {
            stb.append(" select ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("  T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("  T1.SUBCLASSCD, ");
            }
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.HR_CLASS, ");
            stb.append("  T1." + field + " as VALUE, ");
            stb.append("  T1.GET_CREDIT, ");
            stb.append("  T1.COMP_CREDIT ");
            stb.append(" from RECORD_DAT T1 inner join SCHREG_REGD_DAT T2 ");
            stb.append("  on T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(" where ");
            stb.append("  T1.YEAR = '" + year + "' and ");
            stb.append("  T1.YEAR = T2.YEAR and ");
            stb.append("  T2.SEMESTER = '" + semester + "' and ");
            stb.append("  T2.GRADE = '" + grade + "' and ");
            stb.append("  int(substr(T1.SUBCLASSCD, 1, 2)) < " + ILLEGAL_CLASS_CODE_VALUE + " ");
            log.debug("RECORD_DATのSQL文=" + stb.toString());
        }
        return stb.toString();
    }

    private void printGraduation(final DB2UDB db2) throws SQLException {
        log.debug("===卒業見込み者===");
        final Graduate graduate = new Graduate();
        graduate.count(db2);
        graduate.printCount();
    }

    private void noPrintGraduation(final DB2UDB db2) throws SQLException {
        final Graduate graduate = new Graduate();
        graduate.count(db2);
    }

    private void printGood(final DB2UDB db2, final Set records) throws SQLException {
        log.debug("===成績優良者===");

        final Good good = new Good();
        good.count(db2);
        good.printCount();
    }

    private void printHyoutei(final DB2UDB db2, final Set records) {
        log.debug("===評定分布===");

        final Hyoutei hyoutei = new Hyoutei(records);
        hyoutei.count();
        hyoutei.printCount();
    }

    private void printZaiseki(final DB2UDB db2) throws SQLException {
        log.debug("===在籍===");

        final Zaiseki zaiseki = new Zaiseki();
        zaiseki.loadCount(db2);
        zaiseki.printCount(_param._homeRooms.values());
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester;

        final String choiceSem = request.getParameter("COMBO_SEM");
        final boolean isGakunenMatu = "9".equals(choiceSem) ? true : false;
        semester = isGakunenMatu ? request.getParameter("NOW_SEM") : choiceSem;
        log.debug("対象学期=" + semester + ", 学年末か?=" + isGakunenMatu);

        final String grade = request.getParameter("GRADE");
        final String upper = request.getParameter("ASSESS1");
        final String lower = request.getParameter("ASSESS2");
        final String date = request.getParameter("DATE");
        final boolean detail1 = null != request.getParameter("ASSESS1_DETAIL") ? true : false;
        final boolean detail2 = null != request.getParameter("ASSESS2_DETAIL") ? true : false;
        final String lowerValue = request.getParameter("VALUE");
        final String lowerCount = request.getParameter("COUNT");
        final String lowerUnstudyCount = request.getParameter("UNSTUDY");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");
        final String useVirus = request.getParameter("useVirus");
        final String useKoudome = request.getParameter("useKoudome");

        BigDecimal upperLine = null;
        try {
            if (null == upper) {
                throw new NumberFormatException();
            }
            upperLine = new BigDecimal(upper);
        } catch (final NumberFormatException e) {
            log.error("成績優良者の評定平均が不正");
        }

        BigDecimal lowerLine = null;
        try {
            if (null == lower) {
                throw new NumberFormatException();
            }
            lowerLine = new BigDecimal(lower);
        } catch (final NumberFormatException e) {
            log.error("成績不振者の評定平均が不正");
        }

        final Param param = new Param(
                year,
                semester,
                choiceSem,
                isGakunenMatu,
                grade,
                upperLine,
                lowerLine,
                date,
                detail1,
                detail2,
                lowerValue,
                lowerCount,
                lowerUnstudyCount,
                useCurriculumcd,
                useVirus,
                useKoudome
        );
        return param;
    }

    class Param {
        private final String _year;
        private final String _semester;
        private final String _combSeme;
        private final boolean _isGakunenMatu;
        private final String _grade;
        private final BigDecimal _upperLine;
        private final BigDecimal _lowerLine;
        private final String _date;
        private final String _japaneseDate;
        private final boolean _isDetail1;
        private final boolean _isDetail2;

        private final int _lowerValue;
        private final int _lowerCount;
        private final int _lowerUnStudyCount;

        /** 卒業単位数 */
        private Integer _gradCredits;
        /** 卒業履修数 */
        private Integer _gradCompCredits;

        /** 年組 */
        private HashMap _homeRooms;

        /** 学期名 */
        private String _semesterName;

        /** 教科マスタ */
        private Map _classes;

        /** 科目マスタ */
        private Map _subClasses;

        /** 年度開始日付 */
        private String _startDate;

        /** 単位マスタ */
        private MultiMap _creditMst = new MultiHashMap();

        /** 名称マスタ（学校等） */
        private String _z010Name1; //学校
        private String _z010NameSpare1; //record_score_dat使用フラグ

        /** 学校マスタ */
        private KNJSchoolMst _knjSchoolMst;

        private String _absenceDiv = "1";
        private PreparedStatement _absencePs = null;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        public Param(
                final String year,
                final String semester,
                final String choiceSem,
                final boolean isGakunenMatu,
                final String grade,
                final BigDecimal upperLine,
                final BigDecimal lowerLine,
                final String date,
                final boolean detail1,
                final boolean detail2,
                final String lowerValue,
                final String lowerCount,
                final String lowerUnstudyCount,
                final String useCurriculumcd,
                final String useVirus,
                final String useKoudome
        ) {
            _year = year;
            _semester = semester;
            _combSeme = choiceSem;
            _isGakunenMatu = isGakunenMatu;
            _grade = grade;
            _upperLine = upperLine;
            _lowerLine = lowerLine;
            _date = date.replace('/', '-');
            _isDetail1 = detail1;
            _isDetail2 = detail2;

            try {
                final Calendar cal = KNJServletUtils.parseDate(_date);
                final int yyyy = cal.get(Calendar.YEAR);
                final int mm = cal.get(Calendar.MONTH) + 1;
                final int dd = cal.get(Calendar.DAY_OF_MONTH);
                _japaneseDate = KenjaProperties.gengou(yyyy, mm, dd);
            } catch (ParseException e) {
                throw new IllegalArgumentException("日付を和暦に変換できない!");
            }

            _lowerValue = (null == lowerValue) ? 1 : Integer.parseInt(lowerValue);
            _lowerCount = (null == lowerCount) ? 1 : Integer.parseInt(lowerCount);
            _lowerUnStudyCount = (null == lowerUnstudyCount) ? 1 : Integer.parseInt(lowerUnstudyCount);
            _useCurriculumcd = useCurriculumcd;
            _useVirus = useVirus;
            _useKoudome = useKoudome;
        }

        public HomeRoom getHomeRoom(final String grade, final String hrClass) {
            final Collection homeRooms = (Collection) _homeRooms.get(grade);
            for (final Iterator it = homeRooms.iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                if (hrClass.equals(hr._room)) {
                    return hr;
                }
            }
            return null;
        }

        public void load(final DB2UDB db2) throws SQLException {
            setNameMst(db2);
            setSchoolMst(db2);
            _homeRooms = setHomeRooms(db2);
            setStudentOnHr(db2);
            setAbroadCredits(db2);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
            setCombinedOnSubClass(db2);
            _startDate = setStartDate(db2);
            _semesterName = setSemesterName(db2);
            _knjSchoolMst = new KNJSchoolMst(db2, _year);

            try {
                _absencePs = db2.prepareStatement(getAbsencePsSql(_year));
            } catch (SQLException e1) {
                log.error("_absencePs", e1);
            }

            final int currentGrade = Integer.valueOf(_grade).intValue();
            int year = Integer.valueOf(_year).intValue();
            for (int i = currentGrade; i >= 1; i--) {  // 指定学年から1年生まで
                final String gradeCode = "0" + i;
                setCreditMst(db2, String.valueOf(year), gradeCode);

                final Collection coll = (Collection) _creditMst.get(String.valueOf(year));
                if (null != coll) {
                    log.debug("単位マスタ " + i + "年生時:" + coll.size());
                } else {
                    log.debug("単位マスタ " + i + "年生時:0");
                }
                year--;
            }
        }

        private String getAbsencePsSql(String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COMP_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ABSENCE_HIGH_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND DIV = '" + _absenceDiv + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? ");
            } else {
                stb.append("     AND SUBCLASSCD = ? ");
            }
            stb.append("     AND SCHREGNO = ? ");

            return stb.toString();
        }

        private void setAbroadCredits(final DB2UDB db2) throws SQLException {
            for (final Iterator it = getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                setAbroadCredits0(db2, hr);
            }
        }

        private void setAbroadCredits0(final DB2UDB db2, final HomeRoom hr) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                for (final Iterator it = hr._students.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps = db2.prepareStatement(sqlAbroadCredits(student._schregno));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final Integer total = KNJServletUtils.getInteger(rs, "TOTAL_ABROAD_CREDITS");
                        if (null != total) {
                            student._transferTotalAbroadCredits = total.intValue();
                            log.debug("留学修得単位数の合計=" + total + ", " + student.info());
                        }
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlAbroadCredits(final String schregno) {
            final String sql;
            sql = " select"
                    + "  sum(ABROAD_CREDITS) as TOTAL_ABROAD_CREDITS"
                    + " from"
                    + "  SCHREG_TRANSFER_DAT"
                    + " where"
                    + "  SCHREGNO ='" + schregno + "' and"
                    + "  ABROAD_CREDITS is not null"
                    ;
            return sql;
        }

        private void setCreditMst(final DB2UDB db2, final String year, final String grade) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCreditMst(year, grade));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final CourseInfo courseInfo = new CourseInfo(courseCd, majorCd, courseCode);

                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final Integer credits = KNJServletUtils.getInteger(rs, "CREDITS");
                    final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                    final String requireFlag = rs.getString("REQUIRE_FLG");

                    final CreditMst creditMst = new CreditMst(
                            year,
                            grade,
                            courseInfo,
                            subClassCd,
                            credits,
                            absenceHigh,
                            requireFlag
                    );
                    _creditMst.put(year, creditMst);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlCreditMst(final String year, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.CREDITS, ");
            stb.append("     T1.ABSENCE_HIGH, ");
            stb.append("     T1.REQUIRE_FLG ");
            stb.append(" FROM ");
            stb.append("     CREDIT_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.year = '" + year + "' AND ");
            stb.append("     T1.grade = '" + grade + "' AND ");
            stb.append("     int(substr(T1.SUBCLASSCD, 1, 2)) < ").append(ILLEGAL_CLASS_CODE_VALUE);

            return stb.toString();
        }

        private Integer setSchoolMst(DB2UDB db2) throws SQLException {
            Integer rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSchoolMst());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _gradCredits = KNJServletUtils.getInteger(rs, "GRAD_CREDITS");
                    _gradCompCredits = KNJServletUtils.getInteger(rs, "GRAD_COMP_CREDITS");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("卒業単位数=" + rtn);
            return rtn;
        }

        private String sqlSchoolMst() {
            return " select"
                    + "    VALUE(GRAD_CREDITS, 0) AS GRAD_CREDITS,"
                    + "    VALUE(GRAD_COMP_CREDITS, 0) AS GRAD_COMP_CREDITS"
                    + " from"
                    + "    SCHOOL_MST"
                    + " where"
                    + "    year = '" + _year + "'";
        }

        private HashMap setHomeRooms(final DB2UDB db2) throws SQLException {
            final HashMap rtn = new MultiHashMap();
            int i = 1;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlHomeRooms());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (i++ > MAX_HRCLASS) {
                        break;
                    }
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("ROOM");
                    final String name = rs.getString("NAME");
                    final String abbv = rs.getString("NAMEABBR");
                    final HomeRoom hr = new HomeRoom(grade, hrClass, name, abbv);
                    rtn.put(grade, hr);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        public String sqlHomeRooms() {
            return "select"
                    + "    GRADE as grade,"
                    + "    HR_CLASS as room,"
                    + "    HR_NAME as name,"
                    + "    HR_NAMEABBV as nameAbbr"
                    + "  from SCHREG_REGD_HDAT"
                    + "  where"
                    + "    YEAR = '" + _year + "'"
                    + "  and"
                    + "    SEMESTER = '" + _semester + "'"
                    + "  and"
                    + "    GRADE = '" + _grade + "'"
                    + "  order by"
                    + "    GRADE,"
                    + "    HR_CLASS"
                ;
        }

        private void setStudentOnHr(final DB2UDB db2) throws SQLException {
            for (final Iterator it = getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                setStudent(db2, hr);
            }
        }

        private void setStudent(final DB2UDB db2, final HomeRoom hr) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStudents(hr));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String abbv = rs.getString("NAME");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String entName = rs.getString("ENT_NAME");

                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final CourseInfo courseInfo = new CourseInfo(courseCd, majorCd, courseCode);

                    final Student student = new Student(schregno, abbv, entName, hr, attendNo, courseInfo);
                    hr._students.put(schregno, student);
                }
                log.debug("年組=" + hr + "の総数=" + hr._students.size());
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public String sqlStudents(final HomeRoom hr) {
            final String sql;
            sql = "select"
                    + "    T1.SCHREGNO,"
                    + "    T2.NAME,"
                    + "    T1.ATTENDNO,"
                    + "    L1.NAME1 AS ENT_NAME,"
                    + "    T1.COURSECD,"
                    + "    T1.MAJORCD,"
                    + "    T1.COURSECODE"
                    + "  from SCHREG_REGD_DAT T1 inner join SCHREG_BASE_MST T2 on T1.schregno = T2.schregno"
                    + "    left join NAME_MST L1 ON L1.NAMECD1 = 'A002'"
                    + "         and L1.NAMECD2 =T2.ENT_DIV"
                    + "  where"
                    + "    T1.YEAR = '" + _year + "' and"
                    + "    T1.SEMESTER = '" + _semester + "' and"
                    + "    T1.GRADE = '" + hr._grade + "' and"
                    + "    T1.HR_CLASS = '" + hr._room + "' and"
                    + "    (T2.GRD_DIV is null or T2.GRD_DIV not in('2', '3')) and" // 2:退学, 3:転学
                    + "    (T2.GRD_DATE is null or T2.GRD_DATE >= '" + _date + "')"
                ;
            return sql;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CLASSCD");
                    final String name = rs.getString("CLASSNAME");
                    final String abbv = rs.getString("CLASSABBV");
                    rtn.put(code, new Class(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        public String sqlClasses() {
            if ("1".equals(_param._useCurriculumcd)) {
                return "select"
                + "    CLASSCD || SCHOOL_KIND AS CLASSCD,"
                + "    CLASSNAME,"
                + "    CLASSABBV"
                + "  from V_CLASS_MST"
                + "  where"
                + "    YEAR = '" + _year + "'"
                + "  order by"
                + "    CLASSCD || SCHOOL_KIND "
            ;
            } else {
                return "select"
                + "    CLASSCD,"
                + "    CLASSNAME,"
                + "    CLASSABBV"
                + "  from V_CLASS_MST"
                + "  where"
                + "    YEAR = '" + _year + "'"
                + "  order by"
                + "    CLASSCD "
            ;
            }
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSubClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String name = rs.getString("SUBCLASSNAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    rtn.put(code, new SubClass(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            if ("1".equals(_param._useCurriculumcd)) {
                return "select"
                + "    CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD,"
                + "    SUBCLASSNAME,"
                + "    SUBCLASSABBV"
                + "  from V_SUBCLASS_MST"
                + "  where"
                + "    YEAR = '" + _year + "'"
                + "  order by"
                + "    CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD"
            ;
            } else {
                return "select"
                + "    SUBCLASSCD,"
                + "    SUBCLASSNAME,"
                + "    SUBCLASSABBV"
                + "  from V_SUBCLASS_MST"
                + "  where"
                + "    YEAR = '" + _year + "'"
                + "  order by"
                + "    SUBCLASSCD"
            ;
            }
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCombined());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
                    sakiSet.add(combined);
                    motoSet.add(attend);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // 合併先
            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
                final String saki = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(saki);
                if (null != subClass) {
                    subClass.setSaki();
                }
            }
            // 合併元
            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
                final String moto = (String) it.next();
                final SubClass subClass = (SubClass) _subClasses.get(moto);
                if (null != subClass) {
                    subClass.setMoto();
                }
            }
        }

        public String sqlCombined() {
            if ("1".equals(_param._useCurriculumcd)) {
                return "select distinct"
                + "  COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,"
                + "  ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
                + " from SUBCLASS_REPLACE_COMBINED_DAT"
                + " where"
                + "  YEAR = '" + _year + "'"
            ;
            } else {
                return "select distinct"
                + "  COMBINED_SUBCLASSCD,"
                + "  ATTEND_SUBCLASSCD"
                + " from SUBCLASS_REPLACE_COMBINED_DAT"
                + " where"
                + "  YEAR = '" + _year + "'"
            ;
            }
        }

        private String setStartDate(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sqlStartDate());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sDate = rs.getString("SDATE");
                    rtn = sDate;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("年度開始日付=" + rtn);
            return rtn;
        }

        public String sqlStartDate() {
            return "select"
                    + "    SDATE"
                    + "  from SEMESTER_MST"
                    + "  where"
                    + "    YEAR = '" + _year + "'"
                    + "  and"
                    + "    SEMESTER = '9'"
                ;
        }

        private String setSemesterName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sqlSemesterName());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        public String sqlSemesterName() {
            return "select"
                    + "    SEMESTERNAME"
                    + "  from"
                    + "    SEMESTER_MST"
                    + "  where"
                    + "    YEAR = '" + _year + "'"
                    + "  and"
                    + "    SEMESTER = '" + _combSeme + "'"
                ;
        }

        private void setNameMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlNameMst("Z010", "00"));
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010NameSpare1 = rs.getString("NAMESPARE1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校=" + _z010Name1 + "、成績テーブル=" + getRecordTable());
        }

        private String sqlNameMst(final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     NAME_MST "
                + " WHERE "
                + "         NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }

        public String getRecordTable() {
            return isUseRecordScoreDat() ? "RECORD_SCORE_DAT" : "RECORD_DAT";
        }

        /**
         * record_score_dat使用か?。
         * @return is not nullならtrue
         */
        public boolean isUseRecordScoreDat() {
            return _z010NameSpare1 != null;
        }
        
        public Collection getGradeClass() {
            return (Collection) _homeRooms.get(_grade);
        }

        /**
         * 最終学年か?。
         * @return 3年生ならtrue
         */
        public boolean isLastAnnual() {
            return Integer.valueOf(_grade).intValue() == 3;
        }

        public BigDecimal getLowerLine() {
            return _lowerLine;
        }

        public int getLowerCount() {
            return _lowerCount;
        }

        public int getLowerUnStudyCount() {
            return _lowerUnStudyCount;
        }

        public int getLowerValue() {
            return _lowerValue;
        }

        public Integer getGradCredits() {
            return _gradCredits;
        }

        public Integer getGradCompCredits() {
            return _gradCompCredits;
        }

        public String getYear() {
            return _year;
        }

        public String getSemester() {
            return _semester;
        }

        public String getSemesterName() {
            return _semesterName;
        }

        public boolean getIsGakunenMatu() {
            return _isGakunenMatu;
        }

        public String getGrade() {
            return _grade;
        }

        public String getDate() {
            return _date;
        }

        public String getJapaneseDate() {
            return _japaneseDate;
        }

        public BigDecimal getUpperLine() {
            return _upperLine;
        }
        /**
         * 教科名を得る。
         * @param subClass 科目
         * @return 教科名
         */
        public String getClassName(final SubClass subClass) {
            final String classCode = "1".equals(_param._useCurriculumcd) ? subClass._code.substring(0, 3) : subClass._code.substring(0, 2);
            final Class clazz = (Class) _classes.get(classCode);
            if (null == clazz) {
                return "???";
            }
            return clazz._abbv;
        }

        public String getCredit(final SubClass subClass) {
            final Collection creditMsts = (Collection) _creditMst.get(_param._year);
            final List credits = new ArrayList();
            for (final Iterator it = creditMsts.iterator(); it.hasNext();) {
                final CreditMst creditMst = (CreditMst) it.next();
                if (subClass._code.equals(creditMst._subClassCd)) {
                    credits.add(creditMst._credits);
                }
            }
            if (null == credits || credits.isEmpty()) {
                return null;
            }

            if (credits.size() > 1) {
                final Object max = Collections.max(credits);
                final Object min = Collections.min(credits);
                return max + "〜" + min;
            }

            final String rtn = credits.iterator().next().toString();
            return rtn;
        }

        public String getPrintCredit(final SubClass subClass) {
            final Collection creditMsts = (Collection) _creditMst.get(_param._year);
            for (final Iterator it = creditMsts.iterator(); it.hasNext();) {
                final CreditMst creditMst = (CreditMst) it.next();
                if (subClass._code.equals(creditMst._subClassCd)) {
                    return String.valueOf(creditMst._credits);
                }
            }

            return "";
        }

        public String getPrintAbsenceHigh(final SubClass subClass) {
            final Collection creditMsts = (Collection) _creditMst.get(_param._year);
            for (final Iterator it = creditMsts.iterator(); it.hasNext();) {
                final CreditMst creditMst = (CreditMst) it.next();
                if (subClass._code.equals(creditMst._subClassCd)) {
                    return String.valueOf(creditMst._absenceHigh);
                }
            }

            return "";
        }

        public String getPrintAbsenceHigh(final SubClass subClass, final String schregno, final String absenceHigh) {
            if (_knjSchoolMst.isHoutei()) {
                return absenceHigh;
            }
            ResultSet rs = null;
            int pp = 1;
            try {
                _absencePs.setString(pp++, subClass._code);
                _absencePs.setString(pp++, schregno);
                rs = _absencePs.executeQuery();
                while (rs.next()) {
                    return null == rs.getString("COMP_ABSENCE_HIGH") ? "99" : rs.getString("COMP_ABSENCE_HIGH");
                }
            } catch (SQLException e) {
                log.error("_absencePs", e);
            }

            return "99";
        }

        public CreditMst find(final StudyRec studyRec, final CourseInfo courseInfo) {
            final Collection creditMsts = (Collection) _creditMst.get(studyRec._year);
            for (final Iterator it = creditMsts.iterator(); it.hasNext();) {
                final CreditMst creditMst = (CreditMst) it.next();

                if (!courseInfo.equals(creditMst._courseInfo)) {
                    continue;
                }
                if (!studyRec._subClassCd.equals(creditMst._subClassCd)) {
                    continue;
                }
                return creditMst;
            }
            return null;
        }
    }

    class Form {
        private final String _file;
        private Vrw32alp _svf;

        private int lineIndex = 0;

        private final List _meisai = new ArrayList();

        public Form(final String file, final HttpServletResponse response) throws IOException {
            _file = file;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 4);
        }

        public void printHeader() {

            final int grade = Integer.parseInt(_param._grade);  // ZERO SUPPRESS
            _svf.VrsOut("TITLE", _param._semesterName + " 成績判定会議総括資料(" + grade + "年)");

            final int year = Integer.parseInt(_param._year);
            final String gengou = KenjaProperties.gengou(year);
            _svf.VrsOut("NENDO", gengou + "年度");

            // 在籍の表
            _svf.VrsOutn("ITEM", 1, gengou + "年度当初");

            String month = "?";
            try {
                final Calendar cal = KNJServletUtils.parseDate(_param._date);
                month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            } catch (ParseException e) {
                log.error("ParseException", e);
            }
            _svf.VrsOutn("ITEM", 2, month + "月現在");
        }

        public void testPrint() {
            // ヘッダー
            _svf.VrsOut("TITLE", "たいとる");
            _svf.VrsOut("NENDO", "1hoge");

            // 在籍
            _svf.VrsOut("DATE", "2hoge");

            _svf.VrsOutn("ITEM", 2, "3hoge");
            _svf.VrsOutn("CLASS1", 2, "4hoge");

            _svf.VrsOutn("A_CNT1_1", 2, "5hoge");
            _svf.VrsOutn("A_CNT1_2", 2, "6(hoge)");
            _svf.VrsOutn("A_CNT1_3", 2, "7(777)");
            _svf.VrsOutn("REMARK1_1", 2, "あいうえお");
            final StringBuffer sb = new StringBuffer(64);
            for (int i = 0; i < 20; i++) {
                sb.append("１２３４５６７８９★");
            }
            _svf.VrsOutn("REMARK1_2", 3, sb.toString());

            // 成績優良者…
            _svf.VrsOut("SUBTITLE", "３．成績優良者(評定平均 xx.x以上)、不振者(評定平均 xx.x以上)、皆勤者");
            _svf.VrsOutn("CLASS2", 2, "KUMI2");
            _svf.VrsOutn("C_CNT1", 2, "123");   // 優良者
            _svf.VrsOutn("C_CNT2", 2, "234");   // 不振者
            _svf.VrsOutn("C_CNT3", 2, "345");   // 皆勤者

            // 卒業見込み者…
            _svf.VrsOutn("CLASS3", 2, "KUMI3");
            _svf.VrsOutn("D_CNT1", 2, "456");   // 卒業見込み者
            _svf.VrsOutn("D_CNT2", 2, "567");   // 見込みが立たない人
            _svf.VrsOutn("D_CNT3", 2, "678");   // 計

            // 評定分布表
            for (int i = 0; i < 4; i++) {
                _svf.VrsOut("COURSE", "COURSE");
                _svf.VrsOut("SUBJECT", "SUBJECT");
                _svf.VrsOut("CREDIT", "CREDIT");

                _svf.VrsOut("B_CNT1", "111");
                _svf.VrsOut("B_CNT2", "222");
                _svf.VrsOut("B_CNT3", "333");
                _svf.VrsOut("B_CNT4", "444");
                _svf.VrsOut("B_CNT5", "555");

                _svf.VrsOut("B_CNT_NONE", "0011aa");
                _svf.VrsOut("B_CNT_TOTAL", "987");
                _svf.VrsOut("VALU_AVERAGE1", "876");

                _svf.VrEndRecord(); // RECORDは最後に実行する必要がある
            }
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }

        void printCurrentHrClass(final String field) {
            int i = 1;
            for (final Iterator it = _param._homeRooms.values().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                _svf.VrsOutn(field, i, hr._abbv);
                i++;
            }
        }

        public void printZaiseki(final Collection homeRooms, final MultiMap transferStudents) {
            int i = 1;
            for (final Iterator it = homeRooms.iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                final Collection students = (Collection) transferStudents.get(hr);
                if (null != students) {
                    final String buf = studentsToString(students);
                    try {
                        final int len = buf.getBytes("MS932").length;
                        final String field = len < 150 ? "REMARK1_1" : "REMARK1_2";
                        _svf.VrsOutn(field, i, buf);
                    } catch (final UnsupportedEncodingException e) {
                        log.error("バイト数を算出できない", e);
                        _svf.VrsOutn("REMARK1_2", i, buf);
                    }
                }
                i++;
            }
        }

        private String studentsToString(final Collection coll) {
            StringBuffer buf = new StringBuffer();
            Iterator e = coll.iterator();
            int maxIndex = coll.size() - 1;
            for (int i = 0; i <= maxIndex; i++) {
                buf.append(String.valueOf(e.next()));
                if (i < maxIndex)
                    buf.append(", ");
            }
            return buf.toString();
        }

        public void printZaiseki(
                Collection homeRooms,
                Total[] normalValue,
                Total[] transferValue,
                final int column
        ) {
            int i = 1;
            for (final Iterator it = homeRooms.iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                final Integer maleValue = normalValue[0].get(hr);
                final Integer maleTransfer = transferValue[0].get(hr);
                if (null != maleValue) {
                    final String v = getValueString(maleValue, maleTransfer);
                    _svf.VrsOutn("A_CNT" + column + "_1", i, v);
                }

                final Integer femaleValue = normalValue[1].get(hr);
                final Integer femaleTransfer = transferValue[1].get(hr);
                if (null != femaleValue) {
                    final String v = getValueString(femaleValue, femaleTransfer);
                    _svf.VrsOutn("A_CNT" + column + "_2", i, v);
                }

                final Integer total = add(maleValue, femaleValue);
                final Integer transfer = add(maleTransfer, femaleTransfer);
                if (null != total) {
                    final String v = getValueString(total, transfer);
                    _svf.VrsOutn("A_CNT" + column + "_3", i, v);
                }
                i++;
            }

            final int totalIndex = 11;
            final Integer maleTotal = normalValue[0].getTotal();
            final Integer maleTransfer = transferValue[0].getTotal();
            _svf.VrsOutn("A_CNT" + column + "_1", totalIndex, getValueString(maleTotal, maleTransfer));

            final Integer femaleTotal = normalValue[1].getTotal();
            final Integer femaleTransfer = transferValue[1].getTotal();
            _svf.VrsOutn("A_CNT" + column + "_2", totalIndex, getValueString(femaleTotal, femaleTransfer));

            final Integer total = add(maleTotal, femaleTotal);
            final Integer transfer = add(maleTransfer, femaleTransfer);
            if (null != total) {
                _svf.VrsOutn("A_CNT" + column + "_3", totalIndex, getValueString(total, transfer));
            }
        }

        private Integer add(final Integer v1, final Integer v2) {
            if (null == v1 && null == v2) {
                return null;
            }

            if (null == v1) {
                return v2;
            }
            if (null == v2) {
                return v1;
            }
            return new Integer(v1.intValue() + v2.intValue());
        }

        private String getValueString(final Integer mainValue, Integer subValue) {
            if (null == subValue) {
                return mainValue.toString();
            }
            return mainValue + "(" + subValue + ")";
        }
    }

    class HomeRoom implements Comparable {
        private final String _grade;
        private final String _room;
        private final String _name;
        private final String _abbv;

        final Map _students = new HashMap();

        public HomeRoom(final String grade, final String hrClass, final String name, final String abbv) {
            _grade = grade;
            _room = hrClass;
            _name = name;
            _abbv = abbv;
        }

        public String toString() {
            return _abbv + "(" + _grade + "-" + _room + ")";
        }

        public int compareTo(Object o) {
            if (!(o instanceof HomeRoom)) {
                return -1;
            }
            final HomeRoom that = (HomeRoom) o;

            // 学年
            if (!this._grade.equals(that._grade)) {
                return this._grade.compareTo(that._grade);
            }

            // 組（学年が同じだった場合）
            return this._room.compareTo(that._room);
        }

        public Student getStudent(final String schregno) {
            return (Student) _students.get(schregno);
        }

        public Collection getStudents() {
            return _students.values();
        }

        public String getGrade() {
            return _grade;
        }

        public String getRoom() {
            return _room;
        }

        public String getName() {
            return _name;
        }

        /**
         * 成績優良者、不振者を得る。
         * @return [0]=成績優良者, [1]=不振者
         */
        public Integer[] calcGoodOrBad() {
            int good = 0;
            int bad1 = 0;
            int bad2 = 0;
            int bad3 = 0;

            for (final Iterator it = _students.values().iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                // 優良者判定
                if (student.isGood()) {
                    good++;
                }

                // 不振者判定
                if (student.isBadAvgUnder(_param._lowerLine)) {
                    bad1++;
                } else  if (student.isBadCountUnder(_param._lowerCount)) {
                    bad2++;
                } else if (student.isBadUnStudy(_param._lowerUnStudyCount)) {
                    bad3++;
                }
            }

            final Integer[] rtn = {new Integer(good), new Integer(bad1 + bad2 + bad3), };
            return rtn;
        }
    }

    private class Zaiseki {
        Total[] _firstYear;
        Total[] _firstYearTransfer;

        Total[] _nowYear;
        Total[] _nowYearTransfer;

        MultiMap _transferStudents;

        public Zaiseki() {
            // xx現在の日付
            _form._svf.VrsOut("DATE", _param._japaneseDate);

            // 年組
            int i = 1;
            for (final Iterator it = _param._homeRooms.values().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                _form._svf.VrsOutn("CLASS1", i, hr._abbv);
                i++;
            }
        }

        /**
         * 「年・組・男女」別の集計を取る。
         * @param db2 db2
         */
        public void loadCount(final DB2UDB db2) throws SQLException {
            // 年度当初
            _firstYear = getCounts(db2, FIRST_SEMESTER, _param._startDate);
            _firstYearTransfer = getTransferCounts(db2, FIRST_SEMESTER, _param._startDate);

            // 月現在
            _nowYear = getCounts(db2, _param._semester, _param._date);
            _nowYearTransfer = getTransferCounts(db2, _param._semester, _param._date);

            // 備考
            _transferStudents = loadTransferStudent(db2);
            for (final Iterator it = _transferStudents.keySet().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final Collection students = (Collection) _transferStudents.get(hr);
                log.debug(hr + ", 異動した生徒の総人数=" + students.size());
            }
        }

        private MultiMap loadTransferStudent(final DB2UDB db2) throws SQLException {
            final MultiMap rtn = new MultiHashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlTransferStudent());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");

                    final String grade = rs.getString("grade");
                    final String hrClass = rs.getString("hr_class");
                    final HomeRoom hr = _param.getHomeRoom(grade, hrClass);
                    if (null == hr) {
                        continue;
                    }

                    final Date date = rs.getDate("transfer_sdate");
                    final String reason = rs.getString("transferreason");

                    final Student student = hr.getStudent(schregno);
                    if (null == student) {
                        continue;
                    }
                    student.set(date, reason);
                    rtn.put(hr, student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        // TODO: SCHREG_TRANSFER_DAT を3箇所で見ている。まとめよ!

        private String sqlTransferStudent() {
            final String sql;
            sql = "SELECT"
                + " t1.schregno,"
                + " t3.grade,"
                + " t3.hr_class,"
                + " t1.transfer_sdate,"
                + " t1.transferreason"
            + " FROM"
                + " schreg_transfer_dat t1,"
                + " schreg_base_mst t2,"
                + " schreg_regd_dat t3"
            + " WHERE"
                + " t1.schregno = t2.schregno AND"
                + " t1.schregno = t3.schregno AND"
                + " t3.year='" + _param._year + "' AND"
                + " t3.semester='" + _param._semester + "' AND"
                + " t3.grade='" + _param._grade + "' AND"
                + " t1.TRANSFERCD in ('1', '2') AND"    // 1=留学, 2=休学
                + "'" + _param._date + "' between t1.transfer_sdate AND t1.transfer_edate"
                ;
            return sql;
        }

        private Total[] getTransferCounts(DB2UDB db2, String semester, String date) throws SQLException {
            final Total[] rtn = {new Total(), new Total()};   // [0]=male, [1]=female

            for (final Iterator it = _param._homeRooms.values().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                final String sql = sqlTransfer(_param, hr, FIRST_SEMESTER, date);
                final Integer[] sexCounts = getSexCounts(db2, _param, hr, sql);
                rtn[0].add(hr, sexCounts[0]);
                rtn[1].add(hr, sexCounts[1]);
            }
            return rtn;
        }

        private Total[] getCounts(final DB2UDB db2, final String semester, final String date) throws SQLException {
            final Total[] rtn = {new Total(), new Total()};   // [0]=male, [1]=female

            for (final Iterator it = _param._homeRooms.values().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final String sql = sqlCount(_param, hr, semester, date);
                final Integer[] sexCounts = getSexCounts(db2, _param, hr, sql);
                rtn[0].add(hr, sexCounts[0]);
                rtn[1].add(hr, sexCounts[1]);
            }
            return rtn;
        }

        public void printCount(final Collection homeRooms) {
            _form.printZaiseki(homeRooms, _firstYear, _firstYearTransfer, 1);
            _form.printZaiseki(homeRooms, _nowYear, _nowYearTransfer, 2);
            _form.printZaiseki(homeRooms, _transferStudents);
        }

        private Integer[] getSexCounts(
                final DB2UDB db2,
                final Param param,
                final HomeRoom hr,
                final String sql
        ) throws SQLException {
            final Integer[] rtn = new Integer[2];   // 0=male, 1=female
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sexCode = rs.getString("SEX_CODE");
                    final Integer count = KNJServletUtils.getInteger(rs, "SEX_COUNT");

                    if ("1".equals(sexCode)) {
                        rtn[0] = count;
                    } else if ("2".equals(sexCode)) {
                        rtn[1] = count;
                    } else {
                        log.warn("性別コードが不正(" + hr + "):" + sexCode);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlCount(final Param param, final HomeRoom hr, final String semester, final String date) {
            final String sql;
            sql = " select"
                    + "    T1.SEX as SEX_CODE,"
                    + "    count(T1.SEX) as SEX_COUNT"
                    + " from"
                    + "    SCHREG_BASE_MST T1 inner join SCHREG_REGD_DAT T2 on T1.SCHREGNO = T2.SCHREGNO"
                    + " where"
                    + "    T2.YEAR='" + _param._year + "' and"
                    + "    T2.SEMESTER='" + semester + "' and"
                    + "    (T1.GRD_DATE is null or T1.GRD_DATE >= '" + date + "') and"  // RGD_DATE は在籍に含めます
                    + "    T2.GRADE='" + hr._grade + "' and"
                    + "    T2.HR_CLASS='" + hr._room + "'"
                    + " group by T1.SEX"
            ;
            return sql;
        }

        private String sqlTransfer(final Param param, final HomeRoom hr, final String semester, final String date) {
            final String sql;
            sql = " select"
                    + "    T1.SEX as SEX_CODE,"
                    + "    count(T1.SEX) as SEX_COUNT"
                    + " from"
                    + "    SCHREG_BASE_MST T1"
                    + "      inner join SCHREG_REGD_DAT T2 on T1.SCHREGNO = T2.SCHREGNO"
                    + "      inner join SCHREG_TRANSFER_DAT T3 on T1.SCHREGNO = T3.SCHREGNO"
                    + " where"
                    + "    T2.YEAR='" + _param._year + "' and"
                    + "    T2.SEMESTER='" + semester + "' and"
                    + "    (T1.GRD_DATE is null or T1.GRD_DATE > '" + date + "') and"
                    + "    T2.GRADE='" + hr._grade + "' and"
                    + "    T2.HR_CLASS='" + hr._room + "' and"
                    + "    T3.TRANSFERCD in ('1', '2') and" // 1=留学, 2=休学
                    + "    '" + date + "' between T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE"
                    + " group by T1.SEX"
            ;
            return sql;
        }
    }

    private class Good {
        /** 成績優良者 */
        private final Map _goodCount = new HashMap();
        /** 成績不振者 */
        private final Map _badCount = new HashMap();
        /** 皆勤者 */
        private MultiMap _fullCount = new MultiHashMap();

        public Good() {
            final String upper = (null == _param._upperLine) ? "??" : _param._upperLine.toString();
            final String lower = (null == _param._lowerLine) ? "??" : _param._lowerLine.toString();

            final String cond0 = "(評定平均 " + lower + "以下 ";
            final String cond1 = "又は 評定" + _param._lowerValue + "が" + _param._lowerCount + "科目以上";
            final String badCondition = cond0 + cond1 + " 又は 未履修科目が" + _param._lowerUnStudyCount + "科目以上)";
            _form._svf.VrsOut("SUBTITLE", "３．成績優良者(評定平均 " + upper + "以上)、不振者" + badCondition + "、皆勤者");

            _form.printCurrentHrClass("CLASS2");
        }

        public void count(final DB2UDB db2) throws SQLException {
            // 優良者、不振者
            for (final Iterator it = _param.getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final Integer[] ans = hr.calcGoodOrBad();
                _goodCount.put(hr, ans[0]);
                _badCount.put(hr, ans[1]);
            }

            // 皆勤者(欠席、遅刻、早退、欠課がゼロの人)
            for (final Iterator it = _param.getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                final String[] schregnos = (String[]) hr._students.keySet().toArray(new String[0]);

                final Map semesterMap = Attend.get2(db2, _param._year, _param._date, schregnos, true, _param._useVirus, _param._useKoudome);
                final Map subClassMap = Attend.get3(db2, _param._year, _param._date, schregnos, true, _param._useVirus, _param._useKoudome);
                for (final Iterator it2 = semesterMap.keySet().iterator(); it2.hasNext();) {
                    final String schregno = (String) it2.next();

                    final Attend attendSemester = (Attend) semesterMap.get(schregno);
                    final Attend attendSubclass = (Attend) subClassMap.get(schregno);

                    if (attendSemester == null || attendSubclass == null) {
                        continue;
                    }

                    if (attendSemester.isFullAttend() && attendSubclass.isFullAttend()) {
                        _fullCount.put(hr, attendSemester._schregno);
                    }
                }
            }
        }

        public void printCount() {
            int totalGood = 0;
            int totalBad = 0;
            int totalFullCount = 0;

            int i = 1;
            for (final Iterator it = _param.getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                // 優良者、不振者
                final Integer goodCount = (Integer) _goodCount.get(hr);
                if (null != goodCount) {
                    _form._svf.VrsOutn("C_CNT1", i, goodCount.toString());
                    totalGood += goodCount.intValue();
                }

                final Integer badCount = (Integer) _badCount.get(hr);
                if (null != badCount) {
                    _form._svf.VrsOutn("C_CNT2", i, badCount.toString());
                    totalBad += badCount.intValue();
                }

                // 皆勤者
                final Collection students = (Collection) _fullCount.get(hr);
                log.debug("皆勤者(" + hr + "): " + students);
                final int n = (null == students) ? 0 : students.size();
                totalFullCount += n;
                _form._svf.VrsOutn("C_CNT3", i, String.valueOf(n));

                i++;
            }
            _form._svf.VrsOutn("C_CNT1", 11, String.valueOf(totalGood));    // 優良者合計
            _form._svf.VrsOutn("C_CNT2", 11, String.valueOf(totalBad));     // 不振者合計
            _form._svf.VrsOutn("C_CNT3", 11, String.valueOf(totalFullCount));   // 皆勤者合計
        }
    }

    private class Hyoutei {
        private final HashMap _map = new MultiHashMap();
        private final Map _answerMap = new HashMap();

        public Hyoutei(final Set records) {
            for (final Iterator it = records.iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                _map.put(record._subClass, record);
            }
        }

        public void count() {
            for (final Iterator it = _map.keySet().iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();

                final Collection coll = (Collection) _map.get(subClass);
                final int[] ans = countUp(coll);
                _answerMap.put(subClass, ans);
            }
        }

        /**
         * 集計する。
         * @param coll 集計対象
         * @return 5段階評価＋なしの配列。[0]=なし, [1]=評定1の人数, ...[5]=評定5の人数
         */
        private int[] countUp(final Collection coll) {
            final int[] rtn = new int[6];
            for (final Iterator it = coll.iterator(); it.hasNext();) {
                final Record record = (Record) it.next();

                final Integer gradValue = record._value;
                if (KNJServletUtils.isEmpty(gradValue)) {
                    rtn[0]++;   // 「なし」をカウントアップ
                    continue;
                }

                final int value = gradValue.intValue();
                if (value >= 1 && value <= 5) {
                    rtn[value]++;
                } else {
//                    log.warn("想定外の評定:" + record + " =" + value);
                }
            }
            return rtn;
        }

        public void printCount() {
            final Set keySet = _answerMap.keySet();
            final Set sorted = new TreeSet(keySet);
            for (final Iterator it = sorted.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                final int[] ans = (int[]) _answerMap.get(subClass);

                _form._svf.VrsOut("COURSE", _param.getClassName(subClass));
                _form._svf.VrsOut("SUBJECT", subClass._name);
                _form._svf.VrsOut("CREDIT", _param.getCredit(subClass));

                _form._svf.VrsOut("B_CNT1", String.valueOf(ans[1]));
                _form._svf.VrsOut("B_CNT2", String.valueOf(ans[2]));
                _form._svf.VrsOut("B_CNT3", String.valueOf(ans[3]));
                _form._svf.VrsOut("B_CNT4", String.valueOf(ans[4]));
                _form._svf.VrsOut("B_CNT5", String.valueOf(ans[5]));
                _form._svf.VrsOut("B_CNT_NONE", String.valueOf(ans[0]));

                // 合計
                int total = 0;
                for (int i = 0; i < ans.length; i++) {
                    total += ans[i];
                }
                _form._svf.VrsOut("B_CNT_TOTAL", String.valueOf(total));

                // 平均
                final double dAvg = calcAvg(ans);
                final BigDecimal avg = new BigDecimal(String.valueOf(dAvg));
                final String avgStr = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                _form._svf.VrsOut("VALU_AVERAGE1", avgStr);

                log.debug("教科=" + _param.getClassName(subClass) + ", 科目=" + subClass + ", 平均=" + avgStr);
                _form._svf.VrEndRecord();
            }
        }

        /**
         * 平均を算出する。
         * @param ans 5段階評価＋なしの配列。[0]=なし, [1]=評定1の人数, ...[5]=評定5の人数
         * @return 平均
         */
        private double calcAvg(final int[] ans) {
            final int ninzu = ans[5] + ans[4] + ans[3] + ans[2] + ans[1];
            if (0 >= ninzu) {
                return 0;
            }
            final double rtn = (5 * ans[5]
                  + 4 * ans[4]
                  + 3 * ans[3]
                  + 2 * ans[2]
                  + 1 * ans[1]) / (double) ninzu;
            ;
            return rtn;
        }
    }

    class Graduate {

        public void count(final DB2UDB db2) throws SQLException {
            for (final Iterator it = _param.getGradeClass().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();

                for (final Iterator it2 = hr.getStudents().iterator(); it2.hasNext();) {
                    final Student student = (Student) it2.next();

                    setPrevCourseInfo(db2, student);        // 過去のコース情報
                    setStudyRecOnStudent0(db2, student);    // 全ての学習データ
                }
            }
        }

        private void setPrevCourseInfo(final DB2UDB db2, final Student student) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlPrevCourseInfo(student));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("year");

                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final CourseInfo courseInfo = new CourseInfo(courseCd, majorCd, courseCode);

                    student._prevCourseInfo.put(year, courseInfo);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlPrevCourseInfo(final Student student) {
            final String sql;
            sql = "SELECT"
                + "  year,"
                + "  max(semester) as max_semester,"
                + "  grade,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode"
                + " FROM"
                + "  schreg_regd_dat"
                + " WHERE"
                + "  year < '" + _param._year + "' AND"
                + "  schregno = '" + student._schregno + "'"
                + " GROUP BY"
                + "  year,"
                + "  grade,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode"
                ;
            return sql;
        }

        private void setStudyRecOnStudent0(final DB2UDB db2, final Student student) throws SQLException {
            // TAKAESU: 「'05年&1年生/'06年&2年生/'07年&3年生」とSQL文を3回発行で早くなる!?
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStudyRec());
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("year");
                    final String annual = rs.getString("annual");
                    final String subClassCd = rs.getString("subclasscd");
                    final Integer getCredit = KNJServletUtils.getInteger(rs, "get_credit");
                    final Integer addCredit = KNJServletUtils.getInteger(rs, "add_credit");
                    final Integer compCredit = KNJServletUtils.getInteger(rs, "comp_credit");

                    final StudyRec studyRec = new StudyRec(year, annual, subClassCd, getCredit, addCredit, compCredit);
                    student._studyRec.put(year, studyRec);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlStudyRec() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  year, ");
            stb.append("  annual, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("  CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("  SUBCLASSCD, ");
            }
            stb.append("  get_credit, ");
            stb.append("  add_credit, ");
            stb.append("  comp_credit ");
            stb.append(" FROM ");
            stb.append("  schreg_studyrec_dat ");
            stb.append(" WHERE ");
            stb.append("  schregno=? ");
            return stb.toString();
        }

        public void printCount() {
            _form.printCurrentHrClass("CLASS3");    // 組タイトル

            int graduateTotal = 0;
            int unGraduateTotal = 0;

            int i = 1;
            for (final Iterator it = _param._homeRooms.values().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final int[] buf = getCount(hr);
                _form._svf.VrsOutn("D_CNT1", i, String.valueOf(buf[0]));    // 卒業見込み者
                _form._svf.VrsOutn("D_CNT2", i, String.valueOf(buf[1]));    // 見込みが立たない人
                final int total = buf[0] + buf[1];
                _form._svf.VrsOutn("D_CNT3", i, String.valueOf(total)); // 計

                graduateTotal += buf[0];
                unGraduateTotal += buf[1];

                i++;
            }
            _form._svf.VrsOutn("D_CNT1", 11, String.valueOf(graduateTotal));
            _form._svf.VrsOutn("D_CNT2", 11, String.valueOf(unGraduateTotal));
            _form._svf.VrsOutn("D_CNT3", 11, String.valueOf(graduateTotal + unGraduateTotal));
        }

        /**
         * 卒業見込み者、見込みが立たない人を算出する。
         * @param hr 年組
         * @return [0]=卒業見込み者数, [1]=見込みが立たない人の数
         */
        private int[] getCount(final HomeRoom hr) {
            final int[] rtn = {0, 0};

            for (final Iterator it = hr.getStudents().iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student.isGraduate()) {
                    rtn[0]++;
                } else {
                    rtn[1]++;
//                    debugPrevInfo(student);
                }
            }
            return rtn;
        }
    }

    private void debugPrevInfo(final Student student) {
        log.trace(student._schregno + ": 過去のコース情報=" + student._prevCourseInfo);
        for (final Iterator it = student._studyRec.keySet().iterator(); it.hasNext();) {
            final String year = (String) it.next();

            final Collection studys = (Collection) student._studyRec.get(year);
            log.trace("\t" + year + "年の成績データ数=" + studys.size());
        }
    }

    private class Total {
        private final Map _map = new HashMap();

        public void add(final Object key, final Integer input) {
            if (null == input) {
                return;
            }

            final Integer val = (Integer) _map.get(key);
            if (null == val) {
                _map.put(key, input);
            } else {
                _map.put(key, new Integer(val.intValue() + input.intValue()));
            }
        }

        public Integer get(final Object key) {
            return (Integer) _map.get(key);
        }

        public Integer getTotal() {
            int ans = 0;
            for (final Iterator it = _map.values().iterator(); it.hasNext();) {
                final Integer value = (Integer) it.next();
                ans += value.intValue();
            }
            return new Integer(ans);
        }

        public String toString() {
            return _map.toString();
        }
    }

    /**
     * 教科。
     */
    private class Class {
        private final String _code;
        private final String _name;
        private final String _abbv;

        public Class(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }
    }

    class SubClass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public void setSaki() {
            _hasCombined = true;
            _isSaki = true;
        }

        public void setMoto() {
            _hasCombined = true;
            _isMoto = true;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public Map getSubclassInfo() {
            final Map rtn = new HashMap();
            rtn.put("CODE", _code);
            rtn.put("NAME", _name);
            rtn.put("ABBV", _abbv);
            rtn.put("HAS_COMBINED", String.valueOf(_hasCombined));
            rtn.put("IS_SAKI", String.valueOf(_isSaki));
            rtn.put("IS_MOTO", String.valueOf(_isMoto));

            return rtn;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            final SubClass that = (SubClass) o;
            return this._code.compareTo(that._code);
        }
    }

    class Record implements Comparable {
        private final SubClass _subClass;
        private final String _schregno;
        private final HomeRoom _hr;
        /** 評定 */
        private final Integer _value;

        /** 未履修か? */
        private final boolean _isMirisyu;

        public Record(
                final SubClass subClass,
                final String schregno,
                final HomeRoom hr,
                final Integer value,
                final Integer getCredit,
                final Integer compCredit
        ) {
            _subClass = subClass;
            _schregno = schregno;
            _hr = hr;
            _value = value;

            if (_param._isGakunenMatu) {
                final boolean getCreditEmpty = KNJServletUtils.isEmpty(getCredit);
                final boolean compCreditEmpty = KNJServletUtils.isEmpty(compCredit);
                _isMirisyu = getCreditEmpty && compCreditEmpty;
            } else {
                final boolean getValueEmpty = KNJServletUtils.isEmpty(value);
                _isMirisyu = getValueEmpty;
            }
        }

        public SubClass getSubclass() {
            return _subClass;
        }

        public HomeRoom getHomeRoom() {
            return _hr;
        }

        public String getSchregno() {
            return _schregno;
        }

        public Integer getGradValue() {
            return _value;
        }

        public String toString() {
            return _subClass + ":" + _schregno + ":" + _hr + ":" + _value;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Record)) {
                return -1;
            }
            final Record that = (Record) o;
            return this._subClass.compareTo(that._subClass);
        }
    }

    class Student implements Comparable {
        final String _schregno;
        final String _abbv;
        final String _entName;
        final HomeRoom _hr;
        final String _attendNo;
        /** コース情報(課程、学科、コース) */
        final CourseInfo _courseInfo;

        private final Map _records = new HashMap();

        /** 評定n以下の数 */
        int _floorCreditCount = 0;

        /** 必履修科目のうちの未履修科目の数 */
        int _mirisyuCount = 0;

        /** 異動事由 */
        private String _reason;
        /** 異動期間開始日付。 Note: 本来は複数のはず */
        private Date _transferSDate;
        /** 学籍異動データ.留学修得単位数 の合計 */
        private int _transferTotalAbroadCredits = 0;
        // TODO: 上記の異動情報関連を整理せよ!

        /** 成績データ */
        MultiMap _studyRec = new MultiHashMap();
        /** 過去のコース情報(課程、学科、コース) */
        private Map _prevCourseInfo = new HashMap();

        public Student(
                final String schregno,
                final String abbv,
                final String entName,
                final HomeRoom hr,
                final String attendNo,
                final CourseInfo courseInfo
        ) {
            _schregno = schregno;
            _abbv = abbv;
            _entName = entName;
            _hr = hr;
            _attendNo = attendNo;
            _courseInfo = courseInfo;
        }

        /**
         * 不振者か?
         * @param lowerUnStudyCount
         * @return
         */
        public boolean isBadUnStudy(int lowerUnStudyCount) {
            final boolean isBad = _mirisyuCount >= lowerUnStudyCount;

            if (isBad) {
                _form._meisai.add("成績不振者:" + info() + ":未履修数が" + lowerUnStudyCount + "つ以上");
            }
            return isBad;
        }

        /**
         * 不振者か?
         * @param lowerCount 基準となる評定の数
         * @return 評定n以下の数が lowerCount以下なら true
         */
        public boolean isBadCountUnder(int lowerCount) {
            final boolean isBad = _floorCreditCount >= lowerCount;

            if (isBad) {
                _form._meisai.add("成績不振者:" + info() + ":評定" + _param._lowerValue + "以下が" + lowerCount + "つ以上");
            }
            return isBad;
        }

        /**
         * 不振者か?
         * @param lowerLine 基準となる評定平均
         * @return 評定平均が lowerLine 以下なら true
         */
        public boolean isBadAvgUnder(final BigDecimal lowerLine) {
            final double creditAvg = getGradValueAvg(1, BigDecimal.ROUND_HALF_UP);
            final boolean isBad = creditAvg <= lowerLine.doubleValue();

            if (isBad) {
                _form._meisai.add("成績不振者:" + info() + ":評定平均(" + creditAvg + ")が" + lowerLine + "以下");
            }
            return isBad;
        }

        /**
         * 優良者か?
         * @return 優良者なら true
         */
        public boolean isGood() {
            final double creditAvg = getGradValueAvg(1, BigDecimal.ROUND_HALF_UP);
            return creditAvg >= _param._upperLine.doubleValue();
        }

        public void addRecord(final Record rec) {
            _records.put(rec._subClass._code, rec);

            if (null == rec._value || rec._value.intValue() <= _param._lowerValue) {
                _floorCreditCount++;
            }

            if (rec._isMirisyu) {
                _mirisyuCount++;
            }
        }

        public double getGradValueAvg(int scale, int roundingMode) {
            final String creditAvg = String.valueOf(getGradValueAvg());
            final BigDecimal wrk = new BigDecimal(creditAvg).setScale(scale, roundingMode);
            return wrk.doubleValue();
        }

        /**
         * 評定平均を得る。
         * @return 評定平均
         */
        public double getGradValueAvg() {
            int credit = 0;
            int count = 0;
            for (final Iterator it = _records.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                if (null == record._value) {
                    continue;
                }
                final SubClass subClass = record._subClass;
                if (subClass._hasCombined) {
                    final boolean isGakunenMatu = _param._isGakunenMatu;
                    if (isGakunenMatu && subClass._isMoto || !isGakunenMatu && subClass._isSaki) {
                        log.debug("合併科目の無視条件に合致:" + subClass + ", 学年末か? " + isGakunenMatu);
                        continue;
                    }
                }
                credit += record._value.intValue();
                count++;
            }
            if (0 == count) {
                return 0;
            }
            return credit / (double) count;
        }

        /**
         * 卒業見込みか?
         * @return 見込みなら true
         */
        public boolean isGraduate() {
            // 卒業単位数以上、修得しているか?
            if (null != _param._gradCredits) {
                final int gradCredits = _param._gradCredits.intValue();
                final int studyRecTotal = calcStudyRecCredit();
                if (studyRecTotal < gradCredits) {
                    _form._meisai.add("卒業不可:" + info() + ": 卒業単位数未満:" + studyRecTotal);
                    return false;
                }
            }

            // 必履修科目を履修しているか?
            for (final Iterator it = _studyRec.values().iterator(); it.hasNext();) {
                final StudyRec studyRec = (StudyRec) it.next();

                final CourseInfo courseInfo;
                if (studyRec._year.equals(_param._year)) {
                    courseInfo = _courseInfo;
                } else {
                    courseInfo = (CourseInfo) _prevCourseInfo.get(studyRec._year);
                }

                // TODO: 緊急対応!国分寺は移行データの都合上、1,2年は無視する。
                if (!studyRec._year.equals(_param._year)) {
                    continue;
                }
                final CreditMst creditMst = _param.find(studyRec, courseInfo);
                if (null == creditMst) {
//                    log.debug("単位マスタが見つからない。学習データ=" + studyRec + ", コース情報=" + courseInfo);
                    continue;
                }
                if (creditMst._required) {
                    final int credits = creditMst._credits.intValue();
                    if (null == studyRec._compCredit) {
                        _form._meisai.add("卒業不可:" + info() + ": 学習データ、" + studyRec + "の履修単位がnullだから");
                        return false;
                    }
                    final int compCredit = studyRec._compCredit.intValue();
                    if (credits > compCredit) {
                        _form._meisai.add("卒業不可:" + info() + ":履修単位が規定の単位数未満" + compCredit + "<" + credits);
                        return false;
                    }
                }
                
            }

            // 卒業履修単位数以上、履修しているか?
            int studyRecCompCredit = calcStudyRecCompCredit() ;
            if ( studyRecCompCredit < _param.getGradCompCredits().intValue()) {
                _form._meisai.add("卒業不可:" + info() + ":履修が規定の単位数未満:" + studyRecCompCredit + "<" + _param.getGradCompCredits());
                return false;
            }

            return true;
        }

        private String info() {
            final Integer attendNo = Integer.valueOf(_attendNo);
            return _hr._abbv + "-" + attendNo + ":" + _abbv;
        }

        /**
         * 総修得単位数を得る。
         * @return 総修得単位数
         */
        public int calcStudyRecCredit() {
            int ans = 0;
            for (final Iterator it = _studyRec.values().iterator(); it.hasNext();) {
                final StudyRec studyRec = (StudyRec) it.next();

                if (null != studyRec._getCredit) {
                    ans += studyRec._getCredit.intValue();
                }
                if (null != studyRec._addCredit) {
                    ans += studyRec._addCredit.intValue();
                }
            }

            return ans + _transferTotalAbroadCredits;
        }

        /**
         * 総履修単位数を得る。
         * @return 総修得単位数
         */
        public int calcStudyRecCompCredit() {
            int ans = 0;
            for (final Iterator it = _studyRec.values().iterator(); it.hasNext();) {
                final StudyRec studyRec = (StudyRec) it.next();

                if (null != studyRec._compCredit) {
                    ans += studyRec._compCredit.intValue();
                }
            }

            return ans;
        }

        public void set(final Date date, final String reason) {
            _transferSDate = date;
            _reason = reason;
        }

        public Map getRecords() {
            return _records;
        }

        public MultiMap getStudyRecs() {
            return _studyRec;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            return this._attendNo.compareTo(that._attendNo);
        }

        public String toString() {
            final String date;
            if (null != _transferSDate) {
                final String transferSDate = _transferSDate.toString();
                date = (transferSDate.length() >= 5) ? transferSDate.substring(5) : transferSDate;
            } else {
                date = null;
            }

            return _abbv + "/" + date + "/" + _reason;
        }
    }

    private class StudyRec {
        private final String _year;
        private final String _annual;
        private final String _subClassCd;
        /** 修得単位 */
        private final Integer _getCredit;
        /** 増加単位 */
        private final Integer _addCredit;
        /** 履修単位 */
        private final Integer _compCredit;

        public StudyRec(
                final String year,
                final String annual,
                final String subClassCd,
                final Integer getCredit,
                final Integer addCredit,
                final Integer compCredit
        ) {
            _year = year;
            _annual = annual;
            _subClassCd = subClassCd;
            _getCredit = getCredit;
            _addCredit = addCredit;
            _compCredit = compCredit;
        }

        public String toString() {
            return _year + "/" + _annual + "/" + _subClassCd;
        }
    }

    private class CreditMst {
        private final String _year;
        private final String _grade;
        private final CourseInfo _courseInfo;
        private final String _subClassCd;
        private final Integer _credits;
        private final BigDecimal _absenceHigh;
        /** 必履修か? */
        private final boolean _required;

        public CreditMst(
                final String year,
                final String grade,
                final CourseInfo courseInfo,
                final String subClassCd,
                final Integer credits,
                final BigDecimal absenceHigh,
                final String requireFlag
        ) {
            _year = year;
            _grade = grade;
            _courseInfo = courseInfo;
            _subClassCd = subClassCd;
            _credits = credits;
            _absenceHigh = absenceHigh;
            _required = "1".equals(requireFlag) ? true : false;
        }

        public String toString() {
            return _courseInfo + "/" + _subClassCd + "/" + _credits;
        }
    }

    private class CourseInfo {
        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;

        public CourseInfo(final String courseCd, final String majorCd, final String courseCode) {
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }

        public String toString() {
            return _courseCd + _majorCd + _courseCode;
        }

        public boolean equals(final Object obj) {
            if (obj instanceof CourseInfo) {
                final CourseInfo that = (CourseInfo) obj;
                if (!this._courseCd.equals(that._courseCd)) {
                    return false;
                }
                if (!this._majorCd.equals(that._majorCd)) {
                    return false;
                }
                return this._courseCode.equals(that._courseCode);
            }
            return false;
        }
    }
    
    /**
     * 出欠累積。
     * @author takaesu
     * @version $Id: a6a28756e85812e916bcbd4856d4e975a46eb0fd $
     */
    private static class Attend {
        
        private static Log log = LogFactory.getLog(Attend.class);
        private static String revision = "$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $";

        public String _schregno;
        // TODO: 以下の _lesson や _offDays 等は、ATTEND_SEMES_DAT と ATTEND_SUBCLASS_DAT の両方に在るが、意味(単位)が違う!
        public Integer _lesson;
        public Integer _offDays;
        public Integer _absent;
        public Integer _suspend;
        public Integer _mourning;
        public Integer _abroad;
        public Integer _sick;
        public Integer _notice;
        public Integer _nonotice;
        /** 保健室欠課。ATTEND_SUBCLASS_DATにのみある。(ATTEND_SEMES_DATに無い) */
        public Integer _nurseOff;
        public Integer _late;
        public Integer _early;
        public Integer _virus;
        public Integer _koudome;

        private static final String[] MonthTable = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03", };
        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        /*
         * TAKAESU: get1 や get2 メソッドで集計した値は、集計対象の最新月の APPOINTED_DAY が締め日となる。その締め日を返すメソッドを作っても良い。
         */

        /**
         * @deprecated 中途半端で使い道無いと思われる。2013/08/16 使用プログラムがないため保守対象外とする
         * 出欠累積を集計する。
         * @param db2 db2
         * @param year 年度
         * @param schregnos 対象の学籍番号
         * @return 対象生徒全員 and 年度全て の出欠累積の<code>Map</code>
         * @throws SQLException SQLエラー
         */
        public static Map get1(final DB2UDB db2, final String year, final String[] schregnos) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlFullCount1(year, schregnos));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Attend aa = new Attend();
                    aa._schregno = rs.getString("schregno");
                    aa._lesson = KNJServletUtils.getInteger(rs, "lesson");
                    aa._offDays = KNJServletUtils.getInteger(rs, "offdays");
                    aa._absent = KNJServletUtils.getInteger(rs, "absent");
                    aa._suspend = KNJServletUtils.getInteger(rs, "suspend");
                    aa._mourning = KNJServletUtils.getInteger(rs, "mourning");
                    aa._abroad = KNJServletUtils.getInteger(rs, "abroad");
                    aa._sick = KNJServletUtils.getInteger(rs, "sick");
                    aa._notice = KNJServletUtils.getInteger(rs, "notice");
                    aa._nonotice = KNJServletUtils.getInteger(rs, "nonotice");
                    aa._late = KNJServletUtils.getInteger(rs, "late");
                    aa._early = KNJServletUtils.getInteger(rs, "early");

                    rtn.put(aa._schregno, aa);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlFullCount1(final String year, final String[] schregnos) {
            final String sql;
            final String whereSchreg = (null != schregnos && schregnos.length > 0) ? "in " + SQLUtils.whereIn(true, schregnos) : "is null";
            sql = "SELECT"
                + "  schregno,"
                + "  sum(lesson) as lesson,"
                + "  sum(offdays) as offdays,"
                + "  sum(absent) as absent,"
                + "  sum(suspend) as suspend,"
                + "  sum(mourning) as mourning,"
                + "  sum(abroad) as abroad,"
                + "  sum(sick) as sick,"
                + "  sum(notice) as notice,"
                + "  sum(nonotice) as nonotice,"
                + "  sum(late) as late,"
                + "  sum(early) as early"
                + " FROM"
                + "  attend_semes_dat"
                + " WHERE"
                + "  year='" + year + "' AND"
                + "  schregno " + whereSchreg
                + " GROUP BY"
                + "  schregno"
                ;
            return sql;
        }
        
        public static Map get2(
                final DB2UDB db2,
                final String year,
                final String date,
                final String[] schregnos,
                final boolean containNowMonth
        ) throws SQLException {
            return get2(db2, year, date, schregnos, containNowMonth, null, null);
        }

        /**
         * 出欠累積を集計する。(ATTEND_SEMES_DAT)
         * @param db2 db
         * @param year 年度
         * @param date 日付
         * @param schregnos 対象の学籍番号
         * @param containNowMonth 当月を含めるか否か? true=含める
         * @return 対象生徒全員 and 4月〜日付の[前月/当月]までの ATTEND_SEMES_DAT の出欠累積の<code>Map</code>
         * @throws SQLException SQLエラー
         */
        public static Map get2(
                final DB2UDB db2,
                final String year,
                final String date,
                final String[] schregnos,
                final boolean containNowMonth,
                final String useVirus,
                final String useKoudome
        ) throws SQLException {
            final Map rtn = new HashMap();

            final String month = date.substring(5, 7);
            if ("04".equals(month)) {
                return rtn; // 4月だけなら、4月が端数処理対象。
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlFullCount2(year, month, schregnos, containNowMonth, useVirus, useKoudome));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Attend aa = new Attend();
                    aa._schregno = rs.getString("schregno");
                    aa._lesson = KNJServletUtils.getInteger(rs, "lesson");
                    aa._offDays = KNJServletUtils.getInteger(rs, "offdays");
                    aa._absent = KNJServletUtils.getInteger(rs, "absent");
                    aa._suspend = KNJServletUtils.getInteger(rs, "suspend");
                    aa._mourning = KNJServletUtils.getInteger(rs, "mourning");
                    aa._abroad = KNJServletUtils.getInteger(rs, "abroad");
                    aa._sick = KNJServletUtils.getInteger(rs, "sick");
                    aa._notice = KNJServletUtils.getInteger(rs, "notice");
                    aa._nonotice = KNJServletUtils.getInteger(rs, "nonotice");
                    aa._late = KNJServletUtils.getInteger(rs, "late");
                    aa._early = KNJServletUtils.getInteger(rs, "early");
                    if ("true".equals(useVirus)) {
                        aa._virus = KNJServletUtils.getInteger(rs, "virus");
                    }
                    if ("true".equals(useKoudome)) {
                        aa._koudome = KNJServletUtils.getInteger(rs, "koudome");
                    }
                    rtn.put(aa._schregno, aa);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlFullCount2(
                final String year,
                final String month,
                final String[] schregnos,
                final boolean containNowMonth,
                final String useVirus,
                final String useKoudome
        ) {
            // SQL文作成
            final String[] monthArray = getMonthArray(Integer.valueOf(month).intValue(), containNowMonth);   // 4月〜前月/当月まで

            String sql;
            final String whereSchreg = (null != schregnos && schregnos.length > 0) ? "in " + SQLUtils.whereIn(true, schregnos) : "is null";
            sql = "SELECT"
                + "  schregno,"
                + "  sum(lesson) as lesson,"
                + "  sum(offdays) as offdays,"
                + "  sum(absent) as absent,"
                + "  sum(suspend) as suspend,"
                + "  sum(mourning) as mourning,"
                + "  sum(abroad) as abroad,"
                + "  sum(sick) as sick,"
                + "  sum(notice) as notice,"
                + "  sum(nonotice) as nonotice,"
                + "  sum(late) as late,"
                + "  sum(early) as early";
            if ("true".equals(useKoudome)) {
                sql += " ,sum(koudome) as koudome ";
            }
            if ("true".equals(useVirus)) {
                sql += " ,sum(virus) as virus ";
            }
            sql +=" FROM"
                + "  attend_semes_dat"
                + " WHERE"
                + "  year='" + year + "' AND"
//                + "  schregno in " + SQLUtils.whereIn(true, schregnos) + " AND"
                + "  schregno " + whereSchreg + " AND"
                + "  month in " + SQLUtils.whereIn(true, monthArray)
                + " GROUP BY"
                + "  schregno"
                ;
            return sql;
        }

        public static Map get3(
                final DB2UDB db2,
                final String year,
                final String date,
                final String[] schregnos,
                final boolean containNowMonth
        ) throws SQLException {
            return get3(db2, year, date, schregnos, containNowMonth, null, null);
        }

        public static Map get3(
                final DB2UDB db2,
                final String year,
                final String date,
                final String[] schregnos,
                final boolean containNowMonth,
                final String useVirus,
                final String useKoudome
        ) throws SQLException {
            final Map rtn = new HashMap();

            final String month = date.substring(5, 7);
            if ("04".equals(month)) {
                return rtn; // 4月だけなら、4月が端数処理対象。
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlFullCount3(year, month, schregnos, containNowMonth, useVirus, useKoudome));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Attend aa = new Attend();
                    aa._schregno = rs.getString("schregno");
                    aa._lesson = KNJServletUtils.getInteger(rs, "lesson");
                    aa._offDays = KNJServletUtils.getInteger(rs, "offdays");
                    aa._absent = KNJServletUtils.getInteger(rs, "absent");
                    aa._suspend = KNJServletUtils.getInteger(rs, "suspend");
                    aa._mourning = KNJServletUtils.getInteger(rs, "mourning");
                    aa._abroad = KNJServletUtils.getInteger(rs, "abroad");
                    aa._sick = KNJServletUtils.getInteger(rs, "sick");
                    aa._notice = KNJServletUtils.getInteger(rs, "notice");
                    aa._nonotice = KNJServletUtils.getInteger(rs, "nonotice");
                    aa._nurseOff = KNJServletUtils.getInteger(rs, "nurseoff");  // ATTEND_SUBCLASS_DAT only
                    aa._late = KNJServletUtils.getInteger(rs, "late");
                    aa._early = KNJServletUtils.getInteger(rs, "early");
                    if ("true".equals(useVirus)) {
                        aa._virus = KNJServletUtils.getInteger(rs, "virus");
                    }
                    rtn.put(aa._schregno, aa);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlFullCount3(
                final String year,
                final String month,
                final String[] schregnos,
                final boolean containNowMonth,
                final String useVirus,
                final String useKoudome
        ) {
            // SQL文作成
            final String[] pastMonthArray = getMonthArray(Integer.valueOf(month).intValue(), containNowMonth);   // 4月〜前月/当月まで

            String sql;
            final String whereSchreg = (null != schregnos && schregnos.length > 0) ? "in " + SQLUtils.whereIn(true, schregnos) : "is null";
            sql = "SELECT"
                + "  schregno,"
                + "  sum(lesson) as lesson,"
                + "  sum(offdays) as offdays,"
                + "  sum(absent) as absent,"
                + "  sum(suspend) as suspend,"
                + "  sum(mourning) as mourning,"
                + "  sum(abroad) as abroad,"
                + "  sum(sick) as sick,"
                + "  sum(notice) as notice,"
                + "  sum(nonotice) as nonotice,"
                + "  sum(nurseoff) as nurseoff,"
                + "  sum(late) as late,"
                + "  sum(early) as early";
            if ("true".equals(useVirus)) {
                sql += ", sum(virus) as virus";
            }
            if ("true".equals(useKoudome)) {
                sql += ", sum(koudome) as koudome";
            }
            sql +=" FROM"
                + "  attend_subclass_dat"
                + " WHERE"
                + "  year='" + year + "' AND"
                + "  schregno " + whereSchreg + " AND"
                + "  month in " + SQLUtils.whereIn(true, pastMonthArray)
                + " GROUP BY"
                + "  schregno"
                ;
            return sql;
        }

        /*
         * containNowMonth==true : 例えば6月なら、{[0]="04", [1]="05", [2]="06"} を返す
         * containNowMonth==false: 例えば6月なら、{[0]="04", [1]="05"} を返す
         */
        private static String[] getMonthArray(final int month, final boolean containNowMonth) {
            final int index;
            if (containNowMonth) {
                index = (month <= 3) ? month + 9: month - 3;
            } else {
                // TAKAESU: 3月の時、"04"〜"12","01","02" まで。4月の時、空っぽ。本当にこの仕様で良いの?
                index = (month <= 3) ? month + 8: month - 4;
            }
            final String[] rtn = new String[index];
            System.arraycopy(MonthTable, 0, rtn, 0, index);

            return rtn;
        }

        // ==========================================

        /**
         * 皆勤か?
         * @return 皆勤ならtrue
         */
        public boolean isFullAttend() {
            if (null == _offDays || _offDays.intValue() != 0) {
                return false;
            }
            if (null == _sick || _sick.intValue() != 0) {
                return false;
            }
            if (null == _late || _late.intValue() != 0) {
                return false;
            }
            if (null == _early || _early.intValue() != 0) {
                return false;
            }
            if (null == _notice || _notice.intValue() != 0) {
                return false;
            }
            if (null == _nonotice || _nonotice.intValue() != 0) {
                return false;
            }
            // TAKAESU: 以下のコメントを有効にすると、KNJD232 の結果と(高江洲作のKNJD234が)異なってしまう
//            if (null == _nurseOff || _nurseOff.intValue() != 0) {
//                return false;
//            }
            return true;
        }

    }
    
} // KNJD234

// eof
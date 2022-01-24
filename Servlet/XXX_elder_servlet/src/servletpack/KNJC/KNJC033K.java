// kanji=漢字
/*
 * $Id: 4365bef11e33a8333fac149a62b9fa2c9c563667 $
 *
 * 作成日: 2010/05/25 13:47:13 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4365bef11e33a8333fac149a62b9fa2c9c563667 $
 */
public class KNJC033K {

    private static final Log log = LogFactory.getLog("KNJC033K.class");

    private static final String FORM_NAME = "KNJC033K.frm";

    private boolean _hasData;

    static final int LINE_MAX = 50;
    
    private static final String KOTEI_GOUKEI = "1";
    private static final String KOTEI_SYUSSEKI_SUBEKI = "2";
    private static final String KOTEI_KETUJI = "3";
    private static final String KOTEI_TIKOKU = "4";
    private static final String KOTEI_KEKKA = "5";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List pageStudent = getPageStudentMap(db2);

        for (final Iterator iter = pageStudent.iterator(); iter.hasNext();) {
            final List students = (List) iter.next();
            svf.VrSetForm(FORM_NAME, 4);
            svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
            svf.VrsOut("ABSENT", _param._sickName);
            svf.VrsOut("SUBCLASS", "(科目:" + _param._subclassName + ") (講座:" + _param._chairName + ")");
            svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate));

            printMonthData(svf, students);
            printSemesData(svf, students);
            printKoteiData(svf, students);
            _hasData = true;
        }
    }

    private void printMonthData(final Vrw32alp svf, final List students) {
        for (final Iterator itSemMont = _param._semeMonthMap.keySet().iterator(); itSemMont.hasNext();) {
            final String key = (String) itSemMont.next();
            final SemeMonth semeMonth = (SemeMonth) _param._semeMonthMap.get(key);
            svf.VrsOut("MONTH", semeMonth._monthName);
            final String semeField = semeMonth._semesterName.length() > 2 ? "2" : "1";
            svf.VrsOut("MONTH_SEM" + semeField, semeMonth._semesterName);

            int lineCnt = 1;
            for (final Iterator iterator = students.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                svf.VrsOutn("ATTENDNO", lineCnt, student._hrNameabbv + "-" + student._attendNo);
                svf.VrsOutn("NAME", lineCnt, student._name);
                String setData = (String) student._monthData.get(key);
                svf.VrsOutn("SICK1", lineCnt, getAttendZeroHyoji(setData));
                lineCnt++;
            }
            svf.VrEndRecord();
        }
    }

    //プロパティ「use_Attend_zero_hyoji」= '1'のとき、データの通りにゼロ、NULLを表示
    //それ以外のとき、ゼロは表示しない
    private String getAttendZeroHyoji(final String val) {
        if ("1".equals(_param._use_Attend_zero_hyoji)) return val;
        if ("0".equals(val) || "0.0".equals(val)) return "";
        return val;
    }

    private void printSemesData(final Vrw32alp svf, final List students) {
        for (final Iterator itSemMont = _param._semeAll.keySet().iterator(); itSemMont.hasNext();) {
            final String key = (String) itSemMont.next();
            final SemesterData semesterData = (SemesterData) _param._semeAll.get(key);
            final String semeField = semesterData._semesterName.length() > 2 ? "2" : "1";
            svf.VrsOut("RESULT_SEM" + semeField, semesterData._semesterName);
            svf.VrsOut("SICK_DIV1", _param._sickName);

            int lineCnt = 1;
            for (final Iterator iterator = students.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                String setData = (String) student._semesterData.get(key);
                setData = null == setData ? "0" : setData;
                svf.VrsOutn("SICK2", lineCnt, setData);
                lineCnt++;
            }
            svf.VrEndRecord();
        }
    }

    private void printKoteiData(final Vrw32alp svf, final List students) {
        for (final Iterator itSemMont = _param._koteiTitle.keySet().iterator(); itSemMont.hasNext();) {
            final String key = (String) itSemMont.next();
            final String koteiTitle = (String) _param._koteiTitle.get(key);
            final String semeField = koteiTitle.length() > 3 ? "2" : "1";
            svf.VrsOut("TOTAL_TITLE" + semeField, koteiTitle);

            int lineCnt = 1;
            for (final Iterator iterator = students.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                String setData = (String) student._koteiData.get(key);
                setData = null == setData ? "0" : setData;
                svf.VrsOutn("SICK3", lineCnt, setData);
                lineCnt++;
            }
            svf.VrEndRecord();
        }
    }

    private List getPageStudentMap(final DB2UDB db2) throws SQLException {
        final List pageStudent = new ArrayList();

        final String studentSql = selectQuery();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            List pageList = new ArrayList();
            int lineCnt = 1;
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String schregNo = rsStudent.getString("SCHREGNO");
                final String name = rsStudent.getString("NAME_SHOW");
                final String attendNo = rsStudent.getString("ATTENDNO");
                final String hrNameabbv = rsStudent.getString("HR_NAMEABBV");
                final Student student = new Student(schregNo, name, attendNo, hrNameabbv);
                for (final Iterator iter = _param._semeMonthMap.keySet().iterator(); iter.hasNext();) {
                    final String key = (String) iter.next();
                    final String setVal = rsStudent.getString("MONTH" + key);
                    student.setMonthData(key, setVal);
                }
                lineCnt++;
                student.setKoteiData(db2);
                pageList.add(student);
                if (pageList.size() == LINE_MAX) {
                    pageStudent.add(new ArrayList(pageList));
                    pageList = new ArrayList();
                    lineCnt = 1;
                }
            }
            if (pageList.size() > 0) {
                pageStudent.add(new ArrayList(pageList));
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }

        return pageStudent;
    }

    private String selectQuery()
    {
        final StringBuffer stb = new StringBuffer();
        String target_month = null != _param._monthCd && _param._monthCd.length() >= 4 ? _param._monthCd.substring(0, 2) : "0";
        if (Integer.parseInt(target_month) < 4) {
            target_month = String.valueOf(Integer.parseInt(_param._ctrlYear) + 1) + target_month;
        } else {
            target_month = _param._ctrlYear + target_month;
        }
        stb.append(" SELECT ");
        stb.append("     t1.NAME_SHOW, t1.HR_NAMEABBV, t1.ATTENDNO, t1.SCHREGNO ");
        for (final Iterator iter = _param._semeMonthMap.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            stb.append(" ,w" + key + "." + (String) _param._sickField.get(_param._sick) + " AS MONTH" + key + " ");
            stb.append(" ,w" + key + ".SEMESTER AS SEMESTER" + key + " ");
        }
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("        t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno ");
        stb.append("     FROM ");
        stb.append("         schreg_base_mst t1, chair_std_dat t2, schreg_regd_hdat t3, schreg_regd_dat t4 ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append("     WHERE ");
        stb.append("         t1.schregno = t2.schregno ");
        stb.append("         AND t1.schregno = t4.schregno ");
        stb.append("         AND t2.year = '" + _param._ctrlYear + "' ");
        stb.append("         AND t2.year = t3.year ");
        stb.append("         AND t2.year = t4.year ");
        stb.append("         AND t2.chaircd = '" + _param._chairCd + "' ");
        stb.append("         AND t3.grade = t4.grade ");
        stb.append("         AND t3.hr_class = t4.hr_class ");
        stb.append("         AND t3.semester = '" + _param._ctrlSemester + "' ");
        stb.append("         AND t3.semester = t4.semester ");
        if (target_month.length() == 6) {
            stb.append("     AND '" + target_month + "' BETWEEN rtrim(char(year(t2.appdate))) || SUBSTR(CHAR(DECIMAL(month(t2.appdate),2,0)),1,2) ");
            stb.append("     AND rtrim(char(year(t2.appenddate))) || SUBSTR(CHAR(DECIMAL(month(t2.appenddate),2,0)),1,2) ");
        }
        stb.append("     GROUP BY ");
        stb.append("         t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno ");
        stb.append("     ORDER BY ");
        stb.append("         t3.grade, t3.hr_class, t4.attendno ");
        stb.append("     )t1 ");

        for (final Iterator iter = _param._semeMonthMap.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final SemeMonth semeMonth = (SemeMonth) _param._semeMonthMap.get(key);
            stb.append(" LEFT OUTER JOIN ");
            stb.append("     (SELECT ");
            stb.append("         year, month, SEMESTER, schregno, classcd, subclasscd, " + (String) _param._sickField.get(_param._sick) + " ");
            stb.append("     FROM ");
            stb.append("         attend_subclass_dat ");
            stb.append("     WHERE ");
            stb.append("         copycd = '0' ");
            stb.append("         AND year = '" + _param._ctrlYear + "' ");
            stb.append("         AND month = '" + semeMonth._month + "' ");
            stb.append("         AND SEMESTER = '" + semeMonth._semester + "' ");
            if (("1".equals(_param._useCurriculumcd))) {
                stb.append("         AND classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd = '" + _param._subclassCd + "' ");
            } else {
                stb.append("         AND classcd = '" + _param._subclassCd.substring(0, 2) + "' ");
                stb.append("         AND subclasscd = '" + _param._subclassCd + "' ");
            }
            stb.append("     )w" + key + " ");
            stb.append("     ON ");
            stb.append("         t1.schregno = w" + key + ".schregno ");
        }
        stb.append(" ORDER BY ");
        stb.append(" t1.grade, t1.hr_class, t1.attendno");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _attendNo;
        private final String _hrNameabbv;
        private final Map _monthData;
        private final Map _semesterData;
        private final Map _koteiData;

        public Student(final String schregNo, final String name, final String attendNo, final String hrNameabbv) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = attendNo;
            _hrNameabbv = hrNameabbv;
            _monthData = new LinkedMap();
            _semesterData = new LinkedMap();
            _koteiData = new LinkedMap();
        }

        public void setKoteiData(final DB2UDB db2) throws SQLException {
            String ruisekiSql = "";
            if (_param._schoolMst._absentCov.equals("0") ||
                _param._schoolMst._absentCov.equals("2") ||
                _param._schoolMst._absentCov.equals("4")
            ) {
                ruisekiSql = getAttendDataSql();
            } else {
                // (学期ごとに集計）する場合
                ruisekiSql = getAttendDataSql2();
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(ruisekiSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int lesson = rs.getInt("LESSON");
                    setKoteiData(KOTEI_SYUSSEKI_SUBEKI, lesson);
                    final int tNotice = rs.getInt("T_NOTICE");
                    setKoteiData(KOTEI_KETUJI, tNotice);
                    final int tLateearly = rs.getInt("T_LATEEARLY");
                    setKoteiData(KOTEI_TIKOKU, tLateearly);
                    final int noticeLate = rs.getInt("NOTICE_LATE");
                    setKoteiData(KOTEI_KEKKA, noticeLate);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAttendDataSql() {
            final StringBuffer stb = new StringBuffer();
            final StringBuffer lesson = new StringBuffer();
            lesson.append("SUM(value(LESSON,0)) - SUM(value(ABROAD,0))");
            //学校マスタの各フラグを参照し「休学・出停・忌引・出停（伝染病）」を授業時数から除く処理。
            final KNJSchoolMst schoolMst = _param._schoolMst;
            if (!"1".equals(schoolMst._subOffDays)) {
                lesson.append(" - SUM(value(OFFDAYS,0))");
            }
            if (!"1".equals(schoolMst._subSuspend)) {
                lesson.append(" - SUM(value(SUSPEND,0))");
            }
            if (!"1".equals(schoolMst._subMourning)) {
                lesson.append(" - SUM(value(MOURNING,0))");
            }
            if (!"1".equals(schoolMst._subKoudome) && _param._hasATTEND_SUBCLASS_DAT_KOUDOME) {
                lesson.append(" - SUM(value(KOUDOME,0))");
            }
            if (!"1".equals(schoolMst._subVirus) && _param._hasATTEND_SUBCLASS_DAT_VIRUS) {
                lesson.append(" - SUM(value(VIRUS,0))");
            }

            final StringBuffer notice = new StringBuffer();
            notice.append("SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))");
            //学校マスタの各フラグを参照し「休学・公欠・出停・忌引・出停（伝染病）」を欠課に含める処理。
            if ("1".equals(schoolMst._subOffDays)) {
                notice.append(" + SUM(value(OFFDAYS,0))");
            }
            if ("1".equals(schoolMst._subAbsent)) {
                notice.append(" + SUM(value(ABSENT,0))");
            }
            if ("1".equals(schoolMst._subSuspend)) {
                notice.append(" + SUM(value(SUSPEND,0))");
            }
            if ("1".equals(schoolMst._subMourning)) {
                notice.append(" + SUM(value(MOURNING,0))");
            }
            if ("1".equals(schoolMst._subKoudome) && _param._hasATTEND_SUBCLASS_DAT_KOUDOME) {
                notice.append(" + SUM(value(KOUDOME,0))");
            }
            if ("1".equals(schoolMst._subVirus) && _param._hasATTEND_SUBCLASS_DAT_VIRUS) {
                notice.append(" + SUM(value(VIRUS,0))");
            }

            final StringBuffer late_early = new StringBuffer();
            late_early.append("SUM(value(LATE,0)) + SUM(value(EARLY,0))");

            stb.append(" SELECT SCHREGNO ");
            stb.append("       ," + lesson.toString() + "  AS LESSON ");
            stb.append("       ," + notice.toString() + "  AS T_NOTICE ");
            //遅刻
            if ("1".equals(_param._chikokuHyoujiFlg)) {
                stb.append("   ," + late_early.toString() + "     AS T_LATEEARLY ");

            } else if ("4".equals(schoolMst._absentCov) &&
                (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("   ,MOD(" + late_early.toString() + "," + schoolMst._absentCovLate + ") AS T_LATEEARLY ");

            } else if ("2".equals(schoolMst._absentCov) &&
                       (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("   ,MOD(" + late_early.toString() + "," + schoolMst._absentCovLate + ") AS T_LATEEARLY ");

            } else {
                stb.append("   ," + late_early.toString() + "     AS T_LATEEARLY ");

            }
            //小数点あり
            if ("4".equals(schoolMst._absentCov) &&
                (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("   ,DECIMAL((FLOAT(" + late_early.toString() + ") / " + schoolMst._absentCovLate + ") + (" + notice.toString() + "),4,1) AS NOTICE_LATE ");

            } else if ("2".equals(schoolMst._absentCov) &&
                       (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("   ,((" + late_early.toString() + ") / " + schoolMst._absentCovLate + ") + (" + notice.toString() + ") AS NOTICE_LATE ");
            } else {
                stb.append("   ," + notice.toString() + "  AS NOTICE_LATE ");
            }
            stb.append("  FROM attend_subclass_dat  ");
            stb.append(" WHERE COPYCD     = '0' ");
            stb.append("   AND SCHREGNO   = '" + _schregNo + "' ");
            stb.append("   AND YEAR       = '" + _param._ctrlYear + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclassCd + "' ");
            } else {
                stb.append("   AND SUBCLASSCD = '" + _param._subclassCd + "' ");
            }
            stb.append("   AND EXISTS (SELECT 'X' FROM chair_std_dat ");
            stb.append("                WHERE YEAR    = '" + _param._ctrlYear + "' ");
            stb.append("                  AND CHAIRCD = '" + _param._chairCd + "') ");
            stb.append(" GROUP BY SCHREGNO ");

            return stb.toString();
        }

        private String getAttendDataSql2() {
            final StringBuffer stb = new StringBuffer();

            final StringBuffer lesson = new StringBuffer();
            lesson.append("SUM(value(LESSON,0)) - SUM(value(ABROAD,0))");
            //学校マスタの各フラグを参照し「休学・出停・忌引・出停（伝染病）」を授業時数から除く処理。
            final KNJSchoolMst schoolMst = _param._schoolMst;
            if (!"1".equals(schoolMst._subOffDays)) {
                lesson.append(" - SUM(value(OFFDAYS,0))");
            }
            if (!"1".equals(schoolMst._subSuspend)) {
                lesson.append(" - SUM(value(SUSPEND,0))");
            }
            if (!"1".equals(schoolMst._subMourning)) {
                lesson.append(" - SUM(value(MOURNING,0))");
            }
            if (!"1".equals(schoolMst._subKoudome) && _param._hasATTEND_SUBCLASS_DAT_KOUDOME) {
                lesson.append(" - SUM(value(KOUDOME,0))");
            }
            if (!"1".equals(schoolMst._subVirus) && _param._hasATTEND_SUBCLASS_DAT_VIRUS) {
                lesson.append(" - SUM(value(VIRUS,0))");
            }

            final StringBuffer notice = new StringBuffer();
            notice.append("SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))");
            //学校マスタの各フラグを参照し「休学・公欠・出停・忌引・出停（伝染病）」を欠課に含める処理。
            if ("1".equals(schoolMst._subOffDays)) {
                notice.append(" + SUM(value(OFFDAYS,0))");
            }
            if ("1".equals(schoolMst._subAbsent)) {
                notice.append(" + SUM(value(ABSENT,0))");
            }
            if ("1".equals(schoolMst._subSuspend)) {
                notice.append(" + SUM(value(SUSPEND,0))");
            }
            if ("1".equals(schoolMst._subMourning)) {
                notice.append(" + SUM(value(MOURNING,0))");
            }
            if ("1".equals(schoolMst._subKoudome) && _param._hasATTEND_SUBCLASS_DAT_KOUDOME) {
                notice.append(" + SUM(value(KOUDOME,0))");
            }
            if ("1".equals(schoolMst._subVirus) && _param._hasATTEND_SUBCLASS_DAT_VIRUS) {
                notice.append(" + SUM(value(VIRUS,0))");
            }

            final StringBuffer late_early = new StringBuffer();
            late_early.append("SUM(value(late,0)) + SUM(value(early,0))");
            stb.append(" SELECT schregno ");
            stb.append("       ,SUM(LESSON)      AS LESSON ");
            stb.append("       ,SUM(T_NOTICE)    AS T_NOTICE ");
            stb.append("       ,SUM(T_LATEEARLY) AS T_LATEEARLY ");
            stb.append("       ,SUM(NOTICE_LATE) AS NOTICE_LATE ");
            stb.append("  FROM ");
            stb.append("       (SELECT  T1.schregno ");
            stb.append("               ,T1.semester ");
            stb.append("               ," + lesson.toString() + " AS LESSON ");
            stb.append("               ," + notice.toString() + " AS T_NOTICE ");
            //遅刻
            if ("1".equals(_param._chikokuHyoujiFlg)) {
                stb.append("           ," + late_early.toString() + "    AS T_LATEEARLY ");

            } else if ("3".equals(schoolMst._absentCov) &&
                (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("           ,MOD(" + late_early.toString() + "," + schoolMst._absentCovLate + ") AS T_LATEEARLY ");

            } else if ("1".equals(schoolMst._absentCov) &&
                       (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("           ,MOD(" + late_early.toString() + "," + schoolMst._absentCovLate + ") AS T_LATEEARLY ");

            } else {
                stb.append("           ," + late_early.toString() + "    AS T_LATEEARLY ");

            }
            //小数点ありAdd
            if ("3".equals(schoolMst._absentCov) &&
                (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("           ,DECIMAL((FLOAT(" + late_early.toString() + ") / " + schoolMst._absentCovLate + ") + (" + notice.toString() + "),4,1) AS NOTICE_LATE ");

            } else if ("1".equals(schoolMst._absentCov) &&
                       (NumberUtils.isNumber(schoolMst._absentCovLate) && Integer.parseInt(schoolMst._absentCovLate) != 0)
            ) {
                stb.append("           ,((" + late_early.toString() + ") / " + schoolMst._absentCovLate + ") + (" + notice.toString() + ") AS NOTICE_LATE ");
            } else {
                stb.append("           ," + notice.toString() + " AS NOTICE_LATE ");
            }
            stb.append("          FROM attend_subclass_dat T1   ");
            stb.append("         WHERE COPYCD        = '0' ");
            stb.append("           AND T1.YEAR       = '" + _param._ctrlYear + "' ");
            if (("1".equals(_param._useCurriculumcd))) {
                stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            } else {
                stb.append("           AND T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            }
            stb.append("           AND EXISTS (SELECT 'X' FROM CHAIR_STD_DAT T2 ");
            stb.append("                        WHERE T1.YEAR     = T2.YEAR ");
            stb.append("                          AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("                          AND T1.SCHREGNO = T2.SCHREGNO "); 
            stb.append("                          AND T2.CHAIRCD  = '" + _param._chairCd + "') ");
            stb.append("         GROUP BY T1.SCHREGNO, T1.SEMESTER) T1 ");
            stb.append(" WHERE SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY schregno ");

            return stb.toString();
        }

        private void setMonthData(final String key, final String val) {
            _monthData.put(key, val);
            final int addVal = null != val ? Integer.parseInt(val) : 0;
            setSemesterData(key, addVal);
            setKoteiData(KOTEI_GOUKEI, addVal);
        }

        private void setSemesterData(final String key, final int val) {
            int setVal = val;
            final SemeMonth semeMonth = (SemeMonth) _param._semeMonthMap.get(key);
            final String semester = semeMonth._semester;
            if (_semesterData.containsKey(semester)) {
                setVal += Integer.parseInt((String) _semesterData.get(semester));
            }
            _semesterData.put(semester, String.valueOf(setVal));
        }

        private void setKoteiData(final String key, final int val) {
            int setVal = val;
            if (_koteiData.containsKey(key)) {
                setVal += Integer.parseInt((String) _koteiData.get(key));
            }
            _koteiData.put(key, String.valueOf(setVal));
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            final StringBuffer stb = new StringBuffer();
            stb.append(_schregNo).append(":").append(_name).append("\n");
            for (final Iterator iter = _monthData.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final String val = (String) _monthData.get(key);
                final SemeMonth semeMonth = (SemeMonth) _param._semeMonthMap.get(key);
                stb.append(semeMonth._monthName).append(":").append(semeMonth._semesterName).append(" = ").append(val).append("\n");
            }
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68684 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _subclassCd;
        private final String _subclassName;
        private final String _chairCd;
        private final String _chairName;
        private final String _sick;
        private final String _sickName;
        private final Map _sickField;
        private final String _monthCd;
        private final Map _semeAll;
        private final Map _semeMonthMap;
        private final Map _koteiTitle;
        private final KNJSchoolMst _schoolMst;
        private final String _chikokuHyoujiFlg;
        private final String _useCurriculumcd;
        private final String _use_Attend_zero_hyoji;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;
        private final boolean _hasATTEND_SUBCLASS_DAT_KOUDOME;
        private final boolean _hasATTEND_SUBCLASS_DAT_VIRUS;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _monthCd = request.getParameter("MONTHCD");

            _subclassCd = request.getParameter("SUBCLASSCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassName = getSubclassName(db2, _useCurriculumcd, _subclassCd);

            _chairCd = request.getParameter("CHAIRCD");
            String target_seme = null != _monthCd && _monthCd.length() >= 4 ? _monthCd.substring(3) : _ctrlSemester;
            _chairName = getChairName(db2, _ctrlYear, target_seme, _chairCd);

            _sick = request.getParameter("SICK");
            _sickName = getSickName(db2, _sick);
            _sickField = new LinkedMap();
            _sickField.put("1", "ABSENT");
            _sickField.put("2", "SUSPEND");
            _sickField.put("3", "MOURNING");
            _sickField.put("4", "SICK");
            _sickField.put("5", "NOTICE");
            _sickField.put("6", "NONOTICE");
            _sickField.put("15", "LATE");
            _sickField.put("16", "EARLY");
            _sickField.put("19", "VIRUS");
            _sickField.put("25", "VIRUS");

            _semeAll = getSeme(db2, _ctrlYear);
            _semeMonthMap = getSemeMonth(db2, _ctrlYear, _semeAll);
            _koteiTitle = new LinkedMap();
            _koteiTitle.put(KOTEI_GOUKEI, "合計");
            _koteiTitle.put(KOTEI_SYUSSEKI_SUBEKI, "出席すべき授業時数");
            _koteiTitle.put(KOTEI_KETUJI, "欠時数");
            _koteiTitle.put(KOTEI_TIKOKU, "遅刻数");
            _koteiTitle.put(KOTEI_KEKKA, "欠課数");
            for (final Iterator iter = _semeMonthMap.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final SemeMonth semeMonth = (SemeMonth) _semeMonthMap.get(key);
                log.debug(key + " = " + semeMonth._monthName + ":" + semeMonth._semesterName);
            }

            for (final Iterator iter = _semeAll.keySet().iterator(); iter.hasNext();) {
                final String semester = (String) iter.next();
                final SemesterData semesterData = (SemesterData) _semeAll.get(semester);
                log.debug(semesterData._semesterName + ":" + _sickName);
            }

            for (final Iterator iter = _koteiTitle.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final String title = (String) _koteiTitle.get(key);
                log.debug(title);
            }

            _schoolMst = new KNJSchoolMst(db2, _ctrlYear);

            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _use_Attend_zero_hyoji = request.getParameter("use_Attend_zero_hyoji");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            _hasATTEND_SUBCLASS_DAT_KOUDOME = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_SUBCLASS_DAT", "KOUDOME");
            _hasATTEND_SUBCLASS_DAT_VIRUS = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_SUBCLASS_DAT", "VIRUS");
        }

        private String getSubclassName(final DB2UDB db2, final String useCurriculumcd, final String subclassCd) throws SQLException {
            String ret = "";
            final String subclassNameSql = getSubclassNameSql(useCurriculumcd, subclassCd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("SUBCLASSNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return ret;
        }

        private String getSubclassNameSql(final String useCurriculumcd, final String subclassCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST ");
            stb.append(" WHERE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclassCd + "' ");
            } else {
                stb.append("     SUBCLASSCD = '" + subclassCd + "' ");
            }
            return stb.toString();
        }

        private String getChairName(final DB2UDB db2, final String ctrlYear, final String semester, final String chairCd) throws SQLException {
            String ret = "";
            final String chairNameSql = getChairNameSql(ctrlYear, semester, chairCd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(chairNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("CHAIRNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return ret;
        }

        private String getChairNameSql(final String ctrlYear, final String semester, final String chairCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CHAIRNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + semester + "' ");
            stb.append("     AND CHAIRCD = '" + chairCd + "' ");

            return stb.toString();
        }

        private String getSickName(final DB2UDB db2, final String sick) throws SQLException {
            String ret = "";
            final String sickNameSql = getSickNameSql(sick);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sickNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return ret;
        }

        private String getSickNameSql(final String sick) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'C001' AND ");
            stb.append("     NAMECD2 = '" + sick + "' ");

            return stb.toString();
        }

        private Map getSeme(final DB2UDB db2, final String ctrlYear) throws SQLException {
            final Map retMap = new LinkedMap();
            final String semesMonthSql = getSemesMonthSql(ctrlYear);

            PreparedStatement psSemMon = null;
            ResultSet rsSemMon = null;
            try {
                psSemMon = db2.prepareStatement(semesMonthSql);
                rsSemMon = psSemMon.executeQuery();
                while (rsSemMon.next()) {
                    final String semester = rsSemMon.getString("SEMESTER");
                    final String semesterName = rsSemMon.getString("SEMESTERNAME");
                    final int sMonth = rsSemMon.getInt("S_MONTH");
                    final int eMonth = rsSemMon.getInt("E_MONTH");
                    final SemesterData semesterData = new SemesterData(semester, semesterName, sMonth, eMonth);
                    retMap.put(semester, semesterData);
                }
            } finally {
                DbUtils.closeQuietly(null, psSemMon, rsSemMon);
                db2.commit();
            }
            return retMap;
        }

        private String getSemesMonthSql(final String ctrlYear) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SEMESTERNAME, ");
            stb.append("     CASE WHEN MONTH(SDATE) < 4 ");
            stb.append("          THEN MONTH(SDATE) + 12 ");
            stb.append("          ELSE MONTH(SDATE) END AS S_MONTH, ");
            stb.append("     CASE WHEN MONTH(EDATE) < 4 ");
            stb.append("          THEN MONTH(EDATE) + 12 ");
            stb.append("          ELSE MONTH(EDATE) END AS E_MONTH ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + ctrlYear + "' ");
            stb.append("     AND SEMESTER <> '9' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private Map getSemeMonth(final DB2UDB db2, final String ctrlYear, final Map semeAll) throws SQLException {
            final Map retMap = new LinkedMap();
            final String monthSql = getSelectMonthSql(ctrlYear);

            PreparedStatement psMonth = null;
            ResultSet rsMonth = null;
            try {
                psMonth = db2.prepareStatement(monthSql);
                int keyNo = 1;
                for (final Iterator iter = semeAll.keySet().iterator(); iter.hasNext();) {
                    final String semester = (String) iter.next();
                    final SemesterData semesterData = (SemesterData) semeAll.get(semester);
                    final String semesterName = semesterData._semesterName;
                    final int sMonth = semesterData._sMonth;
                    final int eMonth = semesterData._eMonth;
                    for (int i = sMonth; i <= eMonth; i++) {
                        int month = i > 12 ? i - 12 : i;
                        psMonth.setString(1, month < 10 ? "0" + month : String.valueOf(month));
                        rsMonth = psMonth.executeQuery();
                        SemeMonth semeMonth = null;
                        while (rsMonth.next()) {
                            final String monthCd = rsMonth.getString("NAMECD2");
                            final String monthName = rsMonth.getInt("NAMECD2") + "月";
                            semeMonth = new SemeMonth(semester, semesterName, monthCd, monthName);
                            retMap.put(String.valueOf(keyNo), semeMonth);
                            keyNo++;
                        }
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, psMonth, rsMonth);
                db2.commit();
            }
            return retMap;
        }

        private String getSelectMonthSql(final String ctrlYear) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1, ");
            stb.append("     NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR  = '" + ctrlYear + "' ");
            stb.append("     AND NAMECD1 = 'Z005' ");
            stb.append("     AND NAMECD2 = ? ");

            return stb.toString();
        }

    }

    private class SemesterData {
        private final String _semester;
        private final String _semesterName;
        private final int _sMonth;
        private final int _eMonth;

        public SemesterData(final String semester, final String semesterName, final int sMonth, final int eMonth) {
            _semester = semester;
            _semesterName = semesterName;
            _sMonth = sMonth;
            _eMonth = eMonth;
        }
        
    }

    private class SemeMonth {
        private final String _semester;
        private final String _semesterName;
        private final String _month;
        private final String _monthName;

        public SemeMonth(final String semester, final String semesterName, final String month, final String monthName) {
            _semester = semester;
            _semesterName = semesterName;
            _month = month;
            _monthName = monthName;
        }
        
    }
}

// eof

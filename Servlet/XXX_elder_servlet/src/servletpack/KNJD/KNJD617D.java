// kanji=漢字
/*
 * 作成日: 2020/07/27
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * 推薦名簿
 *
 * @author yogi
 *
 */
public class KNJD617D {
    private static final String SEMEALL = "9";
    private static final String FORM_NAME = "KNJD617D.frm";
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD617D.class);

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
            close(db2, svf);
        }

    }

    /**
     * 印刷処理メイン
     * @param db2	ＤＢ接続オブジェクト
     * @param svf	帳票オブジェクト
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        // データ取得
        getStudentInfo(db2);
        getAttendInfo(db2);
        for (int i=0 ; i < _param._hrInfo.size() ; i++) {
            svf.VrSetForm("KNJD617D.frm", 4);
            final HrInfo hrWk = (HrInfo)_param._hrInfo.get(i);
            // タイトル部
            setTitle(db2, svf, hrWk);
            // データ部
            printData(db2, svf, hrWk);

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final HrInfo hrClsWk) {
        final String fixedTitle = "学業成績一覧表";
        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._year + "/04/01");
        svf.VrsOut("year2", nendo);
        svf.VrsOut("TITLE", nendo + hrClsWk._hr_Nameabbv + " " + _param._semesterName + " " + _param._testName + " " + fixedTitle);
        svf.VrsOut("teacher", hrClsWk._staffname);
        svf.VrsOut("MUST", hrClsWk._maxMLesson > 0 ? String.valueOf(hrClsWk._maxMLesson) : "");
    }

    private void printData(final DB2UDB db2, final Vrw32alp svf, final HrInfo hrClsWk) {
        Map taidoMap = new LinkedMap();
        taidoMap.put("11", "A");
        taidoMap.put("22", "B");
        taidoMap.put("33", "C");
        taidoMap.put("44", "D");
        int rCnt = 0;
        for (Iterator ite = hrClsWk._studentMap.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            rCnt++;
            final StudentInfo schInfo = (StudentInfo)hrClsWk._studentMap.get(schregno);
            if (Integer.parseInt(schInfo._attendno) > 50) {
                continue;   //出力予定の出席番号が50以上なら、以降の番号は出力しない。
            }
            while (Integer.parseInt(schInfo._attendno) != rCnt) {
                svf.VrsOutn("NUMBER", rCnt, String.valueOf(rCnt));
                rCnt = Integer.parseInt(schInfo._attendno);
            }
            //出席番号
            svf.VrsOutn("NUMBER", rCnt, String.valueOf(Integer.parseInt(schInfo._attendno)));
            //氏名
            svf.VrsOutn("name1", rCnt, schInfo._name);

            //"92"は先頭1件だけが対象のはずなので、ループ不要。
            Iterator itr = schInfo._seikatuTaidoMap.keySet().iterator();
            if (itr.hasNext()) {
                final String kStr = (String)itr.next();
                ScoreInfo seikatuTaidoInf = (ScoreInfo)schInfo._seikatuTaidoMap.get(kStr);
                //生徒態度
                if (seikatuTaidoInf._score != null && taidoMap.containsKey(seikatuTaidoInf._score)) {
                    svf.VrsOutn("ATTITUDE", rCnt, (String)taidoMap.get(seikatuTaidoInf._score));
                }
            }

            if (schInfo._totalInf != null) {
                //総得点
                svf.VrsOutn("TOTAL", rCnt, StringUtils.defaultString(schInfo._totalInf._score, ""));
                //平均点
                svf.VrsOutn("AVERAGE", rCnt, StringUtils.defaultString(schInfo._totalInf._avg, ""));
                 //順位(学級/学年)
                svf.VrsOutn("CLASS_RANK", rCnt, StringUtils.defaultString(schInfo._totalInf._class_Rank, ""));
                svf.VrsOutn("RANK", rCnt, StringUtils.defaultString(schInfo._totalInf._grade_Rank, ""));
            }

            if (schInfo._Attendance != null) {
                //欠席
                //早退
                svf.VrsOutn("LEAVE", rCnt, schInfo._Attendance._early);
                //遅刻
                svf.VrsOutn("TOTAL_LATE", rCnt, schInfo._Attendance._late);
                //欠課
                svf.VrsOutn("KEKKA", rCnt, schInfo._Attendance._absent);
                //忌引・出停
                if (schInfo._Attendance._suspend != null || schInfo._Attendance._mourning != null) {
                    svf.VrsOutn("SUSPEND", rCnt, String.valueOf(Integer.parseInt(StringUtils.defaultString(schInfo._Attendance._suspend, "0")) + Integer.parseInt(StringUtils.defaultString(schInfo._Attendance._mourning, "0"))));
                }
                //備考
                if (schInfo._Attendance._sick != null || schInfo._Attendance._early != null || schInfo._Attendance._late != null) {
                    svf.VrsOutn("REMARK", rCnt, (
                                                 "0".equals(StringUtils.defaultString(schInfo._Attendance._sick, "0"))
                                                 && "".equals(StringUtils.defaultString(schInfo._Attendance._early, "0"))
                                                 && "".equals(StringUtils.defaultString(schInfo._Attendance._late, "0"))
                                                ) ? "皆勤" : "");
                }
            }
        }
        for (Iterator ity = hrClsWk._subclsMap.keySet().iterator();ity.hasNext();) {
            final String subclsCd = (String)ity.next();
            final SubclsInfo subCldObj = (SubclsInfo)hrClsWk._subclsMap.get(subclsCd);
            //データ出力(科目毎)
            ////科目名
            svf.VrsOut("course1", subCldObj._subclassabbv);
            ////単位数
            svf.VrsOut("credit1", subCldObj._credits);
            ////時間
            //svf.VrsOut("lesson1", );  //仕様不明なので空き。->宮城さんに空きで良いことを確認済み。
            for (Iterator ito = hrClsWk._studentMap.keySet().iterator();ito.hasNext();) { //各生徒毎
                final String schregnos = (String)ito.next();
                final StudentInfo schInfos = (StudentInfo)hrClsWk._studentMap.get(schregnos);
                final ScoreInfo sInf = (ScoreInfo)schInfos._scoreMap.get(subclsCd);
                final String fKey = "9" + ":" + schregnos + ":" + subclsCd;
                SubclassAttendance saWk = null;
                if (hrClsWk._subclsAttendMap.containsKey(fKey)) {
                    saWk = (SubclassAttendance)hrClsWk._subclsAttendMap.get(fKey);
                }
                if (sInf != null) {
                    ////評価
                    svf.VrsOutn("SCORE1", Integer.parseInt(schInfos._attendno), sInf._score);
                    if (saWk != null) {
                        ////欠時
                        svf.VrsOutn("kekka2_1", Integer.parseInt(schInfos._attendno), String.valueOf(saWk._absent.intValue()));
                        ////出停
                        svf.VrsOutn("kekka1", Integer.parseInt(schInfos._attendno), String.valueOf(saWk._suspend.intValue()));
                    }
                }
            }
            svf.VrEndRecord();
        }

    }

    private void getStudentInfo(final DB2UDB db2) {
        for (int cnt = 0;cnt < _param._hrInfo.size();cnt++) {
            final HrInfo hrInfo = (HrInfo)_param._hrInfo.get(cnt);
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH BDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T3.NAME, ");

            stb.append("   T4.CLASSCD, ");
            stb.append("   T4.SCHOOL_KIND, ");
            stb.append("   T4.CURRICULUM_CD, ");
            stb.append("   T4.SUBCLASSCD, ");

            stb.append("   T4.SCORE, ");
            stb.append("   ROUND(T4.AVG, 2) AS AVG, ");
            stb.append("   T4.GRADE_RANK, ");
            stb.append("   T4.CLASS_RANK ");

            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
            stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T4 ");
            stb.append("     ON T4.YEAR = T1.YEAR ");
            stb.append("    AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _param._testKindCd + "' ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            if(SEMEALL.equals(_param._semester)) {
                stb.append("   AND T1.SEMESTER = '" + _param._loginSemester + "' ");
            } else {
                stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + hrInfo._cd + "' ");
            stb.append("   AND T4.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
            stb.append("   AND (T4.CLASSCD < '90' OR T4.CLASSCD = '92' OR T4.CLASSCD = '99') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   BDAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
            stb.append("     SELECT ");
            stb.append("       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
            stb.append("     FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T2W ");
            stb.append("     WHERE ");
            stb.append("       T2W.REPLACECD = '1' AND T2W.YEAR = '" + _param._year + "' ");
            stb.append("       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
            stb.append("           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ");
            stb.append("       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (");
            stb.append("         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ");
            stb.append("       ) ");
            stb.append("   ) ");
            stb.append(" ORDER BY ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String hr_Class = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String grade_Rank = rs.getString("GRADE_RANK");
                    final String class_Rank = rs.getString("CLASS_RANK");
                    StudentInfo addwk = null;
                    if (!hrInfo._studentMap.containsKey(schregno)) {
                        addwk = new StudentInfo(hr_Class, attendno, schregno, name);
                        hrInfo._studentMap.put(schregno, addwk);
                    } else {
                        addwk = (StudentInfo)hrInfo._studentMap.get(schregno);
                    }
                    ScoreInfo sInf = new ScoreInfo(classcd, school_Kind, curriculum_Cd, subclasscd, score, avg, grade_Rank, class_Rank);
                    final String sndKey = classcd + "-" +  school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                    if ("92".equals(classcd)) {
                        addwk._seikatuTaidoMap.put(sndKey, sInf);
                    } else if ("99".equals(classcd) && "999999".equals(subclasscd)) {
                        addwk._totalInf = sInf;
                    } else {
                        addwk._scoreMap.put(sndKey, sInf);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private void getAttendInfo(final DB2UDB db2) {
        String edate = StringUtils.replace(_param._date, "/", "-");
        String sdate = StringUtils.replace(_param._sdate, "/", "-");
        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;
        try {
            _param._attendSemeParamMap.put("hrClass", "?");

            for (int cnt = 0;cnt < _param._hrInfo.size();cnt++) {
                final HrInfo hrInfo = (HrInfo)_param._hrInfo.get(cnt);
                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._year,
                        _param._semester,
                        sdate,
                        edate,
                        _param._attendSemeParamMap
                );
                psAtSeme = db2.prepareStatement(sql);

                DbUtils.closeQuietly(rsAtSeme);

                psAtSeme.setString(1, hrInfo._cd.substring(2));
                rsAtSeme = psAtSeme.executeQuery();

                int maxMLesson = 0;
                while (rsAtSeme.next()) {
                    if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                        continue;
                    }

                    final String schregno = rsAtSeme.getString("SCHREGNO");
                    if (hrInfo._studentMap.containsKey(schregno)) {
                        final StudentInfo schInfos = (StudentInfo)hrInfo._studentMap.get(schregno);
                        final Attendance attendance = new Attendance(
                                rsAtSeme.getString("LESSON"),
                                rsAtSeme.getString("MLESSON"),
                                rsAtSeme.getString("SUSPEND"),
                                rsAtSeme.getString("MOURNING"),
                                rsAtSeme.getString("SICK"),
                                rsAtSeme.getString("ABSENT"),
                                rsAtSeme.getString("PRESENT"),
                                rsAtSeme.getString("LATE"),
                                rsAtSeme.getString("EARLY")
                        );
                        schInfos._Attendance = attendance;
                        if (maxMLesson < rsAtSeme.getInt("MLESSON")) {
                            maxMLesson = rsAtSeme.getInt("MLESSON");
                        }
                    }
                }
                hrInfo._maxMLesson = maxMLesson;
                DbUtils.closeQuietly(rsAtSeme);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(psAtSeme);
            db2.commit();
        }
    }

    private static class Attendance {
        final String _lesson;
        final String _mLesson;
        final String _suspend;
        final String _mourning;
        final String _sick;
        final String _absent;
        final String _present;
        final String _late;
        final String _early;
        Attendance(
                final String lesson,
                final String mLesson,
                final String suspend,
                final String mourning,
                final String sick,
                final String absent,
                final String present,
                final String late,
                final String early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }
    }

    private class StudentInfo {
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _scoreMap;
        final Map _seikatuTaidoMap;
        ScoreInfo _totalInf;
        Attendance _Attendance;
        public StudentInfo (final String hr_Class, final String attendno, final String schregno, final String name)
        {
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _scoreMap = new LinkedMap();
            _seikatuTaidoMap = new LinkedMap();
            _totalInf = null;
            _Attendance = null;
        }
    }
    private class ScoreInfo{
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _score;
        final String _avg;
        final String _grade_Rank;
        final String _class_Rank;
        ScoreInfo(final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String score, final String avg, final String grade_Rank, final String class_Rank) {
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _grade_Rank = grade_Rank;
            _class_Rank = class_Rank;
        }
    }

    private class HrInfo {
        final String _hr_Nameabbv;
        final String _grade_Name1;
        final String _grade_Name2;
        final String _grade_Name_INH;
        final String _cd;
        final String _hrName;
        final String _staffname;
        final Map _studentMap;
        final Map _subclsMap;
        final Map _subclsAttendMap;
//        final Map _totalAttendMap;
        int _maxMLesson;
        public HrInfo (final String cd, final String hr_Nameabbv, final String grade_Name1, final String grade_Name2, final String grade_Name_INH, final String hrName, final String staffname)
        {
            _hr_Nameabbv = hr_Nameabbv;
            _grade_Name1 = grade_Name1;
            _grade_Name2 = grade_Name2;
            _grade_Name_INH = grade_Name_INH;
            _cd = cd;
            _hrName = hrName;
            _staffname = staffname;
            _studentMap = new LinkedMap();
            _subclsMap = new LinkedMap();
            _subclsAttendMap = new LinkedMap();
//            _totalAttendMap = new LinkedMap();
        }
    }

    private class SubclsInfo {
        final String _grade;
        final String _hr_Class;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _credits;
        public SubclsInfo (final String grade, final String hr_Class, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String subclassname, final String subclassabbv, final String credits)
        {
            _grade = grade;
            _hr_Class = hr_Class;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _credits = credits;
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _mlesson;
        final BigDecimal _sick1;
        final BigDecimal _sick2;
        final BigDecimal _absent;
        final BigDecimal _late;
        final BigDecimal _early;
        final BigDecimal _suspend;
        final BigDecimal _mourning;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal mlesson, final BigDecimal sick1, final BigDecimal sick2, final BigDecimal absent, final BigDecimal late, final BigDecimal early, final BigDecimal suspend, final BigDecimal mourning) {
            _lesson = lesson;
            _mlesson = mlesson;
            _sick1 = sick1;
            _sick2 = sick2;
            _absent = absent;
            _late = late;
            _early = early;
            _suspend = suspend;
            _mourning = mourning;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75729 $ $Date: 2020-07-30 14:08:25 +0900 (木, 30 7 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _loginSemester;
        final String _grade;
        final String _testKindCd;
        final String _sdate;
        final String _date;
        final String[] _classSelected;
        final String _loginDate;
        final String _useCurriculumCd;
        final String _useVirus;
        final String _useKekkaJisu;
        final String _useKekka;
        final String _useLateDetail;
        final String _useKoudome;
        final String _testCdReadType;

        final String _testName;
        final String _semesterName;
        final List _hrInfo;

        final Map _attendParamMap;
        final Map _attendSemeParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testKindCd = request.getParameter("TESTKINDCD");
            _sdate = request.getParameter("SDATE");
            _date = request.getParameter("DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _useCurriculumCd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKekkaJisu = request.getParameter("useKekkaJisu");
            _useKekka = request.getParameter("useKekka");
            _useLateDetail = request.getParameter("useLatedetail");
            _useKoudome = request.getParameter("useKoudome");
            _testCdReadType = request.getParameter("testcd_readtype");

            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _hrInfo = getHrNames(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _attendSemeParamMap = new HashMap();
            _attendSemeParamMap.put("DB2UDB", db2);
            _attendSemeParamMap.put("HttpServletRequest", request);
            _attendSemeParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendSemeParamMap.put("useCurriculumcd", "1");
            _attendSemeParamMap.put("grade", _grade);

            getSubclsInfo(db2);
            getSubclassAttendance(db2);
        }

        private void getSubclsInfo(final DB2UDB db2) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH BDAT AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T3.CLASSCD, ");
            stb.append("   T3.SCHOOL_KIND, ");
            stb.append("   T3.CURRICULUM_CD, ");
            stb.append("   T3.SUBCLASSCD, ");
            stb.append("   T4.SUBCLASSNAME, ");
            stb.append("   T4.SUBCLASSABBV, ");
            stb.append("   T5.CREDITS ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN CHAIR_DAT T3 ");
            stb.append("     ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append("   LEFT JOIN SUBCLASS_MST T4 ");
            stb.append("     ON T4.CLASSCD = T3.CLASSCD ");
            stb.append("    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("    AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("   LEFT JOIN CREDIT_MST T5 ");
            stb.append("     ON T5.YEAR = T1.YEAR ");
            stb.append("    AND T5.COURSECD = T1.COURSECD ");
            stb.append("    AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("    AND T5.GRADE = T1.GRADE ");
            stb.append("    AND T5.COURSECODE = T1.COURSECODE ");
            stb.append("    AND T5.CLASSCD = T3.CLASSCD ");
            stb.append("    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("    AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            if(SEMEALL.equals(_semester)) {
                stb.append("   AND T1.SEMESTER = '" + _loginSemester + "' ");
            } else {
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
            }
            stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
            stb.append("   AND T3.CLASSCD < '90' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T3.* ");
            stb.append(" FROM ");
            stb.append("   BDAT T3 ");
            stb.append(" WHERE ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD NOT IN ( ");
            stb.append("     SELECT ");
            stb.append("       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
            stb.append("     FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T2W ");
            stb.append("     WHERE ");
            stb.append("       T2W.REPLACECD = '1' AND T2W.YEAR = '" + _year + "' ");
            stb.append("       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
            stb.append("           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ");
            stb.append("       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (");
            stb.append("         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ");
            stb.append("       ) ");
            stb.append("   ) ");
            stb.append(" ORDER BY ");
            stb.append("   T3.CLASSCD, ");
            stb.append("   T3.SCHOOL_KIND, ");
            stb.append("   T3.CURRICULUM_CD, ");
            stb.append("   T3.SUBCLASSCD ");

            for (int cnt = 0;cnt < _hrInfo.size();cnt++) {
                HrInfo hrWk = (HrInfo)_hrInfo.get(cnt);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(stb.toString());
                    ps.setString(1, hrWk._cd);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String grade = rs.getString("GRADE");
                        final String hr_Class = rs.getString("HR_CLASS");
                        final String classcd = rs.getString("CLASSCD");
                        final String school_Kind = rs.getString("SCHOOL_KIND");
                        final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String credits = rs.getString("CREDITS");
                        SubclsInfo addWk = new SubclsInfo(grade, hr_Class, classcd, school_Kind, curriculum_Cd, subclasscd, subclassname, subclassabbv, credits);
                        final String fStr = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                        hrWk._subclsMap.put(fStr, addWk);
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }

        private List getHrNames(final DB2UDB db2) {
            List retLst = new ArrayList();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.HR_NAMEABBV, ");
            stb.append("   T1.GRADE_NAME AS INHDAT_GRADE_NAME, ");
            stb.append("   T3.GRADE_NAME1, ");
            stb.append("   T3.GRADE_NAME2, ");
            stb.append("   T1.GRADE || T1.HR_CLASS AS CD, ");
            stb.append("   T1.HR_NAME AS HR_NAME, ");
            stb.append("   T2.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_HDAT T1 ");
            stb.append("   LEFT JOIN STAFF_MST T2 ");
            stb.append("     ON T2.STAFFCD = T1.TR_CD1 ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.GRADE = T1.GRADE ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            if(SEMEALL.equals(_semester)) {
                stb.append("   AND T1.SEMESTER = '" + _loginSemester + "' ");
            } else {
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
            }
            stb.append("     AND T1.GRADE || T1.HR_CLASS = ? ");
            for (int cnt = 0;cnt < _classSelected.length;cnt++) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(stb.toString());
                    ps.setString(1, _classSelected[cnt]);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
                        final String grade_Name1 = rs.getString("GRADE_NAME1");
                        final String grade_Name2 = rs.getString("GRADE_NAME2");
                        final String grade_Name_INH = rs.getString("INHDAT_GRADE_NAME");
                           final String cd = rs.getString("CD");
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        HrInfo addWk = new HrInfo(cd, hr_Nameabbv, grade_Name1, grade_Name2, grade_Name_INH, hrName, staffname);
                        retLst.add(addWk);
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return retLst;
        }

        private String getSemesterName(final DB2UDB db2) {
            String retStr = "";
            final String sql;
            sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR='" + _year + "' AND SEMESTER = '" + _semester + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getTestName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     ADMIN_CONTROL_SDIV_DAT T1 ");
            stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ");
            stb.append("                                                AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("                                                AND T1.TESTKINDCD = L1.TESTKINDCD ");
            stb.append("                                                AND T1.TESTITEMCD = L1.TESTITEMCD ");
            stb.append("                                                AND T1.SCORE_DIV = L1.SCORE_DIV ");
            stb.append("     INNER JOIN SEMESTER_DETAIL_MST L3 ON L3.YEAR = L1.YEAR ");
            stb.append("                                      AND L3.SEMESTER = L1.SEMESTER ");
            stb.append("                                      AND L3.SEMESTER_DETAIL = L1.SEMESTER_DETAIL ");
            if ("1".equals(_testCdReadType)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ");
                stb.append("                                   AND L2.GRADE = '" + _grade + "' ");
                stb.append("                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("         T1.YEAR         = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER     = '" + _semester + "' ");
            stb.append("     AND T1.CLASSCD      = '00' ");
            if (!"1".equals(_testCdReadType)) {
                stb.append("     AND T1.SCHOOL_KIND = '00' ");
            }
                stb.append("     AND T1.CURRICULUM_CD  = '00' ");
                stb.append("     AND T1.SUBCLASSCD  = '000000' ");
            if (!"".equals(_testKindCd)) {
                stb.append("  AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testKindCd + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        public Map getSubclassAttendance(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String SSEMESTER = "1";
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", "?");
            _attendParamMap.put("sSemester", SSEMESTER);
            String edate = StringUtils.replace(_date, "/", "-");
            String sdate = StringUtils.replace(_sdate, "/", "-");
            if (sdate.compareTo(edate) < 0) {
                final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
                        _year,
                        "9",
                        (String)sdate,
                        (String)edate,
                        _attendParamMap
                        );
                try {
                    ps = db2.prepareStatement(sqlAttendSubclass);
                    for (int clsCnt = 0;clsCnt < _hrInfo.size();clsCnt++) {
                        HrInfo hInfo = (HrInfo)_hrInfo.get(clsCnt);
                        ps.setString(1, hInfo._cd.substring(2));
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String semesStr = rs.getString("SEMESTER");
                            final String schregno = rs.getString("SCHREGNO");
                            final String subclasscd = rs.getString("SUBCLASSCD");

                            final BigDecimal lesson = rs.getBigDecimal("LESSON");
                            final BigDecimal mlesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal sick1 = rs.getBigDecimal("SICK1");
                            final BigDecimal sick2 = rs.getBigDecimal("SICK2");
                            final BigDecimal absent = rs.getBigDecimal("ABSENT");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");
                            final BigDecimal suspend = rs.getBigDecimal("SUSPEND");
                            final BigDecimal mourning = rs.getBigDecimal("MOURNING");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mlesson, sick1, sick2, absent, late, early, suspend, mourning);
                            final String fKey = semesStr+ ":" + schregno + ":" + subclasscd;
                            hInfo._subclsAttendMap.put(fKey, subclassAttendance);
                        }
                        if (clsCnt+1 < _hrInfo.size()) {  //最終じゃなければ、psを再利用するので、rsのみ閉じる。最後はfinallyで。
                            DbUtils.closeQuietly(rs);
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return retMap;
        }
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


//    /**
//     * 帳票に設定する文字数が制限文字超の場合
//     * 帳票設定エリアの変更を行う
//     * @param area_name	帳票出力エリア
//     * @param sval			値
//     * @param maxVal		最大値
//     * @return
//     */
//    private String setformatArea(String area_name, String sval, int maxVal) {
//
//    	String retAreaName = "";
//		// 値がnullの場合はnullが返される
//    	if (sval == null) {
//			return null;
//		}
//    	// 設定値が10文字超の場合、帳票設定エリアの変更を行う
//    	if(maxVal >= sval.length()){
//   			retAreaName = area_name + "_1";
//    	} else {
//   			retAreaName = area_name + "_2";
//    	}
//        return retAreaName;
//    }
}

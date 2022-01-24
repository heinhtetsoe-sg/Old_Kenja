/*
 * 作成日: 2021/01/06
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 洛南高校　高校通知票
 */
public class KNJD187P {

    private static final Log log = LogFactory.getLog(KNJD187P.class);

    private boolean _hasData;

    private static String SEMEALL = "9";
    private static String G1_HYOKA_TESTCD = "1990008";
    private static String G2_HYOKA_TESTCD = "2990008";
    private static String G3_HYOKA_TESTCD = "3990008";
    private static String GAKUNENHYOKA_TESTCD = "9990008";
    private static String HYOTEI_TESTCD = "9990009";

    private static String CLSCD9 = "99";
    private static String CURCCD9 = "99";
    private static String SUBCLSCD9 = "999999";
    private static String SUBCLSCD9_HEAD = CLSCD9 + "-";
    private static String SUBCLSCD9_TAIL = "-" + CURCCD9 + "-" + SUBCLSCD9;
    private static String DLM = "-";
    private static String TYPE_COMM = "1";
    private static String TYPE_ATT = "2";

    private static int KAIKIN_LMT = 0;
    private static int SEIKIN_LMT = 3;
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

        for (final Iterator sit = Student.getStudentList(db2, _param).iterator(); sit.hasNext();) {
            final Student student = (Student) sit.next();

            log.info(" schregno = " + student._schregno);

            svf.VrSetForm("KNJD187P.frm", 1);
            printMain(svf, db2, student);
            svf.VrEndPage();

            _hasData = true;
        }
    }

    // 生徒内容印刷
    private void printMain(final Vrw32alp svf, final DB2UDB db2, final Student student) {

        //ヘッダ部分
        svf.VrsOut("TITLE", _param._nendo + "　" + "学業成績通知表");  //ltrim(_param._certifSchoolSchoolName) +
        svf.VrsOut("ATTEND_NO", student.getAttendno()); // 出席番号
        svf.VrsOut("GRADE", StringUtils.defaultString(_param._gradeCd, " "));
        svf.VrsOut("HR", StringUtils.defaultString(student._hrClassName1));
        svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 氏名

        //学期タイトル
        int semesMaxIdx = 4;
        for (int semes = 1; semes <= semesMaxIdx; semes++) {
            final String semestername = _param.getSemesterName(semes == semesMaxIdx ? SEMEALL : String.valueOf(semes));
            svf.VrsOutn("SEMESTER1", semes, semestername);  //成績
            svf.VrsOutn("SEMESTER2", semes, semestername);  //出欠
        }

        //フッタ部分
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._date));
        svf.VrsOut("JOB_NAME1", _param._certifSchoolJobName);
        svf.VrsOut("TEACHER_NAME1", _param._certifSchoolPrincipalName);
        svf.VrsOut("JOB_NAME2", _param._certifSchoolHrJobName);
        svf.VrsOut("TEACHER_NAME2", student._staffname);

        //特記事項
        String communication = StringUtils.defaultString(toString(student._communicationMap.get(_param._paramSemester + DLM + TYPE_COMM)));
        final List comCutLst = KNJ_EditKinsoku.getTokenList(communication, Integer.parseInt(_param._hRepRemDatCS[0]) * 2, Integer.parseInt(_param._hRepRemDatCS[1]));
        int nCnt = 1;
        for (Iterator ite = comCutLst.iterator();ite.hasNext();) {
            final String pWk = (String)ite.next();
            svf.VrsOutn("COMM1", nCnt, pWk);
            nCnt++;
        }

        //出欠の記録
        final int lastLine = semesMaxIdx;
        for (int semesi = 0; semesi < _param.getTargetSemes(false).length; semesi++) {
            final String semester = _param.getTargetSemes(false)[semesi];
            if (SEMEALL.equals(semester)) {
                checkKaikin(student, _param);
            }

            final Map attendSemes = getMappedMap(student._attendMap, _param._year + "-" + semester);
            if (attendSemes.size() == 0) {
                continue;
            }
            final int semesline = SEMEALL.equals(semester) ? lastLine : Integer.parseInt(semester);
            svf.VrsOutn("LESSON", semesline, toString(attendSemes.get("LESSON"))); // 授業日数
            svf.VrsOutn("SUSPEND", semesline, toString(add(add(toString(attendSemes.get("SUSPEND")), toString(attendSemes.get("VIRUS"))), toString(attendSemes.get("MOURNING"))))); // 出停忌引
            svf.VrsOutn("NOTICE", semesline, toString(attendSemes.get("SICK"))); // 病気
            svf.VrsOutn("PRESENT", semesline, toString(attendSemes.get("PRESENT"))); // 出席日数
            svf.VrsOutn("LATE", semesline, toString(attendSemes.get("LATE"))); // 遅刻
            svf.VrsOutn("EARLY", semesline, toString(attendSemes.get("EARLY"))); // 早退
            svf.VrsOutn("KEKKA", semesline, toString(attendSemes.get("M_KEKKA_JISU"))); // 欠課時数

            String attRemark = StringUtils.defaultString(toString(student._communicationMap.get(semester + DLM + TYPE_ATT)));
            if (SEMEALL.equals(semester)) {
                if (student._isKaikin) {
                    attRemark = "".equals(attRemark) ? "皆勤" : ("皆勤 " + attRemark);
                } else if (student._isSeikin) {
                    attRemark = "".equals(attRemark) ? "精勤" : ("精勤 " + attRemark);
                }
            }
            if (!"".equals(attRemark)) {
                svf.VrsOutn("REMARK", semesline, attRemark);
            }
        }

        // 科目
        int count = 1;
        final List classCdGroupList = getClassCdGroupList(_param, student._subclassList);
        for (int cli = 0; cli < classCdGroupList.size(); cli++) {
            final Map sameClasscdMap = (Map) classCdGroupList.get(cli);
            final List subclassList = getMappedList(sameClasscdMap, "SUBCLASSLIST");
            for (int subi = 0; subi < subclassList.size(); subi++) {
                final Subclass subclass = (Subclass) subclassList.get(subi);
                if (null != subclass._subclassname) {
                    if (subclass._subclassname.length() <= 5) {
                        svf.VrsOutn("SUBCLASS_NAME1", count, subclass._subclassname); // 科目名
                    } else if (subclass._subclassname.length() <= 7) {
                        svf.VrsOutn("SUBCLASS_NAME2", count, subclass._subclassname); // 科目名
                    } else if (subclass._subclassname.length() <= 9) {
                        svf.VrsOutn("SUBCLASS_NAME3", count, subclass._subclassname); // 科目名
                    } else {
                        svf.VrsOutn("SUBCLASS_NAME3_1", count, subclass._subclassname.substring(0, 9)); // 科目名
                        svf.VrsOutn("SUBCLASS_NAME3_2", count, subclass._subclassname.substring(9)); // 科目名
                    }
                }
                count += 1;
            }
        }

        for (int tii = 0; tii < _param._testitemList.size(); tii++) {
            final Testitem testitem = (Testitem) _param._testitemList.get(tii);
            if (null == testitem._semester || Integer.parseInt(testitem._semester) > Integer.parseInt(_param._semester)) {
                continue;
            }
            count = 1;
            boolean printAvgFlg = false;
            final List<BigDecimal> scores = new ArrayList<BigDecimal>();
            for (int cli = 0; cli < classCdGroupList.size(); cli++) {
                final Map sameClasscdMap = (Map) classCdGroupList.get(cli);
                final List subclassList = getMappedList(sameClasscdMap, "SUBCLASSLIST");

                for (int subi = 0; subi < subclassList.size(); subi++) {
                    final Subclass subclass = (Subclass) subclassList.get(subi);

                    final Score s = (Score) getMappedMap(student._scoreMap, testitem._semester + testitem._testcd).get(subclass._subclasscd);
                    if (null != s) {
                        if (!"*".equals(s._score)) {
                            svf.VrsOutn("SCORE" + testitem._semester, count, s._score); // 評価
                            if (NumberUtils.isNumber(s._score)) {
                                scores.add(new BigDecimal(s._score));
                            }
                        }
                        if (s._avg != null) {
                            svf.VrsOut("AVE" + testitem._semester, s._avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString()); // 平均(取得値はどれも同じ値)
                            printAvgFlg = true;
                        }
                    }
                    count += 1;
                }
            }
            if (!printAvgFlg && !scores.isEmpty()) {
                BigDecimal sum = new BigDecimal(0);
                for (final BigDecimal scoreBd : scores) {
                    sum = sum.add(scoreBd);
                }
                svf.VrsOut("AVE" + testitem._semester, sum.divide(new BigDecimal(scores.size()), 1, BigDecimal.ROUND_HALF_UP).toString()); // 平均(取得値はどれも同じ値)
            }
        }
    }

    private static List getClassCdGroupList(final Param param, final List subclassList) {
        final List rtn = new ArrayList();
        String classkey = null;
        Map current = null;
        final String subcls9Cd = SUBCLSCD9_HEAD + param._schoolKind + SUBCLSCD9_TAIL;
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._subclasscd.startsWith("9") && !subcls9Cd.equals(subclass._subclasscd)) {
                continue;
            }
            if (param._d026List.contains(subclass._subclasscd)) {
                continue;
            }
            if (null == current || !subclass._classkey.equals(classkey)) {
                current = new HashMap();
                current.put("CLASSKEY", subclass._classkey);
                current.put("CLSIDX", new Integer(rtn.size()));
                current.put("CLASSNAME", subclass._classname);
                rtn.add(current);
                classkey = subclass._classkey;
            }
            getMappedList(current, "SUBCLASSLIST").add(subclass);
        }
        return rtn;
    }

    private static Map addAttendMap(final Map m1, final Map m2) {
        if (null == m1 || m1.isEmpty()) {
            return m2;
        }
        if (null == m2 || m2.isEmpty()) {
            return m1;
        }
        final Map rtn = new HashMap();
        final String[] fields = {"LESSON", "VIRUS", "SICK_ONLY", "NOTICE_ONLY", "LATE", "EARLY", "M_KEKKA_JISU", };
        for (int i = 0; i < fields.length; i++) {
            rtn.put(fields[i], add((String) m1.get(fields[i]), (String) m2.get(fields[i])));
        }
        return rtn;
    }

    private static void checkKaikin(final Student student, final Param param) {
        //皆勤
        student._isKaikin = false;
        final Map attendSemes = getMappedMap(student._attendMap, param._year + "-" + SEMEALL);
        //attendSemes.put("YEAR", param._year);
        if (attendSemes.size() > 0 && isKaikin(param, student, attendSemes, KAIKIN_LMT)) {
            student._isKaikin = true;
        }
        //精勤
        student._isSeikin = false;
        if (attendSemes.size() > 0 && isKaikin(param, student, attendSemes, SEIKIN_LMT)) {
            student._isSeikin = true;
        }
    }

    private static boolean isKaikin(final Param param, final Student student, final Map attendMap, final int kaiseiLimit) {
        final int lesson = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("LESSON")), "0"));
        final int sick = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("SICK")), "0"));
        final int late = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("LATE")), "0"));
        final int early = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("EARLY")), "0"));
        final int mKekkaJisu = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("M_KEKKA_JISU")), "0"));
        boolean rtn = true;
        if (lesson <= 0 || sick + late + early + mKekkaJisu > kaiseiLimit) {
            rtn = false;
        }
        return rtn;
    }

    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /**
     * 数値を加算して文字列（両方nullの場合、null）で返す
     * @param num1
     * @param num2
     * @return
     */
    private static String add(String num1, String num2) {
        if (NumberUtils.isNumber(num2)) {
            if (NumberUtils.isNumber(num1)) {
                num1 = new BigDecimal(num1).add(new BigDecimal(num2)).toString();
            } else {
                num1 = num2;
            }
        }
        return num1;
    }

    private static class Score {
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;

        Score(
                final String subclasscd,
                final String score,
                final BigDecimal avg) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
        }
    }

    private static class Subclass {
        final String _classkey;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
        public Subclass(final String classkey, final String subclasscd, final String classname, final String subclassname, final String subclassabbv, final String requireFlg) {
            _classkey = classkey;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }
        private static Subclass getSubclass(final String subclasscd, final List subclassList) {
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclasscd.equals(subclass._subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _hrClassName1;
        final String _hrClassName2;
        final String _staffname;
        final String _staffname2;
        final String _attendno;
        final String _schregno;
        final String _name;
        final List _subclassList = new ArrayList();
        final Map _scoreMap = new HashMap();
        final Map _attendMap = new HashMap();
        final Map _attendSubclassMap = new HashMap();
        final Map _communicationMap = new HashMap();
        boolean _isKaikin;
        boolean _isSeikin;

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrNameAbbv,
            final String hrClassName1,
            final String hrClassName2,
            final String staffname,
            final String staffname2,
            final String attendno,
            final String schregno,
            final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _hrClassName1 = hrClassName1;
            _hrClassName2 = hrClassName2;
            _staffname = staffname;
            _staffname2 = staffname2;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }

        public String getAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno);
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEMESTER, COMMUNICATION, ATTENDREC_REMARK ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER <= '" + param._paramSemester + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._communicationMap.put(rs.getString("SEMESTER") + DLM + TYPE_COMM, rs.getString("COMMUNICATION"));
                        student._communicationMap.put(rs.getString("SEMESTER") + DLM + TYPE_ATT, rs.getString("ATTENDREC_REMARK"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   GDAT.GRADE_CD, ");
            stb.append("   GDAT.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   HDAT.HR_NAME, ");
            stb.append("   HDAT.HR_NAMEABBV, ");
            stb.append("   HDAT.HR_CLASS_NAME1, ");
            stb.append("   HDAT.HR_CLASS_NAME2, ");
            stb.append("   HRSTF.STAFFNAME, ");
            stb.append("   HRSTF2.STAFFNAME AS STAFFNAME2, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   BASE.NAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_BASE_MST BASE ");
            stb.append("     ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("     ON GDAT.YEAR = T1.YEAR ");
            stb.append("    AND GDAT.GRADE = T1.GRADE ");
            stb.append("   INNER JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("     ON HDAT.YEAR = T1.YEAR ");
            stb.append("    AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("    AND HDAT.GRADE = T1.GRADE ");
            stb.append("    AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("   LEFT JOIN STAFF_MST HRSTF ");
            stb.append("     ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
            stb.append("   LEFT JOIN STAFF_MST HRSTF2 ");
            stb.append("     ON HRSTF2.STAFFCD = HDAT.TR_CD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_CD, ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.HR_NAMEABBV, ");
            stb.append("   T1.HR_CLASS_NAME1, ");
            stb.append("   T1.HR_CLASS_NAME2, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.STAFFNAME2, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSKEY, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME, ");
            stb.append("   VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   TREC.VALUE_DI, ");
            stb.append("   TREC.SEMESTER || TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS SEM_TESTCD, ");
            stb.append("   TREC.SCORE, ");
            stb.append("   TRANK.AVG ");
            stb.append(" FROM ");
            stb.append("   REGD T1 ");
            stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER <= T1.SEMESTER ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN CHAIR_DAT T3 ");
            stb.append("     ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append("    AND T3.CLASSCD < '90' ");
            stb.append("   LEFT JOIN SUBCLASS_MST SUBM ");
            stb.append("     ON SUBM.CLASSCD = T3.CLASSCD ");
            stb.append("    AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("    AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("    AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("   LEFT JOIN CLASS_MST CLM ");
            stb.append("     ON CLM.CLASSCD = T3.CLASSCD ");
            stb.append("    AND CLM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("   LEFT JOIN RECORD_SCORE_DAT TREC ");
            stb.append("     ON TREC.YEAR = T3.YEAR ");
            stb.append("    AND TREC.SEMESTER <= '" + param._semester + "' ");
            stb.append("    AND  ((TREC.SEMESTER <> '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '08') ");
            stb.append("          OR (TREC.SEMESTER  = '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '08') ");
            stb.append("          OR (TREC.SEMESTER  = '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '09') ) ");
            stb.append("    AND TREC.CLASSCD = T3.CLASSCD ");
            stb.append("    AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("    AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("    AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("    AND TREC.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ");
            stb.append("     ON TRANK.YEAR = TREC.YEAR ");
            stb.append("    AND TRANK.SEMESTER = TREC.SEMESTER ");
            stb.append("    AND TRANK.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("    AND TRANK.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("    AND TRANK.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("    AND TRANK.CLASSCD = '" + CLSCD9 + "' ");
            stb.append("    AND TRANK.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("    AND TRANK.CURRICULUM_CD = '" + CURCCD9 + "' ");
            stb.append("    AND TRANK.SUBCLASSCD = '" + SUBCLSCD9 + "' ");
            stb.append("    AND TRANK.SCHREGNO = TREC.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD NOT IN ");
            stb.append("   ( ");
            stb.append("    SELECT ");
            stb.append("      TW.ATTEND_CLASSCD || '-' || TW.ATTEND_SCHOOL_KIND || '-' || TW.ATTEND_CURRICULUM_CD || '-' || TW.ATTEND_SUBCLASSCD ");
            stb.append("    FROM ");
            stb.append("      SUBCLASS_REPLACE_COMBINED_DAT TW ");
            stb.append("    WHERE ");
            stb.append("      TW.YEAR = T3.YEAR ");
            stb.append("      AND TW.COMBINED_CLASSCD || '-' || TW.COMBINED_SCHOOL_KIND || '-' || TW.COMBINED_CURRICULUM_CD || '-' || TW.COMBINED_SUBCLASSCD ");
            stb.append("          <> T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
            stb.append("   ) ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     VALUE(CLM.SHOWORDER3, 99), ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND, ");
            stb.append("     VALUE(SUBM.SHOWORDER3, 99), ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");

            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map studentMap = new HashMap();
            try {
                final String sql = stb.toString();
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == schregno) {
                        continue;
                    }

                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String hrNameAbbv = rs.getString("HR_NAME");
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String hrClassName2 = rs.getString("HR_CLASS_NAME2");
                        final String staffname = rs.getString("STAFFNAME");
                        final String staffname2 = rs.getString("STAFFNAME2");

                        String attendno = rs.getString("ATTENDNO");
                        attendno = null == attendno || !NumberUtils.isDigits(attendno) ? "" : String.valueOf(Integer.valueOf(attendno));
                        final String name = rs.getString("NAME");

                        final Student student = new Student(grade, hrClass, hrName, hrNameAbbv, hrClassName1, hrClassName2, staffname, staffname2, attendno, schregno, name); //, realName, useRealName);

                        studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    final Student student = (Student) studentMap.get(schregno);

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == subclasscd) {
                        continue;
                    }

                    if (null == Subclass.getSubclass(subclasscd, student._subclassList)) {
                        final String classkey = rs.getString("CLASSKEY");
                        final String classname = rs.getString("CLASSNAME");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = null; // rs.getString("REQUIRE_FLG");

                        final Subclass subclass = new Subclass(classkey, subclasscd, classname, subclassname, subclassabbv, requireFlg);
                        student._subclassList.add(subclass);
                    }

                    final String semtestcd = rs.getString("SEM_TESTCD");

                    if (null != semtestcd) {
                        final String score = null != rs.getString("VALUE_DI") ? rs.getString("VALUE_DI") : rs.getString("SCORE");

                        final Score s = new Score(subclasscd, score, rs.getBigDecimal("AVG"));

                        getMappedMap(student._scoreMap, semtestcd).put(subclasscd, s);
                    }

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            loadAttendance(db2, param, param._year, param._semester, param._date, studentMap, new HashMap(param._attendParamMap));

            Student.setHreportremarkCommunication(param, db2, studentList);

            return studentList;
        }

        private static void loadAttendance(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String date,
                final Map studentMap,
                final Map attendParamMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, attendParamMap);
                //log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final Map semes = getMappedMap(student._attendMap, year + "-" + rs.getString("SEMESTER"));

                        semes.put("LESSON", rs.getString("LESSON"));
                        semes.put("MLESSON", rs.getString("MLESSON"));
                        semes.put("SUSPEND", rs.getString("SUSPEND"));
                        semes.put("MOURNING", rs.getString("MOURNING"));
                        semes.put("SICK_ONLY", rs.getString("SICK_ONLY"));
                        semes.put("NOTICE_ONLY", rs.getString("NOTICE_ONLY"));
                        semes.put("SICK_NOTICE", add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")));
                        final String putWk = add(add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")), rs.getString("NONOTICE_ONLY"));
                        semes.put("SICK", putWk);
                        semes.put("PRESENT", rs.getString("PRESENT"));
                        semes.put("VIRUS", rs.getString("VIRUS"));
                        semes.put("LATE", rs.getString("LATE"));
                        semes.put("EARLY", rs.getString("EARLY"));
                        semes.put("M_KEKKA_JISU", rs.getString("M_KEKKA_JISU"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(year, semester, null, date, param._attendParamMap);
                //log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        getMappedMap(student._attendSubclassMap, rs.getString("SUBCLASSCD")).put(year + "-" + rs.getString("SEMESTER"), "1".equals(rs.getString("IS_COMBINED_SUBCLASS")) ? rs.getBigDecimal("REPLACED_SICK") : rs.getBigDecimal("SICK2"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Regd {
        final Student _student;
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendNo;

        public Regd(
                final Student student,
                final String year,
                final String semester,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendNo) {
            _student = student;
            _year = year;
            _semester = semester;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }
    }

    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _semestername = semestername;
        }

        public static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, T1.TESTITEMNAME ";
            sql += " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + param._year + "' ";
            sql += "   AND T1.TESTKINDCD || T1.TESTITEMCD = '9900' ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            Map cdMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname);
                    cdMap.put(semester + testcd, testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            final String[] cds = {G1_HYOKA_TESTCD, G2_HYOKA_TESTCD, G3_HYOKA_TESTCD, GAKUNENHYOKA_TESTCD};
            List rtn = new ArrayList();
            for (int i = 0; i < cds.length; i++) {
                Testitem testitem = (Testitem) cdMap.get(cds[i]);
                if (null == testitem) {
                    testitem = new Testitem(cds[i].substring(0, 1), cds[i].substring(1), null, null);
                }
                rtn.add(testitem);
            }
            return rtn;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _paramSemester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _date;
        final String _nendo;

        final List _testitemList;
        final String _gradeCd;

        final String _schoolKind;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _hRepRemDatCS[];

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private Map _semesterNameMap;
        private String _useClassDetailDat;
        private List _d026List = new ArrayList();

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;

        private final Map _attendParamMap;

        private final String _lastSemester;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _paramSemester = request.getParameter("SEMESTER");
            _definecode = createDefineCode(db2);
            _lastSemester = String.valueOf(_definecode.semesdiv);
            if (String.valueOf(_definecode.semesdiv).equals(_paramSemester)) {
                _semester = SEMEALL; // 最終学期は学年末
            } else {
                _semester = _paramSemester;
            }
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _nendo = _year + "年度";
            _date = request.getParameter("DATE");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE");
            if ("".equals(_HREPORTREMARK_DAT_COMMUNICATION_SIZE)) {
                _hRepRemDatCS = new String[2];
                _hRepRemDatCS[0] = "20";
                _hRepRemDatCS[1] = "20";
            } else {
                _hRepRemDatCS = StringUtils.split(_HREPORTREMARK_DAT_COMMUNICATION_SIZE, "*");
            }

            _schoolKind = getSchoolKind(db2);
            _testitemList = Testitem.getTestitemList(db2, this);

            loadSemester(db2, _year);
            loadNameMstD026(db2);
            setCertifSchoolDat(db2);
            _gradeCd = getGradeCd(db2, _grade);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "2");

        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("非表示科目:" + _d026List);
        }

        public String getSemesterName(final String semester) {
            return (String) (_semesterNameMap.containsKey(semester) ?_semesterNameMap.get(semester) : "");
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            //log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _semesterNameMap = new HashMap();
            try {
                final String sql;
                sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE"
                    + " from"
                    + "   SEMESTER_MST"
                    + " where"
                    + "   YEAR='" + year + "'"
                    + " order by SEMESTER"
                ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    _semesterNameMap.put(semester, name);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolJobName = rs.getString("JOB_NAME");
                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolHrJobName = rs.getString("REMARK2");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _certifSchoolSchoolName = StringUtils.defaultString(_certifSchoolSchoolName);
            _certifSchoolJobName = StringUtils.defaultString(_certifSchoolJobName, "学校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(_certifSchoolPrincipalName);
            _certifSchoolHrJobName = StringUtils.defaultString(_certifSchoolHrJobName, "担任");
        }

        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "　";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        public String[] getTargetSemes(final boolean addForce_SemeAll) {
            List semesters = new ArrayList();
            String[] s = {"1", "2", "3"};
            for (int i = 0; i < s.length; i++) {
                if (_semester.compareTo(s[i]) >= 0) {
                    semesters.add(s[i]);
                }
            }
            if (semesters.contains(_lastSemester)) {
                semesters.add(SEMEALL);
            } else if (addForce_SemeAll) {
                semesters.add(SEMEALL);
            }
            String[] rtn = new String[semesters.size()];
            semesters.toArray(rtn);
            return rtn;
        }
    }
}

// eof


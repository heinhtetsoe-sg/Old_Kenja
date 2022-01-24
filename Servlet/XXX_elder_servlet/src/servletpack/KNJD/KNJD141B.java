/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/02/24
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD141B {

    private static final Log log = LogFactory.getLog(KNJD141B.class);

    private static final String SEMEALL = "9";

    private static final String SELECT_CLASSCD_UNDER = "89";
    private static final String REIHAI = "95-H-3-950100"; //礼拝
    private static final String SHR    = "92-H-3-920100"; //SHR
    private static final String LHR    = "94-H-3-940100"; //LHR
    private static final String SDIV   = "01"; //素点のみ

    private static final String ALL3  = "333333";
    private static final String ALL5  = "555555";
    private static final String ALL9  = "999999";
    private static final String ALL9A = "99999A";
    private static final String ALL9B = "99999B";

    private static final String PER_DIV_SUBCLASS = "01";
    private static final String PER_DIV_GRADE = "02";
    private static final String PER_DIV_COURSE = "03";

    private boolean _hasData;
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
        final Map studentMap = getStudentMap(db2);

        if (studentMap.isEmpty()) return;
        setScore(db2, studentMap);

        for (Iterator ite = _param._semesterMap.keySet().iterator(); ite.hasNext();) {
            final String seme = (String) ite.next();
            if (Integer.valueOf(seme) <= Integer.valueOf(_param._semester)) {
                final DateRange range = _param._semesterMap.get(seme);
                SubclassAttendance.load(db2, _param, studentMap, range);
            }
        }

        final String form = "1".equals(_param._semester)  ? "KNJD141B_1.frm" : "KNJD141B_2.frm";

        for (Iterator iterator = studentMap.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final Student student = (Student) studentMap.get(key);
            svf.VrSetForm(form , 1);

            printTitle(svf, student);
            printScoreRecord(svf, student);
            printAttend(svf, student);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printScoreRecord(final Vrw32alp svf, final Student student) {

        String semeChangeFlg = ""; //学期切替フラグ
        final int maxCnt = 11; //最大科目数
        int cnt = 1; //繰返し回数
        int idx = 1; //ページの数
        Map bunkatsuSubclassMap = new LinkedHashMap(); //ページに印字する科目Map
        Map pagePrintMap = new LinkedHashMap(); //ページ毎map

        //１ページに印字する科目数で分割する
        for (Iterator ite = student._printSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String subclassKey = (String)ite.next();
            final String seme = subclassKey.substring(0,1); //KEYの最初は学期
            final Map<String, String> printSubclassMap = (Map)student._printSubclassMap.get(subclassKey);
            final String classcd = (String) printSubclassMap.get("CLASSCD");
            if (Integer.valueOf(classcd) > Integer.valueOf(SELECT_CLASSCD_UNDER)) {
                continue;
            }

            if (!semeChangeFlg.equals(seme)) { //学期が変わったら
                cnt = 1;
                idx = 1; //最初のページ
                if (!"".equals(semeChangeFlg)) { //最初の学期でなければ
                    if (!pagePrintMap.containsKey(idx)) { //ページのmapがなければ作成
                        pagePrintMap.put(idx, bunkatsuSubclassMap);
                    }
                    bunkatsuSubclassMap = (Map)pagePrintMap.get(idx); //1ページ目のmap取得
                }
                semeChangeFlg = seme;
            }
            if (cnt > maxCnt) { //最大数を超えたら
                if (!pagePrintMap.containsKey(idx)) { //今のページmapがなければ作成
                    pagePrintMap.put(idx, bunkatsuSubclassMap);
                }
                idx++; //次のページ
                cnt = 1;

                if (!pagePrintMap.containsKey(idx)) { //次のページmapがなければ作成
                    pagePrintMap.put(idx, new LinkedHashMap());
                }
                bunkatsuSubclassMap = (Map)pagePrintMap.get(idx); //次のページmap取得
            }
            bunkatsuSubclassMap.put(subclassKey, printSubclassMap); //印字ページmapに格納
            cnt++;
        }

        if (pagePrintMap.isEmpty()) {
            pagePrintMap.put(idx, bunkatsuSubclassMap);
        }

        boolean semeNameFlg = false; //学期名称印字フラグ
        semeChangeFlg = ""; //学期切替フラグ
        cnt = 1;

        //ページ毎のループ
        for (Iterator pageIte = pagePrintMap.values().iterator(); pageIte.hasNext();) {
            if (cnt > 1) {
                printAttend(svf, student);
                svf.VrEndPage();
                printTitle(svf, student);
                semeNameFlg = false;
            }
            final Map bunkatsuMap = (Map) pageIte.next();
            int gyo = 1; //印字行

            //科目毎のループ
            for (Iterator ite = bunkatsuMap.keySet().iterator(); ite.hasNext();) {
                final String subclassKey = (String) ite.next();
                final String seme = subclassKey.substring(0, 1); //KEYの最初は学期

                if (!semeChangeFlg.equals(seme)) {
                    semeChangeFlg = seme;
                    semeNameFlg = false;
                    gyo = 1;
                }

                if (!semeNameFlg) {
                    final DateRange range = _param._semesterMap.get(seme);
                    svf.VrsOut("SEMESTER_NAME" + seme, range._name); //学期
                    semeNameFlg = true;
                }

                final Map<String, String> printSubclassMap = (Map) bunkatsuMap.get(subclassKey);

                svf.VrsOutn("CLASS_NAME" + seme, gyo, printSubclassMap.get("CLASSNAME")); //教科

                final String subclassName = printSubclassMap.get("SUBCLASSNAME");
                final int keta = KNJ_EditEdit.getMS932ByteLength(subclassName);
                final String field = keta <= 16 ? "1" : keta <= 20 ? "2" : "3";
                svf.VrsOutn("SUBCLASS_NAME" + seme + "_" + field, gyo, subclassName); //科目

                svf.VrsOutn("SELECT" + seme, gyo, printSubclassMap.get("DIV")); //必・選
                svf.VrsOutn("CREDIT" + seme, gyo, printSubclassMap.get("CREDITS")); //単位

                final Map testMap = student._testMap.get(seme);
                if (testMap == null) {
                    gyo++;
                    continue;
                }

                int testCnt = 1;
                final String subclassCd = subclassKey.substring(2, subclassKey.length());

                for (Iterator iteTest = testMap.keySet().iterator(); iteTest.hasNext();) {
                    final String testKey = (String) iteTest.next();
                    final Test test = (Test) testMap.get(testKey);
                    svf.VrsOut("TEST_NAME" + seme + "_" + testCnt, test._testName); //考査

                    if (test._scoreMap.containsKey(subclassCd)) {
                        final Score score = test._scoreMap.get(subclassCd);

                        if (score._score != null && !"*".equals(score._valueDi)) {
                            String mark = "";
                            if (Integer.valueOf(score._score) <= Integer.valueOf(score._attentionScore)) {
                                mark = "▼"; //注意点以下なら頭に記号
                            }
                            svf.VrsOutn("SCORE" + seme + "_" + testCnt, gyo, mark + score._score); //点数
                            svf.VrsOutn("PERFECT" + seme + "_" + testCnt, gyo, score._perfect); //満点
                            svf.VrsOutn("HR_AVE" + seme + "_" + testCnt, gyo, score._hrAvg); //クラス平均
                            svf.VrsOutn("GRADE_AVE" + seme + "_" + testCnt, gyo, score._gradeAvg); //学年平均
                        }
                    }
                    testCnt++;
                }

                //備考
                svf.VrsOutn("HR_ABBV" + seme + "_1", gyo, printSubclassMap.get("REMARK3")); //クラス
                svf.VrsOutn("CHAIR_TR_NAME" + seme + "_1", gyo, printSubclassMap.get("STAFFNAME")); //担当者
                if (student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map attendSubClassMap = (Map) student._attendSubClassMap.get(subclassCd);
                    if (attendSubClassMap.containsKey(seme)) {
                        final SubclassAttendance attend = (SubclassAttendance) attendSubClassMap.get(seme);
                        svf.VrsOutn("SICK" + seme + "_1", gyo, String.valueOf(attend._sick)); //欠席
                        svf.VrsOutn("MOURNING" + seme + "_1", gyo, String.valueOf(attend._mourning)); //忌引
                        svf.VrsOutn("LESSON" + seme + "_1", gyo, String.valueOf(attend._late)); //遅刻（フィールド名誤り）
                    }
                }
                gyo++;
            }
            cnt++;
        }
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        svf.VrsOut("PRINT_DATE",  _param._printDate + "  現在");
        final String hrName = StringUtils.defaultString(student._hr_Name);
        final String attendno = String.valueOf(Integer.parseInt(StringUtils.defaultString(student._attendno)));
        svf.VrsOut("HR_NAME",  hrName + " No." + attendno);
        svf.VrsOut("NAME",  student._name);
    }

    //担任授業 礼拝、SHR、LHR
    private void printAttend(final Vrw32alp svf, final Student student) {
        final String[] subclassCds = {REIHAI, SHR, LHR};
        for (int seme = 1; seme <= Integer.valueOf(_param._semester); seme++) {
            int gyo = 1; //印字行
            for (final String subclassCd : subclassCds) {
                final String key = seme + "-" + subclassCd;
                if (student._printSubclassMap.containsKey(key)) {
                    final Map<String, String> printSubclassMap = (Map) student._printSubclassMap.get(key);
                    svf.VrsOutn("CHAIR_TR_NAME" + seme + "_2", gyo, printSubclassMap.get("STAFFNAME")); //担当者
                }

                if (student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map attendSubClassMap = (Map) student._attendSubClassMap.get(subclassCd);
                    if (attendSubClassMap.containsKey(String.valueOf(seme))) {
                        final SubclassAttendance attend = (SubclassAttendance) attendSubClassMap.get(String.valueOf(seme));
                        svf.VrsOutn("SICK" + seme + "_2", gyo, String.valueOf(attend._sick));         //欠席
                        svf.VrsOutn("MOURNING" + seme + "_2", gyo, String.valueOf(attend._mourning)); //忌引
                        svf.VrsOutn("LESSON" + seme + "_2", gyo, String.valueOf(attend._late));       //遅刻（フィールド名誤り）
                    }
                }
                gyo++;
            }
        }
    }

    private Map getStudentMap(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if ("1".equals(rs.getString("LEAVE"))){
                    continue; //異動フラグ生徒は対象外
                }
                final String schregno = rs.getString("SCHREGNO");
                final String semester = rs.getString("SEMESTER");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String course = rs.getString("COURSE");
                final String hr_Name = rs.getString("HR_NAME");
                final Student student = new Student(schregno, semester, hr_Class, name, hr_Name, attendno, grade, course);

                student.setSubclass(db2, student._course);

                retMap.put(schregno, student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  W1.SCHREGNO");
        stb.append("            ,W1.SEMESTER ");
        stb.append("            ,W1.GRADE ");
        stb.append("            ,W1.HR_CLASS ");
        stb.append("            ,W1.ATTENDNO ");
        stb.append("            ,W7.NAME ");
        stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
        stb.append("            ,W6.HR_NAME ");
        stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  ELSE 0 END AS LEAVE ");
        stb.append("     FROM    SCHREG_REGD_DAT W1 ");
        stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + _param._grade + "' ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
        stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
        stb.append("                  AND W6.GRADE = W1.GRADE ");
        stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
        stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("         AND W1.GRADE || W1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND W1.GRADE = '" + _param._grade + "'");
            stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
        stb.append("         W1.HR_CLASS, W1.ATTENDNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        final String _schregno;
        final String _semester;
        final String _hr_Class;
        final String _name;
        final String _hr_Name;
        final String _attendno;
        final String _grade;
        final String _course;
        final Map _attendSubClassMap = new TreeMap();
        final Map _printSubclassMap = new LinkedHashMap();
        final Map<String, Map<String, Test>> _testMap = new TreeMap();

        public Student(final String schregno, final String semester, final String hr_Class, final String name,
                final String hr_Name, final String attendno, final String grade, final String course) {
            _schregno = schregno;
            _semester = semester;
            _hr_Class = hr_Class;
            _name = name;
            _hr_Name = hr_Name;
            _attendno = attendno;
            _grade = grade;
            _course = course;
        }

        //講座から科目情報取得
        private String setSubclassSql(final String course) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     STD.YEAR, ");
            stb.append("     STD.SEMESTER, ");
            stb.append("     STD.CHAIRCD, ");
            stb.append("     CHAIR.CLASSCD, ");
            stb.append("     CD004.REMARK3, ");
            stb.append("     CLASS.CLASSNAME, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSKEY, ");
            stb.append("     SUBCLASS.SUBCLASSNAME, ");
            stb.append("     CREDIT.CREDITS, ");
            stb.append("     CASE WHEN SUBCLASS.ELECTDIV = '1' THEN '1' ELSE '0' END AS DIV, ");
            stb.append("     VALUE(NAME.NAME1, '') AS RETSUNAME, ");
            stb.append("     STF.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD ");
            stb.append(" INNER JOIN ");
            stb.append("     CHAIR_DAT CHAIR ");
            stb.append("      ON CHAIR.YEAR = STD.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = STD.SEMESTER ");
            stb.append("     AND CHAIR.CHAIRCD = STD.CHAIRCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CHAIR_DETAIL_DAT CD004 ");
            stb.append("      ON CD004.YEAR = CHAIR.YEAR ");
            stb.append("     AND CD004.SEMESTER = CHAIR.SEMESTER ");
            stb.append("     AND CD004.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     AND CD004.SEQ = '004' ");
            stb.append(" INNER JOIN ");
            stb.append("     CLASS_MST CLASS ");
            stb.append("      ON CLASS.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND CLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append(" INNER JOIN ");
            stb.append("     SUBCLASS_MST SUBCLASS ");
            stb.append("      ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CREDIT_MST CREDIT ");
            stb.append("      ON CREDIT.YEAR = STD.YEAR ");
            stb.append("     AND CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE = '" + course + "' ");
            stb.append("     AND CREDIT.GRADE = '" + _param._grade + "' ");
            stb.append("     AND CREDIT.CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("     AND CREDIT.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("     AND CREDIT.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("     AND CREDIT.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     V_NAME_MST NAME ");
            stb.append("      ON NAME.YEAR = STD.YEAR ");
            stb.append("     AND NAME.NAMECD1 = 'B023' ");
            stb.append("     AND NAME.NAMECD2 = CD004.REMARK1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     CHAIR_STF_DAT CSTF ");
            stb.append("      ON CSTF.YEAR = STD.YEAR ");
            stb.append("     AND CSTF.SEMESTER = STD.SEMESTER ");
            stb.append("     AND CSTF.CHAIRCD = STD.CHAIRCD ");
            stb.append("     AND CSTF.CHARGEDIV = '1' ");
            stb.append(" LEFT JOIN ");
            stb.append("     STAFF_MST STF ");
            stb.append("      ON STF.STAFFCD = CSTF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     STD.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND STD.YEAR = '" + _param._year + "' ");
            stb.append("     AND STD.SEMESTER <= '" + _param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     STD.YEAR, STD.SEMESTER, DIV, SUBCLASSKEY ");

            return stb.toString();
        }

        private void setSubclass(final DB2UDB db2, final String course) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = setSubclassSql(course);
            log.debug(" subclass sql = " + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Map subclass;
                    final String key = rs.getString("SEMESTER") + "-" + rs.getString("SUBCLASSKEY");
                    if (_printSubclassMap.containsKey(key)) {
                        continue;
                    } else {
                        _printSubclassMap.put(key, new TreeMap());
                    }
                    subclass = (Map)_printSubclassMap.get(key);

                    subclass.put("SUBCLASSKEY", rs.getString("SUBCLASSKEY"));
                    subclass.put("CLASSNAME", rs.getString("CLASSNAME"));
                    subclass.put("SUBCLASSNAME", rs.getString("SUBCLASSNAME"));
                    subclass.put("CREDITS", rs.getString("CREDITS"));
                    subclass.put("DIV", "1".equals(rs.getString("DIV")) ? "選" + rs.getString("RETSUNAME") : "必");
                    subclass.put("STAFFNAME", rs.getString("STAFFNAME"));
                    subclass.put("REMARK3", rs.getString("REMARK3"));
                    subclass.put("CLASSCD", rs.getString("CLASSCD"));
                }
                DbUtils.closeQuietly(rs);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private void setScore(final DB2UDB db2, final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Student student = null;
        final String sql = setScoreSql();
        log.debug("scoreSql = " + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String semester = rs.getString("SEMESTER");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String score_Div = rs.getString("SCORE_DIV");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String gradeAvg = rs.getString("GRADE_AVG");
                final String hrAvg = rs.getString("HR_AVG");
                final String valueDi = rs.getString("VALUE_DI");
                final String testName = rs.getString("TESTITEMABBV1");
                final String perfect = rs.getString("PERFECT");
                final String attentionScore = rs.getString("ATTENTION_SCORE");

                if (studentMap.containsKey(schregno)) {
                    student = (Student) studentMap.get(schregno);
                } else {
                    continue;
                }

                if (!student._testMap.containsKey(semester)) {
                    student._testMap.put(semester, new LinkedMap()); //学期毎
                }

                final Map semeMap = student._testMap.get(semester);
                final String key = testkindcd + testitemcd + score_Div;

                if (!semeMap.containsKey(key)) {
                    semeMap.put(key, new Test(testName)); //考査毎
                }

                final Test test = (Test)semeMap.get(key);
                if (!test._scoreMap.containsKey(subclasscd)) {
                     final Score wk = new Score(score, gradeAvg, hrAvg, valueDi, perfect, attentionScore); //科目毎
                     test._scoreMap.put(subclasscd, wk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    //考査情報取得　素点のみ
    private String setScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH  SCHNO_A AS(SELECT ");
        stb.append("     W1.SCHREGNO, ");
        stb.append("     W1.YEAR, ");
        stb.append("     W1.SEMESTER, ");
        stb.append("     W1.GRADE, ");
        stb.append("     W1.HR_CLASS, ");
        stb.append("     W1.COURSECD, ");
        stb.append("     W1.MAJORCD, ");
        stb.append("     W1.COURSECODE, ");
        stb.append("     CHAIR.CHAIRCD, ");
        stb.append("     CHAIR.CLASSCD, ");
        stb.append("     CHAIR.SCHOOL_KIND, ");
        stb.append("     CHAIR.CURRICULUM_CD, ");
        stb.append("     CHAIR.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT W1 ");
        stb.append(" INNER JOIN ");
        stb.append("     CHAIR_STD_DAT STD ");
        stb.append("      ON STD.YEAR = W1.YEAR ");
        stb.append("     AND STD.SEMESTER = W1.SEMESTER ");
        stb.append("     AND STD.SCHREGNO = W1.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     CHAIR_DAT CHAIR ");
        stb.append("      ON CHAIR.YEAR = STD.YEAR ");
        stb.append("     AND CHAIR.SEMESTER = STD.SEMESTER ");
        stb.append("     AND CHAIR.CHAIRCD = STD.CHAIRCD ");
        stb.append("     AND CHAIR.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
        stb.append(" WHERE ");
        stb.append("     W1.YEAR = '" + _param._year + "' AND ");
        stb.append("     W1.SEMESTER <= '" + _param._semester + "' AND ");
        if ("1".equals(_param._disp)) {
            stb.append("         W1.GRADE || W1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         W1.GRADE = '" + _param._grade + "' AND ");
            stb.append("         W1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append(" )  ");
        stb.append(" SELECT ");
        stb.append("     W3.SCHREGNO, ");
        stb.append("     W3.SEMESTER, ");
        stb.append("     W3.TESTKINDCD, ");
        stb.append("     W3.TESTITEMCD, ");
        stb.append("     W3.SCORE_DIV, ");
        stb.append("     W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     W3.SCORE, ");
        stb.append("     DECIMAL(INT(FLOAT(T_AVG1.AVG) * 10 + 0.5) / 10.0, 5, 1) AS GRADE_AVG, ");
        stb.append("     DECIMAL(INT(FLOAT(T_AVG2.AVG) * 10 + 0.5) / 10.0, 5, 1) AS HR_AVG, ");
        stb.append("     W3.VALUE_DI, ");
        stb.append("     TEST.TESTITEMABBV1, ");
        stb.append("     CASE WHEN PER_SUBCLASS.PERFECT IS NOT NULL THEN PER_SUBCLASS.PERFECT WHEN PER_GRADE.PERFECT IS NOT NULL THEN PER_GRADE.PERFECT ELSE PER_COURSE.PERFECT END AS PERFECT, ");
        stb.append("     W1.CHAIRCD, ");
        stb.append("     VALUE(ATTENTION.ATTENTION_SCORE, 0) AS ATTENTION_SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHNO_A W1 ");
        stb.append(" LEFT JOIN  ");
        stb.append("     RECORD_SCORE_DAT W3 ");
        stb.append("      ON W3.YEAR = W1.YEAR ");
        stb.append("     AND W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("     AND W3.SEMESTER = W1.SEMESTER ");
        stb.append("     AND W3.CLASSCD = W1.CLASSCD ");
        stb.append("     AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
        stb.append("     AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
        stb.append("     AND W3.SUBCLASSCD = W1.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_AVERAGE_SDIV_DAT T_AVG1 ");
        stb.append("      ON T_AVG1.YEAR = W3.YEAR ");
        stb.append("     AND T_AVG1.SEMESTER = W3.SEMESTER ");
        stb.append("     AND T_AVG1.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND T_AVG1.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND T_AVG1.SCORE_DIV = W3.SCORE_DIV ");
        stb.append("     AND T_AVG1.GRADE = W1.GRADE ");
        stb.append("     AND T_AVG1.CLASSCD = W3.CLASSCD ");
        stb.append("     AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND T_AVG1.AVG_DIV = '1' "); //学年平均
        stb.append("     AND T_AVG1.HR_CLASS   = '000' ");
        stb.append("     AND T_AVG1.COURSECD   = '0' ");
        stb.append("     AND T_AVG1.MAJORCD    = '000' ");
        stb.append("     AND T_AVG1.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_AVERAGE_SDIV_DAT T_AVG2 ");
        stb.append("      ON T_AVG2.YEAR = W3.YEAR ");
        stb.append("     AND T_AVG2.SEMESTER = W3.SEMESTER ");
        stb.append("     AND T_AVG2.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND T_AVG2.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND T_AVG2.SCORE_DIV = W3.SCORE_DIV ");
        stb.append("     AND T_AVG2.GRADE = W1.GRADE ");
        stb.append("     AND T_AVG2.CLASSCD = W3.CLASSCD ");
        stb.append("     AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND T_AVG2.AVG_DIV = '2' "); //クラス平均
        stb.append("     AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
        stb.append("     AND T_AVG2.COURSECD   = '0' ");
        stb.append("     AND T_AVG2.MAJORCD    = '000' ");
        stb.append("     AND T_AVG2.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_RANK_SDIV_DAT S1 ");
        stb.append("      ON S1.YEAR = W3.YEAR ");
        stb.append("     AND S1.SEMESTER = W3.SEMESTER ");
        stb.append("     AND S1.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND S1.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND S1.SCORE_DIV = W3.SCORE_DIV ");
        stb.append("     AND S1.CLASSCD = W3.CLASSCD ");
        stb.append("     AND S1.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND S1.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND S1.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND S1.SCHREGNO = W3.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV TEST ");
        stb.append("      ON TEST.YEAR = W3.YEAR ");
        stb.append("     AND TEST.SEMESTER = W3.SEMESTER ");
        stb.append("     AND TEST.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND TEST.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND TEST.SCORE_DIV = W3.SCORE_DIV ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_SUBCLASS ");
        stb.append("      ON PER_SUBCLASS.YEAR = W3.YEAR ");
        stb.append("     AND PER_SUBCLASS.SEMESTER = W3.SEMESTER ");
        stb.append("     AND PER_SUBCLASS.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND PER_SUBCLASS.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND PER_SUBCLASS.CLASSCD = W3.CLASSCD ");
        stb.append("     AND PER_SUBCLASS.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND PER_SUBCLASS.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND PER_SUBCLASS.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND PER_SUBCLASS.DIV = '" + PER_DIV_SUBCLASS + "' "); // 01:科目
        stb.append("     AND PER_SUBCLASS.GRADE = '00' ");
        stb.append("     AND PER_SUBCLASS.COURSECD = '0' ");
        stb.append("     AND PER_SUBCLASS.MAJORCD = '000' ");
        stb.append("     AND PER_SUBCLASS.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_GRADE ");
        stb.append("      ON PER_GRADE.YEAR = W3.YEAR ");
        stb.append("     AND PER_GRADE.SEMESTER = W3.SEMESTER ");
        stb.append("     AND PER_GRADE.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND PER_GRADE.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND PER_GRADE.CLASSCD = W3.CLASSCD ");
        stb.append("     AND PER_GRADE.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND PER_GRADE.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND PER_GRADE.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND PER_GRADE.DIV = '" + PER_DIV_GRADE + "' "); // 02:学年
        stb.append("     AND PER_GRADE.GRADE = W1.GRADE ");
        stb.append("     AND PER_GRADE.COURSECD = '0' ");
        stb.append("     AND PER_GRADE.MAJORCD = '000' ");
        stb.append("     AND PER_GRADE.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_COURSE ");
        stb.append("      ON PER_COURSE.YEAR = W3.YEAR ");
        stb.append("     AND PER_COURSE.SEMESTER = W3.SEMESTER ");
        stb.append("     AND PER_COURSE.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND PER_COURSE.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND PER_COURSE.CLASSCD = W3.CLASSCD ");
        stb.append("     AND PER_COURSE.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND PER_COURSE.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND PER_COURSE.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND PER_COURSE.DIV = '" + PER_DIV_COURSE + "' "); // 3:コース
        stb.append("     AND PER_COURSE.GRADE = W1.GRADE ");
        stb.append("     AND PER_COURSE.COURSECD = W1.COURSECD ");
        stb.append("     AND PER_COURSE.MAJORCD = W1.MAJORCD ");
        stb.append("     AND PER_COURSE.COURSECODE = W1.COURSECODE ");
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_ATTENTION_SCORE_MST ATTENTION ");
        stb.append("      ON ATTENTION.YEAR = W3.YEAR ");
        stb.append("     AND ATTENTION.SEMESTER = W3.SEMESTER ");
        stb.append("     AND ATTENTION.TESTKINDCD = W3.TESTKINDCD ");
        stb.append("     AND ATTENTION.TESTITEMCD = W3.TESTITEMCD ");
        stb.append("     AND ATTENTION.SCORE_DIV = W3.SCORE_DIV ");
        stb.append("     AND ATTENTION.CLASSCD = W3.CLASSCD ");
        stb.append("     AND ATTENTION.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("     AND ATTENTION.CURRICULUM_CD = W3.CURRICULUM_CD ");
        stb.append("     AND ATTENTION.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append("     AND ATTENTION.CHAIRCD = W1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     W3.SEMESTER <= '" + _param._semester + "' AND ");
        stb.append("     W3.SCORE_DIV = '" + SDIV + "' AND ");
        stb.append("     W3.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "','" + ALL9A + "','" + ALL9B + "') ");
        stb.append(" ORDER BY ");
        stb.append("     W3.SCHREGNO, W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, W3.SUBCLASSCD ");

        return stb.toString();
    }

    private class Test {
        final String _testName;
        final Map<String, Score> _scoreMap = new LinkedMap();

        public Test(final String testName) {
            _testName = testName;
        }
    }

    private class Score {
        final String _score;
        final String _gradeAvg;
        final String _hrAvg;
        final String _valueDi;
        final String _perfect;
        final String _attentionScore;

        public Score(final String score, final String gradeAvg, final String hrAvg, final String valueDi, final String perfect, final String attentionScore) {
            _score = score;
            _gradeAvg = gradeAvg;
            _hrAvg = hrAvg;
            _valueDi = valueDi;
            _perfect = perfect;
            _attentionScore = attentionScore;
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        final BigDecimal _mourning;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early, final BigDecimal mourning) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
            _mourning = mourning;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final DateRange dateRange) {
            log.debug(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || dateRange._sdate.compareTo(dateRange._edate) > 0) {
                return;
            }

            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;

            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    param._year,
                    dateRange._key,
                    dateRange._sdate,
                    edate,
                    param._attendParamMap
            );
            log.debug(sql);

            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it3 = studentMap.keySet().iterator(); it3.hasNext();) {
                    final String key = (String) it3.next();
                    final Student student = (Student) studentMap.get(key);

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!dateRange._key.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");
                            final BigDecimal mourning = rs.getBigDecimal("MOURNING");

                            final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early, mourning);
                            Map setSubAttendMap = null;

                            if (student._attendSubClassMap.containsKey(subclasscd)) {
                                setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                            } else {
                                setSubAttendMap = new TreeMap();
                            }

                            setSubAttendMap.put(dateRange._key, subclassAttendance);

                            student._attendSubClassMap.put(subclasscd, setSubAttendMap);
                        }

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
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _semester;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public boolean isNull() {
            return null == _sdate || null == _edate;
        }
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            final DateRange dr = (DateRange) o;
            return _key.equals(dr._key) && StringUtils.defaultString(_name).equals(StringUtils.defaultString(dr._name)) && rangeEquals(dr);
        }
        public boolean rangeEquals(final DateRange dr) {
            return StringUtils.defaultString(_sdate).equals(StringUtils.defaultString(dr._sdate)) && StringUtils.defaultString(_edate).equals(StringUtils.defaultString(dr._edate));
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _name + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static String mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        if (null == list) {
            return stb.toString();
        }
        String comma0 = "";
        for (final String s : list) {
            if (null == s || s.length() == 0) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _gradeHrclass;
        final String _disp;;
        final String _semester;
        final String _year;
        final String _grade;
        final String _date;
        final String _printDate;
        final Map<String, DateRange> _semesterMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, UnsupportedEncodingException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _disp = request.getParameter("DISP");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
                _gradeHrclass = "";
            } else {
                _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
                _grade = _gradeHrclass.substring(0,2);
            }

            _semester = request.getParameter("SEMESTER");
            _year = request.getParameter("YEAR");
            _date = request.getParameter("DATE").replace('/', '-');
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分");
            _printDate = sdf.format(new Date());
            _semesterMap = getSemesterMap(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "1");

            setSubclassMst(db2);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            String sql = "";
            sql += " SELECT ";
            sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
            sql += " T1.CLASSCD, ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
            sql += " COMB1.CALCULATE_CREDIT_FLG, ";
            sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
            sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
            sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _year + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
            sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _year + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._attendSubclassList.add(attend);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        protected Map<String, DateRange> getSemesterMap(
                final DB2UDB db2
        ) {
            final Map<String, DateRange> rtnMap= new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME  "
                    + "  ,T1.SDATE "
                    + "  ,T1.EDATE "
                    + " FROM SEMESTER_MST T1 "
                    + " WHERE T1.YEAR = '" + _year + "' "
                    + " ORDER BY T1.SEMESTER ";
            log.debug(" sql = " + sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put(rs.getString("SEMESTER"), new DateRange(rs.getString("SEMESTER"), rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
}
// eof

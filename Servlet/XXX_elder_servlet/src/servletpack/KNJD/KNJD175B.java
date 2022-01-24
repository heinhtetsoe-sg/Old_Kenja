/*
 * $Id: 11a9cd899293915f0c487d89678cead76350bc84 $
 *
 * 作成日: 2017/11/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD175B {

    private static final Log log = LogFactory.getLog(KNJD175B.class);

    private static final String SEME1 = "1";
    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_1GAKKI_HYOKA = "1990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

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

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            log.info(" student = " + student._schregno);

            printStudent(db2, svf, student);
        }
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD175B.frm";
        svf.VrSetForm(form, 4);

        svf.VrsOut("SEMESTER1_1", _param._semestername1); // 学期
        svf.VrsOut("SEMESTER1_9", "学年"); // 学期
        svf.VrsOutn("SEMESTER2", 1, _param._semestername1); // 学期
        svf.VrsOutn("SEMESTER2", 2, "年間"); // 学期
        svf.VrsOut("DATE", "出欠集計日:"+KNJ_EditDate.h_format_JP(db2, _param._date)); //出欠集計日
        final List rowList = KnjDbUtils.query(db2, " SELECT * FROM RECORD_TOTALSTUDYTIME_ITEM_MST WHERE CLASSCD = '90' AND SCHOOL_KIND = 'H' ORDER BY SHOWORDER ");
		final Map itemnameRow = KnjDbUtils.getColumnValMap(rowList, "COLUMNNAME", "ITEMNAME");
        svf.VrsOut("SOGO1", KnjDbUtils.getString(itemnameRow, "TOTALSTUDYACT"));
        svf.VrsOut("SOGO2", KnjDbUtils.getString(itemnameRow, "TOTALSTUDYTIME"));
        final Map datasizeRow = KnjDbUtils.getColumnValMap(rowList, "COLUMNNAME", "DATA_SIZE");
        
        String sogakuSubclassname = null;
        final List printSubclassList = student.getPrintSubclassList(_param, 99);
        for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            final String classcdOnly = StringUtils.split(subClass._mst._classcd, "-")[0];
            if ("90".equals(classcdOnly)) {
            	sogakuSubclassname = subClass._mst._subclassname;
            }
        }
        final StringBuffer sogakuGakushunaiyouHyoka = new StringBuffer();
        final String[] text = {KnjDbUtils.getString(itemnameRow, "TOTALSTUDYACT"), KnjDbUtils.getString(itemnameRow, "TOTALSTUDYTIME")};
        for (int i = 0; i < text.length; i++) {
        	if (!StringUtils.isBlank(text[i])) {
        		sogakuGakushunaiyouHyoka.append(sogakuGakushunaiyouHyoka.length() == 0 ? "" : "・").append(text[i]);
        	}
        }
        if (null != sogakuSubclassname) {
        	sogakuGakushunaiyouHyoka.insert(0, sogakuSubclassname + "の");
        }
        svf.VrsOut("SOGAKU_TITLE", sogakuGakushunaiyouHyoka.toString()); // タイトル
               
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + _param._gradeName + "　成績通知表"); // タイトル
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)) + "番";
        svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + " " + attendno); // 年組番
        svf.VrsOut("NAME1", student._name); // 氏名

        svf.VrsOut("PRINCIPAL_JOB_NAME", _param._jobName); // 校長名
        svf.VrsOut("PRINCIPAL_NAME", _param._principalName); // 校長名
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("HR_JOB_NAME", _param._hrJobName); // 校長名
        svf.VrsOut("TEACHER_NAME", student._staffname); // 担任名

        if (_param._isLastSemester) {
            svf.VrsOut("CREDIT_TOTAL", student.getTotalGetCredit(_param)); // 修得単位数合計
        }

        final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._gakki);
        if (null != hReportRemarkDat) {
        	final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(KnjDbUtils.getString(datasizeRow, "TOTALSTUDYACT"), 40, 6);
        	List tokenList1 = KNJ_EditKinsoku.getTokenList(hReportRemarkDat._totalstudyact, size.getKeta());
        	if (size._gyo > 0 && tokenList1.size() > size._gyo) {
        		tokenList1 = tokenList1.subList(0, size._gyo);
        	}
            svfVrsOutnRepeat(svf, "TOTALSTUDYTIME1", tokenList1); // 総学観点
            
        	List tokenList2 = KNJ_EditKinsoku.getTokenList(hReportRemarkDat._totalstudytime, size.getKeta());
        	if (size._gyo > 0 && tokenList2.size() > size._gyo) {
        		tokenList2 = tokenList1.subList(0, size._gyo);
        	}
            svfVrsOutnRepeat(svf, "TOTALSTUDYTIME2", tokenList2); // 総学評価
        }

        printShukketsu(svf, student);
        printSeiseki(svf, student);

        _hasData = true;
    }

    private void printSeiseki(final Vrw32alp svf, final Student student) {

        final int maxRecord = 18 * 2;
        int count = 0;
        final List printSubclassList = student.getPrintSubclassList(_param, maxRecord);
        for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            if (_param._isOutputDebug) {
                log.info(" subclass = " + subClass);
            }
            final String subclasscd = subClass._mst._subclasscd;
            if (KNJ_EditEdit.getMS932ByteLength(subClass._mst._classabbv) <= 8) {
                svf.VrsOut("CLASSNAME", subClass._mst._classabbv); // 教科名
                svf.VrsOut("CLASSNAME2", subClass._mst._classabbv); // 教科名
                svf.VrAttribute("CLASSNAME2", "X=10000");
            } else {
                svf.VrsOut("CLASSNAME", subClass._mst._classabbv); // 教科名
                svf.VrsOut("CLASSNAME2", subClass._mst._classabbv); // 教科名
                svf.VrAttribute("CLASSNAME", "X=10000");
            }
            final int subclassnameketa = KNJ_EditEdit.getMS932ByteLength(subClass._mst._subclassname);
            final String[] token;
            final String[] subclassnamefield;
            if (subclassnameketa <= 8 * 2) {
                if (subclassnameketa == 10) {
                    token = KNJ_EditEdit.get_token(subClass._mst._subclassname, 6, 2);
                } else {
                    token = KNJ_EditEdit.get_token(subClass._mst._subclassname, 8, 2);
                }
                subclassnamefield = new String[] {"SUBCLASSNAME2", "SUBCLASSNAME3"};
            } else {
                token = KNJ_EditEdit.get_token(subClass._mst._subclassname, 8, 3);
                subclassnamefield = new String[] {"SUBCLASSNAME", "SUBCLASSNAME2", "SUBCLASSNAME3"};
            }
            for (int i = 0; i < Math.min(token.length, subclassnamefield.length); i++) {
                svf.VrsOut(subclassnamefield[i], token[i]);
            }
            svf.VrsOut("CREDIT", _param.getCredits(subclasscd, student._course)); // 単位

            final Score score1 = subClass.getScore(TESTCD_1GAKKI_HYOKA);
            if (null != score1) {
                svf.VrsOut("VAL1", score1._score); // 評定
            }
            final SubclassAttendance subattend1 = subClass.getAttendance(SEME1);
            if (null != subattend1) {
                svf.VrsOut("KEKKA1", getAbsentStr(_param, subattend1._sick, true)); // 欠課
            }

            if (_param._isLastSemester) {
                final Score score9 = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != score9) {
                    svf.VrsOut("VAL9", score9._score); // 評定
                }
                final SubclassAttendance subattend9 = subClass.getAttendance(SEMEALL);
                if (null != subattend9) {
                    svf.VrsOut("KEKKA9", getAbsentStr(_param, subattend9._sick, true)); // 欠課
                }
                if (null != score9) {
                    svf.VrsOut("GET_CREDIT", score9._getCredit); // 修得単位
                }
            }
            svf.VrEndRecord();

            count += 1;
        }

        for (int i = count; i < maxRecord; i++) {
            svf.VrsOut("CLASSNAME", String.valueOf(i)); // 教科名
            svf.VrsOut("CLASSNAME2", String.valueOf(i)); // 教科名
            svf.VrAttribute("CLASSNAME", "X=10000");
            svf.VrAttribute("CLASSNAME2", "X=10000");
            svf.VrEndRecord();
        }
    }

    private void printShukketsu(final Vrw32alp svf, final Student student) {
        final String[] seme = {SEME1, SEMEALL};
        for (int semei = 0; semei < seme.length; semei++) {
            final int line = semei + 1;
            if (!_param._isLastSemester && Integer.parseInt(seme[semei]) > Integer.parseInt(_param._gakki)) {
                continue;
            }

            final AttendSemesDat att = (AttendSemesDat) student._attendSemesDatMap.get(seme[semei]);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停・忌引
                svf.VrsOutn("MUST", line, String.valueOf(att._mlesson)); // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, String.valueOf(att._sick)); // 欠席日数
                svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
            }
        }
    }

    private void svfVrsOutnRepeat(final Vrw32alp svf, final String field, final List token) {
        for (int j = 0; j < token.size(); j++) {
            final int line = j + 1;
            //log.info(" VrsOutn(\"" + field + "\", " + line + ", " + token.get(j) + "); -> length = " + KNJ_EditEdit.getMS932ByteLength((String) token.get(j)));
            svf.VrsOutn(field, line, (String) token.get(j));
        }
    }

    private static String trimLeft(final String s) {
        if (null == s) {
            return null;
        }
        String rtn = s;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                rtn = s.substring(i);
                break;
            }
        }
        return rtn;
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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

    private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
        if (null == bd || notPrintZero && bd.doubleValue() == .0) {
            return null;
        }
        final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _course;
        final String _staffname;
        final Map _attendMap;
        final Map _subclassMap;
        Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠の記録
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 通知表所見

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String course, final String staffname) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _course = course;
            _staffname = staffname;
            _attendMap = new TreeMap();
            _subclassMap = new TreeMap();
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.info(" regd sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String course = rs.getString("COURSE");
                    final String staffname = rs.getString("STAFFNAME");
                    final Student student = new Student(schregno, name, hrName, attendno, course, staffname);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            SubclassAttendance.load(db2, param, studentList);
            HReportRemarkDat.setHReportRemarkDatMap(db2, param, studentList);

            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            final List testcds = new ArrayList(Arrays.asList(new String[] {TESTCD_1GAKKI_HYOKA}));
            if (param._isLastSemester) {
                testcds.add(TESTCD_GAKUNEN_HYOTEI);
            }
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = (String) testcds.get(i);
                if (null == testcd) {
                    continue;
                }
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                stbtestcd.append(testcdor);
                stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                testcdor = " OR ";
            }
            stbtestcd.append(") ");
            Score.load(db2, param, studentList, stbtestcd);

            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("            , V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._gakki +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
//            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
//            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
//            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
//            stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
//            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        T8.STAFFNAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("        LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }

        protected List getPrintSubclassList(final Param param, final int max) {
            final List printSubclassList = new ArrayList();
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final SubClass subClass = getSubClass(subclasscd);
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                printSubclassList.add(subClass);
                if (printSubclassList.size() >= max) {
                    break;
                }
            }
            Collections.sort(printSubclassList);
            return printSubclassList;
        }

        SubClass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubClass(new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false));
            }
            return (SubClass) _subclassMap.get(subclasscd);
        }

        public String getTotalGetCredit(final Param param) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final SubClass subClass = getSubClass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != score && NumberUtils.isDigits(score.getGetCredit())) {
                    final int iCredit = Integer.parseInt(score.getGetCredit());
                    if ("1".equals(score._provFlg)) {
                        totalGetCreditKari += iCredit;
                    } else {
                        totalGetCredit += iCredit;
                    }
                }
            }
            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
                return "(" + String.valueOf(totalGetCreditKari) + ")";
            }
            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._gakki,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendSemesDatMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        student._attendSemesDatMap.put(semester, attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    /**
     * 科目
     */
    private static class SubClass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;

        SubClass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd) {
                return null;
            }
            return (Score) _scoreMap.get(testcd);
        }

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            return (SubclassAttendance) _attendMap.get(key);
        }

        public int compareTo(final Object o) {
            final SubClass subclass = (SubClass) o;
            return _mst.compareTo(subclass._mst);
        }

        public String toString() {
            return "SubClass(" + _mst.toString() + ", score = " + _scoreMap + ")";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal sick) {
            _lesson = lesson;
            _sick = sick;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        null,
                        param._date,
                        param._attendParamMap
                );
                if (param._isOutputDebug) {
                    log.info(" attend subclass sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            // final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            // final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);

                            if (null == student._subclassMap.get(subclasscd)) {
                                final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            }
                            final SubClass subClass = student.getSubClass(subclasscd);
                            subClass._attendMap.put(semester, subclassAttendance);
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

    /**
     * 成績
     */
    private static class Score {
        final String _testcd;
        final String _score;
        final String _assessLevel;
        final String _avg;
        final String _karihyotei;
        final String _replacemoto;
        final String _compCredit;
        final String _getCredit;
        final String _provFlg;

        Score(
                final String testcd,
                final String score,
                final String assessLevel,
                final String avg,
                final String karihyotei,
                final String replacemoto,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _testcd = testcd;
            _score = score;
            _assessLevel = assessLevel;
            _avg = avg;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _provFlg = provFlg;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
                return false;
            }
            return true;
        }

//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map studentMap = new HashMap();
            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);
                studentMap.put(student._schregno, student);
            }

            try {
                final String sql = sqlScore(param, stbtestcd);
                if (param._isOutputDebug) {
                    log.info(" subclass sql = " + sql);
                }
                log.info(" subclass query start.");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Score score = new Score(
                            testcd,
                            rs.getString("SCORE"),
                            null, // rs.getString("ASSESS_LEVEL"),
                            rs.getString("AVG"),
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("PROV_FLG")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final SubClass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append("     AND W1.SEMESTER = '" + param._gakki + "' ");
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, ");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._gakki + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

//            stb.append("   , REL_COUNT AS (");
//            stb.append("   SELECT SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("     , COUNT(*) AS COUNT ");
//            stb.append("          FROM RELATIVEASSESS_MST ");
//            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
//            stb.append("   GROUP BY SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
//            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM RELATIVEASSESS_MST L3 ");
//            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("      ) ELSE ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM ASSESS_MST L3 ");
//            stb.append("       WHERE L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("      ) ");
//            stb.append("     END AS ASSESS_LEVEL ");
            stb.append("    ,W3.AVG ");
//            stb.append("    ,W3.GRADE_RANK ");
//            stb.append("    ,W3.GRADE_AVG_RANK ");
//            stb.append("    ,W3.CLASS_RANK ");
//            stb.append("    ,W3.CLASS_AVG_RANK ");
//            stb.append("    ,W3.COURSE_RANK ");
//            stb.append("    ,W3.COURSE_AVG_RANK ");
//            stb.append("    ,W3.MAJOR_RANK ");
//            stb.append("    ,W3.MAJOR_AVG_RANK ");
//            stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
//            stb.append("    ,T_AVG1.COUNT AS GRADE_COUNT ");
//            stb.append("    ,T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
//            stb.append("    ,T_AVG2.AVG AS HR_AVG ");
//            stb.append("    ,T_AVG2.COUNT AS HR_COUNT ");
//            stb.append("    ,T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
//            stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
//            stb.append("    ,T_AVG3.COUNT AS COURSE_COUNT ");
//            stb.append("    ,T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
//            stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
//            stb.append("    ,T_AVG4.COUNT AS MAJOR_COUNT ");
//            stb.append("    ,T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("        AND T_AVG1.AVG_DIV = '1' ");
//            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("        AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
//            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("        AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
//            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("        AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("     AND (W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("       OR W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.COMP_CREDIT ");
            stb.append("    ,W3.GET_CREDIT ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("            AND (SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("              OR SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append("     ) ");

//            //成績不振科目データの表
//            stb.append(",RECORD_SLUMP AS(");
//            stb.append("    SELECT  W3.SCHREGNO ");
//            stb.append("    ,W3.SEMESTER ");
//            stb.append("    ,W3.TESTKINDCD ");
//            stb.append("    ,W3.TESTITEMCD ");
//            stb.append("    ,W3.SCORE_DIV, ");
//            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T4.NAME1 END AS SLUMP_MARK_NAME1 ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
//            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM RELATIVEASSESS_MST L3 ");
//            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("          ) ELSE ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM ASSESS_MST L3 ");
//            stb.append("           WHERE L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("          ) ");
//            stb.append("         END ");
//            stb.append("    END AS SLUMP_SCORE_KANSAN ");
//            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
//            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
//            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
//            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
//            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
//            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("        LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
//            stb.append(stbtestcd.toString());
//            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            stb.append("            AND (W1.SIDOU_INPUT_INF = '1' AND W3.MARK IS NOT NULL ");
//            stb.append("              OR W1.SIDOU_INPUT_INF = '2' AND W3.SCORE IS NOT NULL ");
//            stb.append("                ) ");
//            stb.append("     ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
//            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T3.AVG ");
//            stb.append("        ,T3.GRADE_RANK ");
//            stb.append("        ,T3.GRADE_AVG_RANK ");
//            stb.append("        ,T3.CLASS_RANK ");
//            stb.append("        ,T3.CLASS_AVG_RANK ");
//            stb.append("        ,T3.COURSE_RANK ");
//            stb.append("        ,T3.COURSE_AVG_RANK ");
//            stb.append("        ,T3.MAJOR_RANK ");
//            stb.append("        ,T3.MAJOR_AVG_RANK ");
//            stb.append("        ,T3.GRADE_AVG ");
//            stb.append("        ,T3.GRADE_COUNT ");
//            stb.append("        ,T3.GRADE_HIGHSCORE ");
//            stb.append("        ,T3.HR_AVG ");
//            stb.append("        ,T3.HR_COUNT ");
//            stb.append("        ,T3.HR_HIGHSCORE ");
//            stb.append("        ,T3.COURSE_AVG ");
//            stb.append("        ,T3.COURSE_COUNT ");
//            stb.append("        ,T3.COURSE_HIGHSCORE ");
//            stb.append("        ,T3.MAJOR_AVG ");
//            stb.append("        ,T3.MAJOR_COUNT ");
//            stb.append("        ,T3.MAJOR_HIGHSCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
//            stb.append("        ,K1.SLUMP ");
//            stb.append("        ,K1.SLUMP_MARK ");
//            stb.append("        ,K1.SLUMP_MARK_NAME1 ");
//            stb.append("        ,K1.SLUMP_SCORE ");
//            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

//            //成績不振科目データの表
//            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }

        public String toString() {
            return "(score = " + _score + "" + (TESTCD_GAKUNEN_HYOTEI.equals(_testcd) ? (" [getCredit = " + _getCredit + ", compCredit = " + _compCredit + "]") : "") +")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semester, _semestername, sdate, edate);
        }
        public String toString() {
            return "Semester(" + _semester + ", " + _semestername + ")";
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

    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final int classShoworder3,
                final int subclassShoworder3,
                final boolean isSaki, final boolean isMoto) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = new Integer(classShoworder3);
            _subclassShoworder3 = new Integer(subclassShoworder3);
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
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
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudyact;
        final String _totalstudytime;

        public HReportRemarkDat(
                final String semester,
                final String totalstudyact,
                final String totalstudytime) {
            _semester = semester;
            _totalstudyact = totalstudyact;
            _totalstudytime = totalstudytime;
        }

        public static void setHReportRemarkDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hReportRemarkDatMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String totalstudyact = rs.getString("TOTALSTUDYACT");
                        final String totalstudytime = rs.getString("TOTALSTUDYTIME");

                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudyact, totalstudytime);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._gakki + "' ");
            stb.append("     AND T1.CLASSCD = '90' ");
            stb.append("     AND T1.SCHOOL_KIND = 'H' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY CURRICULUM_CD, SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68021 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _gakki;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _useCurriculumcd;

        final String _gradeStr;
        final String _gradeName;
        private boolean _isLastSemester;

        final Map _attendParamMap;
        private Map _subclassMst;
        private Map _creditMst;
        private boolean _isNoPrintMoto;

        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H;


        private String _semestername1;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;

        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private String _title;
        final boolean _isOutputDebug;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _gradeStr = String.valueOf(Integer.parseInt(_grade));
            _gradeName = "第" + StringUtils.defaultString(_gradeStr) + "学年";

            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H = request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H"); // 総合的な学習の時間
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _definecode = createDefineCode(db2);

            setSemester(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            loadNameMstD016(db2);
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD175B' AND NAME = '" + propName + "' "));
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int classShoworder3 = rs.getInt("CLASS_SHOWORDER3");
                    final int subclassShoworder3 = rs.getInt("SUBCLASS_SHOWORDER3");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), classShoworder3, subclassShoworder3, isSaki, isMoto);
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private String getCredits(final String subclasscd, final String course) {
            final String credit = (String) _creditMst.get(subclasscd + ":" + course);
            if (NumberUtils.isDigits(credit) && Integer.parseInt(credit) == 0) {
            	return "";
            }
			return credit;
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMst = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD,  ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE,  ";
                sql += " T1.CREDITS  ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE T1.YEAR = '" + _year + "' ";
                sql += "   AND T1.GRADE = '" + _grade + "' ";
                sql += "   AND T1.CREDITS IS NOT NULL";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _creditMst.put(rs.getString("SUBCLASSCD") + ":" + rs.getString("COURSE"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("HR_CLASS_NAME1")) {
                        rtn = rs.getString("HR_CLASS_NAME1");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == rtn) {
                try {
                    final String sql = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null == rtn && null != rs.getString("HR_CLASS")) {
                            rtn = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                        }
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private void setSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String lastSemester = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.SEMESTER ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if ("1".equals(semester)) {
                        _semestername1 = rs.getString("SEMESTERNAME");
                    }
                    if (!"9".equals(semester)) {
                        lastSemester = semester;
                    }
                }
                _isLastSemester = null != lastSemester && lastSemester.equals(_gakki);
                log.info(" isLastSemester = " + _isLastSemester + "(lastSemester = " + lastSemester + ")");
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private String getSemestername(final String semester) {
            final Semester s = getSemester(semester);
            if (null == s) {
                log.warn(" no semester : " + s);
                return null;
            }
            return s._semestername;
        }

        private Semester getSemester(final String semester) {
            return (Semester) _semesterMap.get(semester);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _title = KnjDbUtils.getString(row, "REMARK6");

            _schoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _jobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _principalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _hrJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
        }

    }
}

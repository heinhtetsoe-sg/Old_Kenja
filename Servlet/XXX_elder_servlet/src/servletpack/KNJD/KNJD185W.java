package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;

/**
 * 学校教育システム 賢者 [成績管理]  福井県通知票
 */
public class KNJD185W {

    private static final Log log = LogFactory.getLog(KNJD185W.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_HYOKA = "990008";
    private static final String TESTCD_GAKUNEN_HYOKA = "9" + TESTCD_HYOKA;
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private static final String PATTERN_A = "1";
    private static final String PATTERN_B = "2";
    private static final String PATTERN_C = "3";
    private static final String PATTERN_D = "4";
    private static final String PATTERN_E = "5";
    private static final String PATTERN_F = "6";

    private static final int COLUMN_ITEM_TOTAL = 1;
    private static final int COLUMN_ITEM_HR_AVG = 2;
    private static final int COLUMN_ITEM_HR_RANK = 3;
    private static final int COLUMN_ITEM_COURSE_AVG = 4;
    private static final int COLUMN_ITEM_COURSE_RANK = 5;
    private static final int COLUMN_ITEM_GRADE_AVG = 6;
    private static final int COLUMN_ITEM_GRADE_RANK = 7;
    private static final int COLUMN_ITEM_MAJOR_AVG = 8;
    private static final int COLUMN_ITEM_MAJOR_RANK = 9;

    private static final int LINE_ITEM_TANNI = 1;
    private static final int LINE_ITEM_SCORE = 2;
    private static final int LINE_ITEM_KEKKA = 3;
    
    private static String CLASSCD_90 = "90";
    private static String CLASSCD_HR = "94";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    public void outputPdf(
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final List<Student> studentList = Student.getStudentList(db2, param);

        load(param, db2, studentList);
        
        _hasData = param._form.print(svf, studentList);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T t : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(t);
        }
        return rtn;
    }
    
	private static <T> List<List<T>> getSaidanyouPageList(final List<T> list, int max) {
        final List<List<T>> rtn = new ArrayList();
        final int pageMax = list.size() / max + (list.size() % max == 0 ? 0 : 1);
        for (int pi = 0; pi < pageMax; pi++) {
        	final List<T> currentPage = new ArrayList();
        	for (int i = 0; i < max; i++) {
        		final int idx = i * pageMax + pi;
        		if (idx < list.size()) {
        			currentPage.add(list.get(idx));
        		}
        	}
        	rtn.add(currentPage);
        }
        return rtn;
	}

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static String getKekkaString(final int val) {
        return String.valueOf(val);
    }

    private static String getString(final Map<String, String> m, final String colname) {
        if (m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(colname)) {
            throw new RuntimeException("not found column \"" + colname + "\" in " + m);
        }
        return m.get(colname);
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List<Student> studentList
    ) {
        Student.loadPreviousCredits(db2, param, studentList);  // 前年度までの修得単位数取得

        final Form form = param._form;

        final Map courseStudentsMap = new HashMap();
        for (final Student student : studentList) {
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }

        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final List courseGroupStudentList = (List) courseStudentsMap.get(key);
            final String[] split = StringUtils.split(key, "-");

            final List recordMockOrderSdivDatList;
            if (PATTERN_E.equals(param._cond01_1TyohyoPattern)) {
            	recordMockOrderSdivDatList = new ArrayList();
            	recordMockOrderSdivDatList.add("1990008");
            	recordMockOrderSdivDatList.add("2990008");
            	recordMockOrderSdivDatList.add("9990008");
            	if (param._cond21_1_hyoteiHyojiE) {
                	recordMockOrderSdivDatList.add("9990009");
            	}
            } else {
                recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);
            }

            form.init(recordMockOrderSdivDatList);

            String testcdor = "";
            final List testcdList = new ArrayList();
            for (int i = 0; i < form._testcds.length; i++) {
                final String testcd = form._testcds[i];
                if (null == testcd) {
                    continue;
                }
                testcdList.add(testcd);
            }
            if (("9".equals(param._semester) || param._semester.equals(param._knjSchoolMst._semesterDiv))) {
                testcdList.add(TESTCD_GAKUNEN_HYOTEI);
            }
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            for (int i = 0; i < testcdList.size(); i++) {
                final String testcd = (String) testcdList.get(i);
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                
                stbtestcd.append(testcdor);
                stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                testcdor = " OR ";
            }
            stbtestcd.append(") ");
            Score.load(db2, param, courseGroupStudentList, stbtestcd);

            for (final DateRange range : form._attendRanges.values()) {
                Attendance.load(db2, param, courseGroupStudentList, range);
            }
            for (final DateRange range : form._attendSubclassRanges.values()) {
                SubclassAttendance.load(db2, param, courseGroupStudentList, range);
            }
        }

//        Student.setTotalStudy(db2, param, studentList);
        Student.setRemark(db2, param, studentList);
//        Student.setFootnote(db2, param, studentList);
//        Student.setClub(db2, param, studentList);
//        Student.setCommittee(db2, param, studentList);
//        Student.setQualifiedList(db2, param, studentList);
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static BigDecimal addNumber(final BigDecimal num1, final BigDecimal num2) {
    	if (null == num1) { return num2; }
    	if (null == num2) { return num1; }
    	return num1.add(num2); 
    }
    
    private static String addNumber(final String num1, final String num2) {
    	if (!NumberUtils.isNumber(num1)) { return num2; }
    	if (!NumberUtils.isNumber(num2)) { return num1; }
    	return new BigDecimal(num1).add(new BigDecimal(num2)).toString(); 
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _grade;
        String _gradeCd;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _coursecodename;
        String _attendno;
		String _attendnoZeroSuprpess;
        String _hrClassName1;
        final Map<String, Attendance> _attendMap = new TreeMap();
        final Map<String, Subclass> _subclassMap = new TreeMap();
        String _entyear;
        private int _previousCredits0;  // 前年度までの本校修得単位数
        private int _previousCredits1;  // 前年度までの前籍校修得単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数

        private String _totalstudytime; // ABF
        private Map<String, String> _semesterClub = new HashMap(); // ABF
        private Map<String, String> _semesterCommittee = new HashMap(); // ABF
        private String _communication; // ABCD
        private Map<String, String> _semesterAttendrecRemark = new HashMap(); // ABDE
        private String _remark; // CF
        private int _subclassPageIdx = 0;

        Student copy() {
        	final Student copy = new Student();
            copy._schregno = _schregno;
            copy._name = _name;
            copy._hrname = _hrname;
            copy._staffname = _staffname;
            copy._staffname2 = _staffname2;
            copy._grade = _grade;
            copy._gradeCd = _gradeCd;
            copy._coursecd = _coursecd;
            copy._majorcd = _majorcd;
            copy._course = _course;
            copy._majorname = _majorname;
            copy._coursecodename = _coursecodename;
            copy._attendno = _attendno;
            copy._attendnoZeroSuprpess = _attendnoZeroSuprpess;
            copy._hrClassName1 = _hrClassName1;
            copy._attendMap.putAll(_attendMap);
            copy._subclassMap.putAll(_subclassMap);
            copy._entyear = _entyear;
            copy._previousCredits0 = _previousCredits0;
            copy._previousCredits1 = _previousCredits1;
            copy._previousCredits = _previousCredits;
            copy._previousMirisyu = _previousMirisyu;
            
            copy._totalstudytime = _totalstudytime;
            copy._semesterClub = new HashMap(_semesterClub);
            copy._semesterCommittee = new HashMap(_semesterCommittee);
            copy._communication = _communication;
            copy._semesterAttendrecRemark = new HashMap(_semesterAttendrecRemark);
            copy._remark = _remark;
            copy._subclassPageIdx = _subclassPageIdx;
        	return copy;
        }

        Subclass getSubclass(final String subclasscd) {
        	if (null == _subclassMap.get(subclasscd)) {
        		return new Subclass(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        	}
            return _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,HDAT.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,STFM2.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,GDAT.GRADE_CD ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,CCM.COURSECODENAME ");
            stb.append("            ,HDAT.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEMEG ON SEMEG.YEAR = '" + param._year + "' AND SEMEG.SEMESTER = REGD.SEMESTER AND SEMEG.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND HDAT.GRADE = REGD.GRADE ");
            stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = HDAT.TR_CD2 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD ");
            stb.append("                  AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < SEMEG.SDATE) ");
            }
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List<Student> students = new ArrayList();

            try {
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final Student student = new Student();
                    student._schregno = KnjDbUtils.getString(row, "SCHREGNO");
                    student._name = KnjDbUtils.getString(row, "NAME");
                    student._hrname = KnjDbUtils.getString(row, "HR_NAME");
                    student._staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                    student._staffname2 = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME2"));
                    student._attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) + "番" : KnjDbUtils.getString(row, "ATTENDNO");
                    student._attendnoZeroSuprpess = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                    student._grade = KnjDbUtils.getString(row, "GRADE");
                    student._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                    student._coursecd = KnjDbUtils.getString(row, "COURSECD");
                    student._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                    student._course = KnjDbUtils.getString(row, "COURSE");
                    student._majorname = KnjDbUtils.getString(row, "MAJORNAME");
                    student._coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                    student._hrClassName1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    student._entyear = KnjDbUtils.getString(row, "ENT_YEAR");
                    students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return students;
        }

        // 前年度までの修得単位数計
        private static void loadPreviousCredits(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SCHOOLCD, SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            stb.append(" GROUP BY  T1.SCHOOLCD ");
            final String sql = stb.toString();

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    int i = 1;
                    ps.setString(i++, student._schregno);

                    student._previousCredits = 0;
                    student._previousCredits0 = 0;
                    student._previousCredits1 = 0;
                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        while (rs.next()) {
                        	if ("0".equals(rs.getString("SCHOOLCD"))) {
                                student._previousCredits0 += rs.getInt("CREDIT");
                        	} else {
                                student._previousCredits1 += rs.getInt("CREDIT");
                        	}
                        }
                        student._previousCredits = student._previousCredits0 + student._previousCredits1;
                    } catch (Exception e) {
                        log.error("Exception", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

//        // 仮評定があるか
//        public boolean hasKari(final Param param) {
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd) || subclasscd.endsWith("333333") || subclasscd.endsWith("555555") || subclasscd.endsWith("99999B")) {
//                    continue;
//                }
//                final Subclass subClass = getSubclass(subclasscd);
//                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                if (null != score && NumberUtils.isDigits(score._score) && "1".equals(score._provFlg)) {
//                    return true;
//                }
//            }
//            return false;
//        }

        public String getTotalGetCredit(final Param param, final boolean isAddPrevious, final boolean checkSemester) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            boolean hasValue = false;
            final String seme = param._isLastSemester ? SEMEALL : param._semester;
            for (final String subclasscd : _subclassMap.keySet()) {
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subClass = getSubclass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                final String cre = subClass.getGetCredit(subClass.creditEnabled(param, seme, checkSemester));
				if (NumberUtils.isDigits(cre)) {
                    final int iCredit = Integer.parseInt(cre);
//                    if ("1".equals(score._provFlg)) {
//                        totalGetCreditKari += iCredit;
//                        if (param._isOutputDebug) {
//                        	log.info(" " + subClass._mst.toString() + " = (" + iCredit + ")");
//                        }
//                    } else {
                        totalGetCredit += iCredit;
                        if (param._isOutputDebug) {
                        	log.info(" " + subClass._mst.toString() + " =  " + iCredit + "");
                        }
//                    }
                    hasValue = true;
                }
            }
//            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
//                return "(" + String.valueOf(totalGetCreditKari) + ")";
//            }
//            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
            int total = totalGetCredit + totalGetCreditKari;
            if (isAddPrevious && _previousCredits > 0) {
            	total += _previousCredits;
            	hasValue = true;
            }
            return hasValue ? String.valueOf(total) : null;
        }
        
        public String getGetCredit(final Param param, final String semester, final boolean checkSemester) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            boolean hasValue = false;
            final String seme = SEMEALL;
            for (final String subclasscd : _subclassMap.keySet()) {
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subclass = getSubclass(subclasscd);
                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
                    continue;
                }
                if ("1".equals(semester) && !subclass.isZenkiKamoku()) {
                	continue;
                } else if (!"1".equals(semester) && subclass.isZenkiKamoku()) {
                	continue;
                }
                final String cre = subclass.getGetCredit(subclass.creditEnabled(param, seme, checkSemester));
				if (NumberUtils.isDigits(cre)) {
                    final int iCredit = Integer.parseInt(cre);
//                    if ("1".equals(subclass._provFlg)) {
//                        totalGetCreditKari += iCredit;
//                        if (param._isOutputDebug) {
//                        	log.info(" " + subclass._mst.toString() + " = (" + iCredit + ")");
//                        }
//                    } else {
                        totalGetCredit += iCredit;
                        if (param._isOutputDebug) {
                        	log.info(" " + subclass._mst.toString() + " (" + semester + ")=  " + iCredit + "");
                        }
//                    }
                    hasValue = true;
                }
            }
//            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
//                return "(" + String.valueOf(totalGetCreditKari) + ")";
//            }
//            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
            int total = totalGetCredit + totalGetCreditKari;
            return hasValue ? String.valueOf(total) : null;
        }

//        public String getKettenSubclassCount(final Param param, final TestItem testItem) {
//            final List list = new ArrayList();
//            boolean hasNotNullSubclassScore = false;
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd)) {
//                    continue;
//                }
//                final SubClass subClass = getSubClass(subclasscd);
//                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subClass.getScore(testItem._testcd);
//                if (null != score) {
//                    if (score.isFail(param, testItem)) {
//                        list.add(subClass);
//                    }
//                    if (null != score._score) {
//                        hasNotNullSubclassScore = true;
//                    }
//                }
//            }
//            if (!hasNotNullSubclassScore) {
//                return null;
//            }
//            return String.valueOf(list.size());
//        }

        public static void setRemark(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps1 = null;
            try {
                final StringBuffer sql1 = new StringBuffer();
                sql1.append(" SELECT SEMESTER, SCHREGNO, TOTALSTUDYTIME, COMMUNICATION, ATTENDREC_REMARK, REMARK1 ");
                sql1.append(" FROM HREPORTREMARK_DAT ");
                sql1.append(" WHERE YEAR = '" + param._year + "' ");
                sql1.append("   AND SCHREGNO = ? ");

                ps1 = db2.prepareStatement(sql1.toString());

                for (final Student student : studentList) {

                    for (final Map row : KnjDbUtils.query(db2, ps1, new Object[] { student._schregno})) {

                    	final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    	student._semesterAttendrecRemark.put(semester, KnjDbUtils.getString(row, "ATTENDREC_REMARK"));
                    	if (SEMEALL.equals(semester)) {
                    		student._totalstudytime = KnjDbUtils.getString(row, "TOTALSTUDYTIME");
                    		student._remark = KnjDbUtils.getString(row, "REMARK1");
                    		student._communication = KnjDbUtils.getString(row, "COMMUNICATION");
                    	}
                    }
                    if (param._isOutputDebug) {
                    	log.info(" " + student._schregno + ": totalstudytime = " + student._totalstudytime + ", remark = " + student._remark + ", communication = " + student._communication + ", attendrecRemark = " + student._semesterAttendrecRemark);
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps1);
                db2.commit();
            }
            
            try {
                final StringBuffer sql1 = new StringBuffer();
                sql1.append(" SELECT SCHREGNO, SEMESTER, CODE, REMARK1 ");
                sql1.append(" FROM HREPORTREMARK_DETAIL_DAT ");
                sql1.append(" WHERE YEAR = '" + param._year + "' ");
                sql1.append("   AND DIV = '01' ");
                sql1.append("   AND CODE IN ('01', '02') ");
                sql1.append("   AND SCHREGNO = ? ");

                ps1 = db2.prepareStatement(sql1.toString());

                for (final Student student : studentList) {

                    for (final Map row : KnjDbUtils.query(db2, ps1, new Object[] { student._schregno})) {

                    	final String code = KnjDbUtils.getString(row, "CODE");
						if ("01".equals(code)) {
                            student._semesterClub.put(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "REMARK1"));
                    	} else if ("02".equals(code)) {
                            student._semesterCommittee.put(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "REMARK1"));
                    	}
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps1);
                db2.commit();
            }
        	
//            final StringBuffer sql1 = new StringBuffer();
//            sql1.append(" SELECT SEMESTER, REMARK1, REMARK2 ");
//            sql1.append(" FROM HREPORTREMARK_DETAIL_DAT ");
//            sql1.append(" WHERE YEAR = '" + param._year + "' ");
//            if (SEMEALL.equals(param._semester)) {
//                sql1.append("   AND SEMESTER = '" + SEMEALL + "' ");
//            } else {
//                sql1.append("   AND SEMESTER <= '" + param._semester + "' ");
//            }
//            sql1.append("   AND SCHREGNO = ? ");
//            sql1.append(" ORDER BY SEMESTER, CODE ");
//
//            PreparedStatement ps1 = null;
//            try {
//                ps1 = db2.prepareStatement(sql1.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//
//                    String oldsemester = null;
//                    StringBuffer stb = new StringBuffer();
//
//                    ps1.setString(1, student._schregno);
//                    ResultSet rs = ps1.executeQuery();
//                    while (rs.next()) {
//                        if (!"1".equals(rs.getString("REMARK2"))) {
//                            continue;
//                        }
//                        if (null == rs.getString("REMARK1")) {
//                            continue;
//                        }
//                        if (null != oldsemester && !rs.getString("SEMESTER").equals(oldsemester) && 0 != stb.length()) {
//                            stb.append("\n"); // 学期が変われば改行
//                        }
//                        stb.append(rs.getString("REMARK1"));
//
//                        oldsemester = rs.getString("SEMESTER");
//                    }
//                    DbUtils.closeQuietly(rs);
//
//                    student._hreportremarkDetailDatRemark1 = stb.toString();
//                }
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(ps1);
//                db2.commit();
//            }
//            final StringBuffer sql2 = new StringBuffer();
//            sql2.append(" SELECT REMARK1 ");
//            sql2.append(" FROM HREPORTREMARK_DAT ");
//            sql2.append(" WHERE YEAR = '" + param._year + "' ");
//            sql2.append("   AND SEMESTER = '" + SEMEALL + "' ");
//            sql2.append("   AND SCHREGNO = ? ");
//
//            PreparedStatement ps2 = null;
//            try {
//                ps2 = db2.prepareStatement(sql2.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//                    ps2.setString(1, student._schregno);
//                    ResultSet rs = ps2.executeQuery();
//                    while (rs.next()) {
//                        student._hreportremarkDat9Remark1 = rs.getString("REMARK1");
//                    }
//                    DbUtils.closeQuietly(rs);
//                }
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(ps2);
//                db2.commit();
//            }
//
//            for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                final Student student = it.next();
//                final String a = student._hreportremarkDetailDatRemark1;
//                final String b = student._hreportremarkDat9Remark1;
//                if (!StringUtils.isEmpty(a) && !StringUtils.isEmpty(b)) {
//                    student._remark = StringUtils.defaultString(a) + "\n" + StringUtils.defaultString(b);
//                } else {
//                    student._remark = StringUtils.defaultString(a) + StringUtils.defaultString(b);
//                }
//            }
        }

//        /**
//         * 総合的な学習の時間の所見をセットする
//         * @param db2
//         * @param param
//         * @param studentList
//         */
//        public static void setTotalStudy(final DB2UDB db2, final Param param, final List studentList) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT ");
//                stb.append("     T1.SEMESTER ");
//                stb.append("     ,T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
//                stb.append("     ,T1.TOTALSTUDYTIME ");
//                stb.append("     ,T1.TOTALSTUDYACT");
//                stb.append(" FROM ");
//                stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
//                stb.append(" WHERE ");
//                stb.append("     T1.YEAR = '" + param._year + "' ");
//                stb.append("     AND T1.SCHREGNO = ? ");
//                stb.append(" ORDER BY ");
//                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
//
//                final String sql = stb.toString();
//
//                log.debug(" total study sql = " + sql);
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student= it.next();
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//                    
//                    student._semesterTotalStudyactMap = new HashMap();
//                    student._semesterTotalStudytimeMap = new HashMap();
//                    while (rs.next()) {
//                        final String semester = rs.getString("SEMESTER");
//                        final String totalStudyact = StringUtils.defaultString((String) student._semesterTotalStudyactMap.get(semester));
//                        student._semesterTotalStudyactMap.put(semester, addLine(totalStudyact, rs.getString("TOTALSTUDYACT")));
//                        final String totalStudytime = StringUtils.defaultString((String) student._semesterTotalStudytimeMap.get(semester));
//                        student._semesterTotalStudytimeMap.put(semester, addLine(totalStudytime, rs.getString("TOTALSTUDYTIME")));
//                    }
//                }
//
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

//        /**
//         * 定型コメントをセット
//         * @param studentList
//         */
//        public static void setFootnote(final DB2UDB db2, final Param param, final List studentList) {
//
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.FOOTNOTE ");
//            stb.append(" FROM ");
//            stb.append("     RECORD_DOCUMENT_KIND_DAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' ");
//            stb.append("     AND T1.SEMESTER = '" + ("9".equals(param._semester) ? param._knjSchoolMst._semesterDiv : param._semester) + "' ");
//            stb.append("     AND T1.TESTKINDCD = '99' ");
//            stb.append("     AND T1.TESTITEMCD = '00' ");
//            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
//            stb.append("     AND T1.HR_CLASS = '000' ");
//            stb.append("     AND T1.COURSECD = '0' ");
//            stb.append("     AND T1.MAJORCD = '000' ");
//            stb.append("     AND T1.COURSECODE = '0000' ");
//            stb.append("     AND T1.CLASSCD = '00' ");
//            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
//            stb.append("     AND T1.CURRICULUM_CD = '00' ");
//            stb.append("     AND T1.SUBCLASSCD = '000000' ");
//            stb.append("     AND T1.KIND_DIV = '2' ");
//
//            final String sql = stb.toString();
//            //log.debug(" footnote sql = " + sql);
//
//            try {
//                final String footnote = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
//                
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student= it.next();
//
//                    student._footnote = footnote;
//                }
//
//            } catch (Exception e) {
//                log.error("exception!", e);
//            }
//        }

//        public static void setClub(final DB2UDB db2, final Param param, final List studentList) {
//            PreparedStatement ps = null;
//            final StringBuffer stb = new StringBuffer();
//            stb.append("  ");
//            stb.append(" SELECT DISTINCT ");
//            stb.append("     T1.SCHREGNO, ");
//            stb.append("     TSEM.SEMESTER, ");
//            stb.append("     T1.CLUBCD, ");
//            stb.append("     T2.CLUBNAME, ");
//            stb.append("     CASE WHEN T1.EDATE <= TSEM.EDATE THEN 1 END AS TAIBU_FLG, ");
//            stb.append("     CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
//            stb.append("                       T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) THEN 1 END AS FLG ");
//            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
//            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
//            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
//            stb.append("     AND TSEM.SEMESTER <> '9' ");
//            stb.append("     AND TSEM.SEMESTER <= '" + param.getRegdSemester() + "' ");
//            stb.append(" WHERE ");
//            stb.append("     T1.SCHREGNO = ? ");
//            stb.append(" ORDER BY ");
//            stb.append("     T1.SCHREGNO, ");
//            stb.append("     T1.CLUBCD ");
//            try {
//                ps = db2.prepareStatement(stb.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//
//                    ps.setString(1, student._schregno);
//
//                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
//                    	final Map row = (Map) rit.next();
//                        final String clubname = KnjDbUtils.getString(row, "CLUBNAME");
//                        final String flg = KnjDbUtils.getString(row, "FLG");
//                        final String taibuFlg = KnjDbUtils.getString(row, "TAIBU_FLG");
//
//                        if ("1".equals(taibuFlg) || !"1".equals(flg) || StringUtils.isBlank(clubname)) {
//                            continue;
//                        }
//                        if (!getMappedList(student._semesClubListMap, "9").contains(clubname)) {
//                            getMappedList(student._semesClubListMap, "9").add(clubname);
//                        }
//                    }
//
//                    final StringBuffer stbClub = new StringBuffer();
//                    String lf = "";
//                    for (final Iterator semeit = student._semesClubListMap.keySet().iterator(); semeit.hasNext();) {
//                        final String semester = (String) semeit.next();
//                        stbClub.append(lf).append(distinctConcat(getMappedList(student._semesClubListMap, semester), "、"));
//                        lf = "\n";
//                    }
//                    student._club = stbClub.toString();
//                }
//            } catch (Exception ex) {
//                log.fatal("exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

//        public static void setCommittee(final DB2UDB db2, final Param param, final List studentList) {
//            PreparedStatement ps = null;
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.SEMESTER, ");
//            stb.append("     VALUE(NMJ004.NAME1, '') AS SEMESTERNAME, ");
//            stb.append("     T1.SCHREGNO, ");
//            stb.append("     T1.COMMITTEE_FLG, ");
//            stb.append("     T1.COMMITTEECD, ");
//            stb.append("     T1.CHARGENAME, ");
//            stb.append("     T2.COMMITTEENAME ");
//            stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
//            stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
//            stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
//            stb.append(" LEFT JOIN NAME_MST NMJ004 ON NMJ004.NAMECD1 = 'J004' ");
//            stb.append("     AND NMJ004.NAMECD2 = T1.SEMESTER ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' ");
//            stb.append("     AND (T1.SEMESTER = '9' OR T1.SEMESTER BETWEEN '1' AND '" + param.getRegdSemester() + "') ");
//            stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
//            stb.append("     AND T1.SCHREGNO = ? ");
//            stb.append(" ORDER BY ");
//            stb.append("     T1.SCHREGNO, ");
//            stb.append("     T1.COMMITTEE_FLG, ");
//            stb.append("     T1.COMMITTEECD ");
//            try {
//                ps = db2.prepareStatement(stb.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//
//                    final Map semesternameMap = new HashMap();
//                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
//                    	
//                    	final Map row = (Map) rit.next();
//
//                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
////                        final String committeeFlg = KnjDbUtils.getString(row, "COMMITTEE_FLG");
//                        String name = null;
////                        if ("2".equals(committeeFlg)) {
////                            name = KnjDbUtils.getString(row, "CHARGENAME");
////                        } else if ("1".equals(committeeFlg)) {
//                            name = KnjDbUtils.getString(row, "COMMITTEENAME");
////                        }
//                        if (StringUtils.isBlank(name)) {
//                            continue;
//                        }
//                        semesternameMap.put(semester, KnjDbUtils.getString(row, "SEMESTERNAME"));
//                        getMappedList(student._semesCommitteeListMap, semester).add(name);
//                    }
//
//                    final StringBuffer stbCommittee = new StringBuffer();
//                    String lf = "";
//                    for (final Iterator semeit = student._semesCommitteeListMap.keySet().iterator(); semeit.hasNext();) {
//                        final String semester = (String) semeit.next();
//                        final String semestername = (String) semesternameMap.get(semester);
//                        stbCommittee.append(lf).append(semestername).append("：").append(distinctConcat(getMappedList(student._semesCommitteeListMap, semester), "、"));
//                        lf = "\n";
//                    }
//                    student._committee = stbCommittee.toString();
//                }
//            } catch (Exception ex) {
//                log.fatal("exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

//        private static String addLine(final String source, final String data) {
//            if (StringUtils.isBlank(source)) {
//                return data;
//            }
//            if (StringUtils.isBlank(data)) {
//                return source;
//            }
//            return source + "\n" + data;
//        }

//        private static void setQualifiedList(final DB2UDB db2, final Param param, final List studentList) {
//            PreparedStatement ps = null;
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" WITH DATA_MAX8 AS ( ");
//                stb.append(" SELECT ");
//                stb.append("    T2.QUALIFIED_NAME ");
//                stb.append("    , NMH312.NAME1 AS RANK_NAME ");
//                stb.append("    , T1.REGDDATE ");
//                stb.append("    , T1.SEQ ");
//                stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
//                stb.append(" INNER JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
//                stb.append(" LEFT JOIN NAME_MST NMH312 ON NMH312.NAMECD1 = 'H312' ");
//                stb.append("     AND NMH312.NAMECD2 = T1.RANK ");
//                stb.append(" WHERE ");
//                stb.append(" T1.YEAR = '" + param._year + "' ");
//                stb.append(" AND T1.SCHREGNO = ? ");
//                stb.append(" ORDER BY ");
//                stb.append("   T1.REGDDATE DESC ");
//                stb.append("   , T1.SEQ ");
//                stb.append(" FETCH FIRST 8 ROWS ONLY ");
//                stb.append(" ) ");
//                stb.append(" SELECT ");
//                stb.append("    QUALIFIED_NAME, ");
//                stb.append("    RANK_NAME, ");
//                stb.append("    REGDDATE, ");
//                stb.append("    SEQ ");
//                stb.append(" FROM ");
//                stb.append("    DATA_MAX8 ");
//                stb.append(" ORDER BY ");
//                stb.append("    REGDDATE, ");
//                stb.append("    SEQ ");
//
//                ps = db2.prepareStatement(stb.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//
//                    student._qualifiedList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno});
//                }
//
//            } catch (Exception ex) {
//                log.fatal("exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

        public String getRegdInfo(final Param param) {
            final String a;
            a = StringUtils.defaultString(_hrname) + " " + StringUtils.defaultString(_attendno);
            return StringUtils.defaultString(_majorname) + " " + a;
        }

        private static Student getStudent(final List<Student> studentList, final String code) {
            if (code == null) {
                return null;
            }
            for (final Student student : studentList) {
                if (code.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                
                ps = db2.prepareStatement(sql);
                
                final Integer zero = new Integer(0);

                for (final Student student : studentList) {

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        if (!SEMEALL.equals(getString(row, "SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                                KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                                KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                                KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                                KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                                KnjDbUtils.getInt(row, "EARLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue()
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
//            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, dateRange._sdate, edate);
//            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

//        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List studentList, final DateRange dateRange) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT T1.MONTH, T1.SEMESTER, T1.SCHREGNO, T1.REMARK1 ");
//                stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
//                stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//                stb.append(" WHERE ");
//                stb.append("   T1.COPYCD = '0' ");
//                stb.append("   AND T1.YEAR = '" + param._year + "' ");
//                stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
//                stb.append("   AND T1.SCHREGNO = ? ");
//                stb.append("   AND T1.REMARK1 IS NOT NULL ");
//                stb.append(" ORDER BY T1.MONTH, T1.SEMESTER ");
//
//                //log.debug(" dateRange = " + dateRange + " /  remark sql = " + stb.toString());
//                ps = db2.prepareStatement(stb.toString());
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = it.next();
//
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//
//                    String comma = "";
//                    final StringBuffer remark = new StringBuffer();
//                    while (rs.next()) {
//                        remark.append(comma).append(rs.getString("REMARK1"));
//                        comma = "、";
//                    }
//                    if (remark.length() != 0) {
//                        student._attendRemarkMap.put(dateRange._key, remark.toString());
//                    }
//
//                    DbUtils.closeQuietly(rs);
//                }
//
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable<Subclass> {
        final String _chaircd;
        final Map _semesterChaircdMap = new HashMap();
        final Map _semesterTakeSemesMap = new HashMap();
        final Map _semesterStaffnameMap = new TreeMap();
        final String _chairname;
        final SubclassMst _mst;
        final Map<String, Score> _scoreMap;
        final Map<String, SubclassAttendance> _attendMap;
        final String _credits;
        final String _minTakesemes;
        
        final String _compCredit;
        final String _getCredit;
        final String _provFlg;

        Subclass(
                final String chaircd,
                final String chaircdSeme1,
                final String chaircdSeme2,
                final String chaircdSeme3,
                final String chairname,
                final SubclassMst mst,
                final String credits,
                final String minTakesemes,
                final String takeSemesSeme1,
                final String takeSemesSeme2,
                final String takeSemesSeme3,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _chaircd = chaircd;
            _semesterChaircdMap.put("1", chaircdSeme1);
            _semesterChaircdMap.put("2", chaircdSeme2);
            _semesterChaircdMap.put("3", chaircdSeme3);
            _semesterTakeSemesMap.put("1", takeSemesSeme1);
            _semesterTakeSemesMap.put("2", takeSemesSeme2);
            _semesterTakeSemesMap.put("3", takeSemesSeme3);
            _chairname = chairname;
            _mst = mst;
            _credits = credits;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
            _minTakesemes = minTakesemes;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _provFlg = provFlg;
        }

        public boolean isZenkiKamoku() {
//            return null != _semesterChaircdMap.get("1") && null == _semesterChaircdMap.get("2");
//            return "1".equals(_minTakesemes);
        	return _mst._isZenki;
        }

        public boolean isKoukiKamoku() {
        	return _mst._isKouki;
        }
        
        public boolean isTsunen() {
        	return !isZenkiKamoku() && !isKoukiKamoku();
        }

        public Score getScore(final String testcd) {
            if (null == testcd) {
                return null;
            }
            return _scoreMap.get(testcd);
        }
        
        public boolean creditEnabled(final Param param, final String semester, final boolean isEnabled) {
        	//return (param._isPrintCreditSemester2OnlyInSemester2 && !isZenkiKamoku()) || (!param._isPrintCreditSemester2OnlyInSemester2 && (SEMEALL.equals(semester) || isZenkiKamoku() || isEnabled));
        	return SEMEALL.equals(semester) || isZenkiKamoku() || isEnabled;
        }
        
        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit(final boolean creditEnabled) {
			if (creditEnabled) {
				return !_mst._isMoto ? _compCredit : null;
			}
			return null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit(final boolean creditEnabled) {
			if (creditEnabled) {
				return !_mst._isMoto ? _getCredit : null;
			}
			return null;
        }

		public String getNinteiString(final String semester, final boolean checkLastSemester) {
        	final Score hyotei = getScore(TESTCD_GAKUNEN_HYOTEI);
			String nintei = "";
			if (SEMEALL.equals(semester) || checkLastSemester || isZenkiKamoku()) {
				if (null == hyotei) {
					if (NumberUtils.isDigits(_mst._classcd) && Integer.parseInt(_mst._classcd) <= 89) {
						nintei = "未";
					}
				} else if (!NumberUtils.isDigits(hyotei._score)) {
					if (NumberUtils.isDigits(_getCredit) && Integer.parseInt(_getCredit) > 0) {
						nintei = "認";
					}
				} else if (NumberUtils.isDigits(hyotei._score) && Integer.parseInt(hyotei._score) == 1) {
					nintei = "否";
				} else if (NumberUtils.isDigits(hyotei._score) && Integer.parseInt(hyotei._score) > 1) {
					nintei = "認";
				}
			}
			return nintei;
		}
		
		private List<String> getStaffnameList(final Param param) {
			final List<String> staffnameList = new ArrayList();
			final Set printed = new HashSet();
			for (final Iterator it = _semesterStaffnameMap.keySet().iterator(); it.hasNext();) {
				final String semester = (String) it.next();
				List semeStaffnameList = getMappedList(_semesterStaffnameMap, semester);
				for (int sti = 0; sti < semeStaffnameList.size(); sti++) {
					final String staffname = (String) semeStaffnameList.get(sti);
					if (null != staffname) {
						if (printed.contains(staffname)) {
							continue;
						}
						printed.add(staffname);
						String cut = staffname;
						if (cut.indexOf('　') >= 0) {
							cut = cut.substring(0, cut.indexOf('　'));
						} else if (cut.indexOf(' ') >= 0) {
							cut = cut.substring(0, cut.indexOf(' '));
						}
						staffnameList.add(cut);
					}
				}
			}
			if (param._isOutputDebug) {
				log.info(" subclass " + _mst._subclasscd + ", staffnameList = " + staffnameList);
			}
			return staffnameList;
		}

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            SubclassAttendance subclassAttendance = _attendMap.get(key);
            if (null == subclassAttendance) {
            	log.info(" null at " + key + " in " + _attendMap);
            }
			return subclassAttendance;
        }

        public int compareTo(final Subclass os) {
            int rtn;
            if (null == _mst) {
                return 1;
            }
            if (null == os._mst) {
                return -1;
            }
            rtn = _mst.compareTo(os._mst);
            return rtn;
        }

        public String toString() {
            return "Subclass { chaircd: " + _chaircd + ", chairname: " + _chairname + ", mst = " + _mst + ", nintei = " + getNinteiString(SEMEALL, false) + "} ";
        }
        
        public static List<Subclass> getPrintSubclassList(final Param param, final Student student) {
        	final Subclass _999999 = student.getSubclass(SUBCLASSCD999999);
			final List<Subclass> subclassList = new ArrayList(student._subclassMap.values());
			final List<String> notPrintZenkiSubclass = new ArrayList<String>();
            for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = it.next();
                if (param._d026List.contains(subclass._mst._subclasscd)) {
                    if (param._isOutputDebug) {
                        log.info(" not print subclass = " + subclass._mst._subclasscd);
                    }
                    it.remove();
                } else if (param._isNoPrintMoto && subclass._mst._isMoto) {
                    if (param._isOutputDebug) {
                        log.info(" not print moto subclass = " + subclass._mst._subclasscd);
                    }
                    it.remove();
                } else if ("1".equals(param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && !"1".equals(param._semester) && subclass._mst._isZenki) {
                	notPrintZenkiSubclass.add(subclass._mst._subclasscd);
                    it.remove();
                } else if (_999999 == subclass) {
                    it.remove();
                }
            }
            if (param._isOutputDebug && notPrintZenkiSubclass.size() > 0) {
                log.info(" not print zenki subclass = " + notPrintZenkiSubclass);
            }
            Collections.sort(subclassList);
			return subclassList;
		}
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
        }


        public static SubclassAttendance add(final SubclassAttendance att1, final SubclassAttendance att2) {
        	if (null == att2) { return att1; }
        	if (null == att1) { return att2; }
        	return new SubclassAttendance(
        			addNumber(att1._lesson, att2._lesson)
        		  , addNumber(att1._attend, att2._attend)
        		  , addNumber(att1._sick, att2._sick)
        		  );
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                final BigDecimal zero = new BigDecimal(0);
                for (final Student student : studentList) {

//                    final Map specialGroupKekkaMinutes = new HashMap();
                    
                    final Set logged = new HashSet();

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final SubclassMst mst = param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T) || iclasscd == Integer.parseInt(CLASSCD_HR)) || "1".equals(param._tutihyoPrintSubclasscd90Over)) {
                            if (null == student._subclassMap.get(subclasscd)) {
                            	final String message = " null chair subclass = " + subclasscd;
                            	if (logged.contains(message)) {
                            		log.info(message);
                            		logged.add(message);
                            	}
                                continue;
                            }

                            final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", zero);
                            final BigDecimal rawSick = KnjDbUtils.getBigDecimal(row, "SICK1", zero);
                            final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", zero);
                            final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(row, "RAW_REPLACED_SICK", zero);
                            final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", zero);

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2);

                            final Subclass subClass = student.getSubclass(subclasscd);
//                            log.debug(" schregno = " + student._schregno + ", sa = " + subclassAttendance);
                            subClass._attendMap.put(dateRange._key, subclassAttendance);
                        }

//                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
//                        if (null != specialGroupCd) {
//                            // 特別活動科目の処理 (授業分数と結果数の加算)
//                            final String subclassCd = rs.getString("SUBCLASSCD");
//                            final String kekkaMinutes = rs.getString("SPECIAL_SICK_MINUTES1");
//
//                            getMappedMap(specialGroupKekkaMinutes, specialGroupCd).put(subclassCd, kekkaMinutes);
//                        }
                    }

                    // 特別活動出欠
//                    for (final Iterator spit = specialGroupKekkaMinutes.entrySet().iterator(); spit.hasNext();) {
//                        final Map.Entry e = (Map.Entry) spit.next();
//                        final String specialGroupCd = (String) e.getKey();
//                        final Map subclassKekkaMinutesMap = (Map) e.getValue();
//
//                        int totalMinutes = 0;
//                        for (final Iterator subit = subclassKekkaMinutesMap.entrySet().iterator(); subit.hasNext();) {
//                            final Map.Entry subMinutes = (Map.Entry) subit.next();
//                            final String minutes = (String) subMinutes.getValue();
//                            if (NumberUtils.isDigits(minutes)) {
//                                totalMinutes += Integer.parseInt(minutes);
//                            }
//                        }
//
//                        final BigDecimal spGroupKekkaJisu = getSpecialAttendExe(totalMinutes, param);
//
//                        if (null == student._attendMap.get(dateRange._key)) {
//                            student._attendMap.put(dateRange._key, new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0));
//                        }
//                        final Attendance attendance = student._attendMap.get(dateRange._key);
//
//                        if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
//                            attendance._lhrKekka = spGroupKekkaJisu;
//                        } else if (Attendance.GROUP_EVENT.equals(specialGroupCd)) {
//                            attendance._gyojiKekka = spGroupKekkaJisu;
//                        } else if (Attendance.GROUP_COMMITTEE.equals(specialGroupCd)) {
//                            attendance._iinkaiKekka = spGroupKekkaJisu;
//                        }
//                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

//        /**
//         * 欠課時分を欠課時数に換算した値を得る
//         * @param kekka 欠課時分
//         * @return 欠課時分を欠課時数に換算した値
//         */
//        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
//            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
//            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
//            int hasu = 0;
//            final String retSt = bigD.toString();
//            final int retIndex = retSt.indexOf(".");
//            if (retIndex > 0) {
//                hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
//            }
//            final BigDecimal rtn;
//            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
//                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
//            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
//                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
//            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
//                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
//            } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
//                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
//            } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
//                rtn = bigD;
//            } else {
//                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
//            }
//            return rtn;
//        }
    }

    /**
     * 成績
     */
    private static class Score {
    	final SubclassMst _subclassMst;
        final String _score;
        final String _assessmark;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _slump;
        final String _slumpMark;
        final String _slumpMarkName1;
        final String _slumpScore;
        final String _slumpScoreKansan;

        Score(
        		final SubclassMst subclassMst,
                final String score,
                final String assessmark,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String slump,
                final String slumpMark,
                final String slumpMarkName1,
                final String slumpScore,
                final String slumpScoreKansan
        ) {
        	_subclassMst = subclassMst;
            _score = score;
            _assessmark = assessmark;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _karihyotei = karihyotei;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpScoreKansan = slumpScoreKansan;
            _slumpMark = slumpMark;
            _slumpMarkName1 = slumpMarkName1;
        }
        
		private Rank getRank(final int rankDiv) {
			final Rank nullRank = new Rank(null, null, null, null, null);
			Rank rank = nullRank;
			switch (rankDiv) {
			case COLUMN_ITEM_HR_RANK:
				rank = _hrRank;
				break;
			case COLUMN_ITEM_COURSE_RANK:
				rank = _courseRank;
				break;
			case COLUMN_ITEM_GRADE_RANK:
				rank = _gradeRank;
				break;
			case COLUMN_ITEM_MAJOR_RANK:
				rank = _majorRank;
				break;
			default:
				if (-1 != rankDiv) {
					log.warn("not found : " + rankDiv);
				}
			}
			return rank;
		}


//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

//        private String getPrintSlump(final TestItem testItem) {
//            if (null != testItem._sidouinput) {
//                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
//                    return _slumpMark;
//                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
//                    return _slumpScore;
//                }
//            }
//            return null;
//        }

//        private boolean isFail(final Param param, final TestItem testItem) {
//            if (null != testItem._sidouinput) {
//                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
//                    if (null != _slumpMark) {
//                        if (null != param._d054Namecd2Max && param._d054Namecd2Max.equals(_slumpMark)) {
//                            return true;
//                        }
//                        return false;
//                    }
//                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
//                    if (null != _slumpScoreKansan) {
//                        return "1".equals(_slumpScoreKansan);
//                    }
//                }
//            }
//            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
//                return "1".equals(_score);
//            }
//            return "1".equals(_assessLevel);
//        }


        private String getPrintScore(final TestItem testItem) {
        	String rtn = null;
            if (null != testItem && null != testItem._sidouinput) {
                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
                    rtn = _slumpMarkName1;
                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点 
                    rtn = _slumpScore;
                }
                if (null != rtn) {
                	return rtn;
                }
            }
            return _score;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            try {
                final String sql = sqlScore(param, stbtestcd);
                if (param._isOutputDebugQuery) {
                	log.info(" record sql = " + sql);
                	log.info(" subclass query start. ");
                }
                final List rowList = KnjDbUtils.query(db2, sql);
                if (param._isOutputDebugQuery) {
                	log.info(" subclass query end.");
                }

                for (final Iterator it = rowList.iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final Student student = Student.getStudent(studentList, KnjDbUtils.getString(row, "SCHREGNO"));
                    final String testcd = KnjDbUtils.getString(row, "TESTCD");
                    if (null == student) {
                        continue;
                    }

                	boolean notTarget = false;
                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final SubclassMst subclassMst = param.getSubclassMst(subclasscd);
                        
                        if (!"1".equals(param._tutihyoPrintSubclasscd90Over)) {
                        	final String classcd = StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-")[0];
							final int iclasscd = Integer.parseInt(classcd);
                        	if (iclasscd <= 90 || (PATTERN_A.equals(param._cond01_1TyohyoPattern) || PATTERN_B.equals(param._cond01_1TyohyoPattern)) && CLASSCD_HR.equals(classcd)) {
                        		// 90以下もしくはA、BパターンのHR
                        	} else if (null != subclassMst && subclassMst._isJirtsuKatudou) {
                        		if (param._isOutputDebug) {
                        			log.info(" 自立活動 " + student._schregno + " : " + subclasscd);
                        		}
                    		} else {
                    			notTarget = true;
                    		}
                        }
                    }
                    if (notTarget) {
                    	continue;
                    }
                    
                    if (null == student._subclassMap.get(subclasscd)) {
                        SubclassMst subclassMst = param.getSubclassMst(subclasscd);
                        if (null == subclassMst) {
                        	if (!SUBCLASSCD999999.equals(subclasscd)) {
                        		log.info(" no subclass : " + subclasscd);
                        		continue;
                        	}
                            subclassMst = new SubclassMst(param, "", "", subclasscd, "", "", "", "", new Integer(999999), new Integer(999999), new Integer(999999));
                        }
                        final Subclass subClass = new Subclass(KnjDbUtils.getString(row, "CHAIRCD"), KnjDbUtils.getString(row, "CHAIRCD1"), KnjDbUtils.getString(row, "CHAIRCD2"), KnjDbUtils.getString(row, "CHAIRCD3"), KnjDbUtils.getString(row, "CHAIRNAME"), subclassMst, KnjDbUtils.getString(row, "CREDITS"), KnjDbUtils.getString(row, "MIN_TAKESEMES"), KnjDbUtils.getString(row, "TAKESEMES1"), KnjDbUtils.getString(row, "TAKESEMES2"), KnjDbUtils.getString(row, "TAKESEMES3"), KnjDbUtils.getString(row, "COMP_CREDIT"), KnjDbUtils.getString(row, "GET_CREDIT"), KnjDbUtils.getString(row, "PROV_FLG"));
                        
                        getMappedList(subClass._semesterStaffnameMap, "1").add(KnjDbUtils.getString(row, "STAFFNAME1_1"));
                        getMappedList(subClass._semesterStaffnameMap, "1").add(KnjDbUtils.getString(row, "STAFFNAME1_2"));
                        getMappedList(subClass._semesterStaffnameMap, "2").add(KnjDbUtils.getString(row, "STAFFNAME2_1"));
                        getMappedList(subClass._semesterStaffnameMap, "2").add(KnjDbUtils.getString(row, "STAFFNAME2_2"));
                        getMappedList(subClass._semesterStaffnameMap, "3").add(KnjDbUtils.getString(row, "STAFFNAME3_1"));
                        getMappedList(subClass._semesterStaffnameMap, "3").add(KnjDbUtils.getString(row, "STAFFNAME3_2"));
                        
                        student._subclassMap.put(subclasscd, subClass);
                    }

                    final Rank gradeRank = new Rank(KnjDbUtils.getString(row, "GRADE_RANK"), KnjDbUtils.getString(row, "GRADE_AVG_RANK"), KnjDbUtils.getString(row, "GRADE_COUNT"), KnjDbUtils.getString(row, "GRADE_AVG"), KnjDbUtils.getString(row, "GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(KnjDbUtils.getString(row, "CLASS_RANK"), KnjDbUtils.getString(row, "CLASS_AVG_RANK"), KnjDbUtils.getString(row, "HR_COUNT"), KnjDbUtils.getString(row, "HR_AVG"), KnjDbUtils.getString(row, "HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(KnjDbUtils.getString(row, "COURSE_RANK"), KnjDbUtils.getString(row, "COURSE_AVG_RANK"), KnjDbUtils.getString(row, "COURSE_COUNT"), KnjDbUtils.getString(row, "COURSE_AVG"), KnjDbUtils.getString(row, "COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(KnjDbUtils.getString(row, "MAJOR_RANK"), KnjDbUtils.getString(row, "MAJOR_AVG_RANK"), KnjDbUtils.getString(row, "MAJOR_COUNT"), KnjDbUtils.getString(row, "MAJOR_AVG"), KnjDbUtils.getString(row, "MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                    		(student._subclassMap.get(subclasscd))._mst,
                            KnjDbUtils.getString(row, "SCORE"),
                            KnjDbUtils.getString(row, "ASSESSMARK"),
                            KnjDbUtils.getString(row, "AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // KnjDbUtils.getString(row, "KARI_HYOUTEI"),
                            KnjDbUtils.getString(row, "SLUMP"),
                            KnjDbUtils.getString(row, "SLUMP_MARK"),
                            KnjDbUtils.getString(row, "SLUMP_MARK_NAME1"),
                            KnjDbUtils.getString(row, "SLUMP_SCORE"),
                            KnjDbUtils.getString(row, "SLUMP_SCORE_KANSAN")
                    );

                    if (null == testcd) {
                    	if (null != KnjDbUtils.getString(row, "GET_CREDIT") && null != KnjDbUtils.getString(row, "ZOUKA")) {
                            final Subclass subClass = student.getSubclass(subclasscd);
                            subClass._scoreMap.put(TESTCD_GAKUNEN_HYOTEI, score);
                    	}
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subClass = student.getSubclass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
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
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIRS AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, W1.SEMESTER, ");
            stb.append("        W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append("        W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        W2.CHAIRCD, W2.CHAIRNAME, W2.TAKESEMES ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SCHNO_A T1 ON T1.SCHREGNO = W1.SCHREGNO ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     )");

            stb.append(",CHAIR_STAFF AS(");
            stb.append("     SELECT ");
            stb.append("        T1.SEMESTER, T1.CHAIRCD, ROW_NUMBER() OVER(PARTITION BY T1.SEMESTER, T1.CHAIRCD ORDER BY (CASE WHEN CHARGEDIV = 0 THEN 2 ELSE 1 END), T1.STAFFCD) AS PRINTORDER, T1.CHARGEDIV, T1.STAFFCD ");
            stb.append("     FROM   CHAIR_STF_DAT T1 ");
            stb.append("     WHERE  T1.YEAR = '" + param._year + "' ");
            stb.append("     )");

            stb.append(",CHAIR_A_SEME AS(");
            stb.append("     SELECT ");
            stb.append("       W1.SCHREGNO, ");
            stb.append("       W1.SUBCLASSCD, W2.SEMESTER, W2.CHAIRCD, W3.TAKESEMES, STF1.STAFFCD AS STAFFCD1, STF2.STAFFCD AS STAFFCD2 ");
            stb.append("     FROM   (SELECT DISTINCT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM CHAIRS) W1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, SEMESTER, SUBCLASSCD, MAX(CHAIRCD) AS CHAIRCD ");
            stb.append("                 FROM CHAIRS ");
            stb.append("                 GROUP BY SCHREGNO, SEMESTER, SUBCLASSCD ");
            stb.append("                ) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("     INNER JOIN CHAIRS W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("         AND W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("         AND W3.CHAIRCD = W2.CHAIRCD ");
            stb.append("         AND W3.SEMESTER = W2.SEMESTER ");
            stb.append("     INNER JOIN SCHNO_A W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN CHAIR_STAFF STF1 ON STF1.SEMESTER = W2.SEMESTER AND STF1.CHAIRCD = W2.CHAIRCD AND STF1.PRINTORDER = 1 ");
            stb.append("     LEFT JOIN CHAIR_STAFF STF2 ON STF2.SEMESTER = W2.SEMESTER AND STF2.CHAIRCD = W2.CHAIRCD AND STF2.PRINTORDER = 2 ");
            stb.append("     )");

            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT ");
            stb.append("       W1.SCHREGNO, ");
            stb.append("       W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, W1.SUBCLASSCD, W2.CHAIRCD, W3.CHAIRNAME, TCRE.CREDITS, W2.MIN_TAKESEMES ");
            stb.append("     FROM   (SELECT DISTINCT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM CHAIRS) W1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, SUBCLASSCD, MAX(CHAIRCD) AS CHAIRCD, MIN(TAKESEMES) AS MIN_TAKESEMES ");
            stb.append("                 FROM CHAIRS ");
            stb.append("                 GROUP BY SCHREGNO, SUBCLASSCD ");
            stb.append("                ) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("     INNER JOIN CHAIRS W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("         AND W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("         AND W3.CHAIRCD = W2.CHAIRCD ");
            stb.append("     INNER JOIN SCHNO_A W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST TCRE ON W4.YEAR = TCRE.YEAR ");
            stb.append("        AND W4.COURSECD = TCRE.COURSECD ");
            stb.append("        AND W4.GRADE = TCRE.GRADE ");
            stb.append("        AND W4.MAJORCD = TCRE.MAJORCD ");
            stb.append("        AND W4.COURSECODE = TCRE.COURSECODE ");
            stb.append("        AND W1.SUBCLASSCD = TCRE.CLASSCD || '-' || TCRE.SCHOOL_KIND || '-' || TCRE.CURRICULUM_CD || '-' || TCRE.SUBCLASSCD ");
            stb.append("     )");

            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CH1.CHAIRCD ");
            stb.append("    ,CH1.CHAIRNAME ");
            stb.append("    ,CH1.CREDITS ");
            stb.append("    ,CH1.MIN_TAKESEMES ");
        	stb.append("    ,W3.SCORE ");
        	stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
        	stb.append("      (SELECT L3.ASSESSMARK FROM RELATIVEASSESS_MST L3 ");
        	stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' AND L3.ASSESSLEVEL = W3.SCORE ");
        	stb.append("           AND L3.CLASSCD = W3.CLASSCD ");
        	stb.append("           AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
        	stb.append("           AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
        	stb.append("           AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
        	stb.append("      ) ELSE ");
        	stb.append("      (SELECT L4.ASSESSMARK FROM ASSESS_MST L4 WHERE L4.ASSESSCD = '3' AND L4.ASSESSLEVEL = W3.SCORE) ");
        	stb.append("     END AS ASSESSMARK ");
        	stb.append("    ,W3.AVG ");
        	stb.append("    ,W3.GRADE_RANK ");
        	stb.append("    ,W3.GRADE_AVG_RANK ");
        	stb.append("    ,W3.CLASS_RANK ");
        	stb.append("    ,W3.CLASS_AVG_RANK ");
        	stb.append("    ,W3.COURSE_RANK ");
        	stb.append("    ,W3.COURSE_AVG_RANK ");
        	stb.append("    ,W3.MAJOR_RANK ");
        	stb.append("    ,W3.MAJOR_AVG_RANK ");
        	stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
        	stb.append("    ,T_AVG1.COUNT AS GRADE_COUNT ");
        	stb.append("    ,T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
        	stb.append("    ,T_AVG2.AVG AS HR_AVG ");
        	stb.append("    ,T_AVG2.COUNT AS HR_COUNT ");
        	stb.append("    ,T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
        	stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
        	stb.append("    ,T_AVG3.COUNT AS COURSE_COUNT ");
        	stb.append("    ,T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
        	stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
        	stb.append("    ,T_AVG4.COUNT AS MAJOR_COUNT ");
        	stb.append("    ,T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG1.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
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
            stb.append("    ,CH1.CHAIRCD ");
            stb.append("    ,CH1.CHAIRNAME ");
            stb.append("    ,CH1.CREDITS ");
            stb.append("    ,CH1.MIN_TAKESEMES ");
            stb.append("    ,W3.COMP_CREDIT ");
            stb.append("    ,W3.GET_CREDIT ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A T1 ON T1.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("        AND W3.SEMESTER = '9' AND W3.TESTKINDCD = '99' AND W3.TESTITEMCD = '00' AND W3.SCORE_DIV = '09' ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T4.NAME1 END AS SLUMP_MARK_NAME1 ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END ");
            stb.append("    END AS SLUMP_SCORE_KANSAN ");
            stb.append("    ,CH1.CHAIRCD ");
            stb.append("    ,CH1.CHAIRNAME ");
            stb.append("    ,CH1.CREDITS ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A T1 ON T1.SCHREGNO = W3.SCHREGNO ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("        AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("        AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("        AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("        AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND (W3.SCORE IS NOT NULL OR W3.MARK IS NOT NULL) ");
            stb.append("     ) ");
            stb.append(", QUALIFIED AS(");
            stb.append("   SELECT ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       VALUE(T2.SUBCLASSORDERNAME2, T2.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("   FROM ");
            stb.append("       SCHREG_QUALIFIED_DAT T1 ");
            stb.append("       LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.CREDITS IS NOT NULL ");
            stb.append("       AND EXISTS (SELECT 'X' FROM SCHNO_A WHERE SCHREGNO = T1.SCHREGNO) ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD, ");
            stb.append("       VALUE(T2.SUBCLASSORDERNAME2, T2.SUBCLASSNAME) ");
            stb.append(" )");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, CAST(NULL AS VARCHAR(1)) AS MIN_TAKESEMES FROM RECORD_SLUMP ");
            if (SEMEALL.equals(param._semester) && param._cond22_1_addZoukaTanni) {
                stb.append("    UNION ");
                stb.append("    SELECT SCHREGNO, SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CHAIRCD, SUBCLASSNAME AS CHAIRNAME, CREDITS, CAST(NULL AS VARCHAR(1)) AS MIN_TAKESEMES FROM QUALIFIED ");
            } 
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T1.CHAIRCD ");
            stb.append("        ,T1.MIN_TAKESEMES ");
            stb.append("        ,CHSEME1.CHAIRCD AS CHAIRCD1 ");
            stb.append("        ,CHSEME2.CHAIRCD AS CHAIRCD2 ");
            stb.append("        ,CHSEME3.CHAIRCD AS CHAIRCD3 ");
            stb.append("        ,CHSEME1.TAKESEMES AS TAKESEMES1 ");
            stb.append("        ,CHSEME2.TAKESEMES AS TAKESEMES2 ");
            stb.append("        ,CHSEME3.TAKESEMES AS TAKESEMES3 ");
            stb.append("        ,CHSTF11.STAFFNAME AS STAFFNAME1_1 ");
            stb.append("        ,CHSTF12.STAFFNAME AS STAFFNAME1_2 ");
            stb.append("        ,CHSTF21.STAFFNAME AS STAFFNAME2_1 ");
            stb.append("        ,CHSTF22.STAFFNAME AS STAFFNAME2_2 ");
            stb.append("        ,CHSTF31.STAFFNAME AS STAFFNAME3_1 ");
            stb.append("        ,CHSTF32.STAFFNAME AS STAFFNAME3_2 ");
            stb.append("        ,T1.CHAIRNAME ");
            stb.append("        ,T1.CREDITS ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.ASSESSMARK ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T3.GRADE_RANK ");
            stb.append("        ,T3.GRADE_AVG_RANK ");
            stb.append("        ,T3.CLASS_RANK ");
            stb.append("        ,T3.CLASS_AVG_RANK ");
            stb.append("        ,T3.COURSE_RANK ");
            stb.append("        ,T3.COURSE_AVG_RANK ");
            stb.append("        ,T3.MAJOR_RANK ");
            stb.append("        ,T3.MAJOR_AVG_RANK ");
            stb.append("        ,T3.GRADE_AVG ");
            stb.append("        ,T3.GRADE_COUNT ");
            stb.append("        ,T3.GRADE_HIGHSCORE ");
            stb.append("        ,T3.HR_AVG ");
            stb.append("        ,T3.HR_COUNT ");
            stb.append("        ,T3.HR_HIGHSCORE ");
            stb.append("        ,T3.COURSE_AVG ");
            stb.append("        ,T3.COURSE_COUNT ");
            stb.append("        ,T3.COURSE_HIGHSCORE ");
            stb.append("        ,T3.MAJOR_AVG ");
            stb.append("        ,T3.MAJOR_COUNT ");
            stb.append("        ,T3.MAJOR_HIGHSCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,TQ.SUBCLASSCD AS ZOUKA ");
            if (param._cond22_1_addZoukaTanni) {
                stb.append("        ,CASE WHEN T33.GET_CREDIT IS NOT NULL OR TQ.CREDITS IS NOT NULL THEN VALUE(T33.GET_CREDIT, 0) + VALUE(TQ.CREDITS, 0) END AS GET_CREDIT ");
            } else {
                stb.append("        ,T33.GET_CREDIT ");
            }
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_MARK_NAME1 ");
            stb.append("        ,K1.SLUMP_SCORE ");
            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO ");
            //資格取得
            stb.append("  LEFT JOIN QUALIFIED TQ ON TQ.SCHREGNO = T1.SCHREGNO AND TQ.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME1 ON CHSEME1.SCHREGNO = T1.SCHREGNO AND CHSEME1.SEMESTER = '1' AND CHSEME1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME2 ON CHSEME2.SCHREGNO = T1.SCHREGNO AND CHSEME2.SEMESTER = '2' AND CHSEME2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME3 ON CHSEME3.SCHREGNO = T1.SCHREGNO AND CHSEME3.SEMESTER = '3' AND CHSEME3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF11 ON CHSTF11.STAFFCD = CHSEME1.STAFFCD1 ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF12 ON CHSTF12.STAFFCD = CHSEME1.STAFFCD2 ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF21 ON CHSTF21.STAFFCD = CHSEME2.STAFFCD1 ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF22 ON CHSTF22.STAFFCD = CHSEME2.STAFFCD2 ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF31 ON CHSTF31.STAFFCD = CHSEME3.STAFFCD1 ");
            stb.append("  LEFT JOIN STAFF_MST CHSTF32 ON CHSTF32.STAFFCD = CHSEME3.STAFFCD2 ");

            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     T1.SUBCLASSCD not like '%333333' AND T1.SUBCLASSCD not like '%555555' ");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD ");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
        public String getRank(final Param param) {
            return "2".equals(param._cond05_1SogoOrHeikin) ? _avgRank : _rank;
        }
    }

    private static class TestItem {
        public String _semester;
        public String _testkindcd;
        public String _testitemcd;
        public String _scoreDiv;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _scoreDivName;
        public DateRange _dateRange;
        public boolean _printScore;
        public String testcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + testcd() + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
    	final Param _param;
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
//        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final Integer _electdiv;
        boolean _isSaki;
        boolean _isMoto;
        String _calculateCreditFlg;
        SubclassMst _sakikamoku;
        boolean _isZenki;
        boolean _isKouki;
        boolean _isJirtsuKatudou;
        public SubclassMst(final Param param, final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final Integer electdiv) {
        	_param = param;
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
//            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _electdiv = electdiv;
        }
        public int compareTo(final SubclassMst os) {
            int rtn;
            if ("1".equals(_param._tutihyoUseSubclassElectdivAsShoworder)) {
				rtn = _electdiv.compareTo(os._electdiv);
				if (0 != rtn) {
					return rtn;
				}
            }
            final int iclasscd = Integer.parseInt(_classcd);
            final int iosclasscd = Integer.parseInt(os._classcd);
            if (_isJirtsuKatudou && !os._isJirtsuKatudou) { // 自立活動は総学のあとに表示
            	return 1;
            } else if (!_isJirtsuKatudou && os._isJirtsuKatudou) {
            	return -1;
            }
            if (iclasscd > 90 && iosclasscd <= 90) {
            	return 1;
            } else if (iclasscd <= 90 && iosclasscd > 90) {
            	return -1;
            }
            rtn = _classShoworder3.compareTo(os._classShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classcd.compareTo(os._classcd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder3.compareTo(os._subclassShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ", subclassname = " + _subclassname + (_isZenki ? ", zenki=" + _isZenki : "") + (_isKouki ? ", kouki = " + _isKouki : "") + ")";
        }
        
        private static Map<String, SubclassMst> getSubclassMst(
                final DB2UDB db2,
                final Param param,
                final String year
        ) {
        	Map<String, SubclassMst> subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += "   VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += "   T1.CLASSCD, ";
                sql += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += "   T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += "   SD012.SUBCLASS_REMARK1 AS SUBCLASS_REMARK1_SEQ12, ";
                sql += "   SD012.SUBCLASS_REMARK2 AS SUBCLASS_REMARK2_SEQ12, ";
                sql += "   VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += "   VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += "   VALUE(T1.ELECTDIV, '0') AS ELECTDIV ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_DETAIL_DAT SD012 ON SD012.YEAR = '" + year + "' AND SD012.CLASSCD = T1.CLASSCD AND SD012.SCHOOL_KIND = T1.SCHOOL_KIND AND SD012.CURRICULUM_CD = T1.CURRICULUM_CD AND SD012.SUBCLASSCD = T1.SUBCLASSCD ";
                sql += "     AND SD012.SUBCLASS_SEQ = '012' ";
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    final SubclassMst mst = new SubclassMst(param, KnjDbUtils.getString(row, "SPECIALDIV"), KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME")
                    		, KnjDbUtils.getString(row, "SUBCLASSABBV")
                    		, KnjDbUtils.getString(row, "SUBCLASSNAME")
                    		, KnjDbUtils.getInt(row, "CLASS_SHOWORDER3", new Integer(999)) 
                    		, KnjDbUtils.getInt(row, "SUBCLASS_SHOWORDER3", new Integer(999))
                    		, KnjDbUtils.getInt(row, "ELECTDIV", null));
                    subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                    if ("1".equals(KnjDbUtils.getString(row, "SUBCLASS_REMARK1_SEQ12"))) {
                    	mst._isZenki = true;
                    } else if ("1".equals(KnjDbUtils.getString(row, "SUBCLASS_REMARK2_SEQ12"))) {
                    	mst._isKouki = true;
                    }
                    mst._isJirtsuKatudou = param._e065JiritsuKaudouName1List.contains(mst._subclasscd);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, CALCULATE_CREDIT_FLG,  ";
                sql += " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT ";
                sql += " WHERE YEAR = '" + year + "' ";
                if (param._isOutputDebug) {
                	log.info(" repl sub sql = " + sql);
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    
                    final SubclassMst combined = subclassMstMap.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = subclassMstMap.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null != combined && null != attend) {
                    	combined._isSaki = true;
                    	attend._isMoto = true;
                    	combined._calculateCreditFlg = KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG");
                    	attend._sakikamoku = combined;
                    } else {
                    	log.warn(" combined = " + combined + ", attend = " + attend + " in " + row);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return subclassMstMap;
        }
    }

    private static class Form {
    	private final String ATTRIBUTE_TEISEISEN = "UnderLine=(0,3,5)";
        private final String ATTRIBUTE_TUISHIDO = "Paint=(1,90,2),Bold=1";
    	
		KNJD185W.Param _param;
		
		boolean _hasData;
        String[] _testcds;
        TestItem[] _testItems;
        Map<String, DateRange> _attendRanges;
        Map<String, DateRange> _attendSubclassRanges;
        Vrw32alp _svf;
		final Rank nullRank = new Rank(null, null, null, null, null);

        protected void initDebug() {
            for (int i = 0; i < _testcds.length; i++) {
                if (null != _testcds[i]) {
                    log.info(" testcds[" + i + "] = " + _testcds[i] + " : " + _testItems[i]);
                }
            }
            for (final String key : _attendRanges.keySet()) {
                log.info(" attendRanges[" + key + "] = " + _attendRanges.get(key));
            }
            for (final String key : _attendSubclassRanges.keySet()) {
                log.info(" attendSubclassRanges[" + key + "] = " + _attendSubclassRanges.get(key));
            }
        }

        private static int getMS932ByteLength(final String s) {
        	return KNJ_EditEdit.getMS932ByteLength(s);
        }

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = "3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov) ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

//        protected Map getAttendances(final Student student) {
//            final Map attendances = new HashMap();
//            for (final Iterator it = _attendRanges.keySet().iterator(); it.hasNext();) {
//                final String key = (String) it.next();
//                final DateRange dateRange = _attendRanges.get(key);
//                if (null != dateRange) {
//                    attendances.put(dateRange._key, student._attendMap.get(dateRange._key));
//                }
//            }
//            return attendances;
//        }

        protected Map getAttendanceRemarks(final Student student) {
            final Map remarks = new HashMap();
            for (final Iterator it = _attendRanges.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final DateRange dateRange = _attendRanges.get(key);
                if (null != dateRange) {
                    remarks.put(dateRange._key, student._semesterAttendrecRemark.get(dateRange._key));
                }
            }
            return remarks;
        }

        protected TestItem[] getTestItems(
                final Param param,
                final String[] testcds
        ) {
            final TestItem[] testitems = new TestItem[testcds.length];
            final Map<String, TestItem> testitemMap = new HashMap();
            for (final TestItem testItem : param._allTestItemList) {
                testitemMap.put(testItem.testcd(), testItem);
            }
            try {
                for (int j = 0; j < testcds.length; j++) {
                	if (null == testcds[j]) {
                		continue;
                	}
                	testitems[j] = testitemMap.get(testcds[j]);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            for (int i = 0; i < testcds.length; i++) {
                final String testcd = testcds[i];
                if (null == testitems[i] && null != testcd) {
                    log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + testcd);
                }
            }
            return testitems;
        }

        protected DateRange[] getSemesterDetails(
                final Param param,
                final int max
        ) {
            final DateRange[] semesterDetails = new DateRange[max];
            for (int j = 0, i = 0; i < param._allSemesterDetailList.size(); i++) {
                final DateRange sd = param._allSemesterDetailList.get(i);
                semesterDetails[j++] = sd;
                if (j >= max) {
                    break;
                }
            }
            return semesterDetails;
        }

        public static String[] get_token(final String strx0, final int f_len) {
            final List<String> token = KNJ_EditKinsoku.getTokenList(strx0, f_len);
            if (token.size() == 0) {
                return new String[] {};
            }
            final String[] array = new String[token.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = token.get(i);
            }
            return array;
        }

        public static String[] get_token(final String strx0, final int f_len, final int f_cnt) {
            final List<String> token = getTokenList(strx0, f_len, f_cnt);
            if (token.size() == 0) {
                return new String[] {};
            }
            final String[] array = new String[token.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = token.get(i);
            }
            return array;
        }

        public static List<String> getTokenList(final String strx0, final int f_len, final int f_cnt) {
            return KNJ_EditKinsoku.getTokenList(strx0, f_len, f_cnt);
        }

        protected void setTestcd(final List<String> testcdList, final int max, final String[] array) {
            log.info(" db testcdList = " + testcdList);
            if (testcdList.isEmpty()) {
                _testcds = array;
            } else {
                _testcds = new String[array.length];
                for (int i = 0; i < Math.min(testcdList.size(), max); i++) {
                    _testcds[i] = testcdList.get(i);
                }
                for (int i = max; i < array.length; i++) {
                    _testcds[i] = array[i];
                }
            }
        }

        public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
            return ipdf.setRecordString(field, gyo, data);
        }
        
        void init(final List testcdList) {
//            if (PATTERN_C.equals(_param._patternDiv)) {
//                final int maxTest = 6;
//                final String[] deftestcd = new String[maxTest * 2 + 1];
//                deftestcd[deftestcd.length - 1] = TESTCD_GAKUNEN_HYOTEI;
//                setTestcd(testcdList, maxTest * 2, deftestcd);
//            } else if (_param.maxSemesterIs3()) {
//                setTestcd(testcdList, 4, new String[] {"1990008", "2990008", null, TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
//            } else {
//                setTestcd(testcdList, 2, new String[] {"1990008", TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
//            }
            if (PATTERN_A.equals(_param._cond01_1TyohyoPattern) || PATTERN_B.equals(_param._cond01_1TyohyoPattern) || PATTERN_E.equals(_param._cond01_1TyohyoPattern) || PATTERN_F.equals(_param._cond01_1TyohyoPattern)) {
                setTestcd(testcdList, 4, new String[] {"1990008", "2990008", TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
            } else if (PATTERN_C.equals(_param._cond01_1TyohyoPattern)) {
                setTestcd(testcdList, 2, new String[] {_param._semester + TESTCD_HYOKA, TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
            } else if (PATTERN_D.equals(_param._cond01_1TyohyoPattern)) {
                setTestcd(testcdList, 2, new String[] {"1" + TESTCD_HYOKA, _param._semester + TESTCD_HYOKA, TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
            }
            _testItems = getTestItems(_param, _testcds);
            _attendRanges = new HashMap();
            for (final String semester : _param._semesterMap.keySet()) {
                final Semester oSemester = _param._semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            _attendSubclassRanges = new HashMap();
//            if (PATTERN_C.equals(_param._cond01_1TyohyoPattern)) {
//                for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
//                    final String semester = it.next();
//                    final Semester oSemester = _param._semesterMap.get(semester);
//                    _attendSubclassRanges.put(semester, oSemester._dateRange);
//                }
//            } else {
                for (int i = 0; i < _testItems.length; i++) {
                    final TestItem testitem = _testItems[i];
                    if (null == testitem) {
                        continue;
                    }
                    DateRange range = testitem._dateRange;
                    if (null != range && "99".equals(testitem._testkindcd)) {
                        final Semester semester = _param.getSemester(testitem._semester);
                        if (null != semester && null != semester._dateRange) {
                            range = semester._dateRange;
                        }
                    }
                    if (null != range) {
                        _attendSubclassRanges.put(testitem.testcd(), new DateRange(testitem.testcd(), testitem._testitemname, range._sdate, range._edate));
                    }
                }
                for (final Map.Entry<String, ?> e : _param._semesterMap.entrySet()) {
                    if (!SEMEALL.equals(e.getKey()) && null == _attendSubclassRanges.get(e.getKey())) {
                        _attendSubclassRanges.put(e.getKey(), _param.getSemester(e.getKey())._dateRange);
                    }
                }
                if (null != _param.getSemester(SEMEALL) && null != _param.getSemester(SEMEALL)._dateRange) {
                    _attendSubclassRanges.put(SEMEALL, _param.getSemester(SEMEALL)._dateRange);
                }
//            }
            if (_param._isOutputDebug) {
                initDebug();
            }
        }
        
        public void vrsOutRepeat(final String field, final List<String> dataList) {
        	if (null == dataList) {
        		return;
        	}
        	for (int i = 0; i < dataList.size(); i++) {
        		_svf.VrsOut(field + String.valueOf(i + 1), dataList.get(i));
        	}
        }
        
        public void vrsOutnRepeat(final String field, final List<String> dataList) {
        	if (null == dataList) {
        		return;
        	}
        	for (int i = 0; i < dataList.size(); i++) {
        		_svf.VrsOutn(field, i + 1, dataList.get(i));
        	}
        }
        
        // 空白の画像を表示して欄を非表示
        private void whitespace(final Vrw32alp svf, final String field) {
            if (null != _param._whitespaceImagePath) {
//                log.info(" space:" + field);
                svf.VrsOut(field, _param._whitespaceImagePath);
            }
        }
        
        public boolean print(final Vrw32alp svf, final List studentList) {
        	_svf = svf;
        	if (PATTERN_A.equals(_param._cond01_1TyohyoPattern)) {
        		printA(studentList);
        	} else if (PATTERN_B.equals(_param._cond01_1TyohyoPattern)) {
        		printB(studentList);
        	} else if (PATTERN_C.equals(_param._cond01_1TyohyoPattern)) {
        		printC(studentList);
        	} else if (PATTERN_D.equals(_param._cond01_1TyohyoPattern)) {
        		printD(studentList);
        	} else if (PATTERN_E.equals(_param._cond01_1TyohyoPattern)) {
        		printE(studentList);
        	} else if (PATTERN_F.equals(_param._cond01_1TyohyoPattern)) {
        		printF(studentList);
        	}
        	return _hasData;
        }
		
		private void setForm(String form, final int i) {
	    	_svf.VrSetForm(form, i);
	    	log.info(" form = " + form);
		}
		
		private String credits(final Student student, final Subclass subclass) {
			String credits;
			if (subclass._mst._isSaki) {
				if (_param._isOutputDebug) {
					log.info(" saki " + subclass._mst._subclasscd + " creditFlg = " + subclass._mst._calculateCreditFlg);
				}
				if ("2".equals(subclass._mst._calculateCreditFlg)) {
					String num = null;
					for (final Subclass s : student._subclassMap.values()) {
						if (s._mst._isMoto && s._mst._sakikamoku == subclass._mst) {
							num = addNumber(num, s._credits);
							if (_param._isOutputDebug) {
								log.info(" saki " + subclass._mst._subclasscd + " add credit moto " + s._mst._subclasscd + " " + s._credits + " ");
							}
						}
					}
					if (_param._isOutputDebug) {
						log.info(" saki " + subclass._mst._subclasscd + " credit = " + num);
					}
					credits = num;
				} else {
					credits = subclass._credits;
				}
			} else {
				credits = subclass._credits;
			}
			return credits;
		}

		private void printA(final List<Student> studentList) {
			final String form;
			final int maxSemester;
			// 2学期
		    form = "KNJD185W_1_1.frm";
		    maxSemester = 2;
			final int maxLine = maxSemester + 1;
			
			final int maxSubclass = 18;
			final int maxStudent = 3;
		    final List<List<Student>> pageList;
		    if (_param._cond16_1_saidanyouSortAB) {
			    pageList = getSaidanyouPageList(multiline(studentList, maxSubclass), maxStudent);
		    } else {
			    pageList = getPageList(multiline(studentList, maxSubclass), maxStudent);
		    }

		    for (int pi = 0; pi < pageList.size(); pi++) {
		    	final List<Student> pageStudentList = pageList.get(pi);
		    	
		    	setForm(form, 4);

		    	for (int sti = 0; sti < maxStudent; sti++) {
			    	final String ssti = String.valueOf(sti + 1);
		    		if (_param._cond08_1_noPrintSogoABF && _param._cond07_1_noPrintTokubetsuKatsudouABF) {
		    			whitespace(_svf, "IMG_SOGOSP" + ssti);
		    		} else if (_param._cond08_1_noPrintSogoABF) {
	    				whitespace(_svf, "IMG_SOGO" + ssti);
		    		} else if (_param._cond07_1_noPrintTokubetsuKatsudouABF) {
	    				whitespace(_svf, "IMG_SP" + ssti);
	    			}
		    		if (_param._cond17_1_noPrintShokenABCD) {
	    				whitespace(_svf, "IMG_SHOKEN" + ssti);
		    		}
			    	if (!_param._cond12_1_noPrintRank) {
			    		for (int i = 0; i < _param._rankDiv.length; i++) {
			    			final String si = String.valueOf(i + 1);
			    			switch (_param._rankDiv[i]) {
			    			case COLUMN_ITEM_HR_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "クラス");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_COURSE_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "コ―ス");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_GRADE_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "学年");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_MAJOR_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "学科");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			}
			    		}
			    	}
		    	}

		    	for (int sti = 0; sti < pageStudentList.size(); sti++) {
			    	final Student student = pageStudentList.get(sti);
			    	
		            log.info(" schregno = " + student._schregno);
		            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

			    	final String stline = String.valueOf(sti + 1);
			    	
				    _svf.VrsOut("PRINCIPAL_NAME" + stline + "_" + (KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) <= 30 ? "1" : "2"), _param._certifSchoolPrincipalName); // 校長名

				    if (!StringUtils.isBlank(student._staffname2)) {
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_3", student._staffname); // 担任名
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_5", student._staffname2); // 担任名
				    } else {
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_" + (KNJ_EditEdit.getMS932ByteLength(student._staffname) <= 30 ? "1" : "2"), student._staffname); // 担任名
				    }

				    final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
				    _svf.VrsOut("NAME" + stline + "_" + (ketaName <= 40 ? "1" : ketaName <= 50 ? "2" : "3"), student._name); // 氏名

		    		if (!_param._cond08_1_noPrintSogoABF) {
		    			vrsOutRepeat("TOTAL_ACT" + stline + "_", KNJ_EditKinsoku.getTokenList(student._totalstudytime, 54)); // 総学 5行
		    		}
		    		if (!_param._cond07_1_noPrintTokubetsuKatsudouABF) {
				        _svf.VrsOut("CLUB" + stline, student._semesterClub.get(SEMEALL)); // 部活動
				        _svf.VrsOut("COMMITTEE" + stline, student._semesterCommittee.get(SEMEALL)); // 委員会
		    		}
		    		if (!_param._cond17_1_noPrintShokenABCD) {
		    			vrsOutRepeat("VIEW" + stline + "_", KNJ_EditKinsoku.getTokenList(student._communication, 44)); // 所見 7行
		    		}

		    		final String title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student._majorname);
			        _svf.VrsOut("TITLE" + stline, title); // タイトル
			        _svf.VrsOut("HR_NAME" + stline, StringUtils.defaultString(student._hrname) + StringUtils.defaultString(student._attendno)); // 年組
			        final String getGetCredit;
			        if (_param._isPrintCreditSemester2OnlyInSemester2) {
			        	getGetCredit = student.getGetCredit(_param, _param._semester, false);
			        } else {
			        	getGetCredit = student.getTotalGetCredit(_param, false, true);
			        }
			        _svf.VrsOut("SUBTOTAL_GET_CREDIT" + stline, getGetCredit); // 合計修得単位数
			        _svf.VrsOut("TOTAL_GET_CREDIT" + stline, student.getTotalGetCredit(_param, true, true)); // 累計修得単位数
			        
				    for (int j = 0; j < maxLine; j++) {
				        final int line = j + 1;
				        final Semester semester = j == maxLine - 1 ? _param.getSemester(SEMEALL) : _param.getSemester(String.valueOf(line));
				        if (null != semester) {
				        	_svf.VrsOutn("SEMESTER_NAME" + stline + "_1", line, semester._semestername); // 学期名称
					        _svf.VrsOutn("SEMESTER_NAME" + stline + "_2", line, semester._semestername); // 学期名称
				        }
				    }

				    for (int k = 0; k < maxLine; k++) {
				        final int semeline = k + 1;
				        final String semes = k == maxLine - 1 ? SEMEALL : String.valueOf(semeline);
				        if (!SEMEALL.equals(semes) && Integer.parseInt(_param._semester) < Integer.parseInt(semes)) {
				        	continue;
				        }
//				        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(semes)) {
//				        	continue;
//				        }
				        final Attendance att = student._attendMap.get(semes);
				        if (null != att) {
				        	_svf.VrsOutn("LESSON" + stline, semeline, String.valueOf(att._lesson)); // 授業日数
				        	_svf.VrsOutn("MOURNING" + stline, semeline, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
				        	_svf.VrsOutn("MUST" + stline, semeline, String.valueOf(att._mLesson)); // 出席しなければならない日数
				        	_svf.VrsOutn("ABSENT" + stline, semeline, String.valueOf(att._absent)); // 欠席日数
				        	_svf.VrsOutn("ATTEND" + stline, semeline, String.valueOf(att._present)); // 欠席日数
					    	if (!_param._cond19_1_noPrintLateEarlyC) {
					    		_svf.VrsOutn("LATE" + stline, semeline, String.valueOf(att._late)); // 遅刻
					    		_svf.VrsOutn("EARLY" + stline, semeline, String.valueOf(att._early)); // 早退
					    	}
				        }
				        _svf.VrsOutn("ATTEND_REMARK" + stline, semeline, student._semesterAttendrecRemark.get(semes)); // 出欠備考
				    }

		            List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
		            
		            final Subclass subclass999999 = student.getSubclass(SUBCLASSCD999999);
		            final List<Subclass> subclass90 = new ArrayList<Subclass>();
		            final List<Subclass> subclassHr = new ArrayList<Subclass>();
		            for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
		            	final Subclass subclass = it.next();
		            	if (CLASSCD_90.equals(subclass._mst._classcd)) {
		            		subclass90.add(subclass);
		            		it.remove();
		            	} else if (CLASSCD_HR.equals(subclass._mst._classcd)) {
		            		subclassHr.add(subclass);
		            		it.remove();
		            	}
		            }
		            print90Hr(stline, student, subclass90, subclassHr);
		            
		            printTotal(stline, subclass999999);

		            int subline = 0;
		            final List<List<Subclass>> subclassLineList = getPageList(subclassList, maxSubclass);
		            if (student._subclassPageIdx < subclassLineList.size()) {
		            	subclassList = subclassLineList.get(student._subclassPageIdx);
		            }
		            for (int i = 0; i < subclassList.size(); i++) {
		                final Subclass subclass = subclassList.get(i);
		                if (_param._isOutputDebug) {
		                    log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
		                }
		            	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 10) {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classname); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
		            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
		            	} else if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 14) {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classname); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
		            		_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
		            	} else {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classname); // 教科名
		            		_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
		            	}
				        _svf.VrsOut("CREDIT1_1", credits(student, subclass)); // 単位
					    final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname);
					    if (subclassnameKeta > 14) {
					    	_svf.VrsOut("SUBCLASS_NAME1_3_1", subclass._mst._subclassname); // 科目名
					    } else {
					    	_svf.VrsOut("SUBCLASS_NAME1_" + (subclassnameKeta <= 10 ? "1" : "2"), subclass._mst._subclassname); // 科目名
					    }
					    for (int ti = 0; ti < _testItems.length; ti++) {
					        final int testline = ti + 1;
					        final TestItem testItem = _testItems[ti];
					        if (null == testItem) {
					        	continue;
					        }
					        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(testItem._semester)) {
					        	continue;
					        }
				            
		            		final Score score = subclass.getScore(testItem.testcd());
		            		if (null != score) {
		            			_svf.VrsOutn("SCORE1_1", testline, StringUtils.defaultString(score._slumpScore, score._score)); // 評点
		    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score._score) && Integer.parseInt(score._score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
		    						_svf.VrAttributen("SCORE1_1", testline, ATTRIBUTE_TUISHIDO); // 点数等
		    					}
		            		}

					        printKekka("KEKKA1_1", testline, subclass.getAttendance(testItem._dateRange._key));
				        }
				        _svf.VrEndRecord();
				        subline += 1;
		            }
				    for (int k = subline; k < maxSubclass; k++) {
				        _svf.VrsOut("CLASS_NAME1", String.valueOf(k)); // 教科名
				        _svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
				        _svf.VrEndRecord();
				    }
				    _hasData = true;
			    }
		    	
		    	for (int sti = pageStudentList.size(); sti < maxStudent; sti++) {
				    for (int subline = 0; subline < maxSubclass; subline++) {
				        _svf.VrsOut("CLASS_NAME1", String.valueOf(subline)); // 教科名
				        _svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
				        _svf.VrEndRecord();
				    }
		    	}
		    }
		}

		private List<Student> multiline(final List<Student> studentList, final int maxSubclass) {
			final List<Student> list = new ArrayList<Student>();
			for (final Student student : studentList) {
				list.add(student);

				final List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
				
//				final Subclass subclass999999 = student.getSubclass(SUBCLASSCD999999);
//				Subclass subclass90 = null;
//				Subclass subclassHr = null;
				for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
					final Subclass subclass = it.next();
					if (CLASSCD_90.equals(subclass._mst._classcd)) {
//						subclass90 = subclass;
						it.remove();
					} else if (CLASSCD_HR.equals(subclass._mst._classcd)) {
//						subclassHr = subclass;
						it.remove();
					}
				}
				final List<List<Subclass>> subclassLineList = getPageList(subclassList, maxSubclass);
				for (int i = 1; i < subclassLineList.size(); i++) {
					final Student copy = student.copy();
					copy._subclassPageIdx = i;
					list.add(copy);
					if (_param._isOutputDebug) {
						log.info(" add student " + copy._schregno);
					}
				}
			}

			return list;
		}

		private void printTotal(final String stline, Subclass subclass999999) {
			if (null == subclass999999) {
				return;
			}
			final Rank nullRank = new Rank(null, null, null, null, null);
			for (int ti = 0; ti < _testItems.length; ti++) {
				final int testline = ti + 1;
				final TestItem testItem = _testItems[ti];
				if (null == testItem) {
					continue;
				}
		        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(testItem._semester)) {
		        	continue;
		        }
				final Score score = subclass999999.getScore(testItem.testcd());
				if (null != score) {
					
					_svf.VrsOutn("TOTAL_SCORE" + stline, testline, score._score); // 総合点
					_svf.VrsOutn("TOTAL_AVERAGE"+ stline, testline, sishaGonyu(score._avg)); // 総合平均点
					
			    	if (!_param._cond12_1_noPrintRank) {
			    		for (int i = 0; i < _param._rankDiv.length; i++) {
			    			final String si = String.valueOf(i + 1);
			    			Rank rank = nullRank;
			    			switch (_param._rankDiv[i]) {
			    			case COLUMN_ITEM_HR_RANK:
			    				rank = score._hrRank;
			    				break;
			    			case COLUMN_ITEM_COURSE_RANK:
			    				rank = score._courseRank;
			    				break;
			    			case COLUMN_ITEM_GRADE_RANK:
			    				rank = score._gradeRank;
			    				break;
			    			case COLUMN_ITEM_MAJOR_RANK:
			    				rank = score._majorRank;
			    				break;
			    			default:
			    				if (-1 != _param._rankDiv[i]) {
			    					log.warn("not found : " + _param._rankDiv[i]);
			    				}
			    			}
			    			_svf.VrsOutn("RANK" + stline + "_" + si, testline, rank.getRank(_param)); // 順位
			    		}
			    	}
				}
			}
		}

		private void print90Hr(final String stline, Student student, List<Subclass> subclass90, List<Subclass> subclassHr) {
			for (int subi = 2; subi <= 3; subi++) {
				List<Subclass> subclasses = null;
				if (subi == 2) {
					subclasses = subclass90;
				} else if (subi == 3) {
					subclasses = subclassHr;
				}
				if (null == subclasses) {
					continue;
				}
				String credits = null;
				for (final Subclass subclass : subclasses) {
					credits = addNumber(credits, credits(student, subclass));
				}
			    _svf.VrsOut("CREDIT" + stline + "_" + String.valueOf(subi), credits); // 単位
			    
			    for (int ti = 0; ti < _testItems.length; ti++) {
			        final int testline = ti + 1;
			        final TestItem testItem = _testItems[ti];
			        if (null == testItem) {
			        	continue;
			        }
			        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(testItem._semester)) {
			        	continue;
			        }

//					final Score score = subclass.getScore(testItem.testcd());
//					if (null != score) {
//						_svf.VrsOutn("SCORE" + stline + "_" + String.valueOf(subi), testline, StringUtils.defaultString(score._slumpScore, score._score)); // 評点
//					}

			        SubclassAttendance totalAtt = null;
					for (final Subclass subclass : subclasses) {
						totalAtt = SubclassAttendance.add(totalAtt, subclass.getAttendance(testItem._dateRange._key));
					}
			        printKekka("KEKKA" + stline + "_" + String.valueOf(subi), testline, totalAtt);
			    }
			}
		}

		private void printKekka(final String field, final int testline, final SubclassAttendance subatt) {
			if (null == subatt || null == subatt._sick) {
				return;
			}
			if (null != subatt._lesson && 0 < subatt._sick.doubleValue()) {
				BigDecimal limit = getKekkaLimit(subatt._lesson);
				if (limit.compareTo(subatt._sick) <= 0) { // 以上
					_svf.VrAttributen(field, testline, ATTRIBUTE_TUISHIDO); // 欠課網掛け
				}
			}
			_svf.VrsOutn(field, testline, _param._df.format(subatt._sick)); // 欠課
		}
		
		private void printB(final List<Student> studentList) {
			
			final String form;
			final int maxSemester;
			// 2学期
			form = "KNJD185W_2_1.frm";			
			maxSemester = 2;
			
			final int maxSubclass = 18;
			final int maxStudent = 2;
		    final List<List<Student>> pageList;
		    if (_param._cond16_1_saidanyouSortAB) {
			    pageList = getSaidanyouPageList(multiline(studentList, maxSubclass), maxStudent);
		    } else {
			    pageList = getPageList(multiline(studentList, maxSubclass), maxStudent);
		    }
		    
		    for (int pi = 0; pi < pageList.size(); pi++) {
		    	final List<Student> pageStudentList = pageList.get(pi);
		    	
		    	log.info(" pi = " + pi + ", pageStudentList size = " + pageStudentList.size());

		    	setForm(form, 4);

		    	for (int sti = 0; sti < maxStudent; sti++) {
			    	final String ssti = String.valueOf(sti + 1);
		    		if (_param._cond08_1_noPrintSogoABF && _param._cond07_1_noPrintTokubetsuKatsudouABF) {
		    			whitespace(_svf, "IMG_SOGOSP" + ssti);
		    		} else if (_param._cond08_1_noPrintSogoABF) {
	    				whitespace(_svf, "IMG_SOGO" + ssti);
		    		} else if (_param._cond07_1_noPrintTokubetsuKatsudouABF) {
	    				whitespace(_svf, "IMG_SP" + ssti);
	    			}
		    		if (_param._cond17_1_noPrintShokenABCD) {
	    				whitespace(_svf, "IMG_SHOKEN" + ssti);
		    		}
			    	if (!_param._cond12_1_noPrintRank) {
			    		for (int i = 0; i < _param._rankDiv.length; i++) {
			    			final String si = String.valueOf(i + 1);
			    			switch (_param._rankDiv[i]) {
			    			case COLUMN_ITEM_HR_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "クラス");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_COURSE_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "コ―ス");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_GRADE_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "学年");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			case COLUMN_ITEM_MAJOR_RANK:
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si, "学科");
							    _svf.VrsOut("RANKNAME" + ssti + "_" + si + "_2", "順位");
			    				break;
			    			}
			    		}
			    	}
		    	}

		    	for (int sti = 0; sti < pageStudentList.size(); sti++) {
			    	final Student student = pageStudentList.get(sti);
			    	
		            log.info(" schregno = " + student._schregno);
		            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

					final String stline = String.valueOf(sti + 1);

		    		final String title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student._majorname);
					_svf.VrsOut("TITLE" + stline, title); // タイトル
					_svf.VrsOut("SUBTOTAL_GET_CREDIT" + stline, student.getTotalGetCredit(_param, false, false)); // 合計修得単位数
					_svf.VrsOut("TOTAL_GET_CREDIT" + stline, student.getTotalGetCredit(_param, true, false)); // 累計修得単位数
			        _svf.VrsOut("HR_NAME" + stline, StringUtils.defaultString(student._hrname) + StringUtils.defaultString(student._attendno)); // 年組

				    _svf.VrsOut("PRINCIPAL_NAME" + stline + "_" + (KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) <= 30 ? "1" : "2"), _param._certifSchoolPrincipalName); // 校長名

				    if (!StringUtils.isBlank(student._staffname2)) {
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_3", student._staffname); // 担任名
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_5", student._staffname2); // 担任名
				    } else {
				    	_svf.VrsOut("TEACHER_NAME" + stline + "_" + (KNJ_EditEdit.getMS932ByteLength(student._staffname) <= 30 ? "1" : "2"), student._staffname); // 担任名
				    }
				    
				    final int ketaName1_ = KNJ_EditEdit.getMS932ByteLength(student._name);
				    _svf.VrsOut("NAME" + stline + "_" + (ketaName1_ <= 40 ? "1" : ketaName1_ <= 50 ? "2" : "3"), student._name); // 氏名

				    if (!_param._cond08_1_noPrintSogoABF) {
				    	vrsOutnRepeat("TOTAL_ACT" + stline + "_", KNJ_EditKinsoku.getTokenList(student._totalstudytime, 54)); // 総学 5行
				    }
				    if (!_param._cond07_1_noPrintTokubetsuKatsudouABF) {
				    	_svf.VrsOut("CLUB" + stline, student._semesterClub.get(SEMEALL)); // 部活動
				    	_svf.VrsOut("COMMITTEE" + stline, student._semesterCommittee.get(SEMEALL)); // 委員会
				    }
				    if (!_param._cond17_1_noPrintShokenABCD) {
				    	vrsOutnRepeat("VIEW" + stline + "_", KNJ_EditKinsoku.getTokenList(student._communication, 44)); // 所見 7行
				    }

				    final int maxLine = maxSemester + 1;

				    for (int j = 0; j < maxLine; j++) {
				        final int line = j + 1;
				        final Semester semester;
				        if (j == maxLine - 1) {
				        	semester = _param.getSemester(SEMEALL);
				        } else {
				        	semester = _param.getSemester(String.valueOf(line));
				        }
				        if (null != semester) {
				        	_svf.VrsOutn("SEMESTER_NAME" + stline + "_1", line, semester._semestername); // 学期名称
					        _svf.VrsOutn("SEMESTER_NAME" + stline + "_2", line, semester._semestername); // 学期名称
				        }
				    }

				    for (int j = 0; j < maxLine; j++) {
				        final int line = j + 1;
				        final String semes = j == maxLine - 1 ? SEMEALL : String.valueOf(line);
				        if (!SEMEALL.equals(semes) && Integer.parseInt(_param._semester) < Integer.parseInt(semes)) {
				        	continue;
				        }
//				        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(semes)) {
//				        	continue;
//				        }
				        final Attendance att = student._attendMap.get(semes);
				        if (null != att) {
				        	_svf.VrsOutn("LESSON" + stline, line, String.valueOf(att._lesson)); // 授業日数
				        	_svf.VrsOutn("MOURNING" + stline, line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
				        	_svf.VrsOutn("MUST" + stline, line, String.valueOf(att._mLesson)); // 出席しなければならない日数
				        	_svf.VrsOutn("ABSENT" + stline, line, String.valueOf(att._absent)); // 欠席日数
				        	_svf.VrsOutn("ATTEND" + stline, line, String.valueOf(att._present)); // 欠席日数
					    	if (!_param._cond19_1_noPrintLateEarlyC) {
					    		_svf.VrsOutn("LATE" + stline, line, String.valueOf(att._late)); // 遅刻
					    		_svf.VrsOutn("EARLY" + stline, line, String.valueOf(att._early)); // 早退
					    	}
				        }
				        final String remark = student._semesterAttendrecRemark.get(semes);
				        _svf.VrsOutn("ATTEND_REMARK" + stline, line, remark); // 出欠備考
				    }
				    
		            List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
		            
		            
		            final Subclass subclass999999 = student.getSubclass(SUBCLASSCD999999);
		            List<Subclass> subclass90 = new ArrayList<Subclass>();
		            List<Subclass> subclassHr = new ArrayList<Subclass>();
		            for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
		            	final Subclass subclass = it.next();
		            	if (CLASSCD_90.equals(subclass._mst._classcd)) {
		            		subclass90.add(subclass);
		            		it.remove();
		            	} else if (CLASSCD_HR.equals(subclass._mst._classcd)) {
		            		subclassHr.add(subclass);
		            		it.remove();
		            	}
		            }
		            print90Hr(stline, student, subclass90, subclassHr);

		            printTotal(stline, subclass999999);

		            int subline = 0;
		            final List<List<Subclass>> subclassLineList = getPageList(subclassList, maxSubclass);
		            if (student._subclassPageIdx < subclassLineList.size()) {
		            	subclassList = subclassLineList.get(student._subclassPageIdx);
		            }
		            for (int i = 0; i < subclassList.size(); i++) {
		                final Subclass subclass = subclassList.get(i);
		                if (_param._isOutputDebug) {
		                    log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
		                }
					    final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname);
					    if (subclassnameKeta > 14) {
					    	_svf.VrsOut("SUBCLASS_NAME1_3_1", subclass._mst._subclassname); // 科目名
					    } else {
						    _svf.VrsOut("SUBCLASS_NAME1_" + (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) <= 10 ? "1" : "2"), subclass._mst._subclassname); // 科目名
					    }

		            	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 10) {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classname); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
		            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
		            	} else if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 14) {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classname); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
		            		_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
		            	} else {
		            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
		            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classname); // 教科名
		            		_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
		            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
		            	}

						_svf.VrsOut("CREDIT1_1", credits(student, subclass)); // 単位
						
					    for (int ti = 0; ti < _testItems.length; ti++) {
					        final int testline = ti + 1;
					        final TestItem testItem = _testItems[ti];
					        if (null == testItem) {
					        	continue;
					        }
					        if ("1".equals(_param._knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && "1".equals(testItem._semester)) {
					        	continue;
					        }
				            
		            		final Score score = subclass.getScore(testItem.testcd());
		            		if (null != score) {
		            			_svf.VrsOutn("SCORE1_1", testline, score.getPrintScore(testItem)); // 評点
		    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score._score) && Integer.parseInt(score._score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
		    						_svf.VrAttributen("SCORE1_1", testline, ATTRIBUTE_TUISHIDO); // 点数等
		    					}
		            		}

					        printKekka("KEKKA1_1", testline, subclass.getAttendance(testItem._dateRange._key));
				        }
						_svf.VrEndRecord();
						subline += 1;
		            }
					for (int j = subline; j < maxSubclass; j++) {
		    			_svf.VrsOut("CLASS_NAME1", String.valueOf(subline)); // 教科名
		    			_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
						_svf.VrEndRecord();
					}
					_hasData = true;
			    }
		    	
		    	for (int sti = pageStudentList.size(); sti < maxStudent; sti++) {
		    		for (int subline = 0; subline < maxSubclass; subline++) {
		    			_svf.VrsOut("CLASS_NAME1", String.valueOf(subline)); // 教科名
		    			_svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
		    			_svf.VrEndRecord();
		    		}
		    	}
		    }
		}

		private void printC(final List<Student> studentList) {
		    final String form = "KNJD185W_3.frm";
		    
	        final Semester semester = _param.getSemester(_param._semester);
	        final String semestername = null == semester ? "" : semester._semestername; 

		    for (int sti = 0; sti < studentList.size(); sti++) {
		    	final Student student = studentList.get(sti);
		    	
	            log.info(" schregno = " + student._schregno);
	            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

			    setForm(form, 4);
			    
			    _svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 30 ? "1" : "2"), student._name); // 名前

			    if (!StringUtils.isBlank(student._staffname2)) {
			    	final String stf1 = StringUtils.defaultString(student._staffname) + " 印　　";
			    	final String stf2 = StringUtils.defaultString(student._staffname2);
			    	final int space = 34 - KNJ_EditEdit.getMS932ByteLength(stf1) - KNJ_EditEdit.getMS932ByteLength(stf2) - KNJ_EditEdit.getMS932ByteLength("　");
				    _svf.VrsOut("TEACHER_NAME1_1", stf1 + StringUtils.repeat("　", space / 2) + stf2); // 担任名
			    } else {
				    _svf.VrsOut("TEACHER_NAME1_" + (KNJ_EditEdit.getMS932ByteLength(student._staffname) <= 34 ? "1" : "2"), student._staffname); // 担任名
			    }
			    if ("1".equals(_param._knjd185wNotPrintAttendSubclassDatLesson)) {
				    _svf.VrsOut("KEKKA_TITLE", "欠課");
			    }

			    _svf.VrsOut("SUBTOTAL_GET_CREDIT2", String.valueOf(student._previousCredits1)); // 合計修得単位数 前籍校での既修得単位数
			    _svf.VrsOut("SUBTOTAL_GET_CREDIT1", String.valueOf(student._previousCredits0)); // 合計修得単位数 本校での既修得単位数
			    _svf.VrsOut("TOTAL_GET_CREDIT1", student.getTotalGetCredit(_param, true, _param._isLastSemester)); // 累計修得単位数
			    
			    final String title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student._majorname) + "　成績通知表　" + StringUtils.defaultString(semestername);
			    _svf.VrsOut("SCHOOL_NAME", title); // 学校名
			    _svf.VrsOut("HR_NAME", student._hrname); // 年組
			    _svf.VrsOut("NO", student._attendnoZeroSuprpess); // NO
			    
	            final List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
	            final Subclass subclass999999 = student.getSubclass(SUBCLASSCD999999);
			    final TestItem testItem = _param._testItemMap.get(_param._semester + TESTCD_HYOKA);
			    if (null != testItem) {
			    	if (null != subclass999999) {
			    		final Score score = subclass999999.getScore(testItem.testcd());
			    		if (null != score) {
			    			_svf.VrsOut("TOTAL_SCORE1", score._score); // 総合点
			    			_svf.VrsOut("TOTAL_AVERAGE1", sishaGonyu(score._avg)); // 総合平均点
					    	if (!_param._cond12_1_noPrintRank) {
					    		for (int i = 0; i < _param._rankDiv.length; i++) {
					    			String name = "";
					    			Rank rank = nullRank;
					    			switch (_param._rankDiv[i]) {
					    			case COLUMN_ITEM_HR_RANK:
					    				name = "クラス";
					    				rank = score._hrRank;
					    				break;
					    			case COLUMN_ITEM_COURSE_RANK:
					    				name = "コース";
					    				rank = score._courseRank;
					    				break;
					    			case COLUMN_ITEM_GRADE_RANK:
					    				name = "学年";
					    				rank = score._gradeRank;
					    				break;
					    			case COLUMN_ITEM_MAJOR_RANK:
					    				name = "学科";
					    				rank = score._majorRank;
					    				break;
					    			default:
					    				if (-1 != _param._rankDiv[i]) {
					    					log.warn("not found : " + _param._rankDiv[i]);
					    				}
					    			}
					    			_svf.VrsOut("RANK1_NAME", name); // 順位
					    			_svf.VrsOut("RANK1_NAME2", "順位"); // 順位
					    			_svf.VrsOut("RANK1_1", rank.getRank(_param)); // 順位
					    		}
					    	}
			    		}
			    	}
			    }
			    
			    if (!_param._cond18_1_noPrintChairStaffnameCD) {
			    	_svf.VrsOut("TANTOU", "担当");
			    }
			    if (!_param._cond19_1_noPrintLateEarlyC) {
			    	_svf.VrsOut("CHIKOKU_KAISU", "遅刻回数");
			    	_svf.VrsOut("SOUTAI_KAISU", "早退回数");
			    }

			    final Attendance attAll = student._attendMap.get(SEMEALL);
			    if (null != attAll) {
			    	_svf.VrsOut("LESSON1", String.valueOf(attAll._lesson)); // 授業日数
			    	_svf.VrsOut("MOURNING1", String.valueOf(attAll._suspend + attAll._mourning)); // 忌引出停日数
			    	_svf.VrsOut("MUST1", String.valueOf(attAll._mLesson)); // 出席しなければならない日数
			    	_svf.VrsOut("ABSENT1", String.valueOf(attAll._absent)); // 欠席日数
			    	_svf.VrsOut("ATTEND1", String.valueOf(attAll._present)); // 欠席日数
			    	if (!_param._cond19_1_noPrintLateEarlyC) {
			    		_svf.VrsOut("LATE1", String.valueOf(attAll._late)); // 遅刻
			    		_svf.VrsOut("EARLY1", String.valueOf(attAll._early)); // 早退
			    	}
			    }

			    vrsOutnRepeat("REMARK", KNJ_EditKinsoku.getTokenList(student._remark, 46)); // 備考 7行

			    if (_param._cond17_1_noPrintShokenABCD) {
    				whitespace(_svf, "IMG_VIEW");
	    		} else {
				    vrsOutnRepeat("VIEW", KNJ_EditKinsoku.getTokenList(student._communication, 46)); // 11行
	    		}
			    
	            int subline = 0;
	            String totalGetCredit = null;
	            for (int i = 0; i < subclassList.size(); i++) {
	                final Subclass subclass = subclassList.get(i);
	                if (_param._isOutputDebug) {
	                    //log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
	                	log.info(" subclass = " + subclass);
	                }
	            	
	            	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 12) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 20) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classname); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            	}
			        _svf.VrsOut("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) <= 22 ? "1" : "2"), subclass._mst._subclassname); // 科目名

			        _svf.VrsOut("CREDIT", credits(student, subclass)); // 単位数
			        if (null != testItem) {
			        	final Score score = subclass.getScore(testItem.testcd());
			        	if (null != score) {
			        		_svf.VrsOut("SCORE", score.getPrintScore(testItem)); // 点数等
	    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score._score) && Integer.parseInt(score._score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
	    						_svf.VrAttribute("SCORE", ATTRIBUTE_TUISHIDO); // 点数等
	    					}
					        _svf.VrsOut("SUBCLASS_AVERAGE", sishaGonyu(score._gradeRank._avg)); // 科目平均点 (学年平均点)
			        	}
			        }
			        final TestItem gakunenHyoteiTestItem = _param._testItemMap.get(TESTCD_GAKUNEN_HYOTEI);
		        	final Score hyotei = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
		        	if (null != hyotei) {
		        		_svf.VrsOut("DIV", hyotei.getPrintScore(gakunenHyoteiTestItem)); // 評定
		        	}
		        	final String getCredit = subclass.getGetCredit(subclass.creditEnabled(_param, _param._semester, _param._isLastSemester));
		        	totalGetCredit = addNumber(totalGetCredit, getCredit);
		        	_svf.VrsOut("JUDGE", subclass.getNinteiString(_param._semester, _param._isLastSemester)); // 判定
		        	final SubclassAttendance att = subclass.getAttendance(SEMEALL);
		        	if (null != att) {
		        		final String sick = null == att._sick ? " " : _param._df.format(att._sick);
		        		if ("1".equals(_param._knjd185wNotPrintAttendSubclassDatLesson)) {
		        			_svf.VrsOut("KEKKA", sick); // 欠課
		        		} else {
		        			final String lesson = null == att._lesson ? " " : att._lesson.toString();
		        			_svf.VrsOut("KEKKA", sick + " / " + lesson); // 欠課
		        		}
		        	}
			        
			        if (!_param._cond18_1_noPrintChairStaffnameCD) {
				    	final String subclassTeacher = mkString(subclass.getStaffnameList(_param), "、");
						_svf.VrsOut("SUBCLASS_TEACHER" + (KNJ_EditEdit.getMS932ByteLength(subclassTeacher) <= 14 ? "1" : "2"), subclassTeacher); // 科目担当
				    }

	                _svf.VrEndRecord();
	                subline += 1;
	            }
			    final int maxSubclass = 20;
			    for (int i = subline; i < maxSubclass; i++) {
			    	_svf.VrEndRecord();
			    }
		        _svf.VrsOut("SUBCLASS_NAME1", "修得単位数計"); // 科目名
		        _svf.VrsOut("CREDIT", totalGetCredit); // 単位数
		    	_svf.VrEndRecord();
			    _hasData = true;
		    }
		}

		public static String mkString(final List<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
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

		private void printD(final List<Student> studentList) {
		    final String form;
		    final int maxSemester;
			// 2学期
			form = "KNJD185W_4_1.frm";
			maxSemester = 2;
				
	        final Semester semester = _param.getSemester(_param._semester);
	        final String semestername = null == semester ? "" : semester._semestername; 
			
		    for (final Student student : studentList) {

		    	log.info(" schregno = " + student._schregno);
	            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

			    setForm(form, 4);
			    
	    		final String courseName = StringUtils.defaultString(student._majorname) + "　" + StringUtils.defaultString(student._coursecodename);
			    _svf.VrsOut("COURSE_NAME" + (KNJ_EditEdit.getMS932ByteLength(courseName) <= 30 ? "1" : "2"), courseName); // 学科・コース

			    _svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 30 ? "1" : "2"), student._name); // 名前

			    _svf.VrsOut("PRINCIPAL_NAME1_" + (KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) <= 34 ? "1" : "2"), _param._certifSchoolPrincipalName); // 校長名

			    if (!StringUtils.isBlank(student._staffname2)) {
			    	_svf.VrsOut("TEACHER_NAME2", student._staffname); // 担任名
			    	_svf.VrsOut("TEACHER_NAME3", student._staffname2); // 担任名
			    } else {
			    	_svf.VrsOut("TEACHER_NAME1_" + (KNJ_EditEdit.getMS932ByteLength(student._staffname) <= 34 ? "1" : "2"), student._staffname); // 担任名
			    }
			    
			    if (!_param._cond18_1_noPrintChairStaffnameCD) {
			    	_svf.VrsOut("TANTOU", "担当");
			    }
				
			    final String title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student._majorname) + "　成績通知表　" + StringUtils.defaultString(semestername);
				_svf.VrsOut("SCHOOL_NAME", title); // 学校名
				_svf.VrsOut("NO", student._attendnoZeroSuprpess); // NO
				
		    	if (!_param._cond19_1_noPrintLateEarlyC) {
			    	_svf.VrsOut("CHIKOKU_KAISU", "遅刻回数");
			    	_svf.VrsOut("SOUTAI_KAISU", "早退回数");
		    	}
				
		        final Subclass subclass999999 = student.getSubclass(SUBCLASSCD999999);
			    final int maxLine = maxSemester;
			    for (int j = 0; j < maxLine; j++) {
			        final int line = j + 1;
			        final String semes = j == maxLine - 1 ? SEMEALL : String.valueOf(line);
			        final Semester s = _param.getSemester(semes);
			        if (null != s) {
			        	_svf.VrsOutn("SEMESTER", line, s._semestername); // 学期
			        }
			        
			        final Score score999999 = subclass999999.getScore(semes + TESTCD_HYOKA);
			        if (null != score999999) {
			        	_svf.VrsOutn("TOTAL_SCORE1", line, score999999._score); // 総合点
			        	_svf.VrsOutn("TOTAL_AVERAGE1", line, sishaGonyu(score999999._avg)); // 総合平均点

				    	if (!_param._cond12_1_noPrintRank) {
				    		for (int i = 0; i < _param._rankDiv.length; i++) {
				    			String name = "";
				    			Rank rank = nullRank;
				    			switch (_param._rankDiv[i]) {
				    			case COLUMN_ITEM_HR_RANK:
				    				name = "クラス順位";
				    				rank = score999999._hrRank;
				    				break;
				    			case COLUMN_ITEM_COURSE_RANK:
				    				name = "コース順位";
				    				rank = score999999._courseRank;
				    				break;
				    			case COLUMN_ITEM_GRADE_RANK:
				    				name = "学年順位";
				    				rank = score999999._gradeRank;
				    				break;
				    			case COLUMN_ITEM_MAJOR_RANK:
				    				name = "学科順位";
				    				rank = score999999._majorRank;
				    				break;
				    			default:
				    				if (-1 != _param._rankDiv[i]) {
				    					log.warn("not found : " + _param._rankDiv[i]);
				    				}
				    			}
				    			_svf.VrsOut("RANK1_TITLE", name); // 順位
					        	_svf.VrsOutn("RANK1_1", line, rank.getRank(_param)); // 順位
				    		}
				    	}
			        }

				    final Attendance att = student._attendMap.get(semes);
				    if (null != att) {
				    	_svf.VrsOutn("LESSON1", line, String.valueOf(att._lesson)); // 授業日数
				    	_svf.VrsOutn("MOURNING1", line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
				    	_svf.VrsOutn("MUST1", line, String.valueOf(att._mLesson)); // 出席しなければならない日数
				    	_svf.VrsOutn("ABSENT1", line, String.valueOf(att._absent)); // 欠席日数
				    	_svf.VrsOutn("ATTEND1", line, String.valueOf(att._present)); // 欠席日数
				    	if (!_param._cond19_1_noPrintLateEarlyC) {
				    		_svf.VrsOutn("LATE1", line, String.valueOf(att._late)); // 遅刻
				    		_svf.VrsOutn("EARLY1", line, String.valueOf(att._early)); // 早退
				    	}
				    }
			        _svf.VrsOutn("REMARK1", line, student._semesterAttendrecRemark.get(SEMEALL.equals(semes) ? String.valueOf(maxSemester) : semes)); // 備考
			        
			        _svf.VrsOutn("SUBTOTAL_GET_CREDIT1", line, String.valueOf(student._previousCredits0)); // 合計修得単位数
			        _svf.VrsOutn("SUBTOTAL_GET_CREDIT2", line, String.valueOf(student._previousCredits1)); // 合計修得単位数
			        _svf.VrsOutn("TOTAL_GET_CREDIT1", line, student.getTotalGetCredit(_param, true, false)); // 累計修得単位数
			    }
				
			    if (_param._cond17_1_noPrintShokenABCD) {
    				whitespace(_svf, "IMG_VIEW");
	    		} else {
				    vrsOutnRepeat("VIEW", KNJ_EditKinsoku.getTokenList(student._communication, 44)); // 15行
	    		}

			    String totalGetCredit = null;
	            final List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
	            for (int i = 0; i < subclassList.size(); i++) {
	                final Subclass subclass = subclassList.get(i);
	                totalGetCredit = addNumber(totalGetCredit, subclass.getGetCredit(subclass.creditEnabled(_param, _param._semester, false)));
	            }
				if (SEMEALL.equals(_param._semester)) {
					_svf.VrsOut("GET_CREDIT", totalGetCredit); // 期間
				}

	            int subline = 0;
	            for (int i = 0; i < subclassList.size(); i++) {
	                final Subclass subclass = subclassList.get(i);
	                if (_param._isOutputDebug) {
	                    log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
	                }
	                final String testcd = _param._semester + TESTCD_HYOKA;
	                final TestItem testItem = _param._testItemMap.get(testcd);

	            	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 12) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 20) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classname); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            	}
			        _svf.VrsOut("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) <= 22 ? "1" : "2"), subclass._mst._subclassname); // 科目名

			        if ("0".equals(subclass._minTakesemes)) {
				        _svf.VrsOut("PERIOD", "通年"); // 期間
			        } else if (null != subclass._minTakesemes) {
			        	final Semester chairSemester = _param.getSemester(subclass._minTakesemes);
			        	if (null != chairSemester) {
			        		_svf.VrsOut("PERIOD", chairSemester._semestername); // 期間
			        	}
			        }
			        _svf.VrsOut("CREDIT", credits(student, subclass)); // 単位数
			        
			        final Score score = subclass.getScore(testcd);
			        if (null != score) {
			        	_svf.VrsOut("SCORE", score.getPrintScore(testItem)); // 点数等
    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score._score) && Integer.parseInt(score._score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
    			        	_svf.VrAttribute("SCORE", ATTRIBUTE_TUISHIDO); // 点数等
    					}
			        	_svf.VrsOut("SUBCLASS_AVERAGE", sishaGonyu(score._gradeRank._avg)); // 科目平均点
			        }
			        
			        final Score hyotei = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
			        if (null != hyotei) {
			        	_svf.VrsOut("DIV", hyotei._score); // 評定
			        }
		        	final SubclassAttendance att = subclass.getAttendance(SEMEALL);
		        	if (null != att) {
	                    final String sick = null == att._sick ? " " : _param._df.format(att._sick);
		        		final String lesson = null == att._lesson ? " " : att._lesson.toString();
				        _svf.VrsOut("KEKKA", sick + " / " + lesson); // 欠課
		        	}
			        
			        _svf.VrsOut("JUDGE", subclass.getNinteiString(_param._semester, false)); // 判定
			        
				    if (!_param._cond18_1_noPrintChairStaffnameCD) {
				    	final String subclassTeacher = mkString(subclass.getStaffnameList(_param), "、");
						_svf.VrsOut("SUBCLASS_TEACHER" + (KNJ_EditEdit.getMS932ByteLength(subclassTeacher) <= 14 ? "" : "2"), subclassTeacher); // 科目担当
				    }

			        _svf.VrEndRecord();
			        subline += 1;
	            }
			    final int maxSubclass = 20;
			    for (int i = subline; i < maxSubclass; i++) {
			    	_svf.VrEndRecord();
			    }
			    _hasData = true;
		    }
		}
		
		private BigDecimal getKekkaLimit(final BigDecimal jugyoNissu) {
			if (null == jugyoNissu) {
				return null;
			}
			return jugyoNissu.multiply(new BigDecimal(_param._cond15_1_kekkaBunshi)).divide(new BigDecimal(_param._cond15_2_kekkaBunbo), 10, BigDecimal.ROUND_HALF_UP);
		}
		

		private void printE(final List<Student> studentList) {
		    final String form;
		    // 2学期
		    if (_param._cond21_1_hyoteiHyojiE) {
				form = "KNJD185W_5_1_2.frm";
		    } else {
				form = "KNJD185W_5_1.frm";
		    }
		    
		    final Map<String, TestItem> testitemMap = Param.getTestItemMap(Arrays.asList(_testItems));
			
		    for (int sti = 0; sti < studentList.size(); sti++) {
		    	final Student student = studentList.get(sti);
		    	
	            log.info(" schregno = " + student._schregno);
	            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

		    	setForm(form, 4);

		    	final String title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student._majorname) + "　成績通知表";
		    	if (getMS932ByteLength(title) > 70) {
		    		_svf.VrsOut("SCHOOL_NAME2", title); // 学校名
		    	} else {
		    		_svf.VrsOut("SCHOOL_NAME", title); // 学校名
		    	}

		    	_svf.VrsOut("COURSE_NAME" + (KNJ_EditEdit.getMS932ByteLength(student._coursecodename) <= 20 ? "1" : "2"), student._coursecodename); // コース名称
		    	
		    	final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
		    	_svf.VrsOut("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 名前
		    	
			    if (!StringUtils.isBlank(student._staffname2)) {
			    	_svf.VrsOut("TEACHER_NAME3_1", student._staffname); // 担任名
			    	_svf.VrsOut("TEACHER_NAME3_2", student._staffname2); // 担任名
			    } else {
			    	final int ketaStaffname = KNJ_EditEdit.getMS932ByteLength(student._staffname);
			    	_svf.VrsOut("TEACHER_NAME" + (ketaStaffname <= 22 ? "1" : ketaStaffname <= 30 ? "2" : "3"), student._staffname); // 担任名
			    }
		    	
		    	if (!_param._cond19_1_noPrintLateEarlyC) {
			    	_svf.VrsOut("CHIKOKU_KAISU", "遅刻回数");
			    	_svf.VrsOut("SOUTAI_KAISU", "早退回数");
		    	}
		    	if (!_param._cond12_1_noPrintRank) {
		    		for (int i = 0; i < _param._rankDiv.length; i++) {
		    			final String si = String.valueOf(i + 1);
		    			switch (_param._rankDiv[i]) {
		    			case COLUMN_ITEM_HR_RANK:
					    	_svf.VrsOut("RANK" + si + "_NAME", "クラス順位／人数");
		    				break;
		    			case COLUMN_ITEM_COURSE_RANK:
					    	_svf.VrsOut("RANK" + si + "_NAME", "コース順位／人数");
		    				break;
		    			case COLUMN_ITEM_GRADE_RANK:
					    	_svf.VrsOut("RANK" + si + "_NAME", "学年順位／人数");
		    				break;
		    			case COLUMN_ITEM_MAJOR_RANK:
					    	_svf.VrsOut("RANK" + si + "_NAME", "学科順位／人数");
		    				break;
		    			default:
		    				if (-1 != _param._rankDiv[i]) {
		    					log.warn("not found : " + _param._rankDiv[i]);
		    				}
		    			}
		    		}
		    	}
		    	for (int j = 0; j < 3; j++) {
		    		final String sj = String.valueOf(j + 1);
		    		final String semes = j + 1 == 3 ? SEMEALL : sj;
			        final Semester semester = _param.getSemester(semes);
			        if (null != semester) {
			        	_svf.VrsOut("SEMESTER_NAME1_" + sj, semester._semestername); // 学期名称
			    		_svf.VrsOutn("SEMESTER2", j + 1, semester._semestername); // 学期
			        }
		    	}
		    	if (NumberUtils.isDigits(student._gradeCd)) {
		    		_svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._gradeCd))); // 学年
		    	}
		    	final String hrname = StringUtils.defaultString(student._hrClassName1, student._hrname);
		    	if (getMS932ByteLength(hrname) > 4) {
		    		_svf.VrsOut("HR3_1", hrname); // クラス
		    	} else if (getMS932ByteLength(hrname) > 2) {
		    		_svf.VrsOut("HR2", hrname); // クラス
		    	} else {
		    		_svf.VrsOut("HR", hrname); // クラス
		    	}
		    	_svf.VrsOut("NO", student._attendnoZeroSuprpess); // 番号
		    	
	    		final Subclass subclassAll = student.getSubclass(SUBCLASSCD999999);
		    	for (int j = 0; j < 3; j++) {
		    		final int line = j + 1;
                    final String sj = String.valueOf(line);
		    		final String semes = line == 3 ? SEMEALL : sj;

		    		final String testcd = semes + "990008";
		    		if (null != subclassAll) {
		    			final Score score = subclassAll.getScore(testcd);
		    			if (null != score) {
		    				if (!_param._cond12_1_noPrintRank) {
		    					for (int divi = 0; divi < _param._rankDiv.length; divi++) {
		    						final String r = StringUtils.defaultString(score.getRank(_param._rankDiv[divi]).getRank(_param));
		    						final String z = StringUtils.defaultString(score.getRank(_param._rankDiv[divi])._count);
		    						_svf.VrsOutn("RANK" + String.valueOf(divi + 1), line, r + "／" + z); // 順位
		    					}
		    				}
		    				_svf.VrsOutn("TOTAL_AVERAGE", line, StringUtils.defaultString(score._score) + "／" + StringUtils.defaultString(sishaGonyu(score._avg))); // 総合平均点
		    			}
		    		}
		    		final String attendrecRemark = student._semesterAttendrecRemark.get(sj);
		    		_svf.VrsOutn("REMARK1", line, attendrecRemark); // 備考
		    		if (SEMEALL.equals(semes)) {
			    		_svf.VrsOutn("SUBTOTAL_GET_CREDIT", 3, student.getTotalGetCredit(_param, false, false)); // 合計修得単位数
		    		} else {
			    		_svf.VrsOutn("SUBTOTAL_GET_CREDIT", 3, student.getGetCredit(_param, semes, false)); // 合計修得単位数
		    		}
		    		
				    final Attendance att = student._attendMap.get(semes);
				    if (null != att) {
				    	_svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
				    	_svf.VrsOutn("MOURNING", line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
				    	_svf.VrsOutn("MUST", line, String.valueOf(att._mLesson)); // 出席しなければならない日数
				    	_svf.VrsOutn("ABSENT", line, String.valueOf(att._absent)); // 欠席日数
				    	_svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 欠席日数
				    	if (!_param._cond19_1_noPrintLateEarlyC) {
				    		_svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
				    		_svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
				    	}
				    }
		    	}
		    	
	            final List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
	            int subline = 0;
	            for (int i = 0; i < subclassList.size(); i++) {
	                final Subclass subclass = subclassList.get(i);
	                if (_param._isOutputDebug) {
	                    log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
	                }
	            	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 12) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._classname) <= 20) {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classcd); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME3_1", "X=10000"); // 教科名
	            	} else {
	            		_svf.VrsOut("CLASS_NAME", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME2", subclass._mst._classcd); // 教科名
	            		_svf.VrsOut("CLASS_NAME3_1", subclass._mst._classname); // 教科名
	            		_svf.VrAttribute("CLASS_NAME", "X=10000"); // 教科名
	            		_svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
	            	}
		    		
		    		_svf.VrsOut("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) <= 22 ? "1" : "2"), subclass._mst._subclassname); // 科目名
		    		
		    		_svf.VrsOut("CREDIT", credits(student, subclass)); // 単位数
		    		
		    		for (int j = 0; j < 3; j++) {
		    			final String sj = String.valueOf(j + 1);
		    			final String seme = j == 2 ? SEMEALL : sj;
		    			if (!SEMEALL.equals(seme) && j + 1 > Integer.parseInt(_param._semester)) {
		    				continue;
		    			}
		    			final String testcd = seme + "990008";
		    			final TestItem testItem = testitemMap.get(testcd);
		    			boolean slumpUseScore = false;
		    			boolean slumpUseMark = false;
		    			if (null != testItem) {
		    				slumpUseScore = SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf);
		    				slumpUseMark = SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf);
		    			}
						final Score score = subclass.getScore(testcd);
		    			if (null != score) {
		    				if (_param._cond20_1_tuishidouTeiseisenE && (slumpUseScore && null != score._slumpScore || slumpUseMark && null != score._slumpMark)) { // 追認点数あり
		    					if (null != score._score) {
		    						_svf.VrsOut("SCORE" + sj + "_2", score._score); // 点数等
		    						_svf.VrAttribute("SCORE" + sj + "_2", ATTRIBUTE_TEISEISEN); // 点数等 訂正
		    					}
		    					if (slumpUseScore && null != score._slumpScore) {
			    					_svf.VrsOut("SCORE" + sj + "_3", score._slumpScore); // 点数等
		    					} else if (slumpUseMark && null != score._slumpMark) {
			    					_svf.VrsOut("SCORE" + sj + "_3", score._slumpMarkName1); // 点数等
		    					}
		    				} else if (slumpUseScore && null != score._slumpScore || slumpUseMark && null != score._slumpMark) {
		    					if (slumpUseScore && null != score._slumpScore) {
			    					_svf.VrsOut("SCORE" + sj + "_1", score._slumpScore); // 点数等
		    					} else if (slumpUseMark && null != score._slumpMark) {
			    					_svf.VrsOut("SCORE" + sj + "_1", score._slumpMarkName1); // 点数等
		    					}
		    				} else {
		    					_svf.VrsOut("SCORE" + sj + "_1", score._score); // 点数等
		    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score._score) && Integer.parseInt(score._score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
		    						_svf.VrAttribute("SCORE" + sj + "_1", ATTRIBUTE_TUISHIDO); // 点数等
		    					}
		    				}
		    			}
		    			final SubclassAttendance subatt = subclass.getAttendance(seme);
		    			if (null != subatt && null != subatt._sick) {
		    				_svf.VrsOut("KEKKA" + sj, _param._df.format(subatt._sick)); // 欠課
		    				if (null != subatt._lesson) {
		    					final BigDecimal limit = getKekkaLimit(subatt._lesson);
		    					if (limit.doubleValue() < subatt._sick.doubleValue()) { // 1/3を超えると網掛け
				    				_svf.VrAttribute("KEKKA" + sj, ATTRIBUTE_TUISHIDO); // 欠課
		    					}
		    				}
		    			}
		    		}
		    		if (_param._cond21_1_hyoteiHyojiE) {
		    			if (SEMEALL.equals(_param._semester)) {
		    				final Score score = subclass.getScore("9990009");
		    				final String sj = "4";
		    				if (null != score) {
		    					if (_param._cond20_1_tuishidouTeiseisenE && null != score._slumpScore) { // 追認点数あり
		    						if (null != score._score) {
		    							_svf.VrsOut("SCORE" + sj + "_2", score._score); // 点数等
		    							_svf.VrAttribute("SCORE" + sj + "_2", ATTRIBUTE_TEISEISEN); // 点数等 訂正
		    						}
		    						_svf.VrsOut("SCORE" + sj + "_3", score._slumpScore); // 点数等
		    					} else if (null != score._slumpScore) {
		    						_svf.VrsOut("SCORE" + sj + "_1", score._slumpScore); // 点数等
		    					} else {
		    						_svf.VrsOut("SCORE" + sj + "_1", score._score); // 点数等
		    					}
		    				}
		    			}
		    		}
		    		
	    			_svf.VrsOut("JUDGE", subclass.getNinteiString(_param._semester, false)); // 認定
		    		_svf.VrEndRecord();
		    		subline += 1;
	            }
		    	final int maxSubclass = 20;
		    	for (int i = subline; i < maxSubclass; i++) {
		    		_svf.VrEndRecord();
		    	}
		    	_hasData = true;
		    }
		}

		private List<LineItem> createLineItemList(final int max) {
            final List<LineItem> list = new ArrayList();
            list.add(new LineItem(LINE_ITEM_TANNI, null, null));
            TestItem last = null;
            Set kekkaSemester = new HashSet();
            kekkaSemester.add(SEMEALL);
            for (int i = 0; i < _testItems.length; i++) {
                if (null == _testItems[i]) {
                    continue;
                }
                if (null != last && null != last._semester && !last._semester.equals(_testItems[i]._semester)) {
                    for (final String cdSemester : _param._semesterMap.keySet()) {
                        if (last._semester.compareTo(cdSemester) <= 0 && cdSemester.compareTo(_testItems[i]._semester) < 0 && !kekkaSemester.contains(cdSemester)) {
                            list.add(new LineItem(LINE_ITEM_KEKKA, cdSemester, null));
                            kekkaSemester.add(cdSemester);
                        }
                    }
                }
                list.add(new LineItem(LINE_ITEM_SCORE, _testItems[i]._semester, _testItems[i]));
                last = _testItems[i];
            }
            if (null != last && null != last._semester) {
                for (final String cdSemester : _param._semesterMap.keySet()) {
                    if (last._semester.compareTo(cdSemester) <= 0 && !kekkaSemester.contains(cdSemester)) {
                        list.add(new LineItem(LINE_ITEM_KEKKA, cdSemester, null));
                        kekkaSemester.add(cdSemester);
                    }
                }
            }
            return list;
        }
		
		private Object getItemPrintValue(final ColumnItem columnItem, final int lineItemKind, final LineItem lineItem, final String paramKekkaKey, final Student student, final Subclass subclass) {
            if (null == subclass) {
                return null;
            }
            Object rtn = null;
            switch (lineItemKind) {
            case LINE_ITEM_TANNI:
                if (null == columnItem) {
                    rtn = credits(student, subclass);
                }
                break;
            case LINE_ITEM_SCORE:
                final Score s = subclass.getScore(lineItem._testItem.testcd());
                if (null == s) {
                    return null;
                }
                if (null == columnItem) {
                	if (TESTCD_GAKUNEN_HYOTEI.equals(lineItem._testItem.testcd())) {
                		rtn = StringUtils.defaultString(s._assessmark, s._score);
                	} else {
                		rtn = s._score;
                	}
                } else {
                    switch (columnItem._kind) {
                    case COLUMN_ITEM_TOTAL:
                        rtn = s._score;
                        break;
                    case COLUMN_ITEM_HR_AVG:
                        rtn = sishaGonyu(s._hrRank._avg);
                        break;
                    case COLUMN_ITEM_HR_RANK:
                        rtn = s._hrRank.getRank(_param);
                        break;
                    case COLUMN_ITEM_COURSE_AVG:
                        rtn = sishaGonyu(s._courseRank._avg);
                        break;
                    case COLUMN_ITEM_COURSE_RANK:
                        rtn = s._courseRank.getRank(_param);
                        break;
                    case COLUMN_ITEM_GRADE_AVG:
                        rtn = sishaGonyu(s._gradeRank._avg);
                        break;
                    case COLUMN_ITEM_GRADE_RANK:
                        rtn = s._gradeRank.getRank(_param);
                        break;
                    case COLUMN_ITEM_MAJOR_AVG:
                        rtn = sishaGonyu(s._majorRank._avg);
                        break;
                    case COLUMN_ITEM_MAJOR_RANK:
                        rtn = s._majorRank.getRank(_param);
                        break;
                    }
                }
                if (_param._isOutputDebug) {
                    log.info(" score lineItem = " + lineItem + ", " + (null == lineItem || null == lineItem._testItem ? "" : lineItem._testItem.toString()) + " => " + rtn);
                }
                break;
            case LINE_ITEM_KEKKA:
                String kekkaKey = paramKekkaKey;
                if (_param._isOutputDebug) {
                    log.info(" lineItem = " + lineItem + ", " + (null == lineItem || null == lineItem._testItem ? "" : lineItem._testItem.toString()));
                }
                if (null == kekkaKey && null != lineItem) {
                    if (null != lineItem._testItem) {
                        if (lineItem._testItem.testcd().substring(1).startsWith("99")) {
                            kekkaKey = lineItem._testItem._semester;
                        } else {
                            kekkaKey = lineItem._testItem._dateRange._key;
                        }
                    } else {
                        kekkaKey = lineItem._semester;
                    }
                }
                if (_param._isOutputDebug) {
                    log.info(" kekkaKey = " + kekkaKey + " / " + subclass);
                }
                if (null == kekkaKey) {
                    return null;
                }
                final SubclassAttendance sa = subclass._attendMap.get(kekkaKey);
                if (null == sa) {
                    if (_param._isOutputDebug) {
                        log.info(" null at " + kekkaKey + " / " + subclass._attendMap);
                    }
                    return null;
                }
                rtn = kekkaString(_param, sa);
                break;
            }
            if (null == columnItem) {
            	if (!_param._isOutputDebug) {
            		log.warn(" rtn = " + rtn + ", lineItemKind = " + lineItemKind + ", columnItem = " + columnItem + ", subclass = " + subclass);
            	}
            }
            return rtn;
        }
		
		private static Object kekkaString(final Param param, final SubclassAttendance sa) {
            if (null == sa) {
                return null;
            }
            Object rtn = null;
//            if ("1".equals(param._kekkaDisp)) {
                if (null != sa._sick) {
                    rtn = param._df.format(sa._sick);
                }
//            } else if ("2".equals(param._kekkaDisp)) {
//                if (null != sa._sick || null != sa._lesson) {
//                    final Map m = new HashMap();
//                    String bunsi = null;
//                    if (null != sa._sick) {
//                        final DecimalFormat df = null != param._knjSchoolMst && ("3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");
//                        bunsi = df.format(sa._sick);
//                    }
//                    m.put("_BUNSI", bunsi);
//                    m.put("_SLASH", "/");
//                    m.put("_BUNBO", null == sa._lesson ? null : sa._lesson.setScale(0, BigDecimal.ROUND_DOWN).toString());
//                    rtn = m;
//                }
//            } else if ("3".equals(param._kekkaDisp)) {
//                if (null != sa._lesson || null != sa._attend) {
//                    String bunsi = null;
//                    if (null != sa._sick) {
//                        if (sa._attend.setScale(0, BigDecimal.ROUND_DOWN).equals(sa._attend)) {
//                            bunsi = sa._attend.setScale(0, BigDecimal.ROUND_DOWN).toString();
//                        } else {
//                            bunsi = sa._attend.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                        }
//                    }
//                    final Map m = new HashMap();
//                    m.put("_BUNSI", bunsi);
//                    m.put("_SLASH", "/");
//                    m.put("_BUNBO", null == sa._lesson ? null : sa._lesson.setScale(0, BigDecimal.ROUND_DOWN).toString());
//                    rtn = m;
//                }
//            }
            return rtn;
        }

        private void printF(final List<Student> studentList) {
			
		    final String form;
		    final int testline;
		    final int maxSemester;
			// 3学期
			form = "KNJD185W_6_2.frm";
			testline = 7;
			maxSemester = 4;

	    	for (int sti = 0; sti < studentList.size(); sti++) {
		    	final Student student = studentList.get(sti);
		    	
	            log.info(" schregno = " + student._schregno);
	            init(_param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));

	            setForm(form, 4);

			    _svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 30 ? "1" : "2"), student._name); // 名前

			    //_svf.VrsOut("TITLE", nendo() + "　" + "　成績通知表"); // タイトル
	            _svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student.getRegdInfo(_param))); // 学校名
			    
	            final String subtotal = student.getTotalGetCredit(_param, false, false);
	            _svf.VrsOut("SUBTOTAL_GET_CREDIT", subtotal); // 合計修得単位数
	            if (NumberUtils.isDigits(subtotal) || student._previousCredits > 0) {
	            	final String total = String.valueOf((NumberUtils.isDigits(subtotal) ? Integer.parseInt(subtotal) : 0) + student._previousCredits);
	            	_svf.VrsOut("TOTAL_GET_CREDIT", total); // 累計修得単位数
	            }

	            _svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
			    if (!StringUtils.isBlank(student._staffname2)) {
				    _svf.VrsOut("TEACHER_NAME2", student._staffname); // 担任名
				    _svf.VrsOut("TEACHER_NAME3", student._staffname2); // 担任名
			    } else {
			    	_svf.VrsOut("TEACHER_NAME", student._staffname); // 担任名
			    }

		    	if (!_param._cond19_1_noPrintLateEarlyC) {
			    	_svf.VrsOut("CHIKOKU_KAISU", "遅刻");
			    	_svf.VrsOut("SOUTAI_KAISU", "早退");
		    	}

	            vrsOutnRepeat("REMARK", getTokenList(student._remark, 30, 12)); // 備考

			    if (_param._cond07_1_noPrintTokubetsuKatsudouABF) {
			    	whitespace(_svf, "IMG_SP");
			    } else {
			    	vrsOutnRepeat("CLUB", KNJ_EditKinsoku.getTokenList(student._semesterClub.get(SEMEALL), 36)); // クラブ 4行
				    vrsOutnRepeat("COMMITTEE", KNJ_EditKinsoku.getTokenList(student._semesterCommittee.get(SEMEALL), 36)); // 委員会 4行
			    }
			    
			    if (_param._cond08_1_noPrintSogoABF) {
			    	whitespace(_svf, "IMG_SOGO");
			    } else {
			    	
                    int moji = getParamSizeNum(_param._RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H, 0);
                    int gyo = getParamSizeNum(_param._RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H, 1);
                    if (-1 == moji || -1 == gyo) {
                        moji = 30;
                    }
                    gyo = 7;
                    final List totalstudyLineList = new ArrayList();
                    totalstudyLineList.addAll(KNJ_EditKinsoku.getTokenList(student._totalstudytime, moji * 2));
//                    final String totalStudyact = student._semesterTotalStudyactMap.get(SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester);
//                    if (null != totalStudyact) {
//                        totalstudyLineList.add("＜学習内容＞");
//                        totalstudyLineList.addAll(KNJ_EditKinsoku.getTokenList(totalStudyact, moji * 2));
//                    }
//                    final String totalStudytime = student._semesterTotalStudytimeMap.get(SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester);
//                    if (null != totalStudytime) {
//                        totalstudyLineList.add("＜評価＞");
//                        totalstudyLineList.addAll(KNJ_EditKinsoku.getTokenList(totalStudytime, moji * 2));
//                    }
                    final StringBuffer totalStudy = new StringBuffer();
                    for (final Iterator it = totalstudyLineList.iterator(); it.hasNext();) {
                        if (totalStudy.length() != 0) {
                            totalStudy.append("\n");
                        }
                        totalStudy.append(it.next());
                    }
                    //vrsOutnRepeat(moji > 30 ? "TOTAL_ACT2" : "TOTAL_ACT", getTokenList(totalStudy.toString(), moji * 2, gyo)); // 総学
                    vrsOutnRepeat("TOTAL_ACT", getTokenList(totalStudy.toString(), moji * 2, gyo)); // 総学
			    }
			    vrsOutnRepeat("REMARK", KNJ_EditKinsoku.getTokenList(student._remark, 30)); // 備考, 15行 ???

			    for (int j = 0; j < maxSemester + 1; j++) {
			        final int line = j + 1;
			        final String semes;
	                if (line == maxSemester) {
	                	semes = SEMEALL;
	                    _svf.VrsOutn("SEMESTER_NAME", line, "合計"); // 学期名称
	                } else {
	                	semes = String.valueOf(line);
	                    final Semester semester = _param.getSemester(semes);
	                    if (null != semester) {
	                        _svf.VrsOutn("SEMESTER_NAME", line, semester._semestername); // 学期名称
	                    }
	                }
				    final Attendance att = student._attendMap.get(semes);
				    if (null != att) {
				    	_svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
				    	_svf.VrsOutn("MOURNING", line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
				    	_svf.VrsOutn("MUST", line, String.valueOf(att._mLesson)); // 出席しなければならない日数
				    	_svf.VrsOutn("ABSENT", line, String.valueOf(att._absent)); // 欠席日数
				    	_svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 欠席日数
				    	if (!_param._cond19_1_noPrintLateEarlyC) {
				    		_svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
				    		_svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
				    	}
				    }
			    }
	            
			    final int itemLineMax = testline + 1;
	            final List<LineItem> lineItems = createLineItemList(itemLineMax);

	            for (int i = 0; i < _param._columnItemList.size(); i++) {
	                final ColumnItem item = _param._columnItemList.get(i);
	                _svf.VrsOut("ITEM2_" + String.valueOf(i + 1), item._itemName); // 項目名
	            }
	            for (int j = 0; j < itemLineMax; j++) {
	                if (j >= lineItems.size()) {
	                    continue;
	                }
	                final LineItem lineItem = lineItems.get(j);
	                final int line = j + 1;
	                for (int i = 0; i < _param._columnItemList.size(); i++) {
	                    final ColumnItem columnItem = _param._columnItemList.get(i);
	                    _svf.VrsOutn("SCORE2_" + String.valueOf(i + 1), line, (String) getItemPrintValue(columnItem, lineItem._kind, lineItem, null, student, student.getSubclass(SUBCLASSCD999999))); // 点数等
	                }
	            }

	            for (int j = 0; j < itemLineMax; j++) {
	                if (j >= lineItems.size()) {
	                    continue;
	                }
	                final LineItem lineItem = lineItems.get(j);
	                _svf.VrsOutn("ITEM_NAME1", j + 1, lineItem.getLineName(_param)); // 得点/評価/欠課名
	            }

	            final List<Subclass> subclassList = Subclass.getPrintSubclassList(_param, student);
	            int subline = 0;
	            for (int i = 0; i < subclassList.size(); i++) {
	            	final Subclass subclass = subclassList.get(i);
	            	if (_param._isOutputDebug) {
	            		log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
	            	}
	            	final String classname = CLASSCD_90.equals(subclass._mst._classcd) ? "　　　　　　　　　　A" : subclass._mst._classname;
	            	if (KNJ_EditEdit.getMS932ByteLength(classname) <= 8) {
	            		_svf.VrsOut("CLASS_NAME1", classname); // 教科名
	            		_svf.VrsOut("CLASS_NAME2_1", subclass._mst._classcd);
	            		_svf.VrsOut("CLASS_NAME2_2", subclass._mst._classcd);
	            		_svf.VrAttribute("CLASS_NAME2_1", "X=10000");
	            		_svf.VrAttribute("CLASS_NAME2_2", "X=10000");
	            	} else {
	            		final String[] token = KNJ_EditEdit.get_token(classname, 10, 2);
	            		for (int j = 0; j < token.length; j++) {
	            			
	            			_svf.VrsOut("CLASS_NAME2_" + String.valueOf(j + 1), token[j]); // 教科名
	            		}
	            		_svf.VrsOut("CLASS_NAME1", subclass._mst._classcd);
	            		_svf.VrAttribute("CLASS_NAME1", "X=10000");
	            	}
					final int subclassnameLen = StringUtils.defaultString(subclass._mst._subclassname).length();
					_svf.VrsOut("SUBCLASS_NAME" + (subclassnameLen <= 10 ? "1" : subclassnameLen <= 13 ? "2" : "3"), subclassnameLen > 15 ? subclass._mst._subclassname.substring(0, 15) : subclass._mst._subclassname); // 科目名

					for (int j = 0; j < itemLineMax; j++) {
						if (j >= lineItems.size()) {
							continue;
						}
						final LineItem lineItem = (LineItem) lineItems.get(j);
						if (_param._isOutputDebug) {
							log.info(" print record " + lineItem);
						}
						final Object o = getItemPrintValue(null, lineItem._kind, lineItem, null, student, subclass);
						if (o instanceof String) {
							_svf.VrsOutn("SCORE1", j + 1, o.toString()); // 点数等
//						} else if (o instanceof Map) {
//							for (final Iterator it = ((Map) o).entrySet().iterator(); it.hasNext();) {
//								final Map.Entry e = (Map.Entry) it.next();
//								final String field = (String) e.getKey();
//								final String value = (String) e.getValue();
//								_svf.VrsOutn("SCORE1" + field, j + 1, value); // 点数等
//							}
						}
						if (LINE_ITEM_SCORE == lineItem._kind && null != o && null != lineItem._testItem && !TESTCD_GAKUNEN_HYOTEI.equals(lineItem._testItem.testcd())) {
							final String score = o.toString();
	    					if (null != _param._cond14_1TuishidouAmikakeKakko && NumberUtils.isDigits(score) && Integer.parseInt(score) <= _param._cond14_1TuishidouAmikakeKakko.intValue()) {
								_svf.VrAttributen("SCORE1", j + 1, ATTRIBUTE_TUISHIDO); // 点数等
	    					}
						}
					}

					_svf.VrsOut("COMP_CREDIT", subclass._compCredit); // 履修単位数
					_svf.VrsOut("GET_CREDIT", subclass._getCredit); // 修得単位数
					if (SEMEALL.equals(_param._semester)) {
						final Object o = getItemPrintValue(null, LINE_ITEM_KEKKA, null, SEMEALL, student, subclass);
						if (o instanceof String) {
							_svf.VrsOut("KEKKA", o.toString()); // 欠課数
//						} else if (o instanceof Map) {
//							for (final Iterator it = ((Map) o).entrySet().iterator(); it.hasNext();) {
//								final Map.Entry e = (Map.Entry) it.next();
//								final String field = (String) e.getKey();
//								final String value = (String) e.getValue();
//								_svf.VrsOut("KEKKA" + field, value);  // 欠課数
//							}
						}
					}
					_svf.VrEndRecord();
					subline += 1;
	            }
			    final int maxSubclass = 20;
	            if (0 == subline || subline % maxSubclass != 0) {
	                for (int i = subline % maxSubclass; i < maxSubclass; i++) {
	                    final String classname = "1";
	                    _svf.VrsOut("CLASS_NAME1", classname); // 教科名
	                    _svf.VrAttribute("CLASS_NAME1", "X=10000");
	                    _svf.VrEndRecord();
	                }
	            }
			    _hasData = true;
		    }
		}
	}
    
    private static class ColumnItem {
        final int _kind;
        final String _itemName;
        ColumnItem(final int kind, final String itemName) {
            _kind = kind;
            _itemName = itemName;
        }
        public String toString() {
            return "Column(" + _itemName + ")";
        }
    }

    private static class LineItem {
        final int _kind;
        final String _semester;
        final TestItem _testItem;
        LineItem(final int kind, final String semester, final TestItem testItem) {
            _kind = kind;
            _semester = semester;
            _testItem = testItem;
        }
        public String getLineName(final Param param) {
            switch (_kind) {
            case LINE_ITEM_TANNI:
                return "単位数";
            case LINE_ITEM_SCORE:
                return _testItem._testitemname;
            case LINE_ITEM_KEKKA:
                return StringUtils.defaultString((param._semesterMap.get(_semester))._semestername) + "欠課数";
            }
            return null;
        }
        public String toString() {
            switch (_kind) {
            case LINE_ITEM_TANNI:
                return "単位数";
            case LINE_ITEM_SCORE:
                return _testItem._testitemname;
            case LINE_ITEM_KEKKA:
                return StringUtils.defaultString(_semester) + "欠課数";
            }
            return null;
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75974 $ $Date: 2020-08-12 17:53:13 +0900 (水, 12 8 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _loginDate;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        
        final Map _hreportConditionDat;

//        final String _rankDiv; // 1:総合点 2:平均点
//        final String _kekkaDisp; // 1:欠課数 2:欠課数／時間数 3:出席数／時数
//        final String _isPrintspDisp;
//        final String _isPrintSogoKentei;
//        final String _isPrintRemark;
//        final String _isPrintTeikeiComment;
//        final String _isPrintmirisyuDisp;
        final String _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H;
        final String _documentroot;
        final String _imagepath;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;

        final String _schoolKind;

        final String _tutisyoPrintKariHyotei; // 仮評定を表示する
        final String _tutihyoUseSubclassElectdivAsShoworder; // 科目マスタ選択科目区分を表示順に使用する。
        final String _tutihyoPrintSubclasscd90Over; // 教科コード91以上の科目を表示する
        final String _knjd185wNotPrintAttendSubclassDatLesson;
        final String _knjd185wNotPrintNotPrintZenkiKamokuInSemester2;
        final boolean _isPrintCreditSemester2OnlyInSemester2;

        final boolean _isSeireki;
        private final Form _form;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;
        final String _whitespaceImagePath;

        private Map<String, Semester> _semesterMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private Map _creditMst;
        private Map _recordMockOrderSdivDatMap;
        private List<TestItem> _allTestItemList;
        private List<DateRange> _allSemesterDetailList;
        private Map<String, TestItem> _testItemMap;

        private KNJSchoolMst _knjSchoolMst;

//        private String _d054Namecd2Max;
//        private String _sidouHyoji;
        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;
        private boolean _isNoPrintMoto;
        private List _d026List = Collections.EMPTY_LIST;
        private List _e065JiritsuKaudouName1List = Collections.EMPTY_LIST;
        private List<ColumnItem> _columnItemList;

        private final DecimalFormat _df;
        private final boolean _isLastSemester;
        
        private int[] _rankDiv = {};
        
        final String _nendo;
        
        /** 帳票出力 1:A 2:B 3:C 4:D 5:E 6:F */
        final String _cond01_1TyohyoPattern;

        /** 順位の基準点ラジオボタン 1:総合点 2:平均点 */
        final String _cond05_1SogoOrHeikin;

        /** 席次表記 */
        final boolean _cond06_1_isPrintRankHr;
        final boolean _cond06_2_isPrintRankCourse;
        final boolean _cond06_3_isPrintRankGrade;
        final boolean _cond06_4_isPrintRankMajor;

        /** 順位表記 表記なし */
        final boolean _cond12_1_noPrintRank;
        /** 追指導表示 評点を網掛けして括弧表示する */
        final boolean _cond13_1_tuishidouAmikakeKakko;
        /** 欠点表示 X点以下を網掛けして表示する */
        final Integer _cond14_1TuishidouAmikakeKakko;
        /** 欠課時数 分子 */
        final int _cond15_1_kekkaBunshi;
        /** 欠課時数 分母 */
        final int _cond15_2_kekkaBunbo;

        /** 出力順設定 裁断用にソートして出力する */
        final boolean _cond16_1_saidanyouSortAB;
        /** 総合的な学習の時間 表記なし */
        final boolean _cond08_1_noPrintSogoABF;
        /** 特別活動 表記なし */
        final boolean _cond07_1_noPrintTokubetsuKatsudouABF;
        /** 所見欄  表記なし */
        final boolean _cond17_1_noPrintShokenABCD;
        /** 科目担当教員 表記なし */
        final boolean _cond18_1_noPrintChairStaffnameCD;
        /** 出欠の記録 遅刻・早退回数表記なし */ // OK
        final boolean _cond19_1_noPrintLateEarlyC;
        /** 追指導表示 追指導前得点に訂正線を付けて、追指導後得点を併記する */
        final boolean _cond20_1_tuishidouTeiseisenE;
        /** 評定表示 */
        final boolean _cond21_1_hyoteiHyojiE;
        /** 増加単位加算する  */
        final boolean _cond22_1_addZoukaTanni;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("LOGIN_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            
            _hreportConditionDat = getHreportConditionDat(db2);
            _cond01_1TyohyoPattern = StringUtils.defaultString(getString(getMappedMap(_hreportConditionDat, "001"), "REMARK1"), "1");
            _cond05_1SogoOrHeikin = StringUtils.defaultString(getString(getMappedMap(_hreportConditionDat, "005"), "REMARK1"), "1");
            _cond06_1_isPrintRankHr = "1".equals(getString(getMappedMap(_hreportConditionDat, "006"), "REMARK1"));
            _cond06_2_isPrintRankCourse = "1".equals(getString(getMappedMap(_hreportConditionDat, "006"), "REMARK2"));
            _cond06_3_isPrintRankGrade = "1".equals(getString(getMappedMap(_hreportConditionDat, "006"), "REMARK3"));
            _cond06_4_isPrintRankMajor = "1".equals(getString(getMappedMap(_hreportConditionDat, "006"), "REMARK4"));

            _cond12_1_noPrintRank = "1".equals(getString(getMappedMap(_hreportConditionDat, "012"), "REMARK1"));
            _cond13_1_tuishidouAmikakeKakko = "1".equals(getString(getMappedMap(_hreportConditionDat, "013"), "REMARK1"));
            final String _014_1 = getString(getMappedMap(_hreportConditionDat, "014"), "REMARK1");
            if (NumberUtils.isNumber(_014_1)) {
            	_cond14_1TuishidouAmikakeKakko = new Integer((int) Double.parseDouble(_014_1));
            } else if (_hreportConditionDat.containsKey("014")) {
            	_cond14_1TuishidouAmikakeKakko = null;
        	} else {
            	_cond14_1TuishidouAmikakeKakko = new Integer(40);
            }
            final String _015_1 = getString(getMappedMap(_hreportConditionDat, "015"), "REMARK1");
			_cond15_1_kekkaBunshi = !NumberUtils.isNumber(_015_1) ? 1 : ((int) Double.parseDouble(_015_1));
            final String _015_2 = getString(getMappedMap(_hreportConditionDat, "015"), "REMARK2");
			_cond15_2_kekkaBunbo = !NumberUtils.isNumber(_015_2) || Double.parseDouble(_015_2) == 0.0 ? 3 : ((int) Double.parseDouble(_015_2));
            _cond16_1_saidanyouSortAB = "1".equals(getString(getMappedMap(_hreportConditionDat, "016"), "REMARK1"));
            _cond08_1_noPrintSogoABF = "1".equals(getString(getMappedMap(_hreportConditionDat, "008"), "REMARK1"));
            _cond07_1_noPrintTokubetsuKatsudouABF = "1".equals(getString(getMappedMap(_hreportConditionDat, "007"), "REMARK1"));
            _cond17_1_noPrintShokenABCD = "1".equals(getString(getMappedMap(_hreportConditionDat, "017"), "REMARK1"));
            _cond18_1_noPrintChairStaffnameCD = "1".equals(getString(getMappedMap(_hreportConditionDat, "018"), "REMARK1"));
            _cond19_1_noPrintLateEarlyC = "1".equals(getString(getMappedMap(_hreportConditionDat, "019"), "REMARK1"));
            _cond20_1_tuishidouTeiseisenE = "1".equals(getString(getMappedMap(_hreportConditionDat, "020"), "REMARK1"));
            _cond21_1_hyoteiHyojiE = "1".equals(getString(getMappedMap(_hreportConditionDat, "021"), "REMARK1"));
            _cond22_1_addZoukaTanni = "1".equals(getString(getMappedMap(_hreportConditionDat, "022"), "REMARK1"));

            _tutisyoPrintKariHyotei = null; // request.getParameter("tutisyoPrintKariHyotei");
            _tutihyoUseSubclassElectdivAsShoworder = request.getParameter("tutihyoUseSubclassElectdivAsShoworder");
            _tutihyoPrintSubclasscd90Over = request.getParameter("tutihyoPrintSubclasscd90Over");
            _knjd185wNotPrintAttendSubclassDatLesson = request.getParameter("knjd185wNotPrintAttendSubclassDatLesson");
            _knjd185wNotPrintNotPrintZenkiKamokuInSemester2 = request.getParameter("knjd185wNotPrintNotPrintZenkiKamokuInSemester2");
            _isPrintCreditSemester2OnlyInSemester2 = "2".equals(_semester) && "1".equals(_knjd185wNotPrintNotPrintZenkiKamokuInSemester2) && PATTERN_A.equals(_cond01_1TyohyoPattern);
            if (_isOutputDebug) {
            	log.info(" _isPrintCreditSemester2OnlyInSemester2 = " + _isPrintCreditSemester2OnlyInSemester2);
            }

//            _kekkaDisp = request.getParameter("SEQ004");
//            _rankDiv = request.getParameter("SEQ005");
//            _isPrintspDisp = request.getParameter("SEQ007");
//            _isPrintSogoKentei = request.getParameter("SEQ008");
//            _isPrintRemark = request.getParameter("SEQ009");
//            _isPrintTeikeiComment = request.getParameter("SEQ010");
//            _isPrintmirisyuDisp = request.getParameter("SEQ011");
            _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H = request.getParameter("RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H");
            _documentroot = request.getParameter("DOCUMENTROOT");

            _nendo = nendo(db2);
            _schoolKind = getSchoolKind(db2);

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;

            try {
            	final Map paramMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		paramMap.put("SCHOOL_KIND", "H");
            	}
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _df = null != _knjSchoolMst && ("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");
            _isLastSemester = null != _knjSchoolMst && _semester.equals(_knjSchoolMst._semesterDiv);

//            setD054Namecd2Max(db2);
            _e065JiritsuKaudouName1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'E065' ORDER BY NAMECD2 "), "NAME1");
            _subclassMstMap = SubclassMst.getSubclassMst(db2, this, _year);
//            setCreditMst(db2);
            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);
            loadNameMstD026(db2);
            _allTestItemList = getAllTestItems(db2);
            _allSemesterDetailList = getSemesterDetails(db2);
            _testItemMap = getTestItemMap(_allTestItemList);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));

            if (PATTERN_A.equals(_cond01_1TyohyoPattern) || PATTERN_B.equals(_cond01_1TyohyoPattern) || PATTERN_C.equals(_cond01_1TyohyoPattern) || PATTERN_D.equals(_cond01_1TyohyoPattern) || PATTERN_E.equals(_cond01_1TyohyoPattern)) {
            	if (!_cond12_1_noPrintRank) {
            		if (PATTERN_A.equals(_cond01_1TyohyoPattern) || PATTERN_B.equals(_cond01_1TyohyoPattern)) {
            			_rankDiv = new int[] {-1, -1, -1};
            		} else if (PATTERN_C.equals(_cond01_1TyohyoPattern) || PATTERN_D.equals(_cond01_1TyohyoPattern)) {
            			_rankDiv = new int[] {-1};
            		} else if (PATTERN_E.equals(_cond01_1TyohyoPattern)) {
            			_rankDiv = new int[] {-1, -1};
            		}
            		int rankIdx = 0;
            		if (_cond06_1_isPrintRankHr) {
            			if (rankIdx < _rankDiv.length) {
            				_rankDiv[rankIdx++] = COLUMN_ITEM_HR_RANK;
            			}
            		}
            		if (_cond06_2_isPrintRankCourse) {
            			if (rankIdx < _rankDiv.length) {
            				_rankDiv[rankIdx++] = COLUMN_ITEM_COURSE_RANK;
            			}
            		}
            		if (_cond06_3_isPrintRankGrade) {
            			if (rankIdx < _rankDiv.length) {
            				_rankDiv[rankIdx++] = COLUMN_ITEM_GRADE_RANK;
            			}
            		}
            		if (_cond06_4_isPrintRankMajor) {
            			if (rankIdx < _rankDiv.length) {
            				_rankDiv[rankIdx++] = COLUMN_ITEM_MAJOR_RANK;
            			}
            		}
            		if (rankIdx == 0) {
            			if (PATTERN_A.equals(_cond01_1TyohyoPattern) || PATTERN_B.equals(_cond01_1TyohyoPattern)) {
            				_rankDiv[0] = COLUMN_ITEM_HR_RANK;
            				_rankDiv[1] = COLUMN_ITEM_COURSE_RANK;
            				_rankDiv[2] = COLUMN_ITEM_GRADE_RANK;
            			} else if (PATTERN_C.equals(_cond01_1TyohyoPattern) || PATTERN_D.equals(_cond01_1TyohyoPattern)) {
            				_rankDiv[0] = COLUMN_ITEM_HR_RANK;
            			} else if (PATTERN_E.equals(_cond01_1TyohyoPattern)) {
            				_rankDiv[0] = COLUMN_ITEM_HR_RANK;
            				_rankDiv[1] = COLUMN_ITEM_COURSE_RANK;
            			}
            		}
            	}
                log.info(" rankDiv = " + ArrayUtils.toString(_rankDiv));
            } else if (PATTERN_F.equals(_cond01_1TyohyoPattern)) {
            	_columnItemList = new ArrayList();
            	_columnItemList.add(new ColumnItem(COLUMN_ITEM_TOTAL, "総点"));
            	if (_cond06_1_isPrintRankHr) {
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_HR_AVG, "学級平均"));
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_HR_RANK, "学級順位"));
            	}
            	if (_cond06_2_isPrintRankCourse) {
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_COURSE_AVG, "コース平均"));
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_COURSE_RANK, "コース順位"));
            	}
            	if (_cond06_3_isPrintRankGrade) {
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_GRADE_AVG, "学年平均"));
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_GRADE_RANK, "学年順位"));
            	}
            	if (_cond06_4_isPrintRankMajor) {
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_MAJOR_AVG, "学科平均"));
            		_columnItemList.add(new ColumnItem(COLUMN_ITEM_MAJOR_RANK, "学科順位"));
            	}
            }

            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));

            _whitespaceImagePath = getImageFilePath("whitespace.png");

            if (_isOutputDebug) {
            	log.info(" _cond01_1TyohyoPattern      = " + _cond01_1TyohyoPattern);
            	log.info(" _cond05_1SogoOrHeikin       = " + _cond05_1SogoOrHeikin);
            	log.info(" _cond06_1_isPrintRankHr     = " + _cond06_1_isPrintRankHr);
            	log.info("         2_isPrintRankCourse = " + _cond06_2_isPrintRankCourse);
            	log.info("         3_isPrintRankGrade  = " + _cond06_3_isPrintRankGrade);
            	log.info("         4_isPrintRankMajor  = " + _cond06_4_isPrintRankMajor);
            	log.info(" _cond07_1_noPrintTokubetsuKatsudouABF = " + _cond07_1_noPrintTokubetsuKatsudouABF);
            	log.info(" _cond08_1_noPrintSogoABF    = " + _cond08_1_noPrintSogoABF);
            	log.info(" _cond12_1_noPrintRank       = " + _cond12_1_noPrintRank);
            	log.info(" _cond13_1_tuishidouAmikakeKakko = " + _cond13_1_tuishidouAmikakeKakko);
            	log.info(" _cond14_1KettenAmikakeKakko = " + _cond14_1TuishidouAmikakeKakko);
            	log.info(" _cond15_kekka               = " + _cond15_1_kekkaBunshi + " / " + _cond15_2_kekkaBunbo);
            	log.info(" _cond16_1_saidanyouSortAB   = " + _cond16_1_saidanyouSortAB);
            	log.info(" _cond17_1_noPrintShokenABCD = " + _cond17_1_noPrintShokenABCD);
            	log.info(" _cond18_1_noPrintChairStaffnameCD = " + _cond18_1_noPrintChairStaffnameCD);
            	log.info(" _cond19_1_noPrintLateEarly  = " + _cond19_1_noPrintLateEarlyC);
            	log.info(" _cond20_1_tuishidouTeiseisenE = " + _cond20_1_tuishidouTeiseisenE);
            	log.info(" _cond21_1_hyoteiHyojiE      = " + _cond21_1_hyoteiHyojiE);
            	log.info(" _cond22_1_addZoukaTanni     = " + _cond22_1_addZoukaTanni);
            }
        }
        
        private String nendo(final DB2UDB db2) {
			return _isSeireki ? StringUtils.defaultString(_year) + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
		}

        private Map getHreportConditionDat(DB2UDB db2) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT * ");
        	stb.append(" FROM HREPORT_CONDITION_DAT ");
        	stb.append(" WHERE YEAR = '" + _year + "' ");
        	stb.append("  AND GRADE = '00' ");
        	stb.append("  AND COURSECD = '0' ");
        	stb.append("  AND MAJORCD = '000' ");
        	stb.append("  AND COURSECODE = '0000' ");
        	
        	return KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, stb.toString()), "SEQ");
		}

		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD185W' AND NAME = '" + propName + "' "));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

//        private void setD054Namecd2Max(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
//                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _d054Namecd2Max = rs.getString("NAMECD2");
//                    _sidouHyoji = rs.getString("NAME1");
//                }
//            } catch (SQLException ex) {
//                log.debug("getZ010 exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ")));
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void loadNameMstD026(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
//            if ("1".equals(_useClassDetailDat)) {
//                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
//                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
//                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
//            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
//            }
            
            _d026List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
            log.info("非表示科目:" + _d026List);
        }

        private Semester getSemester(final String semester) {
            return _semesterMap.get(semester);
        }

//        private boolean useSemester3Form() {
//            return "3".equals(_knjSchoolMst._semesterDiv);
//        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            final Map map = new TreeMap();
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
            
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	map.put(KnjDbUtils.getString(row, "SEMESTER"), new Semester(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE")));
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolHrJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
        }

//        // 卒業認定単位数の取得
//        private int getGradCredits(
//                final DB2UDB db2
//        ) {
//            int gradcredits = 0;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    gradcredits = rs.getInt("GRAD_CREDITS");
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return gradcredits;
//        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                //log.info("科目マスタなし:" + subclasscd);
                return null;
            }
            return _subclassMstMap.get(subclasscd);
        }

//        private String getCredits(final String subclasscd, final String course) {
//            return (String) _creditMst.get(subclasscd + ":" + course);
//        }
//
//        private void setCreditMst(
//                final DB2UDB db2
//        ) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            _creditMst = new HashMap();
//            try {
//                String sql = "";
//                sql += " SELECT ";
//                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
//                sql += " T1.SUBCLASSCD AS SUBCLASSCD,  ";
//                sql += " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE,  ";
//                sql += " T1.CREDITS  ";
//                sql += " FROM CREDIT_MST T1 ";
//                sql += " WHERE T1.YEAR = '" + _year + "' ";
//                sql += "   AND T1.GRADE = '" + _grade + "' ";
//                sql += "   AND T1.CREDITS IS NOT NULL";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _creditMst.put(rs.getString("SUBCLASSCD") + ":" + rs.getString("COURSE"), rs.getString("CREDITS"));
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        public List getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = { grade + "-" + coursecd + "-" + majorcd
                                   , "00"  + "-" + coursecd + "-" + majorcd
                                   , grade + "-" + "0"      + "-" + "000"
                                   , "00"  + "-" + "0"      + "-" + "000"
                                   };
            for (int i = 0; i < keys.length; i++) {
                final List rtn = (List) _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) return rtn;
            }
            return Collections.EMPTY_LIST;
        }

        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
            _recordMockOrderSdivDatMap = new HashMap();
            
            String sql = "";
            sql += " SELECT ";
            sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
            sql += " T1.SEQ,  ";
            sql += " T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD ";
            sql += " FROM RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
            sql += " WHERE T1.YEAR = '" + _year + "' ";
            sql += "   AND T1.TEST_DIV = '1' ";
            sql += " ORDER BY ";
            sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
            sql += " T1.SEQ  ";
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String key = KnjDbUtils.getString(row, "GRADE") + "-" + KnjDbUtils.getString(row, "COURSECD") + "-" + KnjDbUtils.getString(row, "MAJORCD");
                getMappedList(_recordMockOrderSdivDatMap, key).add(KnjDbUtils.getString(row, "TESTCD"));
            }
        }

        private List<TestItem> getAllTestItems(final DB2UDB db2) {
            final List<TestItem> testitemList = new ArrayList();
            try {
                final String sql = "SELECT T1.SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T2.SDATE "
                                 + "  ,T2.EDATE "
                                 +  " ,CASE WHEN T1.SEMESTER <= '" + _semester + "' THEN 1 ELSE 0 END AS PRINT "
                                 +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                                 +  " AND T2.SEMESTER = T1.SEMESTER "
                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                                 +  "WHERE T1.YEAR = '" + _year + "' "
                                 +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.debug(" sql = " + sql);
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final TestItem testitem = new TestItem();
                    testitem._testkindcd = getString(row, "TESTKINDCD");
                    testitem._testitemcd = getString(row, "TESTITEMCD");
                    testitem._scoreDiv = getString(row, "SCORE_DIV");
                    testitem._testitemname = getString(row, "TESTITEMNAME");
                    testitem._sidouinput = getString(row, "SIDOU_INPUT");
                    testitem._sidouinputinf = getString(row, "SIDOU_INPUT_INF");
                    testitem._semester = getString(row, "SEMESTER");
                    testitem._dateRange = new DateRange(testitem.testcd(), testitem._testitemname, getString(row, "SDATE"), getString(row, "EDATE"));
                    testitem._printScore = "1".equals(getString(row, "PRINT"));
                    testitem._scoreDivName = getString(row, "SCORE_DIV_NAME");
                    testitemList.add(testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return testitemList;
        }
        
        protected static Map<String, TestItem> getTestItemMap(final List<TestItem> testItemList) {
            final Map<String, TestItem> map = new HashMap();
            for (final TestItem testItem : testItemList) {
                if (null != testItem) {
                    map.put(testItem.testcd(), testItem);
                }
            }
            return map;
        }

        private List<DateRange> getSemesterDetails(final DB2UDB db2) {
            final List<DateRange> list = new ArrayList();
            final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
                    + "  ,T1.SDATE "
                    + "  ,T1.EDATE "
                    + " FROM SEMESTER_DETAIL_MST T1 "
                    + " WHERE T1.YEAR = '" + _year + "' "
                    + " ORDER BY T1.SEMESTER_DETAIL ";
            log.debug(" sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                list.add(new DateRange(getString(row, "SEMESTER_DETAIL"), getString(row, "SEMESTERNAME"), getString(row, "SDATE"), getString(row, "EDATE")));
            }
            return list;
        }

        private String getSchoolKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT T1 WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' "));
        }

        public String getRegdSemester() {
            return (SEMEALL.equals(_semester)) ? _ctrlSeme : _semester;
        }
    }
}

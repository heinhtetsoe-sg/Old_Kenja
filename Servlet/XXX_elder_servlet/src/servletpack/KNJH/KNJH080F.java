// kanji=漢字
/*
 * $Id: 8338fb189cde03abba68db42abf2186193aa8fbe $
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 母校訪問連絡票  
 */
public class KNJH080F {
    private static final Log log = LogFactory.getLog(KNJH080F.class);

    private static final String SEMEALL = "9";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD = "1010108";

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
            log.fatal("$Revision: 60377 $ $Date: 2018-05-29 14:29:03 +0900 (火, 29 5 2018) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(request, db2);

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
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(db2, param, studentList);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);
            
            printStudent(svf, param, student);
            _hasData = true;
        }
    }
    
    private String mkString(final List textList, final String comma) {
    	final StringBuffer stb = new StringBuffer();
    	String useComma = "";
    	for (final Iterator it = textList.iterator(); it.hasNext();) {
    		final String text = (String) it.next();
    		if (null == text || text.length() == 0) {
    			continue;
    		}
    		stb.append(useComma).append(text);
    		useComma = comma;
    	}
    	return stb.toString();
    }
    
    private void printStudent(final Vrw32alp svf, final Param param, final Student student) {
        final String form = "KNJH080F.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("TITLE", param._nendo + "　母校訪問連絡票"); // タイトル
        svf.VrsOut("SCHOOL_NAME", param._schoolname1); // 学校名
        svf.VrsOut("COURSE_NAME", student._majorname); // コース名
        svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + " " + student.getAttendno() + "番"); // 年組番
        svf.VrsOut("NAME", student._name); // 氏名

        final List clubShikakuTokenList = KNJ_EditKinsoku.getTokenList(mkString(student._clubShikakutokenList, "\n"), 42);
        final int maxLine = 5;
        for (int j = 0; j < Math.min(clubShikakuTokenList.size(), maxLine); j++) {
            final int line = j + 1;
            svf.VrsOutn("CLUB", line, (String) clubShikakuTokenList.get(j)); // 部活
        }

        printAttendance(svf, student);
        
        final List baseRemarkToken = KNJ_EditKinsoku.getTokenList(student._baseRemark1, 21 * 2);
        final int reportMaxLine = 7;
        for (int j = 0; j < Math.min(baseRemarkToken.size(), reportMaxLine); j++) {
            final int line = j + 1;
            svf.VrsOutn("REPORT", line, (String) baseRemarkToken.get(j)); // 担任報告
        }

        final int ketaTeacher_name = KNJ_EditEdit.getMS932ByteLength(student._staffName);
        svf.VrsOut("TEACHER_NAME" + (ketaTeacher_name <= 20 ? "1" : ketaTeacher_name <= 30 ? "2" : "3"), student._staffName); // 担任名

        int printLine = 0;
        final int maxRecord = 11 + 6;
        final List subclassList = new ArrayList(student._subclassMap.values());
        
        for (int i = 0; i < subclassList.size(); i++) {
            final Subclass subclass = (Subclass) subclassList.get(i);

        	svf.VrsOut("CLASS_NAME", subclass._mst._classname); // 教科名
        	if (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) > 12) {
        		svf.VrsOut("SUBCLASS_NAME2_1", subclass._mst._subclassname); // 科目名
        	} else {
                svf.VrsOut("SUBCLASS_NAME1", subclass._mst._subclassname); // 科目名
        	}
            svf.VrsOut("VALUE", subclass.getScore(TESTCD)); // 成績
            svf.VrEndRecord();
            printLine += 1;
        }
        for (int i = printLine; i < maxRecord; i++) {
        	svf.VrsOut("CLASS_NAME", "DUMMY" + String.valueOf(i));
        	svf.VrAttribute("CLASS_NAME", "X=10000");
            svf.VrEndRecord();
        }
        _hasData = true;
    }
    
    private void printAttendance(final Vrw32alp svf, final Student student) {
        final Attendance att = student._attendance;
        if (null != att) {
            svf.VrsOut("LESSON", String.valueOf(att._lesson)); // 授業日数
            svf.VrsOut("MUST", String.valueOf(att._mLesson)); // 出席すべき日数
            svf.VrsOut("ATTEND", String.valueOf(att._present)); // 出席日数
            svf.VrsOut("ABSENT", String.valueOf(att._absent)); // 欠席日数

            BigDecimal bdPercent = new BigDecimal(0);
            if (att._mLesson > 0) {
            	bdPercent = new BigDecimal(att._present).multiply(new BigDecimal(100)).divide(new BigDecimal(att._mLesson), 1, BigDecimal.ROUND_HALF_UP);
            	svf.VrsOut("ATTEND_PER", bdPercent.toString()); // 出席率
            }
            String hyoka = "";
            if (bdPercent.doubleValue() >= 95.0) { // 95%以上
            	hyoka = "Ａ";
            } else if (90.0 <= bdPercent.doubleValue() && bdPercent.doubleValue() < 95) { // 94～90%
            	hyoka = "Ｂ";
            } else if (bdPercent.doubleValue() < 90.0) { // 90%未満
            	hyoka = "Ｃ";
            }
            svf.VrsOut("ATTEND_SITUATION", hyoka); // 出席状況
        }
    }

//    private void printScoreOld(final Vrw32alp svf, final Param param, final Student student) {
//        
//        final List subclassList = new ArrayList(student._subclassMap.values());
//        Collections.sort(subclassList);
//        final Map classcdSubclassListMap = new HashMap();
//        for (int i = 0; i < subclassList.size(); i++) {
//            final Subclass subclass = (Subclass) subclassList.get(i);
//            if (null != subclass._mst._classcd) {
//                getMappedList(classcdSubclassListMap, subclass._mst._classcd).add(subclass);
//            }
//        }
//        for (int i = 0; i < subclassList.size(); i++) {
//            final Subclass subclass = (Subclass) subclassList.get(i);
//            final String recordDiv;
//            final List sameClasscdSubclassList = getMappedList(classcdSubclassListMap, subclass._mst._classcd);
//            final boolean isFirstSubclassInClass = sameClasscdSubclassList.size() > 0 && subclass == sameClasscdSubclassList.get(0);
//            final List recordSubclassList = new ArrayList();
//            final String[] suffixes;
//            recordDiv = "2";
//            recordSubclassList.add(subclass);
//            suffixes = new String[] {""};
//            
//            for (int j = 0; j < Math.min(recordSubclassList.size(), suffixes.length); j++) {
//                final Subclass recSub = (Subclass) recordSubclassList.get(j);
//                
//                if (isFirstSubclassInClass) {
//                    final int classnameKeta = KNJ_EditEdit.getMS932ByteLength(recSub._mst._classabbv);
//                    if (classnameKeta <= 4) {
//                        svf.VrsOut("CLASS_NAME" + recordDiv, recSub._mst._classabbv); // 教科名
//                    } else {
//                        svf.VrsOut("CLASS_NAME" + recordDiv + "_2", recSub._mst._classabbv); // 教科名
//                    }
//                }
//                final boolean isLastSubclassInClass = sameClasscdSubclassList.size() > 0 && recSub == sameClasscdSubclassList.get(sameClasscdSubclassList.size() - 1);
//                if (isLastSubclassInClass) {
//                    svf.VrAttribute("LINE_" + recordDiv, "UnderLine=(0,2,1),Keta=377"); // 教科境界線 0:実線 2:2dot 1:アンダーライン 
//                }
//
//                final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(recSub._mst._subclassname);
//                if (subclassnameKeta <= 10) {
//                    svf.VrsOut("SUBCLASS_NAME" + recordDiv + suffixes[j], recSub._mst._subclassname); // 科目名
//                } else if (subclassnameKeta <= 14) {
//                    svf.VrsOut("SUBCLASS_NAME" + recordDiv + "_2" + suffixes[j], recSub._mst._subclassname); // 科目名
//                } else {
//                    svf.VrsOut("SUBCLASS_NAME" + recordDiv + "_3_1" + suffixes[j], recSub._mst._subclassname); // 科目名
//                }
//                final String score = recSub.getScore(TESTCD);
//                if (null != score) {
//                	final String field = ""; // TODO:
//                	svf.VrsOut(field, score); // 素点
//                }
//            }
//            
//            svf.VrEndRecord();
//        }
//    }

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

    private void load(
            final DB2UDB db2,
            final Param param,
            final List studentList
    ) {
        
        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }
        
        Attendance.load(db2, param, studentMap);
        
        final StringBuffer stbtestcd = new StringBuffer();
        
        final String seme = TESTCD.substring(0, 1);
        final String kind = TESTCD.substring(1, 3);
        final String item = TESTCD.substring(3, 5);
        final String sdiv = TESTCD.substring(5);
        stbtestcd.append(" AND (");
        stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
        stbtestcd.append(") ");
        Score.load(db2, param, studentMap, stbtestcd);
        
        Student.loadClubShikaku(db2, param, studentMap);
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        Attendance _attendance;
        final Map _subclassMap;
        final String _entyear;
        final String _baseRemark1;
        private String _coursecodeAbbv;
        private List _clubShikakutokenList = new ArrayList();

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear, final String baseRemark1) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _subclassMap = new TreeMap();
            _baseRemark1 = baseRemark1;
//            _specialGroupKekkaMinutes = new HashMap();
        }
        
        Subclass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new Subclass(new SubclassMst(classcd, subclasscd, null, null, null, null));
            }
            return (Subclass) _subclassMap.get(subclasscd);
        }

        public static void loadClubShikaku(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps1 = null;
            PreparedStatement ps2 = null;
            try {
                final String sql1 = getClubSql(param);
                log.info(" club sql = " + sql1);
                ps1 = db2.prepareStatement(sql1);

                final String sql2 = getQualifiedSql(param);
                log.info(" qualified sql = " + sql2);
                ps2 = db2.prepareStatement(sql2);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();
                	
                	student._clubShikakutokenList = new ArrayList();
                	
                	for (final Iterator itc = KnjDbUtils.query(db2, ps1, new Object[] {student._schregno}).iterator(); itc.hasNext();) {
                		final Map row = (Map) itc.next();
                		final String name = KnjDbUtils.getString(row, "CLUBNAME");
                		if ((null != name && !student._clubShikakutokenList.contains(name))) {
                			student._clubShikakutokenList.add(name);
                		}
                	}
                	
                	for (final Iterator itq = KnjDbUtils.query(db2, ps2, new Object[] {student._schregno}).iterator(); itq.hasNext();) {
                		final Map row = (Map) itq.next();
                		final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "QUALIFIED_NAME")) + StringUtils.defaultString(KnjDbUtils.getString(row, "RESULT_NAME"));
                		if ((null != name && !student._clubShikakutokenList.contains(name))) {
                			student._clubShikakutokenList.add(name);
                		}
                	}
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps1);
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }
        }

        private static String getClubSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH TMP1 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.* ");
            stb.append("   FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("     AND SDATE <= '" + param._edate + "' ");
            stb.append("     AND (EDATE IS NULL OR '" + param._sdate + "' <= EDATE) ");
            stb.append(" ) SELECT ");
            stb.append("     T1.CLUBCD, L1.CLUBNAME ");
            stb.append(" FROM TMP1 T1 ");
            stb.append(" INNER JOIN (SELECT MIN(SDATE) AS MIN_SDATE FROM TMP1) T2 ON T2.MIN_SDATE = T1.SDATE ");
            stb.append(" INNER JOIN CLUB_MST L1 ON ");
            stb.append("    L1.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("    AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L1.CLUBCD = T1.CLUBCD ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SDATE, L1.CLUBCD ");
            return stb.toString();
        }
        
        private static String getQualifiedSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     T1.TEST_DATE, T1.QUALIFIED_CD, T1.RESULT_CD, T2.QUALIFIED_NAME, L1.RESULT_NAME ");
            stb.append(" FROM SCHREG_QUALIFIED_TEST_DAT T1 ");
            stb.append(" INNER JOIN qualified_result_mst L1 ON ");
            stb.append("    L1.YEAR = T1.YEAR ");
            stb.append("    AND L1.QUALIFIED_CD = T1.QUALIFIED_CD ");
            stb.append("    AND L1.RESULT_CD = T1.RESULT_CD ");
            stb.append(" INNER JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
            stb.append(" WHERE ");
            stb.append("   L1.YEAR = '" + param._loginYear + "' ");
            stb.append("   AND CERT_FLG = 'T' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append("   AND T1.TEST_DATE <= '" + param._edate + "' ");
            stb.append(" ORDER BY ");
            stb.append("   TEST_DATE, T1.QUALIFIED_CD, T1.RESULT_CD ");
            return stb.toString();
        }
        
        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W10.COURSECODEABBV1 ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,YDET.BASE_REMARK1 ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._loginYear + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = REGD.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST YDET ON YDET.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND YDET.YEAR = REGD.YEAR ");
            stb.append("                  AND YDET.BASE_SEQ = '006' ");
            stb.append("     WHERE   REGD.YEAR = '" + param._loginYear + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._loginSemester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append("         AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE ");
            stb.append("         , REGD.HR_CLASS ");
            stb.append("         , REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);
            
            final List students = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
            	final String staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
            	final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "HR_NAME"), staffname, attendno, KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "MAJORNAME"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "ENT_YEAR"), KnjDbUtils.getString(row, "BASE_REMARK1"));
            	student._coursecodeAbbv = KnjDbUtils.getString(row, "COURSECODEABBV1");
            	students.add(student);
            }
            return students;
        }

        public String getAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno);
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
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }
        
        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap
        ) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");
                
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        param._sdate,
                        param._edate,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);
                
                final Integer zero = new Integer(0);
                
                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                    	
                    	if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
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
                                KnjDbUtils.getInt(row, "EARLY", zero).intValue()
                        );
                        student._attendance = attendance;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;

        Subclass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }
        
        public String getScore(final String testcd) {
            return (String) _scoreMap.get(testcd);
        }
        
        public int compareTo(final Object o) {
            final Subclass subclass = (Subclass) o;
            return _mst.compareTo(subclass._mst);
        }
    }
    
    /**
     * 成績
     */
    private static class Score {
        
        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final StringBuffer stbtestcd
        ) {
            final String sql = sqlScore(param, stbtestcd);
//          log.info(" sql = " + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String testcd = KnjDbUtils.getString(row, "TESTCD");
                
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (null == student._subclassMap.get(subclasscd)) {
                    final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                    student._subclassMap.put(subclasscd, subClass);
                }
                if (null == testcd) {
                    continue;
                }
                final Subclass subClass = student.getSubClass(subclasscd);
                subClass._scoreMap.put(testcd, KnjDbUtils.getString(row, "SCORE"));
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
            
            stb.append("     WHERE   W1.YEAR = '" + param._loginYear + "' ");
            stb.append("         AND W1.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            stb.append("         AND W1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV AS TESTCD ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND SUBSTR(W3.SUBCLASSCD, 1, 2) < '90' ");
            stb.append(stbtestcd.toString());
            
            return stb.toString();
        }
    }
    
    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int cmp = 0;
            if (0 != cmp) return cmp;
            if (null != _subclasscd && null != mst._subclasscd) {
                cmp = _subclasscd.compareTo(mst._subclasscd);
            }
            return cmp;
        }
    }
    
    private static class Param {
        final String _loginYear;
        final String _semester;
        final String _loginSemester;

        final String _grade;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _sdate;
        final String _edate;
        final boolean _isSeireki; // 西暦表示するならtrue
        final String _schoolKind;
        final String _schoolname1;
        final String _nendo;

        final String _gradeCd;

        //private Map _semesterMap;
        private Map _subclassMstMap;

        private KNJSchoolMst _knjSchoolMst;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _edate = KNJ_EditDate.H_Format_Haifun(request.getParameter("EDATE"));
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _nendo = _isSeireki ? String.valueOf(_loginYear) + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _schoolKind = getSchoolKind(db2);
            
            final String schoolMstSql = " SELECT * FROM SCHOOL_MST WHERE YEAR = '" + _loginYear + "' " + (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND") ? " AND SCHOOL_KIND = '" + _schoolKind + "' " : "");
            final Map schoolMstRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, schoolMstSql));
            _schoolname1 = KnjDbUtils.getString(schoolMstRow, "SCHOOLNAME1");
            
            //_semesterMap = loadSemester(db2, _loginYear, _grade);
            _gradeCd = getSchoolKind(db2);

            setSubclassMst(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
        }
        
//        /**
//         * 年度の開始日を取得する 
//         */
//        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            final Map map = new HashMap();
//            try {
//                final String sql = "select"
//                        + "   SEMESTER,"
//                        + "   SEMESTERNAME "
//                        + " from"
//                        + "   V_SEMESTER_GRADE_MST"
//                        + " where"
//                        + "   YEAR = '" + year + "'"
//                        + "   AND GRADE = '" + grade + "'"
//                        + " order by SEMESTER"
//                    ;
//                
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    map.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
//                }
//            } catch (final Exception ex) {
//                log.error("テスト項目のロードでエラー", ex);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            return map;
//        }
        
        private String getSchoolKind(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");
            log.debug("gdat sql = " + sql.toString());
            
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }
        
        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null);
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, T1.SUBCLASSNAME ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
                    _subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }
}

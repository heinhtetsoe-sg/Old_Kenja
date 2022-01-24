// kanji=漢字
/*
 * $Id: c894c8b9f78c8712a35317bac9b73044218db1ca $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186T {
    private static final Log log = LogFactory.getLog(KNJD186T.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private Param _param;
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

            printMain(svf, request);

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

    public void printMain(
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            log.fatal("$Revision: 72916 $ $Date: 2020-03-12 09:57:12 +0900 (木, 12 3 2020) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2);

            printMain(db2, svf, _param);

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

    protected void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);

            printStudent(db2, svf, student);
            _hasData = true;
        }
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

    private static String sishaGonyu(final String val, final int scale) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sum(final Collection numList) {
        if (numList.isEmpty()) {
            return null;
        }
        Integer sum = null;
        for (final Iterator it = numList.iterator(); it.hasNext();) {
            final String e = (String) it.next();
            if (!NumberUtils.isDigits(e)) {
            	continue;
            }
            if (null == sum) {
                sum = Integer.valueOf(e);
            } else if (null != e) {
                sum = new Integer(sum.intValue() + Integer.parseInt(e));
            }
        }
        if (null == sum) {
        	return null;
        }
        return sum.toString();
    }

	private Map getFieldInfoMap(final String fieldname) {
        final Map fieldInfo = new HashMap();
        if (_param._svfFormFieldInfoMap.get(_param._currentForm) == null) {
            return fieldInfo;
        }
        try {
            SvfField field = (SvfField) getMappedMap(_param._svfFormFieldInfoMap, _param._currentForm).get(fieldname);
            if (null != field) {
            	final Map attributeMap = field.getAttributeMap();
            	fieldInfo.put("X", attributeMap.get(SvfField.AttributeX));
            	fieldInfo.put("Y", attributeMap.get(SvfField.AttributeY));
            	fieldInfo.put("Keta", attributeMap.get(SvfField.AttributeKeta));
            	fieldInfo.put("Size", attributeMap.get(SvfField.AttributeSize));
            }
        } catch (Throwable t) {
            log.error("error " + t.toString());
        }
        return fieldInfo;
    }

	private String attributeIntPlus(final String fieldname, final String intProperty, final int plus) {
        final Map fieldInfoMap = getFieldInfoMap(fieldname);
        if (fieldInfoMap.isEmpty()) {
        	log.warn(" not found " + fieldname + " in " + getMappedMap(_param._svfFormFieldInfoMap, _param._currentForm));
            return null;
        }
        final int propVal = toInt((String) fieldInfoMap.get(intProperty), 10000);
        return intProperty + "=" + String.valueOf(propVal + plus);
    }

    private static int toInt(final String str, final int def) {
        return NumberUtils.isNumber(str) ? new BigDecimal(str).intValue() : def;
    }

    private void load(final Param param, final DB2UDB db2, final List studentList) {

        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }

        Attendance.load(db2, param, studentMap);
        SubclassAttendance.load(db2, param, studentMap);

        String testcdor = "";
        final StringBuffer stbtestcd = new StringBuffer();
        stbtestcd.append(" AND (");
        for (int i = 0; i < param._testcds.length; i++) {
            final String testcd = param._testcds[i];
            if (null == testcd) {
                continue;
            }
            final String seme = testcd.substring(0, 1);
            final String kind = testcd.substring(1, 3);
            final String item = testcd.substring(3, 5);
            final String sdiv = testcd.substring(5);
            if (seme.compareTo(param._semester) <= 0) {
                stbtestcd.append(testcdor);
                stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                testcdor = " OR ";
            }
        }
        stbtestcd.append(") ");
        Score.load(db2, param, studentMap, stbtestcd);

        Student.setCommunication(db2, param, studentMap.values());
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffname;
        final String _grade;
        final String _gradeCd;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map _subclassMap;
        final String _entyear;
        private String _coursecodeAbbv;
        final Map _shokenMap = new HashMap();

        Student(final String schregno, final String name, final String hrName, final String staffname, final String attendno, final String grade, final String gradeCd, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffname = staffname;
            _attendno = attendno;
            _grade = grade;
            _gradeCd = gradeCd;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _attendMap = new TreeMap();
            _subclassMap = new TreeMap();
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
                return new Subclass(new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999)));
            }
            return (Subclass) _subclassMap.get(subclasscd);
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
            stb.append("            ,GDAT.GRADE_CD ");
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
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._ctrlYear + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
//                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List students = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                final String staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "HR_NAME"), staffname, attendno, KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "GRADE_CD"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "MAJORNAME"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "ENT_YEAR"));
                student._coursecodeAbbv = KnjDbUtils.getString(row, "COURSECODEABBV1");
                students.add(student);
            }
            return students;
        }

        private static void setCommunication(final DB2UDB db2, final Param param, final Collection studentList) {
        	final StringBuffer sql = new StringBuffer();
        	sql.append(" SELECT SEMESTER, COMMUNICATION, REMARK1, REMARK2, REMARK3 ");
        	sql.append(" FROM HREPORTREMARK_DAT T1 ");
        	sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
        	sql.append("   AND SEMESTER <= '" + param._semester + "' ");
        	sql.append("   AND SCHREGNO = ? ");

        	PreparedStatement ps = null;
        	try {
            	ps = db2.prepareStatement(sql.toString());

            	for (final Iterator it = studentList.iterator(); it.hasNext();) {
            		final Student student = (Student) it.next();
            		student._shokenMap.clear();
            		student._shokenMap.putAll(KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}), "SEMESTER"));
            	}

        	} catch (Exception e) {
        		log.error("exception!", e);
        	} finally {
        		DbUtils.closeQuietly(ps);
        		db2.commit();
        	}
        }

        public String getAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno);
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _transferDate;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        Attendance(
                final int lesson,
                final int transferDate,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _transferDate = transferDate;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        private static void load(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue(),
                                KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                                KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                                KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                                KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                                KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                                KnjDbUtils.getInt(row, "EARLY", zero).intValue()
                        );
                        student._attendMap.put(KnjDbUtils.getString(row, "SEMESTER"), attendance);
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
        Subclass _sakiSubclass;

        Subclass(final SubclassMst mst) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd || null == _scoreMap.get(testcd)) {
                return Score.nullScore;
            }
            return (Score) _scoreMap.get(testcd);
        }

        public String getScoreYomikae(final Param param, final String score) {
            String retStr = score;
            if (param._d065Map.containsKey(_mst._subclasscd)) {
                if (param._d001Map.containsKey(score)) {
                    final NameMst nameMst = (NameMst) param._d001Map.get(score);
                    retStr = nameMst._name1;
                }
            }
            return retStr;
        }

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            return (SubclassAttendance) _attendMap.get(key);
        }

        public int compareTo(final Object o) {
            final Subclass subclass = (Subclass) o;
            return _mst.compareTo(subclass._mst);
        }

        public String toString() {
        	return "Subclass(" + _mst.toString() + ")";
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
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString(), 1)  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final Map studentMap) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        SEMEALL,
                        null,
                        param._date,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2,  ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map rs = (Map) rit.next();
                        final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        final String subclasscd = KnjDbUtils.getString(rs, "SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            final BigDecimal lesson = KnjDbUtils.getBigDecimal(rs, "MLESSON", null);
                            final BigDecimal sick = KnjDbUtils.getBigDecimal(rs, "SICK2", null);
                            final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(rs, "REPLACED_SICK", null);

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);

                            if (null == student._subclassMap.get(subclasscd)) {
                                final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            }
                            final Subclass subClass = student.getSubClass(subclasscd);
							subClass._attendMap.put(semester, subclassAttendance);
                        }
                    }
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

        static final Score nullScore = new Score(null, null, null, null, null, null, null);

        final String _score;
        final String _avg;
        final String _replacemoto;
        final String _getCredit;
        final String _provFlg;

        Score(
                final String score,
                final String assessLevel,
                final String avg,
                final String replacemoto,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _score = score;
            _avg = avg;
            _replacemoto = replacemoto;
            _getCredit = getCredit;
            _provFlg = provFlg;
        }

        public String toString() {
            return "Score(" + _score + ")";
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

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final StringBuffer stbtestcd
        ) {
            final String sql = sqlScore(param, stbtestcd);
            if (param._isOutputDebug) {
            	log.info(" sql = " + sql);
            }
            log.info(" subclass query start.");
            final List rowList = KnjDbUtils.query(db2, sql);
            log.info(" subclass query end.");

            for (final Iterator rit = rowList.iterator(); rit.hasNext();) {
            	final Map row = (Map) rit.next();
                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String testcd = KnjDbUtils.getString(row, "TESTCD");

                String scoreString = KnjDbUtils.getString(row, "SCORE");
//                if ("*".equals(KnjDbUtils.getString(row, "VALUE_DI"))) {
//                    scoreString = "欠";
//                }

                final Score score = new Score(
                        scoreString,
                        null, // KnjDbUtils.getString(row, "ASSESS_LEVEL"),
                        KnjDbUtils.getString(row, "AVG"),
                        KnjDbUtils.getString(row, "REPLACEMOTO"),
                        KnjDbUtils.getString(row, "COMP_CREDIT"),
                        KnjDbUtils.getString(row, "GET_CREDIT"),
                        KnjDbUtils.getString(row, "PROV_FLG")
                );

                final String subclasscd;
                if (SUBCLASSCD999999.equals(StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-")[3])) {
                    subclasscd = SUBCLASSCD999999;
                } else {
                    subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                if (null == student._subclassMap.get(subclasscd)) {
                    final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                    student._subclassMap.put(subclasscd, subClass);
                }
                if (null == testcd) {
                    continue;
                }
                // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                final Subclass subClass = student.getSubClass(subclasscd);
                subClass._scoreMap.put(testcd, score);
            }
        }

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO, W1.YEAR, W1.SEMESTER, W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, ");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,W3.AVG ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
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
            stb.append("    ,W3.VALUE_DI ");
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
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
            stb.append("     ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,T33.VALUE_DI ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");

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

            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) <= '90' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

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
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        boolean _isSaki;
        SubclassMst _combinedSubclassMst;
		public String _calcurateCreditFlg;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3, final Integer subclassShoworder3) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int cmp = 0;
            if (0 != cmp) return cmp;
            cmp = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != cmp) return cmp;
            if (null != _classcd && null != mst._classcd) {
                cmp = _classcd.compareTo(mst._classcd);
                if (0 != cmp) return cmp;
            }
            cmp = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != cmp) return cmp;
            if (null != _subclasscd && null != mst._subclasscd) {
                cmp = _subclasscd.compareTo(mst._subclasscd);
            }
            return cmp;
        }
        public String toString() {
        	return "SubclassMst(" + _subclasscd + ", " + _subclassname + ")";
        }
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD186T.frm";
        svf.VrSetForm(form, 4);

        if (null == _param._svfFormFieldInfoMap.get(form)) {
        	_param._currentForm = form;
        	_param._svfFormFieldInfoMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
        }

		printHeader(svf, student);

        printShoken(svf, student);

        printScore(svf, student);
    }

	private void printShoken(final Vrw32alp svf, final Student student) {
		final String[] semes = {"1", "2", "3", SEMEALL};
		for (int semei = 0; semei < semes.length; semei++) {
        	final Attendance att = (Attendance) student._attendMap.get(semes[semei]);
        	if (!SEMEALL.equals(_param._semester) && Integer.parseInt(semes[semei]) > Integer.parseInt(_param._semester)) {
        		continue;
        	}
            final String ssi = String.valueOf(semei + 1);
            if (null != att) {
            	svf.VrsOutn("ATTEND" + ssi, 1, String.valueOf(att._lesson)); // 出欠 授業日数
            	svf.VrsOutn("ATTEND" + ssi, 2, String.valueOf(att._suspend + att._mourning)); // 出欠 忌引・出席停止日数
            	svf.VrsOutn("ATTEND" + ssi, 3, String.valueOf(att._transferDate)); // 出欠 留学中の授業日数
            	svf.VrsOutn("ATTEND" + ssi, 4, String.valueOf(att._mLesson)); // 出欠 出席すべき日数
            	svf.VrsOutn("ATTEND" + ssi, 5, String.valueOf(att._absent)); // 出欠 欠席日数
            	svf.VrsOutn("ATTEND" + ssi, 6, String.valueOf(att._present)); // 出欠 出席日数
            	svf.VrsOutn("ATTEND" + ssi, 7, String.valueOf(att._late)); // 出欠 遅刻回数
            	svf.VrsOutn("ATTEND" + ssi, 8, String.valueOf(att._early)); // 出欠 早退回数
            }
        }

        final Map hreportremarkDat = getMappedMap(student._shokenMap, _param._semester);

		svf.VrsOut("SPECIAL1", KnjDbUtils.getString(hreportremarkDat, "REMARK1")); // 特別活動の記録

		final List remark2TokenList = KNJ_EditKinsoku.getTokenList(KnjDbUtils.getString(hreportremarkDat, "REMARK2"), 30);
        for (int si = 0; si < remark2TokenList.size(); si++) {
            svf.VrsOutn("SPECIAL2_1", si + 1, (String) remark2TokenList.get(si)); // 特別活動の記録
        }
		final List remark3TokenList = KNJ_EditKinsoku.getTokenList(KnjDbUtils.getString(hreportremarkDat, "REMARK3"), 30);
        for (int si = 0; si < remark3TokenList.size(); si++) {
            svf.VrsOutn("SPECIAL3_1", si + 1, (String) remark3TokenList.get(si)); // 特別活動の記録
        }

        for (int semei = 1; semei <= Integer.parseInt(_param._shokenSemester); semei++) {
            final String communication = KnjDbUtils.getString(getMappedMap(student._shokenMap, String.valueOf(semei)), "COMMUNICATION");
            final List tokenList = KNJ_EditKinsoku.getTokenList(communication, 50);
            for (int i = 0; i < tokenList.size(); i++) {
                final String ssi = String.valueOf(i + 1);
                svf.VrsOutn("FIELD" + ssi, semei, (String) tokenList.get(i)); //
            }
        }
	}

	private void printScore(final Vrw32alp svf, final Student student) {
		//log.info(" subclassmap = " + student._subclassMap);
        final List subclassList = new ArrayList(student._subclassMap.values());
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (_param._d026List.contains(subclass._mst._subclasscd)) {
            	if (_param._isOutputDebug) {
            		log.info(" not print D026 subclass " + subclass._mst._subclasscd);
            	}
            	it.remove();
            	continue;
            }
            if (_param._isNoPrintMoto && null != subclass._mst._combinedSubclassMst) {
            	log.info(" not print moto kamoku " + subclass._mst._subclasscd);
            	it.remove();
            	continue;
            }
            if (!_param._isPrintSakiKamoku && subclass._mst._isSaki) {
            	log.info(" not print saki kamoku " + subclass._mst._subclasscd);
            	it.remove();
            	continue;
            }
            if (SUBCLASSCD999999.equals(subclass._mst._subclasscd)) {
                it.remove();
            }
        }

        Collections.sort(subclassList);
        //log.info(" subclassList = " + subclassList.size());
        final List classcdList = new ArrayList();
        final Map classnameMap = new HashMap();
        final Map classcdSubclassListMap = new HashMap();
        for (int i = 0; i < subclassList.size(); i++) {
            final Subclass subclass = (Subclass) subclassList.get(i);
            if (!classcdList.contains(subclass._mst._classcd)) {
            	classcdList.add(subclass._mst._classcd);
            }
            if (null != subclass._mst._classcd) {
                getMappedList(classcdSubclassListMap, subclass._mst._classcd).add(subclass);
                classnameMap.put(subclass._mst._classcd, subclass._mst._classname);
            }
        }
        int line = 0;
        for (int i = 0; i < classcdList.size(); i++) {
        	final String classcd = (String) classcdList.get(i);
        	final String classname = (String) classnameMap.get(classcd);
        	final String classnameField = "CLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(classname) <= 6 ? "1" : KNJ_EditEdit.getMS932ByteLength(classname) <= 8 ? "2" : "3_1");
        	final List classcdSubclassList = getMappedList(classcdSubclassListMap, classcd);
        	for (int subi = 0; subi < classcdSubclassList.size(); subi++) {
        		final Subclass subclass = (Subclass) classcdSubclassList.get(subi);
        		svf.VrsOut("GRP", classcd); // グループ用コード
        		if (subi == classcdSubclassList.size() / 2) {
        			if (classcdSubclassList.size() % 2 == 0) {
        				final int recordHeight = 890 - 792;
        				final int recordHeightHalf = recordHeight / 2;
        				svf.VrAttribute(classnameField, attributeIntPlus(classnameField, "Y", line * recordHeight - recordHeightHalf)); // 教科名
        			}
        			svf.VrsOut(classnameField, classname); // 教科名
        		}
        		if (_param._isOutputDebug) {
        			log.info(" " + subclass._mst._subclasscd + " " + subclass._mst._subclassname);
        		}

        		final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname);
				svf.VrsOut("SUBCLASS_NAME" + (subclassnameKeta <= 10 ? "1" : subclassnameKeta <= 12 ? "2" : subclassnameKeta <= 16 ? "3" : "4_1"), subclass._mst._subclassname); // 科目名

        		final String credit;
        		if (subclass._mst._isSaki && "2".equals(subclass._mst._calcurateCreditFlg)) { // 加算
        			final List attendSubclassCredit = new ArrayList();
                	for (int cri = 0; cri < classcdSubclassList.size(); cri++) {
                		final Subclass creditSubclass = (Subclass) classcdSubclassList.get(cri);
                		if (creditSubclass._sakiSubclass == subclass) {
                			attendSubclassCredit.add(_param.getCredits(creditSubclass._mst._subclasscd, student._course));
                		}
                	}
                	log.info(" saki kamoku " + subclass._mst + " attendSubclassCredit = " + attendSubclassCredit);
                	credit = sum(attendSubclassCredit);
        		} else {
        			credit = _param.getCredits(subclass._mst._subclasscd, student._course);
        		}
        		svf.VrsOut("CREDIT", credit); // 単位数

        		for (int j = 0; j < _param._testcds.length; j++) {
        			Score score = subclass.getScore(_param._testcds[j]);
        			if (null != score) {
                        final String yomikaeScore = subclass.getScoreYomikae(_param, score._score);
        				svf.VrsOut("VALUE" + String.valueOf(j + 1), yomikaeScore); // 評価
        			}
        		}
        		for (int semei = 1; semei <= 4; semei++) {
        			final String seme = semei == 4 ? SEMEALL : String.valueOf(semei);
                	if (!SEMEALL.equals(_param._semester) && semei > Integer.parseInt(_param._semester)) {
                		continue;
                	}
        			final SubclassAttendance att = subclass.getAttendance(seme);
        			if (null != att && null != att._sick) {
        				svf.VrsOut("KEKKA" + String.valueOf(semei), sishaGonyu(att._sick.toString(), _param._kekkaScale)); // 欠課時数
        			}
        		}
        		svf.VrEndRecord();
        		line += 1;
        	}
        }
        if (line == 0) {
        	final int max = 20;
        	for (int i = 0; i < max; i++) {
        		svf.VrsOut("CLASS_NAME1", "DUMMY"); // 教科名
        		svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
        		svf.VrEndRecord();
        	}
        }
	}

	private void printHeader(final Vrw32alp svf, final Student student) {
		svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolRemark8); // 学校名 法人名
		svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName); // 学校名

		final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
		svf.VrsOut("NAME" + (ketaName <= 18 ? "1" : ketaName <= 22 ? "2" : "3"), student._name); // 氏名

		svf.VrsOut("SEMESTER", (String) _param._semesterMap.get(_param._semester)); // 学期
		svf.VrsOut("NENDO", _param._nendo); // 年度
		svf.VrsOut("MAJORNAME", student._majorname); // 学科名
		final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno) + "番"): student._attendno;
		final String hrName = (NumberUtils.isDigits(student._gradeCd) ? String.valueOf(Integer.parseInt(student._gradeCd)) : "　") + "年" + StringUtils.defaultString(student._hrClassName1, "　") + "組";
		svf.VrsOut("HR_NAME", hrName + attendno); // 年組番

		svf.VrsOut("TEACHER1", _param._certifSchoolPrincipalName); // 職員名
		svf.VrsOut("TEACHER2", student._staffname); // 職員名
//		svf.VrsOut("STAFFBTM1", null); //
//		svf.VrsOut("STAFFBTMC1", null); //
//		svf.VrsOut("STAFFBTM2", null); //
//		svf.VrsOut("STAFFBTMC2", null); //

        for (int semei = 1; semei <= 4; semei++) {
        	final String semestername = (String) _param._semesterMap.get(4 == semei ? SEMEALL : String.valueOf(semei));
            final String ssi = String.valueOf(semei);
            svf.VrsOut("SEMESTER1_" + ssi, semestername); // 学期
            svf.VrsOut("SEMESTER2_" + ssi, semestername); // 学期
            svf.VrsOut("SEMESTER3_" + ssi, semestername); // 学期
            svf.VrsOut("SEMESTER4_" + ssi, semestername); // 学期
        }
	}

    private static class NameMst {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _abbv1;
        public NameMst(
                final String namecd2,
                final String name1,
                final String name2,
                final String abbv1
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _abbv1 = abbv1;
        }
    }

    private static class Param {
		final String _ctrlYear;
        final String _semester;
        final String _ctrlSemester;
        final String _shokenSemester;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final boolean _isOutputDebug;
        final boolean _isSeireki;
        final String _nendo;
        final int _kekkaScale;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark8;
        final String _certifSchoolPrincipalName;
        final String _certifSchoolJobName;

        final String _gradeCd;
        final String _documentroot;
        final String _imagePath;
        final String _extension;
        final String _schoolLogoImagePath;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMstMap;
        final Map _creditMst = new HashMap();

        private List _d026List;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        final Map _attendParamMap;

        final String[] _testcds;

        private String _currentForm;
        public Map _svfFormFieldInfoMap = new HashMap();
        final Map _d065Map;
        final Map _d001Map;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _documentroot = request.getParameter("DOCUMENTROOT");
            _testcds = new String[] {"1990008", "2990008", "3990008", "9990009"};

            String semesterDiv = "";
            int kekkaScale = 0;
            try {
            	final Map paramMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		paramMap.put("SCHOOL_KIND", "H");
            	}
                final KNJSchoolMst knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear, paramMap);
                semesterDiv = StringUtils.defaultString(knjSchoolMst._semesterDiv);
                kekkaScale = "3".equals(knjSchoolMst._absentCov) || "4".equals(knjSchoolMst._absentCov) ? 1 : 0;
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _shokenSemester = SEMEALL.equals(request.getParameter("SEMESTER")) ? semesterDiv : request.getParameter("SEMESTER"); // 学年末は最終学期
            _kekkaScale = kekkaScale;
            _semester = request.getParameter("SEMESTER");
            _semesterMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT SEMESTER, SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' "), "SEMESTER", "SEMESTERNAME");
            _gradeCd = getGradeCd(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark8 = getCertifSchoolDat(db2, "REMARK8");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");

            setSubclassMst(db2);
            setCreditMst(db2);
            loadNameMstD016(db2);
            loadNameMstD021(db2);
            loadNameMstD026(db2);
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '01' ")));
            _nendo = _isSeireki ? _ctrlYear + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlYear)) + "年度";

            final KNJ_Control.ReturnVal returnval = getImagepath(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _schoolLogoImagePath = getImagePath();

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _d001Map = getNameMst(db2, "D001", "NAMECD2");
            _d065Map = getNameMst(db2, "D065", "NAME1");
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186T' AND NAME = '" + propName + "' "));
        }

        /**
         * 写真データ格納フォルダの取得
         */
        private KNJ_Control.ReturnVal getImagepath(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getImagePath() {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO_H." + _extension;
            final boolean exists = new java.io.File(path).exists();
            log.info(" image path " + path + " exists? " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ")));
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1, final String keyName) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setKey = rs.getString(keyName);
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    final String abbv1 = rs.getString("ABBV1");
                    NameMst nameMst = new NameMst(namecd2, name1, name2, abbv1);
                    retMap.put(setKey, nameMst);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /**
         * 合併先科目を印刷するか
         */
        private void loadNameMstD021(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _ctrlYear + "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.info("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private String getGradeCd(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' "));
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '104' ")));
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
                return new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999));
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }

        private void setSubclassMst(final DB2UDB db2) {
            _subclassMstMap = new HashMap();
            String sql = "";
            sql += " SELECT ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            sql += " VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, VALUE(T2.CLASSORDERNAME2, T2.CLASSABBV) AS CLASSABBV, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
            sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
            sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3 ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final Integer classShoworder3 = Integer.valueOf(KnjDbUtils.getString(row, "CLASS_SHOWORDER3"));
                final Integer subclassShoworder3 = Integer.valueOf(KnjDbUtils.getString(row, "SUBCLASS_SHOWORDER3"));
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"), classShoworder3, subclassShoworder3);
                _subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
            }

            String sql1 = "";
            sql1 += " SELECT ";
            sql1 += "    COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            sql1 += "    ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
            sql1 += "    CALCULATE_CREDIT_FLG ";
            sql1 += " FROM SUBCLASS_REPLACE_COMBINED_DAT ";
            sql1 += " WHERE YEAR = '" + _ctrlYear + "' ";

            for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String combiendSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");

                SubclassMst combinedSubclassMst = (SubclassMst) _subclassMstMap.get(combiendSubclasscd);
                SubclassMst attendSubclassMst = (SubclassMst) _subclassMstMap.get(attendSubclasscd);
                if (null != combinedSubclassMst && null != attendSubclassMst) {
                    attendSubclassMst._combinedSubclassMst = combinedSubclassMst;
                    combinedSubclassMst._isSaki = true;
                    combinedSubclassMst._calcurateCreditFlg = KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG");
                }
            }
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
//            if ("1".equals(_useClassDetailDat)) {
//                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
//                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
//                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
//            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
//            }

            _d026List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
            if (_isOutputDebug) {
            	log.info(" D026 = " + _d026List);
            }
        }

        private String getCredits(final String subclasscd, final String course) {
            return (String) _creditMst.get(subclasscd + ":" + course);
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            String sql = "";
            sql += " SELECT ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS SUBCLASS_COURSE_KEY,  ";
            sql += " T1.CREDITS  ";
            sql += " FROM CREDIT_MST T1 ";
            sql += " WHERE T1.YEAR = '" + _ctrlYear + "' ";
            sql += "   AND T1.GRADE = '" + _grade + "' ";
            sql += "   AND T1.CREDITS IS NOT NULL";
            _creditMst.clear();
            _creditMst.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "SUBCLASS_COURSE_KEY", "CREDITS"));
        }
    }
}

// kanji=漢字
/*
 * $Id: fb63c2eef037ffb6b912f5f7e9d5db3f24e404ae $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186G {
    private static final Log log = LogFactory.getLog(KNJD186G.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

//    private static final String OUTPUT_RANK1 = "1";
//    private static final String OUTPUT_RANK2 = "2";
//    private static final String OUTPUT_RANK3 = "3";
    
//    private static final String SIDOU_INPUT_INF_MARK = "1";
//    private static final String SIDOU_INPUT_INF_SCORE = "2";

//    private static final String ATTRIBUTE_TUISHIDO = "Paint=(1,90,2),Bold=1";
    
    private static final String CLASSCD_TOKUBETSUKATSUDO = "91";

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

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            outputPdf(ipdf, request);

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
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, ipdf, param);

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
            final IPdf ipdf,
            final Param param
    ) {
        final List studentList = Student.getStudentList(db2, param);
        log.info(" student size = " + studentList.size());
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);
            param._form.init(param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));
            param._form.print(ipdf, student);
        }
        _hasData = true;
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

    private static void loginfo(final Param param, final Object o) {
        if (param._isOutputDebug) {
            log.info(o);
        }
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList0
    ) {
//        Student.loadPreviousCredits(db2, param, studentList0);  // 前年度までの修得単位数取得
        
        final Form form = param._form;
        
        final Map courseStudentsMap = new HashMap();
        for (final Iterator it = studentList0.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }
        
        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final List studentList = (List) courseStudentsMap.get(key);
            final String[] split = StringUtils.split(key, "-");
            
            final List recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);
            
            form.init(recordMockOrderSdivDatList);
            
            for (int i = 0; i < form._attendRanges.length; i++) {
                final DateRange range = form._attendRanges[i];
                if (null == range) {
                	continue;
                }
                if (null != range._testitem) {
                	if (range._testitem._testcd.compareTo(param._testcd) > 0) {
                		continue;
                	}
                }
                Attendance.load(db2, param, studentList, range);
            }
            for (int i = 0; i < form._attendSubclassRanges.length; i++) {
                final DateRange range = form._attendSubclassRanges[i];
                if (null == range) {
                	continue;
                }
                if (null != range._testitem) {
                	if (range._testitem._testcd.compareTo(param._testcd) > 0) {
                		continue;
                	}
                }
                SubclassAttendance.load(db2, param, studentList, range);
            }
            
            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            final List testcds = new ArrayList(form._testcds);
            if (SEMEALL.equals(param._semester)) {
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
                if (seme.compareTo(param._semester) <= 0) {
                    stbtestcd.append(testcdor);
                    stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                    testcdor = " OR ";
                }
            }
            stbtestcd.append(") ");
            Score.load(db2, param, studentList, stbtestcd);

            if ("1".equals(param._address)) {
            	Address.setAddress(db2, studentList, param);
            }
            Student.setHreportremarkCommunication(param, db2, studentList);
        }
    }

    private static Student getStudent(final List studentList, final String code) {
        if (code == null) {
            return null;
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (code.equals(student._schregno)) {
                return student;
            }
        }
        return null;
    }
    
    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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
        final Map _attendMap;
        final Map _attendRemarkMap;
        final Map _subclassMap;
        final String _entyear;
        private Address _address;
        private String _communication;

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
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
            _attendMap = new TreeMap();
            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
        }
        
        Subclass getSubclass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new Subclass(new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false));
            }
            return (Subclass) _subclassMap.get(subclasscd);
        }
        
        public String getHrAttendNo(final Param param) {
            try {
                final String grade = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(0, 2)));
                final String hrclass = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(2)));
                final String attendno = String.valueOf(Integer.parseInt(_attendno));
                return grade + "-" + hrclass + "-" + attendno + " " + StringUtils.defaultString(_name);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }
        
        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  W1.SCHREGNO");
            stb.append("            ,W1.SEMESTER ");
            stb.append("            ,W7.NAME ");
            stb.append("            ,W6.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
//                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         W1.ATTENDNO ");
            final String sql = stb.toString();
            if (param._isOutputDebug) {
            	log.info(" student sql = " + sql);
            }
            
            final List students = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
            	final Map map = (Map) it.next();
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(map, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(map, "ATTENDNO"))) : KnjDbUtils.getString(map, "ATTENDNO");
                final String staffname = StringUtils.defaultString(KnjDbUtils.getString(map, "STAFFNAME"));
                students.add(new Student(KnjDbUtils.getString(map, "SCHREGNO"), KnjDbUtils.getString(map, "NAME"), KnjDbUtils.getString(map, "HR_NAME"), staffname, attendno, KnjDbUtils.getString(map, "GRADE"), KnjDbUtils.getString(map, "COURSECD"), KnjDbUtils.getString(map, "MAJORCD"), KnjDbUtils.getString(map, "COURSE"), KnjDbUtils.getString(map, "MAJORNAME"), KnjDbUtils.getString(map, "HR_CLASS_NAME1"), KnjDbUtils.getString(map, "ENT_YEAR")));
            }
            return students;
        }
        
        // 仮評定があるか
        public boolean hasKari(final Param param) {
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd) || subclasscd.endsWith("333333") || subclasscd.endsWith("555555") || subclasscd.endsWith("99999B")) {
                    continue;
                }
                final Subclass subclass = getSubclass(subclasscd);
                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
                    continue;
                }
                final Score score = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != score && NumberUtils.isDigits(score._score) && "1".equals(score._provFlg)) {
                    return true;
                }
            }
            return false;
        }

        public String getTotalGetCredit(final Param param) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            final Map getCreditMap = new TreeMap();
            final Map getCreditKariMap = new TreeMap();
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subclass = getSubclass(subclasscd);
                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
                    log.info(" // skip credit : " + (null == subclass ? "" : subclass._mst + " : " + subclass._mst._isMoto));
                    continue;
                }
                final Score score = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
                final String getCredit = null == score ? null : score.getGetCredit(param);
                if (NumberUtils.isDigits(getCredit)) {
                    final int iCredit = Integer.parseInt(getCredit);
                    if ("1".equals(score._provFlg)) {
                        totalGetCreditKari += iCredit;
                        getCreditKariMap.put(subclass._mst, getCredit);
                    } else {
                        totalGetCredit += iCredit;
                        getCreditMap.put(subclass._mst, getCredit);
                    }
                }
            }
            if (param._isOutputDebug) {
                log.info(" total get credit      = " + getCreditMap);
                log.info(" total get credit kari = " + getCreditKariMap);
            }
            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
//                if (addPrevious && _previousCredits > 0) {
//                    return String.valueOf(_previousCredits);
//                }
                return "(" + String.valueOf(totalGetCreditKari) + ")";
            }
//            if (addPrevious) {
//                totalGetCredit += _previousCredits;
//            }
            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
        }

        public String getKettenSubclassCount(final Param param, final TestItem testItem) {
            final List list = new ArrayList();
            boolean hasNotNullSubclassScore = false;
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subclass = getSubclass(subclasscd);
//                if (null == subclass || param._isNoPrintMoto && subclass._mst._isMoto) {
//                    continue;
//                }
                final Score score = subclass.getScore(testItem._testcd);
                if (null != score) {
                    if (score.isKetten(param, testItem)) {
                        list.add(subclass);
                    }
                    if (null != score._score) {
                        hasNotNullSubclassScore = true;
                    }
                }
            }
            if (!hasNotNullSubclassScore) {
                return null;
            }
            if (!list.isEmpty()) {
            	if (param._isOutputDebug) {
            		log.info(" ketten " + testItem._testcd + " subclass list = " + list);
            	}
            }
            return String.valueOf(list.size());
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT COMMUNICATION ");
            stb.append(" SELECT REMARK1 ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
//            stb.append("   AND SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("   AND SEMESTER = '" + SEMEALL + "' ");
            stb.append("   AND SCHREGNO = ? ");
            
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    student._communication = KnjDbUtils.getOne(KnjDbUtils.query(db2, ps, new Object[] { student._schregno}));
                    
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }
        
        private static String addLine(final String source, final String data) {
            if (StringUtils.isBlank(source)) {
                return data;
            }
            if (StringUtils.isBlank(data)) {
                return source;
            }
            return source + "\n" + data;
        }
    }
    
    /**
     * 宛先住所データ
     */
    private static class Address {
        final String _addressee;
        final String _address1;
        final String _address2;
        final String _zipcd;
        
        public Address(final String addressee, final String address1, final String address2, final String zipcd) {
            _addressee = addressee;
            _address1 = address1;
            _address2 = address2;
            _zipcd = zipcd;
        }
        
        /**
         * 宛先の住所をセットする
         * @param db2
         * @param student
         * @param param
         */
        public static void setAddress(final DB2UDB db2, final List studentList, final Param param) {
            PreparedStatement ps = null;
            try {
                final StringBuffer stb = new StringBuffer();
                
//                if ("1".equals(param._addressSelect)) {
//                    stb.append(" SELECT T0.SCHREGNO, ");
//                    stb.append("        CASE WHEN T5.SCHREGNO IS NOT NULL THEN T0.REAL_NAME ELSE T0.NAME END AS ADDRESSEE, ");
//                    stb.append("        T4.ADDR1, T4.ADDR2, T4.ZIPCD ");
//                    stb.append(" FROM SCHREG_BASE_MST T0 ");
//                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T3 ON ");
//                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
//                    stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
//                    stb.append(" LEFT JOIN SCHREG_NAME_SETUP_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO AND T5.DIV = '03' ");
//                    stb.append(" WHERE ");
//                    stb.append("     T0.SCHREGNO = ? ");
//                } else if ("2".equals(param._addressSelect)) {
                    stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._printDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._printDate + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = ? ");
//                } else {
//                    stb.append(" SELECT T0.SCHREGNO, T2.SEND_NAME AS ADDRESSEE, T2.SEND_NAME AS ADDRESSEE2, T2.SEND_ADDR1 AS ADDR1, T2.SEND_ADDR2 AS ADDR2, T2.SEND_ZIPCD AS ZIPCD ");
//                    stb.append(" FROM SCHREG_BASE_MST T0 ");
//                    stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO AND T2.DIV = '1' ");
//                    stb.append(" WHERE ");
//                    stb.append("     T0.SCHREGNO = ? ");
//                }

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map map = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}));
                    
                    final String addressee = true && null != KnjDbUtils.getString(map, "ADDRESSEE2") ? KnjDbUtils.getString(map, "ADDRESSEE2") : KnjDbUtils.getString(map, "ADDRESSEE");
                    final String addr1 = KnjDbUtils.getString(map, "ADDR1");
                    final String addr2 = KnjDbUtils.getString(map, "ADDR2");
                    final String zipcd = KnjDbUtils.getString(map, "ZIPCD");
                    student._address = new Address(addressee, addr1, addr2, zipcd);

                }

            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }
    }

    private static class Attendance {
//        private static final String GROUP_LHR = "001";
//        private static final String GROUP_EVENT = "002";
//        private static final String GROUP_COMMITTEE = "003";

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
//        BigDecimal _lhrKekka = new BigDecimal("0");
//        BigDecimal _gyojiKekka = new BigDecimal("0");
//        BigDecimal _iinkaiKekka = new BigDecimal("0");
        DateRange _dateRange;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int abroad,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }
        
        private Attendance add(final Attendance att) {
        	return new Attendance(_lesson + att._lesson,
        			_mLesson + att._mLesson,
        			_suspend + att._suspend,
        			_mourning + att._mourning,
        			_abroad + att._abroad,
        			_absent + att._absent,
        			_present + att._present,
        			_late + att._late,
        			_early + att._early
        			);
        }
        
        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
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
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator ait = KnjDbUtils.query(db2, ps, new Object[] { student._schregno}).iterator(); ait.hasNext();) {
                    	final Map map = (Map) ait.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(map, "SEMESTER"))) {
                            continue;
                        }
                        
                        final Attendance attendance = new Attendance(
                                Integer.parseInt(KnjDbUtils.getString(map, "LESSON")),
                                Integer.parseInt(KnjDbUtils.getString(map, "MLESSON")),
                                Integer.parseInt(KnjDbUtils.getString(map, "SUSPEND")),
                                Integer.parseInt(KnjDbUtils.getString(map, "MOURNING")),
                                Integer.parseInt(KnjDbUtils.getString(map, "TRANSFER_DATE")),
                                Integer.parseInt(KnjDbUtils.getString(map, "SICK")),
                                Integer.parseInt(KnjDbUtils.getString(map, "PRESENT")),
                                Integer.parseInt(KnjDbUtils.getString(map, "LATE")),
                                Integer.parseInt(KnjDbUtils.getString(map, "EARLY"))
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
//            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, dateRange._sdate, edate);
//            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

//        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List studentList, final DateRange dateRange) {
//            PreparedStatement ps = null;
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
//                ps = db2.prepareStatement(stb.toString());
//                
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    ps.setString(1, student._schregno);
//                    
//                    String comma = "";
//                    final StringBuffer remark = new StringBuffer();
//                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] { student._schregno}).iterator(); rit.hasNext();) {
//                    	final Map map = (Map) rit.next();
//                        remark.append(comma).append(KnjDbUtils.getString(map, "REMARK1"));
//                        comma = "、";
//                    }
//                    if (remark.length() != 0) {
//                        student._attendRemarkMap.put(dateRange._key, remark.toString());
//                    }
//                }
//
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//            }            
//        }
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
            final Subclass subclass = (Subclass) o;
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
                final List studentList,
                final DateRange dateRange) {
        	if (param._isOutputDebug) {
        		log.info(" subclass attendance dateRange = " + dateRange);
        	}
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
                //loginfo(param, " attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

//                    final Map specialGroupKekkaMinutes = new HashMap();
                    
//                    for (final Iterator ait = KnjDbUtils.query(db2, ps, new Object[] { student._schregno}).iterator(); ait.hasNext();) {
//                    	final Map map = (Map) ait.next();
//                        if (!SEMEALL.equals(KnjDbUtils.getString(map, "SEMESTER"))) {
//                            continue;
//                        }
//                        final String subclasscd = KnjDbUtils.getString(map, "SUBCLASSCD");
//                        
//                        final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
//                        if (null == mst) {
//                            continue;
//                        }
//                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
//                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T) || iclasscd == Integer.parseInt(CLASSCD_TOKUBETSUKATSUDO))) {
//                            final BigDecimal lesson = new BigDecimal(KnjDbUtils.getString(map, "MLESSON"));
//                            // final BigDecimal rawSick = new BigDecimal(KnjDbUtils.getString(map, "SICK1");
//                            final BigDecimal sick = new BigDecimal(KnjDbUtils.getString(map, "SICK2"));
//                            // final BigDecimal rawReplacedSick = new BigDecimal(KnjDbUtils.getString(map, "RAW_REPLACED_SICK");
//                            final BigDecimal replacedSick = new BigDecimal(KnjDbUtils.getString(map, "REPLACED_SICK"));
//                            
//                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);
//                            
//                            if (null == student._subclassMap.get(subclasscd)) {
//                                final Subclass subclass = new Subclass(param.getSubclassMst(subclasscd));
//                                student._subclassMap.put(subclasscd, subclass);
//                            }
//                            final Subclass subclass = student.getSubclass(subclasscd);
//                            subclass._attendMap.put(dateRange._key, subclassAttendance);
//                        }
//                        
////                        final String specialGroupCd = KnjDbUtils.getString(map, "SPECIAL_GROUP_CD");
////                        if (null != specialGroupCd) {
////                            // 特別活動科目の処理 (授業分数と結果数の加算)
////                            final String subclassCd = KnjDbUtils.getString(map, "SUBCLASSCD");
////                            final String kekkaMinutes = KnjDbUtils.getString(map, "SPECIAL_SICK_MINUTES1");
////                            
////                            getMappedMap(specialGroupKekkaMinutes, specialGroupCd).put(subclassCd, kekkaMinutes);
////                        }
//                    }
//                    
////                    for (final Iterator spit = specialGroupKekkaMinutes.entrySet().iterator(); spit.hasNext();) {
////                        final Map.Entry e = (Map.Entry) spit.next();
////                        final String specialGroupCd = (String) e.getKey();
////                        final Map subclassKekkaMinutesMap = (Map) e.getValue();
////                        
////                        int totalMinutes = 0;
////                        for (final Iterator subit = subclassKekkaMinutesMap.entrySet().iterator(); subit.hasNext();) {
////                            final Map.Entry subMinutes = (Map.Entry) subit.next();
////                            final String minutes = (String) subMinutes.getValue();
////                            if (NumberUtils.isDigits(minutes)) {
////                                totalMinutes += Integer.parseInt(minutes);
////                            }
////                        }
////                        
////                        final BigDecimal spGroupKekkaJisu = getSpecialAttendExe(totalMinutes, param);
////                        
////                        if (null == student._attendMap.get(dateRange._key)) {
////                            student._attendMap.put(dateRange._key, new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0));
////                        }
////                        final Attendance attendance = (Attendance) student._attendMap.get(dateRange._key);
////                        
////                        if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
////                            attendance._lhrKekka = spGroupKekkaJisu;
////                        } else if (Attendance.GROUP_EVENT.equals(specialGroupCd)) {
////                            attendance._gyojiKekka = spGroupKekkaJisu;
////                        } else if (Attendance.GROUP_COMMITTEE.equals(specialGroupCd)) {
////                            attendance._iinkaiKekka = spGroupKekkaJisu;
////                        }
////                    }
                    
                    final Map combinedFlgKekkaMap = new TreeMap();
                    final List rowList = KnjDbUtils.query(db2, ps, new Object[] { student._schregno});
                    if (param._isOutputDebug) {
                    	log.info(" " + student._schregno + "  attend rowList size (" + dateRange._key + ") = " + rowList.size());
                    }
					for (final Iterator ait = rowList.iterator(); ait.hasNext();) {
                    	final Map map = (Map) ait.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(map, "SEMESTER"))) {
                            continue;
                        }
                        
                        
                        final String subclasscd = KnjDbUtils.getString(map, "SUBCLASSCD");
                        final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        
                        getMappedList(getMappedMap(combinedFlgKekkaMap, subclasscd), KnjDbUtils.getString(map, "IS_COMBINED_SUBCLASS")).add(map); 
                    }
                    
                    for (final Iterator ait = combinedFlgKekkaMap.keySet().iterator(); ait.hasNext();) {
                    	final String subclasscd = (String) ait.next();
                    	final Map combineFlgMapListMap = (Map) combinedFlgKekkaMap.get(subclasscd);

                    	final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T) || iclasscd == Integer.parseInt(CLASSCD_TOKUBETSUKATSUDO))) {

                        	if (param._isOutputDebug) {
                        		log.info(" key = " + dateRange._key + " | subclasscd = " + subclasscd + ", combineFlgMapListMap = " + combineFlgMapListMap.keySet());
                        	}

                        	BigDecimal lesson = null;
                        	BigDecimal sick = null;
                        	BigDecimal zero = new BigDecimal(0);
                        	final String flgSaki = "1";
                        	final String flgMoto = "0";
                        	if (mst._isSaki && combineFlgMapListMap.containsKey(flgMoto) && combineFlgMapListMap.containsKey(flgSaki)) {
                        		boolean hasAttend = false;
                        		for (final Iterator bit = getMappedList(getMappedMap(combinedFlgKekkaMap, subclasscd), flgMoto).iterator(); bit.hasNext();) {
                        			final Map row = (Map) bit.next();
                        			if (null == lesson) { lesson = zero; }
                        			lesson = lesson.add(new BigDecimal(KnjDbUtils.getString(row, "MLESSON")));
                        			if (null == sick) { sick = zero; }
                        			sick = sick.add(new BigDecimal(KnjDbUtils.getString(row, "SICK2")));
                        			log.info(" 出欠合併先科目直接入力 : " + mst._subclasscd + " " + mst._subclassname + ", schregno = " + student._schregno);
                        			hasAttend = true;
                        		}
                        		if (!hasAttend) {
                            		for (final Iterator bit = getMappedList(getMappedMap(combinedFlgKekkaMap, subclasscd), flgSaki).iterator(); bit.hasNext();) {
                            			final Map row = (Map) bit.next();
                            			if (null == lesson) { lesson = zero; }
                            			lesson = lesson.add(new BigDecimal(KnjDbUtils.getString(row, "MLESSON")));
                            			if (null == sick) { sick = zero; }
                            			sick = sick.add(new BigDecimal(KnjDbUtils.getString(row, "REPLACED_SICK")));
                            			hasAttend = true;
                            		}
                        		}

                        	} else if (mst._isSaki && combineFlgMapListMap.containsKey(flgSaki)) {

                        		for (final Iterator bit = getMappedList(getMappedMap(combinedFlgKekkaMap, subclasscd), flgSaki).iterator(); bit.hasNext();) {
                        			final Map row = (Map) bit.next();
                        			if (null == lesson) { lesson = zero; }
                        			lesson = lesson.add(new BigDecimal(KnjDbUtils.getString(row, "MLESSON")));
                        			if (null == sick) { sick = zero; }
                        			sick = sick.add(new BigDecimal(KnjDbUtils.getString(row, "REPLACED_SICK")));
                        		}

                        	} else { // (combineFlgMapListMap.containsKey(flgMoto))
                        		if (mst._isSaki) {
                        			log.info(" 出欠合併先科目 直接入力分 subclasscd = " + subclasscd);
                        		}
                        		for (final Iterator bit = getMappedList(getMappedMap(combinedFlgKekkaMap, subclasscd), flgMoto).iterator(); bit.hasNext();) {
                        			final Map row = (Map) bit.next();
                        			if (null == lesson) { lesson = zero; }
                        			lesson = lesson.add(new BigDecimal(KnjDbUtils.getString(row, "MLESSON")));
                        			if (null == sick) { sick = zero; }
                        			sick = sick.add(new BigDecimal(KnjDbUtils.getString(row, "SICK2")));
                        		}
                        	}
                        	if (null == lesson && null == sick) {
                        		continue;
                        	}
                        	
                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, sick);
                            
                            if (null == student._subclassMap.get(subclasscd)) {
                                final Subclass subclass = new Subclass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subclass);
                            }
                            final Subclass subclass = student.getSubclass(subclasscd);
                            subclass._attendMap.put(dateRange._key, subclassAttendance);
                        }
                    }
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
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
        final String _testcd;
        final String _score;
//        final String _assessLevel;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _replacemoto;
        final String _compCredit;
        final String _getCredit;
        final String _slump;
        final String _slumpMark;
        final String _slumpMarkName1;
        final String _slumpScore;
        final String _slumpScoreKansan;
        final String _provFlg;
        
        Score(
                final String testcd,
                final String score,
                final String assessLevel,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String replacemoto,
                final String slump,
                final String slumpMark,
                final String slumpMarkName1,
                final String slumpScore,
                final String slumpScoreKansan,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _testcd = testcd;
            _score = score;
//            _assessLevel = assessLevel;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpScoreKansan = slumpScoreKansan;
            _slumpMark = slumpMark;
            _slumpMarkName1 = slumpMarkName1;
            _provFlg = provFlg;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit(final Param param) {
            return enableCredit(param) ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit(final Param param) {
            return enableCredit(param) ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit(final Param param) {
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
        
//        private String getPrintSlump(final TestItem testItem) {
//            if (null != testItem._sidouinput) {
//                String rtn = null;
//                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
//                    rtn = _slumpMarkName1;
//                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点 
//                    rtn = _slumpScore;
//                }
//                return rtn;
//            }
//            return null;
//        }

        private boolean isKetten(final Param param, final TestItem testItem) {
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
            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
                return "1".equals(_score);
            }
            return NumberUtils.isNumber(_score) && NumberUtils.isNumber(param._ketten) && Double.parseDouble(param._ketten) > Double.parseDouble(_score);  //"1".equals(_assessLevel);
        }
        
        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            try {
                final String sql = sqlScore(param, stbtestcd);
                if (param._isOutputDebug) {
                	log.info(" subclass query start. sql = " + sql);
                }
                final List rowList = KnjDbUtils.query(db2, sql);
                if (param._isOutputDebug) {
                	log.info(" subclass query end.");
                }

                for (final Iterator it = rowList.iterator(); it.hasNext();) {
                	final Map map = (Map) it.next();
                    final Student student = getStudent(studentList, KnjDbUtils.getString(map, "SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    
                    final String testcd = KnjDbUtils.getString(map, "TESTCD");
                    final Rank gradeRank = new Rank(KnjDbUtils.getString(map, "GRADE_RANK"), KnjDbUtils.getString(map, "GRADE_AVG_RANK"), KnjDbUtils.getString(map, "GRADE_COUNT"), KnjDbUtils.getString(map, "GRADE_AVG"), KnjDbUtils.getString(map, "GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(KnjDbUtils.getString(map, "CLASS_RANK"), KnjDbUtils.getString(map, "CLASS_AVG_RANK"), KnjDbUtils.getString(map, "HR_COUNT"), KnjDbUtils.getString(map, "HR_AVG"), KnjDbUtils.getString(map, "HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(KnjDbUtils.getString(map, "COURSE_RANK"), KnjDbUtils.getString(map, "COURSE_AVG_RANK"), KnjDbUtils.getString(map, "COURSE_COUNT"), KnjDbUtils.getString(map, "COURSE_AVG"), KnjDbUtils.getString(map, "COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(KnjDbUtils.getString(map, "MAJOR_RANK"), KnjDbUtils.getString(map, "MAJOR_AVG_RANK"), KnjDbUtils.getString(map, "MAJOR_COUNT"), KnjDbUtils.getString(map, "MAJOR_AVG"), KnjDbUtils.getString(map, "MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            testcd,
                            KnjDbUtils.getString(map, "SCORE"),
                            KnjDbUtils.getString(map, "ASSESS_LEVEL"),
                            KnjDbUtils.getString(map, "AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // KnjDbUtils.getString(map, "KARI_HYOUTEI"),
                            KnjDbUtils.getString(map, "REPLACEMOTO"),
                            KnjDbUtils.getString(map, "SLUMP"),
                            KnjDbUtils.getString(map, "SLUMP_MARK"),
                            KnjDbUtils.getString(map, "SLUMP_MARK_NAME1"),
                            KnjDbUtils.getString(map, "SLUMP_SCORE"),
                            KnjDbUtils.getString(map, "SLUMP_SCORE_KANSAN"),
                            KnjDbUtils.getString(map, "COMP_CREDIT"),
                            KnjDbUtils.getString(map, "GET_CREDIT"),
                            KnjDbUtils.getString(map, "PROV_FLG")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(KnjDbUtils.getString(map, "SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = KnjDbUtils.getString(map, "SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final Subclass subclass = new Subclass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subclass);
                    }
                    if (null == testcd) {
                    	if (null != KnjDbUtils.getString(map, "GET_CREDIT") && null != KnjDbUtils.getString(map, "ZOUKA")) {
                            final Subclass subclass = student.getSubclass(subclasscd);
                            subclass._scoreMap.put(TESTCD_GAKUNEN_HYOTEI, score);
                    	}
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subclass = student.getSubclass(subclasscd);
                    subclass._scoreMap.put(testcd, score);
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
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
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
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM RELATIVEASSESS_MST L3 ");
            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("      ) ELSE ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM ASSESS_MST L3 ");
            stb.append("       WHERE L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("      ) ");
            stb.append("     END AS ASSESS_LEVEL ");
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
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
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
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
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
            
            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
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
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
            stb.append("            AND (W1.SIDOU_INPUT_INF = '1' AND W3.MARK IS NOT NULL ");
            stb.append("              OR W1.SIDOU_INPUT_INF = '2' AND W3.SCORE IS NOT NULL ");
            stb.append("                ) ");
            stb.append("     ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD");
            stb.append(" ) ");
            
            stb.append(", QUALIFIED AS(");
            stb.append("   SELECT ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("   FROM ");
            stb.append("       SCHREG_QUALIFIED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.CREDITS IS NOT NULL ");
            stb.append("       AND EXISTS (SELECT 'X' FROM SCHNO_A WHERE SCHREGNO = T1.SCHREGNO) ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" )");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            if (SEMEALL.equals(param._semester)) { // && "1".equals(param._zouka)) {
                stb.append("    UNION ");
                stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM QUALIFIED ");
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
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.ASSESS_LEVEL ");
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
//            if ("1".equals(param._zouka)) {
                stb.append("        ,CASE WHEN T33.GET_CREDIT IS NOT NULL OR TQ.CREDITS IS NOT NULL THEN VALUE(T33.GET_CREDIT, 0) + VALUE(TQ.CREDITS, 0) END AS GET_CREDIT ");
//            } else {
//                stb.append("        ,T33.GET_CREDIT ");
//            }
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
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
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");
            //資格取得
            stb.append("  LEFT JOIN QUALIFIED TQ ON TQ.SCHREGNO = T1.SCHREGNO AND TQ.SUBCLASSCD = T1.SUBCLASSCD ");

            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + CLASSCD_TOKUBETSUKATSUDO + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }
        
        public String toString() {
            final String slump = StringUtils.defaultString(_slumpScore, StringUtils.defaultString(_slumpScoreKansan, _slumpMark));
            return "(score = " + _score + "" + (null == slump ? "" : " (slump" + slump + ")") + (TESTCD_GAKUNEN_HYOTEI.equals(_testcd) ? (" [getCredit = " + _getCredit + ", compCredit = " + _compCredit + ", provFlg = " + _provFlg + "]") : "") +")";
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
            return "2".equals(param._rankDiv) ? _avgRank : _rank;
        }
    }
    
    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _semester;
        public String _scoreDivName;
        public String _semesterDetail;
        public DateRange _dateRange;
        public boolean _printScore;
        public String semester() {
            return _testcd.substring(0, 1);
        }
        public String scorediv() {
            return _testcd.substring(_testcd.length() - 2);
        }
        public boolean isKarihyotei() {
            return !SEMEALL.equals(semester()) && "09".equals(scorediv());
        }
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
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
        TestItem _testitem;
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
    
    private abstract static class Form {

        List _testcds;
        List _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
        int _formTestCount; // フォームに表示する考査の回数
        boolean _use5testForm; // 5回考査用フォームを使用するか
        private Param _param;
        Map _fieldInfoMap = Collections.EMPTY_MAP;
        
        protected Param param() {
            return _param;
        }
        
        abstract void init(final List testcdList);
        protected void initDebug() {
            if (_param._isOutputDebug) {
                for (int i = 0; i < _testcds.size(); i++) {
                    log.info(" testcds[" + i + "] = " + _testcds.get(i) + " : " + _testItems.get(i));
                }
                for (int i = 0; i < _attendRanges.length; i++) {
                    log.info(" attendRanges[" + i + "] = " + _attendRanges[i]);
                }
                for (int i = 0; i < _attendSubclassRanges.length; i++) {
                    log.info(" attendSubclassRanges[" + i + "] = " + _attendSubclassRanges[i]);
                }
                log.info(" _formTestCount = " + _formTestCount);
            }
        }

        abstract void print(final IPdf ipdf, final Student student);
        
        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        protected Attendance[] getAttendances(final Student student) {
            final Attendance[] attendances = new Attendance[_attendRanges.length];
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key) && !SEMEALL.equals(param()._semester)) {
                    	Attendance sum = new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0);
                        for (int j = 0; j < i; j++) {
                            final DateRange dateRange2 = (DateRange) _attendRanges[j];
                            if (null == dateRange2) {
                            	continue;
                            }
                        	final Attendance atttmp = (Attendance) student._attendMap.get(dateRange2._key);
                        	if (null != atttmp) {
                        		sum = sum.add(atttmp);
                        	}
                        }
                        sum._dateRange = dateRange; // 9学期印字ではあるが範囲が違うけど
                    	attendances[i] = sum;
                    } else {
                    	if (null != dateRange._testitem) {
                    		if (dateRange._testitem._testcd.compareTo(param()._testcd) > 0) {
                    			continue;
                    		}
                    	}
                    	attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                    	if (null != attendances[i]) {
                    		attendances[i]._dateRange = dateRange;
                    	}
                    }
                }
            }
            return attendances;
        }

//        protected String[] getAttendanceRemarks(final Student student) {
//            final String[] remarks = new String[_attendRanges.length];
//            for (int i = 0; i < _attendRanges.length; i++) {
//                final DateRange dateRange = (DateRange) _attendRanges[i];
//                if (null != dateRange) {
//                    if (SEMEALL.equals(dateRange._key)) {
//                        continue;
//                    }
//                    remarks[i] = (String) student._attendRemarkMap.get(dateRange._key);
//                }
//            }
//            return remarks;
//        }

        protected List getTestItems(
                final Param param,
                final List testcds
        ) {
            final List testitems = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                testitems.add(null);
            }
            for (int j = 0; j < testcds.size(); j++) {
                testitems.set(j, (TestItem) param._testitemMap.get(testcds.get(j)));
            }
            final List notFoundTestcds = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = (String) testcds.get(i);
                if (null == testitems.get(i)) {
                    notFoundTestcds.add(testcd);
                }
            }
            if (!notFoundTestcds.isEmpty()) {
                log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + notFoundTestcds + " / 実際のマスタのコード:" + param._testitemMap.keySet());
            }
            return testitems;
        }
        
//        protected DateRange[] getSemesterDetails(
//                final Param param,
//                final int max
//        ) {
//            final DateRange[] semesterDetails = new DateRange[max];
//            for (int i = 0; i < Math.min(max, param._semesterDetailList.size()); i++) {
//                semesterDetails[i] = (DateRange) param._semesterDetailList.get(i);
//            }
//            return semesterDetails;
//        }
        
//        public static String[] get_token(final String strx0, final int f_len, final int f_cnt) {
//
//            if (strx0 == null || strx0.length() == 0) {
//                return new String[] {};
//            }
//            final String strx = StringUtils.replace(StringUtils.replace(strx0, "\r\n", "\n"), "\r", "\n"); 
//            final String[] stoken = new String[f_cnt];        //分割後の文字列の配列
//            int slen = 0;                               //文字列のバイト数カウント
//            int s_sta = 0;                              //文字列の開始位置
//            int ib = 0;
//            for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
//                //改行マークチェック
//                if (strx.charAt(s_cur) == '\n') {
//                    stoken[ib] = strx.substring(s_sta, s_cur);
//                    ib++;
//                    slen = 0;
//                    s_sta = s_cur + 1;
//                } else{
//                    //文字数チェック
//                    int blen = 0;
//                    try{
//                        blen = (strx.substring(s_cur,s_cur+1)).getBytes("MS932").length;
//                    } catch (Exception e) {
//                        log.fatal("get_token exception", e);
//                    }
//                    slen += blen;
//                    if (slen > f_len) {
//                        stoken[ib] = strx.substring(s_sta, s_cur);
//                        ib++;
//                        slen = blen;
//                        s_sta = s_cur;
//                    }
//                }
//            }
//            if (slen > 0 && ib < f_cnt) {
//                stoken[ib] = strx.substring(s_sta);
//            }
//            return stoken;
//
//        }
        
        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        public static List getTokenList(final String source, final int bytePerLine) {
            if (source == null || source.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final List tokenList = new ArrayList();        //分割後の文字列の配列
            int startIndex = 0;                         //文字列の分割開始位置
            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
            for (int idx = 0; idx < source.length(); idx += 1) {
                //改行マークチェック
                if (source.charAt(idx) == '\r') {
                    continue;
                }
                if (source.charAt(idx) == '\n') {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = 0;
                    startIndex = idx + 1;
                } else {
                    final int sbytelen = KNJ_EditEdit.getMS932ByteLength(source.substring(idx, idx + 1));
                    byteLengthInLine += sbytelen;
                    if (byteLengthInLine > bytePerLine) {
                        tokenList.add(source.substring(startIndex, idx));
                        byteLengthInLine = sbytelen;
                        startIndex = idx;
                    }
                }
            }
            if (byteLengthInLine > 0) {
                tokenList.add(source.substring(startIndex));
            }
            return tokenList;
        }
        
//        protected static List setTestcd(final List testcdList, final int max, final String[] array) {
//            final List testcds;
//            log.info(" db testcdList = " + testcdList);
//            if (testcdList.isEmpty()) {
//                testcds = new ArrayList(Arrays.asList(array));
//            } else {
//                testcds = new ArrayList();
//                for (int i = 0; i < Math.min(testcdList.size(), max); i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    log.info(" testcds size = " + testcds.size() + " / testcdList = " + testcdList + ", size = " + testcdList.size());
//                    testcds.set(i, testcdList.get(i));
//                }
//                for (int i = max; i < array.length; i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, array[i]);
//                }
//            }
//            return testcds;
//        }
        
        protected List setTestcd3(final List testcdList, final String[] array, int min, final boolean has3Form) {
            List testcds;
            if (param()._isOutputDebug) {
            	log.info(" db testcdList = " + testcdList);
            }
            if (testcdList.isEmpty()) {
                testcds = new ArrayList(Arrays.asList(array));
            } else {
                testcds = new ArrayList(testcdList);
            }
            testcds.remove(TESTCD_GAKUNEN_HYOKA);
            testcds.remove(TESTCD_GAKUNEN_HYOTEI);
            if (testcds.size() > 5) {
                testcds = testcds.subList(0, 5);
            }
            while (testcds.size() < min) {
                testcds.add(null);
            }
            testcds.add(TESTCD_GAKUNEN_HYOKA);
            testcds.add(TESTCD_GAKUNEN_HYOTEI);
            return testcds;
        }

        // 学年評価、学年評定を除いた表記する考査の数
        protected Collection getTestItemSet(final List testItemList) {
            final Map map = new TreeMap();
            for (final Iterator it = testItemList.iterator(); it.hasNext();) {
                final TestItem ti = (TestItem) it.next();
                if (null == ti) {
                    continue;
                }
                map.put(ti._testcd, ti);
            }
            map.remove(TESTCD_GAKUNEN_HYOKA);
            map.remove(TESTCD_GAKUNEN_HYOTEI);
            return map.values();
        }
        
//        /**
//         * ただし「とりあえず設定できる分」以上にDBで設定していればそちらを使用
//         * @param testcdList DBから取得したテスト種別のリスト
//         * @param max とりあえず設定できる分
//         * @param array 未設定の場合に使用するデフォルトテスト種別
//         */
//        protected static List getTestcd2(final List testcdList, final int max, final int max2, final String[] array) {
//            final List testcds;
//            log.info(" db testcdList2 = " + testcdList);
//            if (testcdList.isEmpty()) {
//                testcds = new ArrayList(Arrays.asList(array));
//            } else {
//                testcds = new ArrayList();
//                for (int i = 0; i < array.length; i++) {
//                    testcds.add(null);
//                }
//                for (int i = 0; i < Math.min(testcdList.size(), testcds.size()); i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, (String) testcdList.get(i));
//                }
//                for (int i = Math.max(testcdList.size(), max); i < array.length; i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, array[i]); // 設定より
//                }
//                for (int i = max2; i < array.length; i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, array[i]);
//                }
//            }
//            return testcds;
//        }
        
        public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
//            if (Param._isDemo) {
//                return ipdf.VrsOutn(field, gyo, data);
//            }
            return ipdf.setRecordString(field, gyo, data);
        }
        
        protected Rank getGroupDivRank(final Score score) {
            return score._courseRank; // 「学年順位」の欄にコース順位を出力
        }

        protected String getGroupDivName() {
            return "学年順位";
        }

        protected List getPrintSubclassList(final Student student, final int max) {
            final List printSubclassList = new ArrayList();
            for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final Subclass subclass = student.getSubclass(subclasscd);
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                if (null == subclass || param()._isNoPrintMoto && subclass._mst._isMoto || _param._d026List.contains(subclasscd)) {
                    continue;
                }
                printSubclassList.add(subclass);
                if (printSubclassList.size() >= max) {
                    break;
                }
            }
            Collections.sort(printSubclassList);
            return printSubclassList;
        }
        
        protected void debugSlump(final Student student, final Subclass subclass, final TestItem testItem) {
            final Score score = subclass.getScore(testItem._testcd);
            final String scoreString = null == score ? null : score._score;
            final String slumpScore = ""; //null == score ? null : score.getPrintSlump(testItem);
            if (null != slumpScore) {
            	if (_param._isOutputDebug) {
            		log.info(" slumpScore " + slumpScore + " (score = " + scoreString + ") schregno = " + student._schregno + ", subclasscd = " + subclass._mst._subclasscd + ":" + subclass._mst._subclassname + ", testitem = " + testItem._testcd);
            	}
            }
        }
        
        protected TestItem testItem(final int i) {
            return (TestItem) _testItems.get(i);
        }
        
        protected void setNotPrintTestItem() {
            final TreeMap semesterTestItemListMap = new TreeMap();
            for (int i = 0; i < _testItems.size(); i++) {
                if (null != testItem(i) && !SEMEALL.equals(testItem(i)._semester)) {
                    getMappedList(semesterTestItemListMap, testItem(i)._semester).add(testItem(i));
                }
            }
            final Collection testItemSet = getTestItemSet(_testItems);
            loginfo(param(), " testItemSet = " + testItemSet);
            _formTestCount = testItemSet.size();
            _use5testForm = _formTestCount >= 5;
        }

        public void setForm(final IPdf ipdf, final String form) {
            //log.info(" form = " + form);
            int rtn = ipdf.VrSetForm(form, 4);
            if (rtn < 0) {
                throw new IllegalArgumentException("フォーム設定エラー:" + rtn);
            }
            if (ipdf instanceof SvfPdf) {
                SvfPdf svfpdf = (SvfPdf) ipdf;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(svfpdf.getVrw32alp());
            }
        }
        
        public boolean isKarihyoteiPrintAttendance(final TestItem testitem, final List testItems) {
            if (null == testitem) {
                return false;
            }
            if (!testitem.isKarihyotei()) {
                return true;
            }
            // 以下、仮評定
            // 学期詳細がなければ印字しない
            log.info(" karihyotei " + testitem + " ( semester_detail = " + testitem._semesterDetail + ")");
            if (null == testitem._semesterDetail) {
                return false;
            }
            for (int i = 0; i < testItems.size(); i++) {
                final TestItem ti = (TestItem) testItems.get(i);
                if (null == ti || ti == testitem || null == ti._semesterDetail) {
                    continue;
                }
                if (ti._semesterDetail.equals(testitem._semesterDetail)) {
                    log.warn("他の試験が同じ学期詳細をもつので出欠欄に表示しない: " + testitem);
                    return false;
                }
            }
            return true;
        }
        
        // パターンＣ
        public static class FormC extends Form {
            
            int _attendSemeAllPos;
            
            void init(final List testcdList) {
                _testcds = setTestcd3(testcdList, new String[] {"1010108", "2010108", "2020108", "3990008"}, 4, true);
                _testItems = getTestItems(param(), _testcds);

                _attendRanges = new DateRange[_testItems.size()];
                _attendSubclassRanges = new DateRange[_testItems.size()];
                final int last = _testItems.size() - 1;
                for (int i = 0, attendRangeIdx = 0; i < last; i++) {
                    final TestItem item = testItem(i);
                    if (null != item) {
                        final DateRange dr = new DateRange(item._testcd, item._semester, item._testitemname, item._dateRange._sdate, item._dateRange._edate);
                        if (!item.isKarihyotei() || item.isKarihyotei() && isKarihyoteiPrintAttendance(item, _testItems)) {
                        	attendRangeIdx = i;
                            _attendRanges[attendRangeIdx] = dr;
                            _attendRanges[attendRangeIdx]._testitem = item;
                        }
                        _attendSubclassRanges[i] = dr;
                    }
                }
                if (null != param().getSemester(SEMEALL)) {
                    final Semester seme9 = param().getSemester(SEMEALL);
                    _attendRanges[last] = new DateRange(seme9._semester, seme9._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                    //_attendSubclassRanges[last] = _attendRanges[last];
                }
                setNotPrintTestItem();
                initDebug();
            }

            void print(final IPdf ipdf, final Student student) {
                final String _form;
                loginfo(param(), " testItems = " + _testItems);
                _form = "KNJD186G.frm"; // 考査5列
                _attendSemeAllPos = 5;
                setForm(ipdf, _form);

                printCHeader(ipdf, student);
                printCAddress(ipdf, student);
                printCAttendance(ipdf, student);
                printCCommunication(ipdf, student);
                printCScore(ipdf, student);
            }
            
            void printCAddress(final IPdf ipdf, final Student student) {
                final Address address = student._address;
                if (!"1".equals(param()._address) || null == address) {
                    return;
                }
//                ipdf.setString("ZIPCD", address._zipcd); // 郵便番号
                if (!StringUtils.isBlank(address._zipcd)) {
                    ipdf.VrsOut("ZIPCD", "〒" + address._zipcd); // 郵便番号
                }
                final boolean useAddress2 = KNJ_EditEdit.getMS932ByteLength(address._address1) > 40 || KNJ_EditEdit.getMS932ByteLength(address._address2) > 40;
                ipdf.VrsOut(useAddress2 ? "ADDR1_2" : "ADDR1", address._address1); // 住所
                ipdf.VrsOut(useAddress2 ? "ADDR2_2" : "ADDR2", address._address2); // 住所
                if (null != address._addressee) {
                    ipdf.VrsOut(KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(address._addressee) + "　様") > 36 ? "ADDRESSEE2" : "ADDRESSEE", address._addressee + "　様"); // 受取人2
                }
            }
            
            void printCAttendance(final IPdf ipdf, final Student student) {
                final Attendance[] attendances = getAttendances(student);
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att || att._lesson == 0) {
                        continue;
                    }
                    final int line = SEMEALL.equals(att._dateRange._key) ? _attendSemeAllPos : i + 1;
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引等日数
                    ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
//                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                    ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                }
            }
            

            void printCCommunication(final IPdf ipdf, final Student student) {
                final List tokenList = KNJ_EditKinsoku.getTokenList(student._communication, 30, 5);
                for (int i = 0; i < tokenList.size(); i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), (String) tokenList.get(i));
                }
            }
            
            void printCHeader(final IPdf ipdf, final Student student) {
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName); // 担任
//                if (null != student.getHrAttendNo(param())) {
//                    ipdf.VrsOut("HR_ATTNO_NAME", "(" + student.getHrAttendNo(param()) + ")"); // 年組番・氏名
//                }
//                ipdf.setString("MAJORNAME", student._majorname); // 学科名
//                ipdf.setString("HR_NAME", student._hrName); // クラス名
//                ipdf.setString("ATTENDNO", student._attendno); // 出席番号
//                ipdf.setString("NAME", student._name); // 氏名
                final String smajorname = StringUtils.defaultString(student._majorname);
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("SCH_INFO", smajorname + "  " + shrname + sattendno + "番　　" + sname);
                ipdf.VrsOut("NENDO", param()._nendo); // 年度
                ipdf.VrsOut("TITLE", StringUtils.defaultString(param()._title, "通 知 表")); // 年度
                ipdf.VrsOut("RANK_NAME", getGroupDivName()); // 項目

                if (SEMEALL.equals(param()._semester)) {
                    ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param())); // 修得単位数
                }
                
//                final int maxLine2 = 8;
//                for (int j = 0; j < maxLine2; j++) {
//                    final int line = j + 1;
//                    ipdf.setStringn("COMMUNICATION", line, null); // 通信欄
//                }

                final Subclass subclass999999 = (Subclass) student.getSubclass(SUBCLASSCD999999);
                for (int ti = 0; ti < _testItems.size(); ti++) {
                    if (null != testItem(ti)) {
                        final boolean isGakunenHyotei = TESTCD_GAKUNEN_HYOTEI.equals(testItem(ti)._testcd);
                        final boolean isGakunenHyoka = TESTCD_GAKUNEN_HYOKA.equals(testItem(ti)._testcd);
                        boolean printGakunenHyokaHyoteiSumAvg = true;
                        final String line = isGakunenHyotei ? "9" : String.valueOf(ti + 1);
                        final String line2 = isGakunenHyoka ? "9" : line;
                        final TestItem testItem = testItem(ti);
                        ipdf.VrsOut("HYOKA" + line, StringUtils.defaultString(testItem._scoreDivName, "評価"));
                        String testname = StringUtils.defaultString(testItem._testitemname);
                        ipdf.VrsOut("TESTNAME" + line, testname); // テスト名
                    	if (testItem(ti)._testcd.compareTo(param()._testcd) > 0) {
                    		continue;
                    	}
                        if (!isGakunenHyotei && testItem._printScore) {
                            ipdf.VrsOut("KETTEN_CNT" + line2, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数
                        }
                        if (null != subclass999999._scoreMap.get(testItem(ti)._testcd)) {
                            final Score score = (Score) subclass999999._scoreMap.get(testItem(ti)._testcd);
                            final Rank rank = getGroupDivRank(score);
                            if (isGakunenHyotei) {
                                if (!student.hasKari(param()) || student.hasKari(param()) && "1".equals(param()._tutisyoPrintKariHyotei)) {
                                    if (printGakunenHyokaHyoteiSumAvg) {
                                        ipdf.VrsOut("AVE_VALUE", sishaGonyu(score._avg)); // 平均
                                        ipdf.VrsOut("TOTAL_VALUE", score._score); // 合計
                                    }
                                }
                            } else {
                                if (printGakunenHyokaHyoteiSumAvg) {
                                    ipdf.VrsOut("AVERAGE" + line2, sishaGonyu(score._avg)); // 平均
                                    ipdf.VrsOut("TOTAL" + line2, score._score); // 合計
                                }
                                ipdf.VrsOut("HR_CLASS_RANK" + line2, score._hrRank.getRank(param())); // クラス順位
                                ipdf.VrsOut("HR_CLASS_CNT" + line2, score._hrRank._count); // クラス人数
                                ipdf.VrsOut("GRADE_RANK" + line2, rank.getRank(param())); // 学年順位 or 学科順位
                                ipdf.VrsOut("GRADE_CNT" + line2, rank._count); // 学年人数 or 学科人数
                            }
                        }
                    }
                }
                for (int ari = 0; ari < _attendRanges.length; ari++) {
                    final DateRange dr = _attendRanges[ari];
                    if (null == dr) {
                        continue;
                    }
                    String attendTestname = "";
                    int attendTestnamePos;
                    if (SEMEALL.equals(dr._semester)) {
                    	TestItem gakunenHyoka = (TestItem) param()._testitemMap.get(TESTCD_GAKUNEN_HYOKA);
                    	if (null != gakunenHyoka) {
                    		attendTestname = gakunenHyoka._testitemname;
                    	}
                        attendTestnamePos = _attendSemeAllPos;
                    } else {
                        attendTestname = StringUtils.defaultString(dr._testitem._testitemname);
                        attendTestnamePos = ari + 1;
                    }
                    ipdf.VrsOutn("ATTEND_TESTNAME", attendTestnamePos, attendTestname); // テスト名（出欠の記録）
                }
                ipdf.VrsOut("LATE_TITLE", "遅刻回数");
                ipdf.VrsOut("EARLY_TITLE", "早退回数");
            }
            
            void printCScore(final IPdf ipdf, final Student student) {
            	final int maxSubclass = 32;
                int count = 0;
                final List printSubclassList = getPrintSubclassList(student, maxSubclass);
                final List gedanKekkanomiSubclassList = new ArrayList(); // 下行に印字する科目リスト
                final Map classcdSubclassListMap = new HashMap();
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    if (CLASSCD_TOKUBETSUKATSUDO.equals(subclass._mst._subclasscd.length() < 2 ? null : subclass._mst._subclasscd.substring(0, 2))) {
                    	gedanKekkanomiSubclassList.add(subclass);
                    	it.remove();
                    	continue;
                    }
                    getMappedList(classcdSubclassListMap, subclass._mst._classcd).add(subclass);
                }
                
                final int recordStartY = 1464 + 3;
                final int recordHeight = 50;
                
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final int ci = count + 1;
                    final Subclass subclass = (Subclass) it.next();
                    loginfo(param(), " subclass = " + subclass);
                    final List sameClasscdSubclassList = getMappedList(classcdSubclassListMap, subclass._mst._classcd);
                    final boolean isCenter = sameClasscdSubclassList.indexOf(subclass) == sameClasscdSubclassList.size() / 2;

                    String classname = null;
                    int classnameFieldY = recordStartY + count * recordHeight;
                    if (isCenter) {
                    	classname = subclass._mst._classname;
                    	if (sameClasscdSubclassList.size() % 2 == 0) {
                    		classnameFieldY -= recordHeight / 2;
                    	}
                    }
                    
                    printCSubclass(ipdf, student, classname, classnameFieldY, ci, subclass);
                    ipdf.VrEndRecord();
                    count++;
                }
                
                for (int i = count; i < maxSubclass - gedanKekkanomiSubclassList.size(); i++) {
                    final int ci = count + 1;
                    Form.setRecordString(ipdf, "CLASSNAME", ci, String.valueOf(i)); // 教科名
                    ipdf.VrAttribute("CLASSNAME", "X=10000");
    				Form.setRecordString(ipdf, "GRP", ci, "B" + String.valueOf(ci % 10)); // ブランク
                    ipdf.VrEndRecord();
                    count++;
                }
                for (int i = 0; i < gedanKekkanomiSubclassList.size(); i++) {
                    final Subclass subclass = (Subclass) gedanKekkanomiSubclassList.get(i);
                    final int ci = count + 1;
                    int classnameFieldY = recordStartY + count * recordHeight;
                    printCSubclass(ipdf, student, subclass._mst._classname, classnameFieldY, ci, subclass);
                    ipdf.VrEndRecord();
                }
            }

			private void printCSubclass(final IPdf ipdf, final Student student, final String classname, final int classnameFieldY, final int ci, final Subclass subclass) {
				final String subclasscd = subclass._mst._subclasscd;
				Form.setRecordString(ipdf, "GRP", ci, subclasscd.substring(0, 2));
				if (null != classname) {
				    final boolean isField2 = KNJ_EditEdit.getMS932ByteLength(classname) > 8;
					final String classnameField = "CLASSNAME" + (isField2 ? "_2" : "");
					Form.setRecordString(ipdf, classnameField, ci, classname); // 教科名
					if (!isField2) {
						ipdf.VrAttribute(classnameField, "Y=" + classnameFieldY); // 教科名
					}
				}
				final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname);
				final String subclassnameField;
				if (subclassnameKeta > 28) {
					subclassnameField = "SUBCLASSNAME2";
				} else {
					subclassnameField = "SUBCLASSNAME1";
				}
				Form.setRecordString(ipdf, subclassnameField, ci, subclass._mst._subclassname); // 科目名
				Form.setRecordString(ipdf, "CREDIT", ci, param().getCredits(subclasscd, student._course)); // 単位数
				for (int ti = 0; ti < _testItems.size(); ti++) {
				    final TestItem item = testItem(ti);
				    if (null == item || 0 > param()._testcd.compareTo(item._testcd) && !TESTCD_GAKUNEN_HYOKA.equals(item._testcd) && !TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
				        continue;
				    }
//                        if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && SEMEALL.equals(item._semester) && subclass._mst._isMoto) {
//                            continue;
//                        }
				    debugSlump(student, subclass, item);
				    final String line = String.valueOf(ti + 1);
			        if (TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
					    final Score score = subclass.getScore(item._testcd);
			        	if (null != score) {
			        		if ((!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei))) {
			        			Form.setRecordString(ipdf, "VALUE", ci, score._score); // 評定
			        		}
			        	}
			        } else {
			            final String field1;
			            String printScore = "";
			            if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd)) {
			                field1 = "SCORE9";
			                if (SEMEALL.equals(param()._semester)) {
							    final Score score = subclass.getScore(item._testcd);
							    if (null != score) {
							    	printScore = score._score; // RECORD_SCORE_DAT.SCORE
							    }
			                } else {
			                	// 指定学期までの平均
			                	final Map scoreMap = new HashMap();
			    				for (int ti2 = 0; ti2 < _testItems.size(); ti2++) {
			    				    final TestItem item2 = testItem(ti2);
			    				    if (null == item2 || 0 > param()._testcd.compareTo(item2._testcd)) {
			    				        continue;
			    				    }
			    				    if (TESTCD_GAKUNEN_HYOKA.equals(item2._testcd)) {
			    				    	continue;
			    				    }
			    				    final Score score2 = subclass.getScore(item2._testcd);
			    				    if (null != score2 && null != score2._score) {
			    				    	scoreMap.put(item2._testcd, score2._score);
			    				    }
			    				}
			    				if (!scoreMap.isEmpty()) {
			    					if (param()._isOutputDebug) {
			    						log.info(" " + student._schregno + ", subclasscd = " + subclasscd + ", scoreMap = " + scoreMap);
			    					}
			    				}
				                printScore = getAverage(scoreMap.values());
			                }
			            } else {
			                field1 = "SCORE" + line;
						    final Score score = subclass.getScore(item._testcd);
						    if (null != score) {
						    	printScore = score._score;
						    }
			            }
			            boolean isPrint = true;
//                          if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd) && "1".equals(param()._knjd186vNoPrintGakunenHyokaIfHyoteiNull)) {
//                              final Score gakunenHyotei = subclass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                              if (null == gakunenHyotei || null == gakunenHyotei._score || "1".equals(gakunenHyotei._provFlg)) {
//                                  // 学年評定がないので学年評価を印字しない
//                                  isPrint = false;
//                              }
//                              log.info(" subclass = " + subclass._mst + " : gakunenHyotei = " + gakunenHyotei + " => " + isPrint);
//                          }

			            if (isPrint) {
			            	Form.setRecordString(ipdf, field1, ci, printScore); // 評価
			            }
			        }
			        BigDecimal kekka = null;
				    final String pos;
				    if (SEMEALL.equals(item._testcd.substring(0, 1))) {
				        //attendKey = SEMEALL;
				        pos = SEMEALL;
						for (int ti2 = 0; ti2 < _testItems.size(); ti2++) {
						    final TestItem item2 = testItem(ti2);
						    if (null == item2 || 0 > param()._testcd.compareTo(item2._testcd)) {
						        continue;
						    }
						    if (TESTCD_GAKUNEN_HYOKA.equals(item2._testcd) || TESTCD_GAKUNEN_HYOTEI.equals(item2._testcd)) {
						    	continue;
						    }
					        final SubclassAttendance sa = subclass.getAttendance(item2._testcd);
						    if (null != sa && null != sa._sick) {
						    	if (null == kekka) {
						    		kekka = new BigDecimal(0);
						    	}
						    	kekka = kekka.add(sa._sick);
						    }
						}
				    } else {
				        pos = String.valueOf(line);
				        final SubclassAttendance sa = subclass.getAttendance(item._testcd);
					    if (null != sa && null != sa._sick) {
					    	kekka = sa._sick;
					    }
				    }
				    Form.setRecordString(ipdf, "KEKKA" + pos +"_2", ci, getAbsentStr(param(), kekka, false)); // 欠課時数
				}
			}

			private static String getAverage(final Collection scoreList) {
				BigDecimal sum = new BigDecimal(0);
				int count = 0;
				for (final Iterator it = scoreList.iterator(); it.hasNext();) {
					final String score = (String) it.next();
					if (!NumberUtils.isDigits(score)) {
						continue;
					}
					sum = sum.add(new BigDecimal(score));
					count += 1;
				}
				return count == 0 ? "" : sum.divide(new BigDecimal(count), 0, BigDecimal.ROUND_HALF_UP).toString();
			}
        }
    }
    
    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 71322 $ $Date: 2019-12-19 19:37:18 +0900 (木, 19 12 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _testcd;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final String _ketten;
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点
        final String _address; // 保護者住所
        final String _tutisyoPrintKariHyotei; // 仮評定を表示する
        final String _nendo;
        final String _printDate;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        
        private final Form _form;
        
        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMst;
        private Map _creditMst;
        private Map _recordMockOrderSdivDatMap;

        private KNJSchoolMst _knjSchoolMst;

//        private int _gradCredits;  // 卒業認定単位数
        
        private String _avgDiv;
        private String _d054Namecd2Max;
        private String _sidouHyoji;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private String _title;
        final String _h508Name1;
        private boolean _isNoPrintMoto;
        final Map _testitemMap;
        final List _semesterDetailList;
        final boolean _isOutputDebug;
        final Map _attendParamMap;
        List _d026List;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _testcd = request.getParameter("TESTCD");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _ketten = request.getParameter("KETTEN");
            _rankDiv = request.getParameter("RANK_DIV");
            _address = request.getParameter("ADDRESS");
            _tutisyoPrintKariHyotei = request.getParameter("tutisyoPrintKariHyotei");
            _printDate = StringUtils.replace(StringUtils.isBlank(request.getParameter("PRINT_DATE")) ? request.getParameter("CTRL_DATE") : request.getParameter("PRINT_DATE"), "/", "-");

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _form = new Form.FormC();
            _form._param = this;
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            setD054Namecd2Max(db2);
            _definecode = createDefineCode(db2);
//            _gradCredits = getGradCredits(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            setRecordMockOrderSdivDat(db2);
            _h508Name1 = getH508Name1(db2);
            loadNameMstD016(db2);
            loadNameMstD026(db2);
            _testitemMap = getTestItemMap(db2);
            _semesterDetailList = getSemesterDetailList(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186G' AND NAME = '" + propName + "' "));
        }
        
        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
        }

        private void setD054Namecd2Max(final DB2UDB db2) {
            final Map map = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(map, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(map, "NAME1");
        }
        
        /**
         * 名称マスタ NAMECD1='H508' NAMECD2='02'読込
         */
        private String getH508Name1(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H508' AND NAMECD2 = '02' "));
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
            final Map map = new HashMap();
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
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _hrJobName = KnjDbUtils.getString(row, "REMARK2");
            _title = KnjDbUtils.getString(row, "REMARK6");

            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
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
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
//            }
            
            _d026List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
            log.info("非表示科目:" + _d026List);
        }
        
//        // 卒業認定単位数の取得
//        private int getGradCredits(
//                final DB2UDB db2
//        ) {
//            int gradcredits = 0;
//            final String gradecreditsStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "'"));
//            if (NumberUtils.isDigits(gradecreditsStr)) {
//                gradcredits = Integer.parseInt(gradecreditsStr);
//            }
//            return gradcredits;
//        }
        
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
            _subclassMst = new HashMap();
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
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map map = (Map) it.next();
                final int classShoworder3 = Integer.parseInt(KnjDbUtils.getString(map, "CLASS_SHOWORDER3"));
                final int subclassShoworder3 = Integer.parseInt(KnjDbUtils.getString(map, "SUBCLASS_SHOWORDER3"));
                final boolean isSaki = "1".equals(KnjDbUtils.getString(map, "IS_SAKI"));
                final boolean isMoto = "1".equals(KnjDbUtils.getString(map, "IS_MOTO"));
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(map, "CLASSCD"), KnjDbUtils.getString(map, "SUBCLASSCD"), KnjDbUtils.getString(map, "CLASSABBV"), KnjDbUtils.getString(map, "CLASSNAME"), KnjDbUtils.getString(map, "SUBCLASSABBV"), KnjDbUtils.getString(map, "SUBCLASSNAME"), classShoworder3, subclassShoworder3, isSaki, isMoto);
                _subclassMst.put(KnjDbUtils.getString(map, "SUBCLASSCD"), mst);
            }
        }
        
        private String getCredits(final String subclasscd, final String course) {
            return (String) _creditMst.get(subclasscd + ":" + course);
        }
        
        private void setCreditMst(
                final DB2UDB db2
        ) {
            _creditMst = new HashMap();
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
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map map = (Map) it.next();
                _creditMst.put(KnjDbUtils.getString(map, "SUBCLASSCD") + ":" + KnjDbUtils.getString(map, "COURSE"), KnjDbUtils.getString(map, "CREDITS"));
            }
        }
        
        public List getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = {grade + "-" + coursecd + "-" + majorcd, "00" + "-" + coursecd + "-" + majorcd, grade + "-" + "0" + "-" + "000", "00" + "-" + "0" + "-" + "000"};
            for (int i = 0; i < keys.length; i++) {
                final List rtn = (List) _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) {
                    log.info(" set key = " + keys[i]);
                    return rtn;
                }
            }
            log.info(" set key = " + ArrayUtils.toString(keys) + " but nothing");
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
            	final Map map = (Map) it.next();
                final String key = KnjDbUtils.getString(map, "GRADE") + "-" + KnjDbUtils.getString(map, "COURSECD") + "-" + KnjDbUtils.getString(map, "MAJORCD");
                if (null == _recordMockOrderSdivDatMap.get(key)) {
                    _recordMockOrderSdivDatMap.put(key, new ArrayList());
                }
                ((List) _recordMockOrderSdivDatMap.get(key)).add(KnjDbUtils.getString(map, "TESTCD"));
            }
        }
        
        protected Map getTestItemMap(
                final DB2UDB db2
        ) {
            final Map testitemMap = new HashMap();
            try {
                final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T1.SEMESTER_DETAIL "
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
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map map = (Map) it.next();
                    final String testcd = KnjDbUtils.getString(map, "TESTCD");
                    final TestItem testitem = new TestItem();
                    testitem._testcd = testcd;
                    testitem._testitemname = KnjDbUtils.getString(map, "TESTITEMNAME");
                    testitem._sidouinput = KnjDbUtils.getString(map, "SIDOU_INPUT");
                    testitem._sidouinputinf = KnjDbUtils.getString(map, "SIDOU_INPUT_INF");
                    testitem._semester = KnjDbUtils.getString(map, "SEMESTER");
                    testitem._semesterDetail = KnjDbUtils.getString(map, "SEMESTER_DETAIL");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._semester, testitem._testitemname, KnjDbUtils.getString(map, "SDATE"), KnjDbUtils.getString(map, "EDATE"));
                    testitem._printScore = "1".equals(KnjDbUtils.getString(map, "PRINT"));
                    testitem._scoreDivName = KnjDbUtils.getString(map, "SCORE_DIV_NAME");
                    testitemMap.put(testcd, testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return testitemMap;
        }
        
        protected List getSemesterDetailList(
                final DB2UDB db2
        ) {
            final List semesterDetailList = new ArrayList();
            final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
                    + "  ,T1.SDATE "
                    + "  ,T1.EDATE "
                    + " FROM SEMESTER_DETAIL_MST T1 "
                    + " WHERE T1.YEAR = '" + _year + "' "
                    + " ORDER BY T1.SEMESTER_DETAIL ";
            log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map map = (Map) it.next();
                semesterDetailList.add(new DateRange(KnjDbUtils.getString(map, "SEMESTER_DETAIL"), KnjDbUtils.getString(map, "SEMESTER"), KnjDbUtils.getString(map, "SEMESTERNAME"), KnjDbUtils.getString(map, "SDATE"), KnjDbUtils.getString(map, "EDATE")));
            }
            return semesterDetailList;
        }
    }
}

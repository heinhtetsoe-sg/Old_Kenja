// kanji=漢字
/*
 * $Id: 4e1cdd24fcdc1cc19d100545c12a43e966711abb $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4e1cdd24fcdc1cc19d100545c12a43e966711abb $
 */
public class KNJD667F {

    private static final Log log = LogFactory.getLog("KNJD667F.class");

    private Param _param;

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private List getPageList(final List studentList) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (null == current || current.size() >= 50) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
        }
        return rtn;
    }

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd);
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List studentListAll = Student.getStudentList(db2, _param);
        TestScore.setScoreList(db2, _param, studentListAll);
        log.debug(" studentList size = " + studentListAll.size());
        Collections.sort(studentListAll, new Student.StudentComparator(_param));

        final List subclasscdList = new ArrayList();
        final List beforeList = new ArrayList(); // 3科、5科に含まれている科目
        final List afterList = new ArrayList(); // 3科、5科に含まれていない科目
        final Map subclassNameMap = new HashMap();
        for (final Iterator it = studentListAll.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._testScoreMap.values().iterator(); sit.hasNext();) {
                final TestScore ts = (TestScore) sit.next();
                if (SUBCLASS5.equals(ts._subclasscd)) {
                    continue;
                }
                if (ts._inGroup) {
                    if (!beforeList.contains(ts._subclasscd)) {
                        beforeList.add(ts._subclasscd);
                    }
                } else {
                    if (!afterList.contains(ts._subclasscd)) {
                        afterList.add(ts._subclasscd);
                    }
                }
                if (!subclassNameMap.containsKey(ts._subclasscd)) {
                    subclassNameMap.put(ts._subclasscd, ts._subclassname);
                }
            }
        }
        subclassNameMap.put(SUBCLASS5, "総合");

        subclasscdList.addAll(beforeList);
        subclasscdList.add(SUBCLASS5);
        subclasscdList.addAll(afterList);

        final String form = "KNJD667F.frm";

        final List pageList = getPageList(studentListAll);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List studentList = (List) it.next();

            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", _param.changePrintYear() + " " + StringUtils.defaultString(_param._testName) + " 成績一覧"); // タイトル
            svf.VrsOut("GRADE", _param._gradeName1); // 学年

            for (int i = 0; i < subclasscdList.size(); i++) {
                final String subclasscd = (String) subclasscdList.get(i);
                final String subclassname = (String) subclassNameMap.get(subclasscd);
                svf.VrsOut("CLASS_NAME" + String.valueOf(i + 1), subclassname); // 教科名

                final String junname;
                if (SUBCLASS5.equals(subclasscd)) {
                    junname = "総合順位";
                } else {
                    junname = StringUtils.defaultString(ketaMax(subclassname, 8 - 2)) + "順";
                }
                final int junameKeta = getMS932ByteLength(junname);

                svf.VrsOut("CLASS_RANK_NAME" + String.valueOf(i + 1) + (junameKeta > 4 ? "_2" : ""), junname); // 教科順位名
            }


            for (int gyo = 1; gyo <= studentList.size(); gyo++) {
                final Student student = (Student) studentList.get(gyo - 1);

                printStudent(svf, student, subclasscdList, gyo);

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }

    private static String ketaMax(final String subclassname, final int max) {
        if (null == subclassname || getMS932ByteLength(subclassname) < max) {
            return subclassname;
        }
        final StringBuffer stb = new StringBuffer();
        int sum = 0;
        for (int i = 0; i < subclassname.length(); i++) {
            final String ch = String.valueOf(subclassname.charAt(i));
            final int len = getMS932ByteLength(ch);
            if (sum + len > max) {
                break;
            }
            stb.append(ch);
            sum += len;
        }
        return stb.toString();
    }

    private void printStudent(final Vrw32alp svf, final Student student, final List subclasscdList, final int gyo) {

        final int nameKeta = getMS932ByteLength(student._name);
        svf.VrsOutn("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : "1"), gyo, student._name); // 氏名
        svf.VrsOutn("HR_NAME", gyo, student._hrNameAbbv); // 組
        svf.VrsOutn("NO", gyo, (NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : StringUtils.defaultString(student._attendNo))); // 出席番号
        svf.VrsOutn("TYPE", gyo, student._A025Namespare1); // タイプ
        if (!(StringUtils.isEmpty(student._handicap) || "001".equals(student._handicap))) {
            // 「空白か001」以外は○
            svf.VrsOutn("TYPE_B", gyo, "○"); // タイプB
        }

        for (int i = 0; i < subclasscdList.size(); i++) {
            final String subclasscd = (String) subclasscdList.get(i);

            final TestScore testScore = (TestScore) student._testScoreMap.get(subclasscd);
            if (null != testScore) {
                svf.VrsOutn("CLASS_SCORE" + String.valueOf(i + 1), gyo, testScore._score); // 教科得点
                if (null != testScore._majorRank) {
                    svf.VrsOutn("CLASS_RANK" + String.valueOf(i + 1), gyo, testScore._majorRank.toString()); // 教科順位
                }
            }
        }
    }

    private static class Student {

        private static class StudentComparator implements Comparator {
            final Integer _rankMax = new Integer(Integer.MAX_VALUE);
            final Param _param;

            StudentComparator(final Param param) {
                _param = param;
            }

            public int compare(final Object o1, final Object o2) {
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                int ret;
                if ("2".equals(_param._outdiv)) {
                    final TestScore ts1 = (TestScore) s1._testScoreMap.get(SUBCLASS5);
                    final TestScore ts2 = (TestScore) s2._testScoreMap.get(SUBCLASS5);
                    if (null != ts1 && null == ts2) {
                        return -1;
                    } else if (null == ts1 && null != ts2) {
                        return 1;
                    } else if (null != ts1 && null != ts2) {
                        final Integer r1 = null == ts1._majorRank ? _rankMax : ts1._majorRank;
                        final Integer r2 = null == ts2._majorRank ? _rankMax : ts2._majorRank;
                        ret = r1.compareTo(r2);
                        if (0 != ret) return ret;
                    }
                }
                ret = StringUtils.defaultString(s1._hrClass, "999").compareTo(StringUtils.defaultString(s2._hrClass, "999"));
                if (0 != ret) return ret;
                ret = StringUtils.defaultString(s1._attendNo, "999").compareTo(StringUtils.defaultString(s2._attendNo, "999"));
                return ret;
            }
        }

        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final String _handicap;
        final String _A025Namespare1;
        final Map _testScoreMap;

        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final String handicap,
                final String A025Namespare1
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _handicap = handicap;
            _A025Namespare1 = A025Namespare1;
            _testScoreMap = new TreeMap();
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                // log.info("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"),
                                                  rs.getString("GRADE"),
                                                  rs.getString("HR_CLASS"),
                                                  rs.getString("ATTENDNO"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("HR_NAMEABBV"),
                                                  "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                                  rs.getString("COURSECD"),
                                                  rs.getString("COURSENAME"),
                                                  rs.getString("MAJORCD"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODE"),
                                                  rs.getString("COURSECODENAME"),
                                                  rs.getString("HANDICAP"),
                                                  rs.getString("A025_NAMESPARE1"));
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return studentList;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     VSCH.SCHREGNO, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGDH.HR_NAMEABBV, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.REAL_NAME, ");
            stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     L1.COURSENAME, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     L1.MAJORNAME, ");
            stb.append("     VSCH.COURSECODE, ");
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     BASE.HANDICAP, ");
            stb.append("     NMA025.NAMESPARE1 AS A025_NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT VSCH ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = VSCH.YEAR ");
            stb.append("          AND VSCH.SEMESTER = REGDH.SEMESTER ");
            stb.append("          AND VSCH.GRADE = REGDH.GRADE ");
            stb.append("          AND VSCH.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
            stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
            stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
            stb.append("     LEFT JOIN NAME_MST NMA025 ON NMA025.NAMECD1 = 'A025' AND NMA025.NAMECD2 = BASE.HANDICAP ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._regdSemester + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append("     AND VSCH.SCHREGNO IN (SELECT DISTINCT ");
            stb.append("                               STD.SCHREGNO ");
            stb.append("                           FROM ");
            stb.append("                               CHAIR_STD_DAT STD  ");
            stb.append("                               LEFT JOIN CHAIR_DAT CHA ON STD.YEAR     = CHA.YEAR ");
            stb.append("                                                      AND STD.SEMESTER = CHA.SEMESTER ");
            stb.append("                                                      AND STD.CHAIRCD  = CHA.CHAIRCD ");
            stb.append("                               INNER JOIN REC_SUBCLASS_GROUP_DAT REC ON REC.YEAR          = CHA.YEAR ");
            stb.append("                                                                    AND REC.GRADE         = '" + param._grade + "' ");
            stb.append("                                                                    AND REC.CLASSCD       = CHA.CLASSCD ");
            stb.append("                                                                    AND REC.SCHOOL_KIND   = CHA.SCHOOL_KIND ");
            stb.append("                                                                    AND REC.CURRICULUM_CD = CHA.CURRICULUM_CD ");
            stb.append("                                                                    AND REC.SUBCLASSCD    = CHA.SUBCLASSCD ");
            stb.append("                           WHERE ");
            stb.append("                                   STD.YEAR     = '" + param._year + "' ");
            stb.append("                           ) ");
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }
    }

    private static class TestScore {
        final String _schregno;
        final String _subclasscd;
        final String _subclassname;
        final boolean _inGroup;
        final String _score;
        final Integer _majorRank;
        final String _avg;
        final String _count;
        final String _valueDi;

        TestScore(
            final String schregno,
            final String subclasscd,
            final String subclassname,
            final boolean inGroup,
            final String score,
            final Integer majorRank,
            final String avg,
            final String count,
            final String valueDi
        ) {
            _schregno = schregno;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _inGroup = inGroup;
            _score = score;
            _majorRank = majorRank;
            _avg = avg;
            _count = count;
            _valueDi = valueDi;
        }

        public static void setScoreList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    ps.setString(2, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        if (null == schregno) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final boolean inGroup = null != rs.getString("IN_GROUP");
                        final String score = rs.getString("SCORE");
                        final Integer majorRank = NumberUtils.isDigits(rs.getString("MAJOR_RANK")) ? Integer.valueOf(rs.getString("MAJOR_RANK")) : null;
                        final String avg = rs.getString("AVG");
                        final String count = rs.getString("COUNT");
                        final String valueDi = rs.getString("VALUE_DI");
                        final TestScore testScore = new TestScore(schregno, subclasscd, subclassname, inGroup, score, majorRank, avg, count, valueDi);
                        student._testScoreMap.put(testScore._subclasscd, testScore);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   RANK.SCHREGNO, ");
            stb.append("   RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   CASE WHEN RG3.GROUP_DIV IS NOT NULL OR RG5.GROUP_DIV IS NOT NULL THEN 1 END AS IN_GROUP, ");
            stb.append("   RANK.SCORE, ");
            stb.append("   RANK.MAJOR_RANK, ");
            stb.append("   AVE.AVG, ");
            stb.append("   AVE.COUNT, ");
            stb.append("   TSCORE.VALUE_DI ");
            stb.append(" FROM ");
            stb.append(" RECORD_SCORE_DAT TSCORE  ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RANK ON RANK.YEAR = TSCORE.YEAR ");
            stb.append("   AND RANK.SEMESTER = TSCORE.SEMESTER ");
            stb.append("   AND RANK.TESTKINDCD = TSCORE.TESTKINDCD ");
            stb.append("   AND RANK.TESTITEMCD = TSCORE.TESTITEMCD ");
            stb.append("   AND RANK.SCORE_DIV = TSCORE.SCORE_DIV ");
            stb.append("   AND RANK.CLASSCD = TSCORE.CLASSCD ");
            stb.append("   AND RANK.SCHOOL_KIND = TSCORE.SCHOOL_KIND ");
            stb.append("   AND RANK.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
            stb.append("   AND RANK.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append("   AND RANK.SCHREGNO = TSCORE.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = TSCORE.SCHREGNO ");
            stb.append("   AND REGD.YEAR = TSCORE.YEAR ");
            stb.append("   AND REGD.SEMESTER = '" + param._regdSemester + "' ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = TSCORE.CLASSCD ");
            stb.append("   AND SUBM.SCHOOL_KIND = TSCORE.SCHOOL_KIND  ");
            stb.append("   AND SUBM.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
            stb.append("   AND SUBM.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT RG3 ON RG3.YEAR = TSCORE.YEAR ");
            stb.append("   AND RG3.GROUP_DIV = '3' ");
            stb.append("   AND RG3.GRADE = REGD.GRADE ");
            stb.append("   AND RG3.COURSECD = REGD.COURSECD ");
            stb.append("   AND RG3.MAJORCD = REGD.MAJORCD ");
            stb.append("   AND RG3.COURSECODE = REGD.COURSECODE ");
            stb.append("   AND RG3.CLASSCD = TSCORE.CLASSCD ");
            stb.append("   AND RG3.SCHOOL_KIND = TSCORE.SCHOOL_KIND  ");
            stb.append("   AND RG3.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
            stb.append("   AND RG3.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT RG5 ON RG5.YEAR = TSCORE.YEAR ");
            stb.append("   AND RG5.GROUP_DIV = '5' ");
            stb.append("   AND RG5.GRADE = REGD.GRADE ");
            stb.append("   AND RG5.COURSECD = REGD.COURSECD ");
            stb.append("   AND RG5.MAJORCD = REGD.MAJORCD ");
            stb.append("   AND RG5.COURSECODE = REGD.COURSECODE ");
            stb.append("   AND RG5.CLASSCD = TSCORE.CLASSCD ");
            stb.append("   AND RG5.SCHOOL_KIND = TSCORE.SCHOOL_KIND  ");
            stb.append("   AND RG5.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
            stb.append("   AND RG5.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVE ON AVE.YEAR = TSCORE.YEAR ");
            stb.append("   AND AVE.SEMESTER = TSCORE.SEMESTER ");
            stb.append("   AND AVE.TESTKINDCD = TSCORE.TESTKINDCD ");
            stb.append("   AND AVE.TESTITEMCD = TSCORE.TESTITEMCD ");
            stb.append("   AND AVE.SCORE_DIV = TSCORE.SCORE_DIV ");
            stb.append("   AND AVE.CLASSCD = TSCORE.CLASSCD ");
            stb.append("   AND AVE.SCHOOL_KIND = TSCORE.SCHOOL_KIND ");
            stb.append("   AND AVE.CURRICULUM_CD = TSCORE.CURRICULUM_CD  ");
            stb.append("   AND AVE.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append("   AND AVE.AVG_DIV = '5' ");
            stb.append("   AND AVE.GRADE = '" + param._grade + "' ");
            stb.append("   AND AVE.HR_CLASS = '000' ");
            stb.append("   AND AVE.COURSECD = '0' ");
            stb.append("   AND AVE.MAJORCD = '001' ");
            stb.append("   AND AVE.COURSECODE = '0000' ");
            stb.append(" WHERE ");
            stb.append("   TSCORE.YEAR = '" + param._year + "' ");
            stb.append("   AND TSCORE.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("   AND TSCORE.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   RANK.SCHREGNO, ");
            stb.append("   RANK.SUBCLASSCD, ");
            stb.append("   CAST (NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("   CAST (NULL AS INTEGER) AS IN_GROUP, ");
            stb.append("   RANK.SCORE, ");
            stb.append("   RANK.MAJOR_RANK, ");
            stb.append("   AVE.AVG, ");
            stb.append("   AVE.COUNT, ");
            stb.append("   CAST (NULL AS VARCHAR(1)) AS VALUE_DI ");
            stb.append(" FROM ");
            stb.append(" RECORD_RANK_SDIV_DAT RANK ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVE ON AVE.YEAR = RANK.YEAR ");
            stb.append("   AND AVE.SEMESTER = RANK.SEMESTER ");
            stb.append("   AND AVE.TESTKINDCD = RANK.TESTKINDCD ");
            stb.append("   AND AVE.TESTITEMCD = RANK.TESTITEMCD ");
            stb.append("   AND AVE.SCORE_DIV = RANK.SCORE_DIV ");
            stb.append("   AND AVE.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append("   AND AVE.AVG_DIV = '5' ");
            stb.append("   AND AVE.GRADE = '" + param._grade + "' ");
            stb.append("   AND AVE.HR_CLASS = '000' ");
            stb.append("   AND AVE.COURSECD = '0' ");
            stb.append("   AND AVE.MAJORCD = '001' ");
            stb.append("   AND AVE.COURSECODE = '0000' ");
            stb.append(" WHERE ");
            stb.append("   RANK.YEAR = '" + param._year + "' ");
            stb.append("   AND RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("   AND RANK.SUBCLASSCD = '" + SUBCLASS5 + "' ");
            stb.append("   AND RANK.SCHREGNO = ? ");
            stb.append(" ORDER BY SUBCLASSCD ");
            return stb.toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 60855 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _testcd;
        final String _testName;
        final String _semester;
        final String _outdiv;
        final String _grade;
        final String _gradeName1;
        final String _z010;
        final boolean _isSeireki;
        final String _regdSemester;
        private String _semesterName;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _testcd = request.getParameter("TESTCD");
            _semester = request.getParameter("SEMESTER");
            _outdiv = request.getParameter("OUTDIV");
            _regdSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _gradeName1 = getGradeName1(db2);

            _z010 = setNameMst(db2, "Z010", "00");
            _isSeireki = "2".equals(setNameMst(db2, "Z012", "01"));
        }

        private String getGradeName1(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNameMst(_year, namecd1, namecd2);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            }
        }
    }
}

// eof

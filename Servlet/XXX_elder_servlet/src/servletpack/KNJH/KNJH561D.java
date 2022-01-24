// kanji=漢字
/*
 * $Id: bed8712ada3f624c093899eaeb344c62bd9346b1 $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 個人成績表印刷
 * @version $Id: bed8712ada3f624c093899eaeb344c62bd9346b1 $
 */
public class KNJH561D {

    private static final Log log = LogFactory.getLog(KNJH561D.class);

    private Param _param;
    private boolean _hasData = false;

    private static final String RANK_KANSAN = "03";
    private static final String RANK_GOUKEI = "01";
    private static final String RANK_GRADE = "01";

    private static final String RANK_DATA_DIV_SCORE = "01";
    private static final String RANK_DATA_DIV_AVG = "02";
    private static final String RANK_DATA_DIV_DEVIATION = "03";

    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";
    private static final String RANK_DIV_COURSE = "03";

    private static final String _999999 = "999999";
    private static final String _999999AVG = "999999AVG";

    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_HR_CLASS = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_RYO = "07";

    private static final String AVG_DATA_SCORE = "1";

//    private static final String HYOTEI_TESTCD = "9990009";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.fatal("$Revision: 62332 $ $Date: 2018-09-14 15:07:39 +0900 (金, 14 9 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = new Param(db2, request);

            //SVF出力
            printMain(db2, svf);

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, _hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り
    
    private List getPageList(final List studentList, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (null == current || current.size() >= max || !befGradeClass.equals(student._grade + student._hrclass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrclass;
        }
        return rtn;
    }
    
    private static Integer toInteger(final String intString) {
        if (NumberUtils.isDigits(intString)) {
            return Integer.valueOf(intString);
        }
        return null;
    }

    private static String toStr(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishagonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static String sishagonyu(final String s) {
        return NumberUtils.isNumber(s) ? sishagonyu(new BigDecimal(s)) : null;
    }
    
    private static String toString(final String v, final int scale) {
        return !NumberUtils.isNumber(v) ? null : new BigDecimal(v).setScale(scale).toString();
    }
    
    private static boolean isAkaten(final Rank rank, final Param param) {
        if (NumberUtils.isNumber(rank._score) && Double.parseDouble(rank._score)  < param._akaten) {
            return true;
        }
        return false;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map avgMap = ProficiencyAverage.getAverageMap(db2, _param);
        final List dataListAll = Student.getStudentList(db2, _param);

        final String form = "KNJH561D.frm";
        final int maxCol = 2;
        final int maxLine = 4;
        final List pageList = getPageList(dataListAll, maxLine * maxCol);
        
        for (int pi = 0; pi < pageList.size(); pi++) {

            svf.VrSetForm(form, 1);

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                
                final int line = (j + 1) / maxCol + ((j + 1) % maxCol == 0 ? 0 : 1);
                final int col = (j % maxCol) + 1;
                final Student student = (Student) dataList.get(j);
                
                printStudent(svf, avgMap, line, col, student);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printStudent(final Vrw32alp svf, final Map avgMap, final int line, final int col, final Student student) {
        final String title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度 " + StringUtils.defaultString(_param._testitemname) + " 個人成績票";
        svf.VrsOutn("TITLE" + col, line, title); // タイトル
        svf.VrsOutn("HR_NAME" + col, line, student._hrClassName1); // 
        svf.VrsOutn("HR_NAME" + col + "_1", line, StringUtils.defaultString(student._hrClassName1) + "平均"); // クラス名
        svf.VrsOutn("HR_NAME" + col + "_2", line, StringUtils.defaultString(student._hrClassName1) + "平均"); // クラス名
        svf.VrsOutn("NO" + col, line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)); // 出席番号
        
        final int ketaName = getMS932ByteLength(student._name);
        svf.VrsOutn("NAME" + col + (ketaName <= 20 ? "_1" : ketaName <= 30 ? "_2" : "_3"), line, student._name);

        int akaten = 0;
        final List subclassList = student.getSubclassList();
        for (int subi = 0; subi < Math.min(11, subclassList.size()); subi++) {
            final Rank rank = (Rank) subclassList.get(subi);
            final String ssubi = String.valueOf(subi + 1);

            svf.VrsOutn("SUBCLASS_NAME" + col + "_" + ssubi, line, (String) _param._subclassnameMap.get(rank._subclasscd)); // 科目名
            
            svf.VrsOutn("SCORE" + col + "_" + ssubi, line, rank._score); // 得点
            if (isAkaten(rank, _param)) {
                akaten += 1;
            }
            svf.VrsOutn("GRADE_AVE" + col + "_" + ssubi, line, sishagonyu(ProficiencyAverage.getAverageGrade(avgMap, student._grade, rank._subclasscd)._avg)); // 学年平均
            svf.VrsOutn("HR_AVE" + col + "_" + ssubi, line, sishagonyu(ProficiencyAverage.getAverageHr(avgMap, student._grade, student._hrclass, rank._subclasscd)._avg)); // クラス平均
        }

        final Rank subclassTotal = (Rank) student._ranks.get(_999999);

        svf.VrsOutn("SCORE_ALL" + col, line, null == subclassTotal ? "" : subclassTotal._score); // 得点合計
        svf.VrsOutn("SCORE_AVE" + col, line, null == subclassTotal ? "" : sishagonyu(subclassTotal._avg)); // 得点平均
        svf.VrsOutn("GRADE_ALL_AVE" + col, line, sishagonyu(ProficiencyAverage.getAverageGrade(avgMap, student._grade, _999999)._avg)); // 学年合計平均
        svf.VrsOutn("GRADE_AVE_AVE" + col, line, sishagonyu(ProficiencyAverage.getAverageGrade(avgMap, student._grade, _999999AVG)._avg)); // 学年平均平均
        svf.VrsOutn("HR_ALL_AVE" + col, line, sishagonyu(ProficiencyAverage.getAverageHr(avgMap, student._grade, student._hrclass, _999999)._avg)); // クラス合計平均
        svf.VrsOutn("HR_AVE_AVE" + col, line, sishagonyu(ProficiencyAverage.getAverageHr(avgMap, student._grade, student._hrclass, _999999AVG)._avg)); // クラス平均平均
        svf.VrsOutn("HR_RANK" + col, line, null == subclassTotal ? "" : toStr(subclassTotal._hrRank)); // クラス順位
        svf.VrsOutn("HR_NUM" + col, line, ProficiencyAverage.getAverageHr(avgMap, student._grade, student._hrclass, _999999)._count); // クラス人数
        svf.VrsOutn("GRADE_RANK" + col, line, null == subclassTotal ? "" : toStr(subclassTotal._gradeRank)); // 学年順位
        svf.VrsOutn("GRADE_NUM" + col, line, ProficiencyAverage.getAverageGrade(avgMap, student._grade, _999999)._count); // 学年人数
        svf.VrsOutn("RED_POINT" + col, line, String.valueOf(akaten)); // 赤点
        svf.VrsOutn("SUBJECT_NUM" + col, line, String.valueOf(student.getSubclassList().size())); // 科目数
    }
    
    private static class Student {
        final String _schregno;
        final String _entDiv;
        final String _grade;
        final String _gradeName1;
        final String _hrclass;
        final String _hrClassName1;
        final String _course;
        final String _coursecode;
        final String _coursecodeName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _inoutCd;
        final String _residentcdAbbv2;
        final String _residentcdNamespare1;
        final Map _ranks = new TreeMap();
//        final Map _classScoreMap = new HashMap();
//        final Map _classRankMap = new HashMap();
        public Student(final String schregno, final String entDiv, final String name, final String grade, final String gradeName1, final String hrclass, final String hrClassName1, final String course, final String coursecode, final String coursecodeName, final String attendno, final String sex, final String inoutCd, final String residentcdAbbv2, final String residentcdNamespare1) {
            _schregno = schregno;
            _entDiv = entDiv;
            _name = name;
            _grade = grade;
            _gradeName1 = gradeName1;
            _hrclass = hrclass;
            _hrClassName1 = hrClassName1;
            _course = course;
            _coursecode = coursecode;
            _coursecodeName = coursecodeName;
            _attendno = attendno;
            _sex = sex;
            _inoutCd = inoutCd;
            _residentcdAbbv2 = residentcdAbbv2;
            _residentcdNamespare1 = residentcdNamespare1;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            final Map studentMap = new HashMap();
            final String sql = Student.sqlStudent(param);
//            log.info(" student sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "ENT_DIV"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "GRADE_NAME1"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "COURSECODE"), KnjDbUtils.getString(row, "COURSECODENAME"), KnjDbUtils.getString(row, "ATTENDNO"), KnjDbUtils.getString(row, "SEX"), KnjDbUtils.getString(row, "INOUTCD"), KnjDbUtils.getString(row, "RESIDENTCD_ABBV2"), KnjDbUtils.getString(row, "RESIDENTCD_NAMESPARE1"));
                studentMap.put(student._schregno, student);
                studentList.add(student);
            }

            final String sqlRank = Rank.sqlRank(param);
            for (final Iterator it = KnjDbUtils.query(db2, sqlRank).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String valueDi = KnjDbUtils.getString(row, "SCORE_DI");
                final String score = null != valueDi ? valueDi : KnjDbUtils.getString(row, "SCORE");
                final String avg = KnjDbUtils.getString(row, "AVG");
                final Integer gradeRank = toInteger(KnjDbUtils.getString(row, "GRADE_RANK"));
                final Integer courseRank = toInteger(KnjDbUtils.getString(row, "COURSE_RANK"));
                final Integer hrRank = toInteger(KnjDbUtils.getString(row, "HR_RANK"));
                student._ranks.put(subclasscd, new Rank(subclasscd, score, avg, gradeRank, courseRank, hrRank));
            }

            return studentList;
        }

        private static String sqlStudent(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     ENTGRD.ENT_DIV, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGDH.HR_CLASS_NAME1, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     CCM.COURSECODENAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.SEX, ");
            stb.append("     BASE.INOUTCD, ");
            stb.append("     BRANCH.RESIDENTCD, ");
            stb.append("     J008.ABBV2 AS RESIDENTCD_ABBV2, ");
            stb.append("     J008.NAMESPARE1 AS RESIDENTCD_NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND REGDH.GRADE = REGD.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("         AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON BRANCH.SCHOOLCD = '000000000000' ");
            stb.append("         AND BRANCH.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("         AND BRANCH.YEAR = REGD.YEAR ");
            stb.append("         AND BRANCH.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ");
            stb.append("         AND J008.NAMECD2 = BRANCH.RESIDENTCD ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else if ("2".equals(param._categoryIsClass)) {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");
            return stb.toString();
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }

        public List getSubclassList() {
            final List subclassList = new ArrayList();
            for (final Iterator it = _ranks.values().iterator(); it.hasNext();) {
                final Rank rank = (Rank) it.next();
                if (_999999.equals(rank._subclasscd) || _999999AVG.equals(rank._subclasscd)) {
                    continue;
                }
                subclassList.add(rank);
            }
            return subclassList;
        }
    }
    
    private static class Rank {
        final String _subclasscd;
        final String _score;
        final String _avg;
        final Integer _gradeRank;
        final Integer _courseRank;
        final Integer _hrRank;
        public Rank(final String subclasscd, final String score, final String avg, final Integer gradeRank, final Integer courseRank, final Integer hrRank) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _courseRank = courseRank;
            _hrRank = hrRank;
        }

        private static String sqlRank(final Param param) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append("SELECT");
            stb.append("  T1.SCHREGNO,");
            stb.append("  T1.proficiency_subclass_cd as subclasscd,");
            stb.append("  T1.SCORE,");
            stb.append("  CAST(NULL AS DOUBLE) AS AVG,");
            stb.append("  T1.SCORE_DI,");
            stb.append("  CAST(NULL AS INT) as GRADE_RANK,");
            stb.append("  CAST(NULL AS INT) as HR_RANK, ");
            stb.append("  CAST(NULL AS INT) as COURSE_RANK ");
            stb.append(" FROM proficiency_dat T1 ");
            stb.append(" WHERE SCORE_DI IS NOT NULL AND ");
            stb.append("  T1.year = '" + param._year + "' AND");
            stb.append("  T1.semester = '" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv = '" + param._proficiencyDiv + "' AND");
            stb.append("  T1.proficiencycd = '" + param._proficiencyCd + "' AND");
            stb.append(" EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT REGD ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");

            stb.append(" UNION ALL ");

            stb.append("SELECT");
            stb.append("  T1.SCHREGNO,");
            stb.append("  T1.proficiency_subclass_cd as subclasscd,");
            stb.append("  T1.SCORE,");
            stb.append("  T1.AVG,");
            stb.append("  CAST(NULL AS VARCHAR(1)) as SCORE_DI,");
            stb.append("  T1.rank as GRADE_RANK,");
            stb.append("  T7.rank as HR_RANK, ");
            stb.append("  T8.rank as COURSE_RANK ");
            stb.append(" FROM proficiency_rank_dat T1 ");
            stb.append(" LEFT JOIN proficiency_subclass_mst T3 ON T3.proficiency_subclass_cd = T1.proficiency_subclass_cd ");
            stb.append(" LEFT JOIN proficiency_rank_dat T7 ON ");
            stb.append("     T7.year = T1.year AND ");
            stb.append("     T7.semester = T1.semester AND");
            stb.append("     T7.proficiencydiv = T1.proficiencydiv AND");
            stb.append("     T7.proficiencycd = T1.proficiencycd AND");
            stb.append("     T7.proficiency_subclass_cd = T1.proficiency_subclass_cd AND");
            stb.append("     T7.schregno = T1.schregno AND");
            stb.append("     T7.rank_data_div = t1.rank_data_div AND");
            stb.append("     T7.rank_div = '" + RANK_DIV_HR + "' ");
            stb.append(" LEFT JOIN proficiency_rank_dat T8 ON ");
            stb.append("     T8.year = T1.year AND ");
            stb.append("     T8.semester = T1.semester AND");
            stb.append("     T8.proficiencydiv = T1.proficiencydiv AND");
            stb.append("     T8.proficiencycd = T1.proficiencycd AND");
            stb.append("     T8.proficiency_subclass_cd = T1.proficiency_subclass_cd AND");
            stb.append("     T8.schregno = T1.schregno AND");
            stb.append("     T8.rank_data_div = t1.rank_data_div AND");
            stb.append("     T8.rank_div = '" + RANK_DIV_COURSE + "' ");
            stb.append(" WHERE");
            stb.append("  T1.year = '" + param._year + "' AND");
            stb.append("  T1.semester = '" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv = '" + param._proficiencyDiv + "' AND");
            stb.append("  T1.proficiencycd = '" + param._proficiencyCd + "' AND");
            stb.append("  (T3.proficiency_subclass_cd IS NOT NULL OR T1.proficiency_subclass_cd IN ('" + _999999 + "')) AND");
            if ("2".equals(param._juni)) {
                stb.append("  T1.rank_data_div = '" + RANK_DATA_DIV_AVG + "' AND");
            } else {
                stb.append("  T1.rank_data_div = '" + RANK_DATA_DIV_SCORE + "' AND");
            }
            stb.append("  T1.rank_div = '" + RANK_DIV_GRADE + "' AND");
            
            stb.append(" EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT REGD ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");
            return stb.toString();
        }
    }

    private static class ProficiencyAverage {

        private static ProficiencyAverage NULL = new ProficiencyAverage(null, null, null, null, null, null, null);

        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final String _avg;
        final String _stddev;
        public ProficiencyAverage(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final String avg, final String stddev) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public String toString() {
            return "ProfAvg(subclasscd = " + _subclasscd + ", max = " + _highscore + ", min = " + _lowscore + ", count = " + _count + ", avg = " + _avg + ")";
        }

        public static ProficiencyAverage getAverageGrade(final Map avgMap, final String grade, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_GRADE, grade, "000", "00000000", subclasscd));
        }

        public static ProficiencyAverage getAverageHr(final Map avgMap, final String grade, final String hrclass, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_HR_CLASS, grade, hrclass, "00000000", subclasscd));
        }

        public static ProficiencyAverage getAverageCourse(final Map avgMap, final String grade, final String course, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_COURSE, grade, "000", course, subclasscd));
        }

        public static ProficiencyAverage getAverage(final Map avgMap, final String avgKey) {
            ProficiencyAverage average = (ProficiencyAverage) avgMap.get(avgKey);
            if (null == average) {
                return NULL;
            }
            return average;
        }

        public static String avgKey(final String avgDiv, final String grade, final String hrClass, final String course, final String subclasscd) {
            return avgDiv + "-" + grade + "-" + hrClass + "-" + course + ":" + subclasscd;
        }

        private static String bdSetScale(final String val) {
            if (!NumberUtils.isNumber(val)) {
                return val;
            }
            final BigDecimal kirisage = new BigDecimal(val).setScale(0, BigDecimal.ROUND_FLOOR);
            if (new BigDecimal(val).compareTo(kirisage) == 0) {
                return kirisage.toString();
            }
            return val;
        }

        private static Map getAverageMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            final String sql = ProficiencyAverage.sqlAverage(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String course = KnjDbUtils.getString(row, "COURSE");
                final String score = bdSetScale(KnjDbUtils.getString(row, "SCORE"));
                final String highscore = bdSetScale(KnjDbUtils.getString(row, "HIGHSCORE"));
                final String lowscore = bdSetScale(KnjDbUtils.getString(row, "LOWSCORE"));
                final String count = bdSetScale(KnjDbUtils.getString(row, "COUNT"));
                final String avg = KnjDbUtils.getString(row, "AVG");
                final String stddev = KnjDbUtils.getString(row, "STDDEV");
                final ProficiencyAverage average = new ProficiencyAverage(subclasscd, score, highscore, lowscore, count, avg, stddev);

                final String avgKey = avgKey(KnjDbUtils.getString(row, "AVG_DIV"), grade, hrClass, course, subclasscd);
                rtn.put(avgKey, average);
            }
            log.info(" average dat size = " + rtn.size());
            return rtn;
        }

        private static String sqlAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , CASE WHEN N1.NAMESPARE1 IN ('2','4') THEN '1' END AS RYOUSEI_FLG ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._year +"' ");
            stb.append("                 AND   SEMESTER = '" + param._semester+ "' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT S3 ");
            stb.append("             ON S3.YEAR         = REGD.YEAR ");
            stb.append("            AND S3.GRADE        = REGD.GRADE ");
            stb.append("        LEFT JOIN SCHREG_BRANCH_DAT S4 ");
            stb.append("             ON S4.SCHOOLCD     = '000000000000' ");
            stb.append("            AND S4.SCHOOL_KIND  = S3.SCHOOL_KIND ");
            stb.append("            AND S4.YEAR         = REGD.YEAR ");
            stb.append("            AND S4.SCHREGNO     = REGD.SCHREGNO ");
            stb.append("        LEFT JOIN V_NAME_MST N1 ");
            stb.append("             ON N1.YEAR         = REGD.YEAR ");
            stb.append("            AND N1.NAMECD1      = 'J008' ");
            stb.append("            AND N1.NAMECD2      = S4.RESIDENTCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_SCORE + "' ");
            stb.append("     AND T1.RANK_DIV = '" + RANK_DIV_GRADE + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.STDDEV ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_AVERAGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
            
            // 学年
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_GRADE +  "' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");
            // 年組
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_HR_CLASS +  "' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            // 寮
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_RYO +  "' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" WHERE ");
            stb.append("     RYOUSEI_FLG = '1' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");

            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _proficiencyDiv;
        final String _proficiencyCd;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _juni;
        final String[] _categorySelected;  //学籍番号または学年-組
        final String _ketten;
        final Map _subclassnameMap;
        
        private String _testitemname;
        private String _semestername;
        private String _gradename;

        final int _akaten = 30;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _juni = request.getParameter("JUNI");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _subclassnameMap = getSubclassnameMap(db2);

            _ketten = request.getParameter("KETTEN");
            setName(db2);
        }
        
        private Map getSubclassnameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     PROFICIENCY_SUBCLASS_CD, ");
            stb.append("     VALUE(SUBCLASS_ABBV, SUBCLASS_NAME) AS SUBCLASS_NAME ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_SUBCLASS_MST ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "PROFICIENCY_SUBCLASS_CD", "SUBCLASS_NAME");
        }

        private void setName(final DB2UDB db2) {
            final String proficiencySql = "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencyDiv + "' AND PROFICIENCYCD = '" + _proficiencyCd + "'";
            _testitemname = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, proficiencySql)));

            final String sqlSemester = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            _semestername = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlSemester)));

            final String sqlGrade = " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _grade + "' ";
            _gradename = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlGrade)));
        }
    }
}

// eof

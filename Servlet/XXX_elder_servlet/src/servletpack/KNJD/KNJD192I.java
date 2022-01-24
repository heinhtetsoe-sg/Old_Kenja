// kanji=漢字
/*
 * $Id: b9d12d253f98312ea76bc17cdb4b96dacf1fb90e $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 個人成績表印刷
 * @version $Id: b9d12d253f98312ea76bc17cdb4b96dacf1fb90e $
 */
public class KNJD192I {

    private static final Log log = LogFactory.getLog(KNJD192I.class);

    private Param _param;
    private boolean _hasData = false;

    private static final String SEMEALL = "9";

    private static final String _333333 = "333333";
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";
    private static final String _999999AVG = "999999AVG";

//    private static final String HYOTEI_TESTCD = "9990009";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.fatal("$Revision: 68864 $ $Date: 2019-07-22 17:56:41 +0900 (月, 22 7 2019) $"); // CVSキーワードの取り扱いに注意
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
            if (null == current || current.size() >= max || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

    private static boolean isSubclassAll(final String subclassCd) {
        return _333333.equals(subclassCd) || _555555.equals(subclassCd) || _999999.equals(subclassCd) || "99999A".equals(subclassCd) || "99999B".equals(subclassCd) || "999999AVG".equals(subclassCd);
    }

    private static String getSubclasscd(final Map row, final Param param) throws SQLException {
        final String subclasscd;
        if (isSubclassAll(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
            subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
        } else {
            subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
        }
        return subclasscd;
    }

    private static String toString(final String v, final int scale) {
        return !NumberUtils.isNumber(v) ? null : new BigDecimal(v).setScale(scale).toString();
    }
    
    private static String sishagonyu(final String v) {
        return !NumberUtils.isNumber(v) ? null : new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    /**
     * @param db2
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {

        final Map avgMap = AverageDat.getAverageMap(db2, _param);
        final List dataListAll = Student.getStudentList(db2, _param);
        Subclass.setChairStd(db2, _param, avgMap, dataListAll);
        Subclass.setRank(db2, _param, dataListAll);
    
        log.debug(" studentList size = " + dataListAll.size());
        
        final String form = "KNJD192I.frm";
        final int maxCol = 2;
        final int maxLine = 4;
        final List pageList = getPageList(dataListAll, maxLine * maxCol);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);

            if (_param._courseList.size() > 1) {
            	for (int line = 1; line <= maxLine; line++) {
            		for (int col = 1; col <= maxCol; col++) {
                        svf.VrsOutn("CRS1_" + col + "_1", line, "文系");
                        svf.VrsOutn("CRS2_" + col + "_1", line, "理系");
            		}
            	}
            }

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
        final String title = _param._nendo + "　" + StringUtils.defaultString(student._gradeName1) + " " + StringUtils.defaultString(_param._semesterName) + StringUtils.defaultString(_param._testName);
        svf.VrsOutn("TITLE" + col, line, title); // タイトル

        svf.VrsOutn("HR_NAME" + col, line, student._hrClassName1); // 
        svf.VrsOutn("HR_NAME" + col + "_1", line, student._hrClassName1); // 
        svf.VrsOutn("HR_NAME" + col + "_2", line, student._hrClassName1); // 
        svf.VrsOutn("NO" + col, line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)); // 出席番号
        
        final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
        svf.VrsOutn("NAME" + col + "_" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), line, student._name);
        
        int akaten = 0;
        for (int subi = 0; subi < Math.min(19, student._subclassList.size()); subi++) {
            final String ssubi = String.valueOf(subi + 1);
            final Subclass subclass = (Subclass) student._subclassList.get(subi);

            svf.VrsOutn("SUBCLASS_NAME" + col + "_" + ssubi, line, subclass._subclassname); // 科目名
            final Subclass subclassRank = (Subclass) student._rankMap.get(subclass._subclasscd);
            if (null != subclassRank) {
                svf.VrsOutn("SCORE" + col + "_" + ssubi, line, subclassRank._score); // 得点
                if (NumberUtils.isDigits(subclassRank._score) && subclassRank.isKetten(Integer.parseInt(subclassRank._score), _param)) {
                    akaten += 1;
                }
            }
            svf.VrsOutn("GRADE" + col + "_" + ssubi, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, subclass._subclasscd))._avg)); // 学年
            svf.VrsOutn("HR" + col + "_" + ssubi, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("2", student, subclass._subclasscd))._avg)); // クラス
            if (_param._courseList.size() > 1) {
            	for (int cci = 0; cci < _param._courseList.size(); cci++) {
            		final String course = (String) _param._courseList.get(cci);
            		svf.VrsOutn("COURSE" + col + "_" + ssubi + "_" + (cci + 1), line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("3", student._grade, "000", course, subclass._subclasscd))._avg)); // コース
            	}
            }
            svf.VrsOutn("MAX" + col + "_" + ssubi, line, toString(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, subclass._subclasscd))._highscore, 0)); // 最高
        }
        
        // 合計
        final Subclass subclassTotal = (Subclass) student._rankMap.get(_999999);
        svf.VrsOutn("TOTAL_SCORE" + col, line, null == subclassTotal ? "" : subclassTotal._score); // 合計得点
        svf.VrsOutn("TOTAL_GRADE" + col, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, _999999))._avg)); // 合計学年
        if (_param._courseList.size() > 1) {
        	for (int cci = 0; cci < _param._courseList.size(); cci++) {
        		final String course = (String) _param._courseList.get(cci);
        		svf.VrsOutn("TOTAL_COURSE" + col + "_" + (cci + 1), line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("3", student._grade, "000", course, _999999))._avg)); // 合計コース
        	}
        }
        svf.VrsOutn("TOTAL_HR" + col, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("2", student, _999999))._avg)); // 合計クラス
        svf.VrsOutn("TOTAL_MAX" + col, line, toString(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, _999999))._highscore, 0)); // 合計最高

        // 平均
        svf.VrsOutn("AVE_SCORE" + col, line, null == subclassTotal ? "" : sishagonyu(subclassTotal._avg)); // 平均得点
        svf.VrsOutn("AVE_GRADE" + col, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, _999999AVG))._avg)); // 平均学年
        if (_param._courseList.size() > 1) {
        	for (int cci = 0; cci < _param._courseList.size(); cci++) {
        		final String course = (String) _param._courseList.get(cci);
        		svf.VrsOutn("AVE_COURSE" + col + "_" + (cci + 1), line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("3", student._grade, "000", course, _999999AVG))._avg)); // 平均コース
        	}
        }
        svf.VrsOutn("AVE_HR" + col, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("2", student, _999999AVG))._avg)); // 平均クラス
        svf.VrsOutn("AVE_MAX" + col, line, sishagonyu(AverageDat.getAverageDat(avgMap, AverageDat.avgKey("2", student, _999999AVG))._highscore)); // 平均最高

        if (_param._courseList.size() > 1) {
        	svf.VrsOutn("COURSE_NAME" + col + "_1", line, student._courseCodeName); // コース名
        }

        // 順位
        if (null != subclassTotal) {
            svf.VrsOutn("RANK_HR" + col, line, subclassTotal._rankHr); // 順位クラス
            if (_param._courseList.size() > 1) {
            	svf.VrsOutn("RANK_COURSE" + col, line, subclassTotal._rankCourse); // 順位コース
            }
            svf.VrsOutn("RANK_GRADE" + col, line, subclassTotal._rankGrade); // 順位学年
        }
        if (akaten > 0) {
            svf.VrsOutn("RANK_MAX" + col, line, String.valueOf(akaten)); // 赤点
        }

        // 人数
        svf.VrsOutn("NUM_HR" + col, line, AverageDat.getAverageDat(avgMap, AverageDat.avgKey("2", student, _999999))._count); // 人数クラス
        if (_param._courseList.size() > 1) {
        	for (int cci = 0; cci < _param._courseList.size(); cci++) {
        		final String course = (String) _param._courseList.get(cci);
        		svf.VrsOutn("NUM_COURSE" + col + "_" + (cci + 1), line, AverageDat.getAverageDat(avgMap, AverageDat.avgKey("3", student._grade, "000", course, _999999))._count); // 人数コース
        	}
        }
        svf.VrsOutn("NUM_GRADE" + col, line, AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, _999999))._count); // 人数学年
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _gradeName1;
        final String _hrClass;
        final String _attendno;
        final String _hrName;
        final String _hrClassName1;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final List _subclassList = new ArrayList();
        final Map _rankMap = new HashMap();

        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String gradeName1,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrClassName1,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName
                ) {
            _schregno = schregno;
            _grade = grade;
            _gradeName1 = gradeName1;
            _hrClass = hrClass;
            _attendno = attendNo;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
        }
        
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            try {
                final String sql = sql(param);
                
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {

                    final Map row = (Map) it.next();
                    final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"),
                            KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "GRADE_NAME1"),
                            KnjDbUtils.getString(row, "HR_CLASS"),
                            KnjDbUtils.getString(row, "ATTENDNO"),
                            KnjDbUtils.getString(row, "HR_NAME"),
                            KnjDbUtils.getString(row, "HR_CLASS_NAME1"),
                            "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME")) ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME"),
                            KnjDbUtils.getString(row, "COURSECD"),
                            KnjDbUtils.getString(row, "COURSENAME"),
                            KnjDbUtils.getString(row, "MAJORCD"),
                            KnjDbUtils.getString(row, "MAJORNAME"),
                            KnjDbUtils.getString(row, "COURSECODE"),
                            KnjDbUtils.getString(row, "COURSECODENAME")
                            );

                    studentList.add(student);

                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return studentList;
        }
        
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGDG.GRADE_NAME1, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGDH.HR_CLASS_NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.REAL_NAME, ");
            stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     L1.COURSENAME, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     L1.MAJORNAME, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     L2.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("          AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND REGDH.GRADE = REGD.GRADE ");
            stb.append("          AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("          AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON REGD.YEAR = L1.YEAR ");
            stb.append("          AND REGD.COURSECD = L1.COURSECD ");
            stb.append("          AND REGD.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON REGD.YEAR = L2.YEAR ");
            stb.append("          AND REGD.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = REGD.SCHREGNO AND L4.DIV = '04' ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
                stb.append("     AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
                stb.append("     AND REGD.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            // 寮生or下宿生
            if (param._ryoOnly || param._geshukuOnly) {
                List cds = new ArrayList();
                if (param._ryoOnly) {
                    cds.add("4");
                }
                if (param._geshukuOnly) {
                    cds.add("2");
                }
                final String[] array = new String[cds.size()];
                stb.append("        AND EXISTS(SELECT 'X' FROM SCHREG_BRANCH_DAT BRANCH ");
                stb.append("            INNER JOIN NAME_MST NMJ008 ON NMJ008.NAMECD1 = 'J008' ");
                stb.append("                AND NMJ008.NAMECD2 = BRANCH.RESIDENTCD ");
                stb.append("            WHERE ");
                stb.append("              BRANCH.SCHOOLCD = '000000000000' ");
                stb.append("              AND BRANCH.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
                stb.append("              AND BRANCH.YEAR = REGD.YEAR ");
                stb.append("              AND BRANCH.SCHREGNO = REGD.SCHREGNO ");
                stb.append("              AND NMJ008.NAMESPARE1 IN " + SQLUtils.whereIn(true, (String[]) cds.toArray(array)) + " ");
                stb.append("              ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }
    }
    
    private static class AverageDat {
        
        private static AverageDat NULL = new AverageDat();
        private String _score;
        private String _highscore;
        private String _lowscore;
        private String _count;
        private String _avg;
        
        public static String avgKey(final String avgDiv, final Student student, final String subclasscd) {
            if ("1".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, "000", "00000000", subclasscd);
            } else if ("2".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, student._hrClass, "00000000", subclasscd);
            } else if ("3".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, "000", student._courseCd + student._majorCd + student._courseCode, subclasscd);
            }
            return null;
        }
        
        public static String avgKey(final String avgDiv, final String grade, final String hrClass, final String course, final String subclasscd) {
            return avgDiv + "-" + grade + "-" + hrClass + "-" + course + ":" + subclasscd;
        }

        public static AverageDat getAverageDat(final Map avgMap, final String avgKey) {
            if (null == avgMap.get(avgKey)) {
                return NULL;
            }
            return (AverageDat) avgMap.get(avgKey);
        }
        
        public static Map getAverageMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            final String sql = getAverageSql(param);
            //log.info(" average sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                
                final String keySubclasscd;
                if (null != KnjDbUtils.getString(row, "SUBCLASS_MST_SUBCLSSCD")) {
                    keySubclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
                } else {
                    keySubclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                final String avgKey = avgKey(KnjDbUtils.getString(row, "AVG_DIV"), KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "COURSE"), keySubclasscd);
                
                AverageDat avgDat = new AverageDat();
                avgDat._score = KnjDbUtils.getString(row, "SCORE");
                avgDat._highscore = KnjDbUtils.getString(row, "HIGHSCORE");
                avgDat._lowscore = KnjDbUtils.getString(row, "LOWSCORE");
                avgDat._count = KnjDbUtils.getString(row, "COUNT");
                avgDat._avg = KnjDbUtils.getString(row, "AVG");
                
                map.put(avgKey, avgDat);
            }
            return map;
        }
        
        private static String getAverageSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._year +"' ");
            stb.append("                   AND SEMESTER = '" + param._schregSemester +"' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T1.SUBCLASSCD = '" + _999999 + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, T1.GRADE, T1.HR_CLASS, T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            // 学年
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '1' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");
            // 年組
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '2' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            // コース
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '3' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, T1.COURSE AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.COURSE ");

            return stb.toString();
        }
    }

    private static class Subclass {
        String _subclasscd;
        String _subclassname;
        String _combinedSubclasscd;
        String _score;
        String _avg;
        String _rankGrade;
        String _rankHr;
        String _rankCourse;

        private int getFailValue(final Param param) {
            if (NumberUtils.isNumber(param._ketten)) {
                return Integer.parseInt(param._ketten);
            }
            return -1;
        }
        
        private boolean isKetten(int score, final Param param) {
            return score < getFailValue(param);
        }

        private static void setChairStd(final DB2UDB db2, final Param param, final Map avgMap, final List studentList) throws SQLException {
            PreparedStatement ps;
            String sql = getChairStdSql(param);
            
            ps = db2.prepareStatement(sql);
            
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator stit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); stit.hasNext();) {
                    final Map row = (Map) stit.next();
                    final String subclasscd = getSubclasscd(row, param);
                    
//                    if (param._printTestOnly && AverageDat.NULL == AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, subclasscd))) {
//                        continue;
//                    }
                    if (AverageDat.NULL == AverageDat.getAverageDat(avgMap, AverageDat.avgKey("1", student, subclasscd))) {
                        continue;
                    }
                    
                    final Subclass subclass = new Subclass();
                    subclass._subclasscd = subclasscd;
                    subclass._subclassname = KnjDbUtils.getString(row, "SUBCLASSABBV");
                    subclass._combinedSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                    student._subclassList.add(subclass);
                }
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getChairStdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH COMBINED_SUBCLASS AS (");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.COMBINED_CLASSCD, ");
            stb.append("     T1.COMBINED_SCHOOL_KIND, ");
            stb.append("     T1.COMBINED_CURRICULUM_CD, ");
            stb.append("     T1.COMBINED_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append(" )");

            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.COMBINED_SUBCLASSCD, ");
            stb.append("     VALUE(L1.SUBCLASSABBV, L1.SUBCLASSNAME) AS SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("     LEFT JOIN COMBINED_SUBCLASS L2 ON L2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T1.CLASSCD = L2.COMBINED_CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T2 ON ");
            stb.append("         T2.YEAR = T1.YEAR AND ");
            stb.append("         T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("         T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }

        private static void setRank(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            
            final String sql = getRecordRankSql(param._year, param._semester, param._testcd);
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    for (final Iterator rowit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rowit.hasNext();) {
                        final Map row = (Map) rowit.next();
                        final Subclass Subclass = new Subclass();
                        Subclass._subclasscd = getSubclasscd(row, param);
                        Subclass._score = KnjDbUtils.getString(row, "SCORE");
                        Subclass._avg = KnjDbUtils.getString(row, "AVG");
                        Subclass._rankGrade = KnjDbUtils.getString(row, "GRADE_" + param.getRankAvgField() + "RANK");
                        Subclass._rankHr = KnjDbUtils.getString(row, "CLASS_" + param.getRankAvgField() + "RANK");
                        Subclass._rankCourse = KnjDbUtils.getString(row, "COURSE_" + param.getRankAvgField() + "RANK");
                        student._rankMap.put(Subclass._subclasscd, Subclass);
                    }
                }
                
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 席次データの取得
         * @return sql
         */
        public static String getRecordRankSql(
                final String year,
                final String semester,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _testcd;
        final String _testName;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _outputKijun;
        final String[] _categorySelected;  //学籍番号または学年-組
        final String _nendo;
        final String _z010;
        final String _ketten;
        final String _schregSemester;
        private String _semesterName;
        final List _courseList;

        final boolean _ryoOnly;
        final boolean _geshukuOnly;
        

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _semester = request.getParameter("SEMESTER");
             _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";

            _z010 = setNameMst(db2, "Z010", "00");

            _ketten = request.getParameter("KETTEN");

            _ryoOnly = "1".equals(request.getParameter("RYO_ONLY"));
            _geshukuOnly = "1".equals(request.getParameter("GESHUKU_ONLY"));
            _courseList = getCourseList(db2);
        }
        
        private List getCourseList(final DB2UDB db2) {
            final String sql = "SELECT DISTINCT COURSECD || MAJORCD || COURSECODE AS COURSE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY COURSECD || MAJORCD ||  COURSECODE ";
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "COURSE");
        }
        
        private String getTestName(final DB2UDB db2) {
            String rtn = "";
            final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "'" +
                    " AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) {
            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ORDER BY SEMESTER ";
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            final Map lastRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, getNameMst(_year, namecd1, namecd2)));
            return StringUtils.defaultString(KnjDbUtils.getString(lastRow, "NAME1"), "");
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

        private String getRankAvgField() {
            return "2".equals(_outputKijun) ? "AVG_" : "";
        }
    }
}

// eof

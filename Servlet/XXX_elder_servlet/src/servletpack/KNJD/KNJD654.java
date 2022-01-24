// kanji=漢字
/*
 * $Id: e34ff86ffeb4208d8797231f9b7266dabcd3198b $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 得点分布表
 * 
 * @author nakamoto
 * @version $Id: e34ff86ffeb4208d8797231f9b7266dabcd3198b $
 */
public class KNJD654 {
    private static final Log log = LogFactory.getLog(KNJD654.class);

    private final String FORM_FILE = "KNJD654.frm";

    private final String FORM_FILE2 = "KNJD654_2.frm";

    private final String FORM_FILE3 = "KNJD654_3.frm";

    private final String SEME_ALL = "9";

    private final String AVG_DIV_GRADE = "1";

    private final String AVG_DIV_HR_CLASS = "2";

    private final String AVERAGE_DAT_GRADE_HR_CLASSCD = "000";

    private final String SUBCLASSCD_ALL3 = "333333";

    private final String SUBCLASSCD_ALL5 = "555555";

    private final String SUBCLASSCD_ALL9 = "999999";

    private Param _param;

    /**
     * KNJD.classから最初に起動されます。
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alp svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);

            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            _param = createParam(request);

            svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            sd.setSvfInit(request, response, svf);
            if ("1".equals(_param._form)) {
                hasData = svfPrintMain2(svf, db2, _param._grade);
            } else {
                hasData = svfPrintMain(svf, db2, _param._grade);
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
        log.debug(" hasData = " + hasData);
    }

    private boolean svfPrintMain(final Vrw32alp svf, final DB2UDB db2, final String grade) {
        boolean hasData = false;
        _param.load(db2, grade);

        svf.VrSetForm(FORM_FILE, 1);
        
        final List printHomerooms = getPrintHomerooms(db2, grade);
        final List avgSubclasses = getSubclasses(db2, grade, true); // 平均点表示用科目
        final List distSubclasses = getSubclasses(db2, grade, false); // 分布表示用科目
        final Map hrAverage = loadHrAverage(db2, grade, avgSubclasses);
        final Map subclassDistribution = loadSubclassDistribution(db2, grade, distSubclasses);
        
        if (!hrAverage.isEmpty() || !subclassDistribution.isEmpty()) {

            printHeader(svf, "GRADE");
            // log.debug("== クラス別平均点一覧表 ==");
            hasData = printHrAverage(svf, printHomerooms, null, avgSubclasses, hrAverage);
            // log.debug("== 科目別得点分布表 ==");
            final Map pageSubclassListMap = splitSubclass(distSubclasses, _param._schoolKind);
            for (final Iterator it = pageSubclassListMap.keySet().iterator(); it.hasNext();) {
                final Integer page = (Integer) it.next();
                if ("J".equals(_param._schoolKind) && 1 != page.intValue()) {
                    break;
                }
                if (1 != page.intValue()) {
                    svf.VrSetForm(FORM_FILE3, 1);
                    printHeader(svf, "GRADE");
                }
                final List subclassList = (List) pageSubclassListMap.get(page);
                hasData = printSubclassDistribution(svf, printHomerooms, subclassList, subclassDistribution, _param._schoolKind) || hasData;
                if (hasData) {
                    svf.VrEndPage();
                }
            }
        }
        return hasData;
    }

    private void printHeader(final Vrw32alp svf, final String gradeNameField) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut(gradeNameField, _param._gradeName);
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("PERIOD", _param._testItemName);
        svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
    }
    
    private Map splitSubclass(final List subclasses, final String schoolKind) {
        int count = 0;
        Integer page = new Integer(1);
        final TreeMap pageSubclassListMap = new TreeMap();
        if ("J".equals(schoolKind)) {
            final int max = 6;
            SubClass sub333 = null;
            SubClass sub555 = null;
            SubClass sub999 = null;
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (SUBCLASSCD_ALL3.equals(subClass._code)) {
                    sub333 = subClass;
                } else if (SUBCLASSCD_ALL5.equals(subClass._code)) {
                    sub555 = subClass;
                } else if (SUBCLASSCD_ALL9.equals(subClass._code)) {
                    sub999 = subClass;
                } else {
                    count += 1;
                    if (count > max) {
                        page = new Integer(page.intValue() + 1);
                        count -= max;
                    }
                    if (null == pageSubclassListMap.get(page)) {
                        pageSubclassListMap.put(page, new ArrayList());
                    }
                    final List subclassList = (List) pageSubclassListMap.get(page);
                    subclassList.add(subClass);
                }
            }
            final SubClass[] arr = new SubClass[]{sub333, sub555, sub999};
            for (int i = 0; i < arr.length; i++) {
                if (null == arr[i]) {
                    continue;
                }
                for (final Iterator it = pageSubclassListMap.values().iterator(); it.hasNext();) {
                    final List subclassList = (List) it.next();
                    subclassList.add(arr[i]);
                }
            }
        } else {
            int max = 8;
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                count += 1;
                if (count > max) {
                    page = new Integer(page.intValue() + 1);
                    count -= max;
                    max = 12;
                }
                if (null == pageSubclassListMap.get(page)) {
                    pageSubclassListMap.put(page, new ArrayList());
                }
                final List subclassList = (List) pageSubclassListMap.get(page);
                subclassList.add(subClass);
            }
        }
        log.debug(" page map = " + pageSubclassListMap);
        return pageSubclassListMap;
    }

    private boolean svfPrintMain2(final Vrw32alp svf, final DB2UDB db2, final String paramGrade) {
        boolean hasData = false;
        
        final String schoolKind = "99".equals(paramGrade) ? "H" : "98".equals(paramGrade) ? "J" : null;
        final List gradeList = null == schoolKind ? Collections.singletonList(paramGrade) : _param.getGdatGradeList(db2, schoolKind);
        
        final int GRADE_MAX_COUNT = 3;
        int gradeCount = 0;
        
        svf.VrSetForm(FORM_FILE2, 1);
        
        for (final Iterator it = gradeList.iterator(); it.hasNext();) {
            final String grade = (String) it.next();
            _param.load(db2, grade);
            
            final List printHomerooms = getPrintHomerooms(db2, grade);
            final List avgSubclasses = getSubclasses(db2, grade, true); // 平均点表示用科目
            final Map hrAverage = loadHrAverage(db2, grade, avgSubclasses);
            
            if (!hrAverage.isEmpty()) {
                printHeader(svf, "GRADE" + String.valueOf(gradeCount + 1));
                // log.debug("== クラス別平均点一覧表 ==");
                hasData = printHrAverage(svf, printHomerooms, String.valueOf(gradeCount + 1), avgSubclasses, hrAverage);
                gradeCount += 1;
                if (0 != gradeCount && gradeCount % GRADE_MAX_COUNT == 0) {
                    svf.VrEndPage();
                    gradeCount = 0;
                }
            }
        }
        if (0 != gradeCount) {
            svf.VrEndPage();
        }
        return hasData;
    }

    private List getSubclasses(final DB2UDB db2, final String grade, final boolean isAvg) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtn = new ArrayList();
        try {
            final String sql = sqlSubclassList(grade, isAvg);
            // log.debug(" sqlSubclassList (isAvg = " + isAvg + " ) = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subClassCd = rs.getString("SUBCLASSCD");
                final String abbv = rs.getString("SUBCLASSABBV");
                final String name = rs.getString("SUBCLASSNAME");
                rtn.add(new SubClass(subClassCd, abbv, name));
            }
            if (rtn.size() != 0) {
                rtn.add(new SubClass(SUBCLASSCD_ALL3, "３教科平均", "３教科平均"));
                rtn.add(new SubClass(SUBCLASSCD_ALL5, "５教科平均", "５教科平均"));
                if (isAvg) {
                    rtn.add(new SubClass(SUBCLASSCD_ALL9, "全教科平均", "全教科平均"));
                }
            }
            // log.debug(" subclasses (isAvg = " + isAvg + ")" + rtn);
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
//        for (Iterator it = rtn.iterator(); it.hasNext();) {
//            SubClass s = (SubClass) it.next();
//            log.debug(" subClass = " + s);
//        }
        return rtn;
    }

    private SubClass getSubClass(final String subClassCd, final List subClasses) {
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            if (subClassCd != null && subClassCd.equals(subClass._code)) {
                return subClass;
            }
        }
        return null;
    }

    private String sqlSubclassList(final String grade, final boolean isAvg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECORD AS (");
        stb.append(sqlSchregRecordRankDat(grade, "WITH"));
        if (!isAvg) {
            stb.append(" ) , COURSE AS ( ");
            stb.append("   SELECT DISTINCT T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECD ");
            stb.append("   FROM RECORD T1 ");
        } 
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT VALUE(T4.SHOWORDER4, 999), T2.SUBCLASSNAME, T2.SUBCLASSABBV ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      , T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("      , T1.SUBCLASSCD ");
        }
        stb.append(" FROM RECORD T1 ");
        stb.append(" INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T1.CLASSCD = T2.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
        }
        if (!isAvg) {
            stb.append(" INNER JOIN REC_SUBCLASS_GROUP_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T3.COURSECD || T3.MAJORCD || T3.COURSECODE IN (SELECT COURSECD FROM COURSE) ");
        }
        stb.append(" ORDER BY VALUE(T4.SHOWORDER4, 999) ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      , T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("      , T1.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private String sqlSchregRecordRankDat(final String grade, final String sqlDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, T1.SEMESTER, ");
        stb.append("     T2.GRADE, T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE, ");
        stb.append("     T1.SCHREGNO, T1.SCORE, T1.AVG ");
        if ("1".equals(_param._useCurriculumcd) && "SELECT".equals(sqlDiv)) {
            stb.append("     , CASE WHEN T1.SUBCLASSCD IN ('333333', '555555', '999999') THEN T1.SUBCLASSCD ELSE T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD END AS SUBCLASSCD ");
        } else if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("     , T1.SUBCLASSCD ");
        } else {
            stb.append("     , T1.SUBCLASSCD ");
        }
        stb.append(" FROM RECORD_RANK_DAT T1");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        if (SEME_ALL.equals(_param._semester)) {
            stb.append("    AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "' ");
        stb.append("    AND T2.GRADE= '" + grade + "' ");
        stb.append(" ORDER BY T2.GRADE, T2.HR_CLASS ");
        return stb.toString();
    }

    private List getPrintHomerooms(final DB2UDB db2, final String grade) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtn = new ArrayList();
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT T1.HR_CLASS, T1.HR_NAME ");
            sql.append(" FROM SCHREG_REGD_HDAT T1");
            sql.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
            sql.append("     AND T2.GRADE = T1.GRADE ");
            sql.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
            sql.append(" WHERE T1.YEAR = '" + _param._ctrlYear + "' ");
            if (SEME_ALL.equals(_param._semester)) {
                sql.append("    AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
                sql.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            }
            sql.append("     AND T1.GRADE = '" + grade + "' ");
            sql.append(" ORDER BY T1.HR_CLASS ");
            // log.debug(" printhomerooms sql = " + sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hrClassCd = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                rtn.add(new PrintHomeroom(AVG_DIV_HR_CLASS, hrClassCd, hrName));
            }
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        rtn.add(new PrintHomeroom(AVG_DIV_GRADE, AVERAGE_DAT_GRADE_HR_CLASSCD, "全体"));
        return rtn;
    }

    private String getAverageKey(String avgDiv, String hrClass, String subClassCd) {
        return avgDiv + "-" + hrClass + "-" + subClassCd;
    }

    /**
     * クラス別平均点一覧表を印字する
     * 
     * @param svf
     * @param printHomerooms 印字するクラス・学年
     * @param i フィールドのインデクス
     * @param subClasses 印字する科目
     * @param hrAverage 平均点データ
     */
    private boolean printHrAverage(final Vrw32alp svf, final List printHomerooms, final String i, final List subClasses, final Map hrAverage) {
        int hrc = 0;
        boolean hasData = false;
        final String i0 = null == i ? "1" : i;
        final String i1 = null == i ? "" : "S_" + i;
        final String i1_2 = null == i ? "" : "_" + i;
        final String i2 = null == i ? "" : i;
        final int maxLine = null == i ? 7 : 10;
        final int maxColumn = null == i ? 19 : 27;
        // クラス別平均点
        for (final Iterator it = printHomerooms.iterator(); it.hasNext();) {
            final PrintHomeroom phr = (PrintHomeroom) it.next();
            hrc += 1;
            final int c = AVERAGE_DAT_GRADE_HR_CLASSCD.equals(phr._code) ? maxLine : hrc;
            svf.VrsOutn("HR_CLASS" + i2, c, phr._name);
            int subc = 0;
            for (final Iterator itsub = subClasses.iterator(); itsub.hasNext();) {
                final SubClass subClass = (SubClass) itsub.next();
                subc += 1;
                if (!SUBCLASSCD_ALL3.equals(subClass._code) && !SUBCLASSCD_ALL5.equals(subClass._code) && !SUBCLASSCD_ALL9.equals(subClass._code)) {
                    svf.VrsOut("SUBCLASS" + i0 + "_" + String.valueOf(subc), subClass._abbv);
                }
                final String avg = (String) hrAverage.get(getAverageKey(phr._avgDiv, phr._code, subClass._code));
                final String field;
                if (SUBCLASSCD_ALL3.equals(subClass._code)) {
                    field = "AVE_SUBCLASS3" + i1;
                } else if (SUBCLASSCD_ALL5.equals(subClass._code)) {
                    field = "AVE_SUBCLASS5" + i1;
                } else if (SUBCLASSCD_ALL9.equals(subClass._code)) {
                    field = "AVE_SUBCLASSALL" + i1_2;
                } else if (subc >= maxColumn) {
                    field = null;
                } else {
                    field = "AVE_SUBCLASS" + i0 + "_" + String.valueOf(subc);
                }
                if (avg == null || field == null) {
                    continue;
                }
                svf.VrsOutn(field, c, avg);
                hasData = true;
            }
        }
        return hasData;
    }

    /**
     * 科目別得点分布表を印字する
     * 
     * @param svf
     * @param printHomerooms 印字するクラス・学年
     * @param subclasses 印字する科目
     * @param subclassDistribution 得点分布データ
     */
    private boolean printSubclassDistribution(final Vrw32alp svf, final List printHomerooms, final List subclasses, final Map subclassDistribution, final String schoolKind) {
        // クラス別平均点
        final int maxHrColumn = 7;
        int si = 0;
        boolean hasData = false;
        for (final Iterator itsub = subclasses.iterator(); itsub.hasNext();) {
            final SubClass subclass = (SubClass) itsub.next();
            final String sc;
            if (SUBCLASSCD_ALL9.equals(subclass._code)) {
                sc = null;
            } else if ("J".equals(schoolKind)) {
                if (SUBCLASSCD_ALL3.equals(subclass._code)) {
                    sc = "11";
                } else if (SUBCLASSCD_ALL5.equals(subclass._code)) {
                    sc = "12";
                } else {
                    si += 1;
                    sc = String.valueOf(si);
                }
            } else {
                si += 1;
                sc = String.valueOf(new String[]{"1", "2", "3", "11", "4", "5", "6", "12", "7", "8", "9", "13"}[si - 1]);
            }
            final String abbvField = "11".equals(sc) ? "AVERAGE_NAME1" : "12".equals(sc) ? "AVERAGE_NAME2" : "13".equals(sc) ? "AVERAGE_NAME3" : "SUBCLASS2_" + sc;
            svf.VrsOut(abbvField, subclass._abbv);
            int hi = 0;
            for (final Iterator it = printHomerooms.iterator(); it.hasNext();) {
                final PrintHomeroom phr = (PrintHomeroom) it.next();
                hi += 1;
                final String his = AVERAGE_DAT_GRADE_HR_CLASSCD.equals(phr._code) ? String.valueOf(maxHrColumn) : String.valueOf(hi);
                svf.VrsOut("HR_NAME" + sc + "_" + his, phr._name);
                final ScoreDistribution dist = (ScoreDistribution) subclassDistribution.get(phr._code);
                for (int i = 0; i < ScoreDistribution._scoreKeys.length; i++) {
                    final String count = (dist == null) ? "" : dist.getCount(subclass._code, ScoreDistribution._scoreKeys[i]).toString();
                    svf.VrsOutn("SUM" + sc + "_" + his, (11 - i), count);
                    hasData = true;
                }
            }
        }
        return hasData;
    }

    private Map loadHrAverage(final DB2UDB db2, final String grade, final List subClasses) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map rtn = new HashMap();
        try {
            final String sql = sqlHrAverage(grade);
            // log.debug(" sqlHrAverage = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String avgDiv = rs.getString("AVG_DIV");
                final String hrClass = rs.getString("HR_CLASS");
                final String subClassCd = rs.getString("SUBCLASSCD");
                if (getSubClass(subClassCd, subClasses) == null) {
                    continue;
                }
                final String avg = rs.getString("AVG") == null ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                rtn.put(getAverageKey(avgDiv, hrClass, subClassCd), avg);
            }
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private String sqlHrAverage(final String grade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD END AS SUBCLASSCD, ");
        } else {
            stb.append("   SUBCLASSCD, ");
        }
        stb.append("   AVG_DIV, GRADE, HR_CLASS, SCORE, AVG ");
        stb.append(" FROM ");
        stb.append("   RECORD_AVERAGE_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._ctrlYear + "' ");
        stb.append("   AND SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND GRADE = '" + grade + "' ");
        stb.append("   AND TESTKINDCD || TESTITEMCD = '" + _param._testKindCd + "' ");
        stb.append("   AND AVG_DIV IN ('1', '2') ");
        return stb.toString();
    }

    private Map loadSubclassDistribution(final DB2UDB db2, final String grade, final List subClasses) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map rtn = new HashMap();
        rtn.put(AVERAGE_DAT_GRADE_HR_CLASSCD, new ScoreDistribution(AVERAGE_DAT_GRADE_HR_CLASSCD));
        try {
            final String sql = sqlSchregRecordRankDat(grade, "SELECT");
            // log.debug(" dist sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hrClass = rs.getString("HR_CLASS");
                final String subClassCd = rs.getString("SUBCLASSCD");
                final Integer score;
                if (SUBCLASSCD_ALL3.equals(subClassCd) || SUBCLASSCD_ALL5.equals(subClassCd)) {
                    score = rs.getString("AVG") == null ? null : new Integer(Double.valueOf(rs.getString("AVG")).intValue());
                } else {
                    score = rs.getString("SCORE") == null ? null : Integer.valueOf(rs.getString("SCORE"));
                }
                final SubClass subClass = getSubClass(subClassCd, subClasses);
                if (subClass == null || score == null) {
                    continue;
                }
                if (!rtn.containsKey(hrClass)) {
                    rtn.put(hrClass, new ScoreDistribution(hrClass));
                }
                ScoreDistribution dist;
                dist = (ScoreDistribution) rtn.get(hrClass);
                dist.add(subClass, score);
                dist = (ScoreDistribution) rtn.get(AVERAGE_DAT_GRADE_HR_CLASSCD);
                dist.add(subClass, score);
            }
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    /**
     * 印字するクラス・学年
     */
    private class PrintHomeroom {
        /** 平均値区分 学年=1 クラス=2 */
        final String _avgDiv;
        final String _code;
        final String _name;
        PrintHomeroom(String avgDiv, String code, String name) {
            _avgDiv = avgDiv;
            _code = code;
            _name = name;
        }
    }

    /**
     * 科目
     */
    private class SubClass {
        private final String _code;
        private final String _abbv;
        private final String _name;
        SubClass(final String code, final String abbv, final String name) {
            _code = code;
            _abbv = abbv;
            _name = name;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass))
                return false;
            final SubClass that = (SubClass) obj;
            return _code.equals(that._code);
        }

        public int hashCode() {
            return _code.hashCode();
        }

        public String toString() {
            return "[" + _code + " , " + _abbv + "]";
        }
    }

    /**
     * 科目ごとの得点分布
     */
    private static class ScoreDistribution {
        public final static String[] _scoreKeys = new String[] {
                "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"
        };
        public final Set _keySubClasses = new HashSet();
        private final String _key;
        private final Map _distributions = new HashMap();

        private ScoreDistribution(String key) {
            _key = key;
        }

        private Map getSubclassDistributionMap(SubClass subClass) {
            return getSubclassDistributionMap(subClass._code);
        }

        private Map getSubclassDistributionMap(String subClassCd) {
            if (!_distributions.containsKey(subClassCd)) {
                _distributions.put(subClassCd, new HashMap());
            }
            return (Map) _distributions.get(subClassCd);
        }

        public void add(SubClass subClass, Integer score) {
            int scoreKeyInd = (score.intValue() / 10);
            if (scoreKeyInd <= _scoreKeys.length) {
                _keySubClasses.add(subClass);
                increment(subClass, _scoreKeys[scoreKeyInd]);
            }
        }

        private void increment(SubClass subClass, String scoreKey) {
            Integer count = getCount(subClass._code, scoreKey);
            getSubclassDistributionMap(subClass).put(scoreKey, new Integer(count.intValue() + 1));
        }

        public Integer getCount(String subClassCd, String scoreKey) {
            Map subclassScoreDist = getSubclassDistributionMap(subClassCd);
            final Integer count;
            if (subclassScoreDist.containsKey(scoreKey)) {
                count = (Integer) subclassScoreDist.get(scoreKey);
            } else {
                count = Integer.valueOf("0");
            }
            return count;
        }

        private String distStr() {
            StringBuffer stb = new StringBuffer();
            String comma = "";
            for (Iterator it = _keySubClasses.iterator(); it.hasNext();) {
                SubClass subClass = (SubClass) it.next();
                stb.append("[subClass=").append(subClass.toString());
                for (int i = 0; i < _scoreKeys.length; i++) {
                    String scoreKey = _scoreKeys[i];
                    Integer count = getCount(subClass._code, scoreKey);
                    stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
                    comma = ", ";
                }
                stb.append("] ");
            }
            return stb.toString();
        }

        public String toString() {
            return " dist = (" + distStr() + ")";
        }
    }

    Param createParam(HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private class Param {

        /** 年度 */
        final String _ctrlYear;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期） */
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _testKindCd;
        final String _grade;
        final String _form;
        final Map _homeRoomNames = new HashMap();
        private String _semesterName;
        private String _testItemName;
        private String _gradeName;
        private String _schoolKind;
        private String _hrstaffname;
        private static final String FROM_TO_MARK = "\uFF5E";
        private final String _useCurriculumcd;

        Param(final HttpServletRequest request) throws ServletException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _testKindCd = request.getParameter("TESTKINDCD"); // テスト・成績種別
            _grade = request.getParameter("GRADE");
            _form = request.getParameter("FORM"); // 「平均点一覧のみ」チェックボックス  {GRADE, SCHOOL_KIND} => {98, 'J'}, {99, 'H'}
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        public void load(final DB2UDB db2, final String grade) {
            setTestName(db2);
            // setHrStaff(db2);
            setGradeName(db2, grade);
            setHrclassName(db2, grade);
            setSemesterName(db2);
        }

        private void setGradeName(final DB2UDB db2, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SCHOOL_KIND, GRADE_NAME1 FROM SCHREG_REGD_GDAT " 
                        + "WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + grade  +"' ";
                // log.debug(" gradeName sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                _gradeName = "";
                _schoolKind = "";
                if (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1");
                    _schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setTestName(final DB2UDB db2) {
            _testItemName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME " + "FROM TESTITEM_MST_COUNTFLG_NEW " + "WHERE YEAR = '" + _ctrlYear + "' " + "AND SEMESTER = '" + _semester + "' "
                        + "AND TESTKINDCD || TESTITEMCD = '" + _testKindCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _testItemName = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setHrclassName(final DB2UDB db2, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _homeRoomNames.clear();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.GRADE || T1.HR_CLASS AS CD, T1.HR_NAME AS NAME ");
                sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                sql.append(" WHERE T1.YEAR = '" + _param._ctrlYear + "' AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("CD");
                    final String name = rs.getString("NAME");
                    _homeRoomNames.put(cd, name);
                }
            } catch (SQLException e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public List getGdatGradeList(final DB2UDB db2, final String schoolKind) {
            final List gradeList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    T1.GRADE ");
                sql.append(" FROM ");
                sql.append("    SCHREG_REGD_GDAT T1 ");
                sql.append(" WHERE ");
                sql.append("    T1.YEAR = '" + _param._ctrlYear + "' ");
                sql.append("    AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
                sql.append(" ORDER BY ");
                sql.append("    T1.GRADE ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    gradeList.add(grade);
                }
            } catch (SQLException e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeList;
        }
    }
}

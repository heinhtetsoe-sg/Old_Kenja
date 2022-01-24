// kanji=漢字
/*
 * $Id: f021e87174388866bd59a4616be0832998cfc242 $
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 得点分布表
 * 
 * @author nakamoto
 * @version $Id: f021e87174388866bd59a4616be0832998cfc242 $
 */
public class KNJD654A {
    private static final Log log = LogFactory.getLog(KNJD654A.class);

    private static final String SEME_ALL = "9";

    private static final String AVG_DIV_GRADE = "1";
    private static final String AVG_DIV_HR_CLASS = "2";
    private static final String AVG_DIV_COURSE = "3";
    private static final String AVERAGE_DAT_GRADE_CODE = "00000000000";

    private static final String SUBCLASSCD_ALL3 = "333333";
    private static final String SUBCLASSCD_ALL5 = "555555";
    private static final String SUBCLASSCD_ALL9 = "999999";
    
    private static final String TESTCD9900 = "9900";
    
    private static final String SCHOOL_KIND_J = "J";

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
//            if ("1".equals(_param._form)) {
//                hasData = svfPrintMain2(svf, db2, _param._grade);
//            } else {
                hasData = svfPrintMain(svf, db2, _param._grade);
//            }
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

        final List courseList = new ArrayList();
        if ("2".equals(_param._group)) {
            courseList.addAll(_param.getCourseList(db2, grade));
        } else {
            courseList.add(null); // dummy
        }
        
        for (final Iterator itc = courseList.iterator(); itc.hasNext();) {
            final Course course = (Course) itc.next();
            
            final List printHomerooms = PrintGroup.getPrintGroups(db2, _param, grade, course);
            final List avgSubclasses = SubClass.getSubclasses(db2, _param, grade, course, true); // 平均点表示用科目
            final List distSubclasses = SubClass.getSubclasses(db2, _param, grade, course, false); // 分布表示用科目
            final Map chairGroupSubclassCdCourseMap = AverageDat.loadChairGroupSubclassCd(db2, _param, grade, avgSubclasses);
            final Map hrAverage = AverageDat.loadHrAverage(db2, _param, grade, avgSubclasses);
            final Map subclassDistribution = ScoreDistribution.loadSubclassDistribution(db2, _param, grade, course, distSubclasses);

            if (hrAverage.isEmpty() && subclassDistribution.isEmpty()) {
                continue;
            }

            final List pageList = getPrintPageList(avgSubclasses, distSubclasses);
            
            for (int pi = 0; pi < pageList.size(); pi++) {
                
                final Page page = (Page) pageList.get(pi);
                
                svf.VrSetForm(page._form, 1);

                printHeader(svf, "GRADE", course);
                // log.debug("== クラス別平均点一覧表 ==");
                boolean hasGroupAverageData = false;
                if (null != page._averageSubclasses) {
                    hasGroupAverageData = printGroupAverage(svf, printHomerooms, page._averageSubclasses, hrAverage, chairGroupSubclassCdCourseMap);
                }
                // log.debug("== 科目別得点分布表 ==");
                boolean hasDataPage = false;
                if (null != page._subclassList) {
                    hasDataPage = printSubclassDistribution(svf, printHomerooms, page._subclassList, subclassDistribution, page._isForm1);
                }
                if (hasDataPage || hasGroupAverageData) {
                    svf.VrEndPage();
                    hasData = true;
                }
            }
        }
        return hasData;
    }

    private List getPrintPageList(final List avgSubclasses, final List distSubclasses) {
        final int avgSubclassMax;
        final String form1, form2;
        if (TESTCD9900.equals(_param._testKindCd)) {
            if ("2".equals(_param._formSelect)) {
                form1 = "KNJD654A_7.frm";  // 平均点科目列25
                avgSubclassMax = 25;
            } else {
                form1 = "KNJD654A_4.frm";
                avgSubclassMax = 18;
            }
            form2 = "KNJD654A_5.frm";
        } else {
            if ("2".equals(_param._formSelect)) {
                form1 = "KNJD654A_6.frm"; // 平均点科目列25
                avgSubclassMax = 25;
            } else {
                form1 = "KNJD654A.frm";
                avgSubclassMax = 18;
            }
            form2 = "KNJD654A_3.frm";
        }

        final List averageSubclassListList = getAverageSubclassListList(avgSubclasses, avgSubclassMax);
        final int averageSubclassPage = averageSubclassListList.size();
        final TreeMap pageSubclassListMap = splitSubclass(distSubclasses, averageSubclassPage);
        final int subclassListPage = pageSubclassListMap.isEmpty() ? 0 : ((Integer) pageSubclassListMap.lastKey()).intValue();

        final List rtn = new ArrayList();
        for (int i = 0; i < Math.max(averageSubclassPage,  subclassListPage); i++) {
            final List averageSubclassList = i >= averageSubclassListList.size() ? null : (List) averageSubclassListList.get(i);
            final List subclassList = (List) pageSubclassListMap.get(new Integer(i + 1));
            
            final Page p = new Page();
            if (null != averageSubclassList) {
                p._form = form1;
                p._isForm1 = true;
            } else {
                p._form = form2;
                p._isForm1 = false;
            }
            p._averageSubclasses = averageSubclassList;
            p._subclassList = subclassList;
            rtn.add(p);
            
            if (SCHOOL_KIND_J.equals(_param._schoolKind)) { // 中学は1ページのみ
                break;
            }
        }
        
        return rtn;
    }

    private List getAverageSubclassListList(final List avgSubclasses, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        SubClass subClass333333 = null;
        SubClass subClass555555 = null;
        SubClass subClass999999 = null;
        for (final Iterator it = avgSubclasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            
            if (SUBCLASSCD_ALL3.equals(subClass._code)) {
                subClass333333 = subClass;
                continue;
            } else if (SUBCLASSCD_ALL5.equals(subClass._code)) {
                subClass555555 = subClass;
                continue;
            } else if (SUBCLASSCD_ALL9.equals(subClass._code)) {
                subClass999999 = subClass;
                continue;
            }
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(subClass);
        }
        for (final Iterator it = rtn.iterator(); it.hasNext();) {
            final List subclassList = (List) it.next();
            if (null != subClass333333) {
                subclassList.add(subClass333333);
            }
            if (null != subClass555555) {
                subclassList.add(subClass555555);
            }
            if (null != subClass999999) {
                subclassList.add(subClass999999);
            }
        }
        return rtn;
    }
    
    private static class Page {
        String _form;
        boolean _isForm1;
        List _averageSubclasses;
        List _subclassList;
    }

    private void printHeader(final Vrw32alp svf, final String gradeNameField, final Course course) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut(gradeNameField, _param._gradeName);
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("PERIOD", _param._testItemName);
        svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("KIND1", "クラス");
        if (null != course) {
            svf.VrsOut("TYPE_GROUP_NAME", course._name);
        }
        final String valueName;
        if (TESTCD9900.equals(_param._testKindCd)) {
            if ("9".equals(_param._semester)) {
                valueName = "評定";
            } else {
                valueName = "評価";
            }
        } else {
            valueName = "範囲";
        }
        svf.VrsOut("VALUE_NAME", valueName);
    }
    
    private TreeMap splitSubclass(final List subclasses, final int avgUsePage) {
        int count = 0;
        Integer page = new Integer(1);
        final TreeMap pageSubclassListMap = new TreeMap();
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
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
            final int subclassColumn = 4;
            int subclassLine;
            if (TESTCD9900.equals(_param._testKindCd)) {
                subclassLine = 3; // フォームKNJD654A_4.frm
            } else {
                subclassLine = 2; // フォームKNJD654A.frm
            }
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                count += 1;
                if (count > subclassColumn * subclassLine) {
                    page = new Integer(page.intValue() + 1);
                    count -= subclassColumn * subclassLine;
                    if (page.intValue() > avgUsePage) {
                        if (TESTCD9900.equals(_param._testKindCd)) {
                            subclassLine = 4; // フォームKNJD654A_5.frm
                        } else {
                            subclassLine = 3; // フォームKNJD654A_3.frm
                        }
                    }
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

    /**
     * クラス別平均点一覧表を印字する
     * 
     * @param svf
     * @param printGroups 印字するクラス・学年
     * @param subClasses 印字する科目
     * @param hrAverage 平均点データ
     */
    private boolean printGroupAverage(final Vrw32alp svf, final List printGroups, final List subClasses, final Map hrAverage,
            final Map chairGroupSubclassCdCourseMap) {
        int hrc = 0;
        boolean hasData = false;
        final String i0 = "1";
        final String i1 = "";
        final String i1_2 = "";
        final String i2 = "";
        final int maxLine = 11;
        final int maxColumn = "2".equals(_param._formSelect) ? 25 : 18;
        // クラス別平均点
        for (final Iterator it = printGroups.iterator(); it.hasNext();) {
            final PrintGroup phr = (PrintGroup) it.next();
            hrc += 1;
            final int c;
            if (phr._islast) {
                c = maxLine;
            } else {
                c = hrc;
            }
            svf.VrsOutn("HR_CLASS" + i2, c, phr._name);
            int subc = 0;
            for (final Iterator itsub = subClasses.iterator(); itsub.hasNext();) {
                final SubClass subClass = (SubClass) itsub.next();
                subc += 1;
                if (!SUBCLASSCD_ALL3.equals(subClass._code) && !SUBCLASSCD_ALL5.equals(subClass._code) && !SUBCLASSCD_ALL9.equals(subClass._code)) {
                    svf.VrsOut("SUBCLASS" + i0 + "_" + String.valueOf(subc), subClass._abbv);
                }
                final String avg = (String) hrAverage.get(AverageDat.getAverageKey(phr._avgDiv, phr._code, subClass._code));
                final String field;
                if (SUBCLASSCD_ALL3.equals(subClass._code)) {
                    field = "AVE_SUBCLASS3" + i1;
                } else if (SUBCLASSCD_ALL5.equals(subClass._code)) {
                    field = "AVE_SUBCLASS5" + i1;
                } else if (SUBCLASSCD_ALL9.equals(subClass._code)) {
                    field = "AVE_SUBCLASSALL" + i1_2;
                } else if (subc > maxColumn) {
                    field = null;
                } else {
                    field = "AVE_SUBCLASS" + i0 + "_" + String.valueOf(subc);
                }
                if (avg == null || field == null) {
                    continue;
                }
                
                if (c == maxLine) {
                    boolean isPrint = true;
                    final List chairGroupCoursecodeList = (List) chairGroupSubclassCdCourseMap.get(subClass._code);
                    // 講座グループに登録されている講座の名簿のコースは、科目は全体平均点を表示しない
                    if (null != chairGroupCoursecodeList) {
                        log.fatal(subClass._code + " tgt course = " + phr._coursecodeList + ", subclass course = " + chairGroupCoursecodeList + "(" + chairGroupSubclassCdCourseMap);
                        for (final Iterator itc = phr._coursecodeList.iterator(); itc.hasNext();) {
                            final String coursecode = (String) itc.next();
                            if (chairGroupCoursecodeList.contains(coursecode)) {
                                isPrint = false;
                                break;
                            }
                        }
                    }
                    if (isPrint) {
                        svf.VrsOutn(field, c, avg);
                    }
                } else {
                    svf.VrsOutn(field, c, avg);
                }
                hasData = true;
            }
        }
        return hasData;
    }

    /**
     * 科目別得点分布表を印字する
     * 
     * @param svf
     * @param printGroups 印字するクラス・学年
     * @param subclasses 印字する科目
     * @param subclassDistribution 得点分布データ
     */
    private boolean printSubclassDistribution(final Vrw32alp svf, final List printGroups, final List subclasses, final Map subclassDistribution, final boolean isForm1) {
        // クラス別平均点
        final String IDX_SUBC_ALL3;
        final String IDX_SUBC_ALL5;
        final int maxHrColumn = 11;
        final int maxScoreLine = TESTCD9900.equals(_param._testKindCd) ? 6 : 11;
        final String[] subIdx;
        if (TESTCD9900.equals(_param._testKindCd)) {
            if (isForm1) {
                IDX_SUBC_ALL3 = "11";
                IDX_SUBC_ALL5 = "12";
                subIdx = new String[]{"1", "2", "3", IDX_SUBC_ALL3, "4", "5", "6", IDX_SUBC_ALL5, "7", "8", "9", "13"};
            } else {
                IDX_SUBC_ALL3 = "21";
                IDX_SUBC_ALL5 = "22";
                subIdx = new String[]{"1", "2", "3", IDX_SUBC_ALL3, "4", "5", "6", IDX_SUBC_ALL5, "7", "8", "9", "23", "10", "11", "12", "24"};
            }
        } else {
            IDX_SUBC_ALL3 = "11";
            IDX_SUBC_ALL5 = "12";
            subIdx = new String[]{"1", "2", "3", IDX_SUBC_ALL3, "4", "5", "6", IDX_SUBC_ALL5, "7", "8", "9", "13"};
        }
        int si = 0;
        boolean hasData = false;
        for (final Iterator itsub = subclasses.iterator(); itsub.hasNext();) {
            final SubClass subclass = (SubClass) itsub.next();
            final String sc;
            if (SUBCLASSCD_ALL9.equals(subclass._code)) {
                sc = null;
            } else if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
                if (SUBCLASSCD_ALL3.equals(subclass._code)) {
                    sc = IDX_SUBC_ALL3;
                } else if (SUBCLASSCD_ALL5.equals(subclass._code)) {
                    sc = IDX_SUBC_ALL5;
                } else {
                    si += 1;
                    sc = String.valueOf(si);
                }
            } else {
                si += 1;
                sc = subIdx[si - 1];
            }
            final String abbvField = IDX_SUBC_ALL3.equals(sc) ? "AVERAGE_NAME1" : IDX_SUBC_ALL5.equals(sc) ? "AVERAGE_NAME2" : "13".equals(sc) ? "AVERAGE_NAME3" : "SUBCLASS2_" + sc;
            svf.VrsOut(abbvField, subclass._abbv);
            int hi = 0;
            for (final Iterator it = printGroups.iterator(); it.hasNext();) {
                final PrintGroup phr = (PrintGroup) it.next();
                hi += 1;
                final String his = phr._islast ? String.valueOf(maxHrColumn) : String.valueOf(hi);
                final String namehis = phr._islast ? null : String.valueOf(hi + (getMS932ByteLength(phr._abbv) > 6 ? 10 : 0));
                svf.VrsOut("HR_NAME" + sc + "_" + namehis, phr._abbv);
                final ScoreDistribution dist = (ScoreDistribution) subclassDistribution.get(phr._code);
                if (null != dist) {
                    for (int i = 0; i < dist._scoreKeys.length; i++) {
                        final String count = dist.getCount(subclass._code, dist._scoreKeys[i]).toString();
                        svf.VrsOutn("SUM" + sc + "_" + his, (maxScoreLine - i), count);
                        hasData = true;
                    }
                }
            }
        }
        return hasData;
    }
    
    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }
    
    private static String getKeySubClassCd(final Param param, final String subClassCd) {
        if ("1".equals(param._useCurriculumcd)) {
            final String split3 = StringUtils.split(subClassCd, "-")[3];
            if (SUBCLASSCD_ALL3.equals(split3) || SUBCLASSCD_ALL5.equals(split3) || SUBCLASSCD_ALL9.equals(split3)) {
                return split3;
            } else {
                return subClassCd;
            }
        }
        return subClassCd;
    }

    private static class AverageDat {
        private static String getAverageKey(final String avgDiv, final String code, final String subClassCd) {
            return avgDiv + "-" + code + "-" + subClassCd;
        }

        private static Map loadChairGroupSubclassCd(final DB2UDB db2, final Param param, final String grade, final List subClasses) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map chairGroupSubclassCd = new HashMap();
            try {
                final String sql = sqlChairGroupSubclass(param, grade);
                // log.debug(" sqlGroupSubclass = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassCd = getKeySubClassCd(param, rs.getString("SUBCLASSCD"));
                    if (SubClass.getSubClass(subClassCd, subClasses) == null) {
                        continue;
                    }
                    if (!chairGroupSubclassCd.containsKey(subClassCd)) {
                        chairGroupSubclassCd.put(subClassCd, new ArrayList());
                    }
                    ((List) chairGroupSubclassCd.get(subClassCd)).add(rs.getString("COURSE"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return chairGroupSubclassCd;
        }

        private static Map loadHrAverage(final DB2UDB db2, final Param param, final String grade, final List subClasses) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map rtn = new HashMap();
            try {
                final String sql = sqlHrAverage(param, grade);
                // log.debug(" sqlHrAverage = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String avgDiv = rs.getString("AVG_DIV");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String coursecode = rs.getString("COURSECODE");
                    final String code = hrClass + coursecd + majorcd + coursecode;
                    final String subClassCd = getKeySubClassCd(param, rs.getString("SUBCLASSCD"));
                    if (SubClass.getSubClass(subClassCd, subClasses) == null) {
                        continue;
                    }
                    final String avg = rs.getString("AVG") == null ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    rtn.put(AverageDat.getAverageKey(avgDiv, code, subClassCd), avg);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private static String sqlHrAverage(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("   SUBCLASSCD AS SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, SCORE, AVG ");
            stb.append(" FROM ");
            stb.append("   RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + param._ctrlYear + "' ");
            stb.append("   AND SEMESTER = '" + param._semester + "' ");
            stb.append("   AND GRADE = '" + grade + "' ");
            stb.append("   AND TESTKINDCD || TESTITEMCD = '" + param._testKindCd + "' ");
            stb.append("   AND AVG_DIV IN ('1', '2', '3') ");
            return stb.toString();
        }
        
        private static String sqlChairGroupSubclass(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T0.CLASSCD || '-' || T0.SCHOOL_KIND || '-' || T0.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T0.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,T4.COURSECD || T4.MAJORCD || T4.COURSECODE AS COURSE ");
            stb.append(" FROM CHAIR_GROUP_MST T0 ");
            stb.append(" INNER JOIN CHAIR_GROUP_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("     AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("     AND T1.CHAIR_GROUP_CD = T0.CHAIR_GROUP_CD ");
            stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ");
            stb.append("     AND T4.YEAR = T3.YEAR ");
            stb.append("     AND T4.SEMESTER = T3.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("   T0.YEAR = '" + param._ctrlYear + "' ");
            if (SEME_ALL.equals(param._semester)) {
                stb.append("    AND T0.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("    AND T0.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD = '0000' "); // 固定
            stb.append("   AND T4.GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
    }

    /**
     * 印字するクラス・学年
     */
    private static class PrintGroup {
        /** 平均値区分 学年=1 クラス=2 */
        final String _avgDiv;
        final String _code;
        final String _name;
        final String _abbv;
        final boolean _islast;
        final List _coursecodeList;
        PrintGroup(final String avgDiv, final String code, final String name, final String abbv, final boolean islast) {
            _avgDiv = avgDiv;
            _code = code;
            _name = name;
            _abbv = abbv;
            _islast = islast;
            _coursecodeList = new ArrayList();
        }
        
        private static List getPrintGroups(final DB2UDB db2, final Param param, final String grade, final Course course) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Set coursecodes = new HashSet();
            final List rtn = new ArrayList();
            try {
                final StringBuffer sql = new StringBuffer();
//                if ("1".equals(param._group)) {
                    sql.append(" SELECT DISTINCT T1.HR_CLASS || '00000000' AS CODE, ");
                    sql.append(" T2.HR_NAME AS NAME, T2.HR_NAMEABBV AS ABBV, ");
//                } else if ("2".equals(param._group)) {
//                    sql.append(" SELECT DISTINCT '000' || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS CODE, ");
//                    sql.append(" T4.COURSECODENAME AS NAME, T4.COURSECODENAME AS ABBV ");
//                }
                    sql.append(" T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECODE ");
                    sql.append(" FROM SCHREG_REGD_DAT T1");
//                if ("1".equals(param._group)) {
                    sql.append(" INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
                    sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                    sql.append("     AND T2.GRADE = T1.GRADE ");
                    sql.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
//                } else if ("2".equals(param._group)) {
//                    sql.append(" INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
//                    sql.append(" INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD ");
//                    sql.append(" INNER JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ");
//                }
                sql.append(" WHERE T1.YEAR = '" + param._ctrlYear + "' ");
                if (SEME_ALL.equals(param._semester)) {
                    sql.append("    AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
                } else {
                    sql.append("    AND T1.SEMESTER = '" + param._semester + "' ");
                }
                sql.append("     AND T1.GRADE = '" + grade + "' ");
                if (null != course) {
                    sql.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course._coursecode + "' ");
                }
                sql.append(" ORDER BY CODE ");
                log.debug(" printgroups sql = " + sql.toString());
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String div;
//                    if ("2".equals(param._group)) {
//                        div = AVG_DIV_COURSE;
//                    } else { // if ("1".equals(param._printDiv)) {
                        div = AVG_DIV_HR_CLASS;
//                    }
                    final String code = rs.getString("CODE");
                    PrintGroup pg = null;
                    for (final Iterator it = rtn.iterator(); it.hasNext();) {
                        PrintGroup pg0 = (PrintGroup) it.next();
                        if (pg0._code.equals(code)) {
                            pg = pg0;
                            break;
                        }
                    }
                    if (null == pg) {
                        final String name = rs.getString("NAME");
                        final String abbv = rs.getString("ABBV");
                        pg = new PrintGroup(div, code, name, abbv, false);
                        rtn.add(pg);
                    }
                    coursecodes.add(rs.getString("COURSECODE"));
                }
            } catch (SQLException e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final PrintGroup pgLast;
            if (null != course) {
                pgLast = new PrintGroup(AVG_DIV_COURSE, "000" + course._coursecode, "全体", "全体", true);
            } else {
                pgLast = new PrintGroup(AVG_DIV_GRADE, AVERAGE_DAT_GRADE_CODE, "全体", "全体", true);
            }
            pgLast._coursecodeList.addAll(coursecodes);
            rtn.add(pgLast);
            return rtn;
        }
        
        public String toString() {
            return "PrintGroup(" + _code + ", " + _name + ")";
        }
    }

    /**
     * 科目
     */
    private static class SubClass {
        private final String _code;
        private final String _abbv;
        private final String _name;
        SubClass(final String code, final String abbv, final String name) {
            _code = code;
            _abbv = abbv;
            _name = name;
        }
        
        private static List getSubclasses(final DB2UDB db2, final Param param, final String grade, final Course course, final boolean isAvg) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtn = new ArrayList();
            try {
                final String sql = sqlSubclassList(param, grade, course, isAvg);
                // log.debug(" sqlSubclassList (isAvg = " + isAvg + " ) = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassCd = getKeySubClassCd(param, rs.getString("SUBCLASSCD"));
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
//            for (Iterator it = rtn.iterator(); it.hasNext();) {
//                SubClass s = (SubClass) it.next();
//                log.debug(" subClass = " + s);
//            }
            return rtn;
        }

        private static SubClass getSubClass(final String subClassCd, final List subClasses) {
            for (final Iterator it = subClasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (subClassCd != null && subClassCd.equals(subClass._code)) {
                    return subClass;
                }
            }
            return null;
        }

        private static String sqlSubclassList(final Param param, final String grade, final Course course, final boolean isAvg) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH RECORD AS (");
            stb.append(sqlSchregRecordRankDat(param, grade, course));
            stb.append(" ) , COURSE AS ( ");
            stb.append("   SELECT DISTINCT T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECD ");
            stb.append("   FROM RECORD T1 ");
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT VALUE(T4.SHOWORDER4, 999), ");
            stb.append("      T1.SUBCLASSCD, T2.SUBCLASSNAME, T2.SUBCLASSABBV ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" INNER JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("     T4.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            }
            if (!isAvg) {
                stb.append(" INNER JOIN REC_SUBCLASS_GROUP_DAT T3 ON T3.YEAR = T1.YEAR AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T3.COURSECD || T3.MAJORCD || T3.COURSECODE IN (SELECT COURSECD FROM COURSE) ");
            }
            stb.append(" ORDER BY VALUE(T4.SHOWORDER4, 999), T1.SUBCLASSCD ");
            return stb.toString();
        }
        
        private static String sqlSchregRecordRankDat(final Param param, final String grade, final Course course) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.SEMESTER, ");
            stb.append("     T2.GRADE, T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE, ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, T1.SCORE, T1.AVG ");
            stb.append(" FROM RECORD_RANK_DAT T1");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            if (SEME_ALL.equals(param._semester)) {
                stb.append("    AND T2.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("    AND T2.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testKindCd + "' ");
            stb.append("    AND T2.GRADE= '" + grade + "' ");
            if (null != course) {
                stb.append("    AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + course._coursecode + "' ");
            }
            return stb.toString();
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
    
    private static class Course {
        final String _coursecode;
        final String _name;
        final String _abbv;
        public Course(final String course, final String name, final String abbv) {
            _coursecode = course;
            _name = name;
            _abbv = abbv;
        }
    }

    /**
     * 科目ごとの得点分布
     */
    private static class ScoreDistribution {
        private final String[] _scoreKeys;
        
        public final Set _keySubClasses = new HashSet();
        private final String _key;
        private final Map _distributions = new HashMap();

        private ScoreDistribution(final String[] scoreKeys, final String key) {
            _scoreKeys = scoreKeys;
            _key = key;
        }

        private Map getSubclassDistributionMap(final SubClass subClass) {
            return getSubclassDistributionMap(subClass._code);
        }

        private Map getSubclassDistributionMap(final String subClassCd) {
            if (!_distributions.containsKey(subClassCd)) {
                _distributions.put(subClassCd, new HashMap());
            }
            return (Map) _distributions.get(subClassCd);
        }

        public void add(final SubClass subClass, final Integer score, final int scoreDiv) {
            final int scoreKeyInd = (score.intValue() / scoreDiv);
            if (scoreKeyInd <= _scoreKeys.length) {
                _keySubClasses.add(subClass);
                increment(subClass, _scoreKeys[scoreKeyInd]);
            }
        }

        private void increment(final SubClass subClass, final String scoreKey) {
            final Integer count = getCount(subClass._code, scoreKey);
            getSubclassDistributionMap(subClass).put(scoreKey, new Integer(count.intValue() + 1));
        }

        public Integer getCount(final String subClassCd, final String scoreKey) {
            final Map subclassScoreDist = getSubclassDistributionMap(subClassCd);
            final Integer count;
            if (subclassScoreDist.containsKey(scoreKey)) {
                count = (Integer) subclassScoreDist.get(scoreKey);
            } else {
                count = Integer.valueOf("0");
            }
            return count;
        }

        private String distStr() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (final Iterator it = _keySubClasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                stb.append("[subClass=").append(subClass.toString());
                for (int i = 0; i < _scoreKeys.length; i++) {
                    final String scoreKey = _scoreKeys[i];
                    final Integer count = getCount(subClass._code, scoreKey);
                    stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
                    comma = ", ";
                }
                stb.append("] ");
            }
            return stb.toString();
        }
        
        private static Map loadSubclassDistribution(final DB2UDB db2, final Param param, final String grade, final Course course, final List subClasses) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            rtn.put(AVERAGE_DAT_GRADE_CODE, new ScoreDistribution(param._scoreKeys, AVERAGE_DAT_GRADE_CODE));
            try {
                final String sql = SubClass.sqlSchregRecordRankDat(param, grade, course);
                log.debug(" dist sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
//                    final String hrClass = "1".equals(param._group) ? rs.getString("HR_CLASS") : "000";
//                    final String coursecd = "2".equals(param._group) ? rs.getString("COURSECD") : "0";
//                    final String majorcd = "2".equals(param._group) ? rs.getString("MAJORCD") : "000";
//                    final String coursecode = "2".equals(param._group) ? rs.getString("COURSECODE") : "0000";
                    final String hrClass = rs.getString("HR_CLASS");
                    final String coursecd = "0";
                    final String majorcd = "000";
                    final String coursecode = "0000";
                    final String code = hrClass + coursecd + majorcd + coursecode;
                    final String subClassCd = getKeySubClassCd(param, rs.getString("SUBCLASSCD"));
                    final Integer score;
                    if (SUBCLASSCD_ALL3.equals(subClassCd) || SUBCLASSCD_ALL5.equals(subClassCd)) {
                        score = rs.getString("AVG") == null ? null : new Integer(Double.valueOf(rs.getString("AVG")).intValue());
                    } else {
                        score = rs.getString("SCORE") == null ? null : Integer.valueOf(rs.getString("SCORE"));
                    }
                    final SubClass subClass = SubClass.getSubClass(subClassCd, subClasses);
                    if (subClass == null || score == null) {
                        continue;
                    }
                    if (!rtn.containsKey(code)) {
                        rtn.put(code, new ScoreDistribution(param._scoreKeys, code));
                    }
                    ScoreDistribution dist;
                    dist = (ScoreDistribution) rtn.get(code);
                    dist.add(subClass, score, param._scoreDiv);
                    dist = (ScoreDistribution) rtn.get(AVERAGE_DAT_GRADE_CODE);
                    dist.add(subClass, score, param._scoreDiv);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        public String toString() {
            return " dist = (" + distStr() + ")";
        }
    }

    private Param createParam(HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private static class Param {
        private static final String[] scoreKeys100 = new String[] {
            "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100",
        };
        
        private static final String[] scoreKeys5 = new String[] {
            "0", "1", "2", "3", "4", "5",
        };

        /** 年度 */
        final String _ctrlYear;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期） */
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _testKindCd;
        final String _grade;
        // final String _form;
        final String _group;
        final String _formSelect; // 1:18科目 2:25科目
        final String _useCurriculumcd;
        final String[] _scoreKeys;
        final int _scoreDiv;
        private String _semesterName;
        private String _testItemName;
        private String _gradeName;
        private String _schoolKind;
        private String _hrstaffname;
        private static final String FROM_TO_MARK = "\uFF5E";

        Param(final HttpServletRequest request) throws ServletException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _testKindCd = request.getParameter("TESTKINDCD"); // テスト・成績種別
            _grade = request.getParameter("GRADE");
            // _form = request.getParameter("FORM"); // 「平均点一覧のみ」チェックボックス  {GRADE, SCHOOL_KIND} => {98, 'J'}, {99, 'H'}
            _group = request.getParameter("GROUP"); // 1:クラス別 2:コース別
            _formSelect = request.getParameter("FORM_SELECT");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            if (TESTCD9900.equals(_testKindCd)) {
                _scoreKeys = scoreKeys5;
                _scoreDiv = 1;
            } else {
                _scoreKeys = scoreKeys100;
                _scoreDiv = 10;
            }
        }

        public void load(final DB2UDB db2, final String grade) {
            setTestName(db2);
            // setHrStaff(db2);
            setGradeName(db2, grade);
            setSemesterName(db2);
        }
        
        private List getCourseList(final DB2UDB db2, final String grade) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT DISTINCT ");
                sql.append("   T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ");
                sql.append("  ,T4.COURSECODENAME AS NAME ");
                sql.append("FROM SCHREG_REGD_DAT T1 "); 
                sql.append(" INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
                sql.append(" INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD ");
                sql.append(" INNER JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ");
                sql.append("WHERE T1.YEAR = '" + _ctrlYear + "' ");
                sql.append("      AND T1.GRADE = '" + grade  +"' ");
                // log.debug(" gradeName sql = " + sql);
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Course course = new Course(rs.getString("COURSE"), rs.getString("NAME"), rs.getString("NAME"));
                    rtn.add(course);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
    }
}

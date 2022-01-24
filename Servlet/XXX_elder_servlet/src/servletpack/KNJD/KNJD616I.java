/*
 * $Id: dc8a1c3eb8d8a37543881ddd3eab949c517f04aa $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD616I {

    private static final Log log = LogFactory.getLog(KNJD616I.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private static final String ALL_SEME = "9";
    private static final String HYOTEI_TESTCD = "9990009";
    
    private final String _1010101 = "1010101";
    private final String _1020101 = "1020101";
    private final String _2010101 = "2010101";
    private final String _2020101 = "2020101";
    private final String _3020101 = "3020101";
    private final String _1990008 = "1990008";
    private final String _2990008 = "2990008";
    private final String _3990008 = "3990008";
    private final String _9990008 = "9990008";

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

            for (int i = 0; i < _param._hrClasses.length; i++) {
                final HomeRoom homeroom = HomeRoom.query(db2, _param, _param._hrClasses[i]);
                
                for (int j = 0; j < _param._subclasscds.length; j++) {
                    final SubclassMst subclassMst = (SubclassMst) _param._subclassMstMap.get(StringUtils.split(_param._subclasscds[j], ":")[1]);
                    if (null != subclassMst) {
                        if (subclassMst._isSaki) {
                            continue;
                        }
                        log.info(" hr = " + homeroom._gradehrclass + " / subclass " + subclassMst.getKeySubclassCd());
                        
                        final List subclassSplitByCreditList = getSubclassSplitByCreditList(Subclass.getSubclassList(db2, _param, subclassMst, homeroom));

                        printSubclass(subclassSplitByCreditList, svf);
                    }
                }
            }
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
    
    private List getSubclassSplitByCreditList(final List subclassList) {
        for (int subi = 0; subi < subclassList.size(); subi++) {
            
            final Subclass subclass = (Subclass) subclassList.get(subi);
            final List newHrClassList = new ArrayList();
            
            for (final Iterator hrit = subclass._hrClassList.iterator(); hrit.hasNext();) {
                final HomeRoom hr = (HomeRoom) hrit.next();
                
                // 1つのクラスを単位数ごとに改ページする
                TreeSet creditSet = getMappedTreeSet(hr._hrCreditSetMap, hr._gradehrclass);
                if (creditSet.size() <= 1) {
                    // 単位数が1種類しかない
                    newHrClassList.add(hr);
                    continue;
                }
                
                log.info(" split by credit :" + subclass._subclassMst.getKeySubclassCd() + " / " + hr._gradehrclass + " / " + creditSet);

                for (final Iterator it = creditSet.iterator(); it.hasNext();) {
                    final Integer credit = (Integer) it.next();
                    
                    final HomeRoom newHr = hr.copy();
                    newHrClassList.add(newHr);
                    
                    for (int j = 0; j < hr._studentList.size(); j++) {
                        final Student student = (Student) hr._studentList.get(j);
                        
                        if (credit == student._creditMstCreditsMap.get(subclass._subclassMst.getKeySubclassCd())) {
                            newHr._studentList.add(student);
                        }
                    }
                    log.info(" credit = " + credit + " / student size = " + newHr._studentList.size());
                    
                    getMappedTreeSet(newHr._hrCreditSetMap, newHr._gradehrclass).add(credit);
                }
            }
            
            subclass._hrClassList.clear();
            subclass._hrClassList.addAll(newHrClassList);
            
        }
        return subclassList;
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (s != null) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }
    
    private static String mkString(final List list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String c = "";
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String s = (String) it.next();
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(c).append(s);
            c = comma;
        }
        return stb.toString();
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static TreeSet getMappedTreeSet(final Map m, final String key) {
        if (null == m.get(key)) {
            m.put(key, new TreeSet());
        }
        return (TreeSet) m.get(key);
    }

    private static String max(final String intStr1, final String intStr2) {
        if (!NumberUtils.isNumber(intStr1)) { return intStr2; }
        if (!NumberUtils.isNumber(intStr2)) { return intStr1; }
        final BigDecimal bd1 = new BigDecimal(intStr1);
        final BigDecimal bd2 = new BigDecimal(intStr2);
        return (bd1.compareTo(bd2) > 0 ? bd1 : bd2).toString();
    }

    private void printSubclass(final List subclassList, final Vrw32alp svf) {
        final String form = "KNJD616I.frm";
        final int maxLine = 52;
        final boolean absentIsJissu = "3".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov);
        
        for (int subi = 0; subi < subclassList.size(); subi++) {
            
            final Subclass subclass = (Subclass) subclassList.get(subi);
            final SubclassMst subclassMst = subclass._subclassMst;
            final SubclassMst gappeisakiSubclassMst = SubclassMst.getPrintGappeisakiSubclassMst(subclassMst);
            if (null != gappeisakiSubclassMst) {
                log.info(" gappekisaki = " + gappeisakiSubclassMst._subclasscd + " ( subclass = " + subclassMst._subclasscd + ")");
            }
            
            for (int hri = 0; hri < subclass._hrClassList.size(); hri++) {
                final HomeRoom hr = (HomeRoom) subclass._hrClassList.get(hri);
                
                final List pageList = getPageList(hr._studentList, maxLine - 2);
                
                final Map avgTargetMap = new HashMap();
                for (int pi = 0; pi < pageList.size(); pi++) {
                    
                    final boolean isLastPage = pageList.size() - 1 == pi;
                    final List studentList = (List) pageList.get(pi);
                    
                    svf.VrSetForm(form, 1);

                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　成績評定及出席状況報告単票"); // タイトル
                    svf.VrsOut("SCHOOL_NAME", (String) _param._schoolKindSchoolMstSchoolname1Map.get(hr._schoolKind)); // 学校名
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日付
                    svf.VrsOut("GRADE", NumberUtils.isDigits(hr._gradeCd) ? String.valueOf(Integer.parseInt(hr._gradeCd)) : StringUtils.defaultString(hr._gradeCd)); // 学年
                    svf.VrsOut("HR_NAME", hr._hrClassName1); // クラス名
                    svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclass._subclassMst._subclassname) > 20 ? "2" : ""), subclass._subclassMst._subclassname); // 科目名
                    
                    final TreeSet creditSet = (TreeSet) hr._hrCreditSetMap.get(hr._gradehrclass);
                    if (null != creditSet) {
                        if (creditSet.size() == 0) {
                        } else if (creditSet.size() == 1) {
                            svf.VrsOut("CREDIT", creditSet.iterator().next().toString()); // 単位数
                        } else {
                            final Integer min = (Integer) creditSet.first();
                            final Integer max = (Integer) creditSet.last();
                            svf.VrsOut("CREDIT", min.toString() + FROM_TO_MARK + max.toString()); // 単位数
                        }
                    }
                    
                    svf.VrsOut(getMS932ByteLength(mkString(hr._hrStfStfName, "、")) <= 20 ? "HR_TEACHER1" : "HR_TEACHER2", mkString(hr._hrStfStfName, "、"));
                    
                    final List chairList = getMappedList(subclass._hrChairMap, hr._gradehrclass);
                    final String subclassTeacher = mkString(Chair.getStaffnameList(chairList), "、");
                    svf.VrsOut(getMS932ByteLength(subclassTeacher) <= 44 ? "SUBCLASS_TEACHER1" : "SUBCLASS_TEACHER2", subclassTeacher);
                    
                    for (int si = 0; si < _param._semesterList.size(); si++) {
                        final Semester semester = (Semester) _param._semesterList.get(si);
                        svf.VrsOut("SEMESTER" + semester._cdSemester, semester._semestername); // 学期名
                    }
                    
                    String lesson1Max = null, lesson2Max = null, lesson3Max = null;
                    for (int j = 0; j < studentList.size(); j++) {
                        final int line = j + 1;
                        
                        final Student student = (Student) studentList.get(j);
                        
                        final String attendno = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);
                        svf.VrsOutn("NO", line, attendno); // 番号
                        
                        final String name = student._name;
                        svf.VrsOutn(getMS932ByteLength(name) <= 20 ? "NAME1" : getMS932ByteLength(name) <= 30 ? "NAME2" : "NAME3", line, name);

                        printScore(svf, "SCORE1_1", line, _1010101, subclassMst, student, avgTargetMap); // 評価
                        printScore(svf, "SCORE1_2", line, _1020101, subclassMst, student, avgTargetMap); // 評価
                        printScore(svf, "SCORE1_3", line, _1990008, subclassMst, student, avgTargetMap); // 評価
                        svfVrsOutnKirikae(svf, "NOTICE1", line, student.getSick2(subclassMst, "1", absentIsJissu, _param)); // 欠課
                        
//                      final String remark1_ = null;
//                      svf.VrsOutn(getMS932ByteLength(remark1_) <= 8 ? "REMARK1_1" : "REMARK1_2", line, remark1_);
                        
                        printScore(svf, "SCORE2_1", line, _2010101, subclassMst, student, avgTargetMap); // 評価
                        printScore(svf, "SCORE2_2", line, _2020101, subclassMst, student, avgTargetMap); // 評価
                        printScore(svf, "SCORE2_3", line, _2990008, subclassMst, student, avgTargetMap); // 評価
                        svfVrsOutnKirikae(svf, "NOTICE2", line, student.getSick2(subclassMst, "2", absentIsJissu, _param)); // 欠課
                        
//                      final String remark2_ = null;
//                      svf.VrsOutn(getMS932ByteLength(remark2_) <= 8 ? "REMARK2" : "REMARK2_2", line, remark2_);
                        
                        printScore(svf, "SCORE3_2", line, _3020101, subclassMst, student, avgTargetMap); // 評価
                        printScore(svf, "SCORE3_3", line, _3990008, subclassMst, student, avgTargetMap); // 評価
                        svfVrsOutnKirikae(svf, "NOTICE3", line, student.getSick2(subclassMst, "3", absentIsJissu, _param)); // 欠課
                        
//                      final String remark3 = null;
//                      svf.VrsOutn(getMS932ByteLength(remark3) <= 8 ? "REMARK3" : "REMARK3_2", line, remark3);
                        
                        if (subclassMst._isMoto) {
                            printScore(svf, "SCORE9", line, _9990008, subclassMst, student, avgTargetMap); // 評価
                            svfVrsOutnKirikae(svf, "NOTICE9", line, student.getSick2(subclassMst, "9", absentIsJissu, _param)); // 欠課
                            svfVrsOutnKirikae(svf, "LESSON9", line, student.getJugyoJisu(subclassMst, "9")); // 時数
                            
                            if (null != gappeisakiSubclassMst) {
                                // 合併先の値
                                printScore(svf, "SCORE99", line, _9990008, gappeisakiSubclassMst, student, avgTargetMap); // 評価
                                printScore(svf, "VAL99", line, HYOTEI_TESTCD, gappeisakiSubclassMst, student, avgTargetMap); // 評定
                                svfVrsOutnKirikae(svf, "NOTICE99", line, student.getSick2(gappeisakiSubclassMst, "9", absentIsJissu, _param)); // 欠課
                                svfVrsOutnKirikae(svf, "LESSON99", line, student.getMust(gappeisakiSubclassMst, "9")); // 時数
                            }

                        } else {
                            printScore(svf, "SCORE99", line, _9990008, subclassMst, student, avgTargetMap); // 評価
                            printScore(svf, "VAL99", line, HYOTEI_TESTCD, subclassMst, student, avgTargetMap); // 評定
                            svfVrsOutnKirikae(svf, "NOTICE99", line, student.getSick2(subclassMst, "9", absentIsJissu, _param)); // 欠課
                            svfVrsOutnKirikae(svf, "LESSON99", line, student.getMust(subclassMst, "9")); // 時数
                        }
                        
                        
                        final String remark99 = student.getRemark(gappeisakiSubclassMst, subclassMst);
                        svf.VrsOutn(getMS932ByteLength(remark99) <= 18 ? "REMARK99_1" : "REMARK99_2", line, remark99);

                        lesson1Max = max(lesson1Max, student.getJugyoJisu(subclassMst, "1"));
                        lesson2Max = max(lesson2Max, student.getJugyoJisu(subclassMst, "2"));
                        lesson3Max = max(lesson3Max, student.getJugyoJisu(subclassMst, "3"));
                    }
                    
                    svf.VrsOut("LESSON1", lesson1Max); // 授業時数
                    svf.VrsOut("LESSON2", lesson2Max); // 授業時数
                    svf.VrsOut("LESSON3", lesson3Max); // 授業時数
                    
                    if (isLastPage) {
                        int line;
                        line = maxLine - 1;
                        svfVrsOutnKirikae(svf, "SCORE1_1", line, sum(getMappedList(avgTargetMap, _1010101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE1_2", line, sum(getMappedList(avgTargetMap, _1020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE1_3", line, sum(getMappedList(avgTargetMap, _1990008))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_1", line, sum(getMappedList(avgTargetMap, _2010101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_2", line, sum(getMappedList(avgTargetMap, _2020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_3", line, sum(getMappedList(avgTargetMap, _2990008))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE3_2", line, sum(getMappedList(avgTargetMap, _3020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE3_3", line, sum(getMappedList(avgTargetMap, _3990008))); // 評価
                        
                        if (subclassMst._isMoto) {
                            svfVrsOutnKirikae(svf, "SCORE9", line, sum(getMappedList(avgTargetMap, _9990008))); // 評価

                            if (null != gappeisakiSubclassMst) {
                                // 合併先の値
                                svfVrsOutnKirikae(svf, "SCORE99", line, sum(getMappedList(avgTargetMap, _9990008))); // 評価
                                svfVrsOutnKirikae(svf, "VAL99", line, sum(getMappedList(avgTargetMap, HYOTEI_TESTCD))); // 評定
                            }
                        } else {
                            svfVrsOutnKirikae(svf, "SCORE99", line, sum(getMappedList(avgTargetMap, _9990008))); // 評価
                            svfVrsOutnKirikae(svf, "VAL99", line, sum(getMappedList(avgTargetMap, HYOTEI_TESTCD))); // 評定
                        }

                        line = maxLine;
                        svfVrsOutnKirikae(svf, "SCORE1_1", line, avg(getMappedList(avgTargetMap, _1010101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE1_2", line, avg(getMappedList(avgTargetMap, _1020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE1_3", line, avg(getMappedList(avgTargetMap, _1990008))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_1", line, avg(getMappedList(avgTargetMap, _2010101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_2", line, avg(getMappedList(avgTargetMap, _2020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE2_3", line, avg(getMappedList(avgTargetMap, _2990008))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE3_2", line, avg(getMappedList(avgTargetMap, _3020101))); // 評価
                        svfVrsOutnKirikae(svf, "SCORE3_3", line, avg(getMappedList(avgTargetMap, _3990008))); // 評価
                        if (subclassMst._isMoto) {
                            svfVrsOutnKirikae(svf, "SCORE9", line, avg(getMappedList(avgTargetMap, _9990008))); // 評価

                            if (null != gappeisakiSubclassMst) {
                                // 合併先の値
                                svfVrsOutnKirikae(svf, "SCORE99", line, avg(getMappedList(avgTargetMap, _9990008))); // 評価
                                svfVrsOutnKirikae(svf, "VAL99", line, avg(getMappedList(avgTargetMap, HYOTEI_TESTCD))); // 評定
                            }
                        } else {
                            svfVrsOutnKirikae(svf, "SCORE99", line, avg(getMappedList(avgTargetMap, _9990008))); // 評価
                            svfVrsOutnKirikae(svf, "VAL99", line, avg(getMappedList(avgTargetMap, HYOTEI_TESTCD))); // 評定
                        }
                    }
                    
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }
    
    private static void svfVrsOutnKirikae(final Vrw32alp svf, final String field, final int line, final String val) {
        if (null == val) {
            return;
        }
        if (getMS932ByteLength(val) > 3) {
            svf.VrsOutn(field + "_2", line, val);
        } else {
            svf.VrsOutn(field, line, val);
        }
    }

    private String avg(final List scoreList) {
        final String sum = sum(scoreList);
        return scoreList.isEmpty() ? null : new BigDecimal(sum).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString(); 
    }

    private String sum(final List scoreList) {
        int sum = 0;
        boolean hasData = false;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            sum += Integer.parseInt(score);
            hasData = true;
        }
        return hasData ? String.valueOf(sum) : null;
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private void printScore(final Vrw32alp svf, final String field, final int line, final String testcd, final SubclassMst subclassMst, final Student student, final Map avgTargetMap) {
        final String score = student.getScore(subclassMst, testcd);
        svfVrsOutnKirikae(svf, field, line, score);
        if (NumberUtils.isDigits(score)) {
            getMappedList(avgTargetMap, testcd).add(score);
            final boolean isAkaten;
            if (HYOTEI_TESTCD.equals(testcd)) {
                isAkaten = Integer.parseInt(score) == 1;
            } else {
                isAkaten = Integer.parseInt(score) < 30;
            }
            if (isAkaten) {
                svf.VrAttributen(field, line, "Palette=9");
            }
        }
    }
    
    
    private static class Student {
        private static DecimalFormat df1 = new DecimalFormat("0");
        private static DecimalFormat df2 = new DecimalFormat("00");
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _hrClassName1;
        final String _hrStfStaffname;
        final String _majorname;
        final String _attendno;
        int _gyo;
        final Map _subclassMap;
        final Map _creditMstCreditsMap;
        final Map _remarkArrayMap;

        Student(String schregno, String name, String grade, String hrClass, String hrClassName1, String hrStfStaffname,
                String attendno, String majorname) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrClass = hrClass;
            _hrClassName1 = hrClassName1;
            _hrStfStaffname = hrStfStaffname;
            _attendno = attendno;
            _majorname = majorname;
            _subclassMap = new HashMap();
            _creditMstCreditsMap = new HashMap();
            _remarkArrayMap = new HashMap();
        }
        
        String val(final Map map, final String key) {
            return (String) map.get(key);
        }

        public String getRemark(final SubclassMst gappeisakiSubclassMst, final SubclassMst subclassMst) {
            final List subclassList = new ArrayList();
            if (null != gappeisakiSubclassMst) {
                subclassList.addAll(gappeisakiSubclassMst._attendsubclassList);
            } else {
                subclassList.add(subclassMst);
            }
            BigDecimal bdKoketsu = null;
            BigDecimal bdKibiki = null;
            BigDecimal bdShuttei = null;
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final SubclassMst sm = (SubclassMst) it.next();
                StudentSubclass studentSubclass = getStudentSubclass(sm, _subclassMap) ;
                if (null != studentSubclass) {
                    final String koketsu = (String) studentSubclass._koketsuMap.get(ALL_SEME);
                    final String kikbiki = (String) studentSubclass._kibikiMap.get(ALL_SEME);
                    final String shuttei = (String) studentSubclass._shutteiMap.get(ALL_SEME);
                    if (null != koketsu) {
                        bdKoketsu = (null == bdKoketsu ? new BigDecimal(0) : bdKoketsu).add(new BigDecimal(koketsu));
                    }
                    if (null != kikbiki) {
                        bdKibiki = (null == bdKibiki ? new BigDecimal(0) : bdKibiki).add(new BigDecimal(kikbiki));
                    }
                    if (null != shuttei) {
                        bdShuttei = (null == bdShuttei ? new BigDecimal(0) : bdShuttei).add(new BigDecimal(shuttei));
                    }
                }
            }
            final String[] title = {"公欠", "忌引", "出停"};
            final BigDecimal[] bds = {bdKoketsu, bdKibiki, bdShuttei};
            final StringBuffer remark = new StringBuffer();
            String comma = "";
            for (int i = 0; i < title.length; i++) {
                if (null == bds[i] || bds[i].doubleValue() == 0.0) {
                    continue;
                }
                remark.append(comma).append(title[i]).append(bds[i]);
                comma = "、";
            }
            return remark.toString();
        }

        public String getGyoNoStr() {
            return "　" + (_gyo < 10 ? " " : "") + String.valueOf(_gyo);
        }

        public String getAttendNoStr() {
            final String gr = !NumberUtils.isDigits(_grade) ? " " : df1.format(Integer.parseInt(_grade));
            final String hr = !NumberUtils.isDigits(_hrClass) ? " " : df1.format(Integer.parseInt(_hrClass));
            final String at = !NumberUtils.isDigits(_attendno) ? "  " : df2.format(Integer.parseInt(_attendno));
            return gr + "-" + hr + "-" + at;
        }

        private StudentSubclass createStudentSubclass(final SubclassMst subclassMst) {
            if (null == getStudentSubclass(subclassMst, _subclassMap)) {
                _subclassMap.put(subclassMst.getKeySubclassCd(), new StudentSubclass(subclassMst));
            }
            return getStudentSubclass(subclassMst, _subclassMap);
        }

        public SubclassScore getSubclassScore(final SubclassMst subclassMst, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclassMst);
            return (SubclassScore) studentSubclass._scoreMap.get(testcd);
        }
        
        public String getScore(final SubclassMst subclassMst, final String testcd) {
            final SubclassScore subclassScore = getSubclassScore(subclassMst, testcd);
//            log.debug(" " + _schregno + ", " + testcd + ", " + subclassScore + ", " + _subclassMap);
            if (null == subclassScore) {
                return null;
            }
            if (null != subclassScore._valueDi) {
                return subclassScore._valueDi;
            }
            return subclassScore._score;
        }
        
        public String getKoketsu(final SubclassMst subclassMst, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclassMst);
            return val(studentSubclass._koketsuMap, testcd);
        }
        
        public String getJugyoJisu(final SubclassMst subclassMst, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclassMst);
            return subtract(val(studentSubclass._jugyoJisuMap, testcd), getKoketsu(subclassMst, testcd));
        }
        
        public String getMust(final SubclassMst subclassMst, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclassMst);
            return subtract(val(studentSubclass._mustMap, testcd), getKoketsu(subclassMst, testcd));
        }
        
        public String getSick2(final SubclassMst subclassMst, final String testcd, final boolean isJissu, final Param param) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclassMst);
            final String sick2 = (String) studentSubclass._sick2Map.get(testcd);
            return null == sick2 ? null : new BigDecimal(sick2).setScale(isJissu ? 1 : 0, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String toString() {
            return "Student(" + _schregno + ":" + _name + ")";
        }
        
        public static Student getStudent(final String schregno, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
    }
    
    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        final List _attendsubclassList = new ArrayList();
        final List _combinedsubclassList = new ArrayList();

        SubclassMst(
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String subclassname,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }

        public String getKeySubclassCd() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }
        
        public int compareTo(final Object o) {
            final SubclassMst other = (SubclassMst) o;
            return getKeySubclassCd().compareTo(other.getKeySubclassCd());
        }
        
        // 引数が合併元科目かつ合併先科目の元科目の科目コード順にソートした一つ目なら合併先科目のSubclassMstを返す
        public static SubclassMst getPrintGappeisakiSubclassMst(final SubclassMst subclassMst) {
            if (!subclassMst._isMoto || subclassMst._combinedsubclassList.size() == 0) {
                return null;
            }
            final SubclassMst gappeisaki = (SubclassMst) subclassMst._combinedsubclassList.get(0);
            if (gappeisaki._attendsubclassList.size() == 0) {
                log.warn("科目" + subclassMst._subclasscd + "の元科目がない");
                return null;
            }
            if (subclassMst != gappeisaki._attendsubclassList.get(0)) {
                return null;
            }
            return gappeisaki;
        }
    }

    private static class Subclass {
        final SubclassMst _subclassMst;
        final Map _hrChairMap = new TreeMap();
        final List _hrClassList = new ArrayList();

        public Subclass(final SubclassMst subclassMst) {
            _subclassMst = subclassMst;
        }
        
        public static List getSubclassList(final DB2UDB db2, final Param param, final SubclassMst paramSubclassMst, final HomeRoom homeRoom) {
            final List subclasslist = new ArrayList();
            final Map subclassMap = new HashMap();
            
            final Map studentMap = new HashMap();

            final String sql = getRecordSql(param, paramSubclassMst, homeRoom);
            log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final SubclassMst subclassMst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                
                if (null == subclassMap.get(subclasscd)) {
                    final Subclass subclass = new Subclass(subclassMst);
                    if (!subclassMst._isSaki) {
                        // 先科目は成績のみ表示
                        subclasslist.add(subclass);
                    }
                    subclassMap.put(subclassMst.getKeySubclassCd(), subclass);
                }
                final Subclass subclass = (Subclass) subclassMap.get(subclassMst.getKeySubclassCd());
                
                final String key = KnjDbUtils.getString(row, "GRADE") + KnjDbUtils.getString(row, "HR_CLASS");
                if (null == HomeRoom.getHrclass(key, subclass._hrClassList)) {
                    final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                    final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                    final String hrName = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    final String hrStfStaffname = KnjDbUtils.getString(row, "HR_STF_STAFFNAME");
                    subclass._hrClassList.add(new HomeRoom(key, schoolKind, gradeCd, hrName, Arrays.asList(new String[] {hrStfStaffname})));
                }
                final HomeRoom hrclass = HomeRoom.getHrclass(key, subclass._hrClassList);
                
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (null == Student.getStudent(schregno, hrclass._studentList)) {
                    if (null == studentMap.get(schregno)) {
                        final String name = KnjDbUtils.getString(row, "NAME");
                        final String grade = KnjDbUtils.getString(row, "GRADE");
                        final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                        final String hrClassName1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                        final String hrStfStaffname = KnjDbUtils.getString(row, "HR_STF_STAFFNAME");
                        final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                        final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
                        final Student student = new Student(schregno, name, grade, hrClass, hrClassName1, hrStfStaffname, attendno, majorname);
                        studentMap.put(schregno, student);
                    }
                    hrclass._studentList.add(studentMap.get(schregno));
                }
                
                final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
                if (null != chaircd) {
                    final List chairList = getMappedList(subclass._hrChairMap, hrclass._gradehrclass);
                    if (null == Chair.getChair(chaircd, chairList)) {
                        final String chairname = KnjDbUtils.getString(row, "CHAIRNAME");
                        chairList.add(new Chair(chaircd, chairname));
                    }
                }
                
                final Student student = Student.getStudent(schregno, hrclass._studentList);
                
                StudentSubclass studentSubclass = getStudentSubclass(subclassMst, student._subclassMap);
                if (null == studentSubclass) {
                    studentSubclass = new StudentSubclass(subclassMst);
                    student._subclassMap.put(subclassMst.getKeySubclassCd(), studentSubclass);
                    final String credits = KnjDbUtils.getString(row, "CREDITS");
                    if (NumberUtils.isDigits(credits)) {
                        student._creditMstCreditsMap.put(subclassMst.getKeySubclassCd(), Integer.valueOf(credits));
                        getMappedTreeSet(hrclass._hrCreditSetMap, hrclass._gradehrclass).add(Integer.valueOf(credits));
                    }
                }
 
                final String score = KnjDbUtils.getString(row, "SCORE");
                final String valueDi = KnjDbUtils.getString(row, "VALUE_DI");
                if (null != score || null != valueDi) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                    final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                    final String scoreDiv = KnjDbUtils.getString(row, "SCORE_DIV");
                    final String getCredit = KnjDbUtils.getString(row, "GET_CREDIT");
                    final String compCredit = KnjDbUtils.getString(row, "COMP_CREDIT");
                    final String provFlg = KnjDbUtils.getString(row, "PROV_FLG");
                    final SubclassScore subclassScore = new SubclassScore(year, semester, testkindcd, testitemcd, scoreDiv, score, valueDi, getCredit, compCredit, provFlg);
                    studentSubclass._scoreMap.put(subclassScore.getTestcd(), subclassScore);
                }
            }
            
            

//            for (final Iterator it = chairlist.iterator(); it.hasNext();) {
//                final Chair chair = (Chair) it.next();
//                for (final Iterator it2 = chair._hrClassList.iterator(); it2.hasNext();) {
//                    final Hrclass hr = (Hrclass) it2.next();
//                    for (final Iterator it3 = hr._studentList.iterator(); it3.hasNext();) {
//                        final Student st = (Student) it3.next();
//                        for (final Iterator it4 = st._subclassMap.values().iterator(); it4.hasNext();) {
//                            final StudentSubclass ssub = (StudentSubclass) it4.next();
//                            for (final Iterator it5 = ssub._scoreMap.values().iterator(); it5.hasNext();) {
//                                final SubclassScore sscore = (SubclassScore) it5.next();
//                                if (null != sscore._slumpRemark) {
//                                    log.info(st._schregno + " " + sscore._year + sscore._semester + sscore._testkindcd + sscore._testitemcd + " remark = " + sscore._slumpRemark);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            
            param.setChairStaff(db2, subclasslist);
            
            if (null != param._date) {
                try {
                    setAttendance(param, db2, studentMap.values(), paramSubclassMst);
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                } finally {
                    db2.commit();
                }
            }
            return subclasslist;
        }
        
        private static String getRecordSql(final Param param, final SubclassMst subclassMst, final HomeRoom homeRoom) {
            final String[] cds = new String[1 + subclassMst._combinedsubclassList.size()];
            cds[0] = subclassMst.getKeySubclassCd();
            for (int i = 0; i < subclassMst._combinedsubclassList.size(); i++) {
                final SubclassMst gappeisakiSubclassMst = (SubclassMst) subclassMst._combinedsubclassList.get(i);
                cds[1 + i] = gappeisakiSubclassMst.getKeySubclassCd();
            }
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_STD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD ");
            stb.append(" FROM CHAIR_STD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = '" + param._semester + "' AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS = '" + homeRoom._gradehrclass + "' ");
            stb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(true, cds) + " ");
            stb.append(" WHERE T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGDH.HR_CLASS_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     HR_STF.STAFFNAME AS HR_STF_STAFFNAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     TBASE.NAME, ");
            stb.append("     TMAJ.MAJORNAME, ");
            stb.append("     TCRED.CREDITS, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     TCHAIR.CHAIRNAME, ");
            stb.append("     TSCORE.YEAR, ");
            stb.append("     TSCORE.SEMESTER, ");
            stb.append("     TSCORE.TESTKINDCD, ");
            stb.append("     TSCORE.TESTITEMCD, ");
            stb.append("     TSCORE.SCORE_DIV, ");
            stb.append("     TCHAIR.CLASSCD || '-' || TCHAIR.SCHOOL_KIND || '-' || TCHAIR.CURRICULUM_CD || '-' || TCHAIR.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     TSCORE.SCORE, ");
            stb.append("     TSCORE.VALUE_DI, ");
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE TSCORE.GET_CREDIT END AS GET_CREDIT, ");
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE TSCORE.ADD_CREDIT END AS ADD_CREDIT, ");
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE TSCORE.COMP_CREDIT END AS COMP_CREDIT, ");
            stb.append("     T15.PROV_FLG ");
            stb.append(" FROM CHAIR_STD T1 ");
            stb.append(" INNER JOIN CHAIR_DAT TCHAIR ON TCHAIR.YEAR = T1.YEAR ");
            stb.append("     AND TCHAIR.SEMESTER = T1.SEMESTER ");
            stb.append("     AND TCHAIR.CHAIRCD = T1.CHAIRCD ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("     AND GDAT.GRADE = REGD.GRADE ");
            stb.append(" INNER JOIN SCHREG_BASE_MST TBASE ON TBASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" LEFT JOIN MAJOR_MST TMAJ ON TMAJ.COURSECD = REGD.COURSECD ");
            stb.append("     AND TMAJ.MAJORCD = REGD.MAJORCD ");
            stb.append(" LEFT JOIN STAFF_MST HR_STF ON HR_STF.STAFFCD = REGDH.TR_CD1 ");
            stb.append(" LEFT JOIN CREDIT_MST TCRED ON TCRED.YEAR = TCHAIR.YEAR ");
            stb.append("     AND TCRED.COURSECD = REGD.COURSECD ");
            stb.append("     AND TCRED.MAJORCD = REGD.MAJORCD ");
            stb.append("     AND TCRED.GRADE = REGD.GRADE ");
            stb.append("     AND TCRED.COURSECODE = REGD.COURSECODE ");
            stb.append("     AND TCRED.CLASSCD = TCHAIR.CLASSCD ");
            stb.append("     AND TCRED.SCHOOL_KIND = TCHAIR.SCHOOL_KIND ");
            stb.append("     AND TCRED.CURRICULUM_CD = TCHAIR.CURRICULUM_CD ");
            stb.append("     AND TCRED.SUBCLASSCD = TCHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT TSCORE ON TSCORE.YEAR = TCHAIR.YEAR ");
            stb.append("     AND TSCORE.CLASSCD = TCHAIR.CLASSCD ");
            stb.append("     AND TSCORE.SCHOOL_KIND = TCHAIR.SCHOOL_KIND ");
            stb.append("     AND TSCORE.CURRICULUM_CD = TCHAIR.CURRICULUM_CD ");
            stb.append("     AND TSCORE.SUBCLASSCD = TCHAIR.SUBCLASSCD ");
            stb.append("     AND TSCORE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_PROV_FLG_DAT T15 ON T15.YEAR = TSCORE.YEAR ");
            stb.append("     AND T15.CLASSCD = TSCORE.CLASSCD ");
            stb.append("     AND T15.SCHOOL_KIND = TSCORE.SCHOOL_KIND ");
            stb.append("     AND T15.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
            stb.append("     AND T15.SUBCLASSCD = TSCORE.SUBCLASSCD ");
            stb.append("     AND T15.SCHREGNO = TSCORE.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND TCHAIR.CLASSCD <= '90' ");
//            if ("1".equals(param._use_school_detail_gcm_dat)) {
//                stb.append("     AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + param._COURSE_MAJOR + "' ");
//                stb.append("     AND GDAT.SCHOOL_KIND = '" + param._PRINT_SCHOOLKIND + "' ");
//            }

            stb.append(" ORDER BY ");
            stb.append("   TCHAIR.SUBCLASSCD, REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO, T1.CHAIRCD ");
            return stb.toString();
        }
        
        private static void setAttendance(final Param param, final DB2UDB db2, final Collection students, final SubclassMst paramSubclassMst) throws Exception {
            PreparedStatement ps1 = null;
            
            final List subclassList = new ArrayList();
            subclassList.add(paramSubclassMst);
            subclassList.addAll(paramSubclassMst._combinedsubclassList);
            for (int i = 0; i < paramSubclassMst._combinedsubclassList.size(); i++) {
                subclassList.addAll(((SubclassMst) paramSubclassMst._combinedsubclassList.get(i))._attendsubclassList);
            }
            
            final String[] subclasscdArray = new String[subclassList.size()];
            for (int i = 0; i < subclasscdArray.length; i++) {
                subclasscdArray[i] = ((SubclassMst) subclassList.get(i)).getKeySubclassCd();
            }

            param._attendParamMap.put("schregno", "?");
            param._attendParamMap.put("subclasscdArray", subclasscdArray);
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    param._ctrlYear,
                    "9",
                    param._sdate,
                    param._date,
                    param._attendParamMap
            );
            ps1 = db2.prepareStatement(sql);
            
            log.info(" set attendance. students size = " + students.size());
            
            for (final Iterator srit = students.iterator(); srit.hasNext();) {
                final Student student = (Student) srit.next();
                
                for (final Iterator stit = KnjDbUtils.query(db2, ps1, new Object[] {student._schregno}).iterator(); stit.hasNext();) {
                    final Map row = (Map) stit.next();
                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    
                    //log.info(" subclassCd = " + subclassCd);

                    final SubclassMst subclassMst = (SubclassMst) param._subclassMstMap.get(subclassCd);
                    if (null == subclassMst) {
                        continue;
                    }
                    
                    final String lesson = KnjDbUtils.getString(row, "LESSON");
                    final String must = KnjDbUtils.getString(row, "MLESSON");
                    String sick = subclassMst._isSaki ? KnjDbUtils.getString(row, "REPLACED_SICK") : KnjDbUtils.getString(row, "SICK2");
                    StudentSubclass studentSubclass = student.createStudentSubclass(subclassMst);
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    
                    studentSubclass._koketsuMap.put(semester, KnjDbUtils.getString(row, "ABSENT"));
                    studentSubclass._kibikiMap.put(semester, KnjDbUtils.getString(row, "MOURNING"));
                    studentSubclass._shutteiMap.put(semester, add(add(KnjDbUtils.getString(row, "SUSPEND"), KnjDbUtils.getString(row, "VIRUS")), KnjDbUtils.getString(row, "KOUDOME")));
                    studentSubclass._jugyoJisuMap.put(semester, lesson);
                    studentSubclass._mustMap.put(semester, must);
                    
                    if (ALL_SEME.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        final String lateEarly2 = add(KnjDbUtils.getString(row, "LATE2"), KnjDbUtils.getString(row, "EARLY2"));
                        // 1捨2入
                        if (NumberUtils.isNumber(lateEarly2) && Double.parseDouble(lateEarly2) > 1.0) {
                            sick = add(sick, "1");
                        }
                    }
                    
                    studentSubclass._sick2Map.put(semester, sick);
                }
            }
            log.info(" set attendance done.");
            DbUtils.closeQuietly(ps1);        
            db2.commit();
        }
    }
    
    private static String add(String v1, String v2) {
        if (!NumberUtils.isNumber(v1)) return v2;
        if (!NumberUtils.isNumber(v2)) return v1;
        return new BigDecimal(v1).add(new BigDecimal(v2)).toString();
    }
    
    private static String subtract(String v1, String v2) {
        if (!NumberUtils.isNumber(v2)) return v1;
        if (!NumberUtils.isNumber(v1)) {
            v1 = "0";
        }
        return new BigDecimal(v1).subtract(new BigDecimal(v2)).toString();
    }
    
    private static class Chair {
        final String _chaircd;
        final String _chairname;
        final List _chairStfNameList;
        public Chair(String chaircd, String chairname) {
            _chaircd = chaircd;
            _chairname = chairname;
            _chairStfNameList = new ArrayList();
        }

        public static Chair getChair(final String chaircd, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                if (chair._chaircd.equals(chaircd)) {
                    return chair;
                }
            }
            return null;
        }
        
        public static List getStaffnameList(final List list) {
            final List chairStfNameList = new ArrayList();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                for (final Iterator stfit = chair._chairStfNameList.iterator(); stfit.hasNext();) {
                    final String stfName = (String) stfit.next();
                    if (!chairStfNameList.contains(stfName)) {
                        chairStfNameList.add(stfName);
                    }
                }
            }
            return chairStfNameList;
        }
    }
    
    private static class HomeRoom {
        final String _gradehrclass;
        final String _schoolKind;
        final String _gradeCd;
        final String _hrClassName1;
        final List _hrStfStfName;
        final List _studentList;
        final Map _hrCreditSetMap = new HashMap();

        public HomeRoom(final String gradehrclass, final String schoolKind, final String gradeCd, final String hrClassName1, final List hrStfStfName) {
            _gradehrclass = gradehrclass;
            _schoolKind = schoolKind;
            _gradeCd = gradeCd;
            _hrClassName1 = hrClassName1;
            _hrStfStfName = hrStfStfName;
            _studentList = new ArrayList();
        }
        
        public HomeRoom copy() {
            return new HomeRoom(_gradehrclass, _schoolKind, _gradeCd, _hrClassName1, _hrStfStfName);
        }

        public static HomeRoom query(final DB2UDB db2, final Param param, final String gradehrclass) {
            String sql = "";
            sql += " SELECT T1.GRADE, T1.HR_CLASS, T2.GRADE_CD, T1.HR_CLASS_NAME1, T2.SCHOOL_KIND ";
            sql += "      , TSTF1.STAFFNAME AS STAFFNAME1 ";
            sql += "      , TSTF2.STAFFNAME AS STAFFNAME2 ";
            sql += "      , TSTF3.STAFFNAME AS STAFFNAME3 ";
            sql += "      , TSTF4.STAFFNAME AS STAFFNAME4 ";
            sql += "      , TSTF5.STAFFNAME AS STAFFNAME5 ";
            sql += "      , TSTF6.STAFFNAME AS STAFFNAME6 ";
            sql += " FROM SCHREG_REGD_HDAT T1 ";
            sql += " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            sql += "   AND T2.GRADE = T1.GRADE ";
            sql += " LEFT JOIN STAFF_MST TSTF1 ON TSTF1.STAFFCD = T1.TR_CD1 ";
            sql += " LEFT JOIN STAFF_MST TSTF2 ON TSTF2.STAFFCD = T1.TR_CD2 ";
            sql += " LEFT JOIN STAFF_MST TSTF3 ON TSTF3.STAFFCD = T1.TR_CD3 ";
            sql += " LEFT JOIN STAFF_MST TSTF4 ON TSTF4.STAFFCD = T1.SUBTR_CD1 ";
            sql += " LEFT JOIN STAFF_MST TSTF5 ON TSTF5.STAFFCD = T1.SUBTR_CD2 ";
            sql += " LEFT JOIN STAFF_MST TSTF6 ON TSTF6.STAFFCD = T1.SUBTR_CD3 ";
            sql += " WHERE T1.YEAR = '" + param._ctrlYear + "' ";
            sql += "   AND T1.SEMESTER = (SELECT MAX(SEMESTER) ";
            sql += "                   FROM SCHREG_REGD_HDAT ";
            sql += "                   WHERE YEAR = '" + param._ctrlYear + "' ";
            sql += "                     AND GRADE || '-' || HR_CLASS = '" + gradehrclass + "' ";
            sql += "                  ) ";
            sql += "   AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradehrclass + "' ";
            
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            
            final List staff = new ArrayList();
            if (null != KnjDbUtils.getString(row, "STAFFNAME1")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME1")); }
//            if (null != KnjDbUtils.getString(row, "STAFFNAME2")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME2")); }
//            if (null != KnjDbUtils.getString(row, "STAFFNAME3")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME3")); }
//            if (null != KnjDbUtils.getString(row, "STAFFNAME4")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME4")); }
//            if (null != KnjDbUtils.getString(row, "STAFFNAME5")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME5")); }
//            if (null != KnjDbUtils.getString(row, "STAFFNAME6")) { staff.add(KnjDbUtils.getString(row, "STAFFNAME6")); }
            
            return new HomeRoom(gradehrclass, KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "GRADE_CD"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), staff);
        }
        
        public static HomeRoom getHrclass(final String gradehrclass, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final HomeRoom hrclass = (HomeRoom) it.next();
                if (hrclass._gradehrclass.equals(gradehrclass)) {
                    return hrclass;
                }
            }
            return null;
        }
    }
    
    private static class StudentSubclass {
        final SubclassMst _subclassMst;
        final Map _scoreMap;
        final Map _jugyoJisuMap;
        final Map _koketsuMap;
        final Map _kibikiMap;
        final Map _shutteiMap;
        final Map _mustMap;
        final Map _sick2Map;

        StudentSubclass(
            final SubclassMst subclassMst
        ) {
            _subclassMst = subclassMst;
            _scoreMap = new HashMap();
            _koketsuMap = new HashMap();
            _kibikiMap = new HashMap();
            _shutteiMap = new HashMap();
            _jugyoJisuMap = new HashMap();
            _mustMap = new HashMap();
            _sick2Map = new HashMap();
        }
    }
    
    private static class SubclassScore {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _score;
        final String _valueDi;
        final String _getCredit;
        final String _compCredit;
        final String _provFlg;
        
        SubclassScore(
            final String year,
            final String semester,
            final String testkindcd,
            final String testitemcd,
            final String scoreDiv,
            final String score,
            final String valueDi,
            final String getCredit,
            final String compCredit,
            final String provFlg
        ) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _score = score;
            _valueDi = valueDi;
            _getCredit = getCredit;
            _compCredit = compCredit;
            _provFlg = provFlg;
        }

        public String getTestcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "SubclassScore(" + getTestcd() + ": "+ _score + ")";
        }
    }
    
    private static StudentSubclass getStudentSubclass(final SubclassMst subclass, Map subclassMap) {
        return (StudentSubclass) subclassMap.get(subclass.getKeySubclassCd());
    }
    
    private static class Semester {
        final String _year;
        final String _cdSemester;
        final String _semestername;
        final String _sdate;
        final String _edate;
        final List _testItemList;
        public Semester(String year, String semester, String semestername, final String sdate, final String edate) {
            _year = year;
            _cdSemester = semester;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
    }
    
    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
        }
        public String getTestcd() {
            return _semester._cdSemester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._cdSemester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57998 $");
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _ctrlDate;
        String _sdate;
        String _date;
        final String _printDiv; // 1:クラス別 2:科目別
        final String[] _hrClasses;
        final String[] _subclasscds;
        final String _useCurriculumcd;
        final String _use_SchregNo_hyoji;
        final String _COURSE_MAJOR;
        final String _PRINT_SCHOOLCD;
        final String _PRINT_SCHOOLKIND;
        final String _useSchool_KindField;
//        final String _use_school_detail_gcm_dat;

        final List _semesterList;
        final Map _subclassMstMap;
//        final String _now;
        private KNJSchoolMst _knjSchoolMst;
        final Map _attendParamMap;
        final Map _schoolKindSchoolMstSchoolname1Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = null == request.getParameter("DATE") ? null : KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _printDiv = request.getParameter("PRINT_DIV");
            if ("1".equals(_printDiv)) {
                _hrClasses = new String[] {request.getParameter("HR_CLASS")};
                _subclasscds = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                _subclasscds = new String[] {request.getParameter("SUBCLASS")};
                _hrClasses = request.getParameterValues("CATEGORY_SELECTED");
            }
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            
            _COURSE_MAJOR = request.getParameter("COURSE_MAJOR");
            _PRINT_SCHOOLCD = request.getParameter("SCHOOLCD");
            _PRINT_SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
//            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");

            _subclassMstMap = getSubclassMap(db2, _ctrlYear);
            _semesterList = getSemesterList(db2);
            if (null == _sdate) {
                for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                    final Semester s = (Semester) it.next();
                    if ("1".equals(s._cdSemester) || "9".equals(s._cdSemester)) {
                        _sdate = s._sdate;
                    }
                    if ("9".equals(s._cdSemester)) {
                        _date = s._edate;
                    }
                }
            }
            log.info(" sdate = " + _sdate + ", edate = " + _date);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
//            if ("1".equals(_use_school_detail_gcm_dat)) {
//                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
//            } else {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
//            }
            
            final String z010 = getZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            _schoolKindSchoolMstSchoolname1Map = getSchoolKindSchoolMstSchoolname1Map(db2);
        }
        
        private void setChairStaff(final DB2UDB db2, final List subclasslist) {
            String sql = "";
            sql += " SELECT DISTINCT T1.STAFFCD, STAFFNAME FROM CHAIR_STF_DAT T1 ";
            sql += " INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ";
            sql += " WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER <= '" + _semester + "' AND CHAIRCD = ? AND CHARGEDIV = 1 ";
            sql += " ORDER BY T1.STAFFCD ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                for (final Iterator it = subclasslist.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    for (final Iterator hrit = subclass._hrChairMap.keySet().iterator(); hrit.hasNext();) {
                        final String gradeHrclass = (String) hrit.next();
                        
                        for (final Iterator cit = getMappedList(subclass._hrChairMap, gradeHrclass).iterator(); cit.hasNext();) {
                            final Chair chair = (Chair) cit.next();
                            chair._chairStfNameList.clear();
                            ps.setString(1, chair._chaircd);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                if (StringUtils.isEmpty(rs.getString("STAFFNAME")) || chair._chairStfNameList.contains(rs.getString("STAFFNAME"))) {
                                    continue;
                                }
                                chair._chairStfNameList.add(rs.getString("STAFFNAME"));
                            }
                            DbUtils.closeQuietly(rs);
                        }
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getSchoolKindSchoolMstSchoolname1Map(DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND, SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' "), "SCHOOL_KIND", "SCHOOLNAME1");
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String getZ010Name1(DB2UDB db2) {
            String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            return name1;
        }
        
        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' ORDER BY SEMESTER ";
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                final String semestername = KnjDbUtils.getString(row, "SEMESTERNAME");
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                final String edate = KnjDbUtils.getString(row, "EDATE");
                Semester semes = new Semester(year, semester, semestername, sdate, edate);
                list.add(semes);
            }
            return list;
        }
        
        private Map getSubclassMap(final DB2UDB db2, final String paramYear) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH REPLACE AS ( ");
            sql.append(" SELECT DISTINCT 'COMBINED' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
            sql.append(" UNION ");
            sql.append(" SELECT DISTINCT 'ATTEND' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append(" T1.*, ");
            sql.append(" CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
            sql.append(" CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ");
            sql.append(" L3.ATTEND_CLASSCD || '-' || L3.ATTEND_SCHOOL_KIND || '-' || L3.ATTEND_CURRICULUM_CD || '-' || L3.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            sql.append(" L4.COMBINED_CLASSCD || '-' || L4.COMBINED_SCHOOL_KIND || '-' || L4.COMBINED_CURRICULUM_CD || '-' || L4.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            sql.append(" FROM SUBCLASS_MST T1 ");
            sql.append(" LEFT JOIN REPLACE L1 ON L1.DIV = 'COMBINED' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
            sql.append(" LEFT JOIN REPLACE L2 ON L2.DIV = 'ATTEND' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
            sql.append(" LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT L3 ON L3.YEAR = '" + paramYear + "' ");
            sql.append("   AND L3.COMBINED_CLASSCD || '-' || L3.COMBINED_SCHOOL_KIND || '-' || L3.COMBINED_CURRICULUM_CD || '-' || L3.COMBINED_SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            sql.append(" LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT L4 ON L4.YEAR = '" + paramYear + "' ");
            sql.append("   AND L4.ATTEND_CLASSCD || '-' || L4.ATTEND_SCHOOL_KIND || '-' || L4.ATTEND_CURRICULUM_CD || '-' || L4.ATTEND_SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            sql.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

            final Map attendsubclasscdListMap = new HashMap();
            final Map combinedsubclasscdListMap = new HashMap();
            final Map subclassMap = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String key = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                if (null == subclassMap.get(key)) {
                    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                    final boolean isSaki = "1".equals(KnjDbUtils.getString(row, "IS_SAKI"));
                    final boolean isMoto = "1".equals(KnjDbUtils.getString(row, "IS_MOTO"));
                    SubclassMst subclass = new SubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, isSaki, isMoto);
                    subclassMap.put(key, subclass);
                }
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");
                if (null != attendSubclasscd) {
                    getMappedList(attendsubclasscdListMap, key).add(attendSubclasscd);
                }
                final String combiendSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                if (null != combiendSubclasscd) {
                    getMappedList(combinedsubclasscdListMap, key).add(combiendSubclasscd);
                }
            }
            for (final Iterator it = attendsubclasscdListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next(); 
                final String combinedSubclasscd = (String) e.getKey();
                final List attendsubclasscdList = (List) e.getValue();
                final SubclassMst combinedSubclassMst = (SubclassMst)subclassMap.get(combinedSubclasscd);
                for (final Iterator sit = attendsubclasscdList.iterator(); sit.hasNext();) {
                    final String attendsubclasscd = (String) sit.next();
                    final SubclassMst attendsubclassMst = (SubclassMst)subclassMap.get(attendsubclasscd);
                    if (null == attendsubclassMst) {
                        log.warn("元科目がない:" + attendsubclasscd);
                    } else if (null != combinedSubclassMst) {
                        combinedSubclassMst._attendsubclassList.add(attendsubclassMst);
                        Collections.sort(combinedSubclassMst._attendsubclassList);
                    }
                }
            }
            for (final Iterator it = combinedsubclasscdListMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next(); 
                final String attendSubclasscd = (String) e.getKey();
                final List combinedsubclasscdList = (List) e.getValue();
                final SubclassMst attendSubclassMst = (SubclassMst)subclassMap.get(attendSubclasscd);
                for (final Iterator sit = combinedsubclasscdList.iterator(); sit.hasNext();) {
                    final String combinedsubclasscd = (String) sit.next();
                    final SubclassMst combinedsubclassMst = (SubclassMst)subclassMap.get(combinedsubclasscd);
                    if (null == combinedsubclassMst) {
                        log.warn("先科目がない:" + combinedsubclasscd);
                    } else if (null != attendSubclassMst) {
                        attendSubclassMst._combinedsubclassList.add(combinedsubclassMst);
                        Collections.sort(attendSubclassMst._combinedsubclassList);
                    }
                }
            }
            
            
            return subclassMap;
        }
    }
}

// eof

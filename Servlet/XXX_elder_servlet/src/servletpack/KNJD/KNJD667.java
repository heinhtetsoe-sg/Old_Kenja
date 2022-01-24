/*
 * $Id: 5970f68e929d997b75a3d4806f8a7e40f556fe46 $
 *
 * 作成日: 2015/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 文京学園　得点成績一覧表（科目別）
 */
public class KNJD667 {

    private static final Log log = LogFactory.getLog(KNJD667.class);

    private boolean _hasData;

    private Param _param;

    private String _101010_ = "1010108";
    private String _102010_ = "1020108";
    private String _201010_ = "2010108";
    private String _201020_ = "2010208";
    private String _202010_ = "2020108";
    private final String _9990008 = "9990008";

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
            
            if ("J".equals(_param._schoolKind)) {
                _101010_ = "1010101";
                _102010_ = "1020101";
                _201010_ = "2010101";
                _201020_ = "2010201";
                _202010_ = "2020101";
            }

            _hasData = false;

            printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        final int MAX_STUDENT = 50;
        
        final String[] semtestcds = {_101010_, _102010_, _201010_, _201020_, _202010_, _9990008};
        final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param, false);
        final Map avgDatRuikeiMap = RecordAverageDat.getRecordAverageDatMap(db2, _param, true);
        
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup courseGroup = (CourseGroup) courseGroupList.get(cgi);
            Collections.sort(courseGroup._subclassList);

            // 科目
            for (int subi = 0; subi < courseGroup._subclassList.size(); subi++) {
                final Subclass subclass = (Subclass) courseGroup._subclassList.get(subi);
                Student.Comparator comparator = new Student.Comparator(_param, subclass);
                
                log.debug(" subclass = " + subclass._subclasscd);
                
                final List avgAvgTargetList = new ArrayList();
                final List studentAllList = new ArrayList();
                for (final Iterator it = getMappedList(courseGroup._subclassSchregnoListMap, subclass._subclasscd).iterator(); it.hasNext();) {
                    final String schregno = (String) it.next();
                    final Student student = (Student) courseGroup._studentMap.get(schregno);
                    studentAllList.add(student);
                }
                Collections.sort(studentAllList, comparator);
                final List studentListList = getGroupList(studentAllList, MAX_STUDENT);
                for (int sli = 0; sli < studentListList.size(); sli++) {
                    
                    final List studentList = (List) studentListList.get(sli);

                    final String form = "KNJD667.frm";
                    svf.VrSetForm(form, 1);
                    
                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　科目別成績順一覧表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"); // タイトル
                    svf.VrsOut("GRADE_NAME", courseGroup._gradeName1); // 学年名
                    svf.VrsOut("GROUP_NAME", courseGroup._groupName); // コースグループ名
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
                    //svf.VrsOut("PAGE", null); // ページ
                    svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目
                    svf.VrsOut("SUBCLASSNAME", subclass._subclassname); // 科目名
                    
                    final RecordAverageDat avgDat0 = RecordAverageDat.get(avgDatMap, _param._semester + _param._testcd, RecordAverageDat.getCourseGroupAvgDivKey(_param._grade, courseGroup._groupCd), subclass._subclasscd);
                    if (null != avgDat0) {
                        svf.VrsOut("SCH_COUNT", avgDat0._count); // 人数
                        svf.VrsOut("SUBCLASS_AVG", sishagonyu(avgDat0._avg)); // 科目平均点
                    }

//                    for (int tii = 0; tii < semtestcds.length; tii++) {
//                        final Testitem testitem = (Testitem) Testitem.getTestItem(semtestcds[tii], _param._testitemList);
//                        if (null != testitem) {
//                        }
//                    }
                    
                    // 生徒
                    BigDecimal kekkaTotal = new BigDecimal(0);
                    for (int si = 0; si < studentList.size(); si++) {
                        final Student student = (Student) studentList.get(si);
                        
                        final String chairStaffname = student.getChairStaffname(subclass._subclasscd);
                        
                        final int line = si + 1;
                        final String mark = student.getMark(_param._semester + _param._testcd, subclass._subclasscd);
                        svf.VrsOutn("FAILED", line, "◎".equals(mark) || "●".equals(mark) ? mark : ""); // 赤点者
                        svf.VrsOutn("ATTENTION", line, "▲".equals(mark) ? mark : ""); // 注意者
                        svf.VrsOutn("KUMI", line, student._hrClassName1); // 組
                        svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 出席番号
                        svf.VrsOutn("NAME" + (getMS932ByteLength(student._name) > 20 ? "_2" : ""), line, student._name); // 氏名
                        svf.VrsOutn("STAFFNAME" + (getMS932ByteLength(chairStaffname) > 20 ? "_2" : ""), line, chairStaffname); // 担当教員名
                        final BigDecimal kekka = (BigDecimal) student._kekkaMap.get(subclass._subclasscd);
                        if (null != kekka) {
                            svf.VrsOutn("KESSEKI", line, kekka.toString()); // 欠席時数
                            kekkaTotal = kekkaTotal.add(kekka);
                        }
                        
                        for (int tii = 0; tii < semtestcds.length; tii++) {
                            final Score s = (Score) getMappedMap(student._scoreMap, semtestcds[tii]).get(subclass._subclasscd);
                            //log.debug(" schregno = " + student._schregno + " // " + semtestcds[tii] + " // " + getMappedMap(student._scoreMap, semtestcds[tii]));
                            if (null != s) {
                                String rank = "*".equals(s._score) ? null : s._majorRank;
                                if (_101010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_10101", line, s._score); // 得点（前期・中間）
                                    svf.VrsOutn("RANK_10101", line, rank); // 席次（前期・中間）
                                } else if (_102010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_10201", line, s._score); // 得点（前期・期末）
                                    svf.VrsOutn("RANK_10201", line, rank); // 席次（前期・期末）
                                } else if (_201010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20101", line, s._score); // 得点（後期・中間１）
                                    svf.VrsOutn("RANK_20101", line, rank); // 席次（後期・中間１）
                                } else if (_201020_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20102", line, s._score); // 得点（後期・中間２）
                                    svf.VrsOutn("RANK_20102", line, rank); // 席次（後期・中間２）
                                } else if (_202010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20201", line, s._score); // 得点（後期・期末）
                                    svf.VrsOutn("RANK_20201", line, rank);
                                }
                                if ((_param._semester + _param._testcd).equals(semtestcds[tii])) {
                                    if ("J".equals(_param._schoolKind)) {
                                        svf.VrsOutn("SCORE_99900", line, s._score); // 総合点
                                        svf.VrsOutn("AVG_99900", line, sishagonyu(s._avg)); // 平均点
                                        svf.VrsOutn("RANK_99900", line, rank);
                                        if (null != s._avg) {
                                            avgAvgTargetList.add(s._avg);
                                        }
                                    } else {
                                        svf.VrsOutn("SCORE_99900", line, s._scoreRuikei); // 総合点
                                        final int count = Testitem.getTestCount(_param._semester + _param._testcd, semtestcds, student._scoreMap, subclass._subclasscd);
                                        svf.VrsOutn("RANK_99900", line, s._majorRankRuikei);
                                        if (0 < count && NumberUtils.isDigits(s._scoreRuikei)) {
                                            final BigDecimal bd = new BigDecimal(s._scoreRuikei).divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP);
                                            log.info("score = " + s._scoreRuikei + ", count = " + count + ", bd = " + bd);
                                            svf.VrsOutn("AVG_99900", line, sishagonyu(bd)); // 平均点 テーブルにないので帳票で算出する
                                            avgAvgTargetList.add(bd);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (sli == studentListList.size() - 1) {
                        final int line = MAX_STUDENT + 1;
                        svf.VrsOutn("STAFFNAME", line, "平　均　点"); // 平均点表示

                        for (int tii = 0; tii < semtestcds.length; tii++) {
                            final RecordAverageDat avgDat1 = RecordAverageDat.get(avgDatMap, semtestcds[tii], RecordAverageDat.getCourseGroupAvgDivKey(_param._grade, courseGroup._groupCd), subclass._subclasscd);
                            if (null != avgDat1) {
                                final String avg = sishagonyu(avgDat1._avg);
                                if (_101010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_10101", line, avg); // 得点（前期・中間）
                                } else if (_102010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_10201", line, avg); // 得点（前期・期末）
                                } else if (_201010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20101", line, avg); // 得点（後期・中間１）
                                } else if (_201020_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20102", line, avg); // 得点（後期・中間２）
                                } else if (_202010_.equals(semtestcds[tii])) {
                                    svf.VrsOutn("SCORE_20201", line, avg); // 得点（後期・期末）
                                }
                                if ((_param._semester + _param._testcd).equals(semtestcds[tii])) {
                                    if ("J".equals(_param._schoolKind)) {
                                        svf.VrsOutn("SCORE_99900", line, avg); // 総合点
                                    } else {
                                        final RecordAverageDat avgDatR = RecordAverageDat.get(avgDatRuikeiMap, semtestcds[tii], RecordAverageDat.getCourseGroupAvgDivKey(_param._grade, courseGroup._groupCd), subclass._subclasscd);
                                        if (null != avgDatR) {
                                            svf.VrsOutn("SCORE_99900", line, sishagonyu(avgDatR._avg)); // 総合点
                                        }
                                    }
                                    svf.VrsOutn("AVG_99900", line, avg(avgAvgTargetList)); // 平均点 テーブルにないので帳票で算出する
                                }
                            }
                        }
                        svf.VrsOutn("KESSEKI", line, kekkaTotal.toString()); // 欠席時数
                    }
                    
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }
    
    private List getGroupList(final List list, final int max) {
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
    
    private static String sishagonyu(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static String avg(final List scoreBdList) {
        if (scoreBdList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = scoreBdList.iterator(); it.hasNext();) {
            final BigDecimal scoreBd = (BigDecimal) it.next();
            if (null != scoreBd) {
                sum = sum.add(scoreBd);
                count += 1;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Score {
        final String _subclasscd;
        String _score;
        final BigDecimal _avg;
        final String _majorRank;
        final Integer _scoreLine;
        final Integer _assesslevel;
        String _scoreRuikei;
        final BigDecimal _avgRuikei;
        final String _majorRankRuikei;

        Score(
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final String majorRank,
                final Integer scoreLine,
                final Integer assesslevel,
                final String scoreRuikei,
                final BigDecimal avgRuikei,
                final String majorRankRuikei) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _majorRank = majorRank;
            _scoreLine = scoreLine;
            _assesslevel = assesslevel;
            _scoreRuikei = scoreRuikei;
            _avgRuikei = avgRuikei;
            _majorRankRuikei = majorRankRuikei;
        }
        public String toString() {
            return "{subclasscd : " + _subclasscd + ", score : " + _score + "}";
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _requireFlg;
        public Subclass(final String subclasscd, final String subclassname, final String subclassabbv, final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _requireFlg = requireFlg;
        }
        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
            String requireFlg1 = StringUtils.defaultString(_requireFlg, "0");
            String requireFlg2 = StringUtils.defaultString(subclass._requireFlg, "0");
            if (!"3".equals(requireFlg1) && "3".equals(requireFlg2)) {
                return -1;
            } else if ("3".equals(requireFlg1) && !"3".equals(requireFlg2)) { // 選択科目は後
                return 1;
            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }
        private static Subclass getSubclass(final String subclasscd, final List subclassList) {
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclasscd.equals(subclass._subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }
    }

    private static class Student {

        private static class Comparator implements java.util.Comparator {
            private static Integer rankMax = new Integer(999999);
            Param _param;
            Subclass _subclass;
            Comparator(final Param param, final Subclass subclass) {
                _param = param;
                _subclass = subclass;
            }

            public int compare(final Object o1, final Object o2) {
                Student student1 = (Student) o1;
                Student student2 = (Student) o2;
                if (null == student2) {
                    return -1;
                } else if (null == student1) {
                    return 1;
                }
                if ("2".equals(_param._ranking)) {
                    Integer rank1 = null;
                    Integer rank2 = null;
                    final Score score1 = (Score) getMappedMap(student1._scoreMap, _param._semester + _param._testcd).get(_subclass._subclasscd);
                    if (null != score1 && NumberUtils.isDigits("J".equals(_param._schoolKind) ? score1._majorRank : score1._majorRankRuikei)) {
                        rank1 = Integer.valueOf("J".equals(_param._schoolKind) ? score1._majorRank : score1._majorRankRuikei);
                    } else {
                        rank1 = rankMax;
                    }
                    final Score score2 = (Score) getMappedMap(student2._scoreMap, _param._semester + _param._testcd).get(_subclass._subclasscd);
                    if (null != score2 && NumberUtils.isDigits("J".equals(_param._schoolKind) ? score2._majorRank : score2._majorRankRuikei)) {
                        rank2 = Integer.valueOf("J".equals(_param._schoolKind) ? score2._majorRank : score2._majorRankRuikei);
                    } else {
                        rank2 = rankMax;
                    }
                    int cmp = rank1.compareTo(rank2);
                    if (0 != cmp) {
                        return cmp;
                    }
                }
                return (student1._grade + student1._hrClass + student1._attendno).compareTo(student2._grade + student2._hrClass + student2._attendno);
            }
            
        }
        
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _scoreMap = new HashMap();
        final Map _chairStfMap = new HashMap();
        final Map _kekkaMap = new HashMap();
        final List _kekkaOverSubclasscdList = new ArrayList();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrClassName1,
            final String attendno,
            final String schregno,
            final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
        
        public String getChairStaffname(final String subclasscd) {
            final Map chaircdChairStaffMap = (Map) getMappedMap(_chairStfMap, subclasscd);
            if (chaircdChairStaffMap.isEmpty()) {
                return null;
            }
            final Object minChaircd = new TreeMap(chaircdChairStaffMap).firstKey();
            final Map chairStaffMap = (Map) chaircdChairStaffMap.get(minChaircd);
            if (null == chairStaffMap || chairStaffMap.isEmpty()) {
                return null;
            }
            final Object minChairStaffcd = new TreeMap(chairStaffMap).firstKey();
            return (String) chairStaffMap.get(minChairStaffcd);

        }
        
        public String getMark(final String semtestcd, final String subclasscd) {
            boolean isAkaten = false;
            boolean isTyui = false;
            Score s = (Score) getMappedMap(_scoreMap, semtestcd).get(subclasscd);
            if (null != s) {
                if (null != s._assesslevel && s._assesslevel.intValue() == 1) {
                    isAkaten = true;
                }
                if (null == s._assesslevel && NumberUtils.isDigits(s._score) && null != s._scoreLine && Integer.parseInt(s._score) < s._scoreLine.intValue()) {
                    // 得点が、ライン点未満
                    isAkaten = true;
                }
                if (!isAkaten && null != s._assesslevel && s._assesslevel.intValue() == 2) {
                    isTyui = true;
                }
            }
            final boolean isKetujiOver = _kekkaOverSubclasscdList.contains(subclasscd);

            final String mark;
            if (isAkaten) {
                if (isKetujiOver) {
                    // 赤点かつ欠時オーバー
                    mark = "◎";
                } else {
                    // 赤点
                    mark = "●";
                }
            } else if (isTyui) {
                // 注意
                mark = "▲";
            } else if (isKetujiOver) {
                // 欠時オーバー
                mark = "欠";
            } else {
                mark = null;
            }
            return mark;
        }

        private static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }
    }
    
    private static class CourseGroup {
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _subclassList;
        final Map _studentMap = new HashMap();
        final Map _subclassSchregnoListMap;

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _subclassList = new ArrayList();
            _subclassSchregnoListMap = new HashMap();
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   GDAT.GRADE_NAME1, ");
            stb.append("   CGRP.GROUP_CD, ");
            stb.append("   CGRPH.GROUP_NAME, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   HDAT.HR_NAME, ");
            stb.append("   HDAT.HR_CLASS_NAME1, ");
            stb.append("   HRSTF.STAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   T2.CHAIRCD, ");
            stb.append("   CSTFM.STAFFCD AS CHAIR_STAFFCD, ");
            stb.append("   CSTFM.STAFFNAME AS CHAIR_STAFFNAME, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   CRE.REQUIRE_FLG, ");
            stb.append("   TREC.VALUE_DI, ");
            stb.append("   TREC.SEMESTER || TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS SEM_TESTCD, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TRANK.AVG, ");
            stb.append("   TRANK.MAJOR_RANK, ");
            stb.append("   TRUI.SCORE AS SCORE_RUIKEI, ");
            stb.append("   TRUI.AVG AS AVG_RUIKEI, ");
            stb.append("   TRUI.MAJOR_RANK AS MAJOR_RANK_RUIKEI, ");
            stb.append("   TPS.SCORE_LINE, ");
            stb.append("   ASLV.ASSESSLEVEL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("     AND GDAT.GRADE = T1.GRADE ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("     AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("     AND HDAT.GRADE = T1.GRADE ");
            stb.append("     AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
            stb.append(" INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
            stb.append("     AND CGRP.GRADE = T1.GRADE ");
            stb.append("     AND CGRP.COURSECD = T1.COURSECD ");
            stb.append("     AND CGRP.MAJORCD = T1.MAJORCD ");
            stb.append("     AND CGRP.COURSECODE = T1.COURSECODE ");
            stb.append(" INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
            stb.append("     AND CGRPH.GRADE = CGRP.GRADE ");
            stb.append("     AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
            stb.append(" INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append(" LEFT JOIN CHAIR_STF_DAT CSTF ON CSTF.YEAR = T3.YEAR ");
            stb.append("     AND CSTF.SEMESTER = T3.SEMESTER ");
            stb.append("     AND CSTF.CHAIRCD = T3.CHAIRCD ");
            stb.append("     AND CSTF.CHARGEDIV = 1 ");
            stb.append(" LEFT JOIN STAFF_MST CSTFM ON CSTFM.STAFFCD = CSTF.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
            stb.append("     AND CRE.COURSECD = T1.COURSECD ");
            stb.append("     AND CRE.MAJORCD = T1.MAJORCD ");
            stb.append("     AND CRE.GRADE = T1.GRADE ");
            stb.append("     AND CRE.COURSECODE = T1.COURSECODE ");
            stb.append("     AND CRE.CLASSCD = T3.CLASSCD ");
            stb.append("     AND CRE.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND CRE.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND CRE.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
            stb.append("     AND TREC.SEMESTER || TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV <= '" + (param._semester + param._testcd) + "' ");
            stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
            stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = TREC.YEAR ");
            stb.append("     AND TRANK.SEMESTER = TREC.SEMESTER ");
            stb.append("     AND TRANK.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("     AND TRANK.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("     AND TRANK.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("     AND TRANK.CLASSCD = TREC.CLASSCD ");
            stb.append("     AND TRANK.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("     AND TRANK.CURRICULUM_CD = TREC.CURRICULUM_CD ");
            stb.append("     AND TRANK.SUBCLASSCD = TREC.SUBCLASSCD ");
            stb.append("     AND TRANK.SCHREGNO = TREC.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_RUIKEI_SDIV_DAT TRUI ON TRUI.YEAR = TREC.YEAR ");
            stb.append("     AND TRUI.SEMESTER = TREC.SEMESTER ");
            stb.append("     AND TRUI.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("     AND TRUI.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("     AND TRUI.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("     AND TRUI.CLASSCD = TREC.CLASSCD ");
            stb.append("     AND TRUI.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("     AND TRUI.CURRICULUM_CD = TREC.CURRICULUM_CD ");
            stb.append("     AND TRUI.SUBCLASSCD = TREC.SUBCLASSCD ");
            stb.append("     AND TRUI.SCHREGNO = TREC.SCHREGNO ");
            stb.append(" LEFT JOIN PASS_SCORE_DAT TPS ON TPS.YEAR = TREC.YEAR ");
            stb.append("     AND TPS.SEMESTER = TREC.SEMESTER ");
            stb.append("     AND TPS.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("     AND TPS.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("     AND TPS.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("     AND TPS.RUISEKI_DIV = '1' ");
            stb.append("     AND TPS.CLASSCD = TREC.CLASSCD ");
            stb.append("     AND TPS.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("     AND TPS.CURRICULUM_CD = TREC.CURRICULUM_CD ");
            stb.append("     AND TPS.SUBCLASSCD = TREC.SUBCLASSCD ");
            stb.append("     AND TPS.PASS_DIV = '5' ");
            stb.append("     AND TPS.GRADE = T1.GRADE ");
            stb.append("     AND TPS.HR_CLASS = '000' ");
            stb.append("     AND TPS.COURSECD = '0' ");
            stb.append("     AND TPS.MAJORCD = CGRP.GROUP_CD ");
            stb.append("     AND TPS.COURSECODE = '0000' ");
            stb.append(" LEFT JOIN ASSESS_LEVEL_SDIV_MST ASLV ON ASLV.YEAR = T3.YEAR ");
            stb.append("     AND ASLV.SEMESTER = TREC.SEMESTER ");
            stb.append("     AND ASLV.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("     AND ASLV.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("     AND ASLV.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("     AND ASLV.RUISEKI_DIV = '1' ");
            stb.append("     AND ASLV.CLASSCD = TREC.CLASSCD ");
            stb.append("     AND ASLV.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("     AND ASLV.CURRICULUM_CD = TREC.CURRICULUM_CD ");
            stb.append("     AND ASLV.SUBCLASSCD = TREC.SUBCLASSCD ");
            stb.append("     AND ASLV.DIV = '5' ");
            stb.append("     AND ASLV.GRADE = T1.GRADE ");
            stb.append("     AND ASLV.HR_CLASS = '000' ");
            stb.append("     AND ASLV.COURSECD = '0' ");
            stb.append("     AND ASLV.MAJORCD = CGRP.GROUP_CD ");
            stb.append("     AND ASLV.COURSECODE = '0000' ");
            stb.append("     AND TRANK.SCORE BETWEEN ASLV.ASSESSLOW AND ASLV.ASSESSHIGH ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
            stb.append("     AND T3.CLASSCD <= '90' ");
            stb.append(" ORDER BY ");
            stb.append("     CGRP.GROUP_CD, ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            
            final Map schregMap = new HashMap();
            try {
                final String sql = stb.toString();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map courseGroupMap = new HashMap();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == subclasscd) {
                        continue;
                    }

                    final String groupCd = rs.getString("GROUP_CD");
                    if (null == courseGroupMap.get(groupCd)) {
                        final String gradeName1 = rs.getString("GRADE_NAME1");
                        final String groupName = rs.getString("GROUP_NAME");
                        
                        final CourseGroup coursegroup = new CourseGroup(gradeName1, groupCd, groupName);
                        courseGroupList.add(coursegroup);
                        courseGroupMap.put(groupCd, coursegroup);
                    }
                    final CourseGroup coursegroup = (CourseGroup) courseGroupMap.get(groupCd);
                    
                    if (null == Subclass.getSubclass(subclasscd, coursegroup._subclassList)) {
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = rs.getString("REQUIRE_FLG");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv, requireFlg);
                        coursegroup._subclassList.add(subclass);
                    }
                    
                    final String schregno = rs.getString("SCHREGNO");
                    if (!getMappedList(coursegroup._subclassSchregnoListMap, subclasscd).contains(schregno)) {
                        getMappedList(coursegroup._subclassSchregnoListMap, subclasscd).add(schregno);

                        if (null == coursegroup._studentMap.get(schregno)) {
                            final String grade = rs.getString("GRADE");
                            final String hrClass = rs.getString("HR_CLASS");
                            final String hrName = rs.getString("HR_NAME");
                            final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                            
                            final String attendno = rs.getString("ATTENDNO");
                            final String name = rs.getString("NAME");
                            
                            final Student student = new Student(grade, hrClass, hrName, hrClassName1, attendno, schregno, name);
                            schregMap.put(schregno, student);
                            coursegroup._studentMap.put(schregno, student);
                        }
                    }
                    
                    final Student student = (Student) coursegroup._studentMap.get(schregno);
                    if (null != rs.getString("CHAIR_STAFFCD")) {
                        getMappedMap(getMappedMap(student._chairStfMap, subclasscd), rs.getString("CHAIRCD")).put(rs.getString("CHAIR_STAFFCD"), rs.getString("CHAIR_STAFFNAME"));
                    }

                    final String semtestcd = rs.getString("SEM_TESTCD");
                    
                    if (null != semtestcd) {
                        final String score = rs.getString("SCORE");
                        final String scoreLine = rs.getString("SCORE_LINE");
                        final String assesslevel = rs.getString("ASSESSLEVEL");
                        final String scoreRuikei = rs.getString("SCORE_RUIKEI");
                        final String majorRankRuikei = rs.getString("MAJOR_RANK_RUIKEI");
                        
                        final Score s = new Score(subclasscd, score, rs.getBigDecimal("AVG"), rs.getString("MAJOR_RANK"), toInteger(scoreLine), toInteger(assesslevel), scoreRuikei, rs.getBigDecimal("AVG_RUIKEI"), majorRankRuikei);
                        
                        getMappedMap(student._scoreMap, semtestcd).put(subclasscd, s);
                    }
                    
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final StringBuffer stb2 = new StringBuffer();
                stb2.append(" SELECT ");
                stb2.append("   T2.SCHREGNO, ");
                stb2.append("   T2.CHAIRCD, ");
                stb2.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb2.append("   TREC.SEMESTER, ");
                stb2.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
                stb2.append("   TREC.VALUE_DI ");
                stb2.append(" FROM CHAIR_STD_DAT T2 ");
                stb2.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb2.append("     AND T3.SEMESTER = T2.SEMESTER ");
                stb2.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                stb2.append("     AND T3.CLASSCD <= '90' ");
                stb2.append(" INNER JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
                stb2.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD <= '" + param._testcd.substring(0, 4) + "' ");
                stb2.append("     AND TREC.SCORE_DIV = '01' ");
                stb2.append("     AND TREC.CLASSCD = T3.CLASSCD ");
                stb2.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb2.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb2.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
                stb2.append("     AND TREC.SCHREGNO = T2.SCHREGNO ");
                stb2.append("     AND TREC.VALUE_DI = '*' ");
                stb2.append(" WHERE ");
                stb2.append("     T2.YEAR = '" + param._year + "' ");

                final String sql = stb2.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = (Student) schregMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final String semtestcd = rs.getString("SEMESTER") + rs.getString("TESTCD").substring(0, 4) + ("J".equals(param._schoolKind) ? "01" : "08");
                    final Map semtestSubclassMap = getMappedMap(student._scoreMap, semtestcd);
                    if (null == semtestSubclassMap.get(rs.getString("SUBCLASSCD"))) {
                        semtestSubclassMap.put(rs.getString("SUBCLASSCD"), new Score(rs.getString("SUBCLASSCD"), null, null, null, null, null, null, null, null));
                    }
                    final Score s = (Score) semtestSubclassMap.get(rs.getString("SUBCLASSCD"));
                    s._score = "*"; // 1個でも欠試があれば"*"
//                    log.info(" schregno = " + student._schregno + " , " + semtestcd + ", " + s._subclasscd + ", " + s._score);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = courseGroupList.iterator(); it.hasNext();) {
                final CourseGroup courseGroup = (CourseGroup) it.next();
                loadAttendSubclass(db2, param, courseGroup._studentMap);
            }

            return courseGroupList;
        }
        
        private static void loadAttendSubclass(
                final DB2UDB db2,
                final Param param,
                final Map studentMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {

                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSubclassSql(param._year, param._semester, null, param._edate, param._attendParamMap);
                //log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String schregno = (String) e.getKey();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!"9".equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        if (!NumberUtils.isNumber(rs.getString("SICK2"))) {
                            continue;
                        }
                        final BigDecimal sick2 = rs.getBigDecimal("SICK2");
                        student._kekkaMap.put(rs.getString("SUBCLASSCD"), sick2);
                        if (!NumberUtils.isNumber(rs.getString("ABSENCE_HIGH"))) {
                            continue;
                        }
                        final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                        if (absenceHigh.doubleValue() < sick2.doubleValue()) {
                            student._kekkaOverSubclasscdList.add(rs.getString("SUBCLASSCD"));
                        }
                    }

                    DbUtils.closeQuietly(null, null, rs);
                    db2.commit();
                }
                
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static Integer toInteger(final String v) {
            if (NumberUtils.isDigits(v)) {
                return Integer.valueOf(v);
            }
            return null;
        }
    }
    
    private static class RecordAverageDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageDat(
            final String subclasscd,
            final String avgDivKey,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg,
            final BigDecimal stddev
        ) {
            _subclasscd = subclasscd;
            _avgDivKey = avgDivKey;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public static Map getSubclassMap(final Map avgDatMap, final String semtestcd, final String avgDivKey) {
            return getMappedMap(getMappedMap(avgDatMap, semtestcd), avgDivKey);
        }
        
        public static RecordAverageDat get(final Map avgDatMap, final String semtestcd, final String avgDivKey, final String subclasscd) {
            return (RecordAverageDat) getSubclassMap(avgDatMap, semtestcd, avgDivKey).get(subclasscd);
        }
        
        public static String getGradeAvgDivKey(final String grade) {
            return "1" + "-" + grade + "-" + "000" + "-" + "00000000";
        }
        
        public static String getHrAvgDivKey(final String grade, final String hrClass) {
            return "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
        }
        
        public static String getCourseAvgDivKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
        }
        
        public static String getCourseGroupAvgDivKey(final String grade, final String coursegroupCd) {
            return "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
        }

        public static Map getRecordAverageDatMap(final DB2UDB db2, final Param param, final boolean isRuikei) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS SEMTESTCD, ");
                stb.append("  CLASSCD, ");
                stb.append("  SCHOOL_KIND, ");
                stb.append("  CURRICULUM_CD, ");
                stb.append("  SUBCLASSCD, ");
                stb.append("  AVG_DIV || '-' || GRADE || '-' || HR_CLASS || '-' || COURSECD || MAJORCD || COURSECODE AS AVG_DIV_KEY, ");
                stb.append("  SCORE, ");
                stb.append("  HIGHSCORE, ");
                stb.append("  LOWSCORE, ");
                stb.append("  COUNT, ");
                stb.append("  AVG, ");
                stb.append("  STDDEV ");
                stb.append(" FROM  ");
                if (isRuikei) {
                    stb.append("  RECORD_AVERAGE_RUIKEI_SDIV_DAT  ");
                } else {
                    stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
                }
                stb.append(" WHERE  ");
                stb.append("  YEAR = '" + param._year + "' ");
                stb.append("  AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <= '" + param._semester + param._testcd + "' ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semtestcd = rs.getString("SEMTESTCD");
                    final String subclasscd;
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || "777777".equals(rs.getString("SUBCLASSCD")) || "888888".equals(rs.getString("SUBCLASSCD")) || "999999".equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }
                    final String avgDivKey = rs.getString("AVG_DIV_KEY");
                    final String score = rs.getString("SCORE");
                    final String highscore = rs.getString("HIGHSCORE");
                    final String lowscore = rs.getString("LOWSCORE");
                    final String count = rs.getString("COUNT");
                    final BigDecimal avg = rs.getBigDecimal("AVG");
                    final BigDecimal stddev = rs.getBigDecimal("STDDEV");
                    final RecordAverageDat recordaveragedat = new RecordAverageDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
                    getMappedMap(getMappedMap(map, semtestcd), avgDivKey).put(subclasscd, recordaveragedat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
    
    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _semestername = semestername;
        }
        
        public static int getTestCount(final String semTestcd,
                final String[] semtestcds, final Map scoreMap, final String subclasscd) {
            int count = 0;
            for (int tii = 0; tii < semtestcds.length; tii++) {
                final Score s = (Score) getMappedMap(scoreMap, semtestcds[tii]).get(subclasscd);
                if (null != s && null != s._score) {
                    if (!"9990008".equals(semtestcds[tii])) {
                        count += 1;
                    }
                }
                if (semTestcd.equals(semtestcds[tii])) {
                    continue;
                }
            }
            return count;
        }
        
        public static Testitem getTestItem(final String semTestcd, final List testitemList) {
            for (final Iterator it = testitemList.iterator(); it.hasNext();) {
                final Testitem ti = (Testitem) it.next();
                if ((ti._semester + ti._testcd).equals(semTestcd)) {
                    return ti;
                }
            }
            return null;
        }
        
        public static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, T1.TESTITEMNAME ";
            sql += " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + param._year + "' ";
            sql += "   AND NOT (T1.SEMESTER <> '9' AND T1.TESTKINDCD = '99') ";
            sql += "   AND T1.SCORE_DIV = '" + ("J".equals(param._schoolKind) ? "01" : "08") + "' ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname);
                    rtn.add(testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        public String toString() {
            return "Testitem(" + _semester + ", " + _testcd + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade;
        final String _testcd;
        final String _groupCd;
        final String _loginDate;
        final List _testitemList;
        final String _edate;
        final String _semestername;
        final String _testitemname;
        final String _ranking; // 順位 1:番号順 2:成績順
        final String _schoolKind;
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        private final Map _attendParamMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _groupCd = request.getParameter("GROUP_CD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _ranking = request.getParameter("RANKING");
            _schoolKind = getSchoolKind(db2);

            _testitemList = Testitem.getTestitemList(db2, this);
            log.info(" _testitemList = " + _testitemList);
            String edate = getTestitem(db2, "EDATE");
            if (null == edate) {
                _edate = _loginDate;
                log.warn(" 出欠集計日付がnull -> " + _loginDate);
            } else {
                _edate = edate;
            }
            _testitemname = getTestitemname(db2);
            _semestername = getSemestername(db2);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _definecode = createDefineCode(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "2");
        }
        
        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }
        
        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getTestitem(final DB2UDB db2, final String field) {
            String sql = "";
            sql += " SELECT " + field + " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ";
            sql += "     AND T2.SEMESTER = T1.SEMESTER ";
            sql += "     AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + _year + "' ";
            sql += "   AND T1.SEMESTER = '" + _semester + "' ";
            sql += "   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private String getTestitemname(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            sql += "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private String getSemestername(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SEMESTERNAME FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }
    }
}

// eof


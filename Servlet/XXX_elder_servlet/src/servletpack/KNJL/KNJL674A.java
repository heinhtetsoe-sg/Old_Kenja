/*
 * $Id: ca92ac2e3f57b71fc369a3e5c11516d85578385f $
 *
 * 作成日: 2020/06/15
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

public class KNJL674A{

    private static final Log log = LogFactory.getLog(KNJL674A.class);

    private boolean _hasData;

    private Param _param;

    private final String COURSEDIV_AE = "0071"; // アドバンスト叡智コース
    private static final String COURSEDIV_EE = "0072"; // エッセンシャル叡智コース
    private static final String COURSEDIV_ALL = "9999";

    private static final String TESTDIV_NOBE = "5";
    private static final String TESTDIV_JITSU = "6";

    private static final String HOPE_COURSE_4 = "1"; //第1志望(EE4科)
    private static final String HOPE_COURSE_3 = "2"; //第1志望(EE3科)
    private static final String HOPE_COURSE_4_2 = "3"; //第2志望(EE4科)
    private static final String HOPE_COURSE_ALL = "4";

    private final String TESTSUBCLASSCD3 = "T3";
    private final String TESTSUBCLASSCD5 = "T5";

    private final String TESTSUBCLASSCD01 = "1001"; //AE4科(1,2回)
    private final String TESTSUBCLASSCD02 = "1002"; //EE4科(1,2回)
    private final String TESTSUBCLASSCD03 = "1003"; //EE3科(1,2回)
    private final String TESTSUBCLASSCD04 = "1004"; //EE4科(3,4回)
    private final String TESTSUBCLASSCD05 = "1005"; //EE3科(3,4回)

    private static final String TARGET_SEX1 = "SEX1";
    private static final String TARGET_SEX2 = "SEX2";
    private static final String TARGET_JUKEN = "TARGET_JUKEN";
    private static final String TARGET_GOUKAKU = "TARGET_GOUKAKU";
    private static final String TARGET_NYUGAKU = "ENT_FLG";
    private static final String DIV_SHIGAN = "DIV_SHIGAN";
    private static final String DIV_JUKEN = "DIV_JUKEN";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        print1_2(db2, svf); //ページ(1/3)

        print2(db2, svf); //ページ(2/3)

        print3(db2, svf); //ページ(3/3)
    }

    private static <K, V, U> Map<V, U> getMappedMap(final Map<K, Map<V, U>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<V, U>());
        }
        return map.get(key1);
    }

    private static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    public static String debugMapToStr(final String debugText, final Map map0) {
        final Map m = new HashMap();
        m.putAll(map0);
        for (final Iterator<Map.Entry> it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = it.next();
            if (e.getKey() instanceof Integer) {
                it.remove();
            } else if (e.getKey() instanceof String) {
                final String key = (String) e.getKey();
                final int numIndex = StringUtils.indexOfAny(key, "123456789");
                if (0 <= numIndex && StringUtils.repeat("_", numIndex).equals(key.substring(0, numIndex))) {
                    it.remove();
                }
            }
        }
        final Map map = new TreeMap(m);
        final StringBuffer stb = new StringBuffer();
        stb.append(StringUtils.defaultString(debugText));
        stb.append(" [");
        final List keys = new ArrayList(map.keySet());
        try {
            Collections.sort(keys);
        } catch (Exception e) {
        }
        final String newline = ""; // "\n";
        for (int i = 0; i < keys.size(); i++) {
            final Object key = keys.get(i);
            stb.append(i == 0 ? newline + "   " : " , ").append(key).append(": ").append(map.get(key)).append(newline);
        }
        stb.append("]");
        return stb.toString();
    }

    public static String addNum(final String a, final String b) {
        if (!NumberUtils.isDigits(b)) return a;
        if (!NumberUtils.isDigits(a)) return b;
        return String.valueOf(Integer.parseInt(a) + Integer.parseInt(b));
    }

    private void print1(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, PrintData1> printDataMap = getPrintData1Map(db2);

        svf.VrSetForm("KNJL674A_1.frm", 1);
        svf.VrsOut("TITLE", "麗澤中学校　" + _param._nendo + "　入学試験概況"); //タイトル
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;

        svf.VrsOut("NOTE", "※" + _param._examcourseDivMap.get(COURSEDIV_EE) + "第2志望の「受験者」は、第1志望の" + _param._examcourseDivMap.get(COURSEDIV_AE) + "に合格した者を除いた人数。");//注釈

        //全体合計の値を格納
        Map<String, PrintData1> totalCountMap = new TreeMap();
        for (final String testdiv : _param._testdivMstMap.keySet()) {
            totalCountMap.put(testdiv, new PrintData1());
        }

        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            final String examcourseName = _param._examcourseDivMap.get(examcourseDiv);
            final String courseNo = (!COURSEDIV_ALL.equals(examcourseDiv)) ? String.valueOf(++cNo) : "3";
            String[] nameArr = KNJ_EditEdit.get_token(examcourseName, 24, 2);
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", nameArr[0]); //コース名
            svf.VrsOut("COURSE_NAME" + courseNo + "_2", nameArr[1]);
            //◆ループ(列6つ)各回(4つ)・延人数・実人数
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                final TestdivMst testdivMst = _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                svf.VrsOutn("DATE" + courseNo, line, KNJ_EditDate.h_format_JP_MD(testdivMst._testdivDate)); //試験日
                for (final String sex : _param._sexMap.keySet()) {
                    final String sexName = _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }
                for (final String hopeCourseDiv : _param._hopeCourseDivList) {
                    final String key = "exam" + examcourseDiv + "-test" + testdiv + "-hope" + hopeCourseDiv;
                    PrintData1 printData = printDataMap.get(key);

                    if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望

                        PrintData1 totalCount = totalCountMap.get(testdiv);
                        if (!COURSEDIV_ALL.equals(examcourseDiv)) {
                            totalCount.add(printData);
                            log.info("   key = " + key + ", printData = " + printData);
                            if (printData == null) {
                                log.info(" no data : " + key);
                                continue;
                            }
                        } else {
                            printData = totalCount;
                        }
                        //志願者数
                        svf.VrsOutn("HOPE" + courseNo + "_1", line, printData._hope._1); //男子
                        svf.VrsOutn("HOPE" + courseNo + "_2", line, printData._hope._2); //女子
                        svf.VrsOutn("HOPE" + courseNo + "_3", line, printData._hope._3); //合計
                        //受験者数
                        svf.VrsOutn("EXAM" + courseNo + "_1", line, printData._exam._1); //男子
                        svf.VrsOutn("EXAM" + courseNo + "_2", line, printData._exam._2); //女子
                        svf.VrsOutn("EXAM" + courseNo + "_3", line, printData._exam._3); //合計
                        //合格者数
                        svf.VrsOutn("PASS" + courseNo + "_1", line, printData._pass._1); //男子
                        svf.VrsOutn("PASS" + courseNo + "_2", line, printData._pass._2); //女子
                        svf.VrsOutn("PASS" + courseNo + "_3", line, printData._pass._3); //合計
                        //入学者数
                        svf.VrsOutn("ENT" + courseNo + "_1", line, printData._ent._1); //男子
                        svf.VrsOutn("ENT" + courseNo + "_2", line, printData._ent._2); //女子
                        svf.VrsOutn("ENT" + courseNo + "_3", line, printData._ent._3); //合計
                        //募集人員
                        svf.VrsOutn("RECRUIT" + courseNo + "_3", line, printData._recruit); //合計
                        //志願者倍率
                        svf.VrsOutn("HOPE_RATIO" + courseNo + "_3", line, getRate(printData._hope._3, printData._recruit, false)); //合計

                        //実質倍率
                        svf.VrsOutn("PASS_RATIO" + courseNo + "_1", line, getRate(printData._exam._1, printData._pass._1, false)); //男子
                        svf.VrsOutn("PASS_RATIO" + courseNo + "_2", line, getRate(printData._exam._2, printData._pass._2, false)); //女子
                        svf.VrsOutn("PASS_RATIO" + courseNo + "_3", line, getRate(printData._exam._3, printData._pass._3, false)); //合計

                        //入学手続率
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_1", line, getRate(printData._ent._1, printData._pass._1, true)); //男子
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_2", line, getRate(printData._ent._2, printData._pass._2, true)); //女子
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_3", line, getRate(printData._ent._3, printData._pass._3, true)); //合計

                    } else if (!COURSEDIV_ALL.equals(examcourseDiv)) {

                        if (printData == null) {
                            log.info(" no data : " + key);
                            continue;
                        }

                        String hcNo = "";
                        if (HOPE_COURSE_4.equals(hopeCourseDiv)) { //第1志望(4科)
                            hcNo = "1";
                        } else if (HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望(英語)
                            hcNo = "2";
                        } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                            hcNo = "3";
                        }
                        log.info(" key = " + key + ", hcNo = " + hcNo + ", printData = " + printData);
                        if (null != hcNo) {
                            //志願者数
                            svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._hope._1); //男子
                            svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._hope._2); //女子
                            svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._hope._3); //合計
                            //受験者数
                            svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._exam._1); //男子
                            svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._exam._2); //女子
                            svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._exam._3); //合計
                            //合格者数
                            svf.VrsOutn("PASS_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._pass._1); //男子
                            svf.VrsOutn("PASS_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._pass._2); //女子
                            svf.VrsOutn("PASS_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._pass._3); //合計
                            //入学者数
                            svf.VrsOutn("ENT_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._ent._1); //男子
                            svf.VrsOutn("ENT_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._ent._2); //女子
                            svf.VrsOutn("ENT_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._ent._3); //合計
                        }
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private List<Map<String, String>> filter1(final String debug, final String examcourseDiv, final String testdiv, final String hopeCourseDiv, final List<Map<String, String>> rowList, final String... wheres) {

        final List<Map<String, String>> rtn = new ArrayList<Map<String, String>>();
        if (rowList.size() == 0) {
            return rtn;
        }

        final Map<String, Map<String, Map<String, String>>> testdivExamnoRowMap = new TreeMap<String, Map<String, Map<String, String>>>();
        if (TESTDIV_JITSU.equals(testdiv)) {
            for (final Map<String, String> row : rowList) {
                getMappedMap(testdivExamnoRowMap, KnjDbUtils.getString(row, "EXAMNO")).put(KnjDbUtils.getString(row, "DATA_TESTDIV"), row);
            }
        }

        final Map<String, Integer> notTargetCount = new TreeMap<String, Integer>();
        for (final Map<String, String> row : rowList) {
            String notTarget = null;

            final String dataTestdiv = KnjDbUtils.getString(row, "DATA_TESTDIV");
            final String dataExamcourseDiv = KnjDbUtils.getString(row, "DATA_EXAMCOURSE_DIV");

            HopeCourse hopeCourse = null;
            List<String> examCourseCdList = Collections.emptyList();
            if (null != hopeCourseDiv) {
                if (TESTDIV_JITSU.equals(testdiv)) {
                    hopeCourse = _param._hopeCourseCdMap.get(dataExamcourseDiv + "-" + dataTestdiv);
                } else if (COURSEDIV_ALL.equals(examcourseDiv)) {
                    // 全体合計の表は各コースの値の合計値を出力する
                    hopeCourse = _param._hopeCourseCdMap.get(dataExamcourseDiv + "-" + testdiv);
                } else {
                    hopeCourse = _param._hopeCourseCdMap.get(examcourseDiv + "-" + testdiv);
                }
                examCourseCdList = hopeCourse._map.get(hopeCourseDiv);
            }

//            final String examno = KnjDbUtils.getString(row, "EXAMNO");
            final String remark1 = KnjDbUtils.getString(row, "REMARK1");
            final String remark2 = KnjDbUtils.getString(row, "REMARK2");
            final String remark7 = KnjDbUtils.getString(row, "REMARK7");
            final String remark8 = KnjDbUtils.getString(row, "REMARK8");
            final String examFlg = KnjDbUtils.getString(row, "EXAM_FLG");

            for (final String where : wheres) {
                if (TARGET_SEX1.equals(where)) { // 性別男
                    if (!("1".equals(KnjDbUtils.getString(row, "SEX")))) {
                        notTarget = where;
                        break;
                    }
                } else if (TARGET_SEX2.equals(where)) { // 性別女
                    if (!("2".equals(KnjDbUtils.getString(row, "SEX")))) {
                        notTarget = where;
                        break;
                    }
                } else if (DIV_JUKEN.equals(where)) { // 受験者EE
                    if (COURSEDIV_EE.equals(dataExamcourseDiv)) {
                        //EEではAEの第1志望合格者は受験者数から除く
                        if ("1".equals(remark7) && null != remark2) {
                            notTarget = where;
                            break;
                        }
                    }
                } else if (TARGET_JUKEN.equals(where)) { // 受験
                    if (!("1".equals(examFlg))) {
                        notTarget = where;
                        break;
                    }
                } else if (TARGET_NYUGAKU.equals(where)) { // 入学
                    if (!("1".equals(KnjDbUtils.getString(row, "ENT_FLG")))) {
                        notTarget = where;
                        break;
                    }
                } else if (TARGET_GOUKAKU.equals(where)) { // 合格
                    if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) {  //第1志望(4科・英語)
                        if (!(examCourseCdList.contains(remark1) && "1".equals(remark7))) {
                            notTarget = where + ":" + hopeCourseDiv;
                            break;
                        }
                    } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                        if (!(!"1".equals(remark7) && examCourseCdList.contains(remark2) && "1".equals(remark8))) {
                            notTarget = where + ":" + hopeCourseDiv;
                            break;
                        }
                    } else {
                        if (!(hopeCourse._map.get(HOPE_COURSE_4).contains(remark1) && "1".equals(remark7) ||  //第1志望(4科)
                              hopeCourse._map.get(HOPE_COURSE_3).contains(remark1) && "1".equals(remark7) ||  //第1志望(英語)
                              !"1".equals(remark7) && hopeCourse._map.get(HOPE_COURSE_4_2).contains(remark2) && "1".equals(remark8))) {  //第2志望(4科)
                            notTarget = where + ":" + hopeCourseDiv;
                            break;
                        } else {
//                            if ("3".equals(testdiv)) {
//                                log.info(" remark1 course = " + remark1 + ", remark2 course = " + remark2 + ", remark7 1?= " + remark7 + ", remark8 1?=" + remark8
//                                + "\n HOPE_COURSE_4 " + hopeCourse._map.get(HOPE_COURSE_4)
//                                + "\n HOPE_COURSE_3 " + hopeCourse._map.get(HOPE_COURSE_3)
//                                + "\n HOPE_COURSE_4_2 " + hopeCourse._map.get(HOPE_COURSE_4_2)
//                                );
//                            }
                        }
                    }
                }
            }

            // 志望コース
            if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) {  //第1志望(4科) 第1志望(英語)
                if (!examCourseCdList.contains(remark1)) {
                    notTarget = "HOPE_COURSE_DIV" + hopeCourseDiv + "(" + examCourseCdList + "|" + StringUtils.defaultString(remark1) + ")";
                }
            } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                if (!examCourseCdList.contains(remark2)) {
                    notTarget = "HOPE_COURSE_DIV" + hopeCourseDiv + "(" + examCourseCdList + "|" + StringUtils.defaultString(remark1) + ")";
                }
            } else if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) {
                if (!(hopeCourse._map.get(HOPE_COURSE_4).contains(remark1) ||
                        hopeCourse._map.get(HOPE_COURSE_3).contains(remark1) ||
                        hopeCourse._map.get(HOPE_COURSE_4_2).contains(remark2))) {
                    notTarget = "HOPE_COURSE_VAL " + hopeCourseDiv + "(" + examCourseCdList + "|" + StringUtils.defaultString(remark1) + ")"; //  + " / examcourseDiv-testdiv = " + examcourseDiv + "-" + testdiv + ", remark1 = " + remark1 + ", remark2 = " + remark2 + " / HOPE_COURSE_4 = " + hopeCourse._map.get(HOPE_COURSE_4) + ", HOPE_COURSE_3 = " + hopeCourse._map.get(HOPE_COURSE_3) + ", HOPE_COURSE_4_2 = " + hopeCourse._map.get(HOPE_COURSE_4_2);
                }
            }

            if (null != notTarget) {
                notTargetCount.put(notTarget, (!notTargetCount.containsKey(notTarget) ? 0 : notTargetCount.get(notTarget)) + 1);
            } else {
                rtn.add(row);
            }
        }

        if (TESTDIV_JITSU.equals(testdiv)) {
            final Map<String, List<String>> removedExamCoursedivTestdivExamnos = new TreeMap<String, List<String>>();
            final Set<String> examnos = new TreeSet<String>();

            final Map<String, Map<String, String>> debugRemovedExamCoursedivTestdivExamnos = new TreeMap<String, Map<String, String>>();
            final Map<String, List<String>> removeDatatestdivReceptnoList = new HashMap<String, List<String>>();
            if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) {
                for (final Iterator<Map<String, String>> it = rtn.iterator(); it.hasNext();) {
                    final Map<String, String> row = it.next();
                    final String dataExamcourseDiv = KnjDbUtils.getString(row, "DATA_EXAMCOURSE_DIV");
                    final String dataTestdiv = KnjDbUtils.getString(row, "DATA_TESTDIV");
                    final String examno = KnjDbUtils.getString(row, "EXAMNO");
                    final String remark1 = KnjDbUtils.getString(row, "REMARK1");
                    final String remark2 = KnjDbUtils.getString(row, "REMARK2");

                    final List<String> examnoHopeCourseDivList = new ArrayList<String>();
                    for (final String eachHopeCourseDiv : Arrays.asList(HOPE_COURSE_4, HOPE_COURSE_3, HOPE_COURSE_4_2)) {
                        final HopeCourse hopeCourse = _param._hopeCourseCdMap.get(dataExamcourseDiv + "-" + dataTestdiv);
                        final List<String> examCourseCdList = hopeCourse._map.get(eachHopeCourseDiv);

                        if (HOPE_COURSE_4.equals(eachHopeCourseDiv)) {
                            if (examCourseCdList.contains(remark1)) { // 入試区分1, 2なら1002、入試区分3, 4なら1005
                                examnoHopeCourseDivList.add("HOPE_COURSE_4");
                            }
                        } else if (HOPE_COURSE_3.equals(eachHopeCourseDiv)) {
                            if (examCourseCdList.contains(remark1)) { // 入試区分1, 2なら1003、入試区分3, 4なら""
                                examnoHopeCourseDivList.add("HOPE_COURSE_3");
                            }
                        } else if (HOPE_COURSE_4_2.equals(eachHopeCourseDiv)) {
                            if (examCourseCdList.contains(remark2)) { // 入試区分1, 2なら1002、入試区分3, 4なら1005
                                examnoHopeCourseDivList.add("HOPE_COURSE_4_2");
                            }
                        }
                    }
                    getMappedMap(debugRemovedExamCoursedivTestdivExamnos, dataExamcourseDiv + "-" + examno).put(dataTestdiv + "-" + KnjDbUtils.getString(row, "RECEPTNO"), examnoHopeCourseDivList.toString());
                }

                // 異なるhopeCourseDivに重複してカウントされる志願者(dataTestdiv - receptno)のみを除く
                for (final Map.Entry<String, Map<String, String>> e : debugRemovedExamCoursedivTestdivExamnos.entrySet()) {
                    final String dataExamcourseDivExamno = e.getKey();
                    final Map<String, String> testdivReceptnoHopeCourseDivMap = e.getValue();
                    final Map<String, List<String>> hopeCourseDivdataTestdivReceptNoListMap = new HashMap<String, List<String>>();
                    for (final Map.Entry<String, String> testdivReceptnoHopeCourseDivEntry : testdivReceptnoHopeCourseDivMap.entrySet()) {
                        final String testdivReceptno = testdivReceptnoHopeCourseDivEntry.getKey();
                        final String hopeCourseDivString = testdivReceptnoHopeCourseDivEntry.getValue();
                        getMappedList(hopeCourseDivdataTestdivReceptNoListMap, hopeCourseDivString).add(testdivReceptno);
                    }
                    if (hopeCourseDivdataTestdivReceptNoListMap.size() > 1) {

                        for (final String hopeCourseDivString : hopeCourseDivdataTestdivReceptNoListMap.keySet()) {
                            final List<String> dataTestdivReceptNoList = getMappedList(hopeCourseDivdataTestdivReceptNoListMap, hopeCourseDivString);
                            for (int i = 1; i < dataTestdivReceptNoList.size(); i++) { // 最初以外の要素を削除
                                getMappedList(removeDatatestdivReceptnoList, dataExamcourseDivExamno).add(dataTestdivReceptNoList.get(i));
                            }
                        }
                        final int removeSize = getMappedList(removeDatatestdivReceptnoList, dataExamcourseDivExamno).size();
                        if (removeSize > 0) {
                            log.info(" ★ " + dataExamcourseDivExamno + " = " + hopeCourseDivdataTestdivReceptNoListMap + ",  remove (" + removeSize + ") = " + getMappedList(removeDatatestdivReceptnoList, dataExamcourseDivExamno));
                        }
                    }
                }

                // 受験番号の件数
                for (final Iterator<Map<String, String>> it = rtn.iterator(); it.hasNext();) {
                    final Map<String, String> row = it.next();
                    final String dataExamcourseDiv = KnjDbUtils.getString(row, "DATA_EXAMCOURSE_DIV");
                    final String dataTestdiv = KnjDbUtils.getString(row, "DATA_TESTDIV");
                    final String receptno = KnjDbUtils.getString(row, "RECEPTNO");
                    final String examno = KnjDbUtils.getString(row, "EXAMNO");

                    if (removeDatatestdivReceptnoList.containsKey(dataExamcourseDiv + "-" + examno)) {
                        if (getMappedList(removeDatatestdivReceptnoList, dataExamcourseDiv + "-" + examno).contains(dataTestdiv + "-" + receptno)) {
                            it.remove();
                        }
                    } else {
                        if (!removedExamCoursedivTestdivExamnos.containsKey(dataExamcourseDiv + "-" + examno)) {
                            // 1件のみ残す
                            removedExamCoursedivTestdivExamnos.put(dataExamcourseDiv + "-" + examno, new ArrayList<String>());
                        } else {
                            it.remove();
                            removedExamCoursedivTestdivExamnos.get(dataExamcourseDiv + "-" + examno).add(debugMapToStr("", row));
                        }
                    }

                    examnos.add(examno);
                }
            } else {

                // 受験番号の件数
                for (final Iterator<Map<String, String>> it = rtn.iterator(); it.hasNext();) {
                    final Map<String, String> row = it.next();
                    final String dataExamcourseDiv = KnjDbUtils.getString(row, "DATA_EXAMCOURSE_DIV");
                    final String examno = KnjDbUtils.getString(row, "EXAMNO");

                    if (!removedExamCoursedivTestdivExamnos.containsKey(dataExamcourseDiv + "-" + examno)) {
                        // 1件のみ残す
                        removedExamCoursedivTestdivExamnos.put(dataExamcourseDiv + "-" + examno, new ArrayList<String>());
                    } else {
                        it.remove();
                        removedExamCoursedivTestdivExamnos.get(dataExamcourseDiv + "-" + examno).add(debugMapToStr("", row));
                    }

                    examnos.add(examno);
                }
            }

//            if (null != debug && COURSEDIV_EE.equals(examcourseDiv) && TESTDIV_JITSU.equals(testdiv) && ArrayUtils.contains(wheres, TARGET_SEX2)) {
//                final int sourceSize = rtn.size();
//                log.info(" debug examnos size = " + debugRemovedExamCoursedivTestdivExamnos.size());
//                int totalRemovedSize = 0;
//                for (final Map.Entry<String, Map<String, String>> e : debugRemovedExamCoursedivTestdivExamnos.entrySet()) {
//                    if (e.getValue().size() > 1) {
//                        //log.info("  " + e.getKey() + " (" + e.getValue().size() + ") = " + e.getValue());
//                        totalRemovedSize += e.getValue().size() - 1;
//                    }
//                }
//                log.info(" total removed size = " + totalRemovedSize + ", sourceSize = " + sourceSize);
//                int count = 0;
//                for (final String examno : examnos) {
//                    log.info(" examcourseDiv " + examcourseDiv + ", hopeCourseDiv " + hopeCourseDiv + ", wheres = " + ArrayUtils.toString(wheres) + ", tgt " + (++count) + " / " + examnos.size() + ",  examno " + examno);
//                }
//            }
        }

//        if (null != debug && TESTDIV_JITSU.equals(testdiv)) {
//            log.info(" testdiv = " + testdiv + ", examcourseDiv = " + examcourseDiv + " , hopeCourseDiv = " + hopeCourseDiv + ", wheres = " + ArrayUtils.toString(wheres) + ", rtn size = " + rtn.size() + " (source size = " + rowList.size() + ", notTargetCount = " + notTargetCount + ")");
//        }

        return rtn;
    }

    private int VrsOutn(final Vrw32alp svf, final String fieldname, final int gyo, final String data) {
        final Map<String, SvfField> fieldMap = _param._svfFormFieldInfoMap.get(_param._currentForm);
        if (null != fieldMap) {
            if (!fieldMap.containsKey(fieldname)) {
                log.error("no such field : " + fieldname + ", " + gyo + ", " + data);
            }
        }
//        log.info(" VrsOutn(\"" + fieldname + "\", " + gyo + ", " + (null == data ? null : "\"" + data + "\"") + ")");
        return svf.VrsOutn(fieldname, gyo, data);
    }

    private static String[] addWhere(final String[] wheres, final String where) {
        final List<String> l = new ArrayList<String>(Arrays.asList(wheres));
        l.add(where);
        return l.toArray(new String[wheres.length + 1]);
    }

    private void printDanjokei(final Vrw32alp svf, final String debugString, final String examcourseDiv, final String testdiv, final String hopeCourseDiv, final String field, final int line, final List<Map<String, String>> rowList, final String...wheres) {
        final String danshi = String.valueOf(filter1(debugString, examcourseDiv, testdiv, hopeCourseDiv, rowList, addWhere(wheres, TARGET_SEX1)).size());
        final String joshi = String.valueOf(filter1(debugString, examcourseDiv, testdiv, hopeCourseDiv, rowList, addWhere(wheres, TARGET_SEX2)).size());
        final String gokei = String.valueOf(filter1(debugString, examcourseDiv, testdiv, hopeCourseDiv, rowList, wheres).size());
//        log.info(" examcoursediv " + examcourseDiv + ", testdiv " + testdiv + ", wheres = " + ArrayUtils.toString(wheres) + ", " + field + " = (" + danshi + ", " + joshi + ", " + gokei + ")");
        VrsOutn(svf, field + "_1", line, danshi); //男子
        VrsOutn(svf, field + "_2", line, joshi); //女子
        VrsOutn(svf, field + "_3", line, gokei); //合計
    }

    private void printDanjokeiRate(final Vrw32alp svf, final String examcourseDiv, final String testdiv, final String hopeCourseDiv, final String field, final int line, final List<Map<String, String>> rowList, final List<Map<String, String>> rowList2, final boolean percentFlg) {
        svf.VrsOutn(field + "_1", line, getRate(String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList, new String[] {TARGET_SEX1}).size()), String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList2, new String[] {TARGET_SEX1}).size()), percentFlg)); //男子
        svf.VrsOutn(field + "_2", line, getRate(String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList, new String[] {TARGET_SEX2}).size()), String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList2, new String[] {TARGET_SEX2}).size()), percentFlg)); //女子
        svf.VrsOutn(field + "_3", line, getRate(String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList, new String[] {}).size()), String.valueOf(filter1(null, examcourseDiv, testdiv, hopeCourseDiv, rowList2, new String[] {}).size()), percentFlg)); //合計
    }

    private void print1_2(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, PrintData1_2> printDataMap = PrintData1_2.getPrintData1Map_2(db2, _param);

        final String formname = "KNJL674A_1.frm";
        svf.VrSetForm(formname, 1);
        _param._currentForm = formname;
        _param._svfFormFieldInfoMap.put(formname, SvfField.getSvfFormFieldInfoMapGroupByName(svf));

        svf.VrsOut("TITLE", "麗澤中学校　" + _param._nendo + "　入学試験概況"); //タイトル
        //◆ループ(表3つ)各コース(2つ)・全体合計

        svf.VrsOut("NOTE", "※" + _param._examcourseDivMap.get(COURSEDIV_EE) + "第2志望の「受験者」は、第1志望の" + _param._examcourseDivMap.get(COURSEDIV_AE) + "に合格した者を除いた人数。");//注釈

        for (final String testdiv : _param._testdivMstMap.keySet()) {
            final TestdivMst testdivMst = _param._testdivMstMap.get(testdiv);
            final int line = testdivMst._line;

            for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
                final String examcourseName = _param._examcourseDivMap.get(examcourseDiv);
                final String courseNo = COURSEDIV_AE.equals(examcourseDiv) ? "1" : COURSEDIV_EE.equals(examcourseDiv) ? "2" : /* COURSEDIV_ALL */ "3";
                String[] nameArr = KNJ_EditEdit.get_token(examcourseName, 24, 2);
                svf.VrsOut("COURSE_NAME" + courseNo + "_1", nameArr[0]); //コース名
                svf.VrsOut("COURSE_NAME" + courseNo + "_2", nameArr[1]);
                //◆ループ(列6つ)各回(4つ)・延人数・実人数
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                svf.VrsOutn("DATE" + courseNo, line, KNJ_EditDate.h_format_JP_MD(testdivMst._testdivDate)); //試験日
                for (final String sex : _param._sexMap.keySet()) {
                    final String sexName = _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }

                //メイン表
                final String key = "exam" + examcourseDiv + "-test" + testdiv;

                if (COURSEDIV_AE.equals(examcourseDiv) || COURSEDIV_EE.equals(examcourseDiv)) {
                    final PrintData1_2 printData = printDataMap.get(key);
                    //募集人員
                    svf.VrsOutn("RECRUIT" + courseNo + "_3", line, printData._recruit); //合計

                    final List<Map<String, String>> rowList = new ArrayList<Map<String, String>>(printData._rowList);
//                    log.info(" examcourseDiv " + examcourseDiv + " 志願者");
                    //志願者数
                    printDanjokei(svf, "志願者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "HOPE" + courseNo,               line, rowList, DIV_SHIGAN);
                    printDanjokei(svf, "志願者", examcourseDiv, testdiv, HOPE_COURSE_4, "HOPE_COURSE" + courseNo + "_1", line, rowList, DIV_SHIGAN);  //第1志望(4科)
                    printDanjokei(svf, "志願者", examcourseDiv, testdiv, HOPE_COURSE_3, "HOPE_COURSE" + courseNo + "_2", line, rowList, DIV_SHIGAN);  //第1志望(英語)
                    printDanjokei(svf, "志願者", examcourseDiv, testdiv, HOPE_COURSE_4_2, "HOPE_COURSE" + courseNo + "_3", line, rowList, DIV_SHIGAN);

                    //志願者倍率
                    svf.VrsOutn("HOPE_RATIO" + courseNo + "_3", line, getRate(String.valueOf(filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, DIV_SHIGAN).size()), printData._recruit, false)); //合計

//                    log.info(" examcourseDiv " + examcourseDiv + " 受験者");
                    //受験者数
                    printDanjokei(svf, "受験者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "EXAM" + courseNo,               line, rowList, TARGET_JUKEN, DIV_JUKEN);
                    printDanjokei(svf, "受験者", examcourseDiv, testdiv, HOPE_COURSE_4, "EXAM_COURSE" + courseNo + "_1", line, rowList, TARGET_JUKEN, DIV_JUKEN);  //第1志望(4科)
                    printDanjokei(svf, "受験者", examcourseDiv, testdiv, HOPE_COURSE_3, "EXAM_COURSE" + courseNo + "_2", line, rowList, TARGET_JUKEN, DIV_JUKEN);  //第1志望(英語)
                    printDanjokei(svf, "受験者", examcourseDiv, testdiv, HOPE_COURSE_4_2, "EXAM_COURSE" + courseNo + "_3", line, rowList, TARGET_JUKEN, DIV_JUKEN);  //第2志望(4科)

//                    log.info(" examcourseDiv " + examcourseDiv + " 合格者");
                    //合格者数
                    printDanjokei(svf, "合格者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "PASS" + courseNo,               line, rowList, TARGET_JUKEN, TARGET_GOUKAKU);
                    printDanjokei(svf, "合格者", examcourseDiv, testdiv, HOPE_COURSE_4, "PASS_COURSE" + courseNo + "_1", line, rowList, TARGET_JUKEN, TARGET_GOUKAKU);  //第1志望(4科)
                    printDanjokei(svf, "合格者", examcourseDiv, testdiv, HOPE_COURSE_3, "PASS_COURSE" + courseNo + "_2", line, rowList, TARGET_JUKEN, TARGET_GOUKAKU);  //第1志望(英語)
                    printDanjokei(svf, "合格者", examcourseDiv, testdiv, HOPE_COURSE_4_2, "PASS_COURSE" + courseNo + "_3", line, rowList, TARGET_JUKEN, TARGET_GOUKAKU);  //第2志望(4科)

                    //実質倍率
                    printDanjokeiRate(svf, examcourseDiv, testdiv, HOPE_COURSE_ALL, "PASS_RATIO" + courseNo, line, filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, DIV_JUKEN), filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, TARGET_GOUKAKU), false);

//                    log.info(" examcourseDiv " + examcourseDiv + " 入学者");
                    //入学者数
                    printDanjokei(svf, "入学者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "ENT" + courseNo,               line, rowList, TARGET_JUKEN, TARGET_NYUGAKU);
                    printDanjokei(svf, "入学者", examcourseDiv, testdiv, HOPE_COURSE_4, "ENT_COURSE" + courseNo + "_1", line, rowList, TARGET_JUKEN, TARGET_NYUGAKU);  //第1志望(4科)
                    printDanjokei(svf, "入学者", examcourseDiv, testdiv, HOPE_COURSE_3, "ENT_COURSE" + courseNo + "_2", line, rowList, TARGET_JUKEN, TARGET_NYUGAKU);  //第1志望(英語)
                    printDanjokei(svf, "入学者", examcourseDiv, testdiv, HOPE_COURSE_4_2, "ENT_COURSE" + courseNo + "_3", line, rowList, TARGET_JUKEN, TARGET_NYUGAKU);  //第2志望(4科)

                    //入学手続率
                    printDanjokeiRate(svf, examcourseDiv, testdiv, HOPE_COURSE_ALL, "ENT_RATIO" + courseNo, line, filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, TARGET_NYUGAKU), filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, TARGET_GOUKAKU), true);

                } else if (COURSEDIV_ALL.equals(examcourseDiv)) {

                    final PrintData1_2 zentaiGokei = new PrintData1_2();
                    for (final String examcourseDiv2 : _param._examcourseDivMap.keySet()) {
                        if (COURSEDIV_ALL.equals(examcourseDiv2)) {
                            continue;
                        }
                        final String key2 = "exam" + examcourseDiv2 + "-test" + testdiv;
                        PrintData1_2 printData = printDataMap.get(key2);
                        if (null != printData) {
                            zentaiGokei._recruit = addNum(zentaiGokei._recruit, printData._recruit);
                            zentaiGokei._rowList.addAll(printData._rowList);
//                            if (TESTDIV_JITSU.equals(testdiv)) {
//                                log.info(" add list " + key2 + " : " + printData._rowList.size() + " = " + zentaiGokei._rowList.size());
//                            }
                        }
                    }

                    final PrintData1_2 printData = zentaiGokei;

                    //募集人員
                    svf.VrsOutn("RECRUIT" + courseNo + "_3", line, printData._recruit); //合計

//                    log.info(" examcourseDiv " + examcourseDiv + " 志願者");
                    final List<Map<String, String>> rowList = new ArrayList<Map<String, String>>(printData._rowList);
                    //志願者数
                    final List<Map<String, String>> shigansha = filter1("志願者", examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, DIV_SHIGAN);
                    printDanjokei(svf, "志願者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "HOPE" + courseNo,               line, shigansha);

                    //志願者倍率
                    svf.VrsOutn("HOPE_RATIO" + courseNo + "_3", line, getRate(String.valueOf(filter1(null, examcourseDiv, testdiv, HOPE_COURSE_ALL, shigansha, DIV_SHIGAN).size()), printData._recruit, false)); //合計

//                    log.info(" examcourseDiv " + examcourseDiv + " 受験者");
                    //受験者数
                    final List<Map<String, String>> jukensha = filter1("受験者", examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, DIV_JUKEN);
                    printDanjokei(svf, "受験者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "EXAM" + courseNo,               line, jukensha);

//                    log.info(" examcourseDiv " + examcourseDiv + " 合格者");
                    //合格者数
                    final List<Map<String, String>> gokakusha = filter1("合格者", examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, TARGET_GOUKAKU);
                    printDanjokei(svf, "合格者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "PASS" + courseNo,               line, gokakusha);

                    //実質倍率
                    printDanjokeiRate(svf, examcourseDiv, testdiv, HOPE_COURSE_ALL, "PASS_RATIO" + courseNo, line, jukensha, gokakusha, false);

//                    log.info(" examcourseDiv " + examcourseDiv + " 入学者");
                    //入学者数
                    final List<Map<String, String>> nyugakusha = filter1("入学者", examcourseDiv, testdiv, HOPE_COURSE_ALL, rowList, TARGET_JUKEN, TARGET_NYUGAKU);
                    printDanjokei(svf, "入学者", examcourseDiv, testdiv, HOPE_COURSE_ALL, "ENT" + courseNo,               line, nyugakusha);

                    //入学手続率
                    printDanjokeiRate(svf, examcourseDiv, testdiv, HOPE_COURSE_ALL, "ENT_RATIO" + courseNo, line, nyugakusha, gokakusha, true);
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private String getRate(final String bunsi, final String bunbo, final boolean percentFlg) {
        if (bunsi == null || bunbo == null) return null;

        if (NumberUtils.isNumber(bunbo) && Integer.parseInt(bunbo) == 0) {
            if (!percentFlg) {
                return "0.00";
            } else {
                return "0%";
            }
        }
        BigDecimal result = new BigDecimal(bunsi).divide(new BigDecimal(bunbo), 2, BigDecimal.ROUND_HALF_UP);//小数点第3位を四捨五入し、小数点第2位まで表示
        if (!percentFlg) {
            return result.toString();
        } else {
            final BigDecimal hyaku = new BigDecimal("100");
            return result.multiply(hyaku).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "%"; //パーセント表記
        }
    }

    private void print2(final DB2UDB db2, final Vrw32alp svf) {

        final String DIV_TOKUSHOU = "TK";
        final String DIV_TOKUSHOU1 = "TK1";
        final String DIV_TOKUSHOU2 = "TK2";
        final String DIV_TOKUSHOU3 = "TK3";
        final String DIV_TOKUSHOU_NAI1 = "TK_NAI1"; //内数1
        final String DIV_TOKUSHOU_NAI2 = "TK_NAI2"; //内数2
        final String DIV_KIKOKU = "KIKOKU";

        final List<String> divList = Arrays.asList(DIV_TOKUSHOU, DIV_TOKUSHOU1, DIV_TOKUSHOU2, DIV_TOKUSHOU3, DIV_TOKUSHOU_NAI1, DIV_TOKUSHOU_NAI2, DIV_KIKOKU);

        final Map<String, PrintData2> printDataMap = new TreeMap();
        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                for (final String div : divList) {
                    for (final String hopeCourseDiv : _param._hopeCourseDivList) {
                        final String key = "ex" + examcourseDiv + "-t" + testdiv + "-h" + hopeCourseDiv + "-d" + div;
                        final String sql = getPrintData2Sql(examcourseDiv, testdiv, hopeCourseDiv, div, DIV_TOKUSHOU, DIV_TOKUSHOU1, DIV_TOKUSHOU2, DIV_TOKUSHOU3, DIV_TOKUSHOU_NAI1, DIV_TOKUSHOU_NAI2, DIV_KIKOKU);
                        log.fatal(sql);
                        if (COURSEDIV_EE.equals(examcourseDiv) && "5".equals(testdiv) && "4".equals(hopeCourseDiv)) {

                            log.debug(" sql =" + sql);
                        }
                        for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                            // log.info(" row = " + row);
                            final Count hope = new Count(KnjDbUtils.getString(row, "HOPE1"), KnjDbUtils.getString(row, "HOPE2"), KnjDbUtils.getString(row, "HOPE3"));
                            final Count exam = new Count(KnjDbUtils.getString(row, "EXAM1"), KnjDbUtils.getString(row, "EXAM2"), KnjDbUtils.getString(row, "EXAM3"));
                            final Count pass = new Count(KnjDbUtils.getString(row, "PASS1"), KnjDbUtils.getString(row, "PASS2"), KnjDbUtils.getString(row, "PASS3"));
                            final Count ent = new Count(KnjDbUtils.getString(row, "ENT1"), KnjDbUtils.getString(row, "ENT2"), KnjDbUtils.getString(row, "ENT3"));
                            final PrintData2 printData2 = new PrintData2(examcourseDiv, testdiv, hopeCourseDiv, div, hope, exam, pass, ent);

                            printDataMap.put(key, printData2);
                            if (DIV_KIKOKU.equals(div)) {
                                log.info(" fetched key " + key + ", printData2 = " + printData2);
                            }
                        }
                    }
                }
            }
        }

        svf.VrSetForm("KNJL674A_2.frm", 1);
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            final String examcourseName = _param._examcourseDivMap.get(examcourseDiv);
            final String courseNo = (!COURSEDIV_ALL.equals(examcourseDiv)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列6つ)各回(4つ)・延人数・実人数
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                final TestdivMst testdivMst = _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                for (final String sex : _param._sexMap.keySet()) {
                    final String sexName = _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }
                for (final String div : divList) {
                    for (final String hopeCourseDiv : _param._hopeCourseDivList) {
                        final String key = "ex" + examcourseDiv + "-t" + testdiv + "-h" + hopeCourseDiv + "-d" + div;
                        PrintData2 printData = printDataMap.get(key);
                        if (printData == null) continue;
                        if (!DIV_KIKOKU.equals(div) && HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                            final String tokushouNo = DIV_TOKUSHOU1.equals(div) ? "_1" : DIV_TOKUSHOU2.equals(div) ? "_2" : DIV_TOKUSHOU3.equals(div) ? "_3" : DIV_TOKUSHOU_NAI1.equals(div) ? "_4" : DIV_TOKUSHOU_NAI2.equals(div) ? "_5" : "";
                            //特奨採用者数(全,1,2,3種, 内数)
                            svf.VrsOutn("SC_ADPTION" + courseNo + tokushouNo + "_1", line, printData._hope._1); //男子
                            svf.VrsOutn("SC_ADPTION" + courseNo + tokushouNo + "_2", line, printData._hope._2); //女子
                            svf.VrsOutn("SC_ADPTION" + courseNo + tokushouNo + "_3", line, printData._hope._3); //合計
                            //特奨入学者数(全,1,2,3種, 内数)
                            svf.VrsOutn("SC_ENT" + courseNo + tokushouNo + "_1", line, printData._ent._1); //男子
                            svf.VrsOutn("SC_ENT" + courseNo + tokushouNo + "_2", line, printData._ent._2); //女子
                            svf.VrsOutn("SC_ENT" + courseNo + tokushouNo + "_3", line, printData._ent._3); //合計
                        } else if (DIV_KIKOKU.equals(div)) { // 帰国子女
                            if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                                if (COURSEDIV_ALL.equals(examcourseDiv)) { // 帰国子女の全体合計はアドバンスト叡智コース、エッセンシャル叡智コースの合計を表示する
                                    PrintData2 kikokuTotal = new PrintData2();

                                    for (final String tExamcourseDiv : _param._examcourseDivMap.keySet()) {
                                        if (COURSEDIV_ALL.equals(tExamcourseDiv)) {
                                            continue;
                                        }
                                        final String tKey = "ex" + tExamcourseDiv + "-t" + testdiv + "-h" + hopeCourseDiv + "-d" + div;
                                        PrintData2 t = printDataMap.get(tKey);
                                        if (null != t) {
                                            kikokuTotal.add(t);
                                        }
                                    }
                                    printData = kikokuTotal;
                                }
                                //志願者数
                                svf.VrsOutn("RET_HOPE" + courseNo + "_1", line, printData._hope._1); //男子
                                svf.VrsOutn("RET_HOPE" + courseNo + "_2", line, printData._hope._2); //女子
                                svf.VrsOutn("RET_HOPE" + courseNo + "_3", line, printData._hope._3); //合計
                                //受験者数
                                svf.VrsOutn("RET_EXAM" + courseNo + "_1", line, printData._exam._1); //男子
                                svf.VrsOutn("RET_EXAM" + courseNo + "_2", line, printData._exam._2); //女子
                                svf.VrsOutn("RET_EXAM" + courseNo + "_3", line, printData._exam._3); //合計
                                //合格者数
                                svf.VrsOutn("RET_PASS" + courseNo + "_1", line, printData._pass._1); //男子
                                svf.VrsOutn("RET_PASS" + courseNo + "_2", line, printData._pass._2); //女子
                                svf.VrsOutn("RET_PASS" + courseNo + "_3", line, printData._pass._3); //合計
                                //入学者数
                                svf.VrsOutn("RET_ENT" + courseNo + "_1", line, printData._ent._1); //男子
                                svf.VrsOutn("RET_ENT" + courseNo + "_2", line, printData._ent._2); //女子
                                svf.VrsOutn("RET_ENT" + courseNo + "_3", line, printData._ent._3); //合計
                            } else if (!COURSEDIV_ALL.equals(examcourseDiv)) {
                                String hcNo = null;
                                if (HOPE_COURSE_4.equals(hopeCourseDiv)) { //第1志望(4科)
                                    hcNo = "1";
                                } else if (HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望(英語)
                                    hcNo = "2";
                                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                                    hcNo = "3";
                                }
                                log.info(" key = " + key + ", hcNo = " + hcNo + ", printData2 = " + printData);
                                if (null != hcNo) {
                                    //志願者数
                                    svf.VrsOutn("RET_HOPE_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._hope._1); //男子
                                    svf.VrsOutn("RET_HOPE_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._hope._2); //女子
                                    svf.VrsOutn("RET_HOPE_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._hope._3); //合計
                                    //受験者数
                                    svf.VrsOutn("RET_EXAM_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._exam._1); //男子
                                    svf.VrsOutn("RET_EXAM_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._exam._2); //女子
                                    svf.VrsOutn("RET_EXAM_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._exam._3); //合計
                                    //合格者数
                                    svf.VrsOutn("RET_PASS_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._pass._1); //男子
                                    svf.VrsOutn("RET_PASS_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._pass._2); //女子
                                    svf.VrsOutn("RET_PASS_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._pass._3); //合計
                                    //入学者数
                                    svf.VrsOutn("RET_ENT_COURSE" + courseNo + "_" + hcNo + "_1", line, printData._ent._1); //男子
                                    svf.VrsOutn("RET_ENT_COURSE" + courseNo + "_" + hcNo + "_2", line, printData._ent._2); //女子
                                    svf.VrsOutn("RET_ENT_COURSE" + courseNo + "_" + hcNo + "_3", line, printData._ent._3); //合計
                                }
                            }
                        }
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void print3(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, PrintData3> printDataMap = getPrintData3Map(db2);

        final String formname = "KNJL674A_3.frm";
        svf.VrSetForm(formname, 1);
        _param._currentForm = formname;
        _param._svfFormFieldInfoMap.put(formname, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
        svf.VrsOut("TITLE", "麗澤中学校　" + _param._nendo + "　入学試験概況"); //タイトル
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            final String examcourseName = _param._examcourseDivMap.get(examcourseDiv);
            final String courseNo = (!COURSEDIV_ALL.equals(examcourseDiv)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列4つ)各回(4つ)
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                if (TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) {
                    continue;
                }
                final TestdivMst testdivMst = _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                VrsOutn(svf, "COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                VrsOutn(svf, "DATE" + courseNo, line, KNJ_EditDate.h_format_JP_MD(testdivMst._testdivDate)); //試験日

                int subNo = 0;
                List<TestSubclass> testSubclassList = _param._testsubclassMap.get(examcourseDiv + "-" + testdiv);
                for (final TestSubclass testSubclass : testSubclassList) {
                    final String testsubclasscd = testSubclass._subclassCd;
                    final String testsubclassName = testSubclass._subclassName;

                    String subclassNo = "";
                    if (TESTSUBCLASSCD01.equals(testsubclasscd) || TESTSUBCLASSCD02.equals(testsubclasscd) || TESTSUBCLASSCD04.equals(testsubclasscd) || TESTSUBCLASSCD05.equals(testsubclasscd)) {
                        subclassNo = "7";
                    } else if (TESTSUBCLASSCD03.equals(testSubclass._subclassCd)) {
                        subclassNo = "8";
                    } else {
                        subclassNo = String.valueOf(++subNo);
                    }
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(testsubclassName) > 6 ? "2" : "1";

                    final String key = examcourseDiv + "-" + testdiv + "-" + testsubclasscd;
                    final PrintData3 printData = printDataMap.get(key);
                    if (printData == null) continue;

                    VrsOutn(svf, "CLASS_NAME" + courseNo + "_" + subclassNo + "_" + nameField, line, testsubclassName); //試験科目名
                    VrsOutn(svf, "EXAM" + courseNo + "_" + subclassNo, line, printData._examCnt); //受験者数
                    VrsOutn(svf, "PERFECT" + courseNo + "_" + subclassNo, line, printData._perfect); //満点
                    VrsOutn(svf, "AVERAGE" + courseNo + "_1_" + subclassNo, line, printData._scoreAvgSex1); //男子平均点
                    VrsOutn(svf, "AVERAGE" + courseNo + "_2_" + subclassNo, line, printData._scoreAvgSex2); //女子平均点
                    VrsOutn(svf, "AVERAGE" + courseNo + "_3_" + subclassNo, line, printData._scoreAvg); //全体平均点
                    VrsOutn(svf, "DEVI" + courseNo + "_" + subclassNo, line, printData._scoreStddev); //全体標準偏差
                    VrsOutn(svf, "MAX" + courseNo + "_" + subclassNo, line, printData._scoreMax); //最高点
                    VrsOutn(svf, "MIN" + courseNo + "_" + subclassNo, line, printData._scoreMin); //最低点
                    VrsOutn(svf, "PASS_AVERAGE" + courseNo + "_" + subclassNo, line, printData._passAvg); //合格者平均点
                    if ((!COURSEDIV_ALL.equals(examcourseDiv))) {
                        VrsOutn(svf, "SC_MIN" + courseNo + "_1_" + subclassNo, line, printData._tokushouMin1); //特奨1種最低点
                        VrsOutn(svf, "SC_MIN" + courseNo + "_2_" + subclassNo, line, printData._tokushouMin2); //特奨2種最低点
                        VrsOutn(svf, "SC_MIN" + courseNo + "_3_" + subclassNo, line, printData._tokushouMin3); //特奨3種最低点
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private Map<String, PrintData1> getPrintData1Map(final DB2UDB db2) {
        final Map<String, PrintData1> retMap = new TreeMap();
        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            if (COURSEDIV_ALL.equals(examcourseDiv)) {
                continue;
            }
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                for (final String hopeCourseDiv : _param._hopeCourseDivList) {
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        final String sql = getPrintData1Sql(examcourseDiv, testdiv, hopeCourseDiv);

                        final String key = "exam" + examcourseDiv + "-test" + testdiv + "-hope" + hopeCourseDiv;
                        if (Arrays.asList("6").contains(testdiv)) {
                            log.debug(" " + key + ", sql =" + sql);
                        }

                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String recruit = rs.getString("RECRUIT");
                            final Count hope = new Count(rs.getString("HOPE1"), rs.getString("HOPE2"), rs.getString("HOPE3"));
                            final Count exam = new Count(rs.getString("EXAM1"), rs.getString("EXAM2"), rs.getString("EXAM3"));
                            final Count pass = new Count(rs.getString("PASS1"), rs.getString("PASS2"), rs.getString("PASS3"));
                            final Count ent = new Count(rs.getString("ENT1"), rs.getString("ENT2"), rs.getString("ENT3"));
                            final PrintData1 printData1 = new PrintData1(examcourseDiv, testdiv, hopeCourseDiv, recruit, hope, exam, pass, ent);
                            retMap.put(key, printData1);
                        }
                    } catch (SQLException ex) {
                        log.error("Exception:", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                }
            }
        }
        return retMap;
    }

    private Map<String, PrintData3> getPrintData3Map(final DB2UDB db2) {
        final Map<String, PrintData3> retMap = new TreeMap();
        for (final String examcourseDiv : _param._examcourseDivMap.keySet()) {
            for (final String testdiv : _param._testdivMstMap.keySet()) {
                if (TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) {
                    continue;
                }
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getPrintData3Sql(examcourseDiv, testdiv);
//                    log.info(" examcourseDiv " + examcourseDiv + ", testdiv " + testdiv + " sql =" + sql + "\n");
//                    log.debug(" sql =" + sql);
//                    if ("1".equals(testdiv)) {
//                        log.info(" " + examcourseDiv + " sql = " + sql);
//                    }
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                        final String examCnt = rs.getString("EXAM_CNT");
                        final String perfect = rs.getString("PERFECT");
                        final String scoreAvgSex1 = sishagonyu(rs.getString("SCORE_AVG_SEX1"), 1);
                        final String scoreAvgSex2 = sishagonyu(rs.getString("SCORE_AVG_SEX2"), 1);
                        final String scoreAvg = sishagonyu(rs.getString("SCORE_AVG"), 1);
                        final String scoreStddev = sishagonyu(rs.getString("SCORE_STDDEV"), 1);
                        final String scoreMax = sishagonyu(rs.getString("SCORE_MAX"), 0);
                        final String scoreMin = sishagonyu(rs.getString("SCORE_MIN"), 0);
                        final String passAvg = sishagonyu(rs.getString("PASS_AVG"), 1);
                        final String tokushouMin1 = rs.getString("TOKUSHOU_MIN1");
                        final String tokushouMin2 = rs.getString("TOKUSHOU_MIN2");
                        final String tokushouMin3 = rs.getString("TOKUSHOU_MIN3");

                        final PrintData3 PrintData3 = new PrintData3(examcourseDiv, testdiv, testsubclasscd, examCnt, perfect, scoreAvgSex1, scoreAvgSex2, scoreAvg, scoreStddev, scoreMax, scoreMin, passAvg, tokushouMin1, tokushouMin2, tokushouMin3);
                        retMap.put(examcourseDiv + "-" + testdiv + "-" + testsubclasscd, PrintData3);
                    }
                } catch (SQLException ex) {
                    log.error("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }
        return retMap;
    }

    private String sishagonyu(final String decimal, final int scale) {
        return !NumberUtils.isNumber(decimal) ? null : new BigDecimal(decimal).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getPrintData1Sql(final String examcourseDiv, final String testdiv, final String hopeCourseDiv) {

        String examCourseCd = "";
        HopeCourse hopeCourse = null;

//        if (!COURSEDIV_ALL.equals(examcourseDiv)) {
             hopeCourse = _param._hopeCourseCdMap.get(examcourseDiv + "-" + testdiv);
            if (hopeCourse != null) {

                examCourseCd = interquote(hopeCourse._map.get(hopeCourseDiv));
            }
//        }

        final StringBuffer stb = new StringBuffer();
        //メイン表
        stb.append(" SELECT ");
        stb.append("     MAX(C1.CAPACITY) AS RECRUIT, "); //募集人員
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            String jitsuNotTarget = "";
            String jitsuNotTargetExam = "";
            if (HOPE_COURSE_3.equals(hopeCourseDiv) || HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第1志望(英語) or 第2志望(4科)
                jitsuNotTarget = " AND NOT (JITSU_NOT.EXAMNO IS NOT NULL) "; // 第1志望（4科）対象者を除く
                jitsuNotTargetExam = " AND NOT (JITSU_NOT.EXAMNO IS NOT NULL AND JITSU_NOT.EXAM_FLG = '1') "; // 第1志望（4科）対象者を除く
            }

            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '1' " + jitsuNotTarget + " THEN R1.EXAMNO END) AS HOPE1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '2' " + jitsuNotTarget + " THEN R1.EXAMNO END) AS HOPE2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN 1 = 1        " + jitsuNotTarget + " THEN R1.EXAMNO END) AS HOPE3, ");
            String whennull = "";
            if (COURSEDIV_EE.equals(examcourseDiv)) { //EEではAEの第1志望合格者は受験者数から除く
                whennull = " WHEN value(RD_007.REMARK7, '0') = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL ";
            }
            stb.append("     COUNT(DISTINCT CASE " + whennull + " WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' " + jitsuNotTargetExam + " THEN R1.EXAMNO END) AS EXAM1, ");
            stb.append("     COUNT(DISTINCT CASE " + whennull + " WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' " + jitsuNotTargetExam + " THEN R1.EXAMNO END) AS EXAM2, ");
            stb.append("     COUNT(DISTINCT CASE " + whennull + " WHEN S1.EXAM_FLG = '1'                  " + jitsuNotTargetExam + " THEN R1.EXAMNO END) AS EXAM3, ");
            final String where;
//            if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
                if (HOPE_COURSE_4.equals(hopeCourseDiv)) { //第1志望(4科)
                    where = " AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') ";
                } else if (HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望(英語) (第1志望（4科）対象者を除く)
                    where = " AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') " + jitsuNotTargetExam;
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科) (第1志望（4科）対象者を除く)
                    where = " AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') " + jitsuNotTargetExam;
                } else {
                    String caseStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(4科)
                                    + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(英語)
                                    + "value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "') AND RD_007.REMARK8 = '1' ";   //第2志望(4科)
                    where = " AND (" + caseStmt + ") ";
                }
//            } else { //全体合計の表
//                where = " AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) ";
//            }
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " THEN R1.EXAMNO END) AS PASS3, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' " + where + " AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
        } else { //各回・延人数
            stb.append("     COUNT(CASE WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
            stb.append("     COUNT(CASE WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
            stb.append("     COUNT(RD_007.RECEPTNO) AS HOPE3, ");
            if (COURSEDIV_EE.equals(examcourseDiv)) { //EEではAEの第1志望合格者は受験者数から除く
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            } else {
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            }
//            if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
                if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望(4科・英語)
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    String caseStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(4科)
                                    + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(英語)
                                    + "value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "') AND RD_007.REMARK8 = '1' ";   //第2志望(4科)

                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
//            } else { //全体合計の表
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) THEN RD_007.RECEPTNO END) AS PASS3, ");
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
//                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
//            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             S1.RECEPTNO, ");
        stb.append("             '1' AS EXAM_FLG ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_SCORE_DAT S1 ");
        stb.append("         WHERE ");
        stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND S1.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("             AND S1.SCORE2 IS NOT NULL ");
        stb.append("         GROUP BY ");
        stb.append("             S1.RECEPTNO ");
        stb.append("     ) S1 ON S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             BD_007.EXAMNO, ");
        stb.append("             '1' AS ENT_FLG, ");
        stb.append("             BD_007.REMARK4 AS TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
        stb.append("         WHERE ");
        stb.append("             BD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND BD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND BD_007.SEQ = '007' ");
        stb.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_COURSE_MST C1 ");
        stb.append("                 WHERE ");
        stb.append("                     C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
        }
//        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            stb.append("                     AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
//        }
        stb.append("                 GROUP BY ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
        stb.append("             ) ");
        stb.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             C1.ENTEXAMYEAR, C1.APPLICANTDIV, ");
        stb.append("             SUM(C1.CAPACITY) AS CAPACITY ");
        stb.append("         FROM ");
        stb.append("          ( ");
        stb.append("            SELECT ");
        stb.append("              ENTEXAMYEAR, ");
        stb.append("              APPLICANTDIV, ");
        stb.append("              TESTDIV, ");
        stb.append("              ENTER_COURSECODE, ");
        stb.append("              MAX(CAPACITY) AS CAPACITY ");
        stb.append("            FROM ");
        stb.append("              ENTEXAM_COURSE_MST ");
        stb.append("            GROUP BY ");
        stb.append("              ENTEXAMYEAR, ");
        stb.append("              APPLICANTDIV, ");
        stb.append("              TESTDIV, ");
        stb.append("              ENTER_COURSECODE ");
        stb.append("          ) C1 ");
        stb.append("         WHERE ");
        stb.append("             C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND C1.TESTDIV = '" + testdiv + "' ");
        }
//        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            stb.append("             AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
//        }
        stb.append("         GROUP BY ");
        stb.append("             C1.ENTEXAMYEAR, C1.APPLICANTDIV ");
        stb.append("     ) C1 ON C1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND C1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_014 ");
        stb.append("       ON RD_014.ENTEXAMYEAR 	= RD_007.ENTEXAMYEAR ");
        stb.append("       AND RD_014.APPLICANTDIV 	= RD_007.APPLICANTDIV ");
        stb.append("       AND RD_014.TESTDIV 		= RD_007.TESTDIV ");
        stb.append("       AND RD_014.EXAM_TYPE 	= RD_007.EXAM_TYPE ");
        stb.append("       AND RD_014.RECEPTNO 		= RD_007.RECEPTNO ");
        stb.append("       AND RD_014.SEQ 		    = '014' ");
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            if (HOPE_COURSE_3.equals(hopeCourseDiv) || HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第1志望(英語) or 第2志望(4科)
                stb.append("     LEFT JOIN ( ");
                stb.append("      SELECT I1.EXAMNO, MIN(S1.EXAM_FLG) AS EXAM_FLG FROM ENTEXAM_RECEPT_DAT I1 ");
                stb.append("      INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT IRD_007 ON ");
                stb.append("          I1.ENTEXAMYEAR = IRD_007.ENTEXAMYEAR ");
                stb.append("          AND I1.APPLICANTDIV = IRD_007.APPLICANTDIV ");
                stb.append("          AND I1.TESTDIV = IRD_007.TESTDIV ");
                stb.append("          AND I1.EXAM_TYPE = IRD_007.EXAM_TYPE ");
                stb.append("          AND I1.RECEPTNO = IRD_007.RECEPTNO ");
                stb.append("      LEFT JOIN ( ");
                stb.append("         SELECT DISTINCT ");
                stb.append("             S1.RECEPTNO, ");
                stb.append("             S1.TESTDIV, ");
                stb.append("             '1' AS EXAM_FLG ");
                stb.append("         FROM ");
                stb.append("             ENTEXAM_SCORE_DAT S1 ");
                stb.append("         WHERE ");
                stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
                stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
                stb.append("             AND S1.SCORE2 IS NOT NULL ");
                stb.append("         GROUP BY ");
                stb.append("             S1.RECEPTNO, ");
                stb.append("             S1.TESTDIV ");
                stb.append("      ) S1 ON S1.RECEPTNO = IRD_007.RECEPTNO ");
                stb.append("      WHERE I1.ENTEXAMYEAR = '" + _param._entexamyear + " ' ");
                stb.append("        AND I1.APPLICANTDIV = '" + _param._applicantDiv + " ' ");
                stb.append("        AND ( ");
                String or = "";
                for (final String itestdiv : _param._testdivMstMap.keySet()) {
                    if (TESTDIV_NOBE.equals(itestdiv) || TESTDIV_JITSU.equals(itestdiv)) {
                        continue;
                    }
                    stb.append(or);
                    stb.append(" IRD_007.TESTDIV = '" + itestdiv + "' AND IRD_007.REMARK1 IN ('" + interquote(_param._hopeCourseCdMap.get(examcourseDiv + "-" + itestdiv)._map.get(HOPE_COURSE_4)) + "') "); // 第1志望（4科）対象者を除く
                    or = " OR ";
                }
                stb.append("        ) "); // 第1志望（4科）対象者を除く
                if (COURSEDIV_EE.equals(examcourseDiv)) { //EEではAEの第1志望合格者は受験者数から除く
                    stb.append(" AND NOT (value(IRD_007.REMARK7, '0') = '1' AND IRD_007.REMARK2 IS NOT NULL) ");
                }
                stb.append("      GROUP BY I1.EXAMNO ");
                stb.append("     ) JITSU_NOT ON JITSU_NOT.EXAMNO = R1.EXAMNO ");
            }
        }

        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("     AND RD_007.SEQ = '007' ");
//        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) {  //第1志望(4科・英語)
                stb.append("     AND (RD_007.REMARK1 IN ('" + examCourseCd + "')) ");
            } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                stb.append("     AND (RD_007.REMARK2 IN ('" + examCourseCd + "')) ");
            } else {
                String whereStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') OR " //第1志望(4科)
                                 + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') OR " //第1志望(英語)
                                 + "(RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "')) ";   //第2志望(4科)

                stb.append("     AND (" + whereStmt + ") ");
            }
//        }
        if (!"ALL".equals(_param._hallCd)) {
            stb.append("     AND RD_014.REMARK1 = '" + _param._hallCd + "' ");
        }

        return stb.toString();
    }

    private static String interquote(final List<String> cds) {
        final StringBuffer stb = new StringBuffer();
        if (null != cds) {
            String comma = "";
            for (final String cd : cds) {
                stb.append(comma).append(cd);
                comma = "', '";
            }
        }
        return stb.toString();
    }

    private String getPrintData2Sql(final String examcourseDiv, final String testdiv, final String hopeCourseDiv, final String div,
            final String DIV_TOKUSHOU, final String DIV_TOKUSHOU1, final String DIV_TOKUSHOU2, final String DIV_TOKUSHOU3, final String DIV_TOKUSHOU_NAI1, final String DIV_TOKUSHOU_NAI2, final String DIV_KIKOKU) {

        String examCourseCd = "";
        String examCourseCd2 = "";
        HopeCourse hopeCourse = null;
        HopeCourse hopeCourse2 = null; //EE第2志望に対するAE第1志望
        if (!COURSEDIV_ALL.equals(examcourseDiv)) {
             hopeCourse = _param._hopeCourseCdMap.get(examcourseDiv + "-" + testdiv);
             hopeCourse2 = _param._hopeCourseCdMap.get(COURSEDIV_AE + "-" + testdiv);
            if (hopeCourse != null) {

                examCourseCd = interquote(hopeCourse._map.get(hopeCourseDiv));
            }
            if (hopeCourse2._map != null) {

                examCourseCd2 = interquote(hopeCourse2._map.get(hopeCourseDiv));
            }
        }
//        log.fatal("コース区分:" + examcourseDiv);
//        log.fatal("テスト区分:" + testdiv);
//        log.fatal("志望:" 		+ hopeCourseDiv);
//        log.fatal("コード:"     + examCourseCd);
//        log.fatal("");

        final StringBuffer stb = new StringBuffer();
        //メイン表
        stb.append(" SELECT ");
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            if (COURSEDIV_EE.equals(examcourseDiv) && !DIV_KIKOKU.equals(div)) {
                //EEではAEの第1志望合格者は特奨者から除く
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN B1.SEX = '1' THEN R1.EXAMNO END) AS HOPE1, ");
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN B1.SEX = '2' THEN R1.EXAMNO END) AS HOPE2, ");
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL ELSE R1.EXAMNO END) AS HOPE3, ");
            } else {
                stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '1' THEN R1.EXAMNO END) AS HOPE1, ");
                stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '2' THEN R1.EXAMNO END) AS HOPE2, ");
                stb.append("     COUNT(DISTINCT R1.EXAMNO) AS HOPE3, ");
            }
            if (COURSEDIV_EE.equals(examcourseDiv)) { //EEではAEの第1志望合格者は受験者数から除く
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(DISTINCT CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            } else {
                stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            }
            if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
                if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    String caseStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(4科)
                                    + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(英語)
                                    + "value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "') AND RD_007.REMARK8 = '1' ";   //第2志望(4科)

                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            }
        } else { //各回・延人数
            if (COURSEDIV_EE.equals(examcourseDiv) && !DIV_KIKOKU.equals(div)) {
                //EEではAEの第1志望合格者は特奨者から除く
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL ELSE RD_007.RECEPTNO END) AS HOPE3, ");
            } else {
                stb.append("     COUNT(CASE WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
                stb.append("     COUNT(CASE WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
                stb.append("     COUNT(RD_007.RECEPTNO) AS HOPE3, ");
            }
            if (COURSEDIV_EE.equals(examcourseDiv)) { //EEではAEの第1志望合格者は受験者数から除く
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(CASE WHEN value(RD_007.REMARK7, 0) = '1' AND RD_007.REMARK2 IS NOT NULL THEN NULL WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            } else {
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
                stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            }
            if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
                if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 IN ('" + examCourseCd + "') AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + examCourseCd + "') AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    String caseStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(4科)
                                    + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') AND RD_007.REMARK7 = '1' OR " //第1志望(英語)
                                    + "value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "') AND RD_007.REMARK8 = '1' ";   //第2志望(4科)

                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (" + caseStmt + ") AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK8 = '1')) AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             S1.RECEPTNO, ");
        stb.append("             '1' AS EXAM_FLG ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_SCORE_DAT S1 ");
        stb.append("         WHERE ");
        stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND S1.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("             AND S1.SCORE2 IS NOT NULL ");
        stb.append("         GROUP BY ");
        stb.append("             S1.RECEPTNO ");
        stb.append("     ) S1 ON S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             BD_007.EXAMNO, ");
        stb.append("             '1' AS ENT_FLG, ");
        stb.append("             BD_007.REMARK4 AS TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
        stb.append("         WHERE ");
        stb.append("             BD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND BD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND BD_007.SEQ = '007' ");
        stb.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_COURSE_MST C1 ");
        stb.append("                 WHERE ");
        stb.append("                     C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
        }
        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            stb.append("                     AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
        stb.append("             ) ");
        stb.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_005 ");
        stb.append("          ON BD_005.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND BD_005.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND BD_005.EXAMNO = R1.EXAMNO ");
        stb.append("         AND BD_005.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_015 ");
        stb.append("          ON RD_015.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND RD_015.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND RD_015.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND RD_015.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND RD_015.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("         AND RD_015.SEQ = '015' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_014 ");
        stb.append("       ON RD_014.ENTEXAMYEAR 	= RD_007.ENTEXAMYEAR ");
        stb.append("       AND RD_014.APPLICANTDIV 	= RD_007.APPLICANTDIV ");
        stb.append("       AND RD_014.TESTDIV 		= RD_007.TESTDIV ");
        stb.append("       AND RD_014.EXAM_TYPE 	= RD_007.EXAM_TYPE ");
        stb.append("       AND RD_014.RECEPTNO 		= RD_007.RECEPTNO ");
        stb.append("       AND RD_014.SEQ 		    = '014' ");

        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("     AND RD_007.SEQ = '007' ");
        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            if (HOPE_COURSE_4.equals(hopeCourseDiv) || HOPE_COURSE_3.equals(hopeCourseDiv)) {  //第1志望(4科・英語)
                stb.append("     AND (RD_007.REMARK1 IN ('" + examCourseCd + "')) ");
            } else if (HOPE_COURSE_4_2.equals(hopeCourseDiv)) { //第2志望(4科)
                stb.append("     AND (RD_007.REMARK2 IN ('" + examCourseCd + "')) ");
            } else {
                String whereStmt = "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4))   + "') OR " //第1志望(4科)
                                 + "RD_007.REMARK1 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_3))   + "') OR " //第1志望(英語)
                                 + "RD_007.REMARK2 IN ('" + interquote(hopeCourse._map.get(HOPE_COURSE_4_2)) + "') ";   //第2志望(4科)

                stb.append("     AND (" + whereStmt + ") ");
            }
        }
        if (DIV_TOKUSHOU.equals(div) || DIV_TOKUSHOU_NAI1.equals(div) || DIV_TOKUSHOU_NAI2.equals(div)) { //特奨(全)
            stb.append("     AND RD_015.REMARK1 IS NOT NULL ");

            if (DIV_TOKUSHOU_NAI1.equals(div)) {
                stb.append("     AND (RD_007.REMARK1 IN ('1001', '1002', '1004', '1005') OR RD_007.REMARK2 IN ('1001', '1002', '1004', '1005'))");
            } else if (DIV_TOKUSHOU_NAI2.equals(div)) {
                stb.append("     AND (RD_007.REMARK1 IN ('1003') OR RD_007.REMARK2 IN ('1003')) ");
            }
        } else if (DIV_TOKUSHOU1.equals(div) || DIV_TOKUSHOU2.equals(div) || DIV_TOKUSHOU3.equals(div)) { //特奨(1,2,3種)
            final String tokushouCd = DIV_TOKUSHOU1.equals(div) ? "001" : DIV_TOKUSHOU2.equals(div) ? "002" : DIV_TOKUSHOU3.equals(div) ? "003" : "";
            stb.append("     AND RD_015.REMARK1 = '" + tokushouCd + "' ");
        } else if (DIV_KIKOKU.equals(div)) {
            stb.append("     AND BD_005.REMARK2 = '1' "); //帰国子女
        }

        if (!"ALL".equals(_param._hallCd)) {
            stb.append("     AND RD_014.REMARK1 = '" + _param._hallCd + "' ");
        }

        return stb.toString();
    }

    private String getPrintData3Sql(final String examcourseDiv, final String testdiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_PERFECT AS ( ");
        stb.append("     SELECT ");
        stb.append("         P1.TESTSUBCLASSCD, ");
        stb.append("         MIN(P1.PERFECT) AS PERFECT ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_PERFECT_MST P1 ");
        stb.append("      	 LEFT JOIN ENTEXAM_COURSE_MST C1 ");
        stb.append("        	ON C1.ENTEXAMYEAR = P1.ENTEXAMYEAR ");
        stb.append("        	AND C1.APPLICANTDIV = P1.APPLICANTDIV ");
        stb.append("        	AND C1.TESTDIV = P1.TESTDIV ");
        stb.append("        	AND C1.EXAMCOURSECD = P1.EXAMCOURSECD ");

        stb.append("     WHERE ");
        stb.append("         P1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND P1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND P1.TESTDIV = '" + testdiv + "' ");
        if (COURSEDIV_AE.equals(examcourseDiv)) { // アドバンスト叡智の場合、国算の満点について傾斜配点を加味した120を印字する
            stb.append("     AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
        }
        stb.append("         AND P1.TESTSUBCLASSCD != 'A' ");
        stb.append("     GROUP BY ");
        stb.append("         P1.TESTSUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" , T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         S1.TESTSUBCLASSCD, ");
        stb.append("         S1.SCORE, ");
        stb.append("         S1.SCORE2, ");
        stb.append("         T2.MAGNIFYING ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT S1 ");
        stb.append("       INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("         ON RD_007.ENTEXAMYEAR = S1.ENTEXAMYEAR ");
        stb.append("         AND RD_007.APPLICANTDIV = S1.APPLICANTDIV ");
        stb.append("         AND RD_007.TESTDIV = S1.TESTDIV ");
        stb.append("         AND RD_007.EXAM_TYPE = S1.EXAM_TYPE ");
        stb.append("         AND RD_007.RECEPTNO = S1.RECEPTNO ");
        stb.append("       LEFT JOIN ENTEXAM_PERFECT_MST T2 ");
        stb.append("         ON T2.ENTEXAMYEAR = S1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = S1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = S1.TESTDIV ");
        stb.append("         AND T2.TESTSUBCLASSCD = S1.TESTSUBCLASSCD ");
        stb.append("         AND T2.EXAMCOURSECD = VALUE(RD_007.REMARK1, RD_007.REMARK2) ");
        stb.append("     WHERE ");
        stb.append("         S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND S1.TESTDIV = '" + testdiv + "' ");
        stb.append("         AND S1.TESTSUBCLASSCD != 'A' ");
        stb.append("         AND RD_007.SEQ = '007' ");
        stb.append(" ) ");
        stb.append(" , T_PERFECT2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         P1.TESTSUBCLASSCD, ");
        stb.append("         P1.PERFECT ");
        stb.append("     FROM ");
        stb.append("         T_PERFECT P1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("       P1.EXAMCOURSECD AS TESTSUBCLASSCD, ");
        stb.append("       SUM(P1.PERFECT) AS PERFECT ");
        stb.append("     FROM ");
        stb.append("       ENTEXAM_PERFECT_MST P1 ");
        stb.append("       LEFT JOIN ENTEXAM_COURSE_MST C1 ");
        stb.append("         ON C1.ENTEXAMYEAR = P1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = P1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = P1.TESTDIV ");
        stb.append("         AND C1.EXAMCOURSECD = P1.EXAMCOURSECD ");
        stb.append("     WHERE ");
        stb.append("       P1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("       AND P1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("       AND P1.TESTDIV = '" + testdiv + "' ");
        stb.append("       AND P1.TESTSUBCLASSCD <> 'A' ");
        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            stb.append("       AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
        }
        stb.append("     GROUP BY ");
        stb.append("       P1.EXAMCOURSECD ");
        stb.append(" ) ");
        stb.append(" , T_SCORE2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         S1.TESTSUBCLASSCD, ");
        if (COURSEDIV_ALL.equals(examcourseDiv) || COURSEDIV_EE.equals(examcourseDiv)) {
            stb.append("         S1.SCORE AS SCORE2, ");
            stb.append("         S1.SCORE AS RAW_SCORE2 ");
        } else {
            stb.append("         S1.SCORE * VALUE(MAGNIFYING, 1.0) AS SCORE2, ");
            stb.append("         S1.SCORE2 AS RAW_SCORE2 ");
        }
        stb.append("     FROM ");
        stb.append("         T_SCORE S1 ");
        stb.append("     WHERE ");
        stb.append("         S1.SCORE IS NOT NULL ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("       T1.RECEPTNO, ");
        stb.append("       T2.EXAMCOURSECD AS TESTSUBCLASSCD, ");
        if (COURSEDIV_ALL.equals(examcourseDiv) || COURSEDIV_EE.equals(examcourseDiv)) {
            stb.append("       SUM(T1.SCORE ) AS SCORE2, ");
            stb.append("       SUM(T1.SCORE ) AS RAW_SCORE2 ");
        } else {
            stb.append("       SUM(T1.SCORE2) AS SCORE2, ");
            stb.append("       SUM(T1.SCORE2) AS RAW_SCORE2 ");
        }
        stb.append("     FROM ");
        stb.append("       ENTEXAM_SCORE_DAT T1 ");
        stb.append("       INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("         ON RD_007.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND RD_007.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND RD_007.TESTDIV = T1.TESTDIV ");
        stb.append("         AND RD_007.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("         AND RD_007.RECEPTNO = T1.RECEPTNO ");
        stb.append("       INNER JOIN ENTEXAM_PERFECT_MST T2 ");
        stb.append("         ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
        stb.append("       INNER JOIN ENTEXAM_COURSE_MST T3 ");
        stb.append("         ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T2.TESTDIV ");
        stb.append("         AND T3.EXAMCOURSECD = T2.EXAMCOURSECD ");
        stb.append("         AND T3.EXAMCOURSECD IN (RD_007.REMARK1, RD_007.REMARK2) ");
        stb.append("     WHERE ");
        stb.append("       T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("       AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("       AND T1.TESTDIV = '" + testdiv + "' ");
        stb.append("       AND T1.TESTSUBCLASSCD <> 'A' ");
        stb.append("       AND T1.SCORE IS NOT NULL ");
        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            stb.append("       AND T3.ENTER_COURSECODE = '" + examcourseDiv + "' ");
        }
        stb.append("     GROUP BY T1.RECEPTNO, T2.EXAMCOURSECD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     S1.TESTSUBCLASSCD, ");
        stb.append("     COUNT(RD_007.RECEPTNO) AS EXAM_CNT, ");
        stb.append("     MAX(P1.PERFECT) AS PERFECT, ");
        stb.append("     AVG(FLOAT(CASE WHEN B1.SEX = '1' THEN S1.SCORE2 END)) AS SCORE_AVG_SEX1, ");
        stb.append("     AVG(FLOAT(CASE WHEN B1.SEX = '2' THEN S1.SCORE2 END)) AS SCORE_AVG_SEX2, ");
        stb.append("     AVG(FLOAT(S1.SCORE2)) AS SCORE_AVG, ");
        stb.append("     STDDEV(FLOAT(S1.SCORE2)) AS SCORE_STDDEV, ");
        stb.append("     MAX(INT(S1.RAW_SCORE2)) AS SCORE_MAX, ");
        stb.append("     MIN(INT(S1.RAW_SCORE2)) AS SCORE_MIN, ");
        if (!COURSEDIV_ALL.equals(examcourseDiv)) { //各コースの表
            String whenStmt = "";
            if (COURSEDIV_AE.equals(examcourseDiv)) {
                if ("1".equals(testdiv) || "2".equals(testdiv)) {
                    whenStmt = " RD_007.REMARK1 = '1001' AND RD_007.REMARK7 = '1' ";
                } else {
                    whenStmt = " RD_007.REMARK1 = '1004' AND RD_007.REMARK7 = '1' ";
                }
            } else if (COURSEDIV_EE.equals(examcourseDiv)) {
                if ("1".equals(testdiv) || "2".equals(testdiv)) {
                    whenStmt = "     (RD_007.REMARK1 IN ('1002', '1003') AND RD_007.REMARK7 = '1') ";
                    whenStmt += "           OR (RD_007.REMARK1 = '1001' AND value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 IN ('1002', '1003') AND RD_007.REMARK8 = '1') ";

                } else {
                    whenStmt = "     (RD_007.REMARK1 = '1005' AND RD_007.REMARK7 = '1') ";
                    whenStmt += "           OR (RD_007.REMARK1 = '1004' AND value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK2 = '1005' AND RD_007.REMARK8 = '1') ";
                }
            }
            stb.append("     AVG(FLOAT(CASE WHEN (" + whenStmt + ") THEN S1.SCORE2 END)) AS PASS_AVG, ");
        } else { //全体合計の表
            stb.append("     AVG(FLOAT(CASE WHEN (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN S1.SCORE2 END)) AS PASS_AVG, ");
        }
        stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '001' THEN S1.RAW_SCORE2 END) AS TOKUSHOU_MIN1, ");
        stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '002' THEN S1.RAW_SCORE2 END) AS TOKUSHOU_MIN2, ");
        stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '003' THEN S1.RAW_SCORE2 END) AS TOKUSHOU_MIN3 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     INNER JOIN T_SCORE2 S1 ON S1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN T_PERFECT2 P1 ON P1.TESTSUBCLASSCD = S1.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             RD_015.RECEPTNO, ");
        stb.append("             RD_015.REMARK1 AS TOKUSHOU_CD "); //特奨(1,2,3種)
        stb.append("         FROM ");
        stb.append("             ENTEXAM_RECEPT_DETAIL_DAT RD_015 ");
        stb.append("         WHERE ");
        stb.append("             RD_015.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND RD_015.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND RD_015.TESTDIV = '" + testdiv + "' ");
        stb.append("             AND RD_015.EXAM_TYPE = '1' ");
        stb.append("             AND RD_015.SEQ = '015' ");
        stb.append("             AND RD_015.REMARK1 IS NOT NULL ");
        stb.append("     ) RD_015 ON RD_015.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_014 ");
        stb.append("       ON RD_014.ENTEXAMYEAR 	= RD_007.ENTEXAMYEAR ");
        stb.append("       AND RD_014.APPLICANTDIV 	= RD_007.APPLICANTDIV ");
        stb.append("       AND RD_014.TESTDIV 		= RD_007.TESTDIV ");
        stb.append("       AND RD_014.EXAM_TYPE 	= RD_007.EXAM_TYPE ");
        stb.append("       AND RD_014.RECEPTNO 		= RD_007.RECEPTNO ");
        stb.append("       AND RD_014.SEQ 		    = '014' ");

        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        stb.append("     AND RD_007.SEQ = '007' ");

        if (COURSEDIV_AE.equals(examcourseDiv)) {
            if ("1".equals(testdiv) || "2".equals(testdiv)) {
                stb.append("     AND (RD_007.REMARK1 = '1001') ");
            } else {
                stb.append("     AND (RD_007.REMARK1 = '1004') ");
            }
        } else if (COURSEDIV_EE.equals(examcourseDiv)) {
            if ("1".equals(testdiv) || "2".equals(testdiv)) {
                stb.append("     AND (RD_007.REMARK1 IN ('1002', '1003') OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK1 = '1001' AND RD_007.REMARK2 IN ('1002', '1003'))) ");

            } else {
                stb.append("     AND (RD_007.REMARK1 = '1005' OR (value(RD_007.REMARK7, 0) <> '1' AND RD_007.REMARK1 = '1004' AND RD_007.REMARK2 = '1005')) ");
            }
        }

        if (!"ALL".equals(_param._hallCd)) {
            stb.append("     AND RD_014.REMARK1 = '" + _param._hallCd + "' ");
        }

        stb.append(" GROUP BY ");
        stb.append("     S1.TESTSUBCLASSCD ");
        return stb.toString();
    }

    private static class PrintData1_2 {
        String _recruit;
        List<Map<String, String>> _rowList = new ArrayList<Map<String, String>>();

        private static Map<String, PrintData1_2> getPrintData1Map_2(final DB2UDB db2, final Param param) {
            final Map<String, PrintData1_2> retMap = new TreeMap();
            for (final String examcourseDiv : param._examcourseDivMap.keySet()) {
                if (COURSEDIV_ALL.equals(examcourseDiv)) {
                    continue;
                }
                for (final String testdiv : param._testdivMstMap.keySet()) {

                    final StringBuffer stb = new StringBuffer();
                    stb.append("         SELECT ");
                    stb.append("             SUM(C1.CAPACITY) AS CAPACITY ");
                    stb.append("         FROM ");
                    stb.append("          ( ");
                    stb.append("            SELECT ");
                    stb.append("              ENTEXAMYEAR, ");
                    stb.append("              APPLICANTDIV, ");
                    stb.append("              TESTDIV, ");
                    stb.append("              ENTER_COURSECODE, ");
                    stb.append("              MAX(CAPACITY) AS CAPACITY ");
                    stb.append("            FROM ");
                    stb.append("              ENTEXAM_COURSE_MST ");
                    stb.append("            GROUP BY ");
                    stb.append("              ENTEXAMYEAR, ");
                    stb.append("              APPLICANTDIV, ");
                    stb.append("              TESTDIV, ");
                    stb.append("              ENTER_COURSECODE ");
                    stb.append("          ) C1 ");
                    stb.append("         WHERE ");
                    stb.append("             C1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
                    stb.append("             AND C1.APPLICANTDIV = '" + param._applicantDiv + "' ");
                    if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
                        stb.append("             AND C1.TESTDIV = '" + testdiv + "' ");
                    }
                    stb.append("             AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
                    final String recruit = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

                    final String sql = getPrintData1Sql2(db2, param, examcourseDiv, testdiv);

                    final String key = "exam" + examcourseDiv + "-test" + testdiv;
                    if (!COURSEDIV_ALL.equals(examcourseDiv) && Arrays.asList("6").contains(testdiv)) {
                        log.debug(" data1sql2 " + key + ", sql =" + sql);
                    }

                    final PrintData1_2 printData1 = new PrintData1_2();
                    printData1._recruit = recruit;
                    printData1._rowList = KnjDbUtils.query(db2, sql);
                    log.info(" record size " + key + " = " + printData1._rowList.size());
                    retMap.put(key, printData1);
                }
            }
            return retMap;
        }

        private static String getPrintData1Sql2(final DB2UDB db2, final Param param, final String examcourseDiv, final String testdiv) {

            final StringBuffer stb3 = new StringBuffer();
            stb3.append(" SELECT ");
            stb3.append("     '" + examcourseDiv + "' AS DATA_EXAMCOURSE_DIV, ");
            stb3.append("     RD_007.TESTDIV AS DATA_TESTDIV, ");
            stb3.append("     RD_007.RECEPTNO, ");
            stb3.append("     R1.EXAMNO, ");
            stb3.append("     B1.SEX, ");
            stb3.append("     BD_007.ENT_FLG, ");
            stb3.append("     S1.EXAM_FLG, ");
            stb3.append("     RD_007.REMARK1, ");
            stb3.append("     RD_007.REMARK2, ");
            stb3.append("     RD_007.REMARK7, ");
            stb3.append("     RD_007.REMARK8 ");
            stb3.append(" FROM ");
            stb3.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
            stb3.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
            stb3.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
            stb3.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
            stb3.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
            stb3.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
            stb3.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
            stb3.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb3.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb3.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb3.append("         AND B1.EXAMNO = R1.EXAMNO ");
            stb3.append("     LEFT JOIN ( ");
            stb3.append("         SELECT ");
            stb3.append("             S1.RECEPTNO, ");
            stb3.append("             '1' AS EXAM_FLG ");
            stb3.append("         FROM ");
            stb3.append("             ENTEXAM_SCORE_DAT S1 ");
            stb3.append("         WHERE ");
            stb3.append("             S1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb3.append("             AND S1.APPLICANTDIV = '" + param._applicantDiv + "' ");
            if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
                stb3.append("             AND S1.TESTDIV = '" + testdiv + "' ");
            }
            stb3.append("             AND S1.SCORE2 IS NOT NULL ");
            stb3.append("         GROUP BY ");
            stb3.append("             S1.RECEPTNO ");
            stb3.append("     ) S1 ON S1.RECEPTNO = R1.RECEPTNO ");
            stb3.append("     LEFT JOIN ( ");
            stb3.append("         SELECT ");
            stb3.append("             BD_007.EXAMNO, ");
            stb3.append("             '1' AS ENT_FLG, ");
            stb3.append("             BD_007.REMARK4 AS TESTDIV ");
            stb3.append("         FROM ");
            stb3.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
            stb3.append("         WHERE ");
            stb3.append("             BD_007.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb3.append("             AND BD_007.APPLICANTDIV = '" + param._applicantDiv + "' ");
            stb3.append("             AND BD_007.SEQ = '007' ");
            stb3.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
            stb3.append("                 SELECT ");
            stb3.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
            stb3.append("                 FROM ");
            stb3.append("                     ENTEXAM_COURSE_MST C1 ");
            stb3.append("                 WHERE ");
            stb3.append("                     C1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb3.append("                     AND C1.APPLICANTDIV = '" + param._applicantDiv + "' ");
            if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
                stb3.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
            }
            stb3.append("                     AND C1.ENTER_COURSECODE = '" + examcourseDiv + "' ");
            stb3.append("                 GROUP BY ");
            stb3.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
            stb3.append("             ) ");
            stb3.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
            stb3.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_014 ");
            stb3.append("       ON RD_014.ENTEXAMYEAR 	= RD_007.ENTEXAMYEAR ");
            stb3.append("       AND RD_014.APPLICANTDIV 	= RD_007.APPLICANTDIV ");
            stb3.append("       AND RD_014.TESTDIV 		= RD_007.TESTDIV ");
            stb3.append("       AND RD_014.EXAM_TYPE 	= RD_007.EXAM_TYPE ");
            stb3.append("       AND RD_014.RECEPTNO 		= RD_007.RECEPTNO ");
            stb3.append("       AND RD_014.SEQ 		    = '014' ");
            stb3.append(" WHERE ");
            stb3.append("     RD_007.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb3.append("     AND RD_007.APPLICANTDIV = '" + param._applicantDiv + "' ");
            if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
                stb3.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
            }
            stb3.append("     AND RD_007.SEQ = '007' ");
            if (!"ALL".equals(param._hallCd)) {
                stb3.append("     AND RD_014.REMARK1 = '" + param._hallCd + "' ");
            }

            return stb3.toString();
        }
    }

    private class PrintData1 {
        final String _examcourseCd;
        final String _testdiv;
        final String _hopeCourseDiv;
        String _recruit;
        Count _hope;
        Count _exam;
        Count _pass;
        Count _ent;

        public PrintData1(
                final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String recruit,
                final Count hope,
                final Count exam,
                final Count pass,
                final Count ent
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _recruit = recruit;
            _hope = hope;
            _exam = exam;
            _pass = pass;
            _ent = ent;
        }

        public PrintData1() {
            _examcourseCd = null;
            _testdiv = null;
            _hopeCourseDiv = null;
            _recruit = null;
            _hope = new Count();
            _exam = new Count();
            _pass = new Count();
            _ent = new Count();
        }

        public void add(final PrintData1 printData1) {
            log.info(" _examcourseCd = " + _examcourseCd + ", _testdiv = " + _testdiv + ", _hopeCourseDiv = " + _hopeCourseDiv);
            log.info("   add _recruit " + _recruit + " + " + printData1._recruit + " = " + addNum(_recruit, printData1._recruit));
            _recruit = addNum(_recruit, printData1._recruit);
            log.info("   add _hope " + _hope + " + " + printData1._hope + " = " + _hope.add(printData1._hope));
            _hope = _hope.add(printData1._hope);
            log.info("   add _exam " + _exam + " + " + printData1._exam + " = " + _exam.add(printData1._exam));
            _exam = _exam.add(printData1._exam);
            log.info("   add _pass " + _pass + " + " + printData1._pass + " = " + _pass.add(printData1._pass));
            _pass = _pass.add(printData1._pass);
            log.info("   add _ent " + _ent + " + " + printData1._ent + " = " + _ent.add(printData1._ent));
            _ent = _ent.add(printData1._ent);
        }

        public String toString() {
            return "PD1(recruit = " + _recruit + ", hope = " + _hope + ", exam = " + _exam + ", pass = " + _pass + ", ent = " + _ent + ")";
        }
    }

    private static class Count {
        String _1;
        String _2;
        String _3;
        Count() {
            this("0", "0", "0");
        }
        Count(final String _1, final String _2, final String _3) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }
        Count add(final Count c) {
            return new Count(
                    addNum(_1, c._1),
                    addNum(_2, c._2),
                    addNum(_3, c._3)
                    );
        }
        public String toString() {
            return "(" + _1 + ", " + _2 + ", " + _3 + ")";
        }
    }
    private class PrintData2 {
        final String _examcourseCd;
        final String _testdiv;
        final String _hopeCourseDiv;
        final String _div;
        Count _hope;
        Count _exam;
        Count _pass;
        Count _ent;

        public PrintData2(
                final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String div,
                final Count hope,
                final Count exam,
                final Count pass,
                final Count ent
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _div = div;
            _hope = hope;
            _exam = exam;
            _pass = pass;
            _ent = ent;
        }

        public PrintData2() {
            _examcourseCd = null;
            _testdiv = null;
            _hopeCourseDiv = null;
            _div = null;
            _hope = new Count();
            _exam = new Count();
            _pass = new Count();
            _ent = new Count();
        }

        public void add(final PrintData2 printData2) {
            _hope = _hope.add(printData2._hope);
            _exam = _exam.add(printData2._exam);
            _pass = _pass.add(printData2._pass);
            _ent = _ent.add(printData2._ent);
        }

        public String toString() {
            return "PD2(hope = " + _hope + ", exam = " + _exam + ", pass = " + _pass + ", ent = " + _ent + ")";
        }
    }

    private class PrintData3 {
        final String _examcourseCd;
        final String _testdiv;
        final String _testsubclasscd;
        final String _examCnt;
        final String _perfect;
        final String _scoreAvgSex1;
        final String _scoreAvgSex2;
        final String _scoreAvg;
        final String _scoreStddev;
        final String _scoreMax;
        final String _scoreMin;
        final String _passAvg;
        final String _tokushouMin1;
        final String _tokushouMin2;
        final String _tokushouMin3;

        public PrintData3(
                final String examcourseCd,
                final String testdiv,
                final String testsubclasscd,
                final String examCnt,
                final String perfect,
                final String scoreAvgSex1,
                final String scoreAvgSex2,
                final String scoreAvg,
                final String scoreStddev,
                final String scoreMax,
                final String scoreMin,
                final String passAvg,
                final String tokushouMin1,
                final String tokushouMin2,
                final String tokushouMin3
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _testsubclasscd = testsubclasscd;
            _examCnt = examCnt;
            _perfect = perfect;
            _scoreAvgSex1 = scoreAvgSex1;
            _scoreAvgSex2 = scoreAvgSex2;
            _scoreAvg = scoreAvg;
            _scoreStddev = scoreStddev;
            _scoreMax = scoreMax;
            _scoreMin = scoreMin;
            _passAvg = passAvg;
            _tokushouMin1 = tokushouMin1;
            _tokushouMin2 = tokushouMin2;
            _tokushouMin3 = tokushouMin3;
        }
    }

    private class TestdivMst {
        final String _testdiv;
        final String _testdivAbbv;
        final String _testdivDate;
        final int _line;

        public TestdivMst(
                final String testdiv,
                final String testdivAbbv,
                final String testdivDate,
                final int line
        ) {
            _testdiv = testdiv;
            _testdivAbbv = testdivAbbv;
            _testdivDate = testdivDate;
            _line = line;
        }
    }

    private class TestSubclass {
        final String _subclassCd;
        final String _subclassName;

        public TestSubclass(
                final String subclassCd,
                final String subclassName
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }

    private static class HopeCourse {
        final Map<String, List<String>> _map;
        HopeCourse(final Map<String, List<String>> map) {
            _map = map;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77379 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _loginDate;
        private final String _loginYear;
        private final String _hallCd;
        final String _applicantdivName;
        final String _nendo;
        private final Map<String, String> _examcourseDivMap;
        private final Map<String, HopeCourse> _hopeCourseCdMap;
        private final Map<String, TestdivMst> _testdivMstMap;
        private final Map<String, String> _sexMap;
        private final Map<String, List<TestSubclass>> _testsubclassMap;
        private String _currentForm;
        private final Map<String, Map<String, SvfField>> _svfFormFieldInfoMap = new HashMap<String, Map<String, SvfField>>();
        final List<String> _hopeCourseDivList = new ArrayList();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _hallCd = request.getParameter("HALL_CD");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entexamyear)) + "年度";
            _examcourseDivMap = getExamcourseDivMap();
            _hopeCourseCdMap = getHopecourseCdMap();
            _testdivMstMap = getTestdivMstMap(db2);
            _sexMap = getSexMap(db2);
            _testsubclassMap = getTestsubclassMap(db2);
            _hopeCourseDivList.add(HOPE_COURSE_ALL);
            _hopeCourseDivList.add(HOPE_COURSE_4);
            _hopeCourseDivList.add(HOPE_COURSE_3);
            _hopeCourseDivList.add(HOPE_COURSE_4_2);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map<String, String> getExamcourseDivMap() {
            final Map<String, String> retMap = new TreeMap();

            retMap.put(COURSEDIV_AE, "アドバンスト叡智コース");
            retMap.put(COURSEDIV_EE, "エッセンシャル叡智コース");
            retMap.put(COURSEDIV_ALL, "全体合計");

            return retMap;
        }

        private Map<String, HopeCourse> getHopecourseCdMap() {
            final Map<String, HopeCourse> retMap = new TreeMap();

            //AE
            Map<String, List<String>> aeMap1 = new TreeMap();
            aeMap1.put(HOPE_COURSE_ALL, Arrays.asList("1001"));
            aeMap1.put(HOPE_COURSE_4, Arrays.asList("1001"));
            aeMap1.put(HOPE_COURSE_3, Arrays.asList(""));
            aeMap1.put(HOPE_COURSE_4_2, Arrays.asList("1001"));
            retMap.put(COURSEDIV_AE + "-" + "1" , new HopeCourse(aeMap1));
            retMap.put(COURSEDIV_AE + "-" + "2" , new HopeCourse(aeMap1));
            Map<String, List<String>> aeMap2 = new TreeMap();
            aeMap2.put(HOPE_COURSE_ALL, Arrays.asList("1004"));
            aeMap2.put(HOPE_COURSE_4, Arrays.asList("1004"));
            aeMap2.put(HOPE_COURSE_3, Arrays.asList(""));
            aeMap2.put(HOPE_COURSE_4_2, Arrays.asList("1004"));
            retMap.put(COURSEDIV_AE + "-" + "3" , new HopeCourse(aeMap2));
            retMap.put(COURSEDIV_AE + "-" + "4" , new HopeCourse(aeMap2));
            Map<String, List<String>> aeMapAll = new TreeMap();
            aeMapAll.put(HOPE_COURSE_ALL, Arrays.asList("1001", "1004"));
            aeMapAll.put(HOPE_COURSE_4, Arrays.asList("1001", "1004"));
            aeMapAll.put(HOPE_COURSE_3, Arrays.asList(""));
            aeMapAll.put(HOPE_COURSE_4_2, Arrays.asList("1001", "1004"));
            retMap.put(COURSEDIV_AE + "-" + "5" , new HopeCourse(aeMapAll));
            retMap.put(COURSEDIV_AE + "-" + "6" , new HopeCourse(aeMapAll));

            //EE
            Map<String, List<String>> eeMap1 = new TreeMap();
            eeMap1.put(HOPE_COURSE_ALL, Arrays.asList("1002", "1003"));
            eeMap1.put(HOPE_COURSE_4, Arrays.asList("1002"));
            eeMap1.put(HOPE_COURSE_3, Arrays.asList("1003"));
            eeMap1.put(HOPE_COURSE_4_2, Arrays.asList("1002"));
            retMap.put(COURSEDIV_EE + "-" + "1" , new HopeCourse(eeMap1));
            retMap.put(COURSEDIV_EE + "-" + "2" , new HopeCourse(eeMap1));
            Map<String, List<String>> eeMap2 = new TreeMap();
            eeMap2.put(HOPE_COURSE_ALL, Arrays.asList("1005"));
            eeMap2.put(HOPE_COURSE_4, Arrays.asList("1005"));
            eeMap2.put(HOPE_COURSE_3, Arrays.asList(""));
            eeMap2.put(HOPE_COURSE_4_2, Arrays.asList("1005"));
            retMap.put(COURSEDIV_EE + "-" + "3" , new HopeCourse(eeMap2));
            retMap.put(COURSEDIV_EE + "-" + "4" , new HopeCourse(eeMap2));
            Map<String, List<String>> eeMapAll = new TreeMap();
            eeMapAll.put(HOPE_COURSE_ALL, Arrays.asList("1002", "1003", "1005"));
            eeMapAll.put(HOPE_COURSE_4, Arrays.asList("1002", "1005"));
            eeMapAll.put(HOPE_COURSE_3, Arrays.asList("1003"));
            eeMapAll.put(HOPE_COURSE_4_2, Arrays.asList("1002", "1005"));
            retMap.put(COURSEDIV_EE + "-" + "5" , new HopeCourse(eeMapAll));
            retMap.put(COURSEDIV_EE + "-" + "6" , new HopeCourse(eeMapAll));

            //全体合計
            Map<String, List<String>> allMap1 = new TreeMap();
            allMap1.put(HOPE_COURSE_ALL, Arrays.asList("1002", "1003"));
            allMap1.put(HOPE_COURSE_4, Arrays.asList("1002"));
            allMap1.put(HOPE_COURSE_3, Arrays.asList("1003"));
            allMap1.put(HOPE_COURSE_4_2, Arrays.asList("1002"));
            retMap.put(COURSEDIV_ALL + "-" + "1" , new HopeCourse(allMap1));
            retMap.put(COURSEDIV_ALL + "-" + "2" , new HopeCourse(allMap1));
            Map<String, List<String>> allMap2 = new TreeMap();
            allMap2.put(HOPE_COURSE_ALL, Arrays.asList("1005"));
            allMap2.put(HOPE_COURSE_4, Arrays.asList("1005"));
            allMap2.put(HOPE_COURSE_3, Arrays.asList(""));
            allMap2.put(HOPE_COURSE_4_2, Arrays.asList("1005"));
            retMap.put(COURSEDIV_ALL + "-" + "3" , new HopeCourse(allMap2));
            retMap.put(COURSEDIV_ALL + "-" + "4" , new HopeCourse(allMap2));
            Map<String, List<String>> allMapAll = new TreeMap();
            allMapAll.put(HOPE_COURSE_ALL, Arrays.asList("1002", "1003", "1005"));
            allMapAll.put(HOPE_COURSE_4, Arrays.asList("1002", "1005"));
            allMapAll.put(HOPE_COURSE_3, Arrays.asList("1003"));
            allMapAll.put(HOPE_COURSE_4_2, Arrays.asList("1002", "1005"));
            retMap.put(COURSEDIV_ALL + "-" + "5" , new HopeCourse(allMapAll));
            retMap.put(COURSEDIV_ALL + "-" + "6" , new HopeCourse(allMapAll));

            return retMap;
        }

        private String getExamcourseSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     EXAMCOURSECD, ");
            stb.append("     EXAMCOURSE_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     EXAMCOURSECD ");
            return stb.toString();
        }

        private Map getTestdivMstMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTestdivMstSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int line = 0;
                while (rs.next()) {
                    final String testdiv = rs.getString("TESTDIV");
                    final String testdivAbbv = rs.getString("TESTDIV_ABBV");
                    final String testdivDate = rs.getString("TESTDIV_DATE");
                    line++;
                    if (line >= 5) continue;

                    final TestdivMst testdivMst = new TestdivMst(testdiv, testdivAbbv, testdivDate, line);
                    retMap.put(testdiv, testdivMst);
                }
                retMap.put(TESTDIV_NOBE, new TestdivMst(TESTDIV_NOBE, "延人数", null, 5));
                retMap.put(TESTDIV_JITSU, new TestdivMst(TESTDIV_JITSU, "実人数", null, 6));
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getTestdivMstSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS TESTDIV, ");
            stb.append("     ABBV1 AS TESTDIV_ABBV, ");
            stb.append("     NAMESPARE1 AS TESTDIV_DATE ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'L024' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map<String, String> getSexMap(final DB2UDB db2) {
            final Map<String, String> retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSexSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");

                    retMap.put(sex, sexName);
                }
                retMap.put("3", "合計");
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getSexSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS SEX, ");
            stb.append("     NAME1 AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'Z002' ");
            stb.append(" ORDER BY ");
            stb.append("     SEX ");
            return stb.toString();
        }

        private Map<String, List<TestSubclass>> getTestsubclassMap(final DB2UDB db2) {
            final Map<String, List<TestSubclass>> retMap = new TreeMap<String, List<TestSubclass>>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                Map<String, String[]> goukeiNamesMap = new TreeMap();
                int maxCnt = 0;
                String befCourseTestDiv = "";
                final String sql = getTestsubclassSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseDiv = rs.getString("ENTER_COURSECODE");
                    final String testdiv = rs.getString("TESTDIV");
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String testsubclassName = rs.getString("TESTSUBCLASS_NAME");
                    final String courseNamesStr = rs.getString("EXAMCOURSE_NAMES");

                    //各試験回の受験コースを取得(複数コースの場合あり)
                    if (!befCourseTestDiv.equals(courseDiv + "-" + testdiv)) {
                        maxCnt = 0;
                    }
                    String[] courseNameArray = StringUtils.split(courseNamesStr, ",");
                    if (courseNameArray.length > maxCnt) {
                        maxCnt = courseNameArray.length;
                        goukeiNamesMap.put(courseDiv + "-" + testdiv, courseNameArray);
                    }

                    final TestSubclass testSubclass = new TestSubclass(testsubclasscd, testsubclassName);
                    if (!retMap.containsKey(courseDiv + "-" + testdiv)) {
                        retMap.put(courseDiv + "-" + testdiv,  new ArrayList());
                    }
                    List<TestSubclass> subclassList = retMap.get(courseDiv + "-" + testdiv);
                    subclassList.add(testSubclass);

                    befCourseTestDiv = courseDiv + "-" + testdiv;
                }

                //コース毎合計用の科目CDを追加
                for (final String key : retMap.keySet()) {
                    final List<TestSubclass> subclassList = retMap.get(key);
                    String[] courseNames = goukeiNamesMap.get(key);

                    for (String goukeiCourseName : courseNames) {
                        final String[] courseCdAndName = StringUtils.split(goukeiCourseName, "-");
                        if (courseCdAndName.length != 2) continue;
                        final TestSubclass testSubclass = new TestSubclass(courseCdAndName[0], courseCdAndName[1]);
                        subclassList.add(testSubclass);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getTestsubclassSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append("    SELECT ");
            stb.append("      T2.ENTER_COURSECODE, ");
            stb.append("      T1.TESTDIV, ");
            stb.append("      T1.TESTSUBCLASSCD, ");
            stb.append("      T3.NAME2 AS TESTSUBCLASS_NAME,  ");
            stb.append("      LISTAGG(T2.EXAMCOURSECD || '-' || T2.EXAMCOURSE_NAME, ',') WITHIN GROUP (ORDER BY T2.EXAMCOURSECD) AS EXAMCOURSE_NAMES ");
            stb.append("    FROM ");
            stb.append("      ENTEXAM_PERFECT_MST T1 ");
            stb.append("      INNER JOIN ENTEXAM_COURSE_MST T2 ");
            stb.append("        ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("        AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("        AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append("      LEFT JOIN V_NAME_MST T3 ");
            stb.append("      	ON T3.YEAR = T1.ENTEXAMYEAR ");
            stb.append("    	AND T3.NAMECD2 = T1.TESTSUBCLASSCD ");
            stb.append("    	AND T3.NAMECD1 = 'L009' ");
            stb.append("    	AND T3.NAME2 IS NOT NULL ");
            stb.append("    WHERE ");
            stb.append("      T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("      AND T1.APPLICANTDIV = '1' ");
            stb.append("      AND T1.TESTSUBCLASSCD <> 'A' "); //面接除く
            stb.append("    GROUP BY ");
            stb.append("      T2.ENTER_COURSECODE, ");
            stb.append("      T1.TESTDIV, ");
            stb.append("      T1.TESTSUBCLASSCD, ");
            stb.append("      T3.NAME2 ");
            stb.append(" UNION ");
            stb.append("    SELECT ");
            stb.append("      '9999' AS ENTER_COURSECODE, ");
            stb.append("      T1.TESTDIV, ");
            stb.append("      T1.TESTSUBCLASSCD, ");
            stb.append("      T3.NAME2 AS TESTSUBCLASS_NAME,  ");
            stb.append("      LISTAGG(T2.EXAMCOURSECD || '-' || T2.EXAMCOURSE_NAME, ',') WITHIN GROUP (ORDER BY T2.EXAMCOURSECD) AS EXAMCOURSE_NAMES ");
            stb.append("    FROM ");
            stb.append("      ENTEXAM_PERFECT_MST T1 ");
            stb.append("      INNER JOIN ENTEXAM_COURSE_MST T2 ");
            stb.append("        ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("        AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("        AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append("      LEFT JOIN V_NAME_MST T3 ");
            stb.append("      	ON T3.YEAR = T1.ENTEXAMYEAR ");
            stb.append("    	AND T3.NAMECD2 = T1.TESTSUBCLASSCD ");
            stb.append("    	AND T3.NAMECD1 = 'L009' ");
            stb.append("    	AND T3.NAME2 IS NOT NULL ");
            stb.append("    WHERE ");
            stb.append("      T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("      AND T1.APPLICANTDIV = '1' ");
            stb.append("      AND T1.TESTSUBCLASSCD <> 'A' "); //面接除く
            stb.append("    GROUP BY ");
            stb.append("      T1.TESTDIV, ");
            stb.append("      T1.TESTSUBCLASSCD, ");
            stb.append("      T3.NAME2 ");
            stb.append(" ORDER BY ");
            stb.append("   ENTER_COURSECODE, ");
            stb.append("   TESTDIV, ");
            stb.append("   TESTSUBCLASSCD ");

            return stb.toString();
        }
    }
}

// eof


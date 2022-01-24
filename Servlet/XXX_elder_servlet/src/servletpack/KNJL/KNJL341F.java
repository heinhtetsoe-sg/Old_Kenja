package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL341F {

    private static final Log log = LogFactory.getLog(KNJL341F.class);

    private boolean _hasData;
    private Param _param;

    private final String FROM_TO_MARK = "\uFF5E";
    private final String EXAM_TYPE1 = "1";
    private final String EXAM_TYPE2 = "2";
    private final String EXAM_TYPE3 = "3";
    private final String EXAM_TYPE4 = "4";
    private final String EXAM_TYPE5 = "5";
    private final String EXAM_TYPE6 = "6";
    private final String EXAM_TYPE7 = "7";
    private final String EXAM_TYPE8 = "8";
    private static final String EXAM_TYPE_TOTAL = "EXAM_TYPE_TOTAL";
    private static final String SPECIAL_REASON_DIV = "SPECIAL_REASON_DIV";

    private static final String SHUTSUGAN = "1";
    private static final String TETSUZUKI = "TETSUZUKI";
    private static final String SONOTA = "SONOTA";

    private static final String ZENJITSU = "ZENJITSU"; // 前日
    private static final String HONJITSU = "HONJITSU"; // 本日
    private static final String HONJITSU_JITAI = "HONJITSU_JITAI"; // 本日辞退
    private static final String ZENJITSU_HONJITSU = "ZENJITSU_HONJITSU"; // 前日+本日
    private static final String KESSEKI = "KESSEKI"; // 欠席者
    private static final String JITSU_JUKEN = "4"; // 実受検者
    private static final String GOKAKU = "5"; // 合格者
    private static final String TOKUTAI = "6"; // 特待生
    private static final String FUGOKAKU = "7"; // 不合格者
    private static final String SLIDE_LEFT = "8"; // スライド合格左
    private static final String SLIDE_RIGHT = "9"; // スライド合格右

    private static final String TESTDIV_NO_J = "1"; // 中学のテスト回数は固定1

    private static final String TESTDIV0_1 = "1";
    private static final String TESTDIV0_2 = "2";
    private static final String TESTDIV0_3 = "3";
    private static final String TESTDIV0_4 = "4";
    private static final String TESTDIV0_5 = "5";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 70829 $ $Date: 2019-11-22 14:39:54 +0900 (金, 22 11 2019) $"); // CVSキーワードの取り扱いに注意

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            if ("1".equals(_param._applicantdiv)) {
                printJ(svf, db2);
            } else if ("2".equals(_param._applicantdiv)) {
                printH(svf, db2);
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedHashMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
//            log.info(" create map : key = " + key1);
        }
        return (Map) map.get(key1);
    }

    private static Date getDate(final Map m, final String field, final Param param) {
        if (null == getString(m, field)) {
            return null;
        }
        return parseDate(getString(m, field), param);
    }


    private static Date parseDate(final String dateStr, final Param param) {
        if (null == dateStr) {
            return null;
        }
        try {
            return param.df.parse(dateStr.replace('/', '-'));
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        return null;
    }

    private static List mapSetField(final List mapList, String[] field) {
        final List list = new ArrayList();
        for (final Iterator it = mapList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final Map newm = new HashMap();
            list.add(newm);
            for (int i = 0; i < field.length; i++) {
                newm.put(field[i], getString(m, field[i]));
            }
        }
        return list;
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        try {
            if (!m.containsKey(field)) {
                throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private static Map swap(final Map m) {
        final Map newm = new HashMap();
        for (final Iterator it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            newm.put(e.getValue(), e.getKey());
        }
        return newm;
    }

    private static Map toMap(final List mapList, final String keyField, final String valueField) {
        final Map rtn = new HashMap();
        for (final Iterator it = mapList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            rtn.put(getString(map, keyField), getString(map, valueField));
        }
        return rtn;
    }

    private static List getList(final DB2UDB db2, final String sql) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnLabel(i), rs.getString(i));
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static String quote(final String s) {
        if (null == s) {
            return null;
        }
        return "'" + s + "'";
    }

    private static String add(final String s1, final String s2) {
        if (!NumberUtils.isNumber(s1)) { return s2; }
        if (!NumberUtils.isNumber(s2)) { return s1; }
        return String.valueOf(Integer.parseInt(s1) + Integer.parseInt(s2));
    }

    private static String subtract(final String s1, final String s2) {
        if (!NumberUtils.isNumber(s1) && !NumberUtils.isNumber(s2)) {
            return null;
        }
        final int v1 = NumberUtils.isNumber(s1) ? Integer.parseInt(s1) : 0;
        final int v2 = NumberUtils.isNumber(s2) ? Integer.parseInt(s2) : 0;
        return String.valueOf(v1 - v2);
    }

    private void printH(final Vrw32alp svf, final DB2UDB db2) {
        final String TOTAL = "TOTAL";
        final String EXAM_TYPE1 = "1";

        final String form = "KNJL341F_H1.frm";
        svf.VrSetForm(form, 4);

        final List courseList = new ArrayList(_param._entexamcourseList);
        final Map examcourseTotal = new HashMap(); // 計表示用
        examcourseTotal.put("EXAMCOURSECD", TOTAL);
        courseList.add(examcourseTotal);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　高等学校入試応募/手続状況一覧"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._toujituDate)); // 印刷日

        for (int testdivi = 1; testdivi <= 7; testdivi++) {
            final String testdiv = String.valueOf(testdivi);
            final TestDivDat testDivDat = (TestDivDat) _param._testdivMap.get(testdiv);
            if (null == testDivDat) {
                continue;
            }
            if ("1".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"Ａ推薦", "(推薦含む)"});
            } else if ("2".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"第１回", "Ｂ推薦"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_2, new String[] {"第２回", "Ｂ推薦"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_3, new String[] {"第３回", "Ｂ推薦"});
            } else if ("3".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"第１回", "一般", "(併願優遇含む)"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_2, new String[] {"第２回", "一般", "(併願優遇含む)"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_3, new String[] {"第３回", "一般", "(併願優遇含む)"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_4, new String[] {"第１回", "２次募集", ""});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_5, new String[] {"第２回", "２次募集", ""});
            } else if ("7".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"第１回", "一般特別"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_2, new String[] {"第２回", "一般特別"});
            } else if ("4".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"帰国生入試", "第１回Ａ方式"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_2, new String[] {"帰国生入試", "第２回Ａ方式"});
            } else if ("5".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"帰国生入試", "第１回Ｂ方式"});
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_2, new String[] {"帰国生入試", "第２回Ｂ方式"});
            } else if ("6".equals(testdiv)) {
                testDivDat._testdiv0NameArrayMap.put(TESTDIV0_1, new String[] {"一貫生"});
            }
        }

        // 右 手続辞退者
        final List jitaiExamnoList = getJitaiExamnoList(db2);
        for (int i = 0; i < jitaiExamnoList.size(); i++) {
            final int line = i + 1;
            svf.VrsOutn("DECLENSION_EXAM_NO", line, (String) jitaiExamnoList.get(i)); // 手続辞退者
        }

        // 右 手続者合計
        final Map tokeiMap         = Tokei.getTokeiMap(db2, _param, _param._entexamyear,     _param._toujituDate);
        final Map lastyearTokeiMap = Tokei.getTokeiMap(db2, _param, _param._lastEntexamyear, _param._lastYearToujituDate);
        String total = null;
        for (int i = 0; i < courseList.size(); i++) {
            final int line = i + 1;
            final Map courseMap = (Map) courseList.get(i);
            final String examcoursecd = getString(courseMap, "EXAMCOURSECD");
            if (TOTAL.equals(examcoursecd)) {
                svf.VrsOutn("COURSE_NAME2_1", line, "合計"); // コース名
                svf.VrsOutn("PROCE_NUM_TOTAL", line, total); // 手続者合計
            } else {
                final String courseName = (String) courseMap.get("EXAMCOURSE_NAME");
                if (getMS932Bytecount(courseName) > 12) {
                    svf.VrsOutn("COURSE_NAME2_2_1", line, courseName); // コース名
                } else {
                    svf.VrsOutn("COURSE_NAME2_1", line, courseName); // コース名
                }
                final Set examnoSet = new HashSet();
                String gokakuCount = null;
                for (final Iterator it = _param._testdivMap.values().iterator(); it.hasNext();) {
                    final TestDivDat testDivDat = (TestDivDat) it.next();
                    for (final Iterator it2 = testDivDat._testdiv0NameArrayMap.keySet().iterator(); it2.hasNext();) {
                        final String testdiv0 = (String) it2.next();

                        for (int chki = 0; chki < courseList.size(); chki++) {
                            final Map checkCourseMap = (Map) courseList.get(chki);
                            final String checkExamcoursecd = getString(checkCourseMap, "EXAMCOURSECD");

                            final List targetList = new ArrayList();
                            targetList.addAll(Tokei.getTokei(tokeiMap, TETSUZUKI, _param._entexamyear, testDivDat._testdiv, testdiv0, EXAM_TYPE_TOTAL, checkExamcoursecd).getRemarkList(ZENJITSU)); // 前日
                            targetList.addAll(Tokei.getTokei(tokeiMap, TETSUZUKI, _param._entexamyear, testDivDat._testdiv, testdiv0, EXAM_TYPE_TOTAL, checkExamcoursecd).getRemarkList(HONJITSU)); // 当日
                            final List honjitsuJitaiList = Tokei.getTokei(tokeiMap, TETSUZUKI, _param._entexamyear, testDivDat._testdiv, testdiv0, EXAM_TYPE_TOTAL, checkExamcoursecd).getRemarkList(HONJITSU_JITAI); // 当日辞退
                            for (final Iterator mit = targetList.iterator(); mit.hasNext();) {
                                final Map m = (Map) mit.next();
                                if (null != getString(m, "SUC_COURSECODE") && !getString(m, "SUC_COURSECODE").equals(examcoursecd)) {
                                    continue;
                                }
                                if (honjitsuJitaiList.contains(m)) {
                                    continue;
                                }
                                examnoSet.add(getString(m, "EXAMNO"));
                            }
                        }
                        gokakuCount = String.valueOf(examnoSet.size());

                    }
                }
                if (null != gokakuCount) {
                    svf.VrsOutn("PROCE_NUM_TOTAL", line, gokakuCount); // 手続者合計
                    total = add(total, gokakuCount);
                }
            }
        }

        // 右 特別措置者合計
        int tokubetuSochiLine = 0;
        for (final Iterator it = _param._testdivMap.values().iterator(); it.hasNext();) {
            final TestDivDat testDivDat = (TestDivDat) it.next();
            for (final Iterator it2 = testDivDat._testdiv0NameArrayMap.keySet().iterator(); it2.hasNext();) {
                final String testdiv0 = (String) it2.next();

                String tuishiDate = null;
                if ("1".equals(testdiv0)) {
                    tuishiDate = testDivDat._tuishiDate1;
                } else if ("2".equals(testdiv0)) {
                    tuishiDate = testDivDat._tuishiDate2;
                }
                if (null == tuishiDate || parseDate(tuishiDate, _param).after(parseDate(_param._toujituDate, _param))) {
                    // 指定日付以前の追試を表示しない
                    continue;
                }

                for (int i = 0; i < courseList.size(); i++) {
                    final Map courseMap = (Map) courseList.get(i);
                    final String examcoursecd = getString(courseMap, "EXAMCOURSECD");

                    final Set examnoSet = new HashSet();
                    final List targetList = new ArrayList();
                    targetList.addAll(Tokei.getTokei(tokeiMap, SONOTA, _param._entexamyear, testDivDat._testdiv, testdiv0, EXAM_TYPE_TOTAL, examcoursecd).getRemarkList(SPECIAL_REASON_DIV)); // 特別措置者
                    for (final Iterator mit = targetList.iterator(); mit.hasNext();) {
                        final Map m = (Map) mit.next();
                        examnoSet.add(getString(m, "EXAMNO")); // 追試者
                    }
                    if (!examnoSet.isEmpty()) {

                        final String[] nameArray = (String[]) testDivDat._testdiv0NameArrayMap.get(testdiv0);
                        final StringBuffer stb = new StringBuffer();
                        if (null != nameArray) {
                            for (int j = 0; j < nameArray.length; j++) {
                                stb.append(nameArray[j]);
                            }
                        }

                        final String courseName = (String) courseMap.get("EXAMCOURSE_NAME");
                        tokubetuSochiLine += 1;
                        svf.VrsOutn("SPECIAL_REASON_TESTNAME", tokubetuSochiLine, stb.toString()); // 特別措置入試名
                        if (null != tuishiDate) {
                            svf.VrsOutn("SPECIAL_REASON_DATE", tokubetuSochiLine, "(" + tuishiDate.replace('-', '/') + ")"); // 特別措置入試名
                        }
                        svf.VrsOutn("SPECIAL_REASON_COURSE", tokubetuSochiLine, courseName); // 特別措置者コース名
                        svf.VrsOutn("SPECIAL_REASON_COUNT", tokubetuSochiLine, String.valueOf(examnoSet.size())); // 特別措置者合計
                    }
                }
            }
        }

        final Tokei tokeiTotalKind1          = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind1LastYear1 = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind1LastYear2 = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind2          = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind2LastYear1 = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind2LastYear2 = new Tokei(null, null, null, null, null, null);
        final Tokei tokeiTotalKind3          = new Tokei(null, null, null, null, null, null);
        // 入試区分ごとに表示
        for (final Iterator it = _param._testdivMap.values().iterator(); it.hasNext();) {
            final TestDivDat testDivDat = (TestDivDat) it.next();

            if ("6".equals(testDivDat._testdiv)) {
                // 一貫生の前に、一貫生以外の合計を表示
                svf.VrsOut("TOTAL_NAME", "合計"); // 合計名称
                svf.VrsOut("TOTAL_APPLI_NUM1",      tokeiTotalKind1.getRemark(ZENJITSU)); // 合計出願数
                svf.VrsOut("TOTAL_APPLI_NUM2",      tokeiTotalKind1.getRemark(HONJITSU)); // 合計出願数
                svf.VrsOut("TOTAL_APPLI_NUM3",      tokeiTotalKind1.getRemark(ZENJITSU_HONJITSU)); // 合計出願数
                svf.VrsOut("TOTAL_LAST_APPLI_NUM1", tokeiTotalKind1LastYear2.getRemark(HONJITSU)); // 合計前年度出願数
                svf.VrsOut("TOTAL_LAST_APPLI_NUM2", tokeiTotalKind1LastYear1.getRemark(ZENJITSU_HONJITSU)); // 合計前年度出願数

                svf.VrsOut("TOTAL_PROCE_NUM1",      tokeiTotalKind2.getRemark(ZENJITSU)); // 合計手続人数
                svf.VrsOut("TOTAL_PROCE_NUM2",      tetsuzukiHonjitsu(tokeiTotalKind2.getRemark(HONJITSU), tokeiTotalKind2.getRemark(HONJITSU_JITAI))); // 合計手続人数
                svf.VrsOut("TOTAL_PROCE_NUM3",      tokeiTotalKind2.getRemark(ZENJITSU_HONJITSU)); // 合計手続人数
                svf.VrsOut("TOTAL_LAST_PROCE_NUM1", tokeiTotalKind2LastYear2.getRemark(HONJITSU)); // 合計前年度手続人数
                svf.VrsOut("TOTAL_LAST_PROCE_NUM2", tokeiTotalKind2LastYear1.getRemark(ZENJITSU_HONJITSU)); // 合計前年度手続人数

                svf.VrsOut("TOTAL_ABSENCE",       tokeiTotalKind3.getRemark(KESSEKI)); // 合計欠席
                svf.VrsOut("TOTAL_REAL_EXAM_NUM", tokeiTotalKind3.getRemark(JITSU_JUKEN)); // 合計実受験者
//                svf.VrsOut("TOTAL_SLIDE1",        tokeiTotalKind3.getRemark(SLIDE_LEFT)); // 合計スライド合格
//                svf.VrsOut("TOTAL_SLIDE2",        tokeiTotalKind3.getRemark(SLIDE_RIGHT)); // 合計スライド合格
                svf.VrsOut("TOTAL_PASS",          tokeiTotalKind3.getRemark(GOKAKU)); // 合計合格
                svf.VrsOut("TOTAL_REJECT",        tokeiTotalKind3.getRemark(FUGOKAKU)); // 合計不合格
                svf.VrEndRecord();

                svf.VrsOut("BLANK", "DUMMY"); // 空行
                svf.VrEndRecord();
            }

            for (final Iterator it2 = testDivDat._testdiv0NameArrayMap.entrySet().iterator(); it2.hasNext();) {
                final Map.Entry e = (Map.Entry) it2.next();
                final String testdiv0 = (String) e.getKey();
                final String[] nameArray = (String[]) e.getValue();

                if (null != nameArray) {
                    for (int j = 0; j < nameArray.length; j++) {
                        svf.VrsOut("EXAM_NAME" + String.valueOf(j + 1), nameArray[j]); // 試験名称
                    }
                }

                // 高校は第1回:NAMESPARE1, 第2回:NAME3
                String testDate = null;
                if (TESTDIV0_1.equals(testdiv0)) {
                    testDate =  testDivDat._date1;
                } else if (TESTDIV0_2.equals(testdiv0)) {
                    testDate =  testDivDat._date2;
                } else if (TESTDIV0_3.equals(testdiv0)) {
                    testDate =  testDivDat._date3;
                } else if (TESTDIV0_4.equals(testdiv0)) {
                    testDate =  testDivDat._date4;
                } else if (TESTDIV0_5.equals(testdiv0)) {
                    testDate =  testDivDat._date5;
                }
                if (null != testDate) {
                    testDate = "(" + testDate.replace('-', '/') + ")";
                }
                svf.VrsOut("EXAM_DATE", testDate); // 試験日付

                final Tokei tokeiTestdivTotalKind1          = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind1LastYear1 = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind1LastYear2 = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind2          = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind2LastYear1 = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind2LastYear2 = new Tokei(null, null, null, null, null, null);
                final Tokei tokeiTestdivTotalKind3          = new Tokei(null, null, null, null, null, null);
                for (int j = 0; j < courseList.size(); j++) {
                    final int line = j + 1;
                    final Map courseMap = (Map) courseList.get(j);
                    final String examcoursecd = getString(courseMap, "EXAMCOURSECD");

                    final Tokei tokei1, tokei1LastYear1, tokei1LastYear2, tokei2, tokei2LastYear1, tokei2LastYear2, tokei3;
                    if (TOTAL.equals(examcoursecd)) {
                        //svf.VrsOutn("COURSE_NAME1", line, "計"); // コース名

                        tokei1          = tokeiTestdivTotalKind1;
                        tokei1LastYear1 = tokeiTestdivTotalKind1LastYear1;
                        tokei1LastYear2 = tokeiTestdivTotalKind1LastYear2;
                        tokei2          = tokeiTestdivTotalKind2;
                        tokei2LastYear1 = tokeiTestdivTotalKind2LastYear1;
                        tokei2LastYear2 = tokeiTestdivTotalKind2LastYear2;
                        tokei3          = tokeiTestdivTotalKind3;
                    } else {
                        final String coursecode = (String) courseMap.get("EXAMCOURSECD");
                        svf.VrsOutn("COURSE_NAME1", line, (String) courseMap.get("EXAMCOURSE_NAME")); // コース名

                        tokei1          = Tokei.getTokei(tokeiMap,         SHUTSUGAN, _param._entexamyear,        testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei1LastYear1 = Tokei.getTokei(lastyearTokeiMap, SHUTSUGAN, _param._lastEntexamyear,    testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei1LastYear2 = Tokei.getTokei(lastyearTokeiMap, SHUTSUGAN, _param._lastEntexamyear,    testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei2          = Tokei.getTokei(tokeiMap,         TETSUZUKI, _param._entexamyear,        testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei2LastYear1 = Tokei.getTokei(lastyearTokeiMap, TETSUZUKI, _param._lastEntexamyear,    testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei2LastYear2 = Tokei.getTokei(lastyearTokeiMap, TETSUZUKI, _param._lastEntexamyear,    testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                        tokei3          = Tokei.getTokei(tokeiMap,         SONOTA, _param._entexamyear,        testDivDat._testdiv, testdiv0, EXAM_TYPE1, coursecode);
                    }

                    if (null != tokei1) {
                        svf.VrsOutn("APPLI_NUM1", line, tokei1.getRemark(ZENJITSU)); // 出願数
                        svf.VrsOutn("APPLI_NUM2", line, tokei1.getRemark(HONJITSU)); // 出願数
                        svf.VrsOutn("APPLI_NUM3", line, tokei1.getRemark(ZENJITSU_HONJITSU)); // 出願数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind1.addTokei(tokei1);
                            tokeiTotalKind1.addTokei(tokei1);
                        }
                    }
                    if (null != tokei1LastYear2) {
                        svf.VrsOutn("LAST_APPLI_NUM1", line, tokei1LastYear2.getRemark(HONJITSU)); // 前年度出願数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind1LastYear2.addTokei(tokei1LastYear2);
                            tokeiTotalKind1LastYear2.addTokei(tokei1LastYear2);
                        }
                    }
                    if (null != tokei1LastYear1) {
                        svf.VrsOutn("LAST_APPLI_NUM2", line, tokei1LastYear1.getRemark(ZENJITSU_HONJITSU)); // 前年度出願数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind1LastYear1.addTokei(tokei1LastYear1);
                            tokeiTotalKind1LastYear1.addTokei(tokei1LastYear1);
                        }
                    }

                    if (null != tokei2) {
                        svf.VrsOutn("PROCE_NUM1", line, tokei2.getRemark(ZENJITSU)); // 手続人数
                        svf.VrsOutn("PROCE_NUM2", line, tetsuzukiHonjitsu(tokei2.getRemark(HONJITSU), tokei2.getRemark(HONJITSU_JITAI))); // 手続人数
                        svf.VrsOutn("PROCE_NUM3", line, tokei2.getRemark(ZENJITSU_HONJITSU)); // 手続人数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind2.addTokei(tokei2);
                            tokeiTotalKind2.addTokei(tokei2);
                        }
                    }
                    if (null != tokei2LastYear2) {
                        svf.VrsOutn("LAST_PROCE_NUM1", line, tokei2LastYear2.getRemark(HONJITSU)); // 前年度手続人数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind2LastYear2.addTokei(tokei2LastYear2);
                            tokeiTotalKind2LastYear2.addTokei(tokei2LastYear2);
                        }
                    }
                    if (null != tokei2LastYear1) {
                        svf.VrsOutn("LAST_PROCE_NUM2", line, tokei2LastYear1.getRemark(ZENJITSU_HONJITSU)); // 前年度手続人数
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind2LastYear1.addTokei(tokei2LastYear1);
                            tokeiTotalKind2LastYear1.addTokei(tokei2LastYear1);
                        }
                    }

                    if (null != tokei3) {
                        svf.VrsOutn("ABSENCE",       line, tokei3.getRemark(KESSEKI)); // 欠席
                        svf.VrsOutn("REAL_EXAM_NUM", line, tokei3.getRemark(JITSU_JUKEN)); // 実受験者
                        if (!TOTAL.equals(examcoursecd)) {
                            svf.VrsOutn("SLIDE1",        line, tokei3.getRemark(SLIDE_LEFT)); // スライド合格
                            svf.VrsOutn("SLIDE2",        line, tokei3.getRemark(SLIDE_RIGHT)); // スライド合格
                        }
                        svf.VrsOutn("PASS",          line, tokei3.getRemark(GOKAKU)); // 合格
                        svf.VrsOutn("REJECT",        line, tokei3.getRemark(FUGOKAKU)); // 不合格
                        if (!TOTAL.equals(examcoursecd)) {
                            tokeiTestdivTotalKind3.addTokei(tokei3);
                            tokeiTotalKind3.addTokei(tokei3);
                        }
                    }
                }
                svf.VrEndRecord();
            }
        }

        svf.VrsOut("BLANK", "DUMMY"); // 空行
        svf.VrEndRecord();

        svf.VrsOut("TOTAL_NAME", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　高校入試合計"); // 合計名称
        svf.VrsOut("TOTAL_APPLI_NUM1",      tokeiTotalKind1.getRemark(ZENJITSU)); // 合計出願数
        svf.VrsOut("TOTAL_APPLI_NUM2",      tokeiTotalKind1.getRemark(HONJITSU)); // 合計出願数
        svf.VrsOut("TOTAL_APPLI_NUM3",      tokeiTotalKind1.getRemark(ZENJITSU_HONJITSU)); // 合計出願数
        svf.VrsOut("TOTAL_LAST_APPLI_NUM1", tokeiTotalKind1LastYear2.getRemark(HONJITSU)); // 合計前年度出願数
        svf.VrsOut("TOTAL_LAST_APPLI_NUM2", tokeiTotalKind1LastYear1.getRemark(ZENJITSU_HONJITSU)); // 合計前年度出願数

        svf.VrsOut("TOTAL_PROCE_NUM1",      tokeiTotalKind2.getRemark(ZENJITSU)); // 合計手続人数
        svf.VrsOut("TOTAL_PROCE_NUM2",      tetsuzukiHonjitsu(tokeiTotalKind2.getRemark(HONJITSU), tokeiTotalKind2.getRemark(HONJITSU_JITAI))); // 合計手続人数
        svf.VrsOut("TOTAL_PROCE_NUM3",      tokeiTotalKind2.getRemark(ZENJITSU_HONJITSU)); // 合計手続人数
        svf.VrsOut("TOTAL_LAST_PROCE_NUM1", tokeiTotalKind2LastYear2.getRemark(HONJITSU)); // 合計前年度手続人数
        svf.VrsOut("TOTAL_LAST_PROCE_NUM2", tokeiTotalKind2LastYear1.getRemark(ZENJITSU_HONJITSU)); // 合計前年度手続人数

        svf.VrsOut("TOTAL_ABSENCE",       tokeiTotalKind3.getRemark(KESSEKI)); // 合計欠席
        svf.VrsOut("TOTAL_REAL_EXAM_NUM", tokeiTotalKind3.getRemark(JITSU_JUKEN)); // 合計実受験者
//        svf.VrsOut("TOTAL_SLIDE1",        tokeiTotalKind3.getRemark(SLIDE_LEFT)); // 合計スライド合格
//        svf.VrsOut("TOTAL_SLIDE2",        tokeiTotalKind3.getRemark(SLIDE_RIGHT)); // 合計スライド合格
        svf.VrsOut("TOTAL_PASS",          tokeiTotalKind3.getRemark(GOKAKU)); // 合計合格
        svf.VrsOut("TOTAL_REJECT",        tokeiTotalKind3.getRemark(FUGOKAKU)); // 合計不合格
        svf.VrEndRecord();
        _hasData = true;
    }

    private void printJ(final Vrw32alp svf, final DB2UDB db2) {
        final String EXAMCOURSECD_J = null; // 中学は指定しない

        final Map tokeiMap         = Tokei.getTokeiMap(db2, _param, _param._entexamyear,     _param._toujituDate);
        final Map lastYearTokeiMap = Tokei.getTokeiMap(db2, _param, _param._lastEntexamyear, _param._lastYearToujituDate);

        final String form = "KNJL341F_J1.frm";
        svf.VrSetForm(form, 4);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　中学校入試応募/手続状況一覧"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._toujituDate)); // 印刷日

        // 入試区分TESTDIV、表示する受験型
        final Map testdivExamtypeListMap = _param.getTestdivExamtypeListMap(db2);
        for (final Iterator it = testdivExamtypeListMap.values().iterator(); it.hasNext();) {
        	final List examptypeList = (List) it.next();
        	examptypeList.add(EXAM_TYPE_TOTAL);
        }

        // 特別措置者合計
        final StringBuffer tokubetuSochiRemark = new StringBuffer();
        for (int i = 0; i < _param._l024namecd2List.size(); i++) {
            final Map l024 = (Map) _param._l024namecd2List.get(i);
            final String testdiv = getString(l024, "NAMECD2");

            final TestDivDat testDivDat = (TestDivDat) _param._testdivMap.get(testdiv);

            String tuishiDate = testDivDat._tuishiDate1;
            if (null == tuishiDate || parseDate(tuishiDate, _param).after(parseDate(_param._toujituDate, _param))) {
            	if (null != tuishiDate) {
            		if (_param._isOutputDebug) {
            			log.info("指定日付以前の追試を表示しない:" + testdiv + ", " + testDivDat._name1 + ", " + tuishiDate);
            		}
            	}
                continue;
            }

            final Set receptnoSet = new HashSet();
            final List targetList = new ArrayList();
            targetList.addAll(Tokei.getTokei(tokeiMap, SONOTA, _param._entexamyear, testdiv, TESTDIV_NO_J, EXAM_TYPE_TOTAL, EXAMCOURSECD_J).getRemarkList(SPECIAL_REASON_DIV));
            for (final Iterator mit = targetList.iterator(); mit.hasNext();) {
                final Map m = (Map) mit.next();
                receptnoSet.add(getString(m, "RECEPT_DAT_RECEPTNO")); // 追試者
            }

            if (!receptnoSet.isEmpty()) {
                final String testname = StringUtils.defaultString(getString(l024, "NAME1"));
                if (tokubetuSochiRemark.length() == 0) {
                    tokubetuSochiRemark.append("特別措置対応:");
                } else {
                    tokubetuSochiRemark.append(" ");
                }
                final String remark = testname + " (" + tuishiDate.replace('-', '/') + ") " + String.valueOf(receptnoSet.size()) + "人";
                tokubetuSochiRemark.append(remark);
            }
        }

        final Tokei tokei1Total      = new Tokei(null, null, null, null, null, null);
        final Tokei tokei1LastTotal1 = new Tokei(null, null, null, null, null, null);
        final Tokei tokei1LastTotal2 = new Tokei(null, null, null, null, null, null);
        final Tokei tokei2Total      = new Tokei(null, null, null, null, null, null);
        final Tokei tokei2LastTotal1 = new Tokei(null, null, null, null, null, null);
        final Tokei tokei2LastTotal2 = new Tokei(null, null, null, null, null, null);
        final Tokei tokei3Total      = new Tokei(null, null, null, null, null, null);
        for (int i = 0; i < _param._l024namecd2List.size(); i++) {
            final Map l024 = (Map) _param._l024namecd2List.get(i);
            final String testdiv = getString(l024, "NAMECD2");
            final List examtypeList = getMappedList(testdivExamtypeListMap, testdiv);
            if (examtypeList.isEmpty()) {
                log.warn(" null examtype : " + testdiv);
                continue;
            }
            String titleField;
            int idxStart;
            boolean multiRecord = false;
            if (examtypeList.size() == 3) { // 表示行が3
                titleField = "1";
                idxStart = 1;
//                end = 3;
            } else if (examtypeList.size() == 2) { // 表示行が2
                titleField = "2";
                idxStart = 4;
//                end = 5;
            } else if (examtypeList.size() == 4) {  // 表示行が4
                titleField = "3";
                idxStart = 6;
//                end = 9;
            } else {
            	multiRecord = true;
                titleField = "_YOBI";
                idxStart = -1;
            }


        	String testDate = "";
        	final TestDivDat testDivDat = (TestDivDat) _param._testdivMap.get(testdiv);
        	if (null != testDivDat) {
        		testDate = testDivDat._date1; // 中学はNAMESPARE1
        		if (null != testDate) {
        			testDate = "(" + testDate.replace('-', '/') + ")";
        		}
        	}

            if (multiRecord) {

                final String[] nameArray = KNJ_EditEdit.get_token(StringUtils.defaultString(getString(l024, "NAME1")) + "入試", 14, 2);
                final int dateLine = (null == nameArray ? 0 : nameArray.length) + 1;

                for (int j = 0; j < examtypeList.size(); j++) {
                    final String sj = "_YOBI";
                    final String examType = (String) examtypeList.get(j);

                    svf.VrsOut("GRP", testdiv);

                    final String examTypeName;
                    if (EXAM_TYPE_TOTAL.equals(examType)) {
                    	examTypeName = "計";
                    } else {
                    	examTypeName = (String) _param._l005Name1Map.get(examType);
                    }
                    svf.VrsOut("TITLE" + sj + "_1", examTypeName);

                    if (null != nameArray && j < nameArray.length) {
                        svf.VrsOut("EXAM_NAME" + titleField + "_1", nameArray[j]); // 試験名称
                    } else if (j + 1 == dateLine) {
                        svf.VrsOut("EXAM_DATE" + titleField, testDate); // 試験日付
                    }

                    printExamType(svf, EXAMCOURSECD_J, tokeiMap, lastYearTokeiMap, tokei1Total, tokei1LastTotal1,
							tokei1LastTotal2, tokei2Total, tokei2LastTotal1, tokei2LastTotal2, tokei3Total, testdiv, sj,
							examType);
                    svf.VrEndRecord();
                }

            } else {
            	for (int j = 0; j < examtypeList.size(); j++) {
            		final String sj = String.valueOf(idxStart + j);
            		final String name = (String) _param._l005Name1Map.get(examtypeList.get(j));

            		svf.VrsOut("TITLE" + String.valueOf(sj) + "_1", name);
            	}

                int idx = 0;
                final String[] nameArray = KNJ_EditEdit.get_token(getString(l024, "NAME1"), 14, 2);
                if (null != nameArray) {
                    for (int ti = 0; ti < nameArray.length; ti++) {
                        if (StringUtils.isEmpty(nameArray[ti])) {
                            continue;
                        }
                        svf.VrsOut("EXAM_NAME" + titleField + "_" + String.valueOf(ti + 1), nameArray[ti]); // 試験名称
                        idx = ti + 1;
                    }
                }
                svf.VrsOut("EXAM_NAME" + titleField + "_" + String.valueOf(idx + 1), "入試"); // 試験名称
                svf.VrsOut("EXAM_DATE" + titleField, testDate); // 試験日付

                for (int j = 0; j < examtypeList.size(); j++) {
                    final String sj = String.valueOf(idxStart + j);
                    final String examType = (String) examtypeList.get(j);

                    printExamType(svf, EXAMCOURSECD_J, tokeiMap, lastYearTokeiMap, tokei1Total, tokei1LastTotal1,
							tokei1LastTotal2, tokei2Total, tokei2LastTotal1, tokei2LastTotal2, tokei3Total, testdiv, sj,
							examType);
                }
                svf.VrEndRecord();
            }
        }

        svf.VrsOut("TOTAL_APPLI_NUM1",      tokei1Total.getRemark(ZENJITSU)); // 合計出願数
        svf.VrsOut("TOTAL_APPLI_NUM2",      tokei1Total.getRemark(HONJITSU)); // 合計出願数
        final String sum1 = tokei1Total.getRemark(ZENJITSU_HONJITSU);
        svf.VrsOut("TOTAL_APPLI_NUM3", sum1); // 合計出願数
        svf.VrsOut("TOTAL_LAST_APPLI_NUM1", tokei1LastTotal1.getRemark(HONJITSU)); // 合計前年度出願数
        final String sum2 = tokei1LastTotal2.getRemark(ZENJITSU_HONJITSU);
        svf.VrsOut("TOTAL_LAST_APPLI_NUM2", sum2); // 合計前年度出願数
        svf.VrsOut("TOTAL_LAST_APPLI_NUM3", subtract(sum1, sum2)); //
        svf.VrsOut("TOTAL_PROCE_NUM1",      tokei2Total.getRemark(ZENJITSU)); // 合計手続人数
        svf.VrsOut("TOTAL_PROCE_NUM2",      tetsuzukiHonjitsu(tokei2Total.getRemark(HONJITSU), tokei2Total.getRemark(HONJITSU_JITAI))); // 合計手続人数
        svf.VrsOut("TOTAL_PROCE_NUM3",      tokei2Total.getRemark(ZENJITSU_HONJITSU)); // 合計手続人数
        svf.VrsOut("TOTAL_LAST_PROCE_NUM1", tokei2LastTotal1.getRemark(HONJITSU)); // 合計前年度手続人数
        svf.VrsOut("TOTAL_LAST_PROCE_NUM2", tokei2LastTotal2.getRemark(ZENJITSU_HONJITSU)); // 合計前年度手続人数
        svf.VrsOut("TOTAL_ABSENCE",         tokei3Total.getRemark(KESSEKI)); // 合計欠席
        svf.VrsOut("TOTAL_REAL_EXAM_NUM",   tokei3Total.getRemark(JITSU_JUKEN)); // 合計実受験者
        svf.VrsOut("TOTAL_PASS",            tokei3Total.getRemark(GOKAKU)); // 合計合格
        svf.VrsOut("TOTAL_SCHOLARSHIP",     tokei3Total.getRemark(TOKUTAI)); // 合計特待生
        svf.VrsOut("TOTAL_REJECT",          tokei3Total.getRemark(FUGOKAKU)); // 合計不合格
        svf.VrsOut("TOTAL_REMARK", "XXX"); // 備考 ダミー
        svf.VrAttribute("TOTAL_REMARK", "X=10000");
        svf.VrEndRecord();
        _hasData = true;

        // 下 手続辞退者
        final List jitaiExamnoList = getJitaiExamnoList(db2);
        if (jitaiExamnoList.size() > 0) {
            boolean add = false;
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < jitaiExamnoList.size(); i++) {
                if (add) {
                    stb.append(" ");
                }
                stb.append(jitaiExamnoList.get(i));
                add = true;
            }
            svf.VrsOut("REMARK", "入学後手続辞退:" + stb.toString());
            svf.VrEndRecord();
        }
        if (tokubetuSochiRemark.length() > 0) {
            svf.VrsOut("REMARK", tokubetuSochiRemark.toString());
            svf.VrEndRecord();
        }
    }

	private void printExamType(final Vrw32alp svf, final String EXAMCOURSECD_J, final Map tokeiMap,
			final Map lastYearTokeiMap, final Tokei tokei1Total, final Tokei tokei1LastTotal1,
			final Tokei tokei1LastTotal2, final Tokei tokei2Total, final Tokei tokei2LastTotal1,
			final Tokei tokei2LastTotal2, final Tokei tokei3Total, final String testdiv, final String sj,
			final String examType) {
		final Tokei tokei1 = Tokei.getTokei(tokeiMap, SHUTSUGAN, _param._entexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		String shutuganTotal = null;
		if (null != tokei1) {
		    svf.VrsOut("APPLI_NUM" + sj + "_1", tokei1.getRemark(ZENJITSU)); // 出願数
		    svf.VrsOut("APPLI_NUM" + sj + "_2", tokei1.getRemark(HONJITSU)); // 出願数
		    shutuganTotal = tokei1.getRemark(ZENJITSU_HONJITSU);
		    svf.VrsOut("APPLI_NUM" + sj + "_3", shutuganTotal); // 出願数
		    if (EXAM_TYPE_TOTAL.equals(examType)) {
		        tokei1Total.addTokei(tokei1);
		    }
		}
		if (EXAM_TYPE_TOTAL.equals(examType)) {
		    String shutuganLastTotal = null;
		    final Tokei tokei1Last1 = Tokei.getTokei(lastYearTokeiMap, SHUTSUGAN, _param._lastEntexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		    if (null != tokei1Last1) {
		        svf.VrsOut("LAST_APPLI_NUM" + sj + "_1", tokei1Last1.getRemark(HONJITSU)); // 前年度出願数
		        tokei1LastTotal1.addTokei(tokei1Last1);
		    }
		    final Tokei tokei1Last2 = Tokei.getTokei(lastYearTokeiMap, SHUTSUGAN, _param._lastEntexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		    if (null != tokei1Last2) {
		        shutuganLastTotal = tokei1Last2.getRemark(ZENJITSU_HONJITSU);
		        svf.VrsOut("LAST_APPLI_NUM" + sj + "_2", shutuganLastTotal); // 前年度出願数
		        tokei1LastTotal2.addTokei(tokei1Last2);
		    }
		    if (null != shutuganTotal && null != shutuganLastTotal) {
		        svf.VrsOut("LAST_APPLI_NUM" + sj + "_3", subtract(shutuganTotal, shutuganLastTotal)); // 前年比
		    }
		}

		final Tokei tokei2 = Tokei.getTokei(tokeiMap, TETSUZUKI, _param._entexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		if (null != tokei2) {
		    svf.VrsOut("PROCE_NUM" + sj + "_1", tokei2.getRemark(ZENJITSU)); // 手続人数
		    svf.VrsOut("PROCE_NUM" + sj + "_2", tetsuzukiHonjitsu(tokei2.getRemark(HONJITSU), tokei2.getRemark(HONJITSU_JITAI))); // 手続人数
		    svf.VrsOut("PROCE_NUM" + sj + "_3", tokei2.getRemark(ZENJITSU_HONJITSU)); // 手続人数
		    if (EXAM_TYPE_TOTAL.equals(examType)) {
		        tokei2Total.addTokei(tokei2);
		    }
		}
		if (EXAM_TYPE_TOTAL.equals(examType)) {
		    final Tokei tokei2Last1 = Tokei.getTokei(lastYearTokeiMap, TETSUZUKI, _param._lastEntexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		    if (null != tokei2Last1) {
		        svf.VrsOut("LAST_PROCE_NUM" + sj + "_1", tokei2Last1.getRemark(HONJITSU)); // 前年度手続人数
		        tokei2LastTotal1.addTokei(tokei2Last1);
		    }
		    final Tokei tokei2Last2 = Tokei.getTokei(lastYearTokeiMap, TETSUZUKI, _param._lastEntexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		    if (null != tokei2Last2) {
		        svf.VrsOut("LAST_PROCE_NUM" + sj + "_2", tokei2Last2.getRemark(ZENJITSU_HONJITSU)); // 前年度手続人数
		        tokei2LastTotal2.addTokei(tokei2Last2);
		    }
		}

		final Tokei tokei3 = Tokei.getTokei(tokeiMap, SONOTA, _param._entexamyear, testdiv, TESTDIV_NO_J, examType, EXAMCOURSECD_J);
		if (null != tokei3) {
		    svf.VrsOut("ABSENCE"       + sj, tokei3.getRemark(KESSEKI)); // 欠席
		    svf.VrsOut("REAL_EXAM_NUM" + sj, tokei3.getRemark(JITSU_JUKEN)); // 実受験者
		    svf.VrsOut("PASS"          + sj, tokei3.getRemark(GOKAKU)); // 合格
		    svf.VrsOut("SCHOLARSHIP"   + sj, tokei3.getRemark(TOKUTAI)); // 特待生
		    svf.VrsOut("REJECT"        + sj, tokei3.getRemark(FUGOKAKU)); // 不合格
		    if (EXAM_TYPE_TOTAL.equals(examType)) {
		        tokei3Total.addTokei(tokei3);
		    } else if (null != tokei3._remarkText) {
		        svf.VrsOut("REMARK" + sj, tokei3._remarkText); // 備考
		    }
		}
	}

    private static class J024 {
        final String _cd;
        public J024(final String cd) {
            _cd = cd;
        }
    }

    private static String tetsuzukiHonjitsu(final String count, String jitaiCount) {
        if (null == count && null == jitaiCount) {
            return null;
        }
        return StringUtils.defaultString(count, "0") + "(" + StringUtils.repeat(" ", 2 - StringUtils.defaultString(jitaiCount, "0").length()) + StringUtils.defaultString(jitaiCount, "0") + ")";
    }

    private static class Tokei {

        final String _entexamyear;
        final String _testdiv;
        final String _testdivNo;
        final String _coursecode;
        final String _kindCd;
        final String _examType;
        Map _remarkTargetListMap = new HashMap();
        Map _remarkValueMap = new HashMap();
        String _remarkText;

        Tokei(
            final String entexamyear,
            final String testdiv,
            final String testdivNo,
            final String coursecode,
            final String kindCd,
            final String examType
        ) {
            _entexamyear = entexamyear;
            _testdiv = testdiv;
            _testdivNo = testdivNo;
            _coursecode = coursecode;
            _kindCd = kindCd;
            _examType = examType;
        }

        private void setRemark(final String remark, final String value) {
            _remarkValueMap.put(remark, value);
//            log.info(" putValue = " + value);
        }

        private String getRemark(final String remark) {
            return (String) _remarkValueMap.get(remark);
        }

        private static void addTokeiRemark(final Map tokeiMap, final String kind, final String entexamyear, final String testdiv, final String testdivNo, final String[] examTypes, final String coursecode, final String remark, final Map data) {
            for (int i = 0; i < examTypes.length; i++) {
                getTokei(tokeiMap, kind, entexamyear, testdiv, testdivNo, examTypes[i], coursecode).getRemarkList(remark).add(data);
            }
        }

        public static Map getTokeiMap(final DB2UDB db2, final Param param, final String entexamyear, final String toujituDateStr) {
            final Map tokeiMap = new HashMap();
            final String sql = getTokeiSql(param, entexamyear, toujituDateStr);
            final List list = getList(db2, sql);
            final String ALL = "ALL";
            final String RECEPT_DAT = "RECEPT_DAT";
            final Date toujituDate = parseDate(toujituDateStr, param);

            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String coursecode = getString(m, "COURSECODE");
                final String testdiv = getString(m, "TESTDIV");
                final String testdiv0 = getString(m, "TESTDIV0");
                final String examType = getString(m, "EXAM_TYPE");
                final String generalFlg = getString(m, "GENERAL_FLG");
                final String examno = getString(m, "EXAMNO");
                final boolean isTokubetuNyushi = ("1".equals(param._applicantdiv) && "5".equals(testdiv) || "2".equals(param._applicantdiv) && "3".equals(testdiv)) && "1".equals(generalFlg);
                final String[] examTypes = {examType, EXAM_TYPE_TOTAL};

                addTokeiRemark(tokeiMap, ALL, entexamyear, testdiv, testdiv0, examTypes, coursecode, ALL, m);
                // 出願人数
                final Date receptDate = getDate(m, "RECEPTDATE", param);
                if (null != receptDate && null != toujituDate) {
                    if (receptDate.before(toujituDate)) {
                        addTokeiRemark(tokeiMap, SHUTSUGAN, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU, m);
                        addTokeiRemark(tokeiMap, SHUTSUGAN, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU_HONJITSU, m);
                    } else if (receptDate.equals(toujituDate)) {
                        addTokeiRemark(tokeiMap, SHUTSUGAN, entexamyear, testdiv, testdiv0, examTypes, coursecode, HONJITSU, m);
                        addTokeiRemark(tokeiMap, SHUTSUGAN, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU_HONJITSU, m);
                    } else {
                		if (param._isOutputDebug) {
                			log.warn(" not target receptDate (" + testdiv0 + ") : " + param.df.format(receptDate) + " limit = " + param.df.format(toujituDate));
                		}
                    }
                }

                // 手続き人数
                final Date tetsuzukiDate = getDate(m, "TETUZUKI_DATE", param);
                final String judgediv = getString(m, "JUDGEDIV");
                final boolean isJitai = "1".equals(judgediv) && null != tetsuzukiDate && "2".equals(getString(m, "ENTDIV"));
                final String jitaiDateStr = getString(m, "ENTDIV2_DATE");
                final Date jitaiDate = getDate(m, "ENTDIV2_DATE", param);
                if (isTokubetuNyushi) {
                    // 特別入試対象者は対象外
                } else {
                    if (null != tetsuzukiDate && null != toujituDate) {
                        if (isJitai && (null == jitaiDate || jitaiDate.before(toujituDate) || jitaiDate.equals(toujituDate))) { // 辞退日付がnull or 前日 or 本日 (未来なら未辞退として扱う)
                            if (null == jitaiDate || jitaiDate.before(toujituDate)) { // 辞退日付がnull or 前日 -> 追加無し
                                log.info(entexamyear + "年度  辞退者を除く:" + examno);
                            } else if (jitaiDate.equals(toujituDate)) { // 辞退日付が本日
                                if (tetsuzukiDate.before(toujituDate)) {
                                    // 前日手続きして本日辞退 -> 前日に追加
                                    addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU, m);
                                    addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, HONJITSU_JITAI, m);
                                    log.info(" 前日手続の辞退:" + examno + " 辞退日付=" + jitaiDateStr + ", 当日=" + toujituDateStr);
                                } else if (tetsuzukiDate.equals(toujituDate)) {
                                    // 本日手続きして本日辞退 -> 本日に追加  ... そもそもありえるのか
                                    addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, HONJITSU, m);
                                    addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, HONJITSU_JITAI, m);
                                    log.info(" 本日手続の辞退:" + examno + " 辞退日付=" + jitaiDateStr + ", 当日=" + toujituDateStr);
                                }
                            }
                        } else if (tetsuzukiDate.before(toujituDate)) {
                            addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU, m);
                            addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU_HONJITSU, m);
                        } else if (tetsuzukiDate.equals(toujituDate)) {
                            addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, HONJITSU, m);
                            addTokeiRemark(tokeiMap, TETSUZUKI, entexamyear, testdiv, testdiv0, examTypes, coursecode, ZENJITSU_HONJITSU, m);
                        } else {
                    		if (param._isOutputDebug) {
                                log.warn(" not target entPayDate (" + testdiv0 + ") : " + param.df.format(tetsuzukiDate) + " limit = " + param.df.format(toujituDate));
                    		}
                        }
                    }
                }

                final TestDivDat testDivDat = (TestDivDat) param._testdivMap.get(testdiv);
                if (null == testDivDat) {
                    log.fatal(" not set testdiv :" + testdiv);
                    continue;
                }
                Date testJisshiDate = null;
                Date tuishiJisshiDate = null;
                if (TESTDIV0_1.equals(testdiv0)) {
                    testJisshiDate = parseDate(testDivDat._date1, param);
                    tuishiJisshiDate = parseDate(testDivDat._tuishiDate1, param);
                } else if (TESTDIV0_2.equals(testdiv0)) {
                    testJisshiDate = parseDate(testDivDat._date2, param);
                    tuishiJisshiDate = parseDate(testDivDat._tuishiDate2, param);
                } else if (TESTDIV0_3.equals(testdiv0)) {
                    testJisshiDate = parseDate(testDivDat._date3, param);
                    tuishiJisshiDate = parseDate(null, param);
                } else if (TESTDIV0_4.equals(testdiv0)) {
                    testJisshiDate = parseDate(testDivDat._date4, param);
                    tuishiJisshiDate = parseDate(null, param);
                } else if (TESTDIV0_5.equals(testdiv0)) {
                    testJisshiDate = parseDate(testDivDat._date5, param);
                    tuishiJisshiDate = parseDate(null, param);
                } else {
                }
                final String receptDatReceptno = getString(m, "RECEPT_DAT_RECEPTNO");
                if (null != receptDatReceptno && null != testJisshiDate && (testJisshiDate.before(toujituDate) || testJisshiDate.equals(toujituDate))) {

                    addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, RECEPT_DAT, m);
                    final boolean isTokubetuSochi = null != getString(m, "SPECIAL_REASON_DIV");
                    final boolean isKesseki;
                    if (null != tuishiJisshiDate && tuishiJisshiDate.after(toujituDate)) {
                        // 追試日付以前
                        if (isTokubetuSochi) {
                            isKesseki = true;
                            if ("4".equals(judgediv)) {
                                log.info(" 特別措置者で欠席:" + examno);
                            } else {
                                log.info(" 追試日付前の特別措置者を欠席として扱う:" + examno + ", 追試日付 = " + param.df.format(tuishiJisshiDate));
                            }
                        } else {
                            isKesseki = "4".equals(judgediv);
                        }
                    } else {
                        isKesseki = "4".equals(judgediv);
                    }
                    if (isKesseki) {
                        // 欠席1
                        addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, KESSEKI, m);
                    } else if (isTokubetuNyushi) {
                        // 特別入試対象者は合格、不合格、特待生から対象外
                        if ("1".equals(judgediv)) {
                            // 合格者には含めない
                            if (NumberUtils.isDigits(getString(m, "HONORDIV"))) {
                                // 特待生
                                final String oldHonordiv = (String) param._examnoOldHonordivMap.get(examno);
                                final String honordiv = getString(m, "HONORDIV");
//                                final int iHonordiv = Integer.parseInt(honordiv);
//                                final int iOldHonordiv = NumberUtils.isDigits(oldHonordiv) ? Integer.parseInt(oldHonordiv) : 999;
//                                final boolean isTokutaiCountTaisho = iHonordiv < iOldhonordiv;
                                final boolean isTokutaiCountTaisho = null == oldHonordiv && null != honordiv;
                                if (isTokutaiCountTaisho) {
                                    addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, TOKUTAI, m);
                                }
                            }
                        }
                    } else {
                        if ("2".equals(judgediv)) {
                            // 不合格者
                            addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, FUGOKAKU, m);
                        } else if ("1".equals(judgediv)) {
                            // 合格者
                            addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, GOKAKU, m);
                            if (null != getString(m, "HONORDIV")) {
                                // 特待生
                                addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, TOKUTAI, m);
                            }
                        }
                    }
                    if (isTokubetuSochi) {
                        // 特別措置者
                        addTokeiRemark(tokeiMap, SONOTA, entexamyear, testdiv, testdiv0, examTypes, coursecode, SPECIAL_REASON_DIV, m);
                    }
                }
            }

            for (final Iterator it = Tokei.toTokeiList(tokeiMap).iterator(); it.hasNext();) {
                final Tokei tokei = (Tokei) it.next();
                if (SHUTSUGAN.equals(tokei._kindCd) || TETSUZUKI.equals(tokei._kindCd)) {
                    for (final Iterator remit = Arrays.asList(new String[] {HONJITSU, ZENJITSU, ZENJITSU_HONJITSU, HONJITSU_JITAI}).iterator(); remit.hasNext();) {
                        final String key = (String) remit.next();
                        tokei.setRemark(key, String.valueOf(tokei.getRemarkList(key).size()));
                    }
                }
            }

            for (final Iterator it = Tokei.toTokeiList(tokeiMap).iterator(); it.hasNext();) {
                final Tokei tokei = (Tokei) it.next();
                if (SONOTA.equals(tokei._kindCd)) {
                    final List judgediv4CountList = tokei.getRemarkList(KESSEKI);
                    final List baseList = getTokei(tokeiMap, ALL, tokei._entexamyear, tokei._testdiv, tokei._testdivNo, tokei._examType, tokei._coursecode).getRemarkList(ALL);
                    final List receptList =  getTokei(tokeiMap, SONOTA, tokei._entexamyear, tokei._testdiv, tokei._testdivNo, tokei._examType, tokei._coursecode).getRemarkList(RECEPT_DAT);
                    //log.debug(" tokei " + tokei.getKey() + "-" + tokei._coursecode + " : " + judgediv4Count + ", " + shutsugan + ", " + uketsuke);

                    final List baseAriReceptNasiList = new ArrayList(); // APPLICANTBASEあり、RECEPTなし
                    for (final Iterator bit = baseList.iterator(); bit.hasNext();) {
                        final Map base = (Map) bit.next();
                        if (!receptList.contains(base)) {
                            baseAriReceptNasiList.add(base);
                        }
                    }
                    final List jitsuJukensyaList = new ArrayList(); // receptList.size() - judgediv4CountList.size();
                    for (final Iterator rit = receptList.iterator(); rit.hasNext();) {
                        final Map recept = (Map) rit.next();
                        if (!judgediv4CountList.contains(recept)) {
                            jitsuJukensyaList.add(recept);
                        }
                    }

                    tokei.setRemark(KESSEKI, String.valueOf(judgediv4CountList.size() + baseAriReceptNasiList.size())); // 欠席
                    tokei.setRemark(JITSU_JUKEN, String.valueOf(jitsuJukensyaList.size())); // 実受験者
                    if ("1".equals(param._applicantdiv) && "5".equals(tokei._testdiv)) {
                        // 中学入試第5回は特別入試対象者をカウントする
                        final List tokubetsuNyushiTaishoshaList = new ArrayList();
                        for (final Iterator jit = jitsuJukensyaList.iterator(); jit.hasNext();) {
                            final Map jitsu = (Map) jit.next();
                            if ("1".equals(getString(jitsu, "GENERAL_FLG"))) {
                                tokubetsuNyushiTaishoshaList.add(jitsu);
                            }
                        }
                        if (tokubetsuNyushiTaishoshaList.size() > 0) {
                            tokei._remarkText = "受験者" + String.valueOf(tokubetsuNyushiTaishoshaList.size()) + "人は入学手続済";
                        }
                    }

                    tokei.setRemark(GOKAKU, String.valueOf(tokei.getRemarkList(GOKAKU).size())); // 合格
                    tokei.setRemark(TOKUTAI, String.valueOf(tokei.getRemarkList(TOKUTAI).size())); // 特待生
                    tokei.setRemark(FUGOKAKU, String.valueOf(tokei.getRemarkList(FUGOKAKU).size())); // 不合格
                }
            }

            if ("2".equals(param._applicantdiv)) {
                final Map slideGoukakuMap = new HashMap();
                // 元 -> 先
                slideGoukakuMap.put("1001", "1002"); // 理数キャリア スタンダード -> アドバンスト
                slideGoukakuMap.put("2001", "2002"); // 国際教養 スタンダード -> アドバンスト

                slideGoukakuMap.put("1002", "1001"); // 理数キャリア アドバンスト -> スタンダード
                slideGoukakuMap.put("2002", "2001"); // 国際教養 アドバンスト -> スタンダード

                for (final Iterator it = Tokei.toTokeiList(tokeiMap).iterator(); it.hasNext();) {
                    final Tokei tokeiSlideMoto = (Tokei) it.next();
                    if (SONOTA.equals(tokeiSlideMoto._kindCd)) {
                        final String slideSakiCoursecode = (String) slideGoukakuMap.get(tokeiSlideMoto._coursecode);
                        if (null != slideSakiCoursecode) {
                            final List slideList = new ArrayList();
                            final Tokei tokeiSlideSaki = getTokei(tokeiMap, SONOTA, tokeiSlideMoto._entexamyear, tokeiSlideMoto._testdiv, tokeiSlideMoto._testdivNo, tokeiSlideMoto._examType, slideSakiCoursecode);
                            final List slideMotoReceptList = tokeiSlideMoto.getRemarkList(RECEPT_DAT);
                            //log.debug(" moto = " + tokeiSlideMoto._coursecode + ", slideMotoReceptList (" + slideMotoReceptList.size() + ") = " + mapSetField(slideMotoReceptList, new String[] {"SUC_COURSECODE", "EXAMNO"}));
                            for (final Iterator rit = slideMotoReceptList.iterator(); rit.hasNext();) {
                                final Map m = (Map) rit.next();
                                if (tokeiSlideSaki._coursecode.equals(getString(m, "SUC_COURSECODE")) && "1".equals(getString(m, "JUDGEDIV"))) {
                                    slideList.add(m);
                                }
                            }
                            if (slideList.size() > 0) {
                                final int slideCount = slideList.size();
                                tokeiSlideSaki.setRemark(SLIDE_RIGHT, String.valueOf(slideCount));
                                tokeiSlideSaki.setRemark(GOKAKU, add(tokeiSlideSaki.getRemark(GOKAKU), String.valueOf(slideCount))); // 合格者に合算

                                tokeiSlideMoto.setRemark(SLIDE_LEFT, String.valueOf(-1 * slideCount));
                                tokeiSlideMoto.setRemark(GOKAKU, subtract(tokeiSlideMoto.getRemark(GOKAKU), String.valueOf(slideCount)));
                            }
                        }
                    }
                }
            }

//            final String lastYearExamtype = "1".equals(param._applicantdiv) ? EXAM_TYPE_TOTAL : "1";
//            final String lastYear = String.valueOf(Integer.parseInt(entexamyear) - 1);
//            final List lastYearList = getList(db2, getTokeiLastYearSql(param, lastYear));
//            for (final Iterator it = lastYearList.iterator(); it.hasNext();) {
//                final Map m = (Map) it.next();
//                final String coursecode = getString(m, "COURSECODE");
//                final String testdiv = getString(m, "TESTDIV");
//                final String testdivNo = getString(m, "TESTDIV_NO");
//                final String kindcd = getString(m, "KIND_CD");
//                final Tokei tokei = getTokei(tokeiMap, kindcd, lastYear, testdiv, testdivNo, lastYearExamtype, coursecode);
//                for (int i = 1; i <= 10; i++) {
//                    tokei.setRemark(String.valueOf(i), getString(m, "REMARK" + String.valueOf(i)));
//                }
//            }
            return tokeiMap;
        }

        private void addTokei(final Tokei tokei) {
            final Set keys = new HashSet();
            keys.addAll(_remarkValueMap.keySet());
            keys.addAll(tokei._remarkValueMap.keySet());
            for (final Iterator it = keys.iterator(); it.hasNext();) {
                final String key = (String) it.next();
                setRemark(key, add(getRemark(key), tokei.getRemark(key)));
            }
        }

        private List getRemarkList(final String remarkId) {
            return getMappedList(_remarkTargetListMap, remarkId);
        }

        private String getKey() {
            return getTokeiKey(_entexamyear, _kindCd, _testdiv, _testdivNo, _examType);
        }

        public String toString() {
            return "{" + getKey() + " = " + _remarkValueMap + "}";
        }

        private static String getTokeiKey(final String entexamyear, final String kindCd, final String testdiv, final String testdivNo, final String examType) {
            return entexamyear + "-" + kindCd + "-" + testdiv + "-" + testdivNo + "-" + examType;
        }

        private static List toTokeiList(final Map tokeiMap) {
            final List rtn = new ArrayList();
            for (final Iterator it = new TreeMap(tokeiMap).keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final Map cm = (Map) tokeiMap.get(key);
                rtn.addAll(cm.values());
            }
            return rtn;
        }

        private static Tokei getTokei(final Map tokeiMap, final String kindcd, final String entexamyear, final String testdiv, final String testdivNo, final String examType, final String coursecode) {
            final String key = getTokeiKey(entexamyear, kindcd, testdiv, testdivNo, examType);
            final Map keyMap = getMappedHashMap(tokeiMap, key);
            if (StringUtils.isBlank(coursecode)) {
                if (keyMap.size() == 0) {
                    return null;
                } else {
                    return (Tokei) keyMap.values().iterator().next();
                }
            }
            if (null == keyMap.get(coursecode)) {
                keyMap.put(coursecode, new Tokei(entexamyear, testdiv, testdivNo, coursecode, kindcd, examType));
            }
            return (Tokei) keyMap.get(coursecode);
        }

        private static String getTokeiSql(final Param param, final String entexamyear, final String toujituDate) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_TESTDIV AS ( ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV ");
            stb.append("     FROM V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + entexamyear + "' ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("         AND NAMECD1 = 'L024' ");
            } else if ("2".equals(param._applicantdiv)) {
                stb.append("         AND NAMECD1 = 'L004' ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.ENTEXAMYEAR ");
            stb.append("   , T1.APPLICANTDIV ");
            stb.append("   , APD001.REMARK10 AS COURSECODE ");
            stb.append("   , T1.SUC_COURSECODE ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("   , VATD.EXAM_TYPE ");
            } else {
                stb.append("   , TREC.EXAM_TYPE ");
            }
            stb.append("   , T1.EXAMNO ");
            stb.append("   , T1.RECEPTDATE ");
//            stb.append("   , EM.ENT_PAY_DATE AS TETUZUKI_DATE ");
            stb.append("   , T1.SPECIAL_REASON_DIV ");
            stb.append("   , L1.TESTDIV ");
            stb.append("   , T1.GENERAL_FLG ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("   , '" + TESTDIV_NO_J + "' AS TESTDIV0 ");
            } else {
                stb.append("   , TRDET003.REMARK1 AS TESTDIV0 ");
            }

            stb.append("   , TREC.RECEPTNO AS RECEPT_DAT_RECEPTNO ");
            stb.append("   , TREC.JUDGEDIV ");
            stb.append("   , TREC.HONORDIV ");
            stb.append("   , TREC.PROCEDUREDATE1 AS TETUZUKI_DATE ");
            stb.append("   , T1.PROCEDUREDIV ");
            stb.append("   , T1.ENTDIV ");
            stb.append("   , APD022.REMARK1 AS ENTDIV2_DATE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND APD001.EXAMNO = T1.EXAMNO ");
            stb.append("         AND APD001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD022 ON APD022.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND APD022.EXAMNO = T1.EXAMNO ");
            stb.append("         AND APD022.SEQ = '022' ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VATD ON VATD.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("                                    AND VATD.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("                                    AND VATD.EXAMNO       = T1.EXAMNO ");
            }
            stb.append("     LEFT JOIN ENTEXAM_MONEY_DAT EM ON EM.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND EM.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND EM.EXAMNO = T1.EXAMNO ");
            stb.append("     INNER JOIN T_TESTDIV L1 ON L1.TESTDIV =  ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("         VATD.TESTDIV ");
            } else {
                stb.append("         T1.TESTDIV ");
            }
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT TREC ON TREC.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND TREC.TESTDIV = L1.TESTDIV ");
            stb.append("         AND TREC.EXAMNO = T1.EXAMNO ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("         AND TREC.RECEPTNO = VATD.RECEPTNO ");
            }
            stb.append("         AND TREC.EXAM_TYPE = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET003.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET003.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET003.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDET003.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET003.SEQ = '003' ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND APD001.REMARK10 IS NOT NULL ");
            if ("1".equals(param._applicantdiv)) {
//                stb.append("     AND (L1.TESTDIV <> '5' OR L1.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') "); // 2017/02/04 出願者、欠席、実受験者に特別入試対象者を含める。
            } else if ("2".equals(param._applicantdiv)) {
//                stb.append("     AND (L1.TESTDIV <> '3' OR L1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') ");
            }
            stb.append(" ORDER BY ");
            stb.append("     int(TESTDIV) ");
            return stb.toString();
        }
    }

    private List getJitaiExamnoList(final DB2UDB db2) {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._applicantdiv)) {
            stb.append(" SELECT TREC.RECEPTNO ");
            stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT APBASE ");
            stb.append(" INNER JOIN V_ENTEXAM_RECEPT_DAT TREC ON TREC.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("     AND TREC.EXAMNO = APBASE.EXAMNO ");
            stb.append("     AND TREC.JUDGEDIV = '1' ");
        } else {
            stb.append(" SELECT APBASE.EXAMNO ");
            stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT APBASE ");
        }
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD022 ON APD022.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
        stb.append("     AND APD022.EXAMNO = APBASE.EXAMNO");
        stb.append("     AND APD022.SEQ = '022' ");
        stb.append(" WHERE APBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND APBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND (APBASE.PROCEDUREDIV = '2' AND (APBASE.PROCEDUREDATE IS NULL OR APBASE.PROCEDUREDATE <= '" + _param._toujituDate + "') ");
        stb.append("     OR APBASE.ENTDIV = '2' AND (APD022.REMARK1 IS NULL OR APD022.REMARK1 <= '" + _param._toujituDate + "')) ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append(" ORDER BY TREC.RECEPTNO ");
        } else {
            stb.append(" ORDER BY APBASE.EXAMNO ");
        }
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //log.info(" jitai examno sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if ("1".equals(_param._applicantdiv)) {
                    rtn.add(rs.getString("RECEPTNO"));
                } else if ("2".equals(_param._applicantdiv)) {
                    rtn.add(rs.getString("EXAMNO"));
                }
            }
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private Map getProcedureCountMap(final DB2UDB db2) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT APD001.REMARK10 AS EXAMCOURSECD, COUNT(*) AS COUNT ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT APBASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
        stb.append("         AND APD001.EXAMNO = APBASE.EXAMNO");
        stb.append("         AND APD001.SEQ = '001' ");
        stb.append(" WHERE APBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND APBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND APBASE.PROCEDUREDIV = '1' ");
        stb.append(" GROUP BY APD001.REMARK10 ");

        final Map m = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                m.put(rs.getString("EXAMCOURSECD"), rs.getString("COUNT"));
            }
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return m;
    }

    private static class TestDivDat {
        final String _testdiv;
        final String _name1;
        final String _date1;
        final String _date2;
        final String _date3;
        final String _date4;
        final String _date5;
        String _tuishiDate1;
        String _tuishiDate2;
        final Map _testdiv0NameArrayMap = new TreeMap();
        public TestDivDat(final String testDiv, final String name1, final String namespare1, final String name3, final String nml044namespare1, final String nml044name3, final String nml059namespare1) {
            _testdiv = testDiv;
            _name1 = name1;
            _date1 = namespare1;
            _date2 = name3;
            _date3 = nml044namespare1;
            _date4 = nml044name3;
            _date5 = nml059namespare1;
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _loginDate;
        final String _toujituDate;
        final String _lastEntexamyear;
        final String _lastYearToujituDate;
        final boolean _seirekiFlg;
        final Map _testdivMap;
        final List _entexamcourseList;
        Map _examnoOldHonordivMap = Collections.EMPTY_MAP;
        Map _l005Name1Map = Collections.EMPTY_MAP;
        List _l024namecd2List = Collections.EMPTY_LIST;
        final boolean _isOutputDebug;

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _toujituDate  = request.getParameter("TOUJITU_DATE").replace('/', '-');
            _lastEntexamyear = String.valueOf(Integer.parseInt(_entexamyear) - 1);

            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(_toujituDate));
            cal.add(Calendar.YEAR, -1);
            _lastYearToujituDate = df.format(cal.getTime());
            log.debug(" toujituDate = " + _toujituDate + " / lastYearToujituDate = " + _lastYearToujituDate);

            _seirekiFlg = getSeirekiFlg(db2);
            _testdivMap = getTestdivMap(db2);
            _entexamcourseList = getEntexamcourseList(db2);
//            log.debug(" entexam course = " + _entexamcourseList);
            if ("1".equals(_applicantdiv)) {
                _examnoOldHonordivMap = getExamnoOldHonordivMap(db2);
                _l024namecd2List = KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L024' ORDER BY INT(NAMESPARE3) ");
                _l005Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L005' "), "NAMECD2", "NAME1");
            }
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJL341F", "outputDebug"));
        }

        private Map getTestdivExamtypeListMap(final DB2UDB db2) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT DISTINCT ");
        	stb.append("     INT(T1.TESTDIV) AS TESTDIV ");
        	stb.append("   , T1.EXAM_TYPE ");
        	stb.append(" FROM ENTEXAM_PERFECT_EXAMTYPE_MST T1 ");
        	stb.append(" WHERE ");
        	stb.append("     T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
        	stb.append("     AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("     INT(T1.TESTDIV) ");
        	stb.append("   , T1.EXAM_TYPE ");
        	final Map rtn = new HashMap();
            for (final Iterator it = getList(db2, stb.toString()).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	getMappedList(rtn, getString(row, "TESTDIV")).add(getString(row, "EXAM_TYPE"));
            }
            return rtn;
        }

        /* 西暦表示にするのかのフラグ  */
        private List getEntexamcourseList(final DB2UDB db2) {
            final String sql = "SELECT EXAMCOURSECD, T1.* FROM ENTEXAM_COURSE_MST T1 WHERE T1.ENTEXAMYEAR = '" + _entexamyear + "' AND T1.APPLICANTDIV = '" + _applicantdiv + "' AND T1.TESTDIV = '1' ORDER BY COURSECD, MAJORCD, EXAMCOURSECD ";
            final List rtn = getList(db2, sql);
            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                m.put("EXAMCOURSE_NAME", kakkoIgai(getString(m, "EXAMCOURSE_NAME")));
            }
            return rtn;
        }

        private static final String kakkoIgai(final String s) {
            if (null == s) {
                return s;
            }
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                switch (ch) {
                case '(':
                case ')':
                case '（':
                case '）':
                    break;
                default:
                    stb.append(ch);
                }
            }
            return stb.toString();
        }

        /* 西暦表示にするのかのフラグ  */
        private Map getTestdivMap(final DB2UDB db2) {
            final String namecd1 = "2".equals(_applicantdiv) ? "L004" : "L024";
            final String namecd1_2 = "2".equals(_applicantdiv) ? "L044" : "NULL";
            final String namecd1_3 = "2".equals(_applicantdiv) ? "L059" : "NULL";
            String sql = "";
            sql += " SELECT T1.NAMECD2, T1.NAME1, T1.NAMESPARE1, T1.NAME3, NML044.NAMESPARE1 AS NML044_NAMESPARE1, NML044.NAME3 AS NML044_NAME3, NML059.NAMESPARE1 AS NML059_NAMESPARE1 ";
            if ("1".equals(_applicantdiv)) {
                sql += " , CAST(NULL AS VARCHAR(1)) AS TUISHI_DATE2 ";
                sql += " , NML039.NAME2 AS TUISHI_DATE1 ";
            } else if ("2".equals(_applicantdiv)) {
                sql += " , NML039.NAME3 AS TUISHI_DATE2 ";
                sql += " , NML039.NAMESPARE1 AS TUISHI_DATE1 ";
            }
            sql += " FROM V_NAME_MST T1 ";
            sql += " LEFT JOIN NAME_MST NML039 ON NML039.NAMECD1 = 'L039' ";
            sql += "     AND NML039.NAMECD2 = T1.NAMECD2 ";
            sql += " LEFT JOIN NAME_MST NML044 ON NML044.NAMECD1 = '" + namecd1_2 + "' ";
            sql += "     AND NML044.NAMECD2 = T1.NAMECD2 ";
            sql += " LEFT JOIN NAME_MST NML059 ON NML059.NAMECD1 = '" + namecd1_3 + "' ";
            sql += "     AND NML059.NAMECD2 = T1.NAMECD2 ";
            sql += " WHERE T1.YEAR = '" + _entexamyear + "' AND T1.NAMECD1 = '" + namecd1 + "' ";
            sql += " ORDER BY INT(T1.NAMECD2) ";

            final Map testdivMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.info(" testdiv sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TestDivDat d = new TestDivDat(rs.getString("NAMECD2"), rs.getString("NAME1"), rs.getString("NAMESPARE1"), rs.getString("NAME3"), rs.getString("NML044_NAMESPARE1"), rs.getString("NML044_NAME3"), rs.getString("NML059_NAMESPARE1"));
                    d._tuishiDate2 = rs.getString("TUISHI_DATE2");
                    d._tuishiDate1 = rs.getString("TUISHI_DATE1");
                    testdivMap.put(d._testdiv, d);
                    log.info(" testdiv =" + d._testdiv + ", testdate = (" + d._date1 + ", " + d._date2 + " , " + d._date3 + ", " + d._date4 + ", " + d._date5 + "), tuishi = (" + d._tuishiDate1 + ", " + d._tuishiDate2 + ")");
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if ("2".equals(_applicantdiv)) {
            	//高校の時に出力順を入れ替えたいので、ここで順番入れ替え。3の後に7が来るように変更。
                final Map sortMap = new LinkedMap();
                for (Iterator ite = testdivMap.keySet().iterator();ite.hasNext();) {
                    final String kStr = (String)ite.next();
                    if ("7".equals(kStr)) {
                    	continue;
                    }
                    final TestDivDat pWk = (TestDivDat)testdivMap.get(kStr);
                    sortMap.put(kStr, pWk);
                    if ("3".equals(kStr) && testdivMap.containsKey("7")) {
                        final TestDivDat pWk7 = (TestDivDat)testdivMap.get("7");
                        sortMap.put("7", pWk7);
                    }
                }
                return sortMap;
            } else {
                return testdivMap;
            }
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
                }
                return date;
            }
            return null;
        }

        private Map getExamnoOldHonordivMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT EXAMNO, MIN(HONORDIV) AS OLD_HONORDIV ");
            stb.append(" FROM V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("   AND INT(TESTDIV) < 5 ");
            stb.append(" GROUP BY EXAMNO ");

            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("EXAMNO"), rs.getString("OLD_HONORDIV"));
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}//クラスの括り

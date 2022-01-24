/*
 * $Id: f63300a0571af072c1bd5b243e8c80ac5c1a5811 $
 *
 * 作成日: 2013/07/25
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD234G {

    private static final Log log = LogFactory.getLog(KNJD234G.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String HYOTEI_TESTCD = "9900";
    private static final String AVG_DIV_GRADE = "1";
    private static final String AVG_DIV_HR_CLASS = "2";
    private static final String AVG_DIV_COURSE = "3";
    private static final String AVG_DIV_MAJOR = "4";
    private static final String SEX1 = "1";
    private static final String SEX2 = "2";
    private static final String AMIKAKE_ATTR = "Paint=(1,80,1),Bold=1";

    private static final String OUTPUTRANK_HR = "1";
    private static final String OUTPUTRANK_GRADE = "2";
    private static final String OUTPUTRANK_COURSE = "3";
    private static final String OUTPUTRANK_MAJOR = "4";

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
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _hasData = false;

            _param = createParam(db2, request);
            _param._courseWeightingSubclassCdListMap = getCourseWeightingSubclassCdListMap(db2);
            
            final List studentList = getStudentList(db2, _param);
            final List hrClassList = HrClass.getHrClassList(studentList);
            setData(db2, _param, studentList, hrClassList);
//            _param._averageDatMap = AverageDat.getAverageDatMap(db2, _param);

            svf = new Vrw32alp();
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            printMain(svf, studentList, hrClassList);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }
    
    private void printMain(final Vrw32alp svf, final List studentList, final List hrClassList) {
        
        int page = 1;
    
        printGaihyou12(svf, hrClassList, page);
    
        page += 1;
        
        printGaihyou345(svf, studentList, page);
        
        _hasData = true;
    }
    
    private List notTargetSubclasscdList(final String gradeCourse) {
        final List notTargetSubclassCdList;
        if (_param._testcd.startsWith("99")) {
            notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        } else {
            // [学期末、学年末]以外は先を表示しない
            notTargetSubclassCdList = getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "COMBINED_SUBCLASS");
        }
        return notTargetSubclassCdList;
    }
    
    private Map getCourseWeightingSubclassCdListMap(final DB2UDB db2) {
        final Map courseWeightingSubclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String flg = "9900".equals(_param._testcd) ? "2" : "1";
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            } else {
                stb.append("       T1.ATTEND_SUBCLASSCD ");
            }
            stb.append("       , ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            } else {
                stb.append("       T1.COMBINED_SUBCLASSCD ");
            }
            stb.append("   FROM ");
            stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.FLG = '" + flg + "' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return courseWeightingSubclassCdListMap;
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

    private String getTitle() {
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String gradename = StringUtils.defaultString(_param._gradename1);
        final String title = "　成績概況　" + StringUtils.defaultString(_param._testItem._testitemname);
        return nendo + gradename + title;
    }

    private void printGaihyou12(final Vrw32alp svf, final List hrClassList, int page) {
            final boolean hyotei5Dankai = "H".equals(_param._schoolKind);
            final String form1 = hyotei5Dankai ? "KNJD234G_3.frm" : "KNJD234G_1.frm";
            svf.VrSetForm(form1, 4);
            
            final BigDecimal[][] table;
            if (hyotei5Dankai) {
                final TreeSet assessLevels = new TreeSet(_param._assessMap.keySet());
                if (assessLevels.isEmpty()) {
                    table = new BigDecimal[][] {};
                } else {
                    log.debug(" assessMap = " + _param._assessMap);
                    int line = 1;
                    final Integer maxAssessLevel = (Integer) assessLevels.last();
                    table = new BigDecimal[maxAssessLevel.intValue()][];
                    for (int i = maxAssessLevel.intValue(); i > 0; i--) {
                        final Integer key = new Integer(i);
                        final Map record = (Map) _param._assessMap.get(key);
                        if (null == record) {
                            continue;
                        }
                        final String mark = (String) record.get("ASSESSMARK");
                        svf.VrsOut("DIV_NAME" + String.valueOf(line), mark);
                        if (null != record.get("ASSESSLOW") && null != record.get("ASSESSHIGH")) {
                            table[line - 1] = new BigDecimal[] {(BigDecimal) record.get("ASSESSLOW"), (BigDecimal) record.get("ASSESSHIGH")};
                        } else {
                            table[line - 1] = new BigDecimal[] {new BigDecimal(-1), new BigDecimal(-1)};
                        }
                        line += 1;
                    }
                }
            } else {
                table = new BigDecimal[10][];
                for (int i = 0; i < table.length; i++) {
                    final int upper = i == 0 ? 100 : 100 - i * 10 - 1;
                    final int lower = 100 - (i + 1) * 10;
                    table[i] = new BigDecimal[] {new BigDecimal(lower), new BigDecimal(upper)}; 
                }
            }
    
            final String roman1 = "\u2160"; // I
            final String roman2 = "\u2161"; // II
    
            svf.VrsOut("TITLE", getTitle()); // タイトル
            svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日付
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ
    
            svf.VrsOut("SUB_TITLE2", roman2 + ". クラス別成績上位者"); // サブタイトル2
            
            for (int k = 0, min = Math.min(hrClassList.size(), 9); k < min; k++) {
                final HrClass hrClass = (HrClass) hrClassList.get(k);
                final String hrn = String.valueOf(k + 1);
                svf.VrsOut("HR_NAME2_" + hrn, hrClass._hrNameabbv); // クラス名
                final int maxLine = 10;
                final List scoreStudentList = new ArrayList(hrClass._studentList);
                Collections.sort(scoreStudentList, new StudentRankComparator(OUTPUTRANK_HR, _param)); // HRランクでソート
                for (int j = 0, max = Math.min(maxLine, scoreStudentList.size()); j < max; j++) {
                    final Student student = (Student) scoreStudentList.get(j);
                    final SubclassScore subScore = student._subclassScore999999;
                    if (null == subScore) {
                        break;
                    }
                    final int line = j + 1;
                    svf.VrsOutn("RANK" + hrn, line, subScore.getRank(OUTPUTRANK_HR, _param)); // 順位
                    svf.VrsOutn("SCORE" + hrn, line, subScore._score); // 得点
                    svf.VrsOutn("AVERAGE" + hrn, line, subScore.getAvg()); // 平均
                    final int keta = getMS932ByteLength(student._name);
                    svf.VrsOutn("NAME" + hrn + (keta <= 14 ? "_1" : keta <= 20 ? "_2" : "_3"), line, student._name); // 氏名
                }
            }
    
            if (hyotei5Dankai) {
                svf.VrsOut("SUB_TITLE1", roman1 + ". クラス別成績段階"); // サブタイトル1
            } else {
                svf.VrsOut("SUB_TITLE1", roman1 + ". クラス別成績概況"); // サブタイトル1
            }
            
            //        for (int ti = 0; ti < table.length; ti++) {
            //            if (null != table[ti]) {
            //                svf.VrsOut("POINT_AREA" + String.valueOf(ti + 1), String.valueOf(table[ti][0]) + (table[ti][0] == table[ti][1] ? "" : "〜" + String.valueOf(table[ti][1]))); // 点数
            //            }
            //        }
            final ScoreDistribution distGrade = new ScoreDistribution();
            int totalKessiSize = 0;
            int totalZaisekiSize = 0;
            
            final int LINE_GRADE = 12;  // 学年
            final int hrclassSize = Math.min(LINE_GRADE - 1, hrClassList.size());
            for (int hri = 0; hri < hrclassSize; hri++) {
                final HrClass hrClass = (HrClass) hrClassList.get(hri);
                final ScoreDistribution distHrclass = new ScoreDistribution();
    
                final List scoreList999999 = getAvgList(hrClass._studentList, SUBCLASSCD999999);
                distHrclass.addScoreList(scoreList999999);
                distGrade.addScoreList(scoreList999999);

                final int zaisekiSize = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate).size();
                final int kessiSize = getKessiStudentList(hrClass._studentList).size();
    
                final String bordern = false ? "2" : "1";
                svf.VrsOut("HR_NAME1_" + bordern, hrClass._hrNameabbv); // クラス名
                for (int disti = 0; disti < table.length; disti++) {
                    if (null != table[disti]) {
                        svf.VrsOut("DIST" + bordern + "_" + String.valueOf(disti + 1), String.valueOf(distHrclass.getCount(table[disti][0], table[disti][1]))); // 分布1
                    }
                }
                svf.VrsOut("NO_SCORE" + bordern, String.valueOf(kessiSize)); // 成績無し
                svf.VrsOut("ENROLL" + bordern, String.valueOf(zaisekiSize)); // 在籍者数
    //            for (int i = 0; i <= 3; i++) {
    //                svf.VrsOut("REMARK" + bordern + "_" + String.valueOf(i + 1), null); // 備考
    //            }
                
                totalKessiSize += kessiSize;
                totalZaisekiSize += zaisekiSize;
                svf.VrEndRecord();
            }
            for (int i = hrclassSize; i < LINE_GRADE - 1; i++) {
                svf.VrsOut("HR_NAME1_1", "\n"); // クラス名
                svf.VrEndRecord();
            }
    
            //log.debug(" dgrade = " + distGrade._scoreList);
            // 度数分布学年
            final String borderg = false ? "2" : "1";
            svf.VrsOut("HR_NAME1_" + borderg, "学年"); // クラス名
            for (int disti = 0; disti < table.length; disti++) {
                if (null != table[disti]) {
                    svf.VrsOut("DIST" + borderg + "_" + String.valueOf(disti + 1), String.valueOf(distGrade.getCount(table[disti][0], table[disti][1]))); // 分布1
                }
            }
            svf.VrsOut("NO_SCORE" + borderg, String.valueOf(totalKessiSize)); // 成績無し
            svf.VrsOut("ENROLL" + borderg, String.valueOf(totalZaisekiSize)); // 在籍者数
    //      for (int i = 0; i <= 3; i++) {
    //          svf.VrsOut("REMARK" + String.valueOf(i + 1), null); // 備考
    //      }
            svf.VrEndRecord();
        }

    private void printGaihyou345(final Vrw32alp svf, final List studentList, int page) {
        final String form2 = "KNJD234G_2.frm";
    
        final List recordPageList = getGroupListByCount(getGaihyou345RecordList(studentList), 54);
        for (int pi = 0; pi < recordPageList.size(); pi++) {
            final List recordList = (List) recordPageList.get(pi);
            
            svf.VrSetForm(form2, 4);
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ
    
            for (int i = 0, recMax = recordList.size(); i < recMax; i++) {
                final Map record = (Map) recordList.get(i);
                //log.info(" record = " + record);
                for (final Iterator it = record.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    svf.VrsOut((String) e.getKey(), (String) e.getValue());
                }
                svf.VrEndRecord();
            }
            page += 1;
        }
    }

    private List getGaihyou345RecordList(final List studentList) {
        final List recordList = new ArrayList();
        final String roman3 = "\u2162"; // III
        final String roman4 = "\u2163"; // IV
        final String roman5 = "\u2164"; // V
    
        final Map blankRecord = new HashMap();
        blankRecord.put("BRANK", "-"); // 空白行用
        
        final List studentListZaiseki = HrClass.getZaisekiList(studentList, null, _param, _param._edate);
        
        newRecord(recordList).put("SUB_TITLE1", roman3 + ". 成績不振者"); // サブタイトル1
        addGaihyou3Record(studentListZaiseki, recordList, blankRecord);
        recordList.add(blankRecord);
    
        
        newRecord(recordList).put("SUB_TITLE1", roman4 + ". 欠席・遅刻・早退"); // サブタイトル1
        addGaihyou4Record(studentListZaiseki, recordList, blankRecord);
        recordList.add(blankRecord);
        
        newRecord(recordList).put("SUB_TITLE1", roman5 + ". 成績の出ない生徒"); // サブタイトル1
        addGaihyou5Record(studentListZaiseki, recordList, blankRecord);
    
        return recordList;
    }

    private void addGaihyou3Record(final List studentList, final List recordList, final Map blankRecord) {
        
        final Map rec3 = newRecord(recordList);
        String maru1 = "";
//        if ("J".equals(_param._schoolKind)) {
        maru1 = "\u2460 "; // ○の中に1
//        }
        rec3.put("CONDITION", maru1 + String.valueOf(_param._fushinScore) + "点未満(" + String.valueOf(_param._fushinSu) + "科目以上)"); // 条件
        
        final Map rec4 = newRecord(recordList);
        rec4.put("HEADER1_1", "組"); // ヘッダ1
        rec4.put("HEADER1_2", "氏名"); // ヘッダ2
        rec4.put("HEADER1_3", "欠点科目・得点"); // ヘッダ3
        rec4.put("HEADER1_4", "科目数"); // ヘッダ4
    
        final List fushinStudentList = getFushinKamokuStudentList(studentList, _param._fushinScore, _param._fushinSu);
        if (fushinStudentList.size() == 0) {
            putName(newRecord(recordList), "NAME6", "該当なし");
            
        } else {
            for (int i = 0; i < fushinStudentList.size(); i++) {
                final Student student = (Student) fushinStudentList.get(i);
                
                final List fushinAllSubclassList = getFushinKamokuList(Collections.singletonList(student), _param._fushinScore);
                final List fushinSubclassGroup = getGroupListByCount(fushinAllSubclassList, 10);
                for (int li = 0, max = fushinSubclassGroup.size(); li < max; li++) {
                    final Map rec11 = newRecord(recordList);
                    if (li == 0) {
                        rec11.put("HR_NAME4", student._hrClassInt); // クラス名
                        putName(rec11, "NAME6", student._name);
                        rec11.put("SUBJECT_NUM2", String.valueOf(fushinAllSubclassList.size())); // 科目数
    
                    }
                    final List kessiSubclassList = (List) fushinSubclassGroup.get(li);
                    for (int si = 0; si < kessiSubclassList.size(); si++) {
                        final SubclassScore subScore = (SubclassScore) kessiSubclassList.get(si);
                        final String abbv = subScore._subclass._subclassabbv;
                        rec11.put("SHORT_SUBJECT" + String.valueOf(si + 1), abbv + StringUtils.repeat(" ", 6 - getMS932ByteLength(abbv)) + subScore._score); // 不足科目
                    }
                }
            }
        }
    
        final String maru2 = "\u2461 "; // ○の中に2
        
        final Map rec3_2 = newRecord(recordList);
        rec3_2.put("CONDITION", maru2 + " 平均 " + String.valueOf(_param._fushinHeikin) + "点未満"); // 条件

        final Map rec6 = newRecord(recordList);
        rec6.put("HEADER2_1", "組"); // ヘッダ1
        rec6.put("HEADER2_2", "氏名"); // ヘッダ2
        rec6.put("HEADER2_3", "平均点"); // ヘッダ3
        
        final List fushinHeikinStudentList = getFushinHeikinStudentList(studentList, _param._fushinHeikin);

        if (fushinHeikinStudentList.size() == 0) {
            putName(newRecord(recordList), "NAME2", "該当なし");
            
        } else {
            for (int i = 0, max = Math.max(fushinHeikinStudentList.size(), 1); i < max; i++) {
                final Student student = (Student) fushinHeikinStudentList.get(i);
                
                final Map rec7 = newRecord(recordList);
                rec7.put("HR_NAME2", student._hrClassInt); // クラス名
                rec7.put("AVERAGE2", student._subclassScore999999.getAvg()); // 平均
                putName(rec7, "NAME2", student._name);
            }
        }
    }

    private void addGaihyou4Record(final List studentList, final List recordList, final Map blankRecord) {
    
        final Map rec8 = newRecord(recordList);
        rec8.put("CONDITION2_1", "欠席　" + String.valueOf(_param._kesseki) + "日以上"); // 条件
        rec8.put("CONDITION2_2", "遅刻　" + String.valueOf(_param._chikoku) + "日以上"); // 条件
        rec8.put("CONDITION2_3", "早退　" + String.valueOf(_param._soutai) + "日以上"); // 条件
    
        final Map rec9 = newRecord(recordList);
        rec9.put("HEADER3_1", "組"); // ヘッダ1
        rec9.put("HEADER3_2", "氏名"); // ヘッダ2
        rec9.put("HEADER3_3", "日数"); // ヘッダ3
        rec9.put("HEADER3_4", "組"); // ヘッダ4
        rec9.put("HEADER3_5", "氏名"); // ヘッダ5
        rec9.put("HEADER3_6", "日数"); // ヘッダ6
        rec9.put("HEADER3_7", "組"); // ヘッダ7
        rec9.put("HEADER3_8", "氏名"); // ヘッダ8
        rec9.put("HEADER3_9", "日数"); // ヘッダ9
    
        final List studentListKesseki = getAttendOverStudentList(_param, studentList, 0);
        final List studentListChikoku = getAttendOverStudentList(_param, studentList, 1);
        final List studentListSoutai = getAttendOverStudentList(_param, studentList, 2);
    
        for (int i = 0, max = Math.max(1, Math.max(studentListKesseki.size(), Math.max(studentListChikoku.size(), studentListSoutai.size()))); i < max; i++) {
            final Map rec10 = newRecord(recordList);
            if (i < studentListKesseki.size()) {
                final Student student = (Student) studentListKesseki.get(i);
                rec10.put("HR_NAME3_1", student._hrClassInt); // クラス名
                putName(rec10, "NAME3", student._name);
                rec10.put("DAY1", String.valueOf(student._attendance._absence)); // 日数
            } else if (i == 0) {
                putName(rec10, "NAME3", "該当なし"); // 氏名
            }
            
            if (i < studentListChikoku.size()) {
                final Student student = (Student) studentListChikoku.get(i);
                rec10.put("HR_NAME3_2", student._hrClassInt); // クラス名
                putName(rec10, "NAME4", student._name);
                rec10.put("DAY2", String.valueOf(student._attendance._late)); // 日数
            } else if (i == 0) {
                putName(rec10, "NAME4", "該当なし");
            }
            
            if (i < studentListSoutai.size()) {
                final Student student = (Student) studentListSoutai.get(i);
                rec10.put("HR_NAME3_3", student._hrClassInt); // クラス名
                putName(rec10, "NAME5", student._name);
                rec10.put("DAY3", String.valueOf(student._attendance._early)); // 日数
            } else if (i == 0) {
                putName(rec10, "NAME5", "該当なし");
            }
        }
    }

    private void addGaihyou5Record(final List studentList, final List recordList, final Map blankRecord) {
        
        final Map rec4 = newRecord(recordList);
        rec4.put("HEADER1_1", "組"); // ヘッダ1
        rec4.put("HEADER1_2", "氏名"); // ヘッダ2
        rec4.put("HEADER1_3", "欠点科目・得点"); // ヘッダ3
        rec4.put("HEADER1_4", "科目数"); // ヘッダ4
    
        final List kessiStudentList = getKessiStudentList(studentList);
        boolean addFlg = false;
        if (kessiStudentList.size() != 0) {
            for (int i = 0; i < kessiStudentList.size(); i++) {
                final Student student = (Student) kessiStudentList.get(i);
                
                //log.info(" student = " + student._schregno + " / " + student._name);
                
                final List kessiAllSubclassList = student.getKesshiSubclassList();
                final List kessiSubclassGroup = getGroupListByCount(kessiAllSubclassList, 10);
                for (int li = 0, max = kessiSubclassGroup.size(); li < max; li++) {
                    final Map rec11 = newRecord(recordList);
                    if (li == 0) {
                        rec11.put("HR_NAME4", student._hrClassInt); // クラス名
                        putName(rec11, "NAME6", student._name);
                        rec11.put("SUBJECT_NUM2", String.valueOf(kessiAllSubclassList.size())); // 科目数
    
                    }
                    final List kessiSubclassList = (List) kessiSubclassGroup.get(li);
                    for (int si = 0; si < kessiSubclassList.size(); si++) {
                        final SubclassScore subScore = (SubclassScore) kessiSubclassList.get(si);
                        //log.info("  subclass = " + subScore._subclass._subclasscd + " / " + subScore._subclass._subclassname);
                        rec11.put("SHORT_SUBJECT" + String.valueOf(si + 1), subScore._subclass._subclassabbv); // 不足科目
                    }
                    addFlg = true;
                }
            }
        }
        if (!addFlg) {
            final Map rec11 = newRecord(recordList);
            putName(rec11, "NAME6", "該当なし");
        }
    }

    private static boolean isSubclass999999(final String subclasscd, final Param param) {
        if ("1".equals(param._useCurriculumcd)) {
            final String split = StringUtils.split(subclasscd, "-")[3];
            if (SUBCLASSCD999999.equals(split)) {
                return true;
            }
        }
        if (SUBCLASSCD999999.equals(subclasscd)) {
            return true;
        }
        return false;
    }
    
    private void setData(final DB2UDB db2, final Param param, final List studentList, final List hrClassList) {
        log.debug(" setData ");
        PreparedStatement ps = null;
        ResultSet rs = null;

        // １日出欠
        try {
            boolean semesFlg = ((Boolean) param._hasuuMap.get("semesFlg")).booleanValue();
            String sql = AttendAccumulate.getAttendSemesSql(
                    semesFlg,
                    param._definecode0,
                    param._knjSchoolMst,
                    param._year,
                    param.SSEMESTER,
                    param._semester,
                    (String) param._hasuuMap.get("attendSemesInState"),
                    param._periodInState,
                    (String) param._hasuuMap.get("befDayFrom"),
                    (String) param._hasuuMap.get("befDayTo"),
                    (String) param._hasuuMap.get("aftDayFrom"),
                    (String) param._hasuuMap.get("aftDayTo"),
                    "?",
                    "?",
                    null,
                    "SEMESTER",
                    param._useCurriculumcd,
                    param._useVirus,
                    param._useKoudome
            );
            ps = db2.prepareStatement(sql);
            for (final Iterator hIt = hrClassList.iterator(); hIt.hasNext();) {
                final HrClass hrClass = (HrClass) hIt.next();
                log.debug(" set Attendance " + hrClass);

                //log.debug(" attend semes sql = " + sql);
                ps.setString(1, hrClass._grade);
                ps.setString(2, hrClass._hrClass);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), hrClass._studentList);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final int lesson = rs.getInt("LESSON");
                    final int mourning = rs.getInt("MOURNING");
                    final int suspend = rs.getInt("SUSPEND") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0);
                    final int abroad = rs.getInt("TRANSFER_DATE");
                    final int mlesson = rs.getInt("MLESSON");
                    final int absence = rs.getInt("SICK");
                    final int attend = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                        
                    final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early);
                    // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                    student._attendance = attendance;
                }
            }
            
        } catch (SQLException e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 成績
        try {
            final String sql = SubclassScore.getSubclassScoreSql(param);
            log.info(" setRecord  sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = Student.getStudent(schregno, studentList);
                if (null == student) {
                    continue;
                }
                
                final String subclasscd = rs.getString("SUBCLASSCD");
                if (notTargetSubclasscdList(student.gradeCourse()).contains(subclasscd)) {
                    continue;
                }
                final String classabbv = rs.getString("CLASSABBV");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String score = rs.getString("SCORE");
                final BigDecimal avg = rs.getBigDecimal("AVG");
                final SubclassScore.DivRank gradeRank = new SubclassScore.DivRank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"));
                final SubclassScore.DivRank classRank = new SubclassScore.DivRank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"));
                final SubclassScore.DivRank courseRank = new SubclassScore.DivRank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"));
                final SubclassScore.DivRank majorRank = new SubclassScore.DivRank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"));
                
                if (!param._subclassMap.containsKey(subclasscd)) {
                    param._subclassMap.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv));
                }
                final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
                subclass._subclassScoreAllNull = rs.getString("SCORE_ALL_NULL");
                
                final SubclassScore subclassscore = new SubclassScore(student, subclass, score, avg, gradeRank,
                        classRank, courseRank, majorRank);
                
                if (isSubclass999999(subclasscd, param)) {
                    student._subclassScore999999 = subclassscore;
                } else {
                    student._subclassScore.put(subclasscd, subclassscore);
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        final TreeSet entDateSet = new TreeSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._entdate) {
                entDateSet.add(student._entdate);
            }
        }
        if (!entDateSet.isEmpty()) {
            final String entDateMin = (String) entDateSet.first();
            if (param._yearSdate.compareTo(entDateMin) < 1) {
                param._yearSdate = entDateMin;
            }
        }
    }
    
    private static List getStudentList(final DB2UDB db2, final Param param) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_TRANSFER1 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '9' ");
            stb.append("   WHERE ");
            stb.append("     T2.SDATE BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ), SCHREG_TRANSFER2 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     '" + param._edate + "' BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_NAMEABBV, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     T3.ENT_DATE, ");
            stb.append("     T3.ENT_DIV, ");
            stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     T3.GRD_DATE, ");
            stb.append("     T3.GRD_DIV, ");
            stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
            stb.append("     T5.TRANSFERCD AS TRANSFERCD1, ");
            stb.append("     NMA004_1.NAME1 AS TRANSFER_NAME1, ");
            stb.append("     T5.TRANSFERREASON AS TRANSFERREASON1, ");
            stb.append("     T5.TRANSFER_SDATE AS TRANSFER_SDATE1, ");
            stb.append("     T5.TRANSFER_EDATE AS TRANSFER_EDATE1, ");
            stb.append("     T7.TRANSFERCD AS TRANSFERCD2, ");
            stb.append("     NMA004_2.NAME1 AS TRANSFER_NAME2, ");
            stb.append("     T7.TRANSFERREASON AS TRANSFERREASON2, ");
            stb.append("     T7.TRANSFER_SDATE AS TRANSFER_SDATE2, ");
            stb.append("     T7.TRANSFER_EDATE AS TRANSFER_EDATE2, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T9.MAJORNAME, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T8.COURSECODENAME ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER1 T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T5.TRANSFER_SDATE = T4.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER2 T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T7.TRANSFER_SDATE = T6.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
            stb.append(" LEFT JOIN MAJOR_MST T9 ON T9.COURSECD = T1.COURSECD AND T9.MAJORCD = T1.MAJORCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_1 ON NMA004_1.NAMECD1 = 'A004' AND NMA004_1.NAMECD2 = T5.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_2 ON NMA004_2.NAMECD1 = 'A004' AND NMA004_2.NAMECD2 = T7.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T3.SEX ");
            stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T3.ENT_DIV ");
            stb.append(" LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T3.GRD_DIV ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");

            log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrClassInt = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String sexName = rs.getString("SEX_NAME");
                final String entdiv = rs.getString("ENT_DIV");
                final String entdivName = rs.getString("ENT_DIV_NAME");
                final String entdate = rs.getString("ENT_DATE");
                final String grddiv = rs.getString("GRD_DIV");
                final String grddivName = rs.getString("GRD_DIV_NAME");
                final String grddate = rs.getString("GRD_DATE");
                final String transfercd1 = rs.getString("TRANSFERCD1");
                final String transferName1 = rs.getString("TRANSFER_NAME1");
                final String transferreason1 = rs.getString("TRANSFERREASON1");
                final String transferSdate1 = rs.getString("TRANSFER_SDATE1");
                final String transferEdate1 = rs.getString("TRANSFER_EDATE1");
                final String transfercd2 = rs.getString("TRANSFERCD2");
                final String transferName2 = rs.getString("TRANSFER_NAME2");
                final String transferreason2 = rs.getString("TRANSFERREASON2");
                final String transferSdate2 = rs.getString("TRANSFER_SDATE2");
                final String transferEdate2 = rs.getString("TRANSFER_EDATE2");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String majorname = rs.getString("MAJORNAME");
                final String coursecode = rs.getString("COURSECODE");
                final String coursecodename = rs.getString("COURSECODENAME");
                final Student student = new Student(grade, hrClass, hrClassInt, hrNameabbv, attendno, schregno, hrName, name, sex, sexName, entdiv, entdivName, entdate, grddiv, grddivName, grddate,
                        transfercd1, transferName1, transferreason1 ,transferSdate1, transferEdate1,
                        transfercd2, transferName2, transferreason2, transferSdate2, transferEdate2,
                        coursecd, majorcd, majorname, coursecode, coursecodename);
                studentList.add(student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }
    
    /**
     * 生徒のリストから生徒の科目の得点のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の得点のリストを得る
     */
    private static List getScoreList(final List studentList, final String subclasscd) {
        final List scoreList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String score = student.getScore(subclasscd);
            if (null != score) {
                scoreList.add(score);
            }
        }
        return scoreList;
    }
    
    /**
     * 生徒のリストから生徒の科目の平均のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の平均のリストを得る
     */
    private static List getAvgList(final List studentList, final String subclasscd) {
        final List avgList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String avg = student.getAvg(subclasscd);
            if (null != avg) {
                avgList.add(avg);
            }
        }
        return avgList;
    }
    
    private static List getGroupListByCount(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
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

    private static List getSubclassList(final Param param, final List studentList) {
        final Set set = new TreeSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._subclassScore.values().iterator(); sit.hasNext();) {
                final SubclassScore subScore = (SubclassScore) sit.next();
                if (null != subScore._subclass._subclasscd) {
                    set.add(subScore._subclass._subclasscd);
                }
            }
        }
        final List rtn = new ArrayList();
        for (final Iterator it = set.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
            if (null != subclass) {
                rtn.add(subclass);
            }
        }
        return rtn;
    }

    private static class Student {
        
        private static DecimalFormat attendnodf = new DecimalFormat("00");
        
        final String _grade;
        final String _hrClass;
        final String _hrClassInt;
        final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _entdiv;
        final String _entdivName;
        final String _entdate;
        final String _grddiv;
        final String _grddivName;
        final String _grddate;
        // 年度開始日時点の異動データ
        final String _transfercd1;
        final String _transfername1;
        final String _transferreason1;
        final String _transferSdate1;
        final String _transferEdate1;
        // パラメータ指定日付時点の異動データ
        final String _transfercd2;
        final String _transfername2;
        final String _transferreason2;
        final String _transferSdate2;
        final String _transferEdate2;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodename;
        final Map _subclassScore;
        
        private SubclassScore _subclassScore999999;
        private Attendance _attendance;

        Student(
            final String grade,
            final String hrClass,
            final String hrClassInt,
            final String hrNameabbv,
            final String attendno,
            final String schregno,
            final String hrName,
            final String name,
            final String sex,
            final String sexName,
            final String entdiv,
            final String entdivName,
            final String entdate,
            final String grddiv,
            final String grddivName,
            final String grddate,
            final String transfercd1,
            final String transfername1,
            final String transferreason1,
            final String transferSdate1,
            final String transferEdate1,
            final String transfercd2,
            final String transfername2,
            final String transferreason2,
            final String transferSdate2,
            final String transferEdate2,
            final String coursecd,
            final String majorcd,
            final String majorname,
            final String coursecode,
            final String coursecodename
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrClassInt = hrClassInt;
            _hrNameabbv = hrNameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _entdiv = entdiv;
            _entdivName = entdivName;
            _entdate = entdate;
            _grddiv = grddiv;
            _grddivName = grddivName;
            _grddate = grddate;
            _transfercd1 = transfercd1;
            _transfername1 = transfername1;
            _transferreason1 = transferreason1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transfercd2 = transfercd2;
            _transfername2 = transfername2;
            _transferreason2 = transferreason2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _subclassScore = new TreeMap();
        }
        
        public String gradeCourse() {
            return _grade + _coursecd + _majorcd + _coursecode;
        }
        
        public String majorcd() {
            return _coursecd + _majorcd;
        }

        public String getHrclassAttendnoCd() {
            return
            StringUtils.defaultString((NumberUtils.isDigits(_hrClass)) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) +
            StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        public List getKesshiSubclassList() {
            final List list = new ArrayList();
            for (final Iterator it = _subclassScore.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final SubclassScore subScore = (SubclassScore) _subclassScore.get(subclasscd);
                if (subScore._subclass._combinedSubclassCourse.contains(gradeCourse())) {
                    continue;
                }
                if (null == subScore._score && !"1".equals(subScore._subclass._subclassScoreAllNull)) {
                    list.add(subScore);
                }
            }
            return list;
        }

//        private List getKettenSubclassList(final Param param) {
//            final List list = new ArrayList();
//            for (final Iterator it = _subclassScore.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                final SubclassScore subScore = (SubclassScore) _subclassScore.get(subclasscd);
//                if (subScore.isKetten(param)) {
//                    list.add(subScore);
//                }
//            }
//            return list;
//        }

        public String getScore(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = (SubclassScore) _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : subScore._score;
        }

        public String getAvg(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = (SubclassScore) _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : subScore.getAvg();
        }

        public static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
//        /**
//         * 
//         * @param flg 1:学年開始日 2:指定日付 0:どちらか
//         * @return
//         */
//        public boolean isRyugakuKyugaku(final int flg) {
//            return flg == 1 && null != _transfercd1 || flg == 2 && null != _transfercd2 || flg == 0 && (null != _transfercd1 || null != _transfercd2);
//        }
//
//        public boolean isTenhennyuugaku(final Param param) {
//            final boolean isTenhennyuugaku = ("4".equals(_entdiv) || "5".equals(_entdiv)) && (null == _entdate || param._yearSdate.compareTo(_entdate) <= 0);
////            if (null != _entdate) {
////                log.info(" " + toString() + " Tenhennyuugaku " + _entdate + " ( " + param._yearSdate + ")");
////            }
//            return isTenhennyuugaku;
//        }

        public boolean isJoseki(final Param param, final String date) {
            final boolean isJoseki = null != _grddiv && !"4".equals(_grddiv) && null != _grddate && ((param._yearSdate.compareTo(_grddate) <= 0 && (null == date || _grddate.compareTo(date) <= 0)));
//            if (isJoseki) {
//                log.debug(" " + toString() + " joseki = " + isJoseki + " : " + _grddiv + " / "   + _grddate + " ( " + param._yearSdate + ", " + date + ")");
//            }
            return isJoseki;
        }
        
        public String toString() {
            return "Student(" + _schregno + ")";
        }

    }
    
    /**
     * 1日出欠データ
     */
    private static class Attendance {
        
        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _early;
        
        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _early = early;
        }
        
        public String toString() {
            return "[lesson=" + _lesson + 
            ",mlesson=" + _mlesson + 
            ",mourning=" + _mourning + 
            ",suspend=" + _suspend + 
            ",abroad=" + _abroad +
            ",absence=" + _absence + 
            ",attend=" + _attend + 
            ",late=" + _late +
            ",leave=" + _early;
        }
    }
    
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final List _studentList;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrNameabbv
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _studentList = new ArrayList();
        }
        
        public String getCode() {
            return _grade + _hrClass;
        }
        
        public static List getZaisekiList(final List studentList, final String sex, final Param param, final String date) {
            final List list = new ArrayList();
            for (final Iterator it =studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if ((null == sex || sex.equals(student._sex)) && !student.isJoseki(param, date)) {
                    list.add(student);
                }
            }
            return list;
        }

//        public static List getStudentCountRyugakuKyugakuSex(final List studentList, final int flg, final String sex) {
//            final List list = new ArrayList();
//            for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                final Student student = (Student) it.next();
//                if (sex.equals(student._sex)) {
//                    if (student.isRyugakuKyugaku(flg)) {
//                        list.add(student);
//                    }
//                }
//            }
//            return list;
//        }
        
        public static HrClass getHrClass(final String grade, final String hrClass, final List hrClassList) {
            for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
                HrClass hrclass = (HrClass) it.next();
                if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
                    return hrclass;
                }
            }
            return null;
        }
        
        public static List getHrClassList(final List studentList) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == getHrClass(student._grade, student._hrClass, list)) {
                    list.add(new HrClass(student._grade, student._hrClass, student._hrNameabbv));
                }
                final HrClass hrclass = getHrClass(student._grade, student._hrClass, list);
                hrclass._studentList.add(student);
            }
            return list;
        }

        public String toString() {
            return "HrClass(" + _grade + _hrClass + ":" + _hrNameabbv + ")";
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        String _subclassScoreAllNull;
        // この科目を合併元科目とするコース
        final Set _attendSubclassCourse = new HashSet();
        // この科目を合併先科目とするコース
        final Set _combinedSubclassCourse = new HashSet();
        Subclass(
            final String subclasscd,
            final String classabbv,
            final String subclassname,
            final String subclassabbv
        ) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }
        
        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) return -1;
            final Subclass s = (Subclass) o;
            return _subclasscd.compareTo(s._subclasscd);
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }
    }
    
    /**
     * 生徒の科目の得点
     */
    private static class SubclassScore {
        final Student _student;
        final Subclass _subclass;
        final String _score;
        final BigDecimal _avg;
        final DivRank _gradeRank;
        final DivRank _classRank;
        final DivRank _courseRank;
        final DivRank _majorRank;


        SubclassScore(
            final Student student,
            final Subclass subclass,
            final String score,
            final BigDecimal avg,
            final DivRank gradeRank,
            final DivRank classRank,
            final DivRank courseRank,
            final DivRank majorRank
        ) {
            _student = student;
            _subclass = subclass;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
        }
        
        public String getRank(final String rankDiv, final Param param) {
            final DivRank divRank;
            if (OUTPUTRANK_HR.equals(rankDiv)) {
                divRank = _classRank;
            } else if (OUTPUTRANK_MAJOR.equals(rankDiv)) {
                divRank = _majorRank;
            } else if (OUTPUTRANK_COURSE.equals(rankDiv)) {
                divRank = _courseRank;
            } else {
                divRank = _gradeRank;
            }
            return divRank.get(param);
        }
        
        public String getAvg() {
            return null == _avg ? null : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public static String getSubclassScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
//            stb.append("   WITH REL_COUNT AS (");
//            stb.append("   SELECT SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("     , COUNT(*) AS COUNT ");
//            stb.append("          FROM RELATIVEASSESS_MST ");
//            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
//            stb.append("   GROUP BY SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("   ) ");

            stb.append("   WITH SCORE_ALL_NULL_SUBCLASS AS (");
            stb.append("   SELECT T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    FROM RECORD_SCORE_DAT T1 ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
//                stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
//                stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
//                stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
//            }
            stb.append("    WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("        AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("   GROUP BY T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    HAVING MAX(T1." + (param._testcd.startsWith("99") ? "VALUE" : "SCORE") + ") IS NULL ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                if ("1".equals(param._kariHyotei)) {
//                    stb.append("    AND MIN(PROV.PROV_FLG) IS NOT NULL ");
//                } else {
//                    stb.append("    OR MIN(PROV.PROV_FLG) IS NOT NULL ");
//                }
//            }
            stb.append("   ) ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                getHyoteiDataSql(param, stb);
//            }
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T6.CLASSABBV, ");
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T3.SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
            stb.append("     CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN '1' END AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND BASE.ENT_DATE > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T20.TESTKINDCD || T20.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("     AND T20.SCHREGNO = T1.SCHREGNO ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append(" LEFT JOIN HYOTEI_DATA T2 ON T2.SUBCLASSCD = T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD ");
//                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
//            } else {
                stb.append(" LEFT JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T20.YEAR ");
                stb.append("     AND T2.SEMESTER = T20.SEMESTER ");
                stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD = T20.TESTKINDCD || T20.TESTITEMCD ");
                stb.append("     AND T2.CLASSCD = T20.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T20.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T20.CURRICULUM_CD ");
                stb.append("     AND T2.SUBCLASSCD = T20.SUBCLASSCD ");
                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
//            }
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append("     AND T3.CLASSCD = T20.CLASSCD ");
            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW T4 ON T4.YEAR = T20.YEAR ");
            stb.append("     AND T4.SEMESTER = T20.SEMESTER ");
            stb.append("     AND T4.TESTKINDCD = T20.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD = T20.TESTITEMCD ");
            stb.append("  LEFT JOIN CLASS_MST T6 ON T6.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SCORE_ALL_NULL_SUBCLASS T9 ON T9.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T9.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T9.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T9.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     '00' || '-' || '00' || '-' || '00' || '-' || ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSABBV, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
//            stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK_CD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND BASE.ENT_DATE > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append(" INNER JOIN HYOTEI_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            } else {
                stb.append(" INNER JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD = '" + param._testcd + "' ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
//            }
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T2.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            return stb.toString();
        }

//        public static void getHyoteiDataSql(final Param param, final StringBuffer stb) {
//            stb.append(" , ");
//            stb.append("   REGD AS ( ");
//            stb.append("     SELECT ");
//            stb.append("      T1.SCHREGNO, ");
//            stb.append("      T1.GRADE, ");
//            stb.append("      T1.HR_CLASS, ");
//            stb.append("      T1.COURSECD, ");
//            stb.append("      T1.MAJORCD, ");
//            stb.append("      T1.COURSECODE ");
//            stb.append("     FROM SCHREG_REGD_DAT T1 ");
//            stb.append("     WHERE ");
//            stb.append("      T1.YEAR = '" + param._year + "' ");
//            stb.append("      AND T1.SEMESTER = '" + param.regdSemester() + "' ");
//            stb.append("      AND T1.GRADE = '" + param._grade + "' ");
//            stb.append(" ), REC AS ( ");
//            stb.append("     SELECT ");
//            stb.append("      T1.SCHREGNO, ");
//            stb.append("      T1.CLASSCD, ");
//            stb.append("      T1.SCHOOL_KIND, ");
//            stb.append("      T1.CURRICULUM_CD, ");
//            stb.append("      T1.SUBCLASSCD, ");
//            stb.append("      T1.SCORE ");
//            stb.append("     FROM RECORD_SCORE_DAT T1 ");
//            stb.append("     INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
//            stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
//            stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
//            stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
//            stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
//            stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     WHERE ");
//            stb.append("      T1.YEAR = '" + param._year + "' ");
//            stb.append("      AND T1.SEMESTER = '9' ");
//            stb.append("      AND T1.TESTKINDCD = '99' ");
//            stb.append("      AND T1.TESTITEMCD = '00' ");
//            if ("1".equals(param._kariHyotei)) {
//                stb.append(" AND PROV.PROV_FLG = '1' ");
//            } else {
//                stb.append(" AND PROV.PROV_FLG IS NULL ");
//            }
//            stb.append(" ), REC2 AS ( ");
//            stb.append("     SELECT SCHREGNO, CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SCORE, SCORE AS AVG ");
//            stb.append("     FROM REC T1 ");
//            stb.append("     WHERE SCORE IS NOT NULL ");
//            stb.append("     UNION ALL ");
//            stb.append("     SELECT SCHREGNO, '999999' AS SUBCLASSCD, SUM(SCORE) AS SCORE, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
//            stb.append("     FROM REC T1 ");
//            stb.append("     GROUP BY SCHREGNO ");
//            stb.append("     HAVING COUNT(SCORE) <> 0 ");
//            stb.append(" ), SCHREG_TOTAL_RANK AS ( ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'GRADE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE ORDER BY SCORE DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'HR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ORDER BY SCORE DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'COURSE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ORDER BY SCORE DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'MAJOR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ORDER BY SCORE DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append(" ), SCHREG_AVG_RANK AS ( ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'GRADE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE ORDER BY AVG DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'HR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ORDER BY AVG DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'COURSE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ORDER BY AVG DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'MAJOR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ORDER BY AVG DESC) AS RANK ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append(" ), SUBCLASS_AVG AS ( ");
//            stb.append("     SELECT T1.SUBCLASSCD, 'GRADE' AS DIV, T2.GRADE AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SUBCLASSCD, 'HR' AS DIV, T2.GRADE || T2.HR_CLASS AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SUBCLASSCD, 'COURSE' AS DIV, T2.GRADE || T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
//            stb.append("     UNION  ");
//            stb.append("     SELECT T1.SUBCLASSCD, 'MAJOR' AS DIV, T2.GRADE || T2.COURSECD || T2.MAJORCD || '0000' AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
//            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ");
//            stb.append(" ) ");
//            stb.append(" , HYOTEI_DATA AS ( ");
//            stb.append(" SELECT  ");
//            stb.append("   T1.SCHREGNO, ");
//            stb.append("   T1.SUBCLASSCD, ");
//            stb.append("   T1.SCORE, ");
//            stb.append("   T1.AVG, ");
//            stb.append("   R1.RANK AS GRADE_RANK, ");
//            stb.append("   A1.RANK AS GRADE_AVG_RANK, ");
//            stb.append("   R2.RANK AS CLASS_RANK, ");
//            stb.append("   A2.RANK AS CLASS_AVG_RANK, ");
//            stb.append("   R3.RANK AS COURSE_RANK, ");
//            stb.append("   A3.RANK AS COURSE_AVG_RANK, ");
//            stb.append("   R4.RANK AS MAJOR_RANK, ");
//            stb.append("   A4.RANK AS MAJOR_AVG_RANK, ");
//            stb.append("   S1.AVG AS GRADE_AVG, ");
//            stb.append("   S2.AVG AS HR_AVG, ");
//            stb.append("   S3.AVG AS COURSE_AVG, ");
//            stb.append("   S4.AVG AS MAJOR_AVG ");
//            stb.append(" FROM REC2 T1 ");
//            stb.append(" INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
//            stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R1 ON R1.SCHREGNO = T1.SCHREGNO AND R1.SUBCLASSCD = T1.SUBCLASSCD AND R1.DIV = 'GRADE' ");
//            stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R2 ON R2.SCHREGNO = T1.SCHREGNO AND R2.SUBCLASSCD = T1.SUBCLASSCD AND R2.DIV = 'HR' ");
//            stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R3 ON R3.SCHREGNO = T1.SCHREGNO AND R3.SUBCLASSCD = T1.SUBCLASSCD AND R3.DIV = 'COURSE' ");
//            stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R4 ON R4.SCHREGNO = T1.SCHREGNO AND R4.SUBCLASSCD = T1.SUBCLASSCD AND R4.DIV = 'MAJOR' ");
//            stb.append(" LEFT JOIN SCHREG_AVG_RANK A1 ON A1.SCHREGNO = T1.SCHREGNO AND A1.SUBCLASSCD = T1.SUBCLASSCD AND A1.DIV = 'GRADE' ");
//            stb.append(" LEFT JOIN SCHREG_AVG_RANK A2 ON A2.SCHREGNO = T1.SCHREGNO AND A2.SUBCLASSCD = T1.SUBCLASSCD AND A2.DIV = 'HR' ");
//            stb.append(" LEFT JOIN SCHREG_AVG_RANK A3 ON A3.SCHREGNO = T1.SCHREGNO AND A3.SUBCLASSCD = T1.SUBCLASSCD AND A3.DIV = 'COURSE' ");
//            stb.append(" LEFT JOIN SCHREG_AVG_RANK A4 ON A4.SCHREGNO = T1.SCHREGNO AND A4.SUBCLASSCD = T1.SUBCLASSCD AND A4.DIV = 'MAJOR' ");
//            stb.append(" LEFT JOIN SUBCLASS_AVG S1 ON S1.SUBCLASSCD = T1.SUBCLASSCD AND S1.DIV = 'GRADE' AND S1.KEY = REGD.GRADE ");
//            stb.append(" LEFT JOIN SUBCLASS_AVG S2 ON S2.SUBCLASSCD = T1.SUBCLASSCD AND S2.DIV = 'HR' AND S2.KEY = REGD.GRADE || REGD.HR_CLASS ");
//            stb.append(" LEFT JOIN SUBCLASS_AVG S3 ON S3.SUBCLASSCD = T1.SUBCLASSCD AND S3.DIV = 'COURSE' AND S3.KEY = REGD.GRADE || '000' || REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE ");
//            stb.append(" LEFT JOIN SUBCLASS_AVG S4 ON S4.SUBCLASSCD = T1.SUBCLASSCD AND S4.DIV = 'MAJOR' AND S4.KEY = REGD.GRADE || '000' || REGD.COURSECD || REGD.MAJORCD || '0000' ");
//            stb.append(" ) ");
//        }
        public String toString() {
            return "SubclassScore(" + _subclass + ", " + _score + ", " + _avg + ")";
        }

        public Number compareValue(final String rankDiv, final Param param) {
//            if ("2".equals(param._outputKijun)) {
//                return _avg;
//            }
//            return null == _score ? null : Integer.valueOf(_score);
            
            final String rank = getRank(rankDiv, param);
            if (!NumberUtils.isDigits(rank)) {
                return null;
            }
            return new Integer(rank);
        }
        
        static class DivRank {
            final String _rank;
            final String _avgRank;
            DivRank(final String rank, final String avgRank) {
                _rank = rank;
                _avgRank = avgRank;
            }
            String get(final Param param) {
                final String rank;
                if ("2".equals(param._outputKijun)) {
                    rank = _avgRank;
                } else {
                    rank = _rank;
                }
                return rank;
            }
        }
    }
    
    /**
     * 得点分布
     */
    private static class ScoreDistribution {
        final List _scoreList = new ArrayList(); // 母集団の得点
        void addScore(final String score) {
            if (NumberUtils.isNumber(score)) {
                _scoreList.add(new BigDecimal(score));
            }
        }
        void addScoreList(final List scoreList) {
            for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                final String score = (String) it.next();
                addScore(score);
            }
        }
        int getCount(final BigDecimal lower, final BigDecimal upper) {
            return getList(lower, upper).size(); 
        }
        List getList(final BigDecimal lower, final BigDecimal upper) {
            final List rtn = new ArrayList();
            for (final Iterator it = _scoreList.iterator(); it.hasNext();) {
                final BigDecimal i = (BigDecimal) it.next();
                if (null == i) {
                    continue;
                }
                final BigDecimal iKirisute = i.setScale(0, BigDecimal.ROUND_HALF_UP);
                //log.debug(" i = " + i + ", int = " + i.intValue() + ", lower = " + lower + ", upper = " + upper);
                if (lower.compareTo(iKirisute) <= 0 && iKirisute.compareTo(upper) <= 0) {
                    rtn.add(i);
                }
            }
            return rtn;
        }
        
    }
    
    private static List getFushinKamokuStudentList(final List studentList, final int fushinScore, final int fushinSu) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final List fushinKamokuList = getFushinKamokuList(Collections.singletonList(student), fushinScore);
            if (fushinKamokuList.size() >= fushinSu) {
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static List getFushinKamokuList(final List studentList, final int fushinScore) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._subclassScore.values().iterator(); sit.hasNext();) {
                final SubclassScore subScore = (SubclassScore) sit.next();
                if (subScore._subclass._attendSubclassCourse.contains(student.gradeCourse())) {
                    // 元科目は含まない
                    continue;
                }
                if (NumberUtils.isNumber(subScore._score) && Double.parseDouble(subScore._score) < fushinScore) {
                    rtn.add(subScore);
                }
            }
        }
        return rtn;
    }

    private static List getFushinHeikinStudentList(final List studentList, final int fushinHeikin) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._subclassScore999999 && null != student._subclassScore999999._avg && student._subclassScore999999._avg.doubleValue() < fushinHeikin) {
                rtn.add(student);
            }
        }
        return rtn;
    }
    
    private static List getAttendOverStudentList(final Param param, final List studentList, final int flg) {
        List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._attendance) {
                if (flg == 0 && student._attendance._absence >= param._kesseki) {
                    rtn.add(student);
                } else if (flg == 1 && student._attendance._late >= param._chikoku) {
                    rtn.add(student);
                } else if (flg == 2 && student._attendance._early >= param._soutai) {
                    rtn.add(student);
                }
            }
        }
        return rtn;
    }

    private static List getKessiStudentList(final List studentList) {
        List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            // log.debug(" student = " + student + ", kessi = " + student.getKesshiSubclassList());
            if (student.getKesshiSubclassList().size() != 0) {
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static Map newRecord(final List recordList) {
        final Map record = new HashMap();
        recordList.add(record);
        return record;
    }

    private static void putName(final Map record, final String field, final String name) {
        final int keta = getMS932ByteLength(name);
        record.put(field + (keta <= 14 ? "_1" : keta <= 20 ? "_2" : "_3"), name);
    }

    private static class StudentRankComparator implements Comparator {
        
        final String _rankDiv;
        final Param _param;

        public StudentRankComparator(final String rankDiv, final Param param) {
            _rankDiv = rankDiv;
            _param = param;
        }
        
        public int compare(final Object o1, final Object o2) {
            final Student std1 = (Student) o1;
            final Student std2 = (Student) o2;
            if (null == std1._subclassScore999999 && null == std2._subclassScore999999) {
                return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
            } else if (null == std1._subclassScore999999) {
                return 1;
            } else if (null == std2._subclassScore999999) {
                return -1;
            } else if (null == std1._subclassScore999999.compareValue(_rankDiv, _param) && null == std2._subclassScore999999.compareValue(_rankDiv, _param)) {
                return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
            } else if (null == std1._subclassScore999999.compareValue(_rankDiv, _param)) {
                return 1;
            } else if (null == std2._subclassScore999999.compareValue(_rankDiv, _param)) {
                return -1;
            }
            final Double v1 = new Double(std1._subclassScore999999.compareValue(_rankDiv, _param).doubleValue());
            final Double v2 = new Double(std2._subclassScore999999.compareValue(_rankDiv, _param).doubleValue());
            final int rtn = v1.compareTo(v2);
            if (rtn != 0) { // 昇順
                return rtn;
            }
            return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
        }
    }
    
    private static class TestItem {
        final String _year;
        final String _semester;
        final String _semestername;
        final String _testkindcd;
        final String _testitemcd;
        final String _testitemname;
        public TestItem(final String year, final String semester, final String semestername,
                final String testkindcd, final String testitemcd, final String testitemname
                ) {
            _year = year;
            _semester = semester;
            _semestername = semestername;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _testitemname = testitemname;
        }
        public String getSemeTestcd() {
            return _semester + "-" +_testkindcd + "-" +_testitemcd;
        }
        public String toString() {
            return "TestItem(" + _semester + _testkindcd + _testitemcd + ")";
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
        final String _ctrlDate;
        final String _grade;
        final String _testcd;
        final String _sdate;
        final String _edate;
        final int _kesseki;
        final int _chikoku;
        final int _soutai;
        final int _fushinSu;
        final int _fushinScore;
        final int _fushinHeikin;
//        final String _kariHyotei; // 990009:学年評定の場合
        final String _outputKijun; // 1:総計 2:平均点
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        private String _sidouHyoji;
        final TestItem _testItem;
        private String _yearSdate;
//        final String _cmd;
        final String _gradename1;
        final String _schoolKind;
        
        private KNJSchoolMst _knjSchoolMst;
        final String SSEMESTER = "1";
        private KNJDefineCode _definecode0;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        final Map _subclassMap;
        final Map _assessMap;
        
        private Map _courseWeightingSubclassCdListMap = Collections.EMPTY_MAP;

//        private Map _averageDatMap = Collections.EMPTY_MAP;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            
            _testcd = request.getParameter("TEST_CD");
            _edate = request.getParameter("EDATE").replace('/', '-');
            _kesseki = toInt(request.getParameter("KESSEKI"), 0);
            _chikoku = toInt(request.getParameter("CHIKOKU"), 0);
            _soutai = toInt(request.getParameter("SOUTAI"), 0);
            _sdate = request.getParameter("SDATE").replace('/', '-');
//            _kariHyotei = request.getParameter("KARI_HYOTEI");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _yearSdate = getYearSdate(db2);
//            _cmd = request.getParameter("cmd");
//            _d056 = request.getParameter("D056");
            _subclassMap = getSubclassMap(db2);
            _assessMap = getAssessMst(db2);
            _fushinSu = toInt(request.getParameter("FUSHIN_SU"), 1);
            _fushinScore = toInt(request.getParameter("FUSHIN_SCORE"), 1);
            _fushinHeikin = toInt(request.getParameter("FUSHIN_HEIKIN"), 30);
//            _dateDiv = request.getParameter("DATE_DIV");
//            _date = request.getParameter("DATE").replace('/', '-');
//            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }
            _testItem = getTestKindItem(db2);
            _gradename1 = getRegdGdat(db2, "GRADE_NAME1");
            _schoolKind = getRegdGdat(db2, "SCHOOL_KIND");
            loadAttendAccumulateParameter(db2);
        }
        
        public String regdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }
        
        private static int toInt(final String s, final int defaultInt) {
            return NumberUtils.isNumber(s) ? Integer.parseInt(s) : defaultInt;
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private void loadAttendAccumulateParameter(final DB2UDB db2) {
            try {
                _definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sdate, _edate);
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        private String getYearSdate(DB2UDB db2) {
            String yearSdate = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SDATE ");
                stb.append(" FROM SEMESTER_MST T1 ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    yearSdate = rs.getString("SDATE");
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == yearSdate) {
                yearSdate = _year + "-04-01";
            }
            return yearSdate;
        }

        private String getRegdGdat(final DB2UDB db2, final String field) {
            String gradename1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     " + field + " ");
                stb.append(" FROM SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.GRADE = '" + _grade + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    gradename1 = rs.getString(field);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" " + field + " = " + gradename1);
            return gradename1;
        }

        private TestItem getTestKindItem(final DB2UDB db2) {
            TestItem testItem = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.SEMESTERNAME, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.TESTITEMNAME ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW T1 ");
                stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _testcd + "' ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    testItem = new TestItem(
                            year, semester, semestername, testkindcd, testitemcd, testitemname);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testItem = " + testItem);
            return testItem;
        }
        
        private Map getSubclassMap(DB2UDB db2) {
            Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                
                final String flg = _testcd.startsWith("99") ? "2" : "1";
                
                stb.append(" WITH SUBCLASS_WEIGHTING_COURSE_ATTEND AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                stb.append("     T1.ATTEND_CLASSCD, T1.ATTEND_SCHOOL_KIND, T1.ATTEND_CURRICULUM_CD, T1.ATTEND_SUBCLASSCD ");
                stb.append(" FROM SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append("   AND FLG = '" + flg + "' ");
                stb.append("   AND GRADE = '" + _grade + "' ");
                stb.append(" ), SUBCLASS_WEIGHTING_COURSE_COMBINED AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                stb.append("     T1.COMBINED_CLASSCD, T1.COMBINED_SCHOOL_KIND, T1.COMBINED_CURRICULUM_CD, T1.COMBINED_SUBCLASSCD ");
                stb.append(" FROM SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append("   AND FLG = '" + flg + "' ");
                stb.append("   AND GRADE = '" + _grade + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     T3.COURSE, ");
                stb.append("     'ATTEND' AS DIV ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" LEFT JOIN SUBCLASS_WEIGHTING_COURSE_ATTEND T3 ON T3.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     T3.COURSE, ");
                stb.append("     'COMBINED' AS DIV ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" LEFT JOIN SUBCLASS_WEIGHTING_COURSE_COMBINED T3 ON T3.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
                log.debug(" subclass sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (!map.containsKey(subclasscd)) {
                        final String classabbv = rs.getString("CLASSABBV");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        map.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv));
                    }
                    if (null != rs.getString("COURSE")) {
                        if ("ATTEND".equals(rs.getString("DIV"))) {
                            final Subclass subclass = (Subclass) map.get(subclasscd);
                            subclass._attendSubclassCourse.add(rs.getString("COURSE"));
                        }
                        if ("COMBINED".equals(rs.getString("DIV"))) {
                            final Subclass subclass = (Subclass) map.get(subclasscd);
                            subclass._combinedSubclassCourse.add(rs.getString("COURSE"));
                        }
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        
        
        private Map getAssessMst(final DB2UDB db2) {
            Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.ASSESSLEVEL, ");
                stb.append("     T1.ASSESSLOW, ");
                stb.append("     T1.ASSESSHIGH, ");
                stb.append("     T2.ASSESSMARK ");
                stb.append(" FROM ASSESS_MST T1 ");
                stb.append(" LEFT JOIN ASSESS_MST T2 ON T2.ASSESSCD = '4' ");
                stb.append("     AND T1.ASSESSLEVEL BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ");
                stb.append(" WHERE T1.ASSESSCD = '3' ");

                log.debug(" subclass sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Integer assesslevel = (Integer) rs.getObject("ASSESSLEVEL");
                    
                    final Map record = new HashMap();
                    record.put("ASSESSLOW", rs.getBigDecimal("ASSESSLOW"));
                    record.put("ASSESSHIGH", rs.getBigDecimal("ASSESSHIGH"));
                    record.put("ASSESSMARK", rs.getString("ASSESSMARK"));
                    map.put(assesslevel, record);
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
}

// eof


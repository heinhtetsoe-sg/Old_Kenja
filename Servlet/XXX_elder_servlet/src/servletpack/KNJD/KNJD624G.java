// kanji=漢字
/*
 * $Id: f20c93656946211f48841ca0f4ca0d2f8ea11e4c $
 *
 * 作成日: 2012/03/08 13:09:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f20c93656946211f48841ca0f4ca0d2f8ea11e4c $
 */
public class KNJD624G {

    private static final Log log = LogFactory.getLog("KNJD624G.class");

    private boolean _hasData;

    private static final String AVG_GRADE = "1";
    private static final String AVG_HR = "2";
    private static final String AVG_COURSE = "3";
    private static final int MAX_RETU = 8;
    private static final int MAX_RETU2 = 6;
    private static final int GOKEI_IDX = 9;
    private static final Integer hyoteiNullCount = new Integer(-1);

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            _param._courseWeightingSubclassCdListMap = getCourseWeightingSubclassCdListMap(db2);
            for (final String subclassCd : _param._subclassMap.keySet()) {
                final Subclaas subclaas = _param._subclassMap.get(subclassCd);
                printHrclass(db2, svf, subclaas);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printHrclass(final DB2UDB db2, final Vrw32alp svf, final Subclaas subclaas) throws SQLException {

        final List<HrClassData> printHrList = HrClassData.getPrintHrList(db2, _param, subclaas);
        if ("2".equals(_param._groupDiv)) {
            // コースごとに改ページ
            final List<Student> studentList = new ArrayList();
            for (final HrClassData hrClassData : printHrList) {
                studentList.addAll(hrClassData._students);
            }

            boolean isPrint1 = false;
            final List<String> gradeCourseList = getGradeCourseList(studentList);
            for (final String gradeCourse : gradeCourseList) {
                if (!notTargetSubclasscdList(gradeCourse).contains(subclaas._subclassCd)) {
                    isPrint1 = true;
                    break;
                }
            }
            if (!isPrint1) {
                // すべてのコースが科目を表示しない
                log.debug("すべてのコースが科目を表示しない:" + subclaas._subclassCd + ":" + subclaas._subclassName);
                return;
            }

            for (final String gradeCourse : gradeCourseList) {
                final List<HrClassData> courseHrPrintList = new ArrayList();

                for (final HrClassData hrClassData : printHrList) {
                    final List<String> hrGradeCourse = getGradeCourseList(hrClassData._students);
                    if (hrGradeCourse.contains(gradeCourse)) {
                        courseHrPrintList.add(hrClassData);
                    }
                }
                printMain(db2, svf, subclaas, courseHrPrintList, gradeCourse);
            }
        } else {
            // 指定学年
            for (final Iterator<HrClassData> itHr = printHrList.iterator(); itHr.hasNext();) {
                final HrClassData hrClassData = itHr.next();

                boolean isPrint1 = false;
                final List<String> gradeCourseList = getGradeCourseList(hrClassData._students);
                for (final String gradeCourse : gradeCourseList) {
                    if (!notTargetSubclasscdList(gradeCourse).contains(subclaas._subclassCd)) {
                        isPrint1 = true;
                        break;
                    }
                }
                if (!isPrint1) {
                    // すべてのコースが科目を表示しない
                    itHr.remove();
                    continue;
                }
            }
            if (printHrList.size() == 0) {
                // すべてのHRが科目を表示しない
                log.debug("すべてのHRが科目を表示しない:" + subclaas._subclassCd + ":" + subclaas._subclassName);
                return;
            }
            printMain(db2, svf, subclaas, printHrList, null);
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Subclaas subclaas, final List<HrClassData> printHrList, final String gradeCourse) {

        final Map<Integer, Integer> bunpu = new LinkedMap();
        if (_param.isGakunenHyotei()) {
            bunpu.put(5, 5);
            bunpu.put(4, 4);
            bunpu.put(3, 3);
            bunpu.put(2, 2);
            bunpu.put(1, 1);
            bunpu.put(hyoteiNullCount, hyoteiNullCount); // 評定nullをカウント
        } else {
            bunpu.put(100, 100);
            bunpu.put(95, 99);
            bunpu.put(90, 94);
            bunpu.put(85, 89);
            bunpu.put(80, 84);
            bunpu.put(75, 79);
            bunpu.put(70, 74);
            bunpu.put(65, 69);
            bunpu.put(60, 64);
            bunpu.put(55, 59);
            bunpu.put(50, 54);
            bunpu.put(45, 49);
            bunpu.put(40, 44);
            bunpu.put(35, 39);
            bunpu.put(30, 34);
            bunpu.put(25, 29);
            bunpu.put(20, 24);
            bunpu.put(15, 19);
            bunpu.put(10, 14);
            bunpu.put(5, 9);
            bunpu.put(0, 4);
        }

        final List<List<HrClassData>> pageList = new ArrayList();
        List current = null;
        for (final HrClassData o : printHrList) {
            if (null == current || current.size() >= MAX_RETU) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }

        final String FORM_FILE = "KNJD624G_1.frm";
        final String FORM_FILE2 = _param.isGakunenHyotei() ? "KNJD624G_3.frm" : "KNJD624G_2.frm";

        boolean putForm2 = false;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List<HrClassData> hrList = pageList.get(pi);
            if (pi == pageList.size() - 1 && hrList.size() <= MAX_RETU2) {
                svf.VrSetForm(FORM_FILE2, 1);
                printHeader(svf, subclaas, gradeCourse);
                for (int hri = 0; hri < printHrList.size(); hri++) {
                    final HrClassData hrClassData = printHrList.get(hri);
                    //基本部分印字
                    printKihonData(svf, hri + 1, subclaas, hrClassData, gradeCourse);
                    printHrBunpu(svf, bunpu, hri + 1, subclaas, hrClassData, gradeCourse);
                }
                printHrBunpuGoukei(svf, printHrList, bunpu, gradeCourse);

                putForm2 = true;
            } else {
                svf.VrSetForm(FORM_FILE, 1);
            }
            printHeader(svf, subclaas, gradeCourse);
            for (int hri = 0; hri < hrList.size(); hri++) {
                final HrClassData hrClassData = hrList.get(hri);
                //基本部分印字
                printKihonData(svf, hri + 1, subclaas, hrClassData, gradeCourse);
                final int retuCnt = hri + 1;


                //生徒データ印字
                List<String> scores = new ArrayList<String>();
                for (int stLine = 1; stLine <= hrClassData._students.size(); stLine++) {
                	final Student student = hrClassData._students.get(stLine - 1);
                    svf.VrsOutn("ATTENDNO" + stLine, retuCnt, student._attendNo);
                    svf.VrsOutn("NAME" + stLine, retuCnt, student._name);
                    boolean isPrint = true;
                    if (isPrint && "2".equals(_param._groupDiv)) {
                        if (!student.getGradeCourse().equals(gradeCourse)) {
                            isPrint = false;
                        }
                    }
                    if (isPrint && notTargetSubclasscdList(student.getGradeCourse()).contains(subclaas._subclassCd)) {
                    	isPrint = false;
                    }
                    if (isPrint) {
                        svf.VrsOutn("SCORE" + stLine, retuCnt, student._score);
                        if (NumberUtils.isDigits(student._score)) {
                        	scores.add(student._score);
                        }
                    }
                }

                final AvgDat hrAvgDat = _param._avgDatMap.get(subclaas._subclassCd + "HR_CLASS" + hrClassData._hrClass);
                if (0 < scores.size() && null != hrAvgDat) {
                    svf.VrsOutn("TOTAL_POINT1", retuCnt, hrAvgDat._score);
                    svf.VrsOutn("TOTAL_NUM1", retuCnt, hrAvgDat._cnt);

                    if (null != hrAvgDat._avg) {
                        svf.VrsOutn("TOTAL_AVERAGE1", retuCnt, sishagonyu(hrAvgDat._avg, 1));
                    }
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
        if (!putForm2) {
            svf.VrSetForm(FORM_FILE2, 1);
            printHeader(svf, subclaas, gradeCourse);
            for (int hri = 0; hri < printHrList.size(); hri++) {
                final HrClassData hrClassData = printHrList.get(hri);
                //基本部分印字
                printKihonData(svf, hri + 1, subclaas, hrClassData, gradeCourse);
                printHrBunpu(svf, bunpu, hri + 1, subclaas, hrClassData, gradeCourse);
            }
            printHrBunpuGoukei(svf, printHrList, bunpu, gradeCourse);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List<String> notTargetSubclasscdList(final String gradeCourse) {
        final List<String> notTargetSubclassCdList;
        if ("9900".equals(_param._testCd) || "9901".equals(_param._testCd)) {
            notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        } else {
            // [学期末、学年末]以外は先を表示しない
            notTargetSubclassCdList = getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "COMBINED_SUBCLASS");
        }
        return notTargetSubclassCdList;
    }

    private Map<String, Map<String, List<String>>> getCourseWeightingSubclassCdListMap(final DB2UDB db2) {
        final Map<String, Map<String, List<String>>> courseWeightingSubclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String flg = "9900".equals(_param._testCd) ? "2" : "1";
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

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return map.get(key1);
    }

//    private void printDanjoBunpu(final Vrw32alp svf, final List printHrList, final Map bunpu) {
//        Map dansiBunpu = new LinkedMap();
//        Map jyosiBunpu = new LinkedMap();
//        Map totalBunpu = new LinkedMap();
//        int dansiTotalScore = 0;
//        int dansiTotalCnt = 0;
//        int jyosiTotalScore = 0;
//        int jyosiTotalCnt = 0;
//        for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
//            final HrClassData hrClassData = (HrClassData) itHr.next();
//            int hrBunpuLine = 1;
//            for (final Iterator itBunpu = bunpu.keySet().iterator(); itBunpu.hasNext();) {
//                final Integer lowScore = (Integer) itBunpu.next();
//                final String highScore = (String) bunpu.get(lowScore);
//                final Map bunpuCnt = hrClassData.getBunpuDanjoCnt(lowScore.toString(), highScore);
//                final String dansiCnt = (String) bunpuCnt.get("DANSI");
//                final String jyosiCnt = (String) bunpuCnt.get("JYOSI");
//                final String totalCnt = (String) bunpuCnt.get("TOTAL");
//                final String dansiScore = (String) bunpuCnt.get("DANSISCORE");
//                final String jyosiScore = (String) bunpuCnt.get("JYOSISCORE");
//                dansiTotalScore += Integer.parseInt(dansiScore);
//                jyosiTotalScore += Integer.parseInt(jyosiScore);
//                dansiTotalCnt += Integer.parseInt(dansiCnt);
//                jyosiTotalCnt += Integer.parseInt(jyosiCnt);
//                int setDansiCnt = 0;
//                int setJyosiCnt = 0;
//                int setTotalCnt = 0;
//                if (dansiBunpu.containsKey(String.valueOf(hrBunpuLine))) {
//                    setDansiCnt = Integer.parseInt((String) dansiBunpu.get(String.valueOf(hrBunpuLine)));
//                    setJyosiCnt = Integer.parseInt((String) jyosiBunpu.get(String.valueOf(hrBunpuLine)));
//                    setTotalCnt = Integer.parseInt((String) totalBunpu.get(String.valueOf(hrBunpuLine)));
//                }
//                setDansiCnt += Integer.parseInt(dansiCnt);
//                setJyosiCnt += Integer.parseInt(jyosiCnt);
//                setTotalCnt += Integer.parseInt(totalCnt);
//                dansiBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setDansiCnt));
//                jyosiBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setJyosiCnt));
//                totalBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setTotalCnt));
//                hrBunpuLine++;
//            }
//        }
//        for (final Iterator itBunpu = dansiBunpu.keySet().iterator(); itBunpu.hasNext();) {
//            final String key = (String) itBunpu.next();
//            final String cnt = (String) dansiBunpu.get(key);
//            svf.VrsOutn("FIELD" + key, 1, cnt);
//        }
//        for (final Iterator itBunpu = jyosiBunpu.keySet().iterator(); itBunpu.hasNext();) {
//            final String key = (String) itBunpu.next();
//            final String cnt = (String) jyosiBunpu.get(key);
//            svf.VrsOutn("FIELD" + key, 2, cnt);
//        }
//        for (final Iterator itBunpu = totalBunpu.keySet().iterator(); itBunpu.hasNext();) {
//            final String key = (String) itBunpu.next();
//            final String cnt = (String) totalBunpu.get(key);
//            svf.VrsOutn("FIELD" + key, 3, cnt);
//        }
//        svf.VrsOutn("TOTAL_POINT3", 1, String.valueOf(dansiTotalScore));
//        svf.VrsOutn("TOTAL_POINT3", 2, String.valueOf(jyosiTotalScore));
//        svf.VrsOutn("TOTAL_NUM3", 1, String.valueOf(dansiTotalCnt));
//        svf.VrsOutn("TOTAL_NUM3", 2, String.valueOf(jyosiTotalCnt));
//
//        if (dansiTotalCnt > 0) {
//            svf.VrsOutn("TOTAL_AVERAGE3", 1, avgSishagonyu(dansiTotalScore, dansiTotalCnt, 1));
//        }
//
//        if (jyosiTotalCnt > 0) {
//            svf.VrsOutn("TOTAL_AVERAGE3", 2, avgSishagonyu(jyosiTotalScore, jyosiTotalCnt, 1));
//        }
//    }

    private static String avgSishagonyu(final int total, final int count, final int scale) {
        return new BigDecimal(total).divide(new BigDecimal(count), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishagonyu(final String num, final int scale) {
        if (!NumberUtils.isNumber(num)) {
            return null;
        }
        return new BigDecimal(num).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printKihonData(final Vrw32alp svf, int retuCnt, final Subclaas subclaas, final HrClassData hrClassData, final String gradeCourse) {
        svf.VrsOutn("CLASS1", retuCnt, hrClassData._hrClassName);
        svf.VrsOutn("CLASS2", retuCnt, hrClassData._hrClassName);
        //分布の合計欄
        svf.VrsOutn("CLASS2", GOKEI_IDX, "全体");
        final String avgTitle;
        final AvgDat avgDat;
        if ("2".equals(_param._groupDiv)) {
            avgDat = _param._avgDatMap.get(subclaas._subclassCd + "COURSE" + gradeCourse);
            avgTitle = "コース平均：";
        } else {
            avgDat = _param._avgDatMap.get(subclaas._subclassCd + "GRADE" + hrClassData._grade);
            avgTitle = "学年平均：";
        }
        svf.VrsOut("GRADE_AVERAGE", avgTitle);
        List<Student> students = hrClassData._students;
        if ("2".equals(_param._groupDiv)) {
            students = Student.filterGradeCourse(hrClassData._students, gradeCourse);
        }
        final List<String> scores = new ArrayList<String>();
        for (final Student student : students) {
            if (NumberUtils.isDigits(student._score)) {
                scores.add(student._score);
            }
        }
        if (0 < scores.size() && null != avgDat) {
            //分布の合計欄
            svf.VrsOutn("TOTAL_POINT2", GOKEI_IDX, avgDat._scoreKansan);
            svf.VrsOutn("TOTAL_POINT3", 3, avgDat._scoreKansan);
            svf.VrsOut("GRADE_AVERAGE", avgTitle + StringUtils.defaultString(sishagonyu(avgDat._avg, 1)));

            svf.VrsOutn("TOTAL_NUM2", GOKEI_IDX, avgDat._cnt);
            svf.VrsOutn("TOTAL_NUM3", 3, avgDat._cnt);
            if (null != avgDat._avgKansan) {
                svf.VrsOutn("TOTAL_AVERAGE3", 3, sishagonyu(avgDat._avgKansan, 1));
                svf.VrsOutn("TOTAL_AVERAGE2", GOKEI_IDX, sishagonyu(avgDat._avgKansan, 1));
            }
            if ("1".equals(_param._highLowStd)) {
                svf.VrsOutn("HIGHSCORE", GOKEI_IDX, avgDat._highscore);
                svf.VrsOutn("LOWSCORE", GOKEI_IDX, avgDat._lowscore);
                svf.VrsOutn("STDDEV", GOKEI_IDX, sishagonyu(avgDat._stddev, 1));
            }
        }
    }

    private void printHrBunpuGoukei(final Vrw32alp svf, final List<HrClassData> printHrList, final Map<Integer, Integer> bunpu, final String gradeCourse) {
        Map<Integer, Integer> hrBunpu = new LinkedMap();
        for (final HrClassData hrClassData : printHrList) {
            List<Student> students = hrClassData._students;
            if ("2".equals(_param._groupDiv)) {
                students = Student.filterGradeCourse(hrClassData._students, gradeCourse);
            }
            int hrBunpuLine = 1;
            for (final Integer lowScore : bunpu.keySet()) {
                final Integer highScore = bunpu.get(lowScore);
                int setCnt = 0;
                if (hrBunpu.containsKey(hrBunpuLine)) {
                    setCnt = hrBunpu.get(hrBunpuLine);
                }
                setCnt += HrClassData.getBunpuCnt(students, lowScore, highScore);
                hrBunpu.put(hrBunpuLine, setCnt);
                hrBunpuLine++;
            }
        }
        for (final Integer hrBunpuLine : hrBunpu.keySet()) {
            final Integer cnt = hrBunpu.get(hrBunpuLine);
            svf.VrsOutn("CLASS_NUM" + hrBunpuLine, GOKEI_IDX, String.valueOf(cnt));
        }
    }

    private void printHrBunpu(final Vrw32alp svf, final Map<Integer, Integer> bunpu, int retuCnt, final Subclaas subclaas, final HrClassData hrClassData, final String gradeCourse) {
        int hrBunpuLine = 1;
        List<Student> students = hrClassData._students;
        if ("2".equals(_param._groupDiv)) {
            students = Student.filterGradeCourse(hrClassData._students, gradeCourse);
        }
        int totalCount = 0;
        for (final Integer lowScore : bunpu.keySet()) {
            final Integer highScore = bunpu.get(lowScore);
            final int count = HrClassData.getBunpuCnt(students, lowScore, highScore);
            totalCount += count;
            svf.VrsOutn("CLASS_NUM" + hrBunpuLine, retuCnt, String.valueOf(count));
            hrBunpuLine++;
        }

        final AvgDat hrAvgDat = _param._avgDatMap.get(subclaas._subclassCd + "HR_CLASS" + hrClassData._hrClass);
        if (0 < totalCount && null != hrAvgDat) {
            svf.VrsOutn("TOTAL_NUM2", retuCnt, hrAvgDat._cnt);
            if ("1".equals(_param._highLowStd)) {
                svf.VrsOut("HIGHSCORE_NAME", "最高点");
                svf.VrsOutn("HIGHSCORE", retuCnt, hrAvgDat._highscore);
                svf.VrsOut("LOWSCORE_NAME", "最低点");
                svf.VrsOutn("LOWSCORE", retuCnt, hrAvgDat._lowscore);
                svf.VrsOut("STDDEV_NAME", "標準偏差");
                svf.VrsOutn("STDDEV", retuCnt, sishagonyu(hrAvgDat._stddev, 1));
            }
            if (null != hrAvgDat._avg) {
                svf.VrsOutn("TOTAL_AVERAGE2", retuCnt, sishagonyu(hrAvgDat._avgKansan, 1));
            }
        }
    }

    private void printHeader(final Vrw32alp svf, final Subclaas subclaas, final String gradeCourse) {
        /* テスト名称 */
        final String testName = _param._semesterName + "  " + _param._testName + "分布表";
        svf.VrsOut("TITLE", _param._nendo + testName);

        svf.VrsOut("GRADE", _param._gradeName + "　" + StringUtils.defaultString(_param._courseNameMap.get(gradeCourse)));
        /* 科目名称 */
        svf.VrsOut("SUBJECT", subclaas._subclassName);
        /* 満点 */
        final String setPerfect = subclaas._highPerfect.equals(subclaas._lowPerfect) ? subclaas._highPerfect : subclaas._lowPerfect + "\uFF5E" + subclaas._highPerfect;
        svf.VrsOut("PERFECT", setPerfect);
        svf.VrsOut("TODAY", _param._ctrlDateStr);
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private List<String> getGradeCourseList(final List<Student> students) {
        final List<String> gradeCourseList = new ArrayList();
        for (final Student student : students) {
            if (!gradeCourseList.contains(student.getGradeCourse())) {
                gradeCourseList.add(student.getGradeCourse());
            }
        }
        return gradeCourseList;
    }

    private static class HrClassData {
        final String _grade;
        final String _hrClass;
        final Subclaas _subclaas;
        final String _hrName;
        final String _hrClassName;
        List<Student> _students;

        public HrClassData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName,
                final Subclaas subclaas
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName = hrClassName;
            _subclaas = subclaas;
        }

        public void load(final DB2UDB db2, final Param param) throws SQLException {
            _students = getStudents(db2,param,  _grade, _hrClass, _subclaas);
        }

        public static int getBunpuCnt(final List<Student> students, final Integer intLow, final Integer intHigh) {
            int retCnt = 0;
            for (final Student student : students) {
                if (null != student._score) {
                    final int score = Integer.parseInt(student._score);
                    if (intLow <= score && score <= intHigh) {
                        retCnt++;
                    }
                } else { // null == student._score
                    if (intLow == hyoteiNullCount && intHigh == hyoteiNullCount) {
                        if (null != student._recordFlg) {
                            retCnt++;
                        }
                    }
                }
            }
            return retCnt;
        }

//        public Map getBunpuDanjoCnt(final String lowScore, final String highScore) {
//            Map retCnt = new HashMap();
//            int dansi = 0;
//            int jyosi = 0;
//            int total = 0;
//            int dansiScore = 0;
//            int jyosiScore = 0;
//            final int intLow = Integer.parseInt(lowScore);
//            final int intHigh = Integer.parseInt(highScore);
//            for (final Iterator iter = _students.iterator(); iter.hasNext();) {
//                final Student student = (Student) iter.next();
//                if (null != student._score) {
//                    final int score = Integer.parseInt(student._score);
//                    if (intLow <= score && score <= intHigh) {
//                        if ("1".equals(student._sex)) {
//                            dansi++;
//                            dansiScore += score;
//                        } else if ("2".equals(student._sex)) {
//                            jyosi++;
//                            jyosiScore += score;
//                        }
//                        total++;
//                    }
//                }
//            }
//            retCnt.put("DANSI", String.valueOf(dansi));
//            retCnt.put("JYOSI", String.valueOf(jyosi));
//            retCnt.put("TOTAL", String.valueOf(total));
//            retCnt.put("DANSISCORE", String.valueOf(dansiScore));
//            retCnt.put("JYOSISCORE", String.valueOf(jyosiScore));
//            return retCnt;
//        }

        private List<Student> getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String hrClass,
                final Subclaas subclaas
        ) throws SQLException {
            final List<Student> retStudent = new ArrayList();
            final String studentSql = getStudentsSql(param, grade, hrClass, subclaas);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String recordFlg = rs.getString("RECORD_FLG");
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final Student student = new Student(schregNo, attendNo, name, sex, recordFlg, score, scoreKansan, grade, courseCd, majorCd, courseCode);
                    retStudent.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStudent;
        }

        private String getStudentsSql(final Param param, final String grade, final String hrClass, final Subclaas subclaas) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS RECORD_FLG, ");
            stb.append("     L1.SCORE, ");
            stb.append("     L1.SCORE AS SCORE_KANSAN, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD || L1.TESTITEMCD = '" + param._testCd + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("              L1.SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        private static List<HrClassData> getPrintHrList(final DB2UDB db2, final Param param, final Subclaas subclaas) throws SQLException {
            final List<HrClassData> retHr = new ArrayList();
            final String HrSql = getHrSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(HrSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName = rs.getString("HR_CLASS_NAME1");
                    final HrClassData hrClassData = new HrClassData(grade, hrClass, hrName, hrClassName, subclaas);
                    retHr.add(hrClassData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final HrClassData hrClassData : retHr) {
                hrClassData.load(db2, param);
            }
            return retHr;
        }

        private static String getHrSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME, ");
            stb.append("     HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS ");

            return stb.toString();
        }
    }

    private static class AvgDat {
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _stddev;
        final String _scoreKansan;
        final String _cnt;
        final String _avg;
        final String _avgKansan;

        public AvgDat(
                final String score,
                final String highscore,
                final String lowscore,
                final String stddev,
                final String scoreKansan,
                final String cnt,
                final String avg,
                final String avgKansan
        ) {
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _stddev = stddev;
            _scoreKansan = scoreKansan;
            _cnt = cnt;
            _avg = avg;
            _avgKansan = avgKansan;
        }

        private static Map<String, AvgDat> getAvgDat(
                final DB2UDB db2,
                final Param param,
                final String grade
        ) throws SQLException {
            final Map<String, AvgDat> retAvgMap = new HashMap();
            final String avgSql = getAvgSql(param, grade);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(avgSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String highscore = rs.getString("HIGHSCORE");
                    final String lowscore = rs.getString("LOWSCORE");
                    final String stddev = rs.getString("STDDEV");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String avgKansan = rs.getString("AVG_KANSAN");
                    final AvgDat avgDat = new AvgDat(score, highscore, lowscore, stddev, scoreKansan, cnt, avg, avgKansan);
                    final String div = rs.getString("DIV");
                    final String key = rs.getString("KEY");
                    retAvgMap.put(subclasscd + div + key, avgDat);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'GRADE' AS DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     STDDEV, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     GRADE AS KEY, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_GRADE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     'HR_CLASS' AS DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     STDDEV, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     HR_CLASS AS KEY, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     'COURSE' AS DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     STDDEV, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     GRADE || COURSECD || MAJORCD || COURSECODE AS KEY, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_COURSE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _recordFlg;
        final String _score;
        final String _scoreKansan;
        final String _grade;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;

        public Student (
                final String schregNo,
                final String attendNo,
                final String name,
                final String sex,
                final String recordFlg,
                final String score,
                final String scoreKansan,
                final String grade,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _recordFlg = recordFlg;
            _score = score;
            _scoreKansan = scoreKansan;
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }

        public static List<Student> filterGradeCourse(final List<Student> students, final String gradeCourse) {
             final List<Student> rtn = new ArrayList<Student>();
             if (null != gradeCourse) {
                 for (final Student student : students) {
                     if (gradeCourse.equals(student.getGradeCourse())) {
                         rtn.add(student);
                     }
                 }
             }
             return rtn;
        }

        public String getGradeCourse() {
            return _grade + _courseCd + _majorCd + _courseCode;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id: f20c93656946211f48841ca0f4ca0d2f8ea11e4c $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlSeme;
        private final String _semesterName;
        private final String _ctrlDate;
        private final String _testCd;
        private final String _testName;
        private final String _grade;
        private final String _gradeName;
        private final String _groupDiv; // 1: 学年 2:コース
        private final String _highLowStd;
        private final String _nendo;
        private final String _ctrlDateStr;
        final String[] _categorySelected;
        final String _useCurriculumcd;
        private final Map<String, Subclaas> _subclassMap;
        private Map<String, Map<String, List<String>>> _courseWeightingSubclassCdListMap;
        private Map<String, String> _courseNameMap;
        private Map<String, AvgDat> _avgDatMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _semesterName = getSemesterName(db2, _year, _semester);
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            _testName = getTestName(db2, _year, _semester, _testCd);
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2, _year, _grade);
            _groupDiv = request.getParameter("GROUP_DIV");
            _highLowStd = request.getParameter("HIGH_LOW_STD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassMap = getSubclassMap(db2, _categorySelected, _year, _semester, _testCd, _grade, _useCurriculumcd);
            _courseNameMap = getCourseNameMap(db2);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _ctrlDateStr = KNJ_EditDate.h_format_JP(db2, _ctrlDate);
            _avgDatMap = AvgDat.getAvgDat(db2, this, _grade);
        }

        private boolean isGakunenHyotei() {
            return "9".equals(_semester) && "9900".equals(_testCd);
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'")));
        }


        private Map<String, String> getCourseNameMap(final DB2UDB db2) throws SQLException {
            Map<String, String> rtn = new HashMap();
            final String sql = "SELECT '" + _grade + "' || COURSECD || MAJORCD || COURSECODE AS CODE, COURSECODENAME AS NAME FROM MAJOR_MST, COURSECODE_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("CODE"), rs.getString("NAME"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map<String, Subclaas> getSubclassMap(
                final DB2UDB db2,
                final String[] categorySelected,
                final String year,
                final String semester,
                final String testCd,
                final String grade,
                final String useCurriculumcd
        ) throws SQLException {
            final Map retMap = new LinkedMap();
            for (int i = 0; i < categorySelected.length; i++) {
                final String subclassCd = categorySelected[i];
                final String subclassName = getSubclassName(db2, subclassCd);
                final Subclaas subclaas = new Subclaas(subclassCd, subclassName);
                subclaas.setPerfect(db2, this, year, semester, testCd, subclassCd, grade, useCurriculumcd);
                retMap.put(subclassCd, subclaas);
            }
            return retMap;
        }

        private String getSubclassName(final DB2UDB db2, final String subclassCd) throws SQLException {
            final String subclassSql;
            if ("1".equals(_useCurriculumcd)) {
                subclassSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE " +
                              "  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || "+
                              "  SUBCLASSCD = '" + subclassCd + "'";
            } else {
                subclassSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE " +
                              "  SUBCLASSCD = '" + subclassCd + "'";
            }
            String retSubclassName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, subclassSql)));
            return retSubclassName;
        }


        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testCd) throws SQLException {
            final String testItemSql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + testCd + "'";
            String retTestName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, testItemSql)));
            return retTestName;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) throws SQLException {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'")));
        }
    }

    private static class Subclaas {
        private final String _subclassCd;
        private final String _subclassName;
        private String _highPerfect;
        private String _lowPerfect;

        public Subclaas(
                final String subclassCd,
                final String subclassName
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }

        private void setPerfect(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String testCd,
                final String subclassCd,
                final String grade,
                final String useCurriculumcd
        ) throws SQLException {
            final String perfectSql = getPerfectSql(param, year, semester, testCd, subclassCd, grade, useCurriculumcd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(perfectSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _highPerfect = rs.getString("MAX_PERFECT");
                    _lowPerfect = rs.getString("MIN_PERFECT");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getPerfectSql(
                final Param param,
                final String year,
                final String semester,
                final String testCd,
                final String subclassCd,
                final String grade,
                final String useCurriculumcd
        ) {
            final StringBuffer stb = new StringBuffer();
            final String defaultPerfect = param.isGakunenHyotei() ? "5" : "100";
            stb.append(" SELECT ");
            stb.append("     VALUE(MAX(PERFECT), " + defaultPerfect + ") AS MAX_PERFECT, ");
            stb.append("     VALUE(MIN(PERFECT), " + defaultPerfect + ") AS MIN_PERFECT ");
            stb.append(" FROM ");
            stb.append("     PERFECT_RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + year + "' ");
            stb.append("     AND SEMESTER = '" + semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + testCd + "' ");
            stb.append("     AND ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD = '" + subclassCd + "' ");
            stb.append("     AND GRADE = CASE WHEN DIV = '01' ");
            stb.append("                      THEN '00' ");
            stb.append("                      ELSE '" + grade + "' ");
            stb.append("                 END ");

            return stb.toString();
        }
    }
}

// eof

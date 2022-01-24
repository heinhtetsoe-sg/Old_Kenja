// kanji=漢字
/*
 * $Id: 55f52ead6b64d8fb2177c361d4717352a4c3adad $
 *
 * 作成日: 2012/03/08 13:09:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 55f52ead6b64d8fb2177c361d4717352a4c3adad $
 */
public class KNJD624N {

    private static final Log log = LogFactory.getLog("KNJD624N.class");

    private boolean _hasData;

    private static final String FORM_FILE = "KNJD624N_1.frm";
    private static final String FORM_FILE2 = "KNJD624N_2.frm";
    private static final String AVG_GRADE = "1";
    private static final String AVG_HR = "2";
    private static final int MAX_RETU = 10;
    private static final int MAX_RETU2 = 7;
    private static final int DIST_COLUMN = 9;
    private static final int GOKEI_IDX = 9;

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

            _param._replaceCombinedSubclassCdListMap = getReplaceCombinedSubclassCdListMap(db2);
            for (final Iterator iter = _param._subclassMap.keySet().iterator(); iter.hasNext();) {
                final String subclassCd = (String) iter.next();
                final Subclaas subclaas = (Subclaas) _param._subclassMap.get(subclassCd);
                printMain(db2, svf, subclaas);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Subclaas subclaas) throws SQLException {
        if (notTargetSubclasscdList().contains(subclaas._subclassCd)) {
            log.debug(" 出力対象外科目:" + subclaas._subclassCd + " " + subclaas._subclassName);
            return;
        }
        final List printHrList = HrClassData.getPrintHrList(db2, _param, subclaas);

        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = printHrList.iterator(); it.hasNext();) {
            final Object o = (Object) it.next();
            if (null == current || current.size() >= MAX_RETU) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        
        boolean putForm2 = false;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List hrList = (List) pageList.get(pi);
            if (pi == pageList.size() - 1 && hrList.size() <= MAX_RETU2) {
                svf.VrSetForm(FORM_FILE2, 1);
                printForm2(svf, subclaas, printHrList);

                putForm2 = true;
            } else {
                svf.VrSetForm(FORM_FILE, 1);
            }
            printForm1(svf, subclaas, hrList);
            svf.VrEndPage();
            _hasData = true;
        }
        if (!putForm2) {
            svf.VrSetForm(FORM_FILE2, 1);
            printForm2(svf, subclaas, printHrList);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printForm1(final Vrw32alp svf, final Subclaas subclaas, final List hrList) {
        printHeader(svf, subclaas, hrList);
        for (int hri = 0; hri < hrList.size(); hri++) {
            final HrClassData hr = (HrClassData) hrList.get(hri);
            final int col = hri + 1;
            svf.VrsOutn("CLASS1", col, hr._hrClassName);
            //生徒データ印字
            int stLine = 1;
            for (final Iterator iter = hr._students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                svf.VrsOutn("ATTENDNO" + stLine, col, student._attendNo);
                svf.VrsOutn("NAME" + stLine, col, student._name);
                if (!notTargetSubclasscdList().contains(subclaas._subclassCd)) {
                    svf.VrsOutn("SCORE" + stLine, col, student._score);
                }
                stLine++;
            }
            
            final AvgDat hrAvgDat = (AvgDat) hr._avgDatMap.get("HR_CLASS");
            if (null != hrAvgDat) {
                svf.VrsOutn("TOTAL_POINT1", col, hrAvgDat._score);
                svf.VrsOutn("TOTAL_NUM1", col, hrAvgDat._cnt);
                svf.VrsOutn("TOTAL_AVERAGE1", col, sishagonyu(hrAvgDat._avg, 1));
            }
        }
    }

    private void printForm2(final Vrw32alp svf, final Subclaas subclaas, final List printHrList) {
        printHeader(svf, subclaas, printHrList);
        final String gokeiSfx = printHrList.size() >= DIST_COLUMN ? "_2" : "";
        final String[] titleSfxs = {"", "_2"};

        for (int i = 0; i < titleSfxs.length; i++) {
            if ("1".equals(_param._highLowStd)) {
                final String sfx = titleSfxs[i];
                svf.VrsOut("HIGHSCORE_NAME" + sfx, "最高点");
                svf.VrsOut("LOWSCORE_NAME" + sfx, "最低点");
                svf.VrsOut("STDDEV_NAME" + sfx, "標準偏差");
            }
        }
        for (int hri = 0; hri < printHrList.size(); hri++) {
            final HrClassData hr = (HrClassData) printHrList.get(hri);
            final int col = hri + 1;

            printHrBunpu(svf, col, hr);
        }
        printHrBunpuGoukei(svf, gokeiSfx, printHrList);
    }
    
    private List notTargetSubclasscdList() {
        final List notTargetSubclassCdList;
        if ("9900".equals(_param._testCd)) {
            notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        } else {
            // [学期末、学年末]以外は先を表示しない
            notTargetSubclassCdList = getMappedList(_param._replaceCombinedSubclassCdListMap, "COMBINED_SUBCLASS");
        }
        return notTargetSubclassCdList;
    }
    
    private Map getReplaceCombinedSubclassCdListMap(final DB2UDB db2) {
        final Map replaceCombinedSubclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
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
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(replaceCombinedSubclassCdListMap, "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                getMappedList(replaceCombinedSubclassCdListMap, "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return replaceCombinedSubclassCdListMap;
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

    private static String avgSishagonyu(final int total, final int count, final int scale) {
        return new BigDecimal(total).divide(new BigDecimal(count), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishagonyu(final String num, final int scale) {
        if (!NumberUtils.isNumber(num)) {
            return null;
        }
        return new BigDecimal(num).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printHrBunpuGoukei(final Vrw32alp svf, final String gokeiSfx, final List printHrList) {
        Map hrBunpu = new LinkedMap();
        for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
            final HrClassData hr = (HrClassData) itHr.next();
            int hrBunpuLine = 1;
            for (final Iterator itBunpu = _param._bunpu.keySet().iterator(); itBunpu.hasNext();) {
                final Integer lowScore = (Integer) itBunpu.next();
                final Integer highScore = (Integer) _param._bunpu.get(lowScore);
                final String bunpuCnt = hr.getBunpuCnt(lowScore, highScore);
                int setCnt = 0;
                if (hrBunpu.containsKey(String.valueOf(hrBunpuLine))) {
                    setCnt = Integer.parseInt((String) hrBunpu.get(String.valueOf(hrBunpuLine)));
                }
                setCnt += Integer.parseInt(bunpuCnt);
                hrBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setCnt));
                hrBunpuLine++;
            }
        }
        for (final Iterator itBunpu = hrBunpu.keySet().iterator(); itBunpu.hasNext();) {
            final String key = (String) itBunpu.next();
            final String cnt = (String) hrBunpu.get(key);
            svf.VrsOutn("CLASS_NUM" + key + gokeiSfx, GOKEI_IDX, cnt);
        }
        
        //分布の合計欄
        final HrClassData hr = (HrClassData) printHrList.get(0);
        svf.VrsOutn("CLASS2" + gokeiSfx, GOKEI_IDX, "全体");
        final AvgDat gradeAvgDat = (AvgDat) hr._avgDatMap.get("GRADE");
        if (null != gradeAvgDat) {
            svf.VrsOutn("TOTAL_POINT2" + gokeiSfx, GOKEI_IDX, gradeAvgDat._scoreKansan);
            svf.VrsOutn("TOTAL_NUM2" + gokeiSfx, GOKEI_IDX, gradeAvgDat._cnt);
            svf.VrsOutn("TOTAL_AVERAGE2" + gokeiSfx, GOKEI_IDX, sishagonyu(gradeAvgDat._avgKansan, 1));
        }
    }

    private void printHrBunpu(final Vrw32alp svf, int col, final HrClassData hr) {
        final String sfx;
        if (col > DIST_COLUMN) {
            sfx = "_2";
            col -= DIST_COLUMN;
        } else {
            sfx = "";
        }
        svf.VrsOutn("CLASS2" + sfx, col, hr._hrClassName);
        
        int hrBunpuLine = 1;
        for (final Iterator itBunpu = _param._bunpu.keySet().iterator(); itBunpu.hasNext();) {
            final Integer lowScore = (Integer) itBunpu.next();
            final Integer highScore = (Integer) _param._bunpu.get(lowScore);
            final String bunpuCnt = hr.getBunpuCnt(lowScore, highScore);
            svf.VrsOutn("CLASS_NUM" + hrBunpuLine + sfx, col, bunpuCnt);
            hrBunpuLine++;
        }
        
        final AvgDat hrAvgDat = (AvgDat) hr._avgDatMap.get("HR_CLASS");
        if (null != hrAvgDat) {
            svf.VrsOutn("TOTAL_NUM2" + sfx, col, hrAvgDat._cnt);
            if ("1".equals(_param._highLowStd)) {
                svf.VrsOutn("HIGHSCORE" + sfx, col, hrAvgDat._highscore);
                svf.VrsOutn("LOWSCORE" + sfx, col, hrAvgDat._lowscore);
                svf.VrsOutn("STDDEV" + sfx, col, hrAvgDat._stddev);
            }
            svf.VrsOutn("TOTAL_AVERAGE2" + sfx, col, sishagonyu(hrAvgDat._avgKansan, 1));
        }
    }

    private void printHeader(final Vrw32alp svf, final Subclaas subclaas, final List printHrList) {
        /* 年度 */
        final String nendo = KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度";
        /* テスト名称 */
        final String testName = _param._semesterName + "  " + _param._testName + "分布表";
        svf.VrsOut("TITLE", nendo + testName);

        /* 学年 */
        svf.VrsOut("GRADE", _param._gradeName);
        /* 科目名称 */
        svf.VrsOut("SUBJECT", subclaas._subclassName);
        /* 満点 */
        final String setPerfect = subclaas._highPerfect.equals(subclaas._lowPerfect) ? subclaas._highPerfect : subclaas._lowPerfect + "\uFF5E" + subclaas._highPerfect;
        svf.VrsOut("PERFECT", setPerfect);
        svf.VrsOut("TODAY", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        
        if (printHrList.size() != 0) {
            HrClassData hr = (HrClassData) printHrList.get(0);
            if (null != hr) {
                final AvgDat gradeAvgDat = (AvgDat) hr._avgDatMap.get("GRADE");
                if (null != gradeAvgDat) {
                    svf.VrsOut("GRADE_AVERAGE", sishagonyu(gradeAvgDat._avg, 1));
                }
            }
        }
    }

    private static class HrClassData {
        final String _grade;
        final String _hrClass;
        final Subclaas _subclaas;
        final String _hrName;
        final String _hrClassName;
        List _students;
        Map _avgDatMap;

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
            _avgDatMap = getAvgDat(db2, param, _grade, _hrClass, _subclaas);
            _students = getStudents(db2, param, _grade, _hrClass, _subclaas);
        }
        
        public String getBunpuCnt(final Integer lowScore, final Integer highScore) {
            int retCnt = 0;
            final int intLow = lowScore.intValue();
            final int intHigh = highScore.intValue();
            for (final Iterator iter = _students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                if (null != student._score) {
                    final int score = Integer.parseInt(student._score);
                    if (intLow <= score && score <= intHigh) {
                        retCnt++;
                    }
                }
            }
            return String.valueOf(retCnt);
        }

        private static Map getAvgDat(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String hrClass,
                final Subclaas subclaas
        ) throws SQLException {
            final Map retAvgMap = new HashMap();
            final String avgSql = getAvgSql(param, grade, hrClass, subclaas);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(avgSql);
                rs = ps.executeQuery();
                while (rs.next()) {
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
                    retAvgMap.put(div, avgDat);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final String grade, final String hrClass, final Subclaas subclaas) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'GRADE' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     STDDEV, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
            stb.append("     AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_GRADE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     'HR_CLASS' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     HIGHSCORE, ");
            stb.append("     LOWSCORE, ");
            stb.append("     STDDEV, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
            stb.append("     AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND HR_CLASS = '" + hrClass + "' ");
            return stb.toString();
        }

        private static List getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String hrClass,
                final Subclaas subclaas
        ) throws SQLException {
            final List retStudent = new ArrayList();
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
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final Student student = new Student(schregNo, attendNo, name, sex, score, scoreKansan, grade, courseCd, majorCd, courseCode);
                    retStudent.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStudent;
        }

        private static String getStudentsSql(final Param param, final String grade, final String hrClass, final Subclaas subclaas) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
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
        
        private static List getPrintHrList(final DB2UDB db2, final Param param, final Subclaas subclaas) throws SQLException {
            final List retHr = new ArrayList();
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
                    final HrClassData hr = new HrClassData(grade, hrClass, hrName, hrClassName, subclaas);
                    retHr.add(hr);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = retHr.iterator(); it.hasNext();) {
                final HrClassData hr = (HrClassData) it.next();
                hr.load(db2, param);
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
    }

    private static class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _sex;
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
            _score = score;
            _scoreKansan = scoreKansan;
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }
        
        public String gradeCourse() {
            return _grade + _courseCd + _majorCd + _courseCode;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlSeme;
        private final String _semesterName;
        private final String _ctrlDate;
        private final String _testCd;
        private final String _testName;
        private final String _grade;
        private final String _gradeName;
        private final String _highLowStd;
        final String[] _categorySelected;
        final String _useCurriculumcd;
        private final Map _subclassMap;
        private Map _replaceCombinedSubclassCdListMap;
        private final Map _bunpu;

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
            _highLowStd = request.getParameter("HIGH_LOW_STD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassMap = getSubclassMap(db2, _categorySelected, _year, _semester, _testCd, _grade, _useCurriculumcd);
            
            _bunpu = new LinkedMap();
            _bunpu.put(new Integer(100), new Integer(100));
            _bunpu.put(new Integer(95), new Integer(99));
            _bunpu.put(new Integer(90), new Integer(94));
            _bunpu.put(new Integer(85), new Integer(89));
            _bunpu.put(new Integer(80), new Integer(84));
            _bunpu.put(new Integer(75), new Integer(79));
            _bunpu.put(new Integer(70), new Integer(74));
            _bunpu.put(new Integer(65), new Integer(69));
            _bunpu.put(new Integer(60), new Integer(64));
            _bunpu.put(new Integer(55), new Integer(59));
            _bunpu.put(new Integer(50), new Integer(54));
            _bunpu.put(new Integer(45), new Integer(49));
            _bunpu.put(new Integer(40), new Integer(44));
            _bunpu.put(new Integer(35), new Integer(39));
            _bunpu.put(new Integer(30), new Integer(34));
            _bunpu.put(new Integer(25), new Integer(29));
            _bunpu.put(new Integer(20), new Integer(24));
            _bunpu.put(new Integer(15), new Integer(19));
            _bunpu.put(new Integer(10), new Integer(14));
            _bunpu.put(new Integer(5), new Integer(9));
            _bunpu.put(new Integer(0), new Integer(4));
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            String retSemesterName = "";
            final String subclassSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSemesterName = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemesterName;
        }

        private Map getSubclassMap(
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
                subclaas.setPerfect(db2, year, semester, testCd, subclassCd, grade, useCurriculumcd);
                retMap.put(subclassCd, subclaas);
            }
            return retMap;
        }

        private String getSubclassName(final DB2UDB db2, final String subclassCd) throws SQLException {
            String retSubclassName = "";
            final String subclassSql;
            if ("1".equals(_useCurriculumcd)) {
                subclassSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE " +
                              "  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || "+
                              "  SUBCLASSCD = '" + subclassCd + "'";
            } else {
                subclassSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE " +
                              "  SUBCLASSCD = '" + subclassCd + "'";
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSubclassName = rs.getString("SUBCLASSNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSubclassName;
        }


        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testCd) throws SQLException {
            String retTestName = "";
            final String testItemSql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + testCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(testItemSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retTestName = rs.getString("TESTITEMNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retTestName;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) throws SQLException {
            String retGradeName = "";
            final String gradeSql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(gradeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retGradeName = rs.getString("GRADE_NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retGradeName;
        }
    }

    private class Subclaas {
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
                final String year,
                final String semester,
                final String testCd,
                final String subclassCd,
                final String grade,
                final String useCurriculumcd
        ) throws SQLException {
            final String perfectSql = getPerfectSql(year, semester, testCd, subclassCd, grade, useCurriculumcd);
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
                final String year,
                final String semester,
                final String testCd,
                final String subclassCd,
                final String grade,
                final String useCurriculumcd
        ) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(MAX(PERFECT), 100) AS MAX_PERFECT, ");
            stb.append("     VALUE(MIN(PERFECT), 100) AS MIN_PERFECT ");
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

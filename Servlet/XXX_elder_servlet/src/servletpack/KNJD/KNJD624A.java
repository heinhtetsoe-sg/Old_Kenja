// kanji=漢字
/*
 * $Id: 51facb4aa826d5836c3833fd9c11ee70c09daed4 $
 *
 * 作成日: 2012/03/08 13:09:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * @version $Id: 51facb4aa826d5836c3833fd9c11ee70c09daed4 $
 */
public class KNJD624A {

    private static final Log log = LogFactory.getLog("KNJD624A.class");

    private boolean _hasData;

    private static final String FORM_FILE = "KNJD624A.frm";
    private static final String AVG_GRADE = "1";
    private static final String AVG_HR = "2";
    private static final int MAX_RETU = 6;

    Param _param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Subclaas subclaas) throws SQLException {
        final List printHrList = getPrintHrList(db2, subclaas);

        svf.VrSetForm(FORM_FILE, 1);
        printHeader(svf, subclaas);

        final Map bunpu = new LinkedMap();
        bunpu.put(new Integer(100), "100");
        bunpu.put(new Integer(90), "99");
        bunpu.put(new Integer(80), "89");
        bunpu.put(new Integer(70), "79");
        bunpu.put(new Integer(60), "69");
        bunpu.put(new Integer(50), "59");
        bunpu.put(new Integer(40), "49");
        bunpu.put(new Integer(30), "39");
        bunpu.put(new Integer(20), "29");
        bunpu.put(new Integer(10), "19");
        bunpu.put(new Integer(0), "9");
        int retuCnt = 1;
        for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
            if (retuCnt > MAX_RETU) {
                //クラス分布合計印字
                printHrBunpuGoukei(svf, printHrList, bunpu);
                //男女分布印字
                printDanjoBunpu(svf, printHrList, bunpu);
                svf.VrEndPage();
                retuCnt = 1;
                printHeader(svf, subclaas);
            }
            final HrClassData hrClassData = (HrClassData) itHr.next();
            //基本部分印字
            printKihonData(svf, retuCnt, hrClassData);
            //生徒データ印字
            pritnStudent(svf, retuCnt, hrClassData);
            //クラス分布印字
            printHrBunpu(svf, bunpu, retuCnt, hrClassData);
            retuCnt++;
            _hasData = true;
        }
        if (_hasData) {
            //男女分布印字
            printDanjoBunpu(svf, printHrList, bunpu);
            //クラス分布合計印字
            printHrBunpuGoukei(svf, printHrList, bunpu);
            svf.VrEndPage();
        }
    }

    private void printDanjoBunpu(final Vrw32alp svf, final List printHrList, final Map bunpu) {
        Map dansiBunpu = new LinkedMap();
        Map jyosiBunpu = new LinkedMap();
        Map totalBunpu = new LinkedMap();
        int dansiTotalScore = 0;
        int dansiTotalCnt = 0;
        int jyosiTotalScore = 0;
        int jyosiTotalCnt = 0;
        for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
            final HrClassData hrClassData = (HrClassData) itHr.next();
            int hrBunpuLine = 1;
            for (final Iterator itBunpu = bunpu.keySet().iterator(); itBunpu.hasNext();) {
                final Integer lowScore = (Integer) itBunpu.next();
                final String highScore = (String) bunpu.get(lowScore);
                final Map bunpuCnt = hrClassData.getBunpuDanjoCnt(lowScore.toString(), highScore);
                final String dansiCnt = (String) bunpuCnt.get("DANSI");
                final String jyosiCnt = (String) bunpuCnt.get("JYOSI");
                final String totalCnt = (String) bunpuCnt.get("TOTAL");
                final String dansiScore = (String) bunpuCnt.get("DANSISCORE");
                final String jyosiScore = (String) bunpuCnt.get("JYOSISCORE");
                dansiTotalScore += Integer.parseInt(dansiScore);
                jyosiTotalScore += Integer.parseInt(jyosiScore);
                dansiTotalCnt += Integer.parseInt(dansiCnt);
                jyosiTotalCnt += Integer.parseInt(jyosiCnt);
                int setDansiCnt = 0;
                int setJyosiCnt = 0;
                int setTotalCnt = 0;
                if (dansiBunpu.containsKey(String.valueOf(hrBunpuLine))) {
                    setDansiCnt = Integer.parseInt((String) dansiBunpu.get(String.valueOf(hrBunpuLine)));
                    setJyosiCnt = Integer.parseInt((String) jyosiBunpu.get(String.valueOf(hrBunpuLine)));
                    setTotalCnt = Integer.parseInt((String) totalBunpu.get(String.valueOf(hrBunpuLine)));
                }
                setDansiCnt += Integer.parseInt(dansiCnt);
                setJyosiCnt += Integer.parseInt(jyosiCnt);
                setTotalCnt += Integer.parseInt(totalCnt);
                dansiBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setDansiCnt));
                jyosiBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setJyosiCnt));
                totalBunpu.put(String.valueOf(hrBunpuLine), String.valueOf(setTotalCnt));
                hrBunpuLine++;
            }
        }
        for (final Iterator itBunpu = dansiBunpu.keySet().iterator(); itBunpu.hasNext();) {
            final String key = (String) itBunpu.next();
            final String cnt = (String) dansiBunpu.get(key);
            svf.VrsOutn("FIELD" + key, 1, cnt);
        }
        for (final Iterator itBunpu = jyosiBunpu.keySet().iterator(); itBunpu.hasNext();) {
            final String key = (String) itBunpu.next();
            final String cnt = (String) jyosiBunpu.get(key);
            svf.VrsOutn("FIELD" + key, 2, cnt);
        }
        for (final Iterator itBunpu = totalBunpu.keySet().iterator(); itBunpu.hasNext();) {
            final String key = (String) itBunpu.next();
            final String cnt = (String) totalBunpu.get(key);
            svf.VrsOutn("FIELD" + key, 3, cnt);
        }
        svf.VrsOutn("TOTAL_POINT3", 1, String.valueOf(dansiTotalScore));
        svf.VrsOutn("TOTAL_POINT3", 2, String.valueOf(jyosiTotalScore));
        svf.VrsOutn("TOTAL_NUM3", 1, String.valueOf(dansiTotalCnt));
        svf.VrsOutn("TOTAL_NUM3", 2, String.valueOf(jyosiTotalCnt));

        if (dansiTotalCnt > 0) {
            svf.VrsOutn("TOTAL_AVERAGE3", 1, avgSishagonyu(dansiTotalScore, dansiTotalCnt, 1));

        }

        if (jyosiTotalCnt > 0) {
            svf.VrsOutn("TOTAL_AVERAGE3", 2, avgSishagonyu(jyosiTotalScore, jyosiTotalCnt, 1));
        }
    }
    
    private String avgSishagonyu(final int total, final int count, final int scale) {
        return new BigDecimal(total).divide(new BigDecimal(count), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String sishagonyu(final String num, final int scale) {
        if (!NumberUtils.isNumber(num)) {
            return null;
        }
        return new BigDecimal(num).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printKihonData(final Vrw32alp svf, int retuCnt, final HrClassData hrClassData) {
        svf.VrsOutn("CLASS1", retuCnt, hrClassData._hrClassName);
        svf.VrsOutn("CLASS2", retuCnt, hrClassData._hrClassName);
        final AvgDat hrAvgDat = (AvgDat) hrClassData._avgDatMap.get("HR_CLASS");
        if (null != hrAvgDat) {
            svf.VrsOutn("TOTAL_POINT1", retuCnt, hrAvgDat._score);
            svf.VrsOutn("TOTAL_NUM1", retuCnt, hrAvgDat._cnt);
            svf.VrsOutn("TOTAL_NUM2", retuCnt, hrAvgDat._cnt);
            if (null != hrAvgDat._avg) {
                svf.VrsOutn("TOTAL_AVERAGE1", retuCnt, sishagonyu(hrAvgDat._avg, 1));
                svf.VrsOutn("TOTAL_AVERAGE2", retuCnt, sishagonyu(hrAvgDat._avgKansan, 1));
            }
            //分布の合計欄
            svf.VrsOutn("CLASS2", 7, "全体");
            svf.VrsOutn("CLASS3", 1, "男子");
            svf.VrsOutn("CLASS3", 2, "女子");
            svf.VrsOutn("CLASS3", 3, "学年");
            final AvgDat gradeAvgDat = (AvgDat) hrClassData._avgDatMap.get("GRADE");
            svf.VrsOutn("TOTAL_POINT2", 7, gradeAvgDat._scoreKansan);
            svf.VrsOutn("TOTAL_POINT3", 3, gradeAvgDat._scoreKansan);
            if (null != gradeAvgDat._avg) {
                svf.VrsOut("GRADE_AVERAGE", sishagonyu(gradeAvgDat._avg, 1));
            }

            svf.VrsOutn("TOTAL_NUM2", 7, gradeAvgDat._cnt);
            svf.VrsOutn("TOTAL_NUM3", 3, gradeAvgDat._cnt);
            if (null != gradeAvgDat._avgKansan) {
                svf.VrsOutn("TOTAL_AVERAGE3", 3, sishagonyu(gradeAvgDat._avgKansan, 1));
                svf.VrsOutn("TOTAL_AVERAGE2", 7, sishagonyu(gradeAvgDat._avgKansan, 1));
            }
        }
    }

    private void printHrBunpuGoukei(final Vrw32alp svf, final List printHrList, final Map bunpu) {
        Map hrBunpu = new LinkedMap();
        for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
            final HrClassData hrClassData = (HrClassData) itHr.next();
            int hrBunpuLine = 1;
            for (final Iterator itBunpu = bunpu.keySet().iterator(); itBunpu.hasNext();) {
                final Integer lowScore = (Integer) itBunpu.next();
                final String highScore = (String) bunpu.get(lowScore);
                final String bunpuCnt = hrClassData.getBunpuCnt(lowScore.toString(), highScore);
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
            svf.VrsOutn("CLASS_NUM" + key, 7, cnt);
        }
    }

    private void printHrBunpu(final Vrw32alp svf, final Map bunpu, int retuCnt, final HrClassData hrClassData) {
        int hrBunpuLine = 1;
        for (final Iterator itBunpu = bunpu.keySet().iterator(); itBunpu.hasNext();) {
            final Integer lowScore = (Integer) itBunpu.next();
            final String highScore = (String) bunpu.get(lowScore);
            final String bunpuCnt = hrClassData.getBunpuCnt(lowScore.toString(), highScore);
            svf.VrsOutn("CLASS_NUM" + hrBunpuLine, retuCnt, bunpuCnt);
            hrBunpuLine++;
        }
    }

    private void pritnStudent(final Vrw32alp svf, int retuCnt, final HrClassData hrClassData) {
        int stLine = 1;
        for (final Iterator iter = hrClassData._students.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            svf.VrsOutn("ATTENDNO" + stLine, retuCnt, student._attendNo);
            svf.VrsOutn("NAME" + stLine, retuCnt, student._name);
            svf.VrsOutn("SCORE" + stLine, retuCnt, student._score);
            stLine++;
        }
    }

    private List getPrintHrList(final DB2UDB db2, final Subclaas subclaas) throws SQLException {
        final List retHr = new ArrayList();
        final String HrSql = getHrSql();
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
                final HrClassData hrClassData = new HrClassData(db2, grade, hrClass, hrName, hrClassName, subclaas);
                retHr.add(hrClassData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retHr;
    }

    private String getHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME, ");
        stb.append("     HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("     AND SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     AND GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS ");

        return stb.toString();
    }

    private void printHeader(final Vrw32alp svf, final Subclaas subclaas) {
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
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class HrClassData {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName;
        final List _students;
        final Map _avgDatMap;

        public HrClassData(
                final DB2UDB db2,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName,
                final Subclaas subclaas
        ) throws SQLException {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName = hrClassName;
            _avgDatMap = getAvgDat(db2, _grade, _hrClass, subclaas);
            _students = getStudents(db2, _grade, _hrClass, subclaas);
        }

        public String getBunpuCnt(final String lowScore, final String highScore) {
            int retCnt = 0;
            final int intLow = Integer.parseInt(lowScore);
            final int intHigh = Integer.parseInt(highScore);
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

        public Map getBunpuDanjoCnt(final String lowScore, final String highScore) {
            Map retCnt = new HashMap();
            int dansi = 0;
            int jyosi = 0;
            int total = 0;
            int dansiScore = 0;
            int jyosiScore = 0;
            final int intLow = Integer.parseInt(lowScore);
            final int intHigh = Integer.parseInt(highScore);
            for (final Iterator iter = _students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                if (null != student._score) {
                    final int score = Integer.parseInt(student._score);
                    if (intLow <= score && score <= intHigh) {
                        if ("1".equals(student._sex)) {
                            dansi++;
                            dansiScore += score;
                        } else if ("2".equals(student._sex)) {
                            jyosi++;
                            jyosiScore += score;
                        }
                        total++;
                    }
                }
            }
            retCnt.put("DANSI", String.valueOf(dansi));
            retCnt.put("JYOSI", String.valueOf(jyosi));
            retCnt.put("TOTAL", String.valueOf(total));
            retCnt.put("DANSISCORE", String.valueOf(dansiScore));
            retCnt.put("JYOSISCORE", String.valueOf(jyosiScore));
            return retCnt;
        }

        private Map getAvgDat(
                final DB2UDB db2,
                final String grade,
                final String hrClass,
                final Subclaas subclaas
        ) throws SQLException {
            final Map retAvgMap = new HashMap();
            final String avgSql = getAvgSql(grade, hrClass, subclaas);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(avgSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String avgKansan = rs.getString("AVG_KANSAN");
                    final AvgDat avgDat = new AvgDat(score, scoreKansan, cnt, avg, avgKansan);
                    final String div = rs.getString("DIV");
                    retAvgMap.put(div, avgDat);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private String getAvgSql(final String grade, final String hrClass, final Subclaas subclaas) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'GRADE' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testCd + "' ");
            stb.append("     AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_GRADE + "' ");
            stb.append("     AND GRADE = '" + _param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     'HR_CLASS' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testCd + "' ");
            stb.append("     AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("              CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
            stb.append("     AND GRADE = '" + _param._grade + "' ");
            stb.append("     AND HR_CLASS = '" + hrClass + "' ");
            return stb.toString();
        }

        private List getStudents(
                final DB2UDB db2,
                final String grade,
                final String hrClass,
                final Subclaas subclaas
        ) throws SQLException {
            final List retStudent = new ArrayList();
            final String studentSql = getStudentsSql(grade, hrClass, subclaas);
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
                    final Student student = new Student(schregNo, attendNo, name, sex, score, scoreKansan, courseCd, majorCd, courseCode);
                    retStudent.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStudent;
        }

        private String getStudentsSql(final String grade, final String hrClass, final Subclaas subclaas) {
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
            stb.append("     LEFT JOIN RECORD_RANK_CONV_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + _param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testCd + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("              L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("              L1.SUBCLASSCD = '" + subclaas._subclassCd + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + _param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }
    }

    private class AvgDat {
        final String _score;
        final String _scoreKansan;
        final String _cnt;
        final String _avg;
        final String _avgKansan;

        public AvgDat(
                final String score,
                final String scoreKansan,
                final String cnt,
                final String avg,
                final String avgKansan
        ) {
            _score = score;
            _scoreKansan = scoreKansan;
            _cnt = cnt;
            _avg = avg;
            _avgKansan = avgKansan;
        }
    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _score;
        final String _scoreKansan;
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
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
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
        final String[] _categorySelected;
        final String _useCurriculumcd;
        private final Map _subclassMap;

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
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassMap = getSubclassMap(db2, _categorySelected, _year, _semester, _testCd, _grade, _useCurriculumcd);
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

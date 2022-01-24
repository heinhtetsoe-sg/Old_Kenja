/*
 * $Id: af5c4b386cda894bb3e386821ab3367d1ec19e7f $
 *
 * 作成日: 2017/08/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD618U {

    private static final Log log = LogFactory.getLog(KNJD618U.class);

    private static final String GAKKIMATSU = "9";

    private static final String CLASSCD_ALL = "99";
    private static final String CURRICULUM_ALL = "99";
    private static final String SUBCLASSCD_ALL = "999999";

    private static final int YUURYOU_MAXCNT = 20;
    private static final int YUURYOUALL_MAXCNT = 30;

    private Map _bunpuMap = new TreeMap();

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
        final String frmName = "J".equals(_param._schoolkind) ? "KNJD618U_1_1.frm" : "KNJD618U_1_2.frm";
        svf.VrSetForm(frmName, 1);
        setTitle(db2, svf);

        setBunpuData(db2);
        int maxSubclassCnt = "J".equals(_param._schoolkind) ? 10 : 8;
        int subclassCnt = 1;
        //度数分布
        for (Iterator itBunpu = _bunpuMap.keySet().iterator(); itBunpu.hasNext();) {
            final String subclassCd = (String) itBunpu.next();
            final PrintSubclass printSubclass = (PrintSubclass) _bunpuMap.get(subclassCd);
            String fieldName = "DI_SUBTOTAL" + subclassCnt;
            if ((CLASSCD_ALL + printSubclass._schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL).equals(subclassCd)) {
                fieldName = "DI_TOTAL";
                //総合は、１ページ目に出力する(レコードでは最終レコードなので、改ページを避ける為。ここで出力)
                int allYuuryouLineCnt = 1;
                String befAvg = "";
                int befCnt = 0;
                for (Iterator itAllYuuryou = printSubclass._yuuryouSyaList.iterator(); itAllYuuryou.hasNext();) {
                    final Student student = (Student) itAllYuuryou.next();
                    if (!printSubclass._scoreRange1Map.containsKey(student._avg)) {
                        continue;
                    }
                    final Integer rangeCnt = (Integer) printSubclass._scoreRange1Map.get(student._avg);
                    if (!befAvg.equals(student._avg)) {
                        befCnt += rangeCnt.intValue();
                    }
                    final int intRangeCnt = rangeCnt.intValue();
                    if (YUURYOUALL_MAXCNT < befCnt) {
                        break;
                    }
                    svf.VrsOutn("TOTAL_RANK", allYuuryouLineCnt, student._rank);
                    svf.VrsOutn("TOTAL_AVE", allYuuryouLineCnt, henkanRound(student._avg));
                    svf.VrsOutn("TOTAL_HR", allYuuryouLineCnt, student._hrClassName1);
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 14 ? "2" : "1";
                    svf.VrsOutn("TOTAL_ST_NAME" + nameField, allYuuryouLineCnt, student._name);
                    allYuuryouLineCnt++;
                    befAvg = student._avg;
                }
                printBunpuData(svf, subclassCnt, printSubclass, fieldName);
            } else {
                printBunpuData(svf, subclassCnt, printSubclass, fieldName);
                subclassCnt++;
            }
            _hasData = true;
        }

        //優良者
        subclassCnt = 1;
        for (Iterator itBunpu = _bunpuMap.keySet().iterator(); itBunpu.hasNext();) {
            final String subclassCd = (String) itBunpu.next();
            final PrintSubclass printSubclass = (PrintSubclass) _bunpuMap.get(subclassCd);
            int lineCnt = 1;
            String befScore = "";
            int befCnt = 0;
            if ((CLASSCD_ALL + printSubclass._schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL).equals(subclassCd)) {
                //総合は、１ページ目に出力する(レコードでは最終レコードなので、改ページを避ける為。度数分布処理で出力)
            } else {
                if (subclassCnt > maxSubclassCnt) {
                    svf.VrEndPage();
                    svf.VrSetForm("KNJD618U_2.frm", 1);
                    setTitle(db2, svf);
                    maxSubclassCnt = 15;
                    subclassCnt = 1;
                }
                svf.VrsOut("EX_SUBJECT_NAME" + subclassCnt, printSubclass._subclassName);
                for (Iterator itAllYuuryou = printSubclass._yuuryouSyaList.iterator(); itAllYuuryou.hasNext();) {
                    final Student student = (Student) itAllYuuryou.next();
                    if (!printSubclass._scoreRange1Map.containsKey(student._score)) {
                        continue;
                    }
                    final Integer rangeCnt = (Integer) printSubclass._scoreRange1Map.get(student._score);
                    if (!befScore.equals(student._score)) {
                        befCnt += rangeCnt.intValue();
                    }
                    final int intRangeCnt = rangeCnt.intValue();
                    if (YUURYOU_MAXCNT < befCnt) {
                        break;
                    }
                    svf.VrsOutn("SUBJECT_RANK" + subclassCnt, lineCnt, student._rank);
                    svf.VrsOutn("SUBJECT_SCORE" + subclassCnt, lineCnt, student._score);
                    svf.VrsOutn("SUBJECT_HR" + subclassCnt, lineCnt, student._hrClassName1);
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 14 ? "_2" : "_1";
                    svf.VrsOutn("SUBJECT_ST_NAME" + subclassCnt + nameField, lineCnt, student._name);
                    lineCnt++;
                    befScore = student._score;
                }
                subclassCnt++;
            }

            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void printBunpuData(final Vrw32alp svf, final int subclassCnt, final PrintSubclass printSubclass, final String fieldName) {
        int lineCnt = 1;
        svf.VrsOut("DI_SUBCLASS_NAME" + subclassCnt, printSubclass._subclassName);
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score100));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score95));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score90));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score85));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score80));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score75));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score70));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score65));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score60));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score55));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score50));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score45));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score40));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score35));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score30));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score25));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score20));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score15));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score10));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score5));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._score0));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(printSubclass._ninzu));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(henkanRound(printSubclass._avg)));
        svf.VrsOutn(fieldName, lineCnt++, String.valueOf(henkanRound(printSubclass._stdDev)));
    }

    private static String henkanRound(final String avg) {
        BigDecimal avgDecimal = new BigDecimal(avg);
        return String.valueOf(avgDecimal.setScale(1, BigDecimal.ROUND_HALF_UP));
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("TITLE", _param._gradeName + (StringUtils.isBlank(_param._courseName) ? "" : "　" + _param._courseName) + "　" + _param._testName + "　成績資料");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
    }

    private void setBunpuData(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int yuuryouCnt = 1;
            String befSubclass = "";
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String hrName = rs.getString("HR_NAME");
                final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String score = rs.getString("SCORE");
                final String rank = rs.getString("RANK");
                final String avg = rs.getString("AVG");
                final String subclassKey = classCd + schoolKind + curriculumCd + subclassCd;
                if (_param._isNoPrintMoto && _param._replaceCombinedAttendSubclassList.contains(subclassKey)) {
                    continue;
                }

                final Student student = new Student(schregno, grade, hrClass, attendno, name, sex, hrName, hrClassName1, classCd, schoolKind, curriculumCd, subclassCd, subclassName, score, rank, avg);
                if (!"".equals(befSubclass) && !befSubclass.equals(subclassKey)) {
                    yuuryouCnt = 1;
                }

                PrintSubclass printSubclass = null;
                if (_bunpuMap.containsKey(subclassKey)) {
                    printSubclass = (PrintSubclass) _bunpuMap.get(subclassKey);
                } else {
                    printSubclass = new PrintSubclass(db2, classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                }
                final String taisyouScore = (CLASSCD_ALL + schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL).equals(subclassKey) ? avg : score;
                printSubclass.setCnt(taisyouScore);
                printSubclass.setYuuryouList(yuuryouCnt, schoolKind, taisyouScore, subclassKey, student);

                _bunpuMap.put(subclassKey, printSubclass);
                befSubclass = subclassKey;
                yuuryouCnt++;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECORD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_CLASS_NAME1, ");
        stb.append("     RECORD.CLASSCD, ");
        stb.append("     RECORD.SCHOOL_KIND, ");
        stb.append("     RECORD.CURRICULUM_CD, ");
        stb.append("     RECORD.SUBCLASSCD, ");
        stb.append("     SUBCLASS.SUBCLASSNAME, ");
        stb.append("     RECORD.SCORE, ");
        if ("ALL".equals(_param._course)) {
            stb.append("     RECORD.GRADE_AVG_RANK AS RANK, ");
        } else {
            stb.append("     RECORD.COURSE_AVG_RANK AS RANK, ");
        }
        stb.append("     RECORD.AVG ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT RECORD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON RECORD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON RECORD.YEAR = REGD.YEAR ");
        stb.append("          AND REGD.SEMESTER = '" + _param.getSeme() + "' ");
        stb.append("          AND RECORD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("          AND REGD.GRADE = '" + _param._grade + "' ");
        if (!"ALL".equals(_param._course)) {
            stb.append("          AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '" + _param._course + "' ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUBCLASS ON RECORD.CLASSCD = SUBCLASS.CLASSCD ");
        stb.append("          AND RECORD.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
        stb.append("          AND RECORD.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
        stb.append("          AND RECORD.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     RECORD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND RECORD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND RECORD.TESTKINDCD || RECORD.TESTITEMCD || RECORD.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("     AND (SUBCLASS.SUBCLASSCD IS NOT NULL OR RECORD.SUBCLASSCD = '" + SUBCLASSCD_ALL + "') ");
        stb.append(" ORDER BY ");
        stb.append("     RECORD.CLASSCD, ");
        stb.append("     RECORD.SCHOOL_KIND, ");
        stb.append("     RECORD.CURRICULUM_CD, ");
        stb.append("     RECORD.SUBCLASSCD, ");
        if ("ALL".equals(_param._course)) {
            stb.append("     RECORD.GRADE_AVG_RANK, ");
        } else {
            stb.append("     RECORD.COURSE_AVG_RANK, ");
        }
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _hrName;
        final String _hrClassName1;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _score;
        final String _rank;
        final String _avg;
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String name,
                final String sex,
                final String hrName,
                final String hrClassName1,
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName,
                final String score,
                final String rank,
                final String avg
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _score = score;
            _rank = rank;
            _avg = avg;
        }
    }

    /** 科目クラス */
    private class PrintSubclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        int _score100 = 0;
        int _score95 = 0;
        int _score90 = 0;
        int _score85 = 0;
        int _score80 = 0;
        int _score75 = 0;
        int _score70 = 0;
        int _score65 = 0;
        int _score60 = 0;
        int _score55 = 0;
        int _score50 = 0;
        int _score45 = 0;
        int _score40 = 0;
        int _score35 = 0;
        int _score30 = 0;
        int _score25 = 0;
        int _score20 = 0;
        int _score15 = 0;
        int _score10 = 0;
        int _score5 = 0;
        int _score0 = 0;
        String _ninzu;
        String _avg;
        String _stdDev;
        List _yuuryouSyaList = new ArrayList();
        Map _scoreRange1Map = new HashMap();
        public PrintSubclass(
                final DB2UDB db2,
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName
        ) throws SQLException {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            setAvgDat(db2);
        }

        public void setAvgDat(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String subclassKey = _classCd + _schoolKind + _curriculumCd + _subclassCd;
                final String sql = (CLASSCD_ALL + _schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL).equals(subclassKey) ? getAllAvg() : getAvgSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _ninzu = rs.getString("COUNT");
                    _avg = rs.getString("AVG");
                    _stdDev = rs.getString("STDDEV");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

        }

        private String getAvgSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVGSDIV.COUNT, ");
            stb.append("     AVGSDIV.AVG, ");
            stb.append("     AVGSDIV.STDDEV ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT AVGSDIV ");
            stb.append(" WHERE ");
            stb.append("     AVGSDIV.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND AVGSDIV.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND AVGSDIV.TESTKINDCD || AVGSDIV.TESTITEMCD || AVGSDIV.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("     AND AVGSDIV.CLASSCD = '" + _classCd + "' ");
            stb.append("     AND AVGSDIV.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND AVGSDIV.CURRICULUM_CD = '" + _curriculumCd + "' ");
            stb.append("     AND AVGSDIV.SUBCLASSCD = '" + _subclassCd + "' ");
            if (!"ALL".equals(_param._course)) {
                stb.append("     AND AVGSDIV.AVG_DIV = '3' ");
            } else {
            	stb.append("     AND AVGSDIV.AVG_DIV = '1' ");
            }
            stb.append("     AND AVGSDIV.GRADE = '" + _param._grade + "' ");
            if (!"ALL".equals(_param._course)) {
                stb.append("     AND AVGSDIV.COURSECD || AVGSDIV.MAJORCD || AVGSDIV.COURSECODE = '" + _param._course + "' ");
            }
            return stb.toString();
        }

        private String getAllAvg() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG(RECORD.AVG) AS AVG, ");
            stb.append("     STDDEV(RECORD.AVG) AS STDDEV, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RECORD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON RECORD.YEAR = REGD.YEAR ");
            stb.append("          AND REGD.SEMESTER = '" + _param.getSeme() + "' ");
            stb.append("          AND RECORD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("          AND REGD.GRADE = '" + _param._grade + "' ");
            if (!"ALL".equals(_param._course)) {
                stb.append("     AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '" + _param._course + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     RECORD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND RECORD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND RECORD.TESTKINDCD || RECORD.TESTITEMCD || RECORD.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("     AND RECORD.CLASSCD = '" + CLASSCD_ALL + "' ");
            stb.append("     AND RECORD.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND RECORD.CURRICULUM_CD = '" + CURRICULUM_ALL + "' ");
            stb.append("     AND RECORD.SUBCLASSCD = '" + SUBCLASSCD_ALL + "' ");
            return stb.toString();
        }

        public void setCnt(final String score) {
            final double intScore = Double.parseDouble(score);
            if (100 <= intScore) {
                _score100++;
            } else if (95 <= intScore) {
                _score95++;
            } else if (90 <= intScore) {
                _score90++;
            } else if (85 <= intScore) {
                _score85++;
            } else if (80 <= intScore) {
                _score80++;
            } else if (75 <= intScore) {
                _score75++;
            } else if (70 <= intScore) {
                _score70++;
            } else if (65 <= intScore) {
                _score65++;
            } else if (60 <= intScore) {
                _score60++;
            } else if (55 <= intScore) {
                _score55++;
            } else if (50 <= intScore) {
                _score50++;
            } else if (45 <= intScore) {
                _score45++;
            } else if (40 <= intScore) {
                _score40++;
            } else if (35 <= intScore) {
                _score35++;
            } else if (30 <= intScore) {
                _score30++;
            } else if (25 <= intScore) {
                _score25++;
            } else if (20 <= intScore) {
                _score20++;
            } else if (15 <= intScore) {
                _score15++;
            } else if (10 <= intScore) {
                _score10++;
            } else if (5 <= intScore) {
                _score5++;
            } else {
                _score0++;
            }
            if (null != score && !"".equals(score)) {
                int setCnt = 0;
                if (_scoreRange1Map.containsKey(score)) {
                    final Integer integerCnt = (Integer) _scoreRange1Map.get(score);
                    setCnt = integerCnt.intValue();
                }
                setCnt++;
                _scoreRange1Map.put(score, new Integer(setCnt));
            }
        }

        public void setYuuryouList(
                int yuuryouCnt,
                final String schoolKind,
                final String score,
                final String subclassKey,
                final Student student
        ) {
            final int yuuryouScore = 80;
            if ((CLASSCD_ALL + schoolKind + CURRICULUM_ALL + SUBCLASSCD_ALL).equals(subclassKey)) {
                if (Double.parseDouble(score) >= yuuryouScore && yuuryouCnt <= YUURYOUALL_MAXCNT) {
                    _yuuryouSyaList.add(student);
                }
            } else {
                if (Integer.parseInt(score) >= yuuryouScore && yuuryouCnt <= YUURYOU_MAXCNT) {
                    _yuuryouSyaList.add(student);
                }
            }
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 66256 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _major;
        final String _grade;
        final String _course;
        final String _testcd;
        final String _ctrlYear;
        final String _ctrlSeme;
        final String _loginDate;
        final String _prgid;
        final String _usecurriculumcd;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useSchoolDetailGcmDat;
        final String _useSchoolKindField;
        final String _paraSchoolkind;
        final String _gradeName;
        final String _schoolkind;
        final String _schoolkindName;
        final String _schoolcd;
        final String _printLogStaffcd;
        final String _semesterName;
        final String _testName;
        final String _courseName;
        boolean _isNoPrintMoto;
        final List _replaceCombinedAttendSubclassList = new ArrayList();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _major = request.getParameter("MAJOR");
            _grade = request.getParameter("GRADE");
            _course = request.getParameter("COURSE");
            _testcd = request.getParameter("TESTCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useSchoolDetailGcmDat = request.getParameter("use_school_detail_gcm_dat");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            final String[] schoolkindData = getSchoolKind(db2);
            _gradeName = schoolkindData[0];
            _schoolkind = schoolkindData[1];
            _schoolkindName = schoolkindData[2];
            _semesterName = getSemesterName(db2);
            _testName = getTestName(db2);
            _courseName = getCourseName(db2);
            loadNameMstD016(db2);
            setReplaceCombined(db2);
        }

        private String[] getSchoolKind(final DB2UDB db2) throws SQLException {
            String[] retStr = {"", "", ""};
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr[0] = rs.getString("GRADE_NAME1");
                    retStr[1] = rs.getString("SCHOOL_KIND");
                    retStr[2] = rs.getString("ABBV1");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKind() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     A023.ABBV1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
            stb.append("          AND GDAT.SCHOOL_KIND = A023.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + _grade + "' ");
            return stb.toString();
        }

        public String getSeme() {
            return GAKKIMATSU.equals(_semester) ? _ctrlSeme : _semester;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemester();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemester() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            return stb.toString();
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getTestNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("TESTITEMNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getTestNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            if ("1".equals(_useSchoolDetailGcmDat)) {
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ");
            } else {
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            }
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ");
            if ("1".equals(_useSchoolDetailGcmDat)) {
                stb.append("             AND GRADE = '00' ");
                stb.append("             AND COURSECD || '-' || MAJORCD = '" + _major + "' ");
                if ("1".equals(_useSchoolKindField)) {
                    stb.append(" AND SCHOOL_KIND  = '" + _schoolkind + "' ");
                    stb.append(" AND SCHOOLCD     = '" + _schoolcd + "' ");
                }
            }

            return stb.toString();
        }

        private String getCourseName(final DB2UDB db2) {
        	if ("ALL".equals(_course)) {
        		return "";
        	}

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     COURSECODE_MST ");
            stb.append(" WHERE ");
            stb.append("     COURSECODE = '" + (_course.length() > 4 ? _course.substring(4) : null) + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D" + _schoolkind + "16' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void setReplaceCombined(final DB2UDB db2) {
            final String sql = "SELECT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
            final List rowList = KnjDbUtils.query(db2, sql);
            _replaceCombinedAttendSubclassList.addAll(KnjDbUtils.getColumnDataList(rowList, "ATTEND_SUBCLASSCD"));
        }

    }
}

// eof


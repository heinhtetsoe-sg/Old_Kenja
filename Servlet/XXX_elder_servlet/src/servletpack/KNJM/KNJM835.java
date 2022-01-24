/*
 * $Id: 2fedf8198de14de3adf0dddec0ca0aa19aac073c $
 *
 * 作成日: 2012/12/11
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

/**
 * 定期考査欠点者一覧
 */
public class KNJM835 {

    private static final Log log = LogFactory.getLog(KNJM835.class);

    private static String PRGID_KNJM836 = "KNJM836";
    private static String PRGID_KNJM835W = "KNJM835W";
    private static String PRGID_KNJM836W = "KNJM836W";

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

    private List<KettenStudent> getKettenStudentList(final DB2UDB db2) {
        final List<KettenStudent> list = new ArrayList();
        final Map<String, KettenStudent> map = new HashMap<String, KettenStudent>();
        final Map<String, Map<String, KettenKamoku>> kamokuMap = new HashMap<String, Map<String, KettenKamoku>>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String kettenSql = getKettenSql();
             log.info(" kettenSql = " + kettenSql);
             ps = db2.prepareStatement(kettenSql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String schregno = rs.getString("SCHREGNO");
                 if (!map.containsKey(schregno)) {
                     final String name = rs.getString("NAME");
                     KettenStudent kettenStudent = new KettenStudent(schregno, name);
                     list.add(kettenStudent);
                     map.put(schregno, kettenStudent);
                 }
                 final KettenStudent student = map.get(schregno);

                 final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                 final String subclassname = rs.getString("SUBCLASSNAME");

                 if (_param._isGakkiSeiseiki) {
                     final Integer seq = Integer.valueOf(rs.getString("SEQ"));
                     final String score = rs.getString("SCORE");
                     final String testDate = rs.getString("TEST_DATE");
                     if (!getMappedMap(kamokuMap, schregno).containsKey(subclasscd)) {
                         final KettenKamoku kettenKamoku = new KettenKamoku(student, subclasscd, subclassname, null, null);
                         getMappedMap(kamokuMap, schregno).put(subclasscd, kettenKamoku);
                         student._kettenKamokuList.add(kettenKamoku);
                     }
                     final KettenKamoku kettenKamoku = getMappedMap(kamokuMap, schregno).get(subclasscd);
                     if (seq.intValue() == 1) {
                         kettenKamoku._score = score;
                     } else {
                         if (NumberUtils.isDigits(score)) {
                             kettenKamoku._seqScoreMap.put(seq, Integer.valueOf(score));
                             kettenKamoku._seqTestDateMap.put(seq, testDate);
                         }
                     }

                 } else {
                     final String score = rs.getString("SCORE");
                     final String scoreHoju = rs.getString("SCORE_HOJU");
                     final KettenKamoku kettenKamoku = new KettenKamoku(student, subclasscd, subclassname, score, scoreHoju);
                     student._kettenKamokuList.add(kettenKamoku);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJM835.frm", 4);
        final int maxLine = 40;
        final List<KettenStudent> kettenStudentList = getKettenStudentList(db2);

        final List<List<KettenKamoku>> kettenKamokuPageList = getPageList(kettenStudentList, maxLine);
        final int totalPage = kettenKamokuPageList.size();

        String title = _param._nendo + " " + _param._semesterName;
        if (_param._isGakkiSeiseiki) {
            title += " 学期末";
            if (Arrays.asList(PRGID_KNJM836W).contains(_param._prgid)) {
                title += " 補充指導状況";
            } else {
                title += " 欠点者一覧";
            }
        } else {
            if (Arrays.asList(PRGID_KNJM835W, PRGID_KNJM836W).contains(_param._prgid)) {
                title += " " + _param._testitemname;
            }
            if (Arrays.asList(PRGID_KNJM836, PRGID_KNJM836W).contains(_param._prgid)) {
                title += " 定期考査補充指導状況";
            } else {
                title += " 定期考査欠点者一覧";
            }
        }

        KettenStudent student = null;
        for (int pi = 0; pi < kettenKamokuPageList.size(); pi++) {
            final List<KettenKamoku> kettenKamokuList = kettenKamokuPageList.get(pi);

            svf.VrsOut("TITLE", title);
            svf.VrsOut("PAGE", String.valueOf(pi + 1));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));

            for (final KettenKamoku kettenKamoku : kettenKamokuList) {

                if (null == student || student != kettenKamoku._student) {
                    student = kettenKamoku._student;
                }
                svf.VrsOut("SCHREG_NO", student._schregno);
                final String suf = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + suf, student._name);
                svf.VrsOut("SUBCLASS_SUM", String.valueOf(student._kettenKamokuList.size()));

                svf.VrsOut("AVERAGE", student.getAvg());

                final String suf2 = KNJ_EditEdit.getMS932ByteLength(kettenKamoku._subclassname) > 20 ? "2" : "1";
                svf.VrsOut("SUBCLASS_NAME" + suf2, kettenKamoku._subclassname); // 科目名
                svf.VrsOut("SCORE", kettenKamoku._score);
                if (Arrays.asList(PRGID_KNJM836, PRGID_KNJM836W).contains(_param._prgid)) {
                    if (_param._isGakkiSeiseiki) {
                        Integer hoju = null;
                        String testDate = null;
                        for (final Integer seq : kettenKamoku._seqScoreMap.keySet()) {
                            final Integer score = kettenKamoku._seqScoreMap.get(seq);
                            if (null == hoju || hoju < score) {
                                hoju = score;
                                testDate = kettenKamoku._seqTestDateMap.get(seq);
                            }
                        }
                        if (null != hoju) {
                            log.info(" schregno " + student._schregno + ", subclasscd " + kettenKamoku._subclasscd + ", seqScore " + kettenKamoku._seqScoreMap + ", " + kettenKamoku._seqTestDateMap);
                            svf.VrsOut("LEAD_SCORE", hoju.toString());
                            svf.VrsOut("REMARK", KNJ_EditDate.h_format_JP(db2, testDate));
                        }
                    } else {
                        svf.VrsOut("LEAD_SCORE", kettenKamoku._scoreHoju);
                    }
                }
                _hasData = true;
                svf.VrEndRecord();
            }
        }
    }

    protected static <K, V, U> Map<V, U> getMappedMap(final Map<K, Map<V, U>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<V, U>());
        }
        return map.get(key1);
    }

    private List<List<KettenKamoku>> getPageList(final List<KettenStudent> kettenStudentList, final int size) {
        final int checkScore;
        if (Arrays.asList(PRGID_KNJM835W, PRGID_KNJM836W).contains(_param._prgid)) {
            checkScore = 31;
        } else {
            checkScore = 40;
        }
        final List<List<KettenKamoku>> pageList = new ArrayList();
        List<KettenKamoku> current = null;
        for (final KettenStudent kettenStudent : kettenStudentList) {
            for (final KettenKamoku kettenKamoku : kettenStudent._kettenKamokuList) {
                if (NumberUtils.isNumber(kettenKamoku._score) && Double.parseDouble(kettenKamoku._score) >= checkScore) {
                    continue; // 40点以上は表示しない
                }
                if (null == current || current.size() >= size) {
                    current = new ArrayList();
                    pageList.add(current);
                }
                current.add(kettenKamoku);
            }
        }
        return pageList;
    }

    public String getKettenSql() {

        final boolean useRecordScoreHistDat = Arrays.asList(PRGID_KNJM835W, PRGID_KNJM836W).contains(_param._prgid);

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STD_MAX AS ( ");
        stb.append("     SELECT YEAR, SEMESTER, CHAIRCD, SCHREGNO, MAX(APPDATE) AS APPEDATE ");
        stb.append("     FROM CHAIR_STD_DAT ");
        stb.append("     WHERE YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     GROUP BY YEAR, SEMESTER, CHAIRCD, SCHREGNO ");
        stb.append(" ), SUBCLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        if (_param._isGakkiSeiseiki) {
            stb.append("         AVG(DECIMAL(VALUE(T4.VALUE,0),5,1)) AS AVG, ");
        } else if (useRecordScoreHistDat) {
            stb.append("         AVG(DECIMAL(VALUE(T4.SCORE,0),5,1)) AS AVG, ");
        } else {
            stb.append("         AVG(DECIMAL(VALUE(T4.SEM1_INTR_VALUE,0),5,1)) AS AVG, ");
        }
        stb.append("         COUNT(*) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         INNER JOIN CHAIR_STD_MAX T1_2 ON T1_2.YEAR = T1.YEAR ");
        stb.append("             AND T1_2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T1_2.CHAIRCD = T1.CHAIRCD ");
        stb.append("             AND T1_2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
        if (_param._isGakkiSeiseiki) {
            stb.append("         LEFT JOIN RECORD_SCORE_HIST_DAT T4 ON T4.YEAR = T2.YEAR ");
            stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("             AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("             AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T4.SEQ = 1 ");
            stb.append("             AND T4.VALUE IS NOT NULL ");
        } else if (useRecordScoreHistDat) {
            stb.append("         LEFT JOIN RECORD_SCORE_HIST_DAT T4 ON T4.YEAR = T2.YEAR ");
            stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("             AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("             AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T4.SEQ = 1 ");
            stb.append("             AND T4.SCORE IS NOT NULL ");
            stb.append("         INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT T5 ON T5.YEAR = T4.YEAR ");
            stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
            stb.append("             AND T5.TESTKINDCD = T4.TESTKINDCD ");
            stb.append("             AND T5.TESTITEMCD = T4.TESTITEMCD ");
            stb.append("             AND T5.SCORE_DIV = T4.SCORE_DIV ");
            stb.append("             AND T5.CLASSCD = T2.CLASSCD ");
            stb.append("             AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND T5.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("             AND T5.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T5.SEM_PASS_FLG = '1' ");
        } else {
            stb.append("         LEFT JOIN RECORD_DAT T4 ON T4.YEAR = T2.YEAR ");
            stb.append("             AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("             AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T4.SEM1_INTR_VALUE IS NOT NULL ");
            stb.append("         INNER JOIN SUBCLASS_STD_PASS_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("             AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T5.CLASSCD = T2.CLASSCD ");
            stb.append("             AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND T5.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("             AND T5.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T5.SEM_PASS_FLG = '1' ");
        }
        stb.append("     GROUP BY ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T4.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.AVG, ");
        stb.append("     T3.SUBCLASSNAME, ");
        if (_param._isGakkiSeiseiki) {
            stb.append("     T4.SEQ, ");
            stb.append("     T4.TEST_DATE, ");
            stb.append("     T4.VALUE AS SCORE ");
        } else if (useRecordScoreHistDat) {
            stb.append("     T4.SEQ, ");
            stb.append("     T4.SCORE AS SCORE, ");
            stb.append("     T4_2.SCORE AS SCORE_HOJU ");
        } else {
            stb.append("     T4." + _param._scoreField + " AS SCORE, ");
            stb.append("     T4." + _param._scoreFieldHoju + " AS SCORE_HOJU ");
        }
        stb.append(" FROM ");
        stb.append("     SUBCLASS T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        if (_param._isGakkiSeiseiki) {
            stb.append("     INNER JOIN RECORD_SCORE_HIST_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if (Arrays.asList(PRGID_KNJM835W).contains(_param._prgid)) {
                stb.append("         AND T4.SEQ = 1 "); // 全て
            }
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T4.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T4.VALUE IS NOT NULL ");
        } else if (useRecordScoreHistDat) {
            stb.append("     INNER JOIN RECORD_SCORE_HIST_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.SEQ = 1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T4.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT T5 ON T5.YEAR = T4.YEAR ");
            stb.append("         AND T5.SEMESTER = T4.SEMESTER ");
            stb.append("         AND T5.TESTKINDCD = T4.TESTKINDCD ");
            stb.append("         AND T5.TESTITEMCD = T4.TESTITEMCD ");
            stb.append("         AND T5.SCORE_DIV = T4.SCORE_DIV ");
            stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T5.SCHREGNO = T4.SCHREGNO ");
            stb.append("         AND T5.SEM_PASS_FLG = '1' ");
            stb.append("     LEFT JOIN V_RECORD_SCORE_HIST_DAT T4_2 ON T4_2.YEAR = T4.YEAR ");
            stb.append("         AND T4_2.SEMESTER = T4.SEMESTER ");
            stb.append("         AND T4_2.TESTKINDCD = T4.TESTKINDCD ");
            stb.append("         AND T4_2.TESTITEMCD = T4.TESTITEMCD ");
            stb.append("         AND T4_2.SCORE_DIV = T4.SCORE_DIV ");
            stb.append("         AND T4_2.CLASSCD = T4.CLASSCD ");
            stb.append("         AND T4_2.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("         AND T4_2.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("         AND T4_2.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("         AND T4_2.SCHREGNO = T4.SCHREGNO ");
            stb.append("         AND T4_2.SEQ > 1 ");
            stb.append(" WHERE ");
            stb.append("     T4.SCORE IS NOT NULL ");
        } else {
            stb.append("     INNER JOIN RECORD_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T4.SCHREGNO ");
            stb.append("         AND VALUE(T2.INOUTCD, '') <> '8' "); // 聴講生は集計の対象外
            stb.append("     INNER JOIN SUBCLASS_STD_PASS_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T5.SCHREGNO = T4.SCHREGNO ");
            stb.append("         AND T5.SEM_PASS_FLG = '1' ");
            stb.append(" WHERE ");
            stb.append("     T4." + _param._scoreField + " IS NOT NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SUBSTR(T4.SCHREGNO, 1, 4) DESC, ");
        stb.append("     SUBSTR(T4.SCHREGNO, 5, 4) ASC, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    private static class KettenStudent {
        final String _schregno;
        final String _name;
        final List<KettenKamoku> _kettenKamokuList = new ArrayList();
        KettenStudent(
                final String schregno,
                final String name
        ) {
            _schregno = schregno;
            _name = name;
        }
        public String getAvg() {
            BigDecimal sum = new BigDecimal(0);
            int count = 0;
            for (final KettenKamoku kamoku : _kettenKamokuList) {
                if (NumberUtils.isNumber(kamoku._score)) {
                    sum = sum.add(new BigDecimal(kamoku._score));
                    count += 1;
                }
            }
            if (0 == count) {
                return null;
            }
            return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    private static class KettenKamoku {
        final String _subclasscd;
        final String _subclassname;
        String _score;
        final String _scoreHoju;
        final KettenStudent _student;
        final Map<Integer, Integer> _seqScoreMap = new TreeMap<Integer, Integer>();
        final Map<Integer, String> _seqTestDateMap = new TreeMap<Integer, String>();
        KettenKamoku(
                final KettenStudent student,
                final String subclasscd,
                final String subclassname,
                final String score,
                final String scoreHoju
        ) {
            _student = student;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _scoreHoju = scoreHoju;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77246 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _testcd; // KNJM835W、KNJM836W
        private final String _prgid;
        private final String _scoreField;
        private final String _loginDate;
        private final String _nendo;
        private final String _semesterName;
        private boolean _isGakkiSeiseiki = false;
        private String _testitemname;
        private String _scoreFieldHoju = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _testcd = request.getParameter("TESTCD");
            _prgid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            if ("2".equals(_semester)) {
                _scoreField = "SEM2_INTR_VALUE";
                _scoreFieldHoju = "SEM2_TERM_VALUE";
            } else {
                _scoreField = "SEM1_INTR_VALUE";
                _scoreFieldHoju = "SEM1_TERM_VALUE";
            }
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _semesterName = getSemesterName(db2);
            if (null != _testcd) {
                if ("990008".equals(_testcd)) {
                    _isGakkiSeiseiki = true;
                } else {
                    _testitemname = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT VALUE(TESTITEMNAME, '') AS TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' "));
                }
            }
        }


        public String getSemesterName(final DB2UDB db2) {
            String semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT VALUE(SEMESTERNAME, '') AS SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
            return semesterName;
        }
    }
}

// eof


// kanji=漢字
/*
 * $Id: 58529f9c4b22c5dbc3586cddd2ba195c53376fb6 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
//import servletpack.pdf.AlpPdf;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: 58529f9c4b22c5dbc3586cddd2ba195c53376fb6 $
 */
public class KNJD617Q {
    private static final Log log = LogFactory.getLog(KNJD617Q.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

    private static final String ATTRIBUTE_KETTEN = "Paint=(1,80,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,80,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";

    protected boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        Param param = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            param = createParam(request, db2);

            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            printMain(db2, param, ipdf);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != param) {
                for (final Iterator it = param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            int ret = svf.VrQuit();
            if (ret == 0) {
                log.info("===> VrQuit():" + ret);
            }
        }
    }

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void printMain(
            final DB2UDB db2,
            final Param param,
            final IPdf ipdf
    ) throws Exception {

        _hasData = false;

        if ("2".equals(param._gradeOrHr)) {
            for (int i = 0; i < param._classSelected.length; i++) {
                final HRInfo hrInfo = new HRInfo(param._classSelected[i]);  //HR組
                hrInfo.load(db2, param, null);
                // 印刷処理
                final Form form = new Form();
                if (form.print(ipdf, param, hrInfo)) {
                    _hasData = true;
                }
            }
            
        } else {
            final HRInfo hrInfo = new HRInfo(null);  //HR組
            hrInfo.load(db2, param, null);
            // 印刷処理
            final Form form = new Form();
            if (form.print(ipdf, param, hrInfo)) {
                _hasData = true;
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private static class HRInfo {
        final String _hrClass;
        private List _students = Collections.EMPTY_LIST;
        private final Map _subclasses = new TreeMap();
        private List _ranking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrAbsent;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数
        private String _avgGradeAverage; // 平均点の学級平均
        private String _avgGradeTotal; // 総合点の学年平均
        private String _maxTotal;   // 総合点の最高点
        private String _minTotal;   // 総合点の最低点
        private String _failTotal;  // 欠点の数
        
        HRInfo(final String hrClass) {
            _hrClass = hrClass;
        }

        public void load(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            loadStudents(db2, param, courseCd);
            loadStudentsInfo(db2, param);
            loadAttend(db2, param);
            loadHrclassAverage(db2, param, courseCd);
            loadRank(db2, param);
            loadScoreDetail(db2, param);
            _ranking = createRanking(param);
            log.debug("RANK:" + _ranking);
            setSubclassAverage(param);
            setHrTotal();  // 学級平均等の算出
            setHrTotalMaxMin();
            setHrTotalFail();
            setSubclassGradeAverage(db2, param);
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _students = new LinkedList();
            try {
                final String sql = sqlHrclassStdList(param);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"), this, rs.getString("HR_CLASS"), rs.getString("HR_CLASS_NAME1"));
                    _students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * SQL HR組の学籍番号を取得するSQL
         */
        private String sqlHrclassStdList(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.HR_CLASS, W1.ATTENDNO, COURSECD || MAJORCD AS MAJOR, COURSECD || MAJORCD || COURSECODE AS COURSE, HDAT.HR_CLASS_NAME1 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = W1.YEAR ");
            stb.append("    AND W1.SEMESTER = HDAT.SEMESTER ");
            stb.append("    AND W1.GRADE = HDAT.GRADE ");
            stb.append("    AND W1.HR_CLASS = HDAT.HR_CLASS ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("    AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append("    AND W1.GRADE = '" + param._grade + "' ");
            if (null != _hrClass) {
                stb.append("    AND W1.GRADE || W1.HR_CLASS = '" + _hrClass + "' ");
            }
            stb.append("ORDER BY W1.HR_CLASS, W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdNameInfo(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._attendNo = rs.getString("ATTENDNO");
                            student._name = rs.getString("NAME");
                            student._sexName = rs.getString("SEX_NAME");
                            String inoutcdcoursecodename = "";
                            if ("2".equals(rs.getString("INOUTCD"))) {
                                inoutcdcoursecodename = "○";
                            }
                            student._inoutcdcoursecodename = inoutcdcoursecodename;

                            TransInfo transInfo = null;
                            final String d1 = rs.getString("KBN_DATE1");
                            final String d2 = rs.getString("KBN_DATE2");
                            if (null != d1) {
                                final String n1 = rs.getString("KBN_NAME1");
                                transInfo = new TransInfo(d1, n1);
                            } else if (null != d2) {
                                final String n2 = rs.getString("KBN_NAME2");
                                transInfo = new TransInfo(d2, n2);
                            }
                            if (null == transInfo) {
                                transInfo = new TransInfo(null, null);
                            }
                            student._transInfo = transInfo;

                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         */
        private String sqlStdNameInfo(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, W3.INOUTCD, TCORCDM.COURSECODENAME, NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append("             ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append("        W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append("        (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = W1.YEAR AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = '" + param._semester + "' AND W2.GRADE = W1.GRADE ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                              AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
            stb.append("                                OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                                  AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("LEFT   JOIN COURSECODE_MST TCORCDM ON TCORCDM.COURSECODE = W1.COURSECODE ");
            stb.append("LEFT   JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = W3.SEX ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append("    AND W1.SCHREGNO = ? ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }

            return stb.toString();
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (code.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }

        private void loadAttend(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String psKey = "ATTENDSEMES";
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");
                    final String sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            null,
                            param._date,
                            param._attendParamMap
                    );
                    log.debug(" sql = " + sql);

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }

                        final AttendInfo attendInfo = new AttendInfo(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                rs.getInt("TRANSFER_DATE")
                        );
                        student._attendInfo = attendInfo;
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private void loadHrclassAverage(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassAverage(param);
                log.debug(" avg sql = " + sql);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String avgTotal = rs.getString("AVG_HR_TOTAL");
                    final String avgAveage = rs.getString("AVG_HR_AVERAGE");
                    if ("HR".equals(rs.getString("FLG"))) {
                    } else if ("GRADE".equals(rs.getString("FLG"))) {
                        _avgGradeTotal = avgTotal;
                        _avgGradeAverage = avgAveage;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String[] toArray(final Set set) {
            final List list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String) list.get(i);
            }
            return arr;
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private String sqlHrclassAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            stb.append(") ");

            stb.append("SELECT 'GRADE' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdTotalRank(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            if (param.SUBCLASSCD555555.equals(rs.getString("SUBCLASSCD"))) {
                                student._classRank555555 = rs.getInt("CLASS_RANK");
                                student._rank555555 = rs.getInt("TOTAL_RANK");
                                student._scoreSum555555 = rs.getString("TOTAL_SCORE");
                                student._scoreAvg555555 = rs.getString("TOTAL_AVG");
                            } else if (param.SUBCLASSCD999999.equals(rs.getString("SUBCLASSCD"))) {
                                student._classRank999999 = rs.getInt("CLASS_RANK");
                                student._rank999999 = rs.getInt("TOTAL_RANK");
                                student._scoreSum999999 = rs.getString("TOTAL_SCORE");
                                student._scoreAvg999999 = rs.getString("TOTAL_AVG");
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * SQL 任意の生徒の順位を取得するSQL
         */
        private String sqlStdTotalRank(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("             , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  W3.SCHREGNO ");
            if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                stb.append("   ,GRADE_AVG_RANK AS GRADE_RANK");
                stb.append("   ,CLASS_AVG_RANK AS CLASS_RANK");
            } else if (OUTPUT_KJUN3.equals(param._outputKijun)) {
                stb.append("   ,GRADE_DEVIATION_RANK AS GRADE_RANK");
                stb.append("   ,CLASS_DEVIATION_RANK AS CLASS_RANK");
            } else {
                stb.append("   ,GRADE_RANK ");
                stb.append("   ,CLASS_RANK ");
            }
            stb.append("       ," + param._rankFieldName + "  AS TOTAL_RANK ");
            stb.append("       ,W3.SCORE AS TOTAL_SCORE ");
            stb.append("       ,W3.SUBCLASSCD ");
            stb.append("       ,DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  (W3.SUBCLASSCD = '" + param.SUBCLASSCD555555 + "' OR W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "') ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String psKey = "SCORE_DETAIL";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlStdSubclassDetail(param);
                    log.debug(" subclass detail sql = " + sql);

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, param._grade);

                rs = ps.executeQuery();

                while (rs.next()) {
                    if (param._enablePringFlg && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        final ScoreDetail scoreDetail = new ScoreDetail(
                                getSubClass(rs, _subclasses),
                                rs.getString("SCORE"),
                                rs.getString("SCORE_DI"),
                                rs.getString("ASSESS_LEVEL"),
                                rs.getString("KARI_HYOUTEI"),
                                (Integer) rs.getObject("REPLACEMOTO"),
                                (String) rs.getObject("PRINT_FLG"),
                                null, // rs.getString("SLUMP"),
                                null, // rs.getString("SLUMP_MARK"),
                                null, // rs.getString("SLUMP_SCORE"),
                                (Integer) rs.getObject("COMP_CREDIT"),
                                (Integer) rs.getObject("GET_CREDIT"),
                                (Integer) rs.getObject("CREDITS")
                        );
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }

            try {
                final String psKey = "ATTENDSUBCLASS";
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            null,
                            param._date,
                            param._attendParamMap
                            );
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);

                log.info("load attendSubclass");
                long attendSubclassStart = System.currentTimeMillis();
                long attendSubclassAcc = 0;
                for (final Iterator sit = _students.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();

                    ps.setString(1, student._schregno);
                    long attendSubclassAccStart = System.currentTimeMillis();
                    rs = ps.executeQuery();
                    attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }

                        ScoreDetail scoreDetail = null;
                        for (final Iterator it = student._scoreDetails.keySet().iterator(); it.hasNext();) {
                            final String subclasscd = (String) it.next();
                            if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                                scoreDetail = (ScoreDetail) student._scoreDetails.get(subclasscd);
                                break;
                            }
                        }
                        if (null == scoreDetail) {
                            SubClass subClass = null;
                            for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
                                final String subclasscd = (String) it.next();
                                if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                                    subClass = (SubClass) _subclasses.get(subclasscd);
                                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null, null, null, null);
                                    student._scoreDetails.put(subclasscd, scoreDetail);
                                    break;
                                }
                            }
                            if (null == scoreDetail) {
                                continue;
                            }
                        }

                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                        final Integer specialAbsentMinutes = (Integer) rs.getObject("SPECIAL_SICK_MINUTES3");
                        if (specialGroupCd != null && specialAbsentMinutes != null) {
                            if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
                                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
                            }
                            int minute = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
                        }


                        if (0 != rs.getInt("MLESSON") && scoreDetail._subClass._jisu < rs.getInt("MLESSON")) {
                            scoreDetail._subClass._jisu = rs.getInt("MLESSON");
                        }
                        scoreDetail._jisu = (Integer) rs.getObject("MLESSON");
                        scoreDetail._absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                        if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
                    }

                    DbUtils.closeQuietly(rs);
                }
                long attendSubclassEnd = System.currentTimeMillis();
                log.info(" load attendSubclass elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("             , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = ? ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            stb.append(",CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     LEFT JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("            SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append("    FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.COURSECD = T2.COURSECD ");
            stb.append("        AND T1.MAJORCD = T2.MAJORCD ");
            stb.append("        AND T1.COURSECODE = T2.COURSECODE ");
            stb.append("        AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                    T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");

            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END AS ASSESS_LEVEL ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("           W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("           ,W3.COMP_CREDIT ");
            stb.append("           ,W3.GET_CREDIT ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

            //仮評定の表
            stb.append(",RECORD_KARI_HYOUTEI AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE AS KARI_HYOUTEI ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd.substring(0, 4) + SCORE_DIV_09 + "' ");
            stb.append("     ) ");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("             W2.SUBCLASSCD, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("              W2.SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,W4.VALUE_DI AS SCORE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T35.KARI_HYOUTEI ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV ");
            stb.append("        ,T7.SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        , CAST(NULL AS VARCHAR(1)) AS SLUMP ");
            stb.append("        , CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK ");
            stb.append("        , CAST(NULL AS SMALLINT) AS SLUMP_SCORE ");

            stb.append("    , W23.STAFFNAME ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI T35 ON T35.SUBCLASSCD = T1.SUBCLASSCD AND T35.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T8.CLASSCD || '-' || T8.SCHOOL_KIND = ");
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(" T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            }

            stb.append("     LEFT JOIN STAFF_MST W23 ON W23.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            SDET.CLASSCD || '-' || SDET.SCHOOL_KIND || '-' || SDET.CURRICULUM_CD || '-' || ");
            }
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append("    LEFT JOIN RECORD_SCORE_DAT W4 ON W4.YEAR = '" + param._year + "' AND W4.SEMESTER = '" + param._semester + "' AND W4.TESTKINDCD || W4.TESTITEMCD || W4.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("                                 AND W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || W4.SUBCLASSCD = T1.SUBCLASSCD AND W4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubClass(
                final ResultSet rs,
                final Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            try {
                subclasscode = rs.getString("SUBCLASSCD");
                if (rs.getString("CREDITS") != null) { credit = rs.getInt("CREDITS"); }
            } catch (Exception e) {
                 log.error("Exception", e);
            }
            //科目クラスのインスタンスを更新して返す
            SubClass subclass;
            if (!subclasses.containsKey(subclasscode)) {
                //科目クラスのインスタンスを作成して返す
                String classabbv = null;
                String subclassabbv = null;
                String subclassname = null;
                String staffname = null;
                boolean electdiv = false;
                try {
                    classabbv = rs.getString("CLASSNAME");
                    subclassabbv = rs.getString("SUBCLASSABBV");
                    subclassname = rs.getString("SUBCLASSNAME");
                    staffname = rs.getString("STAFFNAME");
                    if ("1".equals(rs.getString("ELECTDIV"))) {
                        electdiv = true;
                    }
                } catch (Exception e) {
                     log.error("Exception", e);
                }
                subclass = new SubClass(subclasscode, classabbv, subclassabbv, subclassname, electdiv, credit, staffname);
                subclasses.put(subclasscode, subclass);
            } else {
                subclass = (SubClass) subclasses.get(subclasscode);
                int[] maxMin = setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = maxMin[0];
                subclass._mincredit = maxMin[1];
            }
            return subclass;
        }

        /**
         * 欠点の算出
         */
        private void setHrTotalFail() {
            int countFail = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null != student._total) {
                    if (0 < student._total._countFail) {
                        countFail += student._total._countFail;
                    }
                }
            }
            if (0 < countFail) {
                _failTotal = String.valueOf(countFail);
            }
        }

        /**
         * 最高点・最低点の算出
         */
        private void setHrTotalMaxMin() {
            int totalMax = 0;
            int totalMin = Integer.MAX_VALUE;
            int countT = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null == student._scoreSum999999) {
                    continue;
                }
                countT++;
                final int totalInt = Integer.parseInt(student._scoreSum999999);
                //最高点
                totalMax = Math.max(totalMax, totalInt);
                //最低点
                totalMin = Math.min(totalMin, totalInt);
            }
            if (0 < countT) {
                _maxTotal = String.valueOf(totalMax);
                _minTotal = String.valueOf(totalMin);
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        private void setSubclassAverage(final Param param) {
            final Map map = new HashMap();

            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
                    final String scorevalue = detail._score;
                    if (null == map.get(detail._subClass)) {
                        map.put(detail._subClass, new int[5]);
                    }
                    final int[] arr = (int[]) map.get(detail._subClass);
                    if (null != scorevalue && StringUtils.isNumeric(scorevalue)) {
                        final int v = Integer.parseInt(scorevalue);
                        arr[0] += v;
                        arr[1]++;
                        //最高点
                        if (arr[2] < v) {
                            arr[2] = v;
                        }
                        //最低点
                        if (arr[3] > v || arr[1] == 1) {
                            arr[3] = v;
                        }
                    }
                    //欠点（赤点）
                    if (ScoreDetail.isFailCount(param, detail)) {
                        arr[4]++;
                    }
                }
            }

            for (final Iterator it = _subclasses.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    if (0 != val[1]) {
                        final double d = Math.round(val[0] * 10.0 / val[1]) / 10.0;
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                        subclass._scoreMax = String.valueOf(val[2]);
                        subclass._scoreMin = String.valueOf(val[3]);
                        if (0 != val[4]) {
                            subclass._scoreFailCnt = String.valueOf(val[4]);
                        }
                    }
                }
            }
        }

        /**
         * 学級平均の算出
         */
        private void setHrTotal() {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int absent = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrj = {0,0};  // 授業日数
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._count) {
                        totalT += totalObj._total;
                        countT++;
                    }
                    if (0< totalObj._count) {
                        totalA += totalObj._total;
                        countA += totalObj._count;
                    }
                }
                final AttendInfo attend = student._attendInfo;
                if (null != attend) {
                    mlesson += attend._mLesson;
                    present += attend._present;
                    absent += attend._absent;
                    arrj = setMaxMin(arrj[0], arrj[1], attend._mLesson);
                }
                arrc = setMaxMin(arrc[0], arrc[1], student._compCredit);
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < mlesson) {
                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
                _perHrAbsent = new BigDecimal((float) absent / (float) mlesson * 100);
            }
            if (0 < arrc[0]) {
                _HrCompCredits = arrc[0] + "単位";
            }
            if (0 < arrj[0]) {
                _HrMLesson = arrj[0] + "日";
            }
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    VALUE(T2.ELECTDIV, '0') AS ELECTDIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '" + param._avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");

            final String sql = stb.toString();
            log.debug(" gradeAverage sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();

                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String electDiv = rs.getString("ELECTDIV");

                    final SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
                    final BigDecimal subclassGradeAvg = rs.getBigDecimal("AVG");
                    if (subclass == null || subclassGradeAvg == null) {
                        continue;
                    }
                    subclass._scoresubaverage = Form.sishaGonyu(subclassGradeAvg);
                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 順位の算出
         */
        private List createRanking(final Param param) {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(param, student);
                if (0 < student._total._count) {
                    list.add(student._total);
                }
            }

            Collections.sort(list);
            return list;
        }

        private int rank(final Student student) {
            if (0 >= student._total._count) {
                return -1;
            }
            return 1 + _ranking.indexOf(student._total);
        }

        private static int[] setMaxMin(
                int maxInt,
                int minInt,
                int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new int[]{maxInt, minInt};
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class TransInfo {
        final String _date;
        final String _name;

        public TransInfo(
                final String date,
                final String name
        ) {
            _date = date;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _date) {
                sb.append(KNJ_EditDate.h_format_JP(_date));
            }
            if (null != _name) {
                sb.append(_name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private static class Student {
        int _gnum;  // 行番号
        final String _schregno;  // 学籍番号
        final HRInfo _hrInfo;
        private String _hrClass;
        private String _attendNo;
        private String _hrclassname1;
        private String _name;
        private String _sexName;
        private String _inoutcdcoursecodename;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0);
        private String _scoreSum555555;
        private String _scoreAvg555555;
        private int _classRank555555;
        private int _rank555555;
        private String _scoreSum999999;
        private String _scoreAvg999999;
        private int _classRank999999;
        private int _rank999999;
        private final Map _scoreDetails = new TreeMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
        private int _specialAbsent; // 特活欠課時数
        private String _attendSemesRemarkDatRemark1;

        Student(final String code, final HRInfo hrInfo, final String hrClass, final String hrClassName1) {
            _schregno = code;
            _hrInfo = hrInfo;
            _hrClass = hrClass;
            _hrclassname1 = hrClassName1;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver(final Param param) {
            return null != getKekkaOverKamokuCount(param);
        }

        public String getKekkaOverKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (detail._isOver) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (ScoreDetail.isFailCount(param, detail)) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenTanni(final Param param) {
            int credit = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (ScoreDetail.isFailCount(param, detail) && null != detail._credits) {
                    credit += detail._credits.intValue();
                }
            }
            return credit == 0 ? null : String.valueOf(credit);
        }

        public String getRemark(final Param param) {
            String remark = "";
            remark += _transInfo.toString();  // 備考
            return remark;
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
        }
        
        private static class StudentSorter implements Comparator {
            // 科目コード999999の順位でソート。順位がなければHR出欠番号順。
            final Param _param;
            StudentSorter(final Param param) {
                _param = param;
            }

            public int compare(final Object o1, final Object o2) {
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                final int s1rank = s1._rank999999;
                final int s2rank = s2._rank999999;
                int cmp = 0;
                if (s1rank != 0 && s2rank != 0) {
                    if (s1rank < s2rank) {
                        cmp = -1;
                    } else if (s1rank > s2rank) {
                        cmp = 1;
                    }
                } else if (s1rank != 0 || s2rank != 0) {
                    if (s1rank == 0) {
                        cmp = 1;
                    } else if (s2rank == 0) {
                        cmp = -1;
                    }
                }
                if (0 != cmp) {
                    return cmp;
                }
                return (s1._hrClass + s1._attendNo).compareTo(s2._hrClass + s2._attendNo);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class AttendInfo {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;

        AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        public boolean isAttendPerfect() {
            return _absent == 0 && _late == 0 && _early == 0;
        }

        public String toString() {
            return "Attendance(" + _lesson + ", " + _mLesson + ", " + _suspend + ", " + _mourning + ", " + _absent + ", " + _present + ", " + _late + ", " + _early + ")";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass {
        final String _classabbv;
        final String _classcode;
        final String _subclasscode;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _electdiv; // 選択科目
        final String _staffname;  // 科目担当者名
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoresubaverage;  // 学年平均
        private String _scoretotal;  // 学級合計
        private String _scoreCount;  // 学級人数
        private String _scoreMax;  // 最高点
        private String _scoreMin;  // 最低点
        private String _scoreFailCnt;  // 欠点者数


        SubClass(
                final String subclasscode,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final int credit,
                final String staffname
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3);
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _staffname = staffname;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + " , " +_electdiv + " , " +_maxcredit + " , " +_mincredit + " , " +_jisu +"]";
        }

        public String keySubclasscd() {
            return _subclasscode.substring(1);
        }

        public String getPrintCredit() {
            if (0 == _maxcredit) {
                return "";
            }
            return _maxcredit == _mincredit ? String.valueOf(_maxcredit) : String.valueOf(_mincredit) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_maxcredit);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        final SubClass _subClass;
        Double _absent;
        Integer _jisu;
        final String _score;
        final String _scoreDi;
        final String _assessLevel;
        final String _karihyotei;
        final Integer _replacemoto;
        final String _print_flg;
        final Integer _compCredit;
        final Integer _getCredit;
        BigDecimal _absenceHigh;
        final Integer _credits;
        boolean _isOver;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String scoreDi,
                final String assessLevel,
                final String karihyotei,
                final Integer replacemoto,
                final String print_flg,
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _assessLevel = assessLevel;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _print_flg = print_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpMark = slumpMark;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public Integer getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public Integer getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            return !(null != _replacemoto && _replacemoto.intValue() >= 1);
        }

        private static boolean isFail(final Param param, final ScoreDetail detail) {
            if (!NumberUtils.isDigits(detail._score)) {
                return false;
            }
            return Integer.parseInt(detail._score) <= param._ketten;
        }

        private static boolean isFailCount(final Param param, final ScoreDetail detail) {
            if (!NumberUtils.isDigits(detail._score)) {
                return false;
            }
            return Integer.parseInt(detail._score) <= param._ketten;
        }

        public String toString() {
            return (_subClass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private static class Total implements Comparable {
        final int _total;  // 総合点
        final int _count;  // 件数（成績）
        final BigDecimal _avgBigDecimal;  // 平均点
        final int _countFail;  //欠点科目数

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
         * @param student
         */
        Total(final Param param, final Student student) {

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            int countFail = 0;

            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final String scoreValue = detail._score;
                if (isAddTotal(detail._replacemoto, param)) {
                    if (null != scoreValue && StringUtils.isNumeric(scoreValue)) {
                        total += Integer.parseInt(scoreValue);
                        count++;
                    }
                    if (ScoreDetail.isFailCount(param, detail)) {
                        countFail++;
                    }
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            } else {
                _avgBigDecimal = null;
            }
            if (0 < compCredit) {
                student._compCredit = compCredit;
            }
            if (0 < getCredit) {
                student._getCredit = getCredit;
            }
            _countFail = countFail;
        }

        /**
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final Integer replacemoto,
                final Param param
        ) {
            if (param._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    static class Form {

        public boolean print(final IPdf ipdf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
//            log.info("print1");
//            for (int i = 0; i < hrInfo._students.size(); i++) {
//                final Student student = (Student) hrInfo._students.get(i);
//                log.info(" " + student._hrClass + " " + student._attendNo + " : rank = " + student._rank999999);
//            }
            Collections.sort(hrInfo._students, new Student.StudentSorter(param));
//            log.info("sorted");
//            for (int i = 0; i < hrInfo._students.size(); i++) {
//                final Student student = (Student) hrInfo._students.get(i);
//                log.info(" " + student._hrClass + " " + student._attendNo + " : rank = " + student._rank999999);
//            }
            final List studentListList = getStudentListList(hrInfo._students, param._formMaxLine);
            for (int pi = 0; pi < studentListList.size(); pi++) {
                final List studentList = (List) studentListList.get(pi);

                final String formname = pi == studentListList.size() - 1 ? "KNJD617Q_J2.frm" : "KNJD617Q_J1.frm";

                if (Form1.print(ipdf, formname, param, hrInfo, studentList)) {
                    hasData = true;
                }
            }
            return hasData;
        }

        private static String sishaGonyu(final BigDecimal bd) {
            return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static int currentColumn(final List lineList) {
            int max = 0;
            for (final Iterator it = lineList.iterator(); it.hasNext();) {
                final List line = (List) it.next();
                max = Math.max(max, line.size());
            }
            return max;
        }

        private static List setSameSize(final List list, final int max) {
            for (int i = list.size(); i < max; i++) {
                list.add(null);
            }
            return list;
        }

        private static List getStudentListList(final List students, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            int page = 0;
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int idx = (null == current ? 0 : current.size()) + 1;
                final int cpage = idx / count + (idx % count == 0 ? 0 : 1); //student._gnum / count + (student._gnum % count != 0 ? 1 : 0);
                if (null == current || page < cpage) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(student);
                student._gnum = current.size();
                page = cpage;
            }
            return rtn;
        }

        private static List getPageList(final Collection list, final int count) {
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

        private static int getMS932ByteLength(final String s) {
            int len = 0;
            try {
                if (null != s) {
                    len = s.getBytes("MS932").length;
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return len;
        }

        private static String zeroToNull(final int num) {
            return num == 0 ? null : String.valueOf(num);
        }

        /**
         *
         * @param ipdf
         * @param field
         * @param line
         * @param data
         */
        private static void svfsetString1(final IPdf ipdf, final String field, final int line, final String pf, final int col, final String data) {
            ipdf.VrsOutn(field, line, data);
        }

        private static int gnumToLine(final Param param, final int gnum) {
            return param._formMaxLine == gnum ? param._formMaxLine : gnum % param._formMaxLine;
        }

        private static void printAttendInfo(final IPdf ipdf, final Param param, final AttendInfo attendInfo, final int line) {
            ipdf.VrsOutn("SUSPEND1", line, zeroToNull(attendInfo._suspend));      // 出停
            ipdf.VrsOutn("KIBIKI1", line, zeroToNull(attendInfo._mourning));      // 忌引
            ipdf.VrsOutn("ABROAD1", line, zeroToNull(attendInfo._transDays));        // 留学
            ipdf.VrsOutn("PRESENT1", line, zeroToNull(attendInfo._mLesson));      // 出席すべき日数
            ipdf.VrsOutn("ABSENCE1", line, zeroToNull(attendInfo._absent));       // 欠席日数
            ipdf.VrsOutn("ATTEND1", line, String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            ipdf.VrsOutn("LEAVE1", line, zeroToNull(attendInfo._early));        // 早退回数
            ipdf.VrsOutn("TOTAL_LATE1", line, zeroToNull(attendInfo._late));      // 遅刻回数
        }

        static class Form1 {

            private static boolean print(
                    final IPdf ipdf,
                    final String formname,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist
            ) {
                boolean hasData = false;

                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());

                final List subclassListList = getPageList(printSubclassList, param._formMaxColumn);

                for (int p = 0, pages = subclassListList.size(); p < pages; p++) {
                    final List subclassList = (List) subclassListList.get(p);

                    ipdf.VrSetForm(formname, 4);
                    ipdf.addRecordField(param._recordField);
                    printHeader(ipdf, param, hrInfo);
                    for (int i = 0; i < stulist.size(); i++) {
                        final Student student = (Student) stulist.get(i);
                        
                        printStudentName(ipdf, param, student);

                        if (p == pages - 1) {
                            printStudentTotal(ipdf, param, student);
                            printHrInfo(ipdf, param, hrInfo);
                        }

                        printRemark(ipdf, param, student);

                    }

                    for (int i = 0, size = subclassList.size(); i < size; i++) {

                        final SubClass subclass = (SubClass) subclassList.get(i);
                        log.debug("p=" + p + ", i=" + i + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        printSubclasses(ipdf, param, i + 1, subclass);

                        for (final Iterator it = stulist.iterator(); it.hasNext();) {
                            final Student student = (Student) it.next();
                            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                                printDetail(ipdf, param, detail, gnumToLine(param, student._gnum), i + 1);
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(ipdf, param, i + 1, subclass);

                        ipdf.VrEndRecord();
                        hasData = true;
                    }
                }
                return hasData;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param ipdf
             * @param hrInfo
             */
            private static void printHeader(
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                ipdf.VrsOut("ymd1", param.getNow()); // 作成日
                ipdf.VrsOut("DATE", param.getTermKekka());  // 欠課の集計範囲
                ipdf.VrsOut("DATE2", param.getTermKekka());
                if ("2".equals(param._gradeOrHr)) {
                    if (hrInfo._students.size() > 0) {
                        final Student student = (Student) hrInfo._students.get(0);
                        ipdf.VrsOut("HR_NAME", student._hrclassname1);  // 履修単位
                    }
                } else {
                    ipdf.VrsOut("HR_NAME", param._gradename1);  // 履修単位
                }
                if (param._hasCompCredit) {
                    ipdf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
                }
                ipdf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数
                ipdf.VrsOut("ITEM7", param._rankName + "順位");
                ipdf.VrsOut("ITEM4", param._item4Name);
                ipdf.VrsOut("ITEM5", param._item5Name);
                ipdf.VrsOut("ITEM6", param._item1Name + "・" + param._item2Name);
                ipdf.VrsOut("ITEM8", param._rankName);
                ipdf.VrsOut("ITEM9", "欠点科目数");
                ipdf.VrsOut("ITEM10", "欠点者数");

                // 一覧表枠外の文言
                ipdf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
                ipdf.VrsOut("NOTE1",  " " );
                ipdf.VrsOut("NOTE2",  "：欠課時数超過者" );
                ipdf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
                ipdf.VrsOut("NOTE3",  " " );
                ipdf.VrsOut("NOTE4",  " ：欠点" );

                ipdf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度　" + param._title);

                String name1;
                name1 = "校長";
                ipdf.VrsOut("JOB4" + (getMS932ByteLength(name1) > 4 ? "_2" : "_1"), name1);
                name1 = "副校長";
                ipdf.VrsOut("JOB3" + (getMS932ByteLength(name1) > 4 ? "_2" : "_1"), name1);
                name1 = "教務主任";
                ipdf.VrsOut("JOB2" + (getMS932ByteLength(name1) > 4 ? "_2" : "_1"), name1);
                name1 = "学年主任";
                ipdf.VrsOut("JOB1" + (getMS932ByteLength(name1) > 4 ? "_2" : "_1"), name1);
            }

            /**
             * 学級データの印字処理(学級平均)
             * @param ipdf
             */
            private static void printHrInfo(
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                final int col = -1;
                //学級合計
                String gavg = null;
                String gtotal = null;
                gavg = hrInfo._avgGradeAverage;
                gtotal = hrInfo._avgGradeTotal;
                if (null != gtotal) {
                    svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 3, "", col, gtotal); // 学年合計
                }
                if (null != gavg) {
                    svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 3, "", col, gavg); // 指定母集団平均
                }
                //欠点者数
                if (null != hrInfo._failTotal) {
                    svfsetString1(ipdf, "FAIL1", param._formMaxLine + 4, "", col, hrInfo._failTotal); // 欠点者数
                }
                //最高点
                if (null != hrInfo._maxTotal) {
                    svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 5, "", col, hrInfo._maxTotal); // 最高点
                }
                //最低点
                if (null != hrInfo._minTotal) {
                    svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 6, "", col, hrInfo._minTotal); // 最低点
                }
                //出席率・欠席
                if (null != hrInfo._perHrPresent) {
                    ipdf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrInfo._perHrPresent.setScale(1, BigDecimal.ROUND_HALF_UP)));
                }
                if (null != hrInfo._perHrAbsent) {
                    ipdf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrInfo._perHrAbsent.setScale(1, BigDecimal.ROUND_HALF_UP)));
                }
            }

            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param ipdf
             */
            private static void printStudentTotal(
                    final IPdf ipdf,
                    final Param param,
                    final Student student
            ) {
                final int line = gnumToLine(param, student._gnum);
                ipdf.VrsOutn("9SCORE_TOTAL", line, student._scoreSum999999);  //総合点
                ipdf.VrsOutn("9SCORE_AVERAGE", line, student._scoreAvg999999);  //平均点
                int rank9 = student._rank999999;
                int rank5 = student._rank555555;
                if ("2".equals(param._gradeOrHr)) {
                    rank9 = student._classRank999999;
                    rank5 = student._classRank555555;
                }
                if (1 <= rank9) {
                    ipdf.VrsOutn("9SCORE_RANK", line, String.valueOf(rank9));
                }
                ipdf.VrsOutn("59SCORE_TOTAL", line, student._scoreSum555555);  //総合点
                ipdf.VrsOutn("5SCORE_AVERAGE", line, student._scoreAvg555555);  //平均点
                if (1 <= rank5) {
                    ipdf.VrsOutn("5SCORE_RANK", line, String.valueOf(rank5));
                }

                printAttendInfo(ipdf, param, student._attendInfo, line);
            }

            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentName(
                    final IPdf ipdf,
                    final Param param,
                    final Student student
            ) {
                final int line = gnumToLine(param, student._gnum);
                if ("1".equals(param._gradeOrHr)) {
                    ipdf.VrsOutn("NUMBER", line, StringUtils.defaultString(student._hrclassname1) + student.getPrintAttendno());  // 出席番号
                } else {
                    ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                }
                // 学籍番号表記
                String nameNo = "1";
                if ("1".equals(param._use_SchregNo_hyoji)) {
                    ipdf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                    nameNo = "2" + (getMS932ByteLength(student._name) > 16 ? "_2" : "_1"); // 学籍番号表示用の氏名フィールド
                }
                ipdf.VrsOutn("NAME" + nameNo, line, student._name);    // 氏名
                ipdf.VrsOutn("INOUT", line, student._inoutcdcoursecodename);
                ipdf.VrsOutn("SEX", line, student._sexName);
            }

            private static void printRemark(final IPdf ipdf, final Param param, final Student student) {
                final int line = gnumToLine(param, student._gnum);
                final String remark = student.getRemark(param);
                ipdf.VrsOutn("REMARK1", line, remark);  // 備考
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param ipdf
             * @param subclass
             * @param line：科目の列番号
             * @param stulist：List hrInfo._studentsのsublist
             * @return
             */
            private static void printSubclasses(
                    final IPdf ipdf,
                    final Param param,
                    final int col,
                    final SubClass subclass
            ) {
                //教科名
                ipdf.setRecordString("course1", col, subclass._classabbv);
                //科目名
                final String subclassfield = "subject1";
                if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                ipdf.setRecordString(subclassfield, col, subclass._subclassabbv);
                if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_NORMAL); }
                //単位数
                ipdf.setRecordString("credit1", col, subclass.getPrintCredit());
                ipdf.setRecordString("CLASS_TEACHER", col, subclass._staffname);
                //授業時数
                ipdf.setRecordString("lesson1", col, (subclass._jisu <= 0 ? "" : String.valueOf(subclass._jisu)));
                //項目名
                ipdf.setRecordString("ITEM1", col, param._item1Name);
                ipdf.setRecordString("ITEM2", col, param._item2Name);
            }

            public static void printSubclassStat(final IPdf ipdf, final Param param, final int col, final SubClass subclass) {
                if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                    ipdf.setRecordString("TOTAL_SUBCLASS", col, StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                }
                ipdf.setRecordString("AVE_CLASS", col, subclass._scoreaverage);
                ipdf.setRecordString("AVE_SUBCLASS", col, subclass._scoresubaverage);
                ipdf.setRecordString("FAIL_STD", col, subclass._scoreFailCnt);
                ipdf.setRecordString("MAX_SCORE", col, subclass._scoreMax);
                ipdf.setRecordString("MIN_SCORE", col, subclass._scoreMin);
            }

            /**
             * 生徒別科目別素点・評定・欠課時数等を印刷します。
             * @param ipdf
             * @param line 生徒の行番
             */
            private static void printDetail(
                    final IPdf ipdf,
                    final Param param,
                    final ScoreDetail detail,
                    final int line,
                    final int col
            ) {
                final String scoreField = "SCORE";
                if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_KETTEN); }
                final String aster;
                if ("990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
                    aster = "*";
                } else {
                    aster = "";
                }
                final String printScore;
                if (null == detail._score) {
                    printScore = StringUtils.defaultString(detail._scoreDi);
                } else {
                    printScore = StringUtils.defaultString(detail._score);
                }
                ipdf.setRecordString(scoreField + String.valueOf(line) + "", col, aster + printScore);
                if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_NORMAL); }

                // 欠課
                if (null != detail._absent) {
                    final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                    final String field;
                    String pf = "";
                    if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                        field = "kekka2_";
                    } else {
                        field = "kekka";
                    }
                    if (0 != value) {
                        if (detail._isOver) { ipdf.VrAttribute(field + String.valueOf(line) + pf, ATTRIBUTE_KEKKAOVER); }
                        ipdf.setRecordString(field + String.valueOf(line) + pf, col, param.getAbsentFmt().format(detail._absent.floatValue()));
                        if (detail._isOver) { ipdf.VrAttribute(field + String.valueOf(line) + pf, ATTRIBUTE_NORMAL); }
                    }
                }
            }
        }
    }

    private static class TestItem {
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String toString() {
            return "TestItem(" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class SubclassMst {
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto) {
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;

        /** 学年 */
        final String _grade;
        /** 出欠集計日付 */
        final String _sdate;
        final String _date;
        final String _testKindCd;

        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
        final String _outputRank;
        /** 順位の基準点 1:総合点 2:平均点 */
        final String _outputKijun;
        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;

        final String _cmd;
        final int _ketten;

        /** フォーム（1:４５名、2:５０名）*/
        int _formMaxLine;

        /** 科目数　（1:15科目、2:20科目) */
        int _formMaxColumn;
        final String _documentroot;
        final String _keninwakuImagePath;
        private String[] _recordField;
        private String FORM_FILE;

        private String _semesterName;
        private TestItem _testItem;

        private static final String FROM_TO_MARK = "\uFF5E";

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        private KNJSchoolMst _knjSchoolMst;

        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String _gradeOrHr;
        final String _gradename1;
        final String[] _classSelected;

        private KNJ_Get_Info _getinfo;
        private String _rankName;
        private String _rankFieldName;
        private String _avgDiv;
        private Map _d055Name1Map;
        private Map _subclassMst;
        private final String SUBCLASSCD999999 = "999999";
        private final String SUBCLASSCD555555 = "555555";

        private String _title;
        private String _mark;
        private String _item1Name;  // 明細項目名
        private String _item2Name;  // 明細項目名
        private String _item4Name;  // 総合点欄項目名
        private String _item5Name;  // 平均点欄項目名
        private String _form2Item4Name;  // 平均点欄項目名
        private String _form2Item5Name;  // 平均点欄項目名
        private boolean _creditDrop;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
        private boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        private boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
        private boolean _printPrgid;
        final Map _psMap = new HashMap();

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _outputRank = request.getParameter("OUTPUT_RANK");
            _creditDrop = (request.getParameter("OUTPUT4") != null);
            _cmd = request.getParameter("cmd");
            _recordField = new String[] {};
            _formMaxLine = 80;
            _formMaxColumn = 12;
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _prgId = request.getParameter("PRGID");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _documentroot = request.getParameter("DOCUMENTROOT");
            if (NumberUtils.isDigits(request.getParameter("KETTEN"))) {
                _ketten = Integer.parseInt(request.getParameter("KETTEN"));
            } else {
                _ketten = -1;
            }
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _gradeOrHr = request.getParameter("GRADE_OR_HR");
            _classSelected = null == request.getParameter("CLASS_SELECTED") ? new String[] {} : request.getParameterValues("CLASS_SELECTED");
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));
            _gradename1 = getSchregRegdGdat(db2, "GRADE_NAME1");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
            _attendParamMap.put("sdate", _sdate);

            setD055Name1(db2);
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = StringUtils.defaultString(returnval.val1);  //学期名称
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            final String scoreDiv = _testKindCd.substring(4);
            if (SCORE_DIV_01.equals(scoreDiv) || SCORE_DIV_02.equals(scoreDiv)) {
                _isGakunenMatu = false;
                _hasCompCredit = false;
                _enablePringFlg = false;
                _isPrintDetailTani = false;
                _failValue = 30;
                _printPrgid = true;

                if (SCORE_DIV_02.equals(scoreDiv)) {
                    _item1Name = "平常点";
                } else {
                    _item1Name = "素点";
                }
                _item2Name = "欠課";
                _item4Name = "総合点";
                _item5Name = "平均点";
                _form2Item4Name = _item4Name;
                _form2Item5Name = _item5Name;
                _creditDrop = false;
                _title = _semesterName + " " + _testItem._testitemname + " 成績順一覧表";

            } else if (SCORE_DIV_08.equals(scoreDiv)) {
                _failValue = 30;
                _printPrgid = true;

                _enablePringFlg = false;
                _isPrintDetailTani = true;
                _item1Name = "評価";
                _item2Name = "欠課";
                _item4Name = "評価総合点";
                _item5Name = "評価平均点";
                _form2Item4Name = "評価合計";
                _form2Item5Name = "評価平均";
                _isGakunenMatu = false;
                _hasCompCredit = false;
                _title = _semesterName + " " + _testItem._testitemname + " 成績順一覧表";

            } else if (SCORE_DIV_09.equals(scoreDiv)) {
                _failValue = 30;
                _printPrgid = true;

                _item1Name = "評定";
                _item2Name = "欠課";
                _item4Name = "評定総合点";
                _item5Name = "評定平均点";
                _form2Item4Name = "評定合計";
                _form2Item5Name = "評定平均";
                _isGakunenMatu = true;
                _hasCompCredit = true;
                _enablePringFlg = false;
                _isPrintDetailTani = true;
                _title = _testItem._testitemname + "  成績順一覧表（評定）";
            }

            _rankName = "学年";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                _rankFieldName = "MAJOR_RANK";
                _avgDiv = "4";
            } else {
                _rankFieldName = "GRADE_RANK";
                _avgDiv = "1";
            }
            log.debug("順位名称=" + _rankName);
            setSubclassMst(db2);
            String path = null;
            final File file = new File(_documentroot + "/image/KNJD615_keninwaku2.jpg");
            if (file.exists()) {
                path = file.getPath();
            } else {
                log.warn("file not found:" + file.getPath());
            }
            _keninwakuImagePath = path;
            log.info(" keninwaku path = " + _keninwakuImagePath);
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        " + field + " ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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

        /** 欠課の集計範囲 */
        public String getTermKekka() {
            return KNJ_EditDate.h_format_JP(_sdate) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(_date);
        }

        /** 「出欠の記録」の集計範囲 */
        public String getTermAttend() {
            return KNJ_EditDate.h_format_JP(_sdate) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(_date);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
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

        private TestItem getTestItem(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            TestItem testitem = new TestItem();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + year + "'" +
                        " AND SEMESTER = '" + semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                            " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitem;
        }

        private String getAttendRemarkMonth(final DB2UDB db2, final String date) {
            String rtn = "99";
            if (null == date) {
                return rtn;
            }
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            int month = cal.get(Calendar.MONTH) + 1;
            if (month < 4) {
                month += 12;
            }
            return String.valueOf(month);
        }

        private void setD055Name1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d055Name1Map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM V_NAME_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _d055Name1Map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.debug("getD055 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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
            } catch (Exception ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, false, false);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON ";
                if ("1".equals(_useCurriculumcd)) {
                    sql += " T2.CLASSCD = T1.CLASSCD ";
                    sql += " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                } else {
                    sql += " T2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), isSaki, isMoto);
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

}

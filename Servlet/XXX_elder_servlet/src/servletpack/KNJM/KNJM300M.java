/*
 * $Id: f63023e527ddb05f917361f2cef72891cfffac1d $
 *
 * 作成日: 2012/12/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * レポート提出状況（個人）
 */
public class KNJM300M {

    private static final Log log = LogFactory.getLog(KNJM300M.class);
    
    // 文字化け対策
    private static String M1C = "M1";
    private static String M2C = "M2";
    private static String M3C = "M3";
    private static String M4C = "M4";
    private static String M5C = "M5";
    private static String M6C = "M6";
    private static String M7C = "M7";
    private static String M8C = "M8";

    private static String M1 = "◎";
    private static String M2 = "○";
    private static String M3 = "●";
    private static String M4 = "△";
    private static String M5 = "▲";
    private static String M6 = "×";
    private static String M7 = "受";
    private static String M8 = "受*";
    
    private static Map markMap = new HashMap();
    private static String mark = "";
    static {
        markMap.put(M1C, M1);
        markMap.put(M2C, M2);
        markMap.put(M3C, M3);
        markMap.put(M4C, M4);
        markMap.put(M5C, M5);
        markMap.put(M6C, M6);
        markMap.put(M7C, M7);
        markMap.put(M8C, M8);
        mark = M1 + ":良好 " + M2 + ":遅れ " +  M3 + ":再提出合格 " + M4 + M5 + ":返送中 " + M6 + ":未提出";
    }
    
    private static DecimalFormat zero = new DecimalFormat("00");
    private static DecimalFormat space = new DecimalFormat("##");

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

        final List studentList = getStudentList(db2);

        final int maxLine = 60;
        
        int totalPage = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            totalPage += student._repPresentDatList.size() / maxLine + (student._repPresentDatList.size() % maxLine == 0 ? 0 : 1);
        }
        
        final int SIMEKIRI     = 1; // 締切
        final int HYOUKA_KIGOU = 2; // 評価記号
        final int HYOUKA       = 3; // 評価
        final int UKETUKEBI    = 4; // 受付日
        final int HENSOUBI     = 5; // 返送日
        final int SAIUKETUKE   = 6; // 再受付
        final int SAIHENBI     = 7; // 再返日
        final int SAIUKETUKE2  = 8; // 再受付２
        final int SAIHENSOU2   = 9; // 再返送２
        final String dateStr = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
        
        int page = 0;
        for (int j = 0; j <  studentList.size(); j++) {
            final Student student = (Student) studentList.get(j);
            page += 1;
            
            svf.VrSetForm("KNJM300M.frm", 4);
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ
            svf.VrsOut("TITLE", "レポート提出状況（個人）"); // タイトル
            svf.VrsOut("MARK", mark); // マーク
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage)); // 総ページ数
			svf.VrsOut("DATE", dateStr); // 日付
            svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
            if ("1".equals(student._baseRemark1)) {
                svf.VrsOut("GRAD", "卒予"); // 卒業予定
            }
            svf.VrsOut("NAME", student._name); // 氏名
            
            for (int i = 0; i < student._repPresentDatList.size(); i++) {
                final RepPresentDat rpd = (RepPresentDat) student._repPresentDatList.get(i);
                
                final String suf = rpd._isLast ? "2" : "1";

                if (rpd._isPrintSubclassname) {
                    svf.VrsOut("SUBCLASS_CD" + suf, rpd._subclasscd); // 科目コード
                    int subclassnamelen = KNJ_EditEdit.getMS932ByteLength(rpd._subclassname);
                    svf.VrsOut("SUBCLASS_NAME" + suf + "_" + ((subclassnamelen > 20) ? "3" : (subclassnamelen > 14) ? "2" : "1"), rpd._subclassname); // 科目名
                }

                svf.VrsOut("COUNT" + suf, String.valueOf(Integer.parseInt(rpd._standardSeq))); // 回数

                svf.VrsOutn("REP" + suf, SIMEKIRI, formatDate(rpd._standardDate)); // 締切
                svf.VrsOutn("REP" + suf, HYOUKA_KIGOU, rpd._mark); // 評価記号
                
                if (i % maxLine == 1 && i > 1) {
                    page += 1; 
                }
                svf.VrsOut("PAGE", String.valueOf(page)); // ページ

                for (int k = 0; k < rpd._representSeqList.size(); k++) {
                    final RepPresentDatRepresentSeq rpdrs = (RepPresentDatRepresentSeq) rpd._representSeqList.get(k);
                    if (k == rpd._representSeqList.size() - 1) {
                        svf.VrsOutn("REP" + suf, HYOUKA, rpdrs._gradValueName);
                    }
                    if (null != rpdrs._representSeq) {
                        if (0 == Integer.parseInt(rpdrs._representSeq)) {
                            svf.VrsOutn("REP" + suf, UKETUKEBI, formatDate(rpdrs._receiptDate));
                            svf.VrsOutn("REP" + suf, HENSOUBI, formatDate(rpdrs._gradDate));
                        } else if (1 == Integer.parseInt(rpdrs._representSeq)) {
                            svf.VrsOutn("REP" + suf, SAIUKETUKE, formatDate(rpdrs._receiptDate));
                            svf.VrsOutn("REP" + suf, SAIHENBI, formatDate(rpdrs._gradDate));
                        } else if (1 < Integer.parseInt(rpdrs._representSeq)) {
                            svf.VrsOutn("REP" + suf, SAIUKETUKE2, formatDate(rpdrs._receiptDate));
                            svf.VrsOutn("REP" + suf, SAIHENSOU2, formatDate(rpdrs._gradDate));
                        }
                    }
                }
                svf.VrEndRecord();
            }
            
            _hasData = true;
            final int currentLine = student._repPresentDatList.size() > 0 && student._repPresentDatList.size() % maxLine == 0 ? 0 : (student._repPresentDatList.size() % maxLine);  
            for (int i = currentLine; i < maxLine; i++) {
                svf.VrsOut("SUBCLASS_CD1", "\n");
                svf.VrEndRecord();
            }
        }
    }
    
    private String formatDate(final String date) {
        if (null == date) {
            return null;
        }
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8));
        return space.format(month) + "/" + zero.format(day);
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentlist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            Student student = null;
            RepPresentDat repPresentDat = null;
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (null == student || !student._schregno.equals(schregno)) {
                    student = new Student(schregno, rs.getString("NAME"), rs.getString("BASE_REMARK1"));
                    studentlist.add(student);
                    if (null != repPresentDat) {
                        repPresentDat._isLast = true;
                    }
                    repPresentDat = null;
                }
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String standardSeq = rs.getString("STANDARD_SEQ");
                final String maxStandardSeq = rs.getString("MAX_STANDARD_SEQ");
                final String subclassKey = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                final String key = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + standardSeq;
                if (null == repPresentDat || !key.equals(repPresentDat.getKey())) {
                    final String standardDate = rs.getString("STANDARD_DATE");
                    final String mark = (String) markMap.get(rs.getString("MARK"));
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    
                    boolean isNewSubclass = false;
                    if (null == repPresentDat || !repPresentDat.getSubclassKey().equals(subclassKey)) {
                        isNewSubclass = true;
                    }
                    if (null != repPresentDat && isNewSubclass) {
                        repPresentDat._isLast = true;
                    }
                    repPresentDat = new RepPresentDat(classcd, schoolKind, curriculumCd, subclasscd, standardSeq, maxStandardSeq, standardDate, mark, subclassname);
                    repPresentDat._isPrintSubclassname = isNewSubclass;
                    student._repPresentDatList.add(repPresentDat);
                }

                final String representSeq = rs.getString("REPRESENT_SEQ");
                final String receiptDate = rs.getString("RECEIPT_DATE");
                final String gradDate = rs.getString("GRAD_DATE");
                final String gradValue = rs.getString("GRAD_VALUE");
                final String gradValueName = rs.getString("GRAD_VALUE_NAME");
                repPresentDat._representSeqList.add(new RepPresentDatRepresentSeq(representSeq, receiptDate, gradDate, gradValue, gradValueName));
            }
            if (null != repPresentDat) {
                repPresentDat._isLast = true;
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return studentlist;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH REP_STANDARDDATE_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , COURSECD, MAJORCD, COURSECODE ");
        }
        stb.append("       , MAX(STANDARD_SEQ) AS MAX_STANDARD_SEQ ");
        stb.append("     FROM ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         REP_STANDARDDATE_COURSE_DAT ");
        } else {
            stb.append("         REP_STANDARDDATE_DAT ");
        }
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , COURSECD, MAJORCD, COURSECODE ");
        }

        stb.append(" ), REP_PRESENT_DAT_MIN_MAX_DATE AS ( ");
        stb.append("     SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, REPRESENT_SEQ, SCHREGNO,  ");
        stb.append("      MIN(RECEIPT_DATE) AS MIN_RECEIPT_DATE, MAX(RECEIPT_DATE) AS MAX_RECEIPT_DATE ");
        stb.append("     FROM REP_PRESENT_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, REPRESENT_SEQ, SCHREGNO ");
        stb.append(" ), REP_PRESENT_DAT_MAX_REPRESENT_SEQ AS ( ");
        stb.append("     SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, MAX(REPRESENT_SEQ) AS MAX_REPRESENT_SEQ, SCHREGNO ");
        stb.append("     FROM REP_PRESENT_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, SCHREGNO ");
        stb.append(" ), REP_PRESENT_DAT_MIN_MAX AS ( ");
        stb.append("     SELECT 'MIN' AS FLG, T1.* ");
        stb.append("     FROM REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN REP_PRESENT_DAT_MIN_MAX_DATE T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ AND T2.SCHREGNO = T1.SCHREGNO AND T2.MIN_RECEIPT_DATE = T1.RECEIPT_DATE ");
        stb.append("     WHERE ");
        stb.append("         T1.REPRESENT_SEQ = 0 ");
        stb.append("   UNION ALL ");
        stb.append("     SELECT 'MAX' AS FLG, T1.* ");
        stb.append("     FROM REP_PRESENT_DAT T1   ");
        stb.append("     INNER JOIN REP_PRESENT_DAT_MIN_MAX_DATE T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ AND T2.SCHREGNO = T1.SCHREGNO AND T2.MAX_RECEIPT_DATE = T1.RECEIPT_DATE ");
        stb.append("     INNER JOIN REP_PRESENT_DAT_MAX_REPRESENT_SEQ T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T3.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T3.MAX_REPRESENT_SEQ = T1.REPRESENT_SEQ AND T3.SCHREGNO = T1.SCHREGNO  ");
        stb.append(" ), SUBCLASS_STD_MAIN AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T3.COURSECD, T3.MAJORCD, T3.COURSECODE ");
        }
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append(" ), REPORT_MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T0.YEAR, T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T1.STANDARD_SEQ, T1.STANDARD_DATE, T0.SCHREGNO, ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        }
        stb.append("         T2.MAX_STANDARD_SEQ, ");
        stb.append("         (CASE WHEN T3.RECEIPT_DATE <= T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M1C + "' ");
        stb.append("               WHEN T3.RECEIPT_DATE >  T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M2C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND VALUE(T52.NAMESPARE1, '') = '1' THEN '" + M3C + "' ");
        stb.append("               WHEN VALUE(T3.GRAD_VALUE, '') = '1' AND T5.REPRESENT_SEQ  = 0 AND VALUE(T5.GRAD_VALUE, '') = '1' THEN '" + M4C + "' ");
        stb.append("               WHEN VALUE(T3.GRAD_VALUE, '') = '1' AND T5.REPRESENT_SEQ >= 1 AND VALUE(T5.GRAD_VALUE, '') = '1' THEN '" + M5C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ =  0 AND VALUE(T5.GRAD_VALUE, '') = '' THEN '" + M7C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND VALUE(T5.GRAD_VALUE, '') = '' THEN '" + M8C + "' ");
        stb.append("               WHEN '" + _param._loginDate + "' > T1.STANDARD_DATE AND T3.RECEIPT_DATE IS NULL THEN '" + M6C + "' ");
        stb.append("          END) AS MARK ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_MAIN T0 ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN REP_STANDARDDATE_COURSE_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("             AND T1.CLASSCD = T0.CLASSCD AND T1.SCHOOL_KIND = T0.SCHOOL_KIND AND T1.CURRICULUM_CD = T0.CURRICULUM_CD AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("             AND T1.COURSECD = T0.COURSECD AND T1.MAJORCD = T0.MAJORCD AND T1.COURSECODE = T0.COURSECODE ");
        } else {
            stb.append("         INNER JOIN REP_STANDARDDATE_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("             AND T1.CLASSCD = T0.CLASSCD AND T1.SCHOOL_KIND = T0.SCHOOL_KIND AND T1.CURRICULUM_CD = T0.CURRICULUM_CD AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
        }
        stb.append("         INNER JOIN REP_STANDARDDATE_MAX T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("             AND T2.COURSECD = T1.COURSECD AND T2.MAJORCD = T1.MAJORCD AND T2.COURSECODE = T1.COURSECODE ");
        }
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T3.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T3.FLG = 'MIN' ");
        stb.append("         LEFT JOIN NAME_MST T32 ON T32.NAMECD1 = 'M003' ");
        stb.append("             AND T32.NAMECD2 = T3.GRAD_VALUE ");
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T5 ON T5.YEAR = T1.YEAR ");
        stb.append("             AND T5.CLASSCD = T1.CLASSCD AND T5.SCHOOL_KIND = T1.SCHOOL_KIND AND T5.CURRICULUM_CD = T1.CURRICULUM_CD AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T5.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T5.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T5.FLG = 'MAX' ");
        stb.append("             AND T5.SCHREGNO = T3.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST T52 ON T52.NAMECD1 = 'M003' ");
        stb.append("             AND T52.NAMECD2 = T5.GRAD_VALUE ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         VALUE(T6.SEMESTER, '1') AS SEMESTER, ");
        stb.append("         T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T2.STANDARD_SEQ, T2.STANDARD_DATE, T1.SCHREGNO, ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        }
        stb.append("         T2.MAX_STANDARD_SEQ, ");
        stb.append("         T2.MARK ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_MAIN T1 ");
        stb.append("         LEFT JOIN REPORT_MAIN T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T2.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("             AND T2.COURSECD = T1.COURSECD ");
            stb.append("             AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("             AND T2.COURSECODE = T1.COURSECODE ");
        }
        stb.append("         LEFT JOIN SEMESTER_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("             AND T6.SEMESTER <> '9' ");
        stb.append("             AND T2.STANDARD_DATE BETWEEN T6.SDATE AND T6.EDATE ");
        stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO, T1.MAX_STANDARD_SEQ, ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        }
        stb.append("                  T11.BASE_REMARK1, T12.NAME, T13.SUBCLASSNAME ");
        stb.append("     FROM ");
        stb.append("         MAIN T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T11 ON T11.YEAR = T1.YEAR ");
        stb.append("             AND T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T11.BASE_SEQ = '001' ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T12 ON T12.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST T13 ON T13.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T13.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T13.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T13.SUBCLASSCD = T1.SUBCLASSCD ");

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T0.SCHREGNO, T0.YEAR, T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T0.MAX_STANDARD_SEQ, T0.BASE_REMARK1, T0.NAME, T0.SUBCLASSNAME, ");
        stb.append("    T2.STANDARD_SEQ, T2.STANDARD_DATE, T1.MARK, ");
        stb.append("    T14.REPRESENT_SEQ, T14.RECEIPT_DATE, T14.GRAD_DATE, T14.GRAD_VALUE, T15.NAME1 AS GRAD_VALUE_NAME ");
        stb.append(" FROM SUBCLASS_SCHREGNO T0 ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN REP_STANDARDDATE_COURSE_DAT T2 ON T2.YEAR = T0.YEAR ");
            stb.append("             AND T2.CLASSCD = T0.CLASSCD ");
            stb.append("             AND T2.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("             AND T2.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("             AND T2.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("             AND T2.COURSECD = T0.COURSECD ");
            stb.append("             AND T2.MAJORCD = T0.MAJORCD ");
            stb.append("             AND T2.COURSECODE = T0.COURSECODE ");
        } else {
            stb.append("         INNER JOIN REP_STANDARDDATE_DAT T2 ON T2.YEAR = T0.YEAR ");
            stb.append("             AND T2.CLASSCD = T0.CLASSCD ");
            stb.append("             AND T2.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("             AND T2.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("             AND T2.SUBCLASSCD = T0.SUBCLASSCD ");
        }
        stb.append("         LEFT JOIN MAIN T1 ON T1.YEAR = T0.YEAR ");
        stb.append("             AND T1.CLASSCD = T0.CLASSCD ");
        stb.append("             AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
        stb.append("             AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
        stb.append("             AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
        stb.append("             AND T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T1.STANDARD_SEQ = T2.STANDARD_SEQ ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("             AND T1.COURSECD = T0.COURSECD ");
            stb.append("             AND T1.MAJORCD = T0.MAJORCD ");
            stb.append("             AND T1.COURSECODE = T0.COURSECODE ");
        }
        stb.append("         LEFT JOIN REP_PRESENT_DAT T14 ON T14.YEAR = T1.YEAR ");
        stb.append("             AND T14.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T14.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T14.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T14.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T14.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T14.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST T15 ON T15.NAMECD1 = 'M003' ");
        stb.append("             AND T15.NAMECD2 = T14.GRAD_VALUE ");
        stb.append(" WHERE ");
        stb.append("     T0.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T0.SCHREGNO, T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T2.STANDARD_SEQ, ");
        stb.append("     T14.REPRESENT_SEQ, T14.RECEIPT_DATE ");
        return stb.toString();
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _baseRemark1;
        final List _repPresentDatList = new ArrayList();
        
        Student(final String schregno, final String name, final String baseRemark1) {
            _schregno = schregno;
            _name = name;
            _baseRemark1 = baseRemark1;
        }
    }
    
    
    private static class RepPresentDat {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _standardSeq;
        final String _maxStandardSeq;
        final String _standardDate;
        final String _mark;
        final String _subclassname;
        final List _representSeqList = new ArrayList();
        boolean _isLast;
        boolean _isPrintSubclassname;

        RepPresentDat(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String standardSeq,
                final String maxStandardSeq,
                final String standardDate,
                final String mark,
                final String subclassname
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _standardSeq = standardSeq;
            _maxStandardSeq = maxStandardSeq;
            _standardDate = standardDate;
            _mark = mark;
            _subclassname = subclassname;
        }
        
        String getSubclassKey() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }
        
        String getKey() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd + "-" + _standardSeq;
        }
    }
    
    private static class RepPresentDatRepresentSeq {
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;

        RepPresentDatRepresentSeq(
                final String representSeq,
                final String receiptDate,
                final String gradDate,
                final String gradValue,
                final String gradValueName
        ) {
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = gradValueName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74234 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gakki;
        private final String _gradeHrclass;
        private final String _loginDate;
        private final String[] _categorySelected;
        private final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
        }

    }
}

// eof


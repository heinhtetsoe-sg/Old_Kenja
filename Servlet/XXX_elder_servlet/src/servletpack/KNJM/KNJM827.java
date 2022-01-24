/*
 * $Id: e108b9ca86d7e8176f91d519d187c7696211803e $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
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
 * レポート提出状況一覧
 */
public class KNJM827 {

    private static final Log log = LogFactory.getLog(KNJM827.class);
    
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

        final List<RepPresentDat> printLineListAll = getPrintLineList(db2);

        final String title;
        if ("KNJM827W".equals(_param._prgid)) {
        	title = _param._semesterName + _param._testitemname + "受験許可一覧";
        } else {
        	title = _param._semesterName + "受験許可一覧";
        }
        
        final int maxLine = 64;
        final List<List<RepPresentDat>> pageList = getPageList(printLineListAll, maxLine);
        final String printDate = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
        
        final Set<String> logs = new HashSet<String>();
        
        for (int j = 0; j <  pageList.size(); j++) {
            svf.VrSetForm("KNJM827.frm", 4);
            svf.VrsOut("PAGE", String.valueOf(j + 1)); // ページ
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("MARK", mark); // マーク
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageList.size())); // 総ページ数
			svf.VrsOut("DATE", printDate); // 日付
            
            final List<RepPresentDat> repPresentDatList = pageList.get(j);
            
            String schregno = null;
            for (int i = 0; i < repPresentDatList.size(); i++) {
                final RepPresentDat printLine = repPresentDatList.get(i);
                
                final String suf = printLine._isdivLast ? "2" : "1";

                if (null == schregno || !schregno.equals(printLine._schregno)) {
                    if (null != printLine._name) {
                        int len = KNJ_EditEdit.getMS932ByteLength(printLine._name);
                        svf.VrsOut("NAME" + suf + "_" + ((len > 30) ? "4" : (len > 20) ? "3" : (len > 14) ? "2" : "1"), printLine._name); // 氏名
                        svf.VrsOut("SCHREG_NO" + suf, printLine._schregno); // 学籍番号
                    }
                    if ("1".equals(printLine._baseRemark1)) {
                        svf.VrsOut("GRAD_DIV" + suf, "卒予"); // 卒業予定
                    }
                }

                svf.VrsOut("SUBCLASS_CD" + suf, printLine._subclasscd); // 科目コード
                int subclassnamelen = KNJ_EditEdit.getMS932ByteLength(printLine._subclassname);
                svf.VrsOut("SUBCLASS_NAME" + suf + "_" + ((subclassnamelen > 20) ? "3" : (subclassnamelen > 14) ? "2" : "1"), printLine._subclassname); // 科目名
                svf.VrsOut("ALLOW" + suf, printLine._allowName); // 許可

                for (final RepPresentDatRepresentSeq rpdrs : printLine._repPresentDatSeqList) {
                    if (!NumberUtils.isDigits(rpdrs._standardSeq)) {
                        final String text = " standartSeq = " + rpdrs._standardSeq + ", " + printLine._subclasscd;
                        if (!logs.contains(text)) {
                        	log.warn(text);
                        	logs.add(text);
                        }
                    } else {
                        svf.VrsOutn("REP" + suf, Integer.parseInt(rpdrs._standardSeq), rpdrs._mark); // レポート
                    }
                }
                int paintStart = 0;
                if (!NumberUtils.isDigits(printLine._maxStandardSeq)) {
                	final String text = " maxStandardSeq = " + printLine._maxStandardSeq + ", " + printLine._subclasscd;
                	if (!logs.contains(text)) {
                		log.warn(text);
                		logs.add(text);
                	}
                } else {
                	paintStart = Integer.parseInt(printLine._maxStandardSeq) + 1;
                }
                for (int seq = paintStart; seq <= 12; seq++) {
                	svf.VrAttributen("REP" + suf, seq, "Paint=(0,70,2)"); // レポート提出部分でない箇所は網掛
                }
                svf.VrEndRecord();
                _hasData = true;
                schregno = printLine._schregno;
            }
            for (int i = repPresentDatList.size(); i < maxLine; i++) {
                svf.VrsOut("SCHREG_NO1", "\n");
                svf.VrEndRecord();
            }
        }
    }

    private List<List<RepPresentDat>> getPageList(final List<RepPresentDat> printLineList, final int maxLine) {
        final List<List<RepPresentDat>> pageList = new ArrayList();
        List<RepPresentDat> current = null;
        RepPresentDat old = null;
        for (final RepPresentDat printLine : printLineList) {
            if (null != old && !old._schregno.equals(printLine._schregno)) {
                old._isdivLast = true;
            }
            if (null == current || current.size() >= maxLine) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(printLine);
            old = printLine;
        }
        if (null != old) {
            old._isdivLast = true;
        }
        return pageList;
    }

    public List<RepPresentDat> getPrintLineList(final DB2UDB db2) {
        final List<RepPresentDat> list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            RepPresentDat rpd = null;
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String schregno = rs.getString("SCHREGNO");
                final String key = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + subclasscd + "-" + schregno;
                
                if (rpd == null || !rpd.getKey().equals(key)) {
                    final String maxStandardSeq = rs.getString("MAX_STANDARD_SEQ");
                    final String baseRemark1 = rs.getString("BASE_REMARK1");
                    final String name = rs.getString("NAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String allowName = rs.getString("ALLOW_NAME");
                    rpd = new RepPresentDat(year, classcd, schoolKind, curriculumCd, subclasscd, schregno, maxStandardSeq, allowName, baseRemark1, name, subclassname);
                    list.add(rpd);
                }

                final String standardSeq = rs.getString("STANDARD_SEQ");
                final String standardDate = rs.getString("STANDARD_DATE");
                final String mark = (String) markMap.get(rs.getString("MARK"));
                
                final RepPresentDatRepresentSeq dpdrs = new RepPresentDatRepresentSeq(standardSeq, standardDate, mark);
                rpd._repPresentDatSeqList.add(dpdrs);
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REP_STANDARDDATE_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         , COURSECD, MAJORCD, COURSECODE ");
        }
        stb.append("         , MAX(STANDARD_SEQ) AS MAX_STANDARD_SEQ ");
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
            stb.append("         , COURSECD, MAJORCD, COURSECODE ");
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
        stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T3.COURSECD ");
            stb.append("       , T3.MAJORCD ");
            stb.append("       , T3.COURSECODE ");
        }
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._gakki + "' ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         TGT.YEAR, TGT.CLASSCD, TGT.SCHOOL_KIND, TGT.CURRICULUM_CD, TGT.SUBCLASSCD, T1.STANDARD_SEQ, T1.STANDARD_DATE, TGT.SCHREGNO, T3.SCHREGNO AS REP_PRESENT_DAT_SCHREGNO, ");
        stb.append("         T2.MAX_STANDARD_SEQ, ");
        stb.append("         (CASE WHEN T3.SCHREGNO IS NULL THEN NULL ");
        stb.append("               WHEN T3.RECEIPT_DATE <= T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M1C + "' ");
        stb.append("               WHEN T3.RECEIPT_DATE >  T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M2C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND VALUE(T52.NAMESPARE1, '') = '1' THEN '" + M3C + "' ");
        stb.append("               WHEN '" + _param._loginDate + "' <= T1.STANDARD_DATE AND T3.GRAD_VALUE = '1' AND T5.REPRESENT_SEQ  = 0 AND T5.GRAD_VALUE = '1' THEN '" + M4C + "' ");
        stb.append("               WHEN '" + _param._loginDate + "' <= T1.STANDARD_DATE AND T3.GRAD_VALUE = '1' AND T5.REPRESENT_SEQ >= 1 AND T5.GRAD_VALUE = '1' THEN '" + M5C + "' ");
        stb.append("               WHEN '" + _param._loginDate + "' >  T1.STANDARD_DATE AND T5.GRAD_VALUE = '1' THEN '" + M6C + "' ");
        stb.append("               WHEN T3.GRAD_VALUE IS NULL THEN '" + M7C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND T5.GRAD_VALUE IS NULL THEN '" + M8C + "' ");
        stb.append("          END) AS MARK ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_SCHREGNO TGT ");
        stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TGT.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         LEFT JOIN REP_STANDARDDATE_COURSE_DAT T1 ON TGT.YEAR = T1.YEAR ");
            stb.append("             AND TGT.CLASSCD = T1.CLASSCD AND TGT.SCHOOL_KIND = T1.SCHOOL_KIND AND TGT.CURRICULUM_CD = T1.CURRICULUM_CD AND TGT.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND TGT.COURSECD = T1.COURSECD AND TGT.MAJORCD = T1.MAJORCD AND TGT.COURSECODE = T1.COURSECODE ");
        } else {
            stb.append("         LEFT JOIN REP_STANDARDDATE_DAT T1 ON TGT.YEAR = T1.YEAR ");
            stb.append("             AND TGT.CLASSCD = T1.CLASSCD AND TGT.SCHOOL_KIND = T1.SCHOOL_KIND AND TGT.CURRICULUM_CD = T1.CURRICULUM_CD AND TGT.SUBCLASSCD = T1.SUBCLASSCD ");
        }

        stb.append("         LEFT JOIN REP_STANDARDDATE_MAX T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("             AND T2.COURSECD = T1.COURSECD AND T2.MAJORCD = T1.MAJORCD AND T2.COURSECODE = T1.COURSECODE ");
        }
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T3.FLG = 'MIN' ");
        stb.append("             AND T3.SCHREGNO = TGT.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST T32 ON T32.NAMECD1 = 'M003' ");
        stb.append("             AND T32.NAMECD2 = T3.GRAD_VALUE ");
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T5 ON T5.YEAR = T1.YEAR ");
        stb.append("             AND T5.CLASSCD = T1.CLASSCD AND T5.SCHOOL_KIND = T1.SCHOOL_KIND AND T5.CURRICULUM_CD = T1.CURRICULUM_CD AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T5.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T5.FLG = 'MAX' ");
        stb.append("             AND T5.SCHREGNO = TGT.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST T52 ON T52.NAMECD1 = 'M003' ");
        stb.append("             AND T52.NAMECD2 = T5.GRAD_VALUE ");
        stb.append("         LEFT JOIN V_NAME_MST M015 ON M015.YEAR = '" + _param._year + "' AND M015.NAMECD1 = 'M015' ");
        stb.append("             AND M015.NAME1 = TGT.CLASSCD AND M015.NAME2 = TGT.CURRICULUM_CD AND M015.NAME3 = TGT.SUBCLASSCD AND M015.ABBV1 = TGT.SCHOOL_KIND ");
        stb.append("         LEFT JOIN V_NAME_MST M016 ON M016.YEAR = '" + _param._year + "' AND M016.NAMECD1 = 'M016' ");
        stb.append("             AND M016.NAME1 = TGT.CLASSCD AND M016.NAME2 = TGT.CURRICULUM_CD AND M016.NAME3 = TGT.SUBCLASSCD AND M016.ABBV1 = TGT.SCHOOL_KIND ");
        stb.append("     WHERE ");
        stb.append("         TGT.YEAR = '" + _param._year + "' ");
        stb.append("         AND VALUE(BASE.INOUTCD, '') <> '8' "); // 聴講生は対象外
        if ("1".equals(_param._gakki)) {
            stb.append("             AND M016.NAME1 IS NULL "); // 前期は後期科目を出力しない
        } else if ("2".equals(_param._gakki)) {
            stb.append("             AND M015.NAME1 IS NULL "); // 後期は前期科目を出力しない
        }
        stb.append(" ), SUBCLASS_STD_PASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, SCHREGNO ");
        stb.append("     FROM ");
        if ("KNJM827W".equals(_param._prgid)) {
        	stb.append("         SUBCLASS_STD_PASS_SDIV_DAT ");
        } else {
        	stb.append("         SUBCLASS_STD_PASS_DAT ");
        }
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._gakki + "' ");
        if ("KNJM827W".equals(_param._prgid)) {
        	stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testcd + "' ");
        }
        stb.append("     GROUP BY ");
        stb.append("         YEAR, SCHREGNO ");
        if ("1".equals(_param._output)) {
            stb.append("     HAVING ");
            stb.append("         MAX(SEM_PASS_FLG) IS NOT NULL ");
        } else if ("2".equals(_param._output)) {
            stb.append("     HAVING ");
            stb.append("         MAX(SEM_PASS_FLG) IS NULL ");
        } else if ("3".equals(_param._output)) {
            // 全てのデータが対象
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.STANDARD_SEQ, T1.STANDARD_DATE, T1.SCHREGNO, T1.REP_PRESENT_DAT_SCHREGNO, ");
        stb.append("    T1.MAX_STANDARD_SEQ, T1.MARK, ");
        stb.append("    T11.BASE_REMARK1, T12.NAME, T13.SUBCLASSNAME, ");
        stb.append("    (CASE WHEN T3.SEM_PASS_FLG IS NOT NULL AND T1.CLASSCD <> '90' THEN '許可' ELSE '／' END) AS ALLOW_NAME ");
        stb.append(" FROM MAIN T1 ");
        stb.append("         INNER JOIN SUBCLASS_STD_PASS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.YEAR = T1.YEAR ");
        if ("KNJM827W".equals(_param._prgid)) {
            stb.append("         LEFT JOIN SUBCLASS_STD_PASS_SDIV_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = '" + _param._gakki + "' ");
            stb.append("             AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + _param._testcd + "' ");
        } else {
            stb.append("         LEFT JOIN SUBCLASS_STD_PASS_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = '" + _param._gakki + "' ");
        }
        stb.append("             AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T11 ON T11.YEAR = T1.YEAR ");
        stb.append("             AND T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T11.BASE_SEQ = '001' ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST T12 ON T12.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SUBCLASS_MST T13 ON T13.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T13.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T13.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T13.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4) ASC, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        return stb.toString();
    }
    
    
    private static class RepPresentDat {
        final String _year;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _schregno;
        final String _maxStandardSeq;
        final String _baseRemark1;
        String _name;
        final String _subclassname;
        final String _allowName;
        boolean _isdivLast = false;
        
        final List<RepPresentDatRepresentSeq> _repPresentDatSeqList = new ArrayList();
        
        RepPresentDat(
                final String year,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String schregno,
                final String maxStandardSeq,
                final String allowName,
                final String baseRemark1,
                final String name,
                final String subclassname
        ) {
            _year = year;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _schregno = schregno;
            _maxStandardSeq = maxStandardSeq;
            _baseRemark1 = baseRemark1;
            _name = name;
            _subclassname = subclassname;
            _allowName = allowName;
        }
        
        String getKey() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd + "-" + _subclasscd + "-" + _schregno;
        }
    }
    
    private static class RepPresentDatRepresentSeq {
        final String _standardSeq;
        final String _standardDate;
        final String _mark;

        RepPresentDatRepresentSeq(
                final String standardSeq,
                final String standardDate,
                final String mark
        ) {
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _mark = mark;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77241 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gakki;
        private final String _prgid;
        private final String _testcd; // KNJM827W
        private final String _output;
        private final String _semesterName;
        private String _testitemname; // KNJM827W
        private final String _loginDate;
        private final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _prgid = request.getParameter("PRGID");
            _testcd = request.getParameter("TESTCD");
            _output = request.getParameter("OUTPUT");
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
            _semesterName = getSemesterName(db2);
            if (null != _testcd) {
            	_testitemname = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT VALUE(TESTITEMNAME, '') AS TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _gakki + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' "));
            }
        }

        public String getSemesterName(final DB2UDB db2) {
            String semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT VALUE(SEMESTERNAME, '') AS SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _gakki + "' "));
            return semesterName;
        }
    }
}

// eof


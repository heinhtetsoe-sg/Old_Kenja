/*
 * $Id: ab3403b103a92d79f0bdc811523c6c37cdd0b801 $
 *
 * 作成日: 2012/12/03
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * レポート提出状況一覧
 */
public class KNJM290W {

    private static final Log log = LogFactory.getLog(KNJM290W.class);

    // 文字化け対策
    private static String M1C = "M1";
    private static String M2C = "M2";
    private static String M3C = "M3";
    private static String M4C = "M4";
    private static String M5C = "M5";
    private static String M6C = "M6";
    private static String M7C = "M7";
    private static String M8C = "M8";
    private static String M9C = "M9";

    private static String M1 = "◎";
    private static String M2 = "○";
    private static String M3 = "●";
    private static String M4 = "△";
    private static String M5 = "▲";
    private static String M6 = "×";
    private static String M7 = "受";
    private static String M8 = "受*";
    private static String M9 = "添";

    private static Map<String, String> markMap = new HashMap();
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
        markMap.put(M9C, M9);
        mark = M1 + ":良好 " + M2 + ":遅れ " +  M3 + ":再提出合格 " + M4 + M5 + ":返送中 " + M6 + ":未提出";
    }

    private static String PASS_FLG_KYOKA = "1";
    private static String PASS_FLG_FUKYOKA = "2";
    private static String PASS_FLG_MIHANTEI = "3";

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
        boolean hasDifferentHr = false;
        if ("1".equals(_param._output)) {
            hasDifferentHr = hasDifferentHr(printLineListAll);
            // 複数HRの生徒を含む場合、ソートからHRを除く
            if (hasDifferentHr) {
                Collections.sort(printLineListAll, RepPresentDat.SCHREGNO_COMPARATOR);
            }
        }

        final String title = "レポート提出状況一覧" + ("1".equals(_param._output) ? "（クラス別）" : "2".equals(_param._output) ? "（科目別）" : "");

        final int maxLine = 64;
        final List<List<RepPresentDat>> pageList = getPageList(printLineListAll, maxLine);

        final String h_format_JP = KNJ_EditDate.h_format_JP(db2, _param._loginDate);

        for (int j = 0; j <  pageList.size(); j++) {
            svf.VrSetForm("KNJM290W_2.frm", 4);
            // svf.VrSetForm("2".equals(_param._output) ? "KNJM290M_2.frm" : "KNJM290M.frm", 4);
            svf.VrsOut("PAGE", String.valueOf(j + 1)); // ページ
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("MARK", mark); // マーク
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageList.size())); // 総ページ数
			svf.VrsOut("DATE", h_format_JP); // 日付

            final List<RepPresentDat> repPresentDatList = pageList.get(j);
            
            final List<String> pageSubclasscd = new ArrayList<String>();
            for (int i = 0; i < repPresentDatList.size(); i++) {
                final RepPresentDat printLine = repPresentDatList.get(i);
                if (null != printLine._subclasscd) {
                	final String subclasskey = printLine.getSubclassKey();
                	if (!pageSubclasscd.contains(subclasskey)) {
                		pageSubclasscd.add(subclasskey);
                	}
                }
            }
            log.info(" pageSubclasscd (size = " + pageSubclasscd.size() + ") = " + pageSubclasscd);
            for (int i = 0; i < pageSubclasscd.size(); i++) {
            	final String subclasscd = pageSubclasscd.get(i);
            	log.info(" test " + subclasscd + " = " + _param._subclasscdTestKindMapListMap.get(subclasscd));
            	if (i == 0) {
            		final List<Map<String, String>> testitems = _param._subclasscdTestKindMapListMap.get(subclasscd);
            		if (null != testitems) {
            			for (int ti = 0; ti < testitems.size(); ti++) {
            				svf.VrsOut("TESTITEMNAME" + String.valueOf(ti + 1), KnjDbUtils.getString(testitems.get(ti), "TESTITEMNAME"));
            				svf.VrsOut("TESTITEMNAME" + String.valueOf(ti + 1) + "_2", "受験許可");
            			}
            		}
            	}
            }

            String schregno = null;
            for (int i = 0; i < repPresentDatList.size(); i++) {
                final RepPresentDat printLine = repPresentDatList.get(i);

                if ("1".equals(_param._output)) {
                    if (hasDifferentHr) { // 複数HRの生徒を含む場合、担任名を表示しない
                    } else {
                        svf.VrsOut("TEACHER", printLine._staffname); // 担任
                    }
                }
                final String suf = printLine._isdivLast ? "2" : "1";

                if (// "2".equals(_param._output) || !"2".equals(_param._output) &&
                        (null == schregno || !schregno.equals(printLine._schregno))) {
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

                svf.VrsOut("CREDIT" + suf, printLine._credits); // 単位
                
        		final List<Map<String, String>> testitems = _param._subclasscdTestKindMapListMap.get(printLine.getSubclassKey());
        		if (null != testitems) {
        			for (int ti = 0; ti < testitems.size(); ti++) {
        				final Map<String, String> testitem = testitems.get(ti);
        				final String passFlg = printLine._testcdPassFlgMap.get(KnjDbUtils.getString(testitem, "TESTCD"));
        				svf.VrsOut("PERMIT" + String.valueOf(ti + 1) + "_" + suf, PASS_FLG_KYOKA.equals(passFlg) ? "許可" : PASS_FLG_FUKYOKA.equals(passFlg) ? "／" : PASS_FLG_MIHANTEI.equals(passFlg) ? "△" : null); // 受験許可
        			}
        		}
                if (NumberUtils.isNumber(printLine._jisu)) {
                    final String jisuStr;
                    final BigDecimal jisubd = new BigDecimal(printLine._jisu);
                    if (jisubd.setScale(0, BigDecimal.ROUND_DOWN).intValue() == jisubd.setScale(0, BigDecimal.ROUND_UP).intValue()) {
                        jisuStr = jisubd.setScale(0, BigDecimal.ROUND_DOWN).toString();
                    } else {
                        jisuStr = jisubd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    svf.VrsOut("SCHOOLING" + suf, jisuStr);
                }

                for (final RepPresentDatRepresentSeq rpdrs : printLine._repPresentDatSeqList) {
                    svf.VrsOutn("REP" + suf, Integer.parseInt(rpdrs._standardSeq), rpdrs._mark); // レポート
                }
                if (NumberUtils.isDigits(printLine._maxStandardSeq)) {
                    for (int seq = Integer.parseInt(printLine._maxStandardSeq) + 1; seq <= 12; seq++) {
                        svf.VrAttributen("REP" + suf, seq, "Paint=(0,70,2)"); // レポート提出部分でない箇所は網掛
                    }
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

    private boolean hasDifferentHr(final List<RepPresentDat> printLineList) {
        boolean hasDifferentHr = false;
        String oldGradeHrclass = null;
        for (final RepPresentDat printLine : printLineList) {
            final String gradeHrclass = printLine._grade + printLine._hrClass;
            if ("1".equals(_param._output) && null != oldGradeHrclass && !oldGradeHrclass.equals(gradeHrclass)) {
                hasDifferentHr = true;
                break;
            }
            oldGradeHrclass = gradeHrclass;
        }
        return hasDifferentHr;
    }

    private List<List<RepPresentDat>> getPageList(final List<RepPresentDat> printLineList, final int maxLine) {
        final List<List<RepPresentDat>> pageList = new ArrayList<List<RepPresentDat>>();
        List<RepPresentDat> current = null;
        RepPresentDat old = null;
        String oldSubclasskey = null;
        for (final RepPresentDat printLine : printLineList) {
            String subclasskey = printLine.getSubclassKey();
            boolean isNewList = false;
            if (null == current || current.size() >= maxLine) {
                isNewList = true;
            } else if ("2".equals(_param._output) && null != oldSubclasskey && !oldSubclasskey.equals(subclasskey)) {
                isNewList = true;
            }
            if ("1".equals(_param._output) && null != old && !old._schregno.equals(printLine._schregno)) {
                old._isdivLast = true;
            }
            if (isNewList) {
                current = new ArrayList<RepPresentDat>();
                pageList.add(current);
            }
            current.add(printLine);
            oldSubclasskey = subclasskey;
            old = printLine;
        }
        if (null != old) {
            old._isdivLast = true;
        }
        return pageList;
    }

    public List<RepPresentDat> getPrintLineList(final DB2UDB db2) {
        final List<RepPresentDat> list = new ArrayList<RepPresentDat>();
        final Map<String, RepPresentDat> map = new HashMap<String, RepPresentDat>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String schregno = rs.getString("SCHREGNO");
                final String key = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + schregno;

                if (!map.containsKey(key)) {
                    final String maxStandardSeq = rs.getString("MAX_STANDARD_SEQ");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String staffname = rs.getString("STAFFNAME");
                    final String baseRemark1 = rs.getString("BASE_REMARK1");
                    final String name = rs.getString("NAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");
                    final String jisu = rs.getString("JISU");
                    RepPresentDat rpd = new RepPresentDat(year, classcd, schoolKind, curriculumCd, subclasscd, schregno, maxStandardSeq, credits, grade, hrClass, staffname, baseRemark1, name, subclassname, jisu);
                    list.add(rpd);
                    map.put(rpd.getKey(), rpd);
                }
                
                final String standardSeq = rs.getString("STANDARD_SEQ");
                final String standardDate = rs.getString("STANDARD_DATE");
                final String mark = markMap.get(rs.getString("MARK"));

                if (null != standardSeq) {
                    final RepPresentDatRepresentSeq dpdrs = new RepPresentDatRepresentSeq(standardSeq, standardDate, mark);
                    map.get(key)._repPresentDatSeqList.add(dpdrs);
                }
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        
        final String passSql = passSql();
        for (final Map<String, String> row : KnjDbUtils.query(db2, passSql)) {
            final String classcd = KnjDbUtils.getString(row, "CLASSCD");
            final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final String key = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + schregno;
        	final RepPresentDat rpd = map.get(key);
        	if (null == rpd) {
        		continue;
        	}
        	rpd._testcdPassFlgMap.put(KnjDbUtils.getString(row, "TESTCD"), KnjDbUtils.getString(row, "PASS_FLG"));
        }
        
        return list;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SELECTED(CODE) AS ( ");
        String union = "";
        for (int i = 0; i < _param._categorySelected.length; i++) {
            stb.append(union);
            stb.append(" VALUES('" + _param._categorySelected[i] + "') ");
            union = " UNION ";
        }
        stb.append(" ), ");
        stb.append(" REP_STANDARDDATE_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , COURSECD, MAJORCD, COURSECODE ");
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
            stb.append("       , COURSECD, MAJORCD, COURSECODE ");
        }

        stb.append(" ), REP_PRESENT_DAT_MIN_MAX_DATE AS ( ");
        stb.append("     SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ, REPRESENT_SEQ, SCHREGNO,  ");
        stb.append("      MIN(RECEIPT_DATE) AS MIN_RECEIPT_DATE, MAX(RECEIPT_DATE) AS MAX_RECEIPT_DATE ");
        stb.append("     FROM REP_PRESENT_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = SCHREGNO) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD) ");
        }
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
        if ("1".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T1.SCHREGNO) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) ");
        }
        stb.append(" ), REPORT_MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T0.YEAR, T0.CLASSCD, T0.SCHOOL_KIND, T0.CURRICULUM_CD, T0.SUBCLASSCD, T1.STANDARD_SEQ, T1.STANDARD_DATE, T0.SCHREGNO, ");
        stb.append("         T2.MAX_STANDARD_SEQ, ");
        stb.append("         (CASE WHEN T3_MIN.RECEIPT_DATE <= T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M1C + "' ");
        stb.append("               WHEN T3_MIN.RECEIPT_DATE >  T1.STANDARD_DATE AND VALUE(T32.NAMESPARE1, '') = '1' THEN '" + M2C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND VALUE(T52.NAMESPARE1, '') = '1' THEN '" + M3C + "' ");
        stb.append("               WHEN VALUE(T3_MIN.GRAD_VALUE, '') = '1' AND T5.REPRESENT_SEQ  = 0 AND VALUE(T5.GRAD_VALUE, '') = '1' THEN '" + M4C + "' ");
        stb.append("               WHEN VALUE(T3_MIN.GRAD_VALUE, '') = '1' AND T5.REPRESENT_SEQ >= 1 AND VALUE(T5.GRAD_VALUE, '') = '1' THEN '" + M5C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ =  0 AND VALUE(T5.GRAD_VALUE, '') = '' THEN '" + M7C + "' ");
        stb.append("               WHEN T5.REPRESENT_SEQ >= 1 AND VALUE(T5.GRAD_VALUE, '') = '' THEN '" + M8C + "' ");
        stb.append("               WHEN '" + _param._loginDate + "' > T1.STANDARD_DATE AND T3_MIN.RECEIPT_DATE IS NULL THEN '" + M6C + "' ");
        stb.append("               WHEN VALUE(T3_MAX.GRAD_VALUE, '') = '8' THEN '" + M9C + "' ");
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

        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T3_MIN ON T3_MIN.YEAR = T1.YEAR ");
        stb.append("             AND T3_MIN.CLASSCD = T1.CLASSCD AND T3_MIN.SCHOOL_KIND = T1.SCHOOL_KIND AND T3_MIN.CURRICULUM_CD = T1.CURRICULUM_CD AND T3_MIN.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3_MIN.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T3_MIN.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T3_MIN.FLG = 'MIN' ");
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T3_MAX ON T3_MAX.YEAR = T1.YEAR ");
        stb.append("             AND T3_MAX.CLASSCD = T1.CLASSCD AND T3_MAX.SCHOOL_KIND = T1.SCHOOL_KIND AND T3_MAX.CURRICULUM_CD = T1.CURRICULUM_CD AND T3_MAX.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3_MAX.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T3_MAX.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T3_MAX.FLG = 'MAX' ");
        stb.append("         LEFT JOIN NAME_MST T32 ON T32.NAMECD1 = 'M003' ");
        stb.append("             AND T32.NAMECD2 = T3_MIN.GRAD_VALUE ");
        stb.append("         LEFT JOIN REP_PRESENT_DAT_MIN_MAX T5 ON T5.YEAR = T1.YEAR ");
        stb.append("             AND T5.CLASSCD = T1.CLASSCD AND T5.SCHOOL_KIND = T1.SCHOOL_KIND AND T5.CURRICULUM_CD = T1.CURRICULUM_CD AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T5.SCHREGNO = T0.SCHREGNO ");
        stb.append("             AND T5.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("             AND T5.FLG = 'MAX' ");
        stb.append("             AND T5.SCHREGNO = T3_MIN.SCHREGNO ");
        stb.append("         LEFT JOIN NAME_MST T52 ON T52.NAMECD1 = 'M003' ");
        stb.append("             AND T52.NAMECD2 = T5.GRAD_VALUE ");
        if ("1".equals(_param._output)) {
        	if (!"9".equals(_param._semester)) {
            	stb.append("      INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR  ");
            	stb.append("            AND N1.NAMECD1 = 'M002'  ");
            	stb.append("            AND N1.NAMECD2 = T1.REPORTDIV  ");
            	stb.append("            AND N1.NAMESPARE1 IS NOT NULL  ");
            	stb.append("            AND SUBSTR(N1.NAMESPARE1, 1, 1) = '" + _param._semester + "' ");
        	}
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append(" ), ATTEND_ALL AS ( ");
        stb.append("   SELECT  ");
        stb.append("       T1.YEAR,  ");
        stb.append("       T2.SEMESTER,  ");
        stb.append("       T3.CLASSCD,  ");
        stb.append("       T3.SCHOOL_KIND,  ");
        stb.append("       T3.CURRICULUM_CD,  ");
        stb.append("       T3.SUBCLASSCD,  ");
        stb.append("       T1.SCHREGNO,  ");
        stb.append("       T1.SCHOOLINGKINDCD,  ");
        stb.append("       T4.NAMESPARE1,  ");
        stb.append("       T1.EXECUTEDATE,  ");
        stb.append("       T1.PERIODCD,  ");
        stb.append("       T1.CREDIT_TIME, ");
        stb.append("       T5.SCH_SEQ_MIN ");
        stb.append("   FROM  ");
        stb.append("       SCH_ATTEND_DAT T1  ");
        stb.append("       INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
        stb.append("           AND T2.SEMESTER <> '9'  ");
        stb.append("           AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE  ");
        stb.append("       INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR  ");
        stb.append("           AND T3.SEMESTER = T2.SEMESTER  ");
        stb.append("           AND T3.CHAIRCD = T1.CHAIRCD  ");
        stb.append("       LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001'  ");
        stb.append("           AND T4.NAMECD2 = T1.SCHOOLINGKINDCD  ");
        stb.append("       LEFT JOIN CHAIR_CORRES_DAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("           AND T5.CHAIRCD = T3.CHAIRCD ");
        stb.append("   WHERE  ");
        stb.append("       T1.YEAR = '" + _param._year + "'  ");
        stb.append("       AND T1.EXECUTEDATE <= '" + _param._loginDate + "' ");
        if ("1".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T1.SCHREGNO) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     AND EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD) ");
        }
        stb.append(" ), ATTEND_KIND1 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         1 AS KIND, ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         COUNT(EXECUTEDATE) AS JISU1, ");
        stb.append("         COUNT(EXECUTEDATE) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ");
        stb.append(" ), ATTEND_KIND2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         2 AS KIND, ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, ");
        stb.append("         SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10 AS LIMIT, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         VALUE(INT(MIN(SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10, SUM(CREDIT_TIME))), 0) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'M020' AND L1.NAMECD2 = '01'");
        stb.append("     WHERE SCHOOLINGKINDCD = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, L1.NAME1 ");
        stb.append(" ), ATTEND_KIND3 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         3 AS KIND, ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD <> '1' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ");
        stb.append(" ), ATTEND_KIND13 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("          MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("          SUM(JISU1) AS JISU1, ");
        stb.append("          SUM(JISU2) AS JISU2 ");
        stb.append("     FROM (");
        stb.append("       SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, JISU1, JISU2 ");
        stb.append("       FROM ");
        stb.append("         ATTEND_KIND1 ");
        stb.append("       UNION ALL ");
        stb.append("       SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, JISU1, JISU2 ");
        stb.append("       FROM ");
        stb.append("         ATTEND_KIND3 ");
        stb.append("      ) ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ");
        stb.append(" ), ADD1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO, ");
        stb.append("         T1.SCH_SEQ_MIN, ");
        stb.append("         L1.JISU2 AS NOT_HOUSOU, ");
        stb.append("         L2.JISU2 AS HOUSOU, ");
        // 最低出席回数 - スクーリング数（スクーリング種別2以外）の合計 = 不足のスクーリング数 (マイナスの場合は0とする)
        //  不足のスクーリング数 >= 放送で認められる数 の場合は放送で認められる数を加算する。
        //  不足のスクーリング数 <  放送で認められる数 の場合は不足のスクーリング数を加算する。
        stb.append("         MAX(0, T1.SCH_SEQ_MIN - VALUE(L1.JISU2, 0)) AS FUSOKU_SCHOOLING, ");
        stb.append("         CASE WHEN MAX(0, T1.SCH_SEQ_MIN - VALUE(L1.JISU2, 0)) >= VALUE(L2.JISU2, 0) THEN VALUE(L2.JISU2, 0) ");
        stb.append("              WHEN MAX(0, T1.SCH_SEQ_MIN - VALUE(L1.JISU2, 0)) <  VALUE(L2.JISU2, 0) THEN MAX(0, T1.SCH_SEQ_MIN - VALUE(L1.JISU2, 0)) ");
        stb.append("         END AS JISU2 ");
        stb.append("     FROM ");
        stb.append("     (SELECT DISTINCT ");
        stb.append("         YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN ");
        stb.append("     FROM ");
        stb.append("         ATTEND_ALL ");
        stb.append("     ) T1 ");
        stb.append("     LEFT JOIN ATTEND_KIND13 L1 ON L1.YEAR = T1.YEAR ");
        stb.append("         AND L1.CLASSCD = T1.CLASSCD ");
        stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L1.SCH_SEQ_MIN = T1.SCH_SEQ_MIN ");
        stb.append("     LEFT JOIN ATTEND_KIND2 L2 ON L2.YEAR = T1.YEAR ");
        stb.append("         AND L2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L2.SCH_SEQ_MIN = T1.SCH_SEQ_MIN ");
        stb.append(" ), SUBCLASS_ATTEND AS ( ");
        stb.append("     SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, JISU2 ");
        stb.append("     FROM ATTEND_KIND13 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, JISU2 ");
        stb.append("     FROM ADD1 ");
        stb.append(" ), SUBCLASS_ATTEND_SUM AS ( ");
        stb.append(" SELECT ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO, ");
        stb.append("         SUM(T1.JISU2) AS JISU ");
        stb.append("     FROM SUBCLASS_ATTEND T1 ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         VALUE(T6.SEMESTER, '1') AS SEMESTER, ");
        stb.append("         T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T2.STANDARD_SEQ, T2.STANDARD_DATE, T1.SCHREGNO, ");
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
        stb.append("         LEFT JOIN SEMESTER_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("             AND T6.SEMESTER <> '9' ");
        stb.append("             AND T2.STANDARD_DATE BETWEEN T6.SDATE AND T6.EDATE ");
        if ("1".equals(_param._output) && !"9".equals(_param._semester)) {
            stb.append(" WHERE ");
            stb.append("     T2.YEAR IS NOT NULL ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.STANDARD_SEQ, T1.STANDARD_DATE, T1.SCHREGNO, ");
        stb.append("    T1.MAX_STANDARD_SEQ, T1.MARK, ");
        stb.append("    T8.CREDITS, ");
        stb.append("    T7.GRADE, T7.HR_CLASS, T10.STAFFNAME, T11.BASE_REMARK1, T12.NAME, T13.SUBCLASSNAME, ");
        stb.append("    T14.JISU ");
        stb.append(" FROM MAIN T1 ");
        if ("2".equals(_param._output)) {
            stb.append("         LEFT JOIN SCHREG_REGD_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T7.YEAR = T1.YEAR ");
            stb.append("             AND T7.SEMESTER = T1.SEMESTER ");
        } else {
            stb.append("         LEFT JOIN SCHREG_REGD_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T7.YEAR = T1.YEAR ");
            if ("9".equals(_param._semester)) {
            	stb.append("             AND T7.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
            	stb.append("             AND T7.SEMESTER = '" + _param._semester + "' ");
            }
        }
        stb.append("         LEFT JOIN CREDIT_MST T8 ON T8.YEAR = T1.YEAR ");
        stb.append("             AND T8.COURSECD = T7.COURSECD ");
        stb.append("             AND T8.MAJORCD = T7.MAJORCD ");
        stb.append("             AND T8.GRADE = T7.GRADE ");
        stb.append("             AND T8.COURSECODE = T7.COURSECODE ");
        stb.append("             AND T8.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT T9 ON T9.YEAR = T7.YEAR ");
        stb.append("             AND T9.SEMESTER = T7.SEMESTER ");
        stb.append("             AND T9.GRADE = T7.GRADE ");
        stb.append("             AND T9.HR_CLASS = T7.HR_CLASS ");
        stb.append("         LEFT JOIN STAFF_MST T10 ON T10.STAFFCD = T9.TR_CD1 ");
        stb.append("         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T11 ON T11.YEAR = T1.YEAR ");
        stb.append("             AND T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T11.BASE_SEQ = '001' ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST T12 ON T12.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SUBCLASS_MST T13 ON T13.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T13.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T13.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T13.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN SUBCLASS_ATTEND_SUM T14 ON T14.YEAR = T1.YEAR ");
        stb.append("             AND T14.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T14.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T14.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T14.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T14.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._output)) {
            stb.append(" WHERE ");
            stb.append("     EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T1.SCHREGNO) ");
        } else if ("2".equals(_param._output)) {
            stb.append(" WHERE ");
            stb.append("     EXISTS (SELECT 'X' FROM SELECTED WHERE CODE = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) ");
        }
        if ("2".equals(_param._output)) {
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4) ASC, T1.STANDARD_SEQ ");
        } else {
            stb.append(" ORDER BY ");
            stb.append("     T7.GRADE, T7.HR_CLASS, SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4) ASC, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.STANDARD_SEQ ");
        }
        
        return stb.toString();
    }
    
    private String passSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SELECTED(CODE) AS ( ");
        String union = "";
        for (int i = 0; i < _param._categorySelected.length; i++) {
            stb.append(union);
            stb.append(" VALUES('" + _param._categorySelected[i] + "') ");
            union = " UNION ";
        }

        stb.append(" ), SUBCLASS_STD_MAIN AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._output)) {
            stb.append("     AND T1.SCHREGNO IN (SELECT CODE FROM SELECTED) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN (SELECT CODE FROM SELECTED) ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO, ");
    	stb.append("    I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV AS TESTCD,  ");
        stb.append("    CASE WHEN PD.SCHREGNO IS NOT NULL THEN CASE WHEN PD.SEM_PASS_FLG IS NOT NULL THEN " + PASS_FLG_KYOKA + " ELSE " + PASS_FLG_FUKYOKA + " END ELSE " + PASS_FLG_MIHANTEI + " END AS PASS_FLG ");
        stb.append(" FROM SUBCLASS_STD_MAIN T1 ");
    	stb.append(" INNER JOIN REP_STANDARDDATE_DAT T2 ON T2.YEAR = T1.YEAR  ");
    	stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
    	stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append(" INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR  ");
    	stb.append("     AND N1.NAMECD1 = 'M002'  ");
    	stb.append("     AND N1.NAMECD2 = T2.REPORTDIV  ");
    	stb.append("     AND N1.NAMESPARE1 IS NOT NULL  ");
    	stb.append(" INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON T1.YEAR = I1.YEAR  ");
    	stb.append("     AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = N1.NAMESPARE1  ");
        stb.append(" INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT PD ON PD.YEAR = I1.YEAR ");
        stb.append("     AND PD.SEMESTER = I1.SEMESTER ");
        stb.append("     AND PD.TESTKINDCD = I1.TESTKINDCD ");
        stb.append("     AND PD.TESTITEMCD = I1.TESTITEMCD ");
        stb.append("     AND PD.SCORE_DIV = I1.SCORE_DIV ");
        stb.append("     AND PD.CLASSCD = T1.CLASSCD ");
        stb.append("     AND PD.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND PD.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND PD.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND PD.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        if ("1".equals(_param._output)) {
            stb.append("     T1.SCHREGNO IN (SELECT CODE FROM SELECTED) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN (SELECT CODE FROM SELECTED) ");
        }
        
        return stb.toString();
    }

    private static class RepPresentDat {

        // 学籍番号前4桁降順、後4桁昇順、科目コードでソート
        private static class SchregnoComparator implements Comparator<RepPresentDat> {
            public int compare(final RepPresentDat r1, final RepPresentDat r2) {
                int rtn;
                rtn = -1 * (r1._schregno.substring(0, 4).compareTo(r2._schregno.substring(0, 4))); // 学籍番号降順
                if (0 != rtn) return rtn;
                rtn = r1._schregno.substring(4).compareTo(r2._schregno.substring(4)); // 学籍番号昇順
                if (0 != rtn) return rtn;
                rtn = r1._subclasscd.compareTo(r2._subclasscd); // 科目コード
                return rtn;
            }
        }

        static final Comparator SCHREGNO_COMPARATOR = new SchregnoComparator();

        final String _year;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _schregno;
        final String _maxStandardSeq;
        final String _grade;
        final String _hrClass;
        String _staffname;
        final String _baseRemark1;
        String _name;
        final String _subclassname;
        final String _credits;
        final Map<String, String> _testcdPassFlgMap = new HashMap<String, String>();
        final String _jisu;
        boolean _isdivLast = false;

        final List<RepPresentDatRepresentSeq> _repPresentDatSeqList = new ArrayList<RepPresentDatRepresentSeq>();

        RepPresentDat(
                final String year,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String schregno,
                final String maxStandardSeq,
                final String credits,
                final String grade,
                final String hrClass,
                final String staffname,
                final String baseRemark1,
                final String name,
                final String subclassname,
                final String jisu
        ) {
            _year = year;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _schregno = schregno;
            _maxStandardSeq = maxStandardSeq;
            _grade = grade;
            _hrClass = hrClass;
            _staffname = staffname;
            _baseRemark1 = baseRemark1;
            _name = name;
            _subclassname = subclassname;
            _credits = credits;
            _jisu = jisu;
        }
        
        String getSubclassKey() {
        	return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }

        String getKey() {
            return getSubclassKey() + "-" + _schregno;
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
        log.fatal("$Revision: 77429 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _semester;
        private final String _output;
        private final String _loginDate;
        private final String[] _categorySelected;
        private final String _useRepStandarddateCourseDat;
        private final Map<String, List<Map<String, String>>> _subclasscdTestKindMapListMap = new TreeMap<String, List<Map<String, String>>>(); 

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _output = request.getParameter("OUTPUT");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            if ("1".equals(_output)) {
                _semester = request.getParameter("GAKKI");
            } else {
                _semester = null;
            }
            _categorySelected = request.getParameterValues("category_selected");
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
            setTestname(db2);
        }

        private void setTestname(final DB2UDB db2) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append("  SELECT  ");
        	stb.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        	stb.append("      I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV AS TESTCD,  ");
        	stb.append("      I1.TESTITEMNAME,  ");
        	stb.append("      I2.SEMESTER, ");
        	stb.append("      I2.SEMESTERNAME  ");
        	stb.append("  FROM  ");
            if ("1".equals(_useRepStandarddateCourseDat)) {
                stb.append("      REP_STANDARDDATE_COURSE_DAT T1 ");
            } else {
            	stb.append("      REP_STANDARDDATE_DAT T1  ");
            }
        	stb.append("      INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR  ");
        	stb.append("            AND N1.NAMECD1 = 'M002'  ");
        	stb.append("            AND N1.NAMECD2 = T1.REPORTDIV  ");
        	stb.append("            AND N1.NAMESPARE1 IS NOT NULL  ");
        	stb.append("      INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON T1.YEAR = I1.YEAR  ");
        	stb.append("            AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = N1.NAMESPARE1  ");
        	stb.append("      INNER JOIN SEMESTER_MST I2 ON I2.YEAR = I1.YEAR  ");
        	stb.append("            AND I2.SEMESTER = I1.SEMESTER  ");
        	stb.append("  WHERE  ");
        	stb.append("      T1.YEAR = '" + _year + "'  ");
        	if ("2".equals(_output)) {
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
            }
        	stb.append("  GROUP BY ");
        	stb.append("      T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        	stb.append("      I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV, ");
        	stb.append("      I1.TESTITEMNAME,  ");
        	stb.append("      I2.SEMESTER,  ");
        	stb.append("      I2.SEMESTERNAME  ");
        	stb.append("  ORDER BY ");
        	stb.append("      T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        	stb.append("      I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV ");

        	_subclasscdTestKindMapListMap.clear();
        	
        	for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
        		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
        		if (!_subclasscdTestKindMapListMap.containsKey(subclasscd)) {
        			_subclasscdTestKindMapListMap.put(subclasscd, new ArrayList<Map<String, String>>());
        		}
        		_subclasscdTestKindMapListMap.get(subclasscd).add(row);
        	}
        }
    }
}

// eof


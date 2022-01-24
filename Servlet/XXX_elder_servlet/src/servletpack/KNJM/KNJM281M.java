/*
 * $Id: 5a87bf6ae108ee0fb2ca047d4ec506607910824b $
 *
 * 作成日: 2012/12/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * レポートチェックリスト
 */
public class KNJM281M {

    private static final Log log = LogFactory.getLog(KNJM281M.class);
    
    private static DecimalFormat zero = new DecimalFormat("00");
    private static DecimalFormat space = new DecimalFormat("##");
    
    private static String HYOUKA_CD_SAI_TEISHUTSU = "1";
    private static String HYOUKA_CD_TENSAKU_NOMI = "8";
    private static String HYOUKA_UKE = "受";
    private static String HYOUKA_BATU = "×";

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
    
    private static int getMS932ByteLength(final String name) {
        int len = 0;
        if (null != name) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }
    
    private static String substringMS932(final String name, final int bytelength) {
        StringBuffer stb = new StringBuffer();
        if (null != name) {
            int maxlen = bytelength;
            try {
                for (int i = 0; i < name.length(); i++) {
                    final String sb = name.substring(i, i + 1);
                    maxlen -= sb.getBytes("MS932").length;
                    if (maxlen < 0) {
                        break;
                    }
                    stb.append(sb);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return stb.toString();
    }
    
    private static String subtract(final String name, final String sub) {
        if (null == name || null == sub || -1 == name.indexOf(sub)) {
            return null;
        }
        return name.substring(sub.length());
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8));
        return space.format(month) + "/" + zero.format(day);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int UKETUKEBI = 1;
        final int HENSOUBI = 2;
        final int HYOUKA = 3;
        
        svf.VrSetForm("KNJM281M.frm", 4);
        final List list = getSubclassList(db2);
        for (final Iterator stit = list.iterator(); stit.hasNext();) {
            final Subclass subclass = (Subclass) stit.next();
            final String title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + "　提出レポートチェックリスト";
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date) + "現在"); // 日付

            for (final Iterator it = subclass._studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                String remark = "";
                boolean isTarget = false; // 印刷対象か
                String nullOrBatuSeq = null;
                String tensakunomiSeq = null;
                for (int repi = 0; repi < student._reportList.size(); repi++) {
                    final Report report = (Report) student._reportList.get(repi);
                    if (!NumberUtils.isDigits(report._standardSeq)) {
                        continue;
                    }
                    final Report.Print print = report.getPrint(_param);
                    if (null != nullOrBatuSeq && ("1".equals(report._namespare1) || HYOUKA_UKE.equals(print._hyouka))) { // 不合格のあとに合格・受付のレポートがある
                        isTarget = true;
                        // log.fatal(" student = " + student._schregno + ", subclass = " + subclass._subclassname + ", nullOrBatuSeq = " + nullOrBatuSeq + ", thisseq = " + report._standardSeq + ", hyouka = " + print._hyouka + " , " + report._namespare1);
                        break;
                    }
                    if (!"1".equals(report._namespare1) || HYOUKA_UKE.equals(print._hyouka)) { // 不合格もしくは受付のレポートがある
                        nullOrBatuSeq = report._standardSeq;
                    }
                    if (null != tensakunomiSeq && ("1".equals(report._namespare1) || HYOUKA_UKE.equals(print._hyouka) || HYOUKA_CD_SAI_TEISHUTSU.equals(report._gradValue))) { // 添削のみのあとに合格・受付・再提出のレポートがある
                        isTarget = true;
                        // log.fatal(" student = " + student._schregno + ", subclass = " + subclass._subclassname + ", tensakunomiSeq = " + tensakunomiSeq + ", hyouka = " + print._hyouka + " , " + report._namespare1);
                        break;
                    }
                    if (HYOUKA_CD_TENSAKU_NOMI.equals(report._gradValue)) { // 添削のみがある
                        tensakunomiSeq = report._standardSeq;
                    }
                }
                if (!isTarget) {
                    continue;
                }

                svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
                
                printStudentname(svf, student);
                printSubclassname(svf, subclass);

                svf.VrsOutn("COUNT_NAME", UKETUKEBI, "受付日");
                svf.VrsOutn("COUNT_NAME", HENSOUBI, "返送日");
                svf.VrsOutn("COUNT_NAME", HYOUKA, "評価");
                
                for (int repi = 0; repi < student._reportList.size(); repi++) {
                    final Report report = (Report) student._reportList.get(repi);
                    if (!NumberUtils.isDigits(report._standardSeq)) {
                        continue;
                    }
                    final int i = Integer.parseInt(report._standardSeq);
                    final Report.Print print = report.getPrint(_param);
 
                    if (null != print._hensou)  { svf.VrsOutn("REP" + i, HENSOUBI, print._hensou); } // 返送日
                    if (null != print._uketuke) { svf.VrsOutn("REP" + i, UKETUKEBI, print._uketuke); } // 受付日
                    if (null != print._hyouka)  { svf.VrsOutn("REP" + i, HYOUKA, print._hyouka); } // 評価

                    if ("2".equals(report._semester)) { // レポートが後期の場合
                        // log.debug(" student = " + student._name + ", subclasscd = " + subclass._subclassname + ", repi = " + report._standardSeq + ", " + report._standardDate + ", " + student._zenkiBatsu);
                        if (student._zenkiBatsu != null) {
                            remark = "前期試験×";
                        }
                    }
                }

                if (!StringUtils.isBlank(remark)) {
                    final String[] token = KNJ_EditEdit.get_token(remark, 20, 5);
                    if (null != token) {
                        for (int i = 0; i < Math.min(5, token.length); i++) {
                            svf.VrsOut("REMARK" + (i + 1), token[i]); // 備考
                        }
                    }
                }
                _hasData = true;
                svf.VrEndRecord();
            }
        }
    }

    private void printStudentname(final Vrw32alp svf, final Student student) {
        final int name1len = 10 * 2;
        if (getMS932ByteLength(student._name) <= name1len) {
            svf.VrsOut("NAME1", student._name); // 科目名
        } else {
            int idx = student._name.indexOf(" ");
            if (-1 == idx) {
                idx = student._name.indexOf("　");
            }
            if (-1 == idx || 8 < idx) {
                svf.VrsOut("NAME2", substringMS932(student._name, name1len)); // 科目名
                svf.VrsOut("NAME3", subtract(student._name, substringMS932(student._name, name1len))); // 科目名
            } else {
                svf.VrsOut("NAME2", student._name.substring(0, idx)); // 科目名
                if (idx <= student._name.length()) {
                    svf.VrsOut("NAME3", student._name.substring(idx + 1)); // 科目名
                }
            }
        }
    }

    private void printSubclassname(final Vrw32alp svf, final Subclass subclass) {
        final String subclassname = subclass.getSubclassname();
        final int line1len = 9 * 2;
        if (getMS932ByteLength(subclassname) <= line1len) {
            svf.VrsOut("SUBCLASS_NAME1", subclassname); // 科目名
        } else {
            int idx = subclassname.indexOf(" ");
            if (-1 == idx) {
                idx = subclassname.indexOf("　");
            }
            if (-1 == idx || 8 < idx) {
                svf.VrsOut("SUBCLASS_NAME2", substringMS932(subclassname, line1len)); // 科目名
                svf.VrsOut("SUBCLASS_NAME3", subtract(subclassname, substringMS932(subclassname, line1len))); // 科目名
            } else {
                svf.VrsOut("SUBCLASS_NAME2", subclassname.substring(0, idx)); // 科目名
                if (idx <= subclassname.length()) {
                    svf.VrsOut("SUBCLASS_NAME3", subclassname.substring(idx + 1)); // 科目名
                }
            }
        }
    }
    
    private static String getDispNum(final BigDecimal bd) {
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getSubclassList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Subclass subclass = null;
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                if (null == subclass || !subclass.getSubclasscd().equals(classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd)) {
                    final String year = rs.getString("YEAR");
                    final String chaircd = rs.getString("CHAIRCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String repSeqAll = rs.getString("REP_SEQ_ALL");
                    final String schSeqAll = rs.getString("SCH_SEQ_ALL");
                    final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                    final String credits = rs.getString("CREDITS");
                    
                    subclass = new Subclass(year, chaircd, classcd, schoolKind, curriculumCd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin, credits);
                    list.add(subclass);
                }
                
                final String inoutcd = rs.getString("INOUTCD");
                final String name = rs.getString("NAME");
                final String zenkiBatsu = rs.getString("ZENKI_BATSU");
                
                final Student student = new Student(rs.getString("SCHREGNO"), inoutcd, name, zenkiBatsu);

//                log.debug(" subclass = " + subclass._subclasscd + " : " +  subclass._subclassname);
//                log.debug(" student  = " + student._schregno + " : " + student._name);
                subclass._studentList.add(student);
            }

        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }

       try {
           final String reportSql = getReportSql();
           log.debug(" report sql = " + reportSql);
           ps = db2.prepareStatement(reportSql);
           rs = ps.executeQuery();
           while (rs.next()) {
               final Subclass subclass = getSubclass(list, rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD"));
               if (null == subclass) {
                   continue;
               }
               final Student student = getStudent(subclass._studentList, rs.getString("SCHREGNO"));
               if (null == student) {
                   continue;
               }
//             log.debug(" student  = " + student._schregno + " : " + student._name);
               student._reportList.add(new Report(rs.getString("SEMESTER"), rs.getString("STANDARD_SEQ"), rs.getString("STANDARD_DATE"), rs.getString("REP_STANDARD_SEQ"), rs.getString("NAMESPARE1"), rs.getString("REPRESENT_SEQ"), rs.getString("RECEIPT_DATE"), rs.getString("GRAD_DATE"), rs.getString("GRAD_VALUE"), rs.getString("GRAD_VALUE_NAME")));
           }
      } catch (Exception ex) {
           log.fatal("exception!", ex);
      } finally {
           DbUtils.closeQuietly(null, ps, rs);
           db2.commit();
      }
      return list;
    }
   
    private Student getStudent(final List list, final String schregno) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private Subclass getSubclass(final List list, final String subclasscd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass.getSubclasscd().equals(subclasscd)) {
                return subclass;
            }
        }
        return null;
    }
    
    private String sql() {
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_SCHREGNO AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected)  + " ");
        stb.append(" ) ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T8.SUBCLASSNAME, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         T6.CREDITS, ");
        stb.append("         SUBT.SCHREGNO, ");
        stb.append("         T4.INOUTCD, ");
        stb.append("         T4.NAME, ");
        stb.append("         SUBSTR(SUBT.SCHREGNO, 1, 4), ");
        stb.append("         SUBSTR(SUBT.SCHREGNO, 5, 4), ");
        stb.append("         CASE WHEN (T9.SUBCLASSCD IS NOT NULL AND T10.SEM1_INTR_VALUE IS NULL OR T10.SEM1_INTR_VALUE < 40) AND T10.SEM1_TERM_VALUE IS NULL THEN 1 END AS ZENKI_BATSU ");
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN SUBCLASS_SCHREGNO SUBT ON SUBT.YEAR = T1.YEAR ");
        stb.append("         AND SUBT.CLASSCD = T1.CLASSCD ");
        stb.append("         AND SUBT.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND SUBT.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND SUBT.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = SUBT.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = SUBT.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST T8 ON T8.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_CHKFIN_DAT T9 ON T9.YEAR = T1.YEAR ");
        stb.append("          AND T9.SEMESTER = '1' "); // 前期
        stb.append("          AND T9.CLASSCD = T2.CLASSCD ");
        stb.append("          AND T9.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("          AND T9.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("          AND T9.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("          AND T9.TESTKINDCD = '99' ");
        stb.append("          AND T9.TESTITEMCD = '00' ");
        stb.append("          AND T9.RECORD_DIV = '2' ");
        stb.append("     LEFT JOIN RECORD_DAT T10 ON T10.YEAR = T9.YEAR ");
        stb.append("          AND T10.CLASSCD = T9.CLASSCD ");
        stb.append("          AND T10.SCHOOL_KIND = T9.SCHOOL_KIND ");
        stb.append("          AND T10.CURRICULUM_CD = T9.CURRICULUM_CD ");
        stb.append("          AND T10.SUBCLASSCD = T9.SUBCLASSCD ");
        stb.append("          AND T10.SCHREGNO = SUBT.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.SUBCLASSCD, T1.CURRICULUM_CD, ");
        stb.append("     SUBSTR(SUBT.SCHREGNO, 1, 4) DESC, ");
        stb.append("     SUBSTR(SUBT.SCHREGNO, 5, 4) ");
        return stb.toString();
    }

    private String getReportSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_REPRESENT_SEQ AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected)  + " ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), MAX_RECEIPT_DATE AS ( ");
        stb.append("     SELECT  ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ ");
        stb.append(" ), PRINT_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         T1.RECEIPT_DATE, ");
        stb.append("         T3.NAMESPARE1, ");
        stb.append("         T1.GRAD_DATE, ");
        stb.append("         T1.GRAD_VALUE, ");
        stb.append("         T3.ABBV1 AS GRAD_VALUE_NAME ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
        stb.append("     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
        stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
        stb.append("     WHERE ");
        stb.append("         T2.RECEIPT_DATE <= '" + _param._date + "' ");
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
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected)  + " ");
        stb.append(" ), MAIN AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.STANDARD_DATE, ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     FROM ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         REP_STANDARDDATE_COURSE_DAT T1 ");
        } else {
            stb.append("         REP_STANDARDDATE_DAT T1 ");
        }
        stb.append("     INNER JOIN SUBCLASS_SCHREGNO T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         AND T2.COURSECD = T1.COURSECD ");
            stb.append("         AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
        }
        stb.append(" ) ");
        stb.append("     SELECT ");
        stb.append("         T0.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T0.CLASSCD, ");
        stb.append("         T0.SCHOOL_KIND, ");
        stb.append("         T0.CURRICULUM_CD, ");
        stb.append("         T0.SUBCLASSCD, ");
        stb.append("         T0.STANDARD_SEQ, ");
        stb.append("         T0.STANDARD_DATE, ");
        stb.append("         T0.SCHREGNO, ");
        stb.append("         T1.STANDARD_SEQ AS REP_STANDARD_SEQ, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         T1.RECEIPT_DATE, ");
        stb.append("         T1.NAMESPARE1, ");
        stb.append("         T1.GRAD_DATE, ");
        stb.append("         T1.GRAD_VALUE, ");
        stb.append("         T1.GRAD_VALUE_NAME ");
        stb.append("     FROM ");
        stb.append("         MAIN T0 ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T0.YEAR ");
        stb.append("         AND T2.SEMESTER <> '9' ");
        stb.append("         AND T0.STANDARD_DATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("     LEFT JOIN PRINT_DATA T1 ON T1.YEAR = T0.YEAR ");
        stb.append("         AND T1.CLASSCD = T0.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
        stb.append("         AND T1.STANDARD_SEQ = T0.STANDARD_SEQ ");
        stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("     ORDER BY ");
        stb.append("         T0.CLASSCD, ");
        stb.append("         T0.SCHOOL_KIND, ");
        stb.append("         T0.SUBCLASSCD, ");
        stb.append("         T0.CURRICULUM_CD, ");
        stb.append("         T0.STANDARD_SEQ, ");
        stb.append("         T1.REPRESENT_SEQ ");
        return stb.toString();
    }
    
    private static class Student {
        final String _schregno;
        final String _inoutcd;
        final String _name;
        final String _zenkiBatsu;

        final List _reportList = new ArrayList();

        Student(
                final String schregno,
                final String inoutcd,
                final String name,
                final String zenkiBatsu
        ) {
            _schregno = schregno;
            _inoutcd = inoutcd;
            _name = name;
            _zenkiBatsu = zenkiBatsu;
        }
    }
    
    private static class Subclass {
        final String _year;
        final String _chaircd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        final String _credits;

        final List _studentList = new ArrayList();

        Subclass(
                final String year,
                final String chaircd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String repSeqAll,
                final String schSeqAll,
                final String schSeqMin,
                final String credits
        ) {
            _year = year;
            _chaircd = chaircd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
            _credits = credits;
        }

        public String getSubclasscd() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }

        public String getSubclassname() {
            return StringUtils.defaultString(_subclassname);
        }

    }
    
    private static class Report {
        final String _semester;
        final String _standardSeq; // REP_STANDARDDATE_DAT.STANDARD_SEQ
        final String _standardDate;
        final String _repStandardSeq; // REP_REPORT_DAT.STANDARD_SEQ
        final String _namespare1;
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;
        public Report(final String semester, String standardSeq, final String standardDate, final String repStandardSeq, final String namespare1, final String representSeq, final String receiptDate, final String gradDate, final String gradValue, final String gradValueName) {
            _semester = semester;
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _repStandardSeq = repStandardSeq;
            _namespare1 = namespare1;
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = null == gradValue ? HYOUKA_UKE : gradValueName;
        }
        
        public Print getPrint(final Param param) {
            final String aster = NumberUtils.isDigits(_representSeq) && Integer.parseInt(_representSeq) >= 1 ? "*" : "";
            final String uketuke;
            final String hyouka;
            String hensou = null;
            if (null == _repStandardSeq) { // レポートが未提出
                uketuke = formatDate(_receiptDate);
                hensou = aster;
                if (null == _standardDate) {
                    // log.debug(" standardSeq = " + _standardSeq + ":  _ standardDate = " + _standardDate + "");
                    hyouka = null;
                } else if (_standardDate.compareTo(param._date) < 0) { // 未提出 && 提出期限を超えた
                    // log.debug(" standardSeq = " + _standardSeq + ":  x standardDate = " + _standardDate + "");
                    hyouka = HYOUKA_BATU;
                } else { // 未提出 && 提出期限を超えていない
                    // log.debug(" standardSeq = " + _standardSeq + ":  n standardDate = " + _standardDate + "");
                    hyouka = null;
                }
            } else if (HYOUKA_CD_TENSAKU_NOMI.equals(_gradValue)) { // 評価 添削のみ
                // log.debug(" standardSeq = " + _standardSeq + ":  g gradValue = " + _gradValueName + "");
                uketuke = formatDate(_receiptDate);
                hensou = formatDate(_gradDate)+ aster;
                hyouka = _gradValueName;
            } else if (null == _gradValue ||
                        null != _gradDate && param._date.compareTo(_gradDate) < 0 
                    ) {
                // log.debug(" standardSeq = " + _standardSeq + ":  r receiiptDate = " + _receiptDate + "");
                uketuke = formatDate(_receiptDate);
                hensou = aster;
                hyouka = HYOUKA_UKE;
            } else {
                // log.debug(" standardSeq = " + _standardSeq + ":  g gradValue = " + _gradValueName + "");
                uketuke = formatDate(_receiptDate);
                hensou = formatDate(_gradDate)+ aster;
                hyouka = _gradValueName;
            }
            return new Print(uketuke, hensou, hyouka);
        }
        
        private static class Print {
            final String _uketuke;
            final String _hyouka;
            final String _hensou;
            public Print(final String uketuke, final String hensou, final String hyouka) {
                _uketuke = uketuke;
                _hensou = hensou;
                _hyouka = hyouka;
            }
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74219 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String[] _categorySelected;
        final String _date;
        final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("temp_year");
            _semester = request.getParameter("SEMESTER");
            _categorySelected = request.getParameterValues("subclassyear");
            _date = request.getParameter("DATE").replace('/', '-');
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
        }
    }
}

// eof


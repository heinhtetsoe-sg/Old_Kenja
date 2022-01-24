/*
 * $Id: e483ede823f346bd5572586606b4dcc2bd564ab6 $
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学習状況通知
 */
public class KNJM500M {

    private static final Log log = LogFactory.getLog(KNJM500M.class);

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

    private String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8));
        return space.format(month) + "/" + zero.format(day);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int TOUKOU = 1;
        final int HOUSOU = 2;
        final int HYOUKA = 3;
        final int HENSOUBI = 4;

        final List list = getStudentList(db2);
        for (final Iterator stit = list.iterator(); stit.hasNext();) {
            final Student student = (Student) stit.next();

            svf.VrSetForm("KNJM500M.frm", 4);
            final String title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + _param._schoolName1 + "　学習状況通知書";
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
            svf.VrsOut("NAME", student._name); // 氏名
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._kijun) + "現在"); // 日付

            svf.VrsOut("SP_TOTAL", StringUtils.defaultString(student._totalYearCreditTime, "0") + "／" + StringUtils.defaultString(student._baseRemark2Name1)); // 特別活動時数合計
            svf.VrsOut("LAST_CREDIT", StringUtils.defaultString(student._beforeYearCreditTime, "0")); // 前年度までの計
            svf.VrsOut("THIS_YEAR_TOTAL", StringUtils.defaultString(student._thisYearCreditTime, "0")); // 今年度の計

            for (int i = 1; i <= 6; i++) {
                final String remarkId = String.valueOf(i);
                final String remark = (String) _param._hreportRemarkTDat.get(remarkId);
                svf.VrsOut("TEXT" + remarkId, remark); // 文言
            }

            for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();

                final String subclassname = subclass.getSubclassname();
                final int line1len = 9 * 2;
                if (getMS932ByteLength(subclassname) <= line1len) {
                    svf.VrsOut("SUBCLASS_NAME2_1", subclassname); // 科目名
                } else {
                    int idx = subclassname.indexOf(" ");
                    if (-1 == idx) {
                        idx = subclassname.indexOf("　");
                    }
                    if (-1 == idx || 8 < idx) {
                        svf.VrsOut("SUBCLASS_NAME2_2", substringMS932(subclassname, line1len)); // 科目名
                        svf.VrsOut("SUBCLASS_NAME2_3", subtract(subclassname, substringMS932(subclassname, line1len))); // 科目名
                    } else {
                        svf.VrsOut("SUBCLASS_NAME2_2", subclassname.substring(0, idx)); // 科目名
                        if (idx <= subclassname.length()) {
                            svf.VrsOut("SUBCLASS_NAME2_3", subclassname.substring(idx + 1)); // 科目名
                        }
                    }
                }

                svf.VrsOut("CREDIT", subclass._credits);
                svf.VrsOut("SCH_REP_MARK1", "Ｓ"); // スクーリングorレポート
                svf.VrsOut("SCH_REP_MARK2", "Ｒ"); // スクーリングorレポート
                svf.VrsOut("REG_COUNT1", subclass._schSeqMin); // 規定数
                svf.VrsOut("REG_COUNT2", subclass._repSeqAll); // 規定数
                svf.VrsOut("COUNT1", subclass.getAttendCount(false)); // 出校回数
                svf.VrsOut("COUNT2", subclass.getAttendCount(true)); // 通信回数
                int count = 0;

                svf.VrsOutn("COUNT_NAME", TOUKOU, "登校");
                svf.VrsOutn("COUNT_NAME", HOUSOU, "放送");
                svf.VrsOutn("COUNT_NAME", HYOUKA, "評価");
                svf.VrsOutn("COUNT_NAME", HENSOUBI, "返送日");

                final List toukouList = subclass.getAttendList(false);
                for (int ati = 0; ati < toukouList.size(); ati++) {
                    final String date = (String) toukouList.get(ati);
                    svf.VrsOutn("REP" + (ati + 1), TOUKOU, formatDate(date)); // 登校
                }
                final List housouList = subclass.getAttendList(true);
                for (int ati = 0; ati < housouList.size(); ati++) {
                    final String date = (String) housouList.get(ati);
                    svf.VrsOutn("REP" + (ati + 1), HOUSOU, formatDate(date)); // 放送
                }

                // log.debug(" kijun = " + _param._kijun + " (" + subclass._subclasscd + " : " + subclass._subclassname + ")");
                for (int repi = 0; repi < subclass._reportList.size(); repi++) {
                    final Report report = (Report) subclass._reportList.get(repi);
                    if (!NumberUtils.isDigits(report._standardSeq)) {
                        continue;
                    }
                    final int i = Integer.parseInt(report._standardSeq);
                    final String aster = NumberUtils.isDigits(report._representSeq) && Integer.parseInt(report._representSeq) >= 1 ? "*" : "";
                    if (null == report._repStandardSeq) { // レポートが未提出
                        final String hyouka;
                        if (null == report._standardDate) {
                            // log.debug(" standardSeq = " + report._standardSeq + ":  _ standardDate = " + report._standardDate + "");
                            hyouka = null;
                        } else if (report._standardDate.compareTo(_param._kijun) < 0) { // 未提出 && 提出期限を超えた
                            // log.debug(" standardSeq = " + report._standardSeq + ":  x standardDate = " + report._standardDate + "");
                            hyouka = "×";
                        } else { // 未提出 && 提出期限を超えていない
                            // log.debug(" standardSeq = " + report._standardSeq + ":  n standardDate = " + report._standardDate + "");
                            hyouka = null;
                        }
                        svf.VrsOutn("REP" + i, HYOUKA, hyouka); // 評価
                        svf.VrsOutn("REP" + i, HENSOUBI, aster); // 返送日
                    } else if (null == report._gradValue || "".equals(report._gradValue) ||
                                null != report._gradDate && _param._kijun.compareTo(report._gradDate) < 0
                            ) {
                        // log.debug(" standardSeq = " + report._standardSeq + ":  r receiiptDate = " + report._receiptDate + "");
                        svf.VrsOutn("REP" + i, HYOUKA, "受"); // 評価
                        svf.VrsOutn("REP" + i, HENSOUBI, aster); // 返送日
                    } else {
                        // log.debug(" standardSeq = " + report._standardSeq + ":  g gradValue = " + report._gradValueName + "");
                        svf.VrsOutn("REP" + i, HYOUKA, report._gradValueName); // 評価
                        svf.VrsOutn("REP" + i, HENSOUBI, formatDate(report._gradDate)+ aster); // 返送日
                        if ("1".equals(report._namespare1)) {
                            count += 1;
                        }
                    }
                }

                svf.VrsOut("COUNT3", String.valueOf(count)); // レポート合格回数

                _hasData = true;
                svf.VrEndRecord();
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

    private List getStudentList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Student student = null;
            ps = db2.prepareStatement(sql());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null == student || !student._schregno.equals(rs.getString("SCHREGNO"))) {
                    final String inoutcd = rs.getString("INOUTCD");
                    final String name = rs.getString("NAME");
                    final String baseRemark1 = rs.getString("BASE_REMARK1");

                    final String thisYearCreditTime = null == rs.getString("THIS_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("THIS_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("THIS_YEAR_CREDIT_TIME"));
                    final String beforeYearCreditTime = null == rs.getString("BEFORE_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("BEFORE_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("BEFORE_YEAR_CREDIT_TIME"));
                    final String totalYearCreditTime = null == rs.getString("TOTAL_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("TOTAL_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("TOTAL_YEAR_CREDIT_TIME"));
                    final String baseRemark2Name1 = StringUtils.defaultString(rs.getString("BASE_REMARK2_NAME1"));

                    student = new Student(rs.getString("SCHREGNO"), inoutcd, name, baseRemark1, thisYearCreditTime, beforeYearCreditTime, totalYearCreditTime, baseRemark2Name1);
                    list.add(student);
                }

                final String year = rs.getString("YEAR");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String repSeqAll = rs.getString("REP_SEQ_ALL");
                final String schSeqAll = rs.getString("SCH_SEQ_ALL");
                final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                final String credits = rs.getString("CREDITS");

                final Subclass subclass = new Subclass(year, chaircd, classcd, schoolKind, curriculumCd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin, credits);
                student._subclassList.add(subclass);
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }

        try {
            ps = db2.prepareStatement(getSchAttendSql());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = getStudent(list, rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final Subclass subclass = getSubclass(student._subclassList, rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                if (null == subclass) {
                    continue;
                }
                subclass._attendList.add(new Attend(rs.getString("SCHOOLINGKINDCD"), rs.getString("NAMESPARE1"), rs.getString("EXECUTEDATE"), rs.getString("PERIODCD"), rs.getBigDecimal("CREDIT_TIME")));
            }
       } catch (Exception ex) {
            log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }

       try {
           final String reportSql = getReportSql();
           ps = db2.prepareStatement(reportSql);
           rs = ps.executeQuery();
           while (rs.next()) {
               final Student student = getStudent(list, rs.getString("SCHREGNO"));
               if (null == student) {
                   continue;
               }
               final Subclass subclass = getSubclass(student._subclassList, rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
               if (null == subclass) {
                   continue;
               }
               subclass._reportList.add(new Report(rs.getString("STANDARD_SEQ"), rs.getString("STANDARD_DATE"), rs.getString("REP_STANDARD_SEQ"), rs.getString("NAMESPARE1"), rs.getString("REPRESENT_SEQ"), rs.getString("RECEIPT_DATE"), rs.getString("GRAD_DATE"), rs.getString("GRAD_VALUE"), rs.getString("GRAD_VALUE_NAME")));
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

    private Subclass getSubclass(final List list, final String classcd, final String schoolKind, final String curriculumCd, final String subclassCd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._classcd.equals(classcd) && subclass._schoolKind.equals(schoolKind) && subclass._curriculumCd.equals(curriculumCd) && subclass._subclasscd.equals(subclassCd)) {
                return subclass;
            }
        }
        return null;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SPECIAL_ATTEND AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO,  ");
        stb.append("         VALUE(CASE WHEN YEAR = '" + _param._year + "' THEN 'THIS' ELSE 'BEFORE' END, 'TOTAL') AS DIV, ");
        stb.append("         SUM(CREDIT_TIME) AS CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SPECIALACT_ATTEND_DAT ");
        stb.append("     WHERE ");
        stb.append("         (YEAR < '" + _param._year + "' OR YEAR = '" + _param._year + "' AND ATTENDDATE <= '" + _param._kijun + "') ");
        stb.append("         AND CLASSCD IN ('93', '94') ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ( ");
        stb.append("             (SCHREGNO), ");
        stb.append("             (SCHREGNO, ");
        stb.append("              CASE WHEN YEAR = '" + _param._year + "' THEN 'THIS' ELSE 'BEFORE' END)) ");
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
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T4.INOUTCD, ");
        stb.append("         T4.NAME, ");
        stb.append("         T7.BASE_REMARK1, ");
        stb.append("         T9.CREDIT_TIME AS THIS_YEAR_CREDIT_TIME, ");
        stb.append("         T10.CREDIT_TIME AS BEFORE_YEAR_CREDIT_TIME, ");
        stb.append("         T11.CREDIT_TIME AS TOTAL_YEAR_CREDIT_TIME, ");
        stb.append("         T13.NAME1 AS BASE_REMARK2_NAME1 ");
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO ");
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
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T7.YEAR = T1.YEAR ");
        stb.append("         AND T7.BASE_SEQ = '001' ");
        stb.append("     LEFT JOIN SUBCLASS_MST T8 ON T8.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SPECIAL_ATTEND T9 ON T9.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T9.DIV = 'THIS' ");
        stb.append("     LEFT JOIN SPECIAL_ATTEND T10 ON T10.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T10.DIV = 'BEFORE' ");
        stb.append("     LEFT JOIN SPECIAL_ATTEND T11 ON T11.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T11.DIV = 'TOTAL' ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T12 ON T12.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T12.BASE_SEQ = '004' ");
        stb.append("     LEFT JOIN NAME_MST T13 ON T13.NAMECD1 = 'M013' ");
        stb.append("         AND T13.NAMECD2 = T12.BASE_REMARK2 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND T3.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T3.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        return stb.toString();
    }

    private String getSchAttendSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T3.CLASSCD, ");
        stb.append("         T3.SCHOOL_KIND, ");
        stb.append("         T3.CURRICULUM_CD, ");
        stb.append("         T3.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.SCHOOLINGKINDCD, ");
        stb.append("         T4.NAMESPARE1, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SCH_ATTEND_DAT T1 ");
        stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER <> '9' ");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
        stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.EXECUTEDATE <= '" + _param._kijun + "' ");
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
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
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
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
        stb.append("         T2.RECEIPT_DATE <= '" + _param._kijun + "' ");
        stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
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
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
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
        stb.append("         REP_STANDARDDATE_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_SCHREGNO T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append("     SELECT ");
        stb.append("         T0.YEAR, ");
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
        stb.append("         T0.CURRICULUM_CD, ");
        stb.append("         T0.SUBCLASSCD, ");
        stb.append("         T0.STANDARD_SEQ, ");
        stb.append("         T1.REPRESENT_SEQ ");
        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final String _inoutcd;
        final String _name;
        final String _baseRemark1;
        final String _thisYearCreditTime;
        final String _beforeYearCreditTime;
        final String _totalYearCreditTime;
        final String _baseRemark2Name1;

        final List _subclassList = new ArrayList();

        Student(
                final String schregno,
                final String inoutcd,
                final String name,
                final String baseRemark1,
                final String thisYearCreditTime,
                final String beforeYearCreditTime,
                final String totalYearCreditTime,
                final String baseRemark2Name1
        ) {
            _schregno = schregno;
            _inoutcd = inoutcd;
            _name = name;
            _baseRemark1 = baseRemark1;
            _thisYearCreditTime = thisYearCreditTime;
            _beforeYearCreditTime = beforeYearCreditTime;
            _totalYearCreditTime = totalYearCreditTime;
            _baseRemark2Name1 = baseRemark2Name1;
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
        final List _reportList = new ArrayList();
        final List _attendList = new ArrayList();

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

        public String getSubclassname() {
            return StringUtils.defaultString(_subclassname);
        }

        public String getAttendCount(final boolean isHousou) {
            final Set set = new HashSet();
            boolean addval = false;
            BigDecimal n = new BigDecimal(0);
            for (final Iterator it = _attendList.iterator(); it.hasNext();) {
                final Attend at = (Attend) it.next();
                if (isHousou && "2".equals(at._schoolingkindcd)) {
                    if (null != at._creditTime) {
                        addval = true;
                        n = n.add(at._creditTime);
                    }
                } else if (!isHousou && "1".equals(at._namespare1)) {
                    if ("1".equals(at._schoolingkindcd)) {
                        set.add(at._executedate);
                    } else {
                        if (null != at._creditTime) {
                            addval = true;
                            n = n.add(at._creditTime);
                        }
                    }
                }

            }
            if (!set.isEmpty()) {
                addval = true;
                n = n.add(new BigDecimal(set.size()));
            }
            return addval ? getDispNum(n) : !isHousou ? "0" : null;
        }

        public List getAttendList(final boolean isHousou) {
            final TreeSet set = new TreeSet();
            for (final Iterator it = _attendList.iterator(); it.hasNext();) {
                final Attend at = (Attend) it.next();
                if (isHousou && !"1".equals(at._namespare1) && null != at._executedate) {
                    set.add(at._executedate);
                } else if (!isHousou && "1".equals(at._namespare1) && null != at._executedate) {
                    set.add(at._executedate);
                }
            }
            return new ArrayList(set);
        }
    }

    private static class Report {
        final String _standardSeq; // REP_STANDARDDATE_DAT.STANDARD_SEQ
        final String _standardDate;
        final String _repStandardSeq; // REP_REPORT_DAT.STANDARD_SEQ
        final String _namespare1;
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;
        public Report(final String standardSeq, final String standardDate, final String repStandardSeq, final String namespare1, final String representSeq, final String receiptDate, final String gradDate, final String gradValue, final String gradValueName) {
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _repStandardSeq = repStandardSeq;
            _namespare1 = namespare1;
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = null == gradValue ? "受" : gradValueName;
        }
    }

    private static class Attend {
        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;
        public Attend(final String schoolingkindcd, final String namespare1, final String executedate, final String periodcd, final BigDecimal creditTime) {
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75120 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _kijun;
        final String _schoolName1;
        final String _loginDate;
        final Map _hreportRemarkTDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _kijun = request.getParameter("KIJUN").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolName1 = getSchoolName1(db2);
            _hreportRemarkTDat = getHreportRemarkTDatMap(db2);
        }

        private String getSchoolName1(final DB2UDB db2) {
            String schoolName1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolName1 = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName1;
        }

        private Map getHreportRemarkTDatMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT REMARKID, REMARK FROM HREPORTREMARK_T_DAT WHERE REMARKID IN ('1', '2', '3', '4', '5', '6') ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("REMARKID"), rs.getString("REMARK"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof


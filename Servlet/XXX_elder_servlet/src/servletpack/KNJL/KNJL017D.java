/*
 * $Id: fad01554518cccdb5707dec6c700ef38a4337248 $
 *
 * 作成日: 2018/01/22
 * 作成者: kawata
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL017D {

    private static final Log log = LogFactory.getLog(KNJL017D.class);

    private boolean _hasData;

    private Param _param;

	private String bithdayField;

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
        svf.VrSetForm("KNJL017D.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 25;
        int printLine = 1;
        int pageCnt = 1;
        final String ttlTotalInfoStr = getTotalCntInfo(db2);
      log.debug(" ttlTotalInfoStr = " + ttlTotalInfoStr);

        setTitle(db2, svf, ttlTotalInfoStr);//ヘッダ
        svf.VrsOut("PAGE", String.valueOf(1) + "頁");
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(db2, svf, ttlTotalInfoStr);//ヘッダ
                printLine = 1;
                svf.VrsOut("PAGE", String.valueOf(pageCnt + 1) + "頁");
                pageCnt = pageCnt + 1;
            }

            //データ
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, printData._examNo);
            //氏名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME1" , printLine, printData._name);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME2" , printLine, printData._name);
            }else {
                svf.VrsOutn("NAME3" , printLine, printData._name);
            }
            //専併区分
            svf.VrsOutn("SHDIV" , printLine, printData._shdiv);
            //志望累計
            svf.VrsOutn("DESIRE_DIV" , printLine, printData._desirediv);
            //内申合計（５科目）
            svf.VrsOutn("TOTAL5" , printLine, printData._subtotal5);
            //内申合計（９科目）
            svf.VrsOutn("TOTAL_ALL" , printLine, printData._subtotalAll);
            //欠席日数（１年）
            svf.VrsOutn("ABSENCE_DAYS" , printLine, printData._absenceDays);
            //欠席日数（２年）
            svf.VrsOutn("ABSENCE_DAYS2" , printLine, printData._absenceDays2);
            //欠席日数（３年）
            svf.VrsOutn("ABSENCE_DAYS3" , printLine, printData._absenceDays3);
            //入試得点（国語）
            svf.VrsOutn("SCORE1" , printLine, printData._score1);
            //入試得点（数学）
            svf.VrsOutn("SCORE2" , printLine, printData._score2);
            //入試得点（英語）
            svf.VrsOutn("SCORE3" , printLine, printData._score3);
            //合計
            svf.VrsOutn("SCORE_ALL" , printLine, printData._scoreTotal);
            //特待区分
            svf.VrsOutn("JUDGE_KIND" , printLine, printData._judgeKind);
            //クラブ名
            final String addrStr = printData._clubNm;
            if (30 >= KNJ_EditEdit.getMS932ByteLength(addrStr)) {
                svf.VrsOutn("CLUB_NAME1", printLine, addrStr);
            }else {
                svf.VrsOutn("CLUB_NAME2", printLine, addrStr);
            }
            //内部判定
            svf.VrsOutn("SUB_ORDER" , printLine, printData._subOrder);

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String ttlTotalInfoStr) {
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + "　" + _param._testdivName + "　内部選考資料");
        final String _prntSortStr = jdgSortStr();
        svf.VrsOut("SUBTITLE", "(表示順：" + _prntSortStr + ")");
        svf.VrsOut("TOTAL_NUM", ttlTotalInfoStr);
        svf.VrsOut("CLASS_NAME1", "国語");
        svf.VrsOut("CLASS_NAME2", "数学");
        svf.VrsOut("CLASS_NAME3", "英語");
    }

    private String jdgSortStr() {
    	String jdgStr = "受験番号順";
        if ("J_LANG_ASC".equals(_param._sortType)) {
        	jdgStr = "国語(昇順)";
        } else if ("J_LANG_DESC".equals(_param._sortType)) {
        	jdgStr = "国語(降順)";
        } else if ("MATH_ASC".equals(_param._sortType)) {
        	jdgStr = "数学(昇順)";
        } else if ("MATH_DESC".equals(_param._sortType)) {
        	jdgStr = "数学(降順)";
        } else if ("E_LANG_ASC".equals(_param._sortType)) {
        	jdgStr = "英語(昇順)";
        } else if ("E_LANG_DESC".equals(_param._sortType)) {
        	jdgStr = "英語(降順)";
        } else if ("TOTAL_ASC".equals(_param._sortType)) {
        	jdgStr = "合計(昇順)";
        } else if ("TOTAL_DESC".equals(_param._sortType)) {
        	jdgStr = "合計(降順)";
        }
    	return jdgStr;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug(" DBG_ROUTE_1 ");

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                log.debug(" EXAM_NO = " + examNo);
                final String name = rs.getString("NAME");
                final String shdiv = rs.getString("SHDIV");
                final String desirediv = rs.getString("DESIREDIV");
                final String subtotal5 = rs.getString("SUBTOTAL5");
                final String subtotalAll = rs.getString("SUBTOTAL_ALL");
                final String absenceDays = rs.getString("ABSENCE_DAYS");
                final String absenceDays2 = rs.getString("ABSENCE_DAYS2");
                final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String scoreTotal = rs.getString("SCORE_TOTAL");
                final String judgeKind = rs.getString("JUDGE_KIND");
                final String clubCd = rs.getString("CLUBCD");
                final String clubNm = rs.getString("CLUBNAME");
                final String subOrder = rs.getString("SUB_ORDER");

                final PrintData printData = new PrintData(examNo, name, shdiv, desirediv, subtotal5, subtotalAll, absenceDays, absenceDays2, absenceDays3, score1, score2, score3, scoreTotal, judgeKind, clubCd, clubNm, subOrder );
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getTotalCntInfo(final DB2UDB db2) {
    	String retStr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String summaryval = getTotalCustomInfo(db2, "");
        retStr = "合計人数：" + summaryval + "人  内部判定";
        try {
            final String cdlistsql = getDecisionMstCdsql();
            log.debug(" cdlistsql =" + cdlistsql);
            ps = db2.prepareStatement(cdlistsql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cdval = rs.getString("VALUE");
                final String customval = getTotalCustomInfo(db2, cdval);
                retStr += " " + cdval + "：" + customval + "人";
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    	return retStr;
    }
    private String getTotalCustomInfo(final DB2UDB db2, final String jdgcode) {
    	String retStr = "";
        PreparedStatement pssub = null;
        ResultSet rssub = null;

        try {
            final String cntsql = getTotalCntsql(jdgcode);
            log.debug(" cntsql =" + cntsql);
            pssub = db2.prepareStatement(cntsql);
            rssub = pssub.executeQuery();
            String setcntstr = "";
            while (rssub.next()) {
                setcntstr = rssub.getString("CNT");
            }
          	retStr = setcntstr;
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, pssub, rssub);
            db2.commit();
        }
    	return retStr;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.EXAMNO AS EXAMNO,  ");
        stb.append("    T1.NAME AS NAME,  ");
        stb.append("    NAME_SHDIV.NAME1 AS SHDIV,  ");
        stb.append("    T1.DESIREDIV AS DESIREDIV,  ");
        stb.append("    CASE WHEN T3_1.REMARK10 IS NOT NULL OR T3_2.REMARK10 IS NOT NULL OR T3.TOTAL5 IS NOT NULL THEN ");
        stb.append("        VALUE(INT(T3_1.REMARK10), 0) + VALUE(INT(T3_2.REMARK10), 0) + VALUE(T3.TOTAL5,0) END AS SUBTOTAL5, ");
        stb.append("    CASE WHEN T3_1.REMARK11 IS NOT NULL OR T3_2.REMARK11 IS NOT NULL OR T3.TOTAL_ALL IS NOT NULL THEN ");
        stb.append("        VALUE(INT(T3_1.REMARK11), 0) + VALUE(INT(T3_2.REMARK11), 0) + VALUE(T3.TOTAL_ALL,0) END AS SUBTOTAL_ALL, ");
        stb.append("    T3.ABSENCE_DAYS AS ABSENCE_DAYS, ");
        stb.append("    T3.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ");
        stb.append("    T3.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ");
        stb.append("    D1_J_LANG.SCORE AS SCORE1, ");
        stb.append("    D1_MATH.SCORE AS SCORE2, ");
        stb.append("    D1_E_LANG.SCORE AS SCORE3, ");
        stb.append("    (D1_J_LANG.SCORE + D1_MATH.SCORE + D1_E_LANG.SCORE) AS SCORE_TOTAL, ");
        stb.append("    T1.JUDGE_KIND AS JUDGE_KIND, ");
        stb.append("    T2.REMARK3 AS CLUBCD, ");
        stb.append("    CLUB_M.CLUBNAME AS CLUBNAME, ");
        stb.append("    T1.SUB_ORDER AS SUB_ORDER ");
        stb.append(" FROM ");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("      ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T2.EXAMNO ");
        stb.append("      AND T2.SEQ = '019' ");
        stb.append("    LEFT JOIN CLUB_MST CLUB_M ");
        stb.append("      ON T2.REMARK1 = CLUB_M.SCHOOLCD ");
        stb.append("      AND T2.REMARK2 = CLUB_M.SCHOOL_KIND ");
        stb.append("      AND T2.REMARK3 = CLUB_M.CLUBCD ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ");
        stb.append("      ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T3.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_1 ");
        stb.append("      ON T1.ENTEXAMYEAR = T3_1.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T3_1.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T3_1.EXAMNO ");
        stb.append("      AND T3_1.SEQ = '001' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_2 ");
        stb.append("      ON T1.ENTEXAMYEAR = T3_2.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T3_2.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T3_2.EXAMNO ");
        stb.append("      AND T3_2.SEQ = '002' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_J_LANG ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_J_LANG.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_J_LANG.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_J_LANG.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_J_LANG.RECEPTNO ");
        stb.append("      AND D1_J_LANG.TESTSUBCLASSCD = '1' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_MATH ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_MATH.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_MATH.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_MATH.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_MATH.RECEPTNO ");
        stb.append("      AND D1_MATH.TESTSUBCLASSCD = '2' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_E_LANG ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_E_LANG.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_E_LANG.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_E_LANG.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_E_LANG.RECEPTNO ");
        stb.append("      AND D1_E_LANG.TESTSUBCLASSCD = '3' ");
        stb.append("    LEFT JOIN NAME_MST NAME_SHDIV ");
        stb.append("      ON NAME_SHDIV.NAMECD1 = 'L006' ");
        stb.append("      AND T1.SHDIV = NAME_SHDIV.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T2.REMARK1      = '" + _param._schoolcd + "' ");
        stb.append("     AND T2.REMARK2      = '"+ _param._schoolkind + "' ");
        stb.append(" ORDER BY ");
        if ("J_LANG_ASC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 ASC ");
        } else if ("J_LANG_DESC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 DESC ");
        } else if ("MATH_ASC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 ASC ");
        } else if ("MATH_DESC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 DESC ");
        } else if ("E_LANG_ASC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 ASC ");
        } else if ("E_LANG_DESC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 DESC ");
        } else if ("TOTAL_ASC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL ASC ");
        } else if ("TOTAL_DESC".equals(_param._sortType)) {
            stb.append("     CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL DESC ");
        } else {
            stb.append("     SUBSTR(CHAR(DECIMAL(T1.EXAMNO, 10, 0)),1,10) ");
        }

        return stb.toString();
    }

    private String getTotalCntsql(String jdgtype) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append("  FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" WHERE T1.ENTEXAMYEAR    = '" + _param._entExamYear + "'");
        stb.append("   AND T1.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV        = '" + _param._testDiv + "' ");
        if (! "".equals(jdgtype)) {
        	stb.append("   AND T1.SUB_ORDER      = '" + jdgtype + "' ");
//        } else {
//        	stb.append("   AND T1.SUB_ORDER IS NOT NULL ");
        }
        return stb.toString();
    }

    private String getDecisionMstCdsql()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     DECISION_CD AS VALUE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_INTERNAL_DECISION_MST ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE ");
        return stb.toString();
    }

    private class PrintData {
        final String _examNo;
        final String _name;
        final String _shdiv;
        final String _desirediv;
        final String _subtotal5;
        final String _subtotalAll;
        final String _absenceDays;
        final String _absenceDays2;
        final String _absenceDays3;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _scoreTotal;
        final String _judgeKind;
        final String _clubCd;
        final String _clubNm;
        final String _subOrder;


        public PrintData(
                final String examNo,
                final String name,
                final String shdiv,
                final String desirediv,
                final String subtotal5,
                final String subtotalAll,
                final String absenceDays,
                final String absenceDays2,
                final String absenceDays3,
                final String score1,
                final String score2,
                final String score3,
                final String scoreTotal,
                final String judgeKind,
                final String clubCd,
                final String clubNm,
                final String subOrder
        ) {
            _examNo = examNo;
            _name = name;
            _shdiv = shdiv;
            _desirediv = desirediv;
            _subtotal5 = subtotal5;
            _subtotalAll = subtotalAll;
            _absenceDays = absenceDays;
            _absenceDays2 = absenceDays2;
            _absenceDays3 = absenceDays3;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _scoreTotal = scoreTotal;
            _judgeKind = judgeKind;
            _clubCd = clubCd;
            _clubNm = clubNm;
            _subOrder = subOrder;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final String _sortType;
        private final String _schoolcd;
        private final String _schoolkind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _sortType       = request.getParameter("SORT_TYPE");
            _schoolcd       = request.getParameter("SCHOOLCD");
            _schoolkind       = request.getParameter("SCHOOLKIND");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

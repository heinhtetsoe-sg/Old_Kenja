/*
 * $Id: 067ef3a6736684e9875dc04e4085afc8f3930434 $
 *
 * 作成日: 2012/10/23
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 教科書・学習書無償給与承認者一覧
 */
public class KNJM808 {

    private static final Log log = LogFactory.getLog(KNJM808.class);

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

    private int yearToNendo(final DB2UDB db2, final String year) {
        final String nen = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, year + "-04-01"))[1];
		return "元".equals(nen) ? 1 : Integer.parseInt(nen);
    }
    
    private long toLong(final String l) {
        return null == l ? 0 : Long.parseLong(l);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJM808.frm", 1);
        printTitle(db2, svf);
        
        int line = 0;
        int no = 0;
        
        long totalpay = 0;
        long totalsaray = 0;
        long totalprice = 0;
        long totalprice2 = 0;
        long totaltextcount = 0;
        long totalacsalary = 0;
        
        final String sql = sql();
        log.debug(" sql =" + sql);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            line += 1;
            no += 1;
            if (line > 40) {
                svf.VrEndPage();
                printTitle(db2, svf);
                line = 1;
            }

            svf.VrsOutn("NO", line, String.valueOf(no));
            svf.VrsOutn("NAME", line, KnjDbUtils.getString(row, "NAME"));
            svf.VrsOutn("SCH_NO", line, KnjDbUtils.getString(row, "SCHREGNO"));
            
            final String addr1 = KnjDbUtils.getString(row, "ADDR1");
            final String addr2 = KnjDbUtils.getString(row, "ADDR2");
            final String fno;
            if ((KNJ_EditEdit.getMS932ByteLength(addr1) > 30 || KNJ_EditEdit.getMS932ByteLength(addr2) > 30)) {
                fno = "_3";
            } else if (KNJ_EditEdit.getMS932ByteLength(addr1) > 20 || KNJ_EditEdit.getMS932ByteLength(addr2) > 20) {
                fno = "_2";
            } else {
                fno = "";
            }
            svf.VrsOutn("ADDRESS1" + fno, line, addr1);
            svf.VrsOutn("ADDRESS2" + fno, line, addr2);

            svf.VrsOutn("CHECK_NO", line, KnjDbUtils.getString(row, "SCHREGNO").substring(2));

            svf.VrsOutn("PAY", line, KnjDbUtils.getString(row, "PRICE_TOTAL"));
            totalpay += toLong(KnjDbUtils.getString(row, "PRICE_TOTAL"));
            svf.VrsOutn("SALARY", line, KnjDbUtils.getString(row, "TOTAL_GK"));
            totalsaray += toLong(KnjDbUtils.getString(row, "TOTAL_GK"));
            svf.VrsOutn("TEXT_PRICE", line, KnjDbUtils.getString(row, "BOOKDIV1_GK"));
            totalprice += toLong(KnjDbUtils.getString(row, "BOOKDIV1_GK"));
            svf.VrsOutn("TEXT_PRICE2", line, KnjDbUtils.getString(row, "BOOKDIV2_GK"));
            totalprice2 += toLong(KnjDbUtils.getString(row, "BOOKDIV2_GK"));
            svf.VrsOutn("TEXT_COUNT", line, KnjDbUtils.getString(row, "TOTAL_COUNT"));
            totaltextcount += toLong(KnjDbUtils.getString(row, "TOTAL_COUNT"));

            svf.VrsOutn("LAST_CREDIT", line, KnjDbUtils.getString(row, "MISHUTOKU_TANNI_SUM"));
            svf.VrsOutn("COMMIT_SUBJECT", line, KnjDbUtils.getString(row, "SUBCLASS_COUNT"));
            svf.VrsOutn("ITEM", line, KnjDbUtils.getString(row, "PROVIDE_REASON"));
            
            if (KNJ_EditEdit.getMS932ByteLength(KnjDbUtils.getString(row, "ATTACH_DOCUMENTS")) > 14) {
                svf.VrsOutn("ATTACH2", line, KnjDbUtils.getString(row, "ATTACH_DOCUMENTS"));
            } else {
                svf.VrsOutn("ATTACH1", line, KnjDbUtils.getString(row, "ATTACH_DOCUMENTS"));
            }
            svf.VrsOutn("REMARK", line, KnjDbUtils.getString(row, "REMARK"));
            svf.VrsOutn("JUDGE", line, KnjDbUtils.getString(row, "JUDGE_RESULT_NAME"));
            if ("2".equals(KnjDbUtils.getString(row, "JUDGE_RESULT"))) {
                svf.VrsOutn("AC_SLARY", line, KnjDbUtils.getString(row, "TOTAL_GK"));
                totalacsalary += toLong(KnjDbUtils.getString(row, "TOTAL_GK"));
            }
            _hasData = true;
        }

        if (line != 0) {
            // 合計
            svf.VrsOutn("PAY", 41, String.valueOf(totalpay));
            svf.VrsOutn("SALARY", 41, String.valueOf(totalsaray));
            svf.VrsOutn("TEXT_PRICE", 41, String.valueOf(totalprice));
            svf.VrsOutn("TEXT_PRICE2", 41, String.valueOf(totalprice2));
            svf.VrsOutn("TEXT_COUNT", 41, String.valueOf(totaltextcount));
            svf.VrsOutn("AC_SLARY", 41, String.valueOf(totalacsalary));
        }

        svf.VrEndPage();

    }
    
    private void printTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("TITLE", "1".equals(_param._output) ? "申請者一覧" : "承認者一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
        svf.VrsOut("YRAR_BEFORE_LAST", hankakuToZenkaku(String.valueOf(yearToNendo(db2, _param._year) - 2)) + "年度");
        svf.VrsOut("LAST_YEAR", hankakuToZenkaku(String.valueOf(yearToNendo(db2, _param._year) - 1)) + "年度");
        svf.VrsOut("THIS_YEAR", hankakuToZenkaku(String.valueOf(yearToNendo(db2, _param._year))) + "年度");
    }
    
    private String hankakuToZenkaku(final String s) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char ch;
            if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                ch = (char) (s.charAt(i) - '0' + '０');
            } else {
                ch = s.charAt(i);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_SUBCLASS_TEXT_PRICE AS (");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     SUM(T2.TEXTBOOKUNITPRICE) AS PRICE_TOTAL ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TEXTBOOK_SUBCLASS_DAT T1 ");
        stb.append("     INNER JOIN TEXTBOOK_MST T2 ON T2.TEXTBOOKCD = T1.TEXTBOOKCD ");
        stb.append("         AND (T2.TEXTBOOKCD = '1' OR T2.TEXTBOOKCD = '2' OR T2.TEXTBOOKCD = '3') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, T1.YEAR ");
        stb.append(" ), SUBCLASS_COUNT AS (");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     COUNT(DISTINCT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) AS COUNT ");
        stb.append(" FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append(" INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" , LAST_YEAR_MISHUTOKU_TANNI AS (");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     COUNT(DISTINCT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) AS COUNT, ");
        stb.append("     SUM(L2.CREDITS) AS MISHUTOKU_TANNI_SUM ");
        stb.append(" FROM SCHREG_STUDYREC_DAT T1 ");
        stb.append(" INNER JOIN (SELECT SCHREGNO, YEAR, ANNUAL, MAX(GRADE) AS GRADE, MAX(COURSECD || '-' || MAJORCD || '-' || COURSECODE) AS COURSE ");
        stb.append("             FROM SCHREG_REGD_DAT  ");
        stb.append("             GROUP BY SCHREGNO, YEAR, ANNUAL ");
        stb.append("            ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.ANNUAL = T1.ANNUAL ");
        stb.append(" INNER JOIN CREDIT_MST L2 ON L2.YEAR = L1.YEAR ");
        stb.append("     AND L2.COURSECD || '-' || L2.MAJORCD || '-' || L2.COURSECODE = L1.COURSE ");
        stb.append("     AND L2.GRADE = L1.GRADE ");
        stb.append("     AND L2.CLASSCD = T1.CLASSCD ");
        stb.append("     AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND L2.CREDITS IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 1) + "' ");
        stb.append("     AND (T1.GET_CREDIT IS NULL AND T1.ADD_CREDIT IS NULL OR VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) = 0) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     L1.SEND_ZIPCD AS ZIPCD, ");
        stb.append("     L1.SEND_ADDR1 AS ADDR1, ");
        stb.append("     L1.SEND_ADDR2 AS ADDR2, ");
        stb.append("     L1.SEND_NAME AS NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.JUDGE_RESULT, ");
        stb.append("     T1.DECISION_DATE, ");
        stb.append("     T1.TOTAL_GK, ");
        stb.append("     T1.BOOKDIV1_GK, ");
        stb.append("     T1.BOOKDIV2_GK, ");
        stb.append("     T1.TOTAL_COUNT, ");
        stb.append("     T1.PROVIDE_REASON, ");
        stb.append("     T1.ATTACH_DOCUMENTS, ");
        stb.append("     T1.REMARK, ");
        stb.append("     NMB021.NAME1 AS JUDGE_RESULT_NAME, ");
        stb.append("     T1.TOTAL_GK, ");
        stb.append("     L3.PRICE_TOTAL, ");
        stb.append("     L4.COUNT AS SUBCLASS_COUNT, ");
        stb.append("     L5.MISHUTOKU_TANNI_SUM ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TEXTBOOK_FREE_APPLY_DAT T1 ");
        stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(REGISTER_DATE) AS REGISTER_DATE ");
        stb.append("                 FROM SCHREG_TEXTBOOK_FREE_APPLY_DAT T1 ");
        stb.append("                 GROUP BY SCHREGNO, YEAR) TT1 ON TT1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                             AND TT1.YEAR = T1.YEAR ");
        stb.append("                                             AND TT1.REGISTER_DATE = T1.REGISTER_DATE ");
        stb.append("     LEFT JOIN SCHREG_SEND_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L1.DIV = '1' ");
        stb.append("     LEFT JOIN SCHREG_SUBCLASS_TEXT_PRICE L3 ON L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L3.YEAR = T1.YEAR ");
        stb.append("     LEFT JOIN NAME_MST NMB021 ON NMB021.NAMECD1 = 'B021' ");
        stb.append("         AND NMB021.NAMECD2 = T1.JUDGE_RESULT ");
        stb.append("     LEFT JOIN SUBCLASS_COUNT L4 ON L4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN LAST_YEAR_MISHUTOKU_TANNI L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._output)) {
        } else if ("2".equals(_param._output)) {
            stb.append("     AND JUDGE_RESULT IS NOT NULL ");
            stb.append("     AND DECISION_DATE IS NOT NULL ");
        }
        stb.append(" ORDER BY T1.SCHREGNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63400 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _output;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _loginDate = request.getParameter("LOGIN_DATE");
            _useAddrField2 = request.getParameter("useAddrField2");
        }
    }
}

// eof


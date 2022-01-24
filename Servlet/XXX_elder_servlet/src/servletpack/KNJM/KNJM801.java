/*
 * $Id: 28c47381dd041bf366d41d7c972a9d8c8fbbe220 $
 *
 * 作成日: 2012/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

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

/**
 * 受講料・諸経費の各種帳票
 */
public class KNJM801 {

    private static final Log log = LogFactory.getLog(KNJM801.class);

    private static final String OUT_NOUNYU = "3"; // 納入通知書
    private static final String OUT_JUKOURYOU = "1"; // 受講料・諸経費払込取扱票
    private static final String OUT_SOTSU = "2";  // 卒業関係費取扱票
    
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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }
    
    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }
    
    private String hankakuToZenkaku(final String s) {
        if (null == s) {
            return s;
        }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        for (int i = 0; i < _param._schregnos.length; i++) {
            if (OUT_JUKOURYOU.equals(_param._outputNo) || OUT_SOTSU.equals(_param._outputNo)) {
                printMain1(db2, svf, _param._schregnos[i]);
            } else if (OUT_NOUNYU.equals(_param._outputNo)) {
                printMain2(db2, svf, _param._schregnos[i]);
            }
        }
    }

    private void printMain1(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        svf.VrSetForm("KNJM801_1.frm", 1);
        
        printBank(db2, svf, schregno);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schregno);
//            log.debug("address sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
           
            while (rs.next()) {
                final String zipcd = rs.getString("ZIPCD");
                if (null != zipcd) {
                    if (-1 != zipcd.indexOf('-')) {
                        svf.VrsOut("ZIP1", StringUtils.split(zipcd, '-')[0]);
                        svf.VrsOut("ZIP2", StringUtils.split(zipcd, '-')[1]);
                    } else if (3 > zipcd.length()) {
                        svf.VrsOut("ZIP1", zipcd.substring(0, 3));
                        svf.VrsOut("ZIP2", zipcd.substring(3));
                    }
                }
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 50 || getMS932ByteLength(addr2) > 50)) {
                    svf.VrsOut("ADDRESS1_3", addr1);
                    svf.VrsOut("ADDRESS2_3", addr2);
                } else if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 40 || getMS932ByteLength(addr2) > 40)) {
                    svf.VrsOut("ADDRESS1_2", addr1);
                    svf.VrsOut("ADDRESS2_2", addr2);
                } else {
                    svf.VrsOut("ADDRESS1", addr1);
                    svf.VrsOut("ADDRESS2", addr2);
                }
                svf.VrsOut("SCH_NO", rs.getString("SCHREGNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("NAME2", rs.getString("NAME"));
            }
        } catch (final Exception ex) {
            log.error("set address error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }   
        
        long sum = 0;
        boolean hasnotnull = false;
        String outputCnt = null;
        try {
            final String sql = getMoneyDueSql(schregno);
            log.debug(" monye_due sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null != rs.getString("MONEY_DUE")) {
                    sum += rs.getLong("MONEY_DUE");
                    hasnotnull = true;
                }
                outputCnt = rs.getString("COUTPUT_CNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        svf.VrsOut("MARK", hankakuToZenkaku(outputCnt));
        if (hasnotnull) {
            svf.VrsOut("PRICE", String.valueOf(sum));
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolname);
        svf.VrsOut("SCHOOL_NAME2", _param._schoolname);

        svf.VrEndPage();
        _hasData = true;
    }
    

    private void printMain2(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        svf.VrSetForm("KNJM801_2.frm", 4);

        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._year + "-04-01") + "度" + "受講料・諸経費納入通知書");
        svf.VrsOut("LIMIT", StringUtils.defaultString(KNJ_EditDate.h_format_JP(_param._outputDate)) + "　" + StringUtils.defaultString(_param._youbi));
        
        for (int i = 0; i < 5; i++) {
            svf.VrsOut("U_REMARK" +  String.valueOf(i + 1), _param._certifSchoolRemark[i]);
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schregno);
//            log.debug("name sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
           
            while (rs.next()) {
                svf.VrsOut("NO", rs.getString("SCHREGNO"));
                svf.VrsOut("NAME", StringUtils.defaultString(rs.getString("NAME2")) + "　殿");
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }        
        
        try {
            final String sql = getMoneyDueSql(schregno);
            log.debug(" monye_due sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            long sumMoneyDue = 0;
            while (rs.next()) {
                if (null == rs.getString("FRESH_SCHREGNO")) {
                    svf.VrsOut("SUBCLASSCOUNT", "受講科目数");
                    if ("01".equals(rs.getString("COLLECT_L_CD"))) {
                        svf.VrsOut("FIELD1", rs.getString("SUM_COLLECT_CNT"));
                    }
                }
                svf.VrsOut("ITEM", rs.getString("COLLECT_M_NAME"));
                svf.VrsOut("PRICE", rs.getString("MONEY_DUE"));
                if (null != rs.getString("MONEY_DUE")) {
                    sumMoneyDue += rs.getLong("MONEY_DUE");
                    svf.VrsOut("TOTAL_PRICE", String.valueOf(sumMoneyDue));
                }
                if (getMS932ByteLength(rs.getString("REMARK")) > 30) {
                    svf.VrsOut("REMARK2", rs.getString("REMARK"));
                } else {
                    svf.VrsOut("REMARK1", rs.getString("REMARK"));
                }
                _hasData = true;
                svf.VrEndRecord();
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String getSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("00000".equals(_param._gradeHrclass)) { // 新入生
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.ZIPCD,");
            stb.append("  t0.ADDR1,");
            stb.append("  t0.ADDR2,");
            stb.append("  t0.NAME, ");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   FRESHMAN_DAT t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.ENTERYEAR = '" + _param._year + "' ");
            stb.append("  AND t0.SCHREGNO = '" + schregno + "' ");
        } else {
            stb.append(" WITH SCHREG_ADDRESS AS ( ");
            stb.append("   SELECT  ");
            stb.append("      T3.NAME AS SCHREG_NAME, ");
            stb.append("      T1.*  ");
            stb.append("   FROM  ");
            stb.append("      SCHREG_ADDRESS_DAT T1  ");
            stb.append("      INNER JOIN ( ");
            stb.append("        SElECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO ");
            stb.append("      ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("      INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");
            
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ZIPCD ELSE t2.ZIPCD END AS ZIPCD,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR1 ELSE t2.ADDR1 END AS ADDR1,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR2 ELSE t2.ADDR2 END AS ADDR2,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_NAME  ELSE t2.SCHREG_NAME END AS NAME,");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST t0 ");
            stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT t1 ON t1.SCHREGNO = t0.SCHREGNO ");
            stb.append("   AND t1.DIV = '1' ");
            stb.append(" LEFT JOIN SCHREG_ADDRESS t2 ON t2.SCHREGNO = t0.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  t0.SCHREGNO = '" + schregno + "' ");
        }
        return stb.toString();
    }

    private void printBank(final DB2UDB db2, final Vrw32alp svf, final String schregno) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YUUCYO_CD, ");
        stb.append("     T1.YUUCYO_DEPOSIT_ITEM, ");
        stb.append("     T1.YUUCYO_ACCOUNTNO, ");
        stb.append("     T1.YUUCYO_ACCOUNTNAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_BANK_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.COLLECT_BANK_CD = '0001' ");
        stb.append("     AND T1.COLLECT_BANK_DIV = '2' ");
        
//        log.debug(" bank sql =" + stb.toString());
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            
            if (rs.next()) {
                svf.VrsOut("BANK_CD1", rs.getString("YUUCYO_CD"));
                svf.VrsOut("BANK_CD2", rs.getString("YUUCYO_DEPOSIT_ITEM"));
                svf.VrsOut("BANK_CD3", rs.getString("YUUCYO_ACCOUNTNO"));
                svf.VrsOut("SUBJECT", rs.getString("YUUCYO_ACCOUNTNAME"));
            }
        } catch (final SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    public String getMoneyDueSql(final String schregno) {
        
        String outputNo = null;
        if (OUT_NOUNYU.equals(_param._outputNo)) {
            outputNo = "003";
        } else if (OUT_JUKOURYOU.equals(_param._outputNo)) {
            outputNo = "001";
        } else if (OUT_SOTSU.equals(_param._outputNo)) {
            outputNo = "002";
        }
        final StringBuffer stb = new StringBuffer();
        stb.append("  ");
        stb.append(" WITH MONEY_DUE_S_SUM AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.COLLECT_GRP_CD, T1.COLLECT_L_CD, T1.COLLECT_M_CD, SUM(T3.MONEY_DUE) AS MONEY_DUE ");
        stb.append("     FROM COLLECT_MONEY_DUE_M_DAT T1 ");
        stb.append("     INNER JOIN COLLECT_M_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("         AND T2.COLLECT_M_CD = T1.COLLECT_M_CD  ");
        stb.append("         AND T2.COLLECT_S_EXIST_FLG = '1' ");
        stb.append("     LEFT JOIN COLLECT_MONEY_DUE_S_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ");
        stb.append("         AND T3.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("         AND T3.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.COLLECT_GRP_CD, T1.COLLECT_L_CD, T1.COLLECT_M_CD ");
        stb.append(" ), MIN_COLLECT_MONEY_DUE_M AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         T2.COLLECT_GRP_CD, ");
        stb.append("         T2.COLLECT_L_CD, ");
        stb.append("         MIN(T2.COLLECT_M_CD) AS COLLECT_M_CD, ");
        stb.append("         SUM(COLLECT_CNT) AS SUM_COLLECT_CNT ");
        stb.append("     FROM ");
        stb.append("         COLLECT_MONEY_DUE_M_DAT T2 ");
        stb.append("     GROUP BY ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         T2.COLLECT_GRP_CD, ");
        stb.append("         T2.COLLECT_L_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COLLECT_GRP_CD, ");
        stb.append("     T1.COLLECT_L_CD, ");
        stb.append("     T1.COLLECT_M_CD, ");
        stb.append("     T2.SUM_COLLECT_CNT, ");
        stb.append("     CASE WHEN T3.SCHREGNO IS NOT NULL THEN T3.MONEY_DUE ELSE T1.MONEY_DUE END AS MONEY_DUE, ");
        stb.append("     T4.COLLECT_M_NAME, ");
        stb.append("     T4.REMARK, ");
        stb.append("     T5.COUTPUT_CNT, ");
        stb.append("     T6.SCHREGNO AS FRESH_SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     COLLECT_MONEY_DUE_M_DAT T1  ");
        stb.append("     INNER JOIN MIN_COLLECT_MONEY_DUE_M T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ");
        stb.append("         AND T2.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("         AND T2.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     LEFT JOIN MONEY_DUE_S_SUM T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ");
        stb.append("         AND T3.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("         AND T3.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     LEFT JOIN COLLECT_M_MST T4 ON T4.YEAR = T1.YEAR ");
        stb.append("         AND T4.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("         AND T4.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     LEFT JOIN COLLECT_MONEY_DUE_PRINT_DAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ");
        stb.append("         AND T5.OUTPUT_NO = '" + outputNo + "' ");
        stb.append("     LEFT JOIN FRESHMAN_DAT T6 ON T6.ENTERYEAR = T1.YEAR ");
        stb.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.COLLECT_GRP_CD = '" + _param._collectGrpCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.COLLECT_L_CD, ");
        stb.append("     T1.COLLECT_M_CD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year; // ログイン年度 or ログイン年度 + 1
//        private final String _ctrlyear;
//        private final String _semester;
        private final String _collectGrpCd;
        private final String _gradeHrclass;
        private final String _outputNo;
        private final String[] _schregnos;
        private final String _outputDate;
        private String _youbi;
        private String _schoolname;
        private String[] _certifSchoolRemark;
        private String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
//            _ctrlyear = request.getParameter("CTRL_YEAR");
//            _semester = request.getParameter("SEMESTER");
            _collectGrpCd = request.getParameter("COLLECT_GRP_CD");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); 
            _outputNo = request.getParameter("OUTPUT_NO");
            _outputDate = request.getParameter("OUTPUTDATE"); // 納入期限
            if (null != _outputDate) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(Date.valueOf(_outputDate.replace('/', '-')));
                final int keta = ((cal.get(Calendar.MONTH) + 1) < 10 ? 1 : 2) + (cal.get(Calendar.DAY_OF_MONTH) < 10 ? 1 : 2);
                final int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (1 <= dow && dow <= 7) {
                    _youbi = StringUtils.repeat(" ", 4 - keta) + new String[] {null, "日", "月", "火", "水", "木", "金", "土"}[dow] + "曜日";
                }
            }
            final String[] src = request.getParameterValues("category_name");
            final String[] dst = new String[src.length];
            for (int i = 0; i < src.length; i++) {
                dst[i] = StringUtils.split(src[i], "-")[0];
            }
            _schregnos = dst;
            loadCertifSchoolDat(db2);
            _useAddrField2 = request.getParameter("useAddrField2");
        }
        
        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _certifSchoolRemark = new String[5];
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '122' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolname = rs.getString("SCHOOL_NAME");
                    for (int i = 0; i < 5; i++) {
                        _certifSchoolRemark[i] = rs.getString("REMARK" + String.valueOf(i + 1));
                    }
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof


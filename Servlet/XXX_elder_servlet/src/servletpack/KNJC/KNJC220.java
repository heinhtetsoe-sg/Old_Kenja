// kanji=漢字
/*
 * $Id: 4a1dbfdd8bb535fa6ec3334e5afd0bec0ad0c994 $
 *
 * 作成日: 2009/08/19 15:47:43 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 4a1dbfdd8bb535fa6ec3334e5afd0bec0ad0c994 $
 */
public class KNJC220 {

    private static final Log log = LogFactory.getLog("KNJC220.class");
    private static final String FORM_NAME1  = "KNJC220_1.frm";

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request);

            _param.load(db2);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    /**
     * 印刷処理（メイン）
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        printHeader(svf);
        printPetitionCnt(svf);
    }

    /**
     * ヘッダ
     */
    private void printHeader(final Vrw32alp svf) {
        svf.VrSetForm(FORM_NAME1, 4);
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("DATE" , _param.getDate());
        svf.VrsOut("PRINT_DATE" , _param._printDate);
    }

    /**
     * 集計
     */
    private void printPetitionCnt(final Vrw32alp svf) {
        for (final Iterator iter = _param._petitionCntList.iterator(); iter.hasNext();) {
            final PetitionCnt petitionCnt = (PetitionCnt) iter.next();

            if (petitionCnt.isTotalLine()) setAmikake(svf, "paint");
            svf.VrsOut("HR_NAME",  petitionCnt._hrClassName);
            svf.VrsOut("ABSENCE",  petitionCnt._total);
            svf.VrsOut("CONDITION1",  petitionCnt._cnt1);
            svf.VrsOut("CONDITION2",  petitionCnt._cnt2);
            svf.VrsOut("CONDITION3",  petitionCnt._cnt3);
            svf.VrsOut("CONDITION4",  petitionCnt._cnt4);
            svf.VrsOut("CONDITION5",  petitionCnt._cnt5);
            svf.VrsOut("CONDITION6",  petitionCnt._cnt6);
            svf.VrsOut("CONDITION7",  petitionCnt._cnt7);
            svf.VrsOut("CONDITION8",  petitionCnt._cnt8);
            svf.VrEndRecord();
            _hasData = true;
            if (petitionCnt.isTotalLine()) setAmikake(svf, "clear");
        }
    }

    /**
     * 学年計・総合計を網掛けする
     */
    private void setAmikake(final Vrw32alp svf, String flg) {
        String strPaint = "paint".equals(flg) ? "Paint=(1,70,2),Bold=1" : "Paint=(0,0,0),Bold=0";
        svf.VrAttribute("HR_NAME",  strPaint);
        svf.VrAttribute("ABSENCE",  strPaint);
        svf.VrAttribute("CONDITION1",  strPaint);
        svf.VrAttribute("CONDITION2",  strPaint);
        svf.VrAttribute("CONDITION3",  strPaint);
        svf.VrAttribute("CONDITION4",  strPaint);
        svf.VrAttribute("CONDITION5",  strPaint);
        svf.VrAttribute("CONDITION6",  strPaint);
        svf.VrAttribute("CONDITION7",  strPaint);
        svf.VrAttribute("CONDITION8",  strPaint);
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");
        
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
    
    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request) throws Exception {
        final Param param = new Param(request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _printDate;
        private final String _fromDate;
        private final String _toDate;
        
        private List _petitionCntList = new ArrayList();

        Param(final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _printDate = KNJ_EditDate.h_format_JP(_loginDate);
            _fromDate = request.getParameter("DATE_FROM").replace('/', '-');
            _toDate = request.getParameter("DATE_TO").replace('/', '-');
        }
        
        /** 年度 */
        private String getNendo() {
            return nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
        }
        
        /** 集計範囲 */
        private String getDate() {
            return KNJ_EditDate.h_format_JP(_fromDate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_toDate);
        }

        /**
         * ヘッダ情報
         */
        private void load(final DB2UDB db2) throws SQLException, ParseException {
            setPetitionCnt(db2);
        }

        /**
         * 集計
         */
        private void setPetitionCnt(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getPetitionCntSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final PetitionCnt petitionCnt = new PetitionCnt(
                            rs.getString("GRADE_HR_CLASS"),
                            rs.getString("HR_NAME"),
                            rs.getString("TOTAL"),
                            rs.getString("CNT1"),
                            rs.getString("CNT2"),
                            rs.getString("CNT3"),
                            rs.getString("CNT4"),
                            rs.getString("CNT5"),
                            rs.getString("CNT6"),
                            rs.getString("CNT7"),
                            rs.getString("CNT8")
                    );
                    _petitionCntList.add(petitionCnt);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 集計
         */
        private String getPetitionCntSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCH_CHR AS ( ");
            stb.append("     SELECT T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD ");
            stb.append("       FROM SCH_CHR_DAT T1 ");
            stb.append("           ,CHAIR_STD_DAT T2 ");
            stb.append("      WHERE T1.YEAR     = '" + _year + "' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN DATE('" + _fromDate + "') AND DATE('" + _toDate + "') ");
            stb.append("        AND T1.YEAR     = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.CHAIRCD  = T2.CHAIRCD ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("     GROUP BY T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD ");
            stb.append("     ) ");
            stb.append(" , PETITION_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(T2.SEQNO) AS SEQNO, ");
            stb.append("         T2.SCHREGNO, ");
            stb.append("         T2.ATTENDDATE, ");
            stb.append("         T2.PERIODCD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_DAT T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR = '" + _year + "' ");
            stb.append("         AND T2.ATTENDDATE BETWEEN DATE('" + _fromDate + "') AND DATE('" + _toDate + "') ");
            stb.append("         AND EXISTS(SELECT  'X' FROM T_SCH_CHR W1 ");
            stb.append("                     WHERE  W1.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND  W1.EXECUTEDATE = T2.ATTENDDATE ");
            stb.append("                       AND  W1.PERIODCD = T2.PERIODCD) ");
            stb.append("     GROUP BY ");
            stb.append("         T2.SCHREGNO, ");
            stb.append("         T2.ATTENDDATE, ");
            stb.append("         T2.PERIODCD ");
            stb.append("     ) ");
            stb.append(" , PETITION_HDAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD1 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD2 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD3 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD4 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD5 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD6 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD7 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD8 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD9 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SEQNO, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.DI_REMARK_CD10 AS DI_REMARK_CD ");
            stb.append("     FROM ");
            stb.append("         ATTEND_PETITION_HDAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.HR_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("                                       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("                                       AND T2.GRADE = T1.GRADE ");
            stb.append("                                       AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.HR_NAME ");
            stb.append("     ) ");
            stb.append(" , T_MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T2.ATTENDDATE, ");
            stb.append("         L1.NAMESPARE1 AS DI_REMARK_NO ");
            stb.append("     FROM ");
            stb.append("         PETITION_HDAT T1 ");
            stb.append("         INNER JOIN PETITION_DAT T2 ON T2.SEQNO = T1.SEQNO ");
            stb.append("                                   AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         INNER JOIN V_NAME_MST L1 ON L1.YEAR = '" + _year + "' ");
            stb.append("                                 AND L1.NAMECD1 = 'C900' ");
            stb.append("                                 AND L1.NAMECD2 = T1.DI_REMARK_CD ");
            stb.append("                                 AND L1.NAMESPARE1 IS NOT NULL ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T2.ATTENDDATE, ");
            stb.append("         L1.NAMESPARE1 ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     T0.GRADE || T0.HR_CLASS AS GRADE_HR_CLASS, ");
            stb.append("     T0.HR_NAME, ");
            stb.append("     COUNT(DISTINCT L1.SCHREGNO) AS TOTAL, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '1' THEN 1 ELSE 0 END) AS CNT1, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '2' THEN 1 ELSE 0 END) AS CNT2, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '3' THEN 1 ELSE 0 END) AS CNT3, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '4' THEN 1 ELSE 0 END) AS CNT4, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '5' THEN 1 ELSE 0 END) AS CNT5, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '6' THEN 1 ELSE 0 END) AS CNT6, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '7' THEN 1 ELSE 0 END) AS CNT7, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '8' THEN 1 ELSE 0 END) AS CNT8 ");
            stb.append(" FROM ");
            stb.append("     T_SCHREG T0 ");
            stb.append("     LEFT JOIN T_MAIN L1 ON L1.SCHREGNO = T0.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T0.GRADE, ");
            stb.append("     T0.HR_CLASS, ");
            stb.append("     T0.HR_NAME ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T0.GRADE || '999' AS GRADE_HR_CLASS, ");
            stb.append("     '学年計' AS HR_NAME, ");
            stb.append("     COUNT(DISTINCT L1.SCHREGNO) AS TOTAL, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '1' THEN 1 ELSE 0 END) AS CNT1, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '2' THEN 1 ELSE 0 END) AS CNT2, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '3' THEN 1 ELSE 0 END) AS CNT3, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '4' THEN 1 ELSE 0 END) AS CNT4, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '5' THEN 1 ELSE 0 END) AS CNT5, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '6' THEN 1 ELSE 0 END) AS CNT6, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '7' THEN 1 ELSE 0 END) AS CNT7, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '8' THEN 1 ELSE 0 END) AS CNT8 ");
            stb.append(" FROM ");
            stb.append("     T_SCHREG T0 ");
            stb.append("     LEFT JOIN T_MAIN L1 ON L1.SCHREGNO = T0.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T0.GRADE ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '99999' AS GRADE_HR_CLASS, ");
            stb.append("     '総合計' AS HR_NAME, ");
            stb.append("     COUNT(DISTINCT L1.SCHREGNO) AS TOTAL, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '1' THEN 1 ELSE 0 END) AS CNT1, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '2' THEN 1 ELSE 0 END) AS CNT2, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '3' THEN 1 ELSE 0 END) AS CNT3, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '4' THEN 1 ELSE 0 END) AS CNT4, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '5' THEN 1 ELSE 0 END) AS CNT5, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '6' THEN 1 ELSE 0 END) AS CNT6, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '7' THEN 1 ELSE 0 END) AS CNT7, ");
            stb.append("     SUM(CASE WHEN L1.DI_REMARK_NO = '8' THEN 1 ELSE 0 END) AS CNT8 ");
            stb.append(" FROM ");
            stb.append("     T_SCHREG T0 ");
            stb.append("     LEFT JOIN T_MAIN L1 ON L1.SCHREGNO = T0.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE_HR_CLASS ");
            return stb.toString();
        }
    }

    /**
     * 集計の内部クラス
     */
    private class PetitionCnt {
        private final String _hrClassCd;
        private final String _hrClassName;
        private final String _total;
        private final String _cnt1;
        private final String _cnt2;
        private final String _cnt3;
        private final String _cnt4;
        private final String _cnt5;
        private final String _cnt6;
        private final String _cnt7;
        private final String _cnt8;

        private PetitionCnt(
                final String hrClassCd,
                final String hrClassName,
                final String total,
                final String cnt1,
                final String cnt2,
                final String cnt3,
                final String cnt4,
                final String cnt5,
                final String cnt6,
                final String cnt7,
                final String cnt8
        ) {
            _hrClassCd = hrClassCd;
            _hrClassName = hrClassName;
            _total = total;
            _cnt1 = cnt1;
            _cnt2 = cnt2;
            _cnt3 = cnt3;
            _cnt4 = cnt4;
            _cnt5 = cnt5;
            _cnt6 = cnt6;
            _cnt7 = cnt7;
            _cnt8 = cnt8;
        }

        private boolean isTotalLine() {
            String str = _hrClassCd.substring(2, 5);
            return "999".equals(str) ? true : false;
        }

        public String toString() {
            return "クラス：" + _hrClassCd + "：" + _hrClassName;
        }
    }
}

// eof

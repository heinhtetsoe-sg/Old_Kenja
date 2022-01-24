// kanji=漢字
/*
 * $Id: a2c5b707ddd30568a6aefdd59160edede9487657 $
 *
 * 作成日: 2014/08/19 14:10:02 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * @author m-yama
 * @version $Id: a2c5b707ddd30568a6aefdd59160edede9487657 $
 */
public class KNJTH140 {

    private static final Log log = LogFactory.getLog("KNJTH140.class");

    public static final String KOJIN = "1";
    public static final String SCHOOL = "2";

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map monthField = new HashMap();
        monthField.put("04", "1");
        monthField.put("05", "2");
        monthField.put("06", "3");
        monthField.put("07", "4");
        monthField.put("08", "5");
        monthField.put("09", "6");
        monthField.put("10", "7");
        monthField.put("11", "8");
        monthField.put("12", "9");
        monthField.put("01", "10");
        monthField.put("02", "11");
        monthField.put("03", "12");

        int kiFutanTotal = 0;
        int henkouGoTotal = 0;
        int sabunTotal = 0;

        int total1 = 0;
        int total2 = 0;
        int total3 = 0;

        String befShugakuNo = "";
        final String printSql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            svf.VrSetForm("KNJTH140.frm", 1);
            Map furikomiDate = new HashMap();
            while (rs.next()) {
                if (befShugakuNo != "" && !befShugakuNo.equals(rs.getString("SHUUGAKU_NO"))) {
                    furikomiDate = new HashMap();
                    svf.VrEndPage();
                }
                svf.VrsOut("NAME", rs.getString("KOJIN_NAME"));

                svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));

                final String moneyField = (String) monthField.get(rs.getString("MONTH"));
                svf.VrsOutn("MONEY" + moneyField, 1, rs.getString("SHIHARAI_PLAN_GK"));
                kiFutanTotal += null != rs.getString("SHIHARAI_PLAN_GK") ? rs.getInt("SHIHARAI_PLAN_GK") : 0;
                svf.VrsOutn("MONEY" + moneyField, 2, rs.getString("SHISHUTSU_YOTEI_GK"));
                henkouGoTotal += null != rs.getString("SHISHUTSU_YOTEI_GK") ? rs.getInt("SHISHUTSU_YOTEI_GK") : 0;
                svf.VrsOutn("MONEY" + moneyField, 3, rs.getString("SABUN"));
                sabunTotal += null != rs.getString("SABUN") ? rs.getInt("SABUN") : 0;

                svf.VrsOutn("TOTAL_MONEY", 1, String.valueOf(kiFutanTotal));
                svf.VrsOutn("TOTAL_MONEY", 2, String.valueOf(henkouGoTotal));
                svf.VrsOutn("TOTAL_MONEY", 3, String.valueOf(sabunTotal));

                final String shiharaiKi = rs.getString("SHIHARAI_KI");
                if ("1".equals(shiharaiKi)) {
                    total1 += rs.getInt("SHISHUTSU_GK");
                }
                if ("2".equals(shiharaiKi)) {
                    total2 += rs.getInt("SHISHUTSU_GK");
                }
                if ("3".equals(shiharaiKi)) {
                    total3 += rs.getInt("SHISHUTSU_GK");
                }
                svf.VrsOut("REAL_MONEY1", String.valueOf(total1));
                svf.VrsOut("REAL_MONEY2", String.valueOf(total2));
                svf.VrsOut("REAL_MONEY3", String.valueOf(total3));
                svf.VrsOut("REAL_MONEY4", String.valueOf(total1 + total2 + total3));

                if (null != rs.getString("FURIKOMI_DATE")) {
                    furikomiDate.put(rs.getString("FURIKOMI_DATE"), rs.getString("FURIKOMI_DATE"));
                    int shiharaiKiCnt = 1;
                    for (final Iterator iter = furikomiDate.keySet().iterator(); iter.hasNext();) {
                        final String furikomiBi = (String) iter.next();
                        svf.VrsOut("DATE" + shiharaiKi + "_" + String.valueOf(shiharaiKiCnt), KNJ_EditDate.h_format_JP(furikomiBi));
                        shiharaiKiCnt++;
                    }
                }
                befShugakuNo = rs.getString("SHUUGAKU_NO");
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  WITH MXSEQ_KSKDAT AS ( ");
        stb.append("    SELECT ");
        stb.append("      T0.* ");
        stb.append("    FROM ");
        stb.append("     KOJIN_SHINSEI_KYUHU_DAT T0 ");
        stb.append("    WHERE ");
        stb.append("      T0.SEQ = (SELECT MAX(J0.SEQ) FROM KOJIN_SHINSEI_KYUHU_DAT J0 WHERE J0.KOJIN_NO = T0.KOJIN_NO AND J0.SHINSEI_YEAR = T0.SHINSEI_YEAR) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     CONCAT(CONCAT(KOJIN.FAMILY_NAME, '　'), KOJIN.FIRST_NAME) AS KOJIN_NAME, ");
        stb.append("     KEIKAKU.YEAR, ");
        stb.append("     KEIKAKU.MONTH, ");
        stb.append("     KEIKAKU.SHIHARAI_PLAN_GK, ");
        stb.append("     KEIKAKU.SHISHUTSU_YOTEI_GK, ");
        stb.append("     VALUE(KEIKAKU.SHISHUTSU_YOTEI_GK, 0) - VALUE(KEIKAKU.SHIHARAI_PLAN_GK, 0) AS SABUN, ");
        stb.append("     CASE WHEN FURIKOMI_DATE IS NOT NULL ");
        stb.append("          THEN VALUE(KEIKAKU.SHISHUTSU_GK, 0) ");
        stb.append("          ELSE 0 ");
        stb.append("     END AS SHISHUTSU_GK, ");
        stb.append("     KEIKAKU.FURIKOMI_DATE, ");
        stb.append("     T035.NAMECD2 AS SHIHARAI_KI, ");
        stb.append("     T1.KETTEI_DATE AS PUT_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_GENGAKU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN MXSEQ_KSKDAT KYUHU ON T1.SHINSEI_YEAR = KYUHU.SHINSEI_YEAR ");
        stb.append("          AND T1.KOJIN_NO = KYUHU.KOJIN_NO ");
        stb.append("     LEFT JOIN TAIYO_KEIKAKU_DAT KEIKAKU ON T1.SHUUGAKU_NO = KEIKAKU.SHUUGAKU_NO ");
        stb.append("          AND T1.SHINSEI_YEAR = KEIKAKU.SHINSEI_YEAR ");
        stb.append("     LEFT JOIN NAME_MST T035 ON T035.NAMECD1 = 'T035' ");
        stb.append("          AND KEIKAKU.MONTH BETWEEN T035.NAME1 AND NAME2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SHINSEI_YEAR = '" + _param._shinseiYear + "' ");
        if (KOJIN.equals(_param._classDiv)) {
            stb.append("     AND T1.KOJIN_NO = '" + _param._kojinNo + "' ");
        }
        stb.append("     AND VALUE(T1.KETTEI_FLG, '0') = '1' ");
        stb.append("     AND T1.KETTEI_DATE IS NOT NULL ");
        if (SCHOOL.equals(_param._classDiv)) {
            stb.append("     AND T1.GENGAKU_UKE_YEAR || '-' || T1.GENGAKU_UKE_NO || '-' ||  T1.GENGAKU_UKE_EDABAN = '" + _param._uke + "' ");
            stb.append("     AND KYUHU.H_SCHOOL_CD IN " + _param._schoolInState + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     KYUHU.H_SCHOOL_CD, ");
        stb.append("     KEIKAKU.YEAR, ");
        stb.append("     KEIKAKU.MONTH ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74827 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _classDiv;
        private final String _uke;
        private String _schoolInState;
        private final String _kojinNo;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _classDiv = request.getParameter("CLASS_DIV");
            _uke = request.getParameter("UKE");

            if (SCHOOL.equals(_classDiv)) {
                final String schools[] = request.getParameterValues("SCHOOL_SELECTED");
                String schoolInState = "( ";
                String sep = "";
                for (int ia = 0; ia < schools.length; ia++) {
                    schoolInState += sep + "'" + schools[ia] + "'";
                    sep = ", ";
                }
                _schoolInState = schoolInState + " )";
            }
            _kojinNo = request.getParameter("KOJIN_NO");
            _ctrlDate = request.getParameter("LOGIN_DATE");
        }

    }
}

// eof

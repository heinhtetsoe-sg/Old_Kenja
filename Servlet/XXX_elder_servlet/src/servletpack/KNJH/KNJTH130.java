// kanji=漢字
/*
 * $Id: bd1cd22cb4047aac7098cb5ea1771e11915f60f5 $
 *
 * 作成日: 2014/08/18 15:03:23 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: bd1cd22cb4047aac7098cb5ea1771e11915f60f5 $
 */
public class KNJTH130 {

    private static final Log log = LogFactory.getLog("KNJTH130.class");

    public static final String KOJIN = "1";
    public static final String SCHOOL = "2";

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

    private void printAddress(final Vrw32alp svf, final String addr1, final String addr2) {
        final String[] addr1a = KNJ_EditEdit.get_token(addr1, 50, 2);
        final String[] addr2a = KNJ_EditEdit.get_token(addr2, 50, 2);
        final List addr = new ArrayList();
        if (null != addr1a && !StringUtils.isBlank(addr1a[0])) addr.add(addr1a[0]);
        if (null != addr1a && !StringUtils.isBlank(addr1a[1])) addr.add(addr1a[1]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[0])) addr.add(addr2a[0]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[1])) addr.add(addr2a[1]);
        final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
        for (int j = 0; j < addr.size(); j++) {
            svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String printSql = getPrintSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            svf.VrSetForm("KNJTH130.frm", 1);
            while (rs.next()) {
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                printAddress(svf, rs.getString("ADDR1"), rs.getString("ADDR2"));
                svf.VrsOut("NAME", rs.getString("KOJIN_NAME"));

                svf.VrsOut("CERT_NO", getBunshoBangou(rs)); // 証明書番号
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(rs.getString("PUT_DATE")));

                svf.VrsOut("APPLI_NO", rs.getString("SHUUGAKU_NO")); // 知事名

                svf.VrsOut("DAY1", KNJ_EditDate.h_format_JP(rs.getString("PUT_DATE")));

                //決定
                svf.VrsOut("MONEY", rs.getString("HENKOUGO_TAIYOGK"));

                svf.VrsOut("FROM", KNJ_EditDate.h_format_JP_M(rs.getString("S_KEIKAKU_HENKOU_YM") + "-01"));
                svf.VrsOut("TO", KNJ_EditDate.h_format_JP_M(rs.getString("E_KEIKAKU_HENKOU_YM") + "-01"));

                svf.VrsOut("CHARGE", "京都府教育庁指導部高校教育課");
                svf.VrsOut("FIELD1", "(075)574-7518");
                _hasData = true;
                svf.VrEndPage();
            }
        } catch (final Exception e) {
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
        stb.append("     KOJIN.ZIPCD, ");
        stb.append("     KOJIN.ADDR1, ");
        stb.append("     KOJIN.ADDR2, ");
        stb.append("     T1.GENGAKU_UKE_YEAR, ");
        stb.append("     T1.GENGAKU_UKE_NO, ");
        stb.append("     T1.GENGAKU_UKE_EDABAN, ");
        stb.append("     T1.HENKOUGO_TAIYOGK, ");
        stb.append("     T1.S_KEIKAKU_HENKOU_YM, ");
        stb.append("     T1.E_KEIKAKU_HENKOU_YM, ");
        stb.append("     T1.KETTEI_DATE AS PUT_DATE ");
        stb.append(" FROM ");
        stb.append("     KOJIN_SHINSEI_GENGAKU_DAT T1 ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT KOJIN ON T1.KOJIN_NO = KOJIN.KOJIN_NO ");
        stb.append("     LEFT JOIN MXSEQ_KSKDAT KYUHU ON T1.SHINSEI_YEAR = KYUHU.SHINSEI_YEAR ");
        stb.append("          AND T1.KOJIN_NO = KYUHU.KOJIN_NO ");
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
        stb.append("     T1.KOJIN_NO ");

        return stb.toString();
    }

    public String getBunshoBangou(final ResultSet rs) {
        try {
            final String gengou = KenjaProperties.gengou(Integer.parseInt(rs.getString("GENGAKU_UKE_YEAR")));
            final String wa = (gengou.length() < 1) ? "" : gengou.substring(gengou.length() - 1);
            final String bangou = (null == rs.getString("GENGAKU_UKE_NO")) ? "" : String.valueOf(Integer.parseInt(rs.getString("GENGAKU_UKE_NO")));
            final String edaban = (null ==rs.getString("GENGAKU_UKE_EDABAN") || Integer.parseInt(rs.getString("GENGAKU_UKE_EDABAN")) == 1) ? "" : ("の" + Integer.parseInt(rs.getString("GENGAKU_UKE_EDABAN")));
            return wa + "教高第" + bangou + "号" + edaban;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return null;
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
        log.fatal("$Revision: 74826 $");
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
        final String _chijiName;

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
            _chijiName = getChijiName(db2);
        }

        private String getChijiName(DB2UDB db2) {
            String name = null;
            final String sql = " SELECT VALUE(CHIJI_YAKUSHOKU_NAME, '') || '　　' || VALUE(CHIJI_NAME, '') AS CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST) ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("CHIJI_NAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

    }
}

// eof

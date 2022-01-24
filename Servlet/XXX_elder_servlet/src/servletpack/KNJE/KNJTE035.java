/*
 * $Id: d8e3161463dc19b5c66248952686424eb6b15256 $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 返還猶予決定通知書
 */
public class KNJTE035 {

    private static final Log log = LogFactory.getLog(KNJTE035.class);

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
    
    
    private void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }
    
    
    public String getBunshoBangou(final Param param, final Map row) {
        try {
            final String wa = param._shugakuDate.getUkeYearNum(KnjDbUtils.getString(row, "UKE_YEAR"));
            final String bangou = (null == KnjDbUtils.getString(row, "UKE_NO")) ? "" : String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "UKE_NO")));
            final String edaban = (null ==KnjDbUtils.getString(row, "UKE_EDABAN") || Integer.parseInt(KnjDbUtils.getString(row, "UKE_EDABAN")) == 1) ? "" : ("の" + Integer.parseInt(KnjDbUtils.getString(row, "UKE_EDABAN")));
            return wa + "教高第" + bangou + "号" + edaban;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return null;
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
        final String form;
        if ("KNJTE038".equals(_param._prgid)) {
            form = "1".equals(_param._shikinShubetsu) ? "KNJTE038.frm" : "KNJTE039.frm";
        } else if ("KNJTE037".equals(_param._prgid)) {
            form = "KNJTE037.frm";
        } else {
            form = "1".equals(_param._shikinShubetsu) ? "KNJTE035_1.frm" : "KNJTE035_2.frm";
        }
        final String sql = sql();
        log.debug(" sql =" + sql);
        
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();

            svf.VrSetForm(form, 1);
            svf.VrsOut("ZIP_NO", StringUtils.defaultString(KnjDbUtils.getString(row, "ZIPCD"))); // 郵便番号
            printAddress(svf, KnjDbUtils.getString(row, "ADDR1"), KnjDbUtils.getString(row, "ADDR2"));
//            svf.VrsOut("ADDRESS1", KnjDbUtils.getString(row, "ADDR1")); // 住所
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "ADDR2"), 40, 2), svf);
            svf.VrsOut("NAME", KnjDbUtils.getString(row, "KOJIN_NAME")); // 氏名

            svf.VrsOut("CERT_NO", getBunshoBangou(_param, row)); // 証明書番号
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._outputDate, false)); // 日付
            
            if ("KNJTE037".equals(_param._prgid)) {
                svf.VrsOut("GOVERNER", _param._chijiName); // 知事名
            }

            svf.VrsOut("APPLI_NO", KnjDbUtils.getString(row, "SHUUGAKU_NO")); // 申込番号
            svf.VrsOut("DAY1", _param._shugakuDate.formatDate(KnjDbUtils.getString(row, "SHINSEI_DATE"), false)); // 日付
            
            if ("KNJTE038".equals(_param._prgid)) {
                svf.VrsOut("REASON", KnjDbUtils.getString(row, "YUYO_BIKOU")); // 理由
            } else {
                svf.VrsOut("FROM", _param._shugakuDate.formatNentuki(KnjDbUtils.getString(row, "S_YUYO_YM"))); // 期間From
                svf.VrsOut("TO", _param._shugakuDate.formatNentuki(KnjDbUtils.getString(row, "E_YUYO_YM"))); // 期間To
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CASE WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
        stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
        stb.append("          ELSE V1.ZIPCD ");
        stb.append("     END AS ZIPCD, ");
        stb.append("     CASE WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
        stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
        stb.append("          ELSE V1.ADDR1 ");
        stb.append("     END AS ADDR1, ");
        stb.append("     CASE WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
        stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
        stb.append("          ELSE V1.ADDR2 ");
        stb.append("     END AS ADDR2, ");
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME");
        stb.append("              WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME");
        stb.append("              ELSE CONCAT(CONCAT(V1.FAMILY_NAME,'　'),V1.FIRST_NAME)");
        stb.append("     END AS KOJIN_NAME,");
        stb.append("     T2.SHIKIN_SHUBETSU, ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.SHINSEI_DATE, ");
        stb.append("     T1.UKE_YEAR, ");
        stb.append("     T1.UKE_NO, ");
        stb.append("     T1.UKE_EDABAN, ");
        stb.append("     T1.S_YUYO_YM, ");
        stb.append("     T1.E_YUYO_YM, ");
        stb.append("     T1.YUYO_BIKOU ");
        stb.append(" FROM ");
        stb.append("     YUYO_DAT T1 ");
        stb.append("     LEFT JOIN SAIKEN_DAT T2 ON T1.SHUUGAKU_NO = T2.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V1 ON T2.KOJIN_NO = V1.KOJIN_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON T2.KOJIN_NO = S1.KOJIN_NO AND T2.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON T2.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        stb.append(" WHERE ");
        stb.append("      T2.SHIKIN_SHUBETSU = '" + _param._shikinShubetsu + "' AND ");
        if ("1".equals(_param._selectDiv)) {
            stb.append("     T1.UKE_YEAR = '" + _param._ukeYear + "' AND ");
            stb.append("     T1.UKE_NO = '" + _param._ukeNo + "' AND ");
            if ("001".equals(_param._ukeEdaban)) {
                stb.append("     (T1.UKE_EDABAN IS NULL OR T1.UKE_EDABAN = '" + _param._ukeEdaban + "') AND ");
            } else {
                stb.append("     T1.UKE_EDABAN = '" + _param._ukeEdaban + "' AND ");
            }
        } else if ("2".equals(_param._selectDiv) && !StringUtils.isBlank(_param._shuugakuNo)) {
            stb.append("     T1.SHUUGAKU_NO = '" + _param._shuugakuNo + "' AND ");
        }
        stb.append("     T1.YUYO_KEKKA_CD = '" + _param._yuyoKekkaCd + "' AND ");
        stb.append("     T1.UPD_KUBUN = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.UKE_YEAR, T1.UKE_NO, T1.UKE_EDABAN, T1.SHUUGAKU_NO ");
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 67235 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _shikinShubetsu;
        final String _selectDiv;
        final String _shuugakuNo;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _outputDate;
        final String _prgid;
        final String _yuyoKekkaCd;
        final String _chijiName;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shuugakuNo = request.getParameter("SHUUGAKU_NO");
            _selectDiv = request.getParameter("SELECT_DIV");
            _prgid = request.getParameter("PRGID");
            _shikinShubetsu = "KNJTE037".equals(_prgid) ? "2" : request.getParameter("SHIKIN_SHUBETSU"); // 1:修学金、2:支度金 KNJTE037は支度金のみ
            _yuyoKekkaCd = "KNJTE038".equals(_prgid) ? "3" : "2"; // 1:申請中、2:承認、3:不承認
            _ukeYear = request.getParameter("UKE_YEAR");
            _ukeNo = request.getParameter("UKE_NO");
            _ukeEdaban = StringUtils.isBlank(request.getParameter("UKE_EDABAN")) ? "001" : request.getParameter("UKE_EDABAN");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _outputDate = _shugakuDate.d7toDateStr(request.getParameter("OUTPUT_DATE"));
            _chijiName = _shugakuDate.getChijiName3(db2);
        }
    }
}

// eof


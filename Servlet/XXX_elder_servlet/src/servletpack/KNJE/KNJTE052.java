/*
 * $Id: 31058e2b48680c32cd5d22033722c3121bd17f9b $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 口座振替依頼書/納付書
 */
public class KNJTE052 {

    private static final Log log = LogFactory.getLog(KNJTE052.class);

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
        final String sql = sql();
        log.info(" sql =" + sql);
        
        int page = 0;
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            page += 1;
            
            final String nendo = _param._shugakuDate.getNen(_param._ctrlYear);
            final String system = "334";
            final String kaikei = "01";
            final String shuusi = "1";
            final String yosan = "1";
            final String kamokuId = "002508";
            final String shuunou = "21";
            final String noufuBangou = "1" + "000000000000" + _param._bankcd + KnjDbUtils.getString(row, "SHIKIN_SHUBETSU");
            final String count = KnjDbUtils.getString(row, "COUNT");
            final String henkanGk = KnjDbUtils.getString(row, "HENKAN_GK");
            final String bankname = KnjDbUtils.getString(row, "BANKNAME");
            final String nounyusha = "00000000";
            final String biko = "00000000000";
            String furikoaeNaiyou = "";
            if ("1".equals(KnjDbUtils.getString(row, "SHIKIN_SHUBETSU"))) {
                furikoaeNaiyou = "高等学校等修学金返還金";
            } else if ("2".equals(KnjDbUtils.getString(row, "SHIKIN_SHUBETSU"))) {
                furikoaeNaiyou = "高等学校等修学支度金返還金";
            }
            
            svf.VrSetForm("KNJTE052_1.frm", 1);
            svf.VrsOut("TITLE1", "京都府　口座振替依頼書・納付書"); // タイトル
            svf.VrsOut("TITLE2", "京都府　収納済通知書"); // タイトル
            svf.VrsOut("TITLE3", "京都府　収納書"); // タイトル
            svf.VrsOut("SYSTEM1", system); // システム区分
            svf.VrsOut("NENDO1", nendo); // 年度
            svf.VrsOut("ACCOUNT1", kaikei); // 会計
            svf.VrsOut("INOUT1", shuusi); // 収支
            svf.VrsOut("BUDGET1", yosan); // 予算
            svf.VrsOut("SUBJECT_ID1", kamokuId); // 科目ID
            svf.VrsOut("STORAGE1", shuunou); // 収納区分
            svf.VrsOut("BANK_NAME1", bankname);
            svf.VrsOut("COUNT1", count); // 件数
            svf.VrsOut("MONEY1", henkanGk); // 金額
            svf.VrsOut("DELIVERY1", nounyusha); // 納入者
            svf.VrsOut("REMARK1", biko); // 備考
            svf.VrsOut("PAY_NO1", noufuBangou); // 納付番号
            svf.VrsOut("PAY_NAME1", furikoaeNaiyou); // 振替内容
            svf.VrsOut("LIMIT_DAY1", _param._shugakuDate.formatDate(_param._furikomiDate, false)); // 振替指定日
            svf.VrsOut("PRINT_DAY1", _param._shugakuDate.formatDate(_param._tsuuchiDate, false)); // 納入通知日
            svf.VrsOut("JOB_NAME2", _param._chijiName); // 京都府知事
            svf.VrsOut("PAGE", String.valueOf(page)); // 京都府知事
            
            svf.VrEndPage();
            
            _hasData = true;
        }
	}

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append("  SELECT ");
        stb.append("       V1.BANKCD, ");
        stb.append("       T1.SHIKIN_SHUBETSU, ");
        stb.append("       MAX(T2.NAME1) AS SHUGAKU_ITAKU_CD, ");
        stb.append("       MAX(T2.NAME2) AS SHITAKU_ITAKU_CD, ");
        stb.append("       MAX(T2.NAME3) AS FILENAME, ");
        stb.append("       COUNT(*) AS COUNT, ");
        stb.append("       SUM(HENKAN_GK) AS HENKAN_GK ");
        stb.append("   FROM ");
        stb.append("       V_CHOTEI_NOUFU V1 ");
        stb.append("       LEFT JOIN SAIKEN_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
        stb.append("       LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'T044' AND T2.NAMECD2 = V1.BANKCD ");
        stb.append("   WHERE ");
        stb.append("       V1.SHUNO_FLG = '0' AND ");
        stb.append("       V1.NOUFU_KIGEN_ORG = '" + _param._furikomiDate + "' AND ");
        stb.append("       V1.BANKCD = '" + _param._bankcd + "' AND ");
        stb.append("       V1.SHIHARAI_HOHO_CD = '1' AND ");
        stb.append("       V1.TORIKESI_FLG = '0'  ");
        stb.append("   GROUP BY ");
        stb.append("       V1.BANKCD, ");
        stb.append("       T1.SHIKIN_SHUBETSU ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("      T1.BANKCD, ");
        stb.append("      T1.SHIKIN_SHUBETSU, ");
        stb.append("      T1.SHUGAKU_ITAKU_CD, ");
        stb.append("      T1.SHITAKU_ITAKU_CD, ");
        stb.append("      T1.FILENAME, ");
        stb.append("      T1.COUNT, ");
        stb.append("      T1.HENKAN_GK, ");
        stb.append("      T3.BANKNAME ");
        stb.append(" FROM ");
        stb.append("      MAIN T1 ");
        stb.append("      LEFT JOIN (SELECT BANKCD, MIN(BANKNAME) AS BANKNAME ");
        stb.append("                 FROM BANK_MST T3 ");
        stb.append("                 GROUP BY BANKCD) T3 ON T3.BANKCD = T1.BANKCD ");
        stb.append(" ORDER BY ");
        stb.append("      T1.SHIKIN_SHUBETSU ");
	    return stb.toString();
	}

	/** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67238 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _bankcd;
        final String _furikomiDate;
        final String _tsuuchiDate;
        final String _prgid;
        final String _chijiName;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _prgid = request.getParameter("PRGID");
            _bankcd = request.getParameter("BANKCD");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _furikomiDate = _shugakuDate.d7toDateStr(request.getParameter("FURIKOMI_DATE"));
            _tsuuchiDate = _shugakuDate.d7toDateStr(request.getParameter("TSUUCHI_DATE"));
            _chijiName = _shugakuDate.getChijiName3(db2);
        }
    }
}

// eof


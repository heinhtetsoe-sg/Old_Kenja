/*
 * $Id: 35d85193e6fe31520d9c344217a28925b817a7df $
 *
 * 作成日: 2019/02/13
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP728 {

    private static final Log log = LogFactory.getLog(KNJP728.class);

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
        svf.VrSetForm("KNJP728.frm", 1);

        NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            // 郵便番号
            svf.VrsOut("ZIP_NO", printData._zipCd);

            // 住所
            final String addr1Idx = 50 < KNJ_EditEdit.getMS932ByteLength(printData._addr1) ? "4": 40 < KNJ_EditEdit.getMS932ByteLength(printData._addr1) ? "3": 30 < KNJ_EditEdit.getMS932ByteLength(printData._addr1) ? "2": "1";
            final String addr2Idx = 50 < KNJ_EditEdit.getMS932ByteLength(printData._addr2) ? "4": 40 < KNJ_EditEdit.getMS932ByteLength(printData._addr2) ? "3": 30 < KNJ_EditEdit.getMS932ByteLength(printData._addr2) ? "2": "1";
            svf.VrsOut("ADDR1_" + addr1Idx, printData._addr1);
            svf.VrsOut("ADDR2_" + addr2Idx, printData._addr2);

            // 住所用氏名
            final String setName1 = printData._name + "　様";
            final String setName2 = printData._gName + "　様";
            final String addrNameIdx  = 28 < KNJ_EditEdit.getMS932ByteLength(setName1) ? "2": "1";
            final String addrNameIdx2 = 28 < KNJ_EditEdit.getMS932ByteLength(setName2) ? "2": "1";
            svf.VrsOut("ADDR_NAME1_" + addrNameIdx, setName1);
            svf.VrsOut("ADDR_NAME2_" + addrNameIdx2, setName2);

            // 日付
            final String date = KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);
            svf.VrsOut("DATE", date);

            // 学校名
            svf.VrsOut("SCHOOL_NAME1", _param._schoolName);
            svf.VrsOut("SCHOOL_NAME2", _param._schoolName);

            // タイトル
            final String nendo    = KNJ_EditDate.h_format_JP_N(db2, _param._year + "-04-01");
            String shisetsu = "";
            if (_param._isFresh) {
                shisetsu = "";
            } else {
                if ("04".equals(_param._paidLimitMonth) || printData._isDoyou) {
                    shisetsu = "（年間施設費）";
                } else {
                    shisetsu = "";
                }
            }
            final String title = nendo + "度　授業料" + shisetsu + "納入について";
            svf.VrsOut("TITLE", title);

            // 本文
            String moneyDivT = "";
            if (printData._isDoyou) {
                moneyDivT = "授業料";
            } else {
                if ("04".equals(_param._paidLimitMonth)) {
                    moneyDivT = "前期授業料";
                } else {
                    moneyDivT = "後期授業料";
                }
            }
            String shisetsuT = "";
            if (_param._isFresh) {
                shisetsuT = "";
            } else {
                if ("04".equals(_param._paidLimitMonth) || printData._isDoyou) {
                    shisetsuT = "と年間施設費";
                } else {
                    shisetsuT = "";
                }
            }
            final String text = "さて、" + nendo + "度" + moneyDivT + shisetsuT + "が右記の通りとなっております。";
            svf.VrsOut("TEXT", text);

            // 納入期限
            final String limitDate   = KNJ_EditDate.h_format_JP(db2, _param._limitDate);
            final String[] limitArr  = KNJ_EditDate.tate2_format(limitDate);
            final String youbi       = KNJ_EditDate.h_format_W(_param._limitDate);
            final String setLimitTxt = limitArr[0] + " " + limitArr[1] + " " + limitArr[2] + " " + limitArr[3] + " " + limitArr[4] + " " + limitArr[5] + " " + limitArr[6] +  " (" + youbi + ")";
            svf.VrsOut("LIMIT", setLimitTxt);

            // 学費タイトル
            final String Moneytitle = "［" + nendo + "度学費］";
            svf.VrsOut("MONEY_TITLE", Moneytitle);

            // 授業料タイトル
            final String jugyouRyotitle = (printData._isDoyou) ? "授業料": "前期授業料";
            svf.VrsOut("TUITION_NAME", jugyouRyotitle);

            // 元号名セット
            final String gengo = KNJ_EditDate.h_format_JP_N(db2, _param._limitDate).substring(0, 2);
            svf.VrsOut("ERA_NAME", gengo);
            svf.VrsOut("ERA_NAME2", gengo);
            svf.VrsOut("ERA_NAME3", gengo);

            // 授業料名称
            final String month = String.valueOf(Integer.parseInt(_param._paidLimitMonth));
            svf.VrsOut("TUITION_NAME", "授業料(納期限" + month + "月)");
            // 授業料等セット
            int jugyoRyo = 0;
            if (printData._isDoyou) {
                svf.VrsOut("CREDIT", String.valueOf(printData._moneyData._colectCnt));
                svf.VrsOut("CREDIT2", "×");
                svf.VrsOut("PRICE", String.valueOf(printData._moneyData._jugyouMoneyTani));
                svf.VrsOut("CREDIT3", "＝");
                jugyoRyo = printData._moneyData._jugyouMoney;
            } else {
                jugyoRyo = printData._moneyData._jugyouMoney;
            }
            // 授業料
            svf.VrsOut("TUITION", jugyoRyo == 0 ? "": String.valueOf(nfNum.format(jugyoRyo)));
            // 施設費名称
            svf.VrsOut("FACILITY_NAME", printData._moneyData._colectMname);
            // 施設費
            svf.VrsOut("FACILITY", printData._moneyData._shisetsuMoney == 0 ? "": String.valueOf(nfNum.format(printData._moneyData._shisetsuMoney)));
            // 就学支援金
            final String minusStr = printData._moneyData._syugakuMoney > 0 ? "△": "";
            svf.VrsOut("SUPPORT", printData._moneyData._syugakuMoney == 0 ? "": minusStr + String.valueOf(nfNum.format(printData._moneyData._syugakuMoney)));
            // 合計
            svf.VrsOut("TOTAL", printData._moneyData._totalMoney == 0 ? "": String.valueOf(nfNum.format(printData._moneyData._totalMoney)));

            // 下部（学費納入のご案内）
            svf.VrsOut("PRICE1", String.valueOf(printData._moneyData._totalMoney));
            svf.VrsOut("EXAM_NO1", printData._schregNo);
            final String nameIdx1  = 30 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "2": "1";
            svf.VrsOut("NAME1_" + nameIdx1, printData._name);
            svf.VrsOut("LIMIT1", limitDate);

            // 下部（振込依頼書）
            svf.VrsOut("PRICE2", String.valueOf(printData._moneyData._totalMoney));
            svf.VrsOut("EXAM_NO2", printData._schregNo);
            final String nameIdx2  = 30 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "3": 16 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "2": "1";
            svf.VrsOut("NAME2_" + nameIdx2, printData._name);

            svf.VrsOut("PRICE3", String.valueOf(printData._moneyData._totalMoney));
            svf.VrsOut("EXAM_NO3", printData._schregNo);
            final String name3kanaIdx  = 30 < KNJ_EditEdit.getMS932ByteLength(printData._kana) ? "3": 20 < KNJ_EditEdit.getMS932ByteLength(printData._kana) ? "2": "1";
            svf.VrsOut("KANA" + name3kanaIdx, printData._kana);
            final String name3Idx  = 30 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "3": 20 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "2": "1";
            svf.VrsOut("NAME3_" + name3Idx, printData._name);

            svf.VrEndPage();
            _hasData = true;
        }
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

            String befSlipNo = "";
            int totalMoney = 0;
            PrintData printData = null;

            while (rs.next()) {
                final String slipNo         = rs.getString("SLIP_NO");
                final String schregNo       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String kana           = rs.getString("KANA");
                final String gName          = rs.getString("GNAME");
                final String zipCd          = rs.getString("ZIPCD");
                final String addr1          = rs.getString("ADDR1");
                final String addr2          = rs.getString("ADDR2");
                final boolean isDoyou      = "1".equals(rs.getString("IS_DOYOU")); // 土曜コース
                final boolean isJugyouRyou = "1".equals(rs.getString("GAKUNOKIN_DIV"));
                final String colectMname    = rs.getString("COLLECT_M_NAME");
                final int colectCnt         = rs.getInt("COLLECT_CNT");
                final int colectMoney       = rs.getInt("COLLECT_MONEY");
                final int colectMMoney      = rs.getInt("COLLECT_M_MONEY");
                final int reducMoney        = rs.getInt("REDUC_MONEY");

                if (!befSlipNo.equals(slipNo)) {
                    printData = new PrintData(schregNo, name, kana, gName, zipCd, addr1, addr2, isDoyou);
                    retList.add(printData);

                    totalMoney = 0; // 初期化
                }
                totalMoney += colectMoney;
                if (isJugyouRyou) {
                    printData._moneyData._colectCnt        = colectCnt;    // 単位数
                    printData._moneyData._jugyouMoneyTani  = colectMMoney; // 授業料(１単位)単位制で使用
                    printData._moneyData._jugyouMoney      = colectMoney;  // 授業料
                    totalMoney -= reducMoney; // 合計から就学支援金を引く
                } else {
                    if (null == printData._moneyData._colectMname) {
                        printData._moneyData._colectMname   = colectMname; // 項目名セット ※最初の項目のみセット
                        printData._moneyData._shisetsuMoney = colectMoney; // 施設費 ※最初の項目のみセット
                    }
                }
                //就学支援金
                printData._moneyData._syugakuMoney = reducMoney;
                //合計
                printData._moneyData._totalMoney = totalMoney;

                befSlipNo = slipNo;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        // 就学支援金データ
        stb.append("  SYUGAKU AS ( ");
        stb.append("     SELECT ");
        stb.append("         REDC.SLIP_NO, ");
        stb.append("         sum(value(REDC.PLAN_MONEY, 0)) + sum(value(REDC.ADD_PLAN_MONEY, 0)) AS REDUC_MONEY ");
        stb.append("     FROM ");
        stb.append("         REDUCTION_COUNTRY_PLAN_DAT REDC ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SLIP ON SLIP.SCHOOLCD    = REDC.SCHOOLCD ");
        stb.append("                                        AND SLIP.SCHOOL_KIND = REDC.SCHOOL_KIND ");
        stb.append("                                        AND SLIP.YEAR        = REDC.YEAR ");
        stb.append("                                        AND SLIP.SLIP_NO     = REDC.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON LIMI.SCHOOLCD    = REDC.SCHOOLCD ");
        stb.append("                                                       AND LIMI.SCHOOL_KIND = REDC.SCHOOL_KIND ");
        stb.append("                                                       AND LIMI.YEAR        = REDC.YEAR ");
        stb.append("                                                       AND LIMI.SCHREGNO    = REDC.SCHREGNO ");
        stb.append("                                                       AND LIMI.SLIP_NO     = REDC.SLIP_NO ");
        stb.append("                                                       AND LIMI.PLAN_YEAR   = REDC.PLAN_YEAR ");
        stb.append("                                                       AND LIMI.PLAN_MONTH  = REDC.PLAN_MONTH ");
        stb.append("     WHERE ");
        stb.append("             REDC.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND REDC.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         AND REDC.YEAR        = '" + _param._year + "' ");
        stb.append("         AND SLIP.CANCEL_DATE is null ");
        stb.append("         AND LIMI.PAID_LIMIT_MONTH = '" + _param._paidLimitMonth + "' ");
        stb.append("     GROUP BY ");
        stb.append("         REDC.SCHOOLCD, ");
        stb.append("         REDC.SCHOOL_KIND, ");
        stb.append("         REDC.YEAR, ");
        stb.append("         REDC.SLIP_NO ");
        // 未支払い伝票
        stb.append(" ), NO_PAIND_SLIP AS ( ");
        stb.append("     SELECT ");
        stb.append("         PLAN_M.SLIP_NO ");
        stb.append("     FROM ");
        stb.append("         COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SLIP ON SLIP.SCHOOLCD    = PLAN_M.SCHOOLCD ");
        stb.append("                                        AND SLIP.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
        stb.append("                                        AND SLIP.YEAR        = PLAN_M.YEAR ");
        stb.append("                                        AND SLIP.SLIP_NO     = PLAN_M.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON LIMI.SCHOOLCD    = PLAN_M.SCHOOLCD ");
        stb.append("                                                       AND LIMI.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
        stb.append("                                                       AND LIMI.YEAR        = PLAN_M.YEAR ");
        stb.append("                                                       AND LIMI.SCHREGNO    = PLAN_M.SCHREGNO ");
        stb.append("                                                       AND LIMI.SLIP_NO     = PLAN_M.SLIP_NO ");
        stb.append("                                                       AND LIMI.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
        stb.append("                                                       AND LIMI.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
        stb.append("     WHERE ");
        stb.append("             PLAN_M.SCHOOLCD      = '" + _param._schoolCd + "' ");
        stb.append("         AND PLAN_M.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("         AND PLAN_M.YEAR          = '" + _param._year + "' ");
        stb.append("         AND SLIP.CANCEL_DATE   is null ");
        stb.append("         AND LIMI.PAID_LIMIT_MONTH = '" + _param._paidLimitMonth + "' ");
        stb.append("     GROUP BY ");
        stb.append("         PLAN_M.SCHOOLCD, ");
        stb.append("         PLAN_M.SCHOOL_KIND, ");
        stb.append("         PLAN_M.YEAR, ");
        stb.append("         PLAN_M.SLIP_NO ");
        stb.append("     HAVING ");
        stb.append("         SUM(VALUE(PLAN_M.PAID_MONEY, 0)) = 0 ");
        // 授業料のある伝票
        stb.append(" ), JUGYOU_SLIP AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         MDAT.SLIP_NO ");
        stb.append("     FROM ");
        stb.append("         COLLECT_SLIP_M_DAT MDAT ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SLIP ON SLIP.SCHOOLCD    = MDAT.SCHOOLCD ");
        stb.append("                                        AND SLIP.SCHOOL_KIND = MDAT.SCHOOL_KIND ");
        stb.append("                                        AND SLIP.YEAR        = MDAT.YEAR ");
        stb.append("                                        AND SLIP.SLIP_NO     = MDAT.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_M_MST MMST ON MMST.SCHOOLCD     = MDAT.SCHOOLCD ");
        stb.append("                                     AND MMST.SCHOOL_KIND  = MDAT.SCHOOL_KIND ");
        stb.append("                                     AND MMST.YEAR         = MDAT.YEAR ");
        stb.append("                                     AND MMST.COLLECT_L_CD = MDAT.COLLECT_L_CD ");
        stb.append("                                     AND MMST.COLLECT_M_CD = MDAT.COLLECT_M_CD ");
        stb.append("     WHERE ");
        stb.append("             MDAT.SCHOOLCD      = '" + _param._schoolCd + "' ");
        stb.append("         AND MDAT.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("         AND MDAT.YEAR          = '" + _param._year + "' ");
        stb.append("         AND SLIP.CANCEL_DATE   is null ");
        stb.append("         AND MMST.GAKUNOKIN_DIV = '1' ");
        // 金額取得
        stb.append(" ), MONEY_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         PLAN_M.SLIP_NO, ");
        stb.append("         PLAN_M.SCHREGNO, ");
        stb.append("         PLAN_M.COLLECT_L_CD, ");
        stb.append("         PLAN_M.COLLECT_M_CD, ");
        stb.append("         sum(value(PLAN_M.PLAN_MONEY , 0)) as COLLECT_MONEY ");
        stb.append("     FROM ");
        stb.append("         COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SLIP ON SLIP.SCHOOLCD    = PLAN_M.SCHOOLCD ");
        stb.append("                                        AND SLIP.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
        stb.append("                                        AND SLIP.YEAR        = PLAN_M.YEAR ");
        stb.append("                                        AND SLIP.SLIP_NO     = PLAN_M.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON LIMI.SCHOOLCD    = PLAN_M.SCHOOLCD ");
        stb.append("                                                       AND LIMI.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
        stb.append("                                                       AND LIMI.YEAR        = PLAN_M.YEAR ");
        stb.append("                                                       AND LIMI.SCHREGNO    = PLAN_M.SCHREGNO ");
        stb.append("                                                       AND LIMI.SLIP_NO     = PLAN_M.SLIP_NO ");
        stb.append("                                                       AND LIMI.PLAN_YEAR   = PLAN_M.PLAN_YEAR ");
        stb.append("                                                       AND LIMI.PLAN_MONTH  = PLAN_M.PLAN_MONTH ");
        stb.append("     WHERE ");
        stb.append("             PLAN_M.SCHOOLCD      = '" + _param._schoolCd + "' ");
        stb.append("         AND PLAN_M.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("         AND PLAN_M.YEAR          = '" + _param._year + "' ");
        stb.append("         AND SLIP.CANCEL_DATE   is null ");
        stb.append("         AND LIMI.PAID_LIMIT_MONTH = '" + _param._paidLimitMonth + "' ");
        stb.append("     GROUP BY ");
        stb.append("         PLAN_M.SLIP_NO, ");
        stb.append("         PLAN_M.SCHREGNO, ");
        stb.append("         PLAN_M.COLLECT_L_CD, ");
        stb.append("         PLAN_M.COLLECT_M_CD ");
        // 住所データ
        stb.append(" ), ADDRESS_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         A1.SCHREGNO, ");
        stb.append("         A1.ZIPCD, ");
        stb.append("         A1.ADDR1, ");
        stb.append("         A1.ADDR2 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT A1 ");
        stb.append("     INNER JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_ADDRESS_DAT ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("         ) A2 ON  A2.SCHREGNO  = A1.SCHREGNO ");
        stb.append("              AND A2.ISSUEDATE = A1.ISSUEDATE ");
        stb.append(" ) ");
        // メイン
        stb.append(" SELECT ");
        stb.append("     SLIP.SLIP_NO, ");
        stb.append("     SLIP.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     TRANSLATE_H_K(BASE.NAME_KANA) as KANA, ");
        stb.append("     value(GUAR.GUARD_NAME, '') as GNAME, ");
        if (_param._isFresh) {
            stb.append("     value(BASE.ZIPCD, '') as ZIPCD, ");
            stb.append("     value(BASE.ADDR1, '') as ADDR1, ");
            stb.append("     value(BASE.ADDR2, '') as ADDR2, ");
            stb.append("     CASE WHEN SUBSTR(BASE.COURSECODE, 3, 1) = '1' THEN 1 END AS IS_DOYOU, ");
        } else {
            stb.append("     value(ADDR.ZIPCD, '') as ZIPCD, ");
            stb.append("     value(ADDR.ADDR1, '') as ADDR1, ");
            stb.append("     value(ADDR.ADDR2, '') as ADDR2, ");
            stb.append("     CASE WHEN SUBSTR(REGD.COURSECODE, 3, 1) = '1' THEN 1 END AS IS_DOYOU, ");
        }
        stb.append("     MMST.COLLECT_M_NAME, ");
        stb.append("     MDAT.COLLECT_L_CD, ");
        stb.append("     MDAT.COLLECT_M_CD, ");
        stb.append("     MDAT.COLLECT_CNT, ");
        stb.append("     MONEY.COLLECT_MONEY, ");
        stb.append("     MMST.COLLECT_M_MONEY, ");
        stb.append("     value(SYUG.REDUC_MONEY, 0) as REDUC_MONEY, ");
        stb.append("     value(MMST.GAKUNOKIN_DIV, '0') as GAKUNOKIN_DIV ");
        stb.append(" FROM ");
        stb.append("     COLLECT_SLIP_DAT SLIP  ");
        stb.append("     LEFT JOIN COLLECT_SLIP_M_DAT MDAT ON SLIP.SCHOOLCD    = MDAT.SCHOOLCD ");
        stb.append("                                      AND SLIP.SCHOOL_KIND = MDAT.SCHOOL_KIND ");
        stb.append("                                      AND SLIP.YEAR        = MDAT.YEAR ");
        stb.append("                                      AND SLIP.SLIP_NO     = MDAT.SLIP_NO ");
        stb.append("     LEFT JOIN COLLECT_M_MST MMST ON MMST.SCHOOLCD     = MDAT.SCHOOLCD ");
        stb.append("                                 AND MMST.SCHOOL_KIND  = MDAT.SCHOOL_KIND ");
        stb.append("                                 AND MMST.YEAR         = MDAT.YEAR ");
        stb.append("                                 AND MMST.COLLECT_L_CD = MDAT.COLLECT_L_CD ");
        stb.append("                                 AND MMST.COLLECT_M_CD = MDAT.COLLECT_M_CD ");
        stb.append("     LEFT JOIN MONEY_DATA MONEY ON MONEY.SCHREGNO     = MDAT.SCHREGNO ");
        stb.append("                               AND MONEY.SLIP_NO      = MDAT.SLIP_NO ");
        stb.append("                               AND MONEY.COLLECT_L_CD = MDAT.COLLECT_L_CD ");
        stb.append("                               AND MONEY.COLLECT_M_CD = MDAT.COLLECT_M_CD ");
        stb.append("     INNER JOIN JUGYOU_SLIP JGYO ON JGYO.SLIP_NO = SLIP.SLIP_NO ");
        stb.append("     INNER JOIN NO_PAIND_SLIP NO_PAIND ON NO_PAIND.SLIP_NO = SLIP.SLIP_NO ");
        stb.append("     LEFT JOIN SYUGAKU SYUG ON SYUG.SLIP_NO = SLIP.SLIP_NO ");
        if (_param._isFresh) {
            stb.append("     LEFT JOIN FRESHMAN_DAT BASE ON BASE.ENTERYEAR = SLIP.YEAR ");
            stb.append("                                AND BASE.SCHREGNO  = SLIP.SCHREGNO ");
        } else {
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = SLIP.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = SLIP.SCHREGNO ");
            stb.append("                                   AND REGD.YEAR     = SLIP.YEAR ");
            stb.append("                                   AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     LEFT JOIN ADDRESS_DAT ADDR ON ADDR.SCHREGNO = SLIP.SCHREGNO ");
        }
        stb.append("     LEFT JOIN GUARDIAN_DAT GUAR ON GUAR.SCHREGNO = SLIP.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         SLIP.SCHOOLCD      = '" + _param._schoolCd + "' ");
        stb.append("     AND SLIP.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("     AND SLIP.YEAR          = '" + _param._year + "' ");
        stb.append("     AND SLIP.CANCEL_DATE   is null ");
        stb.append("     AND VALUE(MONEY.COLLECT_MONEY, 0) > 0 ");
        if ("1".equals(_param._choice)) {
            if (_param._isFresh) {
                stb.append("     AND BASE.GRADE || value(BASE.HR_CLASS, '') IN (" + _param._sqlInstate + ") ");
            } else {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInstate + ") ");
            }
        } else {
            stb.append("     AND BASE.SCHREGNO IN (" + _param._sqlInstate + ")             ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SLIP.SLIP_NO, ");
        stb.append("     SLIP.SCHREGNO, ");
        stb.append("     case when MMST.GAKUNOKIN_DIV = '1' then 1 else 2 end, ");
        stb.append("     MDAT.COLLECT_L_CD, ");
        stb.append("     MDAT.COLLECT_M_CD ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregNo;
        final String _name;
        final String _kana;
        final String _gName;
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final boolean _isDoyou;
        PrintMoney _moneyData = new PrintMoney();
        public PrintData(
                final String schregNo,
                final String name,
                final String kana,
                final String gName,
                final String zipCd,
                final String addr1,
                final String addr2,
                final boolean isDoyou
        ) {
            _schregNo       = schregNo;
            _name           = name;
            _kana           = kana;
            _gName          = gName;
            _zipCd          = zipCd;
            _addr1          = addr1;
            _addr2          = addr2;
            _isDoyou        = isDoyou;
        }
    }

    private class PrintMoney {
        String _colectMname;
        int _colectCnt;
        int _jugyouMoney;
        int _jugyouMoneyTani;
        int _shisetsuMoney;
        int _syugakuMoney;
        int _totalMoney;
        public PrintMoney() {
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70308 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _choice;
        private final String _paidLimitMonth;
        private final String _limitDate;
        private final boolean _isFresh;
        private String _schoolName = "";
        private final String[] _categorySelected;
        private final String _sqlInstate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year               = request.getParameter("YEAR");
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _schoolCd           = request.getParameter("SCHOOLCD");
            _schoolKind         = request.getParameter("SCHOOL_KIND");
            _choice             = request.getParameter("CHOICE");
            _paidLimitMonth     = request.getParameter("PAID_LIMIT_MONTH");
            _limitDate          = request.getParameter("LIMIT_DATE");
            _isFresh            = "1".equals(request.getParameter("DIV"));
            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");

            setCertifSchoolDat(db2);

            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                setInstate += sep + "'" + selectVal + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;
        }

        /** 証明書学校データ */
        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if ("H".equals(_schoolKind)) {
                certifKindCd = "106";
            } else {
                certifKindCd = "105";
            }

            final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

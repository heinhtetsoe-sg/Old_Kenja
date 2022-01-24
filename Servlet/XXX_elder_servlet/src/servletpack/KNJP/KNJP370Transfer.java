// kanji=漢字
/*
 * $Id: 7bab145cfbbffdc522de652b564a01f36e571960 $
 *
 * 作成日: 2005/11/27 11:40:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３７０＞  振込依頼書
 *
 *  2005/11/27 m-yama 作成
 *  2005/11/27 m-yama NO001 パラメーター印刷対象外追加
 *  2006/03/23 m-yama NO002 プログラム追加によりクラス名変更KNJP370→KNJP370Transfer
 *  2006/03/29 m-yama NO003 生活行事費での出力も可
 *  2006/05/07 m-yama NO004 生活行事費のみ出力
 *  2006/05/11 m-yama NO005 パラメータの追加
 *  @version $Id: 7bab145cfbbffdc522de652b564a01f36e571960 $
 */

public class KNJP370Transfer {


    private static final Log log = LogFactory.getLog(KNJP370Transfer.class);

    final Map _hmparam = new HashMap();
    String[] _checksch;
    final StringBuffer _unprint = new StringBuffer();
    String[] _checkdus;                          //NO005
    final StringBuffer _inprint = new StringBuffer();  //NO005

    /**
     * KNJP370.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        DB2UDB db2 = null;                            //Databaseクラスを継承したクラス

        try {
            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //パラメータセット
            getParam(request, db2, svf);
            for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                log.debug(key + " = " + _hmparam.get(key));
            }

            //  ＳＶＦ作成処理
            boolean nonedata = false; //該当データなしフラグ

            //SVF出力
            log.debug("SVF syuturyoku!!!");
            if (printMain(db2, svf)) {
                nonedata = true;
            }

            log.debug("nonedata=" + nonedata);

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
            if (null != db2) {
                db2.commit();
                db2.close();                //DBを閉じる
            }
            if (null != outstrm) {
                outstrm.close();            //ストリームを閉じる
            }
        }

    }

    /** パラメータの取得 */
    private void getParam(
            final HttpServletRequest request,
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {

        svf.VrSetForm("KNJP370.frm", 1);

        _hmparam.put("YEAR", request.getParameter("YEAR"));             //年度
        _hmparam.put("SEMESTER", request.getParameter("SEMESTER"));     //学期
        _hmparam.put("STAFFCD", request.getParameter("STAFF"));         //担当者
        _hmparam.put("PARADATE", request.getParameter("DATE"));         //日付

        //SQL条件作成
        _checksch = request.getParameterValues("category_selected");
        if (null == _checksch) {
            _unprint.append("NOT IN ('')");
        } else {
            _unprint.append("NOT IN (");
            String com = "";
            for (int sccnt = 0; sccnt < _checksch.length; sccnt++) {
                _unprint.append(com).append("'").append(_checksch[sccnt].substring(0, _checksch[sccnt].indexOf(":"))).append("'");
                com = ",";
            }
            _unprint.append(")");
        }

        //SQL条件作成
        _checkdus = request.getParameterValues("due_selected");
        if (null == _checkdus) {
            _inprint.append("IN ('')");
        } else {
            _inprint.append("IN (");
            String com = "";
            for (int sccnt = 0; sccnt < _checkdus.length; sccnt++) {
                _inprint.append(com).append("'").append(_checkdus[sccnt].substring(0, _checkdus[sccnt].lastIndexOf(":"))).append("'");
                com = ",";
            }
            _inprint.append(")");
        }
        //担当者の取得
        setStaffName(db2);
        //作成日
        _hmparam.put("DATE", KNJ_EditDate.h_format_JP((String) _hmparam.get("PARADATE")));
        //学校名の取得
        setSchoolName(db2);
        //銀行名の取得
        setBank(db2);
        //総頁の取得
        setTotalPage(db2);
        //合計の取得
        setTotalMoney(db2);
    }

    /** 担当者名 */
    private void setStaffName(
            final DB2UDB db2
    ) throws Exception {
        final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _hmparam.get("STAFFCD") + "' ";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("STAFFNAME", rs.getString("STAFFNAME"));
            }
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }

    }

    /** 学校名 */
    private void setSchoolName(
            final DB2UDB db2
    ) throws Exception {

        final String sql = "SELECT SCHOOLNAME1,SCHOOLTELNO FROM SCHOOL_MST WHERE YEAR = '" + _hmparam.get("YEAR") + "' ";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("SCHOOLNAME1", rs.getString("SCHOOLNAME1"));
                _hmparam.put("SCHOOLTELNO", rs.getString("SCHOOLTELNO"));
            }
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }
    }

    /** 銀行名 */
    private void setBank(
            final DB2UDB db2
    ) throws Exception {
        final String sql = "SELECT VAR1,VAR2,BANKNAME,BRANCHNAME"
            + " FROM SCHOOL_EXPENSES_SYS_INI"
            + " LEFT JOIN BANK_MST ON BANKCD = VAR1 AND BRANCHCD = VAR2"
            + " WHERE PROGRAMID = 'BANK' AND DIV = '0001'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("BANKNAME", rs.getString("BANKNAME"));
                _hmparam.put("BRANCHNAME", rs.getString("BRANCHNAME"));
                _hmparam.put("VAR1", rs.getString("VAR1"));
                _hmparam.put("VAR2", rs.getString("VAR2"));
            }
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }
    }

    /** 総頁 */
    private void setTotalPage(
            final DB2UDB db2
    ) throws Exception {
        db2.query(totalpageSql());
        final ResultSet rs = db2.getResultSet();
        try {
            int totalp = 0;
            while (rs.next()) {
                totalp = totalp + rs.getInt("TOTAL_PAGE");
            }
            _hmparam.put("TOTAL_PAGE", String.valueOf(totalp));
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }

    }

    /** 合計 */
    private void setTotalMoney(
            final DB2UDB db2
    ) throws Exception {
        db2.query(totalMoneySql());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("TOTALCNT", rs.getString("TOTALCNT"));
                _hmparam.put("TOTALMONEY", rs.getString("TOTALMONEY"));
            }
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }

    }

    /**印刷処理メイン NO001*/
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean nonedata = false;
        int gyo  = 1;
        int pcnt = 1;
        int syoukei  = 0;
        int goukei   = 0;
        int totalgyo = 0;
        String bifchangepage = "";
        db2.query(meisaiSql());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                //合計印字 NO001
                if (!nonedata && pcnt == 1) {
                    svf.VrsOut("TOTAL_NUMBER", (String) _hmparam.get("TOTALCNT"));
                    svf.VrsOut("TOTAL", (String) _hmparam.get("TOTALMONEY"));
                }
                //１ページ印刷
                if (15 < gyo || (gyo > 1 && !bifchangepage.equalsIgnoreCase(rs.getString("BANKNO")))) {
                    printOutEndPage(svf, String.valueOf(gyo - 1), String.valueOf(syoukei));
                    gyo = 1;
                    pcnt++;
                    syoukei = 0;
                }
                //明細データをセット
                printMeisai(svf, rs, gyo, String.valueOf(pcnt));
                nonedata = true;

                gyo++;
                totalgyo++;
                syoukei = syoukei + rs.getInt("MONEY");
                goukei  = goukei + rs.getInt("MONEY");
                bifchangepage = rs.getString("BANKNO");

            }

            //最終ページ印刷
            if (nonedata) {
                printOutEndPage(svf, String.valueOf(gyo - 1), String.valueOf(syoukei));
            }

        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
        }
        return nonedata;

    }

    /** 改ページ・最終頁印刷 */
    private void printOutEndPage(
            final Vrw32alp svf,
            final String gyo,
            final String syoukei
    ) {
        svf.VrsOut("NUMBER", gyo);
        svf.VrsOut("SUBTOTAL", syoukei);
        svf.VrEndPage();
    }

    /**明細データをセット*/
    private void printMeisai(
            final Vrw32alp svf,
            final ResultSet rs,
            final int gyo,
            final String pcnt
    ) throws SQLException {
        //ヘッダ
        svf.VrsOut("TITLE", "振込依頼書(連記式)");
        svf.VrsOut("PAGE", (String) _hmparam.get("TOTAL_PAGE") + "-" + pcnt);
        svf.VrsOut("BANK1", (String) _hmparam.get("BANKNAME"));
        svf.VrsOut("BRANCH1", (String) _hmparam.get("BRANCHNAME"));
        svf.VrsOut("SCHOOLNAME", (String) _hmparam.get("SCHOOLNAME1"));
        svf.VrsOut("CHARGE", (String) _hmparam.get("STAFFNAME"));
        svf.VrsOut("PHONE", (String) _hmparam.get("SCHOOLTELNO"));
        svf.VrsOut("DATE", (String) _hmparam.get("DATE"));
        //明細
        svf.VrsOutn("BANK2", gyo, rs.getString("BANKNAME_KANA"));
        svf.VrsOutn("BRANCH2", gyo, rs.getString("BRANCHNAME_KANA"));
        svf.VrsOutn("DEPOSIT_ITEM", gyo, rs.getString("ABBV1"));
        svf.VrsOutn("ACCOUNTNO", gyo, rs.getString("ACCOUNTNO"));
        svf.VrsOutn("NAME", gyo, rs.getString("ACCOUNTNAME"));
        svf.VrsOutn("MONEY", gyo, rs.getString("MONEY"));

    }

    /** WITH共通SQL */
    //  CSOFF: ExecutableStatementCount
    private String withMain() {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH MONEYTBL AS (");
        stb.append("SELECT");
        stb.append("    t2.GRADE || t2.HR_CLASS || ATTENDNO AS GRD_CLS_ATD,");
        stb.append("    t3.BANKCD,");
        stb.append("    t4.BANKNAME_KANA,");
        stb.append("    t3.BRANCHCD,");
        stb.append("    t4.BRANCHNAME_KANA,");
        stb.append("    t3.DEPOSIT_ITEM,");
        stb.append("    t5.ABBV1,");
        stb.append("    t3.ACCOUNTNO,");
        stb.append("    t3.ACCOUNTNAME,");
        stb.append("    '0' AS FRMID,");
        stb.append("    SUM(t1.REPAY_MONEY) AS MONEY");
        stb.append(" FROM");
        stb.append("    MONEY_REPAY_S_DAT t1");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = t1.SCHREGNO");
        stb.append("    AND t2.YEAR = t1.YEAR");
        stb.append("    AND t2.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("'");
        stb.append("    LEFT JOIN REGISTBANK_DAT t3 ON t3.SCHREGNO = t1.SCHREGNO");
        stb.append("    LEFT JOIN BANK_MST t4 ON t4.BANKCD = t3.BANKCD");
        stb.append("    AND t4.BRANCHCD = t3.BRANCHCD");
        stb.append("    LEFT JOIN NAME_MST t5 ON t5.NAMECD1 = 'G203'");
        stb.append("    AND t5.NAMECD2 = t3.DEPOSIT_ITEM");
        stb.append("    LEFT JOIN MONEY_DUE_M_DAT t6 ON t6.YEAR = t1.YEAR");
        stb.append("    AND t6.SCHREGNO = t1.SCHREGNO");
        stb.append("    AND t6.EXPENSE_M_CD = '13'");
        stb.append(" WHERE");
        stb.append("    t1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND t1.SCHREGNO ").append(_unprint.toString());
        stb.append("    AND t1.EXPENSE_M_CD || ':' || t1.EXPENSE_S_CD || ':' || CAST(t1.REPAY_MONEY_DATE AS VARCHAR(10)) ").append(_inprint.toString());
        stb.append("    AND t1.REPAY_MONEY IS NOT NULL");
        stb.append("    AND VALUE(t1.REPAY_FLG,'0') <> '1'");
        stb.append(" GROUP BY");
        stb.append("    t2.GRADE,");
        stb.append("    t2.HR_CLASS,");
        stb.append("    ATTENDNO,");
        stb.append("    t3.BANKCD,");
        stb.append("    t4.BANKNAME_KANA,");
        stb.append("    t3.BRANCHCD,");
        stb.append("    t4.BRANCHNAME_KANA,");
        stb.append("    t3.DEPOSIT_ITEM,");
        stb.append("    t5.ABBV1,");
        stb.append("    t3.ACCOUNTNO,");
        stb.append("    t3.ACCOUNTNAME");
        stb.append(")");

        return stb.toString();
    }
    //  CSON: ExecutableStatementCount

    /** クラス別軽減データを抽出 */
    //  CSOFF: ExecutableStatementCount|MethodLength
    private String meisaiSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(withMain());
        stb.append("SELECT");
        stb.append("    '1' AS BANKNO,");
        stb.append("    GRD_CLS_ATD,");
        stb.append("    BANKCD,");
        stb.append("    BANKNAME_KANA,");
        stb.append("    BRANCHCD,");
        stb.append("    BRANCHNAME_KANA,");
        stb.append("    DEPOSIT_ITEM,");
        stb.append("    ABBV1,");
        stb.append("    ACCOUNTNO,");
        stb.append("    ACCOUNTNAME,");
        stb.append("    FRMID,");
        stb.append("    MONEY");
        stb.append(" FROM");
        stb.append("    MONEYTBL");
        stb.append(" WHERE");
        stb.append("    FRMID = '0'");
        stb.append("    AND BANKCD = '").append(_hmparam.get("VAR1")).append("'");
        stb.append(" UNION");
        stb.append(" SELECT");
        stb.append("    '2' AS BANKNO,");
        stb.append("    GRD_CLS_ATD,");
        stb.append("    BANKCD,");
        stb.append("    BANKNAME_KANA,");
        stb.append("    BRANCHCD,");
        stb.append("    BRANCHNAME_KANA,");
        stb.append("    DEPOSIT_ITEM,");
        stb.append("    ABBV1,");
        stb.append("    ACCOUNTNO,");
        stb.append("    ACCOUNTNAME,");
        stb.append("    FRMID,");
        stb.append("    MONEY");
        stb.append(" FROM");
        stb.append("    MONEYTBL");
        stb.append(" WHERE");
        stb.append("    FRMID = '0'");
        stb.append("    AND (BANKCD < '").append(_hmparam.get("VAR1")).append("' OR BANKCD > '").append(_hmparam.get("VAR1")).append("')");
        stb.append(" ORDER BY");
        stb.append("    BANKNO,GRD_CLS_ATD");

        log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount|MethodLength

    /** トータル金額を抽出 */
    private String totalMoneySql() {

        final StringBuffer stb = new StringBuffer();
        stb.append(withMain());
        stb.append("SELECT");
        stb.append("    FRMID,");
        stb.append("    COUNT(*) AS TOTALCNT,");
        stb.append("    SUM(MONEY) AS TOTALMONEY");
        stb.append(" FROM");
        stb.append("    MONEYTBL");
        stb.append(" WHERE");
        stb.append("    FRMID = '0'");
        stb.append(" GROUP BY");
        stb.append("    FRMID");

        return stb.toString();

    }

    /** 総ページ数を抽出 */
    //  CSOFF: ExecutableStatementCount|MethodLength
    private String totalpageSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append(withMain());
        stb.append("SELECT");
        stb.append("    CASE WHEN 0 < MOD(COUNT(*),15) THEN COUNT(*)/15 + 1 ELSE COUNT(*)/15 END TOTAL_PAGE");
        stb.append(" FROM");
        stb.append("    MONEYTBL");
        stb.append(" WHERE");
        stb.append("    FRMID = '0'");
        stb.append("    AND BANKCD = '").append(_hmparam.get("VAR1")).append("'");
        stb.append(" UNION ALL");
        stb.append(" SELECT");
        stb.append("    CASE WHEN 0 < MOD(COUNT(*),15) THEN COUNT(*)/15 + 1 ELSE COUNT(*)/15 END TOTAL_PAGE");
        stb.append(" FROM");
        stb.append("    MONEYTBL");
        stb.append(" WHERE");
        stb.append("    FRMID = '0'");
        stb.append("    AND (BANKCD < '").append(_hmparam.get("VAR1")).append("' OR BANKCD > '").append(_hmparam.get("VAR1")).append("')");

        return stb.toString();

    }
    //  CSON: ExecutableStatementCount|MethodLength

}

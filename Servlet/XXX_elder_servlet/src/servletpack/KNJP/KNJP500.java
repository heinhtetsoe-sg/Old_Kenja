// kanji=漢字
/*
 * $Id: 51e78790b0bae0efea432b40e9aff68684914a0f $
 *
 * 作成日: 2006/03/14 11:10:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2006-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ５００＞  NO24.一般費目入金・取消報告書
 *
 *  2006/03/14 m-yama 作成
 *  2006/04/08 m-yama NO001 内生のみ出力対象とする。
 *  @version $Id: 51e78790b0bae0efea432b40e9aff68684914a0f $
 */

public class KNJP500 {


    private static final Log log = LogFactory.getLog(KNJP500.class);

    final Map _hmparam = new HashMap();

    /**
     * KNJP.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());

        try {
            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //  ＳＶＦ作成処理
            boolean nonedata = false; //該当データなしフラグ

            //  パラメータの取得
            setParam(request, db2, svf);
            for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                log.debug(key + " = " + _hmparam.get(key));
            }


            //SVF出力
            if (printMain(db2, svf)) {
                nonedata = true;
            }

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
                db2.close();
            }
            if (null != outstrm) {
                outstrm.close();
            }
        }

    }


    /**ヘッダーデータを抽出*/
    private void setParam(
            final HttpServletRequest request,
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {

        _hmparam.put("YEAR", request.getParameter("YEAR"));         //年度
        _hmparam.put("SEMESTER", request.getParameter("SEMESTER")); //学期
        //  学校名
        setSchool(db2);
        //  総頁
        setAllPage(db2);

    }

    /** 学校名 */
    private void setSchool(
            final DB2UDB db2
    ) throws Exception {
        final String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _hmparam.get("YEAR") + "' ";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("SCHOOLNAME1", rs.getString("SCHOOLNAME1"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    /** 総頁 */
    private void setAllPage(
            final DB2UDB db2
    ) throws Exception {
        db2.query(pageSql());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _hmparam.put("TOTAL_PAGE", rs.getString("TOTAL_PAGE"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }

    }

    /**印刷処理メイン*/
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ps = db2.prepareStatement(meisaiSql());
        final ResultSet rs = ps.executeQuery();
        try {
            //明細データをセット
            nonedata = printMeisai(svf, rs);
        } finally {
            if (null != db2) {
                db2.commit();
            }
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
        }
        return nonedata;

    }

    //ヘッダ印刷
    private void printHead(final Vrw32alp svf, final String pagecnt) {

        svf.VrSetForm("KNJP500.frm", 1);
        svf.VrsOut("SCHOOLNAME", (String) _hmparam.get("SCHOOLNAME1"));
        svf.VrsOut("TOTAL_PAGE", (String) _hmparam.get("TOTAL_PAGE"));
        svf.VrsOut("PAGE", pagecnt);

    }

    /**明細データをセット*/
    private boolean printMeisai(
            final Vrw32alp svf,
            final ResultSet rs
    ) throws Exception {
        boolean nonedata = false;
        int gyo = 1;
        int pagecnt = 1;
        printHead(svf, String.valueOf(pagecnt));
        while (rs.next()) {

            if (gyo > 30) {
                svf.VrEndPage();
                gyo = 1;
                pagecnt++;
                printHead(svf, String.valueOf(pagecnt));
            }
            svf.VrsOutn("SLIPNO", gyo, rs.getString("SLIP_NO"));
            svf.VrsOutn("SCHOOLCD", gyo, rs.getString("SCHOOL_CD"));
            svf.VrsOutn("SCHOOLDIV", gyo, rs.getString("SCHOOL_DIV"));
            svf.VrsOutn("COURSE", gyo, rs.getString("FACULTY"));
            svf.VrsOutn("MAJOR", gyo, rs.getString("BANK_MAJORCD"));
            svf.VrsOutn("GRADE", gyo, rs.getString("GRADE"));
            svf.VrsOutn("HR_CLASS", gyo, rs.getString("BANK_HR_CLASS"));
            svf.VrsOutn("SCHREGNO", gyo, rs.getString("SCHREGNO"));
            svf.VrsOutn("CANCELDIV", gyo, rs.getString("RESET_DIV"));
            svf.VrsOutn("PERIOD", gyo, rs.getString("SEM"));
            svf.VrsOutn("YEAR1", gyo, rs.getString("PAID_Y"));
            svf.VrsOutn("MONTH1", gyo, rs.getString("PAID_M"));
            svf.VrsOutn("DAY1", gyo, rs.getString("PAID_D"));
            svf.VrsOutn("MONEY", gyo, rs.getString("MONEY_DUE"));
            svf.VrsOutn("NAME", gyo, rs.getString("NAME"));

            gyo++;
            nonedata = true;
        }
        if (gyo > 1) {
            svf.VrEndPage();
        }

        return nonedata;
    }

    /** 明細データを抽出 */
    //  CSOFF: ExecutableStatementCount|MethodLength
    private String meisaiSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH MONEY_M AS (");
        stb.append("SELECT");
        stb.append("    T1.EXPENSE_GRP_CD AS M_GRP_CD,");
        stb.append("    SUM(VALUE(T2.EXPENSE_M_MONEY,0)) AS M_MONEY");
        stb.append(" FROM");
        stb.append("    EXPENSE_GRP_M_DAT T1");
        stb.append("    LEFT JOIN EXPENSE_M_MST T2 ON T2.YEAR = T1.YEAR");
        stb.append("    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD");
        stb.append(" WHERE");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND T1.EXPENSE_M_CD <> '12'");
        stb.append("    AND T1.EXPENSE_M_CD <> '13'");
        stb.append(" GROUP BY");
        stb.append("    T1.EXPENSE_GRP_CD");
        stb.append("), MONEY_S_ALL AS (");
        stb.append("SELECT");
        stb.append("    T1.EXPENSE_GRP_CD AS S_GRP_CD_ALL,");
        stb.append("    SUM(VALUE(T2.EXPENSE_S_MONEY,0)) AS S_MONEY_ALL");
        stb.append(" FROM");
        stb.append("    EXPENSE_GRP_S_DAT T1");
        stb.append("    LEFT JOIN V_EXPENSE_S_MST T2 ON T2.YEAR = T1.YEAR");
        stb.append("    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD");
        stb.append("    AND T2.EXPENSE_S_CD = T1.EXPENSE_S_CD");
        stb.append("    AND (T2.SEX IS NULL OR T2.SEX = '')");
        stb.append(" WHERE");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append(" GROUP BY");
        stb.append("    T1.EXPENSE_GRP_CD");
        stb.append("), MONEY_S_MAN AS (");
        stb.append("SELECT");
        stb.append("    T1.EXPENSE_GRP_CD AS S_GRP_CD_MAN,");
        stb.append("    SUM(VALUE(T2.EXPENSE_S_MONEY,0)) AS S_MONEY_MAN");
        stb.append(" FROM");
        stb.append("    EXPENSE_GRP_S_DAT T1");
        stb.append("    LEFT JOIN V_EXPENSE_S_MST T2 ON T2.YEAR = T1.YEAR");
        stb.append("    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD");
        stb.append("    AND T2.EXPENSE_S_CD = T1.EXPENSE_S_CD");
        stb.append("    AND T2.SEX = '1'");
        stb.append(" WHERE");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append(" GROUP BY");
        stb.append("    T1.EXPENSE_GRP_CD");
        stb.append("), MONEY_S_GIL AS (");
        stb.append("SELECT");
        stb.append("    T1.EXPENSE_GRP_CD AS S_GRP_CD_GIL,");
        stb.append("    SUM(VALUE(T2.EXPENSE_S_MONEY,0)) AS S_MONEY_GIL");
        stb.append(" FROM");
        stb.append("    EXPENSE_GRP_S_DAT T1");
        stb.append("    LEFT JOIN V_EXPENSE_S_MST T2 ON T2.YEAR = T1.YEAR");
        stb.append("    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD");
        stb.append("    AND T2.EXPENSE_S_CD = T1.EXPENSE_S_CD");
        stb.append("    AND T2.SEX = '2'");
        stb.append(" WHERE");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append(" GROUP BY");
        stb.append("    T1.EXPENSE_GRP_CD");
        stb.append(")");
        stb.append("SELECT");
        stb.append("    '24' AS SLIP_NO,");
        stb.append("    '0245' AS SCHOOL_CD,");
        stb.append("    '6' AS SCHOOL_DIV,");
        stb.append("    '' AS FACULTY,");
        stb.append("    L3.BANK_MAJORCD,");
        stb.append("    T1.GRADE,");
        stb.append("    L3.BANK_HR_CLASS,");
        stb.append("    T1.SCHREGNO,");
        stb.append("    '1' RESET_DIV,");
        stb.append("    '01' AS SEM,");
        stb.append("    '").append(_hmparam.get("YEAR")).append("' AS PAID_Y,");
        stb.append("    '04' AS PAID_M,");
        stb.append("    '01' AS PAID_D,");
        stb.append("    L2.EXPENSE_GRP_CD,");
        stb.append("    CASE WHEN L1.SEX = '1'");
        stb.append("    THEN M1.M_MONEY + M2.S_MONEY_ALL + M3.S_MONEY_MAN");
        stb.append("    ELSE M1.M_MONEY + M2.S_MONEY_ALL + M4.S_MONEY_GIL");
        stb.append("    END AS MONEY_DUE,");
        stb.append("    L1.NAME");
        stb.append(" FROM");
        stb.append("    SCHREG_REGD_DAT T1");
        stb.append("    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO");
        stb.append("    LEFT JOIN EXPENSE_GRP_HR_DAT L2 ON L2.YEAR = T1.YEAR");
        stb.append("    AND L2.GRADE = T1.GRADE");
        stb.append("    AND L2.HR_CLASS = T1.HR_CLASS");
        stb.append("    LEFT JOIN BANK_CLASS_MST L3 ON L3.YEAR = T1.YEAR");
        stb.append("    AND L3.GRADE = T1.GRADE");
        stb.append("    AND L3.HR_CLASS = T1.HR_CLASS");
        stb.append("    LEFT JOIN MONEY_M M1 ON M1.M_GRP_CD = L2.EXPENSE_GRP_CD");
        stb.append("    LEFT JOIN MONEY_S_ALL M2 ON M2.S_GRP_CD_ALL = L2.EXPENSE_GRP_CD");
        stb.append("    LEFT JOIN MONEY_S_MAN M3 ON M3.S_GRP_CD_MAN = L2.EXPENSE_GRP_CD");
        stb.append("    LEFT JOIN MONEY_S_GIL M4 ON M4.S_GRP_CD_GIL = L2.EXPENSE_GRP_CD");
        stb.append(" WHERE");
        stb.append("    t1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND t1.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("'");
        stb.append("    AND t1.GRADE = '01'");
        stb.append("    AND t1.HR_CLASS LIKE '%J%'");
        stb.append("    AND L1.INOUTCD = '0'");    //NO001

        return stb.toString();

    }
    //  CSON: ExecutableStatementCount|MethodLength

    /**
     *  頁数を抽出
     *
     */
    private String pageSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        stb.append("    CASE WHEN 0 < MOD(COUNT(*),30) THEN COUNT(*)/30 + 1 ELSE COUNT(*)/30 END TOTAL_PAGE");
        stb.append(" FROM");
        stb.append("    SCHREG_REGD_DAT T1");
        stb.append("    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO");
        stb.append(" WHERE");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND T1.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("'");
        stb.append("    AND T1.GRADE = '01'");
        stb.append("    AND T1.HR_CLASS LIKE '%J%'");
        stb.append("    AND L1.INOUTCD = '1'");    //NO001

        return stb.toString();

    }

}

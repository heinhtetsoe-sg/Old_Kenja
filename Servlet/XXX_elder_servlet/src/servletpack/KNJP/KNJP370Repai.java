// kanji=漢字
/*
 * $Id: d3c7e0b0267da78c4202704908677828d694302d $
 *
 * 作成日: 2006/03/23 11:40:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.PrintWriter;
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

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３７０＞  振込依頼書
 *
 *  2006/03/23 m-yama 作成
 *  2006/05/09 m-yama NO001 パラメータの追加
 *  @version $Id: d3c7e0b0267da78c4202704908677828d694302d $
 */

public class KNJP370Repai {


    private static final Log log = LogFactory.getLog(KNJP370Repai.class);

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
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //  ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db2.open();

    //  パラメータセット
        getParam(request, db2);
        for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            log.debug(key + " = " + _hmparam.get(key));
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

    //SVF出力
        if (printMain(db2, svf)) { nonedata = true; }

    //  該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }

    /** パラメータの取得 */
    private void getParam(
            final HttpServletRequest request,
            final DB2UDB db2
    ) throws Exception {

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
        //作成日
        setMakeDate(db2);
        //学校名の取得
        setSchoolName(db2);
        //返戻事由の取得
        setRepayReason(db2);
    }

    /** 作成日 */
    private void setMakeDate(final DB2UDB db2) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        returnval = getinfo.Control(db2);
        _hmparam.put("DATE", KNJ_EditDate.h_format_JP(returnval.val3));
        getinfo = null;
        returnval = null;
    }

    /** 学校名 */
    private void setSchoolName(
            final DB2UDB db2
    ) throws Exception {

        PreparedStatement ps = null;
        final String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _hmparam.get("YEAR") + "' ";
        ps = db2.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                _hmparam.put("SCHOOLNAME1", rs.getString("SCHOOLNAME1"));
            }
            psrsClose(ps, rs);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            if (null != db2) {
                db2.commit();
            }
        }

    }

    /** 返戻事由 */
    private void setRepayReason(
            final DB2UDB db2
    ) throws Exception {

        PreparedStatement ps = null;
        final String sql = "SELECT DIV,REMARK FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'KNJP370' AND DIV IN ('0001','0002') ORDER BY DIV ";
        ps = db2.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                if (rs.getString("DIV").equals("0001")) {
                    _hmparam.put("REMARK1", rs.getString("REMARK"));
                } else {
                    _hmparam.put("REMARK2", rs.getString("REMARK"));
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            if (null != db2) {
                db2.commit();
            }
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
            while (rs.next()) {
                //明細データをセット
                printMeisai(svf, rs);
                svf.VrEndPage();
                nonedata = true;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            if (null != db2) {
                db2.commit();
            }
        }
        return nonedata;

    }

    /**明細データをセット*/
    private void printMeisai(final Vrw32alp svf, final ResultSet rs) throws SQLException {

        svf.VrSetForm("KNJP370_2.frm", 1);
        svf.VrsOut("DATE", (String) _hmparam.get("DATE"));
        svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
        svf.VrsOut("ATTENDNO", rs.getString("ATTENDNO"));
        svf.VrsOut("GUARDIANNAME", rs.getString("GUARANTOR_NAME"));
        svf.VrsOut("NAME", rs.getString("NAME"));
        svf.VrsOut("SCHOOLNAME", (String) _hmparam.get("SCHOOLNAME1"));
        svf.VrsOut("MONEY", rs.getString("REPAY_MONEY"));
        svf.VrsOut("TRANS_DATE", (String) _hmparam.get("PARADATE"));
        svf.VrsOut("REASON1", (String) _hmparam.get("REMARK1"));
        svf.VrsOut("REASON2", (String) _hmparam.get("REMARK2"));

    }

    private void psrsClose(final PreparedStatement ps, final ResultSet rs) {
        try {
            if (null != rs) { rs.close(); }
        } catch (final SQLException e) {
            log.error("rsclose error", e);
        }
        try {
            if (null != ps) { ps.close(); }
        } catch (final SQLException e) {
            log.error("psclose error", e);
        }
    }

    /**
     *  データを抽出
     *
     */
    //  CSOFF: ExecutableStatementCount
    private String meisaiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH MONEYTBL AS (");
        stb.append("SELECT");
        stb.append("    SCHREGNO,");
        stb.append("    SUM(REPAY_MONEY) AS REPAY_MONEY");
        stb.append(" FROM");
        stb.append("    MONEY_REPAY_S_DAT");
        stb.append(" WHERE");
        stb.append("    YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND REPAY_MONEY IS NOT NULL");
        stb.append("    AND EXPENSE_M_CD || ':' || EXPENSE_S_CD || ':' || CAST(REPAY_MONEY_DATE AS VARCHAR(10)) ").append(_inprint.toString());
        stb.append("    AND SCHREGNO ").append(_unprint.toString());
        stb.append(" GROUP BY");
        stb.append("    SCHREGNO");
        stb.append(")");
        stb.append("SELECT");
        stb.append("    t5.GUARANTOR_NAME,");
        stb.append("    t3.HR_NAME,");
        stb.append("    t2.ATTENDNO,");
        stb.append("    t4.NAME,");
        stb.append("    t1.REPAY_MONEY");
        stb.append(" FROM");
        stb.append("    MONEYTBL t1");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = t1.SCHREGNO");
        stb.append("    AND t2.YEAR = '").append(_hmparam.get("YEAR")).append("'");
        stb.append("    AND t2.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("'");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS");
        stb.append("    AND t3.YEAR = t2.YEAR");
        stb.append("    AND t3.SEMESTER = t2.SEMESTER");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = t1.SCHREGNO");
        stb.append("    LEFT JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = t1.SCHREGNO");
        stb.append(" ORDER BY");
        stb.append("    t2.GRADE,t2.HR_CLASS,t2.ATTENDNO");

        log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount

}


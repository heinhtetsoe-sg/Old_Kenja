// kanji=漢字
/*
 * $Id: b632a94b745234e0231bbb36f05593942fc2a485 $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [通信制] ＜ＫＮＪＭ０２０Ｍ＞ 学籍番号バーコードラベル(通信制)
 **/

public class KNJM020M {

    private static final Log log = LogFactory.getLog(KNJM020M.class);
    
    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス

        // print設定
        PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        svf.VrInit(); // クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); // PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!");
            return;
        }
        _param = createParam(request, db2);

        // ＳＶＦ作成処理
        PreparedStatement ps = null;
        boolean nonedata = false; // 該当データなしフラグ
        // SQL作成
        try {
            final String preStat = preStat();
            log.debug(" preStat = " + preStat);
            ps = db2.prepareStatement(preStat);
            
            // SVF出力
            if (setSvfMain(db2, svf, ps)) {
                nonedata = true; // 帳票出力のメソッド
            }

        } catch (Exception ex) {
            log.error("DB2 prepareStatement set error!");
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
            db2.close(); // DBを閉じる
            // 該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "");
                svf.VrEndPage();
            }
            // 終了処理
            svf.VrQuit();
            outstrm.close(); // ストリームを閉じる
        }
    }// doGetの括り

    /**
     * svf print 印刷処理
     */
    private boolean setSvfMain(DB2UDB db2, Vrw32alp svf, PreparedStatement ps) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            svf.VrSetForm("KNJM020M.frm", 1); // セットフォーム
            if ("KNJM020M".equals(_param._prgid) && "1".equals(_param._formOnly)) {
                for (int bu = 0; bu < Integer.parseInt(_param._busu); bu++) {
                    svf.VrsOut("NENDO", _param._year + "年度"); // 年度
                    svf.VrsOut("DATE", _param._loginDate); // 作成日
                    svf.VrEndPage();
                    nonedata = true;
                }
                return nonedata;
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String schregno = rs.getString("SCHREGNO");
                final String yearNth = rs.getString("YEAR_NTH");

                for (int bu = 0; bu < Integer.parseInt(_param._busu); bu++) {
                    // ヘッダ
                    svf.VrsOut("NENDO", _param._year + "年度"); // 年度
                    svf.VrsOut("DATE", _param._loginDate); // 作成日
                    if (!"1".equals(_param._formOnly)) {
                        svf.VrsOut("GAKU1", schregno); // 生徒番号
                        svf.VrsOut("SCHNAME1", name); // 生徒名１
                        for (int i = 1; i <= 6; i++) {
                            // 明細
                            svf.VrsOutn(KNJ_EditEdit.setformatArea("SCHNAME2", name, 10, "", "_2"), i, name); // 生徒名１
                            svf.VrsOutn("BARCODE1", i, schregno); // バーコード１
                            svf.VrsOutn("YEAR1", i, yearNth); // 在籍年数(入学日付から制御日付までの年数)
                            svf.VrsOutn("GAKU2", i, schregno); // 生徒番号２
                            svf.VrsOutn(KNJ_EditEdit.setformatArea("SCHNAME3", name, 10, "", "_2"), i, name); // 生徒名１
                            svf.VrsOutn("BARCODE2", i, schregno); // バーコード２
                            svf.VrsOutn("YEAR2", i, yearNth); // 在籍年数(入学日付から制御日付までの年数)
                            svf.VrsOutn("GAKU3", i, schregno); // 生徒番号３
                        }
                    }
                    svf.VrEndPage();
                }
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;
    }

    /** データ　取得 **/
    private String preStat() {
        StringBuffer stb = new StringBuffer();
        stb.append("with atable(SCHREGNO) as (SELECT ");
        stb.append("    w1.SCHREGNO ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT w1 LEFT JOIN SCHREG_REGD_HDAT w2 on w1.GRADE||w1.HR_CLASS = w2.GRADE||w2.HR_CLASS AND w2.year = '" + _param._year + "' AND w2.semester = '" + _param._gakki + "' ");
        stb.append("WHERE ");
        stb.append("    w1.YEAR = '" + _param._year + "' AND ");
        if ("KNJM021M".equals(_param._prgid)) {
            stb.append("    w1.SEMESTER = '" + _param._gakki + "' AND ");
            stb.append("    w1.SCHREGNO in " + SQLUtils.whereIn(true, _param._categorySelected));
        } else { // if "KNJM020M".equals(request.getParameter("PRGID")))
            if (_param._ctrlyear.equals(_param._year)) {
                stb.append("    w1.SEMESTER = '" + _param._gakki + "' AND ");
            } else {
                stb.append("    w1.SEMESTER = '1' AND ");
            }
            if ("1".equals(_param._output)) {
                stb.append("    w1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "' AND ");
            }
            stb.append("    EXISTS (SELECT 'X' FROM SUBCLASS_STD_SELECT_DAT WHERE SCHREGNO = w1.SCHREGNO AND YEAR = w1.YEAR AND SEMESTER = w1.SEMESTER) ");
        }
        stb.append(") ");
        stb.append("select w1.SCHREGNO, w1.NAME, (" + _param._year + " - INT(FISCALYEAR(VALUE(w1.ENT_DATE, '0001-01-01'))) + 1) as year_nth ");
        stb.append("from schreg_base_mst w1,atable w2 ");
        stb.append("where w1.SCHREGNO = w2.SCHREGNO ");
        if ("KNJM021M".equals(_param._prgid)) {
            stb.append("order by w1.SCHREGNO ");
        } else {
            stb.append("order by SUBSTR(w1.SCHREGNO, 1, 4) DESC, SUBSTR(w1.SCHREGNO, 5, 4) ASC ");
        }
        return stb.toString();

    }// preStat()の括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        final Param param = new Param(request, db2);
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 56595 $");
        return param;
    }
    
    private static class Param {
        final String _prgid;
        final String _year;
        final String _ctrlyear;
        final String _gakki;
        final String _busu;
        final String _loginDate;
        final String _output; // NJM020Mのみ
        final String _sSchregno; // NJM020Mのみ
        final String _eSchregno; // NJM020Mのみ
        final String[] _categorySelected; // KNJM021Mのみ
        final String _formOnly;
        
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgid = request.getParameter("PRGID");
            if ("KNJM021M".equals(_prgid)) {
                _output = null;
                _sSchregno = null;
                _eSchregno = null;
                _categorySelected = request.getParameterValues("category_selected"); // 学籍番号
            } else { // if "KNJM020M".equals(request.getParameter("PRGID")))
                _output = request.getParameter("OUTPUT");
                _sSchregno = request.getParameter("S_SCHREGNO");
                _eSchregno = request.getParameter("E_SCHREGNO");
                _categorySelected = null;
            }
            // パラメータの取得
            _year = request.getParameter("YEAR"); // 年度
            _ctrlyear = request.getParameter("CTRL_YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _busu = request.getParameter("BUSU"); // 部数
            _loginDate = request.getParameter("LOGIN_DATE"); // ログイン日付
            _formOnly = request.getParameter("FORM_ONLY"); // フォームのみ出力
        }
    }

}// クラスの括り

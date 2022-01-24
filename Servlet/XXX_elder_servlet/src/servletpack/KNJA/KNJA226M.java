// kanji=漢字
/*
 * $Id: baf308b4252de71f71d1f8eb507abe54443a76f1 $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 学校教育システム 賢者 [通信制] ＜ＫＮＪＡ２２６Ｍ＞ 名札
 **/

public class KNJA226M {

    private static final Log log = LogFactory.getLog(KNJA226M.class);

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

    static private int getMs932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /**
     * svf print 印刷処理
     */
    private boolean setSvfMain(DB2UDB db2, Vrw32alp svf, PreparedStatement ps) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            int col = 1;
            int count = 0;
            rs = ps.executeQuery();
            final String frmName = "sagaken".equals(_param._schoolName) ? "KNJA226M_2.frm" : "KNJA226M.frm";
            svf.VrSetForm(frmName, 1); // セットフォーム
            while (rs.next()) {
                count += 1;
                svf.VrsOutn("SCHREGNO" + col, count, rs.getString("SCHREGNO")); // 生徒番号
                svf.VrsOutn("NAME" + col + "_" + ((getMs932ByteLength(rs.getString("NAME")) > 14) ? "2" : "1"), count, rs.getString("NAME")); // 生徒名

                if("sagaken".equals(_param._schoolName)) {
                    //写真
                    final File lSchregimg = _param.getImageFile("P" + rs.getString("SCHREGNO") + "." + _param._extension.toLowerCase( )); //写真データ存在チェック用
                    final File uSchregimg = _param.getImageFile("P" + rs.getString("SCHREGNO") + "." + _param._extension.toUpperCase( )); //写真データ存在チェック用
                    if (null != lSchregimg && lSchregimg.exists()) {
                        svf.VrsOutn("PHOTO_BMP" + col, count, lSchregimg.getPath());
                    }else if (null != uSchregimg && uSchregimg.exists()) {
                        svf.VrsOutn("PHOTO_BMP" + col, count, uSchregimg.getPath());
                    } else {
                        log.warn("PHOTO_BMP:P" + rs.getString("SCHREGNO") + "." + _param._extension + " is not exist.");
                    }

                    //学校証
                    final File logoFile = _param.getImageFile("SCHOOLLOGO" + ".jpg");
                    if (logoFile != null && logoFile.exists()) {
                        svf.VrsOutn("SCHOOL_LOGO" + col, count, logoFile.getPath());
                    }
                }

                if (count >= 5) {
                    if (col == 2) {
                        svf.VrEndPage();
                    }
                    col = col == 2 ? 1 : 2;
                    count = 0;
                }
                nonedata = true;
            }
            if (!(col == 1 && 0 == count)) {
                svf.VrEndPage();
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
        if (_param._ctrlyear.equals(_param._year)) {
            stb.append("    w1.SEMESTER = '" + _param._gakki + "' AND ");
        } else {
            stb.append("    w1.SEMESTER = '1' AND ");
        }
        if ("1".equals(_param._output)) {
            stb.append("    w1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "' AND ");
        }
        stb.append("    EXISTS (SELECT 'X' FROM SUBCLASS_STD_SELECT_DAT WHERE SCHREGNO = w1.SCHREGNO AND YEAR = w1.YEAR AND SEMESTER = w1.SEMESTER) ");
        stb.append(") ");
        stb.append("select w1.SCHREGNO, w1.NAME ");
        stb.append("from schreg_base_mst w1,atable w2 ");
        stb.append("where w1.SCHREGNO = w2.SCHREGNO ");
        stb.append("order by SUBSTR(w1.SCHREGNO, 1, 4) DESC, SUBSTR(w1.SCHREGNO, 5, 4) ASC ");
        return stb.toString();

    }// preStat()の括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        final Param param = new Param(request, db2);
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 67216 $");
        return param;
    }

    private static class Param {
        final String _prgid;
        final String _year;
        final String _ctrlyear;
        final String _gakki;
        final String _output; // NJM020Mのみ
        final String _sSchregno; // NJM020Mのみ
        final String _eSchregno; // NJM020Mのみ
        final String _schoolName;
        private final String _documentRoot;
        private String _imagepath;
        private String _extension;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgid = request.getParameter("PRGID");
            _output = request.getParameter("OUTPUT");
            _sSchregno = request.getParameter("S_SCHREGNO");
            _eSchregno = request.getParameter("E_SCHREGNO");
            // パラメータの取得
            _year = request.getParameter("YEAR"); // 年度
            _ctrlyear = request.getParameter("CTRL_YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _schoolName = getNameMst(db2, "NAME1", "Z010", "00");

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
        }

        public String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private void setPhotoFileExtension(DB2UDB db2) {
            try {
                _extension = "";
                String sql = "SELECT EXTENSION FROM CONTROL_MST ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _extension = rs.getString("EXTENSION");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            //log.info(" file " + file.getAbsolutePath() + " exists? " + file.exists());
            if (file.exists()) {
                return file;
            }
            return null;
        }

    }

}// クラスの括り

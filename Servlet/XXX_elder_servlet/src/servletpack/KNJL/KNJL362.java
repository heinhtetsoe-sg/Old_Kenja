// kanji=漢字
/*
 * $Id: 3746837a0a9c86e564feb7facd9234ce781745f3 $
 *
 * Copyright(C) 2007-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;


public class KNJL362 extends HttpServlet {
    Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                  // Databaseクラスを継承したクラス
    boolean nonedata;           // 該当データなしフラグ

    private static final Log log = LogFactory.getLog(KNJL362.class);

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);      //PDFファイル名の設定

        // ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.warn("[KNJL362]DB2 open error!");
            return;
        }

        try {
            // パラメータの取得
            _param = createParam(db2, request);

        } catch( Exception ex ) {
            log.error("[KNJL362]parameter error!");
            log.error(ex);
            return;
        }

    /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理       
      -----------------------------------------------------------------------------*/
        nonedata = false;       // 該当データなしフラグ(MES001.frm出力用)

        set_detail1();

        //該当データ無しフォーム出力
        if (nonedata == false) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }   //doGetの括り


    /*----------------------------*
     * SVF出力                    *
     *----------------------------*/
    public void set_detail1()
                     throws ServletException, IOException
    {
        final String sql;
        if ("3".equals(_param._finschoolDiv)) {
            sql = "SELECT "
                + "VALUE(T1.EXAMNO,'') AS COMMON_CD,"
                + "VALUE(T1.ZIPCD,'') AS ZIPCD,"
                + "VALUE(T1.ADDRESS1,'') || VALUE(T1.ADDRESS2,'') AS ADDRESS,"
                + "VALUE(T2.NAME,'') AS NAME, "
                + "'' AS NAME1 "
            + "FROM "
                + "ENTEXAM_APPLICANTADDR_DAT T1 "
                + "INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.EXAMNO = T1.EXAMNO "
            + "WHERE "
                + "T1.ENTEXAMYEAR = '" + _param._year + "' AND "
                + "T1.EXAMNO IN " + _param._classInState + " "
            + "ORDER BY "
                + "T1.EXAMNO";
        } else if ("4".equals(_param._finschoolDiv)) {
            sql = "SELECT "
                + "VALUE(T1.EXAMNO,'') AS COMMON_CD,"
                + "VALUE(T1.GZIPCD,'') AS ZIPCD,"
                + "VALUE(T1.GADDRESS1,'') || VALUE(T1.GADDRESS2,'') AS ADDRESS,"
                + "VALUE(T1.GNAME,'') AS NAME, "
                + "'' AS NAME1 "
            + "FROM "
                + "ENTEXAM_APPLICANTADDR_DAT T1 "
                + "INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.EXAMNO = T1.EXAMNO "
            + "WHERE "
                + "T1.ENTEXAMYEAR = '" + _param._year + "' AND "
                + "T1.EXAMNO IN " + _param._classInState + " "
            + "ORDER BY "
                + "T1.EXAMNO";
        } else if ("2".equals(_param._finschoolDiv)) {
            sql = "SELECT "
                + "VALUE(PRISCHOOLCD,'') AS COMMON_CD,"
                + "VALUE(PRISCHOOL_ZIPCD,'') AS ZIPCD,"
                + "VALUE(PRISCHOOL_ADDR1,'') || VALUE(PRISCHOOL_ADDR2,'') AS ADDRESS,"
                + "VALUE(PRISCHOOL_NAME,'') AS NAME, "
                + "'' AS NAME1 "
            + "FROM "
                + "PRISCHOOL_MST T1 "
            + "WHERE "
                + "PRISCHOOLCD IN " + _param._classInState + " "
            + "ORDER BY "
                + "PRISCHOOLCD";
        } else {
            sql = "SELECT "
                + "VALUE(FINSCHOOLCD,'') AS COMMON_CD,"
                + "VALUE(FINSCHOOL_ZIPCD,'') AS ZIPCD,"
                + "VALUE(FINSCHOOL_ADDR1,'') || VALUE(FINSCHOOL_ADDR2,'') AS ADDRESS,"
                + "VALUE(FINSCHOOL_NAME, '') AS NAME, "
                + "VALUE(T2.NAME1, '') AS NAME1 "
            + "FROM "
                + "FINSCHOOL_MST T1 "
                + "LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L019' AND T2.NAMECD2 = T1.FINSCHOOL_TYPE "
            + "WHERE "
                + "FINSCHOOLCD IN " + _param._classInState + " "
            + "ORDER BY "
                + "FINSCHOOLCD";
        }
        log.debug("[KNJL362]set_detail1 sql=" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

           /** 照会結果の取得とsvf_formへ出力 **/
            final String frmFile = "KNJL362A".equals(_param._prgId) ? "KNJL362A.frm" : "KNJL362.frm";
            svf.VrSetForm(frmFile, 1);    //SuperVisualFormadeで設計したレイアウト定義態の設定

            final String sama = "3".equals(_param._finschoolDiv) || "4".equals(_param._finschoolDiv) ? "様" : "御中";

            int ia = Integer.parseInt(_param._poRow);    //行
            final int iaMax = "KNJL362A".equals(_param._prgId) ? 8 : 6;
            int ib = Integer.parseInt(_param._poCol);    //列
            while (rs.next()) {
                if (ib > 3) {
                    ib = 1;
                    ia++;
                    if (ia > iaMax) {
                        svf.VrEndPage();
                        log.debug("[KNJL362]set_detail1 check1");
                        nonedata = true;
                        ia = 1;
                    }
                }
                svf.VrsOutn("ZIPCODE"   + ib, ia, "〒" + rs.getString("ZIPCD"));   //郵便番号
                svf.VrsOutn("ADDRESS1_" + ib, ia, rs.getString("ADDRESS"));        //住所
                String nameSet = rs.getString("NAME") + _param.getSchoolPrint(rs.getString("NAME1"));
                if ("KINJUNIOR".equals(_param._jhcheck) || "KIN".equals(_param._jhcheck)) {
                    final int namecheck = rs.getString("NAME").indexOf("　");
                    if ((namecheck + 1) <= 6) {
                        nameSet = rs.getString("NAME").substring(namecheck + 1) + _param.getSchoolPrint(rs.getString("NAME1"));
                    }
                }
                svf.VrsOutn("SCHOOLNAME" + ib, ia, h_finschoolname(nameSet, sama));
                if (_param.isWakayama()) {
                    svf.VrsOutn("NO" + ib, ia, rs.getString("COMMON_CD")); //1枚のラベルシールの下の方に確認のための（受験番号、出身学校コード、塾コード）を小さく表示
                }
                ib++;
            }
            if (ib > 1) {
                svf.VrEndPage();
                nonedata = true;
            }
            svf.VrPrint();
        } catch (Exception ex) {
            log.error("[KNJL362]set_detail1 read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

    }   //set_detail1の括り



    /*-----------------------------------------------------------------*
     * 住所の編集
     *-----------------------------------------------------------------*/
    public String h_finschoolname(final String finschoolname1, final String finschoolname2) {
        final StringBuffer finschoolname = new StringBuffer();
        try {
            if (finschoolname1 != null) {
                finschoolname.append(finschoolname1);
                final byte[] SendB = finschoolname1.getBytes();

                int j = 0;
                if (SendB.length > 16) j = 2;
                if (SendB.length > 38) j = 3;
//                log.debug("[KNJL362]h_finschoolname SendB.length=" + SendB.length);

                for (int i = SendB.length; i < (22 * j - 4); i++) {
                    finschoolname.append(" ");
                }

                if (j == 0) {
                    finschoolname.append(" ");
                    finschoolname.append(" ");
                }
                if (finschoolname2 != null) {
                    finschoolname.append(finschoolname2);
                }
            }
        } catch (Exception ex) {
            log.error("[LBA130H]h_finschoolname error!", ex);
        }
        return finschoolname.toString();
    }  //h_finschoolnameの括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64128 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _poRow;
        final String _poCol;
        final String[] _classSelected;
        final String _prgId;
        final String _classInState;
        final String _finschoolDiv;
        final String _jhcheck;
        final String _z010SchoolCode;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _prgId = request.getParameter("PRGID");

            _finschoolDiv = request.getParameter("FINSCHOOLDIV");
            _jhcheck = getJhCheck(db2, "NAME1");
            _z010SchoolCode = getJhCheck(db2, "NAME2");

            //対象学校コードの編集
            _classSelected = request.getParameterValues("SCHOOL_SELECTED"); // 学校コード
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _classSelected.length; ia++) {
                if (_classSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_classSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _classInState = sbx.toString();
        }

        private String getJhCheck(final DB2UDB db2, String field) {
            final String jhsql = "SELECT "+field+" FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
            String jhcheck = "*";
            PreparedStatement ps = null;
            ResultSet jhrs = null;
            try {
                ps = db2.prepareStatement(jhsql);
                jhrs = ps.executeQuery();
                while (jhrs.next()) {
                    if (null != jhrs.getString(field)) {
                        jhcheck = jhrs.getString(field);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, jhrs);
            }
            return jhcheck;
        }
        
        public String getSchoolPrint(final String name1) {
            final String retSchool;
            if ("2".equals(_finschoolDiv) || "3".equals(_finschoolDiv) || "4".equals(_finschoolDiv)) {
                retSchool = "";
            } else {
                if ("CHIBEN".equals(_jhcheck)) {
                    retSchool = "";
                } else {
                    retSchool = name1;
                }
            }
            return retSchool;
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }
        
        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }

}   //クラスの括り


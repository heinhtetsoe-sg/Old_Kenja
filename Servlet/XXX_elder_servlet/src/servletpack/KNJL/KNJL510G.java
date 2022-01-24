// kanji=漢字
/*
 * $Id: c179a435257f619d5156237fc9dd1957cee69acb $
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;


public class KNJL510G extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJL510G.class);

    Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                  // Databaseクラスを継承したクラス
    boolean nonedata;           // 該当データなしフラグ


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
            log.warn("[KNJL510G]DB2 open error!");
            return;
        }

        try {
            // パラメータの取得
            _param = createParam(db2, request);

        } catch( Exception ex ) {
            log.error("[KNJL510G]parameter error!");
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
        StringBuffer stb  = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._finschoolDiv)) {
            stb.append(" VALUE(T1.EXAMNO,'') AS EXAMNO, ");
            stb.append(" VALUE(T1.ZIPCD,'') AS ZIPCD, ");
            stb.append(" VALUE(T1.ADDRESS1,'') AS ADDRESS1, ");
            stb.append(" VALUE(T1.ADDRESS2,'') AS ADDRESS2, ");
        } else if ("2".equals(_param._finschoolDiv)) {
            stb.append(" VALUE(T1.EXAMNO,'') AS EXAMNO, ");
            stb.append(" VALUE(T1.GZIPCD,'') AS ZIPCD, ");
            stb.append(" VALUE(T1.GADDRESS1,'') AS ADDRESS1, ");
            stb.append(" VALUE(T1.GADDRESS2,'') AS ADDRESS2, ");
        }
        stb.append(" VALUE(T2.NAME,'') AS NAME, ");
        stb.append(" VALUE(T1.GNAME,'') AS GNAME ");
        stb.append(" FROM ");
        stb.append("    ENTEXAM_APPLICANTADDR_DAT T1 ");
        stb.append("    INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "' AND ");
        stb.append("    T1.EXAMNO IN " + _param._classInState + " ");
        stb.append(" ORDER BY ");
        stb.append("    T1.EXAMNO ");
        sql = stb.toString();
        log.debug("KNJL510G]set_detail1 sql=" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

           /** 照会結果の取得とsvf_formへ出力 **/
            final String frmFile = "KNJL510G.frm";
            svf.VrSetForm(frmFile, 1);    //SuperVisualFormadeで設計したレイアウト定義態の設定

            int ia = Integer.parseInt(_param._poRow);    //行
            final int iaMax = 6;

            int ib = Integer.parseInt(_param._poCol);    //列
            while (rs.next()) {
                final String rsAddr1 = (String) rs.getString("ADDRESS1");
                final String rsAddr2 = (String) rs.getString("ADDRESS2");

                if (ib > 2) {
                    ib = 1;
                    ia++;
                    if (ia > iaMax) {
                        svf.VrEndPage();
                        log.debug("[KNJL510G]set_detail1 check1");
                        nonedata = true;
                        ia = 1;
                    }
                }
                svf.VrsOutn("ZIPCODE"   + ib, ia, "〒" + rs.getString("ZIPCD"));   //郵便番号
                final int check_len = KNJ_EditEdit.getMS932ByteLength(rsAddr1);
                final int check_len2 = KNJ_EditEdit.getMS932ByteLength(rsAddr2);
                if (check_len > 50 || check_len2 > 50) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_3" , ia, rsAddr1);     //住所
                    svf.VrsOutn("ADDRESS" + ib + "_2_3" , ia, rsAddr2);     //住所
                } else if (check_len > 40 || check_len2 > 40) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_2" , ia, rsAddr1);     //住所
                    svf.VrsOutn("ADDRESS" + ib + "_2_2" , ia, rsAddr2);     //住所
                } else if (check_len > 0 || check_len2 > 0) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_1" , ia, rsAddr1);     //住所
                    svf.VrsOutn("ADDRESS" + ib + "_2_1" , ia, rsAddr2);     //住所
                }
                //String nameSet = rs.getString("NAME") + _param.getSchoolPrint(rs.getString("NAME1"));
                if ("2".equals(_param._finschoolDiv)) {
                    final String gname = rs.getString("GNAME") + " 様";
                    final int gnameByte = KNJ_EditEdit.getMS932ByteLength(gname);
                    final String gnameFieldName = gnameByte > 30 ? "_2" : "";
                    svf.VrsOutn("NAME" + ib + "_1" + gnameFieldName, ia, gname);        //保護者氏名
                    final String name = rs.getString("NAME") + " 様";
                    final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                    final String nameFieldName = nameByte > 30 ? "_2" : "";
                    svf.VrsOutn("NAME" + ib + "_2" + nameFieldName, ia, name);         //志願者氏名
                } else {
                    final String name = rs.getString("NAME") + " 様";
                    final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                    final String nameFieldName = nameByte > 30 ? "_2" : "";
                    svf.VrsOutn("NAME" + ib + "_1" + nameFieldName, ia, name);        //保護者氏名
                }
                svf.VrsOutn("EXAM_NO" + ib, ia, rs.getString("EXAMNO"));         //志願者番号
                ib++;
            }
            if (ib > 1) {
                svf.VrEndPage();
                nonedata = true;
            }
            svf.VrPrint();
        } catch (Exception ex) {
            log.error("[KNJL510G]set_detail1 read error!", ex);
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
            log.error("[KNJL510G]h_finschoolname error!", ex);
        }
        return finschoolname.toString();
    }  //h_finschoolnameの括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65154 $");
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

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _prgId = request.getParameter("PRGID");

            _finschoolDiv = request.getParameter("FINSCHOOLDIV");

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
    }

}   //クラスの括り


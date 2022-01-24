// kanji=漢字
/*
 * $Id: f2d8b18211020d0b57736b0e0e92f138ff807878 $
 *
 * Copyright(C) 2007-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ;

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

/*
 *
 *  学校教育システム 賢者 [マスタ管理]
 *
 *                  ＜ＫＮＪＺ１００＞  小・中学校送付タックシール
 *
 *      ＊高校用と中学校用の相違個所    param[5]?[7]
 *
 * 2005.10.03 nakamoto １行しか出力されない不具合を修正
 *
 **/

public class KNJZ100 extends HttpServlet {
    Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                  // Databaseクラスを継承したクラス
    boolean nonedata;           // 該当データなしフラグ

    private static final Log log = LogFactory.getLog(KNJZ100.class);

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
            log.warn("[KNJZ100]DB2 open error!");
            return;
        }

        try {
            // パラメータの取得
            _param = createParam(db2, request);

        } catch( Exception ex ) {
            log.error("[KNJZ100]parameter error!");
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

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    /*----------------------------*
     * SVF出力                    *
     *----------------------------*/
    public void set_detail1()
                     throws ServletException, IOException
    {
        final StringBuffer stb = new StringBuffer();
        if ("2".equals(_param._finschoolDiv)) {
            stb.append("SELECT ");
            stb.append("    VALUE(T1.PRISCHOOL_ZIPCD,'') AS ZIPCD,");
            if ("jyoto".equals(_param._jhcheck)) {
                stb.append("     CASE WHEN T2.PRISCHOOL_CLASS_CD IS NULL THEN VALUE(T1.PRISCHOOL_ADDR1, '') ELSE VALUE(T2.PRISCHOOL_ADDR1, '') END AS ADDRESS1, ");
                stb.append("     CASE WHEN T2.PRISCHOOL_CLASS_CD IS NULL THEN VALUE(T1.PRISCHOOL_ADDR2, '') ELSE VALUE(T2.PRISCHOOL_ADDR2, '') END AS ADDRESS2, ");
                stb.append("     VALUE(T2.PRISCHOOL_NAME, '') AS NAME1, ");
                stb.append("     VALUE(T2.PRISCHOOL_CLASS_CD, '') AS PRISCHOOL_CLASS_CD, ");
            } else {
                stb.append("    VALUE(PRISCHOOL_ADDR1, '') || VALUE(PRISCHOOL_ADDR2, '') AS ADDRESS,");
                stb.append("    '' AS NAME1, ");
            }
            stb.append("    VALUE(T1.PRISCHOOL_NAME, '') AS NAME ");
            stb.append("FROM ");
            stb.append("    PRISCHOOL_MST T1 ");
            if ("jyoto".equals(_param._jhcheck)) {
                stb.append("     LEFT JOIN ");
                stb.append("         PRISCHOOL_CLASS_MST T2 ");
                stb.append("             ON T2.PRISCHOOLCD = T1.PRISCHOOLCD ");
            }
            stb.append("WHERE ");
            stb.append("    T1.PRISCHOOLCD IN " + _param._classInState + " ");
            stb.append("ORDER BY ");
            stb.append("    T1.PRISCHOOLCD ");
            if ("jyoto".equals(_param._jhcheck)) {
                stb.append("    , T2.PRISCHOOL_CLASS_CD ");
            }
        } else {
            stb.append("SELECT ");
            stb.append("    VALUE(FINSCHOOL_ZIPCD,'') AS ZIPCD,");
            if ("jyoto".equals(_param._jhcheck)) {
                stb.append("    VALUE(FINSCHOOL_ADDR1,'') AS ADDRESS1,");
                stb.append("    VALUE(FINSCHOOL_ADDR2,'') AS ADDRESS2,");
            } else {
                stb.append("    VALUE(FINSCHOOL_ADDR1,'') || VALUE(FINSCHOOL_ADDR2,'') AS ADDRESS,");
            }
            stb.append("    VALUE(FINSCHOOL_NAME, '') AS NAME, ");
            stb.append("    VALUE(T2.NAME1, '') AS NAME1 ");
            stb.append("FROM ");
            stb.append("    FINSCHOOL_MST T1 ");
            stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L019' AND T2.NAMECD2 = T1.FINSCHOOL_TYPE ");
            stb.append("WHERE ");
            stb.append("    FINSCHOOLCD IN " + _param._classInState + " ");
            if ("jyoto".equals(_param._jhcheck)) {
                if ("1".equals(_param._personnel)) {
                    stb.append("AND FINSCHOOL_STAFFCD IS NOT NULL ");
                }
            }
            stb.append("ORDER BY ");
            stb.append("    FINSCHOOLCD");
        }
        log.debug("[KNJZ100]set_detail1 sql=" + stb.toString());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

           /** 照会結果の取得とsvf_formへ出力 **/
            final String frmFile = "jyoto".equals(_param._jhcheck) ? "KNJZ100A_2.frm" : "KNJZ100A".equals(_param._prgId) ? "KNJZ100A.frm" : "KNJZ100.frm";
            svf.VrSetForm(frmFile, 1);    //SuperVisualFormadeで設計したレイアウト定義態の設定

            int ia = Integer.parseInt(_param._poRow);    //行
            final int iaMax = "jyoto".equals(_param._jhcheck) ? 6 : "KNJZ100A".equals(_param._prgId) ? 8 : 6;
            int ib = Integer.parseInt(_param._poCol);    //列
            final int ibMax = "jyoto".equals(_param._jhcheck) ? 2 : 3;
            while (rs.next()) {
                if (ib > ibMax) {
                    ib = 1;
                    ia++;
                    if (ia > iaMax) {
                        svf.VrEndPage();
                        log.debug("[KNJZ100]set_detail1 check1");
                        nonedata = true;
                        ia = 1;
                    }
                }
                svf.VrsOutn("ZIPCODE"   + ib, ia, "〒" + rs.getString("ZIPCD"));   //郵便番号
                if ("jyoto".equals(_param._jhcheck)) {
                    final String address1 = rs.getString("ADDRESS1");
                    final int addressByte1 = KNJ_EditEdit.getMS932ByteLength(address1);
                    final String addressFieldName1 = addressByte1 > 50 ? "3" : addressByte1 > 40 ? "2" : "1";
                    svf.VrsOutn("ADDRESS" + ib + "_1_" + addressFieldName1, ia, address1);        //住所１

                    final String address2 = rs.getString("ADDRESS2");
                    final int addressByte2 = KNJ_EditEdit.getMS932ByteLength(address2);
                    final String addressFieldName2 = addressByte2 > 50 ? "3" : addressByte2 > 40 ? "2" : "1";
                    svf.VrsOutn("ADDRESS" + ib + "_2_" + addressFieldName2, ia, address2);        //住所２
                } else {
                    if ("1".equals(_param._useAddrField2) && getMS932ByteLength(rs.getString("ADDRESS")) > 26 * 4) {
                        svf.VrsOutn("ADDRESS1_" + ib + "_2", ia, rs.getString("ADDRESS"));        //住所
                    } else {
                        svf.VrsOutn("ADDRESS1_" + ib, ia, rs.getString("ADDRESS"));        //住所
                    }
                }
                if ("jyoto".equals(_param._jhcheck) && "1".equals(_param._finschoolDiv)) {
                    String name = rs.getString("NAME");
                    if ("1".equals(_param._toPrincipal)) {
                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                        final String nameFieldName = nameByte > 26 ? "2" : "1";
                        svf.VrsOutn("SCHOOL_NAME" + ib + "_1_" + nameFieldName, ia, name);
                    } else {
                        name += "　御中";
                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                        final String nameFieldName = nameByte > 26 ? "2" : "1";
                        svf.VrsOutn("SCHOOL_NAME" + ib + "_1_" + nameFieldName, ia, name);
                    }
                }
                String nameSet;
                if ("jyoto".equals(_param._jhcheck) && "2".equals(_param._finschoolDiv)) {
                    if ("".equals(rs.getString("PRISCHOOL_CLASS_CD"))) {
                        nameSet = rs.getString("NAME");
                    } else {
                        nameSet = rs.getString("NAME") + "　" + rs.getString("NAME1");
                    }
                } else {
                    nameSet = rs.getString("NAME") + _param.getSchoolPrint(rs.getString("NAME1"));
                }

                if ("KINJUNIOR".equals(_param._jhcheck) || "KIN".equals(_param._jhcheck)) {
                    final int namecheck = rs.getString("NAME").indexOf("　");
                    if ((namecheck + 1) <= 6) {
                        nameSet = rs.getString("NAME").substring(namecheck + 1) + _param.getSchoolPrint(rs.getString("NAME1"));
                    }
                }

                if ("jyoto".equals(_param._jhcheck) && "1".equals(_param._finschoolDiv)) {
                    if ("1".equals(_param._toPrincipal)) {
                        svf.VrsOutn("SCHOOL_NAME" + ib + "_2_1", ia, "学校長　殿");
                    } else {
                        // 空欄
                    }
                } else {
                    if ("jyoto".equals(_param._jhcheck)) {
                        nameSet += "　御中";
                        final int nameSetByte = KNJ_EditEdit.getMS932ByteLength(nameSet);
                        final String nameSetFieldName = nameSetByte > 36 ? "2" : "1";
                        svf.VrsOutn("SCHOOL_NAME" + ib + "_2_" + nameSetFieldName, ia, nameSet);
                    } else {
                        svf.VrsOutn("SCHOOLNAME" + ib, ia, h_finschoolname(nameSet, "御中"));
                    }
                }
                ib++;
            }
            if (ib > 1) {
                svf.VrEndPage();
                nonedata = true;
            }
            svf.VrPrint();
        } catch (Exception ex) {
            log.error("[KNJZ100]set_detail1 read error!", ex);
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
                final String principalSuffix = _param._toPrincipal != null ? "長" : "" ;
                final int suffixLen = principalSuffix.getBytes().length;
                finschoolname.append(principalSuffix);
                final byte[] SendB = finschoolname1.getBytes();

                int j = 0;
                if (SendB.length + suffixLen > 24 && SendB.length + suffixLen <= 33) j = 1;
                if (SendB.length + suffixLen > 33) j = 2;
                if (SendB.length + suffixLen > 66) j = 3;
//                log.debug("[KNJZ100]h_finschoolname SendB.length=" + SendB.length);

                for (int i = SendB.length + suffixLen; i < (33 * j); i++) {
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
        log.fatal("$Revision: 69122 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        final String _year;
        final String _semester;
        final String _poRow;
        final String _poCol;
        final String[] _classSelected;
        final String _prgId;
        final String _classInState;
        final String _finschoolDiv;
        final String _jhcheck;
        final String _useAddrField2;
        final String _notPrintFinschooltypeName;
        final String _toPrincipal;
        final String _personnel;
        final String _schoolType;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _prgId = request.getParameter("PRGID");

            _finschoolDiv = request.getParameter("FINSCHOOLDIV");
            _jhcheck = getJhCheck(db2);

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
            _useAddrField2 = request.getParameter("useAddrField2");
            _notPrintFinschooltypeName = request.getParameter("notPrintFinschooltypeName");
            _toPrincipal = request.getParameter("TO_PRINCIPAL");
            _personnel = request.getParameter("PERSONNEL");
            _schoolType = request.getParameter("SCHOOL_TYPE");
        }

        private String getJhCheck(final DB2UDB db2) {
            final String jhsql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
            String jhcheck = "*";
            PreparedStatement ps = null;
            ResultSet jhrs = null;
            try {
                ps = db2.prepareStatement(jhsql);
                jhrs = ps.executeQuery();
                while (jhrs.next()) {
                    if (null != jhrs.getString("NAME1")) {
                        jhcheck = jhrs.getString("NAME1");
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
            if ("2".equals(_finschoolDiv)) {
                retSchool = "";
            } else {
                if ("CHIBEN".equals(_jhcheck) || "tokiwa".equals(_jhcheck) || "miyagiken".equals(_jhcheck) || "1".equals(_notPrintFinschooltypeName)) {
                    retSchool = "";
                } else {
                    retSchool = name1;
                }
            }
            return retSchool;
        }
    }

}   //クラスの括り


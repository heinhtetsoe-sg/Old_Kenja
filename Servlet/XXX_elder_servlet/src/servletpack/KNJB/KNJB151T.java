package servletpack.KNJB;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 *  学校教育システム 賢者 [時間割管理]
 *
 *                  ＜ＫＮＪＢ１５１Ｔ＞  生徒別教科書購入表（東京都）
 *
 *  2006/11/17 m-yama 作成日
 *  @version $Id: d7e4b235090fbdd6e36d1286f81fc97d83f8d46d $
 *  Copyright(C) 2006-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

public class KNJB151T extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJB151T.class);

    final Map _hmparam = new HashMap();

    /**
     * KNJM.classから最初に呼ばれる処理。
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
            setParam(request);
            final String[] schregno = request.getParameterValues("category_selected");
            for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                log.debug(key + " = " + _hmparam.get(key));
            }


            //SVF出力
            int i = 0;
            while (i < schregno.length) {
                svf.VrSetForm("KNJB151T.frm", 4);
                if (printMain(db2, svf, schregno[i])) {
                    nonedata = true;
                }
                i++;
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

    private void setParam(final HttpServletRequest request) {
        _hmparam.put("YEAR", request.getParameter("YEAR"));
        _hmparam.put("SEMESTER", request.getParameter("SEMESTER"));
    }

    /**
     * ＳＶＦ出力
     */
    private boolean printMain(
        final DB2UDB db2,
        final Vrw32alp svf,
        final String schno
    ) throws Exception {

        boolean nonedata = false;
        final PreparedStatement ps = db2.prepareStatement(mainSql(schno));
        final ResultSet rs = ps.executeQuery();
        try {
            //年度
            svf.VrsOut("NENDO1", _hmparam.get("YEAR") + "年度");

            int totalprice = 0;         //合計金額
            int syouprice  = 0;         //小計金額
            int syoukei    = 0;         //小計出力判定
            int gyo        = 0;         //行
            String bfdata  = "0";       //前回コード
            String afdata  = "0";       //現在コード

            while (rs.next()) {

                bfdata = String.valueOf(afdata);
                afdata = rs.getString("TEXTBOOKDIV");

                //出力前処理
                syoukei = judgementTextDiv(bfdata, afdata, gyo);
                gyo = gyoClear(gyo);
                printHead(svf, rs);

                //小計出力
                if (syoukei == 1) {
                    gyo = putSyoukei(svf, gyo, syouprice);
                    syouprice = 0;
                }
                //先頭データと小計出力時は、タイトル印字
                if (gyo == 0 || syoukei == 1) {
                    gyo = putTitle(svf, gyo, afdata);
                    syoukei = 0;
                }
                //明細出力
                gyo = printMeisai(svf, rs, gyo);
                //合計金額を計算
                if (rs.getString("TEXTBOOKPRICE") != null) {
                    syouprice  = syouprice  + Integer.parseInt(rs.getString("TEXTBOOKPRICE"));
                    totalprice = totalprice + Integer.parseInt(rs.getString("TEXTBOOKPRICE"));
                }
                nonedata = true; //該当データなしフラグ
            }
            if (nonedata) {
                putLastData(svf, gyo, syouprice, totalprice);
            }
        } finally {
            DbUtils.close(rs);
            DbUtils.close(ps);
        }
        return nonedata;
    }

    /** 行のクリア gyo == 50の時0を返す */
    private int gyoClear(final int gyo) {
        int retgyo = gyo;
        if (retgyo == 50) {
            retgyo = 0;
        }
        return retgyo;
    }

    /** 小計出力判定 */
    private int judgementTextDiv(
            final String bfdata,
            final String afdata,
            final int gyo) {
        int syoukei = 0;
        //先頭データ以外で、教科書区分が不一致
        if (!bfdata.equalsIgnoreCase(afdata) && gyo > 0) {
            syoukei = 1;
        }
        return syoukei;
    }

    /** ヘッダ出力 */
    private void printHead(final Vrw32alp svf, final ResultSet rs) throws SQLException {

        svf.VrsOut("HR_NAME" , rs.getString("HR_NAME"));                             //組名称
        svf.VrsOut("ATTENDNO", Integer.parseInt(rs.getString("ATTENDNO")) + "番");   //出席番号
        svf.VrsOut("NAME1"   , rs.getString("NAME_SHOW"));                           //生徒氏名
    }

    /** 空行出力 */
    private int printNullLine(final Vrw32alp svf, final int gyo) {
        int retgyo = gyo;
        svf.VrsOut("SPACE1", "空");
        svf.VrEndRecord();
        retgyo++;
        return retgyo;
    }

    /** タイトル設定 */
    private void printTitle(final Vrw32alp svf, final String afdata) {
        String titlenm  = "教科書名";
        if (afdata.equals("2")) {
            titlenm = "学習書名";
        } else if (afdata.equals("3")) {
            titlenm = "副教材名";
        } else if (afdata.equals("4")) {
            titlenm = "地図";
        }
        svf.VrsOut("KOTEI1"     , "教科書");
        svf.VrsOut("KOTEI2"     , "コード");
        svf.VrsOut("KOTEI3"     , "発行社");
        svf.VrsOut("KOTEI4"     , "定価");
        svf.VrsOut("KOTEI5"     , "講座");
        svf.VrsOut("KOTEI6"     , "コード");
        svf.VrsOut("KOTEI7"     , "講座名");
        svf.VrsOut("TEXT_TITLE"     , titlenm);

        svf.VrEndRecord();
    }
    
    public static int byteCountMS932(final String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }

    /** 明細印字 */
    private int printMeisai(
            final Vrw32alp svf,
            final ResultSet rs,
            final int gyo
    ) throws SQLException {
        int retgyo = gyo;
        final int checkCount = byteCountMS932(rs.getString("TEXTBOOKNAME"));
        String field1 = "1";
        String field2 = "";
        if (checkCount > 40) {
            field1 = "2";
            field2 = "2";
        }
        svf.VrsOut("TEXTCD"       + field1, rs.getString("TEXTBOOKCD"));       //教科書番号
        if (checkCount > 40) {
            svf.VrsOut("TEXTNAME" + field1 + "_1", rs.getString("TEXTBOOKNAME"));     //教科書名
        } else {
            svf.VrsOut("TEXTNAME" + field1, rs.getString("TEXTBOOKNAME"));     //教科書名
        }
        svf.VrsOut("ISSUECOMPANY" + field1, rs.getString("ISSUECOMPANYNAME")); //発行社
        svf.VrsOut("TEXTPRICE"    + field1, rs.getString("TEXTBOOKPRICE"));    //定価
        svf.VrsOut("CHAIRCD"      + field2, rs.getString("CHAIRCD"));          //講座コード
        svf.VrsOut("CHAIRNAME"    + field2, rs.getString("CHAIRNAME"));        //講座名
        svf.VrEndRecord();
        if (checkCount > 40) {
            retgyo += 2;
        } else {
            retgyo++;
        }
        retgyo = gyoClear(retgyo);

        return retgyo;
    }

    /** タイトル出力処理 */
    private int putTitle(final Vrw32alp svf, final int gyo, final String afdata) {
        int retgyo = gyo;
        if (retgyo > 47) {
            for (int i = retgyo; i < 50; i++) {
                retgyo = printNullLine(svf, retgyo);
            }
            retgyo = 0;
        }
        printTitle(svf, afdata);
        retgyo = retgyo + 2;
        retgyo = gyoClear(retgyo);
        return retgyo;
    }

    /** 小計出力処理 */
    private int putSyoukei(final Vrw32alp svf, final int gyo, final int syouprice) {
        int retgyo = gyo;
        retgyo = printTotal(svf, "小計", String.valueOf(syouprice), retgyo);
        retgyo = printNullLine(svf, retgyo);
        retgyo = gyoClear(retgyo);
        return retgyo;
    }

    /** 最終データ出力処理 */
    private void putLastData(
            final Vrw32alp svf,
            final int gyo,
            final int syouprice,
            final int totalprice
    ) {
        int retgyo = gyo;
        retgyo = printTotal(svf, "小計", String.valueOf(syouprice), retgyo);
        retgyo = printTotal(svf, "合計", String.valueOf(totalprice), retgyo);
        if (retgyo > 0) {
            for (int lastcnt = retgyo; lastcnt < 50; lastcnt++) {
                printNullLine(svf, retgyo);
            }
        }
    }

    /** 小計合計をセット */
    private int printTotal(
            final Vrw32alp svf,
            final String totalName,
            final String totalVal,
            final int gyo
    ) {
        int retgyo = gyo;
        svf.VrsOut("kei"      , totalName);
        svf.VrsOut("kingaku"  , totalVal);
        svf.VrEndRecord();
        retgyo++;
        if (retgyo == 50) {
            retgyo = 0;
        }
        return retgyo;
    }

    /**データ　取得**/
    //  CSOFF: ExecutableStatementCount
    private String mainSql(final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    L7.HR_NAME, ");
        stb.append("    L5.NAME_SHOW, ");
        stb.append("    L6.ATTENDNO, ");
        stb.append("    L3.TEXTBOOKDIV, ");
        stb.append("    L1.TEXTBOOKCD, ");
        stb.append("    L3.TEXTBOOKNAME, ");
        stb.append("    L3.ISSUECOMPANYCD, ");
        stb.append("    L4.ISSUECOMPANYNAME, ");
        stb.append("    L3.TEXTBOOKPRICE, ");
        stb.append("    T1.CHAIRCD, ");
        stb.append("    L2.CHAIRNAME ");
        stb.append("FROM ");
        stb.append("    (SELECT DISTINCT ");
        stb.append("        TT1.YEAR, ");
        stb.append("        TT1.SEMESTER, ");
        stb.append("        TT1.CHAIRCD, ");
        stb.append("        TT1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("        CHAIR_STD_DAT TT1 ");
        stb.append("     WHERE ");
        stb.append("        TT1.YEAR = '").append(_hmparam.get("YEAR")).append("' ");
        stb.append("        AND TT1.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("' ");
        stb.append("        AND TT1.SCHREGNO = '").append(schno).append("' ");
        stb.append("    ) T1 ");
        stb.append("    LEFT JOIN CHAIR_TEXTBOOK_DAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("         AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("         AND L1.CHAIRCD = T1.CHAIRCD ");
        stb.append("    LEFT JOIN CHAIR_DAT L2 ON L2.CHAIRCD = T1.CHAIRCD ");
        stb.append("         AND L2.YEAR = T1.YEAR ");
        stb.append("         AND L2.SEMESTER = T1.SEMESTER ");
        stb.append("    LEFT JOIN TEXTBOOK_MST L3 ON L3.TEXTBOOKCD = L1.TEXTBOOKCD ");
        stb.append("    LEFT JOIN ISSUECOMPANY_MST L4 ON L4.ISSUECOMPANYCD = L3.ISSUECOMPANYCD ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT L6 ON L6.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L6.YEAR = T1.YEAR ");
        stb.append("         AND L6.SEMESTER = T1.SEMESTER ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT L7 ON L7.YEAR = T1.YEAR ");
        stb.append("         AND L7.SEMESTER = T1.SEMESTER ");
        stb.append("         AND L7.GRADE = L6.GRADE ");
        stb.append("         AND L7.HR_CLASS = L6.HR_CLASS ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '").append(_hmparam.get("YEAR")).append("' ");
        stb.append("    AND T1.SEMESTER = '").append(_hmparam.get("SEMESTER")).append("' ");
        stb.append("    AND T1.SCHREGNO = '").append(schno).append("' ");
        stb.append("    AND L1.YEAR = T1.YEAR ");
        stb.append("    AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("    AND L1.CHAIRCD = T1.CHAIRCD ");
        stb.append("ORDER BY ");
        stb.append("    L3.TEXTBOOKDIV, ");
        stb.append("    L1.TEXTBOOKCD ");

        log.debug(stb);

        return stb.toString();

    }
    //  CSON: ExecutableStatementCount

}

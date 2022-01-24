// kanji=漢字
/*
 * $Id: 7e9ea7abd27d82eb69c9e854044869f2ee9b8e8a $
 *
 * 作成日: 2005/07/29 11:25:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１３＞  入学試験志願者データチェックリスト
 *
 *  2005/07/29 nakamoto 作成日
 *  2005/08/23 nakamoto 性別は「男・女」で印字
 *  2005/08/28 nakamoto 専併を追加（高校のみ）
 *  2005/12/20 m-yama   NO001 通知承諾を印字
 *  2005/12/29 nakamoto NO002 生年月日 --> 'H02.01.08'表記に変更
 *  2006/01/14 nakamoto NO003 (中学)'現住所コード'と'所在地コード' --> '所在地名称'表記に変更
 *  2006/01/14 nakamoto NO004 (高校)'現住所コード'と'所在地コード' --> '所在地名称'表記に変更
 *  2006/01/14 nakamoto NO005 NO004をもとに戻す。(高校)'現住所コード'と'所在地コード' --> 'コード'表記に変更
 *  2006/01/17 m-yama   NO006 '現住所コード'と'所在地コード' --> 'コード'+' '+'名称'に変更
 *  2006/01/25 nakamoto NO007 ○志願者基礎データの出願区分が'2'の者を除く
 *  2006/10/24 m-yama   NO008 前期の受験番号3000より大きいデータで改ページする。
 * @author nakamoto
 * @version $Id: 7e9ea7abd27d82eb69c9e854044869f2ee9b8e8a $
 */
public class KNJL313K {


    private static final Log log = LogFactory.getLog(KNJL313K.class);

    /**
     * KNJL.classから最初に起動されるクラス
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        final String[] param = new String[10];
        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());

        try {
            //  パラメータの取得
            param[0] = request.getParameter("YEAR"); //次年度
            param[1] = request.getParameter("TESTDIV"); //試験区分 99:全て
            param[3] = request.getParameter("JHFLG"); //中学/高校フラグ 1:中学,2:高校
            param[4] = request.getParameter("OUTPUT1"); //氏名出力有り
            param[5] = request.getParameter("OUTPUT2"); //かな氏名出力有り
            param[9] = request.getParameter("SPECIAL_REASON_DIV"); //特別理由

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

            getHeaderData(db2, svf, param); //ヘッダーデータ抽出メソッド

            for (int ia = 0; ia < param.length; ia++) {
                log.debug("param[" + ia + "]=" + param[ia]);
            }

            //SVF出力

            if (printMain(db2, svf, param)) {
                nonedata = true;
            }

            log.debug("nonedata=" + nonedata);

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "");
                svf.VrEndPage();
            }
        } finally {
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }


    }

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[9] + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }
    
    /**ヘッダーデータを抽出*/
    private void getHeaderData(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param
    ) throws Exception {

        //  フォーム---2005.08.28
        if (param[3].equals("1")) {
            svf.VrSetForm("KNJL313_1.frm", 1);
        }
        if (param[3].equals("2")) {
            svf.VrSetForm("KNJL313_2.frm", 1);
        }

        //  次年度
        param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";

        //  学校
        if (param[3].equals("1")) {
            param[6] = "中学校";
        }
        if (param[3].equals("2")) {
            param[6] = "高等学校";
        }

        //  作成日
        final String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        final String[] arr_ctrl_date = new String[3];
        int number = 0;
        while (rs.next()) {
            arr_ctrl_date[number] = rs.getString(1);
            number++;
        }
        db2.commit();
        param[8] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0]) + arr_ctrl_date[1] + "時" + arr_ctrl_date[2] + "分" + "　現在";
    }


    /**印刷処理メイン*/
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param
    ) throws Exception {
        boolean nonedata = false;

        //総ページ数
        final int[] total_page = new int[3];
        getTotalPage(db2, svf, param, total_page);

        //明細データ
        if (printMeisai(db2, svf, param, total_page)) {
            nonedata = true;
        }

        return nonedata;

    }


    /**試験区分毎の総ページ数*/
    private void getTotalPage(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final int[] totalpage
    ) throws Exception {
        log.debug("TotalPage start!");
        db2.query(statementTotalPage(param));
        final ResultSet rs = db2.getResultSet();
        log.debug("TotalPage end!");

        int cnt = 0;
        while (rs.next()) {
            totalpage[cnt] = rs.getInt("COUNT");
            cnt++;
        }
        rs.close();
        db2.commit();
    }


    /**明細データ印刷処理*/
    private boolean printMeisai(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final int[] totalpage
    ) throws Exception {
        boolean nonedata = false;
        boolean examflg  = false;   //NO008
        boolean firstflg = true;    //NO008
        db2.query(statementMeisai(param));
        final ResultSet rs = db2.getResultSet();

        int gyo = 0;
        int sex_cnt = 0;    //合計
        int sex1_cnt = 0;   //男
        int sex2_cnt = 0;   //女
        int page_cnt = 1;   //ページ数
        int page_arr = 0;   //総ページ数配列No
        String testdiv = "d";
        while (rs.next()) {
            //１ページ印刷
            if (49 < gyo
                    || (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV")))
                    || (!firstflg && !examflg && rs.getString("EXAMDIV").equals("1"))) {
                //合計印刷
                if (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))
                        || (!examflg && rs.getString("EXAMDIV").equals("1"))) {
                    printTotal(svf, param, sex1_cnt, sex2_cnt, sex_cnt);     //合計出力のメソッド
                }
                svf.VrEndPage();
                page_cnt++;
                gyo = 0;
                if (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))
                        || (!examflg && rs.getString("EXAMDIV").equals("1"))) {
                    sex_cnt = 0; sex1_cnt = 0; sex2_cnt = 0; page_cnt = 1; page_arr++;
                }
                if (!examflg && rs.getString("EXAMDIV").equals("1")) {
                    examflg = true;
                }
            }
            //見出し
            printHeader(db2, svf, param, rs, page_cnt, totalpage, page_arr);
            //明細データをセット
            printExam(svf, param, rs, gyo);
            //性別
            sex1_cnt = sexCnt(rs, "1", sex1_cnt);
            sex2_cnt = sexCnt(rs, "2", sex2_cnt);
            sex_cnt = sex1_cnt + sex2_cnt;

            if (!examflg && rs.getString("EXAMDIV").equals("1")) {
                examflg = true;
            }
            testdiv = rs.getString("TESTDIV");
            gyo++;
            nonedata = true;
            firstflg = false;
        }
        //最終ページ印刷
        if (nonedata) {
            printTotal(svf, param, sex1_cnt, sex2_cnt, sex_cnt);     //合計出力のメソッド
            svf.VrEndPage();
        }
        rs.close();
        return nonedata;

    }

    /**性別カウンタ*/
    private int sexCnt(
            final ResultSet rs,
            final String sex,
            final int cnt
    ) throws SQLException {
        int sexcnt = cnt;
        if ((rs.getString("SEX")).equals(sex)) {
            sexcnt++;
        }
        return sexcnt;
    }

    /**ヘッダーデータをセット*/
    private void printHeader(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final ResultSet rs,
            final int pagecnt,
            final int[] totalpage,
            final int pagearr
    ) throws SQLException {
        svf.VrsOut("NENDO"        , param[7]);
        svf.VrsOut("SCHOOLDIV"    , param[6]);
        svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME"));
        svf.VrsOut("DATE"         , param[8]);

        svf.VrsOut("PAGE"         , String.valueOf(pagecnt));
        svf.VrsOut("TOTAL_PAGE"   , String.valueOf(totalpage[pagearr]));

        setInfluenceName(db2, svf, param);
    }


    /**明細データをセット*/
    private void printExam(
            final Vrw32alp svf,
            final String[] param,
            final ResultSet rs,
            final int gyo
    ) throws SQLException {
        String len2 = "0";
        String len3 = "0";
        String len4 = "0";
        String len5 = "0";
        String len6 = "0";

        len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
        len3 = (10 < (rs.getString("NAME_KANA")).length()) ? "2" : "1" ;
        len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;
        len5 = (10 < (rs.getString("GKANA")).length()) ? "2" : "1" ;
        len6 = (10 < (rs.getString("GNAME")).length()) ? "2" : "1" ;

        svf.VrsOutn("EXAMNO"          , gyo + 1    , rs.getString("EXAMNO"));
        svf.VrsOutn("SHDIV"           , gyo + 1    , rs.getString("SHDIV_NAME"));   //2005.08.28
        svf.VrsOutn("DESIREDIV"       , gyo + 1    , rs.getString("DESIREDIV"));
        svf.VrsOutn("SEX"             , gyo + 1    , rs.getString("SEX_NAME"));
        svf.VrsOutn("BIRTHDAY"        , gyo + 1    , setBirthday(rs.getString("BIRTHDAY")));    //NO002
        svf.VrsOutn("ADDRESSCD"       , gyo + 1    , rs.getString("ADDRESSCD") + " " + rs.getString("ADDRESSNAME"));    //NO003 NO005 NO006
        svf.VrsOutn("TELNO"           , gyo + 1    , rs.getString("TELNO"));
        svf.VrsOutn("LOCATIONCD"      , gyo + 1    , rs.getString("LOCATIONCD") + " " + rs.getString("LOCATIONNAME"));  //NO003 NO005 NO006
        svf.VrsOutn("NATPUBPRIDIV"    , gyo + 1    , rs.getString("NATPUB_NAME"));
        svf.VrsOutn("G_TELNO"         , gyo + 1    , rs.getString("GTELNO"));
        svf.VrsOutn("NOTICE"          , gyo + 1    , rs.getString("APPROVAL_FLG")); //NO001

        if (param[5] != null) {
            svf.VrsOutn("KANA" + len3       , gyo + 1    , rs.getString("NAME_KANA"));
        }
        if (param[4] != null) {
            svf.VrsOutn("NAME" + len2       , gyo + 1    , rs.getString("NAME"));
        }
        svf.VrsOutn("FINSCHOOL" + len4  , gyo + 1    , rs.getString("FINSCHOOL_NAME"));
        svf.VrsOutn("G_KANA" + len5     , gyo + 1    , rs.getString("GKANA"));
        svf.VrsOutn("G_NAME" + len6     , gyo + 1    , rs.getString("GNAME"));

    }


    /**合計をセット*/
    private void printTotal(
            final Vrw32alp svf,
            final String[] param,
            final int sex1cnt,
            final int sex2cnt,
            final int sexcnt
    ) {
        svf.VrsOut("TOTAL_MEMBER" , "男" + String.valueOf(sex1cnt) + "名、"
                + "女" + String.valueOf(sex2cnt) + "名、"
                + "合計" + String.valueOf(sexcnt) + "名");
    }


    /**
     *  生年月日をセット
     *
     * ※「H02.01.08」表記に変更---NO002
     * NO002:平成元年の場合、'H00'となっていたので、'H01'と修正
     */
    private String setBirthday(final String birthday) {
        String ret_val = "";
        if (birthday != null) {
            final String wareki_date = KNJ_EditDate.h_format_JP(birthday);
            final String[] bunkatu_date = KNJ_EditDate.tate_format(wareki_date);

            String gengou = "";
            if (bunkatu_date[0].equals("平成")) {
                gengou = "H";
            }
            if (bunkatu_date[0].equals("昭和")) {
                gengou = "S";
            }
            if (bunkatu_date[0].equals("平成元年")) {
                gengou = "H";
            }
            if (bunkatu_date[0].equals("平成元年")) {
                bunkatu_date[1] = "1";  //NO002
            }

            for (int i = 1; i < 4; i++) {
                if (Integer.parseInt(bunkatu_date[i]) < 10) {
                    bunkatu_date[i] = "0" + bunkatu_date[i];
                }
            }

            ret_val = gengou + bunkatu_date[1] + "." + bunkatu_date[2] + "." + bunkatu_date[3];
        }
        return ret_val;
    }


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        //志願者基礎データ
        stb.append("WITH EXAM_BASE AS ( ");
        stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,SHDIV, ");
        //NO008
        if (param[3].equals("1")) {
            stb.append("           CASE WHEN TESTDIV = '1' AND EXAMNO >= '3000' ");
            stb.append("           THEN '1' ");
            stb.append("           ELSE '0' END AS EXAMDIV, ");
        } else {
            stb.append("           '0' AS EXAMDIV, ");
        }
        stb.append("           NAME_KANA,NAME,SEX,BIRTHDAY, ");
        stb.append("           ADDRESSCD,TELNO,LOCATIONCD,NATPUBPRIDIV,FS_CD,APPROVAL_FLG, ");  //NO001
        stb.append("           GNAME,GKANA,GTELNO ");
        stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("    WHERE  ENTEXAMYEAR = '" + param[0] + "' ");
        if (!"9".equals(param[9])) {
            stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
        }
        //試験区分
        if (!param[1].equals("99")) {
            stb.append("       AND TESTDIV = '" + param[1] + "' ");
        }
        stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");  //NO007
        stb.append("    ) ");

        //メイン
        stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME,T2.EXAMNO,T2.EXAMDIV,T2.DESIREDIV,N5.ABBV1 AS SHDIV_NAME, ");  //2005.08.28
        stb.append("       VALUE(T2.NAME_KANA,'') AS NAME_KANA,VALUE(T2.NAME,'') AS NAME, ");
        stb.append("       N1.ABBV1 AS SEX_NAME, ");    //2005.08.23
        stb.append("       VALUE(T2.SEX,'0') AS SEX,T2.BIRTHDAY, ");
        stb.append("       T2.ADDRESSCD,T2.TELNO,T2.LOCATIONCD, ");
        stb.append("       N6.NAME1 AS ADDRESSNAME,N7.NAME1 AS LOCATIONNAME, ");    //NO003
        stb.append("       T2.NATPUBPRIDIV, N2.ABBV1 AS NATPUB_NAME, ");
        stb.append("       T2.FS_CD,APPROVAL_FLG, VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");    //NO001
        stb.append("       VALUE(T2.GNAME,'') AS GNAME, VALUE(T2.GKANA,'') AS GKANA, T2.GTELNO ");
        stb.append("FROM   EXAM_BASE T2 ");
        stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T2.SEX ");
        stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T2.NATPUBPRIDIV ");
        stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
        stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");   //2005.08.28
        stb.append("       LEFT JOIN NAME_MST N6 ON N6.NAMECD1='L007' AND N6.NAMECD2=T2.ADDRESSCD ");   //NO003
        stb.append("       LEFT JOIN NAME_MST N7 ON N7.NAMECD1='L007' AND N7.NAMECD2=T2.LOCATIONCD ");  //NO003
        stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
        stb.append("ORDER BY T2.TESTDIV,T2.EXAMNO ");

        return stb.toString();

    }


    /**
     *  試験区分毎の総ページ数を取得
     *
     */
    private String statementTotalPage(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        //志願者基礎データ
        stb.append("WITH EXAM_BASE AS ( ");
        stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV, ");
        //NO008
        if (param[3].equals("1")) {
            stb.append("           CASE WHEN TESTDIV = '1' AND EXAMNO >= '3000' "); //NO008
            stb.append("           THEN '1' ");
            stb.append("           ELSE '0' END AS EXAMDIV, ");
        } else {
            stb.append("           '0' AS EXAMDIV, ");
        }
        stb.append("           NAME_KANA,NAME,SEX,BIRTHDAY, ");
        stb.append("           ADDRESSCD,TELNO,LOCATIONCD,NATPUBPRIDIV,FS_CD, ");
        stb.append("           GNAME,GKANA,GTELNO ");
        stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("    WHERE  ENTEXAMYEAR = '" + param[0] + "' ");
        if (!"9".equals(param[9])) {
            stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
        }
        //試験区分
        if (!param[1].equals("99")) {
            stb.append("       AND TESTDIV = '" + param[1] + "' ");
        }
        stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");  //NO007
        stb.append("    ) ");

        //メイン
        stb.append("SELECT TESTDIV,EXAMDIV, ");
        stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
        stb.append("FROM   EXAM_BASE ");
        stb.append("GROUP BY TESTDIV,EXAMDIV ");
        stb.append("ORDER BY TESTDIV,EXAMDIV ");

        return stb.toString();

    }



}

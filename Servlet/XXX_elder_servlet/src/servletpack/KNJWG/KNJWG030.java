// kanji=漢字
/*
 * $Id: 1adde586a977b942643b43715ae5ae3fb48535d2 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *
 *  学校教育システム 賢者 [事務管理]  単位修得証明書
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *
 */

public class KNJWG030 {
    private static final Log log = LogFactory.getLog(KNJWG030.class);  //05/11/18
    Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                         //Databaseクラスを継承したクラス
    int ret;                            //ＳＶＦ応答値
    boolean nonedata;                   //該当データなしフラグ
    String param[];
    String pschregno[];
    final private KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定 06/03/23

    /*
     * HTTP Get リクエストの処理
     **/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        //  パラメータの取得
        String dbname = request.getParameter("DBNAME");                 //データベース名
        pschregno = request.getParameterValues("category_selected");    //学籍番号
        param = new String[8];
        Map paramap = new HashMap();        //HttpServletRequestからの引数 05/11/18
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("SEMESTER");        //学期

            //記載日がブランクの場合桁数０で渡される事に対応 05/11/22 Modify
            if( request.getParameter("DATE") != null  &&  3 < request.getParameter("DATE").length() )
                param[6] = request.getParameter("DATE");            //記載日

            paramap.put( "CTRL_YEAR", request.getParameter("CTRL_YEAR") );  //今年度  05/11/18
            paramap.put("CERTIFKIND", "011");  // 証明書種別
        } catch( Exception ex ) {
            log.error("[KNJWG030]parameter error!" + ex);
        }
        if( _definecode.schoolmark.equals("TOK")) {
//            param[7] = getCertificateNum(db2,param[0]);  // 証明書番号セット
        }

        //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //  svf設定
        ret = svf.VrInit();                         //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        //  ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJWG030]DB2 open error!" + ex);
        }

        //  ＳＶＦ作成
        student( paramap );     //在校生のPAPER作成  05/11/18Modify

        //  終了処理
        if( nonedata == true )  ret = svf.VrQuit();
        db2.commit();
        db2.close();
        outstrm.close();
    }

    /**
     * 在校生のPAPER作成
     **/
    public void student(Map paramap) throws SQLException {
        if (log.isDebugEnabled()) {
            for (int ia = 0; ia < param.length; ia++) {
                log.debug("[KNJWG030]param[" + ia + "]=" + param[ia]);
            }
        }
        KNJWG030_1 pobj = new KNJWG030_1(db2, svf);                    //ＳＶＦ出力用のインスタンス作成
        final String certifkind = "011";
        pobj.pre_stat();                                        //PrepareStatement作成
        for (int len = 0; len < pschregno.length; len++) {
            svf.VrSetForm("KNJWG030.frm", 4);
            svf.VrsOut("TITLE", "成績単位修得証明書");
            pobj.head_out(param[0], param[6], paramap, certifkind, param[7]);            //学校名、校長名のセット  05/11/18Modify
            pobj.studentInfoPrint(pschregno[len], param[0], param[1], certifkind);    //氏名、住所等出力
            pobj.study_out(pschregno[len], param[0], certifkind, paramap);                    //学習の記録出力-->VrEndRecord()はここで！
        }
        if (pobj.nonedata) {
            nonedata = true;
        }
        pobj.pre_stat_f();                                          //PrepareStatement CLOSE
    }

    /*
     *   証明書番号セット
     */
    private String getCertificateNum(
            final DB2UDB db2,
            final String pnendo
    ) {
        String str = null;
        str = nao_package.KenjaProperties.gengou( Integer.parseInt(pnendo) ).substring(2, 4);
        if( str == null ) str = "";
        return str;
    }

}
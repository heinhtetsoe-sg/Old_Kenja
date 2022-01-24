// kanji=漢字
/*
 * $Id: c43ccc2aa58bf82e1c15c8aa80ca54a708f035e5 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWE;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書
 *
 *  2004/04/27 yamashiro・学習成績概評の学年人数の出力条件を追加 -> 指示画面より受取
 *  2004/08/17 yamashiro・進学用に関して保健データを表示しない
 *  2004/09/13 yamashiro・所見出力用に印刷指示画面よりＯＳ選択パラメータを追加
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *  2005/12/08 yamashiro・学籍処理日を引数として追加（「卒業日が学籍処理日の後なら卒業見込とする」仕様に伴い）
 *                        但し、現時点では指示画面が未対応のため、処理日を使用する
 */

public class KNJWE070
{
    private static final Log log = LogFactory.getLog(KNJWE070.class);  //05/11/18
    Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                         //Databaseクラスを継承したクラス
    boolean nonedata;                   //該当データなしフラグ
    final private KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定 06/03/23

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        dumpParam(request);

        final String[] param = createParam(request);

        //ＯＳ区分 04/09/13 1:XP 2:WINDOWS2000
        final Map paramap = createParamap(request, param); //HttpServletRequestからの引数 05/11/18

        // ＤＢ接続
        final String dbname = request.getParameter("DBNAME");
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!" + ex);
        }

        // ＳＶＦ設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();
        settingSvf(outstrm);

        // ＳＶＦ作成
        final String pschregno[] = request.getParameterValues("category_selected");   // 学籍番号
        final String output = request.getParameter("OUTPUT");
        if(Integer.parseInt(output) == 1){
            paramap.put("CERTIFKIND", "008");  // 証明書種別
            ent_paper( param, pschregno, paramap );     //進学用のPAPER作成 05/11/18Modify
        } else{
            paramap.put("CERTIFKIND", "009");  // 証明書種別
            emp_paper( param, pschregno, paramap );     //就職用のPAPER作成 05/11/18Modify
        }

        // 終了処理
        if( nonedata == true ) {
            svf.VrQuit();
        }
        db2.commit();
        db2.close();
        outstrm.close();
    }

    private void settingSvf(OutputStream outstrm) {
        int ret;
        ret = svf.VrInit();                         //クラスの初期化
        if (ret != 0) {
            log.fatal("SVF初期化失敗!");
        }
        ret = svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定
        if (ret != 0) {
            log.fatal("SVF VrSetSpoolFileStream失敗!");
        }
    }

    private Map createParamap(final HttpServletRequest request, final String[] param) {
        final Map paramap = new HashMap();        

        if( request.getParameter("OS") != null ) {
            paramap.put( "OS", request.getParameter("OS") );  //ＯＳ区分 05/11/18 HashMapへ変更
        } else {
            paramap.put( "OS", new String("1") );             //ＯＳ区分 05/11/18 HashMapへ変更
        }
        paramap.put( "CTRL_YEAR", request.getParameter("CTRL_YEAR") );  //今年度  05/11/18

        if( request.getParameter("DATE") != null ) {
            paramap.put( "CTRL_DATE", request.getParameter("DATE") );  //学籍処理日 05/12/08
        }
        if( request.getParameter("MIRISYU") != null ) {
            paramap.put( "MIRISYU", request.getParameter("MIRISYU") );  // 未履修科目を出力する:1 しない:2
        }
        if( request.getParameter("RISYU") != null ) {
            paramap.put( "RISYU", request.getParameter("RISYU") );  // 未履修科目を出力する:1 しない:2
        }
        if (!paramap.containsKey("HYOTEI")) {
            if (request.getParameter("HYOTEI") != null && request.getParameter("HYOTEI").equals("on")) {
                paramap.put("HYOTEI", "on");  // 評定の読み替え offの場合はparamapに追加しない。
            }
        }
        if (!paramap.containsKey("FORM6")) {
            if (request.getParameter("FORM6") != null && request.getParameter("FORM6").equals("on")) {
                paramap.put("FORM6", "on");  // ６年生用フォーム offの場合はparamapに追加しない。
            }
        }

        if ("TOK".equals(_definecode.schoolmark)) {
            paramap.put("NUMBER", "");
        }

        return paramap;
    }

    private String[] createParam(HttpServletRequest request) {
        final String param[] = new String[10];

        param[0] = request.getParameter("YEAR");                            //年度
        param[1] = request.getParameter("GAKKI");                           //学期

        // '学年＋組'パラメータを分解
        final String strx = request.getParameter("GRADE_HR_CLASS");               //学年＋組
        KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();           //クラスのインスタンス作成
        KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(strx);
        param[2] = returnval.val1;                                          //学年

        param[3] = request.getParameter("SEKI");                            //記載責任者

        param[4] = request.getParameter("HYOTEI");                          //評定の読み替え
        if(request.getParameter("HYOTEI") == null) {
            param[4] = "off";
        }
        param[5] = request.getParameter("KANJI");                           //漢字出力

        //記載日がブランクの場合桁数０で渡される事に対応 05/11/22 Modify
        if( request.getParameter("DATE") != null  &&  3 < request.getParameter("DATE").length() ) {
            param[6] = request.getParameter("DATE");                            //処理日付
        }

        param[8] = request.getParameter("COMMENT");                         //学習成績概評 04/04/27Add
        param[9] = request.getParameter("OS");                              //ＯＳ区分 04/09/13Add 1:XP 2:WINDOWS2000

        return param;
    }

    private void dumpParam(HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /**進学用のPAPER作成**/
    public void ent_paper( String param[], String pschregno[], Map paramap ) throws SQLException  //05/11/18
    {
        KNJWE070_1 pobj = new KNJWE070_1(db2, svf, _definecode);
        pobj.pre_stat(param[4]);

        for(int len=0;len < pschregno.length;len++){
            param[7] = pschregno[len];                                  //対象学籍番号
            pobj.svf_int(param[7],param[0],paramap);                                     //ＳＶＦ−ＦＯＲＭフィールド初期化
            pobj.head_out(param[0],param[6],param[3],paramap);         //学校名、校長名のセット 05/11/18Modify
            pobj.address_out(param[7],param[0],param[1],param[5],(String) paramap.get("NUMBER"),paramap); //氏名、住所等出力 05/12/08Modify
            pobj.geneviewmbr_out(param[7],param[0],param[1],param[8]);  //成績段階別人数の出力 04/04/27変更
            pobj.attend_out(param[7],param[0]);                         //出欠の出力
            pobj.exam_out(param[7],param[0],paramap);                  //所見の出力 04/09/13Modify
            pobj.study_out(param[7],param[0],paramap);                 //学習の記録出力-->VrEndRecord()はここで！
        }

        if( pobj.nonedata == true ) {
            nonedata = true;
        }
        pobj.pre_stat_f();
    }

    /**就職用のPAPER作成**/
    public void emp_paper( String param[], String pschregno[], Map paramap ) throws SQLException   //05/11/18
    {
        KNJWE070_2 pobj = new KNJWE070_2(db2, svf, _definecode);
        pobj.pre_stat(param[4]);

        for(int len=0;len < pschregno.length;len++){
            param[7] = pschregno[len];                                  //対象学籍番号
            pobj.svf_int(param[7],param[0],paramap);                                     //ＳＶＦ−ＦＯＲＭフィールド初期化
            pobj.head_out(param[0],param[6],param[3],paramap);         //学校名、校長名のセット  05/11/18Modify
            pobj.address_out(param[7],param[0],param[1],param[5],(String) paramap.get("NUMBER"),paramap); //氏名、住所等出力 05/12/08Modify
            pobj.attend_out(param[7],param[0]);                         //出欠の出力
            pobj.medexam_out(param[7],param[0]);                        //身体状況の出力
            pobj.exam_out(param[7],param[0],paramap);                  //所見の出力 04/09/13Modify
            pobj.study_out(param[7],param[0],paramap);                 //学習の記録出力-->VrEndRecord()はここで！
        }

        if( pobj.nonedata == true ) {
            nonedata = true;
        }
        pobj.pre_stat_f();
    }

    /**
     * 証明書番号の年度を取得します。
     */
    private String getCertificateNum(
            final DB2UDB db2,
            final String pnendo
    ) {
        final int intNendo = Integer.parseInt(pnendo);
        final String gengou = nao_package.KenjaProperties.gengou(intNendo);

        final String str = gengou.substring(2, 4);
        if( str == null ) {
            return "";
        }
        return str;
    }
}

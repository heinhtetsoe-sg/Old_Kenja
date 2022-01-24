// kanji=漢字
/*
 * $Id: d75e68227d5948ee7e67650fd4343490328fca9c $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE070_1;
import servletpack.KNJE.KNJE070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [卒業生管理] 卒業生用高校調査書
 *
 *  2004/08/17 yamashiro・進学用に関して保健データを表示しない
 *  2004/11/05 yamashiro・所見出力用に印刷指示画面よりＯＳ選択パラメータを追加
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *  2005/12/08 yamashiro・学籍処理日を引数として追加（「卒業日が学籍処理日の後なら卒業見込とする」仕様に伴い）
 *                        但し、現時点では指示画面が未対応のため、処理日を使用する
 */

public class KNJI060 {

	private static final Log log = LogFactory.getLog(KNJI060.class);

    private StringTokenizer stkx, stky, stkz, stkt;    //学籍番号・年度・学期・学年

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 58708 $ $Date: 2018-02-21 21:26:45 +0900 (水, 21 2 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

            /*  << request.Parameter >>
                        OUTPUT      1               調査書種類 1進学用: 2:就職用
                    5   KANJI       1               漢字氏名出力 1:する 2:しない
                    6   DATE        2003/06/05      記載日付
                    3   SEKI        999999          記載責任者
                    7   SCHREGNO    181010,181241   学籍番号
                    0   G_YEAR      2002,2002       卒業時年度
                    1   G_SEMESTER  3,3             卒業時学期
                    2   G_GRADE     3,3             卒業時学年
            */

        //  パラメーターを受取
        final String[] param = new String[10];
        final Map paramap = new HashMap();        //HttpServletRequestからの引数
        try {
            param[0] = request.getParameter("YEAR");                            //年度
            param[1] = request.getParameter("GAKKI");                           //学期
            param[3] = request.getParameter("SEKI");                            //記載責任者
            param[5] = request.getParameter("KANJI");                           //漢字出力

            //記載日がブランクの場合桁数０で渡される事に対応
            if (request.getParameter("DATE") != null && 3 < request.getParameter("DATE").length()) {
                param[6] = request.getParameter("DATE");                            //処理日付
            }

            stkx = new StringTokenizer(request.getParameter("SCHREGNO"), ",");      //学籍番号
            stky = new StringTokenizer(request.getParameter("G_YEAR"), ",");        //卒業年度
            stkz = new StringTokenizer(request.getParameter("G_SEMESTER"), ",");    //卒業学期
            stkt = new StringTokenizer(request.getParameter("G_GRADE"), ",");       //卒業学年
            param[8] = request.getParameter("COMMENT");                         //学習成績概評
            param[9] = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000

            putParam(paramap, request, "CTRL_YEAR");  //今年度
            putParam(paramap, request, "MIRISYU");  // 未履修科目を出力する:1 しない:2
            putParam(paramap, request, "FORM6");  // ６年生用フォーム
            putParam(paramap, request, "RISYU");  // 履修のみ科目出力　1:する／2:しない
            putParam(paramap, request, "useCurriculumcd");
            putParam(paramap, request, "useClassDetailDat");
            putParam(paramap, request, "useAddrField2");
            putParam(paramap, request, "useProvFlg");
            putParam(paramap, request, "useGakkaSchoolDiv");
            putParamDef(paramap, request, "OS", "1");  //ＯＳ区分 1:XP 2:WINDOWS2000
            putParamDef(paramap, request, "TANIPRINT_SOUGOU", "1");  // "総合的な学習の時間"修得単位数の計 "0"表示
            putParamDef(paramap, request, "TANIPRINT_RYUGAKU", "1");  // 留学修得単位数の計 "0"表示
            putParamDef(paramap, request, "HYOTEI", "off");  // 評定の読み替え
            putParamBl(paramap, request, "useSyojikou3");
            putParamBl(paramap, request, "certifNoSyudou");
            putParamBl(paramap, request, "gaihyouGakkaBetu");
            putParamBl(paramap, request, "train_ref_1_2_3_field_size");
            putParamBl(paramap, request, "train_ref_1_2_3_gyo_size");
            putParamBl(paramap, request, "3_or_6_nenYoForm");
            putParamBl(paramap, request, "tyousasyoSougouHyoukaNentani");
            putParamBl(paramap, request, "NENYOFORM");
            putParamBl(paramap, request, "tyousasyoTokuBetuFieldSize");
            putParamBl(paramap, request, "tyousasyoEMPTokuBetuFieldSize");
            putParamBl(paramap, request, "tyousasyoKinsokuForm");
            putParamBl(paramap, request, "tyousasyoNotPrintAnotherAttendrec");
            putParamBl(paramap, request, "tyousasyoNotPrintAnotherStudyrec");
            putParamReplace(paramap, request, "tyousasyoAttendrecRemarkFieldSize");
            putParamReplace(paramap, request, "tyousasyoTotalstudyactFieldSize");
            putParamReplace(paramap, request, "tyousasyoTotalstudyvalFieldSize");
            putParamReplace(paramap, request, "tyousasyoSpecialactrecFieldSize");
            putParamWithName(paramap, "CTRL_DATE", request, "DATE");  //学籍処理日
            
            final List containedParam = Arrays.asList(new String[] {"CTRL_DATE", "SCHREGNO", "G_YEAR", "G_SEMESTER", "G_GRADE"});
            for (final Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
                final String name = (String) en.nextElement();
                if (paramap.containsKey(name) || containedParam.contains(name)) {
                    continue;
                }
                paramap.put(name, request.getParameter(name));
            }
            paramap.put("PRINT_GRD", "1");

        } catch (Exception e) {
            log.error("[KNJI060]parameter error!", e);
        }

        //  print設定
        response.setContentType("application/pdf");
        final OutputStream outstrm = response.getOutputStream();
        
        final Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        //  ＤＢ接続
        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJI060]DB2 open error!", ex);
        }

        //  ＳＶＦ設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        //  ＳＶＦ作成
        boolean nonedata = false;                   //該当データなしフラグ
        final String output = request.getParameter("OUTPUT");                            //出力種別(1:進学 2:就職)
        final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定
        if ("1h".equals(output) || "2h".equals(output)) {
        	paramap.put("HANKI_NINTEI", "1"); // 半期認定の評定・単位数のフォーム
        }
        if ("1".equals(output) || "1h".equals(output)) {
            paramap.put("CERTIFKIND", "025");  // 証明書種別
            paramap.put("CERTIFKIND2", "025");  // 証明書学校データ備考
            nonedata = ent_paper(db2, svf, param, paramap, _definecode);               //進学用のPAPER作成
        } else if ("2".equals(output) || "2h".equals(output)) {
            paramap.put("CERTIFKIND", "026");  // 証明書種別
            paramap.put("CERTIFKIND2", "026");  // 証明書学校データ備考
            nonedata = emp_paper(db2, svf, param, paramap, _definecode);               //就職用のPAPER作成
        }

        //  終了処理
        if (nonedata) {
            svf.VrQuit();
        }
        db2.commit();
        db2.close();

    } //public void doGetの括り

    // パラメータで' 'が'+'に変換されるのでもとに戻す
    private void putParamReplace(final Map paramap, final HttpServletRequest req, final String name) {
        paramap.put(name, StringUtils.replace(StringUtils.defaultString(req.getParameter(name), ""), "+", " "));
    }
    
    // パラメータがnullの場合デフォルト値に置換
    private void putParamDef(final Map paramap, final HttpServletRequest req, final String name, final String defVal) {
        paramap.put(name, StringUtils.defaultString(req.getParameter(name), defVal));
    }
    
    // パラメータがnullの場合""に置換
    private void putParamBl(final Map paramap, final HttpServletRequest req, final String name) {
        putParamDef(paramap, req, name, "");
    }
    
    private void putParam(final Map paramap, final HttpServletRequest req, final String name) {
        paramap.put(name, req.getParameter(name));
    }
    
    private void putParamWithName(final Map paramap, final String paramname, final HttpServletRequest req, final String name) {
        paramap.put(paramname, req.getParameter(name));
    }

    /**進学用のPAPER作成**/
    private boolean ent_paper(final DB2UDB db2, final Vrw32alpWrap svf, final String[] param, final Map paramap, final KNJDefineSchool definecode) {
        final KNJE070_1 pobj = new KNJE070_1(db2, svf, definecode, (String) paramap.get("useSyojikou3"));
        pobj.pre_stat((String) paramap.get("HYOTEI"), paramap);
        while (stkx.hasMoreTokens() && stky.hasMoreTokens() && stkz.hasMoreTokens() && stkt.hasMoreTokens()) {
            param[2] = stkt.nextToken();                        //卒業学年

            final String year = stky.nextToken();                        //卒業年度
            final String semes = stkz.nextToken();                        //卒業学期
            final String staffcd = param[3];
            final String kanji = param[5];
            final String date = param[6];
            final String schregno = stkx.nextToken();                        //学籍番号
            final String comment = param[8];
            final String oadiv = param[9];
            pobj.printSvf(schregno, year, semes, date, staffcd, kanji, comment, oadiv, null, paramap);
        }
        pobj.pre_stat_f();
        return pobj.nonedata;
    }//ent_paterの括り



    /**就職用のPAPER作成**/
    private boolean emp_paper(final DB2UDB db2, final Vrw32alpWrap svf, final String[] param, final Map paramap, final KNJDefineSchool definecode) {
        final KNJE070_2 pobj = new KNJE070_2(db2, svf, definecode, (String) paramap.get("useSyojikou3"));
        pobj.pre_stat((String) paramap.get("HYOTEI"), paramap);

        while (stkx.hasMoreTokens() && stky.hasMoreTokens() && stkz.hasMoreTokens() && stkt.hasMoreTokens()) {
            param[2] = stkt.nextToken();                        //卒業学年

            final String year = stky.nextToken();                        //卒業年度
            final String semes = stkz.nextToken();                        //卒業学期
            final String staffcd = param[3];
            final String kanji = param[5];
            final String date = param[6];
            final String schregno = stkx.nextToken();                        //学籍番号
            final String comment = null;
            final String oadiv = param[9];
            pobj.printSvf(schregno, year, semes, date, staffcd, kanji, comment, oadiv, null, paramap);
        }
        pobj.pre_stat_f();
        return pobj.nonedata;
    }//emp_paperの括り

}//クラスの括り

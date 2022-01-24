// kanji=漢字
/*
 * $Id: 377c94bc3eb4774247a9bd963f2ce9e23ce42c27 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.StringTokenizer;

import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  ＳＶＦ−ＦＯＲＭへの出力編集
 *
 *  2006/05/02 yamashiro・入学前学歴の学校名出力メソッドprintSvfFinSchool( Vrw32alp, String, String )作成
 */

public class KNJ_EditSvf{

    private static final Log log = LogFactory.getLog(KNJ_EditSvf.class);

    int ret;                        //応答値
    Vrw32alp svf;                   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private KNJEditString editString;  //NO001


    public KNJ_EditSvf(Vrw32alp svf){
        this.svf = svf;
    }



    /*--------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力 
     *      改行マークで行替えし各フィールドへ出力
     *      但しフィールド名は name1,name2,name3 のように名前は共通、順序番号を付ける
     * パラメーター
     *      strx:編集元文字列 stry:出力先フィールド名 ib:出力先フィールド名のMAX順序番号
     *--------------------------------------------------------------------------*/
    public void set_toknizer(String strx,String stry,int ib){
        int ic;
        StringTokenizer stkx;

        if(strx != null){
            stkx = new StringTokenizer(strx, "\r\n", false);
            ic = 0;
            while ( stkx.hasMoreTokens()  &  ic<ib) {
                ret = svf.VrsOut(stry+(ic+1)    , stkx.nextToken());
                ic++;
            }
        }
    }//set_toknizerの括り



    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力 
     *      改行マークで行替えし各フィールドへ出力
     *      但しフィールド名は name1,name2,name3 のように名前は共通、順序番号を付ける
     *          name1XXXX   RRRRRRRRR(繰返し)
     *          name2XXXX   RRRRRRRRR(繰返し)
     *          name3XXXX   RRRRRRRRR(繰返し)
     * パラメーター
     *      strx:編集元文字列 stry:出力先フィールド名 ia:繰返し番号 ib:出力先フィールド名のMAX順序番号
     *------------------------------------------------------------------------------------*/
    public void set_toknizer(String strx,String stry,int ia,int ib){
        int ic;
        StringTokenizer stkx;

        if(strx != null){
            stkx = new StringTokenizer(strx, "\r\n", false);
            ic = 0;
            while ( stkx.hasMoreTokens()  &  ic<ib) {
                ret = svf.VrsOutn(stry+(ic+1)   ,ia , stkx.nextToken());
                ic++;
            }
        }
    }//set_toknizerの括り



    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力 
     *      改行マークで行替えし各フィールドへ出力
     *      但しフィールド名は name1,name2,name3 のように名前は共通、順序番号を付ける
     *          name1XXXX
     *          RRRRRRRRR(繰返し)
     *          RRRRRRRRR(繰返し)
     * パラメーター
     *      strx:編集元文字列 stry:出力先フィールド名 ib:繰返しのMAX順序番号
     *------------------------------------------------------------------------------------*/
    public void set_toknizer2(String strx,String stry,int ib){
        int ic;
        StringTokenizer stkx;

        if(strx != null){
            stkx = new StringTokenizer(strx, "\r\n", false);
            ic = 0;
            while ( stkx.hasMoreTokens()  &  ic<ib) {
                ret = svf.VrsOutn(stry,ic+1,stkx.nextToken());
                ic++;
            }
        }
    }//set_toknizerの括り

    /**
     *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
     *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
     *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
     *  全角スペースより前半の文字を○○○○○立と見なします。
     *  @param str1 例えば"千代田区　アルプ"
     *  @param str2 例えば"小学校卒業"
     */
    public void printSvfFinSchool(
            final String str1,
            final String str2
    ) {
        if (editString == null) {
            editString = new KNJEditString();
        }

        final String schoolName;
        if (null == str1) {
            schoolName = "";
        } else {
            final int i = str1.indexOf('　');  // 全角スペース
            if (-1 < i && 5 >= i) {
                final String ritu = str1.substring(0, i);
                if (null != ritu) {
                    svf.VrsOut("INSTALLATION_DIV",  ritu + "立");
                }
                schoolName = str1.substring(i + 1);
            } else {
                schoolName = str1;
            }
        }
        final int schoolNameLen = editString.retStringByteValue(schoolName, 50);

        final String kotei = (null == str2) ? "" : str2;
        final int koteiLen = editString.retStringByteValue(kotei, 20);

        if (schoolNameLen == 0) {
            svf.VrsOut("FINSCHOOL1", kotei);
        } else if (schoolNameLen + koteiLen <= 40) {
            svf.VrsOut("FINSCHOOL1", schoolName + kotei);
        } else if(schoolNameLen + koteiLen <= 50) {
            svf.VrsOut("FINSCHOOL2", schoolName + kotei);
        } else {
            svf.VrsOut("FINSCHOOL2", schoolName);
            svf.VrsOut("FINSCHOOL3", kotei);
        }
    }
}

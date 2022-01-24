// kanji=漢字
/*
 * $Id: a55df5300dedd2413a9d3fe5e1cc0e108e54f38c $
 *
 * 作成日: 2005/06/24
 * 作成者: nakamoto
 * 
 *  2008/10/08 KNJF030, v 1.5 を複製して作成
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWF;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３０＞  保健各種帳票印刷（クラス／個人）
 *
 *  2006/06/02 nakamoto 作成日---KNJF030,KNJF040を統合。
 *  2006/07/28 nakamoto NO001:コード２未満とNULLは空白表示とする。--定期健康診断結果のお知らせ(KNJF030_7)
 *                            コード２未満とNULLは出力しない。------内科健診所見(KNJF030_8)
 */

public class KNJWF030 {

    private static final Log log = LogFactory.getLog(KNJWF030.class);
    KNJ_EditDate editdate = new KNJ_EditDate();     //和暦変換取得クラスのインスタンス作成

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[25];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("GAKKI");                       //学期 1,2,3
            param[3] = request.getParameter("KUBUN");                       //1:クラス,2:個人
            param[4] = request.getParameter("SCHOOL_JUDGE");                //H:高校、J:中学
            //学年・組or学籍番号
            String classcd[] = request.getParameterValues("CLASS_SELECTED");
            param[2] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[2] = param[2] + ",";
                if (param[3].equals("1")) param[2] = param[2] + "'" + classcd[ia] + "'";
                if (param[3].equals("2")) param[2] = param[2] + "'" + classcd[ia] + "'";
//                if (param[3].equals("2")) param[2] = param[2] + "'" + (classcd[ia]).substring(0,(classcd[ia]).indexOf("-")) + "'";
            }
            param[2] = param[2] + ")";

            param[11] = request.getParameter("CHECK1");     // １）生徒学生健康診断票（一般）
            param[5] = request.getParameter("OUTPUTA");             // 1:結果 2:フォーム
            param[12] = request.getParameter("CHECK2");     // ２）生徒学生健康診断票（歯・口腔）
            param[6] = request.getParameter("OUTPUTB");             // 1:結果 2:フォーム
            param[13] = request.getParameter("CHECK3");     // ３）健康診断の未受検項目のある生徒へ
            param[14] = request.getParameter("CHECK4");     // ４）眼科検診のお知らせ 
            param[7] = request.getParameter("DATE");                // 学校への提出日
            param[15] = request.getParameter("CHECK5");     // ５）検診結果のお知らせ（歯・口腔）
            param[16] = request.getParameter("CHECK7");     // ６）定期健康診断結果
            param[17] = request.getParameter("CHECK6");     // ７）検診結果のお知らせ（一般）
            param[8] = request.getParameter("OUTPUT");              // 1:１人で１枚にまとめて出力
                                                                    // 2:１人で各種類ごとに出力
            param[18] = request.getParameter("CHECK8");     // ８）内科検診所見あり生徒の名簿
            param[19] = request.getParameter("CHECK9");     // ９）定期健康診断異常者一覧表
            param[9] = request.getParameter("SELECT1");             // 一般条件
            param[10] = request.getParameter("SELECT2");            // 歯・口腔条件
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        setHead(db2,svf,param);         //見出し項目

        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        //-----結果印刷-----//

        //１）生徒学生健康診断票（一般）
        if( param[11] != null ) 
            if( printMain12(db2,svf,param,1,Integer.parseInt(param[5])) ) nonedata = true;

        //２）生徒学生健康診断票（歯・口腔）
        if( param[12] != null ) 
            if( printMain12(db2,svf,param,2,Integer.parseInt(param[6])) ) nonedata = true;

        //３）健康診断の未受検項目のある生徒へ
        if( param[13] != null ) 
            if( printMain3(db2,svf,param) ) nonedata = true;

        //４）眼科検診のお知らせ 
        if( param[14] != null ) 
            if( printMain4(db2,svf,param) ) nonedata = true;

        //５）検診結果のお知らせ（歯・口腔）
        if( param[15] != null ) 
            if( printMain5(db2,svf,param) ) nonedata = true;

        //６）定期健康診断結果
        if( param[16] != null ) 
            if( printMain6(db2,svf,param) ) nonedata = true;

        //７）検診結果のお知らせ（一般）
        if( param[17] != null ) 
            if( printMain7(db2,svf,param) ) nonedata = true;

        //８）内科検診所見あり生徒の名簿
        if( param[18] != null ) 
            if( printMain8(db2,svf,param) ) nonedata = true;

        //９）定期健康診断異常者一覧表
        if( param[19] != null && !param[9].equals("17") && !param[9].equals("18") ) 
            if( printMain9(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    /** SVF-FORMセット＆見出し項目 **/
    private void setHead(DB2UDB db2,Vrw32alp svf,String param[]){


        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

    //  作成日(現在処理日)
        try {
            returnval = getinfo.Control(db2);
            param[20] = KNJ_EditDate.h_format_JP(returnval.val3);
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get TODAY error!", ex );
        }

    //  学校名・学校住所・校長名の取得
        try {
            KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(param[0]);   //取得クラスのインスタンス作成
            KNJ_Schoolinfo.ReturnVal returnval2 = schoolinfo.get_info(db2);
            param[21] = returnval2.SCHOOL_NAME1;                                            //学校名１
            param[22] = returnval2.SCHOOL_NAME2;                                            //学校名２
            param[23] = returnval2.PRINCIPAL_NAME;                                      //校長名
            param[24] = returnval2.PRINCIPAL_JOBNAME;

        } catch( Exception e ){
            log.error("ReturnVal setHead() get schoolinfo error!", e );
        }

        getinfo = null;
        returnval = null;

    }//setHead()の括り


    /**１or２）生徒学生健康診断票（一般or歯・口腔）*/
    private boolean printMain12(DB2UDB db2,Vrw32alp svf,String param[],int check_no,int output_flg)
    {
        boolean nonedata = false;
        try {
            if( check_no == 1 ) svf.VrSetForm("KNJF030_1.frm", 4);//一般
            if( check_no == 2 ) svf.VrSetForm("KNJF030_2.frm", 4);//一般
            String sql = statementSchno(param);
            log.debug("printmain12 sql="+sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if( output_flg == 1 ) { //結果
                    if( printMain12Result(db2,svf,param,check_no,rs.getString("SCHREGNO"),"J") ) nonedata = true;
                    if( printMain12Result(db2,svf,param,check_no,rs.getString("SCHREGNO"),"H") ) nonedata = true;
                }
                if( output_flg == 2 ) //フォーム
                    if( printMain12Form(db2,svf,param,rs,check_no) ) nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain12 read error!",ex);
        }
        return nonedata;
    }


    /**１or２）生徒学生健康診断票（一般or歯・口腔）結果*/
    private boolean printMain12Result(DB2UDB db2,Vrw32alp svf,String param[],int check_no,String schno,String gradeFlg)
    {
        boolean nonedata = false;
        try {
            final String sql = statementResult(param,check_no,gradeFlg);
            log.debug("printMain12Result sql="+sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, schno);
            ResultSet rs = ps.executeQuery();
            int dataCnt = 0;
            while( rs.next() ){
                if( printMain12ResultSvf(db2,svf,param,rs,check_no) ) nonedata = true;
                dataCnt++;
            }
            while (0 < dataCnt && dataCnt < 4) {
                svf.VrEndRecord();
                dataCnt++;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain12Result read error!",ex);
        }
        return nonedata;
    }


    /**１or２）生徒学生健康診断票（一般or歯・口腔）結果*/
    private boolean printMain12ResultSvf(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int check_no)
    {
        boolean nonedata = false;
        try {
                svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("SEX"        ,  rs.getString("SEX"));
                svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("AGE"        ,  rs.getString("AGE"));        //４月１日現在の年齢
            if( check_no == 1 ) {//一般
                svf.VrsOut("SCHOOL_NAME"  , param[22]);
                svf.VrsOut("YEAR"       ,  nao_package.KenjaProperties.gengou(Integer.parseInt(rs.getString("YEAR"))) + "年度");
                svf.VrsOut("HEIGHT"         ,  rs.getString("HEIGHT"));
                svf.VrsOut("WEIGHT"         ,  rs.getString("WEIGHT"));
                svf.VrsOut("SIT_HEIGHT"     ,  rs.getString("SITHEIGHT"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION")) || 
                        isNumber(rs.getString("R_VISION")) || isNumber(rs.getString("L_VISION"))) {
                    svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION"));
                    svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION"));
                    svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION"));
                    svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION"));
                } else {
                    svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION_MARK"));
                    svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION_MARK"));
                    svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION_MARK"));
                    svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION_MARK"));                    
                }
                svf.VrsOut("R_EAR"          ,  rs.getString("R_EAR"));
                svf.VrsOut("R_EAR_DB"       ,  rs.getString("R_EAR_DB"));
                svf.VrsOut("L_EAR"          ,  rs.getString("L_EAR"));
                svf.VrsOut("L_EAR_DB"       ,  rs.getString("L_EAR_DB"));
                svf.VrsOut("ALBUMINURIA"    ,  rs.getString("ALBUMINURIA1CD"));
                svf.VrsOut("URICSUGAR"      ,  rs.getString("URICSUGAR1CD"));
                svf.VrsOut("URICBLEED"      ,  rs.getString("URICBLEED1CD"));
                svf.VrsOut("URINE_OTHERS1"  ,  rs.getString("URICOTHERTEST"));
                svf.VrsOut("NUTRITION"      ,  rs.getString("NUTRITIONCD"));
                svf.VrsOut("SPINERIB"       ,  rs.getString("SPINERIBCD"));
                svf.VrsOut("EYEDISEASE"     ,  rs.getString("EYEDISEASECD"));
                svf.VrsOut("NOSEDISEASE"    ,  rs.getString("NOSEDISEASECD"));
                svf.VrsOut("SKINDISEASE"    ,  rs.getString("SKINDISEASECD"));
                svf.VrsOut("HEART_MEDEXAM"  ,  rs.getString("HEART_MEDEXAM"));
                svf.VrsOut("HEARTDISEASE1"  ,  rs.getString("HEARTDISEASECD"));
                svf.VrsOut("PHOTO_DATE"     ,  KNJ_EditDate.h_format_JP_MD(rs.getString("TB_FILMDATE")));
                svf.VrsOut("FILMNO"         ,  rs.getString("TB_FILMNO"));
                svf.VrsOut("VIEWS1_1"       ,  rs.getString("TB_REMARKCD"));
                svf.VrsOut("OTHERS"         ,  rs.getString("TB_OTHERTESTCD"));
                svf.VrsOut("DISEASE_NAME"   ,  rs.getString("TB_NAMECD"));
                svf.VrsOut("GUIDANCE"       ,  rs.getString("TB_ADVISECD"));
                svf.VrsOut("ANEMIA"         ,  rs.getString("ANEMIA_REMARK"));
                svf.VrsOut("HEMOGLOBIN"     ,  rs.getString("HEMOGLOBIN"));
                svf.VrsOut("OTHERDISEASE"   ,  rs.getString("OTHERDISEASECD"));
                svf.VrsOut("VIEWS2_1"       ,  rs.getString("DOC_REMARK"));
                svf.VrsOut("DOC_DATE"       ,  KNJ_EditDate.h_format_JP_MD(rs.getString("DOC_DATE")));
                svf.VrsOut("DOC_TREAT1"     ,  rs.getString("TREATCD"));
                svf.VrsOut("NOTE1"          ,  rs.getString("REMARK"));
            } else {//歯・口腔
                svf.VrsOut("NENDO1" ,  nao_package.KenjaProperties.gengou(Integer.parseInt(rs.getString("YEAR"))).substring(0,2));
                svf.VrsOut("NENDO2" ,  nao_package.KenjaProperties.gengou(Integer.parseInt(rs.getString("YEAR"))).substring(2));
                svf.VrsOut("JAWS_JOINTCD0"  ,  rs.getString("JAWS_JOINTCD1"));
                svf.VrsOut("JAWS_JOINTCD1"  ,  rs.getString("JAWS_JOINTCD2"));
                svf.VrsOut("JAWS_JOINTCD2"  ,  rs.getString("JAWS_JOINTCD3"));
                svf.VrsOut("PLAQUECD0"  ,  rs.getString("PLAQUECD1"));
                svf.VrsOut("PLAQUECD1"  ,  rs.getString("PLAQUECD2"));
                svf.VrsOut("PLAQUECD2"  ,  rs.getString("PLAQUECD3"));
                svf.VrsOut("GUMCD0"  ,  rs.getString("GUMCD1"));
                svf.VrsOut("GUMCD1"  ,  rs.getString("GUMCD2"));
                svf.VrsOut("GUMCD2"  ,  rs.getString("GUMCD3"));
                svf.VrsOut("BABYTOOTH"          ,  rs.getString("BABYTOOTH"));
                svf.VrsOut("REMAINBABYTOOTH"    ,  rs.getString("REMAINBABYTOOTH"));
                svf.VrsOut("TREATEDBABYTOOTH"   ,  rs.getString("TREATEDBABYTOOTH"));
                svf.VrsOut("BRACKBABYTOOTH"     ,  rs.getString("BRACK_BABYTOOTH"));//Add
                svf.VrsOut("ADULTTOOTH"         ,  rs.getString("ADULTTOOTH"));
                svf.VrsOut("REMAINADULTTOOTH"   ,  rs.getString("REMAINADULTTOOTH"));
                svf.VrsOut("TREATEDADULTTOOTH"  ,  rs.getString("TREATEDADULTTOOTH"));
                svf.VrsOut("LOSTADULTTOOTH"     ,  rs.getString("LOSTADULTTOOTH"));
                svf.VrsOut("BRACKADULTTOOTH"    ,  rs.getString("BRACK_ADULTTOOTH"));//Add
                svf.VrsOut("TOOTHOTHERDISEASE"  ,  rs.getString("OTHERDISEASECD"));
                svf.VrsOut("DENTISTREMARK"      ,  rs.getString("DENTISTREMARKCD"));
                svf.VrsOut("month"  ,  rs.getString("DENTISTREMARKMONTH"));
                svf.VrsOut("day"    ,  rs.getString("DENTISTREMARKDAY"));
                svf.VrsOut("DENTISTTREAT"       ,  rs.getString("DENTISTTREAT"));
            }
                svf.VrEndRecord();
                nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain12ResultSvf read error!",ex);
        }
        return nonedata;
    }


    /** 文字列strが数値でない、または数値が0のときにfalse、それ以外(strが0以外の数値)でtrue を返す */
    public static boolean isNumber(String str){
        if (null == str) {
            return false;
        }
        try{
            double d = Double.valueOf(str).doubleValue();
            if (d == 0.0) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }


    /**１or２）生徒学生健康診断票（一般or歯・口腔）フォーム*/
    private boolean printMain12Form(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int check_no)
    {
        boolean nonedata = false;
        try {
                if (check_no == 1) {//一般
                    svf.VrsOut("SCHOOL_NAME",  param[22]);
                    svf.VrsOut("YEAR", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                } else {//歯・口腔
                    svf.VrsOut("NENDO1", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])).substring(0,2));
                    svf.VrsOut("NENDO2", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])).substring(2));
                }
                svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("SEX"        ,  rs.getString("SEX"));
                svf.VrsOut("BIRTHDAY"     ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("AGE"        ,  rs.getString("AGE"));

                svf.VrEndRecord();
                nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain12Form read error!",ex);
        }
        return nonedata;
    }


    /**３）健康診断の未受検項目のある生徒へ*/
    private boolean printMain3(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            svf.VrSetForm("KNJF030_3.frm", 4);
            PreparedStatement ps = db2.prepareStatement(statementMeisai(param,3));
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (rs.getString("CHECK1")==null && rs.getString("CHECK2")==null && rs.getString("CHECK3")==null && rs.getString("CHECK4")==null && rs.getString("CHECK5")==null && rs.getString("CHECK6")==null) continue;

                printHeader(db2,svf,param,3);
                printTitle(db2,svf,param,"03");

                svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1"      ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME2"      ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("CHECK1"  ,  rs.getString("CHECK1"));//尿検査
                svf.VrsOut("CHECK2"  ,  rs.getString("CHECK2"));//貧血検査
                svf.VrsOut("CHECK3"  ,  rs.getString("CHECK3"));//内科(校医)検診
                svf.VrsOut("CHECK4"  ,  rs.getString("CHECK4"));//歯科検診
                svf.VrsOut("CHECK5"  ,  rs.getString("CHECK5"));//胸部レントゲン撮影
                svf.VrsOut("CHECK6"  ,  rs.getString("CHECK6"));//心電図検査

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain3 read error!",ex);
        }
        return nonedata;
    }


    /**４）眼科検診のお知らせ*/
    private boolean printMain4(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            svf.VrSetForm("KNJF030_4.frm", 4);
            String sql = statementMeisai(param,4);
            log.debug("printMain4 sql="+sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                printHeader(db2,svf,param,4);
                printTitle(db2,svf,param,"02");

                svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME"      ,  rs.getString("NAME_SHOW"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION")) || 
                        isNumber(rs.getString("R_VISION")) || isNumber(rs.getString("L_VISION"))) {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION"));
                } else {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION_MARK"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION_MARK"));                    
                }
                svf.VrsOut("DISEASE"   ,  rs.getString("EYEDISEASECD"));

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain4 read error!",ex);
        }
        return nonedata;
    }


    /**５）検診結果のお知らせ（歯・口腔）*/
    private boolean printMain5(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            svf.VrSetForm("KNJF030_5.frm", 4);
            PreparedStatement ps = db2.prepareStatement(statementMeisai(param,5));
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                printHeader(db2,svf,param,5);
                printTitle(db2,svf,param,"04");

                svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1"      ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME2"      ,  rs.getString("NAME_SHOW"));
                svf.VrsOut("TEETHLINE"  ,  rs.getString("JAWS_JOINTCD"));
                svf.VrsOut("PLAQUE"     ,  rs.getString("PLAQUECD"));
                svf.VrsOut("GUM"        ,  rs.getString("GUMCD"));
                svf.VrsOut("OTHERS"     ,  rs.getString("OTHERDISEASECD"));
                svf.VrsOut("VIEWS"      ,  rs.getString("DENTISTREMARKCD"));
                svf.VrsOut("TEETHNO"    ,  rs.getString("GENZAI_SU"));
                svf.VrsOut("DECAYED1"   ,  rs.getString("SYOCHI_SU"));
                svf.VrsOut("DECAYED2"   ,  rs.getString("MISYOCHI_SU"));
                svf.VrsOut("DECAYED3"   ,  rs.getString("KANSATSU_SU"));
                svf.VrsOut("LOST"       ,  rs.getString("SOSHITSU_SU"));
                //未処置歯=0なら処置完了
                if (rs.getString("MISYOCHI_SU") != null && (rs.getString("MISYOCHI_SU")).equals("0")) {
                    svf.VrsOut("COMPLETION" ,  "完　了");
                } else {
                    svf.VrsOut("COMPLETION" ,  "");
                }

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain5 read error!",ex);
        }
        return nonedata;
    }


    /**６）定期健康診断結果*/
    private boolean printMain6(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            svf.VrSetForm("KNJF030_6.frm", 4);
            String sql = statementMeisai(param,6);
            log.debug("main6 sql="+sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                printHeader(db2,svf,param,6);

                svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME"      ,  rs.getString("NAME_SHOW"));
                //ヘッダ
                svf.VrsOut("YMD2"       , KNJ_EditDate.h_format_JP(rs.getString("DATE")));
                //詳細
                svf.VrsOut("HEIGHT"     ,  rs.getString("HEIGHT"));
                svf.VrsOut("WEIGHT"     ,  rs.getString("WEIGHT"));
                svf.VrsOut("BMI"        ,  rs.getString("BMI"));
                svf.VrsOut("SITHEIGHT"  ,  rs.getString("SITHEIGHT"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION")) || 
                        isNumber(rs.getString("R_VISION")) || isNumber(rs.getString("L_VISION"))) {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION"));
                } else {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION_MARK"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION_MARK"));                    
                }
                svf.VrsOut("EYES"       ,  rs.getString("EYEDISEASECD"));
                svf.VrsOut("HEARING_R1"     ,  rs.getString("R_EAR_DB"));
                svf.VrsOut("HEARING_R2"     ,  rs.getString("R_EAR"));
                svf.VrsOut("HEARING_L1"     ,  rs.getString("L_EAR_DB"));
                svf.VrsOut("HEARING_L2"     ,  rs.getString("L_EAR"));
                svf.VrsOut("NOSE"       ,  rs.getString("NOSEDISEASECD"));
                svf.VrsOut("SKIN"       ,  rs.getString("SKINDISEASECD"));
                svf.VrsOut("DISEASE"    ,  rs.getString("TB_NAMECD"));
                svf.VrsOut("GUIDE"      ,  rs.getString("TB_ADVISECD"));
                svf.VrsOut("INSPECTION" ,  rs.getString("HEART_MEDEXAM"));
                svf.VrsOut("UNUSUAL"    ,  rs.getString("HEARTDISEASECD"));
                svf.VrsOut("ALBUMIN1"       ,  rs.getString("ALBUMINURIA1CD"));
                svf.VrsOut("SACCHARIDE1"    ,  rs.getString("URICSUGAR1CD"));
                svf.VrsOut("BLOOD1"         ,  rs.getString("URICBLEED1CD"));
                svf.VrsOut("ALBUMIN2"       ,  rs.getString("ALBUMINURIA2CD"));
                svf.VrsOut("SACCHARIDE2"    ,  rs.getString("URICSUGAR2CD"));
                svf.VrsOut("BLOOD2"         ,  rs.getString("URICBLEED2CD"));
                svf.VrsOut("OTHERS1"    ,  rs.getString("OTHERDISEASECD"));
                svf.VrsOut("NOTE1"      ,  rs.getString("REMARK"));

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain6 read error!",ex);
        }
        return nonedata;
    }


    /**７）検診結果のお知らせ（一般）*/
    private boolean printMain7(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            svf.VrSetForm("KNJF030_7.frm", 4);
            PreparedStatement ps = db2.prepareStatement(statementMeisai(param,7));
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                //１人で１枚にまとめて出力
                if (param[8].equals("1")) {
                    printHeader(db2,svf,param,7);
                    printTitle(db2,svf,param,"01");
                    svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    if (rs.getString("Naika")!=null) //内科//NO001
                        svf.VrsOut("RESULT1_1"  ,  rs.getString("Naika"));            //内科
                    if (rs.getString("Hifuka")!=null) //皮膚科//NO001
                        svf.VrsOut("RESULT4_1"  ,  rs.getString("Hifuka"));             //皮膚科
                    if (rs.getString("Ganka")!=null) //眼科//NO001
                        svf.VrsOut("RESULT2_1"  ,  rs.getString("Ganka"));              //眼科
                    if (rs.getString("Sekichu_Kyokaku")!=null) //胸郭・脊柱//NO001
                        svf.VrsOut("RESULT5_1"  ,  rs.getString("Sekichu_Kyokaku"));    //胸郭・脊柱
                    if (rs.getString("Jibika")!=null) //耳鼻科//NO001
                        svf.VrsOut("RESULT3_1"  ,  rs.getString("Jibika"));             //耳鼻科
                    if (rs.getString("Sonota")!=null) //その他//NO001
                        svf.VrsOut("RESULT6"    ,  rs.getString("Sonota"));             //その他
                    if (rs.getString("Shinzou_Kenshin")!=null) //心臓検診//NO001
                        svf.VrsOut("RESULT7"    ,  rs.getString("Shinzou_Kenshin"));    //心臓検診
                    svf.VrEndRecord();
                }
                //１人で各種類ごとに出力
                if (param[8].equals("2")) {
                    if (rs.getString("Naika")!=null) //内科
                        printMain7_2(db2,svf,param,rs,"RESULT1_1",rs.getString("Naika"));
                    if (rs.getString("Hifuka")!=null) //皮膚科
                        printMain7_2(db2,svf,param,rs,"RESULT4_1",rs.getString("Hifuka"));
                    if (rs.getString("Ganka")!=null) //眼科
                        printMain7_2(db2,svf,param,rs,"RESULT2_1",rs.getString("Ganka"));
                    if (rs.getString("Sekichu_Kyokaku")!=null) //胸郭・脊柱
                        printMain7_2(db2,svf,param,rs,"RESULT5_1",rs.getString("Sekichu_Kyokaku"));
                    if (rs.getString("Jibika")!=null) //耳鼻科
                        printMain7_2(db2,svf,param,rs,"RESULT3_1",rs.getString("Jibika"));
                    if (rs.getString("Sonota")!=null) //その他
                        printMain7_2(db2,svf,param,rs,"RESULT6",rs.getString("Sonota"));
                    if (rs.getString("Shinzou_Kenshin")!=null) //心臓検診
                        printMain7_2(db2,svf,param,rs,"RESULT7",rs.getString("Shinzou_Kenshin"));
                }
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain7 read error!",ex);
        }
        return nonedata;
    }


    /**１人で各種類ごとに出力*/
    private void printMain7_2(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,String svfname,String svfdata)
    {
        try {
            printHeader(db2,svf,param,7);
            printTitle(db2,svf,param,"01");
            svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
            svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
            svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
            svf.VrsOut(svfname    ,  svfdata);
            svf.VrEndRecord();
            svf.VrsOut(svfname  ,  "");
        } catch( Exception ex ) {
            log.warn("printMain7_2 read error!",ex);
        }
    }


    /**８）内科検診所見あり生徒の名簿*/
    private boolean printMain8(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            int count = 0;
            svf.VrSetForm("KNJF030_8.frm", 4);
            PreparedStatement ps = db2.prepareStatement(statementMeisai(param,8));
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                printHeader(db2,svf,param,8);

                svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME_SHOW" ,  rs.getString("NAME_SHOW"));
                //内科
                if(rs.getString("Naika")!=null){
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Naika"));                //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("NUTRITION_RESULT"));      //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //皮膚科
                if(rs.getString("Hifuka")!=null){
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Hifuka"));               //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("SKINDISEASE_RESULT"));    //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //眼科
                if(rs.getString("Ganka")!=null){
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Ganka"));                //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("EYEDISEASE_RESULT"));     //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //胸郭・脊柱
                if(rs.getString("Sekichu_Kyokaku")!=null){
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Sekichu_Kyokaku"));      //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("SPINERIB_RESULT"));       //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //耳鼻科
                if(rs.getString("Jibika")!=null){
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Jibika"));               //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("NOSEDISEASE_RESULT"));    //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //その他
                if(rs.getString("Sonota")!=null){
                    svf.VrsOut("CHECKUP"    ,  rs.getString("Sonota"));             //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("OTHERDISEASE_RESULT"));   //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }
                //心臓検診
                if(rs.getString("Shinzou_Kenshin")!=null){
                    svf.VrsOut("CHECKUP"    ,  rs.getString("Shinzou_Kenshin"));    //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("HEARTDISEASE_RESULT"));   //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if(count==25){
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME_SHOW"));
                    count = 0;
                }

                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain8 read error!",ex);
        }
        return nonedata;
    }


    /**９）定期健康診断異常者一覧表*/
    private boolean printMain9(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            int count = 0;
            svf.VrSetForm("KNJF030_9.frm", 4);
            String sql = statementMeisai(param,9);
            log.debug("printMain9 sql="+sql);            
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOut("CHOICE"     , rs.getString("CHOICE"));//一般条件
                svf.VrsOut("CONDITIONS" , rs.getString("CONDITIONS"));//歯・口腔条件
                printHeader(db2,svf,param,9);
                count++;
                svf.VrlOut("NUMBER"     ,  count);
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("SCHOOLNO"   ,  rs.getString("SCHREGNO"));
                svf.VrsOut("NAME"       ,  rs.getString("NAME_SHOW"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION"))) {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));//右裸眼
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));//左裸眼
                } else {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_MARK"));//右裸眼
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_MARK"));//左裸眼                    
                }
//                  svf.VrsOut("SIGHT_R2"   ,  (rs.getString("R_VISION") != null) ? rs.getString("R_VISION") : "(" + rs.getString("R_VISION") + ")");//右矯正
//              svf.VrsOut("POINT"  ,  ",");//POINT
//                  svf.VrsOut("SIGHT_L2"   ,  (rs.getString("L_VISION") != null) ? rs.getString("L_VISION") : "(" + rs.getString("L_VISION") + ")");//左矯正
                svf.VrsOut("URINE"      ,  rs.getString("URINE"));
                svf.VrsOut("ANEMIA"         ,  rs.getString("Hinketsu"));
                svf.VrsOut("TUBERCULOSIS"   ,  rs.getString("TUBERCULOSIS"));
                svf.VrsOut("HEART"          ,  rs.getString("Ekibyo_Shinzo"));
                svf.VrsOut("HEART"          ,  rs.getString("Kensa_Shinzo"));
                svf.VrsOut("NOURISHMENT"    ,  rs.getString("Eiyo_Jyotai"));
                svf.VrsOut("SPINE"  ,  rs.getString("Sekichu_Kyokaku"));
                svf.VrsOut("EYES"   ,  rs.getString("Meekibyo_Ijyo"));
                svf.VrsOut("NOSE"   ,  rs.getString("Jibi_Shikkan"));
                svf.VrsOut("SKIN"   ,  rs.getString("Hifu_Shikkan"));
                svf.VrsOut("OTHERS1"    ,  rs.getString("Sonota"));
                svf.VrsOut("TEETH1"     ,  rs.getString("TEETH1"));
                svf.VrsOut("TEETH2"     ,  rs.getString("TEETH2"));
                svf.VrsOut("TEETH3"     ,  rs.getString("Shiretsu_Ago"));
                svf.VrsOut("SIKOU"      ,  rs.getString("Shikou"));
                svf.VrsOut("SINIKU"     ,  rs.getString("Shiniku"));
                svf.VrsOut("OTHERS2"    ,  rs.getString("Sonota_Ha"));

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain9 read error!",ex);
        }
        return nonedata;
    }


    /**ヘッダ項目をセット　共通*/
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],int check_no)
    {
        try {
            if (check_no == 3) {//健康診断の未受検項目のある生徒へ
                svf.VrsOut("SCHOOLNAME"     , param[22]);
                svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param[7]));
                svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param[7]) + " )");
            }
            if (check_no == 4) {//眼科検診のお知らせ
                svf.VrsOut("schoolname1"    , param[21]);
                svf.VrsOut("schoolname2"    , param[22]);
                svf.VrsOut("post"           , param[24]);
                svf.VrsOut("staff1"         , param[23]);
                svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param[7]));
                svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param[7]) + " )");
            }
            if (check_no == 5) {//検診結果のお知らせ（歯・口腔）
                svf.VrsOut("YMD"            , param[20]);
                svf.VrsOut("schoolname1"   , param[21]);
                svf.VrsOut("post1"          , param[24]);
                svf.VrsOut("staff1"         , param[23]);
                svf.VrsOut("schoolname2"   , param[21]);
                svf.VrsOut("post2"          , param[24]);
                svf.VrsOut("staff2"         , param[23]);
                svf.VrsOut("SCHOOLNAME3"    , param[22]);
            }
            if (check_no == 6) {//定期健康診断結果
                svf.VrsOut("YMD1"           , param[20]);
                svf.VrsOut("DATE"           , param[20]);
                svf.VrsOut("SCHOOLNAME"     , param[21]);
                svf.VrsOut("STAFFNAME1"     , param[23]);
            }
            if (check_no == 7) {//検診結果のお知らせ（一般）
                svf.VrsOut("schoolname1"    , param[21]);
                svf.VrsOut("post"           , param[24]);
                svf.VrsOut("staff1"         , param[23]);
            }
            if (check_no == 8) {//内科検診所見あり生徒の名簿
                svf.VrsOut("NENDO"  , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                svf.VrsOut("DATE"   , param[20]);
            }
            if (check_no == 9) {//定期健康診断異常者一覧表
                svf.VrsOut("NENDO"  , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                svf.VrsOut("YMD"    , param[20]);
            }
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }
    }


    /**文面マスタからタイトルと本文をセット　共通*/
    private void printTitle(DB2UDB db2,Vrw32alp svf,String param[],String documentcd)
    {
        try {
            PreparedStatement ps = db2.prepareStatement(statementTitle(param));
            ps.setString(1, documentcd);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (rs.getString("TEXT") != null) {
                    StringTokenizer st = new StringTokenizer(rs.getString("TEXT"), "\n");
                    int j = 1;
                    while(st.hasMoreTokens()) {
                        svf.VrsOut("TEXT" + j ,   st.nextToken());//本文
                        j++;
                    }
                }
                svf.VrsOut("TITLE"  , rs.getString("TITLE"));     //タイトル
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printTitle read error!",ex);
        }
    }


    /**
     *  １or２）生徒学生健康診断票（一般or歯・口腔）結果
     *
     */
    private String statementResult(String param[],int check_no,String gradeFlg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //在籍（現在年度）
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR = '"+param[0]+"' AND SEMESTER = '"+param[1]+"' AND ");
            stb.append("           SCHREGNO = ? ");//学籍番号
            stb.append("    ) ");
            //現在年度以外の学期を取得
            stb.append(",SCHNO_MIN AS ( ");
            stb.append("    SELECT SCHREGNO, YEAR, MIN(SEMESTER) AS SEMESTER ");
            stb.append("    FROM   SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE  EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO=W1.SCHREGNO AND W2.YEAR<>W1.YEAR) ");
            stb.append("    GROUP BY SCHREGNO, YEAR ");
            stb.append("    ) ");
            //在籍（現在年度以外）
            stb.append(",SCHNO_ALL AS ( ");
            stb.append("    SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE  EXISTS(SELECT 'X' FROM SCHNO_MIN W2 WHERE W2.SCHREGNO=W1.SCHREGNO AND W2.YEAR=W1.YEAR AND W2.SEMESTER=W1.SEMESTER) ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO ");
            stb.append("    FROM   SCHNO W1 ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.*, T2.HR_NAME, T3.NAME_SHOW, ");
            stb.append("       (SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=SEX) AS SEX, BIRTHDAY, ");
            stb.append("       CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) END AS AGE, ");
            if( check_no == 1 ) {//一般
                stb.append("   HEIGHT, WEIGHT, SITHEIGHT, R_BAREVISION, L_BAREVISION, R_VISION, L_VISION, R_BAREVISION_MARK, L_BAREVISION_MARK, R_VISION_MARK, L_VISION_MARK, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR, R_EAR_DB, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR, L_EAR_DB, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD, ");
                stb.append("   URICOTHERTEST, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2=NUTRITIONCD) AS NUTRITIONCD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD) AS SPINERIBCD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASECD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASECD, ");
                stb.append("   TB_FILMDATE, TB_FILMNO, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F100' AND NAMECD2=TB_REMARKCD) AS TB_REMARKCD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F110' AND NAMECD2=TB_OTHERTESTCD) AS TB_OTHERTESTCD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD, ");
                stb.append("   ANEMIA_REMARK, HEMOGLOBIN, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD, ");
                stb.append("   DOC_REMARK, DOC_DATE, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2=TREATCD) AS TREATCD, ");
                stb.append("   REMARK  ");
            } else {//歯・口腔
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN SMALLINT(JAWS_JOINTCD) = 1 THEN '○' END END AS JAWS_JOINTCD1, ");
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN SMALLINT(JAWS_JOINTCD) = 2 THEN '○' END END AS JAWS_JOINTCD2, ");
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN SMALLINT(JAWS_JOINTCD) = 3 THEN '○' END END AS JAWS_JOINTCD3, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN SMALLINT(PLAQUECD) = 1 THEN '○' END END AS PLAQUECD1, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN SMALLINT(PLAQUECD) = 2 THEN '○' END END AS PLAQUECD2, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN SMALLINT(PLAQUECD) = 3 THEN '○' END END AS PLAQUECD3, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN SMALLINT(GUMCD) = 1 THEN '○' END END AS GUMCD1, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN SMALLINT(GUMCD) = 2 THEN '○' END END AS GUMCD2, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN SMALLINT(GUMCD) = 3 THEN '○' END END AS GUMCD3, ");
                stb.append("   BABYTOOTH,REMAINBABYTOOTH,TREATEDBABYTOOTH,BRACK_BABYTOOTH, ");
                stb.append("   ADULTTOOTH,REMAINADULTTOOTH,TREATEDADULTTOOTH,LOSTADULTTOOTH,BRACK_ADULTTOOTH, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD) AS DENTISTREMARKCD, ");
                stb.append("   MONTH(DENTISTREMARKDATE) AS DENTISTREMARKMONTH,DAY(DENTISTREMARKDATE) AS DENTISTREMARKDAY, DENTISTTREAT ");
            }
            stb.append("FROM   SCHNO_ALL T1 ");
            stb.append("       INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR=T1.YEAR AND T2.SEMESTER=T1.SEMESTER AND T2.GRADE=T1.GRADE AND T2.HR_CLASS=T1.HR_CLASS ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            if( check_no == 1 ) {//一般
                stb.append("   INNER JOIN MEDEXAM_DET_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            } else {//歯・口腔
                stb.append("   INNER JOIN MEDEXAM_TOOTH_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            }
            if (gradeFlg.equals("J")) stb.append("WHERE  T1.GRADE <= '03' ");
            if (gradeFlg.equals("H")) stb.append("WHERE  T1.GRADE >= '04' ");
            stb.append("ORDER BY T1.YEAR ");
        } catch( Exception e ){
            log.warn("statementResult error!",e);
        }
        return stb.toString();
    }


    /**
     *  在籍情報
     *
     *  フォーム印刷
     */
    private String statementSchno(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[3].equals("1")) //1:クラス
                stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[3].equals("2")) //2:個人
                stb.append("       SCHREGNO IN "+param[2]+" ) ");

            //メイン
            stb.append("SELECT T2.SCHREGNO,NAME_SHOW,HR_NAME,T2.GRADE,T2.HR_CLASS,ATTENDNO ");
            stb.append("      ,(SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=SEX) AS SEX, BIRTHDAY ");
            stb.append("      ,CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) END AS AGE ");
            stb.append("FROM   SCHNO T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
            stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
            stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
        } catch( Exception e ){
            log.warn("statementSchno error!",e);
        }
        return stb.toString();
    }


    /**
     *  ３〜９）各種帳票
     *
     */
    private String statementMeisai(String param[],int check_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[3].equals("1")) //1:クラス
                stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[3].equals("2")) //2:個人
                stb.append("       SCHREGNO IN "+param[2]+" ) ");

            //メイン
            stb.append("SELECT T2.SCHREGNO,NAME_SHOW,HR_NAME,T2.GRADE,T2.HR_CLASS,ATTENDNO ");
            if( check_no == 3 ) {//健康診断の未受検項目のある生徒へ
                stb.append("  ,CASE WHEN ALBUMINURIA1CD > '0' OR URICSUGAR1CD > '0' OR ");
                stb.append("             URICBLEED1CD > '0' OR VALUE(URICOTHERTEST,'') > '0' THEN NULL ");
                stb.append("        ELSE '○' END AS CHECK1 ");
                stb.append("  ,CASE WHEN HEMOGLOBIN >= 0.1 THEN NULL ELSE '○' END AS CHECK2 ");
                stb.append("  ,CASE WHEN NUTRITIONCD > '0' AND SPINERIBCD > '0' AND ");
                stb.append("             NOSEDISEASECD > '0' AND SKINDISEASECD > '0' THEN NULL ");
                stb.append("        ELSE '○' END AS CHECK3 ");
                stb.append("  ,CASE WHEN DENTISTREMARKDATE IS NOT NULL THEN NULL ELSE '○' END AS CHECK4 ");
                stb.append("  ,CASE WHEN TB_FILMDATE IS NOT NULL THEN NULL ELSE '○' END AS CHECK5 ");
                stb.append("  ,CASE WHEN HEART_MEDEXAM > '0' THEN NULL ELSE '○' END AS CHECK6 ");
            }
            if( check_no == 4 ) {//眼科検診のお知らせ
                stb.append("  ,R_BAREVISION ,L_BAREVISION ,R_VISION ,L_VISION ,R_BAREVISION_MARK ,L_BAREVISION_MARK ,R_VISION_MARK ,L_VISION_MARK ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD ");
            }
            if( check_no == 5 ) {//検診結果のお知らせ（歯・口腔）
                stb.append("  ,BABYTOOTH + ADULTTOOTH AS GENZAI_SU ");
                stb.append("  ,TREATEDADULTTOOTH + TREATEDBABYTOOTH AS SYOCHI_SU ");
                stb.append("  ,REMAINADULTTOOTH + REMAINBABYTOOTH AS MISYOCHI_SU ");
                stb.append("  ,BRACK_ADULTTOOTH AS KANSATSU_SU ");//Modify
                stb.append("  ,LOSTADULTTOOTH AS SOSHITSU_SU ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2=JAWS_JOINTCD) AS JAWS_JOINTCD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F520' AND NAMECD2=PLAQUECD) AS PLAQUECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2=GUMCD) AS GUMCD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=T6.OTHERDISEASECD) AS OTHERDISEASECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD) AS DENTISTREMARKCD ");
            }
            if( check_no == 6 ) {//定期健康診断結果
                stb.append("  ,T7.DATE, R_EAR_DB, L_EAR_DB, REMARK ");
                stb.append("  ,HEIGHT, WEIGHT, SITHEIGHT, R_BAREVISION, R_BAREVISION_MARK, L_BAREVISION, L_BAREVISION_MARK, R_VISION,  R_VISION_MARK, L_VISION, L_VISION_MARK ");
                stb.append("  ,CASE WHEN VALUE(HEIGHT,0) > 0 THEN DECIMAL(ROUND(WEIGHT/HEIGHT/HEIGHT*10000,1),4,1) END AS BMI ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=T5.OTHERDISEASECD) AS OTHERDISEASECD ");
            }
            if( check_no == 7 || check_no == 8 ) {//検診結果のお知らせ（一般）or 内科検診所見あり生徒の名簿
                if( check_no == 8 ) 
                    stb.append("  ,NUTRITION_RESULT ,EYEDISEASE_RESULT ,SKINDISEASE_RESULT ,SPINERIB_RESULT ,NOSEDISEASE_RESULT ,OTHERDISEASE_RESULT ,HEARTDISEASE_RESULT ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2 > '01' AND NAMECD2=NUTRITIONCD) AS Naika ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2 > '01' AND NAMECD2=SPINERIBCD) AS Sekichu_Kyokaku ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2 > '01' AND NAMECD2=EYEDISEASECD) AS Ganka ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2 > '01' AND NAMECD2=NOSEDISEASECD) AS Jibika ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2 > '01' AND NAMECD2=SKINDISEASECD) AS Hifuka ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2 > '01' AND NAMECD2=HEARTDISEASECD) AS Shinzou_Kenshin ");//NO001
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2 > '01' AND NAMECD2=T5.OTHERDISEASECD) AS Sonota ");//NO001
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2=NUTRITIONCD) AS Naika ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD) AS Sekichu_Kyokaku ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS Ganka ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS Jibika ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS Hifuka ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS Shinzou_Kenshin ");
//              stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=T5.OTHERDISEASECD) AS Sonota ");
            }
            if( check_no == 9 ) {//定期健康診断異常者一覧表
                stb.append("  ,R_BAREVISION, L_BAREVISION, R_VISION, L_VISION, R_BAREVISION_MARK, L_BAREVISION_MARK, R_VISION_MARK, L_VISION_MARK ");
                stb.append("  ,CASE WHEN (ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR ");
                stb.append("             (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR ");
                stb.append("             (URICBLEED1CD > '01' AND URICBLEED1CD <> '05') THEN '○' END AS URINE ");
                stb.append("  ,CASE WHEN TB_REMARKCD = '02' THEN '○' END AS TUBERCULOSIS ");
                stb.append("  ,CASE WHEN REMAINBABYTOOTH > 0 THEN REMAINBABYTOOTH END AS TEETH1 ");
                stb.append("  ,CASE WHEN REMAINADULTTOOTH > 0 THEN REMAINADULTTOOTH END AS TEETH2 ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2 > '01' AND NAMECD2=NUTRITIONCD) AS Eiyo_Jyotai ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2 > '01' AND NAMECD2=SPINERIBCD) AS Sekichu_Kyokaku ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2 > '01' AND NAMECD2=EYEDISEASECD) AS Meekibyo_Ijyo ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2 > '01' AND NAMECD2=NOSEDISEASECD) AS Jibi_Shikkan ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2 > '01' AND NAMECD2=SKINDISEASECD) AS Hifu_Shikkan ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2 > '01' AND NAMECD2=HEART_MEDEXAM) AS Kensa_Shinzo ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2 > '01' AND NAMECD2=HEARTDISEASECD) AS Ekibyo_Shinzo ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2 > '01' AND NAMECD2=T5.OTHERDISEASECD) AS Sonota ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2 IN ('04','05') AND NAMECD2=TREATCD) AS Hinketsu ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2 > '01' AND NAMECD2=JAWS_JOINTCD) AS Shiretsu_Ago ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F520' AND NAMECD2 > '01' AND NAMECD2=PLAQUECD) AS Shikou ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2 > '01' AND NAMECD2=GUMCD) AS Shiniku ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2 > '01' AND NAMECD2=T6.OTHERDISEASECD) AS Sonota_Ha ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F610' AND NAMECD2='"+param[9]+"') AS CHOICE ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F620' AND NAMECD2='"+param[10]+"') AS CONDITIONS ");
            }
            stb.append("FROM   SCHNO T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
            stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN MEDEXAM_DET_DAT T5 ON T5.YEAR=T2.YEAR AND T5.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN MEDEXAM_TOOTH_DAT T6 ON T6.YEAR=T2.YEAR AND T6.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN MEDEXAM_HDAT T7 ON T7.YEAR=T2.YEAR AND T7.SCHREGNO=T2.SCHREGNO ");
            if( check_no == 4 ) {//眼科検診のお知らせ
                stb.append("WHERE  T5.YEAR='"+param[0]+"' AND ");
                stb.append("      (EYEDISEASECD >= '02' OR ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION)) AND R_BAREVISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION)) AND L_BAREVISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION)) AND R_VISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION)) AND L_VISION < '1.0')) ");
                stb.append("       OR ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION_MARK)) AND R_BAREVISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION_MARK)) AND L_BAREVISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION_MARK)) AND R_VISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION_MARK)) AND L_VISION_MARK <> 'A')) ) ");
            }
            if( check_no == 7 || check_no == 8 ) {//検診結果のお知らせ（一般）or 内科検診所見あり生徒の名簿
                stb.append("WHERE  T5.YEAR='"+param[0]+"' AND ");
                stb.append("      (SPINERIBCD >= '02' OR ");
                stb.append("       EYEDISEASECD >= '02' OR ");
                stb.append("       NOSEDISEASECD >= '02' OR ");
                if( check_no == 7 ) 
                    stb.append("   SKINDISEASECD >= '02' OR ");
                if( check_no == 8 ) 
                    stb.append("   SKINDISEASECD IN('02','03') OR ");
                stb.append("       HEARTDISEASECD >= '02' OR ");
                stb.append("       T5.OTHERDISEASECD >= '02' OR ");
                stb.append("       NUTRITIONCD >= '02' ) ");
            }
            if( check_no == 9 ) {//定期健康診断異常者一覧表
                stb.append("WHERE  T2.YEAR='"+param[0]+"' ");
                // 一般条件
                if (!param[9].equals("01")) 
                    stb.append(" AND T5.YEAR='"+param[0]+"' ");
                // 02．異常者全部
                if (param[9].equals("02")) {
                    stb.append(" AND ((('0.0' < R_BAREVISION AND R_BAREVISION < '1.0') OR ('0.0' < L_BAREVISION AND L_BAREVISION < '1.0')) ");
                    stb.append("   OR ((R_BAREVISION_MARK <> 'A') OR (L_BAREVISION_MARK <> 'A')) ");
                    stb.append("   OR ((ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR (URICBLEED1CD > '01' AND URICBLEED1CD <> '05')) ");
                    stb.append("   OR (TREATCD = '05') ");
                    stb.append("   OR (TREATCD = '04') ");
                    stb.append("   OR (TB_REMARKCD = '02') ");
                    stb.append("   OR (HEART_MEDEXAM > '01') ");
                    stb.append("   OR (HEARTDISEASECD > '01') ");
                    stb.append("   OR (NUTRITIONCD > '01') ");
                    stb.append("   OR (SPINERIBCD > '01') ");
                    stb.append("   OR (EYEDISEASECD > '01') ");
                    stb.append("   OR (NOSEDISEASECD > '01') ");
                    stb.append("   OR (SKINDISEASECD > '01') ");
                    stb.append("   OR (T5.OTHERDISEASECD > '01')) ");
                }
                // 03．視力 0.9〜0.7
                if (param[9].equals("03")) {
                    stb.append(" AND (('0.7' <= R_BAREVISION AND R_BAREVISION < '1.0') ");
                    stb.append("  OR  ('0.7' <= L_BAREVISION AND L_BAREVISION < '1.0') ");
                    stb.append("  OR  ( R_BAREVISION_MARK = 'B') ");
                    stb.append("  OR  ( L_BAREVISION_MARK = 'B')) ");
                }
                // 04．視力 0.6〜0.3
                if (param[9].equals("04")) {
                    stb.append(" AND (('0.3' <= R_BAREVISION AND R_BAREVISION < '0.7') ");
                    stb.append("  OR  ('0.3' <= L_BAREVISION AND L_BAREVISION < '0.7') ");
                    stb.append("  OR  ( R_BAREVISION_MARK = 'C') ");
                    stb.append("  OR  ( L_BAREVISION_MARK = 'C')) ");
                }
                // 05．視力 0.2以下
                if (param[9].equals("05")) {
                    stb.append(" AND (('0.0' < R_BAREVISION AND R_BAREVISION < '0.3') ");
                    stb.append("  OR  ('0.0' < L_BAREVISION AND L_BAREVISION < '0.3') ");
                    stb.append("  OR  ( R_BAREVISION_MARK = 'D') ");
                    stb.append("  OR  ( L_BAREVISION_MARK = 'D')) ");
                }
                // 06．尿　陽性者
                if (param[9].equals("06")) {
                    stb.append(" AND ((ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR (URICBLEED1CD > '01' AND URICBLEED1CD <> '05')) ");
                }
                // 07．貧血　要食事指導
                if (param[9].equals("07")) {
                    stb.append(" AND (TREATCD = '05') ");
                }
                // 08．貧血　要治療
                if (param[9].equals("08")) {
                    stb.append(" AND (TREATCD = '04') ");
                }
                // 09．結核　要再検者
                if (param[9].equals("09")) {
                    stb.append(" AND (TB_REMARKCD = '02') ");
                }
                // 10．心臓　要再検者
                if (param[9].equals("10")) {
                    stb.append(" AND (HEART_MEDEXAM > '01' OR HEARTDISEASECD > '01') ");
                }
                // 11．栄養状態異常
                if (param[9].equals("11")) {
                    stb.append(" AND (NUTRITIONCD > '01') ");
                }
                // 12．脊柱・胸郭異常
                if (param[9].equals("12")) {
                    stb.append(" AND (SPINERIBCD > '01') ");
                }
                // 13．目の疫病及び異常
                if (param[9].equals("13")) {
                    stb.append(" AND (EYEDISEASECD > '01') ");
                }
                // 14．耳鼻異常
                if (param[9].equals("14")) {
                    stb.append(" AND (NOSEDISEASECD > '01') ");
                }
                // 15．皮膚疾患異常
                if (param[9].equals("15")) {
                    stb.append(" AND (SKINDISEASECD > '01') ");
                }
                // 16．その他の疫病及び異常
                if (param[9].equals("16")) {
                    stb.append(" AND (T5.OTHERDISEASECD > '01') ");
                }
                // 歯・口腔条件
                if (!param[10].equals("01")) 
                    stb.append(" AND T6.YEAR='"+param[0]+"' ");
                // 02．異常者全部
                if (param[10].equals("02")) {
                    stb.append(" AND ((REMAINBABYTOOTH > 0) ");
                    stb.append("   OR (REMAINADULTTOOTH > 0) ");
                    stb.append("   OR (JAWS_JOINTCD > '01') ");
                    stb.append("   OR (PLAQUECD > '01') ");
                    stb.append("   OR (GUMCD > '01') ");
                    stb.append("   OR (T6.OTHERDISEASECD > '01')) ");
                }
                // 03．'未処置
                if (param[10].equals("03")) {
                    stb.append(" AND (REMAINBABYTOOTH > 0 OR REMAINADULTTOOTH > 0) ");
                }
                // 04．05．'歯列・咬合・歯顎関節
                if (param[10].equals("04") || param[10].equals("05")) {
                    stb.append(" AND (JAWS_JOINTCD > '01') ");
                }
                // 06．'歯垢状態
                if (param[10].equals("06")) {
                    stb.append(" AND (PLAQUECD > '01') ");
                }
                // 07．'歯肉状態
                if (param[10].equals("07")) {
                    stb.append(" AND (GUMCD > '01') ");
                }
                // 08．'歯その他疾病及異常
                if (param[10].equals("08")) {
                    stb.append(" AND (T6.OTHERDISEASECD > '01') ");
                }
            }
            stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();
    }


    /**
     *  文面マスタ情報
     *
     *  文面マスタからタイトルと本文を取得
     */
    private String statementTitle(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD = ?");
        } catch( Exception e ){
            log.warn("statementTitle error!",e);
        }
        return stb.toString();
    }



}//クラスの括り

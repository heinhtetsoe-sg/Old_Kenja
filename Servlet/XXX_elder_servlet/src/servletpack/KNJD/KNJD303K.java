// kanji=漢字
/*
 * $Id: cb48c94df158c9a9b3930ca47ac257d609c20a87 $
 *
 * 作成日: 2004/07/23 14:27:31 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＩＮ１０＞  得点分布表（科目別・５点毎）
 *
 *  2004/07/23 nakamoto 作成日
 *  2004/09/06 nakamoto フォームＩＤを変更
 *  2004/09/08 nakamoto 画面パラメータ”学年”を追加に対応
 *  2004/11/01 nakamoto testkind_mstを参照しない（タイトルのテスト種別名を固定で入力）
 *  2004/11/03 nakamoto 平均点と席次について、１科目でも欠席がある者と異動情報での対象者は、対象外とする。
 *  2004/12/03 nakamoto 異動生徒の判定は、指示画面からのパラメータ（異動対象日付）を基準。（学期終了日は基準としない）
 *  2004/12/17 nakamoto 学期末・学年末を追加
 *  2005/02/05 nakamoto db2.commit追加
 *  2005/02/14 nakamoto 補点・補充は、対象 NO001
 *                      出廷は、対象 NO002
 *  2005/02/15 nakamoto 公欠・欠席の条件を変更 NO003
 *  2005/03/10 nakamoto ３学期からの留学生は成績データを出力する。---NO012(学期末の場合のみ)
 *                      (３学期開始日付<=異動日<=３学期終了日付)
 ***********************************************************************************************
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 **/

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD303K {


    private static final Log log = LogFactory.getLog(KNJD303K.class);
    
    private String _useCurriculumcd = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[10];

    //  パラメータの取得
        String classcd[] = request.getParameterValues("CLASS_SELECTED");            //科目コード
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("GAKKI");                               //学期・9:学年末
            param[2] = request.getParameter("TESTKINDCD");                          //01:中間/02:期末/0:学期末
            param[5] = request.getParameter("GRADE");                               //学年 04/09/08
            String idobi = request.getParameter("DATE");                            //異動対象日付 04/12/03Add
            param[7] = idobi.replace('/','-');
            param[9] = param[1];    // 04/12/17Add
            _useCurriculumcd = request.getParameter("useCurriculumcd");              // 教育課程コード
        } catch( Exception ex ) {
            log.warn("parameter error!");
        }

    //  print設定
        new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //得点分布preparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));       //合計分布preparestatement
        } catch( Exception ex ) {
            log.warn("DB2 open error!");
        }
        //SVF出力
        for( int ia=0 ; ia<classcd.length ; ia++ ){
            if( Set_Detail_1(db2,svf,param,classcd[ia],ps1) ){      //帳票出力のメソッド(得点分布)
                Set_Detail_2(db2,svf,param,classcd[ia],ps2);        //合計行出力メソッド
                nonedata = true;
            }
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        Pre_Stat_f(ps1,ps2);        //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJD303.frm", 4);  //04/09/06  KIN10
        svf.VrsOut("NENDO"    ,nao_package.KenjaProperties.gengou
                                                    (Integer.parseInt(param[0])) + "年度");     //年度

    //  ＳＶＦ属性変更--->出力形式が科目別の場合科目毎に改ページ
        svf.VrAttribute("SUBCLASS","FF=1");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));      //作成日
            if (param[9].equals("9")) param[1] = returnval.val2;    //学年末の場合、今学期をセット 04/12/17Add
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
    //  学期名称の取得
        try {
            returnval = getinfo.Semester(db2,param[0],param[9]);    // 04/12/17Modify
            svf.VrsOut("SEMESTER" ,returnval.val1);       //学期名称
            param[6] = returnval.val2;                      //学期開始日 04/11/03Add
        } catch( Exception e ){
            log.warn("Semester name get error!");
        }
    //  テスト種別名の取得 04/11/01 04/12/17Modify
        String stb = "";
        if (param[2].equals("01")) stb = "中間テスト";
        if (param[2].equals("02")) stb = "期末テスト";
        svf.VrsOut("TESTNAME" ,stb);      //成績種別
        getinfo = null;
        returnval = null;

    //  各学期の中間・期末成績及びフラグ項目名の取得
        if(param[2].equals("01")){      //中間
            param[3] = "SEM"+param[1]+"_INTER_REC";         //各学期成績
            param[4] = "SEM"+param[1]+"_INTER_REC_FLG";     //各学期成績フラグ
            param[8] = param[3]+" IS NULL AND SEM"+param[1]+"_INTER_REC_DI in ('KS','KK') ";//---NO003
        }
        if(param[2].equals("02")){      //期末
            param[3] = "SEM"+param[1]+"_TERM_REC";          //各学期成績
            param[4] = "SEM"+param[1]+"_TERM_REC_FLG";      //各学期成績フラグ
            param[8] = param[3]+" IS NULL AND SEM"+param[1]+"_TERM_REC_DI in ('KS','KK') ";//---NO003
        }
        if(param[2].equals("0")){
            if(param[9].equals("9")) {  //学年末 04/12/17Add
                param[3] = "GRADE_RECORD";                  //各成績
                param[8] =  " (( " + 
                            " ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR  " + 
                            "  (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND  " + 
                            "   VALUE(SEM1_REC_FLG,'0') = '0' " + 
                            " ) OR (" + 
                            " ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR  " + 
                            "  (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND  " + 
                            "   VALUE(SEM2_REC_FLG,'0') = '0' " + 
                            " ) OR (" + 
                            " ((SEM3_TERM_REC  IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS'))) AND  " + 
                            "   VALUE(SEM3_TERM_REC_FLG,'0') = '0' " + 
                            " )) ";//---NO003
            } else {                    //学期末 04/12/17Add
                param[3] = "SEM"+param[1]+"_REC";           //各成績
                param[4] = "SEM"+param[1]+"_REC_FLG";       //各成績フラグ
                param[8] = "((SEM"+param[1]+"_INTER_REC IS NULL AND SEM"+param[1]+"_INTER_REC_DI IN('KK','KS')) OR " + 
                           " (SEM"+param[1]+"_TERM_REC  IS NULL AND SEM"+param[1]+"_TERM_REC_DI  IN('KK','KS'))) AND " + 
                           "  VALUE(SEM"+param[1]+"_REC_FLG,'0') = '0' ";//---NO003
            }
        }

    }//Set_Head()の括り



    /**SVF-FORM**/
    private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],String classcd,PreparedStatement ps1)
    {
        boolean nonedata = false;
        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //科目コード
            ps1.setString(++pp,classcd);    //科目コード 04/11/03Add
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
            //  科目出力
                svf.VrsOut("SUBCLASS"     , rs.getString("SUBCLASSNAME") );

            //  明細出力
                svf.VrsOut("HR_NAME"      , rs.getString("HR_NAME") );
                for (int j=1; j<20; j++)
                    svf.VrsOut("PEOPLE" + String.valueOf(j)   , rs.getString("REC" + String.valueOf(j)) );

            //  最高・最低・平均出力
                svf.VrsOut("MAXIMUM"      , rs.getString("REC_MAX") );
                svf.VrsOut("MINIMUM"      , rs.getString("REC_MIN") );
                svf.VrsOut("AVERAGE"      , rs.getString("REC_AVG") );

                svf.VrEndRecord();
                nonedata = true;
                Svf_Int(svf);                           //SVFフィールド初期化
            }
            rs.close();
            db2.commit();//05.02.05
        } catch( Exception ex ) {
            log.warn("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り



    /**SVF-FORM**/
    private void Set_Detail_2(DB2UDB db2,Vrw32alp svf,String param[],String classcd,PreparedStatement ps2)
    {
        try {
            int pp = 0;
            ps2.setString(++pp,classcd);    //科目コード
            ps2.setString(++pp,classcd);    //科目コード 04/11/03Add
            ResultSet rs = ps2.executeQuery();

            while( rs.next() ){
            //  明細出力
                svf.VrsOut("HR_NAME"      , "＜合計＞" );
                for (int j=1; j<20; j++)
                    svf.VrsOut("PEOPLE" + String.valueOf(j)   , rs.getString("REC" + String.valueOf(j)) );

            //  最高・最低・平均出力
                svf.VrsOut("MAXIMUM"      , rs.getString("REC_MAX") );
                svf.VrsOut("MINIMUM"      , rs.getString("REC_MIN") );
                svf.VrsOut("AVERAGE"      , rs.getString("REC_AVG") );

                svf.VrEndRecord();
                Svf_Int(svf);                           //SVFフィールド初期化
            }
            rs.close();
            db2.commit();//05.02.05
        } catch( Exception ex ) {
            log.warn("Set_Detail_2 read error!");
        }

    }//Set_Detail_2()の括り



    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ：科目コード２つ
        try {
            //04/11/03Start
            stb.append("WITH SCHNO AS( ");
            stb.append(   "SELECT YEAR,SCHREGNO ");
            stb.append(   "FROM   SCHREG_REGD_DAT ");
            stb.append(   "WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE='"+param[5]+"' ), ");
            //---NO012 START
            stb.append("SEM3_DATE AS ( ");
            stb.append("    SELECT SDATE,EDATE ");
            stb.append("    FROM   SEMESTER_MST ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='3' ), ");
            stb.append("SEM3_RYU AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("    FROM   SCHREG_TRANSFER_DAT K1,SEM3_DATE K2");
            stb.append("    WHERE  TRANSFERCD='1' AND ");
            stb.append("           TRANSFER_SDATE BETWEEN SDATE AND EDATE ), ");
            //---NO012 END
            //異動 NO025Modify
            stb.append(statementTransfer(param));
            //公欠・欠席 04/11/03Add
            stb.append("ABSENT AS(");
            stb.append(   "SELECT w1.schregno ");
            stb.append(   "FROM   kin_record_dat w1, SCHNO w2 ");
            stb.append(   "WHERE  w2.year=w1.year AND w2.schregno=w1.schregno AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ");
            }
            stb.append("          w1.subclasscd =? AND ");
            stb.append(           param[8]+" ) ");  // 04/12/17Modify
                        //メイン
            stb.append("SELECT ");
            stb.append( "t1.subclasscd,t1.gr_cl,t2.hr_name,t3.subclassname || '(' || t3.SUBCLASSABBV || ')' AS subclassname, ");
            stb.append( "rec1,rec2,rec3,rec4,rec5,rec6,rec7,rec8,rec9,rec10, ");
            stb.append( "rec11,rec12,rec13,rec14,rec15,rec16,rec17,rec18,rec19, ");
            stb.append( "rec_max,rec_min,int(rec_avg) rec_avg ");
            stb.append("FROM ");
            stb.append( "(SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ");
            }
            stb.append(     "w1.subclasscd as subclasscd, w2.gr_cl, ");
            stb.append(     "sum(case when w1."+param[3]+" between 100 and 100 then 1 else 0 end) rec1, ");
            stb.append(     "sum(case when w1."+param[3]+" between 95 and 99 then 1 else 0 end) rec2, ");
            stb.append(     "sum(case when w1."+param[3]+" between 90 and 94 then 1 else 0 end) rec3, ");
            stb.append(     "sum(case when w1."+param[3]+" between 85 and 89 then 1 else 0 end) rec4, ");
            stb.append(     "sum(case when w1."+param[3]+" between 80 and 84 then 1 else 0 end) rec5, ");
            stb.append(     "sum(case when w1."+param[3]+" between 75 and 79 then 1 else 0 end) rec6, ");
            stb.append(     "sum(case when w1."+param[3]+" between 70 and 74 then 1 else 0 end) rec7, ");
            stb.append(     "sum(case when w1."+param[3]+" between 65 and 69 then 1 else 0 end) rec8, ");
            stb.append(     "sum(case when w1."+param[3]+" between 60 and 64 then 1 else 0 end) rec9, ");
            stb.append(     "sum(case when w1."+param[3]+" between 55 and 59 then 1 else 0 end) rec10, ");
            stb.append(     "sum(case when w1."+param[3]+" between 50 and 54 then 1 else 0 end) rec11, ");
            stb.append(     "sum(case when w1."+param[3]+" between 45 and 49 then 1 else 0 end) rec12, ");
            stb.append(     "sum(case when w1."+param[3]+" between 40 and 44 then 1 else 0 end) rec13, ");
            stb.append(     "sum(case when w1."+param[3]+" between 35 and 39 then 1 else 0 end) rec14, ");
            stb.append(     "sum(case when w1."+param[3]+" between 30 and 34 then 1 else 0 end) rec15, ");
            stb.append(     "sum(case when w1."+param[3]+" between 20 and 29 then 1 else 0 end) rec16, ");
            stb.append(     "sum(case when w1."+param[3]+" between 10 and 19 then 1 else 0 end) rec17, ");
            stb.append(     "sum(case when w1."+param[3]+" between 1 and 9 then 1 else 0 end) rec18, ");
            stb.append(     "sum(case when w1."+param[3]+" between 0 and 0 then 1 else 0 end) rec19, ");
            stb.append(     "max(w1."+param[3]+") rec_max, ");
            stb.append(     "min(w1."+param[3]+") rec_min, ");
            stb.append(     "round(avg(float(w1."+param[3]+")),0) rec_avg ");
            stb.append( "FROM ");
            stb.append(     "kin_record_dat w1, ");
            stb.append(     "(SELECT ");
            stb.append(         "schregno, grade || hr_class gr_cl ");
            stb.append(     "FROM ");
            stb.append(         "schreg_regd_dat ");
            stb.append(     "WHERE ");
            stb.append(         "year='"+param[0]+"' AND semester='"+param[1]+"' ");
            stb.append(         "AND grade='"+param[5]+"' ");
            stb.append(     ") w2 ");
            stb.append( "WHERE ");
            stb.append(     "w1.year='"+param[0]+"' AND ");
            stb.append(     "w1."+param[3]+" is not null AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          w1.classcd || '-' || w1.school_kind || '-' || W1.curriculum_cd || '-' || ");
            }
            stb.append(     "w1.subclasscd =? AND ");
            stb.append(     "w2.schregno=w1.schregno ");
            stb.append(     "and not exists(SELECT 'X' FROM IDOU S1 WHERE S1.SCHREGNO=W1.SCHREGNO) ");
            stb.append(     "and not exists(SELECT 'X' FROM ABSENT S2 WHERE S2.SCHREGNO=W1.SCHREGNO) ");//---NO003
            stb.append( "GROUP BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ");
            }
            stb.append(     "w1.subclasscd,w2.gr_cl ");
            stb.append( ") t1 ");
            stb.append( "inner join schreg_regd_hdat t2 on t2.year='"+param[0]+"' and t2.semester='"+param[1]+"' ");
            stb.append(                                                 "and t2.grade || t2.hr_class = t1.gr_cl ");
            stb.append( "left join subclass_mst t3 on ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          t3.classcd || '-' || t3.school_kind || '-' || t3.curriculum_cd || '-' || ");
            }
            stb.append("     t3.subclasscd = t1.subclasscd ");
            stb.append("ORDER BY ");
            stb.append( "t1.subclasscd,t1.gr_cl ");
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り



    /**PrepareStatement作成**/
    private String Pre_Stat2(String param[]){

        StringBuffer stb = new StringBuffer();
    //  パラメータ：科目コード２つ
        try {
            //04/11/03Start
            stb.append("WITH SCHNO AS( ");
            stb.append(   "SELECT YEAR,SCHREGNO ");
            stb.append(   "FROM   SCHREG_REGD_DAT ");
            stb.append(   "WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE='"+param[5]+"' ), ");
            //---NO012 START
            stb.append("SEM3_DATE AS ( ");
            stb.append("    SELECT SDATE,EDATE ");
            stb.append("    FROM   SEMESTER_MST ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='3' ), ");
            stb.append("SEM3_RYU AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("    FROM   SCHREG_TRANSFER_DAT K1,SEM3_DATE K2");
            stb.append("    WHERE  TRANSFERCD='1' AND ");
            stb.append("           TRANSFER_SDATE BETWEEN SDATE AND EDATE ), ");
            //---NO012 END
            //異動 NO025Modify
            stb.append(statementTransfer(param));
            //公欠・欠席 04/11/03Add
            stb.append("ABSENT AS(");
            stb.append(   "SELECT w1.schregno ");
            stb.append(   "FROM   kin_record_dat w1, SCHNO w2 ");
            stb.append(   "WHERE  w2.year=w1.year AND w2.schregno=w1.schregno AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          w1.classcd || '-' || w1.school_kind || '-' || W1.curriculum_cd || '-' || ");
            }
            stb.append("          w1.subclasscd =? AND ");
            stb.append(           param[8]+" ) ");  // 04/12/17Modify
                        //メイン
            stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
            }
            stb.append( "t1.subclasscd as subclasscd, ");
            stb.append( "sum(case when t1."+param[3]+" between 100 and 100 then 1 else 0 end) rec1, ");
            stb.append( "sum(case when t1."+param[3]+" between 95 and 99 then 1 else 0 end) rec2, ");
            stb.append( "sum(case when t1."+param[3]+" between 90 and 94 then 1 else 0 end) rec3, ");
            stb.append( "sum(case when t1."+param[3]+" between 85 and 89 then 1 else 0 end) rec4, ");
            stb.append( "sum(case when t1."+param[3]+" between 80 and 84 then 1 else 0 end) rec5, ");
            stb.append( "sum(case when t1."+param[3]+" between 75 and 79 then 1 else 0 end) rec6, ");
            stb.append( "sum(case when t1."+param[3]+" between 70 and 74 then 1 else 0 end) rec7, ");
            stb.append( "sum(case when t1."+param[3]+" between 65 and 69 then 1 else 0 end) rec8, ");
            stb.append( "sum(case when t1."+param[3]+" between 60 and 64 then 1 else 0 end) rec9, ");
            stb.append( "sum(case when t1."+param[3]+" between 55 and 59 then 1 else 0 end) rec10, ");
            stb.append( "sum(case when t1."+param[3]+" between 50 and 54 then 1 else 0 end) rec11, ");
            stb.append( "sum(case when t1."+param[3]+" between 45 and 49 then 1 else 0 end) rec12, ");
            stb.append( "sum(case when t1."+param[3]+" between 40 and 44 then 1 else 0 end) rec13, ");
            stb.append( "sum(case when t1."+param[3]+" between 35 and 39 then 1 else 0 end) rec14, ");
            stb.append( "sum(case when t1."+param[3]+" between 30 and 34 then 1 else 0 end) rec15, ");
            stb.append( "sum(case when t1."+param[3]+" between 20 and 29 then 1 else 0 end) rec16, ");
            stb.append( "sum(case when t1."+param[3]+" between 10 and 19 then 1 else 0 end) rec17, ");
            stb.append( "sum(case when t1."+param[3]+" between 1 and 9 then 1 else 0 end) rec18, ");
            stb.append( "sum(case when t1."+param[3]+" between 0 and 0 then 1 else 0 end) rec19, ");
            stb.append( "max(t1."+param[3]+") rec_max, ");
            stb.append( "min(t1."+param[3]+") rec_min, ");
            stb.append( "int(round(avg(float(t1."+param[3]+")),0)) rec_avg ");
            stb.append("FROM ");
            stb.append( "kin_record_dat t1, ");
            stb.append( "(SELECT ");
            stb.append(     "schregno, grade || hr_class gr_cl ");
            stb.append( "FROM ");
            stb.append(     "schreg_regd_dat ");
            stb.append( "WHERE ");
            stb.append(     "year='"+param[0]+"' AND semester='"+param[1]+"' ");
            stb.append(         "AND grade='"+param[5]+"' ");
            stb.append( ") t2 ");
            stb.append("WHERE ");
            stb.append( "t1.year='"+param[0]+"' AND ");
            stb.append( "t1."+param[3]+" is not null AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
            }
            stb.append( "t1.subclasscd=? AND ");
            stb.append( "t2.schregno=t1.schregno ");
            stb.append(     "and not exists(SELECT 'X' FROM IDOU S1 WHERE S1.SCHREGNO=T1.SCHREGNO) ");
            stb.append(     "and not exists(SELECT 'X' FROM ABSENT S2 WHERE S2.SCHREGNO=T1.SCHREGNO) ");//---NO003
            stb.append("GROUP BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
            }
            stb.append( "t1.subclasscd ");
        } catch( Exception e ){
            log.warn("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り


    /**
     *  異動(共通SQL)
     *
     *　転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
     *　転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
     *　留学(1)・休学(2)者
     */
    private String statementTransfer(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //異動 NO025Modify
            stb.append("IDOU AS(");
            stb.append(    "SELECT  W1.SCHREGNO ");
            stb.append(    "FROM    SCHNO W1,SEMESTER_MST T1 ");
            stb.append(    "WHERE   T1.YEAR = W1.YEAR AND ");
            stb.append(            "T1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(            "( ");
            stb.append(            "EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                     "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                           "((S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                             "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END) OR ");
            stb.append(                            "(S1.ENT_DIV IN ('4','5') AND ");
            stb.append(                             "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END)) ) ");
            stb.append(            "OR ");
            stb.append(            "EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                     "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
            if (param[8].equals("9")) stb.append("   S1.SCHREGNO NOT IN (SELECT SCHREGNO FROM SEM3_RYU) AND ");//---NO012
            stb.append(                            "(S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                             "CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
            stb.append(            ") ");
            stb.append(    "), ");
        } catch( Exception e ){
            log.warn("statementTransfer error!",e);
        }
        return stb.toString();

    }//statementTransfer()の括り



    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2)
    {
        try {
            ps1.close();
            ps2.close();
        } catch( Exception e ){
            log.warn("Pre_Stat_f error!");
        }
    }//Pre_Stat_f()の括り



    /**SVF-FORM-FIELD-INZ**/
    private void Svf_Int(Vrw32alp svf){

        svf.VrsOut("HR_NAME"      , "" );
        svf.VrsOut("MAXIMUM"      , "" );
        svf.VrsOut("MINIMUM"      , "" );
        svf.VrsOut("AVERAGE"      , "" );
        for (int j=1; j<20; j++)
            svf.VrsOut("PEOPLE" + String.valueOf(j)   , "" );

    }//Svf_Int()の括り



}//クラスの括り

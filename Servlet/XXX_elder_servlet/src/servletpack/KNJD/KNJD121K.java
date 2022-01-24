// kanji=漢字
/*
 * $Id: 254c45bf4c714617219d074c7864a4ff360db7f5 $
 *
 * 作成日: 2004/12/01 0:49:03 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 *
 *  学校教育システム 賢者 [成績管理]  成績チエックリスト（中学）
 *
 *  2004/12/01 yamashiro・新規作成
 *  2004/12/13 yamashiro・同一講座コードを複数科目で使用した際に起こる不具合を修正 => 生徒名が重複して出力される
 *  2005/02/24 yamahsior・学年成績および評定出力条件を追加 M001
 *  2006/03/22 nakamoto ・NO002 複数クラスが混在する講座の場合、２枚目以降に各クラス毎のチェックリストを出力するよう追加修正
 *  2006/03/27 nakamoto ・NO003 不具合修正。ArrayIndexOutOfBoundsExceptionエラー
 *  2006/10/17 nakamoto ・NO004 遡及入力された値は、()表示をする==>１学期成績フラグが１または２学期成績フラグが１
 *  2006/10/26 nakamoto ・NO005 最低点０が出力されない不具合を修正した。
 *  2006/11/16 nakamoto ・NO006 選択科目の文字評定についての対応（ASSESS_MST.ASSESSMARKを表記する）
 *  2006/11/17 nakamoto ・NO007 選択科目の文字評定についての対応（名称マスタから表記する）
 *  2008/03/05 nakamoto ・NO008 ★ 以下の仕様をカットした。
 *                              -- 管理者コントロールで３学期期末もしくは３学期成績が編集可能な場合
 *                              -- ３学期成績にデータが無ければ学年成績、学年評定を非表示
 *
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD121K {

    private static final Log log = LogFactory.getLog(KNJD121K.class);
    private DecimalFormat dmf1 = new DecimalFormat("00");
    private int assessgrade;          //学年 => ３年時相対評価出力に使用
    private List grclList = new ArrayList();//NO002 年組を保持

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[8];//NO006

    //  パラメータの取得
        //String printname = request.getParameter("PRINTNAME");             //プリンタ名
        String printname = null;                                            //PDFで出力用！！
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("SEMESTER");                    //1-3:学期
            param[2] = request.getParameter("CHAIRCD");                     //講座コード
            param[3] = request.getParameter("SUBCLASSCD");                  //科目コード  04/12/13Modify
            param[4] = request.getParameter("ELECTDIV");                    //選択 NO006
            param[5] = request.getParameter("useCurriculumcd");             //プロパティ(教育課程コード)(1:教育課程対応)
            param[6] = request.getParameter("useProvFlg"); // プロパティ：仮評定の追加対応
            param[7] = request.getParameter("Z009");
        } catch( Exception ex ) {
            log.error("[KNJD121K]parameter error!", ex);
        }

    //  print設定-->printnameが存在する-->プリンターへ直接出力の場合
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        if( printname!=null )   response.setContentType("text/html");
        else                    response.setContentType("application/pdf");

    //  svf設定-->printnameが存在する-->プリンターへ直接出力の場合
        int ret = svf.VrInit();                             //クラスの初期化
        if( printname!=null ){
            ret = svf.VrSetPrinter("", printname);          //プリンタ名の設定
            if( ret < 0 ) log.info("printname ret = " + ret);
        } else
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
//for(int ib=0 ; ib<param.length ; ib++)log.debug("[KNJD121K]boolean Set_Detail_2() param["+ib+"]="+param[ib]);
    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJD121K]DB2 open error!", ex);
        }

    //  ＳＶＦ作成処理
        boolean nonedata = false;
        setHead(db2,svf,param);         //見出し項目
//NO002
//      if( printSvfMain(db2,svf,param) )nonedata = true;       //SVF-FORM出力処理
        ret = svf.VrsOut("TITLENO"  , "1" );
        if( printSvfMain(db2,svf,param,"0") )nonedata = true;       //SVF-FORM出力処理
        //複数クラスが混在する講座の場合のみ処理する
        if( nonedata && 1 < grclList.size() ){
            for(int ib=0 ; ib<grclList.size() ; ib++) {
                ret = svf.VrsOut("TITLENO"  , String.valueOf(ib+2) );
                printSvfMain(db2,svf,param,(String)grclList.get(ib));
            }
        }
//NO002

    //  該当データ無し-->printnameが存在する-->プリンターへ直接出力の場合
        if( printname!=null ){
            if( !nonedata ) outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
        } else if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        if( ret == 0 )log.info("===> VrQuit():" + ret);
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り



    /** SVF-FORMセット＆見出し項目 **/
    private void setHead(DB2UDB db2,Vrw32alp svf,String param[]){

        final String form = "1".equals(param[6]) ? "KNJD121_P.frm" : "KNJD121.frm";
        svf.VrSetForm(form, 4);

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

    //  作成日(現在処理日)
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception ex ){
            log.error("[KNJD121K]ReturnVal setHead() get TODAY error!", ex );
        }

    //  科目名
        ResultSet rs = null;
        try {
            db2.query( prestatementHeadSubclass(param) );
            rs = db2.getResultSet();
            if( rs.next() ){
                svf.VrsOut("SUBCLASS",rs.getString("SUBCLASSNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("[KNJD120K]setHead_ hrclass_staff error!", ex );
        }

    //  講座名
        try {
            db2.query( prestatementHeadChair(param) );
            rs = db2.getResultSet();
            if( rs.next() ){
                svf.VrsOut("HR_CLASS",rs.getString("CHAIRNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("[KNJD120K]setHead_ hrclass_staff error!", ex );
        }

        getinfo = null;
        returnval = null;

    }//setHead()の括り



    /** SVF-FORM メイン出力処理 **/
    private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[],String grclcd)
    {
for(int ib=0 ; ib<param.length ; ib++)log.debug("[KNJD121K]boolean Set_Detail_2() param["+ib+"]="+param[ib]);
        //定義
        boolean nonedata = false;
        boolean kanrishacontrol = bookanrishacontrol( db2, param );         //M001
        ResultSet rs = null;
        int total[][] = {{0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0}};   //合計 M001

        //RecordSet作成
        try {
log.debug("grclcd = "+grclcd);
            db2.query( prestatementRecord(param,0,grclcd) );            //生徒別成績データ
            rs = db2.getResultSet();
            int linex = 0;                              //１ページ当り出力行数
            String grcl = "0";//NO002
            while( rs.next() ){
                if ( grclcd.equals("0") && !grcl.equals(rs.getString("GR_CL")) ) grclList.add(rs.getString("GR_CL"));//NO002
                if( printSvfOutMeisai( param, svf, rs, kanrishacontrol, total ) ){          //SVF-FORMへ出力 M001 NO006
                    nonedata = true;
                    linex++;
                }
                grcl = rs.getString("GR_CL");//NO002
            }//while()
            if( linex<50 ){     //明細行は５０行まで->足りない場合は空行を出力！
                for( ; linex<50 ; linex++ ){
                    svf.VrAttribute("NAME","Meido=100");
                    svf.VrsOut("NAME"     ," . ");
                    svf.VrEndRecord();
                }
            }
            printSvfOutTotal( svf, total );     //SVF-FORMへ出力 M001

        } catch( Exception ex ) { log.error("[KNJD121K]printSvfMain read error! ", ex); }

        return nonedata;

    }//boolean printSvfMain()の括り



    /**
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力),
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private boolean printSvfOutMeisai( String param[], Vrw32alp svf, ResultSet rs, boolean kanrishacontrol, int total[][] ){

        boolean nonedata = false;
        try {
            int ret = 0;
            ret = svf.VrsOut("ATTENDNO"   , rs.getString("HR_NAMEABBV") + "-" + dmf1.format(rs.getInt("ATTENDNO")) );
            ret = svf.VrsOut("NAME"       , rs.getString("NAME"));

            printSvfOutDetal(svf, "POINT1_1", rs.getString("SEM1_INTER_REC"), rs.getString("SEM1_INTER_ATTEND") );
            printSvfOutDetal(svf, "POINT1_2", rs.getString("SEM1_TERM_REC") , rs.getString("SEM1_TERM_ATTEND") );
            if (rs.getString("SEM1_REC_FLG").equals("1")) {
                printSvfOutDetal(svf, "AVERAGE1", null , "(" + rs.getString("SEM1_REC") + ")" );
            } else {
                printSvfOutDetal(svf, "AVERAGE1", rs.getString("SEM1_REC")      , null );
            }

            printSvfOutDetal(svf, "POINT2_1", rs.getString("SEM2_INTER_REC"), rs.getString("SEM2_INTER_ATTEND") );
            printSvfOutDetal(svf, "POINT2_2", rs.getString("SEM2_TERM_REC") , rs.getString("SEM2_TERM_ATTEND") );
            if (rs.getString("SEM2_REC_FLG").equals("1")) {
                printSvfOutDetal(svf, "AVERAGE2", null , "(" + rs.getString("SEM2_REC") + ")" );
            } else {
                printSvfOutDetal(svf, "AVERAGE2", rs.getString("SEM2_REC")      , null );
            }

            printSvfOutDetal(svf, "POINT3_2", rs.getString("SEM3_TERM_REC") , rs.getString("SEM3_TERM_ATTEND"));
            printSvfOutDetal(svf, "AVERAGE3", rs.getString("SEM3_REC")      , null);

            if( rs.getString("SEM1_INTER_REC") != null ){
                total[0][0] += Integer.parseInt( rs.getString("SEM1_INTER_REC") );
                total[1][0] += 1;
                if( total[1][0] == 1  ||  Integer.parseInt( rs.getString("SEM1_INTER_REC") ) < total[2][0] ) total[2][0] = Integer.parseInt( rs.getString("SEM1_INTER_REC") );//NO005
                if( total[3][0] < Integer.parseInt( rs.getString("SEM1_INTER_REC") ) ) total[3][0] = Integer.parseInt( rs.getString("SEM1_INTER_REC") );
            }

            if( rs.getString("SEM1_TERM_REC") != null ){
                total[0][1] += Integer.parseInt( rs.getString("SEM1_TERM_REC") );
                total[1][1] += 1;
                if( total[1][1] == 1  ||  Integer.parseInt( rs.getString("SEM1_TERM_REC") ) < total[2][1] ) total[2][1] = Integer.parseInt( rs.getString("SEM1_TERM_REC") );//NO005
                if( total[3][1] < Integer.parseInt( rs.getString("SEM1_TERM_REC") ) ) total[3][1] = Integer.parseInt( rs.getString("SEM1_TERM_REC") );
            }

            if( rs.getString("SEM1_REC") != null && !rs.getString("SEM1_REC_FLG").equals("1") ){
                total[0][2] += Integer.parseInt( rs.getString("SEM1_REC") );
                total[1][2] += 1;
                if( total[1][2] == 1  ||  Integer.parseInt( rs.getString("SEM1_REC") ) < total[2][2] ) total[2][2] = Integer.parseInt( rs.getString("SEM1_REC") );//NO005
                if( total[3][2] < Integer.parseInt( rs.getString("SEM1_REC") ) ) total[3][2] = Integer.parseInt( rs.getString("SEM1_REC") );
            }

            if( rs.getString("SEM2_INTER_REC") != null ){
                total[0][3] += Integer.parseInt( rs.getString("SEM2_INTER_REC") );
                total[1][3] += 1;
                if( total[1][3] == 1  ||  Integer.parseInt( rs.getString("SEM2_INTER_REC") ) < total[2][3] ) total[2][3] = Integer.parseInt( rs.getString("SEM2_INTER_REC") );//NO005
                if( total[3][3] < Integer.parseInt( rs.getString("SEM2_INTER_REC") ) ) total[3][3] = Integer.parseInt( rs.getString("SEM2_INTER_REC") );
            }

            if( rs.getString("SEM2_TERM_REC") != null ){
                total[0][4] += Integer.parseInt( rs.getString("SEM2_TERM_REC") );
                total[1][4] += 1;
                if( total[1][4] == 1  ||  Integer.parseInt( rs.getString("SEM2_TERM_REC") ) < total[2][4] ) total[2][4] = Integer.parseInt( rs.getString("SEM2_TERM_REC") );//NO005
                if( total[3][4] < Integer.parseInt( rs.getString("SEM2_TERM_REC") ) ) total[3][4] = Integer.parseInt( rs.getString("SEM2_TERM_REC") );
            }

            if( rs.getString("SEM2_REC") != null && !rs.getString("SEM2_REC_FLG").equals("1") ){
                total[0][5] += Integer.parseInt( rs.getString("SEM2_REC") );
                total[1][5] += 1;
                if( total[1][5] == 1  ||  Integer.parseInt( rs.getString("SEM2_REC") ) < total[2][5] ) total[2][5] = Integer.parseInt( rs.getString("SEM2_REC") );//NO005
                if( total[3][5] < Integer.parseInt( rs.getString("SEM2_REC") ) ) total[3][5] = Integer.parseInt( rs.getString("SEM2_REC") );
            }

            if( rs.getString("SEM3_TERM_REC") != null ){
                total[0][6] += Integer.parseInt( rs.getString("SEM3_TERM_REC") );
                total[1][6] += 1;
                if( total[1][6] == 1  ||  Integer.parseInt( rs.getString("SEM3_TERM_REC") ) < total[2][6] ) total[2][6] = Integer.parseInt( rs.getString("SEM3_TERM_REC") );//NO005
                if( total[3][6] < Integer.parseInt( rs.getString("SEM3_TERM_REC") ) ) total[3][6] = Integer.parseInt( rs.getString("SEM3_TERM_REC") );
            }

            if( rs.getString("SEM3_REC") != null ){
                total[0][7] += Integer.parseInt( rs.getString("SEM3_REC") );
                total[1][7] += 1;
                if( total[1][7] == 1  ||  Integer.parseInt( rs.getString("SEM3_REC") ) < total[2][7] ) total[2][7] = Integer.parseInt( rs.getString("SEM3_REC") );//NO005
                if( total[3][7] < Integer.parseInt( rs.getString("SEM3_REC") ) ) total[3][7] = Integer.parseInt( rs.getString("SEM3_REC") );
            }

            //NO006
            if (param[4].equals("1")) {
                printSvfOutDetal(svf, "ASSESS"  , null                          , rs.getString("ASSESS") );
            } else {  // 学年成績および評定出力有無のチェック 05/02/24Modify
                printSvfOutDetal(svf, "AVERAGE4", rs.getString("GRADE_RECORD")  , null);
                printSvfOutDetal(svf, "ASSESS"  , null                          , rs.getString("ASSESS") );
                if( rs.getString("GRADE_RECORD") != null ){
                    total[0][8] += Integer.parseInt( rs.getString("GRADE_RECORD") );
                    total[1][8] += 1;
                    if( total[1][8] == 1  ||  Integer.parseInt( rs.getString("GRADE_RECORD") ) < total[2][8] ) total[2][8] = Integer.parseInt( rs.getString("GRADE_RECORD") );//NO005
                    if( total[3][8] < Integer.parseInt( rs.getString("GRADE_RECORD") ) ) total[3][8] = Integer.parseInt( rs.getString("GRADE_RECORD") );
                }
                if ("1".equals(param[6])) {
                    printSvfOutDetal( svf, "PROV_FLG",   rs.getString("PROV_FLG"), null);
                }
                if( rs.getString("ASSESS") != null ){
                    total[0][9] += Integer.parseInt( rs.getString("ASSESS") );
                    total[1][9] += 1;
                    if( total[1][9] == 1  ||  Integer.parseInt( rs.getString("ASSESS") ) < total[2][9] ) total[2][9] = Integer.parseInt( rs.getString("ASSESS") );//NO005
                    if( total[3][9] < Integer.parseInt( rs.getString("ASSESS") ) ) total[3][9] = Integer.parseInt( rs.getString("ASSESS") );
                }
            }

            if( assessgrade == 0 ) assessgrade = Integer.parseInt( rs.getString("GRADE") );
            if( assessgrade == 3 ){
                printSvfOutDetal(svf, "RELAASSESS5"  , null, rs.getString("ASSES_LEVEL_5") );
                printSvfOutDetal(svf, "RELAASSESS10" , null, rs.getString("ASSES_LEVEL_10") );
                if( rs.getString("ASSES_LEVEL_5") != null ){
                    total[0][10] += Integer.parseInt( rs.getString("ASSES_LEVEL_5") );
                    total[1][10] += 1;
                    if( total[1][10] == 1  ||  Integer.parseInt( rs.getString("ASSES_LEVEL_5") ) < total[2][10] ) total[2][10] = Integer.parseInt( rs.getString("ASSES_LEVEL_5") );//NO005
                    if( total[3][10] < Integer.parseInt( rs.getString("ASSES_LEVEL_5") ) ) total[3][10] = Integer.parseInt( rs.getString("ASSES_LEVEL_5") );
                }
                if( rs.getString("ASSES_LEVEL_10") != null ){
                    total[0][11] += Integer.parseInt( rs.getString("ASSES_LEVEL_10") );
                    total[1][11] += 1;
                    if( total[1][11] == 1  ||  Integer.parseInt( rs.getString("ASSES_LEVEL_10") ) < total[2][11] ) total[2][11] = Integer.parseInt( rs.getString("ASSES_LEVEL_10") );//NO005
                    if( total[3][11] < Integer.parseInt( rs.getString("ASSES_LEVEL_10") ) ) total[3][11] = Integer.parseInt( rs.getString("ASSES_LEVEL_10") );
                }
            }

            ret = svf.VrEndRecord();
            if( ret == 0 )nonedata = true;
        } catch( SQLException ex ){
            log.error("[KNJD121K]printSvfOutMeisai error!", ex );
        }

        return nonedata;

    }//printSvfOutMeisai()の括り



    /**
     *   管理者コントロールのチェック
     *   2005/02/24 Modify
     */
    private boolean bookanrishacontrol( DB2UDB db2, String param[] ){

        ResultSet rs = null;
        boolean output = false;
        try {
            db2.query( "SELECT CONTROL_CODE FROM   ADMIN_CONTROL_DAT "
                        + "WHERE  YEAR = '" + param[0] + "' AND CONTROL_FLG = '1' AND SCHOOL_KIND = '" + param[7] + "' "
                               + "CONTROL_CODE IN('0303')" );
            rs = db2.getResultSet();
            if( rs.next() ){
                output = true;
            }
            rs.close();
        } catch( Exception ex ){
            log.error("error! ", ex );
        }
        return output;

    }


    /**
     *   ＨＲ成績合計・平均・最高点・最低点を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力),
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotal( Vrw32alp svf, int total[][] ){

        dmf1.applyPattern("#.#");

        try {
            int i = 0;

            svf.VrsOut("ITEM" , "合計");
            if( 0 < total[1][i] )
                printSvfOutDetal(svf, "TOTAL1_1"         , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL1_2"         , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_1"         , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_2"         , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL3_2"         , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE3"   , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , String.valueOf( total[0][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_ASSESS"          , null  , String.valueOf( total[0][i] ) );

            if( assessgrade == 3 ){
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS5"     , null  , String.valueOf( total[0][i] ) );
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS10"    , null  , String.valueOf( total[0][i] ) );
            }
            svf.VrEndRecord();

            i = 0;
            svf.VrsOut("ITEM" , "平均");
            if( 0 < total[1][i] )
                printSvfOutfloat(svf, "TOTAL1_1"         , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL1_2"         , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL_AVERAGE1"   , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL2_1"         , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL2_2"         , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL_AVERAGE2"   , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL3_2"         , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL_AVERAGE3"   , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL_AVERAGE4"   , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) , null);
            if( 0 < total[1][++i] )
                printSvfOutfloat(svf, "TOTAL_ASSESS"          , null  , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) );
            if( assessgrade == 3 ){
                if( 0 < total[1][++i] )
                    printSvfOutfloat(svf, "TOTAL_RELAASSESS5"     , null  , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) );
                if( 0 < total[1][++i] )
                    printSvfOutfloat(svf, "TOTAL_RELAASSESS10"    , null  , String.valueOf( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) );
            }
            svf.VrEndRecord();

            i = 0;
            svf.VrsOut("ITEM" , "最高点");
            if( 0 < total[1][i] )
                printSvfOutDetal(svf, "TOTAL1_1"         , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL1_2"         , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_1"         , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_2"         , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL3_2"         , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE3"   , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , String.valueOf( total[3][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_ASSESS"          , null  , String.valueOf( total[3][i] ) );
            if( assessgrade == 3 ){
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS5"     , null  , String.valueOf( total[3][i] ) );
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS10"    , null  , String.valueOf( total[3][i] ) );
            }
            svf.VrEndRecord();

            i = 0;
            svf.VrsOut("ITEM" , "最低点");
            if( 0 < total[1][i] )
                printSvfOutDetal(svf, "TOTAL1_1"         , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL1_2"         , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_1"         , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL2_2"         , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL3_2"         , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE3"   , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , String.valueOf( total[2][i] )    , null);
            if( 0 < total[1][++i] )
                printSvfOutDetal(svf, "TOTAL_ASSESS"          , null  , String.valueOf( total[2][i] ) );
            if( assessgrade == 3 ){
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS5"     , null  , String.valueOf( total[2][i] ) );
                if( 0 < total[1][++i] )
                    printSvfOutDetal(svf, "TOTAL_RELAASSESS10"    , null  , String.valueOf( total[2][i] ) );
            }
            svf.VrEndRecord();
        } catch( Exception ex ){
            log.error("[KNJD121K]printSvfOutTotal error!", ex );
        }

    }//printSvfOutTotal()の括り

    /**
     *   ＳＶＦＲｅｃｏｒｄ出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => data2がnullなら成績データが入っている
     *     String data2 => 成績データまたは出欠のデータが入っている
     *                     成績データと出欠のデータが入っている場合は、繋いで出力
     */
    private void printSvfOutDetal(Vrw32alp svf, String svffieldname, String data1, String data2){

        try {
            if( data1 != null  &&  data2 == null )
                svf.VrAttribute(svffieldname , "Hensyu=1");       //右詰め
            else
                svf.VrAttribute(svffieldname , "Hensyu=3");       //中央割付

            if( data1 != null  &&  data2 != null )
                svf.VrsOut(svffieldname , data2 + ("   " + data1).substring( data1.length(), data1.length() + 3 ) );
            else if( data2 != null )
                svf.VrsOut(svffieldname , data2);
            else if( data1 != null )
                svf.VrsOut(svffieldname , data1);

        } catch( Exception ex ){
            log.error("[KNJD121K]printSvfOutDetal error!", ex );
        }

    }//printSvfOutDetal()の括り



    /**
     *   ＳＶＦＲｅｃｏｒｄ出力  formatして小数点を出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => nullでないなら成績データが入っている
     *     String data2 => nullでないなら成績データが入っている
     */
    private void printSvfOutfloat(Vrw32alp svf, String svffieldname, String data1, String data2){

        if( data1 == null  &&  data2 == null ) return;

        try {
            if( data1 != null  &&  data2 == null )
                svf.VrAttribute(svffieldname , "Hensyu=1");       //右詰め
            else
                svf.VrAttribute(svffieldname , "Hensyu=3");       //中央割付

            if( data2 != null )
                svf.VrsOut( svffieldname , dmf1.format(Float.parseFloat(data2)) );
            else if( data1 != null )
                svf.VrsOut(svffieldname , dmf1.format(Float.parseFloat(data1)) );

        } catch( Exception ex ){
            log.error("[KNJD121K]printSvfOutfloat error!", ex );
        }

    }//printSvfOutfloat()の括り



    /**
     *
     *   SQLStatement作成 成績データ
     *     int pdiv : 0=>生徒別  1=>合計
     *
     */
    String prestatementRecord(String param[],int pdiv,String grclcd) {

        StringBuffer stb = new StringBuffer();
        try {
            if( pdiv==0 ){
                if ("1".equals(param[6])) {
                    stb.append(" WITH RECORD_PROV_FLG AS ( ");
                    stb.append("     SELECT  SCHREGNO, PROV_FLG ");
                    stb.append("     FROM   RECORD_PROV_FLG_DAT ");
                    stb.append("     WHERE  YEAR = '" + param[0] + "' ");
                    //教育課程対応
                    if ("1".equals(param[5])) {
                        stb.append(            "AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
                    } else {
                        stb.append(            "AND SUBCLASSCD = '" + param[3] + "' ");
                    }
                    stb.append("     ) ");
                }
                stb.append("SELECT S1.HR_NAMEABBV, S1.ATTENDNO, S1.NAME, S1.GRADE, ");
                stb.append(       "S1.GRADE||S1.HR_CLASS AS GR_CL,");//NO002
                stb.append(       "SEM1_INTER_REC, SEM1_TERM_REC, SEM1_REC,");
                stb.append(       "SEM2_INTER_REC, SEM2_TERM_REC, SEM2_REC,");
                stb.append(       "SEM3_TERM_REC,  SEM3_REC,      GRADE_RECORD,");
                stb.append(       "VALUE(SEM1_REC_FLG,'0') as SEM1_REC_FLG,");//NO004
                stb.append(       "VALUE(SEM2_REC_FLG,'0') as SEM2_REC_FLG,");//NO004
                stb.append(       "CASE VALUE(SEM1_INTER_REC_DI,'0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM1_INTER_ATTEND,");
                stb.append(       "CASE VALUE(SEM1_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM1_TERM_ATTEND,");
                stb.append(       "CASE VALUE(SEM2_INTER_REC_DI,'0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM2_INTER_ATTEND,");
                stb.append(       "CASE VALUE(SEM2_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM2_TERM_ATTEND,");
                stb.append(       "CASE VALUE(SEM3_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM3_TERM_ATTEND,");
                if (param[4].equals("1")) {
                    stb.append(   "S3.NAME1             AS ASSESS,");//NO007
                } else{
                    stb.append(   "GRADE_ASSESS             AS ASSESS,");
                }
                if ("1".equals(param[6])) {
                    stb.append(        "CASE WHEN P0.PROV_FLG = '1' THEN 'レ' END AS PROV_FLG, ");
                }
                stb.append(       "GRADE3_RELAASSESS_5STEP  AS ASSES_LEVEL_5,");
                stb.append(       "GRADE3_RELAASSESS_10STEP AS ASSES_LEVEL_10 ");
            } else{
                stb.append("SELECT SUM(SEM1_INTER_REC)AS SUM_INTER_1,");
                stb.append(       "SUM(SEM1_TERM_REC) AS SUM_TERM_1,");
                stb.append(       "SUM(SEM1_REC)      AS SUM_1,");
                stb.append(       "SUM(SEM2_INTER_REC)AS SUM_INTER_2,");
                stb.append(       "SUM(SEM2_TERM_REC) AS SUM_TERM_2,");
                stb.append(       "SUM(SEM2_REC)      AS SUM_2,");
                stb.append(       "SUM(SEM3_TERM_REC) AS SUM_TERM_3,");
                stb.append(       "SUM(SEM3_REC)      AS SUM_3,");
                stb.append(       "SUM(GRADE_RECORD)  AS SUM_GRADE,");
                stb.append(       "SUM(INT(GRADE_ASSESS))             AS SUM_ASSESS,");
                stb.append(       "SUM(INT(GRADE3_RELAASSESS_5STEP))  AS SUM_ASSES_LEVEL_5,");
                stb.append(       "SUM(INT(GRADE3_RELAASSESS_10STEP)) AS SUM_ASSES_LEVEL_10,");

                stb.append(       "ROUND(AVG(FLOAT(SEM1_INTER_REC))*10,0)/10 AS AVG_INTER_1,");
                stb.append(       "ROUND(AVG(FLOAT(SEM1_TERM_REC))*10,0)/10  AS AVG_TERM_1,");
                stb.append(       "ROUND(AVG(FLOAT(SEM1_REC))*10,0)/10       AS AVG_1,");
                stb.append(       "ROUND(AVG(FLOAT(SEM2_INTER_REC))*10,0)/10 AS AVG_INTER_2,");
                stb.append(       "ROUND(AVG(FLOAT(SEM2_TERM_REC))*10,0)/10  AS AVG_TERM_2,");
                stb.append(       "ROUND(AVG(FLOAT(SEM2_REC))*10,0)/10       AS AVG_2,");
                stb.append(       "ROUND(AVG(FLOAT(SEM3_TERM_REC))*10,0)/10  AS AVG_TERM_3,");
                stb.append(       "ROUND(AVG(FLOAT(SEM3_REC))*10,0)/10       AS AVG_3,");
                stb.append(       "ROUND(AVG(FLOAT(GRADE_RECORD))*10,0)/10   AS AVG_GRADE,");
                stb.append(       "ROUND(AVG(FLOAT(INT(GRADE_ASSESS)))*10,0)/10             AS AVG_ASSESS,");
                stb.append(       "ROUND(AVG(FLOAT(INT(GRADE3_RELAASSESS_5STEP)))*10,0)/10  AS AVG_ASSES_LEVEL_5,");
                stb.append(       "ROUND(AVG(FLOAT(INT(GRADE3_RELAASSESS_10STEP)))*10,0)/10 AS AVG_ASSES_LEVEL_10,");

                stb.append(       "MAX(SEM1_INTER_REC) AS MAX_INTER_1,");
                stb.append(       "MAX(SEM1_TERM_REC)  AS MAX_TERM_1,");
                stb.append(       "MAX(SEM1_REC)       AS MAX_1,");
                stb.append(       "MAX(SEM2_INTER_REC) AS MAX_INTER_2,");
                stb.append(       "MAX(SEM2_TERM_REC)  AS MAX_TERM_2,");
                stb.append(       "MAX(SEM2_REC)       AS MAX_2,");
                stb.append(       "MAX(SEM3_TERM_REC)  AS MAX_TERM_3,");
                stb.append(       "MAX(SEM3_REC)       AS MAX_3,");
                stb.append(       "MAX(GRADE_RECORD)   AS MAX_GRADE,");
                stb.append(       "MAX(INT(GRADE_ASSESS))             AS MAX_ASSESS,");
                stb.append(       "MAX(INT(GRADE3_RELAASSESS_5STEP))  AS MAX_ASSES_LEVEL_5,");
                stb.append(       "MAX(INT(GRADE3_RELAASSESS_10STEP)) AS MAX_ASSES_LEVEL_10,");

                stb.append(       "MIN(SEM1_INTER_REC) AS MIN_INTER_1,");
                stb.append(       "MIN(SEM1_TERM_REC)  AS MIN_TERM_1,");
                stb.append(       "MIN(SEM1_REC)       AS MIN_1,");
                stb.append(       "MIN(SEM2_INTER_REC) AS MIN_INTER_2,");
                stb.append(       "MIN(SEM2_TERM_REC)  AS MIN_TERM_2,");
                stb.append(       "MIN(SEM2_REC)       AS MIN_2,");
                stb.append(       "MIN(SEM3_TERM_REC)  AS MIN_TERM_3,");
                stb.append(       "MIN(SEM3_REC)       AS MIN_3,");
                stb.append(       "MIN(GRADE_RECORD)   AS MIN_GRADE,");
                stb.append(       "MIN(INT(GRADE_ASSESS))             AS MIN_ASSESS,");
                stb.append(       "MIN(INT(GRADE3_RELAASSESS_5STEP))  AS MIN_ASSES_LEVEL_5,");
                stb.append(       "MIN(INT(GRADE3_RELAASSESS_10STEP)) AS MIN_ASSES_LEVEL_10 ");
            }
            stb.append(    "FROM  (SELECT T1.SCHREGNO,T3.NAME,T4.HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");
            stb.append(           "FROM  (SELECT SCHREGNO ");
            stb.append(                  "FROM   CHAIR_STD_DAT S1, ");
            stb.append(                         "CHAIR_DAT S2 ");                             // 04/12/13Add
            stb.append(                  "WHERE  S1.YEAR = '" + param[0] + "' AND ");
            stb.append(                         "S1.CHAIRCD = '" + param[2] + "' AND ");
            if ("1".equals(param[5])) {
                stb.append(                         "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
            } else {
                stb.append(                         "S2.SUBCLASSCD = '" + param[3] + "' AND ");   // 04/12/13Add
            }
            stb.append(                         "S2.YEAR = S1.YEAR AND ");                    // 04/12/13Add
            stb.append(                         "S2.SEMESTER = S1.SEMESTER AND ");            // 04/12/13Add
            stb.append(                         "S2.CHAIRCD = S1.CHAIRCD ");                  // 04/12/13Add
            stb.append(                  "GROUP BY SCHREGNO)T1,");
            stb.append(                  "SCHREG_REGD_DAT T2,");
            stb.append(                  "SCHREG_BASE_MST T3,");
            stb.append(                  "SCHREG_REGD_HDAT T4 ");
            stb.append(           "WHERE  T2.YEAR = '" + param[0] + "' AND T2.SCHREGNO = T1.SCHREGNO AND ");  //05/04/05Modify
            stb.append(                  "T2.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT W2 ");
            stb.append(                               "WHERE  W2.YEAR = '" + param[0] + "' AND ");
            stb.append(                                      "W2.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(                                      "W2.SCHREGNO = T2.SCHREGNO) AND ");
            if( !grclcd.equals("0") ){
                stb.append(              "T2.GRADE||T2.HR_CLASS = '" + grclcd + "' AND ");
            }
            stb.append(                  "T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND ");
            stb.append(                  "T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
            stb.append(                  "T4.HR_CLASS = T2.HR_CLASS)S1 ");
            stb.append(           "LEFT JOIN KIN_RECORD_DAT S2 ON S2.YEAR = '" + param[0] + "' AND ");
            if ("1".equals(param[5])) {
                stb.append(                                          "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
            } else {
                stb.append(                                          "S2.SUBCLASSCD = '" + param[3] + "' AND "); // 04/12/13Modify
            }
            stb.append(                                          "S2.SCHREGNO = S1.SCHREGNO ");
            //NO006
            if (param[4].equals("1"))
                stb.append(       "LEFT JOIN NAME_MST S3 ON S3.NAMECD1 = 'D001' AND S3.NAMECD2 = S2.GRADE_ASSESS ");//NO007
            if( pdiv==0 ){
                /* 仮評定情報 */
                //仮評定フラグ対応
                if ("1".equals(param[6])) {
                    stb.append(        " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = T1.SCHREGNO ");
                }
            }
            if( pdiv==0 )
                stb.append("ORDER BY S1.GRADE,S1.HR_CLASS,S1.ATTENDNO");
        } catch( Exception ex ){
            log.error("[KNJD121K]prestatementRecord error!", ex );
        }
//log.debug("[KNJD121K]prestatementRecord = "+stb.toString());

        return stb.toString();

    }//prestatementRecord()の括り



    /**
     *  SQLStatement作成 科目名
     *     2004/12/13 講座名称と科目名称の取得を分割
     **/
    String prestatementHeadSubclass(String param[]) {

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SUBCLASSNAME ");
            stb.append("FROM   SUBCLASS_MST ");
            if ("1".equals(param[5])) {
                stb.append("WHERE  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append("WHERE  SUBCLASSCD ='" + param[3] + "' ");
            }
        } catch( Exception ex ){
            log.error("[KNJD120K]prestatementHead error!", ex );
        }
        return stb.toString();

    }//prestatementHead()の括り


    /**
     *  SQLStatement作成 講座名
     *     2004/12/13 講座名称と科目名称の取得を分割
     **/
    String prestatementHeadChair(String param[]) {

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT CHAIRNAME ");
            stb.append("FROM   CHAIR_DAT ");
            stb.append("WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(       "CHAIRCD = '" + param[2] + "' AND ");
            if ("1".equals(param[5])) {
                stb.append(       "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append(       "SUBCLASSCD = '" + param[3] + "' ");
            }
        } catch( Exception ex ){
            log.error("[KNJD120K]prestatementHead error!", ex );
        }
        return stb.toString();

    }//prestatementHead()の括り


}//クラスの括り

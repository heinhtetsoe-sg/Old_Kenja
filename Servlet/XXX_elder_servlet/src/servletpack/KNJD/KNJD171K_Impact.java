// kanji=漢字
/*
 * $Id: 6edbab57f332adab776e99d226eaf2690f5f0f33 $
 *
 * 作成日: 2005/03/30 14:27:31 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *  学校教育システム 賢者 [成績管理]  中学校成績通知票（専用用紙）
 *
 *  2005/03/30 yamashiro 新規作成
 *
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD171K_Impact {

    private static final Log log = LogFactory.getLog(KNJD171K_Impact.class);
    private String tsubclass;               //対象科目コード
    private StringBuffer stb;
    private boolean nonedata;
    private String printname;               //プリンタ名
    private String schno[];                 //学籍番号
    private PrintWriter outstrm;
    private DecimalFormat dmf1 = new DecimalFormat("0");
    private DecimalFormat dmf2 = new DecimalFormat("0.0");
    private KNJSchoolMst _knjSchoolMst;


    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        String param[] = new String[13];

    // パラメータの取得
        getParam( request, param );

    // print svf設定
        setSvfInit(response, svf);

    // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }

        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param[0]);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }

    // 印刷処理
        printSvf( request, db2, svf, param );

    // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り


    /**
     *  印刷処理
     */
    private void printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[] ){

        try {
            param[5] = Set_Schno( schno );      //学籍番号の編集
            setHead( db2, svf, param );         //見出し項目
            getDivideAttendDate( db2, param );  //出欠用日付等取得 05/02/17
            if( printSvfMain( db2, svf, param) )nonedata = true;        //SVF-FORM出力処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  出欠集計テーブルをみる最終月と出欠データをみる開始日を取得する
     */
    private void getDivideAttendDate( DB2UDB db2, String param[] )
    {
        KNJDivideAttendDate obj = new KNJDivideAttendDate();
        try {
            obj.getDivideAttendDate( db2, param[0], param[1], param[7] );
            param[9] = obj.date;
            param[10] = obj.month;
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  対象生徒学籍番号編集(SQL用) 
     */
    private String Set_Schno(String schno[]){

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        for( int ia=0 ; ia<schno.length ; ia++ ){
            if( ia==0 ) stb.append("('");
            else        stb.append("','");
            stb.append(schno[ia]);
        }
        stb.append("')");

        return stb.toString();
    }


    /** 
     *  SVF-FORMセット＆見出し項目
     */
    private void setHead(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        if( Integer.parseInt( param[1] ) < 3 )
            svf.VrSetForm("KNJD171_2.frm", 1);            //SVF-FORM
        else
            svf.VrSetForm("KNJD171_1.frm", 1);            //SVF-FORM

    //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("ATTENDNO","FF=1");                   //出席番号で改ページ

    //  出力項目
        param[3] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]));  //年度

        try {
            returnval = getinfo.Staff_name(db2,param[0],param[1],param[2],"");
            param[4] = returnval.val1;                                              //学級担任名
        } catch( Exception ex ){
            log.error("[KNJD171K]setHead_ hrclass_staff error!",ex);
        }
        getinfo = null;
        returnval = null;

    }//setHead()の括り


    /** 
     *
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMain( DB2UDB db2, Vrw32alp svf, String param[] ) {
for(int ib=0 ; ib<param.length ; ib++)log.debug("param["+ib+"]="+param[ib]);
        //定義
        boolean nonedata = false;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        PreparedStatement ps5 = null;
        PreparedStatement ps6 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        KNJD171K_Impact.ReturnVal returnval = new KNJD171K_Impact.ReturnVal();  //ＳＶＦ出力位置およびフィールド名を取得するメソッド

        //SQL作成
        try {
            ps1 = db2.prepareStatement( prestatementRegd(param) );          //学籍データ
            ps2 = db2.prepareStatement( prestatementCommitAndClub(param) ); //委員会・クラブ活動データ
            ps3 = db2.prepareStatement( prestatementSubclass(param) );      //成績明細データ
            ps4 = db2.prepareStatement( prestatementAttend(param) );        //出欠データ
            ps5 = db2.prepareStatement( prestatementBeStudy(param) );       //生徒別科目履修単位情報 04/11/09Add
        } catch( Exception ex ) {
            log.error("[KNJD171K]boolean printSvfMain prepareStatement error! "+ex);
            return nonedata;
        }
        //RecordSet作成
        try {
            rs1 = ps1.executeQuery();
            //rs2 = ps2.executeQuery();
            rs3 = ps3.executeQuery();
//log.debug("ps3="+ps3.toString());
        } catch( SQLException ex ) {
            log.error("[KNJD171K]boolean printSvfMain executeQuery error! "+ex);
            return nonedata;
        }

        //データ読み込み＆ＳＶＦ出力
        try {
            int schno1 = 0; //学籍・健康診断データの出席番号
            int schno3 = 0; //成績明細データの出席番号
            while( rs1.next() ){
                //初回を除いて印刷！
                if( schno1 != 0 ){
                    svf.VrEndPage();
                    nonedata = true;
                }
                schno1 = rs1.getInt("ATTENDNO");                        //学籍データの出席番号を保存
                printSvfRegdOut(db2, svf, rs1, param, ps5);                 //学籍データ出力

                //成績明細データをセット
                for( ; -1 < schno3  &&  schno3 <= schno1 ; ){
                    if( schno3 == schno1 ) printSvfRecDetailOut( svf, rs3, param, returnval );      //成績明細データ出力
                    if( rs3.next() ) schno3 = rs3.getInt("ATTENDNO");               //成績明細データの出席番号を保存
                    else             schno3 = -1;
                }

                //出欠データをセット 04/11/02取り敢えず除外！ 04/11/03Modify
                if( param[6].equals("99") )
                    printSvfAttend( svf, ps4, rs1.getString("SCHREGNO"), Integer.parseInt( param[1] ) );        //出欠データ出力

                //委員会・クラブ活動データをセット
                printSvfCommitAndClub( svf, ps2, rs1.getString("SCHREGNO") );
            }
            if( schno1!=0 ){
                svf.VrEndPage();
                nonedata = true;
log.debug("nonedata="+nonedata);
            }
        } catch( Exception ex ) { log.error("[KNJD171K]printSvfMain read error! "+ex);  }

        try {
            if( rs1!=null ) rs1.close();
            if( rs2!=null ) rs2.close();
            if( rs3!=null ) rs3.close();
        } catch( SQLException ex ) { log.error("[KNJD171K]printSvfMain rs.close() error!  "+ex); }

        prestatementClose(ps1, ps2, ps3, ps4, ps5, ps6);    //preparestatementを閉じる

        return nonedata;

    }//boolean printSvfMain()の括り



    /** 
     *
     * SVF-OUT 学籍
     *
     */
    private void printSvfRegdOut(DB2UDB db2, Vrw32alp svf, ResultSet rs, String param[], PreparedStatement ps5){

        try {
            svf.VrsOut("NENDO",     param[3]);                                                    //年度
            svf.VrsOut("GRADE",     String.valueOf(Integer.parseInt(param[2].substring(0,2))));   //学年
            svf.VrsOut("HR_CLASS",  param[2].substring(2));                                       //組
            svf.VrsOut("ATTENDNO",  String.valueOf(rs.getInt("ATTENDNO")));                       //出席番号
            svf.VrsOut("NAME",      rs.getString("NAME"));                                        //生徒名
            svf.VrsOut("STAFFNAME", param[4]);                                                    //学級担任

            if( param[11] == null )getCoursecodeName( db2, rs.getString("COURSECODE"), param );
            svf.VrsOut("NOTE1",  "上記の平均点は" + param[11]  + "の平均です。" );

        } catch( SQLException ex ){
            log.error("[KNJD171K]printSvfRegdOut error!",ex);
        }

    }//printSvfRegdOut()の括り


    /** 
     *
     * SVF-OUT 備考のコース名設定
     * １学年、２学年の医薬・特進はグループ化
     *
     */
    private void getCoursecodeName( DB2UDB db2, String coursecode, String param[] )
    {
        if( param[11] != null ) return;
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );
        try {
            stb.append(    "SELECT  COURSECODE,COURSECODENAME ");
            stb.append(    "FROM    COURSECODE_MST ");
            if( coursecode.equals("JOIN") )
                stb.append("WHERE   COURSECODE IN('1001','2001') ");
            else
                stb.append("WHERE   COURSECODE = '" + coursecode + "' ");
            db2.query( stb.toString() );
            ResultSet rs = db2.getResultSet();
            if( stb == null ) stb = new StringBuffer();
            else              stb.delete( 0, stb.length() );
            while( rs.next() ){
                if( 0 < stb.length() )stb.append("・");
                stb.append( rs.getString("COURSECODENAME") );
            }
            param[11] = stb.toString();
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /** 
     *
     * SVF-OUT 成績明細印刷
     */
    private void printSvfRecDetailOut( Vrw32alp svf, ResultSet rs, String param[], KNJD171K_Impact.ReturnVal returnval ){

        try {
            returnval.getSvfOutPoint( Integer.parseInt( rs.getString("SEMESTER") ), Integer.parseInt( rs.getString("KIND") ) );
            returnval.getSvfOutField( rs.getString("SUBCLASSCD"), Integer.parseInt( rs.getString("SEMESTER") ), Integer.parseInt( rs.getString("KIND") ) );
            svf.VrsOutn( returnval.field,  returnval.point,      getOutputDat( rs ) );            //得点
            svf.VrsOutn( returnval.field,  returnval.point + 1,  getOutputHrDat( rs ) );          //得点
            if( rs.getString("SEMESTER").equals("3")  &&  Integer.parseInt( rs.getString("KIND") ) == 3  &&  rs.getString("ASSESS") != null )
                svf.VrsOutn( returnval.field,  17,  getOutputHrDat( rs ) );                       //学年評定
//log.debug("semes="+Integer.parseInt( rs.getString("SEMESTER") ) + "   kind="+ Integer.parseInt( rs.getString("KIND") ) );
//log.debug("field="+returnval.field + "   point="+returnval.point + "   getOutputDat( rs )="+getOutputDat( rs ));

        } catch( SQLException ex ){ log.error("error! "+ex ); }

    }//printSvfRecDetailOut()の括り


    /** 
     *
     * SVF-FORM 生徒成績データをセット
     */
    private String getOutputDat( ResultSet rs )
    {
        String retval = null;
        try {
            if( rs.getString("KK_SCORE") == null ){
                if( rs.getString("SCORE") != null ){
                    if( rs.getString("SUBCLASSCD").charAt( rs.getString("SUBCLASSCD").length() - 1 ) == 'H' )
                        retval = setScoreFormat( 2, rs.getString("SCORE") );        //小数点第一位まで
                    else
                        retval = setScoreFormat( 1, rs.getString("SCORE") );        //整数
                }
            } else{
                retval = rs.getString("KK_SCORE");                                      //欠席
            }
        } catch( Exception ex ){
            log.error("getOutputDate error! ",ex);
        }
        if( retval == null ) retval = "";
        return retval;
    }


    /** 
     *
     * SVF-FORM クラス平均データをセット
     */
    private String getOutputHrDat( ResultSet rs )
    {
        String retval = null;
        try {
            if( rs.getString("HR_AVG_SCORE") != null ){
                if( rs.getString("SUBCLASSCD").charAt( rs.getString("SUBCLASSCD").length() - 1 ) == 'G' )
                    retval = setScoreFormat( 1, rs.getString("HR_AVG_SCORE") );     //整数
                else
                    retval = setScoreFormat( 2, rs.getString("HR_AVG_SCORE") );     //小数点第一位まで
            }
        } catch( Exception ex ){
            log.error("getOutputDate error! ",ex);
        }
        if( retval == null ) retval = "";
        return retval;
    }


    /** 
     *
     * SVF-FORM 数値編集
     */
    private String setScoreFormat( int div, String score )
    {
        String retval = null;
        if( score == null )return "";
        try {
            if( div == 1)
                retval = String.valueOf( dmf1.format( Float.parseFloat(score) ) );
            else
                retval = String.valueOf( dmf2.format( Float.parseFloat(score) ) );
        } catch( Exception ex ){
            log.error("getOutputDate error! ",ex);
        }
        if( retval == null ) retval = "";
        return retval;
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷処理 
     */
    private void printSvfAttend( Vrw32alp svf, PreparedStatement ps4, String schregno, int sem )
    {
        try {
            if( sem < 3 )
                printSvfAttendOut( svf, ps4, schregno, sem, sem, "" );
            else{
                printSvfAttendOut( svf, ps4, schregno, sem, sem, "1" );
                printSvfAttendOut( svf, ps4, schregno, 1,   3,   "2" );
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷
     */
    private void printSvfAttendOut( Vrw32alp svf, PreparedStatement ps4, String schregno, int sem1, int sem2, String field )
    {

        ResultSet rs = null;
        try {
            int pp = 0;
            ps4.setString( ++pp, schregno );                    //生徒番号
            ps4.setString( ++pp, String.valueOf(sem1) );        //学期
            ps4.setString( ++pp, String.valueOf(sem2) );        //学期
            ps4.setString( ++pp, String.valueOf(sem1) );        //学期
            ps4.setString( ++pp, String.valueOf(sem2) );        //学期
            ps4.setString( ++pp, String.valueOf(sem1) );        //学期
            ps4.setString( ++pp, String.valueOf(sem2) );        //学期
//log.debug("ps4="+ps4.toString());
            rs = ps4.executeQuery();

            if( rs.next() ){
                if( 0 <= Integer.parseInt(rs.getString("LESSON")) )
                    svf.VrsOut("LESSON" + field,   rs.getString("LESSON") );              //授業日数
                svf.VrsOut("SUSPEND"    + field,   rs.getString("MOURNING_SUSPEND") );    //出停・忌引日数
                svf.VrsOut("ATTEND"     + field,   rs.getString("PRESENT") );             //出席日数
                svf.VrsOut("ABSENCE"    + field,   rs.getString("ABSENT") );              //欠席日数
                svf.VrsOut("LATE"       + field,   rs.getString("LATE") );                //遅刻回数
                svf.VrsOut("LEAVE"      + field,   rs.getString("EARLY") );               //早退回数
            }
        } catch( Exception ex ){
            log.error("[KNJD171K]printSvfAttendOut error!",ex);
        } finally{
            try {
                 rs.close();
            } catch( Exception ex ){
                log.error("[KNJD171K]printSvfRegdOut error!",ex);
            }
        }

    }//printSvfAttendOut()の括り


    /** 
     *
     * SVF-OUT 委員会・クラブ活動印刷処理
     */
    private void printSvfCommitAndClub( Vrw32alp svf, PreparedStatement ps2, String schregno )
    {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps2.setString( ++pp, schregno );                    //生徒番号
            ps2.setString( ++pp, schregno );                    //生徒番号
//log.debug("ps2="+ps2.toString());
            rs = ps2.executeQuery();

            while ( rs.next() ){
                if( rs.getString("FLG").equals("1") )
                    svf.VrsOut("COMMITTEENAME1",   rs.getString("NAME") );    //生徒会
                else if( rs.getString("FLG").equals("2") )
                    svf.VrsOut("COMMITTEENAME2",   rs.getString("NAME") );    //学級活動
                else if( rs.getString("FLG").equals("3") )
                    svf.VrsOut("CLUBNAME",         rs.getString("NAME") );    //クラブ活動
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                 rs.close();
            } catch( Exception ex ){
                log.error("error! ",ex);
            }
        }
    }


    /** 
     *  PrepareStatement作成 
     *  学籍
     */
    String prestatementRegd(String param[])
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try {
            //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO AS(");
            stb.append(    "SELECT T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(           "CASE WHEN T1.GRADE IN('01','02') AND T1.COURSECODE <> '3001' THEN 'JOIN'  ELSE T1.COURSECODE END AS COURSECODE ");
            stb.append(    "FROM   SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
            stb.append(    "WHERE  T1.YEAR = '" + param[0] + "' AND ");
            stb.append(           "T1.SEMESTER = '"+param[1]+"' AND ");
            stb.append(           "T1.YEAR = T2.YEAR AND ");
            stb.append(           "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(           "T1.GRADE||T1.HR_CLASS = '" + param[2] + "' AND ");
            stb.append(           "T1.SCHREGNO IN" + param[5] + " AND ");
            stb.append(           "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE  S1.SCHREGNO = T1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                              "S1.GRD_DATE <= CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                                  "ELSE '" + param[7] + "' END ) AND ");
            stb.append(           "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE  S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                              "((S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                                "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                     "ELSE '" + param[7] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");   // 04/11/09Modify
            stb.append(                               "(S1.TRANSFERCD IN ('4') AND ");
            stb.append(                                "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                     "ELSE '" + param[7] + "' END < S1.TRANSFER_SDATE)) )) ");          // 04/11/09Modify

            stb.append("SELECT  W1.ATTENDNO, W1.SCHREGNO, W2.NAME, W1.COURSECODE ");
            stb.append("FROM    SCHNO W1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST W2 ON W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("ORDER BY 1");

        } catch( Exception ex ){
            log.error("[KNJD171K]prestatementRegd error!",ex);
        }
        return stb.toString();

    }//prestatementRegd()の括り



    /** 
     *  PrepareStatement作成 成績明細-->生徒別科目別 ５教科 ９教科
     */
    private String prestatementSubclass(String param[]){

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try {
            //学籍の表（学年）
            stb.append("WITH SCHNO_C AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, ");
            stb.append(            "CASE WHEN T1.GRADE IN('01','02') AND T1.COURSECODE <> '3001' THEN 'JOIN'  ELSE T1.COURSECODE END AS COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(            "T1.YEAR = T2.YEAR AND ");
            stb.append(            "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(            "T1.GRADE = '" + param[2].substring(0,2) + "' AND ");
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                               "S1.GRD_DATE <= CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                                   "ELSE '" + param[7] + "' END ) AND ");
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param[7] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param[7] + "' END < S1.TRANSFER_SDATE)) )");
            stb.append(    ")");

            //学籍の表（クラス）
            stb.append(",SCHNO_B AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHNO_C T1 ");
            stb.append(    "WHERE   T1.GRADE||T1.HR_CLASS = '" + param[2] + "' ");
            stb.append(    ")");

            //学籍の表（コース）
            stb.append(",SCHNO_A AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHNO_C T1 ");
            stb.append(    "WHERE   (T1.SEMESTER,T1.COURSECODE) IN(SELECT  T2.SEMESTER,T2.COURSECODE ");
            stb.append(                                           "FROM    SCHNO_C T2 ");
            stb.append(                                           "WHERE   T2.GRADE||T2.HR_CLASS = '" + param[2] + "' ) ");
            stb.append(    ")");

            //成績データの表
            stb.append(",RECORD_DAT_A AS(");
            stb.append(    "SELECT  '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, W1.SUBCLASSCD, ");
            stb.append(            "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                      "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE3, ");
            stb.append(            "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM1_INTER_REC IS NOT NULL THEN SEM1_INTER_REC ");
            stb.append(                 "ELSE NULL END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM1_TERM_REC IS NOT NULL THEN SEM1_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                      "VALUE(SEM1_REC_FLG,'0') = '0' THEN NULL ");
            stb.append(                 "WHEN SEM1_REC IS NOT NULL THEN SEM1_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '1' AND ");
            stb.append(                                    "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(    "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, W1.SUBCLASSCD, ");
            stb.append(            "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                      "VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE3, ");
            stb.append(            "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM2_INTER_REC IS NOT NULL THEN SEM2_INTER_REC ");
            stb.append(                 "ELSE NULL END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM2_TERM_REC IS NOT NULL THEN SEM2_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                      "VALUE(SEM2_REC_FLG,'0') = '0' THEN NULL ");
            stb.append(                 "WHEN SEM2_REC IS NOT NULL THEN SEM2_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '2' AND ");
            stb.append(                                     "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(    "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, W1.SUBCLASSCD, ");
            stb.append(            "NULLIF(W1.SCHREGNO,W2.SCHREGNO) AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN (SEM3_TERM_REC  IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
            stb.append(                      "VALUE(SEM3_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE3, ");
            stb.append(            "CASE WHEN W1.SCHREGNO = W2.SCHREGNO THEN NULL ELSE 0 END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM3_TERM_REC IS NOT NULL THEN SEM3_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN (SEM3_TERM_REC  IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
            stb.append(                      "VALUE(SEM3_REC_FLG,'0') = '0' THEN NULL ");
            stb.append(                 "WHEN SEM3_REC IS NOT NULL THEN SEM3_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "GRADE3_RELAASSESS_5STEP AS ASSESS ");
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '3' AND ");
            stb.append(                                     "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(    "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(    ")");

            //成績データの表 学期・成績種別で分解
            stb.append(",RECORD_DAT_B AS(");
            stb.append(    "SELECT  SEMESTER, 1 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, ");
            stb.append(            "KK_SCORE1 AS KK_SCORE, ");
            stb.append(            "SCORE1 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");
            //stb.append(      "WHERE   KK_SCORE1 IS NOT NULL OR SCORE1 IS NOT NULL ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER, 2 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, ");
            stb.append(            "KK_SCORE2 AS KK_SCORE, ");
            stb.append(            "SCORE2 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");
            //stb.append(      "WHERE   KK_SCORE2 IS NOT NULL OR SCORE2 IS NOT NULL ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER, 3 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, ");
            stb.append(            "KK_SCORE3 AS KK_SCORE, ");
            stb.append(            "SCORE3 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");
            //stb.append(      "WHERE   KK_SCORE3 IS NOT NULL OR SCORE3 IS NOT NULL ");
            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別合計
            stb.append(",SCH_SUM AS(");
            stb.append(    "SELECT  SCHREGNO, SEMESTER, KIND,'X5G' AS SUBCLASSCD, ");
            stb.append(            "SUM(SCORE) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '05' ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SCHREGNO,SEMESTER,KIND,'X9G' AS SUBCLASSCD, ");
            stb.append(            "SUM(SCORE) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別平均
            stb.append(",SCH_AVG AS(");
            stb.append(    "SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X5H' AS SUBCLASSCD, ");
            stb.append(            "CASE WHEN 0 < COUNT(W2.SCHREGNO) THEN NULL ");
            stb.append(                 "ELSE ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 END AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(            "LEFT JOIN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                      "FROM    RECORD_DAT_B W2 ");
            stb.append(                      "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                      "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                      "HAVING 0 < COUNT(*) ");
            stb.append(            ")W2 ON W1.SCHREGNO = W2.SCHREGNO AND W1.SEMESTER = W2.SEMESTER AND W1.KIND = W2.KIND ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '05' ");
/*
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
*/
            stb.append(    "GROUP BY W1.SCHREGNO, W1.SEMESTER, W1.KIND ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X9H' AS SUBCLASSCD, ");
            stb.append(            "CASE WHEN 0 < COUNT(W2.SCHREGNO) THEN NULL ");
            stb.append(                 "ELSE ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 END AS SCORE ");
            //stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(            "LEFT JOIN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                      "FROM    RECORD_DAT_B W2 ");
            stb.append(                      "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                      "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                      "HAVING 0 < COUNT(*) ");
            stb.append(            ")W2 ON W1.SCHREGNO = W2.SCHREGNO AND W1.SEMESTER = W2.SEMESTER AND W1.KIND = W2.KIND ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' ");
/*
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
*/
            stb.append(    "GROUP BY W1.SCHREGNO, W1.SEMESTER, W1.KIND ");
            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別席次
            stb.append(",SCH_RANK AS(");
            stb.append(    "SELECT  SCHREGNO,SEMESTER,KIND,'X5R' AS SUBCLASSCD, ");
            stb.append(         "   RANK() OVER(PARTITION BY SEMESTER,KIND ORDER BY AVG(FLOAT(SCORE)) DESC) AS RANK ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '05' AND ");
            stb.append(            "SCORE IS NOT NULL AND ");
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SCHREGNO,SEMESTER,KIND,'X9R' AS SUBCLASSCD, ");
            stb.append(            "RANK() OVER(PARTITION BY SEMESTER,KIND ORDER BY AVG(FLOAT(SCORE)) DESC) AS RANK ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' AND ");
            stb.append(            "SCORE IS NOT NULL AND ");
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(    ")");

            //成績データの表 学期別成績種別合計の学級平均
            stb.append(",HR_SUM AS(");
            stb.append(    "SELECT  SEMESTER,KIND,'X5G' AS SUBCLASSCD, ");
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    SCH_SUM ");
            stb.append(    "WHERE   SUBCLASSCD <= 'X5G' ");
            stb.append(    "GROUP BY SEMESTER,KIND ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER,KIND,'X9G' AS SUBCLASSCD, ");
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    SCH_SUM ");
            stb.append(    "WHERE   SUBCLASSCD <= 'X9G' ");
            stb.append(    "GROUP BY SEMESTER,KIND ");
            stb.append(    ")");

            //成績データの表 学期別成績種別平均の学級平均
            stb.append(",HR_AVERAGE AS(");
            stb.append(    "SELECT  SEMESTER,KIND,SUBCLASSCD, ");
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(    "GROUP BY SEMESTER,KIND,SUBCLASSCD ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER,KIND,'X5H' SUBCLASSCD, ");
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '05' AND ");
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
            stb.append(    "GROUP BY SEMESTER,KIND ");
            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER,KIND,'X9H' SUBCLASSCD, ");
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10 ,0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' AND ");
            stb.append(            "(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "FROM    RECORD_DAT_B W2 ");
            stb.append(                                            "WHERE   KK_SCORE IS NOT NULL ");
            stb.append(                                            "GROUP BY SCHREGNO,SEMESTER,KIND ");
            stb.append(                                            "HAVING 0 < COUNT(*) ) ");
            stb.append(    "GROUP BY SEMESTER,KIND ");
            stb.append(    ")");


            //メイン表 教科別学期別成績種別の成績（３学期は評定付き）
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "W1.KK_SCORE, W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "W4.ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    RECORD_DAT_B W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(        "LEFT JOIN RECORD_DAT_A W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND ");
            stb.append(                                     "W1.SUBCLASSCD = W4.SUBCLASSCD AND W4.SEMESTER = '3' ");
            stb.append("WHERE   W1.SCHREGNO IN " + param[5] + " and ");
            stb.append(        "w1.kk_score is not null or w1.score is not null ");

            //メイン表 教科別学期別成績種別の合計
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN HR_SUM W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X5G' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            //メイン表 ５教科学期別成績種別の平均
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_AVG W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X5H' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            //メイン表 ５教科学期別成績種別の席次
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.RANK AS SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE 0 END AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_RANK W1 ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X5R' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            //メイン表 ９教科学期別成績種別の合計
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN HR_SUM W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X9G' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            //メイン表 ９教科学期別成績種別の平均
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_AVG W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X9H' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            //メイン表 ９教科学期別成績種別の席次
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS KK_SCORE, ");
            stb.append(        "W1.RANK AS SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE 0 END AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
            stb.append("FROM    SCH_RANK W1 ");
            stb.append("WHERE   W1.SUBCLASSCD = 'X9R' AND ");
            stb.append(        "W1.SCHREGNO IN " + param[5] + " ");

            stb.append("ORDER BY ATTENDNO, SCHREGNO, SEMESTER, KIND, SUBCLASSCD");

        } catch( Exception ex ){
            log.error("[KNJD171K]prestatementSubclass error!",ex);
        }
        return stb.toString();

    }//prestatementSubclass()の括り


    /** 
     *  PrepareStatement作成 成績明細-->生徒別科目別 
     *    生徒別出欠データ作成
     *  2005/02/18 出欠集計データと出欠データを合わせて算出する
     *
     */
    String prestatementAttend(String param[])
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try {
            //対象生徒
            stb.append("WITH SCHNO AS(");
            stb.append(    "SELECT  T1.SCHREGNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(            "T1.YEAR = T2.YEAR AND ");
            stb.append(            "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(            "T1.GRADE||T1.HR_CLASS = '" + param[2] + "' AND ");
            stb.append(            "T1.SCHREGNO = ? AND ");
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                               "S1.GRD_DATE <= CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                                   "ELSE '" + param[7] + "' END ) AND ");
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param[7] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param[7] + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param[7] + "' END < S1.TRANSFER_SDATE)) )");
            stb.append(    ")");

/*
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.SCHREGNO, ");
            stb.append(             "CASE WHEN W2.SCHREGNO IS NOT NULL THEN 1 WHEN W3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(             "LEFT JOIN(");
            stb.append(                 "SELECT  SCHREGNO ");
            stb.append(                 "FROM    SCHREG_BASE_MST ");
            stb.append(                 "WHERE   GRD_DIV IN ('2','3') AND ");
            stb.append(                         "'" + param[0] + "-04-01" + "' <= GRD_DATE AND ");
            stb.append(                         "GRD_DATE <= '" + param[7] + "' ");
            stb.append(                 "GROUP BY SCHREGNO ");
            stb.append(             ") W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(             "LEFT JOIN(");
            stb.append(                 "SELECT  SCHREGNO ");
            stb.append(                 "FROM    SCHREG_TRANSFER_DAT ");
            stb.append(                 "WHERE   TRANSFERCD IN ('4') AND '" + param[7] + "' < TRANSFER_SDATE ");
            stb.append(                 "GROUP BY SCHREGNO ");
            stb.append(             ") W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "W1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(             "W1.GRADE||W1.HR_CLASS = '" + param[2] + "' AND ");
            stb.append(             "W1.SCHREGNO = ? ");
            stb.append(     ") ");
*/
            //対象生徒の時間割データ
            stb.append(",SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T1.SEMESTER BETWEEN ? AND ? AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param[9] + "' AND '" + param[7] + "' AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     ") ");

            //対象生徒の出欠データ
            stb.append(",T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T0.DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0, ");
            stb.append(             "SCHEDULE_SCHREG T1 ");
            stb.append(     "WHERE   T0.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param[9] + "' AND '" + param[7] + "' AND ");
            stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T0.PERIODCD = T1.PERIODCD ");
            stb.append(     ") ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(",T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     ") ");

            //異動者（退学・転学）不在日数を算出 05/02/02
            stb.append(",LEAVE_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO,COUNT(DISTINCT T1.EXECUTEDATE)AS LEAVE_DATE ");
            stb.append(     "FROM    SCHREG_BASE_MST T3, SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T1.SEMESTER BETWEEN ? AND ? AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param[0] + "-04-01" + "' AND '" + param[7] + "' AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append(             "T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T3.GRD_DIV IN('2','3') AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T3.GRD_DATE AND '" + param[7] + "' ");
            stb.append(     "GROUP BY T3.SCHREGNO ");
            stb.append(     ") ");

            //メイン表
            stb.append(   "SELECT  TT0.SCHREGNO, ");
            //stb.append(           "TT0.LEAVE, ");   // 05/01/31
            stb.append(           "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) AS LESSON, ");
                                    //出停・忌引日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT4.MOURNING,0) ");
            stb.append(           " + VALUE(TT7.SUSPEND,0) + VALUE(TT7.MOURNING,0) AS MOURNING_SUSPEND, ");
                                    //欠席日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
            stb.append(           "VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0) AS MOURNING, ");
            stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
            stb.append(           " + VALUE(TT7.ABSENT,0) AS ABSENT, ");
                                    //出席日数
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
            stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
            stb.append(           " - VALUE(TT9.LEAVE_DATE,0) ");
            stb.append(           " - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) ");
            stb.append(           " - VALUE(TT7.ABSENT,0) AS PRESENT, ");
            stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT7.LATE,0) AS LATE, ");
            stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY ");
            stb.append(   "FROM    SCHNO TT0 ");
            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('2','9') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('3','10') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0, ");
            stb.append(         "(");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(         "FROM    T_PERIOD_CNT T0, ");
            stb.append(            "(");
            stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    T_ATTEND_DAT W1 ");
            stb.append(            "WHERE   W1.DI_CD IN ('4','5','6','11','12','13') ");
            stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(            ") T1 ");
            stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ");
            stb.append(      "WHERE   W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.DI_CD NOT IN ('0','14') ");
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            //stb.append(         "WHERE   DI_CD IN ('4','5','6','15','11','12','13','3','10') ");
            stb.append(         "WHERE   DI_CD IN ('4','5','6','15','11','12','13') ");             // 05/02/25Modify
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            //stb.append(         "WHERE   DI_CD IN ('4','5','6','16','3','10') ");
            stb.append(         "WHERE   DI_CD IN ('4','5','6','16') ");                            // 05/02/25Modify
            stb.append(         ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ");
            stb.append(                                             "T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ");

            //月別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, ");
            stb.append(                   "SUM(LESSON) AS LESSON, ");
            stb.append(                   "SUM(MOURNING) AS MOURNING, ");
            stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
            stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ) AS ABSENT, ");
            stb.append(                   "SUM(LATE) AS LATE, ");
            stb.append(                   "SUM(EARLY) AS EARLY ");
            stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(            "WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(                   "SEMESTER BETWEEN ? AND ? AND ");
            stb.append(                   "SEMESTER||MONTH <= '" + param[10] + "' AND ");
            stb.append(                   "EXISTS(");
            stb.append(                       "SELECT  'X' ");
            stb.append(                       "FROM    SCHNO W2 ");
            stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(            "GROUP BY SCHREGNO ");
            stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            //異動者不在日数の表
            stb.append(   "LEFT JOIN LEAVE_SCHREG TT9 ON TT9.SCHREGNO=TT0.SCHREGNO ");

            stb.append("ORDER BY SCHREGNO");
        } catch( Exception ex ){
            log.warn("prestatementAttend error!",ex);
        }

        return stb.toString();

    }//prestatementAttend()の括り


    /**PrepareStatement close**/
    private void prestatementClose(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3,PreparedStatement ps4,PreparedStatement ps5,PreparedStatement ps6)
    {
        try {
            if( ps1!=null )ps1.close();
            if( ps2!=null )ps2.close();
            if( ps3!=null )ps3.close();
            if( ps4!=null )ps4.close();
            if( ps5!=null )ps5.close();
            if( ps6!=null )ps6.close();
        } catch( Exception ex ){
            log.error("[KNJD171K]prestatementClose error!",ex);
        }
    }//prestatementClose()の括り


    /** 
     *  PrepareStatement作成 
     *  委員会、クラブ活動
     */
    String prestatementCommitAndClub(String param[])
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try {
            stb.append("WITH COMMITTEE_DAT AS(");
            stb.append(    "SELECT  SCHREGNO, COMMITTEE_FLG, MAX(SEQ) AS SEQ ");
            stb.append(    "FROM    SCHREG_COMMITTEE_HIST_DAT ");
            stb.append(    "WHERE   YEAR='" + param[0] + "' AND ");
            stb.append(            "SCHREGNO = ? ");
            stb.append(    "GROUP BY SCHREGNO, COMMITTEE_FLG) ");
            
            stb.append(",CLUB_DAT AS(");
            stb.append(    "SELECT  SCHREGNO, CLUBCD, MAX(SDATE) AS SDATE ");
            stb.append(    "FROM    SCHREG_CLUB_HIST_DAT ");
            stb.append(    "WHERE   SCHREGNO =  ? ");
            stb.append(    "GROUP BY SCHREGNO, CLUBCD) ");
            
            stb.append("SELECT  T3.COMMITTEE_FLG AS FLG, ");
            stb.append(        "T4.COMMITTEENAME AS NAME ");
            stb.append("FROM    SCHREG_COMMITTEE_HIST_DAT T3 ");
            stb.append(        "INNER JOIN COMMITTEE_MST T4 ON T4.COMMITTEECD = T3.COMMITTEECD AND ");
            stb.append(                                       "T4.COMMITTEE_FLG = T3.COMMITTEE_FLG ");
            stb.append("WHERE   T3.YEAR =  '" + param[0] + "' AND ");
            stb.append(        "EXISTS(SELECT 'X' FROM COMMITTEE_DAT T1 WHERE T1.SEQ=T3.SEQ AND T1.SCHREGNO = T3.SCHREGNO AND ");
            stb.append(                                             "T1.COMMITTEE_FLG = T3.COMMITTEE_FLG) ");
            
            stb.append("UNION ");
            stb.append("SELECT  '3' AS FLG, CLUBNAME AS NAME ");
            stb.append("FROM    SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(        "INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");
            stb.append("WHERE   EXISTS(SELECT 'X' FROM CLUB_DAT T3 WHERE T1.SCHREGNO = T3.SCHREGNO) ");

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();

    }


    /** 
      *
      *  PrepareStatement作成 生徒別科目履修科目（履修科目か否か）
      *    2004/11/09
      *    2004/12/15 評価読み替え科目を追加 => １年国語総合の単位出力のため
      *
      **/
    String prestatementBeStudy(String param[])
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try {
            stb.append("WITH TSUBCLASS AS(");
            stb.append(    "SELECT SUBCLASSCD ");
            stb.append(    "FROM ");
            stb.append(          "(SELECT SCHREGNO,CHAIRCD,SEMESTER,APPDATE,APPENDDATE ");
            stb.append(           "FROM   CHAIR_STD_DAT S1 ");
            stb.append(           "WHERE  YEAR='"+param[0]+"' AND SCHREGNO = ? )W1,");
            stb.append(           "CHAIR_DAT W2 ");
            stb.append(    "WHERE  W2.YEAR = '" + param[0] + "' AND ");
            stb.append(           "W1.SEMESTER = W2.SEMESTER AND ");
            stb.append(           "W1.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCH_CHR_DAT S1 ");
            stb.append(                   "WHERE  S1.YEAR = '" + param[0] + "' AND ");
            stb.append(                          "S1.SEMESTER = W2.SEMESTER AND ");
            stb.append(                          "S1.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(                         "S1.EXECUTEDATE BETWEEN APPDATE AND APPENDDATE)");
            stb.append(    "GROUP BY SUBCLASSCD) ");

            stb.append("SELECT SUBCLASSCD ");
            stb.append("FROM   TSUBCLASS ");
            stb.append("UNION  SELECT DISTINCT GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("FROM   TSUBCLASS W1 ");
            stb.append("INNER JOIN SUBCLASS_REPLACE_DAT W2 ON W2.YEAR = '" + param[0] + "' AND ");
            stb.append(                                      "W2.ANNUAL = '" + param[2].substring(0, 2) + "' AND ");
            stb.append(                                      "W2.ATTEND_SUBCLASSCD = W1.SUBCLASSCD");

        } catch( Exception ex ){
            log.error("[KNJD171K]prestatementBeStudy error!",ex);
        }
        return stb.toString();

    }//prestatementBeStudy()の括り


    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    private void getParam( HttpServletRequest request, String param[] ){

        //param = new String[9];
        try {
            //printname = request.getParameter("PRINTNAME");                //プリンタ名
            schno = request.getParameterValues("category_selected");        //学籍番号
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("GAKKI");                       //1-3:学期
            param[2] = request.getParameter("GRADE_HR_CLASS");              //学年・組
            param[6] = request.getParameter("TESTKINDCD");                  //中間:01,期末:99 04/11/03Add
            param[7] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //異動基準日 04/11/30
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        }
for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("[KNJD171K]param[" + i + "]=" + param[i]);
    }


    /** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){

        try {
            outstrm = new PrintWriter (response.getOutputStream());
            if( printname!=null )   response.setContentType("text/html");
            else                    response.setContentType("application/pdf");

            int ret = svf.VrInit();                             //クラスの初期化

            if( printname!=null ){
                ret = svf.VrSetPrinter("", printname);          //プリンタ名の設定
                if( ret < 0 ) log.info("printname ret = " + ret);
            } else
                ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch( java.io.IOException ex ){
            log.error("db new error:" + ex);
        }
  }


    /** svf close */
    private void closeSvf(Vrw32alp svf){
        if( printname!=null ){
            outstrm.println("<HTML>");
            outstrm.println("<HEAD>");
            outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
            outstrm.println("</HEAD>");
            outstrm.println("<BODY>");
            if( !nonedata ) outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
            outstrm.println("</BODY>");
            outstrm.println("</HTML>");
        } else if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        int ret = svf.VrQuit();
        if( ret == 0 )log.info("===> VrQuit():" + ret);
        outstrm.close();            //ストリームを閉じる 
    }


    /** DB set */
    private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        } catch( Exception ex ){
            log.error("db new error:" + ex);
            if( db2 != null)db2.close();
        }
        return db2;
    }


    /** DB open */
    private boolean openDb(DB2UDB db2){
        try {
            db2.open();
        } catch( Exception ex ){
            log.error("db open error!"+ex );
            return true;
        }//try-cathの括り
        return false;
    }//private boolean Open_db()


    /** DB close */
    private void closeDb(DB2UDB db2){
        try {
            db2.commit();
            db2.close();
        } catch( Exception ex ){
            log.error("db close error!"+ex );
        }//try-cathの括り
    }//private Close_Db()



    /**
     *  印刷位置を設定する
     *
     */
    private static class ReturnVal{

        int point;
        String field;

        /**
         *  学期・成績種別から出力行を設定する
         *
         */
        void getSvfOutPoint( int semes, int kind )
        {
            if( semes == 1 )
                point = ( kind == 1 )? 1 : ( kind == 2 )? 3 : 5;
            else if( semes == 2 )
                point = ( kind == 1 )? 7 : ( kind == 2 )? 9 : 11;
            else
                point = ( kind == 2 )? 13 : 15;
        }

        /**
         *  教科コード等から出力フィールドを設定する
         *
         */
        void getSvfOutField( String subclasscd, int semes, int kind ){
            if( semes < 3 ){
                field = ( subclasscd.equals("X5G") )? "5TOTAL" :
                        ( subclasscd.equals("X5H") )? "5AVERAGE" :
                        ( subclasscd.equals("X5R") )? "5ORDER" :
                        ( subclasscd.equals("X9G") )? "9TOTAL" :
                        ( subclasscd.equals("X9H") )? "9AVERAGE" :
                        ( subclasscd.equals("X9R") )? "9ORDER" :
                        "SUBCLASS" + Integer.parseInt( subclasscd.substring(0,2) );
            }
        }
    }


}//クラスの括り

/**
 *
 *    学校教育システム 賢者 [成績管理]  成績通知票（大宮開成中学用）
 *
 *    2005/08/25 yamashiro
 *  2005/09/06 yamashiro 所見と備考出力を追加 => 暫定措置
 *  2005/10/05 yamashiro 編入のデータ仕様変更および在籍異動条件に転入学を追加
 *  2006/02/01 yamasihro 2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO004
 *  2006/02/21 yamashiro 所見のテーブル定義変更による修正 --NO005
 *  2006/03/03 yamashiro 所見のテーブル定義変更による修正 --NO006
 *                       --NO004修正時の不具合を修正 DB2の型変換の使用方法に間違いがあった --NO007
 *  2006/04/03 yamashiro 修了証の出力処理を追加 --NO008
 *  2006/04/10 yamashiro 修了証の日付を印刷指示画面より取得 --NO009
 *  2006/09/13 nakamoto  固定値"04"を"03"に変更した --NO010
 *  2007/09/14 nakamoto  NO011:前期の時点で3年生は学年評定も入力するので印刷を可能にする
 *                       NO012:3年生の数学欄は1つになります
 *                       NO013:KNJDefineCode#defineCode() ではなく、KNJDefineSchool の同メソッドを使うようにした
 *  2007/09/19 nakamoto  NO014:評定欄は　各教科ごとに１つの欄とする。物理、化学で試験を実施していても欄は1つになる。（数学の代数、幾何と同様）
 *                       NO015:特定教科は通知表に印刷しない。教科コード＝５０（選択）
 */

package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import java.util.Map;
import java.util.LinkedHashMap;

//import Param;

public class KNJD174 {

    private static final Log log = LogFactory.getLog(KNJD174.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private int gnum;    //ページ当たり出力行数のカウント
    
    private Param _param;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();     // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                // Databaseクラスを継承したクラス
        boolean nonedata = false;

        //Param obj = new Param();
        //obj.svf_out(request,response);

        // print svf設定
        sd.setSvfInit( request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
        // パラメータの取得
        _param = createParam(request, db2);
        // 印刷処理
        if( ( request.getParameter("OUTPUT") ).equals("1") )nonedata = printSvf( request, db2, svf, _param );
        else nonedata = printSvfCertificate( request, db2, svf, _param );  //NO008
        //NO008 nonedata = printSvf( request, db2, svf, param );
        // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb(db2);
    }


    /**
     *  印刷処理
     */
    private boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, final Param param )
    {
        boolean nonedata = false;
        try {
            if( printSvfMain( db2, svf) )nonedata = true;        //SVF-FORM出力処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }

    /** 
     *
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMain( DB2UDB db2, Vrw32alp svf) 
    {
        //定義
        boolean nonedata = false;
        PreparedStatement ps1 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        ResultSet rs1 = null;
        ResultSet rs3 = null;

        //SQL作成
        try {
            ps1 = db2.prepareStatement( prestatementRegd( _param ) );        //学籍データ
            ps3 = db2.prepareStatement( prestatementSubclass( _param ) );    //成績明細データ
            ps4 = db2.prepareStatement( prestatementAttend( _param ) );        //出欠データ
        } catch( Exception ex ) {
            log.error("boolean printSvfMain prepareStatement error! ", ex);
            return nonedata;
        }
        //RecordSet作成
        try {
            rs1 = ps1.executeQuery();
            rs3 = ps3.executeQuery();
        } catch( SQLException ex ) {
            log.error("boolean printSvfMain executeQuery error! ", ex);
            return nonedata;
        }

        //データ読み込み＆ＳＶＦ出力
        try {
            int schno1 = 0;        //学籍の出席番号
            int schno3 = 0;        //成績明細データの出席番号
            int mcnt = 0;
            while( rs1.next() ){
                svf.VrSetForm("KNJD174.frm", 4);    //SVF-FORM 05/09/06Modify
                gnum = 0;
                schno1 = rs1.getInt("ATTENDNO");                    //学籍データの出席番号を保存
                printSvfRegdOut( db2, svf, rs1, _param );            //学籍データ出力
                //出欠データをセット
                printSvfAttend( svf, ps4, rs1.getString("SCHREGNO"), Integer.parseInt( _param._gakki ) );

                //成績明細データをセット
                mcnt = 0;
                for( int i = 0 ; -1 < schno3  &&  schno3 <= schno1 ; i++ ){
                    if( schno3 == schno1 ){
                        if( rs3.getInt("SUB_CNT") == 2 )mcnt++; // NO014
                        printSvfRecDetailOut( svf, rs3, _param, mcnt );
                        if( mcnt == 2 )mcnt = 0;
                    }
                    if( rs3.next() ) schno3 = rs3.getInt("ATTENDNO");
                    else             schno3 = -1;
                }
                if( 0 < mcnt ){
                    svf.VrEndRecord();
                    gnum += 2;
                }
                printSvfBlankOut( svf );
                if( ! nonedata )nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(rs3);
        }
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps3);
        DbUtils.closeQuietly(ps4);

        return nonedata;

    }//boolean printSvfMain()の括り



    /** 
     *
     *  SVF-OUT 生徒のデータ等を印刷する処理
     *             学籍データ、総合的な学習の時間、通信欄、学期名称
     *
     *  2005/08/17 Modify あて先出力の有無を追加
     */
    private void printSvfRegdOut(DB2UDB db2, Vrw32alp svf, ResultSet rs, final Param param ){
        final KNJObjectAbs knjobj = new KNJEditString(); //編集用クラス
        try {
            svf.VrsOut("NENDO",        param._nendo);                            //年度
            svf.VrsOut("SCHOOLNAME",   param._schoolname);                        //学校名
            svf.VrsOut("STAFFNAME1",   param._principalname);                        //校長名
            svf.VrsOut("STAFFNAME2",   param._staffname);                        //担任名
            svf.VrsOut("SCHOOLSTAMP",  param._stampfilepath );                        //学校印

            svf.VrsOut("HR_NAME",      rs.getString("HR_NAME"));    //組名称
            svf.VrsOut("ATTENDNO",     String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) );            //出席番号
            svf.VrsOut("NAME",         rs.getString("NAME"));                //生徒氏名

            //05/09/06Modify 所見・備考は暫定措置として前期のみ対応
            ArrayList arrlist = knjobj.retDividString( rs.getString("COMMUNICATION1"), 42, 2 );
            if( arrlist != null )
                for( int i = 0 ; i < arrlist.size() ; i++ )
                    svf.VrsOut("VIEW1_"+ ( i+1 ),  (String)arrlist.get(i) );        //所見 

            
            //委員会情報取得
            final String commSql = getCommittee(rs.getString("SCHOOL_KIND"), rs.getString("SCHREGNO"), rs.getString("GRADE"));
            PreparedStatement commPs = db2.prepareStatement(commSql);            
            ResultSet commRs = commPs.executeQuery();
            String comm1 = ""; //前期委員会
            String comm2 = ""; //後期委員会
            String sep1 = "";
            String sep2 = "";
            while (commRs.next()) {
            	String semester = commRs.getString("SEMESTER");
            	String commitName = StringUtils.defaultString(commRs.getString("COMMITTEENAME"));
            	if ("1".equals(semester)) {
            		comm1 += sep1 + commitName;
            		sep1 = ",";
            	} else if("2".equals(semester)) {
            		comm2 += sep2 + commitName;
            		sep2 = ",";
            	} else if ("9".equals(semester)) {
            		comm1 += sep1 + commitName;
            		comm2 += sep2 + commitName;
            		sep1 = ",";
            		sep2 = ",";

            	}
            }
            
            //部活動情報取得
            final String clubSql = getClub(rs.getString("SCHOOL_KIND"), rs.getString("SCHREGNO"));
            PreparedStatement clubPs = db2.prepareStatement(clubSql);            
            ResultSet clubRs = clubPs.executeQuery();
            Map clubMap1 = new LinkedHashMap(); //前期部活
            Map clubMap2 = new LinkedHashMap(); //後期部活
            while (clubRs.next()) {
            	String semester = clubRs.getString("CLUB_SEMESTER");
            	String clubName = StringUtils.defaultString(clubRs.getString("CLUBNAME"));
            	String clubCd   = StringUtils.defaultString(clubRs.getString("CLUBCD"));

            	if ("1".equals(semester)) {
            		clubMap1.put(clubCd, clubName);
            	} else if("2".equals(semester)) {
            		clubMap2.put(clubCd, clubName);
            	} else if ("9".equals(semester)) {
               		clubMap1.put(clubCd, clubName);
            		clubMap2.put(clubCd, clubName);
            	}
            }

    		svf.VrsOut("COMM_CLUB1_1",  comm1 );      //委員会
    		svf.VrsOut("COMM_CLUB1_2",  convMapToString(clubMap1) );      //部活動
    		
            arrlist = knjobj.retDividString( rs.getString("TOTALSTUDYTIME1"), 42, 2 );
            if( arrlist != null )
                for( int i = 0 ; i < arrlist.size() ; i++ )
                    svf.VrsOut("SPECIALACT1_"+ ( i+1 ),  (String)arrlist.get(i) );  //特別活動

            //NO005 所見・備考の後期を追加
            if( param._gakki.equals("2") ){
                arrlist = knjobj.retDividString( rs.getString("COMMUNICATION2"), 42, 2 );
                if( arrlist != null  &&  0 < arrlist.size() )
                    for( int i = 0 ; i < arrlist.size() ; i++ )
                        svf.VrsOut("VIEW2_"+ ( i+1 ),  (String)arrlist.get(i) );        //所見 

        		svf.VrsOut("COMM_CLUB2_1",  comm2 );          						//委員会
        		svf.VrsOut("COMM_CLUB2_2",  convMapToString(clubMap2) );          //部活動

                arrlist = knjobj.retDividString( rs.getString("TOTALSTUDYTIME2"), 42, 2 );
                if( arrlist != null  &&  0 < arrlist.size() )
                    for( int i = 0 ; i < arrlist.size() ; i++ )
                        svf.VrsOut("SPECIALACT2_"+ ( i+1 ),  (String)arrlist.get(i) );  //特別活動
            }

            //NO006 総合的な学習の時間の記録
            arrlist = knjobj.retDividString( rs.getString("TOTALSTUDYTIME"), 42, 2 );
            if( arrlist != null )
                for( int i = 0 ; i < arrlist.size() ; i++ )
                    svf.VrsOut("TOTALSTUDY"+ ( i+1 ),  (String)arrlist.get(i) );  //総合的な学習の時間の記録

        } catch( SQLException ex ){
            log.error("printSvfRegdOut error! ", ex);
        }

    }//printSvfRegdOut()の括り

    private String convMapToString (Map<String,String> map) {
    	
    	String sep = "";
    	String rtnStr = "";
    	for (String tmp : map.values()) {
    		rtnStr += sep + tmp;
    		sep = ",";
    	}
    	
    	return rtnStr;
    }
    
    /** 
     *
     * SVF-OUT 成績明細印刷
     */
    private void printSvfRecDetailOut( Vrw32alp svf, ResultSet rs, final Param param, int mcnt )
    {
        try {
            if( rs.getInt("SUB_CNT") != 2 ){ // NO014
                printSvfRecDetailOutSub1( svf, rs, param );
                svf.VrEndRecord();
                gnum++;
            }else{
                printSvfRecDetailOutSub2( svf, rs, param, mcnt );
                if( mcnt == 2 ){
                    svf.VrEndRecord();
                    gnum += 2;
                }
            }
        } catch( SQLException ex ){    log.error("error! ", ex ); }
    }


    /** 
     *
     * SVF-OUT 成績明細印刷　数学以外
     */
    private void printSvfRecDetailOutSub1( Vrw32alp svf, ResultSet rs, final Param param )
    {
        try {
            svf.VrsOut( "CLASS1",         rs.getString("CLASSNAME")      );    //教科名称
            svf.VrsOut( "SUBCLASS1",      rs.getString("SUBCLASSNAME")   );    //科目名称

            svf.VrsOut( "SCORE1_1_1",     rs.getString("SEM1_INTR_SCORE"));    //前期中間素点
            svf.VrsOut( "SCORE1_1_2",     rs.getString("SEM1_TERM_SCORE"));    //前期期末素点
            svf.VrsOut( "VALUE1_1",       rs.getString("SEM1_VALUE")     );    //前期評価

            if( 1 < Integer.parseInt(param._gakki) ){
                svf.VrsOut( "SCORE1_2_1",     rs.getString("SEM2_INTR_SCORE"));    //後期中間素点
                svf.VrsOut( "SCORE1_2_2",     rs.getString("SEM2_TERM_SCORE"));    //後期期末素点
                svf.VrsOut( "VALUE1_2",       rs.getString("SEM2_VALUE")     );    //後期評価
//                svf.VrsOut( "VALUE1_3",       rs.getString("GRAD_VALUE")     );    //学年評定 //NO011
            }
            //NO011-->
            if( 1 < Integer.parseInt(param._gakki) || param._gradeHrClass.substring( 0, 2 ).equals("03") ){
                svf.VrsOut( "VALUE1_3",       rs.getString("GRAD_VALUE")     );    //学年評定
            }
            //NO011<--
        } catch( Exception ex ){ log.error("error! ", ex ); }
    }


    /** 
     *
     * SVF-OUT 成績明細印刷　数学
     */
    private void printSvfRecDetailOutSub2( Vrw32alp svf, ResultSet rs, final Param param, int i )
    {
        try {
            svf.VrsOut( "CLASS2",         rs.getString("CLASSNAME")   );    //教科名称
            svf.VrsOut( "SUBCLASS2_" + i, rs.getString("SUBCLASSNAME"));    //科目名称

            svf.VrsOut( "SCORE2_1_" + (( i == 1 )? 1: 3),  rs.getString("SEM1_INTR_SCORE"));    //前期中間素点
            svf.VrsOut( "SCORE2_1_" + (( i == 1 )? 2: 4),  rs.getString("SEM1_TERM_SCORE"));    //前期期末素点
            svf.VrsOut( "VALUE2_1_" + i,  rs.getString("SEM1_VALUE"));    //前期評価

            if( 1 < Integer.parseInt(param._gakki) ){
                svf.VrsOut( "SCORE2_2_" + (( i == 1 )? 1: 3),  rs.getString("SEM2_INTR_SCORE"));    //後期中間素点
                svf.VrsOut( "SCORE2_2_" + (( i == 1 )? 2: 4),  rs.getString("SEM2_TERM_SCORE"));    //後期期末素点
                svf.VrsOut( "VALUE2_2_" + i,  rs.getString("SEM2_VALUE"));    //後期評価
            }
            //NO011-->
            if( 1 < Integer.parseInt(param._gakki) || param._gradeHrClass.substring( 0, 2 ).equals("03") ){
                svf.VrsOut( "VALUE2_3",       rs.getString("GRAD_VALUE"));    //学年評定
            }
            //NO011<--
        } catch( Exception ex ){ log.error("error! ", ex ); }
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷処理 
     */
    private void printSvfAttend( Vrw32alp svf, PreparedStatement ps4, String schregno, int sem )
    {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps4.setString( ++pp, schregno );                    //生徒番号
            ps4.setString( ++pp, schregno );                    //生徒番号
            //log.debug("ps4="+ps4.toString());
            rs = ps4.executeQuery();

            while ( rs.next() )    printSvfAttendOut( svf, rs );

        } catch( Exception ex ){
            log.error("printSvfAttendOut error!", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷処理 
     */
    private void printSvfBlankOut( Vrw32alp svf )
    {
        try {
            for( int i = 0 ; i < 15 - ( gnum % 15 ) ; i++ ){
                svf.VrAttribute( "CLASS1", "Meido=100" );
                svf.VrsOut( "CLASS1",  String.valueOf(i)   );    //教科名称
                svf.VrEndRecord();
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷
     */
    private void printSvfAttendOut( Vrw32alp svf, ResultSet rs )
    {
        try {
            int i = Integer.parseInt( rs.getString("SEMESTER") );
            if( i == 9 )i = _param.definecode.semesdiv + 1;

            if( 0 <= Integer.parseInt(rs.getString("LESSON")) )
                svf.VrsOutn("LESSON",     i,   rs.getString("LESSON") );              //授業日数
            svf.VrsOutn("SUSPEND",    i,   rs.getString("MOURNING_SUSPEND") );    //出停・忌引日数
            svf.VrsOutn("PRESENT",    i,   rs.getString("MLESSON") );                //出席しなければならない日数
            svf.VrsOutn("ABSENT",     i,   rs.getString("ABSENT") );                //欠席日数
            svf.VrsOutn("ATTEND",     i,   rs.getString("PRESENT") );                //出席日数
            svf.VrsOutn("LATE",       i,   rs.getString("LATE") );                //遅刻回数
            svf.VrsOutn("LEAVE",      i,   rs.getString("EARLY") );                //早退回数
        } catch( Exception ex ){
            log.error("printSvfAttendOut error!",ex);
        }
    }


    /** 
     *  PrepareStatement作成  学籍
     *     対象生徒全ての表
     */
    private String prestatementRegd(final Param param)
    {
        final StringBuffer stb = new StringBuffer();

        try {
            //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append(       "SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(             "AND T1.SEMESTER = '"+param._gakki+"' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(           "AND T1.SCHREGNO IN" + param._schno + " ");
                                    //05/10/05Modify
                                    //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
                                    //転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
                                    //異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //04/11/09Modify
            stb.append(    ") ");
            /* ***
            stb.append(           "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE  S1.SCHREGNO = T1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                              "S1.GRD_DATE <= CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                                  "ELSE '" + param._date + "' END ) AND ");
            stb.append(           "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE  S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                              "((S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                                "CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                     "ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");   // 04/11/09Modify
            stb.append(                               "(S1.TRANSFERCD IN ('4') AND ");
            stb.append(                                "CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                     "ELSE '" + param._date + "' END < S1.TRANSFER_SDATE)) )) ");          // 04/11/09Modify
            *** */

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.GRADE, T2.HR_NAME, T3.SCHOOL_KIND, ");
            stb.append(        "T5.NAME, ");
            stb.append(        "T7.SPECIALACTREMARK AS TOTALSTUDYTIME1, T7.COMMUNICATION AS COMMUNICATION1 ");  //--NO005 NO006
            stb.append(       ",T8.SPECIALACTREMARK AS TOTALSTUDYTIME2, T8.COMMUNICATION AS COMMUNICATION2 ");  //--NO005 NO006
            stb.append(       ",T9.TOTALSTUDYTIME AS TOTALSTUDYTIME ");  //NO006
            //NO006 stb.append(        "T7.TOTALSTUDYTIME AS TOTALSTUDYTIME1, T7.COMMUNICATION AS COMMUNICATION1 ");  //--NO005
            //NO006 stb.append(       ",T8.TOTALSTUDYTIME AS TOTALSTUDYTIME2, T8.COMMUNICATION AS COMMUNICATION2 ");  //--NO005
            //NO005 stb.append(        "T7.TOTALSTUDYTIME, T7.COMMUNICATION ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append(                              "AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(                              "AND T2.GRADE || T2.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append("INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR  = T2.YEAR ");
            stb.append(                              "AND T3.GRADE = T2.GRADE ");
            stb.append("LEFT JOIN HREPORTREMARK_DAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SCHREGNO = T1.SCHREGNO AND T7.SEMESTER = '1' "); //--NO005
            stb.append("LEFT JOIN HREPORTREMARK_DAT T8 ON T8.YEAR = '" + param._year + "' AND T8.SCHREGNO = T1.SCHREGNO AND T8.SEMESTER = '2' "); //--NO005
            stb.append("LEFT JOIN HREPORTREMARK_DAT T9 ON T9.YEAR = '" + param._year + "' AND T9.SCHREGNO = T1.SCHREGNO AND T9.SEMESTER = '9' "); //--NO006
            //NO005 stb.append("LEFT JOIN HREPORTREMARK_DAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SCHREGNO = T1.SCHREGNO "); //05/09/06Modify
            stb.append("ORDER BY ATTENDNO");

        } catch( Exception ex ){
            log.error("prestatementRegd error!",ex);
        }

        return stb.toString();

    }//prestatementRegd()の括り

    /**
     * 委員会取得
     */
    private String getCommittee(final String schoolKind, final String schregno, final String grade) {

        final StringBuffer stb = new StringBuffer();

        stb.append("   SELECT ");
        stb.append("     T1.SCHOOLCD,T1.SCHOOL_KIND,T1.YEAR,T1.SEMESTER,T1.SCHREGNO,T1.GRADE,T1.COMMITTEECD,T2.COMMITTEENAME ");
        stb.append("   FROM ");
        stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("     INNER JOIN COMMITTEE_MST T2 ");
        stb.append("       ON T1.SCHOOLCD = T2.SCHOOLCD ");
        stb.append("       AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("       AND T1.COMMITTEECD = T2.COMMITTEECD ");
        stb.append("       AND T1.COMMITTEE_FLG = T2.COMMITTEE_FLG ");
        stb.append("   WHERE ");
        stb.append("     T1.SCHOOLCD = '"+ _param._schoolcd +"' ");
        stb.append("     AND T1.SCHOOL_KIND = '"+ schoolKind +"' ");
        stb.append("     AND T1.YEAR = '"+_param._year+"' ");

        stb.append("     AND T1.GRADE = '"+ grade +"' ");
        stb.append("     AND T1.SCHREGNO = '"+ schregno +"' ");
        stb.append("     AND T1.COMMITTEE_FLG IN (1,2) ");
        stb.append("     AND T2.COMMITTEENAME IS NOT NULL ");
        stb.append("   GROUP BY ");
        stb.append("   	T1.SCHOOLCD,T1.SCHOOL_KIND,T1.YEAR,T1.SEMESTER,T1.SCHREGNO,T1.GRADE,T1.COMMITTEECD,T2.COMMITTEENAME ");
        stb.append("   ORDER BY ");
        stb.append("   	T1.SEMESTER, T1.COMMITTEECD ");

    	return stb.toString();
    }
    
    /**
     * 部活動取得
     */
    
    private String getClub(final String schoolKind, final String schno) {
    	
        final StringBuffer stb = new StringBuffer();

        stb.append("   SELECT ");
        stb.append("     T0.CLUBCD ");
        stb.append("     , CASE ");
        stb.append("       WHEN T0.EDATE IS NULL ");
        stb.append("       THEN '9' ");
        stb.append("       WHEN (T1.SDATE <= T0.SDATE AND T0.SDATE <= T1.EDATE) ");
        stb.append("       AND (T2.SDATE <= T0.EDATE AND T0.EDATE <= T2.EDATE) ");
        stb.append("       THEN '9' ");
        stb.append("       WHEN (T1.SDATE <= T0.SDATE AND T0.SDATE <= T1.EDATE) ");
        stb.append("       AND (T1.SDATE <= T0.EDATE AND T0.EDATE <= T1.EDATE) ");
        stb.append("       THEN '1' ");
        stb.append("       WHEN (T2.SDATE <= T0.SDATE AND T0.SDATE <= T2.EDATE) ");
        stb.append("       AND (T2.SDATE <= T0.EDATE AND T0.EDATE <= T2.EDATE) ");
        stb.append("       THEN '2' ");
        stb.append("   	ELSE '0' ");
        stb.append("       END AS CLUB_SEMESTER ");
        stb.append("     , CLUBM.CLUBNAME ");
        stb.append("     , T0.SDATE ");
        stb.append("     , T0.EDATE ");
        stb.append("     , T1.SDATE ");
        stb.append("     , T1.EDATE ");
        stb.append("     , T2.SDATE ");
        stb.append("     , T2.EDATE ");
        stb.append("   FROM ");
        stb.append("     SCHREG_CLUB_HIST_DAT AS T0 ");
        stb.append("     INNER JOIN SEMESTER_MST T1 ");
        stb.append("       ON T1.YEAR = '"+ _param._year +"' ");
        stb.append("       AND T1.SEMESTER = '1' ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ");
        stb.append("       ON T2.YEAR = '"+ _param._year +"' ");
        stb.append("       AND T2.SEMESTER = '2' ");
        stb.append("     LEFT JOIN CLUB_MST CLUBM ");
        stb.append("       ON T0.SCHOOLCD = CLUBM.SCHOOLCD ");
        stb.append("       AND T0.SCHOOL_KIND = CLUBM.SCHOOL_KIND ");
        stb.append("       AND T0.CLUBCD = CLUBM.CLUBCD ");
        stb.append("   WHERE ");
        stb.append("     T0.SCHOOLCD = '"+ _param._schoolcd +"' ");
        stb.append("     AND T0.SCHOOL_KIND = '"+ schoolKind +"' ");
        stb.append("     AND T0.SCHREGNO = '"+ schno +"' ");
        stb.append("     AND CLUBNAME IS NOT NULL ");
        stb.append("   ORDER BY ");
        stb.append("    T0.SDATE, ");
        stb.append("      T0.CLUBCD ");
log.fatal("("+schno+")"+"(club sql)");
    	return stb.toString();
    }
    
    /** 
     *  PrepareStatement作成 成績
     *     対象生徒全てのデータ表
     */
    private String prestatementSubclass(final Param param){

        final StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
            stb.append(            "T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(    "WHERE   T2.YEAR = '" + param._year + "' AND ");
            stb.append(            "T2.GRADE = '" + param._gradeHrClass.substring( 0, 2 ) + "' AND ");
            stb.append(            "T2.HR_CLASS = '" + param._gradeHrClass.substring( 2 ) + "' AND ");
            stb.append(            "T2.SCHREGNO IN " + param._schno + " AND ");
            stb.append(            "T2.SEMESTER = (SELECT  MAX(SEMESTER) ");
            stb.append(                           "FROM    SCHREG_REGD_DAT W2 ");
            stb.append(                                "WHERE   W2.YEAR = '" + param._year + "' AND ");
            stb.append(                                      "W2.SEMESTER <= '" + param._gakki + "' AND ");
            stb.append(                                      "W2.SCHREGNO = T2.SCHREGNO) ");
            stb.append(        ") ");

            //講座の表
            stb.append(", CHAIR_A AS(");
            stb.append(    "SELECT  S1.SCHREGNO,S2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " , S2.CLASSCD ");
                stb.append(    " , S2.SCHOOL_KIND ");
                stb.append(    " , S2.CURRICULUM_CD ");
            }
            stb.append(    "FROM    CHAIR_STD_DAT S1, ");
            stb.append(            "CHAIR_DAT S2 ");
            stb.append(    "WHERE   S1.YEAR = '" + param._year + "' ");
            stb.append(           "AND S1.SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND S2.YEAR = S1.YEAR ");
            stb.append(        "AND S2.SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND S2.SEMESTER = S1.SEMESTER ");
            stb.append(        "AND S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(        "AND EXISTS( SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO GROUP BY SCHREGNO ) ");
            stb.append(        "AND SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            stb.append(        "AND SUBCLASSCD NOT LIKE '50%' ");//NO015
            stb.append(    "GROUP BY S1.SCHREGNO,S2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " , S2.CLASSCD ");
                stb.append(    " , S2.SCHOOL_KIND ");
                stb.append(    " , S2.CURRICULUM_CD ");
            }
            stb.append(        ") ");

            //成績明細データの表
            stb.append(",RECORD AS(");
            stb.append(       "SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " CLASSCD, ");
                stb.append(    " SCHOOL_KIND, ");
                stb.append(    " CURRICULUM_CD, ");
            }
            stb.append(            "SUBCLASSCD, ");
            stb.append(            "CHAIRCD, ");
            stb.append(               "SEM1_INTR_SCORE, ");
            stb.append(               "SEM1_TERM_SCORE, ");
            stb.append(               "SEM1_VALUE, ");
            stb.append(               "SEM2_INTR_SCORE, ");
            stb.append(               "SEM2_TERM_SCORE, ");
            stb.append(               "SEM2_VALUE, ");
            stb.append(            "GRAD_VALUE ");
            stb.append(       "FROM    RECORD_DAT T1 ");
            stb.append(       "WHERE   YEAR = '" + param._year + "' AND ");
            stb.append(            "EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(                   "GROUP BY SCHREGNO) ");
            stb.append(        "AND SUBCLASSCD NOT LIKE '50%' ");//NO015
            stb.append(       ") ");

            //メイン表1
            stb.append(",T_MAIN AS(");
            stb.append(    "SELECT   T2.ATTENDNO ");
            stb.append(            ",T2.SCHREGNO ");
            stb.append(            ",T3.CLASSCD, T3.CLASSNAME ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " ,T4.SCHOOL_KIND ");
                stb.append(    " ,T4.CURRICULUM_CD ");
            }
            stb.append(            ",T4.SUBCLASSCD, T4.SUBCLASSNAME ");
            stb.append(            ",SEM1_INTR_SCORE ");
            stb.append(            ",SEM1_TERM_SCORE ");
            stb.append(            ",SEM1_VALUE ");
            stb.append(            ",SEM2_INTR_SCORE ");
            stb.append(            ",SEM2_TERM_SCORE ");
            stb.append(            ",SEM2_VALUE ");
            stb.append(            ",GRAD_VALUE ");
            stb.append(    "FROM   SCHNO T2 ");
            stb.append(    "LEFT JOIN RECORD T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(    "INNER JOIN CHAIR_A T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " AND T5.CLASSCD = T1.CLASSCD ");
                stb.append(    " AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(    " AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(    "INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(    "INNER JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(    " AND T4.CLASSCD = T1.CLASSCD ");
                stb.append(    " AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(    " AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(    ") ");
            //教科毎の科目数
            stb.append(",T_MAIN_CNT AS(");
            stb.append(    "SELECT   T1.SCHREGNO ");
            stb.append(            ",T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ",T1.SCHOOL_KIND ");
                stb.append(            ",T1.CURRICULUM_CD ");
            }
            stb.append(            ",COUNT(*) AS SUB_CNT ");
            stb.append(    "FROM   T_MAIN T1 ");
            stb.append(    "GROUP BY T1.SCHREGNO,T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ",T1.SCHOOL_KIND ");
                stb.append(            ",T1.CURRICULUM_CD ");
            }
            stb.append(    ") ");

            //メイン表2
            stb.append("SELECT   T1.SCHREGNO ");
            stb.append(        ",T1.ATTENDNO ");
            stb.append(        ",T1.CLASSCD, T1.CLASSNAME ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ",T1.SCHOOL_KIND ");
                stb.append(            ",T1.CURRICULUM_CD ");
            }
            stb.append(        ",T1.SUBCLASSCD, T1.SUBCLASSNAME ");
            stb.append(        ",T2.SUB_CNT ");
            stb.append(        ",T1.SEM1_INTR_SCORE ");
            stb.append(        ",T1.SEM1_TERM_SCORE ");
            stb.append(        ",T1.SEM1_VALUE ");
            stb.append(        ",T1.SEM2_INTR_SCORE ");
            stb.append(        ",T1.SEM2_TERM_SCORE ");
            stb.append(        ",T1.SEM2_VALUE ");
            stb.append(        ",T1.GRAD_VALUE ");
            stb.append("FROM   T_MAIN T1 ");
            stb.append("LEFT JOIN T_MAIN_CNT T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(            " AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }

            stb.append("ORDER BY T1.ATTENDNO, T1.SUBCLASSCD "); // NO010 T1.SUBCLASSCD → T4.SUBCLASSCD
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ",T1.SCHOOL_KIND ");
                stb.append(            ",T1.CURRICULUM_CD ");
            }

            //NO014<---
            log.debug("ps="+stb.toString());
        } catch( Exception ex ){
            log.error("prestatementSubclass error!",ex);
        }
        return stb.toString();

    }//prestatementSubclass()の括り


    /** 
     *  PrepareStatement作成 生徒別出欠データ作成
     *     任意の生徒の学期別（合計を含める）出欠データ表
     *
     */
    private String prestatementAttend(final Param param)
    {
        final StringBuffer stb = new StringBuffer();

        try {
            //対象生徒
            stb.append("WITH SCHNO (SCHREGNO, SEMESTER) AS(");
            stb.append(       "SELECT  T1.SCHREGNO, T1.SEMESTER ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(           "AND T1.SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "AND T1.SCHREGNO = ? ");
            /* *** 05/10/05Delete
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                               "S1.GRD_DATE <= CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                                   "ELSE '" + param._date + "' END ) AND ");
            stb.append(            "NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND ");
            stb.append(                                 "CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ");
            stb.append(                                      "ELSE '" + param._date + "' END < S1.TRANSFER_SDATE)) )");
            *** */
            stb.append(       "GROUP BY SCHREGNO,T1.SEMESTER ");
            stb.append(       "UNION ");
            stb.append(       "VALUES( cast(? as varchar(8) ), '9') ");
            stb.append(       ")");

            //対象生徒の時間割データ 05/08/17Modify １日出欠集計対象校時を条件に追加
            stb.append(",SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ");
            stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(         "AND T1.SEMESTER <= '" + param._gakki + "' ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(         "AND T1.YEAR = T2.YEAR ");
            stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(         "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            if( param.definecode.usefromtoperiod )
                stb.append(     "AND T1.PERIODCD IN " + param._periodcd + " ");            //05/08/17Build
            // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' ");
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(     "GROUP BY T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ");
            stb.append(     ") ");

            //対象生徒の出欠データ
            stb.append(",T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.ATTENDDATE, T0.PERIODCD, ATDD.REP_DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append(             "INNER JOIN SCHEDULE_SCHREG T1 ON ");
            stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T0.PERIODCD = T1.PERIODCD ");
            stb.append("        LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T0.DI_CD ");
            stb.append(     "WHERE   T0.YEAR = '" + param._year + "' AND ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(     ") ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(",T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE ");
            stb.append(     ") ");

            //異動者（退学・転学）不在日数を算出 05/02/02
            stb.append(",LEAVE_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, VALUE(T1.SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT T1.EXECUTEDATE)AS LEAVE_DATE ");
            stb.append(     "FROM    SCHREG_BASE_MST T3, SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param._year + "' AND ");
            stb.append(             "T1.SEMESTER <= '" + param._gakki + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "' AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append(             "T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T3.GRD_DIV IN('2','3') AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T3.GRD_DATE AND '" + param._date + "' ");
            stb.append(     "GROUP BY GROUPING SETS((T3.SCHREGNO,T1.SEMESTER),T3.SCHREGNO) ");
            stb.append(     ") ");

            //留学日数を算出
            stb.append(",TRANSFER_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, VALUE(T1.SEMESTER,'9')AS SEMESTER,COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ");
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param._year + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "' AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append(             "T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T3.TRANSFERCD IN('1') AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "GROUP BY GROUPING SETS((T3.SCHREGNO,T1.SEMESTER),T3.SCHREGNO) ");
            stb.append(     ") ");

            //メイン表
            stb.append(   "SELECT  TT0.SCHREGNO, TT0.SEMESTER, ");
            //stb.append(           "TT0.LEAVE, ");   // 05/01/31
            stb.append(           "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) AS LESSON, ");
                                    //出席すべき日数
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT3_2.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT3_3.KOUDOME,0) ");
            }
            stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT7.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT7.KOUDOME,0) ");
            }
            stb.append(           " - VALUE(TT9.LEAVE_DATE,0) ");
            stb.append(           " - VALUE(TT12.TRANSFER_DATE,0) AS MLESSON, ");
                                    //出停・忌引日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT4.MOURNING,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " + VALUE(TT3_2.VIRUS,0) + VALUE(TT7.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " + VALUE(TT3_3.KOUDOME,0) + VALUE(TT7.KOUDOME,0) ");
            }
            stb.append(           " + VALUE(TT7.SUSPEND,0) + VALUE(TT7.MOURNING,0) AS MOURNING_SUSPEND, ");
                                    //欠席日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " VALUE(TT3_2.VIRUS,0) + VALUE(TT7.VIRUS,0) AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " VALUE(TT3_3.KOUDOME,0) + VALUE(TT7.KOUDOME,0) AS KOUDOME, ");
            }
            stb.append(           "VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0) AS MOURNING, ");
            stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
            stb.append(           " + VALUE(TT7.ABSENT,0) AS ABSENT, ");
                                    //出席日数
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT3_2.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT3_3.KOUDOME,0) ");
            }
            stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT7.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT7.KOUDOME,0) ");
            }
            stb.append(           " - VALUE(TT9.LEAVE_DATE,0) ");
            stb.append(           " - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) ");
            stb.append(           " - VALUE(TT7.ABSENT,0) AS PRESENT, ");

            stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT6_2.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0) AS LATE, ");     //05/03/04Modify
            stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT6_2.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY, "); //05/03/04Modify

            stb.append(           "VALUE(TT12.TRANSFER_DATE,0) AS TRANSFER_DATE ");
            stb.append(   "FROM    SCHNO TT0 ");
            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO AND TT0.SEMESTER = TT1.SEMESTER ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('2','9') ");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO AND TT0.SEMESTER = TT3.SEMESTER ");
            //個人別出停伝染病日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('19','20') ");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO AND TT0.SEMESTER = TT3_2.SEMESTER ");
            //個人別出停交止日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('25','26') ");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ") TT3_3 ON TT0.SCHREGNO = TT3_3.SCHREGNO AND TT0.SEMESTER = TT3_3.SEMESTER ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('3','10') ");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO AND TT0.SEMESTER = TT4.SEMESTER ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W1.SCHREGNO, VALUE(W1.SEMESTER,'9')AS SEMESTER, ");
            stb.append(              "SUM(CASE ATDD.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE ATDD.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE ATDD.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0 ");
            stb.append(         " INNER JOIN (");
            stb.append(         "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(         "FROM    T_PERIOD_CNT T0 ");
            stb.append(            "INNER JOIN (");
            stb.append(            "SELECT  W1.SCHREGNO, SEMESTER, W1.ATTENDDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    T_ATTEND_DAT W1 ");
            stb.append(            "WHERE   W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
            stb.append(            "GROUP BY W1.SCHREGNO, SEMESTER, W1.ATTENDDATE ");
            stb.append(            ") T1 ON ");
            stb.append(                 "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ");
            stb.append(      "   ON   W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append("        LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = W0.DI_CD ");
            stb.append(      "GROUP BY GROUPING SETS((W1.SCHREGNO,W1.SEMESTER),W1.SCHREGNO) ");
            stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO AND TT0.SEMESTER = TT5.SEMESTER ");

            //個人別遅刻・早退回数 05/08/17Modify
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24') ");    //05/03/04Modify
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('4','5','6','11','12','13') ");         // 05/03/04Modify
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('4','5','6') ");                          // 05/03/03Modify
            stb.append(         ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ");
            stb.append(                                             "T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ");
            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO ");
            stb.append(            ", COUNT(CASE WHEN T2.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS LATE ");
            stb.append(            ", COUNT(CASE WHEN T3.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.REP_DI_CD IN ('4','5','6','11','12','13','29','30','31','32') ");
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(      "LEFT JOIN T_ATTEND_DAT T2 ON T0.SCHREGNO = T2.SCHREGNO ");
            stb.append(                                  " AND T0.EXECUTEDATE  = T2.ATTENDDATE ");
            stb.append(                                  " AND T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(                                  " AND T2.REP_DI_CD = '29' ");
            stb.append(      "LEFT JOIN T_ATTEND_DAT T3 ON T0.SCHREGNO = T3.SCHREGNO ");
            stb.append(                                  " AND T0.EXECUTEDATE  = T3.ATTENDDATE ");
            stb.append(                                  " AND T0.FIRST_PERIOD = T3.PERIODCD ");
            stb.append(                                  " AND T3.REP_DI_CD = '30' ");
            stb.append(      "LEFT OUTER JOIN (");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('31','32') ");
            stb.append(         "GROUP BY SCHREGNO ,ATTENDDATE ");
            stb.append(         ")T4 ON T0.SCHREGNO = T4.SCHREGNO AND T0.EXECUTEDATE  = T4.ATTENDDATE ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6_2 ON TT0.SCHREGNO = TT6_2.SCHREGNO ");

            //個人別遅刻回数  05/08/17Build
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('15','23','24') ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ");

            //個人別早退回数  05/08/17Build
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('16') ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT11 ON TT0.SCHREGNO = TT11.SCHREGNO ");

            //月別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ");
            stb.append(              "SUM(LESSON) AS LESSON, ");
            stb.append(              "SUM(MOURNING) AS MOURNING, ");
            stb.append(              "SUM(SUSPEND) AS SUSPEND, ");
            stb.append(              "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ) AS ABSENT, ");
            stb.append(              "SUM(LATE) AS LATE, ");
            stb.append(              "SUM(EARLY) AS EARLY ");
            if ("true".equals(param._useVirus)) {
                stb.append(              ", SUM(VIRUS) AS VIRUS ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(              ", SUM(KOUDOME) AS KOUDOME ");
            }
            stb.append(      "FROM    ATTEND_SEMES_DAT W1 ");
            stb.append(      "WHERE   YEAR = '" + param._year + "' AND ");
            stb.append(              "SEMESTER <= '" + param._gakki + "' AND ");
            stb.append(              "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (param._divideAttendMonth ) + "' AND ");   //--NO004 NO007
            //NO004 stb.append(              "SEMESTER||MONTH <= '" + param._divideAttendMonth + "' AND ");
            stb.append(              "EXISTS(SELECT  'X' ");
            stb.append(                     "FROM    SCHNO W2 ");
            stb.append(                     "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO AND TT0.SEMESTER = TT7.SEMESTER ");

            //異動者不在日数の表
            stb.append(   "LEFT JOIN LEAVE_SCHREG TT9 ON TT9.SCHREGNO = TT0.SCHREGNO AND TT0.SEMESTER = TT9.SEMESTER ");

            //留学中の授業日数の表
            stb.append(   "LEFT JOIN TRANSFER_SCHREG TT12 ON TT12.SCHREGNO = TT0.SCHREGNO AND TT0.SEMESTER = TT12.SEMESTER ");

            stb.append("ORDER BY SCHREGNO ,TT0.SEMESTER");
        } catch( Exception ex ){
            log.warn("prestatementAttend error!",ex);
        }

        return stb.toString();

    }//prestatementAttend()の括り


    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 71036 $ $Date: 2019-12-05 16:47:01 +0900 (木, 05 12 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }


    /**
     *  印刷処理 修了証 --NO008
     */
    private boolean printSvfCertificate( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, final Param param )
    {
        boolean nonedata = false;
        try {
            if( printSvfMainCertificate( db2, svf, param) )nonedata = true;  //SVF-FORM出力処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /** 
     *
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMainCertificate( DB2UDB db2, Vrw32alp svf, final Param param ) 
    {
        //定義
        boolean nonedata = false;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        //SQL作成
        try {
            ps1 = db2.prepareStatement( prestatementRegdCertificate( param ) );        //学籍データ
            rs1 = ps1.executeQuery();
        } catch( Exception ex ) {
            log.error("printSvfMainCertificate prepareStatement error! ", ex);
            return nonedata;
        }

        //データ読み込み＆ＳＶＦ出力
        try {
            while( rs1.next() ){
                svf.VrSetForm("KNJD174_2.frm", 1);
                printSvfRegdOutCertificate( db2, svf, rs1, param );            //学籍データ出力
                svf.VrEndPage();
                nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("printSvfMainCertificate read error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(ps1);
        }
        return nonedata;
    }
    /** 
     *
     *  SVF-OUT 生徒のデータ等を印刷する処理 修了証
     */
    private void printSvfRegdOutCertificate(DB2UDB db2, Vrw32alp svf, ResultSet rs, final Param param ){

        try {
            if( rs.getString("NAME") != null ) svf.VrsOut("NAME",     rs.getString("NAME"));  //生徒氏名
            if( rs.getString("BIRTHDAY") != null ) svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
            if( rs.getString("GRADE") != null ) svf.VrsOut("GRADE", String.valueOf( Integer.parseInt(rs.getString("GRADE") ) ) );  //学年

            if( param._date2 != null )  svf.VrsOut("DATE", KNJ_EditDate.h_format_JP( param._date2 ) );  //証明書日付 NO009
            //NO009 if( rs.getString("CERTIFICATEDATE") != null ) svf.VrsOut("DATE", KNJ_EditDate.h_format_JP( rs.getString("CERTIFICATEDATE") ) );  //証明書日付
        } catch( SQLException ex ){
            log.error("printSvfRegdOutCertificate error! ", ex);
        }

    }


    /** 
     *  PrepareStatement作成  学籍
     *     対象生徒全ての表
     */
    private String prestatementRegdCertificate(final Param param)
    {
        final StringBuffer stb = new StringBuffer();

        try {
            //証明書日付の取得
            stb.append("WITH CERTIFICATE_DATE AS(");
            stb.append(   "SELECT  EDATE FROM SEMESTER_MST ");
            stb.append(   "WHERE   YEAR = '" + param._year + "' ");
            stb.append(       "AND SEMESTER = '9' ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T3.NAME, T3.BIRTHDAY, T1.GRADE ");
            stb.append(       ",(SELECT EDATE FROM CERTIFICATE_DATE) AS CERTIFICATEDATE ");
            stb.append("FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2,SCHREG_BASE_MST T3 ");
            stb.append("WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(    "AND T1.SEMESTER = '"+param._gakki+"' ");
            stb.append(    "AND T1.YEAR = T2.YEAR ");
            stb.append(    "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(       "AND T1.SCHREGNO IN" + param._schno + " ");
                                    //05/10/05Modify
                                    //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
                                    //転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(                         "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
                                    //異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //04/11/09Modify
            stb.append(    "AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("ORDER BY ATTENDNO");

        } catch( Exception ex ){
            log.error("prestatementRegd error!",ex);
        }

        return stb.toString();
    }
    
    private static class Param {
        final String _year;
        final String _gakki;
        final String _gradeHrClass;
        final String _date;
        String _documentroot;
        final String _schno;
        String _divideAttendDate;
        String _divideAttendMonth;
        String _stampfilepath;
        String _nendo;
        String _schoolcd;
        String _schoolname;
        String _principalname;
        String _staffname;
        String _periodcd;
        final String _date2;
        final String _useCurriculumcd;

        final String _useVirus;
        final String _useKoudome;
        private KNJDefineSchool definecode;        //各学校における定数等設定//NO013 KNJDefineCode → KNJDefineSchool

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");                         //年度
            _gakki = request.getParameter("GAKKI");                       //1-3:学期
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");              //学年・組
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //異動基準日 04/11/30
            _documentroot = request.getParameter("DOCUMENTROOT");                 // '/usr/local/deve_oomiya/src'

            final String schno[] = request.getParameterValues("category_selected");        //学籍番号
            _schno = Set_Schno( schno );        //学籍番号の編集

            _schoolcd = request.getParameter("SCHOOLCD");
            
            _date2 = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE2") );    //修了日 NO009
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            setHead(db2);            //見出し項目
            getDivideAttendDate(db2);  //出欠用日付等取得 05/02/17
        }
        
        /**
         *  対象生徒学籍番号編集(SQL用) 
         */
        private String Set_Schno(String schno[]){

            final StringBuffer stb = new StringBuffer();

            for( int ia=0 ; ia<schno.length ; ia++ ){
                if( ia==0 )    stb.append("('");
                else        stb.append("','");
                stb.append(schno[ia]);
            }
            stb.append("')");

            return stb.toString();
        }
        
        /** 
         *  SVF-FORMセット＆見出し項目
         */
        private void setHead(final DB2UDB db2){

            //    出力項目
            _nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";    //年度

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //学校名、校長名を取得 2005/08/16Modify
            try {
                returnval = getinfo.getSchoolName( db2, _year );
                _schoolname = returnval.val1;                        //学校名
                _principalname = returnval.val2;                        //校長名
            } catch( Exception ex ){
                log.error("getinfo.Staff_name() error! ", ex);
            }

            //学級担任名を取得
            try {
                returnval = getinfo.Staff_name(db2,_year,_gakki,_gradeHrClass,"");
                _staffname = returnval.val1;                                                //学級担任名
            } catch( Exception ex ){
                log.error("getinfo.Staff_name() error! ", ex);
            }

            //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
            try {
                definecode = new KNJDefineSchool();//NO013 KNJDefineCode → KNJDefineSchool
                definecode.defineCode( db2, _year );         //各学校における定数等設定
                log.debug("schoolmark=" + definecode.schoolmark + " *** semesdiv=" + definecode.semesdiv + " *** absent_cov=" + definecode.absent_cov + " *** absent_cov_late=" + definecode.absent_cov_late);
            } catch( Exception ex ){
                log.warn("definecode.defineCode() error! ", ex);
            }

            //１日出欠集計対象校時を取得 2005/08/17Modify
            try {
                returnval = getinfo.getTargetPeriod( db2, _year, _gakki, _gradeHrClass, definecode.usefromtoperiod );
                _periodcd = returnval.val1;                        //１日出欠集計対象校時
            } catch( Exception ex ){
                log.error("getTargetPeriod() error! ", ex);
            }

            //学校印
            try {
                returnval = getinfo.Control( db2 );
                if( returnval.val4 != null  &&  _documentroot != null )
                    _stampfilepath = _documentroot + "/" + returnval.val4 + "/" + "SCHOOLSTAMP.bmp";

                File f2 = new File( _stampfilepath );
                if( ! f2.exists() )_stampfilepath = null;
            } catch( Exception e ){
                log.error("setHeader set error!");
            }

            //    ＳＶＦ属性変更--->改ページ
            //svf.VrAttribute("ATTENDNO","FF=1");                    //出席番号で改ページ

        }//setHead()の括り
        
        /**
         *  出欠集計テーブルをみる最終月と出欠データをみる開始日を取得する
         */
        private void getDivideAttendDate(final DB2UDB db2)
        {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            try {
                obj.getDivideAttendDate( db2, _year, _gakki, _date );
                _divideAttendDate = obj.date;
                _divideAttendMonth = obj.month;
            } catch( Exception ex ){
                log.error("error! ",ex);
            }
        }

    }
}

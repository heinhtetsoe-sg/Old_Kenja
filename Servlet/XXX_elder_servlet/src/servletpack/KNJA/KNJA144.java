// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2006/03/30 16:07:30 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2006-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４４＞  身分証明書（千代田・広島国際）
 *
 *	2006/03/30 nakamoto  作成日
 *  2006/03/31 yamashiro NO001 学校電話番号の出力を追加
 *	2006/04/13 nakamoto  NO002 条件を下記のように変更（写真の上に、高校は課程名称を、中学は「義務課程」と表示する。）
 *	                        ? 千代田と千代田以外の判断：名称マスタより判断する
 *	                        ? 中学と高校の判断：千代田以外の場合、学校マスタより判断する。千代田の場合、学年で判断する。
 *	                     NO003 タイトル名を名称マスタよりセットするように変更
 *	                     NO004 氏名の上は、指示画面から選択した項目を印字する。複数選択時、選択した並びで項目を繋げて印字する。
 *	2006/05/12 nakamoto  NO005 年齢の表示・非表示を選択可能とするよう修正
 **/

public class KNJA144 {

    private static final Log log = LogFactory.getLog(KNJA144.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String _useAddrField2;
    private String _schoolKind;
    private boolean _hasSCHOOL_MST_SCHOOL_KIND;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス
        String param[] = new String[22];//NO001 NO003 NO004 NO005

        //	パラメータの取得
        String classcd[] = request.getParameterValues("category_selected_out"); //1:学科名称,2:クラス名称＋出席番号 NO004
        try {
            param[0] = request.getParameter("YEAR");         				//年度
            param[1] = request.getParameter("GAKKI");         				//学期
            param[2] = request.getParameter("GRADE_HR_CLASS");         		//学年＋組
            String sdate = request.getParameter("TERM_SDATE");         		//有効期限(開始)
            String edate = request.getParameter("TERM_EDATE");         		//有効期限(終了)
            param[5] = sdate.replace('/','-');
            param[3] = edate.replace('/','-');
            param[7] = request.getParameter("DOCUMENTROOT");         		// '/usr/local/development/src'
            // 学籍番号の指定
            String schno[] = request.getParameterValues("category_selected");//学籍番号
            int i = 0;
            param[4] = "(";
            while(i < schno.length){
                if(schno[i] == null ) break;
                if(i > 0) param[4] = param[4] + ",";
                param[4] = param[4] + "'" + schno[i] + "'";
                i++;
            }
            param[4] = param[4] + ")";
            param[16] = request.getParameter("TITLE");         				//タイトル---NO003
            param[18] = request.getParameter("CHECK1");         			//出席番号 not null:出力しない ---NO004
            param[19] = request.getParameter("selectdata");         		//項目選択 not null:あり,null:なし NO004
            param[21] = request.getParameter("CHECK2");         			//年齢 not null:出力しない ---NO005
            _useAddrField2 = request.getParameter("useAddrField2");
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        //	print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //	svf設定
        svf.VrInit();						   		//クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
        if (_hasSCHOOL_MST_SCHOOL_KIND) {
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param[0] + "' AND GRADE = '" + param[2].substring(0, 2) + "' "));
        }

        //	ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;//NO003
        boolean nonedata = false; 								//該当データなしフラグ
        setHeader(db2,svf,param,classcd);//NO004
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));		//生徒情報
            ps2 = db2.prepareStatement(preStat2(param));		//職名・職員名
            ps3 = db2.prepareStatement(preStat3(param));		//学校名・学校住所
            ps4 = db2.prepareStatement(preStat4(param));		//タイトル名---NO003
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        setStaffJobName(db2,svf,param,ps2);					    //職名・職員名取得メソッド
        setSchoolName(db2,svf,param,ps3);					    //学校名取得メソッド
        setTitleName(db2,svf,param,ps4);					    //タイトル名取得メソッド---NO003
        for(int i=0 ; i<param.length ; i++) {
            log.debug("param["+i+"]="+param[i]);
        }
        if (setSvfout(db2,svf,param,ps1)) {
            nonedata = true;	    //帳票出力のメソッド
        }

        log.debug("nonedata = "+nonedata);

        //	該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 	終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2,ps3,ps4);	//preparestatementを閉じる
        db2.commit();
        db2.close();				//DBを閉じる
        outstrm.close();			//ストリームを閉じる

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
        ,String classcd[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        svf.VrSetForm("KNJA144.frm", 4);

        //	写真データ
        try {
            returnval = getinfo.Control(db2);
            param[8] = returnval.val4;      //格納フォルダ
            param[9] = returnval.val5;      //拡張子
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;

        //	項目選択
        StringBuffer majorname = new StringBuffer();
        //1:学科名称,2:クラス名称＋出席番号
        if (param[19] != null && !param[19].equals("")) {
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if (ia > 0) {
                    majorname.append(" || '　' || ");
                }
                   if (classcd[ia].equals("1")) {
                    majorname.append("T7.MAJORNAME");
                }
                   if (classcd[ia].equals("2")) {
                       if (param[18] != null) majorname.append("T8.HR_NAME");
                       else majorname.append("T8.HR_NAME || RTRIM(CHAR(INT(T1.ATTENDNO))) || '番'");
                }
            }
        } else {
            majorname.append("''");
        }
        param[20] = majorname.toString();

//NO004<---

    }


    /**職名・職員名を取得**/
    private void setStaffJobName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps2
    ) {
        try {
            ResultSet rs = ps2.executeQuery();
            while( rs.next() ){
                param[6]  = rs.getString("JOBNAME");    //職名
                param[10] = rs.getString("STAFFNAME");  //職員名
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setStaffJobName set error!");
        }
    }


    /**学校名を取得**/
    private void setSchoolName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps3
    ) {
        try {
            ResultSet rs = ps3.executeQuery();
            while( rs.next() ){
                param[11] = rs.getString("SCHOOLNAME1");    //学校名
                param[12] = rs.getString("SCHOOLADDR1");    //学校住所
                param[15] = rs.getString("SCHOOLTELNO");    //学校電話番号 NO001
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSchoolName set error!");
        }
    }


    /**タイトル名を取得**/
    private void setTitleName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps4
    ) {
        try {
            ResultSet rs = ps4.executeQuery();
            while( rs.next() ){
                param[17] = rs.getString("NAME1");    //タイトル名---NO003
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTitleName set error!");
        }
    }


    /**帳票出力**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps1.executeQuery();

            //生年月日・年齢--------------
            Calendar cals = Calendar.getInstance();
            Calendar calb = Calendar.getInstance();
            cals.setTime(sdf.parse( param[5] ));				//有効期限(開始)をCalendar calに変換
            int cals_year = cals.get(Calendar.YEAR);			//calsの年を取得
               int cals_day  = cals.get(Calendar.DAY_OF_YEAR);		//calsの年の何日目を取得
            String birth_age = "";                              //年齢

            //画像--------------
            String photo = "";                      //顔写真
            String stamp = "SCHOOLSTAMP" + ".bmp";  //学校印
            String photo_check = "";
            String stamp_check = param[7] + "/" + param[8] + "/" + stamp;
            File f2 = new File(stamp_check);        //学校長印データ存在チェック用

            while( rs.next() ){
                //画像--------------
                //顔写真
                photo = "P" + rs.getString("SCHREGNO") + "." + param[9];
                photo_check = param[7] + "/" + param[8] + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists())
                    svf.VrsOut("PHOTO_BMP"    , photo_check );//顔写真
                //学校印
                if (f2.exists())
                    svf.VrsOut("STAMP_BMP"    , stamp_check );//学校印

                //生徒情報--------------
                svf.VrsOut("TITLE"        ,param[17] );                           //タイトル---NO003
//				svf.VrsOut("TITLE"        ,"生徒証" );                            //タイトル
                svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("SDATE"	    ,KNJ_EditDate.h_format_JP(param[5]));   //有効期限(開始)
                svf.VrsOut("FDATE"	    ,KNJ_EditDate.h_format_JP(param[3]));   //有効期限(終了)
                svf.VrsOut("NAME" 	    ,rs.getString("NAME") );                //氏名(漢字)
                svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO") );            //バーコード
                svf.VrsOut("HR_NAME"      ,rs.getString("MAJORNAME") );           //1:学科名称,2:クラス名称＋出席番号 NO004
//NO002--->
                //所属(千代田)
                if (rs.getString("SCHOOL_FLG") != null) {
                    if (rs.getInt("GRADE") <= 3) svf.VrsOut("COURSE2"  ,"義務課程" );//中学
                    else svf.VrsOut("COURSE1"  ,(rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程" : "" );//高校

                //所属(千代田以外)
                } else {
                    if (rs.getString("SCHOOL_FLG2") != null) svf.VrsOut("COURSE2"  ,"義務課程" );//中学
                    else svf.VrsOut("COURSE1"  ,(rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+(rs.getString("COURSENAME").endsWith("課程") ? "" : "課程") : "" );//高校
                }
/***
                if ( rs.getString("SCHOOL_FLG") != null && rs.getInt("GRADE") <= 3 )
                    svf.VrsOut("COURSE2"  ,"義務課程" );                          //所属(千代田：1〜3年生)
                else
                    svf.VrsOut("COURSE1"  ,(rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程" : "" );                        //所属(その他)
***/
//NO002<---

                //生年月日・年齢--------------
                if (rs.getString("BIRTHDAY") != null) {
                    if (param[21] == null) {//NO005 Add
                        calb.setTime(sdf.parse( rs.getString("BIRTHDAY") ));//生年月日をCalendar calに変換
                        int calb_year = calb.get(Calendar.YEAR);			//calbの年を取得
                        int calb_day  = calb.get(Calendar.DAY_OF_YEAR);		//calbの年の何日目を取得
                        if (calb_day <= cals_day) birth_age = "　(" + String.valueOf(cals_year - calb_year) + "才)";
                        else                      birth_age = "　(" + String.valueOf(cals_year - calb_year - 1) + "才)";
                    }//NO005 Add
                    svf.VrsOut("BIRTHDAY" ,KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")) + birth_age );
                }

                //住所１または住所２が２０文字超えたら(50)にセット
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_useAddrField2) &&
                        ((addr1 != null && addr1.length() > 25) ||
                         (addr2 != null && addr2.length() > 25))
                        ) {
                    svf.VrsOut("ADDRESS1_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS2_3" ,addr2 );  //住所２(60)
                } else if ((addr1 != null && addr1.length() > 20) ||
                            (addr2 != null && addr2.length() > 20))
                {
                    svf.VrsOut("ADDRESS1_2" ,addr1 );  //住所１(50)
                    svf.VrsOut("ADDRESS2_2" ,addr2 );  //住所２(50)
                } else {
                    svf.VrsOut("ADDRESS1_1" ,addr1 );  //住所１(40)
                    svf.VrsOut("ADDRESS2_1" ,addr2 );  //住所２(40)
                }

                //発行者情報--------------
                if (null != param[12]) {
                    if ("1".equals(_useAddrField2) && param[12].length() > 25) {
                        svf.VrsOut("SCHOOLADDRESS4",param[12] );                           //学校住所
                    } else if ("1".equals(_useAddrField2) && param[12].length() > 20) {
                        svf.VrsOut("SCHOOLADDRESS3",param[12] );                           //学校住所
                    } else if ("1".equals(_useAddrField2) && param[12].length() > 17) {
                        svf.VrsOut("SCHOOLADDRESS2",param[12] );                           //学校住所
                    } else {
                        svf.VrsOut("SCHOOLADDRESS",param[12] );                           //学校住所
                    }
                }
                svf.VrsOut("SCHOOLNAME"   ,param[11] );                           //学校名
                svf.VrsOut("JOBNAME"      ,param[6] );                            //職名
                svf.VrsOut("STAFFNAME"    ,param[10] );                           //職員名
                svf.VrsOut("SCHOOLPHONE",  param[15] );                           //学校電話番号 NO001

                svf.VrEndRecord();//１行出力
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }


    /**生徒情報**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        //	パラメータ（学籍番号）
        try {
            //住所
            stb.append("WITH SCH_ADDR1 AS ( ");
            stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN "+param[4]+" ");
            stb.append("    GROUP BY SCHREGNO ) ");
            stb.append(",SCH_ADDR2 AS ( ");
            stb.append("    SELECT SCHREGNO,ISSUEDATE,ADDR1,ADDR2 ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN "+param[4]+" ) ");
            stb.append(",SCH_ADDR AS ( ");
            stb.append("    SELECT W2.SCHREGNO,W2.ADDR1,W2.ADDR2 ");
            stb.append("    FROM   SCH_ADDR1 W1,SCH_ADDR2 W2 ");
            stb.append("    WHERE  W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append("           W1.ISSUEDATE=W2.ISSUEDATE ) ");

            //メイン
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       T1.GRADE, ");
            stb.append("       (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 = 'chiyoda') AS SCHOOL_FLG, ");
            stb.append("       T6.SCHOOLNAME1 AS SCHOOL_FLG2, ");//NO002
            stb.append("       T5.COURSENAME, ");
            stb.append("       "+param[20]+" AS MAJORNAME, ");//NO004
            stb.append("       T3.ADDR1, ");
            stb.append("       T3.ADDR2, ");
            stb.append("       T2.NAME, ");
            stb.append("       T2.BIRTHDAY ");
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCH_ADDR T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN COURSE_MST T5 ON T5.COURSECD=T1.COURSECD ");
            stb.append("       LEFT JOIN SCHOOL_MST T6 ON T6.YEAR=T1.YEAR AND T6.SCHOOLNAME1 LIKE '%中学%' ");
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append(" AND  T6.SCHOOL_KIND = '" + _schoolKind + "' ");
            }
            stb.append("       LEFT JOIN MAJOR_MST T7 ON T7.COURSECD = T1.COURSECD AND T7.MAJORCD=T1.MAJORCD ");//NO004
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T8 ON T8.YEAR=T1.YEAR AND T8.SEMESTER=T1.SEMESTER AND T8.GRADE=T1.GRADE AND T8.HR_CLASS=T1.HR_CLASS ");//NO004
            stb.append("WHERE  T1.YEAR='"+param[0]+"' AND ");
            stb.append("       T1.SEMESTER='"+param[1]+"' AND ");
            stb.append("       T1.SCHREGNO IN "+param[4]+" ");
            stb.append("ORDER BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り


    /**職名・職員名**/
    private String preStat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT STAFFNAME, ");
            stb.append("       (SELECT JOBNAME FROM JOB_MST T2 WHERE T2.JOBCD=T1.JOBCD) AS JOBNAME ");
            stb.append("FROM   V_STAFF_MST T1 ");
            stb.append("WHERE  YEAR='"+param[0]+"' AND JOBCD='0001' ");//学校長
        } catch( Exception e ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat2()の括り


    /**タイトル名**/
    private String preStat4(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT NAME1 ");
            stb.append("FROM   NAME_MST ");
            stb.append("WHERE  NAMECD1='A019' AND NAMECD2='"+param[16]+"' ");//NO003
        } catch( Exception e ){
            log.error("preStat4 error!");
        }
        return stb.toString();

    }//preStat4()の括り


    /**学校名**/
    private String preStat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SCHOOLNAME1,SCHOOLADDR1 ");
            stb.append(      ",SCHOOLTELNO ");   //NO001
            stb.append("FROM   SCHOOL_MST ");
            stb.append("WHERE  YEAR='"+param[0]+"' ");
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("AND  SCHOOL_KIND = '" + _schoolKind + "' ");
            }
        } catch( Exception e ){
            log.error("preStat3 error!");
        }
        return stb.toString();

    }//preStat3()の括り


    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4
    ) {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();//NO003
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



}//クラスの括り

// kanji=漢字
/*
 * $Id: 84d7e74bf483db14115a64803481a200641595f2 $
 *
 * 作成日: 2005/06/02
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
http://tokio/servlet/KNJD?DBNAME=SY0403&PRGID=KNJD062&YEAR=2005&SEMESTER=9&GRADE=01&CLASS_SELECTED=01002&DATE=2006/03/31&TESTKINDCD=0&SEME_FLG=2&OUTPUT5=on
 *
 *  学校教育システム 賢者 [成績管理] 成績一覧
 *  2005/06/06 yamashiro・出席番号の欠番を詰めて出力する処理を追加
 *  2005/06/15 yamashiro・出欠集計において、課程マスタの開始終了校時の条件を追加
 *  2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
 *  2005/08/17 yamashiro・時数取得をＨＲクラスにおける複数の同一科目講座の存在に適応
 *  2005/08/18 yamashiro・8/17修正による不具合を修正
 *  2005/09/16 yamashiro・欠課時数上限値の使用方法を修正
 *  2005/09/28 yamashiro・平均点を小数点第一位まで出力
 *  2005/09/30 yamashiro・編入のデータ仕様変更および在籍異動条件に転入学を追加
 *  2006/01/31 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO004
 *  2006/02/16 yamashiro・評価読替科目が複数列出力される不具合を修正 --NO005
 *  2006/02/21 yamashiro・評定読替科目の評定が出力されない不具合を修正 --NO006
 *  2006/03/02 yamashiro・読替科目について　履修単位：読替元は加算しない。修得単位数：読替元は加算しない。 --NO007
 *                      ・読替科目は学年末のみ出力 --NO008
 *                      ・--NO004修正時の不具合を修正 DB2の型変換の使用方法に間違いがあった --NO007
 *  2006/03/13 yamashiro・○単位マスターの無条件履修取得フラグが１の場合は、生徒名簿及び成績データがなくても行を出力し、単位は単位マスターから出力する --NO010
 *                        　但し成績データがあれば成績データを優先する
 *                             => 総合的な学習の時間(教科コード=90)の仕様変更も含める
 *                        ○修得単位は成績データの標準修得単位を出力する --NO010
 *                        ○複数科目に対応( => 教科コード90は１行空けて出力する) --NO009
 *                        ○クラス単位の「履修単位数」「授業日数」は出力しない --NO013 （依頼以外の取り決め）
 *  2006/03/16 yamashiro・○SQL0954Cの不具合対応のため、prestatStdTotalAttendメソッドのSQL文を生徒単位に変更  --NO014
 *                      ・○成績データがあれば成績データから履修・修得単位を出力  --NO0015
 *                          無条件フラグがオンで成績データがない場合、欠課超過(出欠がある場合)でなければ単位マスターから履修・修得単位を出力  --NO0015
 *                        ○教科コード'90'の列送りが１９列目の場合の不具合を修正 --NO016
 *  2006/03/17 yamashiro・○表記科目に読替科目元がない読替先科目が出力される不具合を修正  --NO0017
 *                        ○無条件フラグがONで履修単位・修得単位がnullだったら、単位マスタから単位を出力--NO0018
 *                        ○総合的学習の前の空白はカット --NO0019
 *                        ○SQL0954Cの不具合対応のため、prestatStdTotalAttendメソッドのSQL文を修正  --NO020
 *
 *  2006/03/24 yamashiro・○クラス単位の授業日数を再掲 --NO021
 *                        ○クラス単位の履修単位を再掲 --NO022
 *                        ○学期成績、学年成績の一覧でも個人総合・平均・順位および学級平均・合計を出力する --NO023
 *                          成績の取得仕様を修正(通知表に合わせて) --NO024
 *  2006/04/04 yamashiro・○出欠の記録において、不在者の日数が授業日数から余分に引かれていることによる不具合を修正 --NO025
 *                           => 出欠集計用テーブルの変更および出欠集計処理の変更に対応
 *                              KNJD060K_BASEを参照
 *                              ○学籍不在時および留学・休学中の出欠データはカウントしない
 *                              ○出停・忌引がある日は遅刻・早退をカウントしない
 */
/**
 *
 *  学校教育システム 賢者 [成績管理] 成績一覧
 *  2005/06/02 yamashiro
 */
public class KNJD062{

    private static final Log log = LogFactory.getLog(KNJD062.class);
    private Param param;
    private boolean nonedata;

    /** 0 : パラメータ学年/学期成績 */
    private static final String PARAM_GRAD_SEM_KIND = "0";
    /** 9900 : 学年/学期成績 */
    private static final String GRAD_SEM_KIND = "9900";

    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

    // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                         //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("db new error:", ex);
        }

    // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) db2.close();
            return;
        }

    // パラメータの取得
        param = getParam(request, db2);

    // 印刷処理
        printSvf(request, db2, svf);

    // 終了処理
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        svf.VrQuit();

        try {
            db2.commit();
            db2.close();
        } catch (Exception ex) {
            log.error("db close error!", ex);
        }//try-cathの括り

    }   //doGetの括り


    private void printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf) {

        svf.VrSetForm("KNJD062.frm", 4);

        try {
            String hrclass[] = request.getParameterValues("CLASS_SELECTED"); //学年・組  05/05/30
            KNJD062_BASE obj = null;
            if (param._testitemcd.equals("01")) {
                if (param._semester.equals("3") ) return;                  //05/02/20Modify
                obj = new KNJD062_INTER(db2, svf);
            }else if (param._testitemcd.equals("02") )
                obj = new KNJD062_TERM(db2, svf);
            else if (!param._semester.equals("9") )
                obj = new KNJD062_GAKKI(db2, svf);
            else if (param._semester.equals("9") )
                obj = new KNJD062_GRADE(db2, svf);
            else
                return;

            if (obj.printSvf(hrclass)) {
                nonedata = true;           //05/05/30Modify
            }
        } catch (Exception ex ) {
            log.error("printSvf error!", ex);
        }
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     *       2005/01/05 最終更新日付を追加(param._divideAttendDate)
     *       2005/05/22 学年・組を配列で受け取る
     */
    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private class KNJD062_BASE {

        Vrw32alp svf;                   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2;                     //Databaseクラスを継承したクラス
        String fieldname;
        String fieldname2;
        int subclasslinecount;          //科目列カウント
        KNJDefineSchool definecode;       //各学校における定数等設定 05/05/22
        KNJSchoolMst _knjSchoolMst;
        
        int hr_total;                                   //クラス平均総合点
        int hr_lesson[] = new int[2];                   //クラス授業日数{最大、最小}  04/11/01Add
        int hr_attend[] = new int[9];                   //クラス平均出欠格納
        int hr_seitosu[] = new int[12];                 //クラス平均対象生徒数格納
        int sch_rcredits[] = new int[50];               //生徒ごとの履修単位数 05/01/31
        int sch_ccredits[] = new int[50];               //生徒ごとの修得単位数 05/01/31
        int hr_credits;                                 //クラスの履修単位数   05/01/31
        int subclasstotalcnt;                           //科目別件数     05/03/09
        int subclasstotalnum;                           //科目別得点累積 05/03/09
        int hrtotalnum;                                 //全科目得点累積 05/03/10
        int hrtotalcnt;                                 //全科目件数     05/03/10

        private static final String SUBJECT_D = "01";  //教科コード
        private static final String SUBJECT_U = "89";  //教科コード
        private static final String SUBJECT_T = "90";  //総合的な学習の時間
        /** 9900 : 学年/学期成績 */
        private static final String GRAD_SEM_KIND = "9900";
        
        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";
        
        String sql3;

        /**
         *  コンストラクター
         */
        KNJD062_BASE(final DB2UDB db2, final Vrw32alp svf) {
            this.db2 = db2;
            this.svf = svf;
        }


        /**
         *  印刷処理
         */
        boolean printSvf(final String hrclass[]) {

            boolean nonedata = false;               //該当データなしフラグ
            try {
                set_head();                                     //見出し出力のメソッド
                if (printSvfMain(hrclass)) {
                    nonedata = true;   //印刷処理
                }
            } catch (Exception ex) {
                log.error("error! ", ex);
            }
            return nonedata;
        }



        /**
         *  ページ見出し・初期設定
         */
        private void set_head()
        {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
            if (definecode == null )setClasscode();         //教科コード・学校区分・学期制等定数取得 05/05/22
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, param._year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

        //  学期名称、範囲の取得
            try {
                returnval = getinfo.Semester(db2,param._year,param._semester);
                param._semestername = returnval.val1;                                 //学期名称
                param._semesterSdate = returnval.val2;                                  //学期期間FROM
                param._semesterEdate = returnval.val3;                                  //学期期間TO
            } catch (Exception ex) {
                log.warn("term1 svf-out error!", ex);
            } finally {
                if (param._semesterSdate == null) param._semesterSdate = param._year + "-04-01";
                if (param._semesterEdate == null) param._semesterEdate = ( Integer.parseInt(param._year) + 1 ) + "-03-31";
            }
           
            returnval = getinfo.Semester(db2,param._year,"9");
            param._yearEdate = returnval.val2;  // 年度期間FROM

        //  出欠データ集計用開始日取得 => 2004年度の１学期は累積データを使用する => 出欠データ集計は2004年度2学期以降
            try {
                if (! param._year.equals("2004") )
                    param._attendDate = param._semesterSdate;               //出欠集計用
                else {
                    returnval = getinfo.Semester(db2,param._year, "2" );
                    param._attendDate = returnval.val2;                                 //学期期間FROM
                }
            } catch (Exception ex) {
                log.warn("term1 svf-out error!", ex);
            } finally {
                if (param._attendDate == null) param._attendDate = param._year + "-04-01";
            }

        //  作成日(現在処理日)・出欠集計範囲の出力 05/05/22Modify
            try {
                //システム時刻を表記 05/05/22
                final StringBuffer stb = new StringBuffer();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                stb.append( nao_package.KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
                sdf = new SimpleDateFormat("年M月d日H時m分");
                stb.append( sdf.format(date) );
                param._now = stb.toString();
            } catch (Exception ex) {
                log.warn("ymd1 svf-out error!", ex);
            }

          //対象校時および名称取得  05/06/15
            // setPeiodValue();

            set_head2();
            
          // 出欠状況取得引数をロード
            loadAttendSemesArgument(db2);

    //for( int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i]);
        }//set_headの括り


        /**
         *  ページ見出し
         *  中間、期末、学期、学年末で設定
         */
        void set_head2() {}


        /**
         * メイン印刷処理
         *   対象生徒(Sql=>prestatStdNameList)を読込み、５０名ごとに印刷(printSvfStdDetail)する
         */
        private boolean printSvfMain(final String hrclass[])
        {
            boolean nonedata = false;
            Map schregnoLineMap = new HashMap(50);              //学籍番号と行番号の保管
            String lschname[] = new String[50];     //生徒名の保管 05/05/22
            Map lineRemarkMap = new HashMap();                //行番号(出席番号)と備考の保管 05/05/22
            PreparedStatement arrps[] = new PreparedStatement[5];
            Map subclassCountMap = new HashMap();                //科目別件数     05/06/07
            Map accumulateScoreMap = new HashMap();                //科目別得点累積 05/06/07
//            definecode.absent_cov = 1;  // 試し
            String absentDiv = "1"; // 1:年間 2:随時
            
            try {
                arrps[0] = db2.prepareStatement( prestatStdNameList()       );      //学籍データ
                arrps[1] = db2.prepareStatement( prestatStdTotalRec()       );      //成績累計データ
                arrps[2] = db2.prepareStatement( prestatSubclassInfo()      );      //科目名等データ
                sql3 = prestatStdSubclassDetail(absentDiv);
                arrps[3] = db2.prepareStatement(sql3);      //明細データ
                String prestatStdTotalAttendSql =  AttendAccumulate.getAttendSemesSql(
                        _semesFlg,
                        definecode,
                        _knjSchoolMst,
                        param._year,
                        SSEMESTER,
                        param._semester,
                        (String) _hasuuMap.get("attendSemesInState"),
                        _periodInState,
                        (String) _hasuuMap.get("befDayFrom"),
                        (String) _hasuuMap.get("befDayTo"),
                        (String) _hasuuMap.get("aftDayFrom"),
                        (String) _hasuuMap.get("aftDayTo"),
                        param._grade,
                        "?",
                        "?",
                        "SEMESTER",
                        param._useCurriculumcd,
                        param._useVirus,
                        param._useKoudome
                );
                arrps[4] = db2.prepareStatement( prestatStdTotalAttendSql );      //出欠累計データ
            } catch (Exception ex ) {
                log.error("[KNJD171K]boolean printSvfMain prepareStatement error! ", ex);
                return nonedata;
            }

            for( int i = 0 ; i < hrclass.length ; i++) {
    log.debug("hrclass="+hrclass[i]);
                try {
                    param._hrclass = hrclass[i];
                    if (printSvfMainHrclass( schregnoLineMap, lineRemarkMap, lschname, arrps, subclassCountMap, accumulateScoreMap)) nonedata = true;
                    clearMapValue( schregnoLineMap, lineRemarkMap, subclassCountMap, accumulateScoreMap, lschname );
                    clearValue();
                } catch (Exception ex ) {
                    log.error("[KNJD062]boolean printSvfMainHrclass() error! ", ex);
                }
            }

            return nonedata;
        }


        /**
         *  マップ初期化
         *  2005/06/07
         */
        private void clearMapValue(final Map hm1, final Map hm2, final Map hm3, final Map hm4, final String lschname[])
        {
            try {
                if (hm1 != null )hm1.clear();                       //行番号情報を削除
                if (hm2 != null )hm2.clear();                       //備考情報を削除
                if (hm3 != null )hm3.clear();                           //備考情報を削除
                if (hm4 != null )hm4.clear();                           //備考情報を削除
                if (lschname != null )for( int j = 0 ; j < lschname.length ; j++ )lschname[j] = null;
                param._sattendno = null;
                param._eattendno = null;
            } catch (Exception ex ) {
                log.warn("clearMapValue error!", ex);
            }
        }


        /**
         *
         * SVF-OUT 学級単位の印刷処理
         *    Stirng param[]について ==> param._hrclass:学年・組
         */
        private boolean printSvfMainHrclass(final Map schregnoLineMap, final Map lineRemarkMap, final String lschname[], final PreparedStatement arrps[],
                final Map subclassCountMap, final Map accumulateScoreMap)
        {
            boolean nonedata = false;
            ResultSet arrrs[] = new ResultSet[2];
            int schno = 0;                          //行番号
            int schno2 = 0;                         //行番号
            boolean bout = false;                   //05/06/07Build

            //学級単位の印刷処理
            try {
                int pp = 0;
                arrps[0].setString( ++pp,  param._hrclass );                      //学年・組
                arrrs[0] = arrps[0].executeQuery();                         //生徒名の表
                while( arrrs[0].next()) {
                    //05/06/06Modify 行を詰めて出力する処理を追加
                    if (param._output5 != null) {
                        ++schno;                                            //05/06/06
                        if (50 <= schno )bout = true;
                    } else{
                        schno = ( arrrs[0].getInt("ATTENDNO") % 50 != 0 )? arrrs[0].getInt("ATTENDNO") % 50 : 50;
                        if (schno < schno2 )bout = true;
                        schno2 = schno;
                    }
                    //改ページ処理
                    if (bout) {
                        //log.debug("check schno="+schno);
                        if (printSvfStdDetail( schregnoLineMap, lineRemarkMap, lschname, arrps, false, subclassCountMap, accumulateScoreMap ) ) nonedata = true;          //成績・評定・欠課出力のメソッド
                        clearMapValue( schregnoLineMap, lineRemarkMap, null, null, lschname );
                        if (param._output5 != null )schno = 1;
                        else                   schno = ( arrrs[0].getInt("ATTENDNO") % 50 != 0 )? arrrs[0].getInt("ATTENDNO") % 50 : 50;
                        param._sattendno = null;                                    //開始生徒
                        param._eattendno = null;                                   //終了生徒
                        bout = false;
                        clearValue2();
                    }

                    saveSchInfo( arrrs[0], schno, schregnoLineMap, lschname, lineRemarkMap );     //生徒情報の保存処理 2005/05/22

                }
            } catch (Exception ex ) {
                log.warn("ResultSet-read error!", ex);
            } finally{
                db2.commit();
            }

            //成績・評定・欠課の印刷処理
    //log.debug("hm1 size="+hm1.size());
            if (0 < schregnoLineMap.size()) {
                if (printSvfStdDetail( schregnoLineMap, lineRemarkMap, lschname, arrps, true, subclassCountMap, accumulateScoreMap ) )nonedata = true;     //成績・評定・欠課出力のメソッド OKならインスタンス変数nonedataをtrueとする
                clearMapValue( schregnoLineMap, lineRemarkMap, null, null, lschname );
                clearValue2();
            }
            for (int i = 0 ; i < arrrs.length ; i++) DbUtils.closeQuietly(arrrs[i]);
            return nonedata;
        }


        /**
         *  生徒情報の保存処理
         *    2005/05/22
         *    2005/06/07 Modify String lschname[]の前３桁に出席番号を挿入
         */
        void saveSchInfo(final ResultSet rs, final int schno, final Map hm1, final String lschname[], final Map hm2)
        {
            try {
                hm1.put( rs.getString("SCHREGNO"), new Integer(schno));    //行番号に学籍番号を付ける
                if (param._sattendno == null) {
                    param._sattendno = rs.getString("ATTENDNO"); //開始生徒
                }
                param._eattendno = rs.getString("ATTENDNO");                       //終了生徒
                //lschname[ schno-1 ] = rs.getString("NAME");                   //生徒名
                String str = "000" + rs.getString("ATTENDNO");
                lschname[schno - 1] = str.substring( str.length() - 3 , str.length()) + rs.getString("NAME");  //05/06/07

                //  文言をセットする。（除籍日付＋'付'＋除籍区分名称）04/11/08Add
                if (rs.getString("KBN_DATE1") != null) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE1")));
                    if (rs.getString("KBN_NAME1") != null) {
                        stb.append( rs.getString("KBN_NAME1") );
                    }
                    hm2.put(new Integer(schno), stb.toString() );  //備考に行番号を付ける
                } else if (rs.getString("KBN_DATE2") != null) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE2")));
                    if (rs.getString("KBN_NAME2") != null) {
                        stb.append( rs.getString("KBN_NAME2") );
                    }
                    hm2.put(new Integer(schno), stb.toString() );  //備考に行番号を付ける
                }
            } catch (Exception ex) {
                log.warn("name1 svf-out error!", ex);
            }

        }


        /**
         *  生徒名等出力 
         *    2005/05/22 Modify
         *    2005/06/07 Modify 
         */
        void printSvfStdNameOut(final Map hm2, final String lschname[]) {

            try {
//                Integer int1 = null;
                for( int i = 0 ; i < lschname.length ; i++) {
                    //ret = svf.VrsOut("name" + (i+1) ,lschname[i]);
                    if (lschname[i] == null) {
                        continue;
                    }
                    svf.VrsOut("name" + (i + 1) ,   lschname[i].substring(3));
                    if (param._output5 != null) {
                        svf.VrsOutn("NUMBER", (i + 1) , String.valueOf( Integer.parseInt(lschname[i].substring(0, 3))));
                    } else {
                        svf.VrsOutn("NUMBER", (i + 1), String.valueOf(Integer.parseInt(lschname[i].substring(0, 3))));
                        //svf.VrsOutn("NUMBER", (i + 1), String.valueOf(i + 1));
                    }
                    if (hm2.containsKey(new Integer(i + 1))) {
                        svf.VrsOut("REMARK" + (i + 1) , (String) hm2.get( new Integer(i + 1)));
                    }
                }
            } catch (Exception ex) {
                log.warn("name1 svf-out error!", ex);
            }

        }


        /**
         *  明細出力処理 
         *    科目別明細(成績・評定・欠課)(SQL=>prestatStdSubclassDetail)を読込み出力する
         *    科目名の変わり目で科目情報(教科名・科目名・単位数・授業時数)(SQL=>prestatSubclassInfo)を出力する()
         *    最後に生徒別総合＆合計(SQL=>printSvfStdTotalRec,printSvfStdTotalAttend)を出力する
         *      2005/01/04 SQL変更に伴い修正
         *      2005/06/07 Modify boolean blast追加
         */
        boolean printSvfStdDetail(
                final Map schregnoLineMap,
                final Map lineRemarkMap,
                final String lschname[],
                final PreparedStatement arrps[],
                final boolean blast,
                final Map subclassCountMap,
                final Map accumulateScoreMap
        ) {
            boolean nonedata = false;
            ResultSet rs1 = null;
            ResultSet rs2 = null;
            int assesspattern = 0;          //科目の評定類型 A:0 B:1 C:2
            for (int i = 0 ; i < sch_rcredits.length ; i++) sch_rcredits[i] = 0;   //履修単位数 05/01/31
            for (int i = 0 ; i < sch_rcredits.length ; i++) sch_ccredits[i] = 0;   //修得単位数 05/01/31
            int subclass1 = 0;              //科目コードの保存
            int subclass2 = 0;              //科目コードの保存
            int replaceflg = 0;             //評価読替元科目:1  評価読替先科目:-1  NO007 Build
            clearSvfField( lineRemarkMap, lschname );                         //生徒名等出力のメソッド
            //if (log.isDebugEnabled()) { for (int i = 0 ; i < param.length ; i++) log.debug("param[" + i + "] = " + param[i]); }

            try {
                int p = 0;
                arrps[3].setString( ++p, param._hrclass );
                arrps[3].setString( ++p, param._sattendno  );       //生徒番号
                arrps[3].setString( ++p, param._eattendno );       //生徒番号
                if (log.isDebugEnabled()) { log.debug("prestatStdSubclassDetail executeQuery() start"); }
                rs2 = arrps[3].executeQuery();                  //明細データのRecordSet
                if (log.isDebugEnabled()) { log.debug("prestatStdSubclassDetail executeQuery() end"); }
            } catch (Exception ex) {
                log.warn("ResultSet-read error!", ex);
            }

            try {
                int p = 0;
                arrps[2].setString( ++p, param._hrclass );                //学年・組
                arrps[2].setString( ++p, param._hrclass );                //学年・組
                arrps[2].setDate( ++p, java.sql.Date.valueOf(param._divideAttendDate));  // 出欠データ集計開始日付 2005/01/04
                arrps[2].setDate( ++p, java.sql.Date.valueOf(param._date));  // 出欠データ集計終了日付 2005/01/04
                arrps[2].setString( ++p, KNJC053_BASE.retSemesterMonthValue (param._divideAttendMonth ) );     //出欠集計データ終了学期＋月 NO004
                arrps[2].setString( ++p, param._hrclass );                //学年・組
                if (log.isDebugEnabled()) { log.debug("prestatSubclassInfo executeQuery() start"); }
                rs1 = arrps[2].executeQuery();                                  //科目名等のRecordSet
                if (log.isDebugEnabled()) { log.debug("prestatSubclassInfo executeQuery() end"); }
            } catch (Exception ex) {
                log.warn("ResultSet-read error!", ex);
            }

            boolean rs1read = false; //NO009
            try {
                while ( rs2.next()) {
                    //科目コードのブレイク
                    final int rs2subclass;
                    if ("1".equals(param._useCurriculumcd)) {
                        rs2subclass = Integer.parseInt(StringUtils.split(rs2.getString("SUBCLASSCD"), "-")[3]);
                    } else {
                        rs2subclass = rs2.getInt("SUBCLASSCD");
                    }
                    if (subclass2!= rs2subclass) {
                        if (subclass2!=0) {
                            printSvfsubclasscnt_num( subclass1, blast, subclassCountMap, accumulateScoreMap );
                            svf.VrEndRecord();
                            nonedata = true;
                            subclasslinecount++;
                            subclasstotalcnt = 0;  //05/03/09
                            subclasstotalnum = 0;  //05/03/09
                            replaceflg = 0;  //NO007 Build
                        }
                        subclass2 = rs2subclass;           //科目コードの保存
                        //科目名をセット
                        for( ; subclass1<subclass2 ;) {
                            if (19 <= subclasslinecount ) subclasslinecount = clearSvfField( lineRemarkMap, lschname );
                            if (rs1.next()) {
                                if (!rs1read) {
                                    rs1read = true;  //NO009
                                }
                                replaceflg = getValueReplaceflg( rs1 );  //評価読替科目の判断 NO007 Build
                                final int rs1subclass;
                                if ("1".equals(param._useCurriculumcd)) {
                                    rs1subclass = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                                } else {
                                    rs1subclass = rs1.getInt("SUBCLASSCD");
                                }
                                subclass1 = rs1subclass;
                                assesspattern = 0;
                                printSvfSubclassInfoOut(rs1);       //科目名等出力のメソッド
                                if (subclass1<subclass2) {          //データ出力用にない科目列を出力する
                                    printSvfsubclasscnt_num( subclass1, blast, subclassCountMap, accumulateScoreMap );      //05/03/16Modify
                                    svf.VrEndRecord(); 
                                    nonedata = true; 
                                    subclasslinecount++;
                                    subclasstotalcnt = 0;  //05/03/09
                                    subclasstotalnum = 0;  //05/03/09
                                }
                                if (subclass1==subclass2 )break;        //科目名出力用とデータ出力用の科目コードが一致なら抜ける
                            } else {
                                rs1read = false;  //NO009
                            }
                        }
                    }
                    //明細データの出力 05/03/03Modify 総合学習を別処理とする
                    printSvfStdDetailOut(rs2, schregnoLineMap, assesspattern, replaceflg); //総合学習以外 NO007Modify
                }
                //NO009
                if (rs1read )for( int i = 0; i < 20; i++) {
                    printSvfsubclasscnt_num( subclass1, blast, subclassCountMap, accumulateScoreMap );      //05/03/16Modify
                    svf.VrEndRecord(); 
                    nonedata = true; 
                    subclasslinecount++;
                    subclasstotalcnt = 0;  //05/03/09
                    subclasstotalnum = 0;  //05/03/09
                    if (rs1.next()) {
                        if (19 <= subclasslinecount ) subclasslinecount = clearSvfField( lineRemarkMap, lschname );  //NO0019
                        replaceflg = getValueReplaceflg( rs1 );  //評価読替科目の判断 NO007 Build
                        final int rs1subclass;
                        if ("1".equals(param._useCurriculumcd)) {
                            rs1subclass = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                        } else {
                            rs1subclass = rs1.getInt("SUBCLASSCD");
                        }
                        subclass1 = rs1subclass;
                        assesspattern = 0;
                        printSvfSubclassInfoOut(rs1);       //科目名等出力のメソッド
                    } else break;
                }
            } catch (Exception ex ) {
                log.warn("ResultSet-read error!", ex);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(rs1);
                DbUtils.closeQuietly(rs2);
            }

            //総合欄の印刷処理
            if (subclass2 != 0) {
                printSvfsubclasscnt_num( subclass1, blast, subclassCountMap, accumulateScoreMap );   //05/03/16
                printSvfStdTotalRec( schregnoLineMap, arrps );
                printSvfStdTotalAttend( schregnoLineMap, arrps );
                if (blast )printSvfTotalOut( blast );
                svf.VrEndRecord();
                nonedata = true;
                subclasslinecount++;
            }

            return nonedata;

        }


        /**
         *  科目名等出力 
         */
        void printSvfSubclassInfoOut(final ResultSet rs) {

            try {
                boolean amikake = false;
                //NO009 if (rs.getString("SUBCLASSCD").substring(0,2).equals("90")) {
                //NO009     printSvfOutSpace();
                //NO009     subclasslinecount = 18;
                //NO009 }
                svf.VrsOut("course1" , rs.getString("CLASSABBV"));                //教科名
                //log.debug("subclasslinecount="+subclasslinecount+"   subclassname="+rs.getString("SUBCLASSNAME"));
                if (rs.getString("ELECTDIV") != null  &&  ! rs.getString("ELECTDIV").equals("0") ) amikake = true;
                if (amikake ) svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(2,70,2),Bold=1"); //網掛け
                svf.VrsOutn("subject1", subclasslinecount+1, rs.getString("SUBCLASSNAME"));   //科目名

                svf.VrsOutn("credit1",  subclasslinecount+1, setSubclassCredits(rs));    //単位数
                
                if (rs.getString("JISU") != null  &&  0 < Integer.parseInt( rs.getString("JISU") ) ) //05/03/09
                svf.VrsOutn("lesson1",  subclasslinecount+1, rs.getString("JISU"));           //授業時数
                if (amikake ) svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(0,0,0),Bold=0");  //網掛け

            } catch (Exception ex) {
                log.warn("course1... svf-out error!", ex);
            }

        }//printSvfSubclassInfoOut()の括り


        // クラスの科目別単位数を編集して返す。
        private String setSubclassCredits(final ResultSet rs) {
            int credits_a = 0;
            int credits_b = 0;
            try {
                credits_a = rs.getInt("MAXCREDITS");
                credits_b = rs.getInt("MINCREDITS");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            if (credits_a == 0) { return ""; }
            if (credits_a == credits_b) { return String.valueOf(credits_a); }
            return String.valueOf(credits_b) + " \uFF5E " + String.valueOf(credits_a);
        }

        
        /**
         *  科目名等出力  評価読替科目の判断      NO007 Build
         */
        int getValueReplaceflg(final ResultSet rs) 
        {
            int ret = 0;
            try {
                if (rs.getString("REPLACEFLG") != null) {
                    if (rs.getString("REPLACEFLG").equals("SAKI") ) ret = -1;
                    else if (rs.getString("REPLACEFLG").equals("MOTO") ) ret = 1;
                }
            } catch (Exception ex) {
                log.warn("ResultSet-read error!", ex);
            }
            return ret;
        }


        /**
         *  科目名等出力   総合的な学習を最後列に出力するための処理
         *
        void printSvfOutSpace() {
            
            for( int i = subclasslinecount+1 ; i < 19 ; i++) {
                svf.VrAttribute("course1",  "Meido=100" );    //教科名を白文字設定
                svf.VrsOut("course1", String.valueOf( i ) );  //教科名
                svf.VrEndRecord();
            }
        }*/


        /**
         *  科目名等出力   総合的な学習を最後列に出力するための処理  --NO009
         */
        void printSvfOutSpace2() {
            
            if (subclasslinecount != 0) {
                svf.VrAttribute("course1",  "Meido=100" );    //教科名を白文字設定
                svf.VrsOut("course1", String.valueOf( 1 ) );  //教科名
    log.debug("subclasslinecount="+subclasslinecount);
                svf.VrEndRecord();
                subclasslinecount++;
            }
        }




        /**
         *  科目別および全科目の平均・合計累積処理
         *      学期および学年成績において ( param._testitemcd=='0' ) 評定読替科目は含めない
                                                                (SQLのSELECTで'REPLACEMOTO'がマイナスなら評定読替科目)
         *  2005/03/09
         *  NO023
         */
        void subclasscnt_num(final String score, final int replacemoto)
        {
            try {
                if (score != null  &&
                    ! score.equals("")  &&
                    ! score.equals("-")  &&
                    ! score.equals("=") )
                {
                    subclasstotalnum += Integer.parseInt( score );
                    subclasstotalcnt ++;

                    if (param._testcd.equals(GRAD_SEM_KIND)) {
                        if (0 <= replacemoto) {
                            hrtotalnum += Integer.parseInt( score );
                            hrtotalcnt ++;
                        }
                    } else{
                        hrtotalnum += Integer.parseInt( score );
                        hrtotalcnt ++;
                    }
                }
            } catch (Exception ex) {
                log.warn("subclasscnt_num error!", ex);
            }
        }


        /**
         *  科目別および全科目の平均・合計累積処理
         *      学期および学年成績において ( param._testitemcd=='0' ) 評定読替科目は含めない
                                                                (SQLのSELECTで'REPLACEMOTO'がマイナスなら評定読替科目)
         *  2005/03/09
         */
//        void subclasscnt_num(final ResultSet rs) {
    //
//            try {
//                if (rs.getString("SCORE") != null  &&
//                    ! rs.getString("SCORE").equals("KK")  &&
//                    ! rs.getString("SCORE").equals("KS")  &&
//                    ! rs.getString("SCORE").equals("( )") &&
//                    ! rs.getString("SCORE").equals("欠")  &&
//                    ! rs.getString("SCORE").equals("公")       )
//                {
//                        subclasstotalnum += Integer.parseInt( rs.getString("SCORE") );
//                        subclasstotalcnt ++;
    //
//                        if (param._testitemcd.equals("0")) {
//                            if (0 <= rs.getInt("REPLACEMOTO")) {
//                                hrtotalnum += Integer.parseInt( rs.getString("SCORE") );
//                                hrtotalcnt ++;
//                            }
//                        } else{
//                            hrtotalnum += Integer.parseInt( rs.getString("SCORE") );
//                            hrtotalcnt ++;
//                        }
//                }
//            } catch (Exception ex) {
//                log.warn("term1 svf-out error!", ex);
//            }
//        }


        /**
         *  科目別平均・合計
         *  2005/03/09
         *  2005/06/07
         */
        void printSvfsubclasscnt_num(final int subclass1, final boolean blast, final Map subclassCountMap, final Map accumulateScoreMap) {
//          log.debug("subclass1="+subclass1);
//          log.debug("subclasstotalnum="+subclasstotalnum);
//          log.debug("subclasstotalcnt="+subclasstotalcnt);

            Integer inti = null;
            int intj = 0;
            int intk = 0;

            try {
                if (0 < subclasstotalcnt) {
                    inti = (Integer)subclassCountMap.get( String.valueOf( subclass1 ) );
                    if (inti != null )intj = inti.intValue();
                    //log.debug("1  intj="+intj+"   inti="+inti+ "  subclasstotalcnt="+subclasstotalcnt);
                    intj += subclasstotalcnt;
                    subclassCountMap.remove( String.valueOf( subclass1 ) );
                    subclassCountMap.put( String.valueOf( subclass1 ), new Integer( intj ) );

                    intj = 0;
                    inti = (Integer)accumulateScoreMap.get( String.valueOf( subclass1 ) );
                    if (inti != null )intj = inti.intValue();
                    intj += subclasstotalnum;
                    accumulateScoreMap.remove( String.valueOf( subclass1 ) );
                    accumulateScoreMap.put( String.valueOf( subclass1 ), new Integer( intj ) );
                }
            } catch (Exception ex) {
                log.warn("term1 svf-out error!", ex);
            }

            if (blast )
                try {
                    intj = 0;
                    intk = 0;
                    inti = (Integer)subclassCountMap.get( String.valueOf( subclass1 ) );
                    if (inti != null) intj = inti.intValue();
                    inti = (Integer)accumulateScoreMap.get( String.valueOf( subclass1 ) );
                    if (inti != null) intk = inti.intValue();
                    if (0 < intj) {
                        svf.VrsOutn("TOTAL_SUBCLASS", subclasslinecount+1, String.valueOf( intk ) );  //ＨＲ合計 04/11/03Add 04/11/08Modify
                        //svf.VrsOutn("AVE_CLASS",      subclasslinecount+1, String.valueOf( Math.round( (float)intk / (float)intj ) ) ); //ＨＲ平均             04/11/08Modify
                        svf.VrsOutn("AVE_CLASS",      subclasslinecount+1, String.valueOf( (float)Math.round( (float)intk / (float)intj * 10 ) / 10 ) );  //ＨＲ平均 05/09/28Modify
                    }
                } catch (Exception ex) {
                    log.warn("term1 svf-out error!", ex);
                }
        }


        /**
         *  2005/01/31 履修単位を加算する処理
         *     科目欄の単位数の合計をクラス履修単位とする
         *        => 体育等の扱いを考慮し取り敢えず群コードが０または連続しない群コードの単位を集計する
         * NO022 Delete
        void addSubclassCredits( ResultSet rs, int group, int replaceflg ) {
        //NO007void addSubclassCredits( ResultSet rs, int group ) {

            try {
    //if (rs.getString("SCHREGNO").equals("20051003") )
    //log.debug("kamoku="+rs.getString("SUBCLASSCD") + "    credit="+rs.getString("CREDITS"));
                if (rs.getString("CREDITS") == null ) return;
                if (0 < replaceflg ) return;  //NO007

                if (Integer.parseInt( rs.getString("GROUPCD") ) == 0 )
                    hr_credits += Integer.parseInt( rs.getString("CREDITS") );  //単位数
                else if (Integer.parseInt( rs.getString("GROUPCD") ) != group )
                    hr_credits += Integer.parseInt( rs.getString("CREDITS") );  //単位数
                    
            } catch (Exception ex) {
                log.warn("error! ", ex);
            }
        }*/


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final Map schregnoLineMap, final int assesspattern, final int replaceflg) {   //NO007Modify
        }


        /** 
         *  明細出力 
         *  生徒の科目別欠課時数を出力する
         */
        boolean printSvfStdDetailOutAbsence (
                final ResultSet rs,
                final int i,
                final int j
        ) {
            boolean amikake = false;
            final String field;
            if (definecode.absent_cov == 3 || definecode.absent_cov == 4) {
                field = "kekka2_";
            } else {
                field = "kekka";
            }

            try {
                
                if (rs.getString("ABSENT1") != null  &&  Float.parseFloat(rs.getString("ABSENT1")) != 0) {
                    float absence_high = 0;
                    if (rs.getString("ABSENCE_HIGH") != null) absence_high = rs.getFloat("ABSENCE_HIGH");
                    if (0 < absence_high  &&  rs.getString("ABSENT1") != null  &&  absence_high < Float.parseFloat(rs.getString("ABSENT1"))) {
                        amikake = true;
                    }
                    if (amikake) {
                        svf.VrAttributen(field + i, j, "Paint=(2,70,1),Bold=1");
                    }
                    svf.VrsOutn(field + i, j, rs.getString("ABSENT1"));  // 欠課
                    if ( amikake ) {
                        svf.VrAttributen(field + i, j, "Paint=(0,0,0),Bold=0");  // 網掛けクリア
                    }
                }
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
            return amikake;
        }


        /**
         *  累計出力処理 
         */
        boolean printSvfStdTotalRec(final Map schregnoLineMap, final PreparedStatement arrps[])
        {
            boolean nonedata = false;
            ResultSet rs = null;
            //for(int ia=0 ; ia<param.length ; ia++)log.trace("param["+ia+"]=" + param[ia]);

            try {
                //log.debug("arrps[1]="+arrps[1].toString());           
                int pp = 0;
                arrps[1].setString( ++pp,  param._hrclass );                      //学年・組
                arrps[1].setString( ++pp,  param._hrclass );                      //学年・組 NO024
                log.debug("prestatStdTotalRec executeQuery() start");
                rs = arrps[1].executeQuery();                               //成績累計データのRecordSet
                log.debug("prestatStdTotalRec executeQuery() end");

                while ( rs.next()) {
                    //明細データの出力
                    printSvfStdTotalRecOut(rs, schregnoLineMap);
                }
            } catch (Exception ex ) {
                log.warn("ResultSet-read error!", ex);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(rs);;
            }

            return nonedata;

        }//printSvfStdTotalRec()の括り


        /**
         *  累計出力処理 
         *      2005/01/04 SQL変更に伴い修正
         *      NO014により変更
         */
        boolean printSvfStdTotalAttend(final Map schregnoLineMap, final PreparedStatement arrps[])
        {
            boolean nonedata = false;
            ResultSet rs = null;
            int p = 0;

            try {
                for (final Iterator i = schregnoLineMap.keySet().iterator( ); i.hasNext( );) {
                    p = 0;
                    arrps[4].setString( ++p, (String)i.next() );        //学籍番号
                    arrps[4].setString( ++p, param._hrclass.substring(2, 5)); //組
                    //log.trace("prestatStdTotalAttend executeQuery() start ");
                    rs = arrps[4].executeQuery();                               //出欠累計データのRecordSet
                    //log.trace("prestatStdTotalAttend executeQuery() end ");
                    if (rs.next() )printSvfStdTotalAttendOut(rs,schregnoLineMap);  //明細データの出力
                }
            } catch (Exception ex ) {
                log.warn("ResultSet-read error!", ex);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(rs);
            }

            return nonedata;

        }//printSvfStdTotalAttend()の括り

        /** 
         *  明細出力 
         */
        void printSvfStdTotalRecOut(final ResultSet rs, final Map schregnoLineMap)
        {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)schregnoLineMap.get(rs.getString("SCHREGNO"));
                if (int1 == null) return;

                if (rs.getString("SUM_REC")!=null) {
                    svf.VrsOut("TOTAL" + int1.intValue(), rs.getString("SUM_REC"));           //総合点
                    hr_total += rs.getInt("SUM_REC");
                    hr_seitosu[8]++;
                }

                if (rs.getString("AVG_REC")!=null) {
                    //svf.VrsOut("AVERAGE" + int1.intValue(), String.valueOf(Math.round(rs.getFloat("AVG_REC"))));    //平均点 04/11/08Modify
                    svf.VrsOut("AVERAGE" + int1.intValue(), String.valueOf((float)Math.round(rs.getFloat("AVG_REC")*10)/10)); //平均点 05/09/28Modify
                    //log.debug("rank="+rs.getString("RANK")+"  average="+rs.getFloat("AVG_REC") + "    " +(float)Math.round(rs.getFloat("AVG_REC")*10)/10);
                    //if (hr_average == 0) {      // 04/11/08Modify
                    //    hr_average = rs.getInt("TOTAL_REC_AVG");
                    //    hr_seitosu[9] = rs.getInt("TOTAL_REC_SUM");
                    //}
                }

                if (rs.getString("RANK")!=null )
                    svf.VrsOut("RANK" + int1.intValue(), rs.getString("RANK"));               //順位

                // 04/12/16処理を止めておく  05/01/31Modify 学年末の処理で、３年生は2学期、以外の学年は3学期に出力
                if (param._semester.equals("9")  &&  0 < sch_rcredits[ int1.intValue() ] )
                    svf.VrsOut("R_CREDIT" + int1.intValue(), String.valueOf( sch_rcredits[ int1.intValue() ] ) ); //履修単位数
                if (param._semester.equals("9")  &&  0 < sch_ccredits[ int1.intValue() ] )
                //NO010 if (param._semester.equals("9")  &&  0 < sch_rcredits[ int1.intValue() ] )
                    svf.VrsOut("C_CREDIT" + int1.intValue(), String.valueOf( sch_ccredits[ int1.intValue() ] ) ); //修得単位数
            } catch (Exception ex) {
                log.warn("total svf-out error!", ex);
            }

        }//printSvfStdTotalRecOut()の括り



        /** 
         *  出欠の記録 明細出力 
         *      2005/01/04 SQL変更に伴い修正
         *      2005/02/01 出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。
         *      2009/02/26 出欠端数計算共通関数用に変更
         */
        void printSvfStdTotalAttendOut(final ResultSet rs, final Map schregnoLineMap)
        {
            try {
                //int intx = 0;               //出席日数のカウント
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)schregnoLineMap.get(rs.getString("SCHREGNO"));
                if (int1 == null) {
                    return;
                }
                //if (rs.getString("SCHREGNO").equals("20045094") )
                //log.debug("lesson="+rs.getString("LESSON"));
                if (rs.getString("LESSON") != null  &&  rs.getInt("LESSON") != 0) {
                    //intx = rs.getInt("LESSON");
                    if (hr_lesson[0] < rs.getInt("LESSON") ) 
                        hr_lesson[0] = rs.getInt("LESSON");                 //最大授業日数 04/11/01Add
                    if (hr_lesson[1] == 0 )                  
                        hr_lesson[1] = rs.getInt("LESSON");
                    else if (rs.getInt("LESSON") < hr_lesson[1] ) 
                        hr_lesson[1] = rs.getInt("LESSON");                 //最小授業日数 04/11/01Add
                }

                if (rs.getString("SUSPEND") != null || "true".equals(param._useVirus) && rs.getString("VIRUS") != null || "true".equals(param._useKoudome) && rs.getString("KOUDOME") != null) {
                    int value = 0;
                    boolean hasvalue = false;
                    if (rs.getString("SUSPEND") != null) {
                        if (rs.getInt("SUSPEND") != 0) {
                            hasvalue = true;
                            value += rs.getInt("SUSPEND");
                        }
                    }
                    if ("true".equals(param._useVirus) && rs.getString("VIRUS") != null) {
                        if (rs.getInt("VIRUS") != 0) {
                            hasvalue = true;
                            value += rs.getInt("VIRUS");
                        }
                    }
                    if ("true".equals(param._useKoudome) && rs.getString("KOUDOME") != null) {
                        if (rs.getInt("KOUDOME") != 0) {
                            hasvalue = true;
                            value += rs.getInt("KOUDOME");
                        }
                    }

                    hr_attend[0] += value;         //合計 05/02/01Modify
                    if (hasvalue) {
                        svf.VrsOut( "SUSPEND" + int1.intValue(), String.valueOf(value) );   //出停
                    }
                    hr_seitosu[0]++;                                  //生徒数 05/02/01Modify
                }

                if (rs.getString("MOURNING") != null) {
                    if (rs.getInt("MOURNING") != 0) {
                        //intx -= rs.getInt("MOURNING");
                        svf.VrsOut( "KIBIKI" + int1.intValue(), rs.getString("MOURNING") );   //忌引
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[1] += rs.getInt("MOURNING");        //合計 05/02/01Modify
                        hr_attend[1] += rs.getInt("MOURNING");        //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
                }

                if (rs.getString("MLESSON") != null) {
                    if (rs.getInt("MLESSON") != 0) {
                        svf.VrsOut( "PRESENT" + int1.intValue(), rs.getString("MLESSON") );   //出席すべき日数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[3] += rs.getInt("MLESSON");         //合計 05/02/01Modify
                        hr_attend[3] += rs.getInt("MLESSON");         //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
                }

    /* ******************
                if (0 < intx) {
                    svf.VrsOut( "PRESENT" + int1.intValue(), String.valueOf(intx) );          //出席すべき日数
                    if (rs.getInt("LEAVE") == 0 ) hr_attend[3] += intx;                             //合計 05/02/01Modify
                    if (rs.getInt("LEAVE") == 0 ) hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
                }
    ******************** */

                if (rs.getString("SICK") != null) {
                    if (rs.getInt("SICK") != 0) {
                        //intx -= rs.getInt("SICK");
                        svf.VrsOut( "ABSENCE" + int1.intValue(), rs.getString("SICK") );    //欠席日数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[4] += rs.getInt("SICK");          //合計 05/02/01Modify
                        hr_attend[4] += rs.getInt("SICK");          //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
                }

                if (rs.getString("PRESENT") != null) {
                    if (rs.getInt("PRESENT") != 0) {
                        svf.VrsOut( "ATTEND" + int1.intValue(), rs.getString("PRESENT") );    //出席日数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[5] += rs.getInt("PRESENT");         //合計 05/02/01Modify
                        hr_attend[5] += rs.getInt("PRESENT");         //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
                }

    /* *******************
                if (0 < intx) {
                    svf.VrsOut( "ATTEND" + int1.intValue(), String.valueOf(intx) );           //出席日数
                    if (rs.getInt("LEAVE") == 0 ) hr_attend[5] += intx;                             //合計 05/02/01Modify
                    if (rs.getInt("LEAVE") == 0 ) hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
                }
    ********************* */
                if (rs.getString("EARLY") != null) {
                    if (rs.getInt("EARLY") != 0) {
                        svf.VrsOut( "LEAVE" + int1.intValue(), rs.getString("EARLY") );       //早退回数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[6] += rs.getInt("EARLY");           //合計 05/02/01Modify
                        hr_attend[6] += rs.getInt("EARLY");           //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
                }

                if (rs.getString("LATE") != null) {
                    if (rs.getInt("LATE") != 0) {
                        svf.VrsOut( "TOTAL_LATE" + int1.intValue(), rs.getString("LATE") );   //遅刻回数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[7] += rs.getInt("LATE");            //合計 05/02/01Modify
                        hr_attend[7] += rs.getInt("LATE");            //合計 05/02/01Modify
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
                    hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
                }

                if (rs.getString("TRANSFER_DATE") != null) {  // 05/02/02ココへ移動
                    if (rs.getInt("TRANSFER_DATE") != 0) {
                        svf.VrsOut("ABROAD" + int1.intValue(), rs.getString("TRANSFER_DATE"));    //留学日数
                        //if (rs.getInt("LEAVE") == 0 ) hr_attend[2] += rs.getInt("TRANSFER_DATE");
                        hr_attend[2] += rs.getInt("TRANSFER_DATE");
                    }
                    //if (rs.getInt("LEAVE") == 0 ) hr_seitosu[2]++;
                    hr_seitosu[2]++;
                }

            } catch (Exception ex) {
                log.warn("total svf-out error!", ex);
            }

        }//printSvfStdTotalAttendOut()の括り



        /** 
         *  クラス総合・平均を出力
         *      総合点から遅刻回数
         */
        void printSvfTotalOut(final boolean blast)
        {
            try {

                if (0 < hr_seitosu[8])
                    svf.VrsOut( "TOTAL51",    String.valueOf( Math.round( (float)hr_total / hr_seitosu[8] ) ) );  //総合点 04/11/10Modify => 小数点第１位で四捨五入
                log.debug("hrtotalnum="+hrtotalnum+"  hrtotalcnt="+hrtotalcnt);
                //if (0 < hr_seitosu[9]) {
                if (0 < hrtotalcnt) {
                    //svf.VrsOut( "AVERAGE51", String.valueOf( hr_average ) );        //平均点 04/11/04Modify 04/11/08Modify
                    //svf.VrsOut( "TOTAL53",   String.valueOf( hr_seitosu[9] ) ); //総合点 04/11/08Add
                    svf.VrsOut( "AVERAGE51", String.valueOf( Math.round( (float)hrtotalnum / hrtotalcnt )) );     //平均点 04/11/04Modify 04/11/08Modify 05/03/10Moidfy
                    svf.VrsOut( "TOTAL53",   String.valueOf( hrtotalnum ) );  //総合点 04/11/08Add 05/03/10Modify
                }

                if (0 < hr_seitosu[0] ) svf.VrsOut( "SUSPEND53", String.valueOf( hr_attend[0] ) );    //出停
                if (0 < hr_seitosu[1] ) svf.VrsOut( "KIBIKI53",  String.valueOf( hr_attend[1] ) );    //忌引
                if (0 < hr_seitosu[3] ) svf.VrsOut( "PRESENT53", String.valueOf( hr_attend[3] ) );    //出席すべき日数
                if (0 < hr_seitosu[4] ) svf.VrsOut( "ABSENCE53", String.valueOf( hr_attend[4] ) );    //欠席日数
                if (0 < hr_seitosu[5] ) svf.VrsOut( "ATTEND53",  String.valueOf( hr_attend[5] ) );    //出席日数
                if (0 < hr_seitosu[6] ) svf.VrsOut( "LEAVE53",   String.valueOf( hr_attend[6] ) );    //早退回数
                if (0 < hr_seitosu[7] ) svf.VrsOut( "TOTAL_LATE53",    String.valueOf( hr_attend[7] ) );  //遅刻回数

                if (0 < hr_attend[3]) {
                    svf.VrsOut( "PER_ATTEND",  String.valueOf( (float)Math.round( ((float)hr_attend[5] / (float)hr_attend[3]) * 1000 ) / 10 ) );  //出席率
                    svf.VrsOut( "PER_ABSENCE", String.valueOf( (float)Math.round( ((float)hr_attend[4] / (float)hr_attend[3]) * 1000 ) / 10 ) );  //欠席率
                }

                // 05/01/31Modify  学年末の処理で単位の合計を出力
                //NO013 if (param._semester.equals("9") &&  0 < hr_credits )
                //NO013     svf.VrsOut( "credit20", hr_credits + "単位" );
                //NO022 if (param._semester.equals("9") )svf.VrsOut( "credit20", "---" );  //NO013
                getRCreditsHr();  //NO022
                if (param._semester.equals("9")) svf.VrsOut( "credit20", hr_credits + "単位" );  //NO022

                // 05/01/31Modify  授業日数最大値を出力 => 出欠統計の仕様に合わせる
                // NO013Delete  NO021Revival
                if (0 < hr_lesson[0] )       //授業日数最大値があれば出力 04/11/01Add
                    svf.VrsOut( "lesson20", hr_lesson[0] + "日" );
                // NO021 svf.VrsOut( "lesson20", "---" );  //NO013

            } catch (Exception ex) {
                log.warn("group-average svf-out error!", ex);
            }

        }//printSvfTotalOut()の括り


        /** 
         * SQLStatement作成 ＨＲクラス生徒名の表(生徒名) 04/11/08Modify 
         *   SEMESTER_MATはparam._semesterで検索 => 学年末'9'有り
         *   SCHREG_REGD_DATはparam._semeFlgで検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        String prestatStdNameList() {

            StringBuffer stb = new StringBuffer();
            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W3.NAME,");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");

            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.SEMESTER = '" + param._semester + "' AND W1.YEAR = W2.YEAR ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");
            if (!param._semester.equals("9")) {
                stb.append(    "AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append(    "AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append("ORDER BY W1.ATTENDNO");
            return stb.toString();
        }


        /** 
         *  SQLStatement作成 ＨＲ履修科目の表(教科名・科目名・単位・授業時数・平均) 
         *  2005/02/02 学年末では評価読替え科目を出力する
         *  2005/05/22 科目名の略称を表記
         *  2005/08/17 時数取得の仕方を変更するとともに、対象科目取得の表(CHAIR_A)を簡略化
         */
        String prestatSubclassInfo() {

            StringBuffer stb = new StringBuffer();
            //学籍の表 04/11/08Add
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.YEAR, W1.SEMESTER, W1.SCHREGNO, W1.ATTENDNO,");
            stb.append(             "W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append(     "FROM   SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //05/09/30Build
            stb.append(     "WHERE   W1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals("9")) {
                stb.append(     "AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append(     "AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30

            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            stb.append(     ") ");

            //講座の表 05/01/31Modify 
            //05/08/17Modify 時間割の存在チェックを外し学期の条件を加え、科目・群別の表に変更
            //05/08/18Modify 科目の表に変更
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        K2.SUBCLASSCD AS SUBCLASSCD,MAX(K2.GROUPCD)AS GROUPCD ");
            stb.append(     "FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(     "WHERE   K1.YEAR = K2.YEAR ");
            stb.append(         "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(         "AND (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
            stb.append(         "AND K1.YEAR = '" + param._year + "' ");
            stb.append(         "AND K2.YEAR = '" + param._year + "' ");
            stb.append(         "AND K1.TRGTGRADE||K1.TRGTCLASS = ? ");  //05/05/30
            stb.append(         "AND K1.GROUPCD = K2.GROUPCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         "AND (K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(         "AND (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            if (! param._semester.equals("9")) {
                stb.append(     "AND K1.SEMESTER = '" + param._semester + "' ");
                stb.append(     "AND K2.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(     "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND , ");
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(         " K2.SUBCLASSCD ");
            stb.append(     ")");

            //評価読替前科目の表 NO008 Build
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(               "T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(               "ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO008 Build
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND , ");
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "GRADING_SUBCLASSCD ");
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDIT_MST T1 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T1.GRADE = T2.GRADE ");
            stb.append(                       "AND T1.COURSECD = T2.COURSECD ");
            stb.append(                       "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(                       "AND T1.COURSECODE = T2.COURSECODE) ");
            stb.append(         "AND VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(         "AND NOT EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(         " T1.SUBCLASSCD) ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT W2.SUBCLASSCD,W2.GROUPCD,W4.SUBCLASSABBV AS SUBCLASSNAME,W4.ELECTDIV,W5.CLASSABBV,");
            stb.append(       "W7.MAXCREDITS,W7.MINCREDITS,");
            stb.append(       "W9.AVG_HR,W9.SUM_HR,");    // 04/11/08Add
            stb.append(       "VALUE(W6.JISU,0) + VALUE(W10.LESSON,0) AS JISU ");
            stb.append(      ",CASE WHEN W11.SUBCLASSCD IS NOT NULL THEN 'MOTO' WHEN W12.SUBCLASSCD IS NOT NULL THEN 'SAKI' ELSE NULL END AS REPLACEFLG ");   //NO007Build / NO008Modify
            stb.append("FROM(");
            stb.append(    "SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    CLASSCDS, ");
            }
            stb.append(        " SUBCLASSCD, GROUPCD ");
            stb.append(        "FROM    CHAIR_A w1 ");
            stb.append(        "WHERE   NOT EXISTS( SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE W2.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO005
            //評価読替え科目を追加 05/02/02
            if (param._semester.equals("9")) {
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    CLASSCDS, ");
                }
                stb.append(    " SUBCLASSCD, '0000' AS GROUPCD FROM REPLACE_REC_SAKI ");
            }
                //NO010
            stb.append("UNION ALL ");
            stb.append("SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCDS, ");
            }
            stb.append("        W1.SUBCLASSCD, '0000' AS GROUPCD ");
            stb.append("FROM    CREDITS_UNCONDITION W1 ");

            stb.append(    ") W2 ");

            stb.append(    "INNER JOIN SUBCLASS_MST W4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || ");
            }
            stb.append(                " W4.SUBCLASSCD = W2.SUBCLASSCD ");
            stb.append(    "INNER JOIN CLASS_MST W5 ON W5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    || '-' || W5.SCHOOL_KIND ");
            }
            stb.append(               " = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W2.CLASSCDS ");
            } else {
                stb.append("    SUBSTR(W2.SUBCLASSCD,1,2)");
            }

            //授業時数(集計漏れ)の表 05/08/17Modify 生徒毎の時数のMAX値をとる
            stb.append(    "LEFT JOIN( ");
            stb.append(       "SELECT  SUBCLASSCD, MAX(JISU) AS JISU ");
            stb.append(       "FROM(   SELECT  T2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "T3.SUBCLASSCD AS SUBCLASSCD,COUNT(*) AS JISU ");
            stb.append(               "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2,CHAIR_DAT T3 ");
            stb.append(               "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN ? AND ? ");
            stb.append(                   "AND T1.YEAR = T2.YEAR ");
            stb.append(                   "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                   "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                   "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(                   "AND T3.YEAR='" + param._year + "' ");
            stb.append(                   "AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(                   "AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(               "GROUP BY T2.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                        "T3.SUBCLASSCD ");
            stb.append(           ")T1 ");
            stb.append(       "GROUP BY SUBCLASSCD ");
            stb.append(    ")W6 ON W6.SUBCLASSCD = W2.SUBCLASSCD ");

            //授業時数(集計済)の表 05/08/17Modify 生徒毎の時数のMAX値をとる => 元は月毎のMAX値を集計
            stb.append(    "LEFT JOIN(");
            stb.append(        "SELECT  SUBCLASSCD,");
            stb.append(                "MAX(LESSON) AS LESSON ");
            stb.append(        "FROM(");
            stb.append(             "SELECT  schregno, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                      "W1.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                     "SUM(LESSON) AS LESSON ");
            stb.append(             "FROM    ATTEND_SUBCLASS_DAT W1 ");
            stb.append(             "WHERE   YEAR = '" + param._year + "' AND ");
            if (! param._semester.equals("9") )
                stb.append(                 "SEMESTER = '" + param._semester + "' AND ");
            stb.append(                     "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= ? AND ");   //--NO004 NO007
            stb.append(                     "EXISTS(");
            stb.append(                            "SELECT  'X' ");
            stb.append(                            "FROM    SCHNO W2 ");
            stb.append(                            "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(             "GROUP BY schregno, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                     " W1.SUBCLASSCD ");
            stb.append(             ")S1 ");
            stb.append(        "GROUP BY SUBCLASSCD ");
            stb.append(    ")W10 ON W10.SUBCLASSCD = W2.SUBCLASSCD ");

            //単位の表
            stb.append(    "LEFT JOIN( ");
            stb.append(       "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "W1.SUBCLASSCD AS SUBCLASSCD,MAX(CREDITS) AS MAXCREDITS, MIN(CREDITS) AS MINCREDITS ");
            stb.append(       "FROM    CREDIT_MST W1 ");
            stb.append(       "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(               "W1.GRADE = '" + param._grade + "' AND ");   //05/05/22
            stb.append(               "(W1.COURSECD, W1.MAJORCD, W1.COURSECODE) ");
            stb.append(                   "IN(SELECT COURSECD, MAJORCD, COURSECODE ");
            stb.append(                      "FROM   SCHREG_REGD_DAT ");
            stb.append(                      "WHERE  YEAR = '" + param._year + "' AND ");
            stb.append(                             "GRADE||HR_CLASS = ? AND ");  //05/05/30
            if (!param._semester.equals("9")) {
                stb.append(                         "SEMESTER = '" + param._semester + "') ");
            } else {
                stb.append(                         "SEMESTER = '" + param._semeFlg + "') ");
            }
            stb.append(       "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "W1.SUBCLASSCD");
            stb.append(    ")W7 ON W7.SUBCLASSCD = W2.SUBCLASSCD ");
            //科目別合計点および平均点の表 04/11/08Add
            stb.append(    "LEFT JOIN( ");
            stb.append(        "SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                W1.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                "INT(ROUND(AVG(FLOAT(" + fieldname + ")),0))AS AVG_HR,");
            stb.append(                "SUM(" + fieldname + ")AS SUM_HR ");
            stb.append(         "FROM   RECORD_DAT W1 ");
            stb.append(         "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(         "WHERE  W1.YEAR = '" + param._year + "' AND W1.SUBCLASSCD <= '" + SUBJECT_U + "' AND ");
            stb.append(                 fieldname + " IS NOT NULL ");
            stb.append(         "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                " W1.SUBCLASSCD");
            stb.append(    ")W9 ON W9.SUBCLASSCD = W2.SUBCLASSCD ");
            //評価読替前科目の表 NO007 Build / NO008 Modify
            stb.append(    "LEFT JOIN REPLACE_REC_MOTO W11 ON W11.SUBCLASSCD = W2.SUBCLASSCD ");
            //評価読替後科目の表 NO007 Build / NO008 Modify
            stb.append(    "LEFT JOIN REPLACE_REC_SAKI W12 ON W12.SUBCLASSCD = W2.SUBCLASSCD ");

            stb.append("ORDER BY W2.SUBCLASSCD ");
            return stb.toString();

        }


        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表 
         *  2005/06/20 Modify ペナルティ欠課の算出式を修正
         * @param absentDiv 1:年間 2:随時
         */
        String prestatStdSubclassDetail(final String absentDiv) {

            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append("SCHNO_A AS(");
            if (param._semester.equals("9")) {
                stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
                stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");  //NO010
                stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
                stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
                stb.append(             "W1.SEMESTER = '" + param._semeFlg + "'AND ");
                stb.append(             "W1.GRADE||W1.HR_CLASS = ? AND ");
                stb.append(             "W1.ATTENDNO BETWEEN ? AND ? ");
            } else {
                stb.append(" SELECT  W1.SCHREGNO,W1.SEMESTER, W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE");
                stb.append(" FROM    SCHREG_REGD_DAT W1");
                stb.append(" INNER   JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = '" + param._semester + "'");
                stb.append(" WHERE   W1.YEAR = '" + param._year + "' AND W1.SEMESTER = '" + param._semester + "'");
                stb.append("     AND W1.GRADE||W1.HR_CLASS = ? AND W1.ATTENDNO BETWEEN ? AND ? ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE)");
            }
            stb.append(     ") ");

            //対象生徒の表 クラスの生徒から異動者を除外
            stb.append(",SCHNO_B AS(");
            stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");  //NO010
            stb.append(     "FROM    SCHNO_A W1 ");
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //05/09/30Build

            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            stb.append(     ") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT K1.SCHREGNO, K2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "K2.SUBCLASSCD AS SUBCLASSCD, K2.SEMESTER, K1.APPDATE, K1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT K1, CHAIR_DAT K2 ");
            stb.append(     "WHERE  K1.YEAR = '" + param._year + "' ");
            stb.append(        "AND K1.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND K2.YEAR = K1.YEAR ");
            stb.append(        "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(        "AND (K1.CHAIRCD = K2.CHAIRCD OR ");
            stb.append(            " K1.CHAIRCD = '000000') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(         "AND (K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(         "AND (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = K1.SCHREGNO)");
            stb.append(     ")");

            stb.append(" ,TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(" ) ");

            // 時間割(休学・留学を含む)
            stb.append(", SCHEDULE_SCHREG_R AS( ");
            stb.append(    "SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM   SCH_CHR_DAT T1, CHAIR_A T2 ");
            stb.append(    "WHERE  T1.EXECUTEDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(        "AND T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                         "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                           "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = '" + param._hrclass + "' AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append("GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 時間割(休学・留学を含まない)
            stb.append(", SCHEDULE_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 休学時数
            stb.append(", OFFDAYS_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER ");
            stb.append(")");
            
            //欠課数の表
            stb.append(",ATTEND_A AS(");
            //出欠データより集計
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append(                                   ",'1','8'");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(                                   ",'2','9'");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(                                   ",'3','10'");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(_knjSchoolMst._subVirus)) {
                    stb.append(                                   ",'19','20'");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(_knjSchoolMst._subKoudome)) {
                    stb.append(                                   ",'25','26'");
                }
            }
            stb.append(                                        ") THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM SCHEDULE_SCHREG S1 "); // 休学時数、留学時数を含まない
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append("               LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = S2.DI_CD ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(      "UNION ALL ");
            stb.append(      "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append(           ",SUM(T2.OFFDAYS) AS JISU ");   // 授業時数に休学時数を減算する
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 "); // 欠課時数に休学時数を加算する
            } else {
                stb.append(           ",SUM(0) AS JISU ");
                stb.append(           ",SUM(0) AS ABSENT1 ");
            }
            stb.append(           ",SUM(0) AS LATE_EARLY ");
            stb.append(      "FROM OFFDAYS_SCHREG T2 ");
            stb.append(      "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
                                  //月別科目別出欠集計データより欠課を取得
            stb.append(          "UNION ALL ");
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) ");
            if (!"1".equals(_knjSchoolMst._subOffDays)) { 
                stb.append(                  " - VALUE(OFFDAYS,0) ");
            }
            stb.append(                  " - VALUE(ABROAD,0) ) AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_knjSchoolMst._subAbsent))  {
                stb.append(                   "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(                   "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(                   "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_knjSchoolMst._subOffDays)) { 
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(_knjSchoolMst._subVirus)) { 
                    stb.append(                   "+ VALUE(VIRUS,0) ");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(_knjSchoolMst._subKoudome)) { 
                    stb.append(                   "+ VALUE(KOUDOME,0) ");
                }
            }
            stb.append(                    "   ) AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(              "W1.SEMESTER <= '" + param._semester + "' AND ");

            stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (param._divideAttendMonth ) + "' AND ");   //--NO004 NO007
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                   W1.SUBCLASSCD, W1.SEMESTER ");
            stb.append(     ") ");

            //欠課数の表
            stb.append(",ATTEND_B AS(");
            stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");
            if (definecode.absent_cov == 1 || definecode.absent_cov == 3) {
                //遅刻・早退を学期で欠課換算する
                stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, ");
                    if (definecode.absent_cov == 1 || param._semester.equals("9")) {
                    stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                } else {
                    stb.append(                 "DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + definecode.absent_cov_late + ",5,1) AS ABSENT1 ");   //05/06/20Modify
                }
                stb.append(             "FROM    ATTEND_A ");
                stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append(             ")W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (definecode.absent_cov == 2 || definecode.absent_cov == 4) {
                //遅刻・早退を年間で欠課換算する
                if (definecode.absent_cov == 2) {
                    stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                } else {
                    stb.append(       ", DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + definecode.absent_cov_late + ",5,1) AS ABSENT1 ");   //05/06/20Modify
                }
                stb.append(     "FROM    ATTEND_A ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                //遅刻・早退を欠課換算しない
                stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM    ATTEND_A W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  T2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("      T1.SUBCLASSCD AS SUBCLASSCD, T1.CREDITS, T1.COMP_UNCONDITION_FLG ");
            stb.append(    "FROM    CREDIT_MST T1 ");
            stb.append(     "INNER JOIN SCHNO_A T2 ON ");
            stb.append(        "T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(") ");

            // 欠課数上限の表
            stb.append(",T_ABSENCE_HIGH AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
            if (_knjSchoolMst.isJitu()) {
                stb.append(        " VALUE(T5.COMP_ABSENCE_HIGH, 99) ");
            } else {
                stb.append(        " VALUE(T1.ABSENCE_HIGH, 99) ");
            }
            stb.append(    "AS ABSENCE_HIGH ");
            stb.append(    "FROM ");
            if (_knjSchoolMst.isJitu()) {
                stb.append(    "SCHREG_ABSENCE_HIGH_DAT T5 ");
                stb.append(    "WHERE T5.YEAR = '" + param._year + "' ");
                stb.append(      "AND T5.DIV = '" + absentDiv + "' ");
            } else {
                stb.append(    "CREDIT_MST T1 ");
                stb.append(      "INNER JOIN SCHNO_A T2 ON ");
                stb.append(          "T1.GRADE = T2.GRADE ");
                stb.append(          "AND T1.COURSECD = T2.COURSECD ");
                stb.append(          "AND T1.MAJORCD = T2.MAJORCD ");
                stb.append(          "AND T1.COURSECODE = T2.COURSECODE ");
                stb.append(    "WHERE T1.YEAR = '" + param._year + "' ");
            }
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
            if (! param._testcd.equals(GRAD_SEM_KIND)) {
                //中間・期末成績  NO024 Modify
                // fieldname:SEM?_XXXX_SCORE / fieldname2:SEM?_XXXX
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname2 + "_VALUE IS NOT NULL THEN RTRIM(CHAR(" + fieldname2 + "_VALUE)) ");
                stb.append(             "WHEN " + fieldname2 + "_VALUE_DI IS NOT NULL THEN " + fieldname2 + "_VALUE_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else if (Integer.parseInt( param._semester ) != 9) {
                // fieldname:SEM?_VALUE
                //学期成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else{
                //学年成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            }
            stb.append(    "FROM    RECORD_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append(            "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDITS_A T1 ");
            stb.append(    "WHERE   VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' FROM RECORD_REC T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO) ");
            stb.append(") ");

            //評価読替前科目の表 NO008 Build
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                                        T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!param._semester.equals("9")) {
                stb.append(                                                "AND T2.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(                      ")");
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO008 Build
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("             T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!param._semester.equals("9")) {
                stb.append(                                                "AND T2.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(                      ")");
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("         GRADING_SUBCLASSCD ");
            stb.append(     ") ");

            //評定読替え科目評定の表
            if (param._semester.equals("9")) {   //NO008
                stb.append(",REPLACE_REC AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                stb.append(            "SCORE, ");
                stb.append(            "PATTERN_ASSESS ");
                stb.append(            ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE  W1.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("              W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "W2.YEAR='" + param._year + "' AND ANNUAL='" + param._grade + "' AND REPLACECD='1' ");  //05/05/22
                stb.append(     ") ");
                stb.append(",REPLACE_REC_ATTEND AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append(     "FROM   RECORD_DAT W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE  W1.YEAR = '" + param._year + "' AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
                }
                stb.append(     "       W1.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("              W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
                stb.append(            "W2.YEAR='" + param._year + "' AND ANNUAL='" + param._grade + "' AND REPLACECD='1' ");
                stb.append(     ") ");
             }

            //メイン表
            stb.append("SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
            stb.append(       ",T5.ABSENT1 ");
            stb.append(       ",T3.SCORE ");
                                    //教科コード'90'も同様に評定をそのまま出力 NO015
            stb.append(       ",T3.PATTERN_ASSESS ");
            if (param._semester.equals("9") )  //NO008
                stb.append(   ",REPLACEMOTO ");
            stb.append(       ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T3.COMP_CREDIT END AS COMP_CREDIT ");  //NO0015  NO0018
            stb.append(       ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T3.GET_CREDIT END AS GET_CREDIT ");  //NO0015  NO0018
            stb.append(       ",VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG ");  //NO015
            stb.append(       ",T3.COMP_CREDIT AS ON_RECORD_COMP "); //NO0015 NO0018
            stb.append(       ",T3.GET_CREDIT AS ON_RECORD_GET "); //NO0015 NO0018
            stb.append(       ",T7.ABSENCE_HIGH ");

            //対象生徒・講座の表
            stb.append("FROM(");
            stb.append(     "SELECT  W1.SCHREGNO,W2.SUBCLASSCD ");
            stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
            stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(             "W1.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(             "W1.SEMESTER = W2.SEMESTER AND ");
            stb.append(             "W1.SCHREGNO = W3.SCHREGNO ");
            if (!param._semester.equals("9")) {
                stb.append(     "AND W2.SEMESTER = '" + param._semester + "' ");
            }
            if (!param._semester.equals("9")) {
                stb.append(         "AND NOT EXISTS( SELECT 'X' FROM REPLACE_REC_SAKI W4 WHERE W4.SUBCLASSCD = W2.SUBCLASSCD) ");
            }
            stb.append(     "GROUP BY W1.SCHREGNO,W2.SUBCLASSCD ");
            if (param._semester.equals("9")) {
                stb.append( "UNION   SELECT SCHREGNO,SUBCLASSCD ");
                stb.append( "FROM    REPLACE_REC_ATTEND ");
                stb.append( "GROUP BY SCHREGNO,SUBCLASSCD ");
            }
            //NO010
            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO, SUBCLASSCD ");
            stb.append(     "FROM    CREDITS_UNCONDITION S1 ");
            stb.append(")T1 ");

            //成績の表
            stb.append(  "LEFT JOIN(");
            //成績の表（通常科目）
            if (param._semester.equals("9")) {
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "(SELECT  COUNT(*) ");
                stb.append(             "FROM    REPLACE_REC S1 ");
                stb.append(             "WHERE   S1.SCHREGNO = W3.SCHREGNO AND ");
                stb.append(                     "S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append(             "GROUP BY ATTEND_SUBCLASSCD) AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            } else{
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "0 AS REPLACEMOTO ");
                stb.append(           ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
            }
            if (param._semester.equals("9")) {
                                //評定読替科目 成績の表 NO006
                stb.append(     "UNION ALL ");
                stb.append(     "SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
                stb.append(             "W3.SCORE AS SCORE, ");
                stb.append(             "W3.PATTERN_ASSESS, ");
                stb.append(             "-1 AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            }
            stb.append(     ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");

            //欠課数の表
            stb.append(  "LEFT JOIN(");
            stb.append(         "SELECT  SCHREGNO, SUBCLASSCD, ABSENT1 ");
            stb.append(         "FROM    ATTEND_B W1 ");
            if (param._semester.equals("9")) {
                                //評定読替科目 欠課数の表
                stb.append(     "UNION ");
                stb.append(     "SELECT  W1.SCHREGNO,W1.SUBCLASSCD,SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM    REPLACE_REC_ATTEND W1, ATTEND_B W2 ");
                stb.append(     "WHERE   W1.SCHREGNO = W2.SCHREGNO AND W1.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
                stb.append(         "AND NOT EXISTS(SELECT 'X' FROM ATTEND_B W3 WHERE W3.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO0017
                stb.append(     "GROUP BY W1.SCHREGNO,W1.SUBCLASSCD ");
            }
            stb.append(  ")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");

            stb.append("LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD ");  //NO010
            stb.append("LEFT JOIN T_ABSENCE_HIGH T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append("ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");
            return stb.toString();
        }

        /** 
         *  PrepareStatement作成 成績総合データ 
         */
        String prestatStdTotalRec()
        {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_B AS(");
            stb.append(     "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
            stb.append(             "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //05/09/30Build
            stb.append(     "WHERE   W1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals("9")) {
                stb.append(     "AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append(     "AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");          //05/05/30

            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            if (!param._semester.equals("9")) {
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE)");
            }
            stb.append(     ") ");

            //対象講座の表  NO024 
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append("       K2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(     "FROM   CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(     "WHERE  K1.YEAR = '" + param._year + "' AND ");
            stb.append(            "K2.YEAR = '" + param._year + "' AND ");
            if (!param._semester.equals("9")) {
                stb.append(        "K1.SEMESTER = '" + param._semester + "' AND ");
                stb.append(        "K2.SEMESTER = '" + param._semester + "' AND ");
            }
            stb.append(            "K1.YEAR = K2.YEAR AND ");
            stb.append(            "K1.SEMESTER = K2.SEMESTER AND ");
            stb.append(            "(K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) AND ");
            stb.append(            "K1.TRGTGRADE||K1.TRGTCLASS = ? AND ");
            stb.append(            "K1.GROUPCD = K2.GROUPCD AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "(K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' OR ");
                stb.append(             "K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(            "(SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' OR ");
                stb.append(             "SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            stb.append(     "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(       " K2.SUBCLASSCD ");
            stb.append(     ")");
            //評価読替前科目の表 NO024 
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("              T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO024 
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + param._year + "' AND ANNUAL = '" + param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                          T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD ");
            stb.append(     ") ");

            //成績データの表（通常科目）  読替科目は含めない
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    W3.CLASSCD, ");
                stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD, ");
            if (! param._testcd.equals(GRAD_SEM_KIND)) {
                //中間・期末成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
            } else if (Integer.parseInt( param._semester ) != 9) {
                //学期成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
            } else{
                //学年成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_MOTO W2 WHERE ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
            }
            stb.append(     ") ");

            //メイン表
            stb.append("SELECT T1.SCHREGNO,");
            stb.append(       "T5.SUM_REC, T5.AVG_REC, T5.RANK ");

            stb.append("FROM   SCHNO_B T1 ");

            //平均点、席次の表  04/11/03 T4から分離  05/02/14 結果表RECORD_RECを使用
            stb.append(     "LEFT JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO,");
            stb.append(                 "SUM(INT(SCORE))AS SUM_REC, ");
            stb.append(                 "AVG(FLOAT(INT(SCORE))) AS AVG_REC,");                                 // 04/11/04Modify 04/11/08Modify
            stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");           // 04/12/06Modify
            stb.append(                 "RANK() OVER(ORDER BY CASE ");
            stb.append(                                "WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
            stb.append(                                "ELSE AVG(FLOAT(INT(SCORE))) END DESC)END AS RANK ");   // 04/11/01Modify 04/12/06Modify
            stb.append(         "FROM    RECORD_REC W1 ");
            stb.append(         "WHERE   SCORE IS NOT NULL AND ");  //05/03/01Modify
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                 "W1.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
            } else {
                stb.append(                 "SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
            }
            stb.append(         "GROUP BY W1.SCHREGNO");
            stb.append(     ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            return stb.toString();
        }

        /**
         *  クラス内で使用する定数設定
         *    2005/05/22
         */
        void setClasscode()
        {
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode( db2, param._year );         //各学校における定数等設定
                log.debug("semesdiv="+definecode.semesdiv + "   absent_cov="+definecode.absent_cov + "   absent_cov_late="+definecode.absent_cov_late);
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
        }


        /**
         *  SVF-FORM フィールドを初期化
         *    2005/05/22
         */
        private int clearSvfField(final Map hm2, final String lschname[])
        {
            try {
                svf.VrSetForm("KNJD062.frm", 4);
                set_head2();

                svf.VrsOut("year2", nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");  // 年度          
                svf.VrsOut("ymd1", param._now);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(param._yearEdate) + "\uFF5E" + KNJ_EditDate.h_format_JP(param._date));  // 欠課数の集計範囲
                svf.VrsOut("DATE2", KNJ_EditDate.h_format_JP(param._semesterSdate) + "\uFF5E" + KNJ_EditDate.h_format_JP(param._date));  // 出欠記録の集計範囲

                printSvfStdNameOut( hm2, lschname );                            //生徒名等出力のメソッド

                //  組名称及び担任名の取得
                try {
                    KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
                    KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
                    returnval = getinfo.Hrclass_Staff( db2, param._year, param._semester, param._hrclass, "" );
                    svf.VrsOut( "HR_NAME", returnval.val1 );          //組名称
                    svf.VrsOut( "teacher", returnval.val3 );          //担任名
                } catch (Exception ex) {
                    log.warn("HR_NAME... svf-out error!", ex);
                }
            } catch (Exception ex) {
                log.warn("clearSvfField error! ", ex);
            }
            return 0;
        }


        /**
         *  SVF-FORM フィールドを初期化
         *    2005/05/30
         */
        private void clearValue()
        {
            try {
                for( int i = 0 ; i < hr_lesson.length ; i++ )hr_lesson[i] = 0;
                for( int i = 0 ; i < hr_attend.length ; i++ )hr_attend[i] = 0;
                for( int i = 0 ; i < hr_seitosu.length ; i++ )hr_seitosu[i] = 0;
                for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;
                for( int i = 0 ; i < sch_ccredits.length ; i++ )sch_ccredits[i] = 0;
                hr_credits = 0;
                subclasstotalcnt = 0;
                subclasstotalnum = 0;
                hrtotalnum = 0;
                hrtotalcnt = 0;
                subclasslinecount = 0;
                hr_total = 0;
            } catch (Exception ex) {
                log.warn("clearSvfField error! ", ex);
            }
        }


        /**
         *  SVF-FORM フィールドを初期化
         *    2005/06/07
         */
        private void clearValue2()
        {
            try {
                for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;
                for( int i = 0 ; i < sch_ccredits.length ; i++ )sch_ccredits[i] = 0;
                //subclasstotalcnt = 0;
                //subclasstotalnum = 0;
                subclasslinecount = 0;
            } catch (Exception ex) {
                log.warn("clearSvfField error! ", ex);
            }
        }


//        /** 
//         *  対象校時取得
//         *    2005/06/15 Build
//         */
//        void setPeiodValue()
//        {
//            StringBuffer stb2 = null;         //05/04/16
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                StringBuffer stb = new StringBuffer();
//                stb.append(    "SELECT  NAMECD2 ");
//                if (definecode.usefromtoperiod) {
//                    stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
//                    stb.append("WHERE   NAMECD1 = 'B001' ");
//                    stb.append(        "AND S_PERIODCD <= NAMECD2 AND NAMECD2 <= E_PERIODCD ");
//                    stb.append(        "AND COURSECD IN(SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
//                    stb.append(                        "WHERE  W3.YEAR = '" + param._year + "' ");
//                    if (!param._semester.equals("9")) {
//                        stb.append(                           "AND W3.SEMESTER = '" + param._semester + "' ");
//                    } else {
//                        stb.append(                           "AND W3.SEMESTER = '" + param._semeFlg + "' ");
//                    }
//                    stb.append(                               "AND W3.GRADE || W3.HR_CLASS = '" + param._hrclass + "') ");
//                } else{
//                    stb.append("FROM    NAME_MST W1 ");
//                    stb.append("WHERE   NAMECD1 = 'B001' ");
//                }
//                stb.append("ORDER BY NAMECD2");
//
//                ps = db2.prepareStatement( stb.toString() );
//                rs = ps.executeQuery();
//                int i = 0;
//                while ( rs.next()) {
//                    if (i++ == 0) {                                                     //05/04/16
//                        stb2 = new StringBuffer();
//                        stb2.append("(");
//                    }else{
//                        stb2.append(",");
//                    }
//                    stb2.append( "'" ).append( rs.getString("NAMECD2") ).append( "'" );
//                }
//            } catch (Exception ex) {
//                log.warn("periodname-get error!", ex);
//            } finally{
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//                if (stb2 != null ) stb2.append(")");
//                if (0 < stb2.length() ) param._20 = stb2.toString();
//                else                    param._20 = "('1','2','3','4','5','6','7','8','9')";
//            }
//        }


        /** 
         *  クラス履修単位を取得
         *  NO022
         */
        void getRCreditsHr()
        {
            try {
                for( int i = 0; i < sch_rcredits.length; i++) {
                    if (0 < sch_rcredits[i]  &&  hr_credits < sch_rcredits[i] )
                        hr_credits = sch_rcredits[i];
                }
            } catch (Exception ex) {
                log.warn("group-average svf-out error!", ex);
            }

        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, param._year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        /** 出欠状況取得引数をロード */
        public void loadAttendSemesArgument(final DB2UDB db2) {
            try {
                loadSemester(db2, param._year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, param._year, SSEMESTER, param._semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, param._year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, param._date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
                log.debug(" semesFlg = " + _semesFlg);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);
                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
    }//クラスの括り

    private class KNJD062_INTER extends KNJD062_BASE {

        KNJD062_INTER(final DB2UDB db2, final Vrw32alp svf) {
            super(db2, svf);
        }


        /**
          *  ページ見出し
          */
        void set_head2() {

            String testName = "";
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(param._countflg, param._year, param._semester, param._testcd);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    testName = rs.getString("TESTITEMNAME");
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            svf.VrsOut("TITLE" , param._semestername + "  " + testName + " 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"    );           //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++) {
                svf.VrsOutn("ITEM1",  i + 1,   "素点" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname  = "SEM" + param._semester + "_INTR_SCORE";
            fieldname2 = "SEM" + param._semester + "_INTR";
            if (param._semeFlg == null) param._semeFlg = param._semester;

        }//set_head2の括り


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(ResultSet rs,Map hmm,int assesspattern,int replaceflg)
        {
            try {

                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1 == null) {
                    return;
                }
                int linex = subclasslinecount + 1;

                if (rs.getString("PATTERN_ASSESS") != null )
                    if (param._output4.equals("1")  &&  
                        rs.getString("PATTERN_ASSESS") != null  &&  
                        rs.getString("PATTERN_ASSESS").equals("1") )
                        svf.VrsOutn("late"+int1.intValue() , linex , "*" + rs.getString("PATTERN_ASSESS") );
                    else
                        svf.VrsOutn("late"+int1.intValue() , linex ,  rs.getString("PATTERN_ASSESS") );

                if (rs.getString("SCORE") != null) {
                    /*
                    if (rs.getString("SCORE").equals("KK") )
                        ret = svf.VrsOutn("rate"+int1.intValue() , linex , "公" );
                    else if (rs.getString("SCORE").equals("KS") )
                        ret = svf.VrsOutn("rate"+int1.intValue() , linex , "欠" );
                    else{
                    */
                    svf.VrsOutn("rate"+int1.intValue() , linex , rs.getString("SCORE") );
                        //NO023 subclasscnt_num( rs );
                    subclasscnt_num( rs.getString("SCORE"), 0 );  //NO023
                }

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }

        }//printSvfStdDetailOut()の括り


        /** 
         *  総合学習明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         *  2005/03/03
         *
        void printSvfStdDetailSogotekiOut(final ResultSet rs, final Map hmm, final int subclassjisu, final int assesspattern, final int absencehigh)
        {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex, subclassjisu, absencehigh );
    *
                if (rs.getString("ABSENT1") != null  &&  Integer.parseInt( rs.getString("ABSENT1") ) != 0 )
                    ret = svf.VrsOutn("kekka"+int1.intValue() , linex , rs.getString("ABSENT1"));           //欠課
    *
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }

        }//printSvfStdDetailSogotekiOut()の括り*/

    }


    private class KNJD062_GAKKI extends KNJD062_INTER {

        KNJD062_GAKKI(final DB2UDB db2, final Vrw32alp svf) {
            super(db2, svf);
        }


        /**
          *  ページ見出し
          */
        void set_head2() {

            svf.VrsOut("TITLE" , param._semestername + "成績一覧表");     //タイトル
            svf.VrsOut("MARK"  , "/"                     );     //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++) {
                svf.VrsOutn("ITEM1",  i + 1,   "成績" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname = "SEM" + param._semester + "_VALUE";
            if (param._semeFlg == null) param._semeFlg = param._semester;

        }//set_headの括り


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final Map hmm, final int assesspattern, final int replaceflg)
        {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                if (rs.getString("PATTERN_ASSESS") != null) {
//                  rs.getInt("REPLACEMOTO") <= 0) {
                    if (param._output4.equals("1")  &&  
                        rs.getString("PATTERN_ASSESS").equals("1") )
                        svf.VrsOutn("late"+int1.intValue() , linex , "*" + rs.getString("PATTERN_ASSESS"));   //評定
                    else
                        svf.VrsOutn("late"+int1.intValue() , linex , rs.getString("PATTERN_ASSESS"));     //評定
                    subclasscnt_num( rs.getString("PATTERN_ASSESS"), 0 );  //NO023
                }

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );
    /*
                if (rs.getString("ABSENT1") != null  &&  Integer.parseInt( rs.getString("ABSENT1") ) != 0 )
                    ret = svf.VrsOutn("kekka"+int1.intValue() , linex , rs.getString("ABSENT1"));           //欠課
    */
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }

        }//printSvfStdDetailOut()の括り


        /** 
         *  総合学習明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         *  2005/03/03
         *  2005/03/09 評定は、学期成績には出力しない
         *
        void printSvfStdDetailSogotekiOut(final ResultSet rs, final Map hmm, final int subclassjisu, final int assesspattern, final int absencehigh)
        {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;
                //if (rs.getString("PATTERN_ASSESS") != null) {
                //  ret = svf.VrsOutn("late"+int1.intValue() , linex , rs.getString("PATTERN_ASSESS"));     //評定
                //}

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex, subclassjisu, absencehigh );
    *
                if (rs.getString("ABSENT1") != null  &&  Integer.parseInt( rs.getString("ABSENT1") ) != 0 )
                    ret = svf.VrsOutn("kekka"+int1.intValue() , linex , rs.getString("ABSENT1"));           //欠課
    *
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }

        }//printSvfStdDetailSogotekiOut()の括り*/


    }
    

    private class KNJD062_TERM extends KNJD062_INTER {

        KNJD062_TERM(final DB2UDB db2, final Vrw32alp svf) {
            super(db2, svf);
        }


        /**
          *  ページ見出し
          */
        void set_head2() {

            String testName = "";
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(param._countflg, param._year, param._semester, param._testcd);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    testName = rs.getString("TESTITEMNAME");
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            svf.VrsOut("TITLE" , param._semestername + "  " + testName + " 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"                     );           //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++) {
                svf.VrsOutn("ITEM1",  i + 1,   "素点" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            if (param._testcd.equals("0202")) {
                fieldname = "SEM" + param._semester + "_TERM2_SCORE";
                fieldname2 = "SEM" + param._semester + "_TERM2";
            } else {
                fieldname = "SEM" + param._semester + "_TERM_SCORE";
                fieldname2 = "SEM" + param._semester + "_TERM";
            }
            if (param._semeFlg == null) param._semeFlg = param._semester;

        }//set_head2の括り

    }

    private class KNJD062_GRADE extends KNJD062_GAKKI {

        KNJD062_GRADE(final DB2UDB db2, final Vrw32alp svf) {
            super(db2, svf);
        }


        /**
          *  ページ見出し
          *      2005/01/31Modify:現在学期の取得先を在籍データの最大学期からコントロールマスターの現在学期へ変更
          *      2005/05/30Modify:現在学期を指示画面より取得 <= SEME_FLG
          */
        void set_head2() {

            if (param._semeFlg == null) {
                param._semeFlg = param._semester;
            }

            svf.VrsOut("TITLE",  "  成績一覧表（評定）");
            svf.VrsOut("MARK",   "/"                     );              //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++) {
                svf.VrsOutn("ITEM1",  i + 1,   "成績" );
                svf.VrsOutn("ITEM2",  i + 1,   "評定" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname = "GRAD_VALUE";

        }


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final Map hmm, final int assesspattern, final int replaceflg)  //NO007Modify
        {
            try {
                boolean amikake = false;
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1 == null) {
                    return;
                }
                int linex = subclasslinecount + 1;

                //欠課の出力および網掛け設定
    //if (rs.getString("SCHREGNO").equals("81051001") )log.debug("subclasscd="+rs.getString("SUBCLASSCD")+"  replaceflg="+replaceflg+"   absent="+rs.getString("ABSENT1")+"  amikake="+amikake + "  absencehigh="+absencehigh+"   credit="+rs.getString("COMP_CREDIT"));
                if (rs.getString("ABSENT1") != null) {
                    amikake = printSvfStdDetailOutAbsence( rs, int1.intValue(), linex);
                    //履修単位の加算処理 ==> 履修単位：欠課が授業時数の１／３を超えない場合
                    //NO010 if (! amikake  &&  replaceflg < 1 ) sch_rcredits[ int1.intValue() ] += subclassjisu;        //履修単位の加算処理  NO007Modify
                    //NO007if (! amikake ) sch_rcredits[ int1.intValue() ] += subclassjisu;        //履修単位の加算処理
                }
                //履修単位の加算処理 NO010
                //NO015 if (! amikake  &&  replaceflg < 1  &&  rs.getString("COMP_CREDIT") != null )sch_rcredits[ int1.intValue() ] += Integer.parseInt( rs.getString("COMP_CREDIT") );  //履修単位の加算処理  NO007Modify  NO010Modify
                //--NO015
                //NO0018 if (rs.getString("ON_RECORD") == null  &&  rs.getString("COMP_UNCONDITION_FLG").equals("1")) {
                if (rs.getString("ON_RECORD_COMP") == null  &&  rs.getString("COMP_UNCONDITION_FLG").equals("1")) {
                    if (! amikake  &&  replaceflg < 1  &&  rs.getString("COMP_CREDIT") != null )sch_rcredits[ int1.intValue() ] += Integer.parseInt( rs.getString("COMP_CREDIT") );  //履修単位の加算処理  NO007Modify  NO010Modify
                } else{
                    if (replaceflg < 1  &&  rs.getString("COMP_CREDIT") != null )sch_rcredits[ int1.intValue() ] += Integer.parseInt( rs.getString("COMP_CREDIT") );  //履修単位の加算処理  NO007Modify  NO010Modify
                }
                //修得単位の加算処理 NO010
                //NO015 if (! amikake  &&  replaceflg < 1  &&  rs.getString("GET_CREDIT") != null  )sch_ccredits[ int1.intValue() ] += Integer.parseInt( rs.getString("GET_CREDIT")  );  //修得単位の加算処理  NO007Modify  NO010Modify
                //--NO015
                //NO0018 if (rs.getString("ON_RECORD") == null  &&  rs.getString("COMP_UNCONDITION_FLG").equals("1")) {
                if (rs.getString("ON_RECORD_GET") == null  &&  rs.getString("COMP_UNCONDITION_FLG").equals("1")) {
                    if (! amikake  &&  replaceflg < 1  &&  rs.getString("GET_CREDIT") != null  )sch_ccredits[ int1.intValue() ] += Integer.parseInt( rs.getString("GET_CREDIT")  );  //修得単位の加算処理  NO007Modify  NO010Modify
                } else{
                    if (replaceflg < 1  &&  rs.getString("GET_CREDIT") != null  )sch_ccredits[ int1.intValue() ] += Integer.parseInt( rs.getString("GET_CREDIT")  );  //修得単位の加算処理  NO007Modify  NO010Modify
                }

                //評定の出力
                if (rs.getString("PATTERN_ASSESS") != null) {
                    if (rs.getInt("REPLACEMOTO") <= 0) {
                        if (param._output4.equals("1")  &&  rs.getString("PATTERN_ASSESS").equals("1") )
                            svf.VrsOutn("late"+int1.intValue() , linex , "*" + rs.getString("PATTERN_ASSESS"));   //評定
                        else
                            svf.VrsOutn("late"+int1.intValue() , linex , rs.getString("PATTERN_ASSESS"));     //評定
                    }
                    subclasscnt_num( rs.getString("PATTERN_ASSESS"), rs.getInt("REPLACEMOTO") );  //NO023
                }

                //修得単位の加算処理 ==> 修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合
                /* NO010
                if (!amikake  &&  replaceflg < 1 )  //NO007Modify
                //NO007 if (!amikake )
                    if (( rs.getString("PATTERN_ASSESS") != null  &&  !rs.getString("PATTERN_ASSESS").equals("1") )  ||
                        rs.getString("SUBCLASSCD").substring(0, 2).equals("90")  )
                        sch_ccredits[ int1.intValue() ] += subclassjisu;        //修得単位の加算処理
                */

            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
        }


        /** 
         *  総合学習明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         *  2005/03/09 GAKKIより分離
         *
        //void printSvfStdDetailSogotekiOut(final ResultSet rs, final Map hmm, final int subclassjisu, final int assesspattern, final int absencehigh)
        void printSvfStdDetailSogotekiOut(final ResultSet rs, final Map hmm, final int subclassjisu, final int subclasshigh, final int assesspattern, final int absencehigh)
        {
            try {
                boolean amikake = false;

                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;
                if (rs.getString("PATTERN_ASSESS") != null) {
                    ret = svf.VrsOutn("late"+int1.intValue() , linex , rs.getString("PATTERN_ASSESS"));     //評定
                }
                //欠課の出力および網掛け設定
    //log.debug("subclasscd="+rs.getString("SUBCLASSCD")+"   absent="+rs.getString("ABSENT1")+"  amikake="+amikake);
                if (rs.getString("ABSENT1") != null) {
                    amikake = printSvfStdDetailOutAbsence( rs, int1.intValue(), linex, subclassjisu, absencehigh );

                    //履修単位の加算処理 ==> 履修単位：欠課が授業時数の１／３を超えない場合
                    if (! amikake ) sch_rcredits[ int1.intValue() ] += subclassjisu;        //履修単位の加算処理
    //log.debug("subclasscd="+rs.getString("SUBCLASSCD")+"   absent="+rs.getString("ABSENT1")+"  amikake="+amikake);
                }

                //修得単位の加算処理 ==> 修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合
                if (!amikake )
                    if (( rs.getString("PATTERN_ASSESS") != null  &&  !rs.getString("PATTERN_ASSESS").equals("1") )
                        ||  rs.getString("SUBCLASSCD").substring(0, 2).equals("90")  )
                        sch_ccredits[ int1.intValue() ] += subclassjisu;        //修得単位の加算処理

                if (amikake )
                    ret = svf.VrAttributen("kekka"+int1.intValue() , linex ,"Paint=(0,0,0),Bold=0");   //網掛けクリア

            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
        }*/


    }
    
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        String _hrclass;
        final String _date;
        String _output3;
        String _semesterSdate;
        String _semesterEdate;
        final String _output4;
        String _sattendno;
        String _eattendno;
        final String _testitemcd;
        String _semestername;
        String _semeFlg;
        String _attendDate;
        String _divideAttendDate;
        String _divideAttendMonth;
        String _yearEdate;
        String _now;
        String _output5;
        // String _20;
        final String _testcd;
        final String _countflg;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");                                    //年度
            _semester = ( !(request.getParameter("SEMESTER").equals("4")) )?
                           request.getParameter("SEMESTER") : "9";                      //1-3:学期 9:学年末
            //_grade = request.getParameter("GRADE_HR_CLASS").substring(0,2);         //学年・組 05/05/22
            _grade = request.getParameter("GRADE");                                   //学年     05/05/30
            _output4 = ( request.getParameter("OUTPUT4")!= null )?"1":"0";              //単位保留
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //出欠集計日付

            if (request.getParameter("OUTPUT3") !=null) _output3 = "on";                 //遅刻を欠課に換算 null:無

            if (request.getParameter("TESTKINDCD") != null  &&  2 <= request.getParameter("TESTKINDCD").length() )
                _testitemcd = request.getParameter("TESTKINDCD").substring(0,2);          //テスト種別
            else
                _testitemcd = request.getParameter("TESTKINDCD");

            final String testKindCd = request.getParameter("TESTKINDCD");
            _testcd = PARAM_GRAD_SEM_KIND.equals(testKindCd) ? GRAD_SEM_KIND : testKindCd;
            _countflg = request.getParameter("COUNTFLG");

            _semeFlg = request.getParameter("SEME_FLG");          //LOG-IN時の学期（現在学期）05/05/30
            if (request.getParameter("OUTPUT5") != null )
                _output5 = request.getParameter("OUTPUT5");       //欠番を詰める 05/06/07Build
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            // param._divideAttendDate:attend_semes_datの最終集計日の翌日をセット
            // param._divideAttendMonth:attend_semes_datの最終集計学期＋月をセット
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            try {
                obj.getDivideAttendDate(db2, _year, _semester, _date);
                _divideAttendDate = obj.date;
                _divideAttendMonth = obj.month;
            } catch (Exception ex) {
                log.error("error! ", ex);
            }
    log.debug("_divideAttendDate="+_divideAttendDate);
    log.debug("_divideAttendMonth="+_divideAttendMonth);

        }
    }

}//クラスの括り

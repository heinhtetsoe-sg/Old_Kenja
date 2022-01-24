// kanji=漢字
/*
 * $Id: c829f458a0baf0f826c3b39565d50b58d24f9928 $
 *
 * 作成日: 2006/03/30 14:27:31 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
http://tokio/serv_ktest/KNJD?DBNAME=KINHDB&PRGID=KNJD064K&YEAR=2005&SEMESTER=9&CLASS_SELECTED=02K01&GRADE=02&DATE=2006/03/31&OUTPUT3=on&TESTKINDCD=0&SEME_FLG=3
 *
 *  学校教育システム 賢者 [成績管理] 前年度評価一覧
 *
 *  2006/03/30 yamashiro KNJD060K_BASE.javaを複写して作成
 *  2006/04/04 yamashiro 前年度クラスに'NULL'と出力される不具合を修正 --NO001
 *                       今年度に編入、転入したら、異動情報と同様に備考に表示 --NO002
 *  2006/04/05 o-naka NO003 印刷日付を追加
 *                    NO004 留年者の場合に、「総合点」「平均点」「出欠の記録」が表示されている不具合を修正
 *                    NO005 休学者の場合で学年成績が無い生徒の場合に、「出欠の記録」が表示されている不具合を修正
 *                    NO005 留学者の場合で学年成績が無い生徒の場合に、「履修単位数」「修得単位数」「出欠の記録」「各科目の欠課数」が表示されている不具合を修正
 *  2006/04/06 yamashiro NO006 留学者・休学者の場合で学年成績が無い生徒は、欠課時数を出さない 
 *                             学年成績および評定は「欠」を出力せず、GRADE_RECORDをそのまま出力する => KNJD064K_GRADE
 *                       NO007 学年成績が無い生徒は、欠課時数を出さない 
 *
 */

package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


class KNJD064K_BASE{

	protected Vrw32alp svf;                   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB db2;                     //Databaseクラスを継承したクラス
    protected Param param;
    protected String fieldname;
    protected int subclasslinecount;          //科目列カウント
    private KNJDefineSchool definecode;       //各学校における定数等設定

    private static final Log log = LogFactory.getLog(KNJD064K_BASE.class);

    private KNJSchoolMst _knjSchoolMst;

    private int hr_total;                                   //クラス平均総合点
    private int hr_lesson[] = new int[2];                   //クラス授業日数{最大、最小}
    private int hr_attend[] = new int[9];                   //クラス平均出欠格納
    private int hr_seitosu[] = new int[12];                 //クラス平均対象生徒数格納
    protected int sch_rcredits[] = new int[50];               //生徒ごとの履修単位数
    protected int sch_ccredits[] = new int[50];               //生徒ごとの修得単位数
    private int hr_credits;                                 //クラスの履修単位数
    private int subclasstotalcnt;                           //科目別件数
    private int subclasstotalnum;                           //科目別得点累積
    private int hrtotalnum;                                 //全科目得点累積
    private int hrtotalcnt;                                 //全科目件数
    protected int sch_rcredits_hr[] = new int[50];            //欠課超過単位を除外しない生徒ごとの履修単位数 NO106

    KNJD064K_BASE(){
    }

    /**
     *  コンストラクター
     */
    KNJD064K_BASE(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        this.db2 = db2;
        this.svf = svf;
        this.param = param;
    }

    boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf) {
        
        // パラメータの取得
        final Param param = getParam(db2, request);
        
        boolean nonedata = false;

        svf.VrSetForm("KNJD064.frm", 4);

        try {
            String hrclass[] = request.getParameterValues("CLASS_SELECTED"); //学年・組
            KNJD064K_BASE obj = null;
            obj = new KNJD064K_GRADE(db2, svf, param);
            if (obj.printSvf(hrclass)) {
                nonedata = true;           //05/05/30Modify
            }
        } catch (Exception ex) {
            log.error("printSvf error!", ex);
        }
        return nonedata;
    }
    
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal(" $Revision: 75312 $ $Date: 2020-07-08 13:24:03 +0900 (水, 08 7 2020) $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /**
     *  印刷処理
     */
    private boolean printSvf(final String[] hrclass) {

        boolean nonedata = false;               //該当データなしフラグ
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param._year);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }
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
    private void set_head() {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
        KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
        if (definecode == null) {
            setClasscode();         //教科コード・学校区分・学期制等定数取得
        }

        //  学期名称、範囲の取得
        try {
            returnval = getinfo.Semester(db2,param._lastyear,param._lastyearSemester);
            param._lastyearSemestername = returnval.val1;                                 //学期名称
            param._lastyear0401 = returnval.val2;                                  //学期期間FROM
            param._lastyear0331 = returnval.val3;                                  //学期期間TO

            //学期成績の場合１学期開始日を取得 --NO100
            if (param._testkindcd.equals("0")  &&  !param._lastyearSemester.equals("9")) {
                returnval = getinfo.Semester(db2,param._lastyear,"9");
                param._lastyearSemester9sdate = returnval.val2;                             //学期期間FROM
            }else {
                param._lastyearSemester9sdate = param._lastyear0401;
            }

        } catch (Exception ex) {
            log.warn("term1 svf-out error!", ex);
        } finally {
            if (param._lastyear0401 == null ) {
                param._lastyear0401 = param._lastyear + "-04-01";
            }
            if (param._lastyear0331 == null ) {
                param._lastyear0331 = ( Integer.parseInt(param._lastyear) + 1 ) + "-03-31";
            }
            if (param._lastyearSemester9sdate == null) {
                param._lastyearSemester9sdate = param._lastyear + "-04-01";       //05/07/04Build
            }
        }

        //  ３学期の開始日取得
        try {
            returnval = getinfo.Semester(db2,param._lastyear,"3");
            param._lastyearSemester3sdate = returnval.val2;                                 //学期期間FROM
        } catch (Exception ex) {
            log.warn("term1 svf-out error!", ex);
        } finally {
            if (param._lastyearSemester3sdate == null) {
                param._lastyearSemester3sdate = ( Integer.parseInt(param._lastyear) + 1 ) + "-03-31";
            }
        }

    //  出欠データ集計用開始日取得 => 2004年度の１学期は累積データを使用する => 出欠データ集計は2004年度2学期以降
    //  作成日(現在処理日)・出欠集計範囲の出力
        try {
            //システム時刻を表記
            final StringBuffer stb = new StringBuffer();
            Date date = new Date();
            SimpleDateFormat sdf = null;
            sdf = new SimpleDateFormat("yyyy");
            stb.append( KenjaProperties.gengou(Integer.parseInt( sdf.format(date))));
            sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append( sdf.format(date));
            param._newDateString = stb.toString();
log.debug("date="+stb.toString());

        } catch (Exception ex) {
            log.warn("ymd1 svf-out error!", ex);
        }

        set_head2();
    }//set_headの括り


    /**
     *  ページ見出し
     *  中間、期末、学期、学年末で設定
     */
    void set_head2(){}


    /**
     * メイン印刷処理
     *   対象生徒(Sql=>prestatStdNameList)を読込み、５０名ごとに印刷(printSvfStdDetail)する
     */
    private boolean printSvfMain(final String[] hrclass) {
        boolean nonedata = false;
        Map<String, Integer> hm1 = new HashMap(50);              //学籍番号と行番号の保管
        String lschname[] = new String[50];     //生徒名の保管
        Map<Integer, String> hm2 = new HashMap();                //行番号(出席番号)と備考の保管
        PreparedStatement arrps[] = new PreparedStatement[6];
        List nokaikin = new ArrayList();        //非皆勤者のリスト

        try {
            arrps[0] = db2.prepareStatement( prestatStdNameList()       );      //学籍データ
            arrps[1] = db2.prepareStatement( prestatStdTotalRec()       );      //成績累計データ
            arrps[2] = db2.prepareStatement( prestatSubclassInfo()      );      //科目名等データ
            arrps[3] = db2.prepareStatement( prestatStdSubclassDetail());      //明細データ
            arrps[4] = db2.prepareStatement( prestatStdTotalAttend()    );      //出欠累計データ
            arrps[5] = db2.prepareStatement( prestatAttendKaikin()      );      //非皆勤者
        } catch (Exception ex) {
            log.error("[KNJD171K]boolean printSvfMain prepareStatement error! ", ex);
            return nonedata;
        }

        //クラスごとの印刷処理
        for( int i = 0 ; i < hrclass.length ; i++) {
log.debug("hrclass="+hrclass[i]);
            try {
                param._classSelected = hrclass[i];
                setAttendNoKaikin( arrps[5], nokaikin );                //非皆勤者をリストに保管
                if (printSvfMainHrclass( hm1, hm2, lschname, arrps, nokaikin )) nonedata = true;
                if (hm1 != null )hm1.clear();                           //行番号情報を削除
                if (hm2 != null )hm2.clear();                           //備考情報を削除
                if (lschname != null )for( int j = 0 ; j < lschname.length ; j++ )lschname[j] = null;
                clearValue();
            } catch (Exception ex) {
                log.error("[KNJD064K]boolean printSvfMainHrclass() error! ", ex);
            }
        }

        return nonedata;
    }


    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param._classSelected:学年・組
     */
    private boolean printSvfMainHrclass( Map<String, Integer> hm1, Map<Integer, String> hm2, String lschname[], PreparedStatement arrps[], List<String> nokaikin) {
        boolean nonedata = false;
        ResultSet arrrs[] = new ResultSet[2];
        int schno = 0;                          //行番号

        //学級単位の印刷処理
        try {
            int pp = 0;
            arrps[0].setString( ++pp,  param._classSelected );                      //学年・組
            arrrs[0] = arrps[0].executeQuery();                         //生徒名の表
            while( arrrs[0].next()) {
                schno = ( arrrs[0].getInt("ATTENDNO") % 50 != 0 )? arrrs[0].getInt("ATTENDNO") % 50 : 50;
                saveSchInfo( arrrs[0], schno, hm1, lschname, hm2, nokaikin );               //生徒情報の保存処理
                if (schno == 50) {
                    if (printSvfStdDetail( hm1, hm2, lschname, arrps )) nonedata = true;           //成績・評定・欠課出力のメソッド
                    if (hm1 != null )hm1.clear();                           //行番号情報を削除
                    if (hm2 != null )hm2.clear();                           //備考情報を削除
                    if (lschname != null )for( int j = 0 ; j < lschname.length ; j++ )lschname[j] = null;
                    schno = 0;
                    param._tmpAttendnoStart = null;                                        //開始生徒
                    param._tmpAttendnoEnd = null;                                       //終了生徒
                }
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
        }

        //成績・評定・欠課の印刷処理
        if (schno>0) {
            if (printSvfStdDetail( hm1, hm2, lschname, arrps )) {
                nonedata = true;     //成績・評定・欠課出力のメソッド OKならインスタンス変数nonedataをtrueとする
            }
        }

        for( int i = 0 ; i < arrrs.length ; i++) {
            DbUtils.closeQuietly(arrrs[i]);
        }

        return nonedata;
    }


    /**
     *  生徒情報の保存処理
     */
    void saveSchInfo(ResultSet rs, int schno, Map<String, Integer> hm1, String lschname[], Map<Integer, String> hm2, List<String> nokaikin) {
        try {
            hm1.put( rs.getString("SCHREGNO"), new Integer(schno));    //行番号に学籍番号を付ける
            if (param._tmpAttendnoStart == null ) param._tmpAttendnoStart = rs.getString("ATTENDNO"); //開始生徒
            param._tmpAttendnoEnd = rs.getString("ATTENDNO");                       //終了生徒
            lschname[ schno-1 ] = rs.getString("NAME");                 //生徒名

            //  文言をセットする。（除籍日付＋'付'＋除籍区分名称）
            if (rs.getString("KBN_DATE1") != null) {
                final StringBuffer stb = new StringBuffer();
                stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE1")));
                if (rs.getString("KBN_NAME1") != null ) stb.append( rs.getString("KBN_NAME1"));
                if (rs.getString("HR_NAME") != null )stb.append( "*" + rs.getString("HR_NAME"));
                hm2.put( new Integer(schno), stb.toString());  //備考に行番号を付ける
            } else if (rs.getString("KBN_DATE2") != null) {
                final StringBuffer stb = new StringBuffer();
                stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE2")));
                if (rs.getString("KBN_NAME2") != null ) stb.append( rs.getString("KBN_NAME2"));
                if (rs.getString("HR_NAME") != null )stb.append( "*" + rs.getString("HR_NAME"));
                hm2.put( new Integer(schno), stb.toString());  //備考に行番号を付ける
            } else if (! nokaikin.contains( rs.getString("SCHREGNO"))) {
                //「皆勤者」出力 05/07/04Build
                final StringBuffer stb = new StringBuffer();
                stb.append("皆勤者");
                if (rs.getString("HR_NAME") != null )stb.append( "*" + rs.getString("HR_NAME"));
                hm2.put( new Integer(schno), stb.toString());  //備考に行番号を付ける
            } else {
                final StringBuffer stb = new StringBuffer();
                if (rs.getString("HR_NAME") != null) {
                    stb.append( "*" + rs.getString("HR_NAME"));
                    hm2.put( new Integer(schno), stb.toString());  //備考に行番号を付ける
                }
            }
        } catch (Exception ex) {
            log.warn("name1 svf-out error!", ex);
        }

    }


    /**
     *  生徒名等出力 
     */
    void printSvfStdNameOut(final Map<Integer, String> hm2, String lschname[]) {

        try {
            String str = null;
            for( int i = 0 ; i < lschname.length ; i++) {
                svf.VrsOut("name" + (i+1) ,lschname[i] );
                if (hm2.containsKey( new Integer( i+1 ))) {
                    str = hm2.get( new Integer( i+1 ));
                    if (0 < str.indexOf("*")) svf.VrsOut("REMARK" + (i+1) , str.substring( 0, str.indexOf("*")));
                    else if (-1 == str.indexOf("*")) svf.VrsOut("REMARK" + (i+1) , str );
                    if (-1 < str.indexOf("*")) svf.VrsOut("LASTCLASS" + (i+1) , str.substring( str.indexOf("*") + 1));
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
     */
    boolean printSvfStdDetail( Map<String, Integer> hmm, Map hm2, String lschname[], PreparedStatement arrps[] )
    {
        boolean nonedata = false;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        int replaceflg = 0;             //評価読替元科目:1  評価読替先科目:-1
        int subclassjisu = 0;           //科目の授業時数 -> 単位数へ変更
        int assesspattern = 0;          //科目の評定類型 A:0 B:1 C:2
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;   //履修単位数
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_ccredits[i] = 0;   //修得単位数
        for( int i = 0 ; i < sch_rcredits_hr.length ; i++ )sch_rcredits_hr[i] = 0;   //履修単位数
        int subclass1 = 0;              //科目コードの保存
        int subclass2 = 0;              //科目コードの保存

        clearSvfField( hm2, lschname );                         //生徒名等出力のメソッド

        try {
            int p = 0;
            arrps[3].setString( ++p, param._classSelected  );       //学年・組
            arrps[3].setString( ++p, param._tmpAttendnoStart  );       //生徒番号
            arrps[3].setString( ++p, param._tmpAttendnoEnd );       //生徒番号
log.debug("prestatStdSubclassDetail executeQuery() start");
            rs2 = arrps[3].executeQuery();                  //明細データのRecordSet
log.debug("prestatStdSubclassDetail executeQuery() end");
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        }

        try {
            int p = 0;
            arrps[2].setString( ++p, param._classSelected );                //学年・組
log.debug("prestatSubclassInfo executeQuery() start");
            rs1 = arrps[2].executeQuery();                                  //科目名等のRecordSet
log.debug("prestatSubclassInfo executeQuery() end");
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        }

        try {
            while ( rs2.next()) {
                //科目コードのブレイク
                final int rs2subclasscd = Integer.parseInt(StringUtils.split(rs2.getString("SUBCLASSCD"), "-")[3]);
                if (subclass2 != rs2subclasscd) {
                    if (subclass2 != 0  &&  subclass1 <= subclass2) {  //05/11/24 Modify
                    //05/11/24 Delete if (subclass2!=0) {
                        printSvfsubclasscnt_num();
                        svf.VrEndRecord();
                        nonedata = true;
                        subclasslinecount++;
                        subclassjisu = 0;
                        replaceflg = 0;
                        subclasstotalcnt = 0;
                        subclasstotalnum = 0;
                    }
                    subclass2 = rs2subclasscd;           //科目コードの保存
                    //科目名をセット
                    for( int i = 0 ; subclass1 < subclass2  &&  i < 50 ; i++) {
                        if (19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                        if (rs1.next()) {
                            final int rs1subclasscd = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                            subclass1 = rs1subclasscd;
                            if (rs1.getString("CREDITS") != null) {
                                subclassjisu = Integer.parseInt( rs1.getString("CREDITS"));
                                //読替科目フラグを設定 0:通常科目 1:読替元 -1:読替先  05/11/16
                                if (rs1.getString("REPLACEFLG") != null) {
                                    if (rs1.getString("REPLACEFLG").equals("SAKI"))replaceflg = -1;
                                    else if (rs1.getString("REPLACEFLG").equals("MOTO"))replaceflg = 1;
                                }
                            }

                            printSvfSubclassInfoOut(rs1);       //科目名等出力のメソッド
                            if (subclass1<subclass2) {          //データ出力用にない科目列を出力する
                                printSvfsubclasscnt_num();
                                svf.VrEndRecord(); 
                                nonedata = true; 
                                subclasslinecount++;
                                subclasstotalcnt = 0;
                                subclasstotalnum = 0;
                            }
                            if (subclass1==subclass2 )break;        //科目名出力用とデータ出力用の科目コードが一致なら抜ける
                        }
                    }
                }

                if (subclass1 != subclass2 )continue;  //50/11/24 Build
                //明細データの出力 05/03/03Modify 総合学習を別処理とする
                if (!( String.valueOf( rs2subclasscd ).substring(0,2)).equals("90")) {
                    printSvfStdDetailOut(rs2, hmm, subclassjisu, replaceflg, assesspattern); //総合学習以外
                } else {
                    printSvfStdDetailSogotekiOut(rs2, hmm, subclassjisu, replaceflg, assesspattern); //総合学習
                }

            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(rs2);
        }


        //総合欄の印刷処理 => 最終列に出力
        if (subclass2 != 0) {
            try {
                printSvfsubclasscnt_num();   //05/03/16
                printSvfStdTotalRec( hmm, arrps );
                printSvfStdTotalAttend( hmm, arrps );
                printSvfTotalOut();
                svf.VrEndRecord();
                nonedata = true;
                subclasslinecount++;
            } catch (Exception ex) {
                log.warn("sogotekina... svf-out error!", ex);
            }
        }

        return nonedata;

    }//boolean printSvfStdDetail()の括り


    /**
     *  科目名等出力 
     */
    void printSvfSubclassInfoOut(ResultSet rs) {

        try {
            boolean amikake = false;
            if ("90".equals(rs.getString("SUBCLASSCD").substring(0,2))) {
                printSvfOutSpace();
                subclasslinecount = 18;
            }
            svf.VrsOut("course1" , rs.getString("CLASSABBV"));                //教科名

            if (rs.getString("ELECTDIV") != null  &&  ! rs.getString("ELECTDIV").equals("0")) {
                amikake = true;
            }
            if (amikake) {
                svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(2,70,2),Bold=1"); //網掛け
            }
            svf.VrsOutn("subject1", subclasslinecount+1, rs.getString("SUBCLASSNAME"));   //科目名

            if (rs.getString("CREDITS") != null) {
                if (( rs.getString("CREDITS")).equals( rs.getString("MIN_CREDITS"))) {
                    svf.VrsOutn("credit1",  subclasslinecount+1, rs.getString("CREDITS"));    //単位数
                } else {
                    svf.VrsOutn("credit1",  subclasslinecount+1, rs.getString("MIN_CREDITS") + "\uFF5E" + rs.getString("CREDITS"));  //単位数
                }
            }

            if (amikake) {
                svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(0,0,0),Bold=0");  //網掛け
            }

        } catch (Exception ex) {
            log.warn("course1... svf-out error!", ex);
        }

    }//printSvfSubclassInfoOut()の括り


    /**
     *  科目名等出力   総合的な学習を最後列に出力するための処理
     */
    void printSvfOutSpace() {
        
        for( int i = subclasslinecount+1 ; i < 19 ; i++) {
            svf.VrAttribute("course1",  "Meido=100" );    //教科名を白文字設定
            svf.VrsOut("course1", String.valueOf( i ));  //教科名
            svf.VrEndRecord();
        }
    }


    /**
     *  科目別および全科目の平均・合計累積処理
     *      学期および学年成績において ( param._testkindcd=='0' ) 評定読替科目は含めない
                                                            (SQLのSELECTで'REPLACEMOTO'がマイナスなら評定読替科目)
     */
    void subclasscnt_num( ResultSet rs) {

        try {
            if (rs.getString("SCORE") != null         &&
                ! rs.getString("SCORE").equals("KK")  &&
                ! rs.getString("SCORE").equals("KS")  &&
                ! rs.getString("SCORE").equals("( )") &&
                ! rs.getString("SCORE").equals("欠")  &&
                ! rs.getString("SCORE").equals("公")     ) {

                    subclasstotalnum += Integer.parseInt( rs.getString("SCORE"));
                    subclasstotalcnt ++;

                    if (param._testkindcd.equals("0")) {
                        if (0 <= rs.getInt("REPLACEMOTO")) {
                            hrtotalnum += Integer.parseInt( rs.getString("SCORE"));
                            hrtotalcnt ++;
                        }
                    } else{
                        hrtotalnum += Integer.parseInt( rs.getString("SCORE"));
                        hrtotalcnt ++;
                    }
            }
        } catch (Exception ex) {
            log.warn("term1 svf-out error!", ex);
        }
    }


    /**
     *  科目別平均・合計
     */
    void printSvfsubclasscnt_num() {

        try {
            if (0 < subclasstotalnum) {
                svf.VrsOutn("TOTAL_SUBCLASS", subclasslinecount+1, String.valueOf( subclasstotalnum ));  //ＨＲ合計
                svf.VrsOutn("AVE_CLASS",      subclasslinecount+1, String.valueOf( Math.round( (float)subclasstotalnum / (float)subclasstotalcnt )) );   //ＨＲ平均
            }

        } catch (Exception ex) {
            log.warn("term1 svf-out error!", ex);
        }
    }


    /**
     *  2005/01/31 履修単位を加算する処理
     *     科目欄の単位数の合計をクラス履修単位とする
     *        => 体育等の扱いを考慮し取り敢えず群コードが０または連続しない群コードの単位を集計する
     */
    void addSubclassCredits( ResultSet rs, int group, int replaceflg) {

        try {
log.debug("kamokuname="+rs.getString("SUBCLASSNAME") + "  groupcd="+rs.getString("GROUPCD")+"  credits="+rs.getString("CREDITS")+"  replaceflg="+replaceflg);
            if (rs.getString("CREDITS") == null) {
                return;
            }
            if (0 < replaceflg) {
                return;  //05/11/24 Build
            }

            if (Integer.parseInt( rs.getString("GROUPCD")) == 0) {
                hr_credits += Integer.parseInt( rs.getString("CREDITS"));  //単位数
            } else if (Integer.parseInt( rs.getString("GROUPCD")) != group) {
                hr_credits += Integer.parseInt( rs.getString("CREDITS"));  //単位数
            }
log.debug("hr_credits="+hr_credits);                
        } catch (Exception ex) {
            log.warn("error! ", ex);
        }
    }


    /** 
     *  明細出力 
     *  生徒の科目別成績、評定、欠課を出力する
     */
    void printSvfStdDetailOut(ResultSet rs, Map<String, Integer> hmm, int subclassjisu, int replaceflg, int assesspattern) {
    }


    /** 
     *  総合学習明細出力 
     *  生徒の科目別成績、評定、欠課を出力する
     *  2005/03/03
     */
    void printSvfStdDetailSogotekiOut(ResultSet rs, Map<String, Integer> hmm, int subclassjisu, int replaceflg, int assesspattern)
    {
    }


    /**
     *  累計出力処理 
     */
    boolean printSvfStdTotalRec(Map<String, Integer> hmm, final PreparedStatement[] arrps) {
        boolean nonedata = false;
        ResultSet rs = null;

        try {
            int pp = 0;
            arrps[1].setString( ++pp,  param._classSelected );                      //学年・組
log.debug("prestatStdTotalRec executeQuery() start");
            rs = arrps[1].executeQuery();                               //成績累計データのRecordSet
log.debug("prestatStdTotalRec executeQuery() end");

            while ( rs.next()) {
                //明細データの出力
                printSvfStdTotalRecOut(rs,hmm);
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rs);
        }

        return nonedata;

    }//printSvfStdTotalRec()の括り



    /**
     *  累計出力処理 
     */
    boolean printSvfStdTotalAttend( Map hmm, PreparedStatement arrps[] )
    {
        boolean nonedata = false;
        ResultSet rs = null;

        try {
            int p = 0;
            arrps[4].setString( ++p, param._classSelected );        //学年・組
log.trace("prestatStdTotalAttend executeQuery() start ");
            rs = arrps[4].executeQuery();                               //出欠累計データのRecordSet
log.trace("prestatStdTotalAttend executeQuery() end ");

            while ( rs.next()) {
                //明細データの出力
                printSvfStdTotalAttendOut( rs, hmm );
            }
        } catch (Exception ex) {
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
    void printSvfStdTotalRecOut(ResultSet rs, Map<String, Integer> hmm) {
        try {
            //学籍番号（生徒）に対応した行にデータをセットする。
            Integer int1 = hmm.get(rs.getString("SCHREGNO"));
            if (int1==null )return;

            if (rs.getString("SUM_REC")!=null) {
                svf.VrsOut("TOTAL" + int1, rs.getString("SUM_REC"));           //総合点
                hr_total += rs.getInt("SUM_REC");
                hr_seitosu[8]++;
            }

            if (rs.getString("AVG_REC")!=null) {
                svf.VrsOut("AVERAGE" + int1, String.valueOf(Math.round(rs.getFloat("AVG_REC"))));  //平均点
            }
            //履修単位の(表記)処理を止めておく
            //学年末の処理で、３年生は2学期、以外の学年は3学期に出力 
            //履修単位の表記を中間・期末・学期成績にも適用
            if (0 < sch_rcredits[ int1 ]) {
                svf.VrsOut("R_CREDIT" + int1, String.valueOf( sch_rcredits[ int1 ] )); //履修単位数
            }

            //修得単位数の表記
            //履修単位数の表記を現在学期で制限 => １年生と２年生は３学期、３年生は２学期と３学期とする
            if (param._lastyearSemester.equals("9")  &&  0 < sch_rcredits[ int1 ]) {
                if (( ( param._classSelected.substring( 0,2 ).equals("03"))? 1: 2 ) < Integer.parseInt( param._lastyearLastSemester )) {
                    svf.VrsOut( "C_CREDIT" + int1, String.valueOf( sch_ccredits[ int1 ] )); //修得単位数
                }
            }
        } catch (Exception ex) {
            log.error("printSvfStdTotalRecOut error! ", ex);
        }
    }//printSvfStdTotalRecOut()の括り



    /** 
     *  出欠の記録 明細出力 
     *      2005/01/04 SQL変更に伴い修正
     *      2005/02/01 出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。
     */
    void printSvfStdTotalAttendOut( ResultSet rs, Map<String, Integer> hmm )
    {
        Integer int1 = null;
        try {
            int1 = hmm.get( rs.getString("SCHREGNO"));        //学籍番号に対応した行にデータをセットする。
            if (int1 == null )return;

            if (rs.getString("LESSON") != null  &&  rs.getInt("LESSON") != 0) {
                if (hr_lesson[0] < rs.getInt("LESSON")) 
                    hr_lesson[0] = rs.getInt("LESSON");                 //最大授業日数
                if (hr_lesson[1] == 0) {                 
                    hr_lesson[1] = rs.getInt("LESSON");
                } else if (rs.getInt("LESSON") < hr_lesson[1]) { 
                    hr_lesson[1] = rs.getInt("LESSON");                 //最小授業日数
                }
            }

            if (rs.getString("SUSPEND") != null || "true".equals(param._useVirus) && rs.getString("VIRUS") != null || "true".equals(param._useKoudome) && rs.getString("KOUDOME") != null){
                int value = 0;
                boolean hasvalue = false;
                if (rs.getInt("SUSPEND") != 0) {
                    hasvalue = true;
                    value += rs.getInt("SUSPEND");
                }
                if ("true".equals(param._useVirus) && rs.getString("VIRUS") != null) {
                    hasvalue = true;
                    value += rs.getInt("VIRUS");
                }
                if ("true".equals(param._useKoudome) && rs.getString("KOUDOME") != null) {
                    hasvalue = true;
                    value += rs.getInt("KOUDOME");
                }
                if (hasvalue) {
                    svf.VrsOut( "SUSPEND" + int1, String.valueOf(value));   //出停
                    
                }
                hr_attend[0] += value;         //合計
                hr_seitosu[0]++;                                  //生徒数
            }

            if (rs.getString("MOURNING") != null) {
                if (rs.getInt("MOURNING") != 0) {
                    svf.VrsOut( "KIBIKI" + int1, rs.getString("MOURNING"));   //忌引
                    hr_attend[1] += rs.getInt("MOURNING");        //合計
                }
                hr_seitosu[1]++;                                  //生徒数
            }

            if (rs.getString("MLESSON") != null) {
                if (rs.getInt("MLESSON") != 0) {
                    svf.VrsOut( "PRESENT" + int1, rs.getString("MLESSON"));   //出席すべき日数
                    hr_attend[3] += rs.getInt("MLESSON");         //合計
                }
                hr_seitosu[3]++;                                  //生徒数
            }

            if (rs.getString("ABSENT") != null) {
                if (rs.getInt("ABSENT") != 0) {
                    svf.VrsOut( "ABSENCE" + int1, rs.getString("ABSENT"));    //欠席日数
                    hr_attend[4] += rs.getInt("ABSENT");          //合計
                }
                hr_seitosu[4]++;                                  //生徒数
            }

            if (rs.getString("PRESENT") != null) {
                if (rs.getInt("PRESENT") != 0) {
                    svf.VrsOut( "ATTEND" + int1, rs.getString("PRESENT"));    //出席日数
                    hr_attend[5] += rs.getInt("PRESENT");         //合計
                }
                hr_seitosu[5]++;                                  //生徒数
            }

            if (rs.getString("EARLY") != null) {
                if (rs.getInt("EARLY") != 0) {
                    svf.VrsOut( "LEAVE" + int1, rs.getString("EARLY"));       //早退回数
                    hr_attend[6] += rs.getInt("EARLY");           //合計
                }
                hr_seitosu[6]++;                                  //生徒数
            }

            if (rs.getString("LATE") != null) {
                if (rs.getInt("LATE") != 0) {
                    svf.VrsOut( "TOTAL_LATE" + int1, rs.getString("LATE"));   //遅刻回数
                    hr_attend[7] += rs.getInt("LATE");            //合計
                }
                hr_seitosu[7]++;                                  //生徒数
            }

            if (rs.getString("TRANSFER_DATE") != null) {
                if (rs.getInt("TRANSFER_DATE") != 0) {
                    svf.VrsOut("ABROAD" + int1, rs.getString("TRANSFER_DATE"));    //留学日数
                    hr_attend[2] += rs.getInt("TRANSFER_DATE");
                }
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
    void printSvfTotalOut() {
        try {

            if (0 < hr_seitosu[8]) {
                svf.VrsOut( "TOTAL51",    String.valueOf( Math.round( (float)hr_total / hr_seitosu[8] )) );  //総合点 小数点第１位で四捨五入
            }
            if (0 < hrtotalcnt) {
                svf.VrsOut( "AVERAGE51", String.valueOf( Math.round( (float)hrtotalnum / hrtotalcnt )));     //平均点
                svf.VrsOut( "TOTAL53",   String.valueOf( hrtotalnum ));  //総合点
            }

            if (0 < hr_seitosu[0] ) svf.VrsOut( "SUSPEND53", String.valueOf( hr_attend[0] ));    //出停
            if (0 < hr_seitosu[1] ) svf.VrsOut( "KIBIKI53",  String.valueOf( hr_attend[1] ));    //忌引
            if (0 < hr_seitosu[3] ) svf.VrsOut( "PRESENT53", String.valueOf( hr_attend[3] ));    //出席すべき日数
            if (0 < hr_seitosu[4] ) svf.VrsOut( "ABSENCE53", String.valueOf( hr_attend[4] ));    //欠席日数
            if (0 < hr_seitosu[5] ) svf.VrsOut( "ATTEND53",  String.valueOf( hr_attend[5] ));    //出席日数
            if (0 < hr_seitosu[6] ) svf.VrsOut( "LEAVE53",   String.valueOf( hr_attend[6] ));    //早退回数
            if (0 < hr_seitosu[7] ) svf.VrsOut( "TOTAL_LATE53",    String.valueOf( hr_attend[7] ));  //遅刻回数

            if (0 < hr_attend[3]) {
                svf.VrsOut( "PER_ATTEND",  String.valueOf( (float)Math.round( ((float)hr_attend[5] / (float)hr_attend[3]) * 1000 ) / 10 ));  //出席率
                svf.VrsOut( "PER_ABSENCE", String.valueOf( (float)Math.round( ((float)hr_attend[4] / (float)hr_attend[3]) * 1000 ) / 10 ));  //欠席率
            }

        } catch (Exception ex) {
            log.warn("group-average svf-out error!", ex);
        }

    }//printSvfTotalOut()の括り


    /** 
     *  クラス履修単位を取得
     */
    void getRCreditsHr() {
        try {
            for( int i = 0; i < sch_rcredits_hr.length; i++) {
                if (0 < sch_rcredits_hr[i]  &&  hr_credits < sch_rcredits_hr[i] )
                    hr_credits = sch_rcredits_hr[i];
            }
        } catch (Exception ex) {
            log.warn("group-average svf-out error!", ex);
        }

    }


    /** 
     * SQLStatement作成 ＨＲクラス生徒名の表(生徒名) 04/11/08Modify 
     *   SEMESTER_MATはparam._semesterで検索 => 学年末'9'有り
     *   SCHREG_REGD_DATはparam._semeFlgで検索 => 学年末はSCHREG_REGD_HDATの最大学期
     *   2005/09/07 Modify 学年成績の場合、学期は当学期をみる
     *   2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     */
    String prestatStdNameList() {

        final StringBuffer stb = new StringBuffer();

        stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W3.NAME,W7.HR_NAME, ");
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");                                       
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");  
        stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");             
        stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
        stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
        stb.append("FROM    SCHREG_REGD_DAT W1 ");
        stb.append("INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append("INNER   JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(                               "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < EDATE) ");   
        stb.append(                                 "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE >= SDATE) ");
        stb.append(                                 "OR (W4.ENT_DIV IN('4','5') AND '" + param._year + "-04-01' <= W4.ENT_DATE) ) ");
        
        stb.append("LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (TRANSFERCD IN('1','2') AND CASE WHEN EDATE < '" + param._date + "' THEN EDATE ELSE '" + param._date + "' END BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");  
        stb.append("LEFT JOIN SCHREG_REGD_DAT W6 ON W6.YEAR = '" + param._lastyear + "' AND W6.SEMESTER = '" + param._lastyearLastSemester + "' AND W6.SCHREGNO = W1.SCHREGNO ");
        stb.append("LEFT JOIN SCHREG_REGD_HDAT W7 ON W7.YEAR = '" + param._lastyear + "' AND W7.SEMESTER = '" + param._lastyearLastSemester + "' AND W7.GRADE = W6.GRADE AND W7.HR_CLASS = W6.HR_CLASS ");
        stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");
        stb.append(    "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append("ORDER BY W1.ATTENDNO");
        
        return stb.toString();

    }//prestatStdNameListの括り


    /** 
     *  SQLStatement作成 学籍の表 共通部品
     *    GRD_DIV       2:退学  3:転学
     *    TRANSFERCD    1:留学  2:休学  3出停:  4:編入
     *    異動の基準日は印刷処理日とする
     *    2005/01/04 SEMESTER_MSTのリンクを除去
     *    2005/03/09 
     *    2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     */
    String prestateCommonRegd() {

        StringBuffer stb = new StringBuffer();
        stb.append(     "FROM    SCHREG_REGD_DAT   W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  
        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(         "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");          //05/05/30
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END) ");
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END)) ) ");
        //留学開始日が３学期の場合は成績も出力する
        //停学を除外
        stb.append(        "AND NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(                              "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._lastyearSemester3sdate + "' )) ) ");
        return stb.toString();
    }

    /** 
     *  SQLStatement作成 ＨＲ履修科目の表(教科名・科目名・単位・授業時数・平均) 
     *    2005/02/02 Modify 学年末では評価読替え科目を出力する
     *    2005/05/22 Modify 科目名の略称を表記
     *    2005/09/07 Modify 学期成績・学年成績では、読替先科目の授業時数に読替元の合計を代入して出力
     *    2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     *    2005/11/15〜2005/11/25 Modify 
     *                      メイン表に評価読替前または評価読替後のフラグを作成:'REPLACEFLG'
     *                      学期成績の授業時数は１学期からの累計とする(中間・期末は指定学期のみで変更なし)
     *                      評価読替後科目の講座は時間割作成時に作成されるのでSQLで(一時的に)作成しない仕様に変更
     */
    String prestatSubclassInfo() {

        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");
        //今年度の学籍の表
        stb.append("THIS_SCHNO AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(        "AND W1.GRADE||W1.HR_CLASS = ? ");
        stb.append(") ");

        //前年度の学籍の表
        stb.append(",SCHNO AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._lastyear + "' ");
        stb.append(    "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._lastyearLastSemester + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM THIS_SCHNO S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END)) ) ");
        stb.append(        "AND W1.GRADE = '" + param._lastyearGrade + "' ");
        stb.append(") ");

        //前年度単位の表
        stb.append(",SCH_COURSE AS(");
        stb.append(    "SELECT  GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append(    "FROM    SCHNO ");
        stb.append(    "GROUP BY GRADE, COURSECD, MAJORCD, COURSECODE ");
        stb.append(") ");

        //講座の表
        //中間・期末は指定学期のみ集計、学期・学年(変更なし)は指定学期まで年度内累計
        stb.append(",CHAIR_A AS(");
        stb.append(    "SELECT  ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND AS CLASSCD, ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        stb.append(             " T2.SUBCLASSCD AS SUBCLASSCD, MAX(GROUPCD)AS GROUPCD ");
        stb.append(    "FROM    CHAIR_STD_DAT T1,CHAIR_DAT T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._lastyear + "' ");
        stb.append(       "AND  T2.YEAR = '" + param._lastyear + "' ");
        stb.append(       "AND  T1.SEMESTER = T2.SEMESTER ");
        stb.append(       "AND  T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(       "AND  (SUBSTR(T2.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T2.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
        stb.append(       "AND EXISTS(SELECT 'X' FROM SCHNO S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ");
        stb.append(    "GROUP BY ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND , ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        stb.append(              " T2.SUBCLASSCD ");
        stb.append(")");

        //メイン表
        stb.append("SELECT  W2.SUBCLASSCD,W2.GROUPCD,W4.SUBCLASSABBV AS SUBCLASSNAME,W4.ELECTDIV,W5.CLASSABBV,W7.CREDITS,W7.MIN_CREDITS,");
        stb.append(        "W9.AVG_HR,W9.SUM_HR ");
        stb.append(       ",CASE WHEN W10.ATTEND_SUBCLASSCD IS NOT NULL THEN 'MOTO' WHEN W11.COMBINED_SUBCLASSCD IS NOT NULL THEN 'SAKI' ELSE NULL END AS REPLACEFLG ");
        stb.append("FROM    CHAIR_A W2 ");

        stb.append("INNER JOIN SUBCLASS_MST W4 ON ");
		stb.append(" W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || ");
        stb.append(                               " W4.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("INNER JOIN CLASS_MST W5 ON ");
        stb.append(        " W5.CLASSCD || '-' || W5.SCHOOL_KIND = W2.CLASSCD ");

        //単位の表
        stb.append("LEFT JOIN( ");
        stb.append(   "SELECT ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(           " SUBCLASSCD AS SUBCLASSCD, MAX(CREDITS) AS CREDITS, MIN(CREDITS) AS MIN_CREDITS ");
        stb.append(   "FROM    CREDIT_MST W1 ");
        stb.append(   "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(       "AND EXISTS(SELECT 'X' FROM SCH_COURSE S1 ");
        stb.append(                  "WHERE   S1.GRADE = W1.GRADE ");
        stb.append(                      "AND S1.COURSECD = W1.COURSECD ");
        stb.append(                      "AND S1.MAJORCD = W1.MAJORCD ");
        stb.append(                      "AND S1.COURSECODE = W1.COURSECODE) ");
        stb.append(   "GROUP BY ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(             " SUBCLASSCD ");
        stb.append(")W7 ON W7.SUBCLASSCD = W2.SUBCLASSCD ");

        //科目別合計点および平均点の表
        stb.append(    "LEFT JOIN( ");
        stb.append(        "SELECT ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                " SUBCLASSCD AS SUBCLASSCD,");
        stb.append(                "INT(ROUND(AVG(FLOAT(" + fieldname + ")),0))AS AVG_HR,");
        stb.append(                "SUM(" + fieldname + ")AS SUM_HR ");
        stb.append(         "FROM   KIN_RECORD_DAT W1 ");
        stb.append(         "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append(         "WHERE  W1.YEAR = '" + param._lastyear + "' ");
        stb.append(            "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append(            "AND " + fieldname + " IS NOT NULL ");
        stb.append(         "GROUP BY ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                   " SUBCLASSCD");
        stb.append(    ")W9 ON W9.SUBCLASSCD = W2.SUBCLASSCD ");

        //評価読替前科目の表
        stb.append(    "LEFT JOIN (");
        stb.append(        "SELECT ");
        stb.append(        " W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                " ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        stb.append(        "FROM SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(        "WHERE YEAR ='" + param._lastyear + "' AND REPLACECD = '1' ");
        stb.append(        "GROUP BY ");
        stb.append(        " W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                  " ATTEND_SUBCLASSCD ");
        stb.append(    ")W10 ON W10.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
        //評価読替後科目の表
        stb.append(    "LEFT JOIN (");
        stb.append(        "SELECT ");
        stb.append(        " W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                " COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
        stb.append(        "FROM SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(        "WHERE YEAR ='" + param._lastyear + "' AND REPLACECD = '1' ");
        stb.append(        "GROUP BY ");
        stb.append(        " W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                  " COMBINED_SUBCLASSCD ");
        stb.append(    ")W11 ON W11.COMBINED_SUBCLASSCD = W2.SUBCLASSCD ");

        stb.append("ORDER BY W2.SUBCLASSCD ");

        return stb.toString();

    }//prestatSubclassInfo()の括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表 
     *  => ps3
     *    2004/12/12 2004年度1学期の集計データはATTEND_SUBCLASS_DATに入っている。以外はATTEND_DAT、SCH_CHR_DATを集計する。
     *    2005/01/04 処理速度を考慮して出欠データから集計したデータと月別出欠集計データから集計したデータを混合して出力
     *               欠課および出欠の記録は異動した生徒の情報も出力する
     *    2005/02/02 学年末では評価読替え科目を出力する
     *    2005/05/22 講座出欠カウントフラグの参照を追加
     *    2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     *    2005/11/22 Modify 学期成績の欠課時数は１学期からの累計とする
     *    2005/12/09 Modify ペナルティ欠課算出の不具合のため、ATTEND_B表を追加して学期の換算を行う
     */
    String prestatStdSubclassDetail() {

        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表
        stb.append("SCHNO_A AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(        "AND W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30
        stb.append(        "AND W1.ATTENDNO BETWEEN ? AND ? ");
        stb.append(") ");

        //NO005
        stb.append(",TRANS AS ( ");
        stb.append(    "SELECT  W1.SCHREGNO ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(    "INNER   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (TRANSFERCD IN('1','2') AND CASE WHEN EDATE < '" + param._date + "' THEN EDATE ELSE '" + param._date + "' END BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(    ") ");
        //NO005
        stb.append(",SCHNO_TRANS AS ( ");
        stb.append(    "SELECT W3.SCHREGNO, ");
        stb.append(    "       SUM(GRADE_RECORD) AS SUM_REC ");
        stb.append(    "FROM   KIN_RECORD_DAT W3 ");
        stb.append(    "WHERE  W3.YEAR='" + param._lastyear + "' AND ");
        stb.append(    "       EXISTS(SELECT  'X' FROM TRANS W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        stb.append(    "GROUP BY W3.SCHREGNO ");
        stb.append(    "HAVING SUM(GRADE_RECORD) IS NULL ");
        stb.append(    ") ");

        stb.append(",SCHNO_B AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._lastyear + "' ");
        stb.append(    "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._lastyearLastSemester + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END)) ) ");
        // 留学開始日が３学期の場合は成績も出力する
        stb.append(        "AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                           "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(                             "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._lastyearSemester3sdate + "' )) ) ");
        stb.append(        "AND W1.GRADE = '" + param._lastyearGrade + "' ");

        stb.append(        "AND NOT EXISTS(SELECT 'X' FROM SCHNO_TRANS S2 WHERE S2.SCHREGNO = W1.SCHREGNO) ");
        stb.append(") ");

        //対象講座の表
        //(何れの表にも)欠課超過単位をみるため、指定学期までの講座を抽出する、
        stb.append(",CHAIR_A AS(");
        stb.append(    "SELECT  ");
		stb.append(" T2.CLASSCD, ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        stb.append(             " T2.SUBCLASSCD AS SUBCLASSCD, T2.CHAIRCD, T2.SEMESTER ");
        stb.append(    "FROM    CHAIR_STD_DAT T1,CHAIR_DAT T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._lastyear + "' ");
        stb.append(       "AND  T2.YEAR = '" + param._lastyear + "' ");
        stb.append(       "AND  T1.SEMESTER = T2.SEMESTER ");
        stb.append(       "AND  T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(       "AND  (T2.CLASSCD <= '" + KNJDefineSchool.subject_U + "' OR T2.CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
        stb.append(       "AND EXISTS(SELECT 'X' FROM SCHNO_B S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ");
        stb.append(    "GROUP BY ");
		stb.append(" T2.CLASSCD, ");
		stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        stb.append(              " T2.SUBCLASSCD, T2.CHAIRCD, T2.SEMESTER ");
        stb.append(")");

        //欠課数の表
        //param._output5 != nul: 遅刻・早退を欠課に換算する
        //中間・期末における欠課表記は学期内累積、学期・学年における欠課表記は年度内累積とするため、２通りの欠課時数を持つようにする
        //05/12/09 列にSEMESTERを追加し、学期別の集計だけ行う
        stb.append(",ATTEND_A AS(");
                //月別科目別出欠集計データより欠課を取得
        stb.append(          "SELECT  W1.SCHREGNO, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                " W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
        stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
        if (null != _knjSchoolMst._subOffDays && _knjSchoolMst._subOffDays.equals("1")) {
            stb.append("            + VALUE(OFFDAYS,0) ");
        }
        if (null != _knjSchoolMst._subSuspend && _knjSchoolMst._subSuspend.equals("1")) {
            stb.append("            + VALUE(SUSPEND, 0) ");
        }
        if ("true".equals(param._useVirus)) {
            if (null != _knjSchoolMst._subVirus && _knjSchoolMst._subVirus.equals("1")) {
                stb.append("            + VALUE(VIRUS, 0) ");
            }
        }
        if ("true".equals(param._useKoudome)) {
            if (null != _knjSchoolMst._subKoudome && _knjSchoolMst._subKoudome.equals("1")) {
                stb.append("            + VALUE(KOUDOME, 0) ");
            }
        }
        if (null != _knjSchoolMst._subMourning && _knjSchoolMst._subMourning.equals("1")) {
            stb.append("            + VALUE(MOURNING, 0) ");
        }
        if (null != _knjSchoolMst._subAbsent && _knjSchoolMst._subAbsent.equals("1")) {
            stb.append("            + VALUE(ABSENT, 0) ");
        }
        stb.append(                  ") AS ABSENT1, ");
        stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
        stb.append(          "WHERE   W1.YEAR = '" + param._lastyear + "' AND ");
        stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(          "GROUP BY W1.SCHREGNO, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                 " W1.SUBCLASSCD, W1.SEMESTER ");
        stb.append(     ") ");

        //学期内のペナルティ欠課換算を行う
        if (param._output5 != null) {
            stb.append(",ATTEND_A2 AS(");
            stb.append(     "SELECT SCHREGNO, SEMESTER, SUBCLASSCD, ");
            stb.append(     "SUM(ABSENT1) AS ABSENT1, ");
            stb.append(     "SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(     "FROM   ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO, SEMESTER, SUBCLASSCD ");
            stb.append(     ") ");
        }
        
        stb.append(",ATTEND_B AS(");
        stb.append(     "SELECT SCHREGNO, SUBCLASSCD, ");
        if (param._output5 != null) {
            stb.append(        "SUM(VALUE(ABSENT1 ,0)) + SUM(VALUE(LATE_EARLY, 0) / 3) AS ABSENT1 ");
            stb.append(       ",SUM( CASE WHEN SEMESTER = '" + param._lastyearLastSemester + "' THEN VALUE(ABSENT1,0) + VALUE(LATE_EARLY, 0) / 3 ELSE 0 END ) AS ABSENT2 ");
            stb.append(     "FROM   ATTEND_A2 ");
        } else{
            stb.append(        "SUM(VALUE(ABSENT1, 0)) AS ABSENT1 ");
            stb.append(       ",SUM( CASE WHEN SEMESTER = '" + param._lastyearLastSemester + "' THEN VALUE(ABSENT1,0) ELSE 0 END ) AS ABSENT2 ");
            stb.append(     "FROM   ATTEND_A ");
        }
        stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        stb.append(     ") ");


        //成績データの表（通常科目）
        stb.append(",RECORD_REC AS(");
        stb.append( prestatStdSubclassRecord( 0 ) );
        stb.append(     ") ");

        //評定読替え科目評定の表
        if (param._testkindcd.equals("0")) {
            stb.append(",REPLACE_REC AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(        " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append(            "SCORE, ");
            stb.append(            "PATTERN_ASSESS ");
            stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.SUBCLASSCD = ");
            stb.append(        " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(                             " W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "W2.YEAR='" + param._lastyear + "' AND REPLACECD='1' ");
            stb.append(     ") ");

            stb.append(",REPLACE_REC_CNT AS(");
            stb.append(     "SELECT SCHREGNO,SUBCLASSCD,");
            stb.append(            "sum(case when score in ('KS','( )','欠') then 1 else 0 end) as ks ");
            stb.append(     "FROM REPLACE_REC W1 ");
            stb.append(     "WHERE w1.score in('KK','KS','( )','欠','公') ");
            stb.append(     "GROUP BY SCHREGNO,SUBCLASSCD ");
            stb.append(     "having 0 < count(*) ");
            stb.append(     ") ");

            //05/03/09
            stb.append(",REPLACE_REC_ATTEND AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(        " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append(            fieldname + " AS SCORE, ");
            stb.append(            "CASE VALUE(JUDGE_PATTERN,'X') WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'C' THEN C_PATTERN_ASSESS ");
            stb.append(                                          "ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(     "FROM   KIN_RECORD_DAT W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + param._lastyear + "' AND ");
            stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            stb.append(            "W1.SUBCLASSCD = ");
            stb.append(        " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(                             " W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
            stb.append(            "W2.YEAR='" + param._lastyear + "' AND REPLACECD='1' ");
            stb.append(     ") ");
         }

        //メイン表
        stb.append("SELECT  T1.SUBCLASSCD,T1.SCHREGNO,");
        stb.append(        "CASE WHEN SCORE IS NULL THEN NULL ELSE T5.ABSENT1 END AS ABSENT1, ");
        stb.append(        "T5.ABSENT2, ");

        stb.append(        "SCORE, SCORE_FLG, DICD, ");

        stb.append(        "CASE WHEN ");
        stb.append(                   " T1.CLASSCD ");
        stb.append(                   " <> '90' THEN PATTERN_ASSESS ");
        stb.append(             "ELSE (SELECT ASSESSMARK FROM RELATIVEASSESS_MST S1 ");
        stb.append(                   "WHERE  S1.GRADE = '" + param._lastyearGrade + "' AND ");
        stb.append(                          "S1.ASSESSCD = '3' AND ");
        stb.append(        " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
        stb.append(                          "S1.SUBCLASSCD = T1.SUBCLASSCD AND ");
        stb.append(                          "ASSESSLOW <= INT(PATTERN_ASSESS) AND INT(PATTERN_ASSESS) <= ASSESSHIGH )END ");
        stb.append(             "AS PATTERN_ASSESS ");
        if (param._testkindcd.equals("0") )
            stb.append(    ", REPLACEMOTO ");
        stb.append(        ", T6.CREDITS ");

        //対象生徒・講座の表
        stb.append("FROM(");
        stb.append(     "SELECT  W1.SCHREGNO, ");
        stb.append(               "W2.CLASSCD, ");
        stb.append(              " W2.SUBCLASSCD ");
        stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_B W3 ");
        stb.append(     "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(        "AND  W1.CHAIRCD = W2.CHAIRCD ");
        if (! param._lastyearSemester.equals("9")) {
            stb.append(    "AND  W1.SEMESTER = '" + param._lastyearLastSemester + "' ");
            stb.append(    "AND  W2.SEMESTER = '" + param._lastyearLastSemester + "' ");
        }
        stb.append(        "AND  W1.SEMESTER = W2.SEMESTER ");
        stb.append(        "AND  W1.SCHREGNO = W3.SCHREGNO ");
        stb.append(     "GROUP BY W1.SCHREGNO ");
        stb.append(               ",W2.CLASSCD ");
        stb.append(               ",W2.SUBCLASSCD ");
        stb.append(")T1 ");

        //成績の表
        stb.append(  "LEFT JOIN(");
        //成績の表（通常科目）
        if (param._testkindcd.equals("0")) {
            stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, W3.SCORE_FLG, W3.DICD, ");
            stb.append(            "(SELECT COUNT(*) FROM REPLACE_REC S1 WHERE S1.SCHREGNO = W3.SCHREGNO AND ");
            stb.append(                                                       "S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(                                                 "GROUP BY ATTEND_SUBCLASSCD)AS REPLACEMOTO ");
            stb.append(     "FROM   RECORD_REC W3 ");
            stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
        } else{
            stb.append(     "SELECT W3.* ");
            stb.append(     "FROM   RECORD_REC W3 ");
        }
        if (param._testkindcd.equals("0")) {
            //評定読替え科目の成績の表
            stb.append("UNION ALL ");
            stb.append("SELECT  W1.SCHREGNO, W1.SUBCLASSCD ");
            stb.append(       ",RTRIM(CHAR(SCORE)) AS SCORE ");
            stb.append(       ", PATTERN_ASSESS ");
            stb.append(       ", SCORE_FLG ");
            stb.append(       ", DICD ");
            stb.append(       ", -1 AS REPLACEMOTO ");
            stb.append("FROM   RECORD_REC W1 ");
            stb.append("WHERE  EXISTS(SELECT  'X'   FROM REPLACE_REC W2 ");
            stb.append(              "WHERE   W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                 "AND  W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append(              "GROUP BY W2.SUBCLASSCD) ");
            stb.append(   "AND NOT EXISTS(SELECT  'X'  ");
            stb.append(                  "FROM    REPLACE_REC_CNT W2 ");
            stb.append(                  "WHERE   W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                      "AND W2.SUBCLASSCD = W1.SUBCLASSCD) ");
            stb.append(   "AND W1.SCORE NOT IN('KK','KS','( )','欠','公') ");
            stb.append(   "AND W1.SCORE IS NOT NULL ");
            stb.append(   "AND EXISTS(SELECT 'X' FROM SCHNO_B W3 WHERE W3.SCHREGNO = W1.SCHREGNO) ");

            stb.append(     "UNION ALL ");
            stb.append(     "SELECT SCHREGNO,SUBCLASSCD, ");
            stb.append(            "case when 0 < ks then '欠' else '公' end AS SCORE, ");
            stb.append(            "'' AS PATTERN_ASSESS, ");
            stb.append(            "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE_FLG, ");
            stb.append(            "''  AS DICD,");
            stb.append(            "-1 AS REPLACEMOTO ");
            stb.append(     "FROM REPLACE_REC_cnt W1 ");
            stb.append(     "WHERE EXISTS(SELECT 'X' FROM SCHNO_B W3 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        }
        stb.append(     ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");
        //欠課数の表
        stb.append("LEFT JOIN(");
        stb.append(     "SELECT  W1.SCHREGNO, W1.SUBCLASSCD, ABSENT1, ABSENT2 "); //'ABSENT2'を追加 
        stb.append(     "FROM    ATTEND_B W1 ");
        //評定読替え科目の欠課数の表
        stb.append(     "UNION ");
        stb.append(     "SELECT  W2.SCHREGNO, ");
        stb.append(        " W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                           " W1.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(ABSENT1)AS ABSENT1, SUM(ABSENT2)AS ABSENT2 "); //'ABSENT2'を追加 
        stb.append(     "FROM    SUBCLASS_REPLACE_COMBINED_DAT W1, ATTEND_B W2 ");
        stb.append(     "WHERE   W1.YEAR ='" + param._lastyear + "' AND W1.REPLACECD = '1' ");
        stb.append(         "AND ");
        stb.append(        " W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(              " W1.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(     "GROUP BY W2.SCHREGNO, ");
        stb.append(        " W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                            " W1.COMBINED_SUBCLASSCD ");
        stb.append(")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");

        //単位
        stb.append("LEFT JOIN(");
        stb.append(     "SELECT  W2.SCHREGNO, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                           " W1.SUBCLASSCD AS SUBCLASSCD, MAX(W1.CREDITS) AS CREDITS ");
        stb.append(     "FROM    CREDIT_MST W1, SCHNO_B W2 ");
        stb.append(     "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(         "AND W2.GRADE = W1.GRADE ");
        stb.append(         "AND W2.COURSECD = W1.COURSECD ");
        stb.append(         "AND W2.MAJORCD = W1.MAJORCD ");
        stb.append(         "AND W2.COURSECODE = W1.COURSECODE ");
        stb.append(     "GROUP BY ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(               " SUBCLASSCD,SCHREGNO ");
        stb.append(")T6 ON T6.SUBCLASSCD = T1.SUBCLASSCD AND T6.SCHREGNO = T1.SCHREGNO ");

        //留学者・休学者 NO006

        stb.append("ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

        return stb.toString();

    }//prestatStdSubclassDetailの括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表における成績データの再構築
     */
    String prestatStdSubclassRecord(int sdiv) {
        return "";
    }


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表 部品
     */
    String prestatStdSubclassDetailDetail() {
        return null;
    }


    /** 
     *  PrepareStatement作成 成績総合データ 
     *  => ps4
     *     2005/02/13Modify KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様変更により修正
     */
    String prestatStdTotalRec() {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH SCHNO_B AS(");
        stb.append(     "SELECT W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(     "FROM    SCHREG_REGD_DAT   W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(         "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END) ");     
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END)) ) ");  
        // 留学開始日が３学期の場合は成績も出力する
        // 停学を除外
        stb.append(        "AND NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  
        stb.append(                              "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._lastyearSemester3sdate + "' )) ) ");  
        stb.append(     "),");

        stb.append("TRANS AS ( ");
        stb.append(    "SELECT  W1.SCHREGNO ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(    "INNER   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (TRANSFERCD IN('1','2') AND CASE WHEN EDATE < '" + param._date + "' THEN EDATE ELSE '" + param._date + "' END BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_B S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(    "), ");

        stb.append("SCHNO_TRANS AS ( ");
        stb.append(    "SELECT W3.SCHREGNO, ");
        stb.append(    "       SUM(GRADE_RECORD) AS SUM_REC ");
        stb.append(    "FROM   KIN_RECORD_DAT W3 ");
        stb.append(    "WHERE  W3.YEAR='" + param._lastyear + "' AND ");
        stb.append(    "       EXISTS(SELECT  'X' FROM TRANS W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        stb.append(    "GROUP BY W3.SCHREGNO ");
        stb.append(    "HAVING SUM(GRADE_RECORD) IS NULL ");
        stb.append(    "), ");

        stb.append("SCHNO_B2 AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._lastyearLastSemester + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_B S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND NOT EXISTS(SELECT 'X' FROM SCHNO_TRANS S2 WHERE S2.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND W1.GRADE = '" + param._lastyearGrade + "' ");
        stb.append("), ");

        //成績データの表（通常科目） 読替科目は含めない
        stb.append("RECORD_REC AS(");
        stb.append( prestatStdSubclassRecord( 1 ) );
        stb.append("AND NOT EXISTS(SELECT 'X' ");
        stb.append(               "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(               "WHERE  W3.SUBCLASSCD = ");
        stb.append(        " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                                       " W2.COMBINED_SUBCLASSCD AND ");
        stb.append(                      "W2.YEAR ='" + param._lastyear + "' AND ");
        stb.append(                      "REPLACECD = '1') ");
        stb.append(     ") ");

        //メイン表
        stb.append("SELECT T1.SCHREGNO,");
        stb.append(       "T4.SUM_REC,T5.AVG_REC,T4.CREDITS,T4.CREDITS2,T5.RANK ");

        stb.append("FROM   SCHNO_B2 T1 ");
        //成績
        stb.append(     "LEFT JOIN(");
        stb.append(         "SELECT  W1.SCHREGNO,");
        stb.append(                 "SUM(INT(SCORE))AS SUM_REC,");
        stb.append(                 "SUM(CREDITS)AS CREDITS,");
        if (param._lastyearSemester.equals("9")) {
            stb.append(             "SUM(CASE WHEN '1' < PATTERN_ASSESS THEN CREDITS ELSE 0 END)AS CREDITS2 ");
        } else {
            stb.append(             "0 AS CREDITS2 ");
        }
        stb.append(         "FROM    RECORD_REC W1 ");
        stb.append(                 "INNER JOIN SCHNO_B W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append(                 "LEFT JOIN CREDIT_MST W3 ON W3.YEAR = '" + param._lastyear + "' AND ");
        stb.append(                                            "W3.GRADE = '" + param._lastyearGrade + "' AND ");
        stb.append(                                            "W3.COURSECD = W2.COURSECD AND ");
        stb.append(                                            "W3.MAJORCD = W2.MAJORCD AND ");
        stb.append(                                            "W3.COURSECODE = W2.COURSECODE AND ");
        stb.append(        " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        stb.append(                                            "W3.SUBCLASSCD = W1.SUBCLASSCD ");
        stb.append(         "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') AND ");
        stb.append(                 "W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append("                 AND NOT EXISTS(SELECT 'X' ");
        stb.append(               "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(               "WHERE  W1.SUBCLASSCD = ");
        stb.append(        " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                                       " W2.ATTEND_SUBCLASSCD AND ");
        stb.append(                      "W2.YEAR ='" + param._lastyear + "' AND ");
        stb.append(                      "REPLACECD = '1') ");

        stb.append(         "GROUP BY W1.SCHREGNO");
        stb.append(     ")T4 ON T4.SCHREGNO = T1.SCHREGNO ");

        //平均点、席次の表
        stb.append(     "LEFT JOIN(");
        stb.append(         "SELECT  W1.SCHREGNO,");
        stb.append(                 "AVG(FLOAT(INT(SCORE))) AS AVG_REC,");
        stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");
        stb.append(                 "RANK() OVER(ORDER BY CASE ");
        stb.append(                                "WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
        stb.append(                                "ELSE AVG(FLOAT(INT(SCORE))) END DESC)END AS RANK ");
        stb.append(         "FROM    RECORD_REC W1 ");
        stb.append(         "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') AND ");
        stb.append(                 "W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' AND ");
                                    //１科目でも欠席があれば除外
        stb.append(                 "SCHREGNO NOT IN(SELECT SCHREGNO ");
        stb.append(                                 "FROM   RECORD_REC ");
        stb.append(                                 "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )','欠','公') AND ");
        stb.append(                                        "CLASSCD <= '" + KNJDefineSchool.subject_U + "'  ");
        stb.append(                                 "GROUP BY SCHREGNO ");
        stb.append(                                 "HAVING 0 < COUNT(*) ");
        stb.append(                                 ") ");

        stb.append("AND NOT EXISTS(SELECT 'X' ");
        stb.append(               "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(               "WHERE  W1.SUBCLASSCD = ");
        stb.append(        " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                                       " W2.COMBINED_SUBCLASSCD AND ");
        stb.append(                      "W2.YEAR ='" + param._lastyear + "' AND ");
        stb.append(                      "REPLACECD = '1') ");

        stb.append(         "GROUP BY W1.SCHREGNO");
        stb.append(     ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");

        return stb.toString();

    }//prestatStdTotalRec()の括り


    /** 
     *  PrepareStatement作成 出欠総合データ 
     *  => ps5
     *    2004/11/08 退学・転学・編入者は除外
     *    2004/12/10 2004年度1学期の集計データはATTEND_SEMES_DATに入っている。以外はATTEND_DAT、SCH_CHR_DATを集計する。
     *    2005/01/04 処理速度を考慮して出欠データから集計したデータと月別出欠集計データから集計したデータを混合して出力
     *               出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。2005/02/01
     *    2005/01/31 param._semeFlg:学年末は現在学期(CONTRL_MST)、以外は指定学期
     *    2005/02/02 留学日数を出席すべき日数から除算する
     *    2005/10/05 学籍不在日数に編入と転入を追加
     *               忌引または出停の日は遅刻および早退をカウントしない
     */
    String prestatStdTotalAttend() {
        final StringBuffer stb = new StringBuffer();

        //対象生徒
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  W1.SCHREGNO ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(        "AND W1.GRADE||W1.HR_CLASS = ? ");
        stb.append(     ") ");


        stb.append(",TRANS AS ( ");
        stb.append(    "SELECT  W1.SCHREGNO ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(    "INNER   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (TRANSFERCD IN('1','2') AND CASE WHEN EDATE < '" + param._date + "' THEN EDATE ELSE '" + param._date + "' END BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(    ") ");

        stb.append(",SCHNO_TRANS AS ( ");
        stb.append(    "SELECT W3.SCHREGNO, ");
        stb.append(    "       SUM(GRADE_RECORD) AS SUM_REC ");
        stb.append(    "FROM   KIN_RECORD_DAT W3 ");
        stb.append(    "WHERE  W3.YEAR='" + param._lastyear + "' AND ");
        stb.append(    "       EXISTS(SELECT  'X' FROM TRANS W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        stb.append(    "GROUP BY W3.SCHREGNO ");
        stb.append(    "HAVING SUM(GRADE_RECORD) IS NULL ");
        stb.append(    ") ");

        stb.append(",SCHNO_B2 AS(");
        stb.append(    "SELECT  W1.SCHREGNO ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._lastyearLastSemester + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO S1 WHERE S1.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND NOT EXISTS(SELECT 'X' FROM SCHNO_TRANS S2 WHERE S2.SCHREGNO = W1.SCHREGNO) ");
        stb.append(        "AND W1.GRADE = '" + param._lastyearGrade + "' ");
        stb.append(") ");

        //メイン表
        stb.append(   "SELECT  TT0.SCHREGNO, ");
                                //授業日数
        stb.append(           "VALUE(TT7.LESSON,0) AS LESSON, ");
                                //出席すべき日数
        stb.append(           "VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) AS MLESSON, ");
                                //各種欠席日数
        stb.append(           "VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
        stb.append(           "VALUE(TT7.MOURNING,0) AS MOURNING, ");
        stb.append(           "VALUE(TT7.ABSENT,0) AS ABSENT, ");
                                //出席日数
        stb.append(            " VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) - VALUE(TT7.ABSENT,0) AS PRESENT, ");
                                //遅刻・早退
        stb.append(           "VALUE(TT7.LATE,0) AS LATE, ");
        stb.append(           "VALUE(TT7.EARLY,0) AS EARLY, ");
                                //留学日数
        stb.append(           "VALUE(TT7.ABROAD,0) AS TRANSFER_DATE ");

        stb.append(   "FROM    SCHNO_B2 TT0 ");

        //月別集計データから集計した表
        // 授業日数は学籍不在日は除外されており、留学・休学日は引算する
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT SCHREGNO, ");
        stb.append(                   "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(               " + VALUE(SUM(OFFDAYS),0) ");
        }
        stb.append(                   "AS LESSON, ");
        stb.append(                   "SUM(MOURNING) AS MOURNING, ");
        stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
        stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(                   " + VALUE(OFFDAYS, 0) ");
        }
        stb.append(                   ") AS ABSENT, ");
        stb.append(                   "SUM(LATE) AS LATE, ");
        stb.append(                   "SUM(EARLY) AS EARLY, ");
        stb.append(                   "SUM(ABROAD) AS ABROAD, ");
        stb.append(                   "SUM(OFFDAYS) AS OFFDAYS ");
        stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
        stb.append(            "WHERE  YEAR = '" + param._lastyear + "' AND ");
        stb.append(                   "EXISTS(");
        stb.append(                       "SELECT  'X' ");
        stb.append(                       "FROM    SCHNO W2 ");
        stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
        stb.append(            "GROUP BY SCHREGNO ");
        stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

        stb.append("ORDER BY SCHREGNO");
        
        return stb.toString();

    }//prestatStdTotalAttend()の括り


    /**
     *  クラス内で使用する定数設定
     *    2005/05/22
     */
    void setClasscode()
    {
        try {
            definecode = new KNJDefineSchool();
            definecode.defineCode( db2, param._lastyear );        //各学校における定数等設定
log.debug("semesdiv="+definecode.semesdiv + "   absent_cov="+definecode.absent_cov + "   absent_cov_late="+definecode.absent_cov_late);
        } catch (Exception ex) {
            log.warn("semesterdiv-get error!", ex);
        }
    }


    /**
     *  SVF-FORM フィールドを初期化
     */
    private int clearSvfField( Map<Integer, String> hm2, String lschname[] )
    {
log.debug("clearSvfField() check");
        try {
            svf.VrSetForm("KNJD064.frm", 4);
            set_head2();

            svf.VrsOut( "year2", nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");    //年度          
            svf.VrsOut( "ymd1",  KNJ_EditDate.h_format_JP(param._printDate));
            svf.VrsOut( "DATE",  KNJ_EditDate.h_format_JP(param._lastyearSemester9sdate) + " \uFF5E " + KNJ_EditDate.h_format_JP(param._lastyearAttenddate));         //出欠集計範囲

            printSvfStdNameOut( hm2, lschname );                            //生徒名等出力のメソッド

            //  組名称及び担任名の取得
            try {
                KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
                KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
                returnval = getinfo.Hrclass_Staff( db2, param._year, param._semester, param._classSelected, "" );
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
            for( int i = 0 ; i < sch_rcredits_hr.length ; i++ )sch_rcredits_hr[i] = 0;
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
     *  PrepareStatement作成 非皆勤者の抽出
     *   2005/07/04
     *   2005/10/05 編入（データ仕様の変更による）について修正、および転学を追加
     *   2005/10/05 学籍不在日数に編入と転入を追加
     *              忌引または出停の日は遅刻および早退をカウントしない
     *   2005/11/11 留学日数と休学日数のカウントを分ける => 留学は非皆勤の対象ではない
     */
    final String prestatAttendKaikin() {
        final StringBuffer stb = new StringBuffer();

        //対象生徒
        stb.append("WITH ");
        stb.append("SCHNO AS(");
        stb.append(     "SELECT  W1.SCHREGNO ");
        stb.append(            ",CASE WHEN W4.SCHREGNO IS NOT NULL THEN 1 WHEN W5.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
        stb.append(     "FROM    SCHREG_REGD_DAT   W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");
        stb.append(     "LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                    "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END) ");   
        stb.append(                                      "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END)) ");  
        stb.append(     "LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                        "AND ((W5.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
        stb.append(                                          "OR (W5.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._lastyearAttenddate + "' THEN W2.EDATE ELSE '" + param._lastyearAttenddate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE)) ");  
        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(             "W1.SEMESTER = '" + param._semeFlg + "' AND ");
        stb.append(             "W1.GRADE||W1.HR_CLASS = ? ");
        stb.append(") ");

        //欠課数の表
        stb.append(",SUBCLASS_ATTEND_A AS(");
        stb.append(     "SELECT SCHREGNO, SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(            "SUM(LATE_EARLY) AS LATE_EARLY, ");
        if (param._output5 == null) {
            stb.append(        "SUM(ABSENT1) AS ABSENT1 ");
        } else {
            stb.append(        "SUM( VALUE(ABSENT1,0) + VALUE(LATE_EARLY,0)/3 ) AS ABSENT1 ");
        }
                //出欠データより集計
        stb.append(     "FROM ( ");
                //月別科目別出欠集計データより欠課を取得
        stb.append(          "SELECT  W1.SCHREGNO, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                "W1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append("            + VALUE(OFFDAYS, 0) ");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append("            + VALUE(SUSPEND, 0) ");
        }
        if ("true".equals(param._useVirus)) {
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("            + VALUE(VIRUS, 0) ");
            }
        }
        if ("true".equals(param._useKoudome)) {
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("            + VALUE(KOUDOME, 0) ");
            }
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append("            + VALUE(MOURNING, 0) ");
        }
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append("            + VALUE(ABSENT, 0) ");
        }
        stb.append(                  " ) AS ABSENT1, ");
        stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1 ");
        stb.append(          "WHERE   W1.YEAR = '" + param._lastyear + "' ");
        stb.append(          "GROUP BY W1.SCHREGNO, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                 " W1.SUBCLASSCD, W1.SEMESTER ");
        stb.append(          ")S1 ");
        stb.append(     "GROUP BY S1.SCHREGNO, SUBCLASSCD ");
        stb.append(") ");

        stb.append(",SUBCLASS_ATTEND_B AS(");
        stb.append(     "SELECT  SCHREGNO, SUM(ABSENT1) AS ABSENT1, SUM(LATE_EARLY) AS LATE_EARLY ");
        stb.append(     "FROM    SUBCLASS_ATTEND_A ");
        stb.append(     "GROUP BY SCHREGNO ");
        stb.append(     "HAVING 0 < SUM(ABSENT1) OR 0 < SUM(LATE_EARLY) ");
        stb.append(") ");
        
        //メイン表
        stb.append(   "SELECT  TT0.SCHREGNO, ");
        stb.append(           "TT0.LEAVE, ");
                                //授業日数
                                //出席すべき日数
        stb.append(           "VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) AS MLESSON, ");
                                //各種欠席日数
        stb.append(           "VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
        stb.append(           "VALUE(TT7.MOURNING,0) AS MOURNING, ");
        stb.append(           "VALUE(TT7.ABSENT,0) AS ABSENT, ");
                                //出席日数
                                //遅刻・早退
        stb.append(           "VALUE(TT7.LATE,0) AS LATE, ");
        stb.append(           "VALUE(TT7.EARLY,0) AS EARLY, ");
                                //留学日数
        stb.append(           "VALUE(TT7.ABROAD,0) AS RTRANSFER_DATE, ");
                                //休学日数
        stb.append(           "VALUE(TT7.OFFDAYS,0) AS KTRANSFER_DATE, ");
                                //欠課および授業の遅刻・早退
        stb.append(           "TT13.ABSENT1, TT13.LATE_EARLY ");
        stb.append(   "FROM    SCHNO TT0 ");
        //月別集計データから集計した表
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT SCHREGNO, ");
        stb.append(                   "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) ");
        if (null != _knjSchoolMst._semOffDays && _knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                   " + VALUE(SUM(OFFDAYS),0) ");
        }
        stb.append(                   "AS LESSON, ");  //05/10/07 Modify
        stb.append(                   "SUM(MOURNING) AS MOURNING, ");
        stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
        stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if (null != _knjSchoolMst._semOffDays && _knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                   " + VALUE(OFFDAYS, 0) ");
        }
        stb.append(                   ") AS ABSENT, ");
        stb.append(                   "SUM(LATE) AS LATE, ");
        stb.append(                   "SUM(EARLY) AS EARLY, ");
        stb.append(                   "SUM(ABROAD) AS ABROAD, ");
        stb.append(                   "SUM(OFFDAYS) AS OFFDAYS ");
        stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
        stb.append(            "WHERE  YEAR = '" + param._lastyear + "' AND ");
        stb.append(                   "EXISTS(");
        stb.append(                       "SELECT  'X' ");
        stb.append(                       "FROM    SCHNO W2 ");
        stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
        stb.append(            "GROUP BY SCHREGNO ");
        stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");
        //欠課の表
        stb.append(   "LEFT JOIN SUBCLASS_ATTEND_B TT13 ON TT13.SCHREGNO = TT0.SCHREGNO ");

        stb.append("ORDER BY SCHREGNO");

        return stb.toString();
    }


    /**
     *  非皆勤者をリスト<List nokaikin>に保管
     *   留学日数と休学日数のカウントを分ける => 留学は非皆勤の対象ではない
     */
    final void setAttendNoKaikin( PreparedStatement ps, List<String> nokaikin ) {
        try {
            if (0 < nokaikin.size())nokaikin.clear();
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        ResultSet rs = null;
        try {
            int p = 0;
            ps.setString( ++p, param._classSelected  );         //学年・組
log.debug("kaikin ps start");
            rs = ps.executeQuery();
log.debug("kaikin ps end");

            while( rs.next()) {
                if (rs.getString("MLESSON") != null  &&  0 < Integer.parseInt( rs.getString("MLESSON")) )
                    if (rs.getString("ABSENT")        != null  &&  0 < Integer.parseInt( rs.getString("ABSENT")       )  ||
                        rs.getString("EARLY")         != null  &&  0 < Integer.parseInt( rs.getString("EARLY")        )  ||
                        rs.getString("LATE")          != null  &&  0 < Integer.parseInt( rs.getString("LATE")         )  ||
                        rs.getString("KTRANSFER_DATE") != null  &&  0 < Integer.parseInt( rs.getString("KTRANSFER_DATE"))  ||    // 休学日数のみ対処とする
                        rs.getString("LATE_EARLY")    != null  &&  0 < Integer.parseInt( rs.getString("LATE_EARLY")   )  ||
                        rs.getString("ABSENT1")       != null  &&  0 < Integer.parseInt( rs.getString("ABSENT1"))) {
                        if (! nokaikin.contains( rs.getString("SCHREGNO")) )  //要素がない場合追加
                            nokaikin.add( rs.getString("SCHREGNO"));
                    }

                if (rs.getString("LEAVE") != null  &&  Integer.parseInt( rs.getString("LEAVE")) == 1 )
                        if (! nokaikin.contains( rs.getString("SCHREGNO")) )  //要素がない場合追加
                            nokaikin.add( rs.getString("SCHREGNO"));
            }

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *  欠課時数の印刷: 欠課時数要注意者または超過者は'true'を返す
     *  2005/11/16 Build 欠課時数の網掛け処理を学期成績にも適用する(学年成績のみであったが)ため処理を独立
     *  引数について
     *      int i: 出力行 / int j: 出力列 / int subclassjisu: 科目別単位数
     */
    final boolean printSvfStdDetailKekka( ResultSet rs, int i, int j, int subclassjisu) {
        boolean amikake = false;
        try {
            //欠課出力/網掛け設定/履修単位の加算処理
            if (rs.getString("ABSENT1") != null) {
                if (Integer.parseInt( param._lastyearGrade ) == 3) {
                    //３年生の欠課時数網掛け設定
                    if (Integer.parseInt(param._lastyearLastSemester) == 1) {
                         if (rs.getInt("ABSENT1") >= subclassjisu * 8 * Integer.parseInt(param._lastyearLastSemester) / 2 + 1 )amikake = true;
                    }else {
                         if (rs.getInt("ABSENT1") >= subclassjisu * 8 + 1 )amikake = true;
                    }
                }else{
                    //1・２年生の欠課時数網掛け設定
                    if (Integer.parseInt(param._lastyearLastSemester) < 3) {
                         if (rs.getInt("ABSENT1") >= subclassjisu * 10 * Integer.parseInt(param._lastyearLastSemester) / 3 + 1 )amikake = true;
                    }else {
                         if (rs.getInt("ABSENT1") >= subclassjisu * 10 + 1 )amikake = true;
                    }
                }
//log.debug("subclassjisu="+subclassjisu + "   amikake="+amikake+ "  absent1="+rs.getInt("ABSENT1"));
                if (amikake) {
                    svf.VrAttributen( "kekka" + i, j, "Paint=(2,70,1),Bold=1" );
                }
                if (Integer.parseInt( rs.getString("ABSENT1")) != 0) {
                    svf.VrsOutn( "kekka" + i, j, rs.getString("ABSENT1")); //欠課
                }
            }

            //網掛け解除
            if (amikake) {
                svf.VrAttributen( "kekka" + i, j, "Paint=(0,0,0),Bold=0" );
            }

        } catch (Exception ex) {
            log.error("printSvfStdDetailKekka error! ", ex);
        }
        return amikake;
    }


    /**
     *  履修単位の累積処理: 履修単位＝通常科目＋読替後科目−欠課時数超過科目
     *  履修単位印刷を中間・期末・学期成績にも適用する(学年成績のみであったが)ため処理を独立
     *  引数について
     *      int i: 出力行 / int subclassjisu: 科目別単位数 / int replaceflg: 科目種別 0:通常 1:読替元 -1:読替後
     */
    final protected void accumuStdRCredits( ResultSet rs, int i, int subclassjisu, int replaceflg ) {
        try {
            boolean rflg = true;
            int s = ( Integer.parseInt( param._lastyearGrade ) == 3 )? 8: 10; //param._grade: 学年

            if (0 < replaceflg) {
                rflg = false;
            } else {
                if (rs.getString("ABSENT1") != null  &&  ( subclassjisu * s + 1 ) <=  Integer.parseInt( rs.getString("ABSENT1"))) {
                    rflg = false;
                }
                sch_rcredits_hr[ i ] += subclassjisu; //履修単位
            }

            if (rflg  &&  rs.getString("CREDITS") != null) {
                sch_rcredits[ i ] += Integer.parseInt(rs.getString("CREDITS")); //履修単位
            }
        } catch (Exception ex) {
            log.error("accumuStdRCredits error! ", ex );
        }
    }



    private static class KNJD064K_GRADE extends KNJD064K_BASE {

        KNJD064K_GRADE(DB2UDB db2, Vrw32alp svf, final Param param) {
            super(db2, svf, param);
        }


        /**
          *  ページ見出し
          */
        void set_head2(){

            svf.VrsOut("TITLE",  "  成績一覧表（前年度）");    //タイトル

            svf.VrsOut("MARK",   "/"                     );              //表下部の文言の一部に使用

            fieldname = "GRADE_RECORD";

        }//set_headの括り


        /** 
         *  明細出力  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(ResultSet rs, Map<String, Integer> hmm,int subclassjisu,int replaceflg,int assesspattern)
        {
            boolean amikake = false; //欠課時数要注意または超過
            Integer int1 = null;     //出力行
            int linex = 0;           //出力列
            try {
                //学籍番号をKEYにMap.hmmより出力行を取得  列は既出力列数subclasslinecountに１を加算して取得
                int1 = hmm.get(rs.getString("SCHREGNO"));
                if (int1 == null )return;
                linex = subclasslinecount + 1;

                amikake = printSvfStdDetailKekka( rs, int1, linex, subclassjisu, replaceflg );

                accumuStdRCredits( rs, int1, subclassjisu, replaceflg );

                if (rs.getString("SCORE") != null) {
                    svf.VrsOutn( "rate" + int1, linex, rs.getString("SCORE")); //成績
                    subclasscnt_num( rs );    //05/03/09
                    if (! rs.getString("SCORE").equals("欠")  &&
                        ! rs.getString("SCORE").equals("公")  &&
                          rs.getInt("REPLACEMOTO") <= 0 &&
                          rs.getString("PATTERN_ASSESS") != null)
                        if (param._output4.equals("1")  &&  rs.getString("PATTERN_ASSESS").equals("1")) {
                            svf.VrsOutn( "late" + int1, linex, "*" + rs.getString("PATTERN_ASSESS")); //評定
                        } else {
                            svf.VrsOutn( "late" + int1, linex, rs.getString("PATTERN_ASSESS"));       //評定
                        }
                }
                //修得単位の加算処理 => 修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合表記 
                //修得単位の累積処理変更 
                if (! amikake  &&
                    replaceflg < 1  &&
                    rs.getString("PATTERN_ASSESS") != null         &&  
                    0 < rs.getString("PATTERN_ASSESS").length()    &&
                    ! rs.getString("PATTERN_ASSESS").equals("()")  &&
                    ! rs.getString("PATTERN_ASSESS").equals("1")   &&
                    rs.getString("CREDITS") != null
                )   sch_ccredits[ int1 ] += Integer.parseInt(rs.getString("CREDITS")); //修得単位の加算処理
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
        }//printSvfStdDetailOut()の括り


        /** 
         *  明細出力  <総合学習>  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailSogotekiOut(ResultSet rs, Map<String, Integer> hmm, int subclassjisu, int replaceflg, int assesspattern) {
            boolean amikake = false; //欠課時数要注意または超過
            Integer int1 = null;     //出力行
            int linex = 0;           //出力列
            try {
                //学籍番号をKEYにMap.hmmより出力行を取得  列は既出力列数subclasslinecountに１を加算して取得
                int1 = hmm.get( rs.getString("SCHREGNO"));
                if (int1 == null) {
                    return;
                }
                linex = subclasslinecount + 1;

                if (rs.getString("PATTERN_ASSESS") != null) {
                    svf.VrsOutn("late"+int1 , linex , rs.getString("PATTERN_ASSESS"));     //評定
                }

                //欠課時数の印刷を別メソッドとする( <= 網掛け処理を学期成績にも適用するため )
                amikake = printSvfStdDetailKekka( rs, int1, linex, subclassjisu, replaceflg );
                //05/01/31 履修単位の加算処理  履修単位：欠課が授業時数の１／３を超えない場合
                if (! amikake  &&  rs.getString("CREDITS") != null) {
                    sch_rcredits[ int1 ] += Integer.parseInt(rs.getString("CREDITS")); //履修単位の加算処理
                }
                sch_rcredits_hr[ int1 ] += subclassjisu; //履修単位の加算処理 NO106

                // 修得単位の加算処理   修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合
                if (! amikake  &&  rs.getString("PATTERN_ASSESS") != null  &&  rs.getString("CREDITS") != null) {
                    sch_ccredits[ int1 ] += Integer.parseInt(rs.getString("CREDITS")); //修得単位の加算処理
                }
            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }

        }//printSvfStdDetailSogotekiOut()の括り


        /**
         *  欠課時数の印刷: 欠課時数要注意者または超過者は'true'を返す
         *  2005/11/16 Build 欠課時数の網掛け処理を学期成績にも適用する(学年成績のみであったが)ため処理を独立
         *  引数について
         *      int i: 出力行 / int j: 出力列 / int subclassjisu: 科目別単位数
         */
        boolean printSvfStdDetailKekka( ResultSet rs, int i, int j, int subclassjisu, int replaceflg) {
            boolean amikake = false;
            try {
                //欠課出力/網掛け設定/履修単位の加算処理
                //if (rs.getString("ABSENT1") != null  &&  rs.getString("SCORE") != null){
                if (rs.getString("ABSENT1") != null) {
                    //NO102 if (replaceflg < 1) {    //--NO100により条件を追加
                        if (Integer.parseInt( param._lastyearGrade ) == 3) {
                            //３年生の欠課時数網掛け設定
                            if (rs.getInt("ABSENT1") >= subclassjisu * 8 + 1 )amikake = true;
                        }else{
                            //1・２年生の欠課時数網掛け設定
                            if (rs.getInt("ABSENT1") >= subclassjisu * 10 + 1 )amikake = true;
                        }
    //log.debug("subclassjisu="+subclassjisu + "   amikake="+amikake+ "  absent1="+rs.getInt("ABSENT1"));
                        if (amikake) {
                            svf.VrAttributen( "kekka" + i, j, "Paint=(2,70,1),Bold=1" );
                        }
                    //NO102 }
                    if (Integer.parseInt( rs.getString("ABSENT1")) != 0) {
                        svf.VrsOutn( "kekka" + i, j, rs.getString("ABSENT1")); //欠課
                    }
                }

                //網掛け解除
                if (amikake) {
                    svf.VrAttributen( "kekka" + i, j, "Paint=(0,0,0),Bold=0" );
                }

            } catch (Exception ex) {
                log.error("printSvfStdDetailKekka error! ", ex);
            }
            return amikake;
        }


        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの部品 成績データの再構築
         */
        String prestatStdSubclassRecord(int sdiv) {
            if (param._lastyearSemester.equals("1")) {
                return prestatStdSubclassRecord1( sdiv );    //１学期仕様
            } else if (param._lastyearSemester.equals("2")) {
                return prestatStdSubclassRecord2( sdiv );    //２学期仕様
            } else if (param._lastyearGrade.equals("03")) {
                return prestatStdSubclassRecord2( sdiv );    //２学期仕様
            } else {
                return prestatStdSubclassRecord3( sdiv);     //３学期仕様
            }
        }//prestatStdSubclassDetailの括り


        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの部品 成績データの再構築  １学期仕様
         */
        String prestatStdSubclassRecord1( int sdiv )
        {
            StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W3.SCHREGNO, ");
			stb.append(" W3.CLASSCD, ");
			stb.append(" W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append(                      " W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '欠' ");
            stb.append(             "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(             "ELSE NULL END AS SCORE ");

            stb.append(       ",CASE WHEN ");
            stb.append(                   " CLASSCD ");
            stb.append(                   "<> '90' THEN ");
            stb.append(                  "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                             "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                             "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");
            stb.append(                       "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                            "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                               "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                               "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
            stb.append(                       "ELSE NULL END ");
            stb.append(             "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS ");

            if (sdiv == 0) {
                stb.append(    ", ''  AS SCORE_FLG, ");
                stb.append(    "''  AS DICD ");
            }
            stb.append("FROM    KIN_RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param._lastyear + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");

            return stb.toString();

        }//prestatStdSubclassDetailの括り


        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの部品 成績データの再構築  ２学期仕様
         */
        String prestatStdSubclassRecord2( int sdiv )
        {
            StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W3.SCHREGNO, ");
			stb.append(" W3.CLASSCD, ");
			stb.append(" W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append(                      " W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '欠' ");
            stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '欠' ");
            stb.append(             "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(             "ELSE NULL END AS SCORE ");

            stb.append(       ",CASE WHEN ");
            stb.append(                   " CLASSCD ");
            stb.append(                   "<> '90' THEN ");
            stb.append(             "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                       "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");
            stb.append(                  "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                       "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                       "VALUE(SEM2_REC_FLG,'0') = '0' THEN '()' ");
            stb.append(                  "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                       "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
            stb.append(                  "ELSE NULL END ");
            stb.append(             "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS ");

            if (sdiv == 0) {
                stb.append(    ", ''  AS SCORE_FLG, ");
                stb.append(    "''  AS DICD ");
            }
            stb.append("FROM    KIN_RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param._lastyear + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");

            return stb.toString();

        }//prestatStdSubclassDetailの括り


        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの部品 成績データの再構築  ３学期仕様
         */
        String prestatStdSubclassRecord3( int sdiv )
        {
            StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W3.SCHREGNO, ");
			stb.append(" W3.CLASSCD, ");
			stb.append(" W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append(                      " W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "CASE WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append(       ",CASE WHEN ");
            stb.append(                   " CLASSCD ");
            stb.append(                   "<> '90' THEN ");
            stb.append(             "CASE WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                       "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
            stb.append(                  "ELSE NULL END ");
            stb.append(             "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS ");

            if (sdiv == 0) {
                stb.append(    ", ''  AS SCORE_FLG, ");
                stb.append(    "''  AS DICD ");
            }
            stb.append("FROM    KIN_RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param._lastyear + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");

            return stb.toString();

        }//prestatStdSubclassDetailの括り

    }
    
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        String _classSelected;
        final String _lastyearAttenddate;
        final String _output5;
        String _lastyear0401;
        String _lastyear0331;
        final String _output4;
        String _tmpAttendnoStart;
        String _tmpAttendnoEnd;
        String _testkindcd;
        String _lastyearSemestername;
        final String _semeFlg;
        String _lastyearSemester3sdate;
        String _newDateString;
        String _lastyearSemester9sdate;
        final String _lastyear;
        final String _lastyearSemester;
        final String _lastyearGrade;
        final String _lastyearLastSemester;
        final String _date;
        final String _printDate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR");                                    //年度
            _semester = ( !(request.getParameter("SEMESTER").equals("4"))) ? request.getParameter("SEMESTER") : "9"; //1-3:学期 9:学年末
            //_grade = request.getParameter("GRADE_HR_CLASS").substring(0,2);         //学年・組
            _grade = request.getParameter("GRADE");                                   //学年
            _classSelected = request.getParameter("CLASS_SELECTED");                          //学年・組
            _output4 = request.getParameter("OUTPUT4") != null ? "1" : "0";              //単位保留
            //_lastyearAttenddate = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE"));    //出欠集計日付

            _output5 = request.getParameter("OUTPUT5") != null ? "on" : null;                 //遅刻を欠課に換算 null:無

            if (request.getParameter("TESTKINDCD") != null && 2 <= request.getParameter("TESTKINDCD").length()) {
                _testkindcd = request.getParameter("TESTKINDCD").substring(0,2);          //テスト種別
            } else {
                _testkindcd = request.getParameter("TESTKINDCD");
            }

            _semeFlg = request.getParameter("SEME_FLG");          //LOG-IN時の学期（現在学期）

            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE"));  //印刷日

            _printDate = KNJ_EditDate.H_Format_Haifun( request.getParameter("PRINT_DATE"));  //印刷日
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            _lastyear = String.valueOf(Integer.parseInt(_year) - 1);  //前年度
            _lastyearSemester = "9";  //前年度学期
            _lastyearGrade = "0" + "" + String.valueOf(Integer.parseInt(_grade) - 1);  //前学年
            _lastyearLastSemester = "3";  //前年度最終学期
            _lastyearAttenddate = _year + "-03-31";  //出欠集計日 <= 前年度最終日
            _testkindcd = "0";  //学年成績を指定
        }
    }
}

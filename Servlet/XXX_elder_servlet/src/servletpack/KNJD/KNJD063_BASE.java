// kanji=漢字
/**
 * $Id: 9ea24c89dabb882a3a3d708f0e6399fcf0272d58 $
 *    学校教育システム 賢者 [成績管理] 成績一覧（大宮開成中学用）
 *
 *  2005/08/22 Build
 *  2005/10/05 yamashiro・編入のデータ仕様変更および在籍異動条件に転入学を追加
 *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO004
 *  2006/03/03 yamashiro・--NO004修正時の不具合を修正 DB2の型変換の使用方法に間違いがあった --NO007
 *                      ・SQLの不具合を修正 --NO007
 */

package servletpack.KNJD;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


class KNJD063_BASE{

    private static final Log log = LogFactory.getLog(KNJD063_BASE.class);

    Vrw32alp svf;                     //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                        //Databaseクラスを継承したクラス
    private StringBuffer stb;
    ResultSet rs;
    DecimalFormat df = new DecimalFormat("0.0");
    SimpleDateFormat sdf;
    KNJDefineSchool definecode;        //各学校における定数等設定
    KNJSchoolMst _knjSchoolMst;
    
    /** 教育課程コードを使用するか */
    private final String _useCurriculumcd;
    private final String _useVirus;
    private final String _useKoudome;

    String param[];
    String fieldname;
    String fieldname2;
    int subclasslinecount;          //科目列カウント
    int hr_attend[] = new int[9];                    //クラス平均出欠格納
    int hr_seitosu[] = new int[12];                    //クラス平均対象生徒数格納
    int sch_rcredits[] = new int[50];               //生徒ごとの履修単位数 05/01/31
    int sch_ccredits[] = new int[50];               //生徒ごとの修得単位数 05/01/31
    float totalavghr;
    float totalavggr;
    float averageavghr;
    float averageavggr;
    
    String sql1, sql2;

    /** 出欠状況取得引数 */
    private String _periodInState;
    private Map _attendSemesMap;
    private Map _hasuuMap;
    private boolean _semesFlg;
    private String _sDate;
    private final String SSEMESTER = "1";
    
    /**
     *  コンストラクター
     */
    KNJD063_BASE(DB2UDB db2, Vrw32alp svf, String param[]){
        this.db2 = db2;
        this.svf = svf;
        this.param = param;
        _useCurriculumcd = param[21];
        _useVirus = param[22];
        _useKoudome = param[23];
    }

    /**
     *  印刷処理
     */
    boolean printSvf(final String hrclass[]) {

        boolean nonedata = false;   //該当データなしフラグ
        set_head(); //見出し出力のメソッド

        // 印刷処理
        if (printSvfMain(hrclass)) {
            nonedata = true;    
        }

        return nonedata;
    }

    /**
     *  ページ見出し・初期設定
     */
    private void set_head()
    {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();        //各情報取得用のクラス
        KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
        if( definecode == null )setClasscode();            //教科コード・学校区分・学期制等定数取得
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param[0]);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }

        //学期名称、範囲の取得
        try {
            returnval = getinfo.Semester(db2,param[0],param[1]);
            param[12] = returnval.val1;                                    //学期名称
            param[6] = returnval.val2;                                    //学期期間FROM
            param[7] = returnval.val3;                                    //学期期間TO
        } catch( Exception ex ){
            log.warn("term1 svf-out error!",ex);
        } finally {
            if( param[6] == null ) param[6] = param[0] + "-04-01";
            if( param[7] == null ) param[7] = ( Integer.parseInt(param[0]) + 1 ) + "-03-31";
        }

        try {
            param[18] = KNJ_EditDate.getNowDateWa( true );        //作成日(現在処理日)
        } catch( Exception ex ){
            log.warn("ymd1 svf-out error!",ex);
        }

        setPeiodValue();    //対象校時および名称取得
        set_head2();        //ページ見出し
        
        // 出欠状況取得引数をロード
        loadAttendSemesArgument(db2);
//for( int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i]);
    }


    /**
     *  ページ見出し
     */
    void set_head2(){}


    /**
     * メイン印刷処理
     *   対象生徒(Sql=>prestatStdNameList)を読込み、５０名ごとに印刷(printSvfStdDetail)する
     */
    private boolean printSvfMain(final String hrclass[]) {
        boolean nonedata = false;
        Map hm1 = new HashMap(50);                //学籍番号と行番号の保管
        String lschname[] = new String[50];     //生徒名の保管 05/05/22
        Map hm2 = new HashMap();                //行番号(出席番号)と備考の保管 05/05/22
        PreparedStatement arrps[] = new PreparedStatement[5];

        try {
            arrps[0] = db2.prepareStatement( prestatStdNameList()       );        //学籍データ
            sql1 = prestatStdTotalRec();
            arrps[1] = db2.prepareStatement( sql1       );        //成績累計データ
            sql2 = prestatSubclassInfo();
            arrps[2] = db2.prepareStatement( sql2      );           //科目名等データ
            arrps[3] = db2.prepareStatement( prestatStdSubclassDetail() );        //明細データ
            String prestatStdTotalAttend = AttendAccumulate.getAttendSemesSql(
                    _semesFlg,
                    definecode,
                    _knjSchoolMst,
                    param[0],
                    SSEMESTER,
                    param[1],
                    (String) _hasuuMap.get("attendSemesInState"),
                    _periodInState,
                    (String) _hasuuMap.get("befDayFrom"),
                    (String) _hasuuMap.get("befDayTo"),
                    (String) _hasuuMap.get("aftDayFrom"),
                    (String) _hasuuMap.get("aftDayTo"),
                    param[2],
                    "?",
                    null,
                    "SEMESTER",
                    _useCurriculumcd,
                    _useVirus,
                    _useKoudome
            );
            arrps[4] = db2.prepareStatement( prestatStdTotalAttend );        //出欠累計データ
        } catch( Exception ex ) {
            log.error("[KNJD171K]boolean printSvfMain prepareStatement error! ", ex);
            return nonedata;
        }

        //組の回数分印刷処理を繰り返す
        for (int i = 0 ; i < hrclass.length ; i++) {
            log.debug("hrclass="+hrclass[i]);
            try {
                param[3] = hrclass[i];
                if (printSvfMainHrclass(hm1, hm2, lschname, arrps)) {
                    nonedata = true;
                }
                clearMapValue(hm1, hm2, lschname);
                clearValue();
            } catch( Exception ex ) {
                log.error("[KNJD062]boolean printSvfMainHrclass() error! " + ex);
            }
        }
        return nonedata;
    }

    /**
     *  マップ初期化
     *  2005/06/07
     */
    private void clearMapValue( Map hm1, Map hm2, String lschname[] )
    {
        try {
            if( hm1 != null )hm1.clear();                        //行番号情報を削除
            if( hm2 != null )hm2.clear();                        //備考情報を削除
            if( lschname != null )for( int j = 0 ; j < lschname.length ; j++ )lschname[j] = null;
            param[9] = null;
            param[10] = null;
        } catch( Exception ex ) {
            log.warn("clearMapValue error!",ex);
        }
    }

    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param[3]:学年・組
     */
    private boolean printSvfMainHrclass( Map hm1, Map hm2, String lschname[], PreparedStatement arrps[] )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet arrrs[] = new ResultSet[2];
        int schno = 0;                          //行番号
        int schno2 = 0;                          //行番号
        boolean bout = false;                      //05/06/07Build

        //学級単位の印刷処理
        try {
            int pp = 0;
            arrps[0].setString( ++pp,  param[3] );                      //学年・組
            arrrs[0] = arrps[0].executeQuery();                          //生徒名の表
            while( arrrs[0].next() ){
                //05/06/06Modify 行を詰めて出力する処理を追加
                if( param[19] != null ){
                    ++schno;                                            //05/06/06
                    if( 50 <= schno )bout = true;
                } else{
                    schno = ( arrrs[0].getInt("ATTENDNO") % 50 != 0 )? arrrs[0].getInt("ATTENDNO") % 50 : 50;
                    if( schno < schno2 )bout = true;
                    schno2 = schno;
                }
                //改ページ処理
                if( bout ){
                    if( printSvfStdDetail( hm1, hm2, lschname, arrps, false ) ) nonedata = true;            //成績・評定・欠課出力のメソッド
                    clearMapValue( hm1, hm2, lschname );
                    if( param[19] != null )schno = 1;
                    else                   schno = ( arrrs[0].getInt("ATTENDNO") % 50 != 0 )? arrrs[0].getInt("ATTENDNO") % 50 : 50;
                    param[9] = null;                                    //開始生徒
                    param[10] = null;                                    //終了生徒
                    bout = false;
                    clearValue2();
                }
                saveSchInfo( arrrs[0], schno, hm1, lschname, hm2 );     //生徒情報の保存処理 2005/05/22
            }
        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            db2.commit();
        }

        //成績・評定・欠課の印刷処理
        if( 0 < hm1.size() ){
            if( printSvfStdDetail( hm1, hm2, lschname, arrps, true ) )nonedata = true;     //成績・評定・欠課出力のメソッド OKならインスタンス変数nonedataをtrueとする
            clearMapValue( hm1, hm2, lschname );
            clearValue2();
        }
        try {
            for( int i = 0 ; i < arrrs.length ; i++ ) if( arrrs[i] != null ) arrrs[i].close();
        } catch( SQLException ex ) { log.error("[KNJD062]printSvfMainHrclass rs.close() error! "+ex); }

        return nonedata;
    }


    /**
     *  生徒情報の保存処理
     *    2005/05/22
     *    2005/06/07 Modify String lschname[]の前３桁に出席番号を挿入
     */
    void saveSchInfo( ResultSet rs, int schno, Map hm1, String lschname[], Map hm2 )
    {
        try {
            hm1.put( rs.getString("SCHREGNO"), new Integer(schno) );    //行番号に学籍番号を付ける
            if( param[9] == null ) param[9] = rs.getString("ATTENDNO");    //開始生徒
            param[10] = rs.getString("ATTENDNO");                        //終了生徒
            String str = "000" + rs.getString("ATTENDNO");
            lschname[ schno-1 ] = str.substring( str.length() - 3 , str.length() ) + rs.getString("NAME");  //05/06/07

            //    文言をセットする。（除籍日付＋'付'＋除籍区分名称）04/11/08Add
            if( rs.getString("KBN_DATE1") != null ){
                if( stb == null )stb = new StringBuffer();
                else             stb.delete(0, stb.length() );
                stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE1")) );
                if( rs.getString("KBN_NAME1") != null ) stb.append( rs.getString("KBN_NAME1") );
                hm2.put( new Integer(schno), stb.toString() );    //備考に行番号を付ける
            } else if( rs.getString("KBN_DATE2") != null ){
                if( stb == null )stb = new StringBuffer();
                else             stb.delete(0, stb.length() );
                stb.append( KNJ_EditDate.h_format_JP(rs.getString("KBN_DATE2")) );
                if( rs.getString("KBN_NAME2") != null ) stb.append( rs.getString("KBN_NAME2") );
                hm2.put( new Integer(schno), stb.toString() );    //備考に行番号を付ける
            }
        } catch( Exception ex ){
            log.warn("name1 svf-out error!",ex);
        }

    }


    /**
     *  生徒名等出力 
     *    2005/05/22 Modify
     *    2005/06/07 Modify 
     */
    void printSvfStdNameOut( Map hm2, String lschname[] ){

        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            for( int i = 0 ; i < lschname.length ; i++ ){
                if( lschname[i] == null )continue;
                ret = svf.VrsOut("name" + (i+1) ,   lschname[i].substring( 3 ) );
                if( param[19] != null )
                    ret = svf.VrsOutn("NUMBER", (i+1) , String.valueOf( Integer.parseInt( lschname[i].substring( 0, 3 ) ) ) );
                else
                    ret = svf.VrsOutn("NUMBER", (i+1) , String.valueOf( Integer.parseInt( lschname[i].substring( 0, 3 ) ) ) );
                if( hm2.containsKey( new Integer( i+1 ) ) )
                    ret = svf.VrsOut("REMARK" + (i+1) , (String)hm2.get( new Integer( i+1 ) ) );
            }
        } catch( Exception ex ){
            log.warn("name1 svf-out error!",ex);
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
    boolean printSvfStdDetail( Map hmm, Map hm2, String lschname[], PreparedStatement arrps[], 
                               boolean blast )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;   //履修単位数 05/01/31
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_ccredits[i] = 0;   //修得単位数 05/01/31
        int subclass1 = 0;                //科目コードの保存
        int subclass2 = 0;                //科目コードの保存
//log.debug("lschname="+lschname[0]);
        clearSvfField( hm2, lschname );                            //生徒名等出力のメソッド
for( int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i]);

        try {
            int p = 0;
            arrps[3].setString( ++p, param[3].substring( 2 ) );        //組
            arrps[3].setString( ++p, param[3].substring( 2 ) );        //組
            arrps[3].setString( ++p, param[9]  );                    //生徒番号
            arrps[3].setString( ++p, param[10] );                    //生徒番号
log.debug("prestatStdSubclassDetail executeQuery() start");
            rs2 = arrps[3].executeQuery();                    //明細データのRecordSet
log.debug("prestatStdSubclassDetail executeQuery() end");
        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        }

        try {
            int p = 0;
            arrps[2].setString( ++p, param[3] );                //学年・組
            if( param[11].equals("0") )arrps[2].setString( ++p, param[3].substring( 2 ) );    //組
            arrps[2].setString( ++p, param[15] );                //出欠データ集計開始日付 2005/01/04
            arrps[2].setString( ++p, param[4]  );                //出欠データ集計終了日付 2005/01/04
            arrps[2].setString( ++p, param[3].substring( 2 ) );    //組
            arrps[2].setString( ++p, KNJC053_BASE.retSemesterMonthValue( param[16] ) );        //出欠集計データ終了学期＋月  NO004 Modify
            //NO004 arrps[2].setString( ++p, param[16] );                //出欠集計データ終了学期＋月   2005/01/04
            arrps[2].setString( ++p, param[3].substring( 2 ) );    //組
            arrps[2].setString( ++p, param[3] );                //学年・組
            arrps[2].setString( ++p, param[3].substring( 2 ) );    //組
            arrps[2].setString( ++p, param[3].substring( 2 ) );    //組
log.debug("prestatSubclassInfo executeQuery() start");
            rs1 = arrps[2].executeQuery();                                    //科目名等のRecordSet
log.debug("prestatSubclassInfo executeQuery() end");
        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        }

        boolean rs1closed = false;
        try {
            int rs1subclasscd = 0;
            while ( rs2.next() ){
                //生徒成績レコードの科目コードのブレイク
                final int rs2subclasscd;
                if ("1".equals(_useCurriculumcd) && !"999999".equals(rs2.getString("SUBCLASSCD")) && !"999998".equals(rs2.getString("SUBCLASSCD"))) {
                    rs2subclasscd = Integer.parseInt(StringUtils.split(rs2.getString("SUBCLASSCD"), "-")[3]);
                } else {
                    rs2subclasscd = rs2.getInt("SUBCLASSCD");
                }
                if( subclass2 != rs2subclasscd ){
                    if( subclass2 != 0 ){
                        ret = svf.VrEndRecord();
                        nonedata = true;
                        subclasslinecount++;
                    }
                    subclass2 = rs2subclasscd;            //科目コードの保存
                    //科目名をセット
                    for( int i = 0 ; subclass1 < subclass2  &&  i < 30 ; i++ ){
                        //if( 19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                        if( !rs1closed && rs1.next() ){
                            if ("1".equals(_useCurriculumcd) && !"999999".equals(rs1.getString("SUBCLASSCD")) && !"999998".equals(rs1.getString("SUBCLASSCD"))) {
                                rs1subclasscd = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                            } else {
                                rs1subclasscd = rs1.getInt("SUBCLASSCD");
                            }
                            if( rs1subclasscd == 999999 || rs1subclasscd == 999998 ){
                                saveTotalAverage( rs1 );  //総合点および全科目平均（組／学年）を保管
                                continue;
                            }else{
                                if( 19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                                subclass1 = rs1subclasscd;
                                printSvfSubclassInfoOut(rs1);        //科目名等出力のメソッド
                                if( subclass1<subclass2 ){            //データ出力用にない科目列を出力する
                                    ret = svf.VrEndRecord(); 
                                    nonedata = true; 
                                    subclasslinecount++;
                                }
                                if( subclass1==subclass2 )break;        //科目名出力用とデータ出力用の科目コードが一致なら抜ける
                            }
                        }else {
                            rs1closed = true;
                            if( 19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                        }
                    }
                }
                printSvfStdDetailOut( rs2, hmm );    //明細印刷
            }
            //科目名をセット
            for( int i = 0 ; !rs1closed && rs1.next()  &&  i < 20 ; i++ ){
                //if( 19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                if( rs1subclasscd == 999999 || rs1subclasscd == 999998 ){
                    saveTotalAverage( rs1 );  //総合点および全科目平均（組／学年）を保管
                    continue;
                } else{
                    if( i == 0 ){
                        ret = svf.VrEndRecord(); 
                        subclasslinecount++;
                    }
                    if( 19 <= subclasslinecount ) subclasslinecount = clearSvfField( hm2, lschname );
                    printSvfSubclassInfoOut(rs1);        //科目名等出力のメソッド
                    ret = svf.VrEndRecord(); 
                    subclasslinecount++;
                    nonedata = true; 
                }
            }

        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            try {
                db2.commit();
                if( rs1 != null )rs1.close();
                if( rs2 != null )rs2.close();
            } catch( SQLException ex ){
                log.warn("ResultSet-close error!",ex);
            }
        }


        //総合欄の印刷処理
        if( subclass2 != 0 ){
            printSvfStdTotalRec( hmm, arrps );
            printSvfStdTotalAttend( hmm, arrps );
            if( blast )printSvfTotalOut( blast );
            ret = svf.VrEndRecord();
            nonedata = true;
            subclasslinecount++;
        }

        return nonedata;

    }//boolean printSvfStdDetail()の括り


    /**
     *  教科名・科目名・単位数・授業時数・平均（学級／学年）印刷
     */
    void printSvfSubclassInfoOut( ResultSet rs )
    {
        try {
            boolean amikake = false;
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }

            ret = svf.VrsOut( "course1",  rs.getString("CLASSABBV") );                //教科名

            if( rs.getString("ELECTDIV") != null  &&  ! rs.getString("ELECTDIV").equals("0") )amikake = true;
            if( amikake ) ret = svf.VrAttributen("subject1",  subclasslinecount+1, "Paint=(2,70,2),Bold=1" );    //網掛
             ret = svf.VrsOutn( "subject1",  subclasslinecount+1, rs.getString("SUBCLASSNAME") );  //科目名

            if( rs.getString("CREDITS") != null )ret = svf.VrsOutn( "credit1",   subclasslinecount + 1,  rs.getString("CREDITS") );    //単位数

            if( rs.getString("JISU") != null  &&  0 < Integer.parseInt( rs.getString("JISU") ) )
                ret = svf.VrsOutn( "lesson1",  subclasslinecount + 1, rs.getString("JISU") );  //授業時数

            if( amikake )ret = svf.VrAttributen( "subject1",  subclasslinecount + 1, "Paint=(0,0,0),Bold=0" );    //網掛解除

            if( rs.getString("AVG_HR") != null )ret = svf.VrsOutn( "AVE_CLASS",        subclasslinecount + 1,  String.valueOf( df.format( Float.parseFloat( rs.getString("AVG_HR")))) );    //学級平均
            if( rs.getString("AVG_GR") != null )ret = svf.VrsOutn( "AVE_GRADE",        subclasslinecount + 1,  String.valueOf( df.format( Float.parseFloat( rs.getString("AVG_GR")))) );    //学年平均
            if( rs.getString("SUM_HR") != null )ret = svf.VrsOutn( "TOTAL_SUBCLASS",   subclasslinecount + 1,  rs.getString("SUM_HR") );    //合計


        } catch( Exception ex ){
            log.warn("course1... svf-out error!",ex);
        }
    }


    /** 
     *  明細印刷
     *  生徒の科目別素点（評価・評定）・組順位・学年順位を出力する
     *  2005/08/22
     */
    void printSvfStdDetailOut( ResultSet rs, Map hmm ){}


    /**
     *  累計出力処理 
     */
    boolean printSvfStdTotalRec(Map hmm, PreparedStatement arrps[] )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;
//for(int ia=0 ; ia<param.length ; ia++)log.trace("param["+ia+"]=" + param[ia]);

        try {
            int pp = 0;
            arrps[1].setString( ++pp,  param[3].substring( 2 ) );                      //組
            arrps[1].setString( ++pp,  param[3].substring( 2 ) );                      //組
            arrps[1].setString( ++pp,  param[3].substring( 2 ) );                      //組
            arrps[1].setString( ++pp,  param[3].substring( 2 ) );                      //組
            arrps[1].setString( ++pp,  param[3].substring( 2 ) );                      //組
log.debug("prestatStdTotalRec executeQuery() start");
            rs = arrps[1].executeQuery();                                //成績累計データのRecordSet
log.debug("prestatStdTotalRec executeQuery() end");

            while ( rs.next() ){
                //明細データの出力
                printSvfStdTotalRecOut(rs,hmm);
            }
        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            try {
                db2.commit();
                if( rs!=null )rs.close();
                //if( ps1 != null )ps1.close();
            } catch( SQLException ex ){
                log.warn("ResultSet-close error!",ex);
            }
        }

        return nonedata;

    }//printSvfStdTotalRec()の括り



    /**
     *  累計出力処理 
     *      2005/01/04 SQL変更に伴い修正
     */
    boolean printSvfStdTotalAttend(Map hmm, PreparedStatement arrps[] )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;

        try {
            int p = 0;
            arrps[4].setString( ++p, param[3].substring(2, 5) );        //組
log.trace("prestatStdTotalAttend executeQuery() start ");
            rs = arrps[4].executeQuery();                                //出欠累計データのRecordSet
log.trace("prestatStdTotalAttend executeQuery() end ");

            while ( rs.next() ){
                if (!param[1].equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                //明細データの出力
                printSvfStdTotalAttendOut(rs,hmm);
            }
        } catch( Exception ex ) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            try {
                db2.commit();
                if( rs!=null )rs.close();
                //if( ps1 != null )ps1.close();
            } catch( SQLException ex ){
                log.warn("ResultSet-close error!",ex);
            }
        }

        return nonedata;

    }//printSvfStdTotalAttend()の括り


    /** 
     *  総合欄生徒別明細印刷
     */
    void printSvfStdTotalRecOut( ResultSet rs, Map hmm )
    {
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            //学籍番号（生徒）に対応した行にデータをセットする。
            Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
            if( int1==null )return;

            if( rs.getString("SUM_REC") != null )ret = svf.VrsOut( "TOTAL"      + int1.intValue(), rs.getString("SUM_REC") );     //総合点
            if( rs.getString("AVG_REC") != null )ret = svf.VrsOut( "AVERAGE"    + int1.intValue(), String.valueOf( df.format( Float.parseFloat( rs.getString("AVG_REC")))) );  //平均点
            if( rs.getString("HR_RANK") != null )ret = svf.VrsOut( "CLASS_RANK" + int1.intValue(), rs.getString("HR_RANK") );    //組順位
            if( rs.getString("GR_RANK") != null )ret = svf.VrsOut( "GRADE_RANK" + int1.intValue(), rs.getString("GR_RANK") );    //学年順位

            if( rs.getString("CREDITS")  != null  &&
                0 < Integer.parseInt( rs.getString("CREDITS") ) )ret = svf.VrsOut( "R_CREDIT" + int1.intValue(), rs.getString("CREDITS") );     //履修単位
            if( rs.getString("CREDITS2") != null  &&
                0 < Integer.parseInt( rs.getString("CREDITS2") ))ret = svf.VrsOut( "C_CREDIT" + int1.intValue(), rs.getString("CREDITS2") ); //修得単位

        } catch( Exception ex ){
            log.warn("total svf-out error!",ex);
        }

    }//printSvfStdTotalRecOut()の括り



    /** 
     *  出欠の記録 明細出力 
     *      2005/01/04 SQL変更に伴い修正
     *      2005/02/01 出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。
     */
    void printSvfStdTotalAttendOut(ResultSet rs,Map hmm)
    {
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            //int intx = 0;               //出席日数のカウント
            //学籍番号（生徒）に対応した行にデータをセットする。
            Integer int1 = (Integer)hmm.get( rs.getString("SCHREGNO") );
            if( int1 == null )return;

            if (rs.getString("SUSPEND") != null || "true".equals(_useVirus) && rs.getString("VIRUS") != null || "true".equals(_useKoudome) && rs.getString("KOUDOME") != null) {
                int value = 0;
                boolean hasvalue = false;
                if( rs.getString("SUSPEND") != null ){
                    if( rs.getInt("SUSPEND") != 0 ){
                        hasvalue = true;
                        value += rs.getInt("SUSPEND");
                    }
                }
                if( "true".equals(_useVirus) && rs.getString("VIRUS") != null ){
                    if( rs.getInt("VIRUS") != 0 ){
                        hasvalue = true;
                        value += rs.getInt("VIRUS");
                    }
                }
                if( "true".equals(_useKoudome) && rs.getString("KOUDOME") != null ){
                    if( rs.getInt("KOUDOME") != 0 ){
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

            if( rs.getString("MOURNING") != null ){
                if( rs.getInt("MOURNING") != 0 ){
                    ret = svf.VrsOut( "KIBIKI" + int1.intValue(), rs.getString("MOURNING") );    //忌引
                    hr_attend[1] += rs.getInt("MOURNING");        //合計 05/02/01Modify
                }
                hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("MLESSON") != null ){
                if( rs.getInt("MLESSON") != 0 ){
                    ret = svf.VrsOut( "PRESENT" + int1.intValue(), rs.getString("MLESSON") );    //出席すべき日数
                    hr_attend[3] += rs.getInt("MLESSON");         //合計 05/02/01Modify
                }
                hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("SICK") != null ){
                if( rs.getInt("SICK") != 0 ){
                    ret = svf.VrsOut( "ABSENCE" + int1.intValue(), rs.getString("SICK") );    //欠席日数
                    hr_attend[4] += rs.getInt("SICK");          //合計 05/02/01Modify
                }
                hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("PRESENT") != null ){
                if( rs.getInt("PRESENT") != 0 ){
                    ret = svf.VrsOut( "ATTEND" + int1.intValue(), rs.getString("PRESENT") );    //出席日数
                    hr_attend[5] += rs.getInt("PRESENT");         //合計 05/02/01Modify
                }
                hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("EARLY") != null ){
                if( rs.getInt("EARLY") != 0 ){
                    ret = svf.VrsOut( "LEAVE" + int1.intValue(), rs.getString("EARLY") );        //早退回数
                    hr_attend[6] += rs.getInt("EARLY");           //合計 05/02/01Modify
                }
                hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("LATE") != null ){
                if( rs.getInt("LATE") != 0 ){
                    ret = svf.VrsOut( "TOTAL_LATE" + int1.intValue(), rs.getString("LATE") );    //遅刻回数
                    hr_attend[7] += rs.getInt("LATE");            //合計 05/02/01Modify
                }
                hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
            }

            if( rs.getString("TRANSFER_DATE") != null ){  // 05/02/02ココへ移動
                if( rs.getInt("TRANSFER_DATE") != 0 ){
                    ret = svf.VrsOut("ABROAD" + int1.intValue(), rs.getString("TRANSFER_DATE"));    //留学日数
                    hr_attend[2] += rs.getInt("TRANSFER_DATE");
                }
                hr_seitosu[2]++;
            }
        } catch( Exception ex ){
            log.warn("total svf-out error!",ex);
        }
    }


    /** 
     *  総合点および全科目平均（組／学年）を保管
     */
    private void saveTotalAverage( ResultSet rs )
    {
        try {
            if( rs.getInt("SUBCLASSCD") == 999999 ){
                if( rs.getString("AVG_HR") != null )averageavghr = rs.getFloat("AVG_HR");   //組の全科目平均
                if( rs.getString("AVG_GR") != null )averageavggr = rs.getFloat("AVG_GR");   //学年の全科目平均
            } else{
                if( rs.getString("AVG_HR") != null )totalavghr = rs.getFloat("AVG_HR");     //組の総合点平均
                if( rs.getString("AVG_GR") != null )totalavggr = rs.getFloat("AVG_GR");     //学年の総合点平均
            }
        } catch( Exception ex ){
            log.error( "error! " + ex );
        }
    }


    /** 
     *  総合欄の組および学年（総合計／平均）を印刷
     */
    void printSvfTotalOut( boolean blast )
    {
log.debug("totalavghr="+totalavghr);
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrsOut( "TOTAL51",     String.valueOf( (int)Math.round( totalavghr   )) );    //組の総合点平均
            ret = svf.VrsOut( "TOTAL52",     String.valueOf( (int)Math.round( totalavggr   )) );    //学年の総合点平均
            ret = svf.VrsOut( "AVERAGE51",   String.valueOf( (int)Math.round( averageavghr )) );    //組の全科目平均
            ret = svf.VrsOut( "AVERAGE52",   String.valueOf( (int)Math.round( averageavggr )) );    //学年の全科目平均

            if( 0 < hr_seitosu[0] ) ret = svf.VrsOut( "SUSPEND53", String.valueOf( hr_attend[0] ) );    //出停
            if( 0 < hr_seitosu[1] ) ret = svf.VrsOut( "KIBIKI53",  String.valueOf( hr_attend[1] ) );    //忌引
            if( 0 < hr_seitosu[3] ) ret = svf.VrsOut( "PRESENT53", String.valueOf( hr_attend[3] ) );    //出席すべき日数
            if( 0 < hr_seitosu[4] ) ret = svf.VrsOut( "ABSENCE53", String.valueOf( hr_attend[4] ) );    //欠席日数
            if( 0 < hr_seitosu[5] ) ret = svf.VrsOut( "ATTEND53",  String.valueOf( hr_attend[5] ) );    //出席日数
            if( 0 < hr_seitosu[6] ) ret = svf.VrsOut( "LEAVE53",   String.valueOf( hr_attend[6] ) );    //早退回数
            if( 0 < hr_seitosu[7] ) ret = svf.VrsOut( "TOTAL_LATE53",    String.valueOf( hr_attend[7] ) );    //遅刻回数

            if( 0 < hr_attend[3] ){
                ret = svf.VrsOut( "PER_ATTEND",  String.valueOf( (float)Math.round( ((float)hr_attend[5] / (float)hr_attend[3]) * 1000 ) / 10 ) );    //出席率
                ret = svf.VrsOut( "PER_ABSENCE", String.valueOf( (float)Math.round( ((float)hr_attend[4] / (float)hr_attend[3]) * 1000 ) / 10 ) );    //欠席率
            }

        } catch( Exception ex ){
            log.warn("group-average svf-out error!",ex);
        }
    }


    /** 
     * SQLStatement作成 ＨＲクラス生徒名の表(生徒名) 04/11/08Modify 
     *   SEMESTER_MATはparam[1]で検索 => 学年末'9'有り
     *   SCHREG_REGD_DATはparam[13]で検索 => 学年末はSCHREG_REGD_HDATの最大学期
     */
    String prestatStdNameList() {

        if( stb == null )stb = new StringBuffer();
        else             stb.delete(0, stb.length() );
        try {

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W3.NAME,");
            //stb.append(        "W4.GRD_DATE AS KBN_DATE1,");
            //stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) AS KBN_NAME1,");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");   //05/10/05Moidfy
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");  //05/10/05Moidfy
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");             //05/10/05Moidfy
            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = '" + param[1] + "' AND W1.YEAR = W2.YEAR ");
            stb.append("INNER   JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            //stb.append(                               "AND W4.GRD_DIV IN ('2','3') AND W4.GRD_DATE < EDATE ");
            stb.append(                               "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");    //05/10/05Moidfy
            stb.append(                                 "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");   //05/10/05Moidfy
            stb.append("LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            //stb.append(                                   "AND ( (W5.TRANSFERCD IN ('1','2') AND W5.TRANSFER_SDATE <= W2.EDATE AND W2.EDATE <= W5.TRANSFER_EDATE ) ");
            //stb.append(                                      "OR (W5.TRANSFERCD IN ('4') AND W2.EDATE < W5.TRANSFER_SDATE) ) ");
            stb.append(                                   "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");  //05/10/05Moidfy
            stb.append("WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");
            stb.append(       "AND W1.SEMESTER = '" + param[13] + "' ");
            stb.append("ORDER BY W1.ATTENDNO");


        } catch( Exception ex ){
            log.warn("sql-statement error!",ex);
        }
        return stb.toString();

    }//prestatStdNameListの括り


    /** 
     *  SQLStatement作成 ＨＲ履修科目の表(教科名・科目名・単位・授業時数・平均) 
     *  2005/08/22
     */
    String prestatSubclassInfo()
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete(0, stb.length() );
        try {
            //学籍の表
            stb.append("WITH SCHNO AS(");
            stb.append(        "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
            stb.append(                "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
            stb.append(        "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param[0] + "' ");  //05/10/05Build
            stb.append(        "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(            "AND W1.SEMESTER = '" + param[13] + "' ");
            stb.append(         "AND W1.GRADE = '" + param[2] + "' ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END) ");     //05/10/05Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END)) ) ");  //05/10/05Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/10/05Modify
            /* ***
            stb.append(        "AND  NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                         "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                             "AND S1.GRD_DIV IN ('2','3') ");
            stb.append(                             "AND S1.GRD_DATE < '" + param[4] + "' ) ");

            stb.append(        "AND  NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                         "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                             "AND ((S1.TRANSFERCD IN ('1','2') AND '" + param[4] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                               "OR (S1.TRANSFERCD IN ('4') AND '" + param[4] + "' < S1.TRANSFER_SDATE)) ) ");
            *** */
            stb.append(     "),");

            //講座の表
            stb.append("CHAIR_A AS(");
            stb.append(     "SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " K2.CLASSCD || '-' || K2.SCHOOL_KIND AS CLASSCD_SCHK, ");
                stb.append(            " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "K2.SUBCLASSCD AS SUBCLASSCD,MAX(K2.GROUPCD)AS GROUPCD ");
            stb.append(     "FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(     "WHERE   K1.YEAR = K2.YEAR ");
            stb.append(         "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(         "AND (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
            stb.append(         "AND K1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND K2.YEAR = '" + param[0] + "' ");
            stb.append(         "AND K1.TRGTGRADE||K1.TRGTCLASS = ? ");
            stb.append(         "AND K1.GROUPCD = K2.GROUPCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(         "AND (CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
                stb.append(             "OR  CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
            } else {
                stb.append(         "AND (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
                stb.append(             "OR  SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            }
            if( ! param[1].equals("9") ){
                stb.append(     "AND K1.SEMESTER = '" + param[1] + "' ");
                stb.append(     "AND K2.SEMESTER = '" + param[1] + "' ");
            }
            stb.append(     "GROUP BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " K2.CLASSCD || '-' || K2.SCHOOL_KIND , ");
                stb.append(            " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(          "K2.SUBCLASSCD ");
            stb.append(     ")");

            //評価読替え科目を抽出
            if( param[11].equals("0") ){
                stb.append(",REPLACE_REC AS(");
                stb.append(     "SELECT  ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(            " W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND AS CLASSCD_SCHK, ");
                    stb.append(            " W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(          " W2.GRADING_SUBCLASSCD AS SUBCLASSCD ");
                stb.append(     "FROM    RECORD_DAT W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' ");  //NO007Modify
                stb.append(         "AND ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(            " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
                }
                stb.append(              "W1.SUBCLASSCD=");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(            " W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(              "W2.ATTEND_SUBCLASSCD ");
                stb.append(         "AND EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO and w3.hr_class = ? ) ");
                stb.append(         "AND W2.YEAR ='" + param[0] + "' AND ANNUAL = '" + param[2] + "' AND REPLACECD = '1' ");
                stb.append(     "GROUP BY ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(            " W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND , ");
                    stb.append(            " W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(               "GRADING_SUBCLASSCD ");
                stb.append(     ") ");
            }

            //総合点および全科目平均（組／学年）用科目コード作成
            if ("1".equals(_useCurriculumcd)) {
                stb.append(", SUBCLASS_OTHER (CLASSCD_SCHK, SUBCLASSCD, GROUPCD) AS(");
                stb.append(     "VALUES('99', '999998','0000'),('99', '999999','0000') ");
                stb.append( ") ");
            } else {
                stb.append(", SUBCLASS_OTHER (SUBCLASSCD, GROUPCD) AS(");
                stb.append(     "VALUES('999998','0000'),('999999','0000') ");
                stb.append( ") ");
            }

            //メイン表
            stb.append("SELECT W2.SUBCLASSCD,W2.GROUPCD,W4.SUBCLASSABBV AS SUBCLASSNAME,W4.ELECTDIV,W5.CLASSABBV,");
            stb.append(       "W7.CREDITS,W7.ABSENCE_HIGH,");
            stb.append(       "W9.AVG_HR, W9.SUM_HR, W11.AVG_GR, ");
            stb.append(       "VALUE(W6.JISU,0) + VALUE(W10.LESSON,0) AS JISU ");

            stb.append("FROM  (SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           " CLASSCD_SCHK, ");
            }
            stb.append(               "SUBCLASSCD, GROUPCD FROM CHAIR_A ");
            if( param[11].equals("0") ){
                              //評価読替え科目を追加
                stb.append(   "UNION ALL ");
                stb.append(   "SELECT ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(           " CLASSCD_SCHK, ");
                }
                stb.append(           " SUBCLASSCD, '0000' AS GROUPCD FROM REPLACE_REC ");
            }
                              //総合点および全科目平均（組／学年）用科目コード作成
            stb.append(          "UNION ALL ");
            stb.append(       "SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           " CLASSCD_SCHK, ");
            }
            stb.append(               " SUBCLASSCD, GROUPCD FROM SUBCLASS_OTHER ");
            stb.append(    ") W2 ");

            stb.append("LEFT JOIN SUBCLASS_MST W4 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || ");
            }
            stb.append(                              " W4.SUBCLASSCD = W2.SUBCLASSCD ");
            stb.append("LEFT JOIN CLASS_MST W5 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " CASE WHEN W2.CLASSCD_SCHK = '99' THEN W5.CLASSCD ");
                stb.append(                  " ELSE W5.CLASSCD || '-' || W5.SCHOOL_KIND ");
                stb.append(            " END = W2.CLASSCD_SCHK ");
            } else {
                stb.append(                              " W5.CLASSCD = SUBSTR(W2.SUBCLASSCD,1,2)");
            }

            //授業時数(集計漏れ)の表 05/08/17Modify 生徒毎の時数のMAX値をとる
            stb.append(       "LEFT JOIN( ");
            stb.append(       "SELECT  SUBCLASSCD, MAX(JISU) AS JISU ");
            stb.append(       "FROM(   SELECT  T2.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                     "SUBCLASSCD AS SUBCLASSCD,COUNT(*) AS JISU ");
            stb.append(               "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2,CHAIR_DAT T3 ");
            stb.append(               "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN ? AND ? ");
            stb.append(                   "AND T1.YEAR = T2.YEAR ");
            stb.append(                   "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                   "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                   "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO where hr_class = ?) ");
            stb.append(                   "AND T3.YEAR='" + param[0] + "' ");
            stb.append(                   "AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(                   "AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
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
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(               "GROUP BY T2.SCHREGNO,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                     "SUBCLASSCD ");
            stb.append(           ")T1 ");
            stb.append(       "GROUP BY SUBCLASSCD ");
            stb.append(    ")W6 ON W6.SUBCLASSCD = W2.SUBCLASSCD ");

            //授業時数(集計済)の表 05/08/17Modify 生徒毎の時数のMAX値をとる => 元は月毎のMAX値を集計
            stb.append(    "LEFT JOIN(");
            stb.append(        "SELECT ");
            stb.append(                "SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                "MAX(LESSON) AS LESSON ");
            stb.append(           "FROM(");
            stb.append(             "SELECT  schregno, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                     "SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                     "SUM(LESSON) AS LESSON ");
            stb.append(                "FROM    ATTEND_SUBCLASS_DAT W1 ");
            stb.append(             "WHERE   YEAR = '" + param[0] + "' AND ");
            if( ! param[1].equals("9") )
                stb.append(                 "SEMESTER = '" + param[1] + "' AND ");
            stb.append(                     "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= ? AND ");   //--NO004 NO007
            //NO004 stb.append(                     "SEMESTER||MONTH <= ? AND ");
            stb.append(                     "EXISTS(");
            stb.append(                            "SELECT  'X' ");
            stb.append(                            "FROM    SCHNO W2 ");
            stb.append(                            "WHERE   W1.SCHREGNO = W2.SCHREGNO and w2.hr_class = ?)");
            stb.append(             "GROUP BY schregno, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                 "SUBCLASSCD ");
            stb.append(             ")S1 ");
            stb.append(        "GROUP BY SUBCLASSCD ");
            stb.append(    ")W10 ON W10.SUBCLASSCD = W2.SUBCLASSCD ");

            //単位の表
            stb.append(       "LEFT JOIN( ");
            stb.append(       "SELECT  ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "SUBCLASSCD AS SUBCLASSCD,CREDITS,ABSENCE_HIGH ");
            stb.append(       "FROM    CREDIT_MST W1 ");
            stb.append(       "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(           "AND W1.GRADE = '" + param[2] + "' ");
            stb.append(           "AND (W1.COURSECD, W1.MAJORCD, W1.COURSECODE) ");
            stb.append(                      "IN(SELECT  COURSECD, MAJORCD, COURSECODE ");
            stb.append(                      "FROM    SCHREG_REGD_DAT ");
            stb.append(                         "WHERE   YEAR = '" + param[0] + "' ");
            stb.append(                          "AND GRADE||HR_CLASS = ? ");
            stb.append(                             "AND SEMESTER = '" + param[13] + "')");
            stb.append(       ")W7 ON W7.SUBCLASSCD = W2.SUBCLASSCD ");

            //クラスの科目別合計点および平均点の表
            stb.append("LEFT JOIN( ");
            stb.append(   "SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           "VALUE(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD,'999999') AS SUBCLASSCD,");
            } else {
                stb.append(           "VALUE(SUBCLASSCD,'999999') AS SUBCLASSCD,");
            }
            stb.append(           "ROUND(AVG(FLOAT(" + fieldname + "))*10,0)/10 AS AVG_HR,");
            stb.append(           "SUM(" + fieldname + ")AS SUM_HR ");
            stb.append(   "FROM    RECORD_DAT W1 ");
            stb.append(   "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO and hr_class = ? ");
            stb.append(   "WHERE  W1.YEAR = '" + param[0] + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(      "AND W1.SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(      "AND " + fieldname + " IS NOT NULL ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(   "GROUP BY GROUPING SETS(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD,()) ");
            } else {
                stb.append(   "GROUP BY GROUPING SETS(SUBCLASSCD,())");
            }
            stb.append(      "UNION ");
            stb.append(   "SELECT ");
            stb.append(           "'999998' AS SUBCLASSCD, ");
            stb.append(           "INT(ROUND(AVG(FLOAT( SUM_HR )),0))AS AVG_HR, ");
            stb.append(           "0 AS SUM_HR ");
            stb.append(      "FROM  ( SELECT  SUM(" + fieldname + ")AS SUM_HR ");
            stb.append(            "FROM   RECORD_DAT W1 ");
            stb.append(            "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO and hr_class = ? ");
            stb.append(            "WHERE  W1.YEAR = '" + param[0] + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(                    "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(                    "AND W1.SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(               "AND " + fieldname + " IS NOT NULL ");
            stb.append(            "GROUP BY W1.SCHREGNO ");
            stb.append(      ")T1 ");
            stb.append(")W9 ON W9.SUBCLASSCD = W2.SUBCLASSCD ");

            //学年の科目別平均点の表
            stb.append("LEFT JOIN( ");
            stb.append(   "SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           "VALUE(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD,'999999') AS SUBCLASSCD,");
            } else {
                stb.append(           "VALUE(SUBCLASSCD,'999999') AS SUBCLASSCD,");
            }
            stb.append(           "ROUND(AVG(FLOAT(" + fieldname + "))*10,0)/10 AS AVG_GR ");
            stb.append(   "FROM   RECORD_DAT W1 ");
            stb.append(   "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(   "WHERE  W1.YEAR = '" + param[0] + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(           "AND W1.SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(      "AND " + fieldname + " IS NOT NULL ");
            
            if ("1".equals(_useCurriculumcd)) {
                stb.append(   "GROUP BY GROUPING SETS(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD,())");
            } else {
                stb.append(   "GROUP BY GROUPING SETS(SUBCLASSCD,())");
            }
            stb.append(      "UNION ");
            stb.append(   "SELECT  ");
            stb.append(           "'999998' AS SUBCLASSCD, ");
            stb.append(           "INT(ROUND(AVG(FLOAT( SUM_GR )),0))AS AVG_GR ");
            stb.append(      "FROM  ( SELECT  SUM(" + fieldname + ")AS SUM_GR ");
            stb.append(            "FROM   RECORD_DAT W1 ");
            stb.append(            "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(            "WHERE  W1.YEAR = '" + param[0] + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(               "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(               "AND W1.SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(               "AND " + fieldname + " IS NOT NULL ");
            stb.append(            "GROUP BY W1.SCHREGNO ");
            stb.append(      ")T1 ");
            stb.append(")W11 ON W11.SUBCLASSCD = W2.SUBCLASSCD ");

            stb.append("ORDER BY W2.SUBCLASSCD ");

        } catch( Exception ex ){
            log.warn("sql-statement error!",ex);
        }
        return stb.toString();

    }//prestatSubclassInfo()の括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表 
     *  2005/08/22
     */
    String prestatStdSubclassDetail()
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete(0, stb.length() );
        try {
            stb.append("WITH ");
            stb.append("SCHNO AS(");
            stb.append(        "SELECT  W1.SCHREGNO, W1.HR_CLASS, W1.ATTENDNO ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param[0] + "' ");  //05/10/05Build
            stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND W1.SEMESTER = '" + param[13] + "' ");
            stb.append(         "AND W1.GRADE = '" + param[2] + "' ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END) ");     //05/10/05Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END)) ) ");  //05/10/05Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            /* ***
            stb.append(       "AND NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                           "AND S1.GRD_DIV IN ('2','3') ");
            stb.append(                           "AND S1.GRD_DATE < '" + param[4] + "' ");
            stb.append(                     ") ");
            stb.append(       "AND NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                           "AND ((S1.TRANSFERCD IN ('1','2') AND '" + param[4] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                           "OR   (S1.TRANSFERCD IN ('4') AND '" + param[4] + "' < S1.TRANSFER_SDATE)) ");
            stb.append(                     ") ");
            *** */
            stb.append(") ");
            
            stb.append(", GR_RANK AS(");
            stb.append(      "SELECT  T1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "T1.SUBCLASSCD AS SUBCLASSCD");
            stb.append(             ",RANK() OVER(PARTITION BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                        "T1.SUBCLASSCD ORDER BY " + fieldname + " DESC) AS RANK ");
            stb.append(      "FROM    RECORD_DAT T1 ");
            stb.append(      "WHERE   YEAR = '" + param[0] + "' ");
            stb.append(          "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(          "AND " + fieldname + " IS NOT NULL ");
            stb.append(") ");
            
            stb.append(", HR_RANK AS(");
            stb.append(      "SELECT  T1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "T1.SUBCLASSCD AS SUBCLASSCD");
            stb.append(             ",RANK() OVER(PARTITION BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                            "T1.SUBCLASSCD ORDER BY " + fieldname + " DESC) AS RANK ");
            stb.append(      "FROM    RECORD_DAT T1 ");
            stb.append(      "WHERE   YEAR = '" + param[0] + "' ");
            stb.append(          "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.HR_CLASS = ? ) ");
            stb.append(          "AND " + fieldname + " IS NOT NULL ");
            stb.append(") ");
            
            stb.append("SELECT  T1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(       "T2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(       ",T2." + fieldname + " AS SCORE, T3.RANK AS GR_RANK, T4.RANK AS HR_RANK ");
            stb.append("FROM  ( SELECT  SCHREGNO ");
            stb.append(        "FROM    SCHNO ");
            stb.append(        "WHERE   HR_CLASS = ? ");
            stb.append(            "AND ATTENDNO BETWEEN ? AND ? ");
            stb.append(      ")T1 ");
            stb.append("INNER JOIN RECORD_DAT T2 ON T2.YEAR = '" + param[0] + "' AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN  GR_RANK T3 ON T3.SCHREGNO = T2.SCHREGNO AND T3.SUBCLASSCD = ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(      "T2.SUBCLASSCD ");
            stb.append("LEFT JOIN  HR_RANK T4 ON T4.SCHREGNO = T2.SCHREGNO AND T4.SUBCLASSCD = ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(      "T2.SUBCLASSCD ");
            stb.append("WHERE  " + fieldname + " IS NOT NULL ");

            stb.append("ORDER BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(          "T2.SUBCLASSCD, T1.SCHREGNO");

        } catch( Exception ex ){
            log.warn("sql-statement error!",ex);
        }
        return stb.toString();
    }


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表 部品
     */
    String prestatStdSubclassDetailDetail() {
        return null;
    }


    /** 
     *  PrepareStatement作成 成績総合データ 
     *  2005/08/22
     */
    String prestatStdTotalRec()
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete(0, stb.length() );
        try {
            //学年全体の学籍情報
            stb.append("WITH SCHNO_B AS(");
            stb.append(        "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
            stb.append(                "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
            stb.append(        "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param[0] + "' ");  //05/10/05Build
            stb.append(        "WHERE   W1.YEAR = '" + param[0] + "' ");
            stb.append(              "AND W1.SEMESTER = '" + param[13] + "' ");
            stb.append(         "AND W1.GRADE = '" + param[2] + "' ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END) ");     //05/10/05Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END)) ) ");  //05/10/05Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param[4] + "' THEN W2.EDATE ELSE '" + param[4] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            /* ***
            stb.append(         "AND NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                         "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                             "AND S1.GRD_DIV IN ('2','3') ");
            stb.append(                             "AND S1.GRD_DATE < '" + param[4] + "' ) ");  //異動日は在籍とする
            stb.append(         "AND NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                         "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                             "AND ((S1.TRANSFERCD IN ('1','2') AND '" + param[4] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                               "OR (S1.TRANSFERCD IN ('4') AND '" + param[4] + "' < S1.TRANSFER_SDATE)) ) ");
            *** */
            stb.append(        ")");

            //講座の表
            stb.append(", CHAIR_A AS(");
            stb.append(    "SELECT  S1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "S2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(    "FROM    CHAIR_STD_DAT S1, ");
            stb.append(            "CHAIR_DAT S2 ");
            stb.append(    "WHERE   S1.YEAR = '" + param[0] + "' ");
            if( Integer.parseInt( param[1] ) < 9 ){
                stb.append(       "AND S1.SEMESTER <= '" + param[1] + "' ");
                stb.append(    "AND S2.SEMESTER <= '" + param[1] + "' ");
            }
            stb.append(        "AND S2.YEAR = S1.YEAR ");
            stb.append(        "AND S2.SEMESTER = S1.SEMESTER ");
            stb.append(        "AND S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(        "AND EXISTS( SELECT 'X' FROM SCHNO_B S3 WHERE S3.SCHREGNO = S1.SCHREGNO AND S3.HR_CLASS = ? ) ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(           "AND S2.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(           "AND SUBSTR(S2.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(    "GROUP BY S1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || ");
            }
            stb.append(          " S2.SUBCLASSCD ");
            stb.append(") ");

            //学年全体の成績データの表（通常科目）  読替科目は含めない
            stb.append(", RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            "W3.CLASSCD, ");
                stb.append(            "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD, w1.hr_class, ");
            if( ! param[11].equals("0") ){
                //中間・期末成績
                stb.append(        "CASE WHEN " + fieldname + " IS NULL AND " + fieldname + "_DI IN('KK','KS') THEN " + fieldname + "_DI ");
                stb.append(             "WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "ELSE NULL END AS SCORE ");
            } else if( Integer.parseInt( param[1] ) != 9 ){
                //学期成績
                stb.append(       "RTRIM(CHAR(" + fieldname + ")) AS SCORE ");
            } else{
                //学年成績
                stb.append(       "CASE WHEN W3.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE, ");
                stb.append(       "GRAD_VALUE ");
            }
            stb.append(    "FROM    RECORD_DAT W3 ");
            stb.append(    "INNER JOIN SCHNO_B W1 ON W1.SCHREGNO = W3.SCHREGNO ");
            stb.append(    "WHERE   W3.YEAR = '" + param[0] + "' ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' ");
            stb.append(                       "FROM   SUBCLASS_REPLACE_DAT W2 ");
            stb.append(                       "WHERE ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                              "W3.SUBCLASSCD = ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append(                              "W2.GRADING_SUBCLASSCD AND ");
            stb.append(                              "W2.YEAR ='" + param[0] + "' AND ");
            stb.append(                              "W2.ANNUAL = '" + param[2] + "' AND REPLACECD = '1') ");
            stb.append(     ") ");

            //メイン表
            stb.append("SELECT T1.SCHREGNO,");
            stb.append(          "T4.SUM_REC, T5.AVG_REC, T4.CREDITS, T4.CREDITS2, T5.RANK AS HR_RANK, T6.RANK AS GR_RANK ");

            stb.append("FROM   SCHNO_B T1 ");

            //学級の総合点・履修単位・修得単位の表
            stb.append(        "LEFT JOIN(");
            stb.append(            "SELECT  W1.SCHREGNO,");
            stb.append(                    "SUM(INT(SCORE))AS SUM_REC,");
            stb.append(                    "SUM(CREDITS)AS CREDITS,");
            if( param[1].equals("9") )
                stb.append(                  "SUM(CASE WHEN 1 < VALUE(GRAD_VALUE,0) THEN CREDITS ELSE 0 END)AS CREDITS2 ");
            else
                stb.append(             "0 AS CREDITS2 ");
            stb.append(            "FROM    RECORD_REC W1 ");
            stb.append(         "INNER JOIN SCHNO_B W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.HR_CLASS = ? ");
            stb.append(            "LEFT JOIN CREDIT_MST W3 ON W3.YEAR = '" + param[0] + "' ");
            stb.append(                                   "AND W3.GRADE = '" + param[2] + "' ");
            stb.append(                                   "AND W3.COURSECD = W2.COURSECD ");
            stb.append(                                "AND W3.MAJORCD = W2.MAJORCD ");
            stb.append(                                   "AND W3.COURSECODE = W2.COURSECODE ");
            stb.append(                                "AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                   "W3.SUBCLASSCD = W1.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(            "WHERE   W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(            "WHERE   SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(             "AND EXISTS(SELECT 'X' FROM CHAIR_A W4 WHERE W4.SCHREGNO = W1.SCHREGNO AND W4.SUBCLASSCD = W1.SUBCLASSCD) ");
            stb.append(            "GROUP BY W1.SCHREGNO");
            stb.append(     ")T4 ON T4.SCHREGNO = T1.SCHREGNO ");

            //学級の平均点、席次の表
            stb.append(        "LEFT JOIN(");
            stb.append(            "SELECT  W1.SCHREGNO,");
            stb.append(                    "round(AVG(FLOAT(INT(SCORE)))*10,0)/10 AS AVG_REC,");
            stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");
            stb.append(                    "RANK() OVER(ORDER BY CASE ");
            stb.append(                                   "WHEN SUM(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
            stb.append(                                   "ELSE SUM(FLOAT(INT(SCORE))) END DESC)END AS RANK ");
            stb.append(            "FROM    RECORD_REC W1 ");
            stb.append(         "INNER JOIN SCHNO_B W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.HR_CLASS = ? ");
            stb.append(            "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(             "AND SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
                                        //１科目でも欠席があれば除外
            stb.append(             "AND W1.SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                                 "FROM   RECORD_REC ");
            stb.append(                                 "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )','欠','公') ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(             "AND SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
            stb.append(                                    "AND HR_CLASS = ? ");
            stb.append(                                 "GROUP BY SCHREGNO ");
            stb.append(                                 "HAVING 0 < COUNT(*) ");
            stb.append(                                 ") ");
            stb.append(         "GROUP BY W1.SCHREGNO");
            stb.append(     ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");

            //学年の席次の表
            stb.append(        "LEFT JOIN(");
            stb.append(            "SELECT  W1.SCHREGNO,");
            stb.append(                    "round(AVG(FLOAT(INT(SCORE)))*10,0)/10 AS AVG_REC,");
            stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");
            stb.append(                    "RANK() OVER(ORDER BY CASE ");
            stb.append(                                   "WHEN SUM(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
            stb.append(                                   "ELSE SUM(FLOAT(INT(SCORE))) END DESC)END AS RANK ");
            stb.append(            "FROM    RECORD_REC W1 ");
            stb.append(            "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "AND W1.CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
            } else {
                stb.append(             "AND SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
            }
                                        //１科目でも欠席があれば除外
            stb.append(             "AND SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                                 "FROM   RECORD_REC ");
            stb.append(                                 "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )','欠','公') AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(                                        "CLASSCD <= '" + KNJDefineSchool.subject_U + "'  ");
            } else {
                stb.append(                                        "SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "'  ");
            }
            stb.append(                                 "GROUP BY SCHREGNO ");
            stb.append(                                 "HAVING 0 < COUNT(*) ");
            stb.append(                                 ") ");
            stb.append(         "GROUP BY W1.SCHREGNO");
            stb.append(     ")T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE  T1.HR_CLASS = ? ");
        } catch( Exception ex ){
            log.warn("sql-statement error!",ex);
        }
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
            definecode.defineCode( db2, param[0] );         //各学校における定数等設定
log.debug("semesdiv="+definecode.semesdiv + "   absent_cov="+definecode.absent_cov + "   absent_cov_late="+definecode.absent_cov_late);
        } catch( Exception ex ){
            log.warn("semesterdiv-get error!",ex);
        }
    }


    /**
     *  SVF-FORM フィールドを初期化
     *    2005/05/22
     */
    private int clearSvfField( Map hm2, String lschname[] )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetForm("KNJD063.frm", 4);
            set_head2();

            ret = svf.VrsOut( "year2", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");    //年度            
            ret = svf.VrsOut( "ymd1",  param[18] );
            ret = svf.VrsOut( "DATE",  KNJ_EditDate.h_format_JP(param[6]) 
                                       + " \uFF5E " 
                                       + KNJ_EditDate.h_format_JP(param[4]) );         //出欠集計範囲 04/12/06Add

            printSvfStdNameOut( hm2, lschname );                            //生徒名等出力のメソッド

            //    組名称及び担任名の取得
            try {
                KNJ_Get_Info getinfo = new KNJ_Get_Info();        //各情報取得用のクラス
                KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
                returnval = getinfo.Hrclass_Staff( db2, param[0], param[1], param[3], "" );
                ret = svf.VrsOut( "HR_NAME", returnval.val1 );            //組名称
                ret = svf.VrsOut( "teacher", returnval.val3 );            //担任名
            } catch( Exception ex ){
                log.warn("HR_NAME... svf-out error!",ex);
            }
        } catch( Exception ex ){
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
            for( int i = 0 ; i < hr_attend.length    ; i++ )hr_attend[i] = 0;
            for( int i = 0 ; i < hr_seitosu.length   ; i++ )hr_seitosu[i] = 0;
            for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;
            for( int i = 0 ; i < sch_ccredits.length ; i++ )sch_ccredits[i] = 0;
            subclasslinecount = 0;
        } catch( Exception ex ){
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
            subclasslinecount = 0;
        } catch( Exception ex ){
            log.warn("clearSvfField error! ", ex);
        }
    }


    /** 
     *  対象校時取得
     *    2005/06/15 Build
     */
    void setPeiodValue()
    {
        StringBuffer stb2 = null;         //05/04/16
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            stb = new StringBuffer();
            stb.append(    "SELECT  NAMECD2 ");
            if( definecode.usefromtoperiod ){
                stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
                stb.append(        "AND S_PERIODCD <= NAMECD2 AND NAMECD2 <= E_PERIODCD ");
                stb.append(        "AND COURSECD IN(SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
                stb.append(                        "WHERE  W3.YEAR = '" + param[0] + "' ");
                stb.append(                               "AND W3.SEMESTER = '" + param[13] + "' ");
                stb.append(                               "AND W3.GRADE || W3.HR_CLASS = '" + param[3] + "') ");
            } else{
                stb.append("FROM    NAME_MST W1 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
            }
            stb.append("ORDER BY NAMECD2");

            ps = db2.prepareStatement( stb.toString() );
            rs = ps.executeQuery();
            int i = 0;
            while ( rs.next() ){
                if( i++ == 0 ){                                                        //05/04/16
                    stb2 = new StringBuffer();
                    stb2.append("(");
                }else{
                    stb2.append(",");
                }
                stb2.append( "'" ).append( rs.getString("NAMECD2") ).append( "'" );
            }
        } catch( Exception ex ){
            log.warn("periodname-get error!",ex);
        } finally{
            db2.commit();
            if( rs != null )try {rs.close();} catch( Exception ex ) {log.warn("periodname get-ResultSet error!",ex);}
            if( ps != null )try {ps.close();} catch( Exception ex ) {log.warn("ResultSet error!",ex);}
            if( stb2 != null ) { stb2.append(")");
            if( 0 < stb2.length() )    param[20] = stb2.toString();
            }
            else                    param[20] = "('1','2','3','4','5','6','7','8','9')";
        }
    }

    private KNJDefineCode setClasscode0(final DB2UDB db2) {
        KNJDefineCode definecode = null;
        try {
            definecode = new KNJDefineCode();
            definecode.defineCode(db2, param[0]);         //各学校における定数等設定
        } catch (Exception ex) {
            log.warn("semesterdiv-get error!", ex);
        }
        return definecode;
    }

    /**
     * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
     */
    private String setZ010Name1(DB2UDB db2) {
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
    public void loadAttendSemesArgument(DB2UDB db2) {
        String year = param[0];
        String semester = param[1];
        String date = param[4];
        
        try {
            loadSemester(db2, year);
            // 出欠の情報
            final KNJDefineCode definecode0 = setClasscode0(db2);
            final String z010Name1 = setZ010Name1(db2);
            _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, year, SSEMESTER, semester);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, date); // _sDate: 年度開始日, _date: LOGIN_DATE
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

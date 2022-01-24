/**
 *
 *  学校教育システム 賢者 [成績管理]  科目別高得点表（TOKIO用）
 *
 *  2004/07/21 yamashiro KNJD160から複写して作成
 *  2004/11/16 yamashiro・全科目平均の欄を追加、フォントはゴシック体
 *                      ・平均の網掛けは、平均と同得点がない場合は前後の行に網掛けをする
 *  2005/02/16 yamashiro KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様を変更
 *  2005/02/17 yamashiro 異動対象日のパラメータを追加
 *  2005/03/10 yamashiro 短期留学生を対象とする
 *                       評定読替科目を追加
 *  2005/06/08 yamashiro 試験を受けていない生徒が先の順位になる不具合を修正
 *                       学年成績以外は、留学生は出力しない
 *  2005/06/24 yamashiro KNJD160を基に作成
 *  2005/10/02 yamashiro・編入のデータ仕様変更および在籍異動条件に転入学を追加
 *
 */

package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.HashMap;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJ_ClassCodeImp;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJD161 {

    /** 0 : パラメータ学年/学期成績 */
    private static final String PARAM_GRAD_SEM_KIND = "0";
    /** 9900 : 学年/学期成績 */
    private static final String GRAD_SEM_KIND = "9900";

    private static final Log log = LogFactory.getLog(KNJD161.class);

    KNJ_ClassCodeImp ccimp;                 //教科コード等定数設定

    Map subclasscd_map = new HashMap();     //科目コードと列番号のマップ
    String subclassname_array[];            //科目名の配列
    int score_array[][];                    //科目別生徒別得点の配列
    int studentnum_array[];                 //科目別生徒(得点)数の配列
    int average_array[];                    //科目別平均点の配列
    private int maxline = 18;               //１ページ当たり列数
    private String fieldname;               //成績名称

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        String param[] = new String[22];
        boolean nonedata = false;           //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        //ＤＢ接続
        db2 = sd.setDb( request );
        if( sd.openDb( db2 ) ){
            log.error("db open error");
            return;
        }
        //パラメータの取得
        getParam( db2, request, param );

        //print svf設定
        sd.setSvfInit( request, response, svf);

        //印刷処理
        nonedata = printSvf( request, db2, svf, param );

        //終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb( db2 );
    }


    /**
     *  印刷処理
     */
    boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[] ){

        boolean nonedata = false;           //該当データなしフラグ

        try {
            Set_Head(db2,svf,param);                                //見出し出力のメソッド
            nonedata = ( Set_Detail_1(db2,svf,param) );             //帳票出力のメソッド
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        ccimp = new KNJ_ClassCodeImp(db2);              //教科コード・学校による設定区分

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJD160K.frm", 4);
        ret = svf.VrsOut("nendo"    ,nao_package.KenjaProperties.gengou
                                                    (Integer.parseInt(param[0])) + "年度");     //年度

    //  ＳＶＦ属性変更
        ret = svf.VrAttribute("number","FF=1");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("ymd",KNJ_EditDate.h_format_JP(returnval.val3));       //作成日
        } catch( Exception e ){
            log.debug("Set_Head() ctrl_date get error!", e );
        }

    //  学期名称の取得
        try {
            returnval = getinfo.Semester(db2,param[0],param[1]);
            ret = svf.VrsOut("term" ,returnval.val1);       //学期名称
        } catch( Exception e ){
            log.debug("Set_Head() Semester name get error!", e );
        }

    //  ３学期の開始日取得 05/03/09
        try {
            returnval = getinfo.Semester(db2,param[0],"3");
            param[8] = returnval.val2;                                  //学期期間FROM
        } catch( Exception ex ){
            log.warn("term1 svf-out error!",ex);
        } finally {
            if( param[8] == null ) param[8] = ( Integer.parseInt(param[0]) + 1 ) + "-03-31";
        }

    //  学年末の場合、対象学期を取得
        if( !param[1].equals("9") )
            param[5] = param[1];
        else
            param[5] = Get_Semester(db2,param);

    //  組名称及び担任名の取得
        try {
            returnval = getinfo.Hrclass_Staff(db2,param[0],param[5],param[2],"");
            ret=svf.VrsOut("HR_NAME"    ,returnval.val1);   //組名称
            ret=svf.VrsOut("teacher"    ,returnval.val3);   //担任名
        } catch( Exception e ){
            log.debug("Set_Head() teacher name get error!", e );
        }
        getinfo = null;
        returnval = null;

        if (!GRAD_SEM_KIND.equals(param[6])) {
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(param[20], param[0], param[1], param[6]);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    svf.VrsOut("TEST", rs.getString("TESTITEMNAME"));
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

    }//Set_Head()の括り



    /** 
     *   印刷処理メインルーチン 
     *   2005/02/16Modify
     */
    private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[]){

        boolean nonedata = false;
        _INTR_SCORE obj = setStatment( param );                   //SQL STATEMENT 05/02/16

    //  値の保存用配列を作成
        int studentnum = Get_Sch_Cnt(db2,param);              //生徒数の取得
        if( studentnum == 0 )studentnum = 1;
        int subclassnum = Get_Subj_Cnt(db2,param);            //科目数の取得
log.debug("subclassnum="+subclassnum);
        if( subclassnum == 0 )subclassnum = 1;
        subclassnum++;                                        //全科目平均の分 04/11/16
        score_array  = new int[subclassnum][studentnum];      //科目別全得点の配列
        subclassname_array = new String[subclassnum];         //科目名の配列
        studentnum_array  = new int[subclassnum];             //科目別得点件数の配列
        average_array  = new int[subclassnum];                //科目別平均点の配列
        String student_subclasscd_array[] = new String[subclassnum];          //生徒の科目コードの配列
        int student_row_array[] = new int[subclassnum];               //生徒の得点の配列

    //  SQL作成
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;

        try {
            ps1 = db2.prepareStatement( Pre_Stat1( param, obj, 1 ) );   //クラスの科目別全得点表             05/02/16Modify
            ps3 = db2.prepareStatement( Pre_Stat1( param, obj, 0 ) );   //クラスの全科目全得点表 04/11/15    05/02/16Modify
            ps2 = db2.prepareStatement( Pre_Stat2( param, obj ) );      //クラスの科目別平均点及び科目名の表 05/02/16Modify
        } catch( Exception ex ) {
            log.debug("Set_Detail_1 PreparedStatement error!");
            log.debug(ex);
            return false;
        }
//log.debug("ps2="+Pre_Stat2( param, obj ));
    //  科目別平均点及び科目名を保存しながら学級の全得点を保存する
        try {
            ResultSet rs2 = ps2.executeQuery();
            ResultSet rs1 = null;
            Integer lineInt = null;
            int ia = 0;
            while( rs2.next() ){
                lineInt = new Integer(ia);
                subclasscd_map.put(rs2.getString("SUBCLASSCD"),lineInt);    //科目コードに行番号をセットする
                subclassname_array[ia] = rs2.getString("SUBCLASSABBV");     //科目名の配列
                average_array[ia] = rs2.getInt("AVG_REC");              //科目別平均点の配列
                //科目別全得点を保存
                if( rs2.getString("SUBCLASSCD").equals("0") )
                    rs1 = ps3.executeQuery();
                else{
                    ps1.setString(1,rs2.getString("SUBCLASSCD"));
                    rs1 = ps1.executeQuery();
                }
                for( int ib=0 ; rs1.next() && ib<studentnum ; ib++ ){
                    if( rs1.getString("SCORE") != null )
                        score_array[ia][ib] = Integer.parseInt(rs1.getString("SCORE")); //得点
                        studentnum_array[ia]++;                                             //得点数
                }
                ia++;
            }
            db2.commit();
            rs2.close();
            if( rs1!=null )rs1.close();
            //log.debug("Set_Detail_1 read ok!");
        } catch( Exception ex ) {
            log.debug("Set_Detail_1 read error!");
            log.debug(ex);
        }

    //  生徒別科目別順位位置のSQL作成
        try {
            ps1 = db2.prepareStatement( Pre_Stat3( param, obj ) );  //生徒ごとの科目別の順位位置表（出席番号順） 05/02/16Modify
//log.debug("ps1="+ps1.toString());
        } catch( Exception ex ) {
            log.debug("Set_Detail_1 PreparedStatement error!");
            log.debug(ex);
            return false;
        }

       /*
        * 印刷
        *   Set_Detail_2において生徒毎に印刷を行う
        *       使用する配列  String student_subclasscd_array[] => 生徒の科目コードを入れる
        *                     int student_subclasscd_array[] => 生徒の得点を入れる
        */
        try {
            ResultSet rs = ps1.executeQuery();
            String schno = "0";         //学籍番号保存用
            int sc = 0;
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
//log.debug("ps1="+ps1.toString());
            while( rs.next() ){
                //生徒のブレイク
                if( !rs.getString("SCHREGNO").equals(schno) ){
                    if( !schno.equals("0") ) nonedata = Set_Detail_2(svf,student_subclasscd_array,student_row_array,studentnum);        //SVF出力
                    schno = rs.getString("SCHREGNO");                       //学籍番号の保存
                    ret = svf.VrsOut("number",rs.getString("ATTENDNO"));    //出席番号
                    ret = svf.VrsOut("name"  ,rs.getString("NAME"));        //生徒名
                    sc = 0;
                    for( int i = 0 ; i < student_subclasscd_array.length ; i++ )student_subclasscd_array[i] = null;
                    for( int i = 0 ; i < student_row_array.length ; i++ )student_row_array[i] = 0;
                }
                //生徒個別の科目コードと得点を保存
                
                final String setSubclassCd = rs.getString("SUBCLASSCD");
                student_subclasscd_array[sc] = setSubclassCd;
                student_row_array[sc] = rs.getInt("ROW");
//log.debug("subclasscd="+rs.getString("SUBCLASSCD"));
//log.debug("row="+rs.getInt("ROW"));
                sc++;
            }
            db2.commit();
            rs.close();
            if( !schno.equals("0") )nonedata = Set_Detail_2(svf,student_subclasscd_array,student_row_array,studentnum);     //SVF出力
            log.debug("Set_Detail_1() Stat3 read ok!");
        } catch( Exception ex ) {
            log.debug("Set_Detail_1() Stat3 read error!");
            log.debug(ex);
        }

        Pre_Stat_f(ps1, ps2, ps3);      //preparestatementを閉じる

        return nonedata;

    }//Set_Detail_1()の括り



    /**
      *  SVF-FORM
      *  生徒毎の印刷
      *  2004/11/16Modify
      *  parameter  String student_subclasscd_array[] => 生徒の科目コードが入っている
      *             int student_row_array[] => 生徒の順位位置が入っている
      *             int studentnum => 生徒数
      *  2005/03/10Modify １ページ当たりの列数を14から18に変更（フォームが変更されている？）  インスタンス変数で定数として定義
      **/
    private boolean Set_Detail_2(Vrw32alp svf,String student_subclasscd_array[],int student_row_array[],int studentnum){

        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        Integer m_line = null;
        int m_int = 0;
        int outcount = maxline;         //列残数出力用
        boolean second = false;

        try {
            //生徒個別の科目コードを読込む => １ページ１４列づつ
            for( int i = 0 ; i < student_subclasscd_array.length ; i += maxline ){
                //生徒個別の得点を読込む => １列５０名づつ
                for( int j = 0 ; j < studentnum ; j += 50 ){
                    //１ページ１４科目を出力
                    for( int k = 0 ; k < maxline ; k++ ){
                        if( ( i/maxline*maxline+k ) > student_subclasscd_array.length - 1 ) break;          //生徒個別の科目数で出力を制限する！
                        if( student_subclasscd_array[i/maxline*maxline+k] == null ) break;    // 04/11/17
                        m_line = (Integer)subclasscd_map.get(student_subclasscd_array[ i/maxline*maxline+k ]);//科目別保存番号の取得
                        if( m_line == null ) continue;
                        m_int = m_line.intValue();                                  //科目別保存用配列番号の取得
                        //平均がない場合の処理
                        if( !second  &&  !student_subclasscd_array[ i/maxline*maxline+k ].equals("0") ){
                            svfFieldAttribute(svf, "0");                    // SVF-FIELD-ATTRIBUTE 04/11/16
                            ret = svf.VrsOut("subject"  ,"平均");           //科目名
                            ret = svf.VrEndRecord();
                        }
                        second = true;
                        svfFieldAttribute(svf, student_subclasscd_array[ i/maxline*maxline+k ]);           // SVF-FIELD-ATTRIBUTE 04/11/16
                        ret = svf.VrsOut("subject"  ,subclassname_array[m_int]);            //科目名
                        //全科目得点平均および科目別得点５０個を出力
                        svfAmikakeToAverage( svf, m_int );
                        for( int l = 0 ; l < 50 ; l++ ){
                            if( ( j/50*50+l ) > studentnum_array[m_int]-1 ) break;      //科目別の生徒数で出力を制限する！
                            svfAmikakeToPoint( svf, j/50*50+l, student_row_array[ i/maxline*maxline+k ] );
                            ret = svf.VrsOut("point" + ( l+1 ), String.valueOf( score_array[ m_int ][ j/50*50+l ] ) );  //科目別全得点
                        }
//log.debug("k="+k);
                        ret = svf.VrEndRecord();
                        svfFieldInit(svf);   // 04/10/17Modify
                        nonedata = true;
                        outcount = k+1;
                    }
                    //科目列の出力が１４列に満たない場合、改ページのため空列を出力する！
                    for( ; outcount < maxline ; outcount++ ) ret = svf.VrEndRecord();
                    outcount = maxline;
                }
            }
        } catch( Exception ex ) {
            log.debug("Set_Detail_2() svf set error!");
            log.debug(ex);
        }
        return nonedata;

    }//Set_Detail_2()の括り


    /**
      *  PrepareStatement作成 共通部品
      *  学籍データから対象生徒を抽出
      *      異動者は除外
      *  2005/02/14 停学を対象に含める
      *  2005/02/17 異動対象日を印刷指示画面のパラメータに変更
      *  2005/03/10Modify 留学開始日が３学期の場合（短期留学）は対象とする
      *  2005/06/08Modify 学年成績以外は、留学において基準日が期間内にあれば対象外とする
      **/
    private String preStatCommon(String param[]){

        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS(");
        stb.append(     "SELECT  W1.SCHREGNO,W1.ATTENDNO ");
        stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param[0] + "' ");  //05/10/02Build
        stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' ");
        stb.append(         "AND W1.SEMESTER = '"+param[5]+"' ");
        stb.append(         "AND W1.GRADE||W1.HR_CLASS = '" + param[2] + "' ");

        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param[7] + "' THEN W2.EDATE ELSE '" + param[7] + "' END) ");     //05/10/02Modify
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param[7] + "' THEN W2.EDATE ELSE '" + param[7] + "' END)) ) ");  //05/10/02Modify
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param[7] + "' THEN W2.EDATE ELSE '" + param[7] + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/10/02Modify
        stb.append(     ") ");

        /* ***
        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND ");
        stb.append(                               "S1.GRD_DATE < '" + param[7] + "' ) AND ");  //05/03/09Modify
        if( Integer.parseInt( param[6] ) == 0  &&  Integer.parseInt( param[1] ) == 9 ){
            //05/03/09Modify 留学開始日が３学期の場合は成績も出力する
            stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('2') AND ");  // 05/02/10停学を除外
            stb.append(                                   "'" + param[7] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('1') AND ");
            stb.append(                                   "'" + param[7] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param[8] + "' ) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND '" + param[7] + "' < S1.TRANSFER_SDATE)) )) ");
        } else{
            //学年成績以外は、留学生は出力しない 05/06/08Modiy
            stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('2') AND ");  // 05/02/10停学を除外
            stb.append(                                   "'" + param[7] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('1') AND ");
            stb.append(                                   "'" + param[7] + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND '" + param[7] + "' < S1.TRANSFER_SDATE)) )) ");
        }
        *** */

        return stb.toString();
    }


    /** 
     *  SQL STATEMENT 成績データ再構築の選択肢
     *  2005/02/16
     */
    private _INTR_SCORE setStatment( String param[] )
    {
        _INTR_SCORE obj = null;
        try {
            if( param[6].equals("0101") ){
                obj = new _INTR_SCORE();
                fieldname = "SEM" + param[1] + "_INTR_SCORE";
            }else if( param[6].equals("0201") ){
                obj = new _TERM_SCORE();
                fieldname = "SEM" + param[1] + "_TERM_SCORE";
            }else if( param[6].equals("0202") ){
                obj = new _TERM2_SCORE();
                fieldname = "SEM" + param[1] + "_TERM2_SCORE";
            }else if( param[6].equals("0100") ){
                obj = new _INTR_VALUE();
                fieldname = "SEM" + param[1] + "_INTR_VALUE";
            }else if( param[6].equals("0200") ){
                obj = new _TERM_VALUE();
                fieldname = "SEM" + param[1] + "_TERM_VALUE";
            }else if( param[1].equals("9") ){
                obj = new _GRADE();
                fieldname = "GRAD_VALUE";
            }else {
                obj = new _GAKKI();
                fieldname = "SEM" + param[1] + "_VALUE";
            }
        } catch( Exception ex ){
            log.error("db new error:" + ex);
        }
log.debug("fieldname="+fieldname);
        return obj;
    }



    /**
      *  PrepareStatement作成
      *  任意科目のクラス全員の得点表および生徒別全科目平均表
      *  parameter  int pdiv => 0:全科目   1:科目別
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均から除外???
      *      試験の場合、公・欠の科目は除外
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      **/
    private String Pre_Stat1( String param[], _INTR_SCORE obj, int pdiv )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append( preStatCommon( param ) );


            if( ! param[6].equals(GRAD_SEM_KIND) ){
                stb.append(",RECORD_REC AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
            } else{
                stb.append(",RECORD_REC_A AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
                stb.append( statementReplaceData( param ) );//評価読替科目 05/03/09
                stb.append( statementRecordRec( param ) );  //成績データ(通常科目＋評価読替科目) 05/03/09
            }

            if( pdiv == 0 )
                stb.append( Pre_Stat1_0( param ) );     //生徒別全科目平均
            else
                stb.append( Pre_Stat1_1( param ) );     //生徒別科目別得点

        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }


    /**
      *  PrepareStatement作成 成績データ
      *  2005/03/09
      */
    private String statementRecordRec( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(",RECORD_REC AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(            "SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        CLASSCD, ");
                stb.append("        SCHOOL_KIND, ");
                stb.append("        CURRICULUM_CD, ");
            }
            stb.append(            "SCORE ");
            stb.append(     "FROM   RECORD_REC_A W1 ");
            stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC_B W2 WHERE ");
            stb.append(     "                   W1.SUBCLASSCD = W2.SUBCLASSCD ");
            if ("1".equals(param[21])) {
                stb.append("        AND W1.CLASSCD = W2.CLASSCD ");
                stb.append("        AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ");
                stb.append("        AND W1.CURRICULUM_CD = W2.CURRICULUM_CD ");
            }
            stb.append(     "UNION  ALL ");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(            "SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        CLASSCD, ");
                stb.append("        SCHOOL_KIND, ");
                stb.append("        CURRICULUM_CD, ");
            }
            stb.append(            "SCORE ");
            stb.append(     "FROM   REPLACE_REC_B W1 ");
            stb.append(     ") ");

        } catch( Exception e ){
            log.debug("error! ", e );
        }
        return stb.toString();
    }


    /**
      *  PrepareStatement作成 評価読み替え科目
      *  2005/03/09
      */
    private String statementReplaceData( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(",REPLACE_REC_A AS(");
            stb.append(     "SELECT SCHREGNO, ");
            if ("1".equals(param[21])) {
                stb.append("        W2.GRADING_CLASSCD AS CLASSCD, ");
                stb.append("        W2.GRADING_SCHOOL_KIND AS SCHOOL_KIND, ");
                stb.append("        W2.GRADING_CURRICULUM_CD AS CURRICULUM_CD, ");
                stb.append("        W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("        W2.ATTEND_CLASSCD AS ATTEND_CLASSCD, ");
                stb.append("        W2.ATTEND_SCHOOL_KIND AS ATTEND_SCHOOL_KIND, ");
                stb.append("        W2.ATTEND_CURRICULUM_CD AS ATTEND_CURRICULUM_CD, ");
                stb.append("        W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            } else {
                stb.append("W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            }
            stb.append(            "SCORE ");
            stb.append(     "FROM   RECORD_REC_A W1, SUBCLASS_REPLACE_DAT W2 ");
            stb.append(     "WHERE  W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD ");
            if ("1".equals(param[21])) {
                stb.append("        AND W1.CLASSCD = W2.ATTEND_CLASSCD ");
                stb.append("        AND W1.SCHOOL_KIND = W2.ATTEND_SCHOOL_KIND ");
                stb.append("        AND W1.CURRICULUM_CD = W2.ATTEND_CURRICULUM_CD ");
            }
            stb.append(            "AND W2.YEAR='" + param[0] + "' AND ANNUAL='" + param[2].substring(0, 2) + "' AND REPLACECD='1' ");
            stb.append(     ") ");

            stb.append(",REPLACE_REC_CNT AS(");
            stb.append(     "SELECT SCHREGNO,SUBCLASSCD,");
            if ("1".equals(param[21])) {
                stb.append("        CLASSCD, ");
                stb.append("        SCHOOL_KIND, ");
                stb.append("        CURRICULUM_CD, ");
            }
            stb.append(            "SUM(CASE WHEN SCORE IN ('KS','( )','欠') THEN 1 ELSE 0 END) AS KS ");
            stb.append(     "FROM REPLACE_REC_A W1 ");
            stb.append(     "WHERE W1.SCORE IN('KK','KS','( )','欠','公') ");
            stb.append(     "GROUP BY SCHREGNO,SUBCLASSCD ");
            if ("1".equals(param[21])) {
                stb.append("        , CLASSCD ");
                stb.append("        , SCHOOL_KIND ");
                stb.append("        , CURRICULUM_CD ");
            }
            stb.append(     "HAVING 0 < COUNT(*) ");
            stb.append(     ") ");

            stb.append(",REPLACE_REC_B AS(");
            stb.append(     "SELECT  SCHREGNO,SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        CLASSCD, ");
                stb.append("        SCHOOL_KIND, ");
                stb.append("        CURRICULUM_CD, ");
            }
            stb.append(             "RTRIM(CHAR(INT(ROUND(AVG(FLOAT(INT(SCORE))),0)))) AS SCORE ");
            stb.append(     "FROM    REPLACE_REC_A W1 ");
            stb.append(     "WHERE   W1.SCORE NOT IN('KK','KS','( )','欠','公') AND W1.SCORE IS NOT NULL ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_CNT W2 WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD) ");
            if ("1".equals(param[21])) {
                stb.append("        AND W2.CLASSCD = W1.CLASSCD ");
                stb.append("        AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
                stb.append("        AND W2.CURRICULUM_CD = W1.CURRICULUM_CD ");
            }
            stb.append(     "GROUP BY SCHREGNO,SUBCLASSCD ");
            if ("1".equals(param[21])) {
                stb.append("        , CLASSCD ");
                stb.append("        , SCHOOL_KIND ");
                stb.append("        , CURRICULUM_CD ");
            }
            stb.append(     ") ");

        } catch( Exception e ){
            log.debug("error! ", e );
        }
        return stb.toString();
    }


    /**
      *  PrepareStatement作成 生徒別全科目平均表
      *  2005/02/16Modify
      *  2005/03/10Modify KIN_RECORD_DATの評定読替科目は除外する
      */
    private String Pre_Stat1_0( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  INT(ROUND(AVG(FLOAT(INT(SCORE))),0)) AS SCORE,");
            stb.append(        "AVG(FLOAT(INT(SCORE))) AS FULLSCORE ");

            if( ! param[6].equals(GRAD_SEM_KIND) )
                stb.append("FROM    RECORD_REC W1 ");
            else
                stb.append("FROM    RECORD_REC_A W1 ");

            stb.append("WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
            stb.append(        "NOT EXISTS(SELECT 'X' ");
            stb.append(                   "FROM   SUBCLASS_REPLACE_DAT W2 ");
            stb.append(                   "WHERE  W1.SUBCLASSCD = W2.GRADING_SUBCLASSCD AND ");
            if ("1".equals(param[21])) {
                stb.append(                          "W1.CLASSCD = W2.GRADING_CLASSCD AND ");
                stb.append(                          "W1.SCHOOL_KIND = W2.GRADING_SCHOOL_KIND AND ");
                stb.append(                          "W1.CURRICULUM_CD = W2.GRADING_CURRICULUM_CD AND ");
            }
            stb.append(                          "W2.YEAR ='" + param[0] + "' AND ");
            stb.append(                          "W2.ANNUAL = '" + param[2].substring(0, 2) + "' AND ");
            stb.append(                          "W2.REPLACECD = '1') AND ");
            stb.append(        "SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                        "FROM   RECORD_REC ");
            stb.append(                        "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
            stb.append(                        "GROUP BY SCHREGNO ");
            stb.append(                        "HAVING 0 < COUNT(*) ");
            stb.append(                        ") ");
            stb.append("GROUP BY SCHREGNO ");
            stb.append("ORDER BY FULLSCORE DESC");
        } catch( Exception e ){
            log.debug("error! ", e );
        }
        return stb.toString();
    }


    /**
      *  PrepareStatement作成 生徒別科目別得点
      *  2005/02/16Modify
      */
    private String Pre_Stat1_1( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  INT(SCORE) AS SCORE ");
            stb.append("FROM    RECORD_REC ");
            stb.append("WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
            if ("1".equals(param[21])) {
                stb.append(        "CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? ");
            } else {
                stb.append(        "SUBCLASSCD = ? ");
            }
            stb.append("ORDER BY INT(SCORE) DESC");
        } catch( Exception e ){
            log.debug("Pre_Stat1 error!", e );
        }
        return stb.toString();
    }


    /**
      *  PrepareStatement作成
      *  クラスの全科目平均点・科目別平均点・科目名の表
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均から除外???
      *      試験の場合、公・欠の科目は科目別席次から除外
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      *      
      **/
    private String Pre_Stat2( String param[], _INTR_SCORE obj ){

        StringBuffer stb = new StringBuffer();
        try {
            stb.append( preStatCommon( param ) );
            //stb.append(",RECORD_REC AS(");
            //stb.append( obj.statementDetail( param ) );
            //stb.append(     ") ");

            if( ! param[6].equals(GRAD_SEM_KIND) ){
                stb.append(",RECORD_REC AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
            } else{
                stb.append(",RECORD_REC_A AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
                stb.append( statementReplaceData( param ) );//評価読替科目 05/03/09
                stb.append( statementRecordRec( param ) );  //成績データ(通常科目＋評価読替科目) 05/03/09
            }

            stb.append("SELECT ");
            if ("1".equals(param[21])) {
                stb.append(" S1.CLASSCD || S1.SCHOOL_KIND || S1.CURRICULUM_CD || S1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(" S1.SUBCLASSCD, ");
            }
            stb.append(" SUBCLASSABBV, AVG_REC ");
            stb.append("FROM  (SELECT  SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append(                       "  CLASSCD, ");
                stb.append(                       "  SCHOOL_KIND, ");
                stb.append(                       "  CURRICULUM_CD, ");
            }
            stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE))),0))AS AVG_REC ");
            stb.append(       "FROM    RECORD_REC W1 ");
            stb.append(       "WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') ");
            stb.append(       "GROUP BY ");
            if ("1".equals(param[21])) {
                stb.append(                       "  CLASSCD, ");
                stb.append(                       "  SCHOOL_KIND, ");
                stb.append(                       "  CURRICULUM_CD, ");
            }
            stb.append(       "SUBCLASSCD)S1,");
            stb.append(       "SUBCLASS_MST S2 ");
            stb.append("WHERE  ");
            if ("1".equals(param[21])) {
                stb.append(" S1.CLASSCD = S2.CLASSCD AND ");
                stb.append(" S1.SCHOOL_KIND = S2.SCHOOL_KIND AND ");
                stb.append(" S1.CURRICULUM_CD = S2.CURRICULUM_CD AND ");
            }
            stb.append(" S1.SUBCLASSCD = S2.SUBCLASSCD ");

            stb.append("UNION  SELECT  '0' AS SUBCLASSCD, ");
            stb.append(               "'平均' AS SUBCLASSABBV, ");
            stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE))),0))AS AVG_REC ");
            stb.append(       "FROM    RECORD_REC W1 ");
            stb.append(       "WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
                    /*
            stb.append(               "SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                               "FROM   RECORD_REC ");
            stb.append(                               "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
            stb.append(                               "GROUP BY SCHREGNO ");
            stb.append(                               "HAVING 0 < COUNT(*) ) AND ");
                    */
            stb.append(               "NOT EXISTS(SELECT 'X' ");
            stb.append(                          "FROM   SUBCLASS_REPLACE_DAT W2 ");
            stb.append(                          "WHERE ");
            if ("1".equals(param[21])) {
                stb.append(                                 "W1.CLASSCD = W2.GRADING_CLASSCD AND ");
                stb.append(                                 "W1.SCHOOL_KIND = W2.GRADING_SCHOOL_KIND AND ");
                stb.append(                                 "W1.CURRICULUM_CD = W2.GRADING_CURRICULUM_CD AND ");
            }
            stb.append(                                 "W1.SUBCLASSCD = W2.GRADING_SUBCLASSCD AND ");
            stb.append(                                 "W2.YEAR ='" + param[0] + "' AND ");
            stb.append(                                 "W2.ANNUAL = '" + param[2].substring(0, 2) + "' AND REPLACECD = '1') ");
            stb.append("ORDER BY SUBCLASSCD");
        } catch( Exception e ){
            log.debug("Pre_Stat2 error!", e );
        }
        return stb.toString();

    }//Pre_Stat2()の括り


    /**
      *  PrepareStatement作成
      *  科目別の順位位置表（出席番号順）
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均席次から除外
      *      試験の場合、公・欠の科目は科目別席次から除外
      *      科目別席次は、得点・全科目平均点・出席番号の順
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      *                   全科目平均には評定読替科目を含めない
      *      
      **/
    private String Pre_Stat3( String param[], _INTR_SCORE obj ){

        StringBuffer stb = new StringBuffer();
        try {
            stb.append( preStatCommon( param ) );
            //stb.append(",RECORD_REC AS(");
            //stb.append( obj.statementDetail( param ) );
            //stb.append(     ") ");

            if( ! param[6].equals(GRAD_SEM_KIND) ){
                stb.append(",RECORD_REC AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
            } else{
                stb.append(",RECORD_REC_A AS(");
                stb.append( obj.statementDetail( param ) );
                stb.append(     ") ");
                stb.append( statementReplaceData( param ) );//評価読替科目 05/03/09
                stb.append( statementRecordRec( param ) );  //成績データ(通常科目＋評価読替科目) 05/03/09
            }

            //生徒の学籍の表
            stb.append(",SCHNO_A AS(");
            stb.append(    "SELECT W1.SCHREGNO,ATTENDNO,NAME ");
            stb.append(    "FROM   SCHNO W1,");
            stb.append(           "SCHREG_BASE_MST W2 ");
            stb.append(    "WHERE  W1.SCHREGNO = W2.SCHREGNO), ");

            //生徒の全科目平均の表 04/11/16
            stb.append("TOTALAVERAGE AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            stb.append(            "AVG(FLOAT(INT(SCORE)))AS TOTALAVERAGESCORE ");
            stb.append(    "FROM    RECORD_REC W1 ");
            stb.append(    "WHERE   SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                            "FROM   RECORD_REC ");
            stb.append(                            "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
            stb.append(                            "GROUP BY SCHREGNO ");
            stb.append(                            "HAVING 0 < COUNT(*) ) AND ");
            stb.append(            "NOT EXISTS(SELECT 'X' ");
            stb.append(                       "FROM   SUBCLASS_REPLACE_DAT W2 ");
            stb.append(                       "WHERE  W1.SUBCLASSCD = W2.GRADING_SUBCLASSCD AND ");
            if ("1".equals(param[21])) {
                stb.append(                       "  W1.CLASSCD = W2.GRADING_CLASSCD AND ");
                stb.append(                       "  W1.SCHOOL_KIND = W2.GRADING_SCHOOL_KIND AND ");
                stb.append(                       "  W1.CURRICULUM_CD = W2.GRADING_CURRICULUM_CD AND ");
            }
            stb.append(                              "W2.YEAR ='" + param[0] + "' AND ");
            stb.append(                              "W2.ANNUAL = '" + param[2].substring(0, 2) + "' AND REPLACECD = '1') ");
            stb.append(       "AND SCORE IS NOT NULL ");  //05/06/08
            stb.append(    "GROUP BY SCHREGNO) ");

            //メイン表
            stb.append("SELECT  ATTENDNO, SCHREGNO, NAME, ROW ");
            if ("1".equals(param[21])) {
                stb.append(                       "  ,CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append(                       "  ,SUBCLASSCD ");
            }
            stb.append("FROM  (SELECT ATTENDNO, T1.SCHREGNO, NAME, T1.SUBCLASSCD, SCORE, ");
            if ("1".equals(param[21])) {
                stb.append(                       "  T1.CLASSCD, ");
                stb.append(                       "  T1.SCHOOL_KIND, ");
                stb.append(                       "  T1.CURRICULUM_CD, ");
            }
            if ("1".equals(param[21])) {
                stb.append(              "ROW_NUMBER() OVER(PARTITION BY CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ORDER BY SCORE DESC,VALUE(TOTALAVERAGESCORE,0) DESC,ATTENDNO)AS ROW ");
            } else {
                stb.append(              "ROW_NUMBER() OVER(PARTITION BY SUBCLASSCD ORDER BY SCORE DESC,VALUE(TOTALAVERAGESCORE,0) DESC,ATTENDNO)AS ROW ");
            }
            stb.append(       "FROM  (SELECT  SUBCLASSCD, W1.SCHREGNO, ATTENDNO, NAME, ");
            if ("1".equals(param[21])) {
                stb.append(                       "  CLASSCD, ");
                stb.append(                       "  SCHOOL_KIND, ");
                stb.append(                       "  CURRICULUM_CD, ");
            }
            stb.append(                      "CASE WHEN SCORE IN('KK', 'KS','( )') THEN -1 ELSE INT(SCORE) END AS SCORE ");
            stb.append(              "FROM    RECORD_REC W1,");
            stb.append(                      "SCHNO_A W2 ");
            stb.append(              "WHERE   W2.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                      "SCORE IS NOT NULL ");
            stb.append(              ")T1 ");
            stb.append(              "LEFT JOIN TOTALAVERAGE T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(       ")AS S1 ");
            stb.append("WHERE SCHREGNO IN" + param[3]);

            stb.append("UNION SELECT  ATTENDNO, SCHREGNO, NAME, ROW, SUBCLASSCD ");
            stb.append("FROM  (SELECT ATTENDNO, W2.SCHREGNO, NAME, '0' AS SUBCLASSCD,");
            stb.append(              "CASE WHEN W1.SCHREGNO IS NULL THEN 50 ELSE ROW_NUMBER() OVER(ORDER BY case when w1.schregno is null then -1 else TOTALAVERAGESCORE end DESC,ATTENDNO) END AS ROW ");
            stb.append(       "FROM   SCHNO_A W2 ");
            stb.append(              "LEFT JOIN TOTALAVERAGE W1 ON W2.SCHREGNO = W1.SCHREGNO");
            stb.append(       ")T1 ");
            stb.append("WHERE SCHREGNO IN" + param[3]);
            stb.append("ORDER BY ATTENDNO, SUBCLASSCD ");
        } catch( Exception e ){
            log.debug("Pre_Stat3 error!", e );
        }
        return stb.toString();

    }//Pre_Stat3()の括り


    /**
      *  学籍データの該当年度における最大学期を取得 
      **/
    private String Get_Semester(DB2UDB db2,String param[]) {

        String semester = "0";
        try{
            String sql = "SELECT MAX(SEMESTER) FROM SCHREG_REGD_HDAT WHERE YEAR='"
                        +param[0]+"' AND GRADE||HR_CLASS='"+param[2]+"'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if( rs.next() )semester = rs.getString(1);
            db2.commit();
            rs.close();
            sql = null;
        } catch( Exception ex ){
            log.debug("int Get_Sch_Cnt()  get count1 error!", ex );
        }
        return semester;

    }//private int Get_Semester()


    /**
      *  生徒数を取得 
      **/
    private int Get_Sch_Cnt(DB2UDB db2,String param[]) {

        int sch_cnt = 0;
        try{
            String sql = "SELECT COUNT(DISTINCT SCHREGNO) "
                       + "FROM   RECORD_DAT W1 "
                       + "WHERE  YEAR = '" + param[0]+"' AND "
                              + "EXISTS(SELECT 'X' "
                                     + "FROM   SCHREG_REGD_DAT W2 "
                                     + "WHERE  W2.YEAR = '" + param[0] + "' AND "
                                            + "W2.SEMESTER = '"+param[5]+ "' AND "
                                            + "W2.GRADE||W2.HR_CLASS = '" + param[2] + "' AND "
                                            + "W2.SCHREGNO = W1.SCHREGNO )  AND "
                              +  fieldname + " IS NOT NULL";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if( rs.next() )sch_cnt = rs.getInt(1);
            db2.commit();
            rs.close();
            sql = null;
        } catch( Exception ex ){
            log.debug("int Get_Sch_Cnt()  get count1 error!", ex );
        }
        return sch_cnt;

    }//private int Get_Sch_Cnt()


    /**
      *  全科目数を取得 
      *  2005/03/09Modify
      **/
    private int Get_Subj_Cnt(DB2UDB db2,String param[]) {

        int subj_cnt = 0;
        try{
            StringBuffer stb = new StringBuffer();
            stb.append( preStatCommon( param ) );
            stb.append("SELECT COUNT(*) ");
            stb.append("FROM( SELECT ");
            if ("1".equals(param[21])) {
                stb.append(                       "  CLASSCD, ");
                stb.append(                       "  SCHOOL_KIND, ");
                stb.append(                       "  CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append(       "FROM   RECORD_DAT W1 ");
            stb.append(       "WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(              "EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO = W1.SCHREGNO) AND ");
            stb.append(               fieldname + " IS NOT NULL ");
            stb.append(       "GROUP BY ");
            if ("1".equals(param[21])) {
                stb.append(                       "  CLASSCD, ");
                stb.append(                       "  SCHOOL_KIND, ");
                stb.append(                       "  CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append(     ") S1 ");

            db2.query( stb.toString() );
            ResultSet rs = db2.getResultSet();
            if( rs.next() )subj_cnt = rs.getInt(1);
            db2.commit();
            rs.close();
        } catch( Exception ex ){
            log.debug("int Get_Subj_Cnt()  get count1 error!", ex );
        }
        return subj_cnt;

    }//private int Get_Subj_Cnt()


    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1, PreparedStatement ps2, PreparedStatement ps3)
    {
        try {
            if( ps1 != null )ps1.close();
            if( ps2 != null )ps2.close();
            if( ps3 != null )ps3.close();
        } catch( Exception e ){
            log.debug("Pre_Stat_f error!", e );
        }
    }//Pre_Stat_f()の括り


    /**
      *
      *  SVF-FORM FIELDの初期化
      *  
      **/
    private void svfFieldInit(Vrw32alp svf){

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        for( int i = 0 ; i < 50 ; i++ ){
            ret = svf.VrAttribute("point" + (i+1) , ",Bold=0");
            ret = svf.VrsOut("point" + (i+1) , "");
        }

    }


    /**
      *
      *  SVF-FORM FIELD属性変更 
      *  subclasscdにより、科目名および得点を、明朝体・ゴシック体に切り替える
      *  parameter  subclasscd => 科目コード  但し"0"は全科目平均
      **/
    private void svfFieldAttribute(Vrw32alp svf, String subclasscd){

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if( subclasscd != null )
            if( subclasscd.equals("0") ){
                ret = svf.VrAttribute("subject", "ZenFont=1");
                //for( int i = 1 ; i <= 50 ; i++ )ret = svf.VrAttribute("point" + i  , "ZenFont=1");
            } else{
                ret = svf.VrAttribute("subject", "ZenFont=0");
                //for( int i = 1 ; i <= 50 ; i++ )ret = svf.VrAttribute("point" + i  , "ZenFont=0");
            }

    }


    /**
      *
      *  SVF-FORM FIELD属性変更 
      *  平均得点に網掛けを設定する
      *  parameter  m_int => 科目別配列の要素番号
      **/
    private void svfAmikakeToAverage(Vrw32alp svf,int position){

        boolean booset = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int i = 0;
        //５０個の得点をみていく。科目別平均点と同点なら網掛けをする。得点が平均点以下ならブレイク。
        for( i = 0 ; i < score_array[position].length ; i++ ){
            if( score_array[position][i] == average_array[position] ){
                ret = svf.VrAttribute("point" + (i+1) ,"Paint=(2,90,2),Bold=1");
                booset = true;
            }
            if( score_array[position][i] < average_array[position] ) break;
        }

        //平均点と同点がないなら、平均点の前後の行に網掛けをする。
        if( !booset ){
            if( i < score_array[position].length )
                ret = svf.VrAttribute("point" + (i+1) ,"Paint=(2,90,2),Bold=1");
            if( 0 <= ( i -= 1 ) )
                ret = svf.VrAttribute("point" + (i+1) ,"Paint=(2,90,2),Bold=1");
        }
    }


    /**
      *
      *  SVF-FORM FIELD属性変更 
      *  生徒の得点に網掛けを設定する
      *  parameter  ichi => 帳票の行番号  row => 生徒個人の順位位置
      **/
    private void svfAmikakeToPoint(Vrw32alp svf, int ichi, int row){
//log.debug("ichi="+ichi+"   row="+row);
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        //帳票の行が生徒個人の順位位置に等しいなら、網掛けをする。
        if( ichi+1 == row ) ret = svf.VrAttribute("point"+(ichi+1) ,"Paint=(2,50,2),Bold=1");
    }




    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    void getParam( DB2UDB db2, HttpServletRequest request, String param[] ){
        try {
            log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("SEMESTER");        //1-3:学期 9:学年末
            param[2] = request.getParameter("GRADE_HR_CLASS");  //学年
            param[7] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //出欠集計日付 05/02/17
            param[20] = request.getParameter("COUNTFLG");
            param[21] = request.getParameter("useCurriculumcd");

            //学籍番号の編集
            String schno[] = request.getParameterValues("category_name");   // 学籍番号
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for( int ia=0 ; ia<schno.length ; ia++ ){
                if( ia>0 )stb.append(",");
                stb.append("'");
                stb.append(schno[ia]);
                stb.append("'");
            }
            stb.append(")");
            param[3] = stb.toString();
            param[6] = request.getParameter("TEST");            //テスト種別 (中間・期末) 04/11/16Add
            param[6] = PARAM_GRAD_SEM_KIND.equals(param[6]) ? GRAD_SEM_KIND : param[6];
        } catch( Exception ex ) {
            log.error("parameter error! ",ex);
        }
    }


/* <================================ 内部クラス ==================================> */

/**
 *
 *   成績データの再構築  中間試験用
 *   2005/02/16
 */
private class _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_INTR_SCORE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_INTR_SCORE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}


/**
 *   成績データの再構築  期末試験用
 *   2005/02/16
 */
private class _TERM_SCORE extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_TERM_SCORE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_TERM_SCORE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}

/**
 *   成績データの再構築  期末試験用
 *   2005/02/16
 */
private class _TERM2_SCORE extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_TERM2_SCORE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_TERM2_SCORE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}
/**
 *
 *   成績データの再構築  中間評価用
 *   2005/02/16
 */
private class _INTR_VALUE extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_INTR_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_INTR_VALUE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}


/**
 *   成績データの再構築  期末評価用
 *   2005/02/16
 */
private class _TERM_VALUE extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_TERM_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_TERM_VALUE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}


/**
 *   成績データの再構築  学期成績用
 *   2005/02/16
 */
private class _GAKKI extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN SEM" + param[1] + "_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM" + param[1] + "_VALUE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}


/**
 *   成績データの再構築  学年成績用
 *   2005/02/16
 */
private class _GRADE extends _INTR_SCORE
{
    /**
     *  PrepareStatementの部品
     */
    String statementDetail( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
            if ("1".equals(param[21])) {
                stb.append("        W3.CLASSCD, ");
                stb.append("        W3.SCHOOL_KIND, ");
                stb.append("        W3.CURRICULUM_CD, ");
            }
            stb.append(        "CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    RECORD_DAT W3 ");
            stb.append("WHERE   W3.YEAR = '" + param[0] + "' AND ");
            stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
            stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();
    }
}


}

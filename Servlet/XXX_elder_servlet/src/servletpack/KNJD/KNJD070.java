/**
 *
 *  学校教育システム 賢者 [成績管理] 学習記録報告書 (HIRO用)
 *
 *  2004/08/18 yamashiro・講座クラスデータの同時展開の講座コードゼロに対応
 *  2004/11/12 yamashiro・中間成績、期末成績の出力を追加
 *  2005/06/22 yamasihro・共通化のための調整
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Assess;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;


public class KNJD070 {

    private static final Log log = LogFactory.getLog(KNJD070.class);

    private int maxline = 60;    //１ページ当たりの行数
    
    private Param param;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        boolean nonedata = false;           //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        //ＤＢ接続
        db2 = sd.setDb( request );
        if( sd.openDb( db2 ) ){
            log.error("db open error");
            return;
        }
        //パラメータの取得
        param = getParam(db2, request);

        //print svf設定
        sd.setSvfInit( request, response, svf);

        //印刷処理
        nonedata = printSvf(request, db2, svf, param);

        //終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb( db2 );
    }


    /**
     *  印刷処理
     */
    boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, Param param) {

        boolean nonedata = false;           //該当データなしフラグ
        String subject[] = null;            // 科目
        PreparedStatement ps[] = new PreparedStatement[4];

        try {
            subject = request.getParameterValues("SUBCLASS_SELECTED");      //科目
            ps[0] = db2.prepareStatement( Pre_Stat_1( param ) );            //科目見出し
            ps[1] = db2.prepareStatement( Pre_Stat_2( param ) );            //評定・出欠データ
            ps[2] = db2.prepareStatement( Pre_Stat_3( param, 0 ) );         //学級平均
            ps[3] = db2.prepareStatement( Pre_Stat_3( param, 1 ) );         //校内平均

            setHead( db2, svf, param );                                 //見出し設定
            for( int i = 0 ; i < subject.length ; i++ ){
                setSubclassHead( db2, param, subject[i], ps );              //科目見出し設定
                if( svfprintSubclassHead( svf, param ) ) nonedata = true;   //科目見出し出力
                if( printsvfDetail( db2, svf, param, subject[i], ps ) ) nonedata = true;            //評定・出欠データ出力
                if( printsvfTotal( db2, svf, param, subject[i], ps, nonedata ) ) nonedata = true;   //学級・校内平均出力
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally {
            for (int i = 0; i < ps.length; i++) {
                DbUtils.closeQuietly(ps[i]);
            }
        }
        return nonedata;
    }


    /**
     *  ページ見出し・初期設定
     */
    void setHead( DB2UDB db2, Vrw32alp svf, Param param){

        svf.VrSetForm("KNJD070.frm", 1);

        //作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control();
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            param._4 = returnval.val3;          //現在処理日
        } catch( Exception e ){
            log.error("setHead today get error!",e );
        }

        //組名称及び担任名の取得
        try {
            KNJ_Grade_Hrclass hrclass_staff = new KNJ_Grade_Hrclass();
            KNJ_Grade_Hrclass.ReturnVal returnval = hrclass_staff.Hrclass_Staff(db2,param._0,param._1,param._2,param._3);
            param._5 = returnval.val1;          //組名称
            param._7 = returnval.val3;          //担任名
        } catch( Exception e ){
            log.error("setHead hrclass_staff error!",e );
        }

        //学期名称の取得
        try {
            KNJ_Semester semester = new KNJ_Semester();
            KNJ_Semester.ReturnVal returnval = semester.Semester_T(db2,param._0);
            StringTokenizer scode = new StringTokenizer(returnval.val1, ",",true);  //学期コード
            StringTokenizer sname = new StringTokenizer(returnval.val2, ",",true);  //学期名称
            String sia = null;
            String strx = null;
            for( ; ; ){
                if( !scode.hasMoreTokens() )    break;      //学期コード
                sia = scode.nextToken();
                if( !sname.hasMoreTokens() )    continue;   //学期名称
                strx = sname.nextToken();
                if( sia.equals("1") )   param._8 = strx;
                if( sia.equals("2") )   param._9 = strx;
                if( sia.equals("3") )   param._10 = strx;
                if( sia.equals("9") )   param._11 = strx;
            }
        } catch( Exception e ){
            log.error("setHead Semester error!",e );
        }

        //単位保留値の取得
        try {
            KNJ_Assess assess = new KNJ_Assess();
            KNJ_Assess.ReturnVal returnval = assess.FearvalInfo(db2,param._0);
            param._12 = returnval.val1;     //学期保留値
            param._13 = returnval.val2;     //学年保留値
        } catch( Exception e ){
            log.error("setHead assess error!",e );
        }

        //成績処理種別 04/11/12Add
        try {
            if( param._14.substring(0,2).equals("01") )
                param._15 = "中間";           // 成績処理区分
            else if( param._14.substring(0,2).equals("02") )
                param._15 = "期末";           // 成績処理区分
        } catch( Exception e ){
            log.error("setHead param._15 error!",e );
        }

        //行を詰めて出力するか否か？
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement("SELECT MAX(ATTENDNO) FROM SCHREG_REGD_DAT WHERE YEAR = '" + param._0 + "' AND GRADE = '" + param._2 + "' AND HR_CLASS = '" + param._3 + "' ");
            rs = ps.executeQuery();
            if ( rs.next()  &&  maxline < rs.getInt(1) )param._20 = "1";
            db2.commit();
        } catch( Exception e ){
            log.error("MAX(ATTENDNO) ResultSet error! ", e );
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }


    /**
     *  科目見出し設定
     */
    void setSubclassHead( DB2UDB db2, Param param, String subclasscd, PreparedStatement ps[] )
    {
        try {
            ps[0].setString(1,subclasscd);
            ps[0].setString(2,subclasscd);
            ResultSet rs = ps[0].executeQuery();
            boolean first = false;
            StringBuffer stb = new StringBuffer();

            while( rs.next() ){
                if( ! first ){
                    param._16 = rs.getString("CREDIT");
                    param._17 = rs.getString("CLASSNAME");
                    param._18 = rs.getString("SUBCLASSNAME");
                    first = true;
                }
                if( rs.getString("STAFFNAME") != null )
                    if( stb.length() == 0 )stb.append( rs.getString("STAFFNAME") );
                    else                   stb.append(",").append( rs.getString("STAFFNAME") );
            }
            db2.commit();
            rs.close();
            param._19 = stb.toString();             //科目担任名
        } catch( Exception e ){
            log.error("setSubclassHead() error!",e );
        }
    }



    /**
     *  科目見出し出力
     */
    boolean svfprintSubclassHead( Vrw32alp svf, Param param){

        boolean nonedata = false;

        try {
            svf.VrsOut("nendo",       nao_package.KenjaProperties.gengou( Integer.parseInt( param._0 ) ) );   //年度
            svf.VrsOut("TODAY",       KNJ_EditDate.h_format_JP( param._4 ) );                                     //現在処理日
            svf.VrsOut("HR_NAME",     param._5  );      //組名称
            svf.VrsOut("STAFFNAME2",  param._7  );      //担任名
            svf.VrsOut("term1",       param._8  );      //学期名称
            svf.VrsOut("term2",       param._9  );      //学期名称
            svf.VrsOut("term3",       param._10 );      //学期名称
            svf.VrsOut("term4",       param._11 );      //学期名称

            svf.VrsOut("TEST",        param._15 );      //成績処理区分
            svf.VrsOut("tani",        param._16 );      //単位数
            svf.VrsOut("MAJORNMAE",   param._17 );      //教科名
            svf.VrsOut("CLASSNAME",   param._18 );      //科目名
            svf.VrsOut("STAFFNAME",   param._19 );      //科目担任名

            if( param._20 == null )
                for( int i = 1 ; i <= maxline ; i++ )
                    svf.VrsOutn( "NUMBER",    i, String.valueOf( i ) ); //出席番号

        } catch( Exception e ){
            log.error("Set_Detail_1 error!",e );
        }
        return nonedata;
    }



    /** ps2.executeQuery() **/
    ResultSet setResultsetPs2( PreparedStatement ps, String subclasscd ){

        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            rs = ps.executeQuery();
        } catch( Exception e ){
            log.error("ResultSet setResultsetPs2 error!",e );
        }
        return rs;

    }//ResultSet setResultsetPs2()



    /** ps3.executeQuery() **/
    ResultSet setResultsetPs3( PreparedStatement ps, String subclasscd ){

        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            rs = ps.executeQuery();
        } catch( Exception e ){
            log.error("ResultSet setResultsetPs3 error!",e );
        }
        return rs;

    }//ResultSet setResultsetPs3()



    /**生徒データ**/
    boolean printsvfDetail( DB2UDB db2, Vrw32alp svf, Param param, String subclasscd, PreparedStatement ps[] ){

        boolean nonedata = false;

        try {
log.debug("ps[1] start");
            ResultSet rs = setResultsetPs2( ps[1], subclasscd );
log.debug("ps[1] end");

            int attendno = 0;
            int s = 0;
            int i = 0;
            int j = 0;
            int m = 0;
            StringBuffer stb = new StringBuffer();
            while( rs.next() ){
                //出席番号のブレイク
                if( Integer.parseInt( rs.getString("ATTENDNO") )  !=  attendno ){
                    if( 0 < attendno )svf.VrsOutn( "views", i, stb.toString() );    //備考
                    if( s / maxline  !=  m ){
log.debug("i="+i+"  m="+m+"  s="+s);
                        svf.VrEndPage();
                        m = s / maxline;
                        if( svfprintSubclassHead( svf, param ) )nonedata = true;        //科目見出し出力
                    }
                    stb.delete( 0, stb.length() );
                    if( param._20 != null )s++;
                    else                   s = Integer.parseInt( rs.getString("ATTENDNO") );
                    attendno = Integer.parseInt( rs.getString("ATTENDNO") );
                    i = ( s % maxline == 0 )? maxline : s % maxline;
                    svf.VrsOutn( "NAME_SHOW", i, rs.getString("NAME") );            //生徒氏名
                    if( param._20 != null )svf.VrsOutn( "NUMBER",    i, String.valueOf( attendno ) );   //出席番号
                }
                j = ( Integer.parseInt( rs.getString("SEMESTER") ) != 9 )? Integer.parseInt( rs.getString("SEMESTER") ) : 4;
                if( rs.getString("VALUATION")  != null )svf.VrsOutn( "d" + j + "_1", i, rs.getString("VALUATION") );    //評定
                if( rs.getString("JISU")    != null  &&  Integer.parseInt( rs.getString("JISU") )    != 0 ) svf.VrsOutn( "d" + j + "_2", i, rs.getString("JISU")      );        //授業時数
                if( rs.getString("KEKKA")   != null  &&  Integer.parseInt( rs.getString("KEKKA") )   != 0 ) svf.VrsOutn( "d" + j + "_3", i, rs.getString("KEKKA")     );        //欠課
                if( rs.getString("SUSPEND") != null  &&  Integer.parseInt( rs.getString("SUSPEND") ) != 0 ) svf.VrsOutn( "d" + j + "_4", i, rs.getString("SUSPEND")   );        //出停
                if( rs.getString("MOUNING") != null  &&  Integer.parseInt( rs.getString("MOUNING") ) != 0 ) svf.VrsOutn( "d" + j + "_5", i, rs.getString("MOUNING")   );        //忌引
                if( rs.getString("LATE")    != null  &&  Integer.parseInt( rs.getString("LATE") )    != 0 ) svf.VrsOutn( "d" + j + "_6", i, rs.getString("LATE")      );        //遅刻回数
                nonedata = true;

                //学年末の処理
                if( j == 4 ){
                    if( param._6.equals("1") ){
                        //単位保留表示
                        if( rs.getString("VALUATION") != null  &&
                            Integer.parseInt( rs.getString("VALUATION") ) <= Integer.parseInt( param._13 ) )
                                stb.append( "*" );
                        //欠時表示
                        if( rs.getString("KEKKA") != null  &&  rs.getString("JISU") != null  &&
                            Integer.parseInt( rs.getString("JISU") ) / 3 < Integer.parseInt( rs.getString("KEKKA") ) )
                                stb.append( "#" );
                    }
                    if( rs.getString("REMARK") != null )stb.append( rs.getString("REMARK") );  //所見
                }
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("printsvfDetail error!",e );
        }

        return nonedata;

    }//printsvfDetailの括り



    /**学級・校内平均**/
    boolean printsvfTotal( DB2UDB db2, Vrw32alp svf, Param param, String subclasscd, PreparedStatement ps[], boolean nonedata ){

        ResultSet rs = null;
        int j = 0;

    //  学級平均
        try {
log.debug("ps[2] start");
            rs = setResultsetPs3( ps[2], subclasscd );
log.debug("ps[2] end");

            while( rs.next() ){
                j = ( Integer.parseInt( rs.getString("SEMESTER") ) != 9 )? Integer.parseInt( rs.getString("SEMESTER") ) : 4;
                svf.VrsOut( "g" + j + "_1", rs.getString("VALUATION") );        //評定
                svf.VrsOut( "g" + j + "_2", rs.getString("JISU")      );        //授業時数
                svf.VrsOut( "g" + j + "_3", rs.getString("KEKKA")     );        //欠課
                svf.VrsOut( "g" + j + "_4", rs.getString("SUSPEND")   );        //出停
                svf.VrsOut( "g" + j + "_5", rs.getString("MOUNING")   );        //忌引
                svf.VrsOut( "g" + j + "_6", rs.getString("LATE")      );        //遅刻
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("printsvfTotal hr_class error!",e );
        }//try-cathの括り

    //  校内平均
        try {
log.debug("ps[3] start");
            rs = setResultsetPs3( ps[3], subclasscd );
log.debug("ps[3] end");

            while( rs.next() ){
                j = ( Integer.parseInt( rs.getString("SEMESTER") ) != 9 )? Integer.parseInt( rs.getString("SEMESTER") ) : 4;
                svf.VrsOut( "k" + j + "_1", rs.getString("VALUATION") );        //評定
                svf.VrsOut( "k" + j + "_2", rs.getString("JISU")      );        //授業時数
                svf.VrsOut( "k" + j + "_3", rs.getString("KEKKA")     );        //欠課
                svf.VrsOut( "k" + j + "_4", rs.getString("SUSPEND")   );        //出停
                svf.VrsOut( "k" + j + "_5", rs.getString("MOUNING")   );        //忌引
                svf.VrsOut( "k" + j + "_6", rs.getString("LATE")      );        //遅刻
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("printsvfTotal school error!",e );
        }//try-cathの括り

        if( nonedata ) svf.VrEndPage();

        return nonedata;

    }//printsvfTotalの括り



    /**PrepareStatement作成**/
    String Pre_Stat_1(Param param)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(       "SELECT DISTINCT ");
            stb.append(           "T3.STAFFNAME,");
            stb.append(           "T5.CLASSNAME,");
            stb.append(           "T4.SUBCLASSNAME,");
            stb.append(           "T6.CREDIT ");
            stb.append(         "FROM ");
            stb.append(           "(");
            // 講座クラスデータの同時展開の講座コードゼロに対応
            stb.append(             "SELECT K2.YEAR,K2.SEMESTER,K2.CHAIRCD,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "K2.CLASSCD, ");
                stb.append(                    "K2.SCHOOL_KIND, ");
                stb.append(                    "K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(                    "K2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(                "FROM   CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(                "WHERE  K1.YEAR='"+param._0+"' AND K1.SEMESTER='"+param._1+"' AND ");
            stb.append(                  "K2.YEAR='"+param._0+"' AND K2.SEMESTER='"+param._1+"' AND ");
            stb.append(                  "K1.TRGTGRADE='"+param._2+"' AND K1.TRGTCLASS='"+param._3+"' AND ");
            stb.append(                  "(K1.CHAIRCD='0000000' OR K1.CHAIRCD=K2.CHAIRCD) AND ");
            stb.append(                  "K1.GROUPCD=K2.GROUPCD AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "K2.SUBCLASSCD=?");
            stb.append(           ")T1 ");
            stb.append(           "LEFT JOIN CHAIR_STF_DAT T2 ON T1.YEAR = T2.YEAR ");
            stb.append(                           "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                           "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(           "LEFT JOIN STAFF_MST T3 ON T2.STAFFCD = T3.STAFFCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(           "LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = T1.CLASSCD ");
                stb.append(           "                      AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append(           "LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            }
            stb.append(           "LEFT JOIN SUBCLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "   T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(           "LEFT JOIN (");
            stb.append(             "SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "W3.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(               "MAX(W3.CREDITS) AS CREDIT ");
            stb.append(             "FROM ");
            stb.append(               "SCHREG_REGD_DAT W1 ");
            stb.append(               "INNER JOIN CREDIT_MST W3 ON W1.YEAR = W3.YEAR ");
            stb.append(                       "AND W1.GRADE = W3.GRADE ");
            stb.append(                       "AND W1.COURSECD = W3.COURSECD ");
            stb.append(                       "AND W1.MAJORCD = W3.MAJORCD ");
            stb.append(                       "AND VALUE(W1.COURSECODE,'0000') = VALUE(W3.COURSECODE,'0000') ");
            stb.append(             "WHERE ");
            stb.append(                 "W1.GRADE = '" + param._2 + "' ");
            stb.append(               "AND W1.HR_CLASS = '" + param._3 + "' ");
            stb.append(               "AND W1.YEAR = '" + param._0 + "' ");
            stb.append(               "AND W1.SEMESTER = '" + param._1 + "' ");
            stb.append(               "AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "    W3.SUBCLASSCD =? ");
            stb.append(             "GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "W3.SUBCLASSCD ");
            stb.append(           ")T6 ON T6.SUBCLASSCD = T1.SUBCLASSCD");

        } catch (Exception e) {
            log.error("Pre_Stat_1 error!", e);
        }
        return stb.toString();

    }//Pre_Stat_1の括り



    /**PrepareStatement作成**/
    // 2012/05/01 KNJD070（近大用、廃盤）からコールされた場合動作する。教育課程コードの修正はなし
    String Pre_Stat_2(Param param) {

        StringBuffer stb = new StringBuffer();
        try {
                    //生徒情報
            stb.append("WITH SCHREG AS(");
            stb.append(        "SELECT W1.SCHREGNO,ATTENDNO,NAME ");
            stb.append(        "FROM   SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 ");
            stb.append(        "WHERE  W1.YEAR='"+param._0+"' AND W1.SEMESTER='"+param._1+"' AND ");
            stb.append(           "W1.GRADE='"+param._2+"' AND W1.HR_CLASS='"+param._3+"' AND W1.SCHREGNO=W2.SCHREGNO)");

            stb.append("SELECT T9.SCHREGNO,T9.ATTENDNO,T9.NAME,T9.SEMESTER,T1.VALUATION,");
            stb.append(         "T2.JISU,T2.KEKKA,T2.SUSPEND,T2.MOUNING,T2.LATE,T3.REMARK ");
            stb.append("FROM ( SELECT SCHREGNO,ATTENDNO,NAME,SEMESTER ");
            stb.append(         "FROM   SCHREG W1,SEMESTER_MST W2 ");
            stb.append(         "WHERE  W2.YEAR='"+param._0+"' )T9 ");
                //評定情報
            stb.append(  "LEFT JOIN (");
            stb.append(         "SELECT W1.SCHREGNO,W1.SEMESTER,W1.VALUATION ");
            stb.append(         "FROM   RECORDSEMES_DAT W1,SCHREG W2 ");
            stb.append(         "WHERE  YEAR='"+param._0+"' AND SEMESTER<='"+param._1+"' AND ");
            stb.append(              "GRADINGCLASSCD =? AND W1.SCHREGNO=W2.SCHREGNO ");
            stb.append(                              "AND VALUE(TESTKINDCD,'99') = '" + param._14.substring(0,2) + "' ");
            stb.append(         "UNION SELECT W1.SCHREGNO,'9' AS SEMESTER,W1.VALUATION ");
            stb.append(         "FROM   RECORDGRADE_DAT W1,SCHREG W2 ");
            stb.append(         "WHERE  YEAR='"+param._0+"' AND GRADINGCLASSCD=? AND W1.SCHREGNO=W2.SCHREGNO ");
            stb.append(       ")T1 ON T1.SCHREGNO=T9.SCHREGNO AND T1.SEMESTER=T9.SEMESTER ");
                //出欠情報 <T2>
            stb.append(  "LEFT JOIN (");
            stb.append(         "SELECT W4.SCHREGNO,VALUE(W4.SEMESTER,'9')AS SEMESTER,");
            stb.append(              "SUM(VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-SUSPEND-MOURNING)AS JISU,");
            stb.append(              "SUM(W4.KEKKA) AS KEKKA,SUM(W4.SUSPEND) AS SUSPEND,");
            stb.append(              "SUM(W4.MOURNING) AS MOUNING,SUM(W4.LATE) AS LATE ");
            stb.append(         "FROM ( SELECT S1.SCHREGNO,S1.SEMESTER,S1.SUBCLASSCD,");
            stb.append(                 "VALUE(SUM(S1.SICK),0)+VALUE(SUM(S1.NOTICE),0)+VALUE(SUM(S1.NONOTICE),0) + VALUE(SUM(S1.NURSEOFF),0) AS KEKKA,");
            stb.append(                 "VALUE(SUM(S1.SUSPEND),0) AS SUSPEND,");
            stb.append(                 "VALUE(SUM(S1.MOURNING),0) AS MOURNING,");
            stb.append(                 "VALUE(SUM(S1.LATE),0) AS LATE ");
            stb.append(              "FROM   ATTEND_SUBCLASS_DAT S1,SCHREG S2 ");
            stb.append(              "WHERE  S1.YEAR='"+param._0+"' AND S1.SEMESTER<='"+param._1+"' AND ");
            stb.append(                 "S1.SUBCLASSCD=? AND S1.SCHREGNO=S2.SCHREGNO ");
            stb.append(              "GROUP BY S1.SCHREGNO,S1.SEMESTER,S1.SUBCLASSCD ");
            stb.append(              ")W4 ");
            stb.append(              "INNER JOIN SCHREG_REGD_DAT W1 ON W1.SCHREGNO=W4.SCHREGNO AND ");
            stb.append(                   "W1.SEMESTER=W4.SEMESTER AND W1.YEAR='"+param._0+"' ");
            stb.append(              "INNER JOIN SCHREG W5 ON W5.SCHREGNO=W1.SCHREGNO ");
            stb.append(              "INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR=W2.YEAR AND W1.SEMESTER=W2.SEMESTER AND ");
            stb.append(                   "W1.GRADE= W2.GRADE AND W1.HR_CLASS=W2.HR_CLASS ");
            stb.append(              "LEFT JOIN CREDIT_MST W3 ON W1.YEAR=W3.YEAR AND W1.GRADE=W3.GRADE AND ");
            stb.append(                   "W1.COURSECD=W3.COURSECD AND W1.MAJORCD=W3.MAJORCD AND W4.SUBCLASSCD=W3.SUBCLASSCD AND ");
            stb.append(                   "VALUE(W1.COURSECODE,'0000')=VALUE(W3.COURSECODE,'0000') ");
            stb.append(           "GROUP BY GROUPING SETS ((W4.SCHREGNO,W4.SEMESTER),(W4.SCHREGNO)) ");
            stb.append(       ")T2 ON T2.SCHREGNO=T9.SCHREGNO AND T2.SEMESTER=T9.SEMESTER ");
                //備考
            stb.append( "LEFT JOIN STUDYCLASSREMARK_DAT T3 ON T3.YEAR='"+param._0+"' AND ");
            stb.append(                 "T3.SUBCLASSCD=? AND T3.SCHREGNO=T9.SCHREGNO ");
            stb.append("ORDER BY ATTENDNO,SEMESTER");

        } catch( Exception e ){
            log.error("Pre_Stat_2 error!", e);
        }
        return stb.toString();

    }//Pre_Stat_2の括り



    /**
     *  PrepareStatement作成
     */
    // 2012/05/01 KNJD070（近大用、廃盤）からコールされた場合動作する。教育課程コードの修正はなし
    String Pre_Stat_3(Param param, int ptype ){

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT T9.SEMESTER,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T1.VALUATION,5,2),1),5,1) AS VALUATION,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T2.JISU,5,2),1),5,1) AS JISU,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T2.KEKKA,5,2),1),5,1) AS KEKKA,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T2.SUSPEND,5,2),1),5,1) AS SUSPEND,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T2.MOUNING,5,2),1),5,1) AS MOUNING,");
            stb.append(         "DECIMAL(ROUND(DECIMAL(T2.LATE,5,2),1),5,1) AS LATE ");
            stb.append("FROM(  SELECT SEMESTER FROM SEMESTER_MST W2 WHERE W2.YEAR='"+param._0+"')T9 ");
                //評定情報
            stb.append(   "LEFT JOIN (");
            stb.append(         "SELECT W1.SEMESTER,AVG(FLOAT(W1.VALUATION)) AS VALUATION ");
            stb.append(         "FROM   RECORDSEMES_DAT W1,SCHREG_REGD_DAT W2 ");
            stb.append(         "WHERE  W1.YEAR='"+param._0+"' AND W1.SEMESTER<='"+param._1+"' AND ");
            stb.append(              "W1.GRADINGCLASSCD=? AND W2.YEAR='"+param._0+"' AND ");
            stb.append(                              "VALUE(TESTKINDCD,'99') = '" + param._14.substring(0,2) + "' AND ");
            stb.append(              "W2.SEMESTER='"+param._1+"' AND W2.GRADE='"+param._2+"' AND ");
            if (ptype == 0)
              stb.append(         "W2.HR_CLASS='"+param._3+"' AND ");
            stb.append(           "W1.SCHREGNO=W2.SCHREGNO ");
            stb.append(           "GROUP BY W1.SEMESTER ");
            stb.append(         "UNION ");
            stb.append(         "SELECT '9' AS SEMESTER,AVG(FLOAT(W1.VALUATION)) AS VALUATION ");
            stb.append(         "FROM   RECORDGRADE_DAT W1,SCHREG_REGD_DAT W2 ");
            stb.append(         "WHERE  W1.YEAR='"+param._0+"' AND W1.GRADINGCLASSCD=? AND ");
            stb.append(              "W2.YEAR='"+param._0+"' AND W2.SEMESTER='"+param._1+"' AND ");
            stb.append(              "W2.GRADE='"+param._2+"' AND ");
            if (ptype == 0)
              stb.append(         "W2.HR_CLASS='"+param._3+"' AND ");
            stb.append(         "W1.SCHREGNO=W2.SCHREGNO ");
            stb.append(         "GROUP BY SEMESTER ");
            stb.append(       ")T1 ON T1.SEMESTER=T9.SEMESTER ");

                //出欠情報 <T2>
            stb.append(  "LEFT JOIN (");
            stb.append(         "SELECT SEMESTER,AVG(FLOAT(JISU)) AS JISU,AVG(FLOAT(KEKKA))AS KEKKA,");
            stb.append(              "AVG(FLOAT(SUSPEND)) AS SUSPEND,AVG(FLOAT(MOUNING))AS MOUNING,");
            stb.append(              "AVG(FLOAT(LATE)) AS LATE ");
            stb.append(         "FROM ( SELECT W4.SCHREGNO,VALUE(W4.SEMESTER,'9')AS SEMESTER,");
            stb.append(                 "SUM(VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-SUSPEND-MOURNING)AS JISU,");
            stb.append(                 "SUM(W4.KEKKA) AS KEKKA,SUM(W4.SUSPEND) AS SUSPEND,");
            stb.append(                 "SUM(W4.MOURNING) AS MOUNING,SUM(W4.LATE) AS LATE ");
            stb.append(              "FROM ( SELECT S1.SCHREGNO,S1.SUBCLASSCD,S1.SEMESTER,");
            stb.append(                    "VALUE(SUM(S1.SICK),0)+VALUE(SUM(S1.NOTICE),0)+VALUE(SUM(S1.NONOTICE),0)+VALUE(SUM(S1.NURSEOFF),0)AS KEKKA,");
            stb.append(                    "VALUE(SUM(S1.SUSPEND),0) AS SUSPEND,");
            stb.append(                    "VALUE(SUM(S1.MOURNING),0) AS MOURNING,");
            stb.append(                    "VALUE(SUM(S1.LATE),0) AS LATE ");
            stb.append(                 "FROM   ATTEND_SUBCLASS_DAT S1,SCHREG_REGD_DAT S2 ");
            stb.append(                 "WHERE  S1.YEAR='"+param._0+"' AND S1.SEMESTER<='"+param._1+"' AND ");
            stb.append(                    "S1.SUBCLASSCD =? AND S2.YEAR='"+param._0+"' AND ");
            stb.append(                    "S2.SEMESTER='"+param._1+"' AND S2.GRADE='"+param._2+"' AND ");
            if (ptype == 0)
              stb.append(               "S2.HR_CLASS='"+param._3+"' AND ");
            stb.append(                 "S1.SCHREGNO=S2.SCHREGNO ");
            stb.append(                 "GROUP BY S1.SCHREGNO,S1.SEMESTER,S1.SUBCLASSCD ");
            stb.append(                 ")W4 ");
            stb.append(                 "INNER JOIN SCHREG_REGD_DAT W1 ON W1.SCHREGNO=W4.SCHREGNO AND ");
            stb.append(                   "W1.SEMESTER=W4.SEMESTER AND W1.YEAR='"+param._0+ "' ");
            stb.append(                 "INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR=W2.YEAR AND ");
            stb.append(                   "W1.SEMESTER=W2.SEMESTER AND W1.GRADE=W2.GRADE AND ");
            stb.append(                   "W1.HR_CLASS=W2.HR_CLASS ");
            stb.append(                 "LEFT JOIN CREDIT_MST W3 ON W1.YEAR=W3.YEAR AND W1.GRADE=W3.GRADE AND ");
            stb.append(                   "W1.COURSECD=W3.COURSECD AND W1.MAJORCD=W3.MAJORCD AND ");
            stb.append(                   "W4.SUBCLASSCD=W3.SUBCLASSCD AND VALUE(W1.COURSECODE,'0000')=VALUE(W3.COURSECODE,'0000') ");
            stb.append(              "GROUP BY GROUPING SETS ((W4.SCHREGNO,W4.SEMESTER),(W4.SCHREGNO)) ");
            stb.append(         ")S1 ");
            stb.append(       "GROUP BY SEMESTER ");
            stb.append(     ")T2 ON T2.SEMESTER=T9.SEMESTER ");
            stb.append(   "ORDER BY T9.SEMESTER");

        } catch (Exception e) {
            log.error("Pre_Stat_3 error!", e);
        }
        return stb.toString();

    }//Pre_Stat_3の括り


    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    protected static class Param {
        final String _0;
        final String _1;
        final String _2;
        final String _3;
        String _4;
        String _5;
        final String _6;
        String _7;
        String _8;
        String _9;
        String _10;
        String _11;
        String _12;
        String _13;
        final String _14;
        String _15;
        String _16;
        String _17;
        String _18;
        String _19;
        String _20;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _0 = request.getParameter("YEAR");                            // 年度;
            _1 = request.getParameter("GAKKI");                           // 1-3:学期;
            //  '学年＋組'パラメータを分解
            String strx = request.getParameter("GRADE_HR_CLASS");               //学年＋組
            KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();           //クラスのインスタンス作成
            KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(strx);
            _2 = returnval.val1;                                          //学年
            _3 = returnval.val2;                                          //組
            _6 = ( request.getParameter("HORYU") != null )? "1" : "0";    // 単位保留表示 on:出力;
            //_14 = request.getParameter("TEST");                             //成績種別 中間：0100 期末：0200 学期：9900  04/11/12Add
            _14 = "99";
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        }
    }

}

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Assess;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＪＮＤ０７０Ｋ＞  学習記録報告書
 *
 *	2004/07/29 yamashiro・成績データは'KIN_RECORD_DAT'を使用
 *  2004/11/12 yamashiro・授業時数をATTEND_SUBCLASS_DATから出力( <= 以前は単位×週数 )
 *>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

public class KNJD070K {

    private static Log log = LogFactory.getLog("KNJD070K");
    
    Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                     //Databaseクラスを継承したクラス
    boolean nonedata,nonedata2;     //該当データなしフラグ
    private String sql1, sql2, sql3, sql4;
    PreparedStatement ps1,ps2,ps3,ps4;
    String param[];
    private String _useCurriculumcd;
    private String _useVirus;
    private String _useKoudome;


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        String s_class[]  = request.getParameterValues("SUBCLASS_SELECTED");    // 科目
        param = new String[14];

    // パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                            // 年度
            param[1] = request.getParameter("GAKKI");                           // 1-3:学期
            param[6] = "0";                                                     // 単位保留表示 on:出力
            if(request.getParameter("HORYU") != null)   param[6]="1";

        //  '学年＋組'パラメータを分解
            String strx = request.getParameter("GRADE_HR_CLASS");               //学年＋組
            KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();           //クラスのインスタンス作成
            KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(strx);
            param[2] = returnval.val1;                                          //学年
            param[3] = returnval.val2;                                          //組
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        } catch( Exception ex ) {
            log.error("[KNJD070]parameter error!", ex);
        }

    //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                          //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJD070]DB2 open error!", ex);
        }


    //  ＳＶＦ作成処理       
        Pre_Stat_1();                                   //科目見出し
        Pre_Stat_2();                                   //評定・出欠データ
        Pre_Stat_3(0);                                  //学級平均
        Pre_Stat_3(1);                                  //校内平均
        Set_Head();                                     //見出し出力
for(int ia=0 ; ia<param.length ; ia++)log.debug("[KNJD070]param[" + ia + "]=" + param[ia]);

        for( int ia=0 ; ia<s_class.length ; ia++ ){
            Set_Detail_1(s_class[ia]);                  //教科見出し出力
            Set_Detail_2(s_class[ia]);                  //評定・出欠データ出力
            Set_Detail_3(s_class[ia]);                  //学級・校内平均出力
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        Pre_Stat_F();       //preparestatement
        db2.commit();
        db2.close();        //DBを閉じる
        outstrm.close();    //ストリームを閉じる 

    }//doGetの括り



    /**ページ見出し・初期設定**/
    private void Set_Head(){

        svf.VrSetForm("KNJD070.frm", 1);
    //  作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control();
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            param[4] = returnval.val3;          //現在処理日
        } catch( Exception e ){
            log.error("[KNJD070]Set_Head today get error!", e);
        }
    //  組名称及び担任名の取得
        try {
            KNJ_Grade_Hrclass hrclass_staff = new KNJ_Grade_Hrclass();
            KNJ_Grade_Hrclass.ReturnVal returnval = 
                        hrclass_staff.Hrclass_Staff(db2,param[0],param[1],param[2],param[3]);
            param[5] = returnval.val1;          //組名称
            param[7] = returnval.val3;          //担任名
        } catch( Exception e ){
            log.error("[KNJD070]Set_Head hrclass_staff error!", e);
        }
    //  学期名称の取得
        try {
            KNJ_Semester semester = new KNJ_Semester();
            KNJ_Semester.ReturnVal returnval = semester.Semester_T(db2,param[0]);
            StringTokenizer scode = new StringTokenizer(returnval.val1, ",",true);  //学期コード
            StringTokenizer sname = new StringTokenizer(returnval.val2, ",",true);  //学期名称
            for( ; ; ){
                if( !scode.hasMoreTokens() )    break;      //学期コード
                String sia = scode.nextToken();
                if( !sname.hasMoreTokens() )    continue;   //学期名称
                String strx = sname.nextToken();
                if( sia.equals("1") )   param[8] = strx;
                if( sia.equals("2") )   param[9] = strx;
                if( sia.equals("3") )   param[10] = strx;
                if( sia.equals("9") )   param[11] = strx;
            }
        } catch( Exception e ){
            log.error("[KNJD070]Set_Head Semester name get error!", e);
        }
    //  単位保留値の取得
        try {
            KNJ_Assess assess = new KNJ_Assess();
            KNJ_Assess.ReturnVal returnval = 
                        assess.FearvalInfo(db2,param[0]);
            param[12] = returnval.val1;     //学期保留値
            param[13] = returnval.val2;     //学年保留値
        } catch( Exception e ){
            log.error("[KNJD070]Set_Head assess error!", e);
        }

    }//Set_Headの括り



    /**科目見出し**/
    private void Set_Detail_1(String subclasscd){

        nonedata2 = false;
        Svf_Field_Set_2("nendo",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])));    //年度
        Svf_Field_Set_2("TODAY"     ,KNJ_EditDate.h_format_JP(param[4]));   //現在処理日
        Svf_Field_Set_2("HR_NAME"       ,param[5]);         //組名称
        Svf_Field_Set_2("STAFFNAME2"    ,param[7]);         //担任名
        Svf_Field_Set_2("term1"     ,param[8]);         //学期名称
        Svf_Field_Set_2("term2"     ,param[9]);         //学期名称
        Svf_Field_Set_2("term3"     ,param[10]);        //学期名称
        Svf_Field_Set_2("term4"     ,param[11]);        //学期名称

        try {
            ps1.setString(1,subclasscd);
            ps1.setString(2,subclasscd);
            ResultSet rs = ps1.executeQuery();
//System.out.println("[KNJD070]Set_Detail_1 subclasscd = "+subclasscd);
//System.out.println("[KNJD070]Set_Detail_1 sql ok! sql = "+String.valueOf(ps1));

            boolean first = false;
            String staffname = new String();
            staffname = "";
            while( rs.next() ){
                if( first == false){
                    Svf_Field_Set_2("tani"      , rs.getString("CREDIT"));
                    Svf_Field_Set_2("MAJORNMAE" , rs.getString("CLASSNAME"));
                    Svf_Field_Set_2("CLASSNAME" , rs.getString("SUBCLASSNAME"));
                    first = true;
                }
                if( rs.getString("STAFFNAME")!=null )
                    if( staffname.length() == 0 )
                        staffname = rs.getString("STAFFNAME");
                    else
                        staffname = staffname + "," + rs.getString("STAFFNAME");
            }
            db2.commit();
            rs.close();
            Svf_Field_Set_2("STAFFNAME" ,staffname);                //科目担任名
        } catch( Exception e ){
            log.error("[KNJD070]Set_Detail_1 error!", e);
        }//try-cathの括り

    }//Set_Detail_1の括り

    /**生徒データ**/
    void Set_Detail_2(String subclasscd){

        try {
            int pp = 0;
            ps2.setString(++pp,subclasscd);
            ps2.setString(++pp,subclasscd);
            ps2.setString(++pp,subclasscd);
            ps2.setString(++pp,subclasscd);
            ps2.setString(++pp,subclasscd);
            ps2.setString(++pp,subclasscd);
            ResultSet rs = ps2.executeQuery();

//System.out.println("[KNJD070]Set_Detail_2 sql ok!");
//System.out.println("[KNJD070]Set_Detail_2 sql ="+String.valueOf(ps2));

            int ia = 0;
            int ano = 0;
            int sem = 0;
            String view = new String();
            while( rs.next() ){
            //  出席番号のブレイク
                ano = rs.getInt("ATTENDNO");
                if( ia!=ano ){
                    if( ia>0 )  Svf_Field_Set_1("views" ,ia ,view);             //備考
                    ia = ano;
                    if( ia>60 ) break;
                    Svf_Field_Set_1("NAME_SHOW" ,ia ,rs.getString("NAME"));     //生徒氏名
                    view = "";
                }
            //  生徒データ出力
                sem = rs.getInt("SEMESTER");
                if( sem==9 )    sem=4;
                Svf_Field_Set_1("d"+sem+"_1" ,ia ,rs.getString("VALUATION"));       //評定
                Svf_Field_Set_1("d"+sem+"_2" ,ia ,rs.getString("JISU"));            //授業時数
                Svf_Field_Set_1("d"+sem+"_3" ,ia ,rs.getString("KEKKA"));           //欠課
                Svf_Field_Set_1("d"+sem+"_4" ,ia ,rs.getString("SUSPEND"));     //出停
                Svf_Field_Set_1("d"+sem+"_5" ,ia ,rs.getString("MOUNING"));     //忌引
                Svf_Field_Set_1("d"+sem+"_6" ,ia ,rs.getString("LATE"));            //遅刻回数
                nonedata2 = true;
            //  所見
                if( sem==4 ){
                //  単位保留表示
                    if( param[6].equals("1") ){
                        //int horyu = Integer.parseInt(param[13]);                  //学年末の単位保留値
                        if( rs.getString("VALUATION")!=null ){
                            int valuation = rs.getInt("VALUATION");
                            if( valuation<=Integer.parseInt(param[13])) view = "*";
                        }
                        if( rs.getString("KEKKA")!=null & rs.getString("JISU")!=null ){
                            //int kekka = rs.getInt("KEKKA");
                            //int jisu = rs.getInt("JISU");
                            if( rs.getInt("KEKKA")>(rs.getInt("JISU")/3) )  view = view + "#";
                        }
                    }
                    if( rs.getString("REMARK")!=null )  view = view + rs.getString("REMARK");
                }
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("[KNJD070]Set_Detail_2 error!", e);
        }//try-cathの括り

    }//Set_Detail_2の括り



    /**学級・校内平均**/
    void Set_Detail_3(String subclasscd){

        ResultSet rs = null;
        String semes = null;

    //  学級平均
        try {
            int pp = 0;
            ps3.setString(++pp,subclasscd);
            ps3.setString(++pp,subclasscd);
            ps3.setString(++pp,subclasscd);
            ps3.setString(++pp,subclasscd);
            ps3.setString(++pp,subclasscd);
            rs = ps3.executeQuery();
//System.out.println("[KNJD070]Set_Detail_3 hr_class sql ok!");
//System.out.println("[KNJD070]Set_Detail_3 hr_class sql =" + String.valueOf(ps3));

            while( rs.next() ){
                semes = rs.getString("SEMESTER");
                Svf_Field_Set_2("g"+semes+"_1", rs.getString("VALUATION")); //評定
                Svf_Field_Set_2("g"+semes+"_2", rs.getString("JISU"));      //授業時数
                Svf_Field_Set_2("g"+semes+"_3", rs.getString("KEKKA"));     //欠課
                Svf_Field_Set_2("g"+semes+"_4", rs.getString("SUSPEND"));       //出停
                Svf_Field_Set_2("g"+semes+"_5", rs.getString("MOUNING"));       //忌引
                Svf_Field_Set_2("g"+semes+"_6", rs.getString("LATE"));      //遅刻
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("[KNJD070]Set_Detail_3 hr_class error!", e);
        }//try-cathの括り

    //  校内平均
        try {
            int pp = 0;
            ps4.setString(++pp,subclasscd);
            ps4.setString(++pp,subclasscd);
            ps4.setString(++pp,subclasscd);
            ps4.setString(++pp,subclasscd);
            ps4.setString(++pp,subclasscd);
            rs = ps4.executeQuery();
//System.out.println("[KNJD070]Set_Detail_3 school sql ok!");

            while( rs.next() ){
                semes = rs.getString("SEMESTER");
                Svf_Field_Set_2("k"+semes+"_1", rs.getString("VALUATION")); //評定
                Svf_Field_Set_2("k"+semes+"_2", rs.getString("JISU"));      //授業時数
                Svf_Field_Set_2("k"+semes+"_3", rs.getString("KEKKA"));     //欠課
                Svf_Field_Set_2("k"+semes+"_4", rs.getString("SUSPEND"));       //出停
                Svf_Field_Set_2("k"+semes+"_5", rs.getString("MOUNING"));       //忌引
                Svf_Field_Set_2("k"+semes+"_6", rs.getString("LATE"));      //遅刻
            }
            db2.commit();
            rs.close();
        } catch( Exception e ){
            log.error("[KNJD070]Set_Detail_3 school error!", e);
        }//try-cathの括り

        if( nonedata2 ==true ){
            svf.VrEndPage();
            nonedata = true;
        }

    }//Set_Detail_3の括り



    /**PrepareStatement作成**/
    private void Pre_Stat_1()
    {
        try {
            String sql = new String();
            sql = "SELECT DISTINCT "
                    + "T3.STAFFNAME,"
                    + "T5.CLASSNAME,"
                    + "T4.SUBCLASSNAME,"
                    + "T6.CREDIT "
                + "FROM "
                    + "("
                    //  04/08/18Modify-->講座クラスデータの同時展開の講座コードゼロに対応
                        + "SELECT K2.YEAR,K2.SEMESTER,K2.CHAIRCD,";
            if ("1".equals(_useCurriculumcd)) {
                sql += " K2.CLASSCD, ";
                sql += " K2.SCHOOL_KIND, ";
                sql += " K2.CURRICULUM_CD, ";
                sql += " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ";
            }
            sql += ""
                        + "K2.SUBCLASSCD AS SUBCLASSCD "
                        + "FROM   CHAIR_CLS_DAT K1,CHAIR_DAT K2 "
                        + "WHERE  K1.YEAR='"+param[0]+"' AND K1.SEMESTER='"+param[1]+"' AND "
                               + "K2.YEAR='"+param[0]+"' AND K2.SEMESTER='"+param[1]+"' AND "
                               + "K1.TRGTGRADE='"+param[2]+"' AND K1.TRGTCLASS='"+param[3]+"' AND "
                               + "(K1.CHAIRCD='0000000' OR K1.CHAIRCD=K2.CHAIRCD) AND "
                               + "K1.GROUPCD=K2.GROUPCD AND ";
             if ("1".equals(_useCurriculumcd)) {
                 sql += " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ";
             }
             sql += ""
                               + " K2.SUBCLASSCD=?"
/* ******************** + "SELECT "
                            + "W1.YEAR,"
                            + "W2.SEMESTER,"
                            + "W1.CHAIRCD,"
                            + "W2.SUBCLASSCD "
                        + "FROM "
                            + "CHAIR_CLS_DAT W1,"
                            + "CHAIR_DAT W2 "
                        + "WHERE "
                                + "W1.YEAR = '" + param[0] + "' "
                            + "AND W1.SEMESTER = '" + param[1] + "' "
                            + "AND W1.TRGTGRADE = '" + param[2] + "' "
                            + "AND W1.TRGTCLASS = '" + param[3] + "' "
                            + "AND W2.SUBCLASSCD =? "
                            + "AND W1.YEAR = W2.YEAR "
                            + "AND W1.SEMESTER = W2.SEMESTER "
                            + "AND W1.CHAIRCD = W2.CHAIRCD "*************************** */
                    + ")T1 "
                    + "LEFT JOIN CHAIR_STF_DAT T2 ON T1.YEAR = T2.YEAR "
                                                    + "AND T1.SEMESTER = T2.SEMESTER "
                                                    + "AND T1.CHAIRCD = T2.CHAIRCD "
                    + "LEFT JOIN STAFF_MST T3 ON T2.STAFFCD = T3.STAFFCD ";

            if ("1".equals(_useCurriculumcd)) {
                sql += ""
                    + "LEFT JOIN CLASS_MST T5 ON T5.CLASSCD || '-' || T5.SCHOOL_KIND = "
                    +                          " T1.CLASSCD || '-' || T1.SCHOOL_KIND  "
                    + "LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T1.SUBCLASSCD ";
            } else {
                sql += ""
                    + "LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) "
                    + "LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
            }
            sql += ""
                    + "LEFT JOIN ("
                        + "SELECT DISTINCT ";
            if ("1".equals(_useCurriculumcd)) {
                sql += " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ";
            }            
            sql += ""
                            + "W3.SUBCLASSCD AS SUBCLASSCD,"
                            + "MAX(W3.CREDITS) AS CREDIT "
                        + "FROM "
                            + "SCHREG_REGD_DAT W1 "
                            + "INNER JOIN CREDIT_MST W3 ON W1.YEAR = W3.YEAR "
                                            + "AND W1.GRADE = W3.GRADE "
                                            + "AND W1.COURSECD = W3.COURSECD "
                                            + "AND W1.MAJORCD = W3.MAJORCD "
                                            + "AND VALUE(W1.COURSECODE,'0000') = VALUE(W3.COURSECODE,'0000') "
                        + "WHERE "
                                + "W1.GRADE = '" + param[2] + "' "
                            + "AND W1.HR_CLASS = '" + param[3] + "' "
                            + "AND W1.YEAR = '" + param[0] + "' "
                            + "AND W1.SEMESTER = '" + param[1] + "' "
                            + "AND ";
            if ("1".equals(_useCurriculumcd)) {
                sql += " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ";
            }
            sql += ""
                            + "    W3.SUBCLASSCD =? "
                        + "GROUP BY ";
            if ("1".equals(_useCurriculumcd)) {
                sql += " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ";
            }
            sql += ""
                            + "W3.SUBCLASSCD "
                    + ")T6 ON T6.SUBCLASSCD = T1.SUBCLASSCD";
            sql1 = sql;
            ps1 = db2.prepareStatement(sql1);
        } catch( Exception e ){
            log.error("[KNJD070]Pre_Stat_1 error!", e);
        }

    }//Pre_Stat_1の括り



    /**PrepareStatement作成**/
    /**PrepareStatement作成**/
    void Pre_Stat_2(){

        try {
            StringBuffer stb = new StringBuffer();
                    //生徒情報
            stb.append("WITH SCHREG AS("
                         + "SELECT W1.SCHREGNO,ATTENDNO,NAME "
                         + "FROM   SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 "
                         + "WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND "
                                + "W1.GRADE='"+param[2]+"' AND W1.HR_CLASS='"+param[3]+"' AND W1.SCHREGNO=W2.SCHREGNO)");

            stb.append("SELECT T9.SCHREGNO,T9.ATTENDNO,T9.NAME,T9.SEMESTER,T1.VALUATION,"
                            + "T2.JISU,T2.KEKKA,T2.SUSPEND,T2.MOUNING,T2.LATE,T3.REMARK ");
            stb.append("FROM ( SELECT SCHREGNO,ATTENDNO,NAME,SEMESTER "
                            + "FROM   SCHREG W1,SEMESTER_MST W2 "
                            + "WHERE  W2.YEAR='"+param[0]+"' )T9 ");
                    //評定情報
            stb.append( "LEFT JOIN ("
                            + "SELECT W1.SCHREGNO,'1' AS SEMESTER,SEM1_REC AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "SEM1_REC IS NOT NULL AND SEM1_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG W2 WHERE W1.SCHREGNO=W2.SCHREGNO) "
                            + "UNION SELECT W1.SCHREGNO,'2' AS SEMESTER,SEM2_REC AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "SEM2_REC IS NOT NULL AND SEM2_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG W2 WHERE W1.SCHREGNO=W2.SCHREGNO) "
                            + "UNION SELECT W1.SCHREGNO,'3' AS SEMESTER,SEM3_REC AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "SEM3_REC IS NOT NULL AND SEM3_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG W2 WHERE W1.SCHREGNO=W2.SCHREGNO) "
                            + "UNION SELECT W1.SCHREGNO,'9' AS SEMESTER,"
                            + "CASE JUDGE_PATTERN WHEN 'A' THEN INT(A_PATTERN_ASSESS) "
                                                + "WHEN 'B' THEN INT(B_PATTERN_ASSESS) "
                                                + "WHEN 'C' THEN INT(C_PATTERN_ASSESS) "
                                                + "ELSE NULL END AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS "
                                                + "WHEN 'B' THEN B_PATTERN_ASSESS "
                                                + "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG W2 WHERE W1.SCHREGNO=W2.SCHREGNO) "
                      + ")T1 ON T1.SCHREGNO=T9.SCHREGNO AND T1.SEMESTER=T9.SEMESTER ");
                    //出欠情報 <T2>
            stb.append( "LEFT JOIN ("
                            + "SELECT W4.SCHREGNO,VALUE(W4.SEMESTER,'9')AS SEMESTER,"
                                   //+ "CASE WHEN SUM(VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-SUSPEND-MOURNING)>0 "
                                   //   + "THEN SUM(VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-SUSPEND-MOURNING) "
                                   //       + "ELSE 0 END AS JISU,"
                                   + "SUM(W4.LESSON-W4.SUSPEND-W4.MOURNING) AS JISU,"   // 04/11/12Modify
                                   + "SUM(W4.KEKKA) AS KEKKA,SUM(W4.SUSPEND) AS SUSPEND,"
                                   + "SUM(W4.MOURNING) AS MOUNING,SUM(W4.LATE) AS LATE "
                            + "FROM ( SELECT S1.SCHREGNO,S1.SEMESTER,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "S1.SUBCLASSCD AS SUBCLASSCD,"
                                          + "SUM(S1.LESSON)AS LESSON,"    // 04/11/12Add
                                          + "SUM(S1.SICK)+SUM(S1.NOTICE)+SUM(S1.NONOTICE)+SUM(S1.NURSEOFF) AS KEKKA,"
                                          + "SUM(S1.SUSPEND) ");
            if ("true".equals(_useVirus)) {
                stb.append(              " + SUM(S1.VIRUS) ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(              " + SUM(S1.KOUDOME) ");
            }
            stb.append(                     " AS SUSPEND,");

            stb.append(                     "SUM(S1.MOURNING) AS MOURNING,"
                                          + "SUM(S1.LATE) AS LATE "
                                   + "FROM   ATTEND_SUBCLASS_DAT S1,SCHREG S2 "
                                   + "WHERE  S1.YEAR='"+param[0]+"' AND S1.SEMESTER<='"+param[1]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                     "S1.SUBCLASSCD=? AND S1.SCHREGNO=S2.SCHREGNO "
                                   + "GROUP BY S1.SCHREGNO,S1.SEMESTER,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                       "S1.SUBCLASSCD "
                                 + ")W4 "
                                 + "INNER JOIN SCHREG_REGD_DAT W1 ON W1.SCHREGNO=W4.SCHREGNO AND "
                                                + "W1.SEMESTER=W4.SEMESTER AND W1.YEAR='"+param[0]+"' "
                                 + "INNER JOIN SCHREG W5 ON W5.SCHREGNO=W1.SCHREGNO "
                                 + "INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR=W2.YEAR AND W1.SEMESTER=W2.SEMESTER AND "
                                                + "W1.GRADE= W2.GRADE AND W1.HR_CLASS=W2.HR_CLASS "
                                 + "LEFT JOIN CREDIT_MST W3 ON W1.YEAR=W3.YEAR AND W1.GRADE=W3.GRADE AND "
                                                + "W1.COURSECD=W3.COURSECD AND W1.MAJORCD=W3.MAJORCD AND W4.SUBCLASSCD=");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           "W3.SUBCLASSCD AND "
                                                + "VALUE(W1.COURSECODE,'0000')=VALUE(W3.COURSECODE,'0000') "
                            + "GROUP BY GROUPING SETS ((W4.SCHREGNO,W4.SEMESTER),(W4.SCHREGNO)) "
                      + ")T2 ON T2.SCHREGNO=T9.SCHREGNO AND T2.SEMESTER=T9.SEMESTER ");
                    //備考
            stb.append( "LEFT JOIN STUDYCLASSREMARK_DAT T3 ON T3.YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           "T3.SUBCLASSCD=? AND T3.SCHREGNO=T9.SCHREGNO ");
            stb.append("ORDER BY ATTENDNO,SEMESTER");

            sql2 = stb.toString();
            ps2 = db2.prepareStatement(sql2);
log.debug("[KNJD070K]Pre_Stat_2 ps2 OK!");
//System.out.println("[KNJD070K]Pre_Stat_2 ps2="+ps2.toString());
        } catch( Exception e ){
            log.error("[KNJD070K]Pre_Stat_2 error!", e);
        }

    }//Pre_Stat_2の括り

    /**PrepareStatement作成**/
    void Pre_Stat_3(int ptype){

        try {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT T9.SEMESTER,"
                            + "DECIMAL(ROUND(DECIMAL(T1.VALUATION,5,2),1),5,1) AS VALUATION,"
                            + "DECIMAL(ROUND(DECIMAL(T2.JISU,5,2),1),5,1) AS JISU,"
                            + "DECIMAL(ROUND(DECIMAL(T2.KEKKA,5,2),1),5,1) AS KEKKA,"
                            + "DECIMAL(ROUND(DECIMAL(T2.SUSPEND,5,2),1),5,1) AS SUSPEND,"
                            + "DECIMAL(ROUND(DECIMAL(T2.MOUNING,5,2),1),5,1) AS MOUNING,"
                            + "DECIMAL(ROUND(DECIMAL(T2.LATE,5,2),1),5,1) AS LATE ");
            stb.append("FROM(  SELECT SEMESTER FROM SEMESTER_MST W2 WHERE W2.YEAR='"+param[0]+"')T9 ");
                    //評定情報
            stb.append(  "LEFT JOIN ("
                            + "SELECT '1' AS SEMESTER,AVG(FLOAT(SEM1_REC))AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           "SUBCLASSCD=? AND "
                                   + "SEM1_REC IS NOT NULL AND SEM1_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT W2 "
                                          + "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='1' AND "
                                                 + "W2.GRADE='"+param[2]+"' AND ");
            if( ptype==0 )
                stb.append(                        "W2.HR_CLASS='"+param[3]+"' AND W1.SCHREGNO=W2.SCHREGNO)");
            else
                stb.append(                        "W1.SCHREGNO=W2.SCHREGNO)");
            stb.append(       "UNION SELECT '2' AS SEMESTER,AVG(FLOAT(SEM2_REC))AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "SEM2_REC IS NOT NULL AND SEM2_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT W2 "
                                          + "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='2' AND "
                                                 + "W2.GRADE='"+param[2]+"' AND ");
            if( ptype==0 )
                stb.append(                        "W2.HR_CLASS='"+param[3]+"' AND W1.SCHREGNO=W2.SCHREGNO)");
            else
                stb.append(                        "W1.SCHREGNO=W2.SCHREGNO)");
            stb.append(       "UNION SELECT '3' AS SEMESTER,AVG(FLOAT(SEM3_REC))AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "SEM3_REC IS NOT NULL AND SEM3_REC_FLG IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT W2 "
                                          + "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='3' AND "
                                                 + "W2.GRADE='"+param[2]+"' AND ");
            if( ptype==0 )
                stb.append(                        "W2.HR_CLASS='"+param[3]+"' AND W1.SCHREGNO=W2.SCHREGNO)");
            else
                stb.append(                        "W1.SCHREGNO=W2.SCHREGNO)");
            stb.append(       "UNION SELECT '9' AS SEMESTER,AVG(FLOAT(GRADE_RECORD))AS VALUATION "
                            + "FROM   KIN_RECORD_DAT W1 "
                            + "WHERE  YEAR='"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "SUBCLASSCD=? AND "
                                   + "GRADE_RECORD IS NOT NULL AND "
                                   + "EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT W2 "
                                          + "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND "
                                                 + "W2.GRADE='"+param[2]+"' AND ");
            if( ptype==0 )
                stb.append(                        "W2.HR_CLASS='"+param[3]+"' AND W1.SCHREGNO=W2.SCHREGNO)");
            else
                stb.append(                        "W1.SCHREGNO=W2.SCHREGNO)");
            stb.append(     ")T1 ON T1.SEMESTER=T9.SEMESTER ");

                    //出欠情報 <T2>
            stb.append(  "LEFT JOIN ("
                            + "SELECT SEMESTER,AVG(FLOAT(JISU)) AS JISU,AVG(FLOAT(KEKKA))AS KEKKA,"
                                   + "AVG(FLOAT(SUSPEND)) AS SUSPEND,AVG(FLOAT(MOUNING))AS MOUNING,"
                                   + "AVG(FLOAT(LATE)) AS LATE "
                            + "FROM ( SELECT W4.SCHREGNO,VALUE(W4.SEMESTER,'9')AS SEMESTER,"
                                          + "SUM(VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-SUSPEND-MOURNING)AS JISU,"
                                          + "SUM(W4.KEKKA) AS KEKKA,SUM(W4.SUSPEND) AS SUSPEND,"
                                          + "SUM(W4.MOURNING) AS MOUNING,SUM(W4.LATE) AS LATE "
                                   + "FROM ( SELECT S1.SCHREGNO,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           " S1.SUBCLASSCD AS SUBCLASSCD,S1.SEMESTER,"
                                                 + "VALUE(SUM(S1.SICK),0)+VALUE(SUM(S1.NOTICE),0)+VALUE(SUM(S1.NONOTICE),0)+VALUE(SUM(S1.NURSEOFF),0)AS KEKKA,"
                                                 + "VALUE(SUM(S1.SUSPEND),0) ");
            if ("true".equals(_useVirus)) {
                stb.append(                           " + VALUE(SUM(S1.VIRUS), 0) ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(                           " + VALUE(SUM(S1.KOUDOME), 0) ");
            }
            stb.append(                            "  AS SUSPEND,");
            stb.append(                            "VALUE(SUM(S1.MOURNING),0) AS MOURNING,"
                                                 + "VALUE(SUM(S1.LATE),0) AS LATE "
                                          + "FROM   ATTEND_SUBCLASS_DAT S1,SCHREG_REGD_DAT S2 "
                                          + "WHERE  S1.YEAR='"+param[0]+"' AND S1.SEMESTER<='"+param[1]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           " S1.SUBCLASSCD =? AND S2.YEAR='"+param[0]+"' AND "
                                                 + "S2.SEMESTER='"+param[1]+"' AND S2.GRADE='"+param[2]+"' AND ");
            if( ptype==0 )
                stb.append(                        "S2.HR_CLASS='"+param[3]+"' AND ");
            stb.append(                            "S1.SCHREGNO=S2.SCHREGNO "
                                          + "GROUP BY S1.SCHREGNO,S1.SEMESTER,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           " S1.SUBCLASSCD "
                                        + ")W4 "
                                        + "INNER JOIN SCHREG_REGD_DAT W1 ON W1.SCHREGNO=W4.SCHREGNO AND "
                                                + "W1.SEMESTER=W4.SEMESTER AND W1.YEAR='"+param[0]+ "' "
                                        + "INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR=W2.YEAR AND "
                                                + "W1.SEMESTER=W2.SEMESTER AND W1.GRADE=W2.GRADE AND "
                                                + "W1.HR_CLASS=W2.HR_CLASS "
                                        + "LEFT JOIN CREDIT_MST W3 ON W1.YEAR=W3.YEAR AND W1.GRADE=W3.GRADE AND "
                                                + "W1.COURSECD=W3.COURSECD AND W1.MAJORCD=W3.MAJORCD AND "
                                                + "W4.SUBCLASSCD=");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                           " W3.SUBCLASSCD AND VALUE(W1.COURSECODE,'0000')=VALUE(W3.COURSECODE,'0000') "
                                   + "GROUP BY GROUPING SETS ((W4.SCHREGNO,W4.SEMESTER),(W4.SCHREGNO)) "
                            + ")S1 "
                        + "GROUP BY SEMESTER "
                    + ")T2 ON T2.SEMESTER=T9.SEMESTER "
                + "ORDER BY T9.SEMESTER");

            if( ptype==0 ) {
                sql3 = stb.toString();
                ps3 = db2.prepareStatement(sql3); //学級
            } else {
                sql4 = stb.toString();
                ps4 = db2.prepareStatement(sql4); //校内
            }
        } catch( Exception e ){
            log.error("[KNJD070K]Pre_Stat_3 error!", e);
        }

    }//Pre_Stat_3の括り

    /**PrepareStatement close**/
    private void Pre_Stat_F()
    {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
        } catch( Exception e ){
            log.error("[KNJC070]Pre_Stat_F error!", e);
        }
    }//Pre_Stat_Fの括り



    /**ＳＶＦ−ＦＯＲＭフィールド出力**/
    private int Svf_Field_Set_1(String s_field,int ia,String d_field){

        int ret = 0;
        if( d_field!=null )
            ret = svf.VrsOutn(s_field ,ia ,d_field);
        return ret;

    }//Svf_Field_Set_1の括り



    /**ＳＶＦ−ＦＯＲＭフィールド出力**/
    private int Svf_Field_Set_2(String s_field,String d_field){

        int ret = 0;
        if( d_field!=null )
            ret = svf.VrsOut(s_field ,d_field);
        return ret;

    }//Svf_Field_Set_2の括り



}//クラスの括り

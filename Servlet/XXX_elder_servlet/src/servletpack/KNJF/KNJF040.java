package servletpack.KNJF;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０４０＞  保健各種帳票印刷（個人）
 *
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2003/11/14 nakamoto set_meisai7修正　処置歯数　＝永久歯処置歯数　＋乳歯処置歯数
 *                                      未処置歯数＝永久歯未処置歯数＋乳歯未処置歯数
 * 2003/11/15 nakamoto SQL整理
 * 2003/11/20 nakamoto フォームＩＤ変更（KNJF030_6.frm ⇔ KNJF030_7.frm）
 * 2003/11/27 nakamoto フォームのみ出力を追加
 * 2004/04/21 nakamoto 健康診断の未受検項目のある生徒への歯科検診において、出力条件の項目を修正（バグ）
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJF040 extends HttpServlet {
    
    private static final Log log = LogFactory.getLog(KNJF040.class);
    
    Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;      // Databaseクラスを継承したクラス
    String dbname = new String();
    int ret;            // リターン値
    boolean nonedata = false; //該当データなしフラグ


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        String param[] = new String[25];     /** 0:年度 1:今学期 2:学籍番号 3:提出日 4:出力区分 22:学年 23:組 
                                                 6:作成年月日 7:学校名１ 8:学校名１+２) 9:校長名 10:固定文字（校長） 
                                                 11-19:保健各種帳票 20:一般条件 21:歯・口腔条件 **/

    // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");                          // データベース名
            param[0] = request.getParameter("YEAR");                            // 年度
            param[1] = request.getParameter("GAKKI");                           // 今学期
            param[11] = request.getParameter("CHECK1");                         // 生徒学生健康診断票(一般)
            param[12] = request.getParameter("CHECK2");                         // 生徒学生健康診断票(歯・口腔)
            param[13] = request.getParameter("CHECK3");                         // 健康診断の未受検項目のある生徒へ
        //  日付型を変換
            String strx = request.getParameter("DATE");                         // 学校への提出日
            if(strx != null ){
// 2003/11/12
                param[3] = strx;                                                    // 学校への提出日
            }
            param[14] = request.getParameter("CHECK4");                         // 眼科検診のお知らせ
            param[15] = request.getParameter("CHECK5");                         // 検診結果のお知らせ(歯・口腔)
            param[16] = request.getParameter("CHECK6");                         // 検診結果のお知らせ(一般)
            param[4] = request.getParameter("OUTPUT");                          // 1:１人で１枚にまとめて出力
                                                                                // 2:１人で各種類ごとに出力
            param[17] = request.getParameter("CHECK7");                         // 定期健康診断結果
            param[18] = request.getParameter("CHECK8");                         // 内科検診所見あり生徒の名簿
            param[19] = request.getParameter("CHECK9");                         // 定期健康診断異常者一覧表
            param[20] = request.getParameter("SELECT1");                        // 一般条件
            param[21] = request.getParameter("SELECT2");                        // 歯・口腔条件
            String schno[] = request.getParameterValues("category_selected");       // 学籍番号
            // 学籍番号の指定
            int i = 0;
            param[2] = "(";
            while(i < schno.length){
                if(schno[i] == null ) break;
                if(i > 0) param[2] = param[2] + ",";
                param[2] = param[2] + "'" + schno[i] + "'";
                i++;
            }
            param[2] = param[2] + ")";

            param[23] = request.getParameter("OUTPUTA");                        // 1:結果 2:フォーム 2003/11/27
            param[24] = request.getParameter("OUTPUTB");                        // 1:結果 2:フォーム 2003/11/27

            log.debug("[KNJF040]parameter ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]parameter error!", ex);
        }
        for(int ia=0 ; ia<param.length ; ia++){
            //log.debug("[KNJF040]param[" + ia + "]= " + param[ia]);
            if(param[ia] == null ) param[ia] = "";
        }

    // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        ret = svf.VrInit();                        //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
            log.debug("[KNJF040]DB2 opened ok");
        } catch( Exception ex ) {
            log.debug("[KNJF040]DB2 open error!", ex);
        }

    //  作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control();                            //クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            param[6] = returnval.val3;                                          //現在処理日
        } catch( Exception e ){
            log.debug("[KNJF040]ctrl_date get error!", e);
        }

    //  学校名・学校住所・校長名の取得
        try {
            KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(param[0]);   //取得クラスのインスタンス作成
            KNJ_Schoolinfo.ReturnVal returnval = schoolinfo.get_info(db2);
            param[7] = returnval.SCHOOL_NAME1;                                          //学校名１
            param[8] = returnval.SCHOOL_NAME2;                                          //学校名２
            param[9] = returnval.PRINCIPAL_NAME;                                        //校長名
            param[10] = returnval.PRINCIPAL_JOBNAME;

        } catch( Exception e ){
            log.debug("[KNJF040]schoolinfo error!");
            log.debug( e );
        }

        for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJF040]param[" + ia + "]= " + param[ia]);


    /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理       
      -----------------------------------------------------------------------------*/

    // ＳＶＦフォーム出力
        nonedata = false ; //該当データなしフラグ
        /*１）生徒学生健康診断票（一般）*/
        if(param[11].equalsIgnoreCase("on")){
            if(param[23].equals("1")){
                seito_info1(param);
            } else{
                ret = svf.VrSetForm("KNJF030_1.frm", 4);
                set_detail(param,1);    // 2003/11/27
            }
        }
        /*２）生徒学生健康診断票（歯・口腔）*/
        if(param[12].equalsIgnoreCase("on")){
            if(param[24].equals("1")){
                seito_info2(param);
            } else{
                ret = svf.VrSetForm("KNJF030_2.frm", 4);
                set_detail(param,2);    // 2003/11/27
            }
        }
        /*３）健康診断の未受検項目のある生徒へ*/
        if(param[13].equalsIgnoreCase("on")){
            ret = svf.VrSetForm("KNJF030_3.frm", 4);
            set_head3(param);
            set_meisai3(param);
        }
        /*４）眼科検診のお知らせ*/
        if(param[14].equalsIgnoreCase("on")){
            ret = svf.VrSetForm("KNJF030_4.frm", 4);
            set_head4(param);
            set_meisai4(param);
        }
        /*５）検診結果のお知らせ（歯・口腔）*/
        if(param[15].equalsIgnoreCase("on")){
            ret = svf.VrSetForm("KNJF030_5.frm", 4);
            set_head5(param);
            set_meisai5(param);
        }
        /*７）検診結果のお知らせ（一般）*/
        if(param[16].equalsIgnoreCase("on")){
            ret = svf.VrSetForm("KNJF030_7.frm", 4);    // 2003/11/20
            set_head6(param);
            set_meisai6(param);
        }
        /*６）定期健康診断結果*/
        if(param[17].equalsIgnoreCase("on")){
            seito_info7(param);
        }
        /*８）内科検診所見あり生徒の名簿*/
        if(param[18].equalsIgnoreCase("on")){
            ret = svf.VrSetForm("KNJF030_8.frm", 4);
            set_head8(param);
            set_meisai8(param);
        }
        /*９）定期健康診断異常者一覧表*/
        if(param[19].equalsIgnoreCase("on")){
        if(param[20].equalsIgnoreCase("17") || param[20].equalsIgnoreCase("18")){
        } else{
            ret = svf.VrSetForm("KNJF030_9.frm", 4);
            set_head9(param);
            set_meisai9(param);
        }
        }

        /*該当データ無し*/
        log.debug("[KNJF040]nonedata="+nonedata);
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            log.debug("[KNJF040]nonedata VrEndRecord ret="+ret);
            ret = svf.VrEndPage();
        }

        ret = svf.VrPrint();

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }    //doGetの括り


    /*------------------------------*
     * フォームのみ出力 2003/11/27  *
     *------------------------------*/
    public void set_detail(String param[],int f_type)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T1.GRADE,"
                    + "T1.HR_CLASS,"
                    + "T1.ATTENDNO,"
                    + "T3.HR_NAME,"
                    + "T2.NAME_SHOW AS SCH_NAME,"
                    + "T2.BIRTHDAY,"
                    + "YEAR('" + (param[0]+ "-04-01") + "' - T2.BIRTHDAY) AS AGE,"
                    + "(SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=T2.SEX) AS SEX "
                + "FROM "
                    + "SCHREG_REGD_DAT  T1,"
                    + "SCHREG_BASE_MST  T2,"
                    + "SCHREG_REGD_HDAT T3 "
                + "WHERE "
                        + "T1.YEAR      = '" + param[0] + "' "
                    + "AND T1.SEMESTER  = '" + param[1] + "' "
                    + "AND T1.SCHREGNO  IN " + param[2] + " "
                    + "AND T2.SCHREGNO  = T1.SCHREGNO "
                    + "AND T1.YEAR      = T3.YEAR "
                    + "AND T1.SEMESTER  = T3.SEMESTER "
                    + "AND T1.GRADE     = T3.GRADE "
                    + "AND T1.HR_CLASS  = T3.HR_CLASS "
                + "ORDER BY "
                    + "T1.GRADE,"
                    + "T1.HR_CLASS,"
                    + "T1.ATTENDNO";

            //log.debug("[KNJF040]set_detail sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            //log.debug("[KNJF040]set_detail sql ok!");

            while( rs.next() ){
                ret = svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                ret = svf.VrsOut("NAME_SHOW"  ,  rs.getString("SCH_NAME"));
                ret = svf.VrsOut("SEX"        ,  rs.getString("SEX"));

                ret = svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                ret = svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                ret = svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                ret = svf.VrsOut("AGE"        ,  rs.getString("AGE"));

                if(f_type == 1){
                    ret = svf.VrsOut("SCHOOL_NAME",  param[8]);
                    ret = svf.VrsOut("YEAR", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                } else {
                    ret=svf.VrsOut("NENDO1", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])).substring(0,2));
                    ret=svf.VrsOut("NENDO2", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])).substring(2));
                }

                ret = svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_detail read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_detail read error!", ex);
        }

    }  //set_detailの括り


    /*--------------------*
     * 生徒情報取得       *
     *--------------------*/
    public void seito_info1(String param[])
                     throws ServletException, IOException
    {
        String hr_name_info  = new String();
        String schregno_info = new String();
        String attendno_info = new String();
        int info_no = 0;

        try {
            String sql = new String();
            sql = "SELECT "
                        + "T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T2.HR_NAME "
                    + "FROM "
                        + "SCHREG_REGD_DAT T1,"
                        + "SCHREG_REGD_HDAT T2 "
                    + "WHERE "
                            + "T1.SEMESTER   = '" +  param[1] + "' "
                        + "AND T1.YEAR       = '" +  param[0] + "' "
                        + "AND T1.SCHREGNO   IN " +  param[2] + " "
                        + "AND T1.YEAR       = T2.YEAR "
                        + "AND T1.SEMESTER   = T2.SEMESTER "
                        + "AND T1.GRADE      = T2.GRADE "
                        + "AND T1.HR_CLASS   = T2.HR_CLASS "
                    + "ORDER BY 2, 3, 4 ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            int i = 0;
            int count = 0;
            while( rs.next() ){
                if(i > 0){
                    hr_name_info = hr_name_info + ",";
                    schregno_info = schregno_info + ",";
                    attendno_info = attendno_info + ",";
                }
                hr_name_info = hr_name_info + rs.getString("HR_NAME");
                schregno_info = schregno_info + rs.getString("SCHREGNO");
                attendno_info = attendno_info + rs.getString("ATTENDNO");
                info_no = count;
                i++;
                count++;
            }

            log.debug("[KNJF040]seito_info1 info_no="+info_no);
            db2.commit();
            log.debug("[KNJF040]seito_info1 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]seito_info1 read error!", ex);
        }

        //log.debug("[KNJF040]seito_info1 Please wait for about 2 minutes!");
        ret = svf.VrSetForm("KNJF030_1.frm", 4);
        set_head1(param);

        StringTokenizer stz1 = new StringTokenizer(schregno_info,",",false);    //対象生徒
        StringTokenizer stz2 = new StringTokenizer(hr_name_info,",",false);     //対象生徒
        StringTokenizer stz3 = new StringTokenizer(attendno_info,",",false);    //対象生徒
        while (stz1.hasMoreTokens()){
            set_meisai1_1(param,stz1.nextToken(),stz2.nextToken(),stz3.nextToken());    //明細出力のメソッド
        }
        log.debug("[KNJF040]seito_info1 path!");

    }  //seito_info1の括り


    public void seito_info2(String param[])
                     throws ServletException, IOException
    {
        String hr_name_info  = new String();
        String schregno_info = new String();
        String grade_info    = new String();
        String attendno_info = new String();
        int info_no = 0;

        try {
            String sql = new String();
            sql = "SELECT "
                        + "T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T2.HR_NAME "
                    + "FROM "
                        + "SCHREG_REGD_DAT T1,"
                        + "SCHREG_REGD_HDAT T2 "
                    + "WHERE "
                            + "T1.SEMESTER   = '" +  param[1] + "' "
                        + "AND T1.YEAR       = '" +  param[0] + "' "
                        + "AND T1.SCHREGNO   IN " +  param[2] + " "
                        + "AND T1.YEAR       = T2.YEAR "
                        + "AND T1.SEMESTER   = T2.SEMESTER "
                        + "AND T1.GRADE      = T2.GRADE "
                        + "AND T1.HR_CLASS   = T2.HR_CLASS "
                    + "ORDER BY 2, 3, 4 ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            int i = 0;
            int count = 0;
            while( rs.next() ){
                if(i > 0){
                    hr_name_info = hr_name_info + ",";
                    schregno_info = schregno_info + ",";
                    grade_info = grade_info + ",";
                    attendno_info = attendno_info + ",";
                }
                hr_name_info = hr_name_info + rs.getString("HR_NAME");
                schregno_info = schregno_info + rs.getString("SCHREGNO");
                grade_info = grade_info + rs.getString("GRADE");
                attendno_info = attendno_info + rs.getString("ATTENDNO");
                info_no = count;
                i++;
                count++;
            }

            log.debug("[KNJF040]seito_info2 info_no="+info_no);
            db2.commit();
            log.debug("[KNJF040]seito_info2 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]seito_info2 read error!", ex);
        }

        //log.debug("[KNJF040]seito_info2 Please wait for about 2 minutes!");
        ret = svf.VrSetForm("KNJF030_2.frm", 4);

        StringTokenizer stz1 = new StringTokenizer(schregno_info,",",false);    //対象生徒
        StringTokenizer stz2 = new StringTokenizer(hr_name_info,",",false);     //対象生徒
        StringTokenizer stz3 = new StringTokenizer(grade_info,",",false);       //対象生徒
        StringTokenizer stz4 = new StringTokenizer(attendno_info,",",false);    //対象生徒
        while (stz1.hasMoreTokens()){
            set_meisai2_1(param,stz1.nextToken(),stz2.nextToken(),stz3.nextToken(),stz4.nextToken());   //明細出力のメソッド
        }
        log.debug("[KNJF040]seito_info2 path!");

    }  //seito_info2の括り


    public void seito_info7(String param[])
                     throws ServletException, IOException
    {
        String hr_name_info  = new String();
        String schregno_info = new String();
        String attendno_info = new String();
        int info_no = 0;

        try {
            String sql = new String();
            sql = "SELECT "
                        + "T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T2.HR_NAME "
                    + "FROM "
                        + "SCHREG_REGD_DAT T1,"
                        + "SCHREG_REGD_HDAT T2 "
                    + "WHERE "
                            + "T1.SEMESTER   = '" +  param[1] + "' "
                        + "AND T1.YEAR       = '" +  param[0] + "' "
                        + "AND T1.SCHREGNO   IN " +  param[2] + " "
                        + "AND T1.YEAR       = T2.YEAR "
                        + "AND T1.SEMESTER   = T2.SEMESTER "
                        + "AND T1.GRADE      = T2.GRADE "
                        + "AND T1.HR_CLASS   = T2.HR_CLASS "
                    + "ORDER BY 2, 3, 4 ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            int i = 0;
            int count = 0;
            while( rs.next() ){
                if(i > 0){
                    hr_name_info = hr_name_info + ",";
                    schregno_info = schregno_info + ",";
                    attendno_info = attendno_info + ",";
                }
                hr_name_info = hr_name_info + rs.getString("HR_NAME");
                schregno_info = schregno_info + rs.getString("SCHREGNO");
                attendno_info = attendno_info + rs.getString("ATTENDNO");
                info_no = count;
                i++;
                count++;
            }

            log.debug("[KNJF040]seito_info7 info_no="+info_no);
            db2.commit();
            log.debug("[KNJF040]seito_info7 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]seito_info7 read error!", ex);
        }

        //log.debug("[KNJF040]seito_info7 Please wait for about 2 minutes!");
        ret = svf.VrSetForm("KNJF030_6.frm", 4);    // 2003/11/20

        StringTokenizer stz1 = new StringTokenizer(schregno_info,",",false);    //対象生徒
        StringTokenizer stz2 = new StringTokenizer(hr_name_info,",",false);     //対象生徒
        StringTokenizer stz3 = new StringTokenizer(attendno_info,",",false);    //対象生徒
        String schregno = new String();
        while (stz1.hasMoreTokens()){
            schregno = stz1.nextToken();
            set_head7(param,schregno,stz2.nextToken(),stz3.nextToken());    //明細出力のメソッド
            set_meisai7(param,schregno);                                    //明細出力のメソッド
            set_meisai7_clear(param);                                               //明細出力のメソッド
        }
        log.debug("[KNJF040]seito_info7 path!");

    }  //seito_info7の括り


    /*------------------------------------*
     *     明細ＳＶＦ出力　　　           *
     *------------------------------------*/
    /*---------------------------------------*
     * １）生徒学生健康診断票（一般）        *
     *---------------------------------------*/
    public void set_meisai1_1(String param[],String schregno_info,String hr_name_info,String attendno_info)
                     throws ServletException, IOException
    {
        String medexam_year[] = new String[6];
        int medexam_year_no = 0;

        try {
            String sql = new String();
            sql = "SELECT "
                        + "SCHREGNO,YEAR "
                    + "FROM "
                        + "MEDEXAM_DET_DAT "
                    + "WHERE "
                            + "SCHREGNO            = '" +  schregno_info + "' "
                    + "ORDER BY 2 ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            int i = 0;
            int count = 0;
            while( rs.next() ){
                medexam_year[i] = rs.getString("YEAR");
                
                i++;
                count++;
                medexam_year_no = count;
            }

            //log.debug("[KNJF040]set_meisai1_1 medexam_year_no="+medexam_year_no);
            db2.commit();
            //log.debug("[KNJF040]set_meisai1_1 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai1_1 read error!", ex);
        }

        //log.debug("[KNJF040]set_meisai1_1 Please wait for about 2 minutes!");
        for(int ib=0;ib<medexam_year_no;ib++){
            if(medexam_year[ib].equalsIgnoreCase(param[0])){
                set_meisai1_3(medexam_year[ib],schregno_info,hr_name_info,attendno_info);
            } else {
                set_meisai1_2(medexam_year[ib],schregno_info);
            }
            //log.debug("[KNJF040]set_meisai1_1 nendo_count="+ib);
        }
        //log.debug("[KNJF040]set_meisai1_1 path!");

    }  //set_meisai1_1の括り


    public void set_meisai1_2(String medexam_year,String schregno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T3.GRADE AS GRADE_OLD,"
                    + "T3.HR_CLASS AS HR_CLASS_OLD,"
                    + "T3.ATTENDNO AS ATTENDNO_OLD,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T1.HR_NAME,"
                    + "T0.BIRTHDAY,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 1, 4) AS NEN,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 6, 2) || SUBSTR(CHAR(T0.BIRTHDAY), 9, 2) AS TSUKI_HI,"
                    + "INTEGER(T2.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) AS AGE1,"
                    + "INTEGER(T2.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) - 1 AS AGE2,"
                    + "T0.SEX,"
                    + "T4.NAME2 AS Seibetsu,"
                    + "T2.HEIGHT,"
                    + "T2.WEIGHT,"
                    + "T2.SITHEIGHT,"
                    + "T2.R_BAREVISION,"
                    + "T2.R_VISION,"
                    + "T2.L_BAREVISION,"
                    + "T2.L_VISION,"
                    + "T2.R_EAR_DB,"
                    + "T2.L_EAR_DB,"
                    + "T18.NAME1 AS Chouryoku_Migi,"
                    + "T19.NAME1 AS Chouryoku_Hidari,"
                    + "CASE T20.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T20.NAME1 "
                    + "END AS Nyou_Tanpaku,"
                    + "CASE T21.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T21.NAME1 "
                    + "END AS Nyou_Tou,"
                    + "CASE T22.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T22.NAME1 "
                    + "END AS Nyou_Senketsu,"
                    + "T2.URICOTHERTEST,"
                    + "T2.NUTRITIONCD,"
                    + "T5.NAME1 AS Eiyo_Jyotai,"
                    + "T2.SPINERIBCD,"
                    + "T6.NAME1 AS Sekichuu_Kyokaku,"
                    + "T2.EYEDISEASECD,"
                    + "T7.NAME1 AS Meekibyo_Ijyo,"
                    + "T2.NOSEDISEASECD,"
                    + "T8.NAME1 AS Jibi_Shikkan,"
                    + "T2.SKINDISEASECD,"
                    + "T9.NAME1 AS Hifu_Shikkan,"
                    + "T2.HEART_MEDEXAM,"
                    + "T10.NAME1 AS Rinsyo_Shinzo,"
                    + "T2.HEARTDISEASECD,"
                    + "T11.NAME1 AS Ekibyo_Shinzo,"
                    + "T2.TB_FILMDATE,"
                    + "SUBSTR(CHAR(T2.TB_FILMDATE), 1, 4) || SUBSTR(CHAR(T2.TB_FILMDATE), 6, 2) || SUBSTR(CHAR(T2.TB_FILMDATE), 9, 2) AS TB_FILMDATE1,"
                    + "T2.TB_FILMNO,"
                    + "T2.TB_REMARKCD,"
                    + "T12.NAME1 AS Shoken_Kekkaku,"
                    + "T2.TB_OTHERTESTCD,"
                    + "T13.NAME1 AS Sonota_Kekkaku,"
                    + "T2.TB_NAMECD,"
                    + "T14.NAME1 AS Byomei_Kekkaku,"
                    + "T2.TB_ADVISECD,"
                    + "T15.NAME1 AS Shidoukubun_Kekkaku,"
                    + "T2.ANEMIA_REMARK,"
                    + "T2.HEMOGLOBIN,"
                    + "T2.OTHERDISEASECD,"
                    + "T16.NAME1 AS Sonota,"
                    + "T2.DOC_REMARK,"
                    + "T2.DOC_DATE,"
                    + "SUBSTR(CHAR(T2.DOC_DATE), 1, 4) || SUBSTR(CHAR(T2.DOC_DATE), 6, 2) || SUBSTR(CHAR(T2.DOC_DATE), 9, 2) AS DOC_DATE1,"
                    + "T2.TREATCD,"
                    + "T17.NAME1 AS Jigosyochi,"
                    + "T2.REMARK "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_DET_DAT "
                        + "WHERE "
                                + "YEAR  = '" + medexam_year + "' "
                            + "AND SCHREGNO     = '" + schregno_info + "' "
                    + ") T2 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T2.SCHREGNO) "
                    + "INNER JOIN ("
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SCHREGNO = '" +  schregno_info + "' "
                            + "AND YEAR     = '" +  medexam_year + "' "
                            + "AND SEMESTER = ( "
                                                + "SELECT "
                                                    + "MIN(SEMESTER) "
                                                + "FROM "
                                                    + "SCHREG_REGD_DAT "
                                                + "WHERE "
                                                        + "SCHREGNO = '" +  schregno_info + "' "
                                                    + "AND YEAR     = '" +  medexam_year + "' "
                                            + ") "
                    + ") T3 ON (T2.YEAR = T3.YEAR AND T2.SCHREGNO = T3.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T1 ON (T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'Z002' AND T4.NAMECD2  = T0.SEX) "
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F030' AND T5.NAMECD2  = T2.NUTRITIONCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F040' AND T6.NAMECD2  = T2.SPINERIBCD) "
                    + "LEFT JOIN NAME_MST T7  ON (T7.NAMECD1  = 'F050' AND T7.NAMECD2  = T2.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'F060' AND T8.NAMECD2  = T2.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T9  ON (T9.NAMECD1  = 'F070' AND T9.NAMECD2  = T2.SKINDISEASECD) "
                    + "LEFT JOIN NAME_MST T10 ON (T10.NAMECD1 = 'F080' AND T10.NAMECD2 = T2.HEART_MEDEXAM) "
                    + "LEFT JOIN NAME_MST T11 ON (T11.NAMECD1 = 'F090' AND T11.NAMECD2 = T2.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T12 ON (T12.NAMECD1 = 'F100' AND T12.NAMECD2 = T2.TB_REMARKCD) "
                    + "LEFT JOIN NAME_MST T13 ON (T13.NAMECD1 = 'F110' AND T13.NAMECD2 = T2.TB_OTHERTESTCD) "
                    + "LEFT JOIN NAME_MST T14 ON (T14.NAMECD1 = 'F120' AND T14.NAMECD2 = T2.TB_NAMECD) "
                    + "LEFT JOIN NAME_MST T15 ON (T15.NAMECD1 = 'F130' AND T15.NAMECD2 = T2.TB_ADVISECD) "
                    + "LEFT JOIN NAME_MST T16 ON (T16.NAMECD1 = 'F140' AND T16.NAMECD2 = T2.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T17 ON (T17.NAMECD1 = 'F150' AND T17.NAMECD2 = T2.TREATCD) "
                    + "LEFT JOIN NAME_MST T18 ON (T18.NAMECD1 = 'F010' AND T18.NAMECD2 = T2.R_EAR) "
                    + "LEFT JOIN NAME_MST T19 ON (T19.NAMECD1 = 'F010' AND T19.NAMECD2 = T2.L_EAR) "
                    + "LEFT JOIN NAME_MST T20 ON (T20.NAMECD1 = 'F020' AND T20.NAMECD2 = T2.ALBUMINURIA1CD) "
                    + "LEFT JOIN NAME_MST T21 ON (T21.NAMECD1 = 'F020' AND T21.NAMECD2 = T2.URICSUGAR1CD) "
                    + "LEFT JOIN NAME_MST T22 ON (T22.NAMECD1 = 'F020' AND T22.NAMECD2 = T2.URICBLEED1CD) ";

                //+ "ORDER BY 3, 4, 5, 2 ";

            //log.debug("[KNJF040]set_meisai1_2 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            //log.debug("[KNJF040]set_meisai1_2 sql ok!");

            /** SVFフォームへデータをセット **/
            int nendo_tsukihi = 0401;
            int tsukihi = 0;
            int count = 0;
            while( rs.next() ){
                count++;
                //log.debug("[KNJF040]set_meisai1_2 count="+count);

                ret=svf.VrsOut("SCHREGNO"   ,  schregno_info);   //改ページ用

                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                ret=svf.VrsOut("SEX"        ,  rs.getString("Seibetsu"));
// 2003/11/12
//              ret=svf.VrsOut("BIRTHDAY"   ,  rs.getString("BIRTHDAY"));
                ret=svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));

                ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO_OLD"));
// 2003/11/12
//              ret=svf.VrsOut("YEAR"       ,  medexam_year);
                ret=svf.VrsOut("YEAR", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)) + "年度");
                //４月１日現在の年齢
                tsukihi = rs.getInt("TSUKI_HI");
                if(nendo_tsukihi >= tsukihi){
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE1"));
                } else{
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE2"));
                }
                ret=svf.VrsOut("HEIGHT"         ,  rs.getString("HEIGHT"));
                ret=svf.VrsOut("WEIGHT"         ,  rs.getString("WEIGHT"));
                ret=svf.VrsOut("SIT_HEIGHT"     ,  rs.getString("SITHEIGHT"));
                ret=svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION"));
                ret=svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION"));
                ret=svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION"));
                ret=svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION"));
                ret=svf.VrsOut("R_EAR_DB"       ,  rs.getString("R_EAR_DB"));
                ret=svf.VrsOut("R_EAR"          ,  rs.getString("Chouryoku_Migi"));
                ret=svf.VrsOut("L_EAR_DB"       ,  rs.getString("L_EAR_DB"));
                ret=svf.VrsOut("L_EAR"          ,  rs.getString("Chouryoku_Hidari"));
                ret=svf.VrsOut("ALBUMINURIA"    ,  rs.getString("Nyou_Tanpaku"));
                ret=svf.VrsOut("URICSUGAR"      ,  rs.getString("Nyou_Tou"));
                ret=svf.VrsOut("URICBLEED"      ,  rs.getString("Nyou_Senketsu"));
                ret=svf.VrsOut("URINE_OTHERS1"  ,  rs.getString("URICOTHERTEST"));
                ret=svf.VrsOut("NUTRITION"      ,  rs.getString("Eiyo_Jyotai"));
                ret=svf.VrsOut("SPINERIB"       ,  rs.getString("Sekichuu_Kyokaku"));
                ret=svf.VrsOut("EYEDISEASE"     ,  rs.getString("Meekibyo_Ijyo"));
                ret=svf.VrsOut("NOSEDISEASE"    ,  rs.getString("Jibi_Shikkan"));
                ret=svf.VrsOut("SKINDISEASE"    ,  rs.getString("Hifu_Shikkan"));
                ret=svf.VrsOut("HEART_MEDEXAM"  ,  rs.getString("Rinsyo_Shinzo"));
                ret=svf.VrsOut("HEARTDISEASE1"  ,  rs.getString("Ekibyo_Shinzo"));
// 2003/11/12
//              ret=svf.VrsOut("PHOTO_DATE"     ,  rs.getString("TB_FILMDATE1"));
                ret=svf.VrsOut("PHOTO_DATE"     ,  KNJ_EditDate.h_format_JP_MD(rs.getString("TB_FILMDATE")));
                ret=svf.VrsOut("FILMNO"         ,  rs.getString("TB_FILMNO"));
                ret=svf.VrsOut("VIEWS1_1"       ,  rs.getString("Shoken_Kekkaku"));
                ret=svf.VrsOut("OTHERS"         ,  rs.getString("Sonota_Kekkaku"));
                ret=svf.VrsOut("DISEASE_NAME"   ,  rs.getString("Byomei_Kekkaku"));
                ret=svf.VrsOut("GUIDANCE"       ,  rs.getString("Shidoukubun_Kekkaku"));
                ret=svf.VrsOut("ANEMIA"         ,  rs.getString("ANEMIA_REMARK"));
                ret=svf.VrsOut("HEMOGLOBIN"     ,  rs.getString("HEMOGLOBIN"));
                ret=svf.VrsOut("OTHERDISEASE"   ,  rs.getString("Sonota"));
                ret=svf.VrsOut("VIEWS2_1"       ,  rs.getString("DOC_REMARK"));
// 2003/11/12
//              ret=svf.VrsOut("DOC_DATE"       ,  rs.getString("DOC_DATE1"));
                ret=svf.VrsOut("DOC_DATE"       ,  KNJ_EditDate.h_format_JP_MD(rs.getString("DOC_DATE")));
                ret=svf.VrsOut("DOC_TREAT1"     ,  rs.getString("Jigosyochi"));
                ret=svf.VrsOut("NOTE1"          ,  rs.getString("REMARK"));

                ret = svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            //log.debug("[KNJF040]set_meisai1_2 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai1_2 read error!", ex);
        }
        //log.debug("[KNJF040]set_meisai1_2 path!");

    }  //set_meisai1_2の括り


    public void set_meisai1_3(String medexam_year,String schregno_info,String hr_name_info,String attendno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T0.NAME_SHOW AS NAME,"
                    + "T0.BIRTHDAY,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 1, 4) AS NEN,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 6, 2) || SUBSTR(CHAR(T0.BIRTHDAY), 9, 2) AS TSUKI_HI,"
                    + "INTEGER(T2.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) AS AGE1,"
                    + "INTEGER(T2.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) - 1 AS AGE2,"
                    + "T0.SEX,"
                    + "T4.NAME2 AS Seibetsu,"
                    + "T2.HEIGHT,"
                    + "T2.WEIGHT,"
                    + "T2.SITHEIGHT,"
                    + "T2.R_BAREVISION,"
                    + "T2.R_VISION,"
                    + "T2.L_BAREVISION,"
                    + "T2.L_VISION,"
                    + "T2.R_EAR_DB,"
                    + "T2.L_EAR_DB,"
                    + "T18.NAME1 AS Chouryoku_Migi,"
                    + "T19.NAME1 AS Chouryoku_Hidari,"
                    + "CASE T20.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T20.NAME1 "
                    + "END AS Nyou_Tanpaku,"
                    + "CASE T21.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T21.NAME1 "
                    + "END AS Nyou_Tou,"
                    + "CASE T22.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T22.NAME1 "
                    + "END AS Nyou_Senketsu,"
                    + "T2.URICOTHERTEST,"
                    + "T2.NUTRITIONCD,"
                    + "T5.NAME1 AS Eiyo_Jyotai,"
                    + "T2.SPINERIBCD,"
                    + "T6.NAME1 AS Sekichuu_Kyokaku,"
                    + "T2.EYEDISEASECD,"
                    + "T7.NAME1 AS Meekibyo_Ijyo,"
                    + "T2.NOSEDISEASECD,"
                    + "T8.NAME1 AS Jibi_Shikkan,"
                    + "T2.SKINDISEASECD,"
                    + "T9.NAME1 AS Hifu_Shikkan,"
                    + "T2.HEART_MEDEXAM,"
                    + "T10.NAME1 AS Rinsyo_Shinzo,"
                    + "T2.HEARTDISEASECD,"
                    + "T11.NAME1 AS Ekibyo_Shinzo,"
                    + "T2.TB_FILMDATE,"
                    + "SUBSTR(CHAR(T2.TB_FILMDATE), 1, 4) || SUBSTR(CHAR(T2.TB_FILMDATE), 6, 2) || SUBSTR(CHAR(T2.TB_FILMDATE), 9, 2) AS TB_FILMDATE1,"
                    + "T2.TB_FILMNO,"
                    + "T2.TB_REMARKCD,"
                    + "T12.NAME1 AS Shoken_Kekkaku,"
                    + "T2.TB_OTHERTESTCD,"
                    + "T13.NAME1 AS Sonota_Kekkaku,"
                    + "T2.TB_NAMECD,"
                    + "T14.NAME1 AS Byomei_Kekkaku,"
                    + "T2.TB_ADVISECD,"
                    + "T15.NAME1 AS Shidoukubun_Kekkaku,"
                    + "T2.ANEMIA_REMARK,"
                    + "T2.HEMOGLOBIN,"
                    + "T2.OTHERDISEASECD,"
                    + "T16.NAME1 AS Sonota,"
                    + "T2.DOC_REMARK,"
                    + "T2.DOC_DATE,"
                    + "SUBSTR(CHAR(T2.DOC_DATE), 1, 4) || SUBSTR(CHAR(T2.DOC_DATE), 6, 2) || SUBSTR(CHAR(T2.DOC_DATE), 9, 2) AS DOC_DATE1,"
                    + "T2.TREATCD,"
                    + "T17.NAME1 AS Jigosyochi,"
                    + "T2.REMARK "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_DET_DAT "
                        + "WHERE "
                                + "YEAR  = '" + medexam_year + "' "
                            + "AND SCHREGNO     = '" + schregno_info + "' "
                    + ") T2 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T2.SCHREGNO) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'Z002' AND T4.NAMECD2  = T0.SEX) "
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F030' AND T5.NAMECD2  = T2.NUTRITIONCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F040' AND T6.NAMECD2  = T2.SPINERIBCD) "
                    + "LEFT JOIN NAME_MST T7  ON (T7.NAMECD1  = 'F050' AND T7.NAMECD2  = T2.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'F060' AND T8.NAMECD2  = T2.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T9  ON (T9.NAMECD1  = 'F070' AND T9.NAMECD2  = T2.SKINDISEASECD) "
                    + "LEFT JOIN NAME_MST T10 ON (T10.NAMECD1 = 'F080' AND T10.NAMECD2 = T2.HEART_MEDEXAM) "
                    + "LEFT JOIN NAME_MST T11 ON (T11.NAMECD1 = 'F090' AND T11.NAMECD2 = T2.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T12 ON (T12.NAMECD1 = 'F100' AND T12.NAMECD2 = T2.TB_REMARKCD) "
                    + "LEFT JOIN NAME_MST T13 ON (T13.NAMECD1 = 'F110' AND T13.NAMECD2 = T2.TB_OTHERTESTCD) "
                    + "LEFT JOIN NAME_MST T14 ON (T14.NAMECD1 = 'F120' AND T14.NAMECD2 = T2.TB_NAMECD) "
                    + "LEFT JOIN NAME_MST T15 ON (T15.NAMECD1 = 'F130' AND T15.NAMECD2 = T2.TB_ADVISECD) "
                    + "LEFT JOIN NAME_MST T16 ON (T16.NAMECD1 = 'F140' AND T16.NAMECD2 = T2.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T17 ON (T17.NAMECD1 = 'F150' AND T17.NAMECD2 = T2.TREATCD) "
                    + "LEFT JOIN NAME_MST T18 ON (T18.NAMECD1 = 'F010' AND T18.NAMECD2 = T2.R_EAR) "
                    + "LEFT JOIN NAME_MST T19 ON (T19.NAMECD1 = 'F010' AND T19.NAMECD2 = T2.L_EAR) "
                    + "LEFT JOIN NAME_MST T20 ON (T20.NAMECD1 = 'F020' AND T20.NAMECD2 = T2.ALBUMINURIA1CD) "
                    + "LEFT JOIN NAME_MST T21 ON (T21.NAMECD1 = 'F020' AND T21.NAMECD2 = T2.URICSUGAR1CD) "
                    + "LEFT JOIN NAME_MST T22 ON (T22.NAMECD1 = 'F020' AND T22.NAMECD2 = T2.URICBLEED1CD) ";

                //+ "ORDER BY 3, 4, 5, 2 ";

            //log.debug("[KNJF040]set_meisai1_3 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            //log.debug("[KNJF040]set_meisai1_3 sql ok!");

            /** SVFフォームへデータをセット **/
            int nendo_tsukihi = 0401;
            int tsukihi = 0;
            int count = 0;
            while( rs.next() ){
                count++;
                //log.debug("[KNJF040]set_meisai1_3 count="+count);

                ret=svf.VrsOut("SCHREGNO"   ,  schregno_info);   //改ページ用

                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                ret=svf.VrsOut("SEX"        ,  rs.getString("Seibetsu"));
// 2003/11/12
//              ret=svf.VrsOut("BIRTHDAY"   ,  rs.getString("BIRTHDAY"));
                ret=svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));

                ret=svf.VrsOut("HR_NAME"    ,  hr_name_info);
                ret=svf.VrsOut("ATTENDNO"   ,  attendno_info);
// 2003/11/12
//              ret=svf.VrsOut("YEAR"       ,  medexam_year);
                ret=svf.VrsOut("YEAR", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)) + "年度");
                //４月１日現在の年齢
                tsukihi = rs.getInt("TSUKI_HI");
                if(nendo_tsukihi >= tsukihi){
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE1"));
                } else{
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE2"));
                }
                ret=svf.VrsOut("HEIGHT"         ,  rs.getString("HEIGHT"));
                ret=svf.VrsOut("WEIGHT"         ,  rs.getString("WEIGHT"));
                ret=svf.VrsOut("SIT_HEIGHT"     ,  rs.getString("SITHEIGHT"));
                ret=svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION"));
                ret=svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION"));
                ret=svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION"));
                ret=svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION"));
                ret=svf.VrsOut("R_EAR_DB"       ,  rs.getString("R_EAR_DB"));
                ret=svf.VrsOut("R_EAR"          ,  rs.getString("Chouryoku_Migi"));
                ret=svf.VrsOut("L_EAR_DB"       ,  rs.getString("L_EAR_DB"));
                ret=svf.VrsOut("L_EAR"          ,  rs.getString("Chouryoku_Hidari"));
                ret=svf.VrsOut("ALBUMINURIA"    ,  rs.getString("Nyou_Tanpaku"));
                ret=svf.VrsOut("URICSUGAR"      ,  rs.getString("Nyou_Tou"));
                ret=svf.VrsOut("URICBLEED"      ,  rs.getString("Nyou_Senketsu"));
                ret=svf.VrsOut("URINE_OTHERS1"  ,  rs.getString("URICOTHERTEST"));
                ret=svf.VrsOut("NUTRITION"      ,  rs.getString("Eiyo_Jyotai"));
                ret=svf.VrsOut("SPINERIB"       ,  rs.getString("Sekichuu_Kyokaku"));
                ret=svf.VrsOut("EYEDISEASE"     ,  rs.getString("Meekibyo_Ijyo"));
                ret=svf.VrsOut("NOSEDISEASE"    ,  rs.getString("Jibi_Shikkan"));
                ret=svf.VrsOut("SKINDISEASE"    ,  rs.getString("Hifu_Shikkan"));
                ret=svf.VrsOut("HEART_MEDEXAM"  ,  rs.getString("Rinsyo_Shinzo"));
                ret=svf.VrsOut("HEARTDISEASE1"  ,  rs.getString("Ekibyo_Shinzo"));
// 2003/11/12
//              ret=svf.VrsOut("PHOTO_DATE"     ,  rs.getString("TB_FILMDATE1"));
                ret=svf.VrsOut("PHOTO_DATE"     ,  KNJ_EditDate.h_format_JP_MD(rs.getString("TB_FILMDATE")));
                ret=svf.VrsOut("FILMNO"         ,  rs.getString("TB_FILMNO"));
                ret=svf.VrsOut("VIEWS1_1"       ,  rs.getString("Shoken_Kekkaku"));
                ret=svf.VrsOut("OTHERS"         ,  rs.getString("Sonota_Kekkaku"));
                ret=svf.VrsOut("DISEASE_NAME"   ,  rs.getString("Byomei_Kekkaku"));
                ret=svf.VrsOut("GUIDANCE"       ,  rs.getString("Shidoukubun_Kekkaku"));
                ret=svf.VrsOut("ANEMIA"         ,  rs.getString("ANEMIA_REMARK"));
                ret=svf.VrsOut("HEMOGLOBIN"     ,  rs.getString("HEMOGLOBIN"));
                ret=svf.VrsOut("OTHERDISEASE"   ,  rs.getString("Sonota"));
                ret=svf.VrsOut("VIEWS2_1"       ,  rs.getString("DOC_REMARK"));
// 2003/11/12
//              ret=svf.VrsOut("DOC_DATE"       ,  rs.getString("DOC_DATE1"));
                ret=svf.VrsOut("DOC_DATE"       ,  KNJ_EditDate.h_format_JP_MD(rs.getString("DOC_DATE")));
                ret=svf.VrsOut("DOC_TREAT1"     ,  rs.getString("Jigosyochi"));
                ret=svf.VrsOut("NOTE1"          ,  rs.getString("REMARK"));

                ret = svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            //log.debug("[KNJF040]set_meisai1_3 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai1_3 read error!", ex);
        }
        //log.debug("[KNJF040]set_meisai1_3 path!");

    }  //set_meisai1_3の括り


    /*---------------------------------------*
     * ２）生徒学生健康診断票（歯・口腔）    *
     *---------------------------------------*/
    public void set_meisai2_1(String param[],String schregno_info,String hr_name_info,String grade_info,String attendno_info)
                     throws ServletException, IOException
    {
        String medexam_year[] = new String[6];
        int medexam_year_no = 0;

        try {
            String sql = new String();
            sql = "SELECT "
                        + "SCHREGNO,YEAR "
                    + "FROM "
                        + "MEDEXAM_TOOTH_DAT "
                    + "WHERE "
                            + "SCHREGNO            = '" +  schregno_info + "' "
                    + "ORDER BY 2 ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            int i = 0;
            int count = 0;
            while( rs.next() ){
                medexam_year[i] = rs.getString("YEAR");
                
                i++;
                count++;
                medexam_year_no = count;
            }

            //log.debug("[KNJF040]set_meisai2_1 medexam_year_no="+medexam_year_no);
            db2.commit();
            //log.debug("[KNJF040]set_meisai2_1 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai2_1 read error!", ex);
        }

        //log.debug("[KNJF040]set_meisai2_1 Please wait for about 2 minutes!");
        for(int ib=0;ib<medexam_year_no;ib++){
            if(medexam_year[ib].equalsIgnoreCase(param[0])){
                set_meisai2_3(medexam_year[ib],schregno_info,hr_name_info,grade_info,attendno_info);
            } else {
                set_meisai2_2(medexam_year[ib],schregno_info);
            }
            //log.debug("[KNJF040]set_meisai2_1 nendo_count="+ib);
        }
        //log.debug("[KNJF040]set_meisai2_1 path!");

    }  //set_meisai2_1の括り


    public void set_meisai2_2(String medexam_year,String schregno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T9.GRADE AS GRADE_OLD,"
                    + "T9.HR_CLASS AS HR_CLASS_OLD,"
                    + "T9.ATTENDNO AS ATTENDNO_OLD,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T7.HR_NAME,"
                    + "T0.SEX,"
                    + "T8.NAME2 AS SEIBETSU,"
                    + "T0.BIRTHDAY,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 1, 4) AS NEN,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 6, 2) || SUBSTR(CHAR(T0.BIRTHDAY), 9, 2) AS TSUKI_HI,"
                    + "INTEGER(T1.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) AS AGE1,"
                    + "INTEGER(T1.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) - 1 AS AGE2,"
                    + "CASE T1.JAWS_JOINTCD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS JAWS_JOINTCD,"
                    + "CASE T1.PLAQUECD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS PLAQUECD,"
                    + "CASE T1.GUMCD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS GUMCD,"
                    + "T1.BABYTOOTH,"
                    + "T1.REMAINBABYTOOTH,"
                    + "T1.TREATEDBABYTOOTH,"
                    + "T1.ADULTTOOTH,"
                    + "T1.REMAINADULTTOOTH,"
                    + "T1.TREATEDADULTTOOTH,"
                    + "T1.LOSTADULTTOOTH,"
                    + "T1.OTHERDISEASECD,"
                    + "T3.NAME1 AS SONOTA,"
                    + "T1.DENTISTREMARKCD,"
                    + "T4.NAME1 AS SHOKEN,"
                    + "T1.DENTISTREMARKDATE,"
                    + "T1.DENTISTTREAT "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_TOOTH_DAT "
                        + "WHERE "
                                + "YEAR  = '" + medexam_year + "' "
                            + "AND SCHREGNO     = '" + schregno_info + "' "
                    + ") T1 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
                    + "INNER JOIN ("
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SCHREGNO = '" +  schregno_info + "' "
                            + "AND YEAR     = '" +  medexam_year + "' "
                            + "AND SEMESTER = ( "
                                                + "SELECT "
                                                    + "MIN(SEMESTER) "
                                                + "FROM "
                                                    + "SCHREG_REGD_DAT "
                                                + "WHERE "
                                                        + "SCHREGNO = '" +  schregno_info + "' "
                                                    + "AND YEAR     = '" +  medexam_year + "' "
                                            + ") "
                    + ") T9 ON (T1.YEAR = T9.YEAR AND T1.SCHREGNO = T9.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T7 ON (T9.YEAR = T7.YEAR AND T9.SEMESTER = T7.SEMESTER AND T9.GRADE = T7.GRADE AND T9.HR_CLASS = T7.HR_CLASS) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'Z002' AND T8.NAMECD2  = T0.SEX) "
                    + "LEFT JOIN NAME_MST T2  ON (T2.NAMECD1  = 'F520' AND T2.NAMECD2  = T1.PLAQUECD) "
                    + "LEFT JOIN NAME_MST T3  ON (T3.NAMECD1  = 'F530' AND T3.NAMECD2  = T1.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'F540' AND T4.NAMECD2  = T1.DENTISTREMARKCD) "
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F510' AND T5.NAMECD2  = T1.GUMCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F510' AND T6.NAMECD2  = T1.JAWS_JOINTCD) ";

            //log.debug("[KNJF040]set_meisai2_2 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            //log.debug("[KNJF040]set_meisai2_2 sql ok!");

            /** SVFフォームへデータをセット **/
            int nendo_tsukihi = 0401;
            int tsukihi = 0;
            String month_day = "";

            while( rs.next() ){
                ret=svf.VrsOut("SCHREGNO"   ,  schregno_info);
                ret=svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO_OLD"));
                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                ret=svf.VrsOut("SEX"        ,  rs.getString("SEIBETSU"));
// 2003/11/12
//              ret=svf.VrsOut("BIRTHDAY"   ,  rs.getString("BIRTHDAY"));
                ret=svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                //４月１日現在の年齢
                tsukihi = rs.getInt("TSUKI_HI");
                if(nendo_tsukihi >= tsukihi){
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE1"));
                } else{
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE2"));
                }
// 2003/11/12
//              ret=svf.VrsOut("NENDO"      ,  medexam_year);
        ret=svf.VrsOut("NENDO1", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)).substring(0,2));
        ret=svf.VrsOut("NENDO2", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)).substring(2));
                //歯列・咬合・顎関節
                if(rs.getInt("JAWS_JOINTCD")==1){
                    ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "○");
                    ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                    ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                } else{
                    if(rs.getInt("JAWS_JOINTCD")==2){
                        ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                        ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "○");
                        ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                    } else{
                        if(rs.getInt("JAWS_JOINTCD")==3){
                            ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                        }
                    }
                }
                //歯垢の状態
                if(rs.getInt("PLAQUECD")==1){
                    ret=svf.VrsOut("PLAQUECD0"  ,  "○");
                    ret=svf.VrsOut("PLAQUECD1"  ,  "");
                    ret=svf.VrsOut("PLAQUECD2"  ,  "");
                } else{
                    if(rs.getInt("PLAQUECD")==2){
                        ret=svf.VrsOut("PLAQUECD0"  ,  "");
                        ret=svf.VrsOut("PLAQUECD1"  ,  "○");
                        ret=svf.VrsOut("PLAQUECD2"  ,  "");
                    } else{
                        if(rs.getInt("PLAQUECD")==3){
                            ret=svf.VrsOut("PLAQUECD0"  ,  "");
                            ret=svf.VrsOut("PLAQUECD1"  ,  "");
                            ret=svf.VrsOut("PLAQUECD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("PLAQUECD0"  ,  "");
                            ret=svf.VrsOut("PLAQUECD1"  ,  "");
                            ret=svf.VrsOut("PLAQUECD2"  ,  "");
                        }
                    }
                }
                //歯肉の状態
                if(rs.getInt("GUMCD")==1){
                    ret=svf.VrsOut("GUMCD0"  ,  "○");
                    ret=svf.VrsOut("GUMCD1"  ,  "");
                    ret=svf.VrsOut("GUMCD2"  ,  "");
                } else{
                    if(rs.getInt("GUMCD")==2){
                        ret=svf.VrsOut("GUMCD0"  ,  "");
                        ret=svf.VrsOut("GUMCD1"  ,  "○");
                        ret=svf.VrsOut("GUMCD2"  ,  "");
                    } else{
                        if(rs.getInt("GUMCD")==3){
                            ret=svf.VrsOut("GUMCD0"  ,  "");
                            ret=svf.VrsOut("GUMCD1"  ,  "");
                            ret=svf.VrsOut("GUMCD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("GUMCD0"  ,  "");
                            ret=svf.VrsOut("GUMCD1"  ,  "");
                            ret=svf.VrsOut("GUMCD2"  ,  "");
                        }
                    }
                }

                ret=svf.VrsOut("BABYTOOTH"          ,  rs.getString("BABYTOOTH"));
                ret=svf.VrsOut("REMAINBABYTOOTH"    ,  rs.getString("REMAINBABYTOOTH"));
                ret=svf.VrsOut("TREATEDBABYTOOTH"   ,  rs.getString("TREATEDBABYTOOTH"));
                ret=svf.VrsOut("ADULTTOOTH"         ,  rs.getString("ADULTTOOTH"));
                ret=svf.VrsOut("REMAINADULTTOOTH"   ,  rs.getString("REMAINADULTTOOTH"));
                ret=svf.VrsOut("TREATEDADULTTOOTH"  ,  rs.getString("TREATEDADULTTOOTH"));
                ret=svf.VrsOut("LOSTADULTTOOTH"     ,  rs.getString("LOSTADULTTOOTH"));

                ret=svf.VrsOut("TOOTHOTHERDISEASE"  ,  rs.getString("SONOTA"));
                ret=svf.VrsOut("DENTISTREMARK"      ,  rs.getString("SHOKEN"));
                //学校歯科医日付
                month_day = rs.getString("DENTISTREMARKDATE");
                //log.debug("[KNJF040]month_day="+month_day);
                if(month_day==null) month_day = "";
                //log.debug("[KNJF040]month_day.length="+month_day.length());
                if(month_day.equalsIgnoreCase("")==false){
                    ret=svf.VrsOut("month"              ,  month_day.substring(5,7));
                    ret=svf.VrsOut("day"                ,  month_day.substring(8,10));
                }
                ret=svf.VrsOut("DENTISTTREAT"     ,  rs.getString("DENTISTTREAT"));

                ret = svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            //log.debug("[KNJF040]set_meisai2_2 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai2_2 read error!", ex);
        }
        //log.debug("[KNJF040]set_meisai2_2 path!");

    }  //set_meisai2_2の括り


    public void set_meisai2_3(String medexam_year,String schregno_info,String hr_name_info,String grade_info,String attendno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T0.NAME_SHOW AS NAME,"
                    + "T0.SEX,"
                    + "T8.NAME2 AS SEIBETSU,"
                    + "T0.BIRTHDAY,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 1, 4) AS NEN,"
                    + "SUBSTR(CHAR(T0.BIRTHDAY), 6, 2) || SUBSTR(CHAR(T0.BIRTHDAY), 9, 2) AS TSUKI_HI,"
                    + "INTEGER(T1.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) AS AGE1,"
                    + "INTEGER(T1.YEAR) - INTEGER(SUBSTR(CHAR(T0.BIRTHDAY), 1, 4)) - 1 AS AGE2,"
                    + "CASE T1.JAWS_JOINTCD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS JAWS_JOINTCD,"
                    + "CASE T1.PLAQUECD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS PLAQUECD,"
                    + "CASE T1.GUMCD "
                    + "WHEN '01' THEN '1' "
                    + "WHEN '02' THEN '2' "
                    + "WHEN '03' THEN '3' "
                    + "ELSE '0' "
                    + "END AS GUMCD,"
                    + "T1.BABYTOOTH,"
                    + "T1.REMAINBABYTOOTH,"
                    + "T1.TREATEDBABYTOOTH,"
                    + "T1.ADULTTOOTH,"
                    + "T1.REMAINADULTTOOTH,"
                    + "T1.TREATEDADULTTOOTH,"
                    + "T1.LOSTADULTTOOTH,"
                    + "T1.OTHERDISEASECD,"
                    + "T3.NAME1 AS SONOTA,"
                    + "T1.DENTISTREMARKCD,"
                    + "T4.NAME1 AS SHOKEN,"
                    + "T1.DENTISTREMARKDATE,"
                    + "T1.DENTISTTREAT "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_TOOTH_DAT "
                        + "WHERE "
                                + "YEAR  = '" + medexam_year + "' "
                            + "AND SCHREGNO     = '" + schregno_info + "' "
                    + ") T1 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'Z002' AND T8.NAMECD2  = T0.SEX) "
                    + "LEFT JOIN NAME_MST T2  ON (T2.NAMECD1  = 'F520' AND T2.NAMECD2  = T1.PLAQUECD) "
                    + "LEFT JOIN NAME_MST T3  ON (T3.NAMECD1  = 'F530' AND T3.NAMECD2  = T1.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'F540' AND T4.NAMECD2  = T1.DENTISTREMARKCD) "
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F510' AND T5.NAMECD2  = T1.GUMCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F510' AND T6.NAMECD2  = T1.JAWS_JOINTCD) ";

            //log.debug("[KNJF040]set_meisai2_3 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            //log.debug("[KNJF040]set_meisai2_3 sql ok!");

            /** SVFフォームへデータをセット **/
            int nendo_tsukihi = 0401;
            int tsukihi = 0;
            String month_day = "";

            while( rs.next() ){
                ret=svf.VrsOut("SCHREGNO"   ,  schregno_info);
                ret=svf.VrsOut("HR_NAME"    ,  hr_name_info);
                ret=svf.VrsOut("ATTENDNO"   ,  attendno_info);
                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                ret=svf.VrsOut("SEX"        ,  rs.getString("SEIBETSU"));
// 2003/11/12
//              ret=svf.VrsOut("BIRTHDAY"   ,  rs.getString("BIRTHDAY"));
                ret=svf.VrsOut("BIRTHDAY"   ,  KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                //４月１日現在の年齢
                tsukihi = rs.getInt("TSUKI_HI");
                if(nendo_tsukihi >= tsukihi){
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE1"));
                } else{
                    ret=svf.VrsOut("AGE"        ,  rs.getString("AGE2"));
                }
// 2003/11/12
//              ret=svf.VrsOut("NENDO"      ,  medexam_year);
        ret=svf.VrsOut("NENDO1", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)).substring(0,2));
        ret=svf.VrsOut("NENDO2", nao_package.KenjaProperties.gengou(Integer.parseInt(medexam_year)).substring(2));
                //歯列・咬合・顎関節
                if(rs.getInt("JAWS_JOINTCD")==1){
                    ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "○");
                    ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                    ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                } else{
                    if(rs.getInt("JAWS_JOINTCD")==2){
                        ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                        ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "○");
                        ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                    } else{
                        if(rs.getInt("JAWS_JOINTCD")==3){
                            ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("JAWS_JOINTCD0"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD1"  ,  "");
                            ret=svf.VrsOut("JAWS_JOINTCD2"  ,  "");
                        }
                    }
                }
                //歯垢の状態
                if(rs.getInt("PLAQUECD")==1){
                    ret=svf.VrsOut("PLAQUECD0"  ,  "○");
                    ret=svf.VrsOut("PLAQUECD1"  ,  "");
                    ret=svf.VrsOut("PLAQUECD2"  ,  "");
                } else{
                    if(rs.getInt("PLAQUECD")==2){
                        ret=svf.VrsOut("PLAQUECD0"  ,  "");
                        ret=svf.VrsOut("PLAQUECD1"  ,  "○");
                        ret=svf.VrsOut("PLAQUECD2"  ,  "");
                    } else{
                        if(rs.getInt("PLAQUECD")==3){
                            ret=svf.VrsOut("PLAQUECD0"  ,  "");
                            ret=svf.VrsOut("PLAQUECD1"  ,  "");
                            ret=svf.VrsOut("PLAQUECD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("PLAQUECD0"  ,  "");
                            ret=svf.VrsOut("PLAQUECD1"  ,  "");
                            ret=svf.VrsOut("PLAQUECD2"  ,  "");
                        }
                    }
                }
                //歯肉の状態
                if(rs.getInt("GUMCD")==1){
                    ret=svf.VrsOut("GUMCD0"  ,  "○");
                    ret=svf.VrsOut("GUMCD1"  ,  "");
                    ret=svf.VrsOut("GUMCD2"  ,  "");
                } else{
                    if(rs.getInt("GUMCD")==2){
                        ret=svf.VrsOut("GUMCD0"  ,  "");
                        ret=svf.VrsOut("GUMCD1"  ,  "○");
                        ret=svf.VrsOut("GUMCD2"  ,  "");
                    } else{
                        if(rs.getInt("GUMCD")==3){
                            ret=svf.VrsOut("GUMCD0"  ,  "");
                            ret=svf.VrsOut("GUMCD1"  ,  "");
                            ret=svf.VrsOut("GUMCD2"  ,  "○");
                        } else{
                            ret=svf.VrsOut("GUMCD0"  ,  "");
                            ret=svf.VrsOut("GUMCD1"  ,  "");
                            ret=svf.VrsOut("GUMCD2"  ,  "");
                        }
                    }
                }

                //log.debug("[KNJF040]TREATEDBABYTOOTH="+rs.getString("TREATEDBABYTOOTH"));
                ret=svf.VrsOut("BABYTOOTH"          ,  rs.getString("BABYTOOTH"));
                ret=svf.VrsOut("REMAINBABYTOOTH"    ,  rs.getString("REMAINBABYTOOTH"));
                ret=svf.VrsOut("TREATEDBABYTOOTH"   ,  rs.getString("TREATEDBABYTOOTH"));
                ret=svf.VrsOut("ADULTTOOTH"         ,  rs.getString("ADULTTOOTH"));
                ret=svf.VrsOut("REMAINADULTTOOTH"   ,  rs.getString("REMAINADULTTOOTH"));
                ret=svf.VrsOut("TREATEDADULTTOOTH"  ,  rs.getString("TREATEDADULTTOOTH"));
                ret=svf.VrsOut("LOSTADULTTOOTH"     ,  rs.getString("LOSTADULTTOOTH"));

                ret=svf.VrsOut("TOOTHOTHERDISEASE"  ,  rs.getString("SONOTA"));
                ret=svf.VrsOut("DENTISTREMARK"      ,  rs.getString("SHOKEN"));
                //学校歯科医日付
                month_day = rs.getString("DENTISTREMARKDATE");
                //log.debug("[KNJF040]month_day="+month_day);
                if(month_day==null) month_day = "";
                //log.debug("[KNJF040]month_day.length="+month_day.length());
                if(month_day.equalsIgnoreCase("")==false){
                    ret=svf.VrsOut("month"              ,  month_day.substring(5,7));
                    ret=svf.VrsOut("day"                ,  month_day.substring(8,10));
                }
                ret=svf.VrsOut("DENTISTTREAT"     ,  rs.getString("DENTISTTREAT"));

                ret = svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            //log.debug("[KNJF040]set_meisai2_3 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai2_3 read error!", ex);
        }
        //log.debug("[KNJF040]set_meisai2_3 path!");

    }  //set_meisai2_3の括り


    /*---------------------------------------*
     * ３）健康診断の未受検項目のある生徒へ  *
     *---------------------------------------*/
    public void set_meisai3(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T7.GRADE,"
                    + "T7.HR_CLASS,"
                    + "T7.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T5.HR_NAME,"
                    + "CASE "
                    + "WHEN T1.NUTRITIONCD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS NUTRITIONCD,"
                    + "CASE "
                    + "WHEN T1.SPINERIBCD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS SPINERIBCD,"
                    + "CASE "
                    + "WHEN T1.NOSEDISEASECD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS NOSEDISEASECD,"
                    + "CASE "
                    + "WHEN T1.SKINDISEASECD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS SKINDISEASECD,"
                    + "CASE "
                    + "WHEN T1.HEART_MEDEXAM > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS HEART_MEDEXAM,"
                    + "CASE "
                    + "WHEN T1.ALBUMINURIA1CD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS ALBUMINURIA1CD,"
                    + "CASE "
                    + "WHEN T1.URICSUGAR1CD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS URICSUGAR1CD,"
                    + "CASE "
                    + "WHEN T1.URICBLEED1CD > '0' THEN '' "
                    + "ELSE 'no' "
                    + "END AS URICBLEED1CD,"
                    + "case when value(T1.URICOTHERTEST,'') > '0' then '' else 'no' end as URICOTHERTEST,"
                    + "T1.HEMOGLOBIN,"
                    + "T1.TB_FILMDATE,"
                    + "T1.DOC_DATE,"
                    + "T2.DENTISTREMARKDATE "           //-------------- 2004/04/21 add
                + "FROM "
                    + "( "
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SEMESTER            = '" +  param[1] + "' "
                            + "AND YEAR                = '" +  param[0] + "' "
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                    + ") T7 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T7.SCHREGNO) "
                    //健康診断詳細データ
                    + "INNER JOIN MEDEXAM_DET_DAT T1 ON (T1.YEAR = T7.YEAR AND T1.SCHREGNO = T7.SCHREGNO) "
                    //健康診断歯口腔データ//-------------- 2004/04/21 add
                    + "LEFT JOIN MEDEXAM_TOOTH_DAT T2 ON (T2.YEAR = T7.YEAR AND T2.SCHREGNO = T7.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T5 ON (T5.YEAR = T7.YEAR AND T5.SEMESTER = T7.SEMESTER AND T5.GRADE = T7.GRADE AND T5.HR_CLASS = T7.HR_CLASS) "
                + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai3 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai3 sql ok!");

            /** SVFフォームへデータをセット **/
            while( rs.next() ){
                ret=svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME1"      ,  rs.getString("NAME"));
                ret=svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME2"      ,  rs.getString("NAME"));
                //尿検査
                if(rs.getString("ALBUMINURIA1CD").equalsIgnoreCase("no") 
                    && rs.getString("URICSUGAR1CD").equalsIgnoreCase("no") 
                    && rs.getString("URICBLEED1CD").equalsIgnoreCase("no") 
                    && rs.getString("URICOTHERTEST").equalsIgnoreCase("no") 
                ){
                    ret=svf.VrsOut("CHECK1"  ,  "○");
                } else{
                    ret=svf.VrsOut("CHECK1"  ,  "");
                }
                //貧血検査
                if(rs.getFloat("HEMOGLOBIN")>=0.1){
                    ret=svf.VrsOut("CHECK2"  ,  "");
                } else{
                    ret=svf.VrsOut("CHECK2"  ,  "○");
                }
                //内科(校医)検診
                if(rs.getString("NUTRITIONCD").equalsIgnoreCase("no") 
                    || rs.getString("SPINERIBCD").equalsIgnoreCase("no") 
                    || rs.getString("NOSEDISEASECD").equalsIgnoreCase("no") 
                    || rs.getString("SKINDISEASECD").equalsIgnoreCase("no")
                ){
                    ret=svf.VrsOut("CHECK3"  ,  "○");
                } else{
                    ret=svf.VrsOut("CHECK3"  ,  "");
                }
                //歯科検診
//2004/04/21    if((rs.getString("DOC_DATE")=="") || (rs.getString("DOC_DATE")==null)){
                if((rs.getString("DENTISTREMARKDATE")=="" || rs.getString("DENTISTREMARKDATE")==null)){
                    ret=svf.VrsOut("CHECK4"  ,  "○");
                } else{
                    ret=svf.VrsOut("CHECK4"  ,  "");
                }
                //胸部レントゲン撮影
                if((rs.getString("TB_FILMDATE")=="") || (rs.getString("TB_FILMDATE")==null)){
                    ret=svf.VrsOut("CHECK5"  ,  "○");
                } else{
                    ret=svf.VrsOut("CHECK5"  ,  "");
                }
                //心電図検査
                if(rs.getString("HEART_MEDEXAM").equalsIgnoreCase("no")){
                    ret=svf.VrsOut("CHECK6"  ,  "○");
                } else{
                    ret=svf.VrsOut("CHECK6"  ,  "");
                }

                ret = svf.VrEndRecord();

                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai3 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai3 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai3 path!");

    }  //set_meisai3の括り


    /*------------------------------------*
     * ４）眼科検診のお知らせ             *
     *------------------------------------*/
    public void set_meisai4(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T7.GRADE,"
                    + "T7.HR_CLASS,"
                    + "T7.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T5.HR_NAME,"
                    + "T1.R_BAREVISION AS R_RAGAN,"
                    + "T1.L_BAREVISION AS L_RAGAN,"
                    + "T1.R_VISION AS R_KYOSEI,"
                    + "T1.L_VISION AS L_KYOSEI,"
                    + "T1.EYEDISEASECD,"
                    + "T2.NAME1 AS IJYO "
                + "FROM "
                    + "MEDEXAM_DET_DAT T1 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
                    + "INNER JOIN ( "
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SEMESTER            = '" +  param[1] + "' "
                            + "AND YEAR                = '" +  param[0] + "' "
                        /*クラス選択*/
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                    + ") T7 ON (T1.YEAR = T7.YEAR AND T1.SCHREGNO = T7.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T5 ON (T5.YEAR = T7.YEAR AND T5.SEMESTER = T7.SEMESTER AND T5.GRADE = T7.GRADE AND T5.HR_CLASS = T7.HR_CLASS) "
                    //目の疫病及び異常
// 2003/11/15
                    + "LEFT JOIN NAME_MST T2  ON (T2.NAMECD1  = 'F050' AND T2.NAMECD2  = T1.EYEDISEASECD) "
                + "WHERE "
                        //異常者のみ
                        + "T1.EYEDISEASECD >= '02' "
                    //矯正視力が0.9以下または裸眼視力が0.9以下
                    + "OR ((T1.R_VISION<>'A' AND T1.R_VISION<>'') "
                    + "OR (T1.L_VISION<>'A' AND T1.L_VISION<>'')"
                    + "OR (T1.R_BAREVISION<>'A' AND T1.R_BAREVISION<>'') "
                    + "OR (T1.L_BAREVISION<>'A' AND T1.L_BAREVISION<>'')) "
                + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai4 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai4 sql ok!");

            /** SVFフォームへデータをセット **/
            while( rs.next() ){
                ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME"      ,  rs.getString("NAME"));

                ret=svf.VrsOut("SIGHT_R1"  ,  rs.getString("R_RAGAN"));
                ret=svf.VrsOut("SIGHT_R2"  ,  rs.getString("R_KYOSEI"));
                ret=svf.VrsOut("SIGHT_L1"  ,  rs.getString("L_RAGAN"));
                ret=svf.VrsOut("SIGHT_L2"  ,  rs.getString("L_KYOSEI"));
                ret=svf.VrsOut("DISEASE"   ,  rs.getString("IJYO"));

                ret = svf.VrEndRecord();
                //データをクリア
                ret=svf.VrsOut("SIGHT_R1"  ,  "");
                ret=svf.VrsOut("SIGHT_R2"  ,  "");
                ret=svf.VrsOut("SIGHT_L1"  ,  "");
                ret=svf.VrsOut("SIGHT_L2"  ,  "");
                ret=svf.VrsOut("DISEASE"   ,  "");

                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai4 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai4 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai4 path!");

    }  //set_meisai4の括り


    /*------------------------------------*
     * ５）検診結果のお知らせ（歯・口腔） *
     *------------------------------------*/
    public void set_meisai5(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T7.GRADE,"
                    + "T7.HR_CLASS,"
                    + "T7.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T10.HR_NAME,"
                    + "T6.NAME1 AS SHIRETSU_AGO,"
                    + "T2.NAME1 AS SHIKO,"
                    + "T5.NAME1 AS SHINIKU,"
                    + "T1.BABYTOOTH + T1.ADULTTOOTH AS GENZAI_SU,"
                    + "T1.TREATEDBABYTOOTH AS KANSATSU_SU,"
                    + "T1.REMAINADULTTOOTH + T1.REMAINBABYTOOTH AS MISYOCHI_SU,"
                    + "T1.TREATEDADULTTOOTH + T1.TREATEDBABYTOOTH AS SYOCHI_SU,"
                    + "T1.LOSTADULTTOOTH AS SOSHITSU_SU,"
                    + "T3.NAME1 AS SONOTA,"
                    + "T4.NAME1 AS SHOKEN "
                + "FROM "
                    + "MEDEXAM_TOOTH_DAT T1 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
                    + "INNER JOIN ( "
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SEMESTER            = '" +  param[1] + "' "
                            + "AND YEAR                = '" +  param[0] + "' "
                        /*クラス選択*/
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                    + ") T7 ON (T1.YEAR = T7.YEAR AND T1.SCHREGNO = T7.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T10 ON (T10.YEAR = T7.YEAR AND T10.SEMESTER = T7.SEMESTER AND T10.GRADE = T7.GRADE AND T10.HR_CLASS = T7.HR_CLASS) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T2  ON (T2.NAMECD1  = 'F520' AND T2.NAMECD2  = T1.PLAQUECD) "
                    + "LEFT JOIN NAME_MST T3  ON (T3.NAMECD1  = 'F530' AND T3.NAMECD2  = T1.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'F540' AND T4.NAMECD2  = T1.DENTISTREMARKCD) "
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F510' AND T5.NAMECD2  = T1.GUMCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F510' AND T6.NAMECD2  = T1.JAWS_JOINTCD) "
                //+ "WHERE "
                        //+ "T1.YEAR  = '" +  param[0] + "' "
                    //異常者のみ
                    //+ "AND T1.OTHERDISEASECD >= '02' "
                    //+ "AND T1.OTHERDISEASECD <> '' "
                + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai5 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai5 sql ok!");

            /** SVFフォームへデータをセット **/
            int kanryo = 0;
            String kanryo1 = "";
            int count = 0;
            while( rs.next() ){
                count++;
                //log.debug("[KNJF040]set_meisai5 count="+count);
                
                ret=svf.VrsOut("HR_NAME1"      ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME1"      ,  rs.getString("NAME"));
                ret=svf.VrsOut("HR_NAME2"      ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME2"      ,  rs.getString("NAME"));

                ret=svf.VrsOut("TEETHLINE"  ,  rs.getString("SHIRETSU_AGO"));
                ret=svf.VrsOut("PLAQUE"     ,  rs.getString("SHIKO"));
                ret=svf.VrsOut("GUM"        ,  rs.getString("SHINIKU"));
                ret=svf.VrsOut("TEETHNO"    ,  rs.getString("GENZAI_SU"));
                ret=svf.VrsOut("DECAYED1"   ,  rs.getString("SYOCHI_SU"));
                ret=svf.VrsOut("DECAYED2"   ,  rs.getString("MISYOCHI_SU"));
                ret=svf.VrsOut("DECAYED3"   ,  rs.getString("KANSATSU_SU"));
                //未処置歯=0なら処置完了
                kanryo = rs.getInt("MISYOCHI_SU");
                kanryo1 = rs.getString("MISYOCHI_SU");
                //log.debug("[KNJF040]MISYOCHI_SU="+kanryo1);
                if(kanryo1!=null){
                    if(kanryo==0){
                        ret=svf.VrsOut("COMPLETION" ,  "完　了");
                    } else{
                        ret=svf.VrsOut("COMPLETION" ,  "");
                    }
                }

                ret=svf.VrsOut("LOST"       ,  rs.getString("SOSHITSU_SU"));
                ret=svf.VrsOut("OTHERS"     ,  rs.getString("SONOTA"));
                ret=svf.VrsOut("VIEWS"      ,  rs.getString("SHOKEN"));

                ret = svf.VrEndRecord();
                //データをクリア
                ret=svf.VrsOut("TEETHLINE"  ,  "");
                ret=svf.VrsOut("PLAQUE"     ,  "");
                ret=svf.VrsOut("GUM"        ,  "");
                ret=svf.VrsOut("TEETHNO"    ,  "");
                ret=svf.VrsOut("DECAYED1"   ,  "");
                ret=svf.VrsOut("DECAYED2"   ,  "");
                ret=svf.VrsOut("DECAYED3"   ,  "");
                ret=svf.VrsOut("COMPLETION" ,  "");
                ret=svf.VrsOut("LOST"       ,  "");
                ret=svf.VrsOut("OTHERS"     ,  "");
                ret=svf.VrsOut("VIEWS"      ,  "");
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai5 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai5 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai5 path!");

    }  //set_meisai5の括り


    /*------------------------------------*
     * ７）検診結果のお知らせ（一般）     *
     *------------------------------------*/
    public void set_meisai6(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T7.GRADE,"
                    + "T7.HR_CLASS,"
                    + "T7.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T10.HR_NAME,"
                    + "T9.NAME1 AS Naika,"
                    + "T2.NAME1 AS Sekichu_Kyokaku,"
                    + "T3.NAME1 AS Ganka,"
                    + "T4.NAME1 AS Jibika,"
                    + "T5.NAME1 AS Hifuka,"
                    + "T6.NAME1 AS Shinzou_Kenshin,"
                    + "T8.NAME1 AS Sonota "
                + "FROM "
                    + "MEDEXAM_DET_DAT T1 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
                    + "INNER JOIN ( "
                        + "SELECT "
                            + "SCHREGNO,GRADE,HR_CLASS,ATTENDNO,YEAR,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SEMESTER            = '" +  param[1] + "' "
                            + "AND YEAR                = '" +  param[0] + "' "
                        /*クラス選択*/
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                    + ") T7 ON (T1.YEAR = T7.YEAR AND T1.SCHREGNO = T7.SCHREGNO) "
                    + "INNER JOIN SCHREG_REGD_HDAT T10 ON (T10.YEAR = T7.YEAR AND T10.SEMESTER = T7.SEMESTER AND T10.GRADE = T7.GRADE AND T10.HR_CLASS = T7.HR_CLASS) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T2 ON (T2.NAMECD1 = 'F040' AND T2.NAMECD2 >= '02' AND T2.NAMECD2 = T1.SPINERIBCD) "
                    + "LEFT JOIN NAME_MST T3 ON (T3.NAMECD1 = 'F050' AND T3.NAMECD2 >= '02' AND T3.NAMECD2 = T1.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T4 ON (T4.NAMECD1 = 'F060' AND T4.NAMECD2 >= '02' AND T4.NAMECD2 = T1.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T5 ON (T5.NAMECD1 = 'F070' AND T5.NAMECD2 >= '02' AND T5.NAMECD2 = T1.SKINDISEASECD) "
                    + "LEFT JOIN NAME_MST T6 ON (T6.NAMECD1 = 'F090' AND T6.NAMECD2 >= '02' AND T6.NAMECD2 = T1.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T8 ON (T8.NAMECD1 = 'F140' AND T8.NAMECD2 >= '02' AND T8.NAMECD2 = T1.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T9 ON (T9.NAMECD1 = 'F030' AND T9.NAMECD2 >= '02' AND T9.NAMECD2 = T1.NUTRITIONCD) "
                + "WHERE "
                //異常のある生徒のみ
                        + "T1.SPINERIBCD       >= '02' "
                    + "OR  T1.EYEDISEASECD     >= '02' "
                    + "OR  T1.NOSEDISEASECD    >= '02' "
                    + "OR  T1.SKINDISEASECD    >= '02' "
                    + "OR  T1.HEARTDISEASECD   >= '02' "
                    + "OR  T1.OTHERDISEASECD   >= '02' "
                    + "OR  T1.NUTRITIONCD      >= '02' "
                + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai6 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai6 sql ok!");

            /** SVFフォームへデータをセット **/
            while( rs.next() ){
                ret=svf.VrsOut("HR_NAME"      ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                //１人で１枚にまとめて出力
                if(param[4].equalsIgnoreCase("1")){
                    ret=svf.VrsOut("RESULT1_1"  ,  rs.getString("Naika"));            //内科
                    ret=svf.VrsOut("RESULT4_1"  ,  rs.getString("Hifuka"));             //皮膚科
                    ret=svf.VrsOut("RESULT2_1"  ,  rs.getString("Ganka"));              //眼科
                    ret=svf.VrsOut("RESULT5_1"  ,  rs.getString("Sekichu_Kyokaku"));    //胸郭・脊柱
                    ret=svf.VrsOut("RESULT3_1"  ,  rs.getString("Jibika"));             //耳鼻科
                    ret=svf.VrsOut("RESULT6"    ,  rs.getString("Sonota"));             //その他
                    ret=svf.VrsOut("RESULT7"    ,  rs.getString("Shinzou_Kenshin"));    //心臓検診

                    ret = svf.VrEndRecord();

                    ret=svf.VrsOut("RESULT1_1"  ,  "");
                    ret=svf.VrsOut("RESULT4_1"  ,  "");
                    ret=svf.VrsOut("RESULT2_1"  ,  "");
                    ret=svf.VrsOut("RESULT5_1"  ,  "");
                    ret=svf.VrsOut("RESULT3_1"  ,  "");
                    ret=svf.VrsOut("RESULT6"    ,  "");
                    ret=svf.VrsOut("RESULT7"    ,  "");
                }
                //１人で各種類ごとに出力
                if(param[4].equalsIgnoreCase("2")){
                    if(rs.getString("Naika")!=null){
                        ret=svf.VrsOut("RESULT1_1"  ,  rs.getString("Naika"));            //内科
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT1_1"  ,  "");
                    }
                    if(rs.getString("Hifuka")!=null){
                        ret=svf.VrsOut("RESULT4_1"  ,  rs.getString("Hifuka"));             //皮膚科
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT4_1"  ,  "");
                    }
                    if(rs.getString("Ganka")!=null){
                        ret=svf.VrsOut("RESULT2_1"  ,  rs.getString("Ganka"));              //眼科
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT2_1"  ,  "");
                    }
                    if(rs.getString("Sekichu_Kyokaku")!=null){
                        ret=svf.VrsOut("RESULT5_1"  ,  rs.getString("Sekichu_Kyokaku"));    //胸郭・脊柱
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT5_1"  ,  "");
                    }
                    if(rs.getString("Jibika")!=null){
                        ret=svf.VrsOut("RESULT3_1"  ,  rs.getString("Jibika"));             //耳鼻科
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT3_1"  ,  "");
                    }
                    if(rs.getString("Sonota")!=null){
                        ret=svf.VrsOut("RESULT6"    ,  rs.getString("Sonota"));             //その他
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT6"  ,  "");
                    }
                    if(rs.getString("Shinzou_Kenshin")!=null){
                        ret=svf.VrsOut("RESULT7"    ,  rs.getString("Shinzou_Kenshin"));    //心臓検診
                        ret = svf.VrEndRecord();
                        ret=svf.VrsOut("RESULT7"  ,  "");
                    }
                }

                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai6 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai6 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai6 path!");

    }  //set_meisai6の括り


    /*------------------------------------*
     * ６）定期健康診断結果 det_tooth     *
     *------------------------------------*/
    public void set_meisai7(String param[],String schregno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T4.DATE1,"
                    + "T2.HEIGHT,"
                    + "T2.WEIGHT,"
                    + "T2.SITHEIGHT,"
                    + "CASE "
                    + "WHEN VALUE(T2.HEIGHT,0) > 0 THEN DECIMAL(ROUND(T2.WEIGHT/T2.HEIGHT/T2.HEIGHT*10000,1),4,1) "
                    + "END AS BMI,"
                    + "T2.R_BAREVISION,"
                    + "T2.R_VISION,"
                    + "T2.L_BAREVISION,"
                    + "T2.L_VISION,"
                    + "T5.NAME1 AS Meekibyo_Ijyo,"
                    + "T2.R_EAR_DB,"
                    + "T18.NAME1 AS Chouryoku_Migi,"
                    + "T2.L_EAR_DB,"
                    + "T19.NAME1 AS Chouryoku_Hidari,"
                    + "T6.NAME1 AS Jibi_Shikkan,"
                    + "T7.NAME1 AS Hifu_Shikkan,"
                    + "T8.NAME1 AS Byomei_Kekkaku,"
                    + "T9.NAME1 AS Shidoukubun_Kekkaku,"
                    + "T10.NAME1 AS Kensa_Shinzo,"
                    + "T11.NAME1 AS Ekibyo_Shinzo,"
                    + "CASE T20.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T20.NAME1 "
                    + "END AS Nyou1_Tanpaku,"
                    + "CASE T21.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T21.NAME1 "
                    + "END AS Nyou1_Tou,"
                    + "CASE T22.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T22.NAME1 "
                    + "END AS Nyou1_Senketsu,"
                    + "CASE T23.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T23.NAME1 "
                    + "END AS Nyou2_Tanpaku,"
                    + "CASE T24.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T24.NAME1 "
                    + "END AS Nyou2_Tou,"
                    + "CASE T25.NAME1 "
                    + "WHEN '-' THEN '?' "
                    + "ELSE T25.NAME1 "
                    + "END AS Nyou2_Senketsu,"
                    + "T12.NAME1 AS Sonota_Ekibyou,"
                    + "T2.REMARK,"
                    + "T13.NAME1 AS Shiretsu_Ago,"
                    + "T14.NAME1 AS Shikou,"
                    + "T15.NAME1 AS Shiniku,"
                    + "T3.BABYTOOTH + T3.ADULTTOOTH AS Genzai,"
// 2003/11/14
//                  + "T3.TREATEDADULTTOOTH AS Syochi,"
//                  + "T3.REMAINADULTTOOTH AS Misyochi,"
                    + "T3.TREATEDADULTTOOTH + T3.TREATEDBABYTOOTH AS Syochi,"
                    + "T3.REMAINADULTTOOTH + T3.REMAINBABYTOOTH AS Misyochi,"
                    + "CASE T3.REMAINADULTTOOTH "
                    + "WHEN 0 THEN '完　　了' "
                    + "ELSE '' "
                    + "END AS Syochi_Syuryo,"
                    + "T3.TREATEDBABYTOOTH AS Kansatsu,"
                    + "T3.LOSTADULTTOOTH AS Soushitsu,"
                    + "T16.NAME1 AS Sonota_Shishuu,"
                    + "T17.NAME1 AS Shikai_Syoken "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "YEAR,SCHREGNO,"
                            + "CASE WHEN VALUE(CHAR(DATE),'0') <> '0' THEN "
// 2003/11/12
//                          + "SUBSTR(CHAR(DATE), 1, 4) || SUBSTR(CHAR(DATE), 6, 2) || SUBSTR(CHAR(DATE), 9, 2) "
                            + "CHAR(DATE) "
                            + "ELSE 'no' END AS DATE1 "
                        + "FROM "
                            + "MEDEXAM_HDAT "
                        + "WHERE "
                                + "YEAR     = '" +  param[0] + "' "
                            + "AND SCHREGNO        = '" +  schregno_info + "' "
                    + ") T4 "
                    + "LEFT JOIN ("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_DET_DAT "
                        + "WHERE "
                                + "YEAR     = '" +  param[0] + "' "
                            + "AND SCHREGNO        = '" +  schregno_info + "' "
                    + ") T2 ON (T2.YEAR = T4.YEAR AND T2.SCHREGNO = T4.SCHREGNO) "
                    + "LEFT JOIN ("
                        + "SELECT "
                            + "* "
                        + "FROM "
                            + "MEDEXAM_TOOTH_DAT "
                        + "WHERE "
                                + "YEAR     = '" +  param[0] + "' "
                            + "AND SCHREGNO        = '" +  schregno_info + "' "
                    + ") T3 ON (T4.YEAR = T3.YEAR AND T4.SCHREGNO = T3.SCHREGNO) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T5  ON (T5.NAMECD1  = 'F050' AND T5.NAMECD2  = T2.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F060' AND T6.NAMECD2  = T2.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T7  ON (T7.NAMECD1  = 'F070' AND T7.NAMECD2  = T2.SKINDISEASECD) "
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'F120' AND T8.NAMECD2  = T2.TB_NAMECD) "
                    + "LEFT JOIN NAME_MST T9  ON (T9.NAMECD1  = 'F130' AND T9.NAMECD2  = T2.TB_ADVISECD) "
                    + "LEFT JOIN NAME_MST T10 ON (T10.NAMECD1 = 'F080' AND T10.NAMECD2 = T2.HEART_MEDEXAM) "
                    + "LEFT JOIN NAME_MST T11 ON (T11.NAMECD1 = 'F090' AND T11.NAMECD2 = T2.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T12 ON (T12.NAMECD1 = 'F140' AND T12.NAMECD2 = T2.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T18 ON (T18.NAMECD1 = 'F010' AND T18.NAMECD2 = T2.R_EAR) "
                    + "LEFT JOIN NAME_MST T19 ON (T19.NAMECD1 = 'F010' AND T19.NAMECD2 = T2.L_EAR) "
                    + "LEFT JOIN NAME_MST T20 ON (T20.NAMECD1 = 'F020' AND T20.NAMECD2 = T2.ALBUMINURIA1CD) "
                    + "LEFT JOIN NAME_MST T21 ON (T21.NAMECD1 = 'F020' AND T21.NAMECD2 = T2.URICSUGAR1CD) "
                    + "LEFT JOIN NAME_MST T22 ON (T22.NAMECD1 = 'F020' AND T22.NAMECD2 = T2.URICBLEED1CD) "
                    + "LEFT JOIN NAME_MST T23 ON (T23.NAMECD1 = 'F020' AND T23.NAMECD2 = T2.ALBUMINURIA2CD) "
                    + "LEFT JOIN NAME_MST T24 ON (T24.NAMECD1 = 'F020' AND T24.NAMECD2 = T2.URICSUGAR2CD) "
                    + "LEFT JOIN NAME_MST T25 ON (T25.NAMECD1 = 'F020' AND T25.NAMECD2 = T2.URICBLEED2CD) "
                    + "LEFT JOIN NAME_MST T13 ON (T13.NAMECD1 = 'F510' AND T13.NAMECD2 = T3.JAWS_JOINTCD) "
                    + "LEFT JOIN NAME_MST T14 ON (T14.NAMECD1 = 'F520' AND T14.NAMECD2 = T3.PLAQUECD) "
                    + "LEFT JOIN NAME_MST T15 ON (T15.NAMECD1 = 'F510' AND T15.NAMECD2 = T3.GUMCD) "
                    + "LEFT JOIN NAME_MST T16 ON (T16.NAMECD1 = 'F530' AND T16.NAMECD2 = T3.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T17 ON (T17.NAMECD1 = 'F540' AND T17.NAMECD2 = T3.DENTISTREMARKCD) ";


            //log.debug("[KNJF040]set_meisai7 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            /** SVFフォームへデータをセット **/

            while( rs.next() ){
                //ヘッダ
                if(rs.getString("DATE1")!="no")
// 2003/11/12
//                  ret=svf.VrsOut("YMD2"       ,  rs.getString("DATE1"));
                    ret=svf.VrsOut("YMD2"       , KNJ_EditDate.h_format_JP(rs.getString("DATE1")));

                //詳細
                ret=svf.VrsOut("HEIGHT"     ,  rs.getString("HEIGHT"));
                ret=svf.VrsOut("WEIGHT"     ,  rs.getString("WEIGHT"));
                ret=svf.VrsOut("BMI"        ,  rs.getString("BMI"));
                ret=svf.VrsOut("SITHEIGHT"  ,  rs.getString("SITHEIGHT"));
                ret=svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));
                ret=svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION"));
                ret=svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));
                ret=svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION"));
                ret=svf.VrsOut("EYES"       ,  rs.getString("Meekibyo_Ijyo"));
                ret=svf.VrsOut("HEARING_R1"     ,  rs.getString("R_EAR_DB"));
                ret=svf.VrsOut("HEARING_R2"     ,  rs.getString("Chouryoku_Migi"));
                ret=svf.VrsOut("HEARING_L1"     ,  rs.getString("L_EAR_DB"));
                ret=svf.VrsOut("HEARING_L2"     ,  rs.getString("Chouryoku_Hidari"));
                ret=svf.VrsOut("NOSE"       ,  rs.getString("Jibi_Shikkan"));
                ret=svf.VrsOut("SKIN"       ,  rs.getString("Hifu_Shikkan"));
                ret=svf.VrsOut("DISEASE"    ,  rs.getString("Byomei_Kekkaku"));
                ret=svf.VrsOut("GUIDE"      ,  rs.getString("Shidoukubun_Kekkaku"));
                ret=svf.VrsOut("INSPECTION" ,  rs.getString("Kensa_Shinzo"));
                ret=svf.VrsOut("UNUSUAL"    ,  rs.getString("Ekibyo_Shinzo"));
                ret=svf.VrsOut("ALBUMIN1"       ,  rs.getString("Nyou1_Tanpaku"));
                ret=svf.VrsOut("SACCHARIDE1"    ,  rs.getString("Nyou1_Tou"));
                ret=svf.VrsOut("BLOOD1"         ,  rs.getString("Nyou1_Senketsu"));
                ret=svf.VrsOut("ALBUMIN2"       ,  rs.getString("Nyou2_Tanpaku"));
                ret=svf.VrsOut("SACCHARIDE2"    ,  rs.getString("Nyou2_Tou"));
                ret=svf.VrsOut("BLOOD2"         ,  rs.getString("Nyou2_Senketsu"));
                ret=svf.VrsOut("OTHERS1"    ,  rs.getString("Sonota_Ekibyou"));
                ret=svf.VrsOut("NOTE1"      ,  rs.getString("REMARK"));

                //歯
                ret=svf.VrsOut("TEETHLINE"  ,  rs.getString("Shiretsu_Ago"));
                ret=svf.VrsOut("PLAQUE"     ,  rs.getString("Shikou"));
                ret=svf.VrsOut("GUM"        ,  rs.getString("Shiniku"));
                ret=svf.VrsOut("TEETHNO"    ,  rs.getString("Genzai"));
                ret=svf.VrsOut("DECAYED1"   ,  rs.getString("Syochi"));
                ret=svf.VrsOut("DECAYED2"   ,  rs.getString("Misyochi"));
                ret=svf.VrsOut("DECAYED3"   ,  rs.getString("Kansatsu"));
                ret=svf.VrsOut("DECAYED4"   ,  rs.getString("Syochi_Syuryo"));
                ret=svf.VrsOut("LOST"       ,  rs.getString("Soushitsu"));
                ret=svf.VrsOut("OTHERS2"    ,  rs.getString("Sonota_Shishuu"));
                ret=svf.VrsOut("VIEWS"      ,  rs.getString("Shikai_Syoken"));

                ret = svf.VrEndRecord();

                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            //log.debug("[KNJF040]set_meisai7 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai7 read error!", ex);
        }

    }  //set_meisai7の括り


    /*------------------------------------*
     * ８）内科検診所見あり生徒の名簿     *
     *------------------------------------*/
    public void set_meisai8(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T1.GRADE,"
                    + "T1.HR_CLASS,"
                    + "T1.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T5.HR_NAME,"
                    + "T2.HEARTDISEASECD,"
                    + "T7.NAME1 AS Shinzou_Kenshin,"
                    + "T2.NUTRITIONCD,"
                    + "T8.NAME1 AS Naika,"
                    + "T2.SPINERIBCD,"
                    + "T9.NAME1 AS Sekichu_Kyokaku,"
                    + "T2.EYEDISEASECD,"
                    + "T10.NAME1 AS Ganka,"
                    + "T2.NOSEDISEASECD,"
                    + "T11.NAME1 AS Jibika,"
                    + "T2.SKINDISEASECD,"
                    + "T12.NAME1 AS Hifuka,"
                    + "T2.OTHERDISEASECD,"
                    + "T13.NAME1 AS Sonota,"
                    + "T2.NUTRITION_RESULT,"
                    + "T2.EYEDISEASE_RESULT,"
                    + "T2.SKINDISEASE_RESULT,"
                    + "T2.SPINERIB_RESULT,"
                    + "T2.NOSEDISEASE_RESULT,"
                    + "T2.OTHERDISEASE_RESULT,"
                    + "T2.HEARTDISEASE_RESULT "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "YEAR,SCHREGNO,NUTRITIONCD,SPINERIBCD,EYEDISEASECD,"
                            + "NOSEDISEASECD,SKINDISEASECD,HEARTDISEASECD,OTHERDISEASECD,"
                            + "NUTRITION_RESULT,EYEDISEASE_RESULT,SKINDISEASE_RESULT,"
                            + "SPINERIB_RESULT,NOSEDISEASE_RESULT,OTHERDISEASE_RESULT,HEARTDISEASE_RESULT "
                        + "FROM "
                            + "MEDEXAM_DET_DAT "
                        + "WHERE "
                                + "YEAR     = '" +  param[0] + "' "
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                            + "AND (SPINERIBCD     >= '02' "
                            + "OR  NUTRITIONCD     >= '02' "
                            + "OR  EYEDISEASECD    >= '02' "
                            + "OR  NOSEDISEASECD   >= '02' "
                            + "OR  SKINDISEASECD   >= '02' "
                            + "OR  HEARTDISEASECD  >= '02' "
                            + "OR  OTHERDISEASECD  >= '02') "
                    + ") T2 "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T2.SCHREGNO) "
                    + "INNER JOIN ("
                        + "SELECT "
                            + "SCHREGNO,YEAR,GRADE,HR_CLASS,ATTENDNO,SEMESTER "
                        + "FROM "
                            + "SCHREG_REGD_DAT "
                        + "WHERE "
                                + "SEMESTER = '" +  param[1] + "'"
                            + "AND YEAR     = '" +  param[0] + "' "
                            + "AND SCHREGNO   IN "  +  param[2] + " "
                    + ") T1 ON (T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR) "
                    + "INNER JOIN SCHREG_REGD_HDAT T5 ON (T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER AND T5.GRADE = T1.GRADE AND T5.HR_CLASS = T1.HR_CLASS) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T7  ON (T7.NAMECD1  = 'F090' AND T7.NAMECD2  >= '02' AND T7.NAMECD2  = T2.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'F030' AND T8.NAMECD2  >= '02' AND T8.NAMECD2  = T2.NUTRITIONCD) "
                    + "LEFT JOIN NAME_MST T9  ON (T9.NAMECD1  = 'F040' AND T9.NAMECD2  >= '02' AND T9.NAMECD2  = T2.SPINERIBCD) "
                    + "LEFT JOIN NAME_MST T10 ON (T10.NAMECD1 = 'F050' AND T10.NAMECD2 >= '02' AND T10.NAMECD2 = T2.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T11 ON (T11.NAMECD1 = 'F060' AND T11.NAMECD2 >= '02' AND T11.NAMECD2 = T2.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T13 ON (T13.NAMECD1 = 'F140' AND T13.NAMECD2 >= '02' AND T13.NAMECD2 = T2.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T12 ON (T12.NAMECD1 = 'F070' AND T12.NAMECD2 IN ('02','03') AND T12.NAMECD2 = T2.SKINDISEASECD) "

                + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai8 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai8 sql ok!");

            /** SVFフォームへデータをセット **/
            int count = 0;
            while( rs.next() ){
                ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    //内科
                    if(rs.getString("Naika")!=null){
                        ret=svf.VrsOut("CHECKUP"  ,  rs.getString("Naika"));                //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("NUTRITION_RESULT"));      //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //皮膚科
                    if(rs.getString("Hifuka")!=null){
                        ret=svf.VrsOut("CHECKUP"  ,  rs.getString("Hifuka"));               //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("SKINDISEASE_RESULT"));    //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //眼科
                    if(rs.getString("Ganka")!=null){
                        ret=svf.VrsOut("CHECKUP"  ,  rs.getString("Ganka"));                //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("EYEDISEASE_RESULT"));     //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //胸郭・脊柱
                    if(rs.getString("Sekichu_Kyokaku")!=null){
                        ret=svf.VrsOut("CHECKUP"  ,  rs.getString("Sekichu_Kyokaku"));      //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("SPINERIB_RESULT"));       //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //耳鼻科
                    if(rs.getString("Jibika")!=null){
                        ret=svf.VrsOut("CHECKUP"  ,  rs.getString("Jibika"));               //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("NOSEDISEASE_RESULT"));    //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //その他
                    if(rs.getString("Sonota")!=null){
                        ret=svf.VrsOut("CHECKUP"    ,  rs.getString("Sonota"));             //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("OTHERDISEASE_RESULT"));   //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                    //心臓検診
                    if(rs.getString("Shinzou_Kenshin")!=null){
                        ret=svf.VrsOut("CHECKUP"    ,  rs.getString("Shinzou_Kenshin"));    //内科検診
                        ret=svf.VrsOut("RESULT"  ,  rs.getString("HEARTDISEASE_RESULT"));   //病院受診結果
                        ret = svf.VrEndRecord();
                        count++;
                    }
                if(count==25){
                    ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    ret=svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }


                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai8 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai8 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai8 path!");

    }  //set_meisai8の括り


    /*------------------------------------*
     * ９）定期健康診断異常者一覧表       *
     *------------------------------------*/
    public void set_meisai9(String param[])
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T1.GRADE,"
                    + "T1.HR_CLASS,"
                    + "T1.ATTENDNO,"
                    + "T0.NAME_SHOW AS NAME,"
                    + "T5.HR_NAME,"
                    + "CASE T2.R_BAREVISION "
                    + "WHEN 'A' THEN 'A' "
                    + "WHEN 'B' THEN 'B' "
                    + "WHEN 'C' THEN 'C' "
                    + "WHEN 'D' THEN 'D' "
                    + "ELSE '' "
                    + "END AS R_BAREVISION_CHECK,"
                    + "CASE T2.R_VISION "
                    + "WHEN 'A' THEN 'A' "
                    + "WHEN 'B' THEN 'B' "
                    + "WHEN 'C' THEN 'C' "
                    + "WHEN 'D' THEN 'D' "
                    + "ELSE '' "
                    + "END AS R_VISION_CHECK,"
                    + "CASE T2.L_BAREVISION "
                    + "WHEN 'A' THEN 'A' "
                    + "WHEN 'B' THEN 'B' "
                    + "WHEN 'C' THEN 'C' "
                    + "WHEN 'D' THEN 'D' "
                    + "ELSE '' "
                    + "END AS L_BAREVISION_CHECK,"
                    + "CASE T2.L_VISION "
                    + "WHEN 'A' THEN 'A' "
                    + "WHEN 'B' THEN 'B' "
                    + "WHEN 'C' THEN 'C' "
                    + "WHEN 'D' THEN 'D' "
                    + "ELSE '' "
                    + "END AS L_VISION_CHECK,"
                    + "CASE "
                    + "WHEN T2.ALBUMINURIA1CD > '01' THEN '○' "
                    + "ELSE '' "
                    + "END AS Nyou1_Tanpaku,"
                    + "CASE "
                    + "WHEN T2.URICSUGAR1CD > '01' THEN '○' "
                    + "ELSE '' "
                    + "END AS Nyou1_Tou,"
                    + "CASE "
                    + "WHEN T2.URICBLEED1CD > '01' THEN '○' "
                    + "ELSE '' "
                    + "END AS Nyou1_Senketsu,"
                    + "CASE "
                    + "WHEN T4.NAME1 IS NOT NULL THEN SUBSTR(T4.NAME1,7) "
                    + "ELSE '' "
                    + "END AS Hinketsu,"
                    + "CASE "
                    + "WHEN T2.TB_REMARKCD = '02' THEN 'on' "
                    + "ELSE '' "
                    + "END AS Syoken_Kekkaku,"
                    + "T6.NAME1 AS Kensa_Shinzo,"
                    + "T7.NAME1 AS Ekibyo_Shinzo,"
                    + "T8.NAME1 AS Eiyo_Jyotai,"
                    + "T9.NAME1 AS Sekichu_Kyokaku,"
                    + "T10.NAME1 AS Meekibyo_Ijyo,"
                    + "T11.NAME1 AS Jibi_Shikkan,"
                    + "T12.NAME1 AS Hifu_Shikkan,"
                    + "T13.NAME1 AS Sonota,"
                    + "T3.REMAINBABYTOOTH AS Misyochi_Nyushi,"
                    + "CASE "
                    + "WHEN T3.REMAINBABYTOOTH >= 1 THEN 'on' "
                    + "ELSE '' "
                    + "END AS Misyochi_Nyushi_Check,"
                    + "T3.REMAINADULTTOOTH AS Misyochi_Eikyushi,"
                    + "CASE "
                    + "WHEN T3.REMAINADULTTOOTH >= 1 THEN 'on' "
                    + "ELSE '' "
                    + "END AS Misyochi_Eikyushi_Check,"
                    + "T14.NAME1 AS Shiretsu_Ago,"
                    + "T15.NAME1 AS Shikou,"
                    + "T16.NAME1 AS Shiniku,"
                    + "T17.NAME1 AS Sonota_Ha "
                + "FROM "
                    + "SCHREG_REGD_DAT T1 "
                    + "INNER JOIN SCHREG_REGD_HDAT T5 ON (T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER AND T5.GRADE = T1.GRADE AND T5.HR_CLASS = T1.HR_CLASS) "
                    + "INNER JOIN SCHREG_BASE_MST T0 ON (T0.SCHREGNO = T1.SCHREGNO) "
                    + "LEFT JOIN MEDEXAM_DET_DAT T2 ON (T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR) "
                    + "LEFT JOIN MEDEXAM_TOOTH_DAT T3 ON (T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR) "
// 2003/11/15
                    + "LEFT JOIN NAME_MST T4  ON (T4.NAMECD1  = 'F150' AND T4.NAMECD2  IN ('04','05') AND T4.NAMECD2  = T2.TREATCD) "
                    + "LEFT JOIN NAME_MST T6  ON (T6.NAMECD1  = 'F080' AND T6.NAMECD2  >= '02' AND T6.NAMECD2  = T2.HEART_MEDEXAM) "
                    + "LEFT JOIN NAME_MST T7  ON (T7.NAMECD1  = 'F090' AND T7.NAMECD2  >= '02' AND T7.NAMECD2  = T2.HEARTDISEASECD) "
                    + "LEFT JOIN NAME_MST T8  ON (T8.NAMECD1  = 'F030' AND T8.NAMECD2  >= '02' AND T8.NAMECD2  = T2.NUTRITIONCD) "
                    + "LEFT JOIN NAME_MST T9  ON (T9.NAMECD1  = 'F040' AND T9.NAMECD2  >= '02' AND T9.NAMECD2  = T2.SPINERIBCD) "
                    + "LEFT JOIN NAME_MST T10 ON (T10.NAMECD1 = 'F050' AND T10.NAMECD2 >= '02' AND T10.NAMECD2 = T2.EYEDISEASECD) "
                    + "LEFT JOIN NAME_MST T11 ON (T11.NAMECD1 = 'F060' AND T11.NAMECD2 >= '02' AND T11.NAMECD2 = T2.NOSEDISEASECD) "
                    + "LEFT JOIN NAME_MST T12 ON (T12.NAMECD1 = 'F070' AND T12.NAMECD2 >= '02' AND T12.NAMECD2 = T2.SKINDISEASECD) "
                    + "LEFT JOIN NAME_MST T13 ON (T13.NAMECD1 = 'F140' AND T13.NAMECD2 >= '02' AND T13.NAMECD2 = T2.OTHERDISEASECD) "
                    + "LEFT JOIN NAME_MST T14 ON (T14.NAMECD1 = 'F510' AND T14.NAMECD2 >= '02' AND T14.NAMECD2 = T3.JAWS_JOINTCD) "
                    + "LEFT JOIN NAME_MST T15 ON (T15.NAMECD1 = 'F520' AND T15.NAMECD2 >= '02' AND T15.NAMECD2 = T3.PLAQUECD) "
                    + "LEFT JOIN NAME_MST T16 ON (T16.NAMECD1 = 'F510' AND T16.NAMECD2 >= '02' AND T16.NAMECD2 = T3.GUMCD) "
                    + "LEFT JOIN NAME_MST T17 ON (T17.NAMECD1 = 'F530' AND T17.NAMECD2 >= '02' AND T17.NAMECD2 = T3.OTHERDISEASECD) "

                + "WHERE "
                        + "T1.SEMESTER = '" +  param[1] + "' "
                    + "AND T1.YEAR     = '" +  param[0] + "' "
                    + "AND T1.SCHREGNO   IN " +  param[2] + " ";
        // 02．異常者全部
        if(param[20].equalsIgnoreCase("02")){
                    sql = sql + "AND ((T2.R_BAREVISION <> 'A' OR T2.L_BAREVISION <> 'A') "
                    + "OR (T2.ALBUMINURIA1CD >= '02' OR T2.URICSUGAR1CD >= '02' OR T2.URICBLEED1CD >= '02') "
                    + "OR (T2.TREATCD = '05') "
                    + "OR (T2.TREATCD = '04') "
                    + "OR (T2.TB_REMARKCD = '02') "
                    + "OR (T2.HEART_MEDEXAM >= '02') "
                    + "OR (T2.HEARTDISEASECD >= '02') "
                    + "OR (T2.NUTRITIONCD >= '02') "
                    + "OR (T2.SPINERIBCD >= '02') "
                    + "OR (T2.EYEDISEASECD >= '02') "
                    + "OR (T2.NOSEDISEASECD >= '02') "
                    + "OR (T2.SKINDISEASECD >= '02') "
                    + "OR (T2.OTHERDISEASECD >= '02')) ";
        }
        // 03．視力 0.9?0.7
        if(param[20].equalsIgnoreCase("03")){
                    sql = sql + "AND (T2.R_BAREVISION = 'B' OR T2.L_BAREVISION = 'B') ";
        }
        // 04．視力 0.6?0.3
        if(param[20].equalsIgnoreCase("04")){
                    sql = sql + "AND (T2.R_BAREVISION = 'C' OR T2.L_BAREVISION = 'C') ";
        }
        // 05．視力 0.2以下
        if(param[20].equalsIgnoreCase("05")){
                    sql = sql + "AND ((T2.R_BAREVISION = 'D' OR T2.R_BAREVISION = '') "
                    + "OR (T2.L_BAREVISION = 'D' OR T2.L_BAREVISION = '')) ";
        }
        // 06．尿　陽性者
        if(param[20].equalsIgnoreCase("06")){
                    sql = sql + "AND (T2.ALBUMINURIA1CD >= '02' OR T2.URICSUGAR1CD >= '02' OR T2.URICBLEED1CD >= '02') ";
        }
        // 07．貧血　要食事指導
        if(param[20].equalsIgnoreCase("07")){
                    sql = sql + "AND (T2.TREATCD = '05') ";
        }
        // 08．貧血　要治療
        if(param[20].equalsIgnoreCase("08")){
                    sql = sql + "AND (T2.TREATCD = '04') ";
        }
        // 09．結核　要再検者
        if(param[20].equalsIgnoreCase("09")){
                    sql = sql + "AND T2.TB_REMARKCD = '02' ";
        }
        // 10．心臓　要再検者
        if(param[20].equalsIgnoreCase("10")){
                    sql = sql + "AND ((T2.HEART_MEDEXAM >= '02') "
                    + "OR (T2.HEARTDISEASECD >= '02')) ";
        }
        // 11．栄養状態異常
        if(param[20].equalsIgnoreCase("11")){
                    sql = sql + "AND T2.NUTRITIONCD >= '02' ";
        }
        // 12．脊柱・胸郭異常
        if(param[20].equalsIgnoreCase("12")){
                    sql = sql + "AND T2.SPINERIBCD >= '02' ";
        }
        // 13．目の疫病及び異常
        if(param[20].equalsIgnoreCase("13")){
                    sql = sql + "AND T2.EYEDISEASECD >= '02' ";
        }
        // 14．耳鼻異常
        if(param[20].equalsIgnoreCase("14")){
                    sql = sql + "AND T2.NOSEDISEASECD >= '02' ";
        }
        // 15．皮膚疾患異常
        if(param[20].equalsIgnoreCase("15")){
                    sql = sql + "AND T2.SKINDISEASECD >= '02' ";
        }
        // 16．その他の疫病及び異常
        if(param[20].equalsIgnoreCase("16")){
                    sql = sql + "AND T2.OTHERDISEASECD >= '02' ";
        }
        // 17．ローレル指数　160以上
        // 18．ローレル指数　159以上

        // 02．'異常者全部
        if(param[21].equalsIgnoreCase("02")){
                    sql = sql + "AND (T3.REMAINBABYTOOTH >= 1 "
                    + "OR T3.REMAINADULTTOOTH >= 1 "
                    + "OR T3.JAWS_JOINTCD >= '02' "
                    + "OR T3.PLAQUECD >= '02' "
                    + "OR T3.GUMCD >= '02' "
                    + "OR T3.OTHERDISEASECD >= '02') ";
        }
        // 03．'未処置
        if(param[21].equalsIgnoreCase("03")){
                    sql = sql + "AND (T3.REMAINBABYTOOTH >= 1 OR T3.REMAINADULTTOOTH >= 1) ";
        }
        // 04．'歯列・咬合・歯顎関節
        if(param[21].equalsIgnoreCase("04")){
                    sql = sql + "AND T3.JAWS_JOINTCD >= '02' ";
        }
        // 05．'歯列・咬合・歯顎関節
        if(param[21].equalsIgnoreCase("05")){
                    sql = sql + "AND T3.JAWS_JOINTCD >= '02' ";
        }
        // 06．'歯垢状態
        if(param[21].equalsIgnoreCase("06")){
                    sql = sql + "AND T3.PLAQUECD >= '02' ";
        }
        // 07．'歯肉状態
        if(param[21].equalsIgnoreCase("07")){
                    sql = sql + "AND T3.GUMCD >= '02' ";
        }
        // 08．'歯その他疾病及異常
        if(param[21].equalsIgnoreCase("08")){
                    sql = sql + "AND T3.OTHERDISEASECD >= '02' ";
        }

                sql = sql + "ORDER BY 2, 3, 4 ";

            //log.debug("[KNJF040]set_meisai9 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_meisai9 sql ok!");

            /** SVFフォームへデータをセット **/
            int count = 0;
            while( rs.next() ){
                count++;
                //log.debug("[KNJF040]set_meisai9 count="+count);
                ret=svf.VrlOut("NUMBER"     ,  count);

//                  ret=svf.VrsOut("GRADE"      ,  rs.getString("GRADE"));
//              ret=svf.VrsOut("HR_CLASS"   ,  rs.getString("HR_CLASS"));
                ret=svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                ret=svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                ret=svf.VrsOut("SCHOOLNO"   ,  rs.getString("SCHREGNO"));
                ret=svf.VrsOut("NAME"       ,  rs.getString("NAME"));

                //視力
                //右裸眼
                ret=svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_CHECK"));
                //右矯正
                if(rs.getString("R_VISION_CHECK").equalsIgnoreCase("")){
                    ret=svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION_CHECK"));
                } else {
                    ret=svf.VrsOut("SIGHT_R2"   ,  "(" + rs.getString("R_VISION_CHECK") + ")");
                }
                //POINT
                ret=svf.VrsOut("POINT"  ,  ",");
                //左裸眼
                ret=svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_CHECK"));
                //左矯正
                if(rs.getString("L_VISION_CHECK").equalsIgnoreCase("")){
                    ret=svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION_CHECK"));
                } else {
                    ret=svf.VrsOut("SIGHT_L2"   ,  "(" + rs.getString("L_VISION_CHECK") + ")");
                }
                //尿
                if((rs.getString("Nyou1_Tanpaku").equalsIgnoreCase("")==false) || (rs.getString("Nyou1_Tou").equalsIgnoreCase("")==false) || (rs.getString("Nyou1_Senketsu").equalsIgnoreCase("")==false)){
                    ret=svf.VrsOut("URINE"      ,  "○");
                }

                ret=svf.VrsOut("ANEMIA"         ,  rs.getString("Hinketsu"));
                if(rs.getString("Syoken_Kekkaku").equalsIgnoreCase("on")){
                    ret=svf.VrsOut("TUBERCULOSIS"   ,  "○");
                }

                ret=svf.VrsOut("HEART"          ,  rs.getString("Ekibyo_Shinzo"));
                ret=svf.VrsOut("HEART"          ,  rs.getString("Kensa_Shinzo"));
                ret=svf.VrsOut("NOURISHMENT"    ,  rs.getString("Eiyo_Jyotai"));

                ret=svf.VrsOut("SPINE"  ,  rs.getString("Sekichu_Kyokaku"));
                ret=svf.VrsOut("EYES"   ,  rs.getString("Meekibyo_Ijyo"));

                ret=svf.VrsOut("NOSE"   ,  rs.getString("Jibi_Shikkan"));
                ret=svf.VrsOut("SKIN"   ,  rs.getString("Hifu_Shikkan"));

                ret=svf.VrsOut("OTHERS1"    ,  rs.getString("Sonota"));

                if(rs.getString("Misyochi_Nyushi_Check").equalsIgnoreCase("on")){
                    ret=svf.VrsOut("TEETH1"     ,  rs.getString("Misyochi_Nyushi"));
                }
                if(rs.getString("Misyochi_Eikyushi_Check").equalsIgnoreCase("on")){
                    ret=svf.VrsOut("TEETH2"     ,  rs.getString("Misyochi_Eikyushi"));
                }

                ret=svf.VrsOut("TEETH3"     ,  rs.getString("Shiretsu_Ago"));

                ret=svf.VrsOut("SIKOU"      ,  rs.getString("Shikou"));
                ret=svf.VrsOut("SINIKU"     ,  rs.getString("Shiniku"));

                ret=svf.VrsOut("OTHERS2"    ,  rs.getString("Sonota_Ha"));

                ret = svf.VrEndRecord();
                //データクリア

                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            log.debug("[KNJF040]set_meisai9 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai9 read error!", ex);
        }
        log.debug("[KNJF040]set_meisai9 path!");

    }  //set_meisai9の括り



    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    /*---------------------------------------*
     * １）生徒学生健康診断票（一般）        *
     *---------------------------------------*/
    public void set_head1(String param[])
                     throws ServletException, IOException
    {
        try {
            ret=svf.VrsOut("SCHOOL_NAME"  , param[8]);
            //log.debug("[KNJF040]set_head1 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head1 read error!", ex);
        }
        //log.debug("[KNJF040]set_head1 path!");

    }  //set_head1の括り


    /*---------------------------------------*
     * ３）健康診断の未受検項目のある生徒へ  *
     *---------------------------------------*/
    public void set_head3(String param[])
                     throws ServletException, IOException
    {
        try {
            ret=svf.VrsOut("SCHOOLNAME"     , param[8]);
// 2003/11/12
//          ret=svf.VrsOut("DATE"           , param[3]);
            ret=svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param[3]));
            ret=svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param[3]) + " )");

            String sql = "SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD='03'";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_head3 sql ok!");

            String title03 = "";
            String text03 = "";
            StringTokenizer st;
            if( rs.next() ){
                title03 = rs.getString(1);     //タイトル
                text03  = rs.getString(2);     //本文
                ret=svf.VrsOut("TITLE"  , title03);
                //ret=svf.VrsOut("TEXT1"  , text03);
                int j = 1;
                if(text03 != null) {
                    st = new StringTokenizer(text03,"\n");
                    log.debug("countToken=" + st.countTokens());  
                    while(st.hasMoreTokens()) {
                        ret=svf.VrsOut("TEXT"+j,st.nextToken());
                        j++;
                    }
                }
            }
            db2.commit();
            log.debug("[KNJF040]set_head3 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head3 read error!", ex);
        }
        log.debug("[KNJF040]set_head3 path!");

    }  //set_head3の括り


    /*------------------------------------*
     * ４）眼科検診のお知らせ             *
     *------------------------------------*/
    public void set_head4(String param[])
                     throws ServletException, IOException
    {
        try {
            ret=svf.VrsOut("schoolname1"    , param[7]);
            ret=svf.VrsOut("schoolname2"    , param[8]);
            ret=svf.VrsOut("post"           , param[10]);
            ret=svf.VrsOut("staff1"         , param[9]);
// 2003/11/12
//          ret=svf.VrsOut("DATE"           , param[3]);
            ret=svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param[3]));
            ret=svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param[3]) + " )");

            String sql = "SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD='02'";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_head4 sql ok!");

            String title02 = "";
            String text02 = "";
            StringTokenizer st;
            if( rs.next() ){
                title02 = rs.getString(1);     //タイトル
                text02  = rs.getString(2);     //本文
                ret=svf.VrsOut("TITLE"  , title02);
                //ret=svf.VrsOut("TEXT1"  , text02);
                int j = 1;
                if(text02 != null) {
                    st = new StringTokenizer(text02,"\n");
                    while(st.hasMoreTokens()) {
                        ret=svf.VrsOut("TEXT"+j,st.nextToken());
                        j++;
                    }
                }
            }
            db2.commit();
            log.debug("[KNJF040]set_head4 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head4 read error!", ex);
        }
        log.debug("[KNJF040]set_head4 path!");

    }  //set_head4の括り


    /*------------------------------------*
     * ５）検診結果のお知らせ（歯・口腔） *
     *------------------------------------*/
    public void set_head5(String param[])
                     throws ServletException, IOException
    {
        try {
// 2003/11/12
//          ret=svf.VrsOut("YMD"            , param[6]);
            ret=svf.VrsOut("YMD"            , KNJ_EditDate.h_format_JP(param[6]));
            ret=svf.VrsOut("schoolname1"   , param[7]);
            ret=svf.VrsOut("post1"          , param[10]);
            ret=svf.VrsOut("staff1"         , param[9]);
            ret=svf.VrsOut("schoolname2"   , param[7]);
            ret=svf.VrsOut("post2"          , param[10]);
            ret=svf.VrsOut("staff2"         , param[9]);
            ret=svf.VrsOut("SCHOOLNAME3"    , param[8]);
            String sql = "SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD='04'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_head5 sql ok!");

            String title04 = "";
            String text04 = "";
            StringTokenizer st;
            if( rs.next() ){
                title04 = rs.getString(1);     //タイトル
                text04  = rs.getString(2);     //本文
                ret=svf.VrsOut("TITLE1"  , title04);
                //ret=svf.VrsOut("TEXT1"  , text04);
                int j = 1;
                if(text04 != null) {
                    st = new StringTokenizer(text04,"\n");
                    while(st.hasMoreTokens()) {
                        ret=svf.VrsOut("TEXT"+j,st.nextToken());
                        j++;
                    }
                }
            }
            db2.commit();
            log.debug("[KNJF040]set_head5 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head5 read error!", ex);
        }
        log.debug("[KNJF040]set_head5 path!");

    }  //set_head5の括り


    /*------------------------------------*
     * ７）検診結果のお知らせ（一般）     *
     *------------------------------------*/
    public void set_head6(String param[])
                     throws ServletException, IOException
    {
        try {
            ret=svf.VrsOut("schoolname1"    , param[7]);
            ret=svf.VrsOut("post"           , param[10]);
            ret=svf.VrsOut("staff1"         , param[9]);

            String sql = "SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD='01'";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("[KNJF040]set_head6 sql ok!");

            String title01 = "";
            String text01 = "";
            StringTokenizer st;
            if( rs.next() ){
                title01 = rs.getString(1);     //タイトル
                text01  = rs.getString(2);     //本文
                ret=svf.VrsOut("TITLE1"  , title01);
                //ret=svf.VrsOut("TEXT1"  , text01);
                int j = 1;
                if(text01 != null) {
                    st = new StringTokenizer(text01,"\n");
                    while(st.hasMoreTokens()) {
                        ret=svf.VrsOut("TEXT"+j,st.nextToken());
                        j++;
                    }
                }
            }
            db2.commit();
            log.debug("[KNJF040]set_head6 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head6 read error!", ex);
        }
        log.debug("[KNJF040]set_head6 path!");

    }  //set_head6の括り


    /*------------------------------------*
     * ６）定期健康診断結果 header        *
     *------------------------------------*/
    public void set_head7(String param[],String schregno_info,String hr_name_info,String attendno_info)
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "NAME_SHOW AS NAME "
                + "FROM "
                    + "SCHREG_BASE_MST "
                + "WHERE "
                        + "SCHREGNO        = '" +  schregno_info + "' ";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            /** SVFフォームへデータをセット **/
            while( rs.next() ){
                ret=svf.VrsOut("NAME"       ,  rs.getString("NAME"));

// 2003/11/12
//              ret=svf.VrsOut("YMD1"       , param[6]);
                ret=svf.VrsOut("YMD1"       , KNJ_EditDate.h_format_JP(param[6]));
                ret=svf.VrsOut("HR_NAME"   ,  hr_name_info);
                ret=svf.VrsOut("ATTENDNO"   ,  attendno_info);

            }
            db2.commit();
            //log.debug("[KNJF040]set_head7 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head7 read error!", ex);
        }

    }  //set_head7の括り

    /*------------------------------------*
     * ６）定期健康診断結果 クリア        *
     *------------------------------------*/
    public void set_meisai7_clear(String param[])
                     throws ServletException, IOException
    {
        try {
                //データをクリア:header
                ret=svf.VrsOut("YMD2"       ,  "");
                ret=svf.VrsOut("NAME"       ,  "");
                ret=svf.VrsOut("YMD1"       ,  "");
                ret=svf.VrsOut("GRADE"      ,  "");
                ret=svf.VrsOut("HR_CLASS"   ,  "");
                ret=svf.VrsOut("ATTENDNO"   ,  "");
                //データクリア:det_tooth
                ret=svf.VrsOut("HEIGHT"     ,  "");
                ret=svf.VrsOut("WEIGHT"     ,  "");
                ret=svf.VrsOut("BMI"        ,  "");
                ret=svf.VrsOut("SITHEIGHT"  ,  "");
                ret=svf.VrsOut("SIGHT_R1"   ,  "");
                ret=svf.VrsOut("SIGHT_R2"   ,  "");
                ret=svf.VrsOut("SIGHT_L1"   ,  "");
                ret=svf.VrsOut("SIGHT_L2"   ,  "");
                ret=svf.VrsOut("EYES"       ,  "");
                ret=svf.VrsOut("HEARING_R1"     ,  "");
                ret=svf.VrsOut("HEARING_R2"     ,  "");
                ret=svf.VrsOut("HEARING_L1"     ,  "");
                ret=svf.VrsOut("HEARING_L2"     ,  "");
                ret=svf.VrsOut("NOSE"       ,  "");
                ret=svf.VrsOut("SKIN"       ,  "");
                ret=svf.VrsOut("DISEASE"    ,  "");
                ret=svf.VrsOut("GUIDE"      ,  "");
                ret=svf.VrsOut("INSPECTION" ,  "");
                ret=svf.VrsOut("UNUSUAL"    ,  "");
                ret=svf.VrsOut("ALBUMIN1"       ,  "");
                ret=svf.VrsOut("SACCHARIDE1"    ,  "");
                ret=svf.VrsOut("BLOOD1"         ,  "");
                ret=svf.VrsOut("ALBUMIN2"       ,  "");
                ret=svf.VrsOut("SACCHARIDE2"    ,  "");
                ret=svf.VrsOut("BLOOD2"         ,  "");
                ret=svf.VrsOut("OTHERS1"    ,  "");
                ret=svf.VrsOut("NOTE1"      ,  "");
                ret=svf.VrsOut("TEETHLINE"  ,  "");
                ret=svf.VrsOut("PLAQUE"     ,  "");
                ret=svf.VrsOut("GUM"        ,  "");
                ret=svf.VrsOut("TEETHNO"    ,  "");
                ret=svf.VrsOut("DECAYED1"   ,  "");
                ret=svf.VrsOut("DECAYED2"   ,  "");
                ret=svf.VrsOut("DECAYED3"   ,  "");
                ret=svf.VrsOut("DECAYED4"   ,  "");
                ret=svf.VrsOut("LOST"       ,  "");
                ret=svf.VrsOut("OTHERS2"    ,  "");
                ret=svf.VrsOut("VIEWS"      ,  "");

            //log.debug("[KNJF040]set_meisai7_clear read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_meisai7_clear read error!", ex);
        }

    }  //set_meisai7_clearの括り


    /*------------------------------------*
     * ８）内科検診所見あり生徒の名簿     *
     *------------------------------------*/
    public void set_head8(String param[])
                     throws ServletException, IOException
    {
        try {
// 2003/11/12
//              ret=svf.VrsOut("NENDO"  , param[0]);
//              ret=svf.VrsOut("DATE"   , param[6]);
            ret=svf.VrsOut("NENDO"  , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
            ret=svf.VrsOut("DATE"   , KNJ_EditDate.h_format_JP(param[6]));
            log.debug("[KNJF040]set_head8 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head8 read error!", ex);
        }
        log.debug("[KNJF040]set_head8 path!");

    }  //set_head8の括り


    /*------------------------------------*
     * ９）定期健康診断異常者一覧表       *
     *------------------------------------*/
    public void set_head9(String param[])
                     throws ServletException, IOException
    {
        try {
// 2003/11/12
//          ret=svf.VrsOut("NENDO"  , param[0]);
//          ret=svf.VrsOut("YMD"    , param[6]);
            ret=svf.VrsOut("NENDO"  , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
            ret=svf.VrsOut("YMD"    , KNJ_EditDate.h_format_JP(param[6]));

            String sql = "SELECT "
                        + "TBL1.NAME1,"
                        + "TBL2.NAME1 "
                    + "FROM "
                        + "("
                            + "SELECT "
                                + "T2.NAME1 "
                            + "FROM "
                                + "NAME_YDAT T1,"
                                + "NAME_MST T2 "
                            + "WHERE "
                                    + "T1.YEAR = '" +  param[0] + "' "
                                + "AND T1.NAMECD1 = 'F610' "
                                + "AND T1.NAMECD2 = '" +  param[20] + "' "
                                + "AND T2.NAMECD1 = T1.NAMECD1 "
                                + "AND T2.NAMECD2 = T1.NAMECD2"
                        + ") TBL1,"
                        + "("
                            + "SELECT "
                                + "T2.NAME1 "
                            + "FROM "
                                + "NAME_YDAT T1,"
                                + "NAME_MST T2 "
                            + "WHERE "
                                    + "T1.YEAR = '" +  param[0] + "' "
                                + "AND T1.NAMECD1 = 'F620' "
                                + "AND T1.NAMECD2 = '" +  param[21] + "' "
                                + "AND T2.NAMECD1 = T1.NAMECD1 "
                                + "AND T2.NAMECD2 = T1.NAMECD2"
                        + ") TBL2";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            String Ippan_Jyoken = "";
            String Ha_Jyoken = "";
            if( rs.next() ){
                Ippan_Jyoken = rs.getString(1);     //一般条件
                Ha_Jyoken  = rs.getString(2);       //歯・口腔条件
                ret=svf.VrsOut("CHOICE"     , Ippan_Jyoken);
                ret=svf.VrsOut("CONDITIONS" , Ha_Jyoken);
            }
            db2.commit();
            log.debug("[KNJF040]set_head9 read ok!");
        } catch( Exception ex ) {
            log.debug("[KNJF040]set_head9 read error!", ex);
        }
        log.debug("[KNJF040]set_head9 path!");

    }  //set_head9の括り
}  //クラスの括り

// kanji=漢字
/*
 * $Id: a7d584f0fd0e58014dff1f610f1e503adf8b6bc5 $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;
import java.io.PrintWriter;

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

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ４７０＞  スクーリングとレポートを出力
 *
 *  2005/09/14 m-yama 作成日
 **/

public class KNJM470sc_rep {

    private static final Log log = LogFactory.getLog(KNJM470sc_rep.class);
    private boolean jhighschool = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];
    //  パラメータの取得
        String classno[] = request.getParameterValues("category_name");     //クラス
        param[5] = "(";
        for( int ia=0 ; ia<classno.length ; ia++ ){
            if (ia != 0) param[5] = param[5]+", ";
            param[5] = param[5]+"'";
            param[5] = param[5]+classno[ia];
            param[5] = param[5]+"'";

        }
        param[5] = param[5]+")";
for(int i=0 ; i<classno.length ; i++) log.debug("class="+classno[i]);
log.debug("param[5] = " + String.valueOf(param[5]));
        log.fatal("$Revision: 56595 $");
        try {
            param[0]  = request.getParameter("YEAR");            //年度
            param[1]  = request.getParameter("GAKKI");           //学期
            param[2]  = request.getParameter("SUBCLASS");        //科目コード
            param[3]  = request.getParameter("OUTPUT");          //出力順序
            param[10] = request.getParameter("useCurriculumcd"); //教育課程
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

    //  ＳＶＦ作成処理
        PreparedStatement ps    = null;
        PreparedStatement ps2   = null;
        PreparedStatement ps2_1 = null;
        PreparedStatement ps3   = null;
        PreparedStatement ps4   = null;
        boolean nonedata = false;                               //該当データなしフラグ
for(int i=0 ; i<4 ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps    = db2.prepareStatement(preStat(param));
            ps2   = db2.prepareStatement(preStat2(param));
            ps2_1 = db2.prepareStatement(preStat2_1(param));
            ps3   = db2.prepareStatement(preStat3(param));
            ps4   = db2.prepareStatement(preStat4(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //ヘッダ作成
        setHeader(db2,svf,param);

        if (setSvfMain(db2,svf,param,ps,ps2,ps2_1,ps3,ps4)) nonedata = true;    //帳票出力のメソッド

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps,ps2,ps2_1,ps3,ps4);         //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        param[5] = param[0]+"年度";

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

        try {
            String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '"+param[0]+"' AND SEMESTER = '"+param[1]+"' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[4] = rs.getString("SEMESTERNAME");
            }
            rs.close();
            db2.commit();
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        getinfo = null;
        returnval = null;
    }

    /**
     *  svf print 印刷処理全印刷
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps,
        PreparedStatement ps2,
        PreparedStatement ps2_1,
        PreparedStatement ps3,
        PreparedStatement ps4
    ) {
        boolean nonedata  = false;
        boolean schchange = false;
        boolean strchange = true ;
        boolean setflg    = false;          //回数判定用
        int gyo = 1;
        int schcnt = 0;
        int seqcnt = 0;
        String col = "*";
        String seqdate = "";
        String befseq = "*";            //回数判定用
        String gval   = "*";            //前回評定
        try {
            svf.VrSetForm("KNJM470.frm", 4);          //セットフォーム
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (gyo > 21){
                    schcnt = 0;
                    gyo = 1;
                    schchange = true ;
                }
                if (!col.equals(rs.getString("SCHREGNO")) && gyo > 1){
                    schcnt++;
                    schchange = true ;
                }

                //結合用マスクフィールド
                svf.VrsOut("MASK1"            ,String.valueOf(schcnt));
                svf.VrsOut("MASK2"            ,String.valueOf(schcnt));
                svf.VrsOut("MASK3"            ,String.valueOf(schcnt));
                svf.VrsOut("MASK4"            ,String.valueOf(schcnt));
                svf.VrsOut("MASK5"            ,String.valueOf(schcnt));

                //ヘッダ
                svf.VrsOut("NENDO"            ,param[5]);         //年度
                svf.VrsOut("DATE"             ,param[6]);         //作成日
                svf.VrsOut("SEMESTER"         ,param[4]);         //学期

                //科目
                if (param[2].equals("0")){
                    svf.VrsOut("SUBCLASS1"        ,"全科目");                     //科目
                }else {
                    svf.VrsOut("SUBCLASS1"        ,rs.getString("SUBCLASSNAME")); //科目
                }

                //出力順序
                if (param[3].equals("1")){
                    svf.VrsOut("ORDER"        ,"クラス出席番号順");   //出力順
                }else {
                    svf.VrsOut("ORDER"        ,"学籍番号順");         //出力順
                }

                svf.VrsOut("SUBCLASS2"    ,rs.getString("SUBCLASSABBV"));     //科目名(略称)
                if (param[1].equals("1")){
                    svf.VrsOut("GRAD_VALUE"       ,rs.getString("SEM1_VALUE"));   //前期評定
                }else {
                    svf.VrsOut("GRAD_VALUE"       ,rs.getString("GRAD_VALUE"));   //後期評定
                }

                svf.VrsOut("SCORE1"       ,rs.getString("SEM1_TERM_SCORE"));  //前期
                svf.VrsOut("SCORE2"       ,rs.getString("SEM2_TERM_SCORE"));  //単認
                svf.VrsOut("SEQ1_1"       ,rs.getString("SCH_SEQ_MIN"));      //規定数スクーリング
                svf.VrsOut("SEQ1_2"       ,rs.getString("REP_SEQ_ALL"));      //規定数レポート

log.debug(rs.getString("SCHREGNO") + " " + rs.getString("CHAIRCD"));
                ResultSet rs2 = null;
                if (rs.getString("SCHOOLINGKINDCD") != null && rs.getString("SCHOOLINGKINDCD").equals("1")){
                    ps2.setString( 1, rs.getString("SCHREGNO"));
                    ps2.setString( 2, rs.getString("CHAIRCD"));
                    rs2 = ps2.executeQuery();
                }else {
                    ps2_1.setString( 1, rs.getString("SCHREGNO"));
                    ps2_1.setString( 2, rs.getString("CHAIRCD"));
                    ps2_1.setString( 3, rs.getString("SCHOOLINGKINDCD"));
                    rs2 = ps2_1.executeQuery();
                }
                int kindcnt = 1;
                while( rs2.next() ){
                    if (kindcnt < 25 && rs2.getString("EXECUTEDATE") != null){
                        seqdate = rs2.getString("EXECUTEDATE").substring(5,7) + rs2.getString("EXECUTEDATE").substring(8,10);
log.debug("seqdate" + String.valueOf(seqdate));
                        svf.VrsOut("DATE" + kindcnt   ,String.valueOf(seqdate));
                        seqcnt++;
                        kindcnt++;
                    }else {
                        break;
                    }
                }
                svf.VrsOut("SEQ2_1"           ,String.valueOf(seqcnt));       //実数スクーリング
                rs2.close();
                seqcnt = 0;


                ps4.setString(1,rs.getString("SUBCLASSCD"));    //科目コード
                ps4.setString(2,rs.getString("SCHREGNO"));      //学籍番号
                ResultSet rs4 = ps4.executeQuery();

                befseq = "*";
                gval   = "*";
                setflg = false;
                while( rs4.next() ){
                    if (Integer.parseInt(rs4.getString("STANDARD_SEQ")) > 0 && Integer.parseInt(rs4.getString("STANDARD_SEQ")) < 25){
                        if (befseq.equalsIgnoreCase(rs4.getString("STANDARD_SEQ")) && !setflg){
                            if (gval.equals("受")){
                                if (rs4.getString("GRDVALUE").equals("無")){
                                    svf.VrsOut("VALUE" + rs4.getString("STANDARD_SEQ")    ,String.valueOf("受"));
//                                  seqcnt++;
                                }else {
                                    svf.VrsOut("VALUE" + rs4.getString("STANDARD_SEQ")    ,String.valueOf("再"));
//                                  seqcnt++;
                                }
                            }
                            setflg = true;
                        }else {
                            if (!befseq.equalsIgnoreCase(rs4.getString("STANDARD_SEQ"))){
                                svf.VrsOut("VALUE" + rs4.getString("STANDARD_SEQ")    ,rs4.getString("GRDVALUE"));
                                if (rs4.getString("JUDGE") != null && rs4.getString("JUDGE").equals("1")){
                                    seqcnt++;
                                }
                                setflg = false;
                            }
                        }
                    }
                    befseq = rs4.getString("STANDARD_SEQ");
                    gval = rs4.getString("GRDVALUE");
                }
                svf.VrsOut("SEQ2_2"           ,String.valueOf(seqcnt));       //実数レポート
                rs4.close();
                seqcnt = 0;



                //生徒単位での最初のデータのみ出力
                if (schchange || strchange){
                    svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO"));         //学籍番号
                    svf.VrsOut("NAME"         ,rs.getString("NAME"));             //生徒氏名
                    svf.VrsOut("HR_NAME"      ,rs.getString("HR_NAMEABBV"));      //クラス
                    ps3.setString( 1, rs.getString("SCHREGNO"));
                    ResultSet rs3 = ps3.executeQuery();
                    while( rs3.next() ){
                        svf.VrsOut("HR_ATTEND"    ,rs3.getString("HR_ATTEND"));   //特別活動
                    }
                    rs3.close();
                }

                gyo++;
                col = rs.getString("SCHREGNO");
                svf.VrEndRecord();
                schchange = false;
                strchange = false;
                nonedata  = true ;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }

    /**データ　取得**/
    private String preStat(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("WITH SCHTABLE AS ( ");
            stb.append("SELECT ");
            stb.append("    t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t2.NAME,t3.HR_NAMEABBV ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT t3 ON t1.GRADE || t1.HR_CLASS = t3.GRADE || t3.HR_CLASS ");
            stb.append("    AND t3.YEAR = '"+param[0]+"' ");
            stb.append("    AND t3.SEMESTER = '"+param[1]+"' ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND t1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t1.GRADE || t1.HR_CLASS in "+param[5]+" ");
            if (!param[2].equals("0")){
                stb.append("),ALLDATA AS ( ");
            }else {
                stb.append(") ");
            }
            stb.append("SELECT ");
            stb.append("    t1.*,t2.CHAIRCD, ");
            if ("1".equals(param[10])) {
                stb.append("    t3.CLASSCD || t3.SCHOOL_KIND || t3.CURRICULUM_CD || t3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD AS W_SUBCLASSCD, ");
            } else {
                stb.append("    t3.SUBCLASSCD, ");
            }
            stb.append("    t3.TAKESEMES,t4.SUBCLASSABBV,t4.SUBCLASSNAME, ");
            stb.append("    t5.SEM1_TERM_SCORE,t5.SEM1_VALUE,t5.SEM2_TERM_SCORE,t5.SEM2_VALUE,t5.GRAD_VALUE, ");
            stb.append("    t6.SCH_SEQ_MIN,t6.REP_SEQ_ALL, ");
            stb.append("    t7.SCHOOLINGKINDCD ");
//          stb.append("    COUNT(t7.SCHOOLINGKINDCD) AS JISSU ");
            stb.append("FROM ");
            stb.append("    SCHTABLE t1 ");
            stb.append("    LEFT JOIN CHAIR_STD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
            stb.append("    AND t2.YEAR = '"+param[0]+"' ");
            stb.append("    AND t2.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t2.CHAIRCD NOT LIKE '92%' ");
            stb.append("    LEFT JOIN CHAIR_DAT t3 ON t2.CHAIRCD = t3.CHAIRCD ");
            stb.append("    AND t3.YEAR = '"+param[0]+"' ");
            stb.append("    AND t3.SEMESTER = '"+param[1]+"' ");
            stb.append("    LEFT JOIN SUBCLASS_MST t4 ON t3.SUBCLASSCD = t4.SUBCLASSCD ");
            if ("1".equals(param[10])) {
                stb.append("    AND t3.CLASSCD = t4.CLASSCD ");
                stb.append("    AND t3.SCHOOL_KIND = t4.SCHOOL_KIND ");
                stb.append("    AND t3.CURRICULUM_CD = t4.CURRICULUM_CD ");
            }
            stb.append("    LEFT JOIN RECORD_DAT t5 ON t1.SCHREGNO = t5.SCHREGNO ");
            stb.append("    AND t5.YEAR = '"+param[0]+"' ");
            if ("1".equals(param[10])) {
                stb.append("    AND t5.CLASSCD = t3.CLASSCD ");
                stb.append("    AND t5.SCHOOL_KIND = t3.SCHOOL_KIND ");
                stb.append("    AND t5.CURRICULUM_CD = t3.CURRICULUM_CD ");
            }
            stb.append("    AND t5.SUBCLASSCD = t3.SUBCLASSCD ");
            stb.append("    AND t5.TAKESEMES = t3.TAKESEMES ");
            stb.append("    LEFT JOIN CHAIR_CORRES_DAT t6 ON t2.CHAIRCD = t6.CHAIRCD ");
            stb.append("    AND t6.YEAR = '"+param[0]+"' ");
            if ("1".equals(param[10])) {
                stb.append("    AND t6.CLASSCD = t3.CLASSCD ");
                stb.append("    AND t6.SCHOOL_KIND = t3.SCHOOL_KIND ");
                stb.append("    AND t6.CURRICULUM_CD = t3.CURRICULUM_CD ");
            }
            stb.append("    AND t6.SUBCLASSCD = t3.SUBCLASSCD ");
            stb.append("    LEFT JOIN SCH_ATTEND_DAT t7 ON t1.SCHREGNO = t7.SCHREGNO ");
            stb.append("    AND t7.YEAR = '"+param[0]+"' ");
            stb.append("    AND t7.CHAIRCD = t2.CHAIRCD ");
//          stb.append("    AND t7.SCHOOLINGKINDCD = '1' ");
            stb.append("WHERE ");
            if (param[1].equals("1")){
                stb.append("    t5.SEM1_VALUE = 1 ");
            }else {
                stb.append("    t5.GRAD_VALUE = 1 ");
            }
            stb.append("GROUP BY ");
            stb.append("   t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t1.NAME,t1.HR_NAMEABBV, ");
            stb.append("   t2.CHAIRCD, ");
            if ("1".equals(param[10])) {
                stb.append("    t3.CLASSCD || t3.SCHOOL_KIND || t3.CURRICULUM_CD || t3.SUBCLASSCD, ");
                stb.append("    t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD, ");
            } else {
                stb.append("    t3.SUBCLASSCD, ");
            }
            stb.append("   t3.TAKESEMES,t4.SUBCLASSABBV,t4.SUBCLASSNAME, ");
            stb.append("   t5.SEM1_TERM_SCORE,t5.SEM1_VALUE,t5.SEM2_TERM_SCORE,t5.SEM2_VALUE,t5.GRAD_VALUE, ");
            stb.append("   t6.SCH_SEQ_MIN,t6.REP_SEQ_ALL,t7.SCHOOLINGKINDCD ");
            if (!param[2].equals("0")){
                stb.append(") ");
                stb.append("SELECT ");
                stb.append("    * ");
                stb.append("FROM ");
                stb.append("    ALLDATA ");
                stb.append("WHERE ");
                if ("1".equals(param[10])) {
                    stb.append("    W_SUBCLASSCD = '"+param[2].substring(0,13)+"' ");
                } else {
                    stb.append("    SUBCLASSCD = '"+param[2].substring(0,6)+"' ");
                }
                if (param[3].equals("1")){
                    stb.append("ORDER BY ");
                    stb.append("   GRADE,HR_CLASS,ATTENDNO ");
                }else {
                    stb.append("ORDER BY ");
                    stb.append("   SCHREGNO ");
                }
            }else {
                if (param[3].equals("1")){
                    stb.append("ORDER BY ");
                    stb.append("   t1.GRADE,t1.HR_CLASS,t1.ATTENDNO ");
                }else {
                    stb.append("ORDER BY ");
                    stb.append("   t1.SCHREGNO ");
                }
            }

log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String preStat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    SCHOOLING_SEQ,MIN(EXECUTEDATE) AS EXECUTEDATE ");
            stb.append("FROM ");
            stb.append("    SCH_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO = ? ");
            stb.append("    AND CHAIRCD = ? ");
            stb.append("    AND SCHOOLINGKINDCD = '1' ");
            stb.append("GROUP BY ");
            stb.append("    SCHOOLING_SEQ ");
            stb.append("ORDER BY ");
            stb.append("    EXECUTEDATE ");

log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String preStat2_1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    SCHOOLING_SEQ,EXECUTEDATE ");
            stb.append("FROM ");
            stb.append("    SCH_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO = ? ");
            stb.append("    AND CHAIRCD = ? ");
            stb.append("    AND SCHOOLINGKINDCD = ? ");
            stb.append("ORDER BY ");
            stb.append("    EXECUTEDATE ");

log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat2_1 error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String preStat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ATABLE AS ( ");
            stb.append("SELECT ");
            stb.append("    EXECUTEDATE ");
            stb.append("FROM ");
            stb.append("    HR_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO = ? ");
            stb.append("GROUP BY ");
            stb.append("    EXECUTEDATE ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS HR_ATTEND ");
            stb.append("FROM ");
            stb.append("    ATABLE ");

log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat3 error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String preStat4(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append(" SELECT ");
            stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE, ");
            stb.append("     CASE WHEN t1.GRAD_VALUE IS NULL THEN '受' WHEN t1.GRAD_VALUE = '' THEN '受' ELSE ABBV1 END AS GRDVALUE, ");
            stb.append("     CASE WHEN t1.GRAD_VALUE BETWEEN '2' AND '5' THEN '1' ELSE '0' END AS JUDGE ");
            stb.append(" FROM ");
            stb.append("     REP_PRESENT_DAT t1 ");
            stb.append("     LEFT JOIN NAME_MST ON GRAD_VALUE = NAMECD2 AND NAMECD1 = 'M003' ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '"+param[0]+"' AND ");
            if ("1".equals(param[10])) {
                stb.append("     t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = ? AND ");
            } else {
                stb.append("     t1.SUBCLASSCD = ? AND ");
            }
            stb.append("     t1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE DESC ");

log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat4 error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps2,
        PreparedStatement ps2_1,
        PreparedStatement ps3,
        PreparedStatement ps4
    ) {
        try {
            ps.close();
            ps2.close();
            ps2_1.close();
            ps3.close();
            ps4.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り

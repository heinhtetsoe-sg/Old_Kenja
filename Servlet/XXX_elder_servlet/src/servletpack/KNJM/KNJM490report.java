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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ４９０＞  レポートのみ
 *
 *  2005/06/28 m-yama 作成日
 **/

public class KNJM490report {

    private static final Log log = LogFactory.getLog(KNJM490report.class);
    private boolean jhighschool = false;
    private boolean _isKumamoto = false;

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
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("GAKKI");           //学期
            param[2] = request.getParameter("SUBCLASS");        //科目コード
            param[3] = request.getParameter("OUTPUT");          //出力順序
            param[4] = request.getParameter("OUTPUT2");         //出力方法
            param[10] = request.getParameter("useCurriculumcd"); //教育課程
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        //  ＳＶＦ作成処理
        PreparedStatement ps  = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        for(int i=0 ; i<4 ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps  = db2.prepareStatement(preStat(param));
            ps2 = db2.prepareStatement(preStat2(param));
            ps3 = db2.prepareStatement(preStat3(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        setNameMstZ010(db2);
        //ヘッダ作成
        setHeader(db2,svf,param);

        if (setSvfMain(db2,svf,param,ps,ps2,ps3)) nonedata = true;  //帳票出力のメソッド

        //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps,ps2,ps3);           //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

            }//doGetの括り

    /**
     * 名称マスタ読み込み
     * @param db2
     * @param namecd1 名称コード1
     * @return 名称コード2をキーとするレコードのマップ
     */
    private void setNameMstZ010(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                _isKumamoto = "kumamoto".equals(rs.getString("NAME1"));
            }
        } catch (Exception e) {
            log.error("getNameMst Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 事前処理 **/
    private void setHeader(
            DB2UDB db2,
            Vrw32alp svf,
            String param[]
            ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        param[5] = param[0]+"年度";

        //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
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
            PreparedStatement ps3
            ) {
        boolean nonedata  = false;
        boolean schchange = false;
        boolean strchange = true ;
        boolean setflg    = false;          //回数判定用
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int gyo = 1;
        int schcnt = 0;
        int seqcnt = 0;
        String col = "*";
        String befseq = "*";            //回数判定用
        String gval   = "*";            //前回評定
        try {
            ret = svf.VrSetForm("KNJM490_1.frm", 4);            //セットフォーム
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (_isKumamoto) {
                    svf.VrsOut("SP_NAME", "登校日数");
                    svf.VrsOut("TEST_NAME2", "後期");
                } else {
                    svf.VrsOut("TEST_NAME2", "単認");
                }
                if (gyo > 42){
                    schcnt = 0;
                    gyo = 1;
                    schchange = true ;
                }
                if (!col.equals(rs.getString("SCHREGNO")) && gyo > 1){
                    schcnt++;
                    schchange = true ;
                }

                //結合用マスクフィールド
                ret = svf.VrsOut("MASK1"            ,String.valueOf(schcnt));
                ret = svf.VrsOut("MASK2"            ,String.valueOf(schcnt));
                ret = svf.VrsOut("MASK3"            ,String.valueOf(schcnt));
                ret = svf.VrsOut("MASK4"            ,String.valueOf(schcnt));
                ret = svf.VrsOut("MASK5"            ,String.valueOf(schcnt));

                //ヘッダ
                ret = svf.VrsOut("NENDO"            ,param[5]);         //年度
                ret = svf.VrsOut("DATE"             ,param[6]);         //作成日

                //科目
                if (param[2].equals("0")){
                    ret = svf.VrsOut("SUBCLASS1"        ,"全科目");                     //科目
                }else {
                    ret = svf.VrsOut("SUBCLASS1"        ,rs.getString("SUBCLASSNAME")); //科目
                }

                //出力順序
                if (param[3].equals("1")){
                    ret = svf.VrsOut("ORDER"        ,"クラス出席番号順");   //出力順
                }else {
                    ret = svf.VrsOut("ORDER"        ,"学籍番号順");         //出力順
                }

                //出力形式
                if (param[4].equals("1")){
                    ret = svf.VrsOut("OUTPUTDIV"        ,"スクーリング＆レポート"); //出力形式
                }else if (param[4].equals("2")){
                    ret = svf.VrsOut("OUTPUTDIV"        ,"スクーリングのみ");       //出力形式
                }else {
                    ret = svf.VrsOut("OUTPUTDIV"        ,"レポートのみ");           //出力形式
                }
                if (_isKumamoto) {
                    ret = svf.VrsOut("SIKEN_NAME"       , "評価");   //前期
                } else {
                    ret = svf.VrsOut("SIKEN_NAME"       , "試験");  //前期
                }

                ret = svf.VrsOut("SUBCLASS2"    ,rs.getString("SUBCLASSABBV"));     //科目名(略称)
                ret = svf.VrsOut("GRAD_VALUE"   ,rs.getString("GRAD_VALUE"));       //評定
                ret = svf.VrsOut("CREDITS"      ,rs.getString("CREDITS"));          //認定
                if (_isKumamoto) {
                    ret = svf.VrsOut("SCORE1"       ,rs.getString("SEM1_VALUE"));   //前期
                } else {
                    ret = svf.VrsOut("SCORE1"       ,rs.getString("SEM1_TERM_SCORE"));  //前期
                }
                if (_isKumamoto) {
                    ret = svf.VrsOut("SCORE2"       ,rs.getString("SEM2_VALUE"));  //単認
                } else {
                    ret = svf.VrsOut("SCORE2"       ,rs.getString("SEM2_TERM_SCORE"));  //単認
                }
                ret = svf.VrsOut("SEQ1"         ,rs.getString("REP_SEQ_ALL"));      //規定数

                log.debug(rs.getString("SCHREGNO") + " " + rs.getString("SUBCLASSCD"));
                ps2.setString(1,rs.getString("SUBCLASSCD"));    //科目コード
                ps2.setString(2,rs.getString("SCHREGNO"));      //学籍番号
                ps2.setString(3,rs.getString("SUBCLASSCD"));    //科目コード
                ps2.setString(4,rs.getString("SCHREGNO"));      //学籍番号
                ResultSet rs2 = ps2.executeQuery();

                befseq = "*";
                gval   = "*";
                setflg = false;
                while( rs2.next() ){
                    if (Integer.parseInt(rs2.getString("STANDARD_SEQ")) > 0 && Integer.parseInt(rs2.getString("STANDARD_SEQ")) < 25){
                        if (befseq.equalsIgnoreCase(rs2.getString("STANDARD_SEQ")) && !setflg){
                            if (gval.equals("受")){
                                if (rs2.getString("GRDVALUE").equals("無")){
                                    ret = svf.VrsOut("DATE_VALUE" + rs2.getString("STANDARD_SEQ")   ,String.valueOf("受"));
//                                  seqcnt++;
                                }else {
                                    ret = svf.VrsOut("DATE_VALUE" + rs2.getString("STANDARD_SEQ")   ,String.valueOf("再"));
//                                  seqcnt++;
                                }
                            }
                            setflg = true;
                        }else {
                            if (!befseq.equalsIgnoreCase(rs2.getString("STANDARD_SEQ"))){
                                ret = svf.VrsOut("DATE_VALUE" + rs2.getString("STANDARD_SEQ")   ,rs2.getString("GRDVALUE"));
                                if (rs2.getString("JUDGE") != null && rs2.getString("JUDGE").equals("1")){
                                    seqcnt++;
                                }
                                setflg = false;
                            }
                        }
                    }
                    befseq = rs2.getString("STANDARD_SEQ");
                    gval = rs2.getString("GRDVALUE");
                }
                ret = svf.VrsOut("SEQ2"         ,String.valueOf(seqcnt));       //実数
                rs2.close();
                seqcnt = 0;

                //生徒単位での最初のデータのみ出力
                if (schchange || strchange){
                    ret = svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO"));         //学籍番号
                    ret = svf.VrsOut("NAME"         ,rs.getString("NAME"));             //生徒氏名
                    ret = svf.VrsOut("HR_NAME"      ,rs.getString("HR_NAMEABBV"));      //クラス
                    ps3.setString( 1, rs.getString("SCHREGNO"));
                    ResultSet rs3 = ps3.executeQuery();
                    while( rs3.next() ){
                        ret = svf.VrsOut("HR_ATTEND"    ,rs3.getString("HR_ATTEND"));   //特別活動
                    }
                    rs3.close();
                }

                gyo++;
                col = rs.getString("SCHREGNO");
                ret = svf.VrEndRecord();
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
            stb.append("    t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t2.NAME,t3.HR_NAMEABBV, ");
            stb.append("    t1.COURSECD, t1.MAJORCD, t1.COURSECODE ");
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
            stb.append("    t5.SEM1_TERM_SCORE,t5.SEM1_VALUE,t5.SEM2_VALUE,t5.SEM2_TERM_SCORE, t5.GRAD_VALUE, t6.REP_SEQ_ALL, ");
            stb.append("    COUNT(t7.SCHOOLINGKINDCD) AS JISSU, t8.CREDITS ");
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
            stb.append("    AND t5.TAKESEMES = '0' ");
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
            stb.append("    AND t7.SCHOOLINGKINDCD = '1' ");
            stb.append("    LEFT JOIN CREDIT_MST t8 ON t8.YEAR = '"+param[0]+"' ");
            stb.append("    AND t8.COURSECD = t1.COURSECD ");
            stb.append("    AND t8.MAJORCD = t1.MAJORCD ");
            stb.append("    AND t8.GRADE = t1.GRADE ");
            stb.append("    AND t8.COURSECODE = t1.COURSECODE ");
            if ("1".equals(param[10])) {
                stb.append("    AND t8.CLASSCD = t3.CLASSCD ");
                stb.append("    AND t8.SCHOOL_KIND = t3.SCHOOL_KIND ");
                stb.append("    AND t8.CURRICULUM_CD = t3.CURRICULUM_CD ");
            } else {
                stb.append("    AND t8.CLASSCD = SUBSTR(t3.SUBCLASSCD,1,2) ");
            }
            stb.append("    AND t8.SUBCLASSCD = t3.SUBCLASSCD ");
            stb.append("GROUP BY ");
            stb.append("   t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t1.NAME,t1.HR_NAMEABBV, t1.COURSECD, t1.MAJORCD, t1.COURSECODE, ");
            stb.append("   t2.CHAIRCD, ");
            if ("1".equals(param[10])) {
                stb.append("    t3.CLASSCD || t3.SCHOOL_KIND || t3.CURRICULUM_CD || t3.SUBCLASSCD, ");
                stb.append("    t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD, ");
            } else {
                stb.append("    t3.SUBCLASSCD, ");
            }
            stb.append("   t3.TAKESEMES,t4.SUBCLASSABBV,t4.SUBCLASSNAME, ");
            stb.append("   t5.SEM1_TERM_SCORE,t5.SEM1_VALUE,t5.SEM2_VALUE,t5.SEM2_TERM_SCORE, t5.GRAD_VALUE, t6.REP_SEQ_ALL, t8.CREDITS ");
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

            stb.append(" WITH MAX_REP_PRESENT_DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("     STANDARD_SEQ,MAX(RECEIPT_DATE) AS RECEIPT_DATE ");
            stb.append(" FROM ");
            stb.append("     REP_PRESENT_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+param[0]+"' AND ");
            if ("1".equals(param[10])) {
                stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? AND ");
            } else {
                stb.append("     SUBCLASSCD = ? AND ");
            }
            stb.append("     SCHREGNO = ? ");
            stb.append(" GROUP BY ");
            stb.append("     STANDARD_SEQ ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE, ");
            stb.append("     CASE WHEN t1.GRAD_VALUE IS NULL THEN '受' WHEN t1.GRAD_VALUE = '' THEN '受' ELSE ABBV1 END AS GRDVALUE, ");
            stb.append("     CASE WHEN NAMESPARE1 = '1' THEN '1' ELSE '0' END AS JUDGE ");
            stb.append(" FROM ");
            stb.append("     REP_PRESENT_DAT t1 ");
            stb.append("     LEFT JOIN NAME_MST ON GRAD_VALUE = NAMECD2 AND NAMECD1 = 'M003', ");
            stb.append("     MAX_REP_PRESENT_DAT t2 ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '"+param[0]+"' AND ");
            if ("1".equals(param[10])) {
                stb.append("     t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = ? AND ");
            } else {
                stb.append("     t1.SUBCLASSCD = ? AND ");
            }
            stb.append("     t1.SCHREGNO = ? AND ");
            stb.append("     t1.STANDARD_SEQ = t2.STANDARD_SEQ AND ");
            stb.append("     t1.RECEIPT_DATE = t2.RECEIPT_DATE ");
            stb.append(" ORDER BY ");
            stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE DESC ");

            log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat2 error!");
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
            stb.append("    EXECUTEDATE,PERIODCD ");
            stb.append("FROM ");
            stb.append("    HR_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO = ? ");
            stb.append("    AND CHAIRCD LIKE '92%' ");
            stb.append("GROUP BY ");
            stb.append("    EXECUTEDATE,PERIODCD ");
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

    /**PrepareStatement close**/
    private void preStatClose(
            PreparedStatement ps,
            PreparedStatement ps2,
            PreparedStatement ps3
            ) {
        try {
            ps.close();
            ps2.close();
            ps3.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り

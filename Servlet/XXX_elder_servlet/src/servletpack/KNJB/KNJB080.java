/**
 *
 *	学校教育システム 賢者 [時間割管理]
 *
 *					＜ＫＮＪＢ０８０＞  施設別時間割表
 *
 * 2005/10/23 nakamoto 新規作成
 * 2005/10/24 m-yama   NO001:CLASSNAMEの_1又は、_2の判定を14→12に変更
 * 2006/04/21 nakamoto NO005 特別職員が設定されている場合、特別職員のみを表示するよう修正
 **/

package servletpack.KNJB;

import java.io.IOException;
import java.io.OutputStream;

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


public class KNJB080 {
    
    private static final Log log = LogFactory.getLog(KNJB080.class);
    
    Vrw32alp svf = new Vrw32alp();
    DB2UDB    db2;
    String dbname = new String();
    int ret;
    boolean nonedata  = false;
    
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        String param[] = new String[9];
        
        // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");      	// データベース名
            param[1] = request.getParameter("YEAR");            //年度
            param[2] = request.getParameter("BSCSEQ");          //ＳＥＱ
            param[5] = request.getParameter("SEMESTER");        //学期
            param[3] = request.getParameter("FACCD_NAME1");     //施設from
            param[4] = request.getParameter("FACCD_NAME2");     //施設to
            param[8] = request.getParameter("useCurriculumcd"); //教育課程
        } catch( Exception ex ) {
            log.error("parameter error! ",ex);
        }
        
        // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
        }
        
        // ＤＢ検索（コントロールマスター）
        /* 作成日の取得 */
        try {
            String sql = "SELECT CTRL_DATE FROM CONTROL_MST WHERE CTRL_NO='01'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if( rs.next() ){
                param[6] = rs.getString("CTRL_DATE");
            }
            rs.close();
            ps.close();
        } catch( Exception e ){
            log.error("sakuseibi error!",e);
        }
        
        // ＤＢ検索（基本時間割Ｈ）
        /* ＳＥＱ・タイトルの取得 */
        try {
            String sql = "SELECT BSCSEQ, TITLE "
                + "FROM SCH_PTRN_HDAT "
                + "WHERE YEAR = '"+param[1]+"' AND SEMESTER = '"+param[5]+"' AND BSCSEQ = "+param[2];
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if( rs.next() ){
                param[7] = rs.getString("BSCSEQ") + "：" + rs.getString("TITLE");
            }
            rs.close();
            ps.close();
        } catch( Exception e ){
            log.error("SCH_PTRN_HDAT error!",e);
        }
        
        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);
        
        
        // print設定
//      PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();
        
        // svf設定
        ret = svf.VrInit();						   //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定
        
        /*-----------------------------------------------------------------------------
         ＳＶＦ作成処理       
         -----------------------------------------------------------------------------*/
        // ＳＶＦフォーム出力
        /*施設別時間割表*/
        ret = svf.VrSetForm("KNJB080.frm", 4);
        setFacility(param);
        
        /*該当データ無し*/
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            ret = svf.VrEndPage();
        }
        
        ret = svf.VrPrint();
        
        // 終了処理
        db2.close();		// DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();	// ストリームを閉じる 
        
    }    //doGetの括り
    
    
    /**
     * 施設別時間割表
     */
    public void setFacility(String param[])
    throws ServletException, IOException
    {
        try {
            PreparedStatement ps = db2.prepareStatement(psFacility(param));//施設別時間割表のレコード
            log.debug("setFacility sql = "+psFacility(param));
            ResultSet rs = ps.executeQuery();
            
            //変数の初期化
            String facility = "0";		//施設ＣＤ
            String day = "0";
            String period = "0";
            int gyo_max = 0;
            int gyo_tmp = 0;
            String day_sub_stf[][] = new String[6][48];//列数行数(１校時分)
            for (int i=0; i<6; i++)
                for (int j=0; j<48; j++)
                    day_sub_stf[i][j] = "";
            String period_name = "";
            String kaipagefield = "";
            
            //施設別時間割表のレコードをセット
            while( rs.next() ){
                kaipagefield = rs.getString("FACCD");
                int ia = rs.getInt("DAYCD");	//曜日コード
                int ib = rs.getInt("PERIODCD");	//校時コード
                //施設ＣＤまたは校時または曜日のブレイク時
                if (!facility.equals(kaipagefield) && !facility.equals("0") || 
                        !period.equals(String.valueOf(ib)) && !period.equals("0") || 
                        !day.equals(String.valueOf(ia)) && !day.equals("0")) {
                    gyo_tmp = 0;
                    //施設ＣＤまたは校時のブレイク時
                    if (!facility.equals(kaipagefield) && !facility.equals("0") || 
                            !period.equals(String.valueOf(ib)) && !period.equals("0")) {
                        setTimeTable(gyo_max,day_sub_stf,period,period_name);   //出力メソッド(１校時分をセット)
                        //施設ＣＤのブレイク時
                        if (!facility.equals(kaipagefield) && !facility.equals("0")) {
                        }
                    }
                }
                //科目名・担当者をセット
//              day_sub_stf[ia-2][gyo_tmp*2] = rs.getString("CHAIRNAME");
                day_sub_stf[ia-2][gyo_tmp*2] = rs.getString("SUBCLASSNAME");
                day_sub_stf[ia-2][gyo_tmp*2+1] = rs.getString("STAFFNAME");
                //保管用フィールドをセット
                day = String.valueOf(ia);
                period = String.valueOf(ib);
                period_name = rs.getString("ABBV1");
                gyo_max = rs.getInt("CNT_MAX");	//施設別校時別の表示MAX行数
                facility = kaipagefield;
                gyo_tmp++;
                //ページヘッダー項目セット
                ret=svf.VrsOut("MASK"  , facility );   //改ページ
                ret=svf.VrsOut("NOTE"  , "使用施設名：　"+rs.getString("FACILITYNAME") );
                setHeader(param);
            }
            //最後のレコード出力
            if (!facility.equals("0")) {
                setTimeTable(gyo_max,day_sub_stf,period,period_name);   //出力メソッド(１校時分をセット)
                nonedata  = true; //該当データなしフラグ
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setFacility read error!",ex);
        }
        
    }  //setFacilityの括り
    
    
    /**施設別時間割表**/
    private String psFacility(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append(    "SELECT NAMECD2 AS PERIODCD ");
            stb.append(    "FROM   V_NAME_MST ");
            stb.append(    "WHERE  YEAR='"+param[1]+"' AND ");
            stb.append(           "NAMECD1='B001' AND ");
            stb.append(           "NAMESPARE2 IS NOT NULL ) ");
            
            //講座時間割(施設別)
            stb.append(",SCH_DAT AS ( ");
            stb.append(    "SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD,W4.CHAIRNAME ");//NO005 DISTINCTをAdd
            stb.append(           ",W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
            if ("1".equals(param[8])) {
                stb.append("       ,W4.CLASSCD ");
                stb.append("       ,W4.SCHOOL_KIND ");
                stb.append("       ,W4.CURRICULUM_CD ");
            }
            stb.append(           ",CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
            stb.append(    "FROM   CHAIR_FAC_DAT W2, SCH_PTRN_DAT W1 ");
            stb.append(           "LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append(                                          "W3.SEMESTER = W1.SEMESTER AND ");
            stb.append(                                          "W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append(           "LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append(                                      "W4.SEMESTER = W1.SEMESTER AND ");
            stb.append(                                      "W4.CHAIRCD = W1.CHAIRCD) ");
            stb.append(	       "LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
            stb.append(	                                         "W5.SEMESTER = W1.SEMESTER AND ");
            stb.append(	                                         "W5.BSCSEQ = W1.BSCSEQ AND ");
            stb.append(	                                         "W5.DAYCD = W1.DAYCD AND ");
            stb.append(	                                         "W5.PERIODCD = W1.PERIODCD AND ");
            stb.append(	                                         "W5.CHAIRCD = W1.CHAIRCD) ");
            stb.append(    "WHERE  W1.YEAR='"+param[1]+"' AND  ");
            stb.append(           "W1.SEMESTER='"+param[5]+"' AND  ");
            stb.append(           "W1.BSCSEQ = "+param[2]+" AND  ");
            stb.append(           "W1.DAYCD BETWEEN '2' AND '7' AND  ");
            stb.append(           "W1.YEAR=W2.YEAR AND  ");
            stb.append(           "W1.SEMESTER=W2.SEMESTER AND  ");
            stb.append(           "W1.CHAIRCD = W2.CHAIRCD AND  ");
            stb.append(           "W2.FACCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND  ");
            stb.append(           "W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ");
            stb.append(    " UNION ");
            stb.append(    "SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD,W4.CHAIRNAME ");//NO005 DISTINCTをAdd
            stb.append(           ",W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
            if ("1".equals(param[8])) {
                stb.append("       ,W4.CLASSCD ");
                stb.append("       ,W4.SCHOOL_KIND ");
                stb.append("       ,W4.CURRICULUM_CD ");
            }
            stb.append(           ",CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
            stb.append(    "FROM   SCH_PTRN_FAC_DAT W2, SCH_PTRN_DAT W1 ");
            stb.append(           "LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append(                                          "W3.SEMESTER = W1.SEMESTER AND ");
            stb.append(                                          "W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append(           "LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append(                                      "W4.SEMESTER = W1.SEMESTER AND ");
            stb.append(                                      "W4.CHAIRCD = W1.CHAIRCD) ");
            stb.append(        "LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
            stb.append(                                          "W5.SEMESTER = W1.SEMESTER AND ");
            stb.append(                                          "W5.BSCSEQ = W1.BSCSEQ AND ");
            stb.append(                                          "W5.DAYCD = W1.DAYCD AND ");
            stb.append(                                          "W5.PERIODCD = W1.PERIODCD AND ");
            stb.append(                                          "W5.CHAIRCD = W1.CHAIRCD) ");
            stb.append(    "WHERE  W1.YEAR='"+param[1]+"' AND  ");
            stb.append(           "W1.SEMESTER='"+param[5]+"' AND  ");
            stb.append(           "W1.BSCSEQ = "+param[2]+" AND  ");
            stb.append(           "W1.DAYCD BETWEEN '2' AND '7' AND  ");
            stb.append(           "W1.YEAR=W2.YEAR AND  ");
            stb.append(           "W1.SEMESTER=W2.SEMESTER AND  ");
            stb.append(           "W1.BSCSEQ=W2.BSCSEQ AND  ");
            stb.append(           "W1.DAYCD=W2.DAYCD AND  ");
            stb.append(           "W1.PERIODCD=W2.PERIODCD AND  ");
            stb.append(           "W1.CHAIRCD = W2.CHAIRCD AND  ");
            stb.append(           "W2.FACCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND  ");
            stb.append(           "W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            
            //施設別校時別の表示MAX行数を取得
            stb.append(",GYO_CNT AS ( ");
            stb.append(    "SELECT T1.PERIODCD,T1.FACCD,MAX(CNT) AS CNT_MAX ");
            stb.append(    "FROM ");
            stb.append(       "(SELECT T1.DAYCD,T1.PERIODCD,T1.FACCD,COUNT(*) AS CNT ");
            stb.append(        "FROM   SCH_DAT T1 ");
            stb.append(        "GROUP BY T1.FACCD,T1.PERIODCD,T1.DAYCD)T1 ");
            stb.append(    "GROUP BY T1.FACCD,T1.PERIODCD ) ");
            
            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append(       ",f_period(T1.PERIODCD) as PERIODCD ");
            stb.append(       ",T1.CHAIRCD ");
            stb.append(       ",T1.FACCD ");
            stb.append(       ",T1.STAFFCD ");
            if ("1".equals(param[8])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append(       ",T1.GROUPCD ");
            stb.append(       ",VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            stb.append(       ",VALUE(L5.FACILITYNAME,'') AS FACILITYNAME ");
            stb.append(       ",VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
            stb.append(       ",L7.ABBV1 ");
            stb.append(       ",L8.GROUPNAME ");
            stb.append(       ",VALUE(T1.CHAIRNAME,'') AS CHAIRNAME ");
            stb.append(       ",G1.CNT_MAX ");
            stb.append("FROM   SCH_DAT T1 ");
            stb.append(       "LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append(       "LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append(       "LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[8])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(       "LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append(       "LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            stb.append(       "LEFT JOIN GYO_CNT G1 ON G1.PERIODCD = T1.PERIODCD AND G1.FACCD = T1.FACCD ");
            stb.append("ORDER BY T1.FACCD,2,T1.DAYCD, ");
            if ("1".equals(param[8])) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else {
                stb.append("       T1.SUBCLASSCD ");
            }
        } catch( Exception e ){
            log.error("psFacility error!",e);
        }
        return stb.toString();
        
    }//psFacility()の括り
    
    
    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void setHeader(String param[])
    throws ServletException, IOException
    {
        try {
            String title = "　施設別時間割表";
            String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度";
            
            ret=svf.VrsOut("nendo"  , nendo + title );
            ret=svf.VrsOut("ymd"	, KNJ_EditDate.h_format_JP(param[6]));
            ret=svf.VrsOut("TITLE"  , "(" + param[7] + ")");
        } catch( Exception ex ) {
            log.error("setHeader read error!",ex);
        }
        
    }  //setHeaderの括り
    
    
    /*------------------------------------*
     * 時間割表のセット                   *
     *------------------------------------*/
    public void setTimeTable(int gyo_max,String day_sub_stf[][],String period,String period_name)
    throws ServletException, IOException
    {
        try {
            if (gyo_max > 4) gyo_max = 4;//最大４つまで表示
            for (int max_len=0; max_len<(gyo_max*2); max_len++){
                for (int len=0; len<6; len++){
                    byte arr_byte[] = day_sub_stf[len][max_len].getBytes();
                    if ( arr_byte.length > 12 ){	//NO001
                        ret=svf.VrsOut("CLASSNAME"+String.valueOf(len+1)+"_2", day_sub_stf[len][max_len]);
                    } else {
                        ret=svf.VrsOut("CLASSNAME"+String.valueOf(len+1), day_sub_stf[len][max_len]);
                    }
                    ret=svf.VrsOut("CLASSCD"+String.valueOf(len+1)  , period);		//校時（グループサプレス）
                    day_sub_stf[len][max_len] = "";
                }
                ret=svf.VrsOut("PERIOD"  , period_name);		//校時(表示用)
                ret = svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.error("setTimeTable read error!",ex);
        }
        
    }  //setTimeTableの括り
    
    
}  //クラスの括り

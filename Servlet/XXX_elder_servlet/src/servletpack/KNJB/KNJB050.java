package servletpack.KNJB;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *	学校教育システム 賢者 [時間割管理]
 *
 *					＜ＫＮＪＢ０５０＞  担当学級・教科割一覧表
 *
 * 2005/04/28 nakamoto 東京都版として修正（１６校時対応）
 * 2005/06/27 nakamoto 名称マスタの「B001」の予備2は、Null以外は時間割表で出力しない校時との意味で追加
 * 2006/01/20 nakamoto NO001 extends HttpServletをカット。Log4jに対応
 * 2006/01/20 nakamoto NO002 職員の変更（ダンスの先生）に対応。
 * 2006/04/21 nakamoto NO005 特別職員が設定されている場合、特別職員のみを表示するよう修正
 * 2006/07/07 nakamoto NO006 ＳＥＱが2桁以上の場合、帳票が出力されない不具合を修正。
 *                     ------パラメータを正常に取得できていなかった。
 *                     ------パラメータを分けて取得するようにした。
 * 2006/10/20 nakamoto NO007 「学期」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 *                     ------一週間の間に前期後期両方ある場合の対応である
 * 2006/10/24 nakamoto NO008 通常時間割：タイトル下の日付範囲は、学期範囲内を表示するよう修正した。
 * 2006/10/25 nakamoto NO009 通常時間割：「年度」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 **/

public class KNJB050 {
    
    private static final Log log = LogFactory.getLog(KNJB050.class);//NO001
    
    Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;		// Databaseクラスを継承したクラス
    String dbname = new String();
    int ret;      		//リターン値
    boolean nonedata  = false; //該当データなしフラグ
    
    final String SISETU = "2";

    String _useSchool_KindField;
    String _SCHOOLKIND;
    String _kubun;
    String _faccdName1;
    String _faccdName2;
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        
        String param[] = new String[18];  // 0-6:画面より受取  7:週最終日(2:週最初日） 10:作成日
        
        // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");      	// データベース名
            if(dbname == null) dbname = "gakumudb";
            param[0] = request.getParameter("RADIO");					// 1:基本 2:通常
            if(param[0].equals("1")){
                param[1] = request.getParameter("T_YEAR");		//年度 NO006
                param[2] = request.getParameter("T_BSCSEQ");	//ＳＥＱ NO006
                param[6] = request.getParameter("T_SEMESTER");	//学期 NO006
            } else{
                String strx = request.getParameter("DATE");				//指定日付
                param[11] = strx.substring(0,4) + "-" + strx.substring(5,7) + "-" + strx.substring(8); // NO007
                //週始めの日付を取得
                int nen  = Integer.parseInt(strx.substring(0,4));
                int tuki = Integer.parseInt(strx.substring(5,7));
                int hi   = Integer.parseInt(strx.substring(8));
                param[1] = (tuki <= 3) ? Integer.toString(nen - 1) : Integer.toString(nen); // 年度の取得 NO009
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                while(cals.get(Calendar.DAY_OF_WEEK) != 2){
                    cals.add(Calendar.DATE,-1);
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[2] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                //週最終日の取得
                cals.add(Calendar.DATE,+6);
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[7] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
            }
            param[3] = request.getParameter("SECTION_CD_NAME1");        //所属from
            param[4] = request.getParameter("SECTION_CD_NAME2");   		//所属to
            //出力項目 1:科目名 2:講座名
            param[16] = request.getParameter("SUBCLASS_CHAIR_DIV");
            param[17] = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            //帳票区分
            _kubun = request.getParameter("KUBUN");
            //施設cd
            _faccdName1 = request.getParameter("FACCD_NAME1");
            _faccdName2 = request.getParameter("FACCD_NAME2");
        } catch( Exception ex ) {
            log.debug("parameter error!", ex);
        }
        
        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);
        
        // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.debug("DB2 open error!", ex);
        }
        
        // ＤＢ検索（コントロールマスター）
        /* 作成日の取得 */
        try {
            String sql = "SELECT CHAR(CTRL_DATE) FROM CONTROL_MST WHERE CTRL_NO='01'";
            db2.query(sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if( rs.next() ){
                String strx = (String)rs.getString(1);
                param[10] = strx.substring(0,10);
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception e ){
            log.debug("sakuseibi error!", e);
        }
        
        
        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();
        
        // svf設定
        ret = svf.VrInit();						   //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定
        
        /*-----------------------------------------------------------------------------
         ＳＶＦ作成処理       
         -----------------------------------------------------------------------------*/
        // ＳＶＦフォーム出力
        /*担当教科別時間割表*/
        final String formId = SISETU.equals(_kubun) ? "KNJB050_2.frm" : "KNJB050.frm";
        ret = svf.VrSetForm(formId, 4);
        if(param[0].equals("2")){
            get_semester(param);
        }
        set_head(param);
        set_chapter1(param);
        
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
    
    
    /*------------------------------------*
     * 担当教科別時間割表ＳＶＦ出力       *
     *------------------------------------*/
    public void set_chapter1(String param[])
    throws ServletException, IOException
    {
        try {
            
            db2.query(Pre_Stat1(param));
            ResultSet rs = db2.getResultSet();
//          log.fatal("sql = "+Pre_Stat1(param));
            log.debug("set_chapter1 sql ok!");
            
            /** SVFフォームへデータをセット **/
            String dayName[] = {"月","火","水","木","金","土","日"};//曜日
            String cmpData = ""; //比較用
            String staff = "0";
            int ia = 0; 		//列数
            int komacnt = 0;
            while( rs.next() ){
                if (SISETU.equals(_kubun)) {
                    cmpData = rs.getString("FACCD");
                } else {
                    cmpData = rs.getString("SECTIONCD") + rs.getString("STAFFCD");
                }
                if(staff.equals(cmpData) == false){
                    if(staff.equals("0") == false){
                        ret = svf.VrEndRecord();
                        nonedata  = true; //該当データなしフラグ
                        ia = 0;
                        komacnt = 0;
                    }
                    staff = cmpData;
                } else{
                    //列のオーバーフロー
                    if(ia == 20){
                        ret = svf.VrEndRecord();
                        ia = 0;
                        komacnt = 0;
                    }
                }
                ia++;
                int dayInt = rs.getInt("DAYCD");
                ret=svf.VrsOut("PERIOD_"+(ia), dayName[dayInt-2] + rs.getString("PERIOD_ABBV"));       //曜日・校時
                ret=svf.VrsOut("HR_NAME_"+(ia), rs.getString("TARGETCLASS"));       //対象クラス
                if ("2".equals(param[16])) {
                    ret=svf.VrsOut("SUBCLASS_NAME_"+(ia)  , rs.getString("CHAIRABBV"));         //講座名
                } else {
                    ret=svf.VrsOut("SUBCLASS_NAME_"+(ia)  , rs.getString("SUBCLASSABBV"));		//科目名
                }
                if (SISETU.equals(_kubun)) {
                    ret=svf.VrsOut("FACILITY_NAME", rs.getString("FACILITYNAME"));  //施設名
                } else {
                    ret=svf.VrsOut("subject1", rs.getString("SECTIONABBV"));        //所属
                    ret=svf.VrsOut("STAFF_NAME", rs.getString("STAFFNAME"));        //職員名
                }
                komacnt = ia;
                ret=svf.VrsOut("LESSONTOTAL"	  , String.valueOf(komacnt));			//担当教諭別コマ数
            }
            //最後のレコード出力
            if(staff.equals("0") == false){
                ret = svf.VrEndRecord();
                nonedata  = true; //該当データなしフラグ
            }
            db2.commit();
            log.debug("set_chapter1 read ok!");
        } catch( Exception ex ) {
            log.debug("set_chapter1 read error!", ex);
        }
        
    }  //set_chapter1の括り
    
    
    
    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head(String param[])
    throws ServletException, IOException
    {
        try {
            final String kubunName = SISETU.equals(_kubun) ? "施設" : "教員";
            ret=svf.VrsOut("nendo1"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度　" + kubunName + "時間割一覧");
            ret=svf.VrsOut("TODAY"     , KNJ_EditDate.h_format_JP(param[10]));
            //基本時間割
            if(param[0].equals("1")){
                ret=svf.VrsOut("nendo2"    , param[1]);
                try {
                    String sql = "SELECT TITLE "
                        + "FROM SCH_PTRN_HDAT "
                        + "WHERE YEAR = '" + param[1] + "' AND BSCSEQ = " + param[2] + " AND SEMESTER = '" + param[6] + "'";
                    PreparedStatement ps = db2.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    if( rs.next() ){
                        ret=svf.VrsOut("TITLE"  , "(" + rs.getString(1) + ")");
                    }
                    rs.close();
                    ps.close();
                } catch( Exception e ){
                    log.debug("set_head read error!", e);
                }
            } else{
                //通常時間割
                ret=svf.VrsOut("term"		, "(" + KNJ_EditDate.h_format_JP(param[2]) + " \uFF5E " + 
                        KNJ_EditDate.h_format_JP(param[7]) + ")");
            }
            log.debug("set_head read ok!");
        } catch( Exception ex ) {
            log.debug("set_head read error!", ex);
        }
        
    }  //set_headの括り
    
    
    
    /*--------------------*
     * 学期取得           *
     *--------------------*/
    public void get_semester(String param[])
    throws ServletException, IOException
    {
        try {
            //指定日付を日付型へ変換
            String strx = new String();
            strx = "9";
            
            //学期期間の取得 NO008Modify
            String sql = new String();
            sql = "SELECT SEMESTER "
                + "      ,case when '"+param[2]+"' < SDATE then SDATE else null end as SDATE "
                + "      ,case when '"+param[7]+"' > EDATE then EDATE else null end as EDATE "
                + "FROM SEMESTER_MST "
                + "WHERE SDATE <= ? AND EDATE >= ? AND YEAR=? AND SEMESTER<>?";
            
            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, param[11]); // NO007 param[2]→param[11]
            ps.setString(2, param[11]); // NO007 param[2]→param[11]
            ps.setString(3, param[1]);
            ps.setString(4, strx);
            ResultSet rs = ps.executeQuery();
            
            if( rs.next() ){
                param[6] = rs.getString("SEMESTER"); //学期
                if (rs.getString("SDATE") != null) param[2] = rs.getString("SDATE"); //学期開始日 NO008Add
                if (rs.getString("EDATE") != null) param[7] = rs.getString("EDATE"); //学期終了日 NO008Add
            }
            if(param[6] == null) param[6] = "0";
            ps.close();
            rs.close();
            db2.commit();
            log.debug("get_semester ok!");
        } catch( Exception e ){
            log.debug("get_semester error!", e);
        }
    }  //get_semesterの括り
    
    
    
    /*----------------*
     * 月の前ゼロ挿入 *
     *----------------*/
    public String h_tuki(int intx)
    throws ServletException, IOException
    {
        String strx = null;
        try {
            strx = "00" + String.valueOf(intx);
            strx = strx.substring(strx.length()-2);
        } catch( Exception ex ) {
            log.debug("h_tuki error!", ex);
        }
        return strx;
    }  //h_tukiの括り
    
    
    /**担当学級・教科一覧表**/
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");//2005.06.27
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[17])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[17])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.TRGTGRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' ");
            stb.append("    GROUP BY W2.CHAIRCD ) ");
            if (SISETU.equals(_kubun)) {
                stb.append(",FACILITY AS ( ");
                stb.append("    SELECT W1.FACCD, W1.FACILITYNAME ");
                stb.append("    FROM   V_FACILITY_MST W1 ");
                stb.append("    WHERE  W1.YEAR='"+ param[1]+"' ");
                stb.append("      AND  W1.FACCD BETWEEN '"+_faccdName1+"' AND '"+_faccdName2+"' ");
                stb.append(" ) ");
            } else {
                stb.append(",SECTION AS ( ");
                stb.append("    SELECT W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME,W1.SECTIONABBV ");
                stb.append("    FROM   V_SECTION_MST W1,V_STAFF_MST W2 ");
                stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND ");
                stb.append("           W1.SECTIONCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
                stb.append("           W2.YEAR=W1.YEAR AND ");
                stb.append("           W2.SECTIONCD=W1.SECTIONCD ) ");
            }
            
            //メイン
            stb.append("SELECT DISTINCT ");
            if (SISETU.equals(_kubun)) {
                stb.append("        F1.FACCD ,F1.FACILITYNAME ");
            } else {
                stb.append("        T3.SECTIONCD ,T3.SECTIONABBV ");
                stb.append("       ,T1.STAFFCD ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            }
            stb.append("       ,T1.GROUPCD ,L8.GROUPNAME ");
            if ("1".equals(param[17])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSABBV ");
            stb.append("       ,T1.CHAIRCD ,VALUE(T1.CHAIRABBV,'') AS CHAIRABBV ");
            stb.append("       ,T1.DAYCD ,f_period(T1.PERIODCD) as PERIODCD ,VALUE(L7.ABBV1,'') AS PERIOD_ABBV ");
            stb.append("       ,L3.GRADE ,L3.HR_CLASS ");
            stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L3.HR_NAMEABBV||'*' ");
            stb.append("             ELSE L3.HR_NAMEABBV END AS TARGETCLASS ");
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[17])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='"+param[1]+"' AND  ");
            stb.append("                                        L3.SEMESTER='"+param[6]+"' AND  ");
            stb.append("                                        L3.GRADE||L3.HR_CLASS=T2.TRGTGRCL  ");
            if (SISETU.equals(_kubun)) {
                stb.append("       ,FACILITY F1 ");
                stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
                stb.append("  AND  T1.FACCD=F1.FACCD  ");
            } else {
                stb.append("       ,SECTION T3 ");
                stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD ");
                stb.append("  AND  T1.STAFFCD=T3.STAFFCD ");
            }
            if (SISETU.equals(_kubun)) {
                stb.append("ORDER BY F1.FACCD, ");
            } else {
                stb.append("ORDER BY T3.SECTIONCD,T1.STAFFCD, ");
            }
            stb.append("        T1.GROUPCD, ");
            if ("1".equals(param[17])) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("       T1.SUBCLASSCD, ");
            }
            stb.append("        T1.CHAIRCD,T1.DAYCD,PERIODCD,L3.GRADE,L3.HR_CLASS ");
        } catch( Exception e ){
            log.debug("Pre_Stat1 error!", e);
        }
        return stb.toString();
        
    }//Pre_Stat1()の括り
    
    
    
}  //クラスの括り

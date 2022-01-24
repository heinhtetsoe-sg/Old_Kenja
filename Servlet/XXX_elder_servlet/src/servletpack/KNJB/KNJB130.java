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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ１３０＞  時間割チェックリスト
 *
 * 2003/11/12 nakamoto 和暦変換に対応
 *
 * 2005/07/20 nakamoto 科目名を科目略称名に変更
 * ------以下、ＣＶＳコメント風に記述------
 * 2006/09/04 nakamoto
 *  // 不要なコメント行を削除した。
 * 2006/09/12 nakamoto
 *  // SQL整理。メソッド（getMainSql,getTrgtClsSql）にまとめた。
 *  // ソース整理。メソッド名およびフィールド名を変更した。
 * 2006/09/13 nakamoto
 *  ★ NO001 ＳＥＱが2桁以上の場合、帳票が出力されない不具合を修正。
 *      // パラメータを正常に取得できていなかった。
 *      // パラメータを分けて取得するようにした。
 * 2006/10/20 nakamoto NO007 「学期」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 *                     ------一週間の間に前期後期両方ある場合の対応である
 * 2006/10/24 nakamoto NO008 通常時間割：タイトル下の日付範囲は、学期範囲内を表示するよう修正した。
 * 2006/10/25 nakamoto NO009 通常時間割：「年度」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 **/

public class KNJB130 {

    private static final Log log = LogFactory.getLog(KNJB130.class);

    Vrw32alp svf = new Vrw32alp();     // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                    // Databaseクラスを継承したクラス
    String dbname = new String();
    boolean nonedata;             // 該当データなしフラグ
    int ret;        // ＳＶＦ応答値
    String _notShowStaffcd;
// 2003/11/12


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        String param[] = new String[14]; // NO007 12→13

    // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");          // データベース名
            if(dbname == null) dbname = "gakumudb";
            param[0] = request.getParameter("RADIO");            // 時間割種別[0] 1:基本 2:通常
            if(param[0].equals("1")){
//              param[1] = strx.substring(0,4);                    // 年度   [1]
//              param[2] = strx.substring(5,6);                    // ＳＥＱ [2]
//              param[11] = strx.substring(7,8);                    //学期
                param[1] = request.getParameter("T_YEAR"); // 年度 NO001
                param[2] = request.getParameter("T_BSCSEQ"); // ＳＥＱ NO001
                param[11] = request.getParameter("T_SEMESTER"); // 学期 NO001
            } else{
                String strx = request.getParameter("DATE1");        // 指定日付
                param[12] = strx.substring(0,4) + "-" + strx.substring(5,7) + "-" + strx.substring(8); // NO007
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
                param[2] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);    // 指定日付F[2]
//              //年度の取得
//              if(tuki >= 1 & tuki <= 3){
//                  param[1] = Integer.toString(nen - 1);    // 年度[1]
//              } else{
//                  param[1] = Integer.toString(nen);        // 年度[1]
//              }
                //週最終日の取得
                cals.add(Calendar.DATE,+6); // NO008
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[3] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);    // 指定日付T[3]
            }
            param[4] = request.getParameter("SECTION_CD_NAME1");        // 所属F[4]
            param[5] = request.getParameter("SECTION_CD_NAME2");           // 所属T[5]
            param[6] = request.getParameter("OUTPUT");                   // 出力順[6]
            param[13] = request.getParameter("useCurriculumcd");
            _notShowStaffcd =  request.getParameter("notShowStaffcd");

            log.debug("parameter ok!");
        } catch( Exception ex ) {
            log.error("parameter error!", ex);
        }

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);
        log.debug("dbname=" + dbname);


    // print設定
//        PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        ret = svf.VrInit();                            // クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);       // PDFファイル名の設定
        ret = svf.VrSetForm("KNJB130.frm", 4);           // SuperVisualFormadeで設計したレイアウト定義態の設定

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
        }
        log.debug("DB2 opened");

    // ＤＢ検索（コントロールマスター）
        /* 作成日の取得 */
        try {
            String sql = "SELECT CHAR(CTRL_DATE) FROM CONTROL_MST WHERE CTRL_NO='01'";
            db2.query(sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if( rs.next() ){
                String strx = (String)rs.getString(1);
                param[7] = strx.substring(0,10);
                log.debug("sakuseibi ok!");
            }
            rs.close();
            ps.close();
        } catch( Exception e ){
            log.error("sakuseibi error!", e);
        }


    // ＤＢ検索（基本時間割Ｈ）
        /* 期間の取得 */
        if(param[0].equals("1")){
            try {
                String sql = "SELECT TITLE "
                           + "FROM SCH_PTRN_HDAT "
                           + "WHERE YEAR = ? AND BSCSEQ = ? AND SEMESTER = ?";
                PreparedStatement ps = db2.prepareStatement(sql);
                ps.setString(1, param[1]);
                ps.setString(2, param[2]);
                ps.setString(3, param[11]);
                ResultSet rs = ps.executeQuery();
                if( rs.next() ){
                    param[10] = rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("[LHA080]BSCHEDULE_HDAT error!", e);
            }
        }


    /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理       
      -----------------------------------------------------------------------------*/
        nonedata = false;         // 該当データなしフラグ(MES001.frm出力用)

        // 通常時間割データ
        if(param[0].equals("2"))
            getSemester(param);

            setMain(param);

    // ＳＶＦフォーム出力
        /*該当データ無し*/
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            ret = svf.VrEndPage();
        }

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }    //doGetの括り


    public void setMain(String param[])
                     throws ServletException, IOException
    {
        try {
            PreparedStatement ps = db2.prepareStatement(getMainSql(param));
            ResultSet rs = ps.executeQuery();
            log.debug("setMain sql1 ok!");

            // タイトル
            if (param[0].equals("1")) {
                ret = svf.VrsOut("jikanwari"    , "基本");
                ret = svf.VrsOut("TITLE"        , "(" + param[10] + ")");
            } else {
                ret = svf.VrsOut("jikanwari"    , "通常");
                ret = svf.VrsOut("term"         , "(" + KNJ_EditDate.h_format_JP(param[2]) + " \uFF5E " + KNJ_EditDate.h_format_JP(param[3]) + ")");
            }
            ret = svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度");
            ret = svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(param[7]));

            while( rs.next() ){
                ret = svf.VrsOut("belong"       , rs.getString("SECTIONABBV")); // 所属
                ret = svf.VrsOut("staff"        , (rs.getString("STAFFNAME_SHOW") == null) ? "NONE" : rs.getString("STAFFNAME_SHOW")); // 職員名
                ret = svf.VrsOut("lecture_no"   , rs.getString("CHAIRCD")); // 講座No.
                ret = svf.VrsOut("class_no"     , (rs.getString("CNT_GRCL") == null) ? "NONE" : rs.getString("CNT_GRCL")); // 学級数
                ret = svf.VrsOut("classname"    , (rs.getString("CHAIRNAME") == null) ? "NONE" : rs.getString("CHAIRNAME")); // 講座名
                ret = svf.VrsOut("count_flg"    , rs.getString("COUNTFLG_MARK")); // 集計　○：あり　×：なし　△：ありとなしが混在
                // クラス状況
                if (rs.getString("CNT_SCHNO").equals("0")){
                    ret = svf.VrsOut("class"    , "NONE");
                } else{
                    ret = svf.VrsOut("AT1" , rs.getString("CNT_SCHNO"));
                    ret = svf.VrsOut("AT2" , rs.getString("CNT_NULL_ROW"));
                    ret = svf.VrsOut("AT3" , rs.getString("CNT_NULL_COLUMN"));
                }
                ret = svf.VrsOut("optional_code", (rs.getString("GROUPNAME") == null) ? "NONE" : rs.getString("GROUPNAME")); // 選択科目
                ret = svf.VrsOut("course_code"  , (rs.getString("CLASSNAME") == null) ? "NONE" : rs.getString("CLASSNAME")); // 教科
                ret = svf.VrsOut("subject_code" , (rs.getString("SUBCLASSABBV") == null) ? "NONE" : rs.getString("SUBCLASSABBV")); // 科目

                // 対象クラス
                PreparedStatement ps2 = db2.prepareStatement(getTrgtClsSql(param));
                ps2.setString(1, rs.getString("CHAIRCD"));
                ps2.setString(2, rs.getString("GROUPCD"));
                ResultSet rs2 = ps2.executeQuery();
                String strobject = "";
                int ia = 0;
                while(rs2.next()){
                    if(ia == 0){
                        strobject = rs2.getString("HR_NAME");
                    } else{
                        strobject = strobject + "," + rs2.getString("HR_NAME");
                    }
                    ia++;
                }
                rs2.close();
                ps2.close();
                ret = svf.VrsOut("object"         , strobject); // 対象クラス

                // エラーあり表示
                String strbiko = "  ";
                if (rs.getString("STAFFNAME_SHOW") == null) strbiko = "NONE";
                if (rs.getString("CNT_GRCL") == null) strbiko = "NONE";
                if (rs.getString("CHAIRNAME") == null) strbiko = "NONE";
                if (rs.getString("CNT_SCHNO").equals("0")) strbiko = "NONE";
                if (rs.getString("GROUPNAME") == null) strbiko = "NONE";
                if (rs.getString("CLASSNAME") == null) strbiko = "NONE";
                if (rs.getString("SUBCLASSABBV") == null) strbiko = "NONE";
                ret = svf.VrsOut("biko"         , strbiko); // エラーあり表示

                ret = svf.VrEndRecord();
                nonedata = true; //該当データなしフラグ
            }
            rs.close();
            ps.close();
            db2.commit();
            log.debug("setMain read ok!");
        } catch( Exception ex ){
            log.error("setMain read error!", ex);
        }

    }    //setMainの括り


    /**講座時間割（メイン）**/
    private String getMainSql(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            // 名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            // 講座時間割
            stb.append(",SCH_DAT AS (  ");
            stb.append("    SELECT DISTINCT W1.CHAIRCD, W4.CHAIRNAME  ");
            stb.append("           ,W3.STAFFCD  ");
            stb.append("           ,W4.SUBCLASSCD,W4.GROUPCD  ");
            if ("1".equals(param[13])) {
                stb.append("       ,W4.CLASSCD ");
                stb.append("       ,W4.SCHOOL_KIND ");
                stb.append("       ,W4.CURRICULUM_CD ");
            }
            if (param[0].equals("1")) {// 基本
                stb.append("    FROM   SCH_PTRN_DAT W1  ");
            } else {// 通常
                stb.append("    FROM   SCH_CHR_DAT W1  ");
            }
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND  ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND  ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD)  ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND  ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND  ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD)  ");
            if (param[0].equals("1")) {// 基本
                stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND   ");
                stb.append("           W1.SEMESTER='"+param[11]+"' AND   ");
                stb.append("           W1.BSCSEQ = "+param[2]+" AND   ");
                stb.append("           W1.DAYCD BETWEEN '2' AND '7' AND ");
            } else {// 通常
                stb.append("    WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[3]+"' AND  ");
                stb.append("           DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) )  ");
            // 集計フラグ
            stb.append(",SCH_COUNTFLG AS (  ");
            stb.append("    SELECT W1.CHAIRCD, ");
            stb.append("           MAX(VALUE(COUNTFLG, '1')) AS COUNTFLG_MAX, ");
            stb.append("           MIN(VALUE(COUNTFLG, '1')) AS COUNTFLG_MIN, ");
            stb.append("           CASE WHEN MAX(VALUE(COUNTFLG, '1')) != '0' AND MIN(VALUE(COUNTFLG, '1')) != '0' THEN '○' ");
            stb.append("                WHEN MAX(VALUE(COUNTFLG, '1'))  = '0' AND MIN(VALUE(COUNTFLG, '1'))  = '0' THEN '×' ");
            stb.append("                WHEN MAX(VALUE(COUNTFLG, '1')) != '0' AND MIN(VALUE(COUNTFLG, '1'))  = '0' THEN '△' ");
            stb.append("           END AS COUNTFLG_MARK ");
            if (param[0].equals("1")) {// 基本
                stb.append("    FROM   SCH_PTRN_COUNTFLG_DAT W1  ");
                stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND   ");
                stb.append("           W1.SEMESTER='"+param[11]+"' AND   ");
                stb.append("           W1.BSCSEQ = "+param[2]+" AND   ");
                stb.append("           W1.DAYCD BETWEEN '2' AND '7' AND ");
            } else {// 通常
                stb.append("    FROM   SCH_CHR_COUNTFLG W1  ");
                stb.append("    WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[3]+"' AND  ");
                stb.append("           DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD)  ");
            stb.append("    GROUP BY W1.CHAIRCD )  ");
            // 講座クラス
            stb.append(",CHAIR_CLS AS (  ");
            stb.append("    SELECT W1.CHAIRCD,W1.GROUPCD,W2.TRGTGRADE||W2.TRGTCLASS AS GRCL,W3.HR_NAME  ");
            stb.append("    FROM   CHAIR_DAT W1,  ");
            stb.append("           CHAIR_CLS_DAT W2, ");
            stb.append("           SCHREG_REGD_HDAT W3 ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND  ");
            stb.append("           W1.SEMESTER  = '"+param[11]+"' AND ");
            stb.append("           W2.YEAR=W1.YEAR AND ");
            stb.append("           W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("           W2.GROUPCD=W1.GROUPCD AND  ");
            stb.append("          (W2.CHAIRCD='0000000' OR W2.CHAIRCD=W1.CHAIRCD) AND  ");
            stb.append("           W2.YEAR=W3.YEAR AND ");
            stb.append("           W2.SEMESTER=W3.SEMESTER AND  ");
            stb.append("           W2.TRGTGRADE=W3.GRADE AND ");
            stb.append("           W2.TRGTCLASS=W3.HR_CLASS )  ");
            stb.append(",CNT_CHAIR_CLS AS (  ");
            stb.append("    SELECT CHAIRCD,count(*) AS CNT_GRCL  ");
            stb.append("    FROM   CHAIR_CLS ");
            stb.append("    GROUP BY CHAIRCD )  ");
            // 講座生徒
            stb.append(",CHAIR_STD AS ( ");
            stb.append("    SELECT CHAIRCD,SCHREGNO,APPDATE,APPENDDATE,ROW,COLUMN ");
            stb.append("      FROM CHAIR_STD_DAT ");
            stb.append("     WHERE YEAR = '"+param[1]+"' ");
            stb.append("       AND SEMESTER = '"+param[11]+"' ");
            if (param[0].equals("2")) {// 通常
//              stb.append("       AND '"+param[2]+"' BETWEEN APPDATE AND APPENDDATE ");
                stb.append("       AND '"+param[12]+"' BETWEEN APPDATE AND APPENDDATE "); // NO007
            }
            stb.append("    ) ");
            stb.append(",CNT_CHAIR_STD AS ( ");
            stb.append("    SELECT CHAIRCD, ");
            stb.append("           count(SCHREGNO) AS CNT_SCHNO, ");
            stb.append("           sum(CASE WHEN ROW IS NULL THEN 1 ELSE 0 END) AS CNT_NULL_ROW, ");
            stb.append("           sum(CASE WHEN COLUMN IS NULL THEN 1 ELSE 0 END) AS CNT_NULL_COLUMN ");
            stb.append("      FROM CHAIR_STD ");
            stb.append("     WHERE (CHAIRCD,SCHREGNO,APPDATE) IN ( ");
            stb.append("                SELECT CHAIRCD,SCHREGNO,MAX(APPDATE) AS MAX_APPDATE ");
            stb.append("                  FROM CHAIR_STD ");
            stb.append("                 GROUP BY CHAIRCD,SCHREGNO) ");
            stb.append("     GROUP BY CHAIRCD ) ");
            // 所属
            stb.append(",SECTION AS (  ");
            stb.append("    SELECT W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME_SHOW,W1.SECTIONABBV  ");
            stb.append("    FROM   V_SECTION_MST W1,V_STAFF_MST W2  ");
            stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND  ");
            stb.append("           W1.SECTIONCD BETWEEN '"+param[4]+"' AND '"+param[5]+"' AND  ");
            stb.append("           W2.YEAR=W1.YEAR AND  ");
            stb.append("           W2.SECTIONCD=W1.SECTIONCD )  ");

            // メイン
            stb.append("SELECT T3.SECTIONCD  ");
            stb.append("       ,T3.SECTIONABBV  ");
            stb.append("       ,T3.STAFFCD  ");
            if ("1".equals(_notShowStaffcd)) {
                stb.append("       ,T3.STAFFNAME_SHOW as STAFFNAME_SHOW  ");
            } else {
                stb.append("       ,T3.STAFFCD || ' ' || T3.STAFFNAME_SHOW as STAFFNAME_SHOW  ");
            }
            stb.append("       ,T1.CHAIRCD  ");
            stb.append("       ,T1.CHAIRNAME ");
            stb.append("       ,T2.CNT_GRCL  ");
            stb.append("       ,T1.GROUPCD  ");
            stb.append("       ,case when T1.GROUPCD = '0000' then T1.GROUPCD ");
            stb.append("             else T1.GROUPCD || ' ' || L8.GROUPNAME end as GROUPNAME  ");
            stb.append("       ,substr(T1.SUBCLASSCD,1,2) AS CLASSCD  ");
            stb.append("       ,substr(T1.SUBCLASSCD,1,2) || ' ' || L5.CLASSNAME as CLASSNAME  ");
            if ("1".equals(param[13])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.SUBCLASSCD || ' ' || L6.SUBCLASSABBV as SUBCLASSABBV  ");
            stb.append("       ,value(T5.CNT_SCHNO,0) as CNT_SCHNO ");
            stb.append("       ,value(T5.CNT_NULL_ROW,0) as CNT_NULL_ROW ");
            stb.append("       ,value(T5.CNT_NULL_COLUMN,0) as CNT_NULL_COLUMN ");
            stb.append("       ,CASE WHEN T6.COUNTFLG_MARK IS NULL THEN '○' ELSE T6.COUNTFLG_MARK END AS COUNTFLG_MARK ");
            stb.append("FROM   SCH_DAT T1  ");
            stb.append("       LEFT JOIN CLASS_MST L5 ON L5.CLASSCD = substr(T1.SUBCLASSCD,1,2)  ");
            if ("1".equals(param[13])) {
                stb.append("            AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD  ");
            if ("1".equals(param[13])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD  ");
            stb.append("       LEFT JOIN CNT_CHAIR_CLS T2 ON T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("       LEFT JOIN CNT_CHAIR_STD T5 ON T5.CHAIRCD = T1.CHAIRCD ");
            stb.append("       LEFT JOIN SCH_COUNTFLG T6 ON T6.CHAIRCD = T1.CHAIRCD ");
            stb.append("       ,SECTION T3  ");
            stb.append("WHERE  T1.STAFFCD=T3.STAFFCD  ");
            if (param[6].equals("1")) {
                stb.append("ORDER BY T3.SECTIONCD,T3.STAFFCD,T1.CHAIRCD ");
            } else {
                stb.append("ORDER BY T1.GROUPCD,T3.SECTIONCD,T3.STAFFCD,T1.CHAIRCD ");
            }
//log.debug("sql = "+stb.toString());
        } catch( Exception e ){
            log.error("getMainSql error!",e);
        }
        return stb.toString();
    }//getMainSql()の括り


    /**講座クラス**/
    private String getTrgtClsSql(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            // 講座クラス
            stb.append("SELECT W1.CHAIRCD,W1.GROUPCD,W2.TRGTGRADE||W2.TRGTCLASS AS GRCL,W3.HR_NAME  ");
            stb.append("FROM   CHAIR_DAT W1,  ");
            stb.append("       CHAIR_CLS_DAT W2, ");
            stb.append("       SCHREG_REGD_HDAT W3 ");
            stb.append("WHERE  W1.YEAR = '"+param[1]+"' AND  ");
            stb.append("       W1.SEMESTER  = '"+param[11]+"' AND ");
            stb.append("       W1.CHAIRCD = ? AND  ");
            stb.append("       W1.GROUPCD = ? AND ");
            stb.append("       W2.YEAR=W1.YEAR AND ");
            stb.append("       W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("       W2.GROUPCD=W1.GROUPCD AND  ");
            stb.append("      (W2.CHAIRCD='0000000' OR W2.CHAIRCD=W1.CHAIRCD) AND  ");
            stb.append("       W2.YEAR=W3.YEAR AND ");
            stb.append("       W2.SEMESTER=W3.SEMESTER AND  ");
            stb.append("       W2.TRGTGRADE=W3.GRADE AND ");
            stb.append("       W2.TRGTCLASS=W3.HR_CLASS  ");
            stb.append("ORDER BY W2.TRGTGRADE||W2.TRGTCLASS ");
        } catch( Exception e ){
            log.error("getTrgtClsSql error!",e);
        }
        return stb.toString();
    }//getTrgtClsSql()の括り


    /*--------------------*
     * 学期取得           *
     *--------------------*/
    public void getSemester(String param[])
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
                + "      ,case when '"+param[3]+"' > EDATE then EDATE else null end as EDATE "
                + "FROM SEMESTER_MST "
                + "WHERE SDATE <= ? AND EDATE >= ? AND YEAR=? AND SEMESTER<>?";

            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, param[12]); // NO007 param[2]→param[12]
            ps.setString(2, param[12]); // NO007 param[2]→param[12]
            ps.setString(3, param[1]);
            ps.setString(4, strx);
            ResultSet rs = ps.executeQuery();

            if( rs.next() ){
                param[11] = rs.getString("SEMESTER"); //学期
                if (rs.getString("SDATE") != null) param[2] = rs.getString("SDATE"); //学期開始日 NO008Add
                if (rs.getString("EDATE") != null) param[3] = rs.getString("EDATE"); //学期終了日 NO008Add
            }
            if(param[11] == null) param[11] = "0";
            ps.close();
            rs.close();
            db2.commit();
            log.debug("getSemester ok!");
        } catch( Exception e ){
            log.error("getSemester error!", e);
        }
    }  //getSemesterの括り


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
            log.error("h_tuki error!", ex);
        }
        return strx;
    }  //h_tukiの括り



}    //クラスの括り


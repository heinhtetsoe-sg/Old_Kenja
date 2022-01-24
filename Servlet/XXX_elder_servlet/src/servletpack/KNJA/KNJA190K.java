// kanji=漢字
/*
 * $Id: 2553c0409ee93c57e6b18cca91d061ea8e9dc1c1 $
 *
 * 作成日: 2004/07/20
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_Semester;


/**
 * <<住所のタックシール印刷>>。
 */

/*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１９０Ｋ＞  生徒住所のタックシール印刷
 *
 * 2004/07/20 yamashiro・KNJA190より複写作成
 *
 * 2005/07/20 nakamoto クラス指定を追加---NO001
 */

public class KNJA190K {

    private static final Log log = LogFactory.getLog(KNJA190K.class);
    
    private String _useAddrField2;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        String param[] = new String[12];//---NO001

    // パラメータの取得
        try {
            param[6] = "様";        //  様

            param[0] = request.getParameter("YEAR");                    // 年度
            param[1] = request.getParameter("GAKKI");                   // 学期
            param[3] = request.getParameter("POROW");                   // 行
            param[4] = request.getParameter("POCOL");                   // 列
            param[5] = request.getParameter("OUTPUT");                  // 1:保護者情報印刷2:生徒情報印刷

            param[10] = request.getParameter("DISP");                       //1:個人,2:クラス---NO001
            param[11] = request.getParameter("OUTPUT2");                // 1:学籍番号順 2:年組・番号順

            _useAddrField2 = request.getParameter("useAddrField2");

            //対象学籍番号の編集
            String scool[] = request.getParameterValues("category_name");   // 学籍番号
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for(int ia=0 ; ia<scool.length ; ia++){
                if(scool[ia] == null)   break;
                if(ia>0)    sbx.append(",");
                sbx.append("'");
                if (param[10].equals("1")) sbx.append((scool[ia]).substring(0,(scool[ia]).indexOf("-")));//---NO001
                if (param[10].equals("2")) sbx.append(scool[ia]);//---NO001
                sbx.append("'");
            }
            sbx.append(")");
            param[2] = sbx.toString();

        } catch( Exception ex ) {
            log.error("parameter error!", ex);
        }


    // print設定
//        PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        int ret = svf.VrInit();                         //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

    // ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
        }

    //  学期期間の取得(住所取得用)
        try {
            KNJ_Semester semester = new KNJ_Semester();                     //クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = 
                        semester.Semester(db2,param[0],param[1]);
            param[7] = returnval.val2;                                          //学期開始日
            param[8] = returnval.val3;                                          //学期終了日
        } catch( Exception e ){
            log.error("Semester sdate get error!", e);
        }

    //  ＳＶＦ作成処理
        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);
        boolean nonedata = Set_Detail(db2,svf,param);   //印刷処理

        //該当データ無しフォーム出力
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }//doGetの括り



    /** 印刷処理 **/
    private boolean Set_Detail(DB2UDB db2,Vrw32alp svf,String param[]){

        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = (param[5].equals("1")) ? Set_Stat1(param) : Set_Stat2(param);
			ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm("KNJA190K.frm", 4); //SuperVisualFormadeで設計したレイアウト定義態の設定
            int ia = Integer.parseInt(param[3]) - 1;  //行
            int ib = Integer.parseInt(param[4]) - 1;  //列
            for (int ic = 0; ic < ia; ic++) {
            	svf.VrEndRecord();
            }
            while (rs.next()) {
                if (ib == 3) {
                    svf.VrEndRecord();
                    nonedata = true;
                    ib = 0;
                    for (int ic = 1; ic < 4; ic++) {
                        svf.VrsOut("ZIPCODE"+ic    ,"");  //郵便番号
                        svf.VrsOut("ADDRESS1_"+ic  ,"");  //住所
                        svf.VrsOut("SCHOOLNAME"+ic ,"");  //氏名
                        svf.VrsOut("STUDENT"+ic    ,"");  //学級名称＋生徒名
                    }
                }
                ib++;
                svf.VrsOut("ZIPCODE" + ib    ,"〒" + rs.getString("ZIPCD"));        //郵便番号
                printAddress(svf, rs.getString("ADDR1"), rs.getString("ADDR2"), ib);
                svf.VrsOut("SCHOOLNAME" + ib, h_finschoolname(rs.getString("NAME"), param[6]));  //氏名
                if (param[5].equals("1")) {
                    svf.VrsOut("STUDENT" + ib, "(" + rs.getString("HRCLASS_NAME") + "  " + rs.getString("SCH_NAME") + "　様)");    //学級名称＋生徒名
                }
            }
            if (ib > 0) {
                svf.VrEndRecord();
                nonedata = true;
            }
            log.debug("boolean Set_Detail() read ok!");
        } catch(Exception ex) {
            log.error("boolean Set_Detail() read error!", ex);
        } finally {
        	db2.commit();
        	DbUtils.closeQuietly(null, ps, rs);
        }

        if (nonedata) {
        	svf.VrPrint();
        }
        return nonedata;

    }//boolean Set_Detail()の括り



    /** 保護者出力用ＳＱＬ **/
    private String Set_Stat1(String param[]){

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT VALUE(GUARD_ZIPCD,'')AS ZIPCD,");
            //stb.append(         "VALUE(GUARD_ADDR1,'')||VALUE(GUARD_ADDR2,'')AS ADDRESS,");
            stb.append(       "GUARD_ADDR1 AS ADDR1,GUARD_ADDR2 AS ADDR2,");
            stb.append(       "GUARD_NAME AS NAME,W3.NAME AS SCH_NAME,W4.HR_NAME AS HRCLASS_NAME ");
            stb.append("FROM   SCHREG_REGD_DAT W1,GUARDIAN_DAT W2,SCHREG_BASE_MST W3,SCHREG_REGD_HDAT W4 ");
        //---NO001
        if (param[10].equals("1")) 
            stb.append("WHERE  W1.SCHREGNO IN "+param[2]+" AND ");
        if (param[10].equals("2")) 
            stb.append("WHERE  W1.GRADE||W1.HR_CLASS IN "+param[2]+" AND ");

            stb.append(       "W1.YEAR='"+param[0]+"'AND W1.SEMESTER='"+param[1]+"'AND W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append(       "W1.SCHREGNO=W3.SCHREGNO AND W4.YEAR='"+param[0]+"'AND W4.SEMESTER='"+param[1]+"'AND ");
            stb.append(       "W4.YEAR=W1.YEAR AND W4.SEMESTER=W1.SEMESTER AND W4.GRADE=W1.GRADE AND W4.HR_CLASS=W1.HR_CLASS ");
            stb.append("ORDER BY ");
            if ("1".equals(param[11])) {
                stb.append("W1.SCHREGNO");
            } else {
                stb.append("W1.GRADE, W1.HR_CLASS, W1.ATTENDNO");
            }
        } catch( Exception ex ){
            log.error("[KNJA190K]String Set_Stat1() error!", ex);
        }

        return stb.toString();

    }//String Set_Stat1()の括り



    /** 生徒出力用ＳＱＬ **/
    private String Set_Stat2(String param[]){

        StringBuffer stb = new StringBuffer();
        try {
            //---NO001---↓---
            stb.append("WITH SCHNO AS ( ");
            stb.append(     "SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO ");
            stb.append(     "FROM   SCHREG_REGD_DAT ");
            if (param[10].equals("1")) 
                stb.append( "WHERE  SCHREGNO IN "+param[2]+" AND ");
            if (param[10].equals("2")) 
                stb.append( "WHERE  GRADE||HR_CLASS IN "+param[2]+" AND ");
            stb.append(            "YEAR='"+param[0]+"'AND SEMESTER='"+param[1]+"' ");
            stb.append(     ") ");
            //---NO001---↑---

            stb.append("SELECT T1.SCHREGNO,");
            stb.append(       "VALUE(T1.ZIPCD,'') AS ZIPCD,");
            stb.append(       "T1.ADDR1,T1.ADDR2,T2.NAME AS NAME ");
            stb.append("FROM   SCHREG_BASE_MST T2 ");
            stb.append(       "INNER JOIN (");
            stb.append(            "SELECT SCHREGNO,");
            stb.append(                   "ZIPCD,ADDR1,ADDR2 ");
            stb.append(            "FROM   SCHREG_ADDRESS_DAT W1 ");
            stb.append(            "WHERE  (W1.SCHREGNO,W1.ISSUEDATE) IN ( ");
            stb.append(                            "SELECT SCHREGNO,MAX(ISSUEDATE) ");
            stb.append(                            "FROM   SCHREG_ADDRESS_DAT W2 ");
            stb.append(                            "WHERE  W2.ISSUEDATE <= '" + param[8] + "' AND ");
            stb.append(                                  "(W2.EXPIREDATE IS NULL OR ");
            stb.append(                                   "W2.EXPIREDATE >= '" + param[7] + "') AND ");
            stb.append(                                   "W2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");//---NO001
//          stb.append(                                   "W2.SCHREGNO IN " + param[2] + " ");
            stb.append(                            "GROUP BY SCHREGNO ) ");
            stb.append(       ")T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(       "INNER JOIN SCHNO T3 ON T1.SCHREGNO = T3.SCHREGNO ");
            stb.append("ORDER BY ");
            if ("1".equals(param[11])) {
                stb.append("    T1.SCHREGNO");
            } else {
                stb.append("    T3.GRADE || T3.HR_CLASS || T3.ATTENDNO");
            }
        } catch( Exception ex ){
            log.error("String Set_Stat2() error!", ex);
        }

        return stb.toString();

    }//String Set_Stat2()の括り


    /**
     * 住所を印字します。
     * @param svf
     * @param addr1 住所1
     * @param addr2 住所2
     * @param ib 列名（1〜3）
     */
    private void printAddress(
            final Vrw32alp svf,
            String addr1, 
            final String addr2,
            final int ib
    ) {
        if (null != addr1 && 3 <= addr1.length()) {
            if ("大阪府".equals(addr1.substring(0,3))) {
                addr1 = addr1.substring(3);
            }
        }
        boolean overAddr1Len2 = isOverStringLength(addr1, 50);
        boolean overAddr2len2 = isOverStringLength(addr2, 50);
        boolean overAddr1Len = isOverStringLength(addr1, 40);
        boolean overAddr2len = isOverStringLength(addr2, 40);
        String addressFieldDiv = ("1".equals(_useAddrField2) && (overAddr1Len2 || overAddr2len2)) ? "3" : (overAddr1Len || overAddr2len) ? "2": "1";
        if (null != addr1) {
            svf.VrsOut("ADDRESS1_" + ib + "_" + addressFieldDiv, addr1);
        }
        if (null != addr2) {
            svf.VrsOut("ADDRESS2_" + ib + "_" + addressFieldDiv, addr2);
        }
    }

    
   /**
    * @param str
    * @param i
    * @return String str の長さ(byte)が int i を超えた場合Trueを戻します。
    */
    public static boolean isOverStringLength(
           final String str, 
           final int i
   ) {
       if (null == str) { return false; }
       if (0 == str.length()) { return false; }
       byte arrbyte[] = new byte[i + 2];
       try {
        arrbyte = str.getBytes( "MS932" );
       } catch (UnsupportedEncodingException e) {
           log.error("UnsupportedEncodingException", e);
       }
       return (i < arrbyte.length);
   }

    
    /** 氏名の編集 **/
    public String h_finschoolname(final String finschoolname1, final String finschoolname2) {

        final StringBuffer finschoolname = new StringBuffer();
        try {
            if (finschoolname1 != null) {
                finschoolname.append(finschoolname1);
                byte[] bytes = finschoolname1.getBytes("MS932");

                int j=0;
                if (bytes.length > 18) {
                	j=2;
                }

                for (int i = bytes.length; i < (22 * j - 2); i++) {
                    finschoolname.append(" ");
                }

                if (j == 0) {
                    finschoolname.append(" ");
                    finschoolname.append(" ");
                }
                if (finschoolname2 != null) {
                	finschoolname.append(finschoolname2);
                }
            }
            if (finschoolname == null) {
            	finschoolname.append(" ");
            }
        } catch (Exception ex) {
            log.error("h_finschoolname error!", ex);
        }
        return finschoolname.toString();
    }//h_finschoolnameの括り



}//クラスの括り


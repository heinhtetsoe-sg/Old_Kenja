// kanji=漢字
/*
 * $Id: 7892fb329ae639ef256994f31fcd665c6b28fba7 $
 *
 * 作成日: 2009/09/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者
 *
 *                  ＜ＫＮＪＥ１４０Ｔ＞  概評人数一覧表
 *
 **/

public class KNJE140T {

    private static final Log log = LogFactory.getLog(KNJE140T.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

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
        } catch (Exception ex) {
            log.error("DB2 open error!");
            return;
        }
        
        Param param = createParam(db2, request);
        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        if (setSvfMain(db2, svf, param)) {
            nonedata = true;  //帳票出力のメソッド
        }

        //  該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り
    
    /**
     *  svf print 印刷処理全印刷
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        Param param
    ) {
        boolean nonedata  = false;
        int gyo = 1;
        int allcntA = 0;
        int allcntB = 0;
        int allcntC = 0;
        int allcntD = 0;
        int allcntE = 0;
        int allcnt = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrSetForm("KNJE140T.frm", 4);          //セットフォーム
            final String sql = preStat(param);
            log.debug(" sql = " + sql);
            ps  = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (gyo > 20) {
                    gyo = 1;
                }
                //ヘッダ
                svf.VrsOut("NENDO"            ,param._year + "年度");         //年度
                svf.VrsOut("DATE"             ,param._date);         //作成日
                svf.VrsOut("GRADE"            ,param._printGrade);

                //明細
                svf.VrsOut("GROUPNAME"    ,rs.getString("GROUP_NAME"));   //グループ名
                svf.VrsOut("A_MEMBER"     ,rs.getString("A_SUM"));        //Ａ合計
                svf.VrsOut("B_MEMBER"     ,rs.getString("B_SUM"));        //Ｂ合計
                svf.VrsOut("C_MEMBER"     ,rs.getString("C_SUM"));        //Ｃ合計
                svf.VrsOut("D_MEMBER"     ,rs.getString("D_SUM"));        //Ｄ合計
                svf.VrsOut("E_MEMBER"     ,rs.getString("E_SUM"));        //Ｅ合計
                svf.VrsOut("TOTAL_MEMBER" ,rs.getString("ALL_SUM"));      //合計

                allcntA = allcntA+Integer.parseInt(rs.getString("A_SUM"));
                allcntB = allcntB+Integer.parseInt(rs.getString("B_SUM"));
                allcntC = allcntC+Integer.parseInt(rs.getString("C_SUM"));
                allcntD = allcntD+Integer.parseInt(rs.getString("D_SUM"));
                allcntE = allcntE+Integer.parseInt(rs.getString("E_SUM"));
                allcnt  = allcnt +Integer.parseInt(rs.getString("ALL_SUM"));

                gyo++;
                svf.VrEndRecord();
                nonedata  = true ;
            }
            if (nonedata) {
                if (gyo > 20) gyo = 1;
                for (;gyo < 20; gyo++) {
                    svf.VrEndRecord();
                }
                //合計
                svf.VrsOut("GROUPNAME"    ,"合計"); //グループ名
                svf.VrsOut("A_MEMBER"     ,String.valueOf(allcntA));      //Ａ合計
                svf.VrsOut("B_MEMBER"     ,String.valueOf(allcntB));      //Ｂ合計
                svf.VrsOut("C_MEMBER"     ,String.valueOf(allcntC));      //Ｃ合計
                svf.VrsOut("D_MEMBER"     ,String.valueOf(allcntD));      //Ｄ合計
                svf.VrsOut("E_MEMBER"     ,String.valueOf(allcntE));      //Ｅ合計
                svf.VrsOut("TOTAL_MEMBER" ,String.valueOf(allcnt));       //合計
                svf.VrEndRecord();
            }
        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**データ　取得**/
    private String preStat(Param param) {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH COURSET AS ( ");
        stb.append("SELECT ");
        if ("1".equals(param._gaihyouGakkaBetu)) {
            stb.append("    T1.COURSECD, ");
            stb.append("    T1.MAJORCD, ");
            stb.append("    T1.COURSECODE, ");
            stb.append("    T2.COURSENAME || T3.MAJORNAME || T4.COURSECODENAME AS GROUP_NAME ");
        } else if ("2".equals(param._gaihyouGakkaBetu)) {
            stb.append("    '0' AS COURSECD, ");
            stb.append("    T5.GROUP_CD AS MAJORCD, ");
            stb.append("    '0000' AS COURSECODE, ");
            stb.append("    T6.GROUP_NAME ");
        } else {
            stb.append("    T1.COURSECD, ");
            stb.append("    T1.MAJORCD, ");
            stb.append("    '0000' AS COURSECODE, ");
            stb.append("    T3.MAJORNAME AS GROUP_NAME ");
        }
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT T1 ");
        if ("1".equals(param._gaihyouGakkaBetu)) {
            stb.append("    INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
            stb.append("    INNER JOIN MAJOR_MST T3 ON T3.MAJORCD = T1.MAJORCD ");
            stb.append("        AND T3.COURSECD = T1.COURSECD ");
            stb.append("    INNER JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ");
        } else if ("2".equals(param._gaihyouGakkaBetu)) {
            stb.append("    INNER JOIN COURSE_GROUP_CD_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("        AND T5.GRADE = T1.GRADE ");
            stb.append("        AND T5.COURSECD = T1.COURSECD ");
            stb.append("        AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("        AND T5.COURSECD = T1.COURSECD ");
            stb.append("    INNER JOIN COURSE_GROUP_CD_HDAT T6 ON T6.YEAR = T5.YEAR ");
            stb.append("        AND T6.GRADE = T5.GRADE ");
            stb.append("        AND T6.GROUP_CD = T5.GROUP_CD ");
        } else {
            stb.append("    INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
            stb.append("    INNER JOIN MAJOR_MST T3 ON T3.MAJORCD = T1.MAJORCD ");
            stb.append("        AND T3.COURSECD = T1.COURSECD ");
        }
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + param._year + "' ");
        stb.append("    AND T1.GRADE = '" + param._grade + "' ");
        stb.append("GROUP BY ");
        if ("1".equals(param._gaihyouGakkaBetu)) {
            stb.append("    T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T2.COURSENAME, T3.MAJORNAME, T4.COURSECODENAME ");
        } else if ("2".equals(param._gaihyouGakkaBetu)) {
            stb.append("    T5.GROUP_CD, T6.GROUP_NAME ");
        } else {
            stb.append("    T1.COURSECD, T1.MAJORCD, T2.COURSENAME, T3.MAJORNAME ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    T1.COURSECODE, T2.GROUP_NAME, ");
        stb.append("    SUM(T1.A_MEMBER) AS A_SUM, ");
        stb.append("    SUM(T1.B_MEMBER) AS B_SUM, ");
        stb.append("    SUM(T1.C_MEMBER) AS C_SUM, ");
        stb.append("    SUM(T1.D_MEMBER) AS D_SUM, ");
        stb.append("    SUM(T1.E_MEMBER) AS E_SUM, ");
        stb.append("    SUM(T1.A_MEMBER)+SUM(T1.B_MEMBER)+SUM(T1.C_MEMBER)+SUM(T1.D_MEMBER)+SUM(T1.E_MEMBER) AS ALL_SUM ");
        stb.append("FROM ");
        stb.append("    GENEVIEWMBR_DAT T1 ");
        stb.append("    INNER JOIN COURSET T2 ON ");
        stb.append("        T1.COURSECD = T2.COURSECD ");
        stb.append("        AND T1.MAJORCD = T2.MAJORCD ");
        stb.append("        AND T1.COURSECODE = T2.COURSECODE ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + param._year + "' ");
        stb.append("    AND T1.GRADE = '" + param._grade + "' ");
        stb.append("GROUP BY ");
        stb.append("    T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T2.GROUP_NAME ");
        return stb.toString();

    }//preStat()の括り
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        //  パラメータの取得
        Param param = new Param(db2, request);
        log.debug(" $Revision: 69701 $ $Date: 2019-09-13 19:43:27 +0900 (金, 13 9 2019) $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Param {
        final String _year;
        final String _gakki;
        final String _grade;
        final String _date;
        final String _gaihyouGakkaBetu;
        final String _printGrade;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");            //年度
            _gakki = request.getParameter("GAKKI");           //学期
            _grade = request.getParameter("GRADE");           //学年
            final String gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _printGrade = NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : StringUtils.defaultString(gradeCd, " ");

            //  作成日(現在処理日)の取得
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = getinfo.Control(db2);
            _date = KNJ_EditDate.h_format_thi(returnval.val3,0);
            
            _gaihyouGakkaBetu = request.getParameter("gaihyouGakkaBetu");
        }
    }
}//クラスの括り

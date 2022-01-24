package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１６＞  重複出願者リスト
 *
 *  2012/12/17 nakamoto 作成日
 **/

public class KNJL316 {

    private static final Log log = LogFactory.getLog(KNJL316.class);

    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[2];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APDIV");                       //入試制度
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        int ret = svf.VrInit();
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement psTestDiv = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));
            psTestDiv = db2.prepareStatement(preTestDivMst(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if( setSvfout(db2,svf,param,ps1,psTestDiv) ){
            nonedata = true;
        }

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps1,psTestDiv);
        db2.commit();
        db2.close();
        outstrm.close();

    }//doGetの括り



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
        ret = svf.VrSetForm("KNJL316.frm", 4);
        ret = svf.VrsOut("NENDO"    ,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("DATE" ,KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }



    /**帳票出力**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1,
        PreparedStatement psTestDiv
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rsTestDiv = null;
        try {
            ResultSet rs = ps1.executeQuery();

            int reccnt = 0;             //合計レコード数
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）

            while( rs.next() ){
                //レコードを出力
                if (reccnt > 0) ret = svf.VrEndRecord();
                //５０行超えたとき、ページ数カウント
                if (gyo > 50) {
                    gyo = 1;
                    pagecnt++;
                }
                //ヘッダ
                ret = svf.VrsOut("PAGE"     ,String.valueOf(pagecnt));      //現在ページ数
                //明細
                ret = svf.VrsOut("EXAMNO"   ,rs.getString("EXAMNO"));       //受験番号
                ret = svf.VrsOut("RECOM_EXAMNO1"   ,rs.getString("RECOM_EXAMNO1"));       //重複受験番号
                ret = svf.VrsOut("RECOM_EXAMNO2"   ,rs.getString("RECOM_EXAMNO2"));       //重複受験番号
                ret = svf.VrsOut("RECOM_EXAMNO3"   ,rs.getString("RECOM_EXAMNO3"));       //重複受験番号
                ret = svf.VrsOut("NAME"     ,rs.getString("NAME"));         //名前
                ret = svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));      //出身校
                //合否
                int num = 0;
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    num++;
                    String testdiv = rsTestDiv.getString("TESTDIV"); 

                    ret = svf.VrsOut("JUDGE" + num, getMark(rs.getString("JUDGEDIV" + testdiv), rs.getString("JUDGECLASS" + testdiv), rs.getString("HONORDIV" + testdiv), rs.getString("EXAMINEE_DIV" + testdiv)));
                }
                rsTestDiv.close();
                //レコード数カウント
                reccnt++;
                //現在ページ数判断用
                gyo++;

                nonedata = true;
            }
            //最終レコードを出力
            if (nonedata) {
                ret = svf.VrEndRecord();//レコードを出力
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }

    private String getMark(String judgediv, String judgeclass, String honordiv, String examineeDiv) {
        String mark="";
        if ("1".equals(judgediv) && "1".equals(honordiv)) mark= "☆";
        else if ("1".equals(judgediv) && "3".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "4".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "2".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "1".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "6".equals(judgeclass)) mark= "○";
        else if ("2".equals(judgediv)) mark= "×";
        else if ("2".equals(examineeDiv)) mark= "▲";
        return mark;
    }

    /**一覧を取得**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECOM_EXAMNO1, ");
            stb.append("     T1.RECOM_EXAMNO2, ");
            stb.append("     T1.RECOM_EXAMNO3, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T1.SEX, ");
            stb.append("     R0.JUDGEDIV AS JUDGEDIV0, ");
            stb.append("     R1.JUDGEDIV AS JUDGEDIV1, ");
            stb.append("     R2.JUDGEDIV AS JUDGEDIV2, ");
            stb.append("     R3.JUDGEDIV AS JUDGEDIV3, ");
            stb.append("     R4.JUDGEDIV AS JUDGEDIV4, ");
            stb.append("     R5.JUDGEDIV AS JUDGEDIV5, ");
            stb.append("     R6.JUDGEDIV AS JUDGEDIV6, ");
            stb.append("     R0.JUDGECLASS AS JUDGECLASS0, ");
            stb.append("     R1.JUDGECLASS AS JUDGECLASS1, ");
            stb.append("     R2.JUDGECLASS AS JUDGECLASS2, ");
            stb.append("     R3.JUDGECLASS AS JUDGECLASS3, ");
            stb.append("     R4.JUDGECLASS AS JUDGECLASS4, ");
            stb.append("     R5.JUDGECLASS AS JUDGECLASS5, ");
            stb.append("     R6.JUDGECLASS AS JUDGECLASS6, ");
            stb.append("     R0.HONORDIV AS HONORDIV0, ");
            stb.append("     R1.HONORDIV AS HONORDIV1, ");
            stb.append("     R2.HONORDIV AS HONORDIV2, ");
            stb.append("     R3.HONORDIV AS HONORDIV3, ");
            stb.append("     R4.HONORDIV AS HONORDIV4, ");
            stb.append("     R5.HONORDIV AS HONORDIV5, ");
            stb.append("     R6.HONORDIV AS HONORDIV6, ");
            stb.append("     D0.EXAMINEE_DIV AS EXAMINEE_DIV0, ");
            stb.append("     D1.EXAMINEE_DIV AS EXAMINEE_DIV1, ");
            stb.append("     D2.EXAMINEE_DIV AS EXAMINEE_DIV2, ");
            stb.append("     D3.EXAMINEE_DIV AS EXAMINEE_DIV3, ");
            stb.append("     D4.EXAMINEE_DIV AS EXAMINEE_DIV4, ");
            stb.append("     D5.EXAMINEE_DIV AS EXAMINEE_DIV5, ");
            stb.append("     D6.EXAMINEE_DIV AS EXAMINEE_DIV6 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R0 ON R0.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R0.EXAMNO = T1.EXAMNO  AND R0.TESTDIV = '0' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R1.EXAMNO = T1.EXAMNO  AND R1.TESTDIV = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R2 ON R2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R2.EXAMNO = T1.EXAMNO  AND R2.TESTDIV = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R3 ON R3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R3.EXAMNO = T1.EXAMNO  AND R3.TESTDIV = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R4 ON R4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R4.EXAMNO = T1.EXAMNO  AND R4.TESTDIV = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R5 ON R5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R5.EXAMNO = T1.EXAMNO  AND R5.TESTDIV = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R6 ON R6.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R6.EXAMNO = T1.EXAMNO  AND R6.TESTDIV = '6' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D0 ON D0.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D0.EXAMNO = T1.EXAMNO  AND D0.TESTDIV = '0' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D1 ON D1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D1.EXAMNO = T1.EXAMNO  AND D1.TESTDIV = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D2 ON D2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D2.EXAMNO = T1.EXAMNO  AND D2.TESTDIV = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D3 ON D3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D3.EXAMNO = T1.EXAMNO  AND D3.TESTDIV = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D4 ON D4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D4.EXAMNO = T1.EXAMNO  AND D4.TESTDIV = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D5 ON D5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D5.EXAMNO = T1.EXAMNO  AND D5.TESTDIV = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT D6 ON D6.ENTEXAMYEAR = T1.ENTEXAMYEAR AND D6.EXAMNO = T1.EXAMNO  AND D6.TESTDIV = '6' ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("     AND T1.APPLICANTDIV = '"+param[1]+"' ");
            stb.append("     AND ((T1.NAME, T1.NAME_KANA, T1.FS_NAME) IN ( ");
            stb.append("         SELECT ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.FS_NAME ");
            stb.append("         FROM ");
            stb.append("             ENTEXAM_APPLICANTBASE_DAT T2 ");
            stb.append("         WHERE ");
            stb.append("             T2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("             AND T2.APPLICANTDIV = '"+param[1]+"' ");
            stb.append("         GROUP BY ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.FS_NAME ");
            stb.append("         HAVING ");
            stb.append("             1 < COUNT(*) ");
            stb.append("     ) ");
            stb.append("     OR (T1.NAME, T1.NAME_KANA, T1.SEX) IN ( ");
            stb.append("         SELECT ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.SEX ");
            stb.append("         FROM ");
            stb.append("             ENTEXAM_APPLICANTBASE_DAT T2 ");
            stb.append("         WHERE ");
            stb.append("             T2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("             AND T2.APPLICANTDIV = '"+param[1]+"' ");
            stb.append("         GROUP BY ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.SEX ");
            stb.append("         HAVING ");
            stb.append("             1 < COUNT(*) ");
            stb.append("     ) ");
            stb.append("     OR (T1.NAME, T1.FS_NAME, T1.SEX) IN ( ");
            stb.append("         SELECT ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.FS_NAME, ");
            stb.append("             T2.SEX ");
            stb.append("         FROM ");
            stb.append("             ENTEXAM_APPLICANTBASE_DAT T2 ");
            stb.append("         WHERE ");
            stb.append("             T2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("             AND T2.APPLICANTDIV = '"+param[1]+"' ");
            stb.append("         GROUP BY ");
            stb.append("             T2.NAME, ");
            stb.append("             T2.FS_NAME, ");
            stb.append("             T2.SEX ");
            stb.append("         HAVING ");
            stb.append("             1 < COUNT(*) ");
            stb.append("     ) ");
            stb.append("     OR (T1.NAME_KANA, T1.FS_NAME, T1.SEX) IN ( ");
            stb.append("         SELECT ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.FS_NAME, ");
            stb.append("             T2.SEX ");
            stb.append("         FROM ");
            stb.append("             ENTEXAM_APPLICANTBASE_DAT T2 ");
            stb.append("         WHERE ");
            stb.append("             T2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("             AND T2.APPLICANTDIV = '"+param[1]+"' ");
            stb.append("         GROUP BY ");
            stb.append("             T2.NAME_KANA, ");
            stb.append("             T2.FS_NAME, ");
            stb.append("             T2.SEX ");
            stb.append("         HAVING ");
            stb.append("             1 < COUNT(*) ");
            stb.append("     )) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    private String preTestDivMst(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' ORDER BY SHOWORDER, TESTDIV ");
        } catch( Exception e ){
            log.error("preStat1 error!", e);
        }
        return stb.toString();
    }

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement psTestDiv
    ) {
        try {
            ps1.close();
            psTestDiv.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り
    
}//クラスの括り

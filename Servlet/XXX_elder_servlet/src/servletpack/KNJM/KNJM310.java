// kanji=漢字
/*
 * $Id: f349cf0791e3c42dc87227f2b78f2c3a00c08643 $
 *
 * 作成日: 2005/08/18 17:20:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * ＜ＫＮＪＭ３１０＞  科目別レポート提出状況一覧
 * @author m-yama
 * @version $Id: f349cf0791e3c42dc87227f2b78f2c3a00c08643 $
 */
public class KNJM310 extends HttpServlet {

    private static final int _TEST_CNT_MAX = 12;
    private static final Log log = LogFactory.getLog(KNJM310.class);
    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        PreparedStatement ps1 = null;

        try {
            String param[]  = new String[8];        //NO001
            //  パラメータの取得
            param[0] = request.getParameter("YEAR"); //年度
            param[1] = request.getParameter("SEMESTER"); //学期
            param[3] = request.getParameter("PRINT_DIV"); //1の場合は受付済み件数、2の場合は評価済み件数
            param[4] = param[3].equals("2") ? "評価" : "提出";
            param[6] = request.getParameter("useCurriculumcd");             //教育課程
            param[7] = request.getParameter("useRepStandarddateCourseDat"); //「REP_STANDARDDATE_COURSE_DAT」テーブル使用

            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //  ＳＶＦ作成処理
            Set_Head(db2, svf, param); //見出し出力のメソッド
            for (int ia = 0; ia < param.length; ia++)
                log.debug("[KNJM310]param[" + ia + "]=" + param[ia]);
            //SQL作成
            ps1 = db2.prepareStatement(Pre_Stat1(param)); //設定データpreparestatement

            log.debug("kaisii");
            //SVF出力
            Set_Detail_1(db2, svf, param, ps1);
            log.debug("syuuryou");

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            //  終了処理
            svf.VrQuit();
            DbUtils.closeQuietly(ps1);
            db2.commit();
            db2.close();                //DBを閉じる
        }

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(final DB2UDB db2, final Vrw32alp svf, final String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJM310.frm", 1);
        //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[2] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Head()の括り

    /**SVF-FORM**/
    private boolean Set_Detail_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String param[],
            final PreparedStatement ps1
    ) {
        try {
            int gyo   = 1;          //行数カウント用
            int[] rep = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            int jkei  = 0;
            int tkei  = 0;
            int ykei  = 0;

            ResultSet rs = ps1.executeQuery();
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();
                }
                //ヘッダ出力
                svf.VrsOut("NENDO"      , String.valueOf(param[0]) + "年度" );
                svf.VrsOut("TITLE", param[4]);
                svf.VrsOut("DATE"       , String.valueOf(param[2]) );
                //明細出力
                svf.VrsOutn("SUBCLASS"  ,gyo, rs.getString("SUBCLASSCD"));
                svf.VrsOutn("SUBCLASSNAME",gyo, rs.getString("SUBCLASSABBV"));
                svf.VrsOutn("PERSONS"       ,gyo, rs.getString("JUKOU"));
                jkei = jkei + rs.getInt("JUKOU");
                for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                    if (rs.getInt("REP_SEQ_ALL") >= i){
                        svf.VrsOutn("REPORT" + i, gyo, rs.getString("REPDATCNT" + i));
                        rep[i - 1] = rep[i - 1] + rs.getInt("REPDATCNT" + i);
                    }
                }
                svf.VrsOutn("TOTAL"     ,gyo, rs.getString("ALLCNT"));
                tkei = tkei + rs.getInt("ALLCNT");
                svf.VrsOutn("EXPECTED"  ,gyo, rs.getString("YOTEI"));
                ykei = ykei + rs.getInt("YOTEI");
                svf.VrsOutn("PERCENTAGE"    ,gyo, String.valueOf(rs.getFloat("TEIRITU")));
                svf.VrsOutn("1STPER"        ,gyo, String.valueOf(rs.getFloat("TEIRITU1")));

                nonedata = true;
                gyo++;          //行数カウント用
            }
            if (nonedata){
                if (gyo > 50){
                    svf.VrEndPage();
                }
                svf.VrsOutn("SUBCLASSNAME",50, "合計");
                svf.VrsOutn("PERSONS"    ,50, String.valueOf(jkei));
                for (int i = 0; i < rep.length; i++) {
                    svf.VrsOutn("REPORT" + (i + 1), 50, String.valueOf(rep[i]));
                }
                svf.VrsOutn("TOTAL"      ,50, String.valueOf(tkei));
                svf.VrsOutn("EXPECTED"   ,50, String.valueOf(ykei));

                svf.VrsOutn("PERCENTAGE" ,50, String.valueOf( (float)Math.round( ((float)tkei / (float)ykei ) * 1000 )/10 ) );
                svf.VrsOutn("1STPER"     ,50, String.valueOf( (float)Math.round( ((float)rep[0] / (float)jkei ) * 1000 )/10 ) );
                svf.VrEndPage();
            }
            rs.close();

        } catch( Exception ex ) {
            log.error("Set_Detail_1 read error!" + ex);
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(final String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT ");
            if ("1".equals(param[6])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD, ");
            stb.append("     STANDARD_SEQ, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     REP_PRESENT_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param[0] + "' ");
            stb.append("     AND GRAD_VALUE NOT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1='M011') ");
            if (param[3].equals("2")) {
                stb.append("     AND (GRAD_VALUE IN ('2','3','4','5') ");
                stb.append("      OR  GRAD_VALUE IN ('6','7') AND SUBCLASSCD LIKE '9%') ");
            } else {
                stb.append(" GROUP BY ");
                if ("1".equals(param[6])) {
                    stb.append("       CLASSCD, ");
                    stb.append("       SCHOOL_KIND, ");
                    stb.append("       CURRICULUM_CD, ");
                }
                stb.append("     SUBCLASSCD, ");
                stb.append("     STANDARD_SEQ, ");
                stb.append("     SCHREGNO ");
            }
            stb.append(" ),CHAIRCNT AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD,SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+param[0]+"' ");
            stb.append("     AND SEMESTER = '"+param[1]+"' ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD,SCHREGNO ");

            stb.append(" ),JUKOU AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD,COUNT(CHAIRCD) AS JUKOU ");
            stb.append(" FROM ");
            stb.append("     CHAIRCNT ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD ");

            stb.append(" ),CCORSE AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD,MAX(REP_SEQ_ALL) AS REP_SEQ_ALL ");
            stb.append(" FROM ");
            stb.append("     CHAIR_CORRES_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+param[0]+"' ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD ");

            for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                stb.append(" ),REPDAT" + i + " AS ( ");
                stb.append(" SELECT ");
                if ("1".equals(param[6])) {
                    stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
                } else {
                    stb.append("     SUBCLASSCD ");
                }
                stb.append("     AS SUBCLASSCD_" + i + ", ");
                stb.append("     COUNT(SUBCLASSCD) AS REPDATCNT" + i + " ");
                stb.append(" FROM ");
                stb.append("     MAIN ");
                stb.append(" WHERE ");
                stb.append("     STANDARD_SEQ = " + i + " ");
                stb.append(" GROUP BY ");
                if ("1".equals(param[6])) {
                    stb.append("       CLASSCD, ");
                    stb.append("       SCHOOL_KIND, ");
                    stb.append("       CURRICULUM_CD, ");
                }
                stb.append("     SUBCLASSCD ");
            }
            stb.append(" ) ");

            stb.append(" SELECT ");
            if ("1".equals(param[6])) {
                stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     t1.SUBCLASSCD, ");
            }
            stb.append("     t2.SUBCLASSABBV,t3.JUKOU,t4.REP_SEQ_ALL, ");

            for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                stb.append("     CASE WHEN REPDATCNT" + i + " IS NOT NULL ");
                stb.append("          THEN REPDATCNT" + i + " ");
                stb.append("          ELSE 0 ");
                stb.append("     END AS REPDATCNT" + i + ", ");
            }

            String sep1 = "";
            for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                stb.append(sep1 + "     VALUE(REPDATCNT" + i + ",0) ");
                sep1 = "+";
            }
            stb.append(" AS ALLCNT, ");

            stb.append("     t3.JUKOU * t4.REP_SEQ_ALL AS YOTEI, ");
            //受講数x提出回数が０人の場合は０％固定
            stb.append("     CASE WHEN FLOAT(t3.JUKOU * t4.REP_SEQ_ALL) > 0 THEN ");
            stb.append("     ROUND(FLOAT((VALUE(REPDATCNT1,0) + VALUE(REPDATCNT2,0) + VALUE(REPDATCNT3,0) + VALUE(REPDATCNT4,0) ");
            stb.append("     + VALUE(REPDATCNT5,0) + VALUE(REPDATCNT6,0) + VALUE(REPDATCNT7,0) + VALUE(REPDATCNT8,0) ");
            stb.append("     + VALUE(REPDATCNT9,0) + VALUE(REPDATCNT10,0) + VALUE(REPDATCNT11,0) + VALUE(REPDATCNT12,0)) ");
            stb.append("     / FLOAT(t3.JUKOU * t4.REP_SEQ_ALL) * 100),1) ");
            stb.append("     ELSE 0 END AS TEIRITU, ");
            //受講数が０人の場合は０％固定
            stb.append("     CASE WHEN FLOAT(t3.JUKOU) > 0 THEN ");
            stb.append("     ROUND((FLOAT(VALUE(REPDATCNT1,0)) / FLOAT(t3.JUKOU) *100),1) ");
            stb.append("     ELSE 0 END AS TEIRITU1 ");

            stb.append(" FROM ");
            if ("1".equals(param[7])) {
                stb.append("     REP_STANDARDDATE_COURSE_DAT t1 ");
            } else {
                stb.append("     REP_STANDARDDATE_DAT t1 ");
            }

            stb.append("     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
            if ("1".equals(param[6])) {
                stb.append("       AND t1.CLASSCD = t2.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN JUKOU t3 ON t1.CHAIRCD = t3.CHAIRCD ");
            stb.append("     LEFT JOIN CCORSE t4 ON t1.CHAIRCD = t4.CHAIRCD ");
            for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                if ("1".equals(param[6])) {
                    stb.append("     LEFT JOIN REPDAT" + i + " ON t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = SUBCLASSCD_" + i + " ");
                } else {
                    stb.append("     LEFT JOIN REPDAT" + i + " ON t1.SUBCLASSCD = SUBCLASSCD_" + i + " ");
                }
            }
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '"+param[0]+"' ");
            stb.append(" GROUP BY ");
            if ("1".equals(param[6])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("     t1.SUBCLASSCD,t2.SUBCLASSABBV,t3.JUKOU,t4.REP_SEQ_ALL, ");
            String sep = "";
            for (int i = 1; i <= _TEST_CNT_MAX; i++) {
                stb.append(sep + "     REPDATCNT" + i);
                sep = ",";
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param[6])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("     t1.SUBCLASSCD ");

log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

}//クラスの括り

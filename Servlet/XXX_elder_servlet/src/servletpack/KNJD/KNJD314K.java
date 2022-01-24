package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３１４＞  成績概況表
 *
 *	2004/09/28 nakamoto 作成日
 *	2004/10/28 nakamoto 事故の抽出条件を変更
 *	2004/12/08 nakamoto SQLの不具合修正
 *	2004/12/15 nakamoto 受験人数と平均人数が合っていない不具合の修正
 *	                    欠点科目は、評定が１の生徒数に修正
 *	                    画面パラメータ'学年末'及び'学年'を追加に伴う対応
 *	2004/12/16 nakamoto 休学・留学の抽出条件を修正
 *	2004/12/17 nakamoto 事故の抽出条件を変更（学期成績が１つでもnullの生徒をカウント）
 *	                    事故数---休学・留学・退学者は除いて、１科目でも成績がnullの生徒
 *	                    受験数---休学・留学・退学者は除いて、１科目でも成績があるの生徒
 *	2005/02/15 nakamoto db2.commit NO001
 *	2005/02/16 nakamoto 異動対象日付を追加 NO002
 *	                    ３学年の学年末での欠点科目は、選択履修科目はカウントしない NO003
 *	                    Pre_Stat4のSQL大幅修正 NO004
 *	2005/02/17 nakamoto Pre_Stat1,2,3のSQL大幅修正 NO004
 *	2005/03/08 nakamoto 欠点のカウントを「国語総合」で行うべきところを
 *	                    「現文」「古典」それぞれでカウントしていたのを修正。---NO005
 *	2005/03/10 nakamoto ３学期からの留学生は成績データを出力する。(学期末の場合のみ)
 *	                    (３学期開始日付<=異動日<=３学期終了日付)
 *	                    総合学習(900100)は、この帳票では、対象外とする。
 *	                    評価科目(国語総合)は、最高点・最低点・平均点には含めない。---NO006
 *	2005/03/11 nakamoto クラス平均：平均(平均)の計算方法を修正。---NO007
 *--------------------------------------------------------------------------------------------------
 *	2005/06/02 nakamoto 中間・期末を追加---NO008
 *	2005/06/03 nakamoto 異動対象日付(前年度用)を追加---NO009
 *	2005/06/06 nakamoto NO005は、学期末・学年末のみ適用---NO010
 **/

public class KNJD314K {

    private static final Log log = LogFactory.getLog(KNJD314K.class);

    private boolean hasdata = false; 								//該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス

        //	print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //	svf設定
        svf.VrInit();						   	//クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        Param param = createParam(request, db2);

        //	ＳＶＦ作成処理
        printMain(svf, db2, param);

        //	該当データ無し
        if (!hasdata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        // 	終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(param._ps1);
        DbUtils.closeQuietly(param._ps2);
        DbUtils.closeQuietly(param._ps3);
        DbUtils.closeQuietly(param._ps4);
        db2.commit();
        db2.close();				//DBを閉じる
        outstrm.close();			//ストリームを閉じる
    }//doGetの括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $ ");
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private void printMain(Vrw32alp svf, DB2UDB db2, Param param) {

        svf.VrSetForm("KNJD314_1.frm", 1);		//フォーム１様式指定
        svf.VrsOut("NENDO",	param._nendo);

        //	ＳＶＦ属性変更--->改ページ
        //svf.VrAttribute("GRADE","FF=1");
        svf.VrsOut("DATE",  param._ctrlDate);

        svf.VrsOut("SEMESTER",	param._title);

        //SVF出力
        String year[] = { param._year , param._yearPre };   			//本年度・前年度
        String idodate[] = { param._date , param._datePre };   			//異動対象日付(本年度・前年度)//---NO009
        for (int ia=0 ; ia<year.length ; ia++) {
            if (printForm1(db2,svf,param,year[ia],idodate[ia], String.valueOf(ia + 1))) {
                hasdata = true;	//フォーム１出力のメソッド
            }
        }

        if (hasdata) {
            svf.VrEndPage();							//フォーム１を出力

            svf.VrSetForm("KNJD314_2.frm", 4);		//フォーム２様式指定
            svf.VrAttribute("GRADE","FF=1");			//ＳＶＦ属性変更--->改ページ

            svf.VrsOut("NENDO",	param._nendo);			//年度
            svf.VrsOut("DATE",	param._ctrlDate);			//作成日
            svf.VrsOut("SEMESTER",	param._title);		//学期名称

            printForm2(db2,svf,param);				//フォーム２出力のメソッド
        }
    }

    /**SVF-FORM**/
    private boolean printForm1(DB2UDB db2,Vrw32alp svf,Param param,String year,String idodate, final String nen)
    {
        boolean hasdata = false;
        try {
            if (null == param._ps1) {
                param._ps1 = db2.prepareStatement(Pre_Stat(param, 1));		//フォーム１・成績preparestatement
            }
            int pp = 0;
            param._ps1.setString(++pp,year);	//本年度・前年度
            param._ps1.setString(++pp,idodate);	//異動対象日付(本年度・前年度)

            log.debug("ps1 start!");
            ResultSet rs = param._ps1.executeQuery();

            while( rs.next() ){
                Set_Data1(svf,rs,1,nen);			//フォーム１のメソッド
                hasdata = true;
            }
            DbUtils.closeQuietly(rs);
            db2.commit();//---NO001

            if (hasdata) {
                // 合計列出力
                try {
                    if (null == param._ps2) {
                        param._ps2 = db2.prepareStatement(Pre_Stat(param, 2));		//フォーム１・合計preparestatement
                    }
                    int pp2 = 0;
                    param._ps2.setString(++pp2,year);	//本年度・前年度
                    param._ps2.setString(++pp2,idodate);	//異動対象日付(本年度・前年度)

                    log.debug("ps2 start!");
                    ResultSet rs2 = param._ps2.executeQuery();

                    while( rs2.next() ){
                        Set_Data1(svf,rs2,2,nen);			//フォーム１のメソッド
                        if (Integer.parseInt(rs2.getString("JUKEN")) > 0) {

                            /** 合計列（割合）出力 **/
                            try {
                                if (null == param._ps3) {
                                    param._ps3 = db2.prepareStatement(Pre_Stat(param, 3));		//フォーム１・割合（％）preparestatement
                                }

                                int pp3 = 0;
                                param._ps3.setString(++pp3,year);	//本年度・前年度
                                param._ps3.setString(++pp3,idodate);	//異動対象日付(本年度・前年度)

                                log.debug("ps3 start!");
                                ResultSet rs3 = param._ps3.executeQuery();

                                while( rs3.next() ){
                                    Set_Data1(svf,rs3,3,nen);			//フォーム１のメソッド
                                }
                                DbUtils.closeQuietly(rs3);
                                db2.commit();
                            } catch( SQLException ex ){
                                log.warn("Set_Detail_1_2 read error!", ex);
                            }
                        }
                    }
                    DbUtils.closeQuietly(rs2);
                    db2.commit();//---NO001
                } catch( SQLException ex ){
                    log.warn("Set_Detail_1_1 read error!", ex);
                }
            }
        } catch( Exception ex ) {
            log.warn("Set_Detail_1 read error!", ex);
        }
        return hasdata;

    }//Set_Detail_1()の括り

    /** フォーム２出力 **/
    private void printForm2(DB2UDB db2,Vrw32alp svf,Param param)
    {
        try {
            if (null == param._ps4) {
                param._ps4 = db2.prepareStatement(sqlForm2(param));		//フォーム２・成績preparestatement
            }
            log.debug("ps4 start!");
            ResultSet rs = param._ps4.executeQuery();
            log.debug("ps4 end!");

            while( rs.next() ){
                //学年--->改ページ
                String gr_cl = rs.getString("GR_CL");
                int grade = Integer.parseInt(gr_cl.substring(0,2));
                svf.VrsOut("GRADE"		,"第"+Integer.toString(grade)+"学年");
                //データセット
                int jiko = rs.getInt("JIKO");
                if( jiko < 0 ) jiko = 0;
                svf.VrsOut("ACCIDENT"		,Integer.toString(jiko));		//事故

                svf.VrsOut("HR_CLASS"		,rs.getString("HR_NAMEABBV"));	//学級
                svf.VrsOut("PEOPLE"		,rs.getString("ZAI"));			//在籍
                svf.VrsOut("ABSENT"		,rs.getString("KYU"));			//休学
                svf.VrsOut("ABROAD"		,rs.getString("RYU"));			//留学
                svf.VrsOut("MAXIMUM"		,rs.getString("MAXAVG"));		//最高点
                svf.VrsOut("AVERAGE"		,rs.getString("AVGAVG"));		//平均点
                svf.VrsOut("UNDER"		,rs.getString("SUB_UDR"));		//欠点科目（４科目未満）
                svf.VrsOut("OVER"			,rs.getString("SUB_OVR"));		//欠点科目（４科目以上）
                //データ出力（１レコード）
                svf.VrEndRecord();
                //データクリア
                svf.VrsOut("HR_CLASS"		,"");
                svf.VrsOut("PEOPLE"		,"");
                svf.VrsOut("ABSENT"		,"");
                svf.VrsOut("ABROAD"		,"");
                svf.VrsOut("ACCIDENT"		,"");
                svf.VrsOut("MAXIMUM"		,"");
                svf.VrsOut("AVERAGE"		,"");
                svf.VrsOut("UNDER"		,"");
                svf.VrsOut("OVER"			,"");
            }
            rs.close();
            db2.commit();//---NO001
        } catch( SQLException ex ){
            log.warn("Set_Detail_2 read error!", ex);
        }

    }//Set_Detail_2()の括り



    /** フォーム１出力 **/
    private void Set_Data1(Vrw32alp svf,ResultSet rs,int flg,String nen)
    {
        try {
            String gra = "";	//学年No(1〜3)
            String total = "";	//合計・割合
            //成績
            if( flg==1 ){
                gra = "_" + rs.getString("GR_CL");
                total = "";
            }
            //合計
            if( flg==2 ){
                gra = "";
                total = "TOTAL_";
            }
            //割合
            if( flg==3 ){
                gra = "";
                total = "RATIO_";
            }

            if( flg==1 || flg==2 ){
                svf.VrsOut(total+"PEOPLE"+nen+gra		,rs.getString("ZAI"));			//在籍
                svf.VrsOut(total+"ABSENT"+nen+gra		,rs.getString("KYU"));			//休学
                svf.VrsOut(total+"ABROAD"+nen+gra		,rs.getString("RYU"));			//留学
                svf.VrsOut(total+"ACCIDENT"+nen+gra		,rs.getString("JIKO"));			//事故
                svf.VrsOut(total+"EXAMINATION"+nen+gra	,rs.getString("JUKEN"));		//受験

                svf.VrsOut(total+"MAXIMUM"+nen+gra		,rs.getString("MAXAVG"));		//最高点
                svf.VrsOut(total+"MINIMUM"+nen+gra		,rs.getString("MINAVG"));		//最低点
                svf.VrsOut(total+"AVERAGE"+nen+gra		,rs.getString("AVGAVG"));		//平均点
            }

            for (int ia=1; ia<11; ia++){
                final String avg = Integer.toString(ia);	//平均点No(1〜10)
                svf.VrsOut(total+"AVE"+avg+"_"+nen+gra  ,rs.getString("AVG"+avg));      //平均点（統計）
            }

            svf.VrsOut(total+"UNDER"+nen+gra        ,rs.getString("SUB_UDR"));      //欠点科目（４科目未満）
            svf.VrsOut(total+"OVER"+nen+gra         ,rs.getString("SUB_OVR"));      //欠点科目（４科目以上）

        } catch( SQLException ex ){
            log.warn("Set_Data1 read error!", ex);
        }

    }//Set_Data1()の括り


    /**成績**/
    private String Pre_Stat(Param param, final int sqldiv) {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH PARAM (YEAR, IDOU_DATE) AS(  ");
        stb.append("  VALUES(CAST(? AS VARCHAR(4)), CAST(? AS DATE))  ");
        stb.append("), SCHNO AS(  ");
        stb.append("   SELECT GRADE,HR_CLASS,SCHREGNO  ");
        stb.append("   FROM   SCHREG_REGD_DAT  ");
        stb.append("   WHERE  YEAR = (SELECT YEAR FROM PARAM) AND SEMESTER='"+param._loginGakki+"' AND GRADE IN "+param._sqlGrade+" ) ");
        //学年
        stb.append(",HR_NAME AS(  ");
        if (sqldiv == 1) {
            stb.append("   SELECT DISTINCT GRADE  ");
        } else if (sqldiv == 2) {
            stb.append("   SELECT 'CMN_KEY' AS CMN_KEY, MAX(GRADE) GRADE  ");
        } else {
            stb.append("   SELECT 'CMN_KEY' AS CMN_KEY, MAX(GRADE) GRADE  ");
        }
        stb.append("   FROM   SCHREG_REGD_HDAT  ");
        stb.append("   WHERE  YEAR = (SELECT YEAR FROM PARAM) AND SEMESTER='"+param._loginGakki+"' AND GRADE IN "+param._sqlGrade+" ) ");
        stb.append(",SEM3_DATE AS ( ");
        stb.append("    SELECT SDATE,EDATE ");
        stb.append("    FROM   SEMESTER_MST ");
        stb.append("    WHERE  YEAR = (SELECT YEAR FROM PARAM) AND SEMESTER='3' ) ");
        stb.append(",SEM3_RYU AS ( ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("    FROM   SCHREG_TRANSFER_DAT K1,SEM3_DATE K2");
        stb.append("    WHERE  TRANSFERCD='1' AND ");
        stb.append("           TRANSFER_SDATE BETWEEN SDATE AND EDATE ) ");
        //基礎
        stb.append(",IDOU1 AS(  ");
        stb.append("    SELECT W1.SCHREGNO  ");
        stb.append("    FROM   SCHNO W1,SCHREG_BASE_MST W2  ");
        stb.append("    WHERE  W2.SCHREGNO=W1.SCHREGNO AND  ");
        stb.append("           W2.GRD_DIV IN ('2','3') AND W2.GRD_DATE < (SELECT IDOU_DATE FROM PARAM) ) ");
        //異動
        stb.append(",IDOU2 AS(  ");
        stb.append("   SELECT w3.schregno, TRANSFERCD  ");
        stb.append("   FROM   SCHNO w1, schreg_transfer_dat w3  ");
        stb.append("   WHERE  w3.schregno=w1.schregno and w3.transfercd in ('1','2') and  ");
        if(param._gakki.equals("9")) {
            stb.append("      W3.SCHREGNO NOT IN (SELECT SCHREGNO FROM SEM3_RYU) AND ");
        }
        stb.append("		  (SELECT IDOU_DATE FROM PARAM) BETWEEN W3.TRANSFER_SDATE AND W3.TRANSFER_EDATE ) ");
        //公欠・欠席
        stb.append(",KEKKA2 AS( ");
        stb.append("    SELECT SCHREGNO, ");
        stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        stb.append("           SUBCLASSCD  ");
        stb.append("    FROM   KIN_RECORD_DAT ");
        stb.append("    WHERE  year = (SELECT YEAR FROM PARAM) AND "+param._sqlScoreState+" ) ");
        //成績
        stb.append(",KIN_REC AS (  ");
        stb.append("    SELECT ");
        stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        stb.append("           SUBCLASSCD, SCHREGNO, " + param._scoreField + " AS SCORE ");
        stb.append("    FROM   KIN_RECORD_DAT  ");
        stb.append("    WHERE  YEAR = (SELECT YEAR FROM PARAM) AND " + param._scoreField + " IS NOT NULL ");
        stb.append("           AND SUBCLASSCD NOT IN ('900100', '900200') ");
        stb.append(        " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = (SELECT YEAR FROM PARAM) AND NAMECD1 = 'D065') ");
        stb.append(") ,KIN_SCH AS (   ");
        stb.append("    SELECT GRADE,HR_CLASS,K1.SCHREGNO, ");
        stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        stb.append("           SUBCLASSCD,SCORE  ");
        stb.append("    FROM   SCHNO K1,KIN_REC K2   ");
        stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO )  ");
        if (sqldiv == 1 || sqldiv == 2) {
            //１．在籍・２．休学・３．留学
            stb.append(",NINZUU AS( ");
            if (sqldiv == 1) {
                stb.append("    SELECT GRADE, ");
            } else if (sqldiv == 2) {
                stb.append("    SELECT 'CMN_KEY' AS CMN_KEY, ");
            }
            stb.append("           COUNT(K1.SCHREGNO) AS ZAI, ");
            stb.append("           SUM(CASE WHEN K3.TRANSFERCD='2' THEN 1 ELSE 0 END) AS KYU, ");
            stb.append("           SUM(CASE WHEN K3.TRANSFERCD='1' THEN 1 ELSE 0 END) AS RYU ");
            stb.append("    FROM   SCHNO K1  ");
            stb.append("           LEFT JOIN IDOU1 K2 ON K2.SCHREGNO=K1.SCHREGNO ");
            stb.append("           LEFT JOIN IDOU2 K3 ON K3.SCHREGNO=K1.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ");
            if (sqldiv == 1) {
                stb.append("    GROUP BY GRADE ");
            }
            stb.append(" ) ");
            //４．事故
            stb.append(",JIKO_CNT AS (  ");
            if (sqldiv == 1) {
                stb.append("    SELECT GRADE,COUNT(*) AS JIKO ");
                stb.append("    FROM   (SELECT DISTINCT SCHREGNO FROM KEKKA2) K1 ");
                stb.append("           LEFT JOIN SCHNO K2 ON K2.SCHREGNO=K1.SCHREGNO ");
                stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
                stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
                stb.append("    GROUP BY GRADE ");
            } else if (sqldiv == 2) {
                stb.append("    SELECT 'CMN_KEY' AS CMN_KEY,COUNT(*) AS JIKO  ");
                stb.append("    FROM   (SELECT DISTINCT SCHREGNO FROM KEKKA2) K1,SCHNO K2  ");
                stb.append("    WHERE  K2.SCHREGNO=K1.SCHREGNO AND ");
                stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
                stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            }
            stb.append(" ) ");
        }
        //５．受験・最高点・最低点・平均点分布
        stb.append(",SUB_REP AS(  ");
        stb.append("    SELECT ATTEND_SUBCLASSCD, COMBINED_SUBCLASSCD ");
        stb.append("      ,ATTEND_CLASSCD ");
        stb.append("      ,ATTEND_SCHOOL_KIND ");
        stb.append("      ,ATTEND_CURRICULUM_CD ");
        stb.append("      ,COMBINED_CLASSCD ");
        stb.append("      ,COMBINED_SCHOOL_KIND ");
        stb.append("      ,COMBINED_CURRICULUM_CD ");
        stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT  ");
        stb.append("    WHERE  YEAR = (SELECT YEAR FROM PARAM) AND REPLACECD='1' ) ");
        stb.append(",SCH_AVG AS( ");
        stb.append("    SELECT GRADE,HR_CLASS,K1.SCHREGNO, ");
        stb.append("           SUM(SCORE) AS KEI, ");
        stb.append("           COUNT(*) AS NIN, ");
        stb.append("           ROUND(AVG(FLOAT(SCORE)),0) AS SCHAVG ");
        stb.append("    FROM   SCHNO K1 ");
        stb.append("           LEFT JOIN ( ");
        stb.append("                SELECT SCHREGNO,SCORE FROM KIN_SCH T1  ");
        stb.append("                WHERE  SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM KEKKA2) ");
        stb.append("                       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1  ");
        stb.append("                                      WHERE ");
        stb.append("                                   W1.COMBINED_CLASSCD = T1.CLASSCD AND ");
        stb.append("                                   W1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
        stb.append("                                   W1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
        stb.append("                                            W1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");
        stb.append("                    ) K2 ON K1.SCHREGNO=K2.SCHREGNO ");
        stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
        stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
        stb.append("    GROUP BY GRADE,HR_CLASS,K1.SCHREGNO )  ");
        stb.append(",MAX_AVG AS( ");
        if (sqldiv == 1) {
            stb.append("    SELECT GRADE, ");
        } else if (sqldiv == 2 || sqldiv == 3) {
            stb.append("    SELECT 'CMN_KEY' AS CMN_KEY, ");
        }
        stb.append("           MAX(SCHAVG) AS MAXAVG, ");
        stb.append("           MIN(SCHAVG) AS MINAVG, ");
        stb.append("           SUM(CASE WHEN 90 <= SCHAVG THEN 1 ELSE 0 END) AS AVG1, ");
        stb.append("           SUM(CASE WHEN 85 <= SCHAVG AND SCHAVG < 90 THEN 1 ELSE 0 END) AS AVG2, ");
        stb.append("           SUM(CASE WHEN 80 <= SCHAVG AND SCHAVG < 85 THEN 1 ELSE 0 END) AS AVG3, ");
        stb.append("           SUM(CASE WHEN 75 <= SCHAVG AND SCHAVG < 80 THEN 1 ELSE 0 END) AS AVG4, ");
        stb.append("           SUM(CASE WHEN 70 <= SCHAVG AND SCHAVG < 75 THEN 1 ELSE 0 END) AS AVG5, ");
        stb.append("           SUM(CASE WHEN 60 <= SCHAVG AND SCHAVG < 70 THEN 1 ELSE 0 END) AS AVG6, ");
        stb.append("           SUM(CASE WHEN 50 <= SCHAVG AND SCHAVG < 60 THEN 1 ELSE 0 END) AS AVG7, ");
        stb.append("           SUM(CASE WHEN 40 <= SCHAVG AND SCHAVG < 50 THEN 1 ELSE 0 END) AS AVG8, ");
        stb.append("           SUM(CASE WHEN 35 <= SCHAVG AND SCHAVG < 40 THEN 1 ELSE 0 END) AS AVG9, ");
        stb.append("           SUM(CASE WHEN SCHAVG < 35 THEN 1 ELSE 0 END) AS AVG10, ");
        stb.append("           SUM(CASE WHEN SCHAVG <= 100 THEN 1 ELSE 0 END) AS JUKEN ");
        stb.append("    FROM   SCH_AVG ");
        if (sqldiv == 1) {
            stb.append("    GROUP BY GRADE ");
        }
        stb.append(" ) ");
        if (sqldiv == 1 || sqldiv == 2) {
            //６．平均点
            stb.append(",CLS_AVG AS( ");
            if (sqldiv == 1) {
                stb.append("    SELECT GRADE, ");
            } else if (sqldiv == 2) {
                stb.append("    SELECT 'CMN_KEY' AS CMN_KEY, ");
            }
            stb.append("           SUM(SCORE) AS KEI, ");
            stb.append("           COUNT(*) AS NIN, ");
            stb.append("           ROUND(AVG(FLOAT(SCORE)),0) AS AVGAVG ");
            stb.append("    FROM   SCHNO K1 ");
            stb.append("           LEFT JOIN ( ");
            stb.append("                SELECT SCHREGNO,SCORE FROM KIN_SCH T1  ");
            stb.append("                WHERE  NOT EXISTS( ");
            stb.append("                            SELECT 'X' FROM KEKKA2 T2  ");
            stb.append("                            WHERE  T2.SCHREGNO=T1.SCHREGNO ");
            stb.append("                                   AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("                                   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("                                   AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                                   AND T2.SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("                       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1  ");
            stb.append("                                      WHERE ");
            stb.append("                                   W1.COMBINED_CLASSCD = T1.CLASSCD AND ");
            stb.append("                                   W1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            stb.append("                                   W1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("                                            W1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("                    ) K2 ON K1.SCHREGNO=K2.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            if (sqldiv == 1) {
                stb.append("    GROUP BY GRADE ");
            }
            stb.append(" )  ");
        }

        //７．欠点科目
        stb.append(",ELECT_DIV AS ( ");
        stb.append("    SELECT ");
        stb.append("      CLASSCD, ");
        stb.append("      SCHOOL_KIND, ");
        stb.append("      CURRICULUM_CD, ");
        stb.append("          SUBCLASSCD FROM V_SUBCLASS_MST ");
        stb.append("    WHERE YEAR = (SELECT YEAR FROM PARAM) AND ELECTDIV='1' ) ");
        stb.append(",TYPE_GROUP AS (  ");
        stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
        stb.append("       CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        stb.append("           SUBCLASSCD, "+param._scoreField+"_TYPE_ASSES_CD ");
        stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2  ");
        stb.append("    WHERE  W1.YEAR = (SELECT YEAR FROM PARAM) AND W1.YEAR=W2.YEAR AND  ");
        stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ) ");
        stb.append(",TYPE_ASSES AS (  ");
        stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH   ");
        stb.append("    FROM   TYPE_ASSES_MST  ");
        stb.append("    WHERE  YEAR = (SELECT YEAR FROM PARAM) AND TYPE_ASSES_LEVEL='1' ) ");
        stb.append(",SCH_CNT AS (  ");
        stb.append("    SELECT COUNT(K1.SUBCLASSCD) SUB_CNT, K1.SCHREGNO  ");
        stb.append("    FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4  ");

        stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO AND ");
        stb.append("           K1.CLASSCD = K3.CLASSCD AND  ");
        stb.append("           K1.SCHOOL_KIND = K3.SCHOOL_KIND AND  ");
        stb.append("           K1.CURRICULUM_CD = K3.CURRICULUM_CD AND  ");
        stb.append("           K1.SUBCLASSCD=K3.SUBCLASSCD AND  ");
        stb.append("           K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND  ");
        stb.append("           K1.SCHREGNO=K2.SCHREGNO AND K3."+param._scoreField+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND  ");
        stb.append("           K1.SCORE <= K4.TYPE_ASSES_HIGH  ");
        stb.append("           AND (K1.CLASSCD, K1.SCHOOL_KIND, K1.CURRICULUM_CD, K1.SUBCLASSCD) ");
        stb.append("                             NOT IN (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM ELECT_DIV) ");
        if (param._testkindcd.equals("0") || param._gakki.equals("3")) {
            stb.append("       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1 ");
            stb.append("                      WHERE ");
            stb.append("                            W1.ATTEND_CLASSCD = K3.CLASSCD AND ");
            stb.append("                            W1.ATTEND_SCHOOL_KIND = K3.SCHOOL_KIND AND ");
            stb.append("                            W1.ATTEND_CURRICULUM_CD = K3.CURRICULUM_CD AND ");
            stb.append("                            W1.ATTEND_SUBCLASSCD=K3.SUBCLASSCD) ");
        }
        stb.append("           AND NOT EXISTS(SELECT 'X' FROM KEKKA2 T2 ");
        stb.append("                          WHERE T2.SCHREGNO=K1.SCHREGNO ");
        stb.append("                            AND T2.CLASSCD = K1.CLASSCD ");
        stb.append("                            AND T2.SCHOOL_KIND = K1.SCHOOL_KIND ");
        stb.append("                            AND T2.CURRICULUM_CD = K1.CURRICULUM_CD ");
        stb.append("                                AND T2.SUBCLASSCD=K1.SUBCLASSCD) ");
        stb.append("    GROUP BY K1.SCHREGNO ) ");
        stb.append(",SUB_CNT AS (  ");
        if (sqldiv == 1) {
            stb.append("    SELECT GRADE, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT < 4 THEN 1 ELSE 0 END) AS SUB_UDR, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT > 3 THEN 1 ELSE 0 END) AS SUB_OVR ");
            stb.append("    FROM   SCHNO K1, SCH_CNT K2 ");
            stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            stb.append("    GROUP BY GRADE ) ");
        } else if (sqldiv == 2 || sqldiv == 3) {
            stb.append("    SELECT 'CMN_KEY' AS CMN_KEY, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT < 4 THEN 1 ELSE 0 END) AS SUB_UDR, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT > 3 THEN 1 ELSE 0 END) AS SUB_OVR ");
            stb.append("    FROM   SCHNO K1, SCH_CNT K2 ");
            stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ) ");
        }

        //メイン
        if (sqldiv == 1) {
            stb.append("SELECT INT(T1.GRADE) GR_CL, ");
            stb.append("       T2.ZAI, T2.KYU, T2.RYU, VALUE(T6.JIKO,0) AS JIKO, T4.JUKEN, ");
            stb.append("       INT(T4.MAXAVG) AS MAXAVG, ");
            stb.append("       INT(T4.MINAVG) AS MINAVG, ");
            stb.append("       INT(T3.AVGAVG) AS AVGAVG, ");
            stb.append("       T4.AVG1, ");
            stb.append("       T4.AVG2, ");
            stb.append("       T4.AVG3, ");
            stb.append("       T4.AVG4, ");
            stb.append("       T4.AVG5, ");
            stb.append("       T4.AVG6, ");
            stb.append("       T4.AVG7, ");
            stb.append("       T4.AVG8, ");
            stb.append("       T4.AVG9, ");
            stb.append("       T4.AVG10, ");
            stb.append("       VALUE(T5.SUB_UDR,0) AS SUB_UDR, ");
            stb.append("       VALUE(T5.SUB_OVR,0) AS SUB_OVR ");
            stb.append("FROM   HR_NAME T1 ");
            stb.append("       LEFT JOIN NINZUU T2 ON T2.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN CLS_AVG T3 ON T3.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN MAX_AVG T4 ON T4.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN SUB_CNT T5 ON T5.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN JIKO_CNT T6 ON T6.GRADE = T1.GRADE ");
            stb.append("ORDER BY T1.GRADE ");
        } else if (sqldiv == 2) {
            stb.append("SELECT T1.CMN_KEY, ");
            stb.append("       T2.ZAI, T2.KYU, T2.RYU, VALUE(T6.JIKO,0) AS JIKO, T4.JUKEN, ");
            stb.append("       INT(T4.MAXAVG) AS MAXAVG, ");
            stb.append("       INT(T4.MINAVG) AS MINAVG, ");
            stb.append("       INT(T3.AVGAVG) AS AVGAVG, ");
            stb.append("       T4.AVG1, ");
            stb.append("       T4.AVG2, ");
            stb.append("       T4.AVG3, ");
            stb.append("       T4.AVG4, ");
            stb.append("       T4.AVG5, ");
            stb.append("       T4.AVG6, ");
            stb.append("       T4.AVG7, ");
            stb.append("       T4.AVG8, ");
            stb.append("       T4.AVG9, ");
            stb.append("       T4.AVG10, ");
            stb.append("       VALUE(T5.SUB_UDR,0) AS SUB_UDR, ");
            stb.append("       VALUE(T5.SUB_OVR,0) AS SUB_OVR ");
            stb.append("FROM   HR_NAME T1 ");
            stb.append("       LEFT JOIN NINZUU T2 ON T2.CMN_KEY = T1.CMN_KEY ");
            stb.append("       LEFT JOIN CLS_AVG T3 ON T3.CMN_KEY = T1.CMN_KEY ");
            stb.append("       LEFT JOIN MAX_AVG T4 ON T4.CMN_KEY = T1.CMN_KEY ");
            stb.append("       LEFT JOIN SUB_CNT T5 ON T5.CMN_KEY = T1.CMN_KEY ");
            stb.append("       LEFT JOIN JIKO_CNT T6 ON T6.CMN_KEY = T1.CMN_KEY ");
        } else if (sqldiv == 3) {
            stb.append("SELECT T1.CMN_KEY, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG1)/T4.JUKEN)*1000,0)/10,4,1) AS AVG1, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG2)/T4.JUKEN)*1000,0)/10,4,1) AS AVG2, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG3)/T4.JUKEN)*1000,0)/10,4,1) AS AVG3, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG4)/T4.JUKEN)*1000,0)/10,4,1) AS AVG4, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG5)/T4.JUKEN)*1000,0)/10,4,1) AS AVG5, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG6)/T4.JUKEN)*1000,0)/10,4,1) AS AVG6, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG7)/T4.JUKEN)*1000,0)/10,4,1) AS AVG7, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG8)/T4.JUKEN)*1000,0)/10,4,1) AS AVG8, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG9)/T4.JUKEN)*1000,0)/10,4,1) AS AVG9, ");
            stb.append("       DECIMAL(ROUND((FLOAT(T4.AVG10)/T4.JUKEN)*1000,0)/10,4,1) AS AVG10, ");
            stb.append("       DECIMAL(ROUND((FLOAT(VALUE(T5.SUB_UDR,0))/T4.JUKEN)*1000,0)/10,4,1) AS SUB_UDR, ");
            stb.append("       DECIMAL(ROUND((FLOAT(VALUE(T5.SUB_OVR,0))/T4.JUKEN)*1000,0)/10,4,1) AS SUB_OVR ");
            stb.append("FROM   HR_NAME T1 ");
            stb.append("       LEFT JOIN MAX_AVG T4 ON T4.CMN_KEY = T1.CMN_KEY ");
            stb.append("       LEFT JOIN SUB_CNT T5 ON T5.CMN_KEY = T1.CMN_KEY ");
        }
        return stb.toString();
    }


    /**PrepareStatement作成**/
    private String sqlForm2(Param param){

        //	フォーム２・成績（クラス）
        StringBuffer stb = new StringBuffer();
        try {
            //在籍
            stb.append("WITH SCHNO AS(  ");
            stb.append("   SELECT GRADE,HR_CLASS,SCHREGNO  ");
            stb.append("   FROM   SCHREG_REGD_DAT  ");
            stb.append("   WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._loginGakki+"' AND GRADE IN "+param._sqlGrade+" ) ");
            //学級
            stb.append(",HR_NAME AS(  ");
            stb.append("   SELECT GRADE,HR_CLASS,HR_NAMEABBV  ");
            stb.append("   FROM   SCHREG_REGD_HDAT  ");
            stb.append("   WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._loginGakki+"' AND GRADE IN "+param._sqlGrade+" ) ");
            //---NO006 START
            stb.append(",SEM3_DATE AS ( ");
            stb.append("    SELECT SDATE,EDATE ");
            stb.append("    FROM   SEMESTER_MST ");
            stb.append("    WHERE  YEAR='"+param._year+"' AND SEMESTER='3' ) ");
            stb.append(",SEM3_RYU AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("    FROM   SCHREG_TRANSFER_DAT K1,SEM3_DATE K2");
            stb.append("    WHERE  TRANSFERCD='1' AND ");
            stb.append("           TRANSFER_SDATE BETWEEN SDATE AND EDATE ) ");
            //---NO006 END
            //基礎
            stb.append(",IDOU1 AS(  ");
            stb.append("    SELECT W1.SCHREGNO  ");
            stb.append("    FROM   SCHNO W1,SCHREG_BASE_MST W2  ");
            stb.append("    WHERE  W2.SCHREGNO=W1.SCHREGNO AND  ");
            stb.append("           W2.GRD_DIV IN ('2','3') AND W2.GRD_DATE < '"+param._date+"' ) ");//---NO011
            //異動
            stb.append(",IDOU2 AS(  ");
            stb.append("   SELECT w3.schregno, TRANSFERCD  ");
            stb.append("   FROM   SCHNO w1, schreg_transfer_dat w3  ");
            stb.append("   WHERE  w3.schregno=w1.schregno and w3.transfercd in ('1','2') and  ");
            if(param._gakki.equals("9")) //---NO006
                stb.append("      W3.SCHREGNO NOT IN (SELECT SCHREGNO FROM SEM3_RYU) AND ");//---NO006
            stb.append("		  '"+param._date+"' BETWEEN W3.TRANSFER_SDATE AND W3.TRANSFER_EDATE ) ");
            //公欠・欠席
            stb.append(",KEKKA2 AS( ");
            stb.append("    SELECT SCHREGNO,SUBCLASSCD  ");
            stb.append("      ,CLASSCD ");
            stb.append("      ,SCHOOL_KIND ");
            stb.append("      ,CURRICULUM_CD");
            stb.append("    FROM   KIN_RECORD_DAT ");
            stb.append("    WHERE  year='"+param._year+"' AND "+param._sqlScoreState+" ) ");
            //成績
            stb.append(",KIN_REC AS (  ");
            stb.append("    SELECT SUBCLASSCD,SCHREGNO,"+param._scoreField+" AS SCORE ");
            stb.append("      ,CLASSCD ");
            stb.append("      ,SCHOOL_KIND ");
            stb.append("      ,CURRICULUM_CD");
            stb.append("    FROM   KIN_RECORD_DAT  ");
            stb.append("    WHERE  YEAR='"+param._year+"' AND "+param._scoreField+" IS NOT NULL ");
            stb.append("           AND SUBCLASSCD NOT IN ('900100', '900200') ");//---NO006
            stb.append(        " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '"+param._year+"' AND NAMECD1 = 'D065') ");
            stb.append(") ,KIN_SCH AS (   ");
            stb.append("    SELECT GRADE,HR_CLASS,K1.SCHREGNO,SUBCLASSCD,SCORE  ");
            stb.append("      ,CLASSCD ");
            stb.append("      ,SCHOOL_KIND ");
            stb.append("      ,CURRICULUM_CD");
            stb.append("    FROM   SCHNO K1,KIN_REC K2   ");
            stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO )  ");
            //１．在籍・２．休学・３．留学
            stb.append(",NINZUU AS( ");
            stb.append("    SELECT GRADE,HR_CLASS, ");
            stb.append("           COUNT(K1.SCHREGNO) AS ZAI, ");
            stb.append("           SUM(CASE WHEN K3.TRANSFERCD='2' THEN 1 ELSE 0 END) AS KYU, ");
            stb.append("           SUM(CASE WHEN K3.TRANSFERCD='1' THEN 1 ELSE 0 END) AS RYU ");
            stb.append("    FROM   SCHNO K1  ");
            stb.append("           LEFT JOIN IDOU1 K2 ON K2.SCHREGNO=K1.SCHREGNO ");
            stb.append("           LEFT JOIN IDOU2 K3 ON K3.SCHREGNO=K1.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ");
            stb.append("    GROUP BY GRADE,HR_CLASS ) ");
            //４．事故
            stb.append(",JIKO_CNT AS (  ");
            stb.append("    SELECT GRADE,HR_CLASS,COUNT(*) AS JIKO ");
            stb.append("    FROM   (SELECT DISTINCT SCHREGNO FROM KEKKA2) K1 ");
            stb.append("           LEFT JOIN SCHNO K2 ON K2.SCHREGNO=K1.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            stb.append("    GROUP BY GRADE,HR_CLASS ) ");
            //５．最高点
            stb.append(",SUB_REP AS(  ");
            stb.append("    SELECT ATTEND_SUBCLASSCD, COMBINED_SUBCLASSCD ");
            stb.append("    ,ATTEND_CLASSCD, COMBINED_CLASSCD ");
            stb.append("    ,ATTEND_SCHOOL_KIND, COMBINED_SCHOOL_KIND ");
            stb.append("    ,ATTEND_CURRICULUM_CD, COMBINED_CURRICULUM_CD ");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT  ");
            stb.append("    WHERE  YEAR='"+param._year+"' AND REPLACECD='1' ) ");
            stb.append(",SCH_AVG AS( ");
            stb.append("    SELECT GRADE,HR_CLASS,K1.SCHREGNO, ");
            stb.append("           SUM(SCORE) AS KEI, ");
            stb.append("           COUNT(*) AS NIN, ");
            stb.append("           ROUND(AVG(FLOAT(SCORE)),0) AS AVG2 ");
            stb.append("    FROM   SCHNO K1 ");
            stb.append("           LEFT JOIN ( ");
            stb.append("                SELECT SCHREGNO,SCORE FROM KIN_SCH T1  ");
            stb.append("                WHERE  SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM KEKKA2) ");
            stb.append("                       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1  ");
            stb.append("                                      WHERE ");
            stb.append("                                   W1.COMBINED_CLASSCD = T1.CLASSCD AND ");
            stb.append("                                   W1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            stb.append("                                   W1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("                                            W1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("                    ) K2 ON K1.SCHREGNO=K2.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            stb.append("    GROUP BY GRADE,HR_CLASS,K1.SCHREGNO )  ");
            stb.append(",MAX_AVG AS( ");
            stb.append("    SELECT GRADE,HR_CLASS, ");
            stb.append("           MAX(AVG2) AS MAXAVG ");
            stb.append("    FROM   SCH_AVG ");
            stb.append("    GROUP BY GRADE,HR_CLASS )  ");
            //６．平均点
            stb.append(",CLS_AVG AS( ");
            stb.append("    SELECT GRADE,HR_CLASS, ");
            stb.append("           SUM(SCORE) AS KEI, ");
            stb.append("           COUNT(*) AS NIN, ");
            stb.append("           ROUND(AVG(FLOAT(SCORE)),0) AS AVGAVG ");
            stb.append("    FROM   SCHNO K1 ");
            stb.append("           LEFT JOIN ( ");
            stb.append("                SELECT SCHREGNO,SCORE FROM KIN_SCH T1  ");
            stb.append("                WHERE  NOT EXISTS( ");
            stb.append("                            SELECT 'X' FROM KEKKA2 T2  ");
            stb.append("                            WHERE  T2.SCHREGNO=T1.SCHREGNO AND  ");
            stb.append("                                   T2.CLASSCD = T1.CLASSCD AND ");
            stb.append("                                   T2.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            stb.append("                                   T2.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("                                   T2.SUBCLASSCD=T1.SUBCLASSCD) ");
            stb.append("                       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1  ");
            stb.append("                                      WHERE ");
            stb.append("                                   W1.COMBINED_CLASSCD = T1.CLASSCD AND ");
            stb.append("                                   W1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            stb.append("                                   W1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("                                            W1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD) ");

            stb.append("                    ) K2 ON K1.SCHREGNO=K2.SCHREGNO ");
            stb.append("    WHERE  K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            stb.append("    GROUP BY GRADE,HR_CLASS )  ");
            //７．欠点科目
            stb.append(",ELECT_DIV AS ( ");
            stb.append("    SELECT ");
            stb.append("      CLASSCD, ");
            stb.append("      SCHOOL_KIND, ");
            stb.append("      CURRICULUM_CD, ");
            stb.append("          SUBCLASSCD FROM V_SUBCLASS_MST ");
            stb.append("    WHERE YEAR='"+param._year+"' AND ELECTDIV='1' ) ");
            stb.append(",TYPE_GROUP AS (  ");
            stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
            stb.append("      CLASSCD, ");
            stb.append("      SCHOOL_KIND, ");
            stb.append("      CURRICULUM_CD, ");
            stb.append("           SUBCLASSCD, "+param._scoreField+"_TYPE_ASSES_CD ");
            stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2  ");
            stb.append("    WHERE  W1.YEAR='"+param._year+"' AND W1.YEAR=W2.YEAR AND  ");
            stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ) ");
            stb.append(",TYPE_ASSES AS (  ");
            stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH   ");
            stb.append("    FROM   TYPE_ASSES_MST  ");
            stb.append("    WHERE  YEAR='"+param._year+"' AND TYPE_ASSES_LEVEL='1' ) ");
            stb.append(",SCH_CNT AS (  ");
            stb.append("    SELECT COUNT(K1.SUBCLASSCD) SUB_CNT, K1.SCHREGNO  ");
            stb.append("    FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4  ");
            stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO AND ");
            stb.append("           K1.CLASSCD=K3.CLASSCD AND  ");
            stb.append("           K1.SCHOOL_KIND=K3.SCHOOL_KIND AND  ");
            stb.append("           K1.CURRICULUM_CD=K3.CURRICULUM_CD AND  ");
            stb.append("           K1.SUBCLASSCD=K3.SUBCLASSCD AND  ");
            stb.append("           K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND  ");
            stb.append("           K1.SCHREGNO=K2.SCHREGNO AND K3."+param._scoreField+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND  ");
            stb.append("           K1.SCORE <= K4.TYPE_ASSES_HIGH  ");
            stb.append("           AND (K1.CLASSCD, K1.SCHOOL_KIND, K1.CURRICULUM_CD, K1.SUBCLASSCD) ");
            stb.append("                             NOT IN (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM ELECT_DIV) ");
            //---NO010
            if (param._testkindcd.equals("0") || param._gakki.equals("3")) {
                stb.append("       AND NOT EXISTS(SELECT 'X' FROM SUB_REP W1 ");
                stb.append("                      WHERE ");
                stb.append("                            W1.ATTEND_CLASSCD=K3.CLASSCD AND ");
                stb.append("                            W1.ATTEND_SCHOOL_KIND=K3.SCHOOL_KIND AND ");
                stb.append("                            W1.ATTEND_CURRICULUM_CD=K3.CURRICULUM_CD AND ");
                stb.append("                            W1.ATTEND_SUBCLASSCD=K3.SUBCLASSCD) ");
            }
            stb.append("           AND NOT EXISTS(SELECT 'X' FROM KEKKA2 T2 ");
            stb.append("                          WHERE T2.SCHREGNO=K1.SCHREGNO ");
            stb.append("                                AND T2.CLASSCD = K1.CLASSCD ");
            stb.append("                                AND T2.SCHOOL_KIND = K1.SCHOOL_KIND ");
            stb.append("                                AND T2.CURRICULUM_CD = K1.CURRICULUM_CD ");
            stb.append("                                AND T2.SUBCLASSCD=K1.SUBCLASSCD) ");
            stb.append("    GROUP BY K1.SCHREGNO ) ");
            stb.append(",SUB_CNT AS (  ");
            stb.append("    SELECT GRADE,HR_CLASS, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT < 4 THEN 1 ELSE 0 END) AS SUB_UDR, ");
            stb.append("           SUM(CASE WHEN K2.SUB_CNT > 3 THEN 1 ELSE 0 END) AS SUB_OVR ");
            stb.append("    FROM   SCHNO K1, SCH_CNT K2 ");
            stb.append("    WHERE  K1.SCHREGNO=K2.SCHREGNO AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
            stb.append("           K1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU2) ");
            stb.append("    GROUP BY GRADE,HR_CLASS ) ");

            //メイン
            stb.append("SELECT T1.GRADE||T1.HR_CLASS GR_CL,T1.HR_NAMEABBV, ");
            stb.append("       T2.ZAI,T2.KYU,T2.RYU,VALUE(T6.JIKO,0) AS JIKO, ");
            stb.append("       INT(T4.MAXAVG) AS MAXAVG,INT(T3.AVGAVG) AS AVGAVG, ");
            stb.append("       VALUE(T5.SUB_UDR,0) AS SUB_UDR,VALUE(T5.SUB_OVR,0) AS SUB_OVR ");
            stb.append("FROM   HR_NAME T1 ");
            stb.append("       LEFT JOIN NINZUU T2 ON T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN CLS_AVG T3 ON T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN MAX_AVG T4 ON T4.GRADE = T1.GRADE AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN SUB_CNT T5 ON T5.GRADE = T1.GRADE AND T5.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN JIKO_CNT T6 ON T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            stb.append("ORDER BY T1.GRADE||T1.HR_CLASS ");

        } catch( Exception e ){
            log.warn("Pre_Stat4 error!");
        }
        return stb.toString();

    }//Pre_Stat4()の括り

    private static class Param {
        final String _year;
        String _loginGakki;
        final String _nendo;
        String _ctrlDate;
        final String _title;
        String _scoreField;
        final String _yearPre;
        final String _gakki;
        final String _sqlGrade;
        final String _date;
        final String _testkindcd;
        final String _sqlScoreState;
        final String _datePre;

        PreparedStatement _ps1 = null;
        PreparedStatement _ps2 = null;
        PreparedStatement _ps3 = null;
        PreparedStatement _ps4 = null;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            //  パラメータの取得
            _year = request.getParameter("YEAR");                                //年度
            _loginGakki = request.getParameter("GAKKI");                               //学期
            _yearPre = Integer.toString( Integer.parseInt(_year)-1 );            //前年度
            _gakki = _loginGakki;                                                    //学期の保管 04/12/15Add
            //学年の編集 04/12/15Add
            String pclass[] = request.getParameterValues("GRADE");
            String sqlGrade = "(";
            for(int ia=0 ; ia<pclass.length ; ia++){
                if(ia != 0) sqlGrade = sqlGrade + ",";
                sqlGrade = sqlGrade + "'" + pclass[ia] + "'";
            }
            sqlGrade = sqlGrade + ")";
            _sqlGrade = sqlGrade;

            String idobi = request.getParameter("DATE");                            //異動対象日付 NO002
            _date = idobi.replace('/','-');
            _testkindcd = request.getParameter("TESTKINDCD");                         //成績種別 平均(0),中間(01),期末(02)
            idobi = request.getParameter("DATE_PRE");                               //異動対象日付(前年度用)
            _datePre = idobi.replace('/','-');

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";       //年度

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            //  作成日(現在処理日)の取得
            try {
                returnval = getinfo.Control(db2);
                _ctrlDate = KNJ_EditDate.h_format_JP(db2, returnval.val3);    //作成日
                if (_gakki.equals("9")) {
                    _loginGakki = returnval.val2;    //学年末の場合、今学期をセット 04/12/15Add
                }
            } catch( Exception e ){
                log.warn("ctrl_date get error!");
            }
            //  学期名称の取得
            String title = null;
            try {
                returnval = getinfo.Semester(db2,_year,_gakki);    // 04/12/15Modify _1 ---> _7
                title = returnval.val1;      //学期名称
                if (_testkindcd.equals("01")) {
                    title += "中間";
                } else if (_testkindcd.equals("02")) {
                    title += "期末";
                } else if (_testkindcd.equals("0")) {
                    title += "平均";
                }
                if (_gakki.equals("9")) {
                    title  = "学年平均";
                }
            } catch( Exception e ){
                log.warn("Semester name get error!");
            }
            _title = title;
            getinfo = null;
            returnval = null;

            //  各学期の成績項目名の取得
            if (_testkindcd.equals("01")) {
                _scoreField = "SEM"+_gakki+"_INTER_REC";
            } else if (_testkindcd.equals("02")) {
                _scoreField = "SEM"+_gakki+"_TERM_REC";
            } else if (_testkindcd.equals("0")) {
                _scoreField = "SEM"+_gakki+"_REC";
            }
            if (_gakki.equals("3")) {
                _scoreField = "SEM3_TERM_REC";
            } else if (_gakki.equals("9")) {
                _scoreField = "GRADE_RECORD";
            }
            //_scoreField = "SEM"+_1+"_REC"; //各学期成績

            //  事故数（公欠・欠席）の条件
            if (_gakki.equals("3")) {//３学期
                _sqlScoreState = "     ((SEM3_TERM_REC  IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS'))) AND  "
                        + "       VALUE(SEM3_TERM_REC_FLG,'0') = '0' ";
            } else if (_gakki.equals("9")) {//学年末
                _sqlScoreState = " (( "
                        + " ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR  "
                        + "  (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND  "
                        + "   VALUE(SEM1_REC_FLG,'0') = '0' "
                        + " ) OR ("
                        + " ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR  "
                        + "  (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND  "
                        + "   VALUE(SEM2_REC_FLG,'0') = '0' "
                        + " ) OR ("
                        + " ((SEM3_TERM_REC  IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS'))) AND  "
                        + "   VALUE(SEM3_TERM_REC_FLG,'0') = '0' "
                        + " )) ";
            } else if (_testkindcd.equals("01") || _testkindcd.equals("02")) {//中間or期末
                _sqlScoreState = _scoreField+" IS NULL AND "+_scoreField+"_DI IN('KK','KS') ";
            } else if (_testkindcd.equals("0")) {//１・２学期平均
                _sqlScoreState = "     ((SEM"+_gakki+"_INTER_REC IS NULL AND SEM"+_gakki+"_INTER_REC_DI IN('KK','KS')) OR  "
                        + "      (SEM"+_gakki+"_TERM_REC  IS NULL AND SEM"+_gakki+"_TERM_REC_DI  IN('KK','KS'))) AND  "
                        + "       VALUE("+_scoreField+"_FLG,'0') = '0' ";
            } else {
                _sqlScoreState = "";
            }
        }

    }

}//クラスの括り

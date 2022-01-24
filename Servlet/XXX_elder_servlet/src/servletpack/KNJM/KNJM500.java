// kanji=漢字
/*
 * $Id: 0b2e855b87f0de6a873b82ba45d948a1ea0db595 $
 *
 * 作成日: 2005/06/03
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2009 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
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

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ５００＞  学習状況通知
 *
 *  2005/06/03 m-yama 作成日
 *  2005/08/17 m-yama NO001 学校名出力、備考欄の修正、最終回セルへの網掛け
 *  2006/01/06 m-yama NO002 年度を修正
 *  2006/01/06 m-yama NO003 合否欄出力
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM500 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM500.class);

    Param _param;

    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        final PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        final OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!");
        }

        _param = createParam(db2, request);

    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        PreparedStatement ps5 = null;
        PreparedStatement ps6 = null;
        PreparedStatement ps7 = null;
        PreparedStatement ps8 = null;

        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1());       //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2());       //設定データpreparestatement
            ps3 = db2.prepareStatement(Pre_Stat3());       //設定データpreparestatement
            ps4 = db2.prepareStatement(Pre_Stat4());       //設定データpreparestatement
            ps5 = db2.prepareStatement(Pre_Stat5());       //設定データpreparestatement
            ps6 = db2.prepareStatement(Pre_Stat6());       //設定データpreparestatement
            ps7 = db2.prepareStatement(Pre_Stat7());       //設定データpreparestatement
            ps8 = db2.prepareStatement(Pre_Stat8());       //設定データpreparestatement
        } catch (Exception ex) {
            log.error("SQL read error!");
        }
        //カウンタ
        //SVF出力
        int i = 0;
        while (i < _param._schno.length) {
            Set_Detail_1(db2, svf, _param._schno[i], ps1, ps2, ps3, ps4, ps5, ps6, ps7, ps8);
            i++;
        }
    //  該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1, ps2, ps3, ps4, ps5, ps6, ps7, ps8);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 
        out.close();

    }//doGetの括り

    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String schno,
    PreparedStatement ps1,
    PreparedStatement ps2,
    PreparedStatement ps3,
    PreparedStatement ps4,
    PreparedStatement ps5,
    PreparedStatement ps6,
    PreparedStatement ps7,
    PreparedStatement ps8)
    {
        int hr_attendcnt = 0;
        String subclasscd  ;            //科目コード
        String chaircd     ;            //講座コード
        String befseq = "*";            //回数判定用
        String gval   = "*";            //前回評定
        boolean setflg = false;         //回数判定用
        //NO003-->
        int sch_attend = 0;
        int sch_regulation = 0;
        int max_seq = 0;
        int seq_cnt = 0;
        StringBuffer seqcnt = new StringBuffer();
        String comma = "";
        PreparedStatement psMax = null;
        PreparedStatement psSeq = null;
        boolean judgeflg = true;
        //NO003<--
        //評定・判定印字フラグ
        boolean gradJudgeFlg = null != _param._gradValPrint && _param._gradValPrint.equals("1") ? true : false;
        try {

            svf.VrSetForm("KNJM500.frm", 4);

            ps5.setString(1,schno); //学籍番号
            ResultSet rs5 = ps5.executeQuery();

            //HR出席数
            while (rs5.next()) {
                hr_attendcnt++;
            }
            if (hr_attendcnt > 0){
                svf.VrsOut("HR_ATTEND"        , String.valueOf(hr_attendcnt) );
            }

            //既修得単位数
            PreparedStatement psGetCredit = db2.prepareStatement(sqlGetCredit(schno));
            ResultSet rsGetCredit = psGetCredit.executeQuery();
            while (rsGetCredit.next()){
                svf.VrsOut("CREDIT_CNT", rsGetCredit.getString("CREDIT_CNT"));
            }
            DbUtils.closeQuietly(null, psGetCredit, rsGetCredit);
            db2.commit();
            
            //コメント設定
            ResultSet rs6 = ps6.executeQuery();
            while (rs6.next()) {
                svf.VrsOut("COMMENT" + rs6.getString("REMARKID") , rs6.getString("REMARK") );
            }
            DbUtils.closeQuietly(rs6);

            ps1.setString(1,schno); //学籍番号
            ps1.setString(2,schno); //学籍番号
            ResultSet rs = ps1.executeQuery();

            int gyo   = 1;          //行数カウント用
            while (rs.next()) {
                judgeflg = true;
                if (gyo > 15) {
                    break;
                }
                //ヘッダ出力
                svf.VrsOut("NENDO"        , String.valueOf(_param._nendo));
                svf.VrsOut("SCHOOL"       , String.valueOf(_param._schoolName));  //NO001
                svf.VrsOut("DATE1"        , String.valueOf(_param._printDate));
                svf.VrsOut("HR_NAME"      , rs.getString("HR_NAME"));
                svf.VrsOut("SCHREGNO"     , String.valueOf(schno));
                svf.VrsOut("NAME"         , rs.getString("NAME"));
                svf.VrsOut("DATE2"        , String.valueOf(_param._rKijunBi));
                svf.VrsOut("DATE3"        , String.valueOf(_param._sKijunBi));

                subclasscd = rs.getString("SUBCLASSCD");
                chaircd = rs.getString("CHAIRCD");

                ps7.setString(1,chaircd);               //講座コード
                ps7.setString(2,subclasscd);            //科目コード
                ResultSet rs7 = ps7.executeQuery();
                String recordNo = "1";
                while (rs7.next()) {
                    recordNo = rs7.getString("REP_SEQ_ALL");
                    svf.VrsOut("SCH_REGULATION" + recordNo, rs7.getString("SCH_SEQ_MIN"));
                    svf.VrsOut("REGULATION" + recordNo, rs7.getString("REP_SEQ_ALL"));
                    sch_regulation = rs7.getInt("SCH_SEQ_MIN");         //NO003
                }

                //科目コード・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
                svf.VrsOut("SUBCLASS" + recordNo, rs.getString("SUBCLASSABBV"));

                svf.VrAttribute("CONDITIONS" + recordNo + "_" + rs.getString("STANDARD_SEQM"), "Paint=(2,80,2),Bold=1");
                svf.VrAttribute("CONDITIONS" + recordNo + "_" + rs.getString("STANDARD_SEQE"), "Paint=(2,60,2),Bold=1");


                //出席数
                ps3.setString(1,schno);         //学籍番号
                ps3.setString(2,chaircd);       //講座コード
                ps3.setString(3,schno);         //学籍番号
                ps3.setString(4,chaircd);       //講座コード
                ResultSet rs3 = ps3.executeQuery();
                while (rs3.next()) {
                    svf.VrsOut("SCH_ATTEND" + recordNo, rs3.getString("SCNT"));
                    sch_attend = rs3.getInt("SCNT");                //NO003
                }
                
                //単位数
                final String classCd = "1".equals(_param._useCurriculumcd) ? subclasscd.substring(4,6) : subclasscd.substring(0,2);
                ps4.setString(1, classCd);         //科目コード
                ps4.setString(2,subclasscd);            //科目コード
                ps4.setString(3,schno);                 //学籍番号
                ResultSet rs4 = ps4.executeQuery();
                while (rs4.next()) {
                    svf.VrsOut("CREDIT" + recordNo, rs4.getString("CREDITS"));
                }

                ps2.setString(1,subclasscd);            //科目コード
                ps2.setString(2,schno);                 //学籍番号
                ps2.setString(3,subclasscd);            //科目コード
                ps2.setString(4,schno);                 //学籍番号
                ps2.setString(5,subclasscd);            //科目コード
                ps2.setString(6,schno);                 //学籍番号
log.debug(String.valueOf(subclasscd));
log.debug(String.valueOf(schno));
log.debug(String.valueOf(chaircd));
                ResultSet rs2 = ps2.executeQuery();

                befseq = "*";
                gval   = "*";
                setflg = false;
                while (rs2.next()) {
                    if (befseq.equalsIgnoreCase(rs2.getString("STANDARD_SEQ")) && !setflg) {
                        if (gval.equals("受")) {
                            if (rs2.getString("GRDVALUE").equals("無")) {
                                svf.VrsOut("CONDITIONS" + recordNo + "_" + rs2.getString("STANDARD_SEQ"), String.valueOf("受"));
                                judgeflg = false;
                            } else {
                                svf.VrsOut("CONDITIONS" + recordNo + "_" + rs2.getString("STANDARD_SEQ"), String.valueOf("再"));
                                judgeflg = false;
                            }
                        }
                        setflg = true;
                    } else {
                        if (!befseq.equalsIgnoreCase(rs2.getString("STANDARD_SEQ"))) {
                            svf.VrsOut("CONDITIONS" + recordNo + "_" + rs2.getString("STANDARD_SEQ"), rs2.getString("GRDVALUE"));
                            setflg = false;
                            //NO003-->
                            if (rs2.getInt("GRAD_VALUE") < 2 || rs2.getInt("GRAD_VALUE") > 5) {
                                judgeflg = false;
                            }
                            //NO003<--
                        }
                    }

                    befseq = rs2.getString("STANDARD_SEQ");
                    gval = rs2.getString("GRDVALUE");
                }
                
                ps8.setString(1,subclasscd);            //科目コード
                ps8.setString(2,schno);                 //学籍番号
                ResultSet rs8 = ps8.executeQuery();
                while (rs8.next()) {
                    svf.VrsOut("TEST" + recordNo + "_1", rs8.getString("SEM1_TERM_SCORE"));
                    svf.VrsOut("TEST" + recordNo + "_2", rs8.getString("SEM2_TERM_SCORE"));
                    if (null != rs8.getString("GRAD_VALUE") && gradJudgeFlg) {
                        svf.VrsOut("RATING" + recordNo, rs8.getString("GRAD_VALUE"));
                    }
                    if (null != rs8.getString("SEM1_VALUE")) {
                        svf.VrsOut("TMP_RATING" + recordNo, rs8.getString("SEM1_VALUE"));
                    }
                    //NO003-->
                    if (!classCd.equals("90")) {
                        if (null != rs8.getString("GRAD_VALUE") && 2 <= rs8.getInt("GRAD_VALUE") && 5 >= rs8.getInt("GRAD_VALUE") && gradJudgeFlg){
                            svf.VrsOut("JUDGE" + recordNo, "合");
                        }
                    } else {
                        if (null != rs8.getString("GRAD_VALUE") && !"".equals(rs8.getString("GRAD_VALUE")) && gradJudgeFlg){
                            if (sch_regulation <= sch_attend){
                                psMax = db2.prepareStatement(getMax_Seq(subclasscd));
                                ResultSet rsMax = psMax.executeQuery();
                                while (rsMax.next()) {
                                    if (null != rsMax.getString("REP_SEQ_ALL")) {
                                        max_seq = rsMax.getInt("REP_SEQ_ALL");
                                    }
                                }
                                DbUtils.closeQuietly(null, psMax, rsMax);
                                comma = "";
                                seqcnt.append("(");
                                for (int i = 0 ; i < max_seq ; i++) {
                                    seqcnt.append(comma+(i+1));
                                    comma = ",";
                                }
                                seqcnt.append(")");
                                psSeq = db2.prepareStatement(getSeq_Cnt(subclasscd,seqcnt.toString(),schno));
                                ResultSet rsSeq = psSeq.executeQuery();
                                while (rsSeq.next()) {
                                    seq_cnt = rsSeq.getInt("SEQCNT");
                                }
                                seqcnt.delete(0,seqcnt.length());
                                DbUtils.closeQuietly(null, psSeq, rsSeq);
                                if (seq_cnt >= max_seq) {
                                    if (judgeflg && gradJudgeFlg) {
                                        svf.VrsOut("JUDGE" + recordNo, "合");
                                    }
                                }
                            }
                        }
                    }
                }

                svf.VrEndRecord();
                nonedata = true;
                gyo++;          //行数カウント用
            }
            DbUtils.closeQuietly(rs);

            for (int i = gyo; i <= 15; i++) {
                svf.VrsOut("KARA", "A");
                svf.VrEndRecord();
            }
        } catch (Exception ex) {
            log.error("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATABLE AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       t2.CLASSCD, ");
            stb.append("       t2.SCHOOL_KIND, ");
            stb.append("       t2.CURRICULUM_CD, ");
        }
        stb.append("     t2.SUBCLASSCD,MAX(APPDATE) AS APPDATE ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT t1 LEFT JOIN CHAIR_DAT t2 ON T1.CHAIRCD = T2.CHAIRCD ");
        stb.append("     AND T2.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._year + "' AND ");
        stb.append("     t1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     t1.SCHREGNO = ? AND ");
        stb.append("     t1.CHAIRCD NOT LIKE '92%' ");
        stb.append(" GROUP BY ");
        stb.append("     t1.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       t2.CLASSCD, ");
            stb.append("       t2.SCHOOL_KIND, ");
            stb.append("       t2.CURRICULUM_CD, ");
        }
        stb.append("     t2.SUBCLASSCD ");
        stb.append(" ),STANDATEMAX AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD,MAX(STANDARD_SEQ) AS STANDARD_SEQM ");
        stb.append(" FROM ");
        stb.append("     REP_STANDARDDATE_DAT T1 ");
        stb.append("     INNER JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.NAMECD1 = 'M002' ");
        stb.append("         AND T2.NAMECD2 = T1.REPORTDIV ");
        stb.append("         AND T2.NAMESPARE2 = '1' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND CHAIRCD NOT LIKE '92%' ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD ");
        stb.append(" ),STANDATESEC AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD,MAX(STANDARD_SEQ) AS STANDARD_SEQS ");
        stb.append(" FROM ");
        stb.append("     REP_STANDARDDATE_DAT T1 ");
        stb.append("     INNER JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.NAMECD1 = 'M002' ");
        stb.append("         AND T2.NAMECD2 = T1.REPORTDIV ");
        stb.append("         AND T2.NAMESPARE2 = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND CHAIRCD NOT LIKE '92%' ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD ");
        stb.append(" ),STANDATEEND AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD,MAX(STANDARD_SEQ) AS STANDARD_SEQE ");
        stb.append(" FROM ");
        stb.append("     REP_STANDARDDATE_DAT T1 ");
        stb.append("     INNER JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.NAMECD1 = 'M002' ");
        stb.append("         AND T2.NAMECD2 = T1.REPORTDIV ");
        stb.append("         AND T2.NAMESPARE2 = '3' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND CHAIRCD NOT LIKE '92%' ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     t1.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("    T1.SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSABBV,T3.NAME,T4.HR_NAME,STANDARD_SEQM, ");
        stb.append("     CASE WHEN STANDARD_SEQE IS NOT NULL THEN STANDARD_SEQE ELSE STANDARD_SEQS END AS STANDARD_SEQE ");
        stb.append(" FROM ");
        stb.append("     ATABLE T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T1.CLASSCD = T2.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN STANDATEMAX  T5 ON T1.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T1.CLASSCD = T5.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN STANDATESEC  T7 ON T1.SUBCLASSCD = T7.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T1.CLASSCD = T7.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = T7.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = T7.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN STANDATEEND  T6 ON T1.SUBCLASSCD = T6.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T1.CLASSCD = T6.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = T6.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = T6.CURRICULUM_CD ");
        }
        stb.append("     , SCHREG_BASE_MST T3 ");
        stb.append("     , SCHREG_REGD_HDAT T4 ");
        stb.append("     , SCHREG_REGD_DAT T5 ");
        stb.append(" WHERE ");
        stb.append("     T3.SCHREGNO = ? AND ");
        stb.append("     T3.SCHREGNO = T5.SCHREGNO AND ");
        stb.append("     T5.YEAR = '" + _param._year + "' AND ");
        stb.append("     T5.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T4.YEAR = T5.YEAR AND ");
        stb.append("     T4.SEMESTER = T5.SEMESTER AND ");
        stb.append("     T4.GRADE = T5.GRADE AND ");
        stb.append("     T4.HR_CLASS = T5.HR_CLASS ");
        stb.append(" GROUP BY ");
        stb.append("     t1.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
        } else {
            stb.append("    T1.SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSABBV,T3.NAME,T4.HR_NAME,STANDARD_SEQM,STANDARD_SEQS,STANDARD_SEQE ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat2()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_REP_PRESENT_DAT0 AS ( ");
        stb.append(" SELECT ");
        stb.append("     STANDARD_SEQ,MAX(RECEIPT_DATE) AS RECEIPT_DATE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? AND ");
        } else {
            stb.append("     SUBCLASSCD = ? AND ");
        }
        stb.append("     SCHREGNO = ? ");
        stb.append(" GROUP BY ");
        stb.append("     STANDARD_SEQ ");
        stb.append(" ) ");
        stb.append(" , MAX_REP_PRESENT_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.STANDARD_SEQ, T1.RECEIPT_DATE, MAX(RECEIPT_TIME) AS RECEIPT_TIME ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT T1, MAX_REP_PRESENT_DAT0 T0 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = ? AND ");
        } else {
            stb.append("     T1.SUBCLASSCD = ? AND ");
        }
        stb.append("     T1.SCHREGNO = ? AND ");
        stb.append("     T1.STANDARD_SEQ = T0.STANDARD_SEQ AND ");
        stb.append("     T1.RECEIPT_DATE = T0.RECEIPT_DATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, T1.SUBCLASSCD, T1.STANDARD_SEQ, T1.RECEIPT_DATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE,t1.RECEIPT_TIME, ");
        stb.append("     CASE WHEN t1.GRAD_VALUE IS NULL THEN '0' WHEN t1.GRAD_VALUE = '' THEN '0' ELSE t1.GRAD_VALUE END AS GRAD_VALUE, ");    //NO003
        stb.append("     CASE WHEN t1.GRAD_VALUE IS NULL THEN '受' WHEN t1.GRAD_VALUE = '' THEN '受' ELSE ABBV1 END AS GRDVALUE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT t1 ");
        stb.append("     LEFT JOIN NAME_MST ON GRAD_VALUE = NAMECD2 AND NAMECD1 = 'M003', ");
        stb.append("     MAX_REP_PRESENT_DAT t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = ? AND ");
        } else {
            stb.append("     t1.SUBCLASSCD = ? AND ");
        }
        stb.append("     t1.SCHREGNO = ? AND ");
        stb.append("     t1.STANDARD_SEQ = t2.STANDARD_SEQ AND ");
        stb.append("     t1.RECEIPT_DATE = t2.RECEIPT_DATE AND ");
        stb.append("     (t1.RECEIPT_TIME IS NULL AND t2.RECEIPT_TIME IS NULL OR t1.RECEIPT_TIME = t2.RECEIPT_TIME) ");
        stb.append(" ORDER BY ");
        stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE DESC,t1.RECEIPT_TIME DESC");
        return stb.toString();

    }//Pre_Stat2()の括り
    
    /**PrepareStatement作成**/
    private String Pre_Stat3()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" with kind1 as ( ");
        stb.append(" select ");
        stb.append("     SCHOOLINGKINDCD,SCHOOLING_SEQ ");
        stb.append(" from ");
        stb.append("     SCH_ATTEND_DAT ");
        stb.append(" where ");
        stb.append("     YEAR = '" + _param._year + "' and ");
        stb.append("     SCHREGNO = ? and ");
        stb.append("     EXECUTEDATE <= '"+ _param._sKijun.replace('/','-')+"' and ");
        stb.append("     CHAIRCD = ? and ");
        stb.append("     SCHOOLINGKINDCD = '1' ");
        stb.append(" group by ");
        stb.append("     SCHOOLINGKINDCD,SCHOOLING_SEQ ");
        stb.append(" ),etckind as ( ");
        stb.append(" select ");
        stb.append("     SCHOOLINGKINDCD,SCHOOLING_SEQ ");
        stb.append(" from ");
        stb.append("     SCH_ATTEND_DAT ");
        stb.append(" where ");
        stb.append("     YEAR = '" + _param._year + "' and ");
        stb.append("     SCHREGNO = ? and ");
        stb.append("     EXECUTEDATE <= '"+_param._sKijun.replace('/','-')+"' and ");
        stb.append("     CHAIRCD = ? and ");
        stb.append("     SCHOOLINGKINDCD <> '1' ");
        stb.append(" ),kindcnt as ( ");
        stb.append(" select ");
        stb.append("     count(*) as ktc ");
        stb.append(" from ");
        stb.append("     kind1 t1 ");
        stb.append(" ),etckindcnt as ( ");
        stb.append(" select ");
        stb.append("     count(*) as etc ");
        stb.append(" from ");
        stb.append("     etckind t1 ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("     ktc + etc as SCNT ");
        stb.append(" from ");
        stb.append("     kindcnt,etckindcnt ");
        return stb.toString();

    }//Pre_Stat3()の括り


    /**PrepareStatement作成**/
    private String Pre_Stat4()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("     CREDITS ");
        stb.append(" from ");
        stb.append("     SCHREG_REGD_DAT t1 ");
        stb.append("     LEFT JOIN CREDIT_MST t2 ON t1.COURSECODE = t2.COURSECODE ");
        stb.append("     AND t2.YEAR = '" + _param._year + "' ");
        stb.append("     AND t1.MAJORCD = t2.MAJORCD ");
        stb.append("     AND t1.GRADE = t2.GRADE ");
        stb.append("     AND t2.CLASSCD = ? ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND t2.CLASSCD || t2.SCHOOL_KIND || t2.CURRICULUM_CD || t2.SUBCLASSCD = ? ");
        } else {
            stb.append("     AND t2.SUBCLASSCD = ? ");
        }
        stb.append(" where ");
        stb.append("     t1.SCHREGNO = ? AND ");
        stb.append("     t1.YEAR = '" + _param._year + "' AND ");
        stb.append("     t1.SEMESTER = '" + _param._semester + "' ");
        return stb.toString();

    }//Pre_Stat4()の括り
    
    /**PrepareStatement作成**/
    private String Pre_Stat5()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     EXECUTEDATE,PERIODCD ");
        stb.append(" FROM ");
        stb.append("     HR_ATTEND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     SCHREGNO = ? AND ");
        stb.append("     EXECUTEDATE <= '" + _param._sKijun.replace('/','-') + "' AND ");
        stb.append("     CHAIRCD LIKE '92%' ");
        stb.append(" GROUP BY ");
        stb.append("     EXECUTEDATE,PERIODCD ");
        return stb.toString();
    }//Pre_Stat5()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat6()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REMARKID,REMARK ");
        stb.append(" FROM ");
        stb.append("     HREPORTREMARK_T_DAT ");
        return stb.toString();
    }//Pre_Stat6()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat7()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(t3.SCH_SEQ_MIN,0) AS SCH_SEQ_MIN, VALUE(t3.REP_SEQ_ALL,0) AS REP_SEQ_ALL ");
        stb.append(" FROM ");
        stb.append("     CHAIR_CORRES_DAT t3 ");
        stb.append(" WHERE ");
        stb.append("     t3.YEAR = '" + _param._year + "' AND ");
        stb.append("     t3.CHAIRCD = ? AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t3.CLASSCD || t3.SCHOOL_KIND || t3.CURRICULUM_CD || t3.SUBCLASSCD = ? ");
        } else {
            stb.append("     t3.SUBCLASSCD = ? ");
        }
        return stb.toString();
    }//Pre_Stat7()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat8()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     t2.SEM1_TERM_SCORE, ");
        stb.append("     t2.SEM2_TERM_SCORE, ");
        stb.append("     t2.SEM1_VALUE AS SEM1_VALUE, ");
        stb.append("     t2.GRAD_VALUE AS GRAD_VALUE ");
        stb.append(" FROM ");
        stb.append("     RECORD_DAT t2 ");
        stb.append(" WHERE ");
        stb.append("     t2.YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t2.CLASSCD || t2.SCHOOL_KIND || t2.CURRICULUM_CD || t2.SUBCLASSCD = ? AND ");
        } else {
            stb.append("     t2.SUBCLASSCD = ? AND ");
        }
        stb.append("     t2.TAKESEMES = '0' AND ");
        stb.append("     t2.SCHREGNO = ? ");
        return stb.toString();
    }//Pre_Stat8()の括り

    /**getMax_Seq作成 NO003**/
    private String getMax_Seq(final String subclasscd)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REP_SEQ_ALL ");
        stb.append(" FROM ");
        stb.append("     CHAIR_CORRES_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+subclasscd+"' ");
        } else {
            stb.append("     AND SUBCLASSCD = '"+subclasscd+"' ");
        }
        return stb.toString();
    }//getMax_Seq()の括り

    /**getSeq_Cnt作成 NO003**/
    private String getSeq_Cnt(final String subclasscd, final String instate, final String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     STANDARD_SEQ, ");
        stb.append("     MAX(RECEIPT_DATE) AS MAX_DATE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+subclasscd+"' ");
        } else {
            stb.append("     AND SUBCLASSCD = '"+subclasscd+"' ");
        }
        stb.append("     AND STANDARD_SEQ IN "+instate+" ");
        stb.append("     AND SCHREGNO = '"+schregno+"' ");
        stb.append(" GROUP BY ");
        stb.append("     STANDARD_SEQ ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS SEQCNT, ");
        stb.append("     MAX(MAX_DATE) AS MAX_DATE ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        return stb.toString();
    }//getSeq_Cnt()の括り

    /**getJudge作成 NO003**/
    private String getJudge(final String[] param, final String subclasscd, final String maxdate, final String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(MAX(GRAD_VALUE),'0') AS GRAD_VALUE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+subclasscd+"' ");
        } else {
            stb.append("     AND SUBCLASSCD = '"+subclasscd+"' ");
        }
        stb.append("     AND SCHREGNO = '"+schregno+"' ");
        stb.append("     AND RECEIPT_DATE = '"+maxdate+"' ");
        return stb.toString();
    }//getJudge()の括り

    private String sqlGetCredit(final String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHREGNO,sum(value(GET_CREDIT,0)) + sum(value(ADD_CREDIT,0)) as CREDIT_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = '"+schregno+"' ");
        stb.append("     AND YEAR <= '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        return stb.toString();
    }//sqlGetCredit()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps5,
        PreparedStatement ps6,
        PreparedStatement ps7,
        PreparedStatement ps8
    ) {
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
        DbUtils.closeQuietly(ps4);
        DbUtils.closeQuietly(ps5);
        DbUtils.closeQuietly(ps6);
        DbUtils.closeQuietly(ps7);
        DbUtils.closeQuietly(ps8);
    }//preStatClose()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrClass;
        private final String _rKijun;
        private final String _sKijun;
        private final String _com1;
        private final String _com2;
        private final String _com3;
        private final String _com4;
        private final String _com5;
        private final String _com6;
        private final String _gradValPrint;
        private final String[] _schno;
        private final String _printDate;
        private final String _nendo;
        private final String _rKijunBi;
        private final String _sKijunBi;
        private final String _schoolName;
        private final String _useCurriculumcd;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _rKijun = request.getParameter("RKIJUN");
            _sKijun = request.getParameter("SKIJUN");
            _com1 = request.getParameter("COM1");
            _com2 = request.getParameter("COM2");
            _com3 = request.getParameter("COM3");
            _com4 = request.getParameter("COM4");
            _com5 = request.getParameter("COM5");
            _com6 = request.getParameter("COM6");
            _gradValPrint = request.getParameter("GRADVAL_PRINT");
            _schno = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            //  作成日(現在処理日)の取得
            try {
                returnval = getinfo.Control(db2);
                _printDate = KNJ_EditDate.h_format_thi(returnval.val3,0);
                _nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
                _rKijunBi = KNJ_EditDate.h_format_JP_MD(_rKijun);
                _sKijunBi = KNJ_EditDate.h_format_JP_MD(_sKijun);
            } finally {
                db2.commit();
            }

            try {
                final String schoolSql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(schoolSql);
                rs = ps.executeQuery();
                String setSchoolName = "";
                while (rs.next()) {
                    setSchoolName = rs.getString("SCHOOLNAME1");
                }
                _schoolName = setSchoolName;
            } finally {
                db2.commit();
            }
        }
    }
}//クラスの括り

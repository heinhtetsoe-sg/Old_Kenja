// kanji=漢字
/*
 * $Id: d092dd61ea27e169a340aaac39351a4ee72d5e80 $
 *
 * 作成日: 2005/10/18
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ３２８＞  出身学校・塾別成績一覧表
 *
 *  2005/10/18 m-yama 作成日
 */

public class KNJD328 {

    private static final int _JITURYOKU_TEST_CNT = 9;

    private static final String _3KAMOKU_GOUKEI = "800101";
    private static final String _5KAMOKU_GOUKEI = "800102";
    private static final String _3KAMOKU_AVG = "800201";
    private static final String _5KAMOKU_AVG = "800202";

    private static final Log log = LogFactory.getLog(KNJD328.class);

    Param _param;
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        _param = createParam(db2, request);

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1());       //生徒及び公欠・欠席者
            ps2 = db2.prepareStatement(Pre_Stat2());       //HR出席数
            ps3 = db2.prepareStatement(Pre_Stat3());       //HR出席数
        } catch( Exception ex ) {
            log.warn("DB2 open error!");
        }
        //コース名設定
        Map coursegp = new HashMap();   //コースグループ単位にコース名を設定
        Set_Fsprcode(db2, svf, ps3, coursegp);
        //SVF出力
        for (int ia = 0; ia < _param._fsprCode.length ; ia++ ){
            log.debug(_param._fsprCode[ia]);
            if (Set_Detail_1(db2, svf, _param._fsprCode[ia], ps1, ps2, coursegp)) nonedata = true;
        }

        //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        Pre_Stat_f(ps1,ps2,ps3);    //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    /** SVF-FORM **/
    private void Set_Fsprcode(final DB2UDB db2, final Vrw32alp svf, final PreparedStatement ps3, final Map coursegp) {

        try {
            ResultSet rs = ps3.executeQuery();
            //グループ名設定
            String groubif = "";
            String groudat = "";

            while( rs.next() ){

                if (groubif.equalsIgnoreCase(rs.getString("COURSE_SEQ")+rs.getString("GRADE"))){
                    groudat = groudat+"・"+rs.getString("COURSECODENAME");
                }else {
                    groudat = rs.getString("COURSECODENAME");
                }
                coursegp.put(rs.getString("COURSE_SEQ")+rs.getString("GRADE"),groudat); //コースグループ単位にコース名を設定
                groubif = rs.getString("COURSE_SEQ")+rs.getString("GRADE");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("DB2 Set_Fsprcode error!");
        }

    }//Set_Fsprcode()の括り


    /** SVF-FORM **/
    private void Set_Head(final DB2UDB db2, final Vrw32alp svf, final String fsprcode, final String fsprnm) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJD328.frm", 4);              //共通フォーム
        svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year))+"年度");//年度

        //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
        svf.VrsOut("SCHOOLCD",fsprcode);      //学校又は、塾コード
        if (_param._outPut2.equals("2")){
            svf.VrsOut("SCHOOLNAME","学校名:"+fsprnm);        //学校名
        }else {
            svf.VrsOut("SCHOOLNAME","塾名:"+fsprnm);      //塾名
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り

    /** データ出力 **/
    private void Set_SubHead(
            final DB2UDB db2,
            final Vrw32alp svf,
            final ResultSet rs,
            final String coursenm,
            final int cnt
    ) {

        try {
            svf.VrsOut("GRADE",   "第"+String.valueOf(rs.getInt("GRADE"))+"学年");        //学年
            if (coursenm != null) {
                svf.VrsOut("COURSE",  "("+String.valueOf(cnt)+")"+coursenm);  //コース
            }else {
                svf.VrsOut("COURSE",  "("+String.valueOf(cnt)+")");   //コース
            }

            svf.VrEndRecord();
        } catch( SQLException ex ){
            log.warn("[KNJD328]Set_Detail_1_1 rs1 svf error!");
        }

    }//Set_SubHead()の括り



    /**SVF-FORM**/
    private boolean Set_Detail_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String fsprcode,
            final PreparedStatement ps1,
            final PreparedStatement ps2,
            final Map coursegp
    ) {
        boolean nonedata = false;
        boolean firstflg = true;    //ヘッダ出力フラグ
        boolean datachg  = true;    //コース出力フラグ
        String fsprnm = "";         //学校又は、塾名
        String gradebif = "";       //前回学年
        String databif = "";        //前回コースグループ
        int cnt = 1;                //コース内学年カウンタ
        int datacnt = 1;            //行数カウンタ
        int rencnt = 1;             //連番
        try {

            //学校又は、塾名取得
            ps2.setString(1,fsprcode);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                fsprnm = rs2.getString("FSPRNM");
            }
            rs2.close();

            int pp = 0;
            //メインデータ
            ps1.setString(++pp, fsprcode);
            ResultSet rs = ps1.executeQuery();
            while( rs.next() ){
                if ( firstflg ){
                    //見出し出力のメソッド
                    Set_Head(db2, svf, fsprcode, fsprnm);
                    firstflg = false;
                }
                if (!gradebif.equalsIgnoreCase(rs.getString("GRADE"))) {
                    cnt = 1;
                }
                if (!databif.equalsIgnoreCase(rs.getString("GRADE")+rs.getString("COURSE_SEQ"))) {
                    datachg = true;
                }
                if (datachg) {
                    if (datacnt > 28){
                        for (;datacnt < 31;datacnt++){
                            svf.VrsOut("MASK",    String.valueOf("S"));
                            svf.VrEndRecord();
                        }
                        datacnt = 1;
                    }
                    //コース名称を取得
                    String coursenm = (String)coursegp.get(rs.getString("COURSE_SEQ")+rs.getString("GRADE"));
                    //コース出力のメソッド
                    Set_SubHead(db2, svf, rs, coursenm, cnt);

                    datachg = false;
                    datacnt++;
                    cnt++;
                    rencnt = 1;
                }
                if (datacnt > 29){
                    for (;datacnt < 31;datacnt++){
                        svf.VrsOut("MASK",    String.valueOf("S"));
                        svf.VrEndRecord();
                    }
                    datacnt = 1;
                }
                //データ出力のメソッド
                Set_Maindata(svf,rs,rencnt);
                datacnt = datacnt+2;
                rencnt++;
                if (datacnt == 30) datacnt = 1;
                databif  = rs.getString("GRADE")+rs.getString("COURSE_SEQ");
                gradebif = rs.getString("GRADE");
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("Set_Detail_1 read error!");
        }
        return nonedata;

    }//boolean Set_Detail_1()の括り

    /** データ出力 **/
    private void Set_Maindata(Vrw32alp svf,ResultSet rs,int rencnt){

        try {
            svf.VrsOut("NUMBER",          String.valueOf(rencnt));                //連番
            svf.VrsOut("NAME",            rs.getString("NAME_SHOW"));             //氏名
            if (rs.getInt("GRADE") == 1){
                svf.VrsOut("EXAMNO",          rs.getString("SCHREGNO"));          //受験番号(学籍番号下4桁)
            }else {
                if (rs.getString("HRNO2") != null) {
                    svf.VrsOut("EXAMNO",          rs.getString("HRNO2"));             //受験番号(去年のクラス番号)
                }
            }

            svf.VrsOut("HR_CLASS",        rs.getString("HRNO"));                  //クラス番号

            //定期考査得点出力
            svf.VrsOut("POINT1_1",        rs.getString("SEM1_INTER_REC_TOTAL"));  //1中
            svf.VrsOut("POINT1_2",        rs.getString("SEM1_TERM_REC_TOTAL"));   //1期
            svf.VrsOut("POINT1_3",        rs.getString("SEM1_REC_TOTAL"));        //1学
            svf.VrsOut("POINT2_1",        rs.getString("SEM2_INTER_REC_TOTAL"));  //2中
            svf.VrsOut("POINT2_2",        rs.getString("SEM2_TERM_REC_TOTAL"));   //2期
            svf.VrsOut("POINT2_3",        rs.getString("SEM2_REC_TOTAL"));        //2学
            svf.VrsOut("POINT3_2",        rs.getString("SEM3_TERM_REC_TOTAL"));   //学末
            svf.VrsOut("POINT3_3",        rs.getString("SEM3_REC_TOTAL"));        //3学
            svf.VrsOut("RECORD",          rs.getString("GRADE_RECORD_TOTAL"));    //学年

            //実力テスト得点出力
            printScore(svf, rs, "TEST", "TOTAL");

            //定期考査席次
            svf.VrsOut("ORDER1_1",        rs.getString("SEM1_INTER_REC_RANK"));   //1中
            svf.VrsOut("ORDER1_2",        rs.getString("SEM1_TERM_REC_RANK"));    //1期
            svf.VrsOut("ORDER1_3",        rs.getString("SEM1_REC_RANK"));         //1学
            svf.VrsOut("ORDER2_1",        rs.getString("SEM2_INTER_REC_RANK"));   //2中
            svf.VrsOut("ORDER2_2",        rs.getString("SEM2_TERM_REC_RANK"));    //2期
            svf.VrsOut("ORDER2_3",        rs.getString("SEM2_REC_RANK"));         //2学
            svf.VrsOut("ORDER3_2",        rs.getString("SEM3_TERM_REC_RANK"));    //学末
            svf.VrsOut("ORDER3_3",        rs.getString("SEM3_REC_RANK"));         //3学
            svf.VrsOut("RECORD_ORDER",    rs.getString("GRADE_RECORD_RANK"));     //学年

            //実力テスト席次
            printScore(svf, rs, "TEST_ORDER", "SCHOOL_PRECEDENCE");

            svf.VrEndRecord();
        } catch( SQLException ex ){
            log.warn("[KNJD328]Set_Detail_1_1 rs1 svf error!");
        }

    }//Set_Maindata()の括り

    private void printScore(
            final Vrw32alp svf,
            final ResultSet rs,
            final String setField,
            final String getField
    ) throws SQLException {
        for (int i = 1; i <= _JITURYOKU_TEST_CNT; i++) {

            final String subclassCnt = rs.getString("SUBCLASSCD_CNT" + i);

            String setTotal = "";
            //科目数によって、3科と5科を切り分ける。
            if (null != subclassCnt && Integer.parseInt(subclassCnt) <= 3) {
                setTotal = rs.getString(getField + i + "_3");
            } else {
                setTotal = rs.getString(getField + i + "_5");
            }
            svf.VrsOut(setField + i, setTotal);
        }
    }

    //出力メイン
    private String Pre_Stat1(){

        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    t1.GRADE,L3.COURSE_SEQ,L3.COURSECODE,L2.COURSECODENAME, ");
            stb.append("    t2.NAME_SHOW,cast(right(t1.SCHREGNO,4) as char(4)) as SCHREGNO, ");
            stb.append("    L1.HR_NAMEABBV || '-' || cast(right(t1.ATTENDNO,2) as char(4)) as HRNO, ");
            stb.append("    L1_1.HR_NAMEABBV || cast(right(L1_2.ATTENDNO,2) as char(4)) as HRNO2, ");
            //定期テスト
            stb.append("    L4.SEM1_INTER_REC_TOTAL,L4.SEM1_INTER_REC_RANK, ");
            stb.append("    L4.SEM1_TERM_REC_TOTAL,L4.SEM1_TERM_REC_RANK, ");
            stb.append("    L4.SEM1_REC_TOTAL,L4.SEM1_REC_RANK, ");
            stb.append("    L4.SEM2_INTER_REC_TOTAL,L4.SEM2_INTER_REC_RANK, ");
            stb.append("    L4.SEM2_TERM_REC_TOTAL,L4.SEM2_TERM_REC_RANK, ");
            stb.append("    L4.SEM2_REC_TOTAL,L4.SEM2_REC_RANK, ");
            stb.append("    L4.SEM3_TERM_REC_TOTAL,L4.SEM3_TERM_REC_RANK, ");
            stb.append("    L4.SEM3_REC_TOTAL,L4.SEM3_REC_RANK, ");
            stb.append("    L4.GRADE_RECORD_TOTAL,L4.GRADE_RECORD_RANK, ");
            //実力１
            int tableCnt = 1;
            int fieldCnt = 1;
            for (int i = 0; i < _JITURYOKU_TEST_CNT; i++) {
                final String lastSep = i == 8 ? "" : ",";
                stb.append(getShamexamSelectQuery(tableCnt, fieldCnt, lastSep));
                tableCnt += 4;
                fieldCnt++;
            }

            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT L1 ON t1.GRADE || t1.HR_CLASS = L1.GRADE || L1.HR_CLASS ");
            stb.append("    AND t1.YEAR = L1.YEAR AND t1.SEMESTER = L1.SEMESTER ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT L1_2 ON t1.SCHREGNO = L1_2.SCHREGNO ");
            stb.append("    AND cast(cast(t1.YEAR as int)-1 as char(4)) = L1_2.YEAR AND L1_2.SEMESTER = '" + _param._gakkiSu + "' ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT L1_1 ON L1_2.GRADE || L1_2.HR_CLASS = L1_1.GRADE || L1_1.HR_CLASS ");
            stb.append("    AND cast(cast(t1.YEAR as int)-1 as char(4)) = L1_1.YEAR AND L1_1.SEMESTER = L1_2.SEMESTER ");
            stb.append("    LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR = t1.YEAR ");
            stb.append("    AND L2.COURSECODE = t1.COURSECODE ");
            stb.append("    LEFT JOIN COURSE_GROUP_DAT L3 ON L3.YEAR = t1.YEAR ");
            stb.append("    AND L3.GRADE = t1.GRADE ");
            if (_param._isJunior){
                stb.append("    AND L3.HR_CLASS = '000' ");
            }else {
                stb.append("    AND L3.HR_CLASS = t1.HR_CLASS ");
            }
            stb.append("    AND L3.COURSECODE = t1.COURSECODE ");
            //席次データ
            stb.append("    LEFT JOIN RECORD_RANK_DAT L4 ON L4.YEAR = t1.YEAR ");
            stb.append("    AND L4.SCHREGNO = t1.SCHREGNO ");
            stb.append("    AND L4.RANK_DIV = '2' ");
            //実力１〜９
            final String[] shamexamCds = {"01", "02", "03", "04", "05", "06", "07", "08", "09"};
            int tableJoinCnt = 1;
            for (int i = 0; i < shamexamCds.length; i++) {
                stb.append(getShamexamJoinQuery(tableJoinCnt, shamexamCds[i]));
                tableJoinCnt += 4;
            }

            stb.append("    , SCHREG_BASE_MST t2 ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + _param._year + "' ");
            stb.append("    AND t1.SEMESTER = '" + _param._semester + "' ");
            if (_param._outPut2.equals("2")){
                stb.append("    AND t2.FINSCHOOLCD = ? ");
            }else {
                stb.append("    AND t2.PRISCHOOLCD = ? ");
            }
            stb.append("    AND t1.SCHREGNO = t2.SCHREGNO ");
            stb.append("ORDER BY ");
            stb.append("    t1.GRADE,L3.COURSE_SEQ,L3.COURSECODE,t1.HR_CLASS,t1.ATTENDNO ");

log.debug(stb);
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    private String getShamexamSelectQuery(final int tableNameCnt, final int fieldNameCnt, final String lastSep) {
        final String table1 = "SX" + tableNameCnt;
        final String table2 = "SX" + (tableNameCnt + 1);
        final String table3 = "SX" + (tableNameCnt + 2);
        final String table4 = "SX" + (tableNameCnt + 3);
        final StringBuffer stb = new StringBuffer();
        stb.append("    " + table1 + ".SUBCLASSCD_CNT as SUBCLASSCD_CNT" + fieldNameCnt + ", ");
        stb.append("    cast(cast(" + table1 + ".SCORE as smallint) as char(3)) as TOTAL" + fieldNameCnt + "_3, ");
        stb.append("    " + table1 + ".SCHOOL_PRECEDENCE as SCHOOL_PRECEDENCE" + fieldNameCnt + "_3, ");
        stb.append("    " + table3 + ".SCORE as AVG" + fieldNameCnt + "_3, ");
        stb.append("    cast(cast(" + table2 + ".SCORE as smallint) as char(3)) as TOTAL" + fieldNameCnt + "_5, ");
        stb.append("    " + table2 + ".SCHOOL_PRECEDENCE as SCHOOL_PRECEDENCE" + fieldNameCnt + "_5, ");
        stb.append("    " + table4 + ".SCORE as AVG" + fieldNameCnt + "_5 ");
        stb.append(lastSep + " ");

        return stb.toString();
    }

    private String getShamexamJoinQuery(int tableNameCnt, final String val) {
        final StringBuffer stb = new StringBuffer();
        stb.append(getShamexamJoin(tableNameCnt++, val, _3KAMOKU_GOUKEI));
        stb.append(getShamexamJoin(tableNameCnt++, val, _5KAMOKU_GOUKEI));
        stb.append(getShamexamJoin(tableNameCnt++, val, _3KAMOKU_AVG));
        stb.append(getShamexamJoin(tableNameCnt++, val, _5KAMOKU_AVG));

        return stb.toString();
    }

    private String getShamexamJoin(int tableNameCnt, final String shamexamCd, final String subclassCd) {
        final String table = "SX" + tableNameCnt;
        final StringBuffer stb = new StringBuffer();

        stb.append("    LEFT JOIN SHAMEXAMINATION_DAT " + table + " ON " + table + ".YEAR = t1.YEAR ");
        stb.append("    AND " + table + ".SHAMEXAMCD = '" + shamexamCd + "' ");
        stb.append("    AND t1.SCHREGNO = " + table + ".SCHREGNO ");
        stb.append("    AND " + table + ".SUBCLASSCD = '" + subclassCd + "' ");

        return stb.toString();
    }

    //科目抽出
    String Pre_Stat2()
    {
        StringBuffer stb = new StringBuffer();
        try {
            if (_param._outPut2.equals("2")){
                stb.append(" SELECT ");
                stb.append("     FINSCHOOL_NAME AS FSPRNM ");
                stb.append(" FROM ");
                stb.append("     V_FINSCHOOL_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' AND ");
                stb.append("     FINSCHOOLCD = ? ");
            }else {
                stb.append(" SELECT ");
                stb.append("     PRISCHOOL_NAME AS FSPRNM ");
                stb.append(" FROM ");
                stb.append("     V_PRISCHOOL_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' AND ");
                stb.append("     PRISCHOOLCD = ? ");
            }

//log.debug(stb);
        } catch( Exception e ){
            log.warn("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    //成績抽出
    String Pre_Stat3()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            if (_param._isJunior){
                stb.append("    t1.COURSE_SEQ,t1.GRADE,t2.COURSECODENAME ");
            }else {
                stb.append("    t1.COURSE_SEQ,t1.GRADE,t1.COURSECODE,t1.GROUP_NAME as COURSECODENAME ");
            }
            stb.append("FROM ");
            stb.append("    COURSE_GROUP_DAT t1 ");
            stb.append("    LEFT JOIN V_COURSECODE_MST t2 ON t1.YEAR = t2.YEAR ");
            stb.append("    AND t1.COURSECODE = t2.COURSECODE ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + _param._year + "' ");
            if (!_param._isJunior){
                stb.append("GROUP BY ");
                stb.append("    t1.COURSE_SEQ,t1.GRADE,t1.COURSECODE,t1.GROUP_NAME ");
            }
            stb.append("ORDER BY ");
            stb.append("    t1.GRADE,t1.COURSE_SEQ,t1.COURSECODE ");

        } catch( Exception e ){
            log.warn("Pre_Stat3 error!");
        }
//log.debug(stb);
        return stb.toString();

    }//Pre_Stat3()の括り

    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3)
    {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
        } catch( Exception e ){
            log.warn("Pre_Stat_f error!");
        }
    }//Pre_Stat_f()の括り

    /**
     * @param db2
     * @param request
     * @return
     */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _outPut2;
        private final boolean _isJunior;
        private final String _gakkiSu;
        private final String[] _fsprCode;

        private boolean _seirekiFlg;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _outPut2 = request.getParameter("OUTPUT2");
            _isJunior = request.getParameter("JHFLG").equals("1") ? true : false;
            _gakkiSu = request.getParameter("GAKKISU");
            _fsprCode = request.getParameterValues("DATA_SELECTED");

        }

    }

}//クラスの括り

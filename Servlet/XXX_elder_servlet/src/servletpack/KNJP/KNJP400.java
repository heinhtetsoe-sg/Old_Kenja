// kanji=漢字
/*
 * $Id: 0b8dd792e83aba9ec3fd9b9261885fc42bf75f31 $
 *
 * 作成日: 2006/03/30 9:32:43 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 *                  ＜ＫＮＪＰ４００＞  授業料納付状況
 *
 *  2005/12/15 m-yama 作成
 *  2006/03/06 m-yama NO001 振替停止を追加・・他
 */

public class KNJP400 {


    private static final Log log = LogFactory.getLog(KNJP400.class);
    private static final String IS_TYUGAKU = "1";
    private static final String IS_KOUKOU = "2";
    private static final String MCD11 = "11";
    private static final String MCD12 = "12";
    private static final String MCD13 = "13";

    /**
     * KNJP.classから最初に呼ばれる処理。
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[11];
        String paramcd = null;
        // パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");                   //年度
            param[2]  = request.getParameter("MONTH");                  //月+費目
            param[3]  = param[2].substring(0,param[2].indexOf("-"));    //月
            paramcd   = param[2].substring(param[2].indexOf("-")+1);    //費目
            param[5]  = request.getParameter("JHFLG");                  //中高判定フラグ 1:中学,2:高校
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        // print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        int ret = svf.VrInit();
        if (false && 0 != ret) { ret = 0; }

        ret = svf.VrSetSpoolFileStream(response.getOutputStream());

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        // メイン処理
        try {
            for (int i = 13; i <= Integer.parseInt(paramcd);i++) {
                param[4] = String.valueOf(i);
                mainProcess(db2, svf, param, false);
            }
        } catch (Exception e) {
            log.error("mainProcess error", e);
        }

        // 終了処理
        ret = svf.VrQuit();
        db2.commit();
        db2.close();
        outstrm.close();

    }//doGetの括り

    /**
     * メイン処理。
     * @param db2
     * @param svf
     * @param param
     * @throws Exception
     */
    private void mainProcess(DB2UDB db2,Vrw32alp svf,String param[], final boolean month3Flg) throws Exception {
        // ＳＶＦ作成処理
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        boolean nonedata = false;                       //該当データなしフラグ

        getHeaderData(db2,svf,param);                   //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        // 学年
        try {
            String grade = null;
            List gradeList = getGradeList(db2,param);
            final String maxGrade = (String) gradeList.get(gradeList.size()-1);
            for (Iterator it = gradeList.iterator(); it.hasNext();) {
                grade = (String) it.next();
                if (printMain(db2, svf, param, grade, maxGrade, month3Flg)) nonedata = true;
            }

            // 総合計データ取得
            Map printMapT = printDataSet(db2, svf, param, grade, 2, month3Flg);

            nonedata = printMeisai(svf,param,printMapT,grade,2);

        } catch (final SQLException e) {
            log.error("close error", e);
        }

        // 該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }
    }

    /**
     * ヘッダーデータを抽出。
     * @param db2
     * @param svf
     * @param param
     */
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        if (IS_TYUGAKU.equals(param[5])){
            svf.VrSetForm("KNJP400J.frm", 4);
        }else {
            svf.VrSetForm("KNJP400H.frm", 4);
        }

        // 学期取得
        try {
            // 8月は、固定1学期
            if (param[3].equals("8")){
                param[1] = "1";
            }else {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = ? AND ");
                stb.append("     SEMESTER < '9' AND ");
                stb.append("     ? BETWEEN ");
                stb.append("         CASE WHEN MONTH(SDATE) < 4 ");
                stb.append("              THEN MONTH(SDATE) + 12 ");
                stb.append("              ELSE MONTH(SDATE) ");
                stb.append("         END ");
                stb.append("        AND ");
                stb.append("         CASE WHEN MONTH(EDATE) < 4 ");
                stb.append("              THEN MONTH(EDATE) + 12 ");
                stb.append("              ELSE MONTH(EDATE) ");
                stb.append("         END ");
                stb.append(" ORDER BY ");
                stb.append("     SEMESTER DESC ");

                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(stb.toString());
                ps.setString(1,param[0]);
                final int setSem = Integer.parseInt(param[3]) < 4 ? Integer.parseInt(param[3]) + 12 : Integer.parseInt(param[3]);
                ps.setInt(2, setSem);
                rs = ps.executeQuery();
                while( rs.next()){
                    param[1] = rs.getString("SEMESTER");
                }
                psrsClose(ps,rs);
                db2.commit();
            }
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        // 作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        // 学校名の取得
        if (IS_TYUGAKU.equals(param[5])){
            param[7] = "中学校";
        }else {
            param[7] = "高等学校";
        }

        // 年度
        try {
            param[8] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        // 軽減額出力判定
        try {
            PreparedStatement ps = null;
            ps = db2.prepareStatement(getRedcCnt(param));
            ResultSet rs = ps.executeQuery();

            while ( rs.next() ){
                param[9] = rs.getString("RED_CNT");
            }
            psrsClose(ps,rs);

        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り

    /**
     * 学年取得。
     * @param db2
     * @param param
     * @return List&lt;学年コード&gt;
     * @throws Exception
     */
    private List getGradeList(DB2UDB db2,String param[]) throws Exception {
        // 学年の一覧
        List rtn = new LinkedList();
        String sql = "SELECT DISTINCT GRADE FROM SCHREG_REGD_HDAT WHERE YEAR = ? AND SEMESTER = ? ORDER BY GRADE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            ps.setString(1, param[0]);
            ps.setString(2, param[1]);
            rs = ps.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("GRADE");
                rtn.add(grade);
            }
        } finally {
            psrsClose(ps,rs);
        }
        return rtn;
    }

    /**
     * 印刷処理メイン。
     * @param db2
     * @param svf
     * @param param
     * @param grade
     * @param maxGrade
     * @return 印字データ有無
     */
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String grade,final String maxGrade, final boolean month3Flg)
    {
        boolean nonedata = false;
        try {

            printHead(svf, param, grade, month3Flg);
            // 明細データ取得
            Map printMapM = printDataSet(db2, svf, param, grade, 0, month3Flg);

            nonedata = printMeisai(svf,param,printMapM,grade,0);

            // 合計データ取得
            Map printMapT = printDataSet(db2, svf, param, grade, 1, month3Flg);

            nonedata = printMeisai(svf,param,printMapT,grade,1);

        } catch( Exception e ){
            log.warn("printMain get error!",e);
        }

        return nonedata;

    }//printMain()の括り

    /**
     * ヘッダデータをセット。
     * @param svf
     * @param param
     * @param grade
     */
    private void printHead(final Vrw32alp svf, final String param[], final String grade, final boolean month3Flg)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {

            ret = svf.VrsOut("NENDO"        , param[8]);
            ret = svf.VrsOut("SCHOOLDIV"    , param[7] );
            ret = svf.VrsOut("DATE"         , param[6] );
            ret = svf.VrsOut("MONTH"        , param[3] );
            ret = svf.VrsOut("GRADE"        , String.valueOf(Integer.parseInt(grade)));
            ret = svf.VrEndRecord();

        } catch( Exception ex ) {
            log.warn("printHead read error!",ex);
        }

    }//printHead()の括り

    /**
     * 印字データをMAPにセット。
     * @param rs
     * @param rtndatamap
     * @param rsPtarn
     */
    private void  printDataSet_Sub(final ResultSet rs, final Map rtndatamap, final int rsPtarn, final boolean month3Flg)
    {
        try {
            int fieldNo = 1;
            if (rsPtarn > 0){
                fieldNo = 2;
            }
            while ( rs.next() ){
                String setCd = rs.getString("SORT")+rs.getString("MONEY_DUE");
                rtndatamap.put(setCd, new TreeMap());
                if (rsPtarn == 0){
                    ((Map) rtndatamap.get(setCd)).put("MAJOR",rs.getString("MAJORNAME"));
                    ((Map) rtndatamap.get(setCd)).put("COURSE",rs.getString("REDUCTION"));
                    ((Map) rtndatamap.get(setCd)).put("DIVISION",rs.getString("NAME1"));
                }else {
                    ((Map) rtndatamap.get(setCd)).put("TOTALNAME",rs.getString("MAJORNAME"));
                }
                ((Map) rtndatamap.get(setCd)).put("REGD"+fieldNo,new Integer(month3Flg ? 0 : rs.getInt("ALL_CNT")));
                ((Map) rtndatamap.get(setCd)).put("PRICE"+fieldNo,new Integer(month3Flg ? 0 : rs.getInt("MONEY_DUE")));
                ((Map) rtndatamap.get(setCd)).put("ESTIMATE"+fieldNo,new Integer(month3Flg ? 0 : rs.getInt("MONEYSUM")));

                ((Map) rtndatamap.get(setCd)).put("NUMBER" + fieldNo + "_9", new Integer(rs.getInt("KUNI_REDUCFLG")));
                ((Map) rtndatamap.get(setCd)).put("MONEY" + fieldNo + "_9", new Integer(rs.getInt("KUNI_REDUCTIONMONEY")));

                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_10",new Integer(rs.getInt("KEN_REDUCFLG")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_10",new Integer(rs.getInt("KEN_REDUCTIONMONEY")));

                ((Map) rtndatamap.get(setCd)).put("NUMBER" + fieldNo + "_5", new Integer(rs.getInt("REDUCFLG")));
                ((Map) rtndatamap.get(setCd)).put("MONEY" + fieldNo + "_5", new Integer(rs.getInt("REDUCTIONMONEY")));

                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_8",new Integer(month3Flg ? 0 : rs.getInt("NECESSITY_MONEY_CNT")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_8",new Integer(month3Flg ? 0 : rs.getInt("NECESSITY_MONEY")));
                
                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_1",new Integer(month3Flg ? 0 : rs.getInt("M_PAID_CNT")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_1",new Integer(month3Flg ? 0 : rs.getInt("M_PAID_MONEY")));
                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_2",new Integer(month3Flg ? 0 : rs.getInt("REPAY_MONEY_CNT")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_2",new Integer(month3Flg ? 0 : rs.getInt("REPAY_MONEY")));
                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_6",new Integer(rs.getInt("REDUCFLG_OVR")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_6",new Integer(rs.getInt("REDUCTIONMONEY_OVR")));

                ((Map) rtndatamap.get(setCd)).put("L_CUMULATIVE"+fieldNo,new Integer(month3Flg ? 0 : rs.getInt("LM_PAID_MONEY")));
                ((Map) rtndatamap.get(setCd)).put("T_CUMULATIVE"+fieldNo,new Integer(month3Flg ? 0 : rs.getInt("PAID_MONEY")));
                ((Map) rtndatamap.get(setCd)).put("NUMBER"+fieldNo+"_3",new Integer(month3Flg ? 0 : rs.getInt("UNPAID_CNT")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_3",new Integer(month3Flg ? 0 : rs.getInt("UNPAID_MONEY")));
                ((Map) rtndatamap.get(setCd)).put("MONEY"+fieldNo+"_4",new Integer(month3Flg ? 0 : rs.getInt("NECESSITY_MONEY") - (rs.getInt("PAID_MONEY") + rs.getInt("UNPAID_MONEY") - rs.getInt("REPAY_MONEY")) ));
            }

        } catch( Exception ex ) {
            log.warn("printDataSet_Sub read error!",ex);
        }
    }

    /**
     * 印字データ 。
     * Key：ソートコード,Value：各印字項目。
     * @param db2
     * @param svf
     * @param param
     * @param grade
     * @param dataChange
     * @return Map&lt;k=ソート順,v=Map&lt;k=formフィールド名,v=セット値&gt;&gt;
     */
    private Map printDataSet(DB2UDB db2,Vrw32alp svf,String param[],String grade,int dataChange, final boolean month3Flg){

        PreparedStatement ps  = null;
        ResultSet rs  = null;
        Map rtndatamap = null;
        boolean nendoOver = false;      //NO001

        try {
            rtndatamap = new TreeMap();
            int paraMonth   = Integer.parseInt(param[3]);
            int paraYMonth  = 0;
            String paraYear = "";
            if (Integer.parseInt(param[3]) < 4){
                paraYMonth = Integer.parseInt(param[0])+1+Integer.parseInt(param[3]);
                paraYear   = String.valueOf(Integer.parseInt(param[0])+1);
                nendoOver  = true;
            }else {
                paraYMonth = Integer.parseInt(param[0])+Integer.parseInt(param[3]);
                paraYear   = param[0];
                nendoOver  = false;
            }

            // 印字データ
            ps = db2.prepareStatement(printDataSql(nendoOver, param, paraMonth, paraYMonth, paraYear, grade, dataChange, month3Flg));
            rs = ps.executeQuery();
            printDataSet_Sub(rs, rtndatamap, dataChange, month3Flg);
            psrsClose(ps,rs);

        } catch( Exception e ){
            log.warn("printDataSet get error!",e);
        }
        return rtndatamap;

    }//printDataSet()の括り

    /**
     * SVFへのセットメソッド。
     * @param svf
     * @param map
     */
    private void mapToSvf(Vrw32alp svf, Map map) {
        int ret = 0;
        for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (null == key || null == value) {
                log.debug("NULL! key="+key+", value="+value);
                continue;
            }
            if (value.toString().equals("0")) {
                ret = svf.VrAttribute(key.toString(), "Meido=100");
            }else {
                ret = svf.VrsOut(key.toString(), value.toString());
            }
            if (0 != ret) {
                log.debug("VrsOut:"+ret+", "+key);
            }
        }
    }

    /**
     * 空行印字。
     * @param svf
     */
    private void nullLinePrint(Vrw32alp svf) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {

            ret = svf.VrsOut("MASK" ,"1");
            ret = svf.VrEndRecord();

        } catch( Exception ex ) {
            log.warn("nullLinePrint read error!"+ex);
        }

    }

    /**
     * 明細データをセット。
     * @param svf
     * @param param
     * @param printMap
     * @param dataChange
     * @return 印字データの有無
     */
    private boolean printMeisai(
            final Vrw32alp svf,
            final String param[],
            final Map printMap,
            final String grade,
            final int dataChange
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        boolean nonedata    = false;
        try {

            // 明細
            for (final Iterator it = printMap.keySet().iterator();it.hasNext();){
                final String key = (String) it.next();
                final Map dataMap = (Map) printMap.get(key);

                // SVFへのセットメソッド
                mapToSvf(svf, dataMap);
                ret = svf.VrEndRecord();
                nonedata = true;
            }
            if (dataChange == 1){
                nullLinePrint(svf);
            }
        } catch( Exception ex ) {
            log.warn("printMeisai read error!"+ex);
        }
        return nonedata;
    }//printMeisai()の括り

    /**
     * preparedStatement,ResultSetのクローズ処理。
     * @param ps
     * @param rs
     */
    private void psrsClose(PreparedStatement ps,ResultSet rs){
        try {
            if (null != rs) { rs.close(); }
        } catch (final SQLException e) {
            log.error("rsclose error", e);
        }
        try {
            if (null != ps) { ps.close(); }
        } catch (final SQLException e) {
            log.error("psclose error", e);
        }
    }

    /**
     * 印字データ取得SQL
     * @param nendoOver
     * @param param
     * @param dataChange
     * @return 印字データを抽出する、SQLの文字列
     */
    private String printDataSql(
        final boolean nendoOver,
        final String param[],
        final int paraMonth,
        final int paraYMonth,
        final String paraYear,
        final String grade,
        final int dataChange,
        final boolean month3Flg
    ) {
        final int lastYear = Integer.parseInt(param[0]);
        final String reducCPaidMonth = "12".equals(param[4]) ? "09" : MCD13.equals(param[4]) ? "12" : "99";
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCHREG_DAT AS ( ");
            stb.append("SELECT ");
            stb.append("    t1.YEAR, ");
            stb.append("    t1.SCHREGNO, ");
            stb.append("    t1.SEMESTER, ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.COURSECD, ");
            stb.append("    t1.MAJORCD, ");
            stb.append("    t1.COURSECODE, ");
            stb.append("    t2.MAJORNAME, ");
            stb.append("    CASE WHEN SUBSTR(t3.HR_CLASS,1,1) = 'A' OR ");
            stb.append("              SUBSTR(t3.HR_CLASS,1,1) = 'B' OR ");
            stb.append("              SUBSTR(t3.HR_CLASS,1,1) = 'C' OR ");
            stb.append("              SUBSTR(t3.HR_CLASS,1,1) = 'D' OR ");
            stb.append("              (SUBSTR(t3.HR_CLASS,1,1) = 'P' AND t1.YEAR < '2006') ");
            stb.append("              THEN 'A' ");
            stb.append("              ELSE SUBSTR(t3.HR_CLASS,1,1) END AS HR_CLASS, ");
            stb.append("    CASE WHEN SUBSTR(t3.HR_CLASS,1,1) = 'J' THEN '中高一貫コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'K' THEN '国際コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'S' THEN '理数コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'A' THEN '普通コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'B' THEN '普通コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'C' THEN '普通コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'D' THEN '普通コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'G' THEN '英語特化コース' ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'P' THEN CASE WHEN t1.YEAR < '2006' THEN '普通コース' ");
            stb.append("                                                                            ELSE '進学コース' END ");
            stb.append("         WHEN SUBSTR(t3.HR_CLASS,1,1) = 'T' THEN '特進コース' ");
            stb.append("                                ELSE '' END ");
            stb.append("    AS REDUCTION ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            stb.append("    LEFT JOIN V_COURSE_MAJOR_MST t2 ON t2.YEAR = t1.YEAR ");
            stb.append("    AND t2.COURSECD || t2.MAJORCD = t1.COURSECD || t1.MAJORCD ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.YEAR = t1.YEAR ");
            stb.append("    AND t3.SEMESTER = t1.SEMESTER ");
            stb.append("    AND t3.GRADE || t3.HR_CLASS = t1.GRADE || t1.HR_CLASS ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + param[0] + "' ");
            stb.append("    AND t1.SEMESTER = '" + param[1] + "' ");
            if (dataChange < 2){
                stb.append("    AND t1.GRADE = '" + grade + "' ");
            }

            if (month3Flg) {
                stb.append(" ), MONEY_DUE_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     SUM(T1.MONEY_DUE) AS MONEY_DUE ");
                stb.append(" FROM ");
                stb.append("     MONEY_DUE_M_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param[0] + "' ");
                stb.append("    AND T1.EXPENSE_M_CD IN ('11', '12', '13') ");
                stb.append("    AND T1.SCHREGNO IN (SELECT ");
                stb.append("                            I1.SCHREGNO ");
                stb.append("                        FROM ");
                stb.append("                            SCHREG_DAT I1 ");
                stb.append("                        ) ");
                stb.append(" GROUP BY ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" ), REDUC_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
                stb.append("              THEN VALUE(T1.REDUCTIONMONEY_1, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
                stb.append("              THEN VALUE(T1.REDUCTIONMONEY_2, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTIONMONEY ");
                stb.append(" FROM ");
                stb.append("     REDUCTION_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param[0] + "' ");
                stb.append("    AND (T1.REDUC_DEC_FLG_1 = '1' ");
                stb.append("         OR ");
                stb.append("         T1.REDUC_DEC_FLG_2 = '1') ");
                stb.append("    AND T1.SCHREGNO IN (SELECT ");
                stb.append("                            I1.SCHREGNO ");
                stb.append("                        FROM ");
                stb.append("                            SCHREG_DAT I1 ");
                stb.append("                        ) ");
                stb.append(" GROUP BY ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" ), REDUC_COUNTRY_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTION_C_MONEY, ");
                stb.append("     SUM(CASE WHEN T1.PAID_YEARMONTH <= '" + param[0] + "-12-31" + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN T1.ADD_PAID_YEARMONTH <= '" + param[0] + "-12-31" + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTION_C12_MONEY ");
                stb.append(" FROM ");
                stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param[0] + "' ");
                stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("         OR ");
                stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
                stb.append(" GROUP BY ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" ), ADJUST AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ");
                stb.append("     VALUE(L1.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
                stb.append("     VALUE(L2.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
                stb.append("     VALUE(L2.REDUCTION_C12_MONEY, 0) AS REDUCTION_C12_MONEY ");
                stb.append(" FROM ");
                stb.append("     MONEY_DUE_T T1 ");
                stb.append("     LEFT JOIN REDUC_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
                stb.append("     LEFT JOIN REDUC_COUNTRY_T L2 ON T1.SCHREGNO = L2.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("     VALUE(L1.REDUCTIONMONEY, 0) + VALUE(L2.REDUCTION_C_MONEY, 0) < VALUE(T1.MONEY_DUE, 0) ");

                stb.append(" ), OVER_MONEY AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ");
                stb.append(" FROM ");
                stb.append("     REDUCTION_BURDEN_CHARGE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param[0] + "' ");
            }

            stb.append("), REDUCTION_DAT_ALL AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '" + MCD13 + "' AS EXPENSE_M_CD, ");
            stb.append("     CASE WHEN T1.REDUCTIONMONEY_1 IS NULL AND T1.REDUCTIONMONEY_2 IS NULL ");
            stb.append("          THEN NULL ");
            stb.append("          ELSE CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("                    THEN VALUE(T1.REDUCTIONMONEY_1, 0) ");
            stb.append("                    ELSE 0 ");
            stb.append("               END ");
            stb.append("               + ");
            stb.append("               CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("                    THEN VALUE(T1.REDUCTIONMONEY_2, 0) ");
            stb.append("                    ELSE 0 ");
            stb.append("               END ");
            stb.append("               - ");
            stb.append("               VALUE(L1.TOTAL_ADJUSTMENT_MONEY, 0) ");
            stb.append("     END AS REDUCTIONMONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT T1 ");
            stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     (T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("     OR T1.REDUC_DEC_FLG_2 = '1') ");

            stb.append(" ), INST_MIN AS ( ");
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     MIN(T2.PAID_MONEY_DATE) AS PAID_MONEY_DATE ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1, ");
            stb.append("     INSTALLMENT_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            stb.append("     AND T1.EXPENSE_M_CD = '" + param[4] + "' ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.INST_CD = T2.INST_CD ");
            stb.append("     AND T2.PAID_MONEY_DATE IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     T2.SCHREGNO ");

            stb.append(" ), PAY_SCH AS ( ");
            stb.append(" SELECT ");
            stb.append("     PAY.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("    (SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         INST_MIN ");
            stb.append("     WHERE ");
            stb.append("         MONTH(PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MONEY_PAID_M_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + param[0] + "' ");
            stb.append("         AND EXPENSE_M_CD = '" + param[4] + "' ");
            stb.append("         AND MONTH(PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) PAY ");

            stb.append("), REDUCTION_COUNTRY_ALL AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') <= '0' AND substr(T1.PAID_YEARMONTH, 5) = '" + reducCPaidMonth + "' ");
            stb.append("              THEN T1.PAID_MONEY ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') <= '0' AND substr(T1.ADD_PAID_YEARMONTH, 5) = '" + reducCPaidMonth + "' ");
            stb.append("              THEN T1.ADD_PAID_MONEY ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS KUNI_TOUGETU_PAID ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            if (!MCD13.equals(param[4])) {
                stb.append("     ,PAY_SCH T2 ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            if (!MCD13.equals(param[4])) {
                stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            }
            stb.append("     AND (T1.PAID_YEARMONTH IS NOT NULL ");
            stb.append("          OR T1.ADD_PAID_YEARMONTH IS NOT NULL) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO ");

            stb.append(" ), MINOU_SCH AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1 ");
            stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            stb.append("     AND T1.EXPENSE_M_CD = '" + MCD11 + "' ");
            stb.append("     AND (L1.SCHREGNO IS NULL ");
            stb.append("          OR ");
            stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
            stb.append(" UNION   ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1 ");
            stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            stb.append("     AND T1.EXPENSE_M_CD = '" + MCD12 + "' ");
            stb.append("     AND (L1.SCHREGNO IS NULL ");
            stb.append("          OR ");
            stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
            if (month3Flg) {
                stb.append(" UNION   ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     ADJUST T1 ");
                stb.append("     LEFT JOIN (SELECT ");
                stb.append("                    LL1.SCHREGNO, ");
                stb.append("                    SUM(VALUE(LL1.PAID_MONEY, 0)) AS PAID_MONEY ");
                stb.append("                FROM ");
                stb.append("                    MONEY_PAID_M_DAT LL1 ");
                stb.append("                WHERE ");
                stb.append("                    LL1.YEAR = '" + param[0] + "' ");
                stb.append("                    AND LL1.EXPENSE_M_CD IN ('11', '12', '13') ");
                stb.append("                GROUP BY ");
                stb.append("                    LL1.SCHREGNO ");
                stb.append("                ) L1 ON T1.SCHREGNO = L1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("     T1.TOTAL_LESSON_MONEY > L1.PAID_MONEY + T1.REDUCTIONMONEY + T1.REDUCTION_C12_MONEY ");
            }

            stb.append("), MAIN_T_SUB AS ( ");
            stb.append("SELECT ");
            stb.append("    T1.YEAR, ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.EXPENSE_M_CD, ");
            stb.append("    CASE WHEN VALUE(T2.PAID_MONEY_DIV,'00') > '01' ");
            stb.append("         THEN T4.COURSECD || T4.MAJORCD || T4.HR_CLASS || '999' ");
            stb.append("         ELSE CASE WHEN (T1.REDUCTION_REASON IS NULL OR T1.REDUCTION_REASON = '') ");
            stb.append("                   THEN CASE WHEN T1.INST_CD IS NULL ");
            stb.append("                             THEN T4.COURSECD || T4.MAJORCD || T4.HR_CLASS || '000' ");
            stb.append("                             ELSE T4.COURSECD || T4.MAJORCD || T4.HR_CLASS || '099' ");
            stb.append("                        END ");
            stb.append("                   ELSE T4.COURSECD || T4.MAJORCD || T4.HR_CLASS || '1' || T1.REDUCTION_REASON ");
            stb.append("              END ");
            stb.append("    END AS SORT, ");
            stb.append("    T4.MAJORNAME, ");
            stb.append("    T4.REDUCTION, ");
            stb.append("    CASE WHEN VALUE(T2.PAID_MONEY_DIV,'00') <= '01' ");
            stb.append("         THEN CASE WHEN (T1.REDUCTION_REASON IS NULL OR T1.REDUCTION_REASON = '') ");
            stb.append("                   THEN CASE WHEN T1.INST_CD IS NULL ");
            stb.append("                             THEN '一般' ");
            stb.append("                             ELSE '分納' ");
            stb.append("                        END ");
            stb.append("                   ELSE N1.NAME1 ");
            stb.append("              END ");
            stb.append("         ELSE '窓口等納入金' ");
            stb.append("    END AS NAME1, ");
            stb.append("    T1.MONEY_DUE, ");
            stb.append("    T1.REDUCTION_REASON, ");
            stb.append("    T1.INST_CD, ");
            stb.append("    T1.BANK_TRANS_STOP_RESON, ");
            stb.append("    T2.PAID_MONEY_DATE, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            if (nendoOver){
                stb.append("         THEN CASE WHEN (YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.PAID_MONEY_DATE) = " + paraYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ) ");
            }else {
                stb.append("         THEN CASE WHEN YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                   THEN VALUE(T2.PAID_MONEY,0) ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.PAID_MONEY,0) ");
            stb.append("    END AS PAID_MONEY, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            stb.append("         THEN CASE WHEN MONTH(T2.PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("                   THEN VALUE(T2.PAID_MONEY,0) ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.M_PAID_MONEY,0) ");
            stb.append("    END AS M_PAID_MONEY, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            stb.append("         THEN CASE WHEN MONTH(T2.PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("                   THEN 1 ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.M_PAID_CNT,0) ");
            stb.append("    END AS M_PAID_CNT, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            if (nendoOver){
                stb.append("         THEN CASE WHEN (YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.PAID_MONEY_DATE) = " + paraYear + " AND MONTH(T2.PAID_MONEY_DATE) < " + paraMonth + ") ");
            }else {
                stb.append("         THEN CASE WHEN YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) < " + paraMonth + " ");
            }
            stb.append("                   THEN VALUE(T2.PAID_MONEY,0) ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.LM_PAID_MONEY,0) ");
            stb.append("    END AS LM_PAID_MONEY, ");
            if (nendoOver){
                stb.append("    CASE WHEN (YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("              OR (YEAR(T2.PAID_MONEY_DATE) = " + paraYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("    CASE WHEN YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ");
            }
            stb.append("              AND T2.PAID_MONEY IS NOT NULL ");
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS PAIDFLG, ");
            stb.append("    CASE WHEN T2.PAID_MONEY IS NULL ");
            stb.append("         THEN CASE WHEN VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) >= VALUE(T1.MONEY_DUE,0) ");
            stb.append("                   THEN 0 ");
            stb.append("                   ELSE 1 ");
            stb.append("              END ");
            stb.append("         ELSE ");
            if (nendoOver){
                stb.append("              CASE WHEN (YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.PAID_MONEY_DATE) = " + paraYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ) ");
            }else {
                stb.append("              CASE WHEN YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                   THEN 0");
            stb.append("                   ELSE 1");
            stb.append("              END ");
            stb.append("    END AS UNPAID_CNT, ");
            stb.append("    CASE WHEN T2.PAID_MONEY IS NULL ");
            stb.append("         THEN CASE WHEN VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) >= VALUE(T1.MONEY_DUE,0) ");
            stb.append("                   THEN 0 ");
            stb.append("                   ELSE CASE WHEN T1.INST_CD IS NULL ");
            stb.append("                             THEN VALUE(T1.MONEY_DUE,0) - (VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0)) ");
            stb.append("                             ELSE VALUE(T1.MONEY_DUE,0) - VALUE(T5.PAID_MONEY,0) - (VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0)) ");
            stb.append("                        END ");
            stb.append("              END ");
            stb.append("         ELSE ");
            if (nendoOver){
                stb.append("              CASE WHEN (YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.PAID_MONEY_DATE) = " + paraYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ) ");
            }else {
                stb.append("              CASE WHEN YEAR(T2.PAID_MONEY_DATE) = " + lastYear + " AND MONTH(T2.PAID_MONEY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                   THEN 0");
            stb.append("                   ELSE CASE WHEN T1.INST_CD IS NULL ");
            stb.append("                             THEN VALUE(T1.MONEY_DUE,0) - (VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0)) ");
            stb.append("                             ELSE VALUE(T1.MONEY_DUE,0) - VALUE(T5.PAID_MONEY,0) - (VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0)) ");
            stb.append("                        END ");
            stb.append("         END ");
            stb.append("    END AS UNPAID_MONEY, ");
            stb.append("    VALUE(T2.PAID_MONEY_DIV,'00') AS PAID_MONEY_DIV, ");
            stb.append("    T2.REPAY_DATE, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            if (nendoOver){
                stb.append("         THEN CASE WHEN (YEAR(T2.REPAY_DATE) = " + lastYear + " AND MONTH(T2.REPAY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.REPAY_DATE) = " + paraYear + " AND MONTH(T2.REPAY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("         THEN CASE WHEN YEAR(T2.REPAY_DATE) = " + lastYear + " AND MONTH(T2.REPAY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                   THEN VALUE(T2.REPAY_MONEY,0) ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.REPAY_MONEY,0) ");
            stb.append("    END AS REPAY_MONEY, ");
            stb.append("    CASE WHEN T1.INST_CD IS NULL ");
            if (nendoOver){
                stb.append("         THEN CASE WHEN (YEAR(T2.REPAY_DATE) = " + lastYear + " AND MONTH(T2.REPAY_DATE) > 3) ");
                stb.append("                        OR (YEAR(T2.REPAY_DATE) = " + paraYear + " AND MONTH(T2.REPAY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("         THEN CASE WHEN YEAR(T2.REPAY_DATE) = " + lastYear + " AND MONTH(T2.REPAY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                   THEN 1 ");
            stb.append("                   ELSE 0 END ");
            stb.append("         ELSE VALUE(T5.REPAY_MONEY_CNT,0) ");
            stb.append("    END AS REPAY_MONEY_CNT, ");
            stb.append("    T2.REPAY_DEV, ");

            stb.append("    VALUE(REDUC.REDUCTIONMONEY,0) AS KEN_REDUCTIONMONEY, ");
            stb.append("    CASE WHEN VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KEN_REDUCFLG, ");

            stb.append("    VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) AS KUNI_REDUCTIONMONEY, ");
            stb.append("    CASE WHEN VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0 ");
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KUNI_REDUCFLG, ");

            stb.append("    VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) AS REDUCTIONMONEY, ");
            stb.append("    CASE WHEN VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("              OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0 ");
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCFLG, ");
            stb.append("    CASE WHEN (VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("               OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0) ");
            stb.append("              AND VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) < VALUE(T1.MONEY_DUE,0) ");
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCFLG_UDR, ");
            stb.append("    CASE WHEN (VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("               OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0) ");
            stb.append("              AND VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) >= VALUE(T1.MONEY_DUE,0) ");
            stb.append("         THEN 0 ");
            stb.append("         ELSE 1 ");
            stb.append("    END AS NECESSITY_MONEY_CNT, ");
            stb.append("    CASE WHEN (VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("               OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0) ");
            stb.append("              AND VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0) >= VALUE(T1.MONEY_DUE,0) ");
            stb.append("         THEN 0 ");
            stb.append("         ELSE VALUE(T1.MONEY_DUE,0) - (VALUE(REDUC.REDUCTIONMONEY,0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID,0)) ");
            stb.append("    END AS NECESSITY_MONEY, ");
            stb.append("    CASE WHEN (VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("               OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0) ");
            if (MCD13.equals(param[4])) {
                stb.append("              AND MINOU_SCH.SCHREGNO IS NULL ");
            }
            if (month3Flg) {
                stb.append("              AND VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) + VALUE(OVER_MONEY.OVER_MONEY, 0) >= VALUE(T1.MONEY_DUE, 0) ");
            } else {
                stb.append("              AND VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) >= VALUE(T1.MONEY_DUE, 0) ");
            }
            stb.append("         THEN 1 ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCFLG_OVR, ");
            stb.append("    CASE WHEN (VALUE(REDUC.REDUCTIONMONEY, 0) > 0 ");
            stb.append("               OR VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) > 0) ");
            if (MCD13.equals(param[4])) {
                stb.append("              AND MINOU_SCH.SCHREGNO IS NULL ");
            }
            if (month3Flg) {
                stb.append("              AND VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) + VALUE(OVER_MONEY.OVER_MONEY, 0) >= VALUE(T1.MONEY_DUE, 0) ");
                stb.append("         THEN VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) + VALUE(OVER_MONEY.OVER_MONEY, 0) - VALUE(T1.MONEY_DUE, 0) ");
            } else {
                stb.append("              AND VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) >= VALUE(T1.MONEY_DUE, 0) ");
                stb.append("         THEN VALUE(REDUC.REDUCTIONMONEY, 0) + VALUE(REDUC_C.KUNI_TOUGETU_PAID, 0) - VALUE(T1.MONEY_DUE, 0) ");
            }
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCTIONMONEY_OVR ");
            stb.append("FROM ");
            stb.append("    MONEY_DUE_M_DAT T1 ");
            stb.append("    LEFT JOIN MONEY_PAID_M_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.EXPENSE_L_CD = '01' ");
            stb.append("    AND T2.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    LEFT JOIN REDUCTION_DAT_ALL REDUC ON REDUC.YEAR = T1.YEAR ");
            stb.append("         AND REDUC.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND REDUC.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    LEFT JOIN REDUCTION_COUNTRY_ALL REDUC_C ON REDUC_C.YEAR = T1.YEAR ");
            stb.append("         AND REDUC_C.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            if (MCD13.equals(param[4])) {
                stb.append("    LEFT JOIN MINOU_SCH ON MINOU_SCH.SCHREGNO = T1.SCHREGNO ");
            }
            if (month3Flg) {
                stb.append("    LEFT JOIN OVER_MONEY ON OVER_MONEY.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append("    LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'G204' ");
            stb.append("         AND N1.NAMECD2 = T1.REDUCTION_REASON ");
            stb.append("    LEFT JOIN (SELECT ");
            stb.append("                   SCHREGNO, ");
            stb.append("                   INST_CD, ");
            stb.append("                   SUM(VALUE(INST_MONEY_DUE,0)) AS INST_MONEY_DUE, ");
            stb.append("                   SUM(VALUE(REPAY_MONEY,0)) AS REPAY_MONEY, ");
            stb.append("                   SUM(VALUE(REPAY_MONEY_CNT,0)) AS REPAY_MONEY_CNT, ");
            stb.append("                   SUM(VALUE(PAID_MONEY,0)) AS PAID_MONEY, ");
            stb.append("                   SUM(VALUE(M_PAID_MONEY,0)) AS M_PAID_MONEY, ");
            stb.append("                   SUM(M_PAID_CNT) AS M_PAID_CNT, ");
            stb.append("                   SUM(VALUE(LM_PAID_MONEY,0)) AS LM_PAID_MONEY ");
            stb.append("               FROM ");
            stb.append("                   (SELECT ");
            stb.append("                        SCHREGNO, ");
            stb.append("                        INST_CD, ");
            stb.append("                        VALUE(INST_MONEY_DUE,0) AS INST_MONEY_DUE, ");
            if (nendoOver){
                stb.append("                        CASE WHEN (YEAR(REPAY_DATE) = " + lastYear + " AND MONTH(REPAY_DATE) > 3) ");
                stb.append("                                  OR (YEAR(REPAY_DATE) = " + paraYear + " AND MONTH(REPAY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("                        CASE WHEN YEAR(REPAY_DATE) = " + lastYear + " AND MONTH(REPAY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                             THEN VALUE(REPAY_MONEY,0) ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS REPAY_MONEY, ");
            if (nendoOver){
                stb.append("                        CASE WHEN (YEAR(REPAY_DATE) = " + lastYear + " AND MONTH(REPAY_DATE) > 3) ");
                stb.append("                                  OR (YEAR(REPAY_DATE) = " + paraYear + " AND MONTH(REPAY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("                        CASE WHEN YEAR(REPAY_DATE) = " + lastYear + " AND MONTH(REPAY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                             THEN 1 ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS REPAY_MONEY_CNT, ");
            if (nendoOver){
                stb.append("                        CASE WHEN (YEAR(PAID_MONEY_DATE) = " + lastYear + " AND MONTH(PAID_MONEY_DATE) > 3) ");
                stb.append("                                  OR (YEAR(PAID_MONEY_DATE) = " + paraYear + " AND MONTH(PAID_MONEY_DATE) <= " + paraMonth + ") ");
            }else {
                stb.append("                        CASE WHEN YEAR(PAID_MONEY_DATE) = " + lastYear + " AND MONTH(PAID_MONEY_DATE) <= " + paraMonth + " ");
            }
            stb.append("                             THEN VALUE(PAID_MONEY,0) ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS PAID_MONEY, ");
            stb.append("                        CASE WHEN MONTH(PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("                             THEN VALUE(PAID_MONEY,0) ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS M_PAID_MONEY, ");
            stb.append("                        CASE WHEN MONTH(PAID_MONEY_DATE) = " + paraMonth + " ");
            stb.append("                             THEN 1 ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS M_PAID_CNT, ");
            if (nendoOver){
                stb.append("                        CASE WHEN (YEAR(PAID_MONEY_DATE) = " + lastYear + " AND MONTH(PAID_MONEY_DATE) > 3) ");
                stb.append("                                  OR (YEAR(PAID_MONEY_DATE) = " + paraYear + " AND MONTH(PAID_MONEY_DATE) < " + paraMonth + ") ");
            }else {
                stb.append("                        CASE WHEN YEAR(PAID_MONEY_DATE) = " + lastYear + " AND MONTH(PAID_MONEY_DATE) < " + paraMonth + " ");
            }
            stb.append("                             THEN VALUE(PAID_MONEY,0) ");
            stb.append("                             ELSE 0 ");
            stb.append("                        END AS LM_PAID_MONEY ");
            stb.append("                    FROM ");
            stb.append("                        INSTALLMENT_DAT ");
            stb.append("                    WHERE ");
            stb.append("                        YEAR = '" + param[0] + "') S1 ");
            stb.append("               GROUP BY ");
            stb.append("                   SCHREGNO, ");
            stb.append("                   INST_CD ");
            stb.append("              )T5 ");
            stb.append("    ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T5.INST_CD = T1.INST_CD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param[0] + "' ");
            stb.append("    AND T1.SCHREGNO IN (SELECT ");
            stb.append("                         SCHREGNO ");
            stb.append("                     FROM ");
            stb.append("                         SCHREG_DAT ");
            stb.append("                    ) ");
            stb.append("    AND T1.EXPENSE_M_CD = '" + param[4] + "' ");
            stb.append("    AND (T1.BANK_TRANS_STOP_RESON is null OR T1.BANK_TRANS_STOP_RESON = '') ");
            stb.append("ORDER BY ");
            stb.append("    SORT ");
            stb.append("), MAIN_T AS ( ");
            stb.append("SELECT ");
            stb.append("    YEAR, ");
            stb.append("    SCHREGNO, ");
            stb.append("    EXPENSE_M_CD, ");
            stb.append("    SORT, ");
            stb.append("    MAJORNAME, ");
            stb.append("    REDUCTION, ");
            stb.append("    NAME1, ");
            stb.append("    MONEY_DUE, ");
            stb.append("    REDUCTION_REASON, ");
            stb.append("    INST_CD, ");
            stb.append("    BANK_TRANS_STOP_RESON, ");
            stb.append("    PAID_MONEY_DATE, ");
            stb.append("    PAID_MONEY, ");
            stb.append("    M_PAID_MONEY, ");
            stb.append("    M_PAID_CNT, ");
            stb.append("    LM_PAID_MONEY, ");
            stb.append("    PAIDFLG, ");
            stb.append("    UNPAID_CNT, ");
            stb.append("    UNPAID_MONEY, ");
            stb.append("    PAID_MONEY_DIV, ");
            stb.append("    REPAY_DATE, ");
            stb.append("    REPAY_MONEY, ");
            stb.append("    REPAY_MONEY_CNT, ");
            stb.append("    REPAY_DEV, ");

            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN KEN_REDUCTIONMONEY ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KEN_REDUCTIONMONEY, ");
            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN KEN_REDUCFLG ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KEN_REDUCFLG, ");

            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN KUNI_REDUCTIONMONEY ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KUNI_REDUCTIONMONEY, ");
            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN KUNI_REDUCFLG ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS KUNI_REDUCFLG, ");

            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN REDUCTIONMONEY ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCTIONMONEY, ");
            stb.append("    CASE WHEN REDUCTIONMONEY_OVR > 0 OR PAID_MONEY > 0");
            stb.append("         THEN REDUCFLG ");
            stb.append("         ELSE 0 ");
            stb.append("    END AS REDUCFLG, ");

            stb.append("    REDUCFLG_UDR, ");
            stb.append("    NECESSITY_MONEY_CNT, ");
            stb.append("    CASE WHEN REPAY_DEV = '07' OR REPAY_DEV = '13' OR REPAY_DEV = '14' ");
            stb.append("         THEN NECESSITY_MONEY - REPAY_MONEY ");
            stb.append("         ELSE NECESSITY_MONEY ");
            stb.append("    END AS NECESSITY_MONEY, ");
            stb.append("    REDUCFLG_OVR, ");
            stb.append("    REDUCTIONMONEY_OVR ");
            stb.append("FROM ");
            stb.append("    MAIN_T_SUB ");
            stb.append(") ");
            if (dataChange == 0) {
                stb.append("SELECT ");
                stb.append("    SORT, ");
                stb.append("    MAJORNAME, ");
                stb.append("    REDUCTION, ");
                stb.append("    NAME1, ");
                stb.append("    COUNT(*) AS ALL_CNT, ");
                stb.append("    MONEY_DUE, ");
                stb.append("    SUM(VALUE(MONEY_DUE,0)) AS MONEYSUM, ");
                stb.append("    SUM(KEN_REDUCFLG) AS KEN_REDUCFLG, ");
                stb.append("    SUM(KEN_REDUCTIONMONEY) AS KEN_REDUCTIONMONEY, ");
                stb.append("    SUM(KUNI_REDUCFLG) AS KUNI_REDUCFLG, ");
                stb.append("    SUM(KUNI_REDUCTIONMONEY) AS KUNI_REDUCTIONMONEY, ");
                stb.append("    SUM(REDUCFLG) AS REDUCFLG, ");
                stb.append("    SUM(REDUCTIONMONEY) AS REDUCTIONMONEY, ");
                stb.append("    SUM(NECESSITY_MONEY_CNT) AS NECESSITY_MONEY_CNT, ");
                stb.append("    SUM(NECESSITY_MONEY) AS NECESSITY_MONEY, ");
                stb.append("    SUM(M_PAID_CNT) AS M_PAID_CNT, ");
                stb.append("    SUM(M_PAID_MONEY) AS M_PAID_MONEY, ");
                stb.append("    SUM(REPAY_MONEY_CNT) AS REPAY_MONEY_CNT, ");
                stb.append("    SUM(REPAY_MONEY) AS REPAY_MONEY, ");
                stb.append("    SUM(REDUCFLG_OVR) AS REDUCFLG_OVR, ");
                stb.append("    SUM(REDUCTIONMONEY_OVR) AS REDUCTIONMONEY_OVR, ");
                stb.append("    SUM(LM_PAID_MONEY) AS LM_PAID_MONEY, ");
                stb.append("    SUM(PAID_MONEY) AS PAID_MONEY, ");
                stb.append("    SUM(REDUCFLG_UDR) AS REDUCFLG_UDR, ");
                stb.append("    SUM(UNPAID_CNT) AS UNPAID_CNT, ");
                stb.append("    SUM(UNPAID_MONEY) AS UNPAID_MONEY, ");
                stb.append("    SUM(PAIDFLG) AS PAIDFLG ");
                stb.append("FROM ");
                stb.append("    MAIN_T ");
                stb.append("GROUP BY ");
                stb.append("    SORT, ");
                stb.append("    MAJORNAME, ");
                stb.append("    REDUCTION, ");
                stb.append("    NAME1, ");
                stb.append("    MONEY_DUE ");
            } else {
                stb.append("SELECT ");
                stb.append("    '99999999' AS SORT, ");
                if (dataChange == 1){
                    stb.append("    '合計' AS MAJORNAME, ");
                    stb.append("    '合計' AS REDUCTION, ");
                    stb.append("    '合計' AS NAME1, ");
                }else {
                    stb.append("    '総合計' AS MAJORNAME, ");
                    stb.append("    '総合計' AS REDUCTION, ");
                    stb.append("    '総合計' AS NAME1, ");
                }
                stb.append("    COUNT(*) AS ALL_CNT, ");
                stb.append("    CAST(NULL AS INTEGER) AS MONEY_DUE, ");
                stb.append("    SUM(VALUE(MONEY_DUE,0)) AS MONEYSUM, ");
                stb.append("    SUM(KEN_REDUCFLG) AS KEN_REDUCFLG, ");
                stb.append("    SUM(KEN_REDUCTIONMONEY) AS KEN_REDUCTIONMONEY, ");
                stb.append("    SUM(KUNI_REDUCFLG) AS KUNI_REDUCFLG, ");
                stb.append("    SUM(KUNI_REDUCTIONMONEY) AS KUNI_REDUCTIONMONEY, ");
                stb.append("    SUM(KEN_REDUCFLG) AS KEN_REDUCFLG, ");
                stb.append("    SUM(KEN_REDUCTIONMONEY) AS KEN_REDUCTIONMONEY, ");
                stb.append("    SUM(KUNI_REDUCFLG) AS KUNI_REDUCFLG, ");
                stb.append("    SUM(KUNI_REDUCTIONMONEY) AS KUNI_REDUCTIONMONEY, ");
                stb.append("    SUM(REDUCFLG) AS REDUCFLG, ");
                stb.append("    SUM(REDUCTIONMONEY) AS REDUCTIONMONEY, ");
                stb.append("    SUM(NECESSITY_MONEY_CNT) AS NECESSITY_MONEY_CNT, ");
                stb.append("    SUM(NECESSITY_MONEY) AS NECESSITY_MONEY, ");
                stb.append("    SUM(M_PAID_CNT) AS M_PAID_CNT, ");
                stb.append("    SUM(M_PAID_MONEY) AS M_PAID_MONEY, ");
                stb.append("    SUM(REPAY_MONEY_CNT) AS REPAY_MONEY_CNT, ");
                stb.append("    SUM(REPAY_MONEY) AS REPAY_MONEY, ");
                stb.append("    SUM(REDUCFLG_OVR) AS REDUCFLG_OVR, ");
                stb.append("    SUM(REDUCTIONMONEY_OVR) AS REDUCTIONMONEY_OVR, ");
                stb.append("    SUM(LM_PAID_MONEY) AS LM_PAID_MONEY, ");
                stb.append("    SUM(PAID_MONEY) AS PAID_MONEY, ");
                stb.append("    SUM(REDUCFLG_UDR) AS REDUCFLG_UDR, ");
                stb.append("    SUM(UNPAID_CNT) AS UNPAID_CNT, ");
                stb.append("    SUM(UNPAID_MONEY) AS UNPAID_MONEY, ");
                stb.append("    SUM(PAIDFLG) AS PAIDFLG ");
                stb.append("FROM ");
                stb.append("    MAIN_T ");
            }
            stb.append("ORDER BY ");
            stb.append("    SORT ");

        } catch( Exception e ){
            log.warn("printDataSql error!",e);
        }
        return stb.toString();
    }

    /**
     * 軽減額出力判定SQL。
     * @param param
     * @param sql
     * @return 軽減額出力判定用SQLの文字列
     */
    private String getRedcCnt(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCH_T AS ( ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER = '"+param[1]+"' ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS RED_CNT ");
            stb.append("FROM ");
            stb.append("    MONEY_PAID_M_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ");
            stb.append("    AND EXPENSE_M_CD = '" + MCD13 + "' ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(PAID_MONEY_DATE) > 3 ");
            }

//log.debug(stb);
        } catch( Exception e ){
            log.warn("getRedcCnt error!" ,e);
        }
        return stb.toString();
    }

}//クラスの括り

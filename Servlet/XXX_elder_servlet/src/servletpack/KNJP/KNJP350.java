package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
 *    学校教育システム 賢者 [入試管理]
 *
 *                    ＜ＫＮＪＰ３５０＞  授業料軽減補助金人数内訳
 *
 *    2005/12/05 m-yama 作成
 */

public class KNJP350 {


    private static final Log log = LogFactory.getLog(KNJP350.class);

    private String _output;
    private String _outputStr;
    private String _subTitle;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp();             //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        String param[] = new String[11];        //NO002

        //パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");            //年度
            param[1]  = request.getParameter("GAKKI");            //学期
            _output = request.getParameter("OUTPUT"); //1:支援額１(4〜6月) 2:支援額２(7〜3月)
            _outputStr = "_" + _output;
            _subTitle = ("1".equals(_output)) ? "（支援額１：４月\uFF5E６月）" : "（支援額２：７月\uFF5E３月）";
            param[10] = _output; //ログ確認のためセットしただけ
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();                               //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        //メイン処理
        try {
            mainProcess(db2,svf,param);
        } catch (Exception e) {
            log.error("mainProcess error", e);
        }

        //終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    //メイン処理
    private void mainProcess(DB2UDB db2,Vrw32alp svf,String param[]) throws Exception {
        //ＳＶＦ作成処理
        boolean nonedata = false;                        //該当データなしフラグ

        getHeaderData(db2,svf,param);                    //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //学年
        try {
            List gradeList = getGradeList(db2,param);
            final String maxGrade = (String) gradeList.get(gradeList.size()-1);
            for (Iterator it = gradeList.iterator(); it.hasNext();) {
                String grade = (String) it.next();
                if (printMain(db2,svf,param,grade,maxGrade)) nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("get gradeList error!",ex);
            return;
        }
//log.debug("nonedata="+nonedata);

        //該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }
    }


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        //作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[2] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        //年度
        try {
            param[3] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り


    private List getGradeList(DB2UDB db2,String param[]) throws Exception {
        // 学年の一覧
        List rtn = new LinkedList();
        String sql = "SELECT DISTINCT GRADE FROM SCHREG_REGD_HDAT WHERE YEAR = '" + param[0] + "' AND SEMESTER = '" + param[1] + "'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("GRADE");
                rtn.add(grade);
            }
        } finally {
            try {
                if (null != rs) { rs.close(); }
                if (null != ps) { ps.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }
        return rtn;
    }


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String grade,final String maxGrade)
    {
        boolean nonedata = false;
        try {

            //列データを取得
            Map  retumap   = getPrintRetu(db2,svf,param,grade);
            //頁単位の列データのIN文を作成
            List pageList  = getSplList(retumap);
            //印字処理
            nonedata = printExe(db2,svf,param,grade,maxGrade,retumap,pageList);

            log.debug(pageList.toString());

        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**列データのIN文を作成*/
    private List getSplList(Map retumap)
    {

//        Map pagemap   = new TreeMap();
        List pageList = new ArrayList();

        try {
            //列データを取得
            int retusu = 0;
            int maxmap = 0;
            int mapcnt = 0;
            int listsize = 0;
            int listcnt  = 0;
            StringBuffer instate = new StringBuffer();
            String com = "";
            instate.append("IN (");
            maxmap = maxmap + retumap.size();
            for (final Iterator it = retumap.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final List keyList = (List) retumap.get(key);
                //頁またがりの県は次頁へ
                if (retusu + keyList.size() > 15) {
                    //出力データありの場合
                    if (retusu > 0) {
                        instate.append(")");
                        pageList.add(String.valueOf(instate));
                        instate.delete(0, instate.length());
                        instate.append("IN (");
                        com = "";
                        retusu = 0;
                    }
                }
                mapcnt++;
                listsize = keyList.size();
                listcnt = 0;
                for (final Iterator it2 = keyList.iterator(); it2.hasNext();) {
                    //金額が15件以上は出力
                    if (retusu >= 15) {
                        instate.append(")");
                        pageList.add(String.valueOf(instate));
                        instate.delete(0, instate.length());
                        instate.append("IN (");
                        com = "";
                        listcnt = 0;
                        retusu = 0;
                    }
                    final String data = (String) it2.next();
                    instate.append(com+"'"+key+data+"'");
                    com = ",";
                    retusu++;
                    listcnt++;
                    //最終データ取得
                    if (maxmap == mapcnt && listcnt == listsize) {
                        instate.append(")");
                        pageList.add(String.valueOf(instate));
                    }
                }
            }
        } catch( Exception ex ) {
            log.warn("getSplList read error!",ex);
        }
        return pageList;

    }//getSplList()の括り

    //列データ (Key：県コード,Value：(金額A,金額B))
    private Map getPrintRetu(DB2UDB db2,Vrw32alp svf,String param[],String grade){

        PreparedStatement ps = null;
        ResultSet rs = null;
        Map rtnretumap = null;
        try {
            rtnretumap = new TreeMap();

            ps = db2.prepareStatement(retuSql(param[0], grade));
            rs = ps.executeQuery();
            while (rs.next()) {
                String prefCd = rs.getString("PREFECTURESCD");
                if (!rtnretumap.containsKey(prefCd)) {
                    rtnretumap.put(prefCd, new ArrayList());
                }
                String money = rs.getString("REDUCTIONMONEY"+_outputStr+"");
                ((List) rtnretumap.get(prefCd)).add(money);
            }
        } catch( Exception e ){
            log.warn("getPrintRetu get error!",e);
        } finally {
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
        return rtnretumap;

    }//getPrintRetu()の括り

    //印字処理
    private boolean printExe(DB2UDB db2,Vrw32alp svf,String param[],String grade,final String maxGrade,Map retuMap,List pageList) throws Exception 
    {

        boolean nonedata = false;

        int classcnt = 0;
        String sql = "SELECT DISTINCT HR_CLASS FROM SCHREG_REGD_HDAT WHERE YEAR = '" + param[0] + "' AND SEMESTER = '" + param[1] + "' AND GRADE = '" + grade + "' ORDER BY HR_CLASS";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            StringBuffer instate = new StringBuffer();
            String com = "";
            instate.append("IN (");
            //1頁分のクラス毎に印字
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                classcnt++;
                if (classcnt > 29){
                    instate.append(")");
                    setMeisai(db2,svf,param,grade,maxGrade,retuMap,pageList,String.valueOf(instate));
                    classcnt = 0;
                    instate.delete(0, instate.length());
                    instate.append("IN (");
                    com = "";
                }else {
                    final String data = rs.getString("HR_CLASS");
                    instate.append(com+"'"+data+"'");
                    com = ",";
                }
                nonedata = true;
            }
            if (classcnt > 0){
                instate.append(")");
//                log.debug(String.valueOf(instate));
                setMeisai(db2,svf,param,grade,maxGrade,retuMap,pageList,String.valueOf(instate));
            }
        } finally {
            try {
                if (null != rs) { rs.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
            try {
                if (null != ps) { ps.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }

        return nonedata;
    }

    /**明細データを取得*/
    private void setMeisai(DB2UDB db2,Vrw32alp svf,String param[],String grade,final String maxGrade,Map retuMap,List pageList,String classSt) throws Exception 
    {
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        Map headMap  = new TreeMap();    //印字列マップ
        int pagesize  = 0;
        int pagecnt   = 0;
        try {
            String defPage = null;
            String frmNo   = null;
            pagesize = pageList.size();
            int gyo = 1;
            for (final Iterator it = pageList.iterator();it.hasNext();){
                defPage = "none";
                pagecnt++;
                final String pageSt = (String) it.next();
                //ヘッダ部印刷
                ps1 = db2.prepareStatement(headSql(pageSt, param[0]));
                rs1 = ps1.executeQuery();
                headMap = printHead(db2,svf,param,grade,maxGrade,rs1,pagecnt,pagesize,frmNo,pageSt);

                //名細部印刷
                ps2 = db2.prepareStatement(mainSql(classSt,pageSt, param[0], param[1], grade));
                rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    if (defPage.equals("none")) {
                        classCntPrint(db2,svf,param,grade,rs2.getString("HR_CLASS"),gyo);
                    }else if(!defPage.equals("none") && !defPage.equals(rs2.getString("CHANGEGYO"))){
                        gyo++;
                        classCntPrint(db2,svf,param,grade,rs2.getString("HR_CLASS"),gyo);
                    }
                    defPage = rs2.getString("CHANGEGYO");
                    printMeisai(svf,param,grade,rs2,headMap,gyo,frmNo);
                }
                svf.VrEndPage();
                gyo = 1;
            }
        } finally {
            try {
                if (null != rs1) { rs1.close(); }
                if (null != ps1) { ps1.close(); }
                if (null != rs2) { rs2.close(); }
                if (null != ps2) { ps2.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }

    }//setMeisai()の括り

    /**ヘッダデータをセット*/
    private Map printHead(DB2UDB db2,Vrw32alp svf,String param[],String grade,final String maxGrade,
                        ResultSet rs,int pagecnt,int pagesize,String frmNo,String pageSt) throws Exception 
    {
        int retu  = 1;
        Map rtnHeadMap = new TreeMap();
        try {
            if (pagesize > pagecnt){
                //改頁ありの最終以外
                svf.VrSetForm("KNJP350_5.frm", 1);
                frmNo = "1";
            }else if(grade.equals(maxGrade)){
                //最終頁
                svf.VrSetForm("KNJP350_6.frm", 1);
                frmNo = "3";
                //合計出力
                etcPrint(db2,svf,param,grade,frmNo);
            }else {
                //学年単位の最終頁
                svf.VrSetForm("KNJP350_4.frm", 1);
                frmNo = "2";
                etcPrint(db2,svf,param,grade,frmNo);
            }

            //ヘッダ
            svf.VrsOut("NENDO"        , param[3] );
            svf.VrsOut("GRADE"        , String.valueOf(Integer.parseInt(grade))+"学年" );
            svf.VrsOut("SUBTITLE"     , _subTitle );
            //列データ
            while (rs.next()) {
                String retukey = rs.getString("RETUKEY");
                rtnHeadMap.put(retukey,String.valueOf(retu));
                svf.VrsOut("PREFECTURE"+retu        , rs.getString("NAME1") );
                svf.VrsOut("MONEY"+retu            , rs.getString("REDUCTIONMONEY"+_outputStr+"") );
                retu++;
            }
            //県別合計
            prefCntPrint(db2,svf,param,grade,frmNo,pageSt,rtnHeadMap);

        } catch( Exception e ){
            log.warn("getPrintRetu get error!",e);
        }

        return rtnHeadMap;

    }//printHead()の括り

    /**その他ヘッダデータをセット*/
    private void etcPrint(DB2UDB db2,Vrw32alp svf,String param[],String grade,String frmNo) throws Exception 
    {
        int total = 1;
        int totalMoney = 0;
        int totalCnt   = 0;
        String sql = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        String commonSql = commonSql(param[0]);
        try {
            switch (Integer.parseInt(frmNo)){
                case 3:    //最終頁
                    sql  = commonSql;
                    sql += "SELECT GRADE,SUM(REDUCTIONMONEY"+_outputStr+") AS REDUCTIONMONEY"+_outputStr+" FROM T_REDUCTION WHERE YEAR = '" + param[0] + "' AND REDUC_DEC_FLG"+_outputStr+" = '1' GROUP BY GRADE ORDER BY GRADE";
                    ps1 = db2.prepareStatement(sql);
                    rs1 = ps1.executeQuery();
                    while (rs1.next()) {
                        svf.VrsOutn("GRADE_TOTAL"        ,total    , rs1.getString("REDUCTIONMONEY"+_outputStr+"") );
                        totalMoney = totalMoney + rs1.getInt("REDUCTIONMONEY"+_outputStr+"");
                        total++;
                    }
                    svf.VrsOutn("GRADE_TOTAL"        ,4    , String.valueOf(totalMoney) );
                    totalMoney = 0;
                case 2:    //学年単位の最終頁
                    sql  = commonSql;
                    sql += "SELECT COUNT(REDUCTIONMONEY"+_outputStr+") AS CNT,SUM(REDUCTIONMONEY"+_outputStr+") AS REDUCTIONMONEY"+_outputStr+" FROM T_REDUCTION "
                            +" WHERE YEAR = '" + param[0] + "' AND GRADE = '" + grade + "' "
                            +" AND PREFECTURESCD IS NOT NULL "
                            +" AND PREFECTURESCD != '' "
                            +" AND REDUCTIONMONEY"+_outputStr+" IS NOT NULL "
                            +" AND REDUCTIONMONEY"+_outputStr+" > 0 "
                            +" AND REDUC_DEC_FLG"+_outputStr+" = '1' "
                            +" GROUP BY GRADE ORDER BY GRADE";
                    ps2 = db2.prepareStatement(sql);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        totalMoney = totalMoney + rs2.getInt("REDUCTIONMONEY"+_outputStr+"");
                        totalCnt   = totalCnt + rs2.getInt("CNT");
                        total++;
                    }
                    svf.VrsOutn("TOTAL_NUMBER"        ,31    , String.valueOf(totalCnt) );
                    svf.VrsOutn("TOTAL_MONEY"            ,31    , String.valueOf(totalMoney) );
                case 1:    //改頁ありの最終以外
                    break;
            }

        } finally {
            try {
                if (null != rs1) { rs1.close(); }
                if (null != ps1) { ps1.close(); }
                if (null != rs2) { rs2.close(); }
                if (null != ps2) { ps2.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }

    }//etcPrint()の括り

    /**県別合計をセット*/
    private void prefCntPrint(DB2UDB db2,Vrw32alp svf,String param[],String grade,String frmNo,
                            String pageSt,Map headMap) throws Exception 
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(prefCntSql(pageSt, param[0], grade));
            rs = ps.executeQuery();
            while (rs.next()) {
                String headKey = rs.getString("PREFECTURESCD")+rs.getString("REDUCTIONMONEY"+_outputStr+"");
                String retuKey = (String) headMap.get(headKey);
                int retuNo = Integer.parseInt(retuKey);
                svf.VrsOutn("NUMBER"+retuNo    ,31    , rs.getString("CNT") );
            }
            svf.VrsOutn("HR_NAME"            ,31    , "人数" );

        } finally {
            try {
                if (null != rs) { rs.close(); }
                if (null != ps) { ps.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }
    }//prefCntPrint()の括り


    /**県別合計をセット*/
    private void classCntPrint(DB2UDB db2,Vrw32alp svf,String param[],String grade,
                            String hr_class,int gyo) throws Exception 
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(classCntSql(param[0], param[1], grade, hr_class));
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOutn("TOTAL_NUMBER"    ,gyo    , rs.getString("CNT") );
                svf.VrsOutn("TOTAL_MONEY"        ,gyo    , rs.getString("REDUCTIONMONEY"+_outputStr+"") );
            }

        } finally {
            try {
                if (null != rs) { rs.close(); }
                if (null != ps) { ps.close(); }
            } catch (final SQLException e) {
                log.error("close error", e);
            }
        }
    }//classCntPrint()の括り


    /**明細データをセット*/
    private void printMeisai(Vrw32alp svf,String param[],String grade,ResultSet rs,Map headMap,int gyo,String frmNo) throws Exception 
    {
        try {

            //明細
            if (rs.getString("PREFECTURESCD") != null && rs.getString("REDUCTIONMONEY"+_outputStr+"") != null){
                String headKey = rs.getString("PREFECTURESCD")+rs.getString("REDUCTIONMONEY"+_outputStr+"");
                String retuKey = (String) headMap.get(headKey);
                int retuNo = Integer.parseInt(retuKey);
                svf.VrsOutn("NUMBER"+retuNo    ,gyo    , rs.getString("CNT") );
            }
            svf.VrsOutn("HR_NAME"            ,gyo    , rs.getString("HR_NAME") );
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**
     *    学年/県/別軽減データを抽出
     *
     */
    private String retuSql(String year, String grade)
    {
        StringBuffer stb = new StringBuffer();
        try {
            String commonSql = commonSql(year);
            stb.append(commonSql);

            stb.append("SELECT ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("FROM ");
            stb.append("    REDUCTION_MST ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND GRADE = '" + grade + "' ");
            stb.append("    AND REDUCTIONMONEY"+_outputStr+" > 0 ");
            stb.append("GROUP BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("UNION  ");
            stb.append("SELECT ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("FROM ");
            stb.append("    T_REDUCTION ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND PREFECTURESCD IS NOT NULL ");
            stb.append("    AND REDUCTIONMONEY"+_outputStr+" > 0 ");
            stb.append("    AND REDUC_DEC_FLG"+_outputStr+" = '1' ");
            stb.append("GROUP BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("ORDER BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" DESC ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("retuSql error!",e);
        }
        return stb.toString();

    }//retuSql()の括り

    /**
     *    クラス別軽減データを抽出
     *
     */
    private String mainSql(String classSt,String pageSt, String year, String semester, String grade)
    {
        StringBuffer stb = new StringBuffer();
        try {
            String commonSql = commonSql(year);
            stb.append(commonSql);

            stb.append("SELECT ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS, ");
            stb.append("    t1.GRADE || t1.HR_CLASS AS CHANGEGYO, ");
            stb.append("    t1.HR_NAME, ");
            stb.append("    t3.PREFECTURESCD, ");
            stb.append("    t3.REDUCTIONMONEY"+_outputStr+", ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT t1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.YEAR = t2.YEAR ");
            stb.append("    AND t1.SEMESTER = t2.SEMESTER ");
            stb.append("    AND t1.GRADE || t1.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("    LEFT JOIN T_REDUCTION t3 ON t1.YEAR = t3.YEAR ");
            stb.append("    AND t2.SCHREGNO = t3.SCHREGNO ");
            stb.append("    AND t1.GRADE = t3.GRADE ");
            stb.append("    AND t3.PREFECTURESCD || rtrim(char(t3.REDUCTIONMONEY"+_outputStr+")) "+pageSt+" ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + year + "' ");
            stb.append("    AND t1.SEMESTER = '" + semester + "' ");
            stb.append("    AND t1.GRADE = '" + grade + "' ");
            stb.append("    AND t1.HR_CLASS "+classSt+" ");
            stb.append("    AND t3.REDUC_DEC_FLG"+_outputStr+" = '1'");
            stb.append("GROUP BY ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS, ");
            stb.append("    t1.HR_NAME, ");
            stb.append("    t3.PREFECTURESCD, ");
            stb.append("    t3.REDUCTIONMONEY"+_outputStr+" ");
            stb.append("ORDER BY ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS, ");
            stb.append("    t3.PREFECTURESCD, ");
            stb.append("    t3.REDUCTIONMONEY"+_outputStr+" DESC ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("mainSql error!",e);
        }
        return stb.toString();

    }//mainSql()の括り

    /**
     *    学年/県/別軽減データを抽出
     *
     */
    private String headSql(String pageSt, String year)
    {
        StringBuffer stb = new StringBuffer();
        try {
            String commonSql = commonSql(year);
            stb.append(commonSql);

            stb.append("SELECT DISTINCT ");
            stb.append("    PREFECTURESCD || rtrim(char(REDUCTIONMONEY"+_outputStr+")) AS RETUKEY, ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    NAME1, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("FROM ");
            stb.append("    T_REDUCTION ");
            stb.append("    LEFT JOIN NAME_MST ON NAMECD1 = 'G202' ");
            stb.append("    AND NAMECD2 = PREFECTURESCD ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND PREFECTURESCD || rtrim(char(REDUCTIONMONEY"+_outputStr+")) "+pageSt+" ");
            stb.append("    AND REDUC_DEC_FLG"+_outputStr+" = '1'");
            stb.append("ORDER BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" DESC ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("headSql error!",e);
        }
        return stb.toString();

    }//headSql()の括り

    /**
     *    県/金額毎の合計
     *
     */
    private String prefCntSql(String pageSt, String year, String grade)
    {
        StringBuffer stb = new StringBuffer();
        try {
            String commonSql = commonSql(year);
            stb.append(commonSql);

            stb.append("SELECT ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+", ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append("FROM ");
            stb.append("    T_REDUCTION ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND GRADE = '" + grade + "' ");
            stb.append("    AND PREFECTURESCD || rtrim(char(REDUCTIONMONEY"+_outputStr+")) "+pageSt+" ");
            stb.append("    AND REDUC_DEC_FLG"+_outputStr+" = '1'");
            stb.append("GROUP BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" ");
            stb.append("ORDER BY ");
            stb.append("    PREFECTURESCD, ");
            stb.append("    REDUCTIONMONEY"+_outputStr+" DESC ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("prefCntSql error!",e);
        }
        return stb.toString();

    }//headSql()の括り

    /**
     *    県/金額毎の合計
     *
     */
    private String classCntSql(String year, String semester, String grade, String hr_class)
    {
        StringBuffer stb = new StringBuffer();
        try {
            String commonSql = commonSql(year);
            stb.append(commonSql);

            stb.append("SELECT ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS, ");
            stb.append("    t1.GRADE || t1.HR_CLASS AS CHANGEGYO, ");
            stb.append("    SUM(t3.REDUCTIONMONEY"+_outputStr+") AS REDUCTIONMONEY"+_outputStr+", ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT t1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.YEAR = t2.YEAR ");
            stb.append("    AND t1.SEMESTER = t2.SEMESTER ");
            stb.append("    AND t1.GRADE || t1.HR_CLASS = t2.GRADE || t2.HR_CLASS, ");
            stb.append("    T_REDUCTION t3 ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + year + "' ");
            stb.append("    AND t1.SEMESTER = '" + semester + "' ");
            stb.append("    AND t1.GRADE = '" + grade + "' ");
            stb.append("    AND t1.HR_CLASS = '" + hr_class + "' ");
            stb.append("    AND t1.YEAR = t3.YEAR ");
            stb.append("    AND t2.SCHREGNO = t3.SCHREGNO ");
            stb.append("    AND t1.GRADE = t3.GRADE ");
            stb.append("    AND t3.PREFECTURESCD IS NOT NULL ");
            stb.append("    AND t3.PREFECTURESCD != '' ");
            stb.append("    AND t3.REDUCTIONMONEY"+_outputStr+" IS NOT NULL ");
            stb.append("    AND t3.REDUCTIONMONEY"+_outputStr+" > 0 ");
            stb.append("    AND t3.REDUC_DEC_FLG"+_outputStr+" = '1'");
            stb.append("GROUP BY ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS ");
            stb.append("ORDER BY ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("classCntSql error!",e);
        }
        return stb.toString();

    }//headSql()の括り

    /**
     *    共通ＳＱＬ
     */
    private String commonSql(String year) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH T_REDUCTION AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.YEAR ");
            stb.append("       ,T1.SCHREGNO ");
            stb.append("       ,CASE WHEN T1.PREFECTURESCD IS NOT NULL AND T1.PREFECTURESCD != '' THEN T1.PREFECTURESCD ELSE T2.PREFECTURESCD END AS PREFECTURESCD ");
            stb.append("       ,T1.GRADE ");
            stb.append("       ,T1.REDUCTION_SEQ"+_outputStr+" ");
            stb.append("       ,T1.REDUCTIONMONEY"+_outputStr+" ");
            stb.append("       ,T1.REDUC_DEC_FLG"+_outputStr+" ");
            stb.append("       ,T1.REDUC_INCOME"+_outputStr+" ");
            stb.append("    FROM ");
            stb.append("        REDUCTION_DAT T1 ");
            stb.append("        LEFT JOIN ( ");
            stb.append("            SELECT ");
            stb.append("                ST2.SCHREGNO, ");
            stb.append("                SUBSTR(MIN(ST1.CITYCD),1,2) AS PREFECTURESCD ");
            stb.append("            FROM ");
            stb.append("                ZIPCD_MST ST1, ");
            stb.append("                GUARDIAN_DAT ST2 ");
            stb.append("            WHERE ");
            stb.append("                ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD ");
            stb.append("            GROUP BY ");
            stb.append("                ST2.SCHREGNO ");
            stb.append("        ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR = '" + year + "' ");
            stb.append("        AND (T1.PREFECTURESCD IS NOT NULL OR T2.PREFECTURESCD IS NOT NULL) ");
            stb.append("        AND T1.REDUCTIONMONEY"+_outputStr+" > 0 ");
            stb.append("        AND T1.REDUC_DEC_FLG"+_outputStr+" = '1' ");
            stb.append(" ) ");
        } catch( Exception e ){
            log.warn("commonSql error!",e);
        }
        return stb.toString();
    }

}//クラスの括り

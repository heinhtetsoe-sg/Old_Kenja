package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
 *学校教育システム 賢者 [入試管理]
 *
 *＜ＫＮＪＰ３９２＞  奨学生一覧表
 *
 *2006/03/22 m-yama 作成
 *2006/04/25 m-yama NO001 名称マスタ、Z006→G212に変更
 */

public class KNJP392Q {


    private static final Log log = LogFactory.getLog(KNJP392Q.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
            {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[11];

        //パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");
            param[1]  = request.getParameter("SEMESTER");
            param[2]  = request.getParameter("GRANT");
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();   //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());   //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


        //ＳＶＦ作成処理
        boolean nonedata = false; //該当データなしフラグ
        boolean none     = false; //該当データなしフラグ

        Map grantMap = getHeaderData(db2,svf,param);//ヘッダーデータ抽出メソッド

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力
        for (final Iterator it = grantMap.keySet().iterator(); it.hasNext();){
            final String key = (String) it.next();
            if ( printMain(db2,svf,param,key,(String) grantMap.get(key)) ) none = true;
            if ( none ) nonedata = true;
        }

        //該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();//DBを閉じる
        outstrm.close();//ストリームを閉じる

            }//doGetの括り


    /**ヘッダーデータを抽出*/
    private Map getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        //作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[3] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
        log.debug("heda OK!");
        //年度
        try {
            param[4] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        //交付区分
        Map grantMap = new TreeMap();
        try {
            PreparedStatement ps = null;
            String sql = "";
            if (param[2].equals("99")){
                //NO001
                sql = "SELECT DISTINCT GRANTCD,NAME1 FROM SCHREG_GRANT_DAT "
                        + "LEFT JOIN NAME_MST ON NAMECD1 = 'G212' AND NAMECD2 = GRANTCD "
                        + "WHERE YEAR = '"+param[0]+"' ORDER BY GRANTCD ";
            }else {
                //NO001
                sql = "SELECT DISTINCT GRANTCD,NAME1 FROM SCHREG_GRANT_DAT "
                        + "LEFT JOIN NAME_MST ON NAMECD1 = 'G212' AND NAMECD2 = GRANTCD "
                        + "WHERE YEAR = '"+param[0]+"' AND GRANTCD = '"+param[2]+"' ";
            }

            ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                grantMap.put(rs.getString("GRANTCD"),rs.getString("NAME1"));
            }
            psrsClose(ps,rs);
        } catch( Exception e ){
            log.warn("grant get error!",e);
        }

        return grantMap;

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String grantcd,String grantname)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(meisaiSql(param,grantcd));
            ResultSet rs = ps.executeQuery();

            //明細データをセット
            nonedata = printMeisai(svf,param,rs,grantname);

            psrsClose(ps,rs);
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    //ヘッダ印刷
    private void printHead(Vrw32alp svf,String param[],String pagecnt,String grantname)
    {
        try {
            svf.VrSetForm("KNJP392Q.frm", 1);
            svf.VrsOut("NENDO", param[4] );
            svf.VrsOut("SCHOLARSHIP", grantname );
            svf.VrsOut("DATE", param[3] );
            svf.VrsOut("PAGE", pagecnt );
        } catch( Exception ex ) {
            log.warn("printHead read error!",ex);
        }
    }

    /**明細データをセット*/
    private boolean printMeisai(Vrw32alp svf,String param[],ResultSet rs,String grantname)
    {
        boolean nonedata = false;
        int gyo = 1;
        int man = 0;
        int gil = 0;
        int pagecnt = 1;
        int lineNumber = 1;
        try {
            printHead(svf,param,String.valueOf(pagecnt),grantname);
            while( rs.next() ){

                if (gyo > 50){
                    svf.VrEndPage();
                    gyo = 1;
                    pagecnt++;
                    printHead(svf,param,String.valueOf(pagecnt),grantname);
                }
                svf.VrsOutn("NUMBER",gyo, String.valueOf(lineNumber) );
                svf.VrsOutn("HR_NAME",gyo, rs.getString("REGD") );
                svf.VrsOutn("NAME",gyo, rs.getString("NAME") );
                svf.VrsOutn("SEX",gyo, rs.getString("SEX") );
                if (null != rs.getString("SEX")){
                    gil++;
                }else {
                    man++;
                }

                gyo++;
                lineNumber++;
                nonedata = true;
            }
            if (gyo > 1){
                svf.VrsOut("TOTAL_MEMBER", "男"+String.valueOf(man)+"名　女"+String.valueOf(gil)+"名　計"+String.valueOf(man+gil)+"名");
                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

        return nonedata;

    }//printMeisai()の括り

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
     *明細データを抽出
     *
     */
    private String meisaiSql(String param[],String grantcd)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS REGD, ");
            stb.append("    T2.NAME, ");
            stb.append("    CASE WHEN T2.SEX = '2' THEN '*' ELSE NULL END AS SEX ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T1 ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.GRADE || T3.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            stb.append("    AND T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' ");
            stb.append("    AND T1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND T1.SCHREGNO IN (SELECT ");
            stb.append("                            SCHREGNO ");
            stb.append("                        FROM ");
            stb.append("                            SCHREG_GRANT_DAT ");
            stb.append("                        WHERE ");
            stb.append("                            YEAR = '"+param[0]+"' ");
            stb.append("                            AND GRANTCD = '"+grantcd+"') ");
            stb.append("ORDER BY ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("meisaiSql error!",e);
        }
        return stb.toString();

    }//meisaiSql()の括り

}//クラスの括り

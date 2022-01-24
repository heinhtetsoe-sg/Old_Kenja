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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３６０＞  入学試験志願者一覧表（入学志願者数確認リスト）
 **/

public class KNJL360 {

    private static final Log log = LogFactory.getLog(KNJL360.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[12];

        //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");         //年度
            param[1] = request.getParameter("CTRL_DATE");
            param[2] = request.getParameter("OUTPUT"); //1:実数を印字する
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if (setSvfMain(db2,svf,param)) nonedata = true; //帳票出力のメソッド

        //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //  終了処理
        ret = svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        int page
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut("TITLE","（入学志願者確認リスト）");
        //  年度と作成日(現在処理日)の取得
        try {
            String year = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";
            String ctrlDate = KNJ_EditDate.h_format_JP(param[1]);

            ret = svf.VrsOut("NENDO"     ,year);        //年度
            ret = svf.VrsOut( "DATE"     ,ctrlDate);    //作成日

            if (page == 2) {
                svf.VrsOut( "EXAM_NAME1", "実　数" );
            } else {
                //入試区分名称
                PreparedStatement ps = db2.prepareStatement(getTestName(param));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int num = Integer.parseInt(rs.getString("TESTDIV")) + 1; 
                    svf.VrsOut( "EXAM_NAME" + num, rs.getString("TEST_NAME") );
                }
                rs.close();
                db2.commit();
            }
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

    }

    /* 入試区分名称を得る */
    private String getTestName(String param[]){
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     TESTDIV AS TESTDIV, ");
        stb.append("     NAME AS TEST_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_TESTDIV_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '"+param[0]+"' ");
        stb.append(" ORDER BY ");
        stb.append("     TESTDIV ");

        return stb.toString();
    }

    /* 志願者数一覧を得る */
    private String preStat1(String param[]){
        StringBuffer stb = new StringBuffer();

        for(int i=0; i<=7; i++) {
            if (i>=1) {
                stb.append(" UNION ALL ");
            }
            stb.append(" SELECT ");
            if (i!=7) {
                stb.append("     T1.TESTDIV"+i+" AS TESTDIV, ");
            } else {
                stb.append("     cast(null as varchar(1)) AS TESTDIV, ");
                
            }
            stb.append("     T1.SEX, ");
            stb.append("     VALUE(T1.FS_AREA_CD,'08') AS FS_AREA_CD, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("     AND T1.APPLICANTDIV = '1' ");
            if (i!=7) {
                stb.append("     AND T1.TESTDIV"+i+" = '"+i+"' ");
            } 
            stb.append(" GROUP BY ");
            if (i!=7) {
                stb.append("     T1.TESTDIV"+i+", ");
            }
            stb.append("     GROUPING SETS((VALUE(T1.FS_AREA_CD,'08'), T1.SEX), ");
            stb.append("                   (VALUE(T1.FS_AREA_CD,'08')), ");
            stb.append("                   (T1.SEX), ");
            stb.append("                   ()) ");
            
        }
        stb.append(" ORDER BY ");
        stb.append("     TESTDIV, SEX, FS_AREA_CD ");

        return stb.toString();
    }
    
    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL360.frm", 1);  //セットフォーム
        try {
            int page = 1;
            setHeader(db2, svf, param, page);        //見出しメソッド

            nonedata = true;
            PreparedStatement ps = db2.prepareStatement(preStat1(param));
            ResultSet rs = ps.executeQuery();
            String sex = "-1";
            int kennai = 0, sexi = -1;
            while(rs.next()) {
                if( !(sex == null && rs.getString("SEX")==null || 
                        sex !=null && sex.equals(rs.getString("SEX"))) ){ // 性別が変わったら県内の志願者数を0にする
                    kennai = 0;
                    sex = rs.getString("SEX");
                    sexi = ( sex == null) ? 3 : Integer.parseInt( sex ) ;
                }
                // 入試区分=nullのとき実数
                int num = (null==rs.getString("TESTDIV")) ? 8 : Integer.parseInt(rs.getString("TESTDIV")) + 1; 

                // 実数を印字する
                if ("1".equals(param[2]) && num == 8) {
                    if (page == 1) {
                        if (nonedata) ret = svf.VrEndPage();
                        page = 2;
                        setHeader(db2, svf, param, page);        //見出しメソッド
                    }
                    num = 1; //実数は、２ページ１番目に印字する
                }

                String fsAreaCd = rs.getString("FS_AREA_CD");

                String count = rs.getString("COUNT");
                
                if( fsAreaCd == null ){
                    int iCount = Integer.parseInt(count);
                    ret = svf.VrsOutn( "TOTAL" + num + "_1", sexi, String.valueOf(iCount - kennai ) ); //県外合計
                    ret = svf.VrsOutn( "TOTAL" + num + "_2", sexi, String.valueOf(iCount) ); //総合計
                } else {
                    int areaCd = Integer.parseInt( fsAreaCd );
                    if( 1 == areaCd ) kennai = Integer.parseInt(count);  //県内
                    if( 10<= areaCd ) continue;
                    ret = svf.VrsOutn( "COUNT" + num + "_" + areaCd, sexi, count );
                }
                //log.debug(num+"_"+fsAreaCd+","+sexi+"="+count);
            }
            if (nonedata) ret = svf.VrEndPage();
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!",ex);
        }
        return nonedata;
    }

}//クラスの括り

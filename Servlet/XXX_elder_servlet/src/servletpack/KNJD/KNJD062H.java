// kanji=漢字
/*
 * $Id: 908d50dc81f50e98d2a601e5b59bd7d5e9570cd0 $
 *
 * 作成日: 2006/03/28
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;

import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [成績管理] 成績一覧 (広島国際用)
 *  2006/03/28 yamashiro 東京都版を複写して作成
 */

public class KNJD062H{

    private static final Log log = LogFactory.getLog(KNJD062H.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定


    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String param[] = new String[26];
        boolean nonedata = false;
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

    // print svf設定
        sd.setSvfInit( request, response, svf);
    // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
        // パラメータの取得
        getParam( request, param, db2 );
    // 印刷処理
        nonedata = printSvf( request, db2, svf, param );
    // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb(db2);
    }


    private boolean printSvf(HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[] ){

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        try {
            ret = svf.VrSetForm("KNJD062_2.frm", 4);
            String hrclass[] = request.getParameterValues("CLASS_SELECTED"); //学年・組  05/05/30
            KNJD062H_BASE obj = new KNJD062H_BASE( db2, svf, param );
            if( obj.printSvf( hrclass ) )nonedata = true;           //05/05/30Modify
        } catch( Exception ex ) {
            log.error("printSvf error!",ex);
        }
        return nonedata;
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     *       2005/01/05 最終更新日付を追加(param[15])
     *       2005/05/22 学年・組を配列で受け取る
     */
    private void getParam( HttpServletRequest request, String param[], DB2UDB db2 ){

        try {
            param[0] = request.getParameter("YEAR");                                    //年度
            param[1] = ( !(request.getParameter("SEMESTER").equals("4")) )?
                           request.getParameter("SEMESTER") : "9";                      //1-3:学期 9:学年末
            //param[2] = request.getParameter("GRADE_HR_CLASS").substring(0,2);         //学年・組 05/05/22
            param[2] = request.getParameter("GRADE");                                   //学年     05/05/30
            param[3] = request.getParameter("CLASS_SELECTED");                          //学年・組  05/05/22Modify
            param[8] = ( request.getParameter("OUTPUT4")!= null )?"1":"0";              //単位保留
            param[4] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //出欠集計日付

            if( request.getParameter("OUTPUT3")!=null )param[5] = "on";                 //遅刻を欠課に換算 null:無

            if( request.getParameter("TESTKINDCD") != null  &&  2 <= request.getParameter("TESTKINDCD").length() )
                param[11] = request.getParameter("TESTKINDCD").substring(0,2);          //テスト種別
            else
                param[11] = request.getParameter("TESTKINDCD");

            param[13] = request.getParameter("SEME_FLG");          //LOG-IN時の学期（現在学期）05/05/30
            if( request.getParameter("OUTPUT5") != null )
                param[19] = request.getParameter("OUTPUT5");       //欠番を詰める 05/06/07Build
            param[21] = request.getParameter("COUNTFLG");

            final String sql = KNJ_Testname.getTestNameSql(param[21], param[0], param[1], request.getParameter("TESTKINDCD"));
            db2.query(sql);
            ResultSet rs = null;
            rs = db2.getResultSet();
            while (rs.next()) {
                param[22] = rs.getString("TESTITEMNAME");
            }
            param[23] = request.getParameter("useCurriculumcd");
            param[24] = request.getParameter("useVirus");
            param[25] = request.getParameter("useKoudome");
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        }
//for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("[KNJD062H]param[" + i + "]=" + param[i]);
    }


}

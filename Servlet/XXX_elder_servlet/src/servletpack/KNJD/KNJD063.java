// kanji=漢字
/**
 * $Id: 9077b4e96de7d01a051be8728cede7d10880f45b $
 *    学校教育システム 賢者 [成績管理] 成績一覧
 *  2005/08/22 yamashiro
 */

package servletpack.KNJD;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJD063 {
    private static final Log log = LogFactory.getLog(KNJD063.class);

    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();     // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                // Databaseクラスを継承したクラス
        String param[] = new String[25];
        boolean nonedata = false;
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        //ＤＢ接続
        db2 = sd.setDb(request);
        if(sd.openDb(db2)){
            log.error("db open error");
            return;
        }

        //パラメータの取得
        getParam(request, param);

        //print svf設定
        sd.setSvfInit(request, response, svf);

        //印刷処理
        nonedata = printSvf(request, db2, svf, param);

        //終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private boolean printSvf(HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[]) {
        boolean nonedata = false;
        svf.VrSetForm("KNJD062.frm", 4);

        try {
            getParam2(db2, param);                   // 05/01/21

            String hrclass[] = request.getParameterValues("CLASS_SELECTED"); //学年・組  05/05/30
            KNJD063_BASE obj = null;
            if (param[11].equals("01")) {
                obj = new KNJD063_INTER( db2, svf, param );
            } else if(param[11].equals("02")) {
                obj = new KNJD063_TERM(  db2, svf, param );
            } else if (!param[1].equals("9")) {
                obj = new KNJD063_GAKKI( db2, svf, param );
            } else if (param[1].equals("9")) {
                obj = new KNJD063_GRADE( db2, svf, param );
            } else {
                nonedata = false;
            }

            if (obj.printSvf(hrclass)) {
                nonedata = true;           //05/05/30Modify
            }
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
    private void getParam(HttpServletRequest request, String param[]) {
        param[0] = request.getParameter("YEAR");    //年度

        // 1-3:学期 9:学年末
        final String semester = request.getParameter("SEMESTER");
        param[1] = !semester.equals("4") ? semester : "9";                         

        param[2] = request.getParameter("GRADE");   //学年     05/05/30
        param[3] = request.getParameter("CLASS_SELECTED");  //学年・組  05/05/22Modify
        param[8] = request.getParameter("OUTPUT4")!= null ? "1" : "0";  //単位保留
        param[4] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //出欠集計日付

        // 遅刻を欠課に換算 null:無
        if (request.getParameter("OUTPUT3") != null) {
            param[5] = "on";    
        }

        // テスト種別
        final String testKindCd = request.getParameter("TESTKINDCD");
        if (testKindCd != null  &&  2 <= testKindCd.length()) {
            param[11] = testKindCd.substring(0, 2);
        } else {
            param[11] = testKindCd;
        }

        param[13] = request.getParameter("SEME_FLG");          //LOG-IN時の学期（現在学期）05/05/30

        if (request.getParameter("OUTPUT5") != null) {
            param[19] = request.getParameter("OUTPUT5");       //欠番を詰める 05/06/07Build
        }
        param[21] = request.getParameter("useCurriculumcd");
        param[22] = request.getParameter("useVirus");
        param[23] = request.getParameter("useKoudome");
    }

    /** 
     *  パラメータセット 2005/01/29
     *      param[15]:attend_semes_datの最終集計日の翌日をセット
     *      param[16]:attend_semes_datの最終集計学期＋月をセット
     *  2005/02/20 Modify getDivideAttendDateクラスより取得
     */
    private void getParam2(final DB2UDB db2, final String param[]) {
        KNJDivideAttendDate obj = new KNJDivideAttendDate();
        try {
            obj.getDivideAttendDate(db2, param[0], param[1], param[4]);
            param[15] = obj.date;
            param[16] = obj.month;
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        log.debug("param[15]="+param[15]);
        log.debug("param[16]="+param[16]);
    }
}

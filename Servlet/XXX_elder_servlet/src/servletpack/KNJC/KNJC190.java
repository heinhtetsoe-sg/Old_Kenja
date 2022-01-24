/**
http://tokio/servlet/KNJC?DBNAME=w0611&PRGID=KNJC190&YEAR=2005&DATE1=2005/09/07&DATE2=2005/09/07&SUBCLASS_SELECTED=017001&OUTPUT=1
 *
 *    学校教育システム 賢者 [出欠管理]  出欠未入力講座チェックリスト
 *
 *    2005/10/20 yamashiro
 *  2005/10/22 yamashiro 教員別出力は、教員毎に改ページ
 *  2005/11/15 yamashiro 職員別出力の改ページの不具合を修正
 *  2005/12/02 yamashiro SVF-FIELDの初期化を追加
 *  2005/12/06 yamashiro 12/02の修正による不具合を修正
 *  2006/10/16 nakamoto  [NO001]
 *                       変更前：講座の正担当が複数登録した場合（TT授業等）、職員番号の一番若い担当のみ印刷される。
 *                       変更後：講座の正担当の職員に関しては全員出力するように変更した。
 */

package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJC190 {

    private static final Log log = LogFactory.getLog(KNJC190.class);
    
    private String _year;
    private String _date1;
    private String _date2;
    private String _output;
    private String _useCurriculumcd;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();         //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                    //Databaseクラスを継承したクラス
        boolean nonedata = false;             //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)){
            log.error("db open error");
            return;
        }

        // パラメータの取得
        getParam(db2, request);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // 印刷処理
        nonedata = printSvf(db2, svf, request);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     *
     */
    private void getParam(final DB2UDB db2, final HttpServletRequest request)
    {
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $ ");
        
        _year = request.getParameter("YEAR");
        _date1 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE1")); // 開始日付
        _date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2")); // 終了日付
        _output = request.getParameter("OUTPUT") == null ? "1" : request.getParameter("OUTPUT"); //1:科目 2:教員
        _useCurriculumcd = request.getParameter("useCurriculumcd");
    }


    /**
     *  印刷処理
     *
     */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final HttpServletRequest request)
    {
        boolean nonedata = false;                                 //該当データなしフラグ
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final List cselect = Arrays.asList(request.getParameterValues("SUBCLASS_SELECTED"));
            
            final String sql = prestatDetail();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            if (printsvfDetail(svf, rs, cselect)) {
                nonedata = true;                //SVF-FORM印刷
            }
        } catch (Exception ex) {
            log.error("error! ",ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**
     *  SVF-FORM  印刷処理
     */
    private boolean printsvfDetail(final Vrw32alp svf, final ResultSet rs, final List selectedSet)
    {
        boolean nonedata = false;
        String oldkey = "00";
        try {
            final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            final String dateFromTo = KNJ_EditDate.h_format_JP_MD(_date1) + " \uFF5E " + KNJ_EditDate.h_format_JP_MD(_date2);
            final String date = KNJ_EditDate.getNowDateWa(true);
            svf.VrSetForm("KNJC190.frm", 1);
            svf.VrsOut("NENDO",  nendo ); //年度
            svf.VrsOut("TERM",   dateFromTo);
            svf.VrsOut("DATE",   date); //作成日

            int line = 1;
            while (rs.next()) {
                
                final String key = "2".equals(_output) ? rs.getString("STAFFCD") : rs.getString("SUBCLASSCD");
                if (key == null || !selectedSet.contains(key)) {
                    continue;
                }
                
                //教員選択の場合、教員毎に改ページ 05/10/22
                if("2".equals(_output) && !"00".equals(oldkey) && !oldkey.equals(key) || line > 50) {
                    if (nonedata) {
                        svf.VrEndPage();
                        line = 1;
                        
                        svf.VrSetForm("KNJC190.frm", 1);
                        svf.VrsOut("NENDO",  nendo ); //年度
                        svf.VrsOut("TERM",   dateFromTo);
                        svf.VrsOut("DATE",   date); //作成日
                    }
                }
                svf.VrsOutn("SUBCLASS",      line, rs.getString("SUBCLASSNAME") );     //科目
                svf.VrsOutn("NAME",          line, rs.getString("STAFFNAME")    );     //教員名
                svf.VrsOutn("CHAIRCD",       line, rs.getString("CHAIRCD")      );     //講座コード
                svf.VrsOutn("CHAIRNAME",     line, rs.getString("CHAIRNAME")    );     //講座名
                svf.VrsOutn("EXECUTEDATE",   line, KNJ_EditDate.h_format_thi( rs.getString("EXECUTEDATE"), 1));  //年月日
                svf.VrsOutn("WEEK",          line, KNJ_EditDate.h_format_W( rs.getString("EXECUTEDATE")));     //曜日
                svf.VrsOutn("PERIOD",        line, rs.getString("PERIODCD")     );     //校時
                nonedata = true;
                line++;
                oldkey = key;
            }
            if (nonedata) {
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printsvfDetail read error!", ex);
        }
        return nonedata;
    }


    /**
     *   PrepareStatement作成
     *      テスト得点処理講座明細
     */
    private String prestatDetail()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ");
        //通常の正担任一人を抽出
        //通常の正担任全員を抽出---NO001
        stb.append("STAFF_A AS(");
        stb.append("   SELECT  T1.SEMESTER, T1.CHAIRCD, T1.STAFFCD ");
        stb.append("   FROM    CHAIR_STF_DAT T1 ");
        stb.append("   INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("   WHERE   T1.YEAR = '" + _year + "' ");
        stb.append("       AND T1.CHARGEDIV = 1 ");
        stb.append("       AND T1.SEMESTER BETWEEN (SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' AND '" + _date1 + "' BETWEEN SDATE AND EDATE) ");
        stb.append("                           AND (SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' AND '" + _date2 + "' BETWEEN SDATE AND EDATE) ");
        stb.append(") ");

        //時間割を基に該当する科目および教員を抽出
        stb.append(", DATA_A AS(");
        stb.append("   SELECT DISTINCT ");
        stb.append("           T1.EXECUTEDATE ");
        stb.append("         , T1.PERIODCD ");
        stb.append("         , ");
        if ("1".equals(_useCurriculumcd)) {
            stb.append("          T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD  || '-' || ");
        }
        stb.append("           T2.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("         , T2.CHAIRNAME, T2.CHAIRCD ");
        stb.append("         , CASE WHEN T3.STAFFCD IS NOT NULL THEN T3.STAFFCD ELSE T4.STAFFCD END AS STAFFCD ");
        stb.append("   FROM    SCH_CHR_DAT T1 ");
        stb.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = '" + _year + "' ");
        stb.append("                          AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("   LEFT JOIN SCH_STF_DAT T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("                           AND T3.PERIODCD = T1.PERIODCD AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("   LEFT JOIN STAFF_A T4 ON T4.SEMESTER = T2.SEMESTER AND T4.CHAIRCD = T2.CHAIRCD ");
        stb.append("   WHERE   T1.EXECUTEDATE BETWEEN '" + _date1 + "' AND '" + _date2 + "' ");
        stb.append("       AND VALUE(T1.EXECUTED,'0') <> '1' ");
        if ("2".equals(_output)) {
            stb.append("   AND (T3.STAFFCD IS NOT NULL OR T4.STAFFCD IS NOT NULL) ");
        }
        stb.append(") ");

        //メイン表
        stb.append("SELECT  T1.EXECUTEDATE, ");
        stb.append("        T4.NAME1 AS PERIODCD, ");
        stb.append("        T1.SUBCLASSCD, ");
        stb.append("        T1.STAFFCD, ");
        stb.append("        T1.CHAIRCD, ");
        stb.append("        T1.CHAIRNAME, ");
        stb.append("        T2.STAFFNAME, ");
        stb.append("        T3.SUBCLASSNAME ");
        stb.append("FROM    DATA_A T1 ");
        stb.append("LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        stb.append("LEFT JOIN SUBCLASS_MST T3 ON ");
        if ("1".equals(_useCurriculumcd)) {
            stb.append("          T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD  || '-' || ");
        }
        stb.append("   T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'B001' AND T4.NAMECD2 = T1.PERIODCD ");
        if ("1".equals(_output)) {
            stb.append("ORDER BY T1.SUBCLASSCD, T1.STAFFCD, T1.EXECUTEDATE ");
        } else {
            stb.append("ORDER BY T1.STAFFCD, T1.SUBCLASSCD, T1.EXECUTEDATE ");
        }
        return stb.toString();
    }
}

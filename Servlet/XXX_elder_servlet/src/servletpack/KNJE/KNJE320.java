package servletpack.KNJE;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import nao_package.KenjaProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *    学校教育システム 賢者 [進路情報]
 *
 *                    ＜ＫＮＪＥ３２０＞  校内選考資料（進路）
 *
 *
 * 2006/08/15 nakamoto 作成開始〜08/28
 */

public class KNJE320 {

    private static final Log log = LogFactory.getLog(KNJE320.class);
    KNJ_EditDate editdate = new KNJ_EditDate();

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[10];

        // パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                    // 年度
            param[1] = request.getParameter("GAKKI");                   // 学期
            param[3] = request.getParameter("SCHOOL_SORT");             // 種別         [NAME_MST:E001]
            param[4] = request.getParameter("SENKOU_KAI");              // 選考会       [NAME_MST:E003]
            param[2] = (4 < Integer.parseInt(param[3])) ? "1" : "0" ;   // 受験先種別   [0:学校 1:会社]
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        // print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        int ret = svf.VrInit();
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        // 該当データなしフラグ
        boolean nonedata = false;

        // 見出し項目
        setHead(db2,svf,param);

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        // 出力処理
        if (printMain(db2, svf, param)) nonedata = true;

log.debug("nonedata="+nonedata);

    //    該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //     終了処理
        ret = svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    /**
     * SVF-FORMセット＆見出し項目
     */
    private void setHead(DB2UDB db2, Vrw32alp svf, String param[]) {

        int ret = 0;

        // SVF-FORMセット
        if (param[2].equals("0")) ret = svf.VrSetForm("KNJE320_1.frm", 4);// 学校
        if (param[2].equals("1")) ret = svf.VrSetForm("KNJE320_2.frm", 4);// 会社

        // 年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get NENDO error!", ex );
        }

        // 作成日(現在処理日)
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = getinfo.Control(db2);
            param[6] = editdate.h_format_JP(returnval.val3);
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get TODAY error!", ex );
        }

        // 種別（学校・会社）
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'E001' AND NAMECD2 = '" + param[3] + "'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[7] = rs.getString("NAME1");
            }
            rs.close();
            db2.commit();
        } catch( Exception e ){
            log.error("ReturnVal setHead() get TITLE error!", e );
        }

    }


    /**
     * 出力処理（メイン）
     */
    private boolean printMain(DB2UDB db2, Vrw32alp svf, String param[])
    {
        boolean nonedata = false;
        int ret = 0;
        try {
log.debug("sqlList="+sqlList(param));
            int renban = 0;// 連番
            int line = 0;// 行カウント
            int pline = 0;// １ページ当りの最大行数
            if (param[2].equals("0"))   pline = 35;// 学校
            else                        pline = 50;// 会社
            PreparedStatement ps = db2.prepareStatement(sqlList(param));
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                // ヘッダ
                printHeader(svf, param);
                // 詳細
                line = printResult(svf, param, rs, ++renban, line, pline);

                nonedata = true;
            }
            rs.close();
            ps.close();
            db2.commit();
            if (nonedata == true) {
                if (line < pline) {
                    for ( ; line < pline ; ) {
                        ret = svf.VrsOut("SHIRO"    ,"0");
                        ret = svf.VrEndRecord();
//log.debug("line="+line);
                        line++;
                    }
                }
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;
    }


    /**
     * 出力処理（学校・会社）
     * 複数行分割処理==>進路先(1行15文字)・学部(1行8文字)・推薦基準(1行8文字)
     */
    private int printResult(
        Vrw32alp svf,
        String param[],
        ResultSet rs,
        int renban,
        int line,
        int pline
    ) {
        int ret = 0;
        try {
            // 複数行分割処理
            int maxnum = 0;// 1件当りの最大行数
            int curnum = 0;// 1件当りの現在行
            KNJ_EditEdit edit;
            Vector v_edit1,v_edit2,v_edit3;
            Enumeration e_edit1,e_edit2,e_edit3;
            // 進路先
            edit = new KNJ_EditEdit(rs.getString("SHINRO_SAKI"));
            v_edit1 = edit.get_token(30 ,5);
            maxnum = v_edit1.size();
            e_edit1 = v_edit1.elements();
            // 学部
            edit = new KNJ_EditEdit(rs.getString("GAKUBU"));
            v_edit2 = edit.get_token(16 ,5);
            int num2 = v_edit2.size();
            if (num2 > maxnum) maxnum = num2;
            e_edit2 = v_edit2.elements();
            // 推薦基準
            edit = new KNJ_EditEdit(rs.getString("SUISEN"));
            v_edit3 = edit.get_token(16 ,5);
            int num3 = v_edit3.size();
            if (num3 > maxnum) maxnum = num3;
            e_edit3 = v_edit3.elements();
            if (maxnum == 0) maxnum = 1;
            // 改ページ前の空行レコード ===> １ページ当りの最大行数 < (行カウント + 1件当りの最大行数)
            if (line < pline && pline < (line + maxnum)) {
                for ( ; line < pline ; ) {
                    ret = svf.VrsOut("BLANK"    ,"0");
                    ret = svf.VrEndRecord();
                    line++;
                }
            }
            // 件別１行目の設定
            int ia = 1;                         // 下線なし
            if (line == pline)          line = 0;   // 行カウント
            if ((curnum+1) == maxnum || line == (pline-1))   ia = 2;     // 下線あり

//log.debug("line1="+line);
            // ★出力１★
            printResultData(svf, param, rs, renban, ia);

            // ★出力２★件別複数行フィールドの出力
            for ( ; ; ) {
//log.debug("line2="+line);
                if (e_edit1.hasMoreElements()) 
                    ret = svf.VrsOut("STAT_NAME"+ia     ,   e_edit1.nextElement().toString() );
                // 出力項目（学校のみ）
                if (param[2].equals("0")) {
                    if (e_edit2.hasMoreElements()) 
                        ret = svf.VrsOut("BUNAME"+ia    ,   e_edit2.nextElement().toString() );
                    if (e_edit3.hasMoreElements()) 
                        ret = svf.VrsOut("RECOMMEND"+ia ,   e_edit3.nextElement().toString() );
                }
                ret = svf.VrEndRecord();// １レコード出力
                printSvfClear(svf, ia);// クリア
                curnum++;
                line++;
                ia = 1;                                 // 下線なし
                if (curnum >= maxnum)       break;      // for文から抜ける
                if (line == pline)          line = 0;   // 行カウント初期化
                if ((curnum+1) == maxnum || line == (pline-1))   ia = 2;     // 下線あり
            }
        } catch( Exception ex ) {
            log.warn("printResult read error!",ex);
        }
        return line;
    }


    /**
     * 出力処理（学校・会社）
     */
    private void printResultData(
        Vrw32alp svf,
        String param[],
        ResultSet rs,
        int renban,
        int ia
    ) {
        int ret = 0;
        try {
            // 出力項目（学校のみ）
            if (param[2].equals("0")) 
                ret = svf.VrsOut("JUKEN_HOWTO"+ia ,  rs.getString("JUKEN"));
            // 出力項目（共通）
            ret = svf.VrsOut("NUMBER"+ia      ,  String.valueOf(renban));
            ret = svf.VrsOut("COURSENO"+ia    ,  (param[2].equals("0")) ? rs.getString("SHINRO_NO_COLLEGE") : rs.getString("SHINRO_NO_COMPANY"));
            ret = svf.VrsOut("HR_CLASS"+ia    ,  rs.getString("HR_NAME"));
            ret = svf.VrsOut("ATTENDNO"+ia    ,  rs.getString("ATTENDNO"));
            ret = svf.VrsOut("NAME"+ia        ,  rs.getString("NAME_SHOW"));
            ret = svf.VrsOut("ABSENCE"+ia     ,  rs.getString("ABSENCE"));
            ret = svf.VrsOut("AVERAGE"+ia     ,  rs.getString("AVG"));
            ret = svf.VrsOut("TOTAL"+ia       ,  rs.getString("SEISEKI"));
            ret = svf.VrsOut("SELECTION"+ia   ,  rs.getString("SENKOU_KAI"));
            ret = svf.VrsOut("RESULT"+ia      ,  rs.getString("SENKOU_KEKKA"));
        } catch( Exception ex ) {
            log.warn("printResultData read error!",ex);
        }
    }


    /**
     * クリア処理（学校・会社）
     */
    private void printSvfClear(Vrw32alp svf, int ia)
    {
        int ret = 0;
        try {
            ret = svf.VrsOut("JUKEN_HOWTO"+ia ,  "");
            ret = svf.VrsOut("NUMBER"+ia      ,  "");
            ret = svf.VrsOut("COURSENO"+ia    ,  "");
            ret = svf.VrsOut("HR_CLASS"+ia    ,  "");
            ret = svf.VrsOut("ATTENDNO"+ia    ,  "");
            ret = svf.VrsOut("NAME"+ia        ,  "");
            ret = svf.VrsOut("ABSENCE"+ia     ,  "");
            ret = svf.VrsOut("AVERAGE"+ia     ,  "");
            ret = svf.VrsOut("TOTAL"+ia       ,  "");
            ret = svf.VrsOut("SELECTION"+ia   ,  "");
            ret = svf.VrsOut("RESULT"+ia      ,  "");
        } catch( Exception ex ) {
            log.warn("printSvfClear read error!",ex);
        }
    }


    /**
     * 出力処理（ヘッダ）
     */
    private void printHeader(Vrw32alp svf, String param[])
    {
        int ret = 0;
        try {
            ret = svf.VrsOut("NENDO"        , param[5]);
            ret = svf.VrsOut("DATE"         , param[6]);
            ret = svf.VrsOut("SCHOOL_SORT"  , param[7]);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }
    }


    /**
     * 詳細リスト（学校・会社）
     */
    private String sqlList(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            // 校内選考情報
            stb.append("WITH AFT_GRAD_COURSE AS ( ");
            stb.append("    SELECT SCHREGNO, STAT_CD, JUKEN_HOWTO, SENKOU_NO, STAT_NAME, ");
            stb.append("           BUNAME, RECOMMEND, AVG, SEISEKI, SENKOU_KAI, SENKOU_FIN ");
            stb.append("      FROM AFT_GRAD_COURSE_DAT ");
            stb.append("     WHERE YEAR = '" + param[0] + "' ");
            stb.append("       AND STAT_KIND = '1' ");
            stb.append("       AND SENKOU_KIND = '" + param[2] + "' ");
            stb.append("       AND SCHOOL_SORT = '" + param[3] + "' ");
            if (param[4] != null && !param[4].equals("")) 
                stb.append("   AND SENKOU_KAI = '" + param[4] + "' ");
            stb.append("    ) ");
            // SCHNOにて使用---生徒のMAX年度・学期 ★在
            stb.append(",UNGRD_MAX_SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO, ");
            stb.append("           MAX(YEAR || SEMESTER) AS MAX_YEAR_SEMESTER ");
            stb.append("      FROM SCHREG_REGD_DAT W1 ");
            stb.append("     WHERE EXISTS(SELECT 'X' ");
            stb.append("                    FROM AFT_GRAD_COURSE X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO) ");
            stb.append("       AND W1.YEAR = '" + param[0] + "' ");
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("    ) ");
            // SCHNOにて使用---生徒のMAX年度・学期 ★卒
            stb.append(",GRD_MAX_SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO, ");
            stb.append("           MAX(YEAR || SEMESTER) AS MAX_YEAR_SEMESTER ");
            stb.append("      FROM GRD_REGD_DAT W1 ");
            stb.append("     WHERE EXISTS(SELECT 'X' ");
            stb.append("                    FROM AFT_GRAD_COURSE X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO) ");
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("    ) ");
            // 学籍情報
            stb.append(",SCHNO AS ( ");
            stb.append("    SELECT W1.SCHREGNO, W2.HR_NAME, W1.HR_CLASS, W1.ATTENDNO, W3.NAME_SHOW ");
            stb.append("      FROM SCHREG_REGD_DAT W1 ");
            stb.append("           LEFT JOIN SCHREG_REGD_HDAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("                                        AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("                                        AND W2.GRADE = W1.GRADE ");
            stb.append("                                        AND W2.HR_CLASS = W1.HR_CLASS, ");
            stb.append("           SCHREG_BASE_MST W3 ");
            stb.append("     WHERE W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND EXISTS(SELECT 'X' ");
            stb.append("                    FROM UNGRD_MAX_SCHNO X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO ");
            stb.append("                     AND X1.MAX_YEAR_SEMESTER = W1.YEAR || W1.SEMESTER) ");
            stb.append("       AND NOT EXISTS(SELECT 'X' ");
            stb.append("                    FROM GRD_MAX_SCHNO X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO) ");
            stb.append("    UNION ");
            stb.append("    SELECT W1.SCHREGNO, W2.HR_NAME, W1.HR_CLASS, W1.ATTENDNO, W3.NAME_SHOW ");
            stb.append("      FROM GRD_REGD_DAT W1 ");
            stb.append("           LEFT JOIN GRD_REGD_HDAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("                                        AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("                                        AND W2.GRADE = W1.GRADE ");
            stb.append("                                        AND W2.HR_CLASS = W1.HR_CLASS, ");
            stb.append("           GRD_BASE_MST W3 ");
            stb.append("     WHERE W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND EXISTS(SELECT 'X' ");
            stb.append("                    FROM GRD_MAX_SCHNO X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO ");
            stb.append("                     AND X1.MAX_YEAR_SEMESTER = W1.YEAR || W1.SEMESTER) ");
            stb.append("    ) ");
            // 欠席
            stb.append(",ABSENCE_SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO, ");
            stb.append("           SUM(VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0)) AS ABSENCE ");
            stb.append("      FROM SCHREG_ATTENDREC_DAT W1 ");
            stb.append("     WHERE EXISTS(SELECT 'X' ");
            stb.append("                    FROM AFT_GRAD_COURSE X1 ");
            stb.append("                   WHERE X1.SCHREGNO = W1.SCHREGNO) ");
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("    ) ");

            // メイン
            stb.append("SELECT T1.HR_NAME, smallint(T1.ATTENDNO) AS ATTENDNO, T1.NAME_SHOW, ");
            stb.append("       A1.ABSENCE, ");
            stb.append("       T2.STAT_CD   AS SHINRO_NO_COLLEGE, ");
            stb.append("       T2.SENKOU_NO AS SHINRO_NO_COMPANY, ");
            stb.append("       T2.STAT_NAME AS SHINRO_SAKI, ");
            stb.append("       T2.BUNAME AS GAKUBU, ");
            stb.append("       T2.RECOMMEND AS SUISEN, ");
            stb.append("       T2.AVG, ");
            stb.append("       T2.SEISEKI, ");
            stb.append("       N1.NAME1 AS JUKEN, ");
            stb.append("       N2.NAME1 AS SENKOU_KAI, ");
            stb.append("       N3.NAME1 AS SENKOU_KEKKA ");
            stb.append("FROM SCHNO T1 ");
            stb.append("     LEFT JOIN ABSENCE_SCHNO A1 ON A1.SCHREGNO = T1.SCHREGNO, ");
            stb.append("     AFT_GRAD_COURSE T2 ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E002' AND N1.NAMECD2 = T2.JUKEN_HOWTO ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'E003' AND N2.NAMECD2 = T2.SENKOU_KAI ");
            stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'E004' AND N3.NAMECD2 = T2.SENKOU_FIN ");
            stb.append("WHERE T1.SCHREGNO = T2.SCHREGNO ");
            if (param[2].equals("1")) {
                stb.append("ORDER BY T2.SENKOU_KAI, T2.SENKOU_NO ");
            } else {
                stb.append("ORDER BY T2.SENKOU_KAI, T2.JUKEN_HOWTO, T2.STAT_CD, T2.BUNAME, T1.HR_CLASS, T1.ATTENDNO ");
            }
        } catch( Exception e ){
            log.warn("sqlList error!",e);
        }
        return stb.toString();
    }

}//クラスの括り

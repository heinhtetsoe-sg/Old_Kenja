// kanji=漢字
/*
 * $Id: 5138934811614eb0ede7fb5622fbae577e617d5f $
 *
 * 作成日: 2004/05/31
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＧ１３０＞  クラス別出欠状況リスト
 *
 *  2004/05/31 yamashiro・新規作成
 *  2004/06/25 yamashiro・CHAIR_STD_DATの抽出条件に開始、終了日付を追加
 *  2004/08/18 yamashiro・講座クラスデータの同時展開の講座コードゼロに対応
 *  2005/04/19 yamashiro・１６校時対応
 *  2005/05/18 yamasihro・処理が遅い不具合を修正 => SQL hiro    tokio:05/10/31
 *  2005/10/31 yamashiro・出席番号の追加表示
 */

public class KNJC130
{

    private static final Log log = LogFactory.getLog(KNJC130.class);
    Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                     //Databaseクラスを継承したクラス
    boolean nonedata;               //該当データなしフラグ
    private final int maxlinenum = 45;  //ページ行数

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        // パラメータの取得
        String hrclass[] = request.getParameterValues("category_name");     //年・組

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                    //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJC130]DB2 open error!", ex);
        }

        // ＳＶＦ作成処理
        if (log.isDebugEnabled()) { KNJServletUtils.debugParam(request, log); }
        Param param = new Param(db2, request);
        nonedata = false;
        svf.VrSetForm("KNJC130.frm", 4);
        svf.VrAttribute("TARGET_CLASS","FF=1");       //対象クラスで改ページ
        printsvfMain(param,hrclass);                                    //明細出力

        // 該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        try {
            db2.commit();
            db2.close();                        //DBを閉じる
            outstrm.close();                    //ストリームを閉じる
        } catch( Exception e ){
            log.error("[KNJC130]close error!", e );
        }
    }



    /*
     *  印刷処理
     */
    private void printsvfMain(
            final Param param,
            final String hrclass[]
    ) {
        KNJPeriodMap mobj = null;
        try {
            mobj = new KNJPeriodMap();
            mobj.PeriodMap( db2 );
        } catch( Exception ex ) {
            log.error("KNJPeriodMap error! ", ex);
        }

        svf.VrsOut("NENDO",KNJ_EditDate.h_format_JP_N(param._year+""+"-04-01")+"度");    //年度
        svf.VrsOut("TARGET_DATE",KNJ_EditDate.h_format_JP(param._date));                 //対象日付

        for (int i = 0; i < hrclass.length; i++) { printDetail(param, mobj, hrclass[i]); }
    }

    /*
     * @param param
     * @param mobj
     */
    private void printDetail(
            final Param param,
            final KNJPeriodMap mobj,
            final String hrclass
    ) {
        String schno = "0";         //学籍番号保存用
        String g_hr = "0";          //学年組保存用
        int period_min = 0;         //時間割における最初の校時（生徒ごと）
        int period_max = 0;         //時間割における最後の校時（生徒ごと）
        boolean gh_out = false;     //ページ見出し出力フラグ
        boolean sch_out = false;    //生徒学生情報主力フラグ
        boolean l_out = false;      //区切り線出力フラグ
        int l_cnt = 0;              //行カウント

        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            final String pre_sql = pre_sql(param);
            ps1 = db2.prepareStatement(pre_sql);
            ps1.setString(1, hrclass);
            ps1.setString(2, hrclass);
            rs = ps1.executeQuery();

            while( rs.next() ){
                //対象クラスのブレイク
                if( !g_hr.equals(rs.getString("GRADE_HRCLASS")) ){
                    g_hr = rs.getString("GRADE_HRCLASS");
                    gh_out = true;      //ページ見出し出力フラグをオンにする
                    l_out = false;      //区切り線出力フラグをオフにする
                    l_cnt = 0;
                }
                //学籍番号のブレイク
                if( !schno.equals(rs.getString("SCH2")) ){
                    schno = rs.getString("SCH2");
                    //period_min = Integer.parseInt(rs.getString("PERIODCD"));  //時間割における最初の校時（生徒ごと）
                    period_min = retPeriodValue( mobj.periodnumbermap, rs.getString("PERIODCD") );  //時間割における最初の校時（生徒ごと）
                    sch_out = true;     //生徒学生情報主力フラグをオンにする
                    l_out = true;       //区切り線出力フラグをオンにする
                }
                //明細(空き時間)の出力
                //period_max = Integer.parseInt(rs.getString("PERIODCD"));      //時間割における校時（生徒ごと）
                period_max = retPeriodValue( mobj.periodnumbermap, rs.getString("PERIODCD") );      //時間割における校時（生徒ごと）
                for( ; period_min <= period_max ; period_min++ ){
                    if( period_min == period_max  &&  rs.getString("SCHREGNO") == null )break;  //出席は出力なし！
                    //対象クラスのブレイク
                    if( gh_out ){
                        svf.VrsOut("TARGET_CLASS",rs.getString("HR_NAME"));   //対象クラス
                        gh_out = false;     //ページ見出し出力フラグをオフにする
                    }
                    //学籍番号のブレイク
                    if( sch_out ){
                        //区切り線の出力
                        if( l_out ){
                            if( l_cnt==maxlinenum-1 ){                      //区切り線がページ最後になる場合はブランクを出力する
                                svf.VrsOut("SPACE","ABC");            //空白行
                                svf.VrEndRecord();
                                l_cnt = 0;
                            }
                            if( l_cnt>0 ){                                  //ページ最初以外、生徒の区切り線を出力する
                                svf.VrsOut("HAIHUN","ABC");           //区切り線
                                svf.VrEndRecord();
                                l_cnt++;
                                if( l_cnt>=maxlinenum )l_cnt=0;
                            }
                            l_out = false;                                  //区切り線出力フラグをオフにする
                        }
                        svf.VrsOut("SCHREGNO",schno);                 //学籍番号
                        svf.VrsOut("NAME",rs.getString("NAME"));      //生徒氏名
                        svf.VrsOut("ATTENDNO",rs.getString("ATTENDNO"));  //出席番号 05/10/31
                        sch_out = false;                                    //生徒学生情報主力フラグをオフにする
                    }
                    svf.VrsOut("PERIOD", retPeriodMeisyoValue( mobj.periodmeishomap, period_min ) );

                    svf.VrsOut("SITUATION",( (period_min)==period_max )?
                                                    ( rs.getString("DI_NAME")!=null )?
                                                        rs.getString("DI_NAME"):"":"");             //状況
                    svf.VrsOut("CHAIRNAME",( (period_min)==period_max )?
                                                    ( rs.getString("CHAIRNAME")!=null )?
                                                        rs.getString("CHAIRNAME"):"":"");           //講座名
                    svf.VrsOut("STAFFNAME",( (period_min)==period_max )?
                                                    ( rs.getString("STAFFNAME")!=null )?
                                                        rs.getString("STAFFNAME"):"":"");           //担当
                    svf.VrEndRecord();
                    l_cnt++;
                    if( maxlinenum <= l_cnt ) l_cnt = 0;
                    nonedata = true;
                }
                period_min = period_max + 1;        //時間割における校時（生徒ごと）
            }
        } catch( Exception ex ) {
            log.error("[KNJC130]printsvfMain read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps1, rs);
        }
    }


    /*
     *  SVF-FORM 校時コードを数値変換
     */
    private int retPeriodValue(
            final Map periodmap,
            final String periodcd
    ) {
        int retval = 0;
        try {
            retval = ( (Integer)periodmap.get( periodcd ) ).intValue();
        } catch( Exception ex ){
            log.warn("periodmap get error! ",ex);
        }
        return retval;
    }


    /*
     *  SVF-FORM 校時コードを名称変換
     */
    private String retPeriodMeisyoValue(
            final Map periodmap,
            final int periodcd
    ) {
        String retval = null;
        try {
            retval = (String)periodmap.get( new Integer(periodcd) );
        } catch( Exception ex ){
            log.warn("periodmap get error! ",ex);
        } finally{
            if( retval == null ) retval = "";
        }
        return retval;
    }


    /*------------------------*
     * PrepareStatement SQL
     *------------------------*/
    private String pre_sql(final Param param) {

        StringBuffer stb = new StringBuffer();
        //対象クラスの取得
        stb.append("WITH HRCLASS AS(");
        stb.append(    "SELECT  GRADE, HR_CLASS, HR_NAME ");
        stb.append(    "FROM    SCHREG_REGD_HDAT ");
        stb.append(    "WHERE   YEAR = '" + param._year + "' AND ");
        stb.append(            "SEMESTER = '" + param._semester + "' AND ");
        stb.append(            "GRADE||HR_CLASS =? ) ");

        //メイン表
        stb.append("SELECT  W9.SCHREGNO AS SCH2, W9.PERIODCD, ");
        stb.append(        "W8.GRADE||W8.HR_CLASS AS GRADE_HRCLASS, W8.HR_NAME, ");
        stb.append(        "W1.SCHREGNO, W3.NAME, W4.DI_NAME1 AS DI_NAME, W5.CHAIRNAME, W7.STAFFNAME ");
        stb.append(       ",W2.ATTENDNO ");

        stb.append("FROM   (SELECT  W3.SCHREGNO, PERIODCD, ATTESTOR ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(     ", W2.SCHOOL_KIND, W2.CURRICULUM_CD ");
        }
        stb.append(        "FROM    SCH_CHR_DAT W1, ");
        stb.append(               "(SELECT  K1.SEMESTER, K2.CHAIRCD, K2.GROUPCD, K2.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            ", K2.SCHOOL_KIND, K2.CURRICULUM_CD ");
        }
        stb.append(                "FROM    CHAIR_CLS_DAT K1, ");
        stb.append(                        "CHAIR_DAT K2 ");
        stb.append(                "WHERE   K1.YEAR = '" + param._year + "' AND ");
        stb.append(                        "K2.YEAR = '" + param._year + "' AND ");
        stb.append(                        "K1.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                        "K2.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                        "(K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) AND ");
        stb.append(                        "K1.GROUPCD = K2.GROUPCD AND ");
        stb.append(                        "EXISTS(SELECT  'X' FROM HRCLASS W5 ");
        stb.append(                               "WHERE   W5.GRADE = K1.TRGTGRADE AND ");
        stb.append(                                       "W5.HR_CLASS = K1.TRGTCLASS) ");
        stb.append(                ")W2, ");
        stb.append(                "CHAIR_STD_DAT W3 ");
        stb.append(        "WHERE   W1.EXECUTEDATE = '" + param._date + "' AND ");
        stb.append(                "W1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(                "W3.YEAR = '" + param._year + "' AND ");
        stb.append(                "W3.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                "W3.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(                "W3.APPDATE <= '" + param._date + "' AND ");
        stb.append(                "'" + param._date + "' <= W3.APPENDDATE AND ");
        stb.append(                "EXISTS(SELECT  'X' FROM SCHREG_REGD_DAT W4 ");
        stb.append(                       "WHERE   W4.YEAR = '" + param._year + "' AND ");
        stb.append(                               "W4.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                               "W4.GRADE||W4.HR_CLASS = ? AND ");
        stb.append(                               "W4.SCHREGNO = W3.SCHREGNO)");
        stb.append(        ")W9 ");

        stb.append(        "INNER JOIN SCHREG_REGD_DAT W2 ON W2.YEAR = '" + param._year + "' AND ");
        stb.append(                                         "W2.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                                         "W2.SCHREGNO = W9.SCHREGNO ");
        stb.append(        "LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W2.SCHREGNO ");
        stb.append(        "INNER JOIN HRCLASS W8 ON W8.GRADE = W2.GRADE AND ");
        stb.append(                                 "W8.HR_CLASS = W2.HR_CLASS ");

        stb.append(        "LEFT JOIN ATTEND_DAT W1 ON W1.SCHREGNO = W9.SCHREGNO AND ");
        stb.append(                                   "W1.ATTENDDATE = '" + param._date + "' AND ");
        stb.append(                                   "W1.PERIODCD = W9.PERIODCD ");
        stb.append(        "LEFT JOIN CHAIR_DAT W5 ON W5.YEAR = '" + param._year + "'AND ");
        stb.append(                                  "W5.SEMESTER = '" + param._semester + "' AND ");
        stb.append(                                  "W5.CHAIRCD = W1.CHAIRCD ");
        stb.append(        "LEFT JOIN ATTEND_DI_CD_DAT W4 ON W4.YEAR = W1.YEAR AND ");
        stb.append(                                 "W4.DI_CD = W1.DI_CD ");
        stb.append(        "LEFT JOIN STAFF_MST W7 ON W7.STAFFCD = W9.ATTESTOR ");

        stb.append("ORDER BY W2.GRADE, W2.HR_CLASS, W9.SCHREGNO, W9.PERIODCD ");

        return stb.toString();
    }

    static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                            //年度
            _semester = request.getParameter("SEMESTER");                        //学期
            _date = KNJ_EditDate.h_format_sec_2(request.getParameter("DATE"));   //処理日
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
    }
}

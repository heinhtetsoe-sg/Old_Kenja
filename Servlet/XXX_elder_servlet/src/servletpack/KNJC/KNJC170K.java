// kanji=漢字
/*
 * $Id: 2a94e33f279bd9f4a8a070851e1583315d044cf0 $
 *
 * 作成日: 2005/04/05
 * 作成者: yamashiro
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *    学校教育システム 賢者 [出欠管理] 出席状況一覧
 *
 *    2005/04/05 yamashiro
 *  2005/05/16 yamashiro 中学は出欠集計期間を１日〜月末とする
 *  2005/05/19 yamashiro 作成日付を追加
 *  2005/10/08 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                       留学・休学・不在日数の算出において、ATTEND_SEMES_DATもみる。
 *  2005/10/19 yamashiro 長期欠席者の明細を追加出力
 *  2006/02/28 yamashiro 長期欠席者の明細は、指定した月だけで(5・10・15)日以上を休んだ生徒を対象とする --NO010
 *                       また、１行８０桁を１２４桁に変更 --NO010
 */

package servletpack.KNJC;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJAttendTerm;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJC170K {

    private static final Log log = LogFactory.getLog(KNJC170K.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private DecimalFormat dmf1 = new DecimalFormat("00");
    private String frommonth[];   //出力対象月 04/12/14
    private String tomonth[];     //出力対象月 04/12/14
    private    PreparedStatement ps1, ps2, ps3;
    private boolean nonedata;
    private String _useVirus;
    private String _useKoudome;
    private boolean _hasSchChrDatExecutediv;

    // 学校マスタ参照
    private KNJSchoolMst _knjSchoolMst;
    
    private String _param0;
    private String _param3;
    private String _param4;
    private String _param5;
    private String _param6;
    private String _param7;
    private String _param8;
    private String _param9;

    /**
     *
     *  KNJC.classから最初に起動されるクラス
     *
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();     // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                // Databaseクラスを継承したクラス

    // パラメータの取得
        /** 
         *  get parameter doGet()パラメータ受け取り 
         *            YEAR           年度 2004 
         *            GAKKI          学期 3 
         *            GRADE          対象学年の配列 01,02,03
         *            OUTPUT         1:長欠者要注意者リスト  2:欠課時数要注意者リスト
         *            DAYS           月XX以上 10
         *            NENGETSU_FROM  印刷範囲from 2005-01 
         *            NENGETSU_TO    印刷範囲to   2005-03 
         */
        try {
            _param0 = request.getParameter("YEAR");                         //年度
            _param3 = _param0 + "-04";                                    //印刷範囲年月FROM
            _param4 = request.getParameter("NENGETSU_TO");                  //印刷範囲年月TO
            _param5 = request.getParameter("DAYS");                        //対象日数
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        log.debug("$Revision: 56595 $");
        _useVirus = request.getParameter("useVirus");
        _useKoudome = request.getParameter("useKoudome");
        
    // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                            //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定
         } catch (java.io.IOException ex) {
            log.error("db new error:", ex);
        }
    // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME")    , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) db2.close();
            return;
        }
        
        /** 
         *  パラメータセット 2005/01/29
         *      _param6:attend_semes_datの最終集計日の翌日をセット
         *      _param7:attend_semes_datの最終集計学期＋月をセット
         *      _param8:集計日をセット NULLの場合は学期終了日
         */
        final KNJDivideAttendDate obj = new KNJDivideAttendDate();
        try {
            obj.getDivideAttendDate(db2, _param0, "3", null);
            _param6 = obj.date;
            _param7 = obj.month;
            _param8 = obj.enddate;
log.debug("_param7 = " + _param7);
        } catch (Exception ex) {
            log.error("error! ",ex);
        }
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, _param0);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }
        _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");

    // 印刷処理
        printSvf(db2, svf);
    // 終了処理
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
           svf.VrsOut("note" , "note");
           svf.VrEndPage();
       }
       svf.VrQuit();
        try {
            db2.commit();
            db2.close();
        } catch (Exception ex) {
            log.error("db close error!", ex);
        }//try-cathの括り
    }
    
    private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT 1 FROM ");
        if (StringUtils.isBlank(colname)) {
            stb.append("SYSCAT.TABLES");
        } else {
            stb.append("SYSCAT.COLUMNS");
        }
        stb.append(" WHERE TABNAME = '" + tabname + "' ");
        if (!StringUtils.isBlank(colname)) {
            stb.append(" AND COLNAME = '" + colname + "' ");
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasTableColumn = false;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                hasTableColumn = true;
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
        return hasTableColumn;
    }

    /**
     *
     *  印刷処理
     *
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf) {
        try {
            setHead(db2, svf);                    //見出し出力のメソッド
            printSvfMain(db2, svf);               //統計データ出力のメソッド
        } catch (Exception ex) {
            log.error("printSvf error!", ex);
        }
    }


    /** 
     *  見出し項目等 
     */
    private void setHead(final DB2UDB db2, final Vrw32alp svf) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        svf.VrSetForm("KNJC170.frm", 4);                //共通フォーム
        svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param0)) + "年度");//年度

    //    作成日(現在処理日)の取得 05/05/19Rivive
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    //  対象月を保存 04/12/14
        setTargetMonth(db2);

        getinfo = null;
        returnval = null;

    }//setHead()の括り


    /** 
     *  対象月の開始日と終了日を配列にセットする処理
     *    ４月を配列要素０とし、８月は除外する
     *    2005/05/16 yamashiro 中学は出欠集計期間を１日〜月末とする
     */
    private void setTargetMonth(final DB2UDB db2) {
        final Calendar cals = Calendar.getInstance();
        final Calendar cale = Calendar.getInstance();

        final List arr1 = new ArrayList(); 
        final List arr2 = new ArrayList(); 
        final KNJAttendTerm obj = new KNJAttendTerm();    //05/05/16

        try {
            //ps2 = db2.prepareStatement(prestatementSemesterDate());  //学期の開始日・終了日取得

            cals.setTime(sdf.parse(_param3 + "-01"));    //開始日付
            cale.setTime(sdf.parse(_param4 + "-01"));    //終了日付
            sdf.applyPattern("yyyy-MM");
            while (!cals.after(cale)) {

                //出欠集計の月別集計範囲日付を取得 05/05/13
                obj.setMonthTermDateK(db2, _param0, cals.get(Calendar.MONTH) + 1, true);
                arr1.add(new String(obj.sdate));
                arr2.add(new String(obj.edate));
                cals.add(Calendar.MONTH,1);
/*
                //各月の集計範囲fromをセット
                if (cals.get(Calendar.MONTH) + 1 == 4)      
                        arr1.add(new String(setSemesterDate(db2, param, 1, "SDATE")));
                else if (cals.get(Calendar.MONTH) + 1 == 9) 
                        arr1.add(new String(setSemesterDate(db2, param, 2, "SDATE")));
                else if (cals.get(Calendar.MONTH) + 1 == 1) 
                        arr1.add(new String(setSemesterDate(db2, param, 3, "SDATE")));
                else   
                        arr1.add(new String(sdf.format(cals.getTime()) + "-02"));
                //各月の集計範囲toをセット
                cals.add(Calendar.MONTH,1);
                if (cals.get(Calendar.MONTH) == 8)      
                        arr2.add(new String(setSemesterDate(db2, param, 1, "EDATE")));
                else if (cals.get(Calendar.MONTH) == 12) 
                        arr2.add(new String(setSemesterDate(db2, param, 2, "EDATE")));
                else if (cals.get(Calendar.MONTH) == 3) 
                        arr2.add(new String(setSemesterDate(db2, param, 3, "EDATE")));
                else  
                        arr2.add(new String(sdf.format(cals.getTime()) + "-01"));
*/
            }
            frommonth = new String[arr1.size()];
            tomonth = new String[arr2.size()];
            for (int i = 0; i < arr1.size(); i++) frommonth[i] = (String) arr1.get(i);
            for (int i = 0; i < arr2.size(); i++) tomonth[i] = (String) arr2.get(i);
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(ps2);
            ps2 = null;
        }
for (int i = 0; i < frommonth.length; i++) log.debug("frommonth[" + i + "]=" + frommonth[i] + "  tomonth[" + i + "]=" + tomonth[ i ]);
    }


    /** 
     *  学期の開始日・終了日取得
     */
    private String setSemesterDate(final DB2UDB db2, int semester, String field) {
        ResultSet rs = null;
        String strdate = null;
        try {
            ps2.setString(1, _param0);                        //年度
            ps2.setString(2, String.valueOf(semester));        //学期
            rs = ps2.executeQuery();
            if (rs.next()) 
                if (field.equals("SDATE")) strdate = rs.getString(field);
                else                        strdate = rs.getString(field);
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return strdate;
    }


    /** 
     *  印刷処理 
     *    学年別月別に統計データを配列へ保存後、配列データを印刷する
     */
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf)
    {
        final Calendar cals = Calendar.getInstance();
        final Calendar cale = Calendar.getInstance();
        //List data_lst = null;                    //生徒明細
        ResultSet rs1 = null;
        final int[][] arrsum1 = new int[3][12];          //授業日数（学年別・月別）
        final int[][] arrsum2 = new int[3][12];          //欠席のべ日数（学年別・月別）
        final float[][] arrsum3 = new float[3][12];      //出席率（学年別・月別）
        arrsumInz(arrsum1, arrsum2, arrsum3);    //配列初期化

        try {
            ps1 = db2.prepareStatement(prestatementAbsentNum());        //学年別出欠統計
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        try {
            sdf.applyPattern("yyyy-MM-dd");
            cale.setTime(sdf.parse(_param6));            //出欠データ読み込み開始日

            //学年別月別に統計データを配列へ保存 ８月は除外
            for (int i = 0; i < frommonth.length; i++) {
                cals.setTime(sdf.parse(frommonth[i]));    //月開始日
                _param8 = (cale.after(cals))? sdf.format(cale.getTime()) : sdf.format(cals.getTime());
                try {
                    int pp = 0;
                    ps1.setString(++pp,  (i < 4)? "1" : (i < 9)? "2" : "3");    //学期
                    ps1.setDate(++pp,  Date.valueOf(_param8));                      //出欠データ読み込み開始日
                    ps1.setDate(++pp,  Date.valueOf(tomonth[i]));                    //出欠データ読み込み終了日
                    ps1.setDate(++pp,  Date.valueOf(_param8));                      //出欠データ読み込み開始日
                    ps1.setDate(++pp,  Date.valueOf(tomonth[i]));                    //出欠データ読み込み終了日
                    //ps1.setString(++pp,  frommonth[0]);                  //留学・休学読み込み開始日
                    //ps1.setString(++pp,  tomonth[i]);                    //留学・休学読み込み終了日
                    //ps1.setString(++pp,  frommonth[0]);                  //異動者不在日数読み込み開始日
                    //ps1.setString(++pp,  tomonth[i]);                    //異動者不在日数読み込み終了日
                    //ps1.setString(++pp,  tomonth[i]);                    //異動者基準日
                    ps1.setString(++pp,  frommonth[i].substring(5, 7));  //出欠集計データ読み込み月
//log.debug("_param8="+_param8 + "   " + tomonth[i] + "  " + frommonth[i] + "   " + frommonth[i].substring(5, 7));
                    rs1 = ps1.executeQuery();
//log.debug("_param8="+_param8 + "   " + tomonth[i] + "  " + frommonth[i] + "   " + frommonth[i].substring(5, 7));
//log.debug(ps1.toString());
                    while(rs1.next()) {
                        setOutList(rs1, i, arrsum1, arrsum2, arrsum3);   //学年別月別統計データ保存処理
                    }
                } catch (Exception ex) {
                    log.error("error! ", ex);
                } finally {
                    DbUtils.closeQuietly(rs1);
                    db2.commit();
                }
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        printOutsvf(svf, arrsum1, arrsum2, arrsum3);  //配列データを印刷
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
        printOutsvfCHOKETU(db2, svf);          //長期欠席者を出力 05/10/19

    }


    /** 
     *  学年別月別統計データ保存処理
     */
    private void setOutList(ResultSet rs, int i, final int[][] arrsum1, final int[][] arrsum2, final float[][] arrsum3)
    {
        int j = 0;
        try {
            j = Integer.parseInt(rs.getString("GRADE")) - 1;
            arrsum1[j][i] = Integer.parseInt(rs.getString("LESSON"));        //学年別月別授業日数
            arrsum2[j][i] = Integer.parseInt(rs.getString("ABSENT"));        //学年別月別欠席のべ日数
            arrsum3[j][i] = Float.parseFloat(rs.getString("PERSENT"));    //学年別月別出席率
        } catch (Exception ex) {
            log.error("error! j = " + j, ex);
        }
    }


    /** 
     *  学年別月別統計データ印刷処理
     */
    private void printOutsvf(final Vrw32alp svf, final int[][] arrsum1, final int[][] arrsum2, final float[][] arrsum3)
    {
        try {
            printOutsvfDetailInt(svf, arrsum1, "(1)月別授業日数");        //学年別授業日数印刷
            printOutsvfDetailInt(svf, arrsum2, "(2)月別欠席のべ日数");    //学年別欠席のべ日数印刷
            printOutsvfDetailtotalInt(svf, arrsum2);                        //学年別欠席のべ日数合計行印刷
            printOutsvfDetailFloat(svf, arrsum3, "(3)月別出席率");        //学年別出席率印刷
            printOutsvfDetailtotalFloat(svf, arrsum3);                    //出席率合計行印刷
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /** 
     *  学年別授業日数・学年別欠席のべ日数印刷
     *    データは整数値
     */
    private void printOutsvfDetailInt(final Vrw32alp svf, final int[][] arrsum, String title)
    {
        int total = 0;
        try {
            svf.VrsOut("TITLE",  title);     //表タイトル
            for (int i = 0; i < arrsum.length; i++) {
                for (int j = 0; j < arrsum[i].length; j++) {
                    if (0 < arrsum[i][j])
                        svf.VrsOutn("ATTEND" + (j + 1), (i + 1),  String.valueOf(arrsum[i][j]));  //学年別月別データ
                    total += arrsum[i][j];
                }
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL4", (i + 1),  String.valueOf(total));      //学年別合計
                total = 0;
                for (int j = 0; j < arrsum[i].length && j < 4; j++)    total += arrsum[i][j];
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL1", (i + 1),  String.valueOf(total));      //学年別１学期合計
                total = 0;
                for (int j = 4; j < arrsum[i].length && j < 9; j++)    total += arrsum[i][j];
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL2", (i + 1),  String.valueOf(total));      //学年別２学期合計
                total = 0;
                for (int j = 9; j < arrsum[i].length; j++)    total += arrsum[i][j];
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL3", (i + 1),  String.valueOf(total));      //学年別３学期合計
                total = 0;
            }
            svf.VrEndRecord();
            nonedata = true;

        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /** 
     *  学年別欠席のべ日数合計行印刷
     *    データは整数値
     */
    private void printOutsvfDetailtotalInt(final Vrw32alp svf, final int[][] arrsum)
    {
        int total = 0;
        try {
            svf.VrsOut("ITEMNAME",  "合計");     //行タイトル
            for (int j = 0; j < arrsum[0].length; j++) {
                if (0 < arrsum[0][j] + arrsum[1][j] + arrsum[2][j])
                    svf.VrsOut("TOTAL" +  (j + 1),  String.valueOf(arrsum[0][j] + arrsum[1][j] + arrsum[2][j]));      //学年別合計
                total += (arrsum[0][j] + arrsum[1][j] + arrsum[2][j]);
            }
            if (0 < total) svf.VrsOut("TOTAL_TOTAL4",  String.valueOf(total));                //全学年合計

            total = 0;
            for (int j = 0; j < arrsum[0].length && j < 4; j++)    total += (arrsum[0][j] + arrsum[1][j] + arrsum[2][j]);
            if (0 < total) svf.VrsOut("TOTAL_TOTAL1",  String.valueOf(total));              //全学年１学期合計
            total = 0;
            for (int j = 4; j < arrsum[0].length && j < 9; j++)    total += (arrsum[0][j] + arrsum[1][j] + arrsum[2][j]);
            if (0 < total) svf.VrsOut("TOTAL_TOTAL2",  String.valueOf(total));              //全学年２学期合計
            total = 0;
            for (int j = 9; j < arrsum[0].length; j++)    total += (arrsum[0][j] + arrsum[1][j] + arrsum[2][j]);
            if (0 < total) svf.VrsOut("TOTAL_TOTAL3",  String.valueOf(total));               //全学年３学期合計

            svf.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /** 
     *  学年別出席率印刷
     *    データは浮動小数点
     */
    private void printOutsvfDetailFloat(final Vrw32alp svf, float[][] arrsum, String title)
    {
        float total = 0;
        int count = 0;
        try {
            svf.VrsOut("TITLE",  title);     //表タイトル
            for (int i = 0; i < arrsum.length; i++) {
                for (int j = 0; j < arrsum[i].length; j++) {
                    if (0 < arrsum[i][j])
                        svf.VrsOutn("ATTEND" + (j + 1), (i + 1),  String.valueOf(arrsum[i][j]));  //学年別月別データ
                    total += arrsum[i][j];
                    if (0 < arrsum[i][j])count++;
                }
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL4", (i + 1),  String.valueOf((float) Math.round(total / count * 10) / 10));  //学年別合計

                total = 0;
                count = 0;
                for (int j = 0; j < arrsum[i].length && j < 4; j++) {
                    total += arrsum[i][j];
                    if (0 < arrsum[i][j])count++;
                }
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL1", (i + 1),  String.valueOf((float) Math.round(total / count * 10) / 10));  //学年別１学期合計

                total = 0;
                count = 0;
                for (int j = 4; j < arrsum[i].length && j < 9; j++) {
                    total += arrsum[i][j];
                    if (0 < arrsum[i][j])count++;
                }
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL2", (i + 1),  String.valueOf((float) Math.round(total / count * 10) / 10));  //学年別２学期合計

                total = 0;
                count = 0;
                for (int j = 9; j < arrsum[i].length; j++) {
                    total += arrsum[i][j];
                    if (0 < arrsum[i][j])count++;
                }
                if (0 < total) svf.VrsOutn("ATTEND_TOTAL3", (i + 1),  String.valueOf((float) Math.round(total / count * 10) / 10));  //学年別３学期合計
            }
            svf.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /** 
     *  学年別出席率合計行印刷
     *    データは浮動小数点
     */
    private void printOutsvfDetailtotalFloat(final Vrw32alp svf, final float[][] arrsum)
    {
        float totalm = 0;
        int countm = 0;
        float total = 0;
        int count = 0;
        try {
            svf.VrsOut("ITEMNAME",  "平均");
            for (int j = 0; j < arrsum[0].length; j++) {
                for (int i = 0; i < arrsum.length; i++) {
                    if (0 < arrsum[i][j]) {
                        totalm += arrsum[i][j];
                        countm++;
                    }
                }
                if (0 < totalm)    svf.VrsOut("TOTAL" +  (j + 1),  String.valueOf((float) Math.round(totalm / countm * 10) / 10));
                total += totalm;
                count += countm;
                totalm = 0;
                countm = 0;
            }
            if (0 < total) svf.VrsOut("TOTAL_TOTAL4",  String.valueOf((float) Math.round(total / count * 10) / 10));

            total = 0;
            count = 0;
            for (int j = 0; j < arrsum[0].length && j < 4; j++) {
                for (int i = 0; i < arrsum.length; i++) {
                    if (0 < arrsum[i][j]) {
                        totalm += arrsum[i][j];
                        countm++;
                    }
                }
                total += totalm;
                count += countm;
                totalm = 0;
                countm = 0;
            }
            if (0 < total) svf.VrsOut("TOTAL_TOTAL1",  String.valueOf((float) Math.round(total / count * 10) / 10));

            total = 0;
            count = 0;
            for (int j = 4; j < arrsum[0].length && j < 9; j++) {
                for (int i = 0; i < arrsum.length; i++) {
                    if (0 < arrsum[i][j]) {
                        totalm += arrsum[i][j];
                        countm++;
                    }
                }
                total += totalm;
                count += countm;
                totalm = 0;
                countm = 0;
            }
            if (0 < total) svf.VrsOut("TOTAL_TOTAL2",  String.valueOf((float) Math.round(total / count * 10) / 10));

            total = 0;
            count = 0;
            for (int j = 9; j < arrsum[0].length; j++) {
                for (int i = 0; i < arrsum.length; i++) {
                    if (0 < arrsum[i][j]) {
                        totalm += arrsum[i][j];
                        countm++;
                    }
                }
                total += totalm;
                count += countm;
                totalm = 0;
                countm = 0;
            }
            if (0 < total) svf.VrsOut("TOTAL_TOTAL3",  String.valueOf((float) Math.round(total / count * 10) / 10));

            svf.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /** 
     *  学年別月別統計データ保存用配列の初期化
     */
    private void arrsumInz(int[][] arrsum1, int[][] arrsum2, float[][] arrsum3)
    {
        try {
            for (int i = 0; i < arrsum1.length; i++) for (int j = 0; j < arrsum1[i].length; j++) arrsum1[i][j] = 0;
            for (int i = 0; i < arrsum2.length; i++) for (int j = 0; j < arrsum2[i].length; j++) arrsum2[i][j] = 0;
            for (int i = 0; i < arrsum3.length; i++) for (int j = 0; j < arrsum3[i].length; j++) arrsum3[i][j] = 0;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /** 
     *  PrepareStatement作成  学年別出欠統計データ取得
     */
    private String prestatementAbsentNum() {

        final StringBuffer stb = new StringBuffer();
        try {
            //学籍の表
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.SCHREGNO, SEMESTER, GRADE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "WHERE   W1.YEAR = '" + _param0 + "' AND W1.SEMESTER = ? ");
            stb.append(        "GROUP BY W1.SCHREGNO, SEMESTER, GRADE ");
            stb.append(     ") ");

            //生徒別時間割の表
            stb.append(",SCHEDULE_SCHREG_R AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + _param0 + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(                "T1.EXECUTEDATE BETWEEN ? AND ? AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO group by t3.schregno) AND ");  //05/05/17Modify
                                    // 05/10/08 Modify 転入・編入の除外条件を追加
            stb.append(             "NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(                                "((T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < T1.EXECUTEDATE) ");
            stb.append(                               "OR(T3.ENT_DIV IN('4','5') AND T3.ENT_DATE > T1.EXECUTEDATE)) )");   //05/10/08
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND T4.DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     ") ");

            stb.append(",SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1','2') ");
            stb.append(                            "AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append(     ") ");

            //対象生徒の出欠データ
            stb.append(",T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T3.REP_DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append(             "INNER JOIN SCHEDULE_SCHREG T1 ON T0.ATTENDDATE BETWEEN ? AND ? AND ");
            stb.append(                 "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(                 "T0.PERIODCD = T1.PERIODCD ");
            stb.append(             "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND T3.DI_CD = T0.DI_CD ");
            stb.append(     "WHERE   T0.YEAR = '" + _param0 + "' ");
            stb.append(     ") ");
            
            //対象生徒の出欠データ（忌引・出停した日）
            stb.append(" , T_ATTEND_DAT_B AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    T_ATTEND_DAT T0 ");
            stb.append(" WHERE ");
            stb.append("    REP_DI_CD IN('2','3','9','10'");
            if ("true".equals(_useVirus)) {
                stb.append("                          ,'19','20' ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append("                          ,'25','26' ");
            }
            stb.append("    ) ");
            stb.append(" GROUP BY ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE ");
            stb.append(     ") ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(",T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     ") ");
            
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(" , T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                stb.append(     ") ");
            }

            //留学日数を算出 05/02/02 休学を含める 05/03/09
            //05/10/08Modify <change specification of n-times>
            stb.append(",TRANSFER_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE "); //05/03/09
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(         "AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(         "AND T3.TRANSFERCD IN('1','2') ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ");  //05/03/09
            stb.append(     ") ");

            //生徒別出欠データの表
            stb.append(",ATTEND_A AS(");
            stb.append(     "SELECT  TT0.SCHREGNO, ");
            stb.append(             "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(         " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            stb.append(             " AS LESSON, ");
                                    //出席すべき日数
            stb.append(             "( VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(         " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            if ("true".equals(_useVirus)) {
                stb.append(             " - VALUE(TT7.VIRUS,0) - VALUE(TT3_1.VIRUS,0) ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(             " - VALUE(TT7.KOUDOME,0) - VALUE(TT3_2.KOUDOME,0) ");
            }
            stb.append(             " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ) AS MLESSON, ");  //05/10/08
                                    //欠席日数
            stb.append(             "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(         " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            stb.append(             " AS ABSENT, ");
                                    //出席日数
            stb.append(             "( VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) ) ");
            if ("true".equals(_useVirus)) {
                stb.append(             " - VALUE(TT7.VIRUS,0) - VALUE(TT3_1.VIRUS,0) ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(             " - VALUE(TT7.KOUDOME,0) - VALUE(TT3_2.KOUDOME,0) ");
            }
            stb.append(              " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) - VALUE(TT7.ABSENT,0) ) AS PRESENT ");  //05/10/08
            stb.append(     "FROM    SCHNO TT0 ");
            //個人別授業日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(         "FROM    T_PERIOD_CNT ");
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(         "FROM   T_ATTEND_DAT ");
            stb.append(         "WHERE  REP_DI_CD IN ('2','9') ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別出停伝染病日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
            stb.append(         "FROM   T_ATTEND_DAT ");
            stb.append(         "WHERE  REP_DI_CD IN ('19','20') ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ") TT3_1 ON TT0.SCHREGNO = TT3_1.SCHREGNO ");
            //個人別出停交止日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
            stb.append(         "FROM   T_ATTEND_DAT ");
            stb.append(         "WHERE  REP_DI_CD IN ('25','26') ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
            //個人別忌引日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(         "FROM   T_ATTEND_DAT ");
            stb.append(         "WHERE  REP_DI_CD IN ('3','10') ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            //個人別欠席日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT  W0.SCHREGNO, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(         "FROM    ATTEND_DAT W0 ");
            stb.append(                "INNER JOIN (SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(                 "FROM    T_PERIOD_CNT T0, ");
            stb.append(                        "(SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                                 "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                                 "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(                         "FROM    T_ATTEND_DAT W1 ");
            stb.append("              WHERE ");
            stb.append("                  W1.REP_DI_CD IN ('1', '4','5','6','11','12','13' ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                          ,'2','9','3','10' ");
                if ("true".equals(_useVirus)) {
                    stb.append("                          ,'19','20' ");
                }
                if ("true".equals(_useKoudome)) {
                    stb.append("                          ,'25','26' ");
                }
            }
            stb.append("                              ) ");
            stb.append(                         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(                         ") T1 ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append(                 "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                         "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                         "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                         "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(                ") W1 ON   W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                 "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(                 "W0.PERIODCD = W1.FIRST_PERIOD ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(             "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND W0.DI_CD = T3.DI_CD ");
            stb.append(         "GROUP BY W0.SCHREGNO ");
            stb.append(         ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");

            //月別集計データから集計した表
            stb.append(     "LEFT JOIN(");
            stb.append(        "SELECT  SCHREGNO, ");
            stb.append(                "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                   "+ VALUE(SUM(OFFDAYS),0) ");
            }
            stb.append(                "AS LESSON, ");
            stb.append(                "SUM(MOURNING) AS MOURNING, ");
            stb.append(                "SUM(SUSPEND) AS SUSPEND, ");
            if ("true".equals(_useVirus)) {
                stb.append(                "SUM(VIRUS) AS VIRUS, ");
            } else {
                stb.append(                "0 AS VIRUS, ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(                "SUM(KOUDOME) AS KOUDOME, ");
            } else {
                stb.append(                "0 AS KOUDOME, ");
            }
            stb.append(                "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            stb.append(                ") AS ABSENT, ");
            stb.append(                "SUM(LATE) AS LATE, ");
            stb.append(                "SUM(EARLY) AS EARLY ");
            stb.append(         "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(         "WHERE  YEAR = '" + _param0 + "' AND ");
            //stb.append(                "SEMESTER = '" + _param1 + "' AND ");
            stb.append(                "MONTH = ? AND ");
            stb.append(                "SEMESTER||MONTH <= '" + _param7 + "' AND ");
            stb.append(                "EXISTS(SELECT  'X' FROM SCHNO W2 WHERE W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            //留学日数の表
            stb.append(     "LEFT JOIN(");
            stb.append(        "SELECT  SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
            stb.append(        "FROM    TRANSFER_SCHREG ");
            stb.append(        "WHERE   TRANSFERCD = '1' ");
            stb.append(        "GROUP BY SCHREGNO ");
            stb.append(        ")TT8 ON TT8.SCHREGNO=TT0.SCHREGNO ");

            //休学日数の表
            stb.append(     "LEFT JOIN(");
            stb.append(        "SELECT  SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
            stb.append(        "FROM    TRANSFER_SCHREG ");
            stb.append(        "WHERE   TRANSFERCD = '2' ");
            stb.append(        "GROUP BY SCHREGNO ");
            stb.append(        ")TT9 ON TT9.SCHREGNO=TT0.SCHREGNO ");

            //異動者不在日数の表
            //stb.append(     "LEFT JOIN LEAVE_SCHREG TT9 ON TT9.SCHREGNO=TT0.SCHREGNO ");

            stb.append(     ") ");

            //メイン表
            stb.append("SELECT  GRADE, ");
            stb.append(           "MAX(LESSON) AS LESSON, ");
            stb.append(           "SUM(MLESSON) AS MLESSON, ");
            stb.append(           "SUM(ABSENT) AS ABSENT, ");
            stb.append(        "case when SUM(MLESSON) <= 0 then 0 else ");
            stb.append(           "DECIMAL(ROUND( FLOAT(SUM(PRESENT)) / FLOAT(SUM(MLESSON)) * 1000 ,0) / 10 ,5 ,1 ) end AS PERSENT ");
            stb.append("FROM    ATTEND_A W1, SCHNO W2 ");
            stb.append("WHERE   W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("GROUP BY GRADE ");

            log.debug("prestatementAbsentNum()=" + stb.toString());
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return stb.toString();

    }//prestatementAbsentNum()の括り


    /** 
     *  長期欠席者印刷処理
     *  2005/10/18 Build
     */
    private void printOutsvfCHOKETU(final DB2UDB db2, final Vrw32alp svf)
    {
        ResultSet rs = null;
        try {
            setValueMonthCHOKETU(db2);  //NO010
log.debug("_param9=" + _param9);
            final String paraMonth = _param9.substring(1);
            final String paraSem = paraMonth.equals("08") ? "2" : _param9.substring(0, 1);
            log.debug("paraSem = " + paraSem);
            log.debug("paraMonth = " + paraMonth);
            ps1 = db2.prepareStatement(prestatementAbsentCHOKETU(paraSem, paraMonth));        //長欠者リスト
            int pp = 0;
            final String fromvalue = getValueTermOfCHOKETU(_param6);  //NO010
            ps1.setDate(++pp,  Date.valueOf(fromvalue));                      //出欠データ読み込み開始日  --NO010
            ps1.setDate(++pp,  Date.valueOf(tomonth[tomonth.length - 1]));  //出欠データ読み込み終了日 指定終了月の最終日
            ps1.setDate(++pp,  Date.valueOf(fromvalue));                      //出欠データ読み込み開始日  --NO010
            ps1.setDate(++pp,  Date.valueOf(tomonth[tomonth.length - 1]));  //出欠データ読み込み終了日 指定終了月の最終日
log.debug("fromvalue=" + fromvalue);
log.debug("tomonth[" + (tomonth.length - 1) + "]=" + tomonth[tomonth.length - 1]);
            rs = ps1.executeQuery();
            int len = 0;
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; rs.next(); i++) {
                if (i == 0) {
                    svf.VrsOut("NOTE_TITLE", "長期欠席者");
                }
                //１行当たり８０Byteを超えたら、前の生徒まで出力
                final String str = Integer.parseInt(rs.getString("GRADE")) + "-" + editHrclass(rs.getString("HR_CLASS")) + editName(rs.getString("NAME")) + rs.getString("ABSENT") + "日・";
                final int dlen = KNJ_EditEdit.ret_byte_2(str, 124);  //NO010
                //NO010 dlen = KNJ_EditEdit.ret_byte_2(str, 80);
                if (len + dlen > 124) {   //NO010
                //NO010 if (len + dlen > 80) {
                    svf.VrsOut("NOTE", stb.toString());
                    svf.VrEndRecord();
                    stb.delete(0, stb.length());
                    len = 0;
                }
                stb.append(str);
                len += dlen;
            }
            //最終行の出力
            if (len > 0) {
                //最後の中点を削除
                if (stb.substring(stb.length()-1).equals("・")) {
                    stb.delete(stb.length() - 1, stb.length());
                }
                svf.VrsOut("NOTE", stb.toString());
                svf.VrEndRecord();
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally{
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps1);
        }
    }


    /** 
     *  長欠者算出月を設定
     *  NO010 Build
     */
    private void setValueMonthCHOKETU(final DB2UDB db2)
    {
        ResultSet rs = null;
        try {
            String str = "SELECT  MIN(SEMESTER) AS SEMESTER "
                       + "FROM    SEMESTER_MST "
                       + "WHERE   YEAR = '" + _param0 + "' "
                       +     "AND ('" + frommonth[frommonth.length -1] + "' BETWEEN SDATE AND EDATE "
                       +       "OR '" + tomonth[frommonth.length -1]   + "' BETWEEN SDATE AND EDATE) "
                       +     "AND SEMESTER <> '9' ";
            db2.query(str);
            rs = db2.getResultSet();
            if (rs.next()) {
                _param9 = rs.getString("SEMESTER") + frommonth[frommonth.length - 1].substring(5, 7);
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally{
            DbUtils.closeQuietly(rs);
            db2.commit();
            if (_param9 == null) {
                _param9 = _param7;
            }
        }
    }


    /** 
     *  長欠者算出期間を設定
     *  NO010 Build
     */
    private String getValueTermOfCHOKETU(final String str)
    {
        final Calendar cals = Calendar.getInstance();
        final Calendar cale = Calendar.getInstance();
        String retval = null;
        try {
            cals.setTime(sdf.parse(frommonth[frommonth.length - 1]));    //開始日付
            cale.setTime(sdf.parse(str));                            //出欠データ読み込み開始日
            if (cals.after(cale)) {
                retval = frommonth[frommonth.length - 1];
            }
            else retval = str;
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally{
            if (retval == null) retval = str;
        }
        return retval;
    }


    /** 
     *  氏名編集
     *  2005/10/18 Build
     */
    private String editName(final String str)
    {
        String retval = null;
        try {
            //ブランク以降の文字列を削除 => 苗字を返す
            if (0 < str.indexOf(' ')) retval = str.substring(0, str.indexOf(' '));
            else
            if (0 < str.indexOf('　')) retval = str.substring(0, str.indexOf('　'));
            else
            retval = str;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return retval;
    }


    /** 
     *  組編集
     *  2005/10/18 Build
     */
    private String editHrclass(final String str)
    {
        String retval = null;
        try {
            //最初の文字は数字以外は除外
            if (!Character.isDigit(str.charAt(0))) {
                retval = str.substring(1, str.length());
            } else {
                retval = str;
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return String.valueOf(Integer.parseInt(retval));
    }


    /** 
     *  PrepareStatement作成  長欠者リスト
     *  2005/10/18 Build
     */
    private String prestatementAbsentCHOKETU(final String paraSem, final String paraMonth) {

        final StringBuffer stb = new StringBuffer();
        try {
            //学籍の表
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.SCHREGNO, ATTENDNO, HR_CLASS,GRADE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "WHERE   W1.YEAR = '" + _param0 + "' ");
            stb.append(        "GROUP BY W1.SCHREGNO, ATTENDNO, HR_CLASS,GRADE ");
            stb.append(     ") ");

            //生徒別時間割の表
            stb.append(",SCHEDULE_SCHREG_R AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + _param0 + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(                "T1.EXECUTEDATE BETWEEN ? AND ? AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO) AND ");
            stb.append(             "NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(                                "((T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < T1.EXECUTEDATE) ");
            stb.append(                               "OR(T3.ENT_DIV IN('4','5') AND T3.ENT_DATE > T1.EXECUTEDATE)) )");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append(                   "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND T4.DI_CD = T3.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T3.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append(                   "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND T4.DI_CD = T3.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND T3.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     ") ");

            stb.append(",SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1','2') ");
            stb.append(                            "AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append(     ") ");

            //対象生徒の出欠データ
            stb.append(",T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T3.REP_DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append(             "INNER JOIN SCHEDULE_SCHREG T1 ON T0.ATTENDDATE BETWEEN ? AND ? AND ");
            stb.append(                 "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(                 "T0.PERIODCD = T1.PERIODCD ");
            stb.append(             "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND T0.DI_CD = T3.DI_CD ");
            stb.append(             "WHERE ");
            stb.append(             " T0.YEAR = '" + _param0 + "' ");
            stb.append(     ") ");
            
            //対象生徒の出欠データ（忌引・出停した日）
            stb.append(" , T_ATTEND_DAT_B AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    T_ATTEND_DAT T0 ");
            stb.append(" WHERE ");
            stb.append("    REP_DI_CD IN('2','3','9','10'");
            if ("true".equals(_useVirus)) {
                stb.append("                          ,'19','20' ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append("                          ,'25','26' ");
            }
            stb.append("   ) ");
            stb.append(" GROUP BY ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE ");
            stb.append(     ") ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(",T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     ") ");

            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(" ,T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                stb.append(" ) ");
            }

            //留学日数を算出 休学を含める
            stb.append(",TRANSFER_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ");
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(         "AND T3.TRANSFERCD IN('1','2') ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ");
            stb.append(     ") ");

            //生徒別欠席データの表
            stb.append(",ATTEND_A AS(");
            stb.append(     "SELECT  TT0.SCHREGNO, ");
            stb.append(             "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                 " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            stb.append(             " AS ABSENT ");
            stb.append(     "FROM    SCHNO TT0 ");
                            //個人別欠席日数
            stb.append(     "LEFT OUTER JOIN(");
            stb.append(         "SELECT  W0.SCHREGNO, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(                 "SUM(CASE T3.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(         "FROM    ATTEND_DAT W0 ");
            stb.append(                "INNER JOIN (SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(                 "FROM    T_PERIOD_CNT T0, ");
            stb.append(                        "(SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                                 "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                                 "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(                         "FROM    T_ATTEND_DAT W1 ");
            stb.append("              WHERE ");
            stb.append("                  W1.REP_DI_CD IN ('1', '4','5','6','11','12','13' ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                          ,'2','9','3','10' ");
                if ("true".equals(_useVirus)) {
                    stb.append("                          ,'19','20' ");
                }
                if ("true".equals(_useKoudome)) {
                    stb.append("                          ,'25','26' ");
                }
            }
            stb.append("                              ) ");
            stb.append(                         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(                         ") T1 ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append(                 "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                         "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                         "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                         "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(                ") W1 ON W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                 "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(                 "W0.PERIODCD = W1.FIRST_PERIOD ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(             "INNER JOIN ATTEND_DI_CD_DAT T3 ON T3.YEAR = '" + _param0 + "' AND W0.DI_CD = T3.DI_CD ");
            stb.append(         "GROUP BY W0.SCHREGNO ");
            stb.append(         ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
                            //月別集計データから集計した表
            stb.append(     "LEFT JOIN(");
            stb.append(        "SELECT  SCHREGNO, ");
            stb.append(                "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            stb.append(                ") AS ABSENT ");
            stb.append(         "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(         "WHERE  YEAR = '" + _param0 + "' AND ");
            stb.append(                "SEMESTER||MONTH = '" + paraSem + paraMonth + "' AND ");  //--NO010
            //NO010 stb.append(                "SEMESTER||MONTH <= '" + _param7 + "' AND ");
            stb.append(                "EXISTS(SELECT  'X' FROM SCHNO W2 WHERE W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");
            //休学日数の表
            stb.append(     "LEFT JOIN(");
            stb.append(         "SELECT SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
            stb.append(         "FROM   TRANSFER_SCHREG ");
            stb.append(         "WHERE  TRANSFERCD = '2' ");
            stb.append(         "GROUP BY SCHREGNO ");
            stb.append(         ")TT9 ON TT9.SCHREGNO = TT0.SCHREGNO ");
            stb.append(     ") ");

            //長期欠席者を抽出
            stb.append(",CHOKETU_LIST AS(");
            stb.append("SELECT  W1.SCHREGNO, ATTENDNO, HR_CLASS, GRADE, ");
            stb.append(           "SUM(ABSENT) AS ABSENT ");
            stb.append("FROM    ATTEND_A W1, SCHNO W2 ");
            stb.append("WHERE   W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("GROUP BY W1.SCHREGNO, ATTENDNO, HR_CLASS, GRADE ");
            stb.append("HAVING SUM(ABSENT) >= " + _param5);
            stb.append(     ") ");

            //メイン表
            stb.append("SELECT  ABSENT, NAME, ATTENDNO, HR_CLASS, GRADE ");
            stb.append("FROM    CHOKETU_LIST W1, SCHREG_BASE_MST W2 ");
            stb.append("WHERE   W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("ORDER BY GRADE, HR_CLASS, ATTENDNO");

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return stb.toString();
    }

}//クラスの括り

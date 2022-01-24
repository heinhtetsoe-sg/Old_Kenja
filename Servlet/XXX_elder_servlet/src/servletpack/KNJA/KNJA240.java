package servletpack.KNJA;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import nao_package.svf.*;
import nao_package.db.*;
import java.sql.*;
import java.util.*;
import servletpack.KNJZ.detail.*;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 [学籍管理]
 *
 *                     ＜ＫＮＪＡ２４０＞    在籍調べ
 *
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2004/08/18 nakamoto 組のデータ型が数値でも文字でも対応できるようにする
 * 2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加  --NO003
 *                            => 無い場合は従来通りHR_CLASSを出力
 * 2006/05/18 nakamoto 転学・退学日は在籍になります。NO004 Modify
 *                     編入・転入日は在籍になります。NO005 Add
 *
 * $Id: e59093d96b205e6cb15acdf2b55f0b19075c9189 $
 */

public class KNJA240 {
    private static final Log log = LogFactory.getLog(KNJA240.class);

    Vrw32alp svf = new Vrw32alp();
    DB2UDB    db2;
    String dbname = new String();
    boolean nonedata = false; //該当データなしフラグ
    KNJ_EditDate editdate = new KNJ_EditDate();     //和暦変換取得クラスのインスタンス作成
    private Map hmap;       //NO003

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String param[] = new String[10];  /** 0-2:画面より受取 3:作成年月 4:学年数 7:処理日 **/

        // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");                  // データベース名
            log.debug("dbname= " + dbname);
            param[0] = request.getParameter("YEAR");                    // 年度
            param[2] = request.getParameter("SEMESTER");                // 学期
            //  日付型を変換
            KNJ_EditDate edit_date = new KNJ_EditDate();                            //取得クラスのインスタンス作成
            param[7] = edit_date.h_format_sec(request.getParameter("DATE"));        //学籍処理日
            //  学年数
            // param[4] = request.getParameter("GRADE_HVAL");               // 学年数            
            //  組名称にSCHREG_REGD_HDATのHR_CLASS_NAME1を使用するか
            param[8] = request.getParameter("useSchregRegdHdat");
            
            //  ５年制
            param[5] = "('01','02','03','04','05')";
            //  ６年制
            param[6] = "('06')";

            log.debug("parameter ok!");
        } catch( Exception ex ) {
            log.warn("parameter error!", ex);
        }

        OutputStream outstrm = init(response);

        // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.warn("DB2 open error!", ex);
        }

        /* 作成日の取得 */
        param[3] = getCreateDate(param);

        // DBから学年数の読み込み
        param[4] = getGradeMaxValue(db2, param[0], param[2]);

        for (int ia=0 ; ia<param.length ; ia++) {
            log.debug("param[" + ia + "]= " + param[ia]);
        }

        // ＳＶＦフォーム出力
        nonedata = false ; //該当データなしフラグ
        svf.VrSetForm("KNJA240.frm", 1);
//      set_head(param);
//      set_kyugaku(param);
        if (hmap == null) {
            hmap = KNJ_Get_Info.getMapForHrclassName(db2);  //NO003 表示用組
        }
        set_meisai(param);
        if (Integer.parseInt(param[4]) > 4) {
            set_meisai2(param);
        }

        /* 該当データ無し */
        log.debug("nonedata="+nonedata);
        noneCheck();

        svf.VrPrint();

        // 終了処理
        close(outstrm);
    }

    private String getGradeMaxValue(DB2UDB db2, String year, String semester) {

        ResultSet rs = null;
        Integer maxGrade = new Integer(0);
        try{
            final String sql = "select MAX(GRADE) as MAX_GRADE" +
            " from " +
            "    SCHREG_REGD_DAT " +
            " where " +
            "     year = '"+ year + "' " +
            "     and semester = '" + semester +"' " ;
            log.debug("grade sql="+sql);

            db2.query(sql);
            rs = db2.getResultSet();
            if (rs.next()) {
                maxGrade = Integer.valueOf((String) rs.getString("MAX_GRADE"));
            }
            log.debug("max grade="+maxGrade);

        } catch (Exception ex) {
            log.warn("db2 error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
        }
        
        return maxGrade.toString();
    }
        
    private void close(OutputStream outstrm) throws IOException {
        db2.close();
        svf.VrQuit();
        outstrm.close();
    }

    private void noneCheck() {
        if (nonedata == false) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            final int ret = svf.VrEndRecord();
            log.debug("nonedata VrEndRecord ret=" + ret);
            svf.VrEndPage();
        }
    }

    private OutputStream init(HttpServletResponse response) throws IOException {
        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                          //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定
        return outstrm;
    }

    private String getCreateDate(String[] param) {
        try {
            KNJ_Control date = new KNJ_Control();
            KNJ_Control.ReturnVal returnval = date.Control(db2);
            return returnval.val3;
        } catch( Exception e ){
            log.warn("sakuseibi error!", e);
        }
        return null;
    }

    /*------------------------------------*
     * 　　　　明細ＳＶＦ出力　　　       *
     *------------------------------------*/
    public void set_meisai(String param[]) throws ServletException, IOException {
        try {
            String sql = "SELECT ";
            for (int ia=1 ; ia<6 ; ia++) {
                sql =   sql + "COALESCE(nen" + ia + "_dan,0)             AS CNT" + ia + "_1,"
                            + "COALESCE(nen" + ia + "_jo,0)              AS CNT" + ia + "_2,"
                            + "COALESCE(nen" + ia + "_danjo_kei,0)       AS CNT" + ia + "_3,"
                            + "COALESCE(nen" + ia + "_ya_dan,0)          AS CNT" + ia + "_1S,"
                            + "COALESCE(nen" + ia + "_ya_jo,0)           AS CNT" + ia + "_2S,"
                            + "COALESCE(nen" + ia + "_ya_danjo_kei,0)    AS CNT" + ia + "_3S,";
            }
            sql =   sql + "TBL3.HR_CLASS " + ("1".equals(param[8]) ? ", TBL3.HR_CLASS_NAME1 " : "")
                + "FROM "
                    + "("
                        + "SELECT DISTINCT "
                            + "HR_CLASS, HR_CLASS_NAME1 "
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR       =  '" +  param[0] + "' "
                            + "AND SEMESTER   =  '" +  param[2] + "' "
                    + ") TBL3 "

                    + "LEFT JOIN ("
                        + "SELECT ";
            for (int ia=1 ; ia<6 ; ia++){
                    sql =   sql + "Sum(CASE Z.GRADE || SEX WHEN '0" + ia + "1' THEN 1 ELSE 0 END) AS nen" + ia + "_dan,"
                                + "Sum(CASE Z.GRADE || SEX WHEN '0" + ia + "2' THEN 1 ELSE 0 END) AS nen" + ia + "_jo,"
                                + "Sum(CASE Z.GRADE        WHEN '0" + ia + "'  THEN 1 ELSE 0 END) AS nen" + ia + "_danjo_kei,";
            }
            sql =   sql + "Z.HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_HDAT Z1,"
                            + "SCHREG_REGD_DAT Z,"
                            + "SCHREG_BASE_MST M "
                        + "WHERE "
                                + "Z.YEAR       =  '" +  param[0] + "' "
                            + "AND Z.SEMESTER   =  '" +  param[2] + "' "
                            + "AND Z.GRADE      IN " +  param[5] + " "
                            + "AND Z.YEAR       = Z1.YEAR "
                            + "AND Z.SEMESTER   = Z1.SEMESTER "
                            + "AND Z.GRADE      = Z1.GRADE "
                            + "AND Z.HR_CLASS   = Z1.HR_CLASS "
                            + "AND Z.SCHREGNO   = M.SCHREGNO "
                            + "AND M.SEX        IN ('1','2') "
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.GRD_DATE  < '" +  param[7] + "' "//NO004
                                            + "AND T1.GRD_DIV   IN ('1', '2','3') "   //卒業生・転学者・退学者は含まない
                                    + ") "
//NO005===>
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.ENT_DATE  > '" +  param[7] + "' "
                                            + "AND T1.ENT_DIV   IN ('4','5') "   //編入者・転入者は含まない
                                    + ") "
//NO005<===
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL1 ON (TBL1.HR_CLASS = TBL3.HR_CLASS) "

                    + "LEFT JOIN ("
                        + "SELECT ";
            for (int ia=1 ; ia<6 ; ia++){
                    sql =   sql + "Sum(CASE Z.GRADE || SEX WHEN '0" + ia + "1' THEN 1 ELSE 0 END) AS nen" + ia + "_ya_dan,"
                                + "Sum(CASE Z.GRADE || SEX WHEN '0" + ia + "2' THEN 1 ELSE 0 END) AS nen" + ia + "_ya_jo,"
                                + "Sum(CASE Z.GRADE        WHEN '0" + ia + "'  THEN 1 ELSE 0 END) AS nen" + ia + "_ya_danjo_kei,";
            }
            sql =   sql + "Z.HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_DAT Z "
                        + "LEFT JOIN "
                            + "SCHREG_TRANSFER_DAT D ON ( Z.SCHREGNO = D.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_REGD_HDAT Z1 ON ( Z.YEAR = Z1.YEAR AND Z.SEMESTER = Z1.SEMESTER AND Z.GRADE = Z1.GRADE AND Z.HR_CLASS = Z1.HR_CLASS ) "
                        + "WHERE "
                                + "Z.YEAR               =  '" +  param[0] + "'"
                            + "AND Z.SEMESTER           =  '" +  param[2] + "' "
                            + "AND Z.GRADE              IN " +  param[5] + " "
                            + "AND M.SEX                IN ('1','2') "
                            + "AND D.TRANSFERCD         IN ('1','2') "
                            + "AND '" +  param[7] + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE "
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL2 ON (TBL2.HR_CLASS = TBL3.HR_CLASS) "

                + "ORDER BY "
                    + "TBL3.HR_CLASS";

            log.debug("set_meisai  sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            /** SVFフォームへデータをセット **/
            int calc_m1[][][] = new int[2][5][3];   //クラス行
            int calc_m3[][][] = new int[2][5][3];   //小計行

            //レコードが真の間ループ
            for (int i=1 ; rs.next() ; i++) {
                //改ページ
                if(i > 15){
                    set_head(param);
                    svf.VrEndPage();
                    i=1;
                }
                //クラス行初期化
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m1[ia][ib][ic] = 0;
                        }
                    }
                }
                //明細データ取得
                for (int ia=0 ; ia<5 ; ia++) {
                    for (int ib=0 ; ib<3 ; ib++) {
                        calc_m1[0][ia][ib] = rs.getInt("CNT"+(ia+1)+"_"+(ib+1));
                        calc_m1[1][ia][ib] = rs.getInt("CNT"+(ia+1)+"_"+(ib+1)+"S");
                    }
                }
                //合計累積
                if (Integer.parseInt(param[4]) < 5) {
                    for (int ia=0 ; ia<2 ; ia++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m1[ia][4][ic] =  0;
                        }
                    }
                    for (int ia=0 ; ia<2 ; ia++) {
                        for (int ib=0 ; ib<4 ; ib++) {
                            for (int ic=0 ; ic<3 ; ic++) {
                                calc_m1[ia][4][ic] =  calc_m1[ia][4][ic] +  calc_m1[ia][ib][ic];
                            }
                        }
                    }
                }
                //小計累積
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m3[ia][ib][ic] =  calc_m3[ia][ib][ic] +  calc_m1[ia][ib][ic];
                        }
                    }
                }
                String hrName;
                if ("1".equals(param[8])) {
                    hrName = null == rs.getString("HR_CLASS_NAME1") ? null : rs.getString("HR_CLASS_NAME1") + "組";
                } else {
                    hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), hmap) + "組";
                }
                if (hrName == null) {
                    hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS")) + "組";
                }
                
                svf.VrsOutn("class" , i, hrName);// クラス出力 2004/08/18  NO003Modify

                //１学年出力
                if (calc_m1[0][0][2] != 0) {
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT1"+"_"+(ic+1),     i, calc_m1[0][0][ic]);
                        svf.VrlOutn("CNT1"+"_"+(ic+1)+"S", i, calc_m1[1][0][ic]);
                    }
                    svf.VrsOutn("GRADE" , 1, "１学年");     // 学年出力
                }
                //２学年出力
                if (calc_m1[0][1][2] != 0) {
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT2"+"_"+(ic+1),     i, calc_m1[0][1][ic]);
                        svf.VrlOutn("CNT2"+"_"+(ic+1)+"S", i, calc_m1[1][1][ic]);
                    }
                    svf.VrsOutn("GRADE" , 2, "２学年");     // 学年出力
                }
                //３学年出力
                if (calc_m1[0][2][2] != 0) {
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT3"+"_"+(ic+1),     i, calc_m1[0][2][ic]);
                        svf.VrlOutn("CNT3"+"_"+(ic+1)+"S", i, calc_m1[1][2][ic]);
                    }
                    svf.VrsOutn("GRADE" , 3, "３学年");     // 学年出力
                }
                //４学年出力
                if (calc_m1[0][3][2] != 0) {
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT4"+"_"+(ic+1),     i, calc_m1[0][3][ic]);
                        svf.VrlOutn("CNT4"+"_"+(ic+1)+"S", i, calc_m1[1][3][ic]);
                    }
                    svf.VrsOutn("GRADE" , 4, "４学年");     // 学年出力
                }
                //合計出力
                if (Integer.parseInt(param[4]) < 5) {
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT5"+"_"+(ic+1),     i, calc_m1[0][4][ic]);
                        svf.VrlOutn("CNT5"+"_"+(ic+1)+"S", i, calc_m1[1][4][ic]);
                    }
                    svf.VrsOutn("GRADE" , 5, "合　計");     // 学年出力
                } else {
                    //５学年出力
                    if (calc_m1[0][4][2] != 0) {
                        for (int ic=0 ; ic<3 ; ic++){
                            svf.VrlOutn("CNT5"+"_"+(ic+1),     i, calc_m1[0][4][ic]);
                            svf.VrlOutn("CNT5"+"_"+(ic+1)+"S", i, calc_m1[1][4][ic]);
                        }
                        svf.VrsOutn("GRADE" , 5, "５学年");     // 学年出力
                    }                    
                }
                svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            if (nonedata == true) {
                int nen=5;
                //小計行出力
                for (int ia=0 ; ia<2 ; ia++){
                    for (int ib=0 ; ib<nen ; ib++){
                        if(calc_m3[ia][ib][2]>0){   //ゼロなら出力しない（指定学年のみ出力）
                            for (int ic=0 ; ic<3 ; ic++){
                                svf.VrlOutn("CNT"+(ib+1)+"_"+(ic+1),     16, calc_m3[0][ib][ic]);
                                svf.VrlOutn("CNT"+(ib+1)+"_"+(ic+1)+"S", 16, calc_m3[1][ib][ic]);
                            }
                        }
                    }
                }
                set_head(param);
                if (Integer.parseInt(param[4]) < 6) {
                    set_kyugaku(param);
                }
                svf.VrEndRecord();
                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.warn("set_meisai read error!", ex);
        }
    }  //set_meisaiの括り

    public void set_meisai2(String param[]) throws ServletException, IOException {
        try {
            String sql = "SELECT ";
            for (int ia=1 ; ia<2 ; ia++) {
                sql =   sql + "COALESCE(nen" + ia + "_dan,0)             AS CNT" + ia + "_1,"
                            + "COALESCE(nen" + ia + "_jo,0)              AS CNT" + ia + "_2,"
                            + "COALESCE(nen" + ia + "_danjo_kei,0)       AS CNT" + ia + "_3,"
                            + "COALESCE(nen" + ia + "_ya_dan,0)          AS CNT" + ia + "_1S,"
                            + "COALESCE(nen" + ia + "_ya_jo,0)           AS CNT" + ia + "_2S,"
                            + "COALESCE(nen" + ia + "_ya_danjo_kei,0)    AS CNT" + ia + "_3S,";
            }
            sql =   sql + "TBL3.HR_CLASS " + ("1".equals(param[8]) ? ", TBL3.HR_CLASS_NAME1 " : "")
                + "FROM "
                    + "("
                        + "SELECT DISTINCT "
                            + "HR_CLASS, HR_CLASS_NAME1 "
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR       =  '" +  param[0] + "' "
                            + "AND SEMESTER   =  '" +  param[2] + "' "
                    + ") TBL3 "

                    + "LEFT JOIN ("
                        + "SELECT ";
            for (int ia=1 ; ia<2 ; ia++){
                    sql =   sql + "Sum(CASE Z.GRADE || SEX WHEN '0" + (ia+5) + "1' THEN 1 ELSE 0 END) AS nen" + ia + "_dan,"
                                + "Sum(CASE Z.GRADE || SEX WHEN '0" + (ia+5) + "2' THEN 1 ELSE 0 END) AS nen" + ia + "_jo,"
                                + "Sum(CASE Z.GRADE        WHEN '0" + (ia+5) + "'  THEN 1 ELSE 0 END) AS nen" + ia + "_danjo_kei,";
            }
            sql =   sql + "Z.HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_HDAT Z1,"
                            + "SCHREG_REGD_DAT Z,"
                            + "SCHREG_BASE_MST M "
                        + "WHERE "
                                + "Z.YEAR       =  '" +  param[0] + "' "
                            + "AND Z.SEMESTER   =  '" +  param[2] + "' "
                            + "AND Z.GRADE      IN " +  param[6] + " "
                            + "AND Z.YEAR       = Z1.YEAR "
                            + "AND Z.SEMESTER   = Z1.SEMESTER "
                            + "AND Z.GRADE      = Z1.GRADE "
                            + "AND Z.HR_CLASS   = Z1.HR_CLASS "
                            + "AND Z.SCHREGNO   = M.SCHREGNO "
                            + "AND M.SEX        IN ('1','2') "
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.GRD_DATE  < '" +  param[7] + "' "//NO004
                                            + "AND T1.GRD_DIV   IN ('1', '2','3') "   //卒業生・転学者・退学者は含まない
                                    + ") "
//NO005===>
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.ENT_DATE  > '" +  param[7] + "' "
                                            + "AND T1.ENT_DIV   IN ('4','5') "   //編入者・転入者は含まない
                                    + ") "
//NO005<===
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL1 ON (TBL1.HR_CLASS = TBL3.HR_CLASS) "

                    + "LEFT JOIN ("
                        + "SELECT ";
            for (int ia=1 ; ia<2 ; ia++) {
                    sql =   sql + "Sum(CASE Z.GRADE || SEX WHEN '0" + (ia+5) + "1' THEN 1 ELSE 0 END) AS nen" + ia + "_ya_dan,"
                                + "Sum(CASE Z.GRADE || SEX WHEN '0" + (ia+5) + "2' THEN 1 ELSE 0 END) AS nen" + ia + "_ya_jo,"
                                + "Sum(CASE Z.GRADE        WHEN '0" + (ia+5) + "'  THEN 1 ELSE 0 END) AS nen" + ia + "_ya_danjo_kei,";
            }
            sql =   sql + "Z.HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_DAT Z "
                        + "LEFT JOIN "
                            + "SCHREG_TRANSFER_DAT D ON ( Z.SCHREGNO = D.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_REGD_HDAT Z1 ON ( Z.YEAR = Z1.YEAR AND Z.SEMESTER = Z1.SEMESTER AND Z.GRADE = Z1.GRADE AND Z.HR_CLASS = Z1.HR_CLASS ) "
                        + "WHERE "
                                + "Z.YEAR               =  '" +  param[0] + "'"
                            + "AND Z.SEMESTER           =  '" +  param[2] + "' "
                            + "AND Z.GRADE              IN " +  param[6] + " "
                            + "AND M.SEX                IN ('1','2') "
                            + "AND D.TRANSFERCD         IN ('1','2') "
                            + "AND '" +  param[7] + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE "
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL2 ON (TBL2.HR_CLASS = TBL3.HR_CLASS) "

                + "ORDER BY "
                    + "TBL3.HR_CLASS";

            log.debug("set_meisai2 sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            /** SVFフォームへデータをセット **/
            int calc_m1[][][] = new int[2][5][3];   //クラス行
            int calc_m3[][][] = new int[2][5][3];   //小計行

            //レコードが真の間ループ
            for (int i=1 ; rs.next() ; i++) {
                //改ページ
                if(i > 15){
                    set_head(param);
                    svf.VrEndPage();
                    i=1;
                }
                //クラス行初期化
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m1[ia][ib][ic] = 0;
                        }
                    }
                }
                //明細データ取得
                for (int ia=0 ; ia<1 ; ia++) { 
                    for (int ib=0 ; ib<3 ; ib++) {
                        calc_m1[0][ia][ib] = rs.getInt("CNT"+(ia+1)+"_"+(ib+1));
                        calc_m1[1][ia][ib] = rs.getInt("CNT"+(ia+1)+"_"+(ib+1)+"S");
                    }
                }
                //合計累積
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<4 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m1[ia][4][ic] =  calc_m1[ia][4][ic] +  calc_m1[ia][ib][ic];
                        }
                    }
                }
                //小計累積
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m3[ia][ib][ic] =  calc_m3[ia][ib][ic] +  calc_m1[ia][ib][ic];
                        }
                    }
                }
                //明細出力
                String hrName;
                if ("1".equals(param[8])) {
                    hrName = null == rs.getString("HR_CLASS_NAME1") ? null : rs.getString("HR_CLASS_NAME1") + "組";
                } else {
                    hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), hmap) + "組";
                }
                if (hrName == null) {
                    hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS")) + "組";
                }
                
                svf.VrsOutn("class" , i, hrName);// クラス出力 2004/08/18  NO003Modify
                //６学年出力
                if(calc_m1[0][0][2] != 0){
                    for (int ic=0 ; ic<3 ; ic++){
                        svf.VrlOutn("CNT1"+"_"+(ic+1),     i, calc_m1[0][0][ic]);
                        svf.VrlOutn("CNT1"+"_"+(ic+1)+"S", i, calc_m1[1][0][ic]);
                    }
                    svf.VrsOutn("GRADE" , 1, "６学年");     // 学年出力
                }
                svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ

            }
            db2.commit();
            if (nonedata == true) {
                //小計行出力
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<4 ; ib++) {
                        if (calc_m3[ia][ib][2]>0) {   //ゼロなら出力しない（指定学年のみ出力）
                            for (int ic=0 ; ic<3 ; ic++) {
                                svf.VrlOutn("CNT"+(ib+1)+"_"+(ic+1),     16, calc_m3[0][ib][ic]);
                                svf.VrlOutn("CNT"+(ib+1)+"_"+(ic+1)+"S", 16, calc_m3[1][ib][ic]);
                            }
                        }
                    }
                }
                set_head(param);
                set_kyugaku(param);
                set_goukei(param);
                svf.VrEndRecord();
                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.warn("set_meisai2 read error!", ex);
        }
    }  //set_meisai2の括り

    public void set_goukei(String param[]) throws ServletException, IOException {
        try {
             String sql = "SELECT "
                    + "TBL3.HR_CLASS,"
                    + "COALESCE(dan_kei,0)              AS CNT5_1,"
                    + "COALESCE(jo_kei,0)               AS CNT5_2,"
                    + "COALESCE(kei,0)                  AS CNT5_3,"
                    + "COALESCE(ya_dan_kei,0)           AS CNT5_1S,"
                    + "COALESCE(ya_jo_kei,0)            AS CNT5_2S,"
                    + "COALESCE(ya_kei,0)               AS CNT5_3S "
                + "FROM "
                    + "("
                        + "SELECT DISTINCT "
                            + "HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR       =  '" +  param[0] + "' "
                            + "AND SEMESTER   =  '" +  param[2] + "' "
                    + ") TBL3 "

                    + "LEFT JOIN ("
                        + "SELECT "
                            + "Z.HR_CLASS,"
                            + "Sum(CASE SEX WHEN '1' THEN 1 ELSE 0 END) AS dan_kei,"
                            + "Sum(CASE SEX WHEN '2' THEN 1 ELSE 0 END) AS jo_kei,"
                            + "COUNT(*) AS kei "
                        + "FROM "
                            + "SCHREG_REGD_HDAT Z1,"
                            + "SCHREG_REGD_DAT Z,"
                            + "SCHREG_BASE_MST M "
                        + "WHERE "
                                + "Z.YEAR       =  '" +  param[0] + "' "
                            + "AND Z.SEMESTER   =  '" +  param[2] + "' "
                            + "AND Z.GRADE BETWEEN '01' AND '06' "                            
                            + "AND Z.YEAR       = Z1.YEAR "
                            + "AND Z.SEMESTER   = Z1.SEMESTER "
                            + "AND Z.GRADE      = Z1.GRADE "
                            + "AND Z.HR_CLASS   = Z1.HR_CLASS "
                            + "AND Z.SCHREGNO   = M.SCHREGNO "
                            + "AND M.SEX        IN ('1','2') "
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.GRD_DATE  < '" +  param[7] + "' "//NO004
                                            + "AND T1.GRD_DIV   IN ('1', '2','3') "   //卒業生・転学者・退学者は含まない
                                    + ") "
//NO005===>
                            + "AND NOT EXISTS ( "
                                        + "SELECT "
                                            + "'X' "
                                        + "FROM "
                                            + "SCHREG_BASE_MST T1 "
                                        + "WHERE "
                                                + "T1.SCHREGNO  = Z.SCHREGNO "
                                            + "AND T1.ENT_DATE  > '" +  param[7] + "' "
                                            + "AND T1.ENT_DIV   IN ('4','5') "   //編入者・転入者は含まない
                                    + ") "
//NO005<===
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL1 ON (TBL1.HR_CLASS = TBL3.HR_CLASS) "

                    + "LEFT JOIN ("
                        + "SELECT "
                            + "Z.HR_CLASS,"
                            + "Sum(CASE SEX WHEN '1' THEN 1 ELSE 0 END) AS ya_dan_kei,"
                            + "Sum(CASE SEX WHEN '2' THEN 1 ELSE 0 END) AS ya_jo_kei,"
                            + "COUNT(*) AS ya_kei "
                        + "FROM "
                            + "SCHREG_REGD_DAT Z "
                        + "LEFT JOIN "
                            + "SCHREG_TRANSFER_DAT D ON ( Z.SCHREGNO = D.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) "
                        + "INNER JOIN "
                            + "SCHREG_REGD_HDAT Z1 ON ( Z.YEAR = Z1.YEAR AND Z.SEMESTER = Z1.SEMESTER AND Z.GRADE = Z1.GRADE AND Z.HR_CLASS = Z1.HR_CLASS ) "
                        + "WHERE "
                                + "Z.YEAR               =  '" +  param[0] + "'"
                            + "AND Z.SEMESTER           =  '" +  param[2] + "' "
                            + "AND M.SEX                IN ('1','2') "
                            + "AND D.TRANSFERCD         IN ('1','2') "
                            + "AND '" +  param[7] + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE "
                        + "GROUP BY Z.HR_CLASS "
                    + ")TBL2 ON (TBL2.HR_CLASS = TBL3.HR_CLASS) "

                + "ORDER BY "
                    + "TBL3.HR_CLASS";

            log.debug("set_goukei sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            /** SVFフォームへデータをセット **/
            int calc_m1[][][] = new int[2][5][3];   //クラス行
            int calc_m3[][][] = new int[2][5][3];   //小計行

            //レコードが真の間ループ
            for (int i=1 ; rs.next() ; i++) {
                //改ページ
                if (i > 15) {
                    set_head(param);
                    svf.VrEndPage();
                    i=1;
                }
                //クラス行初期化
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m1[ia][ib][ic] = 0;
                        }
                    }
                }
                //明細データ取得
                for (int ib=0 ; ib<3 ; ib++) {
                    calc_m1[0][4][ib] = rs.getInt("CNT5_"+(ib+1));
                    calc_m1[1][4][ib] = rs.getInt("CNT5_"+(ib+1)+"S");
                }
                //小計累積
                for (int ia=0 ; ia<2 ; ia++) {
                    for (int ib=0 ; ib<5 ; ib++) {
                        for (int ic=0 ; ic<3 ; ic++) {
                            calc_m3[ia][ib][ic] =  calc_m3[ia][ib][ic] +  calc_m1[ia][ib][ic];
                        }
                    }
                }
                //明細出力
                //合計出力
                for (int ic=0 ; ic<3 ; ic++) {
                    svf.VrlOutn("CNT5"+"_"+(ic+1),     i, calc_m1[0][4][ic]);
                    svf.VrlOutn("CNT5"+"_"+(ic+1)+"S", i, calc_m1[1][4][ic]);
                }
                svf.VrsOutn("GRADE" , 5, "合　計");     // 学年出力
                svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
            if (nonedata == true) {
                //小計行出力
                for (int ic=0 ; ic<3 ; ic++) {
                    svf.VrlOutn("CNT5"+"_"+(ic+1),     16, calc_m3[0][4][ic]);
                    svf.VrlOutn("CNT5"+"_"+(ic+1)+"S", 16, calc_m3[1][4][ic]);
                }
            }
        } catch( Exception ex ) {
            log.warn("set_goukei read error!", ex);
        }
    }  //set_goukeiの括り

    /*------------------------------------*
     * 　留学、休学者ＳＶＦ出力　　       *
     *------------------------------------*/
    public void set_kyugaku(String param[]) throws ServletException, IOException {
        try {
            String sql = new String();
            sql = "SELECT "
                    + "Z.GRADE,"
                    + "Z.HR_CLASS,"
                    + "M.NAME_SHOW,"
                    + "ST3.HR_NAMEABBV,"
                    + "CASE "
                        + "D.TRANSFERCD "
                            + "WHEN '1' THEN '留学者:' "
                            + "WHEN '2' THEN '休学者:' "
                            + "END "
                            + "AS NAIYOU "
                + "FROM "
                    + "SCHREG_REGD_DAT Z "
                + "LEFT JOIN "
                    + "SCHREG_TRANSFER_DAT D ON ( Z.SCHREGNO = D.SCHREGNO ) "
                + "INNER JOIN "
                    + "SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) "
                + "INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER "
                                                + "AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS "
                + "WHERE "
                        + "Z.YEAR               =  '" +  param[0] + "' "
                    + "AND Z.SEMESTER           =  '" +  param[2] + "' "
                    + "AND M.SEX                IN ('1','2') "
                    + "AND D.TRANSFERCD         IN ('1','2') "
                    + "AND '" +  param[7] + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE "
                + "ORDER BY "
                    + "Z.GRADE, "
                    + "Z.HR_CLASS, "
                    + "Z.SCHREGNO";

            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            log.debug("set_kyugaku sql ok!");

            /** SVFフォームへデータをセット **/
            //備考行
            for (int i=1 ; rs.next() ; i++) {
                //備考データ出力
                svf.VrsOutn("HR_CLASS"   ,i , "(" + rs.getString("HR_NAMEABBV") + ")"); // 学年-クラス
                svf.VrsOutn("NAME"       ,i , rs.getString("NAME_SHOW"));   // 氏名
                svf.VrsOutn("kubun"      ,i , rs.getString("NAIYOU"));      // 留学者か休学者か
                svf.VrEndRecord();
                nonedata = true ; //該当データなしフラグ
            }
            db2.commit();
        } catch( Exception ex ) {
            log.warn("set_kyugaku read error!", ex);
        }
    }  //set_kyugakuの括り

    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head(String param[]) {
        try {
            // 年度
            svf.VrsOut("nendo", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");

            // 作成日
            svf.VrsOut("TODAY", KNJ_EditDate.h_format_JP(param[3]));

            // 処理日（××日現在）
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(param[7]) + "現在");
        } catch( Exception ex ) {
            log.warn("set_head read error!", ex);
        }
    }  //set_headの括り
}

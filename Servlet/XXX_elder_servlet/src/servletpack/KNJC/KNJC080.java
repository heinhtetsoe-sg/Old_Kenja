package servletpack.KNJC;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_ClassCode;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＣ０８０＞ 勤怠集計表 [全体資料]
 *                                       (科目別勤怠集計表・月別勤怠集計表・本日の勤怠状況)
 *
 *  2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
 *                      ・本日の遅刻において科目を出力。
 *  2004/05/10 yamashiro・ＳＱＬ文を改良
 *  2004/08/24 yamashiro・組名称に"組"がない場合に対処-->組名称と出席番号の間に"-"を挿入
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJC080 implements KNJ_ClassCode {

    private static final Log log = LogFactory.getLog(KNJC080.class);

    private static String RADIO_1 = "1"; // 科目別出欠集計表
    private static String RADIO_2 = "2"; // 月別出欠集計表
    private static String RADIO_3 = "3"; // 本日の出欠状況

    private static String CODE1_1_CHIKOKU = "1"; // 遅刻
    private static String CODE1_2_KEKKA = "2"; // 欠課
    private static String CODE1_3_SHUKKETSU = "3"; // 出欠


    private Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB db2;                     //Databaseクラスを継承したクラス
    private boolean nonedata;               //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
                /*
                    0   RADIO   1
                    1   YEAR    2002                [7]作成日
                    2   DATE1   2002/04/08          [8]sql作成フラグ
                    3   DATE2   2002/08/08
                    4   OUTPUT1 1  (null or 1)
                    5   OUTPUT2 1  (null or 1)
                    6   OUTPUT3 1  (null or 1)
                        DBNAME  gakumudb
                    9   SEMESTER
                */

        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJC080]DB2 open error!  ", ex);
        }

        Param param = new Param(request, db2);

        //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //  svf設定
        svf.VrInit();                    //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定


        //  ＳＶＦ作成処理
        nonedata  = false; //該当データなしフラグ
        KNJServletUtils.debugParam(request, log);

        if (RADIO_1.equals(param._radio)) {
            svf.VrSetForm("KNJC080_1.frm", 4);    //科目別勤怠集計表
            detail1(param);
        } else if (RADIO_2.equals(param._radio)) {
            svf.VrSetForm("KNJC080_2.frm", 4);    //月別勤怠集計表
            detail2(param);
        } else if (RADIO_3.equals(param._radio)) {
            svf.VrSetForm("KNJC080_3.frm", 4);    //本日の勤怠状況
            detail3(param);
        }

        //  該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);   // 04/04/06
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }


        //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();            //DBを閉じる
        outstrm.close();        //ストリームを閉じる

    }//doGetの括り



    /*------------------------------------*
     * 共通のＳＱＬの記述
     *------------------------------------*/
    private String presql(final Param param) {

        final StringBuffer sql = new StringBuffer();
        //  生徒表
        sql.append( "WITH SCHREG AS(");
        sql.append(         "SELECT W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W2.HR_NAME ");
        if( param._radio.equals(RADIO_3) )
            sql.append(             ",W1.ATTENDNO,W3.NAME AS SCH_NAME ");
        sql.append(         "FROM   SCHREG_REGD_DAT W1 ");
        sql.append(                 "INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR=W2.YEAR AND W1.SEMESTER=W2.SEMESTER ");
        sql.append(                                     "AND W1.GRADE=W2.GRADE AND W1.HR_CLASS=W2.HR_CLASS ");
        if( param._radio.equals(RADIO_3) )
            sql.append(             "INNER JOIN SCHREG_BASE_MST W3 ON W1.SCHREGNO=W3.SCHREGNO ");
        sql.append(         "WHERE  W1.YEAR='"+param._year+"' AND W1.SEMESTER='"+param._semester+"' ),");
        //  出欠表
        sql.append( "ATTEND AS(");
        if( param._output1!=null || param._output2!=null ){
            //(遅刻・欠課)
            sql.append(     "SELECT CASE WHEN L1.REP_DI_CD='7'THEN " + CODE1_1_CHIKOKU + " ELSE " + CODE1_2_KEKKA +"  END CODE1,");
            sql.append(             "CASE WHEN L1.REP_DI_CD='7'THEN'遅刻'ELSE'欠課'END NAME1,");
            sql.append(             "W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD,");
            sql.append(             "CASE WHEN SUBSTR(W2.SUBCLASSCD,1,2)='"+subject_T+"'THEN'"+subject_T+"00'");
            sql.append(                 "WHEN SUBSTR(W2.SUBCLASSCD,1,2)='"+subject_E+"'THEN'"+subject_E+"00'");
            sql.append(                 "WHEN SUBSTR(W2.SUBCLASSCD,1,2)='"+subject_L+"'THEN'"+subject_L+"00'");
            sql.append(                 "ELSE W2.SUBCLASSCD END AS CODE2,");
            sql.append(             "CASE WHEN SUBSTR(W2.SUBCLASSCD,1,2) IN('"+subject_T+"','"+subject_E+"','"+subject_L+"') THEN W4.CLASSNAME ");
            sql.append(                 "ELSE W3.SUBCLASSNAME END AS NAME2 ");
            if( param._radio.equals(RADIO_3) )
                sql.append(         ",W1.DI_REMARK ");
            sql.append(     "FROM   ATTEND_DAT W1 ");
            sql.append("            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ");
            // 04/05/10 sql.append(             "INNER JOIN SCHREG W5 ON W5.SCHREGNO=W1.SCHREGNO ");
            sql.append(             "INNER JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND W2.CHAIRCD=W1.CHAIRCD ");
            sql.append(             "LEFT JOIN SUBCLASS_MST W3 ON W3.SUBCLASSCD=W2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("       AND W3.CLASSCD = W2.CLASSCD ");
                sql.append("       AND W3.SCHOOL_KIND = W2.SCHOOL_KIND ");
                sql.append("       AND W3.CURRICULUM_CD = W2.CURRICULUM_CD ");
            }
            sql.append(             "LEFT JOIN CLASS_MST W4 ON W4.CLASSCD=W2.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("       AND W4.SCHOOL_KIND = W2.SCHOOL_KIND ");
            }
            sql.append(     "WHERE  W1.YEAR = '" + param._year + "' ");
            sql.append(             "AND W1.ATTENDDATE BETWEEN'"+param._date1+"'AND'"+param._date2+"' ");
            if( param._output1!=null && param._output2!=null )
                sql.append(         "AND L1.REP_DI_CD IN('4','5','6','7') ");
            if( param._output1!=null && param._output2==null )
                sql.append(         "AND L1.REP_DI_CD='7' ");
            if( param._output1==null && param._output2!=null )
                sql.append(         "AND L1.REP_DI_CD IN('4','5','6') ");
            sql.append(             "AND ((SUBSTR(W2.SUBCLASSCD,1,2) BETWEEN'"+subject_D+"'AND'"+subject_U+"') ");
            sql.append(                 "OR (SUBSTR(W2.SUBCLASSCD,1,2) IN('"+subject_T+"','"+subject_E+"','"+subject_L+"')) ");
            sql.append(                 "OR (W1.PERIODCD <> '0' AND SUBSTR(W2.SUBCLASSCD,1,2)='"+subject_S+"')) ");
            sql.append(             "AND NOT EXISTS(SELECT 'X' FROM    ATTEND_DAT F1 ");
            sql.append("                                               LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = F1.YEAR AND L1.DI_CD = F1.DI_CD ");
            sql.append(                             "WHERE ATTENDDATE BETWEEN'"+param._date1+"'AND'"+param._date2+"'");
            sql.append(                                     "AND ((F1.PERIODCD='1' AND L1.REP_DI_CD IN ('8','9','10','11','12','13' ");
            if ("true".equals(param._useVirus)) {
                sql.append(                                     " ,'20' ");
            }
            if ("true".equals(param._useKoudome)) {
                sql.append(                                     " ,'26' ");
            }
            sql.append(                                                                           ")) ");
            sql.append(                                         "OR (F1.PERIODCD<>'1'AND L1.REP_DI_CD IN('2','3'");
            if ("true".equals(param._useVirus)) {
                sql.append(                                     " ,'19' ");
            }
            if ("true".equals(param._useKoudome)) {
                sql.append(                                     " ,'25' ");
            }
            sql.append(                                            "))) ");
            sql.append(                                     "AND F1.SCHREGNO=W1.SCHREGNO AND F1.ATTENDDATE=W1.ATTENDDATE) ");
        }//(遅刻・欠課)のSQL

        if( param._output3!=null ){
                //(欠席)
            if( param._output1!=null || param._output2!=null )
                sql.append( "UNION ");
            sql.append(     "SELECT " + CODE1_3_SHUKKETSU + " AS CODE1,'欠席' AS NAME1,W1.SCHREGNO,W1.ATTENDDATE,MIN(W1.PERIODCD) AS PERIODCD,");
            sql.append(             "CASE W3.REP_DI_CD WHEN'9'THEN'2'WHEN'10'THEN'3'WHEN'11'THEN'4'WHEN'12'THEN'5'WHEN'13'THEN'6' ");
            if ("true".equals(param._useVirus)) {
                sql.append(                                     " WHEN'20'THEN'19' ");
            }
            if ("true".equals(param._useKoudome)) {
                sql.append(                                     " WHEN'26'THEN'25' ");
            }
            sql.append(             "ELSE W3.REP_DI_CD END AS CODE2,");
            sql.append(             "VALUE(W3.DI_NAME1,'') AS NAME2 ");
            if( param._radio.equals(RADIO_3) )
                sql.append(         ",MAX(W1.DI_REMARK) AS DI_REMARK ");
            sql.append(     "FROM   ATTEND_DAT W1 ");
            // 04/05/10 sql.append(             "INNER JOIN SCHREG W5 ON W5.SCHREGNO=W1.SCHREGNO ");
            sql.append(             "LEFT JOIN ATTEND_DI_CD_DAT W3 ON W3.YEAR = W1.YEAR AND W3.DI_CD = W1.DI_CD ");
            sql.append(     "WHERE  W1.YEAR='"+param._year+"'");
            sql.append(             "AND W1.ATTENDDATE BETWEEN'"+param._date1+"'AND'"+param._date2+"'");
            sql.append(             "AND W3.REP_DI_CD IN ('2','3','9','10','11','12','13'");
            if ("true".equals(param._useVirus)) {
                sql.append(                                     " ,'19' ,'20' ");
            }
            if ("true".equals(param._useKoudome)) {
                sql.append(                                     " ,'25' ,'26' ");
            }
            sql.append(             ") ");
            sql.append(     "GROUP BY W1.SCHREGNO,W1.ATTENDDATE,");
            sql.append(             "CASE W3.REP_DI_CD WHEN'9'THEN'2'WHEN'10'THEN'3'WHEN'11'THEN'4'WHEN'12'THEN'5'WHEN'13'THEN'6'");
            if ("true".equals(param._useVirus)) {
                sql.append(                                     " WHEN'20'THEN'19' ");
            }
            if ("true".equals(param._useKoudome)) {
                sql.append(                                     " WHEN'26'THEN'25' ");
            }
            sql.append(             " ELSE W3.REP_DI_CD END,");
            sql.append(             "W3.DI_NAME1");
        }//(欠席)のSQL
        sql.append(         ")");

        if (RADIO_1.equals(param._radio)) {
            String sql1 =
                    "SELECT     T1.CODE1,T1.NAME1,VALUE(T1.CODE2, '0') AS CODE2,T1.NAME2,"
                    +             "VALUE(T2.GRADE,'0') AS GRADE,VALUE(T2.HR_CLASS,'0') AS HR_CLASS,T2.HR_NAME,"
                    +             "COUNT(*) AS COUNT "
                    + "FROM     ATTEND T1,SCHREG T2 "
                    + "WHERE    T1.SCHREGNO = T2.SCHREGNO "
                    + "GROUP BY GROUPING SETS ((T1.CODE1,T1.NAME1),"
                    +                 "(T1.CODE1,T1.NAME1,T1.CODE2,T1.NAME2),"
                    +                "(T1.CODE1,T1.NAME1,T1.CODE2,T1.NAME2,T2.GRADE,T2.HR_CLASS,T2.HR_NAME)) "
                    + "ORDER BY CODE1,CODE2,GRADE,HR_CLASS";
            sql.append(sql1);

        } else if (RADIO_2.equals(param._radio)) {
            String sql2 =
                    "SELECT     T1.CODE1,T1.NAME1,VALUE(T1.CODE2, '0') AS CODE2,NAME2,"
                    +             "VALUE(T2.GRADE,'0') AS GRADE,VALUE(T2.HR_CLASS,'0') AS HR_CLASS,T2.HR_NAME,"
                    +             "VALUE(YEAR(T1.ATTENDDATE),0) AS YEAR,VALUE(MONTH(T1.ATTENDDATE),0) AS MONTH,"
                    +             "COUNT(*) AS COUNT "
                    + "FROM     ATTEND T1,SCHREG T2 "
                    + "WHERE    T1.SCHREGNO = T2.SCHREGNO "
                    + "GROUP BY GROUPING SETS ((T1.CODE1,T1.NAME1),"
                    +                 "(T1.CODE1,T1.NAME1,T1.CODE2,T1.NAME2,YEAR(T1.ATTENDDATE),MONTH(T1.ATTENDDATE)),"
                    +                 "(T1.CODE1,T1.NAME1,T1.CODE2,T1.NAME2,YEAR(T1.ATTENDDATE),MONTH(T1.ATTENDDATE),T2.GRADE,T2.HR_CLASS,T2.HR_NAME)) "
                    + "ORDER BY CODE1,CODE2,GRADE,HR_CLASS,YEAR,MONTH";
            sql.append(sql2);

        } else if (RADIO_3.equals(param._radio)) {
            final String sql3 =
                    "SELECT     T1.CODE1,T1.NAME1,VALUE(T1.CODE2, '0') AS CODE2,NAME2,"
                    +             "VALUE(T2.GRADE,'0') AS GRADE,VALUE(T2.HR_CLASS,'0') AS HR_CLASS,"
                    +             "T2.HR_NAME,T2.SCH_NAME,VALUE(T2.ATTENDNO,'0') AS ATTENDNO,"
                    +             "VALUE(T1.DI_REMARK,'') AS DI_REMARK,"
                    +             "COUNT(DISTINCT T1.SCHREGNO) AS COUNT "
                    + "FROM     ATTEND T1,SCHREG T2 "
                    + "WHERE    T1.SCHREGNO = T2.SCHREGNO "
                    + "GROUP BY GROUPING SETS ((T1.CODE1,T1.NAME1),"
                    +                  "(T1.CODE1,T1.NAME1,T1.CODE2,T1.NAME2,T2.GRADE,T2.HR_CLASS,T2.HR_NAME,T2.ATTENDNO,T2.sch_NAME,T1.DI_REMARK))"
                    + "ORDER BY CODE1,GRADE,HR_CLASS,ATTENDNO,CODE2";
            sql.append(sql3);
        }

        return sql.toString();

    }//presqlの括り

    /*------------------------------------*
     * 科目別勤怠集計表作成
     *------------------------------------*/
    private void detail1(final Param param)
    {
        final StringBuffer sql = new StringBuffer();
        sql.append(presql(param));

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //log.error("[KNJC080]detail2 sql="+sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            log.info("[KNJC080]detail2 sql ok!");
            svf.VrsOut("ymd"          , KNJ_EditDate.h_format_JP(param._ctrlDate));          //作成日
            svf.VrsOut("period_from"  , KNJ_EditDate.h_format_JP(param._date1));          //集計期間開始
            svf.VrsOut("period_to"    , KNJ_EditDate.h_format_JP(param._date2));          //集計期間開始
            svf.VrsOut("ofject"       , "全学年"); //対象

            while( rs.next() ){
                //  CODE1のブレイク
                if( rs.getString("CODE2").equals("0") ){
                    svf.VrsOut("KINTAI" , rs.getString("NAME1"));                     //遅刻・欠課・欠席
                    svf.VrsOut("total"  , rs.getString("COUNT"));                     //回数
                    continue;
                }
                //  科目・欠席勤怠のブレイク
                if( rs.getString("GRADE").equals("0") ){
                    svf.VrsOut("number1"  , rs.getString("COUNT"));                   //回数
                    continue;
                }
                //  明細行
                svf.VrsOut("subject"  , rs.getString("NAME2"));                       //科目・勤怠名称
                svf.VrsOut("HR_NAME"  , rs.getString("HR_NAME"));                     //組名称
                svf.VrsOut("number2"  , rs.getString("COUNT"));                       //回数
                svf.VrEndRecord();
                nonedata  = true;                                                           //該当データなしフラグ
            }
            log.info("[KNJC080]detail2 read ok!");
        } catch (Exception ex) {
            log.error("[KNJC080]detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }//detail1の括り



    /*------------------------------------*
     * 月別勤怠集計表作成
     *------------------------------------*/
    private void detail2(final Param param)
    {
        final StringBuffer sql = new StringBuffer();
        sql.append(presql(param));

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info("[KNJC080]detail1 sql="+sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            log.info("[KNJC080]detail1 sql ok!");
            svf.VrsOut("ymd"  ,KNJ_EditDate.h_format_JP(param._ctrlDate));                   //作成日
            String code1 = "0";     //code1の保存
            String code2 = "0";     //code2の保存
            String GHCLASS_INI = "0";
            String ghclass = GHCLASS_INI;   //学年・組の保存

            while (rs.next()) {
                //  CODE1のブレイク
                if( rs.getString("CODE2").equals("0") ){
                    if( !code1.equals("0") ){
                        svf.VrEndRecord();
                        svf.VrsOut("total_no"  , "");                                 //回数計
                    }
                    code1 = rs.getString("CODE1");
                    code2 = "0";
                    ghclass = GHCLASS_INI;
                    svf.VrsOut("KINTAI"    , rs.getString("NAME1"));                  //種別名
                    svf.VrsOut("total_no"  , rs.getString("COUNT"));                  //回数計
                    continue;
                }
                //  CODE2のブレイク
                if( rs.getString("GRADE").equals("0") ){
                    if( !rs.getString("CODE2").equals(code2) ){
                        if( !code2.equals("0") ){
                            svf.VrEndRecord();
                            svf.VrsOut("total_no"  , "");                             //回数計
                        }
                        code2 = rs.getString("CODE2");
                        ghclass = GHCLASS_INI;
                    }
                    int mi = rs.getInt("MONTH");                                            //月
                    svf.VrsOut("sum1_"+mi  , rs.getString("COUNT"));                  //回数計
                    continue;
                }
                //  明細行
                String g_h_class = rs.getString("GRADE") + "" + rs.getString("HR_CLASS");
                if( !g_h_class.equals(ghclass) ){
                    if( !ghclass.equals(GHCLASS_INI) ){
                        svf.VrEndRecord();
                        svf.VrsOut("total_no"  , "");                                 //回数計
                    } else{
                        svf.VrsOut("subject1" , rs.getString("NAME2"));               //科目・勤怠名称
                    }
                    svf.VrsOut("subject2" , rs.getString("NAME2"));                   //科目・勤怠名称
                    svf.VrsOut("HR_NAME"  , rs.getString("HR_NAME"));                 //組名称
                    ghclass = g_h_class;
                }
                int mi = rs.getInt("MONTH");                                                //月
                svf.VrsOut("sum2_" + mi, rs.getString("COUNT"));                      //回数計
                nonedata  = true;                                                           //該当データなしフラグ
            }
            if (!ghclass.equals(GHCLASS_INI)) {
                svf.VrEndRecord();
                nonedata  = true;                                                           //該当データなしフラグ
            }
            log.info("[KNJC080]detail1 read ok!");
        } catch (Exception ex) {
            log.error("[KNJC080]detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }//detail2の括り



    /*------------------------------------*
     * 本日の勤怠状況作成
     *------------------------------------*/
    private void detail3(final Param param)
    {
        final StringBuffer sql = new StringBuffer();
        sql.append(presql(param));

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //log.error("[KNJC080]detail3 sql="+sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            log.info("[KNJC080]detail3 sql ok!");
            svf.VrsOut("ymd"          , KNJ_EditDate.h_format_JP(param._ctrlDate));          //作成日
            svf.VrsOut("date"         , KNJ_EditDate.h_format_JP(param._date1));          //集計期間開始
            svf.VrsOut("object"       , "全校");      //対象

            String strx = null;
            while (rs.next()) {
            //  CODE1のブレイク
                if (rs.getString("CODE2").equals("0")) {
                    svf.VrsOut("KUBUN"  , rs.getString("NAME1"));                     //種別名
                    svf.VrsOut("total"  , rs.getString("COUNT"));                     //回数計
                    continue;
                }
            //  組名称の編集 04/08/24追加
                try {
                    if (rs.getString("HR_NAME").lastIndexOf("組") > -1) {
                        strx = rs.getString("HR_NAME") + rs.getInt("ATTENDNO") + "番";
                    } else {
                        strx = rs.getString("HR_NAME") + "-" + rs.getInt("ATTENDNO") + "番";
                    }
                } catch (NullPointerException ex) {
                    log.error("[KNJC080]detail3 HR_NAME error!", ex);
                    strx = "";
                }
            //  明細行
                if (!rs.getString("CODE1").equals(CODE1_3_SHUKKETSU)) {
                    svf.VrsOut("subject"      , rs.getString("NAME2"));               //科目・勤怠名称
                    svf.VrsOut("HR_NAME2"     , strx);                                //学年
                    svf.VrsOut("name2"        , rs.getString("SCH_NAME"));            //氏名
                    svf.VrsOut("note2"        , rs.getString("DI_REMARK"));           //備考
                } else {
                    svf.VrsOut("HR_NAME1"     , strx);                                //学年
                    svf.VrsOut("name1"        , rs.getString("SCH_NAME"));            //氏名
                    svf.VrsOut("note1"        , rs.getString("DI_REMARK"));           //備考
                }
                svf.VrEndRecord();
                nonedata  = true;                                                           //該当データなしフラグ
            }
            log.info("[KNJC080]detail3 read ok!");
        } catch( Exception ex ) {
            log.error("[KNJC080]detail3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }//detail3の括り

    private static class Param {
        String _radio;
        final String _year;
        String _date1;
        String _date2;
        final String _output1;
        final String _output2;
        final String _output3;
        String _ctrlDate;
        final String _semester;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            //  パラメータの取得
            _radio = request.getParameter("RADIO");           //帳票種別
            if (_radio == null) {
                _radio = RADIO_1;
            }
            _year = request.getParameter("YEAR");            //年度
            _semester = request.getParameter("SEMESTER");        //学期

            if (!_radio.equals(RADIO_2)) {    //--->月別以外
                KNJ_EditDate editdate = new KNJ_EditDate();                                 //クラスのインスタンス作成
                _date1 = editdate.h_format_sec(request.getParameter("DATE1"));            //印刷範囲開始
                if (_radio.equals(RADIO_1)) {  //--->科目別のみ
                    _date2 = editdate.h_format_sec(request.getParameter("DATE2"));            //印刷範囲開始
                }
            }

            _output1 = request.getParameter("OUTPUT1");         //遅刻データ出力
            _output2 = request.getParameter("OUTPUT2");         //欠課データ出力
            _output3 = request.getParameter("OUTPUT3");         //欠席データ出力
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            //  作成日(現在処理日)の取得
            try {
                KNJ_Control control = new KNJ_Control();                            //クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = control.Control(db2);
                _ctrlDate = returnval.val3;                                          //現在処理日
            } catch (Exception e) {
                log.error("[KNJC080]ctrl_date get error!", e);
            }


            //  範囲日付の取得(科目別以外)
            if (_radio.equals(RADIO_3)) {
                _date2 = _date1;            //--->本日
            }
            if (_radio.equals(RADIO_2)) {         //--->年間
                try {
                    KNJ_Semester semester = new KNJ_Semester();                         //クラスのインスタンス作成
                    KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, "9");
                    _date1 = returnval.val2;                                          //学年開始日
                    _date2 = returnval.val3;                                          //学年終了日
                } catch (Exception e) {
                    log.error("[KNJC080]Semester get error!", e);
                }
            }
        }
    }

}//クラスの括り

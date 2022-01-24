// kanji=漢字
/*
 * $Id: 9a31fe689fbc6592faca73182bc93148d57de206 $
 *
 * Copyright(C) 2007-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [指導情報管理]
 *
 *                  ＜ＫＮＪＨ０４０＞  生徒調査（高等学校）
 *
 *      ＊高校用と中学校用との相違個所 param._4〜[7]
 *
 * 2004/08/18 nakamoto 組のデータ型が数値でも文字でも対応できるようにする
 *  2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加  --NO003
 *                            => 無い場合は従来通りHR_CLASSを出力
 * 2006/06/20 m-yama   NO004 SCHREG_ENVIR_DATの変更に伴う修正
 * 2006/07/05 m-yama   NO005 出身学校の固定文字を中高で切替える。
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH040 {

    private boolean nonedata, nonedata2;    //該当データなしフラグ

    private static final Log log = LogFactory.getLog(KNJH040.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws IOException
    {

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        // svf設定
        svf.VrInit();                          //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJH040]DB2 open error!", ex);
        }

        try {
            log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            Param param = new Param(request, db2);

            /*-----------------------------------------------------------------------------
                ＳＶＦ作成処理       
              -----------------------------------------------------------------------------*/
            nonedata = false;

            //クラス毎の出力
            StringTokenizer sttoken = new StringTokenizer(param._gakunen,",",false);    //学年
            while (sttoken.hasMoreTokens()) {
                param._tmpGrade = sttoken.nextToken();
                param._tmpGradeCd = param.getGradeCd(db2, param._tmpGrade);
                param._tmpGradeCdIntString = (NumberUtils.isDigits(param._tmpGradeCd) ? Integer.valueOf(param._tmpGradeCd).toString() : "");
                param._tmpSchoolKind = param.getSchoolKind(db2, param._tmpGrade);
                param._tmpSeito = "生徒";
                if ("H".equals(param._tmpSchoolKind)) {
                    param._tmpFinschoolkindname = "中学校";
                } else if ("J".equals(param._tmpSchoolKind)) {
                    param._tmpFinschoolkindname = "小学校";
                } else if ("P".equals(param._tmpSchoolKind)){
                    param._tmpSeito = "児童";
                    param._tmpFinschoolkindname = "幼稚園";
                } else {
                    param._tmpFinschoolkindname = "学校";
                }
                set_detail1(svf, db2, param);       //出身中学校別生徒数
                set_detail2(svf, db2, param);       //通学方法別生徒数
                set_detail3(svf, db2, param);       //兄弟姉妹調査
                set_detail4(svf, db2, param);       //住居調査
                set_detail5(svf, db2, param);       //保護者の職業調査
                if (nonedata2 == true)  {
                    nonedata = true;
                }
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無し
            if (nonedata == false) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndRecord();
                svf.VrEndPage();
            }
            
            // 終了処理
            db2.close();        // DBを閉じる
            svf.VrQuit();
            outstrm.close();    // ストリームを閉じる 
        }

    }    //doGetの括り

    /*------------------------------------*
     * 出身中学校別生徒数　SVF出力        *
     *------------------------------------*/
    private void set_detail1(final Vrw32alp svf, final DB2UDB db2, final Param param)
    {
        boolean nonedata2 = false;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "VALUE(T1.GRADE,'9')                  AS GRADE,"
                    + "VALUE(T3.DISTRICTCD,'total')         AS DISTRICT_CD,"
                    + "VALUE(T3.DISTRICTNAME,'total')       AS DISTRICT_NAME,"
                    + "VALUE(T3.FINSCHOOLCD,'total')    AS J_CD,"
                    + "VALUE(T3.FINSCHOOL_NAME,'合計')    AS J_NAME,"
                    + "SUM(CASE WHEN T1.SEX = '1' THEN 1 ELSE 0 END) AS CNT_1,"
                    + "SUM(CASE WHEN T1.SEX = '2' THEN 1 ELSE 0 END) AS CNT_2,"
                    + "COUNT(T1.SCHREGNO) AS CNT_3 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "ST1.FINSCHOOLCD,"
                            + "ST1.FINSCHOOL_NAME,"
                            + "CASE WHEN ST1.DISTRICTCD IS NULL THEN 'X' WHEN ST1.DISTRICTCD = '' THEN 'X' "
                                + "ELSE ST1.DISTRICTCD END AS DISTRICTCD,"
                            + "VALUE(ST2.NAME1,' ')     AS DISTRICTNAME "
                        + "FROM "
                            + "FINSCHOOL_MST ST1 "
                            + "LEFT JOIN NAME_MST ST2 ON ST2.NAMECD1 = 'Z003' AND ST1.DISTRICTCD = ST2.NAMECD2 "
                    + ") T3 "

                    + "INNER JOIN("
                        + "SELECT "
                            + "ST2.SCHREGNO,"
                            + "ST2.FINSCHOOLCD,"
                            + "ST2.SEX,"
                            + "ST1.GRADE "
                        + "FROM "
                            + "SCHREG_REGD_HDAT ST3 "
                            + "INNER JOIN SCHREG_REGD_DAT ST1 ON ST1.YEAR     = ST3.YEAR "
                            + "AND ST1.SEMESTER = ST3.SEMESTER "
                            + "AND ST1.GRADE    = ST3.GRADE "
                            + "AND ST1.HR_CLASS = ST3.HR_CLASS "
                            + "INNER JOIN SCHREG_BASE_MST ST2 ON ST1.SCHREGNO = ST2.SCHREGNO "
                        + "WHERE "
                                + "ST1.YEAR     = '" +  param._year + "' "
                            + "AND ST1.SEMESTER = '" +  param._semester + "' "
                            + "AND ST1.GRADE    = '" +  param._tmpGrade + "' "
                            + "AND ST2.FINSCHOOLCD IS NOT NULL "
                    + ") T1 ON T3.FINSCHOOLCD = T1.FINSCHOOLCD "

                + "GROUP BY "
                + "GROUPING SETS "
                    + "((T1.GRADE,T3.DISTRICTCD,T3.DISTRICTNAME),"
                        + "(T1.GRADE,T3.DISTRICTCD,T3.DISTRICTNAME,T3.FINSCHOOLCD,T3.FINSCHOOL_NAME))"
                + "ORDER BY 1,2,4";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH040]set_detail1 sql ok!");

            // SVF-formへデータを出力
            svf.VrSetForm("KNJH040_1.frm", 4);      //svf-form
            
            svf.VrsOut("SCHOOL", param._nendo + "  " + param._tmpGradeCdIntString + "学年  出身" + param._tmpFinschoolkindname + "別" + param._tmpSeito + "数");

            String f_grade = "";            //学年コード
            String f_districtcd = "";       //地域コード
            int line = 0;
            int namecheck = 0;
            String nameSet;

            while (rs.next()) {
                //学年コードのブレイク
                if (f_grade == null || f_grade.equals(rs.getString("GRADE")) == false) {
                    f_grade = rs.getString("GRADE");            //学年コード
                    f_districtcd = "";                          //地域コード
                }
                //地域コードのブレイク
                if (f_districtcd == null || f_districtcd.equals(rs.getString("DISTRICT_CD")) == false) {
                    svf.VrsOut("AREA" , rs.getString("DISTRICT_NAME")); //地域名
                    f_districtcd = rs.getString("DISTRICT_CD");         //地域コード
                    line = 0;
                }
                //明細出力
                if (rs.getString("J_CD").equalsIgnoreCase("total")) {
                    int line2 = 40;
                    if (line > 40-1)    line2 = 81;
                    while (line < line2-1) {
                        svf.VrsOut("SCHOOLNAME"     , " "); //学校名
                        svf.VrEndRecord();
                        line++;
                    }
                }
                nameSet = rs.getString("J_NAME");
                namecheck = rs.getString("J_NAME").indexOf("　");
                if ((namecheck + 1) <= 6) {
                    nameSet = rs.getString("J_NAME").substring(namecheck + 1);
                }
                svf.VrsOut("SCHOOLNAME"     , nameSet); //学校名
                svf.VrsOut("BOY"            , rs.getString("CNT_1"));   //男
                svf.VrsOut("GIRL"       , rs.getString("CNT_2"));   //女
                svf.VrsOut("SUBTOTAL"   , rs.getString("CNT_3"));   //計
                svf.VrEndRecord();
                line++;
                if (line == 81) line = 0;
                nonedata2 = true;
            }
            log.debug("[KNJH040]set_detail1 read ok!");
        } catch (Exception ex) {
            log.error("[KNJH040]set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        if (nonedata2 == true) {
            svf.VrPrint();
            nonedata = true;
        }

    }  //set_detail1の括り

    /*------------------------------------*
     * 通学方法別生徒数　SVF出力         *
     *------------------------------------*/
    private void set_detail2(final Vrw32alp svf, final DB2UDB db2, final Param param)
    {
        int howto_number = 0;

        //通学方法の数を取得
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "COUNT(NAMECD2)+1 "
                + "FROM "
                    + "NAME_MST "
                + "WHERE "
                    + "NAMECD1 = 'H100'";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                howto_number = rs.getInt(1);
            }
            log.debug("[KNJH040]set_detail2 howto_number get ok!");

        } catch (Exception ex) {
            log.error("[KNJH040]set_detail2 howto_number get error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        //統計データ取得及びＳＶＦ出力
        try {
            String sql = "";
            sql = "SELECT "
                    + "VALUE(T3.HR_CLASS,'TOTAL') AS HR_CLASS,"
                    + ("1".equals(param.useSchregRegdHdat) ? "HR_CLASS_NAME1," : "")
                    + "VALUE(T1.NAMECD,'TOTAL') AS NAMECD,"
                    + "VALUE(T1.NAME,'合計') AS NAME,"
                    + "VALUE(T1.AREA_NAMECD,'TOTAL') AS AREA_NAMECD,"
                    + "VALUE(T1.AREA_NAME,'合計') AS AREA_NAME,"
                    + "VALUE(SUM(CASE WHEN SEX='1' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT1,"
                    + "VALUE(SUM(CASE WHEN SEX='2' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT2,"
                    + "VALUE(SUM(SCH_CNT),0) AS SCH_CNT3 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "'X' AS HELEN,"
                            + "HR_CLASS "
                            + ("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1 " : "")
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR = '" +  param._year + "' "
                            + "AND SEMESTER = '" +  param._semester + "' "
                            + "AND GRADE = '" +  param._tmpGrade + "' "
                    + ")T3 "
                
                    + "LEFT JOIN("
                        + "SELECT "
                            + "'X' AS JHON,"
                            + "T1.NAMECD2 AS NAMECD,"
                            + "T1.NAME1 AS NAME,"
                            + "T6.NAMECD2 AS AREA_NAMECD,"
                            + "T6.NAME1 AS AREA_NAME "
                        + "FROM "
                            + "("
                                + "SELECT "
                                    + "'X' AS JHON,"
                                    + "NAMECD2,"
                                    + "NAME1 "
                                + "FROM "
                                    + "NAME_MST "
                                + "WHERE "
                                    + "NAMECD1 = 'H100' "
                            + ")T1 "
                            + "LEFT JOIN("
                                + "SELECT "
                                    + "'X' AS TOM,"
                                    + "NAMECD2,"
                                    + "NAME1 "
                                + "FROM "
                                    + "NAME_MST "
                                + "WHERE "
                                        + "NAMECD1 = 'A020' "
                                    + "AND NAMECD2 < '5' "  //add 2004/01/15
                            + ")T6 ON T6.TOM = T1.JHON "
                    + ")T1 ON T3.HELEN = T1.JHON "
                
                    + "LEFT JOIN ("
                        + "SELECT "
                            + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
                            + "ST1.HR_CLASS,"
                            + "ST4.HOWTOCOMMUTECD,"
                            + "ST3.AREACD,"
                            + "ST2.SEX "
                        + "FROM "
                            + "SCHREG_REGD_HDAT ST6,"
                            + "SCHREG_REGD_DAT ST1,"
                            + "SCHREG_BASE_MST ST2,"
                            + "SCHREG_ADDRESS_DAT ST3,"
                            + "("
                                + "SELECT "
                                    + "SCHREGNO,"
                                    + "MAX(ISSUEDATE) AS ISSUEDATE "
                                + "FROM "
                                    + "SCHREG_ADDRESS_DAT "
                                + "WHERE "
                                        + "ISSUEDATE    <= '" +  param._semesterEdate + "' "
                                    + "AND EXPIREDATE   >= '" +  param._semesterSdate + "' "
                                + "GROUP BY "
                                    + "SCHREGNO "
                            + ")ST5,"
                            + "SCHREG_ENVIR_DAT ST4 "
                        + "WHERE "
                                + "ST1.YEAR     = '" +  param._year + "' "
                            + "AND ST1.SEMESTER = '" +  param._semester + "' "
                            + "AND ST1.GRADE    = '" +  param._tmpGrade + "' "
                            + "AND ST1.SCHREGNO = ST2.SCHREGNO "
                            + "AND ST1.SCHREGNO = ST3.SCHREGNO "
                            + "AND ST1.SCHREGNO = ST4.SCHREGNO "
                            + "AND VALUE(ST4.HOWTOCOMMUTECD,'0')>'0' "
                            + "AND ST3.SCHREGNO = ST5.SCHREGNO "
                            + "AND ST3.ISSUEDATE = ST5.ISSUEDATE "
                            + "AND VALUE(ST3.AREACD,'0')>'0' "
                            + "AND ST1.YEAR     = ST6.YEAR "
                            + "AND ST1.SEMESTER = ST6.SEMESTER "
                            + "AND ST1.GRADE    = ST6.GRADE "
                            + "AND ST1.HR_CLASS = ST6.HR_CLASS "
                        + "GROUP BY "
                            + "ST1.HR_CLASS,"
                            + "ST4.HOWTOCOMMUTECD,"
                            + "ST3.AREACD,"
                            + "ST2.SEX "
                    + ") T2 ON INT(T2.HOWTOCOMMUTECD) = INT(T1.NAMECD) "
                                + "AND INT(T2.AREACD) = INT(T1.AREA_NAMECD) AND T2.HR_CLASS = T3.HR_CLASS "
                
                + "GROUP BY "
                + "GROUPING SETS "
                    + "((T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+",NAMECD,NAME,AREA_NAMECD,AREA_NAME),"
                        + "(T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+",NAMECD,NAME),"
                        + "(T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+",AREA_NAMECD,AREA_NAME),"
                        + "(NAMECD,NAME,AREA_NAMECD,AREA_NAME),"
                        + "(NAMECD,NAME),"
                        + "(AREA_NAMECD,AREA_NAME),"
                        + "(T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),()) "
                + "ORDER BY "
                    + "T3.HR_CLASS,"
                    + "NAMECD,"
                    + "AREA_NAMECD";

            log.debug("[KNJH040]set_detail2 sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH040]set_detail2 sql ok!");

            // SVF-formへデータを出力 
            svf.VrSetForm("KNJH040_2.frm", 4);      //svf-form
            svf.VrsOut("NENDO", param._nendo);
            svf.VrsOut("GRADE", param._tmpGradeCd);
            svf.VrsOut("SEITO", param._tmpSeito);
             int h_c  = 0;
             int line = 0;
             int h_c_line = 0;

             while (rs.next()) {
                 //地域名称出力
                 if (line == 0) {
                     svf.VrsOutn("NAME" ,h_c+1   ,rs.getString("AREA_NAME"));
                 }
                 //合計出力前の行調整
                 if (rs.getString("HR_CLASS").equalsIgnoreCase("total")  &  h_c_line == 0) {
                     while (line < 44-howto_number) {
                         svf.VrsOut("KUMI"           ,String.valueOf(line)); //組（マスク）
                         svf.VrEndRecord();
                         line++;
                     }
                 }
                 //地域の計
                 if (rs.getString("AREA_NAMECD").equalsIgnoreCase("total")) {
                     svf.VrsOut("KUMI"           ,rs.getString("HR_CLASS")); //組（マスク）
                     //組名称出力
                     if (h_c_line == 0) {
                         if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                             svf.VrsOut("HR_CLASS"   ,"合");
                         } else {
                             String hrName = "";
                             if ("1".equals(param.useSchregRegdHdat)) {
                                 hrName = rs.getString("HR_CLASS_NAME1");
                             } else {
                                 hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), param.hmap);
                             }
                             if ("".equals(hrName) || hrName == null) {
                                 hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"));
                             }
                             svf.VrsOut("HR_CLASS"   , hrName);//2004/08/18  NO003Modify
                         }
                     }
                     if (h_c_line == 1) {
                         if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                             svf.VrsOut("HR_CLASS"   ,"計");
                         } else {
                             svf.VrsOut("HR_CLASS"   ,"組");
                         }
                     }
                     svf.VrsOut("METHOD1"        ,rs.getString("NAME"));     //通学方法名称
                     svf.VrlOut("TOTAL1"         ,rs.getInt("SCH_CNT3"));    //計人数
                     svf.VrEndRecord();
                     nonedata2 = true;
                     line++;
                     if (line == 44) line = 0;
                     h_c_line++;
                     h_c = 0;
                 } else {
                     h_c++;
                     svf.VrlOut("BOY1_"+h_c      ,rs.getInt("SCH_CNT1"));    //男子人数
                     svf.VrlOut("GIRL1_"+h_c     ,rs.getInt("SCH_CNT2"));    //女子人数
                 }
                 //組のブレイク
                 if (rs.getString("NAMECD").equalsIgnoreCase("total")
                         & rs.getString("AREA_NAMECD").equalsIgnoreCase("total")) {
                     h_c_line = 0;
                 }
            }
            log.debug("[KNJH040]set_detail2 read ok!");

        } catch (Exception ex) {
            log.error("[KNJH040]set_detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail2の括り

    /*------------------------------------*
     * 兄弟姉妹調査　SVF出力               *
     *------------------------------------*/
    private void set_detail3(final Vrw32alp svf, final DB2UDB db2, final Param param)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "VALUE(NAMECD2,'total') AS NAMECD,"
                    + "VALUE(NAME1,'合計') AS NAME,"
                    + "VALUE(T3.HR_CLASS,'total') AS HR_CLASS,"
                    + ("1".equals(param.useSchregRegdHdat) ? "T3.HR_CLASS_NAME1," : "")
                    + "VALUE(SUM(CASE WHEN SEX='1' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT1,"
                    + "VALUE(SUM(CASE WHEN SEX='2' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT2,"
                    + "VALUE(SUM(SCH_CNT),0) AS SCH_CNT3 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "'X' AS HELEN,"
                            + "HR_CLASS "
                            + ("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1 " : "")
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR = '" + param._year + "' "
                            + "AND SEMESTER = '" + param._semester + "' "
                            + "AND GRADE = '" + param._tmpGrade + "' "
                    + ")T3 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "'X' AS JHON,"
                            + "NAMECD2,"
                            + "NAME1 "
                        + "FROM "
                            + "NAME_MST "
                        + "WHERE "
                                + "NAMECD1 = 'H107' "
                            + "AND NAMECD2 < '7' "  //add 2004/01/15
                    + ")T1 ON T3.HELEN = T1.JHON "

                    + "LEFT JOIN ("
                        + "SELECT "
                            + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
                            + "ST4.BRO_SISCD,"
                            + "ST1.HR_CLASS,"
                            + "ST2.SEX "
                        + "FROM "
                            + "SCHREG_REGD_HDAT ST6,"
                            + "SCHREG_REGD_DAT ST1,"
                            + "SCHREG_BASE_MST ST2,"
                            + "SCHREG_ENVIR_DAT ST4 "
                        + "WHERE "
                                + "ST1.YEAR     = '" + param._year + "' "
                            + "AND ST1.SEMESTER = '" + param._semester + "' "
                            + "AND ST1.GRADE    = '" + param._tmpGrade + "' "
                            + "AND ST1.SCHREGNO = ST2.SCHREGNO "
                            + "AND ST1.SCHREGNO = ST4.SCHREGNO "
                            + "AND VALUE(ST4.BRO_SISCD,'0')>'0' "
                            + "AND ST1.YEAR     = ST6.YEAR "
                            + "AND ST1.SEMESTER = ST6.SEMESTER "
                            + "AND ST1.GRADE    = ST6.GRADE "
                            + "AND ST1.HR_CLASS = ST6.HR_CLASS "
                        + "GROUP BY "
                            + "ST4.BRO_SISCD,"
                            + "ST1.HR_CLASS,"
                            + "ST2.SEX "
                    + ") T2 ON INT(T2.BRO_SISCD) = INT(T1.NAMECD2) AND T2.HR_CLASS = T3.HR_CLASS "

                + "GROUP BY "
                + "GROUPING SETS "
                    + "((NAMECD2,NAME1,T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),"
                    + "(NAMECD2,NAME1),"
                    + "(T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),"
                    + "())"
                + "ORDER BY "
                    + "T3.HR_CLASS,"
                    + "NAMECD";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH040]set_detail3 sql ok!");

            // SVF-formへデータを出力 
            svf.VrSetForm("KNJH040_3.frm", 4);      //svf-form
            svf.VrsOut("NENDO", param._nendo + "  " + param._tmpGradeCdIntString + "学年  " + param._tmpSeito + "調査（学校要覧）");
            
            int h_c  = 0;
            int line = 0;

            while (rs.next()) {
                //住居種別名称出力
                if (line == 0) {
                    svf.VrsOutn("NAME" ,h_c+1   ,rs.getString("NAME"));
                }
                //合計
                if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                    while (line < 11-1) {
                        svf.VrsOut("HR_CLASS" ,"");         //組
                        svf.VrEndRecord();
                        line++;
                    }
                    if (rs.getString("NAMECD").equalsIgnoreCase("total")) {
                        h_c = 7;
                        svf.VrlOutn("BROTHER1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));    //男子人数
                        svf.VrlOutn("BROTHER1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));    //女子人数
                        svf.VrlOutn("BROTHER1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));    //人数
                        svf.VrsOut("HR_CLASS" ,"合");                                //組
                        svf.VrsOut("KUMI"   ,"計");                              //組
                        svf.VrEndRecord();
                        nonedata2 = true;
                        h_c = 0;
                        line = 0;
                    } else {
                        h_c++;
                        svf.VrlOutn("BROTHER1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));    //男子人数
                        svf.VrlOutn("BROTHER1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));    //女子人数
                        svf.VrlOutn("BROTHER1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));    //人数
                    }
                    continue;
                }

                //組のブレイク
                if (rs.getString("NAMECD").equalsIgnoreCase("total")) {
                    h_c = 7;
                    svf.VrlOutn("BROTHER1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));    //男子人数
                    svf.VrlOutn("BROTHER1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));    //女子人数
                    svf.VrlOutn("BROTHER1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));    //人数
                    String hrName = "";
                    if ("1".equals(param.useSchregRegdHdat)) {
                        hrName = rs.getString("HR_CLASS_NAME1");
                    } else {
                        hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), param.hmap);
                    }
                    if ("".equals(hrName) || hrName == null) {
                        hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"));
                    }
                    svf.VrsOut("HR_CLASS"   , hrName);//2004/08/18  NO003Modify
                    svf.VrsOut("KUMI"   ,"組");                              //組
                    svf.VrEndRecord();
                    nonedata2 = true;
                    h_c = 0;
                    line++;
                } else {
                    h_c++;
                    svf.VrlOutn("BROTHER1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));    //男子人数
                    svf.VrlOutn("BROTHER1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));    //女子人数
                    svf.VrlOutn("BROTHER1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));    //人数
                }

            }
            log.debug("[KNJH040]set_detail3 read ok!");

        } catch (Exception ex) {
            log.error("[KNJH040]set_detail3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail3の括り



    /*------------------------------------*
     * 住居調査　SVF出力                 *
     *------------------------------------*/
    private void set_detail4(final Vrw32alp svf, final DB2UDB db2, final Param param)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "VALUE(NAMECD2,'total') AS NAMECD,"
                    + "VALUE(NAME1,'合計') AS NAME,"
                    + "VALUE(T3.HR_CLASS,'total') AS HR_CLASS,"
                    + ("1".equals(param.useSchregRegdHdat) ? "HR_CLASS_NAME1," : "") 
                    + "VALUE(SUM(CASE WHEN SEX='1' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT1,"
                    + "VALUE(SUM(CASE WHEN SEX='2' THEN SCH_CNT ELSE 0 END),0) AS SCH_CNT2,"
                    + "VALUE(SUM(SCH_CNT),0) AS SCH_CNT3 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "'X' AS HELEN,"
                            + "HR_CLASS "
                            + ("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1 " : "") 
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR = '" + param._year + "' "
                            + "AND SEMESTER = '" + param._semester + "' "
                            + "AND GRADE = '" + param._tmpGrade + "' "
                    + ")T3 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "'X' AS JHON,"
                            + "NAMECD2,"
                            + "NAME1 "
                        + "FROM "
                            + "NAME_MST "
                        + "WHERE "
                                + "NAMECD1 = 'H108' "
                            + "AND NAMECD2 < '5' "  //add 2004/01/15
                    + ")T1 ON T3.HELEN = T1.JHON "

                    + "LEFT JOIN ("
                        + "SELECT "
                            + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
                            + "ST4.RESIDENTCD,"
                            + "ST1.HR_CLASS,"
                            + "ST2.SEX "
                        + "FROM "
                            + "SCHREG_REGD_HDAT ST6,"
                            + "SCHREG_REGD_DAT ST1,"
                            + "SCHREG_BASE_MST ST2,"
                            + "SCHREG_ENVIR_DAT ST4 "
                        + "WHERE "
                                + "ST1.YEAR     = '" + param._year + "' "
                            + "AND ST1.SEMESTER = '" + param._semester + "' "
                            + "AND ST1.GRADE    = '" + param._tmpGrade + "' "
                            + "AND ST1.SCHREGNO = ST2.SCHREGNO "
                            + "AND ST1.SCHREGNO = ST4.SCHREGNO "
                            + "AND VALUE(ST4.RESIDENTCD,'0')>'0' "
                            + "AND ST1.YEAR     = ST6.YEAR "
                            + "AND ST1.SEMESTER = ST6.SEMESTER "
                            + "AND ST1.GRADE    = ST6.GRADE "
                            + "AND ST1.HR_CLASS = ST6.HR_CLASS "
                        + "GROUP BY "
                            + "ST4.RESIDENTCD,"
                            + "ST1.HR_CLASS,"
                            + "ST2.SEX "
                    + ") T2 ON INT(T2.RESIDENTCD) = INT(T1.NAMECD2) AND T2.HR_CLASS = T3.HR_CLASS "

                + "GROUP BY "
                + "GROUPING SETS "
                    + "((NAMECD2,NAME1,T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),(NAMECD2,NAME1),(T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),())"
                + "ORDER BY "
                    + "T3.HR_CLASS,"
                    + "NAMECD";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH040]set_detail4 sql ok!");

            // SVF-formへデータを出力 
            svf.VrSetForm("KNJH040_4.frm", 4);      //svf-form
            svf.VrsOut("NENDO", param._nendo + "  " + param._tmpGradeCdIntString + "学年　" + param._tmpSeito + "調査（学校要覧）");
            int h_c  = 0;
            int line = 0;

            while (rs.next()) {
                //住居種別名称出力
                if (line == 0) {
                    svf.VrsOutn("NAME" ,h_c+1   ,rs.getString("NAME"));
                }
                //合計
                if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                    while (line < 11-1) {
                        svf.VrsOut("HR_CLASS" ,"");         //組
                        svf.VrEndRecord();
                        line++;
                    }
                    if (rs.getString("NAMECD").equalsIgnoreCase("total")) {
                        h_c = 5;
                        svf.VrlOutn("HOUSE1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));  //男子人数
                        svf.VrlOutn("HOUSE1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));  //女子人数
                        svf.VrlOutn("HOUSE1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));  //人数
                        svf.VrsOut("HR_CLASS" ,"合");                                //組
                        svf.VrsOut("KUMI"   ,"計");                              //組
                        svf.VrEndRecord();
                        nonedata2 = true;
                        h_c = 0;
                        line = 0;
                    } else {
                        h_c++;
                        svf.VrlOutn("HOUSE1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));  //男子人数
                        svf.VrlOutn("HOUSE1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));  //女子人数
                        svf.VrlOutn("HOUSE1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));  //人数
                    }
                    continue;
                }

                //組のブレイク
                if (rs.getString("NAMECD").equalsIgnoreCase("total")) {
                    h_c = 5;
                    svf.VrlOutn("HOUSE1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));  //男子人数
                    svf.VrlOutn("HOUSE1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));  //女子人数
                    svf.VrlOutn("HOUSE1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));  //人数
                    String hrName = "";
                    if ("1".equals(param.useSchregRegdHdat)) {
                        hrName = rs.getString("HR_CLASS_NAME1");
                    } else {
                        hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), param.hmap);
                    }
                    if ("".equals(hrName) || hrName == null) {
                        hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"));
                    }
                    svf.VrsOut("HR_CLASS"   , hrName);
                    svf.VrsOut("KUMI"   ,"組");                              //組
                    svf.VrEndRecord();
                    nonedata2 = true;
                    h_c = 0;
                    line++;
                } else {
                    h_c++;
                    svf.VrlOutn("HOUSE1_"+h_c , 1 ,rs.getInt("SCH_CNT1"));  //男子人数
                    svf.VrlOutn("HOUSE1_"+h_c , 2 ,rs.getInt("SCH_CNT2"));  //女子人数
                    svf.VrlOutn("HOUSE1_"+h_c , 3 ,rs.getInt("SCH_CNT3"));  //人数
                }

            }
            log.debug("[KNJH040]set_detail4 read ok!");

        } catch (Exception ex) {
            log.error("[KNJH040]set_detail4 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail4の括り



    /*------------------------------------*
     * 保護者の職業調査　SVF出力         *
     *------------------------------------*/
    private void set_detail5(final Vrw32alp svf, final DB2UDB db2, final Param param)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "VALUE(NAMECD2,'total') AS NAMECD,"
                    + "VALUE(NAME1,'合計') AS NAME,"
                    + "VALUE(T3.HR_CLASS,'total') AS HR_CLASS,"
                    + ("1".equals(param.useSchregRegdHdat) ? "HR_CLASS_NAME1," : "")
                    + "VALUE(SUM(SCH_CNT),0) AS SCH_CNT "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "'X' AS HELEN,"
                            + "HR_CLASS "
                            + ("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1 " : "")
                        + "FROM "
                            + "SCHREG_REGD_HDAT "
                        + "WHERE "
                                + "YEAR = '" + param._year + "' "
                            + "AND SEMESTER = '" + param._semester + "' "
                            + "AND GRADE = '" + param._tmpGrade + "' "
                    + ")T3 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "'X' AS JHON,"
                            + "NAMECD2,"
                            + "NAME1 "
                        + "FROM "
                            + "NAME_MST "
                        + "WHERE "
                            + "NAMECD1 = 'H202' "
                    + ")T1 ON T3.HELEN = T1.JHON "
                
                    + "LEFT JOIN ("
                        + "SELECT "
                            + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
                            + "ST4.GUARD_JOBCD,"
                            + "ST1.HR_CLASS "
                        + "FROM "
                            + "SCHREG_REGD_DAT ST1,"
                            + "GUARDIAN_DAT ST4 "
                        + "WHERE "
                                + "ST1.YEAR     = '" + param._year + "' "
                            + "AND ST1.SEMESTER = '" + param._semester + "' "
                            + "AND ST1.GRADE    = '" + param._tmpGrade + "' "
                            + "AND ST1.SCHREGNO = ST4.SCHREGNO "
                            + "AND VALUE(ST4.GUARD_JOBCD,'00')>='01' "
                        + "GROUP BY "
                            + "ST4.GUARD_JOBCD,"
                            + "ST1.HR_CLASS "
                    + ") T2 ON T2.GUARD_JOBCD = T1.NAMECD2 AND T2.HR_CLASS = T3.HR_CLASS "
                
                + "GROUP BY "
                + "GROUPING SETS "
                    + "((NAMECD2,NAME1,T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),"
                    + " (NAMECD2,NAME1),"
                    + " (T3.HR_CLASS"+("1".equals(param.useSchregRegdHdat) ? ",HR_CLASS_NAME1":"")+"),"
                    + " ())"
                + "ORDER BY "
                    + "NAMECD,"
                    + "T3.HR_CLASS";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH040]set_detail5 sql ok!");

            // SVF-formへデータを出力 
            svf.VrSetForm("KNJH040_5.frm", 4);      //svf-form
            
            svf.VrsOut("NENDO"      , param._nendo + "  " + param._tmpGradeCdIntString + "学年　" + param._tmpSeito + "調査（学校要覧）");
            int h_c  = 0;
            int line = 0;

            while (rs.next()) {
                //組名称出力
                if (line == 0) {
                    if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                        svf.VrsOutn("HR_CLASS"  , 16    ,"計");
                    } else {
                        String hrName = "";
                        if ("1".equals(param.useSchregRegdHdat)) {
                            hrName = rs.getString("HR_CLASS_NAME1");
                        } else {
                            hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), param.hmap) +"組";
                        }
                        if ("".equals(hrName) || hrName == null) {
                            hrName = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS")) +"組";
                        }
                        svf.VrsOutn("HR_CLASS"  ,h_c+1  , hrName);//2004/08/18  NO003Modify
                    }
                }
                //合計
                if (rs.getString("NAMECD").equalsIgnoreCase("total")) {
                    while (line < 35) {
                        svf.VrsOut("BUSINESS"   ,"");
                        svf.VrEndRecord();
                        line++;
                    }
                    if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                        h_c = 16;
                        svf.VrlOutn("COUNT1"        , h_c   ,rs.getInt("SCH_CNT")); //人数
                        svf.VrsOut("BUSINESS"               ,rs.getString("NAME")); //職業名称
                        svf.VrEndRecord();
                        nonedata2 = true;
                        h_c = 0;
                        line = 0;
                    } else {
                        h_c++;
                        svf.VrlOutn("COUNT1"        , h_c   ,rs.getInt("SCH_CNT")); //人数
                    }
                    continue;
                }

                //職業コードのブレイク
                if (rs.getString("HR_CLASS").equalsIgnoreCase("total")) {
                    svf.VrlOutn("COUNT1" , 16 ,rs.getInt("SCH_CNT"));       //人数
                    svf.VrsOut("BUSINESS"   ,rs.getString("NAME"));     //職業名称
                    svf.VrEndRecord();
                    nonedata2 = true;
                    h_c = 0;
                    line++;
                } else {
                    h_c++;
                    svf.VrlOutn("COUNT1" , h_c ,rs.getInt("SCH_CNT"));  //人数
                }

            }
            log.debug("[KNJH040]set_detail5 read ok!");

        } catch (Exception ex) {
            log.error("[KNJH040]set_detail5 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail5の括り


    private static class Param {
        final String _year;
        final String _semester;
        String _gakunen;
        String _semesterSdate;
        String _semesterEdate;
        final String _nendo;
        final String useSchregRegdHdat;
        String _tmpGrade;
        String _tmpGradeCd;
        String _tmpGradeCdIntString;
        String _tmpSchoolKind;
        String _tmpSeito;
        String _tmpFinschoolkindname;
        
        private Map hmap;       //NO003
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            // パラメータの取得
            _year = request.getParameter("YEAR");        // 年度
            _semester = request.getParameter("GAKKI");       // 学期

            //出力対象学年の編集
            String[] pclass = request.getParameterValues("GAKUNEN");    //学年
            _gakunen = pclass[0];
            for (int i = 1; i < pclass.length; i++) {
                _gakunen = _gakunen + "," + pclass[i];
            }

            _nendo = KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            
            /* 学期範囲日付(学年末)の取得 */
            try {
                KNJ_Semester semester = new KNJ_Semester();                     //クラスのインスタンス作成
                KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, "9");
                _semesterSdate = returnval.val2;                                          //学期開始日
                _semesterEdate = returnval.val3;                                          //学期終了日
            } catch (Exception e) {
                log.error("[KNJH040]semester date error!", e);
            }

            if (hmap == null) {
                hmap = KNJ_Get_Info.getMapForHrclassName(db2);  //NO003 表示用組
            }
        }
        
        /**
         * 中高判定
         */
        private String getSchoolKind(final DB2UDB db2, final String grade) {
            
            String schoolKind = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String jhsql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(jhsql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
                
            } catch (Exception e) {
                log.error("[KNJH040]getJorH error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }
        
        private String getGradeCd(final DB2UDB db2, final String grade) {
            
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String jhsql = "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(jhsql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradeCd = rs.getString("GRADE_CD");
                }
                
            } catch (Exception e) {
                log.error("[KNJH040]getJorH error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }
    }

}  //クラスの括り

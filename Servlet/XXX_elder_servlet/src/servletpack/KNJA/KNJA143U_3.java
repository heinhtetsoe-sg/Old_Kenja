// kanji=漢字
/*
 * $Id: b798eeb37129c52b81b47e0fd49f97cd1bd7217c $
 *
 * 作成日: 2005/06/26 11:28:50 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３H＞  生徒・職員証明書（三重県）
 **/

public class KNJA143U_3 {

    private static final Log log = LogFactory.getLog(KNJA143U_3.class);

    private static final String SEITOSHO = "1";

    private boolean nonedata = false;                               //該当データなしフラグ


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            final Param param = getParam(db2, request);

            if (SEITOSHO.equals(param._formKind)) {
                printSvfMainStudent(db2, svf, param);
            } else {
                printSvfMainTsugaku(db2, svf, param);
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    /** 帳票出力(生徒証) **/
    private void printSvfMainStudent(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 4;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            log.debug("sql:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm(param._useFormNameA143U + ".frm", 1);
            int line = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                }

                final String schregno   = rs.getString("SCHREGNO");
                final String schregnoP  = rs.getString("SCHREGNO_P");
                final String name       = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday   = rs.getString("BIRTHDAY");
                final String majorname  = StringUtils.defaultString(rs.getString("MAJORNAME"));
//                final String hrname     = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String addr1      = rs.getString("ADDR1");    //生徒住所1
                final String addr2      = rs.getString("ADDR2");    //生徒住所2
                final String gradecd    = rs.getString("GRADE_CD"); //学年
                final String limitDateGradeCd = rs.getString("LIMIT_DATE_GRADE_CD");    //学年(コード)
//                final String schoolKind = rs.getString("SCHOOL_KIND");    //校種

                final File schregimg = param.getImageFile("P" + schregnoP + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg) {
                    svf.VrsOutn("PHOTO_BMP", line, schregimg.getPath());
                }

                svf.VrsOutn("TITLE", line, "生徒証明書");  //タイトル
                svf.VrsOutn("SCHREGNO", line, schregno);   //学籍番号
                svf.VrsOutn("SENTENCE", line, "下記の者は、本校の生徒であることを証明する。");

                final String syozoku = majorname + "　コース";  //年組番号
//                final String gradestr = "第 " + String.valueOf(Integer.parseInt(gradecd)) + " 学年 " + hrname + "";
                svf.VrsOutn("ENT_SCHOOL", line, syozoku);
//                svf.VrsOutn("GRADE", line, gradestr);

                final int nameLen = KNJ_EditEdit.getMS932ByteLength(name);
                //氏名
                svf.VrsOutn("NAME1" + (nameLen <= 14 ? "_1" : nameLen <= 18 ? "_2" : "_3"), line, name);
                if (null != birthday) {
                    final String[] birthArray = StringUtils.split(birthday, "-");
                    //生徒誕生日
                    svf.VrsOutn("BIRTHDAY1", line, String.valueOf(Integer.parseInt(birthArray[0])));
                    svf.VrsOutn("BIRTHDAY2", line, String.valueOf(Integer.parseInt(birthArray[1])));
                    svf.VrsOutn("BIRTHDAY3", line, String.valueOf(Integer.parseInt(birthArray[2])));
                }

                //フォントサイズを統一するため、最大文字数のサイズに合わせる。
                final String outaddrstr = addr1 + ("".equals(addr2) ? "" : " " + addr2);
                final int addr1Len = KNJ_EditEdit.getMS932ByteLength(addr1);
                final int addr2Len = KNJ_EditEdit.getMS932ByteLength(addr2);
                final int addrLen = KNJ_EditEdit.getMS932ByteLength(outaddrstr);
                //折り返しを考慮する。フィールドは倍の数で判定。
                final int addrcutsize;
                final String addrfield;
                final String addrstr[];

                //生徒住所
                if (0 < addr1Len && 0 < addr2Len) {
                    if (addr1Len <= 40 && addr2Len <= 40) {
                        if (addrLen <= 24) {
                            svf.VrsOutn("ADDRESS1", line, outaddrstr);         //住所
                            addrfield   = null;
                        } else if (addr1Len <= 26 && addr2Len <= 26) {
                            addrfield   = "_2";
                        } else if (addr1Len <= 30 && addr2Len <= 30) {
                            addrfield   = "_3";
                        } else {
                            addrfield   = "_4";
                        }
                        if (null != addrfield) {
                        	svf.VrsOutn("ADDRESS2" + addrfield, line, addr1);         //学校住所
                        	svf.VrsOutn("ADDRESS3" + addrfield, line, addr2);         //学校住所
                        }
                    } else {
                        addrcutsize = addrLen <= 48 ? 24 : addrLen <= 52 ? 26   : addrLen <= 60 ? 30   : 40;
                        addrfield   = addrLen <= 48 ? "" : addrLen <= 52 ? "_2" : addrLen <= 60 ? "_3" : "_4";
                        addrstr = KNJ_EditEdit.get_token(outaddrstr, addrcutsize, 2);
                        svf.VrsOutn("ADDRESS2" + addrfield, line, addrstr[0]);         //学校住所
                        svf.VrsOutn("ADDRESS3" + addrfield, line, addrstr[1]);         //学校住所
                    }
                } else {
                    final int addrGyo = 2;
                    int addrKeta = 0;
                    String addrField = "";
                    if (24 >= addrLen) {
                        svf.VrsOutn("ADDRESS1", line, outaddrstr);
                    } else if (48 >= addrLen) {
                        addrKeta = 24;
                    } else if (52 >= addrLen) {
                        addrKeta = 26;
                        addrField = "_2";
                    } else if (60 >= addrLen) {
                        addrKeta = 30;
                        addrField = "_3";
                    } else {
                        addrKeta = 80;
                        addrField = "_4";
                    }
                    if (0 != addrKeta) {
                        int addrIdx = 2;
                        final List addrList = KNJ_EditKinsoku.getTokenList(outaddrstr, addrKeta, addrGyo);
                        for (Iterator it = addrList.iterator(); it.hasNext();) {
                            String addr = (String) it.next();
                            svf.VrsOutn("ADDRESS" + String.valueOf(addrIdx) + addrField, line, addr);
                            addrIdx++;
                        }
                    }
                }

                svf.VrsOutn("SDATE1", line, rs.getString("ENT_DATE_NENDO"));
                svf.VrsOutn("SDATE2", line, "4");
                svf.VrsOutn("SDATE3", line, "1");

                final int schaddrLen = KNJ_EditEdit.getMS932ByteLength(param._addr1);
                //折り返しを考慮する。フィールドは倍の数で判定。
                final int schaddrcutsize;
                final String schaddrfield;
                final String schaddrfststr1;
                final String schaddrfststr2;
                if (schaddrLen <= 40) {
                    schaddrcutsize = schaddrLen < 24 ? 24 : schaddrLen < 26 ? 26 : schaddrLen < 30 ? 30 : 40;
                    schaddrfield = schaddrLen < 24 ? "" : schaddrLen < 26 ? "_2" : schaddrLen < 30 ? "_3" : "_4";
                    schaddrfststr1 = param._addr1;
                    svf.VrsOutn("SCHOOLADDRESS1" + schaddrfield, line, schaddrfststr1);         //学校住所
                    svf.VrsOutn("SCHOOLADDRESS2", line, param._remark3);
                } else {
                    schaddrcutsize = schaddrLen < 48 ? 24 : schaddrLen < 52 ? 26 : schaddrLen < 60 ? 30 : 40;
                    schaddrfield = schaddrLen < 48 ? "" : schaddrLen < 52 ? "_2" : schaddrLen < 60 ? "_3" : "_4";
                    schaddrfststr1 = param._addr1.substring(0, schaddrcutsize);
                    svf.VrsOutn("SCHOOLADDRESS1" + schaddrfield, line, schaddrfststr1);         //学校住所
                    schaddrfststr2 = param._addr1.substring(schaddrcutsize);
                    svf.VrsOutn("SCHOOLADDRESS2" + schaddrfield, line, schaddrfststr2);         //学校住所
                }

                svf.VrsOutn("SCHOOLNAME1", line, param._schoolname);  //学校名
                final int pnlen = KNJ_EditEdit.getMS932ByteLength(param._principalName);
                final String pnfield = pnlen < 20 ? "1" : pnlen < 24 ? "2" : pnlen < 26 ? "3" : "4";
                svf.VrsOutn("JOBNAME1", line, "校長");
                svf.VrsOutn("PRINCIPALNAME" + pnfield, line, "    " + param._principalName);//校長名
                final String limitYear;
                if (1 == Integer.parseInt(gradecd)) {
                    limitYear = String.valueOf(Integer.parseInt(param._year) + 3);
                } else if (2 == Integer.parseInt(gradecd)) {
                    limitYear = String.valueOf(Integer.parseInt(param._year) + 2);
                } else {
                    limitYear = String.valueOf(Integer.parseInt(param._year) + 1);
                }
                svf.VrsOutn("LIMIT", line, "(" + limitYear + "年3月31日まで有効)");

//                String image = null;
//                if ("H".equals(schoolKind)) {
//                	image = param._certifSchoolstampHImagePath;
//                } else if ("J".equals(schoolKind)) {
//                	image = param._certifSchoolstampJImagePath;
//                }
//                log.info(" schoolKind " + schoolKind + ", image = " + image);
//                if (null != image) {
//                    svf.VrsOutn("STAMP", line, image); //住所
//                }

                /* ************** */
                /* *右側  通学路* */
                /* ************** */
                svf.VrsOutn("NENDO", line, param._limitDate + "年度"); //年度
                svf.VrsOutn("LIMIT2", line, "(4月1日～" + String.valueOf(Integer.parseInt(param._limitDate) + 1) + "年3月31日まで有効)"); //有効期限
                if (NumberUtils.isDigits(limitDateGradeCd)) {
                	svf.VrsOutn("GRADE", line, String.valueOf(Integer.parseInt(limitDateGradeCd)));          //学年
                }
                svf.VrsOutn("SCHREGNO2", line, schregno);   //学籍番号
                svf.VrsOutn("NAME2", line, name);           //氏名

                final String addrField = addrLen > 60 ? "_3" : (addrLen > 44) ? "_2": "_1";
                svf.VrsOutn("ADDRESS4" + addrField, line, outaddrstr); //住所

                final String josya1  = StringUtils.defaultString(rs.getString("JOSYA_1"));
                final String josya2  = StringUtils.defaultString(rs.getString("JOSYA_2"));
                final String josya3  = StringUtils.defaultString(rs.getString("JOSYA_3"));
                final String josya4  = StringUtils.defaultString(rs.getString("JOSYA_4"));
                final String gesya1  = StringUtils.defaultString(rs.getString("GESYA_1"));
                final String gesya2  = StringUtils.defaultString(rs.getString("GESYA_2"));
                final String gesya3  = StringUtils.defaultString(rs.getString("GESYA_3"));
                final String gesya4  = StringUtils.defaultString(rs.getString("GESYA_4"));

                final String j1Field = KNJ_EditEdit.getMS932ByteLength(josya1) > 12 ? "_2": "_1";
                final String g1Field = KNJ_EditEdit.getMS932ByteLength(gesya1) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION1_1" + j1Field, line, josya1); //乗車1
                svf.VrsOutn("SECTION1_2" + g1Field, line, gesya1); //下車1

                final String j2Field = KNJ_EditEdit.getMS932ByteLength(josya2) > 12 ? "_2": "_1";
                final String g2Field = KNJ_EditEdit.getMS932ByteLength(gesya2) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION2_1" + j2Field, line, josya2); //乗車2
                svf.VrsOutn("SECTION2_2" + g2Field, line, gesya2); //下車2

                final String j3Field = KNJ_EditEdit.getMS932ByteLength(josya3) > 12 ? "_2": "_1";
                final String g3Field = KNJ_EditEdit.getMS932ByteLength(gesya3) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION3_1" + j3Field, line, josya3); //乗車3
                svf.VrsOutn("SECTION3_2" + g3Field, line, gesya3); //下車3

                final String j4Field = KNJ_EditEdit.getMS932ByteLength(josya4) > 12 ? "_2": "_1";
                final String g4Field = KNJ_EditEdit.getMS932ByteLength(gesya4) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION4_1" + j4Field, line, josya4); //乗車4
                svf.VrsOutn("SECTION4_2" + g4Field, line, gesya4); //下車4

                line++;
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力(通学路) **/
    private void printSvfMainTsugaku(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
            ) {
        final int maxLine = 4;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            log.debug("sql:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm("KNJA143U_3_3.frm", 1);

            int line = 1;
            int col  = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                    col  = 1;
                }

                final String schregno   = rs.getString("SCHREGNO");
                final String name       = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String addr1      = rs.getString("ADDR1");    //生徒住所1
                final String addr2      = rs.getString("ADDR2");    //生徒住所2
                final String limitDateGradeCd = rs.getString("LIMIT_DATE_GRADE_CD");    //学年(コード)

                //フォントサイズを統一するため、最大文字数のサイズに合わせる。
                final String outaddrstr = addr1 + ("".equals(addr2) ? "" : " " + addr2);
                final int addrLen = KNJ_EditEdit.getMS932ByteLength(outaddrstr);

                svf.VrsOutn("NENDO" + col, line, param._limitDate + "年度"); //年度
                svf.VrsOutn("LIMIT" + col, line, "(4月1日～" + String.valueOf(Integer.parseInt(param._limitDate) + 1) + "年3月31日まで有効)"); //有効期限
                if (NumberUtils.isDigits(limitDateGradeCd)) {
                	svf.VrsOutn("GRADE" + col, line, String.valueOf(Integer.parseInt(limitDateGradeCd)));          //学年
                }
                svf.VrsOutn("SCHREGNO" + col, line, schregno);   //学籍番号
                svf.VrsOutn("NAME" + col, line, name);           //氏名

                final String addrField = addrLen > 60 ? "_3" : (addrLen > 44) ? "_2": "_1";
                svf.VrsOutn("ADDRESS" + col + addrField, line, outaddrstr); //住所

                final String josya1  = StringUtils.defaultString(rs.getString("JOSYA_1"));
                final String josya2  = StringUtils.defaultString(rs.getString("JOSYA_2"));
                final String josya3  = StringUtils.defaultString(rs.getString("JOSYA_3"));
                final String josya4  = StringUtils.defaultString(rs.getString("JOSYA_4"));
                final String gesya1  = StringUtils.defaultString(rs.getString("GESYA_1"));
                final String gesya2  = StringUtils.defaultString(rs.getString("GESYA_2"));
                final String gesya3  = StringUtils.defaultString(rs.getString("GESYA_3"));
                final String gesya4  = StringUtils.defaultString(rs.getString("GESYA_4"));

                final String j1Field = KNJ_EditEdit.getMS932ByteLength(josya1) > 12 ? "_2": "_1";
                final String g1Field = KNJ_EditEdit.getMS932ByteLength(gesya1) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION" + col + "_1_1" + j1Field, line, josya1); //乗車1
                svf.VrsOutn("SECTION" + col + "_1_2" + g1Field, line, gesya1); //下車1

                final String j2Field = KNJ_EditEdit.getMS932ByteLength(josya2) > 12 ? "_2": "_1";
                final String g2Field = KNJ_EditEdit.getMS932ByteLength(gesya2) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION" + col + "_2_1" + j2Field, line, josya2); //乗車2
                svf.VrsOutn("SECTION" + col + "_2_2" + g2Field, line, gesya2); //下車2

                final String j3Field = KNJ_EditEdit.getMS932ByteLength(josya3) > 12 ? "_2": "_1";
                final String g3Field = KNJ_EditEdit.getMS932ByteLength(gesya3) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION" + col + "_3_1" + j3Field, line, josya3); //乗車3
                svf.VrsOutn("SECTION" + col + "_3_2" + g3Field, line, gesya3); //下車3

                final String j4Field = KNJ_EditEdit.getMS932ByteLength(josya4) > 12 ? "_2": "_1";
                final String g4Field = KNJ_EditEdit.getMS932ByteLength(gesya4) > 12 ? "_2": "_1";
                svf.VrsOutn("SECTION" + col + "_4_1" + j4Field, line, josya4); //乗車4
                svf.VrsOutn("SECTION" + col + "_4_2" + g4Field, line, gesya4); //下車4

                if (col > 1) {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**生徒又は職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , SCHREG_ADDRESS AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.ZIPCD, ");
            stb.append("         P1.PREF_CD, ");
            stb.append("         P1.PREF_NAME, ");
            stb.append("         T1.AREACD, ");
            stb.append("         N1.NAME1 AS AREA_NAME, ");
            stb.append("         T1.ADDR1, ");
            stb.append("         T1.ADDR2, ");
            stb.append("         T1.TELNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
            stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
            stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
            stb.append("     ) ");
            stb.append(" , T_LIMIT_DATE_GRADE_CD AS ( ");
            stb.append("     SELECT T1.SCHREGNO, ");
            stb.append("            T2.GRADE_CD AS LIMIT_DATE_GRADE_CD ");
            stb.append("     FROM ");
            stb.append("         (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE ");
            stb.append("          FROM SCHREG_REGD_DAT T1 ");
            stb.append("          WHERE T1.YEAR = '" + param._limitDate + "' ");
            stb.append("          GROUP BY SCHREGNO, YEAR) T1 ");
            stb.append("         INNER JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("             ON  T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.GRADE = T1.GRADE ");
            stb.append("     ) ");

            stb.append("SELECT T1.SCHREGNO as SCHREGNO_P, "); // 写真用
            stb.append("       substr(T1.SCHREGNO, 4) as SCHREGNO, "); //学籍番号 頭３桁カット
            stb.append("       T2.NAME, ");            //氏名
            stb.append("       T5.MAJORNAME, ");       //学科
            stb.append("       T4.HR_NAME, ");         //クラス名
            stb.append("       T4.HR_NAMEABBV, ");     //クラス名略称
            stb.append("       T4.HR_CLASS_NAME1, ");  //クラス名
            stb.append("       T1.GRADE, ");           //学年(コード)
            stb.append("       T6.GRADE_CD, ");        //学年(コード)
            stb.append("       T1.HR_CLASS, ");        //組(コード)
            stb.append("       T1.ATTENDNO, ");        //出席番号
            stb.append("       T2.REAL_NAME, ");       //氏名
            stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");  //氏名どっち使うフラグ
            stb.append("       T2.BIRTHDAY, ");        //誕生日
//            stb.append("       CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('" + param._issueDate + "' - T2.BIRTHDAY) END AS AGE, "); //年齢
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4, ");
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOSYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.STATION_NAME ELSE L5.JOSYA_2 END AS JOSYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.STATION_NAME ELSE L5.JOSYA_3 END AS JOSYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.STATION_NAME ELSE L5.JOSYA_4 END AS JOSYA_4, ");
            stb.append("       VALUE(ADDR.ADDR1, '') AS ADDR1, ");  //住所1
            stb.append("       VALUE(ADDR.ADDR2, '') AS ADDR2, ");   //住所2
            stb.append("       T6.SCHOOL_KIND, ");        //学年校種
            stb.append("       ENTGRD.ENT_DATE, ");        //入学日付
            stb.append("       FISCALYEAR(ENTGRD.ENT_DATE) AS ENT_DATE_NENDO, ");        //入学日付年度
            stb.append("       T7.LIMIT_DATE_GRADE_CD "); //有効年度の学年
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append("            AND T6.DIV = '01' ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("            AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("            AND T4.GRADE = T1.GRADE ");
            stb.append("            AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN MAJOR_MST T5 ON T5.COURSECD = T1.COURSECD ");
            stb.append("            AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT T6 ");
            stb.append("            ON T6.YEAR = T1.YEAR ");
            stb.append("            AND T6.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ");
            stb.append("            ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
            stb.append("            AND ENTGRD.SCHOOL_KIND = T6.SCHOOL_KIND ");
            stb.append("       LEFT JOIN T_LIMIT_DATE_GRADE_CD T7 ");
            stb.append("            ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = L5.JOSYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = L5.JOSYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = L5.JOSYA_4 ");
            stb.append("       LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
            stb.append("WHERE  T1.YEAR = '" + param._year + "' AND ");
            stb.append("       T1.SEMESTER = '" + param._semester + "' AND ");
            stb.append("       T1.SCHREGNO IN (" + param._findschreg + ") ");
            stb.append("ORDER BY ");
            stb.append("       T1.ATTENDNO ");
        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 66754 $ $Date: 2019-04-04 22:57:49 +0900 (木, 04 4 2019) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _disp;
        private final String _formKind; // 1:生徒証 2:通学路のみ
        private final String[] _gradeHrclass;
        private final String[] _schregnos;

//        private final String _issueDate;
        private final String _limitDate;

        private String _jobname;
        private String _principalName;
        private String _schoolname;
        private String _addr1;
        private String _remark3;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
//        private final String _certifSchoolstampHImagePath;
//        private final String _certifSchoolstampJImagePath;
        private final String _useAddrField2;
        private final String _useFormNameA143U;

        private final String _use_prg_schoolkind;
        private final String _selectSchoolKind;
        private final String _useSchool_KindField;
        private final String _schoolkind;
        private final String _schoolcd;

        private final String _findgr_hr;
        private final String _findschreg;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");                   //年度
            _semester = request.getParameter("SEMESTER");                   //学期

            _disp = request.getParameter("DISP");
            _formKind = request.getParameter("FROM_KIND");

            if ("2".equals(_disp)) {
            	_gradeHrclass = new String[1];
            	_gradeHrclass[0] = request.getParameter("GRADE_HR_CLASS");    //学年＋組
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
            	_schregnos = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
            } else {
                _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
                _schregnos = getSchregnos(db2);
            }
            _findschreg = conv_arystr_to_str(_schregnos, ",", "-", 1);

//            _issueDate = request.getParameter("ISSUE_DATE").replace('/','-');
            _limitDate = request.getParameter("LIMIT_DATE");

            _useAddrField2 = request.getParameter("useAddrField2");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolkind = request.getParameter("SCHOOL_KIND");
            _schoolcd = request.getParameter("SCHOOLCD");

        	_useFormNameA143U = request.getParameter("USEFORMNAMEA143U") + ("J".equals(_schoolkind) ? "_1" : "_2");

            // 学籍番号の指定
            loadCertifSchoolDat(db2, _year);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
//    		_certifSchoolstampHImagePath = getImagePath(_documentRoot, "CERTIF_SCHOOLSTAMP_H", ".bmp");
//    		_certifSchoolstampJImagePath = getImagePath(_documentRoot, "CERTIF_SCHOOLSTAMP_J", ".bmp");
        }

//        public String getImagePath(final String documentRoot, final String filename, final String extension) {
//            final String path = documentRoot + "/" + (null == _imagepath ? "" : _imagepath + "/") + filename + extension;
//            final File file = new File(path);
//            if (!file.exists()) {
//                log.info(" file " + file.getPath() + " exists? " + file.exists());
//            }
//            if (!file.exists()) {
//                return null;
//            }
//            return file.getPath();
//        }

        private String conv_arystr_to_str(final String[] strary, final String sep) {
            return conv_arystr_to_str(strary, sep, "", 0);
        }
        private String conv_arystr_to_str(final String[] strary, final String sep, final String delim, final int cutno) {
        	String convgr_hr = "";
        	String sepwk = "";
        	for (int ii = 0;ii < strary.length;ii++) {
        		String cutwkstr[];
        		int idx = 0;
        		if (!"".equals(delim) && cutno > 0) {
        			cutwkstr = StringUtils.split(strary[ii], delim);
        			idx = cutno - 1;
        		} else {
        			cutwkstr = new String[1];
        			cutwkstr[0] = strary[ii];
        		}
        		convgr_hr += sepwk + "'" + cutwkstr[idx] + "'";
        		sepwk = sep;
        	}
        	return convgr_hr;
        }

        private String[] getSchregnos(final DB2UDB db2) {
        	String[] retstrlist;
        	List retwklist = new ArrayList();

        	String schregno_get_sql = "SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '"+_year+"' AND SEMESTER = '"+_semester+"' AND GRADE || HR_CLASS IN ("+_findgr_hr+") ";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(schregno_get_sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retwklist.add(rs.getString("SCHREGNO"));
                }
            } catch (Exception ex) {
                log.error("setSvfout set error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        	retstrlist = (String[])retwklist.toArray(new String[retwklist.size()]);
        	return retstrlist;
        }
        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            if (file.exists()) {
                return file;
            }
            return null;
        }

        public void loadCertifSchoolDat(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String certifKindcd = "H".equals(_schoolkind) ? "101" : "102";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobname =  rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolname = rs.getString("SCHOOL_NAME");
                    _addr1 = rs.getString("REMARK1");
                    _remark3 = rs.getString("REMARK3");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

}//クラスの括り

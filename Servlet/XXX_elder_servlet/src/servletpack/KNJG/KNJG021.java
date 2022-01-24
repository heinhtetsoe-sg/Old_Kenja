/**
 *  学校教育システム 賢者 [事務管理] 卒業証明書交付台帳出力
 *
 *      2005/01/17 yamashiro
 *      2005/01/19 発行番号の’号’を削除
 *      2006/01/31 過去の年度を指定した場合、今年度の卒業生が2学年時の組で出力される不具合を修正--NO001 => 学年＝’3’の条件を追加
 */

package servletpack.KNJG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJG021 {

    private static final Log log = LogFactory.getLog(KNJG021.class);
    boolean nonedata;
    private static int line = 13;   //１ページのレイアウト １３行２列
    
    private String param_year;
    private String param_gakki;
    private String param_2;
    private String _certifNoSyudou;
    private String _certif_no_8keta;
    private boolean _isOutputCertifNo;
    private String _useSchool_KindField;
    private String _SCHOOLKIND;

    /**
      *
      *  KNJG.classから最初に起動されるクラス
      *
      **/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // パラメータの取得
        getParam(request);

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                           //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("db new error:" + ex);
        }

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db open error", ex);
            if (db2 != null) {
                db2.close();
            }
            return;
        }

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
            log.error("db close error!"+ex);
        }
    }   //doGetの括り


    /**
     *  印刷処理
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf) {

        //フォーム初期設定および項目設定
        try {
            final String z010Name1 = getNameMstZ010(db2);
            final String form = "KINDAI".equals(z010Name1) || "KINJUNIOR".equals(z010Name1) ? "KNJG021_KIN.frm" : "KNJG021.frm"; 
            svf.VrSetForm(form, 1);
        } catch (Exception ex) {
            log.error("VrSetForm error!", ex);
        }

        param_2 = KNJ_EditDate.gengou(db2, Integer.parseInt(param_year)) + "年度";   //年度

        //卒業証明書交付台帳印刷処理

        try {
            final String sql = prestatCertificate();
            final List<Map<String, String>> list = KnjDbUtils.query(db2, sql);

            int outnumclass[] = { 0, 0, 0 };  //クラス別出力件数 0:計 1:男 2:女
            int outnumtotal[] = { 0, 0, 0 };  //総合出力件数     0:計 1:男 2:女
            int totalpage = getTotalpage(db2);
            int nowpage = 0;
            String hrclass = null;            //クラスの保存
            String hrname = null;             //クラス名称の保存
//log.debug("ps="+ps.toString());
            for (final Map<String, String> rs : list) {
                //log.debug(" schregno = " + rs.getString("SCHREGNO"));
                //クラスのブレイク
                if (hrclass == null  ||  ! KnjDbUtils.getString(rs, "HR_CLASS").equals(hrclass)  ||  (outnumclass[0] != 0 && outnumclass[0] % (line * 2) == 0)) {
                    if (hrclass != null) {
                        nowpage++;                                              //ページ
                        printSvfOutHead(svf, hrname, totalpage, nowpage);            //ページ項目出力
                        if (hrclass == null  ||  ! KnjDbUtils.getString(rs, "HR_CLASS").equals(hrclass)) {
                            printSvfOutBottom(svf, 1, outnumclass[0], outnumclass[1], outnumclass[2], hrname);                 //クラス合計行出力
                        }
                        svf.VrEndPage();
                        nonedata = true;
                    }
                    if (hrclass == null  ||  ! KnjDbUtils.getString(rs, "HR_CLASS").equals(hrclass)) {
                        hrclass = KnjDbUtils.getString(rs, "HR_CLASS");
                        hrname = KnjDbUtils.getString(rs, "HR_NAME");
                        outnumclass = new int[]{ 0, 0, 0 };   //クラス別出力件数を初期化
                    }
                }
                try {
                    //クラス合計処理
                    if (KnjDbUtils.getString(rs, "SEX").equals("1")) outnumclass[1]++;
                    if (KnjDbUtils.getString(rs, "SEX").equals("2")) outnumclass[2]++;
                    outnumclass[0]++;
                } catch (Exception ex) {
                    log.error("addnum!", ex);
                }

                try {
                    //総合計処理
                    if (KnjDbUtils.getString(rs, "SEX").equals("1")) outnumtotal[1]++;
                    if (KnjDbUtils.getString(rs, "SEX").equals("2")) outnumtotal[2]++;
                    outnumtotal[0]++;
                } catch (Exception ex) {
                    log.error("addnum!", ex);
                }
                printSvfOutDetail(svf, rs, outnumclass[0]);   //台帳出力
            }
            if (hrclass != null) {
                nowpage++;                                                      //ページ
                printSvfOutHead(svf, hrname, totalpage, nowpage);      //ページ項目出力
                printSvfOutBottom(svf, 1, outnumclass[0], outnumclass[1], outnumclass[2], hrname);               //クラス合計行出力
                printSvfOutBottom(svf, 2, outnumtotal[0], outnumtotal[1], outnumtotal[2], hrname);               //総合計行出力
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("printsvfDetail error!", ex);
        }
    }


    /**
     *  総ページ数取得
     */
    private int getTotalpage(DB2UDB db2) {

        final String sql = prestatTotalPage();
        log.info(" sql2 = " + sql);
        final String val = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        if (NumberUtils.isDigits(val)) {
        	return Integer.parseInt(val);
        }
        return 0;
    }


    /** 
     *  卒業証明書交付台帳 ページ単位の出力
     *      引数について
     */
    private void printSvfOutHead(Vrw32alp svf, String hrname, int totalpage, int nowpage) {

    	svf.VrsOut("NENDO",       param_2);                    //年度
    	svf.VrsOut("PAGE",        String.valueOf(nowpage));   //現在ページ
    	svf.VrsOut("TOTAL_PAGE",  String.valueOf(totalpage));  //総ページ数
    	svf.VrsOut("HR_NAME",     hrname);                        //クラス名称

    }

    private List getNameLines(final int keta, final String name) {
        final String [] tokens = KNJ_EditEdit.get_token(name, keta, 2);
        if (null == tokens) {
            return Collections.EMPTY_LIST;
        }
        final List list = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] != null) {
                list.add(tokens[i]);
            }
        }
        return list;
    }

    /** 
     *  卒業証明書交付台帳 明細出力
     *      引数について int outnum : クラス単位の出力件数 １３行２列
     */
    private void printSvfOutDetail(final Vrw32alp svf, final Map<String, String> row, final int outnum) {

        int i = (outnum % line == 0)? line : outnum % line;
        int j = (outnum % (line * 2) == 0)? 2 : (outnum % line == 0)? 1 : ((outnum / line) % 2 == 0)? 1 : 2;
        //svf.VrsOutn("CERTIF_WORD" + j + "_1",  i, "近高");
        //svf.VrsOutn("CERTIF_WORD" + j + "_2",  i, "年");
        if ("1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG")) && "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME")) && null != KnjDbUtils.getString(row, "REAL_NAME") && null != KnjDbUtils.getString(row, "NAME")) {
            final boolean useField2 = getNameLines(20, KnjDbUtils.getString(row, "REAL_NAME")).size() > 1 || getNameLines(20, "(" + KnjDbUtils.getString(row, "NAME") + ")").size() > 1;
            svf.VrsOutn(useField2 ? "NAME" + j + "_2_1" : "NAME" + j + "_2", i, KnjDbUtils.getString(row, "REAL_NAME"));
            svf.VrsOutn(useField2 ? "NAME" + j + "_3_1" : "NAME" + j + "_3", i, "(" + KnjDbUtils.getString(row, "NAME") + ")");
        } else {
            final String name = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME")) ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME");
            final boolean useField2 = getNameLines(20, name).size() > 1;
            svf.VrsOutn(useField2 ? "NAME" + j + "_1_1" : "NAME" + j + "_1", i, name);
        }
        svf.VrsOutn("ATTENDNO" + j,  i,  String.valueOf(KnjDbUtils.getInt(row, "ATTENDNO", 0)));    //出席番号
        if (_isOutputCertifNo) {
            final String rsCertifno = "1".equals(_certifNoSyudou) || "1".equals(_certif_no_8keta) ? KnjDbUtils.getString(row, "REMARK1") : KnjDbUtils.getString(row, "CERTIF_NO");
            svf.VrsOutn("CERTIFNO" + j,  i,  StringUtils.defaultString(rsCertifno));  //証明書番号 05/01/19Modify
        }
    }

    /** 
     *  卒業証明書交付台帳 合計人数の出力
     *      引数について
     */
    private void printSvfOutBottom(final Vrw32alp svf, final int i, final int total, final int male, final int female, final String hrname) {

        final StringBuffer stb = new StringBuffer();
        stb.append((i == 1)? hrname + "  " : "合計  ");
        stb.append(total).append("名 ");
        stb.append("男 ").append(male).append("名 ");
        stb.append("女 ").append(female).append("名 ");
        svf.VrsOut("NOTE" + i, stb.toString());
    }

    /**
     *  sql作成 卒業証明書交付台帳出力
     *
     */
    private String prestatCertificate() {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT  T1.SCHREGNO,");
        stb.append(        "T2.NAME,");
        stb.append(        "T2.REAL_NAME,");
        stb.append(        "(CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append(        "T11.NAME_OUTPUT_FLG, ");
        stb.append(        "T2.SEX,");
        stb.append(        "T1.ATTENDNO,");
        stb.append(        "T1.HR_CLASS,");
        stb.append(        "T1.HR_NAME,");
        if (!"1".equals(_certif_no_8keta)) {
            stb.append(        "T5.CERTIF_NO, ");
        }
        stb.append(        "T7.REMARK1 ");
        stb.append("FROM   (SELECT  W1.SCHREGNO, ");
        stb.append(                "W1.ATTENDNO,");
        stb.append(                "W1.GRADE,");
        stb.append(                "W1.HR_CLASS,");
        stb.append(                "W2.HR_NAME ");
        stb.append(         "FROM   SCHREG_REGD_DAT W1, ");
        stb.append(                "SCHREG_REGD_HDAT W2 ");
        stb.append(         "WHERE  W1.YEAR = '" + param_year + "' AND ");
        stb.append(                "W1.SEMESTER = '" + param_gakki + "' AND ");
        stb.append(                "W1.GRADE >= '03' AND ");  //NO001
        stb.append(                "W1.YEAR = W2.YEAR AND ");
        stb.append(                "W1.SEMESTER = W2.SEMESTER AND ");
        stb.append(                "W1.GRADE = W2.GRADE AND ");
        stb.append(                "W1.HR_CLASS = W2.HR_CLASS ");
        stb.append(         ") T1 ");
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + param_year + "' AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append(         "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                          "AND T2.GRD_DIV = '1' ");
        
        if ("1".equals(_certif_no_8keta)) {
        	stb.append("  LEFT JOIN (");
        	stb.append("        SELECT  W1.SCHREGNO, ");
        	stb.append("                MAX(W1.CERTIF_INDEX) AS CERTIF_INDEX ");
        	stb.append("        FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append("        WHERE   W1.YEAR = '" + param_year + "' ");
        	stb.append("                AND W1.CERTIF_KINDCD = '001' ");
        	stb.append("        GROUP BY W1.SCHREGNO ");
        	stb.append("  ) T8 ON T8.SCHREGNO = T1.SCHREGNO ");

        } else {
        	stb.append(         "LEFT JOIN(");
        	stb.append(         "SELECT  SCHREGNO, ");
        	stb.append(                 "MAX(CERTIF_NO) AS CERTIF_NO ");
        	stb.append(         "FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append(         "WHERE   W1.YEAR = '" + param_year + "' AND ");
        	stb.append(                 "W1.CERTIF_KINDCD = '001' AND ");
        	stb.append(                 "W1.SCHREGNO IN(SELECT  SCHREGNO ");
        	stb.append(                                "FROM    SCHREG_REGD_DAT ");
        	stb.append(                                "WHERE   YEAR = '" + param_year + "' AND ");
        	stb.append(                                        "SEMESTER = '" + param_gakki + "') ");
        	stb.append(         "GROUP BY W1.SCHREGNO ");
        	stb.append(         ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        	
        	stb.append(         "LEFT JOIN(");
        	stb.append(         "SELECT  SCHREGNO, ");
        	stb.append(                 "CERTIF_NO, ");
        	stb.append(                 "CERTIF_INDEX ");
        	stb.append(         "FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append(         "WHERE   W1.YEAR = '" + param_year + "' AND ");
        	stb.append(                 "W1.CERTIF_KINDCD = '001' AND ");
        	stb.append(                 "W1.SCHREGNO IN(SELECT  SCHREGNO ");
        	stb.append(                                "FROM    SCHREG_REGD_DAT ");
        	stb.append(                                "WHERE   YEAR = '" + param_year + "' AND ");
        	stb.append(                                        "SEMESTER = '" + param_gakki + "') ");
        	stb.append(         ")T8 ON T8.SCHREGNO = T1.SCHREGNO AND T8.CERTIF_NO = T5.CERTIF_NO ");
        }
        
        stb.append(         "LEFT JOIN(");
        stb.append(         "SELECT  SCHREGNO, ");
        stb.append(                 "CERTIF_INDEX, ");
        stb.append(                 "REMARK1 ");
        stb.append(         "FROM    CERTIF_DETAIL_EACHTYPE_DAT W1 ");
        stb.append(         "WHERE   W1.YEAR = '" + param_year + "' AND ");
        stb.append(                 "W1.TYPE = '1' AND ");
        stb.append(                 "W1.SCHREGNO IN(SELECT  SCHREGNO ");
        stb.append(                                "FROM    SCHREG_REGD_DAT ");
        stb.append(                                "WHERE   YEAR = '" + param_year + "' AND ");
        stb.append(                                        "SEMESTER = '" + param_gakki + "') ");
        stb.append(         ")T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.CERTIF_INDEX = T8.CERTIF_INDEX ");
        stb.append(         "LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T1.SCHREGNO AND T11.DIV = '08' ");
        stb.append(         "WHERE FISCALYEAR(T2.GRD_DATE) = '" + param_year + "' ");

        stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();

    }


    /**
     *  sql作成 総ページ数
     *
     */
    private String prestatTotalPage() {

        final StringBuffer stb = new StringBuffer();
        int i = line * 2;

        stb.append("SELECT  SUM(COUNT) AS COUNT ");
        stb.append("FROM   (SELECT  CASE WHEN 0 < MOD(COUNT(*)," + i + ") THEN COUNT(*)/ " + i + " +1 ELSE COUNT(*)/ " + i + " END AS COUNT ");
        stb.append(        "FROM   (SELECT  W1.SCHREGNO, ");
        stb.append(                        "W1.GRADE, ");
        stb.append(                        "W1.HR_CLASS ");
        stb.append(                "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(                        " INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR = W2.YEAR AND ");
        stb.append(                        "W1.SEMESTER = W2.SEMESTER AND ");
        stb.append(                        "W1.GRADE = W2.GRADE AND ");
        stb.append(                        "W1.HR_CLASS = W2.HR_CLASS ");
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W2.YEAR AND GDAT.GRADE = W1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            stb.append(                " AND W1.GRADE >= '03' ");  //NO001
        }
        stb.append(                "WHERE   W1.YEAR = '" + param_year + "' AND ");
        stb.append(                        "W1.SEMESTER = '" + param_gakki + "' ");
        stb.append(                ") T1 ");
        stb.append(                "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                             "AND T2.GRD_DIV = '1' ");
        stb.append(        "WHERE FISCALYEAR(T2.GRD_DATE) = '" + param_year + "' ");
        stb.append(        "GROUP BY GRADE, HR_CLASS ");
        stb.append(        ")U1 ");
        return stb.toString();

    }

    /** 
     *  get parameter doGet()パラメータ受け取り 

     */
    private void getParam(final HttpServletRequest request) {
        log.debug(" $Revision: 71802 $ $Date: 2020-01-16 20:57:04 +0900 (木, 16 1 2020) $ ");
        KNJServletUtils.debugParam(request, log);
        param_year = request.getParameter("YEAR");            //年度
        param_gakki = request.getParameter("GAKKI");           //学期
        _certifNoSyudou = request.getParameter("certifNoSyudou");  //証明書番号は手入力の値を表示するか
        _certif_no_8keta = request.getParameter("certif_no_8keta");
        _isOutputCertifNo = !"1".equals(request.getParameter("hasCheckOutputCertifNo")) || "1".equals(request.getParameter("hasCheckOutputCertifNo")) && "1".equals(request.getParameter("OUTPUT_CERTIF_NO")); 
        _useSchool_KindField = request.getParameter("useSchool_KindField");
        _SCHOOLKIND = request.getParameter("SCHOOLKIND");
    }
    

    private String getNameMstZ010(final DB2UDB db2) {
        return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ")));
    }

}//クラスの括り

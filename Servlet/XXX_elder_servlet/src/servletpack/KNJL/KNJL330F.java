package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
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
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL330F {

    private static final Log log = LogFactory.getLog(KNJL330F.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            log.fatal("$Revision: 72854 $ $Date: 2020-03-10 10:24:52 +0900 (火, 10 3 2020) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            setSvfMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (null != _param) {
                _param.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        final String sql = sql(_param);
        log.info(" sql = " + sql);
        final List dataList = KnjDbUtils.query(db2, sql);

//        final String accountno = "(" + StringUtils.defaultString(KnjDbUtils.getString(_param._schoolBankMst, "DEPOSIT_ITEM_NAME")) + ") No. " + StringUtils.defaultString(KnjDbUtils.getString(_param._schoolBankMst, "ACCOUNTNO"));
//        final String bankname = KnjDbUtils.getString(_param._schoolBankMst, "BANKNAME");
//        final String bankbranchname = KnjDbUtils.getString(_param._schoolBankMst, "BANKNAME") + "　" + KnjDbUtils.getString(_param._schoolBankMst, "BRANCHNAME");
//        final String accountname = KnjDbUtils.getString(_param._schoolBankMst, "ACCOUNTNAME");
        final Form form = new Form(_param, svf);

        final String footerFormname = "KNJL330F_FOOTER.frm";
        for (int j = 0; j < dataList.size(); j++) {
            final Map m = (Map) dataList.get(j);

            final String formfile;
            String[] flg = {Form.FLG_TATESEN};
            if ("1".equals(_param._applicantdiv)) {
                if (null != KnjDbUtils.getString(m, "HONORDIV") && !"2".equals(KnjDbUtils.getString(m, "HONORDIV"))) {
                    flg = new String[] {Form.FLG_TATESEN, Form.FLG_TOKUTAI};
                    formfile = "KNJL330F_J_TOKUTAI.frm";
                } else {
                    formfile = "KNJL330F_J.frm";
                }
            } else { // "2".equals(_param._applicantdiv)) {
                if ("6".equals(_param._testdiv)) {
                    // 一貫生用
                    formfile = "KNJL330F_H_2.frm";
                } else {
                    formfile = "KNJL330F_H.frm";
                    if (null != KnjDbUtils.getString(m, "HONORDIV")) {
                        flg = new String[] {Form.FLG_TATESEN, Form.FLG_TOKUTAI};
                    }
                }
            }
            form.setMergeForm(_param, formfile, footerFormname, flg);

            final String money = KnjDbUtils.getString(m, "MONEY");

            final String title;
            if (!"1".equals(_param._applicantdiv) && "6".equals(_param._testdiv)) {
                title = _param._entexamyear + "年度　進級に関する諸費用納入について";
            } else {
                title = _param._entexamyear + "年度　入学に関する諸費用納入について";
            }
            form.VrsOut("TITLE", title); // タイトル
            form.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名

//            List flist = new ArrayList();
//            flist.add("ERA_NAME");
//            flist.add("ERA_NAME2");
//            flist.add("ERA_NAME3");
//            putGengou2(db2, svf, flist);

            form.VrsOut("MONEY1", money); // 納入金額
            if (!"1".equals(_param._applicantdiv) && "6".equals(_param._testdiv)) {
                form.VrsOut("SELL_DATE", StringUtils.defaultString(formatDate(_param._salesDate)) + "　　　" + StringUtils.defaultString(_param._salesHour)); // 学用品販売日時
            } else {
                form.VrsOut("SELL_DATE", formatDate(_param._salesDate)); // 学用品販売日時
                form.VrsOut("SELL_TIME", _param._salesHour); // 学用品販売時間
                form.VrsOut("SELL_TIME2", _param._salesHour2); // 学用品販売時間
                form.VrsOut("SELL_TIME3", _param._salesHour3); // 学用品販売時間
            }
            form.VrsOut("SELL_PLACE", _param._salesLocation); // 学用品販売場所
            form.VrsOut("PAY_LIMIT1_1", formatDate(_param._limitDate)); // 納入期限
            form.VrsOut("MONEY2", money); // 金額
            form.VrsOut("MONEY3", money); // 金額
            form.VrsOut("MONEY4", money); // 金額
            form.VrsOut("EXAM_NO", KnjDbUtils.getString(m, "RECEPTNO")); // 受験番号
            form.VrsOut("BANKNAME", "三菱ＵＦＪ銀行　駒込支店");

            if ("1".equals(_param._applicantdiv)) {
                form.VrsOut("NOUNYU_LOC_COMMENT", "※　振り込み手続きを行う方の本人確認書類（運転免許証・健康保険証など）をご用意ください。");

                form.VrsOut("HAND1", "・振込受付証明書（学校提出用）");
                form.VrsOut("HAND2", "・配付する学用品が多量になりますので手提げ袋やキャリーバック等をお持ちください。");

                form.VrsOut("HAND_COMME1", "※諸費用納入の確認が取れない場合は、学用品をお受け取りいただけません。");
                form.VrsOut("HAND_COMME2", "※当日は、宅配便をご利用いただくことも可能です。なお、配送費用は保護者様ご負担となります。");

            } else { // if ("2".equals(_param._applicantdiv)) {
                if ("6".equals(_param._testdiv)) {
                    form.VrsOutn("HAND",  1, "・教科書、副教材代（現金）");
                    form.VrsOutn("HAND",  2, "・大きめの手提げ袋");
                    form.VrsOutn("HAND",  3, "・学納金・学用品費 振込受付証明書");
                } else {
                    form.VrsOutn("HAND",  1, "・「学納金・学用品費 振込受付証明書」（学校提出用）");
                    form.VrsOutn("HAND",  2, "・教科書、副教材代・通学かばん、リュック代（現金）");
                    form.VrsOutn("HAND",  3, "・教科書購入用紙、副教材購入用紙");
                    form.VrsOutn("HAND",  4, "・大きめの手提げ袋・キャリーバック等");
                    form.VrsOutn("HAND",  5, "　（配付する学用品が多量になります）");
                    if (!"2020".equals(_param._entexamyear)) {
                        form.VrsOutn("HAND",  6, "※同日実施の『スタディープログラム・すらら受講ガイダンス』の持ち物につきましては、別紙にて");
                        form.VrsOutn("HAND",  7, "　ご確認ください。");
                    }
                    form.VrsOutn("HAND",  8, "");
                    form.VrsOutn("HAND",  9, "※入学諸費用納入の確認が取れない場合は、学用品をお受け取りいただけません。");
                    form.VrsOutn("HAND", 10, "※当日は、宅配便をご利用いただくことも可能です。なお、配送費用はご自身の負担となります。");
                }
            }

            final String name = KnjDbUtils.getString(m, "NAME");
            form.VrsOut("NAME", name);
            final int namelen = KNJ_EditEdit.getMS932ByteLength(name);
            if (namelen > 20) {
                form.VrsOut("NAME2_2_1", name);
                form.VrsOut("NAME3_2_1", name);
            } else {
                form.VrsOut("NAME2", name);
                form.VrsOut("NAME3", name);
            }
            final String nameKanaHk = KnjDbUtils.getString(m, "NAME_KANA_HK");
            final int nameKanaHklen = KNJ_EditEdit.getMS932ByteLength(nameKanaHk);
            if (nameKanaHklen > 24) {
                form.VrsOut("KANA3_3_1", nameKanaHk);
            } else if (nameKanaHklen > 20) {
                form.VrsOut("KANA3_2_1", nameKanaHk);
            } else {
                form.VrsOut("KANA3", nameKanaHk);
            }
//            form.VrsOut("GNAME", getString(m, "GNAME"));
//            if (getMS932Bytecount(getString(m, "NAME")) > 20) {
//                form.VrsOut("GNAME2_1", getString(m, "GNAME"));
//            } else {
//                form.VrsOut("GNAME2", getString(m, "GNAME"));
//            }
//            final String addr = StringUtils.defaultString(getString(m, "ADDRESS1")) + StringUtils.defaultString(getString(m, "ADDRESS2"));
//            if (getMS932Bytecount(addr) > 48) {
//                final String[] split = KNJ_EditEdit.get_token(addr, 48, 2);
//                if (null != split) {
//                    for (int i = 0; i < split.length; i++) {
//                        form.VrsOut("ADDR_" + String.valueOf(i + 1), split[i]);
//                    }
//                }
//            } else {
//                form.VrsOut("ADDR", addr);
//            }
//            if (getMS932Bytecount(addr) > 20 * 2) {
//                final String[] split = KNJ_EditEdit.get_token(addr, 28, 3);
//                if (null != split) {
//                    for (int i = 0; i < split.length; i++) {
//                        form.VrsOut("ADDR3_" + String.valueOf(i + 1), split[i]);
//                    }
//                }
//            } else if (getMS932Bytecount(addr) > 20) {
//                final String[] split = KNJ_EditEdit.get_token(addr, 20, 2);
//                if (null != split) {
//                    for (int i = 0; i < split.length; i++) {
//                        form.VrsOut("ADDR2_" + String.valueOf(i + 1), split[i]);
//                    }
//                }
//            } else {
//                form.VrsOut("ADDR2", addr);
//            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

//    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List fieldList) {
//        //元号(記入項目用)
//        String[] dwk;
//        if (_param._loginDate.indexOf('/') >= 0) {
//            dwk = StringUtils.split(_param._loginDate, '/');
//        } else if (_param._loginDate.indexOf('-') >= 0) {
//            dwk = StringUtils.split(_param._loginDate, '-');
//        } else {
//            //ありえないので、固定値で設定。
//            dwk = new String[1];
//        }
//        if (dwk.length >= 3) {
//            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
//            for (final Iterator it = fieldList.iterator(); it.hasNext();) {
//                final String setFieldStr = (String) it.next();
//                svf.VrsOut(setFieldStr, gengou);
//            }
//        }
//    }

    private String formatDate(final String date) {
        if (null != date) {
            try {
                final StringBuffer stb = new StringBuffer();

                final Calendar cal = Calendar.getInstance();
                cal.setTime(Date.valueOf(date));
//                stb.append(toZenkaku(String.valueOf(cal.get(Calendar.YEAR)))).append("年");
//                stb.append(toZenkaku(String.valueOf(cal.get(Calendar.MONTH) + 1))).append("月");
//                stb.append(toZenkaku(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)))).append("日");
                stb.append(String.valueOf(cal.get(Calendar.YEAR))).append("年");
                stb.append(String.valueOf(cal.get(Calendar.MONTH) + 1)).append("月");
                stb.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH))).append("日");
                stb.append("（").append(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK))).append("）");

                return stb.toString();
            } catch (Exception e) {
                log.error("exception! date = " + date, e);
            }
        }
        return null;
    }

    private String toZenkaku(final String s) {
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            switch (ch) {
            case '1':stb.append("１"); break;
            case '2':stb.append("２"); break;
            case '3':stb.append("３"); break;
            case '4':stb.append("４"); break;
            case '5':stb.append("５"); break;
            case '6':stb.append("６"); break;
            case '7':stb.append("７"); break;
            case '8':stb.append("８"); break;
            case '9':stb.append("９"); break;
            case '0':stb.append("０"); break;
            default: stb.append(ch);
            }
        }
        return stb.toString();
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        String l025namespare;
        if ("1".equals(_param._applicantdiv)) {
            l025namespare  ="NAMESPARE1";
        } else {
            l025namespare  ="NAMESPARE2";
        }

        stb.append(" WITH T_EXEMPTION_CD_MONEY AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXEMPTION_CD, ");
        stb.append("     SUM(T2.ITEM_MONEY) AS MONEY ");
        stb.append(" FROM ENTEXAM_PAYMENT_EXEMPTION_MST T1 ");
        stb.append(" LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T2.DIV = '0' ");
        stb.append("     AND T2.ITEM_CD = T1.ITEM_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.DIV = '0' ");
        stb.append("     AND T1.KIND_CD = '2' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.EXEMPTION_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T2.NAME ");
        stb.append("     ,TREC.EXAMNO ");
        stb.append("     ,TREC.TESTDIV ");
        stb.append("     ,T2.NAME_KANA ");
        stb.append("     ,translate_h_hk(T2.NAME_KANA) AS NAME_KANA_HK ");
        stb.append("     ,L3.ABBV1 AS SEX_NAME ");
        stb.append("     ,T2.BIRTHDAY ");
        stb.append("     ,TREC.RECEPTNO ");
        stb.append("     ,TREC.TOTAL2 ");
        stb.append("     ,TREC.EXAM_TYPE ");
        stb.append("     ,NML013.NAME1 AS JUDGEDIV_NAME ");
        stb.append("     ,VXMD.EXEMPTION_CD ");
        stb.append("     ,VXMD.EXEMPTION_CD2 ");
        stb.append("     ,TXMD.MONEY AS MONEY0 ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     ,L025.NAMESPARE1 AS HONOR_DIV_NAMESPARE1 ");
        } else {
            stb.append("     ,L025.NAMESPARE2 AS HONOR_DIV_NAMESPARE2 ");
        }
        stb.append("     ,VXMD_GENE.EXEMPTION_CD AS EXEMPTION_CD_GENE "); // 確認用
        stb.append("     ,TXMD_GENE.MONEY AS MONEY_GENE "); // 確認用
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     ,L025_GENE." + l025namespare + " AS HONOR_DIV_NAMESPARE1_GENE ");
        } else {
            stb.append("     ,L025_GENE." + l025namespare + " AS HONOR_DIV_NAMESPARE2_GENE ");
        }
        stb.append("     ,CASE WHEN VALUE(T2.GENERAL_FLG, '') = '1' AND TREG_GENE.JUDGEDIV = '1' AND INT(VALUE(L025." + l025namespare + ", '99')) > INT(VALUE(L025_GENE." + l025namespare + ", '99')) ");
        stb.append("             THEN (CASE WHEN TXMD_GENE.MONEY IS NULL OR TXMD_GENE2.MONEY IS NULL THEN VALUE(TXMD_GENE.MONEY, TXMD_GENE2.MONEY)  ELSE  MIN(TXMD_GENE.MONEY, TXMD_GENE2.MONEY) END) ");
        stb.append("      ELSE        (CASE WHEN TXMD.MONEY      IS NULL OR TXMD2.MONEY      IS NULL THEN VALUE(TXMD.MONEY,      TXMD2.MONEY)       ELSE  MIN(TXMD.MONEY,      TXMD2.MONEY)      END) ");
        stb.append("      END AS MONEY ");
        stb.append("     ,CASE WHEN VALUE(T2.GENERAL_FLG, '') = '1' AND TREG_GENE.JUDGEDIV = '1' AND INT(VALUE(L025." + l025namespare + ", '99')) > INT(VALUE(L025_GENE." + l025namespare + ", '99')) THEN TREG_GENE.HONORDIV ELSE TREC.HONORDIV END AS HONORDIV ");
        stb.append("     ,APADDR.GNAME ");
        stb.append("     ,APADDR.ADDRESS1 ");
        stb.append("     ,APADDR.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND T2.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("                                    AND T2.EXAMNO       = TREC.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TD2 ON TD2.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND TD2.EXAMNO       = TREC.EXAMNO ");
        stb.append("                                    AND TD2.SEQ          = '012' ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T2.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = TREC.JUDGEDIV ");
        stb.append("     LEFT JOIN ENTEXAM_MONEY_DAT EMD ON EMD.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND EMD.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("                                    AND EMD.EXAMNO = T2.EXAMNO ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_EXEMPTION_DAT VXMD ON VXMD.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND VXMD.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("                                    AND VXMD.TESTDIV = TREC.TESTDIV ");
        stb.append("                                    AND VXMD.RECEPTNO = TREC.RECEPTNO ");
        stb.append("     LEFT JOIN T_EXEMPTION_CD_MONEY TXMD ON TXMD.EXEMPTION_CD = VXMD.EXEMPTION_CD ");
        stb.append("     LEFT JOIN T_EXEMPTION_CD_MONEY TXMD2 ON TXMD2.EXEMPTION_CD = VXMD.EXEMPTION_CD2 ");
        stb.append("     LEFT JOIN V_NAME_MST L025 ON L025.YEAR = TREC.ENTEXAMYEAR AND L025.NAMECD1 = 'L025' AND L025.NAMECD2 = TREC.HONORDIV ");

        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT TREG_GENE ON TREG_GENE.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("                                    AND TREG_GENE.APPLICANTDIV = TREC.APPLICANTDIV ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     AND TREG_GENE.TESTDIV = '5' ");
        } else {
            stb.append("     AND TREG_GENE.TESTDIV = '3' ");
        }
        stb.append("                                    AND TREG_GENE.EXAMNO = TREC.EXAMNO ");
        stb.append("                                    AND TREG_GENE.JUDGEDIV = '1' ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_EXEMPTION_DAT VXMD_GENE ON VXMD_GENE.ENTEXAMYEAR = TREG_GENE.ENTEXAMYEAR ");
        stb.append("                                    AND VXMD_GENE.APPLICANTDIV = TREG_GENE.APPLICANTDIV ");
        stb.append("                                    AND VXMD_GENE.TESTDIV = TREG_GENE.TESTDIV ");
        stb.append("                                    AND VXMD_GENE.RECEPTNO = TREG_GENE.RECEPTNO ");
        stb.append("     LEFT JOIN T_EXEMPTION_CD_MONEY TXMD_GENE ON TXMD_GENE.EXEMPTION_CD = VXMD_GENE.EXEMPTION_CD ");
        stb.append("     LEFT JOIN T_EXEMPTION_CD_MONEY TXMD_GENE2 ON TXMD_GENE2.EXEMPTION_CD = VXMD_GENE.EXEMPTION_CD2 ");
        stb.append("     LEFT JOIN V_NAME_MST L025_GENE ON L025_GENE.YEAR = TREG_GENE.ENTEXAMYEAR AND L025_GENE.NAMECD1 = 'L025' AND L025_GENE.NAMECD2 = TREG_GENE.HONORDIV ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
        stb.append("         AND TRDET003.APPLICANTDIV = TREC.APPLICANTDIV ");
        stb.append("         AND TRDET003.TESTDIV = TREC.TESTDIV ");
        stb.append("         AND TRDET003.EXAM_TYPE = TREC.EXAM_TYPE ");
        stb.append("         AND TRDET003.RECEPTNO = TREC.RECEPTNO ");
        stb.append("         AND TRDET003.SEQ = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT APADDR ON APADDR.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("                                    AND APADDR.EXAMNO = T2.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("         TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
        if ("1".equals(_param._applicantdiv)) {
            if ("5".equals(_param._testdiv)) {
                stb.append("     AND VALUE(T2.GENERAL_FLG, '') <> '1' ");
            }
        } else if ("2".equals(_param._applicantdiv)) {
            stb.append("     AND TRDET003.REMARK1 = '" + _param._testdiv0 + "' ");
            if ("3".equals(_param._testdiv)) {
                stb.append("     AND VALUE(T2.GENERAL_FLG, '') <> '1' ");
            }
        }
        if ("1".equals(param._output)) {
            stb.append("     AND TREC.JUDGEDIV = '1' ");
        } else if ("2".equals(param._output)) {
            stb.append("     AND TREC.JUDGEDIV = '1' ");
            stb.append("     AND T2.PROCEDUREDIV = '1' ");
            stb.append("     AND EMD.ENT_PAY_DATE IS NOT NULL ");
            stb.append("     AND EMD.ENT_PAY_MONEY IS NOT NULL ");
        } else if ("3".equals(param._output)) {
            stb.append("     AND TREC.RECEPTNO BETWEEN '" + param._fexamno + "' AND '" + param._texamno + "' ");
        }
        stb.append(" ORDER BY TREC.RECEPTNO ");
        return stb.toString();
    }

    private static class Form {
        final Param _param;
        final Vrw32alp _svf;
        String _currentFormname;
        Map _fieldInfoMap = new HashMap();

        private static String FLG_TATESEN = "FLG_TATESEN";
        private static String FLG_TOKUTAI = "FLG_TOKUTAI";
        private static String FLG_MENJO = "FLG_MENJO";

        public Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        public void setForm(final String formname) {
            _svf.VrSetForm(formname, 1);
            _currentFormname = formname;
            log.info(" set form " + formname);
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                _fieldInfoMap.put(_currentFormname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        private static Map getMappedMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap());
            }
            return (Map) map.get(key1);
        }

        public void setMergeForm(final Param param, final String formname, final String formname2, final String[] flg) {
            final String key = formname + " / " + formname2 + ArrayUtils.toString(flg);
            log.info(" set form key = " + key + " / " + param._mergedFormMap.containsKey(key));
            if (!param._mergedFormMap.containsKey(key)) {
                File newFile = null;
                try {
                    final String formPath = _svf.getPath(formname);
                    final File formFile = new File(formPath);
                    final String formPath2 = _svf.getPath(formname2);
                    final File formFile2 = new File(formPath2);
                    if (_param._isOutputDebug) {
                        log.info(" form path = " + formPath + " / " + formPath2);
                    }
                    if (formFile.exists()) {
                        SvfForm svfForm = new SvfForm(formFile);
//    					svfForm._debug = true;
                        if (svfForm.readFile()) {

                            // 振込用紙設定
                            if (ArrayUtils.contains(flg, FLG_TATESEN)) {
                                final int y1 = 3128, y2 = 3266;
                                for (int i = 1; i <= 7; i++) {
                                    final int x = 3128 - (3128 - 2018) / 7 * i;
                                    svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x, y1), new SvfForm.Point(x, y2)));
                                }

                                for (int i = 1; i <= 6; i++) {
                                    final int x2 = 4356 - (4356 - 3800) / 7 * i;
                                    svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x2, y1), new SvfForm.Point(x2, y2)));
                                }
                            }
                            for (int i = 7; i <= 7; i++) {
                                final int y1 = 3128, y2 = 3266;
                                final int x2 = 4356 - (4356 - 3800) / 7 * i;
                                svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x2, y1), new SvfForm.Point(x2, y2)));
                            }

                            for (int i = 1; i <= 6; i++) {
                                final int x = 6454 - (6454 - 5900) / 7 * i;
                                final int y1 = ArrayUtils.contains(flg, FLG_MENJO) ? 3266 : 3128;
                                final int y2 = 3642;
                                svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x, y1), new SvfForm.Point(x, y2)));
                            }

                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji("学納金・学用品費 振込受付証明書（学校提出用）", new SvfForm.Point(596, 2815), 110).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji("学納金・学用品費・振込領収書", new SvfForm.Point(3404, 2824), 110).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji("学納金・学用品費・振込依頼書", new SvfForm.Point(5031, 2828), 110).setFont(SvfForm.Font.Gothic).setFText(true));

                            if (ArrayUtils.contains(flg, FLG_TOKUTAI)) {
                                svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THINEST, new SvfForm.Point(151, 2792), new SvfForm.Point(421, 2942)));
                                svfForm.addKoteiMoji(new SvfForm.KoteiMoji("特待", new SvfForm.Point(213, 2831), 130).setFont(SvfForm.Font.Gothic).setFText(true));
                            }
//    						svfForm.addField(new SvfForm.Field(null, "MENJO3", 1, 10, 5971, false, new SvfForm.Point(5971, 3154), 150, "0", "0", "1", "0", "0", "0", comment, "", ""));

                            final String schoolname;
                            final String account, account2;
                            int schoolname2x1;
                            if ("1".equals(param._applicantdiv)) {
                                schoolname = " 文京学院大学女子中学校";
                                schoolname2x1 = 3739;
                                account = "（普）№　0951716";
                                account2 = "（普） № 0951716";
                            } else {
                                schoolname = "文京学院大学女子高等学校";
                                schoolname2x1 = 3709;
                                account = "（普）№　0930748";
                                account2 = "（普） № 0930748";
                            }
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(schoolname, new SvfForm.Point(1424, 3562), 120).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(schoolname, new SvfForm.Point(schoolname2x1, 3569), 95).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(account, new SvfForm.Point(2481, 3442), 120).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(account2, new SvfForm.Point(3850, 3480), 90).setFont(SvfForm.Font.Gothic).setFText(true));
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(account2, new SvfForm.Point(5078, 3342), 90).setFont(SvfForm.Font.Gothic).setFText(true));

                            if (formFile2.exists()) {
                                SvfForm svfForm2 = new SvfForm(formFile2);
//		    					svfForm2._debug = true;
                                if (svfForm2.readFile()) {

                                    svfForm.addAllElement(svfForm2.getAllElementList());

                                    newFile = svfForm.writeTempFile();
                                    log.info(" create file " + newFile.getPath());
                                }
                            } else {
                                log.error("read file error: " + formFile2);
                            }

                        } else {
                            log.error("read file error: " + formPath);
                        }
                    } else {
                        log.error("read file error: " + formFile);
                    }
                } catch (Throwable e) {
                    log.error("throwed ", e);
                }
                param._mergedFormMap.put(key, newFile);
                if (null == newFile) {
                    throw new IllegalStateException(" form = " + formname + " / " + formname2);
                }
            }
            _currentFormname = param._mergedFormMap.get(key).getName();
            log.info(" merged = " + _currentFormname);
            _svf.VrSetForm(_currentFormname, 1);
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                _fieldInfoMap.put(_currentFormname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }

        }

        public SvfField getField(final String fieldname) {
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                log.error(" not set form ! : " + _currentFormname);
                return null;
            }
            return (SvfField) getMappedMap(_fieldInfoMap, _currentFormname).get(fieldname);
        }


        public int VrsOut(final String fieldname, final String data) {
            SvfField field = getField(fieldname);
            if (null == field) {
                _param.logwarn(" no such field : " + fieldname + " (data = '" + data + "')");
                return -1;
            }
            if (_param._isOutputDebug) {
                log.info("VrsOut('" + fieldname + "', " + (null == data ? data : "'" + data + "'") + ");");
            }
            return _svf.VrsOut(fieldname, data);
        }

        public int VrsOutn(final String fieldname, final int gyo, final String data) {
            SvfField field = getField(fieldname);
            if (null == field) {
                _param.logwarn(" no such field : " + fieldname + " (data = '" + data + "')");
                return -1;
            }
            if (_param._isOutputDebug) {
                log.info("VrsOutn('" + fieldname + "', " + gyo + ", " + (null == data ? data : "'" + data + "'") + ");");
            }
            return _svf.VrsOutn(fieldname, gyo, data);
        }

        public int VrAttribute(final String fieldname, final String attribute) {
            SvfField field = getField(fieldname);
            if (null == field) {
                _param.logwarn(" no such field : " + fieldname + " (attribute = '" + attribute + "')");
                return -1;
            }
            if (_param._isOutputDebug) {
                log.info("VrAttribute('" + fieldname + "', " + (null == attribute ? attribute : "'" + attribute + "'") + ");");
            }
            return _svf.VrAttribute(fieldname, attribute);
        }

        public int VrAttributen(final String fieldname, final int gyo, final String attribute) {
            SvfField field = getField(fieldname);
            if (null == field) {
                _param.logwarn(" no such field : " + fieldname + " (attribute = '" + attribute + "')");
                return -1;
            }
            if (_param._isOutputDebug) {
                log.info("VrAttributen('" + fieldname + "', " + gyo + ", " + (null == attribute ? attribute : "'" + attribute + "'") + ");");
            }
            return _svf.VrAttributen(fieldname, gyo, attribute);
        }

        private String attributeIntPlus(final String fieldname, final String intProperty, final int plus) {
            SvfField field = getField(fieldname);
            if (null == field) {
                _param.logwarn(" no such field : " + fieldname + " (attribute = '" + intProperty + "')");
                return "";
            }

            final int propVal = toInt((String) field.getAttributeMap().get(intProperty), 10000);
            return intProperty + "=" + String.valueOf(propVal + plus);
        }

        static int toInt(final String str, final int def) {
            return NumberUtils.isNumber(str) ? new BigDecimal(str).intValue() : def;
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdiv0; // 入試回数
        final String _output; // 1:合格者全員 2:手続き積み者全員 3:受験者指定
        final String _fexamno; // 受験者指定受験番号From
        final String _texamno; // 受験者指定受験番号To
        final String _limitDate;
        final String _salesDate;
        String _salesHour = "";
        String _salesHour2 = "";
        String _salesHour3 = "";
        String _salesLocation = "";

        final String _applicantdivname;
        final String _testdivname;
        final String _testdiv0name;
        final Map _schoolBankMst;
        private final boolean _isOutputDebug;
        private final Set<String> _logOnce = new TreeSet<String>();
        private Map<String, File> _mergedFormMap = new HashMap<String, File>();

        private boolean _seirekiFlg;
        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _testdiv0     = request.getParameter("TESTDIV0");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _fexamno = request.getParameter("F_EXAMNO");
            _texamno = request.getParameter("T_EXAMNO");
            _limitDate    = KNJ_EditDate.H_Format_Haifun(request.getParameter("LIMIT_DATE"));
            _salesDate    = KNJ_EditDate.H_Format_Haifun(request.getParameter("SALES_DATE"));
            try {
                _salesHour    = new String(StringUtils.defaultString(request.getParameter("SALES_HOUR")).getBytes("ISO8859-1"));
                _salesHour2   = new String(StringUtils.defaultString(request.getParameter("SALES_HOUR2")).getBytes("ISO8859-1"));
                _salesHour3   = new String(StringUtils.defaultString(request.getParameter("SALES_HOUR3")).getBytes("ISO8859-1"));
                _salesLocation= new String(StringUtils.defaultString(request.getParameter("SALES_LOCATION")).getBytes("ISO8859-1"));
            } catch (Exception e) {
                log.error("exception!", e);
            }


            setCertifSchoolDat(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestDivName(db2);
            _testdiv0name = getTestdiv0Name(db2);
            _schoolBankMst = getSchoolBankMst(db2);
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJL330F", "outputDebug"));
        }

        public void close() {
            for (final File file : _mergedFormMap.values()) {
                log.info(" file " + file.getPath() + " : delete? " + file.delete());
            }
        }

        public void logwarn(final String s) {
            if (null == s || _logOnce.contains(s)) {
                return;
            }
            log.warn(s);
            _logOnce.add(s);
        }

        private String getApplicantdivName(DB2UDB db2) {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'")));
        }

        private String getTestDivName(DB2UDB db2) {
            final String namecd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv + "'")));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (_applicantdiv.equals("1")) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _principalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _jobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"));
            _schoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            return seirekiFlg;
        }

//        private String gethiduke(final String inputDate) {
//            // 西暦か和暦はフラグで判断
//            String date;
//            if (null != inputDate) {
//                if (_seirekiFlg) {
//                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
//                } else {
//                    date = KNJ_EditDate.h_format_JP(inputDate);
//                }
//                return date;
//            }
//            return null;
//        }

        private String getTestdiv0Name(final DB2UDB db2) {
            final String namecd1 = "L034";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv0 + "'")));
        }

        private Map getSchoolBankMst(final DB2UDB db2) {
            final String cd = "1".equals(_applicantdiv) ? "1020" : "2020";
            final String sql = " SELECT NMG203.ABBV1 AS DEPOSIT_ITEM_NAME, T1.* "
                    + " FROM SCHOOL_BANK_MST T1 "
                    + " LEFT JOIN NAME_MST NMG203 ON NMG203.NAMECD1 = 'G203' AND NMG203.NAMECD2 = T1.DEPOSIT_ITEM "
                    + " WHERE BANKTRANSFERCD = '" + cd + "' ";
            Map rtn = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            return rtn;
        }

    }
}//クラスの括り

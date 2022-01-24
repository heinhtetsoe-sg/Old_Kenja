// kanji=漢字
/*
 * $Id: d75e68227d5948ee7e67650fd4343490328fca9c $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJE.KNJE070_1;
import servletpack.KNJE.KNJE070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [卒業生管理] 卒業生用高校調査書
 */
public class KNJI060D {

    private static final Log log = LogFactory.getLog(KNJI060D.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: $");
        KNJServletUtils.debugParam(request, log);

        //  パラメーターを受取
        final Param param = new Param(request);

        //  print設定
        response.setContentType("application/pdf");
        final OutputStream outstrm = response.getOutputStream();

        final Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        //  ＤＢ接続
        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJI060]DB2 open error!", ex);
        }

        //  ＳＶＦ設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        //  ＳＶＦ作成
        boolean nonedata = false;                   //該当データなしフラグ
        final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定
        if ("1h".equals(param._output) || "2h".equals(param._output)) {
            param.paramap.put("HANKI_NINTEI", "1"); // 半期認定の評定・単位数のフォーム
        }
        if ("1".equals(param._output) || "1h".equals(param._output)) {
            nonedata = ent_paper(db2, svf, param, _definecode);               //進学用のPAPER作成
        } else if ("2".equals(param._output) || "2h".equals(param._output)) {
            nonedata = emp_paper(db2, svf, param, _definecode);               //就職用のPAPER作成
        }

        //  終了処理
        if (nonedata) {
            svf.VrQuit();
        }
        db2.commit();
        db2.close();

    } //public void doGetの括り

    /**進学用のPAPER作成**/
    private boolean ent_paper(final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final KNJDefineSchool definecode) {

        final KNJE070_1 pobj = new KNJE070_1(db2, svf, definecode, param._useSyojikou3);
        pobj.pre_stat(param._hyotei, param.paramap);
        final Map<String, String> certifKindMap = new TreeMap<String, String>();
        PreparedStatement ps = null;
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.CERTIF_KINDCD ");
            sql.append(" FROM CERTIF_KIND_MST T1 ");
            if (!StringUtils.isBlank(param._date)) {
                sql.append(" INNER JOIN CERTIF_KIND_YDAT T2 ON T2.YEAR = FISCALYEAR(?) AND T2.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
            } else {
                sql.append(" INNER JOIN CERTIF_KIND_YDAT T2 ON T2.YEAR = ? AND T2.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
            }
            sql.append(" INNER JOIN CERTIF_SCHOOL_DAT T3 ON T3.YEAR = T2.YEAR AND T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
            sql.append(" WHERE ");
            sql.append("     T1.CERTIF_KINDCD IN ('058', '059') ");
            sql.append("     AND T3.YEAR IS NOT NULL ");
            ps = db2.prepareStatement(sql.toString());

        } catch (Exception e) {
            log.error("exception!", e);
        }
        for (final Map<String, String> map : param.paramList) {

            final String year = map.get("YEAR");
            if (!certifKindMap.containsKey(year)) {
                Object[] qparam;
                if (!StringUtils.isBlank(param._date)) {
                    qparam = new Object[] { StringUtils.replace(param._date, "/", "-") };
                } else {
                    qparam = new Object[] { year };
                }
                final List<String> certifKinds = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, ps, qparam), "CERTIF_KINDCD");
                final String certifKind;
                if (certifKinds.contains("059")) {
                    certifKind = "059";
                } else if (certifKinds.contains("058")) {
                    certifKind = "058";
                } else {
                    certifKind = "025";
                }
                certifKindMap.put(year, certifKind);
            }
            final String certifKind = certifKindMap.get(year);
            log.info(" certifKind = " + certifKind);
            param.paramap.put("CERTIFKIND", certifKind);  // 証明書種別
            param.paramap.put("CERTIFKIND2", certifKind);  // 証明書学校データ備考

            pobj.printSvf(map.get("SCHREGNO"), year, map.get("SEMESTER"), param._date, param._seki, param._kanji, param._comment, param._oadiv, null, param.paramap);
        }
        DbUtils.closeQuietly(ps);
        pobj.pre_stat_f();
        return pobj.nonedata;
    }//ent_paterの括り

    /**就職用のPAPER作成**/
    private boolean emp_paper(final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final KNJDefineSchool definecode) {
        param.paramap.put("CERTIFKIND", "026");  // 証明書種別
        param.paramap.put("CERTIFKIND2", "026");  // 証明書学校データ備考

        final KNJE070_2 pobj = new KNJE070_2(db2, svf, definecode, param._useSyojikou3);
        pobj.pre_stat(param._hyotei, param.paramap);
        for (final Map<String, String> map : param.paramList) {
            pobj.printSvf(map.get("SCHREGNO"), map.get("YEAR"), map.get("SEMESTER"), param._date, param._seki, param._kanji, null, param._oadiv, null, param.paramap);
        }
        pobj.pre_stat_f();
        return pobj.nonedata;
    }//emp_paperの括り

    private static class Param {
        final String _year;
        final String _gakki;
        final String _seki;
        final String _kanji;
        final String _date;
        final String _comment;
        final String _oadiv;
        final String _output; // 出力種別(1:進学 2:就職)
        final String _hyotei;
        final String _useSyojikou3;

        final Map paramap = new HashMap();        //HttpServletRequestからの引数
        final List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();

        Param(final HttpServletRequest request) {

            /*  << request.Parameter >>
            OUTPUT      1               調査書種類 1進学用: 2:就職用
        5   KANJI       1               漢字氏名出力 1:する 2:しない
        6   DATE        2003/06/05      記載日付
        3   SEKI        999999          記載責任者
        7   SCHREGNO    181010,181241   学籍番号
        0   G_YEAR      2002,2002       卒業時年度
        1   G_SEMESTER  3,3             卒業時学期
        2   G_GRADE     3,3             卒業時学年
            */

            _year = request.getParameter("YEAR"); //年度
            _gakki = request.getParameter("GAKKI"); //学期
            _seki = request.getParameter("SEKI"); //記載責任者
            _kanji = request.getParameter("KANJI"); //漢字出力
            _hyotei = request.getParameter("HYOTEI");
            _useSyojikou3 = request.getParameter("useSyojikou3");

            //記載日がブランクの場合桁数０で渡される事に対応
            if (request.getParameter("DATE") != null && 3 < request.getParameter("DATE").length()) {
                _date = request.getParameter("DATE");                            //処理日付
            } else {
                _date = null;
            }

            final StringTokenizer stkx = new StringTokenizer(request.getParameter("SCHREGNO"), ",");      //学籍番号
            final StringTokenizer stky = new StringTokenizer(request.getParameter("G_YEAR"), ",");        //卒業年度
            final StringTokenizer stkz = new StringTokenizer(request.getParameter("G_SEMESTER"), ",");    //卒業学期
            final StringTokenizer stkt = new StringTokenizer(request.getParameter("G_GRADE"), ",");       //卒業学年
            while (stkx.hasMoreTokens() && stky.hasMoreTokens() && stkz.hasMoreTokens() && stkt.hasMoreTokens()) {
                final Map<String, String> map = new HashMap<String, String>();
                map.put("SCHREGNO", stkx.nextToken());
                map.put("YEAR", stky.nextToken());
                map.put("SEMESTER", stkz.nextToken());
                map.put("GRADE", stkt.nextToken());
                paramList.add(map);
            }
            _comment = request.getParameter("COMMENT");                         //学習成績概評
            _oadiv = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000
            _output = request.getParameter("OUTPUT");

            putParam(paramap, request, "CTRL_YEAR");  //今年度
            putParam(paramap, request, "MIRISYU");  // 未履修科目を出力する:1 しない:2
            putParam(paramap, request, "FORM6");  // ６年生用フォーム
            putParam(paramap, request, "RISYU");  // 履修のみ科目出力　1:する／2:しない
            putParam(paramap, request, "useCurriculumcd");
            putParam(paramap, request, "useClassDetailDat");
            putParam(paramap, request, "useAddrField2");
            putParam(paramap, request, "useProvFlg");
            putParam(paramap, request, "useGakkaSchoolDiv");
            putParamDef(paramap, request, "OS", "1");  //ＯＳ区分 1:XP 2:WINDOWS2000
            putParamDef(paramap, request, "TANIPRINT_SOUGOU", "1");  // "総合的な学習の時間"修得単位数の計 "0"表示
            putParamDef(paramap, request, "TANIPRINT_RYUGAKU", "1");  // 留学修得単位数の計 "0"表示
            putParamDef(paramap, request, "HYOTEI", "off");  // 評定の読み替え
            putParamBl(paramap, request, "useSyojikou3");
            putParamBl(paramap, request, "certifNoSyudou");
            putParamBl(paramap, request, "gaihyouGakkaBetu");
            putParamBl(paramap, request, "train_ref_1_2_3_field_size");
            putParamBl(paramap, request, "train_ref_1_2_3_gyo_size");
            putParamBl(paramap, request, "3_or_6_nenYoForm");
            putParamBl(paramap, request, "tyousasyoSougouHyoukaNentani");
            putParamBl(paramap, request, "NENYOFORM");
            putParamBl(paramap, request, "tyousasyoTokuBetuFieldSize");
            putParamBl(paramap, request, "tyousasyoEMPTokuBetuFieldSize");
            putParamBl(paramap, request, "tyousasyoKinsokuForm");
            putParamBl(paramap, request, "tyousasyoNotPrintAnotherAttendrec");
            putParamBl(paramap, request, "tyousasyoNotPrintAnotherStudyrec");
            putParamReplace(paramap, request, "tyousasyoAttendrecRemarkFieldSize");
            putParamReplace(paramap, request, "tyousasyoTotalstudyactFieldSize");
            putParamReplace(paramap, request, "tyousasyoTotalstudyvalFieldSize");
            putParamReplace(paramap, request, "tyousasyoSpecialactrecFieldSize");
            putParamWithName(paramap, "CTRL_DATE", request, "DATE");  //学籍処理日

            final List<String> containedParam = Arrays.asList("CTRL_DATE", "SCHREGNO", "G_YEAR", "G_SEMESTER", "G_GRADE");
            for (final Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
                final String name = en.nextElement();
                if (paramap.containsKey(name) || containedParam.contains(name)) {
                    continue;
                }
                paramap.put(name, request.getParameter(name));
            }
            paramap.put("PRINT_GRD", "1");
        }

        // パラメータで' 'が'+'に変換されるのでもとに戻す
        private void putParamReplace(final Map paramap, final HttpServletRequest req, final String name) {
            paramap.put(name, StringUtils.replace(StringUtils.defaultString(req.getParameter(name), ""), "+", " "));
        }

        // パラメータがnullの場合デフォルト値に置換
        private void putParamDef(final Map paramap, final HttpServletRequest req, final String name, final String defVal) {
            paramap.put(name, StringUtils.defaultString(req.getParameter(name), defVal));
        }

        // パラメータがnullの場合""に置換
        private void putParamBl(final Map paramap, final HttpServletRequest req, final String name) {
            putParamDef(paramap, req, name, "");
        }

        private void putParam(final Map paramap, final HttpServletRequest req, final String name) {
            paramap.put(name, req.getParameter(name));
        }

        private void putParamWithName(final Map paramap, final String paramname, final HttpServletRequest req, final String name) {
            paramap.put(paramname, req.getParameter(name));
        }
    }
}//クラスの括り

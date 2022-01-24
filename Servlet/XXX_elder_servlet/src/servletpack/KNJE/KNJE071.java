// kanji=漢字
/*
 * $Id: 9ca327cbe53aef9c8afb2f843aeccc4aa4e77ef6 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書
 */

public class KNJE071
{
    private static final Log log = LogFactory.getLog(KNJE071.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        final Param param = createParam(request);

        final KNJDefineSchool definecode = new KNJDefineSchool();  //各学校における定数等設定
        final Map paramap = createParamap(request, definecode, param); //HttpServletRequestからの引数

        // ＤＢ接続
        final String dbname = request.getParameter("DBNAME");
        final DB2UDB db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        final Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        // ＳＶＦ設定
        response.setContentType("application/pdf");
        final OutputStream outstrm = response.getOutputStream();
        int ret;
        ret = svf.VrInit();                         //クラスの初期化
        if (ret != 0) {
            log.fatal("SVF初期化失敗!");
        }
        ret = svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定
        if (ret != 0) {
            log.fatal("SVF VrSetSpoolFileStream失敗!");
        }
        boolean hasdata = false;                   //該当データなしフラグ
        try {
            // ＳＶＦ作成
            final String[] printData = StringUtils.split(request.getParameter("printData"), ",");   // 学籍番号
            hasdata = ent_paper(db2, svf, definecode, param, printData, paramap);     //進学用のPAPER作成

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            outstrm.close();
        }
    }

    private Map createParamap(final HttpServletRequest req, final KNJDefineSchool definecode, final Param param) {
        final Map map = new HashMap();        

        if ("TOK".equals(definecode.schoolmark)) {
            map.put("NUMBER", "");
        }

        putParam(map, req, "OS");  //ＯＳ区分
        putParam(map, req, "CTRL_YEAR");  //今年度
        putParam(map, req, "MIRISYU");  // 未履修科目を出力する:1 しない:2
        putParam(map, req, "SONOTAJUUSYO");  // その他住所を優先して表示する。
        putParam(map, req, "HYOTEI");  // 評定の読み替え offの場合はparamapに追加しない。
        putParam(map, req, "FORM6");  // ６年生用フォーム offの場合はparamapに追加しない。
        putParam(map, req, "PRGID");                          // プログラムID
        putParam(map, req, "RISYU");  // 履修のみ科目出力　1:する／2:しない
        putParam(map, req, "useCurriculumcd");
        putParam(map, req, "useClassDetailDat");
        putParam(map, req, "useAddrField2");
        putParam(map, req, "useProvFlg");
        putParam(map, req, "useGakkaSchoolDiv");
        putParamDef(map, req, "TANIPRINT_SOUGOU", "1"); 
        putParamDef(map, req, "TANIPRINT_RYUGAKU", "1"); 
        putParamBl(map, req, "useSyojikou3");
        putParamBl(map, req, "certifNoSyudou");
        putParamBl(map, req, "useCertifSchPrintCnt");
        putParamBl(map, req, "gaihyouGakkaBetu");
        putParamBl(map, req, "train_ref_1_2_3_field_size");
        putParamBl(map, req, "train_ref_1_2_3_gyo_size");
        putParamBl(map, req, "3_or_6_nenYoForm");
        putParamBl(map, req, "tyousasyoSougouHyoukaNentani");
        putParamBl(map, req, "NENYOFORM");
        putParamBl(map, req, "tyousasyoTokuBetuFieldSize");
        putParamBl(map, req, "tyousasyoEMPTokuBetuFieldSize");
        putParamBl(map, req, "tyousasyoKinsokuForm");
        putParamBl(map, req, "tyousasyoNotPrintAnotherAttendrec");
        putParamBl(map, req, "tyousasyoNotPrintAnotherStudyrec");
        putParamReplace(map, req, "tyousasyoAttendrecRemarkFieldSize");
        putParamReplace(map, req, "tyousasyoTotalstudyactFieldSize");
        putParamReplace(map, req, "tyousasyoTotalstudyvalFieldSize");
        putParamReplace(map, req, "tyousasyoSpecialactrecFieldSize");
        putParamWithName(map, "CTRL_DATE", req, "DATE");  //学籍処理日
        putParamWithName(map, "OUTPUT_PRINCIPAL", req, "KOTYO");

        final List containedParam = Arrays.asList(new String[] {"CTRL_DATE", "OUTPUT_PRINCIPAL", "category_selected"});
        for (final Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
            final String name = (String) en.nextElement();
            if (map.containsKey(name) || containedParam.contains(name)) {
                continue;
            }
            map.put(name, req.getParameter(name));
        }
        return map;
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
    
    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    /**進学用のPAPER作成**/
    public boolean ent_paper(final DB2UDB db2, final Vrw32alpWrap svf, final KNJDefineSchool definecode, final Param param, final String[] printData, final Map srcParamap) throws SQLException
    {
        final KNJE070_1 pobj = new KNJE070_1(db2, svf, definecode, (String) srcParamap.get("useSyojikou3"));
        pobj.pre_stat(param._hyotei, srcParamap);

        for (int len = 0; len < printData.length; len++) {
            log.debug(" line = " + printData[len]);
            final String[] val = StringUtils.split(printData[len], "-");                                  //対象学籍番号
            final String schregno = val[1];
            String number = null;
            if (val.length > 2) {
                number = val[2];
            }
            
            final Map paramap = new HashMap(srcParamap);
            
            
            if (useCertifKindGrd(db2, param._year, schregno, param._date)) {
                paramap.put("CERTIFKIND", "025");  // 証明書種別
                paramap.put("CERTIFKIND2", "025");  // 証明書学校データ備考
            } else {
                paramap.put("CERTIFKIND", "008");  // 証明書種別
                paramap.put("CERTIFKIND2", "008");  // 証明書学校データ備考
            }
            final int schPrintCnt = paramap.get("useCertifSchPrintCnt").equals("1") ? getPrintCnt(db2, "008", schregno) : 1;
            for (int i = 0; i < schPrintCnt; i++) {
                pobj.printSvf(schregno, param._year, param._gakki, param._date, param._staffcd, param._kanji, param._comment, param._os, number, paramap);
            }
        }

        pobj.pre_stat_f();
        return pobj.nonedata;
    }

    private int getPrintCnt(final DB2UDB db2, final String certifKind, final String schregNo) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MAX(PRINT_CNT) AS PRINT_CNT, ");
        stb.append("     COUNT(PRINT_CNT) AS CNT ");
        stb.append(" FROM ");
        stb.append("     CERTIF_SCH_PRINT_CNT_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = '" + schregNo + "' ");
        stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        int retVal = 1;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retVal = rs.getInt("CNT") > 0 ? rs.getInt("PRINT_CNT") : 1;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retVal;
    }

    /**
     * 証明書番号の年度を取得します。
     */
    private String getCertificateNum(
            final DB2UDB db2,
            final String pnendo
    ) {
        final int intNendo = Integer.parseInt(pnendo);
        final String gengou = KenjaProperties.gengou(intNendo);

        final String str = gengou.substring(2, 4);
        if( str == null ) {
            return "";
        }
        return str;
    }

    /**
     * 証明書種別は卒業生のものを使用するか
     * @param db2
     * @param string
     * @return
     */
    private boolean useCertifKindGrd(final DB2UDB db2, final String year, final String schregno, final String date) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean boo = false;
        try {
            // 卒業区分が[nullではなく'4'(卒業見込み)以外]なら証明書種別は卒業生のものを使用する。
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHOOL_KIND AS ( ");
            stb.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHREGNO = '" + schregno + "' ");
            stb.append("         AND T2.YEAR = '" + year + "' ");
            stb.append(" ) ");
            stb.append(" SELECT CASE WHEN (T1.GRD_DIV IS NOT NULL AND T1.GRD_DIV <> '4' ");
            if (null != date) {
                stb.append("          AND T1.GRD_DATE <= '" + date.replace('/', '-') + "' ");
            }
            stb.append("        ) THEN 1 ELSE 0 END AS DIV ");
            stb.append(" FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
            stb.append(" INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            final String sql = stb.toString();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                boo = "1".equals(rs.getString("DIV"));
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return boo;
    }
    
    private class Param {
        final String _year;
        final String _gakki;
        final String _staffcd;
        final String _hyotei;
        final String _kanji;
        final String _date;
        final String _comment;
        final String _os;
        
        public Param(final HttpServletRequest request) {

            //記載日がブランクの場合桁数０で渡される事に対応
            String param6 = null;
            if (request.getParameter("DATE") != null  &&  3 < request.getParameter("DATE").length()) {
                param6 = request.getParameter("DATE");                            //処理日付
            }

            _year = request.getParameter("YEAR");                            //年度
            _gakki = request.getParameter("GAKKI");                           //学期
            _staffcd = request.getParameter("SEKI");                            //記載責任者
            _hyotei = StringUtils.defaultString(request.getParameter("HYOTEI"), "off");                          //評定の読み替え
            _kanji = request.getParameter("KANJI");                           //漢字出力
            _date = param6;
            _comment = request.getParameter("COMMENT");                         //学習成績概評
            _os = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000
        }
    }
}

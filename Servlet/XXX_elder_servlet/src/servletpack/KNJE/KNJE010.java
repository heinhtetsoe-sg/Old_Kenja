// kanji=漢字
/*
 * $Id: 64beb72250a34eb379beb069f77ca68db4710db2 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

public class KNJE010 {

	private static final Log log = LogFactory.getLog(KNJE010.class);

    protected boolean nonedata;                   //該当データなしフラグ
    final private KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定

    private String _year;
    private String _gakki;
    private String _grade;
    private String _staffcd;
    private String _hyotei;
    private String _kanji;
    private String _date;
    private String _comment;
    private String _os;
    private String _prgid;
    private String _kotyo;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.fatal("$Revision: 76366 $ $Date: 2020-09-03 10:09:41 +0900 (木, 03 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Map paramap = createParamap(request, this); //HttpServletRequestからの引数

        final Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        // ＤＢ接続
        final String dbname = request.getParameter("DBNAME");
        final DB2UDB db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

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

        // ＳＶＦ作成
        final String schregno = request.getParameter("SCHREGNO");   // 学籍番号
        final String output = request.getParameter("OUTPUT");
        if (Integer.parseInt(output) == 1) {
        	String certifKindcd = "008";
        	if (null != request.getParameter("tyousasyo2020")) {
        		final boolean hasCertifKindMst058 = KnjDbUtils.query(db2, " SELECT CERTIF_KINDCD AS COUNT FROM CERTIF_KIND_MST WHERE CERTIF_KINDCD = '058' ").size() > 0;
        		final boolean hasCertifSchool058 = KnjDbUtils.query(db2, " SELECT CERTIF_KINDCD FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '058' ").size() > 0;
        		if (hasCertifKindMst058 && hasCertifSchool058) {
        			certifKindcd = "058";
        		}
        	}
        	
            ent_paper(db2, svf, this, schregno, paramap, certifKindcd);     //進学用のPAPER作成
        } else {
            emp_paper(db2, svf, this, schregno, paramap);     //就職用のPAPER作成
        }

        // 終了処理
        if (nonedata == true) {
            svf.VrQuit();
        }
        db2.commit();
        db2.close();
        outstrm.close();
    }

    private Map createParamap(final HttpServletRequest request, final KNJE010 param) {
        param._year = request.getParameter("PRINT_YEAR");                            //年度
        param._gakki = request.getParameter("PRINT_GAKKI");                           //学期

        // '学年＋組'パラメータを分解
        final String strx = request.getParameter("GRADE_HR_CLASS");               //学年＋組
        KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();           //クラスのインスタンス作成
        KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(strx);

        param._grade = returnval.val1;                                          //学年
        param._staffcd = request.getParameter("SEKI");                            //記載責任者
        
//        _hyotei = StringUtils.defaultString(request.getParameter("HYOTEI"), "off");                           //評定の読み替え
        param._kanji = request.getParameter("KANJI");                           //漢字出力
        //記載日がブランクの場合桁数０で渡される事に対応
        param._date = StringUtils.isBlank(request.getParameter("DATE")) ? null : request.getParameter("DATE");  //処理日付
        param._comment = request.getParameter("COMMENT");                         //学習成績概評
        param._os = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000
        param._prgid = request.getParameter("PRGID");                          // プログラムID
//        param._kotyo = request.getParameter("KOTYO");

        final Map paramap = new HashMap();        

        paramap.put("remarkOnly", "1");  // 所見のみ

        paramap.put("OS", StringUtils.defaultString(request.getParameter("OS"), "1"));  //ＯＳ区分
        paramap.put("CTRL_YEAR", StringUtils.defaultString(request.getParameter("CTRL_YEAR"), request.getParameter("PRINT_YEAR")));  //今年度

        if (request.getParameter("DATE") != null) {
            paramap.put("CTRL_DATE", request.getParameter("DATE"));  //学籍処理日
        }
//        if (request.getParameter("MIRISYU") != null) {
//            paramap.put("MIRISYU", request.getParameter("MIRISYU"));  // 未履修科目を出力する:1 しない:2
//        }
//        if ("on".equals(request.getParameter("SONOTAJUUSYO"))) {
//            paramap.put("SONOTAJUUSYO", "on");  // その他住所を優先して表示する。
//        }
//        if ("on".equals(request.getParameter("HYOTEI"))) {
//            paramap.put("HYOTEI", "on");  // 評定の読み替え offの場合はparamapに追加しない。
//        }
        if ("on".equals(request.getParameter("FORM6"))) {
            paramap.put("FORM6", "on");  // ６年生用フォーム offの場合はparamapに追加しない。
        }

        if ("TOK".equals(_definecode.schoolmark)) {
            paramap.put("NUMBER", "");
        }

        paramap.put("PRGID", param._prgid);
//        paramap.put("OUTPUT_PRINCIPAL", param._kotyo);

//        paramap.put("RISYU", request.getParameter("RISYU"));  // 履修のみ科目出力　1:する／2:しない
//        paramap.put("TANIPRINT_SOUGOU", StringUtils.defaultString(request.getParameter("TANIPRINT_SOUGOU"), "1")); 
//        paramap.put("TANIPRINT_RYUGAKU", StringUtils.defaultString(request.getParameter("TANIPRINT_RYUGAKU"), "1")); 
        paramap.put("useSyojikou3", StringUtils.defaultString(request.getParameter("useSyojikou3"), ""));
        paramap.put("certifNoSyudou", StringUtils.defaultString(request.getParameter("certifNoSyudou"), ""));
//        paramap.put("useCertifSchPrintCnt", StringUtils.defaultString(request.getParameter("useCertifSchPrintCnt"), ""));
        paramap.put("tyousasyoAttendrecRemarkFieldSize", StringUtils.replace(StringUtils.defaultString(request.getParameter("tyousasyoAttendrecRemarkFieldSize"), ""), "+", " "));
        paramap.put("gaihyouGakkaBetu", StringUtils.defaultString(request.getParameter("gaihyouGakkaBetu"), ""));
        paramap.put("train_ref_1_2_3_field_size", StringUtils.defaultString(request.getParameter("train_ref_1_2_3_field_size"), "")); 
        paramap.put("train_ref_1_2_3_gyo_size", StringUtils.defaultString(request.getParameter("train_ref_1_2_3_gyo_size"), "")); 
        paramap.put("3_or_6_nenYoForm", StringUtils.defaultString(request.getParameter("3_or_6_nenYoForm"), "")); 
        paramap.put("tyousasyoSougouHyoukaNentani", StringUtils.defaultString(request.getParameter("tyousasyoSougouHyoukaNentani"), ""));
        paramap.put("NENYOFORM", StringUtils.defaultString(request.getParameter("NENYOFORM"), ""));
        paramap.put("tyousasyoTotalstudyactFieldSize", StringUtils.replace(StringUtils.defaultString(request.getParameter("tyousasyoTotalstudyactFieldSize"), ""), "+", " "));
        paramap.put("tyousasyoTotalstudyvalFieldSize", StringUtils.replace(StringUtils.defaultString(request.getParameter("tyousasyoTotalstudyvalFieldSize"), ""), "+", " "));
        paramap.put("tyousasyoSpecialactrecFieldSize", StringUtils.replace(StringUtils.defaultString(request.getParameter("tyousasyoSpecialactrecFieldSize"), ""), "+", " "));
        paramap.put("tyousasyoTokuBetuFieldSize", StringUtils.defaultString(request.getParameter("tyousasyoTokuBetuFieldSize"), ""));
        paramap.put("tyousasyoEMPTokuBetuFieldSize", StringUtils.defaultString(request.getParameter("tyousasyoEMPTokuBetuFieldSize"), ""));
        paramap.put("tyousasyoKinsokuForm", StringUtils.defaultString(request.getParameter("tyousasyoKinsokuForm"), ""));
        paramap.put("tyousasyoNotPrintAnotherAttendrec", StringUtils.defaultString(request.getParameter("tyousasyoNotPrintAnotherAttendrec"), ""));
        paramap.put("tyousasyoNotPrintAnotherStudyrec", StringUtils.defaultString(request.getParameter("tyousasyoNotPrintAnotherStudyrec"), ""));
        paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
        paramap.put("useClassDetailDat", request.getParameter("useClassDetailDat"));
        paramap.put("useAddrField2", request.getParameter("useAddrField2"));
        paramap.put("useProvFlg", request.getParameter("useProvFlg"));
        paramap.put("useGakkaSchoolDiv", request.getParameter("useGakkaSchoolDiv"));
        
        final List<String> containedParam = Arrays.asList("CTRL_DATE", "OUTPUT_PRINCIPAL", "category_selected", "FORM6", "NUMBER");
        for (final Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
            final String name = en.nextElement();
            if (paramap.containsKey(name) || containedParam.contains(name)) {
                continue;
            }
            paramap.put(name, request.getParameter(name));
        }
        return paramap;
    }
    
    /**進学用のPAPER作成**/
    public void ent_paper(final DB2UDB db2, final Vrw32alpWrap svf, final KNJE010 param, final String schregno, final Map paramap, final String certifKindcd) {
        final KNJE070_1 pobj = new KNJE070_1(db2, svf, _definecode, (String) paramap.get("useSyojikou3"));
        pobj.pre_stat(param._hyotei, paramap);

        paramap.put("CERTIFKIND", certifKindcd);  // 証明書種別
        paramap.put("CERTIFKIND2", certifKindcd);  // 証明書学校データ備考
        pobj.printSvf(schregno, param._year, param._gakki, param._date, param._staffcd, param._kanji, param._comment, param._os, (String) paramap.get("NUMBER"), paramap);

        if (pobj.nonedata == true) {
            nonedata = true;
        }
        pobj.pre_stat_f();
    }

    /**就職用のPAPER作成**/
    public void emp_paper(final DB2UDB db2, final Vrw32alpWrap svf, final KNJE010 param, final String schregno, final Map paramap) {
        final KNJE070_2 pobj = new KNJE070_2(db2, svf, _definecode, (String) paramap.get("useSyojikou3"));
        pobj.pre_stat(param._hyotei, paramap);

        paramap.put("CERTIFKIND", "009");  // 証明書種別
        paramap.put("CERTIFKIND2", "009");  // 証明書学校データ備考
        pobj.printSvf(schregno, param._year, param._gakki, param._date, param._staffcd, param._kanji, param._comment, param._os, (String) paramap.get("NUMBER"), paramap);

        if (pobj.nonedata == true) {
            nonedata = true;
        }
        pobj.pre_stat_f();
    }

}

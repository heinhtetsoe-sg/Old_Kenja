// kanji=漢字
/*
 * $Id: 0f632672f3d8dcc6b2c223edb213972faab32030 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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
import servletpack.KNJE.KNJE070_1;
import servletpack.KNJE.KNJE070_2;
import servletpack.KNJE.KNJE080J_1;
import servletpack.KNJE.KNJE080J_2;
import servletpack.KNJE.KNJE080_1;
import servletpack.KNJE.KNJE080_2;
import servletpack.KNJI.KNJI060_1;
import servletpack.KNJI.KNJI060_2;
import servletpack.KNJI.KNJI070_1;
import servletpack.KNJI.KNJI070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
http://tokio/serv_ktest/KNJD?DBNAME=KINJDB&PRGID=KNJG010&category_name=20051241,012,2005,3,01,20052281,,1,2006/01/17,,,&CTRL_YEAR=2005
 *
 *  学校教育システム 賢者 [事務管理] 証明書交付
 *
 *     001  卒業証明書
 *     002  卒業証明書（英）
 *     003  卒業見込証明書
 *     004  在学証明書
 *     005  在学証明書(英)
 *     006  学業成績証明書
 *     007  学業成績証明(英)
 *     008  調査書(進学)
 *     009  調査書(就職)
 *     011  単位修得証明書
 *     012  在学証明書(中学)
 *     006  学業成績証明書(中学)
 *     007  学業成績証明(英)(中学)
 *
 */

public class KNJG010 {
    private static final Log log = LogFactory.getLog(KNJG010.class);
    private final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定

    private static final String KNJG010_1_  = "KNJG010_1";
    private static final String KNJG010_2_  = "KNJG010_2";
    private static final String KNJG010_1T_ = "KNJG010_1T";
    private static final String KNJG010_2T_ = "KNJG010_2T";
    private static final String KNJE070_1_  = "KNJE070_1";
    private static final String KNJE070_2_  = "KNJE070_2";
    private static final String KNJI060_1_  = "KNJI060_1";
    private static final String KNJI060_2_  = "KNJI060_2";
    private static final String KNJE080_1_  = "KNJE080_1";
    private static final String KNJE080_2_  = "KNJE080_2";
    private static final String KNJE080J_1_ = "KNJE080J_1";
    private static final String KNJE080J_2_ = "KNJE080J_2";
    private static final String KNJI070_1_  = "KNJI070_1";
    private static final String KNJI070_2_  = "KNJI070_2";
    private static final String KNJG030_1_  = "KNJG030_1";
    private static final String KNJG030_2_  = "KNJG030_2";

    private static final int _6 = 6;         // 成績証明書
    private static final int _7 = 7;         // 成績証明書 英語
    private static final int _8 = 8;         // 調査書進学用
    private static final int _9 = 9;         // 調査書就職用
    private static final int _11 = 11;        // 単位修得証明書
    private static final int _17 = 17;      // 単位修得証明書 英語
    private static final int _18 = 18; // 単位修得証明書 見込
    private static final int _25 = 25; // 調査書進学用 卒業生用
    private static final int _26 = 26; // 調査書就職用 卒業生用
    private static final int _27 = 27; // 成績証明書 卒業生用
    private static final int _29 = 29; // 単位修得証明書 卒業生用
    private static final int _34 = 34; // 成績証明書 中学用 英語
    private static final int _36 = 36; // 成績証明書 見込
    private static final int _55 = 55; // 単位履修証明書
    private static final int _58 = 58; // 調査書諸事項6分割
    private static final int _59 = 59; // 調査書諸事項6分割(卒)

    private Set<String> _requestParameterKeys;
    private String _documentroot;
    private Map<String, String> _dbPrginfoProperties;
    private Properties _prgInfoPropertiesFilePrperties;
    private boolean _isOutputDebug;
    private boolean _hasdata = false;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 76351 $ $Date: 2020-08-31 17:52:56 +0900 (月, 31 8 2020) $"); // CVSキーワードの取り扱いに注意

        // ＤＢ接続
        final DB2UDB db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        _dbPrginfoProperties = getDbPrginfoProperties(db2);
        _isOutputDebug = "1".equals(_dbPrginfoProperties.get("outputDebug"));
        _documentroot = request.getParameter(Parameter.DOCUMENTROOT.name());
        if (!StringUtils.isEmpty(_documentroot)) {
            _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
        }
        _requestParameterKeys = new HashSet<String>();
        for (final Enumeration<String> enums = request.getParameterNames(); enums.hasMoreElements();) {
            _requestParameterKeys.add(enums.nextElement());
        }

        final String[] plist = request.getParameterValues("category_name");        //証明書情報

        // パラメータの取得
        final Map<String, String> paramap = getParam(db2, request);  //HttpServletRequestからの引数

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // 印刷処理
        _hasdata = false;
        printSvf(db2, svf, plist, paramap);

        // 終了処理
        sd.closeSvf(svf, _hasdata);
        sd.closeDb(db2);
    }


    /*
     *  get parameter doGet()パラメータ受け取り
     *
     *          plist[0] = "181010,1,,2002,3,,,,,2003-04-08,1001";
     *
     *          category_name   181010,     1学籍番号
     *                          004,        2証明書種別
     *                          2002,       3年度
     *                          3,          4学期
     *                          3,          5学年
     *                          100001,     6記載責任者
     *                          ,           7評定の読み替え
     *                          1,          8漢字出力
     *                          2003/02/20, 9処理日付／証明日付／記載日
     *                          ,           10証明書番号
     *
     */
    private Map<String, String> getParam(final DB2UDB db2, final HttpServletRequest request) {
        final Map<String, String> paramap = new TreeMap();
        putParam(paramap, request, Parameter.CTRL_YEAR.name());  //今年度
        putParamDef(paramap, request, Parameter.OS.name(), "1");  //ＯＳ区分 1:XP 2:WINDOWS2000
        putParam(paramap, request, Parameter.MIRISYU.name());  // 未履修科目を出力する:1 しない:2
        putParam(paramap, request, Parameter.FORM6.name());  // ６年生用フォーム
        putParam(paramap, request, Parameter.RISYU.name());  // 履修のみ科目出力　1:する／2:しない
        putParam(paramap, request, Parameter.DOCUMENTROOT.name());
        putParam(paramap, request, Property.useCurriculumcd.name());
        putParam(paramap, request, Property.useClassDetailDat.name());
        putParam(paramap, request, Property.useAddrField2.name());
        putParam(paramap, request, Property.useProvFlg.name());
        putParam(paramap, request, Property.useGakkaSchoolDiv.name());
        putParam(paramap, request, Property.certif_no_8keta.name());
        putParamDef(paramap, request, Parameter.TANIPRINT_SOUGOU.name(), "1");  // "総合的な学習の時間"修得単位数の計 "0"表示
        putParamDef(paramap, request, Parameter.TANIPRINT_RYUGAKU.name(), "1");  // 留学修得単位数の計 "0"表示
        putParamDef(paramap, request, Property.useSyojikou3.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.certifNoSyudou.name(), "");
        putParamDef(paramap, request, Property.Knje080UseAForm.name(), ""); // KNJE080 1:出席の記録のないフォームを使用する
        putParamDef(paramap, request, Property.gaihyouGakkaBetu.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.train_ref_1_2_3_field_size.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.train_ref_1_2_3_gyo_size.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, "3_or_6_nenYoForm", "");
        putParamDef(paramap, request, Property.tyousasyoSougouHyoukaNentani.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Parameter.NENYOFORM.name(), "");
        putParamDef(paramap, request, Property.tyousasyoTokuBetuFieldSize.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.tyousasyoEMPTokuBetuFieldSize.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.tyousasyoKinsokuForm.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.tyousasyoNotPrintAnotherStudyrec.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.seisekishoumeishoNotPrintAnotherStudyrec.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.tannishutokushoumeishoNotPrintAnotherStudyrec.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.seisekishoumeishoTaniPrintRyugaku.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.tyousasyoNotPrintEnterGrade.name(), _prgInfoPropertiesFilePrperties, "");
        putParamDef(paramap, request, Property.knjg010TestPrintInei.name(), _prgInfoPropertiesFilePrperties, null);
        putParamReplace(paramap, request, Property.tyousasyoAttendrecRemarkFieldSize);
        putParamReplace(paramap, request, Property.tyousasyoTotalstudyactFieldSize);
        putParamReplace(paramap, request, Property.tyousasyoTotalstudyvalFieldSize);
        putParamReplace(paramap, request, Property.tyousasyoSpecialactrecFieldSize);
        putParamReplace(paramap, request, Property.knjg010HakkouPrintInei);
        putParamReplace(paramap, request, Property.hyoteiYomikaeRadio);
        try {
            final List<String> containedParam = Arrays.asList("category_name");
            for (final Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
                final String name = en.nextElement();
                if (paramap.containsKey(name) || containedParam.contains(name)) {
                    continue;
                }
                paramap.put(name, request.getParameter(name));
            }

        } catch (Exception ex) {
            log.error("request.getParameter error!", ex);
        }
        return paramap;
    }

    private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
        return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJG010' "), "NAME", "VALUE");
    }

    private Properties loadPropertyFile(final String filename) {
        File file = null;
        if (null != _documentroot) {
            file = new File(new File(_documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
            if (_isOutputDebug) {
                log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
            }
            if (!file.exists()) {
                file = null;
            }
        }
        if (null == file) {
            file = new File(_documentroot + "/" + filename);
        }
        if (!file.exists()) {
            if (_isOutputDebug) {
                log.error("file not exists: " + file.getAbsolutePath());
            }
            return null;
        }
        if (_isOutputDebug) {
            log.error("file : " + file.getAbsolutePath() + ", " + file.length());
        }
        final Properties props = new Properties();
        FileReader r = null;
        try {
            r = new FileReader(file);
            props.load(r);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != r) {
                try {
                    r.close();
                } catch (Exception _ignored) {
                }
            }
        }
        return props;
    }

    /*
     *  印刷処理
     */
    private void printSvf(final DB2UDB db2, final Vrw32alpWrap svf, final String[] plist, final Map<String, String> paramap0) {
        if (_isOutputDebug) {
            for (final String key : paramap0.keySet()) {
                log.info(" paramap " + key + " = " + paramap0.get(key));
            }
        }

        final String ctrlYear = paramap0.get(Parameter.CTRL_YEAR.name());
        int beforcertif = 0;              // 証明書種別の保管
        _definecode.defineCode(db2, ctrlYear);  //各学校における定数等設定
        final boolean dispIs3Or4 = "3".equals(paramap0.get("DISP")) || "4".equals(paramap0.get("DISP"));

        final Map hobj = new HashMap();
        final String z010Name1 = getZ010Name1(paramap0, db2);
        try {
            final Integer paramIdx11certifNoSyudou = new Integer(11);
            final Integer paramIdx12sonotaJuusho = new Integer(12);
            final Integer paramIdx13zenseki = new Integer(13);
            final Integer paramIdx14nentsuki = new Integer(14);
            final Integer paramIdx15inei = new Integer(15);
            final Integer paramIdx16hankiNinteiForm = new Integer(16);
            final Integer paramIdx19certifIndex = new Integer(19);
            for (int i = 0; i < plist.length; i++) {
                final Map<String, String> paramap = new HashMap<String, String>(paramap0);
                final Map<Integer, String> plistMap = getPlistMap(plist[i]);
                if (_isOutputDebug) {
                    log.info("== plist[" + i + "] = " + plist[i]);
                    for (final Map.Entry<Integer, String> e : plistMap.entrySet()) {
                        final Integer idx = e.getKey();
                        final Object v = e.getValue();
                        log.info(" (" + (i + 1) + " / " + plist.length + ")  plist " + idx + " = " + v);
                    }
                }

                final String certifIndex = plistMap.get(paramIdx19certifIndex);
                paramap.put("CERTIF_INDEX", certifIndex);
                log.info(" CERTIF_INDEX " + certifIndex);
                final String schregno = plistMap.get(new Integer(0));
                if (null == certifIndex) {
                    final Integer paramIdx17ryugakuCredit = new Integer(17);
                    final Integer paramIdx18sogakuCredit = new Integer(18);
                    final String ryugakuCredit = plistMap.get(paramIdx17ryugakuCredit);
                    if ("1".equals(ryugakuCredit) || "2".equals(ryugakuCredit)) {
                        paramap.put(Parameter.RYUGAKU_CREDIT.name(), ryugakuCredit);
                        if (_isOutputDebug) {
                            log.info("留学の単位数を0表示? : " + "1".equals(ryugakuCredit));
                        }
                    }

                    final String sogakuCredit = plistMap.get(paramIdx18sogakuCredit);
                    if ("1".equals(sogakuCredit) || "2".equals(sogakuCredit)) {
                        paramap.put(Parameter.SOUGAKU_CREDIT.name(), sogakuCredit);
                        if (_isOutputDebug) {
                            log.info("総合的な学習の時間の単位数を0表示? : " + "1".equals(sogakuCredit));
                        }
                    }
                } else {
                    final Map<String, String> issue = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM CERTIF_ISSUE_DAT WHERE YEAR = '" + ctrlYear + "' AND CERTIF_INDEX = '" + certifIndex + "' "));
                    final String issueSchregno = KnjDbUtils.getString(issue, "SCHREGNO");
                    if (!schregno.equals(issueSchregno)) {
                        log.fatal(" not matches schregno : " + schregno + " <> " + issueSchregno);
                    } else {
                        log.info(" (" + (i + 1) + " / " + plist.length + ") certifIndex = " + certifIndex + ", schregno = " + schregno);
                        final Map<String, String> detail = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM CERTIF_DETAIL_EACHTYPE_DAT WHERE YEAR = '" + ctrlYear + "' AND CERTIF_INDEX = '" + certifIndex + "' "));
                        final String certifKindcd = KnjDbUtils.getString(issue, "CERTIF_KINDCD");
                        if (NumberUtils.isDigits(certifKindcd) && detail.containsKey("REMARK20")) { // REMARK13～REMARK20が存在する
                            if (isTyousasho(Integer.parseInt(certifKindcd)) || isSeisekiShoumeisho(Integer.parseInt(certifKindcd))) {
                                final String ryugakuCredit = KnjDbUtils.getString(detail, "REMARK18");
                                if ("1".equals(ryugakuCredit) || "2".equals(ryugakuCredit)) {
                                    paramap.put(Parameter.RYUGAKU_CREDIT.name(), ryugakuCredit);
                                    if (_isOutputDebug) {
                                        log.info("留学の単位数を0表示? : " + "1".equals(ryugakuCredit));
                                    }
                                }

                                final String sogakuCredit = KnjDbUtils.getString(detail, "REMARK19");
                                if ("1".equals(sogakuCredit) || "2".equals(sogakuCredit)) {
                                    paramap.put(Parameter.SOUGAKU_CREDIT.name(), sogakuCredit);
                                    if (_isOutputDebug) {
                                        log.info("総合的な学習の時間の単位数を0表示? : " + "1".equals(sogakuCredit));
                                    }
                                }
                            }
                        }
                        if (NumberUtils.isDigits(certifKindcd) && detail.containsKey("REMARK21")) { // REMARK21～REMARK40が存在する
                            if (isTyousasho(Integer.parseInt(certifKindcd))) {
                                // 校長印の印影表示 調査書のみ
                                final String stampPrincipal = KnjDbUtils.getString(detail, "REMARK20");
                                if (Arrays.asList("1", "2").contains(stampPrincipal)) {
                                    paramap.put(Parameter.KNJE070_CHECK_PRINT_STAMP_PRINCIPAL.name(), stampPrincipal);
                                    if (_isOutputDebug) {
                                        log.info("校長印の印影表示? : " + "1".equals(stampPrincipal));
                                    }
                                }

                                // 記載責任者の印影表示 調査書のみ
                                final String stampHrStaff = KnjDbUtils.getString(detail, "REMARK21");
                                if (Arrays.asList("1", "2").contains(stampHrStaff)) {
                                    paramap.put(Parameter.KNJE070_CHECK_PRINT_STAMP_HR_STAFF.name(), stampHrStaff);
                                    if (_isOutputDebug) {
                                        log.info("記載責任者の印影表示? : " + "1".equals(stampHrStaff));
                                    }
                                }

                                // 偶数頁に生徒名出力 調査書のみ
                                final String knje070dPrintheadername = KnjDbUtils.getString(detail, "REMARK22");
                                if (Arrays.asList("1", "2").contains(knje070dPrintheadername)) {
                                    paramap.put(Parameter.KNJE070D_PRINTHEADERNAME.name(), knje070dPrintheadername);
                                    if (_isOutputDebug) {
                                        log.info("偶数頁に生徒名出力? : " + "1".equals(knje070dPrintheadername));
                                    }
                                }

                                // 評定平均算出 調査書のみ
                                final String gvalCalcCheck = KnjDbUtils.getString(detail, "REMARK23");
                                if (Arrays.asList("1", "2").contains(gvalCalcCheck)) {
                                    paramap.put("GVAL_CALC_CHECK", gvalCalcCheck);
                                    if (_isOutputDebug) {
                                        log.info("評定平均算出 : " + ("2".equals(gvalCalcCheck) ? "加重平均" : "単純平均"));
                                    }
                                }

                                // 評定平均算出 調査書のみ
                                final String printAvgRank = KnjDbUtils.getString(detail, "REMARK24");
                                if (Arrays.asList("1", "2").contains(printAvgRank)) {
                                    paramap.put("PRINT_AVG_RANK", printAvgRank);
                                    if (_isOutputDebug) {
                                        log.info("評定平均席次出力 : " + ("1".equals(printAvgRank) ? "する" : "しない"));
                                    }
                                }
                            }

                            if (isTyousashoShingakuyou6bunkatsu(Integer.parseInt(certifKindcd))) {
                                // 調査書進学用6分割 ページ指定
                                final String tyousasyo2020shojikouExtendsSelect = KnjDbUtils.getString(detail, "REMARK25");
                                if (!StringUtils.isBlank(tyousasyo2020shojikouExtendsSelect)) {
                                    paramap.put("tyousasyo2020shojikouExtends", tyousasyo2020shojikouExtendsSelect);
                                    if (_isOutputDebug) {
                                        log.info("調査書進学用6分割ページ出力 : " + tyousasyo2020shojikouExtendsSelect);
                                    }
                                }
                            }
                        }
                    }
                }

                //指示画面より受け取ったパラメーターを分解 KNJG010_1Tで使用
                // parama
                //  0: 学籍番号
                //  1: 証明書種別
                //  2: 年度
                //  3: 学期
                //  4: 学年
                //  5: 職員コード
                //  6: 評定フラグ
                //  7: 漢字フラグ
                //  8: 日付
                //  9: 発行番号
                // 10: commentパラメータ (調査書進学用 成績段階別人数コメント）
                // 11: YEAR2パラメータ
                // 12: DOCUMENTROOTパラメータ
                // 13:
                // 14:
                // 15: KNJG010 印影パラメータ
                // 16: property useAddrField2
                // 17: 入学日付、卒業日付フォーマットパラメータ
                // 18: 印影出力パラメータ
                // 19: property certifPrintRealName
                // 20: property useShuryoShoumeisho
                // 21: property chutouKyoikuGakkouFlg
                // 22: property knjg010PrintGradeCdAsGrade
                // 23: 証明書テンプレート印刷フラグパラメータ
                // 24: CERTIF_INDEX
                final String[] parama = new String[25];
                for (int j = 0; j <= 10; j++) {
                    parama[j] = plistMap.get(new Integer(j));
                }
                final int pdiv = toInt(parama[1], -100);
                if (dispIs3Or4 && ("1".equals(paramap0.get(Property.certifNoSyudou.name())) || "1".equals(paramap0.get(Property.certif_no_8keta.name()))) || !dispIs3Or4 && "1".equals(paramap0.get(Property.certifNoSyudou.name()))) {
                    parama[Param.IDX_CERTIF_NO] = plistMap.get(paramIdx11certifNoSyudou);
                }

                if (null != parama[8] && parama[8].length() > 3) {                       //parama[8]が年度でない場合は今年度をいれておく！
                    parama[Param.IDX_YEAR2] = KNJG010_1.b_year(parama[8]);//過卒生対応年度取得->掲載日より年度を算出
                } else {
                    parama[Param.IDX_YEAR2] = ctrlYear;
                }
                if (null == parama[Param.IDX_HYOTEI]) {
                    parama[Param.IDX_HYOTEI] = "off";                           //評定1/2読替のフラグ
                }
                if (!paramap.containsKey("HYOTEI")) {
                    if ("1".equals(paramap.get("hyoteiYomikaeRadio")) && Arrays.asList("0", "1", "notPrint1").contains(parama[Param.IDX_HYOTEI])) {
                        if (isSeisekiShoumeisho(pdiv) || isTanniShutokuShoumeisho(pdiv)) {
                            if (Arrays.asList("0", "1", "notPrint1").contains(parama[Param.IDX_HYOTEI])) {
                                paramap.put("HYOTEI", parama[Param.IDX_HYOTEI]);
                            }
                        } else if ("1".equals(parama[Param.IDX_HYOTEI])) {
                            paramap.put("HYOTEI", "on");  // 評定の読み替え offの場合はparamapに追加しない。
                        }
                        if (_isOutputDebug) {
                            log.info("評定表示切替（成績証明書・単位修得証明書のみ） : " + paramap.get("HYOTEI"));
                        }
                    } else if ("1".equals(parama[Param.IDX_HYOTEI])) {
                        paramap.put("HYOTEI", "on");  // 評定の読み替え offの場合はparamapに追加しない。
                    }
                }
                if ("1".equals(plistMap.get(paramIdx12sonotaJuusho))) {
                    paramap.put("SONOTAJUUSYO", "on");
                    if (_isOutputDebug) {
                        log.info("その他住所を優先して表示する");
                    }
                }
                if ("1".equals(plistMap.get(paramIdx13zenseki))) {
                    paramap.put(Property.tyousasyoNotPrintAnotherAttendrec.name(), "on");
                    if (_isOutputDebug) {
                        log.info("前籍校を含まない");
                    }
                }
                parama[12] = paramap.get(Parameter.DOCUMENTROOT.name());
                parama[16] = paramap.get(Property.useAddrField2.name());

                if ("1".equals(plistMap.get(paramIdx14nentsuki))) {
                    parama[17] = "1";
                    if (_isOutputDebug) {
                        log.info("卒業日付は年月で表示する(卒業証明書のみ) ");
                    }
                }

                if ("1".equals(plistMap.get(paramIdx15inei)) || "osakatoin".equals(z010Name1) || "1".equals(paramap.get(Property.knjg010HakkouPrintInei.name()))) {
                    final boolean testPrintInei = "1".equals(paramap.get(Property.knjg010TestPrintInei.name()));
                    if ("1".equals(paramap.get("PARAM_TESTPRINT")) && !testPrintInei) {
                        if (_isOutputDebug) {
                            log.info("テスト出力は印影出力しない");
                        }
                    } else {
                        paramap.put("PRINT_STAMP", "1");
                        parama[18] = "1";
                        if (_isOutputDebug) {
                            log.info("印影出力する");
                        }
                    }
                }

                parama[19] = paramap.get(Property.certifPrintRealName.name());
                if ("1".equals(parama[19])) {
                    if (_isOutputDebug) {
                        log.info("戸籍名を出力する");
                    }
                }

                paramap.remove("HANKI_NINTEI");
                if ("1".equals(plistMap.get(paramIdx16hankiNinteiForm))) {
                    paramap.put("HANKI_NINTEI", "1");
                    if (_isOutputDebug) {
                        log.info("半期認定フォーム出力");
                    }
                }
                parama[20] = paramap.get("useShuryoShoumeisho");
                parama[21] = paramap.get("chutouKyoikuGakkouFlg");
                parama[22] = paramap.get("knjg010PrintGradeCdAsGrade");
                parama[23] = paramap.get(Parameter.certifSchoolOnly.name()); // 証明書学校データのみ印字フラグ KNJZ251のみ
                parama[24] = certifIndex;

                if (_isOutputDebug) {
                    for (int j = 0; j < parama.length; j++) {
                        log.info(" parama[" + j + "] = "+ parama[j]);
                    }
                }

                if (pdiv != beforcertif) {
                    if (beforcertif != 0) {
                        releaseCertif(hobj, pdiv, beforcertif);
                    }
                    beforcertif = pdiv;       // 証明書種別の保管
                }
                final Param param = new Param(parama, paramap);
                printSvfCertify(hobj, db2, svf, parama, param, paramap);
            }
            printSvfCertifyClose(hobj);                                       //各証明書の終了処理
        } catch (Exception ex) {
            log.error("error!", ex);
        }
    }


    private Map<Integer, String> getPlistMap(final String plist) {
        final Map<Integer, String> map = new TreeMap<Integer, String>();
        if (null == plist) {
            return map;
        }
        Integer key = new Integer(0);
        StringBuffer buffer = null;
        for (int i = 0; i < plist.length(); i++) {
            final char ch = plist.charAt(i);
            if (ch == ',') {
                if (null != buffer) {
                    map.put(key, buffer.toString());
                    buffer = null;
                }
                key = new Integer(key.intValue() + 1);
            } else {
                String before = map.get(key);
                if (null == before) {
                    before = "";
                }
                map.put(key, before + ch);
            }
        }
//        for (int i = 0; i <= key.intValue(); i++) {
//            final Integer k = new Integer(i);
//            if (!map.containsKey(k)) {
//                map.put(k, "");
//            }
//        }
        return map;
    }


    /*
     *  各証明書の印刷処理
     */
    private void printSvfCertify(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final String[] parama, final Param param, final Map<String, String> paramap) {
        try {
            param._grddiv = KnjDbUtils.query(db2, " SELECT SCHREGNO FROM GRD_BASE_MST WHERE SCHREGNO = '" + param._schregno + "' ").size() > 0;  //卒・在判定
            if (_isOutputDebug) {
                log.info(" grddiv = " + param._grddiv);
            }
        } catch (Exception ex) {
            log.error("error!", ex);
        }

        //学籍処理日をMapへセット。現時点で処理日を使用するので暫定的ココに置く。05/12/08
        if (!paramap.containsKey("CTRL_DATE")) {
            paramap.put("CTRL_DATE", param._date);  //学籍処理日
        }

        if (_definecode.schoolmark.equals("TOK")) {
//            parama[PARAM_CERTIF_NO] = getCertificateNum(parama);  // 証明書番号セット parama[PARAM_CERTIF_NO]を上書き
        }
        paramap.put("CERTIFKIND", param._certifKindCd);  // 証明書種別
        paramap.put("CERTIFKIND2", param._certifKindCd);


        try {
            final int pdiv = Integer.parseInt(param._certifKindCd);                                      //証明書種別

            if (contains(pdiv, new int[] {33})) {
                pknje080J_1(hobj, db2, svf, param, paramap);   //中学成績証明書(和)
            } else if (isSeisekiShoumeisho(pdiv)) {
                pknje080(hobj, db2, svf, param, paramap);   //成績証明書
            } else if (contains(pdiv, new int[] {_34})) {
                pknje080J_2(hobj, db2, svf, param, paramap);   //中学成績証明書(英)
            } else if (contains(pdiv, new int[] {_7, 37})) {
                pknje080_2(hobj, db2, svf, param, paramap);   //成績証明書
            } else if (isTyousasho(pdiv)) {
                pknje070(hobj, db2, svf, param, paramap);   //調査書
            } else if (isTanniShutokuShoumeisho(pdiv)) {
                pknjg030(hobj, db2, svf, param, paramap);   //単位修得証明書
            } else { // (contains(pdiv, new int[] {1, 2, 3, 4, 5, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 31, 32, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, etc...})) {  // 卒業証明書等
                pknjg010(hobj, db2, svf, param, parama, paramap);   // 卒業証明書等 東京都用
            }
        } catch (Exception ex) {
            log.error("error!", ex);
        }
    }

    private static int toInt(final String s, final int def) {
        return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
    }

    private static boolean isTyousasho(final int pdiv) {
        return contains(pdiv, new int[] {_8, _9, _25, _26, _58, _59});
    }

    private static boolean isTyousashoShingakuyou6bunkatsu(final int pdiv) {
        return contains(pdiv, new int[] { _58, _59});
    }

    private static boolean isSeisekiShoumeisho(final int pdiv) {
        return contains(pdiv, new int[] {_6, _27, _36});
    }

    private static boolean isTanniShutokuShoumeisho(final int pdiv) {
        return contains(pdiv, new int[] {_11, _18, 29, 17, 35, _55});
    }

    private static boolean contains(final int n, final int[] ns) {
        return ArrayUtils.contains(ns, n);
    }

    private void releaseCertifKNJE070_1(final Map hobj, final String key) {
        if (hobj.containsKey(key)) {
            ((KNJE070_1) hobj.get(key)).pre_stat_f(); //調査書(進学)
            hobj.remove(key);
        }
    }

    private void releaseCertifKNJE080_1(final Map hobj, final String key) {
        if (hobj.containsKey(key)) {
            ((KNJE080_1) hobj.get(key)).pre_stat_f(); //成績証明書
            hobj.remove(key);
        }
    }

    private void releaseCertifKNJE080_2(final Map hobj, final String key) {
        if (hobj.containsKey(key)) {
            ((KNJE080_2) hobj.get(key)).pre_stat_f(); //成績証明書
            hobj.remove(key);
        }
    }

    /*
     *  各証明書の解放 => SQL0954Cのエラー回避のため
     */
    private void releaseCertif(final Map hobj, int certif, int beforcertif) {
        try {
            //成績証明書和文と英文は共存しないようにする
            if (certif == _34) {
                if (hobj.containsKey(KNJE080J_2_)) {
                    ((KNJE080J_2) hobj.get(KNJE080J_2_)).pre_stat_f(); //成績証明書(英語)
                    hobj.remove(KNJE080J_2_);
                }
            } else if (certif == _7) {
                if (hobj.containsKey(KNJI070_2_)) { releaseCertifKNJE080_2(hobj, KNJI070_2_); } //成績証明書(英語)
                if (hobj.containsKey(KNJE080_2_)) { releaseCertifKNJE080_2(hobj, KNJE080_2_); } //成績証明書(英語)
            } else if (certif == 33) {
                if (hobj.containsKey(KNJE080J_1_)) {
                    ((KNJE080J_1) hobj.get(KNJE080J_1_)).pre_stat_f(); //成績証明書(英語)
                    hobj.remove(KNJE080J_1_);
                }
            } else if (certif == _6 || certif == _27) {
                if (hobj.containsKey(KNJI070_1_)) { releaseCertifKNJE080_1(hobj, KNJI070_1_); } //成績証明書
                if (hobj.containsKey(KNJE080_1_)) { releaseCertifKNJE080_1(hobj, KNJE080_1_); } //成績証明書
            //調査書の進学用と就職用は共存しないようにする
            } else if (certif == _9 || certif == _26) {
                if (hobj.containsKey(KNJI060_2_)) { releaseCertifKNJE070_1(hobj, KNJI060_2_); } //調査書(就職)
                if (hobj.containsKey(KNJE070_2_)) { releaseCertifKNJE070_1(hobj, KNJE070_2_); } //調査書(就職)
            } else if (certif == _8 || certif == _25) {
                if (hobj.containsKey(KNJI060_1_)) { releaseCertifKNJE070_1(hobj, KNJI060_1_); } //調査書(進学)
                if (hobj.containsKey(KNJE070_1_)) { releaseCertifKNJE070_1(hobj, KNJE070_1_); } //調査書(進学)
            }
        } catch (Exception ex) {
            log.error("error!", ex);
        }
    }


    /*
     *  各証明書の印刷終了処理
     */
    private void printSvfCertifyClose(Map hobj) {
        try {
            if (hobj.containsKey(KNJG010_1_)) ((KNJG010_1) hobj.get(KNJG010_1_)).pre_stat_f(); //卒業証明書等
            if (hobj.containsKey(KNJG010_2_)) ((KNJG010_1) hobj.get(KNJG010_2_)).pre_stat_f(); //卒業証明書等
            if (hobj.containsKey(KNJG010_1T_)) ((KNJG010_1T) hobj.get(KNJG010_1T_)).pre_stat_f(); //卒業証明書等
            if (hobj.containsKey(KNJG010_2T_)) ((KNJG010_1T) hobj.get(KNJG010_2T_)).pre_stat_f(); //卒業証明書等
            if (hobj.containsKey(KNJI070_1_)) ((KNJE080_1) hobj.get(KNJI070_1_)).pre_stat_f(); //成績証明書
            if (hobj.containsKey(KNJE080_1_)) ((KNJE080_1) hobj.get(KNJE080_1_)).pre_stat_f(); //成績証明書
            if (hobj.containsKey(KNJI070_2_)) ((KNJE080_2) hobj.get(KNJI070_2_)).pre_stat_f(); //成績証明書(英語)
            if (hobj.containsKey(KNJE080_2_)) ((KNJE080_2) hobj.get(KNJE080_2_)).pre_stat_f(); //成績証明書(英語)
            if (hobj.containsKey(KNJI060_1_)) ((KNJE070_1) hobj.get(KNJI060_1_)).pre_stat_f(); //調査書(進学)
            if (hobj.containsKey(KNJE070_1_)) ((KNJE070_1) hobj.get(KNJE070_1_)).pre_stat_f(); //調査書(進学)
            if (hobj.containsKey(KNJI060_2_)) ((KNJE070_1) hobj.get(KNJI060_2_)).pre_stat_f(); //調査書(就職)
            if (hobj.containsKey(KNJE070_2_)) ((KNJE070_1) hobj.get(KNJE070_2_)).pre_stat_f(); //調査書(就職)
            if (hobj.containsKey(KNJG030_2_)) ((KNJG030_1) hobj.get(KNJG030_2_)).pre_stat_f(); //単位修得証明書
            if (hobj.containsKey(KNJG030_1_)) ((KNJG030_1) hobj.get(KNJG030_1_)).pre_stat_f(); //単位修得証明書
            if (hobj.containsKey(KNJE080J_1_)) ((KNJE080J_1) hobj.get(KNJE080J_1_)).pre_stat_f(); //中学成績証明書
            if (hobj.containsKey(KNJE080J_2_)) ((KNJE080J_2) hobj.get(KNJE080J_2_)).pre_stat_f(); //中学成績証明書
        } catch (Exception ex) {
            log.error("error!", ex);
        }
    }

    /*
     *      卒業証明書（和）        証明書種別:001
     *      卒業証明書（英）        証明書種別:002
     *      卒業見込証明書（和）    証明書種別:003
     *      卒業見込証明書（英）    証明書種別:012
     *      在学証明書（和）        証明書種別:004
     *      在学証明書（英）        証明書種別:005
     *      在籍証明書（和）        証明書種別:013
     *      在籍証明書（英）        証明書種別:014
     *      修了証明書（和）        証明書種別:015
     *      修了証明書（英）        証明書種別:016
     */
    private void pknjg010 (
            final Map hobj,
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final Param param,
            final String[] parama,
            final Map paramap
    ) {
        if (!_definecode.schoolmark.equals("KIN") && !_definecode.schoolmark.equals("KINJUNIOR")) {
            final String key;
            if (!param._grddiv) {
                key = KNJG010_1T_;
            } else {
                key = KNJG010_2T_;
            }
            final KNJG010_1T pobj = (KNJG010_1T) getPrintObj(hobj, key, db2, svf, null, paramap);

            boolean nonedata = pobj.printSvfMain(parama, (String) paramap.get(Parameter.CTRL_YEAR.name()));
            if (nonedata) {
                _hasdata = true;
            }
        } else {
            final String key;
            if (!param._grddiv) {
                key = KNJG010_1_;
            } else {
                key = KNJG010_2_;
            }

            final KNJG010_1 pobj = (KNJG010_1) getPrintObj(hobj, key, db2, svf, param, paramap);
            pobj.printSvfMain(Integer.parseInt(param._certifKindCd), param._schregno, param._year, param._year2, param._semester, param._date, param._certifNumber, paramap);
            if (pobj.nonedata == true) {
                _hasdata = true;
            }
        }
    }

    private final Object getPrintObj(final Map hobj, final String key, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map paramap) {
        if (!hobj.containsKey(key)) {

            final Object o;
            if (KNJG010_1_.equals(key)) {
                final KNJG010_1 value = new KNJG010_1(db2, svf, _definecode);
                value.pre_stat(null);
                o = value;
            } else if (KNJG010_2_.equals(key)) {
                final KNJG010_2 value = new KNJG010_2(db2, svf, _definecode);
                value.pre_stat(null);
                o = value;
            } else if (KNJG010_1T_.equals(key)) {
                final KNJG010_1T value = new KNJG010_1T(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJG010_2T_.equals(key)) {
                final KNJG010_2T value = new KNJG010_2T(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJE080_1_.equals(key)) {
                final KNJE080_1 value = new KNJE080_1(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJI070_1_.equals(key)) {
                final KNJI070_1 value = new KNJI070_1(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJE080_2_.equals(key)) {
                final KNJE080_2 value = new KNJE080_2(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJI070_2_.equals(key)) {
                final KNJI070_2 value = new KNJI070_2(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJE070_1_.equals(key)) {
                final KNJE070_1 value = new KNJE070_1(db2, svf, _definecode, (String) paramap.get(Property.useSyojikou3.name()));
                value.pre_stat(param._hyotei, paramap);
                o = value;
            } else if (KNJI060_1_.equals(key)) {
                final KNJI060_1 value = new KNJI060_1(db2, svf, _definecode, (String) paramap.get(Property.useSyojikou3.name()));
                value.pre_stat(param._hyotei, paramap);
                o = value;
            } else if (KNJE070_2_.equals(key)) {
                final KNJE070_2 value = new KNJE070_2(db2, svf, _definecode, (String) paramap.get(Property.useSyojikou3.name()));
                value.pre_stat(param._hyotei, paramap);
                o = value;
            } else if (KNJI060_2_.equals(key)) {
                final KNJI060_2 value = new KNJI060_2(db2, svf, _definecode, (String) paramap.get(Property.useSyojikou3.name()));
                value.pre_stat(param._hyotei, paramap);
                o = value;
            } else if (KNJG030_1_.equals(key)) {
                final KNJG030_1 value = new KNJG030_1(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJG030_2_.equals(key)) {
                final KNJG030_2 value = new KNJG030_2(db2, svf, _definecode);
                value.pre_stat(null, paramap);
                o = value;
            } else if (KNJE080J_1_.equals(key)) {
                final KNJE080J_1 value = new KNJE080J_1(db2, svf, paramap);
                value.pre_stat(null);
                o = value;
            } else if (KNJE080J_2_.equals(key)) {
                final KNJE080J_2 value = new KNJE080J_2(db2, svf, paramap);
                value.pre_stat(null);
                o = value;
            } else {
                o = null;
            }
            hobj.put(key, o);
        }
        return hobj.get(key);
    }

    /*
     *  学業成績証明書
     */
    private void pknje080(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map paramap) {
        final int pdiv = Integer.parseInt(param._certifKindCd);
        final String key;
        //成績証明書
        if (!param._grddiv) {
            key = KNJE080_1_;     //在校生用オブジェクト作成
        } else {
            key = KNJI070_1_;     //卒業生用オブジェクト作成
        }

        final KNJE080_1 pobj = (KNJE080_1) getPrintObj(hobj, key, db2, svf, param, paramap);
        pobj.printSvf(param._year, param._semester, param._date, param._schregno, paramap, param._staffCd, pdiv, param._kanji, param._certifNumber);
        if (pobj.nonedata == true) {
            _hasdata = true;
        }
    }

    /*
     *  学業成績証明書
     */
    private void pknje080_2(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map paramap) {
        final int pdiv = Integer.parseInt(param._certifKindCd);
        final String key;
        //成績証明書(英語)
        if (!param._grddiv) {
            key = KNJE080_2_;     //在校生用オブジェクト作成
        } else{
            key = KNJI070_2_;
        }

        final KNJE080_2 pobj = (KNJE080_2) getPrintObj(hobj, key, db2, svf, param, paramap);
        pobj.printSvf(param._year, param._semester, param._date, param._schregno, paramap, param._staffCd, pdiv, param._kanji, param._certifNumber);
        if (pobj.nonedata == true) {
            _hasdata = true;
        }
    }

    /*
     *  調査書
     */
    private void pknje070(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map paramap) {
        final String key;
        final int pdiv = Integer.parseInt(param._certifKindCd);
        if (ArrayUtils.contains(new int[] {_8, _25, _58, _59}, pdiv)) {
            //調査書(進学用)
            if (!param._grddiv) {
                //在校生用オブジェクト作成
                key = KNJE070_1_;
            } else{
                //卒業生用オブジェクト作成
                key = KNJI060_1_;
            }
            final KNJE070_1 pobj = (KNJE070_1) getPrintObj(hobj, key, db2, svf, param, paramap);
            if (ArrayUtils.contains(new int[] {_58, _59}, pdiv)) {
                paramap.put(Parameter.tyousasyo2020.name(), "1");
                paramap.remove(Parameter.FORM6.name());
            }
            pobj.printSvf(param._schregno, param._year, param._semester, param._date, param._staffCd, param._kanji, param._comment, (String) paramap.get(Parameter.OS.name()), param._certifNumber, paramap);
            if (pobj.nonedata == true) {
                _hasdata = true;
            }
        } else {
            //調査書(就職用)
            if (!param._grddiv) {
                //在校生用オブジェクト作成
                key = KNJE070_2_;
            } else {
                //卒業生用オブジェクト作成
                key = KNJI060_2_;
            }
            final KNJE070_2 pobj = (KNJE070_2) getPrintObj(hobj, key, db2, svf, param, paramap);
            pobj.printSvf(param._schregno, param._year, param._semester, param._date, param._staffCd, param._kanji, param._comment, (String) paramap.get(Parameter.OS.name()), param._certifNumber, paramap);
            if (pobj.nonedata == true) {
                _hasdata = true;
            }
        }
    }


    /*
     *  単位修得証明書
     */
    private void pknjg030(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map paramap) {
        final String key;
        if (!param._grddiv) {
            //在校生用オブジェクト作成
            key = KNJG030_1_;
        } else{
            //卒業生用オブジェクト作成
            key = KNJG030_2_;
        }

        final KNJG030_1 pobj = (KNJG030_1) getPrintObj(hobj, key, db2, svf, param, paramap);
        if ("miyagiken".equals(getZ010Name1(paramap, db2)) || "1".equals(paramap.get("tannishutokushoumeishoKisaisekininsha"))) {
            paramap.put("SEKI", param._staffCd);
        }
        pobj.printSvf(param._year, param._semester, param._schregno, param._date, param._certifKindCd, param._certifNumber, paramap);
        if (pobj.nonedata == true) {
            _hasdata = true;
        }
    }


    /*
     *  中学成績証明書(和)
     *
     */
    private void pknje080J_1(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map<String, String> paramap) {
        final KNJE080J_1 pobj = (KNJE080J_1) getPrintObj(hobj, KNJE080J_1_, db2, svf, param, paramap);

        pobj.printSvf(param._year, param._semester, param._date, param._schregno, paramap, param._staffCd, -1, param._kanji, param._certifNumber);
        if (pobj.nonedata == true) {
            _hasdata = true;
        }
    }


    /*
     *  中学成績証明書(英)
     *
     */
    private void pknje080J_2(final Map hobj, final DB2UDB db2, final Vrw32alpWrap svf, final Param param, final Map<String, String> paramap) {
        final KNJE080J_2 pobj = (KNJE080J_2) getPrintObj(hobj, KNJE080J_2_, db2, svf, param, paramap);
        pobj.printSvf(param._year, param._semester, param._date, param._schregno, paramap, param._staffCd, -1, param._kanji, param._certifNumber);
        if (pobj.nonedata == true) {
            _hasdata = true;
        }
    }

    // パラメータで' 'が'+'に変換されるのでもとに戻す
    private void putParamReplace(final Map<String, String> paramap, final HttpServletRequest req, final Property property) {
        paramap.put(property.name(), StringUtils.replace(StringUtils.defaultString(req.getParameter(property.name()), StringUtils.defaultString(getProperty(_prgInfoPropertiesFilePrperties, property.name()), "")), "+", " "));
    }

    // パラメータがnullの場合デフォルト値に置換
    private void putParamDef(final Map<String, String> paramap, final HttpServletRequest req, final String name, final String defVal) {
        paramap.put(name, StringUtils.defaultString(req.getParameter(name), defVal));
    }

    // パラメータがnullの場合デフォルト値に置換
    private void putParamDef(final Map<String, String> paramap, final HttpServletRequest req, final String name, final Properties props, final String defVal) {
        String val = null;
        if (_requestParameterKeys.contains(name)) {
            val = req.getParameter(name);
            if (_isOutputDebug) {
                log.info(" set parameter: " + name + " = " + val);
            }
        } else {
            if (_dbPrginfoProperties.containsKey(name)) {
                val = _dbPrginfoProperties.get(name);
                if (_isOutputDebug) {
                    log.info(" set parameter @PRGINFO_PROPERTIES: " + name + " = " + val);
                }
            } else if (props.containsKey(name)) {
                val = getProperty(props, name);
                if (_isOutputDebug) {
                    log.info(" set parameter @prgInfo.properties: " + name + " = " + val);
                }
            } else {
                val = defVal;
                if (_isOutputDebug) {
                    log.info(" set parameter default: " + name + " = " + val);
                }
            }
        }
        paramap.put(name, val);
    }

    private void putParam(final Map paramap, final HttpServletRequest req, final String name) {
        paramap.put(name, req.getParameter(name));
    }

    private String getProperty(final Properties props, final String name) {
        String value = null;
        if (props.containsKey(name)) {
            final String propValue = props.getProperty(name);
            if (_isOutputDebug) {
                log.info("# prgInfo.property property [" + name + "] : [" + propValue + "]");
            }
            value = propValue;
        }
        return value;
    }

    protected static enum Property {
        certifNoSyudou,
        certif_no_8keta,
        useSyojikou3,
        gaihyouGakkaBetu,
        tyousasyoAttendrecRemarkFieldSize, // 5.出欠備考のフィールドサイズ
        tyousasyoTokuBetuFieldSize, // 6.特別活動のフィールドサイズ
        tyousasyoEMPTokuBetuFieldSize, // 6.特別活動のフィールドサイズ（就職用）
        tyousasyoSpecialactrecFieldSize, // 6.特別活動の記録のフィールドサイズ
        tyousasyoKinsokuForm, // SVF禁則フォーム
        train_ref_1_2_3_field_size, // 7.指導上参考となる諸事項の桁数変更フラグ
        train_ref_1_2_3_gyo_size, // 7.指導上参考となる諸事項の行数変更フラグ
        tyousasyoTotalstudyactFieldSize, // 8.総合的な学習の時間の内容フィールドサイズ
        tyousasyoTotalstudyvalFieldSize, // 8.総合的な学習の時間の評価フィールドサイズ
        tyousasyoSougouHyoukaNentani, // 8.総合的な学習の時間の内容・評価の年毎/通年フラグ
        tyousasyoNotPrintAnotherAttendrec, // 前籍校の出席（SCHOOLCD='1'のSCHREG_ATTENDREC_DAT）を含まない
        tyousasyoNotPrintAnotherStudyrec, // 前籍校の成績（SCHOOLCD='1'のSCHREG_STUDYREC_DAT）を表示しない
        seisekishoumeishoNotPrintAnotherStudyrec,
        tannishutokushoumeishoNotPrintAnotherStudyrec,
        tyousasyoNotPrintEnterGrade, // 入学の学年・年次を表示しない
        tyousasyoPrintCoursecodename,
        seisekishoumeishoTaniPrintRyugaku,
        certifPrintRealName, // 設定によらず戸籍名を印字する
        useClassDetailDat,
        useAddrField2,
        useProvFlg,
        useGakkaSchoolDiv,
        hyoteiYomikaeRadio,
        knjg010HakkouPrintInei,
        knjg010TestPrintInei,
        Knje080UseAForm,
        useCurriculumcd,
        ;

        final String _name;
        Property() {
            this(null);
        }
        Property(final String name) {
            _name = null == name ? name() : name;
        }
    }

    protected static enum Parameter {
        PRGID,
        CTRL_YEAR,
        DOCUMENTROOT,
        OS,
        FORM6,
        NENYOFORM,
        RISYU,
        MIRISYU,
        TANIPRINT_SOUGOU,
        TANIPRINT_RYUGAKU,
        KNJE070_CHECK_PRINT_STAMP_HR_STAFF,
        KNJE070_CHECK_PRINT_STAMP_PRINCIPAL,
        RYUGAKU_CREDIT,
        SOUGAKU_CREDIT,
        tyousasyo2020,
        KNJE070D_PRINTHEADERNAME,
        certifSchoolOnly,
        ;
        final String _name;
        Parameter() {
            this(null);
        }
        Parameter(final String name) {
            _name = null == name ? name() : name;
        }
    }

    private static class Param {
        private static int IDX_HYOTEI = 6;
        private static int IDX_CERTIF_NO = 9;
        private static int IDX_YEAR2 = 11;

        final String _schregno;
        final String _certifKindCd;
        final String _year;
        final String _semester;
        final String _grade;
        final String _staffCd;
        final String _hyotei;
        final String _kanji;
        final String _date;
        final String _certifNumber;
        final String _comment;
        final String _year2;
        boolean _grddiv;     //卒・在判定

        public Param(final String[] param, final Map<String, String> paramap) {
            _schregno = param[0];
            _certifKindCd = param[1];
            _year = param[2];
            _semester = param[3];
            _grade = param[4];
            _staffCd = param[5];
            _hyotei = param[IDX_HYOTEI];
            _kanji = param[7];
            _date = param[8];
            _certifNumber = param[IDX_CERTIF_NO];
            _comment = param[10];
            _year2 = param[IDX_YEAR2];
        }

        public String toString() {
            return "KNJG010.Param(schregno = " + _schregno + ", certifKindCd = " + _certifKindCd + ", year = "  + _year + ", semester = " + _semester + ", grade = " + _grade + ", staffCd = " + _staffCd + ", date = " + _date + ", certifNumber = " + _certifNumber + ")";
        }
    }

    public static Map execMethod(final DB2UDB db2, final Map paramMap) {
        Map m = null;
        try {
            if ("1".equals(paramMap.get("outputDebug"))) {
                log.info(" execMethod with " + paramMap + "");
            }
            final String methodName = (String) paramMap.get("methodName");
            if ("getCertifKindMst".equals(methodName)) {
                m = getCertifKindMst(db2, paramMap);
            } else if ("outputFuhakkou".equals(methodName)) {
                m = outputFuhakkou(db2, paramMap);
            } else {
                log.warn("no such method:" + methodName);
            }
        } catch (final Exception e) {
            log.fatal("exception!", e);
        }
        if (null == m) {
            m = new HashMap();
        }
        return m;
    }

    private static Map getCertifKindMst(final DB2UDB db2, final Map paramMap) {
        final String sql = " SELECt * FROM CERTIF_KIND_MST "
                          + " WHERE CERTIF_KINDCD = '" + (String) paramMap.get("CERTIF_KINDCD") + "' ";
        return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
    }

    private static String getJosekiDate(final DB2UDB db2, final String schregno, final String schoolKind) {
        final String sql = " SELECT GRD_DATE FROM SCHREG_ENT_GRD_HIST_DAT "
                         + "  WHERE SCHREGNO = '" + schregno + "' AND SCHOOL_KIND = '" + schoolKind + "' AND GRD_DIV <> '4' ";
        return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql)), "GRD_DATE");
    }

    private static String getZ010Name1(final Map paramMap, final DB2UDB db2) {
        if (null != paramMap && null != paramMap.get("Z010.NAME1")) {
            return KnjDbUtils.getString(paramMap, "Z010.NAME1");
        }
        final String sql = " SELECt NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql)), "NAME1");
    }

    /**
     * 証明書不発行か
     * @param grdDate 生徒の卒業日付
     * @param elapsedYears 発行を許可する卒業経過年数 (CERTIF_KIND_MST.ELAPSED_YEARS)
     * @return 不発行(卒業日付に経過年数を加算した日付をシステム日付が超える)ならtrue、それ以外はfalse
     */
    private static boolean isFuhakkou(final String grdDate, final String elapsedYears) {
        if (null == grdDate || !NumberUtils.isDigits(elapsedYears)) {
            //log.debug(" grdDate = " + grdDate + ", elapsedYears = " + elapsedYears);
            return false;
        }

        final Calendar hakkoulimit = Calendar.getInstance();
        hakkoulimit.setTime(Date.valueOf(grdDate));
        hakkoulimit.add(Calendar.YEAR, Integer.parseInt(elapsedYears));

        final Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        final boolean isFuhakkou = hakkoulimit.equals(now) || hakkoulimit.before(now);
        if (isFuhakkou) {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            log.info(" hakkouLimit = " + df.format(hakkoulimit.getTime()) + ", now = " + df.format(now.getTime()) + ", isFuhakkou = " + isFuhakkou);
        }
        return isFuhakkou;
    }

    private static Map outputFuhakkou(final DB2UDB db2, final Map paramMap) {
        final Map rtn = new HashMap();
        rtn.put("IS_FUHAKKOU", "false");
        rtn.put("OUTPUT_SVF", "false");

        final String schregno = (String) paramMap.get("SCHREGNO");
        final String certifKindcd = (String) paramMap.get("CERTIF_KINDCD");
        if (null == schregno || null == certifKindcd) {
            return rtn;
        }

        final String grdDate = getJosekiDate(db2, schregno, StringUtils.defaultString((String) paramMap.get("SCHOOL_KIND"), "H"));
        rtn.put("GRD_DATE", grdDate);
        final Map certifKindMst = getCertifKindMst(db2, paramMap);
        if (!certifKindMst.isEmpty() && !certifKindMst.containsKey("ELAPSED_YEARS")) {
            // テーブルにフィールドがない
            rtn.put("INFO", "フィールド無し:" + "CERTIF_KIND_MST.ELAPSED_YEARS");
            return rtn;
        }
        final String elapsedYears = KnjDbUtils.getString(certifKindMst, "ELAPSED_YEARS");
        rtn.put("CERTIF_KIND_MST.ELAPSED_YEARS", elapsedYears);

        if (!isFuhakkou(grdDate, elapsedYears)) {
            return rtn;
        }

        rtn.put("IS_FUHAKKOU", "true");
        final boolean isNishiyama = "nishiyama".equals(getZ010Name1(paramMap, db2));
        if (!isNishiyama) {
            return rtn;
        }

        final Vrw32alp svf = (Vrw32alp) paramMap.get("Vrw32alp");
        if (null == svf) {
            return rtn;
        }
        printSvfFuhakkou(svf, db2, schregno, paramMap);
        rtn.put("OUTPUT_SVF", "true");
        return rtn;
    }


    private static void printSvfFuhakkou(final Vrw32alp svf, final DB2UDB db2, final String schregno, final Map paramMap) {
        final Map schregBaseMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregno + "' "));
        final String certifKindcdFuhakkou = "040";
        final String sqlCertifSchoolDat = " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + paramMap.get("YEAR") + "' AND CERTIF_KINDCD = '" + certifKindcdFuhakkou + "' ";
        final Map certifSchoolDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sqlCertifSchoolDat));
        final String name = KnjDbUtils.getString(schregBaseMst, "NAME");
        svf.VrSetForm("KNJG010_12.frm", 1);

        final String kindName = getFuhakkouKindName(paramMap);
        svf.VrsOut("TITLE", StringUtils.defaultString(kindName) + "に関する証明書");

        svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(name) > 24 ? "2" : "1"), name);
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(schregBaseMst, "BIRTHDAY")));

        final StringBuffer stb = new StringBuffer();
        stb.append("　上記の者の" + kindName + "については、学校教育法施行規則第２８条");
        stb.append("第２項により、指導要録（指導の記録）の保存期間を過ぎているため、作成で");
        stb.append("きないことを証明します。");

        final String[] lines = KNJ_EditEdit.get_token(stb.toString(), 70, 3);
        for (int i = 0; i < lines.length; i++) {
            svf.VrsOut("MAIN" + String.valueOf(i + 1), lines[i]);
        }
        svf.VrsOut("MAIN4", "※氏名は、卒業時のままで記載しています。");

        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, (String) paramMap.get("DATE")));
        svf.VrsOut("SCHOOLNAME", KnjDbUtils.getString(certifSchoolDat, "SCHOOL_NAME"));
        svf.VrsOut("STAFFNAME", KnjDbUtils.getString(certifSchoolDat, "PRINCIPAL_NAME"));
        svf.VrsOut("JOBNAME", KnjDbUtils.getString(certifSchoolDat, "JOB_NAME"));

        svf.VrEndPage();
    }


    private static String getFuhakkouKindName(final Map paramMap) {
        String rtn = "";
        final int certifKindcd = Integer.parseInt((String) paramMap.get("CERTIF_KINDCD"));
        if (contains(certifKindcd, new int[] {_8, _9, _25, _26})) {
            rtn = "調査書";
        } else if (contains(certifKindcd, new int[] {_6, _7, _27, _36, 37})) {
            rtn = "成績証明書";
        } else if (contains(certifKindcd, new int[] {33, _34})) {
            // 成績証明書中学
            rtn = "成績証明書";
        } else if (contains(certifKindcd, new int[] {_11, _17, _29})) {
            rtn = "単位修得証明書";
        }
        return StringUtils.defaultString((String) paramMap.get("fuhakkouKindName"), rtn);
    }
}
// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJA.detail.KNJ_TransferRecSql;
import servletpack.KNJG.KNJG010;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Field;

/*
 *  学校教育システム 賢者 [進路情報管理]  学業成績証明書（日本語）
 *
 *  2004/04/09 yamashiro・忌引日数が出力されない不具合を修正
 *  2004/11/05 yamashiro・記載責任者表記の不具合を修正
 *  2004/11/15 yamashiro・科目名を文字数(=>byte)により大きさを変えて出力する
 *  2004/11/19 yamashiro・学習の欄において教科間の空白行を除外する
 *  2005/07/19 yamashiro・学習の記録欄において右側に教科のみ出力される不具合を修正
 *                        nullデータの処理を修正
 *  2005/08/04 yamashiro
 *  2005/08/26 yamashiro 近大付属版の校長名出力仕様変更
 *  2005/09/07 yamashiro 2005/08/26の変更を無効とする
 *  2005/09/13 yamashiro 学習の欄において最後に出力する教科の空白行を除外する 04/11/19における積み残し
 *  2005/11/18 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度
 *                        学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJE080_1 {

    private static final Log log = LogFactory.getLog(KNJE080_1.class);

    private static final String CERTIF_KINDCD_MIKOMI = "036";
    private static final String CERTIF_KINDCD = "006";
    private static final int CERTIF006 = 6;
    private static final int CERTIF027 = 27;
    private static final int CERTIF033 = 33;
    private static final int CERTIF036 = 36;

    private static final int CERTIF011 = 11; // 単位修得証明書

    private static final String CERTIF_KINDCD_ENG = "007";
    private static final String CERTIF_KINDCD_ENG_MIKOMI = "037";

    private static final String d_MMMM_yyyy = "d MMMM yyyy";
    private static final String MMMMM_d_comma_yyyy = "MMMMM d,yyyy";
    private static final String MMM_d_comma_yyyy = "MMM d,yyyy";

    private final Vrw32alp _svf;   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private final DB2UDB _db2;                      //Databaseクラスを継承したクラス
    public boolean nonedata;

    protected final Param _param;
    private boolean _isPrintGrd;
    private Form _form;

    public KNJE080_1(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode) {
        _db2 = db2;
        _svf = svf;
        nonedata = false;
        _param = new Param(db2);
        _form = new Form(_param, _svf);
        log.fatal("$Id$"); // CVSキーワードの取り扱いに注意
    }

    /*
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        pre_stat(hyotei, new HashMap());
    }

    /*
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map paramMap) {
        if ("1".equals(paramMap.get("PRINT_GRD"))) {
            _isPrintGrd = true;
            log.info("PRINT_GRD");
        }
    }

    public void pre_stat_f() {
        if (null != _param) {
            _param.close();
        }
    }

    private void outputFuhakkou(final Vrw32alp svf, final PrintData printData) {
        printData._outputFuhakkouResult = new HashMap();
        try {
            final Map paramMap = new HashMap(printData._paramap);
            paramMap.put("YEAR", printData._paramap.get("CTRL_YEAR"));
            paramMap.put("SCHREGNO", printData._schregno);
            paramMap.put("CERTIF_KINDCD", printData._certifKind);
            paramMap.put("Vrw32alp", svf);
            paramMap.put("methodName", "outputFuhakkou");
            paramMap.put("Z010.NAME1", _param._z010Name1);
            if (_param._z010.in(Z010.nishiyama)) {
                paramMap.put("fuhakkouKindName", "調査書（成績証明書）");
            }

            printData._outputFuhakkouResult = KNJG010.execMethod(_db2, paramMap);

            if (_param._isOutputDebug) {
                log.info("fuhakkou = " + printData._outputFuhakkouResult);
            }
        } catch (final Throwable e) {
            log.fatal("exception in executing KNJG010.execMethod()");
        }
        if ("true".equals(printData._outputFuhakkouResult.get("OUTPUT_SVF"))) {
            nonedata = true;
        }
        printData._isFuhakkou = "true".equals(printData._outputFuhakkouResult.get("IS_FUHAKKOU"));
    }

    public void printSvf(
            final String year,
            final String semester,
            final String date,
            final String schregno,
            final Map paramap,
            final String staffCd,
            final int certifKindcdInt,
            final String kanji,
            final String certifNumber) {
        _param._paramap = paramap;
        if (_param._isOutputDebug) {
            for (final String paramKey : new TreeMap<String, Object>(paramap).keySet()) {
                log.info("param " + paramKey + " = " + paramap.get(paramKey));
            }
        }
        _param.setDocumentroot((String) paramap.get("DOCUMENTROOT"));
        _param._useCurriculumcd = _param.property("useCurriculumcd");
        _param._useGakkaSchoolDiv = _param.property("useGakkaSchoolDiv");
        _param._useAddrField2 = _param.property("useAddrField2");
        _param._seisekishoumeishoNotPrintAnotherStudyrec = _param.property("seisekishoumeishoNotPrintAnotherStudyrec");
        _param._certifPrintRealName = _param.property("certifPrintRealName");
        _param._seisekishoumeishoPrintCoursecodename = _param.property("seisekishoumeishoPrintCoursecodename");
        _param._sogoTankyuStartYear = _param.property("sogoTankyuStartYear");
        _param._stampSizeMm = _param.property("knjg080StampSizeMm");
        if (!NumberUtils.isNumber(_param._stampSizeMm)) {
            _param._stampSizeMm = _param.property("stampSizeMm");
        }
        _param._stampPositionXmm = _param.property("knje080StampPositionXmm");
        _param._stampPositionYmm = _param.property("knje080StampPositionYmm");

        for (int g = 1; g <= 12; g++) {
            final String sogakuKoteiTanni = _param.property("sogakuKoteiTanni_" + String.valueOf(g));
            if (NumberUtils.isNumber(sogakuKoteiTanni)) {
                _param._setSogakuKoteiTanni = true;
                _param._sogakuKoteiTanniMap.put(g, new BigDecimal(sogakuKoteiTanni));
            }
        }
        if (_param._isOutputDebug) {
            if (_param._setSogakuKoteiTanni) {
                log.info(" _sogakuKoteiTanni = " + _param._sogakuKoteiTanniMap);
            }
        }
        _param._isCertifPrintPreferGradeCd = _param._isCertifPrintPreferGradeCd || "1".equals(_param.property("knje080PrintGradeCdAsGrade"));

        final PrintData printData = new PrintData(_isPrintGrd, year, semester, date, schregno, paramap, staffCd, certifKindcdInt, kanji, certifNumber, _param);
        log.debug(" schregno = " + printData._schregno);
        outputFuhakkou(_svf, printData);
        printData.load(_db2, _param);

        //成績証明書
        _form.print(_db2, printData);
        if (_form._hasData) {
            nonedata = true;
        }
    }

    private static String formatNentsuki(final DB2UDB db2, final Param param, final String date) {
        if (null == date) {
            return null;
        }
        final String year;
        if (param._isSeireki) {
            year = date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
        } else {
            year = KNJ_EditDate.h_format_JP_M(db2, date);
        }
        return year;
    }

    private static <T> T def(final T t, T def) {
        return null == t ? def : t;
    }

    private static String defstr(final Object o) {
        return defstr(o, "");
    }

    private static String defstr(final Object o, final String alt) {
        return null == o ? alt : o.toString();
    }

    private static String kakko(final String o) {
        return "(" + o + ")";
    }

    private static boolean isNotInt(final String s) {
        return NumberUtils.isNumber(s) && !NumberUtils.isDigits(s);
    }

//    /*
//     * @param val
//     * @return rtnVal
//     */
//    private String getRepValue(String val) {
//        String rtnVal = "";
//        if (null != val) {
//            if (_param._isChiyodaKudan) {
//                if (_param._nameMstD001Map.containsKey(val)) {
//                    rtnVal = (String) _param._nameMstD001Map.get(val);
//                } else {
//                    rtnVal = val;
//                }
//            } else {
//                rtnVal = val;
//            }
//        }
//        return rtnVal;
//    }

    /*
     *  クラス内で使用する定数設定
     */
    public void setClasscode(final String year) {
    }

    private static class Form {

        private Param _param;
        private final Vrw32alp _svf;
        private boolean _hasData;
        public String _formName;
        private boolean _isForm1;
        private boolean _isForm1_2;
        private boolean _isForm3;
        private boolean _isForm4;
        private TreeMap<Integer, Map<String, Title>> _pageYearTitleMap = null;
        private Set<String> _pageLogs;
        boolean _isLastPage;
        protected Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap<String, Map<String, SvfField>>();
        protected Map<String, SvfForm> _svfFormInfoMap = new HashMap<String, SvfForm>();
        private int _formNenyou;
        private int _subForm1yStart;
        private int _subForm1Height;
        private int _subclassRecordHeight;
        private int _subForm2Height;

        private int _lineMax1;
        private int _lineMax12;
        private int _lineMax122;
        private boolean _classnameIsTategaki;
        private Map<String, File> _flagFormnameMap = new HashMap<String, File>();

        Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        /**
         * 単位制の場合の処理など
         */
        private void setForm(final PrintData printData, final Param param) {
            int _formNenyou1 = 0,  _formNenyouEng = 0,  _formNenyou3 = 0,  _formNenyou4 = 0;
            String formName1 = null, formName1_2 = null, formName1Eng = null, formName3 = null, formName4 = null;
            formName1Eng = null;
            _lineMax1 = -1;
            _lineMax12 =  -1;
            _lineMax122 = -1;
            if (param._z010.in(Z010.sapporo)) {
                formName1 = "KNJE080_1SAP.frm";
                _formNenyou1 = 3;
                formName1Eng = "KNJE080_2SAP.frm";
                _formNenyouEng = 3;

                _lineMax1 = 19;
                _lineMax12 =  34;
                _lineMax122 = 35;
            } else if (param._z010.in(Z010.osakatoin)) {
                formName1 = "KNJE080_1TOIN.frm";
                _formNenyou1 = 4;
            } else if (param._z010.in(Z010.sakae)) {
                formName1 = "KNJE080_1SAKAE.frm";
                _formNenyou1 = 3;

                _lineMax1 = 28;
                _lineMax12 =  53;
                _lineMax122 = 53;
                _classnameIsTategaki = true;

            } else if (param._z010.in(Z010.ChiyodaKudan)) {
                formName1 = "KNJE080_1KUDAN.frm";
                _formNenyou1 = 3;

                _lineMax1 = 49;
                _lineMax12 = 52;
                _lineMax122 = 52;
                _classnameIsTategaki = true;
            } else if (param._z010.in(Z010.seijyo)) {
                formName1 = "KNJE080_1SEIJO.frm";
                _formNenyou1 = 3;

                _lineMax1 = 25;
                _lineMax12 = 46;
                _lineMax122 = 50;
            } else if (param._z010.in(Z010.miyagiken)) {
                if (printData._certifKindcdInt == CERTIF011) {
                    formName1 = "KNJG030_1MIYA.frm";
                    _formNenyou1 = 4;
                    formName3 = "KNJG030_3MIYA.frm";
                    _formNenyou3 = 4;
                    formName4 = "KNJG030_4MIYA.frm";
                    _formNenyou4 = 6;
                } else {
                    formName1 = "KNJE080_1MIYA.frm";
                    _formNenyou1 = 4;
                    formName3 = "KNJE080_3MIYA.frm";
                    _formNenyou3 = 4;
                    formName4 = "KNJE080_4MIYA.frm";
                    _formNenyou4 = 6;
                }

                _lineMax1 = 25;
                _lineMax12 = 46;
                _lineMax122 = 50;
            } else if (param._z010.in(Z010.tokiwa)) {
                formName1 = "KNJE080_1TOKIWA.frm";
                _formNenyou1 = 3;

                _lineMax1 = 40;
                _lineMax12 = 40;
                _lineMax122 = 40;

            } else if (param._isFormTori) {
                formName1 = "KNJE080_1TORI.frm";
                _formNenyou1 = 4;
                formName3 = "KNJE080_3TORI.frm";
                _formNenyou3 = 4;
                formName4 = "KNJE080_4TORI.frm";
                _formNenyou4 = 6;
            } else if (param._z010.in(Z010.Kindai)) {
                formName1 = "KNJE080_1KIN.frm";
                _formNenyou1 = 4;
            } else if (param._z010.in(Z010.hirokoudai)) {
                formName1 = "KNJE080_1HIROKOUDAI.frm";
                formName1_2 = formName1;
                _formNenyou1 = 3;

                _lineMax1 = 50;
                _lineMax12 = 50;
                _lineMax122 = 50;
            } else if (param._z010.in(Z010.kwansei)) {
                formName1_2 = "KNJE080_KWANSEI.frm";
                _formNenyou1 = 4;
            } else if (param._z010.in(Z010.suito)) {
                formName1 = "KNJE080_1A_SUITO.frm";
                _formNenyou1 = 4;
            } else if ("1".equals(printData._Knje080UseAForm)) {
                formName1 = "KNJE080_1A.frm";
                if (param._z010.in(Z010.kyoto)) {
                    formName1_2 = "KNJE080_1A_2.frm";
                }
                _formNenyou1 = 4;
                formName3 = "KNJE080_3A.frm";
                _formNenyou3 = 4;
                formName4 = "KNJE080_4A.frm";
                _formNenyou4 = 6;
            } else if ("1".equals(param.property(Property.knje080UseSupendMourningForm))) {
                formName1 = "KNJE080_1_SUSPMOUR.frm";
                _formNenyou1 = 4;
            } else {
                formName1 = "KNJE080_1.frm";
                _formNenyou1 = 4;
                formName3 = "KNJE080_3.frm";
                _formNenyou3 = 4;
                formName4 = "KNJE080_4.frm";
                _formNenyou4 = 6;
            }
            if (null == formName3) {
                formName3 = formName1;
                _formNenyou3 = _formNenyou1;
            }
            if (null == formName4) {
                formName4 = formName1;
                _formNenyou4 = _formNenyou1;
            }

            if (printData.isEng()) {
                _formName = formName1Eng;
                _formNenyou = _formNenyouEng;
            } else if (printData.isGakunensei(param) || _param._z010.in(Z010.sapporo)) {
                if (null != formName1_2) {
                    _formName = formName1_2;
                    _isForm1_2 = true;
                } else {
                    _formName = formName1;
                    _isForm1 = true;
                }
                _formNenyou = _formNenyou1;
            } else {
                //中高一貫の場合、4年生用を使用します。(フォームを再設定)<br>
                //但し、印刷指示画面において"6年用フォーム"がチェックされていない場合です。
                if (!StringUtils.isBlank((String) printData._paramap.get("FORM6")) || !param._isJuniorHiSchool && 4 < printData._yearTitleMap.size()) {
                    _formName = formName4;
                    _isForm4 = true;
                    _formNenyou = _formNenyou4;
                } else {
                    _formName = formName3;
                    _isForm3 = true;
                    _formNenyou = _formNenyou3;
                }
            }
        }

        public void setConfigForm(final PrintData printData, final Param param) {
            final File formFile = new File(_svf.getPath(_formName));
            if (!formFile.exists()) {
                log.warn("no file : " + _formName);
                return;
            }

            SvfForm svfForm = null;
            boolean readFile = false;
            try {
                svfForm = new SvfForm(formFile);
                svfForm._debug = param._isOutputDebugField;
                readFile = svfForm.readFile();
                if (readFile) {
                    final SvfForm.SubForm subForm = svfForm.getSubForm("SUBFORM1");
                    if (null != subForm) {
                        final SvfForm.Field field = svfForm.getField("CLASSNAME");
                        final SvfForm.Record record = svfForm.getRecordOfField(field);
                        if (null != record) {
                            final int lineMax1 = subForm.getHeight() / record.getHeight();
                            if (_lineMax1 == -1) {
                                _lineMax1 = lineMax1;
                                if (null != subForm._linkSubform) {
                                    final SvfForm.SubForm linksubForm = svfForm.getSubForm(subForm._linkSubform);
                                    if (null != linksubForm) {
                                        _lineMax12 = _lineMax1 + linksubForm.getHeight() / record.getHeight();
                                    }
                                }
                                _lineMax122 = _lineMax12;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            if (readFile) {
                final TreeMap<String, String> flags = getFlagMap(printData, svfForm, param);
                String flag = Util.mkString(flags, "|");
                if (!StringUtils.isBlank(flag)) {
                    flag = _formName + ":" + flag;
                    if (param._isOutputDebug) {
                        log.info(" modify flag = " + flag);
                    }
                    if (!param._createdfiles.containsKey(flag)) {
                        svfForm._debug = param._isOutputDebugField;
                        if (svfForm.readFile()) {

                            modifyForm(param, flags, svfForm);

                            try {
                                File file = svfForm.writeTempFile();
                                param._createdfiles.put(flag, file);
                            } catch (Exception e) {
                                log.error("exception!", e);
                            }
                        }
                    }
                    final File file = param._createdfiles.get(flag);
                    if (null != file) {
                        _formName = file.getName();
                    }
                }
            }
        }

        final String FLG_CREDIT_FIELD_DOT = "FLG_CREDIT_FIELD_DOT";
        final String FLG_MOVE_FIELD_KEISHO = "FLG_MOVE_FIELD_KEISHO";
        final String FLG_RESIZE_STAMP_SIZE = "FLG_RESIZE_STAMP_SIZE";
        final String FLG_REITAKU_PRINCIPAL_POS = "FLG_REITAKU_PRINCIPAL_POS";
        final String FLG_KWANSEI_SCHOOLADDRESS_POS = "FLG_KWANSEI_SCHOOLADDRESS_POS";
        final String FLG_KWANSEI_COMPCREDIT_CENTER = "FLG_KWANSEI_COMPCREDIT_CENTER";
        final String FLG_RYUKEI_HEADER  = "FLG_RYUKEI_HEADER";
        final String FLG_PRINT_DAY_OF_DATE  = "FLG_PRINT_DAY_OF_DATE";
        private TreeMap<String, String> getFlagMap(final PrintData printData, final SvfForm svfForm, final Param param) {
            final TreeMap<String, String> flags = new TreeMap<String, String>();
            if (param._setSogakuKoteiTanni) {
                flags.put(FLG_CREDIT_FIELD_DOT, "1");
            }
            if (param._isKeisho && "1".equals(printData._Knje080UseAForm)) {
                flags.put(FLG_MOVE_FIELD_KEISHO, "1");
            } else {
                if (!StringUtils.isBlank(param._stampSizeMm) || NumberUtils.isNumber(param._stampPositionXmm) || NumberUtils.isNumber(param._stampPositionYmm)) {
                    flags.put(FLG_RESIZE_STAMP_SIZE, "1");
                }
            }
            if (param._z010.in(Z010.reitaku)) {
                flags.put(FLG_REITAKU_PRINCIPAL_POS, "1");
            }
            if (param._z010.in(Z010.kwansei)) {
                flags.put(FLG_KWANSEI_SCHOOLADDRESS_POS, "1");
                flags.put(FLG_KWANSEI_COMPCREDIT_CENTER, "1");
            }
            if (param._z010.in(Z010.ryukei)) {
                flags.put(FLG_RYUKEI_HEADER, "1");
            }
            if (_param._knje080PrintDayOfDate) {
                flags.put(FLG_PRINT_DAY_OF_DATE, "1");
            }
            return flags;
        }

        private void modifyForm(final Param param, final TreeMap<String, String> flags, final SvfForm svfForm) {
            if (flags.containsKey(FLG_CREDIT_FIELD_DOT)) {
                if (_isForm1) {
                    for (final String fieldhead : Arrays.asList("subtotal", "CREDIT_3_", "abroad", "total")) {
                        for (int p = 1; p <= 5; p+=1) {
                            final String fieldname = fieldhead + String.valueOf(p);
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null != field) {
                                Field dotField = field.copyTo(fieldname + "_DOT").setFieldLength(5).setCharPoint10(60).setHenshuShiki("").setY(field._position._y + 13).setPrintMethod(SvfForm.Field.PrintMethod.CENTERING);
                                svfForm.addField(dotField);
                                if (param._isOutputDebug) {
                                    log.warn(" add field : " + dotField);
                                }
                            }
                        }
                    }
                } else {
                    for (final String fieldname : Arrays.asList("tani1", "tani2", "tani3", "tani4", "CREDIT", "TOTAL1", "TOTAL2", "TOTAL3", "TOTAL4", "TOTAL")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null == field) {
                            if (param._isOutputDebug) {
                                log.warn(" no such field : " + fieldname);
                            }
                        } else {
                            final Field dotField = field.copyTo(fieldname + "_DOT").setFieldLength(5).setCharPoint10(60).setHenshuShiki("").setY(field._position._y + 15);
                            svfForm.addField(dotField);
                            if (param._isOutputDebug) {
                                log.warn(" add field : " + dotField);
                            }
                        }
                    }
                }
            }

            if (flags.containsKey(FLG_MOVE_FIELD_KEISHO)) {
                // 印影
                final SvfForm.ImageField imageField = svfForm.getImageField("STAMP");
                if (null != imageField) {
                    final int addx = Util.mmToDot("60"); // 右へ60mm
                    final int addy = Util.mmToDot("5"); // 下へ5mm
                    svfForm.move(imageField, imageField.addX(addx).addY(addy));
                }

                // 校長役職名、校長氏名
                for (final String moveFieldname : Arrays.asList("JOBNAME", "STAFFNAME")) {
                    final SvfForm.Field field = svfForm.getField(moveFieldname);
                    if (null != field) {
                        final int addy = Util.mmToDot("-0.5"); // 上へ0.5mm

                        svfForm.removeField(field);
                        svfForm.addField(field.setY(field._position._y + addy));
                    }
                }
            }

            // 印影サイズ変更
            if (flags.containsKey(FLG_RESIZE_STAMP_SIZE)) {
                resizeStampImage(param, svfForm);
            }
            if (flags.containsKey(FLG_REITAKU_PRINCIPAL_POS)) {
                // 麗澤 校長名と職名を上に移動
                final SvfForm.Field STAFFNAME = svfForm.getField("STAFFNAME");
                svfForm.removeField(STAFFNAME);
                svfForm.addField(STAFFNAME.addY(-80));

                final SvfForm.Field JOBNAME = svfForm.getField("JOBNAME");
                svfForm.removeField(JOBNAME);
                svfForm.addField(JOBNAME.addY(-80 - 8));
            }
            if (flags.containsKey(FLG_KWANSEI_SCHOOLADDRESS_POS)) {
                // 関西学院 住所位置
                for (final String fieldname : Arrays.asList("SCHOOLADDRESS", "SCHOOLADDRESS2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addX(500).addY(-200));
                    }
                }
            }
            if (flags.containsKey(FLG_KWANSEI_COMPCREDIT_CENTER)) {
                // 関西学院 履修単位
                for (final String fieldname : Arrays.asList("COMP_CREDIT", "COMP_tani1_2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setPrintMethod(SvfForm.Field.PrintMethod.CENTERING));
                    }
                }
            }
            if (flags.containsKey(FLG_RYUKEI_HEADER)) {
                svfForm.addAdjtY(new BigDecimal(Util.mmToDot("5.0")));
                final int x = new BigDecimal(Util.mmToDot("35.0")).subtract(svfForm.getAdjtX()).intValue();
                final int y = new BigDecimal(Util.mmToDot("12.0")).subtract(svfForm.getAdjtY()).intValue();
                final int width = 160;
                final int charPoint9p = 90;
                final SvfForm.Font font = SvfForm.Font.Mincho;
                for (final SvfForm.Field f : Arrays.asList(
                        new SvfForm.Field(null, "HEADER_SCHREGNO", font, 8, x + width, false, new SvfForm.Point(x, y), charPoint9p, "ヘッダ学籍番号")
                      , new SvfForm.Field(null, "HEADER_NAME", font, 30, x + 50 + width, false, new SvfForm.Point(x + 50, y + 70), charPoint9p, "ヘッダ氏名")
                      , new SvfForm.Field(null, "HEADER_DATE", font, 16, x + width, false, new SvfForm.Point(x, y + 70 * 2), charPoint9p, "ヘッダ日付")
                        )) {
                    svfForm.addField(f);
                }
            }

            if (flags.containsKey(FLG_PRINT_DAY_OF_DATE)) {
                for (final String fieldname : Arrays.asList("KATEI", "GRADE1")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setFieldLength(field._fieldLength - 2).setEndX(field._position._x + (field._endX - field._position._x) * (field._fieldLength - 2) / field._fieldLength));
                    }
                }
                final SvfForm.Field fieldTRANSFER1 = svfForm.getField("TRANSFER1");
                if (null != fieldTRANSFER1) {
                    final SvfForm.Line rightLine = svfForm.getNearestRightLine(fieldTRANSFER1.getPoint());
                    if (null != rightLine) {
                        svfForm.move(rightLine, rightLine.addX(-90));
                    }
                    final SvfForm.Line leftLine = svfForm.getNearestLeftLine(fieldTRANSFER1.getPoint());
                    if (null != leftLine) {
                        svfForm.move(leftLine, leftLine.addX(-90));
                    }
                }
                for (final String fieldname : Arrays.asList("TRANSFER1", "TRANSFER2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addX(-80).setEndX(field._endX - 100));
                    }
                }
                for (final String fieldname : Arrays.asList("YEAR1", "YEAR2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addX(-90).setFieldLength(16));
                    }
                }
            }
        }

        private void resizeStampImage(final Param param, final SvfForm svfForm) {
            final String fieldname = "STAMP";
            final SvfForm.ImageField image = svfForm.getImageField(fieldname);
            if (null == image) {
                log.info(" no image : " + fieldname);
                return;
            }
            final int x = image._point._x;
            final int y = image._point._y;
            final int endX = image._endX;
            final int endY = y + image._height;
            final int l = NumberUtils.isNumber(param._stampSizeMm) ? Util.mmToDot(param._stampSizeMm) : image._height;
            final int newX;
            final int newEndX;
            if (NumberUtils.isNumber(param._stampPositionXmm)) {
                newX = Util.mmToDot(param._stampPositionXmm);
                newEndX = newX + l;
            } else {
                final int centerX = (x + endX) / 2;
                newX = centerX - l / 2;
                newEndX = centerX + l / 2;
            }
            final int newY;
            if (NumberUtils.isNumber(param._stampPositionYmm)) {
                newY = Util.mmToDot(param._stampPositionYmm);
            } else {
                final int centerY = (y + endY) / 2;
                newY = centerY - l / 2;
            }
            final int newHeight = l;

            final SvfForm.ImageField newImage = image.setFieldname(fieldname).setX(newX).setY(newY).setEndX(newEndX).setHeight(newHeight);
            svfForm.removeImageField(image);
            svfForm.addImageField(newImage);
            final BigDecimal xmm = Util.dotToMm(String.valueOf(x));
            final BigDecimal ymm = Util.dotToMm(String.valueOf(y));
            final BigDecimal hmm = Util.dotToMm(String.valueOf(image._height));
            final BigDecimal newXmm = Util.dotToMm(String.valueOf(newX));
            final BigDecimal newYmm = Util.dotToMm(String.valueOf(newY));
            final BigDecimal newHmm = Util.dotToMm(String.valueOf(l));
            log.info("move stamp (x=" + x + "(" + xmm + "mm), y=" + y + "(" + ymm + "mm), len = " + image._height + "(" + hmm + "mm)) ");
            log.info("        to (x=" + newX + "(" + newXmm + "mm), y=" + newY + "(" + newYmm + "mm), len = " + l + "(" + newHmm + "mm))");
        }

        public void print(final DB2UDB db2, final PrintData printData) {

            setForm(printData, _param);
            setConfigForm(printData, _param);

            _pageYearTitleMap = getPageYearTitleMap(printData);
            if (_pageYearTitleMap.isEmpty()) {
                log.warn(" pageYearMap empty.");
            }
            _pageLogs = new HashSet<String>();

            for (final Integer page : _pageYearTitleMap.keySet()) {

                _isLastPage = page == _pageYearTitleMap.lastKey();

                final String info = " schregno = " + printData._schregno + ", form = " + _formName + " (isGakunensei = " + printData.isGakunensei(_param) + ", nen = " + _formNenyou + ", isForm1 = " + _isForm1 + ")";
                if (!_pageLogs.contains(info)) {
                    log.info(info);
                    _pageLogs.add(info);
                }

                if (_param._isOutputDebug) {
                    if (_pageYearTitleMap.size() > 1) {
                        log.info(" page = " + page + " / " + _pageYearTitleMap.lastKey());
                    }
                }

                if (_param._isOutputDebug) {
                    log.info(" form1_2 = " + _isForm1_2);
                }

                if (_isLastPage) {

                    final Map<Integer, Title> pagePositionTitleMap = getPagePositionTitleMap(_pageYearTitleMap.get(page));

                    if ("1".equals(_param._paramap.get("seisekishoumeishoTaniPrintRyugaku")) && null == _param._paramap.get("RYUGAKU_CREDIT") || "1".equals(_param._paramap.get("RYUGAKU_CREDIT"))) {
                        // 留学単位数の欄に0を表示
                        for (int i = 0; i < Acc.N; i++) {
                            final Title title = pagePositionTitleMap.get(i);
                            if (null == title) {
                                continue;
                            }
                            if (printData._accGoukei.hasCredits(title._year)) {
                                printData._accAbroad = printData._accAbroad.add(title._year, Acc.FLAG_STUDYREC, "0");
                            }
                            if (printData._accGoukei.hasCompCredits(title._year)) {
                                printData._accAbroad = printData._accAbroad.add(title._year, Acc.FLAG_CHAIR_SUBCLASS, "0");
                            }
                        }
                    }
                    if ("1".equals(_param._paramap.get("SOUGAKU_CREDIT"))) {
                        // 総合的な学習の時間の単位数の欄に0を表示
                        for (int i = 0; i < Acc.N; i++) {
                            final Title title = pagePositionTitleMap.get(i);
                            if (null == title) {
                                continue;
                            }
                            if (printData._accGoukei.hasCredits(title._year)) {
                                printData._accSogo = printData._accSogo.add(title._year, Acc.FLAG_STUDYREC, "0");
                            }
                            if (printData._accGoukei.hasCompCredits(title._year)) {
                                printData._accSogo = printData._accSogo.add(title._year, Acc.FLAG_CHAIR_SUBCLASS, "0");
                            }
                        }
                    }
                }

                _svf.VrSetForm(_formName, 4);
                if (_param._isOutputDebug) {
                    log.info(" formName = " + _formName);
                }
                setFormInfo(printData, _svf);

                final RecordPages recordPages = getRecordPages(printData, page);
                if (_param._isOutputDebug) {
                    log.info(" recordPages = " + recordPages.status());
                }

                //学習の記録出力
                for (final List<Record> recordPage : recordPages._recordPageList) {

                    //学校名、校長名のセット
                    printHeader(printData, page);
                    //氏名、住所等出力
                    printAddress(db2, printData);
                    //出欠の出力
                    printShukketsu(printData, page);
                    if (printData._certifKindcdInt == CERTIF006 || printData._certifKindcdInt == CERTIF027 || printData._certifKindcdInt == CERTIF033 || printData._certifKindcdInt == CERTIF036) {
                        //所見の出力
                        printShoken(printData, page);
                    }

                    final boolean isCreditFooterRecord = !_isForm1;
                    if (!isCreditFooterRecord) {
                        if (_param._z010.in(Z010.tokiwa)) {
                            printSvfGakunenCreditForm1(printData, page, printData._accShokei.add(printData._accSogo), "subtotal");
                            printSvfGakunenCreditForm1(printData, page, printData._accTokiwahr, "HR");
                        } else {
                            printSvfGakunenCreditForm1(printData, page, printData._accShokei, "subtotal");
                        }
                        printSvfGakunenCreditForm1(printData, page, printData._accSogo, "CREDIT_3_");
                        printSvfGakunenCreditForm1(printData, page, printData._accAbroad, "abroad");
                    }

                    printRecord(printData, page, recordPage);

                    _svf.VrEndPage();
                }
            }
        }

        private TreeMap<Integer, Map<String, Title>> getPageYearTitleMap(final PrintData printData) {
            final TreeMap<Integer, Map<String, Title>> pageYearTitleMap = new TreeMap<Integer, Map<String, Title>>();
            int page = 1;
            if ("1".equals(printData._certifSchoolOnly)) {
                Util.getMappedMap(pageYearTitleMap, page).put(printData._year, new Title(printData._year, "01", 1, printData._nendo, null));
            } else {
                for (final Title title : printData._yearTitleMap.values()) {
                    while (page * _formNenyou < title._position) {
                        page += 1;
                    }
                    Util.getMappedMap(pageYearTitleMap, page).put(title._year, new Title(title._year, title._annual, title._position.intValue() - (page - 1) * _formNenyou, title._nendo, title._dropFlg));
                }
            }
            if (_param._isOutputDebug) {
                log.info(Util.debugMapToStr("yearTitleMap = ", printData._yearTitleMap) + " => " + Util.debugMapToStr("pageYearTitleMap = ", pageYearTitleMap));
            }
            return pageYearTitleMap;
        }

        private void setFormInfo(final PrintData printData, final Vrw32alp svf) {
            if (!_formFieldInfoMap.containsKey(_formName)) {
                _formFieldInfoMap.put(_formName, null);
                try {
                    final File formFile = new File(svf.getPath(_formName));
                    if (!formFile.exists()) {
                        throw new FileNotFoundException(formFile.getAbsolutePath());
                    }
                    _formFieldInfoMap.put(_formName, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                    final SvfForm svfForm = new SvfForm(formFile);
                    if (svfForm.readFile()) {
                        _svfFormInfoMap.put(_formName, svfForm);
                        final SvfForm.SubForm subForm1 = svfForm.getSubForm("SUBFORM1");
                        if (null != subForm1) {
                            _subForm1yStart = subForm1._point1._y;
                            _subForm1Height = subForm1.getHeight();
                        }
                        final SvfForm.SubForm subForm2 = svfForm.getSubForm("SUBFORM2");
                        if (null != subForm2) {
                            _subForm2Height = subForm2.getHeight();
                        }
                        SvfForm.Record subclassRecord = svfForm.getRecord("RECORD");
                        if (null == subclassRecord) {
                            subclassRecord = svfForm.getRecord("RECORD1");
                        }
                        if (null != subclassRecord) {
                            _subclassRecordHeight = subclassRecord.getHeight();
                        }

                        if (_param._isOutputDebug) {
                            if (_subForm1Height > 0 && _subclassRecordHeight > 0) {
                                log.info(" subform1 " + _subForm1Height + " / " + _subclassRecordHeight + " = " + (_subForm1Height * 1.0 / _subclassRecordHeight));
                            }
                            if (_subForm2Height > 0 && _subclassRecordHeight > 0) {
                                log.info(" subform2 " + _subForm2Height + " / " + _subclassRecordHeight + " = " + (_subForm2Height * 1.0 / _subclassRecordHeight));
                            }
                        }
                    }
                } catch (Throwable t) {
                    log.warn(" no class SvfField.", t);
                }
            }

            if (_lineMax122 == -1) {
                _lineMax1 = 23;
                _lineMax12 = 42;
                _lineMax122 = 46;
            }
        }

        public SvfField getField(final String fieldname) {
            try {
                SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
                return f;
            } catch (Throwable t) {
                final String key = _formName + "." + fieldname;
                if (!Util.getMappedMap(_formFieldInfoMap, "ERROR").containsKey(key)) {
                    log.warn(" svf field not found:" + key);
                    if (null == _formName) {
                        log.error(" form not set!");
                    }
                    Util.getMappedMap(_formFieldInfoMap, "ERROR").put(key, null);
                }

            }
            return null;
        }

        public Map getFieldStatusMap(final String fieldname) {
            final Map m = new HashMap();
            try {
                SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
                m.put("X", String.valueOf(f.x()));
                m.put("Y", String.valueOf(f.y()));
                m.put("Size", String.valueOf(f.size()));
                m.put("Keta", String.valueOf(f._fieldLength));
            } catch (Throwable t) {
                final String key = _formName + "." + fieldname;
                if (!Util.getMappedMap(_formFieldInfoMap, "ERROR").containsKey(key)) {
                    if (_param._isOutputDebugField) {
                        log.warn(" svf field not found:" + key);
                    }
                    if (null == _formName) {
                        log.error(" form not set!");
                    }
                    Util.getMappedMap(_formFieldInfoMap, "ERROR").put(key, null);
                }

            }
            return m;
        }

        public int getFieldKeta(final String fieldname, final int defaultKeta) {
            final Map statusMap = getFieldStatusMap(fieldname);
            try {
                final String keta = KnjDbUtils.getString(statusMap, "Keta");
                if (NumberUtils.isDigits(keta)) {
                    return Integer.parseInt(keta);
                }
            } catch (Throwable t) {
                log.warn(t);
            }
            return defaultKeta;
        }

        /*
         *  学校情報
         */
        private void printHeader(final PrintData printData, final Integer page) {

            if (_param._z010.in(Z010.sapporo)) {
                vrsOut("GRADENAME", "年次");
            }

            if (_param._z010.in(Z010.sapporo)) {
                final String title;
                if (CERTIF_KINDCD_ENG_MIKOMI.equals(printData._certifKind)) {
                    title = "Statement of Expected Results (Higher Secondary)";
                } else if (CERTIF_KINDCD_ENG.equals(printData._certifKind)) {
                    title = "Certificate of Results (Higher Secondary)";
                } else if (CERTIF_KINDCD_MIKOMI.equals(printData._certifKind)) {
                    title = "後期課程成績見込証明書";
                } else {
                    title = "後期課程成績証明書";
                }
                vrsOut("TITLE", title);
            }
            if (_param._z010.in(Z010.ChiyodaKudan)) {
                vrsOut("NAME_HEADER", "氏　名");
            } else if (_param._z010.in(Z010.kyoto)) {
                vrsOut("NAME_HEADER", "氏　　名");
            } else if (_param._z010.in(Z010.mieken)) {
                vrsOut("NAME_HEADER", "名　前");
            } else if (_param._z010.in(Z010.suito)) {
                vrsOut("NAME_HEADER", "生徒名");
                vrAttribute("NAME_HEADER", "Hensyu=4"); // 均等割
            }
//            printData._gradeHigh = (_param._isJuniorHiSchool && (3 < printData._annual)) ? printData._annual - 3 : printData._annual;
//            printData._isHiSchool = (_param._isJuniorHiSchool && (3 < printData._annual));
//            if (_param._isOutputDebug) {
//            	log.info(" printData._gradeHigh = " + printData._gradeHigh + ", isHiSchool = " + printData._isHiSchool);
//            }
            final String certHeader;
            if (_param._z010.in(Z010.sapporo)) {
                if (printData.isEng()) {
                    certHeader = "I certify that the above details are correct.";
                } else {
                    certHeader = "上記のとおり証明いたします。";
                }
            } else if (_param._z010.in(Z010.ChiyodaKudan)) {
                certHeader = "上記の記載事項に相違ないことを証明いたします。";
            } else if (_param._z010.in(Z010.kyoto)) {
                certHeader = "上記のとおり証明します。";
            } else if (_param._z010.in(Z010.miyagiken)) {
                certHeader = "上記のとおり証明する。";
            } else if (_param._isFormTori) {
                certHeader = "上記のとおり証明します";
            } else if (_param._z010.in(Z010.nishiyama)) {
                certHeader = "上記の通り証明する。";
            } else if (_param._z010.in(Z010.higashiosaka)) {
                certHeader = "上記の通り証明します";
            } else if (_param._z010.in(Z010.matsudo)) {
                certHeader = "上記の通り証明する。";
            } else {
                certHeader = "上記の通り証明します。";
            }
            vrsOut("CERT_HEADER", certHeader);
            printSchoolInfo(printData, page);  // 学校情報出力

            if (printData._isPrintStamp) {
                vrsOut("STAMP", printData._certifSchoolstampImagePath); // 校長印影
            }
        }

        /*
         *  [東京都用様式] 学校情報
         */
        private void printSchoolInfo(final PrintData printData, final Integer page) {

            if (!printData.isGakunensei(_param) && !printData.isEng()) {
                // 単位制の場合、
                // 学習の記録欄・出欠の記録欄等の欄における[年度/学年]列名を印字します。
                for (final Title title : Util.getMappedMap(_pageYearTitleMap, page).values()) {
                    final String pos = title._position.toString();
                    final String nendo = title._nendo;
                    if (_param._z010.in(Z010.hirokoudai)) {
                        vrsOut("GRADE1_" + pos, nendo);  // 学習の記録　左
                        vrsOut("GRADE1_" + pos, nendo);
                        vrsOut("GRADE1_" + pos, nendo);
                    } else {
                        final String t1 = nendo.substring(0, 2);
                        final String t2 = nendo.substring(2,nendo.length() - 2);
                        final String t3 = nendo.substring(nendo.length() - 2);
                        vrsOut("GRADE1_" + pos + "_1", t1);  // 学習の記録　左
                        vrsOut("GRADE1_" + pos + "_2", t2);
                        vrsOut("GRADE1_" + pos + "_3", t3);
                        vrsOut("GRADE2_" + pos + "_1", t1);  // 学習の記録　右
                        vrsOut("GRADE2_" + pos + "_2", t2);
                        vrsOut("GRADE2_" + pos + "_3", t3);
                    }

                    vrsOutn("GRADE3", title._position, nendo);  // 出欠の記録
                    if (_param._z010.in(Z010.sapporo)) {
                        vrsOut("GRADE1_" + pos, nendo);  // 学習の記録　左
                        vrsOut("GRADE2_" + pos, nendo);  // 学習の記録　左
                    }
                }
            }

            //過卒生対応年度取得->掲載日より年度を算出
            final Map<String, String> schoolInfoMap = printData._schoolInfoMap;
            if (!schoolInfoMap.isEmpty()) {
                vrsOut("DATE", printData._dateStr);   //記載日
                if (!_param._z010.in(Z010.Kindai)) {
                    vrsOut("NENDO", printData._nendo);    //年度
                    printData._syoshoname = defstr(KnjDbUtils.getString(schoolInfoMap, "SYOSYO_NAME"));  //証書名
                    printData._syoshoname2 = defstr(KnjDbUtils.getString(schoolInfoMap, "SYOSYO_NAME2")); //証書名２
                    if ("0".equals(KnjDbUtils.getString(schoolInfoMap, "CERTIF_NO"))) {
                        printData._isOutputCertifNo = true;  //証書番号の印刷 0:あり,1:なし
                    }
                    final String schoolname1 = KnjDbUtils.getString(schoolInfoMap, "SCHOOLNAME1");
                    if (_param._z010.in(Z010.sapporo)) {
                        vrsOut("SCHOOL_NAME", Util.trim(schoolname1));             //学校名
                    } else {
                        vrsOut("SCHOOLNAME", schoolname1);             //学校名
                    }
                    vrsOut("JOBNAME", KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_JOBNAME"));   //校長職名

                    // 画面のパラメータ"プログラムID=KNJE080"かつ"校長名を出力しない"とき校長名を出力しない
                    final boolean notOutputPrincipal = "KNJE080".equals(printData._paramap.get("PRGID")) && "2".equals(printData._paramap.get("OUTPUT_PRINCIPAL"));
                    final String principalName = KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME");
                    if (principalName != null && !notOutputPrincipal) {
                        vrsOut("STAFFNAME", principalName);   //校長名
                    }

                    if (_param._z010.in(Z010.kyoto)) {
                        vrAttribute("JOBNAME",   Util.mkString(Arrays.asList("Y=4168", "Size=15.0"), ","));   //校長職名
                        vrAttribute("STAFFNAME", Util.mkString(Arrays.asList("Y=4168", "Size=15.0"), ","));   //校長名
                    }

                    if (printData.isGakunensei(_param) || printData.isEng()) {
                        printGakunenMidashi(printData, page, true, printData.getSchooldiv(_param));
                    }

                    String staffname2 = "";
                    if (KnjDbUtils.getString(schoolInfoMap, "REMARK2") != null) {
                        staffname2 = KnjDbUtils.getString(schoolInfoMap, "REMARK2") + defstr(KnjDbUtils.getString(schoolInfoMap, "STAFF2_NAME"));
                    }
                    vrsOut("STAFFNAME2", staffname2);  //記載責任者

                    vrsOut("REMARK3", KnjDbUtils.getString(schoolInfoMap, "REMARK3"));

                    vrsOutWithCheckKeta(new String[] {"SCHOOLADDRESS", "SCHOOLADDRESS2"}, KnjDbUtils.getString(schoolInfoMap, "REMARK1")); //学校住所
                    //if(rs.getString("SCHOOLZIPCD")!= null)
                    //  svf.VrsOut("SCHOOLZIP"        ,"〒"+rs.getString("SCHOOLZIPCD"));          //郵便番号

                    // 備考
                    final List<String> remarkList = new ArrayList<String>();
                    if (printData._isFuhakkou) {
                        final String elapsedYears = (String) printData._outputFuhakkouResult.get("CERTIF_KIND_MST.ELAPSED_YEARS");
                        remarkList.add("法令で定められた保存期間（卒業後" + defstr(elapsedYears) + "年）が経過しているため、証明できません。");
                    } else if (_param._z010.in(Z010.mieken) || _param._z010.in(Z010.fukuiken)) {
                        if (null != KnjDbUtils.getString(schoolInfoMap, "REMARK4")) {
                            remarkList.add(KnjDbUtils.getString(schoolInfoMap, "REMARK4"));
                        }
                        if (null != KnjDbUtils.getString(schoolInfoMap, "REMARK5")) {
                            remarkList.add(KnjDbUtils.getString(schoolInfoMap, "REMARK5"));
                        }
                        if (null != KnjDbUtils.getString(schoolInfoMap, "REMARK6")) {
                            remarkList.add(KnjDbUtils.getString(schoolInfoMap, "REMARK6"));
                        }
                    } else if (_param._z010.in(Z010.ChiyodaKudan)) {
                        String remark = defstr(KnjDbUtils.getString(schoolInfoMap, "REMARK4")) + defstr(KnjDbUtils.getString(schoolInfoMap, "REMARK5")) + defstr(KnjDbUtils.getString(schoolInfoMap, "REMARK6"));
                        remark = StringUtils.replace(remark, "↓", "\n");
                        if (!StringUtils.isBlank(remark)) {
                            remarkList.add(remark);
                        }
                    } else {
                        if (null != KnjDbUtils.getString(schoolInfoMap, "REMARK4")) {
                            remarkList.add(KnjDbUtils.getString(schoolInfoMap, "REMARK4"));
                        }
                    }
                    if (_param._isOutputDebug) {
                        log.info(Util.debugListToStr("remarkList = ", remarkList));
                    }
                    if (!remarkList.isEmpty()) {
                        if (_param._z010.in(Z010.mieken) || _param._z010.in(Z010.fukuiken)) {
                            final List<String> fields = Arrays.asList("REMARK", "REMARK_2", "REMARK_3");
                            for (int i = 0; i < Math.min(remarkList.size(), fields.size()); i++) {
                                final String field = fields.get(i);
                                final String remark = remarkList.get(i);
                                vrsOut(field, remark);
                            }
                        } else if (_param._z010.in(Z010.tokiwa) || _param._z010.in(Z010.ChiyodaKudan)) {
                            final List<String> token = KNJ_EditKinsoku.getTokenList(Util.mkString(remarkList, ""), getFieldKeta("REMARK", 1000));
                            for (int i = 0; i < token.size(); i++) {
                                vrsOutn("REMARK", i + 1, token.get(i));
                            }
                        } else {
                            vrsOut("REMARK", Util.mkString(remarkList, ""));
                        }
                    }
                } else {
                    if (KnjDbUtils.getString(schoolInfoMap, "SCHOOLNAME1") != null) {
                        vrsOut("school_name",  KnjDbUtils.getString(schoolInfoMap, "SCHOOLNAME1"));      //学校名
                    }

                    if (_param._z010.in(Z010.Kindai)) {
                        log.debug("name=" + Util.editName(KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME")));
                        if (KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_JOBNAME") != null) {
                            vrsOut("STAFFJOBNAME_1", KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_JOBNAME"));  //校長名
                        }
                        if (KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME") != null) {
                            final int spaces = (KNJ_EditEdit.getMS932ByteLength(KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_JOBNAME")) - 6);
                            final String space = StringUtils.repeat("　", spaces / 2) + StringUtils.repeat(" ", spaces % 2);
                            vrsOut("STAFFNAME_1", space + defstr(KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME")));  //校長名
                        }

                    } else {
                        if (KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME") != null) {
                            vrsOut("STAFFNAME_1", KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME"));  //校長名
                        }
                    }

                    // 記載責任者出力
                    final StringBuffer stb = new StringBuffer();
                    stb.append(defstr(KnjDbUtils.getString(schoolInfoMap, "STAFF2_JOBNAME")));
                    stb.append("  ");
                    stb.append(defstr(KnjDbUtils.getString(schoolInfoMap, "STAFF2_NAME")));
                    vrsOut("STAFFNAME_2", stb.toString());                               //記載責任者

                    printGakunenMidashi(printData, page, false, KnjDbUtils.getString(schoolInfoMap, "T4SCHOOLDIV"));
                }
            }
        }

        private void printGakunenMidashi(final PrintData printData, final Integer page, final boolean checkGradeHigh, final String schooldiv) {
            if (_param._z010.in(Z010.seijyo)) {
                //学年見出し
                vrsOut("GRADE1_1", "1学年");
                vrsOut("GRADE1_2", "2学年");
                vrsOut("GRADE1_3", "3学年");
                vrsOut("GRADE2_1", "1学年");
                vrsOut("GRADE2_2", "2学年");
                vrsOut("GRADE2_3", "3学年");
                vrsOutn("GRADE3", 1, "1学年");
                vrsOutn("GRADE3", 2, "2学年");
                vrsOutn("GRADE3", 3, "3学年");
            } else {
                if ("1".equals(printData._certifSchoolOnly)) {
                    return;
                }
                final String bef, aft;
                if (printData.isEng()) {
                    bef = "Yr";
                    aft = "";
                } else if (_param._z010.in(Z010.tokiwa)) {
                    bef = "";
                    aft = "年";
                } else if (_param._z010.in(Z010.sakae)) {
                    bef = "";
                    aft = "";
                } else if (_param._z010.in(Z010.ChiyodaKudan) || _param._z010.in(Z010.hirokoudai)) {
                    bef = "第";
                    aft = ("0".equals(schooldiv) && !_param._z010.in(Z010.sapporo)) ? "学年" : "年次";
                } else {
                    bef = "";
                    aft = ("0".equals(schooldiv) && !_param._z010.in(Z010.sapporo)) ? "学年" : "年次";
                }
                final int gradeHigh = (checkGradeHigh && _param._isChukouIkkan && !_param._isCertifPrintPreferGradeCd && !_param._z010.in(Z010.sapporo)) ? 3 : 0;
                if (_param._isOutputDebug) {
                    log.info(" gradeHigh = " + gradeHigh);
                }

                int maxPosition = -1;
                for (final Title title : Util.getMappedMap(_pageYearTitleMap, page).values()) {
                    maxPosition = Math.max(maxPosition, title._position);
                }
                for (int i = 1; i <= maxPosition; i++) {
                    final String name = bef + String.valueOf((page - 1) * _formNenyou + i + gradeHigh) + aft;
                    final String name2 = String.valueOf((page - 1) * _formNenyou + i + gradeHigh);
                    vrsOut("GRADE1_" + i,  name);   //学年見出し
                    vrsOut("GRADE2_" + i,  name);   //学年見出し
                    if (_param._z010.in(Z010.ChiyodaKudan)) {
                        vrsOutn("GRADE3",  i,  name2);              //学年見出し
                    } else {
                        vrsOutn("GRADE3",  i,  name);               //学年見出し
                    }
                }
            }
        }

        private void vrsOutWithCheckKeta(final String[] fields, final String data) {
            if (null == fields || fields.length == 0) {
                throw new IllegalArgumentException("フィールド名指定不正");
            }
            final int dataKeta = KNJ_EditEdit.getMS932ByteLength(data);
            String lastField = null;
            String firstValidField = null;
            for (int i = 0; i < fields.length; i++) {
                final int fieldKeta = getFieldKeta(fields[i], 0);
                if (fieldKeta > 0) {
                    if (null == firstValidField && dataKeta <= fieldKeta) {
                        firstValidField = fields[i];
                    }
                    lastField = fields[i];
                }
            }
            if (null != firstValidField) {
                vrsOut(firstValidField, data);
            } else if (null != lastField) {
                if (_param._isOutputDebug) {
                    log.warn(" 桁数オーバー? field " + lastField + " (keta = " + getFieldKeta(lastField, 0) + ") 内容 " + data + "(keta = " + dataKeta + ")");
                }
                vrsOut(lastField, data);
            } else if (_param._isOutputDebugField) {
                log.warn("no such field:" + ArrayUtils.toString(fields));
            }
        }

        private Map<String, String> getEntGrdJRow(final DB2UDB db2, final PrintData printData) {
            String sql = "";
            sql += " SELECT T1.SCHOOL_KIND ";
            sql += "      , T1.ENT_DIV ";
            sql += "      , A002.NAME1 AS ENT_DIV_NAME ";
            sql += "      , T1.ENT_DATE ";
            sql += "      , T1.GRD_DIV ";
            sql += "      , A003.NAME1 AS GRD_DIV_NAME ";
            sql += "      , T1.GRD_DATE ";
            sql += " FROM SCHREG_ENT_GRD_HIST_DAT T1 ";
            sql += " LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' ";
            sql += "     AND A002.NAMECD2 = T1.ENT_DIV ";
            sql += " LEFT JOIN NAME_MST A003 ON A003.NAMECD1 = 'A003' ";
            sql += "     AND A003.NAMECD2 = T1.GRD_DIV ";
            sql += " WHERE T1.SCHREGNO = '" + printData._schregno + "' AND SCHOOL_KIND = 'J' ";
            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
        }

        /*
         *  個人情報
         */
        private void printAddress(final DB2UDB db2, final PrintData printData) {
            if (null == printData._personalInfoMap || "1".equals(printData._certifSchoolOnly)) {
                return;
            }

            if (printData._schregno != null) {
                vrsOut("schregno",   printData._schregno);
            }
            printData._entDate = KnjDbUtils.getString(printData._personalInfoMap, "ENT_DATE");

            if (!_param._z010.in(Z010.Kindai)) {
                // 証書名
                final String certifName;
                if (printData._isOutputCertifNo) {
                    // 証明書番号が無い場合 5スペース挿入
                    certifName = printData._syoshoname + (printData._certifNumber == null || "".equals(printData._certifNumber) ? "     " : printData._certifNumber) + printData._syoshoname2;
                } else {
                    certifName = printData._syoshoname + "     " + printData._syoshoname2;
                }
                vrsOut("SYOSYO_NAME",  certifName);  //証書番号
                vrsOut("NENDO_NAME",  printData._certifNumber + printData._syoshoname);  //証明書番号
            }

            // 氏名
            final String rsName = KnjDbUtils.getString(printData._personalInfoMap, "NAME");
            final String rsAnnual = KnjDbUtils.getString(printData._personalInfoMap, "ANNUAL");
            if (_param._z010.in(Z010.ryukei)) {
                vrsOut("HEADER_SCHREGNO", printData._schregno);
                vrsOut("HEADER_NAME", rsName);
                vrsOut("HEADER_DATE", KNJ_EditDate.h_format_SeirekiJP(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            }
            if (_param._z010.in(Z010.Kindai)) {
                vrsOutWithCheckKeta(new String[] {"NAME", "NAME2", "NAME3"}, rsName);
            } else {
                final String name;
                if (printData.isEng()) {
                    name = KnjDbUtils.getString(printData._personalInfoMap, "NAME_ENG");
                } else {
                    if ("1".equals(KnjDbUtils.getString(printData._personalInfoMap, "USE_REAL_NAME")) || "1".equals(_param._certifPrintRealName)) {
                        if ("1".equals(KnjDbUtils.getString(printData._personalInfoMap, "NAME_OUTPUT_FLG"))) {
                            name = defstr(KnjDbUtils.getString(printData._personalInfoMap, "REAL_NAME")) + "(" + defstr(rsName) + ")";
                        } else {
                            name = KnjDbUtils.getString(printData._personalInfoMap, "REAL_NAME");
                        }
                    } else {
                        name = rsName;
                    }
                }
                vrsOutWithCheckKeta(new String[] {"NAME", "NAME2", "NAME3"}, name);
            }

            // 課程名
            String rsCoursename = KnjDbUtils.getString(printData._personalInfoMap, "COURSENAME");
            boolean checkKatei = _param._z010.in(Z010.ChiyodaKudan);
            if (printData.isEng()) {
                rsCoursename = KnjDbUtils.getString(printData._personalInfoMap, "COURSEENG");
            } else if (checkKatei && null != rsCoursename && !rsCoursename.endsWith("課程")) {
                rsCoursename += "課程";
            }
            if (_param._z010.in(Z010.hirokoudai)) {
                if (!StringUtils.isBlank(rsCoursename)) {
                    rsCoursename = "(" + rsCoursename + ")";
                }
            }
            // 学科名
            String rsMajorname;
            if (printData.isEng()) {
                rsMajorname = KnjDbUtils.getString(printData._personalInfoMap, "MAJORENG");
            } else {
                rsMajorname = KnjDbUtils.getString(printData._personalInfoMap, "MAJORNAME");
                if ("1".equals(_param._seisekishoumeishoPrintCoursecodename)) {
                    if (!printData._personalInfoMap.containsKey("COURSECODEABBV1")) {
                        log.error(" not contained " + "COURSECODEABBV1" + " in " + printData._personalInfoMap.keySet());
                    } else {
                        rsMajorname = defstr(rsMajorname) + defstr(KnjDbUtils.getString(printData._personalInfoMap, "COURSECODEABBV1"));
                    }
                }
            }

            // 課程 学科
            if (_param._z010.in(Z010.Kindai)) {
                vrsOut("katei", rsCoursename);
                vrsOut("gakka", rsMajorname);
            } else {
                vrsOutWithCheckKeta(new String[] {"katei", "katei2"}, rsCoursename);
                if (!StringUtils.isBlank(rsMajorname)) {
                    final int majornameKeta = KNJ_EditEdit.getMS932ByteLength(rsMajorname);
                    if (_param._isFormTori) {
                        vrsOut("GAKKA",      rsMajorname);
                    } else if (_param._z010.in(Z010.sapporo)) {
                        if (printData.isEng()) {
                            if (majornameKeta > 18) {
                                final List<String> token = retDividEnglish(rsMajorname, 18);
                                for (int i = 0; i < token.size(); i++) {
                                    vrsOut("gakka2_" + String.valueOf(i + 1), token.get(i));
                                }
                            } else {
                                vrsOut("gakka", rsMajorname);
                            }
                        } else {
                            vrsOut("gakka", rsMajorname);
                        }
                    } else {
                        // 1行（中央）に6文字と　6文字を超えたら上下に2行で印字する。
                        final int gakkaFieldKeta = getFieldKeta("gakka", 12);
                        String field = "gakka";
                        if (gakkaFieldKeta < majornameKeta) {
                            final int gakka4FieldKeta = getFieldKeta("gakka4", 0);
                            if (_param._z010.in(Z010.musashinohigashi) && gakka4FieldKeta > 0) {
                                field = "gakka4"; // 中央行
                            } else {
                                final String[] fields = {"gakka2", "gakak5", "gakka6", "gakka7"};
                                String ketteiField = null;
                                String kouho = null;
                                for (int i = 0; i < fields.length; i++) {
                                    final int fieldKeta = getFieldKeta(fields[i], 0);
                                    if (0 < fieldKeta) {
                                        if (majornameKeta <= fieldKeta) {
                                            ketteiField = fields[i];
                                            break;
                                        }
                                        kouho = fields[i];
                                    }
                                }
                                if (null != ketteiField) {
                                    field = ketteiField;
                                } else if (null != kouho) {
                                    field = kouho;
                                }
                            }
                        }
                        vrsOut(field, rsMajorname);
                    }
                }
            }

            // 学年
            if (rsAnnual != null) {
                final String grade;
                if (_param._z010.in(Z010.Kindai)) {
                    int igrade = Integer.parseInt(rsAnnual);
                    if (_param._isHesetuKou && (3 < igrade)) {
                        igrade = igrade - 3;
                    }
                    grade = String.valueOf(igrade);
                } else {
                    if (printData.isEng()) {
                        grade = "Yr" + String.valueOf(Integer.parseInt(rsAnnual));
                    } else {
                        int igrade;
                        if (!printData.isGakunensei(_param)) {
                            igrade = Integer.parseInt(rsAnnual);
                        } else if (_param._hasSchoolKindPJH) {
                            final String gradeCd = _param.getGradeCdOfGrade(printData._year, rsAnnual);
                            igrade = Integer.parseInt(NumberUtils.isDigits(gradeCd) ? gradeCd : rsAnnual);
                        } else {
                            if (NumberUtils.isDigits(printData._regdGradeCd)) {
                                if (_param._isHesetuKou && !_param._z010.in(Z010.sapporo) || _param._isChukouIkkan && _param._isCertifPrintPreferGradeCd || !_param._isHesetuKou && !_param._isChukouIkkan) {
                                    igrade = Integer.parseInt(printData._regdGradeCd);
                                } else {
                                    igrade = Integer.parseInt(rsAnnual);
                                }
                            } else {
                                igrade = Integer.parseInt(rsAnnual);
                            }
                        }
                        grade = String.valueOf(igrade) + (printData.isGakunensei(_param) && !_param._z010.in(Z010.sapporo) ? "年生" : "年次");
                    }
                }
                vrsOut("GRADE", grade);
            }
            // 生年月日
            vrsOut("BIRTHDAY", Util.formatBirthday(db2, _param, printData, KnjDbUtils.getString(printData._personalInfoMap, "BIRTHDAY"), _param._isSeireki || "1".equals(KnjDbUtils.getString(printData._personalInfoMap, "BIRTHDAY_FLG"))));

            //　入学卒業と日付
            printEntGrd(db2, printData);

            if (!_param._z010.in(Z010.Kindai)) {
                // 氏名かな
                final String nameKana;
                if (printData.isEng()) {
                    nameKana = KnjDbUtils.getString(printData._personalInfoMap, "NAME_ENG");
                } else {
                    if ("1".equals(KnjDbUtils.getString(printData._personalInfoMap, "USE_REAL_NAME")) || "1".equals(_param._certifPrintRealName)) {
                        final String realNameKana = KnjDbUtils.getString(printData._personalInfoMap, "REAL_NAME_KANA");
                        if ("1".equals(KnjDbUtils.getString(printData._personalInfoMap, "NAME_OUTPUT_FLG"))) {
                            nameKana = defstr(realNameKana) + "(" + defstr(KnjDbUtils.getString(printData._personalInfoMap, "NAME_KANA")) + ")";
                        } else {
                            nameKana = realNameKana;
                        }
                    } else {
                        nameKana = KnjDbUtils.getString(printData._personalInfoMap, "NAME_KANA");
                    }
                }
                vrsOutWithCheckKeta(new String[] {"KANA2", "KANA"}, nameKana);
            }
            if (!_param._z010.in(Z010.Kindai)) {
                // 性別
                vrsOut("SEX", KnjDbUtils.getString(printData._personalInfoMap, "SEX"));
                if (_param._z010.in(Z010.ChiyodaKudan)) {
                    vrsOut("SEX" + KnjDbUtils.getString(printData._personalInfoMap, "SEX_FLG"), "○");
                } else if (_param._z010.in(Z010.sapporo)) {
                    if ("男".equals(KnjDbUtils.getString(printData._personalInfoMap, "SEX"))) {
                        vrsOut("MALE", "○");
                    } else if ("女".equals(KnjDbUtils.getString(printData._personalInfoMap, "SEX"))) {
                        vrsOut("FEMALE", "○");
                    }
                }
            }
        }

        // 入学卒業と日付の出力
        private void printEntGrd(final DB2UDB db2, final PrintData printData) {
            final Map<String, String> entGrdJRow = getEntGrdJRow(db2, printData); // 中学のデータ

            // 入学
            final String enterName = KnjDbUtils.getString(printData._personalInfoMap, "ENTER_NAME");
            final String gakunenOrNenji = printData.isGakunensei(_param) ? "学年" : "年次";
            if (_param._z010.in(Z010.Kindai)) {
                vrsOutNotNull("YEAR1", formatNentsuki(db2, _param, printData._entDate));
                vrsOutNotNull("TRANSFER1", enterName);
            } else {
                if (printData._entDate != null) { // 入学年月
                    final String year1;
                    final boolean formatContainsDate = _param._z010.in(Z010.miyagiken, Z010.hirokoudai, Z010.kwansei, Z010.suito) || _param._knje080PrintDayOfDate;
                    if (printData.isEng()) {
                        year1 = Util.h_format_US(printData._entDate, d_MMMM_yyyy);
                    } else if (_param._isSeireki) {
                        year1 = Util.getChristianEra(printData._entDate, formatContainsDate ? "yyyy年M月d日" : "yyyy年M月");
                    } else {
                        year1 = formatContainsDate ? KNJ_EditDate.h_format_JP(db2, printData._entDate) : KNJ_EditDate.h_format_JP_M(db2, printData._entDate);
                    }
                    vrsOut("YEAR1", year1);
                }

                final String entDiv = KnjDbUtils.getString(printData._personalInfoMap, "ENT_DIV");
                if (enterName != null) {
                    String transfer1 = "";
                    if (_param._z010.in(Z010.ChiyodaKudan)) {
                        final String enterGradeH = KnjDbUtils.getString(printData._personalInfoMap, "ENTER_GRADE");
                        final String entDateJ = KnjDbUtils.getString(entGrdJRow, "ENT_DATE");
                        final String entDivNameH;
                        final boolean hasEntGrdRowJ = null != entGrdJRow && !entGrdJRow.isEmpty(); // 中学の入学卒業履歴データがある
                        if (hasEntGrdRowJ) {
                            entDivNameH = "進級";
                        } else {
                            if ("4".equals(entDiv) || "5".equals(entDiv)) {
                                entDivNameH = enterName;
                            } else {
                                entDivNameH = "入学";
                            }
                        }
                        transfer1 += "第" + String.valueOf(Integer.parseInt(enterGradeH)) + "学年" + defstr(entDivNameH);

                        if (hasEntGrdRowJ) {
                            if (null != entDateJ) {
                                final String entDivJ = KnjDbUtils.getString(entGrdJRow, "ENT_DIV");
                                final String entDivNameJ;
                                if ("4".equals(entDivJ) || "5".equals(entDivJ)) {
                                    entDivNameJ = KnjDbUtils.getString(entGrdJRow, "ENT_DIV_NAME");
                                } else {
                                    entDivNameJ = "入学";
                                }
                                transfer1 += "(" + defstr(KNJ_EditDate.h_format_JP_M(db2, entDateJ)) + defstr(entDivNameJ) + ")";
                            }
                        }
                    } else if ("4".equals(entDiv) || "5".equals(entDiv)) { // 編入・転入
                        transfer1 = enterName;
//                    } else if (_param._isJisyuukan) {
//                        if (-1 < enterName.indexOf("入学")) {
//                            transfer1 = "後期課程開始";
//                        } else if (-1 == enterName.indexOf("入学")) {
//                            transfer1 = enterName;
//                        }
                    } else {
                        transfer1 = "入学";
                    }
                    if (_param._z010.in(Z010.suito)) {
                        transfer1 = (StringUtils.isBlank(transfer1) ? "入学" : transfer1) + "年月日";
                    }
                    vrsOut("TRANSFER1", transfer1);

                    if (_param._z010.in(Z010.sapporo)) {
                        if (printData.isEng()) {
                            if ("4".equals(entDiv) || "5".equals(entDiv)) { // 転入学 or 編入学
                                vrsOut("TRANSFER1_2", "○");
                            } else if ("1".equals(entDiv) || "2".equals(entDiv) || "3".equals(entDiv)) {
                                vrsOut("TRANSFER1_1", "○");
                            }
                        } else {
                            if ("4".equals(entDiv)) { // 転入学
                                vrsOut("TRANSFER1_3", "○");
                            } else if ("5".equals(entDiv)) { // 編入学
                                vrsOut("TRANSFER1_2", "○");
                            } else if ("1".equals(entDiv) || "2".equals(entDiv) || "3".equals(entDiv)) {
                                vrsOut("TRANSFER1_1", "○");
                            }
                        }
                    }
                }
                if (_param._z010.in(Z010.miyagiken)) {
                    final String enterGradeCd = KnjDbUtils.getString(printData._personalInfoMap, "ENTER_GRADE_CD");
                    if (NumberUtils.isDigits(enterGradeCd)) {
                        vrsOut("GRADE1", "第" + String.valueOf(Integer.parseInt(enterGradeCd)) + gakunenOrNenji);
                    }
                } else {
                    final String enterGrade = KnjDbUtils.getString(printData._personalInfoMap, "ENTER_GRADE");
                    if (NumberUtils.isDigits(enterGrade)) {
                        vrsOut("TRANSFER_GRADE1", "第" + String.valueOf(Integer.parseInt(enterGrade) + "学年"));
                    }
                }
            }

            // 卒業
            final String grdDiv = KnjDbUtils.getString(printData._personalInfoMap, "GRD_DIV");
            final String rsGraduDate = KnjDbUtils.getString(printData._personalInfoMap, "GRADU_DATE");
            final String rsGraduName = KnjDbUtils.getString(printData._personalInfoMap, "GRADU_NAME");
            if (_param._z010.in(Z010.Kindai)) {
                vrsOutNotNull("YEAR2", formatNentsuki(db2, _param, rsGraduDate));
                vrsOutNotNull("TRANSFER2", rsGraduName);
            } else if (_param._z010.in(Z010.kwansei)) {
                final String date;
                final String transfer2;
                if (null == grdDiv || "4".equals(grdDiv)) {
                     // 日付フォーマット
                    date = printData._date; // 在学中は発行日を表示する
                    transfer2 = "在学中";
                } else {
                    date = rsGraduDate;
                    transfer2 = rsGraduName;
                }
                final String year2;
                if (_param._isSeireki) {
                    year2 = Util.getChristianEra(date, "yyyy年M月d日");
                } else {
                    year2 = KNJ_EditDate.h_format_JP(db2, date);
                }
                vrsOutNotNull("YEAR2", year2);
                vrsOutNotNull("TRANSFER2", transfer2);
            } else {
                if (rsGraduName != null) {
                    if (printData.isEng()) {
                        vrsOut("YEAR2", Util.h_format_US(rsGraduDate, d_MMMM_yyyy));
                    } else {
                        String year2 = null;
                        if (rsGraduDate != null) { // 卒業年月
                            final boolean isHiduke = _param._z010.in(Z010.miyagiken, Z010.hirokoudai, Z010.suito) || _param._knje080PrintDayOfDate;
                            if (_param._isSeireki) {
                                year2 = isHiduke ? Util.getChristianEra(rsGraduDate, "yyyy年M月d日") : Util.getChristianEra(rsGraduDate, "yyyy年M月");
                            } else {
                                year2 = isHiduke ? KNJ_EditDate.h_format_JP(db2, rsGraduDate) : KNJ_EditDate.h_format_JP_M(db2, rsGraduDate);
                            }
                        }
                        if ("卒業見込み".equals(rsGraduName)) {
                            if (_param._z010.in(Z010.miyagiken)) {
                                vrsOut("TRANSFER2",  rsGraduName);
                                vrsOut("YEAR2", year2);
                            } else if (_param._z010.in(Z010.hirokoudai) && !_param.isTsushin(db2, printData._year)) {
                                vrsOut("TRANSFER2",  "卒業見込");
                                vrsOut("YEAR2", year2);
                            }
                        } else {
                            if (_param._z010.in(Z010.seijyo)) {
                                vrsOut("TRANSFER", defstr(year2) + defstr(rsGraduName));

                            } else if (_param._z010.in(Z010.suito)) {
                                vrsOut("TRANSFER2", defstr(rsGraduName) + "年月日");
                                vrsOut("YEAR2", year2);
                            } else {
                                vrsOut("TRANSFER2",  rsGraduName);
                                vrsOut("YEAR2", year2);
                            }
                            if (_param._z010.in(Z010.miyagiken)) {
                                final String graduGradeCd = KnjDbUtils.getString(printData._personalInfoMap, "GRADU_GRADE_CD");
                                if (NumberUtils.isDigits(graduGradeCd)) {
                                    vrsOut("GRADE2", "第" + String.valueOf(Integer.parseInt(graduGradeCd)) + gakunenOrNenji);
                                }
                            } else {
                                vrsOut("GRADE",  ""); //卒業生は学年を表示しない
                            }

                            if (rsGraduDate != null && (_param._isFormTori)) {
                                vrsOut("GRADE", rsGraduName);
                            }
                        }
                    }
                }
                if (_param._z010.in(Z010.sapporo)) {
                    if ("1".equals(grdDiv)) { // 卒業
                        vrsOut("TRANSFER2_1", "○");
                    } else if ("2".equals(grdDiv)) { // 退学
                        vrsOut("TRANSFER2_2", "○");
                    } else if ("卒業見込み".equals(rsGraduName)) { // 卒業見込
                        vrsOut("TRANSFER2_3", "○");
                    }
                }
            }

            if (_param._z010.in(Z010.Kindai)) {
                if (enterName != null) {
                    final String transfer1;
                    if (-1 < enterName.lastIndexOf("入学")) {
                        transfer1 = "入学";
                    } else {
                        transfer1 = enterName;
                    }
                    vrsOut("TRANSFER1", transfer1);
                }
            }
        }

        private List<String> retDividEnglish(final String s, final int keta) {
            if (null == s || s.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final List<StringBuffer> stbList = new ArrayList<StringBuffer>();
            StringBuffer line = null;
            final String[] spl = StringUtils.split(s, " ");
            for (int i = 0; i < spl.length; i++) {
                final String word = spl[i];
                String blank = " ";
                if (null == line || line.length() + " ".length() + word.length() > keta) {
                    line = new StringBuffer();
                    stbList.add(line);
                    blank = "";
                }
                line.append(blank).append(word);
            }
            final List<String> rtn = new ArrayList<String>();
            for (final StringBuffer stb : stbList) {
                rtn.add(stb.toString());
            }
            return rtn;
        }

        /**
         *  出欠データ
         **/
        private void printShukketsu(final PrintData printData, final Integer page) {
            if (printData._isFuhakkou || "1".equals(printData._certifSchoolOnly)) {
                // 表示しない
                return;
            }
            if (_param._z010.in(Z010.sakae) || _param._z010.in(Z010.kwansei)) { // 欄が無い
                return;
            }

            final int flg1 = 1;
            final List<String> printYearList = new ArrayList<String>();
            final Map<String, Map<String, String>> yearAttendMap = new HashMap<String, Map<String, String>>();
            for (final Map<String, String> row : printData._attendList) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                yearAttendMap.put(year, row);
                final Title title = getPrintPosition(flg1, printData, page, year);
                if (null != title && null != title._dropFlg) {
                    continue;
                }
                printYearList.add(year);
            }

            if (_param._z010.in(Z010.meikei)) {
                final List<String> fields = Arrays.asList("ATTEND_1", "SUSPEND", "MOURNING", "ABROAD", "REQUIREPRESENT", "ATTEND_6", "PRESENT");

                for (final String year : printYearList) {
                    final Title title = getPrintPosition(flg1, printData, page, year);
                    for (final Title t : printData._yearTitleMap.values()) {
                        final Map<String, String> dropAttend = yearAttendMap.get(t._year);
                        if (printData._offdaysYears.contains(t._year) && null != t._dropFlg && null != t._annual && t._annual.equals(title._annual)) {
                            if (_param._isOutputDebug) {
                                log.info(" add drop attend (" + t._year + ", " + Util.debugMapToStr("", dropAttend) + ") to attend(" + year + ")");
                            }
                            if (null != dropAttend) {
                                final Map<String, String> addRow = yearAttendMap.containsKey(year) ? new HashMap<String, String>(yearAttendMap.get(year)) : new HashMap<String, String>();
                                for (final String field : fields) {
                                    addRow.put(field, Util.addNumber(addRow.get(field), KnjDbUtils.getString(dropAttend, field)));
                                }
                                yearAttendMap.put(year, addRow);
                            }
                        }
                    }
                }
            }

            for (final String year : printYearList) {
                final Title title = getPrintPosition(flg1, printData, page, year);
                if (null == title) {
                    log.info(" null title : year = " + year + " / " + Util.getMappedMap(_pageYearTitleMap, page));
                    continue;
                }
                final int i = title._position;
                final Map<String, String> row = yearAttendMap.get(year);

                vrsOutnNotNull("attend_1", i, KnjDbUtils.getString(row, "ATTEND_1"));         //授業日数
                vrsOutnNotNull("attend_2", i, KnjDbUtils.getString(row, "SUSPEND"));          //出停
                vrsOutnNotNull("attend_3", i, KnjDbUtils.getString(row, "MOURNING"));         //忌引
                vrsOutnNotNull("attend_23", i, Util.addNumber(KnjDbUtils.getString(row, "SUSPEND"), KnjDbUtils.getString(row, "MOURNING")));         //出停忌引
                vrsOutnNotNull("attend_4", i, KnjDbUtils.getString(row, "ABROAD"));           //留学
                vrsOutnNotNull("attend_5", i, KnjDbUtils.getString(row, "REQUIREPRESENT"));   //要出席
                vrsOutnNotNull("attend_6", i, KnjDbUtils.getString(row, "ATTEND_6"));         //欠席
                vrsOutnNotNull("attend_7", i, KnjDbUtils.getString(row, "PRESENT"));          //出席
            }
        }

        /*
         *  所見データ
         */
        private void printShoken(final PrintData printData, final Integer page) {
            if (printData._isFuhakkou || "1".equals(printData._certifSchoolOnly)) {
                // 表示しない
                return;
            }
            if (_param._z010.in(Z010.sakae)) { // 欄が無い
                return;
            }
            for (final Map<String, String> rs : printData._remarkList) {

                final Title title = getPrintPosition(2, printData, page, KnjDbUtils.getString(rs, "YEAR"));
                if (null == title || null != title._dropFlg) {
                    continue;
                }
                final int i = title._position;
                //log.info(" year = " + year + " , i = " + i);
                String attendrecRemark = KnjDbUtils.getString(rs, "ATTENDREC_REMARK");
                final String field = "attend_8_1";
                final String field_1 = "attend_8_1_1";
                final String field_2 = "attend_8_1_2";
                final int keta_1 = getFieldKeta(field_1, 0);
                if (getFieldKeta(field, 40) < KNJ_EditEdit.getMS932ByteLength(attendrecRemark) && getFieldKeta(field, 40) < keta_1 * 2) {
                    final String[] fields = {field_1, field_2};
                    if (KNJ_EditKinsoku.getTokenList(attendrecRemark, keta_1).size() > fields.length) {
                        attendrecRemark = StringUtils.replace(StringUtils.replace(StringUtils.replace(attendrecRemark, "\r\n", " "), "\r", " "), "\n", " ");
                    }
                    final List<String> tokenList = KNJ_EditKinsoku.getTokenList(attendrecRemark, keta_1);
                    for (int ti = 0; ti < Math.min(tokenList.size(), fields.length); ti++) {
                        vrsOutn(fields[ti], i, tokenList.get(ti));
                    }
                } else {
                    attendrecRemark = StringUtils.replace(StringUtils.replace(StringUtils.replace(attendrecRemark, "\r\n", " "), "\r", " "), "\n", " ");
                    vrsOutn(field, i, attendrecRemark); //出欠備考
                }
            }
        }

        private Record printSvfBlank(final int i, final Record record) {
            record.vrsOutR("CLASSCD", String.valueOf(i));  // 教科コード
            record.vrsOutR("CLASSNAME", String.valueOf(i));  // 教科コード
            record.vrAttributeR("CLASSNAME", "X=10000");
            return record;
        }

        /*
         *  学習の記録
         */
        private void printRecord(final PrintData printData, final Integer page, final List<Record> recordPage) {
            boolean hasdata0 = false;

            for (final Record record : recordPage) {
                record.endrecord();
                hasdata0 = true;
            }

            final int linex2 = recordPage.size(); //行数

            final List<Acc> lastList = new ArrayList<Acc>();
            Record last = null;
            final boolean isCreditFooterRecord = !_isForm1;
            if (isCreditFooterRecord) {
                if (_param._isOutputDebug) {
                    log.info(" not Form1.");
                }
                if (printData._accDaiken.hasAnyCredits()) { lastList.add(printData._accDaiken); } // 高認等における認定単位
                if (printData._accZenseki.hasAnyCredits()) { lastList.add(printData._accZenseki); } //  前籍校における修得単位
                final boolean notPrintShokei = _param._z010.in(Z010.hirokoudai, Z010.kwansei, Z010.sapporo);
                if (!notPrintShokei) {
                    lastList.add(printData._accShokei); // 小計
                }
                if (!_param._z010.in(Z010.sapporo)) {
                    lastList.add(printData._accSogo); // 総合的な学習の時間
                }
                if (printData._accKyoto88.hasAnyCredits()) { lastList.add(printData._accKyoto88); } //  京都府の自立活動
                if (!_param._z010.in(Z010.hirokoudai) || printData._accAbroad.hasAnyCredits()) {
                    lastList.add(printData._accAbroad); // 留学
                }
                if (_param._z010.in(Z010.hirokoudai)) {
                    final Record record = nextRecord2(null, "total");
                    printData._accGoukei._subclassname = null;
                    printData._accGoukei._item = null;
                    record.printSvfCredits("total", printData, page, printData._accGoukei);
                    record.output();
                } else if (!_param._z010.in(Z010.sapporo)) {
                    lastList.add(printData._accGoukei); // 総合計
                }
                if (_param._isOutputDebug) {
                    log.info(Util.debugListToStr(" lastList = ", lastList));
                }
                //  空行
                for (int i = linex2; i < _lineMax122 - lastList.size(); i++) {
                    printSvfBlank(i, nextRecord2(null, "blank" + String.valueOf(i))).endrecord();
                    hasdata0 = true;
                }
                for (int i = 0; i < lastList.size(); i++) {
                    Acc acc = lastList.get(i);
                    final Record record = nextRecord2(null, "not form1");
                    final String field;
                    if (_param._z010.in(Z010.hirokoudai)) {
                        field = "CREDIT_2";
                    } else if (_param._z010.in(Z010.sapporo)) {
                        field = "total5";
                    } else {
                        field = "total";
                    }
                    record.printSvfCredits(field, printData, page, acc);
                    if (i == lastList.size() - 1) {
                        last = record;
                        last._name = "last";
                    } else {
                        record.endrecord();
                    }
                }
            } else {
                if (_param._isOutputDebug) {
                    log.info(" Form1.");
                }
                if (_param._z010.in(Z010.sapporo)) {
                    //  空行
                    for (int i = linex2; i < _lineMax122 - lastList.size(); i++) {
                        printSvfBlank(i, nextRecord2(null, "blank" + String.valueOf(i))).endrecord();
                        hasdata0 = true;
                    }
                } else if (null != printData._lastLineClasscd) {
                    //  空行
                    if (printData.studyrecLastLineClassList.size() > 0) {
                        final int restSize = _param._z010.in(Z010.tokiwa) ? 0 : 4;
                        for (int i = linex2; i < _lineMax122 - lastList.size() - restSize - 1; i++) {
                            printSvfBlank(i, nextRecord2(null, "blank" + String.valueOf(i))).endrecord();
                            hasdata0 = true;
                        }
                        final int lastLine = (_lineMax122 - lastList.size() - restSize - 1) % _lineMax1;
                        final Studyrec sumLastLine = Studyrec.sum(printData.studyrecLastLineClassList);
                        final int subclassname2keta = getFieldKeta("SUBCLASSNAME_2", 0);
                        final String sfx;
                        if (subclassname2keta > 0) {
                            // 評定斜線レコード
                            sfx = "_2";
                        } else {
                            sfx = "";
                        }
                        final Record record = nextRecord2(null, "form1");
                        record.printSvfClassnameSubclassname(printData, sumLastLine._classcd, sumLastLine._classname, sumLastLine._classname, sumLastLine._subclassname, lastLine, sfx);
                        record.printSvfHyotei(printData, page, sumLastLine, sfx);
                        record.endrecord();
                    }
                }
                //  総合的な学習の時間
                vrsOut("ITEM_SOGO", printData._accSogo._item);
                //  総合計
                printSvfGakunenCreditForm1(printData, page, printData._accGoukei, "total");
            }
            if (!hasdata0) {
                //  学習情報がない場合の処理
                if (null == last) {
                    last = nextRecord2(null, "last");
                }
                last.vrsOutR("CLASSNAME", "-");                  //教科名
                last.vrAttributeR("CLASSNAME", "X=10000");
            }
            if (null == last) {
                last = nextRecord2(null, "last");
            }
            last.endrecord();
            _hasData = true;
        }

        private RecordPages getRecordPages(final PrintData printData, final Integer page) {
            String s_specialDiv = "00";
            String s_subclasscd = "00";
            final boolean formHasClassTitle = getFieldKeta("CLASSTITLE", 0) > 0;

            final List<StudyrecClass> classList = StudyrecClass.groupByClassList(printData._studyrecList);
            if (_param._z010.in(Z010.kyoto)) {
                for (int cli = 0; cli < classList.size(); cli++) {
                    final StudyrecClass classMap = classList.get(cli);
                    final String classname = classMap._classname;
                    if (StudyrecSql.kyoto88.equals(classname)) {
                        classList.remove(cli);
                    }
                }
            }

            if (_param._isOutputDebug) {
                log.info(" classList size = " + classList.size() + " (studyrecList size = " + printData._studyrecList.size() + ")");
            }

            final RecordPages recordPages = new RecordPages();
            final boolean isPrintClassTitle = !(_param._z010.in(Z010.Kindai) || _param._z010.in(Z010.tokiwa) || _param._z010.in(Z010.sapporo) || _param._z010.in(Z010.sakae));
            for (int cli = 0; cli < classList.size(); cli++) {

                final StudyrecClass clazz = classList.get(cli);

                if (_param._isOutputDebugPrint) {
                    log.info(" classcd = " + clazz._classcd + ", list size = " + clazz._studyrecList.size());
                }

                for (int sri = 0; sri < clazz._studyrecList.size(); sri++) {
                    final Studyrec studyrec = clazz._studyrecList.get(sri);

                    if (formHasClassTitle && _param.isPrintClassTitle(printData._certifKind) && !"1".equals(printData._notUseClassMstSpecialDiv)) {
                        if (studyrec._specialDiv != null && !studyrec._specialDiv.equals(s_specialDiv)) {
                            if (isPrintClassTitle) {
                                if (!s_subclasscd.equals("00")) {
                                    if (recordPages.currentPage().size() == _lineMax1) {
                                        final Record record = nextRecord(recordPages, "column last");
                                        record.vrsOutR("CLASSCD", "");
                                        record.vrAttributeR("CLASSNAME", "X=10000");
                                        record.vrsOutR("CLASSNAME", "--");
                                    }
                                }
                                final String specialDivName = _param.getSpecialDivName(_param._z010.in(Z010.sapporo) || Param.isNewForm(_param, printData), studyrec._specialDiv);
                                if (_param._isOutputDebug) {
                                    log.info(" specialDivName = " + specialDivName);
                                }
                                final Record record = nextRecord(recordPages, "classtitle");
                                record.vrsOutR("CLASSTITLE", specialDivName);
                            }
                        }
                    }
                    s_specialDiv = studyrec._specialDiv;
                    if (_param._z010.in(Z010.sapporo)) {
                        if (NumberUtils.isDigits(s_specialDiv) && Integer.parseInt(s_specialDiv) == 1) {
                            while (recordPages.currentPage().size() < _lineMax1) {
                                printSvfBlank(recordPages.currentPage().size(), nextRecord(recordPages, "blank" + String.valueOf(recordPages.currentPage().size())));
                            }
                        }
                    }

                    final String printClassname;
                    if (_classnameIsTategaki) {
                        printClassname = sri < defstr(studyrec._classname).length() ? String.valueOf(studyrec._classname.charAt(sri)) : "";
                    } else {
                        printClassname = (recordPages.currentPage().size() == _lineMax1 || sri == 0) ? studyrec._classname : "";
                    }
                    final int ln = recordPages.currentPage().size() % _lineMax1;
                    final Record record = nextRecord(recordPages, " studyrec i = " + sri);
                    record.printSvfClassnameSubclassname(printData, studyrec._classcd, studyrec._classname, printClassname, studyrec._subclassname, ln, "");
                    record.printSvfHyotei(printData, page, studyrec, "");
                    s_subclasscd = studyrec._subclasscd;
                }

                if (_classnameIsTategaki) {
                    for (int clni = clazz._studyrecList.size(); clni < clazz._classname.length(); clni++) {
                        final String printClassname = String.valueOf(clazz._classname.charAt(clni));
                        final Record record = nextRecord(recordPages, "printClassname " + printClassname);
                        //科目コードの変わり目
                        record.vrsOutR("CLASSNAME", printClassname);        //教科名
                        record.vrsOutR("CLASSCD", clazz._classcd);  // 教科コード
                    }
                }
            }
            return recordPages;
        }

        public Map<Integer, Title> getPagePositionTitleMap(final Map<String, Title> titleMap) {
            final Map<Integer, Title> rtn = new TreeMap<Integer, Title>();
            for (final Title title : titleMap.values()) {
                rtn.put(title._position, title);
            }
            return rtn;
        }

        public Title getTitleOfPageYear(final String year, final Integer page) {
            return Util.getMappedMap(_pageYearTitleMap, page).get(year);
        }

        public Title getPrintPosition(final int flg, final PrintData printData, final Integer page, final String year) {
            if (flg == 0) {
                Title i = null;
                if (NumberUtils.isDigits(year)) {
                    final String yearKey = String.valueOf(Integer.parseInt(year)); // 年度
                    final Title title = getTitleOfPageYear(yearKey, page);
                    if (null == title) {
                        if (_param._isOutputDebug) {
                            final String error = " no title " + yearKey + " in " + Util.getMappedMap(_pageYearTitleMap, page).keySet();
                            if (!_pageLogs.contains(error)) {
                                _pageLogs.add(error);
                                log.info(error);
                            }
                        }
                        return i;
                    }
                    i = title;
                }
                return i;
            } else if (flg == 1 || flg == 2) {
                Title i = null;
                if (printData.isGakunensei(_param)) {
                    final Title title = getTitleOfPageYear(year, page);
                    if (null == title) {
                        final String error = " no title " + year + " in " + Util.getMappedMap(_pageYearTitleMap, page).keySet();
                        if (!_pageLogs.contains(error)) {
                            _pageLogs.add(error);
                            log.info(error);
                        }
                        return null;
                    }
                    if (null != title && null != title._dropFlg) {
                        if (_param._isOutputDebug) {
                            log.info("留年時のデータは表示しない: year = " + year);
                        }
                    }
                    i = title;
                } else {
                    String yearKey = year;
                    if (null == yearKey) {
                        return null;
                    }
                    if (0 == Integer.parseInt(yearKey)) { yearKey = "0"; }
                    final Title title = getTitleOfPageYear(yearKey, page);
                    if (null == title) {
                        return null;
                    }
                    i = title;
                }
                if (_param._isOutputDebug) {
                    log.info(" attend title year = " + year + ", pos = " + i);
                }
                return i;
            }
            return null;
        }

        private int vrsOut(final String fieldname, final String data) {
            SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
            if (null == f) {
                if (_param._isOutputDebugPrint) {
                    log.warn("no field : " + _formName + "." + fieldname + " (data  = " + data + ")");
                }
            } else if (_param._isOutputDebugField) {
                log.info("svf.VrsOut(\"" + fieldname + "\", " + (null == data ? "null" : "\"" + data + "\");"));
            }
            return _svf.VrsOut(fieldname, data);
        }

        private int vrsOutn(final String fieldname, int gyo, final String data) {
            SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
            if (null == f) {
                if (_param._isOutputDebugPrint) {
                    log.warn("no field : " + _formName + "." + fieldname + " (data  = " + data + ")");
                }
            } else if (_param._isOutputDebugField) {
                log.info("svf.VrsOutn(\"" + fieldname + "\", " + gyo + ", " + (null == data ? "null" : "\"" + data + "\");"));
            }
            return _svf.VrsOutn(fieldname, gyo, data);
        }

        private int vrsOutnNotNull(final String fieldname, int gyo, final String data) {
            if (null == data) {
                return -1;
            }
            return vrsOutn(fieldname, gyo, data);
        }

        private int vrsOutNotNull(final String fieldname, final String data) {
            if (null == data) {
                return -1;
            }
            return vrsOut(fieldname, data);
        }

        private int vrAttribute(final String fieldname, final String attribute) {
            SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
            if (null == f) {
                if (_param._isOutputDebugPrint) {
                    log.warn("no field : " + _formName + "." + fieldname + " (attribute  = " + attribute + ")");
                }
            } else if (_param._isOutputDebugField) {
                log.info("svf.VrAttribute(\"" + fieldname + "\", " + (null == attribute ? "null" : "\"" + attribute + "\");"));
            }
            return _svf.VrAttribute(fieldname, attribute);
        }

        private void printSvfGakunenCreditForm1(final PrintData printData, final Integer page, final Acc acc, final String fieldHead) {
            if ("1".equals(printData._certifSchoolOnly)) {
                return;
            }
            if (_param._isOutputDebug) {
                log.info("print total credit : " + acc);
            }
            boolean hasCredit = false;
            boolean hasCompCredit = false;
            String total = "0";
            String totalComp = "0";
            for (final String year : acc.years()) {
                if (StudyrecSql.TOTAL_YEAR.equals(year)) {
                    continue;
                }
                final Title title = getTitleOfPageYear(year, page);
                if (null != title) {
                    if (!printData._isFuhakkou) {
                        final String sfx = title._position < Acc.N ? String.valueOf(title._position) : "";
                        String v = null;
                        if (acc.hasCredits(year)) {
                            v = acc._credits.get(year);  //修得単位数
                        } else if (acc.hasCompCredits(year)) {
                            v = kakko(acc._compCredits.get(year));  //単位マスタの単位数
                        }
                        if (null != v) {
                            String fieldname = fieldHead + sfx;
                            if (_param._setSogakuKoteiTanni && isNotInt(v)) {
                                vrsOut(fieldname, "");
                                fieldname += "_DOT";
                            }
                            vrsOut(fieldname, v);
                        }
                    }
                }
                if (acc.hasCredits(year)) {
                    hasCredit = true;
                    total = Util.addNumber(total, acc._credits.get(year));
                } else if (acc.hasCompCredits(year)) {
                    hasCompCredit = true;
                    totalComp = Util.addNumber(totalComp, acc._compCredits.get(year));
                }
                if (_isLastPage) {
                    String v = null;
                    if (hasCredit) {
                        v = total;  //修得単位数 合計
                    } else if (hasCompCredit) {
                        v = kakko(totalComp);  //単位マスタの単位数 合計
                    }
                    if (null != v) {
                        String fieldname = fieldHead  +"5";
                        if (_param._setSogakuKoteiTanni && isNotInt(v)) {
                            vrsOut(fieldname, "");
                            fieldname += "_DOT";
                        }
                        vrsOut(fieldname, v);
                    }
                }
            }
        }

        private Record nextRecord2(final List<Record> recordList, final String name) {
            final Record record = new Record();
            record._name = name;
            //recordList.add(record);
            return record;
        }


        private Record nextRecord(final RecordPages recordPages, final String name) {
            final Record record = new Record();
            record._name = name;
            recordPages.currentPage().add(record);
            return record;
        }

        private class Record {
            String _name;
            final Map<String, String> _data = new HashMap<String, String>();
            final Map<String, List<String>> _attr = new HashMap<String, List<String>>();

            void vrsOutR(final String field, final String data) {
                _data.put(field, data);
            }
            void vrAttributeR(final String field, final String data) {
                Util.getMappedList(_attr, field).add(data);
            }

            private void printSvfCredits(final String field, final PrintData printData, final Integer page, final Acc acc) {
                vrsOutR("ITEM", defstr(acc._subclassname, acc._item));
                //log.info(" /// acc = " + acc + " in " + acc.years() + " " + field);

                if (!printData._isFuhakkou) {
                    for (final String year : acc.years()) {
                        final Title title = getTitleOfPageYear(year, page);
                        //log.info(" acc = " + acc + ", year = " + year + " in " + acc.years() + " ... title = " + title);
                        if (null != title && title._position < Acc.N) {
                            final String no = title._position.toString();
                            String fieldname = field + no;
                            String val = null;
                            if (acc.hasCredits(year)) {
                                val = acc._credits.get(year);  //修得単位数
                                if (_param._setSogakuKoteiTanni && isNotInt(val)) {
                                    fieldname += "_DOT";
                                }
                            } else if (acc.hasCompCredits(year)) {
                                val = kakko(acc._compCredits.get(year));  //単位マスタの単位数
                            }
                            if (null != val) {
                                vrsOutR(fieldname, val);
                            }
                            if (_param._z010.in(Z010.kwansei)) {
                                if (acc.hasCompCredits(year)) {
                                    vrsOutR("COMP_" + fieldname, defstr(acc._compCredits.get(year)));
                                }
                            }
                        }
                    }
                }
                if (_isLastPage) {
                    String fieldname = field;
                    String val = null;
                    if (acc.hasAnyCredits()) {
                        val = acc.sumCredits();  //修得単位数
                        if (_param._setSogakuKoteiTanni && isNotInt(val)) {
                            fieldname += "_DOT";
                        }
                    } else if (acc.hasAnyCompCredits()) {
                        val = kakko(acc.sumCompCredits());  //単位マスタの単位数
                    }
                    if (null != val) {
                        vrsOutR(fieldname, val);
                    }
                    if (_param._z010.in(Z010.kwansei)) {
                        if (acc.hasAnyCompCredits()) {
                            vrsOutR("COMP_" + fieldname, acc.sumCompCredits());
                        }
                    }
                }
            }

            private void printSvfHyotei(final PrintData printData, final Integer page, final Studyrec studyrec, final String sfx) {
                int gradeCreditKei = 0;
                int compCreditKei = 0;
                //学年ごとの出力
                for (final Grades g : studyrec._gradesList) {

//                    //中高一貫で選択した学年が高校（4・5・6学年）のときは、高校のデータを出力する。
//                    if (_param._isJuniorHiSchool && printData._isHiSchool) {
//                        int annualInt = Integer.parseInt(g._annualOrYear);
//                        if (1 <= annualInt && annualInt <= 3) {
//                            continue;
//                        }
//                    }
                    if (printData._gakunenseiTengakuYears.contains(g._year) && Acc.FLAG_STUDYREC.equals(g._studyFlag)) {
                        if (_param._isOutputDebug) {
                            log.info(" 転学日付が3/31より前の日付の場合、転学年度の成績は認められないため科目名のみ表示し評定、単位を表示しない:" + g._year + " in " + printData._gakunenseiTengakuYears);
                        }
                        continue;
                    }
                    //小計・総合的な学習の時間・留学
                    final Title title = getPrintPosition(0, printData, page, g._year);
                    if (null == title || null != title._dropFlg) {
                        continue;
                    }
                    final int i = title._position;
                    if (printData._isFuhakkou) {
                        // 表示しない
                    } else {
                        if (g._grades != null) {
                            vrsOutR("GRADES" + i + sfx, g.getHyotei(_param));  //評定
                        } else if (Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag)) {
                            vrsOutR("GRADES" + i + sfx, "-");  //評定
                        }
                    }
                    if (g._gradeCredit != null) {
                        String val = null;
                        if (!printData._isFuhakkou) {
                            if (Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag)) {
                                val = kakko(g._gradeCredit);  //単位マスタの単位数
                            } else {
                                val = studyrec.yearCredit(g._year); //修得単位数
                            }
                            if (null != val) {
                                vrsOutR("tani" + i + sfx, val);
                            }
                        }
                        if (!Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag)) {
                            if (_isLastPage) {
                                gradeCreditKei += Integer.parseInt(g._gradeCredit);
                                if (_param._isOutputDebugPrint) {
                                    log.info(" subclass = " + studyrec._subclasscd + ":" + studyrec._subclassname + " | " + g._year + " / " + g._gradeCredit + "(" + gradeCreditKei + ")");
                                }
                                vrsOutR("CREDIT" + sfx, String.valueOf(gradeCreditKei));   //単位数の合計（科目）
                                if (_param._z010.in(Z010.kwansei)) {
                                    if (_param._isOutputDebugPrint) {
                                        log.info(" subclass = " + studyrec._subclasscd + ":" + studyrec._subclassname + " | " + g._year + " / compCredit = " + g._gradeCompCredit + "(" + compCreditKei + ")");
                                    }
                                    if (NumberUtils.isDigits(g._gradeCompCredit)) {
                                        compCreditKei += Integer.parseInt(g._gradeCompCredit);
                                        vrsOutR("COMP_CREDIT" + sfx, String.valueOf(compCreditKei));   //単位数の合計（科目）
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private void printSvfClassnameSubclassname(final PrintData printData, final String classcd, final String classname, final String printClassname, final String subclassname, final int nameLine, final String fieldSfx) {
                //科目コードの変わり目
                if (printData.isGakunensei(_param) && !_classnameIsTategaki) {
                    vrsOutR("CLASSNAME" + fieldSfx, classname);        //教科名
                } else {
                    vrsOutR("CLASSNAME" + fieldSfx, printClassname);        //教科名
                    vrsOutR("CLASSCD" + fieldSfx, classcd);  // 教科コード
                }
                if (!_param._z010.in(Z010.tokiwa)) {
                    final String info = " subForm1yStart = " + _subForm1yStart + ", subForm1Height = " + _subForm1Height + ", subclassRecordHeight = " + _subclassRecordHeight;
                    if (_param._isOutputDebugField && !_pageLogs.contains(info)) {
                        log.info(info);
                        _pageLogs.add(info);
                    }

                    if (!_param._z010.in(Z010.Kindai) && !_classnameIsTategaki) {
                        svfFieldAttributeClassname("CLASSNAME" + fieldSfx, classname, nameLine, _subclassRecordHeight, _subForm1yStart);        //教科名
                    }
                    svfFieldAttributeSubclassname("SUBCLASSNAME" + fieldSfx, subclassname, nameLine, _subclassRecordHeight, _subForm1yStart, printData);
                }
                vrsOutR("SUBCLASSNAME" + fieldSfx, subclassname);     //科目名
            }

            /*
             * SVF-FORM フィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
             */
            private void svfFieldAttributeSubclassname(
                    final String fieldname,
                    final String subclassname,
                    final int ln,
                    final int height,
                    final int ystart,
                    final PrintData printData
            ) {
                if (!_param._z010.in(Z010.Kindai)) {
                    final int minnum = _param._z010.in(Z010.ChiyodaKudan) ? 18 : _param._z010.in(Z010.sapporo) ? 20 : 14;     //最小設定文字数
                    final int maxnum = _param._z010.in(Z010.ChiyodaKudan) ? 46 : 40;     //最大設定文字数
                    final int width = _param._z010.in(Z010.sapporo) ? (printData.isEng() ? 620 : 388) : _param._z010.in(Z010.hirokoudai) ? 700 : 388;     //フィールドの幅(ドット)
                    final int charHeight = height - (_param._z010.in(Z010.hirokoudai) ? 10 : 0);
                    final KNJSvfFieldModify svfobj = new KNJSvfFieldModify(width, charHeight, ystart + (_param._z010.in(Z010.hirokoudai) ? 5 : 0), minnum, maxnum);
                    final float charSize = svfobj.getCharSize(subclassname);
                    final int yModify = (int) svfobj.getYjiku(ln, charSize, height);
                    vrAttributeR(fieldname, "Y="+ yModify);
                    vrAttributeR(fieldname, "Size=" + charSize);
//                    svf.VrAttribute(fieldname, "Hensyu=" + 4); // 均等割付
                } else {
                    //int bketa = 2;
                    final int subclassbyte = KNJ_EditEdit.getMS932ByteLength(subclassname);
                    int jiku = 0;
                    double size = 0.0;

                    double[][] tabs = {
                          //bytes, base, charPointSize
                            {14,  0    , 10.0}
                          , {16,  6 + 0,  8.9}
                          , {18, 12 + 0,  7.5}
                          , {20, 12 + 0,  7.0}
                          , {22, 12 + 0,  6.4}
                          , {24, 16 + 0,  5.6}
                          , {26, 17 + 0,  5.3}
                          , {28, 18 + 2,  4.8}
                          , {30, 18 + 3,  4.4}
                          , {32, 18 + 4,  4.2}
                          , {34, 18 + 5,  3.9}
                          , {36, 15 + 6,  3.7}
                          , {38, 15 + 7,  3.6}
                          , {-1, 15 + 8,  3.4}
                    };

                    try {
                        final int heightBase = 1003;
                        double[] usetab = null;
                        for (double[] tab : tabs) {
                            final int tabSubcassbyte = (int) tab[0];
                            if (tabSubcassbyte == -1 || tabSubcassbyte != -1 && subclassbyte <= tabSubcassbyte) {
                                usetab = tab;
                                break;
                            }
                        }
                        final int tabheight = (int) usetab[1];
                        final double tabSize = usetab[2];
                        int heightOffset = heightBase + tabheight;
                        size = tabSize;
                        jiku = heightOffset + 98 * ln;

                    } catch (Exception e) {
                        log.error("jiku & size set error! ", e);
                    }

                    try {
                        vrAttributeR(fieldname, "Y="+ jiku);
                        vrAttributeR(fieldname, "Size=" + size);
                        //log.debug(" Size = " + size + " --> linex="+ln+"  byte="+subclassbyte+"  subclassname="+subclassname);
                    } catch (Exception e) {
                        log.error("svf.VrAttribute error! ", e);
                    }
                }
            }

            /*
             * SVF-FORM フィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
             */
            private void svfFieldAttributeClassname(final String fieldname, final String classname, final int ln, final int height, final int ystart) {
                int width = -1;
                SvfForm form = _svfFormInfoMap.get(_formName);
                if (null != form) {
                    final SvfForm.Field field = form.getField(fieldname);
                    if (null != field) {
                        width = field._endX - field._position._x;
                    }
                }
                final int minnum = _param._z010.in(Z010.sapporo) ? 8 : _param._z010.in(Z010.hirokoudai) ? 4 : 10;     //最小設定文字数
                final int maxnum = 20;     //最大設定文字数
                //log.info(" " + fieldname + " width = " + width);
                width = _param._z010.in(Z010.ChiyodaKudan) ? (736 - 196) : _param._z010.in(Z010.sapporo) ? 210 : width;     //フィールドの幅(ドット)
                //log.info(" " + fieldname + " width2 = " + width);
                final int charHeight = height - (_param._z010.in(Z010.hirokoudai) ? 10 : 0);
                final KNJSvfFieldModify svfobj = new KNJSvfFieldModify(width, charHeight, ystart + (_param._z010.in(Z010.hirokoudai) ? 5 : 0), minnum, maxnum);
                final float charSize = svfobj.getCharSize(classname);
                final int y = (int) svfobj.getYjiku(ln, charSize, height);
                //log.info(" y = " + y + " / height = " + height + ", ystart = " + ystart + " : " + classname + ", charHeight = " + charHeight + ", charSize = " + charSize);
                vrAttributeR(fieldname, "Y="+ y);
                vrAttributeR(fieldname, "Size=" + charSize);
            }

            private void output() {
                if (_param._isOutputDebugPrint) {
                    log.info(" end rec. (" + _name + ")");
                }
                for (final String field : _attr.keySet()) {
                    for (final String attr : _attr.get(field)) {
                        vrAttribute(field, attr);
                    }
                }
                for (final String field : _data.keySet()) {
                    final String data = _data.get(field);
                    vrsOut(field, data);
                }
                if (_param._isOutputDebugPrint) {
                    log.info(" end : " + _data + ", " + _attr);
                }
            }

            private void endrecord() {
                output();
                _svf.VrEndRecord();
            }
        }

        private class RecordPages {
            final List<List<Record>> _recordPageList = new ArrayList<List<Record>>();

            public RecordPages() {
                _recordPageList.add(new ArrayList<Record>());
            }

            public List<Record> currentPage() {
                if (_recordPageList.get(_recordPageList.size() - 1).size() >= _lineMax12) {
                    log.info(" add page " + _recordPageList.size() + " / " + (_recordPageList.get(_recordPageList.size() - 1).size()) + " / " + _lineMax12);
                    _recordPageList.add(new ArrayList<Record>());
                }
                return _recordPageList.get(_recordPageList.size() - 1);
            }

            public String status() {
                return "RecordPages(" + _recordPageList.size() + ", " + (_recordPageList.isEmpty() ? "" : String.valueOf(_recordPageList.get(_recordPageList.size() - 1).size())) + ")";
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class Studyrec {
        final String _classname;
        final String _classcd;
        final String _specialDiv;
        final String _subclassname;
        final String _subclasscd;
        final List<Grades> _gradesList;
        Studyrec(
                final String classname,
                final String classcd,
                final String specialDiv,
                final String subclassname,
                final String subclasscd
        ) {
            _classname = classname;
            _classcd = classcd;
            _specialDiv = specialDiv;
            _subclassname = subclassname;
            _subclasscd = subclasscd;
            _gradesList = new ArrayList<Grades>();
        }
        public static Studyrec sum(final List<Studyrec> studyrecList) {
            String classname = null;
            String classcd = null;
            String specialDiv = null;
            String subclassname = null;
            String subclasscd = null;
            List<Grades> gradesList = new ArrayList<Grades>();
            for (final Studyrec s : studyrecList) {
                if (null == classname) {
                    classname = s._classname;
                }
                if (null == classcd) {
                    classcd = s._classcd;
                }
                if (null == specialDiv) {
                    specialDiv = s._specialDiv;
                }
                if (null == subclassname) {
                    subclassname = s._subclassname;
                }
                if (null == subclasscd) {
                    subclasscd = s._subclasscd;
                }
                gradesList.addAll(s._gradesList);
            }
            final Studyrec sum = new Studyrec(classname, classcd, specialDiv, subclassname, subclasscd);
            sum._gradesList.addAll(gradesList);
            return sum;
        }
        public Grades getYearGrades(final String year) {
            for (final Grades g : _gradesList) {
                if (null != g && g._year.equals(year)) {
                    return g;
                }
            }
            return null;
        }
        public String yearCredit(final String year) {
            String yCre = null;
            for (final Grades g : _gradesList) {
                if (null != g && g._year.equals(year)) {
                    yCre = Util.addNumber(yCre, g._gradeCredit);
                }
            }
            return yCre;
        }
        public void addGrades(final Grades grades) {
            _gradesList.add(grades);
        }

        public String toString() {
            return "CLASSCD=" + _classcd + ", CLASSNAME=" + _classname + ", SUBCLASSCD=" + _subclasscd + ", SUBCLASSNAME=" + _subclassname + ", " + _gradesList;
        }
    }

    private static class StudyrecClass {

        final String _classcd;
        final String _classname;
        final List<Studyrec> _studyrecList;
        public StudyrecClass(final String classcd, final String classname) {
            _classcd = classcd;
            _classname = classname;
            _studyrecList = new ArrayList<Studyrec>();
        }

        private static List<StudyrecClass> groupByClassList(final List<Studyrec> studyrecList) {
            final List<StudyrecClass> rtn = new ArrayList();
            StudyrecClass current = null;
            for (final Studyrec studyrec : studyrecList) {
                if (null == current || null == studyrec._classcd || !studyrec._classcd.equals(current._classcd)) {
                    current = new StudyrecClass(studyrec._classcd, defstr(studyrec._classname));
                    rtn.add(current);
                }
                current._studyrecList.add(studyrec);
            }
            return rtn;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class Grades {
        final String _studyFlag;
        final String _year;
        final String _annualOrYear;
        final String _grades;
        final String _gradeCredit;
        final String _gradeCompCredit;
        String _credit;
        final String _d065Flg;
        Grades(
                final String studyFlag,
                final String year,
                final String annualOrYear,
                final String grades,
                final String gradeCredit,
                final String gradeCompCredit,
                final String credit,
                final String d065Flg
        ) {
            _studyFlag = studyFlag;
            _year = year;
            _annualOrYear = annualOrYear;
            _grades = grades;
            _gradeCredit = gradeCredit;
            _gradeCompCredit = gradeCompCredit;
            _credit = credit;
            _d065Flg = d065Flg;
        }
        public void addCredit(final String credit) {
            _credit = Util.addNumber(_credit, credit);
        }
        /**
         * @param val
         * @return rtnVal
         */
        public String getHyotei(final Param param) {
            final int intGrades = (int) Double.parseDouble(_grades);
            final String val = String.valueOf(intGrades);

            String rtnVal = "";
            if (null != val) {
                if (param._z010.in(Z010.ChiyodaKudan)) {
                    if (param._nameMstD001Map.containsKey(val)) {
                        rtnVal = param._nameMstD001Map.get(val);
                    } else {
                        rtnVal = val;
                    }
                } else if (null != _d065Flg) {
                    if (param._nameMstD001AbbvMap.containsKey(val)) {
                        rtnVal = param._nameMstD001AbbvMap.get(val);
                    }
                } else {
                    rtnVal = val;
                }
            }
            return rtnVal;
        }
        public String toString() {
            return "[STUDY_FLAG=" + _studyFlag + ", YEAR=" + _year + ", ANNUAL=" + _annualOrYear + ", CREDIT=" + _credit + ", GRADE_CREDIT=" + _gradeCredit + ", GRADES=" + _grades + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class Acc {
        private static final String FLAG_CHAIR_SUBCLASS = "CHAIR_SUBCLASS";
        private static final String FLAG_STUDYREC = "STUDYREC";

        static final int N = 7;
//        final int[] _credits = new int[N];
//        final int[] _compCredits = new int[N];
//        final boolean[] _hasCredits = new boolean[N];
//        final boolean[] _hasCompCredits = new boolean[N];
        final TreeMap<String, String> _credits = new TreeMap<String, String>();
        final TreeMap<String, String> _compCredits = new TreeMap<String, String>();
        final String _comment;
        String _item;
        String _subclassname;
        Acc(final String comment, final String item) {
            _comment = comment;
            _item = item;
        }
        public Acc copy() {
            final Acc rtn = new Acc(_comment, _item);
//            for (int i = 0; i < N; i++) {
//                rtn._hasCredits[i] = _hasCredits[i];
//                if (_hasCredits[i]) {
//                    rtn._credits[i] = _credits[i];
//                }
//                rtn._hasCompCredits[i] = _hasCompCredits[i];
//                if (_hasCompCredits[i]) {
//                    rtn._compCredits[i] = _compCredits[i];
//                }
//            }
            rtn._credits.putAll(_credits);
            rtn._compCredits.putAll(_compCredits);
            rtn._subclassname = _subclassname;
            return rtn;
        }
        public Acc add(final String year, final String studyFlag, final String credits) {
            return add(year, studyFlag, credits, false);
        }
        public Acc add(final String year, final String studyFlag, final String credits, final boolean addTotal) {
            Acc rtn = copy();
//            if (FLAG_CHAIR_SUBCLASS.equals(studyFlag)) {
//                rtn._compCredits[i] += credits;
//                rtn._hasCompCredits[i] = true;
//            } else {
////                final int bef = _credits[i];
//                rtn._credits[i] += credits;
//                rtn._hasCredits[i] = true;
////                log.debug(" [" + _comment + "] add " + studyFlag + " credits (" + i + ", " + credits + ") :" + bef + " => " + _credits[i]);
//            }
            if (FLAG_CHAIR_SUBCLASS.equals(studyFlag)) {
                rtn._compCredits.put(year, Util.addNumber(def(rtn._compCredits.get(year), "0"), credits));
            } else {
                rtn._credits.put(year, Util.addNumber(def(rtn._credits.get(year), "0"), credits));
            }
            if (addTotal) {
//            	final String tYear = StudyrecSql.TOTAL_YEAR;
//                if (FLAG_CHAIR_SUBCLASS.equals(studyFlag)) {
//                	rtn._compCredits.put(tYear, Util.addNumber(def(rtn._compCredits.get(tYear), "0"), credits));
//                } else {
//                	rtn._credits.put(tYear, Util.addNumber(def(rtn._credits.get(tYear), "0"), credits));
//                }
            }
            return rtn;
        }
        public Acc add(final Acc acc) {
            Acc rtn = copy();
//            for (int i = 0; i < N; i++) {
//                if (acc._hasCredits[i]) {
//                    rtn = rtn.add(i, FLAG_STUDYREC, acc._credits[i]);
//                }
//                if (acc._hasCompCredits[i]) {
//                    rtn = rtn.add(i, FLAG_CHAIR_SUBCLASS, acc._compCredits[i]);
//                }
//            }
            for (final String year : acc.years()) {
                if (acc.hasCredits(year)) {
                    rtn = rtn.add(year, FLAG_STUDYREC, acc._credits.get(year));
                }
                if (acc.hasCompCredits(year)) {
                    rtn = rtn.add(year, FLAG_CHAIR_SUBCLASS, acc._compCredits.get(year));
                }
            }
            return rtn;
        }
        public static Acc negate(final Acc acc) {
            final Acc rtn = new Acc(acc._comment, acc._item);
            rtn._subclassname = acc._subclassname;
            for (final String year : acc.years()) {
                if (acc.hasCredits(year)) {
                    rtn._credits.put(year, Util.subtractNumber("0", acc._credits.get(year)));
                }
                if (acc.hasCompCredits(year)) {
                    rtn._compCredits.put(year, Util.subtractNumber("0", acc._compCredits.get(year)));
                }
            }
            return rtn;
        }
        public Set<String> years() {
            final Set<String> rtn = new TreeSet<String>();
            rtn.addAll(_credits.keySet());
            rtn.addAll(_compCredits.keySet());
            return rtn;
        }
        public boolean hasCredits(final String year) {
            return _credits.containsKey(year);
        }
        public boolean hasCompCredits(final String year) {
            return _compCredits.containsKey(year);
        }
        public boolean hasAnyCredits() {
            return !_credits.isEmpty();
        }
        public boolean hasAnyCompCredits() {
            return !_compCredits.isEmpty();
        }
        public static String sum(final Map<String, String> map) {
            String sum = null;
            for (final Map.Entry<String, String> e : map.entrySet()) {
                final String year = e.getKey();
                if (StudyrecSql.TOTAL_YEAR.equals(year)) {
                    continue;
                }
                final String credits = e.getValue();
                sum = Util.addNumber(sum, credits);
            }
            return sum;
        }
        public String sumCredits() {
            return sum(_credits);
        }
        public String sumCompCredits() {
            return sum(_compCredits);
        }
        public String toString() {
            return "Acc(" + _item + ", " + ArrayUtils.toString(_credits) + (null == _subclassname ? "" : ", " + _subclassname) + ")";
        }
    }

    protected static class Title {
        final String _year;
        final String _annual;
        final Integer _position;
        final String _nendo;
        final String _dropFlg;
        public Title(final String year, final String annual, final Integer position, final String nendo, final String dropFlg) {
            _year = year;
            _annual = annual;
            _position = position;
            _nendo = nendo;
            _dropFlg = dropFlg;
        }
        public String toString() {
            return "Title( year = " + _year + ", annual = " + _annual + ", pos = " + _position + ", nendo = " + _nendo + ", drop = " + _dropFlg + ")";
        }
    }

    protected static class KNJSvfFieldModify {

        private final int _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        public KNJSvfFieldModify(final int width, final int height, final int ystart, final int minnum, final int maxnum) {
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2;
            return offset;
        }

        private int getStringLengthPixel(final float charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public float getCharSize(final String str) {
            final int num = Math.min(Math.max(KNJ_EditEdit.getMS932ByteLength(str), _minnum), _maxnum);
            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, num));  //文字サイズ
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static int charSizeToPixel(final double charSize) {
            return (int) Math.round(charSize / 72 * 400);
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final int pixel) {
            return pixel / 400.0 * 72;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public float getYjiku(final int hnum, final float charSize, final int recordHeight) {
            float jiku = retFieldY(_height, charSize) + _ystart + recordHeight * hnum;  //出力位置＋Ｙ軸の移動幅
            return jiku;
        }

        /**
         *  文字サイズを設定
         */
        private static float retFieldPoint(final int width, final int num) {
            return (float) Math.round((float) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static float retFieldY(final int height, final float charSize) {
            return (float) Math.round(((double) height - (charSize / 72 * 400)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    protected static class PrintData {
        final boolean _isPrintGrd;
        final String _year;
        final String _semester;
        final String _date;
        final String _schregno;
        final Map _paramap;
        final String _staffCd;
        final int _certifKindcdInt;
        final String _kanji;
        final String _certifNumber;
        final String _certifKind;
        final String _ctrlYear;
        final boolean _isPrintStamp;
        final String _lastLineClasscd;
        public boolean _isTsushin;   // 通信制:Z000.NAMESPARE3='1'
        public String _Knje080UseAForm;
        final String _certifSchoolOnly;

        String _nendo;
        KNJSchoolMst _knjSchoolMst;
        public TreeMap<String, Title> _yearTitleMap = new TreeMap<String, Title>();  // 学年（年度）出力列
        public String _notUseClassMstSpecialDiv; // 教科マスタの専門区分を使用しない
        private String _syoshoname;
        private String _syoshoname2;
        private boolean _isOutputCertifNo;
        private Collection<String> _gakunenseiTengakuYears = Collections.emptyList();
        private Map _outputFuhakkouResult;
        private boolean _isFuhakkou;
        private boolean _isPrintChairSubclass;
        String _entDate;
        String _curriculumYear;
        String _entYearGradeCd;
        String _regdSchoolKind;
        String _regdGradeCd;
        public String _majorYdatSchooldiv;
        public String _certifSchoolstampImagePath;
        public Map<String, String> _personalInfoMap = new HashMap<String, String>();
        String _dateStr;
        public Map<String, String> _schoolInfoMap = new HashMap<String, String>();
        public List<Studyrec> _studyrecList = new ArrayList<Studyrec>();
        public List<Map<String, String>> _attendList = new ArrayList<Map<String, String>>();
        public List<Map<String, String>> _remarkList = new ArrayList<Map<String, String>>();
        private List<String> _offdaysYears = Collections.emptyList();
        String _d015Namespare1;

        Acc _accGoukei    = new Acc("total", "合　　　計"); //合計の単位数&存在フラグ
        Acc _accShokei = new Acc("subtotal", "小　　　計");
        Acc _accAbroad   = new Acc(StudyrecSql.abroad, "留　　　学");
        Acc _accZenseki  = new Acc(StudyrecSql.zenseki, "前籍校における修得単位");
        Acc _accDaiken   = new Acc(StudyrecSql.daiken, "高認等における認定単位");
        Acc _accTokiwahr = new Acc(StudyrecSql.tokiwahr, "");
        Acc _accKyoto88 = new Acc(StudyrecSql.kyoto88, "");
        Acc _accSogo;

        List<Studyrec> studyrecLastLineClassList;
        List<Studyrec> _inList;

        public PrintData(
                final boolean isPrintGrd,
                final String year,
                final String semester,
                final String date,
                final String schregno,
                final Map paramap,
                final String staffCd,
                final int certifKindcdInt,
                final String kanji,
                final String certifNumber,
                final Param param) {
            _isPrintGrd = isPrintGrd;
            _year = year;
            _semester = semester;
            _date = date;
            _schregno = schregno;
            _paramap = paramap;
            _staffCd = staffCd;
            _certifKindcdInt = certifKindcdInt;
            _kanji = kanji;
            _certifNumber = certifNumber;
            _certifKind = parameter("CERTIFKIND");
            _ctrlYear = parameter("CTRL_YEAR");
            _isPrintStamp = "1".equals(parameter("PRINT_STAMP")) || param._z010.in(Z010.osakatoin) || param._z010.in(Z010.sakae) || "1".equals(param.property("KNJE080_PRINT_STAMP"));

            _Knje080UseAForm = param.property("Knje080UseAForm");
            if (_isPrintStamp) {
                final List<String> exts = new ArrayList<String>();
                exts.add(".bmp");
                if (param._z010.in(Z010.osakatoin) || param._z010.in(Z010.sakae) || "1".equals(param.property("KNJE080_PRINT_STAMP"))) {
                } else {
                    exts.add(".jpg");
                }
                for (final String ext : exts) {
                    _certifSchoolstampImagePath = param.getImageFilePath("CERTIF_SCHOOLSTAMP_H" + ext);
                    if (null == _certifSchoolstampImagePath) {
                        _certifSchoolstampImagePath = param.getImageFilePath("SCHOOLSTAMP_H" + ext);
                    }
                    if (null != _certifSchoolstampImagePath) {
                        break;
                    }
                }
            }
            if (param._z010.in(Z010.nishiyama) || param._z010.in(Z010.higashiosaka)) {
                _lastLineClasscd = "94";
            } else if (param._z010.in(Z010.rakunan)) {
                _lastLineClasscd = "95";
            } else {
                _lastLineClasscd = param.property("seisekishoumeishoCreditOnlyClasscd");
            }
            if (null != _lastLineClasscd) {
                log.info(" lastLineClasscd = " + _lastLineClasscd);
            }
            _certifSchoolOnly = parameter("certifSchoolOnly");
        }

        public String parameter(final String name) {
            return (String) _paramap.get(name);
        }

        public void load(final DB2UDB db2, final Param param) {
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _offdaysYears = getOffdaysYears(db2, param);

            if ("1".equals(param._useGakkaSchoolDiv)) {
                setMajorYdatSchooldiv(db2);
            }
//            _annual = servletpack.KNJA.detail.KNJ_GradeRecSql.max_grade(db2, _year, _schregno);
            setNotUseClassMstSpecialDiv(db2, param);
            setSchoolKind(db2, param);

            final String sogoItem;
            if (param._z010.in(Z010.meiji)) {
                sogoItem = "Catholic Spirit";
            } else {
                sogoItem = getSogoSubclassname(param);
            }
            _accSogo = new Acc(StudyrecSql.sogo, sogoItem);

            final Map knjSchoolMstParamMap = new HashMap();
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                knjSchoolMstParamMap.put("SCHOOL_KIND", _regdSchoolKind);
            }
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year, knjSchoolMstParamMap);
            } catch (final Exception e) {
                log.error("exception!!", e);
            }
            setGakunenseiTengakuYears(db2, param);
            setGradeTitle(db2, param);
            setPersonalInfo(db2, param);
            setSchoolInfo(db2, param);
            setAttend(db2, param);
            setRemark(db2, param);
            setStudyrecList(db2, param);
            _d015Namespare1 = getD015Namespare1(db2, _year, param);

            updateAcc(param);
        }

        // D015に設定された名称予備1
        private String getD015Namespare1(final DB2UDB db2, final String year, final Param param) {
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE NAMECD1 = 'D015' AND YEAR = '" + year + "' ORDER BY NAMECD2 "));
            if (param._isOutputDebug) {
                log.info(" D015 " + year + " = " + rtn);
            }
            return rtn;
        }

        private void setPersonalInfo(final DB2UDB db2, final Param param) {
            final String psKey = "PS_PERSONAL";
            if (null == param.getPs(psKey)) {
                // 個人データ
                final StringBuffer personalInfoSqlFlg = new StringBuffer();
                personalInfoSqlFlg.append("1"); // 0 graduate
                personalInfoSqlFlg.append("1"); // 1 enter
                personalInfoSqlFlg.append("1"); // 2 course
                personalInfoSqlFlg.append("1"); // 3 address
                personalInfoSqlFlg.append("0"); // 4 finschool
                personalInfoSqlFlg.append("0"); // 5 guardian
                personalInfoSqlFlg.append("1"); // 6 semes
                personalInfoSqlFlg.append("1"); // 7 english
                personalInfoSqlFlg.append("1"); // 8 realname
                personalInfoSqlFlg.append("0"); // 9 dorm
                personalInfoSqlFlg.append("0"); // 10 gradeCd
                personalInfoSqlFlg.append(param._hasMAJOR_MST_MAJORNAME2 ? "1" : "0"); // 11 majorname2
                personalInfoSqlFlg.append("0"); // 12
                personalInfoSqlFlg.append("0"); // 13
                personalInfoSqlFlg.append(param._hasCOURSECODE_MST_COURSECODEABBV1 ? "1" : "0"); // 14 coursecodeabbv1

                final Map personalInfoSqlParamMap = new HashMap();
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    personalInfoSqlParamMap.put("SCHOOL_MST_SCHOOL_KIND", "H");
                }
                personalInfoSqlParamMap.put("PRINT_GRD", param._paramap.get("PRINT_GRD"));
                param.sqlPersonalinfo = new KNJ_PersonalinfoSql().sql_info_reg(personalInfoSqlFlg.toString(), personalInfoSqlParamMap);
                param.setPs(db2, psKey, param.sqlPersonalinfo);
            }
            _personalInfoMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _semester, _schregno, _year}));

            if (param._isOutputDebug) {
                log.info(Util.debugMapToStr(" personalinfo = ", _personalInfoMap));
            }
        }

        private void setSchoolInfo(final DB2UDB db2, final Param _param) {

            //過卒生対応年度取得->掲載日より年度を算出
            if (isEng()) {
                _dateStr = defstr(Util.h_format_US(_date, d_MMMM_yyyy));
            } else {
                _dateStr = defstr(Util.getDateStr(db2, _param, _date), "　　年　 月　 日");
            }

            final String psKey = "PS_SCHOOLINFO";
            final String year2 = (_date != null) ? servletpack.KNJG.KNJG010_1.b_year(_date) : _ctrlYear;
            if (!_param._z010.in(Z010.Kindai)) {
                if (null == _param.getPs(psKey)) {
                    final Map<String, String> schoolinfoParamMap = new HashMap();
                    if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
                        schoolinfoParamMap.put("schoolMstSchoolKind", "H");
                    }
                    final String sql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12000").pre_sql(schoolinfoParamMap);
                    _param.setPs(db2, psKey, sql);
                }

                final String[] param = new String[5];
                param[0] = year2;  //対象年度
                param[1] = _certifKind;   //証明書種別
                param[2] = _certifKind;   //証明書種別
                param[3] = _year;    //現年度
                param[4] = _staffCd;
                _schoolInfoMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKey), param));

                if (_param._isOutputDebugQuery) {
                    log.info(" schoolinfo parameter = " + ArrayUtils.toString(param));
                    log.info(Util.debugMapToStr("schoolinfo", _schoolInfoMap));
                }
            } else {
                if (null == _param.getPs(psKey)) {
                    final Map dummy = new HashMap();
                    final String sql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12100").pre_sql(dummy);
                    _param.setPs(db2, psKey, sql);
                }

                final String[] param = new String[4];
                param[0] = parameter("CTRL_YEAR");
                param[1] = year2;
                param[2] = _staffCd;
                param[3] = _year;
                _schoolInfoMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKey), param));
            }
        }

        private void setAttend(final DB2UDB db2, final Param param) {
            if (_isFuhakkou) {
                // 表示しない
                return;
            }
            final String psKey = "PS_ATTEND";
            if (null == param.getPs(psKey)) {
                //  出欠記録データ
                final String tname1;
                if (_isPrintGrd) {
                    tname1 = "GRD_ATTENDREC_DAT";
                } else {
                    tname1 = "SCHREG_ATTENDREC_DAT";
                }
                //  出欠記録データ
                final StringBuffer stb = new StringBuffer();
                stb.append( "SELECT ");
                stb.append(      "T1.YEAR,");
                stb.append(      "VALUE(CLASSDAYS,0) AS CLASSDAYS,");                            //授業日数
                stb.append(      "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                if (param._z010.in(Z010.Kindai)) {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
                } else {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
                }
                stb.append(         "END AS ATTEND_1,"); //授業日数-休学日数:1
                stb.append(    "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,");          //出停・忌引
                stb.append(    "VALUE(SUSPEND,0) AS SUSPEND,");                                //出停:2
                stb.append(    "VALUE(MOURNING,0) AS MOURNING,");                              //忌引:3
                stb.append(    "VALUE(ABROAD,0) AS ABROAD,");                                  //留学:4
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append(         "END AS REQUIREPRESENT,"); //要出席日数:5
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append(         "END AS ATTEND_6,"); //病欠＋事故欠（届・無）:6
                stb.append(    "VALUE(PRESENT,0) AS PRESENT,");                                //出席日数:7
                stb.append(    "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 ");           //忌引＋出停:8
                stb.append("FROM ");
                stb.append(    "(");
                stb.append(        "SELECT ");
                stb.append(            "SCHREGNO,");
                stb.append(            "YEAR,");
                stb.append(            "SUM(CLASSDAYS) AS CLASSDAYS,");
                stb.append(            "SUM(OFFDAYS) AS OFFDAYS,");
                stb.append(            "SUM(ABSENT) AS ABSENT,");
                stb.append(            "SUM(SUSPEND) AS SUSPEND,");
                stb.append(            "SUM(MOURNING) AS MOURNING,");
                stb.append(            "SUM(ABROAD) AS ABROAD,");
                stb.append(            "SUM(REQUIREPRESENT) AS REQUIREPRESENT,");
                stb.append(            "SUM(SICK) AS SICK,");
                stb.append(            "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,");
                stb.append(            "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,");
                stb.append(            "SUM(PRESENT) AS PRESENT ");
                stb.append(       " FROM ");
                stb.append(            tname1);
                stb.append(       " WHERE ");
                stb.append(                "SCHREGNO = ? ");
                stb.append(            "AND YEAR <= ? ");
//	        		if ("on".equals(notPrintAnotherAttendrec)) {
//	        		    stb.append(        "AND SCHOOLCD <> '1' ");
//	        		}
                stb.append(        "GROUP BY ");
                stb.append(            "SCHREGNO,");
                stb.append(            "YEAR ");
                stb.append(    ")T1 ");
                stb.append(    "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    stb.append(    "    AND S1.SCHOOL_KIND = 'H' ");
                }
                stb.append(" WHERE T1.YEAR NOT IN (SELECT T1.YEAR FROM SCHREG_REGD_DAT T1 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND <> 'H' WHERE T1.SCHREGNO = ?) ");
                stb.append("ORDER BY ");
                stb.append(    "T1.YEAR");
                param.sqlAttend = stb.toString();
                param.setPs(db2, psKey, param.sqlAttend);
            }
            _attendList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _schregno});

        }

        private void setRemark(final DB2UDB db2, final Param param) {
            final String psKey = "PS_REMARK";
            if (null == param.getPs(psKey)) {
                //  所見データ
                String tabname;
                if (_isPrintGrd) {
                    tabname = "GRD_HEXAM_ENTREMARK_DAT";
                } else {
                    tabname = "HEXAM_ENTREMARK_DAT";
                }

                String sql = "SELECT "
                        + "T1.SCHREGNO,"
                        + "T1.YEAR,"
                        + "T1.ATTENDREC_REMARK "
                        + "FROM "
                        + " " + tabname + " T1 "
                        + "WHERE SCHREGNO = ? "
                        + " AND YEAR <= ? "
                        + " AND YEAR NOT IN (SELECT T1.YEAR FROM SCHREG_REGD_DAT T1 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND <> 'H' WHERE T1.SCHREGNO = ?) "

                    + "ORDER BY T1.YEAR ";
                param.sqlRemark = sql;
                param.setPs(db2, psKey, param.sqlRemark);
            }
            _remarkList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _schregno});
        }

        private void setStudyrecList(final DB2UDB db2, final Param param) {
//            final HashMap paramMap = new HashMap();
            final int gradeCdInt = NumberUtils.isDigits(_regdGradeCd) ? Integer.parseInt(_regdGradeCd) : -1;
            if (param._z010.in(Z010.tokiwa) && (isTengakuTaigaku(db2) || 1 == gradeCdInt || 2 == gradeCdInt) && thisYearStudyrecDatIsEmpty(db2, param)) {
                _isPrintChairSubclass = true; // 設定した際の単位数カッコ付き表示は現在賢者フォームは未対応
            }

//            log.debug(" paramMap = " + paramMap);

            // 学習記録データ
            //final KNJ_StudyrecSql k = new KNJ_StudyrecSql("off", "off", 2, false, param._isHosei, param._isNotPrintMirishu, param._useCurriculumcd);
            final StudyrecSql k = new StudyrecSql(param);
            if (_isPrintGrd) {
                k.tableSchregStudyrecDat = "GRD_STUDYREC_DAT";
                k.tableSchregTransferDat = "GRD_TRANSFER_DAT";
                k.tableSchregRegdDat = "GRD_REGD_DAT";
            } else {
                k.tableSchregStudyrecDat = "SCHREG_STUDYREC_DAT";
                k.tableSchregTransferDat = "SCHREG_TRANSFER_DAT";
                k.tableSchregRegdDat = "SCHREG_REGD_DAT";
            }

            final List<Map<String, String>> studyrecRowList = new ArrayList<Map<String, String>>();
            _inList = new ArrayList<Studyrec>();

            if ("1".equals(_certifSchoolOnly)) {
                // 成績表示なし
            } else {
                {
                    final String sql = k.getStudyrecSql(this, 1);
                    if (param._isOutputDebugQuery) {
                        log.info(" studyrec 1 sql = " + sql);
                    }
                    studyrecRowList.addAll(KnjDbUtils.query(db2, sql));
                }

                final List<Studyrec> abroadStudyrecList = new ArrayList<Studyrec>();
                {
                    final String sqlAbroad = k.getAbroadSql(this);
                    if (param._isOutputDebugQuery) {
                        log.info(" abroad sql = " + sqlAbroad);
                    }
                    final List<Map<String, String>> abroadRowList = KnjDbUtils.query(db2, sqlAbroad);
                    if (param._setSogakuKoteiTanni) {
                        Map<String, String> before = null;
                        for (final Iterator<Map<String, String>> it = abroadRowList.iterator(); it.hasNext();) {
                            final Map<String, String> row = it.next();
                            final String transferSdate = KnjDbUtils.getString(row, "TRANSFER_SDATE");
                            if (null != transferSdate && null != before) {
                                final String beforeTransferEdate = KnjDbUtils.getString(before, "TRANSFER_EDATE");
                                if (null != beforeTransferEdate) {
                                    if (Util.isNextDate(beforeTransferEdate, transferSdate)) {
                                         // 前のレコードに統合。年度は前のレコードの値
                                        before.put("TRANSFER_EDATE", KnjDbUtils.getString(row, "TRANSFER_EDATE"));
                                        before.put("CREDIT", Util.addNumber(KnjDbUtils.getString(before, "CREDIT"), KnjDbUtils.getString(row, "CREDIT")));
                                        it.remove();
                                        continue;
                                    }
                                }
                            }
                            before = row;
                        }
                    }
                    abroadStudyrecList.addAll(studyrecRowListToStudyrecList(1, null, param, abroadRowList));
                    _inList.addAll(abroadStudyrecList);
                }

                {
                    final String sql2 = k.getStudyrecSql(this, 2);
                    if (param._isOutputDebugQuery) {
                        log.info(" studyrec 2 sql = " + sql2);
                    }
                    final Studyrec abroadStudyrec = abroadStudyrecList.size() > 0 ? abroadStudyrecList.get(0) : null;
                    _inList.addAll(studyrecRowListToStudyrecList(1, abroadStudyrec, param, KnjDbUtils.query(db2, sql2)));
                }

                if (_isPrintChairSubclass) {
                    final String chairSql = k.getChairSql(this, 1);
                    if (param._isOutputDebug) {
                        log.info(" chair sql = " + chairSql);
                    }
                    studyrecRowList.addAll(KnjDbUtils.query(db2, chairSql));

                    final String chairInListSql = k.getChairSql(this, 2);
                    if (param._isOutputDebug) {
                        log.info(" chair 2 sql = " + chairInListSql);
                    }
                    _inList.addAll(studyrecRowListToStudyrecList(0, null, param, KnjDbUtils.query(db2, chairInListSql)));
                }
            }

            Collections.sort(studyrecRowList, k);

            _studyrecList = studyrecRowListToStudyrecList(0, null, param, studyrecRowList);

            studyrecLastLineClassList = new ArrayList<Studyrec>();
            for (final Iterator<Studyrec> its = _studyrecList.iterator(); its.hasNext();) {
                final Studyrec studyrec = its.next();
                if (null != _lastLineClasscd && _lastLineClasscd.equals(studyrec._classcd)) {
                    studyrecLastLineClassList.add(studyrec);
                    if (param._isOutputDebug) {
                        log.info(" lastLineClass = " + studyrec);
                    }
                    its.remove();
                    continue;
                }
//                log.debug(studyrec.toString());

//                //中高一貫で選択した学年が高校（4・5・6学年）のときは、高校のデータを出力する。
//                if (_param._isJuniorHiSchool && _param._isHiSchool) {
//                    boolean continueFlg = true;
//                    for (final Iterator itg = studyrec._gradesList.iterator(); itg.hasNext() && continueFlg;) {
//                        final Grades g = (Grades) itg.next();
//                        final int annualInt = Integer.parseInt(g._annualOrYear);
//                        if (!(1 <= annualInt && annualInt <= 3)) {
//                            continueFlg = false;
//                        }
//                    }
//                    if (continueFlg) {
//                    	if (_param._isOutputDebug) {
//                    		log.info(" not target : " + studyrec);
//                    	}
//                        its.remove();
//                        continue;
//                    }
//                }

                final boolean isInList = (StudyrecSql.daiken.equals(studyrec._classname) ||
                        StudyrecSql.zenseki.equals(studyrec._classname) ||
                        StudyrecSql.shokei.equals(studyrec._classname) || // 小計
                        StudyrecSql.sogo.equals(studyrec._classname) ||
                        StudyrecSql.tokiwahr.equals(studyrec._classname) ||
                        StudyrecSql.kyoto88.equals(studyrec._classname));

                if (isInList) {
                    log.warn(" *** ここへはこないはず: " + studyrec);
                    _inList.add(studyrec);
                    its.remove();
                }
            }
        }

        private List<Studyrec> studyrecRowListToStudyrecList(final int flg, final Studyrec abroadStudyrec, final Param param, final List<Map<String, String>> studyrecRowList) {
            final List<Studyrec> studyrecList = new ArrayList<Studyrec>();
            for (final Map row : studyrecRowList) {

                final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String specialDiv = flg == 1 ? "0" : KnjDbUtils.getString(row, "SPECIALDIV");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                String credit = KnjDbUtils.getString(row, "CREDIT");
                String year = KnjDbUtils.getString(row, "YEAR"); // totalはnull
                final String annualOrYear = flg == 1 ? null : KnjDbUtils.getString(row, "ANNUAL"); // 学年制は年次ANNUAL、単位制は年度YEAR...
                final String grades = flg == 1 ? "0" : KnjDbUtils.getString(row, "GRADES");
                final String gradeCredit = flg == 1 ? "0" : KnjDbUtils.getString(row, "GRADE_CREDIT");
                final String gradeCompCredit = flg == 1 ? null : KnjDbUtils.getString(row, "GRADE_COMP_CREDIT");
                String d065Flg = flg == 1 ? null : KnjDbUtils.getString(row, "D065FLG");

                if (null != year) {
                    final Title title = getTitle(year);
                    if (null != title && null != title._dropFlg) {
                        if (param._isOutputDebug) {
                            log.info("留年時の成績は表示しない:year = "+  year);
                        }
                        continue;
                    }
                } else {
                    year = "";
                }

                boolean koteiTanniToAbroad = false;
                if (StudyrecSql.sogo.equals(classname)) {
                    if (param._setSogakuKoteiTanni) {
                        final Title title = getTitle(year);
                        if (null != title && NumberUtils.isDigits(title._annual)) {
                            final BigDecimal koteiTanni = param._sogakuKoteiTanniMap.get(Integer.parseInt(title._annual));
                            if (null != koteiTanni) {
                                if (Util.toDouble(credit, 0) > 0) {
                                    credit = koteiTanni.toString();
                                }
                            }
                        }
                        if (null != abroadStudyrec) {
                            final Grades g = abroadStudyrec.getYearGrades(year);
                            if (null != g) {
                                koteiTanniToAbroad = true;
                                g.addCredit(credit);
                            }
                        }
                    }
                }
                if (koteiTanniToAbroad) {
                    continue;
                }
                Studyrec studyrec = getStudyrec(param, studyrecList, classcd, subclasscd);
                if (studyrec == null) {
                    studyrec = new Studyrec(classname, classcd, specialDiv, subclassname, subclasscd);
                    studyrecList.add(studyrec);
                }
                final String studyFlag = KnjDbUtils.getString(row, "STUDY_FLAG");
                studyrec.addGrades(new Grades(studyFlag, year, annualOrYear, grades, gradeCredit, gradeCompCredit, credit, d065Flg));
            }
            return studyrecList;
        }

        private Studyrec getStudyrec(final Param param, final List<Studyrec> studyrecList, final String classcd, final String subclasscd) {
            Studyrec studyrec = null;
            for (final Studyrec s : studyrecList) {
                if (param._z010.in(Z010.kyoto)) {
                    final String[] ssplit = StringUtils.split(s._subclasscd, "-");
                    final String[] ssplit1 = StringUtils.split(subclasscd, "-");

                    if (null == s._subclasscd || null != ssplit && null != ssplit1 && ssplit.length != ssplit1.length) {
                        continue;
                    }
                    final String sRawSubclasscd = ssplit[ssplit.length == 1 ? 0 : 3];
                    final String rawSubclasscd = ssplit1[ssplit1.length == 1 ? 0 : 3];
                    if (sRawSubclasscd.equals(rawSubclasscd)) {
                        studyrec = s;
                        break;
                    }
                } else {
                    if ((s._classcd == null || s._classcd.equals(classcd)) && (s._subclasscd == null || s._subclasscd.equals(subclasscd))) {
                        studyrec = s;
                        break;
                    }
                }
            }
            return studyrec;
        }

        /**
         * 指定生徒・年度の学科年度データの学校区分を得る
         */
        private void setMajorYdatSchooldiv(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("   SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + _year + "' ");
            stb.append("   GROUP BY SCHREGNO, YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T4.SCHOOLDIV ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
            stb.append(" INNER JOIN MAJOR_YDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("     AND T4.COURSECD = T1.COURSECD ");
            stb.append("     AND T4.MAJORCD = T1.MAJORCD ");
            log.debug(" majorYdatSchooldiv = " + stb.toString());

            _majorYdatSchooldiv = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "SCHOOLDIV");
            // log.debug(" schoolmst.schooldiv = " + _definecode.schooldiv + ", majorYdatSchoolDiv = " + _majorYdatSchooldiv + " -> "+  schooldiv);
        }

        /**
         * 単位制の場合、
         * 有効な[年度/学年]をメンバ変数 Map _gradeMap に追加します。<br>
         * 学習の記録欄・出欠の記録欄等の欄における[年度/学年]列名を印字するメソッドを呼んでいます。
         * @param schregno
         * @param year
         * @param ps
         */
        private void setGradeTitle(final DB2UDB db2, final Param param) {
            _yearTitleMap.clear();

            final StringBuffer stb = new StringBuffer();
            String with = "WITH";
            if (isGakunensei(param)) {
                stb.append(with);
                stb.append(" DROP_REGD AS ( ");
                stb.append("   SELECT DISTINCT T1.SCHREGNO, T1.ANNUAL, T1.YEAR ");
                stb.append("     FROM  SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN (SELECT SCHREGNO, ANNUAL, MAX(YEAR) AS YEAR ");
                stb.append("              FROM SCHREG_REGD_DAT ");
                stb.append("              WHERE  SCHREGNO = '" + _schregno + "' ");
                stb.append("                AND  YEAR <= '" + _year + "' ");
                stb.append("              GROUP BY SCHREGNO, ANNUAL) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND T2.YEAR = T1.YEAR ");
                stb.append("            AND T2.ANNUAL = T1.ANNUAL ");
                stb.append("   WHERE  T1.SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND  T1.YEAR <= '" + _year + "' ");
                stb.append("     AND  T2.SCHREGNO IS NULL ");
                stb.append(" ) ");
                with = ", ";
            }
            stb.append(with);
            stb.append(" PRINT_REGD AS ( ");
            if (isGakunensei(param)) {
                stb.append(" SELECT  REGD.ANNUAL, REGD.YEAR, CASE WHEN T2.SCHREGNO IS NOT NULL THEN '1' END AS DROP_FLG ");
                stb.append("   FROM  SCHREG_REGD_DAT REGD ");
                stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
                stb.append("       AND GDAT.GRADE = REGD.GRADE ");
                stb.append("       AND GDAT.SCHOOL_KIND = 'H' ");
                stb.append("   LEFT JOIN DROP_REGD T2 ON T2.SCHREGNO = REGD.SCHREGNO ");
                stb.append("       AND T2.YEAR = REGD.YEAR ");
                stb.append("       AND T2.ANNUAL = REGD.ANNUAL ");
                stb.append(" WHERE  REGD.SCHREGNO = '" + _schregno + "' ");
                stb.append("   AND  REGD.YEAR <= '" + _year + "' ");
            } else {
                stb.append(" SELECT  MAX(ANNUAL)AS ANNUAL, YEAR ");
                stb.append("   FROM  SCHREG_REGD_DAT ");
                stb.append(" WHERE  SCHREGNO = '" + _schregno + "' ");
                stb.append("   AND  YEAR <= '" + _year + "' ");
                stb.append(" GROUP BY YEAR ");
            }
            stb.append(" UNION ALL ");
            if (isGakunensei(param)) {
                stb.append(" SELECT  ANNUAL, YEAR, CAST(NULL AS VARCHAR(1)) AS DROP_FLG ");
                stb.append("   FROM  SCHREG_STUDYREC_DAT ");
                stb.append(" WHERE  SCHREGNO = '" + _schregno + "' ");
                stb.append("   AND  YEAR <= '" + _year + "' ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("   AND SCHOOL_KIND = 'H' ");
                }
            } else {
                stb.append(" SELECT  MAX(ANNUAL) AS ANNUAL, YEAR ");
                stb.append("   FROM  SCHREG_STUDYREC_DAT ");
                stb.append(" WHERE  SCHREGNO = '" + _schregno + "' ");
                stb.append("    AND  YEAR <= '" + _year + "' ");
                stb.append(" GROUP BY YEAR ");
            }
            stb.append(" ) ");
            if (isGakunensei(param)) {
                stb.append(" SELECT  ANNUAL, YEAR ");
                if (isGakunensei(param)) {
                    stb.append(" , MAX(DROP_FLG) AS DROP_FLG ");
                }
                stb.append(" FROM  PRINT_REGD ");
                stb.append(" GROUP BY ANNUAL, YEAR ");
            } else {
                stb.append(" SELECT  YEAR, MAX(ANNUAL) AS ANNUAL ");
                stb.append(" FROM  PRINT_REGD ");
                stb.append(" GROUP BY YEAR ");
            }
            stb.append(" ORDER BY YEAR ");
            final String sql = stb.toString();

            if (param._isOutputDebugQuery) {
                log.info(" regd sql = " + sql);
            }
            int i = 0;
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                String year = KnjDbUtils.getString(row, "YEAR");
                String dropFlg = null;
                final String nendo;
                if (0 == Integer.parseInt(year)) {
                    if (_yearTitleMap.containsKey("0")) {
                        continue;
                    }
                    year = "0";
                    nendo = "入学前年度";
                    dropFlg = null;
                } else if (isGakunensei(param)) {
                    nendo = "";
                    dropFlg = KnjDbUtils.getString(row, "DROP_FLG");
                } else {
                    nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
                    dropFlg = null;
                }
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final Integer position;
                if (null != dropFlg) {
                    position = new Integer(-1);
                } else {
                    if (isGakunensei(param)) {
                        final String gradeCd = param.getGradeCdOfGrade(year, annual);
                        if (NumberUtils.isDigits(gradeCd)) {
                            i = Integer.parseInt(gradeCd);
                        } else if (NumberUtils.isDigits(annual)) {
                            i = Integer.parseInt(annual);
                        } else {
                            i = -1;
                        }
                    } else {
                        if (param._z010.in(Z010.sapporo)) {
                            final String schoolKind = param.getSchoolKindOfGrade(year, annual);
                            if (null != schoolKind && !Param.SCHOOL_KIND.equals(schoolKind)) {
                                continue;
                            }
                        }
                        i += 1;
                    }
                    position = Integer.valueOf(i);
                }
                final Title title = new Title(year, annual, position, nendo, dropFlg);
                _yearTitleMap.put(year, title);
            }
            if (param._isOutputDebug) {
                log.info(Util.debugMapToStr("yearTitleMap = ", _yearTitleMap));
            }
        }

        public Title getTitle(final String year) {
            return _yearTitleMap.get(year);
        }

        public boolean isEng() {
            return CERTIF_KINDCD_ENG.equals(_certifKind) || CERTIF_KINDCD_ENG_MIKOMI.equals(_certifKind);
        }

        protected String getSchooldiv(final Param param) {
            final String schoolMstSchooldiv = defstr(null == _knjSchoolMst ? null : _knjSchoolMst._schoolDiv, "0");
            final String rtn;
            if ("1".equals(param._useGakkaSchoolDiv)) {
                rtn = defstr(_majorYdatSchooldiv, schoolMstSchooldiv);
            } else {
                rtn = schoolMstSchooldiv;
            }
            return rtn;
        }

        protected boolean isGakunensei(final Param param) {
            return "0".equals(getSchooldiv(param));
        }

        public void setGakunenseiTengakuYears(final DB2UDB db2, final Param param) {
            if (!isGakunensei(param)) {
                return;
            }

            final String sql = "SELECT T1.GRD_DATE FROM SCHREG_BASE_MST T1 WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.GRD_DIV = '3' AND T1.GRD_DATE IS NOT NULL ";
            final String grdDate = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));

            if (null != grdDate) {
                final Calendar cal = Util.getCalendarOfDate(grdDate);
                if (cal.get(Calendar.MONTH) == Calendar.MARCH && cal.get(Calendar.DAY_OF_MONTH) == 31) {
                } else {
                    int year = cal.get(Calendar.YEAR);
                    if (cal.get(Calendar.MONTH) <= Calendar.MARCH) {
                        year -= 1;
                    }
                    final List list = new ArrayList();
                    list.add(String.valueOf(year));
                    _gakunenseiTengakuYears = list;
                }
            }
        }

        /*
         * 教科マスタの専門区分を使用の設定
         * ・生徒の入学日付の年度が、証明書学校データのREMARK7の値（年度）以前の場合
         *  1) 成績欄データのソートに教科マスタの専門区分を使用しない。
         *  2) 成績欄に教科マスタの専門区分によるタイトルを表示しない。（名称マスタ「E015」設定に優先する。）
         *   ※証明書学校データのREMARK7の値（年度）が null の場合
         *    1) 専門区分をソートに使用する。
         *    2) タイトルの表示/非表示は名称マスタ「E015」の設定による。
         */
        private void setNotUseClassMstSpecialDiv(final DB2UDB db2, final Param param) {

            String notUseClassMstSpecialDiv = null;
            String curriculumYear = null;
            String entYearGradeCd = null;

            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH T_SCHOOL_KIND AS ( ");
            sql.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
            sql.append("     FROM SCHREG_REGD_DAT T1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            sql.append("         AND T2.GRADE = T1.GRADE ");
            sql.append("     WHERE ");
            sql.append("         T1.SCHREGNO = '" + _schregno + "' ");
            sql.append("         AND T2.YEAR = '" + _year + "' ");
            sql.append(" ), MAIN AS ( ");
            sql.append(" SELECT ");
            sql.append("     T1.SCHREGNO, ");
            sql.append("     FISCALYEAR(T1.ENT_DATE) AS ENT_YEAR, ");
            sql.append("     T1.CURRICULUM_YEAR, ");
            sql.append("     T4.REMARK7, ");
            sql.append("     CASE WHEN FISCALYEAR(T1.ENT_DATE) <= T4.REMARK7 THEN 1 ELSE 0 END AS NOT_USE_CLASS_MST_SPECIALDIV ");
            sql.append(" FROM ");
            sql.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
            sql.append("     INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("     INNER JOIN CERTIF_SCHOOL_DAT T4 ON T4.YEAR = T2.YEAR AND T4.CERTIF_KINDCD = '" + _certifKind + "' ");
            sql.append(" ) SELECT T1.* ");
            sql.append("        , T2.GRADE_CD AS ENT_YEAR_GRADE_CD  ");
            sql.append("   FROM MAIN T1 ");
            sql.append("   LEFt JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.ENT_YEAR ");
            sql.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = L1.YEAR AND T2.GRADE = L1.GRADE ");

            for (final Map<String, String> rs : KnjDbUtils.query(db2, sql.toString())) {
                notUseClassMstSpecialDiv = KnjDbUtils.getString(rs, "NOT_USE_CLASS_MST_SPECIALDIV");
                curriculumYear = KnjDbUtils.getString(rs, "CURRICULUM_YEAR");
                entYearGradeCd = KnjDbUtils.getString(rs, "ENT_YEAR_GRADE_CD");
            }
            _notUseClassMstSpecialDiv = notUseClassMstSpecialDiv;
            _curriculumYear = curriculumYear;
            _entYearGradeCd = entYearGradeCd;
            if (param._isOutputDebug) {
                log.info(" sql = " + sql.toString());
                log.info(" notUseClassMstSpecialDiv = " + _notUseClassMstSpecialDiv);
                log.info(" curriculumYear = " + _curriculumYear);
                log.info(" entYearGradeCd = " + _entYearGradeCd);
            }
        }

        /**
         * この年度の成績があるか
         */
        private boolean thisYearStudyrecDatIsEmpty(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT * FROM SCHREG_STUDYREC_DAT T1 ";
            if (param._hasSTUDYREC_PROV_FLG_DAT) {
                sql += " LEFT JOIN STUDYREC_PROV_FLG_DAT T2 ON T2.SCHOOLCD = T1.SCHOOLCD AND T2.YEAR = T1.YEAR AND T2.CLASSCD = T1.CLASSCD ";
                sql += "       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
                sql += "       AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
                sql += "       AND T2.SCHREGNO = T1.SCHREGNO ";
                sql += "       AND T2.PROV_FLG = '1' ";
            }
            sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SCHREGNO = '" + _schregno + "' ";
            if (param._hasSTUDYREC_PROV_FLG_DAT) {
                sql += " AND VALUE(T2.PROV_FLG, '') <> '1' ";
            }
            return KnjDbUtils.query(db2, sql).isEmpty();
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        private boolean isTengakuTaigaku(final DB2UDB db2) {
            boolean isTengakuTaigaku = false;
            final KNJ_TransferRecSql obj = new KNJ_TransferRecSql();
            for (final Map row : KnjDbUtils.query(db2, obj.sql_state(), new Object[] {_schregno, _schregno, _schregno, _schregno, _schregno, _year})) {
                if ("A003".equals(KnjDbUtils.getString(row, "NAMECD1"))) {
                    final int namecd2 = Integer.parseInt(KnjDbUtils.getString(row, "NAMECD2"));
                    if (namecd2 == 3) { // 転学
                        isTengakuTaigaku = null != KnjDbUtils.getString(row, "SDATE");
                    } else if (namecd2 == 2) { // 退学
                        isTengakuTaigaku = null != KnjDbUtils.getString(row, "SDATE");
                    } else if (namecd2 == 1) { // 卒業
                    }
                }
            }
            return isTengakuTaigaku;
        }

        public void setSchoolKind(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T2.SCHOOL_KIND, T2.GRADE_CD ";
            sql += " FROM SCHREG_REGD_DAT T1 ";
            sql += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SCHREGNO = '" + _schregno + "' ";

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _regdSchoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            _regdGradeCd = KnjDbUtils.getString(row, "GRADE_CD");
            if (param._isOutputDebug) {
                log.info(" regdSchoolKind = " + _regdSchoolKind + ", regdGradeCd = " + _regdGradeCd);
            }
        }

        public String getSogoSubclassname(final Param param) {
            final int tankyuStartYear = NumberUtils.isDigits(param._sogoTankyuStartYear) ? Integer.parseInt(param._sogoTankyuStartYear) : 2019;
            boolean isTankyu = false;
            if (NumberUtils.isDigits(_curriculumYear)) {
                if (tankyuStartYear <= Integer.parseInt(_curriculumYear)) {
                    isTankyu = true;
                }
                if (param._isOutputDebug) {
                    log.info(" 探究? " + isTankyu + ", curriculumYear = " + _curriculumYear);
                }
            } else {
                final int year = NumberUtils.isDigits(_year) ? Integer.parseInt(_year) : 0;
                final int gradeCdInt = NumberUtils.isDigits(_regdGradeCd) ? Integer.parseInt(_regdGradeCd) : 0;
                if (year == tankyuStartYear     && gradeCdInt <= 1
                        || year == tankyuStartYear + 1 && gradeCdInt <= 2
                        || year == tankyuStartYear + 2 && gradeCdInt <= 3
                        || year >= tankyuStartYear + 3
                        ) {
                    isTankyu = true;
                }
                if (param._isOutputDebug) {
                    log.info(" 探究? " + isTankyu + ", year = " + year + ", gradeCdInt = " + gradeCdInt);
                }
            }
            return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
        }

        private void updateAcc(final Param param) {
            for (final Studyrec studyrec : _inList) {
                for (final Grades g : studyrec._gradesList) {

                    String credit = g._credit;
                    if (isGakunensei(param)) {
                        if (null == credit && Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag) && null != g._gradeCredit) {
                            credit = g._gradeCredit;
                        }
                    }
                    if (credit != null) {
                        boolean noTotal = false;
                        if (isGakunensei(param)) {
                            if (StudyrecSql.tokiwahr.equals(studyrec._classname)) { _accTokiwahr = _accTokiwahr.add(g._year, g._studyFlag, credit); }
                        } else {
                            if (StudyrecSql.daiken.equals(studyrec._classname)) {
                                _accDaiken = _accDaiken.add(g._year, g._studyFlag, credit, true);
                                noTotal = true;
                            } if (StudyrecSql.zenseki.equals(studyrec._classname)) {
                                _accZenseki = _accZenseki.add(g._year, g._studyFlag, credit, true);
                                noTotal = true;
                            }
                        }
                        if (StudyrecSql.shokei.equals(studyrec._classname)) { _accShokei = _accShokei.add(g._year, g._studyFlag, credit); }
                        if (StudyrecSql.sogo.equals(studyrec._classname)) { _accSogo = _accSogo.add(g._year, g._studyFlag, credit); }
                        if (StudyrecSql.abroad.equals(studyrec._classname)) { _accAbroad = _accAbroad.add(g._year, g._studyFlag, credit); }
                        if (StudyrecSql.kyoto88.equals(studyrec._classname)) {
                            _accKyoto88 = _accKyoto88.add(g._year, g._studyFlag, credit);
                            _accKyoto88._subclassname = studyrec._subclassname;
                        }
                        if (!noTotal) {
                            _accGoukei = _accGoukei.add(g._year, g._studyFlag, credit);
                            if (param._isOutputDebug) {
                                log.info(" add " + studyrec._classname + ", accTotal = " + _accGoukei);
                            }
                        }
                    }
                }
            }
        }

        private List<String> getOffdaysYears(final DB2UDB db2, final Param param) {
            final String psKey = "CHECK_OFFDAYS YEARS";
            if (null == param.getPs(psKey)) {
                final String sql = " SELECT TRANSFER_SDATE, TRANSFER_EDATE, INT(FISCALYEAR(TRANSFER_SDATE)) AS YEAR FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = ? AND TRANSFERCD = '2' ORDER BY TRANSFER_SDATE ";
                if (param._isOutputDebugQuery) {
                    log.info(" offdays years sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }
            final List<String> years = new ArrayList<String>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno})) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null != year && !years.contains(year)) {
                    years.add(year);
                }
            }
            return years;
        }
    }

    /**
    *
    *  [進路情報・調査書]学習記録データSQL作成
    *
    *  KNJ_StudyrecSql...
    */
   private static class StudyrecSql implements Comparator<Map<String, String>> {

       private static String sogo = "sogo";
       private static String abroad = "abroad";
       private static String shokei = "shokei";
       private static String zenseki = "zenseki";
       private static String daiken = "daiken";
       private static String tokiwahr = "tokiwahr";
       private static String kyoto88 = "kyoto88";

       private static final String _88 = "88";
       public static String CONFIG_PRINT_GRD = "PRINT_GRD";
       public String tableSchregStudyrecDat = null;        // SCHREG_STUDYREC_DAT
       public String tableSchregTransferDat = null;        // SCHREG_TRANSFER_DAT
       public String tableSchregRegdDat = null;        // SCHREG_REGD_DAT
       private KNJDefineCode definecode = new KNJDefineCode(); //各学校における定数等設定
       private static final String TOTAL_YEAR = "99999999";

       private final Param _param;
       private PrintData _printData;

       public StudyrecSql(final Param param) {
           _param = param;
       }

       public int compare(final Map<String, String> o1, final Map<String, String> o2) {
           return rowCompare(o1, o2);
       }

       public int rowCompare(final Map<String, String> row1, final Map<String, String> row2) {
           int rtn;
              rtn = compareString("D065FLG", row1, row2);
              if (rtn != 0) { return rtn; }
              if (!"1".equals(_printData._notUseClassMstSpecialDiv)) {
                  rtn = compareString("SPECIALDIV", row1, row2);
                  if (rtn != 0) { return rtn; }
              }
              rtn = compareInt("CLASS_ORDER", row1, row2);
              if (rtn != 0) { return rtn; }
              rtn = compareString("CLASSCD", row1, row2);
              if (rtn != 0) { return rtn; }
              rtn = compareInt("SUBCLASS_ORDER", row1, row2);
              if (rtn != 0) { return rtn; }
              rtn = compareString("CLASSCD", row1, row2);
              if (rtn != 0) { return rtn; }
              if (_param._z010.in(Z010.kyoto)) {
                  rtn = compareString("RAW_SUBCLASSCD", row1, row2);
                  if (rtn != 0) { return rtn; }
              }
              rtn = compareString("SUBCLASSCD", row1, row2);
              if (rtn != 0) { return rtn; }
              rtn = compareString("YEAR", row1, row2);
              if (rtn != 0) { return rtn; }
              rtn = compareString("ANNUAL", row1, row2);
              return rtn;
           }

        private static int compareString(final String field, final Map<String, String> row1, final Map<String, String> row2) {
            final String v1 = KnjDbUtils.getString(row1, field);
               final String v2 = KnjDbUtils.getString(row2, field);
               if (null != v1 || null != v2) {
                   if (null == v1) { return -1; }
                   if (null == v2) { return  1; }
                   return v1.compareTo(v2);
               }
               return 0;
        }

        private static int compareInt(final String field, final Map<String, String> row1, final Map<String, String> row2) {
            final String v1 = KnjDbUtils.getString(row1, field);
               final String v2 = KnjDbUtils.getString(row2, field);
               if (null != v1 || null != v2) {
                   if (null == v1) { return -1; }
                   if (null == v2) { return  1; }
                   return Integer.valueOf(v1).compareTo(Integer.valueOf(v2));
               }
               return 0;
        }

        /**
         * 学習記録のSQL
         */
        public String getStudyrecSql(final PrintData printData, final int flg) {
            _printData = printData;

            String notContainTotalYears = getNotContainTotalYears(printData);

            final List<String> ryunenYearList = new ArrayList<String>();
            {
                for (final Title title : printData._yearTitleMap.values()) {
                    if (null != title && null != title._year && null != title._dropFlg) {
                        if (_param._isOutputDebug) {
                            log.info("留年時の成績は表示しない:year = " + title._year);
                        }
                        ryunenYearList.add(title._year);
                    }
                }
            }
            if (tableSchregStudyrecDat == null) {
                throw new IllegalStateException("not set tname.");
                // setFieldName(); //使用テーブル名設定
            }

            final String schooldiv = printData.getSchooldiv(_param);
            final boolean notUseStudyrecProvFlgDat = !_param._hasSTUDYREC_PROV_FLG_DAT;

            final StringBuffer ryunenYearSqlNotIn = new StringBuffer();
            if (null != ryunenYearList && !ryunenYearList.isEmpty()) {
                ryunenYearSqlNotIn.append(" NOT IN (");
                String comma = "";
                for (final Iterator it = ryunenYearList.iterator(); it.hasNext();) {
                    final String year = (String) it.next();
                    ryunenYearSqlNotIn.append(comma).append("'").append(year).append("'");
                    comma = ", ";
                }
                ryunenYearSqlNotIn.append(" )");
            }

            String lastLineClasscd = printData._lastLineClasscd; // getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
            boolean isSubclassContainLastLineClass = false; // 表示する行を取得
            boolean isHyoteiHeikinLastLineClass = false; // lastLineClasscd教科に評定を入力し評定平均を表示する
            boolean isTotalContainLastLineClass = false; // 'total'にlastLineClasscd教科を含める
            if (null != lastLineClasscd) {
//              isSubclassContainLastLineClass = asBoolean(paramMap, "isSubclassContainLastLineClass", true);
//              isHyoteiHeikinLastLineClass = asBoolean(paramMap, "isHyoteiHeikinLastLineClass", true);
//              isTotalContainLastLineClass = asBoolean(paramMap, "isTotalContainLastLineClass", true);
                // 面倒なのでtrue
                isSubclassContainLastLineClass = true;
                isHyoteiHeikinLastLineClass = true;
                isTotalContainLastLineClass = true;
            } else if (_param._z010.in(Z010.tokiwa)) {
                lastLineClasscd = "94";
                isSubclassContainLastLineClass = false;
                isHyoteiHeikinLastLineClass = false;
                isTotalContainLastLineClass = false;
            } else if (_param._z010.in(Z010.nishiyama)) {
                lastLineClasscd = "94";
                isSubclassContainLastLineClass = true;
                isHyoteiHeikinLastLineClass = true;
                isTotalContainLastLineClass = true;
            }

            final String year = printData._isPrintChairSubclass ? String.valueOf(Integer.parseInt(printData._year) - 1) : printData._year;
            final String schoolMstSchoolKind = _param._hasSCHOOL_MST_SCHOOL_KIND ? "H" : null;
            final StringBuffer sql = new StringBuffer();

            // 評定１を２と判定
            String h_1_2 = null;
            String h_1_3 = null;
//          if( _hyoutei.equals("on") ){ //----->評定読み替えのON/OFF  評定１を２と読み替え
//              h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
//              h_1_3 = "T1.CREDIT ";  //NO001
//              //NO001 h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END ";
//          } else{
            h_1_2 = "T1.GRADES ";
            h_1_3 = "T1.CREDIT ";
//          }

            // 該当生徒の成績データ表
            if (_param._z010.in(Z010.Kindai)) {
                //近大付属は評価読替元科目はココで除外する
                  sql.append("WITH STUDYREC AS(");
                  sql.append("SELECT  T1.CLASSNAME, ");
                  sql.append("        T1.SUBCLASSNAME, ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        T1.ANNUAL, ");
                  sql.append("        T1.CLASSCD, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
                      sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD ");
                  sql.append("       ,T1.VALUATION AS GRADES ");
                  sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
                  sql.append("       ,T1.COMP_CREDIT ");
                  sql.append("       ,NMD065.NAME1 AS D065FLG ");
                  sql.append("FROM   " + tableSchregStudyrecDat + " T1 ");
                  sql.append("        LEFT JOIN SUBCLASS_MST L2 ON ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("                L2.SUBCLASSCD = ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("               T1.SUBCLASSCD ");
                  if (notUseStudyrecProvFlgDat) {
                  } else {
                      sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                      sql.append("            AND L3.YEAR = T1.YEAR ");
                      sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                      sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
                      if ("1".equals(_param._useCurriculumcd)) {
                          sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                          sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                      }
                      sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                      sql.append("            AND L3.PROV_FLG = '1' ");
                  }
                  if (true) {
                      sql.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = ");
                      if ("1".equals(_param._useCurriculumcd)) {
                          sql.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                      }
                      sql.append("       T1.SUBCLASSCD ");
                  }
                  sql.append("WHERE   T1.SCHREGNO = '" + printData._schregno + "' ");
                  sql.append("    AND T1.YEAR <= '" + year + "' ");
                  sql.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                  sql.append("    AND NOT EXISTS(SELECT  'X' ");
                  sql.append("                   FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                  sql.append("                   WHERE   T2.YEAR = T1.YEAR AND ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
                  }
                  sql.append("                           T2.ATTEND_SUBCLASSCD = ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("                           T1.SUBCLASSCD) ");
                  if (notUseStudyrecProvFlgDat) {
                  } else {
                      sql.append("         AND L3.SUBCLASSCD IS NULL ");
                  }
                  if ("1".equals(_param._seisekishoumeishoNotPrintAnotherStudyrec)) {
                      sql.append("         AND T1.SCHOOLCD <> '1' ");
                  }
                  sql.append("    )");
              } else {
                  // 賢者
                  sql.append("WITH T_STUDYREC AS(");
                  sql.append("SELECT  T1.SCHOOLCD, ");
                  sql.append("        T1.CLASSNAME, ");
                  sql.append("        T1.SUBCLASSNAME, ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        T1.ANNUAL, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        L2.SUBCLASSCD2 AS RAW_SUBCLASSCD2, ");
                      sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("        L2.SUBCLASSCD2 AS SUBCLASSCD2 ");
                  sql.append("       ,T1.VALUATION AS GRADES ");
                  if (_param._z010.in(Z010.HOUSEI)) {
                      sql.append("   ,CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
                  } else {
                      sql.append("   ,T1.CLASSCD ");
                  }
                  sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
                  sql.append("       ,T1.COMP_CREDIT ");
                  sql.append("       ,NMD065.NAME1 AS D065FLG ");
                  sql.append("FROM   " + tableSchregStudyrecDat + " T1 ");
                  sql.append("        LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        AND L2.CLASSCD = T1.CLASSCD ");
                      sql.append("        AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                      sql.append("        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                  }
                  if (notUseStudyrecProvFlgDat) {
                  } else {
                      sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                      sql.append("            AND L3.YEAR = T1.YEAR ");
                      sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                      sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
                      if ("1".equals(_param._useCurriculumcd)) {
                          sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                          sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                      }
                      sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                      sql.append("            AND L3.PROV_FLG = '1' ");
                  }
                  sql.append("        LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
                  sql.append("WHERE   T1.SCHREGNO = '" + printData._schregno + "' AND T1.YEAR <= '" + year + "' ");
                  sql.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "' ");
                  if (null != lastLineClasscd) {
                      sql.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
                  }
                  sql.append("        ) ");
                  if (ryunenYearSqlNotIn.length() > 0) {
                      sql.append("    AND T1.YEAR " + ryunenYearSqlNotIn);
                  }
                  if (_param._isNotPrintMirishu) { // isNotPrintMirishu(_config)) {
                      sql.append("        AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
                  }
                  if (notUseStudyrecProvFlgDat) {
                  } else {
                      sql.append("         AND L3.SUBCLASSCD IS NULL ");
                  }
                  if ("1".equals(_param._seisekishoumeishoNotPrintAnotherStudyrec)) {
                      sql.append("         AND T1.SCHOOLCD <> '1' ");
                  }
                  if (null != printData._regdSchoolKind) {
                         // 指定校種以外の学年を対象外とする
                         sql.append(" AND T1.ANNUAL NOT IN (SELECT DISTINCT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND <> '" + printData._regdSchoolKind + "') ");
                  }
                  if ("1".equals(_param.property("hyoteiYomikaeRadio")) && "notPrint1".equals(printData.parameter("HYOTEI"))) {
                      sql.append("         AND (T1.VALUATION IS NULL OR T1.VALUATION <> 1) ");
                  }
                  sql.append(") , T_STUDYREC2 AS( ");
                  sql.append("    SELECT ");
                  sql.append("        T1.* ");
                  sql.append("    FROM ");
                  sql.append("        T_STUDYREC T1 ");
                  if (schooldiv.equals("1")) {
                      if (_param._daiken_div_code) {
                          sql.append(" WHERE  T1.SCHOOLCD = '0'");
                      } else {
                          sql.append(" WHERE  T1.SCHOOLCD = '0'");
                          sql.append("     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)");
                      }
                      if (null != _param._zensekiSubclassCd) {
                          sql.append("     OR ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') ");
                          if ("1".equals(_param._useCurriculumcd)) {
                              sql.append("         AND T1.RAW_SUBCLASSCD <> '" + _param._zensekiSubclassCd + "')");
                          } else {
                              sql.append("         AND T1.SUBCLASSCD <> '" + _param._zensekiSubclassCd + "')");
                          }
                      } else {
                          sql.append("     OR (T1.SCHOOLCD = '1' OR T1.YEAR = '0')");
                      }
                  }

                  sql.append(") , STUDYREC0 AS( ");
                  sql.append("    SELECT ");
                  sql.append("        T1.SCHOOLCD, ");
                  sql.append("        T1.CLASSNAME, ");
                  sql.append("        T1.SUBCLASSNAME, ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        T1.ANNUAL, ");
                  sql.append("        T1.CLASSCD , ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("        T1.RAW_SUBCLASSCD, ");
                  sql.append("        T1.SUBCLASSCD, ");
                  sql.append("        T1.GRADES, ");
                  sql.append("        T1.CREDIT, ");
                  sql.append("        T1.COMP_CREDIT, ");
                  sql.append("        T1.D065FLG ");
                  sql.append("    FROM ");
                  sql.append("        T_STUDYREC2 T1 ");
                  sql.append("    WHERE ");
                  sql.append("        T1.SUBCLASSCD2 IS NULL ");
                  sql.append("    UNION ALL ");
                  sql.append("    SELECT ");
                  sql.append("        T1.SCHOOLCD, ");
                  sql.append("        T1.CLASSNAME, ");
                  sql.append("        T1.SUBCLASSNAME, ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        T1.ANNUAL, ");
                  sql.append("        T1.CLASSCD , ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("        T1.RAW_SUBCLASSCD, ");
                  sql.append("        T1.SUBCLASSCD2 AS SUBCLASSCD, ");
                  sql.append("        T1.GRADES, ");
                  sql.append("        T1.CREDIT, ");
                  sql.append("        T1.COMP_CREDIT, ");
                  sql.append("        T1.D065FLG ");
                  sql.append("    FROM ");
                  sql.append("        T_STUDYREC2 T1 ");
                  sql.append("    WHERE ");
                  sql.append("        T1.SUBCLASSCD2 IS NOT NULL ");

                  final int _hyoteiKeisanMinGrades = "Y".equals(printData._d015Namespare1) ? 0 : 1;
                  // 同一年度同一科目の場合単位は合計とします。
                  //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
                  final String gradesCase0 = "case when " + String.valueOf(0) + " < GRADES then GRADES end";
                  final String gradesCase = "case when " + String.valueOf(_hyoteiKeisanMinGrades) + " < T1.GRADES then GRADES end";
                  final String creditCase = "case when " + String.valueOf(_hyoteiKeisanMinGrades) + " < T1.GRADES then CREDIT end";

                  sql.append(") , STUDYREC AS( ");
                  sql.append("    SELECT ");
                  sql.append("        MIN(T1.SCHOOLCD) AS SCHOOLCD, ");
                  sql.append("        MAX(T1.CLASSNAME) AS CLASSNAME, ");
                  sql.append("        MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        MAX(T1.ANNUAL) AS ANNUAL, ");
                  sql.append("        T1.CLASSCD, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("        T1.RAW_SUBCLASSCD, ");
                  sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
                  sql.append("        case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
                  sql.append("             when GVAL_CALC = '0' then ");
                  if (_hyoteiKeisanMinGrades != 0) {
                      sql.append("           CASE WHEN MAX(GRADES) <= " + String.valueOf(_hyoteiKeisanMinGrades) + " THEN MAX(" + gradesCase0 + ") ");
                      sql.append("                ELSE ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                      sql.append("           END ");
                  } else {
                      sql.append("           ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                  }
                  sql.append("             when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
                  sql.append("             when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
                  sql.append("             else MAX(T1.GRADES) ");
                  sql.append("        end AS GRADES,");
                  sql.append("        SUM(T1.CREDIT) AS CREDIT, ");
                  sql.append("        SUM(T1.COMP_CREDIT) AS COMP_CREDIT, ");
                  sql.append("        MAX(D065FLG) AS D065FLG ");
                  sql.append("    FROM ");
                  sql.append("        STUDYREC0 T1 ");
                  sql.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
                  if (null != schoolMstSchoolKind) {
                      sql.append("        AND SC.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
                  }
                  sql.append("        LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
                  sql.append("    GROUP BY ");
                  sql.append("        T1.SCHREGNO, ");
                  sql.append("        T1.YEAR, ");
                  sql.append("        T1.CLASSCD, ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("        T1.RAW_SUBCLASSCD, ");
                  sql.append("        T1.SUBCLASSCD, ");
                  sql.append("       SC.GVAL_CALC ");
                  sql.append(") ");
              }

              boolean checkDropYear = "0".equals(schooldiv);
              if (checkDropYear) {
                  sql.append(" , DROP_YEAR AS(");
                  sql.append("        SELECT DISTINCT T1.YEAR ");
                  sql.append("        FROM SCHREG_REGD_DAT T1");
                  sql.append("        WHERE T1.SCHREGNO = '" + printData._schregno + "' ");
                  sql.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
                  sql.append("                            WHERE T2.SCHREGNO = '" + printData._schregno + "' ");
                  sql.append("                              AND T2.YEAR <= '" + year + "' ");
                  sql.append("                            GROUP BY T2.GRADE)");
                  sql.append(" ) ");
              }

              final boolean useCreditMst = "1".equals(_param.property("hyoteiYomikaeRadio")) && "1".equals(printData.parameter("HYOTEI"));
              if (useCreditMst) {
                  sql.append(" , CREM_REGD AS (");
                  sql.append("        SELECT T1.SCHREGNO, T1.YEAR, MAX(T1.SEMESTER) AS SEMESTER ");
                  sql.append("        FROM SCHREG_REGD_DAT T1");
                  sql.append("        WHERE T1.SCHREGNO = '" + printData._schregno + "' ");
                  sql.append("        GROUP BY T1.SCHREGNO, T1.YEAR ");
                  sql.append(" ) ");
                  sql.append(" , CREM0 AS (");
                  sql.append("        SELECT T2.SCHREGNO, T2.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CREDITS ");
                  sql.append("        FROM CREDIT_MST T1");
                  sql.append("        INNER JOIN CREM_REGD T2 ON T2.YEAR = T1.YEAR ");
                  sql.append("        INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T2.SCHREGNO ");
                  sql.append("            AND REGD.YEAR = T2.YEAR ");
                  sql.append("            AND REGD.SEMESTER = T2.SEMESTER ");
                  sql.append("            AND REGD.COURSECD = T1.COURSECD ");
                  sql.append("            AND REGD.GRADE = T1.GRADE ");
                  sql.append("            AND REGD.MAJORCD = T1.MAJORCD ");
                  sql.append("            AND REGD.COURSECODE = T1.COURSECODE ");
                  sql.append(" ) ");
                  sql.append(" , CREM AS (");
                  sql.append("        SELECT T1.SCHREGNO, T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CREDITS ");
                  sql.append("        FROM CREM0 T1");
                  sql.append("        UNION ALL ");
                  sql.append("        SELECT T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD AS CLASSCD, T2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS ");
                  sql.append("        FROM CREM0 T1");
                  sql.append("        INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ");
                  sql.append("            AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                  sql.append("            AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                  sql.append("            AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                  sql.append("            AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                  sql.append("        WHERE ");
                  sql.append("                   (T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD) ");
                  sql.append("     NOT IN (SELECT T1.SCHREGNO, T1.YEAR, T1.CLASSCD,          T1.SCHOOL_KIND,          T1.CURRICULUM_CD,          T1.SUBCLASSCD         FROM CREM0) ");
                  sql.append("        GROUP BY ");
                  sql.append("               T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
                  sql.append(" ) ");
              }
              if (flg == 1) {

                  final String groupByColumn = "1".equals(schooldiv) ? " YEAR " : " ANNUAL ";
                  sql.append(", MAIN AS ( ");
                  //該当生徒の科目評定、修得単位及び教科評定平均
                  sql.append(" SELECT ");
                  sql.append("     T2.SHOWORDER2 as CLASS_ORDER,");
                  sql.append("     T3.SHOWORDER2 as SUBCLASS_ORDER,");
                  sql.append("     T1.YEAR,");
                  sql.append("     T1." + groupByColumn + " AS ANNUAL,");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T1.SCHOOL_KIND, ");
                      sql.append("        T1.CURRICULUM_CD, ");
                  }
                  sql.append("     T1.CLASSCD,");
                  if (printData.isEng()) {                     //----->教科名 英語/日本語
                      sql.append(" T2.CLASSNAME_ENG AS CLASSNAME,");
                  } else {
                      sql.append(" VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME,");
                  }
                  sql.append("     T1.SUBCLASSCD,");
                  sql.append("     T1.RAW_SUBCLASSCD, ");
                  if (printData.isEng()) {                     //----->科目名 英語/日本語
                      sql.append(" T3.SUBCLASSNAME_ENG AS SUBCLASSNAME,");
                  } else {
                      sql.append(" VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME,");
                  }
                  if (useCreditMst) {
                      sql.append("       CASE WHEN T1.GRADES = 1 THEN 2 ELSE T1.GRADES END AS GRADES, ");
                      sql.append("       CASE WHEN T1.GRADES = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS GRADE_CREDIT, ");
                  } else {
                      sql.append( h_1_2 + " AS GRADES,");
                      sql.append("     T1.CREDIT AS GRADE_CREDIT,");
                  }
                  sql.append("     T1.COMP_CREDIT AS GRADE_COMP_CREDIT,");
                  sql.append("     SUBCLSGRP.CREDIT, ");
                  sql.append("     T1.D065FLG ");
                  sql.append(" FROM ");
                  sql.append("     STUDYREC T1 ");
                  sql.append("     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                  }
                  sql.append("     LEFT JOIN SUBCLASS_MST T3 ON ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                  }
                  sql.append("         T3.SUBCLASSCD = T1.SUBCLASSCD ");
                  //  修得単位数の計
                  sql.append("     LEFT JOIN(SELECT ");
                  sql.append("             CLASSCD, SUBCLASSCD, SUM(" + h_1_3 + ") AS CREDIT ");
                  sql.append("         FROM ");
                  sql.append("             STUDYREC T1 ");
                  sql.append("         WHERE ");
                  sql.append("             (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                  if (null != lastLineClasscd && isSubclassContainLastLineClass) {
                      sql.append("           OR T1.CLASSCD = '" + lastLineClasscd + "'");
                  }
                  sql.append("             )");
                  sql.append("             AND YEAR NOT IN " + notContainTotalYears);
                  if (checkDropYear) {
                      sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                  }
                  sql.append("         GROUP BY ");
                  sql.append("             CLASSCD, SUBCLASSCD ");
                  sql.append("     ) SUBCLSGRP ON SUBCLSGRP.SUBCLASSCD = T1.SUBCLASSCD ");
                  if (useCreditMst) {
                      sql.append("     LEFT JOIN CREM CREM ON CREM.YEAR = T1.YEAR ");
                      sql.append("         AND CREM.SCHREGNO = T1.SCHREGNO ");
                      sql.append("         AND CREM.CLASSCD = T1.CLASSCD ");
                      sql.append("         AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                      sql.append("         AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                      sql.append("         AND CREM.CLASSCD || '-' || CREM.SCHOOL_KIND || '-' || CREM.CURRICULUM_CD || '-' || CREM.SUBCLASSCD = T1.SUBCLASSCD ");
                  }
                  sql.append(" WHERE ");
                  sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                  if (null != lastLineClasscd && isHyoteiHeikinLastLineClass) {
                      sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
                  }
                  if (_param._z010.in(Z010.kyoto)) {
                      sql.append("           AND T1.CLASSCD <> '" + _88 + "'");
                  }
                  sql.append("      )");
                  sql.append(" ) ");
                  sql.append(" SELECT");
                  sql.append("   '" + Acc.FLAG_STUDYREC + "' AS STUDY_FLAG ");
                  sql.append("  ,T1.CLASS_ORDER ");  // 表示順教科
                  sql.append("  ,T1.SUBCLASS_ORDER ");  // 表示順科目
                  sql.append("  ,T1.RAW_SUBCLASSCD ");
                  sql.append("  ,T1.YEAR");
                  sql.append("  ,T1.ANNUAL");
                  sql.append("  ,T1.CLASSCD");
                  sql.append("  ,T1.CLASSNAME");
                  sql.append("  ,T1.SUBCLASSCD");
                  sql.append("  ,T1.SUBCLASSNAME");
                  sql.append("  ,T1.GRADES");
                  sql.append("  ,T1.GRADE_CREDIT");
                  sql.append("  ,T1.GRADE_COMP_CREDIT");
                  sql.append("  ,T1.CREDIT ");
                  sql.append("  ,VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV ");
                  sql.append("  ,T1.D065FLG ");
                  sql.append(" FROM ");
                  sql.append("    MAIN T1 ");
                  sql.append("    LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                  }

              } else if (flg == 2) {
                  sql.append(", MAIN0 AS ( ");
                  sql.append(" SELECT ");
                  sql.append("        T1.CLASSNAME ");
                  sql.append("      , T1.SUBCLASSNAME ");
                  sql.append("      , T1.SCHREGNO ");
                  sql.append("      , T1.YEAR ");
                  sql.append("      , T1.ANNUAL ");
                  sql.append("      , T1.CLASSCD ");
                  if ("1".equals(_param._useCurriculumcd)) {
                      sql.append("      , T1.SCHOOL_KIND ");
                      sql.append("      , T1.CURRICULUM_CD ");
                  }
                  if (_param._z010.in(Z010.Kindai)) {
                      if ("1".equals(_param._useCurriculumcd)) {
                          sql.append("      , T1.RAW_SUBCLASSCD ");
                      }
                      sql.append("       , T1.SUBCLASSCD AS SUBCLASSCD2 ");
                  } else {
                      sql.append("       ,  T1.SCHOOLCD ");
                      sql.append("       , T1.RAW_SUBCLASSCD ");
                      sql.append("       , T1.SUBCLASSCD2 ");
                  }
                  sql.append("       , T1.SUBCLASSCD ");
                  sql.append("       , T1.COMP_CREDIT ");
                  sql.append("       , T1.D065FLG ");
                  if (useCreditMst) {
                      sql.append("       , CASE WHEN T1.GRADES = 1 THEN 2 ELSE T1.GRADES END AS GRADES ");
                      sql.append("       , CASE WHEN T1.GRADES = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS CREDIT ");
                  } else {
                      sql.append("       , T1.GRADES ");
                      sql.append("       , T1.CREDIT ");
                  }
                  sql.append(" FROM ");
                  if (_param._z010.in(Z010.Kindai)) {
                      sql.append("     STUDYREC T1 ");
                  } else {
                      sql.append("     T_STUDYREC T1 ");
                  }
                  if (useCreditMst) {
                      sql.append("     LEFT JOIN CREM CREM ON CREM.YEAR = T1.YEAR ");
                      sql.append("         AND CREM.SCHREGNO = T1.SCHREGNO ");
                      sql.append("         AND CREM.CLASSCD = T1.CLASSCD ");
                      sql.append("         AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                      sql.append("         AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                      sql.append("         AND CREM.CLASSCD || '-' || CREM.SCHOOL_KIND || '-' || CREM.CURRICULUM_CD || '-' || CREM.SUBCLASSCD = T1.SUBCLASSCD ");
                  }
                  sql.append(" WHERE ");
                  sql.append("     T1.YEAR NOT IN " + notContainTotalYears);
                  if (checkDropYear) {
                      sql.append("     AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                  }
                  sql.append(") ");

                  //  総合学習の修得単位数（学年別、合計）
                  sql.append(", MAIN AS ( ");
                  sql.append(" SELECT ");
                  sql.append("     YEAR AS YEAR,");
                  sql.append("     '" + KNJDefineCode.subject_T + "' AS CLASSCD,");
                  sql.append("     '" + sogo + "' AS CLASSNAME,");
                  sql.append("     '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD,");
                  sql.append("     '" + sogo + "' AS SUBCLASSNAME,");
                  sql.append("     SUM(CREDIT) AS CREDIT ");
                  sql.append(" FROM MAIN0 ");
                  sql.append(" WHERE ");
                  sql.append("     CLASSCD = '" + KNJDefineCode.subject_T + "' ");
                  sql.append(" GROUP BY YEAR ");

                  if (_param._z010.in(Z010.tokiwa)) {
                      // 常盤ホームルーム(教科コード94) 合計はなし
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("     YEAR,");
                      sql.append("     '" + tokiwahr + "' AS CLASSCD, ");
                      sql.append("     '" + tokiwahr + "' AS CLASSNAME,");
                      sql.append("     '" + tokiwahr + "' AS SUBCLASSCD, ");
                      sql.append("     '" + tokiwahr + "' AS SUBCLASSNAME,");
                      sql.append("     SUM(T1.CREDIT) AS CREDIT ");
                      sql.append(" FROM ");
                      sql.append("     MAIN0 T1 ");
                      sql.append(" WHERE ");
                      sql.append("     CLASSCD = '94' ");
                      sql.append(" GROUP BY YEAR ");
                  }
                  if (_param._z010.in(Z010.kyoto)) {
                      // 京都府自立活動(教科コード88)
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("     VALUE(YEAR, '" + StudyrecSql.TOTAL_YEAR + "') AS YEAR,");
                      sql.append("     '" + kyoto88 + "' AS CLASSCD, ");
                      sql.append("     '" + kyoto88 + "' AS CLASSNAME,");
                      sql.append("     '" + kyoto88 + "' AS SUBCLASSCD, ");
                      sql.append("     MAX(VALUE(T1.SUBCLASSNAME, L1.SUBCLASSORDERNAME2, L1.SUBCLASSNAME)) AS SUBCLASSNAME,");
                      sql.append("     SUM(T1.CREDIT) AS CREDIT ");
                      sql.append(" FROM ");
                      sql.append("     MAIN0 T1 ");
                      sql.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD");
                      sql.append("     AND L1.CLASSCD = T1.CLASSCD");
                      sql.append("     AND L1.SCHOOL_KIND = T1.SCHOOL_KIND");
                      sql.append("     AND L1.CURRICULUM_CD = T1.CURRICULUM_CD");
                      sql.append(" WHERE ");
                      sql.append("     T1.CLASSCD = '" + _88 + "' ");
                      sql.append(" GROUP BY GROUPING SETS ((), (YEAR))");
                  }

                  //  修得単位数
                  sql.append(" UNION ALL ");
                  sql.append(" SELECT ");
                  sql.append("     YEAR,");
                  sql.append("     '" + shokei + "' AS CLASSCD,");
                  sql.append("     '" + shokei + "' AS CLASSNAME,");
                  sql.append("     '" + shokei + "' AS SUBCLASSCD,");
                  sql.append("     '" + shokei + "' AS SUBCLASSNAME,");
                  sql.append("     SUM(" + h_1_3 + ") AS CREDIT ");
                  sql.append(" FROM MAIN0 T1 ");
                  sql.append(" WHERE ");
                  sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                  if (null != lastLineClasscd && isTotalContainLastLineClass) {
                      sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
                  }
                  sql.append("      )");
                  if (_param._z010.in(Z010.kyoto)) {
                      sql.append(" AND T1.CLASSCD <> '" + _88 + "' "); // 小計には含めない
                  }
                  sql.append(" GROUP BY YEAR");

                  // 前籍校における修得単位（レコードがある場合のみ）
                  if ("1".equals(schooldiv) && null != _param._zensekiSubclassCd) {
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("      S1.YEAR ");
                      sql.append("    , '" + zenseki + "' AS CLASSCD");
                      sql.append("    , '" + zenseki + "' AS CLASSNAME");
                      sql.append("    , '" + zenseki + "' AS SUBCLASSCD");
                      sql.append("    , '" + zenseki + "' AS SUBCLASSNAME");
                      sql.append("    , S1.CREDIT ");
                      sql.append(" FROM (");
                      sql.append("      SELECT T1.SCHREGNO, SUM(T1.CREDIT) AS CREDIT, T1.YEAR ");
                      sql.append("      FROM (");
                      sql.append("           SELECT SCHREGNO, CREDIT, YEAR ");
                      sql.append("           FROM MAIN0 ");
                      sql.append("           WHERE ((SCHOOLCD = '1' OR YEAR = '0') ");
                      if ("1".equals(_param._useCurriculumcd)) {
                          sql.append("                  AND RAW_SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                      } else {
                          sql.append("                  AND SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                      }
                      sql.append("                 )");
                      sql.append("          )T1");
                      sql.append("      GROUP BY T1.SCHREGNO, T1.YEAR ");
                      sql.append("      ) S1 ");
                  }

                  // 大検における認定単位（レコードがある場合のみ）
                  if ("1".equals(schooldiv) && _param._daiken_div_code) {
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("      S1.YEAR ");
                      sql.append("    , '" + daiken + "' AS CLASSCD");
                      sql.append("    , '" + daiken + "' AS CLASSNAME");
                      sql.append("    , '" + daiken + "' AS SUBCLASSCD");
                      sql.append("    , '" + daiken + "' AS SUBCLASSNAME");
                      sql.append("    , S1.CREDIT ");
                      sql.append(" FROM ( ");
                      sql.append("      SELECT T1.SCHREGNO, SUM(T1.CREDIT) AS CREDIT, T1.YEAR ");
                      sql.append("      FROM (");
                      sql.append("           SELECT SCHREGNO, CREDIT, YEAR ");
                      sql.append("           FROM MAIN0 ");
                      sql.append("           WHERE SCHOOLCD = '2'");
                      sql.append("      ) T1");
                      sql.append("      GROUP BY T1.SCHREGNO, T1.YEAR ");
                      sql.append("    ) S1 ");
                  }
                  sql.append(") ");
                  sql.append(" SELECT");
                  sql.append("   '" + Acc.FLAG_STUDYREC + "' AS STUDY_FLAG ");
                  sql.append("  ,T1.SUBCLASSCD AS RAW_SUBCLASSCD ");
                  sql.append("  ,T1.YEAR ");
                  sql.append("  ,T1.CLASSCD");
                  sql.append("  ,T1.CLASSNAME");
                  sql.append("  ,T1.SUBCLASSCD");
                  sql.append("  ,T1.SUBCLASSNAME");
                  sql.append("  ,T1.CREDIT ");
                  sql.append(" FROM ");
                  sql.append("    MAIN T1 ");
              }
              return sql.toString();
          }

        private String getNotContainTotalYears(final PrintData printData) {
            String notContainTotalYears = "('99999999')";
            if (!printData._gakunenseiTengakuYears.isEmpty()) {
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                for (final String year : printData._gakunenseiTengakuYears) {
                    stb.append("'").append(year).append("'");
                }
                stb.append(")");
                notContainTotalYears = stb.toString();
            }
            return notContainTotalYears;
        }

          public String getAbroadSql(final PrintData printData) {
              final String schooldiv = printData.getSchooldiv(_param);
              boolean checkDropYear = "0".equals(schooldiv);
              String notContainTotalYears = getNotContainTotalYears(printData);
              final String year = printData._isPrintChairSubclass ? String.valueOf(Integer.parseInt(printData._year) - 1)  : printData._year;


              final StringBuffer sql = new StringBuffer();
              //  留学中の修得単位数（学年別）

              sql.append(" WITH ");
              sql.append(" DROP_YEAR AS ( ");
              sql.append("        SELECT DISTINCT T1.YEAR ");
              sql.append("        FROM SCHREG_REGD_DAT T1");
              sql.append("        WHERE T1.SCHREGNO = '" + printData._schregno + "' ");
              sql.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
              sql.append("                            WHERE T2.SCHREGNO = '" + printData._schregno + "' ");
              sql.append("                              AND T2.YEAR <= '" + year + "' ");
              sql.append("                            GROUP BY T2.GRADE)");
              sql.append(" ) ");
              sql.append(" , ST1 AS ( ");
              sql.append("         SELECT ");
              sql.append("             ABROAD_CREDITS,");
              sql.append("             FISCALYEAR(TRANSFER_SDATE) AS TRANSFER_YEAR, ");
              sql.append("             TRANSFER_SDATE, ");
              sql.append("             TRANSFER_EDATE ");
              sql.append("         FROM ");
              sql.append(              tableSchregTransferDat + " ");
              sql.append("         WHERE ");
              sql.append("             SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
              sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
              if (checkDropYear) {
                  sql.append("           AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
              }
              sql.append(" ) ");
              sql.append(" , MAIN AS ( ");
              if (_param._setSogakuKoteiTanni) {
                  sql.append(" SELECT ");
                  sql.append("    ST1.TRANSFER_YEAR,");
                  sql.append("    ST1.TRANSFER_SDATE,");
                  sql.append("    ST1.TRANSFER_EDATE,");
                  sql.append("    ABROAD_CREDITS AS CREDIT ");
                  sql.append(" FROM ");
                  sql.append("         ST1 ");
                  sql.append("         INNER JOIN (SELECT ");
                  sql.append("             YEAR ");
                  sql.append("         FROM ");
                  sql.append(              tableSchregRegdDat + " ");
                  sql.append("         WHERE ");
                  sql.append("             SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + year + "' ");
                  sql.append("         GROUP BY YEAR ");
                  sql.append("         ) ST2 ON ST2.YEAR = ST1.TRANSFER_YEAR ");
                  sql.append(" WHERE ");
                  sql.append("     ST1.TRANSFER_YEAR <= '" + year + "' ");
              } else {
                  sql.append(" SELECT ");
                  sql.append("    ST1.TRANSFER_YEAR,");
                  sql.append("    MIN(ST1.TRANSFER_SDATE) AS TRANSFER_SDATE,");
                  sql.append("    MAX(ST1.TRANSFER_EDATE) AS TRANSFER_EDATE,");
                  sql.append("    SUM(ABROAD_CREDITS) AS CREDIT ");
                  sql.append(" FROM ");
                  sql.append("         ST1 ");
                  sql.append("         INNER JOIN (SELECT ");
                  sql.append("             YEAR ");
                  sql.append("         FROM ");
                  sql.append(              tableSchregRegdDat + " ");
                  sql.append("         WHERE ");
                  sql.append("             SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + year + "' ");
                  sql.append("         GROUP BY YEAR ");
                  sql.append("         ) ST2 ON ST2.YEAR = ST1.TRANSFER_YEAR ");
                  sql.append(" WHERE ");
                  sql.append("     ST1.TRANSFER_YEAR <= '" + year + "' ");
                  sql.append(" GROUP BY ST1.TRANSFER_YEAR ");
              }
              sql.append(") ");
              sql.append(" SELECT");
              sql.append("   'STUDYREC' AS STUDY_FLAG ");
              sql.append("  ,T1.TRANSFER_YEAR AS YEAR ");
              sql.append("  ,T1.TRANSFER_SDATE ");
              sql.append("  ,T1.TRANSFER_EDATE ");
              sql.append("  ,'" + abroad + "' AS CLASSCD ");
              sql.append("  ,'" + abroad + "' AS CLASSNAME ");
              sql.append("  ,'" + abroad + "' AS SUBCLASSCD ");
              sql.append("  ,'" + abroad + "' AS RAW_SUBCLASSCD ");
              sql.append("  ,'" + abroad + "' AS SUBCLASSNAME ");
              sql.append("  ,T1.CREDIT ");
              sql.append(" FROM ");
              sql.append("    MAIN T1 ");
              sql.append(" ORDER BY ");
              sql.append("    T1.TRANSFER_YEAR ");
              return sql.toString();
          }


          /**
           *  学習記録のSQL
           */
          public String getChairSql(final PrintData printData, final int flg) {

              String lastLineClasscd = printData._lastLineClasscd; // getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
              boolean isTotalContainLastLineClass = false;  // 'total'にlastLineClasscd教科を含める
              if (null != lastLineClasscd) {
                  // 面倒なのでtrue
                  isTotalContainLastLineClass = true;
              } else if (_param._z010.in(Z010.tokiwa)) {
                  lastLineClasscd = "94";
                  isTotalContainLastLineClass = false;
              } else if (_param._z010.in(Z010.nishiyama)) {
                  lastLineClasscd = "94";
                  isTotalContainLastLineClass = true;
              }

              final StringBuffer sql = new StringBuffer();

              sql.append(" WITH CHAIR_STD AS ( ");
              sql.append("     SELECT ");
              sql.append("         T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T3.ANNUAL, ");
              sql.append("         T2.CLASSCD, ");
              sql.append("         T2.SCHOOL_KIND, ");
              sql.append("         T2.CURRICULUM_CD, ");
              sql.append("         T2.SUBCLASSCD ");
              sql.append("     FROM CHAIR_STD_DAT T1 ");
              sql.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
              sql.append("         AND T2.SEMESTER = T1.SEMESTER ");
              sql.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
              sql.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T3.YEAR = T1.YEAR ");
              sql.append("         AND T3.SEMESTER = T1.SEMESTER ");
              sql.append("     WHERE ");
              sql.append("         T1.YEAR = '" + printData._year + "' ");
              sql.append("         AND T1.SCHREGNO = '" + printData._schregno + "' ");
              sql.append(" ) ");
              sql.append(" , MAX_SEMESTER_THIS_YEAR AS ( ");
              sql.append("     SELECT ");
              sql.append("         SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
              sql.append("     FROM CHAIR_STD ");
              sql.append("     GROUP BY ");
              sql.append("         SCHREGNO, YEAR ");
              sql.append(" ) ");
              sql.append(" , CREDIT_MST_CREDITS AS ( ");
              sql.append("     SELECT DISTINCT ");
              sql.append("         T1.YEAR, T1.SCHREGNO, T2.ANNUAL, ");
              sql.append("         T1.CLASSCD, ");
              sql.append("         T1.SCHOOL_KIND, ");
              sql.append("         T1.CURRICULUM_CD, ");
              sql.append("         T1.SUBCLASSCD, ");
              sql.append("         T3.CREDITS ");
              sql.append("     FROM CHAIR_STD T1 ");
              sql.append("     INNER JOIN MAX_SEMESTER_THIS_YEAR SEM ON SEM.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND SEM.YEAR = T1.YEAR ");
              sql.append("     LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T2.YEAR = T1.YEAR ");
              sql.append("         AND T2.SEMESTER = SEM.SEMESTER ");
              sql.append("     LEFT JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
              sql.append("         AND T3.COURSECD = T2.COURSECD ");
              sql.append("         AND T3.MAJORCD = T2.MAJORCD ");
              sql.append("         AND T3.GRADE = T2.GRADE ");
              sql.append("         AND T3.COURSECODE = T2.COURSECODE ");
              sql.append("         AND T3.CLASSCD = T1.CLASSCD ");
              sql.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append(" ) ");
              sql.append(" , CHAIR_STD_COMBINED AS ( ");
              sql.append("     SELECT ");
              sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
              sql.append("            T1.CLASSCD, ");
              sql.append("            T1.SCHOOL_KIND, ");
              sql.append("            T1.CURRICULUM_CD, ");
              sql.append("            T1.SUBCLASSCD, ");
              sql.append("            T5.CREDITS ");
              sql.append("     FROM CHAIR_STD T1 ");
              sql.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
              sql.append("         AND T3.COMBINED_CLASSCD = T1.CLASSCD ");
              sql.append("         AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = T1.YEAR ");
              sql.append("         AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
              sql.append("         AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T4.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
              sql.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T5.CLASSCD = T1.CLASSCD ");
              sql.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("     WHERE ");
              sql.append("         T3.COMBINED_SUBCLASSCD IS NULL ");
              sql.append("         AND T4.ATTEND_SUBCLASSCD IS NULL ");
              sql.append("     UNION ");
              sql.append("     SELECT ");
              sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
              sql.append("            T3.COMBINED_CLASSCD AS CLASSCD, ");
              sql.append("            T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
              sql.append("            T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
              sql.append("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
              sql.append("            CASE WHEN '2' = MAX(T3.CALCULATE_CREDIT_FLG) THEN SUM(T5.CREDITS) ");
              sql.append("                 ELSE MAX(T6.CREDITS) ");
              sql.append("            END AS CREDITS ");
              sql.append("     FROM CHAIR_STD T1 ");
              sql.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
              sql.append("         AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
              sql.append("         AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
              sql.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T5.CLASSCD = T3.ATTEND_CLASSCD ");
              sql.append("         AND T5.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ");
              sql.append("         AND T5.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ");
              sql.append("         AND T5.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ");
              sql.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
              sql.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T6.CLASSCD = T3.COMBINED_CLASSCD ");
              sql.append("         AND T6.SCHOOL_KIND = T3.COMBINED_SCHOOL_KIND ");
              sql.append("         AND T6.CURRICULUM_CD = T3.COMBINED_CURRICULUM_CD ");
              sql.append("         AND T6.SUBCLASSCD = T3.COMBINED_SUBCLASSCD ");
              sql.append("     GROUP BY ");
              sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
              sql.append("            T3.COMBINED_CLASSCD, ");
              sql.append("            T3.COMBINED_SCHOOL_KIND, ");
              sql.append("            T3.COMBINED_CURRICULUM_CD, ");
              sql.append("            T3.COMBINED_SUBCLASSCD ");
              sql.append(" ) ");
              sql.append(" , CHAIR_STD_SUBCLASSCD2 AS ( ");
              sql.append("     SELECT ");
              sql.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
              sql.append("         T1.CLASSCD, ");
              sql.append("         T1.SCHOOL_KIND, ");
              sql.append("         T1.CURRICULUM_CD, ");
              sql.append("         T1.SUBCLASSCD, ");
              sql.append("         T1.CREDITS ");
              sql.append("     FROM CHAIR_STD_COMBINED T1 ");
              sql.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
              sql.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("         AND T2.SUBCLASSCD2 IS NULL ");
              sql.append("     UNION ");
              sql.append("     SELECT ");
              sql.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
              sql.append("         T1.CLASSCD, ");
              sql.append("         T1.SCHOOL_KIND, ");
              sql.append("         T1.CURRICULUM_CD, ");
              sql.append("         T2.SUBCLASSCD2 AS SUBCLASSCD, ");
              sql.append("         T6.CREDITS ");
              sql.append("     FROM CHAIR_STD_COMBINED T1 ");
              sql.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
              sql.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append("         AND T2.SUBCLASSCD2 IS NOT NULL ");
              sql.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
              sql.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
              sql.append("         AND T6.CLASSCD = T1.CLASSCD ");
              sql.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("         AND T6.SUBCLASSCD = T2.SUBCLASSCD2 ");
              sql.append(" ) ");
              sql.append(" , CHAIR_STD_SUBCLASS AS (");
              sql.append(" SELECT ");
              sql.append("     T1.YEAR, T1.SCHREGNO, T1.ANNUAL ");
              sql.append("   , T1.CLASSCD ");
              sql.append("   , T1.SCHOOL_KIND ");
              sql.append("   , T1.CURRICULUM_CD ");
              sql.append("   , T1.SUBCLASSCD ");
              sql.append("   , T1.CREDITS");
              if (printData.isEng()) {
                  sql.append("   , T2.CLASSNAME_ENG AS CLASSNAME ");
                  sql.append("   , T3.SUBCLASSNAME_ENG AS SUBCLASSNAME ");
              } else {
                  sql.append("   , T2.CLASSNAME ");
                  sql.append("   , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME ");
              }
              sql.append("   , T2.SHOWORDER AS SHOWORDERCLASS"); // 表示順教科
              sql.append("   , T3.SHOWORDER AS SHOWORDERSUBCLASS"); // 表示順科目
              sql.append("   , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
              sql.append(" FROM CHAIR_STD_SUBCLASSCD2 T1 ");
              sql.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
              if ("1".equals(_param._useCurriculumcd)) {
                  sql.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
              }
              sql.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
              sql.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
              sql.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
              sql.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
              sql.append(" ) ");
              sql.append(" , CHAIR_STD_SUBCLASS_MAIN AS (");
              if (flg == 1) {
                  sql.append(" SELECT ");
                  sql.append("     YEAR, SCHREGNO, ANNUAL ");
                  sql.append("   , CLASSCD ");
                  sql.append("   , SCHOOL_KIND ");
                  sql.append("   , CURRICULUM_CD ");
                  sql.append("   , CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  AS SUBCLASSCD ");
                  sql.append("   , CREDITS");
                  sql.append("   , CLASSNAME ");
                  sql.append("   , SUBCLASSNAME ");
                  sql.append("   , SHOWORDERCLASS");
                  sql.append("   , SHOWORDERSUBCLASS");
                  sql.append("   , SPECIALDIV");
                  sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                  sql.append(" WHERE ");
                  sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                  if (!_param._z010.in(Z010.tokiwa) && null != lastLineClasscd) {
                          sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
                  }
                  sql.append("      )");
              } else if (flg == 2) {
                  sql.append(" SELECT ");
                  sql.append("     YEAR, SCHREGNO, ANNUAL ");
                  sql.append("   , '" + KNJDefineCode.subject_T + "' AS CLASSCD ");
                  sql.append("   , SCHOOL_KIND ");
                  sql.append("   , '" + KNJDefineCode.subject_T + "' AS CURRICULUM_CD ");
                  sql.append("   , '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD ");
                  sql.append("   , SUM(CREDITS) AS CREDITS ");
                  sql.append("   , '" + sogo + "' AS CLASSNAME ");
                  sql.append("   , '" + sogo + "' AS SUBCLASSNAME ");
                  sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
                  sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
                  sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
                  sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                  sql.append(" WHERE ");
                  sql.append("     CLASSCD = '" + KNJDefineCode.subject_T + "' ");
                  sql.append(" GROUP BY ");
                  sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
                  sql.append(" UNION ALL ");
                  sql.append(" SELECT ");
                  sql.append("     YEAR, SCHREGNO, ANNUAL ");
                  sql.append("   , 'ZZ' AS CLASSCD ");
                  sql.append("   , SCHOOL_KIND ");
                  sql.append("   , 'ZZZZ' AS CURRICULUM_CD ");
                  sql.append("   , 'ZZZZ' AS SUBCLASSCD ");
                  sql.append("   , SUM(CREDITS) AS CREDITS ");
                  sql.append("   , '" + shokei + "' AS CLASSNAME ");
                  sql.append("   , '" + shokei + "' AS SUBCLASSNAME ");
                  sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
                  sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
                  sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
                  sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                  sql.append(" WHERE ");
                  sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                  if (null != lastLineClasscd && isTotalContainLastLineClass) {
                      sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
                  }
                  sql.append("      )");
                  sql.append(" GROUP BY ");
                  sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
                  if (_param._z010.in(Z010.tokiwa)) {
                      // 常盤ホームルーム(教科コード94)
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("     YEAR, SCHREGNO, ANNUAL ");
                      sql.append("   , '" + tokiwahr + "' AS CLASSCD ");
                      sql.append("   , SCHOOL_KIND ");
                      sql.append("   , '" + tokiwahr + "' AS CURRICULUM_CD ");
                      sql.append("   , '" + tokiwahr + "' AS SUBCLASSCD ");
                      sql.append("   , SUM(CREDITS) AS CREDITS ");
                      sql.append("   , '" + tokiwahr + "' AS CLASSNAME ");
                      sql.append("   , '" + tokiwahr + "' AS SUBCLASSNAME ");
                      sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
                      sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
                      sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
                      sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                      sql.append(" WHERE ");
                      sql.append("     CLASSCD = '94' ");
                      sql.append(" GROUP BY ");
                      sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
                  }
                  if (_param._z010.in(Z010.kyoto)) {
                      // 京都府自立活動(教科コード88)
                      sql.append(" UNION ALL ");
                      sql.append(" SELECT ");
                      sql.append("     YEAR, SCHREGNO, ANNUAL ");
                      sql.append("   , '" + kyoto88 + "' AS CLASSCD ");
                      sql.append("   , SCHOOL_KIND ");
                      sql.append("   , '" + kyoto88 + "' AS CURRICULUM_CD ");
                      sql.append("   , '" + kyoto88 + "' AS SUBCLASSCD ");
                      sql.append("   , SUM(CREDITS) AS CREDITS ");
                      sql.append("   , '" + kyoto88 + "' AS CLASSNAME ");
                      sql.append("   , MAX(SUBCLASSNAME) AS SUBCLASSNAME ");
                      sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
                      sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
                      sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
                      sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                      sql.append(" WHERE ");
                      sql.append("     CLASSCD = '" + _88 + "' ");
                      sql.append(" GROUP BY ");
                      sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
                  }
              }
              sql.append(" ) ");
              sql.append(" SELECT");
              sql.append("   '" + Acc.FLAG_CHAIR_SUBCLASS + "' AS STUDY_FLAG ");
              sql.append("  ,T2.SHOWORDERCLASS AS CLASS_ORDER ");  // 表示順教科
              sql.append("  ,T2.SHOWORDERSUBCLASS AS SUBCLASS_ORDER ");  // 表示順科目
              sql.append("  ,T2.YEAR");
              sql.append("  ,T2.ANNUAL");
              sql.append("  ,T2.CLASSCD");
              sql.append("  ,T2.CLASSNAME");
              sql.append("  ,T2.SUBCLASSCD AS SUBCLASSCD");
              sql.append("  ,T2.SUBCLASSNAME");
              sql.append("  ,CAST(NULL AS SMALLINT) AS GRADES");
              sql.append("  ,T2.CREDITS AS GRADE_CREDIT");
              sql.append("  ,T2.CREDITS AS GRADE_COMP_CREDIT");
              sql.append("  ,CAST(NULL AS SMALLINT) AS CREDIT ");
              sql.append("  ,T2.SPECIALDIV ");
              sql.append("  ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
              sql.append(" FROM ");
              sql.append("    CHAIR_STD_SUBCLASS_MAIN T2 ");
              sql.append(" ORDER BY ");
              sql.append("   CASE WHEN D065FLG IS NOT NULL THEN 999 ELSE 0 END");
              if (!"1".equals(printData._notUseClassMstSpecialDiv)) {
                  sql.append("   ,SPECIALDIV ");
              }
              sql.append("  ,CLASS_ORDER ");
              sql.append("  ,CLASSCD ");
              sql.append("  ,SUBCLASS_ORDER ");
              if (_param._z010.in(Z010.kyoto)) {
                  sql.append("  ,RAW_SUBCLASSCD ");
              }
              sql.append("  ,SUBCLASSCD ");
              sql.append("  ,YEAR ");
              sql.append("  ,ANNUAL");

              return sql.toString();
          }
   }

   private static class Util {

       public static boolean isNextDate(final String date1, final String date2) {
           final Calendar cal1 = toCalendar(date1);
           final Calendar cal2 = toCalendar(date2);
           cal1.add(Calendar.DATE, 1);
           return cal1.equals(cal2);
       }

       public static Calendar toCalendar(final String date) {
           final Calendar cal = Calendar.getInstance();
           try {
               cal.setTime(java.sql.Date.valueOf(date));
           } catch (Exception e) {
               log.error("exception! " + date, e);
           }
           return cal;
       }

       public static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
       }

       private static BigDecimal dotToMm(final String dot) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final BigDecimal mm = new BigDecimal(dot).multiply(mmPerInch).divide(dpi, 1, BigDecimal.ROUND_HALF_UP);
            return mm;
       }

       public static String mkString(final TreeMap<String, String> map, final String comma) {
           final List<String> list = new ArrayList<String>();
           for (final Map.Entry<String, String> e : map.entrySet()) {
               if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                   continue;
               }
               list.add(e.getKey() + "=" + e.getValue());
           }
           return mkString(list, comma);
       }

       public static String debugMapToStr(final String debugText, final Map map) {
           final StringBuffer stb = new StringBuffer();
           stb.append(defstr(debugText) + " [\n");
           final List keys = new ArrayList(map.keySet());
           try {
               Collections.sort(keys);
           } catch (Exception e) {
           }
           for (int i = 0; i < keys.size(); i++) {
               final Object key = keys.get(i);
               if (key instanceof String && ((String) key).startsWith("__")) {
                   continue;
               }
               stb.append(i == 0 ? "   " : " , ").append(key).append(": ").append(map.get(key)).append("\n");
           }
           stb.append("]");
           return stb.toString();
       }

       public static String debugListToStr(final String debugText, final Collection col) {
           final StringBuffer stb = new StringBuffer();
           stb.append(defstr(debugText) + " [\n");
           final List l = new ArrayList(col);
           try {
               Collections.sort(l);
           } catch (Exception e) {
           }
           for (int i = 0; i < l.size(); i++) {
               final Object val = l.get(i);
               stb.append(i == 0 ? "   " : " , ").append(i).append(": ").append(val).append("\n");
           }
           stb.append("]");
           return stb.toString();
       }

       /**
        * 西暦に変換。
        *
        * @param  strx     : '2008/03/07' or '2008-03-07'
        * @param  pattern  : 'yyyy年M月d日生'
        * @return hdate    : '2008年3月7日生'
        */
       private static String getChristianEra(final String strx, final String pattern) {
           String hdate = null;
           try {
               SimpleDateFormat sdf = new SimpleDateFormat();
               Date dat = new Date();
               try {
                   sdf.applyPattern("yyyy-MM-dd");
                   dat = sdf.parse(strx);
               } catch  (Exception e) {
                   try {
                       sdf.applyPattern("yyyy/MM/dd");
                       dat = sdf.parse(strx);
                   } catch (Exception e2) {
                       hdate = "";
                       return hdate;
                   }
               }
               SimpleDateFormat sdfseireki = new SimpleDateFormat(pattern);
               hdate = sdfseireki.format(dat);

           } catch (Exception e3) {
               hdate = "";
           }
           return hdate;
       }

       private static Calendar getCalendarOfDate(final String date) {
           final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
           final Calendar cal = Calendar.getInstance();
           cal.setTime(sqlDate);
           return cal;
       }

       public static int toInt(final String s, final int def) {
           return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
       }

       public static double toDouble(final String s, final double def) {
           return NumberUtils.isNumber(s) ? Double.parseDouble(s) : def;
       }

       private static String getDateStr(final DB2UDB db2, final Param param, final String date) {
           if (null == date) {
               return null;
           }
           final String rtn;
           if (param._isSeireki) {
               rtn = date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
           } else {
               rtn = KNJ_EditDate.h_format_JP(db2, date);
           }
           return rtn;
       }

       /*
        *  [近大付属用様式] 校長名を編集
        *  2005/08/26
        */
       private static String editName(final String name) {
           StringBuffer stb = new StringBuffer();
           char chr[] = name.toCharArray();
           boolean boo = false;
           try {
               for (int i = 0 ; i < chr.length ; i++) {
                   if (chr[i] == (' ')  ||  chr[i] == ('　')) {
                       if (! boo  &&  0 < i) {
                           stb.append(" ");
                           boo = true;
                       }
                       continue;
                   }
                   if (0 < i) stb.append("  ");
                   stb.append(chr[i]);
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           }
           return stb.toString();
       }

       public static String addNumber(final String num1, final String num2) {
           if (!NumberUtils.isNumber(num1)) return num2;
           if (!NumberUtils.isNumber(num2)) return num1;
           return String.valueOf(new BigDecimal(num1).add(new BigDecimal(num2)));
       }

       public static String subtractNumber(final String num1, final String num2) {
           if (!NumberUtils.isNumber(num2)) return num1;
           return String.valueOf(new BigDecimal(NumberUtils.isNumber(num1) ? num1 : "0").subtract(new BigDecimal(num2)));
       }

       /**
        * 左右のスペース、全角スペースを除去した文字列を得る
        * @param s 文字列
        * @return 左右のスペース、全角スペースを除去した文字列
        */
       public static String trim(final String s) {
           if (null == s) {
               return s;
           }
           int si = 0;
           while (si < s.length() && (s.charAt(si) == ' ' || s.charAt(si) == '　')) {
               si++;
           }
           int ei = s.length() - 1;
           while (0 <= ei && (s.charAt(ei) == ' ' || s.charAt(ei) == '　')) {
               ei--;
           }
           if (si >= s.length() || ei <= 0) {
               return "";
           }
           return s.substring(si, ei + 1);
       }

       public static String formatBirthday(final DB2UDB db2, final Param param, final PrintData printData, final String date, final boolean isSeireki) {
           String birthdayStr = "";
           if (date != null) { // 生年月日
               if (printData.isEng()) {
                   birthdayStr = h_format_US(date, d_MMMM_yyyy);
               } else if (isSeireki) {
                   birthdayStr = getChristianEra(date, "yyyy年M月d日生");
               } else if (param._z010.in(Z010.sakae)) {
                   birthdayStr = KNJ_EditDate.h_format_JP(db2, date);
               } else {
                   birthdayStr = KNJ_EditDate.h_format_JP_Bth(db2, date);
               }
           }
           return birthdayStr;
       }

       public static String h_format_US(final String strx, final String format) {
           String hdate = "";
           if (strx == null) {
               return hdate;
           }
           try {
               final Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strx.replace('/', '-'));
               hdate = new SimpleDateFormat(format, new Locale("en","US")).format(date);
           } catch (Exception e3) {
               hdate = "";
           }
           return hdate;
       }

       public static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
           if (!map.containsKey(key1)) {
               map.put(key1, new HashMap<B, C>());
           }
           return map.get(key1);
       }

       public static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
           if (!map.containsKey(key1)) {
               map.put(key1, new TreeMap<B, C>());
           }
           return map.get(key1);
       }

       public static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
           if (!map.containsKey(key1)) {
               map.put(key1, new ArrayList<B>());
           }
           return map.get(key1);
       }

       public static String mkString(final List<String> list, final String comma) {
           final String last = "";
           final StringBuffer stb = new StringBuffer();
           String comma0 = "";
           String nl = "";
           for (final String s : list) {
               if (null == s || s.length() == 0) {
                   continue;
               }
               stb.append(comma0).append(s);
               comma0 = comma;
               nl = last;
           }
           return stb.append(nl).toString();
       }

       public static <T> List<List<T>> splitByCount(final List<T> list, final int splitCount) {
           final List<List<T>> rtn = new ArrayList<List<T>>();
           List<T> current = null;
           int count = 0;
           for (final T item : list) {
               if (splitCount <= count || null == current) {
                   count = 0;
                   current = new ArrayList<T>();
                   rtn.add(current);
               }
               current.add(item);
               count += 1;
           }
           return rtn;
       }
   }

   private static enum Z010 {
       Kindai("近大", "KINDAI", "KINJUNIOR"),
       HOUSEI("法政"),
       jisyukan("自修館"),
       Withus("WITHUS"),
       CHIBEN("智辯"),
       tottori("鳥取県"),
       chukyo("中京"),
       kumamoto("熊本県"),
       kyoai("共愛"),
       kyoto("京都府"),
       meiji("明治学園"),
       tokiwa("常磐大学"),
       miyagiken("宮城県"),
       sapporo("札幌開成"),
       musashinohigashi("武蔵野東"),
       nishiyama("京都西山"),
       mieken("三重県"),
       sundaikoufu("駿台甲府"),
       rakunan("洛南"),
       seijyo("成城"),
       ChiyodaKudan("千代田九段", "chiyoda"),
       fukuiken("福井県"),
       hirokoudai("広工大"),
       meikei("茗溪"),
       osakatoin("大阪桐蔭"),
       sakae("埼玉栄"),
       sanonihon("佐野日本"),
       Ritsumeikan("立命館", "Keisho", "Moriyama", "Nagaokakyo", "Ritsumeikan"),
       nagisa("広島なぎさ"),
       risshisha("立志舎"),
       higashiosaka("東大阪"),
       matsudo("専修大松戸"),
       NichidaiNikou("日大二校", "nichi-ni"),
       reitaku("麗澤"),
       KaichiIkkan("開智一貫部", "kikan"),
       KaichiSougou("開智総合部", "ksogo"),
       KaichiKoutou("開智高等部", "kkotou"),
       KaichiTushin("開智通信", "ktsushin"),
       kwansei("関西学院"),
       jyoto("福岡工業城東"),
       suito("大阪水都国際"),
       aoyama("青山学院"),
       ryukei("流通経済大学付属柏高校"),
       kenja("賢者", (String[]) null);

       final String _debug;
       final String[] _name1;
       Z010(final String debug, final String ... name1) {
           _debug = debug;
           _name1 = name1;
       }
       public static Z010 fromString(final String name1) {
           Z010 rtn = null;
           for (final Z010 v : Z010.values()) {
               if (null != v._name1 && v._name1.length == 0) {
                   if (v.name().equals(name1)) {
                       rtn = v;
                       break;
                   }
               } else {
                   if (ArrayUtils.contains(v._name1, name1)) {
                       rtn = v;
                       break;
                   }
               }
           }
           if (null == rtn) {
               rtn = kenja;
           }
           return rtn;
       }
       public boolean isKaichiSpec() {
           return this.in(KaichiIkkan, KaichiSougou, KaichiKoutou, KaichiTushin);
       }
       public boolean in(final Z010 ... z010s) {
           if (null != z010s) {
               for (final Z010 v : z010s) {
                   if (this == v) {
                       return true;
                   }
               }
           }
           return false;
       }
   }

   private enum Property {
       knje080UseSupendMourningForm
   }

   protected static class Param {
       public static final String SCHOOL_KIND = "H";
       public Map _paramap;
       public final String _z010Name1;
       public final Z010 _z010;
       public boolean _isJuniorHiSchool;
       public boolean _isHesetuKou;   // 併設校:Z010.NAMESPARE2='1'
       public boolean _isChukouIkkan;   // 併設校:Z010.NAMESPARE2='2'
       public final boolean _isSeireki; // 西暦フラグをセット。
       public final boolean _isKaichi;
       public final boolean _isKeisho;

       public final boolean _hasSchoolKindPJH;
       public boolean _isFormTori;
       public final boolean _useNewForm;
       public boolean _isOutputDebug;
       public boolean _isOutputDebugQuery;
       public boolean _isOutputDebugField;
       public boolean _isOutputDebugPrint;
       public String _documentroot;
       public Properties _prgInfoPropertiesFilePrperties;
       private Map<String, String> _dbPrgInfoProperties;
       public final String _imagepath;
       public String _certifPrintRealName;
       public boolean _isCertifPrintPreferGradeCd;
       public final boolean _knje080PrintDayOfDate;

       private String sqlAttend, sqlRemark, sqlPersonalinfo;
       private Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
       private Map<String, File> _createdfiles = new HashMap<String, File>();

       public String _useCurriculumcd;
       public String _useGakkaSchoolDiv;
       public String _useAddrField2;
       public String _seisekishoumeishoNotPrintAnotherStudyrec;
       public String _zensekiSubclassCd; // 前籍校の成績専用科目コードを設定します。名称マスター'E011'のコード'01'のレコード予備１をセットします。
       public String _seisekishoumeishoPrintCoursecodename;
       public String _sogoTankyuStartYear;
       public String _stampSizeMm;
       private String _stampPositionXmm;
       private String _stampPositionYmm;

       boolean _setSogakuKoteiTanni;
       final Map<Integer, BigDecimal> _sogakuKoteiTanniMap = new TreeMap<Integer, BigDecimal>();
       public boolean _daiken_div_code; // 高等学校卒業程度認定単位（大検）の印刷方法を設定します。名称マスター'E011'のコード'02'のレコードが'Y'の場合はtrueを以外はfalseを設定します。
       public boolean _isNotPrintMirishu = false; // trueなら未履修科目を出力しない 未履修の科目を表示するか (予備1が'Y'なら表示しない。それ以外は表示する。）
       protected Map _isNotPrintClassTitle;
       protected Map<String, Map<String, String>> _a029NameMstMap;
       protected TreeMap<String, Map<String, String>> _gradeGradeCdMap;
       protected TreeMap<String, Map<String, String>> _gradeSchoolKindMap;
       public Map<String, String> _nameMstD001Map;
       public Map<String, String> _nameMstD001AbbvMap;
       public List _d015YearList;
       final boolean _hasMAJOR_MST_MAJORNAME2;
       final boolean _hasSTUDYREC_PROV_FLG_DAT;
       final boolean _hasSCHOOL_MST_SCHOOL_KIND;
       final boolean _hasCOURSECODE_MST_COURSECODEABBV1;
       final Map _sessionCache = new HashMap();

       Param(final DB2UDB db2) {

           final Map<String, String> z010 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
           final String namespare2 = KnjDbUtils.getString(z010, "NAMESPARE2");
           _isHesetuKou = "1".equals(namespare2);
           _isChukouIkkan = "2".equals(namespare2);
           _isJuniorHiSchool = StringUtils.isNotBlank(namespare2); // 名称マスタの0長文字列対策
           _z010Name1 = KnjDbUtils.getString(z010, "NAME1");
           _dbPrgInfoProperties = getDbPrginfoProperties(db2);
           _z010 = Z010.fromString(_z010Name1);
           log.info(" z010 = " + _z010 + " (" + _z010Name1 + ")");
           _isKaichi = Arrays.asList("kmirai", "knihon", "knozomi", "kikan", "kkotou", "ksogo").contains(_z010Name1);
//           _isRitsumeikan = Arrays.asList("Keisho", "Moriyama", "Nagaokakyo").contains(_z010Name1);
           _isKeisho = "Keisho".equals(_z010Name1);
           _isFormTori = _z010.in(Z010.tottori, Z010.kyoai);
           _knje080PrintDayOfDate = _z010.in(Z010.jyoto);

           _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
           _hasSTUDYREC_PROV_FLG_DAT = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);
           _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
           _hasCOURSECODE_MST_COURSECODEABBV1 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV1");

           final List<String> schoolKindList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR IN (SELECT CTRL_YEAR FROM CONTROL_MST) "), "SCHOOL_KIND");
           _hasSchoolKindPJH = _z010.in(Z010.sundaikoufu) || schoolKindList.contains("H") && schoolKindList.contains("J") && schoolKindList.contains("P");
           _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));

           _nameMstD001Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D001' ORDER BY NAMECD2"), "NAMECD2", "NAME1");
           _nameMstD001AbbvMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' ORDER BY NAMECD2"), "NAMECD2", "ABBV1");
           _isNotPrintMirishu = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'A027' AND NAMECD2 = '" + CERTIF_KINDCD +"' ")));
           _isSeireki = !_z010.in(Z010.jyoto) && "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
           _daiken_div_code = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '02'")));
           _zensekiSubclassCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '01'"));
           setPrintClassTitle(db2);
           _a029NameMstMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'A029' "), "NAMECD2");
           _gradeGradeCdMap = getSchregRegdGdatMap(db2, "GRADE_CD");
           _gradeSchoolKindMap = getSchregRegdGdatMap(db2, "SCHOOL_KIND");
           _useNewForm = _z010.in(Z010.miyagiken, Z010.kyoto, Z010.mieken) || _hasSchoolKindPJH;
           _isCertifPrintPreferGradeCd = _isKaichi;
           final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
           _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
           _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
           _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");
           _isOutputDebugPrint = ArrayUtils.contains(outputDebug, "print");
           if (_isOutputDebug) {
               log.info(" _isSeireki = " + _isSeireki);
               log.info(" _daiken_div_code = " + _daiken_div_code);
               log.info(" _zensekiSubclassCd = " + _zensekiSubclassCd);
               log.info(" _isNotPrintClassTitle = " + _isNotPrintClassTitle);
               log.info(" _isChukoIkkan = " + _isChukouIkkan);
               log.info(" _isHesetuKou = " + _isHesetuKou);
           }
       }

       public void close() {
           for (final PreparedStatement ps : _psMap.values()) {
               DbUtils.closeQuietly(ps);
           }
           for (final File file : _createdfiles.values()) {
               log.info(" delete file : " + file.getAbsolutePath() + " = " + file.delete());
           }
       }

       public boolean isTsushin(final DB2UDB db2, final String year) {
           final Map<String, Map<String, String>> cache = Util.getMappedHashMap(_sessionCache, "Z010_MAP");
           if (null == cache.get(year)) {
               final String schoolDiv = KnjDbUtils.getString(getSchoolMst(db2, year), "SCHOOLDIV");
               final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z001' AND NAMECD2 = '" + schoolDiv + "' ";
               cache.put(year, KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql)));
           }
           final Map z001 = Util.getMappedMap(cache, year);
           return "1".equals(KnjDbUtils.getString(z001, "NAMESPARE3"));
       }

       private Map<String, String> getSchoolMst(final DB2UDB db2, final String year) {
           final Map<String, Map<String, String>> cache = Util.getMappedHashMap(_sessionCache, "SCHOOL_MST_MAP");
           if (null == cache.get(year)) {
               String sql = "SELECT * FROM SCHOOL_MST WHERE YEAR = '" + year + "' ";
               if (_hasSCHOOL_MST_SCHOOL_KIND) {
                   sql += " AND SCHOOL_KIND = '" + SCHOOL_KIND + "' ";
               }
               cache.put(year, KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql)));
           }
           return Util.getMappedMap(cache, year);
       }

       public void setDocumentroot(final String documentroot) {
           _documentroot = documentroot;
           _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
       }

       private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
           return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE080' "), "NAME", "VALUE");
       }

       public Properties loadPropertyFile(final String filename) {
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

       public String property(final Property property) {
           return property(property.name());
       }

       public String property(final String name) {
           String val = null;
           if (null != _dbPrgInfoProperties) {
               if (_dbPrgInfoProperties.containsKey(name)) {
                   val = _dbPrgInfoProperties.get(name);
                   if (_isOutputDebug) {
                       log.info("property in db: " + name + " = " + val);
                   }
                   return val;
               }
           }
           if (_paramap.containsKey(name)) {
               return (String) _paramap.get(name);
           }
           if (null != _prgInfoPropertiesFilePrperties) {
               if (_prgInfoPropertiesFilePrperties.containsKey(name)) {
                   val = _prgInfoPropertiesFilePrperties.getProperty(name);
                   if (_isOutputDebug) {
                       log.info("property in file: " + name + " = " + val);
                   }
               } else {
                   if (_isOutputDebug) {
                       log.warn("property not exists in file: " + name);
                   }
               }
           }
           return val;
       }

       public void setPs(final DB2UDB db2, final String psKey, final String sql) {
           if (_isOutputDebugQuery) {
               log.info(" " + psKey + " = " + sql);
           }
           try {
               _psMap.put(psKey, db2.prepareStatement(sql));
           } catch (Exception e) {
               log.error("exception!", e);
           }
       }

       public PreparedStatement getPs(final String psKey) {
           return _psMap.get(psKey);
       }

       public String getImageFilePath(final String filename) {
           String path = "";
           if (null != _documentroot) {
               path += _documentroot;
               if (!path.endsWith("/")) {
                   path += "/";
               }
           }
           if (null != _imagepath) {
               path += _imagepath;
               if (!path.endsWith("/")) {
                   path += "/";
               }
           }
           path += filename;
           final File file = new File(path);
           log.info(" file " + file.getPath() +" exists? = " + file.exists());
           if (!file.exists()) {
               return null;
           }
           return file.getPath();
       }

       private static boolean isNewForm(final Param param, final PrintData printData) {
           final int checkYear = 2013; // 切替年度
           boolean rtn = false;
           if (param._useNewForm && null != printData) {
               if (NumberUtils.isDigits(printData._curriculumYear)) {
                   // 教育課程年度が入力されている場合
                   if (checkYear > Integer.parseInt(printData._curriculumYear)) {
                       rtn = false;
                   } else {
                       rtn = true;
                   }
               } else if (null != nendo(printData._entDate)) {
                   final int iEntYear = nendo(printData._entDate).intValue();
                   if (checkYear > iEntYear) {
                       rtn = false;
                   } else if (checkYear <= iEntYear) {
                       if (NumberUtils.isDigits(printData._entYearGradeCd)) {
                           final int iAnnual = Integer.parseInt(printData._entYearGradeCd);
                           if ((checkYear + 0) == iEntYear && iAnnual >= 2 ||
                               (checkYear + 1) == iEntYear && iAnnual >= 3 ||
                               (checkYear + 2) == iEntYear && iAnnual >= 4) { // 転入生を考慮
                               rtn = false;
                           } else {
                               rtn = true;
                           }
                       } else {
                           rtn = true;
                       }
                   }
               }
           }
           return rtn;
       }

       public static Integer nendo(final String date) {
           if (null != date) {
               final Calendar cal = Util.getCalendarOfDate(date);
               if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                   return new Integer(cal.get((Calendar.YEAR) - 1));
               } else {
                   return new Integer(cal.get((Calendar.YEAR)));
               }
           }
           return null;
       }

       /**
        * 普通・専門の文言
        * @param div 普通・専門区分　0:普通、1:専門、2:その他
        * @return 文言
        */
       private String getSpecialDivName(final boolean isNewForm, final String div) {
           final String defaultname;
           final String namecd2;
           if ("1".equals(div)) {
               //　専門教科
               namecd2 = "2";
               defaultname = isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する各教科・科目";
           } else if ("2".equals(div)) {
               // その他
               namecd2 = "3";
               defaultname = "その他特に必要な教科・科目";
           } else { // if (null == div || "0".equals(div)) {
               // 普通教育
               namecd2 = "1";
               defaultname = isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する各教科・科目";
           }
           final Map<String, String> nameMstRec = Util.getMappedMap(_a029NameMstMap, namecd2);
           return "【" + defstr(nameMstRec.get("NAME1"), defaultname) + "】";
       }

       /**
        * 普通/専門教育に関する教科のタイトルを表示するか
        * @param certifKindCd 証明書種別コード
        * @return 普通/専門教育に関する教科のタイトルを表示するか
        */
       protected boolean isPrintClassTitle(final String certifKindCd) {
           return !"1".equals(_isNotPrintClassTitle.get(certifKindCd)) && !_z010.in(Z010.hirokoudai);
       }

       /**
        * 普通/専門教育に関する教科のタイトル表示設定
        */
       private void setPrintClassTitle(final DB2UDB db2) {
           _isNotPrintClassTitle = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E015' "), "NAMECD2", "NAMESPARE1");
       }

       public String getGradeCdOfGrade(final String year, final String grade) {
           Map<String, String> gradeCdMap = Util.getMappedHashMap(_gradeGradeCdMap, year);
           if (null == gradeCdMap.get(grade) && !_gradeGradeCdMap.isEmpty()) {
               gradeCdMap = Util.getMappedHashMap(_gradeGradeCdMap, _gradeGradeCdMap.lastKey());
           }
           return gradeCdMap.get(grade);
       }

       public String getSchoolKindOfGrade(final String year, final String grade) {
           Map<String, String> schoolKindMap = Util.getMappedHashMap(_gradeSchoolKindMap, year);
           if (null == schoolKindMap.get(grade) && !_gradeSchoolKindMap.isEmpty()) {
               schoolKindMap = Util.getMappedHashMap(_gradeSchoolKindMap, _gradeSchoolKindMap.lastKey());
           }
           return schoolKindMap.get(grade);
       }

       private static TreeMap<String, Map<String, String>> getSchregRegdGdatMap(final DB2UDB db2, final String field) {
           final TreeMap<String, Map<String, String>> rtn = new TreeMap<String, Map<String, String>>();
           final String sql = " SELECT * FROM SCHREG_REGD_GDAT ";

           for (final Map row : KnjDbUtils.query(db2, sql)) {
               Util.getMappedHashMap(rtn, KnjDbUtils.getString(row, "YEAR")).put(KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, field));
           }
           return rtn;
       }
   }
}

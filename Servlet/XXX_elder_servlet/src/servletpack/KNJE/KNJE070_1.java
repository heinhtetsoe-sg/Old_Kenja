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

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;
import static servletpack.KNJZ.detail.KnjDbUtils.getString;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import servletpack.KNJG.KNJG010;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Field;
import servletpack.KNJZ.detail.SvfForm.Field.PrintMethod;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.SvfForm.Line;
import servletpack.KNJZ.detail.SvfForm.LineKind;
import servletpack.KNJZ.detail.SvfForm.LineOption;
import servletpack.KNJZ.detail.SvfForm.LineOptionIndex;
import servletpack.KNJZ.detail.SvfForm.LineWidth;
import servletpack.KNJZ.detail.SvfForm.Point;
import servletpack.KNJZ.detail.SvfForm.Record;
import servletpack.KNJZ.detail.SvfForm.Repeat;
import servletpack.KNJZ.detail.SvfForm.SubForm;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理] 進学用高校調査書
 *
 *  2004/03/16 yamashiro・学校住所２がnullで出力される不具合を修正
 *  2004/03/19 yamashiro・結核の個所に聴力が出力される不具合を修正
 *  2004/04/09 yamashiro・評定の学年表示に’第’を入れる
 *  2004/04/27 yamashiro・学習成績概評の学年人数の出力条件を追加 -> 指示画面より受取
 *  2004/06/28 yamashiro・所見等の文字列編集の仕様変更->改行は改行マークがない場合は文字数で行う
 *  2004/08/17 yamashiro・進学用に関して保健データを表示しない
 *                      ・出欠記録備考、特別活動記録、総合学習活動＆評価、備考の１行あたりの出力文字数変更
 *                      ・住所１と住所２は別段に出力。長さに応じて文字の大きさを代える。
 *                      ・科目名は長さに応じて文字の大きさを代える。
 *                      ・転入学以外の入学種別は”入学”と表記する。
 *  2004/08/19 yamashiro・調査書所見データのフィールド名変更に伴い修正。
 *  2004/08/24 yamashiro・所見等の出力におけて、改行マークと文字数で改行する。<--04/06/28の変更を元に戻す
 *                                                                               KNJZ/KNJ_EditEditにおいて処理
 *  2004/08/30 yamashiro・転入生は、入学欄の学年を括弧で囲んで表示する。
 *  2004/09/13 yamashiro・所見等の出力文字数をＯＳ区分により変更する-->(XP,WINDOWS2000で文字数を変える）
 *                      ・学習の記録欄において科目名を文字数により大きさを変えて出力する
 *  2004/09/22 yamashiro・所見等の出力の不具合を修正-->KNJZ/detail/KNJ_EditEditを修正
 *                      ・入学(転入学)の学年は入学日ENTER_DATEより算出した年度の年次とする-->KNJZ/detail/KNJ_PersonalinfoSqlを修正
 *  2005/07/10 yamashiro・成績段階別人数に近大付属高校用の処理を追加
 *                      ・出欠の記録に近大付属高校用の処理を追加
 *                      ・SVF-FORMへのデータ出力におけるNULLの処理を修正 => KNJEditStringのretStringNullToBlankを使用
 *  2005/07/14 yamashiro・記載責任者の職名と担当者名を分けて出力（職名の固定に対応）
 *  2005/07/19 yamashiro・HEXAM_ENTREMARK_HDATが存在しない場合を考慮して修正
 *  2005/11/18 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度
 *                        学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 *  2005/12/08 yamashiro・卒業日が学籍処理日の後なら卒業見込とする
 *  2006/03/22 yamashiro・就職用に学校電話番号を追加のため、メソッドhead_out_Sub1を作成  --NO001
 *  2006/04/14 yamashiro・KNJ_GeneviewmbrSqlクラスのインスタンス作成を元に戻す --NO002
 */

public class KNJE070_1 extends KNJE070_1Common {

    private static final Log log = LogFactory.getLog(KNJE070_1.class);

    private static final String SOGAKU_FLG = "#SOGAKU_FLG";
    private static final String TOTAL_FLG = "#TOTAL";

    private static final String PRGID_KNJE070A = "KNJE070A";
    private static final String PRGID_KNJE070E = "KNJE070E";
    private static final String PRGID_KNJE010E = "KNJE010E";
    private static final String PRGID_KNJE011E = "KNJE011E";
    private static final String PRGID_KNJG010 = "KNJG010";

    private final Vrw32alp __svf;
    private final List<List<String>> __csvOutputLines;
    private final DB2UDB __db2;
    public boolean nonedata;

    protected boolean _isE070_2 = false;

    private Param _param;

    public KNJE070_1(final DB2UDB db2, final Vrw32alpWrap svf, final KNJDefineSchool definecode, final String useSyojikou3) {
        this(db2, svf, (List) null);
    }

    public KNJE070_1(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode, final String useSyojikou3) {
        this(db2, svf, (List) null);
    }

    public KNJE070_1(final DB2UDB db2, final List csvOutputLines, final KNJDefineSchool definecode, final String useSyojikou3) {
        this(db2, null, csvOutputLines);
    }

    private KNJE070_1(final DB2UDB db2, final Vrw32alp svf, final List<List<String>> csvOutputLines) {
        __db2 = db2;
        __svf = svf;
        __csvOutputLines = csvOutputLines;
        nonedata = false;
        log.fatal("$Id$");
    }

    /**
     * PrepareStatement作成
     * 継承しているので削除不可!アクセス指定子変更不可!
     */
    public void pre_stat(final String hyotei, final Map paramap) {
    }

    private static void outputFuhakkou(final DB2UDB db2, final Param param, final Vrw32alp svf, final PrintData printData) {
        printData._outputFuhakkouResult = new HashMap();
        try {
            final Map paramMap = new HashMap(printData._paramap);
            paramMap.put("YEAR", printData._ctrlYear);
            paramMap.put("SCHREGNO", printData._schregno);
            paramMap.put("CERTIF_KINDCD", printData.getParameter(Parameter.CERTIFKIND));
            paramMap.put("Vrw32alp", svf);
            paramMap.put("methodName", "outputFuhakkou");
            paramMap.put("Z010.NAME1", param._z010Name1);
            if (param._z010.in(Z010Info.Nishiyama)) {
                paramMap.put("fuhakkouKindName", "調査書（成績証明書）");
            }

            printData._outputFuhakkouResult = KNJG010.execMethod(db2, paramMap);

            if (param._isOutputDebug) {
                log.info("fuhakkou = " + printData._outputFuhakkouResult);
            }
        } catch (final Throwable e) {
            log.fatal("exception in executing KNJG010.execMethod()");
        }
        printData._isFuhakkou = "true".equals(printData._outputFuhakkouResult.get("IS_FUHAKKOU"));
    }

    public void printSvf(
            final String schregno,
            final String year,
            final String semes,
            final String date,
            final String paramStaffCd,
            final String kanji,
            final String comment,
            final String os,
            final String certifNumber,
            final Map paramap
    ) {
        long start = System.currentTimeMillis();

        PrintData printData = null;
        try {
            final boolean isPrintGrd = "1".equals(paramap.get(Parameter.PRINT_GRD._name));
            if (null == _param) {
                _param = new Param(__db2, paramap, isPrintGrd);
            }
            printData = loadPrintData(this, __db2, schregno, year, semes, date, paramStaffCd, kanji, comment, os, certifNumber, paramap, _param);

            if (!printData.isCsv()) {
                outputFuhakkou(__db2, _param, __svf, printData);
                if ("true".equals(printData._outputFuhakkouResult.get("OUTPUT_SVF"))) {
                    nonedata = true;
                }
            }

            final IForm form;
            if (PrintData._shusyokuYou == printData._output) {
                // 就職用
                form = new FormKNJE070_2(_param, printData);
            } else { // if (PrintData._shingakuYou == printData._output) {
                // 進学用
                if ("1".equals(printData._tyousasyo2020)) {
                    final FormKNJE070_1_A4.FormInfoKNJE070_1_A4 formInfo = new FormKNJE070_1_A4.FormInfoKNJE070_1_A4(printData, _param);

                    formInfo._formNenYou = FormInfo.setFormNenYou(printData, _param);

                    formInfo._formNen = FormNen.NONE;

                    formInfo.setSvfFormUseSyojikou6(__svf);
                    form = new FormKNJE070_1_A4(_param, formInfo);

                } else {

                    final FormKNJE070_1.FormInfoKNJE070_1 formInfo = new FormKNJE070_1.FormInfoKNJE070_1(printData, _param);

                    formInfo._formNenYou = FormInfo.setFormNenYou(printData, _param);
                    if (_param._isOutputDebug) {
                        log.info(" formNenYou = " + formInfo._formNenYou);
                    }

                    formInfo._formNen = FormNen.NONE;

                    if ("1".equals(_param._useSyojikou3)) {
                        FormKNJE070_1.setSvfFormUseSyojikou3(printData, _param, formInfo);
                    } else {
                        FormKNJE070_1.setSvfFormNotUseSyojikou3(printData, _param, formInfo);
                    }
                    log.fatal(" form = " + (formInfo._is2page ? (formInfo._formNameLeft + ", " + formInfo._formNameRight) : formInfo._formName));
                    form = new FormKNJE070_1(_param, formInfo);
                }
            }

            final List<Title> _gradeTitlesForForm;
            if ("1".equals(printData.getParameter(Parameter.certifSchoolOnly))) {
                _gradeTitlesForForm = new ArrayList<Title>();
                _gradeTitlesForForm.add(new Title(Integer.parseInt(printData._year), printData._year,  "01", __db2, "01", printData, _param));
            } else {
                _gradeTitlesForForm = printData._gradeTitlesForForm;
            }

            if (printData.isCsv()) {
                final List<TitlePage> titlePageList = TitlePage.getTitlePageList(_param, _gradeTitlesForForm, 9999);
                for (int i = 0; i < titlePageList.size(); i++) {
                    final TitlePage titlePage = titlePageList.get(i);
                    Title.setPosition(_param, printData, titlePage._titleList);
                    if (form.csv(__db2, __csvOutputLines, printData, titlePage)) {
                        nonedata = true;
                    }
                }
            } else {
                if (PrintData._shusyokuYou == printData._output) {
                    final List<TitlePage> titlePageList = TitlePage.getTitlePageList(_param, _gradeTitlesForForm, form.getFormInfo()._formNen._intval);
                    for (int i = 0; i < titlePageList.size(); i++) {
                        final TitlePage titlePage = titlePageList.get(i);
                        Title.setPosition(_param, printData, titlePage._titleList);
                        FormKNJE070_2 form2 = (FormKNJE070_2) form;
                        if (form2.printMain2(__db2, __svf, printData, titlePage)) {
                            nonedata = true;
                        }
                        if (!form._notFoundFieldname.isEmpty()) {
                            final String message = form._currentForm + " フィールドがない: " + form._notFoundFieldname;
                            if (!_param._errorMessageOutputs.contains(message)) {
                                log.warn(message);
                                _param._errorMessageOutputs.add(message);
                            }
                        }
                        if (!form.getFormInfo()._useGradeMultiPage) {
                            break;
                        }
                    }
                } else {
                    if ("1".equals(printData._tyousasyo2020)) {

                        final FormKNJE070_1_A4 form1 = (FormKNJE070_1_A4) form;
                        if (form1.printMainA4(__db2, __svf, printData, _gradeTitlesForForm)) {
                            nonedata = true;
                        }
                        if (!form._notFoundFieldname.isEmpty()) {
                            final String message = form._currentForm + " フィールドがない: " + form._notFoundFieldname;
                            if (!_param._errorMessageOutputs.contains(message)) {
                                log.warn(message);
                                _param._errorMessageOutputs.add(message);
                            }
                        }

                    } else {
                        final List<TitlePage> titlePageList = TitlePage.getTitlePageList(_param, _gradeTitlesForForm, form.getFormInfo()._formNen._intval);
                        for (int i = 0; i < titlePageList.size(); i++) {
                            final TitlePage titlePage = titlePageList.get(i);
                            Title.setPosition(_param, printData, titlePage._titleList);
                            final FormKNJE070_1 form1 = (FormKNJE070_1) form;
                            if (form1.printMain1(__db2, __svf, printData, titlePage)) {
                                nonedata = true;
                            }
                            if (!form._notFoundFieldname.isEmpty()) {
                                final String message = form._currentForm + " フィールドがない: " + form._notFoundFieldname;
                                if (!_param._errorMessageOutputs.contains(message)) {
                                    log.warn(message);
                                    _param._errorMessageOutputs.add(message);
                                }
                            }
                            if (!form.getFormInfo()._useGradeMultiPage) {
                                break;
                            }
                        }
                    }
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            _param._elapsedPrintTimeList.add(new BigDecimal(elapsed / 1000.0).setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (Exception e) {
            log.fatal("printSvf exception!" + (null == printData ? "" : "schregno = " + printData._schregno + ", printProcess = " + printData._printProcess), e);
        }
        if (_param._isOutputDebugKinsoku) {
            KNJ_EditKinsoku._isDebug = false;
        }
    }

    private static PrintData loadPrintData(
            final KNJE070_1 o,
            final DB2UDB db2,
            final String schregno,
            final String year,
            final String semes,
            final String date,
            final String paramStaffCd,
            final String kanji,
            final String comment,
            final String os,
            final String certifNumber,
            final Map paramap,
            final Param param
    ) {
        final int output;
        if (o._isE070_2) {
            output = PrintData._shusyokuYou;
        } else {
            output = PrintData._shingakuYou;
        }
        final String staffCd = PrintData.getStaffcd(db2, schregno, year, semes, paramStaffCd, paramap, param._isPrintGrd);
        final PrintData printData = new PrintData(schregno, year, semes, date, staffCd, kanji, comment, os, certifNumber, paramap, output, param);

        if (param._z010.in(Z010Info.Kumamoto, Z010Info.Mieken)) {
            printData.setMajorCategoryDat(param, db2);
        }

        printData.setSchoolMst(db2, param, year);

        if (!"1".equals(paramap.get(Parameter.notOutputLog._name)) || param._isOutputDebug) {
            log.info(" schregno = " + printData._schregno + ", year = " + printData._year);
        }
        if (param._isOutputDebug) {
            log.info(Util.debugMapToStr("parameter", paramap));
        }
        printData._printProcess = 0;
        try {
            final SqlStudyrec sqlStudyrec = o.getPreStatementStudyrec(printData._schregno, printData._year, printData._paramap);
            sqlStudyrec.setParam(printData, param);
//            sqlStudyrec.setDefinecode(_definecode);

            printData._sqlStudyrec = sqlStudyrec;
            printData.load(db2, param);

        } catch (Exception e) {
            log.fatal("printSvf exception! schregno = " + printData._schregno + ", printProcess = " + printData._printProcess, e);
        }
        return printData;
    }

    /**
     * 生徒の評定平均を得る
     * 外部プログラムコール用メソッド
     */
    public List<HyoteiHeikin> getHyoteiHeikinList(final String schregno, final String year, final String semes, final Map paramMap) {
        final Map paramMap1 = new HashMap(paramMap);
        paramMap1.put(Parameter.notUseDaitai._name, "1");
        paramMap1.put(Parameter.notUseShoken._name, "1");
        paramMap1.put(Parameter.notUseAttend._name, "1");
        paramMap1.put(Property.knje070useSql2._name, "1");
        if (!paramMap1.containsKey("debug")) {
            paramMap1.put(Parameter.notOutputLog._name, "1");
        }
        final boolean isPrintGrd = "1".equals(paramMap1.get(Parameter.PRINT_GRD._name));
        if (null == _param) {
            _param = new Param(__db2, paramMap1, isPrintGrd);
        }
        final List<HyoteiHeikin> hyoteiHeikinList = new ArrayList<HyoteiHeikin>();
        final PrintData printData = loadPrintData(this, __db2, schregno, year, semes, null, null, null, null, null, null, paramMap1, _param);
        if (!"1".equals(paramMap1.get("totalOnly"))) {
            hyoteiHeikinList.addAll(printData._hyoteiHeikinList);
        }
        final Map<String, String> total = new HashMap<String, String>();
        hyoteiHeikinList.add(new HyoteiHeikin(total));
        total.put("CLASSCD", "TOTAL");
        if (null != printData.getTotalAvgGrades()) {
            total.put("AVG", printData.getTotalAvgGrades());
            total.put("GAIHYO", printData._assessMark);
        }
        total.put("CREDIT", printData.getPrintTotalCredit());
        return hyoteiHeikinList;
    }

    /**
     * 学習の記録データ の java.sql.PreparedStatement オブジェクトをメンバ変数 ps1 にセットします。<br>
     * ※KNJE070_2、KNJI060_1(間接的にKNJI060_2からも)コールされているためシグネチャ変えちゃだめ
     * @return
     */
    protected SqlStudyrec getPreStatementStudyrec(final String schregno, final String year, final Map paramap) {
        return new SqlStudyrec();
    }

    /**
     * PrepareStatement close
     */
    public void pre_stat_f() {
        if (null != _param) {
            _param.close();
            _param = null;
        }
    }

    private static abstract class IForm extends KNJE070_1Common.Form {
        protected static final String _SLASH = "／";
        protected static final String ATTRIBUTE_MUHENSYU = "Hensyu=0";
        protected static final String ATTRIBUTE_MIGITSUME = "Hensyu=1";
        protected static final String ATTRIBUTE_CENTER = "Hensyu=3";
        protected static final String ATTRIBUTE_BOLD = "Bold=1";
        protected static final String TSUSHINSEI_NIHA = "通信制には";
        protected static final String SYUSSEKI_NO_KITEI_HA_NAI = "出席の規定はない";
        protected static final String TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI = TSUSHINSEI_NIHA + SYUSSEKI_NO_KITEI_HA_NAI;

//        final Map _formNamePathMap = new HashMap();
//        List _svfFormLines;
        protected Param _param;
        protected List<List<String>> _csvOutputLines;
        protected PrintData _printData;
        public IForm(final Param param) {
            super(param);
            _param = param;
        }

        public abstract FormInfo getFormInfo();

        public abstract boolean csv(final DB2UDB db2, final List<List<String>> outputLines, final PrintData printData, final TitlePage titlePage);
        protected Param param() {
            return _param;
        }
        public String toString() {
            return "Form(" + getFormInfo() + ")";
        }

        public static void copyForm(final Param param, final Vrw32alp svf, final String filename1, final String filename2) {
            final String key = filename1 + "|" + filename2;
            if (param.modifyFormPathMap().containsKey(key)) {
                return;
            }
            File formFile = null;
            try {
                // 進学用
                final String formPath = svf.getPath(filename1);
                formFile = new File(formPath);
                if (param._isOutputDebug) {
                    log.info(" form path = " + formPath);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == formFile) {
                return;
            }
            final String newFilename = formFile.getParent() + "/" + filename2;
            final File newFile = Util.copyFile(formFile, newFilename);
            if (param._isOutputDebug) {
                log.info(" copy file " + formFile + " to " + newFile);
            }
            param.modifyFormPathMap().put(key, newFile);
        }

        protected void setSvfFieldInfo(final PrintData printData, final Param param, final String formName) {
            printData._fieldInfoAddr1 = new KNJSvfFieldInfo();
            printData._fieldInfoAddr2 = new KNJSvfFieldInfo();

            // 住所フィールド
            final int addr1X1;
            final int addr1X2;
            final int addr1Ystart;
            final int addr2Ystart;

            if (PrintData._shusyokuYou == printData._output) {
                if (param._isShusyokuyouKinkiToitsu2) {
                    final SvfField addr1 = getField("GUARD_ADDRESS1");
                    addr1X1 = addr1.x();
                    if (-1 != getFormInfo()._guardAddress1EndX) {
                        addr1X2 = getFormInfo()._guardAddress1EndX;
                    } else {
                        addr1X2 = 2304;
                    }
                    addr1Ystart = addr1.y();
                    final SvfField addr2 = getField("GUARD_ADDRESS2");
                    addr2Ystart = addr2.y();
                } else if (getFormInfo()._isKumamotoForm) {
                    addr1X1 = 1241 + 10;
                    addr1X2 = 2236;
                    addr1Ystart = 310 - 5; // 350 - 40
                    addr2Ystart = 378 - 5; //418 - 40
                } else {
                    if (getFormInfo()._isKyotoForm || param._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama)) {
                        addr1X1 = 1261 + 10;
                        addr1X2 = 2232;
                    } else if (param._z010.in(Z010Info.Tokiwa)) {
                        addr1X1 = 1261 + 10;
                        addr1X2 = 2213;
                    } else if (param.isKindaifuzoku() && !"1".equals(printData._tyousasyo2020)) {
                        addr1X1 = 1141 + 10;
                        addr1X2 = 2132;
                    } else {
                        addr1X1 = 1161 + 10;
                        addr1X2 = 2132;
                    }
                    addr1Ystart = 350 - 5;
                    addr2Ystart = 418 - 5;
                }
            } else {
                if ("1".equals(printData._tyousasyo2020)) {
                    final SvfField addr1 = getField("GUARD_ADDRESS1");
                    final SvfField addr2 = getField("GUARD_ADDRESS2");
                    if (_param._isOutputDebugField) {
                        log.info(" addr1 x = " + addr1.x() + " / " + getFormInfo()._guardAddress1EndX + ", y = " + addr1.y() + ", addr2 y = " + addr2.y());
                    }

                    addr1X1 = addr1.x();
                    addr1X2 = getFormInfo()._guardAddress1EndX;
                    addr1Ystart = addr1.y();
                    addr2Ystart = addr2.y();
                } else {
                    // 進学用フォームの住所位置は共通
                    addr1X1 = 1851 + 10;
                    addr1X2 = 3101;
                    addr1Ystart = 462 - 5;
                    addr2Ystart = 541 - 5;
                }

            }
            printData._fieldInfoAddr1._x1 = addr1X1;
            printData._fieldInfoAddr1._x2 = addr1X2;
            printData._fieldInfoAddr1._ystart = addr1Ystart;
            printData._fieldInfoAddr2._ystart = addr2Ystart;
        }

        /**
         * (CSV変換用)
         * @param lines 文字列のリスト
         * @return 文字列のリストのリスト
         */
        protected <T> List<List<T>> setLined(final List<T> lines) {
            final List<List<T>> rtn = new ArrayList<List<T>>();
            for (int i = 0; i < lines.size(); i++) {
                final List<T> l = new ArrayList<T>();
                l.add(lines.get(i));
                rtn.add(l);
            }
            return rtn;
        }

        /**
         * 個人情報
         */
        protected void printAddressCommon(final DB2UDB db2, final boolean isFormatOnly) {

            final PersonInfo personInfo = _printData._personInfo;
            if (null == personInfo) {
                return;
            }
            printStudentName(personInfo, isFormatOnly);
            if (_printData._output == PrintData._shusyokuYou && _param._isShusyokuyouKinkiToitsu2) {
                svfVrsOut("BIRTHDAY_GENGOU", "昭和・平成");
                svfVrsOut("BIRTHDAY", "　　年　　月　　日");
                String birthdayCancelField = null;
                if (null != _printData._personInfo._birthday) {
                    final List<Map<String, String>> l007List = _param.getL007List(db2);
                    Map<String, String> before = null;
                    Map<String, String> target = null;
                    for (final Map<String, String> row : l007List) {
                        final String startDate = StringUtils.defaultString(StringUtils.replace(getString(row, "NAMESPARE2"), "/", "-"), "0001-01-01");
                        final String endDate = StringUtils.defaultString(StringUtils.replace(getString(row, "NAMESPARE3"), "/", "-"), "9999-12-31");
                        if (startDate.compareTo(_printData._personInfo._birthday) <= 0 && _printData._personInfo._birthday.compareTo(endDate) <= 0) {
                            target = row;
                            break;
                        }
                        before = row;
                    }
                    final String name1 = getString(target, "NAME1");
                    if ("平成".equals(name1)) {
                        birthdayCancelField = "BIRTHDAY_CANCEL_LEFT";
                    } else if ("昭和".equals(name1)) {
                        birthdayCancelField = "BIRTHDAY_CANCEL_RIGHT";
                    } else if (null != target) {
                        svfVrsOut("BIRTHDAY_GENGOU", defstr(getString(before, "NAME1"), "　　") + "・" + defstr(getString(target, "NAME1"), "　　"));
                        birthdayCancelField = "BIRTHDAY_CANCEL_LEFT";
                    }
                    svfVrAttributeUchikeshi(birthdayCancelField, 5);
                    final String[] tate_format = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _printData._personInfo._birthday));
                    if (null != tate_format) {
                        svfVrsOut("BIRTHDAY", defstr(Util.hitoketaHaZenkaku(tate_format[1])) + " 年 " + defstr(Util.hitoketaHaZenkaku(tate_format[2])) + " 月 " + defstr(Util.hitoketaHaZenkaku(tate_format[3])) + " 日生");
                    }
                }
            } else if (!isFormatOnly) {
                svfVrsOut("BIRTHDAY", getPrintBirthdate(db2, personInfo));
            }
            if (!isFormatOnly) {
                svfVrsOut("SEX",      defstr(_param._z002Abbv1Map.get(personInfo._sex)));
            }
            if (_printData.isRemarkOnly(param())) {
                return;
            }

            if (!isFormatOnly) {
                printStudentAddress(defstr(personInfo._addr1), defstr(personInfo._addr2), personInfo._addrFlg);
            }

            final String coursename = defstr(personInfo._coursename);
            svfVrsOutForData(Arrays.asList("katei",  "katei2_1",  "katei3_1"), coursename, false);
            if (getFormInfo()._isKumamotoForm) {
                if (PrintData._shingakuYou == _printData._output) {
                    // 熊本進学用のみの処理
                    if (-1 != coursename.indexOf("全日")) {
                        svfVrsOut("COURSENAME1", "○");
                    } else if (-1 != coursename.indexOf("定時")) {
                        svfVrsOut("COURSENAME2", "○");
                    } else if (-1 != coursename.indexOf("通信")) {
                        svfVrsOut("COURSENAME3", "○");
                    }
                }
            }
            printMajorName();

            printNyugakuSotsugyo(db2, isFormatOnly);

            if (param()._z010.in(Z010Info.Sundaikoufu) && !_printData.isSundaiKofuBijutsuDesignKa(_param)) {
                svfVrsOut("GRD_TERM", personInfo.getString("GRD_TERM")); // 卒期
                svfVrsOut("COURSEABBV", personInfo.getString("COURSECODEABBV3")); // 文理区分
                String abbv1 = defstr(personInfo.getString("HR_NAMEABBV"));
                if (1 <= abbv1.length()) {
                    abbv1 = abbv1.substring(abbv1.length() - 1);
                }
                String attendno2 = defstr(personInfo.getString("ATTENDNO"));
                if (NumberUtils.isDigits(attendno2)) {
                    final DecimalFormat z2 = new DecimalFormat("00");
                    attendno2 = z2.format(Integer.parseInt(attendno2));
                }
                svfVrsOut("HRCLASSATTENDNO", abbv1 + attendno2); // HR_NAMEABBV下一桁 + ATTENDNO下二桁
            }
        }

        protected String getPrintBirthdate(final DB2UDB db2, final PersonInfo personInfo) {
            String birthdate = "";
            if (param()._isSeireki || (!param()._isSeireki && "1".equals(personInfo.getString("BIRTHDAY_FLG")))) {
                birthdate = Util.h_format_Seireki(_printData._personInfo._birthday);
                birthdate = StringUtils.isBlank(birthdate) ? "" : birthdate  + "生";
            } else {
                birthdate = KNJ_EditDate.h_format_JP_Bth(db2, _printData._personInfo._birthday);
                birthdate = StringUtils.isBlank(birthdate) ? "" : birthdate;
            }
            return birthdate;
        }

        private String getEnterName(final PrintData printData) {
            final PersonInfo personInfo;
            if (param()._preferEntInfoJ && null != printData._personinfoJ) {
                personInfo = printData._personinfoJ;
            } else {
                personInfo = printData._personInfo;
            }
            final String mEnterName = personInfo.getString("ENTER_NAME");
            final String mEnterName2 = personInfo.getString("ENTER_NAME2");
            final boolean isTennyuHennyu = PersonInfo.isTennyuHennyu(personInfo._entDiv);
            String name = null;
            if (!StringUtils.isBlank(mEnterName2)) {
                name = mEnterName2;
            } else if (isTennyuHennyu) {
                name = mEnterName;
            } else if (mEnterName != null) {
                name = "入学";
            }
            return name;
        }

        private void printNyugakuSotsugyo(final DB2UDB db2, final boolean isFormatOnly) {
            final PersonInfo personInfo = _printData._personInfo;
            if (null == personInfo) {
                return;
            }

            if (_printData.miyagikenShushokuyouUseGappeiTougouForm(_param)) {
                svfVrsOut("ENTERDATE2", getPrintEntDate(db2));
                svfVrsOut("GAPPEIMOTO_GAKKO2", _printData._personInfo._entSchool);
                svfVrsOut("GRADE2", getPrintEntGrade(db2, _printData));
                svfVrsOut("ENTER2", getEnterName(_printData));
                final String foundedYear = getString(_printData._schoolMstMap, "FOUNDEDYEAR");
                if (NumberUtils.isDigits(foundedYear)) {
                    svfVrsOut("FOUNDATION_DATE2", Util.h_format_M(db2, param(), foundedYear + "-04-01", getFormInfo().isShowFormat2(_printData)));
                }
                svfVrsOut("FOUNDATION_TEXT2", "学校再編統合により、学校名・所在地変更");


                final Tuple<String, String> gradu = getPrintGraduDateAndPrintGraduName(db2);
                final String graduDate = gradu._first;
                final String graduName = gradu._second;
                svfVrsOut("TRANSFER_DATE2", graduDate);
                svfVrsOut("TRANSFER2", graduName);

            } else if (PrintData._shusyokuYou == _printData._output && param()._isShusyokuyouKinkiToitsu2) {
                svfVrsOut("ENTERDATE", getPrintEntDate(db2));
                final String[] tate_format = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _printData._year + "-12-31"));
                if (null != tate_format) {
                    svfVrsOut("ENTERDATE", defstr(tate_format[0], "　　") + "　年４月");
                }
                final String entDate = personInfo.getString("ENT_DATE");
                if (!StringUtils.isBlank(entDate)) {
                    svfVrsOut("ENTERDATE", naraYearMonthFormat(db2, entDate));
                }

                svfVrsOut("GRADE", "(第　学年)");
                if (PersonInfo.isNyugaku(personInfo._entDiv)) {
                    svfVrsOut("ENTER_MARU123", "〇");
                } else if (PersonInfo.isTennyuHennyu(personInfo._entDiv)) {
                    svfVrsOut("ENTER_MARU" + personInfo._entDiv, "〇");
                    final String entYearGradeCd = personInfo.getString("ENT_YEAR_GRADE_CD");
                    if (NumberUtils.isDigits(entYearGradeCd)) {
                        svfVrsOut("GRADE", "(第" + Util.hitoketaHaZenkaku(String.valueOf(Integer.parseInt(entYearGradeCd))) + "学年)");
                    }
                }

                final String grdDiv = personInfo._grdDiv;
                boolean printGrdDate = false;
                if (null == grdDiv || "4".equals(grdDiv)) {
                    svfVrsOut("TRANSFER_MARU4", "〇");
                    printGrdDate = true;
                } else if ("1".equals(grdDiv)) {
                    svfVrsOut("TRANSFER_MARU1", "〇");
                    printGrdDate = true;
                }
                if (printGrdDate) {
                    svfVrsOut("TRANSFER_DATE", defstr(tate_format[0], "　　") + "　年３月");
                    final String graduDate = blankDefstr(personInfo._grdDate, personInfo._graduDate);
                    if (!StringUtils.isBlank(graduDate)) {
                        svfVrsOut("TRANSFER_DATE", naraYearMonthFormat(db2, graduDate));
                    }
                }

            } else if (isFormatOnly) {
                final String[] hoge1 = KNJ_EditDate.tate_format4(db2, String.valueOf(Integer.parseInt(_printData._ctrlYear) - 2) + "-03-31");
                final String gengou1 = null != hoge1 && hoge1.length >= 1 ? defstr(hoge1[0], "　　") : "　　";
                final String[] hoge2 = KNJ_EditDate.tate_format4(db2, String.valueOf(Integer.parseInt(_printData._ctrlYear) + 1) + "-03-31");
                final String gengou2 = null != hoge2 && hoge2.length >= 1 ? defstr(hoge2[0], "　　") : "　　";

                svfVrsOut("ENTERDATE", gengou1  + "　年　月");
                svfVrsOut("ENTER1", "入学");
                svfVrsOut("TRANSFER_DATE", gengou2  + "　年　月");
                svfVrsOut("TRANSFER1", "卒業見込");

            } else {
                svfVrsOut("ENTERDATE", getPrintEntDate(db2));
                if (null == _printData._personInfo) {
                } else if (null == _printData._personInfo._entDiv) {
                    log.info(" entDiv null : " + _printData._personInfo._map);
                } else {
                    final Tuple<String, String> entGradeAndEnter1 = getEntGradeAndEnter1(db2);
                    if (_param._isOutputDebugBase) {
                        log.info(" entDiv = " + _printData._personInfo._entDiv + ", gradeAndEnter1 = " + entGradeAndEnter1);
                    }
                    final String grade = entGradeAndEnter1._first;
                    final String enter1 = entGradeAndEnter1._second;
                    svfVrsOut("GRADE", grade); // フォームで"マスク"の場合がある
                    svfVrsOut("ENTER1", enter1);
                }

                final Tuple<String, String> gradu = getPrintGraduDateAndPrintGraduName(db2);
                final String graduDate = gradu._first;
                final String graduName = gradu._second;
                svfVrsOut("TRANSFER_DATE", graduDate);
                svfVrsOut("TRANSFER1", graduName);
            }
        }
        private String naraYearMonthFormat(final DB2UDB db2, final String graduDate) {
            final String[] dateTate_format = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, graduDate));
            String s = null;
            if (null != dateTate_format) {
                s = defstr(dateTate_format[0], "　　") + defstr(Util.hitoketaHaZenkaku(dateTate_format[1]), "　") + "年" + defstr(Util.hitoketaHaZenkaku(dateTate_format[2]), "　") + "月";
            }
            return s;
        }

        protected Tuple<String, String> getEntGradeAndEnter1(final DB2UDB db2) {
            if (null == _printData._personInfo) {
                return Tuple.of(null, null);
            }
            final String entGrade = getPrintEntGrade(db2, _printData);
            final String enterName = getEnterName(_printData);

            final String entDiv;
            if (_param._preferEntInfoJ && null != _printData._personinfoJ) {
                entDiv = _printData._personinfoJ._entDiv;
            } else {
                entDiv = _printData._personInfo._entDiv;
            }
            final boolean isTennyuHennyu = PersonInfo.isTennyuHennyu(entDiv);
            final String grade;
            final String enter1;
            if (getFormInfo().isShowFormat2(_printData)) {
                if (getFormInfo() instanceof FormKNJE070_2.FormInfoKNJE070_2 && ((FormKNJE070_2.FormInfoKNJE070_2) getFormInfo())._useFormKNJE070_3) {
                    if (isTennyuHennyu) { // 転学、編入なら名称を表示する
                        if (_printData._isTsushin) {
                            grade = enterName;
                            enter1 = "";
                        } else {
                            grade = "(" + entGrade + ")";
                            enter1 = enterName;
                        }
                    } else {
                        // 入学学年は表示しない
                        grade = enterName;
                        enter1 = "";
                    }
                } else {
                    grade = "";
                    if (isTennyuHennyu) { // 転学、編入なら名称を表示する
                        if (_printData._isTsushin) {
                            enter1 =  defstr(enterName);
                        } else {
                            enter1 =  "(" + entGrade + ")  " + defstr(enterName);
                        }
                    } else {
                        enter1 = ("2".equals(_printData.property(Property.tyousasyoNotPrintEnterGrade)) ? entGrade + "　" : "") + defstr(enterName);
                    }
                }
            } else if ("1".equals(_printData.property(Property.tyousasyoNotPrintEnterGrade)) && !"1".equals(_printData._tyousasyo2020)) {
                grade = enterName; // 入学学年は表示しない
                enter1 = "";
            } else if (isTennyuHennyu) {
                if (_printData._isTsushin) {
                    grade = enterName;
                    enter1 = "";
                } else {
                    grade = "(" + entGrade + ")";
                    enter1 = enterName;
                }
//                } else if (param()._isJisyuukan) {
//                    grade = entGrade;
//                    if (mEnterName != null) {
//                        if (-1 == mEnterName.indexOf("入学")) {
//                            enter1 = mEnterName;
//                        } else {
//                            enter1 = "後期課程開始";
//                        }
//                    }
            } else {
                grade = entGrade;
                enter1 = enterName;
            }
            return Tuple.of(grade, enter1);
        }

        private String getPrintEntGrade(final DB2UDB db2, final PrintData printData) {
            PersonInfo personInfo = printData._personInfo;
            // 転入学・編入学は、学年を括弧で囲む
            // 転入学以外の入学種別は”入学”と表記する
            String entGrade = "";
            if (printData._isKyoto && !_printData.isGakunensei(param())) {
                if (null != _printData._personInfo._entDate) {
                    entGrade += KNJ_EditDate.h_format_JP_N(db2, Util.date0401(_printData._personInfo._entDate)) + "度";
                }
            } else if (param()._preferEntInfoJ && null != printData._personinfoJ) {
                final String enterGrade = printData._personinfoJ.getString("ENTER_GRADE");
                final String g;
                if (null != enterGrade) {
                    g = String.valueOf(Integer.parseInt(enterGrade));
                } else {
                    g = " ";
                }
                entGrade += "第" + g + defstr(_printData._schoolInfo.anname, "　　");
            } else {
                final String entYearGradeCd = personInfo.getString("ENT_YEAR_GRADE_CD");
                final String enterGrade = personInfo.getString("ENTER_GRADE");
                final String g;
                if (NumberUtils.isDigits(entYearGradeCd) && (_param._dbHasSchoolKindPJH || "1".equals(printData.property(Property.tyousasyoPrintGradeCdAsEntGrade)))) {
                    g = Integer.valueOf(entYearGradeCd).toString();
                } else if (null != enterGrade) {
                    int entgradeint = Integer.parseInt(enterGrade);
                    if (printData._isHesetuKou) {
                        if (param()._isOutputDebug) {
                            log.info(" 併設校 entgradeint = " + String.valueOf(entgradeint));
                        }
                        if (3 < entgradeint) {
                            entgradeint -= 3;
                        }
                    } else if (param()._z010.in(Z010Info.Seijyo)) {
                        if (3 < entgradeint) {
                            entgradeint -= 3;
                        }
                    }
                    g = String.valueOf(entgradeint);
                } else {
                    g = " ";
                }
                entGrade += "第" + g + defstr(_printData._schoolInfo.anname, "　　");
            }
            return entGrade;
        }

        protected Tuple<String, String> getPrintGraduDateAndPrintGraduName(final DB2UDB db2) {
            PersonInfo personInfo = _printData._personInfo;
            if (null == personInfo) {
                return Tuple.of(null, null);
            }
            String graduDate = null;
            final String grdDiv = personInfo._grdDiv;
            if (null != personInfo._grdDate && "4".equals(grdDiv)) {
                graduDate = personInfo._grdDate;
            } else if (_printData._isMiyagikenTsushin || _printData._isHirokoudaiTsushin) {
                graduDate = defstr(personInfo._grdDate, String.valueOf(1 + Integer.parseInt(_printData._ctrlYear)) + "-03-31");
            } else {
                graduDate = personInfo._graduDate;
            }

            //卒業項目
            final String graduName;
            //卒業日が学籍処理日の後なら卒業見込とする
            final String ctrlDate = _printData.getParameter(Parameter.CTRL_DATE);
            if (null != personInfo._grdDate && "4".equals(grdDiv)) {
                graduName = "卒業見込み";
            } else if (!StringUtils.isBlank(graduDate) && !StringUtils.isBlank(ctrlDate) && Util.toCalendar(ctrlDate.replace('/', '-')).before(Util.toCalendar(graduDate))) {
                graduName = "卒業見込み";
            } else {
                graduName = defstr(personInfo.getString("GRADU_NAME"));
            }
            return Tuple.of(defstr(Util.h_format_M(db2, param(), graduDate, getFormInfo().isShowFormat2(_printData))), graduName);
        }

        protected String getPrintEntDate(final DB2UDB db2) {
            //            final String entdate = (param()._isJisyuukan) ? getString(personInfoMap, "ENT_DATE2") : _printData._entDate;
            final String entdate;
            if (param()._preferEntInfoJ && null != _printData._personinfoJ) {
                entdate = _printData._personinfoJ.getString("ENT_DATE");
            } else {
                entdate = _printData._personInfo._entDate;
            }
            final String printEntDate = defstr(Util.h_format_M(db2, param(), entdate, getFormInfo().isShowFormat2(_printData)));
            return printEntDate;
        }

        /*
         * 証書番号の出力
         * @param param
         * @throws SQLException
         */
        protected void printSyoushoNum(final boolean isSeisekiForm, final boolean isFormatOnly) {
            if (param().isKindaifuzoku()) {
                if (isSeisekiForm) {
                    svfVrsOut("NENDO_NAME",  defstr(_printData._certifNumber) + defstr(_printData._schoolInfo.shoshoname) + "証第       号");  // 証明書番号
                }
            } else {
                String syosyoNameLr = getPrintSyosyoNameLr(isFormatOnly);
                if (_printData._isMiyagikenTsushin && "SYOSYO_NAME_R".equals(_printData._schoolInfo._syosyoNameField)) {
                    // 右詰
                    final int keta = PrintData._shusyokuYou == _printData._output ? 86 : 115;
                    final int nokoriSpace = keta - getMS932ByteLength(syosyoNameLr);
                    syosyoNameLr = StringUtils.repeat("　", nokoriSpace / 2) + StringUtils.repeat(" ", nokoriSpace % 2) + syosyoNameLr;
                } else if ("2".equals(_printData._schoolInfo.certifSchoolRemark[10])) {
                    final String field = "1".equals(_printData._tyousasyo2020) ? "CERTIF_NAME_2020" : "CERTIF_NAME_R";
                    svfVrAttribute(field, ATTRIBUTE_MIGITSUME);
                    if (!"1".equals(_printData._tyousasyo2020)) {
                        syosyoNameLr += "\u00A0"; // 右寄せの場合、スペース文字ではサプレスされるためnbsp追加
                    }
                }
                svfVrsOut(_printData._schoolInfo._syosyoNameField, syosyoNameLr);
            }
        }

        protected  String getPrintSyosyoNameLr(final boolean isFormatOnly) {
            final String printnumber;
            if (!isFormatOnly && _printData._schoolInfo._isOutputCertifNo && !StringUtils.isBlank(_printData._certifNumber)) {
                printnumber = _printData._certifNumber;
            } else {
                // 証明書番号が無い場合 5スペース挿入
                printnumber = "     ";
            }
            String syosyoNameLr = defstr(_printData._schoolInfo.shoshoname) + printnumber + defstr(_printData._schoolInfo.shoshoname2);
            if (param()._isOutputDebugField) {
                param().logOnce(" syosyoNameLr = " + syosyoNameLr);
            }
            return syosyoNameLr;
        }

        // Tuple<String, String>
        protected Tuple<String[], String[]> getNameKanaAndName(final PersonInfo personInfo, final int kanaMax, final int nameMax, final boolean checkField) {
            final String[] nameKanas;
            final String[] names;
            final String rsNameKana = defstr(personInfo.nameKana());
            final String rsName = defstr(personInfo.name());
            String debug = "";
            if (personInfo._useRealName || "1".equals(_printData.property(Property.certifPrintRealName))) {
                final String rsRealNameKana = defstr(personInfo.getString("REAL_NAME_KANA"));
                final String rsRealName = defstr(personInfo.realName());
                if (personInfo._isPrintNameBoth) {
                    final int realNameKanaLen = getTextKeta(rsRealNameKana);
                    final int nameKanaLen = getTextKeta(rsNameKana);
                    final int realNameLen = getTextKeta(rsRealName);
                    final int nameLen = getTextKeta(rsName);
                    final int kakkkoWidth = getTextKeta("（）");
//                    log.info(" kanalen = " + (realNameKanaLen + nameKanaLen + kakkkoWidth) + " namelen = " + (realNameLen + nameLen + kakkkoWidth) + " / (" + kanaMax + ", " + nameMax + ")");
                    if ((realNameKanaLen + nameKanaLen + kakkkoWidth > kanaMax || realNameLen + nameLen + kakkkoWidth > nameMax) && (!checkField || checkField && formHasField("KANA2") && formHasField("KANA3") && formHasField("NAME2") && formHasField("NAME3"))) {
                        nameKanas = new String[] { rsRealNameKana, "（" + rsNameKana + "）" };
                        names = new String[] { rsRealName, "（" + rsName + "）" };
                    } else {
                        nameKanas = new String[] { StringUtils.isBlank(rsRealNameKana + rsNameKana) ? "" : rsRealNameKana + "（" + rsNameKana + "）" };
                        names = new String[] { StringUtils.isBlank(rsRealName + rsName) ? "" : rsRealName + "（" + rsName + "）" };
                    }
                    debug = "both";
                } else {
                    nameKanas = new String[] { rsRealNameKana };
                    names = new String[] { rsRealName };
                    debug = "realname";
                }
            } else {
                nameKanas = new String[] { rsNameKana };
                names = new String[] { rsName };
                debug = "";
            }
            if (param()._isOutputDebug) {
                log.info(" nameKanaAndName (" + debug + ") = " + ArrayUtils.toString(nameKanas) + ", " + ArrayUtils.toString(names));
            }
            return Tuple.of(nameKanas, names);
        }

        private void printStudentName(final PersonInfo personInfo, final boolean isFormatOnly) {
            if (isFormatOnly) {
                return;
            }
            final Tuple<String[], String[]> nameKanaAndName = getNameKanaAndName(personInfo, getField("KANA")._fieldLength, getField("NAME")._fieldLength, true);
            final String[] nameKanas = nameKanaAndName._first;
            final String[] names = nameKanaAndName._second;
            if (_param._isOutputDebug) {
                log.info(" 生徒(かな, 氏名) = " + ArrayUtils.toString(nameKanas) + ", " + ArrayUtils.toString(names));
            }

            double nameCharSize = Double.MAX_VALUE;
            if ("1".equals(_printData._kanji)) {
                if (names.length > 1) {
                    svfVrsOut("NAME2", names[0]);
                    svfVrsOut("NAME3", names[1]);
                } else {
                    final String fieldName = "NAME";
                    final SvfField field = getField(fieldName);
                    final String name = names[0];
                    final int maxnum = field._fieldLength;
                    boolean isPrintDiffField = false;

                    if (getTextKeta(name) > maxnum) {
                        final String fieldName2 = getFieldForData(Arrays.asList("NAME", "NAME_2", "NAME_3", "NAME_4", "NAME_5", "NAME_6", "NAME_7", "NAME_8", "NAME_9"), name, false);
                        if (!fieldName.equals(fieldName2)) {
                            svfVrsOut(fieldName2, name);
                            isPrintDiffField = true;
                            final SvfField field2 = getField(fieldName2);
                            if (null != field2) {
                                nameCharSize = field2.size();
                            }
                        }
                    }

                    if (isPrintDiffField == false) {
                        final int nameDefaultCharSize = PrintData._shusyokuYou == _printData._output ? 12 : 14;
                        final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldName, KNJSvfFieldModify.fieldWidth(field.size(), 1, maxnum), (int) KNJSvfFieldModify.charHeightPixel(nameDefaultCharSize), field.y(), 20, maxnum);
                        nameCharSize = modify.getCharSize(name);
                        final int yjiku = (int) modify.getYjiku(0, (float) nameCharSize);
                        if (param()._isOutputDebugField && param().isOutputDebugField(fieldName)) {
                            log.info(" SvfField " + fieldName + " " + modify + " => " + nameCharSize);
                        }
                        svfVrAttribute(fieldName, "Size=" + nameCharSize);
                        svfVrAttribute(fieldName, "Y=" + yjiku);
                        svfVrsOut(fieldName, name);
                    }
                }
            }
            if (nameKanas.length > 1) {
                svfVrsOut("KANA2", nameKanas[0]);
                svfVrsOut("KANA3", nameKanas[1]);
            } else {
                final int nameKanaDefaultCharSize = PrintData._shusyokuYou == _printData._output ? 8 : 9;
                final int height = (int) KNJSvfFieldModify.charHeightPixel(nameKanaDefaultCharSize);
                final String fieldKana = "KANA";
                final String nameKana = nameKanas[0];
                final int minnum = 20;
                final SvfField field = getField(fieldKana);
                final int maxnum = field._fieldLength;
                final double width = KNJSvfFieldModify.fieldWidth(field.size(), 1, maxnum);
                final int ystart = field.y();
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldKana, width, height, ystart, minnum, maxnum);
                final double kanaCharSize = Math.min(nameCharSize, modify.getCharSize(nameKana));
                final int yjiku = (int) modify.getYjiku(0, kanaCharSize);
                if (param()._isOutputDebugField && param().isOutputDebugField(fieldKana)) {
                    log.info(" SvfField " + fieldKana + " " + modify + " => " + nameCharSize);
                }
                svfVrAttribute(fieldKana, "Size=" + kanaCharSize);
                svfVrAttribute(fieldKana, "Y=" + yjiku);
                svfVrsOut(fieldKana, nameKana);
            }
        }

        private void printStudentAddress(final String addr1, final String addr2, final String addrFlg) {

            //住所分割処理
            final String fieldGuardAddress1;
            final String fieldGuardAddress2;
            final int addr1len = getTextKeta(addr1);
            final int addr2len = getTextKeta(addr2);
            final int minnum = 38;
            final int maxnum;
            if ("1".equals(_printData.property(Property.useAddrField2)) && (addr1len > 50 || addr2len > 50)) {
                // 60桁用
                fieldGuardAddress1 = "GUARD_ADDRESS1_2";
                fieldGuardAddress2 = "GUARD_ADDRESS2_2";
                maxnum = 60;
            } else {
                fieldGuardAddress1 = "GUARD_ADDRESS1";
                fieldGuardAddress2 = "GUARD_ADDRESS2";
                maxnum = 50;
            }

            final boolean modify = !(_printData._output == PrintData._shusyokuYou && param()._isShusyokuyouKinkiToitsu2);
            if (modify) {
//                log.info(" addr1 = " + _printData._fieldInfoAddr1);
//                log.info(" addr2 = " + _printData._fieldInfoAddr2);

                final int height = _printData._fieldInfoAddr2._ystart - _printData._fieldInfoAddr1._ystart;
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(fieldGuardAddress1, _printData._fieldInfoAddr1.getWidth(), height, _printData._fieldInfoAddr1._ystart, minnum, maxnum);
                double charSize1 = modify1.getCharSize(addr1);
                double charSize2t = modify1.getCharSize(addr2);
                if (-1.0f != charSize1) {

                    _svf.VrAttribute(fieldGuardAddress1, "Size=" + charSize1);
                    final double charSize2 = -1.0f == charSize2t ? charSize1 : Math.min(charSize1, charSize2t);
                    _svf.VrAttribute(fieldGuardAddress2, "Size=" + charSize2);

                    final int y1 = (int) modify1.getYjiku(0, charSize1);
                    final int y2 = (int) modify1.getYjiku(1, charSize2);
                    _svf.VrAttribute(fieldGuardAddress1, "Y=" + y1);
                    _svf.VrAttribute(fieldGuardAddress2, "Y=" + y2);
//            		log.info(" charSize1 = " + charSize1 + ", y1 = " + y1 + ", y2 = " + y2);
                }
            }
            svfVrsOut(fieldGuardAddress1,  addr1);
            if ("1".equals(addrFlg)) { // 住所2(方書き)
                svfVrsOut(fieldGuardAddress2,  addr2);
            }
        }

        private void printMajorName() {
            if (null == _printData._personInfo) {
                return;
            }
            final String majorName = getPrintMajorname(_printData._personInfo);
            // log.debug(" majorname = "+ majorname);

            if (StringUtils.isBlank(majorName)) {
                return;
            }
            final String gakkaField = "gakka";
            if (param()._z010.in(Z010Info.Musashinohigashi) && !"1".equals(_printData._tyousasyo2020)) {
            } else {
                // 学科フィールド
                final int posx1;
                final int posx2;
                final int minnum;
                final int maxnum;
                final int ystart;

                final SvfField field = getField(gakkaField);
                final double gakkaFieldCharSize = Double.parseDouble((String) field.getAttributeMap().get(SvfField.AttributeSize));
                final int gakkaFieldKeta = Integer.parseInt((String) field.getAttributeMap().get(SvfField.AttributeKeta));
                if (PrintData._shusyokuYou == _printData._output) {
                    if (getFormInfo()._isKyotoForm) {
                        posx1 = 265;
                        posx2 = 1131;
                    } else if (param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji) || param()._z010.in(Z010Info.Tokiwa)) {
                        posx1 = 547;
                        posx2 = 1147;
                    } else if (param().isKindaifuzoku() && !"1".equals(_printData._tyousasyo2020)) {
                        posx1 = 562;
                        posx2 = 1012;
                    } else if (getFormInfo()._isKumamotoForm) {
                        posx1 = 617;
                        posx2 = 1110;
                    } else {
//                        posx1 = 449;
//                        posx2 = 1049;
                        posx1 = field.x();
                        posx2 = (int) (field.x() + KNJSvfFieldModify.fieldWidth(gakkaFieldCharSize, 1, gakkaFieldKeta));
                        if (param()._isOutputDebugField && param().isOutputDebugField(gakkaField)) {
                            log.info(" gakka charSize = " + gakkaFieldCharSize + ", keta = " + gakkaFieldKeta + " majorName x1 = " + posx1 + ", x2 = " + posx2 + ", y = " + field.y());
                        }
                    }

                    if (getFormInfo()._isKumamotoForm) {
                        minnum = 20;
                    } else if (param().isKindaifuzoku() && !"1".equals(_printData._tyousasyo2020)) {
                        minnum = 18;
                    } else {
                        minnum = 24;
                    }

                    if (param().isKindaifuzoku() && !"1".equals(_printData._tyousasyo2020)) {
                        maxnum = 60;
                    } else {
                        maxnum = gakkaFieldKeta;
                    }

                    if (getFormInfo()._isKumamotoForm) {
                        ystart = 684;
                    } else {
                        ystart = field.y();
                    }

                } else {
                    posx1 = field.x();
                    posx2 = (int) (field.x() + KNJSvfFieldModify.fieldWidth(gakkaFieldCharSize, 1, gakkaFieldKeta));
                    if (param()._isOutputDebugField && param().isOutputDebugField(gakkaField)) {
                        log.info(" gakka charSize = " + gakkaFieldCharSize + ", keta = " + gakkaFieldKeta + " majorName x1 = " + posx1 + ", x2 = " + posx2 + ", y = " + field.y());
                    }
                    ystart = field.y();
                    maxnum = gakkaFieldKeta;

                    // 学科フィールド
//                    maxnum = 60;
//                    ystart = 1050;
                    if ("KNJE070_2KIN.frm".equals(getFormInfo()._formName)) {
//                        posx1 = 685;
//                        posx2 = 1618;
                        minnum = 28;
                    } else {
//                        posx1 = 613;
//                        posx2 = 1613;
                        minnum = 30;
                    }
                }

                final int height;
                if (PrintData._shusyokuYou == _printData._output) {
                    height = (int) KNJSvfFieldModify.charHeightPixel(9);
                } else {
                    height = (int) KNJSvfFieldModify.charHeightPixel(12);
                }
                final int xgap;
                if (PrintData._shusyokuYou == _printData._output && _printData._isKyoto) {
                    xgap = 70; // 京都府就職用は、学科名印字を中央割付せずに右へ9ポイント1.5文字分ほど移動
                } else {
                    xgap = 0;
                }
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(gakkaField, posx2 - posx1 - xgap, height, ystart, minnum, maxnum);
                double charSize = modify.getCharSize(majorName);
                if (-1.0f != charSize) {
                    svfVrAttribute(gakkaField, "Size=" + charSize);
                    svfVrAttribute(gakkaField, "Y=" + (int) modify.getYjiku(0, charSize));

                    int offset = 0;
                    if (xgap != 0) {
                        svfVrAttribute(gakkaField, ATTRIBUTE_MUHENSYU); // 編集スタイル=0(無編集)
                        offset = -xgap;
                    } else {
                        // log.debug(modify + " (" + majorName + ") ");
                        offset = modify.getModifiedCenteringOffset(posx1, posx2, maxnum, charSize);
                        if (posx1 - offset < 0) {
                            svfVrAttribute(gakkaField, ATTRIBUTE_MUHENSYU); // 編集スタイル=0(無編集)
                            offset = modify.getModifiedCenteringOffset(posx1, posx2, getMS932ByteLength(majorName), charSize);
                        }
                    }
                    svfVrAttribute(gakkaField, "X=" + (posx1 - offset));

                }
                if (_printData._output == PrintData._shusyokuYou && param()._isShusyokuyouKinkiToitsu) {
                    svfVrAttribute(gakkaField, ATTRIBUTE_MUHENSYU); // 編集スタイル=0(無編集)
                    svfVrAttribute(gakkaField, "X=" + String.valueOf(posx1));
                }
            }

            svfVrsOut(gakkaField,  majorName);
        }

        protected String getPrintMajorname(final PersonInfo personInfo) {
            String majorName;
            if (getFormInfo()._isKumamotoForm || param()._z010.in(Z010Info.Mieken)) {
                final String majorCategoryCd = _printData.getMajorCategoryCd(_printData._personInfo._coursecd, _printData._personInfo._majorcd);
                majorName = defstr(_printData.getMajorNameWithMajorCategoryDat(param(), majorCategoryCd, defstr(personInfo._majorname)));
            } else {
                majorName = defstr(personInfo._majorname);
            }

            final String[] tyousasyoPrintCoursecodenameSplit = Util.splitParam(_printData.property(Property.tyousasyoPrintCoursecodename), " ");
            final boolean isTyousasyoPrintCoursecodename = ArrayUtils.contains(tyousasyoPrintCoursecodenameSplit, "1"); // 学科名欄にコース略称を印字
            final boolean isTyousasyoPrintCoursecodenameSpace = ArrayUtils.contains(tyousasyoPrintCoursecodenameSplit, "space"); // 学科名とコース略称の間にスペースを印字

            if (param()._z010.in(Z010Info.Mieken) || isTyousasyoPrintCoursecodename) {
                final String coursecodeAbbv1 = defstr(personInfo.getString("COURSECODEABBV1"));
                if (isTyousasyoPrintCoursecodenameSpace && !StringUtils.isBlank(coursecodeAbbv1)) {
                    // スペース追加
                    majorName += "　";
                }
                majorName += coursecodeAbbv1;
            }
            return majorName;
        }

        protected String getCertTextCommon() {
            if ("1".equals(_printData._tyousasyo2020)) {
                if (_printData._isKyoto) {
                    return "この調査書の記載事項に誤りがないことを証明する。";
                }
                return "この調査書の記載事項に誤りがないことを証明する";
            } else if (param()._z010.in(Z010Info.Nishiyama)) {
                return "上記の記載事項に誤りがないことを証明する。";
            } else if (param()._z010.in(Z010Info.Sundaikoufu) || param()._isShusyokuyouKinkiToitsu2 || _printData._isKyoto && _printData._isTsushin) {
                return "上記の記載事項に誤りのないことを証明します。";
            }
            return "この調査書の記載事項に誤りがないことを証明する。";
        }

        /**
         *  学校情報
         */
        public void printHeadCommon(final boolean isFormatOnly) {
            if (isFormatOnly) {
                return;
            }
            if (_printData._schoolInfo.hasHeaddata) {
                if (getFormInfo()._isKumamotoForm && !StringUtils.isBlank(_printData._schoolInfo.certifSchoolRemark[6])) {
                    final int keta1 = getMS932ByteLength(_printData._schoolInfo.t4schoolname1);
                    final int keta2 = getMS932ByteLength(_printData._schoolInfo.certifSchoolRemark[6]);
                    final boolean useField3 = keta1 > 30 || keta2 > 30;
                    if (PrintData._shingakuYou == _printData._output) {
                        svfVrsOut("SCHOOL_NAME2_" + (useField3 ? "2" : "1"), _printData._schoolInfo.t4schoolname1);
                        svfVrsOut("SCHOOL_NAME3_" + (useField3 ? "2" : "1"), _printData._schoolInfo.certifSchoolRemark[6]);
                    } else if (PrintData._shusyokuYou == _printData._output) {
                        svfVrsOut("SCHOOL_NAME3_" + (useField3 ? "2" : "1"), _printData._schoolInfo.t4schoolname1);
                        svfVrsOut("SCHOOL_NAME4_" + (useField3 ? "2" : "1"), _printData._schoolInfo.certifSchoolRemark[6]);
                    }
                } else {
                    svfVrsOut("SCHOOL_NAME1", defstr(_printData._schoolInfo.t4schoolname1));
                }

                if (_printData._output == PrintData._shingakuYou) {
                    svfVrsOut("NAMESPARE", _printData._schoolInfo.t4classification);  //学校設立種別
                }
            }
        }

        /**
         *  学校情報
         */
        public void printHeadCommon2(final boolean isFormatOnly) {
            FormRecordPart.printSvfFieldData(this, getHeadCommon2(isFormatOnly));
        }

        /**
         *  学校情報
         */
        public FormRecord getHeadCommon2(final boolean isFormatOnly) {
            final FormRecord formRecord = new FormRecord();
            if (isFormatOnly) {
                return formRecord;
            }
            if (_printData._schoolInfo.hasHeaddata) {
                // 右下
                formRecord.setData("school_name_2", _printData._schoolInfo.schoolname1);

                if (_printData._schoolInfo.schoolzipcd != null) {
                    formRecord.setData("SCHOOLZIP", "〒" + _printData._schoolInfo.schoolzipcd); //郵便番号
                }

                formRecord.setData("CERT_TXT", getCertTextCommon());  //種別

                formRecord.setData("DATE", _printData._schoolInfo.kisaibi);   //記載日

                // 画面のパラメータ"プログラムID=KNJE070"かつ"校長名を出力しない"とき以外に校長名を出力する
                final boolean notOutputPrincipal = _printData.isNotOutputPrincipalName();
                if (!notOutputPrincipal) {
                    formRecord.setData(PrintData._shusyokuYou == _printData._output ? "STAFFNAME" : "STAFFNAME_1", _printData._schoolInfo.principalName);   //校長名
                }

                if (_param.isKindaifuzoku()) {
                    formRecord.setData("PRINCIPAL_JOBNAME", _printData._schoolInfo.principalJobName); // とりあえず近大だけ
                }

                if (_printData._schoolInfo.isPrintSchoolRemark) {
                    printCertifSchoolDatRemark();
                }
            }
            if (_printData._isPrintStamp) {
                formRecord.setImage("STAMP", _printData._certifSchoolstampImagePath); // 校長印影
            }
            if (_printData._isKisaiPrintStamp) {
                formRecord.setImage("KISAI_STAMP", _printData._kisaiStampImagePath); // 記載責任者印影
            }
            return formRecord;
        }

        protected List<String> getPrintCertifSchoolDatRemark() {
            final List<String> rtn = new ArrayList<String>();
            if (!_printData._schoolInfo.isPrintSchoolRemark) {
                return rtn;
            }
            if (param()._z010.in(Z010Info.Meiji) && PrintData._shingakuYou == _printData._output && !"1".equals(_printData._tyousasyo2020)) {
                return rtn;
            }
            if (!param().notHasCertifSchoolDatOrKindai() && !_printData._isPrintCertifSchoolDatRemark123ToField9) {
                // 予備１から予備３を出力
                final List<String> list = _printData.getNotNullCertifRemark123List();
                return list;
            }
            return rtn;
        }

        /**
         * 備考欄下に証明書学校データ備考を表示
         * @param _printData
         */
        private void printCertifSchoolDatRemark() {
            final List<String> list = getPrintCertifSchoolDatRemark();
            // 上から1、2、3。下につめて表示、最大3行
            for (int i = 0; i < list.size(); i++) {
                svfVrsOut("REMARK" + String.valueOf(i + 3 - list.size() + 1), list.get(i));
            }
        }

        protected String getFuhakkouRemark() {
            final String elapsedYears = (String) _printData._outputFuhakkouResult.get("CERTIF_KIND_MST.ELAPSED_YEARS");
            final String remark;
            if (param()._z010.in(Z010Info.Chiben)) {
                remark = "法令で定められた保存期間（卒業後" + defstr(elapsedYears) + "年）が経過しているため、\n"
                       + "学籍および修得単位数に関する記録以外は記載していません。";
            } else {
                remark = "法令で定められた保存期間（卒業後" + defstr(elapsedYears) + "年）が経過しているため、証明できません。";
            }
            return remark;
        }
    }

    private static enum RecordDiv {
        TOTALSTUDY_ACT,
        TOTALSTUDY_VAL,
        SPECIALACT,
        SHOJIKOU,
        BIKO,
        ATTEND,
        CERTIF_SCHOOL
        ;

    }

    private static class FormRecordPart {
        static final String Data = "_FormRecord.Data";
        static final String Attribute = "_FormRecord.Attribute";

        int _lineMax1;
        int _lineMax12;

        int _linex = 0;                          //行数

        final FormRecord _fieldData = new FormRecord();
        final List<FormRecord> _recordList = new ArrayList<FormRecord>();

        KNJSvfFieldInfo _classnameInfo = new KNJSvfFieldInfo();
        int _clx1End;
        int _clWidth;

        KNJSvfFieldInfo _subclassnameInfo = new KNJSvfFieldInfo();
        int _subcx1End;
        int _subcWidth;

        private FormRecord createRecord(final int idx) {
            final FormRecord record = new FormRecord();
            record._debugIdx = idx;
            _recordList.add(record);
            return record;
        }

        private int getMaxPage1(final PrintData printData) {
            final int total = _recordList.size() + printData._form1SuraList.size();
            return Math.max(1, total / _lineMax12 + (total % _lineMax12 == 0 ? 0 : 1));
        }

        private int getMaxPage2() {
            return Math.max(1, _recordList.size() / _lineMax12 + (_recordList.size() % _lineMax12 == 0 ? 0 : 1));
        }

        private List<FormRecord> getPageRecordList(final int pageIdx) {
            final int recordStart = pageIdx * _lineMax12;
            final List<FormRecord> pageRecordList;
            if (recordStart < _recordList.size()) {
                final int recordEnd = Math.min((pageIdx + 1) * _lineMax12, _recordList.size());
                pageRecordList = _recordList.subList(recordStart, recordEnd);
            } else {
                pageRecordList = Collections.emptyList();
            }
            return pageRecordList;
        }

        private static void printSvfRecordList(final IForm form, final List<FormRecord> recordList) {
            for (int i = 0; i < recordList.size(); i++) {
                final FormRecord record = recordList.get(i);
                printSvfFieldData(form, record);
                form.svfVrEndRecord();
            }
        }

        private static void printSvfFieldData(final IForm form, final FormRecord fieldData) {
            for (final Map.Entry<String, String> e : fieldData._dataMap.entrySet()) {
                final String field = e.getKey();
                form.svfVrsOut(field, e.getValue());
            }
            for (final Map.Entry<Tuple<String, Integer>, String> e : fieldData._datanMap.entrySet()) {
                final String field = e.getKey()._first;
                form.svfVrsOutn(field, e.getKey()._second, e.getValue());
            }
            for (final Map.Entry<String, List<String>> e : fieldData._attrMap.entrySet()) {
                final String field = e.getKey();
                form.svfVrAttribute(field, Util.mkString(e.getValue(), ","));
            }
            for (final Map.Entry<Tuple<String, Integer>, List<String>> e : fieldData._attrnMap.entrySet()) {
                form.svfVrAttributen(e.getKey()._first, e.getKey()._second, Util.mkString(e.getValue(), ","));
            }
        }
    }

    private static class FormKNJE070_1 extends IForm {
        static final String ITEMfield = "ITEM";
        static final String TOTAL_CREDITfield = "TOTAL_CREDIT";
        static final String TOTAL_CREDIT2field = "TOTAL_CREDIT2";
        static final String TOTAL_CREDIT_DOTfield = "TOTAL_CREDIT_DOT";
        static final String TOTAL_CREDIT_HANKIfield = "TOTAL_CREDIT_HANKI";
        static final String TOTAL_CREDIT_DOT_HANKIfield = "TOTAL_CREDIT_DOT_HANKI";

        static final String FIELD_LESSON = "LESSON";
        static final String FIELD_SPECIAL = "SPECIAL";
        static final String FIELD_ABROAD = "ABROAD";
        static final String FIELD_PRESENT = "PRESENT";
        static final String FIELD_ABSENCE = "ABSENCE";
        static final String FIELD_ATTEND = "ATTEND";

        boolean hasdata;
        private final FormInfoKNJE070_1 _formInfo;

        public FormKNJE070_1(final Param param, final FormInfoKNJE070_1 formInfo) {
            super(param);
            _formInfo = formInfo;
        }

        protected static class FormInfoKNJE070_1 extends FormInfo {
            private boolean _is6nenyouKenja = false;
            private boolean _isShingakuyouKenja = false;

            /** 進学用の出欠の記録のタイトルをフルで印字するならtrue */
            protected boolean _isShingakuyouFormShukketsuTitleFormat2;
            /** 進学用成績のレコードの文字が大きく行数が少ないフォームならtrue */
            boolean _isShingakuyouFormRecordFormat2;
            boolean _is2page;
            boolean _isShojikouMatome;
            /** <code>_is2page</code>がtrueの場合、1ページ(左側)のフォーム名 */
            String _formNameLeft;
            /** <code>_is2page</code>がtrueの場合、2ページ(右側)のフォーム名 */
            String _formNameRight;

            ShokenType _shokenType6;
            ShokenType _shokenType7;
            ShokenType _shokenType8;
            ShokenType _shokenType9;
            protected int _field9RightX;

            FormInfoKNJE070_1(final PrintData printData, final Param param) {
                super(param);
            }

            protected ShokenType shokenType6() {
                return Util.defObject(_shokenType6, _shokenType);
            }

            protected ShokenType shokenType7() {
                return Util.defObject(_shokenType7, _shokenType);
            }

            protected ShokenType shokenType8() {
                return Util.defObject(_shokenType8, _shokenType);
            }

            protected ShokenType shokenType9() {
                return Util.defObject(_shokenType9, _shokenType);
            }

            public int[] getField7moji(final PrintData printData) {
                final int moji1, moji2, moji3;
                final String prop = StringUtils.defaultString(printData.property(Property.train_ref_1_2_3_field_sizeForPrint), printData.property(Property.train_ref_1_2_3_field_size));
                final String[] split = StringUtils.split(prop, "-");
                if (null != split && split.length >= 3) {
                    moji1 = Util.toInt(split[0], 14);
                    moji2 = Util.toInt(split[1], 14);
                    moji3 = Util.toInt(split[2], 14);
                } else if ("2".equals(prop)) {
                    moji1 = 21;
                    moji2 = 21;
                    moji3 = 7;
                } else if ("1".equals(prop)) {
                    moji1 = 14;
                    moji2 = 21;
                    moji3 = 7;
                } else {
                    moji1 = 14;
                    moji2 = 14;
                    moji3 = 14;
                }
                if (_param._isOutputDebug) {
                    log.info(" field7 moji1 = " + moji1 + ", moji2 = " + moji2 + ", moji3 = " + moji3);
                }
                return new int[] {moji1, moji2, moji3};
            }

            public int[] getField7Keta(final PrintData printData) {
                final int[] rtn = getField7moji(printData);
                for (int i = 0; i < rtn.length; i++) {
                    rtn[i] *= 2;
                }
                return rtn;
            }

            public boolean trainRef123FieldSize1(final PrintData printData) {
                return getField7Keta(printData)[1] == 42;
            }

            public void setSvfForm(final Vrw32alp svf, final String _formName, final IForm iform, final Param param) {
                _field9RightX = -1;
                _guardAddress1EndX = -1;
                try {

                    final String formPath = svf.getPath(_formName);
                    final File formFile = new File(formPath);
                    if (formFile.exists()) {
                        SvfForm svfForm = new SvfForm(formFile);
                        svfForm._debug = param._isOutputDebugSvfForm;
                        if (svfForm.readFile()) {
                            final SvfField field9 = iform.getField("field9");
                            if (null != field9) {
                                final Map field9Attributes = field9.getAttributeMap();
                                final SvfForm.Point field9topleft = pt(Integer.parseInt((String) field9Attributes.get("X")), Integer.parseInt((String) field9Attributes.get("Y")));
                                final SvfForm.Line field9UpperLine = svfForm.getNearestUpperLine(field9topleft);
                                _field9RightX = field9UpperLine._end._x;
                            }

                            SvfForm.Field guardAddress1 = svfForm.getField("GUARD_ADDRESS1");
                            if (null != guardAddress1) {
                                _guardAddress1EndX = guardAddress1._endX;
                            }
                        }
                    }
                    if (param._isOutputDebugField) {
                        log.info(" _field9RightX = " + _field9RightX);
                        log.info(" _guardAddress1EndX = " + _guardAddress1EndX);
                    }
                } catch (Throwable t) {
                    log.error("catch throwed : _formName = " + _formName, t);
                }
            }

            final String FLG_TANKYU1 = "TANKYU1";
            final String FLG_SOGO_SURA_REC1 = "SOGO_SURA_REC1";
            final String FLG_SOGO_SURA_SHOKEN_A4IGAI = "SOGO_SURA_SHOKEN_A4IGAI";
            final String FLG_ABROAD_SURA = "ABROAD_SURA";
            final String FLG_ATTEND_SURA = "ATTEND_SURA";
            final String FLG_ATTEND_HIROKOU_TUSHIN_SURA = "FLG_ATTEND_HIROKOU_TUSHIN_SURA";
            final String FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN = "FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN";
            final String FLG_ATTENDREMARK_SURA = "ATTENDREMARK_SURA";
            final String FLG_KISAI_INEI = "FLG_KISAI_INEI";
            final String FLG_HANKI_NINTEI = "FLG_HANKI_NINTEI";
            final String FLG_CREDIT_FIELD_DOT = "FLG_CREDIT_FIELD_DOT";
            final String FLG_STAMP_FIELD_SIZE_MODIFY = "FLG_STAMP_FIELD_SIZE_MODIFY";
            final String FLG_KISAI_STAMP_FIELD_SIZE_MODIFY = "FLG_KISAI_STAMP_FIELD_SIZE_MODIFY";
            final String FLG_TITLE_SHUKKOU = "FLG_TITLE_SHUKKOU";
            final String FLG_A4_HEADER_NAME = "FLG_A4_HEADER_NAME";
            final String FLG_A4_TOTALSTUDY_TITLE_Y = "FLG_A4_TOTALSTUDY_TITLE_Y";
            final String FLG_A4_TOTALSTUDY_ACT_TITLE_Y = "FLG_A4_TOTALSTUDY_ACT_TITLE_Y";
            final String FLG_A4_TOTALSTUDY_VAL_TITLE_Y = "FLG_A4_TOTALSTUDY_VAL_TITLE_Y";
            final String FLG_A4_SPACT_TITLE_Y = "FLG_A4_SPACT_TITLE_Y";
            final String FLG_A4_SHOJIKOU_TITLE_Y = "FLG_A4_SYOJIKOU_TITLE_Y";
            final String FLG_A4_BIKO_TITLE = "FLG_A4_BIKO_TITLE";
            final String FLG_A4_SOGAKU_ACT_SLASH = "FLG_A4_SOGAKU_ACT_SLASH";
            final String FLG_A4_SOGAKU_VAL_SLASH = "FLG_A4_SOGAKU_VAL_SLASH";
            final String FLG_A4_SVF_KINDAI_ADJUST = "FLG_A4_SVF_KINDAI_ADJUST";
            final String FLG_A4_KINDAI_CERTIF_HEADER = "FLG_A4_KINDAI_CERTIF_HEADER";
            final String FLG_A4_HEADER = "FLG_A4_HEADER";
            final String FLG_A4_PAGE = "FLG_A4_PAGE";
            final String FLG_A4_NAME = "FLG_A4_NAME";
            final String FLG_ADD_COURSENAME2 = "FLG_ADD_COURSENAME2";
            final String FLG_A4_STUDENT_ADDRESS_CENTERING = "FLG_A4_STUDENT_ADDRESS_CENTERING";
            final String FLG_A4_NOT_PRINT_INN_MARK_INNER_STAMP = "FLG_A4_NOT_PRINT_INN_MARK_INNER_STAMP";
            final String FLG_A4_ADD_ITEM_FIELD = "FLG_A4_ADD_ITEM_FIELD";
            final String FLG_A4_FORM_HOJO_ATT = "FLG_A4_FORM_HOJO_ATT";
            final String FLG_NNEN_SET = "FLG_NNEN_SET";
            final String FLG_NNEN_SET4 = "FLG_NNEN_SET4";
            final String FLG_A4_FORM1_TITLE_TATEGAKI = "FLG_A4_FORM1_TITLE_TATEGAKI";
            final String FLG_KUMAMOTO_A4_FORM23_SHOJIKOU_LINES = "FLG_KUMAMOTO_A4_FORM23_SHOJIKOU_LINES";
            final String FLG_A4_ADD_DUMMY9 = "FLG_A4_ADD_DUMMY9";

            public String setConfigFormShingakuyou(final Vrw32alp svf,
                    String formName,
                    final PrintData printData,
                    final TitlePage titlePage,
                    final boolean isLast,
                    final List<FormRecord> recordList,
                    final FormKNJE070_1_A4.FormA4ConfigParam configParam
                    ) {
                final SvfForm.Field.RepeatConfig repeatConfig = null; // 謎のnot used
                final Param param = _param;

                SvfForm svfForm = null;
                try {
                    // 進学用
                    final String formPath = svf.getPath(formName);
                    final File formFile = new File(formPath);
                    if (_param._isOutputDebug) {
                        log.info(" form path = " + formPath);
                    }
                    if (formFile.exists()) {
                        svfForm = new SvfForm(formFile);
                        svfForm._debug = param._isOutputDebugSvfForm;
                        if (!svfForm.readFile()) {
                            svfForm = null;
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                    svfForm = null;
                } finally {
                    if (null == svfForm) {
                        return formName;
                    }
                }
                final TreeMap<String, String> modifyFormFlgMap = getModifyFormFlgMap(formName, svfForm, printData, titlePage, isLast, configParam);

                String modifyFormKey = Util.mkString(modifyFormFlgMap, "|");
                if (!StringUtils.isEmpty(modifyFormKey)) {
                    modifyFormKey = formName + "::" + modifyFormKey;
                }
                if (param._isOutputDebug) {
                    log.info(" check config form = " + modifyFormKey + ", (new? " + !param.modifyFormPathMap().containsKey(modifyFormKey) + " / " + param.modifyFormPathMap().get(modifyFormKey) + " / " + formName + "), configParam = " + configParam);
                }
                if (StringUtils.isEmpty(modifyFormKey)) {
                    return formName;
                }
                if (param.modifyFormPathMap().containsKey(modifyFormKey)) {
                    File file = param.modifyFormPathMap().get(modifyFormKey);
                    formName = file.getName();
                    return formName;
                }

                File newFile = null;
                try {
                    // 進学用
                    modifyFormShingakuyou(printData, configParam, recordList, param, modifyFormFlgMap, svfForm);

                    modifyFormShingakuyouShojikou6bunkatsu(printData, configParam, param, modifyFormFlgMap, svfForm);

                    newFile = svfForm.writeTempFile();

                    if (param._isOutputDebug || param._isOutputDebugSvfForm || param._isOutputDebugSvfFormCreate || param._isOutputDebugSvfFormModify) {
                        log.info(" create form " + newFile.getAbsolutePath());
                    }
                } catch (Throwable e) {
                    if (param._isOutputDebug) {
                        log.error("throwed ", e);
                    } else {
                        log.error("throwed " + e.getMessage());
                    }
                }
                File newFormFile = null;
                if (null != newFile) {
                    newFormFile = newFile;
                }
                param.modifyFormPathMap().put(modifyFormKey, newFormFile);
                if (null != newFormFile && !newFormFile.getName().equals(formName)) {
                    formName = newFormFile.getName();
                }
                return formName;
            }

            private TreeMap<String, String> getModifyFormFlgMap(final String form, final SvfForm svfForm, final PrintData printData, final TitlePage titlePage, final boolean isLast, final FormKNJE070_1_A4.FormA4ConfigParam configParam) {

                final Param param = _param;
                final boolean isA4 = "1".equals(printData._tyousasyo2020);
                final boolean isSeisekiForm = !isA4 || null != configParam && configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._1);
                final boolean isSogakuForm = !isA4 || null != configParam && configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2, FormKNJE070_1_A4.FORM_KIND._2_3_2RECORD, FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD, FormKNJE070_1_A4.FORM_KIND._2PAGE_2);
                final boolean isAttendForm = !isA4 || null != configParam && (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU, FormKNJE070_1_A4.FORM_KIND._2PAGE_2) || null != configParam._attendFormStartY);

                final TreeMap<String, String> modifyFormFlgMap = new TreeMap<String, String>();

                if (KNJE070_1.FormKNJE070_1_A4.FormInfoKNJE070_1_A4.NNEN_FORM.equals(form)) {
                    modifyFormFlgMap.put(FLG_NNEN_SET, String.valueOf(_formNenYou._intval));
                } else if (KNJE070_1.FormKNJE070_1_A4.FormInfoKNJE070_1_A4.NNEN_FORM4.equals(form)) {
                    modifyFormFlgMap.put(FLG_NNEN_SET4, String.valueOf(_formNenYou._intval));
                }

                if (isSogakuForm && PrintData.SOGOTEKI_NA_TANKYU_NO_JIKAN.equals(printData.getSogoSubclassname(param))) {
                    modifyFormFlgMap.put(FLG_TANKYU1, "1");
                }
                if (isAttendForm && printData._isKisaiPrintStamp && null != printData._kisaiStampImagePath) {
                    modifyFormFlgMap.put(FLG_KISAI_INEI, "1");
                }
                if (param._z010.in(Z010Info.naraken) && !isA4) {
                    if (printData.sogoIsSuraShingakuyou("setConfigForm", param, "NO_KEY")) {
                        if (isSeisekiForm && isLast && PrintData.isNoSogoCredit(printData._suramap)) {
                            modifyFormFlgMap.put(FLG_SOGO_SURA_REC1, "1");
                        }

                        final List<String> sogakuRemarkSuraPosList = new ArrayList<String>();
                        if (isSogakuForm) {
                            if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani)) && !isA4) {
                                for (final Title title : printData.titleValues()) {
                                    final HexamEntremarkDat hed = printData.getHexamEntremarkDat(_param, printData.titleValues(), title._year);
                                    final boolean isSlash = null == hed || StringUtils.isBlank(hed.datTotalstudyact()) && StringUtils.isBlank(hed.datTotalstudyval());
                                    if (isSlash) {
                                        sogakuRemarkSuraPosList.add(String.valueOf(title._pos));
                                    }
                                }
                            } else {
                                final boolean isSlash = StringUtils.isBlank(getString(printData._hexamEntremarkHdat, "TOTALSTUDYACT")) && StringUtils.isBlank(getString(printData._hexamEntremarkHdat, "TOTALSTUDYVAL"));
                                if (isSlash) {
                                    sogakuRemarkSuraPosList.add(String.valueOf("0"));
                                }
                            }
                            if (!sogakuRemarkSuraPosList.isEmpty()) {
                                modifyFormFlgMap.put(FLG_SOGO_SURA_SHOKEN_A4IGAI, Util.mkString(sogakuRemarkSuraPosList, "_"));
                            }
                        }
                    }

                    if (isSeisekiForm && isLast && printData._abroadCreditIsSura) {
                        modifyFormFlgMap.put(FLG_ABROAD_SURA, "1");
                    }
                }

                if (param._z010.in(Z010Info.Kumamoto) && Arrays.asList(FormKNJE070_1_A4.FORM_KNJE070_A4_3NEN_2, FormKNJE070_1_A4.FORM_KNJE070_A4_3NEN_3).contains(form)) {
                    modifyFormFlgMap.put(FLG_KUMAMOTO_A4_FORM23_SHOJIKOU_LINES, "1");
                }

                if (isSeisekiForm && !_formNenYou.in(FormNen._6, FormNen._8) && "1".equals(printData.property(Property.tyousasyo2020seisekiTitleTategaki))) {
                    modifyFormFlgMap.put(FLG_A4_FORM1_TITLE_TATEGAKI, "1");
                }

                if (isAttendForm) {
                    // 出欠備考
                    final List<String> attendRemarkSuraPosList = new ArrayList<String>();
                    for (final Title title : printData.titleValues()) {
                        boolean attendRemarkIsSlash = attendRemarkIsSlash(printData, param, title);
                        if (attendRemarkIsSlash) {
                            attendRemarkSuraPosList.add(String.valueOf(title._pos));
                        }
                    }
                    if (!attendRemarkSuraPosList.isEmpty()) {
                        modifyFormFlgMap.put(FLG_ATTENDREMARK_SURA, Util.mkString(attendRemarkSuraPosList, "_"));
                    }

                    final List<String> fieldAll = Arrays.asList(FIELD_LESSON, FIELD_SPECIAL, FIELD_ABROAD, FIELD_PRESENT, FIELD_ABSENCE, FIELD_ATTEND);
                    // 出欠欄
                    final List<String> attendSuraPosList = new ArrayList<String>();
                    for (final Title title : titlePage._titleList) {
                        if (printData._isSagakenTsushin || printData._isConfigFormAttendAllSlash || printData._isMiyagikenTsushin) {
                            final AttendrecDat att = printData.getAttendrecDat(title._year);
                            final List<String> suraFieldList = new ArrayList<String>();
                            if (AttendrecDat.isNull(att, printData)) {
                                suraFieldList.addAll(fieldAll);
                            } else {
                                log.info(" attend " + title._year + " = " + att);
                                if (!printData._isConfigFormAttendAllSlash) {
                                    if (Util.toInt(att.attend1(), 0) == 0) {
                                        suraFieldList.add(FIELD_LESSON);
                                    }
                                    if (Util.toInt(att.suspMour(), 0) == 0) {
                                        suraFieldList.add(FIELD_SPECIAL);
                                    }
                                    if (Util.toInt(att.abroad(), 0) == 0) {
                                        suraFieldList.add(FIELD_ABROAD);
                                    }
                                    if (Util.toInt(att.requirepresent(), 0) == 0) {
                                        suraFieldList.add(FIELD_PRESENT);
                                    }
                                    if (Util.toInt(att.attend6(), 0) == 0) {
                                        suraFieldList.add(FIELD_ABSENCE);
                                    }
                                    if (Util.toInt(att.present(), 0) == 0) {
                                        suraFieldList.add(FIELD_ATTEND);
                                    }
                                }
                            }
                            if (suraFieldList.size() > 0) {
                                attendSuraPosList.add(String.valueOf(title._pos) + ":" + Util.mkString(suraFieldList, "-"));
                            }
                        }
                    }
                    if (!attendSuraPosList.isEmpty()) {
                        modifyFormFlgMap.put(FLG_ATTEND_SURA, Util.mkString(attendSuraPosList, "_"));
                    }
                }

                if (isSeisekiForm) {
                    if (_hankiNinteiTaiou && printData._isHankiNinteiForm) {
                        modifyFormFlgMap.put(FLG_HANKI_NINTEI, "1");
                    }
                    if (param._setSogakuKoteiTanni) {
                        modifyFormFlgMap.put(FLG_CREDIT_FIELD_DOT, "1");
                    }
                }

                if (isAttendForm && printData._isPrintStamp) {
                    modifyFormFlgMap.put(FLG_STAMP_FIELD_SIZE_MODIFY, "1");
                }

                if (isAttendForm && printData._isKisaiPrintStamp) {
                    modifyFormFlgMap.put(FLG_KISAI_STAMP_FIELD_SIZE_MODIFY, "1");
                }

                if (isAttendForm && "1".equals(printData.property(Property.useTitleShukkou))) {
                    modifyFormFlgMap.put(FLG_TITLE_SHUKKOU, "1");
                }

                if (isSeisekiForm) {
                    if (null == svfForm.getField("katei2_1")) {
                        modifyFormFlgMap.put(FLG_ADD_COURSENAME2, "1");
                    }
                }
                if (isA4) {
                    modifyFormFlgMap.put(FLG_A4_PAGE, "1");
                    if (isSeisekiForm) {
                        modifyFormFlgMap.put(FLG_A4_NAME, "1");
                        modifyFormFlgMap.put(FLG_A4_ADD_ITEM_FIELD, "1");
                    }
                    if (isAttendForm) {
                        modifyFormFlgMap.put(FLG_A4_FORM_HOJO_ATT, "1");
                    }

                    if (printData._tyousasho2020CertifnoPage.contains("all")
                     || printData._tyousasho2020CertifnoPage.contains("seiseki") && configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._1)
                     || printData._tyousasho2020CertifnoPage.contains("shoken") && configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2, FormKNJE070_1_A4.FORM_KIND._3_SHOJIKOU, FormKNJE070_1_A4.FORM_KIND._2_3_2RECORD, FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD, FormKNJE070_1_A4.FORM_KIND._2PAGE_2)
                     || printData._tyousasho2020CertifnoPage.contains("shukketsu") && configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU, FormKNJE070_1_A4.FORM_KIND._2PAGE_2)
                     ) {
                        modifyFormFlgMap.put(FLG_A4_HEADER, "1");
                    }

                    if (param.isKindaifuzoku()) {
                        modifyFormFlgMap.put(FLG_A4_SVF_KINDAI_ADJUST, "1");
                        if (isSeisekiForm) {
                            modifyFormFlgMap.put(FLG_A4_KINDAI_CERTIF_HEADER, "1");
                        }
                    }

                    if (isSeisekiForm) {
                        // 住所2を出力する場合、（上の郵便番号フィールドの出力は現状無いため表示が下寄りになるので）1.5行分上に移動する
                        if ("1".equals(printData._personInfo._addrFlg) && !StringUtils.isEmpty(defstr(printData._personInfo._addr2))) {
                            modifyFormFlgMap.put(FLG_A4_STUDENT_ADDRESS_CENTERING, "1");
                        }
                    }

                    if (printData._isHirokoudaiTsushin) {
                        if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU)) {
                            final List<String> attendSuraPosList = new ArrayList<String>();
                            for (final Title title : printData.titleValues()) {

                                final AttendrecDat att = printData.getAttendrecDat(title._year);
                                final HexamEntremarkDat dat = printData.getHexamEntremarkDat(_param, printData.titleValues(), title._year);
                                if (null == att || att.isEmpty() || "0".equals(att.schoolcd())) {
                                    attendSuraPosList.add(String.valueOf(title._pos));
                                    if (null == dat || StringUtils.isBlank(attendrecRemark(dat, param, printData, title))) {
                                        attendSuraPosList.add(String.valueOf(title._pos + "R"));
                                    }
                                }
                            }
                            if (!attendSuraPosList.isEmpty()) {
                                final String flg;
                                if (configParam._is6nen8nen) {
                                    flg = FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN;
                                } else {
                                    flg = FLG_ATTEND_HIROKOU_TUSHIN_SURA;
                                }
                                modifyFormFlgMap.put(flg, Util.mkString(attendSuraPosList, "_"));
                            }
                        }
                    }
                    if (!isSeisekiForm && configParam._formkind != FormKNJE070_1_A4.FORM_KIND._2PAGE_2) {
                        if (null != configParam._a4Page) {
                            final boolean isPrintNameWhenPageEven = "1".equals(printData.property(Property.tyousasho2020PrintHeaderName)) || "check".equals(printData.property(Property.tyousasho2020PrintHeaderName)) && "1".equals(printData.getParameter(Parameter.KNJE070D_PRINTHEADERNAME));
                            if (configParam._a4Page % 2 == 1 || isPrintNameWhenPageEven && configParam._a4Page % 2 == 0) {
                                modifyFormFlgMap.put(FLG_A4_HEADER_NAME, "1");
                            }
                        }
                    }

                    if (isSogakuForm) {
                        // 総合的な学習の時間
                        // 京都府はprintData.sogoIsSuraShingakuyou(...) == trueなら斜線
                        // 賢者はprintData.substitutionNotice90IsBlank(...) == trueかつ総学の修得単位数がなければ斜線
                        if (printData._isA4SogakuShokenActSlash) {
                            modifyFormFlgMap.put(FLG_A4_SOGAKU_ACT_SLASH, "1");
                        }
                        if (printData._isA4SogakuShokenValSlash) {
                            modifyFormFlgMap.put(FLG_A4_SOGAKU_VAL_SLASH, "1");
                        }
                    }
                    if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2_3_2RECORD, FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD)) {
                        if (null != configParam._totalstudyRecordY1) {
                            modifyFormFlgMap.put(FLG_A4_TOTALSTUDY_TITLE_Y, configParam._totalstudyRecordY1.toString());
                        }
                        if (null != configParam._totalstudyActRecordY1) {
                            modifyFormFlgMap.put(FLG_A4_TOTALSTUDY_ACT_TITLE_Y, configParam._totalstudyActRecordY1.toString());
                        }
                        if (null != configParam._totalstudyValRecordY1) {
                            modifyFormFlgMap.put(FLG_A4_TOTALSTUDY_VAL_TITLE_Y, configParam._totalstudyValRecordY1.toString());
                        }
                        if (null != configParam._specialActRecordStartY1) {
                            modifyFormFlgMap.put(FLG_A4_SPACT_TITLE_Y, configParam._specialActRecordStartY1.toString());
                        }
                        if (null != configParam._a4ShojikouStartY) {
                            modifyFormFlgMap.put(FLG_A4_SHOJIKOU_TITLE_Y, configParam._a4ShojikouStartY.toString());
                        }
                    }
                    if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU, FormKNJE070_1_A4.FORM_KIND._4_BIKOU_ONLY, FormKNJE070_1_A4.FORM_KIND._2PAGE_2) || null != configParam._bikoTitleXY && null != configParam._bikoTitleXY._second) {
                        modifyFormFlgMap.put(FLG_A4_BIKO_TITLE, "1");
                    }
                    if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU, FormKNJE070_1_A4.FORM_KIND._4_BIKOU_ONLY, FormKNJE070_1_A4.FORM_KIND._2PAGE_2)) {
                        if ("1".equals(printData.property(Property.tyousasho2020NotPrintInnMark))) {
                            modifyFormFlgMap.put(FLG_A4_NOT_PRINT_INN_MARK_INNER_STAMP, "1");
                        }
                    }
                    if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._4_BIKOU_SHUKKETSU) && configParam._is6nen8nen) {
                        modifyFormFlgMap.put(FLG_A4_ADD_DUMMY9, "1");
                    }
                }
                return modifyFormFlgMap;
            }

            private static String dupDq(final String s) {
                if (StringUtils.isBlank(s)) {
                    return s;
                }
                return s.replaceAll("\"", "\"\"");
            }

            private void modifyFormShingakuyouShojikou6bunkatsu(final PrintData printData, final FormKNJE070_1_A4.FormA4ConfigParam configParam, final Param param, final TreeMap<String, String> modifyFormFlgMap, final SvfForm svfForm) {
                // A4 ページ追加
                if (modifyFormFlgMap.containsKey(FLG_A4_PAGE)) {
                    int length = 14;
                    int endX = 3184;
                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    int pageY;
                    if (printData._isKyoto) {
                        pageY = 98;
                    } else {
                        pageY = 164;
                    }
                    svfForm.addField(new SvfForm.Field(null, "PAGE", mincho, length, endX, false, pt(2834, pageY), 90, "").setPrintMethod(PrintMethod.MIGITSUME));
                }

                // A4 成績タイトル縦書き
                if (modifyFormFlgMap.containsKey(FLG_A4_FORM1_TITLE_TATEGAKI)) {
                    // :PRINT_TATEGAKI

                    final int method = printData.getPrintGradeTitleMethod(param);
                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    final int keta;
                    if (CommonPrintData.PRINT_ANNUAL == method || CommonPrintData.PRINT_GAKUNEN == method) {
                       keta = 4;
                    } else {
                       keta = 5;
                    }
                    final int charPoint10;
                    final int fieldyEachPlus;
                    final int startyPlusGeta;
                    if (PrintData.PRINT_NENDO == printData.getPrintGradeTitleMethod(param)) {
                        if (param._isSeireki) {
                            charPoint10 = 80;
                            fieldyEachPlus = modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI) ? 30 : 40;
                            startyPlusGeta = -80;
                        } else {
                            charPoint10 = modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI) ? 60 : 80;
                            fieldyEachPlus = modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI) ? 20 : 25;
                            startyPlusGeta = 0;
                        }
                    } else {
                        charPoint10 = modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI) ? 80 : 90;
                        fieldyEachPlus = 3;
                        startyPlusGeta = 5;
                    }
                    final int charHeightPixel = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);
                    final int charWidthPixel = charHeightPixel / 2;
                    final int startyplus = (modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI) ? -70 : -30) + startyPlusGeta;
                    for (int g = 1; g <= _formNenYou._intval; g++) {
                        final String sg = String.valueOf(g);
                        for (final String lr : Util.toStrList(Util.seqInclusive(1, 2))) {
                            final SvfForm.Field srcField1 = svfForm.getField("GRADE" + lr + "_" + sg + "_1");
                            if (null != srcField1) {
                                final SvfForm.Line leftLine = svfForm.getNearestLeftLine(srcField1.getPoint());
                                final SvfForm.Line rightLine = svfForm.getNearestRightLine(srcField1.getPoint());
                                final int x = (leftLine.getPoint()._x + rightLine.getPoint()._x) / 2 - charWidthPixel;
                                final int yokoketa = 4;
                                for (final int yi : Util.seq(0, 3)) {
                                    final int y = srcField1.getPoint()._y + startyplus + yi * (charHeightPixel + fieldyEachPlus);
                                    final String fieldname1 = "GRADE" + lr + "_" + sg + "_4_" + String.valueOf(yi + 1);
                                    svfForm.addField(new SvfForm.Field(null, fieldname1, mincho, keta, y + charHeightPixel /* endY */, true, pt(x, y), charPoint10, "学年タイトル縦書き").setZenkaku(true));
                                    final String fieldname2 = "GRADE" + lr + "_" + sg + "_4_" + String.valueOf(yi + 1) + "_YOKO";
                                    final int yokoX = x - charWidthPixel;
                                    final int yokoY = y + (PrintData.PRINT_NENDO == printData.getPrintGradeTitleMethod(param) ? charHeightPixel / 2 : 0);
                                    svfForm.addField(new SvfForm.Field(null, fieldname2, mincho, yokoketa, yokoX + charWidthPixel * yokoketa /* endX */, false, pt(yokoX, yokoY), charPoint10, "学年タイトル横書き").setPrintMethod(PrintMethod.CENTERING));
                                }
                            }
                        }
                    }
                }
                // A4 ページ追加
                if (modifyFormFlgMap.containsKey(FLG_NNEN_SET) || modifyFormFlgMap.containsKey(FLG_NNEN_SET4)) {

                    final List<RecordDiv> recordDivList = new ArrayList<RecordDiv>();

                    final int nenval;
                    if (modifyFormFlgMap.containsKey(FLG_NNEN_SET)) {
                        nenval = Integer.parseInt(modifyFormFlgMap.get(FLG_NNEN_SET));
                    } else { // if (modifyFormFlgMap.containsKey(FLG_NNEN_SET4)) {
                        nenval = Integer.parseInt(modifyFormFlgMap.get(FLG_NNEN_SET4));
                    }

                    if (modifyFormFlgMap.containsKey(FLG_NNEN_SET)) {
                        recordDivList.add(RecordDiv.TOTALSTUDY_ACT);
                        recordDivList.add(RecordDiv.TOTALSTUDY_VAL);
                        recordDivList.add(RecordDiv.SPECIALACT);
                        recordDivList.add(RecordDiv.SHOJIKOU);
                    }
                    recordDivList.add(RecordDiv.BIKO);
                    recordDivList.add(RecordDiv.ATTEND);
                    recordDivList.add(RecordDiv.CERTIF_SCHOOL);

                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    final int charpoint = 80;
                    final int titleRightX = 291;
                    final int dummyCharpoint = 20;
                    int subFormY2 = 4538;
                    boolean isAddCertifSchoolnameSpace = modifyFormFlgMap.containsKey(FLG_NNEN_SET4);
                    if (isAddCertifSchoolnameSpace) {
                        subFormY2 += FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_SPC_HEIGHT;
                    }
                    final SvfForm.SubForm subForm = new SvfForm.SubForm("SUBFORM1", pt(156, 314), pt(3200, subFormY2), true);
                    svfForm.addSubForm(subForm);

                    if (recordDivList.contains(RecordDiv.TOTALSTUDY_ACT) || recordDivList.contains(RecordDiv.TOTALSTUDY_VAL)) {
                        // 総合的な学習の時間 活動内容
                        final int baseY = subForm._point1._y;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recordHeight = FormKNJE070_1_A4.FORM_RECORD_TOTALSTUDY_HEIGHT;
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_SOGO_R2", recp, pt(subForm.getWidth(), recp._y + recordHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線
                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        final List<Line> lines = new ArrayList<Line>();
                        final Line tate = new SvfForm.Line(subForm._point1.addY(rec._point1._y), subForm._point1.addY(rec._point2._y));
                        lines.add(tate.setX(titleRightX));
                        lines.add(tate.setX(444));
                        for (final SvfForm.Line line : lines) {
                            svfForm.addLine(line);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add line " + line);
                            }
                        }

                        final Field dummy = new SvfForm.Field(null, "_DUMMY", mincho, 1, 6, false, pt(0, baseY + 13), dummyCharpoint, "")
                                .setShiromoji(true)
                                .setMaskEnabled(SvfForm.Field.MaskFlag.GROUP_SUPPRESS, SvfForm.Field.MaskFlag.CHOUFUKUJI_NO_KEISEN_INJISHINAI);
                        final List<Field> fields = new ArrayList<Field>();
                        fields.add(dummy.copyTo("DUMMY5_0_R2").setX(251));
                        fields.add(dummy.copyTo("DUMMY5_1_R2").setX(308));
                        fields.add(dummy.copyTo("DUMMY5_2_R2").setX(462));
                        fields.add(new SvfForm.Field(null, "TOTALSTUDY", mincho, 48 * 2 + 1, 3168, false, pt(475, baseY + 5), 100, "活動内容"));
                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }
                    }

                    if (recordDivList.contains(RecordDiv.SPECIALACT)) {
                        // 特別活動
                        final int baseY = 2148;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recordHeight = FormKNJE070_1_A4.FORM_RECORD_SPECIALACTREMARK_HEIGHT;
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_SP_R2", recp, pt(subForm.getWidth(), recp._y + recordHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線
                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        final List<Line> lines = new ArrayList<Line>();
                        final List<Field> fields = new ArrayList<Field>();

                        lines.add(new SvfForm.Line(subForm._point1.addY(rec._point1._y), subForm._point1.addY(rec._point1._y).setX(subForm._point2._x)));
                        final Line tate = new SvfForm.Line(subForm._point1.addY(rec._point1._y), subForm._point1.addY(rec._point2._y));
                        final int spActMoji;
                        if (nenval == 3 || nenval == 6) {
                            lines.add(tate.setX(titleRightX));
                            lines.add(tate.setX(1257));
                            lines.add(tate.setX(2229));

                            final Field dummy = new SvfForm.Field(null, "RECORD_SP_R2_FLGX", mincho, 1, 6, false, pt(0, baseY + 10), dummyCharpoint, "特別活動の記録レコードフラグ")
                                    .setShiromoji(true)
                                    .setMaskEnabled(SvfForm.Field.MaskFlag.GROUP_SUPPRESS, SvfForm.Field.MaskFlag.CHOUFUKUJI_NO_KEISEN_INJISHINAI);
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG0").setX(238));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG1").setX(1243));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG2").setX(1959));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG3").setX(3173));

                            final SvfForm.Field g = new SvfForm.Field(null, "GRADE4_X_R2", mincho, 12, 876 - 609, false, pt(0, baseY + 5), charpoint, "学年")
                                    .setPrintMethod(SvfForm.Field.PrintMethod.CENTERING);
                            fields.add(g.copyTo("GRADE4_1_R2").setX(609));
                            fields.add(g.copyTo("GRADE4_2_R2").setX(1579));
                            fields.add(g.copyTo("GRADE4_3_R2").setX(2551));

                            spActMoji = 16;
                            final SvfForm.Field f7 = new SvfForm.Field(null, "field7_Xg_R2", mincho, spActMoji * 2, 1212 - 336, false, pt(0, baseY + 2), charpoint, "特別活動の記録");
                            fields.add(f7.copyTo("field7_1g_R2").setX(336));
                            fields.add(f7.copyTo("field7_2g_R2").setX(1306));
                            fields.add(f7.copyTo("field7_3g_R2").setX(2278));

                        } else if (nenval == 4 || nenval == 8) {
                            lines.add(tate.setX(titleRightX));
                            lines.add(tate.setX(984));
                            lines.add(tate.setX(1712));
                            lines.add(tate.setX(2440));

                            final Field dummy = new SvfForm.Field(null, "RECORD_SP_R2_FLGX", mincho, 1, 6, false, pt(0, baseY + 10), dummyCharpoint, "特別活動の記録レコードフラグ")
                                    .setShiromoji(true)
                                    .setMaskEnabled(SvfForm.Field.MaskFlag.GROUP_SUPPRESS, SvfForm.Field.MaskFlag.CHOUFUKUJI_NO_KEISEN_INJISHINAI);
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG0").setX(170));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG1").setX(963));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG2").setX(1684));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG3").setX(2408));
                            fields.add(dummy.copyTo("RECORD_SP_R2_FLG4").setX(3164));

                            final SvfForm.Field g = new SvfForm.Field(null, "GRADE4_X_R2", mincho, 12, 739 - 472, false, pt(0, baseY + 5), charpoint, "学年")
                                    .setPrintMethod(SvfForm.Field.PrintMethod.CENTERING);
                            fields.add(g.copyTo("GRADE4_1_R2").setX(472));
                            fields.add(g.copyTo("GRADE4_2_R2").setX(1183));
                            fields.add(g.copyTo("GRADE4_3_R2").setX(1907));
                            fields.add(g.copyTo("GRADE4_4_R2").setX(2653));

                            spActMoji = 10;
                            final SvfForm.Field f7 = new SvfForm.Field(null, "field7_Xg_R2", mincho, spActMoji * 2, 974 - 325, false, pt(0, baseY + 2), charpoint, "特別活動の記録");
                            fields.add(f7.copyTo("field7_1g_R2").setX(325));
                            fields.add(f7.copyTo("field7_2g_R2").setX(1027));
                            fields.add(f7.copyTo("field7_3g_R2").setX(1750));
                            fields.add(f7.copyTo("field7_4g_R2").setX(2496));
                        }

                        for (final SvfForm.Line line : lines) {
                            svfForm.addLine(line);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add line " + line);
                            }
                        }
                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }
                    }

                    if (recordDivList.contains(RecordDiv.SHOJIKOU)) {
                        // 諸事項
                        final int baseY = 2280;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recordHeight = FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT;
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_SHOJIKOU", recp, pt(subForm.getWidth(), recp._y + recordHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線
                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        final List<Line> lines = new ArrayList<Line>();
                        lines.add(new SvfForm.Line(subForm._point1.addY(rec._point1._y), subForm._point1.addY(rec._point1._y).setX(subForm._point2._x)));
                        final Line tate = new SvfForm.Line(subForm._point1.addY(rec._point1._y), subForm._point1.addY(rec._point2._y));
                        lines.add(tate.setX(titleRightX));
                        lines.add(tate.setX(444));
                        lines.add(tate.setX(1359));
                        lines.add(tate.setX(2287));
                        for (final SvfForm.Line line : lines) {
                            svfForm.addLine(line);
                        }

                        final List<Field> fields = new ArrayList<Field>();
                        final Field dummy = new SvfForm.Field(null, "_DUMMY", mincho, 1, 6, false, pt(0, baseY + 7), dummyCharpoint, "").setShiromoji(true)
                                .setMaskEnabled(SvfForm.Field.MaskFlag.GROUP_SUPPRESS, SvfForm.Field.MaskFlag.CHOUFUKUJI_NO_KEISEN_INJISHINAI);
                        fields.add(new SvfForm.Field(null, "GRADE5", mincho, 4, 416, false, pt(322, baseY + 1), 85, "学年").setPrintMethod(SvfForm.Field.PrintMethod.CENTERING));
                        fields.add(dummy.copyTo("GRP_G").setX(169));
                        fields.add(dummy.copyTo("GRP_H").setX(298));
                        // 横に3分割
                        fields.add(dummy.copyTo("GRP_D1").setX(454));
                        fields.add(dummy.copyTo("GRP_D1_2").setX(1339));
                        fields.add(dummy.copyTo("GRP_D2").setX(1366));
                        fields.add(dummy.copyTo("GRP_D2_2").setX(2266));
                        fields.add(dummy.copyTo("GRP_D3").setX(2293));
                        fields.add(dummy.copyTo("GRP_D3_2").setX(3183));
                        final SvfForm.Field f8 = new SvfForm.Field(null, "field8_REC", mincho, 32, 876, false, pt(0, baseY + 2), charpoint, "指導上参考となる諸事項");
                        fields.add(f8.copyTo("field8_REC_1").setX(460));
                        fields.add(f8.copyTo("field8_REC_2").setX(1380));
                        fields.add(f8.copyTo("field8_REC_3").setX(2307));
                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }
                    }

                    if (recordDivList.contains(RecordDiv.BIKO)) {
                        // 備考
                        final int baseY = 614;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recordHeight = FormKNJE070_1_A4.FORM_RECORD_BIKO_HEIGHT;
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_BIKO", recp, pt(subForm.getWidth(), recp._y + recordHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線
                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        final List<SvfForm.Field> fields = new ArrayList<Field>();
                        fields.add(new SvfForm.Field(null, "field9", mincho, 92, 3150, false, pt(310, baseY + 1), 100, "備考"));
                        fields.add(new SvfForm.Field(null, "field9_BOLD", mincho, 92, 3150, false, pt(310, baseY + 1), 100, "備考"));
                        for (final Tuple<String, Integer> fieldAndX : Arrays.asList(Tuple.of("DUMMY9", 174), Tuple.of("DUMMY9_2", 304), Tuple.of("DUMMY9_3", 450), Tuple.of("DUMMY9_4", 1364), Tuple.of("DUMMY9_5", 2290))) {
                            final String field = fieldAndX._first;
                            final Integer x = fieldAndX._second;
                            fields.add(new SvfForm.Field(null, field, mincho, 1, x + 3, false, pt(x, baseY + 13), 10, "")
                                    .setShiromoji(true).setMaskEnabled(SvfForm.Field.MaskFlag.GROUP_SUPPRESS, SvfForm.Field.MaskFlag.CHOUFUKUJI_NO_KEISEN_INJISHINAI));
                        }

                        final List<SvfForm.Line> lines = new ArrayList<SvfForm.Line>();
                        lines.add(new SvfForm.Line(SvfForm.LineWidth.THIN, pt(titleRightX, baseY), pt(titleRightX, baseY + 66)));

                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }

                        for (final SvfForm.Line line : lines) {
                            svfForm.addLine(line);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add line " + line);
                            }
                        }
                    }

                    if (recordDivList.contains(RecordDiv.ATTEND)) {
                        // 出欠
                        final int baseY = 2815;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recordHeight;
                        if (nenval == 6 || nenval == 8) {
                            recordHeight = FormKNJE070_1_A4.FORM_RECORD_ATTEND_HEIGHT6NEN8NEN;
                        } else {
                            recordHeight = FormKNJE070_1_A4.FORM_RECORD_ATTEND_HEIGHT3NEN4NEN;
                        }
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_ATTEND", recp, pt(subForm.getWidth(), recp._y + recordHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線

                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        if (nenval == 6 || nenval == 8) {
                            svfForm.addKoteiMoji(new KoteiMoji("9.出欠の記録", pt(196, baseY + 10), 120).setFont(mincho).setEndX(596));

                            final int titleEndx;
                            if (nenval == 6) {
                                titleEndx = 560;
                            } else {
                                titleEndx = 480;
                            }

                            final List<KoteiMoji> texts = new ArrayList<KoteiMoji>();
                            for (final Tuple<String, Integer> textAndY : Arrays.asList(
                                    Tuple.of("授業日数"      , 207),
                                    Tuple.of("出席停止・忌引", 305),
                                    Tuple.of("き等の日数"    , 345),
                                    Tuple.of("留学中の"      , 423),
                                    Tuple.of("授業日数"      , 463),
                                    Tuple.of("出席しなければ", 541),
                                    Tuple.of("ならない日数"  , 581),
                                    Tuple.of("欠席日数"      , 681),
                                    Tuple.of("出席日数"      , 799),
                                    Tuple.of("備考"          , 986)
                                    )) {
                                texts.add(new SvfForm.KoteiMoji(textAndY._first, pt(181, baseY + textAndY._second), 80).setFont(mincho).setEndX(titleEndx));
                            }
                            for (final SvfForm.KoteiMoji koteiMoji : texts) {
                                svfForm.addKoteiMoji(koteiMoji);
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" add koteiMoji " + koteiMoji);
                                }
                            }

                        } else if (nenval == 3 || nenval == 4) {
                            svfForm.addKoteiMoji(new KoteiMoji("9.出欠の記録", pt(196, baseY + 8), 120).setFont(mincho).setEndX(596));

                            final List<KoteiMoji> texts1 = new ArrayList<KoteiMoji>();
                            final List<KoteiMoji> texts2 = new ArrayList<KoteiMoji>();

                            final int titleX1 = 169;
                            texts1.add(new KoteiMoji("授業日数", pt(titleX1, baseY + 200), 80));
                            texts1.add(new KoteiMoji("出席停止・忌引", pt(titleX1, baseY + 300), 80));
                            texts1.add(new KoteiMoji("き等の日数", pt(titleX1, baseY + 340), 80));
                            texts1.add(new KoteiMoji("留学中の", pt(titleX1, baseY + 410), 80));
                            texts1.add(new KoteiMoji("授業日数", pt(titleX1, baseY + 450), 80));
                            texts1.add(new KoteiMoji("出席しなければ", pt(titleX1, baseY + 530), 80));
                            texts1.add(new KoteiMoji("ならない日数", pt(titleX1, baseY + 570), 80));
                            for (final SvfForm.KoteiMoji koteiMoji : texts1) {
                                svfForm.addKoteiMoji(koteiMoji.setFont(mincho).setEndX(480));
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" add koteiMoji " + koteiMoji);
                                }
                            }
                            final int titleX2 = 1690;
                            texts2.add(new KoteiMoji("欠席日数", pt(titleX2, baseY + 200), 80));
                            texts2.add(new KoteiMoji("出席日数", pt(titleX2, baseY + 315), 80));
                            texts2.add(new KoteiMoji("備考", pt(titleX2, baseY + 493), 80));
                            for (final SvfForm.KoteiMoji koteiMoji : texts2) {
                                svfForm.addKoteiMoji(koteiMoji.setFont(mincho).setEndX(1994));
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" add koteiMoji " + koteiMoji);
                                }
                            }
                        }

                        // 区分
                        final List<Field> fields = new ArrayList<Field>();
                        if (nenval == 6 || nenval == 8) {
                            final int kubunX;
                            if (nenval == 6) {
                                kubunX = 217;
                            } else {
                                kubunX = 175;
                            }

                            final SvfForm.Field kubun = new SvfForm.Field(null, "KUBUN1", mincho, 12, kubunX + 300, false, pt(kubunX, baseY + 103), 90, "区分＼")
                                    .setHenshuShiki(dupDq("IF(KUBUN1==\"\",\"区分＼学年\",KUBUN1)"))
                                    .setPrintMethod(PrintMethod.CENTERING);
                            fields.add(kubun);
                        } else if (nenval == 3 || nenval == 4) {
                            final SvfForm.Field kubun = new SvfForm.Field(null, "KUBUN1", mincho, 12, 475, false, pt(175, baseY + 96), 90, "区分＼").setHenshuShiki(dupDq("IF(KUBUN1==\"\",\"区分＼学年\",KUBUN1)"))
                                    .setPrintMethod(PrintMethod.CENTERING);
                            fields.add(kubun);
                            fields.add(kubun.copyTo("KUBUN2").setX(1683).setHenshuShiki(dupDq("IF(KUBUN2==\"\",\"区分＼学年\",KUBUN2)")));

                        }

                        // タイトル
                        final int charPoint10 = 100;
                        if (nenval == 6 || nenval == 8) {
                            final int gradex;
                            final int gradeSplitx;

                            if (nenval == 6) {
                                gradex = 675;
                                gradeSplitx = 433;

                            } else { // if (nenval == 8) {
                                gradex = 525;
                                gradeSplitx = 338;
                            }
                            final int gradeEndx = gradex + 276;

                            for (int g = 1; g <= nenval; g++) {
                                final SvfForm.Field grade3 = new SvfForm.Field(null, "GRADE3_" + g + "_1", mincho, 10, gradeEndx + gradeSplitx * (g - 1), false, pt(gradex + gradeSplitx * (g - 1), baseY + 104), charPoint10, "学年")
                                        .setPrintMethod(PrintMethod.CENTERING);
                                fields.add(grade3);
                                fields.add(grade3.copyTo("GRADE3_" + g + "_2"));
                            }

                        } else { // if (nenval == 3 || nenval == 4) {
                            final int gradex;
                            final int gradeSplitx;

                            if (nenval == 3) {
                                gradex = 550;
                                gradeSplitx = 392;

                            } else { // if (nenval == 4) {

                                gradex = 500;
                                gradeSplitx = 296;
                            }
                            final int gradeEndx = gradex + 276;

                            for (int g = 1; g <= nenval; g++) {
                                final SvfForm.Field grade3 = new SvfForm.Field(null, "GRADE3_" + g + "_1", mincho, 10, gradeEndx + gradeSplitx * (g - 1), false, pt(gradex + gradeSplitx * (g - 1), baseY + 96), charPoint10, "")
                                        .setPrintMethod(PrintMethod.CENTERING);
                                fields.add(grade3);
                                fields.add(grade3.copyTo("GRADE3_" + g + "_2"));
                                fields.add(grade3.copyTo("GRADE6_" + g + "_1").addX(1515));
                                fields.add(grade3.copyTo("GRADE6_" + g + "_2").addX(1515));
                            }
                        }

                        // 各項目フィールド
                        final int fieldLength;
                        final int width10p3keta;
                        if (param._z010.in(Z010Info.naraken)) {
                            fieldLength = 7;
                            width10p3keta = 194;
                        } else {
                            fieldLength = 3;
                            width10p3keta = 83;
                        }
                        final int shiftx = - (width10p3keta - 83) / 2;
                        if (nenval == 6 || nenval == 8) {
                            final int fieldx1;
                            final SvfForm.Field.RepeatConfig rc;
                            final int left;
                            final int right;
                            if (nenval == 6) {

                                fieldx1 = 767 + shiftx;

                                left = 759 + shiftx;
                                right = 3026;
                                rc = new SvfForm.Field.RepeatConfig("1", 6, 0, -1, 0).setRepeatPitchPoint(27.57);

                            } else { // if (nenval == 8) {

                                fieldx1 = 621 + shiftx;

                                left = 613 + shiftx;
                                right = 3082;
                                rc = new SvfForm.Field.RepeatConfig("1", 8, 0, -1, 0).setRepeatPitchPoint(21.53);
                            }
                            svfForm.addRepeat(new Repeat(rc._repeatNo, left, baseY + 194, right, baseY + 857, 1, rc._repeatCount, rc._repeatPitch, 0, "1"));

                            for (final Indexed<String> idxField : Indexed.indexed(Arrays.asList(FIELD_LESSON, FIELD_SPECIAL, FIELD_ABROAD, FIELD_PRESENT, FIELD_ABSENCE, FIELD_ATTEND))) {
                                final int y = 202 + 118 * idxField._idx;
                                final String fieldname = idxField._val;
                                final int endX = fieldx1 + width10p3keta;
                                Field field = new SvfForm.Field(null, fieldname, mincho, fieldLength, endX, false, pt(fieldx1, baseY + y), charPoint10, "").setRepeatConfig(rc);
                                field = field.setHenshuShiki(param._z010.in(Z010Info.naraken) ? "" : dupDq("FORMAT2(" + fieldname + ",\"ZZ9\")"));
                                if (param._z010.in(Z010Info.naraken)) {
                                    field = field.setPrintMethod(Field.PrintMethod.MIGITSUME);
                                }
                                fields.add(field);
                            }
                        } else if (nenval == 3 || nenval == 4) {

                            final SvfForm.Field.RepeatConfig rc1;
                            final SvfForm.Field.RepeatConfig rc2;

                            if (nenval == 3) {
                                rc1 = new SvfForm.Field.RepeatConfig("1", 3, 0, -1, 3).setRepeatPitchPoint(24.81);
                                svfForm.addRepeat(new Repeat(rc1._repeatNo, 639 + shiftx, baseY + 185, 1518, baseY + 610, 1, rc1._repeatCount, rc1._repeatPitch, 4, "1"));
                                rc2 = new SvfForm.Field.RepeatConfig("2", 3, 0, -1, 3).setRepeatPitchPoint(25.41);
                                svfForm.addRepeat(new Repeat(rc2._repeatNo, 2154 + shiftx, baseY + 185, 3052, baseY + 374, 1, rc2._repeatCount, rc2._repeatPitch, 0, "1"));

                            } else { // if (nenval == 4) {
                                rc1 = new SvfForm.Field.RepeatConfig("1", 4, 0, -1, 0).setRepeatPitchPoint(18.77);
                                svfForm.addRepeat(new Repeat(rc1._repeatNo, 587 + shiftx, baseY + 185, 1571, baseY + 425, 1, rc1._repeatCount, rc1._repeatPitch, 4, "1"));
                                rc2 = new SvfForm.Field.RepeatConfig("2", 4, 0, -1, 0).setRepeatPitchPoint(18.96);
                                svfForm.addRepeat(new Repeat(rc2._repeatNo, 2105 + shiftx, baseY + 185, 3098, baseY + 374, 1, rc2._repeatCount, rc2._repeatPitch, 0, "1"));
                            }

                            final int fieldx1;
                            final int fieldx2;

                            if (nenval == 3) {

                                fieldx1 = 647 + shiftx;
                                fieldx2 = 2162 + shiftx;

                            } else { // if (nenval == 4) {

                                fieldx1 = 595 + shiftx;
                                fieldx2 = 2113 + shiftx;
                            }

                            final int y1 = 193;
                            final List<SvfForm.Field> valFields = new ArrayList<SvfForm.Field>();
                            // 左
                            for (final Indexed<String> idxField : Indexed.indexed(Arrays.asList(FIELD_LESSON, FIELD_SPECIAL, FIELD_ABROAD, FIELD_PRESENT))) {
                                final int y = baseY + y1 + 118 * idxField._idx;
                                final String fieldname = idxField._val;
                                final int endX = fieldx1 + width10p3keta;
                                valFields.add(new SvfForm.Field(null, fieldname, mincho, fieldLength, endX, false, pt(fieldx1, y), charPoint10, "").setRepeatConfig(rc1));
                            }
                            // 右
                            for (final Indexed<String> idxField : Indexed.indexed(Arrays.asList(FIELD_ABSENCE, FIELD_ATTEND))) {
                                final int y = baseY + y1 + 118 * idxField._idx;
                                final String fieldname = idxField._val;
                                final int endX = fieldx2 + width10p3keta;
                                valFields.add(new SvfForm.Field(null, fieldname, mincho, fieldLength, endX, false, pt(fieldx2, y), charPoint10, "").setRepeatConfig(rc2));
                            }
                            for (SvfForm.Field valField : valFields) {
                                valField = valField.setHenshuShiki(param._z010.in(Z010Info.naraken) ? "" : dupDq("FORMAT2(" + valField._fieldname + ",\"ZZ9\")"));
                                if (param._z010.in(Z010Info.naraken)) {
                                    valField = valField.setPrintMethod(Field.PrintMethod.MIGITSUME);
                                }
                                fields.add(valField);
                            }
                        }

                        // 出欠備考
                        final int attendRemarkFieldLength = 16;
                        if (nenval == 6 || nenval == 8) {
                            final SvfForm.Field.RepeatConfig rcBiko;
                            if (Util.toInt(printData.property(Property.tyousasho2020AttendremarkGyou), 7) == 9) {
                                rcBiko = new SvfForm.Field.RepeatConfig("2", 9, 1, -1, 3).setRepeatPitchPoint(1.60);
                            } else {
                                rcBiko = new SvfForm.Field.RepeatConfig("2", 7, 1, -1, 3).setRepeatPitchPoint(2.10);
                            }
                            svfForm.addRepeat(new Repeat(rcBiko._repeatNo, 632, baseY + 880, 3165, baseY + 1127, 0, rcBiko._repeatCount, rcBiko._repeatPitch, 0, "1"));

                            if (nenval == 6) {

                                for (final Tuple<String, Integer> gradeAndX : Arrays.asList(
                                        Tuple.of("1", 640),
                                        Tuple.of("2", 1072),
                                        Tuple.of("3", 1508),
                                        Tuple.of("4", 1948),
                                        Tuple.of("5", 2384),
                                        Tuple.of("6", 2821))) {
                                    fields.add(new SvfForm.Field(null, "NOTE_" + gradeAndX._first + "g", mincho, attendRemarkFieldLength, gradeAndX._second + 336, false, pt(gradeAndX._second, baseY + 888), 60, "")
                                            .setRepeatConfig(rcBiko));
                                }

                            } else { // if (nenval == 8) {

                                for (final Tuple<String, Integer> gradeAndX : Arrays.asList(
                                        Tuple.of("1", 532),
                                        Tuple.of("2", 867),
                                        Tuple.of("3", 1208),
                                        Tuple.of("4", 1547),
                                        Tuple.of("5", 1886),
                                        Tuple.of("6", 2225),
                                        Tuple.of("7", 2566),
                                        Tuple.of("8", 2899))) {
                                    fields.add(new SvfForm.Field(null, "NOTE_" + gradeAndX._first + "g", mincho, attendRemarkFieldLength, gradeAndX._second + 266, false, pt(gradeAndX._second, baseY + 888), 60, "")
                                            .setRepeatConfig(rcBiko));
                                }
                            }

                        } else if (nenval == 3 || nenval == 4) {

                            final SvfForm.Field.RepeatConfig rcBiko;
                            if (Util.toInt(printData.property(Property.tyousasho2020AttendremarkGyou), 7) == 9) {
                                rcBiko = new SvfForm.Field.RepeatConfig("3", 9, 1, -1, 3).setRepeatPitchPoint(1.60);
                            } else {
                                rcBiko = new SvfForm.Field.RepeatConfig("3", 7, 1, -1, 3).setRepeatPitchPoint(2.04);
                            }

                            if (nenval == 3) {
                                svfForm.addRepeat(new Repeat(rcBiko._repeatNo, 2030, baseY + 396, 3180, baseY + 637, 0, rcBiko._repeatCount, rcBiko._repeatPitch, 0, "1"));
                                for (final Tuple<String, Integer> gradeAndX : Arrays.asList(
                                        Tuple.of("1", 2038),
                                        Tuple.of("2", 2430),
                                        Tuple.of("3", 2838))) {
                                    fields.add(new SvfForm.Field(null, "NOTE_" + gradeAndX._first + "g", mincho, attendRemarkFieldLength, gradeAndX._second + 350, false, pt(gradeAndX._second, baseY + 400), 60, "")
                                            .setRepeatConfig(rcBiko));
                                }
                            } else { // if (nenval == 4) {
                                svfForm.addRepeat(new Repeat(rcBiko._repeatNo, 2030, baseY + 398, 3180, baseY + 653, 0, rcBiko._repeatCount, rcBiko._repeatPitch, 0, "1"));
                                for (final Tuple<String, Integer> gradeAndX : Arrays.asList(
                                        Tuple.of("1", 2022),
                                        Tuple.of("2", 2317),
                                        Tuple.of("3", 2620),
                                        Tuple.of("4", 2917))) {
                                    fields.add(new SvfForm.Field(null, "NOTE_" + gradeAndX._first + "g", mincho, attendRemarkFieldLength, gradeAndX._second + 266, false, pt(gradeAndX._second, baseY + 398), 60, "")
                                            .setRepeatConfig(rcBiko));
                                }
                            }
                        }

                        // 罫線
                        final List<Line> lines = new ArrayList<Line>();
                        final List<Integer> tateXs;
                        final List<Integer> ys;
                        if (nenval == 6 || nenval == 8) {
                            ys = Arrays.asList(90, 170, 288, 406, 524, 642, 760, 878, 1130, 1138);

                            for (final int y : ys) { // 横線
                                lines.add(new SvfForm.Line(rec.getAbsPoint1().addY(y), rec.getAbsPoint1().addY(y).setX(rec.getAbsPoint2()._x)));
                            }

                            if (nenval == 6) {
                                tateXs = Arrays.asList(591, 1026, 1461, 1895, 2330, 2765, 3200);
                            } else { // if (nenval == 8) {
                                tateXs = Arrays.asList(494, 832, 1171, 1509, 1847, 2186, 2525, 2863, 3200);
                            }
                            for (final int x : tateXs) { // 縦線
                                if (x != 3200) {
                                    lines.add(new SvfForm.Line(rec.getAbsPoint1().addY(90).setX(x), rec.getAbsPoint2().addY(-8).setX(x)));
                                }
                            }

                        } else { // if (nenval == 3 || nenval == 4) {
                            final List<Integer> ys1 = Arrays.asList(81, 161, 279, 397, 633);
                            final List<Integer> ys2 = Arrays.asList(515);
                            ys = new ArrayList<Integer>();
                            ys.addAll(ys1);
                            ys.addAll(ys2);

                            for (final int y : ys1) { // 横線
                                lines.add(new SvfForm.Line(rec.getAbsPoint1().addY(y), rec.getAbsPoint1().addY(y).setX(rec.getAbsPoint2()._x)));
                            }
                            final int centerX = 1678;
                            for (final int y : ys2) { // 横線
                                lines.add(new SvfForm.Line(rec.getAbsPoint1().addY(y), rec.getAbsPoint1().addY(y).setX(centerX)));
                            }

                            if (nenval == 3) {
                                tateXs = Arrays.asList(493, 885, 1276, centerX, 2015, 2406, 2807, 3200);

                            } else { // if (nenval == 4) {
                                tateXs = Arrays.asList(493, 787, 1081, 1374, centerX, 2005, 2304, 2603, 2901, 3200);
                            }

                            for (final int x : tateXs) { // 縦線
                                if (x != 3200) {
                                    lines.add(new SvfForm.Line(rec.getAbsPoint1().addY(81).setX(x), rec.getAbsPoint2().addY(-8).setX(x)));
                                }
                            }
                        }

                        // 斜線
                        if (modifyFormFlgMap.containsKey(FLG_ATTEND_SURA)) {
                            final Map<String, int[]> poinst = new HashMap<String, int[]>();
                            poinst.put(FIELD_LESSON, new int[] {ys.get(1), ys.get(2)});
                            poinst.put(FIELD_SPECIAL, new int[] {ys.get(2), ys.get(3)});
                            if (nenval == 6 || nenval == 8) {
                                poinst.put(FIELD_ABROAD, new int[] {ys.get(3), ys.get(4)});
                                poinst.put(FIELD_PRESENT, new int[] {ys.get(4), ys.get(5)});
                                poinst.put(FIELD_ABSENCE, new int[] {ys.get(5), ys.get(6)});
                                poinst.put(FIELD_ATTEND, new int[] {ys.get(6), ys.get(7)});
                            } else { // if (nenval == 3 || nenval == 4) {
                                poinst.put(FIELD_ABROAD, new int[] {ys.get(3), ys.get(5)});
                                poinst.put(FIELD_PRESENT, new int[] {ys.get(5), ys.get(4)});
                                poinst.put(FIELD_ABSENCE, new int[] {ys.get(1), ys.get(2)});
                                poinst.put(FIELD_ATTEND, new int[] {ys.get(2), ys.get(3)});
                            }

                            for (final String posfields : modifyFormFlgMap.get(FLG_ATTEND_SURA).split("_")) {
                                final String pos = StringUtils.split(posfields, ":")[0];
                                final String[] slashFields = StringUtils.split(StringUtils.split(posfields, ":")[1], "-");

                                for (final String fieldname : slashFields) {
                                    final int[] pointstys = poinst.get(fieldname);
                                    final int pi = Integer.parseInt(pos);
                                    final int i1, i2;

                                    if (nenval == 6 || nenval == 8) {
                                        i1 = pi - 1;
                                        i2 = pi;

                                    } else { // if (nenval == 3 || nenval == 4) {

                                        i1 = pi - 1 + (Arrays.asList(FIELD_ABSENCE, FIELD_ATTEND).contains(fieldname) ? nenval + 1 : 0);
                                        i2 = pi + (Arrays.asList(FIELD_ABSENCE, FIELD_ATTEND).contains(fieldname) ? nenval + 1 : 0);
                                    }

                                    final int x = i1 < tateXs.size() ? tateXs.get(i1) : -1;
                                    final int x2 = i2 < tateXs.size() ? tateXs.get(i2) : -1;
                                    if (null != pointstys) {
                                        SvfForm.Line slash = new SvfForm.Line(SvfForm.LineWidth.THINEST, rec.getAbsPoint1().addY(pointstys[0]).setX(x2), rec.getAbsPoint1().addY(pointstys[1]).setX(x));
                                        if (param._isOutputDebugSvfFormModify) {
                                            log.info(" add  slash " + fieldname + " " + slash);
                                        }
                                        lines.add(slash);
                                    }
                                }
                            }
                        }

                        for (final SvfForm.Line line : lines) {
                            svfForm.addLine(line);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add line " + line);
                            }
                        }
                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }
                    }

                    if (recordDivList.contains(RecordDiv.CERTIF_SCHOOL)) {
                        // 校長名
                        final int baseY = 3968;
                        final SvfForm.Point recp = pt(0, baseY - subForm._point1._y);
                        final int recHeight;
                        if (isAddCertifSchoolnameSpace) {
                            recHeight = FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_HEIGHT + FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_SPC_HEIGHT;
                        } else {
                            recHeight = FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_HEIGHT;
                        }
                        final SvfForm.Record rec = new SvfForm.Record("RECORD_CERTIF_SCHOOL", recp, pt(subForm.getWidth(), recp._y + recHeight))
                                .setRecordFlagEnabled(SvfForm.Record.RecordFlag.ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI)
                                .setEachWakuOptions(LineOptionIndex.TOP, LineOption.of(LineKind.SOLID, LineWidth.THIN)); // 細線
                        svfForm.addSubFormRecords(subForm, rec);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add record " + rec);
                        }

                        final List<SvfForm.KoteiMoji> texts = new ArrayList<KoteiMoji>();
                        final List<SvfForm.Field> fields = new ArrayList<Field>();
                        final List<SvfForm.Box> boxes = new ArrayList<SvfForm.Box>();

                        fields.add(new SvfForm.Field(null, "CERT_DUMMY", mincho, 2, 270, false, pt(266, baseY + 10), 10, "出力ダミー").setShiromoji(true));
                        fields.add(new SvfForm.Field(null, "CERT_TXT", mincho, 80, 2710, false, pt(266, baseY + 26), 110, "証明文言").setHenshuShiki(dupDq("IF(CERT_TXT=\"\",\"上記の記載事項に誤りがないことを証明します\",CERT_TXT)")));

                        // 日付
                        final int dateY;
                        if (isAddCertifSchoolnameSpace) {
                            dateY = baseY + 105;
                        } else {
                            dateY = baseY + 100;
                        }
                        fields.add(new SvfForm.Field(null, "DATE", mincho, 16, 805, false, pt(361, dateY), 100, "証明月日"));
                        // 学校名
                        final int schnameY;
                        if (isAddCertifSchoolnameSpace) {
                            schnameY = baseY + 190;
                        } else {
                            schnameY = baseY + 180;
                        }
                        texts.add(new SvfForm.KoteiMoji("学校名", pt(253, schnameY), 90).setEndX(453));
                        fields.add(new SvfForm.Field(null, "school_name_2", mincho, 60, 2333, false, pt(499, schnameY - 6), 110, "学校名"));

                        // 学校所在地
                        final int schAddrY;
                        if (isAddCertifSchoolnameSpace) {
                            schAddrY = baseY + 318;
                        } else {
                            schAddrY = baseY + 258;
                        }
                        texts.add(new SvfForm.KoteiMoji("所在地", pt(253, schAddrY), 90).setEndX(453));
                        fields.add(new SvfForm.Field(null, "SCHOOLZIP", mincho, 10, 745, false, pt(509, schAddrY + 2), 85, "学校・郵便番号"));
                        fields.add(new SvfForm.Field(null, "school_address1", mincho, 72, 2975, false, pt(775, schAddrY - 5), 110, "学校所在地"));
                        fields.add(new SvfForm.Field(null, "school_address2", mincho, 100, 3136, false, pt(775, schAddrY + 2), 85, "学校所在地"));
                        fields.add(new SvfForm.Field(null, "school_address3", mincho, 120, 3142, false, pt(775, schAddrY + 6), 71, "学校所在地"));

                        // 校長名
                        final int staffY;
                        if (isAddCertifSchoolnameSpace) {
                            staffY = baseY + 458;
                        } else {
                            staffY = baseY + 406;
                        }
                        texts.add(new SvfForm.KoteiMoji("校長名", pt(253, staffY), 90).setEndX(453));
                        fields.add(new SvfForm.Field(null, "STAFFNAME_1", mincho, 60, 2509, false, pt(509, staffY - 8), 120, "校長名"));
                        // 記載責任者職名
                        texts.add(new SvfForm.KoteiMoji("記載責任者職氏名", pt(1554, staffY), 90).setEndX(1954));
                        fields.add(new SvfForm.Field(null, "JOBNAME", mincho, 12, 2000, false, pt(1948, baseY + 20), 10, "記載責任者職名").setShiromoji(true).setMaskEnabled(SvfForm.Field.MaskFlag.MASK)); // 白文字
                        fields.add(new SvfForm.Field(null, "STAFFNAME_2", mincho, 30, 2500, false, pt(2321, baseY + 20), 10, "記載責任者氏名").setShiromoji(true).setMaskEnabled(SvfForm.Field.MaskFlag.MASK)); // 白文字
                        fields.add(new SvfForm.Field(null, "STAFFNAME_SHOW", mincho, 34, 3092, false, pt(2025, staffY - 8), 120, "記載責任者氏名").setHenshuShiki("JOBNAME + \"\"　\"\" + STAFFNAME_2"));
                        // 「印」と枠
                        if (!modifyFormFlgMap.containsKey(FLG_A4_NOT_PRINT_INN_MARK_INNER_STAMP)) {
                            texts.add(new SvfForm.KoteiMoji("印", pt(1186, staffY + 3), 80).setEndX(1230));
                            boxes.add(new SvfForm.Box(SvfForm.LineKind.DOTTED1, SvfForm.LineWidth.THINEST, pt(1179, staffY - 4), pt(1237, staffY + 54)));
                        }
                        texts.add(new SvfForm.KoteiMoji("印", pt(2998, staffY + 3), 80).setEndX(3042));
                        boxes.add(new SvfForm.Box(SvfForm.LineKind.DOTTED1, SvfForm.LineWidth.THINEST, pt(2991, staffY - 4), pt(3049, staffY + 54)).setCornerBits(15).setCornerRadius(25));

                        for (final SvfForm.Field field : fields) {
                            svfForm.addField(field);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add field " + field);
                            }
                        }

                        for (final SvfForm.KoteiMoji koteiMoji : texts) {
                            svfForm.addKoteiMoji(koteiMoji);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add koteiMoji " + koteiMoji);
                            }
                        }

                        for (final SvfForm.Box box : boxes) {
                            svfForm.addBox(box);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add box " + box);
                            }
                        }
                    }
                }

                // A4 ページ追加
                if (modifyFormFlgMap.containsKey(FLG_A4_FORM_HOJO_ATT)) {
                    for (int i = 1; i <= 6; i++) {
                        final String fieldnameSrc = "GRADE3_" + i + "_1";

                        if (null != svfForm.getField(fieldnameSrc)) {
                            final SvfForm.Field fieldSrc = svfForm.getField(fieldnameSrc);

                            final String fieldnameDest = "GRADE3_" + i + "_2";

                            final SvfForm.Field fieldDestChk = svfForm.getField(fieldnameDest);
                            if (null == fieldDestChk) {
                                final SvfForm.Field fieldDest = fieldSrc.copyTo(fieldnameDest);
                                svfForm.addField(fieldDest);
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" add field : " + fieldDest);
                                }
                            }
                        }

                        final String fieldnameSrcC = "GRADE6_" + i + "_1";

                        if (null != svfForm.getField(fieldnameSrcC)) {
                            final SvfForm.Field fieldSrc = svfForm.getField(fieldnameSrcC);

                            final String fieldnameDest = "GRADE6_" + i + "_2";

                            final SvfForm.Field fieldDestChk = svfForm.getField(fieldnameDest);
                            if (null == fieldDestChk) {
                                final SvfForm.Field fieldDest = fieldSrc.copyTo(fieldnameDest).setHenshuShiki("GRADE3_" + i + "_2");
                                svfForm.addField(fieldDest);
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" add field : " + fieldDest);
                                }
                            }
                        }

                    }
                }
                // A4 氏名サイズ拡張
                if (modifyFormFlgMap.containsKey(FLG_A4_NAME)) {
                    for (final List<String> srcDest : Arrays.asList(Arrays.asList("KANA", "NAME"), Arrays.asList("KANA2", "NAME2"), Arrays.asList("KANA3", "NAME3"))) {
                        final String src = srcDest.get(0);
                        final String dest = srcDest.get(1);
                        final SvfForm.Field srcField = svfForm.getField(src);
                        final SvfForm.Field destField = svfForm.getField(dest);
                        if (null != srcField && null!= destField) {
                            svfForm.removeField(destField);
                            svfForm.addField(destField.setCharPoint10(srcField._charPoint10).setEndX(srcField._endX).setFieldLength(srcField._fieldLength).setX(srcField._position._x));
                        }
                    }
                }
                // A4 課程名2追加
                if (modifyFormFlgMap.containsKey(FLG_ADD_COURSENAME2)) {
                    final SvfForm.Field kateiField = svfForm.getField("katei");
                    final int x = kateiField._position._x;
                    final int y = kateiField._position._y;
                    final int endX = kateiField._endX;
                    svfForm.addField(kateiField.copyTo("katei2_1").setCharPoint10(100).setEndX(endX).setFieldLength(12).setX(x).setY(y - 26).setPrintMethod(PrintMethod.HIDARITSUME).setLinkFieldname("katei2_2"));
                    svfForm.addField(kateiField.copyTo("katei2_2").setCharPoint10(100).setEndX(endX).setFieldLength(12).setX(x).setY(y + 33).setPrintMethod(PrintMethod.HIDARITSUME));
                }
                // A4 ヘッダ追加
                if (modifyFormFlgMap.containsKey(FLG_A4_HEADER)) {
                    int length;
                    int endX;
                    length = 130;
                    endX = 517;
                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    int certifNameY = 0;
                    svfForm.addField(new SvfForm.Field(null, "SYOSYO_NAME_2020", mincho, length, endX, false, pt(156, 20), 10, "").setShiromoji(true)); // 編集式用
                    if (printData._isKyoto) {
                        certifNameY = 164;
                    } else {
                        certifNameY = 40;
                    }
                    length = 112;
                    endX = 3204;
                    svfForm.addField(new SvfForm.Field(null, "CERTIF_NAME_2020", mincho, length, endX, false, pt(103, certifNameY), 100, "").setHenshuShiki("SYOSYO_NAME_2020"));
                    length = 8;
                    endX = 114;
                    svfForm.addField(new SvfForm.Field(null, "SCHREGNO", mincho, length, endX, false, pt(88, 28), 10, "").setShiromoji(true)); // DUMMY
                }

                if (modifyFormFlgMap.containsKey(FLG_A4_SVF_KINDAI_ADJUST)) {
                    svfForm.checkKindai();
                }
                if (modifyFormFlgMap.containsKey(FLG_A4_KINDAI_CERTIF_HEADER)) {
                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    int length;
                    int endX;
                    length = 5;
                    endX = 445;
                    svfForm.addField(new SvfForm.Field(null, "CERTIF_NO", mincho, length, endX, false, pt(334, 135), 10, "").setShiromoji(true));
                    length = 45;
                    endX = 1407;
                    svfForm.addField(new SvfForm.Field(null, "NENDO_NAME", mincho, length, endX, false, pt(157, 32), 100, "").setHenshuShiki("\"近高調　第 \" + IF(CERTIF_NO=\"\",\"                \",CERTIF_NO) + \" 号\"".replace("\"", "\"\"")));
                }


                // A4 生徒住所 センタリング
                if (modifyFormFlgMap.containsKey(FLG_A4_STUDENT_ADDRESS_CENTERING)) {
                    for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                        if (field._fieldname.contains("GUARD_ADDRESS")) {
                            svfForm.removeField(field);
                            final Field movedField = field.setY(field._position._y - 50);
                            if (_param._isOutputDebugField) {
                                log.info(" move field : " + movedField);
                            }
                            svfForm.addField(movedField);
                        }
                    }
                }

                // A4 奇数ページヘッダーに氏名欄
                if (modifyFormFlgMap.containsKey(FLG_A4_HEADER_NAME)) {
                    final int boxY1 = 105;
                    final int boxY2 = 203;
                    final SvfForm.LineKind solid = SvfForm.LineKind.SOLID;
                    final SvfForm.LineWidth lineWidth = SvfForm.LineWidth.width(1);
                    int point10;
                    point10 = 100;
                    svfForm.addBox(new SvfForm.Box(solid, lineWidth, pt(156, boxY1), pt(904, boxY2)));
                    final int vlineX = 304;
                    svfForm.addLine(new SvfForm.Line(true, solid, lineWidth, pt(vlineX, boxY1), pt(vlineX, boxY2))); // 縦棒
                    svfForm.addKoteiMoji(new SvfForm.KoteiMoji("氏名", pt(178, boxY1 + 23), point10).setFText(true));

                    final SvfForm.Font mincho = SvfForm.Font.Mincho;
                    int keta;
                    int endX;
                    int fieldx;
                    fieldx = 332;
                    keta = 20;
                    point10 = 100;
                    endX = 887;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME", mincho, keta, endX, false, pt(fieldx, boxY1 + 23), point10, ""));
                    fieldx = 332;
                    keta = 30;
                    point10 = 66;
                    endX = 882;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME2", mincho, keta, endX, false, pt(fieldx, boxY1 + 33), point10, ""));

                    fieldx = 316;
                    endX = 882;
                    keta = 36;
                    point10 = 58;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_36KETA", mincho, keta, endX, false, pt(fieldx, boxY1 + 37), point10, ""));

                    // 2行表示用
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_36KETA2L_1", mincho, keta, endX, false, pt(fieldx, boxY1 + 16), point10, ""));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_36KETA2L_2", mincho, keta, endX, false, pt(fieldx, boxY1 + 55), point10, ""));

                    // リンクフィールド
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_36KETA2L_N", mincho, keta, endX, false, pt(fieldx, boxY1 + 16), point10, "").setLinkFieldname("STUDENT_NAME_36KETA2L_N2"));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_36KETA2L_N2", mincho, keta, endX, false, pt(fieldx, boxY1 + 50), point10, ""));

                    keta = 40;
                    point10 = 52;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_40KETA", mincho, keta, endX, false, pt(fieldx, boxY1 + 40), point10, ""));

                    // 2行表示用
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_40KETA2L_1", mincho, keta, endX, false, pt(fieldx, boxY1 + 18), point10, ""));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_40KETA2L_2", mincho, keta, endX, false, pt(fieldx, boxY1 + 55), point10, ""));

                    // リンクフィールド
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_40KETA2L_N", mincho, keta, endX, false, pt(fieldx, boxY1 + 18), point10, "").setLinkFieldname("STUDENT_NAME_40KETA2L_N2"));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_40KETA2L_N2", mincho, keta, endX, false, pt(fieldx, boxY1 + 55), point10, ""));

                    fieldx = 312;
                    endX = 895;
                    keta = 50;
                    point10 = 42;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_50KETA", mincho, keta, endX, false, pt(fieldx, boxY1 + 40), point10, ""));

                    // 2行表示用
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_50KETA2L_1", mincho, keta, endX, false, pt(fieldx, boxY1 + 19), point10, ""));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_50KETA2L_2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));

                    // リンクフィールド
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_50KETA2L_N", mincho, keta, endX, false, pt(fieldx, boxY1 + 19), point10, "").setLinkFieldname("STUDENT_NAME_50KETA2L_N2"));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_50KETA2L_N2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));

                    fieldx = 312;
                    endX = 895;
                    keta = 60;
                    point10 = 35;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_60KETA", mincho, keta, endX, false, pt(fieldx, boxY1 + 40), point10, ""));

                    // 2行表示用
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_60KETA2L_1", mincho, keta, endX, false, pt(fieldx, boxY1 + 22), point10, ""));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_60KETA2L_2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));

                    // リンクフィールド
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_60KETA2L_N", mincho, keta, endX, false, pt(fieldx, boxY1 + 22), point10, "").setLinkFieldname("STUDENT_NAME_60KETA2L_N2"));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_60KETA2L_N2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));

                    fieldx = 312;
                    endX = 895;
                    keta = 84;
                    point10 = 25;
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_84KETA", mincho, keta, endX, false, pt(fieldx, boxY1 + 40), point10, ""));

                    // 2行表示用
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_84KETA2L_1", mincho, keta, endX, false, pt(fieldx, boxY1 + 30), point10, ""));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_84KETA2L_2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));

                    // リンクフィールド
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_84KETA2L_N", mincho, keta, endX, false, pt(fieldx, boxY1 + 30), point10, "").setLinkFieldname("STUDENT_NAME_84KETA2L_N2"));
                    svfForm.addField(new SvfForm.Field(null, "STUDENT_NAME_84KETA2L_N2", mincho, keta, endX, false, pt(fieldx, boxY1 + 60), point10, ""));
                }

                final int recordTitleX1 = 74;
                final String recordTitleSpace = "　　　　　　　　　　　　　　　　　　　";
                // A4 総学レコードタイプで文言を追加する
                int charPoint10Sougaku = 120;
                if (null != configParam) {
                    if (null != configParam._totalstudyRecordY1) {
                        final String text0 = (modifyFormFlgMap.containsKey(FLG_TANKYU1) ? PrintData.SOGOTEKI_NA_TANKYU_NO_JIKAN : PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN) + "の内容・評価";
                        int diffx = 0;
                        final String text = "　" + text0;
                        int charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10Sougaku / 10.0);
                        int height = text.length() * charHeight;
                        int endX = recordTitleX1 - charHeight * 3 / 2 + 20; // 半角3文字分
                        // log.info(" set endx " + endX);

                        int y = configParam._totalstudyRecordY1._first;
                        final int y2 = configParam._totalstudyRecordY1._second;
                        int endY;

                        if (height > y2 - y) {
                            charPoint10Sougaku = 110;
                            charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10Sougaku / 10.0);
                            height = text.length() * charHeight;
                            diffx = 5;
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" set endx2 " + endX);
                            }
                            if (height > y2 - y) {

                                if (param._isOutputDebugSvfFormModify) {
                                    log.warn(" text = " + text + ", range = " + configParam._totalstudyRecordY1 + ", " + (y2 - y) + ", height = " + height);
                                }

                                charPoint10Sougaku = 80;
                                charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10Sougaku / 10.0);

                                final List<Character> charList = new ArrayList<Character>();
                                for (final char ch : text0.toCharArray()) {
                                    charList.add(ch);
                                }
                                final List<List<Character>> split = Util.splitByCount(charList, 3);
                                final int maxLine = split.size();
                                final int step = 6;
                                final int h = (y2 - y) / (maxLine + step + 1);
                                svfForm.addKoteiMoji(new SvfForm.KoteiMoji("　5.", pt(recordTitleX1 + 70, y + h * (step / 2 - 1)), 110).setEndX(endX)); // 前にスペースを追加してレコードに巻き込ませない
                                for (int li = 0; li < maxLine; li++) {
                                    final List<Character> line = split.get(li);
                                    height = text.length() * charHeight;
                                    final StringBuffer stb = new StringBuffer();
                                    for (final Character ch : line) {
                                        stb.append(ch);
                                    }
                                    svfForm.addKoteiMoji(new SvfForm.KoteiMoji(stb.toString(), pt(recordTitleX1 + 82, y + 120 + h * (li + (step / 2 - 1))), charPoint10Sougaku).setEndX(endX)); // 後にスペースを追加してレコードに巻き込ませない
                                }

                            } else {
                                endY = y + height;

                                //log.warn(" text = " + text + ", range = " + configParam._totalstudyRecordY1 + ", " + (y2 - y) + ", height = " + height);
                                svfForm.addKoteiMoji(new SvfForm.KoteiMoji("　　5.", pt(recordTitleX1 + diffx, y), charPoint10Sougaku).setEndX(endX)); // 前にスペースを追加してレコードに巻き込ませない
                                svfForm.addKoteiMoji(new SvfForm.KoteiMoji(text + recordTitleSpace, pt(189, y + 8), charPoint10Sougaku).setVertical(true).setEndX(endY)); // 後にスペースを追加してレコードに巻き込ませない
                            }

                        } else {
                            final int spc = ((y2 - y) - height) / 2;
                            y += spc;
                            endY = y2 - spc;

                            //log.warn(" text = " + text + ", range = " + configParam._totalstudyRecordY1 + ", " + (y2 - y) + ", height = " + height);
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji("　　5.", pt(recordTitleX1 + diffx, y), charPoint10Sougaku).setEndX(endX)); // 前にスペースを追加してレコードに巻き込ませない
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(text + recordTitleSpace, pt(189, y + 8), charPoint10Sougaku).setVertical(true).setEndX(endY)); // 後にスペースを追加してレコードに巻き込ませない
                        }

                        if (modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_ACT_SLASH) || modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_VAL_SLASH)) {
                            if (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2_3_2RECORD, FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD)) {
                                final SvfForm.Field totalStudyField = svfForm.getField("TOTALSTUDY");
                                int x1 = 0;
                                int x2 = 0;
                                if (null != totalStudyField) {
                                    final Line leftLine = svfForm.getNearestLeftLine(totalStudyField._position);
                                    if (null != leftLine) {
                                        x1 = leftLine._start._x;
                                    }
                                    final SvfForm.Record rec = svfForm.getRecordOfField(totalStudyField);
                                    final SubForm subForm = null == rec ? null : rec.getSubForm();
                                    if (null != subForm) {
                                        if (0 == x1) {
                                            x1 = subForm._point1._x;
                                        }
                                        x2 = subForm._point2._x;
                                    }

                                    if (modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_ACT_SLASH)) {
                                        if (null != configParam._totalstudyActRecordY1) {
                                            final int tY1 = configParam._totalstudyActRecordY1._first;
                                            final int tY2 = configParam._totalstudyActRecordY1._second;
                                            final Line slashLine = new Line(SvfForm.LineWidth.THINEST, pt(x2, tY1), pt(x1, tY2));
                                            svfForm.addLine(slashLine);
                                        }
                                    }
                                    if (modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_VAL_SLASH)) {
                                        if (null != configParam._totalstudyValRecordY1) {
                                            final int tY1 = configParam._totalstudyValRecordY1._first;
                                            final int tY2 = configParam._totalstudyValRecordY1._second;
                                            final Line slashLine = new Line(SvfForm.LineWidth.THINEST, pt(x2, tY1), pt(x1, tY2));
                                            svfForm.addLine(slashLine);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // A4 総学レコードタイプで文言を追加する
                    if (null != configParam._totalstudyActRecordY1) {
                        final String text = "学習内容";
                        final int charPoint10 = charPoint10Sougaku <= 100 ? 100 : 120;
                        final int diffx = charPoint10 == 100 ? 10 : 0;
                        final int charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);

                        int y = configParam._totalstudyActRecordY1._first;
                        final int y2 = configParam._totalstudyActRecordY1._second;
                        final int height = text.length() * charHeight;
                        final int endY;
                        if (height >= y2 - y) {
                            if (param._isOutputDebugSvfFormModify) {
                                log.warn(" text = " + text + ", range " + (y2 - y) + ", height = " + height);
                            }
                            endY = y + height;
                        } else {
                            final int spc = ((y2 - y) - height) / 2;
                            y += spc;
                            endY = y2 - spc;
                        }
                        final SvfForm.KoteiMoji koteiMoji = new SvfForm.KoteiMoji(text + recordTitleSpace, pt(189 + 140 + diffx, y), charPoint10).setVertical(true).setEndX(endY);
                        if (param._isOutputDebugSvfFormModify) {
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add act koteiMoji : " + koteiMoji);
                            }
                        }
                        svfForm.addKoteiMoji(koteiMoji); // 後にスペースを追加してレコードに巻き込ませない
                    }
                    // A4 総学レコードタイプで文言を追加する
                    if (null != configParam._totalstudyValRecordY1) {
                        final String text = "評価";
                        final int charPoint10 = charPoint10Sougaku <= 100 ? 100 : 120;
                        final int diffx = charPoint10 == 100 ? 10 : 0;
                        final int charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);

                        int y = configParam._totalstudyValRecordY1._first;
                        final int y2 = configParam._totalstudyValRecordY1._second;
                        final int height = text.length() * charHeight;
                        final int endY;
                        if (height >= y2 - y) {
                            if (param._isOutputDebugSvfFormModify) {
                                log.warn(" text = " + text + ", range " + (y2 - y) + ", height = " + height);
                            }
                            endY = y + height;
                        } else {
                            final int spc = ((y2 - y) - height) / 2;
                            y += spc;
                            endY = y2 - spc;
                        }
                        final SvfForm.KoteiMoji koteiMoji = new SvfForm.KoteiMoji(text + recordTitleSpace, pt(189 + 140 + diffx, y), charPoint10).setVertical(true).setEndX(endY);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add val koteiMoji : " + koteiMoji);
                        }
                        svfForm.addKoteiMoji(koteiMoji); // 後にスペースを追加してレコードに巻き込ませない
                    }

                    // A4 レコードタイプで特別活動文言を追加する
                    if (null != configParam._specialActRecordStartY1) {
                        final String text = "　特別活動の記録";
                        int charPoint10 = 120;
                        int charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);

                        int x = recordTitleX1;
                        int y = configParam._specialActRecordStartY1._first;
                        final int y2 = configParam._specialActRecordStartY1._second;
                        int height = text.length() * charHeight;
                        final int endY;
                        if (height > y2 - y) {
                            charPoint10 = 110;
                            charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);
                            height = text.length() * charHeight;
                            if (param._isOutputDebugSvfFormModify) {
                                log.warn(" text = " + text + ", range " + (y2 - y) + ", height = " + height);
                            }
                            x -= 27;
                            endY = y + height;
                        } else {
                            final int spc = ((y2 - y) - height) / 2;
                            y += spc;
                            endY = y2 - spc;
                        }
                        final int endX = 274;
                        final SvfForm.KoteiMoji moji1 = new SvfForm.KoteiMoji("　　6.", pt(x, y), charPoint10).setEndX(endX); // 前にスペースを追加してレコードに巻き込ませない
                        final SvfForm.KoteiMoji moji2 = new SvfForm.KoteiMoji(text + recordTitleSpace, pt(189, y + 8), charPoint10).setVertical(true).setEndX(endY); // 後にスペースを追加してレコードに巻き込ませない
                        if (param._isOutputDebug) {
                            log.info(" add koteiMoji : " + moji1 + ", " + moji2);
                        }

                        for (final KoteiMoji moji : Arrays.asList(moji1, moji2)) {
                            svfForm.addKoteiMoji(moji);
                            if (param._isOutputDebugSvfFormModify) {
                                log.info(" add text " + moji);
                            }
                        }
                    }
                }

                // A4 レコードタイプで諸事項文言を追加する
                if (modifyFormFlgMap.containsKey(FLG_A4_SHOJIKOU_TITLE_Y)) {
                    final int charPoint10 = 120;
//            					final int charHeight = (int) KNJSvfFieldModify.charHeightPixel(charPoint10 / 10.0);

                    final int y = Util.toInt(modifyFormFlgMap.get(FLG_A4_SHOJIKOU_TITLE_Y), 0);
                    final int endX = 274;
                    final KoteiMoji text1 = new SvfForm.KoteiMoji("　　7.", pt(recordTitleX1, y), charPoint10).setEndX(endX); // 前にスペースを追加してレコードに巻き込ませない
                    final int endY = recordTitleSpace.length() * 50 + y + 808;
                    final KoteiMoji text2 = new SvfForm.KoteiMoji("　指導上参考となる諸事項" + recordTitleSpace, pt(189, y + 8), charPoint10).setVertical(true).setEndX(endY); // 後にスペースを追加してレコードに巻き込ませない

                    for (final KoteiMoji text : Arrays.asList(text1, text2)) {
                        svfForm.addKoteiMoji(text);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add text " + text);
                        }
                    }
                }

                // A4 備考レコードに文言を追加する
                if (modifyFormFlgMap.containsKey(FLG_A4_BIKO_TITLE)) {
                    final int charPoint10 = configParam._formkind == FormKNJE070_1_A4.FORM_KIND._2PAGE_2 ? 90 : 120;
                    final int x;
                    final int y;
                    if (null != configParam._bikoTitleXY && null != configParam._bikoTitleXY._first) {
                        x = configParam._bikoTitleXY._first.intValue();
                    } else {
                        x = 195;
                    }
                    if (null != configParam._bikoTitleXY && null != configParam._bikoTitleXY._second) {
                        y = configParam._bikoTitleXY._second.intValue();
                    } else {
                        y = 400;
                    }
                    final int x_2 = x - (configParam._formkind == FormKNJE070_1_A4.FORM_KIND._2PAGE_2 ? 10 : 14);
                    final int endX = 120;
                    final KoteiMoji text1 = new SvfForm.KoteiMoji("8.", pt(x, y), charPoint10).setEndX(endX).setFText(true);
                    final int endY = y + (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2PAGE_2) ? 150 : configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD) ? 200 : 300);
                    final int gapY = (configParam._formkind.in(FormKNJE070_1_A4.FORM_KIND._2PAGE_2 , FormKNJE070_1_A4.FORM_KIND._2_3_4_2RECORD) ? 70 : 100);
                    final KoteiMoji text2 = new SvfForm.KoteiMoji("備考", pt(x_2, y + gapY), charPoint10).setVertical(true).setEndX(endY).setFText(true);

                    for (final KoteiMoji text : Arrays.asList(text1, text2)) {
                        svfForm.addKoteiMoji(text);
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" add text " + text);
                        }
                    }
                }

                // A4 広工大通信制
                if (modifyFormFlgMap.containsKey(FLG_ATTEND_HIROKOU_TUSHIN_SURA) || modifyFormFlgMap.containsKey(FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN)) {
                    final boolean is6nen8nen = modifyFormFlgMap.containsKey(FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN);
                    final String key = is6nen8nen ? FLG_ATTEND_HIROKOU_TUSHIN_SURA_6NEN_8NEN : FLG_ATTEND_HIROKOU_TUSHIN_SURA;
                    final String val = modifyFormFlgMap.get(key);
                    for (final String pos : val.split("_")) {
                        if (param._isOutputDebugSvfFormModify) {
                            log.info(" pos " + pos + " / " + val);
                        }
                        if (pos.endsWith("R")) {
                            final StringBuffer num = new StringBuffer();
                            for (int i = 0; i < pos.length(); i++) {
                                if (Character.isDigit(pos.charAt(i))) {
                                    num.append(pos.charAt(i));
                                }
                            }
                            addSlashLineOfField(svfForm, "NOTE_" + num.toString() + "g");
                        } else if (NumberUtils.isDigits(pos)) {
                            if (is6nen8nen) {
                                addSlashLineOfFieldn(param, svfForm, FIELD_LESSON, FIELD_ABSENCE, pos);
                            } else {
                                addSlashLineOfFieldn(param, svfForm, FIELD_LESSON, FIELD_PRESENT, pos);
                                addSlashLineOfFieldn(param, svfForm, FIELD_ABSENCE, pos);
                            }
                        }
                    }
                }

                if (modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_ACT_SLASH)) {
                    addSlashLineOfField(svfForm, "ACTION");
                }
                if (modifyFormFlgMap.containsKey(FLG_A4_SOGAKU_VAL_SLASH)) {
                    addSlashLineOfField(svfForm, "ASSESS");
                }

                // A4 学校印内の枠線と"印"文字をカット
                if (modifyFormFlgMap.containsKey(FLG_A4_NOT_PRINT_INN_MARK_INNER_STAMP)) {
                    final SvfForm.ImageField koin = svfForm.getImageField("STAMP");
                    if (null != koin) {
                        for (final SvfForm.Box box : svfForm.getElementList(SvfForm.Box.class)) {
                            final boolean isInnerStampField = koin._point._x < box._upperLeft._x && box._lowerRight._x < koin._point._x + koin._endX
                                                           && koin._point._y < box._upperLeft._y && box._lowerRight._y < koin._point._y + koin._height;
                            if (isInnerStampField) { // 点線枠
                                svfForm.removeBox(box);
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" remove box : " + box);
                                }
                            }
                        }
                        for (final SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("印")) {
                            final boolean isInnerStampField = koin._point._x < koteiMoji._point._x && koteiMoji._point._x < koin._point._x + koin._endX
                                                           && koin._point._y < koteiMoji._point._y && koteiMoji._point._y < koin._point._y + koin._height;
                            if (isInnerStampField) { // "印"
                                svfForm.removeKoteiMoji(koteiMoji);
                                if (param._isOutputDebugSvfFormModify) {
                                    log.info(" remove \"印\" : " + koteiMoji);
                                }
                            }
                        }
                    }
                }

                // A4 成績欄総合的な学習の時間科目名フィールド追加
                if (modifyFormFlgMap.containsKey(FLG_A4_ADD_ITEM_FIELD)) {
                    for (final String fieldname : Arrays.asList("ITEM", "ITEM2", "ITEM_HANKI")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null != field) {
                            svfForm.addField(field.copyTo(fieldname + "_2").setFieldLength(36).setCharPoint10(85).setEndX(1035).setY(field._position._y + 2));
                            svfForm.addField(field.copyTo(fieldname + "_3").setFieldLength(40).setCharPoint10(80).setEndX(1065).setY(field._position._y + 4).setX(field._position._x - 10));
                            svfForm.addField(field.copyTo(fieldname + "_4").setFieldLength(46).setCharPoint10(70).setEndX(1065).setY(field._position._y + 8).setX(field._position._x - 14));
                            svfForm.addField(field.copyTo(fieldname + "_5").setFieldLength(50).setCharPoint10(65).setEndX(1069).setY(field._position._y + 10).setX(field._position._x - 18));
                        }
                    }
                }

                // 熊本 諸事項 行間調整
                if (modifyFormFlgMap.containsKey(FLG_KUMAMOTO_A4_FORM23_SHOJIKOU_LINES)) {
                    final Map<String, SvfForm.Field.RepeatConfig> configMap = new HashMap<String, SvfForm.Field.RepeatConfig>();
                    final Map<String, List<SvfForm.Field>> repeatConfigFieldListMap = new TreeMap<String, List<SvfForm.Field>>();
                    for (int g = 1; g <= 2; g++) {
                        for (int seq = 1; seq <= 6; seq++) {
                            final String fieldname = "field8_" + String.valueOf(g) + "g_" + String.valueOf(seq);
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null == field) {
                                continue;
                            }
                            configMap.put(field._repeatConfig._repeatNo, field._repeatConfig);
                            Util.getMappedList(repeatConfigFieldListMap, field._repeatConfig._repeatNo).add(field);
                        }
                    }

                    for (final Map.Entry<String, SvfForm.Field.RepeatConfig> e : configMap.entrySet()) {
                        final String repeatNo = e.getKey();
                        final SvfForm.Field.RepeatConfig rc = e.getValue();
                        final SvfForm.Field.RepeatConfig newRc = rc.setRepeatCount(rc._repeatCount - 3).setRepeatPitchPoint(3.568);

                        for (final SvfForm.Field field : Util.getMappedList(repeatConfigFieldListMap, repeatNo)) {
                            svfForm.removeField(field);
                            svfForm.addField(field.setCharPoint10(100).setRepeatConfig(newRc));
                        }
                    }
                }

                if (modifyFormFlgMap.containsKey(FLG_A4_ADD_DUMMY9)) {
                    final SvfForm.Field field9 = svfForm.getField("field9");
                    final SvfForm.Field dummy9_2 = svfForm.getField("DUMMY9_2");
                    if (null != field9 && null != dummy9_2) {
                        final Line leftLine = svfForm.getNearestLeftLine(field9._position);
                        final Line rightLine = svfForm.getNearestRightLine(field9._position);
                        if (null != leftLine && null != rightLine) {
                            final int startX = leftLine.getPoint()._x + 20;
                            final int endX = rightLine.getPoint()._x - 20;
                            int nextN = 3;
                            while (null != svfForm.getField("DUMMY9_" + String.valueOf(nextN))) {
                                nextN += 1;
                            }
                            // #DUMMY9
                            final int addCount = 36;
                            for (int n = nextN; n <= nextN + addCount; n++) {
                                final SvfForm.Field newField = dummy9_2.copyTo("DUMMY9_" + String.valueOf(n)).setX(startX + (endX - startX) * (n - nextN) / addCount);
                                svfForm.addField(newField);
                            }
                        }
                    }
                }
            }

            private void modifyFormShingakuyou(final PrintData printData, final FormKNJE070_1_A4.FormA4ConfigParam configParam, final List<FormRecord> recordList, final Param param, final TreeMap<String, String> modifyFormFlgMap, final SvfForm svfForm) {
                final boolean isA4 = "1".equals(printData._tyousasyo2020);

                if (modifyFormFlgMap.containsKey(FLG_SOGO_SURA_REC1) || modifyFormFlgMap.containsKey(FLG_ABROAD_SURA)) {
                    int abroadIdx = -1;
                    int sogakuIdx = -1;
                    final List<Map<String, String>> suraList = printData._form1SuraList;
                    for (final Map<String, String> suraMap : suraList) {
                        if ("1".equals(suraMap.get("#ABROAD_FLG"))) {
                            abroadIdx = suraList.indexOf(suraMap);
                        } else if ("1".equals(suraMap.get(SOGAKU_FLG))) {
                            sogakuIdx = suraList.indexOf(suraMap);
                        }
                    }
                    final int blankLineCount = printData.formRecord._lineMax12 - recordList.size() - suraList.size();
                    final Record record1 = svfForm.getRecord("RECORD1");
                    final int recordHeight = record1.getHeight();
                    final SubForm subForm1 = svfForm.getSubForm("SUBFORM1");
                    final SubForm subForm2 = svfForm.getSubForm("SUBFORM2");
                    final int recordCount1 = subForm1.getHeight() / recordHeight;
                    final Field totalCreditField = svfForm.getField(FormKNJE070_1.TOTAL_CREDITfield);
                    final Line totalCreditFieldLeftLine = svfForm.getNearestLeftLine(totalCreditField._position);

                    // 進学用　総合的な学習の時間 単位数スラッシュ
                    if (modifyFormFlgMap.containsKey(FLG_SOGO_SURA_REC1)) {
                        final int sogakuLine = -1 == sogakuIdx ? -1 : recordList.size() + blankLineCount + sogakuIdx;
                        final int rightX, leftX, topY;
                        if (sogakuLine <= recordCount1) {
                            leftX = totalCreditFieldLeftLine._start._x;
                            rightX = subForm1._point2._x;
                            topY = sogakuLine * recordHeight + subForm1._point1._y + record1._point1._y;
                        } else {
                            leftX = totalCreditFieldLeftLine._start._x - subForm1._point1._x + subForm2._point1._x;
                            rightX = subForm2._point2._x;
                            topY = (sogakuLine - recordCount1) * recordHeight + subForm2._point1._y + record1._point1._y;
                        }
                        final Line printSlashLine = new Line(pt(rightX, topY), pt(leftX, topY + recordHeight));
                        if (param._isOutputDebug) {
                            log.info(" sogauSuraRecord line (" + sogakuLine + ") = " + printSlashLine);
                        }
                        svfForm.addLine(printSlashLine);
                    }
                    // 進学用　留学単位数欄スラッシュ
                    if (modifyFormFlgMap.containsKey(FLG_ABROAD_SURA)) {
                        final int abroadLine = -1 == abroadIdx ? -1 : recordList.size() + blankLineCount + abroadIdx;
                        final int rightX, leftX, topY;
                        if (abroadLine <= recordCount1) {
                            leftX = totalCreditFieldLeftLine._start._x;
                            rightX = subForm1._point2._x;
                            topY = abroadLine * recordHeight + subForm1._point1._y + record1._point1._y;
                        } else {
                            leftX = totalCreditFieldLeftLine._start._x - subForm1._point1._x + subForm2._point1._x;
                            rightX = subForm2._point2._x;
                            topY = (abroadLine - recordCount1) * recordHeight + subForm2._point1._y + record1._point1._y;
                        }
                        final Line printSlashLine = new Line(false, LineKind.SOLID, LineWidth.THIN, pt(rightX, topY), pt(leftX, topY + recordHeight));
                        if (param._isOutputDebug) {
                            log.info(" abroadSura line (" + abroadLine + ") = " + printSlashLine);
                        }
                        svfForm.addLine(printSlashLine);
                    }
                }
                // 進学用　総合的な学習の時間 所見スラッシュ
                if (!StringUtils.isEmpty(modifyFormFlgMap.get(FLG_SOGO_SURA_SHOKEN_A4IGAI))) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        for (final String pos : modifyFormFlgMap.get(FLG_SOGO_SURA_SHOKEN_A4IGAI).split("_")) {
                            addSlashLineOfField(svfForm, "ACTION" + pos + "g");
                            addSlashLineOfField(svfForm, "ASSESS" + pos + "g");
                        }
                    } else {
                        addSlashLineOfField(svfForm, "ACTION");
                        addSlashLineOfField(svfForm, "ASSESS");
                    }
                }
                // 進学用　出欠欄スラッシュ
                if (!modifyFormFlgMap.containsKey(FLG_NNEN_SET) || !modifyFormFlgMap.containsKey(FLG_NNEN_SET4)) {
                    if (modifyFormFlgMap.containsKey(FLG_ATTEND_SURA)) {
                        for (final String posfields : modifyFormFlgMap.get(FLG_ATTEND_SURA).split("_")) {
                            final String pos = StringUtils.split(posfields, ":")[0];
                            final String[] fields = StringUtils.split(StringUtils.split(posfields, ":")[1], "-");
                            for (final String field : fields) {
                                addSlashLineOfFieldn(param, svfForm, field, pos);
                            }
                        }
                    }
                }
                // 進学用　出欠備考欄スラッシュ
                if (modifyFormFlgMap.containsKey(FLG_ATTENDREMARK_SURA)) {
                    for (final String pos : modifyFormFlgMap.get(FLG_ATTENDREMARK_SURA).split("_")) {
                        addSlashLineOfField(svfForm, "NOTE_" + pos + "g");
                    }
                }

                // 進学用　「総合的な学習の時間」を「総合的な探究の時間」に変更
                if (modifyFormFlgMap.containsKey(FLG_TANKYU1)) {
                    for (final KoteiMoji koteiMoji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                        if (koteiMoji._moji.contains(PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN + "の内容・評価")) {
                            svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(PrintData.SOGOTEKI_NA_TANKYU_NO_JIKAN + "の内容・評価"));
                        } else if (!koteiMoji._moji.contains(PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN) && koteiMoji._moji.contains("な学習")) {
                            svfForm.move(koteiMoji, koteiMoji.replaceMojiWith("な探究"));
                        }
                    }
                }

                // 進学用　記載責任者印影追加
                if (modifyFormFlgMap.containsKey(FLG_KISAI_INEI)) {

                    final String stampSizeMm = defstr(defval(Util.toBigDecimalList(Arrays.asList(printData.property(Property.knje070_1KisaiStampSizeMm), printData.property(Property.staffStampSizeMm)))), (String) null);
                    final String stampPositionXmm = printData.property(Property.knje070_1KisaiStampPositionXmm);
                    final String stampPositionYmm = printData.property(Property.knje070_1KisaiStampPositionYmm);

                    if (null == svfForm.getImageField("KISAI_STAMP")) {
                        final String imageNo = String.valueOf(svfForm.getElementList(SvfForm.ImageField.class).size() + 1);
                        final int len;
                        if (NumberUtils.isNumber(stampSizeMm)) {
                            len = Util.mmToDot(stampSizeMm);
                        } else {
                            len = 155;
                        }
                        final int width = len;
                        final int height = len;

                        final int centerx;
                        if (NumberUtils.isNumber(stampPositionXmm)) {
                            centerx = Util.mmToDot(stampPositionXmm) + len / 2;
                        } else if (isA4) {
                            centerx = 3020;
                        } else {
                            centerx = 5970;
                        }
                        final int centery;
                        if (NumberUtils.isNumber(stampPositionYmm)) {
                            centery = Util.mmToDot(stampPositionYmm) + len / 2;
                        } else if (isA4) {
                            centery = 4400;
                        } else {
                            centery = 4350;
                        }

                        final int x = centerx - width / 2;
                        final int y = centery - height / 2;
                        final SvfForm.ImageField koin = svfForm.getImageField("STAMP");
                        if (null == koin) {
                            log.info(" no field STAMP");
                        } else {
                            final SvfForm.Point imageXY = pt(x, y);
                            SvfForm.ImageField kisaiStampField = new SvfForm.ImageField(imageNo, "KISAI_STAMP", imageXY, imageXY._x + width, height, "0", "0");
                            try {
                                kisaiStampField = kisaiStampField.setColor(koin.getColor());
                            } catch (Throwable t) {
                                log.info(" throw ", t);
                            }
                            svfForm.addImageField(kisaiStampField);
                            if (param._isOutputDebug) {
                                log.info(" add imageField : image length = " + len + ", pos (x, y) = (" + stampPositionXmm + ", " + stampPositionYmm + ") " + kisaiStampField);
                            }
                        }
                    } else {
                        // 参考 FLG_STAMP_FIELD_SIZE_MODIFY
                        if (modifyFormFlgMap.containsKey(FLG_KISAI_STAMP_FIELD_SIZE_MODIFY)) {
                            if (Util.toInt(stampSizeMm, 0) > 0 || NumberUtils.isNumber(stampPositionXmm) || NumberUtils.isNumber(stampPositionYmm)) {
                                resizeStampImage(printData, svfForm, "KISAI_STAMP", stampSizeMm, stampPositionXmm, stampPositionYmm);
                            }
                        }
                    }
                }

                // 進学用 単位数に小数点つける
                if (modifyFormFlgMap.containsKey(FLG_CREDIT_FIELD_DOT)) {
                    SvfForm.Field field = svfForm.getField(FormKNJE070_1.TOTAL_CREDITfield);
                    svfForm.addField(field.copyTo(FormKNJE070_1.TOTAL_CREDIT_DOTfield).setFieldLength(5).setCharPoint10(80).setHenshuShiki("").setY(field._position._y + 5).setX(field._position._x - 5));
                    if (printData._isHankiNinteiForm) {
                        SvfForm.Field fieldHanki = svfForm.getField(FormKNJE070_1.TOTAL_CREDIT_HANKIfield);
                        if (null != fieldHanki) {
                            svfForm.addField(fieldHanki.copyTo(FormKNJE070_1.TOTAL_CREDIT_DOT_HANKIfield).setFieldLength(5).setCharPoint10(80).setHenshuShiki("").setY(fieldHanki._position._y + 5).setX(fieldHanki._position._x - 5));
                        }
                    }
                }

                // 進学用　半期認定対応
                if (modifyFormFlgMap.containsKey(FLG_HANKI_NINTEI)) {
                    modifyFormHankiNintei(svfForm);
                }

                // 進学用  印影サイズ修正
                if (modifyFormFlgMap.containsKey(FLG_STAMP_FIELD_SIZE_MODIFY)) {
                    final String stampSizeMm = defstr(defval(Util.toBigDecimalList(Arrays.asList(printData.property(Property.knje070_1StampSizeMm), printData.property(Property.stampSizeMm)))), defval(param._z010.in(Z010Info.KaichiNihonbashi) ? "21" : null));
                    final String stampPositionXmm = printData.property(Property.knje070_1StampPositionXmm);
                    final String stampPositionYmm = printData.property(Property.knje070_1StampPositionYmm);

                    if (Util.toInt(stampSizeMm, 0) > 0 || NumberUtils.isNumber(stampPositionXmm) || NumberUtils.isNumber(stampPositionYmm)) {
                        resizeStampImage(printData, svfForm, "STAMP", stampSizeMm, stampPositionXmm, stampPositionYmm);
                    }
                }

                // 文言「出校」
                if (modifyFormFlgMap.containsKey(FLG_TITLE_SHUKKOU)) {
                    for (final SvfForm.KoteiMoji moji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                        if (moji._moji.contains("出欠の記録")) {
                            svfForm.move(moji, moji.replaceMojiWith(moji._moji.replace("出欠", "出校")));
                        } else if (moji._moji.replaceAll("(\\s|　)", "").contains("出席日数")) {
                            svfForm.move(moji, moji.replaceMojiWith("出校日数").setEndX(moji._endX));
                        }
                    }
                }
            }
        }

        public FormInfo getFormInfo() {
            return _formInfo;
        }

        private boolean printMain1(final DB2UDB db2, final Vrw32alp svf, final PrintData printData, final TitlePage titlePage) throws FileNotFoundException {
            _printData = printData;
            _isCsv = _printData.isCsv();
            _svf = svf;
            if (_formInfo._is2page) {
                return printLr(db2, svf, printData, titlePage);
            }
            setForm(_svf, _formInfo._formName, 4, param());
            _formInfo.setSvfForm(_svf, _formInfo._formName, this, param());
            setSvfFieldInfo(_printData, param(), _currentForm);
            final boolean isFormatOnly = false;
            _printData.formRecord = setForm1Record(isFormatOnly, titlePage);
            final int maxPage = _printData.formRecord.getMaxPage1(_printData);
            for (int pageIdx = 0; pageIdx < maxPage; pageIdx++) {
                final boolean isLast = pageIdx == maxPage - 1;
                final List<FormRecord> recordList = _printData.formRecord.getPageRecordList(pageIdx);
                final String bef = _formInfo._formName;
                _formInfo._formName = _formInfo.setConfigFormShingakuyou(svf, _formInfo._formName, printData, titlePage, isLast, recordList, null);
                setForm(_svf, _formInfo._formName, 4, param());

                _printData._printProcess = 1;
                svfVrsOut("SCHREGNO" ,_printData._schregno);
                _printData._printProcess = 2;
                printHeadCommon(isFormatOnly);
                printHeadCommon2(isFormatOnly);
                printHead_1();
                _printData._printProcess = 3;
                printSyoushoNum(true, isFormatOnly);  // 証書番号を出力
                printAddressCommon(db2, isFormatOnly); //氏名、住所等出力
                _printData._printProcess = 4;
                printHyoteiHeikin(isFormatOnly);
                printSeisekiDankai_1(); //成績段階別人数の出力
                _printData._printProcess = 5;
                FormRecordPart.printSvfFieldData(this, getAttendFormRecord(isFormatOnly, titlePage)); //出欠の出力
                _printData._printProcess = 6;
                printShoken_1(titlePage); //所見の出力
                _printData._printProcess = 8;
                printStudyrec_1(isFormatOnly, recordList, isLast, titlePage); //学習の記録出力-->VrEndRecord()はここで！
                _formInfo._formName = bef;
            }
            return hasdata;
        }

        private boolean printLr(final DB2UDB db2, final Vrw32alp svf, final PrintData printData, final TitlePage titlePage) throws FileNotFoundException {
            setForm(_svf, _formInfo._formNameLeft, 4, param());
            setSvfFieldInfo(_printData, param(), _currentForm);
            final boolean isFormatOnly = false;
            _printData.formRecord = setForm1Record(isFormatOnly, titlePage);
            final int maxPage = _printData.formRecord.getMaxPage1(_printData);
            for (int pageIdx = 0; pageIdx < maxPage; pageIdx++) {
                final boolean isLast = pageIdx == maxPage - 1;
                final List<FormRecord> recordList = _printData.formRecord.getPageRecordList(pageIdx);
                setForm(_svf, _formInfo._formNameLeft, 4, param());
                svfVrsOut("SCHREGNO" ,_printData._schregno);
                printHeadCommon(isFormatOnly);
                printHeadCommon2(isFormatOnly);
                printHead_1();
                printSyoushoNum(true, isFormatOnly);  // 証書番号を出力
                printAddressCommon(db2, isFormatOnly); //氏名、住所等出力
                printHyoteiHeikin(isFormatOnly);
                printSeisekiDankai_1(); //成績段階別人数の出力
                printStudyrec_1(isFormatOnly, recordList, isLast, titlePage); //学習の記録出力-->VrEndRecord()はここで！
            }

            setForm(_svf, _formInfo._formNameRight, 1, param());
            svfVrsOut("SCHREGNO", _printData._schregno);

            printHeadCommon(isFormatOnly);
            printHeadCommon2(isFormatOnly);
            printHead_1();
            FormRecordPart.printSvfFieldData(this, getAttendFormRecord(isFormatOnly, titlePage)); //出欠の出力
            printShoken_1(titlePage); //所見の出力
            _svf.VrEndPage();
            return true;
        }

        public boolean csv(final DB2UDB db2, final List<List<String>> csvOutputLines, final PrintData printData, final TitlePage titlePage) {
            _printData = printData;
            _isCsv = _printData.isCsv();
            _csvOutputLines = csvOutputLines;
            _printData.formRecord = setForm1Record(false, titlePage);

            // １．生徒情報
            final boolean isTitleNendo = PrintData.PRINT_NENDO == _printData.getPrintGradeTitleMethod(param());
            final PersonInfo personInfo = _printData._personInfo;
            final Tuple<String[], String[]> nameKanaAndName = getNameKanaAndName(personInfo, 9999, 9999, false);
            final String nameKana = ((String[]) nameKanaAndName._first)[0];
            final String name = "1".equals(_printData._kanji) ? nameKanaAndName._second[0] : null;
            final String seibetsu = defstr(param()._z002Abbv1Map.get(personInfo._sex));
            final String addr1 = defstr(personInfo._addr1);
            final String addr2 = "1".equals(personInfo._addrFlg) ? defstr(personInfo._addr2) : null;
            final String coursename = defstr(personInfo._coursename);
            final Tuple<String, String> entGradeAndEnter1 = getEntGradeAndEnter1(db2);
            final Tuple<String, String> printGraduDateAnddGraduName = getPrintGraduDateAndPrintGraduName(db2);
            final List<List<String>> _1seitoJoho = new ArrayList<List<String>>();
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList(new String[] {getPrintSyosyoNameLr(false), "", "調査書"}));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("１．生徒情報"));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("ふりがな", nameKana, "", "", "", "性別", seibetsu));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("氏名", name, "", "", "", "現住所", addr1));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("生年月日", getPrintBirthdate(db2, personInfo), "", "", "", "", addr2));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("学校名", _printData._schoolInfo.t4classification, _printData._schoolInfo.t4schoolname1, _printData._schoolInfo.certifSchoolRemark[6], "", "在学期間", getPrintEntDate(db2), "", entGradeAndEnter1._first, entGradeAndEnter1._second));
            CsvUtils.newLine(_1seitoJoho).addAll(Arrays.asList("課程名", coursename, "学科名", getPrintMajorname(personInfo), "", "",  printGraduDateAnddGraduName._first, "", printGraduDateAnddGraduName._second));

            // ２．各教科・科目の学習の記録
            final List<List<String>> _2gakushu = new ArrayList<List<String>>();
            CsvUtils.newLine(_2gakushu).addAll(Arrays.asList("２．各教科・科目の学習の記録"));
            CsvUtils.newLine(_2gakushu).addAll(Arrays.asList("教科・科目", "", "", "評定"));
            _2gakushu.addAll(csvGakushunoKirokuShingaku(titlePage));

            // ３．評定平均
            final List<List<String>> _3hyoteiheikin = new ArrayList<List<String>>();
            CsvUtils.newLine(_3hyoteiheikin).addAll(Arrays.asList("３．各教科の評定平均値"));
            List<List<String>> classnameHyotei = setLined(new ArrayList(Arrays.asList("教科", "平均値")));
            for (int i = 0; i < _printData._hyoteiHeikinList.size(); i++) {
                final HyoteiHeikin avgRec = _printData._hyoteiHeikinList.get(i);
                classnameHyotei = CsvUtils.horizontalUnionLines(classnameHyotei, setLined(Arrays.asList(avgRec.classname(), avgRec.avg())));
            }
            classnameHyotei = CsvUtils.horizontalUnionLines(classnameHyotei, setLined(Arrays.asList("全体の評定平均値", null == _printData._studyrecDatTotal ? "" : _printData._studyrecDatTotal._avgGrades)));
            _3hyoteiheikin.addAll(classnameHyotei);

            // ４．学習成績概評
            final List<List<String>> _4gaihyo = new ArrayList<List<String>>();
            CsvUtils.newLine(_4gaihyo).addAll(Arrays.asList("４．学習成績概評", "", "成績段階別人数"));
            String member1 = null, member2 = null, member3 = null, member4 = null, member5 = null, member0 = "", member6 = "";
            if (!_printData._geneviewmbrMap.isEmpty()) {
                final Map m = (Map) _printData._geneviewmbrMap.get(_printData._geneviewmbrMap.keySet().iterator().next());
                member5 = "Ａ　" + defstr(Util.append(getString(m, "MEMBER5"), "人"));
                member4 = "Ｂ　" + defstr(Util.append(getString(m, "MEMBER4"), "人"));
                member3 = "Ｃ　" + defstr(Util.append(getString(m, "MEMBER3"), "人"));
                member2 = "Ｄ　" + defstr(Util.append(getString(m, "MEMBER2"), "人"));
                member1 = "Ｅ　" + defstr(Util.append(getString(m, "MEMBER1"), "人"));
                member0 = defstr(getString(m, "MEMBER0")) + "人";
                member6 = getString(m, "MEMBER6");
                member6 = _printData._comment == null ? "" : " ( " + (NumberUtils.isDigits(member6) ? String.valueOf(Integer.parseInt(member6)) : "") + " 人)";
            }
            CsvUtils.newLine(_4gaihyo).addAll(Arrays.asList(" " + defstr(_printData._hexamEntremarkHdatMark) + defstr(_printData._assessMark) + " 段階", "", member5, member4, member3, member2, member1, "合計　" + member0 + member6));

            // ５．出欠の記録
            final List<List<String>> _5shukketsu = new ArrayList<List<String>>();
            final String kubun = isTitleNendo ? "区分＼年度" : "区分＼学年";
            CsvUtils.newLine(_5shukketsu).addAll(Arrays.asList("５．出欠の記録"));
            final List<String> _5shukketsuHeader = CsvUtils.newLine(_5shukketsu);
            _5shukketsuHeader.add(kubun);
            for (final Title title : titlePage._titleList) {
                String titleStr = null;
                if (_formInfo._isShingakuyouFormShukketsuTitleFormat2) {
                    titleStr = title._name;
                } else if (param()._z010.in(Z010Info.Tokiwa) && !"1".equals(_printData._tyousasyo2020)) {
                    if (NumberUtils.isDigits(title._annual)) {
                        titleStr = String.valueOf(Integer.parseInt(title._annual));
                    }
                } else if (isTitleNendo || Title.NYUGAKUMAE.equals(title._name)) {
                    titleStr = Util.concatList(title._nameArrayAttend);
                } else {
                    titleStr = title._nameArrayAttend.get(1);
                }
                _5shukketsuHeader.add(titleStr);
            }
            for (int i = 0; i < 7; i++) {
                if (i == 6) {
                    List<List<String>> kubunLine = setLined(Arrays.asList("備考"));

                    for (final Title title : titlePage._titleList) {
                        final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, titlePage._titleList, title._year);
                        final String remark = Util.newLineReplace(attendrecRemark(dat, param(), printData, title), "");
                        final List<String> tokenList = Util.getTokenList(remark, attendrecRemarkSize(), param());

                        kubunLine = CsvUtils.horizontalUnionLines(kubunLine, setLined(tokenList));
                    }

                    _5shukketsu.addAll(kubunLine);

                } else {
                    final List<String> kubunLine = CsvUtils.newLine(_5shukketsu);

                    kubunLine.add(new String[] {"授業日数", "出席停止・忌引き等の日数", "留学中の授業日数", "出席しなければならない日数", "欠席日数", "出席日数"}[i]);

                    for (final Title title : titlePage._titleList) {
                        final AttendrecDat att = _printData.getAttendrecDat(title._year);
                        String val = null;
                        if (null != att) {
                            switch (i) {
                            case 0: val = defstr(att.attend1()); break; //授業日数
                            case 1: val = defstr(att.suspMour()); break; //出停・忌引
                            case 2: val = defstr(att.abroad()); break; //留学
                            case 3: val = defstr(att.requirepresent()); break; //要出席
                            case 4: val = defstr(att.attend6()); break; //欠席
                            case 5: val = defstr(att.present()); break; //出席
                        }
                        }
                        kubunLine.add(val);
                    }
                }
            }

            // ６．特別活動の記録
            final List<List<String>> _6tokubetsukatsudou = new ArrayList<List<String>>();
            CsvUtils.newLine(_6tokubetsukatsudou).addAll(Arrays.asList("６．特別活動の記録"));
            List<List<String>> _6rows = new ArrayList<List<String>>();
            final ShokenSize specialActRecShokenSize = specialActRecShokenSize();
            for (final Title title : titlePage._titleList) {
                final List<String> lines = new ArrayList<String>();
                lines.add(title._name);
                final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, titlePage._titleList, title._year);
                if (null != dat) {
                    lines.addAll(Util.getTokenList(dat.specialactrec(), specialActRecShokenSize, param()));
                }
                _6rows = CsvUtils.horizontalUnionLines(_6rows, setLined(lines));
            }
            for (int gi = 0; gi < specialActRecShokenSize._gyo; gi++) {
                if (gi < _6rows.size()) {
                    _6tokubetsukatsudou.add((List) _6rows.get(gi));
                } else {
                    CsvUtils.newLine(_6tokubetsukatsudou);
                }
            }

            // ７．指導上参考となる諸事項
            final List<List<String>> _7sidojo = new ArrayList<List<String>>();
            CsvUtils.newLine(_7sidojo).addAll(Arrays.asList("７．指導上参考となる諸事項"));
            CsvUtils.newLine(_7sidojo).addAll(Arrays.asList("", "(1)学習における特徴等 (2)行動の特徴、特技等", "(3)部活動、ボランティア活動等 (4)取得資格、検定等", "(5)その他"));
            for (final Title title : titlePage._titleList) {
                final List<String> nendoLines = new ArrayList<String>();
                nendoLines.add(title._name);
                List<List<String>> _7rows = setLined(nendoLines);

                final int gyo = Util.toInt(StringUtils.defaultString(_printData.property(Property.train_ref_1_2_3_gyo_sizeForPrint), _printData.property(Property.train_ref_1_2_3_gyo_size)), 5);
                final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, titlePage._titleList, title._year);
                if (!_printData._isFuhakkou && null != dat) {
                    final String trainRef1 = dat.trainRef1();
                    final String trainRef2 = dat.trainRef2();
                    final String trainRef3 = dat.trainRef3();
                    final int[] keta = _formInfo.getField7Keta(_printData);
                    final int keta1 = keta[0];
                    final int keta2 = keta[1];
                    final int keta3 = keta[2];

                    _7rows = CsvUtils.horizontalUnionLines(_7rows, setLined(Util.getTokenList(trainRef1, ShokenSize.getWithKeta(keta1, gyo), param())));
                    _7rows = CsvUtils.horizontalUnionLines(_7rows, setLined(Util.getTokenList(trainRef2, ShokenSize.getWithKeta(keta2, gyo), param())));
                    _7rows = CsvUtils.horizontalUnionLines(_7rows, setLined(Util.getTokenList(trainRef3, ShokenSize.getWithKeta(keta3, gyo), param())));
                }
                for (int gi = 0; gi < gyo; gi++) {
                    if (gi < _7rows.size()) {
                        _7sidojo.add(_7rows.get(gi));
                    } else {
                        CsvUtils.newLine(_7sidojo);
                    }
                }
            }

            final List<List<String>> _8sougaku = new ArrayList<List<String>>();
            CsvUtils.newLine(_8sougaku).addAll(Arrays.asList("８．" + _printData.getSogoSubclassname(param()) + "の内容・評価"));
            _8sougaku.addAll(csvSougaku(titlePage));

            // ９．備考
            final String remarkData = defstr(getString(_printData._hexamEntremarkHdat, "REMARK"));
            final List<List<String>> _9biko = new ArrayList<List<String>>();
            CsvUtils.newLine(_9biko).addAll(Arrays.asList("９．備考"));
            _9biko.addAll(CsvUtils.horizontalUnionLines(blankColumn(), setLined(getPrintRemarkList(getPrintRemark(remarkData, titlePage)))));
            _9biko.addAll(CsvUtils.horizontalUnionLines(blankColumn(), setLined(getPrintCertifSchoolDatRemark())));

            final List<List<String>> footer = new ArrayList<List<String>>();
            CsvUtils.newLine(footer).add(getCertTextCommon());
            CsvUtils.newLine(footer).addAll(Arrays.asList("", _printData._schoolInfo.kisaibi)); // 記載日
            CsvUtils.newLine(footer).addAll(Arrays.asList("学校名", _printData._schoolInfo.schoolname1));
            CsvUtils.newLine(footer).addAll(Arrays.asList("所在地", defstr(Util.prepend("〒", _printData._schoolInfo.schoolzipcd)) + " " + defstr(_printData._schoolInfo.schoolAddr))); // 記載日
            CsvUtils.newLine(footer).addAll(Arrays.asList("校長名", _printData.isNotOutputPrincipalName() ? "　　　　　" : _printData._schoolInfo.principalName, "", "印", "記載責任者職氏名", "", defstr(getJobname1()) + " " + defstr(getStaffname1())));

            final List blankLine = new ArrayList();
            _csvOutputLines.addAll(_1seitoJoho);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_2gakushu);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_3hyoteiheikin);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_4gaihyo);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_5shukketsu);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_6tokubetsukatsudou);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_7sidojo);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_8sougaku);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(_9biko);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(footer);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.add(blankLine);
            hasdata = true;
            return hasdata;
        }

        private List<List<String>> blankColumn() {
            return setLined(Arrays.asList(new String[] {""}));
        }

        private List<List<String>> csvGakushunoKirokuShingaku(final TitlePage _titlePage) {

            final List<List<String>> gakushu = new ArrayList<List<String>>();
            final List<String> header = CsvUtils.newLine(gakushu);
            header.addAll(Arrays.asList("教科", "科目", ""));
            List headerHanki = null;
            if (_printData._isHankiNinteiForm) {
                headerHanki = CsvUtils.newLine(gakushu);
                headerHanki.addAll(new ArrayList(Arrays.asList("", "", "")));
            }

            for (final Title title : _titlePage._titleList) {

                final List<String> nameArray = nameArrayGakushunoKiroku(title);
                if (null != headerHanki) {
                    header.addAll(Arrays.asList(Util.concatList(nameArray), ""));
                    headerHanki.addAll(Arrays.asList("前期", "後期"));
                } else {
                    header.add(Util.concatList(nameArray));
                }
            }
            header.add("修得単位数の計");

            for (int i = 0; i < _printData.formRecord._recordList.size(); i++) {
                final FormRecord record = _printData.formRecord._recordList.get(i);

                final List<String> recordLine = CsvUtils.newLine(gakushu);
                final Map<String, String> dataMap = record._dataMap;
                if (dataMap.containsKey("CLASSTITLE")) {
                    recordLine.add(dataMap.get("CLASSTITLE"));
                } else {
                    if (null != headerHanki) {
                        recordLine.addAll(Arrays.asList(defstr(dataMap.get("CLASSNAME_HANKI")) + defstr(dataMap.get("CLASSNAME_HANKI_2")), dataMap.get("SUBCLASSNAME_HANKI"), ""));
                    } else {
                        recordLine.addAll(Arrays.asList(defstr(dataMap.get("CLASSNAME")) + defstr(dataMap.get("CLASSNAME_2")), dataMap.get("SUBCLASSNAME"), ""));
                    }

                    for (final Title title : _titlePage._titleList) {

                        if (null != headerHanki) {
                            recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos) + "_HANKI_1"));
                            recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos) + "_HANKI_2"));
                        } else {
                            recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos)));
                        }
                    }
                    if (null != headerHanki) {
                        recordLine.add(dataMap.get("CREDIT_HANKI"));
                    } else {
                        recordLine.add(dataMap.get("CREDIT"));
                    }
                }
            }

            for (int i = 0; i < _printData._form1SuraList.size(); i++) {
                final Map<String, String> suraMap = _printData._form1SuraList.get(i);
                final List<String> recordLine = CsvUtils.newLine(gakushu);
                String credit = null;
                String item = null;
                for (final String field : suraMap.keySet()) {
                    if (field.startsWith("#")) {
                        continue;
                    } else if (Arrays.asList(TOTAL_CREDITfield, TOTAL_CREDIT2field, TOTAL_CREDIT_DOTfield).contains(field)) {
                        credit = suraMap.get(field);
                    } else {
                        item = suraMap.get(field);
                    }
                }
                recordLine.add("");
                recordLine.add(item);
                recordLine.add("");
                for (int j = 0; j < _titlePage._titleList.size(); j++) {
                    if (null != headerHanki) {
                        recordLine.add(_SLASH); // 前期
                        recordLine.add(_SLASH); // 後期
                    } else {
                        recordLine.add(_SLASH);
                    }
                }
                recordLine.add(credit);
            }
            return gakushu;
        }

        private void printSvfRepn1(final String header, final String suffix, final List<String> strList) {
            if (null == strList) {
                return;
            }
            final String sintoadiv = Integer.parseInt(_printData._os) == 1 ? "" : "_2K";
            for (int i = 0; i < strList.size(); i++) {
                final String field = header + sintoadiv + suffix;
                svfVrsOutn(field, i + 1, strList.get(i));
            }
        }

        private FormRecord printSvfRepnGradeFormRecord(final String header, final int g, final String suffix, final List<String> strList) {
            final FormRecord rec = new FormRecord();
            if (null == strList) {
                return rec;
            }
            final String sintoadiv = Integer.parseInt(_printData._os) == 1 ? "" : "_2K";
            for (int i = 0; i < strList.size(); i++) {
                final String field = header + String.valueOf(i + 1) + sintoadiv + suffix;
                rec.setDatan(field, g, strList.get(i));
            }
            return rec;
        }

        private void printSvfRepnGrade(final String header, final int g, final String suffix, final List<String> strList) {
            FormRecordPart.printSvfFieldData(this, printSvfRepnGradeFormRecord(header, g, suffix, strList));
        }

        private String getJobname1() {
            final String jobName;
            if (param().notHasCertifSchoolDatOrKindai()) {
                if (null != _printData._schoolInfo.staff2Name) {
                    jobName = param().isKindaifuzoku() ? "教諭 " : defstr(_printData._schoolInfo.staff2Jobname, "教諭");
                } else {
                    jobName = null;
                }
            } else {
                if (param()._z010.in(Z010Info.Hirokoku, Z010Info.Nishiyama)) {
                    jobName = "";
                } else if (param()._z010.in(Z010Info.Kumamoto)) {
                    jobName = StringUtils.isBlank(_printData._schoolInfo.staff2Name) ? "" : defstr(_printData._schoolInfo.staff2Jobname, "教諭");
                } else {
                    jobName = defstr(_printData._schoolInfo.staff2Jobname, "1".equals(_printData.property(Property.tyousasyoPrintHomeRoomStaff)) ? "" : "教諭");
                }
            }
            return jobName;
        }

        private String getStaffname1() {
            final String staff2Name;
            if (param().notHasCertifSchoolDatOrKindai()) {
                staff2Name = defstr(_printData._schoolInfo.staff2Name);
            } else {
                final boolean isPrintInn = !(param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama) || _formInfo._isKyotoForm || _printData._isMiekenForm || param()._z010.in(Z010Info.Hirokoku, Z010Info.Fukuiken, Z010Info.Sakae) || param()._z010.isKaichi() || param()._z010.in(Z010Info.TamagawaSei, Z010Info.Meikei, Z010Info.Hibarigaoka, Z010Info.NaganoSeisen, Z010Info.Reitaku, Z010Info.Matsudo));
                if (isPrintInn) {
                    staff2Name = defstr(_printData._schoolInfo.staff2Name, "　　　　　　　　　　") + "　印";
                } else {
                    staff2Name = defstr(_printData._schoolInfo.staff2Name);
                }
            }
            return staff2Name;
        }

        protected void printHead_1() {
            FormRecordPart.printSvfFieldData(this, getHead_1());
        }

        protected FormRecord getHead_1() {
            FormRecord r = new FormRecord();
            if (param()._z010.in(Z010Info.Hirokoku)) {
                r.setData("MENTION", "記載責任者氏名"); // 「記載責任者職氏名」
            }
            r.setData("JOBNAME", getJobname1()); // 記載責任者職名
            r.setData("STAFFNAME_2", getStaffname1()); // 記載責任者名

            // 住所
            if (_printData._schoolInfo.schoolAddr != null) {
                final String field;
                if (param().isKindaifuzoku() && !"1".equals(_printData._tyousasyo2020)) {
                    field = "school_address";
                } else {
                    field = getFieldForData(Arrays.asList("school_address1", "school_address2", "school_address3"), _printData._schoolInfo.schoolAddr, false);
                }
                r.setData(field, _printData._schoolInfo.schoolAddr);
            }
            return r;
        }

        protected void printTokubetsuKatsudouTitle(final List<Title> titleList) {
            for (final Title title : titleList) {
                final int pos = title._pos;
                svfVrsOut("GRADE4_" + pos, title._name);  // 特別活動の記録
            }
        }

        private List<String> nameArrayGakushunoKiroku(final Title title) {
            final int method = _printData.getPrintGradeTitleMethod(_param);
            final List<String> nameArray;
            if (0 == title._intKey) {
                nameArray = !"1".equals(_printData._tyousasyo2020) && _param._z010.in(Z010Info.KaichiTsushin) ? Arrays.asList(Title.NYUGAKUMAE, "", "") : Arrays.asList("入", "学", "前");
            } else if (CommonPrintData.PRINT_ANNUAL == method || CommonPrintData.PRINT_GAKUNEN == method) {
                final String ann;
                if (CommonPrintData.PRINT_GAKUNEN == method && NumberUtils.isDigits(title._gradeCd)) {
                    ann = String.valueOf(Integer.parseInt(title._gradeCd));
                } else if (NumberUtils.isDigits(title._annual)) {
                    ann = String.valueOf(Integer.parseInt(title._annual));
                } else {
                    ann = "";
                }
                nameArray = Arrays.asList("第", ann, "学年");
            } else {
                if (_param._isSeireki) {
                    final String nen = String.valueOf(title._intKey);
                    nameArray = Arrays.asList("", nen, "年度");
                } else {
                    final String gengou = title._gengouNen.substring(0, 2);
                    final String nen = title._gengouNen.substring(2);
                    nameArray = !"1".equals(_printData._tyousasyo2020) && _param._z010.in(Z010Info.KaichiTsushin) ? Arrays.asList(gengou + "　年度", nen, "") : Arrays.asList(gengou, nen, "年度");
                }
            }
            return nameArray;
        }

        protected void printSeisekiTitle(final TitlePage titlePage) {

            for (final Title title : titlePage._titleList) {
                final List<String> nameArray = nameArrayGakushunoKiroku(title);

                final int pos = title._seisekiPos;
                for (final String lr : Util.toStrList(Util.seqInclusive(1, 2))) {  // 学習の記録　左 右
                    if ("1".equals(_printData._tyousasyo2020) && (_formInfo._formNenYou == FormNen._6 || _formInfo._formNenYou == FormNen._8)) {
                        svfVrsOut("GRADE" + lr + "_" + pos + "_4" , title._name);
                    } else if (!"1".equals(_printData._tyousasyo2020) && param()._z010.in(Z010Info.Tokiwa)) {
                        svfVrsOut("GRADE" + lr + "_" + pos + "_4" , title._name);
                    } else if (!"1".equals(_printData._tyousasyo2020) && param()._z010.in(Z010Info.Musashinohigashi)) {
                        svfVrsOut("GRADE" + lr + "_" + pos + "_4" , Util.concatList(nameArray));
                    } else if ("1".equals(_printData.property(Property.tyousasyo2020seisekiTitleTategaki))) {
                        svfVrsOut("GRADE" + lr + "_" + pos + "_4_1" , nameArray.get(0));
                        if (0 == title._intKey) {
                            svfVrsOut("GRADE" + lr + "_" + pos + "_4_2" , nameArray.get(1));
                        } else {
                            svfVrsOut("GRADE" + lr + "_" + pos + "_4_2_YOKO" , nameArray.get(1));
                        }
                        svfVrsOut("GRADE" + lr + "_" + pos + "_4_3" , nameArray.get(2));
                    } else {
                        svfVrsOut("GRADE" + lr + "_" + pos + "_1" , nameArray.get(0));
                        svfVrsOut("GRADE" + lr + "_" + pos + "_2" , nameArray.get(1));
                        svfVrsOut("GRADE" + lr + "_" + pos + "_3" , nameArray.get(2));
                        if (_formInfo._hankiNinteiTaiou && _printData._isHankiNinteiForm) {
                            for (int i = 1; i <= 3; i++) {
                                final String fieldname1 = "GRADE" + lr + "_" + pos + "_" + String.valueOf(i);
                                svfVrAttribute(fieldname1, attributeIntPlus(fieldname1, "Y", -70));
                            }
                        }
                    }
                }
            }
        }

        private void printShojikouTitle(final List<Title> titleList) {
            final boolean isNendo = PrintData.PRINT_NENDO == _printData.getPrintGradeTitleMethod(param());
            for (final Title title : titleList) {
                final int pos = title._pos;
                if (param()._z010.in(Z010Info.Tokiwa) && !"1".equals(_printData._tyousasyo2020)) {
                    svfVrsOut("GRADE5_" + pos + "_4" , title._name);  // 指導上参考となる諸事項
                } else if (isNendo) {
                    final List<String> nameArray = title._nameArray1;
                    svfVrsOut("GRADE5_" + pos + "_1" , nameArray.get(0));  // 指導上参考となる諸事項
                    svfVrsOut("GRADE5_" + pos + "_2" , nameArray.get(1));
                    svfVrsOut("GRADE5_" + pos + "_3" , nameArray.get(2));
                } else {
                    svfVrsOut("GRADE5_" + pos + "_2" , title._name);  // 指導上参考となる諸事項
                }
            }
        }

        /**
         * 「総合的な学習の時間活動内容」と「備考」
         * @return
         */
        private Tuple<String, String> getPrintTotalStudyActAndPrintRemark(final String remark, final ShokenSize matomeActSize, final int div, final TitlePage titlePage) {

            final String totalStudyAct = getTotalStudy(matomeActSize, "TOTALSTUDYACT", "DAT_TOTALSTUDYACT", titlePage);
            if (param()._isOutputDebugShoken) {
                log.info(" totalStudyAct = " + totalStudyAct);
            }

            String printTotalStudyAct = null;
            String printRemark = null;
            final Param param = param();
            List<String> daitaiBikoNo90All = Collections.EMPTY_LIST;
            boolean hasJiritsuKatsudouRecord = false;
            final boolean notUseBiko = !(div == 2);
            if (!notUseBiko) {
                daitaiBikoNo90All = _printData._studyrecSubstNote.getDaitaiBikoNo90All("getPrintTotalStudyActAndPrintRemark_debug1", param);
                hasJiritsuKatsudouRecord = _printData.hasJiritsuKatsudouRecord(param, _printData._suraStudyrecData1);
            }
            if (_printData._isPrintStudyrecSubstitutionToBiko) {
                // 90の代替備考、90以外の代替備考両方を備考欄に出力する
                printTotalStudyAct = totalStudyAct;
                final List<String> remarkList = new ArrayList<String>();
                if (!notUseBiko) {
                    remarkList.addAll(daitaiBikoNo90All); // 代替科目備考教科コード90以外
                    remarkList.addAll(_printData._studyrecSubstNote.getDaitaiBiko90("getPrintTotalStudyActAndPrintRemark_debug2", "NO_KEY", true, null, param)); // 代替科目備考教科コード90
                    remarkList.add(remark);
                    if (hasJiritsuKatsudouRecord) {
                        remarkList.add(_printData._jiritsuKatudouRemark);
                    }
                }
                printRemark = Util.mkString(remarkList, newLine);
            } else {
                // 通常は90の代替備考を総合的な学習の時間活動内容、90以外の代替備考を備考欄に出力する
                final String daitaiBiko90 = Util.mkString(_printData._studyrecSubstNote.getDaitaiBiko90("getPrintTotalStudyActAndPrintRemark_debug3", "NO_KEY", false, null, param), _SLASH); // 代替科目備考教科コード90
                printTotalStudyAct = Util.mkString(Arrays.asList(daitaiBiko90, totalStudyAct), newLine);
                final List<String> remarkList = new ArrayList<String>();
                if (!notUseBiko) {
                    final String daitaiBikoNo90 = Util.mkString(daitaiBikoNo90All, _SLASH); // 代替科目備考教科コード90以外
                    remarkList.add(daitaiBikoNo90);
                    remarkList.add(remark);
                    if (hasJiritsuKatsudouRecord) {
                        remarkList.add(_printData._jiritsuKatudouRemark);
                    }
                }
                printRemark = Util.mkString(remarkList, newLine);
            }
            return Tuple.of(printTotalStudyAct, printRemark);
        }

        private boolean isSogoMatome() {
            final boolean isSogoMatome = !"1".equals(_printData._tyousasyo2020) && "1".equals(_printData.property(Property.tyousasyoSougouHyoukaNentani)) && (param()._z010.in(Z010Info.Tokiwa, Z010Info.Nishiyama, Z010Info.ChiyodaKudan, Z010Info.Hagoromo) || "1".equals(_printData.property(Property.tyousasyoSougouHyoukaNentaniPrintCombined)));
            return isSogoMatome;
        }

        private boolean sougouHyoukaNentaniPrint() {
            final boolean sougouHyoukaNentaniPrint = !"1".equals(_printData._tyousasyo2020) && "1".equals(_printData.property(Property.tyousasyoSougouHyoukaNentani)) && !isSogoMatome();
            return sougouHyoukaNentaniPrint;
        }

        /**
         *  所見データ
         */
        private void printShoken_1(final TitlePage _titlePage) {

            printTokubetsuKatsudouTitle(_titlePage._titleList);
            printShojikouTitle(_titlePage._titleList);

            if (_printData._notPrintShoken) {
                return;
            }
            if (param()._isOutputDebugShoken) {
                log.info(Util.debugMapToStr("shoken = ", _printData._hexamEntremarkHdat));
                log.info(Util.debugMapToStr("shoken year = ", _printData._hexamEntremarkDatMap));
            }

            if (param()._isOutputDebugShoken) {
                log.info("5.出欠備考");
                log.info("6.特別活動の記録");
            }
            for (final Title title : _titlePage._titleList) {
                final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);

                // 5.出欠備考
                FormRecordPart.printSvfFieldData(this, print5attendrecRemark(title, dat));

                // 6.特別活動の記録
                print6specialActRec(title, dat);
            }

            // 7.指導上参考となる諸事項
            if (param()._isOutputDebugShoken) {
                log.info("7.指導上参考となる諸事項");
            }
            if (_formInfo._isShojikouMatome) {
                print7shojikouTotal(_titlePage);
            } else {
                for (final Title title : _titlePage._titleList) {
                    final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);

                    print7syojikou(title, dat);
                }
            }

            // 8.総合的な学習の時間の内容・評価
            if (param()._isOutputDebugShoken) {
                log.info("8." + _printData.getSogoSubclassname(param()) + "の内容・評価");
            }
            print8Sogaku(_titlePage);

            // 9.備考
            if (param()._isOutputDebugShoken) {
                log.info("9.備考");
            }
            print9biko(_titlePage);

        }

        protected void print8Sogaku(final TitlePage _titlePage) {
            if (_printData._isFuhakkou) {
                return;
            }

            final String propAct = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyactFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyactFieldSize));
            final String propVal = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyvalFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyvalFieldSize));
            if (param()._isOutputDebugShoken) {
                log.info(" propAct = " + propAct);
                log.info(" propVal = " + propVal);
            }
            if (sougouHyoukaNentaniPrint() && !"1".equals(_printData._tyousasyo2020)) {
                final ShokenSize actSize = ShokenSize.get(propAct, 19, 5);
                final ShokenSize valSize = ShokenSize.get(propVal, 19, 6);
                for (final Title title : _titlePage._titleList) {
                    HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);
                    if (null == dat) {
                        dat = new HexamEntremarkDat(new HashMap<String, String>());
                    }

                    //  総合的な学習の時間の内容・評価 (活動内容)
                    print8sougakuGrade(title._pos, "ACTION", actSize, getYearTotalStudyAct(title._year), "1".equals(dat.datTotalstudyactSlashFlg()));
                    //  総合的な学習の時間の内容・評価 (評価)
                    print8sougakuGrade(title._pos, "ASSESS", valSize, getYearTotalStudyVal(title._year), "1".equals(dat.datTotalstudyvalSlashFlg()));
                }
            } else {
                ShokenSize actSize;
                ShokenSize valSize;
                if ("1".equals(_printData._tyousasyo2020)) {
                    actSize = ((FormKNJE070_1_A4.FormInfoKNJE070_1_A4) _formInfo)._A4_totalstudyActShokenSize;
                    valSize = ((FormKNJE070_1_A4.FormInfoKNJE070_1_A4) _formInfo)._A4_totalstudyValShokenSize;
                } else {
                    actSize = ShokenSize.get(propAct, -1, -1);
                    valSize = ShokenSize.get(propVal, -1, -1);
                    if (isSogoMatomeAddHeaderAndAddBlank()) {
                        final int addKeta = getMS932ByteLength("第_学年：");
                        actSize = actSize.addKeta(addKeta);
                        valSize = valSize.addKeta(addKeta);
                    } else if (param()._z010.in(Z010Info.KaichiTsushin)) {
                        final int addKeta = getMS932ByteLength("＿＿＿年度：");
                        actSize = actSize.addKeta(addKeta);
                        valSize = valSize.addKeta(addKeta);
                    }
                }

                final boolean actIsShasen = !isSogoMatome() && "1".equals(getString(_printData._hexamEntremarkHdat, "TOTALSTUDYACT_SLASH_FLG"));
                final boolean valIsShasen = !isSogoMatome() && "1".equals(getString(_printData._hexamEntremarkHdat, "TOTALSTUDYVAL_SLASH_FLG"));
                print8sougakuTannitsu("ACTION", actSize, _printData._isKyoto && !_printData._isTsushin ? 5 : 2, getTotalStudyAct(_titlePage), actIsShasen);
                print8sougakuTannitsu("ASSESS", valSize, _printData._isKyoto && !_printData._isTsushin ? 6 : 3, getTotalStudyVal(_titlePage), valIsShasen);
            }
        }

        private List<List<String>> csvSougaku(final TitlePage _titlePage) {
            final List<List<String>> rtn = new ArrayList<List<String>>();
            final String propAct = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyactFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyactFieldSize));
            final String propVal = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyvalFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyvalFieldSize));
            if (sougouHyoukaNentaniPrint()) {
                final ShokenSize actSize = ShokenSize.get(propAct, 41, 5);
                final ShokenSize valSize = ShokenSize.get(propVal, 41, 6);

                List<String> titleLine = new ArrayList(Arrays.asList(""));
                List<List<String>> katsudo = setLined(Arrays.asList("活動内容"));
                List<List<String>> hyouka = setLined(Arrays.asList("評価"));

                for (final Title title : _titlePage._titleList) {

                    titleLine.add(title._name);

                    List<List<String>> katsudoCol = blankColumn();
                    List<List<String>> hyoukaCol = blankColumn();

                    katsudoCol = setLined(Util.getTokenList(getYearTotalStudyAct(title._year), actSize, param()));
                    hyoukaCol = setLined(Util.getTokenList(getYearTotalStudyVal(title._year), valSize, param()));

                    katsudo = CsvUtils.horizontalUnionLines(katsudo, katsudoCol);
                    hyouka = CsvUtils.horizontalUnionLines(hyouka, hyoukaCol);
                }

                rtn.add(titleLine);
                rtn.addAll(katsudo);
                rtn.addAll(hyouka);

            } else {
                final ShokenSize actSize = ShokenSize.get(propAct, -1, isSogoMatome() ? 4 : _printData._isKyoto && !_printData._isTsushin ? 5 : 2);
                final ShokenSize valSize = ShokenSize.get(propVal, -1, isSogoMatome() ? 4 : _printData._isKyoto && !_printData._isTsushin ? 6 : 3);

                List<List<String>> katsudo = setLined(Arrays.asList("活動内容"));
                katsudo = CsvUtils.horizontalUnionLines(katsudo, setLined(Util.getTokenList(getTotalStudyAct(_titlePage), actSize, param())));
                rtn.addAll(katsudo);

                List<List<String>> hyouka = setLined(Arrays.asList("評価"));
                hyouka = CsvUtils.horizontalUnionLines(hyouka, setLined(Util.getTokenList(getTotalStudyVal(_titlePage), valSize, param())));
                rtn.addAll(hyouka);
            }
            return rtn;
        }

        /**
         *  8.総合的な学習の時間の内容・評価（活動内容）  年度ごと
         *  @param year 年度
         * @return
         */
        public String getYearTotalStudyAct(final String year) {
            HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _printData.titleValues(), year);
            if (null == dat) {
                dat = new HexamEntremarkDat(new HashMap<String, String>());
            }

            return getSubstNotice90Nendogoto(year) + defstr(dat.datTotalstudyact());
        }

        /**
         *  8.総合的な学習の時間の内容・評価（活動内容）
         * @return
         */
        public String getTotalStudyAct(final TitlePage _titlePage) {
            final String propAct = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyactFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyactFieldSize));
            final ShokenSize actSize = ShokenSize.get(propAct, -1, isSogoMatome() ? 4 : _printData._isKyoto && !_printData._isTsushin ? 5 : 2);

            return getPrintTotalStudyActAndPrintRemark(null, actSize, 1, _titlePage)._first;
        }

        /**
         *  8.総合的な学習の時間の内容・評価（評価）  年度ごと
         *  @param year 年度
         * @return
         */
        public String getYearTotalStudyVal(final String year) {
            HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _printData.titleValues(), year);
            if (null == dat) {
                dat = new HexamEntremarkDat(new HashMap<String, String>());
            }
            return defstr(dat.datTotalstudyval());
        }

        /**
         *  8.総合的な学習の時間の内容・評価（評価）
         * @return
         */
        public String getTotalStudyVal(final TitlePage _titlePage) {
            final String propVal = StringUtils.defaultString(_printData.property(Property.tyousasyoTotalstudyvalFieldSizeForPrint), _printData.property(Property.tyousasyoTotalstudyvalFieldSize));
            final ShokenSize matomeValSize = ShokenSize.get(propVal, -1, -1);

            return getTotalStudy(matomeValSize, "TOTALSTUDYVAL", "DAT_TOTALSTUDYVAL", _titlePage);
        }

        private String getSubstNotice90Nendogoto(final String strKey) {
            // 8.総合的な学習の時間の内容と評価[年度毎]
            final String substNotice90;
            if (_printData._isPrintStudyrecSubstitutionToBiko) {
                substNotice90 = "";
            } else {
                substNotice90 = Util.append(Util.mkString(_printData._studyrecSubstNote.getDaitaiBiko90("getSubstNotice90Nendogoto", strKey, false, null, param()), _SLASH), newLine);
            }
            return substNotice90;
        }

        /**
         * "7.指導上参考となる諸事項"を出力する
         */
        private void print7shojikouTotal(final TitlePage _titlePage) {
            if (_printData._isFuhakkou) {
                return;
            }
            if ("1".equals(param()._useSyojikou3)) {
                final List<String> trainRef1List = new LinkedList<String>();
                final List<String> trainRef2List = new LinkedList<String>();
                final List<String> trainRef3List = new LinkedList<String>();
                for (final Title title : _titlePage._titleList) {
                    final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);
                    if (null == dat) {
                        continue;
                    }

                    trainRef1List.add(dat.trainRef1());
                    trainRef2List.add(dat.trainRef2());
                    trainRef3List.add(dat.trainRef3());
                }
                final int[] keta = _formInfo.getField7Keta(_printData);
                //  指導上参考となる諸事項1
                printSvfRepn1("field8_1_1", "", Util.getTokenList(Util.mkString(trainRef1List, newLine), ShokenSize.get(null, keta[0] / 2, 21), param()));

                //  指導上参考となる諸事項2
                printSvfRepn1("field8_2_1", "", Util.getTokenList(Util.mkString(trainRef2List, newLine), ShokenSize.get(null, keta[1] / 2, 21), param()));

                //  指導上参考となる諸事項3
                printSvfRepn1("field8_3_1", "", Util.getTokenList(Util.mkString(trainRef3List, newLine), ShokenSize.get(null, keta[2] / 2, 21), param()));
            }
        }

        /**
         * 出欠データ出力
         **/
        protected FormRecord getAttendFormRecord(final boolean isFormatOnly, final TitlePage _titlePage) {
            final FormRecord formRecord = new FormRecord();
            if (isFormatOnly) {
                return formRecord;
            }
            final boolean isNendo = PrintData.PRINT_NENDO == _printData.getPrintGradeTitleMethod(param());
            for (final Title title : _titlePage._titleList) {
                final List<String> nameArray = title._nameArrayAttend;
                final int pos = title._pos;
                if (_formInfo._isShingakuyouFormShukketsuTitleFormat2) {
                    if ("1".equals(_printData._tyousasyo2020)) {
                        if (isNendo || Title.NYUGAKUMAE.equals(title._name)) {
                            formRecord.setData("GRADE3_" + pos + "_1" , title._name);  // 出欠の記録　左
                            formRecord.setData("GRADE6_" + pos + "_1" , title._name);  // 出欠の記録　右
                        } else {
                            formRecord.setData("GRADE3_" + pos + "_2" , nameArray.get(1));  // 出欠の記録　左
                            formRecord.setData("GRADE6_" + pos + "_2" , nameArray.get(1));  // 出欠の記録　右
                        }
                    } else {
                        formRecord.setData("GRADE3_" + pos + "_1" , title._name);  // 出欠の記録　左
                        formRecord.setData("GRADE6_" + pos + "_1" , title._name);  // 出欠の記録　右
                    }
                } else if (param()._z010.in(Z010Info.Tokiwa) && !"1".equals(_printData._tyousasyo2020)) {
                    if (NumberUtils.isDigits(title._annual)) {
                        formRecord.setData("GRADE3_" + pos + "_1" , String.valueOf(Integer.parseInt(title._annual)));  // 出欠の記録　左
                        formRecord.setData("GRADE6_" + pos + "_1" , String.valueOf(Integer.parseInt(title._annual)));  // 出欠の記録　右
                    }
                } else if (isNendo || Title.NYUGAKUMAE.equals(title._name)) {
                    formRecord.setData("GRADE3_" + pos + "_1" , nameArray.get(0));  // 出欠の記録　左
                    formRecord.setData("GRADE3_" + pos + "_2" , nameArray.get(1));
                    formRecord.setData("GRADE3_" + pos + "_3" , nameArray.get(2));
                    formRecord.setData("GRADE6_" + pos + "_1" , nameArray.get(0));  // 出欠の記録　右
                    formRecord.setData("GRADE6_" + pos + "_2" , nameArray.get(1));
                    formRecord.setData("GRADE6_" + pos + "_3" , nameArray.get(2));
                } else {
                    formRecord.setData("GRADE3_" + pos + "_2" , nameArray.get(1));  // 出欠の記録　左
                    formRecord.setData("GRADE6_" + pos + "_2" , nameArray.get(1));  // 出欠の記録　右
                }
            }
            if (isNendo) {
                final SvfField field = getField("KUBUN1");
                final String kubun;
                if (null != field && field._fieldLength <= 6) {
                    kubun = "年度";
                } else {
                    kubun = "区分＼年度";
                }
                for (int i = 1; i <= 2; i++) {
                    formRecord.setData("KUBUN" + String.valueOf(i), kubun); // 出欠の記録項目名
                }
            }

            if (!_printData._isFuhakkou) {
                if (param()._isOutputDebug) {
                    log.info(Util.debugMapToStr(" attendMap = ", _printData._attendMap));
                }
                final String slashImagePath = param()._slashImagePath;
                for (final Title title : _titlePage._titleList) {
                    final int i = title._pos;
                    AttendrecDat att = null;
                    for (final Map.Entry<String, AttendrecDat> p : _printData._attendMap.entrySet()) {
                        final Title t = Title.getTitle(_param, _titlePage._titleList, p.getKey());
                        if (t == title) {
                            att = _printData.getAttendrecDat(p.getKey());
                            break;
                        }
                    }
                    final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);
                    if (null == att || att.isEmpty()) {
                        if (!"1".equals(_printData._tyousasyo2020)) {
                            if (_printData._isHirokoudaiTsushin) {
                                formRecord.setDatan("ATTEND_SLASH1", i, slashImagePath);
                                formRecord.setDatan("ATTEND_SLASH2", i, slashImagePath);
                                if (null == dat || StringUtils.isBlank(attendrecRemark(dat, param(), _printData, title))) {
                                    formRecord.setData("ATTEND_SLASH3_" + i + "g", slashImagePath);
                                }
                            }
                        }
                        continue;
                    }
                    if (_printData._isMiyagikenTsushin && AttendrecDat.isNull(att, _printData)) {
                        continue;
                    }
                    final String jugyoNissu;        // 授業日数
                    final String shutteiKibiki;       // 出席停止・忌引き等の日数
                    final String ryugaku;          // 留学中の授業日数
                    final String subeki;  // 出席しなければならない日数
                    final String kesseki;        // 欠席日数
                    final String shusseki;     // 出席日数
                    boolean setKeta = false;
                    if (_param._z010.in(Z010Info.naraken) && "1".equals(_printData.getParameter(Parameter.tyousasyo2020))) {
                        final AttendrecDat offAtt = _printData.getOffdaysDropAttendrecDat(title);
                        if (null != offAtt) {
                            jugyoNissu = defstr(offAtt.attend1()) + " " + StringUtils.leftPad(defstr(att.attend1()), 3);
                            shutteiKibiki = defstr(offAtt.suspMour()) + " " + StringUtils.leftPad(defstr(att.suspMour()), 3);
                            ryugaku = defstr(offAtt.abroad()) + " " + StringUtils.leftPad(defstr(att.abroad()), 3);
                            subeki = defstr(offAtt.requirepresent()) + " " + StringUtils.leftPad(defstr(att.requirepresent()), 3);
                            kesseki = defstr(offAtt.attend6()) + " " + StringUtils.leftPad(defstr(att.attend6()), 3);
                            shusseki = defstr(offAtt.present()) + " " + StringUtils.leftPad(defstr(att.present()), 3);
                        } else {
                            jugyoNissu = defstr(att.attend1());
                            shutteiKibiki = defstr(att.suspMour());
                            ryugaku = defstr(att.abroad());
                            subeki = defstr(att.requirepresent());
                            kesseki = defstr(att.attend6());
                            shusseki = defstr(att.present());
                            setKeta = true;
                        }
                    } else {
                        jugyoNissu = defstr(att.attend1());
                        shutteiKibiki = defstr(att.suspMour());
                        ryugaku = defstr(att.abroad());
                        subeki = defstr(att.requirepresent());
                        kesseki = defstr(att.attend6());
                        shusseki = defstr(att.present());
                    }

                    final List<Tuple<String, String>> fieldAndValues = Arrays.asList(
                            Tuple.of(FIELD_LESSON, jugyoNissu),
                            Tuple.of(FIELD_SPECIAL, shutteiKibiki),
                            Tuple.of(FIELD_ABROAD, ryugaku),
                            Tuple.of(FIELD_PRESENT, subeki),
                            Tuple.of(FIELD_ABSENCE, kesseki),
                            Tuple.of(FIELD_ATTEND, shusseki)
                            );
                    if (_printData._isHirokoudaiTsushin) {
                        if ("0".equals(att.schoolcd())) {
                            // 本校の場合、出校日数のみ印字し残りはスラッシュ
                            if (!"1".equals(_printData._tyousasyo2020)) {
                                formRecord.setDatan("ATTEND_SLASH1", i, slashImagePath);
                                formRecord.setDatan("ATTEND_SLASH2", i, slashImagePath);
                                if (null == dat || StringUtils.isBlank(attendrecRemark(dat, param(), _printData, title))) {
                                    formRecord.setData("ATTEND_SLASH3_" + i + "g", slashImagePath);
                                }
                            }
                            formRecord.setDatan(FIELD_ATTEND, i,  shusseki);
                            continue;
                        }
                    } else if (_printData._isSagakenTsushin) {
                        for (final Tuple<String, String> fieldAndValue : fieldAndValues) {
                            final String fieldname = fieldAndValue._first;
                            final String val = fieldAndValue._second;
                            formRecord.setDatan(fieldname, i, val, Util.toInt(val, 0) > 0); // 1以上を表示
                        }
                        continue;
                    } else if (_printData._isConfigFormAttendAllSlash) {
                        if (!AttendrecDat.isNull(att, _printData)) {
                            for (final Tuple<String, String> fieldAndValue : fieldAndValues) {
                                final String fieldname = fieldAndValue._first;
                                final String val = fieldAndValue._second;
                                formRecord.setDatan(fieldname, i, val, Util.toInt(val, -1) > -1); // 0以上を表示
                            }
                        }
                        continue;
                    }
                    for (final Tuple<String, String> fieldAndValue : fieldAndValues) {
                        final String fieldname = fieldAndValue._first;
                        final String val = fieldAndValue._second;
                        formRecord.setDatan(fieldname, i, val);
                    }
                    if (setKeta) {
                        for (final Tuple<String, String> fieldAndValue : fieldAndValues) {
                            final String fieldname = fieldAndValue._first;
                            formRecord.addAttrn(fieldname, i, "KETA=5");
                        }
                    }
                }
            }
            return formRecord;
        }

        private boolean isSogoMatomeAddHeaderAndAddBlank() {
            return param()._z010.in(Z010Info.NaganoSeisen);
        }

        private String getTotalStudy(final ShokenSize matomeSize, final String field, final String datField, final TitlePage _titlePage) {
            final boolean isMatome = isSogoMatome();
            final boolean isAddHeaderIfHasData = param()._z010.in(Z010Info.KaichiTsushin);
            final boolean isAddHeader = isSogoMatomeAddHeaderAndAddBlank() || param()._z010.in(Z010Info.Tokiwa) || isAddHeaderIfHasData;
            final StringBuffer stb = new StringBuffer();
            if (isMatome) {
                // まとめでは斜線フラグを使用しない
                final List<String> textList = new ArrayList<String>();
                for (final Title title : _titlePage._titleList) {
                    final HexamEntremarkDat hed = _printData.getHexamEntremarkDat(_param, _titlePage._titleList, title._year);
                    final String dat = null == hed ? "" : defstr(hed.data(datField));
                    if (isAddHeaderIfHasData && StringUtils.isBlank(dat)) {
                        continue;
                    }
                    final String titlename = isAddHeader ? title._name + "：" : "";
                    if (isSogoMatomeAddHeaderAndAddBlank()) {
                        final List<String> tokenList = Util.getTokenList(dat, null == matomeSize ? 10000 : matomeSize._moji * 2, param());
                        if (tokenList.size() == 0) {
                            tokenList.add("");
                        }
                        final List<String> headerList = new ArrayList<String>(tokenList);
                        Collections.fill(headerList, StringUtils.repeat(" ", getMS932ByteLength(titlename)));
                        headerList.set(0, titlename);
                        textList.addAll(mergeLines(headerList, tokenList));
                    } else {
                        final String add = titlename + dat;
                        if (StringUtils.isBlank(add)) {
                            continue;
                        }
                        textList.add(add);
                    }
                }
                stb.append(Util.mkString(textList, newLine));
            } else {
                stb.append(defstr(getString(_printData._hexamEntremarkHdat, field)));
            }
            return stb.toString();
        }

        private static List<String> mergeLines(final List<String> ... lineLists) {
            final List<String> lines = new ArrayList<String>();
            if (null != lineLists) {
                int maxLines = 0;
                for (final List<String> lineList : lineLists) {
                    maxLines = Math.max(maxLines, lineList.size());
                }
                for (int i = 0; i < maxLines; i++) {
                    final List<String> line = new ArrayList<String>();
                    for (final List<String> lineList : lineLists) {
                        if (i < lineList.size()) {
                            line.add(defstr(lineList.get(i)));
                        }
                    }
                    lines.add(Util.mkString(line, ""));
                }
            }
            return lines;
        }

        private int printSvfRep(final String field, final List<String> tokenList) {
            return printSvfRep(field, tokenList, 1);
        }

        private int printSvfRep(final String field, final List<String> tokenList, final int start) {
            if (tokenList.isEmpty()) {
                return 0;
            }
            final int intoadiv = Integer.parseInt(_printData._os);
            for (int i = 0; i < tokenList.size(); i++) {
                svfVrsOut(field + (i + start) + ((intoadiv == 1) ? "" : "_2K"), tokenList.get(i));
            }
            return tokenList.size();
        }

        private int printSvfRepN(final String field, final int g, final List<String> tokenList) {
            return printSvfRepN(field, g, tokenList, 1);
        }

        private int printSvfRepN(final String field, final int g, final List<String> tokenList, final int start) {
            if (tokenList.isEmpty()) {
                return 0;
            }
            final int intoadiv = Integer.parseInt(_printData._os);
            for (int i = 0; i < tokenList.size(); i++) {
                svfVrsOutn(field + (i + start) + ((intoadiv == 1) ? "" : "_2K"), g, tokenList.get(i));
            }
            return tokenList.size();
        }

        protected int printSvfNRepLine(final String field, final List<String> tokenList) {
            if (tokenList.isEmpty()) {
                return 0;
            }
            for (int i = 0; i < tokenList.size(); i++) {
                svfVrsOutn(field, i + 1, tokenList.get(i));
            }
            return tokenList.size();
        }

        protected int printSvfNRepLineFormRecord(final FormRecord formRecord, final String field, final List<String> tokenList) {
            if (tokenList.isEmpty()) {
                return 0;
            }
            for (int i = 0; i < tokenList.size(); i++) {
                formRecord.setDatan(field, i + 1, tokenList.get(i));
            }
            return tokenList.size();
        }


        /**
         * 総合的な学習の時間（単一）出力
         * @param field
         * @param size
         * @param deflines
         * @param totalStudy
         * @param isShasen
         */
        private void print8sougakuTannitsu(final String field, final ShokenSize size, final int deflines, final String totalStudy, final boolean isShasen) {

            final boolean isSogoMatome = isSogoMatome();

            final int intoadiv = Integer.parseInt(_printData._os);
            final int keta;
            int lines = deflines;
            if (isSogoMatomeAddHeaderAndAddBlank() || param()._z010.in(Z010Info.KaichiTsushin) && !"1".equals(_printData._tyousasyo2020)) {
                keta = size.keta();
                lines = size._gyo;
                lines *= 3;
            } else if (param()._z010.in(Z010Info.Tokiwa) && !"1".equals(_printData._tyousasyo2020)) {
                keta = 46 * 2;
            } else if (_printData._isKyoto && !_printData._isTsushin && !"1".equals(_printData._tyousasyo2020)) {
                final int chars = (intoadiv == 1 ? 41 : 44);
                keta = chars * 2;
            } else if (-1 != size._moji && -1 != size._gyo) {
                final int chars = size._moji;
                lines = size._gyo;
                if (isSogoMatome) {
                    lines *= 3;
                }
                keta = chars * 2;
            } else if (param()._z010.in(Z010Info.Nishiyama)) {
                final int chars = 40;
                keta = chars * 2;
            } else {
                final int chars = (intoadiv == 1 ? 41 : 44);
                keta = chars * 2;
            }
            ShokenSize size1 = ShokenSize.getWithKeta(keta, lines);
            if (param()._isOutputDebug) {
                log.info("shokenType8 = " + _formInfo.shokenType8());
            }
            if (_formInfo.shokenType8() == FormInfo.ShokenType.TYPE2) {
                if (param()._z010.in(Z010Info.Hirokoudai)) {
                    fieldModifyAndPrintRepeatPreferPointThanKeta(field, 9.7, totalStudy);
                } else if (param()._z010.in(Z010Info.Tosa)) {
                    fieldModifyAndPrintRepeatPreferPoint(field, 8.7, totalStudy);
                } else {
                    if (param()._z010.in(Z010Info.Miyagiken, Z010Info.NaganoSeisen)) {
                        size1 = size1.addKeta(1);
                    }
                    fieldModifyAndPrintRepeatPreferKeta(field, size1.keta(), totalStudy, null);
                }
            } else if (_formInfo.shokenType8() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                if (param()._z010.in(Z010Info.Miyagiken, Z010Info.NaganoSeisen)) {
                    size1 = size1.addKeta(1);
                }
                printSvfNRepLine(field, Util.getTokenList(totalStudy, size1, param()));
            } else {
                final int lines2;
                if (isSogoMatome) {
                    lines2 = 4;
                } else {
                    lines2 = lines;
                }
                ShokenSize size2 = ShokenSize.getWithKeta(keta, lines2);
                if (param()._z010.in(Z010Info.Miyagiken, Z010Info.NaganoSeisen)) {
                    size2 = size2.addKeta(1);
                }

                if (_formInfo._isShingakuyouKenja && !param()._z010.in(Z010Info.Sapporo)) {
                    final List<String> tokenList = Util.getTokenList(totalStudy, size2.keta(), param());
                    final int tokyoline = deflines;
                    final String head;
                    if (tokenList.size() > tokyoline) {
                        head = field + "1";
                    } else {
                        head = field;
                    }
                    for (int i = 0; i < tokenList.size(); i++) {
                        svfVrsOut(head + (i + 1), tokenList.get(i));
                    }
                } else {
                    if (isShasen) {
                        svfVrImageOut(field + "_SLASH", param()._slashImagePath);
                    }
                    printSvfRep(field, Util.getTokenList(totalStudy, size2, param()));
                }
            }
        }

        protected Tuple<List<String>, List<String>> getBikoKotei() {
            final List<String> koteiBefore = new ArrayList<String>();
            final List<String> koteiAfter = new ArrayList<String>();
            if (param()._z010.in(Z010Info.Meiji)) {
                koteiBefore.addAll(Arrays.asList(
                    "「Catholic Spirit」は教育課程の特例として文部科学省より認可された科目である。総合的な学",
                    "習の時間と特別活動を削減してこれに充てる(平成23年度より)。評価は次の通りである。"
                ));
                if (_printData._schoolInfo.isPrintSchoolRemark && !"1".equals(_printData._tyousasyo2020)) {
                    koteiAfter.addAll(_printData.getNotNullCertifRemark123List());
                }
            }
            if (_printData._isPrintCertifSchoolDatRemark123ToField9 && !"1".equals(_printData._tyousasyo2020)) {
                koteiBefore.addAll(_printData.getNotNullCertifRemark123List());
            }
            if (null != _printData._bikoKoteiText && "1".equals(_printData._tyousasyo2020)) {
                koteiAfter.add(_printData._bikoKoteiText);
            }
            return Tuple.of(koteiBefore, koteiAfter);
        }

        private ShokenSize bikoSize() {
            int moji;
            int gyo;
            final String _tyousasyoRemarkFieldSize = _printData.property(Property.tyousasyoRemarkFieldSize); // 9.備考フィールドサイズ
            final String prop = StringUtils.defaultString(_printData.property(Property.tyousasyoRemarkFieldSizeForPrint), _tyousasyoRemarkFieldSize);
            if (!StringUtils.isBlank(prop)) {
                final ShokenSize size = ShokenSize.get(prop, 41, 5);
                moji = size._moji;
                gyo = size._gyo;
            } else {
                moji = Integer.parseInt(_printData._os) == 1 ? 41 : 44;
                gyo = (param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai) || _printData._isMiekenForm) ? 4 : 5;
            }
            return ShokenSize.get(null, moji, gyo);
        }

        // 9.備考
        private void print9biko(final TitlePage _titlePage) {
            final String remarkData = defstr(getString(_printData._hexamEntremarkHdat, "REMARK"));
            if (_formInfo.shokenType9() == FormInfo.ShokenType.TYPE2) {
                Map prop = null;
                if (_formInfo._field9RightX != -1) {
                    prop = new HashMap();
                    Util.getMappedHashMap(prop, _formInfo._formName + "." + "field9").put("X2", String.valueOf(_formInfo._field9RightX));
                }
                fieldModifyAndPrintRepeatPreferKeta("field9", bikoSize().keta(), getPrintRemark(remarkData, _titlePage), prop);
            } else if (_formInfo.shokenType9() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                printSvfNRepLine("field9", Util.getTokenList(getPrintRemark(remarkData, _titlePage), bikoSize().keta(), param()));
            } else {
                printSvfRep("field9_", getPrintRemarkList(getPrintRemark(remarkData, _titlePage)));
            }
        }

        protected String getPrintRemark(final String remarkData, final TitlePage _titlePage) {
            final String remark;
            if (_printData._isFuhakkou) {
                remark = getFuhakkouRemark();
            } else {
                remark = getPrintTotalStudyActAndPrintRemark(remarkData, null, 2, _titlePage)._second;
            }
            if (param()._isOutputDebugShoken) {
                log.info(" remark = " + remark);
            }
            return remark;
        }

        private List<String> getPrintRemarkList(final String remark) {

            final String _tyousasyoRemarkFieldSize = _printData.property(Property.tyousasyoRemarkFieldSize); // 9.備考フィールドサイズ
            final String prop = StringUtils.defaultString(_printData.property(Property.tyousasyoRemarkFieldSizeForPrint), _tyousasyoRemarkFieldSize);

            ShokenSize size = bikoSize();
            final Tuple<List<String>, List<String>> kotei = getBikoKotei();
            final List<String> koteiBefore = kotei._first;
            final List<String> koteiAfter = kotei._second;

            final List<String> printRemarkList = new ArrayList<String>();
            if (koteiBefore.size() > 0) {
                if (StringUtils.isBlank(prop)) {
                    size = size.setMoji(44);
                }
                printRemarkList.addAll(Util.getTokenList(Util.concat(koteiBefore), ShokenSize.get(null, size._moji, koteiBefore.size()), param()));
            }
            printRemarkList.addAll(Util.getTokenList(remark, ShokenSize.get(null, size._moji, size._gyo), param()));
            if (koteiAfter.size() > 0) {
                printRemarkList.addAll(Util.getTokenList(Util.concat(koteiAfter), ShokenSize.get(null, size._moji, koteiAfter.size()), param()));
            }
            return printRemarkList;
        }

        protected ShokenSize attendrecRemarkSize() {
            final String prop = StringUtils.defaultString(_printData.property(Property.tyousasyoAttendrecRemarkFieldSizeForPrint), _printData.property(Property.tyousasyoAttendrecRemarkFieldSize));
            final ShokenSize size = ShokenSize.get(prop, 5, 3);
            return size;
        }

        /**
         * 出力用出欠備考 (進学用)
         * @param dat
         * @param param
         * @param printData
         * @param strKey "0"もしくは年度
         * @return 出力用出欠備考 (進学用)
         */
        static String attendrecRemark(final HexamEntremarkDat dat, final Param param, final PrintData printData, final Title title) {
            final List<String> list = new ArrayList<String>();
            if (printData._isHirokoudaiTsushin) {
                final AttendrecDat att = printData.getAttendrecDat(title._year);
                if (null != att) {
                    if ("1".equals(att.schoolcd()) && Util.toInt(att.schoolcd0Present(), 0) > 0) {
                        list.add("うち本校出校日数" + att.schoolcd0Present() + "日");
                    }
                }
            }
            if (null != dat) {
                list.add(dat.attendrecRemark());
            }
            if (param._z010.in(Z010Info.naraken) && "1".equals(printData.getParameter(Parameter.tyousasyo2020))) {
                final AttendrecDat offAtt = printData.getOffdaysDropAttendrecDat(title);
                if (null != offAtt) {
                    list.add("８．備考に記載");
                }
            }
            if (param._isOutputDebugShoken) {
                log.info(" attendrecRemeark = " + title._year + ":" + list);
            }
            return Util.mkString(list, newLine);
        }

        static boolean attendRemarkIsSlash(final PrintData printData, final Param param, final Title title) {
            final boolean isA4 = "1".equals(printData._tyousasyo2020);
            final HexamEntremarkDat dat = printData.getHexamEntremarkDat(param, printData.titleValues(), title._year);
            final String attendrecRemark = attendrecRemark(dat, param, printData, title);
            boolean isSlash = false;
            if (param._z010.in(Z010Info.naraken) && !isA4 && (null == dat || StringUtils.isBlank(attendrecRemark))) {
                isSlash = true;
            } else if (null != dat) {
                isSlash = "1".equals(dat.attendrecRemarkSlashFlg());
            }
            return isSlash;
        }

        protected FormRecord print5attendrecRemark(final Title title, final HexamEntremarkDat dat) {
            final FormRecord formRecord = new FormRecord();
            if (_printData._isFuhakkou) {
                return formRecord;
            }
            String attendrecRemark = attendrecRemark(dat, param(), _printData, title);
            if ("1".equals(_printData.property(Property.tyousasyoPrintAttendrecRemarkKaikinDat))) {
                attendrecRemark = Util.mkString(Arrays.asList(_printData._yearKaikinDatTextMap.get(title._year), attendrecRemark), newLine);
            }

            final boolean isSlash = attendRemarkIsSlash(_printData, param(), title);
            final int g = title._pos;
            if (_printData._isMiyagikenTsushin) {
                final AttendrecDat att = _printData.getAttendrecDat(title._year);
                boolean attendIsNull = AttendrecDat.isNull(att, _printData);
                if (attendIsNull) {
                    if (!"1".equals(_printData._tyousasyo2020)) {
                        svfVrsOutn("LESSON_SLASH", g, _SLASH);
                        svfVrsOutn("SPECIAL_SLASH", g, _SLASH);
                        svfVrsOutn("ABROAD_SLASH", g, _SLASH);
                        svfVrsOutn("PRESENT_SLASH", g, _SLASH);
                        svfVrsOutn("ABSENCE_SLASH", g, _SLASH);
                        svfVrsOutn("ATTEND_SLASH", g, _SLASH);
                    }
                    attendrecRemark = TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI;
                }
            } else if (_printData._isSagakenTsushin) {
                final AttendrecDat att = _printData.getAttendrecDat(title._year);
                boolean attendIsNull = AttendrecDat.isNull(att, _printData);
                if (attendIsNull) {
                    attendrecRemark = TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI;
                }
            } else if (_printData._isFormAttendAllSlash) {
                final AttendrecDat att = _printData.getAttendrecDat(title._year);
                boolean attendIsNull = AttendrecDat.isNull(att, _printData);
                if (attendIsNull) {
                    // 進学用は前に備考をつけない
//                    attendrecRemark = Util.append(attendrecRemark, newLine) + TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI;
                    attendrecRemark = TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI;
                }
            } else if (param()._z010.in(Z010Info.Musashinohigashi)) {
                final StringBuffer stb = new StringBuffer();
//                String late = null; //遅刻
//                String early = null; //早退
//                String mourning = null; //忌引
//                String suspend = null; //出停
//
//                final Map map = Util.getMappedHashMap(_printData._attendMap, strKey);
//                if (null != map && !map.isEmpty()) {
//                    late = getString(map, "LATE");
//                    early = getString(map, "EARLY");
//                    mourning = getString(map, "MOURNING");
//                    suspend = getString(map, "SUSPEND");
//                }
//                stb.append("遅刻 " + defstr(late) + "回　");
//                stb.append("早退 " + defstr(early) +"回　");
//                stb.append("忌引 " + defstr(mourning) + "日　");
//                stb.append("出停 " + defstr(suspend) + "日");
//
//                if (stb.length() > 0) {
//                    stb.append(newLine);
//                }
                stb.append(defstr(attendrecRemark));
                attendrecRemark = stb.toString();
            }
            final boolean notPrint = StringUtils.isBlank(attendrecRemark) && isSlash == false;
            if (param()._isOutputDebug) {
                log.info(" attendrecmark notPrint = " + notPrint + ", attendrecRemark = " + attendrecRemark + " / title = " + title);
            }
            if (notPrint) {
                return formRecord;
            }

            final String sg = String.valueOf(g);
            ShokenSize size;
            final String prop = StringUtils.defaultString(_printData.property(Property.tyousasyoAttendrecRemarkFieldSizeForPrint), _printData.property(Property.tyousasyoAttendrecRemarkFieldSize));
            if (_formInfo.shokenType5() == FormInfo.ShokenType.TYPE2) {

                if (param()._z010.in(Z010Info.Hirokoudai)) {
                    fieldModifyAndPrintRepeatPreferPointThanKeta("NOTE_" + sg + "g", 10.0, attendrecRemark);
                } else {
                    size = ShokenSize.get(prop, 5, 3);
                    fieldModifyAndPrintRepeatPreferKeta("NOTE_" + sg + "g", size.keta(), attendrecRemark, null);
                }

                return formRecord;
            } else if (_formInfo.shokenType5() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                if (_printData._isHirokoudaiTsushin && StringUtils.isBlank(attendrecRemark)) {
                    formRecord.setData("ATTEND_SLASH3_" + sg + "g", param()._slashImagePath);
                } else {
                    size = "1".equals(_printData._tyousasyo2020) ? ShokenSize.get(null, 8, Util.toInt(_printData.property(Property.tyousasho2020AttendremarkGyou), 7)) : ShokenSize.get(prop, 5, 3);
                    printSvfNRepLineFormRecord(formRecord, "NOTE_" + sg + "g", Util.getTokenList(attendrecRemark, size, param()));
                    if (param()._isOutputDebug) {
                        log.info(" attend remark size = " + size + ", formRecord = " + formRecord);
                    }
                }
                return formRecord;
            } else if (param()._z010.in(Z010Info.Fukuiken)) {
                size = ShokenSize.get(prop, 10, 5);
                log.info(" size --> " + size);

//            } else if (_formInfo._shokenType == FormInfo.SHOKEN_TYPE1) {
//                final String cacheKey = _currentForm + "ATTENDREC_REMARK_AVAIRABLE_SIZE";
//                if (null == param()._sessionCache.get(cacheKey)) {
//
//                    final List fieldBlockList = Util.getMappedList(KNJSvfFieldAreaInfo.getSearchFieldResult("NOTE(.*)_1", _currentForm, param().formFieldInfoMap(), param()._isOutputDebugField, KNJSvfFieldAreaInfo.searchMethodRegex, KNJSvfFieldAreaInfo.targetGroup1), "resultList"); // 出欠備考の1行目のフィールドを検索
//                    final TreeMap availableFields = new TreeMap();
//                    for (final Iterator it = fieldBlockList.iterator(); it.hasNext();) {
//                        final String block = (String) it.next();
//
//                        final List fields = Util.getMappedList(KNJSvfFieldAreaInfo.getSearchFieldResult("NOTE" + block + "_(.*)", _currentForm, param().formFieldInfoMap(), param()._isOutputDebugField, KNJSvfFieldAreaInfo.searchMethodRegex, KNJSvfFieldAreaInfo.targetSvfField), "resultList"); // 出欠備考の頭が同一のフィールドを検索
//                        if (fields.isEmpty()) {
//                            continue;
//                        }
//                        int minKeta = 9999;
//                        for (final Iterator fldit = fields.iterator(); fldit.hasNext();) {
//                            final SvfField field = (SvfField) fldit.next();
//                            minKeta = Math.min(minKeta, field._fieldLength);
//                        }
//                        //log.debug(" block = " + block + " -> " + ((SvfField) fields.get(0))._name + ": line = " + fields.size());
//                        // フィールドの数が行数
//                        availableFields.put(Tuple.of(new Integer(minKeta), new Integer(fields.size())), block);
//                    }
//                    if (param()._isOutputDebugField) {
//                        log.info(" 使用可能桁x行 = " + availableFields);
//                    }
//                    param()._sessionCache.put(cacheKey, availableFields);
//                    log.info(" create cache Key = " + cacheKey);
//                }
//                final TreeMap availableFields = (TreeMap) param()._sessionCache.get(cacheKey);
//                ShokenSize largestSize = null;
//                ShokenSize okSize = null;
//                for (final Iterator it = Util.reverse(availableFields.entrySet()).iterator(); it.hasNext();) { // 降順
//                    final Map.Entry e = (Map.Entry) it.next();
//                    final Tuple t = (Tuple) e.getKey();
//                    final int keta = ((Integer) t._first).intValue();
//                    final int gyo = ((Integer) t._second).intValue();
//                    final List tokenList = Util.getTokenList(attendrecRemark, keta, param());
//                    if (param()._isOutputDebugField) {
//                    	log.info("出欠備考フィールド = " + t);
//                    }
//                    if (tokenList.size() <= gyo) {
//                        okSize = ShokenSize.get(null, keta / 2, gyo);
//                    }
//                    if (null == largestSize) {
//                        largestSize = ShokenSize.get(null, keta / 2, gyo);
//                    }
//                }
//                if (null == okSize) {
//                    size = largestSize;
//                    log.warn("出欠備考が桁あふれ: grade = " + g + ", 最大 = " + size);
//                } else {
//                    size = okSize;
//                    if (param()._isOutputDebugField) {
//                    	log.info("出欠備考フィールド = " + size);
//                    }
//                }
//                if (null == size) {
//                    log.warn("出欠備考フィールド無し");
//                    return;
//                }
            } else {
                size = ShokenSize.get(prop, 5, 3);
            }
            final String nums;
            if (size._moji == 5 && size._gyo == 3) {
                nums = "";
            } else {
                final DecimalFormat zz = new DecimalFormat("00");
                nums = "_" + zz.format(size._moji) + zz.format(size._gyo);
            }
            if (isSlash) {
//            		int x = -1;
//            		int y = -1;
//                    final List fields = Util.getMappedList(KNJSvfFieldAreaInfo.getSearchFieldResult("NOTE" + nums + "_1", _currentForm, param().formFieldInfoMap(), param()._isOutputDebugField, KNJSvfFieldAreaInfo.searchMethodRegex, KNJSvfFieldAreaInfo.targetSvfField), "resultList"); // 出欠備考の頭が同一のフィールドを検索
//                    for (final Iterator fldit = fields.iterator(); fldit.hasNext();) {
//                        final SvfField field = (SvfField) fldit.next();
//                        x = field.x();
//                        y = field.y();
//                    }
//                    IForm.SvfLine shasen = getShasen(x, y);
//                    log.info(" shasen = " + shasen);
//                    if (null != shasen) {
//                    	Util.getMappedList(Util.getMappedHashMap(_printData._paramap, "PAGE_POST_PROCESS"), "LINE_INFO").add(shasen.toMap());
//                    }
                    svfVrImageOutn("ATTENDREC_REMARK_SLASH", g, param()._slashImagePath);
            }
            if (param()._z010.in(Z010Info.Miyagiken, Z010Info.NaganoSeisen, Z010Info.Yamamura)) {
                size = size.addKeta(1);
            }
            return formRecord.merge(printSvfRepnGradeFormRecord("NOTE" + nums + "_", g, "", Util.getTokenList(attendrecRemark, size, param())));
        }

        /**
         * 特別活動の記録を印字
         */
        protected void print6specialActRec(final Title title, final HexamEntremarkDat dat) {
            if (_printData._isFuhakkou) {
                return;
            }
            if (null == dat) {
                return;
            }

            final String sg = String.valueOf(title._pos);
            final ShokenSize size = specialActRecShokenSize();
            if (param()._isOutputDebugShoken) {
                log.info(" specialActRecSize = " + size);
            }
            if (_formInfo.shokenType6() == FormInfo.ShokenType.TYPE2) {
                if (param()._z010.in(Z010Info.Hirokoudai)) {
                    fieldModifyAndPrintRepeatPreferPointThanKeta("field7_" + sg + "g", 9.7, dat.specialactrec());
                } else if (param()._z010.in(Z010Info.Tosa)) {
                    fieldModifyAndPrintRepeatPreferPoint("field7_" + sg + "g", 8.0, dat.specialactrec());
                } else {
                    fieldModifyAndPrintRepeatPreferKeta("field7_" + sg + "g", size.keta(), dat.specialactrec(), null);
                }
            } else if (_formInfo.shokenType6() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                printSvfNRepLine("field7_" + sg + "g", Util.getTokenList(dat.specialactrec(), size, param()));

            } else {
                final List<String> tokenList = Util.getTokenList(dat.specialactrec(), size, param());
                for (int i = 0; i < tokenList.size(); i++) {
                    final String suffix = (_formInfo._isKumamotoForm && size.keta() == 24 && !_formInfo._isKyotoForm) ? "_2" : (Integer.parseInt(_printData._os) == 1 ? "" : "_2K");
                    svfVrsOutn("field7_" + (i + 1) + suffix, title._pos, tokenList.get(i));
                }
            }
        }

        protected ShokenSize specialActRecShokenSize() {
            if ("1".equals(_printData._tyousasyo2020)) {
                return ((FormKNJE070_1_A4.FormInfoKNJE070_1_A4) _formInfo)._A4_spActSize;
            }
            final int intoadiv = Integer.parseInt(_printData._os);
            final int defmoji;
            final int defgyo;
            if (_printData._isKyoto && !_printData._isTsushin) {
                defmoji = 13;
                defgyo = 8;
            } else if (param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama)) {
                defmoji = (intoadiv == 1 ? 15 : 16);
                defgyo = 10;
            } else {
                defmoji = (intoadiv == 1 ? 11 : 12);
                defgyo = 8;
            }
            final String prop = defstr(_printData.property(Property.tyousasyoSpecialactrecFieldSizeForPrint), _printData.property(Property.tyousasyoSpecialactrecFieldSize));
            ShokenSize size = ShokenSize.get(prop, defmoji, defgyo);
            if (param()._z010.in(Z010Info.Miyagiken, Z010Info.NaganoSeisen, Z010Info.Yamamura)) {
                size = size.addKeta(1);
            }
            return size;
        }

        /**
         * "7.指導上参考となる諸事項"を出力する
         */
        private void print7syojikou(final Title title, final HexamEntremarkDat dat) {
            if (_printData._isFuhakkou) {
                return;
            }
            final int g = title._pos;
            final boolean isFormFormat2 = param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama) || _printData._isKyoto && !_printData._isTsushin || _formInfo._isKumamotoForm;
            if (!"1".equals(param()._useSyojikou3)) {
                if (null != dat) {
                    final int intoadiv = Integer.parseInt(_printData._os);
                    final int moji = "1".equals(_printData.property(Property.tyousasyoTokuBetuFieldSize)) ? ((intoadiv == 1) ? 58 : 59) : ((intoadiv == 1) ? 41 : 42);
                    final int gyo = isFormFormat2 ? 7 : 5;

                    //  指導上参考となる諸事項
                    FormRecordPart.printSvfFieldData(this, printSvfRepnGradeFormRecord("field8_", g, "", Util.getTokenList(dat.trainRef(), ShokenSize.get(null, moji, gyo), param())));
                }
                return;
            }

            final String trainRef1 = (null == dat ? "" : dat.trainRef1());
            final String trainRef2 = (null == dat ? "" : dat.trainRef2());
            final String trainRef3 = (null == dat ? "" : dat.trainRef3());
            final int[] keta = _formInfo.getField7Keta(_printData);
            final int keta1 = keta[0];
            final int keta2 = keta[1];
            final int keta3 = keta[2];
            final int gyo = Util.toInt(StringUtils.defaultString(_printData.property(Property.train_ref_1_2_3_gyo_sizeForPrint), _printData.property(Property.train_ref_1_2_3_gyo_size)), 5);

            if (_formInfo._is6nenyouKenja) {
                final int field8MaxLine1_d  = 28;
                final int field8MaxLine1_6  = 42;
                final int field8MaxLine2_d = _formInfo.trainRef123FieldSize1(_printData) ? 42 : 28;
                final int field8MaxLine2_6 = _formInfo.trainRef123FieldSize1(_printData) ? 62 : 42;
                final int field8MaxLine3_d = _formInfo.trainRef123FieldSize1(_printData) ? 14 : 28;
                final int field8MaxLine3_6 = _formInfo.trainRef123FieldSize1(_printData) ? 20 : 42;

//                    log.debug(field8MaxLine_d  + ":"+ field8MaxLine_6 + " , " + field8MaxLine2_d + ":" + field8MaxLine2_6 + "," + field8MaxLine3_d + ":" + field8MaxLine3_6);

                print7shojikou6nen("field8_1_", g, trainRef1, field8MaxLine1_d, field8MaxLine1_6); //  指導上参考となる諸事項1
                print7shojikou6nen("field8_2_", g, trainRef2, field8MaxLine2_d, field8MaxLine2_6); //  指導上参考となる諸事項2
                print7shojikou6nen("field8_3_", g, trainRef3, field8MaxLine3_d, field8MaxLine3_6); //  指導上参考となる諸事項3

            } else if (_formInfo.shokenType7() == FormInfo.ShokenType.TYPE2) {
                if (param()._z010.in(Z010Info.Hirokoudai, Z010Info.Fukuiken)) {
                    final double pointDef;
                    if (param()._z010.in(Z010Info.Fukuiken)) {
                        pointDef = 8.7;
                    } else {
                        pointDef = 9.7;
                    }
                    fieldModifyAndPrintRepeatPreferPointThanKeta("field8_" + g + "g_1", pointDef, trainRef1);
                    fieldModifyAndPrintRepeatPreferPointThanKeta("field8_" + g + "g_2", pointDef, trainRef2);
                    fieldModifyAndPrintRepeatPreferPointThanKeta("field8_" + g + "g_3", pointDef, trainRef3);
                } else if (param()._z010.in(Z010Info.Tosa)) {
                    final double pointDef = 9.7;
                    fieldModifyAndPrintRepeatPreferPoint("field8_" + g + "g_1", pointDef, trainRef1);
                    fieldModifyAndPrintRepeatPreferPoint("field8_" + g + "g_2", pointDef, trainRef2);
                    fieldModifyAndPrintRepeatPreferPoint("field8_" + g + "g_3", pointDef, trainRef3);
                } else {
                    fieldModifyAndPrintRepeatPreferKeta("field8_" + g + "g_1", keta1, trainRef1, null);
                    fieldModifyAndPrintRepeatPreferKeta("field8_" + g + "g_2", keta2, trainRef2, null);
                    fieldModifyAndPrintRepeatPreferKeta("field8_" + g + "g_3", keta3, trainRef3, null);
                }
            } else if (_formInfo.shokenType7() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                int addKeta = 0;
                if (param()._z010.in(Z010Info.NaganoSeisen)) {
                    addKeta = 1;
                }
                printSvfNRepLine("field8_" + g + "g_1", Util.getTokenList(trainRef1, keta1 + addKeta, param()));
                printSvfNRepLine("field8_" + g + "g_2", Util.getTokenList(trainRef2, keta2 + addKeta, param()));
                printSvfNRepLine("field8_" + g + "g_3", Util.getTokenList(trainRef3, keta3 + addKeta, param()));
//                } else if (_formInfo._shokenType == FormInfo.SHOKEN_TYPE1) {
//
//                    final String[] divs = {"1", "2", "3"};
//                    final String[] trainRefs = {trainRef1, trainRef2, trainRef3};
//
//                    //debugFormInfo(param());
//
//                    for (int i = 0; i < divs.length; i++) {
//                        final String div = divs[i];
//                        final String trainRefN = trainRefs[i];
//                        final int keta_ = -1;
//
//                        final boolean isRepeat = true;
//                        final Tuple ketaLine = getFieldModifyInfo(isRepeat, "field8_" + div + "_", g, "_1", keta_, trainRefN, "SVF_FIELD_TRAIN_REF" + div + "_N", "field8_" + div + "_(.*)");
//                        if (null != ketaLine) {
//                            final int modifyMoji = ((Integer) ketaLine._first).intValue();
//                            final int modifyLines = ((Integer) ketaLine._second).intValue();
//                            printSvfRepnGrade("field8_" + div + "_", g, "_1", Util.getTokenList(trainRefN, ShokenSize.get(null, modifyMoji, modifyLines), param())); //  指導上参考となる諸事項1,2,3
//                        }
//                    }

            } else {
                final int[] moji = _formInfo.getField7moji(_printData);
                final int moji1 = moji[0];
                final int moji2 = moji[1];
                final int moji3 = moji[2];
                final int gyo_2 = param()._z010.in(Z010Info.RitsumeikanKeisho) ? gyo : 7;
                final String f1, f2, f3;
                if (param()._z010.in(Z010Info.Bunkyo, Z010Info.Sapporo, Z010Info.RitsumeikanKeisho) || _formInfo._isShingakuyouKenja && gyo > 5) {
                    f1 = "_1";
                    f2 = "_1";
                    f3 = "_1";
                } else if (param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai) || _printData._isKyoto && !_printData._isTsushin || _formInfo._isKumamotoForm) {
                    final int gyo1 = isFormFormat2 ? (getMS932ByteLength(trainRef1) > 6 * keta1 ? 7 : 6) : gyo;
                    final int gyo2 = isFormFormat2 ? (getMS932ByteLength(trainRef2) > 6 * keta2 ? 7 : 6) : gyo;
                    final int gyo3 = isFormFormat2 ? (getMS932ByteLength(trainRef3) > 6 * keta3 ? 7 : 6) : gyo;
                    f1 = gyo1 == 6 && Util.getNotNullCount(Util.getTokenList(trainRef1, ShokenSize.get(null, moji1, 7), param())) < 7 ? "_1" : "";
                    f2 = gyo2 == 6 && Util.getNotNullCount(Util.getTokenList(trainRef2, ShokenSize.get(null, moji2, 7), param())) < 7 ? "_1" : "";
                    f3 = gyo3 == 6 && Util.getNotNullCount(Util.getTokenList(trainRef3, ShokenSize.get(null, moji3, 7), param())) < 7 ? "_1" : "";
                } else {
                    f1 = "";
                    f2 = "";
                    f3 = "";
                }
                int add = 0;
                if (param()._z010.in(Z010Info.Miyagiken) && !_printData._isTsushin || param()._z010.in(Z010Info.naraken) || param()._z010.in(Z010Info.Yamamura)) {
                    add = 1; // 入力画面にあわせる
                }
                FormRecordPart.printSvfFieldData(this, printSvfRepnGradeFormRecord("field8_1_", g, f1, Util.getTokenList(trainRef1, ShokenSize.getWithKeta(moji1 * 2 + add, gyo_2), param()))); //  指導上参考となる諸事項1
                FormRecordPart.printSvfFieldData(this, printSvfRepnGradeFormRecord("field8_2_", g, f2, Util.getTokenList(trainRef2, ShokenSize.getWithKeta(moji2 * 2 + add, gyo_2), param()))); //  指導上参考となる諸事項2
                FormRecordPart.printSvfFieldData(this, printSvfRepnGradeFormRecord("field8_3_", g, f3, Util.getTokenList(trainRef3, ShokenSize.getWithKeta(moji3 * 2 + add, gyo_2), param()))); //  指導上参考となる諸事項3
            }
        }

        private void fieldModifyAndPrintRepeatPreferKeta(final String fieldname, final int preferKeta, final String data, final Map prop) {
            final SvfFieldAreaInfo.ModifyParam modifyParam = new SvfFieldAreaInfo.ModifyParam();
            modifyParam._usePreferPoint = false;
            modifyParam._preferKeta = preferKeta;
//        	final Thread th = new Thread(new Runnable() {
//        		public void run() {
                    fieldModifyAndPrintRepeat(fieldname, modifyParam, data, prop);
//        		}
//        	});
//        	Thread wait = new Thread(new Runnable() {
//        		public void run() {
//        			try {
//        				// 10秒待機
//        				for (int i = 0; i < 10; i++) {
//        					Thread.sleep(1000);
//        					if ((i + 1) % 10 == 0) {
//        						log.info("waiting kinsoku " + (i + 1));
//        					}
//        				}
//        			} catch (InterruptedException ignore) {
//        			}
//        			if (th.isAlive()) {
//        				th.interrupt();
//        			}
//        		}
//        	});
//        	wait.start();
//        	th.start();
//        	try {
//        		th.join();
//        		if (wait.isAlive()) {
//        			wait.interrupt();
//        		}
//        	} catch (Exception e) {
//        		log.error("exception! fieldname = " + fieldname + ", preferKeta = " + preferKeta + ", data = " + data, e);
//        	}
        }

        private void fieldModifyAndPrintRepeatPreferPointThanKeta(final String fieldname, final double preferPointKeta, final String data) {
            SvfFieldAreaInfo.ModifyParam modifyParam = new SvfFieldAreaInfo.ModifyParam();
            modifyParam._usePreferPointThanKeta = true;
            modifyParam._preferPointThanKeta = preferPointKeta;
            fieldModifyAndPrintRepeat(fieldname, modifyParam, data, null);
        }

        private void fieldModifyAndPrintRepeatPreferPoint(final String fieldname, final double preferPoint, final String data) {
            SvfFieldAreaInfo.ModifyParam modifyParam = new SvfFieldAreaInfo.ModifyParam();
            modifyParam._usePreferPoint = true;
            modifyParam._preferPoint = preferPoint;
            fieldModifyAndPrintRepeat(fieldname, modifyParam, data, null);
        }

        private void fieldModifyAndPrintRepeat(final String fieldname, final SvfFieldAreaInfo.ModifyParam modifyParam, final String data, final Map prop) {
            final SvfField field = getField(fieldname);
            if (null == field) {
                return;
            }
            modifyParam._repeatCount = field._fieldRepeatCount;
            try {
                modifyParam._otherParam.clear();
                if (null != prop) {
                    modifyParam._otherParam.putAll(prop);
                }
            } catch (Throwable t) {
                if (param()._isOutputDebugField) {
                    log.info(t);
                }
            }
            param()._fieldAreaInfo._param._isOutputDebug = param().isOutputDebugField(fieldname);

            final Map modifyFieldInfoMap = param()._fieldAreaInfo.getModifyFieldInfoMap(param().formFieldInfoMap(), _currentForm, fieldname, modifyParam, data);

            final int fieldKeta = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_KETA", new Integer(0)).intValue();
            final int fieldLine = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_LINE", new Integer(0)).intValue();
            if (fieldKeta > 0) {
                final List<Map<String, String>> repeatList = Util.getMappedList(modifyFieldInfoMap, "REPEAT");
                for (int gi = 0; gi < repeatList.size(); gi++) {
                    final Map<String, String> attrMap = repeatList.get(gi);
                    svfVrAttributen(fieldname, gi + 1, getString(attrMap, "FIELD_ATTR"));
                }
                printSvfNRepLine(fieldname, Util.getTokenList(data, ShokenSize.getWithKeta(fieldKeta, fieldLine), param()));
            }
        }

        private boolean useShojikouField2Kinsoku(final String str, final int keta, final int gyo) {
            boolean rtn = false;
            if ("1".equals(param()._useSyojikou3)) {

                final List<String> hyphendTokenList = Util.KNJ_EditKinsokuGetTokenList(str, keta, param());
                if ((param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai) || _formInfo._isKumamotoForm) && gyo == 6 && hyphendTokenList.size() < 7) {
                    rtn = true;
                } else {
                    // 京都府・明治は禁則処理では7行固定
                    rtn = false;
                }
            }
            return rtn;
        }

        private void print7shojikou6nen(final String field, final int g, final String trainRef, final int keta1, final int keta2) {
            List<String> tokenList = Util.getTokenList(trainRef, keta1, param());
            String suf = "";
            if (tokenList.size() > 3) { // 3行を超えたら文字の小さいフィールドに印字
                suf = "_2";
                List<String> tokenList2 = Util.getTokenList(trainRef, keta2, param());
                if (tokenList2.size() <= 4) {
                    tokenList = tokenList2;
                } else {
                    tokenList = recons(param(), tokenList, keta2);
                }
            }
            for (int i = 0; i < tokenList.size(); i++) {
                svfVrsOutn(field + (i + 1) + suf, g, tokenList.get(i));
            }
        }

        private static List<String> recons(final Param param, final List<String> srcList, final int keta) {
            List<String> rtn = new ArrayList();
            rtn.addAll(Util.getTokenList(Util.concat(srcList.subList(0, 3)), keta, param)); // 最初の3行を「文字の小さいフィールド」の文字幅で行を取得。
            if (rtn.size() > 2) {
                rtn = rtn.subList(0, 2); // 「文字の小さいフィールド」の文字幅でも2行を超えたら最初の2行のみ取得。
            }
            rtn.addAll(Util.getTokenList(Util.concat(srcList.subList(3, srcList.size())), keta, param)); // 最初の3行以降を「文字の小さいフィールド」の文字幅で行を取得。
            return rtn;
        }

        private void print8sougakuGrade(final int g, final String field, ShokenSize size, final String dat, final boolean isShasen) {
            final String sg = String.valueOf(g);

            if (isShasen) {
                // 宮城県に合わせた
                svfVrImageOutn(field + "_SLASH", g, param()._slashImagePath);
            }
            if (param()._z010.in(Z010Info.Miyagiken, Z010Info.Yamamura)) {
                size = size.addKeta(1);
            }
            final List<String> sogaku = Util.getTokenList(dat, size, param());
            if (_formInfo.shokenType8() == FormInfo.ShokenType.TYPE2) {
                fieldModifyAndPrintRepeatPreferKeta(field + sg + "g", size.keta(), dat, null);
            } else {
                if (_formInfo.shokenType8() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                    printSvfNRepLine(field + sg + "g", sogaku);
                } else if (param()._z010.in(Z010Info.Miyagiken)) {
                    printSvfRepN(field + "1_", g, sogaku);
                } else {
                    printSvfRep(field + sg + "_", sogaku);
                }
            }
        }

        /**
         * 成績段階別人数をフォームへ出力
         */
        protected void printSeisekiDankai_1() {
            if (!_printData._geneviewmbrMap.isEmpty()) {
                final Map m = (Map) _printData._geneviewmbrMap.get(_printData._geneviewmbrMap.keySet().iterator().next());
                svfVrsOutn("level", 1, defstr(getString(m, "MEMBER5")));
                svfVrsOutn("level", 2, defstr(getString(m, "MEMBER4")));
                svfVrsOutn("level", 3, defstr(getString(m, "MEMBER3")));
                svfVrsOutn("level", 4, defstr(getString(m, "MEMBER2")));
                svfVrsOutn("level", 5, defstr(getString(m, "MEMBER1")));
                svfVrsOut("level_2",  defstr(getString(m, "MEMBER0")));
                final String member6 = getString(m, "MEMBER6");

                final String comment;
                if (_printData.paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(_printData.getParameter(Parameter.GVAL_CALC_CHECK)) && "1".equals(_printData.getParameter(Parameter.PRINT_AVG_RANK))) {
                    comment = "　　" + defstr(_printData._tajuHeikinRank, " 　") + " 位"; // 多重平均の席次
                    svfVrAttribute("level_3", ATTRIBUTE_MUHENSYU);
                } else {
                    comment = _printData._comment == null ? "" : " ( " + (NumberUtils.isDigits(member6) ? String.valueOf(Integer.parseInt(member6)) : "") + " 人)";
                }
                svfVrsOut("level_3", comment);
            }

            svfVrsOut("R2", defstr(_printData._assessMark));    //学習成績概評
            svfVrsOut("MARU", defstr(_printData._hexamEntremarkHdatMark));    //特Ａ
        }

        /**
         * 学習の記録出力
         */
        protected FormRecordPart setForm1Record(final boolean isFormatOnly, final TitlePage _titlePage) {
            final String certifKindCd = _printData.getParameter(Parameter.CERTIFKIND);

            FormRecordPart formRecord = new FormRecordPart();

//            String s_subclasscd = "00"; //科目コード
//            String s_classcd = "00";    //教科コード
//            String s_specialDiv = "00"; //専門教育に関する教科・科目
//            String s_schoolKind = "00";    //学校種別
            formRecord._linex = 0;                          //行数
//            int lhrline = 0; // LHR出力行調整
            formRecord._lineMax12 =  _printData.isCsv() ? 9999 : _formInfo._isShingakuyouFormRecordFormat2 ? 44 : 52 ; //科目欄の行数（左＋右）：熊本=44,その他=52
            formRecord._lineMax1  = _printData.isCsv() ? 9999 : _formInfo._isShingakuyouFormRecordFormat2 ? 22 : 26 ; //科目欄の行数（左）　　：熊本=22,その他=26

            final String SUBCLASSNAMEfieldname = _printData._isHankiNinteiForm ? "SUBCLASSNAME_HANKI" : "SUBCLASSNAME";
            final String CLASSNAMEfieldname = _printData._isHankiNinteiForm ? "CLASSNAME_HANKI" : "CLASSNAME";
            final String CLASSNAME2fieldname = _printData._isHankiNinteiForm ? "CLASSNAME_HANKI_2" : "CLASSNAME_2";
            final String CLASSCDfieldname = _printData._isHankiNinteiForm ? "CLASSCD_HANKI" : "CLASSCD";

            final int xgap = param()._z010.in(Z010Info.Sundaikoufu) && !"1".equals(_printData._tyousasyo2020) ? -35 : 0;
            final SvfField subclassnameField = getField(SUBCLASSNAMEfieldname);
            formRecord._subclassnameInfo._x1 = 452 + 10 + xgap;
            formRecord._subcx1End = (((_formInfo._isKumamotoForm && !_formInfo._is6nen) || param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama) || (_formInfo._isKyotoForm && !_formInfo._is6nen)) || param()._z010.in(Z010Info.Sakae) ? 1042 : 934) + xgap;
            formRecord._subclassnameInfo._x2 = ("1".equals(_printData._tyousasyo2020) ? 1983 : 1938) + 10 + xgap;
            formRecord._subcWidth = formRecord._subcx1End - formRecord._subclassnameInfo._x1 - 10;
            formRecord._subclassnameInfo._height = _formInfo._isShingakuyouFormRecordFormat2 ? 98 : 78;
            formRecord._subclassnameInfo._ystart = 1594 -  formRecord._subclassnameInfo._height;
            formRecord._subclassnameInfo._minnum = _formInfo._isShingakuyouFormRecordFormat2 ? (param()._z010.in(Z010Info.Sakae) ? 18 : 16) : 20;
            formRecord._subclassnameInfo._maxnum = (null == subclassnameField ? 40 : subclassnameField._fieldLength);

//            final List searchSubclassnameResultList = Util.getMappedList(getSearchFieldResult(SUBCLASSNAMEfield, param(), searchMethodIndexOf, targetSvfField), "resultList"); // 科目名
//            log.debug(" subclassname = " + searchSubclassnameResultList);

            final SvfField classnameField = getField(CLASSNAMEfieldname);
            formRecord._classnameInfo._x1 = 156 + 10 + xgap;  //左列の開始Ｘ軸
            formRecord._clx1End = (_formInfo._is6nen ? 432 : 452) + xgap;
            formRecord._classnameInfo._x2 = ("1".equals(_printData._tyousasyo2020) ? 1683 : 1642) + 10 + xgap;  //右列の開始Ｘ軸
            formRecord._clWidth = formRecord._clx1End - 30 - formRecord._classnameInfo._x1;
            formRecord._classnameInfo._height = _formInfo._isShingakuyouFormRecordFormat2 ? 98 : 78;
            formRecord._classnameInfo._ystart = 1594 - formRecord._classnameInfo._height;
            formRecord._classnameInfo._minnum = _formInfo._isShingakuyouFormRecordFormat2 ? 8 : 10;
            formRecord._classnameInfo._maxnum = (null == classnameField ? 20 : classnameField._fieldLength);
            if (param()._isOutputDebugField && param().isOutputDebugField(CLASSNAMEfieldname)) {
                log.info(" field    classname = " + classnameField);
            }
            if (param()._isOutputDebugField && param().isOutputDebugField(SUBCLASSNAMEfieldname)) {
                log.info(" field subclassname = " + subclassnameField);
            }

            final SvfField classname2Field = getField(CLASSNAME2fieldname);

            final Map mst = new TreeMap();
            final List<String> specialdivList = Util.getMappedList(mst, "SPECIALDIVS");
            final List<StudyrecDat> printTargetStudyrecDatList = isFormatOnly ? new ArrayList<StudyrecDat>() : _printData.getPrintTargetStudyrecDatList(param());
            for (final StudyrecDat studyrecDat : printTargetStudyrecDatList) {
                final String specialDiv;
                if ("1".equals(_printData._notUseClassMstSpecialDiv)) {
                    specialDiv = "X";
                } else {
                    specialDiv = StringUtils.defaultString(studyrecDat._specialDiv, "XX");
                }

                final String classKey = studyrecDat.keyClasscd(param());
                if (!specialdivList.contains(specialDiv)) {
                    specialdivList.add(specialDiv);
                    mst.put("SPECIALDIV" + specialDiv + "CLASSCD", studyrecDat._classcd);
                }

                final String keySubclassCd = studyrecDat.keySubclasscd(param());
                if (!Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv).contains(classKey)) {
                    Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv).add(classKey);
                    mst.put("SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "CLASSNAME", studyrecDat._classname);
                }

                if (!Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey).contains(keySubclassCd)) {
                    Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey).add(keySubclassCd);
                }

                Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "SUBCLASS" + keySubclassCd).add(studyrecDat);
                mst.put("SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "SUBCLASS" + keySubclassCd + "SUBCLASSNAME", studyrecDat._subclassname);
            }

            for (int spi = 0; spi < specialdivList.size(); spi++) {
                final String specialDiv = specialdivList.get(spi);

                if (param().isPrintClassTitle(certifKindCd, _printData) && !"1".equals(_printData._notUseClassMstSpecialDiv) && !param().notHasCertifSchoolDatOrKindai()) {
                    if (0 != spi) {
                        if (formRecord._linex == formRecord._lineMax1 - 1 || formRecord._linex == formRecord._lineMax12 - 1) {
                            final FormRecord recordData = formRecord.createRecord(formRecord._linex);
                            recordData.setData(CLASSCDfieldname, getString(mst, "SPECIALDIV" + specialDiv + "CLASSCD"));
                            formRecord._linex++;
                        }
                    }
                    final FormRecord record = formRecord.createRecord(formRecord._linex);
                    final String specialDivName = param().getSpecialDivName(Param.isNewForm(param(), _printData), specialDiv);
                    record.setData("CLASSTITLE", specialDivName);
                    formRecord._linex++;

                    if (param()._isOutputDebugSeiseki) {
                        log.info(" specialDivName = " + specialDivName);
                    }

                    hasdata = true;
                }

                final List<String> classKeyList = Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv);

                for (int cli = 0; cli < classKeyList.size(); cli++) {
                    final String classKey = classKeyList.get(cli);
                    final String classname = getString(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "CLASSNAME");

                    final List<String> keySubclasscdList = Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey);
                    for (int subi = 0; subi < keySubclasscdList.size(); subi++) {
                        final String keySubclassCd = keySubclasscdList.get(subi);
                        final String subclassname = getString(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "SUBCLASS" + keySubclassCd + "SUBCLASSNAME");

                        final FormRecord record = formRecord.createRecord(formRecord._linex);

                        if (subclassname != null) {
                            if (_printData._vNameMstD077name2List.contains(keySubclassCd)) {
                                record.setData(ITEMfield, subclassname);
                            } else {
                                setSvfFieldAttribute1(record._attrMap, subclassname, formRecord._linex + 1, formRecord._lineMax1, SUBCLASSNAMEfieldname, formRecord._subcWidth, formRecord._subclassnameInfo);
                                record.setData(SUBCLASSNAMEfieldname, subclassname);
                            }
                        }

                        //  教科コードのブレイク 左右一段目は教科名を出力する
                        if ((subi == 0 || formRecord._linex == formRecord._lineMax1) && !_printData._vNameMstD077name2List.contains(keySubclassCd)) {
                            final int classnameKeta = getMS932ByteLength(classname);
                            if (null != classnameField && classnameField._fieldLength < classnameKeta && null != classname2Field) {
                                // 2行表示
                                final String[] token = KNJ_EditEdit.get_token(classname, classname2Field._fieldLength, 2);
                                final String[] fieldnames = {CLASSNAMEfieldname, classname2Field._name};
                                for (int i = 0; i < Math.min(token.length, fieldnames.length); i++) {
                                    record.setData(fieldnames[i], token[i]);
                                }
                            } else {
                                setSvfFieldAttribute1(record._attrMap, classname, formRecord._linex + 1, formRecord._lineMax1, CLASSNAMEfieldname, formRecord._clWidth, formRecord._classnameInfo);
                                record.setData(CLASSNAMEfieldname, classname);
                            }
                        }

                        final List<StudyrecDat> studyrecDatList = Util.getMappedList(mst, "SAME_SPECIALDIV" + specialDiv + "CLASS" + classKey + "SUBCLASS" + keySubclassCd);
                        for (int sti = 0; sti < studyrecDatList.size(); sti++) {
                            final StudyrecDat studyrecDat = studyrecDatList.get(sti);

                            if (sti == 0) {
                                final String CREDITfieldname;
                                if (_printData._vNameMstD077name2List.contains(keySubclassCd)) {
                                    CREDITfieldname = TOTAL_CREDITfield;
                                } else {
                                    final String ccd = studyrecDat._classcd + ("1".equals(param()._useCurriculumcd) ? studyrecDat._schoolKind : "");
                                    record.setData(CLASSCDfieldname, defstr(ccd));
                                    CREDITfieldname = _printData._isHankiNinteiForm ? "CREDIT_HANKI" : "CREDIT";
                                }
                                final String credit = studyrecDat.credit();
                                String pCredit = null;
                                if (param()._isPrintCompCreditWhenCreditIsZero && (null == credit || Util.toInt(credit, -1) == 0 && Util.toInt(studyrecDat.compCredit(), 0) > 0)) {
                                    pCredit = "(" + studyrecDat.compCredit() + ")";
                                } else if (credit != null) {
                                    //log.info(" subclasscd = " + keySubclassCd + ", credit = " + credit + " / " + studyrecDat.getNotDropGradesList());
                                    pCredit = credit;
                                }
                                if (null != pCredit) {
                                    record.setData(CREDITfieldname, pCredit);
                                }
                            }

                            Title titleSchoolcd1 = null;
                            if (param()._z010.in(Z010Info.KaichiTsushin)) {
                                for (final Title title1 : _titlePage._titleList) {
                                    if (title1._seisekiPos == 0) {
                                        // 入学前
                                        titleSchoolcd1 = title1;
                                        break;
                                    }
                                }
                            }
                            if (_printData._vNameMstD077name2List.contains(keySubclassCd)) {
                                if (param()._isOutputDebugSeiseki) {
                                    log.info(" subclass " + keySubclassCd + " in D077(NAME2) hyotei slash. ");
                                }
                            } else if (_printData._vNameMstD077List.contains(keySubclassCd)) {
                                if (param()._isOutputDebugSeiseki) {
                                    log.info(" subclass " + keySubclassCd + " in D077 hyotei slash. ");
                                }
                                if (null != param()._knje070GradeSlashImagePath) {
                                    if (_printData._isHankiNinteiForm) {
                                        for (int i = 0; i < 12; i++) {
                                            record.setDatan("GRADE_HANKI_SLASH", i + 1, param()._knje070GradeSlashImagePath); // 前期
                                        }
                                    } else {
                                        for (int i = 0; i < 6; i++) {
                                            record.setDatan("GRADE_SLASH", i + 1, param()._knje070GradeSlashImagePath); // 前期
                                        }
                                    }
                                }
                            } else {
                                if (param()._isOutputDebugSeiseki) {
                                    log.info(" subclass " + keySubclassCd + " " + subclassname);
                                }
                                //  科目評定(学年)の出力
                                for (final Grades grades : studyrecDat.getNotDropGradesList()) {
                                    Title title = null;
                                    if (param()._z010.in(Z010Info.KaichiTsushin)) {
                                        if ("1".equals(grades._schoolcd)) {
                                            title = titleSchoolcd1;
                                            if (null == title) {
                                                log.info(" not found title (pos = 0 ). : " + grades._year);
                                            }
                                        } else {
                                            title = Title.getTitle(_param, _titlePage._titleList, grades._year);
                                        }
                                    } else {
                                        title = Title.getTitle(_param, _titlePage._titleList, grades._year);
                                    }
                                    if (null == title) {
                                        if (param()._isOutputDebugSeiseki) {
                                            log.warn(" null title : year = " + grades._year + " / titleList = " + _titlePage._titleList);
                                        }
                                        continue;
                                    }
                                    if (param()._isOutputDebugSeiseki) {
                                        log.info("  hyotei " + grades + ". ");
                                    }
                                    if (_printData._isFuhakkou) {
                                        // 表示しない
                                    } else {
                                        if (_printData._isHankiNinteiForm) {
                                            final String pgrade1 = grades.getPrintGrade(param(), grades._zenkiGrades);
                                            record.setData("GRADES" + title._seisekiPos + "_HANKI_1", pgrade1); // 前期
                                            String pgrade2 = null;
                                            if (null != grades._koukiGrades) {
                                                pgrade2 = grades.getPrintGrade(param(), grades._koukiGrades);
                                            } else if (null == grades._zenkiGrades && null != grades._grades) {
                                                pgrade2 = grades.getPrintGrade(param(), grades._grades);
                                            } else if (SqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS.equals(grades._recordFlg)) {
                                                pgrade2 = "*";
                                            }
                                            record.setData("GRADES" + title._seisekiPos + "_HANKI_2", pgrade2); // 後期
                                        } else {
                                            String pgrade = null;

                                            if ("print100".equals(_printData.getParameter(Parameter.HYOTEI))) {
                                                final String subclassMstKey;
                                                if ("1".equals(param()._useCurriculumcd)) {
                                                    subclassMstKey = SubclassMst.key(param(), studyrecDat._classcd, studyrecDat._schoolKind, studyrecDat._curriculumCd, studyrecDat._subclasscd);
                                                } else {
                                                    subclassMstKey = SubclassMst.key(param(), null, null, null, studyrecDat._subclasscd);
                                                }
                                                final SubclassMst sclm = SubclassMst.getSubclassMst(param()._subclassMstMap, subclassMstKey);

                                                pgrade = _printData._gakunenSeiseki.getGakunenSeisekiString(sclm, grades._year);
                                            } else if (null != grades._grades) {
                                                pgrade = grades.getPrintGrade(param(), grades._grades);
                                            } else if (SqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS.equals(grades._recordFlg)) {
                                                pgrade = "*";
                                            }
                                            record.setData("GRADES" + title._seisekiPos, pgrade);
                                        }
                                    }
                                }
                            }

                            formRecord._linex++;
                        }
                    }
                }
            }
            return formRecord;
        }

        /**
         * 学習の記録出力
         */
        protected void printStudyrec_1(final boolean isFormatOnly, final List<FormRecord> recordList, boolean isLast, final TitlePage titlePage) {
            if (!isFormatOnly) {
                printSeisekiTitle(titlePage);
            }

            FormRecordPart.printSvfFieldData(this, _printData.formRecord._fieldData);
            FormRecordPart.printSvfRecordList(this, recordList);

            final int linex = recordList.size();

            if (isLast) {
                final List<Map<String, String>> suraList = isFormatOnly ? new ArrayList<Map<String, String>>() : _printData._form1SuraList;
                for (int i = linex; i < _printData.formRecord._lineMax12 - suraList.size(); i++) { // 空行
                    final String CLASSCDfieldname = _printData._isHankiNinteiForm ? "CLASSCD_HANKI" : "CLASSCD";
                    svfVrsOut(CLASSCDfieldname, String.valueOf(i));  // 教科コード
                    svfVrEndRecord();
                    hasdata = true;
                }
                for (final Map<String, String> suraMap : suraList) {
                    if (param()._isOutputDebugSeiseki) {
                        log.info(" sura = " + suraMap);
                    }
                    for (final String field : suraMap.keySet()) {
                        final String fieldname = _printData._isHankiNinteiForm ? field + "_HANKI" : field;
                        if (field.startsWith("#")) {
                            continue;
                        } else if (Arrays.asList(TOTAL_CREDITfield, TOTAL_CREDIT2field, TOTAL_CREDIT_DOTfield, TOTAL_CREDIT_DOT_HANKIfield).contains(field)) {
                            final String credit = suraMap.get(field);
                            if (credit != null) { svfVrsOut(fieldname, credit); }
                        } else {
                            final String item = suraMap.get(field);
                            if ("1".equals(_printData._tyousasyo2020)) {
                                svfVrsOutForData(Arrays.asList(fieldname, fieldname + "_2", fieldname + "_3", fieldname + "_4"), item, false);
                            } else {
                                svfVrsOut(fieldname, item);
                            }
                        }
                    }
                    svfVrEndRecord();
                }
                hasdata = true;
            }
        }

        // 評定平均を出力する
        protected void printHyoteiHeikin(final boolean isFormatOnly) {
            if (isFormatOnly || _printData._isFuhakkou) {
                // 表示しない
                return;
            }
            final List<HyoteiHeikin> _hyoteiHeikinList = isFormatOnly ? new ArrayList<HyoteiHeikin>() : _printData._hyoteiHeikinList;
            if (param()._isOutputDebugSeiseki) {
                log.info(" hyotei heikin count = " + _hyoteiHeikinList.size());
            }
            final SvfField averageField = getField("average_1");
            boolean averageFieldKurikaeshi = false;
            if (null != averageField) {
                averageFieldKurikaeshi = averageField._fieldRepeatCount > 1;
            }
            if (param()._isOutputDebug) {
                log.info(" averageFieldKurikaeshi = " + averageFieldKurikaeshi);
            }
            for (int i = 0; i < _hyoteiHeikinList.size(); i++) {
                final HyoteiHeikin avgRec = _hyoteiHeikinList.get(i);
                final int avg_line = i + 1;
                if (param()._isOutputDebugSeiseki) {
                    log.info(" hyotei heikin " + i + " : " + avgRec.classkey() + " " + avgRec.classname() + " = " + avgRec.avg());
                }
                if (averageFieldKurikaeshi) {
                    final String line = 10 < avg_line ? "11" : "1";
                    final int col = 10 < avg_line ? avg_line - 10 : avg_line;
                    final int classnameKeta = getMS932ByteLength(avgRec.classname());
                    final SvfField field1 = getField("subject" + line + "_1");
                    final String field = getFieldForData(Arrays.asList("subject" + line + "_1", "subject" + line + "_2", "subject" + line + "_3"), avgRec.classname(), false);
                    svfVrsOutn(field, col, defstr(avgRec.classname()));  //教科名
                    svfVrsOutn("average_" + line, col, defstr(avgRec.avg()));  //評定
                    SvfField svfField = getField(field);
                    if (null != svfField) {
                        if ((param()._z010.in(Z010Info.Sakae) || "1".equals(_printData._tyousasyo2020)) && ("subject" + line + "_1").equals(field) && classnameKeta <= (Integer.parseInt((String) svfField.getAttributeMap().get(SvfField.AttributeKeta)))) {
                            // Yセンタリング
                            svfVrAttributen(field, col, attributeIntPlus(field, "Y", 20));  //教科名
                        } else if ("1".equals(_printData._tyousasyo2020) && ("subject" + line + "_3").equals(field) && classnameKeta <= field1._fieldLength * 3) {
                            for (final String fieldA : Arrays.asList("subject" + line + "_3", "subject" + line + "_3LINK", "subject" + line + "_3LINK2")) {
                                svfVrAttributen(fieldA, col, attributeIntPlus(fieldA, "Y", 20));  // 縦センタリング
                            }
                        }
                    }
                } else {
                    final int avgLineMax = 20; //各教科評定平均値出力行数
                    if (avg_line <= avgLineMax) {
                        final String field;
                        if (param().isKindaifuzoku() && !"1".equals(_printData._tyousasyo2020)) {
                            field = "subject" + avg_line;
                        } else {
                            field = "subject" + avg_line + (8 < defstr(avgRec.classname()).length() ? "_2" : "_1");
                        }
                        svfVrsOut(field, defstr(avgRec.classname()));  //教科名
                        svfVrsOut("average_" + avg_line, defstr(avgRec.avg()));  //評定
                    }
                }
            }

            if (!_printData.isRemarkOnly(param())) {
                //全体の評定平均値
                svfVrsOut("average", _printData.getTotalAvgGrades());
            }
        }

        /*
         * フィールド属性変更(RECORD) 文字数により文字ピッチ及びＹ軸を変更する
         */
        private static void setSvfFieldAttribute1(
                final Map<String, List<String>> attributeMap,
                final String data,
                int ln,
                final int lineMax1,
                final String fieldname,
                final int width,
                final KNJSvfFieldInfo fieldInfo
        ) {
            final int i = ((ln % lineMax1 > 0) ? 1 : 0) + ln / lineMax1;  // lineMax1行の何列かをカウント
            final boolean right = (i % 2 == 0) ? true : false;  // 左右の欄を指定
//            log.debug(" ln = " + ln + ", max1 = " + lineMax1 + ", right = " + right + ", i = " + i + " / " + name);
            ln = (ln % lineMax1 == 0) ? lineMax1 : ln % lineMax1;  // 出力する行位置を再設定
            final int x;
            if (right) {
                x = fieldInfo._x2;  //右列の開始Ｘ軸
            } else {
                x = fieldInfo._x1;  //左列の開始Ｘ軸
            }
            Util.getMappedList(attributeMap, fieldname).add("X=" + String.valueOf(x));
            final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldname, width, fieldInfo._height, fieldInfo._ystart, fieldInfo._minnum, fieldInfo._maxnum);
            final double charSize = modify.getCharSize(data);
            Util.getMappedList(attributeMap, fieldname).add("Size=" + charSize);
            Util.getMappedList(attributeMap, fieldname).add("Y="+ (int) modify.getYjiku(ln, charSize));
        }

//        private Map suraNendogotoMap(final Object item, final PrintData printData, final StudyrecDat studyrecDat, final Object totalCredit) {
//            final Map m = new HashMap();
//            m.put("ITEM_2", item);
//            if (null != studyrecDat) {
//                for (final Grades g : studyrecDat.getNotDropGradesList()) {
//                    final Title title = printData._titles.get(g._year);
//                    if (null != title) {
//                        m.put("GRADES" + String.valueOf(title._seisekiPos) + "_2", g._credit); // 評定のフィールドに修得単位を表示する
//                    }
//                }
//            }
//            m.put("CREDIT_2", totalCredit);
//            return m;
//        }

        private static void setSvfFormUseSyojikou3(final PrintData printData, final Param param, final FormInfoKNJE070_1 formInfo) {
            final String FORM1_KENJA_SYOJIKOU3 = "KNJE070_1B.frm";
            final String FORM5_KENJA_SYOJIKOU3 = "KNJE070_5B.frm";
            if (param._z010.in(Z010Info.KaichiTsushin)) {
                // if (FormNen._3 == formi) {
                // } else if (FormNen._6 == formInfo._formNenYou) {
                //} else if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_1BM_KAICHI.frm";
                    formInfo._formNen = FormNen._4;
                //}
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
            } else if (printData._isTsushin) {
                if (param._z010.in(Z010Info.Miyagiken)) {
                    // if (FormNen._3 == formi) {
                    if (FormNen._6 == formInfo._formNenYou) {
                        formInfo._formName = "KNJE070_2BMMIYA.frm";
                        formInfo._formNen = FormNen._6;
                    } else { // if (FormNen._4 == formi) {
                        formInfo._formName = "KNJE070_1BMMIYA.frm";
                        formInfo._formNen = FormNen._4;
                    }
                    formInfo._isShojikouMatome = true;
                } else if (param._z010.in(Z010Info.Hirokoudai)) {
                    // if (3 == formi) {
                    if (FormNen._6 == formInfo._formNenYou) {
                        formInfo._formName = "KNJE070_2BM_HIROKOUDAI.frm";
                        formInfo._formNen = FormNen._6;
                    } else { // if (4 == formi) {
                        formInfo._formName = "KNJE070_1BM_HIROKOUDAI.frm";
                        formInfo._formNen = FormNen._4;
                    }
                    formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                } else if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_5BM_22.frm" : "KNJE070_5BM.frm";
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_2BM_22.frm" : "KNJE070_2BM.frm";
                    formInfo._is6nen = true;
                    formInfo._formNen = FormNen._6;
                } else { // if (4 == formi) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_1BM_22.frm" : "KNJE070_1BM.frm";
                    formInfo._formNen = FormNen._4;
                }
            } else if (param._z010.in(Z010Info.Ryukei)) {
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_RYUKEI.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNenYou = FormNen._3;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Osakatoin)) {
//              if (FormNen._3 == formInfo._formNenYou) {
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
                formInfo._formName = "KNJE070_1B_21OSAKATOIN.frm";
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNenYou = FormNen._4;
            } else if (param._z010.in(Z010Info.NaganoSeisen)) {
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_NAGANOSEISEN.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Hibarigaoka)) {
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_HIBARIGAOKA.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.RitsumeikanKeisho)) { // 立命館慶祥
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5BKEISHO.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._isShingakuyouKenja = true;
            } else if (param._z010.in(Z010Info.Meikei)) { // 茗渓
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_22MEIKEI.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.TamagawaSei)) { // 玉川聖
//              if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_22TAMAGAWASEI.frm";
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
//              }
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.naraken)) { // 奈良県
//              if (FormNen._3 == formInfo._formNenYou) {
//              } else if (FormNen._6 == formInfo._formNenYou) {
//              } else { // if (FormNen._4 == formi) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = "KNJE070_1BNARA.frm";
                    } else {
                        formInfo._formName = "KNJE070_1B_02NARA.frm";
                    }
//                }
                    formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
            } else if (param._z010.in(Z010Info.Yamamura)) { // 山村学園
//                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5BYAMAMURA.frm";
                    formInfo._formNen = FormNen._3;
                    formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
//                } else if (FormNen._6 == formInfo._formNenYou) {
//                } else { // if (FormNen._4 == formi) {
//                }
            } else if (param._z010.in(Z010Info.risshisha)) { // 立志舎
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5BRISSHISHA.frm";
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_2BRISSHISHA.frm";
                    formInfo._formNen = FormNen._6;
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_1BRISSHISHA.frm";
                    formInfo._formNen = FormNen._4;
                }
            } else if (param._z010.isKaichi()) {
//        		if (FormNen._3 == formInfo._formNenYou) {
                if (param._z010.in(Z010Info.KaichiIkkan)) {
                    formInfo._formName = "KNJE070_5B_22KAICHI_IKKAN.frm";
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                } else {
                    formInfo._formName = "KNJE070_5B_22KAICHI.frm";
                }
                    formInfo._formNen = FormNen._3;
//        		} else if (FormNen._6 == formInfo._formNenYou) {
//        		} else { // if (FormNen._4 == formi) {
//        		}
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Hirokoudai)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5B_HIROKOUDAI.frm";
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_2B_HIROKOUDAI.frm";
                    formInfo._formNen = FormNen._6;
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_1B_HIROKOUDAI.frm";
                    formInfo._formNen = FormNen._4;
                }
                formInfo._shokenType5 = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._shokenType6 = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._shokenType7 = FormInfo.ShokenType.TYPE2;
                formInfo._shokenType8 = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._shokenType9 = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
            } else if (param._z010.in(Z010Info.Nagisa)) {
//        		if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5B_22NAGISA.frm";
                    formInfo._formNen = FormNen._3;
//        		} else if (FormNen._6 == formInfo._formNenYou) {
//        		} else { // if (FormNen._4 == formi) {
//        		}
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Komazawa)) {
//        		if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5B_22KOMAZAWA.frm";
                    formInfo._formNen = FormNen._3;
//        		} else if (FormNen._6 == formInfo._formNenYou) {
//        		} else { // if (FormNen._4 == formi) {
//        		}
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Reitaku)) {
//        		if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5B_22REITAKU.frm";
                    formInfo._formNen = FormNen._3;
//        		} else if (FormNen._6 == formInfo._formNenYou) {
//        		} else { // if (FormNen._4 == formi) {
//        		}
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Matsudo)) {
//        		if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_22MATSUDO.frm";
                formInfo._formNen = FormNen._3;
//        		} else if (FormNen._6 == formInfo._formNenYou) {
//        		} else { // if (FormNen._4 == formi) {
//        		}
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Hagoromo) && FormNen._4 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_1B_22HAGOROMO.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNen = FormNen._4;
            } else if (param._z010.in(Z010Info.Tokiwagi) && FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5B_22TOKIWAGI.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNen = FormNen._3;
            } else if (param._z010.in(Z010Info.Tosa) && FormNen._4 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_1B_22TOSA.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE2;
                formInfo._formNen = FormNen._4;
            } else if (param._z010.in(Z010Info.Sakae)) {
                //if (FormNen._3 == formi) {
                formInfo._is2page = true;
                formInfo._formNameLeft = "KNJE070_5BLSAKAE.frm";
                formInfo._formNameRight = "KNJE070_5BRSAKAE.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNen = FormNen._3;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;

//              } else if (FormNen._6 == formi) {
//              } else { // if (FormNen._4 == formi) {
//              }
            } else if (param._isHigashiosakaKeiai) {
                //if (FormNen._3 == formi) {
                formInfo._formName = "KNJE070_5BKEIAI.frm";
                formInfo._formNen = FormNen._3;
//              } else if (FormNen._6 == formi) {
//              } else { // if (FormNen._4 == formi) {
//              }
            } else if (param._isHigashiosakaKashiwara) {
                //if (FormNen._3 == formi) {
                formInfo._formName = "KNJE070_5B_21KASHIWARA.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNen = FormNen._3;
//              } else if (FormNen._6 == formi) {
//              } else { // if (FormNen._4 == formi) {
//              }
            } else if (param._z010.in(Z010Info.Rakunan)) {
                //if (FormNen._3 == formi) {
//              } else if (FormNen._6 == formi) {
//              } else { // if (FormNen._4 == formi) {
                formInfo._formName = "KNJE070_1BRAKUNAN.frm";
                formInfo._formNen = FormNen._4;
//              }
            } else if (param._z010.in(Z010Info.Seijyo)) {
                //if (FormNen._3 == formi) {
                formInfo._formName = "KNJE070_5B_22SEIJO.frm";
                formInfo._formNen = FormNen._3;
//              } else if (FormNen._6 == formi) {
//              } else { // if (FormNen._4 == formi) {
//              }

            } else if (param._z010.in(Z010Info.Sundaikoufu)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    if (printData.isSundaiKofuBijutsuDesignKa(param)) { // 中央の窓を表示しない
                        formInfo._formName = "KNJE070_5B_02SUNDAIKOUFU2.frm";
                    } else {
                        formInfo._formName = "KNJE070_5B_02SUNDAIKOUFU.frm";
                    }
                    formInfo._formNen = FormNen._3;
                //} else if (FormNen._6 == formi) {
                } else { // if (FormNen._4 == formi) {
                    if (printData.isSundaiKofuBijutsuDesignKa(param)) { // 中央の窓を表示しない
                        formInfo._formName = "KNJE070_1B_02SUNDAIKOUFU2.frm";
                    } else {
                        formInfo._formName = "KNJE070_1B_02SUNDAIKOUFU.frm";
                    }
                    formInfo._formNen = FormNen._4;
                }
                formInfo._isKyotoForm = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
            } else if (param._z010.in(Z010Info.Musashinohigashi)) {
                formInfo._formName = "KNJE070_5BMUSAHIGA.frm";
                formInfo._isShingakuyouKenja = true;
                formInfo._formNen = FormNen._3;
            } else if (printData._isMiekenForm) {
                if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_2B_02MIE.frm";
                    formInfo._formNen = FormNen._6;
                } else { // if (4 == formi) {
                    formInfo._formName = "KNJE070_1B_02MIE.frm";
                    formInfo._isKyotoForm = true;
                    formInfo._formNen = FormNen._4;
                    formInfo._isShingakuyouFormRecordFormat2 = true;
                }
                formInfo._shokenType = FormInfo.ShokenType.TYPE2;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;

                //}
            } else if (param._z010.in(Z010Info.Miyagiken)) { // 宮城県
                if (FormNen._3 == formInfo._formNenYou) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = "KNJE070_5BMIYA.frm";
                    } else {
                        formInfo._formName = "KNJE070_5B_02MIYA.frm";
                    }
                } else if (FormNen._6 == formInfo._formNenYou) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = "KNJE070_2BMIYA.frm";
                    } else {
                        formInfo._formName = "KNJE070_2B_02MIYA.frm";
                    }
                } else { // if (FormNen._4 == formi) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = "KNJE070_1BMIYA.frm";
                    } else {
                        formInfo._formName = "KNJE070_1B_02MIYA.frm";
                    }
                }
            } else if (param._z010.in(Z010Info.Fukuiken)) { // 福井県
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5B_02FUKUI.frm";
                    formInfo._isKyotoForm = true;
                    formInfo._isShingakuyouFormRecordFormat2 = true;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_2B_02FUKUI.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_1B_02FUKUI.frm";
                    formInfo._isKyotoForm = true;
                    formInfo._isShingakuyouFormRecordFormat2 = true;
                }
                formInfo._hankiNinteiTaiou = true;
                formInfo._shokenType5 = null;
                formInfo._shokenType6 = FormInfo.ShokenType.TYPE2;
                formInfo._shokenType7 = FormInfo.ShokenType.TYPE2;
                formInfo._shokenType8 = FormInfo.ShokenType.TYPE2;
                formInfo._shokenType9 = FormInfo.ShokenType.TYPE2;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
            } else if (param._z010.in(Z010Info.Tosajoshi)) { // 土佐女子
                //if (FormNen._3 == formInfo._formNenYou) {
                formInfo._formName = "KNJE070_5BTOSAJOSHI.frm";
                formInfo._shokenType = FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT;
                formInfo._formNen = FormNen._3;
                //} else if (FormNen._6 == formInfo._formNenYou) {
                //} else if (FormNen._4 == formi) {
                //}
            } else if (param._z010.in(Z010Info.Tokiwa)) { // 常磐
                //if (3 == formi) {
                formInfo._formName = "KNJE070_5BTOKIWA.frm";
                formInfo._formNen = FormNen._3;
                //} else if (FormNen._6 == formi) {
                //} else { // if (FormNen._4 == formi) {
                //}
            } else if (param._z010.in(Z010Info.Kaijyo)) { // 海城
                //if (FormNen._3 == formi) {
                //    formInfo._formi = FormNen._4;
                //    setSvfFormUseCurriculumcd(printData, param, formInfo);
                //} else if (FormNen._6 == formi) {
                //} else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_1B_32KAIJO.frm";
                    formInfo._formNen = FormNen._4;
                //}
            } else if (param._z010.in(Z010Info.Nishiyama)) { // 京都西山
                //if (FormNen._3 == formi) {
                //    formInfo._formi = FormNen._4;
                //    setSvfFormUseCurriculumcd(printData, param, formInfo);
                //} else if (FormNen._6 == formi) {
                //    formInfo._formi = FormNen._4;
                //    setSvfFormUseCurriculumcd(printData, param, formInfo);
                //} else { // if (FormNen._4 == formi) {
                    formInfo._is2page = true;
                    formInfo._formNameLeft = "KNJE070_1BLTORI.frm";
                    formInfo._formNameRight = "KNJE070_1BR_22NISHIYAMA.frm";
                    formInfo._formNen = FormNen._4;
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                    formInfo._isShingakuyouFormRecordFormat2 = true;
                //}
            } else if (param._z010.in(Z010Info.Meiji)) { // 明治
                //if (FormNen._3 == formi) {
                //    formInfo._formi = FormNen._4;
                //    setSvfFormUseCurriculumcd(printData, param, formInfo);
                //} else if (FormNen._6 == formi) {
                //    formInfo._formi = FormNen._4;
                //    setSvfFormUseCurriculumcd(printData, param, formInfo);
                //} else { // if (FormNen._4 == formi) {
                    formInfo._is2page = true;
                    formInfo._formNameLeft = "KNJE070_1BLTORI.frm";
                    if (printData.isMeijiSogo(param)) {
                        formInfo._formNameRight = "KNJE070_1BRTORI.frm";
                    } else {
                        formInfo._formNameRight = "KNJE070_1BR_2TORI.frm";
                    }
                    formInfo._formNen = FormNen._4;
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                    formInfo._isShingakuyouFormRecordFormat2 = true;

                //}
            } else if (printData._isKyoto) { // 京都
                if (FormNen._3 == formInfo._formNenYou) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_5B_21KYO.frm" : "KNJE070_5BKYO.frm";
                    } else {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_5B_22KYO.frm" : "KNJE070_5B_02KYO.frm";
                    }
                } else if (FormNen._6 == formInfo._formNenYou) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_2B_21KYO.frm" : "KNJE070_2BKYO.frm";
                    } else {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_2B_22KYO.frm" : "KNJE070_2B_02KYO.frm";
                    }
                    formInfo._is6nen = true;
                } else { // if (FormNen._4 == formi) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani))) {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_1B_21KYO.frm" : "KNJE070_1BKYO.frm";
                    } else {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_1B_22KYO.frm" : "KNJE070_1B_02KYO.frm";
                    }
                }
                formInfo._isKyotoForm = true;
                formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Tottori, Z010Info.Kyoai)) { // 鳥取、共愛
                if (FormNen._3 == formInfo._formNenYou) {
                    if (formInfo.trainRef123FieldSize1(printData)) {
                        formInfo._formName = "KNJE070_5B_22TORI.frm";
                    } else {
                        formInfo._formName = "KNJE070_5BTORI.frm";
                    }
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                //} else if (6 == formInfo._formNenYou) {
                } else { // if (4 == formi) {
                    if (formInfo.trainRef123FieldSize1(printData)) {
                        formInfo._formName = "KNJE070_1B_22TORI.frm";
                    } else {
                        formInfo._formName = "KNJE070_1BTORI.frm";
                    }
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                }
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Kumamoto)) { // 熊本
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_5B_22KUMA.frm" : "KNJE070_5BKUMA.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_2B_22KYO.frm" : "KNJE070_2B_02KYO.frm"; // 2011/08/10 熊本専用がないため京都を使用しておく
                    formInfo._isKyotoForm = true;
                    formInfo._is6nen = true;
                    formInfo._isShingakuyouFormShukketsuTitleFormat2 = true;
                    formInfo._isShingakuyouFormRecordFormat2 = true;
                } else { // if (4 == formi) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_1B_22KUMA.frm" : "KNJE070_1BKUMA.frm";
                }
//            } else if (param._isMusashi) {
//                //if (3 == formi) {
//                //    form = getSvfFormUseCurriculumcd(printData, param, 4);
//                //} else if (6 == formi) {
//                if (6 == formi) {
//                    form = "KNJE070_2BMUSA.frm";
//                    is6nen = true;
//                } else { // if (4 == formi) {
//                    form = "KNJE070_1BMUSA.frm";
//                }
                formInfo._isKumamotoForm = true;
                formInfo._isShingakuyouFormRecordFormat2 = true;
            } else if (param._z010.in(Z010Info.Chukyo)) { // 中京
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5BCHU.frm";
                //} else if (6 == formInfo._formNenYou) {
                } else { // if (4 == formi) {
                    formInfo._formName = "KNJE070_1BCHU.frm";
                }
            } else if (param.isKindaifuzoku()) { // 近大附属高校
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5BKIN.frm";
                //} else if (6 == formInfo._formNenYou) {
                } else { // if (4 == formi) {
                    formInfo._formName = "KNJE070_1BKIN.frm";
                }
            } else if (param._z010.in(Z010Info.Sapporo)) { // 札幌開成
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_5BSAP.frm";
                } else {
                    formInfo._formName = "KNJE070_1BSAP.frm";
                    formInfo._formNen = FormNen._4;
                }
                formInfo._isShingakuyouKenja = true;
            } else if (param._z010.in(Z010Info.Hirokoku)) { // 広島国際
                final boolean isSura = printData.isHirokokuSogakuSuraForm(param);
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = isSura ? "KNJE070_5BHIROKOKUS.frm" : "KNJE070_5BHIROKOKU.frm";
                //} else if (FormNen._6 == formInfo._formNenYou) {
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = isSura ? "KNJE070_1BHIROKOKUS.frm" : "KNJE070_1BHIROKOKU.frm";
                }
            } else { // 賢者
                if (FormNen._3 == formInfo._formNenYou) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani)) && formInfo.trainRef123FieldSize1(printData)) {
                        formInfo._formName = "KNJE070_5B_21.frm";
                    } else {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_5B_22.frm" : FORM5_KENJA_SYOJIKOU3;
                    }
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_2B_22.frm" : "KNJE070_2B.frm";
                    formInfo._is6nen = true;
                    formInfo._is6nenyouKenja = true;
                } else { // if (4 == formi) {
                    if ("1".equals(printData.property(Property.tyousasyoSougouHyoukaNentani)) && formInfo.trainRef123FieldSize1(printData)) {
                        formInfo._formName = "KNJE070_1B_21.frm";
                    } else {
                        formInfo._formName = formInfo.trainRef123FieldSize1(printData) ? "KNJE070_1B_22.frm" : FORM1_KENJA_SYOJIKOU3;
                    }
                }
                formInfo._isShingakuyouKenja = true;
            }
            if (formInfo._formNen == FormNen.NONE) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._6;
                } else { // if (4 == formi) {
                    formInfo._formNen = FormNen._4;
                }
            }
        }

        private static void setSvfFormNotUseSyojikou3(final PrintData printData, final Param param, final FormInfoKNJE070_1 formInfo) {
            if (FormNen._3 == formInfo._formNenYou) {
                if (param._z010.in(Z010Info.Tokiwa)) {
                    formInfo._formName = "KNJE070_5TOKIWA.frm";
                } else if (printData._isKyoto && !printData._isTsushin) {
                    formInfo._formName = null;
                } else if (param._z010.in(Z010Info.Tottori, Z010Info.Kyoai)) {
                    formInfo._formName = "KNJE070_5TORI.frm";
                } else if (param._z010.in(Z010Info.Kumamoto) && !printData._isTsushin) {
                    formInfo._formName = "KNJE070_5KUMA.frm";
                    formInfo._isKumamotoForm = true;
//                } else if (param._isMusashi) {
//                    formInfo._formi = 4;
//                    setSvfFormNotUseCurriculumcd(printData, param, formInfo);
                    formInfo._isKumamotoForm = true;
                } else if (param.isKindaifuzoku()) {
                    formInfo._formNenYou = FormNen._4;
                    setSvfFormNotUseSyojikou3(printData, param, formInfo);
                } else if (param._z010.in(Z010Info.Chukyo)) {
                    formInfo._formNenYou = FormNen._4;
                    setSvfFormNotUseSyojikou3(printData, param, formInfo);
                } else {
                    formInfo._formNenYou = FormNen._4;
                    setSvfFormNotUseSyojikou3(printData, param, formInfo);
                }
            } else if (FormNen._6 == formInfo._formNenYou) {
                if (printData._isTsushin) {
                    formInfo._formName = "KNJE070_2M.frm";
                    formInfo._is6nen = true;
                } else if (param._z010.in(Z010Info.Meiji)) {
                    setSvfFormNotUseSyojikou3(printData, param, formInfo);
//                } else if (param._isMusashi) {
//                    form = "KNJE070_2MUSA.frm";
//                    formInfo._is6nen = true;
                } else if (param.isKindaifuzoku()) {
                    formInfo._formName = "KNJE070_2KIN.frm";
                    formInfo._is6nen = true;
                } else if (param._z010.in(Z010Info.Chukyo)) {
                    formInfo._formName = "KNJE070_2CHU.frm";
                    formInfo._is6nen = true;
                } else {
                    formInfo._formName = "KNJE070_2.frm";
                    formInfo._is6nen = true;
                    formInfo._isShingakuyouKenja = true;
                }
            } else { // if (4 == formi) {
                if (param._z010.in(Z010Info.Tokiwa)) {
                    setSvfFormNotUseSyojikou3(printData, param, formInfo);
                } else if (printData._isTsushin) {
                    formInfo._formName = "KNJE070_1M.frm";
                } else if (printData._isKyoto) {
                    formInfo._formName = "KNJE070_1KYO.frm";
                    formInfo._isKyotoForm = true;
                } else if (param._z010.in(Z010Info.Tottori, Z010Info.Kyoai)) {
                    formInfo._formName = "KNJE070_1TORI.frm";
                } else if (param._z010.in(Z010Info.Kumamoto)) {
                    formInfo._formName = "KNJE070_1KUMA.frm";
                    formInfo._isKumamotoForm = true;
//                } else if (param._isMusashi) {
//                    formInfo._formName = "KNJE070_1MUSA.frm";
                } else if (param.isKindaifuzoku()) {
                    formInfo._formName = "KNJE070_1KIN.frm";
                } else if (param._z010.in(Z010Info.Chukyo)) {
                    formInfo._formName = "KNJE070_1CHU.frm";
                } else {
                    formInfo._formName = "KNJE070_1.frm";
                    formInfo._isShingakuyouKenja = true;
                }
            }
            formInfo._isShingakuyouFormShukketsuTitleFormat2 = param._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji) || formInfo._isKyotoForm;
            if (formInfo._formNen == FormNen.NONE) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._6;
                } else { // if (FormNen._4 == formi) {
                    formInfo._formNen = FormNen._4;
                }
            }
            formInfo._isShingakuyouFormRecordFormat2 = formInfo._isKumamotoForm || param._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama, Z010Info.Sakae) || formInfo._isKyotoForm;
        }
    }

    // 進学用A4 4ページ
    private static class FormKNJE070_1_A4 extends FormKNJE070_1 {

        private static final int SUBFORM_START_Y = 314;
        private static final int SUBFORM_END_Y = 4538;

        private static int FORM_RECORD_TOTALSTUDY_HEIGHT = 66;
        private static int FORM_RECORD_SPECIALACTREMARK_HEIGHT = 50;
        private static int FORM_RECORD_SHOJIKOU_HEIGHT = 48;
        private static int FORM_RECORD_BIKO_HEIGHT = 57;
        private static int FORM_RECORD_ATTEND_HEIGHT3NEN4NEN = 641;
        private static int FORM_RECORD_ATTEND_HEIGHT6NEN8NEN = 1138;
        private static int FORM_RECORD_CERTIF_SCHOOL_HEIGHT = 558;
        private static int FORM_RECORD_CERTIF_SCHOOL_SPC_HEIGHT = 140;

        private static final String FORM_KNJE070_A4_3NEN_2 = "KNJE070_A4_3NEN_2.frm";
        private static final String FORM_KNJE070_A4_3NEN_3 = "KNJE070_A4_3NEN_3.frm";

        private static final Seq seq1 = Seq.of("1");
        private static final Seq seq2 = Seq.of("2");
        private static final Seq seq3 = Seq.of("3");
        private static final Seq seq4 = Seq.of("4");
        private static final Seq seq5 = Seq.of("5");
        private static final Seq seq6 = Seq.of("6");

        private static final String _8nen = "8nen";
        private static final String _6nen = "6nen";

        private static enum FORM_KIND {
            _1, // 共通1ページ目
            _2, // 4ページ用2ページ目
            _3_SHOJIKOU,// 4ページ用3ページ目
            _4_BIKOU_SHUKKETSU, // 4ページ用4ページ目
            _4_BIKOU_ONLY, // 4ページ用4ページ目備考のみ
            _2_3_2RECORD, // 総合的な学習の時間、特別活動の記録、指導上参考となる諸事項はレコードのフォーム(三重県)
            _2_3_4_2RECORD, // 総合的な学習の時間、特別活動の記録、指導上参考となる諸事項、備考、出欠備考はレコードのフォーム (佐賀県)
            _4_BIKOU_SHUKKETSU_RECORD, // 4ページ用4ページ目
            _2PAGE_2 // 2ページ用2ページ目 (宮城県)
            ;

            public boolean in(FORM_KIND ...kinds) {
                return null != kinds && ArrayUtils.contains(kinds, this);
            }

            public static FORM_KIND get(final String a4p) {
                if (NumberUtils.isDigits(a4p)) {
                    final int num = Integer.parseInt(a4p);
                    switch (num) {
                    case 1 : return _1;
                    case 2 : return _2;
                    case 3 : return _3_SHOJIKOU;
                    case 4 : return _4_BIKOU_SHUKKETSU;
                    case 5 : return _4_BIKOU_ONLY;
                    case 23 : return _2_3_2RECORD;
                    case 234 : return _2_3_4_2RECORD;
                    case 202 : return _2PAGE_2;
                    case 204 : return _4_BIKOU_SHUKKETSU_RECORD;
                    }
                }
                return null;
            }
        }

        private static class FormA4ConfigParam {
            FORM_KIND _formkind;
            Integer _a4Page;
            Integer _a4ShojikouStartY;
            Tuple<Integer, Integer> _totalstudyRecordY1;
            Tuple<Integer, Integer> _totalstudyActRecordY1;
            Tuple<Integer, Integer> _totalstudyValRecordY1;
            Tuple<Integer, Integer> _specialActRecordStartY1;
            Tuple<Integer, Integer>_bikoTitleXY;
            boolean _is6nen8nen;
            Integer _attendFormStartY;
            public FormA4ConfigParam setFormKind(final FORM_KIND formkind) {
                _formkind = formkind;
                return this;
            }
            public FormA4ConfigParam setA4Page(final Integer a4Page) {
                _a4Page = a4Page;
                return this;
            }
            public FormA4ConfigParam setA4ShojikouStartY(final Integer a4ShojikouStartY) {
                _a4ShojikouStartY = a4ShojikouStartY;
                return this;
            }
            public FormA4ConfigParam setTotalstudyRecordY(final Tuple<Integer, Integer> totalstudyRecordY1) {
                _totalstudyRecordY1 = totalstudyRecordY1;
                return this;
            }
            public FormA4ConfigParam setTotalstudyActRecordY(final Tuple<Integer, Integer> totalstudyActRecordY1) {
                _totalstudyActRecordY1 = totalstudyActRecordY1;
                return this;
            }
            public FormA4ConfigParam setTotalstudyValRecordY(final Tuple<Integer, Integer> totalstudyValRecordY1) {
                _totalstudyValRecordY1 = totalstudyValRecordY1;
                return this;
            }
            public FormA4ConfigParam setSpecialActRecordY(final Tuple<Integer, Integer> specialActRecordStartY1) {
                _specialActRecordStartY1 = specialActRecordStartY1;
                return this;
            }
            public FormA4ConfigParam setBikoTitleXY(final Tuple<Integer, Integer> bikoTitleXY) {
                _bikoTitleXY = bikoTitleXY;
                return this;
            }
            public FormA4ConfigParam set6nen8en(final boolean is6nen8nen) {
                _is6nen8nen = is6nen8nen;
                return this;
            }
            public FormA4ConfigParam setAttendFormStartY(final int attendFormStartY) {
                _attendFormStartY = attendFormStartY;
                return this;
            }
            private String show(final String name, final Object val) {
                return null == val ? "" : ", " + name + " = " + val;
            }
            public String toString() {
                return "FormA4ConfigParam(" + _formkind
                        + show("a4Page", _a4Page)
                        + show("a4ShojikouStartY", _a4ShojikouStartY)
                        + show("totalstudyRecordY1", _totalstudyRecordY1)
                        + show("totalstudyActRecordY1", _totalstudyActRecordY1)
                        + show("totalstudyValRecordY1", _totalstudyValRecordY1)
                        + show("specialActRecordStartY1", _specialActRecordStartY1)
                        + show("bikoTitleXY", _bikoTitleXY)
                        + show("is6nen8nen", _is6nen8nen)
                        + ")";
            }
        }

        private static enum Extends {
            _6nen("6nen", "6年")
          , _8nen("8nen", "8年")
          , _1("1", "レコードタイプ")
          , _2("2", "レコードタイプ 3ページ 三重県")
          , _2_2("2_2", "レコードタイプ 3ページ 諸事項の出力超過分は備考欄に出力する 熊本県")
          , _3("3", "レコードタイプ 備考出力をデータ行数分とし出欠欄・学校名を寄せる 佐賀県通信制")
          , _4("4", "レコードタイプ 備考・出欠欄・学校名をレコード 奈良県")
          , _2page("2page!", "レコードタイプ 2ページ 諸事項の出力超過分は出力しない 宮城県")
          , _2pageBiko("2page", "レコードタイプ 2ページ 諸事項の出力超過分は備考欄に出力する 熊本県")
          , None(null, "")
            ;

            final String _val;
            final String _comment;
            Extends(final String val, final String comment) {
                _val = val;
                _comment = comment;
            }

            boolean in(final Extends ... es) {
                if (null != es) {
                    for (Extends e : es) {
                        if (null == _val &&  null == e._val || null != _val && _val.equals(e._val)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            static Extends from(final String val) {
                if (null != val) {
                    for (Extends e : Extends.values()) {
                        if (null != e._val && e._val.equals(val)) {
                            return e;
                        }
                    }
                }
                return None;
            }

            public String toString() {
                return "Extends(" + _val + ", " + _comment + ")";
            }
        }

        boolean hasdata;
        private Map<Seq, String> _shojikouTitleMap;
        private int _shojikouHeaderLines = 2;

        private static final String BIKOU_RANNNI_KISAI = "【続きを備考欄に記載】";

        private boolean _isFormatOnly = false;
        private final FormInfoKNJE070_1_A4 _formInfo;

        public FormKNJE070_1_A4(final Param param, final FormInfoKNJE070_1_A4 formInfo) {
            super(param, formInfo);
            _formInfo = formInfo;
        }

        private static class FormInfoKNJE070_1_A4 extends FormInfoKNJE070_1 {
            final PrintData _printData;
            String _formNameA4_1;
            String _formNameA4_2;
            String _formNameA4_2RECORD;
            String _formNameA4_3;
//            String _formNameA4_3RECORD;
            String _formNameA4_4;
            String _formNameA4_BIKO;
            String _formNameA4_2PAGE_2;
            ShokenSize _A4_totalstudyActShokenSize;
            ShokenSize _A4_totalstudyValShokenSize;
            ShokenSize _A4_spActSize;
            ShokenSize _A4_shojikouShokenSize;
            boolean _A4_printDouble;
            int _A4_bikoMaxLinePerPage;
            int _A4_bikoOnlyPageMaxLine = 70;
            int _useFormNnen = -1;
            int _useFormNnen4 = -1;
            final Extends _tyousasyo2020shojikouExtends;

            private static final String NNEN_FORM = "KNJE070_A4_2RECORD.frm";
            private static final String NNEN_FORM4 = "KNJE070_A4_2RECORD4.frm";

            FormInfoKNJE070_1_A4(final PrintData printData, final Param param) {
                super(printData, param);
                _printData = printData;

                _useGradeMultiPage = false;

                if (param._z010.in(Z010Info.Mieken)) {
                    _tyousasyo2020shojikouExtends = Extends._2;
                    if (param._isOutputDebug) {
                        log.info(" _tyousasyo2020shojikouExtends = " + _tyousasyo2020shojikouExtends);
                    }
                } else {
                    final String shojikouExtendsVal = printData.property(Property.tyousasyo2020shojikouExtends);
                    Extends e = Extends.from(shojikouExtendsVal);
                    if (e == Extends.None && Arrays.asList(PRGID_KNJE070E, PRGID_KNJE010E, PRGID_KNJE011E).contains(_param._prgid)) {
                        e = Extends._2page;
                    }
                    _tyousasyo2020shojikouExtends = e;
                    if (param._isOutputDebug) {
                        log.info(" _tyousasyo2020shojikouExtends = " + _tyousasyo2020shojikouExtends + " (" + shojikouExtendsVal + ")");
                    }
                }
            }

            public void setSvfFormUseSyojikou6(final Vrw32alp svf) {
                final PrintData printData = _printData;
                final boolean isSetForm6 = !StringUtils.isBlank(printData.getParameter(Parameter.FORM6));
                final boolean isPageOverOrCheckForm6 = Title.getValidated(printData._gradeTitlesForForm).size() > _formNenYou._intval || isSetForm6;
                final FormNen _formNenYouSource = _formNenYou;
                final boolean useNewPageIfOver6Or8 = _param._z010.in(Z010Info.Mieken);
                if (!_tyousasyo2020shojikouExtends.in(Extends._2page)) {
                    if (_tyousasyo2020shojikouExtends.in(Extends._6nen) || _6nen.equals(printData.property(Property.tyousasyo2020FormNen))) {
                        _formNenYou = FormNen._6;
                    } else if (_tyousasyo2020shojikouExtends.in(Extends._8nen) || _8nen.equals(printData.property(Property.tyousasyo2020FormNen))) {
                        _formNenYou = FormNen._8;
                    } else if (_formNenYouSource == FormNen._3 && isPageOverOrCheckForm6) {
                        _formNenYou = FormNen._6;
                    } else if (_formNenYouSource == FormNen._4 && isPageOverOrCheckForm6) {
                        _formNenYou = FormNen._8;
                    }
                    if (_formNenYou.in(FormNen._6, FormNen._8)) {
                        _useGradeMultiPage = useNewPageIfOver6Or8 && Title.getValidated(printData._gradeTitlesForForm).size() > _formNenYou._intval;
                        log.info(" _useGradeMultiPage = " + _useGradeMultiPage);
                    }
                }

                final boolean preferUseFormNnen = (Arrays.asList(Extends._3, Extends._2pageBiko).contains(_tyousasyo2020shojikouExtends)) || _useGradeMultiPage;
                if (preferUseFormNnen && -1 == _useFormNnen) {
                    _useFormNnen = FormInfo.hasForm(_param, svf, FormInfoKNJE070_1_A4.NNEN_FORM) ? 1 : 0;
                    if (_param._isOutputDebug) {
                        log.info(" _useFormNnen = " + _useFormNnen);
                    }
                }
                final boolean preferUseFormNnen4 = (Arrays.asList(Extends._4).contains(_tyousasyo2020shojikouExtends));
                if (preferUseFormNnen4 && -1 == _useFormNnen4) {
                    _useFormNnen4 = FormInfo.hasForm(_param, svf, FormInfoKNJE070_1_A4.NNEN_FORM) ? 1 : 0;
                    if (_useFormNnen4 == 1) {
                        copyForm(_param, svf, FormInfoKNJE070_1_A4.NNEN_FORM, FormInfoKNJE070_1_A4.NNEN_FORM4);
                    }
                    if (_param._isOutputDebug) {
                        log.info(" _useFormNnen4 = " + _useFormNnen4);
                    }
                }

                {
                    // hoge2(final PrintData printData, final Param param, final boolean useFormNnenForm, final boolean useFormNnenForm4)
                    final boolean useFormNnenForm = _useFormNnen == 1;
                    final boolean useFormNnenForm4 = _useFormNnen4 == 1;

                    _A4_bikoMaxLinePerPage = 53;

                    final int totalstudyMoji = 48;
                    final int spActMoji3nen = 16;
                    final int spActMoji4nen = 10;
                    final int shojikouMoji = _param._z010.in(Z010Info.jyoto) ? 16 : 15;

                    if (_tyousasyo2020shojikouExtends.in(Extends._2page, Extends._2pageBiko)) {
                        final int spActMoji;
                        final int spActGyo;
                        if (Arrays.asList(Extends._2pageBiko).contains(_tyousasyo2020shojikouExtends)) {
                            // 入力は6行。項目名のために3行分スペースを追加
                            spActGyo = 9;
                        } else {
                            spActGyo = 6;
                        }
                        final int shojikouGyo; // フォームは+2行
                        if (FormNen._3 == _formNenYou) {
                            _formNen = FormNen._3;
                            _formNameA4_1 = "KNJE070_A4_3NEN_1.frm";
                            _formNameA4_2PAGE_2 = "KNJE070_A4_3NEN_2P_2.frm";
                            spActMoji = spActMoji3nen;
                            shojikouGyo = 3;
                        } else { // if (FormNen._4 == formi) {
                            _formNen = FormNen._4;
                            _formNameA4_1 = "KNJE070_A4_4NEN_1.frm";
                            _formNameA4_2PAGE_2 = "KNJE070_A4_4NEN_2P_2.frm";
                            spActMoji = spActMoji4nen;
                            shojikouGyo = 2;
                        }
                        _formNameA4_2RECORD = useFormNnenForm ? NNEN_FORM : null;
                        _formNameA4_4 = useFormNnenForm ? NNEN_FORM4 : null;
                        _A4_totalstudyActShokenSize = ShokenSize.get(null, totalstudyMoji, 4);
                        _A4_totalstudyValShokenSize = ShokenSize.get(null, totalstudyMoji, 4);
                        _A4_spActSize = ShokenSize.get(null, spActMoji, spActGyo);
                        _A4_shojikouShokenSize = ShokenSize.get(null, shojikouMoji, shojikouGyo);
                        _A4_bikoMaxLinePerPage = 4;
                        _formNameA4_BIKO = "KNJE070_A4_BIKO.frm";

                    } else if (_formNenYou.in(FormNen._6, FormNen._8)) {
                        final int totalstudyDefaultGyo;
                        final int spActMoji;
                        int spActGyo;
                        int shojikouGyo; // フォームは+2行
                        if (FormNen._6 == _formNenYou) {
                            _formNen = FormNen._6;
                            _formNameA4_1 = "KNJE070_A4_6NEN_1.frm";
                            _formNameA4_2RECORD = useFormNnenForm ? NNEN_FORM : "KNJE070_A4_3NEN_2RECORD.frm";
                            _formNameA4_4 = useFormNnenForm4 ? NNEN_FORM4 : "KNJE070_A4_6NEN_4.frm";
                            totalstudyDefaultGyo = 6;
                            spActMoji = spActMoji3nen;
                            spActGyo = 9;
                            shojikouGyo = 9;
                        } else { // if (FormNen._8 == _formNenYou) {
                            _formNen = FormNen._8;
                            _formNameA4_1 = "KNJE070_A4_8NEN_1.frm";
                            _formNameA4_2RECORD = useFormNnenForm ? NNEN_FORM : "KNJE070_A4_4NEN_2RECORD.frm";
                            _formNameA4_4 = useFormNnenForm4 ? NNEN_FORM4 : "KNJE070_A4_8NEN_4.frm";
                            totalstudyDefaultGyo = 4;
                            spActMoji = spActMoji4nen;
                            spActGyo = 11;
                            shojikouGyo = 6;
                        }
                        int actGyo = totalstudyDefaultGyo;
                        int valGyo = totalstudyDefaultGyo;
                        if (_tyousasyo2020shojikouExtends.in(Extends._1, Extends._2, Extends._2_2)) {
                            actGyo = Util.toInt(printData.property(Property.tyousasyo2020TotalstudyactGyou), totalstudyDefaultGyo);
                            valGyo = Util.toInt(printData.property(Property.tyousasyo2020TotalstudyvalGyou), totalstudyDefaultGyo);
                            spActGyo = Util.toInt(printData.property(Property.tyousasyo2020specialactrecGyou), spActGyo);
                            shojikouGyo = Util.toInt(printData.property(Property.tyousasyo2020shojikouGyou), shojikouGyo);
                        }
                        _A4_totalstudyActShokenSize = ShokenSize.get(null, totalstudyMoji, actGyo);
                        _A4_totalstudyValShokenSize = ShokenSize.get(null, totalstudyMoji, valGyo);
                        _A4_spActSize = ShokenSize.get(null, spActMoji, spActGyo);
                        _A4_shojikouShokenSize = ShokenSize.get(null, shojikouMoji, shojikouGyo);
                        _formNameA4_BIKO = "KNJE070_A4_BIKO.frm";
                        _A4_printDouble = true;
                        _A4_bikoMaxLinePerPage = 45;
                        if (useFormNnenForm4) {
                            _A4_bikoMaxLinePerPage -= 4;
                        }

                    } else {
                        int spActMoji;
                        int spActGyo;
                        int shojikouGyo; // フォームは+2行
                        if (FormNen._3 == _formNenYou) {
                            _formNen = FormNen._3;
                            _formNameA4_1 = "KNJE070_A4_3NEN_1.frm";
                            _formNameA4_2 = FORM_KNJE070_A4_3NEN_2;
                            _formNameA4_2RECORD = useFormNnenForm ? NNEN_FORM : "KNJE070_A4_3NEN_2RECORD.frm";
                            _formNameA4_3 = FORM_KNJE070_A4_3NEN_3;
//                            _formNameA4_3RECORD = _formNameA4_2RECORD;
                            _formNameA4_4 = useFormNnenForm4 ? NNEN_FORM4 : "KNJE070_A4_3NEN_4.frm";
                            _formNameA4_BIKO = "KNJE070_A4_BIKO.frm"; // 4年用と同じ
                            spActMoji = spActMoji3nen;
                            spActGyo = 18;
                            shojikouGyo = _param._z010.in(Z010Info.Kumamoto) ? 17 : 20;
                        } else { // if (FormNen._4 == formi) {
                            _formNen = FormNen._4;
                            _formNameA4_1 = "KNJE070_A4_4NEN_1.frm";
                            _formNameA4_2 = "KNJE070_A4_4NEN_2.frm";
                            _formNameA4_2RECORD = useFormNnenForm ? NNEN_FORM : "KNJE070_A4_4NEN_2RECORD.frm";
                            _formNameA4_3 = "KNJE070_A4_4NEN_3.frm";
//                            _formNameA4_3RECORD = _formNameA4_2RECORD;
                            _formNameA4_4 = useFormNnenForm4 ? NNEN_FORM4 : "KNJE070_A4_4NEN_4.frm";
                            _formNameA4_BIKO = "KNJE070_A4_BIKO.frm";
                            spActMoji = spActMoji4nen;
                            spActGyo = 27;
                            shojikouGyo = 12;
                        }
                        _A4_totalstudyActShokenSize = ShokenSize.get(null, totalstudyMoji, 8);
                        _A4_totalstudyValShokenSize = ShokenSize.get(null, totalstudyMoji, 8);
                        _A4_spActSize = ShokenSize.get(null, spActMoji, spActGyo);
                        _A4_shojikouShokenSize = ShokenSize.get(null, shojikouMoji, shojikouGyo);
                        if (_tyousasyo2020shojikouExtends.in(Extends._2, Extends._2_2)) {
                            final int actGyou = Util.toInt(printData.property("tyousasyo2020TotalstudyactGyou"), _A4_totalstudyActShokenSize._gyo / 2);
                            final int valGyou = Util.toInt(printData.property("tyousasyo2020TotalstudyvalGyou"), _A4_totalstudyValShokenSize._gyo / 2);

                            _A4_totalstudyActShokenSize = ShokenSize.get(null, _A4_totalstudyActShokenSize._moji, actGyou);
                            _A4_totalstudyValShokenSize = ShokenSize.get(null, _A4_totalstudyValShokenSize._moji, valGyou);
                            if (FormNen._3 == _formNenYou) {
                                if (_param._z010.in(Z010Info.Nagisa)) {
                                    //ｚｔ；ｑ
                                    spActGyo = 12;
                                    shojikouGyo = 12;
                                } else {
                                    spActGyo = _A4_spActSize._gyo / 2;
                                    shojikouGyo = 7;
                                }
                            } else {
                                spActGyo = _A4_spActSize._gyo / 2 - 2;
                                shojikouGyo = 6;
                            }
                            _A4_spActSize = ShokenSize.get(null, spActMoji, spActGyo);
                            _A4_shojikouShokenSize = ShokenSize.get(null, shojikouMoji, shojikouGyo);
                        }
                        if (_tyousasyo2020shojikouExtends.in(Extends._3)) {
                            final int actGyo = Util.toInt(printData.property("tyousasyo2020TotalstudyactGyou"), _A4_totalstudyActShokenSize._gyo);
                            final int valGyo = Util.toInt(printData.property("tyousasyo2020TotalstudyvalGyou"), _A4_totalstudyValShokenSize._gyo);
                            _A4_totalstudyActShokenSize = ShokenSize.get(null, _A4_totalstudyActShokenSize._moji, actGyo);
                            _A4_totalstudyValShokenSize = ShokenSize.get(null, _A4_totalstudyValShokenSize._moji, valGyo);

                            spActGyo = Util.toInt(printData.property("tyousasyo2020specialactrecGyou"), spActGyo);
                            shojikouGyo = Util.toInt(printData.property("tyousasyo2020shojikouGyou"), shojikouGyo);

                            _A4_spActSize = ShokenSize.get(null, spActMoji, spActGyo);
                            _A4_shojikouShokenSize = ShokenSize.get(null, shojikouMoji, shojikouGyo);
                        }
                        if (useFormNnenForm4) {
                            _A4_bikoMaxLinePerPage -= 3;
                        }
                    }
                    if (Arrays.asList("KNJE070_A4_3NEN_1.frm", "KNJE070_A4_4NEN_1.frm").contains(_formNameA4_1)) {
                        _hankiNinteiTaiou = true;
                    }
                    if (_param._isOutputDebug) {
                        log.info(" A4 _formNenYou = " + _formNenYou);
                        log.info(" A4_totalstudyActShokenSize = " + _A4_totalstudyActShokenSize);
                        log.info(" A4_totalstudyValShokenSize = " + _A4_totalstudyValShokenSize);
                        log.info(" A4_spActSize = " + _A4_spActSize);
                        log.info(" A4_shojikouShokenSize = " + _A4_shojikouShokenSize);
                    }

                    _isShingakuyouFormRecordFormat2 = true;
                    _isShingakuyouFormShukketsuTitleFormat2 = true;
                    _shokenType = ShokenType.TYPE_FIELDAREA_REPEAT;
                    _isKyotoForm = true;
                }

                if (null != _formNameA4_BIKO) {
                    final String path = svf.getPath(_formNameA4_BIKO);
                    final SvfForm svfForm = new SvfForm(new File(path));
                    if (svfForm.readFile()) {
                        final SvfForm.Field field = svfForm.getField("field9");
                        if (null != field) {
                            SvfForm.Repeat repeat = svfForm.getRepeat(field._repeatConfig._repeatNo);
                            if (null == repeat) {
                                log.warn(" no repeat " + field._fieldname);
                            } else {
                                _A4_bikoOnlyPageMaxLine = repeat._count;
                            }
                        }
                    }
                    if (_param._isOutputDebug) {
                        log.info(" _A4_bikoOnlyPageMaxLine = " + _A4_bikoOnlyPageMaxLine);
                    }
                }
            }

            public boolean useFormNnen() {
                return (_tyousasyo2020shojikouExtends.in(Extends._3, Extends._2pageBiko) || _useGradeMultiPage) && _useFormNnen == 1;
            }

            public boolean useFormNnen4() {
                return _tyousasyo2020shojikouExtends.in(Extends._4) && _useFormNnen4 == 1;
            }
        }

        private int totalstudyRecordAreaHeight(final OutputMethod outputMethod) {
            if (outputMethod.useRecord()) {
                return FORM_RECORD_TOTALSTUDY_HEIGHT * (Util.getMappedList(outputMethod.recordDivFormRecordListMap, RecordDiv.TOTALSTUDY_ACT).size() + Util.getMappedList(outputMethod.recordDivFormRecordListMap, RecordDiv.TOTALSTUDY_VAL).size());
            } else {
                return 1056;
            }
        }

        private int totalstudyActRecordAreaHeight(final OutputMethod outputMethod) {
            if (outputMethod.useRecord()) {
                return FORM_RECORD_TOTALSTUDY_HEIGHT * Util.getMappedList(outputMethod.recordDivFormRecordListMap, RecordDiv.TOTALSTUDY_ACT).size();
            } else {
                return totalstudyRecordAreaHeight(outputMethod) / 2;
            }
        }

        private int specialActRecordAreaHeight(final OutputMethod outputMethod) {
            if (outputMethod.useRecord()) {
                return FORM_RECORD_SPECIALACTREMARK_HEIGHT * Util.getMappedList(outputMethod.recordDivFormRecordListMap, RecordDiv.SPECIALACT).size();
            } else {
                return _formInfo._formNen == FormNen._3 ? 1056 : 1706;
            }
        }

        private int shojikouBlockLines() {
            return (_formInfo._A4_shojikouShokenSize._gyo + _shojikouHeaderLines);
        }

        private int shojikouStartY1(final OutputMethod outputMethod) {
            return SUBFORM_START_Y + totalstudyRecordAreaHeight(outputMethod) + specialActRecordAreaHeight(outputMethod);
        }

        private boolean printMainA4(final DB2UDB db2, final Vrw32alp svf, final PrintData printData, final List<Title> titles) throws FileNotFoundException {

            _printData = printData;
            _isCsv = _printData.isCsv();
            _svf = svf;

            param().logOnce("printMainA4 :: form = " + Arrays.asList(_formInfo._formNameA4_1, _formInfo._formNameA4_2, _formInfo._formNameA4_3, _formInfo._formNameA4_4, _formInfo._formNameA4_BIKO));

            final String touten;
            if (_printData._isKyoto) {
                touten = "、";
            } else {
                touten = "，";
            }

            _shojikouTitleMap = new TreeMap<Seq, String>();
            _shojikouTitleMap.put(seq1, "(1)学習における特徴等");
            _shojikouTitleMap.put(seq2, "(2)行動の特徴" + touten + "特技等");
            _shojikouTitleMap.put(seq3, "(3)部活動" + touten + "ボランティア活動" + touten + "\n 留学・海外経験等");
            _shojikouTitleMap.put(seq4, "(4)取得資格" + touten + "検定等");
            _shojikouTitleMap.put(seq5, "(5)表彰・顕彰等の記録");
            _shojikouTitleMap.put(seq6, "(6)その他");

            final String form1 = _formInfo._formNameA4_1;
            setForm(_svf, form1, 4, param());
            _formInfo.setSvfForm(_svf, form1, this, param());
//            setForm1Record(_isFormatOnly, _titlePage);

            final TitlePage titlePageAll = new TitlePage(0);
            titlePageAll._titleList.addAll(titles);
            final OutputMethod outputMethod = new OutputMethod(param(), _printData, _formInfo, titlePageAll);

            final List<Map<RecordDiv, List<FormRecord>>> recordDivRecordListMapPageList = outputMethod.getRecordDivRecordListMapPageList(titlePageAll);

            final List<TitlePage> titlePageList = TitlePage.getTitlePageList(_param, titles, _formInfo._formNenYou._intval);
            int maxkind1page = 0;
            for (final Indexed<TitlePage> p : Indexed.indexed(titlePageList)) {
                if (titlePageList.size() > 1) {
                    log.info(" idx " + p._idx + ", titlePages = " + p._val);
                }
                FormRecordPart formRecord = setForm1Record(_isFormatOnly, p._val);
                maxkind1page = Math.max(maxkind1page, formRecord.getMaxPage1(_printData));
            }

            final int _kind1page = maxkind1page; // 成績
            final int _kind23page; // 総学、特別活動の記録、諸事項
            final int _kind4page; // 備考、出欠の記録、証明文言

            if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2page)) {
                _kind23page = 1;
                _kind4page = 0;
            } else if (_formInfo.useFormNnen4()) {
                if (outputMethod.useRecord()) {
                    // 6年用、8年用等
                    _kind23page = recordDivRecordListMapPageList.size();
                    _kind4page = 0; // recordDivRecordListMapPageListに統合
                } else {
                    _kind23page = 1 /* 総学、特別活動の記録、諸事項1年目 */ + 1 /* 諸事項残り */;
                    _kind4page = recordDivRecordListMapPageList.size();
                }
            } else {
                if (outputMethod.useRecord()) {
                    _kind23page = recordDivRecordListMapPageList.size();
                } else {
                    _kind23page = 1 /* 総学、特別活動の記録、諸事項1年目 */ + 1 /* 諸事項残り */;
                }
                if (outputMethod.useRecord() && _formInfo.useFormNnen()) {
                    _kind4page = 0; // recordDivRecordListMapPageListに統合
                } else {
                    final ImmutableList<List<ShokenLine>> bikoOnlyPages = outputMethod.bikoPagesAndLastPageBikoLines._first;
                    _kind4page = bikoOnlyPages.size() + 1;
                }
            }
            final int maxPage = titlePageList.size() * _kind1page + _kind23page + _kind4page;
            if (_param._isOutputDebug) {
                log.info(" titlePageList size = " + titlePageList.size() + ", maxPage = " + _kind1page + " + " + _kind23page + " + " + _kind4page);
            }

            final List<FORM_KIND> FORM_KINDS;
            if (null != param()._outputDebugA4PageList && !param()._outputDebugA4PageList.isEmpty()) {
                FORM_KINDS = new ArrayList<FORM_KIND>();
                for (final String a4p : param()._outputDebugA4PageList) {
                    final FORM_KIND fk = FORM_KIND.get(a4p);
                    if (null != fk) {
                        FORM_KINDS.add(fk);
                    }
                }
            } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2page)) {
                FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2PAGE_2);
            } else if (outputMethod.useRecord()) {
                if (_formInfo.useFormNnen()) {
                    FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2_3_4_2RECORD);
                } else if (outputMethod._useRecordBikoOnly) {
                    FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2_3_2RECORD, FORM_KIND._4_BIKOU_SHUKKETSU_RECORD);
                } else {
                    FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2_3_2RECORD, FORM_KIND._4_BIKOU_SHUKKETSU);
                }
            } else if (outputMethod._useRecordBikoOnly) {
                FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2, FORM_KIND._3_SHOJIKOU, FORM_KIND._4_BIKOU_SHUKKETSU_RECORD);
            } else {
                FORM_KINDS = Arrays.asList(FORM_KIND._1, FORM_KIND._2, FORM_KIND._3_SHOJIKOU, FORM_KIND._4_BIKOU_SHUKKETSU);
            }
            if (_param._isOutputDebug) {
                log.info(" FORM KINDS = " + FORM_KINDS);
            }

            int page = 0;
            for (FORM_KIND formkind : FORM_KINDS) {

                log.info(" PRINT FORM KIND = " + formkind);

                Title.setPosition(_param, printData, titles);

                if (formkind.in(FORM_KIND._1)) {

                    for (final Indexed<TitlePage> p : Indexed.indexed(titlePageList)) {

                        Title.setPosition(_param, printData, p._val._titleList);
                        _printData.formRecord = setForm1Record(_isFormatOnly, p._val);

                        for (int pageIdx = 0; pageIdx < _kind1page; pageIdx++) {
                            page += 1;
                            final boolean isLast = pageIdx == _kind1page - 1;
                            String form = form1;
                            final List<FormRecord> recordList = _isFormatOnly ? new ArrayList<FormRecord>() : _printData.formRecord.getPageRecordList(pageIdx);
                            form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, p._val, isLast, recordList, new FormA4ConfigParam().setFormKind(formkind).setA4Page(page));
                            _formInfo.setSvfForm(_svf, form, this, param());
                            setForm(_svf, form, 4, param());

                            printA4HeaderName(maxPage, page, formkind);
                            if (!_isFormatOnly) {
                                printSeisekiTitle(p._val);
                            }
                            printHeadCommon(_isFormatOnly);
                            setSvfFieldInfo(_printData, param(), _currentForm);
                            printAddressCommon(db2, _isFormatOnly); //氏名、住所等出力
                            printHyoteiHeikin(_isFormatOnly);
                            if (!_isFormatOnly) {
                                printSeisekiDankai_1(); //成績段階別人数の出力
                            }
                            printStudyrec_1(_isFormatOnly, recordList, isLast, p._val); //学習の記録出力
                        }

                        if (!getFormInfo()._useGradeMultiPage) {
                            break;
                        }
                    }

                } else if (formkind.in(FORM_KIND._2, FORM_KIND._3_SHOJIKOU)) {
                    String form;
                    if (FORM_KIND._2 == formkind) {
                        form = _formInfo._formNameA4_2;
                    } else { // if (FORM_KIND._3_SHOJIKOU == formkind) {
                        form = _formInfo._formNameA4_3;
                    }

                    page = printFormKind23(printData, titlePageAll, outputMethod, maxPage, page, formkind, form, new FormA4ConfigParam());
                    _svf.VrEndPage();

                } else if (formkind.in(FORM_KIND._2_3_2RECORD, FORM_KIND._2_3_4_2RECORD, FORM_KIND._4_BIKOU_SHUKKETSU_RECORD)) {

                    int nextPi = 0;
                    if (formkind.in(FORM_KIND._2_3_2RECORD, FORM_KIND._2_3_4_2RECORD)) {
                        final int shojikouStartY1 = shojikouStartY1(outputMethod);
                        final int totalstudyActRecordY1 = SUBFORM_START_Y;
                        final int totalstudyValRecordY1 = SUBFORM_START_Y + totalstudyActRecordAreaHeight(outputMethod);
                        final int specialActRecordStartY = SUBFORM_START_Y + totalstudyRecordAreaHeight(outputMethod);

                        final int blockLines = (_formInfo._A4_shojikouShokenSize._gyo + _shojikouHeaderLines);
                        int page1Count = (SUBFORM_END_Y - shojikouStartY1) / FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT / blockLines * blockLines; // 計算順に注意
                        if (recordDivRecordListMapPageList.size() > 0) {
                            int c = 0;
                            final Map<RecordDiv, List<FormRecord>> m = recordDivRecordListMapPageList.get(0);
                            for (final RecordDiv d : m.keySet()) {
                                if (d == RecordDiv.SHOJIKOU) {
                                    c += m.get(d).size();
                                }
                            }
                            page1Count = c;
                        }

                        Integer shojikouPage1TitleHeightY1 = shojikouStartY1 + (SUBFORM_END_Y - shojikouStartY1 - outputMethod.SHOJIKOU_TITLE_HEIGHT) / 2;

                        if (0 == page1Count) {
                            shojikouPage1TitleHeightY1 = null;
                        } else {
                            shojikouPage1TitleHeightY1 = shojikouStartY1 + (page1Count * FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT - outputMethod.SHOJIKOU_TITLE_HEIGHT) / 2;
                        }

                        Tuple<Integer, Integer> bikoTitleXY = null;

                        // 所見最初のページ
                        final Map<RecordDiv, List<FormRecord>> page0Map = recordDivRecordListMapPageList.get(0);
                        if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                            if (Util.getMappedList(page0Map, RecordDiv.BIKO).size() > 0) {
                                if (null == shojikouPage1TitleHeightY1) {
                                    bikoTitleXY = null;
                                } else {
                                    bikoTitleXY = Tuple.of(211, shojikouStartY1 +  page1Count * FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT);
                                }
                            }
                        }

                        page += 1;
                        String form = _formInfo._formNameA4_2RECORD;
                        final FormA4ConfigParam cfgParam = new FormA4ConfigParam()
                                .setFormKind(formkind)
                                .setA4Page(page)
                                .setA4ShojikouStartY(shojikouPage1TitleHeightY1)
                                .setTotalstudyRecordY(Tuple.of(totalstudyActRecordY1, specialActRecordStartY))
                                .setTotalstudyActRecordY(Tuple.of(totalstudyActRecordY1, totalstudyValRecordY1))
                                .setTotalstudyValRecordY(Tuple.of(totalstudyValRecordY1, specialActRecordStartY))
                                .setSpecialActRecordY(Tuple.of(specialActRecordStartY, shojikouStartY1))
                                .setBikoTitleXY(bikoTitleXY)
                                ;
                        form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, titlePageAll, false, null, cfgParam);
                        setForm(_svf, form, 4, param());
                        printA4HeaderName(maxPage, page, formkind);

                        for (final RecordDiv d : Arrays.asList(RecordDiv.TOTALSTUDY_ACT, RecordDiv.TOTALSTUDY_VAL, RecordDiv.SPECIALACT)) {
                            if (param()._isOutputDebugShoken) {
                                if (d == RecordDiv.TOTALSTUDY_ACT) {
                                    log.info("5." + _printData.getSogoSubclassname(param()) + "の内容");
                                } else if (d == RecordDiv.TOTALSTUDY_VAL) {
                                    log.info("5." + _printData.getSogoSubclassname(param()) + "の評価");
                                } else if (d == RecordDiv.SPECIALACT) {
                                    log.info("6.特別活動の記録");
                                }
                            }
                            final List<FormRecord> list = Util.getMappedList(outputMethod.recordDivFormRecordListMap, d);
                            for (int i = 0; i < list.size(); i++) {
                                final FormRecord record = list.get(i);
                                FormRecordPart.printSvfFieldData(this, record);
                                svfVrEndRecord();
                            }
                        }

                        if (param()._isOutputDebugShoken) {
                            log.info("7.指導上参考となる諸事項");
                        }

                        for (int i = 0; i < Util.getMappedList(page0Map, RecordDiv.SHOJIKOU).size(); i++) {
                            final FormRecord record = Util.getMappedList(page0Map, RecordDiv.SHOJIKOU).get(i);
                            FormRecordPart.printSvfFieldData(this, record);
                            svfVrEndRecord();
                        }

                        if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                            // 熊本
                            for (final RecordDiv d : Arrays.asList(RecordDiv.BIKO, RecordDiv.ATTEND, RecordDiv.CERTIF_SCHOOL)) {
                                for (int i = 0; i < Util.getMappedList(page0Map, d).size(); i++) {
                                    final FormRecord record = Util.getMappedList(page0Map, d).get(i);
                                    FormRecordPart.printSvfFieldData(this, record);
                                    svfVrEndRecord();
                                }
                            }
                        }
                        nextPi = 1;
                    }

                    // 所見2ページ以降
                    for (int pi = nextPi; pi < recordDivRecordListMapPageList.size(); pi++) {

                        final List<RecordDiv> divs;
                        if (formkind.in(FORM_KIND._2_3_4_2RECORD)) {
                            divs = Arrays.asList(RecordDiv.SHOJIKOU, RecordDiv.BIKO, RecordDiv.ATTEND, RecordDiv.CERTIF_SCHOOL);
                        } else if (formkind.in(FORM_KIND._4_BIKOU_SHUKKETSU_RECORD)) {
                            divs = Arrays.asList(RecordDiv.BIKO, RecordDiv.ATTEND, RecordDiv.CERTIF_SCHOOL);
                        } else { // if (formkind.in(FORM_KIND._2_3_2RECORD)) {
                            divs = Arrays.asList(RecordDiv.SHOJIKOU);
                        }

                        final Map<RecordDiv, List<FormRecord>> pageRecords = recordDivRecordListMapPageList.get(pi);

                        boolean hasData = false;
                        for (final RecordDiv d : divs) {
                            if (pageRecords.containsKey(d)) {
                                hasData = true;
                                break;
                            }
                        }
                        if (!hasData) {
                            continue;
                        }

                        page += 1;

                        Integer shojikouStartY2 = null;
                        if (pageRecords.containsKey(RecordDiv.SHOJIKOU)) {
                            final int starty2 = SUBFORM_START_Y;
                            final int yohaku = 50;
                            final int height = Util.getMappedList(pageRecords, RecordDiv.SHOJIKOU).size() * FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT;
                            if (height >= outputMethod.SHOJIKOU_TITLE_HEIGHT + yohaku) {
                                shojikouStartY2 = starty2 + (height - outputMethod.SHOJIKOU_TITLE_HEIGHT) / 2;
                            } else {
                                shojikouStartY2 = null;
                            }
                            if (_param._isOutputDebug) {
                                log.info(" shojikou start Y2 = " + shojikouStartY2 + ", height = " + height);
                            }
                        }

                        final FormA4ConfigParam cfgParam = new FormA4ConfigParam().setFormKind(formkind).setA4Page(page).setA4ShojikouStartY(shojikouStartY2).set6nen8en(_formInfo._formNenYou.in(FormNen._6, FormNen._8));

                        if (formkind.in(FORM_KIND._2_3_4_2RECORD, FORM_KIND._4_BIKOU_SHUKKETSU_RECORD)) {
                            if (pageRecords.containsKey(RecordDiv.BIKO)) {
                                final int shojikouHeight = Util.getMappedList(pageRecords, RecordDiv.SHOJIKOU).size() * FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT;
                                final int bikoY = SUBFORM_START_Y + shojikouHeight + 20;

                                cfgParam.setBikoTitleXY(Tuple.of(200,  bikoY));
                            }
                            if (pageRecords.containsKey(RecordDiv.ATTEND)) {
                                cfgParam.setAttendFormStartY(1);
                            }
                        }

                        String form;
                        if (formkind.in(FORM_KIND._4_BIKOU_SHUKKETSU_RECORD)) {
                            form = _formInfo._formNameA4_4; // NNEN4
                        } else {
                            form = _formInfo._formNameA4_2RECORD;
                        }
                        form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, titlePageAll, false, null, cfgParam);
                        setForm(_svf, form, 4, param());
                        printA4HeaderName(maxPage, page, formkind);


                        if (param()._isOutputDebug) {
                            log.info(" page = " + page + ", pi " + pi + " / keys = " + pageRecords.keySet());
                        }

                        for (final RecordDiv d : divs) {
                            if (!pageRecords.containsKey(d)) {
                                continue;
                            }

                            final List<FormRecord> formRecordList = Util.getMappedList(pageRecords, d);
                            if (d == RecordDiv.CERTIF_SCHOOL) {
                                if (formRecordList.size() == 0) {
                                    FormRecord fieldRecord = new FormRecord();
                                    formRecordList.add(fieldRecord);
                                }
                                FormRecord fieldRecord = formRecordList.get(0);
                                fieldRecord = fieldRecord.merge(getHead_1());
                                fieldRecord.setData("CERT_DUMMY", "1");
                                formRecordList.set(0, fieldRecord);
                            }

                            if (param()._isOutputDebug) {
                                log.info("  size " + d + " = " + formRecordList.size());
                            }


                            for (int i = 0; i < formRecordList.size(); i++) {
                                final FormRecord record = formRecordList.get(i);
                                FormRecordPart.printSvfFieldData(this, record);
                                svfVrEndRecord();
                            }
                        }
                    }

                    if (formkind == FORM_KIND._2_3_4_2RECORD) {
                        if (page % 2 == 1) { // 奇数の場合、最終ページ空白を追加
                            page += 1;
                            _svf.VrSetForm("BLANK_A4_TATE.frm", 1);
                            _svf.VrsOut("BLANK", "BLANK");
                            _svf.VrEndPage();
                        }
                    }

                } else if (formkind.in(FORM_KIND._4_BIKOU_SHUKKETSU)) {

                    final FormA4ConfigParam configParamBikoOnlyPage = new FormA4ConfigParam().setBikoTitleXY(Tuple.of(175, (Integer) null)).set6nen8en(_formInfo._formNenYou.in(FormNen._6, FormNen._8)).setFormKind(formkind);
                    page = printFormKind4(printData, titlePageAll, outputMethod, maxPage, page, formkind, configParamBikoOnlyPage);

                    if (page % 2 == 1) { // 奇数の場合、最終ページ空白を追加
                        page += 1;
                        _svf.VrSetForm("BLANK_A4_TATE.frm", 1);
                        _svf.VrsOut("BLANK", "BLANK");
                        _svf.VrEndPage();
                    }

                } else if (formkind.in(FORM_KIND._2PAGE_2)) {

                    final FormA4ConfigParam cfgParam = new FormA4ConfigParam()
                            .setTotalstudyRecordY(Tuple.of(314, 894))
                            .setTotalstudyActRecordY(Tuple.of(314, 602))
                            .setTotalstudyValRecordY(Tuple.of(602, 894))
                            .setBikoTitleXY(Tuple.of(211, 3118))
                            ;

                    // 最後のページなのでpage変数を更新しない
                    printFormKind23(printData, titlePageAll, outputMethod, maxPage, page, formkind, _formInfo._formNameA4_2PAGE_2, cfgParam);
                    printFormKind4(printData, titlePageAll, outputMethod, null, null, formkind, null);
                }
            }
            return true;
        }

        private int printFormKind23(final PrintData printData, final TitlePage titlePage, final OutputMethod outputMethod, final int maxPage, int page, final FORM_KIND formkind, String form, final FormA4ConfigParam cfgParam) throws FileNotFoundException {
            final boolean isAddShojikouHeader = true;
            page += 1;
            form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, titlePage, false, null, cfgParam.setFormKind(formkind).setA4Page(page));
            final int formN = FORM_KIND._2PAGE_2 == formkind ? 4 : 1;
            setForm(_svf, form, formN, param());
            printA4HeaderName(maxPage, page, formkind);
            final List<Title> titleList = Title.getValidated(titlePage._titleList);
            if (FORM_KIND._2 == formkind || FORM_KIND._2PAGE_2 == formkind) {

                for (final Title title : titleList) {
                    final int pos = title._pos;
                    svfVrsOut("GRADE4_" + pos, title._name);  // 特別活動の記録
                }
                final List<Title> shojikouTitleList = Util.take(1, titleList); // 指導上参考となる諸事項は1番目のみ
                printA4ShojikouTitle(shojikouTitleList);

                for (int g = 1; g <= 1; g++) {
                    printA4ShojikouItem(g);
                }

                if (!_printData._notPrintShoken) {
                    if (param()._isOutputDebugShoken) {
                        log.info(Util.debugMapToStr("shoken = ", _printData._hexamEntremarkHdat));
                        log.info(Util.debugMapToStr("shoken year = ", _printData._hexamEntremarkDatMap));
                    }

                    // 5.総合的な学習の時間の内容・評価
                    if (param()._isOutputDebugShoken) {
                        log.info("5." + _printData.getSogoSubclassname(param()) + "の内容・評価");
                    }
                    if (_printData.sogoIsSuraShingakuyou("", param(), null)) {
                        if (param()._isOutputDebug) {
                            log.info("/// " + _printData.getSogoSubclassname(param()) + "の内容・評価");
                        }
                    } else {
                        if (!_isFormatOnly) {
                            print8Sogaku(titlePage);
                        }
                    }

                    if (param()._isOutputDebugShoken) {
                        log.info("6.特別活動の記録");
                    }
                    if (!_isFormatOnly) {
                        for (final Title title : titleList) {
                            final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, titleList, title._year);
                            // 6.特別活動の記録
                            print6specialActRec(title, dat);
                        }
                    }

                    if (param()._isOutputDebugShoken) {
                        log.info("7.指導上参考となる諸事項");
                    }
                    if (!_isFormatOnly) {
                        for (final Title title : shojikouTitleList) {

                            print7syojikouA4Kotei(title._pos, outputMethod.getA4SeqShojikouLinesMap(outputMethod._shojikouToBiko, title._year, isAddShojikouHeader));
                        }
                    }
                }
            }
            if (FORM_KIND._3_SHOJIKOU == formkind || FORM_KIND._2PAGE_2 == formkind) {

                for (int g = 1; g <= 3; g++) {
                    printA4ShojikouItem(g);
                }
                final int shojikouStartIdx;
                if (FORM_KIND._2PAGE_2 == formkind) {
                    shojikouStartIdx = 0;
                } else {
                    shojikouStartIdx = 1; // 指導上参考となる諸事項は2番目から
                }

                final List<Title> shojikouTitleList = Util.drop(shojikouStartIdx, titleList);
                for (int i = 0; i < shojikouTitleList.size(); i++) {
                    shojikouTitleList.set(i, (Title) shojikouTitleList.get(i).clone());
                }
                Title.setPosition(param(), _printData, shojikouTitleList);

                printA4ShojikouTitle(shojikouTitleList);

                if (!_printData._notPrintShoken) {
                    // 7.指導上参考となる諸事項
                    if (param()._isOutputDebugShoken) {
                        log.info("7.指導上参考となる諸事項");
                    }
                    if (!_isFormatOnly) {
                        for (final Title title : shojikouTitleList) {
                            print7syojikouA4Kotei(title._pos, outputMethod.getA4SeqShojikouLinesMap(outputMethod._shojikouToBiko, title._year, isAddShojikouHeader));
                        }
                    }
                }
            }
            return page;
        }

        private Integer printFormKind4(final PrintData printData, final TitlePage titlePage, final OutputMethod outputMethod, final Integer maxPage, Integer page, final FORM_KIND formkind, final FormA4ConfigParam cfgparam) throws FileNotFoundException {

            Tuple<ImmutableList<List<ShokenLine>>, ImmutableList<ShokenLine>> bikoPagesAndLastPageBikoLines = outputMethod.bikoPagesAndLastPageBikoLines;
            if (param()._isOutputDebug) {
                log.info(" 備考のみページ数: " + bikoPagesAndLastPageBikoLines._first.size());
                log.info(" 最終備考ページ行数: " + bikoPagesAndLastPageBikoLines._second.size());
            }

            // 備考欄のみフォーム
            final ImmutableList<List<ShokenLine>> bikoPages = bikoPagesAndLastPageBikoLines._first;
            {
                String form0 = _formInfo._formNameA4_BIKO;
                if (null == form0) {
                    log.info(" 備考のみフォーム指定無し");
                } else {
                    for (int bpi = 0; bpi < bikoPages.size(); bpi++) {
                        final List<ShokenLine> bikoPageLines = bikoPages.get(bpi);
                        if (null != page) {
                            page += 1;
                        }
                        String form = form0;
                        form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, titlePage, false, null, cfgparam.setFormKind(FORM_KIND._4_BIKOU_ONLY).setA4Page(page));
                        setForm(_svf, form, 1, param());
                        printA4HeaderName(maxPage, page, formkind);

                        final List<String> bikoBoldList = new ArrayList<String>();
                        final List<String> bikoList = new ArrayList<String>();
                        for (final ShokenLine biko : bikoPageLines) {
                            bikoBoldList.add(StringUtils.defaultString(biko._boldText));
                            bikoList.add(StringUtils.defaultString(biko._text));
                        }
                        printSvfNRepLine("field9_BOLD", bikoBoldList);
                        printSvfNRepLine("field9", bikoList);

                        _svf.VrEndPage();
                    }
                }
            }


            final ImmutableList<ShokenLine> lastPageBikoLines = bikoPagesAndLastPageBikoLines._second;
            {
                // 備考欄と出欠のフォーム
                String form = _formInfo._formNameA4_4;
                if (null != page) {
                    page += 1;
                }
                if (null != form) {
                    form = _formInfo.setConfigFormShingakuyou(_svf, form, printData, titlePage, false, null, new FormA4ConfigParam().setFormKind(formkind).setA4Page(page).set6nen8en(_formInfo._formNenYou.in(FormNen._6, FormNen._8)));
                    setForm(_svf, form, 4, param());
                }
                printA4HeaderName(maxPage, page, formkind);
                printHeadCommon2(_isFormatOnly);
                FormRecordPart.printSvfFieldData(this, getAttendFormRecord(_isFormatOnly, titlePage)); //出欠の出力
                if (!_isFormatOnly) {
                    printHead_1();
                }

                if (!_printData._notPrintShoken) {

                    if (param()._isOutputDebugShoken) {
                        log.info("9.出欠備考");
                    }
                    final FormRecord formRecord = getAttendRemarkFormRecord(titlePage);
                    FormRecordPart.printSvfFieldData(this, formRecord);
                }

                if (param()._isOutputDebugShoken) {
                    log.info("8.備考");
                }

                // #DUMMY9
                final List<SvfField> dummy9s = searchField("DUMMY9.*");
                for (final ShokenLine biko : lastPageBikoLines) {
                    for (final SvfField field : dummy9s) { svfVrsOut(field._name, "."); }
                    svfVrsOut("field9_BOLD", biko._boldText);
                    svfVrsOut("field9", biko._text);
                    svfVrEndRecord();
                }
            }
            return page;
        }

        private FormRecord getAttendRemarkFormRecord(final TitlePage titlePage) {
            FormRecord formRecord = new FormRecord();
            for (final Title title : titlePage._titleList) {
                final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, titlePage._titleList, title._year);

                // 5.出欠備考
                if (!_isFormatOnly) {
                    formRecord = formRecord.merge(print5attendrecRemark(title, dat));
                }
            }
            return formRecord;
        }

        private void printA4HeaderName(final Integer maxPage, final Integer page, final FORM_KIND formkind) {
            if (null != maxPage && null != page) {
                svfVrsOut("PAGE", String.valueOf(maxPage) + "枚中の" + String.valueOf(page) + "枚目");
            }

            final boolean isSeisekiForm = formkind == FORM_KIND._1;
            printSyoushoNum(isSeisekiForm, _isFormatOnly);  // 証書番号を出力

            if (!_isFormatOnly) {
                svfVrsOut("SCHREGNO", _printData._schregno);

                if (formkind != FORM_KIND._1) {
                    if (null != page) {
                        final boolean isPrintNameWhenPageEven = "1".equals(_printData.property(Property.tyousasho2020PrintHeaderName)) || "check".equals(_printData.property(Property.tyousasho2020PrintHeaderName)) && "1".equals(_printData.getParameter(Parameter.KNJE070D_PRINTHEADERNAME));
                        if (page % 2 == 1 || isPrintNameWhenPageEven && page % 2 == 0) { // 奇数ページに（もしくはプロパティで偶数ページにも）氏名に出力
                            final String[] nameLines = getNameKanaAndName(_printData._personInfo, 99999, 36, false)._second;
                            if (param()._isOutputDebug) {
                                for (int i = 0; i < nameLines.length; i++) {
                                    param().logOnce(" name [" + i + "] ( keta = " + getTextKeta(nameLines[i]) + ") = " + nameLines[i]);
                                }
                            }
                            if (nameLines.length == 1) {
                                svfVrsOutForData(Arrays.asList("STUDENT_NAME", "STUDENT_NAME2", "STUDENT_NAME_36KETA", "STUDENT_NAME_40KETA", "STUDENT_NAME_50KETA", "STUDENT_NAME_60KETA", "STUDENT_NAME_84KETA", "STUDENT_NAME_84KETA2L_N"), nameLines[0], true);
                            } else if (nameLines.length == 2) {
                                final int len1 = getTextKeta(nameLines[0]);
                                final int len2 = getTextKeta(nameLines[1]);
                                final int sum = len1 + len2;
                                final int max = Math.max(len1, len2);
                                final String add = nameLines[0] + nameLines[1];
                                if (max <= 36) {
                                    svfVrsOut("STUDENT_NAME_36KETA2L_1", nameLines[0]);
                                    svfVrsOut("STUDENT_NAME_36KETA2L_2", nameLines[1]);
                                } else if (max <= 40) {
                                    svfVrsOut("STUDENT_NAME_40KETA2L_1", nameLines[0]);
                                    svfVrsOut("STUDENT_NAME_40KETA2L_2", nameLines[1]);
                                } else if (max <= 50) {
                                    svfVrsOut("STUDENT_NAME_50KETA2L_1", nameLines[0]);
                                    svfVrsOut("STUDENT_NAME_50KETA2L_2", nameLines[1]);
                                } else if (max <= 60) {
                                    svfVrsOut("STUDENT_NAME_60KETA2L_1", nameLines[0]);
                                    svfVrsOut("STUDENT_NAME_60KETA2L_2", nameLines[1]);
                                } else if (max <= 84) {
                                    svfVrsOut("STUDENT_NAME_84KETA2L_1", nameLines[0]);
                                    svfVrsOut("STUDENT_NAME_84KETA2L_2", nameLines[1]);
                                } else if (sum <= 36 * 2) {
                                    svfVrsOut("STUDENT_NAME_36KETA2L_N", add);
                                } else if (sum <= 40 * 2) {
                                    svfVrsOut("STUDENT_NAME_40KETA2L_N", add);
                                } else if (sum <= 50 * 2) {
                                    svfVrsOut("STUDENT_NAME_50KETA2L_N", add);
                                } else if (sum <= 60 * 2) {
                                    svfVrsOut("STUDENT_NAME_60KETA2L_N", add);
                                } else { // if (sum <= 84 * 2) {
                                    svfVrsOut("STUDENT_NAME_84KETA2L_N", add);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void printA4ShojikouItem(final int g) {
            for (final Map.Entry<Seq, String> e : _shojikouTitleMap.entrySet()) {
                final Seq seq = e.getKey();
                final String header = e.getValue();
                for (int i = 1; i <= _shojikouHeaderLines; i++) {
                    svfVrAttributen("field8_" + g + "g_" + seq.value(), i, ATTRIBUTE_BOLD);
                }
                printSvfNRepLine("field8_" + g + "g_" + seq.value(), KNJ_EditKinsoku.getTokenList(header, _formInfo._A4_shojikouShokenSize.keta()));
            }
        }

        private void printA4ShojikouTitle(final List<Title> titleList) {
            final boolean isNendo = PrintData.PRINT_NENDO == _printData.getPrintGradeTitleMethod(param());
            for (final Title title : titleList) {
                final int pos = title._pos;
                if (isNendo) {
                    final List<String> nameArray = title._nameArray1;
                    for (int i = 0; i < nameArray.size(); i++) {
                        svfVrsOutn("GRADE5_" + pos, i + 1, nameArray.get(i));  // 指導上参考となる諸事項
                    }
                } else {
                    for (int i = 0; i < StringUtils.defaultString(title._name).length(); i++) {
                        svfVrsOutn("GRADE5_" + pos, i + 1, String.valueOf(title._name.charAt(i)));  // 指導上参考となる諸事項
                    }
                }
            }
        }

        /**
         * "7.指導上参考となる諸事項"を出力する
         */
        private void print7syojikouA4Kotei(final int pos, final Map<Seq, List<String>> shokenLinesMap) {
            if (_printData._isFuhakkou) {
                return;
            }

            for (final Map.Entry<Seq, List<String>> e : shokenLinesMap.entrySet()) {
                final Seq seq = e.getKey();
                final List<String> tokenList = e.getValue();
                for (int i = 1; i <= _shojikouHeaderLines; i++) {
                    svfVrAttributen("field8_" + pos + "g_" + seq.value(), i, ATTRIBUTE_BOLD);
                }
                if (tokenList.size() > 0 && BIKOU_RANNNI_KISAI.equals(tokenList.get(tokenList.size() - 1))) {
                    svfVrAttributen("field8_" + pos + "g_" + seq.value(), tokenList.size(), ATTRIBUTE_BOLD + "," + ATTRIBUTE_MIGITSUME);
                }
                printSvfNRepLine("field8_" + pos + "g_" + seq.value(), tokenList);
            }
        }

        private static class ShokenLine {
            final String _boldText;
            final String _text;
            public ShokenLine(final String boldText, final String text) {
                _boldText = boldText;
                _text = text;
            }
            public ShokenLine(final String text) {
                this("", text);
            }
            public String toString() {
                return "ShokenLine(" + _boldText + ", " + _text + ")";
            }
        }

        private enum ShokenOutputOpt {
            TO_BIKO, // 残りを備考欄に移す
            EXTEND, // 欄を拡張する (レコードのみ)
            TRUNCATE // 欄を超えた場合表示しない
        }

        private class OutputMethod {

            final String space = "　";
            // 指導上参考となる諸事項の備考出力
            final int bikoFieldKeta = 92 - getMS932ByteLength(space);

            final ImmutableList<ShokenLine> bikoList;
            final Tuple<ImmutableList<List<ShokenLine>>, ImmutableList<ShokenLine>> bikoPagesAndLastPageBikoLines; // 備考のみのページと最終ページ（出席備考付き）のリスト

            /** フォームレコード使用時のレコード */
            Map<RecordDiv, List<FormRecord>> recordDivFormRecordListMap = new TreeMap<RecordDiv, List<FormRecord>>();

            final int SHOJIKOU_TITLE_HEIGHT = 800; // 「指導上参考となる諸事項」

            final List<String> emptyRecord = new ArrayList<String>();
            final List<String> emptyBiko = new ArrayList<String>();
            final Tuple<List<String>, List<String>> empty = Tuple.of(emptyRecord, emptyBiko);

//            final boolean _useRecord;
            boolean _useRecordBikoOnly = false;
            final ShokenOutputOpt _shojikouToBiko;
            final ShokenOutputOpt _sonotaOverToBiko;
            ShokenOutputOpt _bikoOverToBiko;

            private OutputMethod(final Param param, final PrintData _printData, final FormInfoKNJE070_1_A4 _formInfo, final TitlePage titlePage) {

                boolean useRecord = false;
                _bikoOverToBiko = ShokenOutputOpt.EXTEND;

                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2page, Extends._2pageBiko)) {
                    if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                        useRecord = true;
                        _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                        _bikoOverToBiko = ShokenOutputOpt.TO_BIKO;
                    } else {
                        _shojikouToBiko = ShokenOutputOpt.TRUNCATE;
                        _bikoOverToBiko = ShokenOutputOpt.TRUNCATE;
                    }
                    _sonotaOverToBiko = ShokenOutputOpt.TRUNCATE;
                } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._4)) {
                    // 奈良県
                    if (_formInfo._formNenYou == FormNen._6 || _formInfo._formNenYou == FormNen._8) {
                        useRecord = true;
                    }
                    _useRecordBikoOnly = true;
                    _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                    _sonotaOverToBiko = ShokenOutputOpt.TO_BIKO;
                } else if (_formInfo._formNenYou == FormNen._6 || _formInfo._formNenYou == FormNen._8) {
                    // 京都府通信制
                    useRecord = true;
                    if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._1, Extends._2, Extends._3)) {
                        _shojikouToBiko = ShokenOutputOpt.EXTEND;
                        _sonotaOverToBiko = ShokenOutputOpt.EXTEND;
                    } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2_2)) {
                        useRecord = true;
                        _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                        _sonotaOverToBiko = ShokenOutputOpt.TRUNCATE;
                    } else {
                        // null, Extends._4
                        _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                        _sonotaOverToBiko = ShokenOutputOpt.TO_BIKO;
                    }
                } else {
                    if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._1, Extends._2, Extends._3)) {
                        // 三重県
                        useRecord = true;
                        _shojikouToBiko = ShokenOutputOpt.EXTEND;
                        _sonotaOverToBiko = ShokenOutputOpt.EXTEND;
                    } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2_2)) {
                        useRecord = true;
                        _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                        _sonotaOverToBiko = ShokenOutputOpt.TRUNCATE;
                    } else {
                        // null, Extends._4
                        // 京都府
                        _shojikouToBiko = ShokenOutputOpt.TO_BIKO;
                        _sonotaOverToBiko = ShokenOutputOpt.TO_BIKO;
                    }
                }
//                _useRecord = useRecord;
                if (param._isOutputDebug) {
                    log.info(" useRecord = " + useRecord);
                    log.info(" isShojikouToBiko = " + _shojikouToBiko);
                    log.info(" sonotaOverToBiko = " + _sonotaOverToBiko);
                }

                final List<ShokenLine> toBikoList = new ArrayList<ShokenLine>();
                if (useRecord) {

                    // 総合的な学習の時間 学習内容
                    Util.destructuringAddAll(Tuple.of(Util.getMappedList(recordDivFormRecordListMap, RecordDiv.TOTALSTUDY_ACT), toBikoList), getTotalStudyRecordList(_sonotaOverToBiko, _printData.getSogoSubclassname(param) + " 学習内容", _formInfo._A4_totalstudyActShokenSize, "0", getTotalStudyAct(titlePage)));
                    // 総合的な学習の時間 評価
                    Util.destructuringAddAll(Tuple.of(Util.getMappedList(recordDivFormRecordListMap, RecordDiv.TOTALSTUDY_VAL), toBikoList), getTotalStudyRecordList(_sonotaOverToBiko, _printData.getSogoSubclassname(param) + " 評価", _formInfo._A4_totalstudyValShokenSize, "1", getTotalStudyVal(titlePage)));

                    if (param._isOutputDebugShoken) {
                        for (final RecordDiv d : Arrays.asList(RecordDiv.TOTALSTUDY_ACT, RecordDiv.TOTALSTUDY_VAL)) {
                            for (int i = 0; i < Util.getMappedList(recordDivFormRecordListMap, d).size(); i++) {
                                final FormRecord r = Util.getMappedList(recordDivFormRecordListMap, d).get(i);
                                log.info(" totalstudy " + i + " = " + r._dataMap);
                            }
                        }
                        for (int i = 0; i < toBikoList.size(); i++) {
                            ShokenLine l = toBikoList.get(i);
                            log.info(" to biko " + i + " = " + l._text);
                        }
                    }

                    Util.destructuringAddAll(Tuple.of(Util.getMappedList(recordDivFormRecordListMap, RecordDiv.SPECIALACT), toBikoList), getSpecialActRecordList(_printData, _formInfo, titlePage, _sonotaOverToBiko));

                    final List<Title> shojikouTitleList = titlePage._titleList; // レコードなのですべて
                    Util.getMappedList(recordDivFormRecordListMap, RecordDiv.SHOJIKOU).addAll(getA4ShojikouRecordList(_shojikouToBiko, shojikouTitleList, _formInfo));
                }

                if (_shojikouToBiko == ShokenOutputOpt.TO_BIKO) {
                    // 諸事項が欄をあふれた場合、備考欄に出力する
                    for (final Map.Entry<Year, Map<Seq, String>> e : _printData._yearHexamEntremarkTrainRefDatMap.entrySet()) {
                        final Year year = e.getKey();
                        final Title title = Title.getTitle(param, titlePage._titleList, year.value());
                        if (null == title) {
                            continue;
                        }
                        final Map<Seq, String> trainRefMap = e.getValue();

                        for (final Map.Entry<Seq, String> itemEnt : _shojikouTitleMap.entrySet()) {
                            final Seq seq = itemEnt.getKey();
                            final String trainRef = trainRefMap.get(seq);

                            final Tuple<List<String>, List<String>> printedLinesAndToBikoLines = getPrintedLinesAndToBikoLines(_shojikouToBiko, seq + "(" + title._year + ") ", trainRef, _formInfo._A4_shojikouShokenSize);
                            final String bikoPrintLine = Util.mkString(printedLinesAndToBikoLines._second, newLine); // 欄からあふれた備考

                            if (!StringUtils.isEmpty(bikoPrintLine)) {
                                final String bikoTitle = Util.mkString(title._nameArray1, "") + StringUtils.replace(itemEnt.getValue(), "\n", "");

                                toBikoList.addAll(asBikoList(bikoTitle, bikoPrintLine));
                            }
                        }
                    }
                }
                bikoList = ImmutableList.of(getBikoList(toBikoList, titlePage));
                bikoPagesAndLastPageBikoLines = getA4BikoPages(bikoList);
            }

            public boolean useRecord() {
                boolean useRecord = false;
                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2page, Extends._2pageBiko)) {
                    useRecord = _formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko);
                } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._4)) {
                    // 奈良県
                    if (_formInfo._formNenYou == FormNen._6 || _formInfo._formNenYou == FormNen._8) {
                        useRecord = true;
                    }
                } else if (_formInfo._formNenYou == FormNen._6 || _formInfo._formNenYou == FormNen._8) {
                    // 京都府通信制
                    useRecord = true;
                } else if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._1, Extends._2, Extends._2_2, Extends._3)) {
                    // 三重県
                    useRecord = true;
                } else {
                }
                return useRecord;
            }

            private List<ShokenLine> asBikoList(final String title, final String data) {
                final List<ShokenLine> list = new ArrayList<ShokenLine>();
                final String head = StringUtils.replace("・" + title, " ", "");
                list.add(new ShokenLine(head, StringUtils.repeat(" ", getMS932ByteLength(head)) + "の続き"));

                final List<String> tokenList = KNJ_EditKinsoku.getTokenList(data, bikoFieldKeta);
                for (int i = 0; i < tokenList.size(); i++) {
                    list.add(new ShokenLine(space + tokenList.get(i)));
                }

                list.add(new ShokenLine("")); // 1行空白

                return list;
            }

            private Tuple<List<FormRecord>, List<ShokenLine>> getSpecialActRecordList(final PrintData _printData, final FormInfoKNJE070_1_A4 _formInfo, final TitlePage _titlePage, final ShokenOutputOpt isOverToBiko) {
                final List<FormRecord> spActRecordList = new ArrayList<FormRecord>();
                final List<ShokenLine> toBikoList = new ArrayList<ShokenLine>();

                boolean groupFlg = false;
                // 特別活動の記録
                final ShokenSize size = _formInfo._A4_spActSize;

                final int maxCol = _formInfo._A4_printDouble ? _formInfo._formNen._intval / 2 : _formInfo._formNen._intval;

                final List<Title> spActRecordTitleList = new ArrayList<Title>(_titlePage._titleList);
                for (int i = 0; i < spActRecordTitleList.size(); i++) {
                    spActRecordTitleList.set(i, (Title) spActRecordTitleList.get(i).clone());
                }

                final List<List<Title>> groupTitles = Util.splitByCount(spActRecordTitleList, maxCol);
                if (_formInfo._A4_printDouble) {
                    if (groupTitles.size() < 2) {
                        groupTitles.add(new ArrayList<Title>()); // 2段目
                    } else if (groupTitles.size() > 2) {
                        while (groupTitles.size() > 2) {
                            groupTitles.remove(2);
                        }
                    }
                }
                for (final List<Title> groupTitle : groupTitles) {
                    Title.setPosition(param(), _printData, groupTitle);
                    final Map<Title, List<String>> map = new HashMap<Title, List<String>>();
                    groupFlg = !groupFlg;
                    for (final Title title : groupTitle) {
                        final HexamEntremarkDat dat = _printData.getHexamEntremarkDat(_param, groupTitle, title._year);

                        final String spActTitle = "特別活動　" + Util.mkString(title._nameArray2020Shojikou, "");
                        final Tuple<List<String>, List<String>> spl = getPrintedLinesAndToBikoLines(isOverToBiko, spActTitle, null == dat ? "" : dat.specialactrec(), size);


                        final String bikoPrintLine = Util.mkString(spl._second, newLine);
                        if (!StringUtils.isEmpty(bikoPrintLine)) {
                            spl._first.add(BIKOU_RANNNI_KISAI);
                            toBikoList.addAll(asBikoList(spActTitle, bikoPrintLine));
                        }
                        map.put(title, spl._first);
                    }

                    int maxLine = size._gyo;
                    for (final List<String> tokenList : map.values()) {
                        maxLine = Math.max(maxLine, tokenList.size());
                    }

                    final FormRecord header = FormRecord.createRecord(spActRecordList);
                    header.setData("RECORD_SP_R2_FLG0", "0");  // 特別活動の記録
                    for (int pos = 1; pos <= maxCol; pos++) {
                        header.setData("RECORD_SP_R2_FLG" + pos, groupFlg ? "1" : "0");  // 特別活動の記録
                    }
                    if (!_isFormatOnly) {
                        for (final Title title : groupTitle) {
                            final int pos = title._pos;
                            header.setData("GRADE4_" + pos + "_R2", title._name);  // 特別活動の記録
                        }
                    }

                    groupFlg = !groupFlg;

                    for (int i = 0; i < maxLine; i++) {
                        final FormRecord record = FormRecord.createRecord(spActRecordList);
                        record.setData("RECORD_SP_R2_FLG0", "0");  // 特別活動の記録
                        for (int pos = 1; pos <= maxCol; pos++) {
                            record.setData("RECORD_SP_R2_FLG" + pos, groupFlg ? "1" : "0");  // 特別活動の記録
                            record.setData("RECORD_SP_R2_FLG" + pos + "_2", groupFlg ? "1" : "0");  // 特別活動の記録
                        }
                        for (final Title title : groupTitle) {
                            final int pos = title._pos;
                            final List<String> tokenList = map.get(title);
                            if (i < tokenList.size()) {
                                final String token = tokenList.get(i);
                                record.setData("field7_" + pos + "g_R2", token);  // 特別活動の記録
                                if (BIKOU_RANNNI_KISAI.equals(token)) {
                                    record.addAttr("field7_" + pos + "g_R2", ATTRIBUTE_BOLD + "," + ATTRIBUTE_MIGITSUME);
                                }
                            }
                        }
                    }
                }
                return Tuple.of(spActRecordList, toBikoList);
            }

            private Tuple<List<FormRecord>, List<ShokenLine>> getTotalStudyRecordList(final ShokenOutputOpt isOverToBiko, final String title, final ShokenSize size, final String groupFlg, final String data) {

                final Tuple<List<String>, List<String>> spl = getPrintedLinesAndToBikoLines(isOverToBiko, title, data, size);

                final List<ShokenLine> toBikoList = new ArrayList<ShokenLine>();
                final String bikoPrintLine = Util.mkString(spl._second, newLine);
                if (!StringUtils.isEmpty(bikoPrintLine)) {
                    toBikoList.addAll(asBikoList(title, bikoPrintLine));
                }

                final List<String> tokenList = spl._first;
                if (!StringUtils.isEmpty(bikoPrintLine)) {
                    tokenList.add(BIKOU_RANNNI_KISAI);
                }

                final List<FormRecord> totalstudysRecordList = new ArrayList<FormRecord>();
                for (int i = 0; i < Math.max(tokenList.size(), size._gyo); i++) {

                    final FormRecord record = FormRecord.createRecord(totalstudysRecordList);

                    record.setData("DUMMY5_0_R2", "0");
                    record.setData("DUMMY5_1_R2", groupFlg);
                    record.setData("DUMMY5_2_R2", groupFlg);
                    record.setData("DUMMY5_3_R2", groupFlg);
                    record.setData("DUMMY5_4_R2", groupFlg);
                    record.setData("DUMMY5_5_R2", groupFlg);
                    if (i < tokenList.size()) {
                        final String token = tokenList.get(i);
                        record.setData("TOTALSTUDY", token);
                        if (BIKOU_RANNNI_KISAI.equals(token)) {
                            record.addAttr("TOTALSTUDY", ATTRIBUTE_BOLD + "," + ATTRIBUTE_MIGITSUME);
                        }
                    }
                }
                return Tuple.of(totalstudysRecordList, toBikoList);
            }

            /**
             * レコードタイプのフォーム用の諸事項FormRecordを得る
             */
            private List<FormRecord> getA4ShojikouRecordList(final ShokenOutputOpt shokenOutputOpt, final List<Title> titleList, final FormInfoKNJE070_1_A4 _formInfo) {

                int gradeCount = 1;
                final List<FormRecord> recordList = new ArrayList<FormRecord>();
                final List<String> seqgrpFields = Arrays.asList("GRP_D1", "GRP_D1_2", "GRP_D1_3", "GRP_D2", "GRP_D2_2", "GRP_D3", "GRP_D3_2");

                boolean gradegrpflg = false;
                boolean seqgrpflg = false;
                for (final Title title : titleList) {
                    gradegrpflg = !gradegrpflg;
                    final String gradeflg = gradegrpflg ? "0" : "1";

                    final List<FormRecord> yearRecordList = new ArrayList<FormRecord>();

                    final Map<Seq, List<String>> seqShokenLinesMap = getA4SeqShojikouLinesMap(shokenOutputOpt, title._year, false);

                    for (final List<Seq> seqs : Arrays.asList(Arrays.asList(seq1, seq2, seq3), Arrays.asList(seq4, seq5, seq6))) {

                        seqgrpflg = !seqgrpflg;
                        final String seqflg = seqgrpflg ? "2" : "3";

                        yearRecordList.addAll(getA4ShojikuHeaderRecordList(_formInfo, seqgrpFields, gradeflg, seqs, seqflg));

                        int maxLine = _formInfo._A4_shojikouShokenSize._gyo;
                        for (final Seq seq : seqs) {
                            final List<String> lines = seqShokenLinesMap.get(seq);
                            maxLine = Math.max(maxLine, lines.size());
                        }
                        for (int li = 0; li < maxLine; li++) {
                            final FormRecord record = FormRecord.createRecord(yearRecordList);

                            record.setData("GRP_G", "0");
                            record.setData("GRP_H", gradeflg);
                            for (final String field : seqgrpFields) { record.setData(field, seqflg); }

                            for (int seqi = 0; seqi < seqs.size(); seqi++) {
                                final Seq seq = seqs.get(seqi);
                                final List<String> lines = seqShokenLinesMap.get(seq);
                                String line = "";
                                if (li < lines.size()) {
                                    line = lines.get(li);
                                }
                                record.setData("field8_REC_" + String.valueOf(seqi + 1), line);
                                if (BIKOU_RANNNI_KISAI.equals(line)) {
                                    record.addAttr("field8_REC_" + String.valueOf(seqi + 1), ATTRIBUTE_BOLD + "," + ATTRIBUTE_MIGITSUME);
                                }
                            }
                        }
                    }

                    if (!_isFormatOnly) {
                        for (final FormRecord fr : yearRecordList) {
                            fr.setData("#SETTING.GRADE5", Util.concatList(title._nameArray2020Shojikou));
                        }
                    }
                    recordList.addAll(yearRecordList);
                    gradeCount += 1;
                }

                // 所見欄空欄のページ
                for (int gi = gradeCount; gi <= _formInfo._formNen._intval; gi += 1) {
                    gradegrpflg = !gradegrpflg;
                    final String gradeflg = gradegrpflg ? "0" : "1";

                    final List<FormRecord> yearRecordList = new ArrayList<FormRecord>();

                    for (final List<Seq> seqs : Arrays.asList(Arrays.asList(seq1, seq2, seq3), Arrays.asList(seq4, seq5, seq6))) {

                        seqgrpflg = !seqgrpflg;
                        final String seqflg = seqgrpflg ? "2" : "3";

                        yearRecordList.addAll(getA4ShojikuHeaderRecordList(_formInfo, seqgrpFields, gradeflg, seqs, seqflg));

                        final int maxLine = _formInfo._A4_shojikouShokenSize._gyo;

                        for (int li = 0; li < maxLine; li++) {
                            final FormRecord record = FormRecord.createRecord(yearRecordList);

                            record.setData("GRP_G", "0");
                            record.setData("GRP_H", gradeflg);
                            for (final String field : seqgrpFields) { record.setData(field, seqflg); }
                        }
                    }
                    recordList.addAll(yearRecordList);
                }
                return recordList;
            }

            private List<FormRecord> getA4ShojikuHeaderRecordList(final FormInfoKNJE070_1_A4 _formInfo, final List<String> seqgrpFields, final String gradeflg, final List<Seq> seqs, final String seqflg) {

                final List<FormRecord> headerRecords = new ArrayList<FormRecord>();
                for (int hli = 0; hli < _shojikouHeaderLines; hli++) {

                    final FormRecord headerRecord = FormRecord.createRecord(headerRecords);
                    headerRecord.setData("GRP_G", "0");
                    headerRecord.setData("GRP_H", gradeflg);
                    for (final String field : seqgrpFields) { headerRecord.setData(field, seqflg); }

                    for (int seqi = 0; seqi < seqs.size(); seqi++) {
                        final Seq seq = seqs.get(seqi);

                        final String header = _shojikouTitleMap.get(seq);
                        final List<String> lines = KNJ_EditKinsoku.getTokenList(header, _formInfo._A4_shojikouShokenSize.keta());
                        String line = null;
                        if (hli < lines.size()) {
                            line = lines.get(hli);
                        }
                        headerRecord.setData("field8_REC_" + String.valueOf(seqi + 1), line);
                        headerRecord.addAttr("field8_REC_" + String.valueOf(seqi + 1), ATTRIBUTE_BOLD);
                    }
                }
                return headerRecords;
            }

            /**
             * 備考出力用所見の行リスト
             * 欄に出力する行リストと、備考欄に出力する行リストのTupleを得る
             */
            private Tuple<List<String>, List<String>> getPrintedLinesAndToBikoLines(final ShokenOutputOpt isOverToBiko, final String hint, final String data, final ShokenSize shokenSize) {
                if (_isFormatOnly) {
                    return empty;
                }
                final List<String> toBikoLines = new ArrayList<String>();
                final List<String> printedLines = new ArrayList<String>();
                if (null == data) {
                    return Tuple.of(printedLines, toBikoLines);
                }
                boolean isOver = false;
                final String[] split = StringUtils.replace(data, "\r\n", newLine).split(newLine);
                for (int i = 0; i < split.length; i++) {
                    final boolean isLastToken = i == split.length - 1;
                    final String line = split[i];

                    if (isOver) {
                        toBikoLines.add(line);
                        //log.info(" targetLine2 = " + Util.mkString(targetLine, newLine));
                        continue;
                    }

                    final List<String> tokenList = KNJ_EditKinsoku.getTokenList(line, shokenSize.keta());
                    boolean isThisOver = false;
                    if (isOverToBiko == ShokenOutputOpt.TO_BIKO) {
                        if (isLastToken) {
                            isThisOver = shokenSize._gyo - 0 < printedLines.size() + tokenList.size();
                        } else {
                            isThisOver = shokenSize._gyo - 1 < printedLines.size() + tokenList.size();
                        }
                    }
                    if (param()._isOutputDebugShoken && isThisOver) {
                        log.info(" check over !! " + hint + " [" + i + "] / gyo = " + shokenSize._gyo + ", printed = " + printedLines.size() + ", token lines = " + tokenList.size());
                    }
                    if (isThisOver) {
                        isOver = true;
                        if (shokenSize._gyo - 1 < printedLines.size()) {

                            toBikoLines.add(printedLines.get(printedLines.size() - 1)); // 最後の1行は「続きを備考欄に記載」で出力されていないはずなので備考欄に出力
                            toBikoLines.add(line);
                            printedLines.remove(printedLines.size() - 1);
                            //log.info(" over 1 " + printedLines + " /\n, " + toBikoLines );
                        } else { // if (printedLine.size() <= shokenSize._gyo && shokenSize._gyo < printedLine.size() + tokenList.size()) {
                            final int rest = shokenSize._gyo - printedLines.size() - 1;
                            printedLines.addAll(tokenList.subList(0, rest));
                            toBikoLines.add(Util.mkString(tokenList.subList(rest, tokenList.size()), ""));
                            //log.info(" over 2 " + printedLines + " /\n, " +  toBikoLines);
                        }
                        //log.info(" targetLine1 = " + Util.mkString(targetLine, newLine));
                    } else {
                        printedLines.addAll(tokenList);
                    }
                }
                if (isOverToBiko == ShokenOutputOpt.TRUNCATE && shokenSize._gyo > 0 && printedLines.size() > shokenSize._gyo) {
                    return Tuple.of(printedLines.subList(0, shokenSize._gyo), toBikoLines);
                }
                return Tuple.of(printedLines, toBikoLines);
            }

            private Tuple<ImmutableList<List<ShokenLine>>, ImmutableList<ShokenLine>> getA4BikoPages(final ImmutableList<ShokenLine> bikoList) {

                final int page4maxLine;
                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                    page4maxLine = _formInfo._A4_bikoMaxLinePerPage;
                } else {
                    page4maxLine = _formInfo._A4_bikoMaxLinePerPage;
                }
                List<List<ShokenLine>> bikoOnlyPageList = new ArrayList<List<ShokenLine>>(); // 備考のみページ行のリスト
                final List<ShokenLine> lastPageBikoLines = new ArrayList<ShokenLine>();

                if (_bikoOverToBiko == ShokenOutputOpt.TRUNCATE) {
                    if (!bikoList.isEmpty()) {
                        lastPageBikoLines.addAll(bikoList.subList(0, Math.min(bikoList.size(), page4maxLine)));
                    }
                } else {
                    bikoOnlyPageList.addAll(Util.getPageList(bikoList, page4maxLine));
                    if (1 < bikoOnlyPageList.size()) {
                        bikoOnlyPageList = Util.getPageList(bikoList, _formInfo._A4_bikoOnlyPageMaxLine);

                        final List<ShokenLine> lastPageBikos = bikoOnlyPageList.get(bikoOnlyPageList.size() - 1);
                        if (lastPageBikos.size() <= page4maxLine) {
                            lastPageBikoLines.addAll(lastPageBikos);
                            bikoOnlyPageList = bikoOnlyPageList.subList(0, bikoOnlyPageList.size() - 1);
                        }
                    } else if (1 == bikoOnlyPageList.size()) {
                        final List<ShokenLine> bikoPageLastLineList = bikoOnlyPageList.get(bikoOnlyPageList.size() - 1);
                        lastPageBikoLines.addAll(bikoPageLastLineList);
                        bikoOnlyPageList.clear();
                    }
                }
                for (int i = lastPageBikoLines.size(); i < _formInfo._A4_bikoMaxLinePerPage; i++) {
                    lastPageBikoLines.add(new ShokenLine(""));
                }
                if (_param._isOutputDebug) {
                    final List<Integer> eachPageLines = new ArrayList<Integer>();
                    for (final List<ShokenLine> eachPage : bikoOnlyPageList) {
                        eachPageLines.add(eachPage.size());
                    }

                    log.info(" bikoOnlyPageList size = " + bikoOnlyPageList.size() + ", each pages = " + eachPageLines + " / bikoList size = " + bikoList.size());
                    log.info(" lastPageBikoLines size = " + lastPageBikoLines.size());
                }
                return Tuple.of(ImmutableList.of(bikoOnlyPageList), ImmutableList.of(lastPageBikoLines));
            }

            private List<ShokenLine> getBikoList(final List<ShokenLine> toBikoList, final TitlePage _titlePage) {
                final List<ShokenLine> bikoList = new ArrayList<ShokenLine>(toBikoList);
                if (_isFormatOnly) {
                    return bikoList;
                }
                if (!bikoList.isEmpty()) {
                    bikoList.add(new ShokenLine("・備考", ""));
                }

                if (!"1".equals(_printData.getParameter(Parameter.certifSchoolOnly))) {

                    if (null != _printData._toeflScore) {
                        bikoList.add(new ShokenLine(space + _printData._toeflScore));
                    }

                    final Tuple<List<String>, List<String>> kotei = getBikoKotei();
                    final List<String> koteiBefore = kotei._first;
                    final List<String> koteiAfter = kotei._second;

                    for (final String line : KNJ_EditKinsoku.getTokenList(Util.mkString(koteiBefore, "\n"), bikoFieldKeta)) {
                        bikoList.add(new ShokenLine(space + line));
                    }
                    // 代替備考 + 備考（6分割用）
                    for (final String line : KNJ_EditKinsoku.getTokenList(getPrintRemark(_printData._hexamEntremarkRemarkHdatRemark, _titlePage), bikoFieldKeta)) {
                        bikoList.add(new ShokenLine(space + line));
                    }
                    for (final String line : KNJ_EditKinsoku.getTokenList(Util.mkString(koteiAfter, "\n"), bikoFieldKeta)) {
                        bikoList.add(new ShokenLine(space + line));
                    }

                }

                // 証明書学校データ備考
                final StringBuffer certifSchoolDatRemark = new StringBuffer();
                for (final String s : getPrintCertifSchoolDatRemark()) {
                    if (null == s || s.length() == 0) {
                        continue;
                    }
                    if (certifSchoolDatRemark.length() == 0) {
                        certifSchoolDatRemark.append(s);
                        continue;
                    }
                    boolean addNewLine = true;
                    final char[] skipChars = {' ', '　', '\t'};
                    final char[] toutenChars = {'、', '，'};
                    char lastChar = 0;
                    for (int idx = certifSchoolDatRemark.length() - 1; idx >= 0; idx--) {
                        final char ch = certifSchoolDatRemark.charAt(idx);
                        if (ArrayUtils.contains(skipChars, ch)) {
                            continue;
                        }
                        // 最後の文字
                        lastChar = ch;
                        break;
                    }
                    if (0 != lastChar && ArrayUtils.contains(toutenChars, lastChar) || getMS932ByteLength(s) > bikoFieldKeta) {
                        addNewLine = false;
                    }

                    if (addNewLine) {
                        certifSchoolDatRemark.append("\n");
                    }
                    certifSchoolDatRemark.append(s);
                }

                for (final String line : KNJ_EditKinsoku.getTokenList(certifSchoolDatRemark.toString(), bikoFieldKeta)) {
                    bikoList.add(new ShokenLine(space + line));
                }
                return bikoList;
            }

            private Map<Seq, List<String>> getA4SeqShojikouLinesMap(final ShokenOutputOpt shokenOutputOpt, final String year, final boolean isAddShojikouHeader) {
                final Map<Seq, List<String>> shokenLinesMap = new TreeMap<Seq, List<String>>();
                Map<Seq, String> trainRefMap = Collections.emptyMap(); // 読取のみ
                if (Util.toInt(year, -1) == 0 && !_printData._yearHexamEntremarkTrainRefDatMap.containsKey(Year.of(year))) {
                    for (final String y : _printData._hexamEntremarkDatMap.keySet()) {
                        if (Util.toInt(y, -1) == 0) {
                            trainRefMap = Util.getMappedMap(_printData._yearHexamEntremarkTrainRefDatMap, Year.of(y));
                            break;
                        }
                    }
                    if (null == trainRefMap) {
                        trainRefMap = Util.getMappedMap(_printData._yearHexamEntremarkTrainRefDatMap, Year.of(year));
                    }
                } else if (null != year) {
                    trainRefMap = Util.getMappedMap(_printData._yearHexamEntremarkTrainRefDatMap, Year.of(year));
                }

                for (final Map.Entry<Seq, String> e : _shojikouTitleMap.entrySet()) {
                    final Seq seq = e.getKey();
                    final String trainRef = _isFormatOnly ? null : trainRefMap.get(seq);
                    final String header = e.getValue();
                    List<String> tokenList = new ArrayList<String>();
                    if (isAddShojikouHeader) {
                        final List<String> headerLineList = new ArrayList();
                        headerLineList.addAll(KNJ_EditKinsoku.getTokenList(header, _formInfo._A4_shojikouShokenSize.keta()));
                        for (int i = headerLineList.size(); i < _shojikouHeaderLines; i++) {
                            headerLineList.add("");
                        }
                        tokenList.addAll(headerLineList);
                    }

                    final Tuple<List<String>, List<String>> shojikouListAndBikoList = getPrintedLinesAndToBikoLines(shokenOutputOpt, "諸事項 " + year + "-" + seq.value(), trainRef, _formInfo._A4_shojikouShokenSize);
                    tokenList.addAll(shojikouListAndBikoList._first);
                    if (shokenOutputOpt == ShokenOutputOpt.TO_BIKO) {
                        if (!shojikouListAndBikoList._second.isEmpty()) {
                            tokenList.add(BIKOU_RANNNI_KISAI);
                        }
                    }

                    shokenLinesMap.put(seq, tokenList);
                }
                return shokenLinesMap;
            }

            /**
             * 所見の種別とその行レコードのマップのリストを得る
             * @param param
             * @param shojikouRecordList
             * @param page1Count 1ページ目行数
             * @param page2Count 2ページ目行数
             * @return 所見の種別とその行レコードのマップのリスト
             */
            private List<Map<RecordDiv, List<FormRecord>>> getShojikouRecordPageList(final Param param, final List<FormRecord> shojikouRecordList, final int page1Count, final int page2Count) {
                final List<Map<RecordDiv, List<FormRecord>>> recordPageList = new ArrayList<Map<RecordDiv, List<FormRecord>>>();
                Map<RecordDiv, List<FormRecord>> current = new TreeMap<RecordDiv, List<FormRecord>>();
                recordPageList.add(current);
                int size = page1Count;
                boolean isPage1 = true;
                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2, Extends._2_2) && page1Count < 10) {
                    current = new TreeMap<RecordDiv, List<FormRecord>>();
                    recordPageList.add(current);
                    size = page2Count;
                    isPage1 = false;
                }

                final String GRP_H = "GRP_H";
                if (param._z010.in(Z010Info.jyoto)) {
                    // 学年ごとにページに収まらなければ改ページ
                    String grph = null;
                    List<List<FormRecord>> gradeGroups = new ArrayList<List<FormRecord>>(); // 学年ごとにグループ化した行
                    List<FormRecord> current2 = null;
                    for (final FormRecord fr : shojikouRecordList) {
                        if (null == fr._dataMap.get(GRP_H)) {
                            break;
                        }
                        if (null == grph || !grph.equals(fr._dataMap.get(GRP_H))) {
                            current2 = new ArrayList<FormRecord>();
                            gradeGroups.add(current2);
                        }
                        current2.add(fr);
                        grph = fr._dataMap.get(GRP_H);
                    }

                    for (final List<FormRecord> gradeShojikouRecordList : gradeGroups) {

                        if (param()._isOutputDebug) {
                            log.info(" gradeShojikouRecodList " + gradeShojikouRecordList.get(0) + " / " + gradeShojikouRecordList.size());
                        }
                        boolean currentPageIsEmpty = true;
                        if (isPage1) {
                            currentPageIsEmpty = false;
                        } else {
                            for (final Map.Entry<RecordDiv, List<FormRecord>> e : current.entrySet()) {
                                if (e.getValue().size() > 0) {
                                    currentPageIsEmpty = false;
                                    break;
                                }
                            }
                        }

                        if (!currentPageIsEmpty) {
                            if (Util.getMappedList(current, RecordDiv.SHOJIKOU).size() + gradeShojikouRecordList.size() >= size) {
                                current = new TreeMap<RecordDiv, List<FormRecord>>();
                                recordPageList.add(current);
                                size = page2Count;
                                isPage1 = false;
                            }
                        }

                        for (final FormRecord formRecord : gradeShojikouRecordList) {
                            if (Util.getMappedList(current, RecordDiv.SHOJIKOU).size() >= size) { // 行数を超えたら改ページ
                                current = new TreeMap<RecordDiv, List<FormRecord>>();
                                recordPageList.add(current);
                                size = page2Count;
                            }
                            Util.getMappedList(current, RecordDiv.SHOJIKOU).add(formRecord);
                        }
                    }
                } else {
                    for (final FormRecord formRecord : shojikouRecordList) {
                        if (Util.getMappedList(current, RecordDiv.SHOJIKOU).size() >= size) { // 行数を超えたら改ページ
                            current = new TreeMap<RecordDiv, List<FormRecord>>();
                            recordPageList.add(current);
                            size = page2Count;
                        }
                        Util.getMappedList(current, RecordDiv.SHOJIKOU).add(formRecord);
                    }
                }

                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                } else {
                    // 1ページ行埋め
                    if (recordPageList.isEmpty()) {
                        recordPageList.add(new TreeMap<RecordDiv, List<FormRecord>>());
                        current = recordPageList.get(recordPageList.size() - 1);
                        for (int i = 0; i < page1Count; i++) {
                            FormRecord.createRecord(Util.getMappedList(current, RecordDiv.SHOJIKOU));
                        }
                    }
                }

                // 全ページ行埋め
                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2, Extends._2_2, Extends._3, Extends._2pageBiko)) {
                    // 1ページで収める
                } else {
                    // 最低2ページ
                    if (recordPageList.size() == 1) {
                        recordPageList.add(new TreeMap<RecordDiv, List<FormRecord>>());
                        current = recordPageList.get(recordPageList.size() - 1);
                        for (int i = 0; i < page2Count; i++) {
                            FormRecord.createRecord(Util.getMappedList(current, RecordDiv.SHOJIKOU));
                        }
                    }
                }

                for (final Map<RecordDiv, List<FormRecord>> pageRecordList : recordPageList) {
                    String grph = null;
                    List<List<FormRecord>> gradeGroups = new ArrayList<List<FormRecord>>();
                    List<FormRecord> current2 = null;
                    for (final FormRecord fr : Util.getMappedList(pageRecordList, RecordDiv.SHOJIKOU)) {
                        if (null == fr._dataMap.get(GRP_H)) {
                            break;
                        }
                        if (null == grph || !grph.equals(fr._dataMap.get(GRP_H))) {
                            current2 = new ArrayList<FormRecord>();
                            gradeGroups.add(current2);
                        }
                        current2.add(fr);
                        grph = fr._dataMap.get(GRP_H);
                    }

                    for (final List<FormRecord> sameGradeList : gradeGroups) {
                        if (sameGradeList.size() <= 6) {
                            continue;
                        }
                        String settingGrade = null;
                        for (final FormRecord fr : sameGradeList) {
                            if (null != fr._dataMap.get("#SETTING.GRADE5")) {
                                settingGrade = fr._dataMap.get("#SETTING.GRADE5");
                            }
                            fr._dataMap.remove("#SETTING.GRADE5");
                        }
                        if (null != settingGrade) {
                            final int gradeStartIdx = (sameGradeList.size() - settingGrade.length()) / 2;
                            for (int i = 0; i < settingGrade.length(); i++) {
                                sameGradeList.get(gradeStartIdx + i).setData("GRADE5", String.valueOf(settingGrade.charAt(i)));
                            }
                        }
                    }
                }
                return recordPageList;
            }

            private List<Map<RecordDiv, List<FormRecord>>> getRecordDivRecordListMapPageList(final TitlePage titlePageAll) {

                final OutputMethod outputMethod = this;
                final List<RecordDiv> recordDivMaeList = new ArrayList<RecordDiv>();
                if (_formInfo.useFormNnen()) {
                    recordDivMaeList.addAll(Arrays.asList(RecordDiv.TOTALSTUDY_ACT, RecordDiv.TOTALSTUDY_VAL, RecordDiv.SPECIALACT));
                }

                List<Map<RecordDiv, List<FormRecord>>> rtn = new ArrayList<Map<RecordDiv, List<FormRecord>>>();

                if (outputMethod.useRecord()) {
                    // shojikouRecordPageListに諸事項をセット
                    final int shojikouStartY1 = shojikouStartY1(outputMethod);
                    final int blockLines = shojikouBlockLines();
                    int page1Count = (SUBFORM_END_Y - shojikouStartY1) / FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT / blockLines * blockLines; // 計算順に注意
                    final int page2Count = (SUBFORM_END_Y - SUBFORM_START_Y) / FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT / blockLines * blockLines; // 計算順に注意

                    rtn = outputMethod.getShojikouRecordPageList(param(), Util.getMappedList(outputMethod.recordDivFormRecordListMap, RecordDiv.SHOJIKOU), page1Count, page2Count);
//    				if (_shojikouRecordPageList.size() > 0) {
//    					page1Count = _shojikouRecordPageList.get(0).size();
//    				}
                    if (param()._isOutputDebug) {
                        log.info(" page1count = " + page1Count + ", page2count = " + page2Count + " / blockLines = " + blockLines + ", shojikouStartY1 = " + shojikouStartY1);
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" shojikouRecordPageList.size() = " + rtn.size());
                    for (int i = 0; i < rtn.size(); i++) {
                        final Map<RecordDiv, List<FormRecord>> formRecordList = rtn.get(i);
                        for (final RecordDiv recordDiv : formRecordList.keySet()) {
                            log.info(" shojikouRecordPageList[" + i + "][" + recordDiv + "].size() = " + Util.getMappedList(formRecordList, recordDiv).size());
                        }
                    }
                }

                if (_formInfo.useFormNnen() || _formInfo.useFormNnen4()) {

                    final Map<RecordDiv, Integer> _recordDivRecordHeightMap = new HashMap<RecordDiv, Integer>();
                    _recordDivRecordHeightMap.put(RecordDiv.TOTALSTUDY_ACT, FormKNJE070_1_A4.FORM_RECORD_TOTALSTUDY_HEIGHT);
                    _recordDivRecordHeightMap.put(RecordDiv.TOTALSTUDY_VAL, FormKNJE070_1_A4.FORM_RECORD_TOTALSTUDY_HEIGHT);
                    _recordDivRecordHeightMap.put(RecordDiv.SPECIALACT, FormKNJE070_1_A4.FORM_RECORD_SPECIALACTREMARK_HEIGHT);
                    _recordDivRecordHeightMap.put(RecordDiv.SHOJIKOU, FormKNJE070_1_A4.FORM_RECORD_SHOJIKOU_HEIGHT);
                    _recordDivRecordHeightMap.put(RecordDiv.BIKO, FormKNJE070_1_A4.FORM_RECORD_BIKO_HEIGHT);
                    final int formRecordAttendHeight;
                    if (_formInfo._formNenYou.in(FormNen._6, FormNen._8)) {
                        formRecordAttendHeight = FormKNJE070_1_A4.FORM_RECORD_ATTEND_HEIGHT6NEN8NEN;
                    } else {
                        formRecordAttendHeight = FormKNJE070_1_A4.FORM_RECORD_ATTEND_HEIGHT3NEN4NEN;
                    }
                    _recordDivRecordHeightMap.put(RecordDiv.ATTEND, formRecordAttendHeight);
                    final int formRecordCertifSchoolHeight;
                    boolean isAddCertifSchoolnameSpace = _formInfo.useFormNnen4();
                    if (isAddCertifSchoolnameSpace) {
                        formRecordCertifSchoolHeight = FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_HEIGHT + FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_SPC_HEIGHT;
                    } else {
                        formRecordCertifSchoolHeight = FormKNJE070_1_A4.FORM_RECORD_CERTIF_SCHOOL_HEIGHT;
                    }
                    _recordDivRecordHeightMap.put(RecordDiv.CERTIF_SCHOOL, formRecordCertifSchoolHeight);

                    for (final RecordDiv mae : recordDivMaeList) {
                        if (rtn.isEmpty()) {
                            final Map<RecordDiv, List<FormRecord>> page = new TreeMap<RecordDiv, List<FormRecord>>();
                            rtn.add(page);
                        }
                        rtn.get(0).put(mae, Util.getMappedList(outputMethod.recordDivFormRecordListMap, mae));
                    }

                    final int useFormNenMinRemarkLine = 3;

                    final int subformHeight = SUBFORM_END_Y - SUBFORM_START_Y;
                    final List<RecordDiv> pageDivs = Arrays.asList(RecordDiv.BIKO, RecordDiv.ATTEND, RecordDiv.CERTIF_SCHOOL);
                    for (final RecordDiv d : pageDivs) {

                        final Integer recordHeight = _recordDivRecordHeightMap.get(d);
                        if (param()._isOutputDebug) {
                            log.info(" * process div " + d);
                        }
                        if (d == RecordDiv.BIKO) { // 備考!

                            List<ShokenLine> bikoList;
                            if (_formInfo.useFormNnen4()) {
                                for (final List<ShokenLine> pageLines : outputMethod.bikoPagesAndLastPageBikoLines._first) {
                                    rtn.add(new TreeMap<RecordDiv, List<FormRecord>>());

                                    final Map<RecordDiv, List<FormRecord>> lastPageRecordListForCalcHeight = rtn.get(rtn.size() - 1); // 最後のページ!
                                    final List<FormRecord> insertList = new ArrayList<FormRecord>();
                                    for (final ShokenLine shokenLine : pageLines) {
                                        final FormRecord r = new FormRecord();
                                        r.setData("field9", shokenLine._text);
                                        r.setData("field9_BOLD", shokenLine._boldText);
                                        r.setData("DUMMY9", "1");
                                        r.setData("DUMMY9_2", "1");
                                        r.setData("DUMMY9_3", "1");
                                        r.setData("DUMMY9_4", "1");
                                        r.setData("DUMMY9_5", "1");

                                        insertList.add(r);
                                    }

                                    Util.getMappedList(lastPageRecordListForCalcHeight, d).addAll(insertList);
                                }

                                bikoList = outputMethod.bikoPagesAndLastPageBikoLines._second.getSourceCopy();
                            } else {
                                bikoList = outputMethod.bikoList.getSourceCopy(); // 全ての備考行
                            }

                            for (int g = bikoList.size(); g <= useFormNenMinRemarkLine; g++) { // 最低4行
                                bikoList.add(new ShokenLine(""));
                            }

                            int loopCount = 0;
                            while (true) {
                                if (rtn.isEmpty()) {
                                    final Map<RecordDiv, List<FormRecord>> page = new TreeMap<RecordDiv, List<FormRecord>>();
                                    rtn.add(page);
                                }
                                final Map<RecordDiv, List<FormRecord>> lastPageRecordListForCalcHeight = rtn.get(rtn.size() - 1); // 最後のページ!

                                int currentHeight = 0;
                                final Set<RecordDiv> otherDivs = new HashSet<RecordDiv>();
                                for (final Map.Entry<RecordDiv, List<FormRecord>> ed : lastPageRecordListForCalcHeight.entrySet()) {
                                    final RecordDiv recordDiv = ed.getKey();
                                    final int size = ed.getValue().size();
                                    final Integer recordHeight2 = _recordDivRecordHeightMap.get(recordDiv);
                                    final int inDataHeight = recordHeight2 * size;
                                    currentHeight += inDataHeight;
                                    if (_param._isOutputDebug) {
                                        log.info(" *** inDataHeight " + recordDiv + " (" + recordHeight2 + ") x " + size + " = " + inDataHeight + " => " + currentHeight);
                                    }
                                    if (!pageDivs.contains(recordDiv)) {
                                        otherDivs.add(recordDiv);
                                    }
                                }

                                final int height = recordHeight * bikoList.size();

                                final boolean isPageBreak = currentHeight + height <= subformHeight;
                                if (param()._isOutputDebug) {
                                    log.info(" *** page break : " + isPageBreak + ", " + currentHeight + " + " + height + " (" + recordHeight + " * " + bikoList.size() + ") <= " + subformHeight);
                                }
                                if (isPageBreak) {
                                    break;
                                }

                                if (_formInfo.useFormNnen4()) {
                                    if (param()._isOutputDebug) {
                                        log.info("混在させずに改ページする: " + otherDivs + ", " + !otherDivs.isEmpty());
                                    }
                                    if (!otherDivs.isEmpty()) {
                                        rtn.add(new TreeMap<RecordDiv, List<FormRecord>>());
                                        continue;
                                    }
                                }

                                final int count = Math.min((subformHeight - currentHeight) / recordHeight, bikoList.size()); // このページに収まる行数
                                if (count <= useFormNenMinRemarkLine) {
                                    if (param()._isOutputDebug) {
                                        log.info("3行も入らないなら改ページする : " + count);
                                    }
                                    rtn.add(new TreeMap<RecordDiv, List<FormRecord>>());
                                    continue;
                                } else {

                                    final List<FormRecord> insertList = new ArrayList<FormRecord>();
                                    for (final ShokenLine shokenLine : bikoList.subList(0, count)) {
                                        final FormRecord r = new FormRecord();
                                        r.setData("field9", shokenLine._text);
                                        r.setData("field9_BOLD", shokenLine._boldText);
                                        r.setData("DUMMY9", "1");
                                        r.setData("DUMMY9_2", "1");
                                        r.setData("DUMMY9_3", "1");
                                        r.setData("DUMMY9_4", "1");
                                        r.setData("DUMMY9_5", "1");

                                        insertList.add(r);
                                    }

                                    Util.getMappedList(lastPageRecordListForCalcHeight, d).addAll(insertList);

                                    bikoList = bikoList.subList(count, bikoList.size()); // 残りの行数をセット
                                }
                                loopCount += 1;
                                if (loopCount > 50) {
                                    final String message = " subformHeight - currentHeight = " + (subformHeight - currentHeight) + ", / recordHeight = " + ((subformHeight - currentHeight) / recordHeight) + ", currentHeight + height = " + (currentHeight + height) + " / subformHeight = " + subformHeight;
                                    throw new IllegalArgumentException(message);
                                }
                            }

                            final List<FormRecord> insertList = new ArrayList<FormRecord>();
                            // 上の処理で1ページに収まる分だけ残っている
                            for (final ShokenLine shokenLine : bikoList) {
                                final FormRecord r = new FormRecord();
                                r.setData("field9", shokenLine._text);
                                r.setData("field9_BOLD", shokenLine._boldText);
                                r.setData("DUMMY9", "1");
                                r.setData("DUMMY9_2", "1");
                                r.setData("DUMMY9_3", "1");
                                r.setData("DUMMY9_4", "1");
                                r.setData("DUMMY9_5", "1");

                                insertList.add(r);
                            }

                            if (isAddCertifSchoolnameSpace) {
                                final FormRecord blankRecord = new FormRecord();
                                blankRecord.setData("DUMMY9", "1");
                                blankRecord.setData("DUMMY9_2", "1");
                                blankRecord.setData("DUMMY9_3", "1");
                                blankRecord.setData("DUMMY9_4", "1");
                                blankRecord.setData("DUMMY9_5", "1");

                                if (_formInfo._A4_bikoMaxLinePerPage < bikoList.size()) {
                                    final int blankCount = _formInfo._A4_bikoOnlyPageMaxLine - bikoList.size();
                                    if (param()._isOutputDebug) {
                                        log.info("備考が入りきれないのでページMAX行まで空行追加して改ページ : " + blankCount);
                                    }
                                    for (int i = 0, max = blankCount; i < max; i++) {
                                        insertList.add(blankRecord);
                                    }
                                    rtn.get(rtn.size() - 1).put(d, new ArrayList<FormRecord>(insertList));
                                    rtn.add(new TreeMap<RecordDiv, List<FormRecord>>());
                                    insertList.clear();
                                }
                                // 空行
                                for (int i = 0, max = _formInfo._A4_bikoMaxLinePerPage - insertList.size(); i < max; i++) {
                                    insertList.add(blankRecord);
                                }
                            }

                            rtn.get(rtn.size() - 1).put(d, insertList);

                        } else if (d == RecordDiv.ATTEND || d == RecordDiv.CERTIF_SCHOOL) { // 出欠の記録 学校名

                            final Map<RecordDiv, List<FormRecord>> lastPageRecordListForCalcHeight = rtn.get(rtn.size() - 1); // 最後のページ!

                            int currentHeight = 0;
                            for (final Map.Entry<RecordDiv, List<FormRecord>> ed : lastPageRecordListForCalcHeight.entrySet()) {
                                final RecordDiv recordDiv = ed.getKey();
                                final int size = ed.getValue().size();
                                final Integer recordHeight2 = _recordDivRecordHeightMap.get(recordDiv);
                                final int inDataHeight = recordHeight2 * size;
                                currentHeight += inDataHeight;
                                if (_param._isOutputDebug) {
                                    log.info(" " + d + " *** inDataHeight " + recordDiv + " (" + recordHeight2 + ") x " + size + " = " + inDataHeight + " => " + currentHeight);
                                }
                            }

                            final boolean isNewPage = currentHeight + recordHeight > subformHeight;
                            if (_param._isOutputDebug) {
                                log.info(" currentHeight = " + currentHeight + ", recordHeight = " + recordHeight + ", subformHeight = " + subformHeight + ", isNewPage = " + isNewPage);
                            }

                            if (isNewPage) {
                                rtn.add(new TreeMap<RecordDiv, List<FormRecord>>());
                                currentHeight = 0;
                            }
                            final Map<RecordDiv, List<FormRecord>> lastPageRecordMap = rtn.get(rtn.size() - 1);

                            if (d == RecordDiv.ATTEND) {

                                if (_formInfo._tyousasyo2020shojikouExtends.in(Extends._2pageBiko)) {
                                    final boolean is1page = !Util.getMappedList(lastPageRecordMap, RecordDiv.SPECIALACT).isEmpty();

                                    if (!is1page) {
                                        final int blankCount = (subformHeight - currentHeight - recordHeight - _recordDivRecordHeightMap.get(RecordDiv.CERTIF_SCHOOL)) / _recordDivRecordHeightMap.get(RecordDiv.BIKO);
                                        if (param()._isOutputDebug) {
                                            log.info("出欠の記録の前に行埋めする : " + blankCount);
                                        }
                                        final List<FormRecord> insertList = new ArrayList<FormRecord>();
                                        final FormRecord blankRecord = new FormRecord();
                                        blankRecord.setData("DUMMY9", "1");
                                        blankRecord.setData("DUMMY9_2", "1");
                                        blankRecord.setData("DUMMY9_3", "1");
                                        blankRecord.setData("DUMMY9_4", "1");
                                        blankRecord.setData("DUMMY9_5", "1");

                                        for (int i = 0, max = blankCount; i < max; i++) {
                                            insertList.add(blankRecord);
                                        }
                                        Util.getMappedList(lastPageRecordMap, RecordDiv.BIKO).addAll(insertList);
                                    }
                                }

                                final List<TitlePage> titlePages;
                                if (_formInfo._useGradeMultiPage) {
                                    titlePages = TitlePage.getTitlePageList(param(), titlePageAll._titleList, _formInfo._formNenYou._intval);
                                } else {
                                    titlePages = Arrays.asList(titlePageAll);
                                }
                                for (final TitlePage titlePage : titlePages) {
                                    final FormRecord r = getAttendFormRecord(_isFormatOnly, titlePage).merge(getAttendRemarkFormRecord(titlePage));
                                    Util.getMappedList(lastPageRecordMap, d).add(r);
                                }

                            } else if (d == RecordDiv.CERTIF_SCHOOL) {
                                final FormRecord r = getHeadCommon2(_isFormatOnly);
                                lastPageRecordMap.put(d, Arrays.asList(r));
                            }
                        }
                    }

                    if (param()._isOutputDebug) {

                        log.info(" calc shojikouRecordPage height ");

                        for (int pi = 0; pi < rtn.size(); pi++) {
                            final Map<RecordDiv, List<FormRecord>> page = rtn.get(pi);

                            log.info(" pi " + pi);

                            int totalHeight = 0;
                            for (final RecordDiv recordDiv : page.keySet()) {
                                final List<FormRecord> formRecordList = page.get(recordDiv);

                                final Integer height = _recordDivRecordHeightMap.get(recordDiv);
                                final int recordDivHeight = height * formRecordList.size();
                                log.info("    " + height + " (" + recordDiv + ") x lines " + formRecordList.size() + " = " + recordDivHeight);
                                totalHeight += recordDivHeight;
                            }
                            log.info("   total " + totalHeight);
                        }
                    }
                }
                return rtn;
            }
        }
    }

    private static enum FormNen {
        NONE(-1),
        _3(3),
        _4(4),
//        _6(2, 6), // debug
        _6(6),
        _8(8)
        ;

        final int _intval;
        final int _intval2;
        FormNen(final int intval) {
            this(intval, intval);
        }
        FormNen(final int intval, final int intval2) {
            _intval = intval;
            _intval2 = intval2;
        }
        public boolean in(final FormNen ...formNens) {
            if (null != formNens) {
                for (final FormNen formNen : formNens) {
                    if (this == formNen) {
                        return true;
                    }
                }
            }
            return false;
        }
        public static FormNen fromInt(final int nen) {
            for (final FormNen formNen : FormNen.values()) {
                if (formNen._intval == nen || formNen._intval2 == nen) {
                    return formNen;
                }
            }
            return null;
        }
    }

    private static abstract class FormInfo {
        final Param _param;
        FormNen _formNenYou; // 3 or 4 or 6年用
        String _formName;
        String _formNameSetOrigin;
        FormNen _formNen;

        /** 京都用フォーム使用ならtrue */
        protected boolean _isKyotoForm;
        protected boolean _isKumamotoForm;  // 熊本用フォーム使用

        protected static enum ShokenType {
            TYPE2, TYPE_FIELDAREA_REPEAT;
        }

        ShokenType _shokenType;
        ShokenType _shokenType5;

        protected boolean _is6nen = false;

        protected boolean _hankiNinteiTaiou;

        protected int _guardAddress1EndX;
        protected boolean _useGradeMultiPage;

        FormInfo(final Param param) {
            _param = param;
        }

        private boolean isShowFormat2(final PrintData printData) {
            return _param._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama) || printData._isKyoto || printData._isMiekenForm || _param._z010.in(Z010Info.Fukuiken, Z010Info.Sakae) || _param._z010.isKaichi() || "1".equals(printData._tyousasyo2020);
        }

        protected static boolean hasForm(final Param param, final Vrw32alp svf, final String formname) {
            boolean hasForm = false;
            String path = null;
            try {
                path = svf.getPath(formname);
                if (null != path) {
                    final File formFile = new File(path);
                    if (formFile.exists()) {
                        hasForm = new SvfForm(formFile).readFile();
                    }
                }
            } catch (Throwable t) {
            }
            if (param._isOutputDebug) {
                log.info(" hasForm " + formname + " (path = " + path + ") = " + hasForm);
            }
            return hasForm;
        }

        protected static FormNen setFormNenYou(final PrintData printData, final Param param) {

            final Collection<Title> gradeTitles = printData.getGradeTitlesForForm(param);
            if (param._isOutputDebug) {
                log.info(Util.debugCollectionToStr(" gradeTitles ", gradeTitles, ", "));
            }

            final FormNen nenyoForm = FormNen.fromInt(printData._nenyoform);
            FormNen formNenYou;
            if ("1".equals(printData._tyousasyo2020)) {
                formNenYou = nenyoForm;
            } else {
                if (!StringUtils.isBlank(printData.getParameter(Parameter.FORM6))) {
                    if (FormNen._3 == nenyoForm) {
                        formNenYou = FormNen._3;
                    } else if (FormNen._6 == nenyoForm) {
                        formNenYou = FormNen._6;
                    } else {
                        formNenYou = FormNen._4;
                    }
                } else if (printData._isChuKouIkkan && !(param._z010.in(Z010Info.Bunkyo, Z010Info.Sundaikoufu, Z010Info.Sapporo, Z010Info.TamagawaSei, Z010Info.Meikei)) && !"1".equals(printData.property(Property.tyousashoIkkanNotUseDefault4YearForm))) { // (param._isChiben || param._isKyoai || param._isKaijyo || param._isNishiyama || param._isRakunan || param._isTosa || param._isChiyodaKudan || param._isOsakatoin)) { //
                    formNenYou = FormNen._4;

                } else if (4 < gradeTitles.size()) {
                    formNenYou = FormNen._6;

                } else if (4 == gradeTitles.size()) {
                    formNenYou = FormNen._4;

                } else {
                    if (FormNen._3 == nenyoForm) {
                        formNenYou = FormNen._3;
                    } else if (FormNen._6 == nenyoForm) {
                        formNenYou = FormNen._6;
                    } else {
                        formNenYou = FormNen._4;
                    }
                }
                if (FormNen._3 == formNenYou && param._z010.in(Z010Info.Mieken)) {
                    formNenYou = FormNen._4;
                }
            }
            if (null == formNenYou) {
                throw new IllegalArgumentException(" formNenYou is null.");
            }
            return formNenYou;
        }

        public abstract void setSvfForm(final Vrw32alp svf, final String _formName, final IForm iform, final Param param);

        protected static void addSlashLineOfField(final SvfForm svfForm, final String fieldname) {
            addSlashLineOfField(svfForm, fieldname, fieldname);
        }

        protected static void addSlashLineOfField(final SvfForm svfForm, final String fieldnameRightUpper, final String fieldnameLeftLower) {
            final SvfForm.Field fieldRU = svfForm.getField(fieldnameRightUpper);
            final SvfForm.Field fieldLL = svfForm.getField(fieldnameLeftLower);
            if (null == fieldRU || null == fieldLL) {
                log.warn(" no field : " + fieldnameRightUpper + " OR " + fieldnameLeftLower);
            } else {
                final Line upperLine = svfForm.getNearestUpperLine(fieldRU._position);
                final Line rightLine = svfForm.getNearestRightLine(fieldRU._position);
                final Line leftLine = svfForm.getNearestLeftLine(fieldLL._position);
                final Line lowerLine = svfForm.getNearestLowerLine(fieldLL._position);
                // 右上、左下
                final Line slashLine = new Line(true, LineKind.SOLID, LineWidth.THIN, pxy(rightLine._end, upperLine._end), pxy(leftLine._end, lowerLine._start));
                log.info(" add slash " + fieldnameRightUpper + ", " + fieldnameLeftLower + " line = " + slashLine);
                svfForm.addLine(slashLine);
            }
        }

        protected static SvfForm.Point pt(final int x, final int y) {
            return new SvfForm.Point(x, y);
        }

        protected static SvfForm.Point pxy(final SvfForm.Point xp, final SvfForm.Point yp) {
            return pt(xp._x, yp._y);
        }

        /**
         * 繰り返しフィールド用斜線追加
         * @param svfForm
         * @param fieldname
         * @param n
         */
        protected static void addSlashLineOfFieldn(final Param param, final SvfForm svfForm, final String fieldname, final String n) {
            addSlashLineOfFieldn(param, svfForm, fieldname, fieldname, n);
        }

        protected static void addSlashLineOfFieldn(final Param param, final SvfForm svfForm, final String fieldnameRightUpper, final String fieldnameLeftLower, final String n) {
            final SvfForm.Field fieldRU = svfForm.getField(fieldnameRightUpper);
            final SvfForm.Field fieldLL = svfForm.getField(fieldnameLeftLower);
            if (null == fieldRU || null == fieldLL) {
                log.warn(" no field : " + fieldnameRightUpper + " OR " + fieldnameLeftLower);
            } else if (StringUtils.isBlank(fieldRU._repeatConfig._repeatNo) || null == svfForm.getRepeat(fieldRU._repeatConfig._repeatNo)) {
                log.warn(" no repeat : " + fieldRU._repeatConfig._repeatNo + " (" + fieldRU._fieldname + ")");
            } else {
                final Repeat repeat = svfForm.getRepeat(fieldRU._repeatConfig._repeatNo);
                if (repeat._direction == 1) {
                    // 横繰り返し
                    final int in = Integer.parseInt(n);
                    if ((in <= 0 || repeat._count < in)) {
                        if (param._isOutputDebug) {
                            log.warn(" out of repeat count : " + n + " / " + repeat._count + ", repeat = " + repeat._no + " (" + fieldRU._fieldname + ") ");
                        }
                    } else {
                        final int addX = repeat._pitch * (in - 1);
                        final Line upperLine = svfForm.getNearestUpperLine(fieldRU._position).addX(addX);
                        final Line rightLine = svfForm.getNearestRightLine(fieldRU._position).addX(addX);
                        final Line leftLine = svfForm.getNearestLeftLine(fieldLL._position).addX(addX);
                        final Line lowerLine = svfForm.getNearestLowerLine(fieldLL._position).addX(addX);
                        // 右上、左下
                        final Line slashLine = new Line(true, LineKind.SOLID, LineWidth.THIN, pt(rightLine._end._x, upperLine._end._y), pt(leftLine._end._x, lowerLine._start._y));
                        log.info(" add slash n " + fieldnameRightUpper + " (" + in + ") line = " + slashLine);
                        svfForm.addLine(slashLine);
                    }
                }
            }
        }

        protected void resizeStampImage(final PrintData printData, final SvfForm svfForm, final String fieldname, final String stampSizeMm, final String stampPositionXmm, final String stampPositionYmm) {

            final SvfForm.ImageField image = svfForm.getImageField(fieldname);
            if (null == image) {
                log.info(" no image : " + fieldname);
                return;
            }
            final int x = image._point._x;
            final int y = image._point._y;
            final int endX = image._endX;
            final int endY = y + image._height;
            final int l = NumberUtils.isNumber(stampSizeMm) ? Util.mmToDot(stampSizeMm) : image._height;
            final int newX;
            final int newEndX;
            if (NumberUtils.isNumber(stampPositionXmm)) {
                newX = Util.mmToDot(stampPositionXmm);
                newEndX = newX + l;
            } else {
                final int centerX = (x + endX) / 2;
                newX = centerX - l / 2;
                newEndX = centerX + l / 2;
            }
            final int newY;
            if (NumberUtils.isNumber(stampPositionYmm)) {
                newY = Util.mmToDot(stampPositionYmm);
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
            log.info("move " + fieldname + "\n"
                    + "    (x=" + x + "(" + xmm + "mm), y=" + y + "(" + ymm + "mm), len = " + image._height + "(" + hmm + "mm)) \n"
                    + " to (x=" + newX + "(" + newXmm + "mm), y=" + newY + "(" + newYmm + "mm), len = " + l + "(" + newHmm + "mm))"
                    );
        }

        protected void modifyFormHankiNintei(final SvfForm svfForm) {
            final List<KoteiMoji> koteiMojiKamokuList = svfForm.getKoteiMojiListWithText("科　　目");
            final List<KoteiMoji> kotei2List = svfForm.getKoteiMojiListWithText("修得単位数の計");
            final List<KoteiMoji> koteiMojiHyoteiList = svfForm.getKoteiMojiListWithText("評　定");

            for (final KoteiMoji koteiMojiKamoku : koteiMojiKamokuList) {

                final Line koteiKamokuUpperLine = svfForm.getNearestUpperLine(koteiMojiKamoku._point); // "科　　目"の上線

                KoteiMoji nearestRightKoteiMojiShutokuTannisuNoKei = null;
                for (final KoteiMoji koteiMojiShutokuTannisuNoKei : kotei2List) {
                    if (koteiMojiShutokuTannisuNoKei._point._x < koteiMojiKamoku._point._x) {
                        continue;
                    }
                    if (null == nearestRightKoteiMojiShutokuTannisuNoKei || Point.abs(koteiMojiKamoku._point, nearestRightKoteiMojiShutokuTannisuNoKei._point) > Point.abs(koteiMojiKamoku._point, koteiMojiShutokuTannisuNoKei._point)) {
                        nearestRightKoteiMojiShutokuTannisuNoKei = koteiMojiShutokuTannisuNoKei;
                    }
                }
                Line shutokuTannisuNoKeiLeftLine = null;
                if (null != nearestRightKoteiMojiShutokuTannisuNoKei) {
                    shutokuTannisuNoKeiLeftLine = svfForm.getNearestLeftLine(nearestRightKoteiMojiShutokuTannisuNoKei._point);
                }
                if (null != koteiKamokuUpperLine && null != shutokuTannisuNoKeiLeftLine) {
                    final Line hLine = new Line(koteiKamokuUpperLine._end, pt(shutokuTannisuNoKeiLeftLine._start._x, koteiKamokuUpperLine._end._y)); // 前|後 の上の線
                    svfForm.addLine(hLine);

                    for (final KoteiMoji koteiMojiHyotei : koteiMojiHyoteiList) {
                        if (hLine._start._x <= koteiMojiHyotei._point._x && koteiMojiHyotei._point._x <= hLine._end._x) {

                            svfForm.move(koteiMojiHyotei, koteiMojiHyotei.addX(30).addY(-10).setMojiPoint(80)); // Xに30、Yに-10移動して8ポイント

                            final Line line = svfForm.getNearestLowerLine(koteiMojiHyotei._point);
                            final Line upLine = line.addY(-35); // "評　定"の下線
                            svfForm.move(line, upLine);

                            final List<Line> vLines = svfForm.getCrossedLineList(hLine);
                            final List<Integer> xs = new ArrayList<Integer>();
                            int vLineBottom = 0;
                            for (final Line vLine : vLines) {
                                if (upLine._start._y < vLine._start._y) {
                                    svfForm.move(vLine, new Line(pt(vLine._start._x, upLine._start._y), vLine._end));
                                }
                                xs.add(vLine._start._x);
                                vLineBottom = vLine._end._y;
                            }
                            Collections.sort(xs);
                            for (int i = 0; i < xs.size() - 1; i++) {
                                final Integer x1 = xs.get(i);
                                final Integer x2 = xs.get(i + 1);
                                final int nx = x1.intValue() + (x2.intValue() - x1.intValue()) / 2;

                                svfForm.addLine(new Line(pt(nx, koteiKamokuUpperLine._end._y), pt(nx, vLineBottom)));

                                int dx1 = 0;
                                int dx2 = 0;
                                int ystart = 0;
                                int charPoint = 0;
                                if (_formNenYou == FormNen._4) {
                                    dx1 = -45;
                                    dx2 = 15;
                                    ystart = 35;
                                    charPoint = 60;
                                } else if (_formNenYou == FormNen._6) {
                                    dx1 = -35;
                                    dx2 = 5;
                                    ystart = 35;
                                    charPoint = 60;
                                } else if (_formNenYou == FormNen._3) {
                                    dx1 = -60;
                                    dx2 = 15;
                                    ystart = 30;
                                    charPoint = 80;
                                }
                                svfForm.addKoteiMoji(new KoteiMoji("前", pt(nx + dx1, koteiKamokuUpperLine._end._y + ystart), charPoint).setFText(true));
                                svfForm.addKoteiMoji(new KoteiMoji("後", pt(nx + dx2, koteiKamokuUpperLine._end._y + ystart), charPoint).setFText(true));
                            }
                        }
                    }
                }
            }
        }

        protected ShokenType shokenType5() {
            return Util.defObject(_shokenType5, _shokenType);
        }
    }

    public static class SqlStudyrec extends CommonSqlStudyrec { // アクセス指定子変更しないで!消さないで!

    }

    private static class TitlePage {
        final int _index;
        final List<Title> _titleList = new ArrayList<Title>();

        TitlePage(final int index) {
            _index = index;
        }

        public String toString() {
            return "TitlePage(" + _index + ", " + _titleList + ")";
        }

        public static List<TitlePage> getTitlePageList(final Param param, final List<Title> titleList, final int formNen) {
            final List<TitlePage> titlePageList = new ArrayList<TitlePage>();
            TitlePage current = null;
            for (final Title title : titleList) {
                if (title._pos < 0 && title._seisekiPos < 0) {
                    log.info(" skip title : " + title);
                    continue;
                }
                if (null == current || current._titleList.size() >= formNen) {
                    current = new TitlePage(titlePageList.size());
                    titlePageList.add(current);
                }
                current._titleList.add(title);
                title._pos -= current._index * formNen;
            }
            if (param._isOutputDebug) {
                log.info(" titlePageList = " + titlePageList);
            }
            return titlePageList;
        }
    }

    /**
     * 生徒ごとの印刷データ
     */
    private static class PrintData extends CommonPrintData {

        static final String SOGOTEKI_NA_GAKUSHU_TANKYU_NO_JIKAN = "総合的な学習（探究）の時間";
        static final String SOGOTEKI_NA_GAKUSHU_NO_JIKAN = "総合的な学習の時間";
        static final String SOGOTEKI_NA_TANKYU_NO_JIKAN = "総合的な探究の時間";

        final SchoolInfo _schoolInfo;
        List<Title> _gradeTitlesForForm = Collections.emptyList();
        Map<String, HexamEntremarkDat> _hexamEntremarkDatMap = new HashMap<String, HexamEntremarkDat>();
        Map<Year, Map<Seq, String>> _yearHexamEntremarkTrainRefDatMap = new TreeMap<Year, Map<Seq, String>>();
        String _hexamEntremarkRemarkHdatRemark = null;
        Map<String, String> _hexamEntremarkHdat = new HashMap<String, String>();
        MedexamDetDat _medexamDetDat = new MedexamDetDat(null); // 健診
        Map _geneviewmbrMap = new HashMap(); // 概評人数
        List<StudyrecDat> _studyrecDataShushokuyou = Collections.emptyList();
        List<StudyrecDat> _suraStudyrecData1 = null;
        Map<String, String> _yearKaikinDatTextMap = Collections.emptyMap();
        String _hexamEntremarkHdatCommentexACd;
        String _hexamEntremarkHdatMark;
        String _assessLevel;
        String _assessMark;
        String _bikoKoteiText = null;
        String _kisaiStampImagePath;
        String _toeflScore;
        String _tajuHeikinRank; // 多重平均指定時の評定平均席次
        String _tajuHeikinRankAvg; // 多重平均指定時の評定平均

        // 就職用所見
        private String _jobHuntAbsence = null;
        private String _jobHuntRec = null;
        private String _jobHuntHealthremark = null;
        private String _jobHuntRecommend = null;

        int _printProcess = 0;

        // 住所フィールド位置情報
        protected KNJSvfFieldInfo _fieldInfoAddr1;
        protected KNJSvfFieldInfo _fieldInfoAddr2;

        protected Map<String, Object> _outputFuhakkouResult;
        protected boolean _isFuhakkou;

        protected FormRecordPart formRecord = null;
        private String _provSemesterName;

        private List<Map<String, String>> _vNameMstE048List = Collections.emptyList();
        private StudyrecDat _studyrecDatTotal;
        private StudyrecDat _studyrecDatTotalCredit;
        public List<StudyrecDat> _studyrecDatAbroadList = new ArrayList<StudyrecDat>(); // 進学用 最大1個、就職用 年度毎
        boolean _abroadCreditIsSura;
        private List<HyoteiHeikin> _hyoteiHeikinList;
        private boolean _isA4SogakuShokenActSlash;
        private boolean _isA4SogakuShokenValSlash;

        Map<String, String> _suramap;  // 大検・前籍項・総合・留学・教科総計の単位数格納用
        List<Map<String, String>> _form1SuraList = Collections.emptyList();

        List<StudyrecDat> _d081List;
        List<StudyrecDat> _e065List;

        public PrintData(
                final String schregno,
                final String year,
                final String semes,
                final String date,
                final String staffCd,
                final String kanji,
                final String comment,
                final String os,
                final String certifNumber,
                final Map paramap,
                final int output,
                final Param param) {
            super(param, paramap, output, schregno, year, semes, date, staffCd, kanji, comment, os, certifNumber);

            _schoolInfo = new SchoolInfo(_staffCd, getParameter(Parameter.certifSchoolOnly));
        }

        HexamEntremarkDat getHexamEntremarkDat(final Param param, final Collection<Title> titles, final String year) {
            if (null == year) {
                return null;
            }
            HexamEntremarkDat dat = null;
            for (final Map.Entry<String, HexamEntremarkDat> p : _hexamEntremarkDatMap.entrySet()) {
                final Title t = Title.getTitle(param, titles, p.getKey());
                if (null != t) {
                    if (year.equals(t._year)) {
                        dat = p.getValue();
                        break;
                    }
                    if (Util.toInt(year, -1) == 0 && Util.toInt(t._year, -1) == 0) {
                        dat = p.getValue();
                        break;
                    }
                }
            }
            return dat;
        }

        public boolean isNotOutputPrincipalName() {
            return paramapContains(Parameter.OUTPUT_PRINCIPAL) && !"1".equals(getParameter(Parameter.OUTPUT_PRINCIPAL));
        }

        public boolean isCsv() {
            return "csv".equals(getParameter(Parameter.cmd));
        }

        private boolean hasJiritsuKatsudouRecord(final Param param, final List<StudyrecDat> studyrecDatList) {
            boolean rtn = false;
            for (final StudyrecDat sd : studyrecDatList) {
                final String keySubclasscd = sd.keySubclasscd(param);
                if (_vNameMstE065List.contains(keySubclasscd)) {
                    rtn = true;
                    break;
                }
            }
            return rtn;
        }

        public List<String> getNotNullCertifRemark123List() {
            final List<String> list = new ArrayList<String>();
            final String replaceText = "［仮評定学期名］";
            for (int i = 1; i <= 3; i++) {
                if (_schoolInfo.certifSchoolRemark[i] != null) {
                    if (_schoolInfo.certifSchoolRemark[i].indexOf(replaceText) > 0) {
                        // replaceTextを仮評定の学科名に置き換える
                        if (null == _provSemesterName) {
                            // もし仮評定がなければその行を印刷しない
                        } else {
                            list.add(StringUtils.replace(_schoolInfo.certifSchoolRemark[i], replaceText, _provSemesterName));
                        }
                    } else {
                        list.add(_schoolInfo.certifSchoolRemark[i]);
                    }
                }
            }
            return list;
        }

        public static String getHexamEntremarkHdatCommentexACd(final DB2UDB db2, final Param param, final PrintData printData) {
            final String psKey = "PS_HEXAM_ENTREMARK_HDAT_COMMENTEX_A_CD";
            if (null == param.getPs(psKey)) {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT COMMENTEX_A_CD ");
                sql.append(" FROM ");
                if (param._isPrintGrd) {
                    sql.append(" GRD_HEXAM_ENTREMARK_HDAT WHERE SCHREGNO = ? ");
                } else {
                    if ("1".equals(printData.getParameter(Parameter.tyousasyo2020)) && "1".equals(printData.property(Property.tyousasyo_shokenTable_Seq))) {
                        sql.append(" HEXAM_ENTREMARK_SEQ_HDAT WHERE SCHREGNO = ? ");
                        if (PRGID_KNJG010.equals(param._prgid)) {
                            sql.append(" AND PATTERN_SEQ = '1' ");
                        } else {
                            sql.append(" AND PATTERN_SEQ = '" + printData.getParameter(Parameter.SELECT_PATTERN)  + "' ");
                        }
                    } else {
                        sql.append(" HEXAM_ENTREMARK_HDAT WHERE SCHREGNO = ? ");
                    }
                }
                param.setPs(db2, psKey, sql.toString());
            }
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {printData._schregno}));
        }

        /**
         * (就職用で使用する)代替科目備考リストを得る
         * @return (就職用で使用する)代替科目備考リスト
         */
        public List<String> getSubstitutionNotices(final Param param) {
            final List<String> rtn = new ArrayList<String>();
            if (param._isShusyokuyouKinkiToitsu2) {
                return rtn;
            }
            for (int i = 0; i < _studyrecSubstNote._substitutionNoticeAllList.size() - 1; i++) {
                for (int j = i + 1; j < _studyrecSubstNote._substitutionNoticeAllList.size(); j++) {
                    final StudyrecSubstitutionNote.SubstitutionInfo sii = _studyrecSubstNote._substitutionNoticeAllList.get(i);
                    final StudyrecSubstitutionNote.SubstitutionInfo sij = _studyrecSubstNote._substitutionNoticeAllList.get(j);
                    if (null != sii._substitutionSubclasscd && null != sij._substitutionSubclasscd && sii._substitutionSubclasscd.compareTo(sij._substitutionSubclasscd) > 0) {
                        // swap
                        _studyrecSubstNote._substitutionNoticeAllList.set(i, sij);
                        _studyrecSubstNote._substitutionNoticeAllList.set(j, sii);
                    }
                }
            }
            for (final StudyrecSubstitutionNote.SubstitutionInfo info : _studyrecSubstNote._substitutionNoticeAllList) {
                rtn.add(info.toText(true, false, param._daitaiTextNotPrintIchibu, param._printEachSubclassCreditSubstZenbu));
            }
            return rtn;
        }

        public boolean substitutionNotice90IsBlank(final String debug, final Param param) {
            return substitutionNotice90IsBlank(debug, param, null);
        }

        public boolean substitutionNotice90IsBlank(final String debug, final Param param, final String year) {
            return _studyrecSubstNote.getDaitaiBiko90(debug, year, false, null, param).isEmpty();
        }

        public static boolean isNoSogoCredit(final Map suramap) {
            final String sogoCredit = (String) suramap.get(SqlStudyrec.sogo);
            final boolean isNoSogoCredit = Util.toInt(sogoCredit, 0) == 0;
            return isNoSogoCredit;
        }

        /** 成績の"総合的な学習の時間"の修得単位数の計は"/"か */
        protected boolean sogoIsSuraShingakuyou(final String debug, final Param param, final String year) {
            final boolean sogoIsSura;
            if (param._isNotOutputSogotekinaGakushunoJikanTanni) {
                sogoIsSura = true;
                if (param._isOutputDebug) {
                    log.info(" isNotOutputSogotekinaGakushunoJikanTanni");
                }
            } else if (param._isNotOutputSogotekinaGakushunoJikanTanniIfReplaceSubstitute && !substitutionNotice90IsBlank(debug, param, year)) {
                // 総合的な学習の時間を代替する場合
                sogoIsSura = true;
                if (param._isOutputDebug) {
                    log.info(" isNotOutputSogotekinaGakushunoJikanTanniIfReplaceSubstitute ");
                }
            } else {
                sogoIsSura = false;
            }
            return sogoIsSura;
        }

        /** 成績の"総合的な学習の時間"の修得単位数の計は"/"か */
        protected boolean sogoIsSuraShushokuyou(final String debug, final Param param, final String year) {
            if (param._z010.in(Z010Info.Miyagiken) && !substitutionNotice90IsBlank(debug, param, year)) {
                return true;
            }
            return sogoIsSuraShingakuyou(debug, param, year);
        }

        private Map<String, String> shushokuYouSogoCreditYear() {
            final Map<String, String> sogo = new HashMap<String, String>();
            for (final StudyrecDat s : _studyrecDataShushokuyou) {
                if (SqlStudyrec.sogo.equalsIgnoreCase(s._classname)) { // TODO: classname
                    for (final Grades g : s.getNotDropGradesList()) {
                        if (null != g._year && null != g._credit) {
                            sogo.put(g._year, g._credit);
                        }
                    }
                }
            }
            return sogo;
        }

        private Map<String, String> shushokuYouRyugakuCreditYear() {
            final Map<String, String> ryugaku = new HashMap<String, String>();
            for (final StudyrecDat s : _studyrecDatAbroadList) {
                for (final Grades g : s.getNotDropGradesList()) {
                    if (null != g._year && null != g._credit) {
                        ryugaku.put(g._year, g._credit);
                    }
                }
            }
            return ryugaku;
        }

        private static List<StudyrecDat> getStudyrecDataShingaku(final DB2UDB db2, final PrintData printData, final Param param) {

            final List<Map<String, String>> src1;
            long startTime = System.currentTimeMillis();
            if (printData._useStudyrecSql2) {
                final Tuple<List<Map<String, String>>, List<Map<String, String>>> rowListTuple = printData._sqlStudyrec.pre_sql2(db2, printData, param);
                printData._tStudyrec = rowListTuple._first;
                src1 = rowListTuple._second;
            } else {
                if (null != printData._e014Subclasscd) {
                    if (null != param.getPs(ps1Key)) {
                        DbUtils.closeQuietly(param.getPs(ps1Key));
                    }
                    final List<String> sqlLines = printData._sqlStudyrec.pre_sql(printData, param);
                    if (param._isOutputDebugQuery) {
                        log.fatal("学習記録データSQL(e014) = " + Util.debugSqlLines("", sqlLines));
                    }
                    param.setPs(db2, ps1Key, Util.mkString(sqlLines, ""));
                }
                src1 = KnjDbUtils.query(db2, param.getPs(ps1Key), null);
            }
            long elapsed = System.currentTimeMillis() - startTime;
            BigDecimal elapsedSec = new BigDecimal(elapsed / 1000.0).setScale(2, BigDecimal.ROUND_HALF_UP);
            if (param._isOutputDebugTime) {
                log.info(" elapsed = " + elapsedSec);
            }
            param._elapsedQueryTimeList.add(elapsedSec);

            final List<StudyrecDat> studyrecDatList = new ArrayList<StudyrecDat>();
            for (final Map<String, String> row : src1) {
                final String recordFlg = getString(row, "RECORD_FLG");
                String classcd = getString(row, "CLASSCD");
                String subclasscd = getString(row, "SUBCLASSCD");
                String classname = getString(row, "CLASSNAME");
                String subclassname = getString(row, "SUBCLASSNAME");
                final String year = getString(row, "YEAR");
                final String grades = getString(row, "GRADES");
                final String avgGrades = getString(row, "AVG_GRADES");
                String credit = getString(row, "CREDIT");
                String gradeCredit = getString(row, "GRADE_CREDIT");
                String compCredit = getString(row, "COMP_CREDIT");
                final String specialDiv = getString(row, "SPECIALDIV");
                final String validFlg = getString(row, "VALID_FLG");
                final String provFlg = printData._useStudyrecSql2 ? getString(row, "PROV_FLG") : null;
                final String provSemester = getString(row, "PROV_SEMESTER");
                final String d065Flg = getString(row, "D065FLG");
                final String schoolcd = getString(row, "SCHOOLCD");
                String schoolKind;
                String curriculumCd;
                if ("1".equals(param._useCurriculumcd)) {
                    schoolKind = getString(row, "SCHOOL_KIND");
                    curriculumCd = getString(row, "CURRICULUM_CD");
                } else {
                    schoolKind = null;
                    curriculumCd = null;
                }

                StudyrecDat studyrecDat = null;

                StudyrecDat.Kind kind = StudyrecDat.Kind.None;
                boolean koteiTanniToAbroad = false;
                if (SqlStudyrec.sogo.equals(classname)) {
                    kind = StudyrecDat.Kind.Sogo;
                    if (param._setSogakuKoteiTanni) {
                        final Title title = Title.getTitle(param, printData.titleValues(), year);
                        if (null == title || !NumberUtils.isDigits(title._annual)) {
                            if (param._isOutputDebug) {
                                log.warn(" koteiTanni sogaku : unknown year = " + year + " / annual = " + (null == title ? null : title._annual));
                            }
                        } else {
                            final BigDecimal koteiTanni = param._sogakuKoteiTanniMap.get(Integer.parseInt(title._annual));
                            if (null == koteiTanni) {
                                if (param._isOutputDebug) {
                                    log.warn(" koteiTanni null : annual = " + title._annual + " / " + param._sogakuKoteiTanniMap.keySet());
                                }
                            } else {
                                if (Util.toDouble(credit, -1) >= 0) {
                                    credit = koteiTanni.toString();
                                }
                                if (Util.toDouble(gradeCredit, -1) >= 0) {
                                    gradeCredit = koteiTanni.toString();
                                }
                                if (Util.toDouble(compCredit, -1) >= 0) {
                                    compCredit = koteiTanni.toString();
                                }
                                if (param._isOutputDebug) {
                                    log.info(" set tanni : credit = " + credit + ", gradeCredit = " + gradeCredit + ", compCredit = " + compCredit);
                                }
                            }
                        }

                        if (printData._abroadYears.contains(year)) {
                            classcd = SqlStudyrec.abroad;
                            classname = SqlStudyrec.abroad;
                            schoolKind = SqlStudyrec.abroad;
                            curriculumCd = SqlStudyrec.abroad;
                            subclasscd = SqlStudyrec.abroad;
                            subclassname = SqlStudyrec.abroad;

                            kind = StudyrecDat.Kind.Abroad;
                            koteiTanniToAbroad = true;
                        }
                        if (param._isOutputDebug) {
                            log.info(" abroad contains : " + year + "? " + printData._abroadYears.contains(year) + " => " + classname + " / " + koteiTanniToAbroad);
                        }
                    }
                } else if (SqlStudyrec.total.equals(classname)) {
                    kind = StudyrecDat.Kind.Total;
                } else if (SqlStudyrec.abroad.equalsIgnoreCase(classname)) {
                    kind = StudyrecDat.Kind.Abroad;
                }

                for (final StudyrecDat srd : studyrecDatList) {
                    if (param.isSameClasscd(classcd, schoolKind, srd._classcd, srd._schoolKind) && (printData._isSubclassOrderNotContainCurriculumcd || (null == curriculumCd && null == srd._curriculumCd || null != curriculumCd && curriculumCd.equals(srd._curriculumCd))) && subclasscd.equals(srd._subclasscd)) {
                        studyrecDat = srd;
                        break;
                    }
                }

                if (null == studyrecDat) {
                    studyrecDat = new StudyrecDat(kind, printData, "", classcd, schoolKind, curriculumCd, subclasscd, classname, subclassname, avgGrades, specialDiv, null);
                    studyrecDatList.add(studyrecDat);
                }
                if (printData._useStudyrecSql2) {
                    studyrecDat._isE014 = param.getE014Name1List(printData._notUseE014).contains(subclasscd);
                }

                final Grades gradez = new Grades(recordFlg, year, grades, credit, gradeCredit, compCredit, validFlg, provFlg, provSemester, d065Flg, schoolcd, koteiTanniToAbroad);
                if (param._isOutputDebugSeiseki) {
                    log.fatal(" strec " + subclasscd + " grades = " + gradez);
                }
                studyrecDat._gradesList.add(gradez);

                if (row.containsKey("ZENKI_GRADES")) {
                    gradez._zenkiGrades = getString(row, "ZENKI_GRADES");
                }
                if (row.containsKey("KOUKI_GRADES")) {
                    gradez._koukiGrades = getString(row, "KOUKI_GRADES");
                }
            }
            for (final Iterator<StudyrecDat> it = studyrecDatList.iterator(); it.hasNext();) {
                final StudyrecDat studyrecDat = it.next();
                // TODO:
                if (studyrecDat._kind == StudyrecDat.Kind.Total) {
                    printData._studyrecDatTotal = studyrecDat;
                    it.remove();
                } else if (studyrecDat._kind == StudyrecDat.Kind.Abroad) {
                    if (printData._output == PrintData._shingakuYou) {
                        printData._studyrecDatAbroadList.add(studyrecDat);
                    }
                    it.remove();
                }
            }
            return studyrecDatList;
        }

        public String getPrintTotalCredit() {
            for (final Map<String, String> row : _form1SuraList) {
                if (row.containsKey(TOTAL_FLG)) {
                    return defstr(row.get(FormKNJE070_1.TOTAL_CREDIT_DOTfield), row.get(FormKNJE070_1.TOTAL_CREDITfield));
                }
            }
            return null;
        }

        public String getTotalAvgGrades() {
            String totalAvgGrades = null;
            if (null != _studyrecDatTotal) {
                totalAvgGrades = _studyrecDatTotal._avgGrades;
            }
            return totalAvgGrades;
        }

        public Tuple<String, String> getAssessLevelAndAssessMark(final DB2UDB db2, final Param param, final String totalAvgGrades) {
            if (null == totalAvgGrades) {
                return null;
            }

            final String pskey = "PS_ASSESS";
            final Object[] arg;
            if ("1".equals(property(Property.useAssessCourseMst))) {
                if (null == _personInfo._coursecd || null == _personInfo._majorcd || null == _personInfo._coursecode) {
                    log.info("在籍データコース無し : " + _personInfo._coursecd + "|" + _personInfo._majorcd + "|" + _personInfo._coursecode);
                    return null;
//                } else {
//                    log.info("在籍データコース : " + _coursecd + "|" + _majorcd + "|" + _coursecode);
                }

                if (null == param.getPs(pskey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ASSESSLEVEL, ASSESSMARK ");
                    stb.append(" FROM ASSESS_COURSE_MST ");
                    stb.append(" WHERE  ASSESSCD = '4' AND ? BETWEEN ASSESSLOW AND ASSESSHIGH  ");
                    stb.append("    AND COURSECD = ? AND MAJORCD = ? AND COURSECODE = ?  ");

                    if (param._isOutputDebugQuery) {
                        log.debug(" assess_mst sql = " + stb.toString());
                    }
                    param.setPs(db2, pskey, stb.toString());
                }

                arg = new Object[] {new BigDecimal(totalAvgGrades), _personInfo._coursecd, _personInfo._majorcd, _personInfo._coursecode};

            } else {
                if (null == param.getPs(pskey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ASSESSLEVEL, ASSESSMARK ");
                    stb.append(" FROM ASSESS_MST ");
                    stb.append(" WHERE  ASSESSCD='4' AND ? BETWEEN ASSESSLOW AND ASSESSHIGH  ");

                    if (param._isOutputDebugQuery) {
                        log.debug(" assess_mst sql = " + stb.toString());
                    }
                    param.setPs(db2, pskey, stb.toString());
                }

                arg = new Object[] {new BigDecimal(totalAvgGrades)};

            }
            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(pskey), arg));
            final String assesslevel = KnjDbUtils.getString(row, "ASSESSLEVEL");
            final String assessmark = KnjDbUtils.getString(row, "ASSESSMARK");

            return Tuple.of(assesslevel, assessmark);
        }

        public boolean isRemarkOnly(final Param param) {
            return "1".equals(getParameter(Parameter.remarkOnly)) && !param._z010.in(Z010Info.Sundaikoufu);
        }

        public boolean notUseStudyrec(final Param param) {
            return isRemarkOnly(param) || "1".equals(getParameter(Parameter.certifSchoolOnly));
        }

        public void load(final DB2UDB db2, final Param param) {
            super.load(db2, param);

            setGradeTitle(db2, param);
            if (!"1".equals(getParameter(Parameter.certifSchoolOnly))) {
                setPersonInfoMap(db2, param);
            }
            getExamMap(db2, param);
            _studyrecSubstNote = new StudyrecSubstitutionNote(this);
            if (isRemarkOnly(param)) {
            } else {
                if (_isFuhakkou) {
                } else {
                    _attendMap = AttendrecDat.getAttendMap(db2, param, this);
                    getGeneviewmbrMap(db2, param);
                    getMedexamMap(db2, param);
                }
            }

            try {
                if ("1".equals(getParameter(Parameter.certifSchoolOnly))) {
                    _studyrecData1 = new ArrayList<StudyrecDat>();
                } else {
                    if (!_useStudyrecSql2) {
                        final List<String> sql = _sqlStudyrec.pre_sql(this, param);
                        if (null != param.getPs(ps1Key)) {
                            DbUtils.closeQuietly(param.getPs(ps1Key));
                        }
                        if (param._isOutputDebugQuery) {
                            log.fatal("学習記録データSQL = " + Util.debugSqlLines("", sql));
                        }
                        param.setPs(db2, ps1Key, Util.mkString(sql, ""));

                        preprocessStudyrecDat(db2, param);
                    }

                    _studyrecData1 = getStudyrecDataShingaku(db2, this, param);
                }

                if (!_notUseDaitai) {
                    final long start = System.currentTimeMillis();
                    _studyrecSubstNote.setCreditMst(db2, this, param);
                    final long start2 = System.currentTimeMillis();
                    _studyrecSubstNote.setSubstitutionSubclassNote(db2, param, this);
                    final long end = System.currentTimeMillis();
                    if (param._isOutputDebugSubst) {
                        final BigDecimal elapsed1 = new BigDecimal(end - start).divide(new BigDecimal(1000), 1, BigDecimal.ROUND_HALF_UP);
                        final BigDecimal elapsed2 = new BigDecimal(end - start2).divide(new BigDecimal(1000), 1, BigDecimal.ROUND_HALF_UP);
                        log.info(" subst process elapsed = " + elapsed1 + " [sec] (" + elapsed2 + " [sec])");
                    }
                }

                if (notUseStudyrec(param)) {
                    _studyrecData1 = Collections.emptyList();
                }

            } catch (Exception e1) {
                log.error("Exception", e1);
            }

            if (notUseStudyrec(param)) {
                // 所見のみ
            } else {
                if (_isFuhakkou) {
                } else {
                    final String totalAvgGrades = getTotalAvgGrades();
                    final Tuple<String, String> assessLevelAndAssessMark = getAssessLevelAndAssessMark(db2, param, totalAvgGrades);
                    if (null != assessLevelAndAssessMark) {
                        _assessLevel = assessLevelAndAssessMark._first;
                        _assessMark = assessLevelAndAssessMark._second;
                    }
                    if (param._isOutputDebugSeiseki) {
                        log.info(" assessLevel = " + _assessLevel + ", assessMark = " + _assessMark);
                    }
                    if (NumberUtils.isNumber(property(Property.useMaruA_avg))) {
                        if (NumberUtils.isNumber(totalAvgGrades)) {
                            _hexamEntremarkHdatMark = new BigDecimal(totalAvgGrades).compareTo(new BigDecimal(property(Property.useMaruA_avg))) >= 0 ? "○" : "　";
                        }
                    } else {
                        _hexamEntremarkHdatCommentexACd = getHexamEntremarkHdatCommentexACd(db2, param, this);
                        if ("1".equals(_hexamEntremarkHdatCommentexACd) && (Util.toInt(_assessLevel, -1) == 5 || "1".equals(property(Property.tyousasyoPrintMaruAigai)))) {
                            _hexamEntremarkHdatMark = "○";
                        } else {
                            _hexamEntremarkHdatMark = "  ";
                        }

                        if (param._z010.in(Z010Info.jyoto) && "1".equals(_hexamEntremarkHdatCommentexACd)) {
                            final String grade = KnjDbUtils.getString(_regdDat, "GRADE");
                            final String sql = " SELECT REMARK FROM HTRAINREMARK_TEMP_DAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' AND DATA_DIV = '20' AND PATTERN_CD = 'A' ";
                            _bikoKoteiText = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
                        }
                    }
                    if (param._isOutputDebugSeiseki) {
                        log.info(" useMaruA_avg = " + property(Property.useMaruA_avg) + ", totalAvgGrades = " + totalAvgGrades + ", mark = " + _hexamEntremarkHdatMark);
                    }
                }
            }

            if (isRemarkOnly(param)) {
                // 所見のみ
            } else {
                _schoolInfo.loadSchoolInfo(db2, param, _date, _ctrlYear, _year, getParameter(Parameter.CERTIFKIND), getParameter(Parameter.CERTIFKIND2), _tyousasyo2020, isGakunensei(param)); //学校名、校長名のセット
                if (_schoolInfo.hasHeaddata) {
                    _schoolInfo.isPrintSchoolRemark = isPrintSchoolRemark(param);
                    if (param._isOutputDebug) {
                        log.info(" isPrintSchoolRemark = " + _schoolInfo.isPrintSchoolRemark);
                    }
                }

                _provSemesterName = getProvSemesterName(db2, getGradesListForPrintSchoolRemark(param, _studyrecData1));

                if (param._tableInfo._hasSUBCLASS_DETAIL_DAT) {
                    _subclassDetailDatSeq006SubclasscdList = param.getSubclassD006(db2, _year);
                }
                if (param._z010.in(Z010Info.Hirokoku)) {
                    _vNameMstE048List = param.getE048List(db2, _year);
                }
            }

            if (null == _suraStudyrecData1) {
                final Tuple<List<StudyrecDat>, List<StudyrecDat>> suraStudyrecDataAndNewStudyrecData1 = setSuraStudyrecDataAndNewStudyrecData1(param, _studyrecData1);
                _suraStudyrecData1 = suraStudyrecDataAndNewStudyrecData1._first;
                _studyrecData1 = suraStudyrecDataAndNewStudyrecData1._second;
            }

            _hyoteiHeikinList = getHyoteiHeikinList(param);
            if (param._isOutputDebugSeiseki) {
                log.info(" hyoteiHeikinList = " + _hyoteiHeikinList);
            }
            _gradeTitlesForForm = getGradeTitlesForForm(param);
            if (param._isOutputDebug) {
                log.info(" gradeTitlesForForm = " + _gradeTitlesForForm);
            }

            _suramap = getSuraMap(param, _gradeTitlesForForm, _suraStudyrecData1);
            _form1SuraList = getSuraList(param, _suramap);

            if (_isKisaiPrintStamp) {
                _kisaiStampImagePath = getAttestInkanMap(db2, param, _staffCd, _date);
                if (param._isOutputDebug) {
                    log.info(" kisaiStampImagePath = " + _kisaiStampImagePath);
                }
            }

            if (paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(getParameter(Parameter.GVAL_CALC_CHECK)) && "1".equals(getParameter(Parameter.PRINT_AVG_RANK))) {
                final Map<String, String> baseDetailMst017 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT BASE_REMARK1 AS RANK, BASE_REMARK2 AS AVG FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = ? AND BASE_SEQ = '017' ", new Object[] {_schregno}));
                _tajuHeikinRank = KnjDbUtils.getString(baseDetailMst017, "RANK");
                _tajuHeikinRankAvg = KnjDbUtils.getString(baseDetailMst017, "AVG");
                if (param._isOutputDebug) {
                    log.info(" " + _schregno + " 多重平均 順位 = " + _tajuHeikinRank + ", 評定平均 = " + _tajuHeikinRankAvg + " / 表示評定平均 = " + getTotalAvgGrades());
                }
            }

            boolean _isA4SogakuShokenSlash = false;
            if ("1".equals(_tyousasyo2020)) {
                _isA4SogakuShokenSlash = sogoIsSuraShingakuyou("", param, null) || !substitutionNotice90IsBlank("", param, null) && isNoSogoCredit(_suramap) || "1".equals(property(Property.tyousasho2020SogakuShokenShasen));
                if (_isA4SogakuShokenSlash) {
                    _isA4SogakuShokenActSlash = true;
                    _isA4SogakuShokenValSlash = true;
                }
                if ("1".equals(getString(_hexamEntremarkHdat, "TOTALSTUDYACT_SLASH_FLG"))) {
                    _isA4SogakuShokenActSlash = true;
                }
                if ("1".equals(getString(_hexamEntremarkHdat, "TOTALSTUDYVAL_SLASH_FLG"))) {
                    _isA4SogakuShokenValSlash = true;
                }
            }
            _isPrintStudyrecSubstitutionToBiko = "1".equals(param._e019_01_namespare1) || _isA4SogakuShokenSlash;
        }

        private static String getProvSemesterName(final DB2UDB db2, final List<Grades> gradesList) {
            final TreeSet<String> provYearSemesterSet = new TreeSet<String>();
            for (final Grades g : gradesList) {
                if (null != g._provSemester) {
                    provYearSemesterSet.add(g._year + g._provSemester);
                }
            }
            if (provYearSemesterSet.isEmpty()) {
                return null;
            }
            final String provYearSemeester = provYearSemesterSet.first();
            if (provYearSemeester.length() < 5) {
                return null;
            }
            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR || SEMESTER = '" + provYearSemeester + "' ";
            final String semesterName = defstr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            return semesterName;
        }

        /**
         * 有効な[年度/学年]をセット。<br>
         * @param printData
         * @param sqlStudyrec
         */
        private void setGradeTitle(final DB2UDB db2, final Param param) {
            _titles = new TreeMap<String, Title>();
            final boolean notContainSchoolcd1 = param._z010.in(Z010Info.KaichiTsushin);
            final List<String> sqlLines = _sqlStudyrec.getGakusekiSqlString(this, param, notContainSchoolcd1);
            if (param._isOutputDebugQuery) {
                log.info(" title sql1 = " + Util.mkString(sqlLines, ""));
            }
            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, Util.mkString(sqlLines, ""));
            if (param._isOutputDebug) {
                log.info(" title row list ( size : " + rowList.size() + ") = " + rowList);
            }
            _abroadYears = getAbroadYears(db2, param);
            if (param._isOutputDebug) {
                log.info(" abroadYears = " + _abroadYears);
            }
            _abroadPrintDropYears = getAbroadPrintDropYears(db2, param);
            _offdaysYears = getOffdaysYears(db2, param);

            final boolean isGakunensei = isGakunensei(param);
            for (final Map<String, String> row : rowList) {
                if (isGakunensei && null != getString(row, "DROP_YEAR")) {
                    _ryunenYears.add(getString(row, "DROP_YEAR"));
                }
            }
            if (param._isOutputDebug && isGakunensei) {
                log.fatal(" 留年した年度 = " + _ryunenYears);
            }

            for (final Map<String, String> row : rowList) {
                final String year = getString(row, "YEAR");
                final String annual = getString(row, "ANNUAL");
                final String gradeCd = getString(row, "GRADE_CD");
                if (null == year) {
                    log.debug("年度がnull === SCHREGNO=" + _schregno + "  KEY=" + year + "===");
                    continue;
                }
                final int intKey = Integer.parseInt(year);
                if (0 > intKey) {
                    continue;
                }
                final String k = (0 == intKey) ? "0" : year;
                final Title title = new Title(intKey, year, annual, db2, gradeCd, this, param);
                if (_titles.containsKey(k)) {
                    log.warn(" *** overwrite title : " + k + " = " + _titles.get(k));
                }
                _titles.put(k, title);
            }
            if (param._isOutputDebug) {
                for (final Map.Entry<String, Title> e : _titles.entrySet()) {
                    log.info(" create title : " + e.getKey() + " = " + e.getValue());
                }
            }

            Title.setPosition(param, this, _titles.values());

            if (param._z010.in(Z010Info.KaichiTsushin)) {
                // 開智通信制は固定で入学前を表示する
                final int intKey = 0;
                final String year = "0";
                final String annual = "0";
                final String gradeCd = "0";
                final Title title = new Title(intKey, year, annual, db2, gradeCd, this, param);
                title._pos = Title.INVALID_POS;
                title._seisekiPos = 0;
                _titles.put(year, title);
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

        private List<String> getAbroadYears(final DB2UDB db2, final Param param) {
            final String psKey = "CHECK_ABROAD YEARS";
            if (null == param.getPs(psKey)) {
                final String sql = " SELECT TRANSFER_SDATE, TRANSFER_EDATE, INT(FISCALYEAR(TRANSFER_SDATE)) AS YEAR FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = ? AND TRANSFERCD = '1' ORDER BY TRANSFER_SDATE ";
                if (param._isOutputDebugQuery) {
                    log.info(" abroad years sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }
            final List<String> years = new ArrayList<String>();
            String beforeEdate = null;
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno})) {
                final String sdate = KnjDbUtils.getString(row, "TRANSFER_SDATE");
                boolean isSeq = false;
                if (null != beforeEdate && Util.isNextDate(beforeEdate, sdate)) {
                    isSeq = true;
                }
                if (!isSeq) {
                    years.add(KnjDbUtils.getString(row, "YEAR"));
                }
                beforeEdate = KnjDbUtils.getString(row, "TRANSFER_EDATE");
            }
            return years;
        }

        private List<String> getAbroadPrintDropYears(final DB2UDB db2, final Param param) {
            final List<String> years = new ArrayList<String>();
            if (param._tableInfo._hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD) {
                final String psKey = "CHECK_TRANSFER_DROP";
                if (null == param.getPs(psKey)) {
                    final String sql = " SELECT INT(FISCALYEAR(TRANSFER_SDATE)) - 1 AS YEAR FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = ? AND ABROAD_PRINT_DROP_REGD = '1' ";
                    if (param._isOutputDebugQuery) {
                        log.info(" abroad print drop years sql = " + sql);
                    }
                    param.setPs(db2, psKey, sql);
                }
                years.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno}), "YEAR"));
            }
            return years;
        }

        public List<Title> getGradeTitlesForForm(final Param param) {
            final List<Title> list = new ArrayList<Title>();
            if (isGakunensei(param)) {
                for (final Title title : titleValues()) {
                    if (_ryunenYears.contains(title._year) && !_abroadPrintDropYears.contains(title._year)) {
                        log.info(" skip title : " + title);
                        continue;
                    }
                    list.add(title);
                }
                if (param._isOutputDebug) {
                    log.info(" gradeTitles size = " + list.size() + " / titles size = " + _titles.size());
                }
            } else {
                list.addAll(titleValues());
            }
            return list;
        }

        private Map<String, String> getSuraMap(final Param param, final List<Title> titleList, final List<StudyrecDat> suraStudyrecData1) {
            final PrintData printData = this;
            final Map<String, String> suramap = new HashMap();
            final List<BigDecimal> _ad_creditList = new ArrayList<BigDecimal>();

            for (final StudyrecDat studyrecDat : printData._studyrecDatAbroadList) { // 進学用は最大1レコード
                final String before = suramap.get(SqlStudyrec.abroad);
                final String credit = Util.add(before, studyrecDat.creditSuraSum2(param));
                suramap.put(SqlStudyrec.abroad, credit);
                if (NumberUtils.isNumber(credit)) {
                    if (param._isOutputDebugSeiseki) {
                        log.info(" add abroad credit " + credit + " (" + studyrecDat._classname + ")");
                    }
                    _ad_creditList.add(new BigDecimal(credit)); // 単位数総計
                }
            }

            for (final StudyrecDat studyrecDat : suraStudyrecData1) {
                final boolean isE014 = null != printData._e014Subclasscd && printData._e014Subclasscd.equals(studyrecDat._subclasscd) || studyrecDat._isE014;
                final List<String> hanasuClassNames = PrintClass.hanasuClassnames(printData);

                // 大検・前籍項・総合・留学・教科総計の単位の処理
                boolean addTotal = false;
                String credit = null;
                if (printData._vNameMstD081List.contains(studyrecDat.keySubclasscd(param))) {
                    if (null == _d081List) {
                        _d081List = new ArrayList<StudyrecDat>();
                    }
                    _d081List.add(studyrecDat);
                } else if (printData._vNameMstE065List.contains(studyrecDat.keySubclasscd(param))) {
                    if (null == _e065List) {
                        _e065List = new ArrayList<StudyrecDat>();
                    }
                    _e065List.add(studyrecDat);
                } else if (isE014 || SqlStudyrec.sogo.equals(studyrecDat._classname) || null != printData._hanasuClass1 && printData._hanasuClass1._sqlCreditIsNendogoto && hanasuClassNames.contains(studyrecDat._classname)) {
                    // isE014 :E014
                    // SqlStudyrec.sogo.equals(studyrecDat._classname) :SOGO
                    // null != printData._hanasuClass1 && printData._hanasuClass1._sqlCreditIsNendogoto && hanasuClassNames.contains(studyrecDat._classname) :
                    credit = studyrecDat.creditSuraSum1();

                    if (SqlStudyrec.sogo.equals(studyrecDat._classname) && (param._z010.in(Z010Info.naraken) || !printData.sogoIsSuraShingakuyou("getSuraMap", param, null))) {
                        suramap.put(SqlStudyrec.sogo, credit);
                        addTotal = true;
                    } else if (null != printData._hanasuClass1 && printData._hanasuClass1._sqlCreditIsNendogoto && hanasuClassNames.contains(studyrecDat._classname)) { // 京都西山
//	                    suramap.put(PrintClass.nishiyamaLhr, credit);
                        printData._hanasuClass1._setCredit = true;
                        printData._hanasuClass1._credit = credit;
                        addTotal = true;
                    } else if (isE014) {
                        suramap.put("E014", credit);
                        suramap.put(SqlStudyrec.E014SUBCLASSNAME, studyrecDat._subclassname);
                        addTotal = true;
                    }

                } else {
                    boolean isSum2 = false;
                    if (SqlStudyrec.totalCredit.equals(studyrecDat._classname)) {
                        isSum2 = true;
                        credit = studyrecDat.creditSuraSum2(param);
                        suramap.put(SqlStudyrec.totalCredit, credit);
                        addTotal = true;
                    } else if (SqlStudyrec.zenseki.equals(studyrecDat._classname)) {
                        isSum2 = true;
                        credit = studyrecDat.creditSuraSum2(param);
                        suramap.put(SqlStudyrec.zenseki, credit);
                        addTotal = true;
                    } else if (SqlStudyrec.daiken.equals(studyrecDat._classname)) {
                        isSum2 = true;
                        credit = studyrecDat.creditSuraSum2(param);
                        suramap.put(SqlStudyrec.daiken, credit);
                        addTotal = true;
                    } else if (SqlStudyrec.hirokokulhr.equals(studyrecDat._classname)) {
                        isSum2 = true;
                        credit = studyrecDat.creditSuraSum2(param);
                        suramap.put(SqlStudyrec.hirokokulhr, credit);
                        addTotal = true;
                    } else if (null != printData._hanasuClass1 && !printData._hanasuClass1._sqlCreditIsNendogoto && hanasuClassNames.contains(studyrecDat._classname)) {
                        isSum2 = true;
                        credit = studyrecDat.creditSuraSum2(param);
                        if (printData._hanasuClass1._isPrintWhenHasCredit && null != credit || !printData._hanasuClass1._isPrintWhenHasCredit) {
                            printData._hanasuClass1._setCredit = true;
                            printData._hanasuClass1._credit = credit;
                            if (!printData._hanasuClass1._notUseRecordSubclassname) {
                                printData._hanasuClass1._printSubclassname = studyrecDat._subclassname;
                            }
                            addTotal = true;
                        }
//                	} else if (PrintClass.hanasuClassnames2().contains(studyrecDat._classname)) {
//                		isSum2 = true;
//    					credit = studyrecDat.creditSuraSum2();
//    					if (PrintClass.tokiwahr.equals(studyrecDat._classname)) {
//                            suramap.put(PrintClass.tokiwahr, credit);
//                            addTotal = true;
//                        } else if (PrintClass.bunkyoLhr.equals(studyrecDat._classname)) {
//                            suramap.put(PrintClass.bunkyoLhr, credit);
//                            addTotal = true;
//                        } else if (PrintClass.rakunanLhr.equals(studyrecDat._classname)) {
//                            suramap.put(PrintClass.rakunanLhr, credit);
//                            addTotal = true;
//                        } else if (PrintClass.higashiosakaKeiaiLhr.equals(studyrecDat._classname)) {
//                            suramap.put(PrintClass.higashiosakaKeiaiLhr, credit);
//                            addTotal = true;
//                        } else if (PrintClass.kyotoJiritsu.equals(studyrecDat._classname)) {
//                            if (null != credit) {
//                                suramap.put(PrintClass.kyotoJiritsu, credit);
//                                suramap.put(SqlStudyrec.KYOTO88_SUBCLASSNAME, studyrecDat._subclassname);
//                                addTotal = true;
//                            }
//                        }
                    }
                    if (!isSum2) {
                        log.info(" creditSuraElse : " + studyrecDat);
                        credit = studyrecDat.creditSuraElse();
                    }
                }

                if (param._isOutputDebugSeiseki) {
                    log.info(" add credit " + credit + " (" + studyrecDat._classname + ", addTotal = " + addTotal + ")");
                }
                if (addTotal && NumberUtils.isNumber(credit)) {
                    _ad_creditList.add(new BigDecimal(credit)); // 単位数総計
                }

                if (param._z010.in(Z010Info.Meiji) && SqlStudyrec.sogo.equals(studyrecDat._classname)) { // 明治の場合、suramapの設定を上書き
                    String meijiSogoCredit = null;
                    String meijiCsCredit = null;
                    for (final Grades g : studyrecDat.getNotDropGradesList()) {
                        final Title title = Title.getTitle(param, titleList, g._year);
                        if (null != title && NumberUtils.isNumber(g._credit)) {
                            if (NumberUtils.isDigits(title._year) && Integer.parseInt(title._year) <= 2010) {
                                meijiSogoCredit = Util.addDigits(meijiSogoCredit, g._credit);
                            } else {
                                meijiCsCredit = Util.addDigits(meijiCsCredit, g._credit);
                            }
                        }
                    }
                    suramap.put("MEIJI_SOGO", meijiSogoCredit);
                    suramap.put(SqlStudyrec.sogo, meijiCsCredit);
                }
            }

            if (param._isOutputDebug) {
                log.info(" _ad_creditList = " + _ad_creditList);
            }
            suramap.put("KEI_CREDIT", defstr(_ad_creditList.isEmpty() ? null : Util.bigDecimalSum(_ad_creditList), (String) null));
            return suramap;
        }

        /**
         *  大検・前籍項・総合・留学・教科総計の単位数の出力処理
         */
        private List<Map<String, String>> getSuraList(final Param param, final Map<String, String> suramap) {

            final PrintData printData = this;
            final List<Map<String, String>> suraList = new ArrayList<Map<String, String>>();
            final Map<String, String> empty = Collections.emptyMap();
            if (printData.isMeijiSogo(param)) {
                suraList.add(suraMap(printData.getSogoSubclassname(param), (String) suramap.get("MEIJI_SOGO"), "明治学園総合的な学習の時間", empty));
            }
            if (param._isOutputDebugSeiseki) {
                log.info(Util.debugMapToStr(" suramap = ", suramap));
            }
            if (suramap.containsKey(SqlStudyrec.E014SUBCLASSNAME)) {
                suraList.add(suraMap((String) suramap.get(SqlStudyrec.E014SUBCLASSNAME), (String) suramap.get("E014"), "E014科目コード", empty));
            }
            if (suramap.containsKey(SqlStudyrec.daiken)) {
                suraList.add(suraMap("高認等における認定単位", (String) suramap.get(SqlStudyrec.daiken), "大検：高認等における認定単位", empty));
            }
            if (suramap.containsKey(SqlStudyrec.zenseki)) {
                suraList.add(suraMap("前籍校における修得単位", (String) suramap.get(SqlStudyrec.zenseki), "前籍校における修得単位", empty));
            }
            if (!(param._z010.in(Z010Info.Tokiwa) || printData._isKyoto) && null != printData._hanasuClass1) {
                if (printData._hanasuClass1._setCredit) {
                    suraList.add(suraMap(printData._hanasuClass1._printSubclassname, printData._hanasuClass1._credit, printData._hanasuClass1.toString(), empty));
                }
            }

            if (null != _d081List) {
                for (final StudyrecDat sr : _d081List) {
                    suraList.add(suraMap(sr._subclassname, sr.credit(), "D081", empty));
                }
            }

            // 総合的な学習の時間
            final String sogoSubclassname;
            if (param._z010.in(Z010Info.Meiji)) {
                sogoSubclassname = "Catholic Spirit";
            } else {
                sogoSubclassname = printData.getSogoSubclassname(param);
            }
            final boolean sogoIsSura = printData.sogoIsSuraShingakuyou("getSuraList", param, null);
            if (sogoIsSura && !param._z010.in(Z010Info.naraken)) {
                final Map suraMap = new HashMap();
                suraMap.put("ITEM2", sogoSubclassname);
                suraList.add(suraMap);
            } else {
                final String setSogoCre;
                if (suramap.get(SqlStudyrec.sogo) == null) {
                    if ("1".equals(printData.getParameter(Parameter.SOUGAKU_CREDIT))) {
                        setSogoCre = "0";
                    } else if ("2".equals(printData.getParameter(Parameter.SOUGAKU_CREDIT))) {
                        setSogoCre = "";
                    } else {
                        setSogoCre = "2".equals(printData.getParameter(Parameter.TANIPRINT_SOUGOU)) || sogoIsSura && param._z010.in(Z010Info.naraken) ? "" : "0";
                    }
                } else {
                    setSogoCre = (String) suramap.get(SqlStudyrec.sogo);
                }
                if (param._z010.in(Z010Info.Hirokoku)) {
                    if (printData.isHirokokuSogakuSuraForm(param)) {
                        final Map suraMap = new HashMap();
                        suraMap.put("ITEM2", sogoSubclassname);
                        suraMap.put(FormKNJE070_1.TOTAL_CREDIT2field, suramap.get(SqlStudyrec.sogo));
                        suraList.add(suraMap);
                    } else {
                        suraList.add(suraMap(sogoSubclassname, setSogoCre, sogoSubclassname + "（広島国際）", empty));
                    }
                } else if (param._z010.in(Z010Info.Nishiyama)) {
                    suraList.add(suraMap(sogoSubclassname, setSogoCre, sogoSubclassname + "（西山）", empty));
                } else {
                    final Map<String, String> sogakuParamMap = new HashMap<String, String>();
                    sogakuParamMap.put(SOGAKU_FLG, "1");
                    suraList.add(suraMap(sogoSubclassname, setSogoCre, sogoSubclassname, sogakuParamMap));
                }
            }

            if (printData._isKyoto && null != printData._hanasuClass1) {
                if (printData._hanasuClass1._setCredit) {
                    suraList.add(suraMap(printData._hanasuClass1._printSubclassname, printData._hanasuClass1._credit, "京都府 " + printData._hanasuClass1.toString(), empty));
                }
            }

            if (null != _e065List) {
                for (final StudyrecDat sr : _e065List) {
                    suraList.add(suraMap(sr._subclassname, sr.credit(), "E065(単位数のみ表記の科目) " + sr._subclasscd + ":" + sr._subclassname, empty));
                }
            }

            if (param._z010.in(Z010Info.Tokiwa)) {
                // 常磐小計
                final String totalCredit = (String) suramap.get(SqlStudyrec.totalCredit);
                final String sogoCredit = (String) suramap.get(SqlStudyrec.sogo);
                suraList.add(suraMap("小計", Util.addDigits(totalCredit, sogoCredit), "常磐小計", empty));
            }
            if (param._z010.in(Z010Info.Hirokoku)) {
                // 広島国際LHR
                suraList.add(suraMap("ＬＨＲ", (String) suramap.get(SqlStudyrec.hirokokulhr), "広島国際LHR", empty));
            }

            // 留学
            final String abroadname = param._z010.in(Z010Info.Tokiwa) ? "留学による修得単位" : "留学";
            final String setAbroadCre;
            final boolean useAbroadCreditSlash = param._z010.in(Z010Info.naraken) && !"1".equals(_tyousasyo2020);
            if (suramap.get(SqlStudyrec.abroad) == null) {
                if ("1".equals(printData.getParameter(Parameter.RYUGAKU_CREDIT))) {
                    setAbroadCre = "0";
                } else if ("2".equals(printData.getParameter(Parameter.RYUGAKU_CREDIT))) {
                    setAbroadCre = "";
                } else {
                    setAbroadCre = "2".equals(printData.getParameter(Parameter.TANIPRINT_RYUGAKU)) || useAbroadCreditSlash ? "" : "0";
                }
            } else {
                setAbroadCre = (String) suramap.get(SqlStudyrec.abroad);
            }
            final Map<String, String> abroadParamMap = new HashMap<String, String>();
            abroadParamMap.put("#ABROAD_FLG", "1");
            if (useAbroadCreditSlash && (!NumberUtils.isDigits(setAbroadCre) || Integer.parseInt(setAbroadCre) == 0)) {
                // 奈良県で留学単位数がないなら欄はスラッシュ
                printData._abroadCreditIsSura = true;
            }
            suraList.add(suraMap(abroadname, setAbroadCre, abroadname, abroadParamMap));

            if (param._z010.in(Z010Info.Tokiwa) && null != printData._hanasuClass1) {
                // 常磐HR
                suraList.add(suraMap(printData._hanasuClass1._printSubclassname, printData._hanasuClass1._credit, "常磐HR", empty));
            }

            // 計
            final Map totalParamMap = new HashMap();
            totalParamMap.put(TOTAL_FLG, "1");
            final String keiCredit = (String) suramap.get("KEI_CREDIT");
            suraList.add(suraMap("計", keiCredit, "計", totalParamMap));

            return suraList;
        }

        private static Map suraMap(final Object item, final Object totalCredit, final String comment, final Map<String, String> otherParam) {
            final Map m = new HashMap();
            m.put(FormKNJE070_1.ITEMfield, item);
            final String s = defstr(totalCredit, (String[]) null);
            if (!StringUtils.isBlank(s)) {
                final boolean isNotInt = NumberUtils.isNumber(s) && !NumberUtils.isDigits(s);
                m.put(isNotInt ? FormKNJE070_1.TOTAL_CREDIT_DOTfield : FormKNJE070_1.TOTAL_CREDITfield, totalCredit);
            }
            m.put("#COMMENT", comment); // デバッグ用
            m.putAll(otherParam);
            return m;
        }

        /**
         * 備考欄下の証明書学校データの備考を表示するか
         * @return 備考を表示するならtrue、そうでなければfalse
         */
        private boolean isPrintSchoolRemark(final Param param) {
            return "1".equals(getParameter(Parameter.certifSchoolOnly)) || !getGradesListForPrintSchoolRemark(param, _studyrecData1).isEmpty();
        }


        /**
         * 備考欄下の証明書学校データの備考の表示判定に使用するGradesのリスト
         */
        private List<Grades> getGradesListForPrintSchoolRemark(final Param param, final List<StudyrecDat> studyrecData1) {
            final List<Grades> list = new ArrayList<Grades>();
            for (final StudyrecDat studyrecDat : studyrecData1) {
                for (final Grades grades : studyrecDat.getNotDropGradesList()) {
                    final Title title = Title.getTitle(param, titleValues(), grades._year);
                    if (null != title && NumberUtils.isDigits(title._annual) &&  Integer.parseInt(title._annual) >= 3) {
                        list.add(grades);
                    }
                }
            }
            return list;
        }

        protected void getExamMap(final DB2UDB db2, final Param param) {
            if (_notPrintShoken) {
                return;
            }
            if (PrintData._shusyokuYou == _output) {
                final String psKey = "ps4";
                if (null == param.getPs(psKey)) {

                    // 調査書就職用所見データのSQL
                    final StringBuffer sql = new StringBuffer();
                    final StringBuffer cond = new StringBuffer();
                    sql.append("SELECT W1.SCHREGNO, JOBHUNT_REC, JOBHUNT_RECOMMEND, JOBHUNT_ABSENCE, JOBHUNT_HEALTHREMARK ");
                    if (param._isPrintGrd) {
                        sql.append(" FROM GRD_HEXAM_EMPREMARK_DAT W1 ");
                    } else {
                        if ("1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            sql.append(" FROM HEXAM_EMPREMARK_SEQ_DAT W1 ");
                            if (PRGID_KNJG010.equals(param._prgid)) {
                                cond.append(" AND PATTERN_SEQ = '1' ");
                            } else {
                                cond.append(" AND PATTERN_SEQ = '" + getParameter(Parameter.SELECT_PATTERN)  + "' ");
                            }
                        } else {
                            sql.append(" FROM HEXAM_EMPREMARK_DAT W1 ");
                        }
                    }
                    sql.append(" WHERE W1.SCHREGNO = ? ");
                    sql.append(cond);

                    if (param._isOutputDebugQuery) {
                        log.info(" shoken emp sql = " + sql);
                    }

                    param.setPs(db2, psKey, sql.toString());
                }
                final List rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno});
                if (rowList.size() > 0) {
                    final Map row = (Map) rowList.get(0);
                    _jobHuntAbsence = getString(row, "JOBHUNT_ABSENCE");
                    _jobHuntRec = getString(row, "JOBHUNT_REC");
                    _jobHuntHealthremark = getString(row, "JOBHUNT_HEALTHREMARK");
                    _jobHuntRecommend = getString(row, "JOBHUNT_RECOMMEND");
                }
            } else {
                final String psKey1 = "ps4_1";
                if (null == param.getPs(psKey1)) {
                    // 調査書進学用所見データのSQL

                    final String tabname;
                    final boolean hasShasenFlgField;
                    final StringBuffer cond = new StringBuffer();
                    if (param._isPrintGrd) {
                        tabname = "GRD_HEXAM_ENTREMARK_HDAT";
                        hasShasenFlgField = param._tableInfo._hasGRD_HEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG;
                    } else {
                        if ("1".equals(getParameter(Parameter.tyousasyo2020)) && "1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            tabname = " HEXAM_ENTREMARK_SEQ_HDAT ";
                            hasShasenFlgField = true;
                            if (PRGID_KNJG010.equals(param._prgid)) {
                                cond.append(" AND PATTERN_SEQ = '1' ");
                            } else {
                                cond.append(" AND PATTERN_SEQ = '" + getParameter(Parameter.SELECT_PATTERN)  + "' ");
                            }
                        } else {
                            tabname = " HEXAM_ENTREMARK_HDAT ";
                            hasShasenFlgField = param._tableInfo._hasHEXAM_ENTREMARK_HDAT_TOTALSTUDYACT_SLASH_FLG;
                        }
                    }
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("  SCHREGNO, TOTALSTUDYACT, TOTALSTUDYVAL, REMARK ");
                    if (hasShasenFlgField) {
                        sql.append("  , TOTALSTUDYACT_SLASH_FLG ");
                        sql.append("  , TOTALSTUDYVAL_SLASH_FLG ");
                    } else {
                        sql.append("  , CAST(NULL AS VARCHAR(1)) AS TOTALSTUDYACT_SLASH_FLG ");
                        sql.append("  , CAST(NULL AS VARCHAR(1)) AS TOTALSTUDYVAL_SLASH_FLG ");
                    }
                    sql.append(" FROM " + tabname + " W1 ");
                    sql.append(" WHERE SCHREGNO = ? ");
                    sql.append(cond);
                    if (param._isOutputDebugQuery) {
                        log.info(" shoken hdat sql = " + sql.toString());
                    }

                    param.setPs(db2, psKey1, sql.toString());
                }
                _hexamEntremarkHdat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey1), new String[] {_schregno}));

                final String psKey2 = "ps4_2";
                if (null == param.getPs(psKey2)) {
                    // 調査書進学用所見データのSQL

                    final String tabname;
                    final StringBuffer cond = new StringBuffer();
                    final boolean attSlashFlg;
                    final boolean totalstudySlashFlg;
                    if (param._isPrintGrd) {
                        tabname = "GRD_HEXAM_ENTREMARK_DAT";
                        attSlashFlg = param._tableInfo._hasGRD_HEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG;
                        totalstudySlashFlg = param._tableInfo._hasGRD_HEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG;
                    } else {
                        if ("1".equals(getParameter(Parameter.tyousasyo2020)) && "1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            tabname = " HEXAM_ENTREMARK_SEQ_DAT ";
                            if (PRGID_KNJG010.equals(param._prgid)) {
                                cond.append(" AND PATTERN_SEQ = '1' ");
                            } else {
                                cond.append(" AND PATTERN_SEQ = '" + getParameter(Parameter.SELECT_PATTERN)  + "' ");
                            }
                            attSlashFlg = true;
                            totalstudySlashFlg = true;
                        } else {
                            tabname = " HEXAM_ENTREMARK_DAT ";
                            attSlashFlg = param._tableInfo._hasHEXAM_ENTREMARK_DAT_ATTENDREC_REMARK_SLASH_FLG;
                            totalstudySlashFlg = param._tableInfo._hasHEXAM_ENTREMARK_DAT_TOTALSTUDYACT_SLASH_FLG;
                        }
                    }
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("  ANNUAL, YEAR, ATTENDREC_REMARK, SPECIALACTREC ");
                    if ("1".equals(_tyousasyo2020)) {
                    } else if ("1".equals(param._useSyojikou3)) {
                        sql.append(" , TRAIN_REF1, TRAIN_REF2, TRAIN_REF3 ");
                    }
                    sql.append(", TRAIN_REF, ");
                    sql.append("  TOTALSTUDYACT AS DAT_TOTALSTUDYACT, ");
                    sql.append("  TOTALSTUDYVAL AS DAT_TOTALSTUDYVAL ");
                    if (attSlashFlg) {
                        sql.append("  , ATTENDREC_REMARK_SLASH_FLG ");
                    } else {
                        sql.append("  , CAST(NULL AS VARCHAR(1)) AS ATTENDREC_REMARK_SLASH_FLG ");
                    }
                    if (totalstudySlashFlg) {
                        sql.append("  , TOTALSTUDYACT_SLASH_FLG AS DAT_TOTALSTUDYACT_SLASH_FLG ");
                        sql.append("  , TOTALSTUDYVAL_SLASH_FLG AS DAT_TOTALSTUDYVAL_SLASH_FLG ");
                    } else {
                        sql.append("  , CAST(NULL AS VARCHAR(1)) AS DAT_TOTALSTUDYACT_SLASH_FLG ");
                        sql.append("  , CAST(NULL AS VARCHAR(1)) AS DAT_TOTALSTUDYVAL_SLASH_FLG ");
                    }
                    sql.append(" FROM " + tabname + " ");
                    sql.append(" WHERE SCHREGNO = ? AND YEAR <= ? ");
                    sql.append(cond);
                    sql.append(" ORDER BY ANNUAL, YEAR ");
                    if (param._isOutputDebugQuery) {
                        log.info(" shoken dat sql = " + sql);
                    }

                    param.setPs(db2, psKey2, sql.toString());
                }
                _hexamEntremarkDatMap = new HashMap<String, HexamEntremarkDat>();
                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {_schregno, _year})) {
                    final String year = getString(row, "YEAR");
                    final int intKey = Integer.parseInt(year);
                    if (0 > intKey) {
                        continue;
                    }
                    final String k = (0 == intKey) ? "0" : year;
                    final Title title = Title.getTitle(param, titleValues(), k);
                    if (null == title) {
//                            if (isGakunensei() && printData._ryunenYears.contains(year)) {
//                                // log.debug(" 所見データが存在するが留年している年度のため表示対象外 = " + year);
//                            } else {
//                                final Title title = new Title(k, intKey, year, annual);
//                                printData._titles.put(k, title);
//                            }
                        continue;
                    }
                    _hexamEntremarkDatMap.put(k, new HexamEntremarkDat(row));
                }

                if ("1".equals(_tyousasyo2020)) {
                    final String psKey3 = "ps4_3";
                    if (null == param.getPs(psKey3)) {

                        final StringBuffer sql = new StringBuffer();
                        sql.append(" SELECT ");
                        sql.append("   YEAR ");
                        sql.append(" , TRAIN_SEQ ");
                        sql.append(" , REMARK ");
                        if ("1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            sql.append(" FROM HEXAM_ENTREMARK_TRAINREF_SEQ_DAT ");
                        } else {
                            sql.append(" FROM HEXAM_ENTREMARK_TRAINREF_DAT ");
                        }
                        sql.append(" WHERE SCHREGNO = ? AND YEAR <= ? AND TRAIN_SEQ IN ('101', '102', '103', '104', '105', '106') ");
                        if ("1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            if (PRGID_KNJG010.equals(param._prgid)) {
                                sql.append(" AND PATTERN_SEQ = '1' ");
                            } else {
                                sql.append(" AND PATTERN_SEQ = '" + getParameter(Parameter.SELECT_PATTERN)  + "' ");
                            }
                        }
                        sql.append(" ORDER BY YEAR ");

                        param.setPs(db2, psKey3, sql.toString());
                    }

                    for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey3), new Object[] {_schregno, _year})) {
                        final String year = getString(row, "YEAR");
                        final int intKey = Integer.parseInt(year);
                        if (0 > intKey) {
                            continue;
                        }
                        final String k = (0 == intKey) ? "0" : year;
                        final Title title = Title.getTitle(param, titleValues(), year);
                        if (null == title) {
                            log.info(" null trainref title : " + year);
//                                if (isGakunensei() && printData._ryunenYears.contains(year)) {
//                                    // log.debug(" 所見データが存在するが留年している年度のため表示対象外 = " + year);
//                                } else {
//                                    final Title title = new Title(k, intKey, year, annual);
//                                    printData._titles.put(k, title);
//                                }
                            continue;
                        }
                        Seq seq = Seq.of(String.valueOf(Integer.parseInt(getString(row, "TRAIN_SEQ")) - 100));
                        Util.getMappedMap(_yearHexamEntremarkTrainRefDatMap, Year.of(k)).put(seq, getString(row, "REMARK"));
                    }

                    final String psKey4 = "ps4_4";
                    if (null == param.getPs(psKey4)) {

                        final StringBuffer sql = new StringBuffer();
                        sql.append(" SELECT ");
                        sql.append("   REMARK ");
                        sql.append(" FROM ");
                        if ("1".equals(property(Property.tyousasyo_shokenTable_Seq))) {
                            sql.append(" HEXAM_ENTREMARK_REMARK_SEQ_HDAT WHERE SCHREGNO = ? ");
                            if (PRGID_KNJG010.equals(param._prgid)) {
                                sql.append(" AND PATTERN_SEQ = '1' ");
                            } else {
                                sql.append(" AND PATTERN_SEQ = '" + getParameter(Parameter.SELECT_PATTERN)  + "' ");
                            }
                        } else {
                            sql.append(" HEXAM_ENTREMARK_REMARK_HDAT WHERE SCHREGNO = ? ");
                        }

                        param.setPs(db2, psKey4, sql.toString());
                    }

                    _hexamEntremarkRemarkHdatRemark = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey4), new Object[] {_schregno}));
                }
            }

            if ("1".equals(property(Property.tyousasyoPrintAttendrecRemarkKaikinDat))) {

                final String psKey = "psAttendRemarkKaikinDat";
                if (null == param.getPs(psKey)) {

                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT  ");
                    sql.append("     T1.YEAR ");
                    sql.append("   , CASE T2.KAIKIN_DIV WHEN '1' THEN '皆勤' ");
                    sql.append("                        WHEN '2' THEN '精勤' ");
                    sql.append("     END AS KAIKIN_NAME ");
                    sql.append(" FROM KAIKIN_DAT T1 ");
                    sql.append(" LEFT JOIN KAIKIN_MST T2 ON T2.KAIKIN_CD = T1.KAIKIN_CD ");
                    sql.append(" WHERE ");
                    sql.append("     T1.YEAR <= '" + _year + "' ");
                    sql.append(" AND T1.SCHREGNO = ? ");
                    sql.append(" AND T1.KAIKIN_FLG = '1' ");
                    sql.append(" AND T1.INVALID_FLG = '0' ");
                    sql.append(" AND T2.KAIKIN_DIV IN ('1', '2') "); // 1:皆勤 2:精勤
                    sql.append(" AND T2.REF_YEAR = 1 "); // 1ヵ年
                    param.setPs(db2, psKey, sql.toString());
                }

                _yearKaikinDatTextMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno }), "YEAR", "KAIKIN_NAME");
            }

            if (param._z010.isRitsumeikan() && Arrays.asList(PRGID_KNJE070A).contains(param._prgid) && _personInfo._graduateAble) {

                final String psKey = "psAftTotalStudyToeflDat";
                if (null == param.getPs(psKey)) {

                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("     MAX(T1.SCORE) AS SCORE ");
                    sql.append(" FROM AFT_TOTAL_STUDY_TOEFL_DAT T1 ");
                    sql.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
                    sql.append("     AND GDAT.GRADE = T1.GRADE ");
                    sql.append(" WHERE ");
                    sql.append("     T1.YEAR <= '" + _year + "' ");
                    sql.append(" AND T1.SCHREGNO = ? ");
                    sql.append(" AND VALUE(GDAT.SCHOOL_KIND, '') NOT IN ('J', 'P') ");
                    param.setPs(db2, psKey, sql.toString());
                }

                _toeflScore = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno }));
                if (NumberUtils.isNumber(_toeflScore)) {
                    _toeflScore = "TOEFL-ITP " + _toeflScore + "点";
                }
            }
        }

        protected void getMedexamMap(final DB2UDB db2, final Param param) {
            if (PrintData._shusyokuYou != _output || "1".equals(getParameter(Parameter.certifSchoolOnly))) {
                return;
            }
            final String psKey = "ps3";
            if (null == param.getPs(psKey)) {

                // 健康診断データのSQL
                final String tname1 = param._isPrintGrd ? "GRD_MEDEXAM_DET_DAT" : "MEDEXAM_DET_DAT";
                final String tname2 = param._isPrintGrd ? "GRD_MEDEXAM_HDAT" : "MEDEXAM_HDAT";

                final StringBuffer sql = new StringBuffer();
                sql.append("SELECT ");
                sql.append("  T2.DATE ");
                sql.append(", T1.HEIGHT ");
                sql.append(", T1.WEIGHT ");
                sql.append(", T1.R_BAREVISION_MARK ");
                sql.append(", T1.L_BAREVISION_MARK ");
                sql.append(", T1.R_VISION_MARK ");
                sql.append(", T1.L_VISION_MARK ");
                sql.append(", T1.R_BAREVISION ");
                sql.append(", T1.L_BAREVISION ");
                sql.append(", T1.R_VISION ");
                sql.append(", T1.L_VISION ");
                sql.append(", T1.R_EAR ");
                sql.append(", T1.L_EAR ");
                sql.append(" FROM ");
                sql.append( tname1 + " T1 ");
                sql.append(" INNER JOIN " + tname2 + " T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR = T2.YEAR ");
                sql.append(" WHERE  ");
                sql.append("  T1.SCHREGNO = ? ");
                if (param._z010.in(Z010Info.Kyoai)) {
                    sql.append("  AND T1.YEAR = ? ");
                } else {
                    sql.append("  AND T1.YEAR <= ? ");
                }
                sql.append(" ORDER BY ");
                sql.append("  T1.YEAR DESC");

                param.setPs(db2, psKey, sql.toString());
            }
            _medexamDetDat = new MedexamDetDat(KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] {_schregno, _year})));
        }

        /**
         *  SVF-FORM  成績段階別人数をフォームへ出力
         */
        public void getGeneviewmbrMap(final DB2UDB db2, final Param param) {
            if (PrintData._shingakuYou != _output || "1".equals(getParameter(Parameter.certifSchoolOnly))) {
                return;
            }

            final String psKey = "ps5";
            if (null == param.getPs(psKey)) {

                final String tname1 = param._isPrintGrd ? "GRD_REGD_DAT" : "SCHREG_REGD_DAT";
                final StringBuffer stb = new StringBuffer();
                if (param.isKindaifuzoku()) {
                    stb.append("WITH COURSE_GROUP_W AS(");
                    stb.append(    "SELECT  CG.COURSE_SEQ, CG.GRADE, CG.YEAR ");
                    stb.append(    "FROM    COURSE_GROUP_DAT CG ");
                    stb.append(    "WHERE   EXISTS(SELECT  'X' ");
                    stb.append(                   "FROM   " + tname1 + " REGD ");
                    stb.append(                   "WHERE   REGD.SCHREGNO = ? AND ");
                    stb.append(                           "REGD.YEAR = ? AND ");
                    stb.append(                           "REGD.SEMESTER = ? AND ");
                    stb.append(                           "CG.YEAR = REGD.YEAR AND ");
                    stb.append(                           "CG.GRADE = REGD.GRADE AND ");
                    stb.append(                           "CG.HR_CLASS = REGD.HR_CLASS) ");
                    stb.append(    ") ");

                    stb.append("SELECT  SUM(GENE.A_MEMBER) AS MEMBER5, ");
                    stb.append(        "SUM(GENE.B_MEMBER) AS MEMBER4, ");
                    stb.append(        "SUM(GENE.C_MEMBER) AS MEMBER3, ");
                    stb.append(        "SUM(GENE.D_MEMBER) AS MEMBER2, ");
                    stb.append(        "SUM(GENE.E_MEMBER) AS MEMBER1, ");
                    stb.append(        "SUM(GENE.COURSE_MEMBER) AS MEMBER0, ");
                    stb.append(        "SUM(GENE.GRADE_MEMBER)  AS MEMBER6 ");
                    stb.append("FROM    GENEVIEWMBR_DAT GENE, ");
                    stb.append(        "COURSE_GROUP_W CG ");
                    stb.append("WHERE   GENE.YEAR = CG.YEAR AND ");
                    stb.append(        "GENE.GRADE = CG.GRADE AND ");
                    stb.append(        "GENE.COURSECODE = CG.COURSE_SEQ");
                } else {
                    final String gaihyouGakkaBetu = property(Property.gaihyouGakkaBetu);
                    stb.append("SELECT ");
                    stb.append(        "GENE.A_MEMBER AS MEMBER5,");
                    stb.append(        "GENE.B_MEMBER AS MEMBER4,");
                    stb.append(        "GENE.C_MEMBER AS MEMBER3,");
                    stb.append(        "GENE.D_MEMBER AS MEMBER2,");
                    stb.append(        "GENE.E_MEMBER AS MEMBER1,");
                    stb.append(        "GENE.COURSE_MEMBER AS MEMBER0,");
                    stb.append(        "GENE.GRADE_MEMBER AS MEMBER6 ");
                    stb.append("FROM ");
                    if (paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(getParameter(Parameter.GVAL_CALC_CHECK))) {
                        stb.append(        "GENEVIEWMBR_KIND_DAT GENE,");
                    } else {
                        stb.append(        "GENEVIEWMBR_DAT GENE,");
                    }
                    stb.append(        tname1 + " REGD ");
                    if ("2".equals(gaihyouGakkaBetu)) {
                        stb.append("  INNER JOIN COURSE_GROUP_CD_DAT CGD ON CGD.YEAR = REGD.YEAR ");
                        stb.append("    AND CGD.GRADE = REGD.GRADE ");
                        stb.append("    AND CGD.COURSECD = REGD.COURSECD ");
                        stb.append("    AND CGD.MAJORCD = REGD.MAJORCD ");
                        stb.append("    AND CGD.COURSECODE = REGD.COURSECODE ");
                    }
                    stb.append("WHERE ");
                    stb.append(        "REGD.SCHREGNO = ? ");
                    stb.append(        "AND REGD.YEAR = ? ");
                    stb.append(        "AND REGD.SEMESTER = ? ");
                    stb.append(        "AND GENE.YEAR = REGD.YEAR ");
                    if (paramapContains(Parameter.GVAL_CALC_CHECK) && "2".equals(getParameter(Parameter.GVAL_CALC_CHECK))) {
                        stb.append(        "AND GENE.KINDCD = '01' "); // 多重平均
                    }
                    stb.append(        "AND GENE.GRADE = REGD.GRADE ");
                    if ("1".equals(gaihyouGakkaBetu)) {
                        stb.append(        "AND GENE.COURSECD = REGD.COURSECD ");
                        stb.append(        "AND GENE.MAJORCD = REGD.MAJORCD ");
                        stb.append(        "AND VALUE(GENE.COURSECODE,'0000') = VALUE(REGD.COURSECODE,'0000')");
                    } else if ("2".equals(gaihyouGakkaBetu)) {
                        stb.append(        "AND GENE.COURSECD = '0' ");
                        stb.append(        "AND GENE.MAJORCD = CGD.GROUP_CD ");
                        stb.append(        "AND GENE.COURSECODE = '0000' ");
                    } else {
                        stb.append(        "AND GENE.COURSECD = REGD.COURSECD ");
                        stb.append(        "AND GENE.MAJORCD = REGD.MAJORCD ");
                        stb.append(        "AND GENE.COURSECODE = '0000' ");
                    }
                }

                //  成績概評人数データ
                param.setPs(db2, psKey, stb.toString());
            }

            _geneviewmbrMap = new HashMap();
            final List rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _semes});
            if (rowList.size() > 0) {
                _geneviewmbrMap.put("true", (Map) rowList.get(0));
            }
        }

        // 広島国際でV_NAME_MST.NAMECD1 = 'E048'に登録された学科は、総合的な学習の時間の所見、単位が斜線のフォームを使用する
        public boolean isHirokokuSogakuSuraForm(final Param param) {
            if (!param._z010.in(Z010Info.Hirokoku)) {
                return false;
            }
            if (null == _personInfo._coursecd || null == _personInfo._majorcd) {
                return false;
            }
//            log.debug(" coursecd = " + _coursecd + ", majorcd = " + _majorcd);
            // 名称1: 課程コード、 名称2: 学科コード
            for (final Map<String, String> row : _vNameMstE048List) {
                if (_personInfo._coursecd.equals(getString(row, "NAME1")) && _personInfo._majorcd.equals(getString(row, "NAME2"))) {
                    return true;
                }
            }
            return false;
        }

        // 駿台甲府で'E050'に登録された学科は、美術デザイン学科
        public boolean isSundaiKofuBijutsuDesignKa(final Param param) {
            if (!param._z010.in(Z010Info.Sundaikoufu)) {
                return false;
            }
            boolean isBijutsuDesignKa = false;
            for (final Map<String, String> row : param._e050List) {
                final String namespare2 = getString(row, "NAMESPARE2");
                final String namespare3 = getString(row, "NAMESPARE3");
                if (null != namespare2 && namespare2.equals(_personInfo._coursecd) && null != namespare3 && namespare3.equals(_personInfo._majorcd)) {
                    isBijutsuDesignKa = true;
                }
            }
            return isBijutsuDesignKa;
        }

        public StudyrecDat getE014() {
            StudyrecDat e014 = null;
            for (final StudyrecDat studyrecDat : _studyrecData1) {
                if (studyrecDat._isE014) {
                    e014 = studyrecDat;
                    break;
                }
            }
            return e014;
        }

         public boolean hasE014() {
             return null != getE014();
        }

        public boolean miyagikenShushokuyouUseGappeiTougouForm(final Param param) {
            if (!param._z010.in(Z010Info.Miyagiken)) {
                return false;
            }
            if (PrintData._shusyokuYou != _output) {
                return false;
            }
            if (!"1".equals(property(Property.tyousasyoSyusyokuPrintGappeiTougou))) {
                return false;
            }
            final String schoolMstFoundedYear = getString(_schoolMstMap, "FOUNDEDYEAR");
            boolean use = true;
            if (NumberUtils.isDigits(_personInfo._entYear) && NumberUtils.isDigits(schoolMstFoundedYear) && Integer.parseInt(schoolMstFoundedYear) < Integer.parseInt(_personInfo._entYear)) {
                use = false;
            }
            if (null != _personInfo) {
                use = use && PersonInfo.isNyugaku(_personInfo._entDiv) && null != _personInfo._entSchool;
                if (param._isOutputDebug) {
                    log.info(" useGappeiForm = " + use + " / entYear=" + _personInfo._entYear + ", foundedYear=" + schoolMstFoundedYear + ", nyugaku=" + PersonInfo.isNyugaku(_personInfo._entDiv) + ", entSchool=" + _personInfo._entSchool);
                }
            }
            return use;
        }

        private Tuple<List<StudyrecDat>, List<StudyrecDat>> setSuraStudyrecDataAndNewStudyrecData1(final Param param, final List<StudyrecDat> studyrecData1) {
            final List<StudyrecDat> suraStudyrecData = new ArrayList<StudyrecDat>();
            final List<StudyrecDat> newStudyrecData1 = new ArrayList<StudyrecDat>();


            final List<String> suraClassNames = Arrays.asList(SqlStudyrec.totalCredit, SqlStudyrec.sogo, SqlStudyrec.zenseki, SqlStudyrec.daiken, SqlStudyrec.hirokokulhr);
            final List<String> hanasuClassNames = PrintClass.hanasuClassnames(this);

            for (final StudyrecDat studyrecDat : studyrecData1) {

                boolean isSura = false;
                isSura = isSura || suraClassNames.contains(studyrecDat._classname);
                isSura = isSura || hanasuClassNames.contains(studyrecDat._classname);
                isSura = isSura || null != _e014Subclasscd && _e014Subclasscd.equals(studyrecDat._subclasscd) || studyrecDat._isE014;
                isSura = isSura || _vNameMstD081List.contains(studyrecDat.keySubclasscd(param));
                isSura = isSura || _vNameMstE065List.contains(studyrecDat.keySubclasscd(param));
                if (isSura) {
                    suraStudyrecData.add(studyrecDat);
                    if (param._isOutputDebugSeiseki) {
                        log.info(" remove sura " + studyrecDat);
                    }
                } else {
                    newStudyrecData1.add(studyrecDat);
                }
            }
            return Tuple.of(suraStudyrecData, newStudyrecData1);
        }

        private List<StudyrecDat> getPrintTargetStudyrecDatList(final Param param) {
            final List<StudyrecDat> printTargetList = new ArrayList<StudyrecDat>();
            for (int i = 0; i < _studyrecData1.size(); i++) {
                final StudyrecDat studyrecDat = _studyrecData1.get(i);

                if (param._isOutputDebugSeiseki) {
                    log.info(" studyrecDat[" + i + "] = " + studyrecDat.toString());
                }

                final String keySubclassCd = studyrecDat.keySubclasscd(param);
                if (_useStudyrecSql2) {
                    if (param._d020Name1List.contains(studyrecDat._subclasscd)) {
                        continue;
                    }
                } else {
                    if (_d020Map.containsKey(keySubclassCd)) {
//                		if (param._isMusashi) {
//                			final int d020Cnt = ((Integer) __d020Map.get(keySubclassCd)).intValue();
//                			if (d020Cnt < __titles.size() - 1) {
//                				continue;
//                			}
//                		} else {
                            continue;
//                		}
                    }
                }

                if (!studyrecDat.hasGrades(this)) { continue; }
                // 未履修科目は出力しないの場合
                if ("2".equals(getParameter(Parameter.MIRISYU))) {
                    final String compCredit = studyrecDat.compCredit();
                    if (compCredit == null || compCredit.equals("0")) {
                        continue;
                    }
                }
                printTargetList.add(studyrecDat);
            }
            return printTargetList;
        }

        private boolean isNotPrintAvgGrades(final Param param, final String classcd, final String schoolKind) {
            if (param._z010.in(Z010Info.Jisyuukan) && "94".equals(classcd)) {
                return true;
            } else if (param._z010.in(Z010Info.Hosei) && ("88".equals(classcd))) {
                return true;
            }
            boolean hasStudyrec = false;
            boolean isAllD077 = true;
            boolean isAllD081 = true;
            boolean isAllE065 = true;
            final List<Grades> sameClassGradesList = new ArrayList<Grades>();
            for (final StudyrecDat sd : _studyrecData1) {
                if (null != sd._classcd && sd._classcd.equals(classcd) && (!"1".equals(param._useCurriculumcd) || "1".equals(param._useCurriculumcd) && null != sd._schoolKind && sd._schoolKind.equals(schoolKind))) {
                    hasStudyrec = true;
                    sameClassGradesList.addAll(sd.getNotDropGradesList());
                    final String keySubclasscd = sd.keySubclasscd(param);
                    if (!_vNameMstD077List.contains(keySubclasscd)) {
                        isAllD077 = false;
                    }
                    if (!_vNameMstD081List.contains(keySubclasscd)) {
                        isAllD081 = false;
                    }
                    if (!_vNameMstE065List.contains(keySubclasscd)) {
                        isAllE065 = false;
                    }
                }
            }
            boolean isNotPrintAvgGrades = true;
            boolean isAllD065 = true;
            if (hasStudyrec && (isAllD077 || isAllD081 || isAllE065)) {
                isNotPrintAvgGrades = true;
            } else if (sameClassGradesList.isEmpty()) {
                isNotPrintAvgGrades = false;
            } else {
                for (final Grades g : sameClassGradesList) {
                    if (!SqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS.equals(g._recordFlg)) {
                        isNotPrintAvgGrades = false;
                        break;
                    }
                }
                for (final Grades g : sameClassGradesList) {
                    if (null == g._d065Flg) {
                        isAllD065 = false;
                    }
                }
                if (isAllD065) {
                    isNotPrintAvgGrades = true;
                }
            }
            if (_isFuhakkou) {
                isNotPrintAvgGrades = true;
            }
            if (param._isOutputDebugSeiseki) {
                log.info(" NotPrintAvgGrades "  + classcd + "-" + schoolKind + " : D077? " + isAllD077 + ", D081 ? " + isAllD081 + ", D065? " + isAllD065 + ", E065? " + isAllE065 + ", fuhakkou? " + _isFuhakkou);
            }
            return isNotPrintAvgGrades;
        }

        private List<HyoteiHeikin> getHyoteiHeikinList(final Param param) {
            final List<StudyrecDat> printTargetStudyrecDatList = getPrintTargetStudyrecDatList(param);

            final Map<String, HyoteiHeikin> classKeyHyoteiHeikinMap = new TreeMap<String, HyoteiHeikin>();
            final List<HyoteiHeikin> hyoteiHeikinList = new ArrayList<HyoteiHeikin>();
            for (final StudyrecDat studyrecDat : printTargetStudyrecDatList) {
                final String specialDiv;
                if ("1".equals(_notUseClassMstSpecialDiv)) {
                    specialDiv = "X";
                } else {
                    specialDiv = StringUtils.defaultString(studyrecDat._specialDiv, "XX");
                }

                final String keyClasscd = studyrecDat.keyClasscd(param);
                final String classKey = specialDiv + ":" + keyClasscd;
                if (classKeyHyoteiHeikinMap.containsKey(classKey)) {
                    final HyoteiHeikin hyoteiHeikin = classKeyHyoteiHeikinMap.get(classKey);
                    hyoteiHeikin._gradesList.addAll(studyrecDat._gradesList);
                } else {

                    final String classcd;
                    final String schoolKind;
                    if ("1".equals(param._useCurriculumcd) && -1 != keyClasscd.indexOf('-')) {
                        classcd = keyClasscd.substring(0, keyClasscd.indexOf('-'));
                        schoolKind = keyClasscd.substring(keyClasscd.indexOf('-') + 1);
                    } else {
                        classcd = keyClasscd;
                        schoolKind = "";
                    }

                    //  各教科の評定平均値の出力
                    if (isNotPrintAvgGrades(param, classcd, schoolKind)) {
                        if (param._isOutputDebugSeiseki) {
                            log.info(" not print avg grades : " + classcd + "-" + schoolKind);
                        }
                    } else {
                        if (param._isOutputDebugSeiseki) {
                            log.info(" avgGrades " + keyClasscd + " " + studyrecDat._classname + " = " + studyrecDat._avgGrades);
                        }
                        final Map<String, String> avgRec = new HashMap<String, String>();
                        avgRec.put("CLASSCD", classcd + "-" + schoolKind);
                        avgRec.put("CLASSNAME", studyrecDat._classname);
                        avgRec.put("AVG", studyrecDat._avgGrades);
                        final HyoteiHeikin hyoteiHeikin = new HyoteiHeikin(avgRec);
                        hyoteiHeikinList.add(hyoteiHeikin);
                        hyoteiHeikin._gradesList.addAll(studyrecDat._gradesList);
                        classKeyHyoteiHeikinMap.put(classKey, hyoteiHeikin);
                    }
                }
            }
            return hyoteiHeikinList;
        }

        private static String getStaffcd(final DB2UDB db2, final String schregno, final String year, final String semes, final String paramStaffCd, final Map paramap, final boolean isPrintGrd) {
            String staffCd = null;
            if ("1".equals(paramap.get("tyousasyoPrintHomeRoomStaff"))) {
                final String tableRegd = isPrintGrd ? "GRD_REGD_DAT" : "SCHREG_REGD_DAT";
                final String tableRegdH = isPrintGrd ? "GRD_REGD_HDAT" : "SCHREG_REGD_HDAT";

                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("       TR_CD1 ");
                stb.append("   FROM ");
                stb.append("       " + tableRegdH + " REG_H ");
                stb.append("       INNER JOIN " + tableRegd + " REGD ON REG_H.YEAR = REGD.YEAR ");
                stb.append("           AND REG_H.SEMESTER = REGD.SEMESTER ");
                stb.append("           AND REG_H.GRADE = REGD.GRADE ");
                stb.append("           AND REG_H.HR_CLASS = REGD.HR_CLASS");
                // 卒業年学期の在籍ヘッダデータ
                stb.append("       INNER JOIN SCHREG_REGD_GDAT REGDG ON REG_H.YEAR = REGDG.YEAR ");
                stb.append("           AND REG_H.GRADE = REGDG.GRADE ");
                stb.append("       INNER JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON REGD.SCHREGNO = ENTGRD.SCHREGNO ");
                stb.append("           AND REGDG.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ");
                stb.append("       INNER JOIN SEMESTER_MST SEME ON REG_H.YEAR = SEME.YEAR ");
                stb.append("           AND REG_H.SEMESTER = SEME.SEMESTER ");
                stb.append("           AND ENTGRD.GRD_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                stb.append("       INNER JOIN " + tableRegd + " CTRL_REGD ON REGD.SCHREGNO = CTRL_REGD.SCHREGNO ");
                stb.append("           AND CTRL_REGD.YEAR = '" + year + "' ");
                stb.append("           AND CTRL_REGD.SEMESTER = '" + semes + "' ");
                stb.append("       INNER JOIN SCHREG_REGD_GDAT CTRL_REGDG ON CTRL_REGD.YEAR = CTRL_REGDG.YEAR ");
                stb.append("           AND CTRL_REGD.GRADE = CTRL_REGDG.GRADE ");
                stb.append("           AND REGDG.SCHOOL_KIND = CTRL_REGDG.SCHOOL_KIND ");
                stb.append("   WHERE ");
                stb.append("       REGD.SCHREGNO = '" + schregno + "' ");

                staffCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

                if (null == staffCd) {
                    final StringBuffer stb2 = new StringBuffer();
                    stb2.append("   SELECT ");
                    stb2.append("       TR_CD1 ");
                    stb2.append("   FROM ");
                    stb2.append("       " + tableRegdH + " REG_H ");
                    stb2.append("       INNER JOIN " + tableRegd + " REGD ON REG_H.YEAR = REGD.YEAR ");
                    stb2.append("           AND REG_H.SEMESTER = REGD.SEMESTER ");
                    stb2.append("           AND REG_H.GRADE = REGD.GRADE ");
                    stb2.append("           AND REG_H.HR_CLASS = REGD.HR_CLASS");
                    // 指定学期の在籍ヘッダデータ
                    stb2.append("   WHERE ");
                    stb2.append("       REGD.SCHREGNO = '" + schregno + "' ");
                    stb2.append("       AND REGD.YEAR = '" + year + "' ");
                    stb2.append("       AND REGD.SEMESTER = '" + semes + "' ");

                    staffCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb2.toString()));
                }
            } else {
                staffCd = paramStaffCd;
            }
            return staffCd;
        }

        public String getSogoSubclassname(final Param param) {
            if (param._isShusyokuyouKinkiToitsu && _output == PrintData._shusyokuYou) {
                return SOGOTEKI_NA_GAKUSHU_TANKYU_NO_JIKAN;
            }
            final int tankyuStartYear = Util.toInt(property(Property.sogoTankyuStartYear), 2019);
            boolean isTankyu = false;
            final int year = NumberUtils.isDigits(_ctrlYear) ? Integer.parseInt(_ctrlYear) : 0;
            int gradeCdInt = 0;
            if (null != _personInfo) {
                gradeCdInt = NumberUtils.isDigits(_personInfo._regdGradeCd) ? Integer.parseInt(_personInfo._regdGradeCd) : 0;
                if (NumberUtils.isDigits(_personInfo._curriculumYear)) {
                    isTankyu = Integer.parseInt(_personInfo._curriculumYear) >= tankyuStartYear;
                } else {
                    if (year == tankyuStartYear     && gradeCdInt <= 1
                            || year == tankyuStartYear + 1 && gradeCdInt <= 2
                            || year == tankyuStartYear + 2 && gradeCdInt <= 3
                            || year >= tankyuStartYear + 3
                            ) {
                        isTankyu = true;
                    }
                }
            }
            if (param._isOutputDebug) {
                param.logOnce(" 探究? " + isTankyu + ", year = " + year + ", gradeCdInt = " + gradeCdInt + ", curriculumYear = " + (null == _personInfo ? null : _personInfo._curriculumYear));
            }
            return isTankyu ? SOGOTEKI_NA_TANKYU_NO_JIKAN : SOGOTEKI_NA_GAKUSHU_NO_JIKAN;
        }


        private static String getAttestInkanMap(final DB2UDB db2, final Param param, final String staffcd, final String date) {
            if (null == staffcd) {
                return null;
            }
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     STAMP_NO ");
            sql.append(" FROM ATTEST_INKAN_DAT ");
            sql.append(" WHERE ");
            sql.append("     STAFFCD = '" + staffcd + "' ");
            sql.append(" ORDER BY ");
            if (null != date) {
                sql.append("  CASE ");
                sql.append("     WHEN '" + date.replace('/', '-') + "' BETWEEN START_DATE AND STOP_DATE THEN 0 ");
                sql.append("     WHEN '" + date.replace('/', '-') + "' BETWEEN START_DATE AND VALUE(STOP_DATE, '9999-12-31') THEN 1 ");
                sql.append("  END, ");
            }
            sql.append("   START_DATE DESC ");
            sql.append(" , STAMP_NO ");

            if (param._isOutputDebugQuery) {
                log.info(" kisai sql = " + sql);
            }

            final String stampNo = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
            if (param._isOutputDebug) {
                log.info(" kisai stampno (" + staffcd + ") = " + stampNo);
            }
            if (null == stampNo) {
                return null;
            }
            return param.getImageFilePath("/stamp/" + stampNo + ".bmp");
        }
    }

    public static class HyoteiHeikin {
        public final Map<String, String> _row;
        public final List<Grades> _gradesList = new ArrayList<Grades>();
        public HyoteiHeikin(final Map<String, String> row) {
            _row = row;
        }
        public String classkey() {
            return (String) _row.get("CLASSCD");
        }
        public String classname() {
            return (String) _row.get("CLASSNAME");
        }
        public String avg() {
            return (String) _row.get("AVG");
        }
        public String gaihyo() {
            return (String) _row.get("GAIHYO");
        }
        public String credit() {
            return (String) _row.get("CREDIT");
        }
        private Map<String, List<String>> gradesMap() {
            final Map<String, List<String>> map = new HashMap<String, List<String>>();
            for (final Grades grades : _gradesList) {
                Util.getMappedList(map, grades._year).add("(g=" + grades._grades + ", p=" + grades._provFlg + ")");
            }
            return map;
        }
        public String toString() {
            return "HyoteiHeikin(" + _row + ", grades = " + gradesMap() + ")";
        }
    }

    /**
     *  学校教育システム 賢者 [進路情報管理]  高校用調査書  就職用
     */
    public static class KNJE070_2P extends KNJE070_1 {

        public KNJE070_2P(final DB2UDB db2, final Vrw32alpWrap svf, final KNJDefineSchool definecode, final String useSyojikou3) {
            this(db2, svf, (List) null);
        }

        public KNJE070_2P(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode, final String useSyojikou3) {
            this(db2, svf, (List) null);
        }

        public KNJE070_2P(final DB2UDB db2, final List outputCsvLines, final KNJDefineSchool definecode, final String useSyojikou3) {
            this(db2, null, outputCsvLines);
        }

        private KNJE070_2P(
                final DB2UDB db2,
                final Vrw32alp svf,
                final List<List<String>> outputCsvLines
        ){
            super(db2, svf, outputCsvLines);
            _isE070_2 = true;
        }
    }

    private static class FormKNJE070_2 extends IForm {

        private static String FIELD_VOLUNTEERNAME = "VOLUNTEERNAME";
        private static String FIELD_NOTICE = "NOTICE";
        private static String FIELD_NOTICE2 = "NOTICE2";
        private final FormInfoKNJE070_2 _formInfo;

        private boolean nonedata;
        public FormKNJE070_2(final Param param, final PrintData printData) {
            super(param);

            _formInfo = new FormInfoKNJE070_2(_param, printData);
        }

        private static class FormInfoKNJE070_2 extends FormInfo {
            // 就職用
            boolean _isShusyokuyouFormFormat2;
            private boolean _useFormKNJE070_3 = false;
            private boolean _isShushokuyouKenja = false;
            private boolean _isShushokuyouShokenRepeat = false;
            private boolean _shushokuyouCreditFooterIsRecord;
            private int _shushokuyouCreditFooterHeightCount;

            final String FLG_NAME_KETA = "FLG_NAME_KETA";
            final String FLG_MIYAGIKEN_SHUSHOKU_GAPPEI_TOUGOU = "MIYAGIKEN_SHUSHOKU_GAPPEI_TOUGOU";
            final String FLG_TEXT_TANKYU1 = "TEXT_TANKYU1";
            final String FLG_SOGAKU_CREDIT_SLASH = "SOGAKU_CREDIT_SLASH";
            final String FLG_RYUGAKU_CREDIT_SLASH = "RYUGAKU_CREDIT_SLASH";
            final String FLG_ATTEND_SURA = "ATTEND_SURA";
            final String FLG_KISAI_INEI = "KISAI_INEI";
            final String FLG_MARK_INKAN = "FLG_MARK_INKAN";
            final String FLG_EAR_SLASH = "FLG_EAR_SLASH";
            final String FLG_CREDIT_FIELD_DOT = "FLG_CREDIT_FIELD_DOT";
            final String FLG_NAME_ADDRESS_LINE_DOT = "FLG_NAME_ADDRESS_LINE_DOT";
            final String FLG_STAMP_FIELD_SIZE_MODIFY = "FLG_STAMP_FIELD_SIZE_MODIFY";
            final String FLG_KISAI_STAMP_FIELD_SIZE_MODIFY = "FLG_KISAI_STAMP_FIELD_SIZE_MODIFY";
            final String FLG_ADD_COURSENAME2 = "FLG_ADD_COURSENAME2";
            final String FLG_REMOVE_FIELD_NOTICE_LINKFIELD = "FLG_REMOVE_FIELD_NOTICE_LINKFIELD";

            FormInfoKNJE070_2(final Param param, final PrintData printData) {
                super(param);

                _formNenYou = setFormNenYou(printData, _param);
                if (_param._isOutputDebug) {
                    log.info(" formNenYou = " + _formNenYou);
                }

                _formNen = FormNen.NONE;

                FormKNJE070_2.setSvfForm2(printData, _param, this);
                log.fatal(" form = " + _formName);
                _formNameSetOrigin = _formName;

                _useGradeMultiPage = _param._isShusyokuyouKinkiToitsu2 || printData._isKyoto && printData._isTsushin || param._z010.in(Z010Info.Mieken);
            }

            // 就職用
            public String setConfigFormShushokuyou(final Vrw32alp svf, String formName, final PrintData printData, final Param param, final TitlePage titlePage) {

                SvfForm svfForm = null;
                try {
                    // 就職用
                    final String formPath = svf.getPath(formName);
                    final File formFile = new File(formPath);
                    if (formFile.exists()) {
                        svfForm = new SvfForm(formFile);
                        svfForm._debug = param._isOutputDebugSvfForm;
                        if (!svfForm.readFile()) {
                            svfForm = null;
                        }
                    }
                } catch (Throwable e) {
                    log.error("throwed ", e);
                    svfForm = null;
                } finally {
                    if (null == svfForm) {
                        return formName;
                    }
                }

                final TreeMap<String, String> modifyFormFlgMap = getModifyFormFlgMap(svfForm, printData, param, titlePage);

                if (modifyFormFlgMap.isEmpty()) {
                    return formName;
                }
                String modifyFormKey = formName + "::" + Util.mkString(modifyFormFlgMap, "|");
                log.info(" modifyFormKeys = " + modifyFormFlgMap);

                if (param.modifyFormPathMap().containsKey(modifyFormKey)) {
                    File file = param.modifyFormPathMap().get(modifyFormKey);
                    if (null == file) {
                        throw new IllegalArgumentException(" cache form null : " + param.modifyFormPathMap());
                    }
                    formName = file.getName();
                    return formName;
                }
                File newFile = null;
                try {
                    // 就職用
                    modifySvfFormShushoku(printData, param, modifyFormFlgMap, svfForm);

                    newFile = svfForm.writeTempFile();
                    log.info(" create new file " + newFile);
                } catch (Throwable e) {
                    log.error("throwed ", e);
                }
                File newFormFile = null;
                if (null != newFile) {
                    newFormFile = newFile;
                }
                param.modifyFormPathMap().put(modifyFormKey, newFormFile);
                if (null != newFormFile && !newFormFile.getName().equals(formName)) {
                    formName = newFormFile.getName();
                }
                return formName;
            }

            private void modifySvfFormShushoku(final PrintData printData, final Param param, final TreeMap<String, String> modifyFormFlgMap, final SvfForm svfForm) {
                if (modifyFormFlgMap.containsKey(FLG_ADD_COURSENAME2)) {
                    // 就職用 課程2
                    final SvfForm.Field kateiField = svfForm.getField("katei");
                    final int x = kateiField._position._x;
                    final int y = kateiField._position._y;
                    final int endX = kateiField._endX;
                    final List<SvfForm.Field> list = Arrays.asList(
                            kateiField.copyTo("katei3_1").setCharPoint10(80).setFieldLength(6).setX(x + 20).setEndX(endX - 20).setY(y - 39).setPrintMethod(PrintMethod.HIDARITSUME).setLinkFieldname("katei3_2"),
                            kateiField.copyTo("katei3_2").setCharPoint10(80).setFieldLength(6).setX(x + 20).setEndX(endX - 20).setY(y + 6).setPrintMethod(PrintMethod.HIDARITSUME).setLinkFieldname("katei3_3"),
                            kateiField.copyTo("katei3_3").setCharPoint10(80).setFieldLength(6).setX(x + 20).setEndX(endX - 20).setY(y + 50).setPrintMethod(PrintMethod.HIDARITSUME)
                            );
                    for (final SvfForm.Field field : list) {
                        svfForm.addField(field);
                    }
                }

                if (modifyFormFlgMap.containsKey(FLG_NAME_KETA)) {
                    // 就職用 氏名桁対応
                    final Field nameField = svfForm.getField("NAME");
                    final List<SvfForm.Field> list = Arrays.asList(
                            nameField.copyTo("NAME_2").setCharPoint10(52).setFieldLength(46).setY(nameField._position._y + 14),
                            nameField.copyTo("NAME_3").setCharPoint10(50).setFieldLength(50).setY(nameField._position._y + 15),
                            nameField.copyTo("NAME_4").setCharPoint10(44).setFieldLength(56).setY(nameField._position._y + 16),
                            nameField.copyTo("NAME_5").setCharPoint10(42).setFieldLength(60).setY(nameField._position._y + 17),
                            nameField.copyTo("NAME_6").setCharPoint10(38).setFieldLength(66).setY(nameField._position._y + 18),
                            nameField.copyTo("NAME_7").setCharPoint10(35).setFieldLength(70).setY(nameField._position._y + 19),
                            nameField.copyTo("NAME_8").setCharPoint10(32).setFieldLength(76).setY(nameField._position._y + 20),
                            nameField.copyTo("NAME_9").setCharPoint10(30).setFieldLength(80).setY(nameField._position._y + 21)
                            );
                    for (final SvfForm.Field field : list) {
                        svfForm.addField(field);
                    }
                }

                // 就職用 代替備考フィールドのリンクフィールドカット
                if (modifyFormFlgMap.containsKey(FLG_REMOVE_FIELD_NOTICE_LINKFIELD)) {
                    final SvfForm.Field fieldNotice = svfForm.getField(FormKNJE070_2.FIELD_NOTICE);
                    svfForm.removeField(fieldNotice);
                    svfForm.addField(fieldNotice.setLinkFieldname(""));
                }

                if ("1".equals(modifyFormFlgMap.get(FLG_MIYAGIKEN_SHUSHOKU_GAPPEI_TOUGOU))) {
                    // 就職用　宮城県就職用で合併統合用フォームに置き換え
                    final Field enterDate = svfForm.getField("ENTERDATE");
                    final Line line0 = svfForm.getNearestUpperLine(enterDate._position);
                    final Line line1 = svfForm.getNearestLowerLine(enterDate._position);
                    final Field transferDate = svfForm.getField("TRANSFER_DATE");
                    final Line line2 = svfForm.getNearestLowerLine(transferDate._position);
                    final int height = line2._start._y - line0._start._y;
                    svfForm.addLine(new SvfForm.Line(line1._lineWidth, line1._start, line1._end).addY(line0._start._y + height / 3 * 1 - line1._start._y));
                    svfForm.addLine(new SvfForm.Line(line1._lineWidth, line1._start, line1._end).addY(line0._start._y + height / 3 * 2 - line1._start._y));
                    svfForm.removeLine(line1);
                }

                if (modifyFormFlgMap.containsKey(FLG_NAME_ADDRESS_LINE_DOT)) {
                    // 就職用 生徒かなと氏名の境界線、住所の境界線は破線
                    final SvfForm.Field fieldKana = svfForm.getField("kana");
                    final SvfForm.Line kanaNameSplitLine = svfForm.getNearestLowerLine(fieldKana._position);

                    final SvfForm.Field fieldGuardAddress1 = svfForm.getField("GUARD_ADDRESS1");
                    final SvfForm.Line fieldGuardAddressSplitLine = svfForm.getNearestLowerLine(fieldGuardAddress1._position);

                    for (final SvfForm.Line source : Arrays.asList(kanaNameSplitLine, fieldGuardAddressSplitLine)) {
                        final int length = 10;
                        final int startx = source._start._x;
                        final int endx = source._end._x;
                        final int y = source._start._y;
                        svfForm.removeLine(source);
                        for (int x = startx; x < endx;) {
                            final int linelen = x == startx ? length / 2 : length;
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.width(1), pt(x, y), pt(Math.min(x + linelen, endx), y)));
                            final int gaplen = length;
                            x += linelen + gaplen;
                        }
                    }
                }

                if ("1".equals(modifyFormFlgMap.get(FLG_TEXT_TANKYU1))) {
                    // 就職用　「総合的な探究の時間」に置き換え
                    final int keta = getMS932ByteLength(printData.getSogoSubclassname(param)) - getMS932ByteLength(PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN);
                    for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN)) {
                        svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(printData.getSogoSubclassname(param)).addX(- 11 * keta));
                    }
                }

                // 就職用　総合的な学習の時間単位数に斜線
                if (modifyFormFlgMap.containsKey(FLG_SOGAKU_CREDIT_SLASH)) {
                    final Field field = svfForm.getField("TOTALSTUDY");
                    final Repeat repeat = svfForm.getRepeat(field._repeatConfig._repeatNo);
                    //log.info(" sogaku field = " + field + ", " + repeat._count);
                    final Line upper = svfForm.getNearestUpperLine(field._position);
                    final Line lower = svfForm.getNearestLowerLine(field._position);
                    for (final String n : modifyFormFlgMap.get(FLG_SOGAKU_CREDIT_SLASH).split("_")) {
                        final Point newpos = field._position.addX((Integer.parseInt(n) - 1) * repeat._pitch);
                        final Line left = svfForm.getNearestLeftLine(newpos);
                        final Line right = svfForm.getNearestRightLine(newpos);
                        final Line l = new Line(pt(right._start._x, upper._start._y), pt(left._end._x, lower._start._y));
                        svfForm.addLine(l);
                    }
                }

                // 就職用　留学単位数に斜線
                if (modifyFormFlgMap.containsKey(FLG_RYUGAKU_CREDIT_SLASH)) {
                    final Field field = svfForm.getField("ABROAD");
                    final Repeat repeat = svfForm.getRepeat(field._repeatConfig._repeatNo);
                    //log.info(" ryugaku field = " + field + ", " + repeat._count + " / pitch = " + repeat._pitch);
                    final Line upper = svfForm.getNearestUpperLine(field._position);
                    final Line lower = svfForm.getNearestLowerLine(field._position);
                    for (final String n : modifyFormFlgMap.get(FLG_RYUGAKU_CREDIT_SLASH).split("_")) {
                        final Point newpos = field._position.addX((Integer.parseInt(n) - 1) * repeat._pitch);
                        final Line left = svfForm.getNearestLeftLine(newpos);
                        final Line right = svfForm.getNearestRightLine(newpos);
                        final Line l = new Line(pt(right._start._x, upper._start._y), pt(left._end._x, lower._start._y));
                        svfForm.addLine(l);
                    }
                }
                // 就職用　出欠欄スラッシュ
                if (modifyFormFlgMap.containsKey(FLG_ATTEND_SURA)) {
                    if (shokenType5() == FormInfo.ShokenType.TYPE_FIELDAREA_REPEAT) {
                    } else {
                        for (final String pos : modifyFormFlgMap.get(FLG_ATTEND_SURA).split("_")) {
                            addSlashLineOfFieldn(param, svfForm, "absence", pos);
                        }
                    }
                }
                // 就職用 身体状況聴力
                if (modifyFormFlgMap.containsKey(FLG_EAR_SLASH)) {
                    for (final String lr : modifyFormFlgMap.get(FLG_EAR_SLASH).split("_")) {
                        addSlashLineOfField(svfForm, lr + "_EAR");
                    }
                }
                // 就職用　記載責任者印影追加
                if (modifyFormFlgMap.containsKey(FLG_KISAI_INEI)) {

                    final String stampSizeMm = defstr(defval(Util.toBigDecimalList(Arrays.asList(printData.property(Property.knje070_2KisaiStampSizeMm), printData.property(Property.staffStampSizeMm)))), (String) null);
                    final String stampPositionXmm = printData.property(Property.knje070_2KisaiStampPositionXmm);
                    final String stampPositionYmm = printData.property(Property.knje070_2KisaiStampPositionYmm);
                    if (null == svfForm.getImageField("KISAI_STAMP")) {
                        final String imageNo = String.valueOf(svfForm.getElementList(SvfForm.ImageField.class).size() + 1);
                        final int width = NumberUtils.isNumber(stampSizeMm) ? Util.mmToDot(stampSizeMm) : 155;
                        final int height = NumberUtils.isNumber(stampSizeMm) ? Util.mmToDot(stampSizeMm) : 155;

                        final int centerX = NumberUtils.isNumber(stampPositionXmm) ? Util.mmToDot(stampPositionXmm) : 4250;
                        final int centerY = NumberUtils.isNumber(stampPositionYmm) ? Util.mmToDot(stampPositionYmm) : 2240;
                        final SvfForm.Point imageXY = pt(centerX - width / 2, centerY - height / 2);
                        SvfForm.ImageField kisaiStampField = null;

                        final SvfForm.ImageField stampField = svfForm.getImageField("STAMP");
                        if (null != stampField) {
                            try {
                                kisaiStampField = stampField.copyTo("KISAI_STAMP").setNo(imageNo).setPoint(imageXY).setEndX(imageXY._x + width).setHeight(height);
                                svfForm.addImageField(kisaiStampField);
                            } catch (Throwable t) {
                                log.error(t.getMessage());
                                kisaiStampField = null;
                            }
                        }
                        if (null == kisaiStampField) {
                            kisaiStampField = new SvfForm.ImageField(imageNo, "KISAI_STAMP", imageXY, imageXY._x + width, height, "0", "0");
                            svfForm.addImageField(kisaiStampField);
                        }
                        log.info(" add field KISAI_STAMP");
                    } else {
                        if (Util.toInt(stampSizeMm, 0) > 0 || NumberUtils.isNumber(stampPositionXmm) || NumberUtils.isNumber(stampPositionYmm)) {
                            resizeStampImage(printData, svfForm, "KISAI_STAMP", stampSizeMm, stampPositionXmm, stampPositionYmm);
                        }
                    }
                }
                // 就職用　"印"と□
                if (modifyFormFlgMap.containsKey(FLG_MARK_INKAN)) {
                    // "印"
                    final SvfForm.KoteiMoji inkan = new SvfForm.KoteiMoji("印", pt(4226, 2868), 90).setFText(true);
                    svfForm.addKoteiMoji(inkan);
                    // □
                    final SvfForm.Point upperLeft = pt(4212, 2854);
                    final SvfForm.Point lowerRight = pt(4290, 2932);
                    final SvfForm.Box inkanBox = new SvfForm.Box(SvfForm.LineKind.DOTTED1, SvfForm.LineWidth.THIN, upperLeft, lowerRight);
                    svfForm.addBox(inkanBox);
                }

                // 就職用 単位数に小数点つける
                if (modifyFormFlgMap.containsKey(FLG_CREDIT_FIELD_DOT)) {
                    for (int g = 1; g <= 6; g++) {
                        SvfForm.Field field = svfForm.getField("GRADES" + String.valueOf(g));
                        if (null != field) {
                            svfForm.addField(field.copyTo("GRADES" + String.valueOf(g) + "_DOT").setFieldLength(5).setCharPoint10(65).setHenshuShiki("").setY(field._position._y + 4).setX(field._position._x - 3));
                        }
                    }
                }

                // 就職用 印影サイズ修正
                if (modifyFormFlgMap.containsKey(FLG_STAMP_FIELD_SIZE_MODIFY)) {
                    final String stampSizeMm = defstr(defval(param._z010.in(Z010Info.KaichiNihonbashi) ? new BigDecimal(21) : null, Util.toBigDecimalList(Arrays.asList(printData.property(Property.knje070_2StampSizeMm), printData.property(Property.stampSizeMm)))), (String[]) null);
                    final String stampPositionXmm = printData.property(Property.knje070_2StampPositionXmm);
                    final String stampPositionYmm = printData.property(Property.knje070_2StampPositionYmm);

                    if (Util.toInt(stampSizeMm, 0) > 0 || NumberUtils.isNumber(stampPositionXmm) || NumberUtils.isNumber(stampPositionYmm)) {
                        resizeStampImage(printData, svfForm, "STAMP", stampSizeMm, stampPositionXmm, stampPositionYmm);
                    }
                }
            }

            private TreeMap<String, String> getModifyFormFlgMap(final SvfForm svfForm, final PrintData printData, final Param param, final TitlePage titlePage) {
                final TreeMap<String, String> modifyFormFlgMap = new TreeMap<String, String>();
                if (null != printData._personInfo && getTextKeta(printData._personInfo.getPrintName()) > 40) {
                    modifyFormFlgMap.put(FLG_NAME_KETA, "1");
                }
                if (null != svfForm.getField("katei") && null != printData._personInfo && getMS932ByteLength(printData._personInfo._coursename) > svfForm.getField("katei")._fieldLength) {
                    modifyFormFlgMap.put(FLG_ADD_COURSENAME2, "1");
                }
                if (printData.miyagikenShushokuyouUseGappeiTougouForm(param)) {
                    modifyFormFlgMap.put(FLG_MIYAGIKEN_SHUSHOKU_GAPPEI_TOUGOU, "1");
                }
                if (!PrintData.SOGOTEKI_NA_GAKUSHU_NO_JIKAN.equals(printData.getSogoSubclassname(param))) {
                    modifyFormFlgMap.put(FLG_TEXT_TANKYU1, "1");
                }
                SvfForm.Field fieldNotice = svfForm.getField(FormKNJE070_2.FIELD_NOTICE);
                if (null != fieldNotice && null != fieldNotice._linkFieldname) {
                    modifyFormFlgMap.put(FLG_REMOVE_FIELD_NOTICE_LINKFIELD, "1");
                }
                if (printData._isKisaiPrintStamp && null != printData._kisaiStampImagePath) {
                    modifyFormFlgMap.put(FLG_KISAI_INEI, "1");
                }
                if (printData._isConfigFormAttendAllSlash) {
                    final List<String> attendSuraPosList = new ArrayList<String>();
                    for (final Title title : titlePage._titleList) {
                        final AttendrecDat att = printData.getAttendrecDat(title._year);
                        final boolean attendIsSlash = AttendrecDat.isNull(att, printData);
                        if (attendIsSlash) {
                            attendSuraPosList.add(String.valueOf(title._seisekiPos));
                        }
                    }
                    if (!attendSuraPosList.isEmpty()) {
                        modifyFormFlgMap.put(FLG_ATTEND_SURA, Util.mkString(attendSuraPosList, "_"));
                    }
                }
                if (printData._isConfigFormEarNullSlash) {
                    final MedexamDetDat medexam = printData._medexamDetDat;
                    final List<String> slashLR = new ArrayList<String>();
                    if (null == medexam.lEar() || medexam.isEarSlash(param, medexam.lEar())) {
                        slashLR.add("L");
                    }
                    if (null == medexam.rEar() || medexam.isEarSlash(param, medexam.rEar())) {
                        slashLR.add("R");
                    }
                    if (slashLR.size() > 0) {
                        modifyFormFlgMap.put(FLG_EAR_SLASH, Util.mkString(slashLR, "_"));
                    }
                }
//                if (param._z010.in(Z010Info.Miyagiken)) {
//                	final List<String> sogakuSlashKey = new ArrayList<String>();
//                	if (printData.sogoIsSuraShushokuyou("configFormShushokuyou", param, null)) {
//                		//final Map<String, String> shushokuYouSogoCreditYear = printData.shushokuYouSogoCreditYear();
//                		for (final Title title : titlePage._titleList) {
//                			//final boolean noCredit = !shushokuYouSogoCreditYear.containsKey(title._year);
//                			final boolean noCredit = true; // 1度でも代替があればすべてスラッシュ
//                			//log.info(" sogo no credit : " + noCredit + ", " + title._year + " in " + shushokuYouSogoCreditYear);
//    						if (noCredit) {
//                				sogakuSlashKey.add(String.valueOf(title._seisekiPos));
//                			}
//                		}
//                	}
//                	if (!sogakuSlashKey.isEmpty()) {
//                		modifyFormFlgMap.put(FLG_SOGAKU_CREDIT_SLASH, Util.mkString(sogakuSlashKey, "_"));
//                	}
//
//                	final List<String> ryugakuSlashKey = new ArrayList<String>();
//            		final Map<String, String> shushokuYouRyugakuCreditYear = printData.shushokuYouRyugakuCreditYear();
//            		for (final Title title : titlePage._titleList) {
//            			final boolean noCredit = !shushokuYouRyugakuCreditYear.containsKey(title._year);
//    					if (noCredit && !"1".equals(printData._paramap.get(Parameter.RYUGAKU_CREDIT._name)) && !"1".equals(printData.getParameter(Parameter.TANIPRINT_RYUGAKU))) {
//            				ryugakuSlashKey.add(String.valueOf(title._seisekiPos));
//            			}
//            		}
//            		if (!ryugakuSlashKey.isEmpty()) {
//            			modifyFormFlgMap.put(FLG_RYUGAKU_CREDIT_SLASH, Util.mkString(ryugakuSlashKey, "_"));
//            		}
//                }

                if (param._isShusyokuyouKinkiToitsu2) {
                    modifyFormFlgMap.put(FLG_NAME_ADDRESS_LINE_DOT, "1");
                }

                if (param._setSogakuKoteiTanni) {
                    modifyFormFlgMap.put(FLG_CREDIT_FIELD_DOT, "1");
                }

                if (param._z010.in(Z010Info.Mieken) || "1".equals(printData.property(Property.tyousasyoShushokuyouShowTextInn))) {
                    modifyFormFlgMap.put(FLG_MARK_INKAN, "1");
                }

                if (printData._isPrintStamp) {
                    modifyFormFlgMap.put(FLG_STAMP_FIELD_SIZE_MODIFY, "1");
                }

                if (printData._isKisaiPrintStamp) {
                    modifyFormFlgMap.put(FLG_KISAI_STAMP_FIELD_SIZE_MODIFY, "1");
                }
                return modifyFormFlgMap;
            }

            public void setSvfForm(final Vrw32alp svf, final String _formName, final IForm iform, final Param param) {
                _guardAddress1EndX = -1;
                try {

                    final String formPath = svf.getPath(_formName);
                    final File formFile = new File(formPath);
                    if (formFile.exists()) {
                        SvfForm svfForm = new SvfForm(formFile);
                        svfForm._debug = param._isOutputDebugSvfForm;
                        if (svfForm.readFile()) {
                            final SvfForm.Field creditSubclassnameField = svfForm.getField("CREDIT_SUBCLASSNAME");
                            if (null != creditSubclassnameField) {
                                final SvfForm.Record record = svfForm.getRecordOfField(creditSubclassnameField);
                                if (null != record) {
                                    _shushokuyouCreditFooterIsRecord = true;
                                    final SvfForm.Record creditFooterTitleRecord = svfForm.getRecordOfField(svfForm.getField("CREDIT_FOOTER_TITLE_DUMMY1"));
                                    final SvfForm.Record subclasssnameFieldRecord = svfForm.getRecordOfField(svfForm.getField("SUBCLASSNAME"));
                                    _shushokuyouCreditFooterHeightCount = creditFooterTitleRecord.getHeight() / subclasssnameFieldRecord.getHeight();
                                }
                            }

                            SvfForm.Field guardAddress1 = svfForm.getField("GUARD_ADDRESS1");
                            if (null != guardAddress1) {
                                _guardAddress1EndX = guardAddress1._endX;
                            }
                        }
                    }
                    if (param._isOutputDebugField) {
                        log.info(" _shushokuyouCreditFooterIsRecord = " + _shushokuyouCreditFooterIsRecord);
                        log.info(" _guardAddress1EndX = " + _guardAddress1EndX);
                    }
                } catch (Throwable t) {
                    log.error("catch throwed : _formName = " + _formName, t);
                }
            }
        }

        public FormInfo getFormInfo() {
            return _formInfo;
        }

        // 就職用
        private boolean printMain2(final DB2UDB db2, final Vrw32alp svf, final PrintData printData, final TitlePage titlePage) throws FileNotFoundException {
            _printData = printData;
            _isCsv = _printData.isCsv();
            _svf = svf;
            setForm(_svf, _formInfo._formName, 4, param());

            final List<StudyrecDat> studyrecDataShushokuyou = getStudyrecDataShushoku(db2, printData, param());
            if (_printData.notUseStudyrec(param())) {
                _printData._studyrecDataShushokuyou = Collections.emptyList(); // 成績を表示しない。ただし改行して線を表示する。;
            } else {
                _printData._studyrecDataShushokuyou = studyrecDataShushokuyou;
            }

            _formInfo._formName = _formInfo.setConfigFormShushokuyou(svf, _formInfo._formName, printData, param(), titlePage);
            _formInfo.setSvfForm(_svf, _formInfo._formName, this, param());
            setSvfFieldInfo(_printData, param(), _currentForm);
            setRecordShushokuyou(db2, titlePage);
            int maxGrade = 0;
            for (final StudyrecDat s : _printData._studyrecDataShushokuyou) {
                maxGrade = Math.max(maxGrade, s._gradesList.size());
            }
            final boolean isFormatOnly = false;
            final int maxPage = _printData.formRecord.getMaxPage2();
            for (int pageIdx = 0; pageIdx < maxPage; pageIdx++) {
                final List<FormRecord> recordList = _printData.formRecord.getPageRecordList(pageIdx);
                final boolean isPrintShoken = !_param._isShusyokuyouKinkiToitsu2 || _param._isShusyokuyouKinkiToitsu2 && titlePage._index == 0;

                setForm(_svf, _formInfo._formName, 4, param());
                printData._printProcess = 1;
                if (param().isKindaifuzoku()) {
                    svfVrsOut("SCHREGNO" ,_printData._schregno);
                }
                printGradeTitleShushokuyou(titlePage);
                printData._printProcess = 2;
                printHeadCommon(isFormatOnly);
                printHeadCommon2(isFormatOnly);
                printHeadShushokuyou();
                printData._printProcess = 3;
                printSyoushoNum(true, isFormatOnly);  // 証書番号を出力
                printAddressCommon(db2, isFormatOnly); //氏名、住所等出力
                printData._printProcess = 4;
                if (isPrintShoken) {
                    printAttendShushokuyou(titlePage); //出欠の出力
                }
                printData._printProcess = 6;
                if (isPrintShoken) {
                    printShintaiShushokuyou(db2); // 身体状況の出力
                }
                printData._printProcess = 7;
                if (isPrintShoken) {
                    printShokenShushokuyou(titlePage); //所見の出力
                }
                printData._printProcess = 8;
                printStudyrecShushokuyou(recordList); //学習の記録出力-->VrEndRecord()はここで！
            }
            return nonedata;
        }

        // 就職用
        public boolean csv(final DB2UDB db2, final List<List<String>> csvOutputLines, final PrintData printData, final TitlePage titlePage) {
            _printData = printData;
            _isCsv = _printData.isCsv();
            _csvOutputLines = csvOutputLines;
            final List<StudyrecDat> studyrecData2 = getStudyrecDataShushoku(db2, _printData, param());
            if (_printData.isRemarkOnly(param())) {
                _printData._studyrecDataShushokuyou = Collections.emptyList(); // 成績を表示しない。ただし改行して線を表示する。;
            } else {
                _printData._studyrecDataShushokuyou = studyrecData2;
            }
            setRecordShushokuyou(db2, titlePage);

            final PersonInfo personInfo = _printData._personInfo;
            final Tuple<String[], String[]> nameKanaAndName = getNameKanaAndName(personInfo, 9999, 9999, false);
            final String nameKana = nameKanaAndName._first[0];
            final String name = "1".equals(_printData._kanji) ? nameKanaAndName._second[0] : null;
            final String seibetsu = defstr(param()._z002Abbv1Map.get(personInfo._sex));
            final String addr1 = defstr(personInfo._addr1);
            final String addr2 = "1".equals(personInfo._addrFlg) ? defstr(personInfo._addr2) : null;
            final String coursename = defstr(personInfo._coursename);
            final Tuple<String, String> entGradeAndEnter1 = getEntGradeAndEnter1(db2);
            final Tuple<String, String> printGraduDateAnddGraduName = getPrintGraduDateAndPrintGraduName(db2);
            final List<List<String>> seitoJoho = new ArrayList();
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList(getPrintSyosyoNameLr(false), "", "調査書"));
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList("ふりがな", nameKana, "", "", "", "性別", seibetsu));
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList("氏名", name, "", "", "", "現住所", addr1));
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList("", getPrintBirthdate(db2, personInfo), "", "", "", "", addr2));
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList("学校名", _printData._schoolInfo.t4classification, _printData._schoolInfo.t4schoolname1, _printData._schoolInfo.certifSchoolRemark[6], "", "在学期間", getPrintEntDate(db2), "", entGradeAndEnter1._first, entGradeAndEnter1._second));
            CsvUtils.newLine(seitoJoho).addAll(Arrays.asList("課程名", coursename, "学科名", getPrintMajorname(personInfo), "", "", printGraduDateAnddGraduName._first, "", printGraduDateAnddGraduName._second));

            final List<List<String>> gakushunoKiroku = new ArrayList();
            CsvUtils.newLine(gakushunoKiroku).addAll(Arrays.asList("学習の記録"));
            CsvUtils.newLine(gakushunoKiroku).addAll(Arrays.asList("教科・科目", "", "評定"));
            gakushunoKiroku.addAll(csvGakushunoKirokuShushoku(titlePage));

            final List tokubetsuKatsudounoKiroku = CsvUtils.horizontalUnionLines(setLined(Arrays.asList("特別活動の記録")), setLined(_printData._isFuhakkou ? Collections.emptyList() : Util.getTokenList(_printData._jobHuntRec, tokubetsuKatudouSize(), param())));

            final List<List<String>> shussekiJokyo = new ArrayList();
            CsvUtils.newLine(shussekiJokyo).addAll(Arrays.asList("出席状況"));
            final List<List<String>> shussekiJokyoNissu = new ArrayList<List<String>>();
            final List<String> nissu1 = CsvUtils.newLine(shussekiJokyoNissu);
            final List<String> nissu2 = CsvUtils.newLine(shussekiJokyoNissu);
            for (final Title title : titlePage._titleList) {
                final String key = 0 == Integer.parseInt(title._year) ? "0" : title._year;
                final AttendrecDat att = _printData.getAttendrecDat(key);
                nissu1.add(Util.concatList(title._nameArrayShushoku));
                if (_printData._isFuhakkou) {
                    // 表示しない
                    nissu2.add(null);
                } else if (attendIsSlash(param(), _printData, att)) {
                    nissu2.add(_SLASH);
                } else {
                    nissu2.add(null == att ? null : att.attend6());        //欠席
                }
            }
            shussekiJokyo.addAll(CsvUtils.horizontalUnionLines(setLined(Arrays.asList("欠席日数")), shussekiJokyoNissu));
            shussekiJokyo.addAll(CsvUtils.horizontalUnionLines(setLined(Arrays.asList("欠席の主な理由")), setLined(_printData._isFuhakkou ? Collections.emptyList() : Util.getTokenList(_printData._jobHuntAbsence, kessekinoOmonaRiyuuSize(), param()))));

            final boolean isPrintNum = "1".equals(printData.property(Property.knjf030PrintVisionNumber));
            final List<List<String>> sintaiJokyo = new ArrayList();
            final MedexamDetDat med = _printData._medexamDetDat;
            final String rBarevisionMark = isPrintNum ? med.rBarevision() : med.isPrintVisionMark(param(), med.rBarevisionMark()) ? med.rBarevisionMark() : "";
            final String lBareVisionMark = isPrintNum ? med.lBarevision() : med.isPrintVisionMark(param(), med.lBarevisionMark()) ? med.lBarevisionMark() : "";
            final String rVisionMark = isPrintNum ? med.rVision() : med.isPrintVisionMark(param(), med.rVisionMark()) ? med.rVisionMark() : "";
            final String lVisionMark = isPrintNum ? med.lVision() : med.isPrintVisionMark(param(), med.lVisionMark()) ? med.lVisionMark() : "";
            final boolean isBarevisionLine = "1".equals(param()._f011Namespare2Max);
            final String hyphen = param()._isShusyokuyouKinkiToitsu ? "\u2014" : "----";
            final String rBare;
            final String lBare;
            if (!StringUtils.isBlank(rVisionMark) && isBarevisionLine) {
                rBare = hyphen;
            } else {
                rBare = rBarevisionMark;
            }
            if (!StringUtils.isBlank(lVisionMark) && isBarevisionLine) {
                lBare = hyphen;
            } else {
                lBare = lBareVisionMark;
            }

            final String rEar = med.rEar();
            final String lEar = med.lEar();
            final String outputREar = med.isPrintEar(param(), rEar) ? (med.isEarSlash(param(), rEar) ? _SLASH : med.getEarName(param(), rEar)) : "";
            final String outoutLEar = med.isPrintEar(param(), lEar) ? (med.isEarSlash(param(), lEar) ? _SLASH : med.getEarName(param(), lEar)) : "";

            CsvUtils.newLine(sintaiJokyo).addAll(Arrays.asList("身体状況", "", "", "", "", "検査日・" + defstr(KNJ_EditDate.h_format_JP_M(db2, getString(med._row, "DATE")))));
            List<List<String>> sintaiJokyoShoken = new ArrayList<List<String>>();
            CsvUtils.newLine(sintaiJokyoShoken).addAll(Arrays.asList("身長", defstr(getString(med._row, "HEIGHT")) + " cm", "視力", "右　" + defstr(rBare, " ") + " (" + defstr(rVisionMark, "    ") + ")", "聴力", "右　" + defstr(outputREar), "備考"));
            CsvUtils.newLine(sintaiJokyoShoken).addAll(Arrays.asList("体重", defstr(getString(med._row, "WEIGHT")) + " kg", "　　", "左　" + defstr(lBare, " ") + " (" + defstr(lVisionMark, "    ") + ")", "　　", "左　" + defstr(outoutLEar), "　　"));
            sintaiJokyoShoken = CsvUtils.horizontalUnionLines(sintaiJokyoShoken, setLined(_printData._isFuhakkou ? Collections.emptyList() : Util.getTokenList(_printData._jobHuntHealthremark, sintaiJokyoBikouSize(), param()))); // 備考
            sintaiJokyo.addAll(sintaiJokyoShoken);

            final List tyoushoSuisenjiyu = CsvUtils.horizontalUnionLines(setLined(Arrays.asList("本人の長所・推薦事由等")), setLined(_printData._isFuhakkou ? Collections.emptyList() : Util.getTokenList(_printData._jobHuntRecommend, tyoushoSuisenjiyuSize(), param())));

            final List<List<String>> footer = new ArrayList();
            CsvUtils.newLine(footer).addAll(Arrays.asList("記載者", "", defstr(getJobname2()) + "　" + defstr(_printData._schoolInfo.staff2Name), "", "", "印"));
            CsvUtils.newLine(footer);
            CsvUtils.newLine(footer).add(getCertTextCommon());
            CsvUtils.newLine(footer).addAll(Arrays.asList("", _printData._schoolInfo.kisaibi)); // 記載日
            CsvUtils.newLine(footer).addAll(Arrays.asList("(所 在 地)", "", defstr(Util.prepend("〒", _printData._schoolInfo.schoolzipcd)) + " " + defstr(_printData._schoolInfo.schoolAddr)));
            CsvUtils.newLine(footer).addAll(Arrays.asList("(学 校 名)", "", _printData._schoolInfo.schoolname1));
            CsvUtils.newLine(footer).addAll(Arrays.asList("(電話番号)", "", _printData._schoolInfo.schooltelo));
            CsvUtils.newLine(footer).addAll(Arrays.asList("(校 長 名)", "", _printData.isNotOutputPrincipalName() ? "　　　　　" : _printData._schoolInfo.principalName));

            final List blankLine = new ArrayList();
            _csvOutputLines.addAll(seitoJoho);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(gakushunoKiroku);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(tokubetsuKatsudounoKiroku);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(shussekiJokyo);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(sintaiJokyo);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(tyoushoSuisenjiyu);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.addAll(footer);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.add(blankLine);
            _csvOutputLines.add(blankLine);
            nonedata = true;
            return nonedata;
        }

        private List<List<String>> csvGakushunoKirokuShushoku(final TitlePage _titlePage) {

            final List<List<String>> gakushu = new ArrayList<List<String>>();
            final List<String> header = CsvUtils.newLine(gakushu);
            header.addAll(Arrays.asList("教科", "科目"));
            List headerHanki = null;
            if (_printData._isHankiNinteiForm) {
                headerHanki = CsvUtils.newLine(gakushu);
                headerHanki.addAll(new ArrayList<String>(Arrays.asList("", "")));
            }

            for (final Title title : _titlePage._titleList) {

                if (null != headerHanki) {
                    header.addAll(Arrays.asList(new String[] {Util.concatList(title._nameArrayShushoku), ""}));
                    headerHanki.addAll(Arrays.asList("前期", "後期"));
                } else {
                    header.add(Util.concatList(title._nameArrayShushoku));
                }
            }

            for (int i = 0; i < _printData.formRecord._recordList.size(); i++) {
                final FormRecord record = _printData.formRecord._recordList.get(i);

                final List<String> recordLine = CsvUtils.newLine(gakushu);
                final Map<String, String> dataMap = record._dataMap;
                if (dataMap.containsKey("CLASSTITLE")) {
                    recordLine.add(dataMap.get("CLASSTITLE"));
                } else if (dataMap.containsKey("CLASSTITLE2")) {
                    recordLine.add(dataMap.get("CLASSTITLE2"));
                } else {
                    recordLine.addAll(Arrays.asList(defstr(dataMap.get("CLASSNAME")) + defstr(dataMap.get("CLASSNAME_2")), (String) dataMap.get("SUBCLASSNAME")));

                    if (dataMap.containsKey("GRADES") && !"DUMMY".equals(dataMap.get("GRADES"))) {
                        recordLine.add(dataMap.get("GRADES"));
                    } else {

                        for (final Title title : _titlePage._titleList) {

                            if (null != headerHanki) {
                                recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos) + "_HANKI_1"));
                                recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos) + "_HANKI_2"));
                            } else {
                                recordLine.add(dataMap.get("GRADES" + String.valueOf(title._seisekiPos)));
                            }
                        }
                    }
                }
            }

            final List<List<String>> shutokuTannisu = new ArrayList();
            shutokuTannisu.add(Arrays.asList("", "", "修得単位数"));
            final List<String> shutokuTannisuNendoLine = CsvUtils.newLine(shutokuTannisu);
            shutokuTannisuNendoLine.addAll(Arrays.asList("", ""));
            for (final Title title : _titlePage._titleList) {
                shutokuTannisuNendoLine.add(Util.concatList(title._nameArrayShushoku));
                if (null != headerHanki) {
                    shutokuTannisuNendoLine.add(null);
                }
            }
            if (_printData.formRecord._fieldData._dataMap.containsKey(FIELD_VOLUNTEERNAME)) {
                final List<String> volunteerLine = CsvUtils.newLine(shutokuTannisu);

                volunteerLine.add("");
                volunteerLine.add(_printData.formRecord._fieldData._dataMap.get(FIELD_VOLUNTEERNAME));

                for (final Title title : _titlePage._titleList) {
                    final String credit = getFieldData(_printData.formRecord, param()._z010.in(Z010Info.Hirokoku) ? "LHR" : "VOLUNTEER", title._seisekiPos); //修得単位数
                    volunteerLine.add(credit);
                    if (null != headerHanki) {
                        volunteerLine.add(null);
                    }
                }
            }

            // 総合的な学習の時間
            final List<String> sogakuLine = CsvUtils.newLine(shutokuTannisu);
            sogakuLine.add("");
            sogakuLine.add(param()._z010.in(Z010Info.Meiji) ? "Catholic Spirit" : _printData.getSogoSubclassname(param()));

            for (final Title title : _titlePage._titleList) {
                final String slash = getFieldData(_printData.formRecord, "SLASH", title._seisekiPos); // スラッシュ
                final String credit = getFieldData(_printData.formRecord, "TOTALSTUDY", title._seisekiPos); // 修得単位数
                sogakuLine.add(defstr(slash, credit));
                if (null != headerHanki) {
                    sogakuLine.add(null);
                }
            }

            // 留学
            final List<String> abroadLine = CsvUtils.newLine(shutokuTannisu);
            abroadLine.add("");
            abroadLine.add("留学");
            for (final Title title : _titlePage._titleList) {
                final String credit = getFieldData(_printData.formRecord, "abroad", title._seisekiPos); // 修得単位数
                abroadLine.add(credit);
                if (null != headerHanki) {
                    abroadLine.add(null);
                }
            }

            final List blankLine = Collections.emptyList();
            gakushu.add(blankLine);
            gakushu.addAll(shutokuTannisu);

            return gakushu;
        }

        private String getFieldData(final FormRecordPart formRecord, final String field, int i) {
            return formRecord._fieldData._datanMap.get(Tuple.of(field, i));
        }

        private String getJobname2() {
            final String jobname;
            if (param().isKindaifuzoku() || param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama, Z010Info.Hirokoku, Z010Info.naraken)) {
                // 記載責任者職名を印字しない
                jobname = "";
            } else {
                final String defJobname;
                if (_formInfo._isKyotoForm) {
                    defJobname = "";
                } else {
                    defJobname = "教諭";
                }
                jobname = defstr(_printData._schoolInfo.staff2Jobname, defJobname); // 記載責任者職名
            }
            return jobname;
        }

        private void printHeadShushokuyou() {
            if (param()._isShusyokuyouKinkiToitsu2) {
                _svf.VrAttribute("FMT_HEADER", "X=10000");
                svfVrsOut("FMT_FOOTER", "(近畿高等学校統一用紙　その３　令和２年度改定)");
            } else if (param()._z010.in(Z010Info.Higashiosaka) || param()._isShusyokuyouKinkiToitsu || _printData._isKyoto && _printData._isTsushin) {
                _svf.VrAttribute("FMT_HEADER", "X=10000");
                svfVrsOut("FMT_FOOTER", "　　　　　　　　　　　　　　　　　　　　　　(近畿高等学校統一用紙　その３　令和２年度改定)");
            } else {
                _svf.VrAttribute("FMT_HEADER", "(応募書類　その2)");
                svfVrsOut("FMT_FOOTER", "全国高等学校統一用紙(文部科学省、厚生労働省、全国高等学校長協会の協議により平成17年度改定)");
            }

            if (_printData._schoolInfo.hasHeaddata) {
                if (_printData._schoolInfo.schooltelo != null) svfVrsOut("SCHOOL_PHONE", _printData._schoolInfo.schooltelo);  // 学校電話番号
                if (_printData._schoolInfo.schoolname1 != null) svfVrsOut("SCHOOLNAME", _printData._schoolInfo.schoolname1);  // 学校名

                svfVrsOut("STAFFNAME_2", _printData._schoolInfo.staff2Name);
                svfVrsOut("JOBNAME", getJobname2()); // 記載責任者職名

                //住所
                svfVrsOutForData(Arrays.asList("school_address", "school_address1", "school_address2", "school_address_1"), _printData._schoolInfo.schoolAddr, false);
            }
        }

        private void printGradeTitleShushokuyou(final TitlePage _titlePage) {
            if (param()._isShusyokuyouKinkiToitsu2) {
                for (int n = 1; n <= _formInfo._formNen._intval; n++) {
                    final String nen = String.valueOf(_titlePage._index * _formInfo._formNen._intval + n) + "年";
                    svfVrsOut("GRADE1_" + n + "_1", nen);
                    svfVrsOut("GRADE2_" + n + "_1", nen);
                    svfVrsOut("GRADE4_" + n + "_2", nen);
                }

            } else {
                boolean isGakunenOrAnnual = CommonPrintData.PRINT_GAKUNEN == _printData.getPrintGradeTitleMethod(param()) || CommonPrintData.PRINT_ANNUAL == _printData.getPrintGradeTitleMethod(param());
                for (final Title title : _titlePage._titleList) {
                    final int p1 = title._pos;
                    final int p2 = title._seisekiPos;

                    if (!_formInfo._shushokuyouCreditFooterIsRecord) {
                        if (param()._z010.in(Z010Info.Kumamoto) || "1".equals(_printData.getParameter(Parameter.TANIPRINT_RYUGAKU)) && !_printData.paramapContains(Parameter.RYUGAKU_CREDIT) || "1".equals(_printData.getParameter(Parameter.RYUGAKU_CREDIT))) {
                            //初期値表示
                            svfVrsOutn("abroad", p2, "0");
                        }
                        if (param()._z010.in(Z010Info.Kumamoto) || "1".equals(_printData.getParameter(Parameter.TANIPRINT_SOUGOU)) && !_printData.paramapContains(Parameter.SOUGAKU_CREDIT) || "1".equals(_printData.getParameter(Parameter.SOUGAKU_CREDIT))) {
                            //初期値表示
                            if (!_printData.sogoIsSuraShushokuyou("setForm2Record_debug3", param(), null)) {
                                svfVrsOutn("TOTALSTUDY", p2, "0");
                            }
                        }
                    }

                    final List<String> nameArray = title._nameArrayShushoku;
                    if (isGakunenOrAnnual) {
                        if (Title.NYUGAKUMAE.equals(title._name)) {
                            for (int di = 1; di <= 3; di++) {
                                svfVrsOut("GRADE" + di + "_" + p2 + "_1" , nameArray.get(0));  // 学習の記録
                                svfVrsOut("GRADE" + di + "_" + p2 + "_2" , nameArray.get(1));
                                svfVrsOut("GRADE" + di + "_" + p2 + "_3" , nameArray.get(2));
                            }
                            for (int di = 4; di <= 4; di++) {
                                svfVrsOut("GRADE" + di + "_" + p1 + "_1" , nameArray.get(0));  // 学習の記録
                                svfVrsOut("GRADE" + di + "_" + p1 + "_2" , nameArray.get(1));  // 出欠の記録
                                svfVrsOut("GRADE" + di + "_" + p1 + "_3" , nameArray.get(2));  // 出欠備考
                            }
                            continue;
                        }
                    }
                    int[] showidx = {};
                    if (nameArray.size() == 1) {
                        showidx = new int[]{2}; // 中央表示
                    } else {
                        showidx = new int[nameArray.size()];
                        for (int k = 0; k < showidx.length; k++) {
                            showidx[k] = k + 1;
                        }
                    }
                    for (int k = 0; k < showidx.length; k++) {
                        svfVrsOut("GRADE1_" + p1 + "_" + showidx[k], nameArray.get(k));  // 学習の記録　左
                        svfVrsOut("GRADE2_" + p1 + "_" + showidx[k], nameArray.get(k));  // 学習の記録　右
                        if (!_formInfo._shushokuyouCreditFooterIsRecord) {
                            svfVrsOut("GRADE3_" + p1 + "_" + showidx[k], nameArray.get(k));  // 出欠の記録　左
                        }
                        svfVrsOut("GRADE4_" + p2 + "_" + showidx[k], nameArray.get(k));  // 出欠備考
                    }
                }
            }
        }

        private void setTanniKotei(final String field, final StudyrecDat s, final FormRecordPart formRecord, final TitlePage _titlePage) {
            for (final Grades g : s.getNotDropGradesList()) {
                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                if (null == title) {
                    continue;
                }
                if (g._credit != null) {
                    formRecord._fieldData.setDatan(field, title._seisekiPos, g._credit);
                }
            }
        }

        /**
         *  SVF-FORM 学習の記録出力 就職用
         */
        private void setRecordShushokuyou(final DB2UDB db2, final TitlePage _titlePage) {
            final String certifKindCd = _printData.getParameter(Parameter.CERTIFKIND);
            FormRecordPart formRecordPart = new FormRecordPart();
            _printData.formRecord = formRecordPart;

            if (_formInfo._shushokuyouCreditFooterIsRecord) {
                // 後処理で対応
            } else {
                if (_printData.sogoIsSuraShushokuyou("setForm2Record_debug1", param(), null)) {
                    for (int i = 1; i <= 6; i++) {
                        final int i1 = i;
                        formRecordPart._fieldData.setDatan("SLASH", i1, "/");
                    }
                }
            }

            formRecordPart._lineMax12 = _printData.isCsv() ? 9999 : param()._isShusyokuyouKinkiToitsu2 ? 62 : _formInfo._isShusyokuyouFormFormat2 ? 41 : param()._z010.in(Z010Info.Tokiwa) ? 49 : (param()._z010.in(Z010Info.Hirokoku)) ? 54 : _formInfo._shushokuyouCreditFooterIsRecord ? 60 : 55 ; //ページ当りの科目数（１＋２列）：熊本=41,その他=55,広島国際=54
            formRecordPart._lineMax1  = _printData.isCsv() ? 9999 : param()._isShusyokuyouKinkiToitsu2 ? 31 : _formInfo._isShusyokuyouFormFormat2 ? 23 : param()._z010.in(Z010Info.Tokiwa) ? 27 :  30 ; //ページ当りの科目数（１列）　　：熊本=23,その他=30

            final String SUBCLASSNAMEfieldname = "SUBCLASSNAME";
            final String CLASSNAMEfieldname = "CLASSNAME";
            final String CLASSNAME2fieldname = "CLASSNAME_2";
            SvfField subclassnameField = getField(SUBCLASSNAMEfieldname);
            final int subclassnameWidth;
            if (_printData.isCsv()) {
                subclassnameWidth = 9999;
            } else {
                subclassnameWidth = (int) KNJSvfFieldModify.fieldWidth(subclassnameField, 1);
                if (param()._isOutputDebugField) {
                    log.info(" SUBCLASSNAME field width = " + subclassnameWidth + ", y = " + subclassnameField.y() + ", x1 = " + subclassnameField.x());
                }
            }
            formRecordPart._subcWidth = _formInfo._isShusyokuyouFormFormat2 ? 472 : subclassnameWidth;      //フィールドの幅(ドット)
            formRecordPart._subclassnameInfo._height = param()._isShusyokuyouKinkiToitsu2 ? 63 : _formInfo._isShusyokuyouFormFormat2 ? 88 : param()._z010.in(Z010Info.Tokiwa) ? 74 : 60;      //フィールドの高さ(ドット)
            formRecordPart._subclassnameInfo._ystart = param()._isShusyokuyouKinkiToitsu2 ? 1018 : _formInfo._isShusyokuyouFormFormat2 ? 1034 : param()._z010.in(Z010Info.Tokiwa) ? 1046 : 1100;    //開始位置(ドット)
            formRecordPart._subclassnameInfo._minnum = _formInfo._isShusyokuyouFormFormat2 ? 16 : 20;      //最小設定文字数
            formRecordPart._subclassnameInfo._maxnum = _printData.isCsv() ? 9999 : subclassnameField._fieldLength;      //最大設定文字数
            formRecordPart._subclassnameInfo._x1 = _printData.isCsv() ? 9999 : _formInfo._isShusyokuyouFormFormat2 ? 392 + 10 : param()._z010.in(Z010Info.Tokiwa) ? 392 + 10 : subclassnameField.x(); //左列の開始Ｘ軸
            formRecordPart._subclassnameInfo._x2 = _formInfo._isShusyokuyouFormFormat2 ? 1436 + 10 : param()._z010.in(Z010Info.Tokiwa) ? 1436 + 10 : 1338 + 10; //右列の開始Ｘ軸

            SvfField classnameField = getField(CLASSNAMEfieldname);
            SvfField classname2Field = getField(CLASSNAME2fieldname);
            formRecordPart._classnameInfo._height = formRecordPart._subclassnameInfo._height;      //フィールドの高さ(ドット)
            formRecordPart._classnameInfo._ystart = formRecordPart._subclassnameInfo._ystart;    //開始位置(ドット)
            formRecordPart._classnameInfo._minnum = param()._isShusyokuyouKinkiToitsu2 ? 4 : _formInfo._isShusyokuyouFormFormat2 ? 8 : 10;      //最小設定文字数
            formRecordPart._classnameInfo._maxnum = _printData.isCsv() ? 9999 : classnameField._fieldLength; //最大設定文字数
            formRecordPart._classnameInfo._x1 = _printData.isCsv() ? 9999 : _formInfo._isShusyokuyouFormFormat2 ? classnameField.x() - 5 : param()._z010.in(Z010Info.Tokiwa) ? 156 + 10 : classnameField.x(); //左列の開始Ｘ軸
            formRecordPart._classnameInfo._x2 = _formInfo._isKyotoForm || _formInfo._isKumamotoForm || param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji) ? 1200 + 2 : param()._isShusyokuyouKinkiToitsu2 ? 1240 + 5 : param()._z010.in(Z010Info.Tokiwa) ? 1200 + 10 : 1102 + 10; //右列の開始Ｘ軸
            final int clwidth;
            if (_printData.isCsv()) {
                clwidth = 9999;
            } else {
                clwidth = param()._isShusyokuyouKinkiToitsu2 ? (264 - 5) - (formRecordPart._classnameInfo._x1 + 5) : (int) KNJSvfFieldModify.fieldWidth(classnameField, 1);
                if (param()._isOutputDebugField) {
                    log.info(" CLASSNAME field width = " + clwidth + ", y = " + classnameField.y() + ", x1 = " + classnameField.x());
                }
            }
            formRecordPart._clWidth = clwidth; //フィールドの幅(ドット)

            final String recordMaskField = _printData._isHankiNinteiForm ? "RECORD_MASK_HANKI" : "RECORD_MASK";
            StudyrecDat sE014 = null;
            StudyrecDat sSogo = null;
            List<StudyrecDat> sD081List = new ArrayList<StudyrecDat>();
            List<StudyrecDat> sE065List = new ArrayList<StudyrecDat>();

            final String sogoTitle = param()._z010.in(Z010Info.Meiji) ? "Catholic Spirit" : _printData.getSogoSubclassname(param());
            try {
                String s_classcd = "00";    //教科コード
                String s_schoolKind = "00";    //学校種別
                String s_specialDiv = "00"; //専門教育に関する教科・科目
                formRecordPart._linex = 0;                          //行数

                if (_formInfo._shushokuyouCreditFooterIsRecord) {
                    // 後処理で対応
                } else {
                    formRecordPart._fieldData.setData("ITEM1", sogoTitle);
                    formRecordPart._fieldData.setData("ITEM2", "留学");

                    for (final StudyrecDat s : _printData._studyrecDatAbroadList) {
                        //  留学
                        setTanniKotei("abroad", s, formRecordPart, _titlePage);
                    }
                }

                for (final StudyrecDat s : _printData._studyrecDataShushokuyou) {
//                    if ("HIRO".equals(param()._definecode.schoolmark)) {
//                        if ("2".equals(s._schoolcd)) { continue; }
//                    }
                    // 名称マスタE014に登録された科目
                    final boolean isE014 = _printData._notUseE014 == false && (null != _printData._e014Subclasscd && _printData._e014Subclasscd.equals(s._subclasscd) || s._isE014);
                    if(isE014) {
                        if (!param()._z010.in(Z010Info.Hirokoku) && !param().isKindaifuzoku()) {
                            if (_formInfo._shushokuyouCreditFooterIsRecord) {
                                sE014 = s;
                            } else {
                                formRecordPart._fieldData.setData(FIELD_VOLUNTEERNAME, s._subclassname);
                                setTanniKotei("VOLUNTEER", s, formRecordPart, _titlePage);
                            }
                        }
                        continue;
                    }
                    //  総合
                    if (SqlStudyrec.sogo.equalsIgnoreCase(s._classname)) {
                        if(_printData.isMeijiSogo(param())) {
                            if (_formInfo._shushokuyouCreditFooterIsRecord) {
                                // 明治未対応
                            } else {
                                // 明治総合的な学習の時間
                                formRecordPart._fieldData.setData(FIELD_VOLUNTEERNAME, _printData.getSogoSubclassname(param()));
                                for (final Grades g : s.getNotDropGradesList()) {
                                    final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                    if (null == title || Integer.parseInt(title._year) > 2010) {
                                        continue;
                                    }
                                    if (g._credit != null) {
                                        formRecordPart._fieldData.setDatan("VOLUNTEER", title._seisekiPos, g._credit);  //修得単位数
                                    }
                                }
                            }
                        }

                        if (_formInfo._shushokuyouCreditFooterIsRecord) {
                            sSogo = s;
                        } else if (!_printData.sogoIsSuraShushokuyou("setForm2Record_debug2", param(), null)) {
//                            log.fatal(" kotei -- " + SqlStudyrec.sogo + " -- ");
//                            for (final Grades g : s._gradesList) {
//                                log.fatal(" | credit = " + g + " / " + _printData._titles.get(g._year));
//                            }
                            for (final Grades g : s.getNotDropGradesList()) {
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                if (null == title || param()._z010.in(Z010Info.Meiji) && Integer.parseInt(title._year) <= 2010) {
                                    continue;
                                }
                                if (g._credit != null) {
                                    formRecordPart._fieldData.setDatan("TOTALSTUDY", title._seisekiPos, g._credit);  //修得単位数
                                }
                            }
                        }
                        continue;
                    }
                    // LHR
                    if (SqlStudyrec.hirokokulhr.equalsIgnoreCase(s._classname)) {
                        if (_formInfo._shushokuyouCreditFooterIsRecord) {
                            // 広国未対応
                        } else {
                            if (param()._z010.in(Z010Info.Hirokoku)) {
                                setTanniKotei("LHR", s, formRecordPart, _titlePage);
                            }
                        }
                        continue;
                    }
//                    if (isGakunensei() && "0".equals(annual)) {
//                        continue;
//                    }
                    // １行まとめの前籍校は処理回避。
                    if (SqlStudyrec.zenseki.equals(s._classname)) {
                        continue;
                    }

                    final String keySubclassCd = s.keySubclasscd(param());
                    if (_printData._vNameMstD081List.contains(keySubclassCd)) {
                        sD081List.add(s);
                        if (param()._isOutputDebugSeiseki) {
                            log.info(" subclass " + keySubclassCd + " in D081 credit only. ");
                        }
                        continue;
                    }
                    if (_printData._vNameMstE065List.contains(keySubclassCd)) {
                        sE065List.add(s);
                        if (param()._isOutputDebugSeiseki) {
                            log.info(" subclass " + keySubclassCd + " in E065 credit only. ");
                        }
                        continue;
                    }

                    // 未履修科目は出力しないの場合
                    if ("2".equals(_printData.getParameter(Parameter.MIRISYU))) {
                        if (s._compCredit2 == null || s._compCredit2.equals("0")) { continue; }
                    }

                    if (formRecordPart._linex == formRecordPart._lineMax12) {
                        formRecordPart._linex = 0;     //ページ当りの科目数=55
                    }
                    final String titleOnlyField = "GRADES";
                    if (param().isPrintClassTitle(certifKindCd, _printData) && !"1".equals(_printData._notUseClassMstSpecialDiv) && !param().notHasCertifSchoolDatOrKindai()) {
                        if (s._specialDiv != null && !s._specialDiv.equals(s_specialDiv)) {
                            if (formRecordPart._linex == formRecordPart._lineMax1 - 1 || formRecordPart._linex == formRecordPart._lineMax12 - 1) {
                                final FormRecord recordData = formRecordPart.createRecord(formRecordPart._linex);

                                recordData.setData("CLASSCD",  s._classcd);  //教科コード
                                if (_formInfo._useFormKNJE070_3) {
                                    recordData.setData(recordMaskField, "1"); //評定 MASK
                                }
                                formRecordPart._linex++;
                            }
                            final FormRecord record = formRecordPart.createRecord(formRecordPart._linex);
                            final String specialDivName = param().getSpecialDivName(Param.isNewForm(param(), _printData),  s._specialDiv);
                            if (getMS932ByteLength(specialDivName) > 30) {
                                record.setData("CLASSTITLE2", specialDivName);
                            } else {
                                record.setData("CLASSTITLE", specialDivName);
                            }
                            if (_formInfo._useFormKNJE070_3) {
                                record.setData(titleOnlyField, "DUMMY");
                                record.addAttr(titleOnlyField, "X=10000");
                            }
                            nonedata = true;
                            formRecordPart._linex++;
                        }
                    }
                    s_specialDiv = s._specialDiv;

                    final FormRecord record = formRecordPart.createRecord(formRecordPart._linex);
                    final int subclassnamey = (formRecordPart._linex + 1) == formRecordPart._lineMax12 ? formRecordPart._lineMax12 : (formRecordPart._linex + 1) % formRecordPart._lineMax12;
                    svfFieldAttribute2(record, s._subclassname, subclassnamey, SUBCLASSNAMEfieldname, formRecordPart._lineMax1, formRecordPart._subcWidth, formRecordPart._subclassnameInfo);
                    record.setData(SUBCLASSNAMEfieldname, s._subclassname);

                    if ("2".equals(s._schoolcd)) {
                        record.setData(titleOnlyField, defstr(param()._e028Name1, "高卒認定等"));
                    } else if (_printData._vNameMstD077List.contains(keySubclassCd)) {
                        if (param()._isOutputDebugSeiseki) {
                            log.info(" subclass " + keySubclassCd + " in D077 hyotei slash. ");
                        }
                        if (null != param()._knje070GradeSlashImagePath) {
                            if (_printData._isHankiNinteiForm) {
                                for (int i = 0; i < 12; i++) {
                                    record.setDatan("GRADE_HANKI_SLASH", i + 1, param()._knje070GradeSlashImagePath); // 前期
                                }
                            } else {
                                for (int i = 0; i < 6; i++) {
                                    record.setDatan("GRADE_SLASH", i + 1, param()._knje070GradeSlashImagePath); // 前期
                                }
                            }
                        }
                    } else {
                        boolean printGrades = false;
                        for (final Grades grades : s.getNotDropGradesList()) {
                            final Title title = Title.getTitle(_param, _titlePage._titleList, grades._year);
                            if (null == title) {
                                continue;
                            }
                            if (_printData._isFuhakkou) {
                                // 表示しない
                            } else {
                                if (_printData._isHankiNinteiForm) {
                                    final String pgrades1 = grades.getPrintGrade(param(), grades._zenkiGrades);
                                    if (null != pgrades1) {
                                        record.setData("GRADES" + title._seisekiPos + "_HANKI_1", pgrades1); // 前期
                                        printGrades = true;
                                    }
                                    String pgrades2 = null;
                                    if (null != grades._koukiGrades) {
                                        pgrades2 = grades.getPrintGrade(param(), grades._koukiGrades);
                                    } else if (null == grades._zenkiGrades && null != grades._grades) {
                                        pgrades2 = grades.getPrintGrade(param(), grades._grades);
                                    } else if (SqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS.equals(grades._recordFlg)) {
                                        pgrades2 = "*";
                                    }
                                    if (null != pgrades2) {
                                        record.setData("GRADES" + title._seisekiPos + "_HANKI_2", pgrades2); // 後期
                                        printGrades = true;
                                    }
                                } else {
                                    String pgrade = null;
                                    if ("print100".equals(_printData.getParameter(Parameter.HYOTEI))) {
                                        final Param param = param();
                                        final String subclassMstKey;
                                        if ("1".equals(param._useCurriculumcd)) {
                                            subclassMstKey = SubclassMst.key(param, s._classcd, s._schoolKind, s._curriculumCd, s._subclasscd);
                                        } else {
                                            subclassMstKey = SubclassMst.key(param, null, null, null, s._subclasscd);
                                        }
                                        final SubclassMst sclm = SubclassMst.getSubclassMst(param._subclassMstMap, subclassMstKey);

                                        pgrade = _printData._gakunenSeiseki.getGakunenSeisekiString(sclm, grades._year);
                                    } else if (null != grades._grades) {
                                        pgrade = grades.getPrintGrade(param(), grades._grades);
                                    } else if (SqlStudyrec.RECORD_FLG_01_CHAIR_SUBCLASS.equals(grades._recordFlg)) {
                                        pgrade = "*";
                                    }
                                    if (null != pgrade) {
                                        record.setData("GRADES" + title._seisekiPos, pgrade);
                                        printGrades = true;
                                    }
                                }
                            }
                        }
                        if (_formInfo._useFormKNJE070_3) {
                            if (!printGrades) {
                                record.setData(recordMaskField, "1"); //評定 MASK
                            }
                        }
                    }
                    //  教科コードのブレイク 左右一段目は教科名を出力する
                    if (s._classcd != null && !param().isSameClasscd(s._classcd, s._schoolKind, s_classcd, s_schoolKind) || formRecordPart._linex == formRecordPart._lineMax1 || formRecordPart._linex == 0) {
//                    if (log.isDebugEnabled()) {
//                        log.debug("--C--linex="+linex+" / SCHOOLCD="+rs.getString("SCHOOLCD")+" / CLASSCD="+rs.getString("CLASSCD")+" / SUBCLASSCD="+rs.getString("SUBCLASSCD"));
//                    }
                        final int classnameKeta = getMS932ByteLength(s._classname);
                        if (!_printData.isCsv() && classnameField._fieldLength < classnameKeta && null != classname2Field) {
                            // 2行表示
                            final String[] token = KNJ_EditEdit.get_token(s._classname, classname2Field._fieldLength, 2);
                            final String[] fieldnames = {CLASSNAMEfieldname, classname2Field._name};
                            for (int i = 0; i < Math.min(token.length, fieldnames.length); i++) {
                                record.setData(fieldnames[i], token[i]);
                            }
                        } else {
                            svfFieldAttribute2(record, s._classname, subclassnamey, CLASSNAMEfieldname, formRecordPart._lineMax1, formRecordPart._clWidth, formRecordPart._classnameInfo);
                            record.setData(CLASSNAMEfieldname, s._classname);
                        }
                    }
                    s_classcd = s._classcd;
                    s_schoolKind = s._schoolKind;
                    formRecordPart._linex += 1;
                    record.setData("CLASSCD", s._classcd);
                }
            } catch (Exception e) {
                log.error("[KNJE070_2]study_out error!", e);
            }

            // 総学等
            if (!_printData.isCsv()) {
                final List<String> footerList = new ArrayList<String>();
                if (_printData.isMeijiSogo(param())) {
                    footerList.add("meiji_sogo");
                } else if (_printData._notUseE014 == false && (null != _printData._e014Subclasscd || _printData.hasE014() || _formInfo._shushokuyouCreditFooterIsRecord && null != sE014) && !param()._z010.in(Z010Info.Hirokoku) && !param().isKindaifuzoku()) {
                    footerList.add("E014");
                }
                if (_formInfo._shushokuyouCreditFooterIsRecord) {
                    for (int i = 0; i < _formInfo._shushokuyouCreditFooterHeightCount; i++) {
                        footerList.add("footerHeight" + String.valueOf(i));
                    }
                    for (final StudyrecDat sD081 : sD081List) {
                        footerList.add(sD081.toString());
                    }
                    footerList.add("sogo"); // 総合的な学習の時間
                    for (final StudyrecDat e065 : sE065List) {
                        footerList.add(e065.toString());
                    }
                    footerList.add("abroad"); // 留学
                } else {
                    if (!sE065List.isEmpty()) {
                        footerList.add(sE065List.get(0).toString());
                    }
                }
                final int num = footerList.size();
                if (param()._isOutputDebug) {
                    log.info(" footer count = " + num  + " / " + footerList);
                }
                for (int i = formRecordPart._linex; i < formRecordPart._lineMax12 * Math.max(1, (formRecordPart._linex / formRecordPart._lineMax12 + (formRecordPart._linex % formRecordPart._lineMax12 == 0 ? 0 : 1))) - num; i++) {
                    final FormRecord recordData = formRecordPart.createRecord(formRecordPart._linex);
                    recordData.setData("CLASSCD", String.valueOf(i));  // 教科コード
                    recordData.setData(recordMaskField, "1"); //評定 MASK
                    formRecordPart._linex += 1;
                    nonedata = true;
                }
                if (_formInfo._shushokuyouCreditFooterIsRecord) {
                    // 「修得単位数」ヘッダタイトル
                    final FormRecord recordFooterTitle = formRecordPart.createRecord(formRecordPart._linex);
                    recordFooterTitle.setData("CREDIT_FOOTER_TITLE_DUMMY1", "1");
                    if (param()._isShusyokuyouKinkiToitsu2) {
                        for (int n = 1; n <= _formInfo._formNen._intval; n++) {
                            final String nen = String.valueOf(_titlePage._index * _formInfo._formNen._intval + n) + "年";
                            recordFooterTitle.setData("GRADE3_" + n + "_3", nen);
                        }
                    } else {
                        for (final Title title : _titlePage._titleList) {
                            final int i = title._seisekiPos;

                            final List<String> nameArray = title._nameArrayShushoku;
                            int[] showidx = {};
                            if (nameArray.size() == 1) {
                                showidx = new int[]{2}; // 中央表示
                            } else {
                                showidx = new int[nameArray.size()];
                                for (int k = 0; k < showidx.length; k++) {
                                    showidx[k] = k + 1;
                                }
                            }
                            for (int k = 0; k < showidx.length; k++) {
                                recordFooterTitle.setData("GRADE3_" + i + "_" + showidx[k], nameArray.get(k));
                                nonedata = true;
                            }
                        }
                    }
                    formRecordPart._linex += 1;

                    final String creditFieldHead = param()._isShusyokuyouKinkiToitsu2 ? "CREDIT" : "GRADES";
                    final String recordMaskFieldFooter = param()._isShusyokuyouKinkiToitsu2 ?  "CREDIT_RECORD_MASK" : "RECORD_MASK";
                    {
                        // E014
                        if (null != sE014) {
                            final FormRecord recordE014 = formRecordPart.createRecord(formRecordPart._linex);
                            recordE014.setData("CREDIT_SUBCLASSNAME", sE014._subclassname);
                            for (final Grades g : sE014.getNotDropGradesList()) {
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                if (null != title && g._credit != null) {
                                    final boolean isNotInt = NumberUtils.isNumber(g._credit) && !NumberUtils.isDigits(g._credit);
                                    final String field;
                                    if (isNotInt) {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos) + "_DOT";
                                    } else {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos);
                                    }
                                    recordE014.setData(field, g._credit);
                                }
                            }
                            recordE014.setData(recordMaskFieldFooter, "1"); //評定 MASK
                            formRecordPart._linex += 1;
                        }
                    }

                    {
                        // D081
                        for (final StudyrecDat s : sD081List) {
                            final FormRecord recordD081 = formRecordPart.createRecord(formRecordPart._linex);
                            recordD081.setData("CREDIT_SUBCLASSNAME", s._subclassname);
                            for (final Grades g : s.getNotDropGradesList()) {
                                log.info("D081 " + g);
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                final String credit = g._gradeCredit;
                                if (null != title && credit != null) {
                                    final boolean isNotInt = NumberUtils.isNumber(credit) && !NumberUtils.isDigits(credit);
                                    final String field;
                                    if (isNotInt) {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos) + "_DOT";
                                    } else {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos);
                                    }
                                    recordD081.setData(field, credit);
                                }
                            }
                            recordD081.setData(recordMaskFieldFooter, "1"); //評定 MASK
                            formRecordPart._linex += 1;
                        }
                    }

                    {
                        // 総合的な学習の時間
                        final Map<String, String> sogoCreditYears = new TreeMap<String, String>();
                        if (null != sSogo) {
                            for (final Grades g : sSogo.getNotDropGradesList()) {
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                if (null != title && g._credit != null) {
                                    sogoCreditYears.put(title._year, g._credit);
                                }
                            }
                        }
                        final List<String> sogoSuraYears = new ArrayList<String>();
                        for (final Title title : _titlePage._titleList) {
                            if (_printData.sogoIsSuraShushokuyou("setForm2Record_debug3", param(), title._year)) {
                                sogoSuraYears.add(title._year);
                            }
                        }
                        if (param()._isOutputDebug) {
                            log.info(" sogo sSogo = " + sSogo + ", credit years = " + sogoCreditYears + ", sura years = " + sogoSuraYears + ", " + _titlePage._titleList);
                        }

                        final boolean isSogoDefault0 = param()._z010.in(Z010Info.Kumamoto) || "1".equals(_printData.getParameter(Parameter.TANIPRINT_SOUGOU)) && !_printData.paramapContains(Parameter.SOUGAKU_CREDIT) || "1".equals(_printData.getParameter(Parameter.SOUGAKU_CREDIT));
                        final FormRecord recordSogo = formRecordPart.createRecord(formRecordPart._linex);
                        recordSogo.setData("CREDIT_SUBCLASSNAME", sogoTitle);
                        for (final Title title : _titlePage._titleList) {
                            final String val = sogoCreditYears.get(title._year);
                            final boolean isNotInt = NumberUtils.isNumber(val) && !NumberUtils.isDigits(val);
                            final String field;
                            if (isNotInt) {
                                field = creditFieldHead + String.valueOf(title._seisekiPos) + "_DOT";
                            } else {
                                field = creditFieldHead + String.valueOf(title._seisekiPos);
                            }
                            if (!StringUtils.isBlank(val)) {
                                recordSogo.setData(field, val);
                            } else if (param()._z010.in(Z010Info.Miyagiken) && sogoSuraYears.contains(title._year) || !param()._z010.in(Z010Info.Miyagiken) && !sogoSuraYears.isEmpty()) {
                                recordSogo.setDatan(param()._isShusyokuyouKinkiToitsu2 ? "CREDIT_SLASH" : "GRADE_SLASH", title._seisekiPos, param()._knje070GradeSlashImagePath); // 前期
                            } else if (isSogoDefault0) {
                                recordSogo.setData(field, "0");
                            }
                        }
                        //log.info(" recordSogo = " + recordSogo + ", isSogoDefault0 = "+ isSogoDefault0 + ", " + _printData.getParameter(Parameter.TANIPRINT_SOUGOU) + " / " + _printData.getParameter(Parameter.SOUGAKU_CREDIT));
                        recordSogo.setData(recordMaskFieldFooter, "1"); //評定 MASK
                        formRecordPart._linex += 1;
                    }

                    {
                        // E065
                        for (final StudyrecDat s : sE065List) {
                            final FormRecord recordE065 = formRecordPart.createRecord(formRecordPart._linex);
                            recordE065.setData("CREDIT_SUBCLASSNAME", s._subclassname);
                            for (final Grades g : s.getNotDropGradesList()) {
                                log.info("E065 " + g);
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                final String credit = g._gradeCredit;
                                if (null != title && credit != null) {
                                    final boolean isNotInt = NumberUtils.isNumber(credit) && !NumberUtils.isDigits(credit);
                                    final String field;
                                    if (isNotInt) {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos) + "_DOT";
                                    } else {
                                        field = creditFieldHead + String.valueOf(title._seisekiPos);
                                    }
                                    recordE065.setData(field, credit);
                                }
                            }
                            recordE065.setData(recordMaskFieldFooter, "1"); //評定 MASK
                            formRecordPart._linex += 1;
                        }
                    }

                    {
                        // 留学
                        final Map<Integer, String> posCreditMap = new HashMap<Integer, String>();
                        final FormRecord recordAbroad = formRecordPart.createRecord(formRecordPart._linex);
                        recordAbroad.setData("CREDIT_SUBCLASSNAME", "留　　学");
                        if (param()._z010.in(Z010Info.Miyagiken)) {
                            final Map<String, String> shushokuYouRyugakuCreditYear = _printData.shushokuYouRyugakuCreditYear();
                            for (final Title title : _titlePage._titleList) {
                                final boolean noCredit = !shushokuYouRyugakuCreditYear.containsKey(title._year);
                                if (noCredit && null != param()._knje070GradeSlashImagePath) {
                                    posCreditMap.put(title._seisekiPos, "");
                                    recordAbroad.setDatan("GRADE_SLASH", title._seisekiPos, param()._knje070GradeSlashImagePath);
                                }
                            }
                        }

                        if (param()._z010.in(Z010Info.Kumamoto) || "1".equals(_printData.getParameter(Parameter.TANIPRINT_RYUGAKU)) && !_printData.paramapContains(Parameter.RYUGAKU_CREDIT) || "1".equals(_printData.getParameter(Parameter.RYUGAKU_CREDIT)) ) {
                            for (final Title title : _titlePage._titleList) {
                                //初期値表示
                                if (!posCreditMap.containsKey(title._seisekiPos)) {
                                    posCreditMap.put(title._seisekiPos, "0");
                                }
                            }
                        }
                        for (final StudyrecDat s : _printData._studyrecDatAbroadList) {
                            log.info(" abroad studyrec " + s);
                            for (final Grades g : s.getNotDropGradesList()) {
                                log.info("  abroad studyrec grades " + g);
                                final Title title = Title.getTitle(_param, _titlePage._titleList, g._year);
                                if (null != title && g._credit != null) {
                                    if (posCreditMap.containsKey(title._seisekiPos)) {
                                        log.warn(" creditMap contains pos " + title._seisekiPos + " : " + posCreditMap.get(title._seisekiPos));
                                    } else {
                                        posCreditMap.put(title._seisekiPos, Util.add(posCreditMap.get(title._seisekiPos), g._credit));
                                        log.info("   abroad cre : " + title._year + " / " + title._seisekiPos + " => " + g._credit);
                                    }
                                }
                            }
                        }
                        for (final Map.Entry<Integer, String> e : posCreditMap.entrySet()) {
                            final int pos = e.getKey();
                            final String val = e.getValue();
                            final boolean isNotInt = NumberUtils.isNumber(val) && !NumberUtils.isDigits(val);
                            final String field;
                            if (isNotInt) {
                                field = creditFieldHead + String.valueOf(pos) + "_DOT";
                            } else {
                                field = creditFieldHead + String.valueOf(pos);
                            }
                            recordAbroad.setData(field, val);
                        }
                        recordAbroad.setData(recordMaskFieldFooter, "1"); //評定 MASK
                        formRecordPart._linex += 1;
                    }
                }
            }

        }

        private void printFuhakkouRemark2() {
            final SvfField fieldNotice = getField(FIELD_NOTICE);
            final SvfField fieldNotice2 = getField(FIELD_NOTICE2);
            final String remark = getFuhakkouRemark();
            if (null == fieldNotice2 || -1 == StringUtils.indexOf(remark, newLine)) {
                svfVrsOut(FIELD_NOTICE,  remark);
            } else {
                svfVrAttribute(FIELD_NOTICE, "LinkField=");
                final String[] fields = {FIELD_NOTICE, FIELD_NOTICE2};
                if (null != fieldNotice) {
                    final List<String> token = Util.getTokenList(remark, fieldNotice._fieldLength, param());
                    for (int i = 0; i < Math.min(fields.length, token.size()); i++) {
                        svfVrsOut(fields[i], token.get(i));
                    }
                }
            }
        }

        /**
         *  SVF-FORM 学習の記録出力
         */
        private void printStudyrecShushokuyou(final List<FormRecord> recordList) {
            if (_printData._isFuhakkou) {
                printFuhakkouRemark2();
            } else if (_printData.hasJiritsuKatsudouRecord(param(), _printData._studyrecDataShushokuyou)) {
                final String[] fields = {FIELD_NOTICE, FIELD_NOTICE2};
                SvfField field = getField(FIELD_NOTICE);
                if (null != field && !StringUtils.isBlank(_printData._jiritsuKatudouRemark)) {
                    final List<String> tokenList = Util.getTokenList(_printData._jiritsuKatudouRemark, field._fieldLength, param());
                    for (int i = 0; i < tokenList.size() && i < fields.length; i++) {
                        svfVrsOut(fields[i], tokenList.get(i));
                    }
                }
            } else {
                // 代替科目備考出力
                printSubstitutionNotices2();
            }
            FormRecordPart.printSvfFieldData(this, _printData.formRecord._fieldData);
            FormRecordPart.printSvfRecordList(this, recordList);
        }

        private static List<StudyrecDat> getStudyrecDataShushoku(final DB2UDB db2, final PrintData printData, final Param param) {
            final List<StudyrecDat> studyrecList = new ArrayList<StudyrecDat>();
            final List<Map<String, String>> rowList;
            if ("1".equals(printData.getParameter(Parameter.certifSchoolOnly))) {
                rowList = Collections.emptyList();
            } else if (printData._useStudyrecSql2) {
                final Tuple<List<Map<String, String>>, List<Map<String, String>>> rowListTuple = printData._sqlStudyrec.pre_sql2(db2, printData, param);
                printData._tStudyrec = rowListTuple._first;
                rowList = rowListTuple._second;
            } else {
                if (null != printData._e014Subclasscd) {
                    if (null != param.getPs(ps1Key)) {
                        DbUtils.closeQuietly(param.getPs(ps1Key));
                    }
                    final List<String> sqlLines = printData._sqlStudyrec.pre_sql(printData, param);
                    if (param._isOutputDebugQuery) {
                        log.fatal("学習記録データSQL(e014) = " + Util.debugSqlLines("", sqlLines));
                    }
                    param.setPs(db2, ps1Key, Util.mkString(sqlLines, ""));
                }

                rowList = KnjDbUtils.query(db2, param.getPs(ps1Key), null);
            }

            for (final Map<String, String> row : rowList) {
                final String year = getString(row, "YEAR");
                final String recordFlg = getString(row, "RECORD_FLG");
                final String schoolcd = getString(row, "SCHOOLCD");
                String classcd = getString(row, "CLASSCD");
                String subclasscd = getString(row, "SUBCLASSCD");
                String classname = getString(row, "CLASSNAME");
                String subclassname = getString(row, "SUBCLASSNAME");
                final String grades = getString(row, "GRADES");
                String compCredit = getString(row, "COMP_CREDIT");
                String gradeCredit = getString(row, "GRADE_CREDIT");
                String credit = getString(row, "CREDIT");
                final String specialDiv = getString(row, "SPECIALDIV");
                final String validFlg = getString(row, "VALID_FLG");
                final String provSemester = getString(row, "PROV_SEMESTER");
                final String d065Flg = getString(row, "D065FLG");
                String schoolKind;
                String curriculumCd;
                if ("1".equals(param._useCurriculumcd)) {
                    schoolKind = getString(row, "SCHOOL_KIND");
                    curriculumCd = getString(row, "CURRICULUM_CD");
                } else {
                    schoolKind = null;
                    curriculumCd = null;
                }

                StudyrecDat.Kind kind = StudyrecDat.Kind.None;
                boolean koteiTanniToAbroad = false;
                if (SqlStudyrec.sogo.equals(classname)) {
                    kind = StudyrecDat.Kind.Sogo;
                    if (param._setSogakuKoteiTanni) {
                        final Title title = Title.getTitle(param, printData.titleValues(), year);
                        if (null == title || !NumberUtils.isDigits(title._annual)) {
                            if (param._isOutputDebug) {
                                log.warn(" koteiTanni sogaku : unknown year = " + year + " / annual = " + (null == title ? null : title._annual));
                            }
                        } else {
                            final BigDecimal koteiTanni = param._sogakuKoteiTanniMap.get(Integer.parseInt(title._annual));
                            if (null == koteiTanni) {
                                if (param._isOutputDebug) {
                                    log.warn(" koteiTanni null : annual = " + title._annual + " / " + param._sogakuKoteiTanniMap.keySet());
                                }
                            } else {
                                if (Util.toDouble(credit, -1) >= 0) {
                                    credit = koteiTanni.toString();
                                }
                                if (Util.toDouble(compCredit, -1) >= 0) {
                                    compCredit = koteiTanni.toString();
                                }
                            }
                        }

                        if (printData._abroadYears.contains(year)) {
                            classcd = SqlStudyrec.abroad;
                            classname = SqlStudyrec.abroad;
                            schoolKind = SqlStudyrec.abroad;
                            curriculumCd = SqlStudyrec.abroad;
                            subclasscd = SqlStudyrec.abroad;
                            subclassname = SqlStudyrec.abroad;

                            kind = StudyrecDat.Kind.Abroad;
                            koteiTanniToAbroad = true;
                        }
                    }
                } else if (SqlStudyrec.total.equals(classname)) {
                    kind = StudyrecDat.Kind.Total;
                } else if (SqlStudyrec.abroad.equalsIgnoreCase(classname)) {
                    kind = StudyrecDat.Kind.Abroad;
                }

                final boolean isE014 = null != printData._e014Subclasscd && printData._e014Subclasscd.equals(subclasscd) || param.getE014Name1List(printData._notUseE014).contains(subclasscd);
                final Title title = Title.getTitle(param, printData.titleValues(), year);
                final boolean isE065 = printData._vNameMstE065List.contains(classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd);
                final boolean isD081 = printData._vNameMstD081List.contains(classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd);
                final boolean isPrintCreditOnly = StudyrecDat.Kind.Abroad == kind || StudyrecDat.Kind.Sogo == kind || SqlStudyrec.hirokokulhr.equalsIgnoreCase(classname) || isE014 || isE065 || isD081;
                final boolean isSetGradeCredit = isPrintCreditOnly && (isE065 || isD081);
                if (null == title) {
                    if (isPrintCreditOnly) {
                    } else {
                        continue;
                    }
                }

                final Grades g;
                if (isPrintCreditOnly) {
                    g = new Grades(recordFlg, year, grades, credit, isSetGradeCredit ? gradeCredit : null, compCredit, validFlg, null, provSemester, d065Flg, schoolcd, koteiTanniToAbroad);
                } else {
                    g = new Grades(recordFlg, year, grades, null, null, null, validFlg, null, provSemester, d065Flg, schoolcd, koteiTanniToAbroad);
                }

                StudyrecDat s = null;
                for (final StudyrecDat s1 : studyrecList) {
                    if (((("0".equals(s1._schoolcd) || "1".equals(s1._schoolcd)) && ("0".equals(schoolcd) || "1".equals(schoolcd)))
                            || ("2".equals(s1._schoolcd) && "2".equals(schoolcd)))
                        && param.isSameClasscd(s1._classcd, s1._schoolKind, classcd, schoolKind) && (printData._isSubclassOrderNotContainCurriculumcd || (null == curriculumCd && null == s1._curriculumCd || null != curriculumCd && curriculumCd.equals(s1._curriculumCd))) && s1._subclasscd.equals(subclasscd)) {
                        s = s1;
                        break;
                    }
                }
                if (s == null) {
                    s = new StudyrecDat(kind, printData, schoolcd, classcd, schoolKind, curriculumCd, subclasscd, classname, subclassname, null, specialDiv, compCredit);
                    if (SqlStudyrec.abroad.equalsIgnoreCase(classname)) {
                        printData._studyrecDatAbroadList.add(s);
                    } else {
                        studyrecList.add(s);
                    }
                }
                if (printData._useStudyrecSql2) {
                    s._isE014 = param.getE014Name1List(printData._notUseE014).contains(subclasscd);
                }

//                log.fatal(" 2 grades = " + g);
                s._gradesList.add(g);

                if (row.containsKey("ZENKI_GRADES")) {
                    g._zenkiGrades = getString(row, "ZENKI_GRADES");
                }
                if (row.containsKey("KOUKI_GRADES")) {
                    g._koukiGrades = getString(row, "KOUKI_GRADES");
                }
            }
            return studyrecList;
        }

        /**
         * 代替科目備考出力
         */
        private void printSubstitutionNotices2() {
            final List<String> substNotices = _printData.getSubstitutionNotices(param());
            if (param()._z010.in(Z010Info.Tottori, Z010Info.Kyoai, Z010Info.Meiji, Z010Info.Nishiyama) || _formInfo._isKyotoForm) {
                // 1行に1項目ずつ出力
                int i = 0;
                final String[] notice = new String[2];
                for (final String substitutionNotice : substNotices) {
                    notice[i] = substitutionNotice;
                    i += 1;
                    if (i >= notice.length) {
                        break;
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" notice = " + ArrayUtils.toString(notice));
                }
                svfVrsOut(FIELD_NOTICE,  notice[0]);
                svfVrsOut(FIELD_NOTICE2, notice[1]);
            } else if (_formInfo._isKumamotoForm) {
                final int ketaMax = 94;
                boolean ketaAfure = false;
                final String[] notice = new String[2];
                int i = 0;
                for (final List<String> list : Util.splitByCount(substNotices, 2)) { // 1行に2個ずつ表示
                    notice[i] = Util.mkString(list, _SLASH);
                    if (getMS932ByteLength(notice[i]) > ketaMax) {
                        ketaAfure = true;
                        break;
                    }
                    i += 1;
                    if (i >= notice.length) {
                        break;
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" notice = " + ArrayUtils.toString(notice) + ", ketaAfure = " + ketaAfure);
                }
                if (ketaAfure) {
                    // 桁あふれした場合、'／'区切りで全て出力
                    final List<String> token = Util.getTokenList(Util.mkString(substNotices, _SLASH), ketaMax, param());
                    if (token.size() > 0) { svfVrsOut(FIELD_NOTICE,  token.get(0)); }
                    if (token.size() > 1) { svfVrsOut(FIELD_NOTICE2, token.get(1)); }
                } else {
                    svfVrsOut(FIELD_NOTICE,  notice[0]);
                    svfVrsOut(FIELD_NOTICE2, notice[1]);
                }
            } else {
                // 1行に'／'区切りで全て出力
                final SvfField f = getField(FIELD_NOTICE);
                final int ketaMax = null == f ? 94 : f._fieldLength;
                final List<String> token = Util.getTokenList(Util.mkString(substNotices, _SLASH), ketaMax, param());
                if (token.size() > 0) { svfVrsOut(FIELD_NOTICE,  token.get(0)); }
                if (token.size() > 1) { svfVrsOut(FIELD_NOTICE2, token.get(1)); }
            }
        }

        /**
         *  SVF-FORM-OUT 出欠データ
         **/
        private void printAttendShushokuyou(final TitlePage _titlePage) {
            for (final Title title : _titlePage._titleList) {
                if (_printData._isFuhakkou) {
                    // 表示しない
                } else {
                    final String key = 0 == Integer.parseInt(title._year) ? "0" : title._year;
                    final AttendrecDat att = _printData.getAttendrecDat(key);
                    if (attendIsSlash(param(), _printData, att)) {
                        svfVrsOutn("ABSENCE_SLASH", title._seisekiPos, _SLASH);
                    } else {
                        if (null != att) {
                            if (_printData._isFormAttendAllSlash) {
                                if (!AttendrecDat.isNull(att, _printData)) {
                                    svfVrsOutn("absence", title._seisekiPos, att.attend6());        //欠席
                                }
                            } else {
                                svfVrsOutn("absence", title._seisekiPos, att.attend6());        //欠席
                            }
                        }
                    }
                }
            }
        }

        private boolean attendIsSlash(final Param param, final PrintData printData, final AttendrecDat att) {
            // 宮城県通信制で出欠のデータがnullか0の場合、斜線を表示する
            return printData._isMiyagikenTsushin && AttendrecDat.isNull(att, printData);
        }

        /**
         *  SVF-FORM-OUT 健康状況データ
         **/
        private void printShintaiShushokuyou(final DB2UDB db2) {
            if (_printData._isFuhakkou) {
                // 表示しない
                return;
            }
            if (_printData._medexamDetDat.isEmpty()) {
                return;
            }
            final MedexamDetDat medexam = _printData._medexamDetDat;
            svfVrsOut("ymd4"         ,KNJ_EditDate.h_format_JP_M(db2, medexam.get("DATE")));
            svfVrsOut("height"       ,medexam.get("HEIGHT"));
            svfVrsOut("weight"       ,medexam.get("WEIGHT"));

            // 視力
            final boolean isBarevisionLine = "1".equals(param()._f011Namespare2Max);
            final String hyphen = param()._isShusyokuyouKinkiToitsu ? "\u2014" : "----";
            final boolean isPrintNum = "1".equals(_printData.property(Property.knjf030PrintVisionNumber));
            {
                final String barevisionMark = isPrintNum ? medexam.rBarevision() : medexam.rBarevisionMark();
                final String visionMark = isPrintNum ? medexam.rVision() : medexam.rVisionMark();
                final String fieldVISION = "R_VISION";
                final String fieldCLEAR = "R_CLEAR";
                final String fieldBAREVISION = "R_BAREVISION";
                if (medexam.isPrintVisionMark(param(), visionMark)) {
                    svfVrsOut(fieldVISION, visionMark); // 矯正視力 ()内に表記
                }
                if (!StringUtils.isBlank(visionMark) && isBarevisionLine) {
                    if (param()._isShusyokuyouKinkiToitsu) {
                        svfVrAttribute(fieldCLEAR, "YokoBai=2.0,Size=6.0");
                        final SvfField f = getField(fieldCLEAR);
                        svfVrAttribute(fieldCLEAR, "Y=" + String.valueOf(f.y() + 10) + ",X=" + String.valueOf(f.x() - 15));
                    }
                    svfVrsOut(fieldCLEAR, hyphen); // 裸眼視力打ち消し線
                } else if (medexam.isPrintVisionMark(param(), barevisionMark)) {
                    svfVrsOut(fieldBAREVISION, barevisionMark); // 裸眼視力
                }
            }
            {
                final String bareVisionMark = isPrintNum ? medexam.lBarevision() : medexam.lBarevisionMark();
                final String visionMark = isPrintNum ? medexam.lVision() : medexam.lVisionMark();
                final String fieldVISION = "L_VISION";
                final String fieldCLEAR = "L_CLEAR";
                final String fieldBAREVISION = "L_BAREVISION";
                if (medexam.isPrintVisionMark(param(), visionMark)) {
                    svfVrsOut(fieldVISION, visionMark); // 矯正視力 ()内に表記
                }
                if (!StringUtils.isBlank(visionMark) && isBarevisionLine) {
                    if (param()._isShusyokuyouKinkiToitsu) {
                        svfVrAttribute(fieldCLEAR, "YokoBai=2.0,Size=6.0");
                        final SvfField f = getField(fieldCLEAR);
                        svfVrAttribute(fieldCLEAR, "Y=" + String.valueOf(f.y() + 10) + ",X=" + String.valueOf(f.x() - 15));
                    }
                    svfVrsOut(fieldCLEAR, hyphen); // 裸眼視力打ち消し線
                } else if (medexam.isPrintVisionMark(param(), bareVisionMark)) {
                    svfVrsOut(fieldBAREVISION, bareVisionMark); // 裸眼視力
                }
            }

            // 聴力
            {
                // 右
                final String ear = medexam.rEar();
                final String fieldEAR_SLASH = "R_EAR_SLASH";
                final String fieldEAR = "R_EAR";
                final String fieldEAR_CLEAR = "R_EAR_CLEAR";
                if (medexam.isEarSlash(param(), ear)) {
                    if (_printData._isConfigFormEarNullSlash) {
                        // フォームで斜線
                    } else if (param()._isShusyokuyouKinkiToitsu2) {
                        svfVrImageOut(fieldEAR_SLASH, param()._slashImagePath);
                    } else if (param()._isShusyokuyouKinkiToitsu) {
                        svfVrAttribute(fieldEAR,  ATTRIBUTE_CENTER);
                        svfVrsOut(fieldEAR        ,_SLASH);
                    } else {
                        svfVrsOut(fieldEAR_CLEAR  ,_SLASH);
                    }
                } else if (medexam.isPrintEar(param(), ear)) {
                    svfVrsOut(fieldEAR, medexam.getEarName(param(), ear));
                }
            }
            {
                // 左
                final String ear = medexam.lEar();
                final String fieldEAR_SLASH = "L_EAR_SLASH";
                final String fieldEAR = "L_EAR";
                final String fieldEAR_CLEAR = "L_EAR_CLEAR";
                if (medexam.isEarSlash(param(), ear)) {
                    if (_printData._isConfigFormEarNullSlash) {
                        // フォームで斜線
                    } else if (param()._isShusyokuyouKinkiToitsu2) {
                        svfVrImageOut(fieldEAR_SLASH, param()._slashImagePath);
                    } else if (param()._isShusyokuyouKinkiToitsu) {
                        svfVrAttribute(fieldEAR,  ATTRIBUTE_CENTER);
                        svfVrsOut(fieldEAR        ,_SLASH);
                    } else {
                        svfVrsOut(fieldEAR_CLEAR  ,_SLASH);
                    }
                } else if (medexam.isPrintEar(param(), ear)) {
                    svfVrsOut(fieldEAR, medexam.getEarName(param(), ear));
                }
            }
        }

        /**
         *  SVF-FORM-OUT  所見データ出力
         */
        private void printShokenShushokuyou(final TitlePage _titlePage) {
            if (_printData._isFuhakkou) {
                // 表示しない
                return;
            }

            String jobhuntAbsence = _printData._jobHuntAbsence;
            if (_printData._isFormAttendAllSlash) {
                // 就職用は前に備考をつける
                jobhuntAbsence = Util.append(_printData._jobHuntAbsence, newLine) + Util.mkString(Arrays.asList(TSUSHINSEI_NIHA, SYUSSEKI_NO_KITEI_HA_NAI), newLine);
            } else if (_printData._isMiyagikenTsushin && AttendrecDat.isAllNull(_param, _printData, _titlePage._titleList) && StringUtils.isBlank(_printData._jobHuntAbsence)) {
                // 宮城県通信制で出欠のデータがすべてnullか0の場合、備考に表示する
                jobhuntAbsence = TSUSHINSEI_NIHA_SYUSSEKI_NO_KITEI_HA_NAI;
            }

            //欠席の主な理由
            printSvfKurikaesi2("reason", Util.getTokenList(jobhuntAbsence, kessekinoOmonaRiyuuSize(), param()), true);

            //特別活動の記録
            printSvfKurikaesi2("record", Util.getTokenList(_printData._jobHuntRec, tokubetsuKatudouSize(), param()), true);

            //身体状況備考
            printSvfKurikaesi2("note", Util.getTokenList(_printData._jobHuntHealthremark, sintaiJokyoBikouSize(), param()), false);

            //本人の長所・推薦事由等
            printSvfKurikaesi2("point", Util.getTokenList(_printData._jobHuntRecommend, tyoushoSuisenjiyuSize(), param()), true);
        }

        private ShokenSize kessekinoOmonaRiyuuSize() {
            final int intoadiv = Integer.parseInt(_printData._os);     // ＯＳ区分
            if (intoadiv == 1 && _printData._isMiekenForm) {
                return ShokenSize.getWithKeta(21, 6);
            }
            return ShokenSize.getWithKeta(20, 6);
        }

        private ShokenSize tokubetsuKatudouSize() {
            final int intoadiv = Integer.parseInt(_printData._os);     // ＯＳ区分
            if (intoadiv == 1) {
                return ShokenSize.getWithKeta(40, 10);
            }
            return ShokenSize.getWithKeta(42, 10);
        }

        private ShokenSize sintaiJokyoBikouSize() {
            final int intoadiv = Integer.parseInt(_printData._os);     // ＯＳ区分
            if (intoadiv == 1) {
                return ShokenSize.getWithKeta(28, 3);
            }
            return ShokenSize.getWithKeta(30, 3);
        }

        private ShokenSize tyoushoSuisenjiyuSize() {
            if (param()._z010.in(Z010Info.Miyagiken)) {
                return ShokenSize.getWithKeta(76, 16);
            }
            final int intoadiv = Integer.parseInt(_printData._os);     // ＯＳ区分
            if (intoadiv == 1) {
                return ShokenSize.getWithKeta(76, 13);
            }
            return ShokenSize.getWithKeta(80, 13);
        }


        private void printSvfKurikaesi2(final String head, final List<String> tokenList, final boolean flg) {
            if (_formInfo._isShushokuyouShokenRepeat) {
                for (int i = 0; i < tokenList.size(); i++) {
                    svfVrsOutn(head, i + 1, tokenList.get(i));
                }
            } else {
                final int intoadiv = Integer.parseInt(_printData._os);     // ＯＳ区分
                for (int i = 0; i < tokenList.size(); i++) {
                    final String field;
                    if (_formInfo._isShushokuyouKenja && flg && "1".equals(_printData.property(Property.tyousasyoEMPTokuBetuFieldSize))) {
                        field = head + (i + 1) + "_2";
                    } else {
                        field = head + (i + 1) + ((intoadiv == 1) ? "" : "_2k");
                    }
                    svfVrsOut(field, tokenList.get(i));
                }
            }
        }

        /*
         * ＳＶＦ－ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
         */
        private static void svfFieldAttribute2(
                final FormRecord record,
                final String name,
                final int ln,
                final String fieldname,
                final int lineMax1,
                final int width,
                final KNJSvfFieldInfo fieldInfo
        ) {
            final KNJSvfFieldModify svfobj = new KNJSvfFieldModify(fieldname, width, fieldInfo._height, fieldInfo._ystart, fieldInfo._minnum, fieldInfo._maxnum);
            final double charSize = svfobj.getCharSize(name);
            //log.info(" " + fieldname + ", width = " + width + ", charSize = " + charSize + " / " + fieldInfo._height + " / " + fieldInfo._ystart + " / " + fieldInfo._minnum);
            final int hnum = (ln % lineMax1 == 0) ? lineMax1 : ln % lineMax1;
            final int x;
            if (ln <= lineMax1) {
                x = fieldInfo._x1;  //左列の開始Ｘ軸
            } else {
                x = fieldInfo._x2;  //右列の開始Ｘ軸
            }
            record.addAttr(fieldname, "X=" + String.valueOf(x));
            record.addAttr(fieldname, "Y=" + String.valueOf((int) svfobj.getYjiku(hnum, charSize)));
            record.addAttr(fieldname, "Size=" + String.valueOf(charSize));
        }

        /**
         * @param vision データ上の視力
         * @return 視力を文字で返す
         */
        private static String getVision(final String vision) {
            if (StringUtils.isBlank(vision)) return "";
            double i = 0.0;
            try {
                i = Double.parseDouble(vision);
            } catch (NumberFormatException e) {
                log.error("NumberFormatException", e);
                return "";
            }
            if (i < 0.3) {
                return "D";
            } else if (i < 0.7) {
                return "C";
            } else if (i < 1.0) {
                return "B";
            } else {
                return "A";
            }
        }

        /**
         * @return
         */
        private static void setSvfForm2(final PrintData printData, final Param param, final FormInfoKNJE070_2 formInfo) {
            final boolean studentHasE014Subclass = null != printData._e014Subclasscd || printData.hasE014(); // 修得単位数の欄が[E014の科目、総合的な学習の時間、留学]の3段
            if (param._z010.in(Z010Info.Sundaikoufu)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2SUNDAIKOUFU.frm" : "KNJE070_6SUNDAIKOUFU.frm";
                //} else if (FormNen._6 == formi) {
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_3_2SUNDAIKOUFU.frm" : "KNJE070_3SUNDAIKOUFU.frm";
                    formInfo._formNen = FormNen._4;
                }
                formInfo._isKyotoForm = true;
                formInfo._isShusyokuyouFormFormat2 = true;
            } else if (param._z010.in(Z010Info.Musashinohigashi)) {
                formInfo._formName = "KNJE070_6MUSAHIGA.frm";
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouKenja = true;
            } else if (printData._isKyoto) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2KYO.frm" : "KNJE070_6KYO.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_4_2KYO.frm" : "KNJE070_4KYO.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_3_2KYO.frm" : "KNJE070_3KYO.frm";
                }
                formInfo._isKyotoForm = true;
                formInfo._isShusyokuyouFormFormat2 = true;
            } else if (param._z010.in(Z010Info.Tottori, Z010Info.Kyoai)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2TORI.frm" : "KNJE070_6TORI.frm";
                //} else if (FormNen._6 == formi) {
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_3_2TORI.frm" : "KNJE070_3TORI.frm";
                    formInfo._formNen = FormNen._4;
                }
                formInfo._isShusyokuyouFormFormat2 = true;
            } else if (param._z010.in(Z010Info.Meiji)) {
//                if (FormNen._3 == formi) {
//                    form = getSvfForm2(printData, param, 4);
//                } else if (FormNen._6 == formi) {
//                    form = getSvfForm2(printData, param, 4);
//                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = printData.isMeijiSogo(param) ? "KNJE070_3_2TORI.frm" : "KNJE070_3TORI.frm";
//                }
                formInfo._formNen = FormNen._4;
//                }
                formInfo._isShusyokuyouFormFormat2 = true;
            } else if (param._z010.in(Z010Info.Tokiwa)) {
//                if (FormNen._3 == formi) {
                    formInfo._formName = "KNJE070_6TOKIWA.frm";
                    formInfo._formNen = FormNen._4;
//                } else if (FormNen._6 == formi) {
//                } else { // if (FormNen._4 == formi) {
//                }
            } else if (param._z010.in(Z010Info.Kumamoto) && !printData._isTsushin) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2KUMA.frm" : "KNJE070_6KUMA.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName =  studentHasE014Subclass ? "KNJE070_4_2KUMA.frm" : "KNJE070_4KUMA.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName =  studentHasE014Subclass ? "KNJE070_3_2KUMA.frm" : "KNJE070_3KUMA.frm";
                }
                formInfo._isKumamotoForm = true;
                formInfo._isShusyokuyouFormFormat2 = true;
            } else if (param._z010.in(Z010Info.Hirokoku)) {
                //if (FormNen._3 == formi) {
                //} else if (FormNen._6 == formi) {
                //} else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_3HIROKOKU.frm";
                    formInfo._useFormKNJE070_3 = true;
                    formInfo._formNen = FormNen._4;
                //}
            } else if (param.isKindaifuzoku()) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_6KIN.frm";
                    formInfo._useFormKNJE070_3 = true;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_4KIN.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_3KIN.frm";
                }
            } else if (param._z010.in(Z010Info.Miyagiken)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_6_3.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_4_3.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_3_3.frm";
                }
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouShokenRepeat = true;
            } else if (param._z010.in(Z010Info.Fukuiken)) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_6FUKUI.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = "KNJE070_4FUKUI.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = "KNJE070_3FUKUI.frm";
                }
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouKenja = true;
            } else if (param._isShusyokuyouKinkiToitsu) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2KINKITOITSU.frm" : "KNJE070_6KINKITOITSU.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_4_2KINKITOITSU.frm" : "KNJE070_4KINKITOITSU.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_3_2KINKITOITSU.frm" : "KNJE070_3KINKITOITSU.frm";
                }
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouKenja = true;
            } else if (param._isShusyokuyouKinkiToitsu2) {
                formInfo._formName = "KNJE070_3_2KINKITOITSU2.frm";
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouKenja = true;
                formInfo._formNen = FormNen._4;
            } else {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_6_2.frm" : "KNJE070_6.frm";
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_4_2.frm" : "KNJE070_4.frm";
                } else { // if (FormNen._4 == formi) {
                    formInfo._formName = studentHasE014Subclass ? "KNJE070_3_2.frm" : "KNJE070_3.frm";
                }
                formInfo._useFormKNJE070_3 = true;
                formInfo._isShushokuyouKenja = true;
            }
            if (formInfo._formNen == FormNen.NONE) {
                if (FormNen._3 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._3;
                } else if (FormNen._6 == formInfo._formNenYou) {
                    formInfo._formNen = FormNen._6;
                } else { // if (FormNen._4 == formi) {
                    formInfo._formNen = FormNen._4;
                }
            }
        }
    }
}

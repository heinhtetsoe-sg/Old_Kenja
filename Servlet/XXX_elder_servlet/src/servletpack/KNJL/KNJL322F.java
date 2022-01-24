/*
 *
 * 作成日: 2016/10/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL322F {

    private static final Log log = LogFactory.getLog(KNJL322F.class);

    private final String AMIKAKE_ATTRIBUTE = "Paint=(0,80,2),Bold=1";

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean isCsv = false;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            isCsv = "csv".equals(_param._cmd);

            _hasData = false;

            final List receptAllList = Recept.getReceptList(db2, _param);
            if (isCsv) {
                if (!receptAllList.isEmpty()) {
                    outputCsv(db2, response, receptAllList);
                }
            } else {

                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if (!receptAllList.isEmpty()) {
                    printMain(db2, svf, receptAllList);
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (isCsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private void outputCsv(final DB2UDB db2, final HttpServletResponse response, final List receptList) {
        final List lines = getCsvOutputLines(db2, receptList);

        CsvUtils.outputLines(log, response, getTitle(db2) + " " + getSubtitle(db2) + " " + getCourse() + " " + getOrder() + ".csv" , lines);
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private List getCsvOutputLines(final DB2UDB db2, final List receptList) {

        boolean isForm2 = 2 == _param.getFormPattern();

        final List lines = new ArrayList();
        final List header1 = newLine(lines);
        final List header2 = newLine(lines);
        final List header3 = newLine(lines);
        final List header4 = newLine(lines);
        final List header5 = newLine(lines);

        header1.addAll(Arrays.asList(new String[] {getTitle(db2) + " " + getSubtitle(db2) + " " + getCourse() + " " + getOrder()}));
        header2.addAll(Arrays.asList(new String[] {_param._currentTime}));

        if (isForm2) {
            if ("2".equals(_param._testdiv)) {
                if ("1".equals(_param._notShowForce)) {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                               , "希望コース", "面接" , "模試TOP1" , ""   , ""      , "模試TOP2"   , ""      , ""    , "6".equals(_param._testdiv) ? "" : "併願校", "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , ""     , "模試名"  , "3科", "5科目" , "模試名"    , "3科"   , "5科"}));
                } else {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                             , "希望コース", "面接" , "模試TOP1" , ""   , ""      , "模試TOP2"   , ""      , ""    , _param._examDivName2, ""                , ""                , ""                , "6".equals(_param._testdiv) ? "" : "併願校", "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""        , ""     , "模試名"  , "3科", "5科目" , "模試名"    , "3科"   , "5科", _param._forceName1  , _param._forceName2, _param._forceName3, _param._forceName4}));
                }
            } else {
                if ("1".equals(_param._notShowForce)) {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                             , "希望コース", _param._examDivName, ""                 , ""                 , ""                 , ""                 , "6".equals(_param._testdiv) ? "" : "併願校", "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , _param._courseName1, _param._courseName2, _param._courseName3, _param._courseName4, _param._courseName5}));
                } else {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                             , "希望コース", _param._examDivName, ""                 , ""                 , ""                 , ""                 , _param._examDivName2, ""                , ""                , ""                , "6".equals(_param._testdiv) ? "" : "併願校", "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , _param._courseName1, _param._courseName2, _param._courseName3, _param._courseName4, _param._courseName5, _param._forceName1  , _param._forceName2, _param._forceName3, _param._forceName4}));
                }
            }
        } else {
            if ("1".equals(_param._testdiv)) {
                if ("1".equals(_param._notShowForce)) {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                               , "希望コース", "面接" , "模試Top1" , ""   , ""      , "模試TOP2" , ""    , ""    , "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , ""     , "模試名"   , "3科", "5科"   , "模試名"   , "3科" , "5科"}));
                } else {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                               , "希望コース",  "面接" , "模試Top1" , ""   , ""      , "模試TOP2" , ""    , ""   , _param._examDivName2, ""                , ""                , ""                , "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , ""     , "模試名"   , "3科", "5科"   , "模試名"   , "3科" , "5科", _param._forceName1  , _param._forceName2, _param._forceName3, _param._forceName4}));
                }
            } else {
                if ("1".equals(_param._notShowForce)) {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                             , "希望コース", "適性検査"         , ""                 , ""                 , ""                 , ""                 , "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , _param._courseName1, _param._courseName2, _param._courseName3, _param._courseName4, _param._courseName5}));
                } else {
                    header3.addAll(Arrays.asList(new String[] {"No.", "受験番号", _param._citynameTitle, _param._schoolnameTitle, "氏名", "ふりがな"                                             , "希望コース", "適性検査"         , ""                 , ""                 , ""                 , ""                 , _param._examDivName2, ""                , ""                , ""                , "3科", "5科", "9科", "備考１", "備考２", "合格コース"}));
                    header4.addAll(Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , StringUtils.defaultString(_param._testdivName) + "合計", ""          , _param._courseName1, _param._courseName2, _param._courseName3, _param._courseName4, _param._courseName5, _param._forceName1  , _param._forceName2, _param._forceName3, _param._forceName4}));
                }
            }
        }
        header5.addAll(        Arrays.asList(new String[] {""   ,""         , ""                   , ""                     , ""    , String.valueOf(receptList.size())}));

        for (int i = 0; i < receptList.size(); i++) {
            final Recept recept = (Recept) receptList.get(i);
            final List line = newLine(lines);

            boolean isTenkakibo = "1".equals(getString(recept._data, "SLIDE_FLG")) && (!NumberUtils.isDigits(_param._advanceScore) || NumberUtils.isDigits(getString(recept._data, "TOTAL4")) && Integer.parseInt(getString(recept._data, "TOTAL4")) > _param._advanceScoreInt);

            line.add(getString(recept._data, "PRINT_LINE_NO")); // NO
            line.add(getString(recept._data, "RECEPTNO")); // 受験番号
            line.add(isTenkakibo ? "〇" : ""); // 転科希望
            line.add("6".equals(_param._testdiv) ? "" : getString(recept._data, "FINSCHOOL_NAME") ); // 中学校
            line.add(getString(recept._data, "NAME")); // 氏名
            line.add(getString(recept._data, "NAME_KANA")); // フリガナ
            line.add(getString(recept._data, "EXAMCOURSE_ABBV")); // 希望コース
            final Map scoreMap1 = _param.getScoreMap(recept, 1);
            if ("1".equals(_param._testdiv) || "2".equals(_param._testdiv)) {
                line.add(getMapDefStr(scoreMap1, "INTERVIEW_VALUE")); // 入試コース 面接
                if (!"".equals(getMapDefStr(recept._data, "TOP1_COMPANYCD"))) {
                    if ("00009999".equals(getMapDefStr(recept._data, "TOP1_COMPANYCD"))) {
                        line.add(getMapDefStr(recept._data, "TOP1_COMPANY_TEXT")); // 模試偏差値 模試名1(その他)
                    } else {
                        line.add(getMapDefStr(recept._data, "TOP1_COMPANYCDNAME")); // 模試偏差値 模試名1
                    }
                }
                //
                line.add(getMapDefStr(recept._data, "TOP1_AVG3")); // 模試偏差値TOP2(3科)
                line.add(getMapDefStr(recept._data, "TOP1_AVG5")); // 模試偏差値TOP2(5科)
                if (!"".equals(getMapDefStr(recept._data, "TOP2_COMPANYCD"))) {
                    if ("00009999".equals(getMapDefStr(recept._data, "TOP2_COMPANYCD"))) {
                        line.add(getMapDefStr(recept._data, "TOP2_COMPANY_TEXT")); // 模試偏差値 模試名2(その他)
                    } else {
                        line.add(getMapDefStr(recept._data, "TOP2_COMPANYCDNAME"));  // 模試偏差値 模試名2
                    }
                }
                line.add(getMapDefStr(recept._data, "TOP2_AVG3")); // 模試偏差値TOP2(3科)
                line.add(getMapDefStr(recept._data, "TOP2_AVG5")); // 模試偏差値TOP2(3科)
            } else {
                if (isForm2) {
                    if ("7".equals(_param._testdiv)) {
                        line.add(getMapDefStr(scoreMap1, "9")); // 入試コース プレゼンテーション
                    } else {
                        line.add(getMapDefStr(scoreMap1, "1")); // 入試コース 国語
                    }
                    line.add(getMapDefStr(scoreMap1, "5")); // 入試コース 英語
                    line.add(getMapDefStr(scoreMap1, "2")); // 入試コース 数学
                    line.add(getMapDefStr(scoreMap1, "TOTAL4")); // 入試コース 合計
                    line.add("6".equals(_param._testdiv) ? "" : getMapDefStr(scoreMap1,"INTERVIEW_VALUE")); // 入試コース 面接
                } else {
                    line.add(getMapDefStr(scoreMap1, "2")); // 適正検査 数学
                    line.add(getMapDefStr(scoreMap1, "5")); // 適正検査 英語
                    line.add(getMapDefStr(scoreMap1, "6")); // 適正検査 作文
                    line.add(getMapDefStr(scoreMap1, "TOTAL4")); // 適正検査 合計
                    line.add(getMapDefStr(scoreMap1, "INTERVIEW_VALUE")); // 適正検査 面接
                }
            }
            if (!"1".equals(_param._notShowForce)) {
                final Map scoreMap2 = _param.getScoreMap(recept, 2);
                line.add(getMapDefStr(scoreMap2, "1")); // 質力診断テスト 国語
                line.add(getMapDefStr(scoreMap2, "5")); // 質力診断テスト 英語
                line.add(getMapDefStr(scoreMap2, "2")); // 質力診断テスト 数学
                line.add(getMapDefStr(scoreMap2, "TOTAL4")); // 質力診断テスト 合計
            }
            if (isForm2) {
                line.add("6".equals(_param._testdiv) ? "" : StringUtils.defaultString(getString(recept._data, "SH_SCHOOL_NAME"))); // 併願校
            }
            line.add(getString(recept._data, "RECR_VISIT_SCORE_TOTAL3")); // 科目合計
            line.add(getString(recept._data, "RECR_VISIT_SCORE_TOTAL5")); // 科目合計
            line.add(getString(recept._data, "RECR_VISIT_SCORE_TOTAL9")); // 科目合計

            line.add(getRemark1(recept)); // 備考1
            line.add(getRemark2(recept)); // 備考2
            line.add(getString(recept._data, "SUC_EXAMCOURSE_ABBV")); // 合格コース
        }
        _hasData = true;
        return lines;
    }

    private String getMapDefStr(final Map sMap, final String key) {
        return sMap.containsKey(key) ? (String)sMap.get(key) : "";
    }
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static int getMS932ByteCount(final String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }

    private String getTitle(final DB2UDB db2) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear));
        final String title;
        if ("9".equals(_param._testdiv)) {
            title = nendo + "年度 学力診断テスト";
        } else if ("3".equals(_param._testdiv)) {
            title = nendo + "年度 一般・併願優遇 入試判定資料";
        } else if ("6".equals(_param._testdiv)) {
            title = nendo + "年度 一貫生 進級テスト判定資料";
        } else {
            title = nendo + "年度 " + StringUtils.defaultString(_param._testdivName) + " 入試判定資料";
        }
        return title;
    }

    private String getSubtitle(final DB2UDB db2) {
        final String testdivname = StringUtils.isEmpty(_param._testdiv0name) ? "" : StringUtils.defaultString(_param._testdiv0name) + " ";
        final String date = null == _param._testdivDate ? "" : KNJ_EditDate.h_format_JP(db2, _param._testdivDate.replace('/', '-'));
        final String subtitle = "(" + testdivname + date + ") ";
        return subtitle;
    }

    private String getOrder() {
        if ("1".equals(_param._sort)) {
            return "成績順";
        } else {
            return "受験番号順";
        }
    }

    private String getCourse() {
        if ("1".equals(_param._coursediv)) {
            return "理数キャリア";
        } else if ("2".equals(_param._coursediv)) {
            return "国際教養";
        } else if ("3".equals(_param._coursediv)) {
            return "スポーツ科学";
        } else if ("9".equals(_param._coursediv)) {
            return "全コース";
        }
        return "";
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List receptAllList) {

        log.info(" formpattern = " + _param.getFormPattern());
        boolean isForm2 = 2 == _param.getFormPattern();

        final String form;
        if (isForm2) {
            if ("2".equals(_param._testdiv)) {
                form = "1".equals(_param._notShowForce) ? "KNJL322F_2_4.frm" : "KNJL322F_2_3.frm";
            } else {
                form = "1".equals(_param._notShowForce) ? "KNJL322F_2_2.frm" : "KNJL322F_2.frm";
            }
        } else {
            if ("1".equals(_param._testdiv)) {
                form = "1".equals(_param._notShowForce) ? "KNJL322F_1_4.frm" : "KNJL322F_1_3.frm";
            } else {
                form = "1".equals(_param._notShowForce) ? "KNJL322F_1_2.frm" : "KNJL322F_1.frm";
            }
        }
        final int maxLine = 55;

        svf.VrSetForm(form, 1);
        final List pageList = getPageList(receptAllList, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List receptList = (List) pageList.get(pi);

            svf.VrsOut("TEXT1", getOrder());
            svf.VrsOut("COURSE", getCourse());

            svf.VrsOut("TITLE", getTitle(db2)); // タイトル
            svf.VrsOut("SUBTITLE", getSubtitle(db2)); // タイトル
            svf.VrsOut("DATE", _param._currentTime); // 印刷日
            svf.VrsOut("CITYNAME_TITLE", _param._citynameTitle);
            svf.VrsOut("SCHOOLNAME_TITLE", _param._schoolnameTitle);
            svf.VrsOut("RECOMMEND_TOTAL_NAME", StringUtils.defaultString(_param._testdivName) + "合計"); // 推薦合計名称
            svf.VrsOut("RECOMMEND_TOTAL", String.valueOf(receptAllList.size())); // 推薦合計
            if (isForm2) {
                svf.VrsOut("EXAN_DIV_NAME", _param._examDivName); // 入試区分名称
                if ("6".equals(_param._testdiv)) {
                } else {
                    svf.VrsOut("ANOTHER_SCHOOL_TITLE", "併願校"); // 入試区分名称
                }
            }
            svf.VrsOut("COURSE_NAME1", _param._courseName1); // 入試コース
            svf.VrsOut("COURSE_NAME2", _param._courseName2); // 入試コース
            svf.VrsOut("COURSE_NAME3", _param._courseName3); // 入試コース
            svf.VrsOut("COURSE_NAME4", _param._courseName4); // 入試コース
            svf.VrsOut("COURSE_NAME5", _param._courseName5); // 入試コース

            if (!"1".equals(_param._notShowForce)) {
                svf.VrsOut("EXAN_DIV_NAME2", _param._examDivName2); // 入試区分名称
                svf.VrsOut("FORCE_NAME1", _param._forceName1); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME2", _param._forceName2); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME3", _param._forceName3); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME4", _param._forceName4); // 質力診断テスト名称
            }

            for (int i = 0; i < receptList.size(); i++) {
                final Recept recept = (Recept) receptList.get(i);
                final int line = i + 1;

                if (null != recept._data.get("PRINT_COURSE_HEADER")) {
                    final int nameKanalen = getMS932ByteCount(getString(recept._data, "PRINT_COURSE_HEADER"));
                    svf.VrsOutn("KANA" + (nameKanalen > 20 ? "2" : "1"), line, getString(recept._data, "PRINT_COURSE_HEADER")); //
                    continue;
                }

                svf.VrsOutn("NO", line, getString(recept._data, "PRINT_LINE_NO")); // NO
                svf.VrsOutn("EXAN_NO", line, getString(recept._data, "RECEPTNO")); // 受験番号
                boolean isTenkaKibou = "1".equals(getString(recept._data, "SLIDE_FLG")) && (!NumberUtils.isDigits(_param._advanceScore) || NumberUtils.isDigits(getString(recept._data, "TOTAL4")) && Integer.parseInt(getString(recept._data, "TOTAL4")) > _param._advanceScoreInt);
                boolean isTenkaKibouAmikake = "1".equals(_param._advanceAmikake) && isTenkaKibou;
                if (isTenkaKibouAmikake) {
                    svf.VrAttributen("EXAN_NO", line, AMIKAKE_ATTRIBUTE);
                }
                if (!"6".equals(_param._testdiv)) {
                    //final String cityname = getString(recept._data, "FINSCHOOL_DISTCDNAME");
                    final String cityname = isTenkaKibou ? "○" : "";
                    final int citynamelen = getMS932ByteCount(cityname);
                    final String field = "CITY_NAME" + (citynamelen > 10 ? "2" : "1");
                    svf.VrAttributen(field, line, "Hensyu=3");
                    svf.VrsOutn(field, line, cityname); // 市・町
                    final int schoolnamelen = getMS932ByteCount(getString(recept._data, "FINSCHOOL_NAME"));
                    svf.VrsOutn("SCHOOL_NAME" + (schoolnamelen > 20 ? "3": schoolnamelen > 10 ? "2" : "1"), line, getString(recept._data, "FINSCHOOL_NAME")); // 中学校
                }
                final int namelen = getMS932ByteCount(getString(recept._data, "NAME"));
                svf.VrsOutn("NAME" + (namelen > 14 ? "2" : "1"), line, getString(recept._data, "NAME")); // 氏名
                final int nameKanalen = getMS932ByteCount(getString(recept._data, "NAME_KANA"));
                svf.VrsOutn("KANA" + (nameKanalen > 20 ? "2" : "1"), line, getString(recept._data, "NAME_KANA")); // フリガナ
                svf.VrsOutn("HOPE_COURSE", line, getString(recept._data, "EXAMCOURSE_ABBV")); // 希望コース
                final Map scoreMap1 = _param.getScoreMap(recept, 1);
                if ("1".equals(_param._testdiv) || "2".equals(_param._testdiv)) {
                    svf.VrsOutn("COURSE5", line, getMapDefStr(scoreMap1, "INTERVIEW_VALUE")); // 入試コース 面接
                    //
                    if (!"".equals(getMapDefStr(recept._data, "TOP1_COMPANYCD"))) {
                        String top1MockName = "";
                        if ("00009999".equals(getMapDefStr(recept._data, "TOP1_COMPANYCD"))) {
                            top1MockName = getMapDefStr(recept._data, "TOP1_COMPANY_TEXT"); // 模試偏差値 模試名1(その他)
                        } else {
                            top1MockName = getMapDefStr(recept._data, "TOP1_COMPANYCDNAME"); // 模試偏差値 模試名1
                        }
                        final int t1len = KNJ_EditEdit.getMS932ByteLength(top1MockName);
                        final String t1field = t1len > 14 ? "_3" : t1len > 10 ? "_2" : "_1";
                        svf.VrsOutn("MOCK_NAME1" + t1field, line, top1MockName); // 模試偏差値 模試名1(その他)
                    }
                    svf.VrsOutn("TOP2_1_1", line, getMapDefStr(recept._data, "TOP1_AVG3")); // 模試偏差値TOP2(3科)
                    svf.VrsOutn("TOP2_1_2", line, getMapDefStr(recept._data, "TOP1_AVG5")); // 模試偏差値TOP2(5科)
                    if (!"".equals(getMapDefStr(recept._data, "TOP2_COMPANYCD"))) {
                        String top2MockName = "";
                        if ("00009999".equals(getMapDefStr(recept._data, "TOP2_COMPANYCD"))) {
                            top2MockName = getMapDefStr(recept._data, "TOP2_COMPANY_TEXT"); // 模試偏差値 模試名2(その他)
                        } else {
                            top2MockName = getMapDefStr(recept._data, "TOP2_COMPANYCDNAME");  // 模試偏差値 模試名2
                        }
                        final int t2len = KNJ_EditEdit.getMS932ByteLength(top2MockName);
                        final String t2field = t2len > 14 ? "_3" : t2len > 10 ? "_2" : "_1";
                        svf.VrsOutn("MOCK_NAME2" + t2field, line, top2MockName);  // 模試偏差値 模試名2
                    }
                    svf.VrsOutn("TOP2_2_1", line, getMapDefStr(recept._data, "TOP2_AVG3")); // 模試偏差値TOP2(3科)
                    svf.VrsOutn("TOP2_2_2", line, getMapDefStr(recept._data, "TOP2_AVG5")); // 模試偏差値TOP2(3科)

                } else {
                    if (isForm2) {
                        if ("7".equals(_param._testdiv)) {
                            svf.VrsOutn("COURSE1", line, getMapDefStr(scoreMap1, "9")); // 入試コース プレゼンテーション
                        } else {
                            svf.VrsOutn("COURSE1", line, getMapDefStr(scoreMap1, "1")); // 入試コース 国語
                        }
                        svf.VrsOutn("COURSE2", line, getMapDefStr(scoreMap1, "5")); // 入試コース 英語
                        svf.VrsOutn("COURSE3", line, getMapDefStr(scoreMap1, "2")); // 入試コース 数学
                        svf.VrsOutn("COURSE4", line, getMapDefStr(scoreMap1, "TOTAL4")); // 入試コース 合計
                        if (isTenkaKibouAmikake) {
                            svf.VrAttributen("COURSE4", line, AMIKAKE_ATTRIBUTE);
                        }
                        svf.VrsOutn("COURSE5", line, getMapDefStr(scoreMap1, "INTERVIEW_VALUE")); // 入試コース 面接
                    } else {
                        svf.VrsOutn("COURSE1", line, getMapDefStr(scoreMap1, "2")); // 適正検査 数学
                        svf.VrsOutn("COURSE2", line, getMapDefStr(scoreMap1, "5")); // 適正検査 英語
                        svf.VrsOutn("COURSE3", line, getMapDefStr(scoreMap1, "6")); // 適正検査 作文
                        svf.VrsOutn("COURSE4", line, getMapDefStr(scoreMap1, "TOTAL4")); // 適正検査 合計
                        if (isTenkaKibouAmikake) {
                            svf.VrAttributen("COURSE4", line, AMIKAKE_ATTRIBUTE);
                        }
                        svf.VrsOutn("COURSE5", line, getMapDefStr(scoreMap1, "INTERVIEW_VALUE")); // 適正検査 面接
                    }
                }

                if (!"1".equals(_param._notShowForce)) {
                    final Map scoreMap2 = _param.getScoreMap(recept, 2);
                    svf.VrsOutn("FORCE1", line, getMapDefStr(scoreMap2, "1")); // 質力診断テスト 国語
                    svf.VrsOutn("FORCE2", line, getMapDefStr(scoreMap2, "5")); // 質力診断テスト 英語
                    svf.VrsOutn("FORCE3", line, getMapDefStr(scoreMap2, "2")); // 質力診断テスト 数学
                    svf.VrsOutn("FORCE4", line, getMapDefStr(scoreMap2, "TOTAL4")); // 質力診断テスト 合計
                }
                if (isForm2 && !"6".equals(_param._testdiv)) {
                    final int shschoolnamelen = getMS932ByteCount(getString(recept._data, "SH_SCHOOL_NAME"));
                    svf.VrsOutn("ANOTHER_SCHOOL_NAME" + (shschoolnamelen > 18 ? "2_1" : "1"), line, getString(recept._data, "SH_SCHOOL_NAME")); // 併願校
                }
                svf.VrsOutn("TOTAL1", line, getString(recept._data, "RECR_VISIT_SCORE_TOTAL3")); // 科目合計
                svf.VrsOutn("TOTAL2", line, getString(recept._data, "RECR_VISIT_SCORE_TOTAL5")); // 科目合計
                svf.VrsOutn("TOTAL3", line, getString(recept._data, "RECR_VISIT_SCORE_TOTAL9")); // 科目合計

                final String remark1 = getRemark1(recept);
                svf.VrsOutn("RENARK" + (getMS932ByteCount(remark1) > 40 ? "1_2" : "1"), line, remark1); // 備考

                final String remark2 = getRemark2(recept);
                svf.VrsOutn("RENARK" + (getMS932ByteCount(remark2) > (isForm2 ? 30 : 50) ? "2_2" : "2"), line, remark2); // 備考

                svf.VrsOutn("PASS_COURSE", line, getString(recept._data, "SUC_EXAMCOURSE_ABBV")); // 合格コース
            }
            _hasData = true;

            svf.VrEndPage();
        }
    }

    private String getRemark2(final Recept recept) {
        final StringBuffer remark2 = new StringBuffer();
        if ("4".equals(getString(recept._data, "JUDGEDIV"))) {
            // 欠席の場合、備考に「欠席」を表示
            remark2.append(remark2.length() == 0 ? "" : " ").append(StringUtils.defaultString(getString(recept._data, "JUDGEDIV_NAME"))); // 「欠席」
            // 特別措置の場合、「欠席」の後に「特別措置」を表示
            if ("1".equals(getString(recept._data, "SPECIAL_REASON_DIV"))) {
                remark2.append(remark2.length() == 0 ? "" : " ").append("特別措置");
            }
        }
        final boolean shimaigenmen = null != getString(recept._data, "SISTER_FLG");
        if (shimaigenmen) {
            remark2.append(remark2.length() == 0 ? "" : " ").append("減免");
        }
        final boolean shijogenmen = null != getString(recept._data, "MOTHER_FLG");
        if (shijogenmen) {
            remark2.append(remark2.length() == 0 ? "" : " ").append("減免");
        }
        if (null != getString(recept._data, "CONFRPT_REMARK1")) {
            remark2.append(remark2.length() == 0 ? "" : " ").append(getString(recept._data, "CONFRPT_REMARK1"));
        }
        return remark2.toString();
    }

    private String getRemark1(final Recept recept) {
        String topAvgStr = "";
        String hyokatenStr = "";
//        if (null != recept._hyokaten) {
//            hyokatenStr = "活動評価点：" + recept._hyokaten.toString() + "点";
//        }
        final StringBuffer remark1 = new StringBuffer();
        final String[] kakuRemark = {getString(recept._data, "JUDGE_KIND_NAME"), topAvgStr, hyokatenStr, getString(recept._data, "RECR_WRAPUP_DAT_REMARK")};
        String ten = "";
        for (int i = 0; i < kakuRemark.length; i++) {
            if (StringUtils.isBlank(kakuRemark[i])) {
                continue;
            }
            remark1.append(ten).append(kakuRemark[i]);
            ten = "、";
        }
        return remark1.toString();
    }

//    // 営業日翌日
//    private static String eigyoubiYokujitsu(String date) {
//        if (StringUtils.isBlank(date)) {
//            return null;
//        }
//        try {
//            date = date.replace('/', '-');
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(Date.valueOf(date));
//            cal.add(Calendar.DAY_OF_MONTH, 1); // 翌日
//            int dow = cal.get(Calendar.DAY_OF_WEEK);
//            while (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) { // 土日ならさらに翌日
//                cal.add(Calendar.DAY_OF_MONTH, 1);
//                dow = cal.get(Calendar.DAY_OF_WEEK);
//            }
//            final DateFormat dateF = new SimpleDateFormat("yyyy-MM-dd");
//            final String rtn = dateF.format(cal.getTime());
//            log.debug(" date " + date + " ==> " + rtn);
//            return rtn.replace('-', '/');
//        } catch (Exception e) {
//            log.error("calc date exception!", e);
//        }
//        return null;
//    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        if (!m.containsKey(field)) {
            log.error("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static class Recept {
        final Map _data = new HashMap();
        final Map _scoreMap = new HashMap();
        final Map _testdiv3testcount2scoreMap = new HashMap();
        Integer _hyokaten = null;

        public static List getReceptList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                List addCol = new ArrayList();
                while (rs.next()) {
                    final Recept recept = new Recept();
                    recept._data.put("RECEPTNO",rs.getString("RECEPTNO"));
                    recept._data.put("REMARK10",rs.getString("REMARK10"));
                    recept._data.put("COURSEDIV",rs.getString("COURSEDIV"));
                    recept._data.put("FINSCHOOL_NAME",rs.getString("FINSCHOOL_NAME"));
                    recept._data.put("FINSCHOOL_DISTCDNAME",rs.getString("FINSCHOOL_DISTCDNAME"));
                    recept._data.put("NAME",rs.getString("NAME"));
                    recept._data.put("NAME_KANA",rs.getString("NAME_KANA"));
                    recept._data.put("SUC_EXAMCOURSECD",rs.getString("SUC_EXAMCOURSECD"));
                    recept._data.put("SUC_EXAMCOURSE_ABBV",rs.getString("SUC_EXAMCOURSE_ABBV"));
                    recept._data.put("EXAMCOURSECD",rs.getString("EXAMCOURSECD"));
                    recept._data.put("EXAMCOURSE_ABBV",rs.getString("EXAMCOURSE_ABBV"));
                    recept._data.put("TOTAL2",rs.getString("TOTAL2"));
                    recept._data.put("TOTAL4",rs.getString("TOTAL4"));
                    recept._data.put("SH_SCHOOL_NAME",rs.getString("SH_SCHOOL_NAME"));
                    recept._data.put("GENERAL_FLG",rs.getString("GENERAL_FLG"));
                    recept._data.put("SLIDE_FLG",rs.getString("SLIDE_FLG"));
                    recept._data.put("RECR_VISIT_SCORE_TOTAL3",rs.getString("RECR_VISIT_SCORE_TOTAL3"));
                    recept._data.put("RECR_VISIT_SCORE_TOTAL5",rs.getString("RECR_VISIT_SCORE_TOTAL5"));
                    recept._data.put("RECR_VISIT_SCORE_TOTAL9",rs.getString("RECR_VISIT_SCORE_TOTAL9"));
                    recept._data.put("RECR_WRAPUP_DAT_REMARK",rs.getString("RECR_WRAPUP_DAT_REMARK"));
                    recept._data.put("RECRUIT_NO",rs.getString("RECRUIT_NO"));
                    recept._data.put("CONFRPT_REMARK1",rs.getString("CONFRPT_REMARK1"));
                    recept._data.put("JUDGE_KIND",rs.getString("JUDGE_KIND"));
                    recept._data.put("JUDGE_KIND_NAME",rs.getString("JUDGE_KIND_NAME"));
                    recept._data.put("TOP1_AVG3",rs.getString("TOP1_AVG3"));
                    recept._data.put("TOP1_AVG5",rs.getString("TOP1_AVG5"));
                    recept._data.put("TOP2_AVG3",rs.getString("TOP2_AVG3"));
                    recept._data.put("TOP2_AVG5",rs.getString("TOP2_AVG5"));
                    recept._data.put("TOP1_COMPANYCD",rs.getString("TOP1_COMPANYCD"));
                    recept._data.put("TOP1_COMPANYCDNAME",rs.getString("TOP1_COMPANYCDNAME"));
                    recept._data.put("TOP1_COMPANY_TEXT",rs.getString("TOP1_COMPANY_TEXT"));
                    recept._data.put("TOP2_COMPANYCD",rs.getString("TOP2_COMPANYCD"));
                    recept._data.put("TOP2_COMPANYCDNAME",rs.getString("TOP2_COMPANYCDNAME"));
                    recept._data.put("TOP2_COMPANY_TEXT",rs.getString("TOP2_COMPANY_TEXT"));
                    recept._data.put("TOP_AVG",rs.getString("TOP_AVG"));
                    recept._data.put("INTERVIEW_VALUE",rs.getString("INTERVIEW_VALUE"));
                    recept._data.put("SISTER_FLG",rs.getString("SISTER_FLG"));
                    recept._data.put("MOTHER_FLG",rs.getString("MOTHER_FLG"));
                    recept._data.put("JUDGEDIV",rs.getString("JUDGEDIV"));
                    recept._data.put("JUDGEDIV_NAME",rs.getString("JUDGEDIV_NAME"));
                    recept._data.put("SPECIAL_REASON_DIV",rs.getString("SPECIAL_REASON_DIV"));
                    list.add(recept);
                }
                log.info(addCol.toString());
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String[][] testdivTestcount;
                if ("9".equals(param._testdiv)) {
                    testdivTestcount = new String[][] {{"1", null}, {"4", null}};
                } else {
                    testdivTestcount = new String[][] {{param._testdiv, param._testcount}};
                }

                for (int ti = 0; ti < testdivTestcount.length; ti++) {
                    final String sql = scoreSql(param, testdivTestcount[ti][0], testdivTestcount[ti][1]);
                    //log.debug(" sql = " + sql);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final Recept recept = (Recept) it.next();

                        ps.setString(1, getString(recept._data, "RECEPTNO"));
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            recept._scoreMap.put(rs.getString("TESTSUBCLASSCD"), "0".equals(rs.getString("ATTEND_FLG")) ? "*" : rs.getString("SCORE"));
                            recept._scoreMap.put("TOTAL4", rs.getString("TOTAL4"));
                        }

                        DbUtils.closeQuietly(rs);
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = getVisitActieDatSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Recept recept = (Recept) it.next();

                    ps.setString(1, getString(recept._data, "RECRUIT_NO"));
                    rs = ps.executeQuery();
                    Integer zero = new Integer(0);
                    Integer hyokaten = null;
                    while (rs.next()) {

                        final String namespare1 = rs.getString("NAMESPARE1");
                        if (NumberUtils.isDigits(namespare1)) {
                            if (null == hyokaten) {
                                hyokaten = zero;
                            }
                            hyokaten = new Integer(hyokaten.intValue() + Integer.parseInt(namespare1));
                        }
                    }
                    recept._hyokaten = hyokaten;

                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            if (1 == param.getFormPattern()) {
                try {
                    final String sql = scoreSql(param, "3", "2"); // 3:一般, 2:第2回
                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final Recept recept = (Recept) it.next();

                        ps.setString(1, getString(recept._data, "RECEPTNO"));
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            recept._testdiv3testcount2scoreMap.put(rs.getString("TESTSUBCLASSCD"), "0".equals(rs.getString("ATTEND_FLG")) ? "*" : rs.getString("SCORE"));
                            recept._testdiv3testcount2scoreMap.put("TOTAL4", rs.getString("TOTAL4"));
                        }
                        DbUtils.closeQuietly(rs);
                    }

                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                if ("9".equals(param._testdiv) && "1".equals(param._sort)) {
                    Collections.sort(list, new Testdiv3Testcount2ScoreComparator());
                }
            }

            for (int i = 0; i < list.size(); i++) {
                final Recept recept = (Recept) list.get(i);
                recept._data.put("PRINT_LINE_NO", String.valueOf(i + 1));
            }
            return list;
        }
        public static int getSqlColumnIndex(final ResultSet rs, final String chkStr) {
            int retval = -1;
            try {
                retval = rs.findColumn(chkStr);
            } catch (Exception ex) {
            } finally {
            }
            return retval;
        }

        public static String getVisitActieDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEQ ");
            stb.append("     , T2.NAME1 ");
            stb.append("     , T2.NAMESPARE1 ");
            stb.append(" FROM RECRUIT_VISIT_ACTIVE_DAT T1 ");
            stb.append(" INNER JOIN V_NAME_MST T2 ON T2.NAMECD1 = 'L408' ");
            stb.append("     AND T2.NAMECD2 = T1.SEQ ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.RECRUIT_NO = ? ");
            stb.append("     AND T1.SEQ_DIV = '1' ");
            stb.append("     AND T1.REMARK1 = '1' ");
            stb.append("  ");
            return stb.toString();
        }

        public static String scoreSql(final Param param, final String testdiv, final String testcount) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT TREC.TOTAL4, TSC.TESTSUBCLASSCD, TSC.ATTEND_FLG, TSC.SCORE ");
            stb.append(" FROM V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDEET003 ON TRDEET003.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDEET003.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDEET003.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDEET003.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDEET003.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDEET003.SEQ = '003' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSC ON TSC.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TSC.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TSC.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TSC.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TSC.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = '" + testdiv + "' ");
            if (null != testcount) {
                stb.append("     AND TRDEET003.REMARK1 = '" + testcount + "' ");
            }
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            stb.append("     AND TREC.RECEPTNO = ? ");
            return stb.toString();
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CALIB_ITEM AS ( ");
            stb.append(" SELECT ");
            stb.append("     TREC.RECEPTNO ");
            stb.append("   , TADET001.REMARK10 ");
            stb.append("   , SUBSTR(TADET001.REMARK10, 1, 1) AS COURSEDIV ");
            stb.append("   , TFIN.FINSCHOOL_NAME ");
            stb.append("   , NML001.NAME1 AS FINSCHOOL_DISTCDNAME ");
            stb.append("   , TAPPL.NAME ");
            stb.append("   , TAPPL.NAME_KANA ");
            stb.append("   , SCRSM.EXAMCOURSECD AS SUC_EXAMCOURSECD ");
            stb.append("   , SCRSM.EXAMCOURSE_ABBV AS SUC_EXAMCOURSE_ABBV ");
            stb.append("   , CRSM.EXAMCOURSECD ");
            stb.append("   , CRSM.EXAMCOURSE_ABBV ");
            stb.append("   , TREC.TOTAL2 ");
            stb.append("   , TREC.TOTAL4 ");
            stb.append("   , TADET016.REMARK1 AS SH_SCHOOL_NAME ");
            stb.append("   , TAPPL.GENERAL_FLG ");
            stb.append("   , TAPPL.SLIDE_FLG ");
            stb.append("   , CASE WHEN TAPCON.TOTAL3 IS NOT NULL ");
            stb.append("          THEN TAPCON.TOTAL3 ");
            stb.append("          ELSE TRVS.TOTAL3 ");
            stb.append("     END AS RECR_VISIT_SCORE_TOTAL3 ");
            stb.append("   , CASE WHEN TAPCON.TOTAL5 IS NOT NULL ");
            stb.append("          THEN TAPCON.TOTAL5 ");
            stb.append("          ELSE TRVS.TOTAL5 ");
            stb.append("     END AS RECR_VISIT_SCORE_TOTAL5 ");
            stb.append("   , CASE WHEN TAPCON.TOTAL_ALL IS NOT NULL ");
            stb.append("          THEN TAPCON.TOTAL_ALL ");
            stb.append("          ELSE TRVS.TOTAL9 ");
            stb.append("     END AS RECR_VISIT_SCORE_TOTAL9 ");
            stb.append("   , TRCW.REMARK AS RECR_WRAPUP_DAT_REMARK ");
            stb.append("   , TADET002.REMARK1 AS RECRUIT_NO ");
            stb.append("   , TAPCON.REMARK1 AS CONFRPT_REMARK1 ");
            stb.append("   , TRVD.JUDGE_KIND ");
            stb.append("   , NML025.NAME2 AS JUDGE_KIND_NAME ");
            stb.append("   , RVMD.TOP1_AVG3 ");
            stb.append("   , RVMD.TOP1_AVG5 ");
            stb.append("   , RVMD.TOP2_AVG3 ");
            stb.append("   , RVMD.TOP2_AVG5 ");
            stb.append("   , RVMD.TOP1_COMPANYCD ");
            stb.append("   , NML406_1.NAME1 AS TOP1_COMPANYCDNAME ");
            stb.append("   , RVMD.TOP1_COMPANY_TEXT ");
            stb.append("   , RVMD.TOP2_COMPANYCD ");
            stb.append("   , NML406_2.NAME1 AS TOP2_COMPANYCDNAME");
            stb.append("   , RVMD.TOP2_COMPANY_TEXT ");
            stb.append("   , RVMD.TOP_AVG ");
            stb.append("   , TINTV.INTERVIEW_VALUE ");
            stb.append("   , VALUE(TADET014.REMARK1, TADET014.REMARK2) AS SISTER_FLG ");
            stb.append("   , VALUE(TADET015.REMARK5, TADET015.REMARK1) AS MOTHER_FLG ");
            stb.append("   , TREC.JUDGEDIV ");
            stb.append("   , NML013.NAME1 AS JUDGEDIV_NAME ");
            stb.append("   , TAPPL.SPECIAL_REASON_DIV ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT TAPPL ON TAPPL.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPPL.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET001 ON TRDET001.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET001.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET001.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET001.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDET001.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET003.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET003.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET003.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDET003.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET003.SEQ = '003' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET001 ON TADET001.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET001.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET002 ON TADET002.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET002.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET002.SEQ = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET014 ON TADET014.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET014.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET014.SEQ = '014' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET015 ON TADET015.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET015.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET015.SEQ = '015' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET016 ON TADET016.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET016.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET016.SEQ = '016' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRSM ON CRSM.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND CRSM.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND CRSM.TESTDIV = '1' ");
            stb.append("         AND CRSM.COURSECD = TADET001.REMARK8 ");
            stb.append("         AND CRSM.MAJORCD = TADET001.REMARK9 ");
            stb.append("         AND CRSM.EXAMCOURSECD = TADET001.REMARK10 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST SCRSM ON SCRSM.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND SCRSM.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND SCRSM.TESTDIV = '1' ");
            stb.append("         AND SCRSM.COURSECD = TAPPL.SUC_COURSECD ");
            stb.append("         AND SCRSM.MAJORCD = TAPPL.SUC_MAJORCD ");
            stb.append("         AND SCRSM.EXAMCOURSECD = TAPPL.SUC_COURSECODE ");
            stb.append("     LEFT JOIN FINSCHOOL_MST TFIN ON TFIN.FINSCHOOLCD = TAPPL.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' ");
            stb.append("         AND NML001.NAMECD2 = TFIN.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = TREC.JUDGEDIV ");
            stb.append("     LEFT JOIN RECRUIT_VISIT_DAT TRVD ON TRVD.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRVD.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("     LEFT JOIN (SELECT T1.* ");
            stb.append("                   FROM RECRUIT_VISIT_SCORE_DAT T1 ");
            stb.append("                   INNER JOIN (SELECT YEAR, RECRUIT_NO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("                                    FROM RECRUIT_VISIT_SCORE_DAT T2 ");
            stb.append("                                    WHERE T2.SELECT_DIV = '1' ");
            stb.append("                                    GROUP BY YEAR, RECRUIT_NO ");
            stb.append("                                  ) T2 ON T2.YEAR = T1.YEAR  ");
            stb.append("                               AND T2.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("                               AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("                   WHERE T1.SELECT_DIV = '1' ");
            stb.append("                 ) TRVS ON TRVS.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRVS.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("         AND TRVD.SCORE_CHK = '1' ");
            stb.append("     LEFT JOIN RECRUIT_CONSULT_WRAPUP_DAT TRCW ON TRCW.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRCW.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("     LEFT JOIN RECRUIT_VISIT_MOCK_DAT RVMD ON RVMD.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND RVMD.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("         AND RVMD.MONTH = '99' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT TAPCON ON TAPCON.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPCON.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT TINTV ON TINTV.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TINTV.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TINTV.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TINTV.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = TRVD.JUDGE_KIND ");
            stb.append("     LEFT JOIN V_NAME_MST NML406_1 ON NML406_1.YEAR = TREC.ENTEXAMYEAR AND NML406_1.NAMECD1 = 'L406' AND VALUE(NML406_1.NAMECD2, 0) = VALUE(RVMD.TOP1_COMPANYCD, 0) ");
            stb.append("     LEFT JOIN V_NAME_MST NML406_2 ON NML406_2.YEAR = TREC.ENTEXAMYEAR AND NML406_2.NAMECD1 = 'L406' AND VALUE(NML406_2.NAMECD2, 0) = VALUE(RVMD.TOP2_COMPANYCD, 0) ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if ("9".equals(param._testdiv)) {
                stb.append("     AND TREC.TESTDIV <> '3' ");
                stb.append("     AND VALUE(TAPPL.GENERAL_FLG, '') = '1' ");

            } else {
                stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
                if ("2".equals(param._applicantdiv)) {
                    stb.append("     AND TRDET003.REMARK1 = '" + param._testcount + "' ");
                }
                if ("3".equals(param._testdiv)) {
                    stb.append("     AND VALUE(TAPPL.GENERAL_FLG, '') <> '1' ");
                }
            }
            if (!"9".equals(param._coursediv)) {
                stb.append("     AND SUBSTR(TADET001.REMARK10, 1, 1) = '" + param._coursediv + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append(" TADET001.REMARK10,");
            if ("1".equals(param._sort)) {
                stb.append(" CASE WHEN VALUE(TREC.JUDGEDIV, '') = '4' THEN 2 ELSE 1 END,");
                stb.append(" VALUE(TREC.TOTAL4, -1) DESC,");
                stb.append(" VALUE(INT(TINTV.INTERVIEW_VALUE), -1) DESC,");
            }
            stb.append(" TREC.RECEPTNO ");
            stb.append(" ) ");
            stb.append(" SELECT * FROM CALIB_ITEM ");
            return stb.toString();
        }
    }

    private static class Testdiv3Testcount2ScoreComparator implements Comparator {
        public int compare(final Object o1, final Object o2) {
            final Recept r1 = (Recept) o1;
            final Recept r2 = (Recept) o2;
            final String r1remark10 = (String) r1._data.get("REMARK10");
            final String r2remark10 = (String) r2._data.get("REMARK10");
            final int cmp = StringUtils.defaultString(r1remark10).compareTo(StringUtils.defaultString(r2remark10));
            if (cmp != 0) {
                return cmp;
            }
            final String r1total4 = (String) r1._testdiv3testcount2scoreMap.get("TOTAL4");
            final String r2total4 = (String) r2._testdiv3testcount2scoreMap.get("TOTAL4");
            if (NumberUtils.isNumber(r1total4) && NumberUtils.isNumber(r2total4)) {
                final double r1total4i = Double.parseDouble(r1total4);
                final double r2total4i = Double.parseDouble(r2total4);
                if (r1total4i - r2total4i != 0) {
                    return - (int) (r1total4i - r2total4i); // 降順
                }
            } else if (NumberUtils.isNumber(r1total4)) {
                return -1;
            } else if (NumberUtils.isNumber(r2total4)) {
                return 1;
            }
            return getString(r1._data, "RECEPTNO").compareTo(getString(r2._data, "RECEPTNO"));
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 71899 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _testcount;
        final String _coursediv;
        final String _sort;
        final String _advanceAmikake;
        final String _advanceScore;
        final int _advanceScoreInt;
        final String _notShowForce;
        final String _loginDate;
        final String _applicantdivName;
        final String _testdivName;
        final String _testdivDate;
        final String _currentTime;
        final String _testdiv0name;
        final String _cmd;

        final String _citynameTitle;
        final String _schoolnameTitle;
        final String _examDivName;
        final String _examDivName2;
        final String _courseName1;
        final String _courseName2;
        final String _courseName3;
        final String _courseName4;
        final String _courseName5;
        final String _forceName1;
        final String _forceName2;
        final String _forceName3;
        final String _forceName4;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testcount = request.getParameter("TESTCOUNT");
            _coursediv = request.getParameter("COURSEDIV");
            _sort = request.getParameter("SORT");
            _advanceAmikake = request.getParameter("ADVANCE_AMIKAKE");
            _advanceScore = request.getParameter("ADVANCE_SCORE");
            _advanceScoreInt = !NumberUtils.isDigits(_advanceScore) ? 0 : Integer.parseInt(_advanceScore);
            _notShowForce = request.getParameter("NOT_SHOW_FORCE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _cmd = request.getParameter("cmd");
            _applicantdivName = getApplicantdivName(db2);
            _testdivName = getTestdivNameMst(db2, _testdiv, "L004", "ABBV1");
            if ("9".equals(_testdiv)) {
                _testdivDate = getTestdivNameMst(db2, "3", "L004", "NAME3");
            } else {
                if ("1".equals(_testcount)) {
                    _testdivDate = getTestdivNameMst(db2, _testdiv, "L004", "NAMESPARE1");
                } else if ("2".equals(_testcount)) {
                    _testdivDate = getTestdivNameMst(db2, _testdiv, "L004", "NAME3");
                } else if ("3".equals(_testcount)) {
                    _testdivDate = getTestdivNameMst(db2, _testdiv, "L044", "NAMESPARE1");
                } else if ("4".equals(_testcount)) {
                    _testdivDate = getTestdivNameMst(db2, _testdiv, "L044", "NAME3");
                } else if ("5".equals(_testcount)) {
                    _testdivDate = getTestdivNameMst(db2, _testdiv, "L059", "NAMESPARE1");
                } else {
                    _testdivDate = null;
                }
            }

            if (2 == getFormPattern()) {
                if ("2".equals(_testdiv)) {
                    _courseName1 = "";
                    _courseName2 = "";
                    _courseName3 = "";
                    _courseName4 = "";
                    _courseName5 = "面接";
                } else {
                    _courseName1 = "7".equals(_testdiv) ? "プレ" : "国語";
                    _courseName2 = "英語";
                    _courseName3 = "数学";
                    _courseName4 = "合計";
                    _courseName5 = "6".equals(_testdiv) ? "" : "面接";
                }
            } else { // if (1 == getFormPattern()) {
                if ("1".equals(_testdiv)) {
                    _courseName1 = "";
                    _courseName2 = "";
                    _courseName3 = "";
                    _courseName4 = "";
                    _courseName5 = "面接";
                } else {
                    _courseName1 = "数学";
                    _courseName2 = "英語";
                    _courseName3 = "作文";
                    _courseName4 = "合計";
                    _courseName5 = "面接";
                }
            }
            _citynameTitle = "6".equals(_testdiv) ? "" : "転科希望";
            _schoolnameTitle = "6".equals(_testdiv) ? "" : "中学名";
            _examDivName = _testdivName + ("6".equals(_testdiv) ? "進級テスト" : "学科試験");
            _examDivName2 = "6".equals(_testdiv) ? "" : "学力診断テスト";
            _forceName1 = "6".equals(_testdiv) ? "" : "国語";
            _forceName2 = "6".equals(_testdiv) ? "" : "英語";
            _forceName3 = "6".equals(_testdiv) ? "" : "数学";
            _forceName4 = "6".equals(_testdiv) ? "" : "合計";
            _currentTime = currentTime(db2);
            _testdiv0name = getTestdiv0Name(db2);
        }

        private Map getScoreMap(final Recept recept, final int flg) {
            final Map scoreMap = new HashMap();
            if (1 == getFormPattern()) {
                if (flg == 1) {
                    // 適性検査
                    scoreMap.putAll(recept._scoreMap);
                    scoreMap.put("INTERVIEW_VALUE", recept._data.get("INTERVIEW_VALUE"));
                } else if (flg == 2) {
                    // 学力診断テスト
                    if ("1".equals(getString(recept._data, "GENERAL_FLG"))) {
                        scoreMap.putAll(recept._testdiv3testcount2scoreMap);
                        scoreMap.put("INTERVIEW_VALUE", recept._data.get("INTERVIEW_VALUE"));
                    }
                }
            } else if (2 == getFormPattern()) {
                if ("2".equals(_testdiv) || "3".equals(_testdiv) || "5".equals(_testdiv) || "7".equals(_testdiv)) {
                    // 学科試験
                    if (flg == 1) {
                        if (!"1".equals(getString(recept._data, "GENERAL_FLG"))) {
                            scoreMap.putAll(recept._scoreMap);
                            scoreMap.put("INTERVIEW_VALUE", recept._data.get("INTERVIEW_VALUE"));
                        }
                    }
                } else if ("6".equals(_testdiv)) {
                    // 学力診断テスト
                    if (flg == 1) {
                        scoreMap.putAll(recept._scoreMap);
                        scoreMap.put("INTERVIEW_VALUE", recept._data.get("INTERVIEW_VALUE"));
                    }
                }
            }
            return scoreMap;
        }

        private int getFormPattern() {
            if ("1".equals(_testdiv) || "4".equals(_testdiv) || "9".equals(_testdiv)) {
                return 1;
            } else if ("2".equals(_testdiv) || "3".equals(_testdiv) || "5".equals(_testdiv) || "6".equals(_testdiv) || "7".equals(_testdiv)) {
                return 2;
            }
            return -1;
        }

        private String getApplicantdivName(final DB2UDB db2) {
            String applicantdivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  applicantdivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantdivName;
        }

        private String getTestdiv0Name(final DB2UDB db2) {
            final String namecd1 = "L034";
            String testDiv0Name = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testcount + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDiv0Name = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDiv0Name;
        }

        private String getTestdivNameMst(final DB2UDB db2, final String testdiv, final String namecd1, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private static String currentTime(final DB2UDB db2) {
            final Calendar cal = Calendar.getInstance();
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String dow = String.valueOf(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int min = cal.get(Calendar.MINUTE);
            final DecimalFormat df = new DecimalFormat("00");
            return KNJ_EditDate.h_format_JP(db2, year + "-" + month + "-" + dayOfMonth) + "(" + dow + ") " + df.format(hour) + ":" + df.format(min);
        }
    }
}

// eof


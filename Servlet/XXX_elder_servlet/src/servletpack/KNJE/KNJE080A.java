// kanji=漢字

package servletpack.KNJE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJA.detail.KNJ_TransferRecSql;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *  学校教育システム 賢者 [進路情報管理] 学業成績証明書
 */
public class KNJE080A {

    private static final Log log = LogFactory.getLog(KNJE080A.class);

    private static final String CERTIF_KINDCD = "151";
    private static final String SCHOOL_KIND = "H";
    private static final String SEMEALL = "9";

    private Param _param;
    private boolean _hasData; //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        //  print設定
        response.setContentType("application/pdf");

        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception e) {
            log.error("[KNJE080A]DB2 open error!", e);
        }

        KNJServletUtils.debugParam(request, log);

        _param = new Param(db2, request);

        try {
            //  ＳＶＦ設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＳＶＦ作成
            final List<Map<String, String>> schregnoMapList = _param.getSchregnoMapList(db2);

            final Map<String, String> majorNumberMap = new TreeMap<String, String>(_param._majorStartNumberMap);

            for (int i = 0; i < schregnoMapList.size(); i++) {
                final Map<String, String> schregnoMap = schregnoMapList.get(i);
                final String schregno = schregnoMap.get("SCHREGNO");

                final Student student = new Student(schregno);
                student.load(db2, _param);

                log.info(" student " + student._schregno);

                // 発行番号
                if ("1".equals(_param._notPrintCertifno)) {
                    // 発行番号は印刷しない
                    student._certifNumber = null;
                } else {
                    if ("1".equals(_param._useCertifnoStart)) {
                        // 印刷する発行番号を指定する
                        if (NumberUtils.isDigits(_param._certifnoStart)) {
                            final int number = Integer.parseInt(_param._certifnoStart) + i;
                            final int keta = Math.max(_param._certifnoStart.length(), String.valueOf(number).length());
                            student._certifNumber = String.format("%0" + keta + "d", number);
                        }
                    } else {
                        // 自動附番
                        final String coursecd = KnjDbUtils.getString(schregnoMap, "COURSECD");
                        final String majorcd = KnjDbUtils.getString(schregnoMap, "MAJORCD");
                        final String number = majorNumberMap.get(coursecd + majorcd);
                        if (NumberUtils.isDigits(number)) {
                            student._certifNumber = number;
                            final int newnumber = Integer.parseInt(number) + 1;
                            final int keta = Math.max(number.length(), String.valueOf(newnumber).length());
                            majorNumberMap.put(coursecd + majorcd, String.format("%0" + keta + "d", newnumber));
                        }
                    }
                }

                final Form form = new Form(_param, svf);

                //成績証明書
                form.print(db2, student);
                if (form._hasData) {
                    _hasData = true;
                }

            }
            _param.close();

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            //  終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.commit();
            db2.close(); // DBを閉じる
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

    private static class Form {

        private Param _param;
        private final Vrw32alp _svf;
        private boolean _hasData;
        public String _formName;
        private TreeMap<Integer, Map<String, Title>> _pageYearTitleMap = null;
        private Set<String> _pageLogs;
        boolean _isLastPage;
        protected Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap<String, Map<String, SvfField>>();
        protected Map<String, SvfForm> _svfFormInfoMap = new HashMap<String, SvfForm>();
        private int _formNenyou;

        private int _lineMax1;
        private int _lineMax12;
        private int _lineMax122;
        private Map<String, File> _flagFormnameMap = new HashMap<String, File>();

        Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        public void print(final DB2UDB db2, final Student student) {

            _formName = "KNJE080A.frm";
            _formNenyou = 4;
            setConfigForm(student, _param);

            _pageYearTitleMap = getPageYearTitleMap(student);
            if (_pageYearTitleMap.isEmpty()) {
                log.warn(" pageYearMap empty.");
            }
            _pageLogs = new HashSet<String>();

            for (final Integer page : _pageYearTitleMap.keySet()) {

                _isLastPage = page == _pageYearTitleMap.lastKey();

                final String info = " schregno = " + student._schregno + ", form = " + _formName + " (nen = " + _formNenyou + ")";
                if (!_pageLogs.contains(info)) {
                    log.info(info);
                    _pageLogs.add(info);
                }

                if (_param._isOutputDebug) {
                    if (_pageYearTitleMap.size() > 1) {
                        log.info(" page = " + page + " / " + _pageYearTitleMap.lastKey());
                    }
                }

                _svf.VrSetForm(_formName, 4);
                if (_param._isOutputDebug) {
                    log.info(" formName = " + _formName);
                }
                setFormInfo(student, _svf);

                final RecordPages recordPages = getRecordPages(student, page);
                if (_param._isOutputDebug) {
                    log.info(" recordPages = " + recordPages.status());
                }

                //学習の記録出力
                for (final List<Record> recordPage : recordPages._recordPageList) {

                    // 学校名、校長名のセット
                    printHeader(student, page);
                    // 氏名、住所等出力
                    printAddress(db2, student);
                    // 出欠の出力
                    printShukketsu(student, page);

                    printRecord(student, page, recordPage);

                    _svf.VrEndPage();
                }
            }
        }

        /*
         *  学校情報
         */
        private void printHeader(final Student student, final Integer page) {
            //過卒生対応年度取得->掲載日より年度を算出
            final Map<String, String> schoolInfoMap = student._schoolInfoMap;
            if (!schoolInfoMap.isEmpty()) {
                vrsOut("DATE", student._dateStr); //記載日
                vrsOut("NENDO", student._nendo); //年度
                student._syoshoname = defstr(KnjDbUtils.getString(schoolInfoMap, "SYOSYO_NAME")); //証書名
                student._syoshoname2 = defstr(KnjDbUtils.getString(schoolInfoMap, "SYOSYO_NAME2")); //証書名２
//                if ("0".equals(KnjDbUtils.getString(schoolInfoMap, "CERTIF_NO"))) {
//                    student._isOutputCertifNo = true; //証書番号の印刷 0:あり,1:なし
//                }
                student._isOutputCertifNo = true; //証書番号の印刷 0:あり,1:なし
                vrsOut("SCHOOLNAME", KnjDbUtils.getString(schoolInfoMap, "SCHOOLNAME1")); //学校名
                vrsOut("JOBNAME", KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_JOBNAME")); //校長職名
                vrsOut("STAFFNAME", KnjDbUtils.getString(schoolInfoMap, "PRINCIPAL_NAME")); //校長名
                vrsOut("REMARK3", KnjDbUtils.getString(schoolInfoMap, "REMARK3"));
            }

            // 指示画面の備考
            final int mojisu = 30;
            final List<String> bikoList = KNJ_EditKinsoku.getTokenList(_param._biko, mojisu * 2, 4);
            for (int i = 0; i < bikoList.size(); i++) {
                vrsOutn("REMARK", i + 1, bikoList.get(i));
            }
        }

        /*
         *  個人情報
         */
        private void printAddress(final DB2UDB db2, final Student student) {
            if (null == student._personalInfoMap) {
                return;
            }

            if (student._schregno != null) {
                vrsOut("schregno", student._schregno);
            }
            student._entDate = KnjDbUtils.getString(student._personalInfoMap, "ENT_DATE");

            // 証書名
            final String certifName;
            if (student._isOutputCertifNo) {
                // 証明書番号が無い場合 5スペース挿入
                certifName = student._syoshoname + (StringUtils.isEmpty(student._certifNumber) ? "     " : student._certifNumber) + student._syoshoname2;
            } else {
                certifName = student._syoshoname + "     " + student._syoshoname2;
            }
            vrsOut("SYOSYO_NAME", certifName); //証書番号

            // 氏名
            final String rsName = KnjDbUtils.getString(student._personalInfoMap, "NAME");
            final String name;
            String d = null;
            if ("1".equals(KnjDbUtils.getString(student._personalInfoMap, "USE_REAL_NAME"))) {
                if ("1".equals(KnjDbUtils.getString(student._personalInfoMap, "NAME_OUTPUT_FLG"))) {
                    name = defstr(KnjDbUtils.getString(student._personalInfoMap, "REAL_NAME")) + "(" + defstr(rsName) + ")";
                    d = " name (real + flg) : " + name;
                } else {
                    name = KnjDbUtils.getString(student._personalInfoMap, "REAL_NAME");
                    d = " name (real) : " + name;
                }
            } else {
                name = rsName;
                d = " name : " + name;
            }
            if (_param._isOutputDebug) {
                log.info(d);
            }
            vrsOutWithCheckKeta(new String[] { "NAME", "NAME2", "NAME3" }, name);

            // 生年月日
            vrsOut("BIRTHDAY", Util.formatBirthday(db2, _param, student, KnjDbUtils.getString(student._personalInfoMap, "BIRTHDAY"), _param._isSeireki || "1".equals(KnjDbUtils.getString(student._personalInfoMap, "BIRTHDAY_FLG"))));

            //　入学卒業と日付
            printEntGrd(db2, student);
        }

        // 入学卒業と日付の出力
        private void printEntGrd(final DB2UDB db2, final Student student) {

            // 入学
            final String entDiv = defstr(KnjDbUtils.getString(student._personalInfoMap, "ENT_DIV"));
            final String entDivName = Arrays.asList("1", "2", "3").contains(entDiv) ? "入学" : defstr(KnjDbUtils.getString(student._personalInfoMap, "ENTER_NAME"));
            vrsOut("TRANSFER1", defstr(KNJ_EditDate.h_format_JP(db2, student._entDate)) + " " + entDivName);

            // 卒業
            final String grdDiv = KnjDbUtils.getString(student._personalInfoMap, "GRD_DIV");
            if ("1".equals(_param._printSotsu)) {
                String formatted = null;
                if (!StringUtils.isEmpty(_param._printGrdDate)) {
                    formatted = KNJ_EditDate.h_format_JP(db2, _param._printGrdDate);
                }
                vrsOut("TRANSFER2", defstr(formatted) + " 卒業");
            } else {
                String formatted = null;
                if (null != grdDiv && !"4".equals(grdDiv)) {
                    formatted = KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(student._personalInfoMap, "GRD_DATE"));
                    vrsOut("TRANSFER2", defstr(formatted) + " " + defstr(KnjDbUtils.getString(student._personalInfoMap, "GRADU_NAME")));
                } else {
                    final String gradeCd = _param.getGradeCdOfGrade(_param._year, student._regdGradeCd);
                    vrsOut("TRANSFER2", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度 第" + defstr(NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : gradeCd) + "学年 在学中");
                }
            }
        }

        /**
         *  出欠データ
         **/
        private void printShukketsu(final Student student, final Integer page) {

            final int flg1 = 1;
            final List<String> printYearList = new ArrayList<String>();
            final Map<String, Map<String, String>> yearAttendMap = new HashMap<String, Map<String, String>>();
            for (final Map<String, String> row : student._attendList) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                yearAttendMap.put(year, row);
                final Title title = getPrintPosition(flg1, student, page, year);
                if (null != title && null != title._dropFlg) {
                    continue;
                }
                printYearList.add(year);
            }

            for (final String year : printYearList) {
                final Title title = getPrintPosition(flg1, student, page, year);
                final int i = title._position;
                final Map<String, String> row = yearAttendMap.get(year);

                vrsOutnNotNull("attend_5", i, KnjDbUtils.getString(row, "MLESSON")); // 出席すべき日数
                vrsOutnNotNull("attend_6", i, KnjDbUtils.getString(row, "SICK")); // 欠席
            }
        }

        private Record printSvfBlank(final int i, final Record record) {
            record.vrsOutR("CLASSNAME", String.valueOf(i)); // 教科コード
            record.vrAttributeR("CLASSNAME", "X=10000");
            return record;
        }

        /*
         *  学習の記録
         */
        private void printRecord(final Student student, final Integer page, final List<Record> recordPage) {
            boolean hasdata0 = false;

            for (final Record record : recordPage) {
                record.endrecord();
                hasdata0 = true;
            }

            final int linex2 = recordPage.size(); //行数

            final List<Acc> lastList = new ArrayList<Acc>();
            Record last = null;
            if (_param._isOutputDebug) {
                log.info(" not Form1.");
            }
            lastList.add(student._accSogo); // 総合的な学習の時間
            lastList.add(student._accShokei); // 小計
            lastList.add(student._accAbroad); // 留学
            lastList.add(student._accGoukei); // 総合計
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
                record.printSvfCredits(student, page, acc);
                if (i == lastList.size() - 1) {
                    last = record;
                    last._name = "last";
                } else {
                    record.endrecord();
                }
            }
            if (!hasdata0) {
                //  学習情報がない場合の処理
                if (null == last) {
                    last = nextRecord2(null, "last");
                }
                last.vrsOutR("CLASSNAME", "-"); //教科名
                last.vrAttributeR("CLASSNAME", "X=10000");
            }
            if (null == last) {
                last = nextRecord2(null, "last");
            }
            last.endrecord();
            _hasData = true;
        }

        private RecordPages getRecordPages(final Student student, final Integer page) {

            final String specialDiv0 = "0"; // 各学科に共通する各教科・科目
            final String specialDiv1 = "1"; // 主として専門学科において開設される各教科・科目
            final int maxRecord1 = _lineMax1;

            final List<StudyrecClass> allClassList = StudyrecClass.groupByClassList(student._studyrecList);

            if (_param._isOutputDebug) {
                log.info(" classList size = " + allClassList.size() + " (studyrecList size = " + student._studyrecList.size() + ")");
            }

            final Map<String, List<StudyrecClass>> specialDivClassListMap = new TreeMap<String, List<StudyrecClass>>();
            for (final StudyrecClass clazz : allClassList) {
                Util.getMappedList(specialDivClassListMap, StringUtils.defaultString(clazz._specialDiv, specialDiv0)).add(clazz); // CLASS_MST.SPECIAL_DIVがnullは"0"として扱う
            }

            final RecordPages recordPages = new RecordPages();

            for (final String specialDiv : Arrays.asList(specialDiv0, specialDiv1)) {
                final List<StudyrecClass> classList = Util.getMappedList(specialDivClassListMap, specialDiv);

                for (int cli = 0; cli < classList.size(); cli++) {

                    final StudyrecClass clazz = classList.get(cli);

                    if (_param._isOutputDebugPrint) {
                        log.info(" classcd = " + clazz._classcd + ", list size = " + clazz._studyrecList.size());
                    }

                    final int subclassLines = clazz._studyrecList.size();

                    for (int sri = 0; sri < clazz._studyrecList.size(); sri++) {
                        final Studyrec studyrec = clazz._studyrecList.get(sri);

                        final int ln = recordPages.currentPage().size() % _lineMax1;
                        final Record record = nextRecord(recordPages, " studyrec i = " + sri);
                        record.printSvfClassnameSubclassname(student, studyrec._classcd, studyrec._classname, studyrec._subclassname, ln, subclassLines);
                        record.printSvfHyotei(student, page, studyrec, "");
                    }
                }

                if (specialDiv0.equals(specialDiv)) {
                    if (recordPages.currentPage().size() > maxRecord1) {
                        for (final Iterator<Record> lit = recordPages.currentPage().listIterator(maxRecord1); lit.hasNext();) {
                            final Record r = lit.next();
                            log.info(" remove record : " + r);
                            lit.remove();
                        }
                    }
                    // 空行追加
                    for (int i = recordPages.currentPage().size(); i < _lineMax1; i++) {
                        final Record record = nextRecord(recordPages, " studyrec blank i = " + i);
                        printSvfBlank(i, record);
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

        public Title getPrintPosition(final int flg, final Student student, final Integer page, final String year) {
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
                final Title title = getTitleOfPageYear(year, page);
                if (null == title) {
                    final String error = " no title " + year + " in "
                            + Util.getMappedMap(_pageYearTitleMap, page).keySet();
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
                if (_param._isOutputDebug) {
                    log.info(" attend title year = " + year + ", pos = " + i);
                }
                return i;
            }
            return null;
        }

        private TreeMap<Integer, Map<String, Title>> getPageYearTitleMap(final Student student) {
            final TreeMap<Integer, Map<String, Title>> pageYearTitleMap = new TreeMap<Integer, Map<String, Title>>();
            int page = 1;
            for (final Title title : student._yearTitleMap.values()) {
                while (page * _formNenyou < title._position) {
                    page += 1;
                }
                Util.getMappedMap(pageYearTitleMap, page).put(title._year, new Title(title._year, title._annual, title._position.intValue() - (page - 1) * _formNenyou, title._nendo, title._dropFlg));
            }
            if (_param._isOutputDebug) {
                log.info(Util.debugMapToStr("yearTitleMap = ", student._yearTitleMap) + " => " + Util.debugMapToStr("pageYearTitleMap = ", pageYearTitleMap));
            }
            return pageYearTitleMap;
        }

        public void setConfigForm(final Student student, final Param param) {
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
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            if (readFile) {
                final TreeMap<String, String> flags = getFlagMap(student, svfForm, param);
                final String flag = _formName + ":" + Util.mkString(flags, "|");
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

        private TreeMap<String, String> getFlagMap(final Student student, final SvfForm svfForm, final Param param) {
            final TreeMap<String, String> flags = new TreeMap<String, String>();
            return flags;
        }

        private void modifyForm(final Param param, final TreeMap<String, String> flags, final SvfForm svfForm) {
        }

        private void setFormInfo(final Student student, final Vrw32alp svf) {
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
                    }
                } catch (Throwable t) {
                    log.warn(" no class SvfField.", t);
                }
            }

            _lineMax1 = 33;
            _lineMax12 = _lineMax1 * 2 - 4;
            _lineMax122 = _lineMax1 * 2;
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

        public int getFieldKeta(final String fieldname, final int defaultKeta) {
            final SvfField f = getField(fieldname);
            if (null != f) {
                return f._fieldLength;
            }
            return defaultKeta;
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

        private int vrsOut(final String fieldname, final String data) {
            SvfField f = Util.getMappedMap(_formFieldInfoMap, _formName).get(fieldname);
            if (null == f) {
                if (_param._isOutputDebugPrint) {
                    log.warn("no field : " + _formName + "." + fieldname + " (data  = " + data + ")");
                }
            } else if (_param._isOutputDebugField) {
                log.info("svf.VrsOut(\"" + fieldname + "\", " + (null == data ? "null" : "\"" + data + "\"") + ");");
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
                log.info("svf.VrsOutn(\"" + fieldname + "\", " + gyo + ", " + (null == data ? "null" : "\"" + data + "\"") + ");");
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
                log.info("svf.VrAttribute(\"" + fieldname + "\", " + (null == attribute ? "null" : "\"" + attribute + "\"") + ");");
            }
            return _svf.VrAttribute(fieldname, attribute);
        }

        private Record nextRecord2(final List<Record> recordList, final String name) {
            final Record record = new Record();
            record._name = name;
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

            private void printSvfCredits(final Student student, final Integer page, final Acc acc) {
                vrsOutR("ITEM", defstr(acc._subclassname, acc._item));

                for (final String year : acc.years()) {
                    final Title title = getTitleOfPageYear(year, page);
                    if (null != title && title._position < Acc.N) {
                        final String no = title._position.toString();
                        String val = null;
                        if (acc.hasCredits(year)) {
                            val = acc._credits.get(year); //修得単位数
                        } else if (acc.hasRishutyuCredits(year)) {
                            val = kakko(acc._rishutyuCredits.get(year)); //単位マスタの単位数
                        }
                        if (null != val) {
                            vrsOutR("total" + no, val);
                        }
                    }
                }
                if (_isLastPage) {
                    String val = acc.hasAnyCredits() ? acc.sumCredits() : null; // 修得単位数
//                    final String sumRishutyuCredits = acc.hasRishutyuCredits() ? acc.sumRishutyuCredits() : null; // 単位マスタの単位数
//                    if (NumberUtils.isNumber(sumRishutyuCredits) && Double.parseDouble(sumRishutyuCredits) > 0) {
//                        val = kakko(Util.addNumber(val, sumRishutyuCredits));
//                    }
                    vrsOutR(KNJ_EditEdit.getMS932ByteLength(val) > 4 ? "total_2" : "total", val);
                }
            }

            private void printSvfHyotei(final Student student, final Integer page, final Studyrec studyrec, final String sfx) {
                String gradeCreditKei = null;
                //学年ごとの出力
                for (final Grades g : studyrec._gradesList) {

                    //小計・総合的な学習の時間・留学
                    final Title title = getPrintPosition(0, student, page, g._year);
                    if (null == title || null != title._dropFlg) {
                        continue;
                    }
                    final int i = title._position;
                    String printGrades = null;
                    if (Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag)) {
                        if ("1".equals(_param._printRi)) {
                            printGrades = "履";
                        } else {
                            // 「履修中は印刷しない」「履修中は「履」を印刷する」共にチェックはない
                            printGrades = g.getHyotei(_param);
                        }
                    } else {
                        printGrades = g.getHyotei(_param);
                    }
                    vrsOutR("GRADES" + i + sfx, printGrades); //評定

                    String totalTargetCredit = null;
                    if (g._gradeCredit != null) {
                        String printCredit = null;
                        if (Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag)) {
                            if ("1".equals(_param._printRi)) {
                                // 履修単位は非表示
                            } else {
                                // 「履修中は印刷しない」「履修中は「履」を印刷する」共にチェックはない
                                printCredit = kakko(g._gradeCredit);
                            }
                        } else {
                            printCredit = studyrec.yearCredit(g._year); //修得単位数
                            totalTargetCredit = printCredit;
                        }
                        vrsOutR("tani" + i + sfx, printCredit);
                        if (null != totalTargetCredit) {
                            if (_isLastPage) {
                                // 単位が履修中単位と混在した際は履修中単位は含めない
                                gradeCreditKei = Util.addNumber(gradeCreditKei, totalTargetCredit);
                                if (_param._isOutputDebugPrint) {
                                    log.info(" subclass = " + studyrec._subclasscd + ":" + studyrec._subclassname + " | " + g._year + " / " + totalTargetCredit + "(" + gradeCreditKei + ")");
                                }
                                vrsOutR("CREDIT" + sfx, String.valueOf(gradeCreditKei)); //単位数の合計（科目）
                            }
                        }
                    }
                }
            }

            private void printSvfClassnameSubclassname(final Student student, final String classcd, final String classname, final String subclassname, final int nameLine, final int subclassLines) {
                //科目コードの変わり目
                vrsOutR("CLASSNAME", classname); //教科名

                final int _subclassRecordHeight = 87;
                final int _subForm1yStart = 1200;
                svfFieldAttributeClassname("CLASSNAME", classname, nameLine, _subclassRecordHeight, _subForm1yStart, subclassLines); //教科名
                if (KNJ_EditEdit.getMS932ByteLength(subclassname) > 16) {
                    vrsOutR("SUBCLASSNAME2", subclassname); //科目名
                } else {
                    svfFieldAttributeSubclassname("SUBCLASSNAME", subclassname, nameLine, _subclassRecordHeight, _subForm1yStart, student);
                    vrsOutR("SUBCLASSNAME", subclassname); //科目名
                }
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
                    final Student student) {
                final int minnum = 14; //最小設定文字数
                final int maxnum = 40; //最大設定文字数
                int width = -1; //フィールドの幅(ドット)
                SvfForm form = _svfFormInfoMap.get(_formName);
                if (null != form) {
                    final SvfForm.Field field = form.getField(fieldname);
                    if (null != field) {
                        width = field._endX - field._position._x;
                    }
                }
                final int charHeight = height;
                final KNJSvfFieldModify svfobj = new KNJSvfFieldModify(width, charHeight, ystart, minnum, maxnum);
                final float charSize = svfobj.getCharSize(subclassname);
                final int yModify = (int) svfobj.getYjiku(ln, charSize, height);
                vrAttributeR(fieldname, "Y=" + yModify);
                vrAttributeR(fieldname, "Size=" + charSize);
            }

            /*
             * SVF-FORM フィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
             */
            private void svfFieldAttributeClassname(final String fieldname, final String classname, final int ln, final int height, final int ystart, final int subclassLines) {
                int width = -1;
                SvfForm form = _svfFormInfoMap.get(_formName);
                if (null != form) {
                    final SvfForm.Field field = form.getField(fieldname);
                    if (null != field) {
                        width = field._endX - field._position._x;
                    }
                }
                final int minnum = 10; //最小設定文字数
                final int maxnum = 20; //最大設定文字数
                final int charHeight = height;
                final KNJSvfFieldModify svfobj = new KNJSvfFieldModify(width, charHeight, ystart, minnum, maxnum);
                final float charSize = svfobj.getCharSize(classname);
                final int y = (int) (svfobj.getYjiku(ln, charSize, height) + ((subclassLines - 1) * height / 2.0));
                vrAttributeR(fieldname, "Y=" + y);
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
                final String subclasscd) {
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

        final String _specialDiv;
        final String _classcd;
        final String _classname;
        final List<Studyrec> _studyrecList;

        public StudyrecClass(final String specialDiv, final String classcd, final String classname) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _classname = classname;
            _studyrecList = new ArrayList<Studyrec>();
        }

        private static List<StudyrecClass> groupByClassList(final List<Studyrec> studyrecList) {
            final List<StudyrecClass> rtn = new ArrayList();
            StudyrecClass current = null;
            for (final Studyrec studyrec : studyrecList) {
                if (null == current || null == studyrec._classcd || !studyrec._classcd.equals(current._classcd)) {
                    current = new StudyrecClass(studyrec._specialDiv, studyrec._classcd, defstr(studyrec._classname));
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
                final String d065Flg) {
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
            if (!NumberUtils.isNumber(_grades)) {
                return null;
            }
            final int intGrades = (int) Double.parseDouble(_grades);
            final String val = String.valueOf(intGrades);
            return val;
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
        final TreeMap<String, String> _credits = new TreeMap<String, String>();
        final TreeMap<String, String> _rishutyuCredits = new TreeMap<String, String>();
        final String _comment;
        String _item;
        String _subclassname;

        Acc(final String comment, final String item) {
            _comment = comment;
            _item = item;
        }

        public Acc copy() {
            final Acc rtn = new Acc(_comment, _item);
            rtn._credits.putAll(_credits);
            rtn._rishutyuCredits.putAll(_rishutyuCredits);
            rtn._subclassname = _subclassname;
            return rtn;
        }

        public Acc add(final String year, final String studyFlag, final String credits) {
            return add(year, studyFlag, credits, false);
        }

        public Acc add(final String year, final String studyFlag, final String credits, final boolean addTotal) {
            Acc rtn = copy();
            if (FLAG_CHAIR_SUBCLASS.equals(studyFlag)) {
                rtn._rishutyuCredits.put(year, Util.addNumber(def(rtn._rishutyuCredits.get(year), "0"), credits));
            } else {
                rtn._credits.put(year, Util.addNumber(def(rtn._credits.get(year), "0"), credits));
            }
            if (addTotal) {
                final String tYear = StudyrecSql.TOTAL_YEAR;
                if (FLAG_CHAIR_SUBCLASS.equals(studyFlag)) {
                    rtn._rishutyuCredits.put(tYear, Util.addNumber(def(rtn._rishutyuCredits.get(tYear), "0"), credits));
                } else {
                    rtn._credits.put(tYear, Util.addNumber(def(rtn._credits.get(tYear), "0"), credits));
                }
            }
            return rtn;
        }

        public Acc add(final Acc acc) {
            Acc rtn = copy();
            for (final String year : acc.years()) {
                if (acc.hasCredits(year)) {
                    rtn = rtn.add(year, FLAG_STUDYREC, acc._credits.get(year));
                }
                if (acc.hasRishutyuCredits(year)) {
                    rtn = rtn.add(year, FLAG_CHAIR_SUBCLASS, acc._rishutyuCredits.get(year));
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
                if (acc.hasRishutyuCredits(year)) {
                    rtn._rishutyuCredits.put(year, Util.subtractNumber("0", acc._rishutyuCredits.get(year)));
                }
            }
            return rtn;
        }

        public Set<String> years() {
            final Set<String> rtn = new TreeSet<String>();
            rtn.addAll(_credits.keySet());
            rtn.addAll(_rishutyuCredits.keySet());
            return rtn;
        }

        public boolean hasCredits(final String year) {
            return _credits.containsKey(year);
        }

        public boolean hasRishutyuCredits(final String year) {
            return _rishutyuCredits.containsKey(year);
        }

        public boolean hasAnyCredits() {
            return !_credits.isEmpty();
        }

        public boolean hasRishutyuCredits() {
            return !_rishutyuCredits.isEmpty();
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

        public String sumRishutyuCredits() {
            return sum(_rishutyuCredits);
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

        public Title(final String year, final String annual, final Integer position, final String nendo,
                final String dropFlg) {
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

        private final int _width; //フィールドの幅(ドット)
        private final int _height; //フィールドの高さ(ドット)
        private final int _ystart; //開始位置(ドット)
        private final int _minnum; //最小設定文字数
        private final int _maxnum; //最大設定文字数

        public KNJSvfFieldModify(final int width, final int height, final int ystart, final int minnum,
                final int maxnum) {
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
            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, num)); //文字サイズ
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
            float jiku = retFieldY(_height, charSize) + _ystart + recordHeight * hnum; //出力位置＋Ｙ軸の移動幅
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
            return "KNJSvfFieldModify: width = " + _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    protected static class Student {
        final String _schregno;

        String _nendo;
        KNJSchoolMst _knjSchoolMst;
        public TreeMap<String, Title> _yearTitleMap = new TreeMap<String, Title>(); // 学年（年度）出力列
        private String _certifNumber;
        private String _syoshoname;
        private String _syoshoname2;
        private boolean _isOutputCertifNo;
        String _entDate;
        String _curriculumYear;
        String _entYearGradeCd;
        String _regdSchoolKind;
        String _regdGradeCd;
        public String _majorYdatSchooldiv;
        public Map<String, String> _personalInfoMap = new HashMap<String, String>();
        String _dateStr;
        public Map<String, String> _schoolInfoMap = new HashMap<String, String>();
        public List<Studyrec> _studyrecList = new ArrayList<Studyrec>();
        public List<Map<String, String>> _attendList = new ArrayList<Map<String, String>>();
        private List<String> _offdaysYears = Collections.emptyList();
        String _d015Namespare1;

        Acc _accGoukei = new Acc("total", "合　　　計"); //合計の単位数&存在フラグ
        Acc _accShokei = new Acc("subtotal", "小　　　計");
        Acc _accAbroad = new Acc(StudyrecSql.abroad, "留　　　学");
        Acc _accSogo;

        List<Studyrec> studyrecLastLineClassList;
        List<Studyrec> _inList;

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public void load(final DB2UDB db2, final Param param) {
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度";
            _offdaysYears = getOffdaysYears(db2, param);

//            setNotUseClassMstSpecialDiv(db2, param);
            setSchoolKind(db2, param);

            _accSogo = new Acc(StudyrecSql.sogo, getSogoSubclassname(param));

            final Map knjSchoolMstParamMap = new HashMap();
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                knjSchoolMstParamMap.put("SCHOOL_KIND", _regdSchoolKind);
            }
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, param._year, knjSchoolMstParamMap);
            } catch (final Exception e) {
                log.error("exception!!", e);
            }
            setGradeTitle(db2, param);
            setPersonalInfo(db2, param);
            setSchoolInfo(db2, param);
            setAttend(db2, param);
            setStudyrecList(db2, param);
            _d015Namespare1 = getD015Namespare1(db2, param._year, param);

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
                    personalInfoSqlParamMap.put("SCHOOL_MST_SCHOOL_KIND", SCHOOL_KIND);
                }
                param.sqlPersonalinfo = new KNJ_PersonalinfoSql().sql_info_reg(personalInfoSqlFlg.toString(), personalInfoSqlParamMap);
                param.setPs(db2, psKey, param.sqlPersonalinfo);
            }
            _personalInfoMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno, param._year, param._semester, _schregno, param._year }));

            if (param._isOutputDebug) {
                log.info(Util.debugMapToStr(" personalinfo = ", _personalInfoMap));
            }
        }

        private void setSchoolInfo(final DB2UDB db2, final Param param) {

            _dateStr = defstr(Util.getDateStr(db2, param, param._date), "　　年　 月　 日");

            final String psKey = "PS_SCHOOLINFO";
            final String year2 = param._date != null ? servletpack.KNJG.KNJG010_1.b_year(param._date) : param._year;

            if (null == param.getPs(psKey)) {
                final Map<String, String> schoolinfoParamMap = new HashMap();
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    schoolinfoParamMap.put("schoolMstSchoolKind", SCHOOL_KIND);
                }
                final String sql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12000").pre_sql(schoolinfoParamMap);
                param.setPs(db2, psKey, sql);
            }

            final Object[] qparam = new Object[] { year2, CERTIF_KINDCD, CERTIF_KINDCD, param._year, null };
            _schoolInfoMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), qparam));

            if (param._isOutputDebugQuery) {
                log.info(" schoolinfo parameter = " + ArrayUtils.toString(qparam));
                log.info(Util.debugMapToStr("schoolinfo", _schoolInfoMap));
            }
        }

        private void setAttend(final DB2UDB db2, final Param param) {
            final String psKey = "PS_ATTEND";
            if (null == param.getPs(psKey)) {
                //  出欠記録データ
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR ");
//                stb.append("   , VALUE(CLASSDAYS,0) AS CLASSDAYS "); //授業日数
//                stb.append("   , CASE WHEN S1.SEM_OFFDAYS = '1' ");
//                stb.append("         THEN VALUE(CLASSDAYS,0) ");
//                stb.append("         ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
//                stb.append("     END AS ATTEND_1 "); //授業日数-休学日数:1
//                stb.append("   , VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR "); //出停・忌引
//                stb.append("   , VALUE(SUSPEND,0) AS SUSPEND "); //出停:2
//                stb.append("   , VALUE(MOURNING,0) AS MOURNING "); //忌引:3
//                stb.append("   , VALUE(ABROAD,0) AS ABROAD "); //留学:4
                stb.append("   , CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append("         THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append("         ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append("     END AS MLESSON "); //要出席日数:5
                stb.append("   , CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append("         THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append("         ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append("     END AS SICK "); //病欠＋事故欠（届・無）:6
//                stb.append("   , VALUE(PRESENT,0) AS PRESENT "); //出席日数:7
//                stb.append("   , VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 "); //忌引＋出停:8
                stb.append(" FROM ");
                stb.append(" (");
                stb.append("    SELECT ");
                stb.append("    SCHREGNO,");
                stb.append("    YEAR,");
                stb.append("    SUM(CLASSDAYS) AS CLASSDAYS,");
                stb.append("    SUM(OFFDAYS) AS OFFDAYS,");
                stb.append("    SUM(ABSENT) AS ABSENT,");
                stb.append("    SUM(SUSPEND) AS SUSPEND,");
                stb.append("    SUM(MOURNING) AS MOURNING,");
                stb.append("    SUM(ABROAD) AS ABROAD,");
                stb.append("    SUM(REQUIREPRESENT) AS REQUIREPRESENT,");
                stb.append("    SUM(SICK) AS SICK,");
                stb.append("    SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,");
                stb.append("    SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,");
                stb.append("    SUM(PRESENT) AS PRESENT ");
                stb.append("     FROM ");
                stb.append("         SCHREG_ATTENDREC_DAT ");
                stb.append("     WHERE ");
                stb.append("        SCHREGNO = ? ");
                if (param._isPrintSotsugyosei) {
                    stb.append("        AND YEAR <= ? ");
                } else {
                    stb.append("        AND YEAR < ? "); // 過年度分
                }
                stb.append("      GROUP BY ");
                stb.append("        SCHREGNO,");
                stb.append("        YEAR ");
                stb.append("  )T1 ");
                stb.append("  LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    stb.append("    AND S1.SCHOOL_KIND = 'H' ");
                }
                stb.append(" WHERE T1.YEAR NOT IN (SELECT T1.YEAR FROM SCHREG_REGD_DAT T1 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND <> 'H' WHERE T1.SCHREGNO = ?) ");
                stb.append("ORDER BY ");
                stb.append("    T1.YEAR");
                param.sqlAttend = stb.toString();
                param.setPs(db2, psKey, param.sqlAttend);
            }
            _attendList = new ArrayList<Map<String, String>>();
            _attendList.addAll(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno, param._year, _schregno }));


            if (!param._isPrintSotsugyosei) {
                final String psKeyAcc = "PS_ATTEND_ACCUMULATE";
                if (null == param.getPs(psKeyAcc)) {
                    param._attendParamMap.put("schregno", "?");
                    //  出欠記録データ
                    final String sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            param._attendStartDate,
                            param._attendEndDate,
                            param._attendParamMap
                            );
                    log.debug(" sql = " + sql);

                    param.sqlAttendAcc = sql;
                    param.setPs(db2, psKeyAcc, param.sqlAttendAcc);
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKeyAcc), new Object[] { _schregno })) {
                    if (SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        _attendList.add(row);
                        row.put("YEAR", param._year);
                    }
                }
            }
        }

        private void setStudyrecList(final DB2UDB db2, final Param param) {

            // 学習記録データ
            final StudyrecSql k = new StudyrecSql(param);

            final List<Map<String, String>> studyrecRowList = new ArrayList<Map<String, String>>();
            _inList = new ArrayList<Studyrec>();

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

            if ("1".equals(param._notPrintRishutyu)) {
                // 6）履修中は印刷しない
            } else {
                final String chairSql = k.getChairSql(this, 1);
                if (param._isOutputDebugQuery) {
                    log.info(" chair sql = " + chairSql);
                }
                studyrecRowList.addAll(KnjDbUtils.query(db2, chairSql));

                if ("1".equals(param._printRi)) {
                    // 7）履修中は「履」を印刷する。(履修単位は非表示)
                } else {
                    final String chairInListSql = k.getChairSql(this, 2);
                    if (param._isOutputDebugQuery) {
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
//                if (null != param._lastLineClasscd && param._lastLineClasscd.equals(studyrec._classcd)) {
//                    studyrecLastLineClassList.add(studyrec);
//                    if (param._isOutputDebug) {
//                        log.info(" lastLineClass = " + studyrec);
//                    }
//                    its.remove();
//                    continue;
//                }

                final boolean isInList = Arrays.asList(StudyrecSql.shokei, StudyrecSql.sogo).contains(studyrec._classname);

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
                            log.info("留年時の成績は表示しない:year = " + year);
                        }
                        continue;
                    }
                } else {
                    year = "";
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
                if ((s._classcd == null || s._classcd.equals(classcd))
                        && (s._subclasscd == null || s._subclasscd.equals(subclasscd))) {
                    studyrec = s;
                    break;
                }
            }
            return studyrec;
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
            stb.append(" WITH ");
            stb.append(" DROP_REGD AS ( ");
            stb.append("   SELECT DISTINCT T1.SCHREGNO, T1.ANNUAL, T1.YEAR ");
            stb.append("     FROM  SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN (SELECT SCHREGNO, ANNUAL, MAX(YEAR) AS YEAR ");
            stb.append("              FROM SCHREG_REGD_DAT ");
            stb.append("              WHERE  SCHREGNO = '" + _schregno + "' ");
            stb.append("                AND  YEAR <= '" + param._year + "' ");
            stb.append("              GROUP BY SCHREGNO, ANNUAL) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("   WHERE  T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND  T1.YEAR <= '" + param._year + "' ");
            stb.append("     AND  T2.SCHREGNO IS NULL ");
            stb.append(" ), ");
            stb.append(" PRINT_REGD AS ( ");
            stb.append(" SELECT  REGD.ANNUAL, REGD.YEAR, CASE WHEN T2.SCHREGNO IS NOT NULL THEN '1' END AS DROP_FLG ");
            stb.append("   FROM  SCHREG_REGD_DAT REGD ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("       AND GDAT.GRADE = REGD.GRADE ");
            stb.append("       AND GDAT.SCHOOL_KIND = 'H' ");
            stb.append("   LEFT JOIN DROP_REGD T2 ON T2.SCHREGNO = REGD.SCHREGNO ");
            stb.append("       AND T2.YEAR = REGD.YEAR ");
            stb.append("       AND T2.ANNUAL = REGD.ANNUAL ");
            stb.append(" WHERE  REGD.SCHREGNO = '" + _schregno + "' ");
            stb.append("   AND  REGD.YEAR <= '" + param._year + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ANNUAL, YEAR, CAST(NULL AS VARCHAR(1)) AS DROP_FLG ");
            stb.append("   FROM  SCHREG_STUDYREC_DAT ");
            stb.append(" WHERE  SCHREGNO = '" + _schregno + "' ");
            stb.append("   AND  YEAR <= '" + param._year + "' ");
            stb.append("   AND SCHOOL_KIND = 'H' ");
            stb.append(" ) ");
            stb.append(" SELECT  ANNUAL, YEAR ");
            stb.append(" , MAX(DROP_FLG) AS DROP_FLG ");
            stb.append(" FROM  PRINT_REGD ");
            stb.append(" GROUP BY ANNUAL, YEAR ");
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
                } else {
                    nendo = "";
                    dropFlg = KnjDbUtils.getString(row, "DROP_FLG");
                }
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final Integer position;
                if (null != dropFlg) {
                    position = new Integer(-1);
                } else {
                    final String gradeCd = param.getGradeCdOfGrade(year, annual);
                    if (NumberUtils.isDigits(gradeCd)) {
                        i = Integer.parseInt(gradeCd);
                    } else if (NumberUtils.isDigits(annual)) {
                        i = Integer.parseInt(annual);
                    } else {
                        i = -1;
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

//        /*
//         * 教科マスタの専門区分を使用の設定
//         * ・生徒の入学日付の年度が、証明書学校データのREMARK7の値（年度）以前の場合
//         *  1) 成績欄データのソートに教科マスタの専門区分を使用しない。
//         *  2) 成績欄に教科マスタの専門区分によるタイトルを表示しない。（名称マスタ「E015」設定に優先する。）
//         *   ※証明書学校データのREMARK7の値（年度）が null の場合
//         *    1) 専門区分をソートに使用する。
//         *    2) タイトルの表示/非表示は名称マスタ「E015」の設定による。
//         */
//        private void setNotUseClassMstSpecialDiv(final DB2UDB db2, final Param param) {
//
//            String curriculumYear = null;
//            String entYearGradeCd = null;
//
//            final StringBuffer sql = new StringBuffer();
//            sql.append(" WITH T_SCHOOL_KIND AS ( ");
//            sql.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
//            sql.append("     FROM SCHREG_REGD_DAT T1 ");
//            sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
//            sql.append("         AND T2.GRADE = T1.GRADE ");
//            sql.append("     WHERE ");
//            sql.append("         T1.SCHREGNO = '" + _schregno + "' ");
//            sql.append("         AND T2.YEAR = '" + param._year + "' ");
//            sql.append(" ), MAIN AS ( ");
//            sql.append(" SELECT ");
//            sql.append("     T1.SCHREGNO, ");
//            sql.append("     FISCALYEAR(T1.ENT_DATE) AS ENT_YEAR, ");
//            sql.append("     T1.CURRICULUM_YEAR, ");
//            sql.append("     T4.REMARK7, ");
//            sql.append("     CASE WHEN FISCALYEAR(T1.ENT_DATE) <= T4.REMARK7 THEN 1 ELSE 0 END AS NOT_USE_CLASS_MST_SPECIALDIV ");
//            sql.append(" FROM ");
//            sql.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
//            sql.append("     INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
//            sql.append("     INNER JOIN CERTIF_SCHOOL_DAT T4 ON T4.YEAR = T2.YEAR AND T4.CERTIF_KINDCD = '" + CERTIF_KINDCD + "' ");
//            sql.append(" ) SELECT T1.* ");
//            sql.append("        , T2.GRADE_CD AS ENT_YEAR_GRADE_CD  ");
//            sql.append("   FROM MAIN T1 ");
//            sql.append("   LEFt JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.ENT_YEAR ");
//            sql.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = L1.YEAR AND T2.GRADE = L1.GRADE ");
//
//            for (final Map<String, String> rs : KnjDbUtils.query(db2, sql.toString())) {
//                curriculumYear = KnjDbUtils.getString(rs, "CURRICULUM_YEAR");
//                entYearGradeCd = KnjDbUtils.getString(rs, "ENT_YEAR_GRADE_CD");
//            }
//            _curriculumYear = curriculumYear;
//            _entYearGradeCd = entYearGradeCd;
//            if (param._isOutputDebug) {
//                log.info(" sql = " + sql.toString());
//                log.info(" curriculumYear = " + _curriculumYear);
//                log.info(" entYearGradeCd = " + _entYearGradeCd);
//            }
//        }

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
            sql += " WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + _schregno + "' ";
            if (param._hasSTUDYREC_PROV_FLG_DAT) {
                sql += " AND VALUE(T2.PROV_FLG, '') <> '1' ";
            }
            return KnjDbUtils.query(db2, sql).isEmpty();
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        private boolean isTengakuTaigaku(final DB2UDB db2, final Param param) {
            boolean isTengakuTaigaku = false;
            final KNJ_TransferRecSql obj = new KNJ_TransferRecSql();
            for (final Map row : KnjDbUtils.query(db2, obj.sql_state(), new Object[] { _schregno, _schregno, _schregno, _schregno, _schregno, param._year })) {
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
            sql += " WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + _schregno + "' ";

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _regdSchoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            _regdGradeCd = KnjDbUtils.getString(row, "GRADE_CD");
            if (param._isOutputDebug) {
                log.info(" regdSchoolKind = " + _regdSchoolKind + ", regdGradeCd = " + _regdGradeCd);
            }
        }

        public String getSogoSubclassname(final Param param) {
            final int tankyuStartYear = Util.toInt(param.property("sogoTankyuStartYear"), 2019);
            boolean isTankyu = false;
            if (NumberUtils.isDigits(_curriculumYear)) {
                if (tankyuStartYear <= Integer.parseInt(_curriculumYear)) {
                    isTankyu = true;
                }
                if (param._isOutputDebug) {
                    log.info(" 探究? " + isTankyu + ", curriculumYear = " + _curriculumYear);
                }
            } else {
                final int year = Util.toInt(param._year, 0);
                final int gradeCdInt = NumberUtils.isDigits(_regdGradeCd) ? Integer.parseInt(_regdGradeCd) : 0;
                if (year == tankyuStartYear && gradeCdInt <= 1
                        || year == tankyuStartYear + 1 && gradeCdInt <= 2
                        || year == tankyuStartYear + 2 && gradeCdInt <= 3
                        || year >= tankyuStartYear + 3) {
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
                    if (null == credit && Acc.FLAG_CHAIR_SUBCLASS.equals(g._studyFlag) && null != g._gradeCredit) {
                        credit = g._gradeCredit;
                    }
                    if (credit != null) {
                        boolean noTotal = false;
                        if (StudyrecSql.shokei.equals(studyrec._classname)) {
                            _accShokei = _accShokei.add(g._year, g._studyFlag, credit);
                        }
                        if (StudyrecSql.sogo.equals(studyrec._classname)) {
                            _accSogo = _accSogo.add(g._year, g._studyFlag, credit);
                            _accShokei = _accShokei.add(g._year, g._studyFlag, credit);
                        }
                        if (StudyrecSql.abroad.equals(studyrec._classname)) {
                            _accAbroad = _accAbroad.add(g._year, g._studyFlag, credit);
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
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey),
                    new Object[] { _schregno })) {
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

        public static String CONFIG_PRINT_GRD = "PRINT_GRD";
        private static final String TOTAL_YEAR = "99999999";

        private final Param _param;
        private Student _printData;

        public StudyrecSql(final Param param) {
            _param = param;
        }

        public int compare(final Map<String, String> o1, final Map<String, String> o2) {
            return rowCompare(o1, o2);
        }

        public int rowCompare(final Map<String, String> row1, final Map<String, String> row2) {
            int rtn;
            rtn = compareString("D065FLG", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("SPECIALDIV", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareInt("CLASS_ORDER", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("CLASSCD", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareInt("SUBCLASS_ORDER", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("CLASSCD", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("SUBCLASSCD", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("YEAR", row1, row2);
            if (rtn != 0) {
                return rtn;
            }
            rtn = compareString("ANNUAL", row1, row2);
            return rtn;
        }

        private static int compareString(final String field, final Map<String, String> row1, final Map<String, String> row2) {
            final String v1 = KnjDbUtils.getString(row1, field);
            final String v2 = KnjDbUtils.getString(row2, field);
            if (null != v1 || null != v2) {
                if (null == v1) {
                    return -1;
                }
                if (null == v2) {
                    return 1;
                }
                return v1.compareTo(v2);
            }
            return 0;
        }

        private static int compareInt(final String field, final Map<String, String> row1, final Map<String, String> row2) {
            final String v1 = KnjDbUtils.getString(row1, field);
            final String v2 = KnjDbUtils.getString(row2, field);
            if (null != v1 || null != v2) {
                if (null == v1) {
                    return -1;
                }
                if (null == v2) {
                    return 1;
                }
                return Integer.valueOf(v1).compareTo(Integer.valueOf(v2));
            }
            return 0;
        }

        /**
         * 学習記録のSQL
         */
        public String getStudyrecSql(final Student student, final int flg) {
            _printData = student;

            String notContainTotalYears = getNotContainTotalYears(student);

            final List<String> ryunenYearList = new ArrayList<String>();
            {
                for (final Title title : student._yearTitleMap.values()) {
                    if (null != title && null != title._year && null != title._dropFlg) {
                        if (_param._isOutputDebug) {
                            log.info("留年時の成績は表示しない:year = " + title._year);
                        }
                        ryunenYearList.add(title._year);
                    }
                }
            }

//            final boolean notUseStudyrecProvFlgDat = !_param._hasSTUDYREC_PROV_FLG_DAT;

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

//            String lastLineClasscd = _param._lastLineClasscd; // getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
//            boolean isSubclassContainLastLineClass = false; // 表示する行を取得
//            boolean isHyoteiHeikinLastLineClass = false; // lastLineClasscd教科に評定を入力し評定平均を表示する
//            boolean isTotalContainLastLineClass = false; // 'total'にlastLineClasscd教科を含める
//            if (null != lastLineClasscd) {
//                // 面倒なのでtrue
//                isSubclassContainLastLineClass = true;
//                isHyoteiHeikinLastLineClass = true;
//                isTotalContainLastLineClass = true;
//            }

            final String year = _param._year; // String.valueOf(Integer.parseInt(_param._ctrlYear) - 1); // 過年度
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
            // 賢者
            sql.append("WITH T_STUDYREC AS(");
            sql.append("SELECT  T1.SCHOOLCD, ");
            sql.append("        T1.CLASSNAME, ");
            sql.append("        T1.SUBCLASSNAME, ");
            sql.append("        T1.SCHREGNO, ");
            sql.append("        T1.YEAR, ");
            sql.append("        T1.ANNUAL, ");
            sql.append("        T1.SCHOOL_KIND, ");
            sql.append("        T1.CURRICULUM_CD, ");
            sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
            sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
            sql.append("        L2.SUBCLASSCD2 AS RAW_SUBCLASSCD2, ");
            sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            sql.append("        L2.SUBCLASSCD2 AS SUBCLASSCD2 ");
            sql.append("       ,T1.VALUATION AS GRADES ");
            sql.append("       ,T1.CLASSCD ");
            sql.append(
                    "       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
            sql.append("       ,T1.COMP_CREDIT ");
            sql.append("       ,NMD065.NAME1 AS D065FLG ");
            sql.append("FROM   SCHREG_STUDYREC_DAT T1 ");
            sql.append("        LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
            sql.append("        AND L2.CLASSCD = T1.CLASSCD ");
            sql.append("        AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
//            if (notUseStudyrecProvFlgDat) {
//            } else {
//                sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
//                sql.append("            AND L3.YEAR = T1.YEAR ");
//                sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
//                sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
//                sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
//                sql.append("            AND L3.PROV_FLG = '1' ");
//            }
            sql.append("        LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
            sql.append("WHERE   T1.SCHREGNO = '" + student._schregno + "' AND T1.YEAR <= '" + year + "' ");
            sql.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "' ");
//            if (null != lastLineClasscd) {
//                sql.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
//            }
            sql.append("        ) ");
            if (ryunenYearSqlNotIn.length() > 0) {
                sql.append("    AND T1.YEAR " + ryunenYearSqlNotIn);
            }
//            if (notUseStudyrecProvFlgDat) {
//            } else {
//                sql.append("         AND L3.SUBCLASSCD IS NULL ");
//            }
            if ("1".equals(_param._seisekishoumeishoNotPrintAnotherStudyrec)) {
                sql.append("         AND T1.SCHOOLCD <> '1' ");
            }
            if (null != student._regdSchoolKind) {
                // 指定校種以外の学年を対象外とする
                sql.append(" AND T1.ANNUAL NOT IN (SELECT DISTINCT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND <> '" + student._regdSchoolKind + "') ");
            }
//            if ("1".equals(_param.property("hyoteiYomikaeRadio")) && "notPrint1".equals(printData.parameter("HYOTEI"))) {
//                sql.append("         AND (T1.VALUATION IS NULL OR T1.VALUATION <> 1) ");
//            }
            sql.append(") , T_STUDYREC2 AS( ");
            sql.append("    SELECT ");
            sql.append("        T1.* ");
            sql.append("    FROM ");
            sql.append("        T_STUDYREC T1 ");

            sql.append(") , STUDYREC0 AS( ");
            sql.append("    SELECT ");
            sql.append("        T1.SCHOOLCD, ");
            sql.append("        T1.CLASSNAME, ");
            sql.append("        T1.SUBCLASSNAME, ");
            sql.append("        T1.SCHREGNO, ");
            sql.append("        T1.YEAR, ");
            sql.append("        T1.ANNUAL, ");
            sql.append("        T1.CLASSCD , ");
            sql.append("        T1.SCHOOL_KIND, ");
            sql.append("        T1.CURRICULUM_CD, ");
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
            sql.append("        T1.SCHOOL_KIND, ");
            sql.append("        T1.CURRICULUM_CD, ");
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

            final int _hyoteiKeisanMinGrades = "Y".equals(student._d015Namespare1) ? 0 : 1;
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
            sql.append("        T1.SCHOOL_KIND, ");
            sql.append("        T1.CURRICULUM_CD, ");
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
            sql.append("             when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT(" + gradesCase + ")),0)");
            sql.append("             when SC.GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * T1.CREDIT)) / SUM(" + creditCase + "),0)");
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
            sql.append(
                    "        LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
            sql.append("    GROUP BY ");
            sql.append("        T1.SCHREGNO, ");
            sql.append("        T1.YEAR, ");
            sql.append("        T1.CLASSCD, ");
            sql.append("        T1.SCHOOL_KIND, ");
            sql.append("        T1.CURRICULUM_CD, ");
            sql.append("        T1.RAW_SUBCLASSCD, ");
            sql.append("        T1.SUBCLASSCD, ");
            sql.append("       SC.GVAL_CALC ");
            sql.append(") ");

            sql.append(" , DROP_YEAR AS(");
            sql.append("        SELECT DISTINCT T1.YEAR ");
            sql.append("        FROM SCHREG_REGD_DAT T1");
            sql.append("        WHERE T1.SCHREGNO = '" + student._schregno + "' ");
            sql.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
            sql.append("                            WHERE T2.SCHREGNO = '" + student._schregno + "' ");
            sql.append("                              AND T2.YEAR <= '" + year + "' ");
            sql.append("                            GROUP BY T2.GRADE)");
            sql.append(" ) ");

//            final boolean useCreditMst = false; // "1".equals(_param.property("hyoteiYomikaeRadio")) && "1".equals(printData.parameter("HYOTEI"));
//            if (useCreditMst) {
//                sql.append(" , CREM_REGD AS (");
//                sql.append("        SELECT T1.SCHREGNO, T1.YEAR, MAX(T1.SEMESTER) AS SEMESTER ");
//                sql.append("        FROM SCHREG_REGD_DAT T1");
//                sql.append("        WHERE T1.SCHREGNO = '" + printData._schregno + "' ");
//                sql.append("        GROUP BY T1.SCHREGNO, T1.YEAR ");
//                sql.append(" ) ");
//                sql.append(" , CREM0 AS (");
//                sql.append(
//                        "        SELECT T2.SCHREGNO, T2.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CREDITS ");
//                sql.append("        FROM CREDIT_MST T1");
//                sql.append("        INNER JOIN CREM_REGD T2 ON T2.YEAR = T1.YEAR ");
//                sql.append("        INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T2.SCHREGNO ");
//                sql.append("            AND REGD.YEAR = T2.YEAR ");
//                sql.append("            AND REGD.SEMESTER = T2.SEMESTER ");
//                sql.append("            AND REGD.COURSECD = T1.COURSECD ");
//                sql.append("            AND REGD.GRADE = T1.GRADE ");
//                sql.append("            AND REGD.MAJORCD = T1.MAJORCD ");
//                sql.append("            AND REGD.COURSECODE = T1.COURSECODE ");
//                sql.append(" ) ");
//                sql.append(" , CREM AS (");
//                sql.append(
//                        "        SELECT T1.SCHREGNO, T1.YEAR, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CREDITS ");
//                sql.append("        FROM CREM0 T1");
//                sql.append("        UNION ALL ");
//                sql.append(
//                        "        SELECT T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD AS CLASSCD, T2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS ");
//                sql.append("        FROM CREM0 T1");
//                sql.append("        INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ");
//                sql.append("            AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
//                sql.append("            AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
//                sql.append("            AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
//                sql.append("            AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
//                sql.append("        WHERE ");
//                sql.append("                   (T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD) ");
//                sql.append("     NOT IN (SELECT T1.SCHREGNO, T1.YEAR, T1.CLASSCD,          T1.SCHOOL_KIND,          T1.CURRICULUM_CD,          T1.SUBCLASSCD         FROM CREM0) ");
//                sql.append("        GROUP BY ");
//                sql.append("               T1.SCHREGNO, T1.YEAR, T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
//                sql.append(" ) ");
//            }
            if (flg == 1) {

                final String groupByColumn = " ANNUAL ";
                sql.append(", MAIN AS ( ");
                //該当生徒の科目評定、修得単位及び教科評定平均
                sql.append(" SELECT ");
                sql.append("     T2.SHOWORDER2 as CLASS_ORDER,");
                sql.append("     T3.SHOWORDER2 as SUBCLASS_ORDER,");
                sql.append("     T1.YEAR,");
                sql.append("     T1." + groupByColumn + " AS ANNUAL,");
                sql.append("        T1.SCHOOL_KIND, ");
                sql.append("        T1.CURRICULUM_CD, ");
                sql.append("     T1.CLASSCD,");
                sql.append(" VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME,");
                sql.append("     T1.SUBCLASSCD,");
                sql.append("     T1.RAW_SUBCLASSCD, ");
                sql.append(" VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME,");
//                if (useCreditMst) {
//                    sql.append("       CASE WHEN T1.GRADES = 1 THEN 2 ELSE T1.GRADES END AS GRADES, ");
//                    sql.append(
//                            "       CASE WHEN T1.GRADES = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS GRADE_CREDIT, ");
//                } else {
                    sql.append(h_1_2 + " AS GRADES,");
                    sql.append("     T1.CREDIT AS GRADE_CREDIT,");
//                }
                sql.append("     T1.COMP_CREDIT AS GRADE_COMP_CREDIT,");
                sql.append("     SUBCLSGRP.CREDIT, ");
                sql.append("     T1.D065FLG ");
                sql.append(" FROM ");
                sql.append("     STUDYREC T1 ");
                sql.append("     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                sql.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append("     LEFT JOIN SUBCLASS_MST T3 ON ");
                sql.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T1.SUBCLASSCD ");
                //  修得単位数の計
                sql.append("     LEFT JOIN(SELECT ");
                sql.append("             CLASSCD, SUBCLASSCD, SUM(" + h_1_3 + ") AS CREDIT ");
                sql.append("         FROM ");
                sql.append("             STUDYREC T1 ");
                sql.append("         WHERE ");
                sql.append("             (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
//                if (null != lastLineClasscd && isSubclassContainLastLineClass) {
//                    sql.append("           OR T1.CLASSCD = '" + lastLineClasscd + "'");
//                }
                sql.append("             )");
                sql.append("             AND YEAR NOT IN " + notContainTotalYears);
                sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                sql.append("         GROUP BY ");
                sql.append("             CLASSCD, SUBCLASSCD ");
                sql.append("     ) SUBCLSGRP ON SUBCLSGRP.SUBCLASSCD = T1.SUBCLASSCD ");
//                if (useCreditMst) {
//                    sql.append("     LEFT JOIN CREM CREM ON CREM.YEAR = T1.YEAR ");
//                    sql.append("         AND CREM.SCHREGNO = T1.SCHREGNO ");
//                    sql.append("         AND CREM.CLASSCD = T1.CLASSCD ");
//                    sql.append("         AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                    sql.append("         AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                    sql.append("         AND CREM.CLASSCD || '-' || CREM.SCHOOL_KIND || '-' || CREM.CURRICULUM_CD || '-' || CREM.SUBCLASSCD = T1.SUBCLASSCD ");
//                }
                sql.append(" WHERE ");
                sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
//                if (null != lastLineClasscd && isHyoteiHeikinLastLineClass) {
//                    sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
//                }
                sql.append("      )");
                sql.append(" ) ");
                sql.append(" SELECT");
                sql.append("   '" + Acc.FLAG_STUDYREC + "' AS STUDY_FLAG ");
                sql.append("  ,T1.CLASS_ORDER "); // 表示順教科
                sql.append("  ,T1.SUBCLASS_ORDER "); // 表示順科目
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
                sql.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");

            } else if (flg == 2) {
                sql.append(", MAIN0 AS ( ");
                sql.append(" SELECT ");
                sql.append("        T1.CLASSNAME ");
                sql.append("      , T1.SUBCLASSNAME ");
                sql.append("      , T1.SCHREGNO ");
                sql.append("      , T1.YEAR ");
                sql.append("      , T1.ANNUAL ");
                sql.append("      , T1.CLASSCD ");
                sql.append("      , T1.SCHOOL_KIND ");
                sql.append("      , T1.CURRICULUM_CD ");
                sql.append("       ,  T1.SCHOOLCD ");
                sql.append("       , T1.RAW_SUBCLASSCD ");
                sql.append("       , T1.SUBCLASSCD2 ");
                sql.append("       , T1.SUBCLASSCD ");
                sql.append("       , T1.COMP_CREDIT ");
                sql.append("       , T1.D065FLG ");
//                if (useCreditMst) {
//                    sql.append("       , CASE WHEN T1.GRADES = 1 THEN 2 ELSE T1.GRADES END AS GRADES ");
//                    sql.append(
//                            "       , CASE WHEN T1.GRADES = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS CREDIT ");
//                } else {
                    sql.append("       , T1.GRADES ");
                    sql.append("       , T1.CREDIT ");
//                }
                sql.append(" FROM ");
                sql.append("     T_STUDYREC T1 ");
//                if (useCreditMst) {
//                    sql.append("     LEFT JOIN CREM CREM ON CREM.YEAR = T1.YEAR ");
//                    sql.append("         AND CREM.SCHREGNO = T1.SCHREGNO ");
//                    sql.append("         AND CREM.CLASSCD = T1.CLASSCD ");
//                    sql.append("         AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                    sql.append("         AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                    sql.append(
//                            "         AND CREM.CLASSCD || '-' || CREM.SCHOOL_KIND || '-' || CREM.CURRICULUM_CD || '-' || CREM.SUBCLASSCD = T1.SUBCLASSCD ");
//                }
                sql.append(" WHERE ");
                sql.append("     T1.YEAR NOT IN " + notContainTotalYears);
                sql.append("     AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
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
//                if (null != lastLineClasscd && isTotalContainLastLineClass) {
//                    sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
//                }
                sql.append("      )");
                sql.append(" GROUP BY YEAR");

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

        private String getNotContainTotalYears(final Student student) {
            String notContainTotalYears = "('99999999')";
            return notContainTotalYears;
        }

        public String getAbroadSql(final Student student) {
            String notContainTotalYears = getNotContainTotalYears(student);
            final String year = _param._year; //　student._isPrintChairSubclass ? String.valueOf(Integer.parseInt(_param._ctrlYear) - 1) : _param._ctrlYear;

            final StringBuffer sql = new StringBuffer();
            //  留学中の修得単位数（学年別）

            sql.append(" WITH ");
            sql.append(" DROP_YEAR AS ( ");
            sql.append("        SELECT DISTINCT T1.YEAR ");
            sql.append("        FROM SCHREG_REGD_DAT T1");
            sql.append("        WHERE T1.SCHREGNO = '" + student._schregno + "' ");
            sql.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
            sql.append("                            WHERE T2.SCHREGNO = '" + student._schregno + "' ");
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
            sql.append("             SCHREG_TRANSFER_DAT ");
            sql.append("         WHERE ");
            sql.append("             SCHREGNO = '" + student._schregno + "' AND TRANSFERCD = '1' ");
            sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
            sql.append("           AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
            sql.append(" ) ");
            sql.append(" , MAIN AS ( ");
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
            sql.append("             SCHREG_REGD_DAT ");
            sql.append("         WHERE ");
            sql.append("             SCHREGNO = '" + student._schregno + "' AND YEAR <= '" + year + "' ");
            sql.append("         GROUP BY YEAR ");
            sql.append("         ) ST2 ON ST2.YEAR = ST1.TRANSFER_YEAR ");
            sql.append(" WHERE ");
            sql.append("     ST1.TRANSFER_YEAR <= '" + year + "' ");
            sql.append(" GROUP BY ST1.TRANSFER_YEAR ");
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
        public String getChairSql(final Student student, final int flg) {

//            String lastLineClasscd = _param._lastLineClasscd; // getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
//            boolean isTotalContainLastLineClass = false; // 'total'にlastLineClasscd教科を含める
//            if (null != lastLineClasscd) {
//                // 面倒なのでtrue
//                isTotalContainLastLineClass = true;
//            }

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
            sql.append("         T1.YEAR = '" + _param._year + "' ");
            sql.append("         AND T1.SCHREGNO = '" + student._schregno + "' ");
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
            sql.append("   , T2.CLASSNAME ");
            sql.append("   , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME ");
            sql.append("   , T2.SHOWORDER AS SHOWORDERCLASS"); // 表示順教科
            sql.append("   , T3.SHOWORDER AS SHOWORDERSUBCLASS"); // 表示順科目
            sql.append("   , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            sql.append(" FROM CHAIR_STD_SUBCLASSCD2 T1 ");
            sql.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            sql.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
            sql.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            sql.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            sql.append(" LEFT JOIN SCHREG_STUDYREC_DAT T4 "); // SCHREG_STUDYREC_DATがない科目が対象
            sql.append("      ON T4.SCHOOLCD = '0' ");
            sql.append("     AND T4.YEAR = T1.YEAR ");
            sql.append("     AND T4.SCHREGNO = T1.SCHREGNO ");
            sql.append("     AND T4.CLASSCD = T1.CLASSCD ");
            sql.append("     AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            sql.append("     AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            sql.append(" WHERE ");
            sql.append("     T4.SCHREGNO IS NULL ");
            sql.append(" ) ");
            sql.append(" , CHAIR_STD_SUBCLASS_MAIN AS (");
            if (flg == 1) {
                sql.append(" SELECT ");
                sql.append("     T1.YEAR, T1.SCHREGNO, T1.ANNUAL ");
                sql.append("   , T1.CLASSCD ");
                sql.append("   , T1.SCHOOL_KIND ");
                sql.append("   , T1.CURRICULUM_CD ");
                sql.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  AS SUBCLASSCD ");
                sql.append("   , T1.CREDITS");
                sql.append("   , T2.GET_CREDIT ");
                sql.append("   , T2.SCORE AS GRADES ");
                sql.append("   , T1.CLASSNAME ");
                sql.append("   , T1.SUBCLASSNAME ");
                sql.append("   , T1.SHOWORDERCLASS");
                sql.append("   , T1.SHOWORDERSUBCLASS");
                sql.append("   , T1.SPECIALDIV");
                sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
                sql.append(" LEFT JOIN RECORD_SCORE_DAT T2 ");
                sql.append("      ON T2.YEAR = '" + _param._year + "' ");
                sql.append("     AND T2.SEMESTER = '" + SEMEALL + "' ");
                sql.append("     AND T2.TESTKINDCD = '99' ");
                sql.append("     AND T2.TESTITEMCD = '00' ");
                sql.append("     AND T2.SCORE_DIV = '09' ");
                sql.append("     AND T2.CLASSCD = T1.CLASSCD ");
                sql.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                sql.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                sql.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                sql.append(" WHERE ");
                sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
//                if (null != lastLineClasscd) {
//                    sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
//                }
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
//                if (null != lastLineClasscd && isTotalContainLastLineClass) {
//                    sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
//                }
                sql.append("      )");
                sql.append(" GROUP BY ");
                sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
            }
            sql.append(" ) ");
            sql.append(" SELECT");
            sql.append("   '" + Acc.FLAG_CHAIR_SUBCLASS + "' AS STUDY_FLAG ");
            sql.append("  ,T2.SHOWORDERCLASS AS CLASS_ORDER "); // 表示順教科
            sql.append("  ,T2.SHOWORDERSUBCLASS AS SUBCLASS_ORDER "); // 表示順科目
            sql.append("  ,T2.YEAR");
            sql.append("  ,T2.ANNUAL");
            sql.append("  ,T2.CLASSCD");
            sql.append("  ,T2.CLASSNAME");
            sql.append("  ,T2.SUBCLASSCD AS SUBCLASSCD");
            sql.append("  ,T2.SUBCLASSNAME");
            if (flg == 1) {
                sql.append("  ,T2.GRADES ");
            } else {
                sql.append("  ,CAST(NULL AS SMALLINT) AS GRADES");
            }
            sql.append("  ,T2.CREDITS AS GRADE_CREDIT");
            sql.append("  ,T2.CREDITS AS GRADE_COMP_CREDIT");
            if (flg == 1) {
                sql.append("  ,T2.GET_CREDIT AS GRADE_GET_CREDIT");
            }
            sql.append("  ,CAST(NULL AS SMALLINT) AS CREDIT ");
            sql.append("  ,T2.SPECIALDIV ");
            sql.append("  ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
            sql.append(" FROM ");
            sql.append("    CHAIR_STD_SUBCLASS_MAIN T2 ");
            sql.append(" ORDER BY ");
            sql.append("   CASE WHEN D065FLG IS NOT NULL THEN 999 ELSE 0 END");
            sql.append("  ,SPECIALDIV ");
            sql.append("  ,CLASS_ORDER ");
            sql.append("  ,CLASSCD ");
            sql.append("  ,SUBCLASS_ORDER ");
            sql.append("  ,SUBCLASSCD ");
            sql.append("  ,YEAR ");
            sql.append("  ,ANNUAL");

            return sql.toString();
        }
    }

    private static class Util {

        public static String decode(final String s, final String encoding) {
            if (null != s) {
                try {
                    return new String(s.getBytes(encoding));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return null;
        }

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
        private static String seirekiFormat(final String strx, final String pattern) {
            String hdate = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat();
                Date dat = new Date();
                try {
                    sdf.applyPattern("yyyy-MM-dd");
                    dat = sdf.parse(strx);
                } catch (Exception e) {
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

        public static String addNumber(final String num1, final String num2) {
            if (!NumberUtils.isNumber(num1)) return num2;
            if (!NumberUtils.isNumber(num2)) return num1;
            return String.valueOf(new BigDecimal(num1).add(new BigDecimal(num2)));
        }

        public static String subtractNumber(final String num1, final String num2) {
            if (!NumberUtils.isNumber(num2))
                return num1;
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

        public static String formatBirthday(final DB2UDB db2, final Param param, final Student student, final String date, final boolean isSeireki) {
            String birthdayStr = "";
            if (date != null) { // 生年月日
                if (isSeireki) {
                    birthdayStr = seirekiFormat(date, "yyyy年M月d日");
                } else {
                    birthdayStr = KNJ_EditDate.h_format_JP(db2, date);
                }
            }
            return birthdayStr;
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

    private static class Param {
        final String _regddiv; // 1:在校生 2:卒業生
        final boolean _isPrintSotsugyosei; //
        final String _year; //年度
        final String _semester; //学期
        final String _gradeHrClass; // 年組
        final String _date; //処理日付
        final String _notPrintCertifno; // 1:発行番号は印刷しない
        final String _useCertifnoStart; // 1:印刷する発行番号を指定する
        final String _certifnoStart; // 印刷開始発行番号
        final String _attendStartDate; // 出欠集計範囲開始日
        final String _attendEndDate; // 出欠集計範囲終了日
        final String _notPrintRishutyu; // 履修中は印刷しない
        final String _printRi; // 履修中は「履」を印刷する。
        final String _biko; // 備考欄へ出力する内容
        final String _printSotsu; // 在学生を卒業と出力する。
        final String _printGrdDate; // 卒業日
        final Map<String, String> _majorStartNumberMap = new TreeMap<String, String>(); // 学科別発行番号開始

        final Map<String, String> _paramap = new TreeMap<String, String>();
        final String[] _categorySelected; //学籍番号

        public final boolean _isSeireki; // 西暦フラグをセット。
//        final String _lastLineClasscd;
        final Map _attendParamMap; // 出欠パラメータ

        public boolean _isOutputDebug;
        public boolean _isOutputDebugQuery;
        public boolean _isOutputDebugField;
        public boolean _isOutputDebugPrint;
        public String _documentroot;
        public Properties _prgInfoPropertiesFilePrperties;
        private Map<String, String> _dbPrgInfoProperties;
        public final String _imagepath;

        private String sqlAttend, sqlAttendAcc, sqlRemark, sqlPersonalinfo;
        private Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        private Map<String, File> _createdfiles = new HashMap<String, File>();

        public String _seisekishoumeishoNotPrintAnotherStudyrec;

        protected Map<String, Map<String, String>> _a029NameMstMap;
        protected TreeMap<String, Map<String, String>> _gradeGradeCdMap;
        final boolean _hasMAJOR_MST_MAJORNAME2;
        final boolean _hasSTUDYREC_PROV_FLG_DAT;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasCOURSECODE_MST_COURSECODEABBV1;
        public Param(final DB2UDB db2, final HttpServletRequest request) {

            _regddiv = request.getParameter("REGDDIV");
            _isPrintSotsugyosei = "2".equals(_regddiv);
            if (_isPrintSotsugyosei) {
                final String[] split = StringUtils.split(request.getParameter("GRD_YEAR_SEMESTER"), "-");
                _year = split[0];
                _semester = split[1];
            } else {
                _year = request.getParameter("CTRL_YEAR");
                _semester = request.getParameter("CTRL_SEMESTER");
            }
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");

            if (!StringUtils.isEmpty(request.getParameter("DATE"))) {
                _date = StringUtils.replace(request.getParameter("DATE"), "/", "-");
            } else {
                _date = null;
            }
            _notPrintCertifno = request.getParameter("NOT_PRINT_CERTIFNO");
            _useCertifnoStart = request.getParameter("USE_CERTIFNO_START");
            _certifnoStart =    request.getParameter("CERTIFNO_START");
            _attendStartDate =  StringUtils.replace(request.getParameter("ATTEND_START_DATE"), "/", "-");
            _attendEndDate =    StringUtils.replace(request.getParameter("ATTEND_END_DATE"), "/", "-");
            _notPrintRishutyu = request.getParameter("NOT_PRINT_RISHUTYU");
            _printRi =          request.getParameter("PRINT_RI");
            _biko =             Util.decode(request.getParameter("BIKO"), "ISO8859-1");
            _printSotsu =       request.getParameter("PRINT_SOTSU");
            _printGrdDate =     request.getParameter("PRINT_GRD_DATE");

            for (final Enumeration<String> enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String parameterName = enums.nextElement();
                if (!_paramap.containsKey(parameterName)) {
                    if (Pattern.matches("NUMBER_([0-9]{4})", parameterName)) {
                        final String majorKey = parameterName.substring(7);
                        _majorStartNumberMap.put(majorKey, request.getParameter(parameterName));
                    } else {
                        _paramap.put(parameterName, request.getParameter(parameterName));
                    }
                }
            }
            log.info(" _majorNubmerStartMap = " + _majorStartNumberMap);
            _categorySelected = request.getParameterValues("category_selected");

            _dbPrgInfoProperties = getDbPrginfoProperties(db2);

            _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            _hasSTUDYREC_PROV_FLG_DAT = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasCOURSECODE_MST_COURSECODEABBV1 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV1");

            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _isSeireki = false; // "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _a029NameMstMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'A029' "), "NAMECD2");
            _gradeGradeCdMap = getSchregRegdGdatMap(db2);
            final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");
            _isOutputDebugPrint = ArrayUtils.contains(outputDebug, "print");
            if (_isOutputDebug) {
                log.info(" _isSeireki = " + _isSeireki);
            }
            setDocumentroot((String) _paramap.get("DOCUMENTROOT"));
            _seisekishoumeishoNotPrintAnotherStudyrec = property("seisekishoumeishoNotPrintAnotherStudyrec");

//            _lastLineClasscd = property("seisekishoumeishoCreditOnlyClasscd");
//            if (null != _lastLineClasscd) {
//                log.info(" lastLineClasscd = " + _lastLineClasscd);
//            }

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _gradeHrClass.substring(0, 2));
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

        }

        /**学籍番号の並べ替え**/
        private List<Map<String, String>> getSchregnoMapList(final DB2UDB db2) {
            final List<Map<String, String>> list = new ArrayList();
            String sql;
            sql = "SELECT "
                    + "T1.ATTENDNO,"
                    + "T1.SCHREGNO, "
                    + "T1.COURSECD, "
                    + "T1.MAJORCD, "
                    + "T2.SCHOOL_KIND "
                    + "FROM "
                    + "SCHREG_REGD_DAT T1 "
                    + "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE "
                    + "WHERE "
                    + "T1.YEAR = '" + _year + "' AND "
                    + "T1.SEMESTER = '" + _semester + "' AND "
                    + "T1.SCHREGNO IN (";
            sql += "'" + _categorySelected[0] + "'";
            for (int len = 1; len < _categorySelected.length; len++) {
                sql += ",'" + _categorySelected[len] + "'";
            }
            sql = sql
                    + ") ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO";

            list.addAll(KnjDbUtils.query(db2, sql));
            return list;
        }

        public void close() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            for (final File file : _createdfiles.values()) {
                log.info(" delete file : " + file.getAbsolutePath() + " = " + file.delete());
            }
        }

        public void setDocumentroot(final String documentroot) {
            _documentroot = documentroot;
            _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE080A' "), "NAME", "VALUE");
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
            log.info(" file " + file.getPath() + " exists? = " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return file.getPath();
        }

        private static boolean isNewForm(final Student student) {
            final int checkYear = 2013; // 切替年度
            boolean rtn = false;
            if (null != student) {
                if (NumberUtils.isDigits(student._curriculumYear)) {
                    // 教育課程年度が入力されている場合
                    if (checkYear > Integer.parseInt(student._curriculumYear)) {
                        rtn = false;
                    } else {
                        rtn = true;
                    }
                } else if (null != nendo(student._entDate)) {
                    final int iEntYear = nendo(student._entDate).intValue();
                    if (checkYear > iEntYear) {
                        rtn = false;
                    } else if (checkYear <= iEntYear) {
                        if (NumberUtils.isDigits(student._entYearGradeCd)) {
                            final int iAnnual = Integer.parseInt(student._entYearGradeCd);
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

        public String getGradeCdOfGrade(final String year, final String grade) {
            Map<String, String> gradeCdMap = Util.getMappedHashMap(_gradeGradeCdMap, year);
            if (null == gradeCdMap.get(grade) && !_gradeGradeCdMap.isEmpty()) {
                gradeCdMap = Util.getMappedHashMap(_gradeGradeCdMap, _gradeGradeCdMap.lastKey());
            }
            return gradeCdMap.get(grade);
        }

        private static TreeMap<String, Map<String, String>> getSchregRegdGdatMap(final DB2UDB db2) {
            final TreeMap<String, Map<String, String>> rtn = new TreeMap<String, Map<String, String>>();
            final String sql = " SELECT * FROM SCHREG_REGD_GDAT ";

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                Util.getMappedHashMap(rtn, KnjDbUtils.getString(row, "YEAR")).put(KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "GRADE_CD"));
            }
            return rtn;
        }
    }
}//クラスの括り

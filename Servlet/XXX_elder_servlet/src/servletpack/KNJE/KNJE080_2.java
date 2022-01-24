// kanji=漢字
/*
 * $Id: 20fabe981652405aed94c9764d817979e4e1079c $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;

/*
 *  学校教育システム 賢者 [進路情報管理]  学業成績証明書（英語）
 */

public class KNJE080_2 {

    private static final Log log = LogFactory.getLog(KNJE080_2.class);

    private static String abroad = "abroad";
    private static String total = "total";
    private static String sogo = "sogo";
    private static String sogoGoukeiAnnual = "0";
    private static String totalGoukeiAnnual = "0";
    private static String creditOnly = "creditOnly";

    private static final String CERTIF_KINDCD = "007";
    private static final String CERTIF_KINDCD_MIKOMI = "037";

    private static final String CERTIF_KINDCD_JP = "006";

    private static final String d_MMMM_yyyy = "d MMMM yyyy";
    private static final String MMM_d_comma_yyyy = "MMM d,yyyy";
    private static final String MMM_dot_d_comma_yyyy_miyagiken = "MMM. d,yyyy";
    private static final String MMMM_d_comma_yyyy = "MMMM d, yyyy";
    private static final String KAIJYO_DATE_FORMAT = MMMM_d_comma_yyyy;

    private static final String Period_for_Integrated_Study = "Period for Integrated Study";

    private Vrw32alp _svf;   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB _db2;                      //Databaseクラスを継承したクラス
    public boolean nonedata;
    private final KNJDefineSchool _definecode;  // 各学校における定数等設定

    private Param _param;
    private boolean _isGrd;
    private Form _form;

    public KNJE080_2(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode) {
        _db2 = db2;
        _svf = svf;
        nonedata = false;
        _definecode = definecode;
        _param = new Param(db2, _definecode);
        _form = new Form(_svf, _param);
        log.fatal("$Revision: 77202 $ $Date: 2020-10-01 15:50:33 +0900 (木, 01 10 2020) $");
    }

    /**
     *  PrepareStatement作成 KNJI070_2で継承しているためメソッドシグネチャ変更しない
     */
    public void pre_stat(final String hyotei, final Map paramMap) {
        _isGrd = "1".equals(paramMap.get("PRINT_GRD"));
    }

    public void printSvf(
            final String year,
            final String semester,
            final String date,
            final String schregno,
            final Map paramap,
            final String staffCd,
            final int paper,
            final String kanji,
            final String certifNumber) {

        if (_isGrd) {
            log.info("GRADUATION DATA");
        }
        final PrintData printData = new PrintData(_isGrd, year, semester, date, schregno, paramap, staffCd, certifNumber);
        if (_param._z010.in(Z010.sapporo)) {
            final KNJE080_1 a = new KNJE080_1(_db2, _svf, _definecode);
            a.pre_stat(null, printData._paramap);
            a.printSvf(printData._year, printData._semester, printData._date, printData._schregno, printData._paramap, printData._staffCd, paper, kanji, printData._certifNumber);
            a.pre_stat_f();
            nonedata = a.nonedata;
            return;
        }


        printData.load(_db2, _param);
        //成績証明書(英語)
        _form.printSvf(_db2, printData, _param);
        if (_form._hasData) {
            nonedata = true;
        }

        if (_param._z010.in(Z010.jyoto) && "KNJG010".equals(printData.parameter("PRGID"))) {
            printData._isPrintJp = true;
            printData.load(_db2, _param);
            //成績証明書(英語)
            _form.printSvf(_db2, printData, _param);
            if (_form._hasData) {
                nonedata = true;
            }
        }
    }

    public void pre_stat_f() {
        if (null != _param) {
            for (final Iterator<Map.Entry<String, File>> it = _param._createdFiles.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, File> e = it.next();
                final File file = e.getValue();
                boolean delete = false;
                if (!_param._isOutputDebugSvfFormCreate) {
                    delete = file.delete();
                }
                log.info(" file " + file.getName() + " " + delete);
                it.remove();
            }
            for (final PreparedStatement ps : _param._psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }
    }

    private static String defstr(final String ... s) {
        if (null == s) {
            return "";
        }
        if (s.length == 1) {
            return StringUtils.defaultString(s[0]);
        }
        for (final String v : s) {
            if (null != v) {
                return v;
            }
        }
        return "";
    }

    private static class PrintData {
        final String _schregno;
        final String _year; //現年度
        final boolean _isGrd;
        private int _schoolKindMinGrade;
        private String _schoolKind;
        private String _majorYdatSchooldiv;
        private int _maxgrade;
        private String _curriculumYear;
        final String _semester;
        final String _certifNumber;
        final String _staffCd;
        final String _date;
        final Map _paramap;
        final String _certifSchoolOnly;
        private boolean _isPrintJp;
        private Map _personalInfo = Collections.emptyMap();
        private List<Attend> _attendList = Collections.emptyList();
        private List<Map<String, String>> _abroadList = Collections.emptyList();
        private String _gradeCd;
        private String _graduDate;
        private String _grdDiv;
        private String _birthdayJp;
        private List<Map<String, String>> _studyrecRowList;
        private List<Map<String, String>> _studyrecRowListNormal;
        private List<Map<String, String>> _studyrecRowListSpecial;
        private Map<String, Map<String, String>> _annualYearMap = new HashMap<String, Map<String, String>>();
        private Map<String, String> _yearSchoolKindMap = new HashMap<String, String>();
        private String _studyrec90MaxSubclasscd;
        private String _ketteiSogoSubclassname;
        private String _ketteiSogoSubclassnameJp;
        private SchoolInfo _schoolInfo;
        private List<Studyrec> _studyrecListAll;

        private Map<Integer, Integer> _totalcredits;

        private Studyrec studyrecSogo; // 総合的な学習の時間
        private Studyrec studyrecAbroad; // 留学
        private Studyrec studyrecTotal; // 合計
        final List<String> _creditOnlyClasscdList = new ArrayList<String>(); // LHR等、最後の行に表示する教科のコード
        final List<Studyrec> _creditOnlyStudyrecClassList = new ArrayList<Studyrec>(); // LHR等、最後の行に表示する教科のコード

        final String _useGakkaSchoolDiv;
        final String _useAddrField2;

        PrintData(final boolean isGrd, final String year, final String semester, final String date, final String schregno, final Map paramap, final String staffCd, final String certifNumber) {
            _isGrd = isGrd;
            _year = year;
            _semester = semester;
            _date = date;
            _schregno = schregno;
            _paramap = paramap;
            _staffCd = staffCd;
            _certifNumber = certifNumber;
            final String paramSeisekishoumeishoCreditOnlyClasscd = parameter("seisekishoumeishoCreditOnlyClasscd");
            if (!StringUtils.isBlank(paramSeisekishoumeishoCreditOnlyClasscd)) {
                final String[] split = StringUtils.split(paramSeisekishoumeishoCreditOnlyClasscd, ",");
                if (null != split) {
                    for (final String classcd0 : split) {
                        final String classcd = classcd0.trim();
                        if (NumberUtils.isDigits(classcd)) {
                            _creditOnlyClasscdList.add(classcd);
                        }
                    }
                }
                log.info(" CreditOnlyClasscd = " + _creditOnlyClasscdList);
            }
            _certifSchoolOnly = parameter("certifSchoolOnly");

            _useGakkaSchoolDiv = parameter("useGakkaSchoolDiv");
            _useAddrField2 = parameter("useAddrField2");
        }

        public String parameter(final String name) {
            return (String) _paramap.get(name);
        }

        private List<Attend> getPrintAttendList(final Param param) {
            final List<Attend> list = new ArrayList<Attend>();
            for (final Attend attend : _attendList) {
                final String year = attend._year;
                final String annual = attend._annual;
                if (null != _schoolKind) {
                    final String yearSchoolKind = _yearSchoolKindMap.get(year);
                    if (!_schoolKind.equals(yearSchoolKind)) {
                        continue;
                    }
                }

                final int pos = getAnnualPosition(param, annual);
                if (pos == -1) {
                    continue;
                }
                list.add(attend);
            }
            return list;
        }

        protected String getSchooldiv(final Param param) {
            if ("1".equals(_useGakkaSchoolDiv)) {
                return null != _majorYdatSchooldiv ? _majorYdatSchooldiv : param._definecode.schooldiv;
            }
            return param._definecode.schooldiv;
        }

        protected boolean isGakunensei(final Param param) {
            return "0".equals(getSchooldiv(param));
        }

        public boolean isTankyu() {
            final int tankyuStartYear = 2019;
            return tankyuStartYear <= entCurriculumYear();
        }

        public int entCurriculumYear() {
            if (NumberUtils.isDigits(_curriculumYear)) {
                return Integer.parseInt(_curriculumYear);
            }
            final int year = NumberUtils.isDigits(_year) ? Integer.parseInt(_year) : 0;
            final int gradeCdInt = NumberUtils.isDigits(_gradeCd) ? Integer.parseInt(_gradeCd) : 0;
            return year - gradeCdInt + 1;
        }

        public void load(final DB2UDB db2, final Param param) {

            try {
                param._definecode.defineCode(db2, _year);         //各学校における定数等設定
                log.debug("schoolmark=" + param._definecode.schoolmark);
            } catch (Exception ex) {
                log.warn("defineCode error!", ex);
            }

            if ("1".equals(_useGakkaSchoolDiv)) {
                /**
                 * 指定生徒・年度の学科年度データの学校区分を得る
                 */
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
            }

            {
                final StringBuffer schoolKindSql = new StringBuffer();
                schoolKindSql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT T1 ");
                schoolKindSql.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE ");
                schoolKindSql.append("                 FROM SCHREG_REGD_DAT ");
                schoolKindSql.append("                 WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + _year + "' ");
                schoolKindSql.append("                 GROUP BY SCHREGNO, YEAR ");
                schoolKindSql.append("                ) T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, schoolKindSql.toString()));
            }

            {
                final StringBuffer lowerSchoolKindMaxGradeSql = new StringBuffer();
                lowerSchoolKindMaxGradeSql.append(" SELECT MIN(GRADE) AS MIN_GRADE FROM SCHREG_REGD_GDAT T1 ");
                lowerSchoolKindMaxGradeSql.append(" WHERE T1.YEAR = '" + _year + "' ");
                lowerSchoolKindMaxGradeSql.append("   AND T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                final String lowerSchoolKindMaxGradeString = KnjDbUtils.getOne(KnjDbUtils.query(db2, lowerSchoolKindMaxGradeSql.toString()));
                if (NumberUtils.isDigits(lowerSchoolKindMaxGradeString)) {
                    _schoolKindMinGrade = Integer.parseInt(lowerSchoolKindMaxGradeString);
                }
            }

            {
                final StringBuffer yearSchoolKindSql = new StringBuffer();
                yearSchoolKindSql.append(" SELECT T1.YEAR, T1.SCHOOL_KIND FROM SCHREG_REGD_GDAT T1 ");
                yearSchoolKindSql.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE ");
                yearSchoolKindSql.append("                 FROM SCHREG_REGD_DAT ");
                yearSchoolKindSql.append("                 WHERE SCHREGNO = '" + _schregno + "' ");
                yearSchoolKindSql.append("                 GROUP BY SCHREGNO, YEAR ");
                yearSchoolKindSql.append("                ) T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                _yearSchoolKindMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, yearSchoolKindSql.toString()), "YEAR", "SCHOOL_KIND");
                if (param._isOutputDebug) {
                    log.info(" schoolKind = " + _schoolKind + ", yearSchoolKindMap = " + _yearSchoolKindMap);
                }
            }

            {
                final StringBuffer gradeCdSql = new StringBuffer();
                gradeCdSql.append(" SELECT GDAT.GRADE_CD ");
                gradeCdSql.append(" FROM SCHREG_REGD_DAT T1 ");
                gradeCdSql.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                gradeCdSql.append(" WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.YEAR = '" + _year + "' ");
                _gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, gradeCdSql.toString()));
            }

            {
                final StringBuffer regdYearAnnualSql = new StringBuffer();
                regdYearAnnualSql.append(" SELECT T1.SCHREGNO, T1.ANNUAL, MIN(T1.YEAR) AS MIN_YEAR, MAX(T1.YEAR) AS MAX_YEAR ");
                regdYearAnnualSql.append(" FROM SCHREG_REGD_DAT T1 ");
                regdYearAnnualSql.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                regdYearAnnualSql.append("     AND GDAT.SCHOOL_KIND = '" + _schoolKind + "' ");
                regdYearAnnualSql.append(" WHERE T1.SCHREGNO = '" + _schregno + "' ");
                regdYearAnnualSql.append(" GROUP BY T1.SCHREGNO, T1.ANNUAL ");
                for (final Map row : KnjDbUtils.query(db2, regdYearAnnualSql.toString())) {
                    _annualYearMap.put(KnjDbUtils.getString(row, "ANNUAL"), row);
                }
            }

            String annual;
            String dropCount = null;
            {
                final StringBuffer sql1 = new StringBuffer();
                sql1.append(" SELECT MAX(ANNUAL) AS ANNUAL ");
                sql1.append(" FROM SCHREG_REGD_DAT T1 ");
                sql1.append(" WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.YEAR <= '" + _year + "' ");
                sql1.append("   AND T1.GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolKind + "') ");
                annual = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql1.toString()));
                if (null == annual) {
                    final StringBuffer sql2 = new StringBuffer();
                    sql2.append(" SELECT MAX(ANNUAL) AS ANNUAL ");
                    sql2.append(" FROM GRD_REGD_DAT T1 ");
                    sql2.append(" WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.YEAR <= '" + _year + "' ");
                    sql1.append("   AND T1.GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolKind + "') ");

                    annual = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql2.toString()));
                }
                if (isGakunensei(param)) {
                    final StringBuffer sqlr = new StringBuffer();
                    sqlr.append(" SELECT COUNT(DISTINCT YEAR) AS DROP_COUNT ");
                    sqlr.append(" FROM SCHREG_REGD_DAT T1 ");
                    sqlr.append(" WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.YEAR NOT IN (SELECT MAX(I1.YEAR) ");
                    sqlr.append("                                                             FROM SCHREG_REGD_DAT I1 ");
                    sqlr.append("                                                             WHERE I1.SCHREGNO = '" + _schregno + "' ");
                    sqlr.append("                                                             GROUP BY I1.GRADE) ");
                    dropCount = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlr.toString()));
                }
            }
            if (param._isOutputDebug) {
                log.info(" schregno = " + _schregno + ", maxgrade = " + annual + ", dropCount = " + dropCount);
            }
            if (null != annual) {
                _maxgrade = Integer.parseInt(annual); //最高学年取得
                if (_schoolKindMinGrade > 0) {
                    _maxgrade -= _schoolKindMinGrade - 1;
                }
                if (NumberUtils.isDigits(dropCount)) {
                    _maxgrade -= Integer.parseInt(dropCount);
                }
            }

            {
                final StringBuffer sqlr = new StringBuffer();
                sqlr.append(" SELECT CURRICULUM_YEAR ");
                sqlr.append(" FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
                sqlr.append(" WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                _curriculumYear = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlr.toString()));
            }

            load2(db2, param);

            _totalcredits = new TreeMap<Integer, Integer>();
            studyrecSogo = new Studyrec();
            studyrecAbroad = new Studyrec();
            studyrecTotal = new Studyrec();
            _studyrecListAll = getStudyrecList(this, param);
        }

        private List<Studyrec> getStudyrecList(final PrintData printData, final Param param) {

            final Map<String, Studyrec> subclassMap = new HashMap<String, Studyrec>();

            final List<String> fieldList = new ArrayList<String>();
            if (printData._studyrecRowList.size() > 0) {
                fieldList.addAll(KnjDbUtils.firstRow(printData._studyrecRowList).keySet());
            }

            final List<Studyrec> studyrecList = new ArrayList<Studyrec>();
            for (final Map<String, String> rs1 : printData._studyrecRowListNormal) {
                final String subclasscd = KnjDbUtils.getString(rs1, "SUBCLASSCD");
                final String sannual = KnjDbUtils.getString(rs1, "ANNUAL");

                //中高一貫で選択した学年が高校（4・5・6学年）のときは、高校のデータを出力する。
                final Map<String, String> yearMap = printData._annualYearMap.get(sannual);
                if (null != yearMap) {
                    if (null != printData._schoolKind) {
                        final String minYear = yearMap.get("MIN_YEAR");
                        final String maxYearSchoolKind = printData._yearSchoolKindMap.get(minYear);
                        if (!printData._schoolKind.equals(maxYearSchoolKind)) {
                            continue;
                        }
                        final String maxYear = yearMap.get("MAX_YEAR");
                        final String minYearSchoolKind = printData._yearSchoolKindMap.get(maxYear);
                        if (!printData._schoolKind.equals(minYearSchoolKind)) {
                            continue;
                        }
                    }
                }
                if (NumberUtils.isDigits(sannual)) {
                    int iannual0 = Integer.parseInt(sannual);
                    if (iannual0 == 0) {
                        continue;
                    }
                }

                Studyrec s = null;
                if (param._z010.in(Z010.kyoto)) {
                    for (final String key : subclassMap.keySet()) {
                        final Studyrec s1 = subclassMap.get(key);

                        final String[] ssplit = StringUtils.split(s1._subclasscd, "-");
                        final String[] ssplit1 = StringUtils.split(subclasscd, "-");

                        if (null == s1._subclasscd || null != ssplit && null != ssplit1 && ssplit.length != ssplit1.length) {
                            continue;
                        }
                        final String sRawSubclasscd = ssplit[ssplit.length == 1 ? 0 : 3];
                        final String rawSubclasscd = ssplit1[ssplit1.length == 1 ? 0 : 3];
                        if (sRawSubclasscd.equals(rawSubclasscd)) {
                            s = s1;
                            break;
                        }
                    }
                } else {
                    s = subclassMap.get(subclasscd);
                }
                if (null == s) {
                    Studyrec s1 = new Studyrec();
                    studyrecList.add(s1);
                    subclassMap.put(subclasscd, s1);
                    if (fieldList.contains("SPECIALDIV")) {
                        s1._specialdiv = KnjDbUtils.getString(rs1, "SPECIALDIV");
                    } else {
                        s1._specialdiv = "0";
                    }
                    s1._classname = KnjDbUtils.getString(rs1, "CLASSNAME");
                    s1._credit = KnjDbUtils.getString(rs1, "CREDIT");
                    s1._classcd = KnjDbUtils.getString(rs1, "CLASSCD");
                    s1._subclasscd = subclasscd;
                    s1._subclassname = KnjDbUtils.getString(rs1, "SUBCLASSNAME");
                    if (param._z010.in(Z010.sakae) || printData._isPrintJp) {
                        s1._classnameJp = KnjDbUtils.getString(rs1, "CLASSNAME_JP");
                        s1._subclassnameJp = KnjDbUtils.getString(rs1, "SUBCLASSNAME_JP");
                    }
                }
                s = subclassMap.get(subclasscd);

                final Hyotei h = new Hyotei();
                s._hyoteiList.add(h);
                h._grades = KnjDbUtils.getString(rs1, "GRADES");
                h._gradeCredit = KnjDbUtils.getString(rs1, "GRADE_CREDIT");

                int annualPosition = printData.getAnnualPosition(param, sannual);
                h._annualPosition = annualPosition;

                if (param._isOutputDebugQuery) {
                    log.info(" (normal) hyotei = " + h + " / " + Util.debugMapToStr("row1 = ", rs1, ""));
                }
            }

            for (final Map<String, String> rs1 : printData._studyrecRowListSpecial) {
                final String sannual = KnjDbUtils.getString(rs1, "ANNUAL");

                //中高一貫で選択した学年が高校（4・5・6学年）のときは、高校のデータを出力する。
                final Map<String, String> yearMap = printData._annualYearMap.get(sannual);
                if (null != yearMap) {
                    if (null != printData._schoolKind) {
                        final String minYear = yearMap.get("MIN_YEAR");
                        final String maxYearSchoolKind = printData._yearSchoolKindMap.get(minYear);
                        if (!printData._schoolKind.equals(maxYearSchoolKind)) {
                            continue;
                        }
                        final String maxYear = yearMap.get("MAX_YEAR");
                        final String minYearSchoolKind = printData._yearSchoolKindMap.get(maxYear);
                        if (!printData._schoolKind.equals(minYearSchoolKind)) {
                            continue;
                        }
                    }
                }
                if (NumberUtils.isDigits(sannual)) {
                    int iannual0 = Integer.parseInt(sannual);
                    if (iannual0 == 0) {
                        continue;
                    }
                }

                Studyrec s = null;
                final String classcd = KnjDbUtils.getString(rs1, "CLASSCD");
                final String specialFlg = KnjDbUtils.getString(rs1, "SPECIAL_FLG");
                final String classname = KnjDbUtils.getString(rs1, "CLASSNAME");
                if (sogo.equals(specialFlg)) {
                    s = printData.studyrecSogo;
                    if (null != param._seisekishoumeishoEngSogoPrintSubclassname) {
                        if (null == printData._ketteiSogoSubclassname) {
                            if (null == s._subclassname) {
                                s._subclassname = KnjDbUtils.getString(rs1, "SUBCLASSNAME");
                            } else if (!s._subclassname.equals(KnjDbUtils.getString(rs1, "SUBCLASSNAME"))) {
                                log.info(" multiple sogo : " + KnjDbUtils.getString(rs1, "SUBCLASSNAME") + " <> " + s._subclassname);
                            }
                        }
                    }
                } else if (abroad.equals(specialFlg)) {
                    s = printData.studyrecAbroad;
                } else if (total.equals(specialFlg)) {
                    s = printData.studyrecTotal;
                } else if (creditOnly.equals(specialFlg)) {
                    for (final Studyrec creditOnly : printData._creditOnlyStudyrecClassList) {
                        if (null != creditOnly._classcd && creditOnly._classcd.equals(KnjDbUtils.getString(rs1, "CLASSCD"))) {
                            s = creditOnly;
                            break;
                        }
                    }
                    if (null == s) {
                        s = new Studyrec();
                        s._classname = classname;
                        s._credit = KnjDbUtils.getString(rs1, "CREDIT");
                        s._classcd = classcd;
                        s._subclasscd = KnjDbUtils.getString(rs1, "SUBCLASSCD");
                        s._subclassname = KnjDbUtils.getString(rs1, "SUBCLASSNAME");
                        printData._creditOnlyStudyrecClassList.add(s);
                    }
                } else {
                    log.info(Util.debugMapToStr("undefined row1 = ", rs1, ""));
                    continue;
                }

                final Hyotei h = new Hyotei();
                s._hyoteiList.add(h);
                h._grades = KnjDbUtils.getString(rs1, "GRADES");
                h._gradeCredit = KnjDbUtils.getString(rs1, "CREDIT");

                int annualPosition = printData.getAnnualPosition(param, sannual);
                h._annualPosition = annualPosition;

                if (!creditOnly.equals(specialFlg)) {
                    log.info(" totalcredits = " + printData._totalcredits + " , annualPosition " + annualPosition + ", credit " + h._gradeCredit);
                    if (0 < annualPosition && h._gradeCredit != null) {
                        if (!printData._totalcredits.containsKey(annualPosition)) {
                            printData._totalcredits.put(annualPosition, 0);
                        }
                        printData._totalcredits.put(annualPosition, printData._totalcredits.get(annualPosition) + Integer.parseInt(h._gradeCredit));
                        log.info(" totalcredits " + printData._totalcredits);
                    }
                }
                if (param._isOutputDebug) {
                    log.info(" (special) hyotei = " + h + " / " + Util.debugMapToStr("row1 = ", rs1, ""));
                }
            }
            return studyrecList;
        }

        public void load2(final DB2UDB db2, final Param param) {

            for (final Map<String, String> row : Util.reverse(param._e021RecordList)) {
                final String namespare1 = KnjDbUtils.getString(row, "NAMESPARE1");
                if (NumberUtils.isDigits(namespare1) && Integer.parseInt(namespare1) <= entCurriculumYear()) {
                    _ketteiSogoSubclassname = KnjDbUtils.getString(row, "NAME1");
                    break;
                }
            }
            if (null != param._seisekishoumeishoEngSogoPrintSubclassname && param._seisekishoumeishoEngSogoPrintSubclassname.length() >= 6) {
                for (final String cdCand : param._seisekishoumeishoEngSogoPrintSubclassname.split("\\s+")) {
                    final String subclassMstNameEng = param._subclassKeySubclassnameEng90Map.get(cdCand.trim());
                    if (!StringUtils.isBlank(subclassMstNameEng)) {
                        _ketteiSogoSubclassname = subclassMstNameEng;
                        if (param._isOutputDebug) {
                            log.info(" 総合の科目名はプロパティ設定の科目 " + param._seisekishoumeishoEngSogoPrintSubclassname + " = " + _ketteiSogoSubclassname);
                        }
                        break;
                    }
                }
            }
            final StringBuffer studyrecSql = new StringBuffer();
            if ("1".equals(param._useCurriculumcd)) {
                studyrecSql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASS_KEY ");
            } else {
                studyrecSql.append(" SELECT SUBCLASSCD AS SUBCLASS_KEY ");
            }
            studyrecSql.append(" FROM SCHREG_STUDYREC_DAT WHERE CLASSCD = '90' AND SCHREGNO = '" + _schregno + "' ");
            final List<String> sogoSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, studyrecSql.toString()), "SUBCLASS_KEY");
            Collections.sort(sogoSubclasscdList);
            _studyrec90MaxSubclasscd = sogoSubclasscdList.isEmpty() ? null : sogoSubclasscdList.get(sogoSubclasscdList.size() - 1);
            if (param._isOutputDebug) {
                log.info(" _studyrec90MaxSubclasscd = " + _studyrec90MaxSubclasscd + " / subclasscdList = " + sogoSubclasscdList);
            }

            final String psKeyPer = "PERSONAL_INFO";
            if (null == param.getPs(psKeyPer)) {
                // 個人データ
                final Map personalinfoSqlParamMap = new HashMap();
                if (_isGrd) {
                    personalinfoSqlParamMap.put("PRINT_GRD", "1");
                }
                if (param._hasSchoolMstSchoolKind) {
                    personalinfoSqlParamMap.put("SCHOOL_MST_SCHOOL_KIND", "H");
                }
                param._psPersonalinfoSql = new KNJ_PersonalinfoSql().sql_info_reg("11111011", personalinfoSqlParamMap);
                param.setPs(db2, psKeyPer, param._psPersonalinfoSql);
            }
            final Object[] ps6Parameter = {_schregno, _year, _semester, _schregno, _year};
            if (param._isOutputDebugQuery) {
                log.info(" personalInfo arg = " + ArrayUtils.toString(ps6Parameter));
            }
            _personalInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKeyPer), ps6Parameter));
            if (null != KnjDbUtils.getString(_personalInfo, "BIRTHDAY")) {
                _birthdayJp = Util.formatBirthdayJp(db2, KnjDbUtils.getString(_personalInfo, "BIRTHDAY"), param._isSeireki || "1".equals(KnjDbUtils.getString(_personalInfo, "BIRTHDAY_FLG")));
            }
            _grdDiv = KnjDbUtils.getString(_personalInfo, "GRD_DIV");
            _graduDate = KnjDbUtils.getString(_personalInfo, "GRADU_DATE");

            if (!param._z010.in(Z010.sapporo)) {

                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  TRANSFER_SDATE,TRANSFER_EDATE,TRANSFERPLACE,ABROAD_CREDITS ");
                stb.append("FROM    SCHREG_TRANSFER_DAT ");
                stb.append("WHERE   SCHREGNO = '" + _schregno + "' AND TRANSFERCD = '1' ");
                stb.append(    "AND INT(FISCALYEAR(TRANSFER_SDATE)) <= " + _year + " ");
                stb.append("ORDER BY TRANSFER_SDATE ");

                _abroadList = KnjDbUtils.query(db2, stb.toString());
            }

            // 出欠記録データ
            final String psKeyAtt = "ATTEND_INFO";
            if (null == param.getPs(psKeyAtt)) {

                //  出欠記録データ
                final String tname1;
                if (_isGrd) {
                    tname1 = "GRD_ATTENDREC_DAT";
                } else {
                    tname1 = "SCHREG_ATTENDREC_DAT";
                }
                //  出欠記録データ
                final StringBuffer stb = new StringBuffer();
                stb.append( "SELECT DISTINCT ");
                stb.append(      "T1.YEAR,");
                stb.append(      "ANNUAL,");
                stb.append(      "VALUE(CLASSDAYS,0) AS CLASSDAYS,"); //授業日数
                stb.append(      "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                if (param._z010.in(Z010.Kindai)) {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
                } else {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
                }
                stb.append(         "END AS ATTEND_1,"); //授業日数-休学日数
                stb.append(    "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,");          //出停・忌引
                stb.append(    "VALUE(SUSPEND,0) AS SUSPEND,");                                //出停
                stb.append(    "VALUE(MOURNING,0) AS MOURNING,");                              //忌引
                stb.append(    "VALUE(ABROAD,0) AS ABROAD,");                                  //留学
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append(         "END AS REQUIREPRESENT,"); //要出席日数
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append(         "END AS KESSEKI,"); //病欠＋事故欠（届・無）
                stb.append(    "VALUE(PRESENT,0) AS PRESENT "); //出席日数
                stb.append("FROM ");
                stb.append(    "(");
                stb.append(        "SELECT ");
                stb.append(            "SCHREGNO,");
                stb.append(            "YEAR,");
                stb.append(            "ANNUAL,");
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
                stb.append(            "ANNUAL,");
                stb.append(            "YEAR ");
                stb.append(    ")T1 ");
                stb.append(    "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                if (param._hasSchoolMstSchoolKind) {
                    stb.append(    "    AND S1.SCHOOL_KIND = 'H' ");
                }
                stb.append("ORDER BY ");
                stb.append(    "T1.ANNUAL");
                param._psAttendrecSql = stb.toString();
                param.setPs(db2, psKeyAtt, param._psAttendrecSql);
            }

            final Object[] ps2Parameter = {_schregno, _year};
            if (param._isOutputDebug) {
                log.info(" attendrec arg = " + ArrayUtils.toString(ps2Parameter));
            }
            if (param._isOutputAttend) {
                _attendList = new ArrayList<Attend>();
                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKeyAtt), ps2Parameter)) {
                    _attendList.add(new Attend(this, param, row));
                }
            }

            try {

                {
                    final String sql = getStudyrecSql(param, this, 0);
                    if (param._isOutputDebugQuery) {
                        log.info(" studyrec sql = " + sql);
                    }
                    _studyrecRowListNormal = KnjDbUtils.query(db2, sql);
                }

                _studyrecRowListSpecial = new ArrayList<Map<String, String>>();
                {
                    // 総学等
                    final String sql1 = getStudyrecSql(param, this, 1);
                    if (param._isOutputDebugQuery) {
                        log.info(" studyrec sql1 = " + sql1);
                    }
                    _studyrecRowListSpecial.addAll(KnjDbUtils.query(db2, sql1));
                }

                {
                    // 留学データ
                    final String abroadSql = getAbroadSql(param, this);
                    if (param._isOutputDebugQuery) {
                        log.info(" abroad sql = " + abroadSql);
                    }
                    _studyrecRowListSpecial.addAll(KnjDbUtils.query(db2, abroadSql));
                }
                _studyrecRowList = new ArrayList<Map<String, String>>();
                _studyrecRowList.addAll(_studyrecRowListNormal);
                _studyrecRowList.addAll(_studyrecRowListSpecial);

            } catch (Exception e) {
                log.error("exception!", e);
            }
            for (final Map row : _studyrecRowList) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                if (null != _schoolKind) {
                    if (!_schoolKind.equals(_yearSchoolKindMap.get(year))) {
                        continue;
                    }
                }
                if (_annualYearMap.containsKey(annual)) {
                    continue;
                }
                if (NumberUtils.isDigits(annual) && NumberUtils.isDigits(year)) {
                    final Map<String, String> map = new HashMap<String, String>();
                    map.put("MIN_YEAR", year);
                    map.put("MAX_YEAR", String.valueOf(Integer.parseInt(year) + 1));
                    _annualYearMap.put(annual, map);
                }
            }
            if (param._isOutputDebug) {
                log.info(Util.debugMapToStr(" annualYearMap = ", _annualYearMap, ""));
            }

            _schoolInfo = SchoolInfo.getSchoolInfo(db2, param, this);
        }

        private int getAnnualPosition(final Param param, final String annual) {
            if (!NumberUtils.isDigits(annual)) {
                return -1;
            }
//            final int annualInt = Integer.parseInt(annual);
//            int pos = annualInt;
//            if (_param._z010.in(Z010.sundaikoufu)) {
//                if (pos > 9) {
//                    pos = pos - 9;
//                } else if (pos > 6) {
//                    pos = pos - 6;
//                }
//            } else if (_param._isJuniorHiSchool && (3 < pos)) {
//                pos = pos - 3;
//            }
            int pos = Integer.parseInt(annual) - _schoolKindMinGrade + 1;
            return pos;
        }

        public String getAbroadSql(final Param param, final PrintData printData) {

            final int _stype = param.isTok() ? 4 : 1; // 総合的な学習の時間、留学単位、修得単位の集計区分 1:合計のみ 2:合計&学年別 3:学年別のみ 0:出力無
            String tname2, tname3;
            if (printData._isGrd) {
                tname2 = "GRD_TRANSFER_DAT";
                tname3 = "GRD_REGD_DAT";
            } else {
                tname2 = "SCHREG_TRANSFER_DAT";
                tname3 = "SCHREG_REGD_DAT";
            }

            final StringBuffer stb = new StringBuffer();
            final boolean withSubclassnameJp = param._z010.in(Z010.sakae) || printData._isPrintJp;
            //  留学中の修得単位数（合計）
            stb.append(     " SELECT ");
            stb.append(         " '" + abroad + "' AS SPECIAL_FLG ");
            stb.append(         ", '0' AS ANNUAL ");
            stb.append(         ", CAST(NULL AS VARCHAR(1)) AS YEAR ");
            stb.append(         ", 'AA' AS CLASSCD ");
            stb.append(         ", '" + abroad + "' AS CLASSNAME ");
            if (withSubclassnameJp) {
                stb.append(     ", '" + abroad + "' AS CLASSNAME_JP ");
            }
            stb.append(         ", 'AAAA' AS SUBCLASSCD ");
            stb.append(         ", '" + abroad + "' AS SUBCLASSNAME ");
            if (withSubclassnameJp) {
                stb.append(     ",'" + abroad + "' AS SUBCLASSNAME_JP ");
            }
            stb.append(         ",0 AS GRADES ");
            stb.append(         ",SUM(ABROAD_CREDITS) AS CREDIT ");
            stb.append(     "FROM ");
            stb.append(         "(SELECT ");
            stb.append(             "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            stb.append(         "FROM  ");
            stb.append(              tname2 + " ");
            stb.append(         "WHERE  ");
            stb.append(             "SCHREGNO = '" + _schregno + "' AND TRANSFERCD = '1' ");
            stb.append(         ")ST1 ");
            stb.append(     "WHERE ");
            stb.append(         "TRANSFER_YEAR <= " + Integer.valueOf(_year) + " ");
            //  留学中の修得単位数（学年別）
            if (_stype > 1) {              //----->学年別/合計
                stb.append( "UNION SELECT ");
                stb.append(     " '" + abroad + "' AS SPECIAL_FLG ");
                stb.append(     ", ANNUAL ");
                stb.append(     ", CAST(NULL AS VARCHAR(1)) AS YEAR ");
                stb.append(     ", 'AA' AS CLASSCD ");
                stb.append(     ", '" + abroad + "' AS CLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append( ", '" + abroad + "' AS CLASSNAME_JP ");
                }
                stb.append(     ", 'AAAA' AS SUBCLASSCD ");
                stb.append(     ", '" + abroad + "' AS SUBCLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(    ", '" + abroad + "' AS SUBCLASSNAME_JP ");
                }
                stb.append(", 0 AS GRADES ");
                stb.append(", SUM(ABROAD_CREDITS) AS CREDIT ");
                stb.append( "FROM ");
                stb.append(         "(SELECT ");
                stb.append(             "ABROAD_CREDITS,");
                stb.append(             "INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                stb.append(         "FROM ");
                stb.append(              tname2 + " ");
                stb.append(         "WHERE ");
                stb.append(             "SCHREGNO = '" + _schregno + "' AND TRANSFERCD = '1' ");
                stb.append(         ")ST1,");
                stb.append(         "(SELECT ");
                stb.append(             "ANNUAL,MAX(YEAR) AS YEAR ");
                stb.append(         "FROM ");
                stb.append(              tname3 + " ");
                stb.append(         "WHERE ");
                stb.append(             "SCHREGNO = '" + _schregno + "' AND YEAR <= '" + _year + "' ");
                stb.append(         "GROUP BY ");
                stb.append(             "ANNUAL ");
                stb.append(         ")ST2 ");
                stb.append( "WHERE ");
                stb.append(     "ST1.TRANSFER_YEAR <= " + Integer.valueOf(_year) + " ");
                stb.append(     "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                stb.append( "GROUP BY ");
                stb.append(     "ANNUAL ");
            }
            stb.append( "ORDER BY CLASSCD, SUBCLASSCD, ANNUAL");
            return stb.toString();

        }

        public String getStudyrecSql(final Param param, final PrintData printData, final int flg) {
//          final String _hyoutei; // 評定の読替え  １を２と評定 on:評定１の読替有 off:評定１の読替無 hyde:出力無 grade:学年別評定有
//			if (param.isTok()) {
//				_hyoutei = "grade";
//			} else {
//				_hyoutei = "off";
//			}
//          final String _atype = atype(_config);
            final boolean useD065 = !printData._isGrd;
            final boolean withSubclassnameJp = param._z010.in(Z010.sakae) || printData._isPrintJp;
            final boolean isNotPrintMirishu = param.isTok() && param.isNotPrintMirishu(CERTIF_KINDCD); // trueなら未履修科目を表示しない

            String studyrecTab;
            if (printData._isGrd) {
                studyrecTab = "GRD_STUDYREC_DAT";
            } else {
                studyrecTab = "SCHREG_STUDYREC_DAT";
            }

            final StringBuffer stb = new StringBuffer();
            //  評定１を２と判定
            String h_1_2 = null;
            String h_1_3 = null;
//            if (_hyoutei.equals("on")) { //----->評定読み替えのON/OFF  評定１を２と読み替え
//                h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
//                h_1_3 = "T1.CREDIT ";  //NO001
//                //NO001 h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END ";
//            } else {
                h_1_2 = "T1.GRADES ";
                h_1_3 = "T1.CREDIT ";
//            }

            final String targetSchoolKind = "1".equals(param._useCurriculumcd) ? printData._schoolKind : null;
            final boolean notUseStudyrecProvFlgDat = !param._hasStudyrecProvFlgDat;
            final boolean checkDropYear = printData.isGakunensei(param);

            stb.append(" WITH DROP_YEAR AS(");
            stb.append("        SELECT DISTINCT T1.YEAR ");
            stb.append("        FROM SCHREG_REGD_DAT T1");
            stb.append("        WHERE T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
            stb.append("                            WHERE T2.SCHREGNO = '" + _schregno + "' ");
            stb.append("                              AND T2.YEAR <= '" + _year + "' ");
            stb.append("                            GROUP BY T2.GRADE) ");
            if (param._z010.in(Z010.Kindai)) {
                stb.append(" ), STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD, ");
                stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK, ");
                stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
                stb.append(        "T1.VALUATION AS GRADES ");
                stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                if (useD065) {
                    stb.append("        ,NMD065.NAME1 AS D065FLG ");
                } else {
                    stb.append("        ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                }
                stb.append("FROM   " + studyrecTab + " T1 ");
                stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                stb.append(        "L2.CLASSCD = T1.CLASSCD ");
                stb.append(        " AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(        " AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append(        " AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                    stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                    stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                    stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                    stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                }
                if (useD065) {
                    stb.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                }

                stb.append("WHERE   T1.SCHREGNO = '" + _schregno + "' ");
                stb.append(        "AND T1.YEAR <= '" + _year + "' ");
                if (checkDropYear) {
                    stb.append("     AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                }
                stb.append(        "AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
                stb.append(        "AND NOT EXISTS(SELECT  'X' ");
                stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                stb.append(                   "WHERE   T2.YEAR = T1.YEAR ");
                stb.append(                           "AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append(                           "AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(                           "AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append(                           "AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(        "           ) ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append("    AND TPROV.SUBCLASSCD IS NULL ");
                }
                stb.append(    ")");

            } else {
                stb.append("), T_STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                } else {
                    stb.append(        "T1.SUBCLASSCD AS SUBCLASSCD, ");
                }
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || L2.SUBCLASSCD2 AS SUBCLASSCD2, ");
                } else {
                    stb.append(        "L2.SUBCLASSCD2 AS SUBCLASSCD2, ");
                }
                stb.append(        "T1.VALUATION AS GRADES ");
                if (param._z010.in(Z010.HOUSEI)) {
                    stb.append(   ",CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(   ",CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END || '-' || T1.SCHOOL_KIND AS CLASS_SCHK ");
                    }
                } else {
                    stb.append(   ",T1.CLASSCD ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(   ",T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK ");
                    }
                }
                stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                stb.append("       ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.append("FROM   " + studyrecTab + " T1 ");
                stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "L2.CLASSCD = T1.CLASSCD AND ");
                    stb.append(        "L2.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                    stb.append(        "L2.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
                }
                stb.append(            "L2.SUBCLASSCD = T1.SUBCLASSCD ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                    stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                    stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                        stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                    stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                }
                stb.append("WHERE   T1.SCHREGNO = '" + _schregno + "' ");
                stb.append("    AND T1.YEAR <= '" + _year + "' ");
                if (checkDropYear) {
                    stb.append("     AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                }
                stb.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                for (final String creditOnlyClasscd : printData._creditOnlyClasscdList) {
                    stb.append("     OR T1.CLASSCD = '" + creditOnlyClasscd + "' "); // 特別活動 ホームルーム
                }
                stb.append("        ) ");
                if (null != targetSchoolKind) {
                    stb.append(        "AND T1.SCHOOL_KIND = '" + targetSchoolKind + "' ");
                }
                if (isNotPrintMirishu) {
                    stb.append(        "AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
                }
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "    AND TPROV.SUBCLASSCD IS NULL ");
                }
                stb.append(") , STUDYREC0 AS( ");
                stb.append(    "SELECT ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append(        "T1.GRADES, ");
                stb.append(        "T1.CREDIT ");
                stb.append("        ,D065FLG ");
                stb.append(    "FROM ");
                stb.append(        "T_STUDYREC T1 ");
                stb.append(    "WHERE ");
                stb.append(        "T1.SUBCLASSCD2 IS NULL ");
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT ");
                stb.append(        "MAX(T1.CLASSNAME) AS CLASSNAME, ");
                stb.append(        "MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append(        "MAX(T1.SUBCLASSNAME_ENG) AS SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "MAX(T1.ANNUAL) AS ANNUAL, ");
                stb.append(        "T1.CLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "MAX(T1.CLASS_SCHK) AS CLASS_SCHK, ");
                }
                stb.append(        "T1.SUBCLASSCD2 AS SUBCLASSCD, ");
                stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                stb.append("        ,MAX(D065FLG) AS D065FLG ");
                stb.append(    "FROM ");
                stb.append(        "T_STUDYREC T1 ");
                stb.append(    "WHERE ");
                stb.append(        "T1.SUBCLASSCD2 IS NOT NULL ");
                stb.append(    "GROUP BY ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.CLASSCD, ");
                stb.append(        "T1.SUBCLASSCD2 ");
                stb.append(") , STUDYREC AS( ");
                stb.append(    "SELECT ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append("        D065FLG, ");
                stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                stb.append(    "FROM ");
                stb.append(        "STUDYREC0 T1 ");
                stb.append(    "GROUP BY ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME_ENG, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append(        "T1.D065FLG ");
                stb.append(") ");
            }

            if (flg == 0) {
                //該当生徒の科目評定、修得単位及び教科評定平均
                stb.append( "SELECT ");
                stb.append(     "VALUE(T2.SPECIALDIV, '0') as SPECIALDIV,");
                stb.append(     "T2.SHOWORDER2 as CLASS_ORDER,");
                stb.append(     "T3.SHOWORDER2 as SUBCLASS_ORDER,");
                stb.append(     "T1.ANNUAL,");
                stb.append(     "T1.YEAR, ");
                stb.append(     "T1.CLASSCD,");
                stb.append(     "T2.CLASSNAME_ENG AS CLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append( "VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME_JP,");
                }
                stb.append(     "T1.SUBCLASSCD,");
                stb.append(     "VALUE(CASE WHEN T1.SUBCLASSNAME_ENG <> '' THEN T1.SUBCLASSNAME_ENG END, T3.SUBCLASSNAME_ENG) AS SUBCLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append( "VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME_JP,");
                }
                stb.append( h_1_2 + " AS GRADES ");
                stb.append(     ",T1.CREDIT AS GRADE_CREDIT ");
                stb.append(     ",T4.CREDIT ");
                stb.append(     ", D065FLG ");
                stb.append( "FROM ");
                stb.append(     "STUDYREC T1 ");
                stb.append(     "LEFT JOIN CLASS_MST T2 ON ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASS_SCHK ");
                } else {
                    stb.append(        " T2.CLASSCD = T1.CLASSCD ");
                }
                stb.append(     "LEFT JOIN SUBCLASS_MST T3 ON ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T1.SUBCLASSCD ");
                } else {
                    stb.append(        " T3.SUBCLASSCD = T1.SUBCLASSCD ");
                }
                //  修得単位数の計
                stb.append(     "INNER JOIN(SELECT ");
                stb.append(             "CLASSCD,SUBCLASSCD,SUM(" + h_1_3 + ") AS CREDIT ");
                stb.append(         "FROM ");
                stb.append(             "STUDYREC T1 ");
                stb.append(         "WHERE ");
                stb.append(             "CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append(         "GROUP BY ");
                stb.append(             "CLASSCD,SUBCLASSCD ");
                stb.append(     ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append( "WHERE ");
                stb.append(     "T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append( "ORDER BY SPECIALDIV, CLASS_ORDER, CLASSCD, SUBCLASS_ORDER, SUBCLASSCD, ANNUAL");
            } else {
                stb.append( " , GOKEI AS ( ");
                //  総合学習の修得単位数（合計）
                stb.append( " SELECT ");
                stb.append(         " '" + sogo + "' AS SPECIAL_FLG ");
                stb.append(         ",'" + sogoGoukeiAnnual + "' AS ANNUAL");
                stb.append(         ", CAST(NULL AS VARCHAR(1)) AS YEAR ");
                stb.append(         ", '" + KNJDefineSchool.subject_T + "' AS CLASSCD ");
                stb.append(         ", '" + sogo + "' AS CLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(     ", '" + sogo + "' AS CLASSNAME_JP ");
                }
                stb.append(         ", '" + KNJDefineSchool.subject_T + "01' AS SUBCLASSCD ");
                stb.append(         ", MAX(T1.SUBCLASSCD) AS MAX_SUBCLASSCD ");
                stb.append(         ", COUNT(DISTINCT T1.SUBCLASSCD) AS DIST_SUBCLASSCD ");
                if (null != param._seisekishoumeishoEngSogoPrintSubclassname) {
                    stb.append(         ", MAX(T3.SUBCLASSNAME_ENG) AS SUBCLASSNAME ");
                } else {
                    stb.append(         ", '" + sogo + "' AS SUBCLASSNAME ");
                }
                if (withSubclassnameJp) {
                    stb.append(     ",'" + sogo + "' AS SUBCLASSNAME_JP ");
                }
                stb.append(         ", SUM(CREDIT) AS CREDIT ");
                stb.append(         ", MAX(D065FLG) AS D065FLG ");
                stb.append(     "FROM ");
                stb.append(         "STUDYREC T1 ");
                stb.append(     "LEFT JOIN SUBCLASS_MST T3 ON ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T1.SUBCLASSCD ");
                } else {
                    stb.append(        " T3.SUBCLASSCD = T1.SUBCLASSCD ");
                }
                stb.append(     "WHERE ");
                stb.append(         "T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");

                //  総合学習の修得単位数（学年別）
                if (param.isTok()) {          //----->学年別
                    stb.append("UNION SELECT ");
                    stb.append(    " '" + sogo + "' AS SPECIAL_FLG ");
                    stb.append(    ", ANNUAL");
                    stb.append(    ",CAST(NULL AS VARCHAR(1)) AS YEAR ");
                    stb.append(    ", '" + KNJDefineSchool.subject_T + "' AS CLASSCD ");
                    stb.append(    ", '" + sogo + "' AS CLASSNAME ");
                    if (withSubclassnameJp) {
                        stb.append(     ", '" + sogo + "' AS CLASSNAME_JP ");
                    }
                    stb.append(     ", '" + KNJDefineSchool.subject_T + "01' AS SUBCLASSCD ");
                    stb.append(     ", MAX(T1.SUBCLASSCD) AS MAX_SUBCLASSCD ");
                    stb.append(     ", COUNT(DISTINCT T1.SUBCLASSCD) AS DIST_SUBCLASSCD ");
                    if (null != param._seisekishoumeishoEngSogoPrintSubclassname) {
                        stb.append(         ", MAX(T3.SUBCLASSNAME_ENG) AS SUBCLASSNAME ");
                    } else {
                        stb.append(         ", '" + sogo + "' AS SUBCLASSNAME ");
                    }
                    if (withSubclassnameJp) {
                        stb.append(     ", '" + sogo + "' AS SUBCLASSNAME_JP");
                    }
                    stb.append(         ", SUM(T1.CREDIT) AS CREDIT ");
                    stb.append(         ", MAX(T1.D065FLG) AS D065FLG ");
                    stb.append( "FROM ");
                    stb.append(     "STUDYREC T1 ");
                    if (null != param._seisekishoumeishoEngSogoPrintSubclassname) {
                        stb.append(     "LEFT JOIN SUBCLASS_MST T3 ON ");
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T1.SUBCLASSCD ");
                        } else {
                            stb.append(        " T3.SUBCLASSCD = T1.SUBCLASSCD ");
                        }
                    }
                    stb.append( "WHERE ");
                    stb.append(     "T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                    stb.append( "GROUP BY ");
                    stb.append(     "T1.ANNUAL ");
                }

                //  修得単位数
                if (param.isTok()) {          //----->学年別
                    stb.append( "UNION SELECT ");
                    stb.append(    " '" + total + "' AS SPECIAL_FLG ");
                    stb.append(    ", ANNUAL ");
                    stb.append(    ", CAST(NULL AS VARCHAR(1)) AS YEAR ");
                    stb.append(    ", 'ZZ' AS CLASSCD ");
                    stb.append(    ", '" + total + "' AS CLASSNAME ");
                    if (withSubclassnameJp) {
                        stb.append(     ", '" + total + "' AS SUBCLASSNAME_JP ");
                    }
                    stb.append(     ", 'ZZZZ' AS SUBCLASSCD ");
                    stb.append(     ", MAX('ZZZZ') AS MAX_SUBCLASSCD ");
                    stb.append(     ", COUNT(DISTINCT 'ZZZZ') AS DIST_SUBCLASSCD ");
                    stb.append(     ", '" + total + "' AS SUBCLASSNAME ");
                    if (withSubclassnameJp) {
                        stb.append(     ", '" + total + "' AS SUBCLASSNAME_JP ");
                    }
                    stb.append(     ", SUM(" + h_1_3 + ") AS CREDIT ");
                    stb.append(     ", MAX(D065FLG) AS D065FLG ");
                    stb.append( "FROM ");
                    stb.append(     "STUDYREC T1 ");
                    stb.append( "WHERE ");
                    stb.append(     " T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                    for (final String lastLineClasscd : printData._creditOnlyClasscdList) {
                        stb.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
                    }
                    stb.append( "GROUP BY ");
                    stb.append(     "ANNUAL ");
                }

                //  全体の修得単位数
                stb.append(     "UNION SELECT ");
                stb.append(        " '" + total + "' AS SPECIAL_FLG ");
                stb.append(        ",'" + totalGoukeiAnnual + "' AS ANNUAL ");
                stb.append(        ",CAST(NULL AS VARCHAR(1)) AS YEAR ");
                stb.append(        ",'ZZ' AS CLASSCD ");
                stb.append(        ",'" + total + "' AS CLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(    ", '" + total + "' AS CLASSNAME_JP ");
                }
                stb.append(        ", 'ZZZZ' AS SUBCLASSCD");
                stb.append(        ", MAX('ZZZZ') AS MAX_SUBCLASSCD ");
                stb.append(        ", COUNT(DISTINCT 'ZZZZ') AS DIST_SUBCLASSCD ");
                stb.append(        ", '" + total + "' AS SUBCLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(     ", '" + total + "' AS SUBCLASSNAME_JP ");
                }
                stb.append(        ", SUM(" + h_1_3 + ") AS CREDIT ");
                stb.append(        ", MAX(D065FLG) AS D065FLG ");
                stb.append(     "FROM ");
                stb.append(         "STUDYREC T1 ");
                stb.append(     "WHERE ");
                stb.append(         "T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                for (final String lastLineClasscd : printData._creditOnlyClasscdList) {
                    stb.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
                }

                //  最後の行に表示する教科の修得単位数
                if (!printData._creditOnlyClasscdList.isEmpty()) {
                    stb.append(     "UNION SELECT ");
                    stb.append(        " '" + creditOnly + "' AS SPECIAL_FLG ");
                    stb.append(        ", T1.ANNUAL ");
                    stb.append(        ",CAST(NULL AS VARCHAR(1)) AS YEAR ");
                    stb.append(        ",T1.CLASSCD ");
                    stb.append( ",T2.CLASSNAME_ENG AS CLASSNAME");
                    if (withSubclassnameJp) {
                        stb.append( ",VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME_JP");
                    }
                    stb.append(        ", '" + creditOnly + "' AS SUBCLASSCD");
                    stb.append(        ", MAX(T1.SUBCLASSCD) AS MAX_SUBCLASSCD ");
                    stb.append(        ", COUNT(DISTINCT T1.SUBCLASSCD) AS DIST_SUBCLASSCD ");
                    stb.append(        ", '" + creditOnly + "' AS SUBCLASSNAME ");
                    if (withSubclassnameJp) {
                        stb.append(     ", '" + creditOnly + "' AS SUBCLASSNAME_JP ");
                    }
                    stb.append(        ", SUM(" + h_1_3 + ") AS CREDIT ");
                    stb.append(        ", CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                    stb.append(     "FROM ");
                    stb.append(         "STUDYREC T1 ");
                    stb.append(     "LEFT JOIN CLASS_MST T2 ON ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASS_SCHK ");
                    } else {
                        stb.append(        " T2.CLASSCD = T1.CLASSCD ");
                    }
                    stb.append(     "WHERE (");
                    String or = "";
                    for (final String lastLineClasscd : printData._creditOnlyClasscdList) {
                        stb.append("     " + or + " T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
                        or = " OR ";
                    }
                    stb.append(     "     ) ");
                    stb.append( "GROUP BY ");
                    stb.append(     " T1.CLASSCD, T1.ANNUAL ");
                    stb.append(     ",T2.CLASSNAME_ENG ");
                    if (withSubclassnameJp) {
                        stb.append( ",VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) ");
                    }
                }

                stb.append( " ) ");
                stb.append( " SELECT ");
                stb.append(     " '9999' as SPECIALDIV ");
                stb.append(         ",SPECIAL_FLG");
                stb.append(         ",ANNUAL");
                stb.append(         ",YEAR ");
                stb.append(         ",CLASSCD ");
                stb.append(         ",CLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(     " ,CLASSNAME_JP ");
                }
                stb.append(        ",SUBCLASSCD ");
                stb.append(     "   ,SUBCLASSNAME ");
                if (withSubclassnameJp) {
                    stb.append(    ", SUBCLASSNAME_JP ");
                }
                stb.append(        ", CAST(NULL AS SMALLINT) AS GRADES ");
                stb.append(        ", CAST(NULL AS SMALLINT) AS GRADE_CREDIT ");
                stb.append(        ", CREDIT ");
                stb.append("        , D065FLG ");
                stb.append( " FROM GOKEI ");
                stb.append( "ORDER BY SPECIALDIV, CLASSCD, SUBCLASSCD, ANNUAL");
            }

            return stb.toString();
        }
    }

    private static class SchoolInfo {
        private final Map _schoolMst;
        private final Map _certifSchoolDat;
        private String _staffPrincipalanme;
        private String _staffJobname;
        private String _principalStaffNameEng;

        private SchoolInfo(final Map schoolMst, final Map certifSchoolDat, final Param param) {
            _schoolMst = schoolMst;
            _certifSchoolDat = certifSchoolDat;
        }

        private static SchoolInfo getSchoolInfo(final DB2UDB db2, final Param param, final PrintData printData) {
            //  学校データ
            final String ctrlYear = printData.parameter("CTRL_YEAR");
            final String dateYear = (printData._date != null) ? Util.b_year(printData._date) : ctrlYear;           //対象年度
            final String psKey = "PS_SCHOOL_MST";
            if (null == param.getPs(psKey)) {
                final String sql = schoolInfoSql(param);
                param.setPs(db2, psKey, sql);
            }
            final Object[] sqlparam;
            if (param.isTok()) {
                sqlparam = new Object[] {dateYear};
            } else {
                sqlparam = new Object[] {ctrlYear};
            }
            if (param._isOutputDebugQuery) {
                log.info(" schoolInfo param = " + ArrayUtils.toString(sqlparam));
            }
            final Map schoolMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), sqlparam));

            Map certifSchoolDat = null;
            if (param.isTok()) {
                final String certifKind = printData.parameter("CERTIFKIND");
                final String psKey2 = "PS_SCHOOLINFO" + certifKind;
                if (null == param.getPs(psKey2)) {
                    final String sql = " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = ? AND CERTIF_KINDCD = '" + certifKind + "' ";
                    param.setPs(db2, psKey2, sql);
                }
                certifSchoolDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey2), sqlparam));
            }
            final SchoolInfo schoolInfo = new SchoolInfo(schoolMst, certifSchoolDat, param);

            final String psKey2 = "PS_STAFF";
            final Object[] sqlparam2;
            if (null == param.getPs(psKey2)) {
                final String sql = staffSql(param);
                param.setPs(db2, psKey2, sql);
            }
            if (param.isTok()) {
                sqlparam2 = new Object[] {printData._year};
            } else {
                sqlparam2 = new Object[] {dateYear};
            }
            final Map staffRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey2), sqlparam2));
            if (param.isTok()) {
                schoolInfo._staffPrincipalanme = KnjDbUtils.getString(staffRow, "STAFF_PRINCIPANAME");
                schoolInfo._staffJobname = KnjDbUtils.getString(staffRow, "STAFF_JOBNAME");
            } else {
                schoolInfo._principalStaffNameEng = KnjDbUtils.getString(staffRow, "PRINCIPAL_STAFFNAME_ENG");
            }
            return schoolInfo;
        }

        public static String schoolInfoSql(final Param param) {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            sql.append(" SELECT ");
            sql.append("     SCHM.YEAR ");
            sql.append("     ,SCHM.SCHOOLZIPCD");
            sql.append("     ,SCHM.SCHOOLNAME1");
            sql.append("     ,SCHM.SCHOOLNAME_ENG");
            sql.append("     ,SCHM.SCHOOLADDR1_ENG");
            sql.append("     ,SCHM.SCHOOLADDR2_ENG");
            sql.append("     ,SCHM.SCHOOLTELNO");
            sql.append(" FROM ");
            sql.append("     SCHOOL_MST SCHM ");
            sql.append("    WHERE ");
            sql.append("        SCHM.YEAR = " + q + " ");
            if (param._hasSchoolMstSchoolKind) {
                sql.append(" AND SCHM.SCHOOL_KIND = 'H' ");
            }

            return sql.toString();
        }

        public static String staffSql(final Param param) {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            //  校長
            sql.append("     SELECT ");
            if (param.isTok()) {
                sql.append("     STFM.STAFFNAME AS STAFF_PRINCIPANAME ");
                sql.append("    ,JBM.JOBNAME AS STAFF_JOBNAME ");
            } else {
                sql.append("     STFM.STAFFNAME_ENG AS PRINCIPAL_STAFFNAME_ENG ");
            }
            sql.append("     FROM ");
            sql.append("         STAFF_YDAT STFY ");
            sql.append("         INNER JOIN STAFF_MST STFM ON STFM.STAFFCD = STFY.STAFFCD ");
            sql.append("         LEFT JOIN JOB_MST JBM ON JBM.JOBCD = STFM.JOBCD ");
            sql.append("     WHERE ");
            sql.append("         STFY.YEAR = " + q + " AND ");
            if (param.isTok()) {
                sql.append("         STFM.JOBCD = '0001' ");
            } else {
                sql.append("        (STFM.JOBCD = '0001' OR STFM.JOBCD = '0005') ");
            }

            return sql.toString();
        }
    }

    private static class Form {

        private final Vrw32alp _svf;
        private final Param _param;
        private boolean _hasData;

        private String _formname;
        private int _formMaxGrade;
        private boolean _formAttendRecord;
        private String _principalSignatureImage;
        private String _schoolLogoImage;
        private boolean _usePrincipalSignatureImage;
        private boolean _useSchoolLogoImage;
        private String _certifSchoolstampImagePath;
        private int _recordCountMax;
        private boolean _isPrintStamp;

        protected Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap();

        public Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        public void printSvf(final DB2UDB db2, final PrintData printData, final Param param) {
            setForm(printData, param);
            log.info(" form = " + _formname + ", schregno = " + printData._schregno);

            setSvfForm(_formname, 4);
            printSchoolInfo(db2, printData, param);  // 学校情報印刷
            printHead(printData, param);
            printPersonalInfo(db2, printData, param); //氏名、住所等出力
            printAttend(printData, param);  // 出欠データ印刷
            printRecord(printData, param); //学習の記録出力-->VrEndRecord()はここで！
        }

        private static boolean isForm2(final Param param) {
            return useForm2_3(param) || !param.isTok();
        }

        private boolean isForm2() {
            return isForm2(_param);
        }

        private static boolean useForm2_3(final Param param) {
            return param._z010.in(Z010.rakunan) || param._z010.in(Z010.miyagiken);
        }

        private void setForm(final PrintData printData, final Param param) {
            _formMaxGrade = 3;
            if (param._z010.in(Z010.sapporo)) {
                _formname = "KNJE080_2SAP.frm";
            } else if (useForm2_3(_param)) {
                _formname = "KNJE080_2_3.frm";
                _usePrincipalSignatureImage = true;
            } else if (param._z010.in(Z010.Kindai)) {
                _formname = "KNJE080_2KIN.frm";
                _usePrincipalSignatureImage = true;
            } else if (param._z010.in(Z010.ChiyodaKudan)) {
                _formname = "KNJE080_2KUDAN.frm";
            } else if (param._z010.in(Z010.kaijyo)) {
                _formname = "KNJE080_2KAIJYO.frm";
            } else if (param._z010.in(Z010.meikei)) {
                _formname = "KNJE080_2MEIKEI.frm";
                _usePrincipalSignatureImage = true;
            } else if (param._z010.in(Z010.sakae)) {
                _formname = "KNJE080_2SAKAE.frm";
                _isPrintStamp = true;
            } else if (param._z010.in(Z010.matsudo)) {
                _formname = "KNJE080_2MATSUDO.frm";
                _isPrintStamp = "1".equals(printData.parameter("PRINT_STAMP"));
                _formAttendRecord = true;
            } else if (param._z010.in(Z010.reitaku)) {
                _formname = "KNJE080_2REITAKU.frm";
            } else if (param._z010.in(Z010.jogakkan)) {
                _formname = "KNJE080_2_JOGAKKAN.frm";
                _isPrintStamp = true;
                _usePrincipalSignatureImage = true;
                _useSchoolLogoImage = true;
            } else if (param._z010.in(Z010.jyoto)) {
                if (printData._isPrintJp) {
                    _formname = "KNJE080_2_JYOTO_JP.frm";
                } else {
                    _formname = "KNJE080_2_JYOTO.frm";
                }
            } else {
                if (param._z010.in(Z010.osakatoin)) {
                    _usePrincipalSignatureImage = true;
                }
                String FORM1 = "KNJE080_2.frm";
                String FORM2 = "KNJE080_2_2.frm";

                _isPrintStamp = "1".equals(printData.parameter("PRINT_STAMP"));
                if (printData._maxgrade > 3) {
                    _formname = FORM2;
                    _formMaxGrade = 4;
                } else {
                    _formname = FORM1;
                }
            }
            final String documentroot = printData.parameter("DOCUMENTROOT");
            if (_usePrincipalSignatureImage) {
                _principalSignatureImage = param.getImagePath(documentroot, "PRINCIPAL_SIGNATURE_H", "jpg");
            }
            if (_useSchoolLogoImage) {
                _schoolLogoImage = param.getImagePath(documentroot, "SCHOOLLOGO_H", "jpg");
            }
            if (_isPrintStamp) {
                _certifSchoolstampImagePath = param.getImagePath(documentroot, "CERTIF_SCHOOLSTAMP_H", "bmp");
                if (null == _certifSchoolstampImagePath) {
                    _certifSchoolstampImagePath = param.getImagePath(documentroot, "SCHOOLSTAMP_H", "bmp");
                }
            }
            setModifyForm(printData, param);
        }

        private void setModifyForm(final PrintData printData, final Param param) {

            File formfile = new File(_svf.getPath(_formname));
            if (!formfile.exists()) {
                throw new IllegalArgumentException("no form file : " + _formname);
            }
            final SvfForm svfForm = new SvfForm(formfile);
            boolean readFile = svfForm.readFile();
            if (readFile) {
                final Map<String, Integer> subFormHeights = new HashMap<String, Integer>();
                for (final SvfForm.SubForm subForm : svfForm.getElementList(SvfForm.SubForm.class)) {
                    subFormHeights.put(subForm._name, subForm.getHeight());
                }
                final Map<String, Integer> recordHeights = new TreeMap<String, Integer>();
                for (final SvfForm.Record record : svfForm.getElementList(SvfForm.Record.class)) {
                    recordHeights.put(record._name, record.getHeight());
                }
                if (subFormHeights.size() > 0 && recordHeights.size() > 0) {
                    _recordCountMax = Util.max(subFormHeights.values()) / Util.max(recordHeights.values());
                }
                if (_param._isOutputDebug) {
                    log.info(" subFormHeights = " + subFormHeights + ", recordHeights = " + recordHeights + ", recordCountMax = " + _recordCountMax);
                }
            }

            final TreeMap<String, String> modifyFlgMap = getModifyFlgMap(printData, param);
            String modifyFlg = Util.mkString(modifyFlgMap, "|");
            if (StringUtils.isEmpty(modifyFlg)) {
                return;
            }
            modifyFlg = _formname + modifyFlg;

            if (readFile) {
                final Map<String, Integer> subFormHeights = new HashMap<String, Integer>();
                for (final SvfForm.SubForm subForm : svfForm.getElementList(SvfForm.SubForm.class)) {
                    subFormHeights.put(subForm._name, subForm.getHeight());
                }
                final Map<String, Integer> recordHeights = new TreeMap<String, Integer>();
                for (final SvfForm.Record record : svfForm.getElementList(SvfForm.Record.class)) {
                    recordHeights.put(record._name, record.getHeight());
                }
                if (subFormHeights.size() > 0 && recordHeights.size() > 0) {
                    _recordCountMax = Util.max(subFormHeights.values()) / Util.max(recordHeights.values());
                }
                if (_param._isOutputDebug) {
                    log.info(" subFormHeights = " + subFormHeights + ", recordHeights = " + recordHeights + ", recordCountMax = " + _recordCountMax);
                }

                try {
                    boolean modified = modifySvfForm(modifyFlgMap, svfForm);
                    if (modified) {
                        final File newFormFile = svfForm.writeTempFile();
                        _param._createdFiles.put(modifyFlg, newFormFile);
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }

            if (_param._createdFiles.containsKey(modifyFlg)) {
                _formname = _param._createdFiles.get(modifyFlg).getName();
            }
        }

        final String MEIKEI_TENGAKU = "MEIKEI_TENGAKU";
        final String JOGAKKAN_LINE = "JOGAKKAN_LINE";
        final String RYUKEI_HEADER = "RYUKEI_HEADER";
        private TreeMap<String, String> getModifyFlgMap(final PrintData printData, final Param param) {
            final TreeMap<String, String> modifyFlgMap = new TreeMap<String, String>();
            if (param._z010.in(Z010.meikei)) {
                if (null != printData._grdDiv && !"3".equals(printData._grdDiv)) {
                    modifyFlgMap.put(MEIKEI_TENGAKU, "1");
                }
            } else if (param._z010.in(Z010.jogakkan)) {
                final List<Map> clazzList = getClazzList(printData._studyrecListAll);
                int rnum = 0;               //出力件数
                final List<String> lineCounts = new ArrayList<String>();
                for (final Map clazz : clazzList) {
                    final int studyrecListCount = Util.getMappedList(clazz, "STUDYREC_LIST").size();
                    rnum += studyrecListCount;
                    lineCounts.add(String.valueOf(rnum));
                }
                modifyFlgMap.put(JOGAKKAN_LINE, Util.mkString(lineCounts, ","));
            } else if (param._z010.in(Z010.ryukei)) {
                modifyFlgMap.put(RYUKEI_HEADER, "1");
            }
            return modifyFlgMap;
        }

        private boolean modifySvfForm(final TreeMap<String, String> modifyFlgMap, final SvfForm svfForm) {
            boolean modified = false;
            if (modifyFlgMap.containsKey(MEIKEI_TENGAKU)) {
                svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THIN, new SvfForm.Point(340, 675), new SvfForm.Point(850, 675)));
                modified = true;
            }
            if (modifyFlgMap.containsKey(JOGAKKAN_LINE)) {
                final List<String> lineCounts = Arrays.asList(modifyFlgMap.get(JOGAKKAN_LINE).split(","));
                final SvfForm.SubForm subForm = svfForm.getSubForm("SUBFORM1");
                final SvfForm.Record record1 = svfForm.getRecord("RECORD1");
                for (final String lineCount : lineCounts) {
                    final int ilineY = subForm._point1._y + record1.getHeight() * Integer.parseInt(lineCount);
                    svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THIN, new SvfForm.Point(record1.getAbsPoint1()._x, ilineY), new SvfForm.Point(record1.getAbsPoint2()._x, ilineY)).setFline(true));
                }
                modified = true;
            }
            if (modifyFlgMap.containsKey(RYUKEI_HEADER)) {
                final int x = new BigDecimal(Util.mmToDot("12.0")).subtract(svfForm.getAdjtX()).intValue(); // 左端から12mm
                final int y = (new BigDecimal(Util.mmToDot("297.0")).subtract(new BigDecimal(Util.mmToDot("35.0")))).subtract(svfForm.getAdjtY()).intValue() - 530; // 下端から35mm
                final int width = 160;
                final int charPoint9p = 100;
                final SvfForm.Font font = SvfForm.Font.Mincho;

                final SvfForm.Field.RepeatConfig rc = new SvfForm.Field.RepeatConfig("101", 20, 1, -1, 0).setRepeatPitchPoint(1.6);
                for (final SvfForm.Field f : Arrays.asList(
                        new SvfForm.Field(null, "HEADER_SCHREGNO", font, 8, x + width, false, new SvfForm.Point(x, y), charPoint9p, "ヘッダ学籍番号").setDegree(90).setRepeatConfig(rc)
                      , new SvfForm.Field(null, "HEADER_NAME", font, 30, x + 70 + width, false, new SvfForm.Point(x + 70, y - 50), charPoint9p, "ヘッダ氏名").setDegree(90).setRepeatConfig(rc)
                      , new SvfForm.Field(null, "HEADER_DATE", font, 16, x + 70 * 2 + width, false, new SvfForm.Point(x + 70 * 2, y), charPoint9p, "ヘッダ日付").setDegree(90).setRepeatConfig(rc)
                        )) {
                    svfForm.addField(f);
                }
                modified = true;
            }
            return modified;
        }

        private void setSvfForm(final String form, int n) {
            _svf.VrSetForm(form, n);
            if (!_formFieldInfoMap.containsKey(form)) {
                _formFieldInfoMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        private SvfField getField(final String fieldname) {
            if (null == fieldname) {
                return null;
            }
            final SvfField field = Util.getMappedMap(_formFieldInfoMap, _formname).get(fieldname);
            return field;
        }

        private int VrAttribute(final String field, final String attribute) {
            if (null == getField(field)) {
                if (_param._isOutputDebugField) {
                    log.warn("no field : " + field + " (" + attribute + ")");
                }
                return -1;
            }
            if (_param._isOutputDebugField) {
                log.warn("VrAttribute(" + field + ", " + attribute + ")");
            }
            return _svf.VrAttribute(field, attribute);
        }

        private int VrsOutFieldSelect(final String[] fields, final String data) {
            return VrsOut(getFieldForDataKeta(fields, data), data);
        }

        private int VrsOut(final String field, final String data) {
            if (null == getField(field)) {
                if (_param._isOutputDebugField) {
                    log.warn("no field : " + field + " (" + data + ")");
                }
                return -1;
            }
            if (_param._isOutputDebugField) {
                log.warn("VrsOut(" + field + ", " + data + ")");
            }
            return _svf.VrsOut(field, data);
        }

        private int VrsOutn(final String field, final int gyo, final String data) {
            if (null == getField(field)) {
                if (_param._isOutputDebugField) {
                    log.warn("no field : " + field + ", gyo = " + gyo + " (" + data + ")");
                }
                return -1;
            }
            if (_param._isOutputDebugField) {
                log.warn("VrsOutn(" + field + ", " + gyo + ", " + data + ")");
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        private int VrEndRecord() {
            if (_param._isOutputDebugField) {
                log.warn("VrEndrecord");
            }
            return _svf.VrEndRecord();
        }

        private void printHead(final PrintData printData, final Param param) {
            if (isForm2()) {
                for (int j = 1; j <= 2; j++) {
                    for (int i = 1; i <= _formMaxGrade; i++) {
                        final String fieldname = "GRADE" + j + "_" + i;
                        final String gradeNum;
                        if (param._z010.in(Z010.Kindai)) {
                            gradeNum = String.valueOf(i + 9);
                        } else if (param._z010.in(Z010.rakunan)) {
                            final SvfField field = getField(fieldname);
                            if (null != field && StringUtils.isBlank((String) field.getAttributeMap().get(SvfField.AttributeEdit))) { // 編集式がnullなら「st」「nd」「rd」「th」追加
                                gradeNum = String.valueOf(i) + getNumSuffix(i);
                            } else {
                                gradeNum = String.valueOf(i);
                            }
                        } else {
                            gradeNum = String.valueOf(i) + getNumSuffix(i);
                        }
                        VrsOut(fieldname, gradeNum); // 学年見出し
                    }
                }
            }
            if (param._z010.in(Z010.sapporo)) {
                if (CERTIF_KINDCD_MIKOMI.equals(printData.parameter("CERTIFKIND"))) {
                    VrsOut("TITLE", "Statement of Expected Results (Higher Secondary)");
                } else if (CERTIF_KINDCD.equals(printData.parameter("CERTIFKIND"))) {
                    VrsOut("TITLE", "Certificate of Results (Higher Secondary)");
                }
                VrsOut("CERT_HEADER", "I certify that the above details are correct.");
            }
            if (param._z010.in(Z010.ChiyodaKudan)) {
                VrsOut("GRADE1",   "10th");
                VrsOut("GRADE1_2", "10th");
                VrsOut("GRADE2",   "11th");
                VrsOut("GRADE2_2", "11th");
                VrsOut("GRADE3",   "12th");
                VrsOut("GRADE3_2", "12th");

                for (final String annual : printData._annualYearMap.keySet()) {
                    final Map<String, String> yearMap = printData._annualYearMap.get(annual);
                    final int pos = printData.getAnnualPosition(param, annual);
                    final String minYear = yearMap.get("MIN_YEAR");
                    final String maxYear = yearMap.get("MAX_YEAR");
                    VrsOut("YEAR_FROM_TO" + String.valueOf(pos), defstr(minYear) + "～" + (NumberUtils.isDigits(maxYear) ? String.valueOf(Integer.parseInt(maxYear) + 1) : ""));
                }
            }
            if (param._z010.in(Z010.jogakkan)) {
                VrsOut("PHONE", KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "REMARK8"));
                VrsOut("FAX", KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "REMARK9"));
            }
        }

        private String getFieldForDataKeta(final String[] fields, final String data) {
            final int keta = getMS932ByteLength(data);
            String existMax = null; // どのフィールドにも収まらない場合最大桁数のフィールドを使用する
            for (int i = 0; i < fields.length; i++) {
                final SvfField field = getField(fields[i]);
                if (null == field) {
                    continue;
                }
                existMax = fields[i];
                if (keta <= field._fieldLength) {
                    return fields[i];
                }
            }
            if (_param._isOutputDebug) {
                log.info(" use existMax \"" + existMax + "\" / fields = " + ArrayUtils.toString(fields) + ", data = \"" + data + "\"");
            }
            return existMax;
        }

        private String[] getFieldGroupForDataKeta(final String[][] fieldGroups, final String data) {
            final int keta = getMS932ByteLength(data);
            String[] existMax = null; // どのフィールドにも収まらない場合最大桁数のフィールドを使用する
            for (int i = 0; i < fieldGroups.length; i++) {
                int fieldLength = 0;
                for (int j = 0; j < fieldGroups[i].length; j++) {
                    final SvfField field = getField(fieldGroups[i][j]);
                    if (null == field) {
                        continue;
                    }
                    fieldLength += field._fieldLength;
                }
                existMax = fieldGroups[i];
                if (keta <= fieldLength) {
                    return fieldGroups[i];
                }
            }
            if (_param._isOutputDebug) {
                log.info(" use existMax " + ArrayUtils.toString(existMax)  + " / fields = " + ArrayUtils.toString(fieldGroups) + ", data = \"" + data + "\"");
            }
            return existMax;
        }

        /**
         *  学習の記録
         */
        private void printRecord(final PrintData printData, final Param param) {
            if (isForm2()) {
                printRecordForm2(printData, param);
            } else {

                _hasData = false;

                final List<Studyrec> studyrecListAll = printData._studyrecListAll;
                if (param._isOutputDebug) {
                    log.info(" studyrecList size = " + studyrecListAll.size());
                }

                if (param._z010.in(Z010.kaijyo)) {

                    //学年見出し
                    VrsOut("GRADE1", "1st Year");
                    VrsOut("GRADE2", "2nd Year");
                    VrsOut("GRADE3", "3rd Year");

                    final List<Map> clazzList = getClazzList(studyrecListAll);

                    int rnum = 0;               //出力件数
                    for (int cli = 0; cli < clazzList.size(); cli++) {
                        final Map clazz = clazzList.get(cli);
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");
                        final int classnameLine = studyrecList.size();
                        final int classnamefieldketa = 30;
                        final String[] classnameArrCentering = setArrayCentering(splitBySizeWithSpace(KnjDbUtils.getString(clazz, "CLASSNAME"), new int[] {classnamefieldketa}), classnameLine);
                        int classnameArrCenteringNotNull = 0;
                        for (int i = 0; i < classnameArrCentering.length; i++) {
                            classnameArrCenteringNotNull += StringUtils.isBlank(classnameArrCentering[i]) ? 0 : 1;
                        }

                        int modifyY = 0;
                        final int yStart = 1095;
                        final int recordHeight = 59;
                        if (Math.abs(studyrecList.size() - classnameArrCenteringNotNull) % 2 == 1) {
                            // 印字位置Yを教科中央に調整(行の高さ半分)
                            modifyY = recordHeight / 2;
                        }

                        for (int sti = 0; sti < studyrecList.size(); sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", credit = " + s._credit + ", subclassname = " + s._subclassname);
                            }
                            final String suffix = sti == 0 ? "" : "_2";

                            VrsOut("SUBJECT_CLASS" + suffix, classnameArrCentering[sti]); //科目名
                            if (modifyY != 0) {
                                _svf.VrAttribute("SUBJECT_CLASS" + suffix, "Y=" + String.valueOf(yStart + recordHeight * rnum + modifyY));
                            }

                            //log.info(" subclassnameField = " + subclassnameField);
                            VrsOutFieldSelect(new String[] {"SUBJECT" + suffix, "SUBJECT2" + suffix, "SUBJECT3" + suffix}, s._subclassname); //科目名

                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("GRADING" + h._annualPosition + suffix, param.getRepValue(h._grades)); //評定
                                    }
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition + suffix, h._gradeCredit); //単位
                                    }
                                }
                            }
                            VrEndRecord();    //最後のレコード出力
                            rnum++;
                        }
                    }
                    final int max = 36;
                    for (; rnum < max; rnum++) {
                        VrsOut("SUBJECT_CLASS", String.valueOf(rnum)); //科目名
                        _svf.VrAttribute("SUBJECT_CLASS", "X=10000");
                        VrEndRecord();
                    }

                    _hasData = true;

                } else if (param._z010.in(Z010.ChiyodaKudan)) {
                    String classnameEng90 = null;
//        		if (null != printData._studyrec90MaxSubclasscd) {
//        			String classKey = null;
//            		if ("1".equals(_param._useCurriculumcd)) {
//            			final String[] split = StringUtils.split(printData._studyrec90MaxSubclasscd, "-");
//            			if (split.length >= 4) {
//            				classKey = split[0] + "-" + split[1];
//            			}
//            		} else {
//            			if (printData._studyrec90MaxSubclasscd.length() >= 2) {
//            				classKey = printData._studyrec90MaxSubclasscd.substring(0, 2);
//            			}
//            		}
//    				classnameEng90 = _param._classKeyClassnameEng90Map.get(classKey);
//        			if (null == classnameEng90) {
//        				log.warn(" no name for " + classKey + " in " + _param._classKeyClassnameEng90Map.keySet());
//        			}
//        		}
                    if (null == classnameEng90) {
                        classnameEng90 = "Integrated Study";
                    }
                    final String subclassnameEng90 = param.sogoSubclassname(printData._studyrec90MaxSubclasscd);

                    final List<Map> clazzList = getClazzList(studyrecListAll);

                    int rnum = 0;               //出力件数
                    for (final Map clazz : clazzList) {
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");
                        final int classnameLine = studyrecList.size();
                        final int classnamefieldketa = classnameLine == 1 ? 26 : 18;
                        final String[] classnameArrCentering = setArrayCentering(splitBySizeWithSpace(KnjDbUtils.getString(clazz, "CLASSNAME"), new int[] {classnamefieldketa}), classnameLine);

                        for (int sti = 0; sti < studyrecList.size(); sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", annual = " + s._credit + ", subclassname = " + s._subclassname);
                            }
                            VrsOut("CLASSCD", s._classcd);      //教科コード

                            VrsOut("SUBJECT_CLASS", classnameArrCentering[sti]); //科目名

                            VrsOutFieldSelect(new String[] {"SUBJECT", "SUBJECT2", "SUBJECT3"}, s._subclassname); //科目名

                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("GRADING" + h._annualPosition, param.getRepValue(h._grades)); //評定
                                    }
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition, h._gradeCredit); //単位
                                    }
                                }
                            }
                            VrEndRecord();    //最後のレコード出力
                            rnum++;
                        }
                    }
                    final int max = 46;
                    for (; rnum < max; rnum++) {
                        VrsOut("CLASSCD", String.valueOf(rnum));
                        VrEndRecord();
                    }
                    final List<Studyrec> sogoAbroadTotal = new ArrayList<Studyrec>();
                    sogoAbroadTotal.add(printData.studyrecSogo);
                    sogoAbroadTotal.add(printData.studyrecAbroad);
                    sogoAbroadTotal.add(printData.studyrecTotal);
                    for (final Studyrec s : sogoAbroadTotal) {
                        String fieldSoeji = "";
                        if (printData.studyrecSogo == s) {
                            fieldSoeji = "2";
                            VrsOut("SUBJECT_CLASS_" + fieldSoeji, classnameEng90);
                            VrsOutFieldSelect(new String[] {"SUBJECT_" + fieldSoeji, "SUBJECT2_" + fieldSoeji, "SUBJECT3_" + fieldSoeji}, subclassnameEng90);
                        } else if (printData.studyrecAbroad == s) {
                            fieldSoeji = "3";
                            VrsOut("SUBJECT_CLASS_" + fieldSoeji, "Study Abroad");
                        } else if (printData.studyrecTotal == s) {
                            fieldSoeji = "3";
                            VrsOut("SUBJECT_CLASS_" + fieldSoeji, "Total");
                        }
                        for (final Hyotei h : s._hyoteiList) {

                            if (printData.studyrecTotal == s) {
                                if (0 < h._annualPosition  &&  h._annualPosition <= _formMaxGrade) {
                                    final Integer cred = printData._totalcredits.get(h._annualPosition);
                                    VrsOut("CREDIT" + h._annualPosition + "_" + fieldSoeji, null == cred ? "0" : cred.toString()); //単位
                                }
                            } else {
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    VrsOut("CREDIT" + h._annualPosition + "_" + fieldSoeji, h._gradeCredit); //単位
                                }
                            }
                        }
                        VrEndRecord();
                    }

                    _hasData = true;
                } else if (param._z010.in(Z010.sakae)) {
                    final String subclassnameEng90 = param.sogoSubclassname(printData._studyrec90MaxSubclasscd);
                    final String subclassnameJp90 = param.sogoSubclassnameJp(printData._studyrec90MaxSubclasscd, printData);

                    final List<Map> clazzList = getClazzList(studyrecListAll);
                    final int classnamefieldketa = getField("SUBJECT_CLASS")._fieldLength;

                    int rnum = 0;               //出力件数
                    for (final Map clazz : clazzList) {
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");
                        final String classNameJp = KnjDbUtils.getString(clazz, "CLASSNAME_JP");
                        final String classnameJpfieldname = getFieldForDataKeta(new String[] {"SUBJECT_CLASS_JP", "SUBJECT_CLASS_JP2", "SUBJECT_CLASS_JP3"}, kakko(classNameJp, -1));
                        final String[] classnameArrCentering = setArrayCentering(splitBySizeWithSpace(KnjDbUtils.getString(clazz, "CLASSNAME"), new int[] {classnamefieldketa}), studyrecList.size());
                        final String[] classnameJpArrCentering = setArrayCentering(get_token(kakko(classNameJp, getField(classnameJpfieldname)._fieldLength), getField(classnameJpfieldname)._fieldLength), studyrecList.size());

                        for (int sti = 0; sti < studyrecList.size(); sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", annual = " + s._credit + ", subclassname = " + s._subclassname);
                            }
                            VrsOut("CLASSCD", s._classcd);      //教科コード

                            VrsOut("SUBJECT_CLASS", classnameArrCentering[sti]); //科目名
                            VrsOut(classnameJpfieldname, classnameJpArrCentering[sti]); //科目名

                            final String[] subclassnameField = getFieldGroupForDataKeta(new String[][] {{"SUBJECT"}, {"SUBJECT2"}, {"SUBJECT3_1", "SUBJECT3_2"}}, s._subclassname);
                            if (param._isOutputDebug) {
                                log.info(" subclassnameField = " + ArrayUtils.toString(subclassnameField));
                            }
                            final String[] token = splitBySizeWithSpace(s._subclassname, new int[] {getField(subclassnameField[0])._fieldLength});
                            for (int i = 0; i < token.length; i++) {
                                VrsOut(subclassnameField[i], token[i]); //科目名
                            }
                            final String subclassnameJpField = getFieldForDataKeta(new String[] {"SUBJECT_JP", "SUBJECT_JP2", "SUBJECT_JP3"}, kakko(s._subclassnameJp, -1));
                            VrsOut(subclassnameJpField, kakko(s._subclassnameJp, getField(subclassnameJpField)._fieldLength)); //科目名

                            for (int annual = 1; annual <= 3; annual++) {
                                VrsOut("GRADING" + String.valueOf(annual), "※");
                            }
                            String credit = null;
                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    VrsOut("GRADING" + h._annualPosition, (h._grades == null || Integer.parseInt(h._grades) == 0) ? "※" : h._grades); //評定
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        credit = String.valueOf(Integer.parseInt(h._gradeCredit) + (null == credit ? 0 : Integer.parseInt(credit)));
                                    }
                                }
                            }
                            VrsOut("CREDIT", null == credit ? "※" : credit); // 単位数
                            VrEndRecord();
                            rnum++;
                        }
                    }
                    final List<Studyrec> sogoAbroadTotal = new ArrayList<Studyrec>();
                    sogoAbroadTotal.add(printData.studyrecSogo);
                    final int maxLine = 31;
                    if (rnum > maxLine) {
                        rnum = rnum % maxLine;
                    }
                    final int blankLine = maxLine - sogoAbroadTotal.size();
                    for (int max = rnum < blankLine ? blankLine : rnum / blankLine + (rnum % blankLine == 0 ? 0 : 1); rnum < max; rnum++) {
                        VrsOut("CLASSCD", String.valueOf(rnum));
                        VrEndRecord();
                    }
                    for (int i = 0; i < sogoAbroadTotal.size(); i++) {
                        final Studyrec s = sogoAbroadTotal.get(i);
                        if (printData.studyrecSogo == s) {
                            VrsOut("SUBJECT_90", subclassnameEng90);
                            VrsOut("SUBJECT_JP_90", kakko(subclassnameJp90, -1));
                        }
                        String credit = null;
                        for (final Hyotei h : s._hyoteiList) {
                            VrsOut("GRADING" + h._annualPosition + "_90", (h._grades == null || Integer.parseInt(h._grades) == 0) ? "" : h._grades); //評定
                            if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                credit = String.valueOf(Integer.parseInt(h._gradeCredit) + (null == credit ? 0 : Integer.parseInt(credit)));
                            }
                        }
                        VrsOut("CREDIT_90", null == credit ? "※" : credit); // 単位数
                        VrEndRecord();
                    }

                    _hasData = true;

                } else if (param._z010.in(Z010.meikei)) {

                    //学年見出し

                    VrsOut("YEAR_FROM_TO1", "1st Year");
                    VrsOut("YEAR_FROM_TO2", "2nd Year");
                    VrsOut("YEAR_FROM_TO3", "3rd Year");

                    final List<Map> clazzList = getClazzList(studyrecListAll);

                    final Map<Integer, String> creditTotalMap = new TreeMap<Integer, String>();
                    int rnum = 0;               //出力件数
                    for (final Map clazz : clazzList) {
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");

                        for (int sti = 0; sti < studyrecList.size(); sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", annual = " + s._credit + ", subclassname = " + s._subclassname);
                            }

                            final String suffix = sti == 0 ? "" : "_3";
                            VrsOutFieldSelect(new String[] {"SUBJECT" + suffix, "SUBJECT2" + suffix, "SUBJECT3" + suffix}, s._subclassname); // 科目名

                            String subclassCreditTotal = null;
                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition + suffix, param.getRepValue(h._grades)); //評定
                                    }
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        subclassCreditTotal = Util.add(subclassCreditTotal, h._gradeCredit);
                                        final Integer iAnnual = new Integer(h._annualPosition);
                                        creditTotalMap.put(iAnnual, Util.add(creditTotalMap.get(iAnnual), h._gradeCredit));
                                    }
                                }
                            }
                            if (null != subclassCreditTotal) {
                                VrsOut("CREDIT9" + suffix, subclassCreditTotal.toString()); //  単位
                            }
                            VrEndRecord();    //最後のレコード出力
                            rnum++;
                        }
                    }

                    final List<Studyrec> sogoAbroadTotal = new ArrayList<Studyrec>();
                    sogoAbroadTotal.add(printData.studyrecAbroad);
                    sogoAbroadTotal.add(printData.studyrecSogo);
                    for (final Iterator<Studyrec> it = sogoAbroadTotal.iterator(); it.hasNext();) {
                        final Studyrec s = it.next();
                        if (s._hyoteiList.isEmpty()) {
                            it.remove();
                        }
                    }
                    final int max = 50;
                    for (; rnum < max - sogoAbroadTotal.size() - 1; rnum++) {
                        VrsOut("SUBJECT", String.valueOf(rnum)); //科目名
                        _svf.VrAttribute("SUBJECT", "X=10000");
                        VrEndRecord();
                    }

                    for (int i = 0; i < sogoAbroadTotal.size(); i++) {
                        final Studyrec s = sogoAbroadTotal.get(i);
                        if (null == s) {
                            continue;
                        }
                        String title = null;
                        if (printData.studyrecSogo == s) {
                            title = defstr(param._e021name1, Period_for_Integrated_Study);
                        } else if (printData.studyrecAbroad == s) {
                            title = "Credits Transferred from Overseas Schools";
                        }

                        final String suffix = "_3";
                        final String field = getFieldForDataKeta(new String[] {"SUBJECT" + suffix, "SUBJECT2" + suffix, "SUBJECT3" + suffix}, title);
                        VrsOut(field, title);  //教科名
                        VrAttribute(field, "Hensyu=3");  //センタリング

                        String subclassCreditTotal = null;
                        for (final Hyotei h : s._hyoteiList) {
                            //明細出力
                            if (h._annualPosition > 0) {
                                if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                    VrsOut("CREDIT" + h._annualPosition + suffix, param.getRepValue(h._grades)); //評定
                                }
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    subclassCreditTotal = Util.add(subclassCreditTotal, h._gradeCredit);
                                    final Integer iAnnual = new Integer(h._annualPosition);
                                    creditTotalMap.put(iAnnual, Util.add(creditTotalMap.get(iAnnual), h._gradeCredit));
                                }
                            }
                        }
                        if (null != subclassCreditTotal) {
                            VrsOut("CREDIT9" + suffix, subclassCreditTotal.toString()); // 単位
                        }
                        VrEndRecord();
                    }

                    String creditTotal = null;
                    VrsOut("TOTAL_NAME", "TOTAL"); //科目名
                    for (final String annualCredit : creditTotalMap.values()) {
                        creditTotal = Util.add(creditTotal, annualCredit);
                    }
                    if (null != creditTotal) {
                        VrsOut("CREDIT9", creditTotal); // 評定
                    }
                    VrEndRecord();

                    _hasData = true;

                } else if (param._z010.in(Z010.reitaku)) {

                    final List<Map> clazzList = getClazzList(studyrecListAll);

                    final Map<Integer, String> creditTotalMap = new TreeMap<Integer, String>();
                    int rnum = 0;               //出力件数
                    final int subclassnameSplitKeta = 42;
                    for (final Map clazz : clazzList) {
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");

                        for (int sti = 0; sti < studyrecList.size(); sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", annual = " + s._credit + ", subclassname = " + s._subclassname);
                            }

                            final int dan;
                            final String suffix;
                            if (getMS932ByteLength(s._subclassname) >= subclassnameSplitKeta) {
                                final String[] splits = splitBySizeWithSpace(s._subclassname, new int[] {subclassnameSplitKeta});
                                suffix = (sti == 0 ? "" : "_3") + "_2DAN";
                                for (int i = 0; i < splits.length; i++) {
                                    VrsOutn("SUBJECT" + suffix, i + 1, splits[i]);
                                }
                                dan = 2;
                            } else {
                                suffix = sti == 0 ? "" : "_3";
                                VrsOut("SUBJECT" + suffix, s._subclassname); // 科目名
                                dan = 1;
                            }

                            String subclassCreditTotal = null;
                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition + suffix, param.getRepValue(h._grades)); //評定
                                    }
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        subclassCreditTotal = Util.add(subclassCreditTotal, h._gradeCredit);
                                        final Integer iAnnual = new Integer(h._annualPosition);
                                        creditTotalMap.put(iAnnual, Util.add(creditTotalMap.get(iAnnual), h._gradeCredit));
                                    }
                                }
                            }
                            if (null != subclassCreditTotal) {
                                VrsOut("CREDIT9" + suffix, subclassCreditTotal.toString()); //  単位
                            }
                            VrEndRecord();    //最後のレコード出力
                            rnum += dan;
                        }
                    }

                    final List<Studyrec> sogoAbroad = new ArrayList<Studyrec>();
                    sogoAbroad.add(printData.studyrecAbroad);
                    sogoAbroad.add(printData.studyrecSogo);
                    for (final Iterator<Studyrec> it = sogoAbroad.iterator(); it.hasNext();) {
                        final Studyrec s = it.next();
                        if (s._hyoteiList.isEmpty()) {
                            it.remove();
                        }
                    }
                    final int max = 33;
                    for (; rnum < max - sogoAbroad.size() - 1; rnum++) {
                        VrsOut("SUBJECT", String.valueOf(rnum)); //科目名
                        _svf.VrAttribute("SUBJECT", "X=10000");
                        VrEndRecord();
                    }

                    for (int i = 0; i < sogoAbroad.size(); i++) {
                        final Studyrec s = sogoAbroad.get(i);
                        if (null == s) {
                            continue;
                        }
                        String title = null;
                        if (printData.studyrecSogo == s) {
                            title = defstr(param._e021name1, "General Studies");
                        } else if (printData.studyrecAbroad == s) {
                            title = "Credits Transferred from Overseas Schools";
                        }

                        final String suffix;
                        if (getMS932ByteLength(title) >= subclassnameSplitKeta) {
                            final String[] splits = splitBySizeWithSpace(title, new int[] {subclassnameSplitKeta});
                            suffix = ("_3") + "_2DAN";
                            for (int ni = 0; ni < splits.length; ni++) {
                                VrsOutn("SUBJECT" + suffix, ni + 1, splits[ni]);
                            }
                        } else {
                            suffix = "_3";
                            final String field = "SUBJECT" + suffix;
                            VrsOut(field, title); // 科目名
                        }

                        String subclassCreditTotal = null;
                        for (final Hyotei h : s._hyoteiList) {
                            //明細出力
                            if (h._annualPosition > 0) {
                                if (printData.studyrecSogo == s) {
                                    if (NumberUtils.isNumber(h._gradeCredit)) {
                                        if (Double.parseDouble(h._gradeCredit) > 0) {
                                            VrsOut("CREDIT" + h._annualPosition + suffix, "S"); //評定
                                        } else {
                                            VrsOut("CREDIT" + h._annualPosition + suffix, "F"); //評定
                                        }
                                    }
                                } else {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition + suffix, param.getRepValue(h._grades)); //評定
                                    }
                                }
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    subclassCreditTotal = Util.add(subclassCreditTotal, h._gradeCredit);
                                    final Integer iAnnual = new Integer(h._annualPosition);
                                    creditTotalMap.put(iAnnual, Util.add(creditTotalMap.get(iAnnual), h._gradeCredit));
                                }
                            }
                        }
                        if (null != subclassCreditTotal) {
                            VrsOut("CREDIT9" + suffix, subclassCreditTotal.toString()); // 単位
                        }
                        VrEndRecord();
                    }

                    String creditTotal = null;
                    VrsOut("SUBJECT_TOTAL", "Total Credits"); //科目名
                    VrAttribute("SUBJECT_TOTAL", "Hensyu=3");  //センタリング
                    for (final String annualCredit : creditTotalMap.values()) {
                        creditTotal = Util.add(creditTotal, annualCredit);
                    }
                    if (null != creditTotal) {
                        VrsOut("CREDIT9_TOTAL", creditTotal); // 評定
                    }
                    VrEndRecord();

                    _hasData = true;

                } else if (param._z010.in(Z010.jogakkan)) {

                    //学年見出し
                    VrsOut("GRADE1", "1st Year");
                    VrsOut("GRADE2", "2nd Year");
                    VrsOut("GRADE3", "3rd Year");

                    final List<Map> clazzList = getClazzList(studyrecListAll);
                    int rnum = 0;               //出力件数
                    for (final Map clazz : clazzList) {
                        final List<Studyrec> studyrecList = Util.getMappedList(clazz, "STUDYREC_LIST");

                        for (int sti = 0, max = studyrecList.size(); sti < max; sti++) {
                            final Studyrec s = studyrecList.get(sti);
                            if (param._isOutputDebug) {
                                log.info("subclasscd = " + s._subclasscd +", credit = " + s._credit + ", subclassname = " + s._subclassname);
                            }

                            //log.info(" subclassnameField = " + subclassnameField);
                            VrsOutFieldSelect(new String[] {"SUBJECT", "SUBJECT2", "SUBJECT3"}, s._subclassname); //科目名

                            for (final Hyotei h : s._hyoteiList) {
                                //明細出力
                                if (h._annualPosition > 0) {
                                    if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                        VrsOut("GRADING" + h._annualPosition, param.getRepValue(h._grades)); //評定
                                    }
                                    if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                        VrsOut("CREDIT" + h._annualPosition, h._gradeCredit); //単位
                                    }
                                }
                            }
                            VrEndRecord();    //最後のレコード出力
                            rnum++;
                        }
                    }

                    List<Studyrec> sogoAbroadTotal = new ArrayList<Studyrec>();
                    sogoAbroadTotal.add(printData.studyrecSogo);
                    if (null != printData.studyrecAbroad && NumberUtils.isDigits(printData.studyrecAbroad._credit)) {
                        sogoAbroadTotal.add(printData.studyrecAbroad);
                    }
                    sogoAbroadTotal.add(printData.studyrecTotal);

                    for (final int max = _recordCountMax - sogoAbroadTotal.size(); rnum < max; rnum++) {
                        VrsOut("SUBJECT", String.valueOf(rnum)); //科目名
                        _svf.VrAttribute("SUBJECT", "X=10000");
                        VrEndRecord();
                    }
                    if (0 <= _recordCountMax - rnum && _recordCountMax - rnum < sogoAbroadTotal.size()) {
                        sogoAbroadTotal = sogoAbroadTotal.subList(0, _recordCountMax - rnum);
                    }

                    for (final Studyrec s : sogoAbroadTotal) {
                        String fieldSoeji = "2";
                        if (printData.studyrecSogo == s) {
                            VrsOutFieldSelect(new String[] {"SUBJECT_" + fieldSoeji, "SUBJECT2_" + fieldSoeji, "SUBJECT3_" + fieldSoeji}, "Integrated Studies");
                        } else if (printData.studyrecAbroad == s) {
                            VrsOut("SUBJECT_" + fieldSoeji, "Study Abroad");
                        } else if (printData.studyrecTotal == s) {
                            VrsOut("SUBJECT_" + fieldSoeji, "Total Credits");
                        }
                        for (final Hyotei h : s._hyoteiList) {
                            if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                VrsOut("CREDIT" + h._annualPosition + "_" + fieldSoeji, h._gradeCredit); //単位
                            }
                        }
                        VrEndRecord();
                    }

                    _hasData = true;

                } else {
                    // 左50列、右40列内下3行固定
                    int maxLine = 50 + 40 - 3;
                    if (_formAttendRecord) {
                        // 出欠レコード
                        maxLine -= 3;
                    }
                    if (param._isOutputDebug) {
                        log.info(" studyrecList size = " + studyrecListAll.size());
                    }

                    String oldspecialdiv = null;      //専門区分
                    String oldclasscd = null;      //教科コードの保管
                    int rnum = 0;               //出力件数
                    for (final Studyrec s : studyrecListAll) {
                        if (param._isOutputDebug) {
                            log.info("subclasscd = " + s._subclasscd +", annual = " + s._credit + ", subclassname = " + (printData._isPrintJp ? s._subclassnameJp : s._subclassname));
                        }
                        //教科コードの変わり目
                        if ((null == oldspecialdiv || "0".equals(oldspecialdiv)) && "1".equals(s._specialdiv)) {
                            for (; rnum < 50; rnum++) {
                                VrsOut("KARA" + getFieldNumber(rnum) + "_1", String.valueOf(rnum));
                                VrEndRecord();
                            }
                            _hasData = true;
                        }
                        oldspecialdiv = s._specialdiv;
                        if ((oldclasscd == null || !s._classcd.equals(oldclasscd))) {
                            VrsOut("CLASS" + getFieldNumber(rnum), printData._isPrintJp ? s._classnameJp : s._classname);   //教科名
                            VrsOut("KARA" + getFieldNumber(rnum) + "_2", "1");
                            oldclasscd = s._classcd;
                            VrEndRecord();
                            _hasData = true;
                            rnum++;
                        }
                        final String retu = getFieldNumber(rnum);
                        VrsOut("CLASSCD" + retu, s._classcd);      //教科コード

                        VrsOutFieldSelect(new String[] {"SUBCLASS" + retu, "SUBCLASS" + retu + "_2", "SUBCLASS" + retu + "_3"}, printData._isPrintJp ? s._subclassnameJp : s._subclassname); //科目名

                        for (final Hyotei h : s._hyoteiList) {
                            //明細出力
                            if (h._annualPosition > 0) {
                                if (h._grades != null && Integer.parseInt(h._grades) > 0) {
                                    VrsOut("GRADE" + retu + "_" + h._annualPosition, param.getRepValue(h._grades)); //評定
                                }
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    VrsOut("CREDIT" + retu + "_" + h._annualPosition, h._gradeCredit); //単位
                                }
                            }
                        }
                        VrEndRecord();    //最後のレコード出力
                        rnum++;
                    }
                    if (!param._z010.in(Z010.sapporo)) {
                        final int max = maxLine - printData._creditOnlyStudyrecClassList.size();
                        for (; rnum < max; rnum++) {   //総合的な学習の時間以降は定位置へ出力の為、空行を送る
                            VrsOut("KARA" + getFieldNumber(rnum) + "_1",  String.valueOf(rnum));
                            VrEndRecord();
                        }
                        if (param._isOutputDebug) {
                            log.info(" creditOnlyStudyrecClassList = " + printData._creditOnlyStudyrecClassList);
                        }
                        for (final Studyrec creditOnlyStudyrec : printData._creditOnlyStudyrecClassList) {
                            VrsOutFieldSelect(new String[] {"ITEM2", "ITEM2_2", "ITEM2_3"}, printData._isPrintJp ? creditOnlyStudyrec._classnameJp : creditOnlyStudyrec._classname);  //教科名

                            for (final Hyotei h : creditOnlyStudyrec._hyoteiList) {
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    VrsOut("TOTAL_CREDIT2_" + h._annualPosition, h._gradeCredit); //単位
                                }
                            }
                            VrEndRecord();
                        }
                    }
                    final List<Tuple<String, Studyrec>> sogoAbroadTotal = new ArrayList<Tuple<String, Studyrec>>();
                    sogoAbroadTotal.add(Tuple.of("sogo", printData.studyrecSogo));
                    sogoAbroadTotal.add(Tuple.of("abroad", printData.studyrecAbroad));
                    sogoAbroadTotal.add(Tuple.of("total", printData.studyrecTotal));
                    for (final Tuple<String, Studyrec> tup : sogoAbroadTotal) {
                        final Studyrec s = tup._second;
                        if (null == s) {
                            continue;
                        }
                        String title = null;
                        if (printData.studyrecSogo == s) {
                            if (printData._isPrintJp) {
                                title = param.sogoSubclassnameJp(printData._studyrec90MaxSubclasscd, printData);
                            } else {
                                title = defstr(printData._ketteiSogoSubclassname, param._e021name1, Period_for_Integrated_Study);
                            }
                        } else if (printData.studyrecAbroad == s) {
                            if (printData._isPrintJp) {
                                title = "留学";
                            } else {
                                title = "CREDITS TRANSFERRED FROM OVERSEAS SCHOOLS";
                            }
                        } else if (printData.studyrecTotal == s) {
                            if (printData._isPrintJp) {
                                title = "計";
                            } else {
                                title = "TOTAL CREDITS";
                            }
                        }
                        VrsOut("KARA2_1", String.valueOf(rnum));

                        if (!param._z010.in(Z010.sapporo)) {
                            VrsOutFieldSelect(new String[] {"ITEM2", "ITEM2_2", "ITEM2_3"}, title);
                        }

                        log.info(" sogoAbroadTotal " + tup + ("total".equals(tup._first) ? ", " + printData._totalcredits : ""));

                        if ("total".equals(tup._first)) {
                            // TODO: printData.studyrecTotalを使用する
                            for (final Map.Entry<Integer, Integer> e : printData._totalcredits.entrySet()) {
                                final Integer annualPosition = e.getKey();
                                final Integer credits = e.getValue();
                                if (0 < annualPosition && annualPosition <= _formMaxGrade) {
                                    VrsOut("TOTAL_CREDIT2_" + annualPosition.toString(), null == credits ? "0" : credits.toString()); //単位
                                }
                            }
                        } else {
                            for (final Hyotei h : s._hyoteiList) {

                                if (printData.studyrecSogo == s && null != h._grades && !"0".equals(h._grades)) {
                                    VrsOut("TOTAL_GRADE2_" + h._annualPosition, param.getRepValue(h._grades)); //評定
                                }
                                if (h._gradeCredit != null && Integer.parseInt(h._gradeCredit) > 0) {
                                    VrsOut("TOTAL_CREDIT2_" + h._annualPosition, h._gradeCredit); //単位
                                }
                            }
                        }
                        VrEndRecord();
                    }
                    if (_formAttendRecord) {
                        final List<Attend> attendList = printData.getPrintAttendList(param);
                        VrsOut("ITEM_ATTEND", "ATTENDANCE");
                        VrsOut("ITEM_ATTEND2", "Days of School");
                        for (final Attend att : attendList) {
                            VrsOut("ATTEND_" + String.valueOf(att._pos), att._classdays);        //授業日数
                        }
                        VrEndRecord();

                        VrsOut("ITEM_ATTEND", "ATTENDANCE");
                        VrsOut("ITEM_ATTEND2", "Days Absent");
                        for (final Attend att : attendList) {
                            VrsOut("ATTEND_" + String.valueOf(att._pos), att._kesseki);        //欠席
                        }
                        VrEndRecord();

                        VrsOut("ITEM_ATTEND", "ATTENDANCE");
                        VrsOut("ITEM_ATTEND2", "Days Present");
                        for (final Attend att : attendList) {
                            VrsOut("ATTEND_" + String.valueOf(att._pos), att._present);         //出席
                        }
                        VrEndRecord();
                    }
                    _hasData = true;
                }
            }
        }

        private void printRecordForm2(final PrintData printData, final Param param) {
            final int gline = 40;
            String s_subclasscd = "00"; //科目コード
            int linex = 0;              //行数

            boolean hasD065flg = false;
            if (printData._studyrecRowList.size() > 0) {
                hasD065flg = KnjDbUtils.firstRow(printData._studyrecRowList).keySet().contains("D065FLG");
            }
            if (!hasD065flg) {
                log.warn("no field D065FLG.");
            }

            final List<List<Map<String, String>>> printSubclassList = new ArrayList<List<Map<String, String>>>();
            List<Map<String, String>> currentSubclassList = null;
            final List<Map<String, String>> sogoList = new ArrayList<Map<String, String>>();
            final List<Map<String, String>> totalList = new ArrayList<Map<String, String>>();
            final List<Map<String, String>> creditOnlyList = new ArrayList<Map<String, String>>();
            for (final Map rs : printData._studyrecRowList) {
                final String specialFlg = !rs.containsKey("SPECIAL_FLG") ? null : KnjDbUtils.getString(rs, "SPECIAL_FLG");
                final String subclasscd = KnjDbUtils.getString(rs, "SUBCLASSCD");

                if (sogo.equals(specialFlg)) {
                    if (sogoGoukeiAnnual.equals(KnjDbUtils.getString(rs, "ANNUAL"))) {
                        sogoList.add(rs);
                    }
                    continue;
                }
                if (abroad.equals(specialFlg)) {
                    continue;
                }
                if (total.equals(specialFlg)) {
                    if (totalGoukeiAnnual.equals(KnjDbUtils.getString(rs, "ANNUAL"))) {
                        totalList.add(rs);
                    }
                    continue;
                }
                if (creditOnly.equals(specialFlg)) {
                    creditOnlyList.add(rs);
                    continue;
                }
                if (param._z010.in(Z010.Kindai) && hasD065flg && null != KnjDbUtils.getString(rs, "D065FLG")) {
                    continue;
                }
                if (null == currentSubclassList || "00".equals(s_subclasscd) || null == s_subclasscd || !s_subclasscd.equals(subclasscd)) {
                    currentSubclassList = new ArrayList<Map<String, String>>();
                    printSubclassList.add(currentSubclassList);
                }

                currentSubclassList.add(rs);
                s_subclasscd = subclasscd;
            }

            if (param._isOutputDebug) {
                for (int i = 0; i < sogoList.size(); i++) {
                    log.info(Util.debugMapToStr(" sogo[" + i + "] = ", sogoList.get(i), ""));
                }
                for (int i = 0; i < totalList.size(); i++) {
                    log.info(Util.debugMapToStr(" total[" + i + "] = ", totalList.get(i), ""));
                }
                for (int i = 0; i < creditOnlyList.size(); i++) {
                    log.info(Util.debugMapToStr(" creditOnly[" + i + "] = ", creditOnlyList.get(i), ""));
                }
            }

            for (final List<Map<String, String>> subclassRecordList : printSubclassList) {

                final Map<String, String> rs0 = subclassRecordList.get(0);
                final String subclasscd = KnjDbUtils.getString(rs0, "SUBCLASSCD");
                final String subclassname = KnjDbUtils.getString(rs0, "SUBCLASSNAME");
                if (param._isOutputDebug) {
                    log.info(" subclass = " + subclasscd + " : " + subclassname);
                }

                //科目名称出力
                if (subclassname != null) {
                    final String field;
                    if (param._z010.in(Z010.Kindai)) {
                        field = getFieldForDataKeta(new String[] {"SUBJECT", "SUBJECT2", "SUBJECT3", "SUBJECT4"}, subclassname);
                    } else {
                        field = getFieldForDataKeta(new String[] {"SUBJECT", "SUBJECT3", "SUBJECT2", "SUBJECT4"}, subclassname);
                    }
                    VrsOut(field, subclassname);  //科目名
                }

                for (final Map<String, String> drs : subclassRecordList) {

                    //学年ごとの出力
                    String grades = KnjDbUtils.getString(drs, "GRADES");
                    if (grades != null) {
                        final int i = printData.getAnnualPosition(param, KnjDbUtils.getString(drs, "ANNUAL"));

                        if (hasD065flg && null != KnjDbUtils.getString(drs, "D065FLG")) {
                            grades = param._d001Abbv1Map.get(grades);
                        }
                        VrsOut("GRADING" + String.valueOf(i), grades);    //評定
                    }
                }

                final String credit = KnjDbUtils.getString(rs0, "CREDIT");
                if (credit != null && 0 < Integer.parseInt(credit)) {
                    VrsOut("CREDIT", credit);      //単位数の合計
                }

                VrEndRecord();
                _hasData = true;
                linex++;
                if (linex == gline) {
                    break;  //行のオーバーフロー
                }
            }
            final List<Map<String, String>> rest = new ArrayList<Map<String, String>>();
            if (!creditOnlyList.isEmpty()) {
                final String classname = KnjDbUtils.getString(creditOnlyList.get(0), "CLASSNAME");
                final String credit = Util.stringNumSum(KnjDbUtils.getColumnDataList(creditOnlyList, "CREDIT"));
                final Map<String, String> record = new HashMap<String, String>();
                final String field = getFieldForDataKeta(new String[] {"SUBJECT_2",  "SUBJECT3_2", "SUBJECT2_2", "SUBJECT4_2"}, classname);
                record.put(field, classname);
                record.put("CREDIT_2", credit);
                rest.add(record);
            }
            final boolean useSogo = param._z010.in(Z010.rakunan) || param._z010.in(Z010.miyagiken);
            String sogoSum = null;
            if (useSogo && null != printData._studyrec90MaxSubclasscd) {
                final String subclassname = param.sogoSubclassname(printData._studyrec90MaxSubclasscd);
                sogoSum = Util.stringNumSum(KnjDbUtils.getColumnDataList(sogoList, "CREDIT"));
                final Map<String, String> record = new HashMap<String, String>();
                final String field = getFieldForDataKeta(new String[] {"SUBJECT_2",  "SUBJECT3_2", "SUBJECT2_2", "SUBJECT4_2"}, subclassname);
                record.put(field, subclassname);
                record.put("CREDIT_2", sogoSum);
                rest.add(record);
            }

            final boolean useTotal = param._z010.in(Z010.miyagiken);
            if (useTotal) {
                String sum = Util.stringNumSum(KnjDbUtils.getColumnDataList(totalList, "CREDIT"));
                if (useSogo) {
                    sum = Util.add(sum, sogoSum);
                }
                final Map<String, String> record = new HashMap<String, String>();
                record.put("SUBJECT_2", "Total");
                record.put("SUBJECT_2.Attribute", "Hensyu=3"); // 中央寄せ
                record.put("CREDIT_2", sum);
                rest.add(record);
            }
            for (int i = 0, count = gline - linex - rest.size(); i < count; i++) {
                VrsOut("SUBJECT", "1");             //科目名
                _svf.VrAttribute("SUBJECT", "X=10000");
                VrEndRecord();
                _hasData = true;
            }
            for (int i = 0; i < rest.size(); i++) {
                final Map<String, String> record = rest.get(i);
                for (final Iterator it = record.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String field = (String) e.getKey();
                    final String value = (String) e.getValue();
                    if (field.endsWith(".Attribute")) {
                        _svf.VrAttribute(field.substring(0, field.indexOf(".Attribute")), value);
                    } else {
                        VrsOut(field, value);
                    }
                }
                VrEndRecord();
                _hasData = true;
            }
        }

        private String[] get_token(final String data, int keta) {
            if (null == data || keta <= 0) {
                return new String[] {};
            }
            String[] split = KNJ_EditEdit.get_token(data, keta, 1);
            if (null == split) {
                return new String[] {};
            }
            return split;
        }

        private String kakko(final String v, final int keta) {
            if (StringUtils.isBlank(v)) {
                return "";
            }
            String rtn = "(" + v + ")";
            if (0 < keta && keta < getMS932ByteLength(rtn)) {
                // 桁あふれする場合、内容を削って最後のかっこを表示する
                final StringBuffer stb = new StringBuffer("(");
                final int lastKakkoKeta = getMS932ByteLength(")");
                int ketaSum = getMS932ByteLength(stb.toString());
                for (int i = 0; i < v.length(); i++) {
                    final char ch = v.charAt(i);
                    final int charKeta = getMS932ByteLength(String.valueOf(ch));
                    if (keta < ketaSum + charKeta + lastKakkoKeta) {
                        break;
                    }
                    stb.append(ch);
                    ketaSum += charKeta;
                    //log.debug(" add '" + ch + "' " + ketaSum + " (" + charKeta + "), keta = " + keta);
                }
                rtn = stb.append(")").toString();
            }
            return rtn;
        }

        /*
         *  学校情報印刷
         */
        private void printSchoolInfo(final DB2UDB db2, final PrintData printData, final Param param) {
            final SchoolInfo schoolInfo = printData._schoolInfo;
            if (param._isOutputDebugSchool) {
                log.info(" schoolInfo = " + toDebugString(schoolInfo._schoolMst));
            }
            if (!schoolInfo._schoolMst.isEmpty()) {

                // 記載日
                String date = null;
                if (printData._isPrintJp) {
                    if (null != printData._date) {
                        date = KNJ_EditDate.h_format_JP(db2, printData._date);
                    }
                } else if (param._z010.in(Z010.jogakkan)) {
                    date = Util.h_format_US(param._dateNow, MMMM_d_comma_yyyy);
                } else if (isForm2()) {
                    if (printData._date == null) {
                        date = "";
                    } else if (!_param.isTok()) {
                        date = Util.h_format_US(printData._date, "d,MMM,yyyy");
                    } else {
                        date = Util.h_format_US(printData._date, d_MMMM_yyyy);
                    }
                } else {
                    if (param._z010.in(Z010.ChiyodaKudan)) {
                        date = KNJ_EditDate.h_format_UK(printData._date, "MMMM");
                    } else {
                        date = Util.h_format_US(printData._date, dateFormat(param));
                    }
                }
                VrsOut("DATE", date);
                if (param._z010.in(Z010.jyoto)) {
                    VrsOut("OUTPUT_DATE", date);
                }

                // 学校住所
                final String schoolAddr1Eng = KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLADDR1_ENG");
                final String schoolAddr2Eng = KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLADDR2_ENG");
                if (isForm2()) {
                    String addr = null;
                    if (schoolAddr1Eng != null) {
                        addr = schoolAddr1Eng + defstr(schoolAddr2Eng);
                    }
                    VrsOutFieldSelect(new String[] {"SCHOOL_ADDRESS1", "SCHOOL_ADDRESS2", "SCHOOL_ADDRESS3"}, addr); // 学校住所
                } else {
                    if (param._z010.in(Z010.meikei)) {
                        VrsOut("SCHOOL_ADDRESS1", schoolAddr1Eng); // 学校住所
                        VrsOut("SCHOOL_ADDRESS2", schoolAddr2Eng); // 学校住所
                        VrsOut("PHONE", "PHONE:" + KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLTELNO")); // 電話番号

                        //VrsOut("STAFFNAME", null); // 職員氏名

                    } else {
                        final String addr;
                        if (param._z010.in(Z010.jogakkan)) {
                            addr = schoolAddr1Eng;
                        } else {
                            addr = defstr(schoolAddr1Eng) + defstr(schoolAddr2Eng);
                        }
                        final String field;
                        if (param._z010.in(Z010.ChiyodaKudan)) {
                            field = getFieldForDataKeta(new String[] {"SCHOOL_ADDRESS1", "SCHOOL_ADDRESS2", "SCHOOL_ADDRESS3"}, addr);
                        } else if (param._z010.in(Z010.kaijyo)) {
                            field = "SCHOOL_ADDRESS1";
                        } else if (param._z010.in(Z010.jogakkan)) {
                            field = getFieldForDataKeta(new String[] {"SCHOOL_ADDRESS1", "SCHOOL_ADDRESS2"}, addr);
                        } else {
                            field = getFieldForDataKeta(new String[] {"SCHOOLADDRESS2", "SCHOOLADDRESS3"}, addr);
                        }
                        VrsOut(field, addr);
                    }
                }

                // 校長名
                final String principalName;
                if (_param.isTok()) {
                    principalName = KnjDbUtils.getString(schoolInfo._certifSchoolDat, "PRINCIPAL_NAME");
                } else {
                    principalName = StringUtils.upperCase(schoolInfo._principalStaffNameEng);
                }
                if (isForm2()) {
                    VrsOut("STAFFNAME", principalName);
                } else {
                    if (param._z010.in(Z010.sapporo)) {
                        VrsOut("JOBNAME",  KnjDbUtils.getString(schoolInfo._certifSchoolDat, "JOB_NAME"));
                        VrsOut("STAFFNAME", principalName);
                    } else if (param._z010.in(Z010.sakae)) {
                        VrsOut("JOBNAME",  KnjDbUtils.getString(schoolInfo._certifSchoolDat, "JOB_NAME"));

                    } else {
                        final String jobname = defstr(KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLNAME1")) + defstr(schoolInfo._staffJobname);
                        if (jobname.length() > 0) {
                            VrsOut("JOBNAME",  jobname);
                        }

                        VrsOut("STAFFNAME", defstr(principalName, schoolInfo._staffPrincipalanme));
                    }
                }

                // 学校名
                if (isForm2()) {
                    final String schoolnameEng = KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLNAME_ENG");
                    VrsOut("SCHOOLNAME1", schoolnameEng);
                    VrsOut("SCHOOLNAME2", "PRINCIPAL," + defstr(schoolnameEng));

                } else {
                    final String schoolname = KnjDbUtils.getString(schoolInfo._certifSchoolDat, "SCHOOL_NAME");
                    final String schoolnameEng = KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLNAME_ENG");

                    if (schoolname != null) {
                        if (param._z010.in(Z010.ChiyodaKudan)) {
                            VrsOut("SCHOOLNAME1", schoolname);
                        } else if (param._z010.in(Z010.meikei)) {
                            if (null != schoolname) {
                                VrsOut("SCHOOLNAME1", schoolname.toUpperCase());
                            }
                            VrsOut("SCHOOLNAME3", schoolname);
                        } else if (param._z010.in(Z010.jogakkan)) {
                            VrsOut("SCHOOLNAME1", defstr(schoolnameEng).toUpperCase());
                            VrsOut("SCHOOLNAME2", schoolnameEng);
                        } else {
                            VrsOut("SCHOOLNAME", schoolname);
                            VrsOut("TEXT_SCHOOLNAME_JP", KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLNAME1"));
                            VrsOut("SCHOOLNAME_JP", KnjDbUtils.getString(schoolInfo._schoolMst, "SCHOOLNAME1"));
                        }
                    }
                    if (param._z010.in(Z010.meikei)) {
                        VrsOut("SCHOOLNAME2", defstr(KnjDbUtils.getString(schoolInfo._certifSchoolDat, "JOB_NAME")) + ", " + defstr(schoolname));
                    }
                    if (param._z010.in(Z010.KaichiIkkan)) {
                        VrAttribute("SCHOOLNAME1", "Size=10");
                    }
                }

                // 備考
                if (param._z010.in(Z010.jyoto)) {
                    if (printData._isPrintJp) {
                        VrsOut("SCHOOL_NAME", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "SCHOOL_NAME")); // 学校名
                        VrsOut("JOB_NAME", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "JOB_NAME")); // 校長職名
                        VrsOut("PRINCIPAL_NAME", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK3")); // 校長名
                    } else {
                        VrsOut("PRINCIPAL_NAME", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "PRINCIPAL_NAME")); // 校長名
                        VrsOut("REMARK1", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK1")); // 学校名 英
                        VrsOut("REMARK2", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK2")); // 学校名2 英
                    }
                } else if (!isForm2()) {
                    VrsOut("REMARK1", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK1")); // 学校名 日本語
                    VrsOut("REMARK2", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK2")); // 校長 職名 日本語
                    VrsOut("REMARK3", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK3")); // 校長名 日本語
                    final String remark4 = KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK4");
                    if (param._z010.in(Z010.kaijyo) && null != remark4) {
                        final String[] splitted = splitBySizeWithSpace(remark4, new int[] {92, 100});
                        for (int i = 0; i < splitted.length; i++) {
                            VrsOut("REMARK4_" + String.valueOf(i + 1), splitted[i]);
                        }
                    }
                    VrsOut("REMARK6", KnjDbUtils.getString(schoolInfo._certifSchoolDat, "REMARK6")); // 法人名
                }
            }
            if (_usePrincipalSignatureImage) {
                if (null != _principalSignatureImage) {
                    VrsOut("SIGNATURE", _principalSignatureImage);
                }
            }
            if (_useSchoolLogoImage) {
                if (null != _schoolLogoImage) {
                    VrsOut("SCHOOLLOGO", _schoolLogoImage);
                }
            }
            if (_isPrintStamp) {
                if (null != _certifSchoolstampImagePath) {
                    VrsOut("STAMP", _certifSchoolstampImagePath);
                }
            }
        }

        private static String getNumSuffix(final int inum) {
            String sfx = "";
            if (inum >= 0) {
                switch (inum % 10) {
                case 1: sfx = "st"; break;
                case 2: sfx = "nd"; break;
                case 3: sfx = "rd"; break;
                default: sfx = "th"; break;
                }
            }
            return sfx;
        }

        private static String toDebugString(final Map infoMap) {
            final StringBuffer stb = new StringBuffer();
            final Map infoMap2 = new HashMap(infoMap);
            for (final Iterator it = infoMap2.keySet().iterator(); it.hasNext();) {
                final Object key = it.next();
                if (key instanceof Integer) {
                    it.remove();
                } else if (key instanceof String) {
                    final String skey = (String) key;
                    final int numIndex = StringUtils.indexOfAny(skey, "123456789");
                    if (0 <= numIndex && StringUtils.repeat("_", numIndex).equals(skey.substring(0, numIndex))) {
                        it.remove();
                    }
                }
            }
            final List keys = new ArrayList(infoMap2.keySet());
            Collections.sort(keys);
            stb.append("{");
            for (int i = 0; i < keys.size(); i++) {
                stb.append("\n " + keys.get(i) + " = [" + infoMap2.get(keys.get(i)) + "] , ");
            }
            stb.append("}");
            return stb.toString();
        }

        /*
         *  個人情報印刷
         */
        private void printPersonalInfo(final DB2UDB db2, final PrintData printData, final Param param) {
            if (null == printData._personalInfo) {
                return;
            }
            if (param._isOutputDebugPersonal) {
                log.info(" _address = " + toDebugString(printData._personalInfo));
            }

            // 生徒氏名
            final String setName;
            if (printData._isPrintJp) {
                setName = KnjDbUtils.getString(printData._personalInfo, "NAME");
            } else {
                final String nameEng = KnjDbUtils.getString(printData._personalInfo, "NAME_ENG");
                if (_param._z010.in(Z010.meikei)) {
                    setName = StringUtils.replaceOnce(nameEng, " ", ", ");
                } else {
                    setName = nameEng;
                }
            }
            VrsOutFieldSelect(new String[] {"NAME", "NAME2", "NAME3"}, setName);
            if (param._z010.in(Z010.sakae)) {
                final String name = KnjDbUtils.getString(printData._personalInfo, "NAME");
                VrsOutFieldSelect(new String[] {"NAME_JP",  "NAME_JP2", "NAME_JP3"}, name);
            }
            if (_param._z010.in(Z010.ryukei)) {
                // 流通経済
                final int max = 20;
                for (int i = 0; i < defstr(printData._schregno).length(); i++) {
                    VrsOutn("HEADER_SCHREGNO", max - i, String.valueOf(printData._schregno.charAt(i)));
                }
                for (int i = 0; i < defstr(setName).length(); i++) {
                    VrsOutn("HEADER_NAME", max - i, String.valueOf(setName.charAt(i)));
                }
                final String printDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
                for (int i = 0; i < defstr(printDate).length(); i++) {
                    VrsOutn("HEADER_DATE", max - i, String.valueOf(printDate.charAt(i)));
                }
            }
            if (isForm2()) {
                VrsOut("CERTIFNO", printData._certifNumber);  // 証明書番号
                if (!printData._personalInfo.isEmpty()) {
                    log.info(" grdDiv = " + printData._grdDiv);
                    VrsOutFieldSelect(new String[] {"DATE_TITLE", "DATE_TITLE2"}, getGrdDivTitle(printData));

                    final String sexEng;
                    final String dateFormat;
                    if (!_param.isTok()) {
                        dateFormat = "d,MMM,yyyy";
                        sexEng = KnjDbUtils.getString(printData._personalInfo, "SEX_ENG");
                    } else {
                        dateFormat = d_MMMM_yyyy;
                        sexEng = KnjDbUtils.getString(printData._personalInfo, "SEX_ENG2");
                    }

                    VrsOut("SEX",      sexEng);
                    VrsOut("BIRTHDAY", Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "BIRTHDAY"), dateFormat));
                    VrsOut("DATE_E",   Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "ENT_DATE"), dateFormat));
                    VrsOut("DATE_C",   Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "GRADU_DATE"), dateFormat));
                    VrsOut("MAJOR",    KnjDbUtils.getString(printData._personalInfo, "MAJORENG"));
                }
            } else if (param._z010.in(Z010.meikei) || param._z010.in(Z010.reitaku)) {
                if (!printData._personalInfo.isEmpty()) {

                    VrsOut("BIRTHDAY", Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "BIRTHDAY"), dateFormat(param)));
                    VrsOut("DATE_ENTER",   Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "ENT_DATE"), dateFormat(param)));
                    VrsOut("SEX", KnjDbUtils.getString(printData._personalInfo, "SEX_ENG2")); // 性別

                    if (printData._graduDate != null) {
                        if ("3".equals(printData._grdDiv)) {
                            VrsOut("TRANSFER_DATE", Util.h_format_US(printData._graduDate, dateFormat(param)));
                        } else if ("1".equals(printData._grdDiv)) {
                            VrsOut("GRADUATION_DATE", Util.h_format_US(printData._graduDate, dateFormat(param)));
                        }
                    }
                }

                printNendoName(printData, param);
            } else if (param._z010.in(Z010.kaijyo)) {
                if (!printData._personalInfo.isEmpty()) {

                    VrsOut("ADDRESS", defstr(KnjDbUtils.getString(printData._personalInfo, "ADDR1_ENG")) + defstr(KnjDbUtils.getString(printData._personalInfo, "ADDR2_ENG")));

                    VrsOut("BIRTHDAY", Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "BIRTHDAY"), dateFormat(param)));
                    VrsOut("SDATE",   Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "ENT_DATE"), dateFormat(param)));

                    if (printData._graduDate != null) {
                        VrsOut("GRAD_DATE",     Util.h_format_US(printData._graduDate, dateFormat(param)));
                    }
                }
                printNendoName(printData, param);
            } else if (param._z010.in(Z010.sakae)) {
                if (!printData._personalInfo.isEmpty()) {
                    final String birthday = KnjDbUtils.getString(printData._personalInfo, "BIRTHDAY");
                    if (null != birthday) {
                        VrsOut("BIRTHDAY", Util.h_format_US(birthday, dateFormat(param)));
                        VrsOut("BIRTHDAY_JP", printData._birthdayJp);
                    }

                    final SvfField field1Jp = getField("ADDRESS1_JP");
                    final SvfField field2Jp = getField("ADDRESS2_JP");
                    final int fieldKeta1Jp = null == field1Jp ? 0 : field1Jp._fieldLength;
                    final int fieldKeta2Jp = null == field2Jp ? 0 : field2Jp._fieldLength;
                    final String addr1Jp = KnjDbUtils.getString(printData._personalInfo, "ADDR1");
                    final String addr2Jp = KnjDbUtils.getString(printData._personalInfo, "ADDR2");
                    if ("1".equals(KnjDbUtils.getString(printData._personalInfo, "ADDR_FLG"))) {
                        if (getMS932ByteLength(addr1Jp) <= fieldKeta1Jp && getMS932ByteLength(addr2Jp) <= fieldKeta2Jp) {
                            VrsOut("ADDRESS1_JP", addr1Jp);
                            VrsOut("ADDRESS2_JP", addr2Jp);
                        } else {
                            final List<String> tokenList = KNJ_EditKinsoku.getTokenList(StringUtils.defaultString(addr1Jp) + StringUtils.defaultString(addr2Jp), fieldKeta1Jp);
                            for (int i = 0; i < tokenList.size(); i++) {
                                VrsOut("ADDRESS" + String.valueOf(i + 1) + "_JP", tokenList.get(i));
                            }
                        }
                    } else {
                        final List<String> tokenList = KNJ_EditKinsoku.getTokenList(StringUtils.defaultString(addr1Jp), fieldKeta1Jp);
                        for (int i = 0; i < tokenList.size(); i++) {
                            VrsOut("ADDRESS" + String.valueOf(i + 1) + "_JP", tokenList.get(i));
                        }
                    }
                    final SvfField field1 = getField("ADDRESS1");
                    final SvfField field2 = getField("ADDRESS2");
                    final int fieldKeta1 = null == field1 ? 0 : field1._fieldLength;
                    final int fieldKeta2 = null == field2 ? 0 : field2._fieldLength;
                    final String addr1Eng = KnjDbUtils.getString(printData._personalInfo, "ADDR1_ENG");
                    final String addr2Eng = KnjDbUtils.getString(printData._personalInfo, "ADDR2_ENG");
                    if ("1".equals(KnjDbUtils.getString(printData._personalInfo, "ADDR_FLG"))) {
                        if (getMS932ByteLength(addr1Eng) <= fieldKeta1 && getMS932ByteLength(addr2Eng) <= fieldKeta2) {
                            VrsOut("ADDRESS1", addr1Eng);
                            VrsOut("ADDRESS2", addr2Eng);
                        } else {
                            final String[] arr = splitBySizeWithSpace(StringUtils.defaultString(addr1Eng) + StringUtils.defaultString(addr2Eng), new int[] {fieldKeta1, fieldKeta2});
                            for (int i = 0; i < arr.length; i++) {
                                VrsOut("ADDRESS" + String.valueOf(i + 1), arr[i]);
                            }
                        }
                    } else {
                        final String[] arr = splitBySizeWithSpace(addr1Eng, new int[] {fieldKeta1, fieldKeta2});
                        for (int i = 0; i < arr.length; i++) {
                            VrsOut("ADDRESS" + String.valueOf(i + 1), arr[i]);
                        }
                    }

                    VrsOut("NATIONALITY", KnjDbUtils.getString(printData._personalInfo, "NATIONALITY_NAME_ENG"));
                    VrsOut("NATIONALITY_JP", KnjDbUtils.getString(printData._personalInfo, "NATIONALITY_NAME"));

                    VrsOut("SEX", KnjDbUtils.getString(printData._personalInfo, "SEX_ENG2"));
                    VrsOut("SEX_JP", KnjDbUtils.getString(printData._personalInfo, "SEX"));
                }
                printNendoName(printData, param);
            } else {
                if (!printData._personalInfo.isEmpty()) {
                    final String birthday = KnjDbUtils.getString(printData._personalInfo, "BIRTHDAY");
                    if (null != birthday) {
                        String birthdayStr = "";
                        if (param._z010.in(Z010.ChiyodaKudan)) {
                            birthdayStr = Util.h_format_US(birthday, "　d ／ M ／ yyyy");
                        } else {
                            birthdayStr = Util.h_format_US(birthday, dateFormat(param));
                        }
                        VrsOut("BIRTHDAY", birthdayStr);
                    }
                    // 入学日付
                    final String sdate;
                    if (printData._isPrintJp) {
                        sdate = KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(printData._personalInfo, "ENT_DATE"));
                    } else {
                        sdate = Util.h_format_US(KnjDbUtils.getString(printData._personalInfo, "ENT_DATE"), dateFormat(param));
                    }
                    VrsOut("SDATE", sdate);

                    if (param._z010.in(Z010.ChiyodaKudan)) {

                        final String sexEng2 = KnjDbUtils.getString(printData._personalInfo, "SEX_ENG2");
                        if ("Female".equalsIgnoreCase(sexEng2)) {
                            VrsOut("FEMALE", "○");
                        } else if ("Male".equalsIgnoreCase(sexEng2)) {
                            VrsOut("MALE", "○");
                        }
                    }
                    if (printData._graduDate != null) {
                        final String fdate;
                        final String expGraduatee;
                        final String gradDate;
                        if (printData._isPrintJp) {
                            fdate = KNJ_EditDate.h_format_JP(db2, printData._graduDate);
                            expGraduatee = fdate;
                            gradDate = fdate;
                        } else {
                            fdate = Util.h_format_US(printData._graduDate, dateFormat(param));
                            expGraduatee = Util.h_format_US(printData._graduDate, d_MMMM_yyyy);
                            gradDate = Util.h_format_US(printData._graduDate, d_MMMM_yyyy);
                        }
                        VrsOut("FDATE",         fdate);
                        VrsOut("EXP_GRADUATEE", expGraduatee);
                        VrsOut("GRAD_DATE",     gradDate);
                    }
                    if (param._z010.in(Z010.jyoto)) {
                        if (null == printData._grdDiv || "4".equals(printData._grdDiv)) {
                            VrsOut("EXPECTED", "expected");
                        }
                    }
                    if (KnjDbUtils.getString(printData._personalInfo, "GRADE") != null) {
                        int ia = printData.getAnnualPosition(param, KnjDbUtils.getString(printData._personalInfo, "GRADE"));
                        String grade = "";
                        if (!printData.isGakunensei(param)) {
                            if (ia == 1) grade = "1st-year";
                            else if (ia == 2) grade = "2nd-year";
                            else if (ia == 3) grade = "3rd-year";
                            else grade = String.valueOf(ia) + "th-year";
                        } else {
                            grade = String.valueOf(ia) + " year";
                        }
                        VrsOut("GRADE", grade);
                    }
                }
                final String gradText;
                final String graduDateUk = null == printData._graduDate ? "" : Util.h_format_US(printData._graduDate, d_MMMM_yyyy);
                if ("2".equals(printData._grdDiv)) { // 退学
                    gradText = "□ This student withdrew from " + defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SCHOOL_NAME")) + " on " + graduDateUk + " .";
                } else {
                    gradText = "□ This student graduated from " + defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SCHOOL_NAME")) + " on " + graduDateUk + " .";
                }
                VrsOut("GRADUATE", gradText);
                printNendoName(printData, param);
                pritnAbroad(printData);  // 留学情報印刷
            }
        }

        private void printNendoName(final PrintData printData, final Param param) {
            String certifName;

            final boolean isOutputCertifNo = "0".equals(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "CERTIF_NO"));  //証書番号の印刷 0:あり,1:なし
            if (isOutputCertifNo) {
                certifName = defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SYOSYO_NAME")) + defstr(printData._certifNumber, "     ") + defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SYOSYO_NAME2"));
            } else {
                certifName = defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SYOSYO_NAME")) + "     " + defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SYOSYO_NAME2"));
            }
            if (param._z010.in(Z010.sundaikoufu)) {
                VrsOut("CERTIF_NAME2", certifName);  //証書番号
            } else {
                VrsOut("SYOSYO_NAME", certifName);  //証書番号
            }
            VrsOut("NENDO_NAME", printData._certifNumber + defstr(KnjDbUtils.getString(printData._schoolInfo._certifSchoolDat, "SYOSYO_NAME")));  // 証明書番号
        }

        private String dateFormat(final Param param) {
            if (param._z010.in(Z010.kaijyo)) {
                return KAIJYO_DATE_FORMAT;
            } else if (param._z010.in(Z010.meikei) || param._z010.in(Z010.sakae) || param._z010.in(Z010.reitaku) || param._z010.in(Z010.jogakkan)) {
                return MMMM_d_comma_yyyy;
            } else if (param._z010.in(Z010.miyagiken)) {
                return MMM_dot_d_comma_yyyy_miyagiken;
            }
            return d_MMMM_yyyy;
        }

        /**
         * 単語途中でなるべく区切らないように分割
         *
         * <pre>
         * splitBySizeWithSpace("abcde fghij klmnop", 13) = {"abcde fghij ", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 10) = {"abcde", "fghij", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 7) =  {"abcde", "fghij", "klmnop"}
         * </pre>
         *
         * @param s 文字列
         * @param keta 桁
         * @return 文字列を分割した配列
         */
        private static String[] splitBySizeWithSpace(final String s, final int[] ketas) {
            if (null == s) {
                return new String[] {};
            } else if (s.length() <= ketas[0]) {
                return new String[] {s};
            }
            final List<String> split = new ArrayList<String>();
            int idx = ketas[0];
            int beforeidx = 0;
            while (true) {
                int idxSpace = -1;
                boolean isCheckSpace = false;
                if (s.charAt(idx) != ' ') { // 単語途中で区切りがスペースでなければ、前方探索
                    isCheckSpace = true;
                    for (int i = idx - 1; i > beforeidx; i--) {
                        if (s.charAt(i) == ' ') {
                            idxSpace = i;
                            break;
                        }
                    }
                    //log.info("  idxSpace = " + idxSpace + " at " + s + " ( " + idx + " = " + s.charAt(idx) + ")");
                }
                if (idxSpace != -1) {
                    split.add(s.substring(beforeidx, idxSpace));
                    beforeidx = idxSpace + 1;
                    idx = beforeidx + ketas[split.size() < ketas.length ? split.size() : ketas.length - 1];
                } else {
                    // スペースがなければ指定区切りまでを追加
                    if (isCheckSpace) {
                        //log.info(" no space-char in [" + beforeidx + ", " + idx + "] string [" + s.substring(beforeidx, idx) + "]  full [" + s + "]");
                    }
                    split.add(s.substring(beforeidx, idx));
                    beforeidx = idx;
                    idx = beforeidx + ketas[split.size() < ketas.length ? split.size() : ketas.length - 1];
                }
                if (s.length() <= idx) {
                    split.add(s.substring(beforeidx));
                    break;
                }
            }
            final String[] array = new String[split.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = split.get(i);
            }
            return array;
        }

        /*
         *  個人情報 留学
         */
        private void pritnAbroad(final PrintData printData) {
            final List<String> places = new ArrayList<String>();
            places.add("");
            int cnum = 0;
            boolean first = true;
            for (final Map rs1 : printData._abroadList) {
                final String transferSdate = KnjDbUtils.getString(rs1, "TRANSFER_SDATE");
                final String transferEdate = KnjDbUtils.getString(rs1, "TRANSFER_EDATE");
                final String place = KnjDbUtils.getString(rs1, "TRANSFERPLACE");
                final String credits = KnjDbUtils.getString(rs1, "ABROAD_CREDITS");

                if (first) {
                    VrsOut("ABROAD_SDATE",    Util.h_format_US(transferSdate, d_MMMM_yyyy));
                    first = false;
                }
                if (transferEdate != null) {
                    VrsOut( "ABROAD_FDATE",    Util.h_format_US(transferEdate, d_MMMM_yyyy));
                }
                if (!StringUtils.isBlank(place) && !places.contains(place)) {
                    places.add(place);
                }
                if (NumberUtils.isDigits(credits)) {
                    cnum += Integer.parseInt(credits);
                }
            }
            if (!first) {
                VrsOut("ABROAD_CREDIT",   String.valueOf(cnum));    //単位
            }
            if (places.size() > 0) {
                VrsOut("ABROAD_SCHOOL", Util.mkString(places, "，"));
            }
        }

        /*
         *  出欠の記録印刷
         */
        private void printAttend(final PrintData printData, final Param param) {
            if (_formAttendRecord) {
                return;
            }
            for (final Attend attend : printData.getPrintAttendList(param)) {
                final String annual = attend._annual;

                final int pos = printData.getAnnualPosition(param, annual);
                if (isForm2()) {
                    if (attend._attend1        != null) VrsOutn("DAYS",    pos, attend._attend1);
                    if (attend._suspend         != null) VrsOutn("EXCUSED", pos, attend._suspend);
                    if (attend._kesseki        != null) VrsOutn("ABSENT",  pos, attend._kesseki);
                    if (attend._present         != null) VrsOutn("ATTEND",  pos, attend._present);
                    if (attend._requirepresent  != null) VrsOutn("MUST",    pos, attend._requirepresent);
                } else if (param._z010.in(Z010.ChiyodaKudan)) {
                    VrsOutn("ATTEND", pos, defstr(attend._present) + " ／ " + defstr(attend._requirepresent));
                } else {
                    VrsOutn("LESSON"      ,pos  ,attend._attend1);
                    VrsOutn("SPECIAL"     ,pos  ,attend._susMour);
                    VrsOutn("ABROAD"      ,pos  ,attend._abroad);
                    VrsOutn("PRESENT"     ,pos  ,attend._requirepresent);
                    VrsOutn("ABSENCE"     ,pos  ,attend._kesseki);
                    VrsOutn("ATTEND"      ,pos  ,attend._present);
                }
            }
        }

        /**
         * @param number
         * @return
         */
        private String getFieldNumber(final int number) {
            return (50 > number) ? "1": "2";
        }

        private String getGrdDivTitle(final PrintData printData) {
            String dateTitle = "";
            if ("1".equals(printData._grdDiv)) {
                // 卒業
                if (_param._z010.in(Z010.miyagiken)) {
                    dateTitle = "Date of Completion : ";
                } else {
                    dateTitle = "Date of Graduation : ";
                }
            } else if ("2".equals(printData._grdDiv)) {
                // 退学
                dateTitle = "Date of Withdrawal : ";
            } else if ("3".equals(printData._grdDiv)) {
                // 転学
                dateTitle = "Date of Transfer : ";
            } else if ("4".equals(printData._grdDiv) || null == printData._grdDiv) {
                // 卒業見込み
                if (_param._z010.in(Z010.miyagiken)) {
                    dateTitle = "Expected Graduation Date : ";
                } else {
                    dateTitle = "Date of Completion : ";
                }
            }
            return dateTitle;
        }

        private static String[] setArrayCentering(final String[] split, final int classnameLine) {
            final String[] arr = new String[Math.max((null == split ? 0 : split.length), classnameLine)];
            Arrays.fill(arr, "");
            if (null != split) {
                final int spc = (arr.length - split.length) / 2;
                for (int i = 0; i < split.length; i++) {
                    arr[spc + i] = split[i];
                }
            }
            return arr;
        }

        private List<Map> getClazzList(final List<Studyrec> studyrecList) {
            final List<Map> clazzList = new ArrayList<Map>();
            Map current = null;
            for (int i = 0; i < studyrecList.size(); i++) {
                final Studyrec studyrec = studyrecList.get(i);
                if (null == current || null == current.get("CLASSCD") || !current.get("CLASSCD").equals(studyrec._classcd)) {
                    current = new HashMap();
                    current.put("CLASSCD", studyrec._classcd);
                    current.put("CLASSNAME", studyrec._classname);
                    current.put("CLASSNAME_JP", studyrec._classnameJp);
                    clazzList.add(current);
                }
                Util.getMappedList(current, "STUDYREC_LIST").add(studyrec);
            }
            return clazzList;
        }
    }

    private static class Studyrec {
        String _specialdiv;
        String _classname;
        String _classnameJp;
        String _credit;
        String _classcd;
        String _subclasscd;
        String _subclassname;
        String _subclassnameJp;
        List<Hyotei> _hyoteiList = new ArrayList<Hyotei>();

        public String toString() {
            return "Studyrec(" + _classcd + ": " + _classname + ", " + _subclasscd + ": " + _subclassname + ", " + _hyoteiList + ")";
        }
    }


    private static class Hyotei {
        String _grades;
        String _gradeCredit;
        int _annualPosition;
        public String toString() {
            return "Hyotei(" + _annualPosition + " = " + _grades + ", " + _gradeCredit + ")";
        }
    }

    private static class Attend {
        final Map<String, String> _row;
        final String _year;
        final String _annual;
        final String _classdays;
        final String _attend1; // 授業日数
        final String _abroad; // 留学
        final String _susMour; // 出停・忌引
        final String _suspend; // 出停
        final String _kesseki; // 欠席
        final String _present; // 出席
        final String _requirepresent; // 出席しなければならない日数
        final int _pos;
        Attend(final PrintData printData, final Param param, final Map<String, String> row) {
            _row = row;
            _year = KnjDbUtils.getString(_row, "YEAR");
            _annual = KnjDbUtils.getString(_row, "ANNUAL");
            _classdays = KnjDbUtils.getString(_row, "CLASSDAYS");
            _attend1 = KnjDbUtils.getString(_row, "ATTEND_1");
            _abroad = KnjDbUtils.getString(row, "ABROAD");
            _susMour = KnjDbUtils.getString(row, "SUSP_MOUR");
            _suspend = KnjDbUtils.getString(row, "SUSPEND");
            _kesseki = KnjDbUtils.getString(row, "KESSEKI");
            _present = KnjDbUtils.getString(row, "PRESENT");
            _requirepresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
            _pos = printData.getAnnualPosition(param, _annual);
        }
    }

    private static class Util {

        private static <T extends Comparable<T>> T max(final Collection<T> col) {
            T max = null;
            for (T t : col) {
                if (null == t) {
                    continue;
                }
                if (null == max || t.compareTo(max) > 0) {
                    max = t;
                }
            }
            return max;
        }

        private static <T extends Comparable<T>> T min(final Collection<T> col) {
            T min = null;
            for (T t : col) {
                if (null == t) {
                    continue;
                }
                if (null == min || t.compareTo(min) < 0) {
                    min = t;
                }
            }
            return min;
        }

        private static <T> List<T> reverse(final Collection<T> col) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = new ArrayList<T>(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        /**
         *  過卒生対応年度取得
         */
        private static String b_year(final String pdate) {
            final List<String> month123 = new ArrayList<String>(Arrays.asList("01", "02", "03"));
            String b_year = null;
            if (pdate != null) {
                b_year = pdate.substring(0, 4);
                String b_month = pdate.substring(5, 7);
                if (month123.contains(b_month)) {
                    b_year = String.valueOf(Integer.parseInt(b_year) - 1);
                }
            }
            return defstr(b_year);
        }

        public static String add(final String d1, final String d2) {
            if (!NumberUtils.isDigits(d1)) return d2;
            if (!NumberUtils.isDigits(d2)) return d1;
            return String.valueOf(Integer.parseInt(d1) + Integer.parseInt(d2));
        }

        public static String stringNumSum(final List<String> list) {
            int sum = 0;
            boolean hasValue = false;
            for (final String v : list) {
                if (NumberUtils.isDigits(v)) {
                    sum += Integer.parseInt(v);
                    hasValue = true;
                }
            }
            return hasValue ? String.valueOf(sum) : null;
        }

        public static String debugMapToStr(final String debugText, final Map map0, final String split) {
            final StringBuffer stb = new StringBuffer();
            final Map map = new HashMap(map0);
            stb.append(defstr(debugText) + " [\n");
            final List keys = new ArrayList(map.keySet());
            for (final Iterator<Object> it = keys.iterator(); it.hasNext();) {
                final Object key = it.next();
                if (key instanceof Integer) {
                    it.remove();
                } else if (key instanceof String) {
                    final String skey = (String) key;
                    final int numIndex = StringUtils.indexOfAny(skey, "123456789");
                    if (0 <= numIndex && StringUtils.repeat("_", numIndex).equals(skey.substring(0, numIndex))) {
                        it.remove();
                    }
                }
            }
            try {
                Collections.sort(keys);
            } catch (Exception e) {
            }
            for (int i = 0; i < keys.size(); i++) {
                final Object key = keys.get(i);
                stb.append(i == 0 ? "   " : " , ").append(key).append(": ").append(map.get(key)).append(split);
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
        }//String h_format_US_Mの括り

        public static String formatBirthdayJp(final DB2UDB db2, final String date, final boolean isSeireki) {
            String birthdayStr = "";
            if (date != null) { // 生年月日
                if (isSeireki) {
                    birthdayStr = getChristianEra(date, "yyyy年M月d日");
                } else {
                    birthdayStr = KNJ_EditDate.h_format_JP(db2, date);
                }
            }
            return birthdayStr;
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

        public static String mkString(final Collection<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            for (final String s : list) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
            }
            return stb.toString();
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

        public static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
       }
    }

    protected static class Tuple<A, B> implements Comparable<Tuple<A, B>> {
        final A _first;
        final B _second;
        private Tuple(final A first, final B second) {
            _first = first;
            _second = second;
        }
        public static <A, B> Tuple<A, B> of(A a, B b) {
            return new Tuple<A, B>(a, b);
        }
        public int hashCode() {
            return _first.hashCode() * 234201017 + _second.hashCode();
        }
        public int compareTo(final Tuple<A, B> o) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == o._first && !(o._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(o._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == o._second && !(o._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(o._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    private static enum Z010 {
        Kindai("近大", "KINDAI", "KINJUNIOR"),
        HOUSEI("法政"),
        kyoto("京都府"),
        miyagiken("宮城県"),
        sapporo("札幌開成"),
        sundaikoufu("駿台甲府"),
        rakunan("洛南"),
        kaijyo("海城"),
        ChiyodaKudan("千代田九段", "chiyoda"),
        meikei("茗溪"),
        osakatoin("大阪桐蔭"),
        sakae("埼玉栄"),
        matsudo("専修大松戸"),
        reitaku("麗澤"),
        KaichiIkkan("開智一貫部", "kikan"),
        KaichiSougou("開智総合部", "ksogo"),
        KaichiKoutou("開智高等部", "kkotou"),
        KaichiTushin("開智通信", "ktsushin"),
        jyoto("福岡工業城東"),
        jogakkan("東京女学館"),
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

    private static class Param {
        final String _z010Name1;
        final Z010 _z010;
        final boolean _isSeireki;
        final String _dateNow;
        final String _dateNowFormatJp;
        public String _certifSchoolstampImagePath;
        final boolean _isOutputAttend;
        final boolean _hasStudyrecProvFlgDat;
        final boolean _hasSchoolMstSchoolKind;
        public String _seisekishoumeishoEngSogoPrintSubclassname;

        final String _useCurriculumcd;
        private Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();

        /**
         * 未履修の科目を表示するか
         *  (予備1が'Y'なら表示しない。それ以外は表示する。）
         */
        protected Map<String, String> _a027Namespare1Map;
        private final Map<String, String> _nameMstD001Map;
        public final KNJDefineSchool _definecode;  // 各学校における定数等設定

        private String _psAttendrecSql;
        private String _psPersonalinfoSql;

        final List<Map<String, String>> _e021RecordList;
        final String _e021name1;
        final String _e021name2;
        private final Map<String, String> _d001Abbv1Map;
        private final Map _controlMstMap;

        private final TreeMap<String, String> _classKeyClassnameEng90Map;
        private final TreeMap<String, String> _subclassKeySubclassnameEng90Map;
        private final TreeMap<String, String> _subclassKeySubclassnameJp90Map;
        public Properties _prgInfoPropertiesFilePrperties;
        private Map<String, String> _dbPrgInfoProperties;

        final Map<String, File> _createdFiles = new HashMap<String, File>();
        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugPersonal;
        final boolean _isOutputDebugSchool;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugField;
        final boolean _isOutputDebugSvfFormCreate;

        Param(final DB2UDB db2, final KNJDefineSchool definecode) {
            _definecode = definecode;

            _z010Name1 = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'")), "NAME1");
            _dbPrgInfoProperties = getDbPrginfoProperties(db2);
            final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            if (!ArrayUtils.isEmpty(outputDebug)) {
                log.info(" outputDebug = " + ArrayUtils.toString(outputDebug));
            }
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugPersonal = ArrayUtils.contains(outputDebug, "personal");
            _isOutputDebugSchool = _isOutputDebug || ArrayUtils.contains(outputDebug, "school");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugField = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "field");
            _isOutputDebugSvfFormCreate = ArrayUtils.contains(outputDebug, "SvfFormCreate");
            _seisekishoumeishoEngSogoPrintSubclassname = property("seisekishoumeishoEngSogoPrintSubclassname");
            if (StringUtils.isEmpty(_seisekishoumeishoEngSogoPrintSubclassname)) {
                _seisekishoumeishoEngSogoPrintSubclassname = null;
            }

            _z010 = Z010.fromString(_z010Name1);
            log.info(" z010 = " + _z010 + " (" + _z010Name1 + ")");
            _isOutputAttend = !_z010.in(Z010.kaijyo);

            _dateNow = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            _dateNowFormatJp = KNJ_EditDate.h_format_JP(db2, _dateNow);

            _a027Namespare1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'A027' "), "NAMECD2", "NAMESPARE1");

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' "))); // 西暦フラグをセット。
            _hasStudyrecProvFlgDat = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);
            _hasSchoolMstSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

            _e021RecordList = KnjDbUtils.query(db2, " SELECT NAME1, NAME2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E021' ORDER BY NAMECD2 ");
            final Map e021 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAME1, NAME2 FROM NAME_MST WHERE NAMECD1 = 'E021' AND NAMECD2 = '1' "));
            _e021name1 = KnjDbUtils.getString(e021, "NAME1");
            _e021name2 = KnjDbUtils.getString(e021, "NAME2");
            _nameMstD001Map = setNameMstD001(db2, "NAME1");
            _d001Abbv1Map = setNameMstD001(db2, "ABBV1");
            _controlMstMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT CTRL_YEAR, CTRL_SEMESTER, CTRL_DATE, IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' "));

            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : null;
            final String class90sql;
            final String subclass90sql;
            if ("1".equals(_useCurriculumcd)) {
                class90sql = " SELECT CLASSCD || '-' || SCHOOL_KIND AS CLASS_KEY, T1.* FROM CLASS_MST T1 WHERE CLASSCD = '90' ";
                subclass90sql = " SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASS_KEY, VALUE(SUBCLASSORDERNAME1, SUBCLASSNAME) AS SUBCLASSNAME_JP, T1.* FROM SUBCLASS_MST T1 WHERE CLASSCD = '90' ";
            } else {
                class90sql = " SELECT CLASSCD AS CLASS_KEY, T1.* FROM CLASS_MST T1 WHERE CLASSCD = '90' ";
                subclass90sql = " SELECT SUBCLASSCD AS SUBCLASS_KEY, VALUE(SUBCLASSORDERNAME1, SUBCLASSNAME) AS SUBCLASSNAME_JP, T1.* FROM SUBCLASS_MST T1 WHERE SUBSTR(SUBCLASSCD, 1, 2) = '90' ";
            }
            _classKeyClassnameEng90Map = new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, class90sql), "CLASS_KEY", "CLASSNAME_ENG"));
            _subclassKeySubclassnameEng90Map = new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, subclass90sql), "SUBCLASS_KEY", "SUBCLASSNAME_ENG"));
            _subclassKeySubclassnameJp90Map = new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, subclass90sql), "SUBCLASS_KEY", "SUBCLASSNAME_JP"));
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

        private String sogoSubclassname(final String subclasscd) {
            String subclassnameEng90 = null;
            if (!StringUtils.isEmpty(_e021name1)) {
                subclassnameEng90 = _e021name1;
            }
            if (null != subclasscd) {
                subclassnameEng90 = _subclassKeySubclassnameEng90Map.get(subclasscd);
                if (null == subclassnameEng90) {
                    log.warn(" no name for " + subclasscd + " in " + _subclassKeySubclassnameEng90Map.keySet());
                }
            }
            if (null == subclassnameEng90) {
                subclassnameEng90 = Period_for_Integrated_Study;
            }
            return subclassnameEng90;
        }

        private String sogoSubclassnameJp(final String subclasscd, final PrintData printData) {
            String subclassnameJp90 = null;
            if (!StringUtils.isEmpty(_e021name2)) {
                subclassnameJp90 = _e021name2;
            }
            if (null != subclasscd) {
                subclassnameJp90 = _subclassKeySubclassnameJp90Map.get(subclasscd);
                if (null == subclassnameJp90) {
                    log.warn(" no name for " + subclasscd + " in " + _subclassKeySubclassnameJp90Map.keySet());
                }
            }
            if (null == subclassnameJp90) {
                subclassnameJp90 = printData.isTankyu() ? "総合的な探究の時間" : "総合的な学習の時間";
            }
            return subclassnameJp90;
        }

        private PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                if (_isOutputDebugQuery) {
                    log.info(" " + psKey + " sql = " + sql);
                }
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE080' "), "NAME", "VALUE");
        }

        private String getRepValue(String val) {
            String rtnVal = "";
            if (null != val) {
                if (_z010.in(Z010.jogakkan)) {
                    if ("1".equals(val)) {
                        rtnVal = "E";
                    } else if ("2".equals(val)) {
                        rtnVal = "D";
                    } else if ("3".equals(val)) {
                        rtnVal = "C";
                    } else if ("4".equals(val)) {
                        rtnVal = "B";
                    } else if ("5".equals(val)) {
                        rtnVal = "A";
                    }
                } else if (_z010.in(Z010.ChiyodaKudan)) {
                    if (_nameMstD001Map.containsKey(val)) {
                        rtnVal = _nameMstD001Map.get(val);
                    } else {
                        rtnVal = val;
                    }
                } else {
                    rtnVal = val;
                }
            }
            return rtnVal;
        }

        private String getImagePath(final String documentroot, final String filename, final String ext) {
            final String imageDir = KnjDbUtils.getString(_controlMstMap, "IMAGEPATH");
            final String imageExt = null != ext ? ext : KnjDbUtils.getString(_controlMstMap, "EXTENSION");
            if (null == documentroot) {
                log.warn(" documentroot null.");
                return null;
            } // DOCUMENTROOT
            if (null == imageDir) {
                log.warn(" imageDir null.");
                return null;
            }
            if (null == imageExt) {
                log.warn(" imageExt null.");
                return null;
            }
            if (null == filename) {
                log.warn(" filename null.");
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(documentroot);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(imageExt);
            final File file = new File(stb.toString());
            log.warn("image file:" + file.getAbsolutePath() + " exists? " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return stb.toString();
        }

        public boolean isTok() {
            return !_z010.in(Z010.Kindai);
        }

        /**
         * @param _nameMstD001Map 設定する _nameMstD001Map。
         */
        private Map<String, String> setNameMstD001(final DB2UDB db2, final String field) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = 'D001' ORDER BY NAMECD2"), "NAMECD2", field);
        }

        public boolean isNotPrintMirishu(final String certifKindcd) {
            final String namespare1 = _a027Namespare1Map.get(certifKindcd);
            return "Y".equals(namespare1); // trueなら未履修科目を出力しない
        }
    }
}

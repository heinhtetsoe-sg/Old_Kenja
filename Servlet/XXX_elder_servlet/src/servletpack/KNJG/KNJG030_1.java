// kanji=漢字
/*
 * $Id: 11c69304bd815bf5af18e13e3d5e354e18db52f7 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.File;
import java.io.FileReader;
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
import servletpack.KNJE.KNJE080_1;
import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJI.KNJI070_1;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfForm;

/*
 *  学校教育システム 賢者 [事務管理]  単位修得証明書
 */

public class KNJG030_1 {

    private static final Log log = LogFactory.getLog(KNJG030_1.class);

    private static final String CERTIF_KINDCD = "011";
    private static final String CERTIF_KINDCD_GRD = "029";
    private static final String CERTIF_KINDCD_18_MIKOMI = "018";
    private static final String CERTIF_KINDCD_17_ENG = "017";
    private static final String CERTIF_KINDCD_35_ENG_MIKOMI = "035";
    private static final String CERTIF_KINDCD_RISHU_TANNI_SHOMEISHO = "055";

    private static final String d_MMMM_yyyy = "d MMMM yyyy";

    private DB2UDB _db2;                         //Databaseクラスを継承したクラス
    boolean nonedata;

    protected Param _param;
    private Form _form;

    public KNJG030_1(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode) {
        _db2 = db2;
        nonedata = false;
        _param = new Param(db2);
        if (_param._isMiyagiken) {
            _param._knje080 = new KNJE080_1(db2, svf, definecode);
            _param._knji070 = new KNJI070_1(db2, svf, definecode);
        }
        _form = new Form(_param, svf);
        log.fatal("$Revision: 76808 $ $Date: 2020-09-12 01:03:11 +0900 (土, 12 9 2020) $");
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        pre_stat(hyotei, new HashMap());
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map paramMap) {
        if (_param._isMiyagiken) {
            if ("1".equals(paramMap.get("PRINT_GRD"))) {
                _param._knji070.pre_stat(hyotei, paramMap);
            } else {
                _param._knje080.pre_stat(hyotei, paramMap);
            }
            return;
        }

        if ("1".equals(paramMap.get("PRINT_GRD"))) {
            log.debug("GRADUATION DATA");
            _form._isGrd = true;
        }
    }

//    private void outputFuhakkou(final PrintData printData) {
//        printData._outputFuhakkouResult = new HashMap();
//        try {
//            final Map paramMap = new HashMap(printData._paramap);
//            paramMap.put("YEAR", printData._paramap.get("CTRL_YEAR"));
//            paramMap.put("SCHREGNO", printData._schregno);
//            paramMap.put("CERTIF_KINDCD", printData._paramap.get("CERTIFKIND"));
//            paramMap.put("Vrw32alp", svf);
//            paramMap.put("methodName", "outputFuhakkou");
//            paramMap.put("Z010.NAME1", _param._z010Name1);
//
//            printData._outputFuhakkouResult = KNJG010.execMethod(db2, paramMap);
//
//            log.info("fuhakkou = " + printData._outputFuhakkouResult);
//        } catch (final Throwable e) {
//            log.fatal("exception in executing KNJG010.execMethod()");
//        }
//        if ("true".equals(printData._outputFuhakkouResult.get("OUTPUT_SVF"))) {
//            nonedata = true;
//        }
//        printData._isFuhakkou = "true".equals(printData._outputFuhakkouResult.get("IS_FUHAKKOU"));
//    }

    public void printSvf(final String year, final String semester, final String schregno, final String date, final String certifKindCd, final String certifNumber, final Map paramap) {
        _param._paramap = paramap;
        if (_param._isMiyagiken) {
            final KNJE080_1 o;
            if ("1".equals(paramap.get("PRINT_GRD"))) {
                o = _param._knji070;
            } else {
                o = _param._knje080;
            }
            final String staffCd = (String) paramap.get("SEKI");
            o.printSvf(year, semester, date, schregno, paramap, staffCd, 11, null, certifNumber);
            nonedata = nonedata || o.nonedata;
            return;
        }
        _param.setDocumentroot((String) paramap.get("DOCUMENTROOT"));
        _param._useCurriculumcd = _param.property("useCurriculumcd");
        _param._useGakkaSchoolDiv = _param.property("useGakkaSchoolDiv");
        _param._certifPrintRealName = _param.property("certifPrintRealName");
        _param._tannishutokushoumeishoPrintCoursecodename = _param.property("tannishutokushoumeishoPrintCoursecodename");
        _param._sogoTankyuStartYear = _param.property("sogoTankyuStartYear");
        _param._stampSizeMm = _param.property("stampSizeMm");
        _param._stampPositionXmmTanniShutokuShomeisho = _param.property("stampPositionXmmTanniShutokuShomeisho");
        _param._stampPositionYmmTanniShutokuShomeisho = _param.property("stampPositionYmmTanniShutokuShomeisho");

        if (null == _param._knjStudyrecSql) {
            //_param._knjStudyrecSql = new KNJ_StudyrecSql("hyde", "hyde", 2, false, _param._isHosei, _param._isNotPrintMirishu, _param._useCurriculumcd);
            _param._knjStudyrecSql = new StudyrecSql(_param);
            if (_form._isGrd) {
                _param._knjStudyrecSql.tableSchregStudyrecDat = "GRD_STUDYREC_DAT";
                _param._knjStudyrecSql.tableSchreggTransferDat = "GRD_TRANSFER_DAT";
                _param._knjStudyrecSql.tableSchregRegdDat = "GRD_REGD_DAT";
            } else {
                _param._knjStudyrecSql.tableSchregStudyrecDat = "SCHREG_STUDYREC_DAT";
                _param._knjStudyrecSql.tableSchreggTransferDat = "SCHREG_TRANSFER_DAT";
                _param._knjStudyrecSql.tableSchregRegdDat = "SCHREG_REGD_DAT";
            }
        }
        if (_param._isOutputDebug) {
            log.info(" parameter = [" + year + ", " + semester + ", " + schregno + ", " + date + ", " + certifKindCd + ", " + certifNumber + "], " + new TreeMap(paramap));
        }

        final PrintData printData = new PrintData(year, semester, schregno, date, certifKindCd, certifNumber, paramap, _param);

//        outputFuhakkou(printData);
        printData.load(_db2, _form._isGrd, _param);

        _form.print(_db2, printData);
        if (_form._hasData) {
            nonedata = true;
        }
    }

    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        if (_param._isMiyagiken) {
            _param._knje080.pre_stat_f();
            _param._knji070.pre_stat_f();
            return;
        }
        _param.close();
    }

    private static String defstr(final Object o) {
        return defstr(o, "");
    }

    private static String defstr(final Object o, final String alt) {
        return null == o ? alt : o.toString();
    }

    private static String mkString(final List<String> list, final String comma) {
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

    private static String addNumber(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) return num2;
        if (!NumberUtils.isDigits(num2)) return num1;
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private static int toInt(final String s, final int def) {
        return NumberUtils.isDigits(s) ? Integer.parseInt(s) : def;
    }

    private static String getNentukihiStr(final DB2UDB db2, final Param param, final String date) {
        if (null == date) {
            return null;
        }
        final String dateStr;
        if (param._seirekiFlg) {
            dateStr = getChristianEra(date, "yyyy年M月d日");
        } else {
            dateStr = KNJ_EditDate.h_format_JP(db2, date);
        }
        return dateStr;
    }

    private static String getNentukiStr(final DB2UDB db2, final Param param, final String date) {
        final String year1;
        if (param._seirekiFlg) {
            year1 = getChristianEra(date, "yyyy年M月");
        } else {
            year1 = KNJ_EditDate.h_format_JP_M(db2, date);
        }
        return year1;
    }

    private static String getBirthdayStr(final DB2UDB db2, final PrintData printData, final Param param, final String birthday, final String birthdayFlg) {
        if (null == birthday) {
            return null;
        }
        final String birthdayStr;
        if (printData.isEng()) {
            birthdayStr = h_format_US(birthday, d_MMMM_yyyy);
        } else {
            if (param._seirekiFlg || (!param._seirekiFlg && "1".equals(birthdayFlg))) {
                birthdayStr = getChristianEra(birthday, "yyyy年M月d日") + "生";  // 証明日付
            } else {
                birthdayStr = KNJ_EditDate.h_format_JP_Bth(db2, birthday);   //生年月日
            }
        }
        return birthdayStr;
    }

    private static String formatDate(final DB2UDB db2, final Param param, final String date) {
        final String year2;
        if (param._seirekiFlg) {
            year2 = date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
        } else {
            year2 = KNJ_EditDate.h_format_JP_M(db2, date);
        }
        return year2;
    }

    private static String h_format_US(final String strx, final String format) {
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

    /**
     * 西暦に変換。
     *
     * @param  strx     : '2008/03/07' or '2008-03-07'
     * @param  pattern  : 'yyyy年M月d日生'
     * @return hdate    : '2008年3月7日生'
     */
    private static String getChristianEra(final String strx, final String pattern) {
        String hdate = new String();
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat();
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
            final SimpleDateFormat sdfseireki = new SimpleDateFormat(pattern);
            hdate = sdfseireki.format(dat);
        } catch (Exception e3) {
            hdate = "";
        }
        return hdate;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static class PrintData {
        final String _year;
        final String _semester;
        final String _schregno;
        final String _date;
        final String _certifKindCd; // 証明書種別
        final String _certifNumber; // 発行番号
        final String _seki; // 記載責任者 職員コード
        final String _certifKind;
        final String _certifSchoolOnly;
        final Map _paramap;
//        public int _annual;
        final String _lastLineClasscd;
        protected String _notUseClassMstSpecialDiv; // 教科マスタの専門区分を使用しない
        private boolean _isPrintStamp;
        private String _nendo;
        private String _dateString;
        private Map _schoolInfo;
        private Map _personalInfo;
        private List<StudyrecClass> _studyrecClassList;
        private String _sekiStaffname; // 記載責任者 職員名
        public boolean _isHiSchool;
        String _d015Namespare1;

        private Collection<String> _gakunenseiTengakuYears = Collections.emptyList();
        private List<String> _dropYears = Collections.emptyList();
        private Map<String, String> _annualGradeCdMap = Collections.EMPTY_MAP;
        private Map<String, String> _annualYearMap = Collections.EMPTY_MAP;
//        private Map _outputFuhakkouResult;
//        private boolean _isFuhakkou;
        private String _entDate; // 入学日付
        private String _curriculumYear;
        private String _entYearGradeCd;
        private String _regdSchoolKind;
        private String _regdGradeCd;
        private String _schoolKindMinGradeCd;
        protected String _majorYdatSchooldiv;
        private Map<String, String> _schoolMst;
        private String _certifSchoolstampImagePath;

        public PrintData(final String year, final String semester, final String schregno, final String date, final String certifKindCd, final String certifNumber, final Map paramap, final Param param) {
            _year = year;
            _semester = semester;
            _schregno = schregno;
            _date = date;
            _certifKindCd = certifKindCd;
            _certifNumber = certifNumber;
            _paramap = paramap;
            _isPrintStamp = "1".equals(parameter("PRINT_STAMP")) || param._isOsakatoin || param._isSakae || "1".equals(param.property("KNJG030_PRINT_STAMP"));
            _seki = parameter("SEKI");
            _certifKind = parameter("CERTIFKIND");
            _certifSchoolOnly = parameter("certifSchoolOnly");

            final String tannishutokuShoumeishoCreditOnlyClasscd = param.property("tannishutokuShoumeishoCreditOnlyClasscd");
            if (!StringUtils.isEmpty(tannishutokuShoumeishoCreditOnlyClasscd)) {
                _lastLineClasscd = tannishutokuShoumeishoCreditOnlyClasscd;
            } else if (param._isNishiyama) {
                _lastLineClasscd = "94";
            } else if (param._isRakunan) {
                _lastLineClasscd = "95";
            } else {
                _lastLineClasscd = null;
            }
        }

        public String parameter(final String name) {
            return (String) _paramap.get(name);
        }

        public boolean isEng() {
            return CERTIF_KINDCD_17_ENG.equals(_certifKindCd) || CERTIF_KINDCD_35_ENG_MIKOMI.equals(_certifKindCd);
        }

        public void load(final DB2UDB db2, final boolean isGrd, final Param param) {

            _d015Namespare1 = getD015Namespare1(db2, _year, param);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _dateString = getNentukihiStr(db2, param, _date);

            if ("1".equals(param._useGakkaSchoolDiv)) {
                setMajorYdatSchooldiv(db2);
            }

//            _annual = servletpack.KNJA.detail.KNJ_GradeRecSql.max_grade(db2, _year, _schregno); //最高学年取得
//            if (param._isOutputDebug) {
//            	log.info(" annual = " + _annual);
//            }
            setDropYearList(db2, param);

            String sql = "";
            sql += " SELECT T2.SCHOOL_KIND, T2.GRADE_CD, T3.GRADE_CD AS SCHOOL_KIND_MIN_GRADE_CD ";
            sql += " FROM SCHREG_REGD_DAT T1 ";
            sql += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            sql += " LEFT JOIN (SELECT SCHOOL_KIND, MIN(GRADE_CD) AS GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' GROUP BY SCHOOL_KIND) T3 ON T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
            sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SCHREGNO = '" + _schregno + "' ";

            final Map<String, String> regdRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _regdSchoolKind = KnjDbUtils.getString(regdRow, "SCHOOL_KIND");
            _regdGradeCd = KnjDbUtils.getString(regdRow, "GRADE_CD");
            _schoolKindMinGradeCd = KnjDbUtils.getString(regdRow, "SCHOOL_KIND_MIN_GRADE_CD");

            _isHiSchool = param._isJuniorHiSchool && "H".equals(_regdSchoolKind);

            if (_isPrintStamp) {
                _certifSchoolstampImagePath = param.getImageFilePath("CERTIF_SCHOOLSTAMP_" + _regdSchoolKind + ".bmp");
                if (null == _certifSchoolstampImagePath) {
                    _certifSchoolstampImagePath = param.getImageFilePath("SCHOOLSTAMP_" + _regdSchoolKind + ".bmp");
                }
            }

            setSchoolInfo(db2, param);

            _personalInfo = getPersonalInfo(db2, isGrd, param);
            if (param._isOutputDebug) {
                log.info(" personalInfo = " + _personalInfo);
            }
            _studyrecClassList = getStudyrecClassList(db2, param);
            if (param._isOutputDebug) {
                log.info(" studyrecClassList size = " + _studyrecClassList.size());
            }

            String schoolMstSql = " SELECT * FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                schoolMstSql += "      AND SCHOOL_KIND = '" + _regdSchoolKind + "' ";
            }
            _schoolMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, schoolMstSql));
        }

        private String getStudentName(final Param param) {
            final String name;
            if (isEng()) {
                name = KnjDbUtils.getString(_personalInfo, ("NAME_ENG"));
            } else {
                name = ("1".equals(KnjDbUtils.getString(_personalInfo, "USE_REAL_NAME")) || "1".equals(param._certifPrintRealName)) ? KnjDbUtils.getString(_personalInfo, "REAL_NAME") : KnjDbUtils.getString(_personalInfo, "NAME");
            }
            return name;
        }

        // D015に設定された名称予備1
        private String getD015Namespare1(final DB2UDB db2, final String year, final Param param) {
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE NAMECD1 = 'D015' AND YEAR = '" + year + "' ORDER BY NAMECD2 "));
            if (param._isOutputDebug) {
                log.info(" D015 " + year + " = " + rtn);
            }
            return rtn;
        }

        public String getSogoSubclassname(final Param param) {
            final int tankyuStartYear = NumberUtils.isDigits(param._sogoTankyuStartYear) ? Integer.parseInt(param._sogoTankyuStartYear) : 2019;
            boolean isTankyu = false;
            final int year = NumberUtils.isDigits(_year) ? Integer.parseInt(_year) : 0;
            final int gradeCdInt = NumberUtils.isDigits(_regdGradeCd) ? Integer.parseInt(_regdGradeCd) : 0;
            if (NumberUtils.isDigits(_curriculumYear)) {
                isTankyu = Integer.parseInt(_curriculumYear) >= tankyuStartYear;
            } else {
                if (year == tankyuStartYear     && gradeCdInt <= 1
                 || year == tankyuStartYear + 1 && gradeCdInt <= 2
                 || year == tankyuStartYear + 2 && gradeCdInt <= 3
                 || year >= tankyuStartYear + 3
                 ) {
                    isTankyu = true;
                }
            }
            if (param._isOutputDebug) {
                log.info(" 探究? " + isTankyu + ", year = " + year + ", gradeCdInt = " + gradeCdInt + ", curriculumYear = " + _curriculumYear);
            }
            return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
        }

        private void setDropYearList(final DB2UDB db2, final Param param) {
            if (isGakunensei(param)) {
                final String psKey = "PS_REGD_DROP_YEAR";
                if (null == param.getPs(psKey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT DISTINCT T1.YEAR");
                    stb.append(" FROM SCHREG_REGD_DAT T1");
                    stb.append(" WHERE T1.SCHREGNO = ? ");
                    stb.append("   AND T1.YEAR <= ? ");
                    stb.append("   AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR <= ? GROUP BY T2.GRADE)");
                    param.setPs(db2, psKey, stb.toString());
                }

                _dropYears = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _year}), "YEAR");
                if (param._isOutputDebug) {
                    log.info(" dropYears = " + _dropYears);
                }

                final String psKeyGradeAnnual = "PS_REGD_GRADE_ANNUAL";
                if (null == param.getPs(psKeyGradeAnnual)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT T1.ANNUAL, MAX(GDAT.GRADE_CD) AS GRADE_CD ");
                    stb.append(" FROM SCHREG_REGD_DAT T1");
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                    stb.append(" WHERE T1.SCHREGNO = ? ");
                    stb.append("   AND T1.YEAR <= ? ");
                    stb.append("   AND T1.YEAR IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 WHERE T2.SCHREGNO = ? AND T2.YEAR <= ? GROUP BY T2.GRADE)");
                    stb.append(" GROUP BY T1.ANNUAL ");
                    param.setPs(db2, psKeyGradeAnnual, stb.toString());
                }

                _annualGradeCdMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, param.getPs(psKeyGradeAnnual), new Object[] {_schregno, _year, _schregno, _year}), "ANNUAL", "GRADE_CD");
                if (param._isOutputDebug) {
                    log.info(" annualGradeCdMap = " + _annualGradeCdMap);
                }

                final String psKeyGradeYear = "PS_REGD_GRADE_YEAR";
                if (null == param.getPs(psKeyGradeYear)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT T1.ANNUAL, MAX(T1.YEAR) AS YEAR ");
                    stb.append(" FROM SCHREG_REGD_DAT T1");
                    stb.append(" WHERE T1.SCHREGNO = ? ");
                    stb.append("   AND T1.YEAR <= ? ");
                    stb.append(" GROUP BY T1.ANNUAL ");
                    param.setPs(db2, psKeyGradeYear, stb.toString());
                }

                _annualYearMap = new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, param.getPs(psKeyGradeYear), new Object[] {_schregno, _year}), "ANNUAL", "YEAR"));
                if (param._isOutputDebug) {
                    log.info(" annualYearMap = " + _annualYearMap);
                }
            }
        }

        public void setGakunenseiTengakuYears(final DB2UDB db2, final Param param) {
            if (!isGakunensei(param)) {
                return;
            }
            final String sql = "SELECT T1.GRD_DATE FROM SCHREG_ENT_GRD_HIST_DAT T1 WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.SCHOOL_KIND = '" + _regdSchoolKind + "' AND T1.GRD_DIV = '3' AND T1.GRD_DATE IS NOT NULL ";
            final String grdDate = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            if (null != grdDate) {
                final Calendar cal = Param.toCalendar(grdDate);
                if (cal.get(Calendar.MONTH) == Calendar.MARCH && cal.get(Calendar.DAY_OF_MONTH) == 31) {
                } else {
                    int year = cal.get(Calendar.YEAR);
                    if (cal.get(Calendar.MONTH) <= Calendar.MARCH) {
                        year -= 1;
                    }
                    final List<String> list = new ArrayList<String>();
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
        private void setNotUseClassMstSpecialDiv(final DB2UDB db2) {

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
            sql.append("     INNER JOIN CERTIF_SCHOOL_DAT T4 ON T4.YEAR = T2.YEAR AND T4.CERTIF_KINDCD = '" + _certifKind + "'");
            sql.append(" ) SELECT T1.*, T2.GRADE_CD AS ENT_YEAR_GRADE_CD  ");
            sql.append("   FROM MAIN T1 ");
            sql.append("   LEFT JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.ENT_YEAR ");
            sql.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = L1.YEAR AND T2.GRADE = L1.GRADE ");

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _notUseClassMstSpecialDiv = KnjDbUtils.getString(row, "NOT_USE_CLASS_MST_SPECIALDIV");
            _curriculumYear = KnjDbUtils.getString(row, "CURRICULUM_YEAR");
            _entYearGradeCd = KnjDbUtils.getString(row, "ENT_YEAR_GRADE_CD");
        }

        public String getSchoolDiv(final Param param) {
            final String schooldiv;
            if ("1".equals(param._useGakkaSchoolDiv)) {
                schooldiv = StringUtils.defaultString(_majorYdatSchooldiv, KnjDbUtils.getString(_schoolMst, "SCHOOLDIV"));
            } else {
                schooldiv = KnjDbUtils.getString(_schoolMst, "SCHOOLDIV");
            }
            return schooldiv;
        }

        public boolean isTannisei(final Param param) {
            return "1".equals(getSchoolDiv(param));
        }

        public boolean isGakunensei(final Param param) {
            return !isTannisei(param);
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
            _majorYdatSchooldiv = KnjDbUtils.getString(KnjDbUtils.lastRow(KnjDbUtils.query(db2, stb.toString())), "SCHOOLDIV");
            // log.debug(" schoolmst.schooldiv = " + _definecode.schooldiv + ", majorYdatSchoolDiv = " + _majorYdatSchooldiv + " -> "+  schooldiv);
        }


        private Map getPersonalInfo(final DB2UDB db2, final boolean isGrd, final Param param) {
            if ("1".equals(_certifSchoolOnly)) {
                return new HashMap();
            }
            final String psKey = "PS_PERSONAL";
            if (null == param.getPs(psKey)) {
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
                personalInfoSqlFlg.append("1"); // 10 gradeCd
                personalInfoSqlFlg.append(param._hasMAJOR_MST_MAJORNAME2 ? "1" : "0"); // 11 majorname2
                personalInfoSqlFlg.append("0"); // 12
                personalInfoSqlFlg.append("0"); // 13
                personalInfoSqlFlg.append(param._hasCOURSECODE_MST_COURSECODEABBV1 ? "1" : "0"); // 14 coursecodeabbv1
                final Map paramMap = new HashMap();
                if (isGrd) {
                    paramMap.put("PRINT_GRD", "1");
                }
                //  個人データ
                final String sql = new KNJ_PersonalinfoSql().sql_info_reg(personalInfoSqlFlg.toString(), paramMap);
                param.setPs(db2, psKey, sql);
            }

            final Map personalInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno, _year, _semester, _schregno, _year}));
            return personalInfo;
        }

        private void setSchoolInfo(final DB2UDB db2, final Param param) {
            final String psKey = "PS_SCHOOL";

            final String ctrlYear = (String) _paramap.get("CTRL_YEAR");
            final String year2;
            if (_date != null) {
                year2 = servletpack.KNJG.KNJG010_1.b_year(_date); // 過卒生対応年度取得->掲載日より年度を算出
            } else {
                year2 = ctrlYear;
            }

            _schoolInfo = null;
            if (param._isKindai) {
                if (null == param.getPs(psKey)) {
                    //  学校データ
                    final String sql = getSchoolInfoSqlKindai(param);
                    param.setPs(db2, psKey, sql);
                }

                final Object[] arg = new Object[] {year2, ctrlYear};
                if (param._isOutputDebug) {
                    log.info(" schoolInfo K arg = " + ArrayUtils.toString(arg));
                }
                _schoolInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), arg));
            } else {
                if (null == param.getPs(psKey)) {
                    //  学校データ
                    final String sql = getSchoolInfoSql(param);
                    param.setPs(db2, psKey, sql);
                }

                final Object[] arg = new Object[] {_certifKind, year2};
                if (param._isOutputDebug) {
                    log.info(" schoolInfo arg = " + ArrayUtils.toString(arg));
                }
                _schoolInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), arg));

            }

            if (null != _seki) {
                final String psKeyStaff = "PS_STAFF";
                if (null == param.getPs(psKeyStaff)) {
                    final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = ? ";
                    param.setPs(db2, psKeyStaff, sql);
                }
                _sekiStaffname = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKeyStaff), new Object[] { _seki }));
            }
        }

        /**
         *  学校情報
         *      ・クラス変数SWITCHにより、検索を制御
         *
         *      ・t_switch -->  //各種設定  初期値は0
         *              1番目  1:校長検索
         *              2番目  1:担当者をJOBCDで検索 2:担当者をSTAFFCDで検索
         *              3番目  1:任意の年度による学校名の取得
         */
        private String getSchoolInfoSqlKindai(final Param param) {
            final String q = "?";
            final String key = "FRED";

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.YEAR ");
            sql.append("     ,T1.SCHOOLNAME1");
            sql.append("     ,T1.SCHOOLNAME2");
            sql.append("     ,T1.SCHOOLNAME3");
            sql.append("     ,T1.SCHOOLZIPCD");
            sql.append("     ,T1.SCHOOLDIV ");
            sql.append("     ,T2.STAFFNAME AS PRINCIPAL_NAME ");
            sql.append("     ,T2.JOBNAME AS PRINCIPAL_JOBNAME ");
            sql.append(" FROM ");
            sql.append("     SCHOOL_MST T1 ");
            //JOBCD(校長)で検索
            //  校長
            sql.append(" LEFT JOIN ( ");
            sql.append("     SELECT ");
            sql.append("         '" + key + "' AS KEY,");
            sql.append("         W1.STAFFCD,");
            sql.append("         W2.STAFFNAME,");
            sql.append("         W3.JOBNAME ");
            sql.append("     FROM ");
            sql.append("         STAFF_YDAT W1 ");
            sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
            sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
            sql.append("     WHERE ");
            sql.append("         W1.YEAR = " + q + " AND (W2.JOBCD = '0001' OR W2.JOBCD = '0005') ");
            sql.append(" ) T2 ON '" + key + "' = T2.KEY ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = " + q + " ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND T1.SCHOOL_KIND = '" + "H" + "' ");
            }

            return sql.toString();
        }

        private String getSchoolInfoSql(final Param param) {

            final String q = "?";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("      T1.YEAR ");
            sql.append("    , T2.SCHOOL_NAME AS SCHOOLNAME1 ");
            sql.append("    , T1.SCHOOLNAME2");
            sql.append("    , T1.SCHOOLNAME3");
            sql.append("    , T1.SCHOOLZIPCD");
            sql.append("    , T1.SCHOOLDIV ");
            sql.append("    , T2.SYOSYO_NAME");
            sql.append("    , T2.SYOSYO_NAME2");
            sql.append("    , T2.CERTIF_NO");
            sql.append("    , T2.PRINCIPAL_NAME ");
            sql.append("    , T2.JOB_NAME AS PRINCIPAL_JOBNAME ");
            sql.append("    , T2.REMARK1");
            sql.append("    , T2.REMARK2");
            sql.append("    , T2.REMARK3");
            sql.append("    , T2.REMARK4");
            sql.append("    , T2.REMARK5 ");
            sql.append("    , T2.REMARK6");
            sql.append("    , T2.REMARK7");
            sql.append("    , T2.REMARK8");
            sql.append("    , T2.REMARK9");
            sql.append("    , T2.REMARK10 ");
            sql.append("  FROM ");
            sql.append("       SCHOOL_MST T1 ");
            sql.append("  LEFT JOIN CERTIF_SCHOOL_DAT T2 ON T2.CERTIF_KINDCD = " + q + " AND T1.YEAR = T2.YEAR ");
            sql.append("  WHERE T1.YEAR = " + q + " ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND T1.SCHOOL_KIND = '" + "H" + "' ");
            }
            return sql.toString();
        }


        private List<StudyrecClass> getStudyrecClassList(final DB2UDB db2, final Param param) {
            final List<StudyrecClass> studyrecClassList = new ArrayList<StudyrecClass>();
            if ("1".equals(_certifSchoolOnly)) {
                return studyrecClassList;
            }

            setNotUseClassMstSpecialDiv(db2);
            setGakunenseiTengakuYears(db2, param);

            for (int flg = 0; flg <= 1; flg++) {
                //  学習記録データ
                final String sql = param._knjStudyrecSql.getStudyrecSql(this, flg);
                if (param._isOutputDebugQuery) {
                    log.info(" studyrec sql " + flg + " = " + sql);
                }

                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final String credit = KnjDbUtils.getString(row, "CREDIT");
                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    final String gradeCredit = flg == 1 ? null : KnjDbUtils.getString(row, "GRADE_CREDIT");
                    final String compCredit = flg == 1 ? null : KnjDbUtils.getString(row, "GRADE_COMP_CREDIT");
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    String annual = KnjDbUtils.getString(row, "ANNUAL");
                    final String classname = StringUtils.defaultString(KnjDbUtils.getString(row, "CLASSNAME"));
                    final String subclassname = StringUtils.defaultString(KnjDbUtils.getString(row, "SUBCLASSNAME"));
                    final String specialdiv = flg == 1 ? null : KnjDbUtils.getString(row, "SPECIALDIV");

                    log.debug("ANNUAL="+annual +", CLASSCD="+classcd +", SUBCLASSCD="+subclasscd +", CREDIT="+credit +", GRADE_CREDIT="+gradeCredit);
                    //中高一貫で選択した学年が高校（4・5・6学年）のときは、高校のデータを出力する。
                    if (param._isJuniorHiSchool && _isHiSchool) {
                        int annualInt = Integer.parseInt(annual);
                        if (1 <= annualInt && null != _schoolKindMinGradeCd && annualInt < Integer.parseInt(_schoolKindMinGradeCd)) continue;
                        if (null != _annualGradeCdMap.get(annual)) {
                            annual = _annualGradeCdMap.get(annual);
                        }
                    }

                    if (null == StudyrecClass.getStudyrecClass(studyrecClassList, classcd)) {
                        studyrecClassList.add(new StudyrecClass(classcd, classname, specialdiv));
                    }
                    final StudyrecClass studyrecClass = StudyrecClass.getStudyrecClass(studyrecClassList, classcd);

                    if (null == StudyrecSubclass.getStudyrecSubclass(studyrecClass._studyrecSubclassList, subclasscd)) {
                        studyrecClass._studyrecSubclassList.add(new StudyrecSubclass(subclasscd, subclassname, credit));
                    }
                    final StudyrecSubclass studyrecSubclass = StudyrecSubclass.getStudyrecSubclass(studyrecClass._studyrecSubclassList, subclasscd);
                    if (flg == 0) {
                        if (_gakunenseiTengakuYears.contains(KnjDbUtils.getString(row, "YEAR"))) {
                            continue;
                        }
                        studyrecSubclass._annualCreditMap.put(annual, gradeCredit);
                        studyrecSubclass._annualCompCreditMap.put(annual, compCredit);
                    } else if (flg == 1) {
                        //if (Total.isInList(studyrecClass))
                        studyrecSubclass._annualCreditMap.put(annual, credit);
                    }
                }
            }

            if (param._isOutputDebug) {
                for (final StudyrecClass sc : studyrecClassList) {
                    for (final StudyrecSubclass ss : sc._studyrecSubclassList) {
                        log.info(" " + ss);
                    }
                }
            }

            return studyrecClassList;
        }
    }

    private static class StudyrecClass {
        final String _classcd;
        final String _classname;
        final String _specialdiv;
        final List<StudyrecSubclass> _studyrecSubclassList;
        StudyrecClass(final String classcd, final String classname, final String specialdiv) {
            _classcd = classcd;
            _classname = classname;
            _specialdiv = specialdiv;
            _studyrecSubclassList = new ArrayList<StudyrecSubclass>();
        }

        public String maxSubclassname() {
            String max = null;
            for (final StudyrecSubclass s : _studyrecSubclassList) {
                if (null != s._subclassname) {
                    if (null == max) {
                        max = s._subclassname;
                    } else if (max.compareTo(s._subclassname) < 0) {
                        max = s._subclassname;
                    }
                }
            }
            return max;
        }

        private static StudyrecClass getStudyrecClass(final List<StudyrecClass> list, final String classcd) {
            StudyrecClass studyrecClass = null;
            for (final StudyrecClass s : list) {
                if (s._classcd != null && s._classcd.equals(classcd)) {
                    studyrecClass = s;
                    break;
                }
            }
            return studyrecClass;
        }

        private StudyrecSubclass getTotalCreditSubclass() {
            final StudyrecSubclass total = new StudyrecSubclass("total", "total", null);
            for (final StudyrecSubclass ss : _studyrecSubclassList) {
                for (final String annual : ss._annualCreditMap.keySet()) {
                    final String credit = ss._annualCreditMap.get(annual);
                    total.addCredit(annual, credit);
                }
            }
            return total;
        }

        public String toString() {
            return "StudyrecClass(" + _classcd + ", studyrecSubclassList =" + _studyrecSubclassList + ")";
        }
    }

    private static class Total {

        final PrintData _printData;
        final Param _param;

        String ad_credit = null;              //加算単位数
        String sogo_credit = null;
        String abroad_credit = null;
        String subtotal_credit = null;
        String daiken_credit = null;
        String zenseki_credit = null;
        String kyoto88_credit = null;
        List<String> annualList = new ArrayList<String>();

        StudyrecClass classdaiken = null;
        StudyrecClass classzenseki = null;
        StudyrecClass classsubtotal = null;
        StudyrecClass classsogo = null;
        StudyrecClass classabroad = null;
        StudyrecClass classtokiwahr = null;
        StudyrecClass classKyoto88 = null;

        StudyrecClass classgoukei = new StudyrecClass("合計", "合計", null);
        StudyrecSubclass goukei = new StudyrecSubclass("合計", "合計", null);

        StudyrecClass studyrecLastLineClass = null;

        public Total(final PrintData printData, final Param param) {
            _printData = printData;
            _param = param;
        }

        public boolean isPrintDaiken() {
            return toInt(daiken_credit, -1) > 0;
        }

        public boolean isPrintZenseki() {
            return toInt(zenseki_credit, -1) > 0;
        }

        public boolean isPrintLastLineClass() {
            return null != _printData._lastLineClasscd && null != studyrecLastLineClass;
        }

        public boolean isPrintAbroad() {
            return !_param._isSakae && (!_param._isHirokoudai ||  _param._isHirokoudai && NumberUtils.isDigits(abroad_credit) && Integer.parseInt(abroad_credit) > 0);
        }

        public boolean isPrintSubtotal() {
            return !(_param._isSakae || _param._isHirokoudai || _param._isKwansei);
        }

        public boolean isPrintKyotoClass88() {
            return _param._isKyoto && null != classKyoto88;
        }

        public static boolean isInList(final StudyrecClass studyrecClass) {
            final boolean isInList = studyrecClass._classname.equals(StudyrecSql.daiken) ||
            studyrecClass._classname.equals(StudyrecSql.zenseki) ||
            studyrecClass._classname.equals(StudyrecSql.total) || // 小計
            studyrecClass._classname.equals(StudyrecSql.sogo) ||
            studyrecClass._classname.equals(StudyrecSql.abroad) ||
            studyrecClass._classname.equals(StudyrecSql.tokiwahr) ||
            studyrecClass._classname.equals(StudyrecSql.kyoto88);
            return isInList;
        }

        public void process(final Param param, final List<StudyrecClass> studyrecClassList) {

            final Set annualSet = new TreeSet();
            final String KEY_TOTAL_ANNUAL = "000";

            for (final Iterator<StudyrecClass> it = studyrecClassList.iterator(); it.hasNext();) {
                final StudyrecClass studyrecClass = it.next();

                if (null != _printData._lastLineClasscd && _printData._lastLineClasscd.equals(studyrecClass._classcd)) {
                    studyrecLastLineClass = studyrecClass;
                    it.remove();
                    continue;
                }

                if (!isInList(studyrecClass)) {
                    continue;
                }

                for (final StudyrecSubclass studyrecSubclass : studyrecClass._studyrecSubclassList) {

                    annualSet.addAll(studyrecSubclass._annualCreditMap.keySet());

                    String credit = null; // KNJ_StudyrecSqlの仕様に基づく
                    if (studyrecClass._classname.equals(StudyrecSql.tokiwahr) || studyrecClass._classname.equals(StudyrecSql.kyoto88)) {
                        for (final String annual : studyrecSubclass._annualCreditMap.keySet()) {
                            final String creditYear = studyrecSubclass._annualCreditMap.get(annual);
                            credit = addNumber(credit, creditYear);
                        }
                    } else {
                        credit = studyrecSubclass._annualCreditMap.get(KEY_TOTAL_ANNUAL);
                    }
                    boolean isAdd = false;
                    if (credit != null) {
                        if (studyrecClass._classname.equals(StudyrecSql.daiken)) {
                            daiken_credit = credit;
                            classdaiken = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.zenseki)) {
                            zenseki_credit = credit;
                            classzenseki = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.sogo)) {
                            isAdd = true;
                            addCredit(studyrecClass, goukei);
                            sogo_credit = credit;
                            classsogo = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.abroad)) {
                            isAdd = true;
                            addCredit(studyrecClass, goukei);
                            abroad_credit = credit;
                            classabroad = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.total)) {
                            isAdd = true;
                            addCredit(studyrecClass, goukei);
                            subtotal_credit = credit;
                            classsubtotal = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.tokiwahr)) {
                            isAdd = true;
                            addCredit(studyrecClass, goukei);
                            classtokiwahr = studyrecClass;
                        } else if (studyrecClass._classname.equals(StudyrecSql.kyoto88)) {
                            isAdd = true;
                            addCredit(studyrecClass, goukei);
                            kyoto88_credit = credit;
                            classKyoto88 = studyrecClass;
                        }
                    }
                    if (isAdd) {
                        ad_credit = addNumber(ad_credit, credit);
                    }
                }
                it.remove(); // studyrecClassListから除く
            }
            annualSet.remove(KEY_TOTAL_ANNUAL);
            annualList = new ArrayList<String>(annualSet);
        }

        private static void addCredit(final StudyrecClass src, final StudyrecSubclass dest) {
            if (null == src) {
                return;
            }
            for (final StudyrecSubclass sub : src._studyrecSubclassList) {
                for (final String annual : sub._annualCreditMap.keySet()) {
                    final String credit = sub._annualCreditMap.get(annual);
                    dest.addCredit(annual, credit);
                }
            }
        }
    }

    private static class StudyrecSubclass {
        final String _subclasscd;
        final String _subclassname;
        final String _credit;
        final Map<String, String> _annualCreditMap = new HashMap<String, String>();
        final Map<String, String> _annualCompCreditMap = new HashMap<String, String>();
        StudyrecSubclass(
                final String subclasscd,
                final String subclassname,
                final String credit) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credit = credit;
        }
        public void addCredit(final String annual, final String credit) {
            String newcre = null;
            if (null == _annualCreditMap.get(annual)) {
                newcre = credit;
            } else {
                final int oldcre = Integer.parseInt(_annualCreditMap.get(annual));
                newcre = String.valueOf(oldcre + (NumberUtils.isDigits(credit) ? Integer.parseInt(credit) : 0));
            }
            _annualCreditMap.put(annual, newcre);
        }
        public Integer getCompCreditSum() {
            return Utils.integerSum(Utils.toIntegerList(_annualCompCreditMap.values()));
        }
        private static StudyrecSubclass getStudyrecSubclass(final List<StudyrecSubclass> list, final String subclasscd) {
            StudyrecSubclass studyrecSublass = null;
            for (final StudyrecSubclass s : list) {
                if (s._subclasscd != null && s._subclasscd.equals(subclasscd)) {
                    studyrecSublass = s;
                    break;
                }
            }
            return studyrecSublass;
        }
        public String toString() {
            return "StudyrecSubclass(" + _subclasscd + ":" + _subclassname + ", " + _annualCreditMap + ")";
        }
    }

    private static class Form {

        boolean _hasData;
        final Param _param;
        final Vrw32alp _svf;      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean _isGrd;
        boolean _isKenja;

        private String _currentForm;
        private Map<String, Map<String, SvfField>> _formFieldMap = new HashMap<String, Map<String, SvfField>>();

        Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        public void print(final DB2UDB db2, final PrintData printData) {
            _hasData = false;

            final String formname = getForm(printData);
            log.info(" formname = " + formname);
            int rtn = _svf.VrSetForm(formname, 4);
            if (rtn == -1) {
                log.error("not found form : " + formname);
                return;
            }
            setFormInfo(formname);

            printSchoolInfo(printData);  // 学校情報出力処理

            printPersonalInfo(db2, printData); //氏名、住所等出力

            printRecord(db2, printData); //学習の記録出力
        }

        private String modifyForm(final String formname, final Param param, final PrintData printData) {
            final List<String> flgList = getModifyFlgList(param, printData);
            final String formCreateFlg = mkString(flgList, "").toString();
            if (param._isOutputDebug) {
                log.info(" form config Flg = " + formCreateFlg);
            }
            if (StringUtils.isEmpty(formCreateFlg)) {
                return formname;
            }
            if (null != param._createFormFiles.get(formCreateFlg)) {
                return param._createFormFiles.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(_svf.getPath(formname)));
                if (svfForm.readFile()) {
                    modifyForm(param, flgList, svfForm);

                    final File newFormFile = svfForm.writeTempFile();
                    param._createFormFiles.put(formCreateFlg, newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            File newFormFile = param._createFormFiles.get(formCreateFlg);
            if (null != newFormFile) {
                return newFormFile.getName();
            }
            return formname;
        }

        private final String FLG_MOVE_FIELD_LEFT_STAMP = "FLG_MOVE_FIELD_LEFT_STAMP";
        private final String FLG_MOVE_STAMP_KEISHO = "FLG_MOVE_STAMP_KEISHO";
        private final String FLG_RESIZE_STAMP_SIZE  = "FLG_RESIZE_STAMP_SIZE";
        private final String FLG_MOVE_STAMP_SIZE  = "FLG_MOVE_STAMP_SIZE";
        private final String FLG_ADD_FIELD_NAME_KANA  = "FLG_ADD_FIELD_NAME_KANA";
        private final String FLG_MOVE_FIELD_DATE_REITAKU  = "FLG_MOVE_FIELD_DATE_REITAKU";
        private final String FLG_MOVE_FIELD_DATE_KWANSEI  = "FLG_MOVE_FIELD_DATE_KWANSEI";
        private final String FLG_HEADER_RYUKEI  = "FLG_HEADER_RYUKEI";
        private final String FLG_PRINT_DAY_OF_ENT_DATE_GRD_DATE  = "FLG_PRINT_DAY_OF_ENT_DATE_GRD_DATE";
        private List<String> getModifyFlgList(final Param param, final PrintData printData) {
            final List<String> flgList = new ArrayList<String>();
            if (param._isKeisho) {
                flgList.add(FLG_MOVE_STAMP_KEISHO);
            } else {
                if (NumberUtils.isNumber(_param._stampPositionXmmTanniShutokuShomeisho) || NumberUtils.isNumber(_param._stampPositionXmmTanniShutokuShomeisho)) {
                    flgList.add(FLG_MOVE_STAMP_SIZE);
                }
                if (param._isReitaku) {
                    flgList.add(FLG_ADD_FIELD_NAME_KANA);
                    flgList.add(FLG_MOVE_FIELD_DATE_REITAKU);
                } else if ((_isKenja && printData._isPrintStamp || param._isMatsudo) && !flgList.contains(FLG_MOVE_STAMP_SIZE)) {
                    flgList.add(FLG_MOVE_FIELD_LEFT_STAMP);
                }
                if (!StringUtils.isBlank(param._stampSizeMm)) {
                    flgList.add(FLG_RESIZE_STAMP_SIZE);
                }
            }
            if (_param._isKwansei) {
                flgList.add(FLG_MOVE_FIELD_DATE_KWANSEI);
            } else if (_param._isRyukei) {
                flgList.add(FLG_HEADER_RYUKEI);
            }
            if (_param._isPrintDayOfEntDateGrdDate) {
                flgList.add(FLG_PRINT_DAY_OF_ENT_DATE_GRD_DATE);
            }
            return flgList;
        }

        private void modifyForm(final Param param, final List<String> flgList, final SvfForm svfForm) {
            if (flgList.contains(FLG_ADD_FIELD_NAME_KANA)) {
                for (final String nameFieldname : Arrays.asList("NAME", "NAME2", "NAME3")) {
                    final SvfForm.Field field = svfForm.getField(nameFieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addY(25).setCharPoint10(field._charPoint10 - 7));
                    }
                }
                svfForm.addField(new SvfForm.Field(null, "KANA", SvfForm.Font.Mincho, 32, 1185, false, new SvfForm.Point(563, 598), 70, "ふりがな 32桁"));
                svfForm.addField(new SvfForm.Field(null, "KANA2", SvfForm.Font.Mincho, 40, 1207, false, new SvfForm.Point(563, 604), 58, "ふりがな2 40桁"));
                svfForm.addField(new SvfForm.Field(null, "KANA3", SvfForm.Font.Mincho, 48, 1204, false, new SvfForm.Point(563, 609), 48, "ふりがな3 48桁"));
            }
            if (flgList.contains(FLG_MOVE_STAMP_KEISHO)) {
                final SvfForm.ImageField imageField = svfForm.getImageField("STAMP");
                if (null != imageField) {
                    final int addx = Utils.mmToDot("-5"); // 左へ5mm
                    final int addy = Utils.mmToDot("10"); // 下へ10mm
                    svfForm.move(imageField, imageField.addX(addx).addY(addy));
                }
                final SvfForm.Field dateField = svfForm.getField("DATE");
                if (null != dateField) {
                    final int addy = Utils.mmToDot("-0.5"); // 上へ0.5mm
                    svfForm.removeField(dateField);
                    svfForm.addField(dateField.setY(dateField._position._y + addy));
                }
            }
            if (flgList.contains(FLG_MOVE_FIELD_LEFT_STAMP)) {
                final Map<String, Integer> moveXmap = new HashMap<String, Integer>();
                moveXmap.put("DATE", -450);
                moveXmap.put("SCHOOLNAME", -400);
                moveXmap.put("JOBNAME", -250);
                moveXmap.put("STAFFNAME", -250);
                for (final String fieldname : moveXmap.keySet()) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null == field) {
                        log.info(" no such field : " + field);
                    } else {
                        final int moveX = moveXmap.get(fieldname);
                        svfForm.removeField(field);
                        svfForm.addField(field.setX(field._position._x + moveX).setEndX(field._endX + moveX));
                    }
                }
            }
            if (flgList.contains(FLG_RESIZE_STAMP_SIZE) || flgList.contains(FLG_MOVE_STAMP_SIZE)) {
                resizeStampImage(param, svfForm);
            }
            if (flgList.contains(FLG_MOVE_FIELD_DATE_REITAKU)) {
                SvfForm.Field DATE = svfForm.getField("DATE");
                if (null != DATE) {
                    svfForm.removeField(DATE);
                    svfForm.addField(DATE.addY(-40).addX(-200).setPrintMethod(SvfForm.Field.PrintMethod.HIDARITSUME));
                    svfForm.addField(DATE.copyTo("SCHOOL_ADDRESS").addX(-200).addY(170).setCharPoint10(100).setFieldLength(60).setPrintMethod(SvfForm.Field.PrintMethod.MUHENSHU).setEndX(DATE._endX - 200));
                }

                SvfForm.Field SCHOOLNAME = svfForm.getField("SCHOOLNAME");
                if (null != SCHOOLNAME) {
                    svfForm.removeField(SCHOOLNAME);
                    svfForm.addField(SCHOOLNAME.addX(-400).addY(-20));
                }

                for (final String nameFieldname : Arrays.asList("JOBNAME", "STAFFNAME")) {
                    final SvfForm.Field field = svfForm.getField(nameFieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addY(70));
                    }
                }
            }
            if (flgList.contains(FLG_MOVE_FIELD_DATE_KWANSEI)) {
                final SvfForm.Field DATE = svfForm.getField("DATE");
                final SvfForm.Field CLASSNAME1 = svfForm.getField("CLASSNAME1");
                if (null != DATE) {
                    svfForm.removeField(DATE);
                    svfForm.addField(DATE.setX(CLASSNAME1._position._x).setPrintMethod(SvfForm.Field.PrintMethod.HIDARITSUME));
                }
            }
            if (flgList.contains(FLG_HEADER_RYUKEI)) {
                for (final String fieldname : Arrays.asList("schregno", "NENDO")) {
                    final SvfForm.Field f = svfForm.getField(fieldname);
                    if (null != f) {
                        svfForm.removeField(f);
                        svfForm.addField(f.addX(10000));
                    }
                }
                final SvfForm.Field fieldNAME = svfForm.getField("NAME");
                if (null != fieldNAME) {
                    final int x = new BigDecimal(Utils.mmToDot("35.0")).subtract(svfForm.getAdjtX()).intValue();
                    final int y = new BigDecimal(Utils.mmToDot("12.0")).subtract(svfForm.getAdjtY()).intValue();
                    final int width = 160;
                    final int charPoint10 = 90;
                    for (final SvfForm.Field f : Arrays.asList(
                            fieldNAME.copyTo("HEADER_SCHREGNO").setX(x).setY(y).setFieldLength(8).setCharPoint10(charPoint10).setEndX(x + width)
                          , fieldNAME.copyTo("HEADER_NAME").setX(x + 50).setY(y + 80).setFieldLength(30).setCharPoint10(charPoint10).setEndX(x + 50 + width)
                          , fieldNAME.copyTo("HEADER_DATE").setX(x).setY(y + 80 * 2).setFieldLength(16).setCharPoint10(charPoint10).setEndX(x + width)
                            )) {
                        svfForm.addField(f);
                    }
                }
            }
            if (flgList.contains(FLG_PRINT_DAY_OF_ENT_DATE_GRD_DATE)) {
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

        protected void resizeStampImage(final Param param, final SvfForm svfForm) {
            final SvfForm.ImageField image = svfForm.getImageField("STAMP");
            if (null == image) {
                return;
            }
            final int l = NumberUtils.isNumber(param._stampSizeMm) ? Utils.mmToDot(param._stampSizeMm) : image._endX - image._point._x;
            final int x;
            if (NumberUtils.isNumber(param._stampPositionXmmTanniShutokuShomeisho)) {
                x = Utils.mmToDot(param._stampPositionXmmTanniShutokuShomeisho);
            } else {
                x = image._point._x;
            }
            final int y;
            if (NumberUtils.isNumber(param._stampPositionYmmTanniShutokuShomeisho)) {
                y = Utils.mmToDot(param._stampPositionYmmTanniShutokuShomeisho);
            } else {
                y = image._point._y;
            }
            final int endX = x + l;
            final int endY = y + l;
            final int centerX = (x + endX) / 2;
            final int centerY = (y + endY) / 2;
            final int newX = centerX - l / 2;
            final int newY = centerY - l / 2;
            final int newEndX = centerX + l / 2;
            final int newHeight = l;

            final SvfForm.ImageField newImage = image.setFieldname("STAMP").setX(newX).setY(newY).setEndX(newEndX).setHeight(newHeight);
            svfForm.removeImageField(image);
            svfForm.addImageField(newImage);
            if (param._isOutputDebug) {
                log.info("move stamp (" + x + ", " + y + ", len = " + image._height + ") to (" + newX + ", " + newY + ", len = " + l + ")");
            }
        }

        private String getForm(final PrintData printData) {
            _isKenja = false;
            String formname;
            if (_param._isKindai) {
                formname = "KNJG030_KIN.frm";
            } else if (_param._isKaijyo) {
                formname = "KNJG030_KAIJYO.frm";
            } else if (_param._isKyoai) {
                formname = "KNJG030_KYOAI.frm";
            } else if (_param._isTottori || _param._isChukyo) {
                formname = "KNJG030_TORI.frm";
            } else if (_param._isTokiwa) {
                formname = "KNJG030_TOKIWA.frm";
            } else if (_param._isSapporo) {
                if (printData.isEng()) {
                    formname = "KNJG030_2SAP.frm";
                } else {
                    formname = "KNJG030_SAP.frm";
                }
            } else if (_param._isOsakatoin) {
                formname = "KNJG030_TOIN.frm";
            } else if (_param._isSakae) {
                formname = "KNJG030_SAKAE.frm";
            } else if (_param._isKyoto) {
                formname = "KNJG030_KYOTO.frm";
            } else if (_param._isHirokoudai) {
                formname = "KNJG030_HIROKOUDAI.frm";
            } else if (_param._isKwansei) {
                formname = "KNJG030_KWANSEI.frm";
            } else {
                formname = "KNJG030.frm";
                _isKenja = true;
            }
            formname = modifyForm(formname, _param, printData);
            return formname;
        }

        private void setFormInfo(final String formname) {
            if (null == _currentForm || !_currentForm.equals(formname)) {
                _currentForm = formname;
                Map<String, SvfField> fieldMap = new HashMap<String, SvfField>();
                try {
                    fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(_svf);
                } catch (Throwable t) {
                    log.error("exception! = " + t);
                }
                _formFieldMap.put(formname, fieldMap);
            }
            try {
                final String path = _svf.getPath(_currentForm);
                SvfForm f = null;
                if (null != path) {
                    f = new SvfForm(new File(path));
                    if (f.readFile()) {
                        SvfForm.SubForm SUBFORM1 = f.getSubForm("SUBFORM1");
                        if (null != SUBFORM1) {
                            final int SUBFORM1_X = SUBFORM1._point1._x;
                            if (_param._isRisshisha) {
                                for (final String fieldname : Arrays.asList("DATE", "SCHOOLNAME", "JOBNAME", "STAFFNAME")) {
                                    _svf.VrAttribute(fieldname, "Hensyu=0,X=" + String.valueOf(SUBFORM1_X)); // 無編集、位置は左端
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                log.error("exception! = " + t);
            }
        }

        private boolean hasField(final String fieldname) {
            try {
                SvfField field = getMappedMap(_formFieldMap, _currentForm).get(fieldname);
                return null != field;
            } catch (Throwable t) {
                log.error("getFieldKeta exception = " + t);
            }
            return false;
        }

        private String getFieldPoint(final String fieldname) {
            try {
                SvfField field = getMappedMap(_formFieldMap, _currentForm).get(fieldname);
                return (String) field.getAttributeMap().get(SvfField.AttributeSize);
            } catch (Throwable t) {
                log.error("getFieldKeta exception = " + t);
            }
            return null;
        }

        private int getFieldY(final String fieldname) {
            try {
                SvfField field = getMappedMap(_formFieldMap, _currentForm).get(fieldname);
                return Integer.parseInt((String) field.getAttributeMap().get("Y"));
            } catch (Throwable t) {
                log.error("getFieldKeta exception = " + t + " (" + fieldname + ")");
            }
            return -1;
        }

        private int getFieldKeta(final String fieldname) {
            try {
                SvfField field = getMappedMap(_formFieldMap, _currentForm).get(fieldname);
                return field._fieldLength;
            } catch (Throwable t) {
                log.error("getFieldKeta exception = " + t + " (" + fieldname + ")");
            }
            return -1;
        }

        private int VrsOutForData(final List<String> fields, final String data) {
            return VrsOut(getFieldForData(fields, data), data);
        }

        private int VrsOut(final String field, final String data) {
            if (_param._isOutputDebug) {
                if (null == field || null == getMappedMap(_formFieldMap, _currentForm).get(field)) {
                    log.warn(" no field " + quote(field) + ", data = " + quote(data) + "");
                    return -1;
                }
            }
            if (_param._isOutputDebugField) {
                log.info("VrsOut(" + quote(field) + ", " + quote(data) + ")");
            }
            return _svf.VrsOut(field, data);
        }

        private int VrsOutn(final String field, final int n, final String data) {
            if (_param._isOutputDebug) {
                if (null == field || null == getMappedMap(_formFieldMap, _currentForm).get(field)) {
                    log.warn(" no field " + quote(field) + ", data =" + quote(data) + "");
                    return -1;
                }
            }
            if (_param._isOutputDebugField) {
                log.info("VrsOutn(" + quote(field) + ", " + n + ", " + quote(data) + ")");
            }
            return _svf.VrsOutn(field, n, data);
        }

        private int VrAttribute(final String field, final String attribute) {
            if (_param._isOutputDebug) {
                if (null == field || null == getMappedMap(_formFieldMap, _currentForm).get(field)) {
                    log.warn(" no field " + quote(field) + ", attribute = " + quote(attribute) + "");
                    return -1;
                }
            }
            if (_param._isOutputDebugField) {
                log.info("VrAttribute(" + quote(field) + ", " + quote(attribute) + ")");
            }
            return _svf.VrAttribute(field, attribute);
        }

        private String quote(final String d) {
            return null == d ? "" : "\"" + d + "\"";
        }

        private int VrEndRecord() {
            if (_param._isOutputDebugField) {
                log.info("endRecord.");
            }
            return _svf.VrEndRecord();
        }

        /*
         *  [東京都用様式] 学校情報
         */
        private void printSchoolInfo(final PrintData printData) {
            boolean hasData = false;
            if (_param._isOutputDebug) {
                log.info(" schoolinfo map = " + printData._schoolInfo);
            }
            if (null != printData._schoolInfo) {
                final String schoolname1 = KnjDbUtils.getString(printData._schoolInfo, "SCHOOLNAME1");
                final String jobname = KnjDbUtils.getString(printData._schoolInfo, "PRINCIPAL_JOBNAME");
                final String principalName = KnjDbUtils.getString(printData._schoolInfo, "PRINCIPAL_NAME");
                if (_param._isKindai) {
                    VrsOut("nendo", printData._nendo);  //年度
                    hasData = true;
                    VrsOut("NAME1", schoolname1); //学校名
                    if (principalName != null) {
                        VrsOut("STAFFNAME", StringUtils.defaultString(jobname, "　　　") + "  " + principalName);   //校長名
                    }
                } else {
                    VrsOut("NENDO",  printData._nendo);  // 年度
                    hasData = true;
                    VrsOut("SCHOOLNAME", schoolname1);  // 学校名
                    VrsOut("JOBNAME", jobname); // 校長職名
                    VrsOut("STAFFNAME", principalName); // 校長名

                    final String remark2 = KnjDbUtils.getString(printData._schoolInfo, "REMARK2");
                    if (null != remark2 && null != printData._seki) {
                        VrsOut("STAFFNAME2", remark2 + "　" + StringUtils.defaultString(printData._sekiStaffname, "　　　　　") + "　印"); // 校長名
                    }
                    VrsOut("CORP_NAME", KnjDbUtils.getString(printData._schoolInfo, "REMARK3")); // 学校法人名

                    final String remark;
//                    if (printData._isFuhakkou) {
//                        final String elapsedYears = (String) printData._outputFuhakkouResult.get("CERTIF_KIND_MST.ELAPSED_YEARS");
//                        remark = "法令で定められた保存期間（卒業後" + StringUtils.defaultString(elapsedYears) + "年）が経過しているため、証明できません。";
//                    } else {
                        remark = KnjDbUtils.getString(printData._schoolInfo, "REMARK4");
//                    }
                    VrsOut("REMARK", remark); // 備考

                    if (_param._isReitaku) {
                        VrsOut("SCHOOL_ADDRESS", KnjDbUtils.getString(printData._schoolInfo, "REMARK5")); // 学校住所
                    }
                }
            }
            if (hasData) {
                final String dateStr;
                if (printData.isEng()) {
                    dateStr = h_format_US(printData._date, d_MMMM_yyyy);
                } else {
                    dateStr = StringUtils.defaultString(printData._dateString, "　　年　 月　 日");
                }
                VrsOut("DATE", dateStr);   //記載日
            }
            if (printData._isPrintStamp) {
                VrsOut("STAMP", printData._certifSchoolstampImagePath); // 校印
            }
        }

        /*
         * SVF-FORM フィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
         */
        private void svfFieldAttribute(
                final String fieldname,
                final String subclassname,
                final int width,
                final int height,
                final int charHeight,
                final int ystart,
                final int minnum,
                final int maxnum,
                final int ln,
                final int yMaxLine
        ) {
            KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldname, width, charHeight, ystart, minnum, maxnum);
            int yLine = ln;
            while (yLine >= yMaxLine) {
                yLine -= yMaxLine;
            }
            final float charSize = modify.getCharSize(subclassname);
            VrAttribute(fieldname, "Keta=" + String.valueOf(maxnum));
            VrAttribute(fieldname, "Y=" + (int) modify.getYjiku(yLine, charSize, height));
            VrAttribute(fieldname, "Size=" + new BigDecimal(charSize).setScale(1, BigDecimal.ROUND_DOWN));
        }

        /**
         *  個人情報
         */
        private void printPersonalInfo(final DB2UDB db2, final PrintData printData) {
            final Map personalInfo = printData._personalInfo;
            if (null == personalInfo) {
                return;
            }

            if (_param._isMieken) {
                VrsOut("NAME_HEADER", "名　前");
            } else if (_param._isKyoto) {
                VrsOut("NAME_HEADER", "氏　　名");
            }

            final String annual = KnjDbUtils.getString(personalInfo, "ANNUAL");
            final String graduName = KnjDbUtils.getString(personalInfo, "GRADU_NAME");
            final String birthdayStr = getBirthdayStr(db2, printData, _param, KnjDbUtils.getString(personalInfo, "BIRTHDAY"), KnjDbUtils.getString(personalInfo, "BIRTHDAY_FLG"));
            final String graduDate = KnjDbUtils.getString(personalInfo, "GRADU_DATE");
            if (!_param._isKindai) {

//		        private String syoshonum;
                final String syoshoname = StringUtils.defaultString(KnjDbUtils.getString(printData._schoolInfo, "SYOSYO_NAME")); //証書名
                final String syoshoname2 = StringUtils.defaultString(KnjDbUtils.getString(printData._schoolInfo, "SYOSYO_NAME2")); //証書名２
                final boolean isOutputCertifNo = "0".equals(KnjDbUtils.getString(printData._schoolInfo, "CERTIF_NO"));  //証書番号の印刷 0:あり,1:なし

                final String certifName;
                if (isOutputCertifNo) {
                    certifName =  syoshoname + (StringUtils.isBlank(printData._certifNumber) ? "     " : printData._certifNumber) + syoshoname2; // 証明書番号が無い場合 5スペース挿入
                } else {
                    certifName =  syoshoname + "     " + syoshoname2;
                }
                VrsOut("SYOSYO_NAME", certifName);  //証書番号
                if (hasField("NENDO_NAME")) {
                    VrsOut("NENDO_NAME", printData._certifNumber + syoshoname);  //証明書番号
                }
            } else {
                VrsOut("CERTIF_NO", printData._certifNumber);         //証明書番号
            }

            VrsOut("schregno", printData._schregno);       //学籍番号

            if (_param._isRyukei) {
                VrsOut("HEADER_SCHREGNO", printData._schregno);
                VrsOut("HEADER_NAME", printData.getStudentName(_param));
                VrsOut("HEADER_DATE", KNJ_EditDate.h_format_SeirekiJP(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            }

            //生徒氏名
            final String name = printData.getStudentName(_param);
            if (name != null) {
                final String nameField;
                if (_param._isKindai) {
                    nameField = "name";
                } else if (_param._isKyoai || _param._isTottori || _param._isChukyo) {
                    nameField = "NAME";
                } else {
                    nameField = getFieldForData(Arrays.asList("NAME", "NAME2", "NAME3"), name);
                }
                VrsOut(nameField, name);
            }

            //フリガナ
            final String nameKana = ("1".equals(KnjDbUtils.getString(personalInfo, "USE_REAL_NAME")) || "1".equals(_param._certifPrintRealName)) ? KnjDbUtils.getString(personalInfo, "REAL_NAME_KANA") : KnjDbUtils.getString(personalInfo, "NAME_KANA");
            if (nameKana != null) {
                VrsOut(getFieldForData(Arrays.asList("KANA", "KANA2", "KANA3"), nameKana), nameKana);
            }

            // 生年月日
            if (_param._isSapporo && printData.isEng()) {
                VrsOut("BIRTHDAY", "D.O.B. " + birthdayStr);
            } else {
                VrsOut("BIRTHDAY", birthdayStr);
            }

            // 入学年月
            printData._entDate = KnjDbUtils.getString(personalInfo, "ENT_DATE");
            if (_param._isKindai) {
                VrsOut("YEAR1", formatDate(db2, _param, printData._entDate));
            } else {
                final String year1;
                if (_param._isHirokoudai || _param._isKwansei || _param._isPrintDayOfEntDateGrdDate) {
                    year1 = getNentukihiStr(db2, _param, printData._entDate);
                } else {
                    year1 = getNentukiStr(db2, _param, printData._entDate);
                }
                VrsOut("YEAR1", year1);
            }

            if (_param._isKindai) {
                if (annual     != null) {
                    VrsOut("GRADE1",     String.valueOf(Integer.parseInt(annual)) + (printData.isGakunensei(_param) ? "年生" : "年次"));
                }

                VrsOut("YEAR2",  formatDate(db2, _param, graduDate));
                VrsOut("TRANSFER2",  graduName);
            } else {
                final String gradeField = "GRADE1";
                if (annual != null) {
                    final String nen;
                    final String gradeCd = KnjDbUtils.getString(personalInfo, "GRADE_CD");
                    if (_param._useGradeCdAsPrintGrade) {
                        nen = NumberUtils.isDigits(gradeCd) ? Integer.valueOf(gradeCd).toString() : " ";
                    } else {
                        int annualInt  = Integer.parseInt(annual);
                        if (_param._isJuniorHiSchool && NumberUtils.isDigits(gradeCd)) {
                            annualInt = Integer.parseInt(gradeCd);
                        }
                        nen = String.valueOf(annualInt);
                    }
                    VrsOut(gradeField, nen + (printData.isGakunensei(_param) ? "年生" : "年次"));
                }

                if (_param._isKwansei) {
                    final String grdDiv = KnjDbUtils.getString(personalInfo, "GRD_DIV");
                    final String printDate;
                    final String transfer2;
                    if (null == grdDiv || "4".equals(grdDiv)) {
                        transfer2 = "在学中";
                        printDate = graduDate;
                        VrsOut(gradeField,  ""); //卒業生は学年を表示しない
                    } else {
                        transfer2 = graduName;
                        printDate = printData._date;
                    }
                    VrsOut("TRANSFER2", transfer2);
                    VrsOut("YEAR2", getNentukihiStr(db2, _param, printDate));

                } else if (graduName != null && !graduName.equals("卒業見込み")) {
                    VrsOut("TRANSFER2", graduName);
                    if (graduDate != null) { // 卒業年月
                        final String year2;
                        if (_param._isHirokoudai || _param._isPrintDayOfEntDateGrdDate) {
                            year2 = getNentukihiStr(db2, _param, graduDate);
                        } else {
                            year2 = getNentukiStr(db2, _param, graduDate);
                        }
                        VrsOut("YEAR2", year2);
                    }
                    if (_param._isTottori || _param._isChukyo || _param._isKyoai) {
                        VrsOut(gradeField, graduName); //鳥取は卒業区分名称を表示する
                    } else {
                        VrsOut(gradeField,  ""); //卒業生は学年を表示しない
                    }
                }

            }

            if (!_param._isKindai) {
                if (KnjDbUtils.getString(personalInfo, "ENT_DIV") != null) {
                    final String transfer1;
                    if ("4".equals(KnjDbUtils.getString(personalInfo, "ENT_DIV")) || "5".equals(KnjDbUtils.getString(personalInfo, "ENT_DIV"))) {
                        transfer1 = KnjDbUtils.getString(personalInfo, "ENTER_NAME");
                    } else {
                        transfer1 = "入学";
                    }
                    VrsOut("TRANSFER1", transfer1);
                }
            } else {
                if (KnjDbUtils.getString(personalInfo, "ENTER_NAME") != null) {
                    final String transfer1;
                    if (-1 < KnjDbUtils.getString(personalInfo, "ENTER_NAME").lastIndexOf("入学")) {    //05/08/04
                        transfer1 = "入学";                //05/08/04
                    } else {
                        transfer1 = KnjDbUtils.getString(personalInfo, "ENTER_NAME");
                    }
                    VrsOut("TRANSFER1", transfer1);
                }
            }

            VrsOut("KATEI", KnjDbUtils.getString(personalInfo, "COURSENAME"));
            String majorname = KnjDbUtils.getString(personalInfo, "MAJORNAME");
            if ("1".equals(_param._tannishutokushoumeishoPrintCoursecodename)) {
                if (!personalInfo.containsKey("COURSECODEABBV1")) {
                    log.error(" not contained " + "COURSECODEABBV1" + " in " + personalInfo.keySet());
                } else {
                    majorname = StringUtils.defaultString(majorname) + StringUtils.defaultString(KnjDbUtils.getString(personalInfo, "COURSECODEABBV1"));
                }
            }
            if (!StringUtils.isBlank(majorname)) {
                final String majorField;
                if (_param._isMusashinohigashi) {
                    majorField = "gakka4";
                } else if (_param._isKindai || _param._isKyoai || _param._isTottori || _param._isChukyo) {
                    majorField = "GAKKA";
                } else {
                    majorField = getFieldForData(Arrays.asList("GAKKA", "gakka2", "gakka6", "gakka7"), majorname);
                }
                VrsOut(majorField, majorname);
            }

            if (_param._isKindai) {
                attend_outKindai(db2, printData);
            }

            if (CERTIF_KINDCD_17_ENG.equals(printData._certifKindCd)) {
                VrsOut("TITLE", "Certificate of Credit Completion");  //表題
                if (_param._isSapporo) {
                    VrsOut("CERT_HEADER",  "This document certifies that the abovementioned student has completed");
                    VrsOut("CERT_HEADER2", "the below classes at this school.");
                }
            } else if (CERTIF_KINDCD_35_ENG_MIKOMI.equals(printData._certifKindCd)) {
                VrsOut("TITLE", "Certificate of Expected Completion of Credits");  //表題
                if (_param._isSapporo) {
                    VrsOut("CERT_HEADER",  "This document certifies that the abovementioned student is expected to");
                    VrsOut("CERT_HEADER2", "complete the below classes at this school.");
                }
            } else if (CERTIF_KINDCD_18_MIKOMI.equals(printData._certifKindCd)) {
                VrsOut("TITLE", "単位修得見込証明書");  //表題
                if (_param._isSapporo) {
                    VrsOut("CERT_HEADER", "本校において、次の教科及び科目の修得見込があることを、証明する。");
                } else {
                    VrsOut("NOTE", "上記の者は、各教科・科目の単位を修得見込であることを証明する");
                }
            } else if (_param._isHirokoudai) {
                if (CERTIF_KINDCD_RISHU_TANNI_SHOMEISHO.equals(printData._certifKindCd)) {
                    VrsOut("TITLE", "単位履修証明書");
                    VrsOut("CREDIT_TITLE", "履修単位数");
                } else {
                    VrsOut("TITLE", "単位修得証明書");
                    VrsOut("CREDIT_TITLE", "修得単位数");
                }
            } else if (!_param._isKindai) {
                VrsOut("TITLE", "単位修得証明書");        //表題
                if (_param._isSapporo) {
                    VrsOut("CERT_HEADER", "本校において、次の教科及び科目の単位を修得したことを、証明する。");
                } else if (_param._isKaijyo || _param._isSakae) {
                    VrsOut("NOTE", "上記の通り証明します。");
                } else {
                    VrsOut("NOTE", "上記の者は、各教科・科目の単位を修得したことを証明する");
                }
            }
        }

        private String getFieldForData(final List<String> fields, final String data) {
            final int dataKeta = KNJ_EditEdit.getMS932ByteLength(data);
            String formFieldName = null;
            String ketteiField = null;
            String kouho = null;
            for (int i = 0; i < fields.size(); i++) {
                final int fieldKeta = getFieldKeta(fields.get(i));
                if (0 < fieldKeta) {
                    if (dataKeta <= fieldKeta) {
                        ketteiField = fields.get(i);
                        break;
                    }
                    kouho = fields.get(i);
                }
            }
            if (null != ketteiField) {
                formFieldName = ketteiField;
            } else if (null != kouho) {
                formFieldName = kouho;
            }
            if (_param._isOutputDebug) {
                log.info(" fields " + ArrayUtils.toString(fields) + ", field = " + formFieldName + " (ketteiField = " + ketteiField + ", kouho = " + kouho + ", data = " + data + ", keta = " + dataKeta + ")");
            }
            return formFieldName;
        }

        /*
         *  出欠情報印刷
         */
        private void attend_outKindai(final DB2UDB db2, final PrintData printData) {
            final String psKey = "PS_ATTEND";
            if (null == _param.getPs(psKey)) {
                final String sql = new KNJ_AttendrecSql().pre_sql(db2, printData._year);
                _param.setPs(db2, psKey, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {printData._schregno, printData._year})) {

                final int i = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                VrsOutn("GRADE2",  i,  i + (printData.isGakunensei(_param) ? "年生" : "年次"));
                final String requirepresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
                final String present = KnjDbUtils.getString(row, "PRESENT");
                if (requirepresent != null) {
                    VrsOutn("PRESENT",  i,  requirepresent);   //要出席
                }
                if (present != null) {
                    VrsOutn("ATTEND",   i,  present);         //出席
                }
            }
        }

        /**
         *  学習の記録
         */
        private void printRecord(final DB2UDB db2, final PrintData printData) {
            final Total total = new Total(printData, _param);

            final int LINE1, LINE2, LINE3, LINE_MAX;
            if (_param._isSapporo) {
                if (printData.isEng()) {
                    LINE1 = 26;
                } else {
                    LINE1 = 28;
                }
                LINE2 = LINE1 * 2;
                LINE3 = LINE2;
                LINE_MAX = LINE3;
            } else if (_param._isSakae) {
                LINE1 = 34;
                LINE2 = 65;
                LINE3 = LINE2;
                LINE_MAX = LINE3;
            } else if (_param._isHirokoudai) {
                LINE1 = 35;
                LINE2 = LINE1;
                LINE3 = LINE2;
                LINE_MAX = LINE3;
            } else {
                LINE1 = 40;
                LINE2 = 80;
                LINE3 = 116;
                LINE_MAX = 120;
            }

            total.classgoukei._studyrecSubclassList.add(total.goukei);

            total.process(_param, printData._studyrecClassList);

            final boolean isPrintCreditEachGrade = _param._isTokiwa || _param._isSakae || _param._isHirokoudai;
            if (isPrintCreditEachGrade) {
                if (printData.isTannisei(_param)) {
                    for (int i = 0; i < total.annualList.size(); i++) {
                        final String year = total.annualList.get(i);
                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(year));
                        if (!StringUtils.isBlank(gengou)) {
                            VrsOut("GRADE1_" + String.valueOf(i + 1), gengou + "年度");
                        }
                    }
                } else if (_param._isSakae) {
                    VrsOut("GRADE1_1", "１");
                    VrsOut("GRADE1_2", "２");
                    VrsOut("GRADE1_3", "３");
                } else if (_param._isHirokoudai) {
                    VrsOut("GRADE1_1", "第１学年");
                    VrsOut("GRADE1_2", "第２学年");
                    VrsOut("GRADE1_3", "第３学年");
                } else {
                    VrsOut("GRADE1_1", "1年");
                    VrsOut("GRADE1_2", "2年");
                    VrsOut("GRADE1_3", "3年");
                }
            }

            if (_param._isSakae) {
                log.info(" classogo = " + total.classsogo + ", classgoukei = " + total.classgoukei);
                printCreditTokiwa("CREDIT_3_", total.classsogo);
                printCreditTokiwa("total", total.classgoukei);
            } else if (_param._isKindai) {
                VrsOut("ITEM", printData.getSogoSubclassname(_param)); // 総合学習修得単位数
                VrsOut("sogo", total.sogo_credit); // 総合学習修得単位数
                VrsOut("abroad", total.abroad_credit); // 留学修得単位数
                VrsOut("subtotal", total.subtotal_credit); // 小計修得単位数
            }

            final boolean isFormFormat2 = _param._isTokiwa || _param._isSapporo || _param._isHirokoudai;

            String s_specialdiv = "00"; // 教科専門区分
            int linex = 0; // 行数

            final boolean isTotalCreditRecordType = !_param._isSakae;
            final String classcdField = "subjectx";

            for (final StudyrecClass studyrecClass : printData._studyrecClassList) {

                if (_param.isPrintClassTitle(printData._certifKind) && !"1".equals(printData._notUseClassMstSpecialDiv)) {
                    if (studyrecClass._specialdiv != null && !studyrecClass._specialdiv.equals(s_specialdiv)) {
                        if (!_param._isKindai && !isFormFormat2) {
                            if (_hasData) {
                                if (linex == LINE3) {
                                    linex = 0;
                                } else if (linex == LINE1 - 1 || linex == LINE2 - 1) {
                                    VrsOut(classcdField, "-");
                                    VrEndRecord();
                                    linex++;
                                }
                            }
                            VrsOut(classcdField, "");                                               //教科コード
                            VrsOutForData(Arrays.asList("CLASSTITLE", "CLASSTITLE2"), _param.getSpecialDivName(Param.isNewForm(_param, printData), studyrecClass._specialdiv));
                            VrEndRecord();
                            linex++;
                            _hasData = true;
                        }
                        s_specialdiv = studyrecClass._specialdiv;
                    }
                }

                String classnameField = "subject1";
                final List<String> classnameCharList = new ArrayList<String>();
                if (null != studyrecClass._classname) {
                    if (_param._isSakae && studyrecClass._classname.length() % 2 == 0 && studyrecClass._classname.length() > studyrecClass._studyrecSubclassList.size()) {
                        for (int i = 0; i < studyrecClass._classname.length(); i+=2) {
                            final String ch = studyrecClass._classname.substring(i, Math.min(studyrecClass._classname.length(), i + 2));
                            classnameCharList.add(ch);
                        }
                        classnameField = "subject2";
                    } else {
                        for (int i = 0; i < studyrecClass._classname.length(); i++) {
                            final String ch = String.valueOf(studyrecClass._classname.charAt(i));
                            classnameCharList.add(ch);
                        }
                    }
                }

                for (int subi = 0; subi < studyrecClass._studyrecSubclassList.size(); subi++) {
                    final StudyrecSubclass studyrecSubclass = studyrecClass._studyrecSubclassList.get(subi);

                    if (isFormFormat2) {
                        VrsOut(classcdField, studyrecClass._classcd); // 教科コード
                        VrsOut("CLASSNAME", studyrecClass._classname); // 教科名
                        VrsOut("SUBCLASSNAME", studyrecSubclass._subclassname); //科目名
                        if (_param._isSapporo) {
                            final boolean isEng = printData.isEng();
                            final int height = 118;
                            final int charHeight = height;
                            final int ystart = isEng ? 744 + height * 2 : 744; //開始位置(ドット);
                            final int width1 = isEng ? 460 : 480;
                            final int minnum1 = 16;
                            final int maxnum1 = 20;
                            svfFieldAttribute("CLASSNAME", studyrecClass._classname, width1, height, charHeight, ystart, minnum1, maxnum1, linex, LINE1);

                            int width2 = isEng ? 460 : 480;
                            try {
                                final int keta = getFieldKeta("SUBCLASSNAME");
                                final double charSize = Double.parseDouble(getFieldPoint("SUBCLASSNAME"));
                                width2 = (int) KNJSvfFieldModify.fieldWidth(charSize, 0, keta);
                                //log.info(" keta = " + keta + ", point = " + charSize + ", width2 = " + width2);
                            } catch (Exception e) {
                                log.error("exception!", e);
                            }
                            final int minnum2 = 16;
                            final int maxnum2 = 40;
                            svfFieldAttribute("SUBCLASSNAME", studyrecSubclass._subclassname, width2, height, charHeight, ystart, minnum2, maxnum2, linex, LINE1);
                        } else if (_param._isHirokoudai) {
                            final int height = 72;
                            final int charHeight = height - 10;
                            final int ystart = 1008 + 5; //開始位置(ドット);
                            final int width1 = 480;
                            final int minnum1 = 16;
                            final int maxnum1 = 20;
                            svfFieldAttribute("CLASSNAME", studyrecClass._classname, width1, height, charHeight, ystart, minnum1, maxnum1, linex, LINE1);

                            int width2 = 480;
                            try {
                                final int keta = getFieldKeta("SUBCLASSNAME");
                                final double charSize = Double.parseDouble(getFieldPoint("SUBCLASSNAME"));
                                width2 = (int) KNJSvfFieldModify.fieldWidth(charSize, 0, keta);
                                //log.info(" keta = " + keta + ", point = " + charSize + ", width2 = " + width2);
                            } catch (Exception e) {
                                log.error("exception!", e);
                            }
                            final int minnum2 = 16;
                            final int maxnum2 = 40;
                            svfFieldAttribute("SUBCLASSNAME", studyrecSubclass._subclassname, width2, height, charHeight, ystart, minnum2, maxnum2, linex, LINE1);
                        }
                        if (isPrintCreditEachGrade) {
                            for (final String annual : studyrecSubclass._annualCreditMap.keySet()) {
                                if (NumberUtils.isDigits(annual)) {
                                    int idx = -1;
                                    if (printData.isTannisei(_param)) {
                                        idx = total.annualList.indexOf(annual) + 1;
                                    } else {
                                        idx = Integer.parseInt(annual);
                                    }
                                    if (-1 != idx) {
                                        final String gradeCredit = studyrecSubclass._annualCreditMap.get(annual);
                                        VrsOut("tani" + String.valueOf(idx), gradeCredit);
                                    }
                                }
                            }
                        }
                        VrsOut("CREDIT", studyrecSubclass._credit); //科目名

                    } else {
                        if (subi < classnameCharList.size()) {
                            VrsOut(classnameField, (String) classnameCharList.get(subi));  // 教科名
                        }
                        VrsOut(classcdField, studyrecClass._classcd); // 教科コード
                        final String subclassnameField = "CLASSNAME1";

                        final int ystart = _param._isKyoto ? getFieldY(subclassnameField) - 20 : _param._isSakae ? 1100 : _param._isKaijyo || _param._isTottori || _param._isChukyo || _param._isKyoai ? 1082 : 1318; // 開始位置(ドット);
                        int width = 331;
                        try {
                            final int keta = getFieldKeta(subclassnameField);
                            final double charSize = Double.parseDouble(getFieldPoint(subclassnameField));
                            width = (int) KNJSvfFieldModify.fieldWidth(charSize, 0, keta);
                            //log.info(" keta = " + keta + ", point = " + charSize + ", width = " + width);
                        } catch (Exception e) {
                            log.error("exception!", e);
                        }
                        final int height = _param._isSakae ? 80 : 68; // レコードの高さ
                        final int charHeight = height;
                        final int minnum = 20;
                        final int maxnum = 40;
                        svfFieldAttribute(subclassnameField, studyrecSubclass._subclassname, width, height, charHeight, ystart, minnum, maxnum, linex, LINE1);
                        VrsOut(subclassnameField, studyrecSubclass._subclassname);                            //科目名
                        if (isPrintCreditEachGrade) {
                            for (final String annual : studyrecSubclass._annualCreditMap.keySet()) {
                                int idx = -1;
                                if (printData.isTannisei(_param)) {
                                    idx = total.annualList.indexOf(annual) + 1;
                                } else {
                                    idx = Integer.parseInt(annual);
                                }
                                if (-1 != idx) {
                                    final String gradeCredit = studyrecSubclass._annualCreditMap.get(annual);
                                    VrsOut("tani" + String.valueOf(idx), gradeCredit);
                                }
                            }
                        } else {
                            if (studyrecSubclass._credit != null) {
                                VrsOut("tani1", studyrecSubclass._credit);   //修得単位数
                            }
                            if (_param._isKwansei) {
                                VrsOut("COMP_tani1", defstr(studyrecSubclass.getCompCreditSum())); // 履修単位数
                            }
                        }
                    }

                    VrEndRecord();
                    _hasData = true;
                    linex++;

                    if (linex == LINE3) {
                        break;
                    } //行のオーバーフロー
                }

                if (!isFormFormat2) {
                    //教科名をすべて出力
                    int end = studyrecClass._studyrecSubclassList.size();
                    while (end < classnameCharList.size()) {
                        VrsOut(classcdField, studyrecClass._classcd); // 教科コード
                        VrsOut(classnameField, classnameCharList.get(end)); // 教科名
                        end++;
                        VrEndRecord();
                        linex++;
                    }
                    //空白行挿入
                    if (linex != LINE1 && linex != LINE2 && end == studyrecClass._studyrecSubclassList.size()) {
                        VrsOut(classcdField, studyrecClass._classcd); // 教科コード
                        VrEndRecord();
                        linex++;
                    }
                }
                if (linex == LINE3) {
                    break;
                }
            }
            if (toInt(total.ad_credit, -1) > 0) {
                VrsOut("total_credit", total.ad_credit);  //総合修得単位数
            }
            if (!_hasData) {
                //学習情報がない場合の処理
                VrsOut(classcdField, String.valueOf(linex)); // 教科コード
                VrEndRecord();
                linex++;
            }
            _hasData = true;
            //log.debug("linex = " + linex);
            final List<Tuple<String, Tuple<String, Tuple<String, StudyrecClass>>>> accList = new ArrayList<Tuple<String, Tuple<String, Tuple<String, StudyrecClass>>>>();
            if (_param._isTokiwa) {
                printCreditTokiwa(null, total.classdaiken);
                printCreditTokiwa(null, total.classzenseki);
                VrsOut("ITEM_SOGO", printData.getSogoSubclassname(_param));
                printCreditTokiwa("CREDIT_3_", total.classsogo);
                if (_param._isTokiwa) {
                    if (null != total.classsubtotal) {
                        Total.addCredit(total.classsogo, total.classsubtotal._studyrecSubclassList.get(0));
                    }
                    printCreditTokiwa("HR", total.classtokiwahr);
                }
                printCreditTokiwa("subtotal", total.classsubtotal);
                printCreditTokiwa("abroad", total.classabroad);
                printCreditTokiwa("total", total.classgoukei);
                for (int i = linex; i < 40; i++) {
                    VrEndRecord();
                }
            } else if (_param._isSapporo) {
                for (int i = linex; i < LINE_MAX; i++) {
                    VrsOut("CLASSNAME", String.valueOf(linex % 100)); // 教科コード
                    VrAttribute("CLASSNAME", "X=10000");
                    VrEndRecord();
                    linex++;
                }
            } else {
                if (isTotalCreditRecordType) {
                    //  空行
                    if (total.isPrintDaiken()) {
                        accList.add(Tuple.of("高認等における認定単位", Tuple.of(total.daiken_credit, Tuple.of((String) null, (StudyrecClass) null))));
                    }
                    if (total.isPrintZenseki()) {
                        accList.add(Tuple.of("前籍校における修得単位", Tuple.of(total.zenseki_credit, Tuple.of((String) null, (StudyrecClass) null))));
                    }
                }
                if (total.isPrintLastLineClass()) {
                    String classname = total.studyrecLastLineClass._classname;
                    String subclassname = null;
                    String credit = null;
                    for (final StudyrecSubclass studyrecSubclass : total.studyrecLastLineClass._studyrecSubclassList) {
                        if (null == subclassname) {
                            subclassname = studyrecSubclass._subclassname;
                        }
                        credit = addNumber(credit, studyrecSubclass._credit);
                    }
                    subclassname = null != classname && null != subclassname && !classname.equals(subclassname) ? classname + "　" + subclassname : null != classname ? classname : subclassname;
                    accList.add(Tuple.of(subclassname, Tuple.of(credit, Tuple.of((String) null, (StudyrecClass) null))));
                }
                if (isTotalCreditRecordType) {
                    if (total.isPrintSubtotal()) {
                        accList.add(Tuple.of("小　計", Tuple.of(total.subtotal_credit, Tuple.of("1", (StudyrecClass) null))));
                    }
                    accList.add(Tuple.of(_param._isMeiji ? "Catholic Spirit" : printData.getSogoSubclassname(_param), Tuple.of(total.sogo_credit, Tuple.of("1", total.classsogo))));
                    if (total.isPrintKyotoClass88()) {
                        accList.add(Tuple.of(total.classKyoto88.maxSubclassname(),  Tuple.of(total.kyoto88_credit, Tuple.of("1", (StudyrecClass) null))));
                    }
                    if (total.isPrintAbroad()) {
                        accList.add(Tuple.of("留　学", Tuple.of(total.abroad_credit, Tuple.of("1", total.classabroad))));
                    }
                    final String gokeiTitle;
                    if (_param._isHirokoudai) {
                        if (CERTIF_KINDCD_RISHU_TANNI_SHOMEISHO.equals(printData._certifKindCd)) {
                            gokeiTitle = "履修単位数";
                        } else {
                            gokeiTitle = "修得単位数";
                        }
                    } else {
                        gokeiTitle = "修得単位数の合計";
                    }
                    accList.add(Tuple.of(gokeiTitle, Tuple.of(total.ad_credit, Tuple.of("1", total.classgoukei))));
                }
                if (_param._isOutputDebug) {
                    log.info(" endrecord : lienx = " + linex + ", rest = " + (LINE_MAX - accList.size()));
                }
                for (int i = linex; i < LINE_MAX - accList.size(); i++) {
                    VrsOut(classcdField, String.valueOf(i % 100)); // 教科コード
                    VrEndRecord();
                }
                if (isTotalCreditRecordType) {
                    for (int i = 0; i < accList.size(); i++) {
                        final Tuple<String, Tuple<String, Tuple<String, StudyrecClass>>> arr = accList.get(i);
                        final String arr0 = arr._first;
                        final String arr1 = arr._second._first;
                        final String arr2 = arr._second._second._first;
                        final StudyrecClass arr3 = arr._second._second._second;
                        if (null == arr2 || null != arr2 && !_param._isKindai) {
                            final String item = arr0;
                            final String credit = arr1;
                            VrsOut("ITEM", item);
                            if (toInt(credit, -1) > 0) {
                                VrsOut("TOTAL", credit);
                            }
                            if (_param._isHirokoudai) {
                                if (null != arr3) {
                                    final StudyrecClass sc = arr3;
                                    final StudyrecSubclass totalCreditSubclass = sc.getTotalCreditSubclass();
                                    for (final String annual : totalCreditSubclass._annualCreditMap.keySet()) {
                                        int idx = -1;
                                        if (printData.isTannisei(_param)) {
                                            idx = total.annualList.indexOf(annual) + 1;
                                        } else {
                                            idx = Integer.parseInt(annual);
                                        }
                                        if (-1 != idx) {
                                            final String gradeCredit = totalCreditSubclass._annualCreditMap.get(annual);
                                            VrsOut("totaltani" + String.valueOf(idx), gradeCredit);
                                        }
                                    }
                                }
                            }
                            VrEndRecord();
                        }
                    }
                }
            }
        }

        private void printCreditTokiwa(final String field, final StudyrecClass studyrecClass) {
            if (null == studyrecClass) {
                return;
            }
            int total = 0;
            for (final StudyrecSubclass sub : studyrecClass._studyrecSubclassList) {
                for (final String annual : sub._annualCreditMap.keySet()) {
                    final String credit = sub._annualCreditMap.get(annual);
                    VrsOut(field + Integer.parseInt(annual), credit);
                    if (NumberUtils.isDigits(credit) && Integer.parseInt(annual) > 0) {
                        total += Integer.parseInt(credit);
                    }
                }
            }
            VrsOut(field + "5", String.valueOf(total));
        }
    }

    /**
     *
     *  [進路情報・調査書]学習記録データSQL作成
     *
     */

    private static class StudyrecSql {

        private static String sogo = "sogo";
        private static String abroad = "abroad";
        private static String total = "total";
        private static String zenseki = "zenseki";
        private static String daiken = "daiken";
        private static String tokiwahr = "tokiwahr";
        private static String kyoto88 = "kyoto88";

        private static String _88 = "88";

        private Param _param;
        public String tableSchregStudyrecDat = null;        // SCHREG_STUDYREC_DAT
        public String tableSchreggTransferDat = null;        // SCHREG_TRANSFER_DAT
        public String tableSchregRegdDat = null;        // SCHREG_REGD_DAT

        public StudyrecSql(Param param) {
            _param = param;
        }

        /**
        *
        *  学習記録のSQL
        */
       public String getStudyrecSql(final PrintData printData, final int flg) {
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

           final String schooldiv = printData.getSchoolDiv(_param);
           final boolean daiken_div_code = _param._daiken_div_code;
           final String _useCurriculumcd = _param._useCurriculumcd;
           final boolean notPrintAnotherStudyrec = "1".equals(printData._paramap.get("tannishutokushoumeishoNotPrintAnotherStudyrec"));
           final boolean isEnglish = printData.isEng();

           final List<String> ryunenYearList = printData._dropYears;
           final StringBuffer ryunenYearSqlNotIn = new StringBuffer();
           if (null != ryunenYearList && !ryunenYearList.isEmpty()) {
               ryunenYearSqlNotIn.append(" NOT IN (");
               String comma = "";
               for (final String year : ryunenYearList) {
                   ryunenYearSqlNotIn.append(comma).append("'").append(year).append("'");
                   comma = ", ";
               }
               ryunenYearSqlNotIn.append(" )");
           }

           String lastLineClasscd = printData._lastLineClasscd; // getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
           boolean isSubclassContainLastLineClass = false; //表示する行を取得
           boolean isHyoteiHeikinLastLineClass = false; // lastLineClasscd教科に評定を入力し評定平均を表示する
           boolean isTotalContainLastLineClass = false;  // 'total'にlastLineClasscd教科を含める
           if (null != lastLineClasscd) {
//               isSubclassContainLastLineClass = asBoolean(paramMap, "isSubclassContainLastLineClass", true);
//               isHyoteiHeikinLastLineClass = asBoolean(paramMap, "isHyoteiHeikinLastLineClass", true);
//               isTotalContainLastLineClass = asBoolean(paramMap, "isTotalContainLastLineClass", true);
               // 面倒なのｄtrue
               isSubclassContainLastLineClass = true;
               isHyoteiHeikinLastLineClass = true;
               isTotalContainLastLineClass = true;
           } else if (_param._isTokiwa) {
               lastLineClasscd = "94";
               isSubclassContainLastLineClass = false;
               isHyoteiHeikinLastLineClass = false;
               isTotalContainLastLineClass = false;
           } else if (_param._isNishiyama) {
               lastLineClasscd = "94";
               isSubclassContainLastLineClass = true;
               isHyoteiHeikinLastLineClass = true;
               isTotalContainLastLineClass = true;
           }
           final String schoolMstSchoolKind = _param._hasSCHOOL_MST_SCHOOL_KIND ? "H" : null;

           final StringBuffer sql = new StringBuffer();

           //該当生徒の成績データ表
           final boolean isKindai = _param._isKindai;
           if (isKindai) {
               //近大付属は評価読替元科目はココで除外する
               sql.append("WITH STUDYREC AS(");
               sql.append("SELECT  T1.CLASSNAME, ");
               sql.append("        T1.SUBCLASSNAME, ");
               sql.append("        T1.SCHREGNO, ");
               sql.append("        T1.YEAR, ");
               sql.append("        T1.ANNUAL, ");
               sql.append("        T1.CLASSCD, ");
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CURRICULUM_CD, ");
               sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD ");
               if (CERTIF_KINDCD_RISHU_TANNI_SHOMEISHO.equals(printData._certifKindCd)) {
                   sql.append("       ,T1.COMP_CREDIT AS CREDIT ");
               } else {
                   sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
               }
               sql.append("        , T1.VALUATION ");
               sql.append("FROM   " + tableSchregStudyrecDat + " T1 ");
               sql.append("        LEFT JOIN SUBCLASS_MST L2 ON ");
               sql.append("        L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
               if (_param._hasSTUDYREC_PROV_FLG_DAT) {
                   sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                   sql.append("            AND L3.YEAR = T1.YEAR ");
                   sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                   sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
                   sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                   sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                   sql.append("            AND L3.PROV_FLG = '1' ");
               }
               sql.append("WHERE   T1.SCHREGNO = '" + printData._schregno + "' ");
               sql.append("        AND T1.YEAR <= '" + printData._year + "' ");
               sql.append("        AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
               sql.append("        AND NOT EXISTS(SELECT  'X' ");
               sql.append("                   FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
               sql.append("                   WHERE   T2.YEAR = T1.YEAR AND ");
               sql.append("        T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || T2.ATTEND_SUBCLASSCD = ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) ");
               if (_param._hasSTUDYREC_PROV_FLG_DAT) {
                   sql.append("         AND L3.SUBCLASSCD IS NULL ");
               }
               if (notPrintAnotherStudyrec) {
                   sql.append("         AND T1.SCHOOLCD <> '1' ");
               }
               sql.append("    )");
           } else {
               sql.append("WITH T_STUDYREC AS(");
               sql.append("SELECT  T1.SCHOOLCD, ");
               sql.append("        T1.CLASSNAME, ");
               sql.append("        T1.SUBCLASSNAME, ");
               sql.append("        T1.SCHREGNO, ");
               sql.append("        T1.YEAR, ");
               sql.append("        T1.ANNUAL, ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
               }
               sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        L2.SUBCLASSCD2 AS RAW_SUBCLASSCD2, ");
                   sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
               }
               sql.append("        L2.SUBCLASSCD2 AS SUBCLASSCD2 ");
               if (_param._isHosei) {
                   sql.append("   ,CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
               } else {
                   sql.append("   ,T1.CLASSCD ");
               }
               sql.append("       ,T1.COMP_CREDIT ");
               if (CERTIF_KINDCD_RISHU_TANNI_SHOMEISHO.equals(printData._certifKindCd)) {
                   sql.append("       ,T1.COMP_CREDIT AS CREDIT ");
               } else {
                   sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
               }
               sql.append("        , T1.VALUATION ");
               sql.append("FROM   " + tableSchregStudyrecDat + " T1 ");
               sql.append("        LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        AND L2.CLASSCD = T1.CLASSCD ");
                   sql.append("        AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   sql.append("        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
               }
               if (_param._hasSTUDYREC_PROV_FLG_DAT) {
                   sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                   sql.append("            AND L3.YEAR = T1.YEAR ");
                   sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                   sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
                   if ("1".equals(_useCurriculumcd)) {
                       sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                       sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                   }
                   sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                   sql.append("            AND L3.PROV_FLG = '1' ");
               }
               sql.append("WHERE   T1.SCHREGNO = '" + printData._schregno + "' ");
               sql.append("    AND T1.YEAR <= '" + printData._year + "' ");
               sql.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "' ");
               if (null != lastLineClasscd) {
                   sql.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
               }
               sql.append("        ) ");
               if (ryunenYearSqlNotIn.length() > 0) {
                   sql.append("    AND T1.YEAR " + ryunenYearSqlNotIn);
               }
               if (_param._isNotPrintMirishu) {
                   sql.append("        AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
               }
               if (_param._hasSTUDYREC_PROV_FLG_DAT) {
                   sql.append("         AND L3.SUBCLASSCD IS NULL ");
               }
               if (notPrintAnotherStudyrec) {
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
               if ("1".equals(schooldiv)) {
                   if (daiken_div_code) {
                       sql.append(" WHERE  T1.SCHOOLCD = '0'");
                   } else {
                       sql.append(" WHERE  T1.SCHOOLCD = '0'");
                       sql.append("     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)");
                   }
                   if (null != _param._zensekiSubclassCd) {
                       sql.append("     OR ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') ");
                       if ("1".equals(_useCurriculumcd)) {
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
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("        T1.RAW_SUBCLASSCD, ");
               sql.append("        T1.SUBCLASSCD, ");
               sql.append("        T1.CREDIT, ");
               sql.append("        T1.COMP_CREDIT, ");
               sql.append("        T1.VALUATION ");
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
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("        T1.RAW_SUBCLASSCD, ");
               sql.append("        T1.SUBCLASSCD2 AS SUBCLASSCD, ");
               sql.append("        T1.CREDIT, ");
               sql.append("        T1.COMP_CREDIT, ");
               sql.append("        T1.VALUATION ");
               sql.append("    FROM ");
               sql.append("        T_STUDYREC2 T1 ");
               sql.append("    WHERE ");
               sql.append("        T1.SUBCLASSCD2 IS NOT NULL ");

               final int _hyoteiKeisanMinGrades = "Y".equals(printData._d015Namespare1) ? 0 : 1;
               // 同一年度同一科目の場合単位は合計とします。
               //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
               final String gradesCase0 = "case when " + String.valueOf(0) + " < T1.VALUATION then T1.VALUATION end";
               final String gradesCase = "case when " + String.valueOf(_hyoteiKeisanMinGrades) + " < T1.VALUATION then VALUATION end";
               final String creditCase = "case when " + String.valueOf(_hyoteiKeisanMinGrades) + " < T1.VALUATION then CREDIT end";

               sql.append(") , STUDYREC AS( ");
               sql.append("    SELECT ");
               sql.append("        MIN(T1.SCHOOLCD) AS SCHOOLCD, ");
               sql.append("        MAX(T1.CLASSNAME) AS CLASSNAME, ");
               sql.append("        MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
               sql.append("        T1.SCHREGNO, ");
               sql.append("        T1.YEAR, ");
               sql.append("        MAX(T1.ANNUAL) AS ANNUAL, ");
               sql.append("        T1.CLASSCD, ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("        T1.RAW_SUBCLASSCD, ");
               sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
               sql.append("        case when COUNT(*) = 1 then MAX(T1.VALUATION) ");//１レコードの場合、評定はそのままの値。
               sql.append("             when GVAL_CALC = '0' then ");
               if (_hyoteiKeisanMinGrades != 0) {
                   sql.append("           CASE WHEN MAX(VALUATION) <= " + String.valueOf(_hyoteiKeisanMinGrades) + " THEN MAX(" + gradesCase0 + ") ");
                   sql.append("                ELSE ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
                   sql.append("           END ");
               } else {
                   sql.append("           ROUND(AVG(FLOAT(" + gradesCase + ")), 0) ");
               }
               sql.append("             when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
               sql.append("             when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
               sql.append("             else MAX(T1.VALUATION) ");
               sql.append("        end AS VALUATION,");
               sql.append("        SUM(T1.CREDIT) AS CREDIT, ");
               sql.append("        SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
               sql.append("    FROM ");
               sql.append("        STUDYREC0 T1 ");
               sql.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
               if (null != schoolMstSchoolKind) {
                   sql.append("        AND SC.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
               }
               sql.append("    GROUP BY ");
               sql.append("        T1.SCHREGNO, ");
               sql.append("        T1.YEAR, ");
               sql.append("        T1.CLASSCD, ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("        T1.RAW_SUBCLASSCD, ");
               sql.append("        T1.SUBCLASSCD ");
               sql.append("      , SC.GVAL_CALC ");
               if (null != schoolMstSchoolKind) {
                   sql.append("      , SC.SCHOOL_KIND ");
               }
               sql.append(") ");
           }

           if ("0".equals(schooldiv)) {
               sql.append(" , DROP_YEAR AS(");
               sql.append("        SELECT DISTINCT T1.YEAR ");
               sql.append("        FROM SCHREG_REGD_DAT T1");
               sql.append("        WHERE T1.SCHREGNO  = '" + printData._schregno + "' ");
               sql.append("          AND T1.YEAR NOT IN (SELECT MAX(T2.YEAR) FROM SCHREG_REGD_DAT T2 ");
               sql.append("                            WHERE T2.SCHREGNO = T1.SCHREGNO ");
               sql.append("                              AND T2.YEAR <= '" + printData._year + "' ");
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
           final String groupByColumn = "1".equals(schooldiv) ? " YEAR " : " ANNUAL ";
           if (flg == 0) {

               sql.append(", MAIN AS ( ");
               //該当生徒の科目評定、修得単位
               sql.append(" SELECT ");
               sql.append("     T2.SHOWORDER2 as CLASS_ORDER,");
               sql.append("     T3.SHOWORDER2 as SUBCLASS_ORDER,");
               sql.append("     T1.YEAR,");
               sql.append("     T1." + groupByColumn + " AS ANNUAL,");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T1.SCHOOL_KIND, ");
                   sql.append("        T1.CURRICULUM_CD, ");
               }
               sql.append("     T1.CLASSCD,");
               if (isEnglish) {                     //----->教科名 英語/日本語
                   sql.append(" T2.CLASSNAME_ENG AS CLASSNAME,");
               } else {
                   sql.append(" VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME,");
               }
               sql.append("     T1.SUBCLASSCD,");
               sql.append("     T1.RAW_SUBCLASSCD, ");
               if (isEnglish) {                     //----->科目名 英語/日本語
                   sql.append(" T3.SUBCLASSNAME_ENG AS SUBCLASSNAME,");
               } else {
                   sql.append(" VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME,");
               }
               if (useCreditMst) {
                   sql.append("       CASE WHEN T1.VALUATION = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS GRADE_CREDIT, ");
                   sql.append("       CASE WHEN T1.VALUATION = 1 AND T1.COMP_CREDIT IS NULL THEN CREM.CREDITS ELSE T1.COMP_CREDIT END AS COMP_CREDIT, ");
               } else {
                   sql.append("     T1.CREDIT AS GRADE_CREDIT,");
                   sql.append("     T1.COMP_CREDIT,");
               }
               sql.append("     SUBCLSGRP.CREDIT ");
               sql.append(" FROM ");
               sql.append("     STUDYREC T1 ");
               sql.append("     LEFT JOIN CLASS_MST T2 ON ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("     T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
               } else {
                   sql.append("     T2.CLASSCD = T1.CLASSCD ");
               }
               sql.append("     LEFT JOIN SUBCLASS_MST T3 ON ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
               }
               sql.append("         T3.SUBCLASSCD = T1.SUBCLASSCD ");
               //  修得単位数の計
               sql.append("     LEFT JOIN(SELECT ");
               sql.append("             T1.CLASSCD, T1.SUBCLASSCD ");
               if (useCreditMst) {
                   sql.append("     , SUM(CASE WHEN T1.VALUATION = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END) AS CREDIT ");
               } else {
                   sql.append("     , SUM(T1.CREDIT) AS CREDIT ");
               }
               sql.append("         FROM ");
               sql.append("             STUDYREC T1 ");
               if (useCreditMst) {
                   sql.append("     LEFT JOIN CREM CREM ON CREM.YEAR = T1.YEAR ");
                   sql.append("         AND CREM.SCHREGNO = T1.SCHREGNO ");
                   sql.append("         AND CREM.CLASSCD = T1.CLASSCD ");
                   sql.append("         AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   sql.append("         AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                   sql.append("         AND CREM.CLASSCD || '-' || CREM.SCHOOL_KIND || '-' || CREM.CURRICULUM_CD || '-' || CREM.SUBCLASSCD = T1.SUBCLASSCD ");
               }
               sql.append("         WHERE ");
               sql.append("             (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
               if (null != lastLineClasscd && isSubclassContainLastLineClass) {
                   sql.append("           OR T1.CLASSCD = '" + lastLineClasscd + "'");
               }
               sql.append("             )");
               sql.append("             AND T1.YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND T1.YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append("         GROUP BY ");
               sql.append("             T1.CLASSCD, T1.SUBCLASSCD ");
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
               sql.append("      )");
               if (_param._isKyoto) {
                   sql.append("   AND T1.CLASSCD <> '" + _88 + "'");
               }

               sql.append(") ");

               sql.append(" SELECT");
               sql.append("   T1.CLASS_ORDER ");  // 表示順教科
               sql.append("  ,T1.SUBCLASS_ORDER ");  // 表示順科目
               sql.append("  ,T1.YEAR");
               sql.append("  ,T1.ANNUAL");
               sql.append("  ,T1.CLASSCD");
               sql.append("  ,T1.CLASSNAME");
               sql.append("  ,T1.SUBCLASSCD");
               sql.append("  ,T1.SUBCLASSNAME");
               sql.append("  ,T1.GRADE_CREDIT");
               sql.append("  ,T1.COMP_CREDIT AS GRADE_COMP_CREDIT");
               sql.append("  ,T1.CREDIT ");
               sql.append("  ,VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV ");
               sql.append(" FROM ");
               sql.append("    MAIN T1 ");
               sql.append("    LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("  AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
               }
               sql.append(" ORDER BY ");
               if (!"1".equals(printData._notUseClassMstSpecialDiv)) {
                   sql.append("   SPECIALDIV, ");
               }
               sql.append("   CLASS_ORDER ");
               sql.append("  ,CLASSCD ");
               sql.append("  ,SUBCLASS_ORDER ");
               if (_param._isKyoto) {
                   sql.append("  ,RAW_SUBCLASSCD ");
               }
               sql.append("  ,SUBCLASSCD ");
               sql.append("  ,YEAR ");
               sql.append("  ,ANNUAL");

           } else if (flg == 1) {
               sql.append(", MAIN0 AS ( ");
               //  総合学習の修得単位数（学年別）
               sql.append("SELECT ");
               sql.append("    T1.SCHOOLCD ");
               sql.append("  , T1.CLASSNAME ");
               sql.append("  , T1.SUBCLASSNAME ");
               sql.append("  , T1.SCHREGNO ");
               sql.append("  , T1.YEAR ");
               sql.append("  , T1.ANNUAL ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("  , T1.SCHOOL_KIND ");
                   sql.append("  , T1.CURRICULUM_CD ");
               }
               sql.append("  , T1.RAW_SUBCLASSCD ");
               sql.append("  , T1.SUBCLASSCD ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("      , T1.RAW_SUBCLASSCD2 ");
               }
               sql.append("  , T1.SUBCLASSCD2 ");
               sql.append("  , T1.CLASSCD ");
               if (useCreditMst) {
                   sql.append("  , CASE WHEN T1.VALUATION = 1 AND T1.CREDIT IS NULL THEN CREM.CREDITS ELSE T1.CREDIT END AS CREDIT ");
                   sql.append("  , CASE WHEN T1.VALUATION = 1 AND T1.COMP_CREDIT IS NULL THEN CREM.CREDITS ELSE T1.COMP_CREDIT END AS COMP_CREDIT ");
               } else {
                   sql.append("  , T1.CREDIT ");
                   sql.append("  , T1.COMP_CREDIT ");
               }
               sql.append("  , T1.VALUATION ");

               sql.append(" FROM ");
               if (isKindai) {
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
               sql.append(" ) ");

               sql.append(", MAIN AS ( ");
               //  総合学習の修得単位数（学年別）
               sql.append(" SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("     " + groupByColumn + " AS ANNUAL,");
               sql.append("     '" + KNJDefineCode.subject_T + "' AS CLASSCD,");
               sql.append("     '" + sogo + "' AS CLASSNAME,");
               sql.append("     '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD,");
               sql.append("     '" + sogo + "' AS SUBCLASSNAME,");
               sql.append("     SUM(CREDIT) AS CREDIT ");
               sql.append(" FROM ");
               sql.append("     MAIN0 ");
               sql.append(" WHERE ");
               sql.append("     CLASSCD = '" + KNJDefineCode.subject_T + "' ");
               sql.append("     AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append(" GROUP BY " + groupByColumn);

               //  総合学習の修得単位数（合計）
               sql.append(" UNION SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("         '000' AS ANNUAL,");
               sql.append("         '" + KNJDefineCode.subject_T + "' AS CLASSCD,");
               sql.append("         '" + sogo + "' AS CLASSNAME,");
               sql.append("         '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD,");
               sql.append("         '" + sogo + "' AS SUBCLASSNAME,");
               sql.append("         SUM(CREDIT) AS CREDIT ");
               sql.append("     FROM ");
               sql.append("         MAIN0 ");
               sql.append("     WHERE ");
               sql.append("         CLASSCD = '" + KNJDefineCode.subject_T + "' ");
               sql.append("         AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }

               //  留学中の修得単位数（学年別）
               sql.append(" UNION SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("     " + groupByColumn + " AS ANNUAL,");
               sql.append("     'AA' AS CLASSCD,");
               sql.append("     '" + abroad + "' AS CLASSNAME,");
               sql.append("     'AAAA' AS SUBCLASSCD,");
               sql.append("     '" + abroad + "' AS SUBCLASSNAME,");
               sql.append("    SUM(ABROAD_CREDITS) AS CREDIT ");
               sql.append(" FROM ");
               sql.append("         (SELECT ");
               sql.append("             ABROAD_CREDITS,");
               sql.append("             INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
               sql.append("         FROM ");
               sql.append(              tableSchreggTransferDat + " ");
               sql.append("         WHERE ");
               sql.append("             SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
               sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append("         )ST1,");
               sql.append("         (SELECT ");
               if ("1".equals(schooldiv)) {
                   sql.append("         YEAR ");
               } else {
                   sql.append("         ANNUAL,MAX(YEAR) AS YEAR ");
               }
               sql.append("         FROM ");
               sql.append(              tableSchregRegdDat + " ");
               sql.append("         WHERE ");
               sql.append("             SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' ");
               sql.append("         GROUP BY " + groupByColumn);
               sql.append("         )ST2 ");
               sql.append(" WHERE ");
               sql.append("     ST1.TRANSFER_YEAR <= " + printData._year + " ");
               sql.append("     and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
               sql.append(" GROUP BY " + groupByColumn);

               //  留学中の修得単位数（合計）
               sql.append("     UNION SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("         '000' AS ANNUAL,");
               sql.append("         'AA' AS CLASSCD,");
               sql.append("         '" + abroad + "' AS CLASSNAME,");
               sql.append("         'AAAA' AS SUBCLASSCD,");
               sql.append("         '" + abroad + "' AS SUBCLASSNAME,");
               sql.append("         SUM(ABROAD_CREDITS) AS CREDIT ");
               sql.append("     FROM ");
               sql.append("         (SELECT ");
               sql.append("             SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
               sql.append("         FROM  ");
               sql.append(              tableSchreggTransferDat + " ");
               sql.append("         WHERE  ");
               sql.append("             SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
               sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append("         )ST1 ");
               sql.append("     WHERE ");
               sql.append("         TRANSFER_YEAR <= " + printData._year + " ");

               if (_param._isTokiwa) {
                   // 常盤ホームルーム(教科コード94)
                   sql.append(" UNION SELECT ");
                   sql.append("     cast(null as varchar(4)) as YEAR,");
                   sql.append("     " + groupByColumn + " AS ANNUAL,");
                   sql.append("     '" + tokiwahr + "' AS CLASSCD, ");
                   sql.append("     '" + tokiwahr + "' AS CLASSNAME,");
                   sql.append("     '" + tokiwahr + "' AS SUBCLASSCD, ");
                   sql.append("     '" + tokiwahr + "' AS SUBCLASSNAME,");
                   sql.append("     SUM(T1.CREDIT) AS CREDIT ");
                   sql.append(" FROM ");
                   sql.append("     MAIN0 T1 ");
                   sql.append(" WHERE ");
                   sql.append("     CLASSCD = '94' ");
                   sql.append("     AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append(" GROUP BY " + groupByColumn);
               }


               if (_param._isKyoto) {
                   // 京都府自立活動(教科コード88)
                   sql.append(" UNION SELECT ");
                   sql.append("     cast(null as varchar(4)) as YEAR,");
                   sql.append("     " + groupByColumn + " AS ANNUAL,");
                   sql.append("     '" + kyoto88 + "' AS CLASSCD, ");
                   sql.append("     '" + kyoto88 + "' AS CLASSNAME,");
                   sql.append("     '" + kyoto88 + "' AS SUBCLASSCD, ");
                   sql.append("     MAX(VALUE(T1.SUBCLASSNAME, SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME)) AS SUBCLASSNAME,");
                   sql.append("     SUM(T1.CREDIT) AS CREDIT ");
                   sql.append(" FROM ");
                   sql.append("     MAIN0 T1 ");
                   sql.append("     LEFT JOIN SUBCLASS_MST SUBM ON ");
                   if ("1".equals(_useCurriculumcd)) {
                       sql.append("     SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
                       sql.append("     AND SUBM.CLASSCD = T1.CLASSCD ");
                       sql.append("     AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                       sql.append("     AND SUBM.CURRICULUM_CD= T1.CURRICULUM_CD ");
                   } else {
                       sql.append("     SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
                   }
                   sql.append(" WHERE ");
                   sql.append("     T1.CLASSCD = '" + _88 + "' ");
                   sql.append("     AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append(" GROUP BY " + groupByColumn);
               }

               //  修得単位数、評定平均（学年別）
               sql.append(" UNION SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("     " + groupByColumn + " AS ANNUAL,");
               sql.append("     'ZZ' AS CLASSCD,");
               sql.append("     '" + total + "' AS CLASSNAME,");
               sql.append("     'ZZZZ' AS SUBCLASSCD,");
               sql.append("     '" + total + "' AS SUBCLASSNAME,");
               sql.append("     SUM(T1.CREDIT) AS CREDIT ");
               sql.append(" FROM ");
               sql.append("         MAIN0 T1 ");
               sql.append(" WHERE ");
               sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
               if (null != lastLineClasscd && isTotalContainLastLineClass) {
                   sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
               }
               sql.append("      )");
               if (_param._isKyoto) {
                   sql.append("     AND T1.CLASSCD <> '" + _88 + "' ");
               }
               sql.append("     AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append(" GROUP BY " + groupByColumn);

               //  全体の修得単位数・全体の評定平均値
               sql.append("     UNION SELECT ");
               sql.append("     cast(null as varchar(4)) as YEAR,");
               sql.append("         '000' AS ANNUAL,");
               sql.append("         'ZZ' AS CLASSCD,");
               sql.append("         '" + total + "' AS CLASSNAME,");
               sql.append("         'ZZZZ' AS SUBCLASSCD,");
               sql.append("     '" + total + "' AS SUBCLASSNAME,");

               sql.append("         SUM(T1.CREDIT) AS CREDIT ");
               sql.append("     FROM ");
               sql.append("         MAIN0 T1 ");
               sql.append("     WHERE ");
               sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
               if (null != lastLineClasscd && isTotalContainLastLineClass) {
                   sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
               }
               sql.append("      )");
               if (_param._isKyoto) {
                   sql.append("     AND T1.CLASSCD <> '" + _88 + "' ");
               }
               sql.append("         AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }

               // 前籍校における修得単位（レコードがある場合のみ）
               if ("1".equals(schooldiv) && null != _param._zensekiSubclassCd) {
                   sql.append(" UNION SELECT");
                   sql.append("      cast(null as varchar(4)) as YEAR");
                   sql.append("    , S1.ANNUAL");
                   sql.append("    , 'ZB' AS CLASSCD");
                   sql.append("    , '" + zenseki + "' AS CLASSNAME");
                   sql.append("    , 'ZZZB' AS SUBCLASSCD");
                   sql.append("    , '" + zenseki + "' AS SUBCLASSNAME");
                   sql.append("    , S1.CREDIT ");
                   sql.append(" FROM(");
                   sql.append("      SELECT T1.SCHREGNO,SUM(T1.CREDIT ) AS CREDIT, T1.ANNUAL ");
                   sql.append("      FROM(");
                   sql.append("           SELECT SCHREGNO, CREDIT");
                   sql.append("            , YEAR AS ANNUAL ");
                   sql.append("           FROM ");
                   sql.append("               MAIN0 T1 ");
                   sql.append("           WHERE ((SCHOOLCD = '1' OR YEAR = '0') ");
                   if ("1".equals(_useCurriculumcd)) {
                       sql.append("                  AND RAW_SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                   } else {
                       sql.append("                  AND SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                   }
                   sql.append("                 )");
                   sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append("      )T1");
                   sql.append("      GROUP BY T1.SCHREGNO, T1.ANNUAL");
                   sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
                   sql.append(" )S1 ");

                   //----->合計
                   sql.append(" UNION SELECT");
                   sql.append("      cast(null as varchar(4)) as YEAR ");
                   sql.append("    , '000' AS ANNUAL");
                   sql.append("    , 'ZB' AS CLASSCD");
                   sql.append("    , '" + zenseki + "' AS CLASSNAME");
                   sql.append("    , 'ZZZB' AS SUBCLASSCD");
                   sql.append("    , '" + zenseki + "' AS SUBCLASSNAME");
                   sql.append("    , S1.CREDIT ");
                   sql.append(" FROM(");
                   sql.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
                   sql.append("      FROM(");
                   sql.append("           SELECT T1.SCHREGNO, CREDIT");
                   sql.append("           FROM ");
                   sql.append("               MAIN0 T1 ");
                   sql.append("           WHERE ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') ");
                   if ("1".equals(_useCurriculumcd)) {
                       sql.append("                  AND T1.RAW_SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                   } else {
                       sql.append("                  AND T1.SUBCLASSCD = '" + _param._zensekiSubclassCd + "'");
                   }
                   sql.append("                 )");
                   sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append("      )T1");
                   sql.append("      GROUP BY T1.SCHREGNO");
                   sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
                   sql.append(" )S1 ");
               }

               // 大検における認定単位（レコードがある場合のみ）
               if ("1".equals(schooldiv) && daiken_div_code) {
                   sql.append(" UNION SELECT");
                   sql.append("      cast(null as varchar(4)) as YEAR ");
                   sql.append("    , S1.ANNUAL");
                   sql.append("    , 'ZA' AS CLASSCD");
                   sql.append("    , '" + daiken + "' AS CLASSNAME");
                   sql.append("    , 'ZZZA' AS SUBCLASSCD");
                   sql.append("    , '" + daiken + "' AS SUBCLASSNAME");
                   sql.append("    , S1.CREDIT ");
                   sql.append(" FROM(");
                   sql.append("      SELECT T1.SCHREGNO,SUM(T1.CREDIT ) AS CREDIT, T1.ANNUAL ");
                   sql.append("      FROM(");
                   sql.append("           SELECT SCHREGNO");
                   sql.append("            , CREDIT");
                   sql.append("            , YEAR AS ANNUAL ");
                   sql.append("           FROM ");
                   sql.append("               MAIN0 T1 ");
                   sql.append("           WHERE SCHOOLCD = '2'");
                   sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append("      )T1");
                   sql.append("      GROUP BY T1.SCHREGNO, T1.ANNUAL");
                   sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
                   sql.append(" )S1 ");

                   //----->合計
                   sql.append(" UNION SELECT");
                   sql.append("      cast(null as varchar(4)) as YEAR ");
                   sql.append("    , '000' AS ANNUAL");
                   sql.append("    , 'ZA' AS CLASSCD");
                   sql.append("    , '" + daiken + "' AS CLASSNAME");
                   sql.append("    , 'ZZZA' AS SUBCLASSCD");
                   sql.append("    , '" + daiken + "' AS SUBCLASSNAME");
                   sql.append("    , S1.CREDIT ");
                   sql.append(" FROM(");
                   sql.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
                   sql.append("      FROM(");
                   sql.append("           SELECT T1.SCHREGNO, CREDIT");
                   sql.append("           FROM ");
                   sql.append("               MAIN0 T1 ");
                   sql.append("           WHERE T1.SCHOOLCD = '2'");
                   sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
                   if ("0".equals(schooldiv)) {
                       sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
                   }
                   sql.append("      )T1");
                   sql.append("      GROUP BY T1.SCHREGNO");
                   sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
                   sql.append(" )S1 ");
               }

               sql.append(") ");

               sql.append(" SELECT");
               sql.append("   T1.YEAR");
               sql.append("  ,T1.ANNUAL");
               sql.append("  ,T1.CLASSCD");
               sql.append("  ,T1.CLASSNAME");
               sql.append("  ,T1.SUBCLASSCD");
               sql.append("  ,T1.SUBCLASSNAME");
               sql.append("  ,T1.CREDIT ");
               sql.append(" FROM ");
               sql.append("    MAIN T1 ");
               sql.append(" ORDER BY ");
               sql.append("   CLASSCD ");
               sql.append("  ,SUBCLASSCD ");
               sql.append("  ,YEAR ");
               sql.append("  ,ANNUAL");
           }

           return sql.toString();
       }
    }

    private static class KNJSvfFieldModify {

        private final String _fieldname; // フィールド名
        private final double _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        private static final double dpi = 400.0;
        private static final double pointPerInch = 72;

        public KNJSvfFieldModify(String fieldname, double width, int height, int ystart, int minnum, int maxnum) {
            _fieldname = fieldname;
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final double charSize, final int upperOrLower, final int keta) {
            return charPointToPixel(charSize, upperOrLower) * keta / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public float getCharSize(String str) {
            return Math.min((float) pixelToCharPoint(_height), retFieldPoint(_width, getStringByteSize(str))); // 文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private int getStringByteSize(String str) {
            return Math.min(Math.max(KNJ_EditEdit.getMS932ByteLength(str), _minnum), _maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final double charPoint, final int upperOrLower) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.charPointToPixel("", charPoint, upperOrLower);
        }


//        /**
//         * 文字サイズをピクセルに変換した値を得る
//         * @param charSize 文字サイズ
//         * @return 文字サイズをピクセルに変換した値
//         */
//        public static int charSizeToPixel(final double charSize) {
//            return (int) Math.round(charSize / 72 * 400);
//        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharPoint(final int pixel) {
            return pixel * pointPerInch / dpi;
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
        private static float retFieldPoint(double width, int num) {
            return (float) Math.round((double) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * pointPerInch / dpi * 10) / 10;
        }

        private static double charHeightPixel(final double charSize) {
            return charPointToPixel(charSize, 0);
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static float retFieldY(int height, float charSize) {
            return (float) Math.round(((double) height - charHeightPixel(charSize)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    private static class Utils {

        public static Integer integerSum(final Collection<Integer> integerList) {
            Integer sum = null;
            for (final Integer v : integerList) {
                if (null == v) {
                    continue;
                }
                if (null == sum) {
                    sum = new Integer(0);
                }
                sum = new Integer(sum.intValue() + v.intValue());
            }
            return sum;
        }

        public static List<Integer> toIntegerList(final Collection<String> integerStringList) {
            final List<Integer> list = new ArrayList<Integer>();
            for (final String s : integerStringList) {
                if (NumberUtils.isDigits(s)) {
                    list.add(Integer.valueOf(s));
                } else {
                    list.add(null);
                }
            }
            return list;
        }

        public static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
        }

        public static BigDecimal dotToMm(final String dot) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final BigDecimal mm = new BigDecimal(dot).multiply(mmPerInch).divide(dpi, 1, BigDecimal.ROUND_HALF_UP);
            return mm;
        }
    }

    private static class Tuple<A, B> implements Comparable<Tuple<A, B>> {
        final A _first;
        final B _second;
        private Tuple(final A first, final B second) {
            _first = first;
            _second = second;
        }
        public static <A, B> Tuple<A, B> of(A a, B b) {
            return new Tuple<A, B>(a, b);
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

    protected static class Param {
        public boolean _isJuniorHiSchool;
        public final String _z010Name1;
        public final boolean _isKindai;
        public final boolean _isHosei;
        public final boolean _isTottori;
        public final boolean _isChukyo;
        public final boolean _isKyoai;
        public final boolean _isTokiwa;
        public final boolean _isSapporo;
        public final boolean _isMeiji;
        public final boolean _isNishiyama;
        public final boolean _isMiyagiken;
        public final boolean _isMieken;
        public final boolean _isRakunan;
        public final boolean _isMusashinohigashi;
        public final boolean _isSundaikoufu;
        public final boolean _isKaijyo;
        public final boolean _isOsakatoin;
        public final boolean _isSakae;
        public final boolean _isKyoto;
        public final boolean _isHirokoudai;
        public final boolean _isRisshisha;
        public final boolean _isKeisho;
        public final boolean _isReitaku;
        public final boolean _isMatsudo;
        public final boolean _isKwansei;
        public final boolean _isRyukei;
        public final boolean _isJyoto;
        public final boolean _useNewForm;
        public final boolean _isOutputDebugAll;
        public final boolean _isOutputDebug;
        public final boolean _isOutputDebugField;
        public final boolean _isOutputDebugQuery;
        public final boolean _useGradeCdAsPrintGrade;
        public final boolean _isPrintDayOfEntDateGrdDate;

        public Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        private final Map<String, File> _createFormFiles = new HashMap<String, File>();

        public boolean _daiken_div_code; // 高等学校卒業程度認定単位（大検）の印刷方法を設定します。名称マスター'E011'のコード'02'のレコードが'Y'の場合はtrueを以外はfalseを設定します。
        public String _zensekiSubclassCd; // 前籍校の成績専用科目コードを設定します。名称マスター'E011'のコード'01'のレコード予備１をセットします。
        private boolean _seirekiFlg = false;
        protected boolean _isNotPrintMirishu = false; // trueなら未履修科目を出力しない // 未履修の科目を表示するか (予備1が'Y'なら表示しない。それ以外は表示する。）
        protected Map _isNotPrintClassTitle; // 普通/専門教育に関する教科のタイトル表示設定
        protected Map<String, Map<String, String>> _a029NameMstMap;
        protected String _useCurriculumcd;
        protected String _useGakkaSchoolDiv;
        protected String _certifPrintRealName;
        protected Map _paramap;
        protected String _documentroot;
        protected Properties _prgInfoPropertiesFilePrperties;
        private final Map<String, String> _dbPrgInfoProperties;
        protected final String _imagepath;
        protected String _sogoTankyuStartYear;
        protected String _tannishutokushoumeishoPrintCoursecodename; // コース略称を印字
        protected String _stampSizeMm;
        protected String _stampPositionXmmTanniShutokuShomeisho;
        protected String _stampPositionYmmTanniShutokuShomeisho;

        protected boolean _hasMAJOR_MST_MAJORNAME2;
        protected boolean _hasSTUDYREC_PROV_FLG_DAT;
        protected boolean _hasSCHOOL_MST_SCHOOL_KIND;
        protected boolean _hasCOURSECODE_MST_COURSECODEABBV1;

        private KNJE080_1 _knje080;
        private KNJE080_1 _knji070;

        private StudyrecSql _knjStudyrecSql;

        public Param(final DB2UDB db2) {
            _z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            log.info(" z010 name1 = " + _z010Name1);
            _isKindai = "KINDAI".equals(_z010Name1) || "KINJUNIOR".equals(_z010Name1);
            _isHosei = "HOUSEI".equals(_z010Name1);
            _isTottori = "tottori".equals(_z010Name1);
            _isChukyo = "chukyo".equals(_z010Name1);
            _isKyoai = "kyoai".equals(_z010Name1);
            _isTokiwa = "tokiwa".equals(_z010Name1);
            _isSapporo = "sapporo".equals(_z010Name1);
            _isMeiji = "meiji".equals(_z010Name1);
            _isNishiyama = "nishiyama".equals(_z010Name1);
            _isMiyagiken = "miyagiken".equals(_z010Name1);
            _isMieken = "mieken".equals(_z010Name1);
            _isRakunan = "rakunan".equals(_z010Name1);
            _isMusashinohigashi = "musashinohigashi".equals(_z010Name1);
            _isSundaikoufu = "sundaikoufu".equals(_z010Name1);
            _isKaijyo = "kaijyo".equals(_z010Name1);
            _isOsakatoin = "osakatoin".equals(_z010Name1);
            _isSakae = "sakae".equals(_z010Name1);
            _isKyoto = "kyoto".equals(_z010Name1);
            _isHirokoudai = "hirokoudai".equals(_z010Name1);
            _isRisshisha = "risshisha".equals(_z010Name1);
            _isKeisho = "Keisho".equals(_z010Name1);
            _isReitaku = "reitaku".equals(_z010Name1);
            _isMatsudo = "matsudo".equals(_z010Name1);
            _isKwansei = "kwansei".equals(_z010Name1);
            _isRyukei = "ryukei".equals(_z010Name1);
            _isJyoto = "jyoto".equals(_z010Name1);
            _useNewForm = _isMiyagiken;
            _isJuniorHiSchool = !StringUtils.isBlank(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'")));
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _daiken_div_code = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '02'")));
            _zensekiSubclassCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '01'"));
            _seirekiFlg = _isJyoto ? false : KNJ_EditDate.isSeireki(db2);
            _isNotPrintMirishu = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'A027' AND NAMECD2 = '" + CERTIF_KINDCD +"' ")));
            _isNotPrintClassTitle = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E015' "), "NAMECD2", "NAMESPARE1");
            _a029NameMstMap = getNameMst(db2, "A029");
            _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            _hasSTUDYREC_PROV_FLG_DAT = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasCOURSECODE_MST_COURSECODEABBV1 = KnjDbUtils.setTableColumnCheck(db2, "COURSECODE_MST", "COURSECODEABBV1");
            _useGradeCdAsPrintGrade = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT "), "SCHOOL_KIND").contains("P");
            _isPrintDayOfEntDateGrdDate = _isJyoto;
            _dbPrgInfoProperties = getDbPrginfoProperties(db2);
            final String[] outputDebugArray = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebugArray, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "1");
            _isOutputDebugField = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "field");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebugArray, "query");
        }

        public void close() {
            for (final Iterator<PreparedStatement> it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = it.next();
                DbUtils.closeQuietly(ps);
                it.remove();
            }
            for (final Iterator<File> it = _createFormFiles.values().iterator(); it.hasNext();) {
                final File file = it.next();
                log.info(" file " + file.getAbsolutePath() + " delete? " + file.delete());
            }
        }

        public void setDocumentroot(final String documentroot) {
            _documentroot = documentroot;
            _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJG030' "), "NAME", "VALUE");
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

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public void setPs(final DB2UDB db2, final String psKey, final String sql) {
            if (_isOutputDebugQuery) {
                log.info(" " + psKey + " sql = " + sql);
            }
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!", e);
            }
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
                final Calendar cal = toCalendar(date);
                if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                    return new Integer(cal.get((Calendar.YEAR) - 1));
                } else {
                    return new Integer(cal.get((Calendar.YEAR)));
                }
            }
            return null;
        }

        public static Calendar toCalendar(final String date) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            return cal;
        }

        /**
         * 名称マスタ読み込み
         * @param db2
         * @param namecd1 名称コード1
         * @return 名称コード2をキーとするレコードのマップ
         */
        private Map<String, Map<String, String>> getNameMst(final DB2UDB db2, final String namecd1) {
            return KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' "), "NAMECD2");
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
                defaultname = isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する教科・科目";
            } else if ("2".equals(div)) {
                // その他
                namecd2 = "3";
                defaultname = "その他特に必要な教科・科目";
            } else { // if (null == div || "0".equals(div)) {
                // 普通教育
                namecd2 = "1";
                defaultname = isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する教科・科目";
            }
            return "【" + StringUtils.defaultString(KnjDbUtils.getString(getMappedMap(_a029NameMstMap, namecd2), "NAME1"), defaultname) + "】";
        }

        /**
         * 普通/専門教育に関する教科のタイトルを表示するか
         * @param certifKindCd 証明書種別コード
         * @return 普通/専門教育に関する教科のタイトルを表示するか
         */
        protected boolean isPrintClassTitle(final String certifKindCd) {
            return !"1".equals(_isNotPrintClassTitle.get(certifKindCd)) && !_isSakae;
        }
    }
}

// kanji=漢字
/*
 * $Id: ea7f67daa742ac8723c331d7b2db8a887e2a5f04 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Line;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 小学校用
 */

public class KNJA133E {

    private static final Log log = LogFactory.getLog(KNJA133E.class);

    private static final int GRADE6 = 6;

    private static final String bar = "\u2588";

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        Param param = null;
        boolean nonedata = false;
        try {
            // print svf設定
            sd.setSvfInit(request, response, svf);

            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            param = getParam(request, db2);

            // 印刷処理
            nonedata = printSvf(db2, svf, param);
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                param.closeStatementQuietly();
                param.closeForm();
            }

            // 終了処理
            sd.closeSvf(svf, nonedata);
            sd.closeDb(db2);
        }
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 72947 $ $Date: 2020-03-12 21:02:21 +0900 (木, 12 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    /**
     * 印刷処理
     */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean hasdata = false;
        final Map<String, StaffMst> staffMstMap = StaffMst.load(db2, param._year);

        final List<String> schregnoList = param.getSchregnoList(db2);

        final Form form = new Form(svf, param);

        for (final String schregno : schregnoList) {

            final Student student = new Student(schregno, db2, staffMstMap, param);

            log.debug(" schregno = " + student._schregno);
            form.printMain(db2, student);
            hasdata = true;
        }
        return hasdata;
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return map.get(key1);
    }

    private static String mkString(final List<String> list, final String comma, final String last) {
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

    /**
     *  文字編集（ブランク挿入）
     */
    private static StringBuffer setFormatInsertBlank(final StringBuffer stb) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else {
                if (0 < n) {
                    if (1 == n) {
                        stb.insert(i - n, "　");
                        i++;
                    } else if (2 == n) {
                        stb.insert(i - n, " ");
                        i++;
                    }
                    stb.insert(i, " ");
                    i++;
                    n = 0;
                } else if (ch == '元') {
                    stb.insert(i, "　");
                    i++;
                }
            }
        }
        return stb;
    }

    /**
     *  年度の編集（ブランク挿入）
     *  @param hdate 編集対象年度「平成3年度」
     *  @param nendo 元号取得用年度
     *  @return 「平成3年度」-> 「平成 3年度」
     */
    private static String setNendoFormat(
            final DB2UDB db2,
            final Param param,
            final String hdate,
            final String nendo
            ) {
        //日付が無い場合は「平成　年度」の様式とする
        if (hdate == null) {
            if (param._isSeireki) {
                return "　    年度";
            } else {
                return wareki(db2, Integer.parseInt(nendo)) + "    年度";
            }
        }
        //「平成18年度」の様式とする => 数値は２桁
        return setFormatInsertBlank(new StringBuffer(hdate)).toString();
    }

    private static String wareki(final DB2UDB db2, final int nen) {
        final String warekinen = KNJ_EditDate.gengou(db2, nen);
        if (null != warekinen && warekinen.length() > 2) {
            return warekinen.substring(0, 2);
        }
        return "";
    }

    private static String setDateFormat1(final DB2UDB db2, final String date, final boolean isSeireki, final Param param) {
        String formatted = "";
        if (isSeireki) {
            formatted = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + setDateFormat2(param, KNJ_EditDate.h_format_JP_MD(date));
        } else {
            formatted = setDateFormat2(param, h_format_JP(db2, param, date));
        }
        return formatted;
    }

    /**
     *  日付の編集（ブランク挿入）
     *  @param hdate 編集対象日付「平成18年1月1日」
     *  @param nendo 元号取得用年度
     *  @return 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    private static String setDateFormat(
            final DB2UDB db2,
            final Param param,
            final String hdate,
            final String nendo
            ) {
        if (hdate == null) {
            if (param._isSeireki) {
                return "　    年    月    日";
            } else {
                //日付が無い場合は「平成　年  月  日」の様式とする
                return wareki(db2, Integer.parseInt(nendo)) + "    年    月    日";
            }
        }
        //「平成18年 1月 1日」の様式とする => 数値は２桁
        return setFormatInsertBlank(new StringBuffer(hdate)).toString();
    }

    /**
     * 日付の編集（XXXX年XX月XX日の様式に編集）
     * @param hdate
     * @return
     */
    private static String setDateFormat2(final Param param, final String hdate) {
        if (StringUtils.isBlank(hdate)) {
            return "　  年  月  日";
        }
        return setFormatInsertBlankDate(new StringBuffer(hdate)).toString();
    }

    /**
     * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
     * @param stb
     * @return
     */
    private static StringBuffer setFormatInsertBlankDate(final StringBuffer stb) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else if (0 < n) {
                if (1 == n) {
                    stb.insert(i - n, " ");
                    i++;
                    n = 0;
                }
            }
        }
        return stb;
    }

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2002-10-27")     :平成14年10月27日
     *   String dat = h_format_JP("2002/10/27")     :平成14年10月27日
     *----------------------------------------------------------------------------------------------*/
    private static String h_format_JP(final DB2UDB db2, final Param param, String strx) {

        String hdate = "";

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
            Calendar cal = new GregorianCalendar(new Locale("ja","JP"));
            cal.setTime(dat);
            final int nen = cal.get(Calendar.YEAR);
            final int tsuki = cal.get(Calendar.MONTH) + 1;
            final int hi = cal.get(Calendar.DATE);
            if (param._isSeireki) {
                hdate = nen + "年" + tsuki + "月" + hi + "日";
            } else {
                hdate = KNJ_EditDate.gengou(db2, nen, tsuki, hi);
            }
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JPの括り

    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        final PersonalInfo _personalInfo;
        final List<Gakuseki> _gakusekiList;
        final List<Address> _addressList;
        final List<HTrainremarkPdat> _htrainremarkPdatList;
//        final HtrainremarkPHdat _htrainremarkPHdat;
        final List<ActRecord> _actRecordList;
        final List<ClassView> _classViewList;
        final List<ValueRecord> _valueRecordList;
        final List<Attend> _attendList;
        final SchregEntGrdHistDat _entGrdHistDat;
        public Student(final String schregno, final DB2UDB db2, final Map<String, StaffMst> staffMstMap, final Param param) {
            _schregno = schregno;
            _personalInfo = PersonalInfo.load(db2, param, _schregno);
            _gakusekiList = Gakuseki.load(db2, param, staffMstMap, _schregno);
            _addressList = Address.load(db2, param, _schregno, false);
            _htrainremarkPdatList = HTrainremarkPdat.load(db2, param, _schregno);
//            _htrainremarkPHdat = HtrainremarkPHdat.load(db2, param, _schregno);
            _actRecordList = ActRecord.load(db2, param, _schregno);
            _entGrdHistDat = SchregEntGrdHistDat.load(db2, param, _schregno);
            _classViewList = ClassView.load(db2, param, _schregno, _entGrdHistDat);
            _valueRecordList = ValueRecord.load(db2, param, _schregno);
            _attendList = Attend.load(db2, param, _schregno);
        }

        public HTrainremarkPdat getHtrainremarkPDatGrade(final int grade) {
            for (final HTrainremarkPdat remark : _htrainremarkPdatList) {
                if (remark._g == grade) {
                    return remark;
                }
            }
            return null;
        }

        public static TreeSet gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet set = new TreeSet();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        public List<ValueRecord> getValueRecordWithClasscd(final String classCd) {
            final List<ValueRecord> list = new ArrayList();
            if (null != classCd) {
                for (final ValueRecord valueRecord : _valueRecordList) {
                    if (classCd.equals(valueRecord._classCd)) {
                        list.add(valueRecord);
                    }
                }
            }
            return list;
        }

        public int currentGradeCd(final Param param) {
            final int paramYear = Integer.parseInt(param._year);
            int diffyear = 100;
            int currentGrade = -1;
            for (final Gakuseki gakuseki : _gakusekiList) {
                if (!StringUtils.isNumeric(gakuseki._year) || -1 == gakuseki._gradeCd) {
                    continue;
                }
                final int dy = paramYear - Integer.parseInt(gakuseki._year);
                if (dy >= 0 && diffyear > dy) {
                    currentGrade = gakuseki._gradeCd;
                    diffyear = dy;
                }
            }
            return currentGrade;
        }
    }

    /**
     * 学校データ
     */
    private static class SchoolInfo {
        final String _schoolName1;
        final String _certifSchoolName;
        final String _principalName;
        final String _schoolZipCd;
        final String _schoolAddr1;
        final String _schoolAddr2;
        final String _remark2;
        final String _remark3;
        final String _jobName;
        public SchoolInfo(
                final String schoolName1,
                final String certifSchoolName1,
                final String principalName,
                final String schoolZipCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String remark2,
                final String remark3,
                final String jobName) {
            _schoolName1 = schoolName1;
            _certifSchoolName = certifSchoolName1;
            _principalName = principalName;
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _remark2 = remark2;
            _remark3 = remark3;
            _jobName = jobName;
        }

        /**
         * 学校データを得る
         */
        private static SchoolInfo load(final DB2UDB db2, final Param param) {
            final String certifKindCd = "116";
            final String sql1 = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR='" + param._year + "' AND CERTIF_KINDCD='" + certifKindCd + "'";

            final Map<String, String> row1 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql1));

            String certifSchoolName = KnjDbUtils.getString(row1, "SCHOOL_NAME");
            if (null != certifSchoolName) {
                final int idx = Math.max(StringUtils.lastIndexOf(certifSchoolName, " "), StringUtils.lastIndexOf(certifSchoolName, "　"));
                if (-1 != idx) {
                    certifSchoolName = certifSchoolName.substring(idx + 1);
                }
            }
            log.info("CERTIF_SCHOOL_DAT の学校名称=[" + certifSchoolName + "]");
            final String jobName = StringUtils.defaultString(KnjDbUtils.getString(row1, "JOB_NAME"), "校長");
            final String principlalName = KnjDbUtils.getString(row1, "PRINCIPAL_NAME");
            final String remark2 = KnjDbUtils.getString(row1, "REMARK2");
            final String remark3 = KnjDbUtils.getString(row1, "REMARK3");

            final String psKey = "PS_KNJ_Schoolinfo";
            if (null == param._psMap.get(psKey)) {
                final Map paramMap = new HashMap();
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    paramMap.put("schoolMstSchoolKind", Param.SCHOOL_KIND);
                }
                final String sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
                param.setPs(psKey, db2, sql);
            }
            final Map<String, String> schoolinfoRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] {param._year, param._year}));
            final String schoolName1 = KnjDbUtils.getString(schoolinfoRow, "SCHOOLNAME1");
            final String schoolAddr1 = KnjDbUtils.getString(schoolinfoRow, "SCHOOLADDR1");
            final String schoolZipCd = KnjDbUtils.getString(schoolinfoRow, "SCHOOLZIPCD");
            final String schoolAddr2 = KnjDbUtils.getString(schoolinfoRow, "SCHOOLADDR2");
            return new SchoolInfo(schoolName1, certifSchoolName, principlalName, schoolZipCd, schoolAddr1, schoolAddr2, remark2, remark3, jobName);
        }
    }

    /**
     * 生徒情報
     */
    private static class PersonalInfo {
        final String _studentName1;
        final String _studentName;
        final String _studentRealName;
        final String _studentNameHistFirst;
        final String _studentKana;
        final boolean _useRealName;
        final boolean _nameOutputFlg;
        final String _birthdayFlg;
        final String _birthday;
        final String _birthdayStr;
        final String _sex;
        final String _schAddress1;
        final String _schAddress2;
        final String _recordDiv;
        final String _staffname1;
        final String _staffname2;

        String _entYear;
        String _entDate;
        String _entReason;
        String _entSchool;
        String _entAddr;
        Integer _entDiv;
        String _entDivName;
        String _grdYear;
        String _grdDate;
        String _grdReason;
        String _grdSchool;
        String _grdAddr;
        String _grdNo;
        Integer _grdDiv;
        String _grdDivName;

        /**
         * コンストラクタ。
         */
        public PersonalInfo(final DB2UDB db2, final Map<String, String> row, final Param param) {
            final String nameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_HIST_FIRST"));
            final String realNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME_HIST_FIRST"));
            final String nameWithRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_WITH_RN_HIST_FIRST"));

            final String nameKana = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_KANA"));
            final String realNameKana = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME_KANA"));
            _useRealName = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME"));
            _nameOutputFlg = "1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG"));
            _studentName = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"));
            _studentRealName = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME"));
            if (_useRealName) {
                if (_nameOutputFlg) {
                    _studentKana = StringUtils.isBlank(realNameKana + nameKana) ? "" : realNameKana + "（" + nameKana + "）";
                    _studentName1         = StringUtils.isBlank(StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME")) + StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"))) ? "" : StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME")) + "（" + StringUtils.defaultString(KnjDbUtils.getString(row, "NAME")) + "）";
                    _studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                } else {
                    _studentKana = realNameKana;
                    _studentName1         = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME"));
                    _studentNameHistFirst = realNameHistFirst;
                }
            } else {
                _studentKana = nameKana;
                _studentName1         = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"));
                _studentNameHistFirst = nameHistFirst;
            }

            _birthdayFlg = KnjDbUtils.getString(row, "BIRTHDAY_FLG");
            _birthday = KnjDbUtils.getString(row, "BIRTHDAY");
            _birthdayStr = getBirthday(db2, _birthday, _birthdayFlg, param);
            _sex = KnjDbUtils.getString(row, "SEX");
            _schAddress1 = KnjDbUtils.getString(row, "ADDR1");
            _schAddress2 = KnjDbUtils.getString(row, "ADDR2");
            _recordDiv = KnjDbUtils.getString(row, "RECORD_DIV");
            _staffname1 = KnjDbUtils.getString(row, "STAFFNAME1");
            _staffname2 = KnjDbUtils.getString(row, "STAFFNAME2");
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            String birthday = setDateFormat1(db2, date, param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg)), param);
            if (!StringUtils.isBlank(birthday)) {
                birthday += "生";
            }
            return birthday;
        }

        /**
         * 生徒情報を得る
         */
        private static PersonalInfo load(final DB2UDB db2, final Param param, final String schregno) {
            final String psKey = "PS_PERSONAL";
            if (null == param._psMap.get(psKey)) {
                final String sql = sql_info_reg(param);
                param.setPs(psKey, db2, sql);
            }
            PersonalInfo personalInfo = new PersonalInfo(db2, KnjDbUtils.firstRow(KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] {schregno, schregno})), param);

            final String psKey2 = "PS_PERSONAL2";
            if (null == param._psMap.get(psKey2)) {
                final String sql = sql_state();
                param.setPs(psKey2, db2, sql);
            }
            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey2), new String[] {schregno})) {

                personalInfo._entYear    = KnjDbUtils.getString(row, "ENT_YEAR");
                personalInfo._entDate    = KnjDbUtils.getString(row, "ENT_DATE");
                personalInfo._entReason  = KnjDbUtils.getString(row, "ENT_REASON");
                personalInfo._entSchool  = KnjDbUtils.getString(row, "ENT_SCHOOL");
                personalInfo._entAddr    = KnjDbUtils.getString(row, "ENT_ADDR");
                personalInfo._entDiv     = StringUtils.isNumeric(KnjDbUtils.getString(row, "ENT_DIV")) ? Integer.valueOf(KnjDbUtils.getString(row, "ENT_DIV")) : null;
                personalInfo._entDivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
                personalInfo._grdYear    = KnjDbUtils.getString(row, "GRD_YEAR");
                personalInfo._grdDate    = KnjDbUtils.getString(row, "GRD_DATE");
                personalInfo._grdReason  = KnjDbUtils.getString(row, "GRD_REASON");
                personalInfo._grdSchool  = KnjDbUtils.getString(row, "GRD_SCHOOL");
                personalInfo._grdAddr    = KnjDbUtils.getString(row, "GRD_ADDR");
                personalInfo._grdNo      = KnjDbUtils.getString(row, "GRD_NO");
                personalInfo._grdDiv     = StringUtils.isNumeric(KnjDbUtils.getString(row, "GRD_DIV")) ? Integer.valueOf(KnjDbUtils.getString(row, "GRD_DIV")) : null;
                personalInfo._grdDivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
            }
            return personalInfo;
        }

        private static String sql_info_reg(final Param param) {
            final String q = "?";
            final StringBuffer sql = new StringBuffer();
            final String switchGradeCd = "0";

            sql.append("SELECT ");
            sql.append("BASE.NAME,");
            sql.append("BASE.NAME_KANA,");
            sql.append("BASE.GRD_DATE, ");
            sql.append("T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            sql.append("BASE.BIRTHDAY, BASE.SEX AS SEX_FLG, T7.ABBV1 AS SEX,");
            sql.append("T21.BIRTHDAY_FLG, ");
            sql.append("BASE.REAL_NAME, ");
            sql.append("BASE.REAL_NAME_KANA, ");
            sql.append("T18.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            sql.append("T1.GRADE,T1.ATTENDNO,T1.ANNUAL, REGDH.HR_NAME,");
            // 課程・学科・コース
            sql.append("T3.COURSENAME,T4.MAJORNAME,T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
            // 入学
            sql.append("ENTGRD.ENT_DATE,ENTGRD.ENT_DIV,");
            sql.append("(SELECT DISTINCT ANNUAL FROM SCHREG_REGD_DAT ST1,SCHREG_ENT_GRD_HIST_DAT ST2 ");
            sql.append("WHERE ST1.SCHREGNO=ST2.SCHREGNO AND ST2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' AND ST1.YEAR=FISCALYEAR(ST2.ENT_DATE) AND ");
            sql.append("ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,");

            sql.append("(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND ENTGRD.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
            sql.append( "(SELECT MIN(TBL1.ANNUAL) FROM SCHREG_REGD_DAT TBL1 WHERE TBL1.SCHREGNO=T1.SCHREGNO AND TBL1.ANNUAL='04') AS ENTER_GRADE2,");
            sql.append( "(SELECT MIN(TBL2.YEAR)   FROM SCHREG_REGD_DAT TBL2 WHERE TBL2.SCHREGNO=T1.SCHREGNO AND TBL2.ANNUAL='04') || '-04-01' AS ENT_DATE2,");
            // 住所
            sql.append("VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,");
            sql.append("T8.ADDR1,T8.ADDR2,T8.TELNO,T8.ZIPCD,");
            sql.append("T8.ADDR_FLG,");
            sql.append("(CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("T11.NAME_OUTPUT_FLG, ");
            if (switchGradeCd.equals("1")) {
                sql.append("T25.GRADE_CD, ");
            }
            if (param._isMusashinohigashi) {
                //sql.append("REGDFIH.RECORD_DIV, ");
                sql.append("CASE WHEN T1.HR_CLASS IN ('003', '004', '005') THEN 2 ELSE 1 END AS RECORD_DIV, ");
            } else {
                sql.append("CAST(NULL AS VARCHAR(1)) AS RECORD_DIV, ");
            }
            sql.append("   T1.SCHREGNO ");
            sql.append(" , STF1.STAFFNAME AS STAFFNAME1 ");
            sql.append(" , STF2.STAFFNAME AS STAFFNAME2 ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("(   SELECT     * ");
            sql.append("FROM       SCHREG_REGD_DAT T1 ");
            sql.append("WHERE      T1.SCHREGNO = " + q + " AND T1.YEAR = '" + param._year + "' ");
            sql.append("AND T1.SEMESTER = '" + param._gakki + "' ");
            sql.append(") T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR ");
            sql.append("  AND REGDH.SEMESTER = T1.SEMESTER ");
            sql.append("  AND REGDH.GRADE = T1.GRADE ");
            sql.append("  AND REGDH.HR_CLASS = T1.HR_CLASS ");
            if (param._isMusashinohigashi) {
                sql.append("LEFT JOIN SCHREG_REGD_FI_DAT REGDFI ON REGDFI.SCHREGNO = T1.SCHREGNO ");
                sql.append("  AND REGDFI.YEAR = T1.YEAR ");
                sql.append("  AND REGDFI.SEMESTER = T1.SEMESTER ");
                sql.append("LEFT JOIN SCHREG_REGD_FI_HDAT REGDFIH ON REGDFIH.YEAR = REGDFI.YEAR ");
                sql.append("  AND REGDFIH.SEMESTER = REGDFI.SEMESTER ");
                sql.append("  AND REGDFIH.GRADE = REGDFI.GRADE ");
                sql.append("  AND REGDFIH.HR_CLASS = REGDFI.HR_CLASS ");
            }
            // 卒業情報有りの場合
            sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(        " AND T10.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = BASE.SCHREGNO AND ENTGRD.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1 = 'Z002' AND T7.NAMECD2 = BASE.SEX ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
            sql.append("AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");

            // 生徒住所
            sql.append("LEFT JOIN SCHREG_ADDRESS_DAT AS T8 ");
            sql.append("INNER JOIN(");
            sql.append("SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
            sql.append("FROM       SCHREG_ADDRESS_DAT ");
            sql.append("WHERE      SCHREGNO= " + q + " AND FISCALYEAR(ISSUEDATE) <= '" + param._year + "' ");
            sql.append(")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '02' ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T13 ON T13.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = T13.SCHREGNO AND T14.ISSUEDATE = T13.ISSUEDATE ");

            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T17 ON T17.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = T17.SCHREGNO AND T18.ISSUEDATE = T17.ISSUEDATE ");

            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = BASE.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");

            if (switchGradeCd.equals("1")) {
                sql.append("LEFT JOIN NAME_MST T24 ON T24.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T24.NAME2 AND T24.NAME3 ");
                sql.append("LEFT JOIN SCHREG_REGD_GDAT T25 ON T25.YEAR = T1.YEAR AND T25.GRADE = T1.GRADE AND T25.SCHOOL_KIND = T24.NAME1 ");
            }
            sql.append("LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
            sql.append("LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");

            return sql.toString();
        }

        private static String sql_state() {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    T4.NAME1 AS GRD_DIV_NAME ");
            sql.append(" FROM ");
            sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
            sql.append(" WHERE ");
            sql.append("    SCHREGNO = ? ");
            sql.append("    AND SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            return sql.toString();
        }
    }

    /**
     * 在籍データ
     */
    private static class Gakuseki {

        final int _i;
        final String _year;
        final String _grade;
        final int _gradeCd;
        final String _hrname;
        final String _attendno;
        final String _nendo;
        final Staff _principal;
        final Staff _principal1;
        final Staff _principal2;

        public Gakuseki(
                final int i,
                final String year,
                final String grade,
                final int gradeCd,
                final String hrname,
                final String attendno,
                final String nendo,
                final Staff principal,
                final Staff principal1,
                final Staff principal2) {
            _i = i;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrname = hrname;
            _attendno = attendno;
            _nendo = nendo;
            _principal = principal;
            _principal1 = principal1;
            _principal2 = principal2;
        }

        private String jpMonthName(String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        private String getStaffNameString(final String staffName, final String fromDate, final String toDate) {
            final String name = null == staffName ? "" : staffName;
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "～" + jpMonthName(toDate) + ")";
            return name + between;
        }

        /**
         * 在籍データのリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<Gakuseki> load(final DB2UDB db2, Param param, final Map<String, StaffMst> staffMstMap, String schregno) {
            final List<Gakuseki> gakusekiList = new ArrayList();
            final String psKey = "PS_GAKUSEKI";
            if (null == param._psMap.get(psKey)) {
                final String sql = sqlSchGradeRec(param);
                param.setPs(psKey, db2, sql);
            }

            if (param.hmap == null) {
                param.hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
            }

            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, schregno})) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String principalStaffcd1 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD1");
                final String principalStaffcd2 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD2");

                final String principalname = KnjDbUtils.getString(row, "PRINCIPALNAME");

                final String principal1FromDate = KnjDbUtils.getString(row, "PRINCIPAL1_FROM_DATE");
                final String principal1ToDate = KnjDbUtils.getString(row, "PRINCIPAL1_TO_DATE");
                final String principal2FromDate = KnjDbUtils.getString(row, "PRINCIPAL2_FROM_DATE");
                final String principal2ToDate = KnjDbUtils.getString(row, "PRINCIPAL2_TO_DATE");

                final int i = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年
                final int gradeCd = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年

                String hrname = null;
                if ("1".equals(param._useSchregRegdHdat)) {
                    hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                } else if ("0".equals(param._useSchregRegdHdat)) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrClass, param.hmap);
                }
                if (hrname == null) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrClass);
                }
                final String nendo = setNendoFormat(db2, param, (param._isSeireki ? year : KNJ_EditDate.gengou(db2, Integer.parseInt(year))) + "年度", param._year);

                final Staff principal = new Staff(year, new StaffMst(null, principalname, null, null, null), null, null, null);
                final Staff principal1 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, null);
                final Staff principal2 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);

                final Gakuseki gakuseki = new Gakuseki(i, year, grade, gradeCd, hrname, attendno, nendo, principal, principal1, principal2);
                gakusekiList.add(gakuseki);
            }
            return gakusekiList;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param) {
            final String q = "?";
            final String certifKind = "116";
            final StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, GRADE ");
            stb.append("    FROM ");
            stb.append("        (SELECT T1.SCHREGNO, MAX(T1.YEAR) AS YEAR, T1.GRADE, T1.ANNUAL ");
            stb.append("         FROM SCHREG_REGD_DAT T1 ");
            stb.append("          INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("              AND T2.GRADE = T1.GRADE ");
            stb.append("              AND T2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         WHERE ");
            stb.append("             SCHREGNO = " + q + " ");
            stb.append("         GROUP BY T1.SCHREGNO, T1.YEAR, T1.GRADE, T1.ANNUAL ");
            stb.append("        ) T1 ");
            stb.append("    GROUP BY ");
            stb.append("        GRADE ");

            stb.append(" ), T_TEACHER AS ( ");
            stb.append("     SELECT ");
            stb.append("         STAFFCD, ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         FROM_DATE, ");
            stb.append("         MIN(TO_DATE) AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT ");
            stb.append("     WHERE ");
            stb.append("         TR_DIV = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD, YEAR, GRADE, HR_CLASS, FROM_DATE ");
            stb.append(" ), T_MINMAX_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         MAX(FROM_DATE) AS MAX_FROM_DATE, ");
            stb.append("         MIN(FROM_DATE) AS MIN_FROM_DATE ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER ");
            stb.append("     GROUP BY ");
            stb.append("         YEAR, GRADE, HR_CLASS ");
            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      WHERE   T1.SCHREGNO = " + q + " ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER IN (SELECT  MAX(T2.SEMESTER)AS SEMESTER");
            stb.append("                             FROM    SCHREG_REGD_DAT T2");
            stb.append("                             WHERE   T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR");
            stb.append("                             GROUP BY T2.YEAR)");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            stb.append("     ORDER BY ");
            stb.append("         T2.YEAR, T1.FROM_DATE ");
            stb.append(" ), YEAR_PRINCIPAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR ");
            stb.append("         ,T2.STAFFCD AS PRINCIPALSTAFFCD1, T2.FROM_DATE AS PRINCIPAL1_FROM_DATE, T2.TO_DATE AS PRINCIPAL1_TO_DATE ");
            stb.append("         ,T3.STAFFCD AS PRINCIPALSTAFFCD2, T3.FROM_DATE AS PRINCIPAL2_FROM_DATE, T3.TO_DATE AS PRINCIPAL2_TO_DATE ");
            stb.append("     FROM ( ");
            stb.append("       SELECT YEAR, MIN(ORDER) AS FIRST, MAX(ORDER) AS LAST FROM PRINCIPAL_HIST GROUP BY YEAR ");
            stb.append("      ) T1 ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T2 ON T2.YEAR = T1.YEAR AND T2.ORDER = T1.LAST ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T3 ON T3.YEAR = T1.YEAR AND T3.ORDER = T1.FIRST ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("   ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");
            stb.append("   ,T13.STAFFCD AS PRINCIPALSTAFFCD1 ");
            stb.append("   ,T14.STAFFCD AS PRINCIPALSTAFFCD2 ");
            stb.append("   ,T12.PRINCIPAL1_FROM_DATE, T12.PRINCIPAL1_TO_DATE ");
            stb.append("   ,T12.PRINCIPAL2_FROM_DATE, T12.PRINCIPAL2_TO_DATE ");

            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + certifKind + "'");

            stb.append(" LEFT JOIN YEAR_PRINCIPAL T12 ON T12.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN STAFF_MST T13 ON T13.STAFFCD = T12.PRINCIPALSTAFFCD1 ");
            stb.append(" LEFT JOIN STAFF_MST T14 ON T14.STAFFCD = T12.PRINCIPALSTAFFCD2 ");

            stb.append(" ORDER BY T1.YEAR, T1.HR_CLASS ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフマスタ>>。
     */
    private static class StaffMst {
        /**pkg*/ static StaffMst Null = new StaffMst(null, null, null, null, null);
        final String _staffcd;
        final String _name;
        final String _kana;
        final String _nameReal;
        final String _kanaReal;
        private final Map<String, Map> _yearStaffNameSetUp;
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map nameSetup = _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map nameSetup = _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }

        public List<String> getNameLine(final String year) {
            final String[] name;
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    if (StringUtils.isBlank(_name)) {
                        name = new String[]{_nameReal};
                    } else {
                        final String n = "（" + _name + "）";
                        if ((null == _nameReal ? "" : _nameReal).equals(_name)) {
                            name =  new String[]{_nameReal};
                        } else if (KNJ_EditEdit.getMS932ByteLength(_nameReal + n) > 26) {
                            name =  new String[]{_nameReal, n};
                        } else {
                            name =  new String[]{_nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    name = new String[]{_nameReal};
                }
            } else {
                name = new String[]{_name};
            }
            return Arrays.asList(name);
        }

        public static StaffMst get(final Map<String, StaffMst> staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map<String, StaffMst> load(final DB2UDB db2, final String year) {
            final Map<String, StaffMst> rtn = new HashMap();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Map m : KnjDbUtils.query(db2, sql1)) {
                final String staffcd = (String) m.get("STAFFCD");
                final String name = (String) m.get("STAFFNAME");
                final String kana = (String) m.get("STAFFNAME_KANA");
                final String nameReal = (String) m.get("STAFFNAME_REAL");
                final String kanaReal = (String) m.get("STAFFNAME_KANA_REAL");

                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);

                rtn.put(s._staffcd, s);
            }

            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            for (final Map m : KnjDbUtils.query(db2, sql2)) {
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = rtn.get(m.get("STAFFCD"));

                final Map nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put((String) m.get("YEAR"), nameSetupDat);
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + ", nameSetupDat=" + _yearStaffNameSetUp + ")";
        }
    }


    /**
     * 出欠データ
     */
    private static class Attend {

        final String _year;
        final int _g;
        final String _lesson;
        final String _suspendMourning;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _requirePresent;
        final String _present;
        final String _absent;
        final String _late;
        final String _early;

        public Attend(
                final String year,
                final int g,
                final String lesson,
                final String suspendMourning,
                final String suspend,
                final String mourning,
                final String abroad,
                final String requirePresent,
                final String present,
                final String absent,
                final String late,
                final String early) {
            _year = year;
            _g = g;
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
            _late = late;
            _early = early;
        }

        /**
         *  年次ごとの出欠データのリストを得る
         */
        public static List<Attend> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<Attend> attendList = new ArrayList<Attend>();
            final String sql = getAttendSql(param);
            //log.debug(" attendrec sql = " + sql);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] {schregno, schregno, schregno})) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final int g = param.getGradeCd(year, KnjDbUtils.getString(row, "ANNUAL"));
                final String lesson = KnjDbUtils.getString(row, "LESSON");
                final String suspendMourning = KnjDbUtils.getString(row, "SUSPEND_MOURNING");
                final String suspend = KnjDbUtils.getString(row, "SUSPEND");
                final String mourning = KnjDbUtils.getString(row, "MOURNING");
                final String abroad = KnjDbUtils.getString(row, "ABROAD");
                final String requirePresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
                final String present = KnjDbUtils.getString(row, "PRESENT");
                final String absent = KnjDbUtils.getString(row, "ABSENT");
                final String late = KnjDbUtils.getString(row, "LATE");
                final String early = KnjDbUtils.getString(row, "EARLY");

                final Attend attend = new Attend(year, g, lesson, suspendMourning, suspend, mourning, abroad, requirePresent, present, absent, late, early);
                attendList.add(attend);
            }
            return attendList;
        }

        /**
         *  priparedstatement作成  出欠の記録
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String getAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        SCHREG_ATTENDREC_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("   GROUP BY ");
            stb.append("        ANNUAL ");
            stb.append(" ), SEMES AS ( ");
            stb.append("    SELECT ");
            stb.append("        YEAR, SCHREGNO, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append("    FROM ");
            stb.append("        ATTEND_SEMES_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("   GROUP BY ");
            stb.append("        YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR, ");
            stb.append("        T1.ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(CLASSDAYS,0) ");
            stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            stb.append(             "END AS LESSON, ");
            stb.append(        "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append(        "VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append(        "VALUE(MOURNING,0) AS MOURNING, ");
            stb.append(        "VALUE(ABROAD,0) AS ABROAD, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append(             "END AS REQUIREPRESENT, ");
            stb.append(        "VALUE(PRESENT,0) AS PRESENT, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append(             "END AS ABSENT, ");
            stb.append(        "VALUE(SEMES.LATE, 0) AS LATE, ");
            stb.append(        "VALUE(SEMES.EARLY, 0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.ANNUAL = T1.ANNUAL ");
            stb.append(        "LEFT JOIN SEMES ON SEMES.YEAR = T1.YEAR AND SEMES.SCHREGNO = T1.SCHREGNO ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append(        " AND S1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            stb.append("WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append(    "AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフクラス>>。
     */
    private static class Staff {
        /**pkg*/ static Staff Null = new Staff(null, StaffMst.Null, null, null, null);
        final String _year;
        final StaffMst _staffMst;
        final String _dateFrom;
        final String _dateTo;
        final String _stampNo;
        public Staff(final String year, final StaffMst staffMst, final String dateFrom, final String dateTo, final String stampNo) {
            _year = year;
            _staffMst = staffMst;
            _dateFrom = dateFrom;
            _dateTo = dateTo;
            _stampNo = stampNo;
        }

        public String getNameString() {
            final StringBuffer stb = new StringBuffer();
            final List name = _staffMst.getNameLine(_year);
            for (int i = 0; i < name.size(); i++) {
                if (null == name.get(i)) continue;
                stb.append(name.get(i));
            }
            return stb.toString();
        }

        public List getNameBetweenLine() {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";

            final List rtn;
            if (KNJ_EditEdit.getMS932ByteLength(getNameString() + between) > 26) {
                rtn = Arrays.asList(new String[]{getNameString(), between});
            } else {
                rtn = Arrays.asList(new String[]{getNameString() + between});
            }
            return rtn;
        }

        private String toYearDate(final String date, final String year) {
            if (null == date) {
                return null;
            }
            final String sdate = year + "-04-01";
            final String edate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
            if (date.compareTo(sdate) <= 0) {
                return sdate;
            } else if (date.compareTo(edate) >= 0) {
                return edate;
            }
            return date;
        }

        private String jpMonthName(final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        public String toString() {
            return "Staff(year=" + _year + ", staffMst=" + _staffMst + ", dateFrom=" + _dateFrom + ", dateTo=" + _dateTo + ", stampNo="+ _stampNo + ")";
        }
    }

    /**
     * 住所データ
     */
    private static class Address {
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCd;
        final boolean _isPrintAddr2;

        private Address(final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _issuedate = issuedate;
            _address1 = addr1;
            _address2 = addr2;
            _zipCd = zip;
            _isPrintAddr2 = isPrintAddr2;
        }

        /**
         * 住所履歴を得る
         */
        public static List<Address> load(final DB2UDB db2, final Param param, final String schregno, final boolean isGuardian) {
            final List<Address> addressList = new ArrayList();
            final String sql = isGuardian ? sqlAddressDat(true) : sqlAddressDat(false);
            for (final Map row : KnjDbUtils.query(db2, sql, new Object[] {schregno, param._year})) {
                final String issuedate = KnjDbUtils.getString(row, "ISSUEDATE");
                final String address1 = KnjDbUtils.getString(row, "ADDR1");
                final String address2 = KnjDbUtils.getString(row, "ADDR2");
                final boolean isPrintAddr2 = "1".equals(KnjDbUtils.getString(row, "ADDR_FLG"));
                final String zipCd = KnjDbUtils.getString(row, "ZIPCD");

                final Address address = new Address(issuedate, address1, address2, zipCd, isPrintAddr2);
                addressList.add(address);
            }
            return addressList;
        }

        public static String sqlAddressDat(final boolean isGuardianAddress) {

            StringBuffer stb = new StringBuffer();
            if (isGuardianAddress) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            }
            stb.append("WHERE  ");
            stb.append("       T1.SCHREGNO = ?  ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= ?  ");
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE ");
            return stb.toString();
        }
        public String toString() {
            return "AddressRec(" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }
    }

    /**
     * 総合的な学習の時間の記録・外国語活動の記録
     */
    private static class HTrainremarkPdat {
        final String _year;
        int _g;
        String _totalRemark;
        String _attendrecRemark;
        String _foreignlangact4;
        public HTrainremarkPdat(final String year) {
            _year = year;
        }

        public static List<HTrainremarkPdat> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<HTrainremarkPdat> htrainRemarkDatList = new ArrayList();
            try {
                final String psKey = "KEY_HTRAINREMARK_P";
                if (null == param._psMap.get(psKey)) {
                    param.setPs(psKey, db2, getRemarkRecordSql(param._year));
                }
                for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno})) {

                    final HTrainremarkPdat remark = new HTrainremarkPdat(KnjDbUtils.getString(row, "YEAR"));

                    remark._g = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                    remark._totalRemark = KnjDbUtils.getString(row, "TOTALREMARK");
                    remark._attendrecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                    remark._foreignlangact4 = KnjDbUtils.getString(row, "FOREIGNLANGACT4");

                    htrainRemarkDatList.add(remark);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            }
            return htrainRemarkDatList;
        }

        private static HTrainremarkPdat getHTrainRemarkDat(final List<HTrainremarkPdat> list, final String year) {
            for (final HTrainremarkPdat d : list) {
                if (d._year.equals(year)) {
                    return d;
                }
            }
            return null;
        }

        /**
         *  priparedstatement作成  総合的な学習の時間の記録
         */
        private static String getRemarkRecordSql(final String year) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_P_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= '" + year + "' ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T1.ANNUAL ");
            stb.append(      " ,TOTALSTUDYACT ");
            stb.append(      " ,TOTALSTUDYVAL ");
            stb.append(      " ,SPECIALACTREMARK ");
            stb.append(      " ,TOTALREMARK ");
            stb.append(      " ,ATTENDREC_REMARK ");
            stb.append(      " ,VIEWREMARK ");
            stb.append(      " ,BEHAVEREC_REMARK ");
            stb.append(      " ,FOREIGNLANGACT1 ");
            stb.append(      " ,FOREIGNLANGACT2 ");
            stb.append(      " ,FOREIGNLANGACT3 ");
            stb.append(      " ,FOREIGNLANGACT4 ");
            stb.append("FROM    HTRAINREMARK_P_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.ANNUAL = T1.ANNUAL ");
            return stb.toString();
        }
    }

    /**
     * 行動の記録・特別活動の記録
     */
    private static class ActRecord {
        final int _g;
        final String _record;
        final int _code;
        final String _div;
        public ActRecord(final int g, final String record, final int code, final String div) {
            _g = g;
            _record = record;
            _code = code;
            _div = div;
        }

        /**
         *  SVF-FORM 印刷処理 明細
         *  行動の記録・特別活動の記録
         */
        public static List<ActRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final String psKey = "PS_ACT";
            if (null == param._psMap.get(psKey)) {

                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD_YEAR AS ( ");
                stb.append("    SELECT ");
                stb.append("        MAX(YEAR) AS YEAR, ANNUAL ");
                stb.append("    FROM ");
                stb.append("        BEHAVIOR_DAT ");
                stb.append("    WHERE ");
                stb.append("             SCHREGNO = ? ");
                stb.append("    GROUP BY ");
                stb.append("        ANNUAL ");
                stb.append(" ) ");
                stb.append("SELECT ");
                stb.append("     DIV ");
                stb.append("    ,CODE ");
                stb.append("    ,T1.ANNUAL ");
                stb.append("    ,RECORD ");
                stb.append("FROM    BEHAVIOR_DAT T1 ");
                stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.ANNUAL = T1.ANNUAL ");
                stb.append("WHERE   T1.YEAR <= '" + param._year + "' ");
                stb.append(    "AND T1.SCHREGNO = ? ");

                param.setPs(psKey, db2, stb.toString());
            }
            final List<ActRecord> actList = new ArrayList();
            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] {schregno, schregno})) {

                final String record = KnjDbUtils.getString(row, "RECORD");
                final int g = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                final int code = Integer.parseInt(KnjDbUtils.getString(row, "CODE"));
                final String div = KnjDbUtils.getString(row, "DIV");

                final ActRecord act = new ActRecord(g, record, code, div);
                actList.add(act);
            }
            return actList;
        }
    }

    /**
     * 観点の教科
     */
    private static class ClassView {
        final String _classcd;  //教科コード
        final String _subclasscd; // 科目コード
        final String _classname;  //教科名称
        final String _subclassname;  //科目名称
        final String _electdiv;
        final List<View> _views;

        public ClassView(
                final String classcd,
                final String subclasscd,
                final String classname,
                final String subclassname,
                final String electdiv
        ) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _views = new ArrayList();
        }

        public int e() {
            return "1".equals(_electdiv) ? 2 : 1;  //必修:1 選択:2;
        }

        public void addView(final View view) {
            _views.add(view);
        }

        public int getViewNum() {
            int c = 0;
            String viewcdOld = "";
            for (final View view : _views) {
                if (view._viewcd != null && !viewcdOld.equals(view._viewcd)) {
                    c += 1;
                    viewcdOld = view._viewcd;
                }
            }
            return c;
        }

        public String toString() {
            return "[" + _classcd + ":" + _subclasscd + ":" + _classname + " e = " + _electdiv + "]";
        }

        private static ClassView getClassView(final List<ClassView> classViewList, final String classcd, final String subclasscd, final String classname, final String electdiv) {
            if (null == classcd || null == subclasscd) {
                return null;
            }
            ClassView classView = null;
            for (final ClassView classView0 : classViewList) {
                if (classView0._classcd.equals(classcd) && subclasscd.equals(classView0._subclasscd) && classView0._classname.equals(classname) && classView0._electdiv.equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<ClassView> load(final DB2UDB db2, final Param param, final String schregno, final SchregEntGrdHistDat entGrdHistDat) {
            final List<ClassView> classViewList = new ArrayList();
            final String psKey;
            final String sql;
            final String[] psParam;
            if (param._useJviewnameSubMstCurriculumcd && null != entGrdHistDat._curriculumYearCurriculumcd) {
                psKey = "PS_CLASS_VIEW_SET_CURRICULUM_YEAR";
                sql = getViewRecordSql(param, true);
                psParam = new String[] {schregno, schregno, schregno, entGrdHistDat._curriculumYearCurriculumcd};
            } else {
                psKey = "PS_CLASS_VIEW";
                sql = getViewRecordSql(param, false);
                psParam = new String[] {schregno, schregno, schregno};
            }
            if (null == param._psMap.get(psKey)) {
                log.info(" class view sql = "+ sql);
                param.setPs(psKey, db2, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), psParam)) {

                //教科コードの変わり目
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String viewcd = KnjDbUtils.getString(row, "VIEWCD");
                final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                final String status = KnjDbUtils.getString(row, "STATUS");
                final String electdiv = KnjDbUtils.getString(row, "ELECTDIV");
                final int g = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "GRADE")); // 学年

                ClassView classView = getClassView(classViewList, classcd, subclasscd, classname, electdiv);
                if (null == classView) {
                    classView = new ClassView(classcd, subclasscd, classname, subclassname, electdiv);
                    classViewList.add(classView);
                }
                final View view = new View(subclasscd, viewcd, viewname, status, g);
                classView.addView(view);
            }
            return classViewList;
        }

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param, final boolean useJviewnameSubMstCurriculumcd) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      ,CLASSCD ");
                stb.append("      ,SCHOOL_KIND ");
                stb.append("      ,CURRICULUM_CD ");
            }
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT  YEAR ");
            stb.append(         ",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT ");
            stb.append("    T2.YEAR ");
            stb.append("   ,T2.GRADE ");
            stb.append("   ,VALUE(T3.ELECTDIV, '0') AS ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD ");
                stb.append("   ,T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("   ,T3.CLASSCD AS CLASSCD ");
                stb.append("   ,T2.SUBCLASSCD AS SUBCLASSCD ");
            }
//            stb.append("   ,VALUE(T3.CLASSORDERNAME1, T3.CLASSNAME) AS CLASSNAME ");
            stb.append("   ,VALUE(T3.CLASSORDERNAME2, T3.CLASSORDERNAME1, T3.CLASSNAME) AS CLASSNAME "); // 「図工」を印字したい
            stb.append("   ,VALUE(T4.SUBCLASSORDERNAME1, T4.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("   ,VALUE(T3.SHOWORDER, -1) AS SHOWORDERCLASS ");
            stb.append("   ,T2.VIEWCD ");
            stb.append("   ,T2.VIEWNAME ");
            stb.append("   ,T1.STATUS ");
            stb.append("FROM  ( SELECT ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , W1.CLASSCD ");
                stb.append("          , W1.SCHOOL_KIND ");
                stb.append("          , W1.CURRICULUM_CD ");
            }
            stb.append("          , W1.SUBCLASSCD ");
            stb.append("          , W1.VIEWCD ");
            stb.append("          , VIEWNAME ");
            stb.append("          , VALUE(W1.SHOWORDER, -1) AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("                INNER JOIN JVIEWNAME_SUB_YDAT W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND W3.CLASSCD = W1.CLASSCD ");
                stb.append("          AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
                stb.append("          AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
            }
            stb.append("          AND W3.VIEWCD = W1.VIEWCD ");
            stb.append("               INNER JOIN SCHREG_DATA W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        WHERE W1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (useJviewnameSubMstCurriculumcd) {
                stb.append("        AND W1.CURRICULUM_CD = ? ");
            }
            stb.append("      ) T2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                stb.append("  AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T4.CLASSCD = T2.CLASSCD ");
                stb.append("  AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("  AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR ");
            stb.append("    AND T1.VIEWCD = T2.VIEWCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            }
            if (!param._isSundaikoufu && "1".equals(param._useCurriculumcd)) {
                stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("    AND T1.SUBCLASSCD = T2.SUBCLASSCD  ");
            if (Integer.parseInt(param._year) >= 2020) {
                stb.append(" WHERE T2.YEAR >= '" + param._year + "' ");
            }
            stb.append("ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T3.SCHOOL_KIND, ");
                stb.append("    T2.CURRICULUM_CD, ");
            }
            stb.append("    VALUE(T3.ELECTDIV, '0'), ");
            stb.append("    T2.SUBCLASSCD, ");
            stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
            stb.append("    T2.VIEWCD, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 観点データ
     */
    private static class View {
        final String _subclasscd;  //科目コード
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final String _status; //観点
        final int _g; // 学年

        public View(final String subclasscd, final String viewcd, final String viewname, final String status, final int g) {
            _subclasscd = subclasscd;
            _viewcd = viewcd;
            _viewname = viewname;
            _status = status;
            _g = g;
        }

        public String toString() {
            return "(" + _subclasscd + "-" + _viewcd + ":" + (null == _status ? " " : _status) + ")";
        }
    }

    /**
     * 評定データ
     */
    private static class ValueRecord {
        final int _g;
        final String _classCd;
        final String _subclassCd;
        final String _electDiv;
        final String _className;
        final String _value; //評定
        public ValueRecord(final int g, final String classCd, final String subclassCd, final String electDiv, final String className, final String value) {
            _g = g;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _electDiv = electDiv;
            _className = className;
            _value = value;
        }

        public String toString() {
            return "ValueRecord(" + _classCd + ":" + _subclassCd + ":" + _className + ")";
        }

        public static List<ValueRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<ValueRecord> valueRecordList = new ArrayList();
            final String psKey = "PS_VALUE_REC";
            if (null == param._psMap.get(psKey)) {
                final String sql = getValueRecordSql(param);
                param.setPs(psKey, db2, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] {schregno, schregno, schregno})) {

                //教科コードの変わり目
                final int g = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "GRADE")); // 学年
                final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                final String classCd = KnjDbUtils.getString(row, "CLASSCD");
                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String className = KnjDbUtils.getString(row, "CLASSNAME");
                //評定出力
                final String value = KnjDbUtils.getString(row, "VALUE");

                final ValueRecord valueRecord = new ValueRecord(g, classCd, subclassCd, electDiv, className, value);
                valueRecordList.add(valueRecord);
            }

            return valueRecordList;
        }

        /**
         *  priparedstatement作成  成績データ（評定）
         */
        private static String getValueRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //評定の表
            stb.append(" VALUE_DATA AS( ");
            stb.append("   SELECT ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append("       ,VALUATION AS VALUE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_STUDYREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND T1.YEAR <= '" + param._year + "' ");
            stb.append(" ) ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("  GROUP BY ");
            stb.append("      YEAR ");
            stb.append("      ,ANNUAL ");
            stb.append("      ,GRADE ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT ");
            stb.append("     T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND AS CLASSCD ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD AS CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD AS SUBCLASSCD ");
            }
            stb.append("    ,MAX(VALUE(T3.CLASSORDERNAME1,T3.CLASSNAME)) AS CLASSNAME ");
            stb.append("    ,MAX(VALUE(T3.SHOWORDER, -1)) AS SHOWORDERCLASS ");
            stb.append("    ,MAX(T5.VALUE) AS VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD ");
            }
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD ");
            }
            stb.append("    ,T3.ELECTDIV ");
            stb.append("    ,T2.GRADE ");
            return stb.toString();
        }
    }

    private static class SchregEntGrdHistDat {

        final static SchregEntGrdHistDat NULL = new SchregEntGrdHistDat(null, null);

        final String _tengakuSakiZenjitu;
        final String _curriculumYearCurriculumcd;
        public SchregEntGrdHistDat(final String tengakuSakiZenjitu, final String curriculumYearCurriculumcd) {
            _tengakuSakiZenjitu = tengakuSakiZenjitu;
            _curriculumYearCurriculumcd = curriculumYearCurriculumcd;
        }

        private static SchregEntGrdHistDat load(final DB2UDB db2, final Param param, final String schregno) {
            final String psKey = "PS_SCHREG_ENT_GRD_HIST_DAT";
            if (null == param._psMap.get(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.TENGAKU_SAKI_ZENJITU ");
                stb.append("   , T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
                stb.append("   , (SELECT MAX(NAMECD2) AS CURRICULUM_CD FROM NAME_MST WHERE NAMECD1 = 'Z018' AND NAME3 <= T1.CURRICULUM_YEAR) AS CURRICULUM_YEAR_CURRICULUM_CD ");
                stb.append(" FROM ");
                stb.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = ? ");
                stb.append("     AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");

                if (param._isOutputDebugQuery) {
                    log.info(" " + psKey + ":" + stb.toString());
                }
                param.setPs(psKey, db2, stb.toString());
            }
            SchregEntGrdHistDat rtn = null;
            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] {schregno})) {

                final String tengakuSakiZenjitu = KnjDbUtils.getString(row, "TENGAKU_SAKI_ZENJITU");
                final String curriculumYearCurriculumcd = KnjDbUtils.getString(row, "CURRICULUM_YEAR_CURRICULUM_CD");
                rtn = new SchregEntGrdHistDat(tengakuSakiZenjitu, curriculumYearCurriculumcd);
            }
            return null == rtn ? NULL : rtn;
        }
    }

    private static class Form {

        private int VIEW_LINE1_MAX;

        final Vrw32alp _svf;
        final Param _param;
        private String _form;
        private Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap();
        private Map<String, String> _formFieldInfoError = new HashMap();

        Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        public void printMain(final DB2UDB db2, final Student student) {
            String form;
            if (_param._isHibarigaoka) {
                form = "KNJA133E_HIBARI.frm";
                VIEW_LINE1_MAX = 15 + 15;
            } else if (_param._isRitsumeikan) {
                form = "KNJA133E_RITSUMEIKAN.frm";
                VIEW_LINE1_MAX = 18 + 18;
            } else {
                form = "KNJA133E.frm";
                VIEW_LINE1_MAX = 18 + 13;
            }
            setForm(form, 4);
            if (_param._isMusashinohigashi && "2".equals(student._personalInfo._recordDiv)) {
                form = createMusashinohigashiCDEform(form);
                setForm(form, 4);
            }

            String schoolname = "";
            if (null != _param._schoolInfo) {
                schoolname = StringUtils.defaultString(_param._schoolInfo._certifSchoolName, _param._schoolInfo._schoolName1);
            }
            _svf.VrsOut("TITLE", StringUtils.defaultString(schoolname) + "　児童指導要録抄本"); // タイトル

            printStudent(db2, student);

            printShoken(student);

            printSchoolInfo1(db2, student);

            printValueRecord(student);

//            printRegd(student); // 年次・ホームルーム・整理番号
        }

        private String getFieldForData(final String[] fields, final String data) {
            final int datasize = getMS932ByteLength(data);
            String fieldFound = null;
            searchField:
            for (int i = 0; i < fields.length; i++) {
                final String fieldname = fields[i];
                final SvfField svfField = getSvfField(fieldname);
                if (null == svfField) {
                    continue searchField;
                }
                fieldFound = fieldname;
                if (datasize <= svfField._fieldLength) {
                    return fieldname;
                }
            }
            return fieldFound;
        }

        private List<ClassView> getValueOnlyViewClassList(final Student student) {
            final List<ClassView> valueOnlyViewClassList = new ArrayList();
            final Map<String, ClassView> classviewMap = new HashMap();
            classviweMapSet:
            for (final ValueRecord value : student._valueRecordList) {

                if (null == value._value || value._g != GRADE6) {
                    continue;
                }
                if (classviewMap.containsKey(value._classCd)) {
                    continue classviweMapSet;
                }

                for (final ClassView classview : student._classViewList) {
                    if (isTarget(_param, classview, value)) {
                        classviewMap.put(value._classCd, classview);
                        continue classviweMapSet;
                    }
                }

                log.info(" 観点教科作成:" + value._classCd + ", " + value._subclassCd);
                // 未設定なので作成
                final ClassView classview = new ClassView(value._classCd, value._subclassCd, value._className, value._subclassCd, value._electDiv);
                valueOnlyViewClassList.add(classview);
                classviewMap.put(value._classCd, classview);
            }
            return valueOnlyViewClassList;
        }

        /**
         *  評定出力処理
         */
        private void printValueRecord(final Student student) {
            int hyoteiLine1 = 1;
            int line1 = 0;  //欄の出力行数

            final List<ClassView> valuationViewClassExcludeList = getValuationViewClassExcludeList(student);
            final List<ClassView> valueOnlyViewClassList = getValueOnlyViewClassList(student);
            final List<ClassView> classViewList = new ArrayList(student._classViewList);
            classViewList.addAll(valueOnlyViewClassList);
            classViewList.removeAll(valuationViewClassExcludeList);
            if (_param._isOutputDebug) {
                log.info(" valueOnlyViewClassList = " + valueOnlyViewClassList);
                log.info(" valuationViewClassExcludeList = " + valuationViewClassExcludeList);
                log.info(" classViewList source = " + student._classViewList);
                log.info(" classViewList = " + classViewList);
            }
            for (int hi = 0; hi < classViewList.size(); hi++) {
                final ClassView classview = classViewList.get(hi);
                final String classname = getShowname(student, classview);
                final String value = getValue(student, hyoteiLine1, classview); // 評定

                final List<List<View>> viewCdListList = getViewCdListList(classview);
                if (viewCdListList.size() == 0) {
                    line1 += 1;
                    _svf.VrsOut("CLASS1", classname);  //教科名
                    _svf.VrsOut("ASSESS2", value);  //評定
                    _svf.VrEndRecord();
                } else {
                    for (int i = 0; i < viewCdListList.size(); i++) {
                        final List<View> viewCdList = viewCdListList.get(i);
                        final View view0 = viewCdList.get(0);
//                      log.debug(" viewcdlist = " + viewCdList);
                        line1 += 1;
                        _svf.VrsOut("CLASS1", classname);  //教科名
                        _svf.VrsOut("ASSESS2", value);  //評定
                        _svf.VrsOut("VIEW1", view0._viewname);  //観点名称
                        _svf.VrsOut("ASSESS1", getView(student, line1, classview, i, viewCdList));  //観点
                        _svf.VrEndRecord();
                    }
                }
                hyoteiLine1++;
            }

            if (line1 != 0 && line1 % VIEW_LINE1_MAX != 0) {
                for (int i = line1 % VIEW_LINE1_MAX; i < VIEW_LINE1_MAX; i++) {
                    _svf.VrsOut("CLASS1", "");  //教科コード
                    line1++;
                    _svf.VrEndRecord();
                }
            }
        }

        private String getView(final Student student, final int line1, final ClassView classview, final int i, final List<View> viewCdList) {
            String view = null;
            for (final View viewe : viewCdList) {
                if (viewe._g == GRADE6) {
                    view = viewe._status;
                }
            }
            if (_param.isSlashView(student._personalInfo._recordDiv, classview._subclasscd, String.valueOf(GRADE6), i + 1)) {
                view = "／";  //観点スラッシュ
            }
            return view;
        }

        private String getValue(final Student student, final int line1, final ClassView classview) {
            final List<ValueRecord> valueList = new ArrayList();
            for (final ValueRecord value : student._valueRecordList) {
                if (isTarget(_param, classview, value)) {
                    valueList.add(value);
                }
            }
            String value = "";
            for (final ValueRecord valuer : valueList) {
                if (valuer._g != GRADE6) {
                    continue;
                }
                if (valuer._value != null) {
                    if ("1".equals(valuer._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                        if ("11".equals(valuer._value)) {
                            value = "A";
                        } else if ("22".equals(valuer._value)) {
                            value = "B";
                        } else if ("33".equals(valuer._value)) {
                            value = "C";
                        } else {
                            value = valuer._value;
                        }
                    } else {
                        value = valuer._value;
                    }
                }
            }
            if (_param.isSlashValue(student._personalInfo._recordDiv, classview._subclasscd, String.valueOf(GRADE6))) {
                value = "／";
            }
            return value;
        }

        private static boolean isTarget(final Param param, final ClassView classview, final ValueRecord value) {
            if (null == value._classCd || !value._classCd.equals(classview._classcd)) {
                return false;
            }
            boolean isTarget = false;
            if (param._isSundaikoufu) {
                try {
                    final String[] vSplit = StringUtils.split(value._subclassCd, "-");
                    final String[] cvSplit = StringUtils.split(classview._subclasscd, "-");
                    if (vSplit[0].equals(cvSplit[0]) && vSplit[1].equals(cvSplit[1]) && vSplit[3].equals(cvSplit[3])) {
                        isTarget = true;
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            } else if (value._subclassCd.equals(classview._subclasscd)) {
                isTarget = true;
            }
            return isTarget;
        }

        private List<ActRecord> tokubetsuKatsudouNoKiroku(final Student student) {
            List<ActRecord> list = new ArrayList<ActRecord>();
            for (final ActRecord act : student._actRecordList) {
                if (act._g == GRADE6) {
                    if ("1".equals(act._record) && "4".equals(act._div)) {
                        list.add(act);
                    }
                }
            }
            return list;
        }

        private List<ActRecord> koudouNoKiroku(final Student student) {
            List<ActRecord> list = new ArrayList<ActRecord>();
            for (final ActRecord act : student._actRecordList) {
                if (act._g == GRADE6) {
                    if ("1".equals(act._record) && "3".equals(act._div)) {
                        list.add(act);
                    }
                }
            }
            return list;
        }

        private void printShoken(final Student student) {
            for (final ActRecord act : tokubetsuKatsudouNoKiroku(student)) {
                if (act._g == GRADE6) {
                    _svf.VrsOutn("SPECIALACT1", act._code, "○"); //特別行動の記録
                }
            }

            for (final Integer cd : _param._specialActMstMap.keySet()) {
                final Map specialActMst = _param._specialActMstMap.get(cd);
                _svf.VrsOutn("SP_ACT_NAME", cd.intValue(), KnjDbUtils.getString(specialActMst, "NAME1")); // 特別活動の記録名称
                if (_param._isMusashinohigashi && "2".equals(student._personalInfo._recordDiv)) {
                    if (2 == cd.intValue() || 3 == cd.intValue()) {
                        _svf.VrsOutn("SPECIALACT1", cd.intValue(), "／");
                    }
                } else {
                    final int g = GRADE6;
                    if ("1".equals(KnjDbUtils.getString(specialActMst, "FLG" + String.valueOf(g)))) {
                        _svf.VrsOutn("SPECIALACT1", cd.intValue(), "／");
                    }
                }
            }

            final int maxLine = 4;
            for (int i = 0; i < _param._behaviorNameList.size(); i++) {
                final int col = i / maxLine + 1;
                final int line = i - (col - 1) * maxLine + 1;
                final Map row = _param._behaviorNameList.get(i);
                _svf.VrsOutn("ACTION_NAME" + String.valueOf(col), line, KnjDbUtils.getString(row, "NAME1")); //行動の記録名称
            }

            for (final ActRecord act : koudouNoKiroku(student)) {
                if (act._g == GRADE6) {
                    final int col = (act._code - 1) / maxLine + 1;
                    final int line = act._code - (col - 1) * maxLine;
                    _svf.VrsOutn("ACTION" + String.valueOf(col), line, "○"); //行動の記録
                }
            }

            final KNJPropertiesShokenSize totalremarkSize;
            if (_param._isHibarigaoka) {
                totalremarkSize = new KNJPropertiesShokenSize(27, 16);
            } else if (_param._isRitsumeikan) {
                totalremarkSize = new KNJPropertiesShokenSize(22, 15);
            } else {
                totalremarkSize = new KNJPropertiesShokenSize(44, 12);
            }
            // 総合的な学習の時間の記録・総合所見
            VrsOutnRenban(_svf, "TOTALREMARK", getPrintTotalRemark(_param, student, totalremarkSize), totalremarkSize, _param); //総合所見

            KNJPropertiesShokenSize doutokuSize;
            if (_param._isHibarigaoka) {
                doutokuSize = KNJPropertiesShokenSize.getShokenSize(null, 28, 5);
            } else if (_param._isRitsumeikan) {
                doutokuSize = KNJPropertiesShokenSize.getShokenSize(null, 32, 4);
            } else {
                doutokuSize = KNJPropertiesShokenSize.getShokenSize(_param._HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P, 32, 2);
            }

            String attendRemark = null;
            String foreignlangact4 = null;
            HTrainremarkPdat remark = student.getHtrainremarkPDatGrade(GRADE6);
            if (null != remark) {
                attendRemark = remark._attendrecRemark;
                foreignlangact4 = remark._foreignlangact4;
            }
            VrsOutnRenban(_svf, "MORAL1", foreignlangact4, doutokuSize, _param);

            for (final Attend attend : student._attendList) {
                if (attend._g == GRADE6) {
                    _svf.VrsOut("LESSON", attend._lesson);
                    _svf.VrsOut("SUSPEND_MOURNING", attend._suspendMourning);
                    _svf.VrsOut("PRESENT", attend._requirePresent);
                    _svf.VrsOut("ABSENCE", attend._absent);
                    _svf.VrsOut("ATTEND", attend._present);
                }
            }
            KNJPropertiesShokenSize shukketsuBikouSize;
            if (_param._isHibarigaoka) {
                shukketsuBikouSize = KNJPropertiesShokenSize.getShokenSize(null, 40, 1);
            } else if (_param._isRitsumeikan) {
                shukketsuBikouSize = KNJPropertiesShokenSize.getShokenSize(null, 35, 1);
            } else {
                shukketsuBikouSize = KNJPropertiesShokenSize.getShokenSize(null, 35, 2);
            }
            VrsOutRenban(_svf, "REMARK", attendRemark, shukketsuBikouSize, _param);
        }

        private String getPrintTotalRemark(final Param param, final Student student, final KNJPropertiesShokenSize knja133eShokenSize) {
            String totalRemark = null;
            HTrainremarkPdat remark = student.getHtrainremarkPDatGrade(GRADE6);
            if (null != remark) {
                totalRemark = remark._totalRemark;
            }
            if (null == totalRemark) {
                return totalRemark;
            }
            final Map headSpaceSet = new TreeMap();
            if (!StringUtils.isEmpty(param._HTRAINREMARK_DAT_TOTALREMARK_SIZE_P)) {
                final KNJPropertiesShokenSize shidoYorokuSogoShokenSize = KNJPropertiesShokenSize.getShokenSize(param._HTRAINREMARK_DAT_TOTALREMARK_SIZE_P, 22, 15);
                // 総合的な学習の時間の記録・総合所見
                final List<String> token1 = getTokenList(totalRemark, shidoYorokuSogoShokenSize.getKeta(), shidoYorokuSogoShokenSize._gyo, param);
                for (final String t : token1) {
                    if (StringUtils.isBlank(t) || t.startsWith("・")) {
                        continue;
                    }
                    final int blankIdx = headBlankLength(t);
                    if (blankIdx == -1) {
                        getMappedList(headSpaceSet, "").add(t);
                    } else {
                        getMappedList(headSpaceSet, t.substring(0, blankIdx + 1)).add(t);
                    }
                }
                if (headSpaceSet.size() == 1 && "".equals(headSpaceSet.keySet().iterator().next())) {
                    // 改行調整しない
                    log.info("改行調整しない: " + headSpaceSet);
                } else if (headSpaceSet.size() > 0) {
                    // 改行調整する
                    log.info("改行調整する: " + headSpaceSet);
                    final List<String> tokenList = getTokenList(StringUtils.replace(totalRemark, "\r\n", "\n"), shidoYorokuSogoShokenSize.getKeta(), shidoYorokuSogoShokenSize._gyo, param);
                    final List<String> newTokenList = new ArrayList();
                    for (final String token : tokenList) {
                        if (!newTokenList.isEmpty() && " ".equals(token)) {
                            continue;
                        }
                        if (StringUtils.isBlank(token) || token.startsWith("・") || newTokenList.isEmpty()) {
                            newTokenList.add(token);
                            continue;
                        }
                        final int blankIdx = headBlankLength(token);
                        if (blankIdx == -1) {
                            newTokenList.add(token);
                        } else {
                            final int lastIndex = newTokenList.size() - 1;
                            String tail = newTokenList.get(lastIndex);
                            final int tailIdx = tailNotBlankLength(tail);
                            if (tailIdx != -1) {
                                tail = tail.substring(0, tailIdx);
                            }
                            final String concat = StringUtils.defaultString(tail) + token.substring(blankIdx + 1);
                            newTokenList.set(lastIndex, concat);
                        }
                    }
                    final StringBuffer totalRemarkStb = new StringBuffer();
                    for (int i = 0; i < newTokenList.size(); i++) {
                        String token = newTokenList.get(i);
                        final List<String> split = KNJ_EditKinsoku.getTokenList(token, knja133eShokenSize.getKeta());
                        if (split.size() > 0) {
                            final String line0 = split.get(0);
                            if (totalRemarkStb.length() != 0) {
                                totalRemarkStb.append("\n");
                            }
                            totalRemarkStb.append(line0);
                            if (token.length() > line0.length()) {
                                final List<String> split2 = KNJ_EditKinsoku.getTokenList(token.substring(line0.length()), knja133eShokenSize.getKeta() - 2);
                                for (int j = 0; j < split2.size(); j++) {
                                    final String token2 = split2.get(j);
                                    totalRemarkStb.append("\n").append("　").append(token2);
                                }
                            }
                        }
                    }
                    totalRemark = totalRemarkStb.toString();
                }
            }
            return totalRemark;
        }

        private static int headBlankLength(final String t) {
            int blankIdx = -1;
            for (int i = 0; i < t.length(); i++) {
                final char ch = t.charAt(i);
                if (ch == ' ' || ch == '　') {
                    blankIdx = i;
                } else {
                    break;
                }
            }
            return blankIdx;
        }

        private static int tailNotBlankLength(final String t) {
            int blankIdx = -1;
            if (null != t) {
                for (int i = t.length() - 1; i >= 0; i--) {
                    final char ch = t.charAt(i);
                    if (ch == ' ' || ch == '　') {
                        blankIdx = i;
                    } else {
                        break;
                    }
                }
            }
            return blankIdx;
        }

        private void printStudent(final DB2UDB db2, final Student student) {
//            _svf.VrsOut("ENTERDIV1", "第　学年　入学");
//            _svf.VrsOut("ENTERDIV2", "第　学年編入学");
//            _svf.VrsOut("ENTERDIV3", "第　学年転入学");
//            _svf.VrsOut("BIRTHDAY", setDateFormat2(param, null) + "生");
//            _svf.VrsOut("ENTERDATE1", setDateFormat(param, null, _param._year));
////            _svf.VrsOut("ENTERDATE2", setDateFormat(null, _param._year));
//            _svf.VrsOut("ENTERDATE3", setDateFormat(param, null, param._year));
//            _svf.VrsOut("TRANSFER_DATE_1", "（" + setDateFormat(_param, null, param._year) + "）");
//            _svf.VrsOut("TRANSFER_DATE_2", "　" + setDateFormat(_param, null, param._year) + "　");
//            _svf.VrsOut("TRANSFER_DATE_4", setDateFormat(_param, null, _param._year));
//            _svf.VrsOut("GRADDATE", setDateFormat(_param, null, _param._year));
//            for (int i = 0; i < 6; i++) {
//                _svf.VrsOut("YEAR_" + (i + 1), setNendoFormat(_param, null, _param._year));
//            }

            final PersonalInfo pi = student._personalInfo;
            final int kn1 = KNJ_EditEdit.getMS932ByteLength(pi._studentKana);
            _svf.VrsOut("KANA" + (kn1 <= 30 ? "1" : "2"), pi._studentKana); // ふりがな
            final int ketaName1 = KNJ_EditEdit.getMS932ByteLength(pi._studentName1);
            _svf.VrsOut("NAME1_" + (ketaName1 <= 20 ? "1" : ketaName1 <= 30 ? "2" : "3"), pi._studentName1); // 生徒氏名

            if (!student._addressList.isEmpty()) {
                final Address address = student._addressList.get(student._addressList.size() - 1);

                final String addr = StringUtils.defaultString(address._address1) + (address._isPrintAddr2 ? StringUtils.defaultString(address._address2) : "");
                final int n1 = KNJ_EditEdit.getMS932ByteLength(addr);

                if (null != address._zipCd) {
                    _svf.VrsOut("ZIPCODE2", "〒" + address._zipCd); // 郵便番号
                }
                _svf.VrsOut("ADDRESS1_" + (n1 <= 68 ? "1" : "2"), addr); // 住所
            }

            _svf.VrsOut("SEX", pi._sex); // 性別
            _svf.VrsOut("BIRTHDAY", pi._birthdayStr); // 生年月日

            if (!StringUtils.isEmpty(pi._staffname1) && !StringUtils.isEmpty(pi._staffname2)) {
                _svf.VrsOut("STAFFNAME2_1", pi._staffname1); // 生年月日
                _svf.VrsOut("STAFFNAME2_2", pi._staffname2); // 生年月日
            } else {
                _svf.VrsOut("STAFFNAME1", pi._staffname1); // 生年月日
            }

            printTransfer(db2, student);
        }

        private void VrsOutnRenban(final Vrw32alp svf, final String field, final String data, final KNJPropertiesShokenSize size, Param param) {
            final List<String> list = getTokenList(data, size.getKeta(), size._gyo, param);
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, list.get(i));
            }
        }

        private void VrsOutRenban(final Vrw32alp svf, final String field, final String data, final KNJPropertiesShokenSize size, Param param) {
            final List<String> list = getTokenList(data, size.getKeta(), size._gyo, param);
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field + String.valueOf(i + 1), list.get(i));
            }
        }

        private Map getFieldStatusMap(Param param, final String fieldname) {
            final Map m = new HashMap();
            try {
                SvfField f = (SvfField) getMappedMap(_formFieldInfoMap, _form).get(fieldname);
                m.put("X", String.valueOf(f.x()));
                m.put("Y", String.valueOf(f.y()));
                m.put("Size", String.valueOf(f.size()));
                m.put("Keta", String.valueOf(f._fieldLength));
            } catch (Throwable t) {
                final String key = _form + "." + fieldname;
                if (_formFieldInfoError.containsKey(key)) {
                    log.warn(" svf field not found:" + key);
                    _formFieldInfoError.put(key, "ERROR");
                }

            }
            return m;
        }

        private static List<String> getTokenList(final String targetsrc, final int keta, final int gyo, final Param param) {
            final List<String> lines = getTokenList(targetsrc, keta, param);
            if (lines.size() > gyo) {
                return lines.subList(0, gyo);
            }
            return lines;
        }

        private static List<String> getTokenList(final String targetsrc0, final int dividlen, final Param param) {
            if (targetsrc0 == null) {
                return Collections.emptyList();
            }
            return KNJ_EditKinsoku.getTokenList(targetsrc0, dividlen);
        }

        private void setForm(final String formname, final int n) {
            log.info(" set form = " + formname);
            _svf.VrSetForm(formname, n);
            _form = formname;
            if (!_formFieldInfoMap.containsKey(_form)) {
                _formFieldInfoMap.put(_form, null);
                try {
                    _formFieldInfoMap.put(_form, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
                } catch (Throwable t) {
                    log.warn(" no class SvfField.");
                }
            }
        }

        private String createMusashinohigashiCDEform(final String form) {
            final String fileKey = "musashinohigashiCDEform";
            if (!_param._createFormFileMap.containsKey(fileKey)) {
                final File formFile = new File(_svf.getPath(form));
                final SvfForm svfForm = new SvfForm(formFile);
                File file = null;
                if (svfForm.readFile()) {
                    final SvfField fieldMoral = getSvfField("MORAL1");
                    final Map attributeMap = fieldMoral.getAttributeMap();
                    final String x = (String) attributeMap.get("X");
                    final String y = (String) attributeMap.get("Y");
                    if (NumberUtils.isDigits(x) && NumberUtils.isDigits(y)) {
                        final SvfForm.Point topLeftPoint = new SvfForm.Point(Integer.parseInt(x), Integer.parseInt(y));
                        final Line upperLine = svfForm.getNearestUpperLine(topLeftPoint);
                        final Line lowerLine = svfForm.getNearestLowerLine(topLeftPoint);
                        final Line leftLine = svfForm.getNearestLeftLine(topLeftPoint);
                        svfForm.addLine(new SvfForm.Line(upperLine._end, new SvfForm.Point(leftLine._start._x, lowerLine._start._y)));
                    }

                    try {
                        file = svfForm.writeTempFile();
                    } catch (IOException e) {
                        if (_param._isOutputDebug) {
                            log.error("exception!", e);
                        }
                    }
                }
                _param._createFormFileMap.put(fileKey, file);
            }
            if (null != _param._createFormFileMap.get(fileKey)) {
                final File file = _param._createFormFileMap.get(fileKey);
                return file.getName();
            }
            return form;
        }

//        /**
//         * 名前を印字する
//         * @param svf
//         * @param name 名前
//         * @param fieldData フィールドのデータ
//         * @param isCentring 中央寄せするか
//         */
//        protected void printName(
//                final Vrw32alp svf,
//                final String name,
//                final KNJSvfFieldInfo fieldData,
//                final boolean isCentering) {
//            final double charSize = KNJSvfFieldModify.getCharSize(name, fieldData);
//            svf.VrAttribute(fieldData._field, "Size=" + charSize);
//            svf.VrAttribute(fieldData._field, "Y=" + (int) KNJSvfFieldModify.getYjiku(0, charSize, fieldData));
//            if (isCentering) {
//                if (-1.0f != charSize) {
//                    final int offset = KNJSvfFieldModify.getModifiedCenteringOffset(fieldData._x1, fieldData._x2, KNJ_EditEdit.getMS932ByteLength(name), charSize);
//                    svf.VrAttribute(fieldData._field, "X=" + (fieldData._x1 - offset));
//                }
//            }
//            svf.VrsOut(fieldData._field, name);
//        }

        private SvfField getSvfField(final String fieldname) {
            SvfField f = (SvfField) getMappedMap(_formFieldInfoMap, _form).get(fieldname);
            return f;
        }

        private boolean hasField(final Param param, final String fieldname) {
            return !getFieldStatusMap(param, fieldname).isEmpty();
        }

//        /**
//         * 個人情報
//         */
//        private void printPersonalInfo(final Vrw32alp svf, final Param param, final Student student) {
//            final PersonalInfo info = student._personalInfo;
//            if (null != info) {
//                if (info._studentKana != null) {
//                    final int height = 40;
//                    final int minnum = 20;
//                    if (info._studentKana != null) {
//                        final KNJSvfFieldInfo fieldData = getFieldInfo(param, "KANA", null, null, height, minnum);
//                        printName(svf, info._studentKana, fieldData, false);
//                    }
//                }
//                final int height = 80;
//                final int minnum = 20;
//                final int maxnum = 48;
//
//                final String name1 = info._studentName1;
//                final String name2 = info._studentNameHistFirst;
//
//                if (StringUtils.isBlank(name2) || name2.equals(name1)) {
//                    // 履歴なしもしくは履歴の名前が現データの名称と同一
//                    final KNJSvfFieldInfo fieldData1 = getFieldInfo(param, "NAME1", null, null, height, minnum);
//                    printName(svf, name1, fieldData1, false);
//                } else {
//                    final KNJSvfFieldInfo fieldData11 = getFieldInfo(param, "NAME1_1", null, null, height, minnum);
//                    final KNJSvfFieldInfo fieldData21 = getFieldInfo(param, "NAME2_1", null, null, height, minnum);
//                    printName(svf, name2, fieldData11, false);
//                    printCancelLine(svf, fieldData11._field, Math.min(KNJ_EditEdit.getMS932ByteLength(name2), maxnum)); // 打ち消し線
//                    printName(svf, name1, fieldData21, false);
//                }
//                svf.VrsOut("BIRTHDAY", info._birthdayStr);
//                if (info._sex != null) {
//                    svf.VrsOut("SEX", info._sex);
//                }
//            }
//
//            if (null != info) {
//                final int height = 50;
//                final int minnum = 20;
//
//                if (info._useRealName && info._nameOutputFlg && !info._studentRealName.equals(info._studentName)) {
//                    printName(svf, info._studentRealName, getFieldInfo(param, "NAME2", null, null, height, minnum), false);
//                    printName(svf, "（" + info._studentName + "）", getFieldInfo(param, "NAME3", null, null, height, minnum), false);
//                } else {
//                    final String name = info._useRealName ? info._studentRealName : info._studentName;
//                    printName(svf, name, getFieldInfo(param, "NAME1", null, null, height, minnum), param._isMusashinohigashi);
//                }
//            }
//        }

        /**
         * 個人情報 異動履歴情報
         */
        private void printTransfer(final DB2UDB db2, final Student student) {
            try {
//                printCancelLine(_svf, "LINE1", 14);
//                printCancelLine(_svf, "LINE2", 14);

//                final int A002_NAMECD2_TENNYUGAKU = 4;
//                final int A002_NAMECD2_5 = 5;
                final int A003_NAMECD2_SOTSUGYO = 1;
                final int A003_NAMECD2_TAIGAKU = 2;
                final int A003_NAMECD2_TENGAKU = 3;

                final PersonalInfo personalInfo = student._personalInfo;

//                // 入学区分
//                if (null != personalInfo._entDiv) {
//                    final String gradeStr;
//                    if (personalInfo._entYear != null) {
//                        final int grade = student.currentGradeCd(param) - (Integer.parseInt(param._year) - Integer.parseInt(personalInfo._entYear));
//                        gradeStr = " " + (0 >= grade ? " " : String.valueOf(grade));
//                    } else {
//                        gradeStr = "  ";
//                    }
//                    final int entdiv = personalInfo._entDiv.intValue();
//                    if (entdiv == A002_NAMECD2_TENNYUGAKU) { // 転入学
//                        if (personalInfo._entDate != null) {
//                            _svf.VrsOut("ENTERDATE3", setDateFormat(param, h_format_JP(param, personalInfo._entDate), _param._year));
//                        }
//                        svf.VrsOut("ENTERDIV3", "第" + gradeStr + "学年転入学");
//                        if (personalInfo._entReason != null) {
//                            _svf.VrsOut("TRANSFERREASON1_1", personalInfo._entReason);
//                        }
//                        if (personalInfo._entSchool != null) {
//                            _svf.VrsOut("TRANSFERREASON1_2", personalInfo._entSchool);
//                        }
//                        if (personalInfo._entAddr != null) {
//                            _svf.VrsOut("TRANSFERREASON1_3", personalInfo._entAddr);
//                        }
//                    } else {
//                        if (entdiv != A002_NAMECD2_5) {
//                            if (personalInfo._entDate != null) {
//                                svf.VrsOut("ENTERDATE1", setDateFormat(param, h_format_JP(param, personalInfo._entDate), _param._year));
//                            }
//                            _svf.VrAttribute("LINE1", "X=5000");
//                            _svf.VrsOut("ENTERDIV1", "第" + gradeStr + "学年　入学");
//                        } else {
//                            if (personalInfo._entDate != null) {
//                                svf.VrsOut("ENTERDATE2", setDateFormat(param, h_format_JP(param, personalInfo._entDate), _param._year));
//                            }
//                            _svf.VrAttribute("LINE2", "X=5000");
//                            _svf.VrsOut("ENTERDIV2", "第" + gradeStr + "学年編入学");
//                        }
//                        if (personalInfo._entReason != null) {
//                            _svf.VrsOut("ENTERRESONS1", personalInfo._entReason);
//                        }
//                        if (personalInfo._entSchool != null) {
//                            _svf.VrsOut("ENTERRESONS2", personalInfo._entSchool);
//                        }
//                        if (personalInfo._entAddr != null) {
//                            _svf.VrsOut("ENTERRESONS3", personalInfo._entAddr);
//                        }
//                    }
//                }

                // 除籍区分
                final String setDateFormat = setDateFormat1(db2, personalInfo._grdDate, _param._isSeireki, _param);
                if (null != personalInfo._grdDiv) {
                    final int grddiv = personalInfo._grdDiv.intValue();
                    if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_TENGAKU) {
                        // 2:退学 3:転学
//                        if (personalInfo._grdDate != null) {
//                            _svf.VrsOut("TRANSFER_DATE_1", "（" + setDateFormat(param, h_format_JP(param, personalInfo._grdDate), param._year) + "）");
//                        }
//                        if (personalInfo._grdReason != null) {
//                            _svf.VrsOut("TRANSFERREASON2_1", personalInfo._grdReason);
//                        }
//                        if (personalInfo._grdSchool != null) {
//                            _svf.VrsOut("TRANSFERREASON2_2", personalInfo._grdSchool);
//                        }
//                        if (personalInfo._grdAddr != null) {
//                            _svf.VrsOut("TRANSFERREASON2_3", personalInfo._grdAddr);
//                        }
                    } else if (grddiv == A003_NAMECD2_SOTSUGYO) {
                        // 1:卒業
                        if (personalInfo._grdDate != null) {
                            _svf.VrsOut("TRANSFER_DATE_4", setDateFormat);
                        }
                    }
                }

                if (null == student._entGrdHistDat._tengakuSakiZenjitu && null != personalInfo._grdDiv && personalInfo._grdDiv.intValue() == A003_NAMECD2_TENGAKU && personalInfo._grdDate != null) {
                    final String dateFormatJp = setDateFormat;
                    _svf.VrsOut("TRANSFER_DATE_4", "　" + dateFormatJp + "　");
                } else if (null != student._entGrdHistDat._tengakuSakiZenjitu) {
                    final String dateFormatJp = setDateFormat(db2, _param, h_format_JP(db2, _param, student._entGrdHistDat._tengakuSakiZenjitu), _param._year);
                    _svf.VrsOut("TRANSFER_DATE_4", "　" + dateFormatJp + "　");
                }

            } catch (Exception ex) {
                log.error("printSvfDetail_4 error!", ex);
            }
        }

        /**
         * 学校情報
         */
        private void printSchoolInfo1(final DB2UDB db2, final Student student) {
            _svf.VrsOut("DATE", setDateFormat1(db2, _param._printDate, _param._isSeireki, _param)); // 日付

            String principalname = null;
            for (final Gakuseki gakuseki : student._gakusekiList) {

                if (gakuseki._gradeCd == GRADE6) {
                    final List<String> nameLine = gakuseki._principal1._staffMst.getNameLine(gakuseki._year);
                    if (!nameLine.isEmpty()) {
                        principalname = nameLine.get(0);
                    }
                }
            }

            final SchoolInfo schoolInfo = _param._schoolInfo;
            if (null != schoolInfo) {
                if (null != schoolInfo._schoolZipCd) {
                    _svf.VrsOut("ZIPCODE1", "〒" + schoolInfo._schoolZipCd); // 郵便番号（学校）
                }

                final String address = StringUtils.defaultString(schoolInfo._schoolAddr1) + StringUtils.defaultString(schoolInfo._schoolAddr2);
                _svf.VrsOut("ADDRESS_gakko1_" + (KNJ_EditEdit.getMS932ByteLength(address) <= 50 ? "2" : "3"), address); // 学校所在地

                final String schoolName1 = !StringUtils.isEmpty(schoolInfo._certifSchoolName) ? schoolInfo._certifSchoolName : schoolInfo._schoolName1;
                _svf.VrsOut("SCHOOLNAME", StringUtils.defaultString(schoolName1) + "　" + StringUtils.defaultString(schoolInfo._jobName) + "　" + StringUtils.defaultString(principalname)); // 学校名
            }

            if (null != _param._schoolStampPath) {
                _svf.VrsOut("STAMP", _param._schoolStampPath); // 印影
            }
        }

        /**
         * 取り消し線印刷
         * @param svf
         * @param keta 桁
         */
        private void printCancelLine(final Vrw32alp svf, final String field, final int keta) {
            svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + keta);
        }

        private void printStaffName(final String j, final int i, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString();
                _svf.VrsOut("STAFFNAME_" + j + "_" + i + (KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_1" : ""), name);
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList();
                line.addAll(staff1._staffMst.getNameLine(staff1._year));
                if (line.size() == 2) {
                    _svf.VrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    _svf.VrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    final String name = staff1.getNameString();
                    _svf.VrsOut("STAFFNAME_" + j + "_" + i + (KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_1" : ""), name);
                }
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List<String> line = new ArrayList();
                line.addAll(staff2.getNameBetweenLine());
                line.addAll(staff1.getNameBetweenLine());
                if (line.size() == 2) {
                    _svf.VrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    _svf.VrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        _svf.VrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), line.get(k));
                    }
                }
            }
        }

        /**
         *  評定に表示する教科取得処理
         *
         */
        private List<ClassView> getValuationViewClassExcludeList(final Student student) {
            final List<ClassView> valuationViewClassExcludeList = new ArrayList();
            int line1 = 0;
            for (final ClassView classview : student._classViewList) {
                final String showname = StringUtils.defaultString(getShowname(student, classview));
                int i = 0;  //教科名称カウント用変数を初期化
                final boolean useClass2 =  2 <= (null == showname ? 0 : showname.length()) / classview.getViewNum();
                final int inc = useClass2 ? 2 : 1;

                final List<List<View>> viewCdListList = getViewCdListList(classview);

                boolean isSlashAll = true;
                for (int j = 0; j < viewCdListList.size(); j++) {
                    // final List viewCdList = (List) viewCdListList.get(j);
                    // final View view = (View) viewCdList.get(0);
                    if (i < showname.length()) {
                        i += inc;
                    }
                    line1++;
                    boolean isSlashLine = true;
                    if (!_param.isSlashView(student._personalInfo._recordDiv, classview._subclasscd, String.valueOf(GRADE6), j + 1)) {
                        isSlashLine = false;
                    }
//                    log.debug(" viewCd = " + view._viewcd + " line = " + line1 + " isSlashLine = " + isSlashLine);
                    if (!isSlashLine) {
                        isSlashAll = false;
                    }
                }
//                log.debug(" classcd = " + classview._classcd + " isSlashAll = " + isSlashAll + " (" + classview._classname + ")");
                if (isSlashAll) {
                    final boolean butShow = _param._isMusashinohigashi && StringUtils.split(classview._subclasscd, "-").length >= 4 && "200100".equals(StringUtils.split(classview._subclasscd, "-")[3]);
                    if (!butShow) {
                        valuationViewClassExcludeList.add(classview);
                    }
                }
//                if (Integer.parseInt(_param._year) >= 2020) {
//                	for (final View v : classview._views) {
//                		if (v.)
//                	}
//                }
                if (null != showname) {
                    if (i < showname.length()) {
                        line1 += (showname.length() - i) / inc + ((showname.length() - i) % inc != 0 ? 1 : 0);
                    } else if (line1 % VIEW_LINE1_MAX != 0) {
                        line1++;
                    }
                }
            }
            return valuationViewClassExcludeList;
        }

//        /**
//         * 同一教科で異なる科目が２つ以上ある場合科目コードの順番を返す。そうでなければ(通常の処理）教科コードを返す。
//         * @param student
//         * @param classview
//         * @return 科目名または教科名
//         */
//        private String getKeyCd(final Student student, final ClassView classview) {
//            final TreeSet set = getSubclassCdSet(student, classview);
//            if (set.size() == 1) {
//                return classview._classcd;
//            } else if (set.size() > 1) {
//                final DecimalFormat df = new DecimalFormat("00");
//                int order = 0;
//                int n = 0;
//                for (final Iterator it = set.iterator(); it.hasNext();) {
//                    final String subclassCd = (String) it.next();
//                    if (classview._subclasscd.equals(subclassCd)) {
//                        order = n;
//                        break;
//                    }
//                    n += 1;
//                }
//                return df.format(order);
//            }
//            return null;
//        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getShowname(final Student student, final ClassView classview) {
            final TreeSet set = getSubclassCdSet(student, classview);
            if (set.size() > 1 || classview.e() == 2) {
                return classview._subclassname;
            } else if (set.size() == 1) {
                return classview._classname;
            }
            return null;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet getSubclassCdSet(final Student student, final ClassView classview) {
            final TreeSet set = new TreeSet();
            for (final ClassView classview0 : student._classViewList) {
                if (classview0._classcd.equals(classview._classcd)) {
                    set.add(classview0._subclasscd);
                }
            }
            return set;
        }

        private List<List<View>> getViewCdListList(final ClassView classview) {
            final List<List<View>> viewCdListList = new ArrayList();
            for (final View view : classview._views) {
                List<View> viewCdList = null;
                for (final List<View> viewCdList0 : viewCdListList) {
                    for (final View view0 : viewCdList0) {
                        if (view0._subclasscd.equals(view._subclasscd) && view0._viewcd.equals(view._viewcd)) {
                            viewCdList = viewCdList0;
                            break;
                        }
                    }
                    if (null != viewCdList) {
                        break;
                    }
                }
                if (null == viewCdList) {
                    viewCdList = new ArrayList();
                    viewCdListList.add(viewCdList);
                }
                viewCdList.add(view);
            }
            return viewCdListList;
        }
    }

    private static class Param {

        final static String SCHOOL_KIND = "P";

        final String _year;
        final String _gakki;
        final String _gradeHrclass;
        final String _useSchregRegdHdat;
        final String _output;
        final String[] _categorySelected;
        final String _ctrlDate;
        final String _printDate;

        final String _documentroot;
        final String _useCurriculumcd;
        final String _use_finSchool_teNyuryoku_P;
//        final String _seitoSidoYorokuNotPrintKantenBlankIfPageOver;
        final boolean _useEditKinsoku;
        final boolean _useJviewnameSubMstCurriculumcd;

        final String _imagePath;
        final String _extension;
        final String _schoolStampPath;

        final SchoolInfo _schoolInfo;

        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;

        final Map _gradeCdMap;

        final Map<String, PreparedStatement> _psMap = new HashMap();

        final Map<Integer, Map> _specialActMstMap;
        final List<Map<String, String>> _behaviorNameList;

        final boolean _hasSCHOOL_MST_SCHOOL_KIND;

        private final Map<String, String> _filenameCheck = new HashMap<String, String>();
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P;
        final String _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P;
        boolean _isMusashinohigashi;
        boolean _isSundaikoufu;
        boolean _isHibarigaoka;
        boolean _isRitsumeikan;
        final Map<String, Map<String, List<String>>> _slashViewFieldIndexMapList;
        final Map<String, List<String>> _slashValueFieldIndexMapList;
        final Map<String, Map<String, List<String>>> _slashViewFieldIndexRecordDiv1MapList;
        final Map<String, List<String>> _slashValueFieldIndexRecordDiv1MapList;
        private Map hmap = null;
        boolean _isOutputDebug;
        boolean _isOutputDebugQuery;

        private final Map<String, File> _createFormFileMap = new HashMap();

        public Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); // 学年・組
            _categorySelected = request.getParameterValues("category_selected");
            _output = request.getParameter("OUTPUT");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printDate = request.getParameter("PRINT_DATE");

            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_finSchool_teNyuryoku_P = request.getParameter("use_finSchool_teNyuryoku_P");

//          final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//          _imagePath = null == returnval ? null : returnval.val4; // 写真データ格納フォルダ
//          _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
//            _imagePath = "image/stamp";
//            _extension = "bmp";

            final String nameMstZ010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + "NAME1" + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            log.debug(" z010 name1 = " + nameMstZ010Name1);
            _isMusashinohigashi = "musashinohigashi".equals(nameMstZ010Name1);
            _isSundaikoufu = "sundaikoufu".equals(nameMstZ010Name1);
            _isHibarigaoka = "hibarigaoka".equals(nameMstZ010Name1);
            _isRitsumeikan = "Ritsumeikan".equals(nameMstZ010Name1);

            _schoolInfo = SchoolInfo.load(db2, this);

            _isSeireki = KNJ_EditDate.isSeireki(db2);

            _gradeCdMap = getGradeCdMap(db2);

            _useEditKinsoku = true;
            _useJviewnameSubMstCurriculumcd = _isSundaikoufu;
            log.info(" _useJviewnameSubMstCurriculumcd = " + _useJviewnameSubMstCurriculumcd);
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");

            _behaviorNameList = KnjDbUtils.query(db2, getBehaviorNameSql(_year));
            _specialActMstMap = new TreeMap();
            for (final Map row : KnjDbUtils.query(db2, getSpecialActNameSql(_year))) {
                _specialActMstMap.put(Integer.valueOf(KnjDbUtils.getString(row, "CD")), row);
            }

            final Map controlMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _imagePath = KnjDbUtils.getString(controlMst, "IMAGEPATH");
            _extension = KnjDbUtils.getString(controlMst, "EXTENSION");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _slashViewFieldIndexMapList = getSlashViewIndexList(db2, null);
            _slashValueFieldIndexMapList = getSlashValueIndexMapList(db2, null);
            _slashViewFieldIndexRecordDiv1MapList = getSlashViewIndexList(db2, "2");
            _slashValueFieldIndexRecordDiv1MapList = getSlashValueIndexMapList(db2, "2");
//            _seitoSidoYorokuNotPrintKantenBlankIfPageOver = request.getParameter("seitoSidoYorokuNotPrintKantenBlankIfPageOver");

            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P = request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_P");
            _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P = request.getParameter("HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P");
        }

        public void closeForm() {
            for (final File file : _createFormFileMap.values()) {
                if (null != file && file.exists()) {
                    log.info(" delete file : " + file.getAbsolutePath() + ", " + file.delete());
                }
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA133E' AND NAME = '" + propName + "' "));
        }

        public void setPs(final String psKey, final DB2UDB db2, final String sql) {
            try {
                PreparedStatement ps = db2.prepareStatement(sql);
                _psMap.put(psKey, ps);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        public void closeStatementQuietly() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getBehaviorNameSql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.NAMECD2 AS CD ");
            stb.append("     ,NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.NAMECD1 = 'D035' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.NAMECD2 ");
            return stb.toString();
        }

        private static String getSpecialActNameSql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.NAMECD2 AS CD ");
            stb.append("     ,NAME1 ");
            stb.append("     ,ABBV1 AS FLG1 ");
            stb.append("     ,ABBV2 AS FLG2 ");
            stb.append("     ,ABBV3 AS FLG3 ");
            stb.append("     ,NAMESPARE1 AS FLG4 ");
            stb.append("     ,NAMESPARE2 AS FLG5 ");
            stb.append("     ,NAMESPARE3 AS FLG6 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.NAMECD1 = 'D034' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.NAMECD2 ");
            return stb.toString();
        }

        protected List<String> getSchregnoList(final DB2UDB db2) {
            final List<String> schregnoList = new ArrayList();
            if ("2".equals(_output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.SCHREGNO ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _gakki + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");

                schregnoList.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO"));

            } else {
                schregnoList.addAll(Arrays.asList(_categorySelected));
            }
            return schregnoList;
        }

        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = (String) _gradeCdMap.get(year + grade);
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }

        private boolean isSlash(final List<int[]> fieldIndex, final int tLine, final int tGrade) {
            for (final int[] indexes : fieldIndex) {
                final int line = indexes[0];
                final int grade = indexes[1];
                if (line == tLine && grade == tGrade) {
                    return true;
                }
            }
            return false;
        }

        private Map<String, String> getGradeCdMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     YEAR || GRADE AS KEY, T1.* ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "KEY", "GRADE_CD");
        }

        private boolean isSlashView(final String recordDiv, final String subclasscd, final String grade, final int line) {
            final Map<String, Map<String, List<String>>> m;
            if ("2".equals(recordDiv)) {
                m = _slashViewFieldIndexRecordDiv1MapList;
            } else {
                m = _slashViewFieldIndexMapList;
            }
            List<String> gradeList = getMappedList(getMappedMap(m, subclasscd), String.valueOf(line));
            if (_isMusashinohigashi) {
                final boolean isKaitei2020 = 2020 <= Integer.parseInt(_year);
                if ("2".equals(recordDiv) && isKaitei2020) {
                    if (subclasscd.endsWith("120100")) { // 社会
                        if (3 == line) { // 主体的に学習に取り組む態度
                            gradeList = new ArrayList<String>(gradeList);
                            gradeList.removeAll(Arrays.asList("3", "4", "5", "6"));
                        }
                    }
                }
            }
            final boolean isSlash = gradeList.contains(grade);
//            log.debug(" isSlashView = " + isSlash + " : " + subclasscd + " : " + grade + " : " + line + " / " + m + " / recordDiv = " + recordDiv);
            return isSlash;
        }

        private boolean isSlashValue(final String recordDiv, final String subclasscd, final String grade) {
            final Map m;
            if ("2".equals(recordDiv)) {
                m = _slashValueFieldIndexRecordDiv1MapList;
            } else {
                m = _slashValueFieldIndexMapList;
            }
            final boolean isSlash = getMappedList(m, subclasscd).contains(grade);
            //log.debug(" isSlashValue = " + isSlash + " : " + subclasscd + " : " + grade + " / " + m);
            return isSlash;
        }

        /**
         * スラッシュを表示する観点フィールド(科目、学年、行)のリスト
         */
        private Map<String, Map<String, List<String>>> getSlashViewIndexList(final DB2UDB db2, final String recordDiv) {
            Map<String, Map<String, List<String>>> map = new HashMap();
            final String sql;
            if ("2".equals(recordDiv)) {
                sql = "SELECT NAME1 AS SUBCLASSCD, ABBV1 AS GAKUNEN, ABBV2 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A038' AND ABBV1 IS NOT NULL AND ABBV2 IS NOT NULL ";
            } else {
                sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN, NAME3 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A038' AND NAME2 IS NOT NULL AND NAME3 IS NOT NULL ";
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN") && NumberUtils.isDigits(KnjDbUtils.getString(row, "LINE"))) {
                    getMappedList(getMappedMap(map, KnjDbUtils.getString(row, "SUBCLASSCD")), KnjDbUtils.getString(row, "LINE")).add(KnjDbUtils.getString(row, "GAKUNEN"));
                }
            }
            return map;
        }

        /**
         * スラッシュを表示する評定フィールド(科目、学年)のリスト
         */
        private Map<String, List<String>> getSlashValueIndexMapList(final DB2UDB db2, final String recordDiv) {
            Map<String, List<String>> map = new HashMap();
            final String sql;
            if ("2".equals(recordDiv)) {
                sql = "SELECT NAME1 AS SUBCLASSCD, ABBV1 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A039' AND ABBV1 IS NOT NULL ";
            } else {
                sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A039' AND NAME2 IS NOT NULL ";
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN")) {
                    getMappedList(map, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getString(row, "GAKUNEN"));
                }
            }
            return map;
        }


        public String getImageFilePath(final String filename) {
            final String path = _documentroot + "/" + (null == _imagePath ? "" : _imagePath + "/") + filename;
            if (!_filenameCheck.containsKey(filename)) {
                final File file = new File(path);
                if (!file.exists() || _isOutputDebug) {
                    log.info(" file " + file.getPath() + " exists? " + file.exists());
                }
                if (!file.exists()) {
                    _filenameCheck.put(filename, null);
                } else {
                    _filenameCheck.put(filename, file.getPath());
                }
            }
            return _filenameCheck.get(filename);
        }
    }
}

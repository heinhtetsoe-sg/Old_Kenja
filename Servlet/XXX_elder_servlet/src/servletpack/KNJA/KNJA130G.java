// kanji=漢字
/*
 * $Id: 3e986d3bcfa134cd86f80ddf25f5804ee42a6360 $
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
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
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
import servletpack.KNJA.KNJA130CCommon.EntGrdHist;
import servletpack.KNJA.KNJA130CCommon.SchregRegdDat;
import servletpack.KNJA.KNJA130CCommon.Semester;
import servletpack.KNJA.KNJA130CCommon.Staff;
import servletpack.KNJA.KNJA130CCommon.StaffInfo;
import servletpack.KNJA.KNJA130CCommon.StaffMst;
import servletpack.KNJA.KNJA130CCommon.Util;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA130G {
    private static final Log log = LogFactory.getLog(KNJA130G.class);

    private static String MARK_FROM_TO = "\uFF5E";

    private static final String CERTIF_KINDCD = "107";

    private static final String _90 = "90";
    private static final String _94 = "94";
    private static final String _ABROAD = "abroad";
    private static final String ANOTHER_YEAR = "0";

    public static boolean DEBUG = false;

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        sd.setSvfInit(request, response, svf);

        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        final Param param = getParam(request, db2);

        // 印刷処理
        boolean nonedata = printSvf(db2, svf, param);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 73741 $ $Date: 2020-04-14 20:07:42 +0900 (火, 14 4 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        boolean nonedata = false;

        final List<KNJA130_0> formList = param.getPrintForm(db2, svf);

        final List<String> schregnos = param.createSchregnos(db2, param); // 出力対象学籍番号を格納
        final List<Student> students = Student.createStudents(schregnos, db2, param);
        Student.setSchregEntGrdHistComebackDat(db2, param, students);
        for (final Student student : students) {
            log.info(" schregno = " + student._schregno);

            final List<PersonalInfo> entGrdHistList = student.getPrintSchregEntGrdHistList(param);
            for (final KNJA130_0 form : formList) {
                for (final PersonalInfo personalInfo : entGrdHistList) {
                    form.setDetail(student, personalInfo);
                    if (form.hasdata) {
                        nonedata = true;
                    }
                }
            }
        }

        return nonedata;
    }

    protected static String defstr(final Object ... os) {
        if (null == os) {
            return "";
        }
        if (os.length == 1) {
            return StringUtils.defaultString(Util.str(os[0]));
        }
        for (final Object o : os) {
            if (null != o && null != o.toString()) {
                return o.toString();
            }
        }
        return "";
    }

    private static String crlfReplaced(final String src, final Param param) {
        String ret;
        if (src == null) {
            ret = "";
        } else {
            if (param._seitoSidoYorokuKinsokuForm) {
                ret = StringUtils.replace(StringUtils.replace(StringUtils.replace(src, "\r\n", "<br/>"), "\r", "<br/>"), "\n", "<br/>");

                final String hanhan = "  "; // 半角スペース2つ
                final String zen = "　"; // 全角スペース1つ
                if (ret.indexOf(hanhan) == 0) { // 頭に半角スペース2つある場合、全角スペース1つに置き換え（SVFの禁則処理にtrimされないようにするため）
                    ret = StringUtils.replaceOnce(ret, hanhan, zen);
                }
                ret = StringUtils.replace(ret, "・", " ・");
            } else {
                ret = src;
            }
        }
        return ret;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static String seirekiFormat(final String date) {
        final String year = year(date);
        final String month = month(date);
        final String dayOfMonth = dayOfMonth(date);
        if (null == year || null == month || null == dayOfMonth) {
            return "";
        }
        return year + "年" + month + "月" + dayOfMonth + "日";
    }

    private static String seirekiFormatM(final String date) {
        final String year = year(date);
        final String month = month(date);
        if (null == year || null == month) {
            return "";
        }
        return year + "年" + month + "月";
    }

    private static String formatDate(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return seirekiFormat(sdate);
        }
        return KNJ_EditDate.h_format_JP(db2, sdate);
    }


    private static String formatDate1(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return "　 " + seirekiFormat(sdate);
        }
        return KNJ_EditDate.h_format_JP(db2, sdate);
    }

    private static String formatDateM(final DB2UDB db2, final String sdate, final Param param) {
        if (param._isSeireki) {
            return "　" + seirekiFormatM(sdate);
        }
        return KNJ_EditDate.h_format_JP_M(db2, sdate);
    }

    private static Calendar getCalendarOfDate(final String date) {
        final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        return cal;
    }

    private static int getNendo(final Calendar cal) {
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return year - 1;
        }
        return year;
    }

    private static String month(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.MONTH) + 1);
        } catch (Exception e) {
        }
        return null;
    }

    private static String dayOfMonth(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
        }
        return null;
    }

    private static String year(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.YEAR));
        } catch (Exception e) {
        }
        return null;
    }

    private static String nendo(final String date) {
        if (null == date) {
            return null;
        }
        try {
            final String year = year(date);
            final String month = month(date);
            String nendo = year;
            if (NumberUtils.isDigits(year) && NumberUtils.isDigits(month) && Integer.parseInt(month) <= 3) {
                nendo = String.valueOf(Integer.parseInt(year) - 1);
            }
            return nendo;
        } catch (Exception e) {
            log.error("exception! date = " + date, e);
        }
        return null;
    }

    private static boolean dateIsInNendo(final String date, final String year) {
        if (null != date && date.compareTo(year + "-04-01") >= 0 && date.compareTo((Integer.parseInt(year) + 1) + "-03-31") <= 0) {
            return true;
        }
        return false;
    }

    private static String addNumber(final String i1, final String i2) {
        if (!NumberUtils.isDigits(i1)) return i2;
        if (!NumberUtils.isDigits(i2)) return i1;
        return String.valueOf((null == i1 ? 0 : Integer.parseInt(i1)) + (null == i2 ? 0 : Integer.parseInt(i2)));
    }

    private static String[] setNendoFormat(final DB2UDB db2, final String flg, final String nendo, final Param param) {
        final String[] arr = new String[] {"", "", "年度"};
        try {
            if (param._isSeireki) {
                if (null != flg) {
                    arr[1] = nendo;
                }
            } else if (NumberUtils.isDigits(nendo)) {
                final String gengou0 = KNJ_EditDate.gengou(db2, Integer.parseInt(nendo));
                final String gengou = 2 < gengou0.length() ? gengou0.substring(0, 2) : gengou0;
                final String nen = 2 < gengou0.length() ? gengou0.substring(2) : gengou0;
                arr[0] = gengou;
                if (null != flg) {
                    arr[1] = nen;
                }
            }
        } catch (NumberFormatException e) {
             log.error("NumberFormatException", e);
        }
        return arr;
    }

    /**
     *  日付の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    private static String setDateFormat(
            final DB2UDB db2,
            final String hdate,
            final String nendo,
            final Param param
    ) {
        final StringBuffer stb = new StringBuffer();
        try {
            if (hdate == null && !StringUtils.isNumeric(nendo)) {
                throw new NumberFormatException("nendo = " + nendo);
            } else if (hdate != null) {
                //「平成18年 1月 1日」の様式とする => 数値は２桁
                stb.append(hdate);
                setFormatInsertBlankFormatJp(stb, "　", " ");
            } else {
                if (param._isSeireki) {
                    stb.append("　　    年    月    日");
                } else {
                    //日付が無い場合は「平成　年  月  日」の様式とする
                    final String hformat = KNJ_EditDate.gengou(db2, Integer.parseInt(nendo), 4, 1);
                    final String gengou = 2 < hformat.length() ? hformat.substring(0, 2) : hformat;
                    stb.append(gengou);
                    stb.append("    年    月    日");
                }
            }
        } catch (NumberFormatException e) {
             log.error("NumberFormatException", e);
        }
        return stb.toString();
    }

    /**
     *  文字編集（ブランク挿入）
     */
    private static void setFormatInsertBlankFormatJp(final StringBuffer stb, final String blank1, final String blank2) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else {
                if (0 < n) {
                    if (1 == n) {
                        stb.insert(i - n, blank1);
                        i++;
                    } else if (2 == n) {
                        stb.insert(i - n, blank2);
                        i++;
                    }
                    stb.insert(i, blank2);
                    i++;
                    n = 0;
                } else if (ch == '元') {
                    stb.insert(i, blank1);
                    i++;
                }
            }
        }
    }

    private static List<String> retDividString(String targetsrc, final int dividlen, final int dividnum) {
        List<String> lines = retDividString(targetsrc, dividlen);
        if (lines.size() > dividnum) {
            lines = lines.subList(0, dividnum);
        }
        return lines;
    }

    private static List<String> retDividString(String targetsrc, final int dividlen) {
        if (targetsrc == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> lines = new ArrayList();         //編集後文字列を格納する配列
        int len = 0;
        StringBuffer stb = new StringBuffer();

        try {
            if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
                targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
            }

            final List charMs932List = CharMS932.toCharMs932List(targetsrc);

            for (final Iterator it = charMs932List.iterator(); it.hasNext();) {
                final CharMS932 c = (CharMS932) it.next();

                if (("\n".equals(c._char) || "\r".equals(c._char))) {
                    if (len <= dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                } else {
                    if (len + c._len > dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                    stb.append(c._char);
                    len += c._len;
                }
            }
            if (0 < len) {
                lines.add(stb.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error! ", ex);
        }
        return lines;
    }

    private static String nullzero(final Object o) {
        return null == o ? "0" : o.toString();
    }

    private static Integer sum(final List<Integer> elemList) {
        if (elemList.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (final Integer e : elemList) {
            sum += e.intValue();
        }
        return new Integer(sum);
    }

    private static Integer max(final List<Integer> elemList) {
        if (elemList.isEmpty()) {
            return null;
        }
        Integer max = elemList.get(0);
        for (final Integer e : elemList) {
            if (e.compareTo(max) > 0) {
                max = e;
            }
        }
        return max;
    }

    private static String getSubclasscd(final ResultSet rs , final Param param) throws SQLException {
        if ("1".equals(param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
        }
        return rs.getString("SUBCLASSCD");
    }

    private static StringBuffer mkString(final List list, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Object item : list) {
            if (null == item || StringUtils.isEmpty(item.toString())) {
                continue;
            }
            stb.append(comma).append(item.toString());
            comma = comma1;
        }
        return stb;
    }

    // --- 内部クラス -------------------------------------------------------
    private static class Student {
        final String _schregno;

        final String _title;

        final PersonalInfo _personalInfo;

        List<SchregRegdDat> _regdList = Collections.emptyList();

        final SchoolInfo _schoolInfo;

        /** 異動履歴 */
        final List _transferRecList;
        Map<String, Map<String, String>> _yearCertifSchoolMap = Collections.emptyMap();

        final Map _attendRecMap;

        final Map<String, HtrainRemark> _htrainRemarkMap;

        final Map _htrainRemarkDetailMap;

        /** 教科コード90の代替科目備考を表示するときtrue */
        private boolean _isShowStudyRecBikoSubstitution90;

        /** 総合学習活動(HTRAINREMARK_HDAT.TOTALSTUDYACT) */
        private String _htrainRemarkHdatAct;

        /** 総合学習評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL) */
        private String _htrainRemarkHdatVal;

        private List _schregEntGrdHistComebackDatList;

        /** 学科年度データの学校区分 */
        private String _majorYdatSchooldiv;

        List<Attend> _attendList;
        List<HTrainRemarkDat> _htrainRemarkDatList;
        HtrainremarkHdat _htrainremarkHdat;
        List<ActRecord> _actRecordList;
        List<ClassView> _classViewList;
        List<ValueRecord> _valueRecordList;
        List _printClassList;
        final Map _yearLimitCache = new HashMap();

        private Student(
                final String schregno,
                final DB2UDB db2,
                final Param param
        ) {
            _schregno = schregno;
            if ("1".equals(param._useGakkaSchoolDiv)) {
                _majorYdatSchooldiv = getMajorYdatSchooldiv(db2, schregno, param._year);
            }

            if (null != param._knja130_1) {
                _yearCertifSchoolMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, " SELECT YEAR, T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME FROM CERTIF_SCHOOL_DAT T6 WHERE T6.CERTIF_KINDCD = '" + CERTIF_KINDCD + "'"), "YEAR");
            }
            _schoolInfo = new SchoolInfo(db2, param, this);

            loadStudent(db2, _schregno, param);

            _title = isKoumokuGakunen(param) ? "学年" : "年度";

            _personalInfo = PersonalInfo.loadPersonal(db2, this, null, param);
            _transferRecList = TransferRec.loadTransferRec(db2, _schregno, param);

            _attendRecMap = AttendRec.loadAttendRec(db2, this, param);
            _htrainRemarkMap = HtrainRemark.loadHtrainRemark(db2, _schregno, param);
            _htrainRemarkDetailMap = Collections.EMPTY_MAP;
            if (!param._isNendogoto) {
                loadHtrainRemarkHdat(db2, _schregno);
            }

            _attendList = Attend.load(db2, param, _schregno);
            _htrainRemarkDatList = HTrainRemarkDat.load(db2, param, _schregno);
            _htrainremarkHdat = HtrainremarkHdat.load(db2, param, _schregno);
            _actRecordList = ActRecord.load(db2, param, _schregno);
            _classViewList = ClassView.load(db2, param, _schregno);
            log.info(" class view size = " + _classViewList.size());
            _valueRecordList = ValueRecord.load(db2, param, _schregno);
            log.info(" value record size = " + _valueRecordList.size());
            _printClassList = Student.getPrintClassList(_classViewList, _valueRecordList);
            log.info(" printClassList = " + _printClassList);
        }

        public static List<Student> createStudents(final List<String> schregnos, final DB2UDB db2, final Param param) {
            final List<Student> list = new ArrayList();
            for (final String schregno : schregnos) {
                final Student student = new Student(schregno, db2, param);
                list.add(student);
            }
            return list;
        }

        /**
         * 印刷する生徒情報
         */
        private List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List<PersonalInfo> rtn = new ArrayList<PersonalInfo>();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                _personalInfo._isFirst = true;
                return Collections.singletonList(_personalInfo);
            }
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List<PersonalInfo> personalInfoList = new ArrayList<PersonalInfo>();
            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
            personalInfoList.add(_personalInfo);
            for (final PersonalInfo personalInfo : personalInfoList) {
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
                    rtn.add(personalInfo);
                }
            }
            if (rtn.isEmpty()) {
                log.fatal("対象データがない!");
            } else {
                rtn.get(0)._isFirst = true;
            }
            return rtn;
        }

        public Map getStudyRecYear(final Param param, final boolean isNewForm, final PersonalInfo personalInfo) {
            final Collection<String> chkDropYears = new HashSet(personalInfo._dropYears);
            final Collection<String> chkDropShowYears = new HashSet();
            return PersonalInfo.createStudyRecYear(param, isNewForm, personalInfo._studyRecList, chkDropYears, chkDropShowYears);
        }

        private Collection<String> getDropYears(final List<Gakuseki> gakusekiList, final Param param) {
            final Collection<String> dropYears = new HashSet();
            for (final Gakuseki gaku : gakusekiList) {
                if (isGakunenSei(param) || param._isGenkyuRyuchi) {
                    if (gaku._isDrop) {
                        dropYears.add(gaku._year);
                    }
                }
            }
            return dropYears;
        }

        /**
         * 生徒名をセットします。
         * @param db2
         */
        private void loadStudent(final DB2UDB db2, final String schregno, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(PersonalInfo.sql_info_reg("0000000000", param, schregno));
                rs = ps.executeQuery();
                if (rs.next()) {

                    // 2009年3月31日までに卒業した生徒は、「総合的な学習の時間の記録」に
                    // 教科コード90の代替科目備考を表示しない。
                    final String grdDate = rs.getString("GRD_DATE");
                    boolean isGraduatedBy2008 = grdDate != null && java.sql.Date.valueOf(grdDate).compareTo(java.sql.Date.valueOf("2009-03-31")) <= 0;
                    _isShowStudyRecBikoSubstitution90 = !isGraduatedBy2008;
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            final String psRegdKey = "PS_REGD";
            if (null == param.getPs(psRegdKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ");
                stb.append(" FROM SCHREG_REGD_DAT ");
                stb.append(" WHERE SCHREGNO = ? ");
                stb.append(" ORDER BY YEAR, SEMESTER ");

                if (param._isOutputDebugQuery) {
                    log.info(" regd sql = " + stb.toString());
                }

                param.setPs(psRegdKey, db2, stb.toString());
            }

            _regdList = new ArrayList<SchregRegdDat>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psRegdKey), new String[] { _schregno })) {
                SchregRegdDat regd = SchregRegdDat.create();
                regd._year = KnjDbUtils.getString(row, "YEAR");
                regd._semester = KnjDbUtils.getString(row, "SEMESTER");
                regd._grade = KnjDbUtils.getString(row, "GRADE");
                regd._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                regd._coursecd = KnjDbUtils.getString(row, "COURSECD");
                regd._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                regd._coursecode = KnjDbUtils.getString(row, "COURSECODE");
                _regdList.add(regd);
            }
        }

        private Map groupByGrade(final List<Gakuseki> gakusekiList) {
            final Map gradeMap = new TreeMap();
            for (final ListIterator<Gakuseki> i = gakusekiList.listIterator(gakusekiList.size()); i.hasPrevious();) {
                final Gakuseki gaku = i.previous();
                if (null != gaku._grade) {
                    getMappedList(gradeMap, gaku._grade).add(gaku);
                }
            }
            return gradeMap;
        }

        private void loadHtrainRemarkHdat(final DB2UDB db2, final String schregno) {
            final String sql = "select TOTALSTUDYACT, TOTALSTUDYVAL"
                + " from HTRAINREMARK_HDAT"
                + " where SCHREGNO = '" + schregno + "'"
                ;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _htrainRemarkHdatAct = rs.getString("TOTALSTUDYACT");
                    _htrainRemarkHdatVal = rs.getString("TOTALSTUDYVAL");
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static List loadAfterGraduatedCourse(final DB2UDB db2, final String schregno, final List<Gakuseki> gakusekiList) {
            final List afterGraduatedCourseTextList = new ArrayList();
            String preSql =
                "select count(*) as COUNT from SYSIBM.SYSCOLUMNS "
                + " where TBNAME = 'AFT_GRAD_COURSE_DAT' ";

            final TreeSet<String> yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
            if (yearSet.isEmpty()) {
                return afterGraduatedCourseTextList;
            }
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            final String sql =
                // 進路用・就職用両方の最新の年度を取得
                "with TA as( select "
                + "         SCHREGNO, "
                + "         '0' as SCH_SENKOU_KIND, "
                + "         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, "
                + "         '1' as COMP_SENKOU_KIND, "
                + "         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR "
                + " from "
                + "         AFT_GRAD_COURSE_DAT "
                + " where "
                + "         SCHREGNO = ?  and PLANSTAT = '1'"
                + "         AND YEAR BETWEEN '" + minYear + "' AND '" + maxYear + "' "
                + " group by "
                + "         SCHREGNO "
                // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得
                + "), TA2 as( select "
                + "     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, "
                + "     T1.SCHREGNO, "
                + "     T1.SENKOU_KIND, "
                + "     MAX(T1.SEQ) AS SEQ "
                + " from "
                + "     AFT_GRAD_COURSE_DAT T1 "
                + " inner join TA on "
                + "     T1.SCHREGNO = TA.SCHREGNO "
                + "     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) "
                + "     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) "
                + " where "
                + "     T1.PLANSTAT = '1'"
                + " group by "
                + "     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), "
                + "     T1.SCHREGNO, "
                + "     T1.SENKOU_KIND "
                + ") "
                // 最新の年度と受験先種別コードの感想を取得
                + "select  "
                + "      T1.SENKOU_KIND "
                + "     ,T1.STAT_CD "
                + "     ,T1.THINKEXAM "
                + "     ,T1.JOB_THINK "
                + "     ,L1.NAME1 as E017NAME1 "
                + "     ,L2.NAME1 as E018NAME1 "
                + "     ,L3.SCHOOL_NAME "
                + "     ,T1.FACULTYCD "
                + "     ,L5.FACULTYNAME "
                + "     ,T1.DEPARTMENTCD "
                + "     ,L6.DEPARTMENTNAME "
                + "     ,L7.ADDR1 AS CAMPUSADDR1 "
                + "     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 "
                + "     ,L4.COMPANY_NAME "
                + "     ,L4.ADDR1 AS COMPANYADDR1 "
                + "     ,L4.ADDR2 AS COMPANYADDR2 "
                + "from "
                + "     AFT_GRAD_COURSE_DAT T1 "
                + "inner join TA2 on "
                + "     T1.YEAR = TA2.YEAR "
                + "     and T1.SCHREGNO = TA2.SCHREGNO "
                + "     and T1.SENKOU_KIND = TA2.SENKOU_KIND "
                + "     and T1.SEQ = TA2.SEQ "
                + "left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD "
                + "left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD "
                + "left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD "
                + "left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L5.FACULTYCD = T1.FACULTYCD "
                + "left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L6.FACULTYCD = T1.FACULTYCD "
                + "     and L6.DEPARTMENTCD = T1.DEPARTMENTCD "
                + "left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD "
                + "left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD "
                + "     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD "
                + "left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD "
                + "where "
                + "     T1.PLANSTAT = '1' "
                + "order by "
                + "     T1.YEAR, T1.SCHREGNO "
                ;

            PreparedStatement ps = null, ps0 = null;
            ResultSet rs = null, rs0 = null;
            try {
                // テーブルがあるか確認する。テーブルが無いなら後の処理を行わない。
                ps0 = db2.prepareStatement(preSql);
                rs0 = ps0.executeQuery();
                int fieldCount = 0;
                if (rs0.next()) {
                    fieldCount = rs0.getInt("COUNT");
                }
                if (fieldCount == 0) {
                    return afterGraduatedCourseTextList;
                }

                ps = db2.prepareStatement(sql);
//                log.debug(" schregno = " + schregno + " sql = " + sql);
                ps.setString(1, schregno);
                rs = ps.executeQuery();
                afterGraduatedCourseTextList.clear();
                if (rs.next()) {
                     if ("0".equals(rs.getString("SENKOU_KIND"))) { // 進学
                         if (null == rs.getString("STAT_CD") || null != rs.getString("E017NAME1")) {
                             afterGraduatedCourseTextList.addAll(retDividString(rs.getString("THINKEXAM"), 50, 10));
                         } else {
                             afterGraduatedCourseTextList.add(StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                             final String faculutyname = "000".equals(rs.getString("FACULTYCD")) || null == rs.getString("FACULTYNAME") ?  "" : rs.getString("FACULTYNAME");
                             final String departmentname = "000".equals(rs.getString("DEPARTMENTCD")) || null == rs.getString("DEPARTMENTNAME") ? "" : rs.getString("DEPARTMENTNAME");
                             afterGraduatedCourseTextList.add(faculutyname + departmentname);
                             afterGraduatedCourseTextList.add(StringUtils.defaultString(rs.getString("CAMPUSFACULTYADDR1"), rs.getString("CAMPUSADDR1")));
                         }
                     } else if ("1".equals(rs.getString("SENKOU_KIND"))) { // 就職
                         if (null == rs.getString("STAT_CD") || null != rs.getString("E018NAME1")) {
                             afterGraduatedCourseTextList.addAll(retDividString(rs.getString("JOB_THINK"), 50, 10));
                         } else {
                             afterGraduatedCourseTextList.add(StringUtils.defaultString(rs.getString("COMPANY_NAME")));
                             afterGraduatedCourseTextList.add(StringUtils.defaultString(rs.getString("COMPANYADDR1")));
                             afterGraduatedCourseTextList.add(StringUtils.defaultString(rs.getString("COMPANYADDR2")));
                         }
                     }
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return afterGraduatedCourseTextList;
        }

        public static String loadAfterGraduatedCourseSenkouKindSub(final DB2UDB db2, final String schregno, final Param param) {
            if (true) { // !param._isKyoto) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_SEQ AS ( ");
            stb.append("     SELECT  ");
            stb.append("         T1.YEAR, T1.SEQ ");
            stb.append("     FROM ");
            stb.append("         AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("        SCHREGNO = '" + schregno + "' ");
            stb.append("        AND SENKOU_KIND = '2' ");
            stb.append(" ), T_AFT_GRAD_COURSE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, MAX(T1.SEQ) AS SEQ ");
            stb.append("     FROM ");
            stb.append("         YEAR_SEQ T1 ");
            stb.append("         INNER JOIN (SELECT MAX(YEAR) AS YEAR FROM YEAR_SEQ) T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SENKOU_KIND_SUB ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     INNER JOIN T_AFT_GRAD_COURSE T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEQ = T1.SEQ ");

            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                  rtn = rs.getString("SENKOU_KIND_SUB");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiAttendRec(final DB2UDB db2, final List<Gakuseki> gakusekiList, final Map<String, AttendRec> attendRecMap, final Param param) {
            final Map map = new HashMap();
            for (final Gakuseki gakuseki : gakusekiList) {
                if (!Param.SCHOOL_KIND_H.equals(param.getGdatSchoolKind(gakuseki._year, gakuseki._grade))) {
                    continue;
                }
                map.put(gakuseki._year, gakuseki);
            }

            final Set<String> kset = attendRecMap.keySet();
            for (final String year : kset) {
                if (map.containsKey(year)) {
                    continue;
                }
                map.put(year, new Gakuseki(db2, attendRecMap.get(year), param));
            }

            final List<Gakuseki> list = new LinkedList(map.values());
            Collections.sort(list, new GakusekiComparator());
            return list;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List createGakusekiStudyRec(final DB2UDB db2, final Student student, List<Gakuseki> gakusekiList, final List<StudyRec> studyRecList, final Param param) {
            final Map map = new HashMap();
            for (final Gakuseki gakuseki : gakusekiList) {
                if (!Param.SCHOOL_KIND_H.equals(param.getGdatSchoolKind(gakuseki._year, gakuseki._grade))) {
                    continue;
                }
                map.put(gakuseki._year, gakuseki);
            }

            for (final StudyRec studyrec : studyRecList) {
                if (map.containsKey(studyrec._year)) {
                    continue;
                }
                final boolean isPrintAnotherStudyrec3 = param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_1 || param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_2 && student.isTanniSei(param);
                if (isPrintAnotherStudyrec3 && NumberUtils.isDigits(studyrec._year)) {
                    // 前籍校の成績を表示する
                    final Gakuseki gakuseki = new Gakuseki(db2, studyrec, param);
                    map.put(studyrec._year, gakuseki);
                }
            }

            final List list = new LinkedList(map.values());
            Collections.sort(list, new GakusekiComparator());
            return list;
        }

        /**
         * 異動データから留学した年度を得る。
         * @param db2
         * @return 留学した年度のリスト
         */
        private static List getAbroadYears(final DB2UDB db2, final String schregno, final List<Gakuseki> gakusekiList) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlAbroadYear(schregno, gakusekiList);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.add(rs.getString("YEAR"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }


            return rtn;
        }

        private static String sqlAbroadYear(final String schregno, final List<Gakuseki> gakusekiList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEARS(YEAR) AS ( ");

            String union = "";
            for (final Gakuseki gakuseki : gakusekiList) {
                stb.append(union).append(" VALUES('" + gakuseki._year + "') ");
                union = " UNION ";
            }
            stb.append(" ) SELECT ");
            stb.append("     T2.YEAR ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1, ");
            stb.append("     YEARS T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.TRANSFERCD = '1' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T2.YEAR BETWEEN FISCALYEAR(T1.TRANSFER_SDATE) AND FISCALYEAR(T1.TRANSFER_EDATE)  ");
            return stb.toString();
        }

        private String getSchooldiv(final Param param) {
            final String schooldiv;
            if ("1".equals(param._useGakkaSchoolDiv)) {
                schooldiv = null != _majorYdatSchooldiv ? _majorYdatSchooldiv : param._definecode.schooldiv;
            } else {
                schooldiv = param._definecode.schooldiv;
            }
            return schooldiv;
        }

        /**
         * 学年制か?
         * @return 学年制なら<code>true</code>
         */
        private boolean isGakunenSei(final Param param) {
            return true; // "0".equals(getSchooldiv(param));
        }

        /**
         * 単位制か?
         * @return 単位制なら<code>true</code>
         */
        private boolean isTanniSei(final Param param) {
            return false; // return "1".equals(getSchooldiv(param));
        }

        /**
         * 単位数欄に"0"を表示するか
         * @param year 京都府の判定用パラメータ
         * @return 表示するならtrue、そうでなければfalse
         */
        public boolean isShowCredit0(final Param param, final PersonalInfo personalInfo, final String year) {
            boolean isGrd = false;
            isGrd = 1 == personalInfo._entGrdH.grdDivInt() || personalInfo._entGrdH.isTaigaku() || personalInfo._entGrdH.isTengaku();
            if (isGrd) {
                return true;
            }
            return false;
        }

        /**
         * 指定生徒・年度の学科年度データの学校区分を得る
         */
        private String getMajorYdatSchooldiv(final DB2UDB db2, final String schregno, final String year) {
            String majorYdatSchooldiv = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD AS ( ");
                stb.append("   SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                stb.append("   FROM SCHREG_REGD_DAT ");
                stb.append("   WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + year + "' ");
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
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    majorYdatSchooldiv = rs.getString("SCHOOLDIV");
                }
            } catch (Exception e) {
                log.error("setMajorYdatSchooldiv Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            // log.fatal(" schoolmst.schooldiv = " + _definecode.schooldiv + ", majorYdatSchoolDiv = " + _majorYdatSchooldiv + " -> "+  getSchooldiv());
            return majorYdatSchooldiv;
        }

        public boolean isKoumokuGakunen(final Param param) {
            Boolean rtn = null;
            if (null == rtn) {
                rtn = Boolean.TRUE;
            }
            return rtn.booleanValue();
        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map<String, List<String>> schregComebackDateMap = new HashMap();
            try {
                for (final Student student : studentList) {
                    student._schregEntGrdHistComebackDatList = Collections.EMPTY_LIST;
                }
                if (!param._hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT) {
                    return;
                }
                final String sql =
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND_H + "' "
                        + " ORDER BY COMEBACK_DATE ";
                // log.debug(" comeback sql = " + sql);
                ps = db2.prepareStatement(sql);
                for (final Student student : studentList) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        getMappedList(schregComebackDateMap, student._schregno).add(rs.getString("COMEBACK_DATE"));
                    }
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            for (final Student student : studentList) {
                if (null == schregComebackDateMap.get(student._schregno)) {
                    continue;
                }
                student._schregEntGrdHistComebackDatList = new ArrayList();
                log.debug(" schregno = " + student._schregno + ",  comebackdate = " + schregComebackDateMap.get(student._schregno));
                for (final String comebackDate : getMappedList(schregComebackDateMap, student._schregno)) {
                    final PersonalInfo comebackPersonalInfo = PersonalInfo.loadPersonal(db2, student, comebackDate, param);
                    student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
                }
            }
        }

        // KNJA133J
        protected boolean isPrintYear(final String regdYear, final Param param) {
          Gakuseki gakuseki = null;
          for (final Gakuseki gakuseki1 : _personalInfo._gakusekiList) {
              if (null != gakuseki1._year && gakuseki1._year.equals(regdYear)) {
                  gakuseki = gakuseki1;
                  break;
              }
          }
          if (null == gakuseki) {
              return false;
          }
          if (!Param.SCHOOL_KIND_J.equals(param.getGdatSchoolKind(gakuseki._year, gakuseki._grade))) {
              return false;
          }
          return true;
        }

        public static TreeSet gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet set = new TreeSet();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final PersonalInfo target, final Param param) {
            return 9999;
        }

        public int currentGradeCd(final Param param) {
            final int paramYear = Integer.parseInt(param._year);
            int diffyear = 100;
            int currentGrade = -1;
            for (final Gakuseki gakuseki : _personalInfo._gakusekiList) {
                if (!StringUtils.isNumeric(gakuseki._year)) {
                    continue;
                }
                final int dy = paramYear - Integer.parseInt(gakuseki._year);
                if (dy >= 0 && diffyear > dy) {
                    currentGrade = Integer.parseInt(gakuseki._grade);
                    diffyear = dy;
                }
            }
            return currentGrade;
        }

        public static List getPrintClassList(final List<ClassView> classViewList, final List<ValueRecord> valueRecordList) {
            final Set printClassSet = new TreeSet();
            for (final ClassView classView : classViewList) {
                for (final ViewSubclass viewSubclass : classView._viewSubclassList) {
                    if (!"1".equals(classView._electdiv)) {
                        printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                    } else {
                        for (final View view : viewSubclass._viewList) {
                            for (final ViewStatus viewStatus : view._viewMap.values()) {
                                if (null != viewStatus._status) {
                                    printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                                }
                            }
                        }
                    }
                }
            }
            for (final ValueRecord valueRecord : valueRecordList) {
                if (!"1".equals(valueRecord._electDiv)) {
                    printClassSet.add(valueRecord._classCd + ":" + valueRecord._subclassCd);
                } else {
                    if (null != valueRecord._value) {
                        printClassSet.add(valueRecord._classCd + ":" + valueRecord._subclassCd);
                    }
                }
            }
            return new ArrayList(printClassSet);
        }

        private List<ValueRecord> getValueRecordList() {
            final List rtn = new ArrayList();
            for (final ValueRecord vr : _valueRecordList) {
                if (_printClassList.contains(vr._classCd + ":" + vr._subclassCd)) {
                    rtn.add(vr);
                }
            }
            return rtn;
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }
    }

    private static class PrintGakuseki {
        final List<String> _yearList = new ArrayList();
        final Map<String, Gakuseki> _gakusekiMap = new TreeMap();
        final Map<String, Integer> _grademap = new TreeMap();
        public Gakuseki _dropGakuseki = null;
        public boolean _lastyearFlg = false;
        public String toString() {
            return "PrintGakuseki(gakusekiMap=" + _gakusekiMap + ", grademap=" + _grademap + ")";
        }

        public StudyRec.AbroadStudyRec getAbroadStudyRec(final List<StudyRec> studyRecList) {
            for (final StudyRec studyrec : studyRecList) {
                if (!_yearList.contains(studyrec._year)) {
                    continue;
                }
                if (studyrec instanceof StudyRec.AbroadStudyRec) {
                    return (StudyRec.AbroadStudyRec) studyrec;
                }
            }
            return null;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒の学籍履歴クラス>>。
     */
    private static class Gakuseki implements Comparable<Gakuseki> {

        final String _grade;
        final String _schoolKind;
        final int _gradeCd;
        final String _gakunenSimple;
        final String _gradeName2;
        final String _nendo;
        final String _hrname;
        final String _attendno;
        final String _year;
        final String _hr_class;
        final String _annual;
        final String _dataflg; // 在籍データから作成したか
        private boolean _isDrop;
        private String _staffSeq;
        private String _principalSeq;
        private String _kaizanFlg;

        /**
         * コンストラクタ。
         * @param rs
         * @param hmap
         * @param param
         */
        private Gakuseki(
                final String year,
                final String nendo,
                final String grade,
                final String schoolKind,
                final String gradeCd,
                final String gradeName2,
                final String hrclass,
                final String hrname,
                final String attendno,
                final String annual,
                final Staff principal,
                final String staffSeq,
                final String principalSeq,
                final String kaizanFlg
        ) {
            _year = year;
            _nendo = nendo;

            _schoolKind = schoolKind;
            _grade = grade;
            _gradeCd = !NumberUtils.isDigits(gradeCd) ? -1 : Integer.parseInt(gradeCd);
            _gradeName2 = gradeName2;

            final String gakunen = !NumberUtils.isDigits(grade) ? " " : String.valueOf(Integer.parseInt(grade));
            _gakunenSimple = gakunen;
            _hr_class = hrclass;
            _hrname = hrname;
            _attendno = attendno;
            _annual = annual;
            _dataflg = "1";
            _staffSeq = staffSeq;
            _principalSeq = principalSeq;
            _kaizanFlg = kaizanFlg;
        }

        /**
         * コンストラクタ。
         */
        private Gakuseki(final DB2UDB db2, final AbstractAttendAndStudy studyrec, final Param param) {
            _year = studyrec._year;
            if (isNyugakumae()) {
                _nendo = "入学前";
            } else {
                if (param._isSeireki) {
                    _nendo = String.valueOf(Integer.parseInt(studyrec._year)) + "年度";
                } else {
                    _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(studyrec._year)) + "年度";
                }
            }
            _grade = studyrec._annual;
            if (null != _grade) {
                if (0 >= Integer.parseInt(_grade)) {
                    _annual = null;
                    _gradeCd = -1;
                    _gakunenSimple = null;
                    _gradeName2 = "入学前";
                    _dataflg = "2";
                } else {
                    _annual = studyrec._annual;
                    final String gradeCd = param.getGdatGradeCd(_year, _grade);
                    _gradeCd = Integer.parseInt(StringUtils.defaultString(gradeCd, _grade));
                    _gakunenSimple = !NumberUtils.isDigits(gradeCd) ? " " : String.valueOf(Integer.parseInt(gradeCd));
                    if (null == param.getGdatGradeName2(_year, _annual)) {
                        _gradeName2 = "第" + Integer.parseInt(_annual) + "学年";
                    } else {
                        _gradeName2 = param.getGdatGradeName2(_year, _annual);
                    }
                    _dataflg = "1";
                }
            } else {
                _annual = null;
                _gradeCd = -1;
                _gakunenSimple = null;
                _gradeName2 = null;
                _dataflg = "2";
            }
            _schoolKind = null;
            _hr_class = null;
            _hrname = null;
            _attendno = null;
        }

        private boolean isNyugakumae() {
            return ANOTHER_YEAR.equals(_year);
        }

        private String[] nendoArray(final Param param) {
            if (isNyugakumae() || _nendo == null || _nendo.length() < 4) {
                return new String[]{"", "", ""};
            }
            final String[] arNendo = new String[3];
            if (param._isSeireki) {
                arNendo[0] = _nendo.substring(0, _nendo.length() - 2);
                arNendo[1] = "";
                arNendo[2] = "年度";
            } else {
                arNendo[0] = _nendo.substring(0, 2);
                arNendo[1] = _nendo.substring(2, _nendo.length() - 2);
                arNendo[2] = "年度";
            }
            return arNendo;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Gakuseki o) {
            final Gakuseki that = o;
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        /**
         * @return 元号を除いた年度を戻します。
         */
        private String getNendo2(final Param param) {
            if (isNyugakumae()) {
                return _nendo;
            } else {
                final String[] arNendo = nendoArray(param);
                if (param._isSeireki) {
                    return arNendo[0] + arNendo[2];
                }
//                if (param._isKyoto) {
//                    return _nendo;
//                }
                return arNendo[1] + arNendo[2];
            }
        }

        public String toString() {
            return "[year = " +_year + " : grade = " + _grade + " : gradeCd = " + _gradeCd + " : hr_class = " + _hr_class + " : attendno = " + _attendno + "]";
        }

        public static TreeMap<String, Gakuseki> getYearGakusekiMap(final String schoolKind, List<Gakuseki> gakusekiList) {
            final TreeMap<String, Gakuseki> yearGakusekiMap = new TreeMap<String, Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiList) {
                if (null == schoolKind || null != schoolKind && schoolKind.equals(gakuseki._schoolKind)) {
                    yearGakusekiMap.put(gakuseki._year, gakuseki);
                }
            }
            return yearGakusekiMap;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学校クラス>>。
     */
    private static class SchoolInfo {

        private String _schoolAddress1;
        private String _schoolAddress2;
        private String _schoolZipcode;
        private String _schoolName1;
        private String _bunkouSchoolAddress1;
        private String _bunkouSchoolAddress2;
        private String _bunkouSchoolName;
        /** 写真データ格納フォルダ */
        private String _imageDir;
        /** 写真データの拡張子 */
        private String _imageExt;

        /**
         * コンストラクタ。
         */
        SchoolInfo(final DB2UDB db2, final Param param, final Student student) {
            loadSchool(db2, param, student);

            _imageDir = "image/stamp";
            _imageExt = "bmp";
        }

        /**
         * 学校クラスを作成ます。
         */
        private void loadSchool(final DB2UDB db2, final Param param, final Student student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                KNJ_SchoolinfoSql obj = new KNJ_SchoolinfoSql("10000");
                final Map paramMap = new HashMap();
                ps = db2.prepareStatement(obj.pre_sql(paramMap));
                ps.setString(1, param._year); // 年度
                ps.setString(2, param._year); // 年度
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolAddress1 = StringUtils.defaultString(rs.getString("SCHOOLADDR1"));
                    _schoolAddress2 = StringUtils.defaultString(rs.getString("SCHOOLADDR2"));
                    _schoolZipcode = rs.getString("SCHOOLZIPCD");
                    _schoolName1 = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if ((student.isGakunenSei(param) || param._isGenkyuRyuchi) && param._isChuKouIkkan) {
                final String schoolName = loadSchoolName(db2, param);
                if (null != schoolName && !StringUtils.isEmpty(schoolName)) {
                    _schoolName1 = schoolName;
                }
            }

            try {
                _bunkouSchoolAddress1 = "";
                _bunkouSchoolAddress2 = "";
                _bunkouSchoolName = "";
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _bunkouSchoolAddress1 = StringUtils.defaultString(rs.getString("REMARK8"));
                    _bunkouSchoolAddress2 = StringUtils.defaultString(rs.getString("REMARK9"));
                    _bunkouSchoolName = rs.getString("REMARK4");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String loadSchoolName(final DB2UDB db2, final Param param) {
            String rtn = null;

            final String certifKindCd = CERTIF_KINDCD;
            final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT"
                + " WHERE YEAR='" + param._year + "'"
                + " AND CERTIF_KINDCD='" + certifKindCd + "'";
            rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + rtn + "]");
            return rtn;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒情報クラス>>。
     */
    private static class PersonalInfo {

        static final String SOGOTEKI_NA_GAKUSHU_NO_JIKAN = "総合的な学習の時間";
        static final String SOGOTEKI_NA_TANKYU_NO_JIKAN = "総合的な探究の時間";

        final String _studentRealName;
        final String _studentName;
        final boolean _isPrintRealName;
        final boolean _isPrintNameAndRealName;
        /** 最も古い履歴の生徒名 */
        final String _studentNameHistFirst;
        final String _annual;
        final String _checkCourseName;
        final String _courseName;
        final String _majorName;
        final String _studentKana;
        final String _studentKanaHistFirst;
        final String _guardKana;
        final String _guardKanaHistFirst;
        final String _guardName;
        final String _guardNameHistFirst;
        final String _guarantorKana;
        final String _guarantorKanaHistFirst;
        final String _guarantorName;
        final String _guarantorNameHistFirst;
        final String _birthday;
        final String _birthdayStr;
        final String _sex;
//        final String _finishDate;
//        final String _installationDiv;
//        final String _juniorSchoolName;
//        final String _finschoolTypeName;
        final String _comebackDate;
        private String _addressGrdHeader;

        /** 保護者のかわりに保証人を表示するか */
        private boolean _isPrintGuarantor;
        /** 住所履歴 */
        private List<AddressRec> _addressRecList;
        /** 保護者住所履歴 */
        private List<AddressRec> _guardianAddressRecList;
        private String _zaigakusubekiKikan;
        /** 学習記録データ */
        private List<StudyRec> _studyRecList;

        /** 代替科目の備考マップのキー(年度指定無し) */
        static final String _keyAll = "9999";
        /** [学習の記録] 備考を保持する */
        private GakushuBiko _gakushuBiko;

        private EntGrdHist _entGrdJ = new EntGrdHist();
        private EntGrdHist _entGrdH = new EntGrdHist();

        private boolean _isFirst;

        List<Gakuseki> _gakusekiList;

        /** 留年した年度 */
        Collection<String> _dropYears;

        /** 進路/就職情報 */
        List _afterGraduatedCourseTextList;

        /** 進路/就職情報（京都府用） */
        String _afterGraduatedCourseSenkouKindSub;

        private List _abroadYears = Collections.EMPTY_LIST;

        /**
         * コンストラクタ。
         */
        private PersonalInfo(
                final String studentRealName,
                final String studentName,
                final boolean isPrintRealName,
                final boolean isPrintNameAndRealName,
                final String studentNameHistFirst,
                final String annual,
                final String checkCourseName,
                final String courseName,
                final String majorName,
                final String studentKana,
                final String studentKanaHistFirst,
                final String guardKana,
                final String guardKanaHistFirst,
                final String guardName,
                final String guardNameHistFirst,
                final String guarantorKana,
                final String guarantorKanaHistFirst,
                final String guarantorName,
                final String guarantorNameHistFirst,
                final String birthday,
                final String birthdayStr,
                final String sex,
//                final String finishDate,
//                final String installationDiv,
//                final String juniorSchoolName,
//                final String finschoolTypeName,
                final String comebackDate
        ) {

            _studentRealName = studentRealName;
            _studentName = studentName;
            _isPrintRealName = isPrintRealName;
            _isPrintNameAndRealName = isPrintNameAndRealName;
            _studentNameHistFirst = studentNameHistFirst;
            _annual = annual;
            _checkCourseName = checkCourseName;
            _courseName = courseName;
            _majorName = majorName;
            _studentKana = studentKana;
            _studentKanaHistFirst = studentKanaHistFirst;
            _guardKana = guardKana;
            _guardKanaHistFirst = guardKanaHistFirst;
            _guardName = guardName;
            _guardNameHistFirst = guardNameHistFirst;
            _guarantorKana = guarantorKana;
            _guarantorKanaHistFirst = guarantorKanaHistFirst;
            _guarantorName = guarantorName;
            _guarantorNameHistFirst = guarantorNameHistFirst;
            _birthday = birthday;
            _birthdayStr = birthdayStr;
            _sex = sex;
//            _finishDate = finishDate;
//            _installationDiv = installationDiv;
//            _juniorSchoolName = juniorSchoolName;
//            _finschoolTypeName = finschoolTypeName;
            _comebackDate = comebackDate;
        }

        public static int gakusekiMinYear(final List<Gakuseki> gakusekiList) {
            int min = Integer.MAX_VALUE;
            for (final String year : gakusekiYearSet(gakusekiList)) {
                if (null == year) {
                    continue;
                }
                final int iyear = Integer.parseInt(year);
                if (0 != iyear) {
                    min = Math.min(min, iyear);
                }
            }
            return min == Integer.MAX_VALUE ? -1 : min;
        }

        /**
         * @param student
         * @return 最終年度を戻します。
         */
        private String getLastYear() {
            for (final ListIterator<Gakuseki> it = _gakusekiList.listIterator(_gakusekiList.size()); it.hasPrevious();) {
                final Gakuseki gakuseki = it.previous();
                if (null != gakuseki) {
                    return gakuseki._year;
                }
                break;
            }
            return null;
        }

        /**
         * 指定年度年次の学籍を得る
         * @param year 指定年度
         * @param annual1 指定年次
         * @return 学籍
         */
        private Gakuseki getGakuseki(final String year, final String annual1) {
            if (year == null || annual1 == null) {
                return null;
            }
            final String annual = Integer.valueOf(annual1).toString();
            for (final Gakuseki gaku : _gakusekiList) {
                if (year.equals(gaku._year) && annual.equals(gaku._annual)) {
                    return gaku;
                }
            }
            return null;
        }

        public static TreeSet<String> gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet<String> set = new TreeSet();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        /**
         * 個人情報クラスを作成ます。
         */
        private static PersonalInfo loadPersonal(final DB2UDB db2, final Student student, final String comebackDate, final Param param) {

            String studentRealName = null;
            String studentName = null;
            boolean isPrintRealName = false;
            boolean isPrintNameAndRealName = false;
            /** 最も古い履歴の生徒名 */
            String studentNameHistFirst = null;

            String annual = null;
            String studentKana = null;
            String studentKanaHistFirst = null;
            String checkCourseName = null;
            String courseName = null;
            String majorName = null;
            String guardKana = null;
            String guardKanaHistFirst = null;
            String guardName = null;
            String guardNameHistFirst = null;
            String guarantorKana = null;
            String guarantorKanaHistFirst = null;
            String guarantorName = null;
            String guarantorNameHistFirst = null;
            String birthday = null;
            String birthdayStr = null;
            String sex = null;
//            String finishDate = null;
//            String installationDiv = null;
//            String juniorSchoolName = null;
//            String finschoolTypeName = null;

            try {
                final String sql = sql_info_reg("1111111000", param, student._schregno);
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
                if (null != row) {

                    isPrintRealName = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME"));
                    isPrintNameAndRealName = "1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG"));

                    final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"));
                    final String nameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_HIST_FIRST"));
                    if (isPrintRealName) {
                        final String realName = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME"));
                        final String realNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "RN_HIST_FIRST"));
                        final String nameWithRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_WITH_RN_HIST_FIRST"));

                        if (isPrintNameAndRealName) {
                            if (StringUtils.isBlank(realName + name)) {
                                studentRealName = "";
                                studentName     = "";
                            } else {
                                studentRealName = realName;
                                studentName     = StringUtils.isBlank(name) ? "" : realName.equals(name) ? name : "（" + name + "）";
                            }
                            studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst.equals(nameWithRealNameHistFirst) ? realNameHistFirst : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                        } else {
                            studentRealName      = StringUtils.isBlank(realName) ? name : realName;
                            studentName          = name;
                            studentNameHistFirst = realNameHistFirst;
                        }
                    } else {
                        studentRealName      = "";
                        studentName          = name;
                        studentNameHistFirst = nameHistFirst;
                    }

                    final String nameKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_KANA"));
                    final String nameKanaHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_KANA_HIST_FIRST"));
                    if (isPrintRealName) {
                        final String realNameKana = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME_KANA"));
                        final String realNameKanaHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "RNK_HIST_FIRST"));
                        final String nameKanaWithRealNameKanaHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "KANA_WITH_RNK_HIST_FIRST"));

                        studentKana = getNameForm(nameKana0, realNameKana, isPrintRealName, isPrintNameAndRealName);
                        studentKanaHistFirst = getNameForm(nameKanaWithRealNameKanaHistFirst, realNameKanaHistFirst, isPrintRealName, isPrintNameAndRealName);

                    } else {
                        studentKana = nameKana0;
                        studentKanaHistFirst = nameKanaHistFirst0;
                    }

                    annual = KnjDbUtils.getString(row, "ANNUAL");
                    checkCourseName = KnjDbUtils.getString(row, "COURSENAME");
//                    if (param._isKyoto && student.isTanniSei(param)) {
//                        // 京都府単位制
//                        final String schooldivName = StringUtils.defaultString((String) param._z001name1.get(student.getSchooldiv(param)));
//                        final String coursename = StringUtils.defaultString(KnjDbUtils.getString(row, "COURSENAME"));
//                        courseName = schooldivName + "による" + coursename;
//                    } else {
                        courseName = KnjDbUtils.getString(row, "COURSENAME");
//                    }
                    majorName = KnjDbUtils.getString(row, "MAJORNAME");

                    final String guardKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_KANA"));
                    final String guardKanaHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_KANA_HIST_FIRST"));
                    final String guardName0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_NAME"));
                    final String guardNameHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_NAME_HIST_FIRST"));
                    final String guarantorKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_KANA"));
                    final String guarantorKanaHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_KANA_HIST_FIRST"));
                    final String guarantorName0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_NAME"));
                    final String guarantorNameHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_NAME_HIST_FIRST"));


                    final boolean isGuardPrintRealName = "1".equals(KnjDbUtils.getString(row, "USE_GUARD_REAL_NAME"));
                    final boolean isGuardPrintNameAndRealName = "1".equals(KnjDbUtils.getString(row, "GUARD_NAME_OUTPUT_FLG"));
                    if (isGuardPrintRealName) {
                        final String guardRealKana = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_REAL_KANA"));
                        final String guardRealKanaHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_R_KANA_WITH_RN_HIST_FIRST"));
                        final String guardKanaWithGuardRealKanaHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_KANA_WITH_RN_HIST_FIRST"));

                        final String guardRealName = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_REAL_NAME"));
                        final String guardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_R_NAME_WITH_RN_HIST_FIRST"));
                        final String guardNameWithGuardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_NAME_WITH_RN_HIST_FIRST"));

                        final String guarantorRealKana = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_REAL_KANA"));
                        final String guarantorRealKanaHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_R_KANA_WITH_RN_HIST_FIRST"));
                        final String guarantorKanaWithGuarantorRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_KANA_WITH_RN_HIST_FIRST"));

                        final String guarantorRealName = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_REAL_NAME"));
                        final String guarantorRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_R_NAME_WITH_RN_HIST_FIRST"));
                        final String guarantorNameWithGuarantorRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_NAME_WITH_RN_HIST_FIRST"));

                        guardKana = getNameForm(guardKana0, guardRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guardKanaHistFirst = getNameForm(guardKanaWithGuardRealKanaHistFirst, guardRealKanaHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guardName = getNameForm(guardName0, guardRealName, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guardNameHistFirst = getNameForm(guardNameWithGuardRealNameHistFirst, guardRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guarantorKana = getNameForm(guarantorKana0, guarantorRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guarantorKanaHistFirst = getNameForm(guarantorKanaWithGuarantorRealNameHistFirst, guarantorRealKanaHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guarantorName = getNameForm(guarantorName0, guarantorRealName, isGuardPrintRealName, isGuardPrintNameAndRealName);
                        guarantorNameHistFirst = getNameForm(guarantorNameWithGuarantorRealNameHistFirst, guarantorRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);

                    } else {
                        guardKana = guardKana0;
                        guardKanaHistFirst = guardKanaHistFirst0;
                        guardName = guardName0;
                        guardNameHistFirst = guardNameHistFirst0;
                        guarantorKana = guarantorKana0;
                        guarantorKanaHistFirst = guarantorKanaHistFirst0;
                        guarantorName = guarantorName0;
                        guarantorNameHistFirst = guarantorNameHistFirst0;
                    }
                    birthday = KnjDbUtils.getString(row, "BIRTHDAY");
                    birthdayStr = getBirthday(db2, birthday, KnjDbUtils.getString(row, "BIRTHDAY_FLG"), param);
                    sex = KnjDbUtils.getString(row, "SEX");
//                    finishDate = setDateFormat(formatDateM(KnjDbUtils.getString(row, "FINISH_DATE"), param), param._year, param);
//                    log.debug("schoolmark = "+param._definecode.schoolmark);
//                    if (!"HIRO".equals(param._definecode.schoolmark)) {
//                        installationDiv = KnjDbUtils.getString(row, "INSTALLATION_DIV");
//                    }
//                    juniorSchoolName = KnjDbUtils.getString(row, "J_NAME");
//                    finschoolTypeName = KnjDbUtils.getString(row, "FINSCHOOL_TYPE_NAME");
                }
            } catch (final Exception e) {
                log.error("個人情報クラス作成にてエラー", e);
            }

            final PersonalInfo personalInfo = new PersonalInfo(
                    studentRealName,
                    studentName,
                    isPrintRealName,
                    isPrintNameAndRealName,
                    studentNameHistFirst,
                    annual,
                    checkCourseName,
                    courseName,
                    majorName,
                    studentKana,
                    studentKanaHistFirst,
                    guardKana,
                    guardKanaHistFirst,
                    guardName,
                    guardNameHistFirst,
                    guarantorKana,
                    guarantorKanaHistFirst,
                    guarantorName,
                    guarantorNameHistFirst,
                    birthday,
                    birthdayStr,
                    sex,
//                    finishDate,
//                    installationDiv,
//                    juniorSchoolName,
//                    finschoolTypeName,
                    comebackDate
            );

            personalInfo._entGrdJ = KNJA130CCommon.EntGrdHist.load(db2, param._semesterMap, param._definecode, param._useAddrField2, Param.SCHOOL_KIND_J, student._schregno, comebackDate);
            personalInfo._entGrdH = KNJA130CCommon.EntGrdHist.load(db2, param._semesterMap, param._definecode, param._useAddrField2, Param.SCHOOL_KIND_H, student._schregno, comebackDate);
            personalInfo._gakusekiList = loadGakuseki(db2, student, param, personalInfo._entGrdH._grdDate);
            log.debug(" gakusekiList = " + personalInfo._gakusekiList);
            personalInfo._dropYears = student.getDropYears(personalInfo._gakusekiList, param);
            personalInfo._afterGraduatedCourseTextList = Student.loadAfterGraduatedCourse(db2, student._schregno, personalInfo._gakusekiList);
            personalInfo._afterGraduatedCourseSenkouKindSub = Student.loadAfterGraduatedCourseSenkouKindSub(db2, student._schregno, param);
            if (!personalInfo._gakusekiList.isEmpty()) {
                personalInfo._abroadYears = Student.getAbroadYears(db2, student._schregno, personalInfo._gakusekiList);
            }
            personalInfo._gakushuBiko = new GakushuBiko();
            GakushuBiko.createStudyRecBiko(db2, student._schregno, param, personalInfo._gakushuBiko);
            personalInfo._studyRecList = StudyRec.loadStudyRec(db2, student, personalInfo, personalInfo._gakusekiList, param, personalInfo._gakushuBiko._ssc);
            GakushuBiko.createStudyRecBikoSubstitution(param, personalInfo._gakushuBiko);
            final String startDate = (personalInfo._entGrdJ._setData ? personalInfo._entGrdJ : personalInfo._entGrdH)._entDate;
            personalInfo._addressRecList = AddressRec.loadAddressRec(db2, student._schregno, param, AddressRec.SQL_SCHREG, startDate, personalInfo._entGrdH._grdDate);
            personalInfo._isPrintGuarantor = getPrintGuarantor(personalInfo);
            personalInfo._addressGrdHeader = personalInfo._isPrintGuarantor ? "保証人" : "保護者";
            personalInfo._guardianAddressRecList = AddressRec.loadAddressRec(db2, student._schregno, param, personalInfo._isPrintGuarantor ? AddressRec.SQL_GUARANTOR : AddressRec.SQL_GUARDIAN, startDate, personalInfo._entGrdH._grdDate);
//            personalInfo._zaigakusubekiKikan = getZaigakusubekiKikan(db2, param, student, personalInfo);

            return personalInfo;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Student student, final Param param, final String grdDate) {
            final String certifKind = CERTIF_KINDCD;
            StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH T_INKAN AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
            stb.append("         STAFFCD ");
            stb.append("     FROM ");
            stb.append("         ATTEST_INKAN_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD ");
            stb.append(" ), YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MAX(SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = '" + student._schregno + "' ");
            stb.append("     GROUP BY YEAR ");
            if (null != grdDate) {
                stb.append("     UNION ALL ");
                stb.append("     SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
                stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("         AND '" + grdDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("     WHERE T1.SCHREGNO = '" + student._schregno + "' ");
            }
            stb.append(" ), MIN_YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MIN(SEMESTER) AS SEMESTER ");
            stb.append("     FROM YEAR_SEMESTER ");
            stb.append("     GROUP BY YEAR ");
            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO ");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND IN ('" + Param.SCHOOL_KIND_H + "', '" + Param.SCHOOL_KIND_J + "') ");
            stb.append("      WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER = (SELECT SEMESTER FROM  MIN_YEAR_SEMESTER WHERE YEAR = T1.YEAR) ");
//            if (student.isGakunenSei(param) && param._isHeisetuKou && param._isHigh) {
//                stb.append("      AND T1.GRADE not in " + param._gradeInChugaku + " ");
//            }

            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T8.SCHOOL_KIND ");
            stb.append("   ,T8.GRADE_CD ");
            stb.append("   ,T8.GRADE_NAME2 ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");

            // 印鑑関連 2
            stb.append("   ,ATTEST.CHAGE_OPI_SEQ ");
            stb.append("   ,ATTEST.LAST_OPI_SEQ ");
            stb.append("   ,ATTEST.FLG ");

            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + certifKind + "'");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append("          AND T8.GRADE = T1.GRADE ");
            stb.append(" LEFT JOIN ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.CHAGE_OPI_SEQ, ");
            stb.append("         T1.LAST_OPI_SEQ, ");
            stb.append("         L1.FLG ");
            stb.append("     FROM ");
            stb.append("         ATTEST_OPINIONS_WK T1 ");
            stb.append("         LEFT JOIN ATTEST_OPINIONS_UNMATCH L1 ");
            stb.append("                ON L1.YEAR = T1.YEAR ");
            stb.append("               AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("      ) ATTEST ON ATTEST.YEAR = T1.YEAR AND ATTEST.SCHREGNO = T1.SCHREGNO ");
            stb.append(" order by t1.hr_class");

            return stb.toString();
        }

        /**
         * 学籍履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<Gakuseki> loadGakuseki(final DB2UDB db2, final Student student, final Param param, final String grdDate) {
            final List<Gakuseki> gakusekiList = new LinkedList();
            final Map hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
            try {
                final String sql = sqlSchGradeRec(student, param, grdDate);
                // log.debug(" gakuseki sql = " + sql);
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String nendo;
                    if (param._isSeireki) {
                        nendo = String.valueOf(year) + "年度";
                    } else {
                        nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
                    }
                    final String grade = KnjDbUtils.getString(row, "GRADE");
                    final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                    final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                    final String gradeName2 = KnjDbUtils.getString(row, "GRADE_NAME2");
                    final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                    String hrname = null;
                    if ("1".equals(param._useSchregRegdHdat)) {
                        hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    } else if ("0".equals(param._useSchregRegdHdat)) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass, hmap);
                    }

                    if (null == hrname) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass);
                    }

                    final String attendno = !NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? KnjDbUtils.getString(row, "ATTENDNO") : String.valueOf(KnjDbUtils.getInt(row, "ATTENDNO", 0)); // + (param._isTokiwa ? "番" : "");
                    final String annual = String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL")));
                    final String principalName = KnjDbUtils.getString(row, "PRINCIPALNAME");
                    final String principalStaffcd = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD");

                    final String principalStampNo;
                    final String staffSeq;
                    final String principalSeq;
                    final String kaizanFlg;
                    if (null != param._inei) { // || (param._isKyoto && null != param._staffGroupcd && param._staffGroupcd.startsWith("999"))) {
                        staffSeq = KnjDbUtils.getString(row, "CHAGE_OPI_SEQ");
                        principalSeq = KnjDbUtils.getString(row, "LAST_OPI_SEQ");
                        kaizanFlg = KnjDbUtils.getString(row, "FLG");
                        principalStampNo = KnjDbUtils.getString(row, "LAST_STAMP_NO");
                    } else {
                        staffSeq = null;
                        principalSeq = null;
                        kaizanFlg = null;
                        principalStampNo = null;
                    }

                    final Staff principal = new Staff(year, new StaffMst(principalStaffcd, principalName, null, null, null), null, null, principalStampNo);

                    final Gakuseki gakuseki = new Gakuseki(year, nendo, grade, schoolKind, gradeCd, gradeName2, hrclass, hrname, attendno, annual,
                            principal, staffSeq, principalSeq, kaizanFlg);
                    if (!param.isGdat(gakuseki._year, gakuseki._grade)) {
                        log.warn(" oi! gakuseki = " + gakuseki);
                        continue;
                    }
                    log.info(" add gakuseki = " + gakuseki);
                    gakusekiList.add(gakuseki);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }

            // リストをソートします。
            Collections.sort(gakusekiList);

            // 学年制の場合、留年対応します。
            if (student.isGakunenSei(param) || param._isGenkyuRyuchi) {
                String grade = null;
                for (ListIterator i = gakusekiList.listIterator(gakusekiList.size()); i.hasPrevious();) {
                    final Gakuseki gaku = (Gakuseki) i.previous();
                    if (null != grade && gaku._grade.equals(grade)) {
                        gaku._isDrop = true;
                    }
                    grade = gaku._grade;
                }
            }
            return gakusekiList;
        }

        private static boolean getPrintGuarantor(final PersonalInfo personalInfo) {
            boolean isPrintGuarantor = false;
//            final String entdate = personalInfo._entDate; // 入学日付
//            final String birthday = personalInfo._birthday;
//            try {
//                final BigDecimal diff = diffYear(birthday, entdate);
//                final int age = diff.setScale(0, BigDecimal.ROUND_DOWN).intValue();
//                // 入学時の年齢が20歳以上なら保護者ではなく保証人を表示
//                if (age >= 20) {
//                    isPrintGuarantor = true;
//                }
//                //log.debug(" student age = " + diff + " [year]  isPrintGuarantor? " + isPrintGuarantor);
//            } catch (Exception e) {
//                log.error("exception!", e);
//            }
            return isPrintGuarantor;
        }

        private static BigDecimal diffYear(final String date1, final String date2) {
            // log.debug(" diffYear date1 ='" + date1 + "', date2 = '" + date2 + "' ");
            if (null == date1 || null == date2) {
                return BigDecimal.valueOf(0);
            }
            final BigDecimal ALL_DAY_OF_YEAR = new BigDecimal(365);
            final Calendar cal1 = getCalendarOfDate(date1);
            final int y1 = cal1.get(Calendar.YEAR);
            final int doy1 = cal1.get(Calendar.DAY_OF_YEAR);

            final Calendar cal2 = getCalendarOfDate(date2);
            final int y2 = cal2.get(Calendar.YEAR);
            final int doy2 = cal2.get(Calendar.DAY_OF_YEAR);

            final BigDecimal diff = new BigDecimal(y2 - y1).add(new BigDecimal(doy2 - doy1).divide(ALL_DAY_OF_YEAR, 10, BigDecimal.ROUND_DOWN));
            return diff;
        }

        public int getYearBegin() {
            EntGrdHist entGrd;
            if (_entGrdJ._setData) {
                entGrd = _entGrdJ;
            } else {
                entGrd = _entGrdH;
            }
            return null == entGrd._entDate ? 0 : getNendo(getCalendarOfDate(entGrd._entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _entGrdH._grdDate ? 9999 : getNendo(getCalendarOfDate(_entGrdH._grdDate)));
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            final String birthday;
            if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(db2, date));
            }
            return birthday;
        }

        private static String getNameForm(final String name, final String realname, final boolean showreal, final boolean showboth) {
            final String rtn;
            if (showboth && !StringUtils.isBlank(realname) && !StringUtils.isBlank(name)) {
                if (realname.equals(name)) {
                    rtn = realname;
                } else {
                    rtn = realname + "（" + name + "）";
                }
            } else if (showreal && !StringUtils.isBlank(realname)) {
                rtn = realname;
            } else if (!StringUtils.isBlank(name)) {
                rtn = name;
            } else {
                rtn = "";
            }
            return rtn;
        }

        private static String sql_info_reg(String t_switch, final Param param, final String schregno) {

            if (t_switch.length() < 8) {
                final StringBuffer stbx = new StringBuffer(t_switch);
                stbx.append("000000");
                t_switch = stbx.toString();
            }
            final String ts0 = t_switch.substring(0, 1);
            //final String ts1 = t_switch.substring(1, 2);
            final String ts2 = t_switch.substring(2, 3);
            //final String ts3 = t_switch.substring(3, 4);
            final String ts4 = t_switch.substring(4, 5);
            final String ts5 = t_switch.substring(5, 6);
            final String ts6 = t_switch.substring(6, 7);
            final String ts7 = t_switch.substring(7, 8);

            final StringBuffer sql = new StringBuffer();

            sql.append("SELECT ");
            sql.append("T2.NAME,");
            sql.append("T2.REAL_NAME,");
            sql.append("T2.GRD_DATE, ");
            sql.append("T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("T14.NAME_KANA AS NAME_KANA_HIST_FIRST, ");
            sql.append("T18.REAL_NAME AS RN_HIST_FIRST, ");
            sql.append("T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            sql.append("T18.REAL_NAME_KANA AS RNK_HIST_FIRST, ");
            sql.append("T18.NAME_KANA AS KANA_WITH_RNK_HIST_FIRST, ");
            if (ts7.equals("1")) {
                sql.append("T2.NAME_ENG,");
            }
            sql.append("T2.NAME_KANA,T2.REAL_NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,");
            sql.append("T21.BIRTHDAY_FLG,");
            if (ts7.equals("1")) {
                sql.append("T7.ABBV2 AS SEX_ENG,");
            }
            sql.append("T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
            // 課程・学科・コース
            if (!ts2.equals("0")) {
                sql.append("T3.COURSENAME,");
                if (param._hasMAJOR_MST_MAJORNAME2) {
                    sql.append("VALUE(T4.MAJORNAME2, T4.MAJORNAME) AS MAJORNAME,");
                } else {
                    sql.append("T4.MAJORNAME,");
                }
                sql.append("T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
                if (ts7.equals("1"))
                    sql.append("T3.COURSEENG,T4.MAJORENG,");
            }
            // 卒業中学情報
            if (ts4.equals("1")) {
                sql.append("T2.FINISH_DATE,");
                sql.append("FIN_S.FINSCHOOL_NAME AS J_NAME,");
                if (!"HIRO".equals(param._definecode.schoolmark)) {
                    sql.append("NM_MST.NAME1 AS INSTALLATION_DIV,");
                }
                sql.append("VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            }
            // 保護者情報
            if (ts5.equals("1")) {
                sql.append("T12.GUARD_NAME, ");
                sql.append("T12.GUARD_REAL_NAME, ");
                sql.append("T16.GUARD_NAME AS GUARD_NAME_HIST_FIRST, ");
                sql.append("T12.GUARD_REAL_KANA, ");
                sql.append("T12.GUARD_KANA,");
                sql.append("T16.GUARD_KANA AS GUARD_KANA_HIST_FIRST, ");
                sql.append("T20.GUARD_REAL_NAME AS G_R_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T20.GUARD_NAME      AS G_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T20.GUARD_REAL_KANA AS G_R_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("T20.GUARD_KANA      AS G_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,");
                sql.append("T12.GUARD_ADDR1,T12.GUARD_ADDR2,T12.GUARD_ZIPCD,");

                sql.append("T12.GUARANTOR_NAME, ");
                sql.append("T12.GUARANTOR_REAL_NAME, ");
                sql.append("T23.GUARANTOR_NAME AS GUARANTOR_NAME_HIST_FIRST, ");
                sql.append("T12.GUARANTOR_REAL_KANA, ");
                sql.append("T12.GUARANTOR_KANA,");
                sql.append("T23.GUARANTOR_KANA AS GUARANTOR_KANA_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_REAL_NAME AS GRT_R_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_NAME      AS GRT_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_REAL_KANA AS GRT_R_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_KANA      AS GRT_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("VALUE(T12.GUARANTOR_ADDR1,'') || VALUE(T12.GUARANTOR_ADDR2,'') AS GUARANTOR_ADDR,");
                sql.append("T12.GUARANTOR_ADDR1,T12.GUARANTOR_ADDR2,T12.GUARANTOR_ZIPCD,");
            }
            sql.append("(CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("T11.NAME_OUTPUT_FLG, ");
            sql.append("(CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("(   SELECT     * ");
            sql.append("FROM       SCHREG_REGD_DAT T1 ");
            sql.append("WHERE      T1.SCHREGNO= '" + schregno + "' AND T1.YEAR= '" + param._year + "' ");
            if (ts6.equals("1")) { // 学期を特定
                sql.append("AND T1.SEMESTER= '" + param._gakki + "' ");
            } else {
                // 最終学期
                sql.append("AND T1.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO= '" + schregno + "' AND YEAR= '" + param._year + "')");
            }

            sql.append(") T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            sql.append("AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            // 卒業情報有りの場合
            if (ts0.equals("1")) {
                sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR ");
            sql.append("    AND REGDG.GRADE = T1.GRADE ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = T2.FINSCHOOLCD ");
            if (!"HIRO".equals(param._definecode.schoolmark)) {
                sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            }
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            if (!ts2.equals("0")) {
                sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
                sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
                sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
                sql.append("AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            }
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T2.SCHREGNO AND T11.DIV = '02' ");
            // 保護者情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ");
            }

            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T13 ON T13.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = T13.SCHREGNO AND T14.ISSUEDATE = T13.ISSUEDATE ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T15 ON T15.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT T16 ON T16.SCHREGNO = T15.SCHREGNO AND T16.ISSUEDATE = T15.ISSUEDATE ");
            }
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T17 ON T17.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = T17.SCHREGNO AND T18.ISSUEDATE = T17.ISSUEDATE ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_REAL_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T19 ON T19.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT T20 ON T20.SCHREGNO = T19.SCHREGNO AND T20.ISSUEDATE = T19.ISSUEDATE ");
            }
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = T2.SCHREGNO AND T26.DIV = '02' ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARANTOR_HIST_DAT WHERE GUARANTOR_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T22 ON T22.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARANTOR_HIST_DAT T23 ON T23.SCHREGNO = T15.SCHREGNO AND T23.ISSUEDATE = T22.ISSUEDATE ");

                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARANTOR_HIST_DAT WHERE GUARANTOR_REAL_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T24 ON T24.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARANTOR_HIST_DAT T25 ON T25.SCHREGNO = T24.SCHREGNO AND T25.ISSUEDATE = T24.ISSUEDATE ");
            }

            return sql.toString();
        }

        public AddressRec getStudentAddressMax() {
            return _addressRecList == null || _addressRecList.isEmpty() ? null : (AddressRec) _addressRecList.get(0);
        }

//        private static String getZaigakusubekiKikan(final DB2UDB db2, final Param param, final Student student, final PersonalInfo personalInfo) {
//            if (!param._isPrintZaigakusubekiKikan) {
//                return null;
//            }
//            // 単位制のみ対象
//            if (!"1".equals(student.getSchooldiv(param))) {
//                return null;
//            }
//            // 編入生のみ対象
//            if (!"5".equals(personalInfo._entDiv)) {
//                return null;
//            }
//            final String entdate = personalInfo._entDate;
//            if (null == entdate) {
//                return null;
//            }
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            int zensekiPeriodMonthCnt = 0;
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT ");
//                stb.append("     T1.PERIOD_MONTH_CNT ");
//                stb.append(" FROM ");
//                stb.append("     ANOTHER_SCHOOL_HIST_DAT T1 ");
//                stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ");
//                stb.append(" WHERE ");
//                stb.append("     T1.SCHREGNO = '" + student._schregno + "'");
//                stb.append("     AND FISCALYEAR(T1.REGD_S_DATE) <= '" + param._year + "' ");
//                stb.append(" ORDER BY ");
//                stb.append("     T1.REGD_S_DATE DESC, ");
//                stb.append("     T1.REGD_E_DATE DESC, ");
//                stb.append("     T1.SEQ ");
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    if (NumberUtils.isDigits(rs.getString("PERIOD_MONTH_CNT"))) {
//                        zensekiPeriodMonthCnt += rs.getInt("PERIOD_MONTH_CNT");
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            final Calendar cEntDate = getCalendarOfDate(entdate);
//            cEntDate.set(Calendar.DAY_OF_MONTH, 1);
//            cEntDate.add(Calendar.MONTH, 36 - zensekiPeriodMonthCnt);
//            cEntDate.add(Calendar.DAY_OF_MONTH, -1);
//            final String y = String.valueOf(cEntDate.get(Calendar.YEAR));
//            final String m = String.valueOf(1 + cEntDate.get(Calendar.MONTH));
//            final String d = String.valueOf(cEntDate.get(Calendar.DAY_OF_MONTH));
//            final String eDate =  y + "-" + m + "-" + d;
//            return "在学すべき期間" + formatDate(eDate, param) + "まで";
//        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・科目別修得単位数計 Student._studyRecSubclass。
         * <br />
         *  ・年度別修得単位数計 Student._studyRecYear。
         * </pre>
         */
        private static Map createStudyRecTotal(final List<StudyRec> studyRecList, final Collection dropYears, final Param param, final int gakusekiListIdx, final List gakusekiList) {
            final Collection yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
//            log.debug(" years = " + yearSet);
            final int minYear = PersonalInfo.gakusekiMinYear(gakusekiList);

            final Map studyRecSubclassMap = new TreeMap();
            final Map subclassStudyrecListMap = new TreeMap();
            for (final StudyRec studyrec : studyRecList) {
                if (param._isPrintAnotherStudyrec2 && 0 == gakusekiListIdx && NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) < minYear) {
                    // 前籍校も出力する
                } else {
                    if (!yearSet.contains(studyrec._year)) {
                        continue;
                    }
                }
                getMappedList(subclassStudyrecListMap, studyrec.getKeySubclasscdForSubclassTotal(param)).add(studyrec);
            }
            for (final Iterator it = subclassStudyrecListMap.keySet().iterator(); it.hasNext();) {
                final String subClasscd = (String) it.next();
                final ArrayList subclassStudyrecList = (ArrayList) subclassStudyrecListMap.get(subClasscd);
                final StudyRecSubclassTotal subclassTotal = new StudyRecSubclassTotal(subclassStudyrecList, dropYears);
//                log.debug(" subclassTotal = " + subclassTotal);
                studyRecSubclassMap.put(subClasscd, subclassTotal);
            }
            return studyRecSubclassMap;
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・科目別修得単位数計 Student._studyRecSubclass。
         * <br />
         *  ・年度別修得単位数計 Student._studyRecYear。
         * </pre>
         */
        private static Map createStudyRecYear(final Param param, final boolean isNewForm, final List<StudyRec> studyRecList, final Collection<String> dropYears, final Collection<String> dropShowYears) {
            final Map<String, List<StudyRec>> yearListMap = new HashMap();
            for (final StudyRec studyrec : studyRecList) {
//                log.fatal(" studyrec [risyunomi:" + studyrec.isRisyuNomi(param) + ", mirisyu:" + studyrec.isMirisyu(param) + "] " + studyrec._subClasscd + " : " + studyrec._subClassName);
                if (!isNewForm && studyrec.isRisyuNomi(param)) {
                    // 京都府は平成24年度以前で履修のみの場合様式1裏に表示しない
                    continue;
                } else if (studyrec.isMirisyu(param)) {
                    // 京都府は単位不認定（未履修）の場合様式1裏に表示しない
                    continue;
                }
                getMappedList(yearListMap, studyrec._year).add(studyrec);
            }

            final Map<String, StudyRecYearTotal> studyRecYear = new HashMap();
            for (final String year : yearListMap.keySet()) {
                final List<StudyRec> studyrecList = yearListMap.get(year);

                if (!studyRecYear.containsKey(year)) {
                    final byte isDrop;
                    if (dropShowYears.contains(year)) {
                        isDrop = StudyRecYearTotal.DROP_SHOW;
                    } else if (dropYears.contains(year)) {
                        isDrop = StudyRecYearTotal.DROP;
                    } else {
                        isDrop = StudyRecYearTotal.GET;
                    }
                    studyRecYear.put(year, new StudyRecYearTotal(year, isDrop));
                }
                final StudyRecYearTotal yeartotal = studyRecYear.get(year);

                for (final StudyRec studyrec : studyrecList) {
//                    log.debug(" studyrec " + studyrec._year + ":" + studyrec._subClasscd + " -> " +  studyrec._credit + ", " + studyrec._compCredit + ", " + studyrec._creditMstCredits);
                    yeartotal._studyRecList.add(studyrec);
                }

//                log.debug(" year = " + yeartotal._year + " : credit = " + yeartotal._total + ", comp = " + yeartotal._totalComp + ", creditMstCredit = " + yeartotal._totalCreditMst);
            }

            return studyRecYear;
        }

        public boolean isTargetYear(final String year, final Param param) {
            if (!NumberUtils.isDigits(year)) {
                return false;
            } else if ((ANOTHER_YEAR.equals(year) || Integer.parseInt(year) < getYearBegin()) && _isFirst || getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= getYearEnd(param)) {
                return true;
            }
            return false;
        }

        /**
         * 成績等を表示するか
         * @param year 年度
         * @param param
         * @return
         */
        public boolean isTargetYearLast(final String year, final Student student, final Param param) {
            if (!NumberUtils.isDigits(year)) {
                return false;
            } else if ((ANOTHER_YEAR.equals(year) || Integer.parseInt(year) < getYearBegin()) && _isFirst || getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= getPersonalInfoYearEnd(student, param)) {
                return true;
            }
            return false;
        }

        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final Student student, final Param param) {
            final TreeSet yearSetAll = new TreeSet();
            final List<PersonalInfo> personalInfoList = student.getPrintSchregEntGrdHistList(param);
            for (final ListIterator<PersonalInfo> it = personalInfoList.listIterator(personalInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final PersonalInfo personalInfo = it.previous();
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                final TreeSet yearSet = new TreeSet();
                for (int y = begin; y <= end; y++) {
                    final Integer year = new Integer(y);
                    if (yearSetAll.contains(year)) {
                        // 新しい生徒情報で表示されるものは含まない
                    } else {
                        yearSetAll.add(year);
                        yearSet.add(year);
                    }
                }
                if (this == personalInfo) {
                    if (yearSet.isEmpty()) {
                        return -1; // 対象の生徒情報は成績等は表示しない
                    }
                    return ((Integer) yearSet.last()).intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }

        /**
         * 指定年度の教科コード90の代替科目備考を得る。
         * @param key 年度のキー
         * @return 備考の配列。なければnullを返す
         */
        private List<String> getArraySubstitutionBiko90(final String key, final Param param) {
            final List<String> list = new ArrayList<String>();
            final List<String> substZenbu = _gakushuBiko.getStudyRecBikoSubstitution90(StudyrecSubstitutionContainer.ZENBU, _gakusekiList, _keyAll, param).get(key);
            if (null != substZenbu) {
                list.addAll(substZenbu);
            }
            final List<String> substIchibu = _gakushuBiko.getStudyRecBikoSubstitution90(StudyrecSubstitutionContainer.ICHIBU, _gakusekiList, _keyAll, param).get(key);
            if (null != substIchibu) {
                list.addAll(substIchibu);
            }
            return list;
        }

        public String getSogoSubclassname(final Param param, final TreeMap<String, Gakuseki> yearGakusekiMap) {
            final int tankyuStartYear = 2019;
            boolean isTankyu = false;
            String minYear = null;
            Gakuseki minYearGakuseki = null;
            final int geta = 3; // 札幌開成は高校1年のGRADE_CDは4
            if (NumberUtils.isDigits(_entGrdH._curriculumYear)) {
                isTankyu = Integer.parseInt(_entGrdH._curriculumYear) >= tankyuStartYear;
            } else {
                yearGakusekiMap.remove("0");
                if (!yearGakusekiMap.isEmpty()) {
                    minYear = yearGakusekiMap.firstKey();
                    minYearGakuseki = yearGakusekiMap.get(minYear);
                }
                if (null != minYearGakuseki) {
                    final int year = NumberUtils.isDigits(minYearGakuseki._year) ? Integer.parseInt(minYearGakuseki._year) : 9999;
                    final int gradeCdInt = minYearGakuseki._gradeCd;
                    if (year == tankyuStartYear     && gradeCdInt <= 1 + geta
                     || year == tankyuStartYear + 1 && gradeCdInt <= 2 + geta
                     || year == tankyuStartYear + 2 && gradeCdInt <= 3 + geta
                     || year >= tankyuStartYear + 3
                            ) {
                        isTankyu = true;
                    }
                }
            }
            if (param._isOutputDebug) {
                log.info(" 探究? " + isTankyu + ", startYear = " + minYear + ", minYearGakuseki = " + minYearGakuseki + ", curriculumYear = " + _entGrdH._curriculumYear);
            }
            return isTankyu ? SOGOTEKI_NA_TANKYU_NO_JIKAN : SOGOTEKI_NA_GAKUSHU_NO_JIKAN;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒住所履歴クラス>>。
     */
    private static class AddressRec {
        static final int SQL_SCHREG = 0;
        static final int SQL_GUARDIAN = 1;
        static final int SQL_GUARANTOR = 2;

        final int _idx;
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCode;
        final boolean _isPrintAddr2;

        private AddressRec(final int idx, final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _idx = idx;
            _issuedate = issuedate;
            _address1 = addr1;
            _address2 = addr2;
            _zipCode = zip;
            _isPrintAddr2 = isPrintAddr2;
        }
        public String toString() {
            return "AddressRec(" + _idx + ":" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }

        private static List getPrintAddressRecList(final List addressRecList, final int max) {
            final LinkedList rtn = new LinkedList();
            if (addressRecList.isEmpty()) {
                return rtn;
            }
            rtn.add(addressRecList.get(0));
            rtn.addAll(reverse(take(max - rtn.size(), reverse(drop(1, addressRecList)))));
            return rtn;
        }
        private static List take(final int count, final List list) {
            final LinkedList rtn = new LinkedList();
            for (int i = 0; i < count && i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        private static List drop(final int count, final List list) {
            final LinkedList rtn = new LinkedList();
            for (int i = count; i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        private static List reverse(final List list) {
            final LinkedList rtn = new LinkedList();
            for (final ListIterator it = list.listIterator(list.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        static boolean isSameAddressList(final List<AddressRec> addrListA, final List<AddressRec> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                rtn = false;
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final AddressRec addressAi = addrListA.get(i);
                    final AddressRec addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        static boolean isSameAddress(final AddressRec addressAi, final AddressRec addressBi) {
            boolean rtn = true;
            if (null == addressAi || null == addressBi) {
                rtn = false;
            } else {
                if (null == addressAi._address1 && null == addressBi._address1) {
                } else if (null == addressAi._address1 || null == addressBi._address1 || !addressAi._address1.equals(addressBi._address1)) {
                    rtn = false;
                }
                if (null == addressAi._address2 && null == addressBi._address2) {
                } else if (!addressAi._isPrintAddr2 && !addressBi._isPrintAddr2) {
                } else if (null == addressAi._address2 || null == addressBi._address2 || !addressAi._address2.equals(addressBi._address2)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 住所履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List loadAddressRec(final DB2UDB db2, final String schregno, final Param param, final int sqlflg, final String startDate, final String endDate) {
            final List addressRecList = new LinkedList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sqlAddressDat = sqlAddressDat(sqlflg, startDate, endDate);
                ps = db2.prepareStatement(sqlAddressDat);
                ps.setString(1, schregno);
                ps.setString(2, param._year);
                rs = ps.executeQuery();
                int idx = 0;
                while (rs.next()) {
                    final String issuedate = rs.getString("ISSUEDATE");
                    final String address1 = rs.getString("ADDR1");
                    final String address2 = rs.getString("ADDR2");
                    final String zipCode = rs.getString("ZIPCD");
                    final boolean isPrintAddr2 = "1".equals(rs.getString("ADDR_FLG"));
                    final AddressRec addressRec = new AddressRec(idx, issuedate, address1, address2, zipCode, isPrintAddr2);
                    addressRecList.add(addressRec);
                    idx += 1;
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return addressRecList;
        }

        /**
         * 住所のSQLを得る
         * @param sqlflg 0:生徒住所, 1:保護者住所, 2:保証人住所
         * @return
         */
        private static String sqlAddressDat(final int sqlflg, final String startDate, final String endDate) {

            StringBuffer stb = new StringBuffer();
            if (AddressRec.SQL_SCHREG == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            } else if (AddressRec.SQL_GUARDIAN == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else if (AddressRec.SQL_GUARANTOR == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARANTOR_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARANTOR_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARANTOR_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARANTOR_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARANTOR_ADDRESS_DAT T1  ");
            }
            stb.append("INNER JOIN (SELECT SCHREGNO, MIN(ENT_DATE) AS ENT_DATE, VALUE(MAX(GRD_DATE), '9999-12-31') AS GRD_DATE  ");
            stb.append("            FROM SCHREG_ENT_GRD_HIST_DAT ");
            stb.append("            WHERE SCHOOL_KIND IN ('" + Param.SCHOOL_KIND_H + "', '" + Param.SCHOOL_KIND_J + "') ");
            stb.append("            GROUP BY SCHREGNO ");
            stb.append("           ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("       T1.SCHREGNO = ?  ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= ?  ");
            if (null != startDate && null != endDate) {
                stb.append("       AND (ISSUEDATE BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR VALUE(EXPIREDATE, '9999-12-31') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '9999-12-31') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ISSUEDATE <= '" + endDate + "') ");
            }
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒異動情報クラス>>。
     */
    private static class TransferRec {

        private static final String NAMECD1_A004 = "A004";
        private static final int A004_NAMECD2_RYUGAKU = 1;
        private static final int A004_NAMECD2_KYUGAKU = 2;

        final String _transfercd;
        final String _name;
        final String _sYear;
        final String _sDate;
        final String _sDateStr;
        private String _eDate;
        private String _eDateStr;
        final String _grade;
        final String _gradeCd;
        final String _reason;
        final String _place;
        final String _address;

        /**
         * コンストラクタ。
         */
        private TransferRec(
                final String transfercd,
                final String name,
                final String sYear,
                final String sDate,
                final String sDateStr,
                final String eDate,
                final String eDateStr,
                final String grade,
                final String gradeCd,
                final String reason,
                final String place,
                final String address
                ) {
            _transfercd = transfercd;
            _name = name;
            _sYear = sYear;
            _sDate = sDate;
            _sDateStr = sDateStr;
            _eDate = eDate;
            _eDateStr = eDateStr;
            _grade = grade;
            _gradeCd = gradeCd;
            _reason = reason;
            _place = place;
            _address = address;
        }

        private static boolean isNextDate(final String date1, final String date2) {
            if (null == date1 || null == date2) {
                return false;
            }
            final Calendar cal1 = Calendar.getInstance();
            cal1.setTime(Date.valueOf(date1));
            final Calendar cal2 = Calendar.getInstance();
            cal2.setTime(Date.valueOf(date2));
            cal1.add(Calendar.DATE, 1);
            return cal1.equals(cal2);
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        private static List<TransferRec> loadTransferRec(final DB2UDB db2, final String schregno, final Param param) {
            final List<TransferRec> transferRecList = new LinkedList<TransferRec>();
            try {
                final String sql = sql_state(schregno, param);
                for (Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                    final String transfercd = KnjDbUtils.getString(row, "TRANSFERCD");
                    final String sDate = KnjDbUtils.getString(row, "SDATE");
                    final String sYear = KnjDbUtils.getString(row, "YEAR");
                    final String sDateStr = setDateFormat(db2, formatDate1(db2, sDate, param), param._year, param);
                    final String eDate = KnjDbUtils.getString(row, "EDATE");

                    if (TransferRec.A004_NAMECD2_RYUGAKU == Integer.parseInt(transfercd)) {
                        TransferRec before = null;
                        for (final Iterator it = transferRecList.iterator(); it.hasNext();) {
                            final TransferRec b = (TransferRec) it.next();
                            if (TransferRec.A004_NAMECD2_RYUGAKU == Integer.parseInt(b._transfercd)) {
                                if (isNextDate(b._eDate, sDate)) {
                                    before = b;
                                    break;
                                }
                            }
                        }
                        if (null != before) {
                            before._eDate = eDate;
                            before._eDateStr = setDateFormat(db2, formatDate1(db2, eDate, param), param._year, param);
                            continue;
                        }
                    }
                    final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME1"));
                    final String eDateStr = setDateFormat(db2, formatDate1(db2, eDate, param), param._year, param);
                    String grade = null;
                    if (StringUtils.isNotBlank(KnjDbUtils.getString(row, "GRADE"))) {
                        grade = String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "GRADE")));
                    }
                    final String gradeCd = !NumberUtils.isDigits(KnjDbUtils.getString(row, "GRADE_CD")) ? null : String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "GRADE_CD")));
                    final String reason = "".equals(KnjDbUtils.getString(row, "REASON")) ? null : KnjDbUtils.getString(row, "REASON");
                    final String place = "".equals(KnjDbUtils.getString(row, "PLACE")) ? null : KnjDbUtils.getString(row, "PLACE");
                    final String address = "".equals(KnjDbUtils.getString(row, "ADDR")) ? null : KnjDbUtils.getString(row, "ADDR");

                    final TransferRec transferRec = new TransferRec(
                            transfercd,
                            name,
                            sYear,
                            sDate,
                            sDateStr,
                            eDate,
                            eDateStr,
                            grade,
                            gradeCd,
                            reason,
                            place,
                            address);
                    transferRecList.add(transferRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return transferRecList;
        }


        private static String sql_state(final String schregno, final Param param) {

            final StringBuffer sql = new StringBuffer();
            sql.append("      SELECT ");
            sql.append("      T1.YEAR,");
            sql.append("      T1.SDATE,");
            sql.append("      T1.EDATE,");
            sql.append("      T1.REASON,");
            sql.append("      T1.PLACE,");
            sql.append("      T1.ADDR,");
            sql.append("      T1.TRANSFERCD,");
            sql.append("      T3.NAME1,");
            sql.append("      CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE, ");
            sql.append("      CASE T2.SCHOOLDIV WHEN '0' THEN T4_2.GRADE_CD ELSE T5_2.GRADE_CD END AS GRADE_CD ");
            sql.append("  FROM ");
            sql.append("      (");
            sql.append("          SELECT ");
            sql.append("              FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            sql.append("              TRANSFER_SDATE AS SDATE,");
            sql.append("              TRANSFER_EDATE AS EDATE,");
            sql.append("              TRANSFERREASON AS REASON,");
            sql.append("              TRANSFERPLACE AS PLACE,");
            sql.append("              TRANSFERADDR AS ADDR,");
            sql.append("              TRANSFERCD ");
            sql.append("          FROM ");
            sql.append("              SCHREG_TRANSFER_DAT ");
            sql.append("          WHERE ");
            sql.append("              SCHREGNO = '" + schregno + "' ");
            sql.append("      )T1 ");
            sql.append("      INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR ");
            sql.append("      INNER JOIN NAME_MST T3 ON T3.NAMECD1 = 'A004' AND T3.NAMECD2 = T1.TRANSFERCD ");
            sql.append("      LEFT JOIN(");
            sql.append("          SELECT '0' AS SCHOOLDIV, YEAR, GRADE FROM V_REGDYEAR_GRADE_DAT WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("      )T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append("      LEFT JOIN(");
            sql.append("          SELECT '1' AS SCHOOLDIV, YEAR, GRADE FROM V_REGDYEAR_UNIT_DAT WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("      )T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
            sql.append("  WHERE ");
            sql.append("      T1.YEAR <= '" + param._year + "' ");
            sql.append("  ORDER BY ");
            sql.append("      NAMECD1,NAMECD2,SDATE");

            return sql.toString();
        }

        public String toString() {
            return "TransferRec(" + _transfercd + ", " + _sDate + ", " + _eDate + ", " + _name + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static abstract class AbstractAttendAndStudy {
        protected final String _year;
        protected final String _annual;
        AbstractAttendAndStudy(final String year, final String annual) {
            _year = year;
            _annual = annual;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class AttendRec extends AbstractAttendAndStudy implements Comparable<AttendRec> {

        private Integer _attend_1; // 授業日数
        private Integer _suspend; //出停
        private Integer _mourning; // 忌引
        private Integer _abroad; // 留学
        private Integer _requirepresent; // 要出席
        private Integer _attend_6; // 欠席
        private Integer _present; // 出席

        /**
         * コンストラクタ。
         */
        private AttendRec(final String year, final String annual, final ResultSet rs) {
            super(year, annual);
            try {
                _attend_1 = (Integer) rs.getObject("ATTEND_1");
                _suspend = (Integer) rs.getObject("SUSPEND");
                _mourning = (Integer) rs.getObject("MOURNING");
                _abroad = (Integer) rs.getObject("ABROAD");
                _requirepresent = (Integer) rs.getObject("REQUIREPRESENT");
                _attend_6 = (Integer) rs.getObject("ATTEND_6");
                _present = (Integer) rs.getObject("PRESENT");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final AttendRec that) {
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        Integer suspendMourning() {
            if (null == _mourning) {
                return _suspend;
            }
            if (null == _suspend) {
                return _mourning;
            }
            return new Integer(_suspend.intValue() + _mourning.intValue());
        }

        /**
         * @param v
         * @return Integer Int がnullでなければ文字列に変換して戻します。
         */
        private static String getString(final Integer v) {
            if (null == v) {
                return null;
            }
            return String.valueOf(v);
        }

        /**
         * 出欠記録クラスを作成し、マップに加えます。
         * @param db2
         */
        private static Map<String, AttendRec> loadAttendRec(final DB2UDB db2, final Student student, final Param param) {
            final Map<String, AttendRec> attendRecMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlAttendRec(param, student));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    attendRecMap.put(year, new AttendRec(year, rs.getString("ANNUAL"), rs));
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return attendRecMap;
        }

        /**
         * @return 出欠の記録のＳＱＬ文を戻します。
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String sqlAttendRec(final Param param, final Student student) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.YEAR, ANNUAL");
            stb.append("       , VALUE(CLASSDAYS,0) AS CLASSDAYS"); // 授業日数
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
                stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
            stb.append("              END AS ATTEND_1"); // 授業日数 - (休学日数) [- 留学日数]
            stb.append("       , VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR"); // 出停・忌引
            stb.append("       , VALUE(SUSPEND,0) AS SUSPEND"); // 出停:2
            stb.append("       , VALUE(MOURNING,0) AS MOURNING"); // 忌引:3
            stb.append("       , VALUE(ABROAD,0) AS ABROAD"); // 留学:4
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append("              ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append("              END AS REQUIREPRESENT"); // 要出席日数:5
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append("              ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append("              END AS ATTEND_6"); // 病欠＋事故欠（届・無）:6
            stb.append("       , VALUE(PRESENT,0) AS PRESENT"); // 出席日数:7
            stb.append("       , VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8"); // 忌引＋出停:8
            stb.append(" FROM(");
            stb.append("      SELECT  YEAR, ANNUAL");
            stb.append("            , SUM(CLASSDAYS) AS CLASSDAYS");
            stb.append("            , SUM(OFFDAYS) AS OFFDAYS");
            stb.append("            , SUM(ABSENT) AS ABSENT");
            stb.append("            , SUM(SUSPEND) AS SUSPEND");
            stb.append("            , SUM(MOURNING) AS MOURNING");
            stb.append("            , SUM(ABROAD) AS ABROAD");
            stb.append("            , SUM(REQUIREPRESENT) AS REQUIREPRESENT");
            stb.append("            , SUM(SICK) AS SICK");
            stb.append("            , SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE");
            stb.append("            , SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE");
            stb.append("            , SUM(PRESENT) AS PRESENT");
            stb.append("       FROM   SCHREG_ATTENDREC_DAT");
            stb.append("       WHERE  SCHREGNO = '" + student._schregno + "' AND YEAR <= '" + param._year + "' ");
//            if ((student.isGakunenSei(param) || param._isGenkyuRyuchi) && param._isHeisetuKou && param._isHigh) {
                stb.append("      AND ANNUAL not in " + param._gradeInChugaku + " ");
//            }
            if ("1".equals(param._seitoSidoYorokuNotPrintAnotherAttendrec)) {
                stb.append("      AND SCHOOLCD <> '1' ");
            }
            stb.append("       GROUP BY YEAR, ANNUAL");
            stb.append("     )T1 ");
            stb.append("     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRec extends AbstractAttendAndStudy {

        private static final String FLAG_STUDYREC = "STUDYREC";
        private static final String FLAG_CHAIR_STD = "CHAIR_STD";

        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subClasscd;
        final String _className;
        final String _subClassName;
        private Integer _grades;
        final Integer _credit;
        final Integer _compCredit;
        final Integer _creditMstCredits;
        final Integer _showorderClass;
        final Integer _showorderSubClass;
        final String _specialDiv;
        final String _studyFlag;

        /**
         * コンストラクタ。
         */
        private StudyRec(final String year, final String annual, final String classcd,
                final String schoolKind, final String curriculumCd, final String subClasscd,
                final String className, final String subClassName, final String specialDiv, final Integer credit,
                final Integer compCredit, final Integer creditMstCredits,
                final Double grades, final Integer showorderClass, final Integer showorderSubClass,
                final String studyFlag) {
            super(year, annual);
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subClasscd = subClasscd;
            _className = className;
            _subClassName = subClassName;
            _credit = credit;
            _compCredit = compCredit;
            _creditMstCredits = creditMstCredits;
            _grades = null == grades ? null : new Integer((int) Math.round(grades.floatValue()));
            _showorderClass = showorderClass;
            _showorderSubClass = showorderSubClass;
            _specialDiv = specialDiv;
            _studyFlag = studyFlag;
        }

        private static class AbroadStudyRec extends StudyRec {
            final String _remark1;
            /**
             * コンストラクタ。留学。
             */
            private AbroadStudyRec(final String year, final String annual, final Integer credit, final String remark1) {
                super(year, annual, "AA",
                        "AA", "AA", "AAAAAA",
                        _ABROAD, _ABROAD, "0", credit,
                        null, null,
                        null, new Integer(0), new Integer(0),
                        FLAG_STUDYREC);
                _remark1 = remark1;
            }
        }

        public String getSubClassName() {
            return _subClassName;
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        public boolean isRisyuNomi(final Param param) {
//            if (param._isKyoto) {
//                // 修得単位数が0 かつ 履修単位数が1以上
//                return 0 == intVal(_credit, -1) && 1 <= intVal(_compCredit, -1);
//            } else {
                // (修得単位数がnullもしくは0) かつ 履修単位数が1以上
                return 0 == intVal(_credit, 0) && 1 <= intVal(_compCredit, -1);
//            }
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        public boolean isMirisyu(final Param param) {
            // 修得単位数が0 かつ 履修単位数が0 かつ 評定がnull
            return 0 == intVal(_credit, -1) && 0 == intVal(_compCredit, -1) && -1 == intVal(_grades, -1);
        }

        private static int intVal(final Number n, final int def) {
            return null == n ? def : n.intValue();
        }

        public static class StudyrecComparator implements Comparator<StudyRec> {
            final Param _param;
            StudyrecComparator(final Param param) {
                _param = param;
            }
            public int compare(final StudyRec s1, final StudyRec that) {
                int rtn;
                rtn = s1._specialDiv.compareTo(that._specialDiv);
                if (0 != rtn)
                    return rtn;
                rtn = s1._showorderClass.compareTo(that._showorderClass);
                if (0 != rtn)
                    return rtn;
                rtn = s1._classcd.compareTo(that._classcd);
                if (0 != rtn)
                    return rtn;
                if (null != s1._schoolKind && null != that._schoolKind) {
                    rtn = s1._schoolKind.compareTo(that._schoolKind);
                    if (0 != rtn)
                        return rtn;
                }
                if (_param._isSubclassOrderNotContainCurriculumcd) {
                } else {
                    if (null != s1._curriculumCd && null != that._curriculumCd) {
                        rtn = s1._curriculumCd.compareTo(that._curriculumCd);
                        if (0 != rtn)
                            return rtn;
                    }
                }
                rtn = s1._showorderSubClass.compareTo(that._showorderSubClass);
                if (0 != rtn)
                    return rtn;
                rtn = s1._subClasscd.compareTo(that._subClasscd);
                if (0 != rtn)
                    return rtn;
                rtn = s1._year.compareTo(that._year);
                return rtn;
            }
        }

        public String toString() {
            return "Studyrec(classcd=" + _classcd + ":subClasscd=" + _subClasscd + ":subClassName=" + _subClassName + ":year=" + _year + ":annual=" + _annual + ":credit=" + _credit + ")";
        }

        private String getKeySubclasscdForSubclassTotal(final Param param) {
            if ("1".equals(param._useCurriculumcd) && param._isSubclassOrderNotContainCurriculumcd) {
                return _classcd + "-" + _schoolKind + "-" + _subClasscd;
            }
            return getKeySubclasscd(param);
        }
        private String getKeySubclasscd(final Param param) {
            if ("1".equals(param._useCurriculumcd)) {
                return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subClasscd;
            }
            return _subClasscd;
        }

        /**
         * 学習記録データクラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<StudyRec> loadStudyRec(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final Param param,
                final StudyrecSubstitutionContainer ssc) {
            // 学年制の場合、留年対応します。
            final List<StudyRec> studyRecList = new LinkedList();
            final TreeMap yearAnnualMap = new TreeMap(); // 在籍データの年度と年次のマップ
            for (final Gakuseki gaku :  gakusekiList) {
                if (null != gaku._annual) {
                    yearAnnualMap.put(gaku._year, gaku._annual);
                }
            }

            studyRecList.addAll(createAbroadStudyrec(db2, student, param, yearAnnualMap));

            studyRecList.addAll(createStudyrec(db2, student, personalInfo, param));

            // 全部/一部代替科目取得
            studyRecList.addAll(createStudyrecSubstitution(db2, student, param, ssc));

            // リストをソートします。
            Collections.sort(studyRecList, new StudyRec.StudyrecComparator(param));
            if (DEBUG) {
                for (final StudyRec sr : studyRecList) {
                    log.debug(" studyrec subclasscd = " + sr.getKeySubclasscd(param) + " " + sr._subClassName);
                }
            }
            return studyRecList;
        }

        private static List<StudyRec> createStudyrecSubstitution(final DB2UDB db2, final Student student, final Param param,
                final StudyrecSubstitutionContainer ssc) {
            final List<StudyRec> studyRecList = new ArrayList();

            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map studyrecSubstitution = ssc.getStudyrecSubstitution(substitutionTypeFlg);

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sqlStudyrecSubstitution = sqlReplaceSubClassSubstitution(param, substitutionTypeFlg, student, param._year);
                    // log.debug(" substitution type = " + substitutionTypeFlg + ", sql = " + sqlStudyrecSubstitution);
                    ps = db2.prepareStatement(sqlStudyrecSubstitution);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final StudyRec replacedStudyRec = createReplacedStudyRec(rs, studyrecSubstitution, param);
                        if (null != replacedStudyRec) {
                            studyRecList.add(replacedStudyRec);
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return studyRecList;
        }

        private static List<StudyRec> createStudyrec(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final Param param) {
            final List<StudyRec> studyRecList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String year;
//            if (param._isTokiwa && student.isTengakuTaigaku(personalInfo)) {
//                year = student.getTengakuTaigakuNendoMinus1(personalInfo);
//            } else {
                year = param._year;
//            }
            final String sql = sqlStudyrec(param, student, personalInfo, year);
            //log.debug(" studyrec sql = " + sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind;
                    final String curriculumCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        schoolKind = rs.getString("SCHOOL_KIND");
                        curriculumCd = rs.getString("CURRICULUM_CD");
                    } else {
                        schoolKind = null;
                        curriculumCd = null;
                    }
                    final StudyRec studyRec = new StudyRec(rs.getString("YEAR"),
                            rs.getString("ANNUAL"),
                            rs.getString("CLASSCD"),
                            schoolKind,
                            curriculumCd,
                            rs.getString("SUBCLASSCD"),
                            rs.getString("CLASSNAME"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("SPECIALDIV"),
                            (Integer) rs.getObject("CREDIT"),
                            (Integer) rs.getObject("COMP_CREDIT"),
                            (Integer) rs.getObject("CREDIT_MST_CREDITS"),
                            (Double) rs.getObject("GRADES"),
                            (Integer) rs.getObject("SHOWORDERCLASS"),
                            (Integer) rs.getObject("SHOWORDERSUBCLASS"),
                            rs.getString("STUDY_FLAG"));
                    if (ANOTHER_YEAR.equals(studyRec._year) && !param._seitoSidoYorokuZaisekiMae) {
                        continue;
                    }
                    studyRecList.add(studyRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return studyRecList;
        }

        private static List<StudyRec.AbroadStudyRec> createAbroadStudyrec(final DB2UDB db2, final Student student, final Param param, final TreeMap yearAnnualMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List<StudyRec.AbroadStudyRec> abroadStudyRecList = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlAbroadCredit(param, student));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("TRANSFER_YEAR");
                    final String annual;
                    if (null != yearAnnualMap.get(year)) {
                        annual = (String) yearAnnualMap.get(year);
                    } else { // 在籍データの範囲外の留学
                        if (param._seitoSidoYorokuZaisekiMae) {
                            final String minYear = yearAnnualMap.isEmpty() || null == yearAnnualMap.firstKey() ? "0000" : (String) yearAnnualMap.firstKey();
                            if (year.compareTo(minYear) < 0) {
                                final int diffyear = Integer.parseInt(minYear) - Integer.parseInt(year);
                                final int minannual = Integer.parseInt((String) yearAnnualMap.get(minYear));
                                annual = new DecimalFormat("00").format(minannual - diffyear); // 年度の差分から在籍前留学時の年次を計算した値
                            } else {
                                annual = null;
                            }
                        } else {
                            annual = null;
                        }
                    }
                    String remark = null;
                    if (param._hasSCHREG_TRANSFER_DAT_REMARK1) {
                        remark = rs.getString("REMARK1");
                    }
                    StudyRec.AbroadStudyRec bef = null;
                    for (final Iterator<StudyRec.AbroadStudyRec> it = abroadStudyRecList.iterator(); it.hasNext();) {
                        final StudyRec.AbroadStudyRec a = it.next();
                        if (a._year.equals(year)) {
                            bef = a;
                            it.remove();
                        }
                    }
                    final Integer credit = (Integer) rs.getObject("CREDIT");
                    final StudyRec.AbroadStudyRec studyRec;
                    if (null != bef) {
                        final Integer newCredit = null == credit ? bef._credit : null == bef._credit ? credit : new Integer(credit.intValue() + bef._credit.intValue());
                        studyRec = new StudyRec.AbroadStudyRec(bef._year, bef._annual, newCredit, bef._remark1);
                    } else {
                        studyRec = new StudyRec.AbroadStudyRec(year, annual, credit, remark);
                    }
                    abroadStudyRecList.add(studyRec);
//                    log.debug(" abroad record = " + studyRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return abroadStudyRecList;
        }

        /** 代替科目作成 */
        private static StudyRec createReplacedStudyRec(final ResultSet rs, final Map studyrecSubstitutionMap, final Param param) {

            try {
                final String year = rs.getString("YEAR");
                final String annual = rs.getString("ANNUAL");
                final String substitutionClasscd = rs.getString("SUBSTITUTION_CLASSCD");           // 代替先科目教科コード
                final String substitutionClassName = rs.getString("SUBSTITUTION_CLASSNAME");       // 代替先科目教科名
                final String substitutionSubClasscd = rs.getString("SUBSTITUTION_SUBCLASSCD");     // 代替先科目コード
                final String substitutionSubClassName = rs.getString("SUBSTITUTION_SUBCLASSNAME"); // 代替先科目名
                final Integer credit = null;
                final Double grades = null;
                final Integer showorderClass = Integer.valueOf(rs.getString("SHOWORDERCLASS"));
                final Integer showorderSubClass = Integer.valueOf(rs.getString("SHOWORDERSUBCLASS"));
                final String specialDiv = rs.getString("SPECIALDIV");
                final String substitutionSchoolKind;
                final String substitutionCurriculumCd;
                final String mapKey;
                if ("1".equals(param._useCurriculumcd)) {
                    substitutionSchoolKind = rs.getString("SUBSTITUTION_SCHOOL_KIND");
                    substitutionCurriculumCd = rs.getString("SUBSTITUTION_CURRICULUM_CD");
                    mapKey = substitutionClasscd + "-" + substitutionSchoolKind + "-" + substitutionCurriculumCd + "-" + substitutionSubClasscd;
                } else {
                    substitutionSchoolKind = null;
                    substitutionCurriculumCd = null;
                    mapKey = substitutionSubClasscd;
                }

                final StudyRec replacedStudyRec = new StudyRec(
                        year, annual, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubClasscd,
                        substitutionClassName, substitutionSubClassName, specialDiv, credit,
                        null, null, grades, showorderClass, showorderSubClass, StudyRec.FLAG_STUDYREC);

                final String attendSubclassCd = rs.getString("ATTEND_SUBCLASSCD"); // 代替元科目コード
                final String attendClassCd;
                final String attendSchoolKind;
                final String attendCurriculumCd;
                if ("1".equals(param._useCurriculumcd)) {
                    attendClassCd = rs.getString("ATTEND_CLASSCD");
                    attendSchoolKind = rs.getString("ATTEND_SCHOOL_KIND");
                    attendCurriculumCd = rs.getString("ATTEND_CURRICULUM_CD");
                } else {
                    attendClassCd = null;
                    attendSchoolKind = null;
                    attendCurriculumCd = null;
                }
                final String attendClassName = rs.getString("ATTEND_CLASSNAME"); // 代替元教科名称
                final String attendSubclassName = rs.getString("ATTEND_SUBCLASSNAME"); // 代替元科目名称
                final Integer attendCredit = (Integer) rs.getObject("ATTEND_CREDIT"); // 代替元科目単位
                final Integer attendCompCredit = (Integer) rs.getObject("ATTEND_COMP_CREDIT"); // 代替元科目履修単位
                final Integer attendGrades = (Integer) rs.getObject("ATTEND_GRADES"); // 代替元科目評定
                final String substitutionCredit = rs.getString("SUBSTITUTION_CREDIT") == null ? " " : rs.getString("SUBSTITUTION_CREDIT"); // 代替先単位

                if (null == studyrecSubstitutionMap.get(mapKey)) { // すでに同一の代替元科目がある場合
                    final StudyRecSubstitution studyRecSubstitution = new StudyRecSubstitution(
                            null, annual, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubClasscd,
                            substitutionClassName, substitutionSubClassName, specialDiv,
                            credit, grades, showorderClass, showorderSubClass);
                    studyrecSubstitutionMap.put(mapKey, studyRecSubstitution);
                }
                final StudyRecSubstitution studyRecSubstitution = (StudyRecSubstitution) studyrecSubstitutionMap.get(mapKey);
                studyRecSubstitution.attendSubclasses.add(new StudyRecSubstitution.SubstitutionAttendSubclass(year, attendClassCd, attendSchoolKind, attendCurriculumCd, attendSubclassCd, attendClassName, attendSubclassName, attendCredit, attendCompCredit, attendGrades, substitutionCredit));
                return replacedStudyRec;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private static String pre_sql_Common(final Param param, final Student student) {
//            log.debug("String pre_sql_Common() ");

            // 教科コードが90より大きい対象
            String classcd90Over = "";
            // 履修未履修をチェックしない
            String noCheckRishuMirishu = "";
//            if (param._isTokiwa) {
//                classcd90Over += " OR T1.CLASSCD = '" + _94 + "' ";
//                noCheckRishuMirishu += "   T1.CLASSCD = '" + _94 + "' OR ";
//            }

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        ,T1.SCHOOL_KIND ");
                stb.append("        ,T1.CURRICULUM_CD ");
            }
            stb.append("        ,T1.SUBCLASSCD, VALUATION AS GRADES ");
            stb.append("        ,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("        ,VALUE(T1.COMP_CREDIT, 0) AS COMP_CREDIT ");
            stb.append("        ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
            stb.append("        ,T1.SCHOOLCD ");
            stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
            if ("1".equals(param._useProvFlg)) {
                stb.append("         LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND L3.YEAR = T1.YEAR ");
                stb.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND L3.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND L3.PROV_FLG = '1' ");
            }
            stb.append(" WHERE   EXISTS(SELECT 'X' FROM SCHBASE T2 WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR <= T2.YEAR) ");
            stb.append("     AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' " + classcd90Over + ") ");
            if ("1".equals(param._seitoSidoYorokuNotPrintAnotherStudyrec)) {
                stb.append("      AND T1.SCHOOLCD <> '1' ");
            }
//            if ((student.isGakunenSei(param) || param._isGenkyuRyuchi) && param._isHeisetuKou && param._isHigh) {
                stb.append("      AND T1.ANNUAL not in " + param._gradeInChugaku + " ");
//            }
            if (!param._isPrintMirisyu) {
                stb.append("     AND ( ");
                stb.append(noCheckRishuMirishu);
                stb.append("         NOT (T1.COMP_CREDIT IS NOT NULL ");
                stb.append("               AND T1.COMP_CREDIT = 0 ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NOT NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END = 0) ");
                stb.append("    ) ");
            }
            if (!param._isPrintRisyuNomi) {
                stb.append("      AND (");
                stb.append(noCheckRishuMirishu);
                stb.append("         NOT (T1.COMP_CREDIT IS NOT NULL ");
                stb.append("               AND T1.COMP_CREDIT <> 0 ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NOT NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END = 0) ");
                stb.append("    ) ");
            }
            if (!param._isPrintRisyuTourokuNomi) {
                stb.append("      AND (");
                stb.append(noCheckRishuMirishu);
                stb.append("         NOT (T1.COMP_CREDIT IS NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NULL) ");
                stb.append("    ) ");
            }
            if ("1".equals(param._useProvFlg)) {
                stb.append("         AND L3.SUBCLASSCD IS NULL ");
            }
            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private static String sqlReplaceSubClassSubstitution(final Param param, final String substitutionTypeFlg, final Student student, final String year) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST('" + student._schregno + "' AS VARCHAR(8)), CAST('" + year + "' AS VARCHAR(4))))");
            stb.append(" , DATA AS(");
            stb.append(pre_sql_Common(param, student));
            stb.append(" )");
            stb.append(" ,MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND");
                stb.append("     ,T1.CURRICULUM_CD");
            }
            stb.append("       ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD");
            stb.append("       ,T1.GRADES,T1.CREDIT,T1.COMP_CREDIT,T1.CLASSNAME,T1.CLASSNAME_ENG,T1.SUBCLASSNAME,T1.SUBCLASSNAME_ENG");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND ");
                stb.append("     ,T1.CURRICULUM_CD ");
            }
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       ,SUM(T1.CREDIT) AS CREDIT");
            stb.append("       ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
            stb.append("       ,MAX(T1.GRADES) AS GRADES");
            stb.append(" FROM DATA2 T1");
            stb.append(" INNER JOIN MAX_SEMESTER T2 ON T1.SCHREGNO = T2.SCHREGNO");
            stb.append("       AND T1.YEAR = T2.YEAR");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND ");
                stb.append("     ,T1.CURRICULUM_CD ");
            }
            stb.append(" )");
            stb.append(" SELECT  T1.YEAR, T1.ANNUAL ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T4.SUBSTITUTION_CLASSCD");
                stb.append("       , T4.SUBSTITUTION_SCHOOL_KIND");
                stb.append("       , T4.SUBSTITUTION_CURRICULUM_CD");
            } else {
                stb.append("       , SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) AS SUBSTITUTION_CLASSCD");
            }
            stb.append("       , T4.SUBSTITUTION_SUBCLASSCD ");
            stb.append("       , T2.CLASSNAME AS SUBSTITUTION_CLASSNAME");
            stb.append("       , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBSTITUTION_SUBCLASSNAME");
            stb.append("       , T7.CREDITS AS SUBSTITUTION_CREDIT");
            stb.append("       , VALUE(T2.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
            stb.append("       , VALUE(T3.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
            stb.append("       , T1.SUBCLASSCD AS ATTEND_SUBCLASSCD"); // 代替元科目(表示する行のグループコード科目)
//          stb.append("       , T4.ATTEND_SUBCLASSCD"); // 代替元科目
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T4.ATTEND_CLASSCD "); // 代替元科目
                stb.append("       , T4.ATTEND_SCHOOL_KIND "); // 代替元科目
                stb.append("       , T4.ATTEND_CURRICULUM_CD "); // 代替元科目
            }
            stb.append("       , T4.ATTEND_SUBCLASSCD AS SRC_ATTEND_SUBCLASSCD"); // 代替元科目
            stb.append("       , T1.CREDIT AS ATTEND_CREDIT"); // 代替元科目修得単位
            stb.append("       , T1.COMP_CREDIT AS ATTEND_COMP_CREDIT"); // 代替元科目履修単位
            stb.append("       , T1.GRADES AS ATTEND_GRADES"); // 代替元科目評定
            stb.append("       , T6.CLASSNAME AS ATTEND_CLASSNAME");
            stb.append("       , VALUE(T5.SUBCLASSORDERNAME1, T5.SUBCLASSNAME) AS ATTEND_SUBCLASSNAME"); // 代替元科目
            stb.append("       , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_DAT T4 ON T1.YEAR = T4.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("        AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T4.ATTEND_SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ");
            stb.append("        AND T4.SUBSTITUTION_TYPE_FLG = '" + substitutionTypeFlg + "' ");
            stb.append(" INNER JOIN MAX_SEMESTER SEM ON SEM.YEAR = T1.YEAR AND SEM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T5 ON ");
            stb.append("        T5.YEAR = T4.YEAR AND T5.SUBSTITUTION_SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T5.SUBSTITUTION_CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.append("        AND T5.SUBSTITUTION_SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.append("        AND T5.SUBSTITUTION_CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.append("        AND T5.ATTEND_SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T5.ATTEND_CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("        AND T5.ATTEND_SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.append("        AND T5.ATTEND_CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            }
            stb.append("        AND T5.MAJORCD = REGD.MAJORCD AND T5.COURSECD = REGD.COURSECD ");
            stb.append("        AND T5.GRADE = REGD.GRADE AND T5.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CREDIT_MST T7 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           T7.CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("           AND T7.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.append("           AND T7.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            } else {
                stb.append("           T7.CLASSCD = SUBSTR(T4.ATTEND_SUBCLASSCD, 1, 2) ");
            }
            stb.append("       AND T7.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            stb.append("       AND T7.YEAR = REGD.YEAR ");
            stb.append("       AND T7.GRADE = REGD.GRADE ");
            stb.append("       AND T7.COURSECD = REGD.COURSECD ");
            stb.append("       AND T7.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND T7.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T2.CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.append("       AND T2.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
            } else {
                stb.append("       T2.CLASSCD = SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) ");
            }
            stb.append(" LEFT JOIN CLASS_MST T6 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T6.CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("       AND T6.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
            } else {
                stb.append("       T6.CLASSCD = SUBSTR(T4.ATTEND_SUBCLASSCD, 1, 2) ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T3.CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.append("       AND T3.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.append("       AND T3.CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T5 ON T5.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T5.CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("       AND T5.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.append("       AND T5.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" WHERE  T4.SUBSTITUTION_CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append("     OR T4.SUBSTITUTION_CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            } else {
                stb.append(" WHERE  SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append("     OR SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "'");
            }

            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private static String sqlStudyrec(final Param param, final Student student, final PersonalInfo personalInfo, final String year) {
            // 同一年度同一科目の場合単位は合計とします。
            //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
            String gradesCase = "case when 0 < T1.GRADES then GRADES end";
            String creditCase = "case when 0 < T1.GRADES then CREDIT end";
            final boolean isPrintChairStd = false; // param._isTokiwa && student.isTengakuTaigaku(personalInfo)

            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST('" + student._schregno + "' AS VARCHAR(8)), CAST('" + year + "' AS VARCHAR(4))))");
            stb.append(" ,MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" , DATA AS(");
            stb.append(pre_sql_Common(param, student));
            stb.append(" )");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SCHOOLCD,T1.GRADES,T1.CREDIT,T1.COMP_CREDIT,T1.CLASSNAME,T1.CLASSNAME_ENG,T1.SUBCLASSNAME,T1.SUBCLASSNAME_ENG");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       ,T1.SUBCLASSCD");
            stb.append("       ,case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
            stb.append("            when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
            stb.append("            when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
            stb.append("            else MAX(T1.GRADES) end AS GRADES");
            stb.append("       ,SUM(T1.CREDIT) AS CREDIT");
            stb.append("       ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.CLASSNAME_ENG) AS CLASSNAME_ENG");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME_ENG) AS SUBCLASSNAME_ENG");
            stb.append("       ,MIN(T1.SCHOOLCD) AS SCHOOLCD");
            stb.append(" FROM DATA2 T1 ");
            stb.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("          ,T1.SUBCLASSCD,SC.GVAL_CALC ");
            stb.append(" )");
            if (isPrintChairStd) {
                stb.append(" , CHAIR_STD AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T3.ANNUAL, ");
                stb.append("         T2.CLASSCD, ");
                stb.append("         T2.SCHOOL_KIND, ");
                stb.append("         T2.CURRICULUM_CD, ");
                stb.append("         T2.SUBCLASSCD ");
                stb.append("     FROM CHAIR_STD_DAT T1 ");
                stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T3.YEAR = T1.YEAR ");
                stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SCHREGNO = '" + student._schregno + "' ");
                stb.append(" ) ");
                stb.append(" , MAX_SEMESTER_THIS_YEAR AS ( ");
                stb.append("     SELECT ");
                stb.append("         SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                stb.append("     FROM CHAIR_STD ");
                stb.append("     GROUP BY ");
                stb.append("         SCHREGNO, YEAR ");
                stb.append(" ) ");
                stb.append(" , CREDIT_MST_CREDITS AS ( ");
                stb.append("     SELECT DISTINCT ");
                stb.append("         T1.YEAR, T1.SCHREGNO, T2.ANNUAL, ");
                stb.append("         T1.CLASSCD, ");
                stb.append("         T1.SCHOOL_KIND, ");
                stb.append("         T1.CURRICULUM_CD, ");
                stb.append("         T1.SUBCLASSCD, ");
                stb.append("         T3.CREDITS ");
                stb.append("     FROM CHAIR_STD T1 ");
                stb.append("     INNER JOIN MAX_SEMESTER_THIS_YEAR SEM ON SEM.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND SEM.YEAR = T1.YEAR ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.SEMESTER = SEM.SEMESTER ");
                stb.append("     LEFT JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
                stb.append("         AND T3.COURSECD = T2.COURSECD ");
                stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
                stb.append("         AND T3.GRADE = T2.GRADE ");
                stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
                stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" ) ");
                stb.append(" , CHAIR_STD_COMBINED AS ( ");
                stb.append("     SELECT ");
                stb.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
                stb.append("            T1.CLASSCD, ");
                stb.append("            T1.SCHOOL_KIND, ");
                stb.append("            T1.CURRICULUM_CD, ");
                stb.append("            T1.SUBCLASSCD, ");
                stb.append("            T5.CREDITS ");
                stb.append("     FROM CHAIR_STD T1 ");
                stb.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("         AND T3.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = T1.YEAR ");
                stb.append("         AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T4.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
                stb.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     WHERE ");
                stb.append("         T3.COMBINED_SUBCLASSCD IS NULL ");
                stb.append("         AND T4.ATTEND_SUBCLASSCD IS NULL ");
                stb.append("     UNION ");
                stb.append("     SELECT ");
                stb.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
                stb.append("            T3.COMBINED_CLASSCD AS CLASSCD, ");
                stb.append("            T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
                stb.append("            T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
                stb.append("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("            CASE WHEN '2' = MAX(T3.CALCULATE_CREDIT_FLG) THEN SUM(T5.CREDITS) ");
                stb.append("                 ELSE MAX(T6.CREDITS) ");
                stb.append("            END AS CREDITS ");
                stb.append("     FROM CHAIR_STD T1 ");
                stb.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("         AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
                stb.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T5.CLASSCD = T3.ATTEND_CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ");
                stb.append("         AND T5.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ");
                stb.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
                stb.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T6.CLASSCD = T3.COMBINED_CLASSCD ");
                stb.append("         AND T6.SCHOOL_KIND = T3.COMBINED_SCHOOL_KIND ");
                stb.append("         AND T6.CURRICULUM_CD = T3.COMBINED_CURRICULUM_CD ");
                stb.append("         AND T6.SUBCLASSCD = T3.COMBINED_SUBCLASSCD ");
                stb.append("     GROUP BY ");
                stb.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
                stb.append("            T3.COMBINED_CLASSCD, ");
                stb.append("            T3.COMBINED_SCHOOL_KIND, ");
                stb.append("            T3.COMBINED_CURRICULUM_CD, ");
                stb.append("            T3.COMBINED_SUBCLASSCD ");
                stb.append(" ) ");
                stb.append(" , CHAIR_STD_SUBCLASSCD2 AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
                stb.append("         T1.CLASSCD, ");
                stb.append("         T1.SCHOOL_KIND, ");
                stb.append("         T1.CURRICULUM_CD, ");
                stb.append("         T1.SUBCLASSCD, ");
                stb.append("         T1.CREDITS ");
                stb.append("     FROM CHAIR_STD_COMBINED T1 ");
                stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND T2.SUBCLASSCD2 IS NULL ");
                stb.append("     UNION ");
                stb.append("     SELECT ");
                stb.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
                stb.append("         T1.CLASSCD, ");
                stb.append("         T1.SCHOOL_KIND, ");
                stb.append("         T1.CURRICULUM_CD, ");
                stb.append("         T2.SUBCLASSCD2 AS SUBCLASSCD, ");
                stb.append("         T6.CREDITS ");
                stb.append("     FROM CHAIR_STD_COMBINED T1 ");
                stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND T2.SUBCLASSCD2 IS NOT NULL ");
                stb.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
                stb.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND T6.SUBCLASSCD = T2.SUBCLASSCD2 ");
                stb.append(" ) ");
                stb.append(" , CHAIR_STD_SUBCLASS AS (");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, T1.SCHREGNO, T1.ANNUAL ");
                stb.append("   , T1.CLASSCD ");
                stb.append("   , T1.SCHOOL_KIND ");
                stb.append("   , T1.CURRICULUM_CD ");
                stb.append("   , T1.SUBCLASSCD ");
                stb.append("   , T1.CREDITS");
                stb.append("   , T2.CLASSNAME ");
                stb.append("   , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME ");
                stb.append("   , VALUE(T2.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
                stb.append("   , VALUE(T3.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
                stb.append("   , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
                stb.append(" FROM CHAIR_STD_SUBCLASSCD2 T1 ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" ) ");
            }
            stb.append(" SELECT  '" + StudyRec.FLAG_STUDYREC + "' AS STUDY_FLAG ");
            stb.append("       , T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       , T1.SUBCLASSCD");
            stb.append("       , VALUE(");
            if (param._hasANOTHER_CLASS_MST) {
                stb.append("               CASE WHEN T1.SCHOOLCD = '1' THEN ANT2.CLASSNAME END, ");
            }
            stb.append("               T1.CLASSNAME, T2.CLASSNAME) AS CLASSNAME");
            stb.append("       , VALUE(");
            if (param._hasANOTHER_SUBCLASS_MST) {
                stb.append("               CASE WHEN T1.SCHOOLCD = '1' THEN ANT3.SUBCLASSORDERNAME1 END, ");
                stb.append("               CASE WHEN T1.SCHOOLCD = '1' THEN ANT3.SUBCLASSNAME END, ");
            }
            stb.append("               T1.SUBCLASSNAME, ");
            stb.append("               T3.SUBCLASSORDERNAME1, ");
            stb.append("               T3.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       , T1.CREDIT");
            stb.append("       , T1.COMP_CREDIT");
            stb.append("       , T7.CREDITS AS CREDIT_MST_CREDITS");
            stb.append("       , T1.GRADES");
            stb.append("       , T1.SCHOOLCD");
            stb.append("       , VALUE(T2.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
            stb.append("       , VALUE(T3.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
            stb.append("       , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" LEFT JOIN MAX_SEMESTER SEM ON SEM.YEAR = T1.YEAR AND SEM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN CREDIT_MST T7 ON T7.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           AND T7.CLASSCD = T1.CLASSCD ");
                stb.append("           AND T7.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("           AND T7.CURRICULUM_CD = T1.CURRICULUM_CD ");
            } else {
                stb.append("           AND T7.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("       AND T7.YEAR = REGD.YEAR ");
            stb.append("       AND T7.GRADE = REGD.GRADE ");
            stb.append("       AND T7.COURSECD = REGD.COURSECD ");
            stb.append("       AND T7.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND T7.COURSECODE = REGD.COURSECODE ");
            if (param._hasANOTHER_CLASS_MST) {
                stb.append(" LEFT JOIN ANOTHER_CLASS_MST ANT2 ON ANT2.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND ANT2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
            }
            if (param._hasANOTHER_SUBCLASS_MST) {
                stb.append(" LEFT JOIN ANOTHER_SUBCLASS_MST ANT3 ON ANT3.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND ANT3.CLASSCD = T1.CLASSCD ");
                    stb.append("       AND ANT3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("       AND ANT3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }
            if (isPrintChairStd) {
                stb.append(" UNION ALL ");
                stb.append(" SELECT  '" + StudyRec.FLAG_CHAIR_STD + "' AS STUDY_FLAG ");
                stb.append("       , T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       ,T1.SCHOOL_KIND");
                    stb.append("       ,T1.CURRICULUM_CD");
                }
                stb.append("       , T1.SUBCLASSCD");
                stb.append("       , T1.CLASSNAME");
                stb.append("       , T1.SUBCLASSNAME");
                stb.append("       , CAST(NULL AS SMALLINT) AS CREDIT");
                stb.append("       , CAST(NULL AS SMALLINT) AS COMP_CREDIT");
                stb.append("       , T1.CREDITS AS CREDIT_MST_CREDITS");
                stb.append("       , CAST(NULL AS SMALLINT) AS GRADES");
                stb.append("       , CAST(NULL AS VARCHAR(1)) AS SCHOOLCD");
                stb.append("       , T1.SHOWORDERCLASS");
                stb.append("       , T1.SHOWORDERSUBCLASS");
                stb.append("       , T1.SPECIALDIV");
                stb.append(" FROM   CHAIR_STD_SUBCLASS T1 ");
                stb.append(" WHERE  T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append("     OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' OR T1.CLASSCD = '94' ");
            }
            return stb.toString();
        }

        /**
         * @return 留学単位のＳＱＬ文を戻します。
         * @see 年度別の単位。(留年の仕様に対応)
         */
        private static String sqlAbroadCredit(final Param param, final Student student) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH TRANSFER AS (");
            stb.append("   SELECT W1.SCHREGNO, FISCALYEAR(W1.TRANSFER_SDATE) AS TRANSFER_YEAR, W1.TRANSFER_SDATE, W1.ABROAD_CREDITS ");
            if (param._hasSCHREG_TRANSFER_DAT_REMARK1) {
                stb.append(" , W1.REMARK1 ");
            }
            stb.append("   FROM SCHREG_TRANSFER_DAT W1 ");
            stb.append("   WHERE ");
            stb.append("     W1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("     AND W1.TRANSFERCD = '1' ");
            stb.append(" ) ");
            stb.append("   SELECT W1.SCHREGNO ");
            stb.append("        , W1.TRANSFER_YEAR ");
            stb.append("        , W1.TRANSFER_SDATE ");
            stb.append("        , W1.ABROAD_CREDITS AS CREDIT ");
            if (param._hasSCHREG_TRANSFER_DAT_REMARK1) {
                stb.append(" , W1.REMARK1 ");
            }
            stb.append("     , CASE WHEN ST2.YEAR IS NOT NULL THEN '1' ELSE '0' END AS SCHOOLCD ");
            stb.append("   FROM TRANSFER W1 ");
            if (param._seitoSidoYorokuZaisekiMae) {
                stb.append("   LEFT JOIN ");
            } else {
                stb.append("   INNER JOIN ");
            }
            stb.append("      (SELECT  W1.YEAR ");
            stb.append("            , W1.SCHREGNO ");
            stb.append("      FROM    SCHREG_REGD_DAT W1 ");
            stb.append("      WHERE W1.YEAR <= '" + param._year + "' ");
//            if ((student.isGakunenSei(param)  || param._isGenkyuRyuchi) && param._isHeisetuKou && param._isHigh) {
                stb.append("      AND W1.GRADE not in " + param._gradeInChugaku + " ");
//            }
            stb.append("      GROUP BY W1.YEAR, W1.SCHREGNO ");
            stb.append("   ) ST2 ON ST2.SCHREGNO = W1.SCHREGNO ");
            stb.append("        AND ST2.YEAR = W1.TRANSFER_YEAR ");
            stb.append("ORDER BY W1.TRANSFER_SDATE ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データ科目別単位数のクラス>>。 学習記録データクラスを科目別に集計しました。
     */
    private static class StudyRecSubclassTotal {

        private static class Comparator implements java.util.Comparator<StudyRecSubclassTotal> {
            private final Param _param;
            Comparator(final Param param) {
                _param = param;
            }
            /**
             * {@inheritDoc}
             */
            public int compare(final StudyRecSubclassTotal t1, final StudyRecSubclassTotal t2) {
                int rtn;
                rtn = t1.specialDiv().compareTo(t2.specialDiv());
                if (0 != rtn) { return rtn; }
                rtn = t1._showorderClass.compareTo(t2._showorderClass);
                if (0 != rtn) { return rtn; }
                rtn = t1.classcd().compareTo(t2.classcd());
                if (0 != rtn) { return rtn; }
                if (null != t1.schoolKind() && null != t2.schoolKind()) {
                    rtn = t1.schoolKind().compareTo(t2.schoolKind());
                    if (0 != rtn) { return rtn; }
                }
                if (_param._isSubclassOrderNotContainCurriculumcd) {
                } else {
                    if (null != t1.curriculumCd() && null != t2.curriculumCd()) {
                        rtn = t1.curriculumCd().compareTo(t2.curriculumCd());
                        if (0 != rtn) { return rtn; }
                    }
                }
                rtn = t1._showorderSubClass.compareTo(t2._showorderSubClass);
                if (0 != rtn) { return rtn; }
                rtn = t1.subClasscd().compareTo(t2.subClasscd());
                return rtn;
            }
        }

        final ArrayList<StudyRec> _subclassStudyrecList;
        final Collection _dropYears;
        final Integer _showorderClass;
        final Integer _showorderSubClass;

        /**
         * コンストラクタ。
         *
         * @param rs
         */
        private StudyRecSubclassTotal(final ArrayList subclassStudyrecList, final Collection dropYears) {
            _subclassStudyrecList = subclassStudyrecList;
            _dropYears = dropYears;
            _showorderClass = studyrec()._showorderClass;
            _showorderSubClass = studyrec()._showorderSubClass;
        }

        public Integer credit() {
            boolean bCredits = false;
            int credits = 0;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!_dropYears.contains(sr._year)) {
                    if (null != sr._credit) {
                        bCredits = true;
                        credits += sr._credit.intValue();
                    }
                }
            }
            if (bCredits) {
                return new Integer(credits);
            }
            return null;
        }

        public Integer compCredit() {
            boolean bCompCredits = false;
            int compCredits = 0;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!_dropYears.contains(sr._year)) {
                    if (null != sr._compCredit) {
                        bCompCredits = true;
                        compCredits += sr._compCredit.intValue();
                    }
                }
            }
            if (bCompCredits) {
                return new Integer(compCredits);
            }
            return null;
        }

        public Integer creditMstCredit() {
            boolean bCreditMstCredits = false;
            int creditMstCredits = 0;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!_dropYears.contains(sr._year)) {
                    if (null != sr._creditMstCredits) {
                        bCreditMstCredits = true;
                        creditMstCredits += sr._creditMstCredits.intValue();
                    }
                }
            }
            if (bCreditMstCredits) {
                return new Integer(creditMstCredits);
            }
            return null;
        }

        private List creditList() {
            final List rtn = new ArrayList();
            for (final StudyRec sr : _subclassStudyrecList) {
                final String dropped = _dropYears.contains(sr._year) ? ",dropped" : "";
                rtn.add("[" + sr._year + "," + sr._credit + "" + dropped + "]");
            }
            return rtn;
        }

        private List compCreditList() {
            final List rtn = new ArrayList();
            for (final StudyRec sr : _subclassStudyrecList) {
                final String dropped = _dropYears.contains(sr._year) ? ",dropped" : "";
                rtn.add("[" + sr._year + "," + sr._compCredit + "" + dropped + "]");
            }
            return rtn;
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        private boolean isRisyuNomi(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!sr.isRisyuNomi(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        private boolean isMirisyu(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!sr.isMirisyu(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        private String creditInfo() {
            final List creditList = creditList();
            final List compCreditList = compCreditList();
            final String compCreditInfo = " " + (compCredit() != null ? (" compcredit = " + compCreditList.toString()) : "");
            return (credit() != null && credit().intValue() == 0) ? compCreditInfo : (" credit = " + creditList.toString());
        }

        private StudyRec studyrec() {
            return _subclassStudyrecList.get(_subclassStudyrecList.size() - 1);
        }

        public String specialDiv() {
            return studyrec()._specialDiv;
        }

        public String classcd() {
            return studyrec()._classcd;
        }

        public String schoolKind() {
            return studyrec()._schoolKind;
        }

        public String curriculumCd() {
            return studyrec()._curriculumCd;
        }

        public String className() {
            return studyrec()._className;
        }

        public String subClassName() {
            return studyrec()._subClassName;
        }

        public String subClasscd() {
            return studyrec()._subClasscd;
        }

        public boolean isAllDropped() {
            boolean isAllDropped = true;
            for (final StudyRec studyRec : _subclassStudyrecList) {
                if (!_dropYears.contains(studyRec._year)) {
                    isAllDropped = false;
                }
            }
            return isAllDropped;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "[" + classcd() + ":" + subClasscd() + " " + creditInfo() + " (" + className() + ":" + subClassName() + ") ]";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <pre>
     * 学習記録データ年度別単位数のクラス
     *  学習記録データクラスを年度別に集計します。
     *  メンバ変数について
     *    _year 年度
     *    _subject90：総合的な学習の時間
     *    _abroad：留学
     *    _subject：教科(総合的な学習の時間を含めた)
     *    _total：総合計（教科＋留学）
     * </pre>
     */
    private static class StudyRecYearTotal {

        private final static byte GET = 0;
        private final static byte DROP = 1;
        private final static byte DROP_SHOW = 2;

        private final String _year;
        private final byte _isDrop;

        final List<StudyRec> _studyRecList = new ArrayList();

        /**
         * コンストラクタ。
         */
        private StudyRecYearTotal(final String year, final byte isDrop) {
            _year = year;
            _isDrop = isDrop;
        }

        /**
         * 総合的な学習の時間の単位のリスト
         * @param flg 1,2以外:修得単位 1:履修単位 2:単位マスタ単位
         * @return 総合的な学習の時間の単位のリスト
         */
        private List subject90(int flg) {
            final List list = new ArrayList();
            for (final StudyRec sr : _studyRecList) {
                final Integer credit = 1 == flg ? sr._compCredit : 2 == flg ? sr._creditMstCredits : sr._credit;
                if (null == credit) {
                    continue;
                }
                if (_ABROAD.equals(sr._className)) {
                    continue;
                }
                if (_90.equals(sr._classcd)) {
                    list.add(credit);
                }
            }
            return list;
        }

        public List subject94(final int flg, final Param param) {
            final List list = new ArrayList();
            for (final StudyRec sr : _studyRecList) {
                final Integer credit = 1 == flg ? sr._compCredit : 2 == flg ? sr._creditMstCredits : sr._credit;
                if (null == credit) {
                    continue;
                }
                if (_ABROAD.equals(sr._className)) {
                    continue;
                }
//                if (param._isTokiwa) {
//                    if (_94.equals(sr._classcd)) {
//                        list.add(credit);
//                    }
//                }
            }
            return list;
        }

        public List subject(final int flg, final Param param, final int yoshiki) {
            final List list = new ArrayList();
            for (final StudyRec sr : _studyRecList) {
                final Integer credit = 1 == flg ? sr._compCredit : 2 == flg ? sr._creditMstCredits : sr._credit;
                if (null == credit) {
                    continue;
                }
                if (_ABROAD.equals(sr._className)) {
                    continue;
                }
                if (yoshiki == 2) {
                    if (_90.equals(sr._classcd)) {
                        continue;
                    }
                }

//                if (param._isTokiwa) {
//                    if (!_94.equals(sr._classcd)) {
//                        list.add(credit);
//                    }
//                } else {
                    list.add(credit);
//                }
            }
            return list;
        }

        // flgなし（留学は履修単位無し、単位マスタの単位無し）
        public List abroad() {
            final List list = new ArrayList();
            for (final StudyRec sr : _studyRecList) {
                final Integer credit = sr._credit;
                if (null == credit) {
                    continue;
                }
                if (_ABROAD.equals(sr._className)) {
                    list.add(credit);
                }
            }
            return list;
        }

        public List total(final int flg) {
            final List list = new ArrayList();
            for (final StudyRec sr : _studyRecList) {
                final Integer credit = 1 == flg ? sr._compCredit : 2 == flg ? sr._creditMstCredits : sr._credit;
                if (null == credit) {
                    continue;
                }
                list.add(credit);
            }
            return list;
        }
    }


    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRecSubstitution extends StudyRec {

        private List<StudyRecSubstitution.SubstitutionAttendSubclass> attendSubclasses = new ArrayList();

        /**
         * コンストラクタ。
         */
        private StudyRecSubstitution(final String year, final String annual, final String classcd,
                final String schoolKind, final String curriculumCd, final String subClasscd,
                final String className, final String subClassName, final String specialDiv, final Integer credit,
                final Double grades, final Integer showorderClass, final Integer showorderSubClass) {
            super(year, annual, classcd, schoolKind, curriculumCd, subClasscd, className, subClassName, specialDiv, credit,
                    null, null, grades, showorderClass, showorderSubClass, StudyRec.FLAG_STUDYREC);
        }

        private String daitaiName(final String substitutionTypeFlg) {
            return StudyrecSubstitutionContainer.ICHIBU.equals(substitutionTypeFlg) ? "一部代替" : "代替";
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考データを得る。
         * @return
         */
        private Object[] getBikoSubstitutionInfo(final Param param) {
            final Set addedKey = new HashSet();

            String totalSubCredit = "";
            final StringBuffer attendSubClassNames = new StringBuffer();
            String sp = "";

            for (final SubstitutionAttendSubclass attendSubclasss : attendSubclasses) {

                final String keyCd;
                if ("1".equals(param._useCurriculumcd)) {
                    keyCd = attendSubclasss._attendClassCd + "-" + attendSubclasss._attendSchoolKind + "-" + attendSubclasss._attendCurriculumCd + "-" + attendSubclasss._attendSubClassCd;
                } else {
                    keyCd = attendSubclasss._attendSubClassCd;
                }
                final String attendSubClassName = attendSubclasss._attendSubclassName;
                final String subCredit = attendSubclasss._substitutionCredit;

                if (!addedKey.contains(keyCd)) {
                    attendSubClassNames.append(sp + attendSubClassName);
                    sp = "、";
                    addedKey.add(keyCd);
                }

                if (subCredit == null || "".equals(subCredit) || " ".equals(subCredit)) {
                    totalSubCredit = "";
                    continue;
                }
                if (totalSubCredit == null || "".equals(totalSubCredit) || " ".equals(totalSubCredit)) {
                    totalSubCredit = subCredit;
                    continue;
                }
                totalSubCredit = String.valueOf(Integer.parseInt(subCredit) + Integer.parseInt(totalSubCredit));
            }
            return new Object[]{attendSubClassNames.toString(), totalSubCredit};
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考文字列を得る。
         * @return
         */
        public String getBikoSubstitution(final String substitutionTypeFlg, final Param param) {
            final Object[] info = getBikoSubstitutionInfo(param);
            final String attendSubClassNames = (String) info[0];
            final String totalSubCredit = (String) info[1];
            return daitaiName(substitutionTypeFlg) + attendSubClassNames + totalSubCredit;
        }

        /**
         * 「活動の記録用」の代替科目備考文字列を得る。
         * @return
         */
        public String getBikoSubstitution90(final String year, final String substitutionTypeFlg, final Param param) {
            final Set addedKey = new HashSet();

            final StringBuffer attendSubClassNames = new StringBuffer();
            String sp = "";

            for (final SubstitutionAttendSubclass attendSubclasss : attendSubclasses) {

                final String attendSubClassName = StringUtils.defaultString(attendSubclasss._attendClassName) + "・" + StringUtils.defaultString(attendSubclasss._attendSubclassName);

                final String keyCd;
                if ("1".equals(param._useCurriculumcd)) {
                    keyCd = attendSubclasss._attendClassCd + "-" + attendSubclasss._attendSchoolKind + "-" + attendSubclasss._attendCurriculumCd + "-" + attendSubclasss._attendSubClassCd;
                } else {
                    keyCd = attendSubclasss._attendSubClassCd;
                }

                if (!addedKey.contains(keyCd) && (year == null || year.equals(attendSubclasss._attendyear))) {
                    attendSubClassNames.append(sp + attendSubClassName);
                    sp = "、";
                    addedKey.add(keyCd);
                }
            }
            if (attendSubClassNames.length() != 0) {
                final String substSubClassName = getSubClassName() != null ? getSubClassName() : "";
                return substSubClassName + "は" + attendSubClassNames.toString() + "で" + daitaiName(substitutionTypeFlg);
            }
            return "";
        }

        /**
         * 履修科目の最大年度を得る
         * @return 履修科目の最大年度
         */
        public String getMaxAttendSubclassYear() {
            String maxyear = null;
            for (final SubstitutionAttendSubclass array : attendSubclasses) {
                if (maxyear == null || array._attendyear != null && maxyear.compareTo(array._attendyear) < 0) {
                    maxyear = array._attendyear;
                }
            }
            return maxyear;
        }

        private static class SubstitutionAttendSubclass {
            final String _attendyear;
            final String _attendClassCd;
            final String _attendSchoolKind;
            final String _attendCurriculumCd;
            final String _attendSubClassCd;
            final String _attendClassName;
            final String _attendSubclassName;
            final String _substitutionCredit;
            final Integer _attendCredit;
            final Integer _attendCompCredit;
            final Integer _attendGrades;
            public SubstitutionAttendSubclass(final String attendyear, final String attendClassCd, final String attendSchoolKind, final String attendCurriculumCd, final String attendSubclassCd, final String attendClassName, final String attendSubclassName, final Integer attendCredit, final Integer attendCompCredit, final Integer attendGrades, final String substitutionCredit) {
                _attendyear = attendyear;
                _attendClassCd = attendClassCd;
                _attendSchoolKind = attendSchoolKind;
                _attendCurriculumCd = attendCurriculumCd;
                _attendSubClassCd = attendSubclassCd;
                _attendClassName = attendClassName;
                _attendSubclassName = attendSubclassName;
                _attendCredit = attendCredit;
                _attendCompCredit = attendCompCredit;
                _attendGrades = attendGrades;
                _substitutionCredit = substitutionCredit;
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static class StudyrecSubstitutionContainer {

        static final String ZENBU = "1";  // 代替フラグ:全部
        static final String ICHIBU = "2";  // 代替フラグ:一部

        static final List<String> TYPE_FLG_LIST = Arrays.asList(StudyrecSubstitutionContainer.ZENBU, StudyrecSubstitutionContainer.ICHIBU);

        private final Map studyRecSubstitutions = new HashMap();

        public Map<String, StudyRecSubstitution> getStudyrecSubstitution(final String substitutionTypeFlg) {
            return getMappedMap(studyRecSubstitutions, substitutionTypeFlg);
        }
    }

    private static class CharMS932 {
        final String _char;
        final String _b;
        final int _len;
        public CharMS932(final String v, final byte[] b) {
            _char = v;
            _b = btos(b);
            _len = b.length;
        }
        public String toString() {
            return "[" + _char + " : " + _b + " : " + _len + "]";
        }
        private static String btos(final byte[] b) {
            final StringBuffer stb = new StringBuffer("[");
            final String[] ns = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
            String spc = "";
            for (int i = 0; i < b.length; i++) {
                final int n = b[i] + (b[i] < 0 ? 256 : 0);
                stb.append(spc).append(ns[n / 16]).append(ns[n % 16]);
                spc = " ";
            }
            return stb.append("]").toString();
        }
        private static List toCharMs932List(final String src) throws Exception {
            final List rtn = new ArrayList();
            for (int j = 0; j < src.length(); j++) {
                final String z = src.substring(j, j + 1);             //1文字を取り出す
                final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
                rtn.add(c);
            }
            return rtn;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class HtrainRemark implements Comparable<HtrainRemark> {
        static HtrainRemark Null = new HtrainRemark(null, null, null, null, null, null, null);

        final String _year;
        final String _annual;
        /** 特別活動 */
        final String _specialActRemark;
        /** 所見 */
        final String _totalRemark;
        /** 出欠備考 */
        final String _attendRecRemark;
        /** 総合的な学習の時間学習活動 */
        final String _totalStudyAct;
        /** 総合的な学習の時間評価 */
        final String _totalStudyVal;

        private HtrainRemark(
                final String year,
                final String annual,
                final String specialActRemark,
                final String totalRemark,
                final String attendRecRemark,
                final String totalStudyAct,
                final String totalStudyVal
        ) {
            _year = year;
            _annual = annual;
            _specialActRemark = specialActRemark;
            _totalRemark = totalRemark;
            _attendRecRemark = attendRecRemark;
            _totalStudyAct = totalStudyAct;
            _totalStudyVal = totalStudyVal;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final HtrainRemark that) {
            return _year.compareTo(that._year);
        }

        public String toString() {
            return "year=" + _year + ", totalStudyAct=[" + _totalStudyAct + "]";
        }

        /**
         * 所見クラスを作成し、マップに加えます。
         */
        private static Map loadHtrainRemark(final DB2UDB db2, final String schregno, final Param param) {
            final Map htrainRemarkMap = new HashMap();
            final String sql;
            sql = "SELECT  YEAR, ANNUAL, TOTALSTUDYACT, TOTALSTUDYVAL, SPECIALACTREMARK, TOTALREMARK, ATTENDREC_REMARK"
                + " FROM HTRAINREMARK_DAT"
                + " WHERE SCHREGNO = '" + schregno + "'"
                + " AND YEAR <= '" + param._year + "'"
                ;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String annual = rs.getString("ANNUAL");
                    final String specialActRemark = rs.getString("SPECIALACTREMARK");
                    final String totalRemark = rs.getString("TOTALREMARK");
                    final String attendRecRemark = rs.getString("ATTENDREC_REMARK");
                    final String totalStudyAct = rs.getString("TOTALSTUDYACT");
                    final String totalStudyVal = rs.getString("TOTALSTUDYVAL");

                    final HtrainRemark htrainRemark = new HtrainRemark(
                            year,
                            annual,
                            specialActRemark,
                            totalRemark,
                            attendRecRemark,
                            totalStudyAct,
                            totalStudyVal
                    );
                    htrainRemarkMap.put(year, htrainRemark);
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } catch (final Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return htrainRemarkMap;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<Gakusekiクラスのソート>>。
     */
    private static class GakusekiComparator implements Comparator<Gakuseki> {
        public int compare(final Gakuseki g1, final Gakuseki g2) {
            return g1._year.compareTo(g2._year);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習備考>>。
     */
    private static class GakushuBiko {

        private static String STUDY = "STUDY";
        private static String RISHU = "RISHU";
        private static String SUBST = "SUBST";
        private Map _biko = new HashMap();

        private final StudyrecSubstitutionContainer _ssc = new StudyrecSubstitutionContainer();

        /**
         * 科目の年度の学習記録履修単位備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param rishuTanniBiko 履修単位備考
         */
        public void putRisyuTanniBiko(final String subclassCd, final String year, final String rishuTanniBiko) {
            getMappedMap(getMappedMap(_biko, RISHU), subclassCd).put(year, rishuTanniBiko);
        }

        /**
         * 科目の年度の学習記録備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param studyrecBiko 学習記録備考
         */
        public void putStudyrecBiko(final String subclassCd, final String year, final String studyrecBiko) {
            getMappedMap(getMappedMap(_biko, STUDY), subclassCd).put(year, studyrecBiko);
        }


        private Map getStudyrecSubstitutionBikoMap(final String subclassCd, final String substitutionTypeFlg) {
            return getMappedMap(getMappedMap(getMappedMap(_biko, SUBST), subclassCd), substitutionTypeFlg);
        }

        /**
         * 科目の年度の学習記録代替科目備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param studyrecSubstitutionBiko 学習記録代替科目備考
         */
        public void putStudyrecSubstitutionBiko(final String subclassCd, final String substitutionTypeFlg, final String year, final String studyrecSubstitutionBiko) {
            getStudyrecSubstitutionBikoMap(subclassCd, substitutionTypeFlg).put(year, studyrecSubstitutionBiko);
        }

        /**
         * 最小年度から最大年度までの備考の連結文字列を得る。
         * @param map 年度をキーとする備考のマップ
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @return 最小年度から最大年度までの備考のリスト
         */
        private List getBiko(final Map map, final String yearMin, final String yearMax) {
            final List list = new ArrayList();
            for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
                final String year = (String) it.next();
                final String biko = (String) map.get(year);
                if ((yearMin == null || yearMin.compareTo(year) <= 0) && (yearMax == null || year.compareTo(yearMax) <= 0) && biko.length() != 0) {
                    list.add(biko);
                }
            }
            return list;
        }

        /**
         * 科目コードのyearMinからyearMaxまでの履修単位備考をコンマ連結で得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getRisyuTanniBiko(final String subclassCd, final String yearMin, final String yearMax) {
            return mkString(getBiko(getMappedMap(getMappedMap(_biko, RISHU), subclassCd), yearMin, yearMax), "、");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録備考を得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecBiko(final String subclassCd, final String yearMin, final String yearMax) {
            return mkString(getBiko(getMappedMap(getMappedMap(_biko, STUDY), subclassCd), yearMin, yearMax), "、");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録代替科目備考を得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecSubstitutionBiko(final String subclassCd, final String substitutionTypeFlg, final String yearMin, final String yearMax) {
            return mkString(getBiko(getStudyrecSubstitutionBikoMap(subclassCd, substitutionTypeFlg), yearMin, yearMax), "、");
        }

        /**
         * 科目コードの学習記録代替科目の単位を得る。
         * @param subclassCd 科目コード
         * @return
         */
        public Integer getStudyrecSubstitutionCredit(final String subclassCd, final String substitutionTypeFlg, final Param param) {
            if (null == subclassCd) {
                return null;
            }
            final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);
            final List<Object[]> list = new ArrayList();
            for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {
                if (!subclassCd.equals(substitutionSubClassCd)) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);
                final Object[] info = studyRecSubstitution.getBikoSubstitutionInfo(param);
                list.add(info);
            }
            if (list.isEmpty()) {
                return null;
            }
            int total = 0;
            for (final Object[] info : list) {
                total += ((Integer) info[1]).intValue();
            }
            return new Integer(total);
        }

        /**
         * 学習記録備考クラスを作成し、マップに加えます。
         */
        private static void createStudyRecBiko(final DB2UDB db2, final String schregno, final Param param, final GakushuBiko gakushuBiko) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStudyrecBiko(param));
                ps.setString(1, schregno);
                ps.setString(2, param._year); // 年度
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("REMARK") == null) {
                        continue;
                    }
                    final String key = _90.equals(rs.getString("CLASSCD")) ? _90 : getSubclasscd(rs, param);
                    gakushuBiko.putStudyrecBiko(key, rs.getString("YEAR"), rs.getString("REMARK"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * @return 学習の記録備考のＳＱＬ文を戻します。
         */
        private static String sqlStudyrecBiko(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            } else {
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            }
            stb.append("     VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, T1.REMARK");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_DAT T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND T1.YEAR <= ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, T1.YEAR");
            return stb.toString();
        }

        /**
         * 代替科目の学習記録備考を作成し、マップに加えます。
         */
        private static void createStudyRecBikoSubstitution(final Param param, final GakushuBiko gakushuBiko) {
            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = gakushuBiko._ssc.getStudyrecSubstitution(substitutionTypeFlg);

                // 代替科目備考追加処理
                for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                    final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);
                    final String keyCd = _90.equals(substitutionSubClassCd.substring(0, 2)) ? _90 : substitutionSubClassCd;

                    final String bikoSubstitution = studyRecSubstitution.getBikoSubstitution(substitutionTypeFlg, param);

                    gakushuBiko.putStudyrecSubstitutionBiko(keyCd, substitutionTypeFlg, studyRecSubstitution.getMaxAttendSubclassYear(), bikoSubstitution);
                }
            }
        }

        /**
         * 代替科目の元科目を取得。
         */
        private List<StudyRecSubstitution.SubstitutionAttendSubclass> getStudyRecBikoSubstitutionAttendSubclass(final String substitutionSubclassBikoKey) {
            final List<StudyRecSubstitution.SubstitutionAttendSubclass> list = new ArrayList();
            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);

                // 代替科目
                for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                    final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);
                    final String keyCd = _90.equals(substitutionSubClassCd.substring(0, 2)) ? _90 : substitutionSubClassCd;
                    if (substitutionSubclassBikoKey.equals(keyCd)) {
                        list.addAll(studyRecSubstitution.attendSubclasses);
                    }
                }
            }
            return list;
        }

        /**
         * 総合的な学習の時間の代替科目の学習記録備考を作成し、マップに加えます。
         */
        private Map<String, List<String>> getStudyRecBikoSubstitution90(final String substitutionTypeFlg, final List<Gakuseki> gakusekiList, final String keyAll, final Param param) {

            final Map<String, List<String>> map = new HashMap();

            final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);
            for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                if (!_90.equals(substitutionSubClassCd.substring(0,2))) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);

                for (final Gakuseki gakuseki : gakusekiList) {
                    if (null == gakuseki._year) {
                        continue;
                    }
                    getMappedList(map, gakuseki._year).add(studyRecSubstitution.getBikoSubstitution90(gakuseki._year, substitutionTypeFlg, param));
                }

                getMappedList(map, keyAll).add(studyRecSubstitution.getBikoSubstitution90(null, substitutionTypeFlg, param));
            }
            return map;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static abstract class KNJA130_0 {

        protected Vrw32alp svf;

        protected DB2UDB db2;

        private Param _param;

        protected boolean hasdata; // データ有りフラグ

        protected String _form;

        protected Map<String, Map<String, SvfField>> _formFieldMap;

        KNJA130_0(final Vrw32alp svf, final Param param) {
            this.svf = svf;
            _param = param;

            _formFieldMap = new TreeMap<String, Map<String, SvfField>>();
        }

        protected Param param() {
            return _param;
        }

        protected void svfVrSetForm(final String form, final int n) {
            _form = form;
            log.info(" setForm = " + _form);
            svf.VrSetForm(_form, n);

            if (!_formFieldMap.containsKey((_form))) {
                _formFieldMap.put(_form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
            }
        }

        protected int VrsOut(final String field, final String data) {
            if (!getMappedMap(_formFieldMap, _form).containsKey(field)) {
                if (param()._isOutputDebug) {
                    log.warn(" no such field : \"" + field + "\" for \"" + data + "\"");
                    return -1;
                }
            }
            return svf.VrsOut(field, data);
        }

        protected int VrsOutn(final String field, final int gyo, final String data) {
            if (!getMappedMap(_formFieldMap, _form).containsKey(field)) {
                if (param()._isOutputDebug || true) {
                    log.warn(" no such field : \"" + field + "\", "+ gyo + " for \"" + data + "\"");
                }
                return -1;
            }
            return svf.VrsOutn(field, gyo, data);
        }

        public abstract void setDetail(final Student student, final PersonalInfo personalInfo);

        /**
         * <pre>
         *  学年・年度表示欄の印字位置（番号）を戻します。
         *  ・学年制の場合は学年による固定位置
         *  ・単位制の場合は連番
         * </pre>
         *
         * @param i 連番
         * @param gakuseki
         * @param flg 表示フラグ
         *   0: 学年制の場合学年コードの位置、それ以外は連番の位置
         *   1: 学年制の場合「在籍前」表示を考慮して連番の位置
         *   2: 連番の位置
         * @return
         */
        public static int getGradeColumnNum(final Student student, final int i, final Gakuseki gakuseki, final int flg, final Param param) {
            final boolean includeZaisekiMae = 1 == flg;
            final boolean isSeq = 2 == flg;
            if (isSeq) {
                return i;
            } else if ((student.isGakunenSei(param) || param._isGenkyuRyuchi) && !includeZaisekiMae) {
//                if (-1 == gakuseki._gradeCd) {
//                    return -1;
//                }
//                int j = gakuseki._gradeCd;
                int j = Integer.parseInt(gakuseki._grade);
                final int column = 3; // param._isFormType3 ? 3 : 4;
                return (0 == j % column) ? column : j % column;
            } else {
                return i;
            }
        }

        protected void printName(final Vrw32alp svf,
                final PersonalInfo personalInfo,
                final String field,
                final String field1,
                final String field2,
                final int width,
                final int height,
                final int minnum,
                final int maxnum,
                final int yStart,
                final int yStart1,
                final int yStart2) {
            if (personalInfo._isPrintRealName &&
                    personalInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(personalInfo._studentRealName + personalInfo._studentName) &&
                    !personalInfo._studentRealName.equals(personalInfo._studentName)
            ) {
                final String printName1 = personalInfo._studentRealName;
                final String printName2 = personalInfo._studentName;
                final KNJA130CCommon.KNJSvfFieldModify modify1 = new KNJA130CCommon.KNJSvfFieldModify(field1, width, height, yStart1, minnum, maxnum);
                final double charSize1 = modify1.getCharSize(printName1);
                final KNJA130CCommon.KNJSvfFieldModify modify2 = new KNJA130CCommon.KNJSvfFieldModify(field2, width, height, yStart2, minnum, maxnum);
                final double charSize2 = modify2.getCharSize(printName2);
                final double charSize = Math.min(charSize1, charSize2);
                svf.VrAttribute(field1, "Size=" + charSize);
                svf.VrAttribute(field2, "Size=" + charSize);
                svf.VrsOut(field1, printName1);
                svf.VrsOut(field2, printName2);
            } else {
                final String printName = personalInfo._isPrintRealName ? personalInfo._studentRealName : personalInfo._studentName;
                final KNJA130CCommon.KNJSvfFieldModify modify = new KNJA130CCommon.KNJSvfFieldModify(field, width, height, yStart, minnum, maxnum);
                final double charSize = modify.getCharSize(printName);
                svf.VrAttribute(field, "Size=" + charSize);
                svf.VrsOut(field, printName);
            }
        }

        protected static boolean isNewForm(final Param param, final Student student, final PersonalInfo personalInfo) {
            boolean rtn = true;
            return rtn;
        }

        protected static List<String> toCharStringList(final String s) {
            final List<String> rtn = new LinkedList();
            if (null != s) {
                for (int i = 0; i < s.length(); i++) {
                    rtn.add(s.substring(i, i + 1));
                }
            }
            return rtn;
        }

        protected static class KNJSvfFieldInfo {
            int _x1;   //開始位置X(ドット)
            int _x2;   //終了位置X(ドット)
            int _height;  //フィールドの高さ(ドット)
            int _ystart;  //開始位置Y(ドット)
            int _ystart1;  //開始位置Y(ドット)フィールド1
            int _ystart2;  //開始位置Y(ドット)フィールド2
            int _minnum;  //最小設定文字数
            int _maxnum;  //最大設定文字数
            public KNJSvfFieldInfo(final int x1, final int x2, final int height, final int ystart, final int ystart1, final int ystart2, final int minnum, final int maxnum) {
                _x1 = x1;
                _x2 = x2;
                _height = height;
                _ystart = ystart;
                _ystart1 = ystart1;
                _ystart2 = ystart2;
                _minnum = minnum;
                _maxnum = maxnum;
            }
            public KNJSvfFieldInfo() {
                this(-1, -1, -1, -1, -1, -1, -1, -1);
            }
        }

        protected void printSvfRenban(final String field, final String data, final ShokenSize size) {
                printSvfRenban(field, retDividString(data, size._mojisu * 2, size._gyo));
        }

        protected void printSvfRenban(final String field, final List list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    VrsOutn(field, i + 1, (String) list.get(i));
                }
            }
        }

        protected int svfVrsOutn(final String field, final int n, final String data) {
            return VrsOutn(field, n, data);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 生徒指導要録(学籍の記録)。
     */
    private static class KNJA130_1 extends KNJA130_0 {

        private final KNJSvfFieldInfo _kana = new KNJSvfFieldInfo(-1, -1, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 8.0, 1), -1, -1, -1, 12, 100);
        private final KNJSvfFieldInfo _gKana = new KNJSvfFieldInfo(-1, -1, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 8.0, 1), -1, -1, -1, 12, 100);
        private final KNJSvfFieldInfo _name = new KNJSvfFieldInfo(-1, -1, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 13.0, 1), -1, -1, -1, 24, 48);
        private final KNJSvfFieldInfo _gName = new KNJSvfFieldInfo(-1, -1, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 13.0, 1), -1, -1, -1, 24, 48);
        final int _addressMax;

        KNJA130_1(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _addressMax = 2;
        }

        private String getForm(final Student student, final PersonalInfo personalInfo) {

            final int kanaX1;
            final int kanaX2;
            final int kanaYstart;
            final int kanaYstart1;
            final int kanaYstart2;
            final int gKanaX1;
            final int gKanaX2;
            final int gKanaYstart;
            final int gKanaYstart1;
            final int gKanaYstart2;
            final int nameX1;
            final int nameX2;
            final int nameYstart;
            final int nameYstart1;
            final int nameYstart2;
            final int gNameX1;
            final int gNameX2;
            final int gNameYstart;
            final int gNameYstart1;
            final int gNameYstart2;

            final String form;

            //form = (param()._isFormType3) ? "KNJA130C_11.frm" : "KNJA130C_1.frm";
            form = "KNJA130G_1.frm";
            kanaX1 = gKanaX1 = nameX1 = gNameX1 = 591;
            kanaX2 = nameX2 = 1496;
            gKanaX2 = gNameX2 = 1652;
            kanaYstart = 719;
            kanaYstart1 = -1;
            kanaYstart2 = -1;
            gKanaYstart = 1545;
            gKanaYstart1 = -1;
            gKanaYstart2 = -1;
            nameYstart = 869;
            nameYstart1 = 829;
            nameYstart2 = 909;
            gNameYstart = 1687;
            gNameYstart1 = 1647;
            gNameYstart2 = 1727;
            _kana._x1 = kanaX1;
            _kana._x2 = kanaX2;
            _kana._ystart = kanaYstart;
            _kana._ystart1 = kanaYstart1;
            _kana._ystart2 = kanaYstart2;
            _gKana._x1 = gKanaX1;
            _gKana._x2 = gKanaX2;
            _gKana._ystart = gKanaYstart;
            _gKana._ystart1 = gKanaYstart1;
            _gKana._ystart2 = gKanaYstart2;
            _name._x1 = nameX1;
            _name._x2 = nameX2;
            _name._ystart = nameYstart;
            _name._ystart1 = nameYstart1;
            _name._ystart2 = nameYstart2;
            _gName._x1 = gNameX1;
            _gName._x2 = gNameX2;
            _gName._ystart = gNameYstart;
            _gName._ystart1 = gNameYstart1;
            _gName._ystart2 = gNameYstart2;

            return form;
        }

        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            final String form = getForm(student, personalInfo);
            svfVrSetForm(form, 1);

            // 印刷処理
            printDetail(student, personalInfo);
            int i = 0;
            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                if (!personalInfo.isTargetYear(gakuseki._year, param())) {
                    continue;
                }
                i = getGradeColumnNum1(student, i, gakuseki);
                printGradeRecDetail(student, personalInfo, i, gakuseki, student._schoolInfo);

                // 留年以降を改ページします。
                //if (gakuseki._isDrop && !param()._isKyoto) {
                if (gakuseki._isDrop) {
                    svf.VrEndPage();
                    printDetail(student, personalInfo);
                    i = 0;
                }
            }
            svf.VrEndPage();
            hasdata = true;
        }

        private int getGradeColumnNum1(final Student student, int i, final Gakuseki gakuseki) {
            if ((student.isGakunenSei(param()) || param()._isGenkyuRyuchi)) {
                i = Integer.parseInt(gakuseki._grade);
            } else {
                i++;
            }
            return i;
        }

        /**
         * 変動しない(ページで)項目を印刷します。
         * @param student
         */
        private void printDetail(final Student student, final PersonalInfo personalInfo) {
            // デフォルト印刷
            try {
                final int max = 6; // param()._isKyoto ? 6 : 4;
                final String setDateFormat = setDateFormat(db2, null, param()._year, param());
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
                svf.VrsOut("J_GRADUATEDDATE_Y", setDateFormat);
                svf.VrsOut("ENTERDATE1", setDateFormat);
                svf.VrsOut("TRANSFER_DATE_1", setDateFormat);
                svf.VrsOut("TRANSFER_DATE_2", setDateFormat);
                svf.VrsOut("TRANSFER_DATE_3", setDateFormat);
                svf.VrsOut("TRANSFER_DATE3_1", setDateFormat + MARK_FROM_TO + setDateFormat);
                svf.VrsOut("TRANSFER_DATE_4", setDateFormat);
                for (int i = 0; i < max; i++) {
                    final String[] arr = setNendoFormat(db2, null, param()._year, param());
                    svf.VrsOutn("YEAR_1_1", i + 1, arr[0]);
                    svf.VrsOutn("YEAR_1_2", i + 1, arr[1]);
                    svf.VrsOutn("YEAR_1_3", i + 1, arr[2]);
                }
            } catch (Exception ex) {
                log.debug("printSvfDefault error!", ex);
            }

            svf.VrsOut("GRADENAME1", "学年／年次");

            if (student.isKoumokuGakunen(param())) {
                svf.VrsOut("GRADENAME2", student._title);
            }

            // 学校情報印刷
            if (null != student._schoolInfo) {
                final SchoolInfo schoolInfo = student._schoolInfo;
                svf.VrsOut("NAME_gakko1", schoolInfo._schoolName1);
                if (!StringUtils.isBlank(schoolInfo._bunkouSchoolName)) {
                    svf.VrsOut("NAME_gakko2", "（" + schoolInfo._bunkouSchoolName + "）");
                }
                final String addr = schoolInfo._schoolAddress1 + schoolInfo._schoolAddress2;
                final int addrlen = getMS932ByteLength(addr);
                svf.VrsOut("ADDRESS_gakko1" + ("1".equals(param()._useAddrField2) && 50 < addrlen ? "_3" : 40 < addrlen ? "_2" : ""), addr);
                final String addrBunkouSrc = schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2;
                if (!StringUtils.isBlank(addrBunkouSrc)) {
                    final String addrBunkou = "（" + addrBunkouSrc + "）";
                    final int addrBunkouLen = getMS932ByteLength(addrBunkou);
                    svf.VrsOut("ADDRESS_gakko2" + ("1".equals(param()._useAddrField2) && 50 < addrBunkouLen ? "_3" : 40 < addrBunkouLen ? "_2" : ""), addrBunkou);
                }
                if (param()._printSchoolZipcd) {
                    final String mark = "〒"; // (param()._isKindaiHigh || param()._isYuushinkan ? "" : "〒");
                    svf.VrsOut("ZIPCODE", mark + schoolInfo._schoolZipcode);
                }
            }

            // 個人情報印刷
            printPersonalInfo(personalInfo);

            // 住所履歴印刷
            printAddressRec(personalInfo);

            // 保護者住所履歴印刷
            printGuardianAddressRec(personalInfo);

            final String torikeshisen = (student.isGakunenSei(param()) || param()._isGenkyuRyuchi) ? "＝＝＝＝＝＝＝" : "＝＝＝";
            final int charWidth = 55;
            final int x = (2697) + (student.isGakunenSei(param()) || param()._isGenkyuRyuchi ? 0 : charWidth * 4);
            final int keta = getMS932ByteLength(torikeshisen);
            svf.VrAttribute("LINE1", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            svf.VrAttribute("LINE2", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            if (student.isGakunenSei(param()) || param()._isGenkyuRyuchi) {
                svf.VrsOut("ENTERDIV1", "第  学年　入学");
                svf.VrsOut("ENTERDIV2", "第  学年編入学");
                svf.VrsOut("TENNYU", "第  学年転入学");
            } else {
                svf.VrsOut("ENTERDIV1", "入　学");
                svf.VrsOut("ENTERDIV2", "編入学");
            }

            // 入学・編入学・転入学
            printNyugaku(student, personalInfo);
            // 転学・退学・卒業
            printTengakuTaigakuSotsugyo(personalInfo);
            // 異動履歴印刷
            printTransferRec(student, personalInfo);

            // 進学先・就職先等印刷
            for (int i = 0, max = personalInfo._afterGraduatedCourseTextList.size(); i < max; i++) {
                final String line = (String) personalInfo._afterGraduatedCourseTextList.get(i);
                final String field = "AFTER_GRADUATION" + String.valueOf(i + 1) + (getMS932ByteLength(line) > 50 ? "_2" : "") ;
                svf.VrsOut(field, line);
            }
        }

        /**
         * 異動情報を印刷します。
         * @param param
         * @param student
         */
        private void printTransferRec(final Student student, final PersonalInfo personalInfo) {
            int ia = 1; // 休学・留学回数
            int ib = 1; // 休学・留学回数 京都以外はiaと同じ
            for (final Iterator it = student._transferRecList.iterator(); it.hasNext();) {
                final TransferRec tr = (TransferRec) it.next();

                final int namecd2 = Integer.parseInt(tr._transfercd);
                if (!(param()._seitoSidoYorokuZaisekiMae && Integer.parseInt(tr._sYear) < PersonalInfo.gakusekiMinYear(personalInfo._gakusekiList) || PersonalInfo.gakusekiYearSet(personalInfo._gakusekiList).contains(tr._sYear))) {
                } else if (ia > 3) {
                } else {
                    final String slash = "／";
                    final String dateFromTo;
                    dateFromTo = tr._sDateStr + MARK_FROM_TO + tr._eDateStr ;
                    if (namecd2 == TransferRec.A004_NAMECD2_RYUGAKU) { // 留学
                        svf.VrsOut("TRANSFER_DATE3_" + ia, dateFromTo);
                        ia++;
                        final String reason;
                        reason = tr._name + (tr._reason == null ? "" : slash + tr._reason);
                        svf.VrsOut("TRANSFERREASON3_" + ib + "_1", reason);
                        svf.VrsOut("TRANSFERREASON3_" + ib + "_2", StringUtils.defaultString(tr._place));
                        ib++;
                    } else if (namecd2 == TransferRec.A004_NAMECD2_KYUGAKU) { // 休学
                        svf.VrsOut("TRANSFER_DATE3_" + ia, dateFromTo);
                        ia++;
                        final String reason = tr._name + (tr._reason == null ? "" : slash + tr._reason);
                        if (!"1".equals(param()._useAddrField2)) {
                            svf.VrsOut("TRANSFERREASON3_" + ib + "_1", reason);
                        }
                        svf.VrsOut("TRANSFERREASON3_" + ib + "_2", StringUtils.defaultString(tr._place));
                        ib++;
                    }
                }
            }
        }

        private void printNyugaku(final Student student, final PersonalInfo personalInfo) {
            final EntGrdHist entgrd = personalInfo._entGrdJ._setData ? personalInfo._entGrdJ : personalInfo._entGrdH;
            final boolean isTennyu = 4 == (null == entgrd._entDiv ? 0 : Integer.parseInt(entgrd._entDiv));
            final boolean isHennyu = 5 == (null == entgrd._entDiv ? 0 : Integer.parseInt(entgrd._entDiv));
            if (isTennyu) {
                svf.VrsOut("TRANSFER_DATE_1", setDateFormat(db2, formatDate1(db2, entgrd._entDate, param()), param()._year, param()));

                if (!"1".equals(param()._useAddrField2)) {
                    if (null != entgrd._entReason) {
                        svf.VrsOut("TRANSFERREASON1_1", "(" + entgrd._entReason + ")");
                    }
                }
                if (null != entgrd._entSchool) {
                    svf.VrsOut("TRANSFERREASON1_2", entgrd._entSchool);
                }
                if ("1".equals(param()._useAddrField2)) {
                    final boolean useField2 = getMS932ByteLength(entgrd._entAddr) > 50 || getMS932ByteLength(entgrd._entAddr2) > 50;
                    if (null != entgrd._entAddr2) {
                        svf.VrsOut("TRANSFERREASON1_4" + (useField2 ? "_2" : ""), entgrd._entAddr);
                        svf.VrsOut("TRANSFERREASON1_5" + (useField2 ? "_2" : ""), entgrd._entAddr2);
                    } else {
                        svf.VrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), entgrd._entAddr);
                    }
                } else {
                    if (null != entgrd._entAddr) {
                        svf.VrsOut("TRANSFERREASON1_3", entgrd._entAddr);
                    }
                }

                if (student.isGakunenSei(param()) || param()._isGenkyuRyuchi) {
                    svf.VrsOut("TENNYU", getGradeName(entgrd._entYearGrade, " ", "転入学"));
                }
            } else {
                final String fldEnterDate;
                final String fldLine;
                final String fldEnterdiv;
                final String enterDiv;
                if (isHennyu) {
                    // 編入学を印字
                    fldEnterDate = "ENTERDATE2";
                    fldLine = "LINE2";
                    fldEnterdiv = "ENTERDIV2";
                    enterDiv = getGradeName(entgrd._entYearGrade, " ", "編入学");
                } else {
                    // 入学を印字
                    fldEnterDate = "ENTERDATE1";
                    fldLine = "LINE1";
                    fldEnterdiv = "ENTERDIV1";
                    enterDiv = getGradeName(entgrd._entYearGrade, "1", "入　学");
                }
                svf.VrsOut(fldEnterDate, setDateFormat(db2, formatDate1(db2, entgrd._entDate, param()), param()._year, param()));
                svf.VrAttribute(fldLine, "X=10000"); // 打ち消し線消去
                svf.VrsOut("ENTERRESONS1", personalInfo._zaigakusubekiKikan);
                if (!"1".equals(param()._useAddrField2)) {
                    if (null != entgrd._entReason) {
                        svf.VrsOut("ENTERRESONS3", "(" + entgrd._entReason + ")");
                    }
                }
                if (student.isGakunenSei(param()) || param()._isGenkyuRyuchi) {
                    svf.VrsOut(fldEnterdiv, enterDiv);
                }
            }
        }

        private void printTengakuTaigakuSotsugyo(final PersonalInfo personalInfo) {
            if ("1".equals(personalInfo._entGrdJ._grdDiv)) {
                svf.VrsOut("TRANSFER_DATE_3", setDateFormat(db2, formatDate1(db2, personalInfo._entGrdJ._grdDate, param()), param()._year, param()));
            }
            final EntGrdHist entGrd;
            if (personalInfo._entGrdH._setData) {
                entGrd = personalInfo._entGrdH;
            } else {
                entGrd = personalInfo._entGrdJ;
            }
            if (entGrd.isTengaku() || entGrd.isTaigaku() || entGrd.isTenseki() || entGrd.isJoseki()) {
                String kubun = "";
                if (entGrd.isTengaku()) {
                    kubun = "転学";
                } else if (entGrd.isTaigaku()){
                    kubun = "退学";
                } else if (entGrd.isTenseki()) {
                    kubun = "転籍";
                } else if (entGrd.isJoseki()) {
                    kubun = "除籍";
                }
                svf.VrsOut("KUBUN", kubun);
                svf.VrsOut("TRANSFER_DATE_2", setDateFormat(db2, formatDate1(db2, entGrd._grdDate, param()), param()._year, param()));
                svf.VrsOut("tengaku_GRADE", StringUtils.isNumeric(entGrd._grdYearGrade) ? " " + String.valueOf(Integer.parseInt(entGrd._grdYearGrade)) : entGrd._grdYearGrade);
                if (!"1".equals(param()._useAddrField2)) {
                    if (entGrd._grdReason != null) {
                        svf.VrsOut("TRANSFERREASON2_1", "(" + entGrd._grdReason + ")");
                    }
                }
                if (entGrd._grdSchool != null) {
                    svf.VrsOut("TRANSFERREASON2_2", entGrd._grdSchool);
                }
                if ("1".equals(param()._useAddrField2)) {
                    final boolean useField2 = getMS932ByteLength(entGrd._grdAddr) > 50 || getMS932ByteLength(entGrd._grdAddr2) > 50;
                    if (null != entGrd._grdAddr2) {
                        svf.VrsOut("TRANSFERREASON2_4" + (useField2 ? "_2" : ""), entGrd._grdAddr);
                        svf.VrsOut("TRANSFERREASON2_5" + (useField2 ? "_2" : ""), entGrd._grdAddr2);
                    } else {
                        svf.VrsOut("TRANSFERREASON2_3" + (useField2 ? "_2" : ""), entGrd._grdAddr);
                    }
                } else {
                    if (entGrd._grdAddr != null) {
                        svf.VrsOut("TRANSFERREASON2_3", entGrd._grdAddr);
                    }
                }
            }
            if ("1".equals(personalInfo._entGrdH._grdDiv)) { // 卒業
                svf.VrsOut("TRANSFER_DATE_4", setDateFormat(db2, formatDate1(db2, personalInfo._entGrdH._grdDate, param()), param()._year, param()));
                if (personalInfo._entGrdH._grdNo != null) {
                    svf.VrsOut("FIELD1", personalInfo._entGrdH._grdNo); // 卒業台帳番号
                }
            }
        }

        /**
         * 3/31転学の場合学年コード+1、それ以外は学年コードを得る。
         * @param gradecd 学年コード
         * @param date 転学日付
         * @return 学年コード
         */
        private String gradecdPlus1When0331(final String gradecd, final String date) {
            if (!StringUtils.isNumeric(gradecd) || !"3".equals(month(date)) || !"31".equals(dayOfMonth(date))) {
                return gradecd;
            }
            return String.valueOf(1 + Integer.parseInt(gradecd));

        }

        private static String getGradeName(final String gradeCd, final String nullGradeCd, final String kubun) {
            final StringBuffer stb = new StringBuffer();
            stb.append("第 ");
            if (StringUtils.isNumeric(gradeCd)) {
                stb.append(String.valueOf(Integer.parseInt(gradeCd)));
            } else {
                stb.append(nullGradeCd);
            }
            stb.append("学年");
            stb.append(kubun);
            return stb.toString();
        }

        /**
         * 生徒住所履歴を印刷します。
         * @param student
         */
        private void printAddressRec(final PersonalInfo personalInfo) {
            final List printAddressRecList = AddressRec.getPrintAddressRecList(personalInfo._addressRecList, _addressMax);
            for (int i = 1, num = printAddressRecList.size(); i <= num; i++) {
                final AddressRec addressRec = (AddressRec) printAddressRecList.get(i - 1);
                final boolean islast = i == num;
                printZipCode("ZIPCODE", i, addressRec._zipCode, islast, "ZIPCODELINE");

                final int n1 = getMS932ByteLength(addressRec._address1);
                printAddr1("ADDRESS", i, addressRec._address1, n1, islast, "ADDRESSLINE");

                if (addressRec._isPrintAddr2) {
                    printAddr2("ADDRESS", i, addressRec._address2, n1, islast, "ADDRESSLINE");
                }
            }
        }

        private void printAddr1(final String field, int i, final String addr1, final int keta1, final boolean islast, final String linefield) {
            String p = null;
            if ("1".equals(param()._useAddrField2) && 50 < keta1) {
                p = "_1_3";
            } else if (40 < keta1) {
                p = "_1_2";
            } else if (0 < keta1) {
                p = "_1_1";
            }
            if (p != null) {
                svf.VrsOut(field + i + p, addr1);
                printAddressLine(svf, addr1, islast, linefield + i + p);
            }
        }

        private void printAddr2(final String field, int i, final String addr2, final int keta1, final boolean islast, final String linefield) {
            final int keta2 = getMS932ByteLength(addr2);
            String p = null;
            if ("1".equals(param()._useAddrField2) && (50 < keta2 || 50 < keta1)) {
                p = "_2_3";
            } else if (40 < keta2 || 40 < keta1) {
                p = "_2_2";
            } else if (0 < keta2) {
                p = "_2_1";
            }
            if (p != null) {
                svf.VrsOut(field + i + p, addr2);
                printAddressLine(svf, addr2, islast, linefield + i + p);
            }
        }

        private AddressRec getSameLineSchregAddress(final List<AddressRec> printAddressRecList, final int i) {
            AddressRec rtn = null;
            if (printAddressRecList.size() > i) {
                rtn = printAddressRecList.get(i);
            }
            return rtn;
        }

        private boolean isSameAddressList(final List<AddressRec> addrListA, final List<AddressRec> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                rtn = false;
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final AddressRec addressAi = addrListA.get(i);
                    final AddressRec addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        private boolean isSameAddress(final AddressRec addressAi, final AddressRec addressBi) {
            boolean rtn = true;
            if (null == addressAi || null == addressBi) {
                rtn = false;
            } else {
                if (null == addressAi._address1 && null == addressBi._address1) {
                } else if (null == addressAi._address1 || null == addressBi._address1 || !addressAi._address1.equals(addressBi._address1)) {
                    rtn = false;
                }
                if (null == addressAi._address2 && null == addressBi._address2) {
                } else if (!addressAi._isPrintAddr2 && !addressBi._isPrintAddr2) {
                } else if (null == addressAi._address2 || null == addressBi._address2 || !addressAi._address2.equals(addressBi._address2)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 保護者住所履歴を印刷します。
         * @param student
         */
        private void printGuardianAddressRec(final PersonalInfo personalInfo) {
            svf.VrsOut("GRD_HEADER", personalInfo._addressGrdHeader);
            final String SAME_TEXT = "生徒の欄に同じ";
            final List<AddressRec> printAddressRecList = AddressRec.getPrintAddressRecList(personalInfo._addressRecList, _addressMax);
            final List<AddressRec> guardPrintAddressRecList = AddressRec.getPrintAddressRecList(personalInfo._guardianAddressRecList, _addressMax);
            if (AddressRec.isSameAddressList(printAddressRecList, guardPrintAddressRecList)) {
                // 住所が生徒と同一
                svf.VrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            }
            for (int i = 1, num = guardPrintAddressRecList.size(); i <= num; i++) {
                final AddressRec guardianAddressRec = guardPrintAddressRecList.get(i - 1);
                final boolean islast = i == num;
                final String guardianAddress1 = StringUtils.defaultString(guardianAddressRec._address1);
                final String guardianAddress2 = StringUtils.defaultString(guardianAddressRec._address2);

                final AddressRec schregAddressRec = getSameLineSchregAddress(printAddressRecList, i - 1);
                boolean isSameAddress = AddressRec.isSameAddress(schregAddressRec, guardianAddressRec);
                if (islast && !isSameAddress) {
                    final AddressRec studentAddressMax = personalInfo.getStudentAddressMax();
                    // 最新の生徒住所とチェック
                    final String addr1 = null == studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address1);
                    final String addr2 = null == studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address2);
                    isSameAddress = addr1.equals(guardianAddress1) && addr2.equals(guardianAddress2);
                }

                if (isSameAddress) {
                    // 内容が生徒と同一
                    svf.VrsOut("GUARDIANADD" + i + "_1_1", SAME_TEXT);
                    printAddressLine(svf, SAME_TEXT, islast, "GUARDIANADDLINE" + i + "_1_1");
                } else {
                    printZipCode("GUARDZIP", i, guardianAddressRec._zipCode, islast, "GUARDZIPLINE");

                    final int keta1 = getMS932ByteLength(guardianAddress1);
                    printAddr1("GUARDIANADD", i, guardianAddress1, keta1, islast, "GUARDIANADDLINE");

                    if (guardianAddressRec._isPrintAddr2) {
                        printAddr2("GUARDIANADD", i, guardianAddress2, keta1, islast, "GUARDIANADDLINE");
                    }
                }
            }
        }

        private void printZipCode(final String field, int i, final String zipCode, final boolean islast, final String linefield) {
            if (param()._printZipcd) {
                svf.VrsOut(field + i, zipCode);
                printAddressLine(svf, zipCode, islast, linefield + i);
            }
        }

        /**
         * 名前を表示する。
         */
        private void printName(final String nameHistFirst, String name, final String field, final KNJSvfFieldInfo fi, final boolean isKana) {
            if (!StringUtils.isBlank(nameHistFirst) && !nameHistFirst.equals(name) && fi._ystart1 != -1 && fi._ystart2 != -1) {
                final int keta = Math.min(getMS932ByteLength(nameHistFirst), fi._maxnum);
                final String fieldname1 = field + "1_1";
                final KNJA130CCommon.KNJSvfFieldModify modify1 = new KNJA130CCommon.KNJSvfFieldModify(fieldname1, fi._x2 - fi._x1, fi._height, fi._ystart1, fi._minnum, fi._maxnum);
                final double charSize1 = modify1.getCharSize(nameHistFirst);
                svf.VrAttribute(fieldname1, "Size=" + charSize1);
                svf.VrAttribute(fieldname1, "Y=" + (int) modify1.getYjiku(0, charSize1));
                svf.VrsOut(fieldname1, nameHistFirst);
                svf.VrAttribute(fieldname1, "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線

                final String fieldname2 = field + "2_1";
                final KNJA130CCommon.KNJSvfFieldModify modify2 = new KNJA130CCommon.KNJSvfFieldModify(fieldname2, fi._x2 - fi._x1, fi._height, fi._ystart2, fi._minnum, fi._maxnum);
                final double charSize2 = modify2.getCharSize(name);
                svf.VrAttribute(fieldname2, "Size=" + charSize2);
                svf.VrAttribute(fieldname2, "Y=" + (int) modify2.getYjiku(0, charSize2));
                svf.VrsOut(fieldname2, name);
            } else {
                // 履歴なしもしくは最も古い履歴の名前が現データの名称と同一
                final String fieldname = field + (isKana ? "" : "1");
                final KNJA130CCommon.KNJSvfFieldModify modify = new KNJA130CCommon.KNJSvfFieldModify(fieldname, fi._x2 - fi._x1, fi._height, fi._ystart, fi._minnum, fi._maxnum);
                final double charSize = modify.getCharSize(name);
                svf.VrAttribute(fieldname, "Size=" + charSize);
                svf.VrAttribute(fieldname, "Y=" + (int) modify.getYjiku(0, charSize));
                svf.VrsOut(fieldname, name);
            }
        }

        /**
         * 生徒情報を印刷します。
         * @param personalinfo
         */
        private void printPersonalInfo(final PersonalInfo personalInfo) {
            final int keta = getMS932ByteLength("＝＝＝＝＝");
            if (!"全日制".equals(personalInfo._checkCourseName)) {
                svf.VrAttribute("LINE4", "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            }
            if (!"定時制".equals(personalInfo._checkCourseName)) {
                svf.VrAttribute("LINE5", "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            }

            printName(personalInfo._studentKanaHistFirst, personalInfo._studentKana, "KANA", _kana, true);
            svf.VrsOut("MAJORNAME", param()._printMajorname);
            final String guarKana = personalInfo._isPrintGuarantor ? personalInfo._guarantorKana : personalInfo._guardKana;
            final String guarKanaHistFirst = personalInfo._isPrintGuarantor ? personalInfo._guarantorKanaHistFirst : personalInfo._guardKanaHistFirst;
            printName(guarKanaHistFirst, guarKana, "GUARD_KANA", _gKana, true);
            if (param()._simei != null) { // 漢字名指定あり？
                final String printName;
                if (personalInfo._isPrintRealName && personalInfo._isPrintNameAndRealName && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                    printName = personalInfo._studentRealName + personalInfo._studentName;
                } else if (personalInfo._isPrintRealName) {
                    printName = personalInfo._studentRealName;
                } else {
                    printName = personalInfo._studentName;
                }
                printName(personalInfo._studentNameHistFirst, printName, "NAME", _name, false);

                final String guarName = personalInfo._isPrintGuarantor ? personalInfo._guarantorName : personalInfo._guardName;
                final String guarNamehistFirst = personalInfo._isPrintGuarantor ? personalInfo._guarantorNameHistFirst : personalInfo._guardNameHistFirst;
                printName(guarNamehistFirst, guarName, "GUARD_NAME", _gName, false);
            }
            svf.VrsOut("BIRTHDAY", null == personalInfo._birthdayStr ? null : personalInfo._birthdayStr + "生");
            svf.VrsOut("SEX", personalInfo._sex);
            final EntGrdHist _entGrd = personalInfo._entGrdJ._setData ? personalInfo._entGrdJ : personalInfo._entGrdH;
            svf.VrsOut("J_GRADUATEDDATE_Y", setDateFormat(db2, formatDateM(db2, _entGrd._finishDate, param()), param()._year, param()));
            svf.VrsOut("INSTALLATION_DIV", _entGrd._installationDiv);

            // 入学前学歴の学校名編集
            final String kotei;
            kotei = StringUtils.defaultString(_entGrd._finschoolTypeName) + "卒業";
            final String finschool1 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL1" : "FINSCHOOL1_HIRO";
            final String finschool2 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL2" : "FINSCHOOL2_HIRO";
            final String finschool3 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL3" : "FINSCHOOL3_HIRO";
            if (StringUtils.isBlank(_entGrd._juniorSchoolName)) {
                svf.VrsOut(finschool1, kotei);
            } else {
                final String schoolName;
//                if (param()._isKumamoto) {
//                    schoolName = personalInfo._installationDiv + personalInfo._juniorSchoolName;
//                } else {
                    final int i = _entGrd._juniorSchoolName.indexOf('　');  // 全角スペース
                    if (-1 < i && 5 >= i) {
                        final String ritu = _entGrd._juniorSchoolName.substring(0, i);
                        if (null != ritu) {
                            svf.VrsOut("INSTALLATION_DIV",  ritu + "立");
                        }
                        schoolName = _entGrd._juniorSchoolName.substring(i + 1);
                    } else {
                        schoolName = _entGrd._juniorSchoolName;
                    }
//                }

                final int totallen = getMS932ByteLength(schoolName) + getMS932ByteLength(kotei);
                if (totallen <= 40) {
                    svf.VrsOut(finschool1, schoolName + kotei);
                } else if (totallen <= 50) {
                    svf.VrsOut(finschool2, schoolName + kotei);
                } else {
                    svf.VrsOut(finschool2, schoolName);
                    svf.VrsOut(finschool3, kotei);
                }
            }
        }

        /**
         * 住所の取り消し線印刷
         * @param svf
         * @param i
         */
        private void printAddressLine(final Vrw32alp svf, final String val, final boolean islast, final String field) {
            if (null == val || islast) {
                return;
            }
            svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + getMS932ByteLength(val));
        }

        /**
         * 学籍履歴を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(
                final Student student,
                final PersonalInfo personalInfo,
                final int i,
                final Gakuseki gakuseki,
                final SchoolInfo school
        ) {
            if (null != school) {
                VrsOut("SCHOOLNAME1", school._schoolName1);
            }

            // 学年
            if (student.isKoumokuGakunen(param())) {
                VrsOut("GRADE2_" + i, gakuseki._gakunenSimple);
            } else {
                final String[] nendoArray = gakuseki.nendoArray(param());
                VrsOut("GRADE1_" + i, nendoArray[0]);
                VrsOut("GRADE2_" + i, nendoArray[1]);
                VrsOut("GRADE3_" + i, nendoArray[2]);
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            VrsOut(hrClassField + i, gakuseki._hrname);

            // 整理番号
            VrsOut("ATTENDNO_" + i, gakuseki._attendno);

            //
            final String[] arr = setNendoFormat(db2, "1", gakuseki._year, param());
            VrsOutn("YEAR_1_1", i, arr[0]);
            VrsOutn("YEAR_1_2", i, arr[1]);
            VrsOutn("YEAR_1_3", i, arr[2]);
            if (student.isKoumokuGakunen(param())) {
                VrsOutn("GRADE_1", i, gakuseki._gakunenSimple);
            }

            EntGrdHist egHist = null;
            if (Param.SCHOOL_KIND_H.equals(gakuseki._schoolKind)) {
                egHist = personalInfo._entGrdH;
            } else if (Param.SCHOOL_KIND_J.equals(gakuseki._schoolKind)) {
                egHist = personalInfo._entGrdJ;
            }
            String grdDate = null;
            if (null != egHist) {
                grdDate = egHist._grdDate;
            }

            Staff _principal = Staff.Null;
            Staff _principal1 = Staff.Null;
            Staff _principal2 = Staff.Null;
//            if (Gakuseki.GAKUSEKI_DATA_FLG1.equals(gakuseki._dataflg)) {
                final Map<String, String> certifSchoolMap = Util.getMappedHashMap(student._yearCertifSchoolMap, gakuseki._year);

                final Param param = param();
                final String principalName = KnjDbUtils.getString(certifSchoolMap, "PRINCIPALNAME");
                final List<Map<String, String>> principalList = getMappedList(param._staffInfo._yearPrincipalListMap, gakuseki._year);
                String principalStaffcd1 = null;
                String principalStaffcd2 = null;
                String principal1FromDate = null;
                String principal1ToDate = null;
                String principal2FromDate = null;
                String principal2ToDate = null;
                if (null != principalList && principalList.size() > 0) {
                    final Map<String, String> listLast = principalList.get(principalList.size() - 1);

                    Map<String, String> last = null;
                    if (null != grdDate) {
                        // 生徒の卒業日付以降の校長は対象外
                        for (int j = 0; j < principalList.size(); j++) {
                            final Map principal = principalList.get(j);
                            final String fromDate = KnjDbUtils.getString(principal, "FROM_DATE");
                            if (fromDate.compareTo(grdDate) > 0) {
                                break;
                            }
                            last = principal;
                        }
                    }
                    if (null != KnjDbUtils.getString(last, "FROM_DATE") && !KnjDbUtils.getString(last, "FROM_DATE").equals(KnjDbUtils.getString(listLast, "FROM_DATE"))) {
                        log.info(" principal last date = " + KnjDbUtils.getString(last, "FROM_DATE") + " instead of " + KnjDbUtils.getString(listLast, "FROM_DATE") + " (student grddate = " + grdDate + ")");
                    } else {
                        last = listLast;
                    }
                    principalStaffcd1 = KnjDbUtils.getString(last, "STAFFCD");
                    principal1FromDate = KnjDbUtils.getString(last, "FROM_DATE");
                    principal1ToDate = KnjDbUtils.getString(last, "TO_DATE");
                    final Map<String, String> first = principalList.get(0);
                    principalStaffcd2 = KnjDbUtils.getString(first, "STAFFCD");
                    principal2FromDate = KnjDbUtils.getString(first, "FROM_DATE");
                    principal2ToDate = KnjDbUtils.getString(first, "TO_DATE");
//                }

                _principal = new Staff(gakuseki._year, new StaffMst(null, principalName, null, null, null), null, null, param._staffInfo.getStampNo(KnjDbUtils.getString(certifSchoolMap, "PRINCIPALSTAFFCD"), gakuseki._year));
                _principal1 = new Staff(gakuseki._year, StaffMst.get(param._staffInfo._staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, param._staffInfo.getStampNo(principalStaffcd1, gakuseki._year));
                _principal2 = new Staff(gakuseki._year, StaffMst.get(param._staffInfo._staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);
            }

            // 校長氏名
            printStaffName("1", i, true, _principal, _principal1, _principal2);

            // 担任１氏名
            final List<Staff> studentStaff1HistList = param()._staffInfo.getStudentStaffHistList(param()._isOutputDebugStaff, param()._semesterMap, student._regdList, egHist, StaffInfo.TR_DIV1, gakuseki._year);
            final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
            final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);
            printStaffName("2", i, false, Staff.Null, staff1Last, staff1First);

            // 担任２氏名
            final List<Staff> studentStaff2HistList = param()._staffInfo.getStudentStaffHistList(param()._isOutputDebugStaff, param()._semesterMap, student._regdList, egHist, StaffInfo.TR_DIV2, gakuseki._year);
            final Staff staff2Last = Util.last(studentStaff2HistList, Staff.Null);
            final Staff staff2First = Util.head(studentStaff2HistList, Staff.Null);
            printStaffName("3", i, false, Staff.Null, staff2Last, staff2First);

            // 担任３氏名
            final List<Staff> studentStaff3HistList = param()._staffInfo.getStudentStaffHistList(param()._isOutputDebugStaff, param()._semesterMap, student._regdList, egHist, StaffInfo.TR_DIV3, gakuseki._year);
            final Staff staff3Last = Util.last(studentStaff3HistList, Staff.Null);
            final Staff staff3First = Util.head(studentStaff3HistList, Staff.Null);
            printStaffName("4", i, false, Staff.Null, staff3Last, staff3First);

            if (!personalInfo.isTargetYearLast(gakuseki._year, student, param())) {
                return;
            }

            //印影
            log.debug("印影："+param()._inei);
            if (null != param()._inei) { // || (param()._isKyoto && null != param()._staffGroupcd && param()._staffGroupcd.startsWith("999"))) {
                log.debug("改竄："+gakuseki._kaizanFlg);
                log.debug("署名（校長）："+gakuseki._principalSeq);
                log.debug("署名（担任）："+gakuseki._staffSeq);
                //改竄されていないか？
                if (null == gakuseki._kaizanFlg) {
                    //署名（校長）しているか？
                    if (null != gakuseki._principalSeq) {
                        final String path1;
                        if (null == _principal2._staffMst._staffcd) {
                            path1 = getImageFilePath(_principal1._stampNo, school);
                        } else {
                            path1 = getImageFilePath(_principal2._stampNo, school);
                        }
                        if (path1 != null) {
                            VrsOut("STAFFBTM_1_" + i + "C", path1); // 校長印
                        }
                    }
                    //署名（担任）しているか？
                    if (null != gakuseki._staffSeq) {
                        final String path2 = getImageFilePath(_principal2._stampNo, school);
                        if (path2 != null) {
                            VrsOut("STAFFBTM_2_" + i + "C", path2); // 担任印
                        }
                    }
                }
            }
        }

        private void printStaffName(final String j, final int gyo, final boolean isPrincipal, final Staff staff0, final Staff staff1Last, final Staff staff1First) {
            final String property_certifPrintRealName = null;
            final boolean isCheckStaff0 = isPrincipal;
            if (isCheckStaff0 && null == staff1Last._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString(property_certifPrintRealName, 20);
                VrsOutn("STAFFNAME_" + j + "_1" + (getMS932ByteLength(name) > 20 ? "_1" : ""), gyo, name);
            } else if (StaffMst.Null == staff1First._staffMst || staff1First._staffMst == staff1Last._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList();
                line.addAll(staff1Last._staffMst.getNameLine(staff1Last._year, property_certifPrintRealName, 36));
                if (line.size() == 2) {
                    VrsOutn("STAFFNAME_" + j + "_1_3", gyo, line.get(0));
                    VrsOutn("STAFFNAME_" + j + "_1_4", gyo, line.get(1));
                } else {
                    final String name = staff1Last.getNameString(property_certifPrintRealName, 30);
                    VrsOutn("STAFFNAME_" + j + "_1" + (getMS932ByteLength(name) > 20 ? "_1" : ""), gyo, name);
                }
            } else {
                final int keta = 36;
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List<String> line = new ArrayList();
                line.addAll(staff1First.getNameBetweenLine(property_certifPrintRealName, keta));
                line.addAll(staff1Last.getNameBetweenLine(property_certifPrintRealName, keta));
                if (line.size() == 2) {
                    VrsOutn("STAFFNAME_" + j + "_1_3", gyo, line.get(0));
                    VrsOutn("STAFFNAME_" + j + "_1_4", gyo, line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        VrsOutn("STAFFNAME_" + j + "_1_" + (k + 2), gyo, line.get(k));
                    }
                }
            }
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename, final SchoolInfo school) {
            if (null == param()._documentRoot || null == school._imageDir || null == school._imageExt || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(param()._documentRoot).append("/").append(school._imageDir).append("/").append(filename).append(".").append(school._imageExt);
            final File file = new File(path.toString());
            if (!file.exists()) {
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 修得単位の記録。
     * 2004/11/29 yamashiro 教科名を左右の欄に跨って出力しない
     * 2006/04/13 yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001
     * ・データが無い場合もフォームを出力する --NO001
     */
    private static class KNJA130_2 extends KNJA130_0 {

        private servletpack.KNJZ.detail.KNJSvfFieldModify svfobj = new servletpack.KNJZ.detail.KNJSvfFieldModify(); // フォームのフィールド属性変更
        /** 1列目までの行数 */
        private int MAX_LINE1 = 39;
        /** 2列目までの行数 */
        private int MAX_LINE2 = 74;
        /** 1ページの最大行数 (3列目までの行数) */
        private int MAX_LINE_PER_PAGE = 74;

        private String SLASH = "/";

        private final KNJSvfFieldInfo _name = new KNJSvfFieldInfo(501, 1235, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 11.0, 1), 561, 531, 591, 24, 48);

        KNJA130_2(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm(final Student student, final PersonalInfo personalInfo) {
            final String form;
            form = "KNJA130G_2.frm";
            return form;
        }

        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            final String form = getForm(student, personalInfo);
            svfVrSetForm(form, 4);

            final Map pageGakusekiListMap = getPageGakusekiListMap(personalInfo);
            final List pageList = new ArrayList(pageGakusekiListMap.keySet());
            for (int gakusekiListIdx = 0; gakusekiListIdx < pageList.size(); gakusekiListIdx++) {
                final Integer page = (Integer) pageList.get(gakusekiListIdx);
                final List gakusekiList = (List) pageGakusekiListMap.get(page);

                printGradeRec2(svf, student, personalInfo, gakusekiList);

                setStudyDetail2(student, personalInfo, gakusekiListIdx, gakusekiList);
            }
        }

        private void setStudyDetail2(final Student student, final PersonalInfo personalInfo, final int gakusekiListIdx, final List gakusekiList) {
            String specialDiv = "00";
            int linex = 0;
            final Map studyRecSubclassTotalMap = PersonalInfo.createStudyRecTotal(personalInfo._studyRecList, personalInfo._dropYears, param(), gakusekiListIdx, gakusekiList);

            final List studyRecSubclassTotalList = new ArrayList(studyRecSubclassTotalMap.values());
            Collections.sort(studyRecSubclassTotalList, new StudyRecSubclassTotal.Comparator(param()));

            boolean printRemark = false;
//            if (param()._isKyoto) {
//                if (isNewForm(param(), student, personalInfo)) {
//                    printRemark = true;
//                } else {
//                }
//            } else {
                printRemark = true;
//            }
            if (printRemark) {
                svf.VrsOut("REMARK", "※( )内は履修だけが認められた単位数を表す。"); // 備考
            }

            final List studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList(student, personalInfo, studyRecSubclassTotalList);
            for (final Iterator itsd = studyrecTotalSpecialDivList.iterator(); itsd.hasNext();) {
                final StudyrecTotalSpecialDiv studyrectotalSpecialDiv = (StudyrecTotalSpecialDiv) itsd.next();
                if (studyrectotalSpecialDiv.isAllDropped()) {
                    continue;
                }

                specialDiv = studyrectotalSpecialDiv._specialDiv;
                final String s_specialname = param().getSpecialDivName(isNewForm(param(), student, personalInfo), specialDiv);
                final List list_specialname = toCharStringList(s_specialname); // 普通・専門名のリスト

                for (final Iterator itc = studyrectotalSpecialDiv._classes.iterator(); itc.hasNext();) {
                    final StudyrecTotalClass studyrectotalClass = (StudyrecTotalClass) itc.next();
                    // 総合的な学習の時間・留学は回避します。
                    if (_90.equals(studyrectotalClass._classcd) || _ABROAD.equals(studyrectotalClass._classname) || studyrectotalClass.isAllDropped()) {
                        continue;
//                    } else if (_94.equals(studyrectotalClass._classcd) && param()._isTokiwa) {
//                        continue;
                    }

                    final String classcd = studyrectotalClass._classcd; // 教科コードの保存
                    final List list_classname = toCharStringList(studyrectotalClass._classname); // 教科名のリスト

                    final List list_subclass = new LinkedList();
                    for (final Iterator its = studyrectotalClass._subclasses.iterator(); its.hasNext();) {
                        final StudyrecTotalSubclass studyrectotalSubclass = (StudyrecTotalSubclass) its.next();
                        // log.debug(" subclass = " + studyrectotalSubclass._subclasscd + ":" + studyrectotalSubclass._subclassname);
                        if (studyrectotalSubclass.isAllDropped()) {
                            continue;
                        }

                        for (final Iterator itst = studyrectotalSubclass._totals.iterator(); itst.hasNext();) {
                            final StudyRecSubclassTotal sst = (StudyRecSubclassTotal) itst.next();
                            list_subclass.add(new Object[]{sst.studyrec().getKeySubclasscd(param()), sst.subClassName(), sst.credit(), sst.compCredit(), sst.creditMstCredit()});
                        }
                    }

                    // 教科名文字数と科目数で多い方を教科の行数にする。教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
                    final int nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 1) : list_classname.size();

                    // 教科が次列に跨らないために、空行を出力する
                    if ((linex < MAX_LINE1 && MAX_LINE1 < linex + nameline) ||
                        (idxin(MAX_LINE1, linex, MAX_LINE2) && MAX_LINE2 < linex + nameline) ||
                        (idxin(MAX_LINE2, linex, MAX_LINE_PER_PAGE) && MAX_LINE_PER_PAGE < linex + nameline)) {
                        final int max = (linex < MAX_LINE1) ? MAX_LINE1 : (linex < MAX_LINE2) ? MAX_LINE2 : MAX_LINE_PER_PAGE;
                        for (int j = linex; j < max; j++) {
                            printGradeRecSub2(svf, student, personalInfo);
                            svf.VrsOut("EDU_DIV2", specialDiv);
                            svf.VrEndRecord();
                            linex++;
                        }
                    }

                    for (int i = 0; i < nameline; i++) {

                        if (0 < list_specialname.size()) {
                            svf.VrsOut("EDU_DIV", str(list_specialname.remove(0))); // 普通・専門名
                        }
                        if (i < list_classname.size()) {
                            svf.VrsOut("CLASSNAME", str(list_classname.get(i))); // 教科名
                        }
                        if (i < list_subclass.size()) {
                            final Object[] subclass = (Object[]) list_subclass.get(i);
                            final String subclasscd = (String) subclass[0];
                            final String subclassname = (String) subclass[1];
                            final Integer credit = (Integer) subclass[2];
                            final Integer compCredit = (Integer) subclass[3];
//                            final Integer creditMstCredits = (Integer) subclass[4];

                            svfFieldAttribute("SUBCLASSNAME", subclassname, linex, student, personalInfo); // SVF-FIELD属性変更のメソッド
                            svf.VrsOut("SUBCLASSNAME", subclassname); // 科目名

                            String creVal = "";
                            String compCreVal = null;
//                            if (param()._isTokiwa && isNewForm(param(), student, personalInfo)) {
//                                if (student.isTengakuTaigaku(personalInfo)) {
//                                    creVal = null == credit ? "0" : credit.toString();
//                                    final String creVal1 = null == compCredit ? "0" : compCredit.toString(); // 修得単位数
//                                    final String creVal2 = null == creditMstCredits ? " " : creditMstCredits.toString(); // 単位マスタの単位
//                                    compCreVal = creVal1 + SLASH + creVal2;
//                                } else {
//                                    creVal = null == credit ? null : credit.toString();
//                                    compCreVal = null == compCredit ? null : compCredit.toString();
//                                }
//                            } else {
                                if (credit != null) {
                                    if (credit.intValue() == 0) {
//                                        if (param()._optionCreditOutput == Param.OPTION_CREDIT2) {
//                                            // 履修単位数があれば0、なければ空欄
//                                            if (null != compCredit && compCredit.intValue() > 0) {
//                                                creVal = "0";
//                                            } else {
//                                                creVal = "";
//                                            }
//                                        } else {
                                            // 履修単位数があればカッコつきで表示
                                            if (null != compCredit && compCredit.intValue() > 0) {
                                                creVal = "(" + compCredit + ")";
                                            }
//                                        }
                                    } else {
                                        // 修得単位数
                                        creVal = credit.toString();
                                    }
                                }
//                            }

                            boolean isOutputCredit = false;

                            if (null != subclasscd) {
                                final String substBikoZenbu = personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ZENBU, null, null).toString();
                                final String substBikoIchibu = personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ICHIBU, null, null).toString();
                                if (!StringUtils.isBlank(substBikoZenbu)) {
                                    final List biko = retDividString(substBikoZenbu, 10, 2); // 全部代替科目備考
                                    for (int j = 0; j < biko.size(); j++) {
                                        svf.VrsOut("CREDIT" + (2 + j), str(biko.get(j)));
                                    }
                                    isOutputCredit = true;   // 全部代替科目備考を表示する場合、修得単位数は表示しない
                                } else if (!StringUtils.isBlank(substBikoIchibu)) {
                                    svf.VrsOut("CREDIT4_1", creVal);
                                    final List biko = retDividString(substBikoIchibu, 14, 2); // 一部代替科目備考
                                    for (int j = 0; j < biko.size(); j++) {
                                        svf.VrsOut("CREDIT4_" + (2 + j), str(biko.get(j)));
                                    }
                                    isOutputCredit = true;
                                }
                            }
                            if (!isOutputCredit) {
                                svf.VrsOut("CREDIT", creVal);
                                if (null != compCreVal) {
                                    svf.VrsOut("COMP_CREDIT", compCreVal);
                                }
                            }
                        }

                        printGradeRecSub2(svf, student, personalInfo);
                        svf.VrsOut("EDU_DIV2", specialDiv);
                        svf.VrsOut("CLASSNAME2", classcd); // 教科コード
                        svf.VrEndRecord();
                        linex++;
                    }

                    if (linex == MAX_LINE_PER_PAGE) {
                        linex = 0;
                    }
                }

                // 普通・専門名文字数
                if (0 != list_specialname.size()) {
                    final int nameline = list_specialname.size();
                    for (int i = 0; i < nameline; i++) {
                        svf.VrsOut("EDU_DIV", str(list_specialname.get(i))); // 普通・専門名
                        printGradeRecSub2(svf, student, personalInfo);
                        svf.VrsOut("EDU_DIV2", specialDiv);
                        svf.VrEndRecord();
                        linex++;
                    }
                    // 普通・専門名のリストを削除する
                    list_specialname.clear();
                    if (linex == MAX_LINE_PER_PAGE) {
                        linex = 0;
                    }
                }
            }

            boolean isfirst = true;
            int printlinex = linex;
            for (;printlinex < MAX_LINE_PER_PAGE - 1; printlinex++) {
                printLine(student, personalInfo, specialDiv, isfirst, printlinex);
                isfirst = false;
            }
            printTotalCredits2(student, personalInfo, gakusekiListIdx, gakusekiList);
            printLine(student, personalInfo, specialDiv, isfirst, printlinex);
            hasdata = true;
        }

        private void printLine(final Student student, final PersonalInfo personalInfo, final String specialDiv, final boolean isfirst, final int linex) {
            svf.VrsOut("EDU_DIV2", specialDiv);
            svf.VrsOut("CLASSNAME2", "000"); // 教科コード
            svf.VrEndRecord();
        }

        private static boolean idxin(final int minidx, final int i, final int length) {
            return minidx <= i && i < length;
        }

        private static String str(final Object o) {
            return (null == o) ? null : o.toString();
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル・学籍履歴）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRec2(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final List gakusekiList) {
            final String lastyear = personalInfo.getLastYear();
            final int max = 3; // param()._isFormType3 ? 3 : 4;
            int i = 1;
            final int flg = 0; // param()._isKyoto ? 2 : 0;
            printGradeRecDetailClear2(svf, student);
            printGradeRecSub2(svf, student, personalInfo);
            for (final Iterator it = gakusekiList.iterator(); it.hasNext();) {
                final Gakuseki gakuseki = (Gakuseki) it.next();
                final int j = getGradeColumnNum(student, i, gakuseki, flg, param());
                printGradeRecDetail2(svf, student, j, gakuseki);
                final boolean islastyear = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                //if (!islastyear && gakuseki._isDrop && !param()._isKyoto) {
                if (!islastyear && gakuseki._isDrop) {
                    //printTurnOverThePages(svf, student);
                } else if (!islastyear && max == i) {
                    //printTurnOverThePages(svf, student);
                    i = 1;
                } else {
                    i++;
                }
            }
        }

        /**
         * ページごとの年度リストのマップを得る。
         * @param student
         */
        private Map getPageGakusekiListMap(final PersonalInfo personalInfo) {
            final int max = 6; // param()._isFormType3 ? 3 : 4;
            final Map rtn = new TreeMap();
            int page = 1;
            rtn.put(new Integer(page), new ArrayList());
            final boolean isSinglePage = false; // param()._isKyoto;
            for (final Iterator it = personalInfo._gakusekiList.iterator(); it.hasNext();) {
                final Gakuseki gakuseki = (Gakuseki) it.next();
                if (!Param.SCHOOL_KIND_H.equals(param().getGdatSchoolKind(gakuseki._year, gakuseki._grade))) {
                    continue;
                }
                if (!personalInfo.isTargetYear(gakuseki._year, param())) {
                    continue;
                }
                final Integer ip = new Integer(page);
                if (null == rtn.get(ip)) {
                    rtn.put(ip, new ArrayList());
                }
                final List gakusekiList = (List) rtn.get(ip);
                gakusekiList.add(gakuseki);
                if (!isSinglePage && gakuseki._isDrop || gakusekiList.size() > max) {
                    page += 1; // 改ページ
                }
            }
            return rtn;
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub2(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo) {
            svf.VrsOut("GRADENAME", student._title);

            printName(svf, personalInfo, "NAME1", "NAME2", "NAME3", _name._x2 - _name._x1, _name._height, _name._minnum, _name._maxnum, _name._ystart, _name._ystart1, _name._ystart2);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetailClear2(final Vrw32alp svf, final Student student) {
            for (int i = 1; i <= 6; i++) {
                if (student.isKoumokuGakunen(param())) {
                    svf.VrsOut("GRADE1_" + i, "");
                } else {
                    svf.VrsOut("GRADE2_" + i, "");
                }
                // ホームルーム
                svf.VrsOut("HR_CLASS1_" + i, "");
                svf.VrsOut("HR_CLASS2_" + i, "");
                svf.VrsOut("ATTENDNO_" + i, "");
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail2(final Vrw32alp svf, final Student student, final int i, final Gakuseki gakuseki) {
            if (student.isKoumokuGakunen(param())) {
                svf.VrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
            } else {
                svf.VrsOut("GRADE2_" + i, gakuseki._nendo);
            }
            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svf.VrsOut(hrClassField + i, gakuseki._hrname);
            svf.VrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits2(final Student student, final PersonalInfo personalInfo, final int gakusekiListIdx, final List<Gakuseki> gakusekiList) {
            final TreeSet yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
            final int minYear = PersonalInfo.gakusekiMinYear(gakusekiList);
            final String yearMin;
            final String yearMax;
            if (yearSet.isEmpty()) {
                yearMin = null;
                yearMax = null;
            } else {
                yearMin = (String) yearSet.first();
                yearMax = (String) yearSet.last();
            }

            final List subject90s = new ArrayList();
            final List subjects = new ArrayList();
            final List abroads = new ArrayList();
            final List totals = new ArrayList();
            final List subject94s = new ArrayList();
            final List subject90Comps = new ArrayList();
            final List subjectComps = new ArrayList();
            final List totalComps = new ArrayList();
            final List subject94Comps = new ArrayList();
            final List subject90CreditMsts = new ArrayList();
            final List subjectCreditMsts = new ArrayList();
            final List totalCreditMsts = new ArrayList();
            final List subject94CreditMsts = new ArrayList();

            final int yoshiki = 2;
            final Collection co = student.getStudyRecYear(param(), isNewForm(param(), student, personalInfo), personalInfo).values();
            for (final Iterator it = co.iterator(); it.hasNext();) {
                final StudyRecYearTotal yearTotal = (StudyRecYearTotal) it.next();
                if (yearTotal._isDrop == StudyRecYearTotal.DROP || yearTotal._isDrop == StudyRecYearTotal.DROP_SHOW) {
                    continue;
                }
                if (param()._isPrintAnotherStudyrec2 && 0 == gakusekiListIdx && NumberUtils.isDigits(yearTotal._year) && Integer.parseInt(yearTotal._year) < minYear) {
                    // 前籍校も出力する
                } else if (!yearSet.contains(yearTotal._year)) {
                    continue;
                }
                int flg;
                flg = 0; // 修得単位
                subject90s.addAll(yearTotal.subject90(flg));
                subject94s.addAll(yearTotal.subject94(flg, param()));
                subjects.addAll(yearTotal.subject(flg, param(), yoshiki));
                abroads.addAll(yearTotal.abroad()); // 留学は修得単位のみ
                totals.addAll(yearTotal.total(flg));
                flg = 1; // 履修単位
                subject90Comps.addAll(yearTotal.subject90(flg));
                subject94Comps.addAll(yearTotal.subject94(flg, param()));
                subjectComps.addAll(yearTotal.subject(flg, param(), yoshiki));
                totalComps.addAll(yearTotal.total(flg));
                flg = 2; // 単位マスタ単位
                subject90CreditMsts.addAll(yearTotal.subject90(flg));
                subject94CreditMsts.addAll(yearTotal.subject94(flg, param()));
                subjectCreditMsts.addAll(yearTotal.subject(flg, param(), yoshiki));
                totalCreditMsts.addAll(yearTotal.total(flg));
            }

            svf.VrsOut("CREDIT4", "");
            svf.VrsOut("CREDIT5", "");
            svf.VrsOut("CREDIT6_1", "");
            svf.VrsOut("CREDIT6_2", "");
            svf.VrsOut("CREDIT6_3", "");

            final String fieldtime = "time"; // param()._isTokiwa ? "GET_time" : "time";
            final String fieldsubtotal = "SUBTOTAL"; // param()._isTokiwa ? "GET_SUBTOTAL" : "SUBTOTAL";
            final String fieldabroad = "ABROAD"; // param()._isTokiwa ? "GET_ABROAD" : "ABROAD";
            final String fieldtotal = "TOTAL"; // param()._isTokiwa ? "GET_TOTAL" :"TOTAL";
            final String fieldhr = null; // param()._isTokiwa ? "GET_HR" : null;
            final String fieldtimeComp = null; // param()._isTokiwa ? "COMP_time" : null;
            final String fieldsubtotalComp = null; // param()._isTokiwa ? "COMP_SUBTOTAL" : null;
            final String fieldtotalComp = null; // param()._isTokiwa ? "COMP_TOTAL" : null;
            final String fieldhrComp = null; // param()._isTokiwa ? "COMP_HR" : null;

            svf.VrsOut(fieldtime, "0"); // 札幌開成は初期値0
            svf.VrsOut(fieldsubtotal, "");
            svf.VrsOut(fieldabroad, "");
            svf.VrsOut(fieldtotal, "");
            svf.VrsOut(fieldhr, "");
            svf.VrsOut(fieldtimeComp, "");
            svf.VrsOut(fieldsubtotalComp, "");
            svf.VrsOut(fieldtotalComp, "");
            svf.VrsOut(fieldhrComp, "");

            svf.VrsOut("FOOTER_SUBCLASSNAME", personalInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(Param.SCHOOL_KIND_H, gakusekiList)));

            String substitutionZenbuBiko = personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ZENBU, yearMin, yearMax).toString();
            String substitutionIchibuBiko = personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ICHIBU, yearMin, yearMax).toString();
            final String creditstime;
            if (param()._optionCreditOutput == Param.OPTION_CREDIT2) {
                final List<StudyRecSubstitution.SubstitutionAttendSubclass> attendSubclassList = personalInfo._gakushuBiko.getStudyRecBikoSubstitutionAttendSubclass(_90);
                boolean attendMirishu = false;
                boolean attendRishuNomi = false;
                for (final StudyRecSubstitution.SubstitutionAttendSubclass sas : attendSubclassList) {
                    log.debug(" attend = " + sas._attendClassCd + "-" + sas._attendSchoolKind + "-" + sas._attendCurriculumCd + "-" + sas._attendSubClassCd + " : " + sas._attendGrades + ", " + sas._attendCredit + ", " + sas._attendCompCredit);
                    if (null == sas._attendGrades && null != sas._attendCredit && sas._attendCredit.intValue() == 0 && null != sas._attendCompCredit && sas._attendCompCredit.intValue() == 0) {
                        // 未履修
                        attendMirishu = true;
                    } else if (null != sas._attendGrades && null != sas._attendCredit && sas._attendCredit.intValue() == 0 && null != sas._attendCompCredit && sas._attendCompCredit.intValue() > 0) {
                        // 履修のみ
                        attendRishuNomi = true;
                    }
                }
                if (attendMirishu) {
                    creditstime = ""; // 空欄（備考無し）
                    substitutionZenbuBiko = null;
                    substitutionIchibuBiko = null;
                } else if (attendRishuNomi) {
                    creditstime = "0"; // 0表示（備考無し）
                    substitutionZenbuBiko = null;
                    substitutionIchibuBiko = null;
                } else if (!subject90s.isEmpty()) {
                    if (sum(subject90s).intValue() == 0 && !subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                        creditstime = "(" + String.valueOf(sum(subject90Comps)) + ")";
                    } else {
                        creditstime = String.valueOf(sum(subject90s));
                    }
                } else {
                    if (!subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                        creditstime = "(" + String.valueOf(sum(subject90Comps)) + ")";
                    } else {
                        creditstime = student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear()) ? "0" : null;
                    }
                }
            } else if (!subject90s.isEmpty()) {
                if (sum(subject90s).intValue() == 0 && !subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                    creditstime = "(" + String.valueOf(sum(subject90Comps)) + ")";
                } else {
                    creditstime = String.valueOf(sum(subject90s));
                }
            } else {
                if (!subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                    creditstime = "(" + String.valueOf(sum(subject90Comps)) + ")";
                } else {
                    creditstime = student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear()) ? "0" : null;
                }
            }
            if (!StringUtils.isBlank(substitutionZenbuBiko)) {
                final String strx = substitutionZenbuBiko;
                final List biko = retDividString(strx, 10, 2);
                for (int j = 0; j < biko.size(); j++) {
                    svf.VrsOut("CREDIT" + (4 + j), (String) biko.get(j));
                }
            } else if (!StringUtils.isBlank(substitutionIchibuBiko)) {
                final String tanni = !subject90Comps.isEmpty() ? String.valueOf(sum(subject90Comps)) : "";
                svf.VrsOut("CREDIT6_1", String.valueOf(tanni));
                final String strx = substitutionIchibuBiko;
                final List biko = retDividString(strx, 14, 2);
                for (int j = 0; j < biko.size(); j++) {
                    svf.VrsOut("CREDIT6_" + (j + 2), (String) biko.get(j));
                }
            } else {
                svf.VrsOut(fieldtime, creditstime);
            }
            final String creditssubtotal = !subjects.isEmpty() ? String.valueOf(sum(subjects)) : student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear()) ? "0" : null;
            svf.VrsOut(fieldsubtotal, creditssubtotal);

            final String creditsabroad = !abroads.isEmpty() ? String.valueOf(sum(abroads)) : "0"; // !(param()._isKyoto || param()._isTokiwa) ? "0" : null;
            if (true) { // !param()._isMiyagi) {
                svf.VrsOut(fieldabroad, creditsabroad);
            }

            final String creditstotal = !totals.isEmpty() ? String.valueOf(sum(totals)) : student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear()) ? "0" : null;
            svf.VrsOut(fieldtotal, creditstotal);

            final String credits94 = !subject94s.isEmpty() ? String.valueOf(sum(subject94s)) : student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear()) ? "0" : null;
            svf.VrsOut(fieldhr, credits94);

//            if (param()._isTokiwa && student.isTengakuTaigaku(personalInfo)) {
//                svf.VrsOut(fieldtimeComp, String.valueOf(nullzero(sum(subject90Comps))) + SLASH + String.valueOf(nullzero(sum(subject90CreditMsts))));
//                svf.VrsOut(fieldsubtotalComp, String.valueOf(nullzero(sum(subjectComps))) + SLASH + String.valueOf(nullzero(sum(subjectCreditMsts))));
//                svf.VrsOut(fieldtotalComp, String.valueOf(nullzero(sum(totalComps))) + SLASH + String.valueOf(nullzero(sum(totalCreditMsts))));
//                svf.VrsOut(fieldhrComp, String.valueOf(nullzero(sum(subject94Comps))) + SLASH + String.valueOf(nullzero(sum(subject94CreditMsts))));
//            } else {
                final String creditsTimeComp = !subject90Comps.isEmpty() ? String.valueOf(sum(subject90Comps)) : null;
                final String creditsSubjectComp = !subjectComps.isEmpty() ? String.valueOf(sum(subjectComps)) : null;
                final String creditsTotalComp = !totalComps.isEmpty() ? String.valueOf(sum(totalComps)) : null;
                final String credits94Comp = !subject94Comps.isEmpty() ? String.valueOf(sum(subject94Comps)) : null;
                svf.VrsOut(fieldtimeComp, creditsTimeComp);
                svf.VrsOut(fieldsubtotalComp, creditsSubjectComp);
                svf.VrsOut(fieldtotalComp, creditsTotalComp);
                svf.VrsOut(fieldhrComp, credits94Comp);
//            }
//            if (param()._isMiyagi) {
//                svf.VrsOut("ABROAD_NAME", "留学");
//                svf.VrsOut(fieldabroad, creditsabroad);
//            }
        }

        /**
         * 科目名の文字数により文字ピッチ及びＹ軸を変更します。（SVF-FORMのフィールド属性変更）
         *
         * @param subclassname:科目名
         * @param line:出力行(通算)
         */
        private void svfFieldAttribute(final String fieldname, final String subclassname, final int line, final Student student, final PersonalInfo personalInfo) {
            int ln = line + 1;
            final int width;
            final int height;
            final int ystart;
            final int namegap;
            final int minnum;
//            if (param()._isTokiwa && isNewForm(param(), student, personalInfo)) {
//                width = 574;
//                height = 92;
//                ystart = 852;
//                minnum = 20;
//                namegap = 21;
//            } else {
                width = 800;
                height = 98;
                ystart = 512;
                minnum = 30;
                namegap = 21;
//            }
            svfobj.width = width; // フィールドの幅(ドット)
            svfobj.height = height; // フィールドの高さ(ドット)
            svfobj.ystart = ystart; // 開始位置(ドット)
            svfobj.minnum = minnum; // 最小設定文字数
            svfobj.maxnum = 40; // 最大設定文字数
            setRetvalue(svfobj, subclassname, (ln % MAX_LINE1 == 0) ? MAX_LINE1 : ln % MAX_LINE1);

            int x;
            if (ln <= MAX_LINE1) {
                // 左列の開始Ｘ軸
                x = 472 + namegap;
            } else if (ln <= MAX_LINE2) {
                // 中列の開始Ｘ軸
//                if (param()._isTokiwa && isNewForm(param(), student, personalInfo)) {
//                    x = 2002 + namegap;
//                } else {
                    x = 2008 + namegap;
//                }
            } else {
                // 右列の開始Ｘ軸
                x = 2008 + namegap;
            }
            svf.VrAttribute(fieldname, "X=" + x);
            svf.VrAttribute(fieldname, "Y=" + svfobj.jiku); // 開始Ｙ軸
            svf.VrAttribute(fieldname, "Size=" + svfobj.size); // 文字サイズ
        }

        /**
         *  ポイント＆Ｙ軸の設定
         *  引数について  String str : 出力する文字列
         *                int hnum   : 出力位置(行)
         */
        public void setRetvalue(final servletpack.KNJZ.detail.KNJSvfFieldModify svfobj, final String str, final int hnum) {
            int num = getMS932ByteLength(str);          //文字数(BYTE)
            if (num < svfobj.minnum) {
                num = svfobj.minnum;
            } else if (num > svfobj.maxnum) {
                num = svfobj.maxnum;
            }
            num += (num % 2 != 0) ? 1 : 0; // 半角等を含んでバイト数が奇数の場合、+1しておく。
            svfobj.size = retFieldPoint(svfobj, num);                  //文字サイズ
            svfobj.jiku = retFieldY(svfobj) + svfobj.ystart + svfobj.height * hnum;  //出力位置＋Ｙ軸の移動幅
        }

        /**
         *  文字サイズを設定
         */
        private float retFieldPoint(final servletpack.KNJZ.detail.KNJSvfFieldModify svfobj, final int num) {
            final float ret = (float) Math.floor((float) svfobj.width / (num / 2) * 72 / 400);
            return ret;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private int retFieldY(final servletpack.KNJZ.detail.KNJSvfFieldModify svfobj) {
            final int ret = (int) Math.round(((double) svfobj.height - (svfobj.size / 72 * 400)) / 2);
            return ret;
        }

        private List<StudyrecTotalSpecialDiv> getStudyrecTotalSpecialDivList(final Student student, final PersonalInfo personalInfo, final List<StudyRecSubclassTotal> studyRecSubclassTotalList) {
            final List<StudyrecTotalSpecialDiv> rtn = new ArrayList();
            for (final StudyRecSubclassTotal studyrectotal : studyRecSubclassTotalList) {
                if (param()._optionCreditOutput == Param.OPTION_CREDIT1) {
                    if (!isNewForm(param(), student, personalInfo) && studyrectotal.isRisyuNomi(param())) {
                        // 京都府は平成24年度以前で履修のみの場合様式1裏に表示しない
                        continue;
                    } else if (studyrectotal.isMirisyu(param())) {
                        // 京都府は単位不認定（未履修）の場合様式1裏に表示しない
                        continue;
                    }
                }
                if (null == getStudyrecTotalSpecialDiv(studyrectotal.specialDiv(), rtn)) {
                    rtn.add(new StudyrecTotalSpecialDiv(studyrectotal.specialDiv()));
                }
                final StudyrecTotalSpecialDiv stsd = getStudyrecTotalSpecialDiv(studyrectotal.specialDiv(), rtn);
                if (null == getStudyrecTotalClass(studyrectotal.classcd(), studyrectotal.schoolKind(), stsd._classes)) {
                    stsd._classes.add(new StudyrecTotalClass(studyrectotal.classcd(), studyrectotal.schoolKind(), studyrectotal.className()));
                }
                final StudyrecTotalClass stc = getStudyrecTotalClass(studyrectotal.classcd(), studyrectotal.schoolKind(), stsd._classes);
                if (null == getStudyrecTotalSubclass(studyrectotal.classcd(), studyrectotal.schoolKind(), studyrectotal.curriculumCd(), studyrectotal.subClasscd(), stc._subclasses)) {
                    stc._subclasses.add(new StudyrecTotalSubclass(studyrectotal.classcd(), studyrectotal.schoolKind(), studyrectotal.curriculumCd(), studyrectotal.subClasscd(), studyrectotal.subClassName()));
                }
                final StudyrecTotalSubclass sts = getStudyrecTotalSubclass(studyrectotal.classcd(), studyrectotal.schoolKind(), studyrectotal.curriculumCd(), studyrectotal.subClasscd(), stc._subclasses);
                sts._totals.add(studyrectotal);
            }
            return rtn;
        }

        private StudyrecTotalSubclass getStudyrecTotalSubclass(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final List<StudyrecTotalSubclass> studyRecTotalSubclassList) {
            StudyrecTotalSubclass rtn = null;
            for (final StudyrecTotalSubclass sts : studyRecTotalSubclassList) {
                if ("1".equals(param()._useCurriculumcd)) {
                    if (sts._classcd.equals(classcd) && sts._schoolKind.equals(schoolKind) && sts._curriculumCd.equals(curriculumCd) && sts._subclasscd.equals(subclasscd)) {
                        rtn = sts;
                        break;
                    }
                } else {
                    if (sts._classcd.equals(classcd) && sts._subclasscd.equals(subclasscd)) {
                        rtn = sts;
                        break;
                    }
                }
            }
            return rtn;
        }

        private StudyrecTotalClass getStudyrecTotalClass(final String classcd, final String schoolKind, final List<StudyrecTotalClass> studyRecTotalClassList) {
            StudyrecTotalClass rtn = null;
            for (final StudyrecTotalClass stc : studyRecTotalClassList) {
                if ("1".equals(param()._useCurriculumcd)) {
                    if (stc._classcd.equals(classcd) && stc._schoolKind.equals(schoolKind)) {
                        rtn = stc;
                        break;
                    }
                } else {
                    if (stc._classcd.equals(classcd)) {
                        rtn = stc;
                        break;
                    }
                }
            }
            return rtn;
        }

        private StudyrecTotalSpecialDiv getStudyrecTotalSpecialDiv(final String specialDiv, final List<StudyrecTotalSpecialDiv> studyRecTotalSpecialDivList) {
            StudyrecTotalSpecialDiv rtn = null;
            for (final StudyrecTotalSpecialDiv stc : studyRecTotalSpecialDivList) {
                if (stc._specialDiv.equals(specialDiv)) {
                    rtn = stc;
                    break;
                }
            }
            return rtn;
        }

        private static class StudyrecTotalSubclass {
            final String _classcd;
            final String _schoolKind;
            final String _curriculumCd;
            final String _subclasscd;
            final String _subclassname;
            final List<StudyRecSubclassTotal> _totals = new ArrayList();
            StudyrecTotalSubclass(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final String subclassname) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _curriculumCd = curriculumCd;
                _subclasscd = subclasscd;
                _subclassname = subclassname;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                boolean isAllDropped = true;
                for (final StudyRecSubclassTotal studyrecSubclassTotal : _totals) {
                    if (!studyrecSubclassTotal.isAllDropped()) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
        }

        private static class StudyrecTotalClass {
            final String _classcd;
            final String _schoolKind;
            final String _classname;
            final List<StudyrecTotalSubclass> _subclasses = new ArrayList();
            StudyrecTotalClass(final String classcd, final String schoolKind, final String classname) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _classname = classname;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                boolean isAllDropped = true;
                for (final StudyrecTotalSubclass studyrectotalSubclass : _subclasses) {
                    if (!studyrectotalSubclass.isAllDropped()) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
        }

        private static class StudyrecTotalSpecialDiv {
            final String _specialDiv;
            final List<StudyrecTotalClass> _classes = new ArrayList();
            StudyrecTotalSpecialDiv(final String specialDiv) {
                _specialDiv = specialDiv;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                boolean isAllDropped = true;
                for (final StudyrecTotalClass studyrectotalClass : _classes) {
                    if (!studyrectotalClass.isAllDropped()) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 学習の記録。
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001 ・データがない場合の不具合修正 --NO001
     * ・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加 --NO003 => 無い場合は従来通りHR_CLASSを出力
     * ・学年をすべて出力 --NO005
     */
    private static class KNJA130_3 extends KNJA130_0 {

        private int MAX_LINE_PER_PAGE = 47;

        private final KNJSvfFieldInfo _name = new KNJSvfFieldInfo(421, 1155, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 11.0, 1), 325, 295, 355, 24, 48);

        private String _formname;

        KNJA130_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm(final Student student, final PersonalInfo personalInfo, final boolean hasDrop, final boolean hasAbroad) {
            final String frm;
            frm = "KNJA130G_5.frm";
            _formname = frm;
            return frm;
        }

        private static Map<Integer, PrintGakuseki> getPagePrintGakusekiMap3(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final Param param) {
            int i = 1;
            boolean hasZeroprintflg = false;

            final Map<Integer, PrintGakuseki> grademap = new TreeMap();
            int page = 1;
            grademap.put(new Integer(page), new PrintGakuseki());
            final int max = 3; // param._isFormType3 ? 3 : 4;
            final String lastyear = String.valueOf(personalInfo.getLastYear());

            final List<Gakuseki> gakusekiStudyRecList = Student.createGakusekiStudyRec(db2, student, personalInfo._gakusekiList, personalInfo._studyRecList, param);
            boolean includeZaisekimae = false;
            for (final Gakuseki gakuseki : gakusekiStudyRecList) {
                if (!personalInfo.isTargetYear(gakuseki._year, param)) {
                    continue;
                }
                boolean zaisekimae = false;
                if ("1".equals(gakuseki._dataflg) && !param.isGdat(gakuseki._year, gakuseki._grade)) {
                    continue;
                } else if ("2".equals(gakuseki._dataflg)) {
                    if (!param._seitoSidoYorokuZaisekiMae) {
                        continue;
                    } else {
                        includeZaisekimae = true;
                        zaisekimae = true;
                    }
                }
                final int j = zaisekimae ? max : getGradeColumnNum(student, i, gakuseki, includeZaisekimae ? 1 : 0, param);

                final Integer iPage = new Integer(page);
                if (null == grademap.get(iPage)) {
                    grademap.put(iPage, new PrintGakuseki());
                }
                final PrintGakuseki printGakuseki = (PrintGakuseki) grademap.get(iPage);
                printGakuseki._yearList.add(gakuseki._year);
                printGakuseki._grademap.put(gakuseki._year, new Integer(j));
                printGakuseki._gakusekiMap.put(gakuseki._year, gakuseki);
                final boolean lastyearflg = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (zaisekimae) {
                } else if (!lastyearflg && gakuseki._isDrop) {
                    printGakuseki._dropGakuseki = gakuseki;
                    page += 1;
                    includeZaisekimae = false;
                    if (hasZeroprintflg) {
                        i = Integer.parseInt(gakuseki._grade);
                        hasZeroprintflg = false;
                    }
                } else if (!lastyearflg && max == i) {
                    page += 1;
                    includeZaisekimae = false;
                    i = 1;
                } else {
                    i++;
                }
                if (ANOTHER_YEAR.equals(gakuseki._year)) {
                    hasZeroprintflg = true; // 入学前を出力した。
                }
            }

            for (final PrintGakuseki printgakuseki : grademap.values()) {
                printgakuseki._lastyearFlg = printgakuseki._grademap.containsKey(lastyear);
            }
            return grademap;
        }

        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            final Map<Integer, PrintGakuseki> pagePringGakusekiMap = getPagePrintGakusekiMap3(db2, student, personalInfo, param());
            final List<Integer> pageList = new ArrayList(pagePringGakusekiMap.keySet());

            for (int printgakusekiIdx = 0; printgakusekiIdx < pageList.size(); printgakusekiIdx++) {
                final Integer page = pageList.get(printgakusekiIdx);
                final PrintGakuseki printgakuseki = pagePringGakusekiMap.get(page);
                printGakuseki3(student, personalInfo, printgakusekiIdx, printgakuseki);
            }
            hasdata = true;
        }

        private void printGakuseki3(final Student student, final PersonalInfo personalInfo, final int printgakusekiIdx, final PrintGakuseki printgakuseki) {
            final String form = getForm(student, personalInfo, null != printgakuseki._dropGakuseki, null != printgakuseki.getAbroadStudyRec(personalInfo._studyRecList));
            svfVrSetForm(form, 4);
            printGradeRecSub3(svf, student, personalInfo, student._schoolInfo);
//                log.debug(" print gradeMap = " + gradeMap);
            for (final String year : printgakuseki._grademap.keySet()) {
                final Integer ii = printgakuseki._grademap.get(year);
                final Gakuseki gakuseki = printgakuseki._gakusekiMap.get(year);
                printGradeRecDetail3(svf, ii.intValue(), gakuseki, student, personalInfo);
            }
            printStudyDetail3(student, personalInfo, printgakusekiIdx, printgakuseki);
        }

        /**
         * 学習の記録明細を印刷します。
         */
        private void printStudyDetail3(final Student student, final PersonalInfo personalInfo, final int printgakusekiIdx, final PrintGakuseki printgakuseki) {
            final Map studyRecSubclassMap = PersonalInfo.createStudyRecTotal(personalInfo._studyRecList, personalInfo._dropYears, param(), printgakusekiIdx, personalInfo._gakusekiList);
            final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || printgakuseki._lastyearFlg;
            final List<PrintLine> printLineList = createPrintLineList3(isPrintTotalCredits, student, personalInfo, printgakuseki._grademap, printgakusekiIdx, studyRecSubclassMap);
            final TreeSet<String> yearSet = new TreeSet(printgakuseki._grademap.keySet());
            final String minYear;
            final String maxYear;
            if (yearSet.isEmpty()) {
                minYear = null;
                maxYear = null;
            } else {
                minYear = yearSet.first();
                maxYear = yearSet.last();
            }

            for (final PrintLine printLine : printLineList) {
                svf.VrsOut("EDU_DIV", printLine._edudiv);
                svf.VrsOut("EDU_DIV_2", printLine._edudiv_2);
                svf.VrsOut("EDU_DIV2", printLine._specialDiv);
                svf.VrsOut("CLASSNAME", printLine._classname);

                // グループ化処理
                final String slinex = String.valueOf(printLine._linex);
                svf.VrsOut("SUBCLASSNAME_GRP", slinex);
                for (int j = 0; j < 4; j++) {
                    svf.VrsOut("GRADES" + String.valueOf(j + 1) + "_GRP", slinex);
                    svf.VrsOut("CREDIT" + String.valueOf(j + 1) + "_GRP", slinex);
                }
                svf.VrsOut("biko_grp", slinex);

                String sfx, sfx2;
//                if (param()._isTokiwa && !isNewForm(param(), student, personalInfo)) {
//                    sfx = printLine._isClassFirstLine ? "1" : "2";
//                    svf.VrsOut("SUBCLASSNAME" + sfx + "_" + (getMS932ByteLength(printLine._subClassName) > 20 ? "2" : "1"), printLine._subClassName);
//                    svf.VrsOut("GRP" + sfx, printLine._classcd); // 教科コード
//                    sfx2 = sfx;
//                    sfx = "_" + sfx;
//                } else {
                    sfx = "";
                    sfx2 = sfx;
                    if (printLine._bikoi <= 0) {
                        svf.VrsOut("SUBCLASSNAME", printLine._subClassName);
                    }
                    svf.VrsOut("CLASSNAME2", printLine._classcd); // 教科コード
//                }
                if (printLine._bikoi <= 0) {
                    for (final Integer col : printLine._gradesMap.keySet()) {
                        final String grades = printLine._gradesMap.get(col);
                        if (null != grades && !NumberUtils.isDigits(grades)) {
                            svf.VrAttribute("GRADES" + col.toString() + sfx, "Hensyu=3"); // 中央割付
                        }
                        svf.VrsOut("GRADES" + col.toString() + sfx, grades); // 評定
                    }
                    for (final Integer col : printLine._creditsMap.keySet()) {
                        final String credits = printLine._creditsMap.get(col);
                        svf.VrsOut("CREDIT" + col.toString() + sfx, credits); // 単位
                    }
                    if (null != printLine._totalCredits) {
                        svf.VrsOut("CREDIT" + sfx, printLine._totalCredits); // 科目別修得単位数
                    }
                }
                if (printLine._biko != null && printLine._biko.length() != 0) {
                    printBiko(printLine, student, personalInfo, sfx2, printLine._bikoi);
                }
                final String gakushuBiko90 = personalInfo._gakushuBiko.getStudyrecBiko(_90, minYear, maxYear).toString();
                final String rishuTaniBiko90 = personalInfo._gakushuBiko.getRisyuTanniBiko(_90, minYear, maxYear).toString();
                final String studyrecSubstitutionBiko90Zenbu = personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ZENBU, minYear, maxYear).toString();
                final String studyrecSubstitutionBiko90Ichibu =personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ICHIBU, minYear, maxYear).toString();
                final String studyrecSubstitutionBiko90 = studyrecSubstitutionBiko90Zenbu + studyrecSubstitutionBiko90Ichibu;
                final String[] bikoArray = new String[] {rishuTaniBiko90, gakushuBiko90, studyrecSubstitutionBiko90};
                final StringBuffer biko = new StringBuffer();
                String touten = "";
                for (int i = 0; i < bikoArray.length; i++) {
                    if (!StringUtils.isBlank(bikoArray[i])) {
                        biko.append(touten).append(bikoArray[i]);
                        touten = "、";
                    }
                }
                printYearCredits3(biko.toString(), student, personalInfo, printgakuseki);
                if (isPrintTotalCredits) {
                    printTotalCredits3(student, personalInfo, printgakuseki._grademap);
                }

                printGradeRecSub3(svf, student, personalInfo, student._schoolInfo);
                svf.VrEndRecord();
                hasdata = true;
            }
        }

        private void printBiko(final PrintLine printLine, final Student student, final PersonalInfo personalInfo, final String sfx, final int bikoi) {
            final int length = getMS932ByteLength(printLine._biko);
            if (bikoi == -1) {
                for (int j = 1; j <= 5; j++) {
                    svf.VrsOut("biko" + j, ""); // クリア処理
                }
                final int i;
                if (length <= 40) {
                    i = 1; // 40桁フィールド
                } else if (length <= 60) {
                    i = 2; // 60桁フィールド
                } else if (length <= 80) {
                    i = 3; // 80桁フィールド
                } else if (length <= 100) {
                    i = 4; // 100桁フィールド
                } else {
                    i = 5; // 240桁フィールド
                }
                svf.VrsOut("biko" + i, printLine._biko);
            } else {
                for (int j = 1; j <= 5; j++) {
                    svf.VrsOut("biko" + j, ""); // クリア処理
                }
                svf.VrsOut("biko" + printLine._bikoField, printLine._biko);
            }
        }

        private boolean yearBetween(final String year, final String min, final String max) {
            if (!NumberUtils.isDigits(year)) {
                return false;
            }
            if (!NumberUtils.isDigits(min) || Integer.parseInt(year) < Integer.parseInt(min)) {
                return false;
            }
            if (!NumberUtils.isDigits(max) || Integer.parseInt(max) < Integer.parseInt(year)) {
                return false;
            }
            return true;
        }

        private List<PrintLine> createPrintLineList3(final boolean isPrintTotalCredits,
                final Student student,
                final PersonalInfo personalInfo,
                final Map<String, Integer> grademap,
                final int printgakusekiIdx,
                final Map studyRecSubclassMap) {

            final TreeSet<String> yearSet = new TreeSet(grademap.keySet());
            int linex = 0; // 行数
            String specialDiv = "";
            final List<PrintLine> printLineList = new ArrayList();
            if (yearSet.isEmpty()) {
            } else {
                final String minYear = yearSet.first();
                final String maxYear = yearSet.last();
                final int iMinYear = Integer.parseInt(minYear);

                final List<StudyRec> studyrecList = new ArrayList();
                for (final StudyRec studyrec : personalInfo._studyRecList) {
                    final boolean isPrintAnotherStudyrec3 = param()._printAnotherStudyrec3 == Param._printAnotherStudyrec3_1 || param()._printAnotherStudyrec3 == Param._printAnotherStudyrec3_2 && student.isTanniSei(param());
                    if (isPrintAnotherStudyrec3 && 0 == printgakusekiIdx && NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) < iMinYear) {
                        // 前籍校の成績を表示する
                    } else if (!yearSet.contains(studyrec._year)) {
                        continue;
                    }
                    studyrecList.add(studyrec);
                }
                final List<StudyrecSpecialDiv> studyrecSpecialDivList = getStudyrecSpecialDivList(studyrecList, param());

                for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                    specialDiv = studyrecSpecialDiv._specialDiv;
                    final List<String> specialName = toCharStringList(param().getSpecialDivName(isNewForm(param(), student, personalInfo), studyrecSpecialDiv._specialDiv));

                    int lineSpecialDiv = 0; // 普通・専門毎の行数
                    // 教科毎の表示
                    for (final StudyrecClass studyrecClass : studyrecSpecialDiv._studyrecClassList) {

                        // 総合的な学習の時間・留学は回避します。
                        if (_90.equals(studyrecClass._classcd)) {
                            for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {
                                final Map<String, List<StudyRec>> yearStudyrecListMap = studyrecSubClass.getYearStudyrecListMap();
                                for (final String year : yearStudyrecListMap.keySet()) {
                                    final List<StudyRec> yearStudyrecList = yearStudyrecListMap.get(year);

                                    for (final StudyRec studyrec : yearStudyrecList) {
                                        final String compVal = getRisyuTanniBiko(student, personalInfo, studyrec);
                                        if (!"".equals(compVal)) {
                                            personalInfo._gakushuBiko.putRisyuTanniBiko(_90, studyrec._year, compVal);
                                        }
                                    }
                                }
                            }
                            continue;
                        } else if (_ABROAD.equals(studyrecClass._className)) {
                            continue;
//                        } else if (_94.equals(studyrecClass._classcd) && param()._isTokiwa) {
//                            continue;
                        }
                        int lineClasscd = 0;
                        // 科目毎の表示
                        for (int subi = 0; subi < studyrecClass._studyrecSubclassList.size(); subi++) {
                            final StudyrecSubClass studyrecSubClass = (StudyrecSubClass) studyrecClass._studyrecSubclassList.get(subi);
                            final PrintLine printLine = new PrintLine(linex);
                            String biko = null;
                            Integer pageSubclassCredit = null;

                            final Map<String, List<StudyRec>> yearStudyrecListMap = studyrecSubClass.getYearStudyrecListMap();
                            for (final String year : yearStudyrecListMap.keySet()) {
                                if (!personalInfo.isTargetYearLast(year, student, param())) {
                                    continue;
                                }
                                final List<StudyRec> yearStudyrecList = yearStudyrecListMap.get(year);

                                List gradesList = new ArrayList();
                                List creditList = new ArrayList();
                                List compCreditList = new ArrayList();
                                boolean isMirisyu = false;

                                for (final StudyRec studyrec : yearStudyrecList) {

                                    final String keysubclasscd = studyrecSubClass.studyrec().getKeySubclasscd(param());

                                    biko = getBiko(student, personalInfo, minYear, maxYear, studyrec, keysubclasscd);

                                    if (null != studyrec._grades) {
                                        gradesList.add(studyrec._grades);
                                    }
                                    if (null != studyrec._credit) {
                                        creditList.add(studyrec._credit);
                                    }
                                    if (null != studyrec._compCredit) {
                                        compCreditList.add(studyrec._compCredit);
                                    }
                                    isMirisyu = studyrec.isMirisyu(param());
                                }
                                Integer grades = max(gradesList);
                                Integer credit = sum(creditList);
                                Integer compCredit = sum(compCreditList);

                                // 学年ごとの出力
                                final Integer column = (Integer) grademap.get(year);
                                if (null != column && column.intValue() != 0) {
                                    final int intColumn = column.intValue();
                                    if (param()._optionCreditOutput == Param.OPTION_CREDIT1 && isMirisyu) {
                                        hasdata = true;
                                    } else {
                                        if (null == grades) {
//                                            if (param()._isTottori && null != credit && null == grades) {
//                                                printLine.setGrades(intColumn, "-");
//                                                nonedata = true;
//                                            }
                                        } else {
                                            if ("1".equals(param()._seitoSidoYorokuHyotei0ToBlank) && grades.intValue() == 0) {
                                            } else {
                                                printLine.setGrades(intColumn, grades.toString());
                                            }
                                            hasdata = true;
                                        }
                                        if (null != credit) {
                                            Integer setCredit = null;
                                            if (param()._optionCreditOutput == Param.OPTION_CREDIT2) {
                                                if (credit.intValue() == 0 && (null == compCredit || null != compCredit && compCredit.intValue() == 0)) {
                                                    // 空欄
                                                    setCredit = null;
                                                } else {
                                                    setCredit = credit;
                                                }
                                            } else {
                                                setCredit = credit;
                                            }
                                            if (null != setCredit) {
                                                printLine.setCredit(intColumn, setCredit.toString());
                                                if (yearBetween(year, minYear, maxYear) && !personalInfo._dropYears.contains(year)) {
                                                    pageSubclassCredit = null == pageSubclassCredit ? setCredit : new Integer(pageSubclassCredit.intValue() + setCredit.intValue());
                                                }
                                                hasdata = true;
                                            }
                                        }
                                    }
                                }
                            }
                            printLine._subClassName = studyrecSubClass.studyrec()._subClassName;

                            if (isPrintTotalCredits) {
                                if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                                    printLine._totalCredits = null == pageSubclassCredit ? null : pageSubclassCredit.toString();
                                } else {

                                    final StudyRecSubclassTotal studytotal = (StudyRecSubclassTotal) studyRecSubclassMap.get(studyrecSubClass.studyrec().getKeySubclasscdForSubclassTotal(param()));
//                                  final Integer substitutionIchibuCredit = student._gakushuBiko.getStudyrecSubstitutionCredit(subclasscd, StudyrecSubstitutionContainer.ICHIBU);
                                    final boolean creditHasValue = null != studytotal && null != studytotal.credit() && studytotal.credit().intValue() != 0;
//                                  final boolean substitutionIchibuCreditHasValue = null != substitutionIchibuCredit && substitutionIchibuCredit.intValue() != 0;
//                                  if (creditHasValue || substitutionIchibuCreditHasValue) {
                                    if (creditHasValue) {
//                                      final int credit = (creditHasValue ? studyobj._credit.intValue() : 0) + (substitutionIchibuCreditHasValue ? substitutionIchibuCredit.intValue() : 0);
                                        final String credit = (creditHasValue ? String.valueOf(studytotal.credit().intValue()) : null);
                                        printLine._totalCredits = credit;
                                    }
                                }
                            }

                            final List sameSubclassPrintLine = new ArrayList();
//                            if (param()._isTokiwa && !isNewForm(param(), student, personalInfo)) {
//                                printLine._biko = biko;
//                                sameSubclassPrintLine.add(printLine);
//                            } else {
                                final List bikoLines;
                                if (true) { // !_remarkFukusugyoFormList.contains(_formname)) {
                                    bikoLines = Arrays.asList(new String[] { biko });
                                    final PrintLine l = (PrintLine) printLine.clone();
                                    l._bikoi = -1;
                                    l._biko = (String) bikoLines.get(0);
                                    sameSubclassPrintLine.add(l);
//                                } else {
//                                    final int keta = getMS932ByteLength(biko);
//                                    if (keta <= 40) {
//                                        printLine._bikoField = "1"; // 40桁
//                                        bikoLines = Arrays.asList(new String[] { biko });
//                                    } else if (keta <= 60) {
//                                        printLine._bikoField = "2"; // 60桁
//                                        bikoLines = Arrays.asList(new String[] { biko });
//                                    } else {
//                                        printLine._bikoField = "2"; // 60桁
//                                        bikoLines = retDividString(biko, 60, -1);
//                                    }
//                                    for (int bi = 0; bi < bikoLines.size(); bi++) {
//                                        final PrintLine l = (PrintLine) printLine.clone();
//                                        l._bikoi = bi;
//                                        l._biko = (String) bikoLines.get(bi);
//                                        sameSubclassPrintLine.add(l);
//                                    }
                                }
//                            }
                            for (int bi = 0; bi < sameSubclassPrintLine.size(); bi++) {
                                final PrintLine l = (PrintLine) sameSubclassPrintLine.get(bi);
//                                if (param()._isTokiwa && !isNewForm(param(), student, personalInfo)) {
//                                    if (lineClasscd == 0) {
//                                        l._classname = studyrecClass._className;
//                                    }
//                                } else {
                                    if (lineClasscd < studyrecClass._className.length()) {
                                        l._classname = studyrecClass._className.substring(lineClasscd, lineClasscd + 1);
                                    }
//                                }
                                l._classcd = studyrecClass._classcd;
                                lineClasscd++;
                                if (specialName.size() > 0) {
                                    l._edudiv = (String) specialName.remove(0);
                                    if (specialName.size() > 0) {
                                        l._edudiv_2 = (String) specialName.remove(0);
                                    }
                                }
                                l._specialDiv = specialDiv;
                                l._isClassFirstLine = subi == 0 && bi == 0;
                                lineSpecialDiv++;
                                printLineList.add(l);
                                linex++;
                            }
                        }

//                        if (param()._isTokiwa && !isNewForm(param(), student, personalInfo)) {
//                        } else {
                            boolean outputNokori = false;
                            while (lineClasscd < studyrecClass._className.length()) {
                                final PrintLine printLine = new PrintLine(linex);
                                if (specialName.size() > 0) {
                                    printLine._edudiv = (String) specialName.remove(0);
                                    if (specialName.size() > 0) {
                                        printLine._edudiv_2 = (String) specialName.remove(0);
                                    }
                                }
                                printLine._specialDiv = specialDiv;
                                lineSpecialDiv++;
                                printLine._classcd = studyrecClass._classcd;
                                printLine._classname = studyrecClass._className.substring(lineClasscd, lineClasscd + 1);
                                lineClasscd++;
                                printLineList.add(printLine);
                                hasdata = true;
                                linex++;
                                if (linex == MAX_LINE_PER_PAGE) {
                                    linex = 0;
                                } // 行のオーバーフロー
                                outputNokori = true;
                            }
                            if (!outputNokori) {
                                final PrintLine printLine = new PrintLine(linex);
                                boolean outputName = false;
                                if (specialName.size() > 0) {
                                    printLine._edudiv = (String) specialName.remove(0);
                                    if (specialName.size() > 0) {
                                        printLine._edudiv_2 = (String) specialName.remove(0);
                                    }
                                    outputName = true;
                                }
                                lineSpecialDiv++;
                                if (outputName || linex != MAX_LINE_PER_PAGE) {
                                    printLine._specialDiv = specialDiv;
                                    printLine._classcd = studyrecClass._classcd;
                                    printLineList.add(printLine);
                                    hasdata = true;
                                    linex++;
                                    if (linex == MAX_LINE_PER_PAGE) {
                                        linex = 0;
                                    } // 行のオーバーフロー
                                }
                            }
//                        }
                    }

//                    if (param()._isTokiwa && !isNewForm(param(), student, personalInfo)) {
//                    } else {
                        while (specialName.size() > 0) {
                            final PrintLine printLine = new PrintLine(linex);
                            printLine._edudiv = (String) specialName.remove(0);
                            if (specialName.size() > 0) {
                                printLine._edudiv_2 = (String) specialName.remove(0);
                            }
                            printLine._specialDiv = specialDiv;
                            printLineList.add(printLine);
                            lineSpecialDiv++;
                            linex++;
                        }
//                    }
                    if (DEBUG) {
                        log.fatal(" risyu tanni    biko = " + personalInfo._gakushuBiko._biko.get(GakushuBiko.RISHU));
                        log.fatal(" studyrec       biko = " + personalInfo._gakushuBiko._biko.get(GakushuBiko.STUDY));
                        log.fatal(" studyrec subst biko = " + personalInfo._gakushuBiko._biko.get(GakushuBiko.SUBST));
                    }
                }
            }

//            boolean isfirst = true;
            for (int i = linex == 0 ? 0 : linex % MAX_LINE_PER_PAGE == 0 ? MAX_LINE_PER_PAGE : linex % MAX_LINE_PER_PAGE; i < MAX_LINE_PER_PAGE; i++) {
                final PrintLine printLine = new PrintLine(i);
                printLine._specialDiv = specialDiv;
                printLine._classcd = "00";
//                if (param()._isTokiwa && !isNewForm(param(), student, personalInfo) && isfirst) {
//                    printLine._isClassFirstLine = true;
//                }
                printLineList.add(printLine);
                hasdata = true;
//                isfirst = false;
            }
            return printLineList;
        }

        private String getBiko(final Student student, final PersonalInfo personalInfo, final String minYear, final String maxYear, final StudyRec studyrec, final String keysubclasscd0) {
            final String compVal = getRisyuTanniBiko(student, personalInfo, studyrec);
            if (!"".equals(compVal)) {
                personalInfo._gakushuBiko.putRisyuTanniBiko(keysubclasscd0, studyrec._year, compVal);
            }
            final List<String> keysubclasscds = new ArrayList();
            keysubclasscds.add(keysubclasscd0);
            final StringBuffer gakusyuubiko = new StringBuffer();
            final StringBuffer substitutionBikoZenbu = new StringBuffer();
            final StringBuffer substitutionBikoIchibu = new StringBuffer();
            final StringBuffer rishuTanniBiko = new StringBuffer();
            for (int i = 0; i < keysubclasscds.size(); i++) {
                final String keysubclasscd = keysubclasscds.get(i);
                gakusyuubiko.append(personalInfo._gakushuBiko.getStudyrecBiko(keysubclasscd, minYear, maxYear));
                substitutionBikoZenbu.append(personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ZENBU, minYear, maxYear));
                substitutionBikoIchibu.append(personalInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ICHIBU, minYear, maxYear));
                rishuTanniBiko.append(personalInfo._gakushuBiko.getRisyuTanniBiko(keysubclasscd, minYear, maxYear));
            }
            final List<String> bikoArray = Arrays.asList(rishuTanniBiko.toString(), gakusyuubiko.toString(), substitutionBikoZenbu.toString() + substitutionBikoIchibu.toString());
            return mkString(bikoArray, "、").toString();
        }

        private String getRisyuTanniBiko(final Student student, final PersonalInfo personalInfo, final StudyRec studyrec) {
            final String rtn;
            final String head = getRisyuTanniBikoHead(student, personalInfo, studyrec); // 第○学年 or ○○○年度
            if (studyrec.isMirisyu(param())) {
                // 未履修の場合の備考処理
                if (null != param()._mirisyuRemarkFormat) {
                    final String compCre = studyrec._compCredit.toString();
                    rtn = formatRemark(param()._mirisyuRemarkFormat, head, compCre);
                } else {
                    rtn = head + "履修不認定";
                }
            } else if (studyrec.isRisyuNomi(param())) {
                // 履修のみの場合の備考処理
                if (null != param()._risyunomiRemarkFormat) {
                    final String compCre = studyrec._compCredit.toString();
                    rtn = formatRemark(param()._risyunomiRemarkFormat, head, compCre);
//                } else if (param()._isKyoto) {
//                    final String compCre = studyrec._compCredit.toString();
//                    rtn = compCre + "単位履修認定";
                } else {
                    final String compCre = studyrec._compCredit.toString();
                    rtn = head + "履修単位数(" + compCre + ")";
                }
            } else {
                rtn = "";
            }
            return rtn;
        }

        private static String formatRemark(final String format, final String gakunenNendo, final String compCre) {
            String tmp = format;
            tmp = StringUtils.replace(tmp, "x", gakunenNendo);
            tmp = StringUtils.replace(tmp, "y", compCre);
            return tmp;
        }

        private String getRisyuTanniBikoHead(final Student student, final PersonalInfo personalInfo, final StudyRec studyrec) {
            final Gakuseki gakuseki = personalInfo.getGakuseki(studyrec._year, studyrec._annual);
            if (gakuseki == null) {
                return null;
            }
            final String head = student.isKoumokuGakunen(param()) ? gakuseki._gradeName2 : gakuseki.getNendo2(param());
            return null == head ? "" : head;
        }

        /**
         * 年度・学年別修得単位数を印字します。（総合的な学習の時間・小計・留学・合計）
         * @param gakusyuubikomap
         * @param substitutionbiko90
         * @param lastyearflg
         * @param studyrecyear
         * @param grademap
         */
        private void printYearCredits3(final String biko, final Student student, final PersonalInfo personalInfo, final PrintGakuseki printgakuseki) {
            final Map studyrecyear = student.getStudyRecYear(param(), isNewForm(param(), student, personalInfo), personalInfo);
            for (final String year : printgakuseki._grademap.keySet()) {
                if (!personalInfo.isTargetYearLast(year, student, param())) {
                    continue;
                }
                final int col = printgakuseki._grademap.get(year).intValue();
                final StudyRecYearTotal yearTotal = (StudyRecYearTotal) studyrecyear.get(year);
                if (null != yearTotal && !yearTotal.subject90(0).isEmpty()) {
                    svf.VrsOut("tani_" + col + "_sgj", sum(yearTotal.subject90(0)).toString());
                } else { // 札幌開成は0
                    svf.VrsOut("tani_" + col + "_sgj", "0");
                }
                if (null != yearTotal && (yearTotal._isDrop == StudyRecYearTotal.DROP || yearTotal._isDrop == StudyRecYearTotal.DROP_SHOW)) {
                    continue;
                }
                if (null != yearTotal && !yearTotal.abroad().isEmpty()) {
                    svf.VrsOut("tani_" + col + "_rg", sum(yearTotal.abroad()).toString());
                } else { // if (!(param()._isKyoto || param()._isTokiwa)) {
                    svf.VrsOut("tani_" + col + "_rg", "0");
                }
                final int yoshiki = 3;
                if (null != yearTotal && !yearTotal.subject(0, param(), yoshiki).isEmpty()) {
                    svf.VrsOut("tani_" + col + "_sk", sum(yearTotal.subject(0, param(), yoshiki)).toString());
                //} else if (null != yearTotal && student.isShowCredit0(param(), personalInfo, yearTotal._year)) {
                } else { // 札幌開成は0
                    svf.VrsOut("tani_" + col + "_sk", "0");
                }
                if (null != yearTotal && !yearTotal.total(0).isEmpty()) {
                    svf.VrsOut("tani_" + col + "_gk", sum(yearTotal.total(0)).toString());
                //} else if (null != yearTotal && student.isShowCredit0(param(), personalInfo, yearTotal._year)) {
                } else { // 札幌開成は0
                    svf.VrsOut("tani_" + col + "_gk", "0");
                }
                if (null != yearTotal && !yearTotal.subject94(0, param()).isEmpty()) {
                    svf.VrsOut("tani_" + col + "_hr", sum(yearTotal.subject94(0, param())).toString());
                } else if (null != yearTotal && student.isShowCredit0(param(), personalInfo, yearTotal._year)) {
                    svf.VrsOut("tani_" + col + "_hr", "0");
                }
            }
            final int bikoKeta = getMS932ByteLength(biko);
            final int keta1 = 40;
            final int keta2 = "KNJA130C_13TOKIWA.frm".equals(_formname) ? 78 : 60;
            if (bikoKeta <= keta1) {
                svf.VrsOut("biko_sgj", biko);
            } else if (bikoKeta <= keta2) {
                svf.VrsOut("biko_sgj2", biko);
            } else {
                svf.VrsOut("biko_sgj3", biko);
            }
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits3(final Student student, final PersonalInfo personalInfo, final Map grademap) {

            svf.VrsOut("TOTAL_SUBCLASSNAME", personalInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(Param.SCHOOL_KIND_H, personalInfo._gakusekiList)));

            final TreeSet yearSet = new TreeSet(grademap.keySet());

            final List subject90s = new ArrayList();
            final List subjects = new ArrayList();
            final List abroads = new ArrayList();
            final List totals = new ArrayList();
            final List subject94s = new ArrayList();

            final int yoshiki = 3;
            for (final Iterator it = student.getStudyRecYear(param(), isNewForm(param(), student, personalInfo), personalInfo).values().iterator(); it.hasNext();) {
                final StudyRecYearTotal yearTotal = (StudyRecYearTotal) it.next();
                if (yearTotal._isDrop == StudyRecYearTotal.DROP || yearTotal._isDrop == StudyRecYearTotal.DROP_SHOW) {
                    continue;
                }
                if (!personalInfo.isTargetYearLast(yearTotal._year, student, param())) {
                    continue;
                }
                if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                    if (!yearSet.contains(yearTotal._year)) {
                        continue;
                    }
                }
                subject90s.addAll(yearTotal.subject90(0));
                subjects.addAll(yearTotal.subject(0, param(), yoshiki));
                abroads.addAll(yearTotal.abroad());
                totals.addAll(yearTotal.total(0));
                subject94s.addAll(yearTotal.subject94(0, param()));
            }

            int intColumn = 5;
            if (!subject90s.isEmpty()) {
                svf.VrsOut("tani_" + intColumn + "_sgj", String.valueOf(sum(subject90s)));
            } else if (student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear())) { // 札幌開成は0
                svf.VrsOut("tani_" + intColumn + "_sgj", "0");
            }
            if (!subjects.isEmpty()) {
                svf.VrsOut("tani_" + intColumn + "_sk", String.valueOf(sum(subjects)));
            } else if (student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear())) {
                svf.VrsOut("tani_" + intColumn + "_sk", "0");
            }
            if (!abroads.isEmpty()) {
                svf.VrsOut("tani_" + intColumn + "_rg", String.valueOf(sum(abroads)));
            } else { // if (!(param()._isKyoto || param()._isTokiwa)) {
                svf.VrsOut("tani_" + intColumn + "_rg", "0");
            }
            if (!totals.isEmpty()) {
                svf.VrsOut("tani_" + intColumn + "_gk", String.valueOf(sum(totals)));
            } else if (student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear())) {
                svf.VrsOut("tani_" + intColumn + "_gk", "0");
            }
            if (!subject94s.isEmpty()) {
                svf.VrsOut("tani_" + intColumn + "_hr", String.valueOf(sum(subject94s)));
            } else if (student.isShowCredit0(param(), personalInfo, personalInfo.getLastYear())) {
                svf.VrsOut("tani_" + intColumn + "_hr", "0");
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub3(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final SchoolInfo schoolinfo) {
            svf.VrsOut("GRADENAME1", "年次");

            printName(svf, personalInfo, "NAME1", "NAME2", "NAME3", _name._x2 - _name._x1, _name._height, _name._minnum, _name._maxnum, _name._ystart, _name._ystart1, _name._ystart2);

            if (null != schoolinfo) {
                if (!StringUtils.isBlank(schoolinfo._bunkouSchoolName)) {
                    svf.VrsOut("SCHOOLNAME2", schoolinfo._schoolName1);
                    svf.VrsOut("SCHOOLNAME3", "（" + schoolinfo._bunkouSchoolName + "）");

                } else if (null != schoolinfo._schoolName1) {
                    if (getMS932ByteLength(schoolinfo._schoolName1) > 30) {
                        final List<String> arr = retDividString(schoolinfo._schoolName1, 30, 2);
                        for (int i = 0; i < arr.size(); i++) {
                            svf.VrsOut("SCHOOLNAME" + (2 + i), arr.get(i));
                        }
                    } else {
                        svf.VrsOut("SCHOOLNAME1", schoolinfo._schoolName1);
                    }
                }
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail3(final Vrw32alp svf, final int i, final Gakuseki gakuseki, final Student student, final PersonalInfo personalInfo) {
            if (student.isKoumokuGakunen(param())) {
                svf.VrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
                svf.VrsOut("GRADE2_" + i, gakuseki._gradeName2);
            } else {
                svf.VrsOut("GRADE3_" + i, gakuseki._nendo);
                svf.VrsOut("GRADE2_" + i, gakuseki.getNendo2(param()));
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svf.VrsOut(hrClassField + i, gakuseki._hrname);
            svf.VrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        private List<StudyrecSpecialDiv> getStudyrecSpecialDivList(final List<StudyRec> studyrecList, final Param param) {
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = new ArrayList();
            for (final StudyRec studyrec : studyrecList) {
                if (null == studyrec._classcd || null == studyrec._subClasscd) {
                    continue;
                }
                final StudyrecSpecialDiv ssd = getStudyrecSpecialDiv(studyrecSpecialDivList, studyrec._specialDiv);
                final StudyrecClass sc = getStudyrecClass(ssd._studyrecClassList, studyrec._classcd, studyrec._schoolKind, studyrec._className, param);
                final StudyrecSubClass ssc = getStudyrecSubClass(sc._studyrecSubclassList, studyrec._classcd, studyrec._schoolKind, studyrec._curriculumCd, studyrec._subClasscd, param);
                ssc._studyrecList.add(studyrec);
            }
            return studyrecSpecialDivList;
        }

        private StudyrecSpecialDiv getStudyrecSpecialDiv(final List<StudyrecSpecialDiv> list, final String specialDiv) {
            StudyrecSpecialDiv studyrecSpecialDiv = null;
            for (final StudyrecSpecialDiv studyrecSpecialDiv0 : list) {
                if (specialDiv.equals(studyrecSpecialDiv0._specialDiv)) {
                    studyrecSpecialDiv = studyrecSpecialDiv0;
                    break;
                }
            }
            if (null == studyrecSpecialDiv) {
                studyrecSpecialDiv = new StudyrecSpecialDiv(specialDiv);
                list.add(studyrecSpecialDiv);
            }
            return studyrecSpecialDiv;
        }

        private StudyrecClass getStudyrecClass(final List<StudyrecClass> list, final String classcd, final String schoolKind, final String classname, final Param param) {
            StudyrecClass studyrecClass = null;
            for (final StudyrecClass studyrecClass0 : list) {
                if ("1".equals(param._useCurriculumcd)) {
                    if (classcd.equals(studyrecClass0._classcd) && schoolKind.equals(studyrecClass0._schoolKind)) {
                        studyrecClass = studyrecClass0;
                        break;
                    }
                } else {
                    if (classcd.equals(studyrecClass0._classcd)) {
                        studyrecClass = studyrecClass0;
                        break;
                    }
                }
            }
            if (null == studyrecClass) {
                studyrecClass = new StudyrecClass(classcd, schoolKind, StringUtils.defaultString(classname));
                list.add(studyrecClass);
            }
            return studyrecClass;
        }

        private StudyrecSubClass getStudyrecSubClass(final List<StudyrecSubClass> list, final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final Param param) {
            StudyrecSubClass studyrecSubClass = null;
            for (final StudyrecSubClass studyrecSubClass0 : list) {
                if ("1".equals(param._useCurriculumcd)) {
                    if (param._isSubclassOrderNotContainCurriculumcd) {
                        if (classcd.equals(studyrecSubClass0._classcd) && schoolKind.equals(studyrecSubClass0._schoolKind) && subclasscd.equals(studyrecSubClass0._subclasscd)) {
                            studyrecSubClass = studyrecSubClass0;
                            break;
                        }
                    } else {
                        if (classcd.equals(studyrecSubClass0._classcd) && schoolKind.equals(studyrecSubClass0._schoolKind) && curriculumCd.equals(studyrecSubClass0._curriculumCd) && subclasscd.equals(studyrecSubClass0._subclasscd)) {
                            studyrecSubClass = studyrecSubClass0;
                            break;
                        }
                    }
                } else {
                    if (subclasscd.equals(studyrecSubClass0._subclasscd)) {
                        studyrecSubClass = studyrecSubClass0;
                        break;
                    }
                }
            }
            if (null == studyrecSubClass) {
                studyrecSubClass = new StudyrecSubClass(classcd, schoolKind, curriculumCd, subclasscd);
                list.add(studyrecSubClass);
            }
            return studyrecSubClass;
        }

        private static class PrintLine {
            final int _linex;
            String _edudiv;
            String _edudiv_2;
            String _specialDiv;
            String _classcd;
            String _classname;
            String _subClassName;
            String _biko;
            String _totalCredits;
            boolean _isClassFirstLine;
            final Map<Integer, String> _creditsMap = new HashMap();
            final Map<Integer, String> _gradesMap = new HashMap();
            int _bikoi;
            String _bikoField;

            private PrintLine(final int linex) {
                _linex = linex;
            }

            public void setCredit(final int colno, final String credits) {
                _creditsMap.put(new Integer(colno), credits);
            }

            public void setGrades(final int colno, final String grades) {
                _gradesMap.put(new Integer(colno), grades);
            }

            public Object clone() {
                PrintLine l = new PrintLine(_linex);
                l._edudiv = _edudiv;
                l._edudiv_2 = _edudiv_2;
                l._specialDiv = _specialDiv;
                l._classcd = _classcd;
                l._classname = _classname;
                l._subClassName = _subClassName;
                l._bikoi = _bikoi;
                l._totalCredits = _totalCredits;
                l._isClassFirstLine = _isClassFirstLine;
                l._creditsMap.putAll(_creditsMap);
                l._gradesMap.putAll(_gradesMap);
                l._biko = _biko;
                l._bikoField = _bikoField;
                return l;
            }

            public String toString() {
                return "[" + String.valueOf(_linex) + " : " + _classcd + ":" + _classname + ":" + _subClassName + "]";
            }
        }

        private class StudyrecSpecialDiv {
            final String _specialDiv;
            final List<StudyrecClass> _studyrecClassList;
            public StudyrecSpecialDiv(final String specialDiv) {
                _specialDiv = specialDiv;
                _studyrecClassList = new ArrayList();
            }
        }

        private class StudyrecClass {
            final String _classcd;
            final String _schoolKind;
            final String _className;
            final List<StudyrecSubClass> _studyrecSubclassList;
            public StudyrecClass(final String classcd, final String schoolKind, final String classname) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _className = classname;
                _studyrecSubclassList = new ArrayList();
            }
        }

        private class StudyrecSubClass {
            final String _classcd;
            final String _schoolKind;
            final String _curriculumCd;
            final String _subclasscd;
            final List<StudyRec> _studyrecList;
            public StudyrecSubClass(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _curriculumCd = curriculumCd;
                _subclasscd = subclasscd;
                _studyrecList = new ArrayList();
            }
            private Map<String, List<StudyRec>> getYearStudyrecListMap() {
                final Map<String, List<StudyRec>> map = new HashMap();
                for (final StudyRec studyrec : _studyrecList) {
                    getMappedList(map, studyrec._year).add(studyrec);
                }
                return map;
            }
            private StudyRec studyrec() {
                return _studyrecList.get(_studyrecList.size() - 1);
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 活動の記録。
     * 高校生徒指導要録の指導に関する記録を印刷します。
     * ２.総合的な学習の時間の記録
     * ３.特別活動の記録
     * ４.総合所見及び指導上参考となる諸事項
     * ５.出欠の記録
     */
    private static class KNJA130_4 extends KNJA130_0 {

        private final KNJSvfFieldInfo _name = new KNJSvfFieldInfo(801, 1601, (int) KNJA130CCommon.KNJSvfFieldModify.charPointToPixel("", 11.0, 1), 282, 249, 315, 24, 48);

        KNJA130_4(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm(final Student student, final PersonalInfo personalInfo) {
            String form = "";
            form = "KNJA130G_6.frm";
            return form;
        }

        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            final Param param = param();
            final Map<Integer, PrintGakuseki> pagePrintGakusekiMap = getPagePrintGakusekiMap(db2, student, personalInfo, param);

            for (final Integer page : pagePrintGakusekiMap.keySet()) {
                final PrintGakuseki printGakuseki = pagePrintGakusekiMap.get(page);
                printPage(student, personalInfo, param, printGakuseki);
            }
        }

        private void printPage(final Student student, final PersonalInfo personalInfo, final Param param, final PrintGakuseki printGakuseki) {
            final String form = getForm(student, personalInfo);
            svfVrSetForm(form, 1);

            svf.VrsOut("SOGO_TITLE", personalInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(Param.SCHOOL_KIND_H, personalInfo._gakusekiList)) + "の記録");

            printGradeRecSub(svf, student, personalInfo);

            // 総合的な学習の時間の記録は１つの欄に年度ごとのデータを連結して出力する
            boolean isPrintSogoConcateHtrainremark = false;
            if (isPrintSogoConcateHtrainremark) {
                log.info("総合的な学習の時間の記録は１つの欄に年度ごとのデータを連結して出力する");
            }

            final List studyRecSubstitution90AllYear = new ArrayList();
            for (final String year0 : printGakuseki._yearList) {
                if (!personalInfo.isTargetYearLast(year0, student, param())) {
                    continue;
                }
                studyRecSubstitution90AllYear.addAll(personalInfo.getArraySubstitutionBiko90(year0, param));
            }

            if (isPrintSogoConcateHtrainremark) {
                printSogoAll(svf, student, personalInfo, studyRecSubstitution90AllYear, printGakuseki, student._htrainRemarkMap, param);
            }

            for (final String year : printGakuseki._gakusekiMap.keySet()) {

                final Gakuseki gakuseki = printGakuseki._gakusekiMap.get(year);
                final Integer i = printGakuseki._grademap.get(year);
                final int j = getGradeColumnNum(student, i.intValue(), gakuseki, 0, param());

                printGradeRecDetail(svf, student, j, gakuseki);
                if (!personalInfo.isTargetYearLast(gakuseki._year, student, param())) {
                    continue;
                }

                // 所見データを印刷
                final HtrainRemark remark = null == student._htrainRemarkMap.get(gakuseki._year) ? HtrainRemark.Null : student._htrainRemarkMap.get(gakuseki._year);
                //final HtrainRemarkDetail remarkDetail = (HtrainRemarkDetail) student._htrainRemarkDetailMap.get(gakuseki._year);

                if (isPrintSogoConcateHtrainremark) {
                } else {
                    final List studyRecSubstitution90Tannen;
                    if (student._isShowStudyRecBikoSubstitution90) {
                        studyRecSubstitution90Tannen = personalInfo.getArraySubstitutionBiko90(gakuseki._year, param);
                    } else {
                        studyRecSubstitution90Tannen = Collections.EMPTY_LIST;
                    }

                    printSogo(svf, student, studyRecSubstitution90Tannen, studyRecSubstitution90AllYear, remark, j, param);
                }

                //printHtrainRemark(svf, student, personalInfo, gakuseki, remark, remarkDetail, j, param);
                printHtrainRemark(svf, student, personalInfo, gakuseki, remark, j, param);

                // 出欠データを印刷
                printAttendRec(svf, student, personalInfo, gakuseki, j);
            }
            svf.VrEndPage();
            hasdata = true;
        }

        private static Map<Integer, PrintGakuseki> getPagePrintGakusekiMap(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final Param param) {
            int i = 1;
            boolean hasZeroPrintFlg = false;
            final int max = 3; // param._isFormType3 ? 3 : 4;

            int ipage = 1;
            final Map<Integer, PrintGakuseki> pageGakusekiListMap = new TreeMap();
            final List<Gakuseki> gakusekiAttendRecList = Student.createGakusekiAttendRec(db2, personalInfo._gakusekiList, student._attendRecMap, param);
            pageGakusekiListMap.put(new Integer(ipage), new PrintGakuseki());
            for (final Gakuseki gakuseki : gakusekiAttendRecList) {

                if (!(student.isGakunenSei(param) || param._isGenkyuRyuchi) && ANOTHER_YEAR.equals(gakuseki._year) || !param.isGdat(gakuseki._year, gakuseki._grade)) {
                    continue;
                }
                if (!personalInfo.isTargetYear(gakuseki._year, param)) {
                    continue;
                }

                final Integer page = new Integer(ipage);
                if (null == pageGakusekiListMap.get(page)) {
                    pageGakusekiListMap.put(page, new PrintGakuseki());
                }
                final PrintGakuseki printGakuseki = pageGakusekiListMap.get(page);
                printGakuseki._yearList.add(gakuseki._year);
                printGakuseki._grademap.put(gakuseki._year, new Integer(i));
                printGakuseki._gakusekiMap.put(gakuseki._year, gakuseki);

                // 留年以降を改ページします。
                if (gakuseki._isDrop) {
                    ipage += 1;
                    if (hasZeroPrintFlg) {
                        i = Integer.parseInt(gakuseki._grade);
                        hasZeroPrintFlg = false;
                    }
                } else if (max == i) {
                    ipage += 1;
                    i = 1;
                } else {
                    i++;
                }
                if (ANOTHER_YEAR.equals(gakuseki._year)) {
                    hasZeroPrintFlg = true;
                }
            }
            //log.debug(" pageGakusekiListMap = " + pageGakusekiListMap);
            return pageGakusekiListMap;
        }


        private void printSogoAll(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final List<String> studyRecSubstitution90AllYear, final PrintGakuseki printGakuseki, final Map htrainremarkMap, final Param param) {
            final List totalStudyActList = new ArrayList();
            final List totalStudyValList = new ArrayList();

            final int keta = 44 * 2; // param()._isTokiwa ? 45 * 2 : 44 * 2;
            final int actLines = 4; // param()._isTokiwa ? 6 : 4;
            final int valLines = 6;
            final TreeSet<String> keySet = new TreeSet(printGakuseki._gakusekiMap.keySet());
            for (final String year : keySet) {

                final Gakuseki gakuseki = printGakuseki._gakusekiMap.get(year);

                // 所見データを印刷
                final HtrainRemark remark = null == student._htrainRemarkMap.get(gakuseki._year) ? HtrainRemark.Null : student._htrainRemarkMap.get(gakuseki._year);

                if (!StringUtils.isBlank(remark._totalStudyAct)) {
                    totalStudyActList.add(remark._totalStudyAct);
                }
                if (!StringUtils.isBlank(remark._totalStudyVal)) {
                    totalStudyValList.add(remark._totalStudyVal);
                }
            }

            // 「総合的な学習の時間の記録」を印字
            if (student._isShowStudyRecBikoSubstitution90) {
                totalStudyActList.addAll(studyRecSubstitution90AllYear);
            }
            final String totalStudyAct = mkString(totalStudyActList, "\n").toString();
            final String totalStudyVal = mkString(totalStudyValList, "\n").toString();
            printSvfRenban(svf, "REC_1", totalStudyAct, keta, actLines);    // 「学習活動」の欄
            printSvfRenban(svf, "REC_2", totalStudyVal, keta, valLines);    // 「評価」の欄
        }

        private void printSogo(final Vrw32alp svf, final Student student, final List studyRecSubstitution90Tannen, final List studyRecSubstitution90AllYear, final HtrainRemark remark, final int j, final Param param) {
            if (param._isNendogoto) {

                    final String totalStudyAct = mkString(cons(remark._totalStudyAct, studyRecSubstitution90Tannen), "\n").toString();
                    final String totalStudyVal = remark._totalStudyVal;
                    final String field1, field2;
                    final int keta, gyo1, gyo2;

                    //学習活動：22文字×8行, 評価：22文字×8行
                    field1 = "rec_1_" + String.valueOf(j);
                    field2 = "rec_2_" + String.valueOf(j);
                    keta = 22 * 2;
                    gyo1 = 8;
                    gyo2 = 8;
                    printSvfRenban(svf, field1, totalStudyAct, keta, gyo1);   // 総合的な学習の時間学習活動
                    printSvfRenban(svf, field2, totalStudyVal, keta, gyo2);   // 総合的な学習の時間評価
//                }
            } else { // 「通年」か?
                // 「総合的な学習の時間の記録」を印字
                final String totalStudyAct = student._isShowStudyRecBikoSubstitution90 ? mkString(cons(student._htrainRemarkHdatAct, studyRecSubstitution90AllYear), "\n").toString() : student._htrainRemarkHdatAct;
                final String totalStudyVal = student._htrainRemarkHdatVal;
                printSvfRenban(svf, "REC_1", totalStudyAct, 44 * 2, 4);    // 「学習活動」の欄
                printSvfRenban(svf, "REC_2", totalStudyVal, 44 * 2, 6);    // 「評価」の欄
            }
        }

        private static List cons(final String s, final List list) {
            if (StringUtils.isBlank(s)) {
                return list;
            }
            if (list.isEmpty()) {
                return Collections.singletonList(s);
            }
            final List rtn = new ArrayList();
            rtn.add(s);
            rtn.addAll(list);
            return rtn;
        }

        /**
         * 所見等データを印刷します。
         */
        public void printHtrainRemark(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final Gakuseki gakuseki, final HtrainRemark remark, final int i, final Param param) {

            final String si = String.valueOf(i);
            // 特別活動
            final String fieldSpact;
            final int spActKeta;
            final int spActGyo;
            fieldSpact = "SPECIALACTREMARK_" + si;
            spActKeta = 22;
            spActGyo = 6;
            printSvfRenban(svf, fieldSpact, remark._specialActRemark, spActKeta, spActGyo);

            final String field;
            final int keta;
            final int gyo;
            if (param._seitoSidoYorokuSougouFieldSize) {
                field = "rec_4_" + si;
                keta = 132;
                gyo = 8;
            } else if ("1".equals(param._seitoSidoYorokuFieldSize)) {
                field = "rec_4_" + si;
                keta = 132;
                gyo = 7;
//            } else if (param()._isKyoto && param._seitoSidoYorokuKinsokuForm && kinsokuOver(remark._totalRemark, 88, 6)) {
//                field = "rec_5_" + si;
//                keta = 88;
//                gyo = 8;
            } else {
                field = "rec_3_" + si;
                keta = 88;
                gyo = 6;
            }
            printSvfRenban(svf, field, remark._totalRemark, keta, gyo);

            printSvfRenban(svf, "syuketu_8_" + si, remark._attendRecRemark, 40, 2);
        }

        /**
         * 所見等データを印刷します。
         * @param svf
         * @param field SVFフィールド名
         * @param data 編集元の文字列
         * @param keta 行当りの文字数（Byte)
         * @param gyo 行数
         * @param param
         * @param i
         */
        private void printSvfRenban(
                final Vrw32alp svf,
                final String field,
                final String data,
                final int keta,
                final int gyo
        ) {
            if (null == data) {
                return;
            }
            if (param()._seitoSidoYorokuKinsokuForm) {
                svf.VrsOut(field, crlfReplaced(data, param()));
            } else {
                final List<String> arrstr = retDividString(data, keta, gyo);
                for (int j = 0; j < arrstr.size(); j++) {
                    if (null == arrstr.get(j)) {
                        continue;
                    }
                    svf.VrsOutn(field, j + 1, arrstr.get(j));
                }
            }
        }

        // TAKAESU: 上記メソッドと同一視可能
        private void printAttendRec(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final Gakuseki gakuseki, int j) {
            final AttendRec attendrec = (AttendRec) student._attendRecMap.get(gakuseki._year);
            if (null != attendrec) {
                svf.VrsOut("syuketu_1_" + j, AttendRec.getString(attendrec._attend_1));// 授業日数
                // 忌引日数
                final String field;
                field = "SUSPEND"    + j;
                svf.VrsOut(field, AttendRec.getString(attendrec.suspendMourning()));
                svf.VrsOut("syuketu_4_" + j, AttendRec.getString(attendrec._abroad));// 留学日数
                svf.VrsOut("syuketu_5_" + j, AttendRec.getString(attendrec._requirepresent));// 要出席日数
                svf.VrsOut("syuketu_6_" + j, AttendRec.getString(attendrec._attend_6));// 欠席日数
                svf.VrsOut("syuketu_7_" + j, AttendRec.getString(attendrec._present));// 出席日数
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo) {
            svf.VrsOut("GRADENAME1", "年次");

            printName(svf, personalInfo, "NAME1", "NAME2", "NAME3", _name._x2 - _name._x1, _name._height, _name._minnum, _name._maxnum, _name._ystart, _name._ystart1, _name._ystart2);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(
                final Vrw32alp svf,
                final Student student,
                final int i,
                final Gakuseki gakuseki
        ) {
            final boolean enableYear = (null != gakuseki._year) && (0 != Integer.parseInt(gakuseki._year));
            if (student.isKoumokuGakunen(param())) {

                if (enableYear) {
                    svf.VrsOut("GRADE4_" + i, gakuseki._gakunenSimple);
                    svf.VrsOut("GRADE3_" + i + "_2", gakuseki._gakunenSimple);
                } else {
                    svf.VrsOut("GRADE3_" + i + "_2", gakuseki._nendo);
                }

                svf.VrsOut("GRADE1_" + i, gakuseki._gradeName2);    // 特別活動の記録
                svf.VrsOut("GRADE2_" + i, gakuseki._gradeName2);    // 総合所見及び...
                svf.VrsOut("GRADE5_" + i, gakuseki._gradeName2);    // 総合的な学習の時間の記録

                VrsOutn("SP_GRADE", i, gakuseki._gradeName2);    // 明治専用 Catholic Spiritの評価
            } else {
                if (enableYear) {
                    svf.VrsOut("GRADE3_" + i, gakuseki._nendo);
                    final String[] nendoArray = gakuseki.nendoArray(param());
                    svf.VrsOut("GRADE3_" + i + "_1", nendoArray[0]);
                    svf.VrsOut("GRADE3_" + i + "_2", nendoArray[1]);
                    svf.VrsOut("GRADE3_" + i + "_3", nendoArray[2]);
                } else {
                    svf.VrsOut("GRADE3_" + i + "_2", gakuseki._nendo);
                }

                svf.VrsOut("GRADE1_" + i, gakuseki._nendo); // 特別活動の記録
                svf.VrsOut("GRADE2_" + i, gakuseki._nendo); // 総合所見及び...
                svf.VrsOut("GRADE5_" + i, gakuseki._nendo); // 総合的な学習の時間の記録

                VrsOutn("SP_GRADE", i, gakuseki._nendo);    // 明治専用 Catholic Spiritの評価
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svf.VrsOut(hrClassField + i, gakuseki._hrname);
            svf.VrsOut("ATTENDNO_" + i, gakuseki._attendno);
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
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
            _late = late;
            _early = early;
        }

        private static PreparedStatement getPreparedStatement(final DB2UDB db2, final Param param, final String schregno) throws SQLException {
            if (null == param._psMap.get("PS_ATTEND")) {
                final String sql = getAttendSql(param);
                param._psMap.put("PS_ATTEND", db2.prepareStatement(sql));
            }
            int p = 0;
            PreparedStatement ps = param._psMap.get("PS_ATTEND");
            ps.setString( ++p, schregno);    //学籍番号
            ps.setString( ++p, param._year);
            return ps;
        }

        /**
         *  年次ごとの出欠データのリストを得る
         */
        public static List<Attend> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<Attend> attendRecordList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = getPreparedStatement(db2, param, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    final int g = Integer.parseInt(rs.getString("ANNUAL"));
                    final String lesson = rs.getString("LESSON");
                    final String suspendMourning = rs.getString("SUSPEND_MOURNING");
                    final String abroad = rs.getString("ABROAD");
                    final String requirePresent = rs.getString("REQUIREPRESENT");
                    final String present = rs.getString("PRESENT");
                    final String absent = rs.getString("ABSENT");
                    final String late = rs.getString("LATE");
                    final String early = rs.getString("EARLY");

                    final Attend attendRecord = new Attend(year, g, lesson, suspendMourning, abroad, requirePresent, present, absent, late, early);
                    attendRecordList.add(attendRecord);
                }
            } catch (Exception ex) {
                log.error("printSvfAttendRecord error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return attendRecordList;
        }

        /**
         *  priparedstatement作成  出欠の記録
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String getAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        SCHREG_ATTENDREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("        AND YEAR <= ? ");
            stb.append("        AND T1.ANNUAL in " + param._gradeInChugaku + " ");
            stb.append("   GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR, ");
            stb.append("        T1.ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
                stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
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
            stb.append(        "VALUE(LATE,0) AS LATE, ");
            stb.append(        "VALUE(EARLY,0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.ANNUAL = T1.ANNUAL ");
            stb.append(        "LEFT JOIN (SELECT SCHREGNO, YEAR, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append(        "           FROM ATTEND_SEMES_DAT ");
            stb.append(        "           GROUP BY SCHREGNO, YEAR ");
            stb.append(        "           ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            return stb.toString();
        }
    }

    /**
     * 所見データ
     */
    private static class HtrainremarkHdat {
        String _totalstudyact;
        String _totalstudyval;

        private static PreparedStatement getPreparedStatement1(final DB2UDB db2, final Param param, final String schregno) throws SQLException {
            if (null == param._psMap.get("PS_HRHD")) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    TOTALSTUDYACT ");
                stb.append("   ,TOTALSTUDYVAL ");
                stb.append("FROM    HTRAINREMARK_HDAT T1 ");
                stb.append("WHERE   SCHREGNO = ? ");

                param._psMap.put("PS_HRHD", db2.prepareStatement(stb.toString()));
            }
            PreparedStatement ps = param._psMap.get("PS_HRHD");
            int p = 0;
            ps.setString(++p, schregno);
            return ps;
        }


        public static HtrainremarkHdat load(final DB2UDB db2, final Param param, final String schregno) {
            HtrainremarkHdat htrainremarkHdat = new HtrainremarkHdat();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = getPreparedStatement1(db2, param, schregno);
                rs = ps.executeQuery();
                if (rs.next()) {
                    htrainremarkHdat._totalstudyact = rs.getString("TOTALSTUDYACT");
                    htrainremarkHdat._totalstudyval = rs.getString("TOTALSTUDYVAL");
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return htrainremarkHdat;
        }
    }

    /**
     * 総合的な学習の時間の記録・外国語活動の記録
     */
    private static class HTrainRemarkDat {
        final String _year;
        int _g;
        String _totalstudyact;
        String _totalstudyval;
        String _specialActRemark;
        String _totalRemark;
        String _attendrecRemark;
        String _viewremark;
        String _detail2DatSeq004Remark1; // 道徳
        public HTrainRemarkDat(
                final String year) {
            _year = year;
        }

        private static HTrainRemarkDat getHTrainRemarkDat(final List<HTrainRemarkDat> list, final String year) {
            for (final HTrainRemarkDat d : list) {
                if (d._year.equals(year)) {
                    return d;
                }
            }
            return null;
        }

        private static PreparedStatement getPreparedStatement1(final DB2UDB db2, final Param param, final String schregno) throws SQLException {
            if (null == param._psMap.get("PS_HRD")) {
                final String sql = getHtrainremarkDatSql();
                param._psMap.put("PS_HRD", db2.prepareStatement(sql));
            }
            PreparedStatement ps = param._psMap.get("PS_HRD");
            int p = 0;
            ps.setString(++p, param._year);
            ps.setString(++p, schregno);
            return ps;
        }


        private static PreparedStatement getPreparedStatement2(final DB2UDB db2, final Param param, final String schregno) throws SQLException {
            if (null == param._psMap.get("PS_HRD_DET2")) {
                final String sql = getHtrainremarkDetail2HDatSql();
                param._psMap.put("PS_HRD_DET2", db2.prepareStatement(sql));
            }
            PreparedStatement ps = param._psMap.get("PS_HRD_DET2");
            int p = 0;
            ps.setString(++p, param._year);
            ps.setString(++p, schregno);
            ps.setString(++p, "001");
            return ps;
        }

        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List htrainRemarkDatList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = getPreparedStatement1(db2, param, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    final String totalstudyact = rs.getString("TOTALSTUDYACT");
                    final String totalstudyval = rs.getString("TOTALSTUDYVAL");
                    final String specialActRemark = rs.getString("SPECIALACTREMARK");
                    final String totalRemark = rs.getString("TOTALREMARK");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String viewremark = rs.getString("VIEWREMARK");

                    final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                    htrainremarkDat._g = Integer.parseInt(rs.getString("ANNUAL"));
                    htrainremarkDat._totalstudyact = totalstudyact;
                    htrainremarkDat._totalstudyval = totalstudyval;
                    htrainremarkDat._specialActRemark = specialActRemark;
                    htrainremarkDat._totalRemark = totalRemark;
                    htrainremarkDat._attendrecRemark = attendrecRemark;
                    htrainremarkDat._viewremark = viewremark;

                    htrainRemarkDatList.add(htrainremarkDat);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,REMARK1 ");
            stb.append("FROM HTRAINREMARK_DETAIL2_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.HTRAIN_SEQ = ? ");
            final String sql = stb.toString();

            for (final Map row : KnjDbUtils.query(db2, sql, new Object[] {param._year, schregno, "004"})) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                    continue;
                }
                final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                htrainremarkDat._detail2DatSeq004Remark1 = KnjDbUtils.getString(row, "REMARK1");
            }
            return htrainRemarkDatList;
        }

        private static String getHtrainremarkDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        YEAR <= ? ");
            stb.append("        AND SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T1.ANNUAL ");
            stb.append("       ,TOTALSTUDYACT ");
            stb.append("       ,TOTALSTUDYVAL ");
            stb.append("       ,SPECIALACTREMARK ");
            stb.append("       ,TOTALREMARK ");
            stb.append("       ,ATTENDREC_REMARK ");
            stb.append("       ,VIEWREMARK ");
            stb.append("       ,BEHAVEREC_REMARK ");
            stb.append("FROM    HTRAINREMARK_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            return stb.toString();
        }

        private static String getHtrainremarkDetail2HDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,REMARK1 ");
            stb.append("FROM HTRAINREMARK_DETAIL2_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.HTRAIN_SEQ = ? ");
            return stb.toString();
        }
    }

    /**
     * 行動の記録・特別活動の記録
     */
    private static class ActRecord {
        final String _year;
        final int _g;
        final String _record;
        final int _code;
        final String _div;
        public ActRecord(
                final String year,
                final int g,
                final String record,
                final int code,
                final String div) {
            _year = year;
            _g = g;
            _record = record;
            _code = code;
            _div = div;
        }

        private static PreparedStatement getPreparedStatement(final DB2UDB db2, final Param param) throws SQLException {
            if (null == param._psMap.get("PS_ACT")) {
                param._psMap.put("PS_ACT", db2.prepareStatement(getActRecordSql(param)));
            }
            return param._psMap.get("PS_ACT");
        }

        /**
         *  SVF-FORM 印刷処理 明細
         *  行動の記録・特別活動の記録
         */
        public static List<ActRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<ActRecord> actList = new ArrayList();
            ResultSet rs = null;
            try {
                int p = 0;
                final PreparedStatement ps = getPreparedStatement(db2, param);
                ps.setString(++p, schregno);   //学籍番号
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String record = rs.getString("RECORD");
                    final int g = Integer.parseInt(rs.getString("ANNUAL"));
                    final int code = Integer.parseInt(rs.getString("CODE"));
                    final String div = rs.getString("DIV");

                    final ActRecord act = new ActRecord(year, g, record, code, div);
                    actList.add(act);
                }
            } catch (Exception ex) {
                log.error("printSvfActRecord error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return actList;
        }

        /**
         *  priparedstatement作成  行動の記録・特別活動の記録
         */
        private static String getActRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(T1.YEAR) AS YEAR, T1.SCHREGNO, T1.ANNUAL ");
            stb.append("    FROM ");
            stb.append("        BEHAVIOR_DAT T1 ");
            stb.append("    WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append("          AND T1.SCHREGNO = ? ");
            stb.append("          AND T1.ANNUAL in " + param._gradeInChugaku + " ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, T1.ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("    ,T1.DIV ");
            stb.append("    ,T1.CODE ");
            stb.append("    ,T1.ANNUAL ");
            stb.append("    ,T1.RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.YEAR = T1.YEAR ");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ClassView {
        final String _classcd;  //教科コード
        final String _classname;  //教科名称
        final String _electdiv;
        final List<ViewSubclass> _viewSubclassList;

        public ClassView(
                final String classcd,
                final String classname,
                final String electdiv
        ) {
            _classcd = classcd;
            _classname = classname;
            _electdiv = electdiv;
            _viewSubclassList = new ArrayList();
        }

        public int getViewNum() {
            int c = 0;
            for (final ViewSubclass viewSubclass : _viewSubclassList) {
                c += viewSubclass._viewList.size();
            }
            return c;
        }

        // 教科名のセット
        private String setClassname(final String classname, final Param param) {
            if (classname == null) {
                return "";
            }
            final int viewnum = getViewNum();
            if (viewnum == 0) {
                return classname;
            }
            final int newviewnum;
            if (classname.length() <= viewnum && !"1".equals(param._seitoSidoYorokuCyugakuKantenNoBlank)) {
                newviewnum = viewnum + 1;  // 教科間の観点行に１行ブランクを挿入
            } else {
                newviewnum = viewnum;
            }
            final String newclassname;

            if (classname.length() < newviewnum) {
                final int i = (newviewnum - classname.length()) / 2;
                String space = "";
                for (int j = 0; j < i; j++) {
                    space = " " + space;
                }  // 教科名のセンタリングのため、空白を挿入
                newclassname = space + classname;
            } else {
                newclassname = classname;
            }
            return newclassname;
        }

        public String toString() {
            return "ViewClass(" + _classcd + ":" + _classname + " e = " + _electdiv + ")";
        }

        private static ClassView getClassView(final List<ClassView> classViewList, final String classcd, final String classname, final String electdiv) {
            if (null == classcd) {
                return null;
            }
            ClassView classView = null;
            for (final ClassView classView0 : classViewList) {
                if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._electdiv.equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }


        private static PreparedStatement getPreparedStatement(final DB2UDB db2, final Param param) throws SQLException {
            if (null == param._psMap.get("PS_VIEW")) {
                final String sql = getViewRecordSql(param);
                //log.info(" view rec sql = " + sql);
                param._psMap.put("PS_VIEW", db2.prepareStatement(sql));
            }
            return param._psMap.get("PS_VIEW");
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<ClassView> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<ClassView> classViewList = new ArrayList();
            ResultSet rs = null;
            try {
                PreparedStatement ps = getPreparedStatement(db2, param);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    //教科コードの変わり目
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String curriculumcd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String status = rs.getString("STATUS");
                    final String electdiv = rs.getString("ELECTDIV");
                    final int g = param.getGradeCd(year, rs.getString("GRADE")); // 学年

                    ClassView classView = getClassView(classViewList, classcd, classname, electdiv);
                    if (null == classView) {
                        classView = new ClassView(classcd, classname, electdiv);
                        classViewList.add(classView);
                    }
                    ViewSubclass viewSubclass = ViewSubclass.getViewSubclass(classView._viewSubclassList, subclasscd);
                    if (null == viewSubclass) {
                        viewSubclass = new ViewSubclass(subclasscd, subclassname);
                        classView._viewSubclassList.add(viewSubclass);
                    }
                    View view = View.getView(viewSubclass._viewList, viewcd);
                    if (null == view) {
                        view = new View(viewcd, viewname);
                        viewSubclass._viewList.add(view);
                    }
                    view._viewMap.put(year, new ViewStatus(curriculumcd, status, year, g));
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return classViewList;
        }

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param) {

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
            stb.append("  FROM    SCHREG_REGD_DAT T1  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <='" + param._year + "' ");
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
                stb.append("   ,T2.CURRICULUM_CD ");
                stb.append("   ,T2.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("   ,T3.CLASSCD");
                stb.append("   ,'' AS CURRICULUM_CD");
                stb.append("   ,T2.SUBCLASSCD");
            }
            stb.append("   ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("   ,CASE WHEN T4.SUBCLASSORDERNAME1 IS NOT NULL THEN T4.SUBCLASSORDERNAME1 ELSE T4.SUBCLASSNAME END AS SUBCLASSNAME");
            stb.append("   ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("   ,T2.VIEWCD ");
            stb.append("   ,T2.VIEWNAME ");
            stb.append("   ,T1.STATUS ");
            stb.append("FROM  ( SELECT DISTINCT ");
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
            stb.append("          , CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("                INNER JOIN JVIEWNAME_SUB_YDAT W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND W3.CLASSCD = W1.CLASSCD ");
                stb.append("          AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
                stb.append("          AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
            }
            stb.append("          AND W3.VIEWCD = W1.VIEWCD ");
            stb.append("               INNER JOIN SCHREG_DATA W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        WHERE W1.SCHOOL_KIND = '" + Param.SCHOOL_KIND_J + "' ");
            stb.append("      ) T2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                stb.append("  AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2)  ");
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
                stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("    AND T1.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T3.SCHOOL_KIND, ");
            }
            stb.append("    VALUE(T3.ELECTDIV, '0'), ");
            stb.append("    T2.SUBCLASSCD, ");
            stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T2.CURRICULUM_CD, "); // 教育課程の昇順に取得（同一の観点コードの場合、観点名称は教育課程の小さいほうを表示）
            }
            stb.append("    T2.VIEWCD, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 観点科目データ
     */
    private static class ViewSubclass {
        final String _subclasscd;  //科目コード
        final String _subclassname;
        final List<View> _viewList = new ArrayList();

        public ViewSubclass(
                final String subclasscd,
                final String subclassname
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        private static ViewSubclass getViewSubclass(final List<ViewSubclass> viewSubclassList, final String subclasscd) {
            ViewSubclass subclassView = null;
            for (final ViewSubclass viewSubclass0 : viewSubclassList) {
                if (viewSubclass0._subclasscd.equals(subclasscd)) {
                    subclassView = viewSubclass0;
                    break;
                }
            }
            return subclassView;
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname.toString() + ")";
        }
    }

    private static class View {
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final Map<String, ViewStatus> _viewMap = new HashMap();
        private View(
                final String viewcd,
                final String viewname
        ) {
            _viewcd = viewcd;
            _viewname = viewname;
        }

        private static View getView(final List<View> viewList, final String viewcd) {
            View view = null;
            for (final View view0 : viewList) {
                if (view0._viewcd.equals(viewcd)) {
                    view = view0;
                    break;
                }
            }
            return view;
        }

        public String toString() {
            return "View(" + _viewcd + ":" + _viewMap.toString() + ")";
        }
    }

    /**
     * 観点データ
     */
    private static class ViewStatus {
        final String _curriculumcd;
        final String _status; //観点
        final String _year;
        final int _g; // 学年

        public ViewStatus(
                final String curriculumcd,
                final String status,
                final String year,
                final int g
        ) {
            _curriculumcd = curriculumcd;
            _status = status;
            _year = year;
            _g = g;
        }

        public String toString() {
            return "(" + _year + "/" + _curriculumcd + ":" + StringUtils.defaultString(_status, " ") + ")";
        }
    }

    /**
     * 評定データ
     */
    private static class ValueRecord {
        final String _year;
        final int _g;
        final String _classCd;
        final String _curriculumCd;
        final String _subclassCd;
        final String _electDiv;
        final String _className;
        final String _subclassName;
        final String _value; //評定
        public ValueRecord(
                final String year,
                final int g,
                final String classCd,
                final String curriculumCd,
                final String subclassCd,
                final String electDiv,
                final String className,
                final String subclassName,
                final String value) {
            _year = year;
            _g = g;
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _electDiv = electDiv;
            _className = className;
            _subclassName = subclassName;
            _value = value;
        }
        public String toString() {
            return "(" + _classCd + ": " + _curriculumCd + ":" + _subclassCd + ", " + _className + ":" + _subclassName + ")";
        }

        private static PreparedStatement getPreparedStatement(final DB2UDB db2, final Param param) throws SQLException {
            if (null == param._psMap.get("PS_VALUE")) {
                final String sql = getValueRecordSql(param);
                //log.info(" value rec sql = " + sql);
                param._psMap.put("PS_VALUE", db2.prepareStatement(sql));
            }
            return param._psMap.get("PS_VALUE");
        }

        public static List<ValueRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<ValueRecord> valueRecordList = new ArrayList();
            ResultSet rs = null;
            try {
                final PreparedStatement ps = getPreparedStatement(db2, param);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    //教科コードの変わり目
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    final int g = param.getGradeCd(year, rs.getString("GRADE")); // 学年
                    final String electDiv = rs.getString("ELECTDIV");
                    final String classCd = rs.getString("CLASSCD");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    //評定出力
                    final String value = rs.getString("VALUE");

                    final ValueRecord valueRecord = new ValueRecord(year, g, classCd, curriculumCd, subclassCd, electDiv, className, subclassName, value);
                    valueRecordList.add(valueRecord);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
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
            stb.append("       AND T1.ANNUAL in " + param._gradeInChugaku + " ");
            stb.append(" ) ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("      AND T1.ANNUAL in " + param._gradeInChugaku + " ");
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
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,'' AS CURRICULUM_CD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD2, T5.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append("    ,MAX(VALUE(T3.CLASSORDERNAME1, T3.CLASSNAME)) AS CLASSNAME ");
            stb.append("    ,MAX(VALUE(T6.SUBCLASSORDERNAME1, T6.SUBCLASSNAME, T4.SUBCLASSORDERNAME1, T4.SUBCLASSNAME)) AS SUBCLASSNAME ");
            stb.append("    ,MAX(VALUE(T3.SHOWORDER, -1)) AS SHOWORDERCLASS ");
            stb.append("    ,MAX(T5.VALUE) AS VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T4.CLASSCD = T5.CLASSCD ");
                stb.append(" AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append(" AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T6 ON T6.SUBCLASSCD2 = T5.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T6.CLASSCD = T5.CLASSCD ");
                stb.append(" AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append(" AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD2, T5.SUBCLASSCD) ");
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD2, T5.SUBCLASSCD) ");
            stb.append("    ,T3.ELECTDIV ");
            stb.append("    ,T2.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  生徒指導要録
     *   各教科・科目の学習の記録
     *   行動の記録
     *   特別活動の記録
     *   出欠の記録
     */
    private static class KNJA133J_3 extends KNJA130_0 {

        private int VIEW_LINE1_MAX;

        KNJA133J_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            boolean _isNoDoutokuForm = false; // 旧フォーム（道徳無しのフォーム）を使用するか
            if (Integer.parseInt(param()._year) < Param.DOUTOKU_J_START_YEAR) {
                _isNoDoutokuForm = true;
            } else {
                final List<Integer> jYears = new ArrayList<Integer>();
                for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                    if (!NumberUtils.isDigits(gakuseki._year)) {
                        continue;
                    }
                    if (Param.SCHOOL_KIND_J.equals(gakuseki._schoolKind) && NumberUtils.isDigits(gakuseki._year)) {
                        jYears.add(Integer.parseInt(gakuseki._year));
                    }
                }
                if (jYears.isEmpty()) {
                    // 中学在籍データがなければ新フォーム
                } else {
                    boolean hasOver = false;
                    for (final Integer jYear : jYears) {
                        if (Param.DOUTOKU_J_START_YEAR <= jYear) {
                            hasOver = true;
                            break;
                        }
                    }
                    if (!hasOver) {
                        // 中学在籍時の年度がすべて Param.DOUTOKU_J_START_YEAR 未満なら旧フォームを使用する
                        _isNoDoutokuForm = true;
                    }
                }
            }
            VIEW_LINE1_MAX = 61;
            final String form;
            if (_isNoDoutokuForm) {
                form = "KNJA130G_3_NO_DOUTOKU.frm";
            } else {
                form = "KNJA130G_3.frm";
                VIEW_LINE1_MAX -= 7; // 道徳欄追加
            }
            svfVrSetForm(form, 4);
            printRegdRecord(svf, student, personalInfo);  //年次・ホームルーム・整理番号
            printShoken(svf, student, personalInfo); // 所見
            printValueRecord2(svf, student, personalInfo);  //評定
            if (printClassView(svf, student, personalInfo, student._classViewList)) {
                hasdata = true;  //観点 ＊ココでsvf.VrEndRecord()
            }
        }

        private void printShoken(Vrw32alp svf, Student student, PersonalInfo personalInfo) {
            //総合的な学習の時間の記録・総合所見
            for (final HTrainRemarkDat remarkRecord : student._htrainRemarkDatList) {
                if (!student.isPrintYear(remarkRecord._year, param())) {
                    continue;
                }
                if (null != remarkRecord._year && !(personalInfo.getYearBegin() <= Integer.parseInt(remarkRecord._year) && Integer.parseInt(remarkRecord._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }

                // 道徳
                printSvfRenban("MORAL" + remarkRecord._g, remarkRecord._detail2DatSeq004Remark1, ShokenSize.getShokenSize(param()._HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J, 32, 2));

                printSvfRenban("TOTAL_ACT" + remarkRecord._g, remarkRecord._totalstudyact, new ShokenSize(5, 8));
                printSvfRenban("TOTAL_VIEW" + remarkRecord._g, remarkRecord._viewremark, new ShokenSize(10, 8));
                printSvfRenban("TOTAL_VALUE" + remarkRecord._g, remarkRecord._totalstudyval, new ShokenSize(15, 8));
            }

            //行動の記録・特別活動の記録
            final StringBuffer specialActRemark = new StringBuffer();
            String nl = "";
            for (final HTrainRemarkDat remarkRecord : student._htrainRemarkDatList) {
                if (!student.isPrintYear(remarkRecord._year, param())) {
                    continue;
                }
                if (null != remarkRecord._year && !(personalInfo.getYearBegin() <= Integer.parseInt(remarkRecord._year) && Integer.parseInt(remarkRecord._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                if (null != remarkRecord._specialActRemark) {
                    specialActRemark.append(nl).append(remarkRecord._specialActRemark);
                    nl = "\n";
                }
            }
            printSvfRenban("SPECIALACTVIEW", specialActRemark.toString(), new ShokenSize(17, 10));

            for (final ActRecord actRecord : student._actRecordList) {
                if (!student.isPrintYear(actRecord._year, param())) {
                    continue;
                }
                if (null != actRecord._year && !(personalInfo.getYearBegin() <= Integer.parseInt(actRecord._year) && Integer.parseInt(actRecord._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + actRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                printAct(svf, actRecord);
            }
        }

        public void printAct(Vrw32alp svf, final ActRecord actRecord) {
            if ("1".equals(actRecord._record)) {
                if ("2".equals(actRecord._div)){
                    VrsOutn("SPECIALACT" + actRecord._code, actRecord._g, "○"); //特別行動の記録
                }
            }
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目コードの順番を返す。そうでなければ(通常の処理）教科コードを返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getKeyCd(final ClassView classview, final ViewSubclass viewSubclass) {
            final TreeSet<String> set = getSubclassCdSet(classview);
            if (set.size() == 1) {
                return classview._classcd;
            } else if (set.size() > 1) {
                final DecimalFormat df = new DecimalFormat("00");
                int order = 0;
                int n = 0;
                for (final String subclassCd : set) {
                    if (viewSubclass._subclasscd.equals(subclassCd)) {
                        order = n;
                        break;
                    }
                    n += 1;
                }
                return df.format(order);
            }
            return null;
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getShowname(final ClassView classview, final ViewSubclass viewSubclass) {
            final TreeSet<String> set = getSubclassCdSet(classview);
            if (set.size() > 1 || "1".equals(classview._electdiv)) {
                return viewSubclass._subclassname;
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
        private TreeSet<String> getSubclassCdSet(final ClassView classview) {
            final TreeSet<String> set = new TreeSet();
            for (final ViewSubclass viewSubclass : classview._viewSubclassList) {
                set.add(viewSubclass._subclasscd);
            }
            return set;
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
         * @param student
         * @param valueRecord
         * @return 科目名または教科名
         */
        private String getShowname(final Student student, final ValueRecord valueRecord) {
            final TreeSet<String> set = getSubclassCdSet(student, valueRecord);
            //log.info(" showname = " + valueRecord._classCd + ":" + valueRecord._className + ", " + valueRecord._subclassCd + ":" + valueRecord._subclassName);
            if (set.size() > 1 || "1".equals(valueRecord._electDiv)) {
                return valueRecord._subclassName;
            } else if (set.size() == 1) {
                return valueRecord._className;
            }
            return null;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet<String> getSubclassCdSet(final Student student, final ValueRecord valueRecord) {
            final TreeSet<String> set = new TreeSet();
            for (final ValueRecord valueRecord0 : student.getValueRecordList()) {
                if (valueRecord0._classCd.equals(valueRecord._classCd)) {
                    set.add(valueRecord0._subclassCd);
                }
            }
            return set;
        }

        /**
         *  観点出力処理
         *
         */
        private boolean printClassView(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo, final List<ClassView> classViewList) {
            boolean nonedata = false;
            try {
                int line1 = 0;  //欄の出力行数

                for (final ClassView classview : classViewList) {
                    // log.debug(" = " + classview);

                    for (final ViewSubclass viewSubclass : classview._viewSubclassList) {
                        // log.debug("     = " + viewSubclass);
                        final String keyCd = getKeyCd(classview, viewSubclass);
                        final String showname = getShowname(classview, viewSubclass);
                        final String name = classview.setClassname(showname, param());  // 教科名のセット
                        int i = 0;  //教科名称カウント用変数を初期化
                        final String field;
                        final int inc;
//                        if (param()._isKaijyo) {
//                            field = "CLASS1";
//                            inc = 1;
//                        } else {
                            final int orikaeshiLine = 4;
                            final boolean useClass3 = orikaeshiLine * 2 < (StringUtils.defaultString(showname).length());
                            final int orikaeshi = useClass3 ? orikaeshiLine * 2 : 9999;
                            final boolean useClass2 =  5 < (StringUtils.defaultString(showname).length());
                            field = useClass2 ? "CLASS2" : "CLASS1";
                            inc = useClass3 || useClass2 ? 2 : 1;
//                        }

                        for (int vi = 0; vi < viewSubclass._viewList.size(); vi++) {
                            final View view = viewSubclass._viewList.get(vi);
                            // log.debug("         = " + view);
//                            if (param()._isKaijyo) {
//                                if (vi == 0) {
//                                    svf.VrsOut(field, showname);  //教科名称
//                                    i = 999;
//                                }
//                            } else {
                                if (i < orikaeshi) {
                                    if (useClass3) {
                                        final int idx1 = Math.min(Math.min(i + inc, orikaeshi), name.length());
                                        if (i < idx1) {
                                            svf.VrsOut("CLASS3_V2", name.substring(i, idx1));  //教科名称 右
                                        }
                                        if (i + orikaeshi < name.length()) {
                                            svf.VrsOut("CLASS3_V1", name.substring(i + orikaeshi, Math.min(i + orikaeshi + inc, name.length())));  //教科名称 左
                                        }
                                    } else {
                                        final int idx1 = Math.min(Math.min(i + inc, orikaeshi), name.length());
                                        if (i < idx1) {
                                            svf.VrsOut(field, name.substring(i, idx1));  //教科名称
                                        }
                                    }
                                    i += inc;
                                }
//                            }
                            svf.VrsOut("CLASSCD1", keyCd);  //教科コード
                            svf.VrsOut("VIEW1", view._viewname);  //観点名称

                            for (final ViewStatus viewStatus : view._viewMap.values()) {
                                if (!student.isPrintYear(viewStatus._year, param())) {
                                    continue;
                                }
                                if (null != viewStatus._year && !(personalInfo.getYearBegin() <= Integer.parseInt(viewStatus._year) && Integer.parseInt(viewStatus._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                                    continue;
                                }
                                svf.VrsOut("ASSESS1_" + viewStatus._g, viewStatus._status);  //観点
                            }
                            line1++;
                            updateLine1(svf, student);
                            svf.VrEndRecord();
                            nonedata = true;
                        }
                        if (null != name) {
                            if (i < name.length()) {
                                for (int j = i; j < name.length() && j < orikaeshi; j += inc) {
                                    line1++;
                                    updateLine1(svf, student);
                                    svf.VrsOut("CLASSCD1", keyCd);  //教科コード
                                    if (useClass3) {
                                        svf.VrsOut("CLASS3_V2", name.substring(j));  // 教科名称
                                        if (j + orikaeshi < name.length()) {
                                            svf.VrsOut("CLASS3_V1", name.substring(j + orikaeshi, Math.min(j + orikaeshi + inc, name.length())));  //教科名称
                                        }
                                    } else {
                                        svf.VrsOut(field, name.substring(j));  // 教科名称
                                    }
                                    svf.VrEndRecord();
                                    nonedata = true;
                                }
                            } else {
//                    			if (!"1".equals(param()._seitoSidoYorokuCyugakuKantenNoBlank)) {
//                    				final int isPrint = ((line1 % VIEW_LINE1_MAX != 0) ? 1 : -1);
//                    				if (-1 != isPrint) { // 行数がオーバーしない場合、レコードを印刷
//                    					line1++;
//                    					updateLine1(svf, student);
//                    					svf.VrsOut("CLASSCD1", keyCd);  // 教科コード
//                    					svf.VrEndRecord();
//                    					nonedata = true;
//                    				}
//                    			}
                            }
                        }
                    }
                }
                for (int i = line1 != 0 && line1 % VIEW_LINE1_MAX == 0 ? VIEW_LINE1_MAX : line1 % VIEW_LINE1_MAX; i < VIEW_LINE1_MAX; i++) {
                    svf.VrsOut("CLASSCD1", "A");  //教科コード
                    line1++;
                    updateLine1(svf, student);
                    svf.VrEndRecord();
                    nonedata = true;
                }
            } catch (Exception ex) {
                log.error( "printSvfDetail_1 error!", ex);
            }
            return nonedata;
        }

        private void updateLine1(final Vrw32alp svf, final Student student) {
            printName(svf, student._personalInfo);
        }

        /**
         *  評定出力処理
         */
        private void printValueRecord2(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo) {
            final List classListList = new ArrayList();
            List current = null;
            String oldClasscd = null;  //教科コードの保管用
            String oldSubClasscd = null;  //教科コードの保管用
            for (final Iterator it = student.getValueRecordList().iterator(); it.hasNext();) {
                final ValueRecord valueRecord = (ValueRecord) it.next();
                //教科コードの変わり目
                if (oldClasscd == null || oldSubClasscd == null || !oldClasscd.equals(valueRecord._classCd) || !oldSubClasscd.equals(valueRecord._subclassCd)) {
                    current = new ArrayList();
                    classListList.add(current);
                }
                current.add(valueRecord);
                oldClasscd = valueRecord._classCd;  //教科コードの保管
                oldSubClasscd = valueRecord._subclassCd;
            }

            for (int i = 0; i < classListList.size(); i++) {
                final List classList = (List) classListList.get(i);
                for (int j = 0; j < classList.size(); j++) {
                    final ValueRecord valueRecord = (ValueRecord) classList.get(j);
                    final String line = String.valueOf(i + 1);
                    if (j == 0) {
                        final String showname = getShowname(student, valueRecord);
                        if (null != showname) { //教科名出力
//                            if (param()._isKaijyo) {
//                                svf.VrsOutn("CLASS3_1", i + 1, showname);  //評定教科名
//                            } else {
                                final int keta = getMS932ByteLength(showname);
                                final String field;
                                if (keta > 10) {
                                    field = "CLASS" + line + "_3";
                                } else if (keta > 6) {
                                    field = "CLASS" + line + "_2";
                                } else {
                                    field = "CLASS" + line + "_1";
                                }
                                svf.VrsOut(field, showname);  //評定教科名
//                            }
                        }
                    }
                    if (!student.isPrintYear(valueRecord._year, param())) {
                        continue;
                    }
                    if (null != valueRecord._year && !(personalInfo.getYearBegin() <= Integer.parseInt(valueRecord._year) && Integer.parseInt(valueRecord._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                        // log.debug(" skip print year = " + valueRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    if (valueRecord._value != null) {
                        final String value;
                        final String fullSubclasscd = valueRecord._classCd + "-" + valueRecord._curriculumCd + "-" + valueRecord._subclassCd;
                        if (param()._d072SubclasscdList.contains(fullSubclasscd)) { // 名称マスタで読み替え
                            value = (String) param()._d071hyoteiAlphabetMap.get(valueRecord._value);
                        } else {
                            value = valueRecord._value;
                        }
//                        if (param()._isKaijyo) {
//                            svf.VrsOutn("ASSESS2_" + valueRecord._g, (i + 1), value);  //評定
//                        } else {
                            VrsOutn("ASSESS" + line, valueRecord._g, value);  //評定
//                        }
                    }
                }
            }
        }

        private void printName(final Vrw32alp svf, final PersonalInfo personalInfo) {
            int posx1 = 416;
            int posx2 = 950;
            int height = 50;
            final int minnum = 20;
            final int maxnum = 48;
            int ystart = 514, ystart1 = 484, ystart2 = 544;

            printName(svf, personalInfo, "NAME1", "NAME2", "NAME3", posx2 - posx1, height, minnum, maxnum, ystart, ystart1, ystart2);
        }

        /**
         *  個人情報  学籍等履歴情報
         */
        private void printRegdRecord(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo) {
            //生徒名印刷
            printName(svf, personalInfo);

            final SchoolInfo schoolInfo = student._schoolInfo;
            if (null != schoolInfo) {
                final int n = getMS932ByteLength(schoolInfo._schoolName1);
                if (0 < n) {
                    svf.VrsOut("SCHOOLNAME" + (40 < n ? "2" : "1"), schoolInfo._schoolName1);
                }
            }

            //学籍履歴の取得および印刷
            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                if (!student.isPrintYear(gakuseki._year, param())) {
                    continue;
                }
                if (null != gakuseki._year && !(personalInfo.getYearBegin() <= Integer.parseInt(gakuseki._year) && Integer.parseInt(gakuseki._year) <= personalInfo.getYearEnd(param()))) {
                    // log.debug(" skip print year = " + regdRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }

                VrsOutn("HR_NAME", Integer.parseInt(gakuseki._grade), gakuseki._hrname); // 組
                VrsOutn("ATTENDNO", Integer.parseInt(gakuseki._grade), String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号
            }
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  生徒指導要録
     *   総合所見及び指導上参考となる事項
     */
    private static class KNJA133J_4 extends KNJA130_0 {

        KNJA133J_4(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public void setDetail(final Student student, final PersonalInfo personalInfo) {
            //boolean nonedata = false;
            final String form;
            form = "KNJA130G_4.frm";
            svfVrSetForm(form, 1);
            printName(svf, personalInfo);  //学籍データ
            printShoken(svf, student, personalInfo); // 所見
            hasdata = true;
            svf.VrEndPage();
        }


        private void printShoken(final Vrw32alp svf, final Student student, final PersonalInfo personalInfo) {
            // 行動の記録
            for (final ActRecord act : student._actRecordList) {
                if (!student.isPrintYear(act._year, param())) {
                    continue;
                }
                if (null != act._year && !(personalInfo.getYearBegin() <= Integer.parseInt(act._year) && Integer.parseInt(act._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + actRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                printAct(svf, act);
            }

            // 総合的な学習の時間の記録・総合所見
            for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                if (!student.isPrintYear(remark._year, param())) {
                    continue;
                }
                if (null != remark._year && !(personalInfo.getYearBegin() <= Integer.parseInt(remark._year) && Integer.parseInt(remark._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }

                if (remark._totalRemark != null) {
                    final ShokenSize size = ShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_TOTALREMARK_SIZE_J, 44, 10);

                    if (size._gyo == 13) {
                        printSvfRenban("TOTALREMARK13_" + remark._g, remark._totalRemark, size); //総合所見
                    } else {
                        final List<String> lines = retDividString(remark._totalRemark, size._mojisu * 2);
                            if (lines.size() > 10) {
                                printSvfRenban("TOTALREMARK21_" + remark._g, lines); //総合所見
                            } else {
                                printSvfRenban("TOTALREMARK" + remark._g, lines); //総合所見
                            }
                    }
                }

                if (remark._attendrecRemark != null) {
                    final ShokenSize size = ShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J, 35, 2);

                        final List<String> list = retDividString(remark._attendrecRemark, size._mojisu * 2, size._gyo);
                        for (int i = 0 ; i < list.size(); i++) {
                            final String n = i == 0 ? "" : String.valueOf(i + 1);
                            svf.VrsOutn("REMARK" + n, remark._g, list.get(i)); //出欠の記録備考
                        }
                }
            }

            //出欠の記録
            for (final Attend attend : student._attendList) {
                if (!student.isPrintYear(attend._year, param())) {
                    continue;
                }
                if (null != attend._year && !(personalInfo.getYearBegin() <= Integer.parseInt(attend._year) && Integer.parseInt(attend._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    // log.debug(" skip print year = " + attendRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                printAttend(attend);
            }

            svf.VrsOut("TEXT1", "　　　①各教科や" + PersonalInfo.SOGOTEKI_NA_GAKUSHU_NO_JIKAN + "の学習に関する所見");
        }

        private void printAct(final Vrw32alp svf, final ActRecord act) {
            if ("1".equals(act._record)) {
                if ("1".equals(act._div)) {
                    VrsOutn("ACTION" + act._code, act._g, "○"); //行動の記録
                }
            }
        }

        public void printSpecialAct(Vrw32alp svf, final ActRecord act) {
            if ("1".equals(act._record)) {
                if ("2".equals(act._div)){
                    VrsOutn("SPECIALACT" + act._g, act._code, "○"); //特別行動の記録
                }
            }
        }

        private void printAttend(final Attend attend) {
            VrsOutn("LESSON",  attend._g, attend._lesson);           //授業日数
            VrsOutn("SUSPEND", attend._g, attend._suspendMourning);  //出停・忌引
            VrsOutn("PRESENT", attend._g, attend._requirePresent);   //要出席
            VrsOutn("ATTEND",  attend._g, attend._present);          //出席
            VrsOutn("ABSENCE", attend._g, attend._absent);           //欠席
        }

        private void printName(final Vrw32alp svf, final PersonalInfo personalInfo) {
            int posx1 = 176;
            int posx2 = 708;
            int height = 50;
            final int minnum = 20;
            final int maxnum = 48;
            int ystart = 510, ystart1 = 480, ystart2 = 540;

            printName(svf, personalInfo, "NAME1", "NAME2", "NAME3", posx2 - posx1, height, minnum, maxnum, ystart, ystart1, ystart2);
        }
    }

    private static class ShokenSize {
        int _mojisu;
        int _gyo;

        ShokenSize(final int mojisu, final int gyo) {
            _mojisu = mojisu;
            _gyo = gyo;
        }

        private static ShokenSize getShokenSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
            final int mojisu = ShokenSize.getParamSizeNum(paramString, 0);
            final int gyo = ShokenSize.getParamSizeNum(paramString, 1);
            if (-1 == mojisu || -1 == gyo) {
                return new ShokenSize(mojisuDefault, gyoDefault);
            }
            return new ShokenSize(mojisu, gyo);
        }

        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param param サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        private static int getParamSizeNum(final String param, final int pos) {
            int num = -1;
            if (StringUtils.isBlank(param)) {
                return num;
            }
            final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
            if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }

        public String toString() {
            return "ShokenSize(" + _mojisu + ", " + _gyo + ")";
        }
    }


    private static class Param {

        private static final String SCHOOL_KIND_H = "H";
        private static final String SCHOOL_KIND_J = "J";
        private static final int DOUTOKU_J_START_YEAR = 2019;

        final String _year;
        final String _gakki;
        final String _gradeInChugaku;

        final String _output;
        final String[] _categorySelected;
        final String _grade;

        /** 生徒指導要録 */
        final String _seito;
        /** 修得単位の記録 */
        final String _tani;
        /** 前期課程 学習の記録 */
        final String _zenkiGakushu;
        /** 前期課程 活動の記録 */
        final String _zenkiKatsudo;
        /** 後期課程 学習の記録 */
        final String _koukiGakushu;
        /** 後期課程 活動の記録 */
        final String _koukiKatsudo;
        /** 生徒・保護者氏名出力(生徒指導要録に関係する) */
        final String _simei;
        /** 陰影出力(生徒指導要録に関係する) */
        final String _inei;
        /** 陰影保管場所(陰影出力に関係する) */
        final String _documentRoot;
        /** 現住所の郵便番号を出力 */
        final boolean _printZipcd;
        /** 学校所在地の郵便番号を出力 */
        final boolean _printSchoolZipcd;

        /** 中高一貫ならtrue */
        final boolean _isChuKouIkkan;
        /** 併設校ならtrue */
        final boolean _isHeisetuKou;

        /**
         *  名称マスタA021を使用するなら"0"
         *  組名称にSCHREG_REGD_HDATのHR_CLASS_NAME1を使用するなら"1"
         */
        final String _useSchregRegdHdat;

        /** 「活動の記録」にて「総合的な学習の時間の記録」欄は年度毎か? */
        final boolean _isNendogoto;

        /** 原級留置ならtrue */
        private boolean _isGenkyuRyuchi;

        /** ３年用フォームならtrue */
        final boolean _isFormType3;

        private KNJA130_1 _knja130_1;
        private KNJA130_2 _knja130_2;
        private KNJA133J_3 _knja133j_3;
        private KNJA133J_4 _knja133j_4;
        private KNJA130_3 _knja130_3;
        private KNJA130_4 _knja130_4;
//        private KNJA130_34 _knja130_34;

        final String _seitoSidoYorokuFieldSize;

        /** プロパティーファイルのseitoSidoYorokuSougouFieldSizeが1なら true */
        final boolean _seitoSidoYorokuSougouFieldSize;
        /** プロパティーファイルのseitoSidoYorokuSpecialactremarkFieldSizeが1なら true */
        final boolean _seitoSidoYorokuSpecialactremarkFieldSize;
        /** プロパティーファイルのseitoSidoYorokuKinsokuFormが1なら true */
        final boolean _seitoSidoYorokuKinsokuForm;
        /** プロパティーファイルのseitoSidoYorokuZaisekiMaeが1なら true */
        final boolean _seitoSidoYorokuZaisekiMae;
        /** プロパティーファイルのseitoSidoYorokuKoumokuMeiが1なら true */
        final String _seitoSidoYorokuKoumokuMei;
        /** プロパティーファイルのseitoSidoYorokuHyotei0ToBlankが1なら指導要録の学習の記録で評定0はブランク表示にする */
        final String _seitoSidoYorokuHyotei0ToBlank;
        /** プロパティーファイルのseitoSidoYorokuYoshiki2PrintOrderが1なら指導要録の様式２の表示順を変更する */
        final String _seitoSidoYorokuYoshiki2PrintOrder;
        /** プロパティーファイルのseitoSidoYorokuNotPrintAnotherStudyrecが1なら指導要録のSCHOOLCD='1'のSCHREG_STUDYREC_DATを読み込みしない */
        final String _seitoSidoYorokuNotPrintAnotherStudyrec;
        /** プロパティーファイルのseitoSidoYorokuNotPrintAnotherAttendrecが1なら指導要録のSCHOOLCD='1'のSCHREG_ATTENDREC_DATを読み込みしない */
        final String _seitoSidoYorokuNotPrintAnotherAttendrec;
        /** プロパティーファイルのtrain_ref_1_2_3_field_sizeが2なら所見サイズ変更 */
        final String _train_ref_1_2_3_field_size;

        final String _useCurriculumcd;
        final String _useGakkaSchoolDiv;
        final String _useAddrField2;
        final String _useProvFlg;
        final String _staffGroupcd; // 京都府は、印刷する職員のグループコードのMAXの頭3桁が999の場合、印影を表示する

        /** 西暦を使用するか */
        final boolean _isSeireki;

        /** 学校区分名称 */
        private Map _z001name1;

        /** 履修のみ科目出力 */
        private boolean _isPrintRisyuNomi;
        /** 未履修科目出力 */
        private boolean _isPrintMirisyu;
        /** 履修登録のみ科目出力 */
        private boolean _isPrintRisyuTourokuNomi;

        private static final int OPTION_CREDIT0 = 0;
        private static final int OPTION_CREDIT1 = 1;
        private static final int OPTION_CREDIT2 = 2;
        /** 科目・単位表示オプション
         *   -+----------+---------------------------+----------------------------+
         *    |          | 様式１裏                  | 様式２表                   |
         *    |          |---------------------------+----------------------------+
         *    |          | 未履修       | 履修のみ   | 未履修        | 履修のみ   |
         *   -+----------+--------------+------------+---------------+------------+
         *   0| 科目表示 | 有           | 有         | 有            | 有         |
         *    | 単位     | 0            | (履修単位) | 0             | (履修単位) |
         *   -+----------+--------------+------------+---------------+------------+
         *   1| 科目表示 | 無           | 無         | 有            | 有         |
         *    | 単位     | -            | -          | 0             | (履修単位) |
         *   -+----------+--------------+------------+---------------+------------+
         *   2| 科目表示 | 有           | 有         | 有            | 有         |
         *    | 単位     | 空欄         | 0          | 空欄          | 0          |
         *   ------------+--------------+------------+---------------+------------+
         *   ※0:賢者 1:京都府 2:鳥取
         * */
        private int _optionCreditOutput;

        final KNJDefineSchool _definecode;

        protected Set<String> _gdatHYearGradeSet = Collections.EMPTY_SET;
        protected Map<String, String> _gdatGradeName2 = Collections.EMPTY_MAP;
        protected Map<String, String> _gdatGradeCd = Collections.EMPTY_MAP;
        protected Map<String, String> _gdatSchoolKind = Collections.EMPTY_MAP;
        protected Map _specialDivNameMap = Collections.EMPTY_MAP;
        private Set _gradeJ = new HashSet();
        protected String _mirisyuRemarkFormat; // 未履修（履修不認定）備考フォーマット
        protected String _risyunomiRemarkFormat; // 履修のみ（修得不認定）備考フォーマット

        final boolean _isSubclassOrderNotContainCurriculumcd;
        final boolean _isPrintYoshiki2OmoteTotalCreditByPage;
        final boolean _isSogoShoken3Bunkatsu; // 総合所見3分割
        final boolean _isPrintAnotherStudyrec2; // 2枚目（様式1裏）に前籍校の成績を含める
        final boolean _useStaffNameHistDat;
        final boolean _isPrintZaigakusubekiKikan;
        private static final int _printAnotherStudyrec3_0 = 0;
        private static final int _printAnotherStudyrec3_1 = 1;
        private static final int _printAnotherStudyrec3_2 = 2;
        final int _printAnotherStudyrec3; // 3枚目（様式2表）に前籍校の成績を含めるか 0:含めない 1:含める 2:単位制は含める。学年制は含めない。
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasANOTHER_CLASS_MST;
        final boolean _hasANOTHER_SUBCLASS_MST;
        final boolean _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT;
        final boolean _hasMAJOR_MST_MAJORNAME2;
        final boolean _hasSCHREG_TRANSFER_DAT_REMARK1;

        final String _gradeHrclass;
        final int _seitoSidoYorokuCyugakuKirikaeNendo;
        final String _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear;

        final String _schzip;
        final String _schoolzip;
        final String _colorPrint;
        final String _documentroot;
        final String _seitoSidoYorokuCyugakuKantenNoBlank;

        final String _imagePath;
        final String _extension;

        final Map<String, String> _gradeCdMap;

        final Map<String, PreparedStatement> _psMap = new HashMap();
        final Map _d071hyoteiAlphabetMap;
        final List _d072SubclasscdList;

        /** 卒業した学校の設立区分を表示するか */
        private boolean _isInstallationDivPrint;

        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J;
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J;

        protected StaffInfo _staffInfo = new StaffInfo();
        protected Map<String, Semester> _semesterMap = Collections.emptyMap();

        final boolean _hasAftGradCourseDat;

        final String _printMajorname;

        final String _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J;
        final String _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J;
        final String _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J;
        final String _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J;
        final String _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability;
        final String _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J;
        final String _HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J;

        private Map<String, String> _dbPrgInfoProperties;
        boolean _isOutputDebug;
        boolean _isOutputDebugStaff;
        boolean _isOutputDebugQuery;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _inei = "".equals(request.getParameter("INEI")) ? null : request.getParameter("INEI"); // 陰影出力
            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001

            _simei = request.getParameter("simei"); // 漢字名出力

            _seito = request.getParameter("seito");
            _tani = request.getParameter("tani");
            _zenkiGakushu = request.getParameter("zenki_gakushu");
            _zenkiKatsudo = request.getParameter("zenki_katsudo");
            _koukiGakushu = request.getParameter("kouki_gakushu");
            _koukiKatsudo = request.getParameter("kouki_katsudo");

            _output = request.getParameter("OUTPUT");    // 1=個人, 2=クラス
            _categorySelected = request.getParameterValues("category_selected"); // 複数生徒 or 複数年組

            final String grade = request.getParameterValues("GRADE_HR_CLASS")[0];
            if (null != grade && grade.length() >= 2) {
                _grade = grade.substring(0, 2);
            } else {
                _grade = null;
            }

            _definecode = new KNJDefineSchool();
            _definecode.setSchoolCode(db2, _year);

            final String z010namespare2 = getNameMstZ010(db2, "NAMESPARE2");
            _isHeisetuKou = "1".equals(z010namespare2);
            _isChuKouIkkan = "1".equals(z010namespare2) || "2".equals(z010namespare2);
            final String z010name1 = getNameMstZ010(db2, "NAME1");
            log.fatal(" z010 name1 = " + z010name1);

            log.debug("中高一貫か? = " + _isChuKouIkkan);
            log.debug("併設校か? = " + _isHeisetuKou);
            setSpecialDivNameMap(db2);
            setFuninteiRemarkFormat(db2);
            setOptionCreditOutput(db2);
            setGenkyuRyuchi(db2);//原級留置
            setGdatHYearGradeSet(db2);
            setNameMstZ001(db2);
            _semesterMap = Semester.load(db2);
            _printMajorname = getPrintMajorname(db2);
            _staffInfo.load(db2, null, _year);

            if (!_gradeJ.isEmpty()) {
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                String comma = "";
                for (final Iterator it = _gradeJ.iterator(); it.hasNext();) {
                    final String g = (String) it.next();
                    stb.append(comma).append("'").append(g).append("'");
                    comma = ", ";
                }
                stb.append(")");
                _gradeInChugaku = stb.toString();
            } else {
                _gradeInChugaku = "('')";
            }

            //final String prgId = request.getParameter("PRGID");
            _isNendogoto = true; // "KNJA130D".equals(prgId);
            log.debug("「活動の記録」にて「総合的な学習の時間の記録」欄を年度毎にするか?⇒" + _isNendogoto);

            _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");

            final String formType = request.getParameter("seitoSidoYorokuFormType");
            _isFormType3 = "3".equals(formType);
            log.debug("３年用フォームを使用するか?⇒" + _isFormType3);

            _printZipcd = "1".equals(request.getParameter("schzip"));
            _printSchoolZipcd = "1".equals(request.getParameter("schoolzip"));

            _seitoSidoYorokuFieldSize = request.getParameter("seitoSidoYorokuFieldSize");
            _seitoSidoYorokuSougouFieldSize = "1".equals(request.getParameter("seitoSidoYorokuSougouFieldSize"));
            _seitoSidoYorokuSpecialactremarkFieldSize = "1".equals(request.getParameter("seitoSidoYorokuSpecialactremarkFieldSize"));
            _seitoSidoYorokuKinsokuForm = false; // "1".equals(request.getParameter("seitoSidoYorokuKinsokuForm"));
            _seitoSidoYorokuZaisekiMae = "1".equals(request.getParameter("seitoSidoYorokuZaisekiMae"));
            _seitoSidoYorokuKoumokuMei = request.getParameter("seitoSidoYorokuKoumokuMei");
            _seitoSidoYorokuHyotei0ToBlank = request.getParameter("seitoSidoYorokuHyotei0ToBlank");
            _seitoSidoYorokuYoshiki2PrintOrder = request.getParameter("seitoSidoYorokuYoshiki2PrintOrder");
            _seitoSidoYorokuNotPrintAnotherStudyrec = request.getParameter("seitoSidoYorokuNotPrintAnotherStudyrec");
            _seitoSidoYorokuNotPrintAnotherAttendrec = request.getParameter("seitoSidoYorokuNotPrintAnotherAttendrec");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useGakkaSchoolDiv = request.getParameter("useGakkaSchoolDiv");
            _useAddrField2 = request.getParameter("useAddrField2");
            _useProvFlg = request.getParameter("useProvFlg");
            _train_ref_1_2_3_field_size = request.getParameter("train_ref_1_2_3_field_size");
            _staffGroupcd = getUserGroupDatGroupcd(db2, request.getParameter("PRINT_LOG_STAFFCD"));

            _isSubclassOrderNotContainCurriculumcd = false; // _isKyoto;
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasANOTHER_CLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
            _hasANOTHER_SUBCLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);
            _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            _hasSCHREG_TRANSFER_DAT_REMARK1 = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_TRANSFER_DAT", "REMARK1");
            _isPrintYoshiki2OmoteTotalCreditByPage = true;
            _isSogoShoken3Bunkatsu = false; // _isTokiwa || _isKaijyo;
            _useStaffNameHistDat = false; // isMiyagi;
            _isPrintZaigakusubekiKikan = false; // !_isMiyagi;
//            if (_isKyoto) {
//                _isPrintAnotherStudyrec2 = !"1".equals(_seitoSidoYorokuNotPrintAnotherStudyrec); // プロパティが設定されていなければ前籍校を表示する
//                _printAnotherStudyrec3 = _printAnotherStudyrec3_2; // 様式2表に前籍校を表示するかは学校制度次第
//            } else {
                // 賢者
                _isPrintAnotherStudyrec2 = false; // 様式1裏に前籍校を表示しない
                _printAnotherStudyrec3 = _printAnotherStudyrec3_1; // 様式2表に前籍校を表示する
//            }
            _isPrintRisyuNomi = null == request.getParameter("RISYU") || "1".equals(request.getParameter("RISYU"));
            _isPrintMirisyu = null == request.getParameter("MIRISYU") || "1".equals(request.getParameter("MIRISYU"));
            _isPrintRisyuTourokuNomi = null == request.getParameter("RISYUTOUROKU") || "1".equals(request.getParameter("RISYUTOUROKU"));

            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); // 学年・組

            _schzip = request.getParameter("schzip");
            _schoolzip = request.getParameter("schoolzip");
            _colorPrint = request.getParameter("color_print");

            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear = request.getParameter("seitoSidoYorokuCyugakuKirikaeNendoForRegdYear");
            _seitoSidoYorokuCyugakuKirikaeNendo = NumberUtils.isDigits(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) ? Integer.parseInt(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) : 0;
            _seitoSidoYorokuCyugakuKantenNoBlank = request.getParameter("seitoSidoYorokuCyugakuKantenNoBlank");

//            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//            _imagePath = null == returnval ? null : returnval.val4; // 写真データ格納フォルダ
//            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _imagePath = "image/stamp";
            _extension = "bmp";

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00'")));

            _gradeCdMap = getGradeCdMap(db2);

            _d071hyoteiAlphabetMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D071' "), "NAMECD2", "NAME1");
            _d072SubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D072' AND NAME1 IS NOT NULL "), "NAME1");
            if (!_d071hyoteiAlphabetMap.isEmpty()) {
                log.info(" D071 hyoteiAlphabetMap = " + _d071hyoteiAlphabetMap);
            }
            if (!_d072SubclasscdList.isEmpty()) {
                log.info(" D072 SubclasscdList = " + _d072SubclasscdList);
            }

            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);

            _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J");
            _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability           = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability");
            _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J");
            _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability           = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability");
            _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J                   = request.getParameter("HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J");
            _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J                         = request.getParameter("HTRAINREMARK_DAT_VIEWREMARK_SIZE_J");
            _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability              = request.getParameter("HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability");
            _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J                 = request.getParameter("HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J");
            _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability        = request.getParameter("HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J             = request.getParameter("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability  = request.getParameter("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability");
            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J                        = request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_J");
            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability             = request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability");
            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J                   = request.getParameter("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J");
            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability        = request.getParameter("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability");
            _HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J                = request.getParameter("HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J");

            _dbPrgInfoProperties = getDbPrginfoProperties(db2);
            final String[] outputDebugArray = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebugArray, "1");
            _isOutputDebugStaff = ArrayUtils.contains(outputDebugArray, "staff");
            if (null != outputDebugArray) {
                log.info(" outputDebug = " + ArrayUtils.toString(outputDebugArray));
            }
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA130G' "), "NAME", "VALUE");
        }

        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = _gradeCdMap.get(year + grade);
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }

        private String getZ010(DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        /**
         * 後期課程の学科名を取得
         */
        private String getPrintMajorname(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAJORNAME FROM MAJOR_MST WHERE COURSECD = '1' ORDER BY COURSECD, MAJORCD "));
        }

        private List<Staff> getStudentStaffHistList(final Student student, final EntGrdHist entGrdHist, final String trDiv, final String year) {
            return _staffInfo.getStudentStaffHistList(_isOutputDebugStaff, _semesterMap, student._regdList, entGrdHist, trDiv, year);
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private Map getGradeCdMap(final DB2UDB db2) {
            final Map gdatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND IN ('" + Param.SCHOOL_KIND_H + "', '" + Param.SCHOOL_KIND_J + "') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    gdatMap.put(year + grade, rs.getString("GRADE_CD"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gdatMap;
        }

        private List createSchregnos(final DB2UDB db2, final Param param) {
            final List rtn;

            if ("2".equals(_output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT SCHREGNO FROM SCHREG_REGD_DAT ");
                stb.append("WHERE YEAR = '" + _year + "' ");
                stb.append("AND SEMESTER = '" + _gakki + "' ");
                stb.append("AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                stb.append("ORDER BY GRADE, HR_CLASS, ATTENDNO ");

                rtn = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO");

            } else {
                rtn = new ArrayList();
                for (int i = 0; i < _categorySelected.length; i++) {
                    rtn.add(_categorySelected[i]);
                }
            }
            return rtn;
        }

        /**
         * 学校区分名称
         */
        private void setNameMstZ001(final DB2UDB db2) {
            _z001name1 = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT T1.NAMECD2, T1.NAME1 FROM NAME_MST T1 WHERE T1.NAMECD1 = 'Z001' "), "NAMECD2", "NAME1");
        }

        private List<KNJA130_0> getPrintForm(final DB2UDB db2, final Vrw32alp svf) {
            final List<KNJA130_0> rtn = new ArrayList();

            // 様式１（学籍に関する記録）
            if (null != _seito) {
                if (null == _knja130_1) {
                    _knja130_1 = new KNJA130_1(svf, this);
                }
                rtn.add(_knja130_1);
            }

            // 様式１の裏（修得単位の記録）
            if (null != _tani) {
                if (null == _knja130_2) {
                    _knja130_2 = new KNJA130_2(svf, this);
                }
                rtn.add(_knja130_2);
            }

            // 様式２（指導に関する記録 前期課程）
            if (null != _zenkiGakushu) {
                if (null == _knja133j_3) {
                    _knja133j_3 = new KNJA133J_3(svf, this);
                }
                rtn.add(_knja133j_3);
            }

            // 様式２の裏（所見等 前期課程）
            if (null != _zenkiKatsudo) {
                if (null == _knja133j_4) {
                    _knja133j_4 = new KNJA133J_4(svf, this);
                }
                rtn.add(_knja133j_4);
            }

//            if (null != _gakushu && null != _katsudo && "1".equals(_seitoSidoYorokuYoshiki2PrintOrder)) {
//                // 様式２
//                if (null == _knja130_34) {
//                    _knja130_34 = new KNJA130_34(svf, this);
//                }
//                if (null == _knja130_3) {
//                    _knja130_3 = new KNJA130_3(svf, this);
//                }
//                if (null == _knja130_4) {
//                    _knja130_4 = new KNJA130_4(svf, this);
//                }
//                rtn.add(_knja130_34);
//            } else {
                // 様式２（指導に関する記録）
                if (null != _koukiGakushu) {
                    if (null == _knja130_3) {
                        _knja130_3 = new KNJA130_3(svf, this);
                    }
                    rtn.add(_knja130_3);
                }

                // 様式２の裏（所見等）
                if (null != _koukiKatsudo) {
                    if (null == _knja130_4) {
                        _knja130_4 = new KNJA130_4(svf, this);
                    }
                    rtn.add(_knja130_4);
                }
//            }
            for (final KNJA130_0 _0 : rtn) {
                _0.db2 = db2;
            }
            return rtn;
        }

        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private String getNameMstZ010(final DB2UDB db2, final String fieldname) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'"));
        }

        private String getUserGroupDatGroupcd(final DB2UDB db2, final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(GROUPCD) AS GROUPCD ");
            stb.append(" FROM ");
            stb.append("     USERGROUP_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.STAFFCD = '" + staffcd + "' ");

            String groupcd = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            log.debug(" groupcd = " + groupcd);
            return groupcd;
        }

        private void setSpecialDivNameMap(final DB2UDB db2) {
            _specialDivNameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A029' "), "NAMECD2", "NAME1");
        }

        private void setFuninteiRemarkFormat(final DB2UDB db2) {
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'A030' AND NAMECD2 = '00' "));
            _mirisyuRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE1");
            _risyunomiRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE2");
        }

        private void setOptionCreditOutput(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _optionCreditOutput = -1;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'A031' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (NumberUtils.isDigits(rs.getString("NAME1"))) {
                        _optionCreditOutput = Integer.parseInt(rs.getString("NAME1"));
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            final int min = OPTION_CREDIT0;
            final int max = OPTION_CREDIT2;
            if (_optionCreditOutput < min || max < _optionCreditOutput) {
//                if (_isKyoto) {
//                    _optionCreditOutput = OPTION_CREDIT1;
//                } else if (_isTottori) {
//                    _optionCreditOutput = OPTION_CREDIT2;
//                } else {
                    _optionCreditOutput = OPTION_CREDIT0;
//                }
            }
            log.debug(" optionCreditOutput = " + _optionCreditOutput);
        }

        /**
         * 普通・専門の文言
         * @param div 普通・専門区分　0:普通、1:専門
         * @return 文言
         */
        private String getSpecialDivName(final boolean isNewForm, String div) {
            if (null == div) {
                div = "0";
            }
            final String defaultname;
            if ("0".equals(div)) {
                // 普通教育
                defaultname = isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する各教科・科目";
            } else if ("1".equals(div)) {
                //　専門教科
                defaultname = isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する各教科・科目";
            } else if ("2".equals(div)) {
                // その他
                defaultname = "その他特に必要な教科・科目";
            } else {
                defaultname = "";
            }
            final String key;
            if (NumberUtils.isDigits(div)) {
                key = String.valueOf(Integer.parseInt(div) + 1);
            } else {
                key = "";
            }
            return StringUtils.defaultString((String) _specialDivNameMap.get(key), defaultname);
        }

        /**
         * 原級留置か?
         * 異なる年度で同じ学年があるなら改頁する機能のこと
         * @param db2 DB2UDB
         * @return 原級留置ならtrue
         */
        private void setGenkyuRyuchi(final DB2UDB db2) {
            _isGenkyuRyuchi = false;
            final String remark6 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT REMARK6 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "'"));
            if ("1".equals(remark6)) {
                _isGenkyuRyuchi = true;
            }
            log.debug("原級留置か? = " + _isGenkyuRyuchi);
        }

        public boolean isGdat(final String year, final String grade) {
            if (null == year || null == grade) {
                return false;
            }
            return _gdatHYearGradeSet.contains(year + ":" + grade);
        }

        public String getGdatGradeName2(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : _gdatGradeName2.get(year + ":" + grade);
            return rtn;
        }

        public String getGdatGradeCd(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : _gdatGradeCd.get(year + ":" + grade);
            return rtn;
        }

        public String getGdatSchoolKind(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : _gdatSchoolKind.get(year + ":" + grade);
            return rtn;
        }

        public void setPs(final String psKey, final DB2UDB db2, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private void setGdatHYearGradeSet(final DB2UDB db2) {
            _gdatHYearGradeSet = new HashSet();
            _gdatGradeName2 = new HashMap();
            _gdatGradeCd = new HashMap();
            _gdatSchoolKind = new HashMap();
            _gradeJ = new HashSet();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT YEAR, GRADE, SCHOOL_KIND, GRADE_NAME2, GRADE_CD FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND IN ('" + SCHOOL_KIND_H + "', '" + SCHOOL_KIND_J + "') ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String yg = rs.getString("YEAR") + ":" + rs.getString("GRADE");
                    _gdatHYearGradeSet.add(yg);
                    _gdatGradeName2.put(yg, rs.getString("GRADE_NAME2"));
                    _gdatGradeCd.put(yg, rs.getString("GRADE_CD"));
                    _gdatSchoolKind.put(yg, rs.getString("SCHOOL_KIND"));
                    if (SCHOOL_KIND_J.equals(rs.getString("SCHOOL_KIND"))) {
                        _gradeJ.add(rs.getString("GRADE"));
                    }
                }
                if (_seitoSidoYorokuZaisekiMae) {
                    _gdatHYearGradeSet.add("0:00");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

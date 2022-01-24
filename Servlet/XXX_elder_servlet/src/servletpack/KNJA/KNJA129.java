// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJPropertiesShokenSize.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJA.KNJA130CCommon.HtrainRemarkOnline;
import servletpack.KNJA.KNJA130CCommon.KNJSvfFieldInfo;
import servletpack.KNJA.KNJA130CCommon.KNJSvfFieldModify;
import servletpack.KNJA.KNJA130CCommon.Property;
import servletpack.KNJA.KNJA130CCommon.SchoolDiv;
import servletpack.KNJA.KNJA130CCommon.SchregRegdDat;
import servletpack.KNJA.KNJA130CCommon.SchregRegdGdat;
import servletpack.KNJA.KNJA130CCommon.SchregRegdHdat;
import servletpack.KNJA.KNJA130CCommon.Util;
import servletpack.KNJA.KNJA130CCommon.Z010;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfForm;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA129 {

    private static final Log log = LogFactory.getLog(KNJA129.class);

    public static boolean DEBUG = false;
    private static final String csv = "csv";

    private boolean _hasdata = false;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Id$"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("exception:", ex);
            return;
        }

        final String cmd = request.getParameter("cmd");
        Param param = null;
        try {
            if (!csv.equals(cmd)) {
                response.setContentType("application/pdf");

                svf.VrInit();                             //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            }

            param = getParam(request, db2, request.getParameter("CTRL_YEAR"), request.getParameter("CTRL_SEMESTER"), Student.getSchoolKind(db2, request.getParameter("SCHREGNO"), request.getParameter("CTRL_YEAR")));

            Student student = createStudent(db2, param, request.getParameter("SCHREGNO"), null, null, null);

//          if (csv.equals(cmd)) {
//              final List<List<String>> outputLines = new ArrayList<List<String>>();
//              final Map csvParam = new HashMap();
//              csvParam.put("HttpServletRequest", request);
//              printSvf(db2, null, param, outputLines);
//              final Map map = new HashMap();
//              map.put("TITLE", "生徒指導要録");
//              map.put("OUTPUT_LINES", outputLines);
//              CsvUtils.outputJson(log, request, response, CsvUtils.toJson(map), new HashMap());
//          } else {

              // 印刷処理
            printSvf(db2, svf, param, student, null);
//          }

        } catch (Exception ex) {
            log.error("exception:", ex);
        } finally {
            if (!csv.equals(cmd)) {
                if (!_hasdata) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note" , "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }
            if (null != param) {
                param.closeForm();
                param.psCloseQuietly();
            }

            if (null != db2) {
                db2.close();
            }
        }
    }

    public boolean hasData() {
        return _hasdata;
    }

    public Param getParam(final HttpServletRequest request, final DB2UDB db2, final String year, final String gakki, final String schoolKind) {
        final Map<String, String> otherParamMap = new HashMap<String, String>();
        otherParamMap.put("KNJA129_SCHOOL_KIND", schoolKind);
        final Param param = new Param(request, db2, year, gakki, otherParamMap);
        return param;
    }

//    public void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final List<List<String>> csvLines) {
//
//        final List<Student> studentList = Student.createStudents(db2, param);
//
//        for (final Iterator<Student> stit = studentList.iterator(); stit.hasNext();) {
//            final Student student = stit.next();
//            student._schregEntGrdHistComebackDatList = Collections.emptyList();
//            List<String> comebackDateList = Collections.emptyList();
//
//            boolean useStartYear = comebackDateList.size() > 0;
//            student.load(db2, param, useStartYear);
//            for (final String comebackDate : comebackDateList) {
//                final boolean useStartYear2 = student._schregEntGrdHistComebackDatList.size() > 0;
//                final PersonalInfo comebackPersonalInfo = PersonalInfo.loadPersonal(db2, student, useStartYear2, comebackDate, param);
//                student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
//            }
//            log.info(" schregno = " + student._schregno + " (学年制 = " + param._schoolDiv.isGakunenSei(null, null, student) + ")");
//
//            for (final KNJA130_0 form : param._printForm) {
//                for (int egi = 0; egi < student._printEntGrdHistList.size(); egi++) {
//                    final PersonalInfo pInfo = student._printEntGrdHistList.get(egi);
//                    if (param._isOutputDebug) {
//                        log.info(" entgrd idx = " + egi);
//                    }
//                    form.setDetail(db2, student, pInfo, csvLines);
//                    if (form.nonedata) {
//                        _nonedata = true;
//                    }
//                    form.closeSession();
//                }
//            }
//            stit.remove();
//        }
//        param.closeForm();
//        param.psCloseQuietly();
//    }

    private Student createStudent(final DB2UDB db2, final Param param, final String schregno, final String[] nameLines, final String schoolKind, final List<String> years) {
        final Student student = new Student(schregno);
        student._nameLines = nameLines;
        student._schoolKind = schoolKind;
        student.load(db2, param, false);
        return student;
    }

    public void printSvf(final Param param, final DB2UDB db2, final Vrw32alp svf, final String schregno, final String[] nameLines, final String schoolKind, final List<String> years, final List<List<String>> csvLines) {
        Student student = createStudent(db2, param, schregno, nameLines, schoolKind, years);
        printSvf(db2, svf, param, student, csvLines);
//        param.closeForm();
//        param.psCloseQuietly();
    }

    private void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final List<List<String>> csvLines) {
        if (null == param._knja130_4) {
            param._knja130_4 = new KNJA129_4(svf, param);
        }
        param._knja130_4.setDetail(db2, student._pInfo, csvLines);
        if (param._knja130_4.hasdata) {
            _hasdata = true;
        }
    }


    public static class Student extends KNJA130CCommon.Student {

        String[] _nameLines = null;
        String _schoolKind;

        PersonalInfo _pInfo;

        Map<String, HtrainRemarkOnline> _htrainRemarkOnlineMap;

        List<SchregRegdDat> _regdList = Collections.emptyList();

        private Student(final String schregno) {
            super(schregno);
        }
        private void load(final DB2UDB db2, final Param param, final boolean useStartYear) {

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

            if (null == _nameLines) {
                final String psSchregName = "PS_SCHREG_NAME";
                if (null == param.getPs(psSchregName)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT NAME ");
                    stb.append(" FROM SCHREG_BASE_MST T1 ");
                    stb.append(" WHERE T1.SCHREGNO = ? ");

                    if (param._isOutputDebugQuery) {
                        log.info(" name sql = " + stb.toString());
                    }

                    param.setPs(psSchregName, db2, stb.toString());
                }
                _nameLines = new String[] { KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psSchregName), new Object[] {_schregno})) };
            }

            if (null == _schoolKind) {
                _schoolKind = getSchoolKind(db2, _schregno, param._year);
            }

            final String psMajorYdatSchooldivKey = "PS_MAJOR_YDAT_SCHOOLDIV";
            if (null == param.getPs(psMajorYdatSchooldivKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD AS ( ");
                stb.append("   SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                stb.append("   FROM SCHREG_REGD_DAT ");
                stb.append("   WHERE SCHREGNO = ? ");
                stb.append("   GROUP BY SCHREGNO, YEAR ");
                stb.append(" ) ");
                stb.append(" SELECT T1.SCHREGNO, T1.YEAR, T4.SCHOOLDIV ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append(" LEFT JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ");
                stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
                stb.append(" INNER JOIN MAJOR_YDAT T4 ON T4.YEAR = T1.YEAR ");
                stb.append("     AND T4.COURSECD = T1.COURSECD ");
                stb.append("     AND T4.MAJORCD = T1.MAJORCD ");
                stb.append(" ORDER BY T1.YEAR, T1.SEMESTER ");

                if (param._isOutputDebugQuery) {
                    log.info(" majorYdatSchooldiv sql = " + stb.toString());
                }

                param.setPs(psMajorYdatSchooldivKey, db2, stb.toString());
            }

            if ("1".equals(param.property(Property.useGakkaSchoolDiv))) {

                _yearMajorYdatSchooldivMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, param.getPs(psMajorYdatSchooldivKey), new String[] { _schregno }), "YEAR", "SCHOOLDIV");
                if (param._isOutputDebug) {
                    log.info(" schoolmst.schooldiv = " + param._schoolDiv._yearSchoolDivMap +
                            ", yearMajorYdatSchooldivMap = " + _yearMajorYdatSchooldivMap +
                            " -> "+  param._schoolDiv.getSchooldiv(null, null, this));
                }

            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            _pInfo = PersonalInfo.loadPersonal(db2, this, useStartYear, null, param);

            _htrainRemarkOnlineMap = HtrainRemarkOnline.loadHtrainRemarkOnline(db2, param, _schregno, param._year);

        }

        private static String getSchoolKind(final DB2UDB db2, final String schregno, final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT MAX(GDAT.SCHOOL_KIND) ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("     ON GDAT.YEAR = T1.YEAR ");
            stb.append("    AND GDAT.GRADE = T1.GRADE ");
            stb.append(" WHERE T1.SCHREGNO = ? AND T1.YEAR = ? ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString(), new String[] { schregno, year }));
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiAttendRec(final DB2UDB db2, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final Param param) {
            final Map<String, Gakuseki> yearGakusekiMap = new TreeMap<String, Gakuseki>();
            final Set<String> notPrintYearSet = new TreeSet<String>();

            for (final Gakuseki gakuseki : gakusekiList) {
                yearGakusekiMap.put(gakuseki._year, gakuseki);
            }
            if (param._isOutputDebug) {
                if (notPrintYearSet.size() > 0) {
                    log.info(" COMEBACK_DATの年度は印字しない: " + notPrintYearSet);
                }
            }

            final List<Gakuseki> list = new LinkedList<Gakuseki>(yearGakusekiMap.values());
            return list;
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }

        public HtrainRemarkOnline getHtrainremarkOnline(final String year) {
            return Util.def(_htrainRemarkOnlineMap.get(year), HtrainRemarkOnline.Null);
        }
    }

    private static class PrintGakuseki {
        int _pageIdx;
        final List<String> _yearList = new ArrayList<String>();
        final TreeMap<String, Gakuseki> _yearGakusekiMap = new TreeMap<String, Gakuseki>();
        final Map<String, Integer> _yearGakusekiPositionMap = new TreeMap<String, Integer>();
        public Gakuseki _dropGakuseki = null;
        public boolean _isLastPrintGakuseki = false;

        boolean includeZaisekimae = false;
        boolean hasZeroPrintFlg = false;

        private List<Gakuseki> _gakusekiList;

        private boolean _isYoshiki2omote3KantenForm;

        public String toString() {
            return "PrintGakuseki(pageIdx = " + _pageIdx + Util.debugMapToStr(", _yearGakusekiMap=", _yearGakusekiMap, "\n") + ", yearGakusekiPositionMap=" + _yearGakusekiPositionMap + ")";
        }

        public TreeSet<String> yearSet() {
            final TreeSet<String> yearSet = new TreeSet(_yearGakusekiPositionMap.keySet());
            if (yearSet.isEmpty()) {
                yearSet.add("0");
            }
            return yearSet;
        }

        public String minYear() {
            return yearSet().first();
        }

        public String maxYear() {
            return yearSet().last();
        }

        public boolean hasZeroPrintFlg(final Param param) {
            boolean hasZeroPrintFlg = false;
            for (final Gakuseki gakuseki : _yearGakusekiMap.values()) {
                if (param._is133m) {
                } else {
                    if (gakuseki.isNyugakumae()) {
                        hasZeroPrintFlg = true; // 入学前を出力した。
                    }
                }
            }
            return hasZeroPrintFlg;
        }

        public static PrintGakuseki getPrintGakuseki(final List<Gakuseki> gakusekiList) {
            final PrintGakuseki pg = new PrintGakuseki();
            int pos = 1;
            pg._gakusekiList = gakusekiList;
            for (final Gakuseki g : gakusekiList) {
                pg._yearList.add(g._year);
                pg._yearGakusekiMap.put(g._year, g);
                pg._yearGakusekiPositionMap.put(g._year, new Integer(pos));
                pos += 1;
                if (g._isDrop) {
                    pg._dropGakuseki = g;
                }
            }
            return pg;
        }

        public static Map<Integer, PrintGakuseki> getPrintGakusekiPageMap(final List<List<Gakuseki>> gakusekiListList) {
            final Map<Integer, PrintGakuseki> rtn = new TreeMap();
            for (final List<Gakuseki> gakusekiList : gakusekiListList) {
                final PrintGakuseki printGakuseki = getPrintGakuseki(gakusekiList);
                printGakuseki._pageIdx = rtn.size();
                rtn.put(printGakuseki._pageIdx + 1, printGakuseki);
            }
            return rtn;
        }
    }

    /**
     * <<生徒の学籍履歴クラス>>。
     */
    private static class Gakuseki extends KNJA130CCommon.Gakuseki {

        private static String GAKUSEKI_DATA_FLG1 = "1";
        private static String GAKUSEKI_DATA_FLG2_ZAISEKIMAE = "2";
        private static String GAKUSEKI_DATA_FLG3_ZAISEKIMAE_NENDOARI = "3";

        private static String NYUGAKUMAE = "入学前";

        public static class GakusekiComparator implements Comparator<Gakuseki> {
            final Student _student;
            final PersonalInfo _pInfo;
            final Param _param;
            GakusekiComparator(final Student student, final PersonalInfo pInfo, final Param param) {
                _student = student;
                _pInfo = pInfo;
                _param = param;
            }
            /**
             * {@inheritDoc}
             */
            public int compare(final Gakuseki g1, final Gakuseki g2) {
                int rtn = 0;

                if (Gakuseki.nyugakuMaeHaMigi(_student, _pInfo, _param)) {
                    rtn = g1._dataflg.compareTo(g2._dataflg);
                    if (0 != rtn) {
                        return rtn;
                    }
                }
                rtn = g1._year.compareTo(g2._year);
                return rtn;
            }
        }

        String _grade;
        SchregRegdGdat _gdat = SchregRegdGdat.create();
        final String _nendo;
        SchregRegdHdat _hdat = SchregRegdHdat.create();
        String _attendno;
        String _hr_class;
        String _annual;
        String _dataflg; // 在籍データから作成したか
        boolean _isDrop;
        boolean _isKoumokuGakunen;

        private Gakuseki(final DB2UDB db2, final Student student, final Map row, final PersonalInfo pInfo, final Param param) {
            super(KnjDbUtils.getString(row, "YEAR"));
            final String nendoDate = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31"; // 年度終了日
            final String nendo;
            if (param._isSeireki) {
                nendo = String.valueOf(_year) + "年度";
            } else {
                nendo = KNJA129_0.dateNendoWareki(db2, nendoDate);
            }
            _nendo = nendo;
            _grade = KnjDbUtils.getString(row, "GRADE");
            _gdat = SchregRegdGdat.getSchregRegdGdat(_year, _grade, param._gdatMap);
            _hr_class = KnjDbUtils.getString(row, "HR_CLASS");
            _hdat = SchregRegdHdat.getSchregRegdHdat(_year, KnjDbUtils.getString(row, "SEMESTER"), _grade, _hr_class, param._hrdatMap);

            final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
            _attendno = !NumberUtils.isDigits(attendno) ? attendno : String.valueOf(Integer.parseInt(attendno)) + (param._z010.in(Z010.tokiwa) ? "番" : "");
            _annual = Util.toDigit(KnjDbUtils.getString(row, "ANNUAL"), "");
            _dataflg = GAKUSEKI_DATA_FLG1;

            _isKoumokuGakunen = param._schoolDiv.isKoumokuGakunen(param, _year, pInfo, student).booleanValue();
        }

        private static boolean nyugakuMaeHaMigi(final Student student, final PersonalInfo pInfo, final Param param) {
            return param._schoolDiv.isGakunenSei(null, pInfo, student);
        }

        private String getGradeOrNendo(final Param param) {
            final String head = _isKoumokuGakunen ? _gdat._gradeName2 : getNendo2(param);
            return Util.defstr(head);
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
                if (param._z010.in(Z010.kyoto) || param._z010.in(Z010.miyagiken)) {
                    return _nendo;
                }
                return arNendo[1] + arNendo[2];
            }
        }

        public boolean isNotPrint(final Param param) {
            boolean isNotPrint = false;
            if (param._is133m && param._z010.in(Z010.miyagiken) && "999".equals(_hr_class)) {
                // 宮城はクラス"未登録"(HR_CLASS='999')を表示しない
                isNotPrint = true;
            }
            return isNotPrint;
        }

        public String toString() {
            return "Gakuseki (year = " +_year + ", grade = " + _grade + ", gdat = " + _gdat + ", hr_class = " + _hdat + ", attendno = " + _attendno + ", drop = " + _isDrop + ", dataFlg  = " + _dataflg + ", nendo = " + _nendo + ")";
        }
    }

    /**
     * <<生徒情報クラス>>。
     */
    private static class PersonalInfo extends KNJA130CCommon.PersonalInfo {

        private final Student _student;
        String _title;

        final Map _regRow;
        final Map _entGrdRow;
        final String _comebackDate;

        List<Gakuseki> _gakusekiList = Collections.emptyList();

        /**
         * コンストラクタ。
         */
        private PersonalInfo(
                final DB2UDB db2,
                final Student student,
                final Param param,
                final Map regRow,
                final Map entGrdRow,
                final String comebackDate
        ) {
            super(student._schregno);
            _student = student;
            _regRow = regRow;

            _comebackDate = comebackDate;

            _entGrdRow = entGrdRow;
        }

        public List<? extends Gakuseki> getGakusekiList() {
            return _gakusekiList;
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

        /**
         * 個人情報クラスを作成ます。
         */
        private static PersonalInfo loadPersonal(final DB2UDB db2, final Student student, final boolean useStartYear, final String comebackDate, final Param param) {

            final Map regRow = getRegRow(db2, student, param);

            final Map entGrdRow = getEntGrdRow(db2, student, student._schoolKind, comebackDate, param);

            final PersonalInfo pInfo = new PersonalInfo(db2, student, param, regRow, entGrdRow, comebackDate);

            loadOthers(db2, student, param, null, pInfo);
            return pInfo;
        }

        public static void loadOthers(final DB2UDB db2, final Student student, final Param param, final String startYear, final PersonalInfo pi) {
            pi._gakusekiList = loadGakuseki(db2, student, pi, param, startYear, null);
            pi._title = param._schoolDiv.isKoumokuGakunen(param, null, pi, student).booleanValue() ? "学年" : "年度";
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final String schoolKind, final boolean useStartYear) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MAX(SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = ? ");
            stb.append("     GROUP BY YEAR ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND ? BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("     WHERE T1.SCHREGNO = ? ");

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
            stb.append("          AND T2.SCHOOL_KIND = '" + schoolKind + "' ");
            stb.append("      WHERE   T1.SCHREGNO = ? ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            if (useStartYear) {
                stb.append("          AND T1.YEAR >= ? ");
            }
            stb.append("          AND T1.SEMESTER = (SELECT SEMESTER FROM MIN_YEAR_SEMESTER WHERE YEAR = T1.YEAR) ");

            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.SEMESTER ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");

            stb.append(" FROM REGD T1 ");

            stb.append(" ORDER BY T1.YEAR, T1.HR_CLASS");
            return stb.toString();
        }

//        /**
//         * 証明書不発行か
//         * @param grdDate 生徒の卒業日付
//         * @param elapsedYears 発行を許可する卒業経過年数 (CERTIF_KIND_MST.ELAPSED_YEARS)
//         * @return 不発行(卒業日付に経過年数を加算した日付をシステム日付が超える)ならtrue、それ以外はfalse
//         */
//        private static boolean isFuhakkou(final String grdDate, final int elapsedYears) {
//            if (null == grdDate) {
//                //log.debug(" grdDate = " + grdDate + ", elapsedYears = " + elapsedYears);
//                return false;
//            }
//
//            final Calendar hakkoulimit = Calendar.getInstance();
//            hakkoulimit.setTime(java.sql.Date.valueOf(grdDate));
//            hakkoulimit.add(Calendar.YEAR, elapsedYears);
//
//            final Calendar now = Calendar.getInstance();
//            now.set(Calendar.HOUR, 0);
//            now.set(Calendar.MINUTE, 0);
//            now.set(Calendar.SECOND, 0);
//            now.set(Calendar.MILLISECOND, 0);
//
//            final boolean isFuhakkou = hakkoulimit.equals(now) || hakkoulimit.before(now);
//            if (isFuhakkou) {
//                final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                log.info(" hakkouLimit = " + df.format(hakkoulimit.getTime()) + ", now = " + df.format(now.getTime()) + ", isFuhakkou = " + isFuhakkou);
//            }
//            return isFuhakkou;
//        }

        /**
         * 学籍履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<Gakuseki> loadGakuseki(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Param param, final String startYear, String grdDate) {
            final List<Gakuseki> gakusekiList = new ArrayList<Gakuseki>();
            grdDate = StringUtils.isBlank(grdDate) ? "9999-12-31" : grdDate;
            final String psKey;
            final boolean useStartYear = null != startYear;
            final String[] queryArg;
            if (useStartYear) {
                psKey = "loadGakuseki.sqlSchGradeRecUseMinYear";
                queryArg = new String[] { student._schregno, grdDate , student._schregno, student._schregno, startYear};
            } else {
                psKey = "loadGakuseki.sqlSchGradeRec";
                queryArg = new String[] { student._schregno, grdDate , student._schregno, student._schregno};
            }
            if (null == param.getPs(psKey)) {
                final String sql = sqlSchGradeRec(param, student._schoolKind, useStartYear);

                if (param._isOutputDebugQuery) {
                    log.info(" " + psKey + " sql = " + sql);
                }

                param.setPs(psKey, db2, sql);
            }

            if (param._isOutputDebugQuery) {
                log.info(" " + psKey +" queryArg = " + ArrayUtils.toString(queryArg));
            }
            final Map<String, Gakuseki> gakusekiMap = new HashMap<String, Gakuseki>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), queryArg)) {

                final Gakuseki gakuseki = new Gakuseki(db2, student, row, pInfo, param);
                if (param._is133m) {
                    gakusekiList.add(gakuseki);
                } else {
                    if (!param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        continue;
                    }
//                            log.debug(" gakuseki = " + gakuseki);
                    gakusekiList.add(gakuseki);
                }
                gakusekiMap.put(gakuseki._year, gakuseki);
            }

            // リストをソートします。
            Collections.sort(gakusekiList, new Gakuseki.GakusekiComparator(student, pInfo, param));

            final boolean ryunenTaiou = param._schoolDiv.isGakunenSei(null, pInfo, student) || student.certifSchool(param)._isGenkyuRyuchi;
            // 学年制の場合、留年対応します。
            if (param._isOutputDebugData) {
                log.info(" 留年対応 = " + ryunenTaiou);
            }
            if (ryunenTaiou) {
                String grade = null;
                for (final ListIterator<Gakuseki> it = gakusekiList.listIterator(gakusekiList.size()); it.hasPrevious();) {
                    final Gakuseki gaku = it.previous();
                    if (null != grade && gaku._grade.equals(grade)) {
                        gaku._isDrop = true;
                        if (param._isOutputDebug) {
                            log.info(" drop " + gaku._grade + " " + gaku._year);
                        }
                    }
                    grade = gaku._grade;
                }
            }
            return gakusekiList;
        }

    }

    private interface Page {
        public void printPage(final DB2UDB db2, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines);
    }

    private static abstract class KNJA129_0 extends KNJA130CCommon.KNJA130_0 {

        private Param _param;

        protected boolean hasdata; // データ有りフラグ

        protected int _gradeLineMax;
        protected boolean _isPrintEduDiv2CharsPerLine; // 様式2表の教科専門区分文言は1行2文字ずつ表示

        KNJA129_0(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _param = param;
        }

        protected String modifyForm0(final String form, final Student student, final PersonalInfo pInfo, final PrintGakuseki pg, final Map<String, String> flgMap) {

            String formCreateFlg = Util.mkString(flgMap, "|").toString();
            if (param()._isOutputDebug) {
                log.info(" form config Flg = " + formCreateFlg);
            }
            if (StringUtils.isEmpty(formCreateFlg)) {
                return form;
            }
            formCreateFlg = form + "::" + formCreateFlg;
            if (null != _form._createFormFiles.get(formCreateFlg)) {
                return _form._createFormFiles.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(_form._svf.getPath(form)));
                if (svfForm.readFile()) {

                    modifySvfForm(pInfo, svfForm, pg, flgMap);

                    final File newFormFile = svfForm.writeTempFile();
                    _form._createFormFiles.put(formCreateFlg, newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            File newFormFile = _form._createFormFiles.get(formCreateFlg);
            if (null != newFormFile) {
                return newFormFile.getName();
            }
            return form;
        }

        // 使用する際はoverrideしてください
        /**
         *
         * @param personalInfo
         * @param svfForm
         * @param printGakuseki
         * @param flgMap
         * @return 修正フラグ
         */
        protected boolean modifySvfForm(final PersonalInfo personalInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {
            log.warn("not implemented.");
            return false;
        }

        protected Param param() {
            return _param;
        }

        public abstract void setDetail(final DB2UDB db2, final PersonalInfo pInfo, final List<List<String>> csvLines);

        public enum GakusekiColumn {
            NORMAL,
            INCLUDE_ZAISEKI_MAE,
            SEQ
        }

        public static int pos(final int p, final int max) {
            return 0 == p % max ? max : p % max;
        }

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
         *  @param max 枠数
         * @return
         */
        public static int getGradeColumnNum(final PersonalInfo pInfo, final int i, final Gakuseki gakuseki, final GakusekiColumn flg, final Param param, final int max) {
            if (GakusekiColumn.SEQ == flg || GakusekiColumn.INCLUDE_ZAISEKI_MAE == flg) {
                return i;
            } else if ((param._schoolDiv.isGakunenSei(gakuseki._year, pInfo, pInfo._student) || pInfo._student.certifSchool(param)._isGenkyuRyuchi)) {
                // 年次の位置に表示
                final int gradeCdInt = Util.parseIntSafe(gakuseki._gdat._gradeCd, -1);
                return pos(gradeCdInt, max);
            } else {
                return i;
            }
        }

        protected void printName(final PersonalInfo pInfo, final KNJSvfFieldInfo _name) {
            final String field = _name._field;
            final String field1 = _name._field1;
            final String field2 = _name._field2;
            printName(pInfo, field, field1, field2, _name);
        }

        protected void printName(final PersonalInfo pInfo, final String field, final String field1, final String field2, final KNJSvfFieldInfo _name) {
            if (!hasField(_name._field) && !hasField(field)) {
                log.info(" has no field : " + _name._field);
                return;
            }
            final int width = _name._x2 - _name._x1;
//            if (pInfo._isPrintRealName &&
//            		pInfo._isPrintNameAndRealName &&
//            		!StringUtils.isBlank(pInfo._studentRealName + pInfo._studentName) &&
//            		!pInfo._studentRealName.equals(pInfo._studentName) &&
//            		null != field1 && null != field2
            if (
                    null != pInfo._student._nameLines && pInfo._student._nameLines.length > 1 &&
                    !StringUtils.isBlank(Util.defstr(pInfo._student._nameLines[0]) + Util.defstr(pInfo._student._nameLines[1])) &&
                    !Util.defstr(pInfo._student._nameLines[0]).equals(Util.defstr(pInfo._student._nameLines[1])) &&
                    null != field1 && null != field2
            ) {
                final String printName1 = Util.defstr(pInfo._student._nameLines[0]);
                final String printName2 = Util.defstr(pInfo._student._nameLines[1]);
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(field1, width, _name._height, _name._ystart1, _name._minnum, _name._maxnum);
                final double charSize1 = modify1.getCharSize(printName1);
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(field2, width, _name._height, _name._ystart2, _name._minnum, _name._maxnum);
                final double charSize2 = modify2.getCharSize(printName2);
                final double charSize = Math.min(charSize1, charSize2);
                svfVrAttribute(field1, "Size=" + charSize);
                svfVrAttribute(field2, "Size=" + charSize);
                svfVrsOut(field1, printName1);
                svfVrsOut(field2, printName2);
            } else {
                final String printName = null == pInfo._student._nameLines || pInfo._student._nameLines.length == 0 ? null : pInfo._student._nameLines[0];
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(field, width, _name._height, _name._ystart, _name._minnum, _name._maxnum);
                if (null != _name._param) {
                    modify._param = _name._param;
                }
                final double charSize = modify.getCharSize(printName);
                svfVrAttribute(field, "Size=" + charSize);
                svfVrAttribute(field, "Y=" + (int) modify.getYjiku(0, charSize));

                svfVrsOut(field, printName);
            }
        }

        enum TargetGakusekiFlag {
            Normal
          , NotPrint2Omote
          , NotTarget2Omote
          , NotGdatH2Omote
          , PrintZaisekiMae2Omote
          , NotPrintZaisekiMae2Omote
          , NotPrint2Ura
          , NotPrintTargetEtc2Ura
          , NotPrintTargetYear2Ura
            ;

            public boolean isTarget() {
                return Arrays.asList(Normal, PrintZaisekiMae2Omote).contains(this);
            }
        }

        private static TargetGakusekiFlag getTargetGakuseki(final PersonalInfo pInfo, final Gakuseki gakuseki, final Param param) {
            TargetGakusekiFlag isTarget = TargetGakusekiFlag.Normal;
//            if (yoshiki == YOSHIKI._2_OMOTE) {
//                if (gakuseki.isNotPrint(param)) {
//                    isTarget = TargetGakusekiFlag.NotPrint2Omote;
//                }
//                if (Gakuseki.GAKUSEKI_DATA_FLG1.equals(gakuseki._dataflg)) {
//                    if (!param.isGdatH(gakuseki._year, gakuseki._grade)) {
//                        isTarget = TargetGakusekiFlag.NotGdatH2Omote;
//                    }
//                } else if (Gakuseki.GAKUSEKI_DATA_FLG2_ZAISEKIMAE.equals(gakuseki._dataflg) || Gakuseki.GAKUSEKI_DATA_FLG3_ZAISEKIMAE_NENDOARI.equals(gakuseki._dataflg)) {
//                    if (!param._is133m) {
//                        if (param._seitoSidoYorokuZaisekiMae) { // TODO: ???
//                            if (Gakuseki.nyugakuMaeHaMigi(pInfo._student, pInfo, param)) {
//                                isTarget = TargetGakusekiFlag.PrintZaisekiMae2Omote; // 在籍前を印字する
//                            }
//                        } else {
//                            isTarget = TargetGakusekiFlag.NotPrintZaisekiMae2Omote;
//                        }
//                    }
//                }
//            } else if (yoshiki == YOSHIKI._2_URA) {
                if (gakuseki.isNotPrint(param)) {
                    isTarget = TargetGakusekiFlag.NotPrint2Ura;
                }
                if (param._is133m) {
                } else {
                    if (!(param._schoolDiv.isGakunenSei(gakuseki._year, null, pInfo._student) || pInfo._student.certifSchool(param)._isGenkyuRyuchi) && gakuseki.isNyugakumae() || !param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        isTarget = TargetGakusekiFlag.NotPrintTargetEtc2Ura;
                    }
                }
//            }
            if (param._isOutputDebugGakuseki) {
                log.info(" gakuseki " + gakuseki + " isTarget = " + isTarget);
            }
            return isTarget;
        }

        private static PrintGakuseki getPagedPrintGakuseki(final Map<Integer, PrintGakuseki> m, final int page) {
            if (null == m.get(page)) {
                m.put(page, new PrintGakuseki());
            }
            return m.get(page);
        }

        private static Map<Integer, PrintGakuseki> getPagePrintGakusekiMap(final DB2UDB db2, final PersonalInfo pInfo, final Param param, int max) {
            final List<Gakuseki> gakusekiAllList = KNJA129_0.getPageGakusekiList(db2, pInfo, param);
            if (param._isOutputDebugGakuseki) {
                log.info(" gakusekiAllList = " + gakusekiAllList);
            }

            final Map<Integer, PrintGakuseki> pageGakusekiListMap = new TreeMap<Integer, PrintGakuseki>();

            final int zaisekimaePos = max; // param._isMieken ? 1 : max;
            final List<Gakuseki> targetGakusekiList = new ArrayList<Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiAllList) {
                if (getTargetGakuseki(pInfo, gakuseki, param).isTarget()) {
                    targetGakusekiList.add(gakuseki);
                }
            }
            final Gakuseki lastGakuseki = (targetGakusekiList.size() == 0 ? null : targetGakusekiList.get(targetGakusekiList.size() - 1));

            if (param._isOutputDebug) {
                log.info(" PrintGakuseki src = " + Util.listString(gakusekiAllList, 0));
            }

//            Map<YOSHIKI2_OMOTE_VER, List<Gakuseki>> yoshikiVerYearListMap = null;
            final List<List<Gakuseki>> gakusekiListList;
//            if (yoshiki == YOSHIKI._2_OMOTE) {
//                yoshikiVerYearListMap = YOSHIKI2_OMOTE_VER.getYoshikiVerYearListMap(param, gakusekiAllList);
//                if (param._isOutputDebug) {
//                    log.info(" yoshikiVerYearListMap size = " + yoshikiVerYearListMap.size());
//                    for (final Map.Entry<YOSHIKI2_OMOTE_VER, List<Gakuseki>> e : yoshikiVerYearListMap.entrySet()) {
//                        log.info(" yoshikiVerYearListMap " + e.getKey() + " = " + Util.listString(e.getValue(), 1));
//                    }
//                }
//                gakusekiListList = new ArrayList<List<Gakuseki>>(yoshikiVerYearListMap.values());
//            } else {
//            	gakusekiListList = Collections.singletonList(gakusekiAllList);
//            }
            gakusekiListList = Collections.singletonList(gakusekiAllList);

            int ipage = 1;
            int pos = 1;
            for (int di = 0; di < gakusekiListList.size(); di++) {

                if (di != 0) {
                    ipage += 1;
                }
                boolean isAbroadPrintDrop = false;
                final List<Gakuseki> gakusekiList = gakusekiListList.get(di);

                for (final Gakuseki gakuseki : gakusekiList) {

                    if (param._isOutputDebugGakuseki) {
                        log.info(" target gakuseki = " + gakuseki);
                    }
                    final TargetGakusekiFlag targetGakuseki = getTargetGakuseki(pInfo, gakuseki, param);
                    if (!targetGakuseki.isTarget()) {
                        log.info(" not target : " + gakuseki + ", " + targetGakuseki);
                        continue;
                    }
                    PrintGakuseki printGakuseki = getPagedPrintGakuseki(pageGakusekiListMap, new Integer(ipage));
                    printGakuseki._isLastPrintGakuseki = printGakuseki._isLastPrintGakuseki || lastGakuseki == gakuseki;
//                    if (yoshiki == YOSHIKI._2_OMOTE) {
//                        printGakuseki._isYoshiki2omote3KantenForm = yoshikiVerYearListMap.get(YOSHIKI2_OMOTE_VER._3KANTEN) == gakusekiList;
//                    }

                    int newpos = -1;
                    boolean zaisekimae = false;
                    if (isAbroadPrintDrop) {
                        newpos = pos;
                    } else {
                        if (targetGakuseki == TargetGakusekiFlag.PrintZaisekiMae2Omote) {
                            printGakuseki.includeZaisekimae = true;
                            zaisekimae = true;
                        }
                        if (zaisekimae) {
                            newpos = zaisekimaePos;
                        } else if (printGakuseki.includeZaisekimae) {
                            newpos = getGradeColumnNum(pInfo, pos, gakuseki, GakusekiColumn.INCLUDE_ZAISEKI_MAE, param, max);
                        } else {
                            newpos = getGradeColumnNum(pInfo, pos, gakuseki, GakusekiColumn.NORMAL, param, max);
                        }
                    }
                    if (param._isOutputDebugGakuseki) {
                        log.info(" ==> col = " + newpos);
                        log.info(" newpos = " + newpos + " / current pos = " + printGakuseki._yearGakusekiPositionMap);
                    }
                    if (printGakuseki._yearGakusekiPositionMap.values().contains(newpos)) {
                        ipage += 1;
                        printGakuseki = getPagedPrintGakuseki(pageGakusekiListMap, ipage);
                        if (param._isOutputDebug) {
                            log.info(" newpage. (contains " + newpos + "), page = " + pageGakusekiListMap.size());
                        }
                        if (!param._schoolDiv.isGakunenSei(null, pInfo, pInfo._student)) {
                            newpos = 1;
                        }
                    }
                    printGakuseki._yearGakusekiPositionMap.put(gakuseki._year, newpos);
                    printGakuseki._yearList.add(gakuseki._year);
                    printGakuseki._yearGakusekiMap.put(gakuseki._year, gakuseki);

                    // 留年以降を改ページします。
                    isAbroadPrintDrop = false;
                    if (zaisekimae) {
                    } else if (gakuseki._isDrop) {
                        printGakuseki._dropGakuseki = gakuseki;
                        if (printGakuseki.hasZeroPrintFlg(param)) {
                            if (param._is133m) {
                                pos = Integer.parseInt(gakuseki._grade);
                            } else {
                                pos = Util.parseIntSafe(gakuseki._gdat._gradeCd, -1);
                            }
                        }
                        ipage += 1;
                        if (param._isOutputDebugGakuseki) {
                            log.info(" newpage. (drop), page = " + pageGakusekiListMap.size());
                        }
                    } else if (max == pos) {
                        ipage += 1;
                        if (param._isOutputDebugGakuseki) {
                            log.info(" newpage. (max), page = " + pageGakusekiListMap.size());
                        }
                        pos = 1;
                    } else {
                        pos++;
                    }
                }
            }

            if (param._isOutputDebugGakuseki) {
                for (final Map.Entry<Integer, PrintGakuseki> e : pageGakusekiListMap.entrySet()) {
                    final Integer n = e.getKey();
                    final PrintGakuseki printgakuseki = e.getValue();
                    log.info(" printgakuseki (page " + n + ") = " + Util.listString(new ArrayList(printgakuseki._yearGakusekiMap.entrySet()), 0));
                }
            }
            return pageGakusekiListMap;
        }

        private static List<Gakuseki> getPageGakusekiList(final DB2UDB db2, final PersonalInfo pInfo, final Param param) {
            List<Gakuseki> gakusekiList = Collections.emptyList();
            gakusekiList = new ArrayList<Gakuseki>(pInfo._gakusekiList);
            return gakusekiList;
        }
    }



    /**
     * 特例の授業等の記録
     */
    public static class KNJA129_4 extends KNJA129_0 implements Page {

        private KNJSvfFieldInfo _nameFieldInfo;

        KNJA129_4(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm4(final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            String form = "";
            _nameFieldInfo = null;

            _gradeLineMax = 6;
            form = "KNJA129.frm";
            return form;
        }

        public void setDetail(final DB2UDB db2, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            setDetail4(db2, pInfo, csvLines);
        }

        private void setDetail4(final DB2UDB db2, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            getForm4(pInfo, null); // _gradeLineMaxセット
            final Map<Integer, PrintGakuseki> pagePrintGakusekiMap = KNJA129_0.getPagePrintGakusekiMap(db2, pInfo, param(), _gradeLineMax);

            log.info(" pagePrintGakusekiMap = " + pagePrintGakusekiMap);

            for (final Integer page : pagePrintGakusekiMap.keySet()) {
                final PrintGakuseki printGakuseki = pagePrintGakusekiMap.get(page);
                printPage4(db2, pInfo, printGakuseki, csvLines);
            }
        }

        @Override
        public void printPage(final DB2UDB db2, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
            printPage4(db2, pInfo, pg, csvLines);
        }

        private void printPage4(final DB2UDB db2, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            String form = null;
            if (null != csvLines) {
                _gradeLineMax = 999;
            } else {
                form = getForm4(pInfo, printGakuseki);
                svfVrSetForm(form, 1);
            }
            if (null == _nameFieldInfo) {
                _nameFieldInfo = _form._formInfo.getFieldInfo("NAME1", "NAME2", "NAME3", charSize11, 24);
                if (param()._isOutputDebug) {
                    log.info(" ## setNameField " + _nameFieldInfo);
                }
            }

            final TreeMap<String, Integer> pageYearPosMap = new TreeMap<String, Integer>();
            for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                final Integer i = printGakuseki._yearGakusekiPositionMap.get(year);
                final int pos;
                pos = getGradeColumnNum(pInfo, i.intValue(), gakuseki, GakusekiColumn.SEQ, param(), _gradeLineMax);
                pageYearPosMap.put(year, pos);
            }
            if (null != csvLines) {
                return;
            }
            final String form4 = form; // modifyForm4(form, student, pInfo, printGakuseki, pageYearPosMap);
            svfVrSetForm(form4, 4);

            printPersonalInfo4(pInfo, printGakuseki);

            final String kindName = Arrays.asList("K", "P").contains(pInfo._student._schoolKind) ? "児童" : "生徒";

            String header = "";
            if (param()._isTokubetsushien) {
                if ("P".equals(pInfo._student._schoolKind)) {
                    header = "特別支援学校（小学部）児童指導要録";
                } else if ("J".equals(pInfo._student._schoolKind)) {
                    header = "特別支援学校（中学部）生徒指導要録";
                } else if (Arrays.asList("H", "A").contains(pInfo._student._schoolKind)) {
                    header = "特別支援学校（高等部）生徒指導要録";
                }
            } else {
                if ("P".equals(pInfo._student._schoolKind)) {
                    header = "小学校児童指導要録";
                } else if ("J".equals(pInfo._student._schoolKind)) {
                    header = "中学校生徒指導要録";
                } else if (Arrays.asList("H", "A").contains(pInfo._student._schoolKind)) {
                    header = "高等学校（全日制の課程・定時制の課程）生徒指導要録";
                }
            }
            svfVrsOut("HEADER", Util.defstr(header) + " 様式2（指導に関する記録）別記");

            int n = 1;
            for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                final Integer pos = pageYearPosMap.get(year);

                for (; n < pos; n++) {
                    svfVrsOut("FLG", "1");
                    svfVrsOut("KIND_NAME", kindName);
                    svfVrEndRecord();
                }

                final String title = getGakunenNendoTitle(pInfo, gakuseki);
                final HtrainRemarkOnline remark = pInfo._student.getHtrainremarkOnline(gakuseki._year);

                svfVrsOut("FLG", "1");
                svfVrsOut("GRADE2", title);
                svfVrsOut("KIND_NAME", kindName);
                printSvfRenban("ABSENCE_REASON", remark.absenceReason(), getShokenSize(param().property(Property.seitoSidoYorokuOnlineJiyuSize), 45, 3));
                svfVrsOut("DAYS", remark.days());
                svfVrsOut("PARTICIPATION_DAYS", remark.participationDays());
                printSvfRenban("METHOD", remark.method(), getShokenSize(param().property(Property.seitoSidoYorokuOnlineJisshiHouhouSize), 35, 3));
                printSvfRenban("OTHER_LEARNING", remark.otherLearning(), getShokenSize(param().property(Property.seitoSidoYorokuOnlineSonotaGakushuSize), 45, 3));

                svfVrEndRecord();
                n++;
            }

            for (; n <= param().getFormGradeCdMax(db2, pInfo._student._schoolKind); n++) {
                svfVrsOut("FLG", "1");
                svfVrsOut("KIND_NAME", kindName);
                svfVrEndRecord();
            }

            hasdata = true;
        }

//        private String modifyForm4(final String form, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final TreeMap<String, Integer> pageYearPosMap) {
//            final Map<String, String> keys = new TreeMap<String, String>();
//
//            if (param()._isOutputDebug) {
//                log.info(" modifyKey = " + keys);
//            }
//
//            return modifyForm0(_form._formInfo._formname, student, pInfo, printGakuseki, keys);
//        }
//
//        @Override
//        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {
//            boolean modified = false;
//            return modified;
//        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printPersonalInfo4(final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            printName(pInfo, _nameFieldInfo);
        }

        private String getGakunenNendoTitle(final PersonalInfo pInfo, final Gakuseki gakuseki) {
            if (null == gakuseki) {
                return null;
            }
            String title;
            if (gakuseki._isKoumokuGakunen) {
                title = gakuseki._gdat._gradeName2;
            } else {
                title = gakuseki._nendo;
            }
            return title;
        }
    }

    public static class Param extends KNJA130CCommon.Param {

        KNJA129_4 _knja130_4;
        final boolean _isTokubetsushien;

        final SchoolDiv _schoolDiv;

        Param(final HttpServletRequest request, final DB2UDB db2, final String year, final String semester, final Map<String, String> otherParamMap) {
            super(request, db2, "KNJA129", year, semester, otherParamMap);

            _isTokubetsushien = Arrays.asList("KNJA134P", "KNJA134J", "KNJA134H").contains(request.getParameter("PRGID")); // || "1".equals(property(Property.useSpecial_Support_School));

            _schoolDiv = new SchoolDiv(this, db2);

            setSemester(db2);
            setHdat(db2);
        }

        public int getFormGradeCdMax(final DB2UDB db2, final String schoolKind) {
            final String psKey = "GRADE_MAX_" + schoolKind;
            if (null == getPs(psKey)) {
                final String sql = " SELECT MAX(GRADE_CD) FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + schoolKind + "' ";

                if (_isOutputDebugQuery) {
                    log.info(" " + psKey + " sql = " + sql);
                }

                setPs(psKey, db2, sql);
            }
            return Util.toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, getPs(psKey), null)), -1);
        }

        public void close() {
            super.psCloseQuietly();
            closeForm();
        }

        private void closeForm() {
            for (final KNJA129_0 f : Arrays.asList(_knja130_4)) {
                if (null != f) {
                    f.close();
                }
            }
        }
    }
}

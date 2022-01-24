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

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfFieldAreaInfo.ModifyParam;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Repeat;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA130C extends KNJA130CCommon {

    private static final Log log = LogFactory.getLog(KNJA130C.class);

    public static boolean DEBUG = false;
    private static final String csv = "csv";

    private boolean _nonedata = false;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal(super.revisionCommon());
        log.fatal("$Id$"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        DB2UDB db2_2 = null;

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();

            final String dbname2 = request.getParameter("DBNAME2");
            if (!StringUtils.isBlank(dbname2)) {
                try {
                    db2_2 = new DB2UDB(dbname2 , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                    db2_2.open();
                } catch (Exception e) {
                    log.warn(" not set db2_2 : " + dbname2);
                    db2_2 = null;
                }
            }
            if (null == db2_2) {
                db2_2 = db2;
            }
        } catch (Exception ex) {
            log.error("exception:", ex);
            return;
        }

        final String cmd = request.getParameter("cmd");
        try {
            if (!csv.equals(cmd)) {
                response.setContentType("application/pdf");

                svf.VrInit();                             //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            }

            final Param[] paramArray = getParamArray(request, db2, db2_2);
            for (int i = 0; i < paramArray.length; i++) {
                final Param param = paramArray[i];

                if (csv.equals(cmd)) {
                    final List<List<String>> outputLines = new ArrayList<List<String>>();
                    final Map csvParam = new HashMap();
                    csvParam.put("HttpServletRequest", request);
                    printSvf(request, db2, null, param, outputLines);
                    final Map map = new HashMap();
                    map.put("TITLE", "生徒指導要録");
                    map.put("OUTPUT_LINES", outputLines);
                    CsvUtils.outputJson(log, request, response, CsvUtils.toJson(map), new HashMap());
                } else {

                    // 印刷処理
                    printSvf(request, db2, svf, param, null);
                }
            }

        } catch (Exception ex) {
            log.error("exception:", ex);
        } finally {
            if (!csv.equals(cmd)) {
                if (!_nonedata) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note" , "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.close();
            }
            if (null != db2_2 && db2 != db2_2) {
                db2_2.close();
            }
        }
    }

    private Param[] getParamArray(final HttpServletRequest request, final DB2UDB db2, final DB2UDB db2_2) {

        if ("KNJI050".equals(request.getParameter("PRGID"))) {
            final String[] empty = {};
            final String[] gYear = null == request.getParameter("G_YEAR") ? empty : StringUtils.split(request.getParameter("G_YEAR"), ",");
            final String[] gSemester = null == request.getParameter("G_SEMESTER") ? empty : StringUtils.split(request.getParameter("G_SEMESTER"), ",");
            final String[] schregno = null == request.getParameter("SCHREGNO") ? empty : StringUtils.split(request.getParameter("SCHREGNO"), ",");
            final int minlen = Math.min(gYear.length, Math.min(gSemester.length, schregno.length));
            final Param[] params = new Param[minlen];
            for (int i = 0; i < minlen; i++) {
                params[i] = new Param(request, db2, db2_2, "KNJI050", gYear[i], gSemester[i], Collections.singletonList(schregno[i]), new HashMap<String, String>());
            }
            return params;
        }
        return new Param[] {getParam(request, db2, db2_2)};
    }

    protected Param getParam(final HttpServletRequest request, final DB2UDB db2, final DB2UDB db2_2) {
        final String flg = "KNJA130C";
        final String year = request.getParameter("YEAR");
        final String gakki = request.getParameter("GAKKI");
        final Param param = new Param(request, db2, db2_2, flg, year, gakki, Param.getSchregnoList(db2, flg, request), new HashMap<String, String>());
        return param;
    }

    private void printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param, final List<List<String>> csvLines) {
        param.setPrintForm(svf, request, db2);

        final List<Student> studentList = Student.createStudents(db2, param);

        for (final Iterator<Student> stit = studentList.iterator(); stit.hasNext();) {
            final Student student = stit.next();
            student._schregEntGrdHistComebackDatList = Collections.emptyList();
            List<String> comebackDateList = Collections.emptyList();

            final String psKey = "PS_SCHREG_ENT_GRD_HIST_COMEBACK_DAT";
            if (param._hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT) {
                if (null == param.getPs(psKey)) {
                    final String sql =
                            " SELECT T1.* "
                                    + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                                    + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' "
                                    + " ORDER BY COMEBACK_DATE ";
                    // log.debug(" comeback sql = " + sql);

                    param.setPs(psKey, db2, sql);
                }
                comebackDateList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno}), "COMEBACK_DATE");

                student._schregEntGrdHistComebackDatList = new ArrayList<PersonalInfo>();
                if (param._isOutputDebug) {
                    log.info(" schregno = " + student._schregno + ",  comebackdate = " + comebackDateList);
                }
            }
            boolean useStartYear = comebackDateList.size() > 0;
            student.load(db2, param, useStartYear);
            for (final String comebackDate : comebackDateList) {
                final boolean useStartYear2 = student._schregEntGrdHistComebackDatList.size() > 0;
                final PersonalInfo comebackPersonalInfo = PersonalInfo.loadPersonal(db2, student, useStartYear2, comebackDate, param);
                student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
            }
            student._printEntGrdHistList = student.getPrintSchregEntGrdHistList(db2, param);
            log.info(" schregno = " + student._schregno + " (学年制 = " + param._schoolDiv.isGakunenSei(null, null, student) + ")");

            for (final KNJA130_0 form : param._printForm) {
                for (int egi = 0; egi < student._printEntGrdHistList.size(); egi++) {
                    final PersonalInfo pInfo = student._printEntGrdHistList.get(egi);
                    if (param._isOutputDebug) {
                        log.info(" entgrd idx = " + egi);
                    }
                    form.setDetail(db2, student, pInfo, csvLines);
                    if (form.nonedata) {
                        _nonedata = true;
                    }
                    form.closeSession();
                }
            }
            stit.remove();
        }
        param.closeForm();
        param.psCloseQuietly();
    }

    private static class Student extends KNJA130CCommon.Student {

//        final int _gradeRecNumber;

        String _printRegdCoursecd;
        String _printRegdMajorcd;
        String _printRegdCoursecode;
        String _handicap;

        PersonalInfo _pInfo;

        List<SchregRegdDat> _regdList = Collections.emptyList();

        List<String> _abroadYears;
        /** 異動履歴 */
        List<TransferRec> _transferRecList = Collections.emptyList();
        Map<String, Map<String, String>> _yearCertifSchoolMap = Collections.emptyMap();

        Map<String, AttendRec> _attendRecMap = Collections.emptyMap();

        Map<String, HtrainRemark> _htrainRemarkMap = Collections.emptyMap();

        Set<String> _notPrintEntGrdComebackYear = new HashSet<String>();

        private Map<String, HtrainRemarkDetail> _htrainRemarkDetailMap = Collections.emptyMap();
        private Map<Year, HtrainRemarkTrainref> _htrainRemarkTrainrefMap = Collections.emptyMap();
        private String _htrainRemarkDetail2Hdat002Remark1 = null;

        /** 教科コード90の代替科目備考を表示するときtrue */
        boolean _isShowStudyRecBikoSubstitution90;

        /** 総合学習活動(HTRAINREMARK_HDAT.TOTALSTUDYACT) */
        /** 総合学習評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL) */
        private Map<String, String> _htrainRemarkHdat = null;

        protected List<PersonalInfo> _schregEntGrdHistComebackDatList;

        protected List<PersonalInfo> _printEntGrdHistList;

        protected List<ClassView> _classViewList = Collections.emptyList();

        // KNJA133M
        /** 東京都奉仕・活動(HTRAINREMARK_HDAT.TOTALSTUDYACT2) */
        /** 東京都奉仕・評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL2) */
        /** 東京都修得単位の記録備考(HTRAINREMARK_HDAT.CREDITREMARK) */
        protected Map<String, String> _htrainRemarkHdat_2 = Collections.emptyMap();

        protected String _formerRegSchoolcd;
        protected String _formerRegSchoolFinschoolname;
        protected Map<String, String> _secondKokuseki; // 2nd 国籍
        private String _gappeimaeSchoolname;
        private Map<Year, Map<String, String>> _hexamEntremarkTrainrefDat; // 調査書6分割指導上参考となる諸事項
        private String _hexamEntremarkRemarkHdatRemark; // 調査書6分割備考

        private Student(final String schregno) {
            super(schregno);
        }
        private void load(final DB2UDB db2, final Param param, final boolean useStartYear) {

            _abroadYears = getAbroadYears(db2, param);
            if (null != param._knja130_1) {
                _transferRecList = TransferRec.loadTransferRec(db2, _schregno, param);
                _yearCertifSchoolMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, " SELECT YEAR, T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME FROM CERTIF_SCHOOL_DAT T6 WHERE T6.CERTIF_KINDCD = '" + _useCertifKindcd + "'"), "YEAR");
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

            if (param._z010.in(Z010.shimaneken)) {
                _handicap = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HANDICAP FROM SCHREG_BASE_MST WHERE SCHREGNO = ? ", new Object[] {_schregno }));
            }

            if (param._isPrintHosoku) {
                final String psKey = "PS_SECOND_KOKUSEKI";
                if (null == param.getPs(psKey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("   T1.BASE_REMARK1 AS NATIONALITY ");
                    stb.append(" , T1.BASE_REMARK2 AS SECOND_NAME ");
                    stb.append(" , T1.BASE_REMARK3 AS SECOND_NAME_KANA ");
                    stb.append(" , T1.BASE_REMARK4 AS SECOND_NAME_ENG ");
                    stb.append(" , T1.BASE_REMARK5 AS SECOND_REAL_NAME ");
                    stb.append(" , T1.BASE_REMARK6 AS SECOND_REAL_NAME_KANA ");
                    stb.append(" , NMA024.NAME1 AS NATIONALITY_NAME ");
                    stb.append(" FROM SCHREG_BASE_DETAIL_MST T1 ");
                    stb.append(" LEFT JOIN NAME_MST NMA024 ON NMA024.NAMECD1 = 'A024' ");
                    stb.append("     AND NMA024.NAMECD2 = T1.BASE_REMARK1 ");
                    stb.append(" WHERE ");
                    stb.append("     SCHREGNO = ? ");
                    stb.append("     AND BASE_SEQ = '011' ");

                    param.setPs(psKey, db2, stb.toString());
                }

                _secondKokuseki = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno}));
                if (param._isOutputDebug) {
                    log.info(" 第二国籍 = " + _secondKokuseki);
                }
            }

            if ("1".equals(param.property(Property.seitoSidoYorokuPrintGappeimaeSchoolname))) {
                final String psKey = "PS_GAPPEIMAE_SCHOOLNAME";
                if (null == param.getPs(psKey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("   T1.BASE_REMARK1 ");
                    stb.append(" FROM SCHREG_BASE_DETAIL_MST T1 ");
                    stb.append(" WHERE ");
                    stb.append("     SCHREGNO = ? ");
                    stb.append("     AND BASE_SEQ = '015' ");

                    param.setPs(psKey, db2, stb.toString());
                }
                _gappeimaeSchoolname = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_schregno}));
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            _pInfo = PersonalInfo.loadPersonal(db2, this, useStartYear, null, param);

            if (param._z010.in(Z010.teihachi) && null != param._knja130_2) {
                final String psKey = "PS_HTRAINREMARK_DETAIL2_HDAT_002 ";
                if (null == param.getPs(psKey)) {
                    final String sql = " SELECT REMARK1 FROM HTRAINREMARK_DETAIL2_HDAT WHERE SCHREGNO = ? AND HTRAIN_SEQ = '002' ";

                    param.setPs(psKey, db2, sql);
                }
                _htrainRemarkDetail2Hdat002Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno }));
            }

            if (null != param._knja130_4 || null != param._knja130_4t) {
                _attendRecMap = AttendRec.loadAttendRec(db2, this, _pInfo, param);
                _htrainRemarkMap = HtrainRemark.loadHtrainRemark(db2, _schregno, param);
                if (param._isOutputDebug) {
                    log.info(" htrainRemarkMap keySet = " + _htrainRemarkMap.keySet());
                }
                if (_pInfo._isSogoShoken6Bunkatsu || _pInfo._isSogoShoken6BunkatsuTo3Bunkatsu) {
                    _htrainRemarkTrainrefMap =  HtrainRemarkTrainref.loadHtrainRemarkTrainref(db2, _schregno, param);
                } else if (_pInfo._isSogoShoken3Bunkatsu || param._z010.in(Z010.tokiwa) || param._z010.in(Z010.seijyo)) {
                    _htrainRemarkDetailMap =  HtrainRemarkDetail.loadHtrainRemarkDetail(db2, _schregno, param);
                }
            }

            if (param._is133m) {
                if (param._z010.in(Z010.tokyoto)) {

                    final String psKey = "PS_HTRAINREMARK_HDAT_2";
                    if (null == param.getPs(psKey)) {
                        final String sql = "select TOTALSTUDYACT2, TOTALSTUDYVAL2, CREDITREMARK from HTRAINREMARK_HDAT where SCHREGNO = ? ";

                        param.setPs(psKey, db2, sql);
                    }
                    _htrainRemarkHdat_2 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] {_schregno}));
                }

            } else {
                if ("2".equals(param.property(Property.seitoSidoYorokuHoushiNentani))) {

                    final String psKey = "PS_HTRAINREMARK_HDAT_2";
                    if (null == param.getPs(psKey)) {
                        final String sql = "select TOTALSTUDYACT2, TOTALSTUDYVAL2, CREDITREMARK from HTRAINREMARK_HDAT where SCHREGNO = ? ";

                        param.setPs(psKey, db2, sql);
                    }
                    _htrainRemarkHdat_2 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] {_schregno}));
                }

                if (!param._isNendogoto) {

                    final String psKey = "PS_HTRAINREMARK_HDAT";
                    if (null == param.getPs(psKey)) {
                        final String sql = "select TOTALSTUDYACT, TOTALSTUDYVAL from HTRAINREMARK_HDAT where SCHREGNO = ? ";

                        param.setPs(psKey, db2, sql);
                    }
                    _htrainRemarkHdat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] {_schregno}));
                }
                if (null != _pInfo._grdDate) {
                    // 2009年3月31日までに卒業した生徒は、「総合的な学習の時間の記録」に
                    // 教科コード90の代替科目備考を表示しない。
                    boolean isGraduatedBy2008 = _pInfo._grdDate != null && java.sql.Date.valueOf(_pInfo._grdDate).compareTo(java.sql.Date.valueOf("2009-03-31")) <= 0;
                    _isShowStudyRecBikoSubstitution90 = !isGraduatedBy2008;
                } else {
                    _isShowStudyRecBikoSubstitution90 = true;
                }

                if (param._z010.in(Z010.jyoto)) {
                    _hexamEntremarkRemarkHdatRemark = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT REMARK FROM HEXAM_ENTREMARK_REMARK_HDAT WHERE SCHREGNO = ? ", new Object[] {_schregno}));
                }
            }

            if (Integer.parseInt(param._year) >= YOSHIKI2_OMOTE_VER.get3KantenFormStartYear(param)) {
                _classViewList = ClassView.loadClassViewList(db2, param, _pInfo);
            }

        }

        public Map<SubclassMst, TreeMap<Year, String>> getSubclassYearViewMap(final List<String> pageYears) {
            Map<SubclassMst, TreeMap<Year, String>> rtn = new HashMap<SubclassMst, TreeMap<Year, String>>();
            for (final ClassView cv : getClassViewList(pageYears)) {
                for (final ViewSubclass vs : cv._viewSubclassList) {

                    final Map<String, List<String>> yearViewsMap = new TreeMap<String, List<String>>();

                    for (final View v : vs._viewList) {
                        for (final Map.Entry<String, ViewStatus> e : v._yearViewMap.entrySet()) {
                            final String year = e.getKey();
                            final ViewStatus vStatus = e.getValue();
                            getMappedList(yearViewsMap, year).add(defstr(vStatus._status, " "));
                        }
                    }

                    for (final Map.Entry<String, List<String>> e : yearViewsMap.entrySet()) {
                        final StringBuilder views = new StringBuilder();
                        for (final String status : e.getValue()) {
                            views.append(status);
                        }
                        Util.getMappedMap(rtn, vs._subclassMst).put(Year.of(e.getKey()), views.toString());
                    }
                }
            }
            return rtn;
        }

        private List<ClassView> getClassViewList(final List<String> pageYears) {
            final List<ClassView> rtn = new ArrayList<ClassView>();
            for (final ClassView cv : _classViewList) {
                final ClassView enabled = ClassView.enabled(cv, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            return rtn;
        }

        private List<String> getAbroadYears(final DB2UDB db2, final Param param) {
            final String psKey = "CHECK_ABROAD YEARS";
            if (null == param.getPs(psKey)) {
                final String sql = " SELECT TRANSFER_SDATE, TRANSFER_EDATE, INT(FISCALYEAR(TRANSFER_SDATE)) AS YEAR FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = ? AND TRANSFERCD = '1' ORDER BY TRANSFER_SDATE ";
                if (param._isOutputDebugQuery) {
                    log.info(" abroad years sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
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

        public static List<Student> createStudents(final DB2UDB db2, final Param param) {
            final String psKey = "PRINT_REGD";
            if (null == param.getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.COURSECD ");
                stb.append(" , T1.MAJORCD ");
                stb.append(" , T1.COURSECODE ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     SCHREGNO = ? ");
                stb.append("     AND YEAR = '" + param._year + "' ");
                stb.append("     AND SEMESTER = '" + param._gakki + "' ");

                param.setPs(psKey, db2, stb.toString());
            }

            final Map<String, String> courseCertifKindcdMap = new HashMap<String, String>();
            final List<Student> list = new ArrayList<Student>();
            for (final String schregno : param._schregnoList) {
                final Student student = new Student(schregno);
                list.add(student);

                final Map regd = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {schregno}));
                student._printRegdCoursecd = KnjDbUtils.getString(regd, "COURSECD");
                student._printRegdMajorcd = KnjDbUtils.getString(regd, "MAJORCD");
                student._printRegdCoursecode = KnjDbUtils.getString(regd, "COURSECODE");
                student._useCertifKindcd = CERTIF_KINDCD;

                final String course = student._printRegdCoursecd + "-" + student._printRegdMajorcd + "-" + student._printRegdCoursecode;
                if (null != courseCertifKindcdMap.get(course)) {
                    student._useCertifKindcd = courseCertifKindcdMap.get(course);
                } else {
                    for (final Map a045 : param._a045CourseCertifKindChangeList) {
                        final String certifKindcd = KnjDbUtils.getString(a045, "NAMESPARE1");
                        if (null == certifKindcd) {
                            continue;
                        }
                        final String coursecd = KnjDbUtils.getString(a045, "NAME1");
                        final String majorcd = KnjDbUtils.getString(a045, "NAME2");
                        final String coursecode = KnjDbUtils.getString(a045, "NAME3");
                        if (null != coursecd && coursecd.equals(student._printRegdCoursecd) && null != majorcd && majorcd.equals(student._printRegdMajorcd)) {
                            // 学科が一致
                            if (null != coursecode) {
                                if (!coursecode.equals(student._printRegdCoursecode)) {
                                    continue;
                                }
                                // コースが一致
                            }
                            // 複数一致は上書き
                            student._useCertifKindcd = defstr(certifKindcd, CERTIF_KINDCD);
                            if (param._isOutputDebugData) {
                                log.info(" course " + course + " useCertifKindcd = " + student._useCertifKindcd);
                            }
                            courseCertifKindcdMap.put(course, student._useCertifKindcd);
                        }
                    }
                    if (null == courseCertifKindcdMap.get(course) && !param._a045CourseCertifKindChangeList.isEmpty()) {
                        log.info("not found certifKindcdMap " + course);
                    }
                }
            }

            for (final String certifKindcd : courseCertifKindcdMap.values()) {
                if (!param._certifSchoolDatMap.containsKey(certifKindcd)) {
                    param._certifSchoolDatMap.put(certifKindcd, KnjDbUtils.firstRow(KnjDbUtils.query(db2,  "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ")));
                }
            }
            if (param._isOutputDebugData) {
                log.info(" courseCertifKindcd = " + courseCertifKindcdMap.values() + ", " + Util.debugMapToStr("certifSchoolDatMap = ", param._certifSchoolDatMap, "\n , "));
            }
            return list;
        }

        /**
         * 印刷する生徒情報
         */
        protected List<PersonalInfo> getPrintSchregEntGrdHistList(final DB2UDB db2, final Param param) {
            final List<PersonalInfo> rtn = new ArrayList();
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List<PersonalInfo> pInfoList = new ArrayList();
            pInfoList.addAll(_schregEntGrdHistComebackDatList);
            pInfoList.add(_pInfo);
            for (final PersonalInfo pInfo : pInfoList) {
                final int begin = pInfo.getYearBegin();
                final int end = pInfo.getYearEnd(param);
                if (param._isOutputDebug && pInfoList.size() > 1) {
                    log.info(" add PersonalInfo " + begin + ", " + end);
                }
                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
                    final boolean useStartYear = rtn.size() > 0;
                    rtn.addAll(pInfo.fuyasu(db2, this, param, useStartYear)); // 増やす!
                } else {
                    for (int y = begin; y <= end; y++) {
                        _notPrintEntGrdComebackYear.add(String.valueOf(y));
                    }
                }
            }
            if (rtn.isEmpty()) {
                if (pInfoList.size() == 0) {
                    log.fatal("対象データがない!");
                } else {
                    log.fatal("最後のデータを表示:" + rtn);
                    rtn.add(pInfoList.get(pInfoList.size() - 1));
                    rtn.get(0)._isFirst = true;
                }
            } else {
                rtn.get(0)._isFirst = true;
                int lastBeginYear = -1;
                for (final PersonalInfo pInfo : Util.reverse(pInfoList)) {
                    if (lastBeginYear != -1) {
                        pInfo._seisekiEndYear = lastBeginYear - 1;
                    }
                    lastBeginYear = pInfo.getYearBegin();
                }
            }
            if (param._isOutputDebug || rtn.size() > 1) {
                log.warn(" schregno = " + _schregno + ", printEntgrdhistList = " + rtn.size() + ", notPrintEntGrdComebackYear = " + _notPrintEntGrdComebackYear);
            }
            for (final PersonalInfo pInfo : rtn) {
                final int begin = pInfo.getYearBegin();
                final int end = pInfo.getYearEnd(param);
                for (int y = begin; y <= end; y++) {
                    final String sy = String.valueOf(y);
                    if (_notPrintEntGrdComebackYear.contains(sy)) {
                        log.info(" set print year : " + sy);
                        _notPrintEntGrdComebackYear.remove(sy);
                    }
                }
            }
            return rtn;
        }

        public static String getTengakuTaigakuNendo(final PersonalInfo pInfo) {
            final String tengakubi = !pInfo.isTengaku() ? null : pInfo._grdDate;
            final String taigakubi = !pInfo.isTaigaku() ? null : pInfo._grdDate;
            final String grdYear = nendo(null != tengakubi ? tengakubi : taigakubi);
            return grdYear;
        }

        public static String getTengakuTaigakuNendoMinus1(final PersonalInfo pInfo) {
            return String.valueOf(Integer.parseInt(getTengakuTaigakuNendo(pInfo)) - 1);
        }

        public HtrainRemark getHtrainremark(final String year) {
            return null == _htrainRemarkMap.get(year) ? HtrainRemark.Null : _htrainRemarkMap.get(year);
        }

//        public static Collection getDropShowYears(final Param param, final PersonalInfo pInfo) {
//            final Collection chkDropShowYears = new HashSet();
//            if (param._isKyoto && (pInfo.isTaigaku() || pInfo.isTengaku()) && pInfo._dropYears.isEmpty()) {
//                final String grdYear = getTengakuTaigakuNendoMinus1(pInfo);
//                if (null != grdYear) {
//                    // 「原級留置した場合、留年時の成績は出力されない」が、
//                    // 再履修の成績データを作成する前に転退学した場合は、留年時の成績を出す。
//                    // （原級留置の年次で改ページはする）
//                    for (final Iterator it = groupByGrade(pInfo._gakusekiList).values().iterator(); it.hasNext();) {
//                        final List gakuList = (List) it.next();
//                        if (gakuList.size() <= 1) { // 留年ではない
//                            continue;
//                        }
//                        final Gakuseki newGaku = (Gakuseki) gakuList.get(0); // 再履修の学籍
//                        if (grdYear.equals(newGaku._year)) {
//                            final Gakuseki oldGaku = (Gakuseki) gakuList.get(1); // 留年時の最新の学籍
//
//                            boolean hasNewGakuYearStudyrec = false;
//                            for (final Iterator its = pInfo._studyRecList.iterator(); its.hasNext();) {
//                                final StudyRec studyRec = (StudyRec) its.next();
//                                if (null != studyRec._year && studyRec._year.equals(newGaku._year)) {
//                                    hasNewGakuYearStudyrec = true;
//                                }
//                            }
//                            if (!hasNewGakuYearStudyrec) {
//                                // 再履修の年度の成績がない場合、留年時の年度を表示非対象年度リストから除く
//                                chkDropShowYears.add(oldGaku._year);
//                                log.fatal("再履修年度の成績データが0件.留年時の成績を表示対象とする. year = " + oldGaku._year);
//                            }
//                        }
//                    }
//                }
//            }
//            return chkDropShowYears;
//        }

        private static Map<String, List<Gakuseki>> groupByGrade(final List<Gakuseki> gakusekiList) {
            final Map<String, List<Gakuseki>> gradeMap = new TreeMap<String, List<Gakuseki>>();
            for (final ListIterator<Gakuseki> i = gakusekiList.listIterator(gakusekiList.size()); i.hasPrevious();) {
                final Gakuseki gaku = i.previous();
                if (null == gaku._grade) {
                    continue;
                }
                getMappedList(gradeMap, gaku._grade).add(gaku);
            }
            return gradeMap;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiAttendRec(final DB2UDB db2, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final Map<String, AttendRec> attendRecMap, final Param param) {
            final Map<String, Gakuseki> yearGakusekiMap = new TreeMap<String, Gakuseki>();
            final Set<String> notPrintYearSet = new TreeSet<String>();

            for (final Gakuseki gakuseki : gakusekiList) {
                if ("1".equals(param._notPrintKyugakuNendoInYoshiki2) && pInfo._kyugakuNendoList.contains(gakuseki._year)) {
                    continue;
                }
                yearGakusekiMap.put(gakuseki._year, gakuseki);
            }
            for (final String year : attendRecMap.keySet()) {
                if ("1".equals(param._notPrintKyugakuNendoInYoshiki2) && pInfo._kyugakuNendoList.contains(year)) {
                    continue;
                }
                if (yearGakusekiMap.containsKey(year)) {
                    continue;
                }
                if (pInfo._student._notPrintEntGrdComebackYear.contains(year)) {
                    notPrintYearSet.add(year);
                    continue;
                }
                if (param._is133m) {
//                    final KNJA133M.AttendRec attendrec = (KNJA133M.AttendRec) attendRecMap.get(year);
//                    map.put(year, new KNJA133M.Gakuseki(attendrec._year, attendrec._));
                } else {
                    final AttendRec attendrec = attendRecMap.get(year);
                    yearGakusekiMap.put(year, new Gakuseki(db2, pInfo._student, attendrec._year, attendrec._annual, param));
                }
            }
            if (param._isOutputDebug) {
                if (notPrintYearSet.size() > 0) {
                    log.info(" COMEBACK_DATの年度は印字しない: " + notPrintYearSet);
                }
            }

            final List<Gakuseki> list = new LinkedList<Gakuseki>(yearGakusekiMap.values());
            return list;
        }

        /**
         * @return Gakusekiリスト（様式２表対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiStudyRec(final DB2UDB db2, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final List<StudyRec> studyRecList, final Param param) {
            final Map<String, Gakuseki> map = new TreeMap<String, Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiList) {
                if ("1".equals(param._notPrintKyugakuNendoInYoshiki2) && pInfo._kyugakuNendoList.contains(gakuseki._year)) {
                    continue;
                }
                map.put(gakuseki._year, gakuseki);
            }
            final int gakusekiMinYear = Gakuseki.gakusekiMinYear(gakusekiList);
            for (final StudyRec studyrec : studyRecList) {
                if ("1".equals(param._notPrintKyugakuNendoInYoshiki2) && pInfo._kyugakuNendoList.contains(studyrec._year)) {
                    continue;
                }
                if (map.containsKey(studyrec._year)) {
                    continue;
                }
                final String year = NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) == 0 ? ANOTHER_YEAR : studyrec._year;

                if (param._is133m) {
                    if (param._z010.in(Z010.kyoto)) {
                        if (gakusekiMinYear > Integer.parseInt(studyrec._year)) {
                            continue;
                        }
                    }
                    if (StudyRec.isNotPrint(param, studyrec, YOSHIKI._2_OMOTE)) {
                        continue;
                    }
                    final Gakuseki gakuseki = new Gakuseki(db2, pInfo._student, year, studyrec._annual, param);
                    gakuseki._isStudyrecGakusekiM = true;
                    map.put(year, gakuseki);
                } else {
                    final boolean isPrintAnotherStudyrec3 = param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_1
                                                         || param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_2 && param._schoolDiv.isTanniSei(null, pInfo, pInfo._student);
                    if (isPrintAnotherStudyrec3 && NumberUtils.isDigits(studyrec._year)) {
                        // 前籍校の成績を表示する
                        final Gakuseki gakuseki = new Gakuseki(db2, pInfo._student, year, studyrec._annual, param);
                        map.put(year, gakuseki);
                    }
                }
            }

            final List<Gakuseki> list = new LinkedList<Gakuseki>(map.values());
            Collections.sort(list, new Gakuseki.GakusekiComparator(pInfo._student, pInfo, param));
            if (param._isOutputDebug) {
                log.info(" gakuseki studyrec = " + Util.listString(list, 0));
            }
            return list;
        }

        /**
         * 異動データから留学した年度を得る。
         * @param db2
         * @return 留学した年度のリスト
         */
        private static List<String> getAbroadPrintDropRegdYears(final DB2UDB db2, final Param param, final String schregno, final List<Gakuseki> gakusekiList) {

            if (!param._hasSCHREG_TRANSFER_DAT_ABROAD_PRINT_DROP_REGD) {
                return Collections.emptyList();
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEARS(YEAR) AS ( ");

            String union = "";
            for (final Gakuseki gakuseki : gakusekiList) {
                stb.append(union).append(" VALUES('" + gakuseki._year + "') ");
                union = " UNION ";
            }
            stb.append(" ) SELECT ");
            stb.append("     T3.YEAR ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1, ");
            stb.append("     SCHREG_TRANSFER_DAT T2, ");
            stb.append("     YEARS T3 ");
            stb.append(" WHERE ");
            stb.append("     T1.TRANSFERCD = '1' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T2.TRANSFERCD = T1.TRANSFERCD ");
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T3.YEAR BETWEEN FISCALYEAR(T1.TRANSFER_SDATE) AND FISCALYEAR(T1.TRANSFER_EDATE)  ");
            stb.append("     AND YEAR(T1.TRANSFER_EDATE) = YEAR(T2.TRANSFER_SDATE) ");
            stb.append("     AND MONTH(T1.TRANSFER_EDATE) = 3 ");
            stb.append("     AND DAY(T1.TRANSFER_EDATE) = 31 ");
            stb.append("     AND MONTH(T2.TRANSFER_SDATE) = 4 ");
            stb.append("     AND DAY(T2.TRANSFER_SDATE) = 1 ");
            stb.append("     AND T2.ABROAD_PRINT_DROP_REGD = '1' ");

            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "YEAR");
        }

        /**
         * 単位数欄に"0"を表示するか
         * @param year 京都府の判定用パラメータ
         * @return 表示するならtrue、そうでなければfalse
         */
        public boolean isShowCredit0(final Param param, final PersonalInfo pInfo, final String year) {
            if (pInfo.getDropYears(param).contains(year)) {
                if (param._isOutputDebug) {
                    log.info("留年した年度は0表示しない");
                }
                return false;
            }
            if (param._z010.in(Z010.naraken)) {
                if (param._isOutputDebug) {
                    log.info("奈良県は0表示しない");
                }
                return false;
            }
            boolean isGrd = false;
            if (param._z010.in(Z010.kyoto)) {
                isGrd = pInfo.isSotsugyo();
            } else {
                isGrd = pInfo.isSotsugyo() || pInfo.isTaigaku() || pInfo.isTengaku();
            }
            if (isGrd) {
                if (param._isOutputDebug) {
                    param.logOnce(" " + pInfo._schregno + " 0表示する : 卒業? " + pInfo.isSotsugyo() + ", 退学? " + pInfo.isTaigaku() + ", 転学? " + pInfo.isTengaku());
                }
                return true;
            }
            if (param._z010.in(Z010.kyoto)) {
                final String tengakubi = !pInfo.isTengaku() ? null : pInfo._grdDate;
                final String taigakubi = !pInfo.isTaigaku() ? null : pInfo._grdDate;
                // 京都府は他校に転学した場合転学した年度は単位数"0"を表示しない (年度判定用パラメータyearがnullの場合は表示する)
                final String date = null != tengakubi ? tengakubi : taigakubi;
                final boolean result = null == year || null != date && !dateIsInNendo(date, year);
//                log.debug(" 京都府 単位数0を表示するか:  result = " + result + ", year = " + year + ", tentaigakubi = " + date);
                return result;
            }
            return false;
        }

        // 島根県はその他欄002は自立活動を固定で表示する
        public boolean isShimanekenPrintJiritsuKatudou(final Param param) {
            return param._z010.in(Z010.shimaneken) && "002".equals(_handicap);
        }

//        /**
//         * 評価の欄に斜線を表示するフォームを使用するか
//         * @return 使用するならtrue、そうでなければfalse
//         */
//        public boolean useShasenForm(final Param param) {
//            // 総合的な学習の時間の代替科目備考がある場合、評価の欄に斜線を表示するフォームを使用する。
//            final List list = (List) _gakushuBiko.getStudyRecBikoSubstitution90(StudyrecSubstitutionContainer.ZENBU, _gakusekiList, _keyAll, param).get(_keyAll);
//            return list != null && !list.isEmpty();
//        }

        public String toString() {
            return "Student(" + _schregno + ")";
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
        boolean _droppedRecordInTengakuTaigakuYearEnabled;
        String _staffSeq;
        String _principalSeq;
        String _kaizanFlg;
        boolean _isKoumokuGakunen;
        boolean _isDroppedAbroad; // 最終学年に留学で留年したか（京都府のみ）

        String _gakunenDaitukiM;
        String _nendofM;
        String[] _arNendoM;
        boolean _isStudyrecGakusekiM;

        private Gakuseki(final DB2UDB db2, final Student student, final Map row, final Map attestMap, final PersonalInfo pInfo, final Param param) {
            super(KnjDbUtils.getString(row, "YEAR"));
            final String nendoDate;
            if (param._useGengoInApr01) {
                nendoDate = _year + "-04-01"; // 年度開始日
            } else {
                nendoDate = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31"; // 年度終了日
            }
            final String nendo;
            if (param._isSeireki) {
                nendo = String.valueOf(_year) + "年度";
            } else {
                nendo = KNJA130_0.dateNendoWareki(db2, nendoDate);
            }
            _nendo = nendo;
            _grade = KnjDbUtils.getString(row, "GRADE");
            _gdat = SchregRegdGdat.getSchregRegdGdat(_year, _grade, param._gdatMap);
            _hr_class = KnjDbUtils.getString(row, "HR_CLASS");
            _hdat = SchregRegdHdat.getSchregRegdHdat(_year, KnjDbUtils.getString(row, "SEMESTER"), _grade, _hr_class, param._hrdatMap);

            final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
            _attendno = !NumberUtils.isDigits(attendno) ? attendno : String.valueOf(Integer.parseInt(attendno)) + (param._z010.in(Z010.tokiwa) ? "番" : "");
            _annual = toDigit(KnjDbUtils.getString(row, "ANNUAL"), "");
            _dataflg = GAKUSEKI_DATA_FLG1;

            //
            _staffSeq = KnjDbUtils.getString(attestMap, "CHAGE_OPI_SEQ");
            _principalSeq = KnjDbUtils.getString(attestMap, "LAST_OPI_SEQ");
            _kaizanFlg = KnjDbUtils.getString(attestMap, "FLG");

            if (param._is133m) {

                _nendofM = KNJ_EditDate.setNendoFormat(db2, KNJA130_0.dateNendoWareki(db2, nendoDate), param._formatDateDefaultYear);
                _arNendoM = arNendo(param, _nendo);

                _gakunenDaitukiM = _gdat._gradeName2;
            }
            _isKoumokuGakunen = param._schoolDiv.isKoumokuGakunen(param, _year, pInfo, student).booleanValue();
        }

        /**
         * コンストラクタ。
         */
        private Gakuseki(final DB2UDB db2, final Student student, final String year, final String annual, final Param param) {
            super(year);
            if (isNyugakumae()) {
                _nendo = NYUGAKUMAE;
            } else {
                if (param._isSeireki) {
                    _nendo = String.valueOf(Integer.parseInt(year)) + "年度";
                } else {
                    final String nendoDate;
                    if (param._useGengoInApr01) {
                        nendoDate = _year + "-04-01"; // 年度開始日
                    } else {
                        nendoDate = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31"; // 年度終了日
                    }
                    _nendo = KNJA130_0.dateNendoWareki(db2, nendoDate);
                }
            }
            _grade = annual;
            if (null != _grade) {
                if (0 >= Integer.parseInt(_grade)) {
                    _annual = null;
                    _gdat = SchregRegdGdat.create();
                    _gdat._gradeName2 = NYUGAKUMAE;
                    _dataflg = GAKUSEKI_DATA_FLG2_ZAISEKIMAE;
                } else {
                    // _yearは0以外のはず
                    _annual = annual;
                    final SchregRegdGdat gradeGdat = SchregRegdGdat.getSchregRegdGdat(_year, _grade, param._gdatMap);
                    final SchregRegdGdat annualGdat = SchregRegdGdat.getSchregRegdGdat(_year, _annual, param._gdatMap);

                    _gdat = SchregRegdGdat.create();
                    _gdat._gradeCd = defstr(gradeGdat._gradeCd, _grade);
                    _gdat._gakunenSimple = toDigit(param._replacePrintGradeCdWithGrade ? gradeGdat._grade : gradeGdat._gradeCd, " ");
                    _gdat._gradeName2 = defstr(annualGdat._gradeName2, "第" + Integer.parseInt(_annual) + "学年");
                    _dataflg = GAKUSEKI_DATA_FLG3_ZAISEKIMAE_NENDOARI;
                }
            } else {
                _annual = null;
                _gdat = SchregRegdGdat.create();
                _dataflg = GAKUSEKI_DATA_FLG2_ZAISEKIMAE;
            }
            _hr_class = null;
            _attendno = null;

            _staffSeq = null;
            _principalSeq = null;
            _kaizanFlg = null;
            if (param._is133m) {
                if (isAnotherSchoolYear(year)) {
                    _arNendoM = new String[] {"", NYUGAKUMAE, ""};
                } else {
                    _arNendoM = arNendo(param, _nendo);
                }
                if (null != _grade) {
                    if (0 == Integer.parseInt(_grade)) {
                        _gakunenDaitukiM = NYUGAKUMAE;
                    } else {
                        _gdat._gakunenSimple = String.valueOf(Integer.parseInt(_annual));
                        _gakunenDaitukiM = "第" + Integer.parseInt(_annual) + "学年";
                    }
                } else {
                    _gakunenDaitukiM = null;
                }
                _nendofM = null;
            }
            _isKoumokuGakunen = param._schoolDiv.isKoumokuGakunen(param, _year, null, student).booleanValue();
        }

        private static boolean nyugakuMaeHaMigi(final Student student, final PersonalInfo pInfo, final Param param) {
            return param._schoolDiv.isGakunenSei(null, pInfo, student);
        }

        private String getGradeOrNendo(final Param param) {
            final String head = _isKoumokuGakunen ? _gdat._gradeName2 : getNendo2(param);
            return defstr(head);
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

        /**
         * 最後の在籍データの前年が留年しておりかつ留年の年度の3/31に留学していれば、最後の在籍データを得る（京都府のみ）
         * @param gakusekiList 在籍データのリスト
         * @param transferRecList 留学・休学データのリスト
         * @param param
         * @return 上記の在籍データ。条件が合致しなければnull
         */
        public static void setLastGakusekiDroppedAbroad(final List<Gakuseki> gakusekiList, final List<TransferRec> transferRecList, final Param param) {
            if (param._z010.in(Z010.kyoto)) {
                if (2 <= gakusekiList.size()) {
                    final Set<String> abroad0331YearSet = new HashSet<String>();
                    final Set<String> abroad0401YearSet = new HashSet<String>();
                    for (final TransferRec t : transferRecList) {
                        if (TransferRec.A004_NAMECD2_RYUGAKU.equals(t._transfercd)) {
                            abroad0331YearSet.addAll(TransferRec.get0331YearSet(t, param));
                            abroad0401YearSet.addAll(TransferRec.get0401YearSet(t, param));
                        }
                    }
                    // 最後の学籍
                    final Gakuseki lastGakuseki = gakusekiList.get(gakusekiList.size() - 1);
                    // 最後から2番目の学籍
                    final Gakuseki beforeLastGakuseki = gakusekiList.get(gakusekiList.size() - 2);
                    // 学籍の年度の差が1年か
                    final boolean isNext = Util.parseIntSafe(lastGakuseki._year, -100) - Util.parseIntSafe(beforeLastGakuseki._year, -200) == 1;

                    log.info(" lastGakuseki (" + lastGakuseki + ") isDropeedAbroad = " + isNext + " && " + abroad0331YearSet.contains(beforeLastGakuseki._year));

                    lastGakuseki._isDroppedAbroad = isNext && abroad0331YearSet.contains(beforeLastGakuseki._year) && abroad0401YearSet.contains(lastGakuseki._year);
                    if (lastGakuseki._isDroppedAbroad) {
                        if (Util.parseIntSafe(lastGakuseki._gdat._gradeCd, 100) > Util.parseIntSafe(param._a023Name3GraduatableGrade, 200)) {
                            lastGakuseki._gdat = SchregRegdGdat.getSchregRegdGdat(lastGakuseki._year, param._a023Name3GraduatableGrade, param._gdatMap);
                        }
                    }
                }
            }
        }

        /**
         * 除籍区分が退学or転学の生徒で除籍日付の年度の単位数が0の時、原級留置の単位数は有効（印字）とする
         */
        public static void setDroppedRecordInTengakuTaigakuYearEnabled(final PersonalInfo pi, final List<Gakuseki> gakusekiList, final Param param) {
            if (!"1".equals(param.property(Property.seitoSidoYorokuPrintDropRecInTenTaiYear))) {
                return;
            }
            if ((pi.isTengaku() || pi.isTaigaku()) && NumberUtils.isDigits(pi._grdYear)) {
                boolean hasTengakuTaigakuYearStudyrec = false;
                for (final StudyRec studyRec : pi._studyRecList) {
                    if (null != studyRec._year && studyRec._year.equals(pi._grdYear)) {
                        hasTengakuTaigakuYearStudyrec = true;
                    }
                }
                if (!hasTengakuTaigakuYearStudyrec) {
                    // 転学退学の年度の成績がない場合、留年時の成績を様式1裏に印字する
                    for (final ListIterator<Gakuseki> git = gakusekiList.listIterator(gakusekiList.size()); git.hasPrevious();) {
                        final Gakuseki gakuseki = git.previous();
                        if (gakuseki._isDrop) {
                            gakuseki._droppedRecordInTengakuTaigakuYearEnabled = true;
                            break;
                        }
                    }
                }
            }
        }

        public static boolean containsDroppedAbroad(final Collection<Gakuseki> gakusekiList) {
            for (final Gakuseki gakuseki : gakusekiList) {
                if (gakuseki._isDroppedAbroad) {
                    return true;
                }
            }
            return false;
        }

        // KNJA133M
        public static TreeSet<String> notPrintYearSet(final List<Gakuseki> gakusekiList, final Param param) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : gakusekiList) {
                if (g.isNotPrint(param)) {
                    set.add(g._year);
                }
            }
            return set;
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
        final Map _guardianDatRow;
        final Map _entGrdRow;
        String _studentRealName;
        String _studentName;
        String _studentNameNextDateOfHistLastDate;
        boolean _isPrintRealName;
        boolean _isPrintNameAndRealName;
        /** 最も古い履歴の生徒名 */
        String _studentNameHistFirst;
//        final String _annual;
        final String _checkCourseName;
        String _courseName;
        String _majorName;
        String _coursecode;
        String _coursecodeName;
        HistVal _studentKana;
        HistVal _guardianOrGuarantorKana;
        HistVal _guardianOrGuarantorName;
        String _guardianOrGuarantorNameNextDateOfHistLastDate;
        final String _birthday;
        final String _birthdayStr;
        final String _sex;
        final String _finishDate;
        final String _installationDiv;
        final String _tengakuSakiGrade;
        final String _comebackDate;
        private String _addressGrdHeader;
        private boolean _isFuhakkou;

        /** 保護者のかわりに保証人を表示するか */
        final boolean _isPrintGuarantor;
        /** 住所履歴 */
        private List<Address> _addressList = Collections.emptyList();
        /** 保護者住所履歴 */
        private List<Address> _guardianAddressList = Collections.emptyList();
        private String _zaigakusubekiKikan;
        private Map<String, String> _lastAnotherSchoolHistDat = Collections.emptyMap();
        /** 学習記録データ */
        private List<StudyRec> _studyRecList = Collections.emptyList();
        /** 学習記録データ(査定) */
        List<StudyRec> _studyRecReplaceList = Collections.emptyList();
        /** 学年成績 */
        private GakunenSeiseki _gakunenSeiseki;
        /** 青山学年成績 */
        private AoyamaGakunenSeiseki _aoyamaGakunenSeiseki;

        /** 代替科目の備考マップのキー(年度指定無し) */
        static final String _keyAll = "9999";
        /** [学習の記録] 備考を保持する */
        private GakushuBiko _gakushuBiko;
        /** 年度と教科コード90の代替科目備考のマップ */
        private Map<String, List<String>> _studyRecBikoSubstitution90M = new HashMap<String, List<String>>();

        private String _entYear;
        private Semester _entSemester;
        // TODO: use EntGrdHist
        private String _entDate;
        private String _entReason;
        private String _entSchool;
        private String _entAddr;
        private String _entAddr2;
        private String _entDiv;
        private String _entDivName;
        private String _entDivName2;
        private String _entYearGrade;
        private String _entYearGradeCd;
        private String _grdYear;
        private Semester _grdSemester;
        private String _grdDate;
        private String _grdReason;
        private String _grdSchool;
        private String _grdAddr;
        private String _grdAddr2;
        private String _grdNo;
        private String _grdTerm;
        private String _grdDiv;
        private String _grdDivName;
        private String _grdYearGrade;
        private String _grdYearGradeCd;
        private String _curriculumYear;
        private String _jSchoolKindGrdDivName;

        private SchregBaseHistDat _tenka = new SchregBaseHistDat(new HashMap());
        private SchregBaseHistDat _tenseki = new SchregBaseHistDat(new HashMap());
        private boolean _isFirst;

        List<Gakuseki> _gakusekiList = Collections.emptyList();

        /** 進路/就職情報 */
        List<String> _afterGraduatedCourseTextList = Collections.emptyList();

        /** 進路/就職情報（京都府用） */
        String _afterGraduatedCourseSenkouKindSub;

        final boolean _isSogoShoken6BunkatsuTo3Bunkatsu; // 総合所見6分割を3分割の欄に表示
        final boolean _isSogoShoken6Bunkatsu; // 総合所見6分割
        final boolean _isSogoShoken3Bunkatsu; // 総合所見3分割
        final boolean _isSogoShoken3BunkatsuHikintou; // 総合所見3分割非均等
        /** 2なら所見サイズ変更 */
        final String _train_ref_1_2_3_field_size;
        final int[] _train_ref_1_2_3_field_sizeInt;
        final int _train_ref_1_2_3_gyo_sizeInt;

        private List<String> _abroadPrintDropRegdYears = Collections.emptyList(); // SCHREG_TRANSFER_DAT.ABROAD_PRINT_DROP_REGD='1'の年度 = 改ページしない

        private List<String> _kyugakuNendoList = Collections.emptyList();
        private List<Map<String, String>> _schregBaseHistList = Collections.emptyList();
        private List<Map<String, String>> _guardianHistOrGuarantorHistList = Collections.emptyList();

        private List<Integer> _hosokuAddressIndexList = Collections.emptyList();
        private List<Integer> _hosokuGuardAddressIndexList = Collections.emptyList();
        private List<Integer> _hosokuTransferRecIndexList = Collections.emptyList();
        private Map<String, List<Staff>> _hosokuYearStaffMap = Collections.emptyMap();
        private Map<String, List<Map<String, String>>> _hosokuYearPrincipalMap = Collections.emptyMap();

        private List<Map<String, String>> _schregBaseHistDatRealNameFlgCheckList = Collections.emptyList();
        private List<Map<String, String>> _schregBaseHistDatNameFlgCheckList = Collections.emptyList();
        private List<Map<String, String>> _guardianHistDatRealNameFlgCheckList = Collections.emptyList();
        private List<Map<String, String>> _guardianHistDatNameFlgCheckList = Collections.emptyList();
        private List<Map<String, String>> _guarantorHistDatRealNameFlgCheckList = Collections.emptyList();
        private List<Map<String, String>> _guarantorHistDatNameFlgCheckList = Collections.emptyList();

        String _entTensekiReason;
        String _grdTensekiReason;
        int _seisekiEndYear = -1;

        /**
         * コンストラクタ。
         */
        private PersonalInfo(
                final DB2UDB db2,
                final Student student,
                final Param param,
                final Map regRow,
                final Map guardianDatRow,
                final Map entGrdRow,
                final String comebackDate
        ) {
            super(student._schregno);
            _student = student;
            _regRow = regRow;
            _guardianDatRow = guardianDatRow;

//            _annual = getString("ANNUAL", regRow);
            _checkCourseName = KnjDbUtils.getString(regRow, "COURSENAME");
            if (param._z010.in(Z010.kyoto) && param._schoolDiv.isTanniSei(param._year, null, student)) {
                // 京都府単位制
                final String schooldivName = defstr(KnjDbUtils.getString(param._z001name1Map, param._schoolDiv.getSchooldiv(param._year, null, student)));
                _courseName = schooldivName + "による" + defstr(KnjDbUtils.getString(regRow, "COURSENAME"));
            } else {
                _courseName = KnjDbUtils.getString(regRow, "COURSENAME");
            }
            _majorName = KnjDbUtils.getString(regRow, "MAJORNAME");
            _coursecode = KnjDbUtils.getString(regRow, "COURSECODE");
            if (!param._seitoSidoYorokuNotPrintCoursecodes.contains(_coursecode)) {
                _coursecodeName = KnjDbUtils.getString(regRow, "COURSECODENAME");
            }

            _birthday = KnjDbUtils.getString(regRow, "BIRTHDAY");
            _birthdayStr = getBirthday(db2, _birthday, KnjDbUtils.getString(regRow, "BIRTHDAY_FLG"), param);
            _sex = KnjDbUtils.getString(regRow, "SEX");
            if ("1".equals(param.property(Property.seitoSidoYorokuFinschoolFinishDateYearOnly))) {
                _finishDate = setDateFormatInsertBlank(db2, formatDateNen(db2, KnjDbUtils.getString(regRow, "FINISH_DATE"), param), param, param._formatDateDefaultYear);
            } else {
                _finishDate = setDateFormatInsertBlank(db2, formatDateNenMonth(db2, KnjDbUtils.getString(regRow, "FINISH_DATE"), param), param, param._formatDateDefaultYear);
            }
            _installationDiv = param._isDefinecodeSchoolMarkHiro ? null : KnjDbUtils.getString(regRow, "INSTALLATION_DIV");
            _tengakuSakiGrade = KnjDbUtils.getString(regRow, "TENGAKU_SAKI_GRADE");
            _comebackDate = comebackDate;

            _entGrdRow = entGrdRow;
            _entYear    = KnjDbUtils.getString(entGrdRow, "ENT_YEAR");
            _entSemester = Semester.get(param._semesterMap, _entYear, KnjDbUtils.getString(entGrdRow, "ENT_SEMESTER"));
            _entDate    = KnjDbUtils.getString(entGrdRow, "ENT_DATE");
            _entReason  = KnjDbUtils.getString(entGrdRow, "ENT_REASON");
            _entSchool  = KnjDbUtils.getString(entGrdRow, "ENT_SCHOOL");
            _entAddr    = KnjDbUtils.getString(entGrdRow, "ENT_ADDR");
            _entAddr2   = KnjDbUtils.getString(entGrdRow, "ENT_ADDR2");
            _entDiv     = KnjDbUtils.getString(entGrdRow, "ENT_DIV");
            _entDivName = KnjDbUtils.getString(entGrdRow, "ENT_DIV_NAME");
            _entDivName2 = KnjDbUtils.getString(entGrdRow, "ENT_DIV_NAME2");
            _entYearGrade = KnjDbUtils.getString(entGrdRow, "ENT_YEAR_GRADE");
            _entYearGradeCd = KnjDbUtils.getString(entGrdRow, "ENT_YEAR_GRADE_CD");
            _grdYear    = KnjDbUtils.getString(entGrdRow, "GRD_YEAR");
            _grdSemester = Semester.get(param._semesterMap, _grdYear, KnjDbUtils.getString(entGrdRow, "GRD_SEMESTER"));
            _grdDate    = KnjDbUtils.getString(entGrdRow, "GRD_DATE");
            _grdReason  = KnjDbUtils.getString(entGrdRow, "GRD_REASON");
            _grdSchool  = KnjDbUtils.getString(entGrdRow, "GRD_SCHOOL");
            _grdAddr    = KnjDbUtils.getString(entGrdRow, "GRD_ADDR");
            _grdAddr2   = KnjDbUtils.getString(entGrdRow, "GRD_ADDR2");
            _grdNo      = KnjDbUtils.getString(entGrdRow, "GRD_NO");
            _grdTerm    = KnjDbUtils.getString(entGrdRow, "GRD_TERM");
            _grdDiv     = KnjDbUtils.getString(entGrdRow, "GRD_DIV");
            _grdDivName = KnjDbUtils.getString(entGrdRow, "GRD_DIV_NAME");
            _grdYearGrade = KnjDbUtils.getString(entGrdRow, "GRD_YEAR_GRADE");
            _grdYearGradeCd = KnjDbUtils.getString(entGrdRow, "GRD_YEAR_GRADE_CD");
            _curriculumYear = KnjDbUtils.getString(entGrdRow, "CURRICULUM_YEAR");
            if (getPrintGuarantor(param, _entDate, _birthday)) {
                final int guarantorAddressCount = Address.loadAddress(db2, student._schregno, param, Address.SQL_GUARANTOR, param._year, _entDate, _grdDate).size();
                _isPrintGuarantor = guarantorAddressCount > 0;
                log.info(" guarantorAddressCount = " + guarantorAddressCount + ", isPrintGuarantor = " + _isPrintGuarantor);
            } else {
                _isPrintGuarantor = false;
            }

            _isSogoShoken6BunkatsuTo3Bunkatsu = param._z010.in(Z010.shimaneken);
            _isSogoShoken6Bunkatsu = !_isSogoShoken6BunkatsuTo3Bunkatsu && "1".equals(param.property(Property.seitoSidoYorokuSogoShoken6Bunkatsu)) && entYearStart(param, this, Util.toInt(param.property(Property.seitoSidoYorokuSogoShoken6BunkatsuStartYear), 2020 - 3)); // 2020年度の高校3年生から
            _isSogoShoken3Bunkatsu = !_isSogoShoken6Bunkatsu && (param._z010.in(Z010.kaijyo, Z010.bunkyo, Z010.rakunan) || param._isHigashiosakaKashiwara || "1".equals(param.property(Property.seitoSidoYorokuSogoShoken3Bunkatsu))); // 埼玉栄、土佐女子、開智未来、青山学院、城東

            _train_ref_1_2_3_field_size = notblankstr(param.property(Property.seitoSidoYoroku_train_ref_1_2_3_field_size), param.property(Property.train_ref_1_2_3_field_size));
            _train_ref_1_2_3_field_sizeInt = new int[] {14, 14, 14};

            final String train_ref_1_2_3_field_size = notblankstr(param.property(Property.seitoSidoYoroku_train_ref_1_2_3_field_size), param.property(Property.train_ref_1_2_3_field_size));
            if (!StringUtils.isBlank(train_ref_1_2_3_field_size)) {
                final int INVALID = -1;
                boolean isValidTrainRef123 = true;
                String[] split;
                split = StringUtils.split(train_ref_1_2_3_field_size, "-");
                if (split.length == 3) {
                    _train_ref_1_2_3_field_sizeInt[0] = Util.toInt(split[0], INVALID);
                    _train_ref_1_2_3_field_sizeInt[1] = Util.toInt(split[1], INVALID);
                    _train_ref_1_2_3_field_sizeInt[2] = Util.toInt(split[2], INVALID);
                    if (ArrayUtils.contains(_train_ref_1_2_3_field_sizeInt, INVALID)) {
                        isValidTrainRef123 = false;
                    }
                    if (isValidTrainRef123 == false) {
                        if ("1".equals(train_ref_1_2_3_field_size)) {
                            _train_ref_1_2_3_field_sizeInt[0] = 14;
                            _train_ref_1_2_3_field_sizeInt[1] = 21;
                            _train_ref_1_2_3_field_sizeInt[2] = 7;
                            isValidTrainRef123 = true;
                        } else if ("2".equals(train_ref_1_2_3_field_size)) {
                            _train_ref_1_2_3_field_sizeInt[0] = 21;
                            _train_ref_1_2_3_field_sizeInt[1] = 21;
                            _train_ref_1_2_3_field_sizeInt[2] = 7;
                            isValidTrainRef123 = true;
                        }
                    }
                    log.info(" valid1 = " + isValidTrainRef123 + ", _train_ref_1_2_3_field_sizeInt = " + ArrayUtils.toString(_train_ref_1_2_3_field_sizeInt));
                }
                if (isValidTrainRef123 == false) {
                    final String prop = param.property(Property.train_ref_1_2_3_field_size);
                    isValidTrainRef123 = true;
                    split = StringUtils.split(prop, "-");
                    if (null != split && split.length == 3) {
                        _train_ref_1_2_3_field_sizeInt[0] = Util.toInt(split[0], INVALID);
                        _train_ref_1_2_3_field_sizeInt[1] = Util.toInt(split[1], INVALID);
                        _train_ref_1_2_3_field_sizeInt[2] = Util.toInt(split[2], INVALID);
                        if (ArrayUtils.contains(_train_ref_1_2_3_field_sizeInt, INVALID)) {
                            isValidTrainRef123 = false;
                        }
                    }
                    if (isValidTrainRef123 == false) {
                        if ("1".equals(prop)) {
                            _train_ref_1_2_3_field_sizeInt[0] = 14;
                            _train_ref_1_2_3_field_sizeInt[1] = 21;
                            _train_ref_1_2_3_field_sizeInt[2] = 7;
                            isValidTrainRef123 = true;
                        } else if ("2".equals(prop)) {
                            _train_ref_1_2_3_field_sizeInt[0] = 21;
                            _train_ref_1_2_3_field_sizeInt[1] = 21;
                            _train_ref_1_2_3_field_sizeInt[2] = 7;
                            isValidTrainRef123 = true;
                        } else {
                            _train_ref_1_2_3_field_sizeInt[0] = 14;
                            _train_ref_1_2_3_field_sizeInt[1] = 14;
                            _train_ref_1_2_3_field_sizeInt[2] = 14;
                            isValidTrainRef123 = true;
                        }
                    }
                    log.info(" valid2 = " + isValidTrainRef123 + ", _train_ref_1_2_3_field_sizeInt = " + ArrayUtils.toString(_train_ref_1_2_3_field_sizeInt));
                }
                _isSogoShoken3BunkatsuHikintou = !(_train_ref_1_2_3_field_sizeInt[0] == _train_ref_1_2_3_field_sizeInt[1] && _train_ref_1_2_3_field_sizeInt[0] == _train_ref_1_2_3_field_sizeInt[2]);
            } else {
                _isSogoShoken3BunkatsuHikintou = (param._z010.in(Z010.kaijyo) || param._isHigashiosakaKashiwara);
                if (_isSogoShoken3BunkatsuHikintou) {
                    _train_ref_1_2_3_field_sizeInt[0] = 21;
                    _train_ref_1_2_3_field_sizeInt[1] = 21;
                    _train_ref_1_2_3_field_sizeInt[2] = 7;
                } else {
                    _train_ref_1_2_3_field_sizeInt[0] = 14;
                    _train_ref_1_2_3_field_sizeInt[1] = 14;
                    _train_ref_1_2_3_field_sizeInt[2] = 14;
                }
            }

            _train_ref_1_2_3_gyo_sizeInt = Util.toInt(notblankstr(param.property(Property.seitoSidoYoroku_train_ref_1_2_3_gyo_size), param._train_ref_1_2_3_gyo_size), 7);
        }

        public List<? extends Gakuseki> getGakusekiList() {
            return _gakusekiList;
        }

        public Collection<String> creditOnlyClasscdList(final Param param) {
            // 離して表示する教科コード
            final List<String> creditOnlyClasscdList = new ArrayList<String>();
            if (!StringUtils.isBlank(param.property(Property.seitoSidoYorokuCreditOnlyClasscd))) {
                for (final String classcd : param.property(Property.seitoSidoYorokuCreditOnlyClasscd).split("\\s,\\s")) {
                    creditOnlyClasscdList.add(classcd.trim());
                }
            } else if (!StringUtils.isBlank(param.property(Property.seitoSidoYorokuHanasuClasscd))) {
                for (final String classcd : param.property(Property.seitoSidoYorokuHanasuClasscd).split("\\s,\\s")) {
                    creditOnlyClasscdList.add(classcd.trim());
                }
            }
            final Map<String, String> creditOnlyClasscdMap = new HashMap<String, String>();
            creditOnlyClasscdMap.put(Z010.tokiwa.name(), _94);
            creditOnlyClasscdMap.put(Z010.nishiyama.name(), _94);
            creditOnlyClasscdMap.put(Z010.bunkyo.name(), _94);
            creditOnlyClasscdMap.put(Z010.rakunan.name(), _95);
            final String classcd = creditOnlyClasscdMap.get(param._z010name1);
            if (null != classcd) {
                creditOnlyClasscdList.add(classcd);
            }
            if (param._isOutputDebug) {
                param.logOnce(" schregno " + _schregno + " creditOnlyClasscd = " + creditOnlyClasscdList);
            }
            return creditOnlyClasscdList;
        }

        public Collection<String> jiritsuKatudouClasscdList(final Param param) {
            final List<String> rtn = new ArrayList<String>();
            if (param._z010.in(Z010.shimaneken)) {
                if (_student.isShimanekenPrintJiritsuKatudou(param)) {
                    rtn.add(_94);
                }
            } else if (param._z010.in(Z010.kyoto)) {
                rtn.add(_88);
            }
            if (param._isOutputDebug) {
                param.logOnce(" schregno " + _schregno + " jiritsuKatudouClasscdList = " + rtn + " / " + _student._handicap);
            }
            return rtn;
        }

        private Collection<String> getDropYears(final Param param) {
            final List<Gakuseki> gakusekiList = _gakusekiList;
            final Collection<String> dropYears = new HashSet<String>();
            if (!param._schoolDiv.isGakunenSei(null, this, _student) && !_student.certifSchool(param)._isGenkyuRyuchi) {
                return dropYears;
            }
            for (final Gakuseki gaku : gakusekiList) {
                if (gaku._isDrop) {
                    dropYears.add(gaku._year);
                }
            }
            return dropYears;
        }

        private Collection<String> getEnabledDropYears(final Param param) {
            final Collection<String> enabledDropYears = new HashSet<String>();
            if (!param._schoolDiv.isGakunenSei(null, this, _student) && !_student.certifSchool(param)._isGenkyuRyuchi) {
                return enabledDropYears;
            }
            for (final ListIterator<Gakuseki> i = _gakusekiList.listIterator(_gakusekiList.size()); i.hasPrevious();) {
                final Gakuseki gaku = i.previous();
                if (gaku._droppedRecordInTengakuTaigakuYearEnabled) {
                    enabledDropYears.add(gaku._year);
                    break;
                }
            }
            return enabledDropYears;
        }

        protected static boolean entYearStart(final Param param, final PersonalInfo pInfo, final int checkYear) {
            boolean rtn = false;
            if (null != pInfo && NumberUtils.isDigits(pInfo._entYear)) {
                final int iEntYear = Integer.parseInt(pInfo._entYear);
                if (checkYear > iEntYear) {
                    rtn = false;
                } else if (checkYear <= iEntYear) {
                    if (NumberUtils.isDigits(pInfo._entYearGradeCd)) {
                        final int iAnnual = Integer.parseInt(pInfo._entYearGradeCd);
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
            if (param._isOutputDebug) {
                log.info(" entYearStart = " + rtn + ", student curYear = " + (null == pInfo ? "" : pInfo._curriculumYear) + ", entYear =" + (null == pInfo ? "" : pInfo._entYear) + ", entYearGradeCd = " + (null == pInfo ? "" : pInfo._entYearGradeCd));
            }
            return rtn;
        }

        private void setName(final Param param) {

            _isPrintRealName = "1".equals(KnjDbUtils.getString(_regRow, "USE_REAL_NAME")) || "1".equals(param.property(Property.certifPrintRealName));
            _isPrintNameAndRealName = "1".equals(KnjDbUtils.getString(_regRow, "NAME_OUTPUT_FLG"));
            final Map schregBaseHistDatRealNameFlg1 = KnjDbUtils.firstRow(_schregBaseHistDatRealNameFlgCheckList);
            if (param._isOutputDebug && !schregBaseHistDatRealNameFlg1.isEmpty()) {
                log.info(Util.debugMapToStr("schregBaseHistDatRealNameFlg1 = ", schregBaseHistDatRealNameFlg1, ", "));
            }

            final Map<String, String> schregBaseHistDatNameFlg1 = KnjDbUtils.firstRow(_schregBaseHistDatNameFlgCheckList);
            if (param._isOutputDebug && !schregBaseHistDatNameFlg1.isEmpty()) {
                log.info(Util.debugMapToStr("schregBaseHistDatNameFlg1 = ", schregBaseHistDatNameFlg1, ", "));
            }

            final String name = defstr(KnjDbUtils.getString(_regRow, "NAME"));
            if (_isPrintRealName) {
                final String realName = defstr(KnjDbUtils.getString(_regRow, "REAL_NAME"));
                final String realNameHistFirst = defstr(KnjDbUtils.getString(schregBaseHistDatRealNameFlg1, "REAL_NAME"));
                final String nameWithRealNameHistFirst = defstr(KnjDbUtils.getString(schregBaseHistDatRealNameFlg1, "NAME"));

                if (_isPrintNameAndRealName) {
                    if (StringUtils.isBlank(realName + name)) {
                        _studentRealName = "";
                        _studentName     = "";
                        _studentNameNextDateOfHistLastDate = null;
                    } else {
                        _studentRealName = realName;
                        _studentName     = StringUtils.isBlank(name) ? "" : realName.equals(name) ? name : "（" + name + "）";
                    }
                    _studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst.equals(nameWithRealNameHistFirst) ? realNameHistFirst : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                    _studentNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_schregBaseHistDatRealNameFlgCheckList), "EXPIREDATE"), 1);
                } else {
                    _studentRealName      = StringUtils.isBlank(realName) ? name : realName;
                    _studentName          = name;
                    _studentNameHistFirst = realNameHistFirst;
                    _studentNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_schregBaseHistDatRealNameFlgCheckList), "EXPIREDATE"), 1);
                }
            } else {
                _studentRealName      = "";
                _studentName          = name;
                _studentNameHistFirst = defstr(KnjDbUtils.getString(schregBaseHistDatNameFlg1, "NAME"));
                _studentNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_schregBaseHistDatNameFlgCheckList), "EXPIREDATE"), 1);
            }

            final String nameKana0 = defstr(KnjDbUtils.getString(_regRow, "NAME_KANA"));
            if (_isPrintRealName) {
                final String realNameKana = defstr(KnjDbUtils.getString(_regRow, "REAL_NAME_KANA"));
                final String realNameKanaHistFirst = defstr(KnjDbUtils.getString(schregBaseHistDatRealNameFlg1, "REAL_NAME_KANA"));
                final String nameKanaWithRealNameKanaHistFirst = defstr(KnjDbUtils.getString(schregBaseHistDatRealNameFlg1, "NAME_KANA"));

                _studentKana = HistVal.of(
                        getNameForm(nameKana0, realNameKana, _isPrintRealName, _isPrintNameAndRealName),
                        getNameForm(nameKanaWithRealNameKanaHistFirst, realNameKanaHistFirst, _isPrintRealName, _isPrintNameAndRealName)
                        );

            } else {
                _studentKana = HistVal.of(nameKana0, defstr(KnjDbUtils.getString(schregBaseHistDatNameFlg1, "NAME_KANA")));
            }

            final String guardKana0 = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARD_KANA"));
            final String guardName0 = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARD_NAME"));
            final String guarantorKana0 = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARANTOR_KANA"));
            final String guarantorName0 = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARANTOR_NAME"));

            final boolean isGuardPrintRealName = "1".equals(KnjDbUtils.getString(_regRow, "USE_GUARD_REAL_NAME")) || "1".equals(param.property(Property.certifPrintRealName));
            final boolean isGuardPrintNameAndRealName = "1".equals(KnjDbUtils.getString(_regRow, "GUARD_NAME_OUTPUT_FLG"));
            if (isGuardPrintRealName) {
                if (_isPrintGuarantor) {
                    final Map guarantorHistDatRealNameFlg1 = KnjDbUtils.firstRow(_guarantorHistDatRealNameFlgCheckList);
                    if (param._isOutputDebug && !guarantorHistDatRealNameFlg1.isEmpty()) {
                        log.info(Util.debugMapToStr("guarantorHistDatRealNameFlg1 = ", guarantorHistDatRealNameFlg1, ", "));
                    }

                    final String guarantorRealKana = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARANTOR_REAL_KANA"));
                    final String guarantorRealKanaHistFirst = defstr(KnjDbUtils.getString(guarantorHistDatRealNameFlg1, "GUARANTOR_REAL_KANA"));
                    final String guarantorKanaWithGuarantorRealNameHistFirst = defstr(KnjDbUtils.getString(guarantorHistDatRealNameFlg1, "GUARANTOR_KANA"));

                    _guardianOrGuarantorKana = HistVal.of(
                            getNameForm(guarantorKana0, guarantorRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName),
                            getNameForm(guarantorKanaWithGuarantorRealNameHistFirst, guarantorRealKanaHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName)
                            );

                    final String guarantorRealName = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARANTOR_REAL_NAME"));
                    final String guarantorRealNameHistFirst = defstr(KnjDbUtils.getString(guarantorHistDatRealNameFlg1, "GUARANTOR_REAL_NAME"));
                    final String guarantorNameWithGuarantorRealNameHistFirst = defstr(KnjDbUtils.getString(guarantorHistDatRealNameFlg1, "GUARANTOR_NAME"));

                    _guardianOrGuarantorName = HistVal.of(
                            getNameForm(guarantorName0, guarantorRealName, isGuardPrintRealName, isGuardPrintNameAndRealName),
                            getNameForm(guarantorNameWithGuarantorRealNameHistFirst, guarantorRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName)
                            );
                    _guardianOrGuarantorNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_guarantorHistDatRealNameFlgCheckList), "EXPIREDATE"), 1);

                } else {

                    final Map<String, String> guardianHistDatRealNameFlg1 = KnjDbUtils.firstRow(_guardianHistDatRealNameFlgCheckList);
                    if (param._isOutputDebug && !guardianHistDatRealNameFlg1.isEmpty()) {
                        log.info(Util.debugMapToStr("guardianHistDatRealNameFlg1 = ", guardianHistDatRealNameFlg1, ", "));
                    }
                    final String guardRealKana = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARD_REAL_KANA"));
                    final String guardRealKanaHistFirst = defstr(KnjDbUtils.getString(guardianHistDatRealNameFlg1, "GUARD_REAL_KANA"));
                    final String guardKanaWithGuardRealNameHistFirst = defstr(KnjDbUtils.getString(guardianHistDatRealNameFlg1, "GUARD_KANA"));

                    _guardianOrGuarantorKana = HistVal.of(
                            getNameForm(guardKana0, guardRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName),
                            getNameForm(guardKanaWithGuardRealNameHistFirst, guardRealKanaHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName)
                            );

                    final String guardRealName = defstr(KnjDbUtils.getString(_guardianDatRow, "GUARD_REAL_NAME"));
                    final String guardRealNameHistFirst = defstr(KnjDbUtils.getString(guardianHistDatRealNameFlg1, "GUARD_REAL_NAME"));
                    final String guardNameWithGuardRealNameHistFirst = defstr(KnjDbUtils.getString(guardianHistDatRealNameFlg1, "GUARD_NAME"));

                    _guardianOrGuarantorName = HistVal.of(
                            getNameForm(guardName0, guardRealName, isGuardPrintRealName, isGuardPrintNameAndRealName),
                            getNameForm(guardNameWithGuardRealNameHistFirst, guardRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName)
                            );
                    _guardianOrGuarantorNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_guardianHistDatRealNameFlgCheckList), "EXPIREDATE"), 1);
                }

            } else {
                if (_isPrintGuarantor) {
                    final Map guarantorHistDatNameFlg1 = KnjDbUtils.firstRow(_guarantorHistDatNameFlgCheckList);
                    if (param._isOutputDebug && !guarantorHistDatNameFlg1.isEmpty()) {
                        log.info(Util.debugMapToStr("guarantorHistDatNameFlg1 = ", guarantorHistDatNameFlg1, ", "));
                    }
                    _guardianOrGuarantorKana = HistVal.of(guarantorKana0, defstr(KnjDbUtils.getString(guarantorHistDatNameFlg1, "GUARANTOR_KANA")));
                    _guardianOrGuarantorName = HistVal.of(guarantorName0, defstr(KnjDbUtils.getString(guarantorHistDatNameFlg1, "GUARANTOR_NAME")));
                    _guardianOrGuarantorNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_guarantorHistDatNameFlgCheckList), "EXPIREDATE"), 1);
                } else {
                    final Map guardianHistDatNameFlg1 = KnjDbUtils.firstRow(_guardianHistDatNameFlgCheckList);
                    if (param._isOutputDebug && !guardianHistDatNameFlg1.isEmpty()) {
                        log.info(Util.debugMapToStr("guardianHistDatNameFlg1 = ", guardianHistDatNameFlg1, ", "));
                    }
                    _guardianOrGuarantorKana = HistVal.of(guardKana0, defstr(KnjDbUtils.getString(guardianHistDatNameFlg1, "GUARD_KANA")));
                    _guardianOrGuarantorName = HistVal.of(guardName0, defstr(KnjDbUtils.getString(guardianHistDatNameFlg1, "GUARD_NAME")));
                    _guardianOrGuarantorNameNextDateOfHistLastDate = addDay(KnjDbUtils.getString(KnjDbUtils.lastRow(_guardianHistDatNameFlgCheckList), "EXPIREDATE"), 1);
                }
            }
        }

        /**
         * 様式1表印字用の生徒氏名
         * @return
         */
        private String getPrintName1() {
            final String printName;
            if (_isPrintRealName && _isPrintNameAndRealName && !_studentRealName.equals(_studentName)) {
                printName = _studentRealName + _studentName;
            } else if (_isPrintRealName) {
                printName = _studentRealName;
            } else {
                printName = _studentName;
            }
            return printName;
        }

        public boolean isDropBefore(final Gakuseki gakuseki) {
            if (_abroadPrintDropRegdYears.isEmpty()) {
                return true;
            }
            for (String year : _abroadPrintDropRegdYears) {
                if (NumberUtils.isDigits(year) && NumberUtils.isDigits(gakuseki._year)) {
                    if (Integer.parseInt(year) <= Integer.parseInt(gakuseki._year)) {
                        return false;
                    }
                }
            }
            return true;
        }

        public Collection<PersonalInfo> fuyasu(final DB2UDB db2, final Student student, final Param param, final boolean useStartYear) {
            if (isTenseki(param, 0, 9999)) {
                // 増やす!
                log.debug("増やす! " + _schregno + "|" + _studentName);

                String befTensekidateGradeName = "";
                if (param._schoolDiv.isGakunenSei(null, this, student) || student.certifSchool(param)._isGenkyuRyuchi) {
                    befTensekidateGradeName = getGradeName(this._tenseki.grade());
                }
                String aftTensekidateGradeName = "";
                if (param._schoolDiv.isGakunenSei(null, this, student) || student.certifSchool(param)._isGenkyuRyuchi) {
                    final Gakuseki gakuseki = this.getDateYearGakuseki(this._tenseki.date());
                    if (null != gakuseki) {
                        aftTensekidateGradeName = getGradeName(defstr(gakuseki._gdat._gradeCd, gakuseki._grade));
                    }
                }

                final String befTensekiReason;
                final String aftTensekiReason;
                if (param._z010.in(Z010.miyagiken)) {
                    befTensekiReason = "本校" + defstr(this._courseName) + "課程" + aftTensekidateGradeName + "へ転籍";
                    aftTensekiReason = "本校" + defstr(this._tenseki.courseName()) + "課程" + befTensekidateGradeName + "より転籍";
                } else {
                    befTensekiReason = "本校" + defstr(this._tenseki.courseName()) + "課程より" + defstr(this._courseName) + "課程へ転籍";
                    aftTensekiReason = befTensekiReason;
                }
                final PersonalInfo bef = this.copy(db2, param, student);
                bef._grdDiv = String.valueOf(GRD_DIV_TENSEKI);
                bef._grdDate = bef._tenseki.date();
                PersonalInfo.loadOthers(db2, student, param, useStartYear ? bef._entYear : null, bef);
                final Gakuseki befTensekiDateGakuseki = bef.getGakuseki(bef._tenseki.year(), bef._tenseki.grade());
                if (null == befTensekiDateGakuseki) {
                    log.warn(" not found tenseki date gakuseki.");
                } else {
                    final SchregRegdHdat hdat = SchregRegdHdat.getSchregRegdHdat(bef._tenseki.year(), bef._tenseki.semester(), bef._tenseki.grade(), bef._tenseki.hrClass(), param._hrdatMap);
                    befTensekiDateGakuseki._hdat = hdat;
                    log.info(" set tenseki hdat = " + hdat);
                    befTensekiDateGakuseki._attendno = bef._tenseki.attendno();
                }
                bef._grdReason = null;
                bef._grdAddr = null;
                bef._grdSchool = null;
                bef._grdAddr2 = null;
                bef._courseName = this._tenseki.courseName();
                bef._grdTensekiReason = befTensekiReason;

                final PersonalInfo aft = this.copy(db2, param, student);
                aft._entDiv = String.valueOf(ENT_DIV_TENSEKI);
                aft._entDate = addDay(bef._tenseki.date(), 1);
                PersonalInfo.loadOthers(db2, student, param, useStartYear ? aft._entYear : null, aft);
                aft._entReason = null;
                aft._entAddr = null;
                aft._entSchool = null;
                aft._entAddr2 = null;
                aft._entTensekiReason = aftTensekiReason;

                return Arrays.asList(bef, aft);
            }


            if (isTenka(param, 0, 9999)) {
                log.info("tenka!");

                if (param._z010.in(Z010.miyagiken)) {

                    String befTenkadateGradeName = "";
                    if (param._schoolDiv.isGakunenSei(null, this, student) || student.certifSchool(param)._isGenkyuRyuchi) {
                        befTenkadateGradeName = getGradeName(this._tenka.grade());
                    }
                    String aftTenkadateGradeName = "";
                    if (param._schoolDiv.isGakunenSei(null, this, student) || student.certifSchool(param)._isGenkyuRyuchi) {
                        final Gakuseki gakuseki = this.getDateYearGakuseki(this._tenka.date());
                        if (null != gakuseki) {
                            aftTenkadateGradeName = getGradeName(defstr(gakuseki._gdat._gradeCd, gakuseki._grade));
                        }
                    }

                    // 宮城県は2枚
                    log.debug("転科増やす! " + _schregno + "|" + _studentName);
                    final PersonalInfo bef = this.copy(db2, param, student);
                    bef._grdDiv = String.valueOf(GRD_DIV_TENKA);
                    bef._grdDate = bef._tenka.date();
                    bef._tenka.setDate(null);
                    PersonalInfo.loadOthers(db2, student, param, useStartYear ? bef._entYear : null, bef);
                    final Gakuseki befTenkaDateGakuseki = bef.getGakuseki(bef._tenka.year(), bef._tenka.grade());
                    if (null != befTenkaDateGakuseki) {
                        final SchregRegdHdat hdat = SchregRegdHdat.getSchregRegdHdat(bef._tenka.year(), bef._tenka.semester(), bef._tenka.grade(), bef._tenka.hrClass(), param._hrdatMap);
                        befTenkaDateGakuseki._hdat = hdat;
                        log.info(" set tenka hdat = " + hdat);
                        befTenkaDateGakuseki._attendno = bef._tenka.attendno();
                    }
                    bef._grdReason = null;
                    bef._grdAddr = null;
                    bef._grdSchool = null;
                    bef._grdAddr2 = null;
                    bef._courseName = this._tenka.courseName();
                    bef._majorName = this._tenka.majorName();
                    if (!param._seitoSidoYorokuNotPrintCoursecodes.contains(this._tenka.coursecode())) {
                        bef._coursecodeName = this._tenka.coursecodeName();
                    }
                    bef._grdTensekiReason = "本校" + defstr(this._courseName) + "課程" + defstr(this._majorName) + aftTenkadateGradeName + "へ転科";

                    final PersonalInfo aft = this.copy(db2, param, student);
                    aft._entDiv = String.valueOf(ENT_DIV_TENKA);
                    aft._entDate = addDay(bef._grdDate, 1);
                    aft._tenka.setDate(null);
                    PersonalInfo.loadOthers(db2, student, param, useStartYear ? aft._entYear : null, aft);
                    aft._entReason = null;
                    aft._entAddr = null;
                    aft._entSchool = null;
                    aft._entAddr2 = null;
                    aft._entTensekiReason = "本校" + defstr(this._tenka.courseName()) + "課程" + defstr(this._tenka.majorName()) + befTenkadateGradeName + "より転科";

                    return Arrays.asList(bef, aft);

                } else {
                    this._entTensekiReason = "本校" + defstr(this._tenka.courseName()) + "課程" + defstr(this._tenka.majorName()) + "より" + defstr(this._majorName) + "へ転科";

                    return Collections.singleton(this);
                }
            }

            return Collections.singleton(this);
        }

        public PersonalInfo copy(final DB2UDB db2, final Param param, final Student student) {
            log.info(" copy PersonalInfo.");
            final PersonalInfo rtn = new PersonalInfo(db2, student, param, new HashMap(_regRow), new HashMap(_guardianDatRow), new HashMap(_entGrdRow), _comebackDate);

            rtn._title = _title;

            rtn._schregBaseHistDatRealNameFlgCheckList = emptyOrCopy(_schregBaseHistDatRealNameFlgCheckList);
            rtn._schregBaseHistDatNameFlgCheckList = emptyOrCopy(_schregBaseHistDatNameFlgCheckList);
            rtn._guardianHistDatRealNameFlgCheckList = emptyOrCopy(_guardianHistDatRealNameFlgCheckList);
            rtn._guardianHistDatNameFlgCheckList = emptyOrCopy(_guardianHistDatNameFlgCheckList);
            rtn._guarantorHistDatRealNameFlgCheckList = emptyOrCopy(_guarantorHistDatRealNameFlgCheckList);
            rtn._guarantorHistDatNameFlgCheckList = emptyOrCopy(_guarantorHistDatNameFlgCheckList);
            rtn.setName(param);

            rtn._addressGrdHeader = _addressGrdHeader;
            rtn._isFuhakkou = _isFuhakkou;

            rtn._addressList = nullOrCopy(_addressList);
            rtn._guardianAddressList = nullOrCopy(_guardianAddressList);
            rtn._zaigakusubekiKikan = _zaigakusubekiKikan;
            rtn._studyRecList = nullOrCopy(_studyRecList);

            rtn._studentKana = _studentKana;
            rtn._guardianOrGuarantorKana = _guardianOrGuarantorKana;
            rtn._guardianOrGuarantorName = _guardianOrGuarantorName;

            rtn._gakushuBiko = null == _gakushuBiko ? null : _gakushuBiko.copy();

            rtn._tenka = new SchregBaseHistDat(_tenka._row);
            rtn._tenseki = new SchregBaseHistDat(_tenseki._row);
            rtn._isFirst = _isFirst;

            rtn._gakusekiList = nullOrCopy(_gakusekiList);

            rtn._afterGraduatedCourseTextList = nullOrCopy(_afterGraduatedCourseTextList);

            rtn._afterGraduatedCourseSenkouKindSub = _afterGraduatedCourseSenkouKindSub;

            rtn._abroadPrintDropRegdYears = nullOrCopy(_abroadPrintDropRegdYears);

            rtn._kyugakuNendoList = nullOrCopy(_kyugakuNendoList);
            rtn._schregBaseHistList = nullOrCopy(_schregBaseHistList);
            rtn._guardianHistOrGuarantorHistList = nullOrCopy(_guardianHistOrGuarantorHistList);

            rtn._hosokuAddressIndexList = nullOrCopy(_hosokuAddressIndexList);
            rtn._hosokuGuardAddressIndexList = new ArrayList(_hosokuGuardAddressIndexList);
            rtn._hosokuTransferRecIndexList = new ArrayList(_hosokuTransferRecIndexList);
            rtn._hosokuYearStaffMap = null == _hosokuYearStaffMap ? null : new HashMap(_hosokuYearStaffMap);
            rtn._hosokuYearPrincipalMap = null == _hosokuYearPrincipalMap ? null : new HashMap(_hosokuYearPrincipalMap);

            rtn._studyRecReplaceList = nullOrCopy(_studyRecReplaceList);

            rtn._studyRecBikoSubstitution90M = new HashMap(_studyRecBikoSubstitution90M);

            rtn._entTensekiReason = _entTensekiReason;
            rtn._grdTensekiReason = _grdTensekiReason;

            return rtn;
        }

        private int entDivInt() {
            if (!NumberUtils.isDigits(_entDiv)) {
                return -1;
            }
            return Integer.parseInt(_entDiv);
        }

        private boolean isTennyu() {
            return 4 == entDivInt();
        }

        private boolean isHennyu() {
            return 5 == entDivInt();
        }

        private boolean isEntDivTenseki(final Param param, final int yearMin, final int yearMax) {
            if (ENT_DIV_TENSEKI == entDivInt()) {
                return Util.dateBetweenYear(param, _entDate, yearMin, yearMax, "ent tenseki");
            }
            return false;
        }

        private boolean isEntDivTenka(final Param param, final int yearMin, final int yearMax) {
            if (ENT_DIV_TENKA == entDivInt()) {
                return Util.dateBetweenYear(param, _entDate, yearMin, yearMax, "ent tenka");
            }
            return false;
        }

        private boolean isTenka(final Param param, final int yearMin, final int yearMax) {
            if (null != _tenka.date()) {
                return Util.dateBetweenYear(param, _tenka.date(), yearMin, yearMax, "tenka");
            }
            return false;
        }

        private boolean isTenseki(final Param param, final int yearMin, final int yearMax) {
            if (null != _tenseki.date()) {
                return Util.dateBetweenYear(param, _tenseki.date(), yearMin, yearMax, "tenseki");
            }
            return false;
        }

        private boolean isGrdDivTenseki(final Param param, final int yearMin, final int yearMax) {
            if (GRD_DIV_TENSEKI == grdDivInt()) {
                return Util.dateBetweenYear(param, _grdDate, yearMin, yearMax, "grd tenseki");
            }
            return false;
        }

        private boolean isGrdDivTenka(final Param param, final int yearMin, final int yearMax) {
            if (GRD_DIV_TENKA == grdDivInt()) {
                return Util.dateBetweenYear(param, _grdDate, yearMin, yearMax, "grd tenka");
            }
            return false;
        }

        protected int grdDivInt() {
            if (!NumberUtils.isDigits(_grdDiv)) {
                return -1;
            }
            return Integer.parseInt(_grdDiv);
        }

        protected boolean isSotsugyo() {
            return 1 == grdDivInt();
        }

        protected boolean isTaigaku() {
            return 2 == grdDivInt();
        }

        protected boolean isTengaku() {
            return 3 == grdDivInt();
        }

        private boolean isJoseki() {
            return 6 == grdDivInt();
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

        /**
         * 指定日付の年度の学籍を得る
         * @param date 指定日付
         * @return 学籍
         */
        private Gakuseki getDateYearGakuseki(final String date) {
            if (date == null) {
                return null;
            }
            final int year = getNendo(getCalendarOfDate(date));
            for (final Gakuseki gaku : _gakusekiList) {
                if (NumberUtils.isDigits(gaku._year) && Integer.parseInt(gaku._year) == year) {
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

            final Map guardianDatRow = getGuardianDatRow(db2, student, param);

            final Map entGrdRow = getEntGrdRow(db2, student, param.SCHOOL_KIND, comebackDate, param);

            final PersonalInfo pInfo = new PersonalInfo(db2, student, param, regRow, guardianDatRow, entGrdRow, comebackDate);
            if ("1".equals(param.property(Property.seitoSidoYorokuUsePrevSchoolKindGrdDivNameAsFinschoolGrdName))) {
                pInfo._jSchoolKindGrdDivName = KnjDbUtils.getString(getEntGrdRow(db2, student, "J", null, param), "GRD_DIV_NAME");
            }

            pInfo._schregBaseHistDatRealNameFlgCheckList = getHistList(db2, student._schregno, "SCHREG_BASE_HIST_DAT", 1, param, pInfo._entDate, pInfo._grdDate);
            pInfo._schregBaseHistDatNameFlgCheckList     = getHistList(db2, student._schregno, "SCHREG_BASE_HIST_DAT", 0, param, pInfo._entDate, pInfo._grdDate);

            if (pInfo._isPrintGuarantor) {
                pInfo._guarantorHistDatRealNameFlgCheckList  = getHistList(db2, student._schregno, "GUARANTOR_HIST_DAT", 1, param, pInfo._entDate, pInfo._grdDate);
                pInfo._guarantorHistDatNameFlgCheckList      = getHistList(db2, student._schregno, "GUARANTOR_HIST_DAT", 0, param, pInfo._entDate, pInfo._grdDate);
            } else {
                pInfo._guardianHistDatRealNameFlgCheckList   = getHistList(db2, student._schregno, "GUARDIAN_HIST_DAT", 1, param, pInfo._entDate, pInfo._grdDate);
                pInfo._guardianHistDatNameFlgCheckList       = getHistList(db2, student._schregno, "GUARDIAN_HIST_DAT", 0, param, pInfo._entDate, pInfo._grdDate);
            }
            pInfo.setName(param);

            setBaseHistTenkaTensekiDate(db2, student._schregno, pInfo, "TENKA", comebackDate, param);
            setBaseHistTenkaTensekiDate(db2, student._schregno, pInfo, "TENSEKI", comebackDate, param);
            loadOthers(db2, student, param, useStartYear ? pInfo._entYear : null, pInfo);
            return pInfo;
        }

        private static Map<String, String> getGuardianDatRow(final DB2UDB db2, final Student student, final Param param) {
            final String psKey = "PS_GUARDIAN_DAT";
            if (null == param.getPs(psKey)) {
                final String sql = " SELECT * FROM GUARDIAN_DAT WHERE SCHREGNO = ? ";
                param.setPs(psKey, db2, sql);
            }

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno }));
        }

        public static void loadOthers(final DB2UDB db2, final Student student, final Param param, final String startYear, final PersonalInfo pi) {
            pi._gakusekiList = loadGakuseki(db2, student, pi, param, startYear, pi._grdDate);
            final List<TransferRec> transferRecList = TransferRec.getGradeOnlyTransferList(param, pi._isFirst, student._transferRecList, pi._gakusekiList);
            pi._kyugakuNendoList = new ArrayList();
            for (final TransferRec tr : transferRecList) {
                if (TransferRec.A004_NAMECD2_KYUGAKU.equals(tr._transfercd) && tr.isFrom0401To0331()) {
                    pi._kyugakuNendoList.add(tr._sYear);
                }
            }
            if (param._isOutputDebugData) {
                log.info(" transferRecList = " + transferRecList + " (src = " + student._transferRecList + ")");
            }
            Gakuseki.setLastGakusekiDroppedAbroad(pi._gakusekiList, transferRecList, param);
            Gakuseki.setDroppedRecordInTengakuTaigakuYearEnabled(pi, pi._gakusekiList, param);
            pi._title = param._schoolDiv.isKoumokuGakunen(param, null, pi, student).booleanValue() ? "学年" : "年度";
            if (param._isOutputDebug) {
                log.info(" dropYears = " + pi.getDropYears(param) + ", enabledDropYears = " + pi.getEnabledDropYears(param));
            }
            pi._afterGraduatedCourseTextList = Student.loadAfterGraduatedCourse(db2, student._schregno, param, pi._gakusekiList);
            pi._afterGraduatedCourseSenkouKindSub = Student.loadAfterGraduatedCourseSenkouKindSub(db2, student._schregno, param);
            if (!param._is133m) {
                if (!pi._gakusekiList.isEmpty()) {
                    pi._abroadPrintDropRegdYears = Student.getAbroadPrintDropRegdYears(db2, param, student._schregno, pi._gakusekiList);
                }
            }
            GakushuBiko gakushuBiko = new GakushuBiko(param);
            pi._gakushuBiko = gakushuBiko;
            if (!param._is133m) {
                pi._isFuhakkou = isFuhakkou(pi._grdDate, param._seitoSidoYorokuHozonkikan);
            }
            pi._studyRecList = StudyRec.loadStudyRec(db2, student, pi, param, gakushuBiko, startYear);
            if (param._is133m) {
                pi._studyRecReplaceList = StudyRec.loadReplace(student, db2,  param);
            }
            GakushuBiko.createStudyRecBiko(db2, student._schregno, param, gakushuBiko);
            GakushuBiko.createStudyRecBikoSubstitution(param, gakushuBiko);
            if (param._is133m) {
                GakushuBiko.createStudyRecQualifiedBiko(db2, student._schregno, gakushuBiko, param);
                pi._studyRecBikoSubstitution90M = pi.createStudyRecBikoSubstitution90M(param, gakushuBiko);
            }
            if (!StringUtils.isBlank(param.property(Property.hyoteiYomikaeRadio))) {
                pi._gakunenSeiseki = GakunenSeiseki.load(db2, param, pi._schregno);
            }
            if (param._z010.in(Z010.aoyama)) {
                pi._aoyamaGakunenSeiseki = AoyamaGakunenSeiseki.load(db2, param, pi);
            }
            pi._addressList = Address.loadAddress(db2, student._schregno, param, Address.SQL_SCHREG, param._year, pi._entDate, pi._grdDate);
            pi._addressGrdHeader = "1".equals(param.property(Property.seitoSidoYorokuPrintTitleHogoshaTou)) ? "保護者等" : pi._isPrintGuarantor ? "保証人" : "保護者";
            pi._guardianAddressList = Address.loadAddress(db2, student._schregno, param, pi._isPrintGuarantor ? Address.SQL_GUARANTOR : Address.SQL_GUARDIAN, param._year, pi._entDate, pi._grdDate);
            pi._lastAnotherSchoolHistDat = getLastAnotherSchoolHistDat(db2, param, student._schregno);
            final boolean isPrintZaigakusubekiKikan = param.isPrintZaigakusubekiKikan(student, pi);
            if (param._isOutputDebugData) {
                log.info(" isPrintZaigakusubekiKikan = " + isPrintZaigakusubekiKikan);
            }
            if (isPrintZaigakusubekiKikan) {
                pi._zaigakusubekiKikan = getZaisekisubekiKikan(db2, param, student._schregno, pi);
                if (param._isOutputDebugData) {
                    log.info(" zaigakusubekiKikan = " + pi._zaigakusubekiKikan);
                }
            }

            final int realNameOrNotDiv;
            if (pi._isPrintNameAndRealName) {
                realNameOrNotDiv = 2;
            } else if (pi._isPrintRealName) {
                realNameOrNotDiv = 1;
            } else {
                realNameOrNotDiv = 0;
            }
            pi._schregBaseHistList = KnjDbUtils.query(db2, getHistSql("SCHREG_BASE_HIST_DAT", param.SCHOOL_KIND,  student._schregno, realNameOrNotDiv, param._year, pi._entDate, pi._grdDate));
            pi._guardianHistOrGuarantorHistList = KnjDbUtils.query(db2, getHistSql(pi._isPrintGuarantor ? "GUARANTOR_HIST_DAT" : "GUARDIAN_HIST_DAT", param.SCHOOL_KIND, student._schregno, 2, param._year, pi._entDate, pi._grdDate));
            if (param._isOutputDebugData) {
                log.info(" schregBaseHistList (size " + pi._schregBaseHistList.size() + ") = " + pi._schregBaseHistList);
                log.info(" guardianHistOrGuarantorHistList (size " + pi._guardianHistOrGuarantorHistList.size() + ") = " + pi._guardianHistOrGuarantorHistList);
            }
            if (param._isPrintHosoku && null != param._knja130_1) {
                pi._hosokuAddressIndexList = KNJA130_Hosoku.getHosokuIndex(param, "address", pi._addressList, Address.getPrintAddressIndex(param, "address", pi._addressList, param._knja130_1._addressMax));
                if (param._isOutputDebugData) {
                    log.info(" hosokuAddressIndexList (size " + pi._hosokuAddressIndexList.size() + " / " + pi._addressList.size() + ") = " + pi._hosokuAddressIndexList);
                }
                pi._hosokuGuardAddressIndexList = KNJA130_Hosoku.getHosokuIndex(param, "guradianAddress", pi._guardianAddressList, Address.getPrintAddressIndex(param, "guardianAddress", pi._guardianAddressList, param._knja130_1._addressMax));
                if (param._isOutputDebugData) {
                    log.info(" hosokuGuardAddressIndexList (size " + pi._hosokuGuardAddressIndexList.size() + " / " + pi._guardianAddressList.size() + ") = " + pi._hosokuGuardAddressIndexList);
                }
                pi._hosokuTransferRecIndexList = KNJA130_Hosoku.getHosokuIndex(param, "transfer", transferRecList, TransferRec.getPrintTransferRecIndexList(transferRecList, param));
                pi._hosokuYearStaffMap = getHosokuYearStaffMap(param, student, pi, StaffInfo.TR_DIV1, 3);
                pi._hosokuYearPrincipalMap = getHosokuYearPrincipalMap(pi, 3, param);
                if (param._isOutputDebugData) {
                    log.info(" hosokuTransferRecIndexList (size " + pi._hosokuTransferRecIndexList.size() + " / " + transferRecList.size() + ") = " + pi._hosokuTransferRecIndexList);
                    log.info(" hosokuYearStaffMap (size " + pi._hosokuYearStaffMap.size() + ") = " + pi._hosokuYearStaffMap);
                    log.info(" hosokuYearPrincipalMap (size " + pi._hosokuYearPrincipalMap.size() + ") = " + pi._hosokuYearPrincipalMap);
                }
            }
        }

        private static Map<String, List<Staff>> getHosokuYearStaffMap(final Param param, final Student student, final PersonalInfo pInfo, final String trDiv, final int n) {
            final Map<String, List<Staff>> yearStaffMap = new TreeMap<String, List<Staff>>();
            for (final Gakuseki gakuseki : pInfo._gakusekiList) {

                final List<Staff> staffHistList = param.getStudentStaffHistList(student, pInfo, trDiv, gakuseki._year);
                for (int i = 1; i < staffHistList.size(); i++) {
                    final Staff bef = staffHistList.get(i - 1);
                    final Staff stf = staffHistList.get(i);
                    if (bef._staffMst == stf._staffMst) {
                        staffHistList.set(i - 1, null);
                        staffHistList.set(i, new Staff(bef._year, bef._staffMst, bef._dateFrom, stf._dateTo, bef._stampNo));
                    }
                }
                for (final Iterator<Staff> it = staffHistList.iterator(); it.hasNext();) {
                    final Staff stf = it.next();
                    if (null == stf) {
                        it.remove();
                    }
                }
                if (staffHistList.size() >= n) { // 担任n回以上の履歴
                    getMappedList(yearStaffMap, gakuseki._year).addAll(staffHistList);
                }
            }
            return yearStaffMap;
        }

        /**
         * 指定回数以上の校長変更履歴の年度ごとのマップを得る
         * @param pInfo
         * @param n 指定回数
         * @return
         */
        private static Map<String, List<Map<String, String>>> getHosokuYearPrincipalMap(final PersonalInfo pInfo, final int n, final Param param) {
            final Map<String, List<Map<String, String>>> yearPrincipalMap = new TreeMap<String, List<Map<String, String>>>();
            final TreeSet<String> gakusekiYearSet = Gakuseki.gakusekiYearSet(pInfo.getGakusekiList());
            for (final String year : gakusekiYearSet) {
                final List<Map<String, String>> principalList = getMappedList(param._staffInfo._yearPrincipalListMap, year);
                if (principalList.size() >= n) { // 校長n回以上の履歴
                    yearPrincipalMap.put(year, principalList);
                }
            }
            return yearPrincipalMap;
        }

        private static void setBaseHistTenkaTensekiDate(final DB2UDB db2, final String schregno, final PersonalInfo pInfo, final String flg, final String comebackDate, final Param param) {
            if (null == pInfo._entDate) {
                return;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.ISSUEDATE, T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, GDAT.GRADE_CD, T1.EXPIREDATE, CM.COURSENAME, MM.MAJORNAME ");
            stb.append("  , T1.COURSECODE ");
            if (param._hasCOURSECODE_MST_COURSECODEABBV1) {
                stb.append("  , VALUE(CCM.COURSECODEABBV1, CCM.COURSECODENAME) AS COURSECODENAME ");
            } else {
                stb.append("  , CCM.COURSECODENAME ");
            }
            stb.append(" FROM SCHREG_BASE_HIST_DAT T1 ");
            stb.append(" LEFT JOIN COURSE_MST CM ON CM.COURSECD = T1.COURSECD ");
            stb.append(" LEFT JOIN MAJOR_MST MM ON MM.COURSECD = T1.COURSECD AND MM.MAJORCD = T1.MAJORCD ");
            stb.append(" LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = T1.COURSECODE ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + param._year + "' AND T1.GRADE = GDAT.GRADE ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO = '" + schregno + "' ");
            stb.append("   AND T1.ISSUEDATE >= '" + pInfo._entDate + "' ");
            if (null != comebackDate) {
                stb.append("   AND T1.ISSUEDATE <= '" + comebackDate + "' ");
            }
            if ("TENSEKI".equals(flg)) {
                // 課程が変更
                stb.append("   AND T1.COURSECD_FLG <> '0' ");
            } else if ("TENKA".equals(flg)) {
                // 学科が変更 (学科MAJOR_MSTのキーはCOURSECD、MAJORCDだけどCOURSECD_FLGをチェックに含めると転籍/転科が出力チェック順に依存するためMAJORCD_FLGだけチェックしておく)
                stb.append("   AND (T1.MAJORCD_FLG <> '0') ");
            }
            stb.append(" ORDER BY T1.ISSUEDATE ");

            if (param._isOutputDebugQuery) {
                log.info(" " + flg + " sql = " + stb.toString());
            }

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {

                if ("TENSEKI".equals(flg)) {
                    pInfo._tenseki = new SchregBaseHistDat(row);
                    log.info(" tenseki record = " + pInfo._tenseki._row);
                } else if ("TENKA".equals(flg)) {
                    pInfo._tenka = new SchregBaseHistDat(row);
                    log.info(" tenka record = " + pInfo._tenka._row);
                }
            }
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final boolean useStartYear) {
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
            stb.append("          AND T2.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
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

        /**
         * 証明書不発行か
         * @param grdDate 生徒の卒業日付
         * @param elapsedYears 発行を許可する卒業経過年数 (CERTIF_KIND_MST.ELAPSED_YEARS)
         * @return 不発行(卒業日付に経過年数を加算した日付をシステム日付が超える)ならtrue、それ以外はfalse
         */
        private static boolean isFuhakkou(final String grdDate, final int elapsedYears) {
            if (null == grdDate) {
                //log.debug(" grdDate = " + grdDate + ", elapsedYears = " + elapsedYears);
                return false;
            }

            final Calendar hakkoulimit = Calendar.getInstance();
            hakkoulimit.setTime(java.sql.Date.valueOf(grdDate));
            hakkoulimit.add(Calendar.YEAR, elapsedYears);

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
                final String sql = sqlSchGradeRec(param, useStartYear);

                if (param._isOutputDebugQuery) {
                    log.info(" " + psKey + " sql = " + sql);
                }

                param.setPs(psKey, db2, sql);
            }

            final StringBuffer attestSql = new StringBuffer();
            attestSql.append(" SELECT ");
            attestSql.append("    T1.YEAR, ");
            attestSql.append("    T1.CHAGE_OPI_SEQ, ");
            attestSql.append("    T1.LAST_OPI_SEQ, ");
            attestSql.append("    L1.FLG ");
            attestSql.append(" FROM ");
            attestSql.append("    ATTEST_OPINIONS_WK T1 ");
            attestSql.append("    LEFT JOIN ATTEST_OPINIONS_UNMATCH L1 ON L1.YEAR = T1.YEAR AND L1.SCHREGNO = T1.SCHREGNO ");
            attestSql.append(" WHERE ");
            attestSql.append("    T1.SCHREGNO = '" + student._schregno + "' ");
            final Map<String, Map<String, String>> yearAttestMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, attestSql.toString()), "YEAR");

            if (param._isOutputDebugQuery) {
                log.info(" " + psKey +" queryArg = " + ArrayUtils.toString(queryArg));
            }
            final Map<String, Gakuseki> gakusekiMap = new HashMap<String, Gakuseki>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), queryArg)) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final Map<String, String> attestMap = yearAttestMap.get(year);

                final Gakuseki gakuseki = new Gakuseki(db2, student, row, attestMap, pInfo, param);
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

        private static boolean getPrintGuarantor(final Param param, final String entdate, final String birthday) {
            boolean isPrintGuarantor = false;
            try {
                final BigDecimal diff = diffYear(birthday, entdate);
                final BigDecimal age = diff.setScale(1, BigDecimal.ROUND_DOWN);
                // 入学時の年齢が20歳以上なら保護者ではなく保証人を表示
                if (age.intValue() >= 20) {
                    isPrintGuarantor = true;
                }
                if (param._isOutputDebug) {
                    log.info(" student age = " + age + " [year]  isPrintGuarantor? " + isPrintGuarantor);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
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
            return null == _entDate ? 0 : getNendo(getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDiv || null == _grdDate ? 9999 : getNendo(getCalendarOfDate(_grdDate)));
        }

        /**
         * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
         * @param stb
         * @return
         */
        private static StringBuffer setFormatInsertBlank2(final StringBuffer stb) {
            int n = 0;
            for (int i = 0; i < stb.length(); i++) {
                final char ch = stb.charAt(i);
                if (Character.isDigit(ch)) {
                    n++;
                } else if (0 < n) {
                        if (1 == n) {
                            stb.insert( i - n, " " );
                            i++;
                        n = 0;
                    }
                }
            }
            return stb;
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            final String birthday;
            if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = setDateFormat2(KNJ_EditDate.h_format_JP(db2, date));
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

        public Address getStudentAddressMax() {
            return Util.last(_addressList, null);
        }

        private static String getZaisekisubekiKikan(final DB2UDB db2, final Param param, final String schregno, final PersonalInfo pi) {
            final String psKey = "PS_ZAIGAKUSUBEKI";
            if (null == param.getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.PERIOD_MONTH_CNT ");
                stb.append(" FROM ");
                stb.append("     ANOTHER_SCHOOL_HIST_DAT T1 ");
                stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = ?");
                stb.append("     AND FISCALYEAR(T1.REGD_S_DATE) <= '" + param._year + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.REGD_S_DATE DESC, ");
                stb.append("     T1.REGD_E_DATE DESC, ");
                stb.append("     T1.SEQ ");

                param.setPs(psKey, db2, stb.toString());
            }

            int zensekiPeriodMonthCnt = 0;
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno })) {

                if (NumberUtils.isDigits(KnjDbUtils.getString(row, "PERIOD_MONTH_CNT"))) {
                    zensekiPeriodMonthCnt += KnjDbUtils.getInt(row, "PERIOD_MONTH_CNT", new Integer(0)).intValue();
                }
            }
            final int max = Util.toInt(param.property(Property.seitoSidoYorokuZaisekiSubekiKikanMaxMonth), 36);

            if ("1".equals(param.property(Property.seitoSidoYorokuZaisekiSubekiKikanSubtractEntGrade))) {
                if (NumberUtils.isDigits(pi._entYearGrade)) {
                    final String psKeyG = "PS_ZAIGAKUSUBEKI_SUBTRACT_GRADE";
                    if (null == param.getPs(psKeyG)) {
                        final StringBuffer stb = new StringBuffer();
                        stb.append(" SELECT MIN(GRADE) AS GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = ? AND SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");

                        param.setPs(psKeyG, db2, stb.toString());
                    }
                    final String minGrade = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKeyG), new Object[] { pi._entYear } ));
                    int subtract = 0;
                    if (NumberUtils.isDigits(minGrade) && Integer.parseInt(pi._entYearGrade) >= Integer.parseInt(minGrade)) {
                        subtract = 12 * (Integer.parseInt(pi._entYearGrade) - Integer.parseInt(minGrade));
                    }
                    if (param._isOutputDebug) {
                        log.info(" zaisekisubekikikan subtract = " + subtract + " (entYearGrade = " +  pi._entYearGrade + ", minGrade = " + minGrade + ")");
                    }
                    zensekiPeriodMonthCnt += subtract;
                }
            }

            if (null == pi._entDate) {
                return "（在籍すべき期間 " + blankFormatDate(false, db2, param, param._formatDateDefaultYear) + "まで）";
            }

            final Calendar cEntDate = getCalendarOfDate(pi._entDate);
            cEntDate.set(Calendar.DAY_OF_MONTH, 1);
            cEntDate.add(Calendar.MONTH, max - zensekiPeriodMonthCnt);
            cEntDate.add(Calendar.DAY_OF_MONTH, -1);
            if (param._isOutputDebugData) {
                log.info(" zensekiPeriodMonthCnt = " + zensekiPeriodMonthCnt + ", max = " + max + ", entdate = " + pi._entDate);
            }
            final String y = String.valueOf(cEntDate.get(Calendar.YEAR));
            final String m = String.valueOf(1 + cEntDate.get(Calendar.MONTH));
            final String d = String.valueOf(cEntDate.get(Calendar.DAY_OF_MONTH));
            final String eDate =  y + "-" + m + "-" + d;
            return "（在籍すべき期間 " + formatDate(db2, eDate, param) + "まで）";
        }

        private static Map<String, String> getLastAnotherSchoolHistDat(final DB2UDB db2, final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.STUDENT_DIV ");
            stb.append("   , VALUE(NME026.NAME1, '') || VALUE(T1.MAJOR_NAME, '') AS COURSE_MAJOR ");
            stb.append("   , T1.FORMER_REG_SCHOOLCD, T2.FINSCHOOL_NAME AS ANOTHER_SCHOOL_NAME ");
            stb.append("   , T2.FINSCHOOL_ADDR1 AS ANOTHER_SCHOOL_ADDR1 ");
            stb.append("   , CASE WHEN INT(T2.FINSCHOOL_PREF_CD) > 47 THEN 1 END AS KAIGAI "); // 47都道府県以外は海外
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_HIST_DAT T1 ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ");
            stb.append(" LEFT JOIN NAME_MST NME026 ON NME026.NAMECD1 = 'E026' AND NME026.NAMECD2 = T1.STUDENT_DIV ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ?");
            stb.append(" ORDER BY ");
            stb.append("     T1.REGD_S_DATE DESC, ");
            stb.append("     T1.REGD_E_DATE DESC, ");
            stb.append("     T1.SEQ DESC ");

            final Map<String, String> rtn = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString(), new String[] { schregno }));
            return rtn;
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         * </pre>
         */
        private static Map<String, StudyRecSubclassTotal> createStudyRecTotalMap(final Param param, final PersonalInfo pInfo, final Map<String, List<StudyRec>> subclassStudyrecListMap) {
            if (param._isOutputDebugData) {
                log.info(" subclassTotal _dropYears = " + pInfo.getDropYears(param) + ", printDropRegdYears = " + pInfo._abroadPrintDropRegdYears);
            }
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = new TreeMap<String, StudyRecSubclassTotal>();
            for (final Map.Entry<String, List<StudyRec>> e : subclassStudyrecListMap.entrySet()) {
                final String key = e.getKey();
                final List<StudyRec> subclassStudyrecList = e.getValue();
                final StudyRecSubclassTotal subclassTotal = new StudyRecSubclassTotal(subclassStudyrecList, pInfo.getDropYears(param), pInfo.getEnabledDropYears(param), pInfo._abroadPrintDropRegdYears, param, pInfo);
                if (param._isOutputDebugData) {
                    log.info(" subclassTotal = " + subclassTotal);
                }
                studyRecSubclassMap.put(key, subclassTotal);
            }
            return studyRecSubclassMap;
        }

        private static Map<String, List<StudyRec>> getSubclassStudyrecListMap(final Param param, final boolean isFirst, final Student student, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final YOSHIKI yoshiki) {
            final List<StudyRec> studyRecList;
            if (param._is133m) {
                studyRecList = new ArrayList<StudyRec>();
                studyRecList.addAll(pInfo._studyRecList);
                studyRecList.addAll(pInfo.getStudyRecReplaceSateiAriList(param, yoshiki));
            } else {
                studyRecList = pInfo._studyRecList;
            }
            Collections.sort(studyRecList, new StudyRec.StudyrecComparator(param, yoshiki));
            final Collection<String> errorYearSet = new HashSet();
            final int minYear = Gakuseki.gakusekiMinYear(gakusekiList);
            final boolean isPrintAnotherStudyrec3 = param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_1
                                                 || param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_2 && param._schoolDiv.isTanniSei(null, pInfo, student);

            if (param._isOutputDebug) {
                if (yoshiki == YOSHIKI._2_OMOTE) {
                    log.info(" isPrintAnotherStudyrec3 = " + isPrintAnotherStudyrec3);
                }
            }
            final Collection<String> notPrintYearSet = Gakuseki.notPrintYearSet(gakusekiList, param);
            if (param._is133m) {
                if (param._isOutputDebug) {
                    log.info(" not print year set = " + notPrintYearSet);
                }
            }
            final Collection<String> yearSet = Gakuseki.gakusekiYearSet(gakusekiList);
            if (!param._is133m) {
                if (param._isOutputDebug) {
                    log.info(" years (yoshiki = " + yoshiki + ") = " + yearSet);
                }
            }
            boolean hasNotPrintEntGrdComebackYear = false;

            final List<StudyRec> studyRecList2 = new ArrayList<StudyRec>();
            for (final StudyRec studyrec : studyRecList) {
                int notPrint = 0;
                if (param._is133m) {
                    if (!SCHOOLCD1.equals(studyrec._schoolcd) && notPrintYearSet.contains(studyrec._year)) {
                        notPrint = 1;
                    } else if (yoshiki == YOSHIKI._2_OMOTE) {
                        if (param._z010.in(Z010.kyoto) && SCHOOLCD1.equals(studyrec._schoolcd)) {
                            notPrint = 2;
                        } else if (param._z010.in(Z010.sagaken) && !isPrintAnotherStudyrec3 && Integer.parseInt(studyrec._year) < minYear) {
                            notPrint = 3;
                        }
                    }
                } else {
                    if (student._notPrintEntGrdComebackYear.contains(studyrec._year)) {
                        hasNotPrintEntGrdComebackYear = true;
                        continue;
                    }
                    if ((yoshiki == YOSHIKI._1_URA && param._isPrintAnotherStudyrec2 && isFirst && NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) < minYear) ||
                        (yoshiki == YOSHIKI._2_OMOTE && isPrintAnotherStudyrec3 && isFirst && NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) < minYear)) {
                        // 前籍校も出力する
                    } else {
                        if (!yearSet.contains(studyrec._year)) {
                            errorYearSet.add(studyrec._year);
                            continue;
                        }
                    }
                    if (StudyRec.FLAG_SUBSTITUTION.equals(studyrec._studyFlag)) {
                        boolean notTarget = false;
                        if (yoshiki == YOSHIKI._1_URA && param.isNotPrintDaitai("1ura")) {
                            notTarget = true;
                        } else if (yoshiki == YOSHIKI._2_OMOTE && param.isNotPrintDaitai("2omote")) {
                            notTarget = true;
                        }
                        if (notTarget) {
                            continue;
                        }
                    }
                    if (NumberUtils.isDigits(studyrec._year) && -1 != pInfo._seisekiEndYear && Integer.parseInt(studyrec._year) > pInfo._seisekiEndYear) {
                        continue;
                    }
                }
                if (notPrint > 0) {
                    if (param._isOutputDebug) {
                        log.info(" notPrint : " + notPrint + " = " + studyrec);
                    }
                    continue;
                }
                studyRecList2.add(studyrec);
            }
            if (hasNotPrintEntGrdComebackYear) {
                log.warn(" 対象外の成績年度 (" + student._notPrintEntGrdComebackYear + ")");
            }
            if (!errorYearSet.isEmpty()) {
                log.warn(" 学籍がない成績の年度 = " + errorYearSet);
            }
            if (param._isOutputDebug) {
                log.info(" studyRecList2 size = " + studyRecList2.size());
            }
            final Map<String, List<StudyRec>> subclassStudyrecListMap = new TreeMap<String, List<StudyRec>>();
            for (final StudyRec studyrec : studyRecList2) {
                String key;
                if (param._is133m) {
                    key = getSubclasscdM(studyrec.subclassMst(param, yoshiki), param);
                } else {
                    key = studyrec.getKeySubclasscdForSubclassTotal(param);
                    if (null != key && key.length() > 2 && _90.equals(key.substring(0, 2))) {
                        key = _90;
                    }
                }
                getMappedList(subclassStudyrecListMap, key).add(studyrec);
            }
            if (param._isOutputDebugSeiseki) {
                log.info(" studyrec list map : isFirst = " + isFirst + ", yoshiki = " + yoshiki);
                for (final String key : subclassStudyrecListMap.keySet()) {
                    final List<StudyRec> strs = subclassStudyrecListMap.get(key);
                    log.info(" subclass " + key + " = " + strs);
                }
            }
            return subclassStudyrecListMap;
        }

        public boolean isTargetYear(final String debug, final String year, final Param param) {
            boolean isTargetYear = false;
            if (!NumberUtils.isDigits(year)) {
                return isTargetYear;
            } else {
                final int iyear = Integer.parseInt(year);
                final int yearBegin = getYearBegin();
                if ((isAnotherSchoolYear(year) || iyear < yearBegin) && _isFirst || yearBegin <= iyear && iyear <= getYearEnd(param)) {
                    isTargetYear = true;
                }
            }
            if (param._isOutputDebugGakuseki) {
                final int yearBegin = getYearBegin();
                log.info(defstr(debug) + " isTargetYear = " + isTargetYear + " : year = " + year + ", isFirst = " + _isFirst + ", yearBegin = " + yearBegin + ", yearEnd = " + getYearEnd(param) + ", grdDate = " + _grdDate);
                if (!isTargetYear) {
                }
            }
            return isTargetYear;
        }

        /**
         * 成績等を表示するか
         * @param year 年度
         * @param param
         * @return
         */
        public boolean isTargetYearLast(final String year, final Student student, final Param param, final boolean checkFirst) {
            if (!NumberUtils.isDigits(year)) {
                return false;
            } else {
                final int iYear = Integer.parseInt(year);
                final int yearBegin = getYearBegin();
                if ((isAnotherSchoolYear(year) || iYear < yearBegin) && (!checkFirst || checkFirst && _isFirst) || yearBegin <= iYear && iYear <= getPersonalInfoYearEnd(student, param)) {
                    return true;
                }
            }
            if (param._isOutputDebugGakuseki) {
                log.info(" isTargetYearLast = false: year = " + year + ", yearBegin = " + getYearBegin() + ", isFirst = " + _isFirst + ", yearEnd = " + getPersonalInfoYearEnd(student, param));
            }
            return false;
        }

        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final Student student, final Param param) {
            final TreeSet<Integer> yearSetAll = new TreeSet<Integer>();
            final List<PersonalInfo> pInfoList = student._printEntGrdHistList;
            for (final ListIterator<PersonalInfo> it = pInfoList.listIterator(pInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final PersonalInfo pInfo = it.previous();
                final int begin = pInfo.getYearBegin();
                final int end = pInfo.getYearEnd(param);
                final TreeSet<Integer> yearSet = new TreeSet<Integer>();
                for (int y = begin; y <= end; y++) {
                    final Integer year = new Integer(y);
                    if (yearSetAll.contains(year)) {
                        // 新しい生徒情報で表示されるものは含まない
                    } else {
                        yearSetAll.add(year);
                        yearSet.add(year);
                    }
                }
                if (this == pInfo) {
                    if (yearSet.isEmpty()) {
                        return -1; // 対象の生徒情報は成績等は表示しない
                    }
                    return yearSet.last().intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }

        /**
         * 指定年度の教科コード90の代替科目備考を得る。
         * @param yearKey 年度のキー
         * @return 備考の配列。なければnullを返す
         */
        private List<String> getArraySubstitutionBiko90(final String yearKey, final Param param) {
            final List<String> list = new ArrayList();
            final List<String> substZenbu = _gakushuBiko.getYearStudyRecBikoSubstitution90ListMap(GakushuBiko.DAITAI_TYPE.ZENBU, _gakusekiList, _keyAll, param).get(yearKey);
            if (null != substZenbu) {
                list.addAll(substZenbu);
            }
            final List<String> substIchibu = _gakushuBiko.getYearStudyRecBikoSubstitution90ListMap(GakushuBiko.DAITAI_TYPE.ICHIBU, _gakusekiList, _keyAll, param).get(yearKey);
            if (null != substIchibu) {
                if (param._z010.in(Z010.naraken)) {
                    log.info("奈良県は総合的な学習の時間の一部代替は自動表示不要 : " + substIchibu);
                } else {
                    list.addAll(substIchibu);
                }
            }
            return list;
        }

        // KNJA133M

        /**
         * 指定年度の教科コード90の代替科目備考を得る。
         * @param key 年度のキー
         * @return 備考の配列。なければnullを返す
         */
        private List<String> getArraySubstitutionBiko90M(final String key) {
            final List<String> list = _studyRecBikoSubstitution90M.get(key);
            if (list != null) {
                return list;
            }
            return Collections.emptyList();
        }

        public Map<String, StudyRecYearTotalM> getStudyRecYearM(final Param param, final YOSHIKI yoshiki) {
            final Collection<String> chkDropYears = new HashSet(getDropYears(param));
            final Collection<String> chkDropShowYears = new HashSet<String>();
//            if (param._z010.in(Z010.kyoto) && (null != _trrecTengaku || null != _trrecTaigaku) && !chkDropYears.isEmpty()) {
//                final String tengakubi = null == _trrecTengaku ? null : _trrecTengaku._sDate;
//                final String taigakubi = null == _trrecTaigaku ? null : _trrecTaigaku._sDate;
//                final String grdYear = nendo(null != tengakubi ? tengakubi : taigakubi);
//                if (null != grdYear) {
//                    // 「原級留置した場合、留年時の成績は出力されない」が、
//                    // 再履修の成績データを作成する前に転退学した場合は、留年時の成績を出す。
//                    // （原級留置の年次で改ページはする）
//                    for (final Iterator it = groupByGrade(_gakusekiList).values().iterator(); it.hasNext();) {
//                        final List gakuList = (List) it.next();
//                        if (gakuList.size() <= 1) { // 留年ではない
//                            continue;
//                        }
//                        final Gakuseki newGaku = (Gakuseki) gakuList.get(0); // 再履修の学籍
//                        if (grdYear.equals(newGaku._year)){
//                            final Gakuseki oldGaku = (Gakuseki) gakuList.get(1); // 留年時の最新の学籍
//
//                            boolean hasNewGakuYearStudyrec = false;
//                            for (final Iterator its = _studyRecList.iterator(); its.hasNext();) {
//                                final StudyRec studyRec = (StudyRec) its.next();
//                                if (null != studyRec._year && studyRec._year.equals(newGaku._year)) {
//                                    hasNewGakuYearStudyrec = true;
//                                }
//                            }
//                            if (!hasNewGakuYearStudyrec) {
//                                // 再履修の年度の成績がない場合、留年時の年度を表示非対象年度リストから除く
//                                chkDropShowYears.add(oldGaku._year);
//                                log.fatal("再履修年度の成績データが0件.留年時の成績を表示対象とする. year = " + oldGaku._year);
//                            }
//                        }
//                    }
//                }
//            }
            final Set<String> notPrintYearSet = Gakuseki.notPrintYearSet(_gakusekiList, param);
            final List<StudyRec> list = new ArrayList<StudyRec>();
            list.addAll(_studyRecList);
            list.addAll(_studyRecReplaceList);
            return createStudyRecYearM(param, list, chkDropYears, chkDropShowYears, notPrintYearSet, yoshiki);
        }

        /**
         * 総合的な学習の時間の代替科目の学習記録備考を作成し、マップに加えます。
         */
        private Map<String, List<String>> createStudyRecBikoSubstitution90M(final Param param, final GakushuBiko gakushuBiko) {
            final StringTemplate template = param._substRemark90Template;

            final Map<String, List<String>> yearBikoListMap = new HashMap<String, List<String>>();

            for (final Map.Entry<SubclassMst, StudyRecSubstitution> e : gakushuBiko.getInputSubclassStudyrecSubstitutionMap(GakushuBiko.DAITAI_TYPE.NO_TYPE_FLG).entrySet()) {

                final SubclassMst substitutionSubclass = e.getKey();
                if (!_90.equals(substitutionSubclass._classcd)) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = e.getValue();

                for (final Gakuseki gakuseki : _gakusekiList) {
                    if (null == gakuseki._year) {
                        continue;
                    }
                    final Map<String, String> bikoSubstitution90TemplateDataMap = studyRecSubstitution.getBikoSubstitution90TemplateDataMap(gakuseki._year, param);
                    if (param._isOutputDebug) {
                        log.info(" year = " + gakuseki._year + ", bikoSubstitution90TemplateDataMap = " + bikoSubstitution90TemplateDataMap);
                    }
                    getMappedList(yearBikoListMap, gakuseki._year).add(template.format(bikoSubstitution90TemplateDataMap));
                }

                final Map<String, String> bikoSubstitution90TemplateDataMap = studyRecSubstitution.getBikoSubstitution90TemplateDataMap(null, param);
                if (param._isOutputDebug) {
                    log.info(" year = " + null + ", bikoSubstitution90TemplateDataMap = " + bikoSubstitution90TemplateDataMap);
                }
                getMappedList(yearBikoListMap, _keyAll).add(template.format(bikoSubstitution90TemplateDataMap));
            }
            return yearBikoListMap;
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・科目別修得単位数計 Student._studyRecSubclass。
         * <br />
         *  ・年度別修得単位数計 Student._studyRecYear。
         * </pre>
         */
        private static Map<String, StudyRecYearTotalM> createStudyRecYearM(final Param param, final List<StudyRec> studyRecList, final Collection<String> dropYears, final Collection<String> dropShowYears, final Collection<String> notPrintYearSet, final YOSHIKI yoshiki) {
            final Map<String, StudyRecYearTotalM> studyRecYearMap = new HashMap<String, StudyRecYearTotalM>();
            for (final StudyRec studyrec : studyRecList) {
                if (param._z010.in(Z010.miyagiken) && studyrec.isMishutoku(param)) {
                    // 宮城県は単位未修得の場合表示しない
                    continue;
                }
                if (param._useStudyrecReplaceDat && yoshiki ==YOSHIKI._2_OMOTE && SCHOOLCD1.equals(studyrec._schoolcd)) {
                    final String key = StudyRec.SATEI;
                    if (!studyRecYearMap.containsKey(key)) {
//                        final byte isDrop = StudyRecYearTotalM.GET;
//                        studyRecYear.put(key, new StudyRecYearTotalM(key, isDrop));
                        studyRecYearMap.put(key, new StudyRecYearTotalM(param, key));
                    }
                    final StudyRecYearTotalM sateitotal = (StudyRecYearTotalM) studyRecYearMap.get(key);
                    if (studyrec.sateiNasi(param, yoshiki)) {
                        sateitotal.list(StudyRec.TotalM.KATEIGAI).add(studyrec);
                    } else {
                        sateitotal.list(StudyRec.TotalM.TOTAL).add(studyrec);

                        if (_ABROAD.equals(studyrec._classMst._classname)) {
                            sateitotal.list(StudyRec.TotalM.ABROAD).add(studyrec);
                        } else {
                            sateitotal.list(StudyRec.TotalM.SUBJECT).add(studyrec);
                            if (_90.equals(studyrec._classMst._classcd)) {
                                sateitotal.list(StudyRec.TotalM.SUBJECT90).add(studyrec);
                            }
                        }
                    }
                    continue;
                }

                if (!SCHOOLCD1.equals(studyrec._schoolcd) && notPrintYearSet.contains(studyrec._year)) {
                    continue;
                }
                if (null != studyrec._credit) {
                    final String key = studyrec._year;
                    if (!studyRecYearMap.containsKey(key)) {
//                        final byte isDrop;
//                        if (dropShowYears.contains(studyrec._year)) {
//                            isDrop = StudyRecYearTotal.DROP_SHOW;
//                        } else if (dropYears.contains(studyrec._year)) {
//                            isDrop = StudyRecYearTotal.DROP;
//                        } else {
//                            isDrop = StudyRecYearTotalM.GET;
//                        }
                        studyRecYearMap.put(key, new StudyRecYearTotalM(param, key));
                    }
                    final StudyRecYearTotalM yeartotal = studyRecYearMap.get(key);

                    yeartotal.list(StudyRec.TotalM.TOTAL).add(studyrec);

                    if (_ABROAD.equals(studyrec._classMst._classname)) {
                        yeartotal.list(StudyRec.TotalM.ABROAD).add(studyrec);
                    } else {
                        yeartotal.list(StudyRec.TotalM.SUBJECT).add(studyrec);

                        if (_90.equals(studyrec._classMst._classcd)) {
                            yeartotal.list(StudyRec.TotalM.SUBJECT90).add(studyrec);
                        }
                    }

                    if (param._z010.in(Z010.sagaken)) {
                        if (SCHOOLCD1.equals(studyrec._schoolcd)) {
                            yeartotal.list(StudyRec.TotalM.TOTAL_SAGA_A).add(studyrec);

                            if (_ABROAD.equals(studyrec._classMst._classname)) {
                            } else {
                                yeartotal.list(StudyRec.TotalM.SUBJECT_SAGA_A).add(studyrec);

                                if (_90.equals(studyrec._classMst._classcd)) {
                                    yeartotal.list(StudyRec.TotalM.SUBJECT90_SAGA_A).add(studyrec);
                                }
                            }
                        } else {
                            yeartotal.list(StudyRec.TotalM.TOTAL_SAGA_B).add(studyrec);

                            if (_ABROAD.equals(studyrec._classMst._classname)) {
                            } else {
                                yeartotal.list(StudyRec.TotalM.SUBJECT_SAGA_B).add(studyrec);

                                if (_90.equals(studyrec._classMst._classcd)) {
                                    yeartotal.list(StudyRec.TotalM.SUBJECT90_SAGA_B).add(studyrec);
                                }
                            }
                        }
                    }
                }
            }
            return studyRecYearMap;
        }

        private List getStudyRecReplaceSateiAriList(final Param param, final YOSHIKI yoshiki) {
            final List<StudyRec> list = new ArrayList<StudyRec>();
            list.addAll(_studyRecReplaceList);
            if (param._useStudyrecReplaceDat) {
                for (final Iterator<StudyRec> it = list.iterator(); it.hasNext();) {
                    final StudyRec sr = it.next();
                    if (sr.sateiNasi(param, yoshiki)) {
                        it.remove();
                    }
                }
            }
            return list;
        }

        private List<StudyRec> getStudyRecReplaceSateiNasiList(final Param param, final YOSHIKI yoshiki) {
            final List<StudyRec> list = new ArrayList<StudyRec>();
            if (param._useStudyrecReplaceDat) {
                for (final StudyRec sr : _studyRecReplaceList) {
                    if (sr.sateiNasi(param, yoshiki)) {
                        list.add(sr);
                    }
                }
            }
            return list;
        }

        public String getSogoSubclassname(final Param param, final TreeMap<String, Gakuseki> yearGakusekiMap0) {
            final int tankyuStartYear = Util.toInt(param.property(Property.sogoTankyuStartYear), 2019);
            final TreeMap<String, Gakuseki> yearGakusekiMap = new TreeMap<String, Gakuseki>(yearGakusekiMap0);
            for (final Iterator<Map.Entry<String, Gakuseki>> it = yearGakusekiMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, Gakuseki> e = it.next();
                if ("0".equals(e.getKey())) {
                    it.remove();
                } else if (!Gakuseki.GAKUSEKI_DATA_FLG1.equals(e.getValue()._dataflg)) {
                    it.remove();
                }
            }
            boolean isTankyu = false;
            String minYear = null;
            Gakuseki minYearGakuseki = null;
            if (NumberUtils.isDigits(_curriculumYear)) {
                isTankyu = Integer.parseInt(_curriculumYear) >= tankyuStartYear;
            } else {
                if (!yearGakusekiMap.isEmpty()) {
                    minYear = yearGakusekiMap.firstKey();
                    minYearGakuseki = yearGakusekiMap.get(minYear);
                }
                if (null != minYearGakuseki) {
                    final int year = NumberUtils.isDigits(minYearGakuseki._year) ? Integer.parseInt(minYearGakuseki._year) : 9999;
                    final int gradeCdInt = NumberUtils.isDigits(minYearGakuseki._gdat._gradeCd) ? Integer.parseInt(minYearGakuseki._gdat._gradeCd) : 99;
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
                param.logOnce(_schregno + " 探究? " + isTankyu + ", startYear = " + minYear + ", minYearGakuseki = " + minYearGakuseki + ", curriculumYear = " + _curriculumYear);
            }
            return isTankyu ? SOGOTEKI_NA_TANKYU_NO_JIKAN : SOGOTEKI_NA_GAKUSHU_NO_JIKAN;
        }

        /**
         * 島根県で専門学科用フォームを使用するか
         * @param param
         * @return 島根県で専門学科用フォームを使用するなら<code>true</code>
         */
        public boolean useSeitoSidoYorokuSenmonGakkaForm(final Param param) {
            return param._a055SenmonGakkaMajorKeyList.contains(_student._printRegdCoursecd + "-" + _student._printRegdMajorcd);
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    private static class AttendRec implements Comparable<AttendRec> {

        final String _year;
        String _annual;
        String _attend_1; // 授業日数
        String _suspend; //出停
        String _mourning; // 忌引
        String _abroad; // 留学
        String _requirepresent; // 要出席
        String _attend_6; // 欠席
        String _present; // 出席
        String _late;
        String _early;

        Integer _executedateCount; // 出校日数
        BigDecimal _creditTime93; //
        BigDecimal _creditTime94; //
        BigDecimal _total; //

        /**
         * コンストラクタ。
         */
        private AttendRec(final String year) {
            _year = year;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final AttendRec that) {
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        /**
         * 出欠記録クラスを作成し、マップに加えます。
         * @param db2
         */
        private static Map<String, AttendRec> loadAttendRec(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Param param) {
            final Map<String, AttendRec> attendRecMap = new HashMap<String, AttendRec>();
            if (param._is133m) {
                if (param._z010.in(Z010.miyagiken) || param._z010.in(Z010.sagaken)) {
                    for (final Gakuseki gakuseki : pInfo._gakusekiList) {
                        final Map<String, String> map = MiyagiAttendance.getMiyagiAttendanceMap(db2, param, gakuseki._year, student._schregno);
                        // log.debug(" map = " + map);
                        final Integer executedateCount = null == map.get("sumShukkou") ? null : Integer.valueOf(map.get("sumShukkou"));
                        final BigDecimal sumHr = null == map.get("sumHr") ? null : new BigDecimal(map.get("sumHr"));
                        final BigDecimal sumGyouji = null == map.get("sumGyouji") ? null : new BigDecimal(map.get("sumGyouji"));
                        final BigDecimal sumTokkatsu = null == map.get("sumTokkatsu") ? null : new BigDecimal(map.get("sumTokkatsu"));
                        final AttendRec attendrec = new AttendRec(gakuseki._year);

                        attendrec._executedateCount = executedateCount;
                        attendrec._creditTime93 = sumHr;
                        attendrec._creditTime94 = sumGyouji;
                        attendrec._total = sumTokkatsu;
                        attendRecMap.put(gakuseki._year, attendrec);
                        if (param._isOutputDebugData) {
                            log.info(" miyagi attendance schregno = " + student._schregno + ", attendrec = " + map);
                        }
                    }
                } else if (param._z010.in(Z010.kyoto) || "1".equals(param.property(Property.HR_ATTEND_DAT_NotSansyou))) {
                    final String psKey = "PS_SCHREG_ATTEND_DAT";
                    if (null == param.getPs(psKey)) {

                        final String sql = sqlAttendRec(param);

                        param.setPs(psKey, db2, sql);
                    }

                    for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno, student._schregno})) {
                        final String year = KnjDbUtils.getString(row, "YEAR");
                        final AttendRec attendrec = new AttendRec(year);
                        final String present = KnjDbUtils.getString(row, "PRESENT");
                        attendrec._executedateCount = NumberUtils.isDigits(present) ? Integer.valueOf(present) : null;

                        attendRecMap.put(year, attendrec);
                    }

                } else {

                    final String psKey = "PS_HR_ATTEND_DAT";
                    if (null == param.getPs(psKey)) {

                        final StringBuffer stb = new StringBuffer();
                        stb.append(" WITH YEAR_DATE AS ( ");
                        stb.append("   SELECT DISTINCT ");
                        stb.append("       S1.YEAR, S1.SCHREGNO, S1.EXECUTEDATE ");
                        stb.append("   FROM ");
                        stb.append("       HR_ATTEND_DAT S1 ");
                        stb.append("   WHERE ");
                        stb.append("       S1.YEAR <= '" + param._year + "' ");
                        stb.append("       AND S1.SCHREGNO = ? ");
                        stb.append("    ) ");
                        stb.append(" SELECT ");
                        stb.append("     T1.YEAR, ");
                        stb.append("     COUNT(L1.EXECUTEDATE) AS EXECUTEDATE_COUNT ");
                        stb.append(" FROM ");
                        stb.append("     (SELECT DISTINCT YEAR FROM YEAR_DATE) T1 ");
                        stb.append(" LEFT JOIN YEAR_DATE L1 ON L1.YEAR = T1.YEAR ");
                        stb.append(" GROUP BY ");
                        stb.append("     T1.YEAR ");

                        final String sql = stb.toString();

                        param.setPs(psKey, db2, sql);
                    }

                    for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno})) {

                        final String year = KnjDbUtils.getString(row, "YEAR");
                        final Integer executedateCount = KnjDbUtils.getInt(row, "EXECUTEDATE_COUNT", null);
                        final AttendRec attendrec = new AttendRec(year);
                        attendrec._executedateCount = executedateCount;
                        attendRecMap.put(year, attendrec);
                    }

                    //log.debug(" schregno = " + student._schregno + ", attendRecMap = " + attendRecMap);
                }
            } else {

                final String psKey = "PS_SCHREG_ATTEND_DAT";
                if (null == param.getPs(psKey)) {

                    final String sql = sqlAttendRec(param);

                    param.setPs(psKey, db2, sql);
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno, student._schregno})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final AttendRec attendrec = new AttendRec(year);
                    attendrec._annual = KnjDbUtils.getString(row, "ANNUAL");
                    attendrec._attend_1 = KnjDbUtils.getString(row, "ATTEND_1");
                    attendrec._suspend = KnjDbUtils.getString(row, "SUSPEND");
                    attendrec._mourning = KnjDbUtils.getString(row, "MOURNING");
                    attendrec._abroad = KnjDbUtils.getString(row, "ABROAD");
                    attendrec._requirepresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
                    attendrec._attend_6 = KnjDbUtils.getString(row, "ATTEND_6");
                    attendrec._present = KnjDbUtils.getString(row, "PRESENT");
                    attendrec._late = KnjDbUtils.getString(row, "LATE");
                    attendrec._early = KnjDbUtils.getString(row, "EARLY");

                    attendRecMap.put(year, attendrec);
                }
            }
            return attendRecMap;
        }

        /**
         * @return 出欠の記録のＳＱＬ文を戻します。
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String sqlAttendRec(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SEMES AS ( ");
            stb.append("    SELECT ");
            stb.append("        YEAR, SCHREGNO, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append("    FROM ");
            stb.append("        ATTEND_SEMES_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("   GROUP BY ");
            stb.append("        YEAR, SCHREGNO ");
            stb.append(" )");

            stb.append(" SELECT  T1.YEAR, ANNUAL");
            stb.append("       , VALUE(CLASSDAYS,0) AS CLASSDAYS"); // 授業日数
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._isDefinecodeSchoolMarkK) {
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
            if (param._z010.in(Z010.chiyodaKudan)) {
                stb.append("       , SEMES.LATE AS LATE"); // 遅刻日数:7
                stb.append("       , SEMES.EARLY AS EARLY"); // 早退日数:7
            } else {
                stb.append("       , VALUE(SEMES.LATE,0) AS LATE"); // 遅刻日数:7
                stb.append("       , VALUE(SEMES.EARLY,0) AS EARLY"); // 早退日数:7
            }
            stb.append(" FROM (");
            stb.append("      SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL");
            stb.append("            , SUM(T1.CLASSDAYS) AS CLASSDAYS");
            stb.append("            , SUM(T1.OFFDAYS) AS OFFDAYS");
            stb.append("            , SUM(T1.ABSENT) AS ABSENT");
            stb.append("            , SUM(T1.SUSPEND) AS SUSPEND");
            stb.append("            , SUM(T1.MOURNING) AS MOURNING");
            stb.append("            , SUM(T1.ABROAD) AS ABROAD");
            stb.append("            , SUM(T1.REQUIREPRESENT) AS REQUIREPRESENT");
            stb.append("            , SUM(T1.SICK) AS SICK");
            stb.append("            , SUM(T1.ACCIDENTNOTICE) AS ACCIDENTNOTICE");
            stb.append("            , SUM(T1.NOACCIDENTNOTICE) AS NOACCIDENTNOTICE");
            stb.append("            , SUM(T1.PRESENT) AS PRESENT");
            stb.append("       FROM   SCHREG_ATTENDREC_DAT T1");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.ANNUAL ");
            stb.append("       WHERE  SCHREGNO = ? AND T1.YEAR <= '" + param._year + "' ");
            stb.append("          AND VALUE(T2.SCHOOL_KIND, '" + param.SCHOOL_KIND + "') = '" + param.SCHOOL_KIND + "' ");
            if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherAttendrec))) {
                stb.append("      AND SCHOOLCD <> '1' ");
            }
            stb.append("       GROUP BY T1.SCHREGNO, T1.YEAR, T1.ANNUAL");
            stb.append("     ) T1 ");
            stb.append("     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("      AND S1.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
            }
            stb.append("     LEFT JOIN SEMES ON SEMES.YEAR = T1.YEAR AND SEMES.SCHREGNO = T1.SCHREGNO ");
            return stb.toString();
        }

        private static class MiyagiAttendance {
            final List<AttendInfo> _schAttendDatList = new ArrayList<AttendInfo>();
            final List<AttendInfo> _specialactAttendDatList = new ArrayList<AttendInfo>();
            final List<AttendInfo> _testAttendDatList = new ArrayList<AttendInfo>();

            public String getTokkatsuCount(final Param param, final String semester, final String sdate, final String edate) {
                String rtn = null;
                if (param._z010.in(Z010.sagaken)) {
                    // 特別活動
                    for (final AttendInfo m : _specialactAttendDatList) {
                        if ((null == semester || semester.equals(m._semester)) && null != m._creditTime && !"1".equals(m._m026namespare2) && null != m._m027name1 &&
                                between(true, m._executedate, sdate, edate)) {
                            rtn = add(rtn, m._creditTime);
                        }
                    }
                } else {
                    rtn = add(getHrCount(param, semester, sdate, edate), getGyoujiCount(param, semester, sdate, edate));
                }
                return rtn;
            }

            public String getGyoujiCount(final Param param, final String semester, final String sdate, final String edate) {
                return getSpecialactCount(param, "94", semester, sdate, edate);
            }

            public String getHrCount(final Param param, final String semester, final String sdate, final String edate) {
                return getSpecialactCount(param, "93", semester, sdate, edate);
            }

            public String getShukkouCount(final Param param, final String semester, final String sdate, final String edate) {
                final Set set = new TreeSet();
                if (param._z010.in(Z010.sagaken)) {
                    // スクーリング
                    for (final AttendInfo m : _schAttendDatList) {
                        // スクーリング種別 = '2' (放送)は出校から除く
                        if ((null == semester || semester.equals(m._semester)) && null != m._executedate && "1".equals(m._namespare1)
                                && between(true, m._executedate, sdate, edate) && !"1".equals(m._m026namespare1)) {
                            set.add(m._executedate);
                        }
                    }
                    // 特別活動
                    for (final AttendInfo m : _specialactAttendDatList) {
                        if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate) && !"1".equals(m._m026namespare1) && null != m._m027name1) {
                            set.add(m._executedate);
                        }
                    }
                    // テスト
                    for (final AttendInfo m : _testAttendDatList) {
                        if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate) && !"1".equals(m._m026namespare1)) {
                            set.add(m._executedate);
                        }
                    }
                } else {
                    // スクーリング
                    for (final AttendInfo m : _schAttendDatList) {
                        // スクーリング種別 = '2' (放送)は出校から除く
                        if ((null == semester || semester.equals(m._semester)) && null != m._executedate && "1".equals(m._namespare1)
                                && between(true, m._executedate, sdate, edate)) {
                            set.add(m._executedate);
                        }
                    }
                    // 特別活動
                    for (final AttendInfo m : _specialactAttendDatList) {
                        if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate)) {
                            set.add(m._executedate);
                        }
                    }
                    // テスト
                    for (final AttendInfo m : _testAttendDatList) {
                        if ((null == semester || semester.equals(m._semester)) && between(true, m._executedate, sdate, edate)) {
                            set.add(m._executedate);
                        }
                    }
                }
                return set.isEmpty() ? null : String.valueOf(set.size());
            }

            public String getSpecialactCount(final Param param, final String classcd, final String semester, final String sdate, final String edate) {
                String sum = null;
                // 特別活動
                for (final AttendInfo m : _specialactAttendDatList) {
                    if ((null == semester || semester.equals(m._semester)) && null != m._creditTime && classcd.equals(m._classcd) &&
                            between(true, m._executedate, sdate, edate)) {
                        sum = add(sum, m._creditTime);
                    }
                }
                return sum;
            }

            private static MiyagiAttendance getHrclassList(final DB2UDB db2, final Param param, final String year, final String schregno) {
                MiyagiAttendance ail = new MiyagiAttendance();

                final String psKey = "PS_KEY_MIYAGI_ATTENDREC";
                if (null == param.getPs(psKey)) {
                    final String sql = sql();
                    if (param._isOutputDebugQuery) {
                        log.info(" miyagi attendance sql = " + sql);
                    }
                    param.setPs(psKey, db2, sql);
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno, year})) {
                    final AttendInfo attend = new AttendInfo(
                            KnjDbUtils.getString(row, "EXECUTEDATE"),
                            KnjDbUtils.getString(row, "SEMESTER"),
//                            KnjDbUtils.getString(row, "PERIODF"),
//                            KnjDbUtils.getString(row, "PERIODT"),
                            KnjDbUtils.getString(row, "CLASSCD"),
//                            KnjDbUtils.getString(row, "CHAIRCD"),
                            KnjDbUtils.getString(row, "CREDIT_TIME"),
//                            KnjDbUtils.getString(row, "SCHOOLINGKINDCD"),
                            KnjDbUtils.getString(row, "NAMESPARE1"),
                            KnjDbUtils.getString(row, "M026_NAMESPARE1"),
                            KnjDbUtils.getString(row, "M026_NAMESPARE2"),
                            KnjDbUtils.getString(row, "M027_NAME1")
                    );
                    if ("SCHOOLING".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                        ail._schAttendDatList.add(attend);
                    }
                    if ("SPECIAL".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                        ail._specialactAttendDatList.add(attend);
                    }
                    if ("TEST".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                        ail._testAttendDatList.add(attend);
                    }
                }
                return ail;
            }

            private static boolean between(final boolean nullequal, final String data, final String before, final String after) {
                return (nullequal && before == null && after == null) || before.compareTo(data) <= 0 && after.compareTo(data) >= 0;
            }

            private static Map<String, String> getMiyagiAttendanceMap(final DB2UDB db2, final Param param, final String year, final String schregno) {
                final String[] semesters = {"1", "2"};

                final MiyagiAttendance ail = getHrclassList(db2, param, year, schregno);

                String sumShukkou = null;
                String sumHr = null;
                String sumGyouji = null;
                String sumTokkatsu = null;
                for (int j = 0; j < semesters.length; j++) {
                    final String semester = semesters[j];
                    sumShukkou = add(sumShukkou, ail.getShukkouCount(param, semester, null, null));
                    sumHr = add(sumHr, ail.getHrCount(param, semester, null, null));
                    sumGyouji = add(sumGyouji, ail.getGyoujiCount(param, semester, null, null));
                    sumTokkatsu = add(sumTokkatsu, ail.getTokkatsuCount(param, semester, null, null));
                }
                final Map<String, String> map = new HashMap<String, String>();
                map.put("sumShukkou", sumShukkou); // 出校日数合計
                map.put("sumHr", sumHr); // HR合計
                map.put("sumGyouji", sumGyouji); // 行事合計
                map.put("sumTokkatsu", sumTokkatsu); // 特活合計
                return map;
            }

            private static String add(final Object count1, final Object count2) {
                if (!NumberUtils.isNumber((String) count1) && !NumberUtils.isNumber((String) count2)) {
                    return null;
                }
                final BigDecimal bd1 = NumberUtils.isNumber((String) count1) ? new BigDecimal((String) count1) : BigDecimal.valueOf(0);
                final BigDecimal bd2 = NumberUtils.isNumber((String) count2) ? new BigDecimal((String) count2) : BigDecimal.valueOf(0);
                return bd1.add(bd2).toString();
            }

            private static String sql() {
                final StringBuffer stb = new StringBuffer();

                stb.append(" WITH SCHREGNOS (SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST(? AS VARCHAR(4)))) ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SCHOOLING' END AS TABLEDIV, ");
                stb.append("     T2.EXECUTEDATE, ");
                stb.append("     T5.SEMESTER, ");
//                stb.append("     T2.PERIODCD AS PERIODF, ");
//                stb.append("     T2.PERIODCD AS PERIODT, ");
                stb.append("     T3.CLASSCD, ");
//                stb.append("     T2.CHAIRCD, ");
                stb.append("     T2.CREDIT_TIME, ");
//                stb.append("     T2.SCHOOLINGKINDCD, ");
                stb.append("     T9.NAMESPARE1 ");
                stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
                stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
                stb.append("   , NM_M027.NAME1 AS M027_NAME1");
                stb.append(" FROM SCHREG_BASE_MST T1 ");
                stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN SCH_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.YEAR = S1.YEAR ");
                stb.append(" LEFT JOIN SEMESTER_MST T5 ON T5.YEAR = T2.YEAR ");
                stb.append("     AND T5.SEMESTER <> '9' ");
                stb.append("     AND T2.EXECUTEDATE BETWEEN T5.SDATE AND T5.EDATE ");
                stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("     AND T3.YEAR = T2.YEAR ");
                stb.append("     AND T3.SEMESTER = T5.SEMESTER ");
                stb.append(" LEFT JOIN NAME_MST T9 ON T9.NAMECD1 = 'M001' ");
                stb.append("     AND T9.NAMECD2 = T2.SCHOOLINGKINDCD ");
                stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
                stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
                stb.append("     AND NM_M026.NAME1 = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
                stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
                stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
                stb.append("     AND NM_M027.NAME1 = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SPECIAL' END AS TABLEDIV, ");
                stb.append("     T2.ATTENDDATE AS EXECUTEDATE, ");
                stb.append("     T2.SEMESTER, ");
//                stb.append("     T2.PERIODF, ");
//                stb.append("     T2.PERIODT, ");
                stb.append("     T2.CLASSCD, ");
//                stb.append("     T2.CHAIRCD, ");
                stb.append("     T2.CREDIT_TIME, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
                stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
                stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
                stb.append("   , NM_M027.NAME1 AS M027_NAME1");
                stb.append(" FROM SCHREG_BASE_MST T1 ");
                stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN SPECIALACT_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.YEAR = S1.YEAR ");
                stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
                stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
                stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
                stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
                stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
                stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'TEST' END AS TABLEDIV, ");
                stb.append("     T2.INPUT_DATE AS EXECUTEDATE, ");
                stb.append("     T2.SEMESTER, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODF, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODT, ");
                stb.append("     T2.CLASSCD, ");
//                stb.append("     CAST(NULL AS VARCHAR(7)) AS CHAIRCD, ");
                stb.append("     CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
                stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
                stb.append("   , CAST(NULL AS VARCHAR(1)) AS M026_NAMESPARE2 ");
                stb.append("   , CAST(NULL AS VARCHAR(1)) AS M027_NAME1");
                stb.append(" FROM SCHREG_BASE_MST T1 ");
                stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN TEST_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.YEAR = S1.YEAR ");
                stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
                stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
                stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");

                return stb.toString();
            }

            private static class AttendInfo {
                final String _executedate;
                final String _semester;
//                final String _periodf;
//                final String _periodt;
                final String _classcd;
//                final String _chaircd;
                final String _creditTime;
//                final String _schoolingkindcd;
                final String _namespare1;
                final String _m026namespare1; // 1: 日数にカウントしない
                final String _m026namespare2; // 2: 時数にカウントしない
                final String _m027name1; // 特別活動科目コード

                public AttendInfo(
                        final String executedate,
                        final String semester,
//                        final String periodf,
//                        final String periott,
                        final String classcd,
//                        final String chaircd,
                        final String creditTime,
//                        final String schoolingkindcd,
                        final String namespare1,
                        final String m026namespare1,
                        final String m026namespare2,
                        final String m027name1
                ) {
                    _executedate = executedate;
                    _semester = semester;
//                    _periodf = periodf;
//                    _periodt = periott;
                    _classcd = classcd;
//                    _chaircd = chaircd;
                    _creditTime = creditTime;
//                    _schoolingkindcd = schoolingkindcd;
                    _namespare1 = namespare1;
                    _m026namespare1 = m026namespare1;
                    _m026namespare2 = m026namespare2;
                    _m027name1 = m027name1;
                }
            }
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRec extends KNJA130CCommon.StudyRec {

        private static final String FLAG_STUDYREC = "STUDYREC";
        private static final String FLAG_CHAIR_STD = "CHAIR_STD";
        private static final String FLAG_SUBSTITUTION = "SUBSTITUTION";

        private static enum KIND {
            SOGO90, SYOKEI, ABROAD, TOTAL, SOGO94,
            CREDIT_ONLY, // 離して表示する
            JIRITSU; // 自立活動
        }

        private static enum CreditKind {
            CREDIT, COMP_CREDIT, CREDIT_MSTCREDIT, BUNKATSU_RISHU_CREDIT,
            CREDIT_ZENKI, COMP_CREDIT_ZENKI, CREDIT_ZENKI_IGAI, COMP_CREDIT_ZENKI_IGAI,
            SATEI_CREDIT,
            CREDIT_SAGA_A,
            CREDIT_SAGA_B;
        }

        private static enum TotalM {
            SUBJECT90,
            ABROAD,
            SUBJECT,
            KATEIGAI,
            TOTAL,
            SUBJECT90_SAGA_A,
            SUBJECT90_SAGA_B,
            SUBJECT_SAGA_A,
            SUBJECT_SAGA_B,
            TOTAL_SAGA_A,
            TOTAL_SAGA_B
            ;
        }
        private static final String SATEI = "SATEI";

        final String _schoolcd; // 現状利用しているのはKNJA133Mのみ。それ以外は"0"
        final String _year;
        final String _annual;
        final ClassMst _classMst;
        final SubclassMst _subclassMst;
        final Integer _valuation;
        final BigDecimal _credit;
        BigDecimal _compCredit;
        final BigDecimal _creditMstCredits;
        final String _studyFlag;
        final String _validFlg;

        protected boolean _isSubstitutionRecord;

        ClassMst _classMstSaki = ClassMst.Null;
        SubclassMst _subclassMstSaki = SubclassMst.Null;

        // KNJA133M
        public static int TANNI_DIV_CREDIT = 1;
        public static int TANNI_DIV_SATEI = 2;

        protected boolean _isDrop;

        protected BigDecimal _sateiCredit;

        private List<Map<String, String>> _zenkiKamokuRowList = Collections.emptyList();
        private List<Map<String, String>> _zenkiKamokuIgaiRowList = Collections.emptyList();
        private List<StudyRec> _yomikaeMotoStudyrecList = null;
        Integer _valuationZenki;
        BigDecimal _creditZenki;
        BigDecimal _compCreditZenki;
        Integer _valuationZenkiIgai;
        BigDecimal _creditZenkiIgai;
        BigDecimal _compCreditZenkiIgai;

        /**
         * コンストラクタ。
         */
        private StudyRec(final String schoolcd, final String year, final String annual, final ClassMst classMst, final SubclassMst subclassMst,
                final BigDecimal credit,
                final BigDecimal compCredit, final BigDecimal creditMstCredits,
                final Integer valuation,
                final String studyFlag, final String validFlg) {
            _schoolcd = schoolcd;
            _year = year;
            _annual = annual;
            _classMst = classMst;
            _subclassMst = subclassMst;
            _credit = credit;
            _compCredit = compCredit;
            _creditMstCredits = creditMstCredits;
            _valuation = valuation;
            _studyFlag = studyFlag;
            _validFlg = validFlg;
        }

        private boolean isSaki(final Param param, final YOSHIKI yoshiki) {
            if (param._useStudyrecReplaceDat && yoshiki == YOSHIKI._2_OMOTE) {
                if (SCHOOLCD1.equals(_schoolcd)) {
                    return true;
                }
            }
            return false;
        }

        private void setHankiKamokuRowList(final Param param, final List<Map<String, String>> zenkiList, final List<Map<String, String>> zenkiIgaiList) {
            _zenkiKamokuRowList = zenkiList;
            _zenkiKamokuIgaiRowList = zenkiIgaiList;

            final BigDecimal valuationZenkiBd = calcNewGrades(param, _year, zenkiList);
            _valuationZenki = null == valuationZenkiBd ? null : new Integer(valuationZenkiBd.intValue());
            _creditZenki = Util.toBdSum(KnjDbUtils.getColumnDataList(zenkiList, "CREDIT")); // SUM(T1.CREDIT) AS CREDIT
            _compCreditZenki = Util.toBdSum(KnjDbUtils.getColumnDataList(zenkiList, "COMP_CREDIT")); // SUM(T1.COMP_CREDIT)
            final BigDecimal valuationZenkiIgaiBd = calcNewGrades(param, _year, zenkiIgaiList);
            _valuationZenkiIgai = null == valuationZenkiIgaiBd ? null : new Integer(valuationZenkiIgaiBd.intValue());
            _creditZenkiIgai = Util.toBdSum(KnjDbUtils.getColumnDataList(zenkiIgaiList, "CREDIT")); // SUM(T1.CREDIT) AS CREDIT
            _compCreditZenkiIgai = Util.toBdSum(KnjDbUtils.getColumnDataList(zenkiIgaiList, "COMP_CREDIT")); // SUM(T1.COMP_CREDIT)
            //log.info(" set hanki = (" + _valuationZenki + ", " + _creditZenki + ", " + _compCreditZenki + "), (" + _valuationZenkiIgai + ", " + _creditZenkiIgai + ", " + _compCreditZenkiIgai + ")");
        }

        public ClassMst classMst(final Param param, final YOSHIKI yoshiki) {
            if (isSaki(param, yoshiki)) {
                return _classMstSaki;
            }
            return _classMst;
        }

        public SubclassMst subclassMst(final Param param, final YOSHIKI yoshiki) {
            if (isSaki(param, yoshiki)) {
                return _subclassMstSaki;
            }
            return _subclassMst;
        }

        private static class AbroadStudyRec extends StudyRec {
            final String _remark1;
            /**
             * コンストラクタ。留学。
             */
            private AbroadStudyRec(final String year, final String annual, final BigDecimal credit, final String remark1) {
                super("0", year, annual, ClassMst.ABROAD, SubclassMst.ABROAD, credit, null, null, null, FLAG_STUDYREC, null);
                _remark1 = remark1;
            }
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        public boolean isRishuNomi(final Param param) {
            final boolean isRishuNomi;
            if (param._z010.in(Z010.kyoto)) {
                // 修得単位数が0 かつ 履修単位数が1以上
                isRishuNomi = 0 == intVal(_credit, -1) && 1 <= intVal(_compCredit, -1);
            } else {
                // (修得単位数がnullもしくは0) かつ 履修単位数が1以上
                isRishuNomi = 0 == intVal(_credit, 0) && 1 <= intVal(_compCredit, -1);
                if (0 == intVal(_credit, 0)) {
                    if (param._isOutputDebug) {
                        log.info(" 履修のみ? " + isRishuNomi + ", 修得単位 " + _credit + ",  履修単位 " + _compCredit + ", " + _year + ", " + _subclassMst.getKey(param));
                    }
                }
            }
            return isRishuNomi;
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        public boolean isMirishu(final Param param) {
            if (param._is133m) {
                // 修得単位数が0 かつ 履修単位数が0
                return 0 == intVal(_credit, -1) && 0 == intVal(_compCredit, -1);
            }
            // 修得単位数が0 かつ 履修単位数が0 かつ 評定がnull
            return 0 == intVal(_credit, -1) && 0 == intVal(_compCredit, -1) && -1 == intVal(_valuation, -1);
        }

        protected static int intVal(final Number n, final int def) {
            return null == n ? def : n.intValue();
        }

        public static class StudyrecComparator implements Comparator<StudyRec> {
            final Param _param;
            final YOSHIKI _yoshiki;
            StudyrecComparator(final Param param, final YOSHIKI yoshiki) {
                _param = param;
                _yoshiki = yoshiki;
            }
            public int compare(final StudyRec that1, final StudyRec that2) {
                int rtn;
                rtn = ClassMst.compareOrder(_param, that1.classMst(_param, _yoshiki), that2.classMst(_param, _yoshiki));
                if (0 != rtn) { return rtn; }
                rtn = SubclassMst.compareOrder(_param, that1.subclassMst(_param, _yoshiki), that2.subclassMst(_param, _yoshiki));
                if (0 != rtn) { return rtn; }
                rtn = that1._year.compareTo(that2._year);
                return rtn;
            }
        }

        public String toString() {
            return "StudyRec(" + _schoolcd + ", year = " + _year + ", " + _subclassMst.toString() + ", valuation = " + _valuation + ", credit = " + _credit + ", compCredit = " + _compCredit + ", (satei = " + _sateiCredit + ", creditMstCredit = " + _creditMstCredits + "))";
        }

        public String toString(final Param param, final YOSHIKI yoshiki) {
            return "StudyRec(" + _schoolcd + ", year = " + _year + ", " + defstr(subclassMst(param, yoshiki)) + ", valuation = " + _valuation + ", credit = " + _credit + ", compCredit = " + _compCredit + ", (satei = " + _sateiCredit + "))";
        }

        private String getKeySubclasscdForSubclassTotal(final Param param) {
            if ("1".equals(param._useCurriculumcd) && param._isSubclassOrderNotContainCurriculumcd) {
                return _subclassMst._classcd + "-" + _subclassMst._schoolKind + "-" + _subclassMst._subclasscd;
            }
            return getKeySubclasscd(param);
        }
        private String getKeySubclasscd(final Param param) {
            return _subclassMst.getKey(param);
        }

        public Map<StudyRec.CreditKind, BigDecimal> creditForTotal(final Param param, final CreditKind kind) {
            final Map<StudyRec.CreditKind, BigDecimal> rtn = new HashMap<StudyRec.CreditKind, BigDecimal>();
            if (CreditKind.COMP_CREDIT == kind) {
                if (null != _compCredit) {
                    rtn.put(CreditKind.COMP_CREDIT, _compCredit);
                }
            } else if (CreditKind.COMP_CREDIT_ZENKI == kind) {
                if (null != _compCreditZenki) {
                    rtn.put(CreditKind.COMP_CREDIT_ZENKI, _compCreditZenki);
                }
            } else if (CreditKind.COMP_CREDIT_ZENKI_IGAI == kind) {
                if (null != _compCreditZenkiIgai) {
                    rtn.put(CreditKind.COMP_CREDIT_ZENKI_IGAI, _compCreditZenkiIgai);
                }
            } else if (CreditKind.CREDIT_MSTCREDIT == kind) {
                if (null != _creditMstCredits) {
                    rtn.put(CreditKind.CREDIT_MSTCREDIT, _creditMstCredits);
                }
            } else if (CreditKind.BUNKATSU_RISHU_CREDIT == kind) {
                if (null != _yomikaeMotoStudyrecList && _yomikaeMotoStudyrecList.size() > 1) {
                    Set<StudyRec.CreditKind> set = new HashSet<StudyRec.CreditKind>();
                    for (final StudyRec sr : _yomikaeMotoStudyrecList) {
                        if (sr.isRishuNomi(param)) {
                            set.add(CreditKind.COMP_CREDIT);
                        } else if (null != sr._credit) {
                            set.add(CreditKind.CREDIT);
                        } else if (null != sr._compCredit) {
                            set.add(CreditKind.COMP_CREDIT);
                        }
                    }
                    if (set.size() > 1) {
                        for (final StudyRec sr : _yomikaeMotoStudyrecList) {
                            if (sr.isRishuNomi(param)) {
                                rtn.put(CreditKind.COMP_CREDIT, sr._compCredit);
                            } else if (null != sr._credit) {
                                rtn.put(CreditKind.CREDIT, sr._credit);
                            } else if (null != sr._compCredit) {
                                rtn.put(CreditKind.COMP_CREDIT, sr._compCredit);
                            }
                        }
                        return rtn;
                    }
                }
                if (isRishuNomi(param)) {
                    rtn.put(CreditKind.COMP_CREDIT, _compCredit);
                } else if (null != _credit) {
                    rtn.put(CreditKind.CREDIT, _credit);
                } else if (null != _compCredit) {
                    rtn.put(CreditKind.COMP_CREDIT, _compCredit);
                }
            } else if (CreditKind.CREDIT == kind) {
                if (null != _credit) {
                    rtn.put(CreditKind.CREDIT, _credit);
                }
            } else if (CreditKind.CREDIT_ZENKI == kind) {
                if (null != _creditZenki) {
                    rtn.put(CreditKind.CREDIT_ZENKI, _creditZenki);
                }
            } else if (CreditKind.CREDIT_ZENKI_IGAI == kind) {
                if (null != _creditZenkiIgai) {
                    rtn.put(CreditKind.CREDIT_ZENKI_IGAI, _creditZenkiIgai);
                }
            }
            return rtn;
        }

        public List<StudyRec.KIND> kindList(final Param param, final PersonalInfo pInfo) {
            final List<StudyRec.KIND> kindList = new ArrayList<StudyRec.KIND>();
            if (_ABROAD.equals(_classMst._classname)) {
                kindList.add(KIND.ABROAD);
            }
            kindList.add(KIND.TOTAL);
            if (_90.equals(_classMst._classcd)) {
                kindList.add(KIND.SOGO90);
            }
            if (pInfo.jiritsuKatudouClasscdList(param).contains(_classMst._classcd) || param._e065Name1JiritsuKatsudouSubclasscdList.contains(getKeySubclasscd(param))) {
                kindList.add(KIND.JIRITSU);
            }
            if (!_ABROAD.equals(_classMst._classname)) {
                if (param._z010.in(Z010.tokiwa)) {
                    // 常磐は小計にLHRを含めない
                    if (_94.equals(_classMst._classcd)) {
                    } else {
                        kindList.add(KIND.SYOKEI);
                    }
                } else {
                    kindList.add(KIND.SYOKEI);
                }
            }
            if (pInfo.creditOnlyClasscdList(param).contains(_classMst._classcd)) {
                kindList.add(KIND.CREDIT_ONLY);
            }
            if (param._z010.in(Z010.tokiwa) || param._z010.in(Z010.nishiyama) || param._z010.in(Z010.bunkyo)) {
                if (_94.equals(_classMst._classcd)) {
                    kindList.add(KIND.SOGO94);
                }
            }
            return kindList;
        }

        /**
         * 留年した年度を考慮した合計用のStudyRecのリスト
         * @param studyRecList 対象のStudyRecのリスト
         * @param dropYears 留年した年度
         * @param printDropRegdYears
         * @param checkDropYears 留年した年度の処理フラグ 0:留年した年度を除く 1:留年時の有効フラグをチェック。それ以外は0と同じ 2:全て
         * @return 合計用のStudyRecのリスト
         */
        private static List<StudyRec> getTargetStudyRecList(final List<StudyRec> studyRecList, final Collection<String> dropYears, final Collection<String> printDropRegdYears, final Collection<String> enabledDropYears, final int checkDropYears) {
            if (checkDropYears == 2) {
                return new ArrayList<StudyRec>(studyRecList);
            }
            final List<StudyRec> normal = new ArrayList<StudyRec>();
            final List<StudyRec> validFlgOnList = new ArrayList<StudyRec>(); // 原級留置した年度で有効フラグが設定されているStudyRecのリスト
            for (final StudyRec sr : studyRecList) {
                if (dropYears.contains(sr._year) && !printDropRegdYears.contains(sr._year)) {
                    if (null != sr._validFlg) {
                        validFlgOnList.add(sr);
                    } else if (null != enabledDropYears && enabledDropYears.contains(sr._year)) {
                        normal.add(sr);
                    }
                } else {
                    normal.add(sr);
                }
            }
            if (checkDropYears == 1 && validFlgOnList.size() > 0) {
                // 原級留置した年度で有効フラグが設定されているStudyRecがあればそちらを返す(原級留置した年度以前の通常の成績を含む)
                int minYear = 9999;
                for (final StudyRec srv : validFlgOnList) {
                    if (NumberUtils.isDigits(srv._year)) {
                        minYear = Math.min(minYear, Integer.parseInt(srv._year));
                    }
                }
                for (final Iterator<StudyRec> nit = normal.iterator(); nit.hasNext();) {
                    final StudyRec srn = nit.next();
                    if (NumberUtils.isDigits(srn._year) && Integer.parseInt(srn._year) < minYear) {
                        validFlgOnList.add(srn);
                        nit.remove();
                    }
                }
                //log.info(" valid on = " + validFlgOnList + " / rtn = " + rtn);
                return validFlgOnList;
            }
            return normal;
        }

        /**
         * 学習記録データクラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<StudyRec> loadStudyRec(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Param param, final GakushuBiko gakushuBiko, final String startYear) {
            final List<StudyRec> studyRecList = new LinkedList<StudyRec>();

            final Set<String> dropYears = new HashSet<String>();
            if (param._is133m) {
                if (param._schoolDiv.isGakunenSei(null, pInfo, student)) {
                    for (final Gakuseki gaku : pInfo._gakusekiList) {
                        if (gaku._isDrop) {
                            dropYears.add(gaku._year);
                        }
                    }
                }
            }

            if (null == param._knja130_2 && null == param._knja130_3 && null == param._knja130_4) {
                // 使用しない
                return Collections.emptyList();
            }

            studyRecList.addAll(createAbroadStudyrec(db2, student, param, pInfo, dropYears));

//            if (true) {
                studyRecList.addAll(createStudyrecNew(db2, student, param, pInfo, dropYears, startYear));
//            } else {
//                studyRecList.addAll(createStudyrec(db2, student, param, pInfo, dropMap));
//            }

            // 全部/一部代替科目取得
            studyRecList.addAll(createStudyrecSubstitution(db2, pInfo, param, gakushuBiko, dropYears, startYear));

            // リストをソートします。
            Collections.sort(studyRecList, new StudyRec.StudyrecComparator(param, YOSHIKI.NONE));

            if (param._isOutputDebugData) {
                for (final StudyRec sr : studyRecList) {
                    log.info(" studyrec subclasscd = " + sr.getKeySubclasscd(param) + " " + sr._subclassMst + " " + (sr._studyFlag));
                }
            }

            return studyRecList;
        }

        private static List<StudyRec> createStudyrecSubstitution(final DB2UDB db2, final PersonalInfo pInfo, final Param param, final GakushuBiko gakushuBiko, final Set<String> dropYears, final String startYear) {
            // 代替科目取得
            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();
            final String psKey = "PS_KEY_STUDYREC_SUBST_(" + startYear + ")" + Util.mkString(pInfo.creditOnlyClasscdList(param), "|") + "," + Util.mkString(pInfo.jiritsuKatudouClasscdList(param), "|");
            if (null == param.getPs(psKey)) {
                final String sql = sqlReplaceSubclassSubstitution(param, startYear, pInfo.creditOnlyClasscdList(param), pInfo.jiritsuKatudouClasscdList(param));
                if (param._isOutputDebugQuery) {
                    log.info(" subst sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }
            final List<GakushuBiko.DAITAI_TYPE> daitaiTypeList;
            if (param._is133m) {
                daitaiTypeList = Collections.singletonList(GakushuBiko.DAITAI_TYPE.NO_TYPE_FLG);
            } else {
                daitaiTypeList = GakushuBiko.TYPE_FLG_LIST;
            }
            for (final GakushuBiko.DAITAI_TYPE daitaiType : daitaiTypeList) {

                final List<Map<String, String>> rowList;

                if (param._is133m) {
                    rowList = KnjDbUtils.query(db2, param.getPs(psKey), new String[] { pInfo._schregno });
                } else {
                    rowList = KnjDbUtils.query(db2, param.getPs(psKey), new String[] { pInfo._schregno, daitaiType._typeFlg });
                }

                if (param._isOutputDebugQuery) {
                    log.info(" type = " + daitaiType + ", substitution rowList = " + Util.listString(rowList, 0));
                }

                for (final Map row : rowList) {
                    final GakushuBiko.DAITAI_TYPE type = GakushuBiko.DAITAI_TYPE.valueOfFlg(KnjDbUtils.getString(row, "SUBSTITUTION_TYPE_FLG"));
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String annual = KnjDbUtils.getString(row, "ANNUAL");
                    final String substitutionClasscd = KnjDbUtils.getString(row, "SUBSTITUTION_CLASSCD");           // 代替先科目教科コード
                    final String substitutionSubclasscd = KnjDbUtils.getString(row, "SUBSTITUTION_SUBCLASSCD");     // 代替先科目コード
                    final BigDecimal credit = null;
                    final BigDecimal substitutionCreditMstCredit = KnjDbUtils.getBigDecimal(row, "SUBSTITUTION_CREDIT_MST_CREDIT", null);
                    final Integer valuationNull = null;
                    final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"); // 代替元科目コード

                    if (!NumberUtils.isDigits(substitutionClasscd)) {
                        continue;
                    } else if (!(KNJDefineSchool.subject_D.compareTo(substitutionClasscd) <= 0 && substitutionClasscd.compareTo(KNJDefineSchool.subject_U) <= 0 || KNJDefineSchool.subject_T.equals(substitutionClasscd))) {
                        continue;
                    }

                    final String substitutionSchoolKind = KnjDbUtils.getString(row, "SUBSTITUTION_SCHOOL_KIND");
                    final String substitutionCurriculumCd = KnjDbUtils.getString(row, "SUBSTITUTION_CURRICULUM_CD");
                    final String attendClassCd = KnjDbUtils.getString(row, "ATTEND_CLASSCD");
                    final String attendSchoolKind = KnjDbUtils.getString(row, "ATTEND_SCHOOL_KIND");
                    final String attendCurriculumCd = KnjDbUtils.getString(row, "ATTEND_CURRICULUM_CD");

                    final ClassMst attendClassMst = ClassMst.get(param, param._classMstMap, ClassMst.key(param, attendClassCd, attendSchoolKind));
                    final SubclassMst attendSubclassMst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, attendClassCd, attendSchoolKind, attendCurriculumCd, attendSubclasscd));
                    if (SubclassMst.Null == attendSubclassMst) {
                        continue;
                    }

                    final String schoolcd;
//                    final String specialDiv;
                    final String studyFlg = StudyRec.FLAG_SUBSTITUTION;
                    final BigDecimal attendCredit; // 代替元科目単位
                    final BigDecimal attendCompCredit; // 代替元科目履修単位
                    final Integer attendValuation; // 代替元科目評定
                    if (param._is133m) {
                        schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
//                        specialDiv = param._isTokyoto ? "0" : getString("SPECIALDIV", row);
                        attendCredit = null; // 代替元科目単位
                        attendCompCredit = null; // 代替元科目履修単位
                        attendValuation = null; // 代替元科目評定
                    } else {
                        schoolcd = "0";
//                        specialDiv = getString("SPECIALDIV", row);
                        attendCredit = KnjDbUtils.getBigDecimal(row, "ATTEND_CREDIT", null); // 代替元科目単位
                        attendCompCredit = KnjDbUtils.getBigDecimal(row, "ATTEND_COMP_CREDIT", null); // 代替元科目履修単位
                        attendValuation = KnjDbUtils.getInt(row, "ATTEND_VALUATION", null); // 代替元科目評定
                    }

                    final ClassMst substClassMst = ClassMst.get(param, param._classMstMap, ClassMst.key(param, substitutionClasscd, substitutionSchoolKind));
                    final SubclassMst substSubclassMst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd));
                    if (SubclassMst.Null == substSubclassMst) {
                        continue;
                    }

                    final StudyRec replacedStudyRec = new StudyRec(schoolcd, year, annual, substClassMst, substSubclassMst, credit, null, null, valuationNull, studyFlg, null);
                    replacedStudyRec._isDrop = dropYears.contains(year);

                    studyRecList.add(replacedStudyRec);

                    final Map<SubclassMst, StudyRecSubstitution> subclassStudyrecSubstitutionMap = gakushuBiko.getInputSubclassStudyrecSubstitutionMap(daitaiType);

                    if (null == subclassStudyrecSubstitutionMap.get(substSubclassMst)) {
                        final String substSchoolcd;
                        final Set<String> substDropYears;
                        if (param._is133m) {
                            substSchoolcd = null;
                            substDropYears = dropYears;
                        } else {
                            substSchoolcd = "0";
                            substDropYears = null;
                        }
                        subclassStudyrecSubstitutionMap.put(substSubclassMst, new StudyRecSubstitution(type, substSchoolcd, null, annual, substClassMst, substSubclassMst, credit, valuationNull, studyFlg, substDropYears));
                    }
                    final StudyRecSubstitution studyRecSubstitution = subclassStudyrecSubstitutionMap.get(substSubclassMst);

                    final BigDecimal attendCreditMstCredit = KnjDbUtils.getBigDecimal(row, "ATTEND_CREDIT_MST_CREDIT", null); // 代替先単位

                    studyRecSubstitution._attendSubclasses.add(new StudyRecSubstitution.SubstitutionAttendSubclass(year, substitutionCreditMstCredit, attendClassMst, attendSubclassMst, attendCredit, attendCompCredit, attendValuation, attendCreditMstCredit));
                }

                if (param._isOutputDebug) {
                    log.info(" substitutionType = " + daitaiType + ", substitution map = " + gakushuBiko.getInputSubclassStudyrecSubstitutionMap(daitaiType));
                }
            }
            return studyRecList;
        }

        private static Tuple<Map<String, Map<String, String>>, Map<String, List<Map<String, String>>>> groupedRowMapAndRowList(final List<Map<String, String>> rowList, final List<String> groupFieldList) {
            final Map<String, Map<String, String>> groupedMap = new TreeMap();
            final Map<String, List<Map<String, String>>> groupedRowList = new TreeMap();
            for (final Map<String, String> row : rowList) {
                final String groupKey = Util.mkString(Util.valueList(row, groupFieldList), "-").toString();
                if (null == groupedMap.get(groupKey)) {
                    groupedMap.put(groupKey, Util.keyValueMap(row, groupFieldList));
                }
                getMappedList(groupedRowList, groupKey).add(row);
            }
            return Tuple.of(groupedMap, groupedRowList);
        }

        private static List<StudyRec> createStudyrecNew(final DB2UDB db2, final Student student, final Param param, final PersonalInfo pInfo, final Set<String> dropYears, final String startYear) {
            final boolean useChairStdDat = param._z010.in(Z010.tokiwa) && (pInfo.isTaigaku() || pInfo.isTengaku());
            final String psKey = "PS_STUDYREC_(" + startYear + ")" + Util.mkString(pInfo.creditOnlyClasscdList(param), "|") + "," + Util.mkString(pInfo.jiritsuKatudouClasscdList(param), "|");
            if (null == param.getPs(psKey)) {
                final String sql = sqlStudyrecNew(param, startYear, pInfo.creditOnlyClasscdList(param), pInfo.jiritsuKatudouClasscdList(param));
                if (param._isOutputDebugQuery) {
                    log.info(" studyrec " + psKey + " sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }

            final String paramYear = useChairStdDat ? Student.getTengakuTaigakuNendoMinus1(pInfo) : param._year;
            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno, paramYear});

            for (final Map<String, String> row : rowList) {

                final String schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                if (param._is133m && param._useStudyrecReplaceDat) {
                    if ("1".equals(schoolcd)) {
                        continue;
                    }
                } else {
                    if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherStudyrec))) {
                        if ("1".equals(schoolcd)) {
                            if (param._isOutputDebugData) {
                                log.info(" not print another studyrec : " + row);
                            }
                            continue;
                        }
                    }
                }

                String year = KnjDbUtils.getString(row, "YEAR");
                if (isAnotherSchoolYear(year)) {
                    year = ANOTHER_YEAR;
                    row.put("YEAR", year);
                }
                SubclassMst mst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"), KnjDbUtils.getString(row, "SUBCLASSCD")));
                if (param._isHankiNinteiForm && null != mst && mst.isZenkiKamoku(year)) {
                    row.put("ZENKI_KAMOKU", "1"); // 前期科目
                    if (param._isOutputDebugSeiseki) {
                        log.info(" set ZENKI_KAMOKU " + debugRow(row, null));
                    }
                }
                final List<SubclassMst> befores = new ArrayList<SubclassMst>();
                if (null != mst._subclasscd2) {
                    row.put("YOMIKAE_MOTO_SUBCLASSCD", mst._subclasscd);
                }
                while (null != mst && null != mst._subclasscd2) {
                    if (befores.contains(mst) || mst._subclasscd.equals(mst._subclasscd2)) {
                        param.logOnce("循環参照 : " + mst + " in " + befores + " or " + mst._subclasscd2);
                        break;
                    }
                    if (param._isHankiNinteiForm && null != mst && mst.isZenkiKamoku(year)) {
                        row.put("ZENKI_KAMOKU", "1"); // 前期科目
                        if (param._isOutputDebugSeiseki) {
                            log.info(" set ZENKI_KAMOKU " + row);
                        }
                    }
                    row.put("SUBCLASSCD", mst._subclasscd2);
                    if (param._isOutputDebugSeiseki) {
                        log.info(" replaced subclasscd " + mst._subclasscd + " with " + mst._subclasscd2);
                    }
                    befores.add(mst);
                    mst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, mst._classcd, mst._schoolKind, mst._curriculumCd, mst._subclasscd2));
                }
            }

            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();
            final List<String> groupByFieldList = Arrays.asList("YEAR", "ANNUAL", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD");
            Tuple<Map<String, Map<String, String>>, Map<String, List<Map<String, String>>>> groupedRowMapAndRowList = groupedRowMapAndRowList(rowList, groupByFieldList);
            final Map<String, Map<String, String>> groupedRowMap = groupedRowMapAndRowList._first;
            final Map<String, List<Map<String, String>>> groupedRowList = groupedRowMapAndRowList._second;
            for (final Map.Entry<String, Map<String, String>> e : groupedRowMap.entrySet()) {
                final String key = e.getKey();
                final Map<String, String> group = e.getValue();

                final String year = KnjDbUtils.getString(group, "YEAR");

                final List<Map<String, String>> groupRowList = getMappedList(groupedRowList, key);

                if (param._isOutputDebugSeiseki && groupRowList.size() > 1) {
                    log.info(" group " + key + " size = " + groupRowList.size());
                    for (int i = 0; i < groupRowList.size(); i++) {
                        final Map<String, String> row = groupRowList.get(i);
                        log.info("  [" + i + "] = " + debugRow(row, null));
                    }
                }

                final List<Map<String, String>> zenkiList = new ArrayList<Map<String, String>>();
                final List<Map<String, String>> zenkiIgaiList = new ArrayList<Map<String, String>>();
                if (param._isHankiNinteiForm) {
                    for (final Map<String, String> row : groupRowList) {
                        if ("1".equals(row.get("ZENKI_KAMOKU"))) {
                            zenkiList.add(row);
                        } else {
                            zenkiIgaiList.add(row);
                        }
                    }
                }

                final BigDecimal newGrades = calcNewGrades(param, year, groupRowList);
                BigDecimal credit = Util.toBdSum(KnjDbUtils.getColumnDataList(groupRowList, "CREDIT")); // SUM(T1.CREDIT) AS CREDIT
                BigDecimal compCredit = Util.toBdSum(KnjDbUtils.getColumnDataList(groupRowList, "COMP_CREDIT")); // SUM(T1.COMP_CREDIT)
                final String classcd = KnjDbUtils.getString(group, "CLASSCD");
                boolean sogakuKoteiTanniToAbroad = false;
                if (param._setSogakuKoteiTanni && _90.equals(classcd)) {
                    final String annual = KnjDbUtils.getString(group, "ANNUAL");
                    if (NumberUtils.isDigits(annual)) {
                        final BigDecimal replaceCredit = param._sogakuKoteiTanniMap.get(Integer.valueOf(annual));
                        if (null != replaceCredit) {
                            if (null != credit) {
                                credit = replaceCredit;
                            }
                            if (null != compCredit) {
                                compCredit = replaceCredit;
                            }
                        }
                    }
                    if (student._abroadYears.contains(year)) {
                        sogakuKoteiTanniToAbroad = true;
                    }
                }

                final String classname = Util.stringMin(KnjDbUtils.getColumnDataList(groupRowList, "CLASSNAME")); // MIN(T1.CLASSNAME)
                final String subclassname = Util.stringMin(KnjDbUtils.getColumnDataList(groupRowList, "SUBCLASSNAME")); // MIN(T1.SUBCLASSNAME)
                final String rowSchoolcd = Util.stringMin(KnjDbUtils.getColumnDataList(groupRowList, "SCHOOLCD")); // MIN(T1.SCHOOLCD)
                final String validFlg = Util.stringMin(KnjDbUtils.getColumnDataList(groupRowList, "VALID_FLG")); // MIN(T1.VALID_FLG)
                final Map newRow = Util.keyValueMap(group, groupByFieldList);
                newRow.put("CLASSNAME", classname);
                newRow.put("SUBCLASSNAME", subclassname);
                newRow.put("GRADES", newGrades);
                newRow.put("CREDIT", credit);
                newRow.put("COMP_CREDIT", compCredit);
                newRow.put("SCHOOLCD", rowSchoolcd);
                newRow.put("VALID_FLG", validFlg);
                if (param._isOutputDebug && groupRowList.size() > 1) {
                    log.info("   newRow = " + newRow);
                }

                final String schoolKind = KnjDbUtils.getString(newRow, "SCHOOL_KIND");
                final String curriculumCd = KnjDbUtils.getString(newRow, "CURRICULUM_CD");
                final String annual = KnjDbUtils.getString(newRow, "ANNUAL");
                final String subclasscd = KnjDbUtils.getString(newRow, "SUBCLASSCD");
                final Integer grades = null == newGrades ? null : new Integer(Math.round(new Double(newGrades.doubleValue()).floatValue()));
                final String classMstKey = ClassMst.key(param, classcd, schoolKind);
                ClassMst classMst;
                if ("1".equals(rowSchoolcd)) {
                    classMst = AnotherClassMst.getAnother(param, param._anotherClassMstMap, classMstKey);
                    if (classMst == AnotherClassMst.Null) {
                        classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                    }
                } else {
                    classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                }
                if (null != classname && !defstr(classMst._classname).equals(classname)) {
                    classMst = classMst.setClassname(classname);
                }
                SubclassMst subclassMst = getSubclassMst(param, newRow, schoolKind, curriculumCd, classcd, subclasscd);
                if (SubclassMst.Null == subclassMst) {
                    subclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, null, SHOWORDER_DEFAULT , null);
                }
                if (null != subclassname && !defstr(subclassMst.subclassname()).equals(defstr(subclassname))) {
                    subclassMst = subclassMst.setSubclassordername(subclassname);
                }

                StudyRec sr = null;
                if (param._is133m) {
                    final String schoolcd = rowSchoolcd;

                    sr = new StudyRec(schoolcd, year, annual, classMst, subclassMst, credit, compCredit, null, grades, null, null);
                    sr._isDrop = dropYears.contains(year);

                } else {
                    final String schoolcd = "0";

                    final SchregRegdDat regd = SchregRegdDat.getMaxSemesterRegd(student._regdList, year);
                    BigDecimal creditMstCredit = null;
                    if (null != regd) {
                        creditMstCredit = CreditMst.getCredit(db2, param, year, regd._grade, regd._coursecd, regd._majorcd, regd._coursecode, classcd, schoolKind, curriculumCd, subclasscd);
                    }
                    if (param._setSogakuKoteiTanni && _90.equals(classcd)) {
                        creditMstCredit = null;
                    }

                    if (sogakuKoteiTanniToAbroad) {
                        sr = new StudyRec.AbroadStudyRec(year, annual, credit, null);
                    } else {
                        sr = new StudyRec(schoolcd, year, annual, classMst, subclassMst, credit, compCredit, creditMstCredit, grades, StudyRec.FLAG_STUDYREC, validFlg);
                    }
                    if (isAnotherSchoolYear(sr._year) && !param._seitoSidoYorokuZaisekiMae) {
                        if (param._isOutputDebug || param._isOutputDebugSeiseki) {
                            log.info(" skip record " + sr);
                        }
                        sr = null;
                    }
                }

                final String provFlg = max(Util.filter(KnjDbUtils.getColumnDataList(groupRowList, "PROV_FLG"), new NotNullPredicate<String>()));
                if (null != provFlg) {
                    if (param._isOutputDebug || param._isOutputDebugSeiseki) {
                        log.info(" skip 仮評定 " + sr);
                    }
                }

                if (null != sr) {
                    if (!param._is133m) {
                        if (groupRowList.size() > 1) {
                            final List<StudyRec> yomikaeMotoStudyrecList = new ArrayList<StudyRec>();
                            for (int i = 0; i < groupRowList.size(); i++) {
                                final Map<String, String> row = groupRowList.get(i);

                                final String rsubclasscd = KnjDbUtils.getString(row, row.containsKey("YOMIKAE_MOTO_SUBCLASSCD") ? "YOMIKAE_MOTO_SUBCLASSCD" : "SUBCLASSCD");

                                final Integer rgrades = KnjDbUtils.getInt(row, "GRADES", null);
                                final BigDecimal rcredit = KnjDbUtils.getBigDecimal(row, "CREDIT", null);
                                final BigDecimal rcompCredit = KnjDbUtils.getBigDecimal(row, "COMP_CREDIT", null);

                                final String rSchoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                                final String rvalidFlg = KnjDbUtils.getString(row, "VALID_FLG");

                                final String rclassMstKey = ClassMst.key(param, classcd, schoolKind);
                                ClassMst rclassMst;
                                if ("1".equals(rowSchoolcd)) {
                                    rclassMst = AnotherClassMst.getAnother(param, param._anotherClassMstMap, rclassMstKey);
                                    if (rclassMst == AnotherClassMst.Null) {
                                        rclassMst = ClassMst.get(param, param._classMstMap, rclassMstKey);
                                    }
                                } else {
                                    rclassMst = ClassMst.get(param, param._classMstMap, rclassMstKey);
                                }
                                if (null != classname && !defstr(rclassMst._classname).equals(classname)) {
                                    rclassMst = rclassMst.setClassname(classname);
                                }
                                SubclassMst rsubclassMst = getSubclassMst(param, newRow, schoolKind, curriculumCd, classcd, rsubclasscd);
                                if (SubclassMst.Null == rsubclassMst) {
                                    rsubclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, rsubclasscd, subclassname, null, SHOWORDER_DEFAULT , null);
                                }
                                if (null != subclassname && !defstr(rsubclassMst.subclassname()).equals(defstr(subclassname))) {
                                    rsubclassMst = rsubclassMst.setSubclassordername(subclassname);
                                }

                                final SchregRegdDat regd = SchregRegdDat.getMaxSemesterRegd(student._regdList, year);
                                BigDecimal rcreditMstCredit = null;
                                if (null != regd) {
                                    rcreditMstCredit = CreditMst.getCredit(db2, param, year, regd._grade, regd._coursecd, regd._majorcd, regd._coursecode, classcd, schoolKind, curriculumCd, rsubclasscd);
                                }

                                yomikaeMotoStudyrecList.add(new StudyRec(rSchoolcd, year, annual, rclassMst, rsubclassMst, rcredit, rcompCredit, rcreditMstCredit, rgrades, StudyRec.FLAG_STUDYREC, rvalidFlg));
                            }
                            sr._yomikaeMotoStudyrecList = yomikaeMotoStudyrecList;
                        }
                    }
                    studyRecList.add(sr);
                    sr.setHankiKamokuRowList(param, zenkiList, zenkiIgaiList);
                }
            }

            if (useChairStdDat) {
                final String psKey2 = "PS_STUDYREC_PLUS_CHAIR_STD2";
                if (null == param.getPs(psKey2)) {
                    final String sql = sqlStudyrecNewChairStdDat(param, useChairStdDat);
                    if (param._isOutputDebugQuery) {
                        log.info(" studyrec chairstd sql = " + sql);
                    }
                    param.setPs(psKey2, db2, sql);
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey2), new String[] {student._schregno})) {
                    addStudyrecDatRow(param, dropYears, studyRecList, row);
                }
            }
            if (param._isOutputDebugSeiseki) {
                for (final StudyRec sr : studyRecList) {
                    log.info(" studyrec " + sr);
                }
            }
            return studyRecList;
        }

        private static BigDecimal calcNewGrades(final Param param, final String year, final List<Map<String, String>> rowList) {
            final List<BigDecimal> gradesBigDecimalList = Util.toBigDecimalList(KnjDbUtils.getColumnDataList(rowList, "GRADES"));
            final String gvalCalc = param._schoolMstYearGvalCalcMap.get(year); // 0:平均値、1:単位による重み付け、2:最大値
            final BigDecimal newGrades;
            final double lower = param.getD015Namespare1YearList().contains(year) ? 0 : 1;
            if (rowList.size() == 1) {
                //    stb.append("       ,case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
                newGrades = Util.numberMax(gradesBigDecimalList);
            } else {
                final Integer creditSum = Util.integerSum(KnjDbUtils.getColumnDataList(rowList, "CREDIT"));
                if ("0".equals(gvalCalc)) {
                    //    stb.append("            when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT(case when 0 < T1.GRADES then GRADES end)),0)");
                    final List<BigDecimal> filtered = Util.filterOver(gradesBigDecimalList, lower);
                    if (filtered.size() == 0) {
                        newGrades = Util.numberMax(Util.filterOver(gradesBigDecimalList, 0));
                    } else {
                        newGrades = Util.bdAvg(filtered, 0);
                    }

                } else if ("1".equals(gvalCalc) && null != creditSum && 0 < creditSum.intValue()) {
                    //    stb.append("            when SC.GVAL_CALC = '1' and 0 < SUM(case when 0 < T1.GRADES then CREDIT end) then ROUND(FLOAT(SUM((case when 0 < T1.GRADES then GRADES end) * T1.CREDIT)) / SUM(case when 0 < T1.GRADES then CREDIT end),0)");
                    List<BigDecimal> omomiList = getOmomiList(rowList, lower);
                    if (omomiList.isEmpty() && lower != 0) {
                        omomiList = getOmomiList(rowList, 0); // 下限0で再計算
                    }
                    if (omomiList.isEmpty()) {
                        newGrades = null;
                    } else {
                        newGrades = Util.bdSum(omomiList).divide(new BigDecimal(creditSum.intValue()), 0, BigDecimal.ROUND_HALF_UP);
                    }
                } else {
                    //    stb.append("            else MAX(T1.GRADES) end AS GRADES");
                    newGrades = Util.numberMax(gradesBigDecimalList);
                }
            }
            if (param._isOutputDebugSeiseki && rowList.size() > 1) {
                log.info("     grades = " + newGrades + ", gradesBigDecimalList = " + gradesBigDecimalList + ", gvalCalc = " + gvalCalc);
            }
            return newGrades;
        }

        /**
         *
         * @param rowList
         * @param lower 下限
         */
        private static List<BigDecimal> getOmomiList(final List<Map<String, String>> rowList, final double lower) {
            final List<BigDecimal> omomiList = new ArrayList<BigDecimal>();
            for (int i = 0; i < rowList.size(); i++) {
                final Map<String, String> row = rowList.get(i);
                final String rowGrades = KnjDbUtils.getString(row, "GRADES"); // 評定
                final String rowCredit = KnjDbUtils.getString(row, "CREDIT"); // 単位
                final BigDecimal bdGrades = NumberUtils.isNumber(rowGrades) && lower < Double.parseDouble(rowGrades) ? new BigDecimal(rowGrades) : null;
                final BigDecimal bdCredits = NumberUtils.isNumber(rowCredit) ? new BigDecimal(rowCredit) : null;
                if (null != bdGrades && null != bdCredits) {
                    omomiList.add(bdGrades.multiply(bdCredits));
                }
            }
            return omomiList;
        }

        private static SubclassMst getSubclassMst(final Param param, final Map row, final String schoolKind, final String curriculumCd, final String classcd, final String subclasscd) {
            SubclassMst subclassMst;
            final String subclassMstKey = SubclassMst.key(param, classcd, schoolKind, curriculumCd, subclasscd);
            if ("1".equals(KnjDbUtils.getString(row, "SCHOOLCD"))) {
                subclassMst = AnotherSubclassMst.getAnother(param, param._anotherSubclassMstMap, subclassMstKey);
                if (subclassMst == AnotherSubclassMst.Null) {
                    subclassMst = SubclassMst.get(param, param._subclassMstMap, subclassMstKey);
                }
            } else {
                subclassMst = SubclassMst.get(param, param._subclassMstMap, subclassMstKey);
            }
            return subclassMst;
        }

        private static ClassMst getClassMst(final Param param, final Map row, final String schoolKind, final String classcd) {
            final String classMstKey = ClassMst.key(param, classcd, schoolKind);
            ClassMst classMst;
            if ("1".equals(KnjDbUtils.getString(row, "SCHOOLCD"))) {
                classMst = AnotherClassMst.getAnother(param, param._anotherClassMstMap, classMstKey);
                if (classMst == AnotherClassMst.Null) {
                    classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                }
            } else {
                classMst = ClassMst.get(param, param._classMstMap, classMstKey);
            }
            if (!defstr(classMst._classname).equals(KnjDbUtils.getString(row, "CLASSNAME"))) {
                classMst = classMst.setClassname(KnjDbUtils.getString(row, "CLASSNAME"));
            }
            return classMst;
        }

        private static void addStudyrecDatRow(final Param param, final Set<String> dropYears, final List<StudyRec> studyRecList, final Map<String, String> row) {
            final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
            final String year = KnjDbUtils.getString(row, "YEAR");
            final String annual = KnjDbUtils.getString(row, "ANNUAL");
            final String classcd = KnjDbUtils.getString(row, "CLASSCD");
            final String subClasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            final BigDecimal credit = KnjDbUtils.getBigDecimal(row, "CREDIT", null);
            final BigDecimal compCredit = KnjDbUtils.getBigDecimal(row, "COMP_CREDIT", null);
            final Integer grades = null == KnjDbUtils.getBigDecimal(row, "GRADES", null) ? null : new Integer(Math.round(KnjDbUtils.getBigDecimal(row, "GRADES", null).floatValue()));
            ClassMst classMst = getClassMst(param, row, schoolKind, classcd);
            SubclassMst subclassMst = getSubclassMst(param, row, schoolKind, curriculumCd, classcd, subClasscd);
            if (SubclassMst.Null == subclassMst) {
                return;
            }
            if (!defstr(subclassMst.subclassname()).equals(defstr(KnjDbUtils.getString(row, "SUBCLASSNAME")))) {
                subclassMst = subclassMst.setSubclassordername(KnjDbUtils.getString(row, "SUBCLASSNAME"));
            }

            final String schoolcd;
            final BigDecimal creditMstCredits;
            final String studyFlg;
            final String validFlg;
            final StudyRec sr;
            if (param._is133m) {
                schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                creditMstCredits = null;
                studyFlg = null;
                validFlg = null;
                sr = new StudyRec(schoolcd, year, annual, classMst, subclassMst, credit, compCredit, creditMstCredits, grades, studyFlg, validFlg);
                sr._isDrop = dropYears.contains(year);

            } else {
                schoolcd = "0";
                creditMstCredits = KnjDbUtils.getBigDecimal(row, "CREDIT_MST_CREDITS", null);
                studyFlg = KnjDbUtils.getString(row, "STUDY_FLAG");
                validFlg = KnjDbUtils.getString(row, "VALID_FLG");
                sr = new StudyRec(schoolcd, year, annual, classMst, subclassMst, credit, compCredit, creditMstCredits, grades, studyFlg, validFlg);
                if (isAnotherSchoolYear(sr._year) && !param._seitoSidoYorokuZaisekiMae) {
                    return;
                }
            }
            studyRecList.add(sr);
        }

        private static List<StudyRec> createAbroadStudyrec(final DB2UDB db2, final Student student, final Param param, final PersonalInfo pInfo, final Set<String> dropYears) {
            final String psKey = "ABROAD_STUDYREC";
            if (null == param.getPs(psKey)) {
                final String sql = sqlAbroadCredit(param);

                param.setPs(psKey, db2, sql);
            }
            final TreeMap<String, String> yearAnnualMap = new TreeMap<String, String>(); // 在籍データの年度と年次のマップ
            for (final Gakuseki gaku : pInfo._gakusekiList) {
                if (null != gaku._annual) {
                    yearAnnualMap.put(gaku._year, gaku._annual);
                }
            }
            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno})) {
                if (param._is133m) {
                    final String schoolcd = "AA";
                    final String annual = "AA";
                    final ClassMst classMst = ClassMst.ABROAD;
                    final SubclassMst subclassMst = SubclassMst.ABROAD;

                    final StudyRec sr = new StudyRec(
                            schoolcd,
                            KnjDbUtils.getString(row, "YEAR"),
                            annual,
                            classMst,
                            subclassMst,
                            KnjDbUtils.getBigDecimal(row, "CREDIT", null),
                            BigDecimal.ZERO,
                            null,
                            null,
                            null,
                            null);
                    sr._isDrop = dropYears.contains(sr._year);

                    studyRecList.add(sr);
                } else {
                    final String year = KnjDbUtils.getString(row, "TRANSFER_YEAR");
                    final String annual;
                    if (null != yearAnnualMap.get(year)) {
                        annual = yearAnnualMap.get(year);
                    } else { // 在籍データの範囲外の留学
                        if (param._seitoSidoYorokuZaisekiMae) {
                            final String minYear = yearAnnualMap.isEmpty() || null == yearAnnualMap.firstKey() ? ANOTHER_YEAR : yearAnnualMap.firstKey();
                            if (year.compareTo(minYear) < 0) {
                                final int diffyear = Integer.parseInt(minYear) - Integer.parseInt(year);
                                final int minannual = Integer.parseInt(yearAnnualMap.get(minYear));
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
                        remark = KnjDbUtils.getString(row, "REMARK1");
                    }
                    StudyRec.AbroadStudyRec bef = null;
                    for (final Iterator it = studyRecList.iterator(); it.hasNext();) {
                        final StudyRec.AbroadStudyRec a = (StudyRec.AbroadStudyRec) it.next();
                        if (a._year.equals(year)) {
                            bef = a;
                            it.remove();
                        }
                    }
                    final String sCredit = KnjDbUtils.getString(row, "CREDIT");
                    final BigDecimal credit = NumberUtils.isNumber(sCredit) ? new BigDecimal(sCredit) : null;
                    final StudyRec.AbroadStudyRec studyRec;
                    if (null != bef) {
                        studyRec = new StudyRec.AbroadStudyRec(bef._year, bef._annual, addNumber(credit, bef._credit), bef._remark1);
                    } else {
                        studyRec = new StudyRec.AbroadStudyRec(year, annual, credit, remark);
                    }
                    studyRecList.add(studyRec);
//                        log.debug(" abroad record = " + studyRec);
                }
            }
            for (final StudyRec sr : studyRecList) {
                sr._creditZenkiIgai = sr._credit;
            }
            return studyRecList;
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        public static String pre_sql_Common(final Param param, final String startYear, final Collection<String> creditOnlyClasscdList, final Collection<String> jiritsuKatsudouClasscdList) {
//            log.debug("String pre_sql_Common() ");

            // 教科コードが90より大きい対象
            String classcd90Over = "";
            // 履修未履修をチェックしない
            String noCheckRishuMirishu = "";
            if (!creditOnlyClasscdList.isEmpty()) {
                if (!param._z010.in(Z010.kyoto)) {
                    classcd90Over += " OR T1.CLASSCD IN ('" + Util.mkString(creditOnlyClasscdList, "', '") + "') ";
                    noCheckRishuMirishu += " T1.CLASSCD IN ('" + Util.mkString(creditOnlyClasscdList, "', '") + "') OR ";
                }
            }
            for (final String subclasscd : param._e065Name1JiritsuKatsudouSubclasscdList) {
                if (param._isSubclassOrderNotContainCurriculumcd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    final String classcd = split[0];
                    final String schoolKind = split[1];
                    final String subccd = split[3];
                    classcd90Over += " OR T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.SUBCLASSCD = '" + classcd + "-" + schoolKind + "-" + subccd + "' ";
                    noCheckRishuMirishu += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.SUBCLASSCD = '" + classcd + "-" + schoolKind + "-" + subccd + "' OR ";
                } else if ("1".equals(param._useCurriculumcd)) {
                    classcd90Over += " OR T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' ";
                    noCheckRishuMirishu += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' OR ";
                } else {
                    classcd90Over += " OR T1.SUBCLASSCD = '" + subclasscd + "' ";
                    noCheckRishuMirishu += "   T1.SUBCLASSCD = '" + subclasscd + "' OR ";
                }
            }
            for (final String classcd : jiritsuKatsudouClasscdList) {
                classcd90Over += " OR T1.CLASSCD = '" + classcd + "' ";
                noCheckRishuMirishu += "   T1.CLASSCD = '" + classcd + "' OR ";
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        , T1.CLASSCD ");
                stb.append("        , T1.SCHOOL_KIND ");
                stb.append("        , T1.CURRICULUM_CD ");
            } else {
                stb.append("        , SUBSTR(T1.SUBCLASSCD, 1, 2) AS CLASSCD ");
                stb.append("        , '' AS SCHOOL_KIND ");
                stb.append("        , '' AS CURRICULUM_CD ");
            }
            stb.append("        , T1.SUBCLASSCD, VALUATION AS GRADES ");
            stb.append("        , CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("        , T1.COMP_CREDIT ");
            stb.append("        , T1.CLASSNAME ");
            stb.append("        , T1.SUBCLASSNAME ");
            stb.append("        , T1.SCHOOLCD ");
            if (param._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.append("        , TDET.REMARK1 AS VALID_FLG ");
            } else {
                stb.append("        , CAST(NULL AS VARCHAR(1)) AS VALID_FLG ");
            }
            if ("1".equals(param._useProvFlg)) {
                stb.append("        , L3.PROV_FLG ");
            } else {
                stb.append("        , CAST(NULL AS VARCHAR(1)) AS PROV_FLG ");
            }
            stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
            if ("1".equals(param._useProvFlg)) {
                stb.append("         LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND L3.YEAR = T1.YEAR ");
                stb.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("            AND L3.CLASSCD = T1.CLASSCD ");
                    stb.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND L3.PROV_FLG = '1' ");
            }
            if (param._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.append("         LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT TDET ON TDET.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND TDET.YEAR = T1.YEAR ");
                stb.append("            AND TDET.SCHREGNO = T1.SCHREGNO ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("            AND TDET.CLASSCD = T1.CLASSCD ");
                    stb.append("            AND TDET.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("            AND TDET.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("            AND TDET.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND TDET.SEQ = '002' ");
            }
            stb.append("         LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.ANNUAL ");
            stb.append(" WHERE   EXISTS(SELECT 'X' FROM SCHBASE T2 WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR <= T2.YEAR) ");
            if (null != startYear) {
                stb.append(" AND T1.YEAR >= '" + startYear + "' ");
            }
            stb.append("     AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' " + classcd90Over + ") ");
            if (param._is133m && param._useStudyrecReplaceDat) {
                // SCHOOLCD = '1'は別途読込
                stb.append("      AND T1.SCHOOLCD <> '1' ");
            } else {
                if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherStudyrec))) {
                    stb.append("      AND T1.SCHOOLCD <> '1' ");
                }
            }
            stb.append("     AND VALUE(GDAT.SCHOOL_KIND, '" + param.SCHOOL_KIND + "') = '" + param.SCHOOL_KIND + "' ");
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
            if ("1".equals(param.property(Property.hyoteiYomikaeRadio))) {
                if ("notPrint1".equals(param._hyotei)) {
                    stb.append("         AND (T1.VALUATION IS NULL OR T1.VALUATION <> 1) ");
                }
            }
            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        protected static String sqlReplaceSubclassSubstitution(final Param param, final String startYear, final Collection<String> creditOnlyClasscdList, final Collection<String> jiritsuKatudouClasscdList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4))))");
            stb.append(" , DATA AS(");
            stb.append(pre_sql_Common(param, startYear, creditOnlyClasscdList, jiritsuKatudouClasscdList));
            stb.append(" )");
            stb.append(" ,MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            stb.append("     ,T1.SCHOOL_KIND");
            stb.append("     ,T1.CURRICULUM_CD");
            stb.append("       ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD");
            stb.append("       ,T1.GRADES ");
            stb.append("       ,T1.CREDIT ");
            stb.append("       ,T1.COMP_CREDIT ");
            stb.append("       ,T1.CLASSNAME ");
            stb.append("       ,T1.SUBCLASSNAME ");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE  T1.CREDIT > 0 ");
            stb.append("   AND  T1.PROV_FLG IS NULL ");
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD");
            if (!param._is133m) {
                stb.append(" , T1.STUDYREC_SUBCLASSCD");
            }
            stb.append("     ,T1.SCHOOL_KIND ");
            stb.append("     ,T1.CURRICULUM_CD ");
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       ,SUM(T1.CREDIT) AS CREDIT");
            if (param._is133m) {
                stb.append("       ,MAX(FLOAT(T1.GRADES)) AS GRADES");
                stb.append("       ,'0' AS SCHOOLCD");
            } else {
                stb.append("       ,MAX(T1.GRADES) AS GRADES");
                stb.append("       ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
            }
            stb.append(" FROM DATA2 T1");
            stb.append(" INNER JOIN MAX_SEMESTER T2 ON T1.SCHREGNO = T2.SCHREGNO");
            stb.append("       AND T1.YEAR = T2.YEAR");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD ");
            if (!param._is133m) {
                stb.append(" , T1.STUDYREC_SUBCLASSCD ");
            }
            stb.append("     ,T1.SCHOOL_KIND ");
            stb.append("     ,T1.CURRICULUM_CD ");
            stb.append(" )");

            stb.append(" SELECT  T1.YEAR, T1.ANNUAL ");
            stb.append("       , T_SUBST_.SUBSTITUTION_TYPE_FLG ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T_SUBST_.SUBSTITUTION_CLASSCD");
                stb.append("       , T_SUBST_.SUBSTITUTION_SCHOOL_KIND");
                stb.append("       , T_SUBST_.SUBSTITUTION_CURRICULUM_CD");
            } else {
                stb.append("       , SUBSTR(T_SUBST_.SUBSTITUTION_SUBCLASSCD, 1, 2) AS SUBSTITUTION_CLASSCD");
                stb.append("       , '' AS SUBSTITUTION_SCHOOL_KIND");
                stb.append("       , '' AS SUBSTITUTION_CURRICULUM_CD");
            }
            stb.append("       , T_SUBST_.SUBSTITUTION_SUBCLASSCD ");

            stb.append("       , SUBST_CREM.CREDITS AS SUBSTITUTION_CREDIT_MST_CREDIT");
            stb.append("       , ATTEND_CREM.CREDITS AS ATTEND_CREDIT_MST_CREDIT");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T_SUBST_.ATTEND_CLASSCD");
                stb.append("       , T_SUBST_.ATTEND_SCHOOL_KIND");
                stb.append("       , T_SUBST_.ATTEND_CURRICULUM_CD");
            } else {
                if (param._is133m) {
                    stb.append("       , SUBSTR(T_SUBST_.ATTEND_SUBCLASSCD, 1, 2) AS ATTEND_CLASSCD");
                } else {
                    stb.append("       , SUBSTR(T1.SUBCLASSCD, 1, 2) AS ATTEND_CLASSCD");
                }
                stb.append("       , '' AS ATTEND_SCHOOL_KIND");
                stb.append("       , '' AS ATTEND_CURRICULUM_CD");
            }
            if (param._is133m) {
                stb.append("       , T1.GRADES");
                stb.append("       , T1.SCHOOLCD");
                stb.append("       , T_SUBST_.ATTEND_SUBCLASSCD "); // 代替科目
            } else {
                stb.append("       , T1.SUBCLASSCD AS ATTEND_SUBCLASSCD"); // 代替元科目(表示する行のグループコード科目)
                stb.append("       , T1.CREDIT AS ATTEND_CREDIT"); // 代替元科目修得単位
                stb.append("       , T1.COMP_CREDIT AS ATTEND_COMP_CREDIT"); // 代替元科目履修単位
                stb.append("       , T1.GRADES AS ATTEND_VALUATION"); // 代替元科目評定
            }
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_DAT T_SUBST_ ON T1.YEAR = T_SUBST_.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T_SUBST_.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("        AND T_SUBST_.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T_SUBST_.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            if (param._is133m) {
                stb.append("        AND T_SUBST_.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            } else {
                stb.append("        AND T_SUBST_.ATTEND_SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ");
                stb.append("        AND T_SUBST_.SUBSTITUTION_TYPE_FLG = ? ");
            }
            stb.append(" INNER JOIN MAX_SEMESTER SEM ON SEM.YEAR = T1.YEAR AND SEM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT SUBSTMAJ ON ");
            stb.append("            SUBSTMAJ.YEAR = T_SUBST_.YEAR ");
            stb.append("        AND SUBSTMAJ.SUBSTITUTION_SUBCLASSCD = T_SUBST_.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND SUBSTMAJ.SUBSTITUTION_CLASSCD = T_SUBST_.SUBSTITUTION_CLASSCD ");
                stb.append("        AND SUBSTMAJ.SUBSTITUTION_SCHOOL_KIND = T_SUBST_.SUBSTITUTION_SCHOOL_KIND ");
                stb.append("        AND SUBSTMAJ.SUBSTITUTION_CURRICULUM_CD = T_SUBST_.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.append("        AND SUBSTMAJ.ATTEND_SUBCLASSCD = T_SUBST_.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND SUBSTMAJ.ATTEND_CLASSCD = T_SUBST_.ATTEND_CLASSCD ");
                stb.append("        AND SUBSTMAJ.ATTEND_SCHOOL_KIND = T_SUBST_.ATTEND_SCHOOL_KIND ");
                stb.append("        AND SUBSTMAJ.ATTEND_CURRICULUM_CD = T_SUBST_.ATTEND_CURRICULUM_CD ");
            }
            stb.append("        AND SUBSTMAJ.MAJORCD = REGD.MAJORCD AND SUBSTMAJ.COURSECD = REGD.COURSECD ");
            stb.append("        AND SUBSTMAJ.GRADE = REGD.GRADE AND SUBSTMAJ.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CREDIT_MST ATTEND_CREM ON ");
            stb.append("           ATTEND_CREM.SUBCLASSCD = T_SUBST_.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND ATTEND_CREM.CLASSCD = T_SUBST_.ATTEND_CLASSCD ");
                stb.append("       AND ATTEND_CREM.SCHOOL_KIND = T_SUBST_.ATTEND_SCHOOL_KIND ");
                stb.append("       AND ATTEND_CREM.CURRICULUM_CD = T_SUBST_.ATTEND_CURRICULUM_CD ");
            }
            stb.append("       AND ATTEND_CREM.YEAR = REGD.YEAR ");
            stb.append("       AND ATTEND_CREM.GRADE = REGD.GRADE ");
            stb.append("       AND ATTEND_CREM.COURSECD = REGD.COURSECD ");
            stb.append("       AND ATTEND_CREM.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND ATTEND_CREM.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CREDIT_MST SUBST_CREM ON ");
            stb.append("           SUBST_CREM.SUBCLASSCD = T_SUBST_.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND SUBST_CREM.CLASSCD = T_SUBST_.SUBSTITUTION_CLASSCD ");
                stb.append("       AND SUBST_CREM.SCHOOL_KIND = T_SUBST_.SUBSTITUTION_SCHOOL_KIND ");
                stb.append("       AND SUBST_CREM.CURRICULUM_CD = T_SUBST_.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.append("       AND SUBST_CREM.YEAR = REGD.YEAR ");
            stb.append("       AND SUBST_CREM.GRADE = REGD.GRADE ");
            stb.append("       AND SUBST_CREM.COURSECD = REGD.COURSECD ");
            stb.append("       AND SUBST_CREM.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND SUBST_CREM.COURSECODE = REGD.COURSECODE ");


            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        public static String sqlStudyrecNew(final Param param, final String startYear, final Collection<String> creditOnlyClasscdList, final Collection<String> jiritsuKatsudouClasscdList) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST(? AS VARCHAR(4))))");
            stb.append(pre_sql_Common(param, startYear, creditOnlyClasscdList, jiritsuKatsudouClasscdList));
            stb.append(" ORDER BY SUBCLASSCD ");

            return stb.toString();
        }

        private static String sqlStudyrecNewChairStdDat(final Param param, final boolean useChairStdDatTrue) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_STD AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T3.ANNUAL, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T2.CLASSCD, ");
                stb.append("         T2.SCHOOL_KIND, ");
                stb.append("         T2.CURRICULUM_CD, ");
            } else {
                stb.append("         SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("         '' AS SCHOOL_KIND, ");
                stb.append("         '' AS CURRICULUM_CD, ");
            }
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
            stb.append("         AND T1.SCHREGNO = ? ");
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = T1.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T4.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     WHERE ");
            stb.append("         T3.COMBINED_SUBCLASSCD IS NULL ");
            stb.append("         AND T4.ATTEND_SUBCLASSCD IS NULL ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            stb.append("            T3.COMBINED_CLASSCD AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
                stb.append("            T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            } else {
                stb.append("            '' AS SCHOOL_KIND, ");
                stb.append("            '' AS CURRICULUM_CD, ");
            }
            stb.append("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("            CASE WHEN '2' = MAX(T3.CALCULATE_CREDIT_FLG) THEN SUM(T5.CREDITS) ");
            stb.append("                 ELSE MAX(T6.CREDITS) ");
            stb.append("            END AS CREDITS ");
            stb.append("     FROM CHAIR_STD T1 ");
            stb.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.CLASSCD = T3.ATTEND_CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ");
            }
            stb.append("         AND T5.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ");
            stb.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
            stb.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T6.CLASSCD = T3.COMBINED_CLASSCD ");
                stb.append("         AND T6.SCHOOL_KIND = T3.COMBINED_SCHOOL_KIND ");
                stb.append("         AND T6.CURRICULUM_CD = T3.COMBINED_CURRICULUM_CD ");
            }
            stb.append("         AND T6.SUBCLASSCD = T3.COMBINED_SUBCLASSCD ");
            stb.append("     GROUP BY ");
            stb.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T3.COMBINED_CLASSCD, ");
                stb.append("            T3.COMBINED_SCHOOL_KIND, ");
                stb.append("            T3.COMBINED_CURRICULUM_CD, ");
            }
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
            stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
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
            stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.SUBCLASSCD2 IS NOT NULL ");
            stb.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
            stb.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
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
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" ) ");

            stb.append(" SELECT  '" + StudyRec.FLAG_CHAIR_STD + "' AS STUDY_FLAG ");
            stb.append("       , T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND");
            stb.append("       ,T1.CURRICULUM_CD");
            stb.append("       , T1.SUBCLASSCD");
            stb.append("       , T1.CLASSNAME");
            stb.append("       , T1.SUBCLASSNAME");
            stb.append("       , CAST(NULL AS SMALLINT) AS CREDIT");
            stb.append("       , CAST(NULL AS SMALLINT) AS COMP_CREDIT");
            stb.append("       , T1.CREDITS AS CREDIT_MST_CREDITS");
            stb.append("       , CAST(NULL AS SMALLINT) AS GRADES");
            stb.append("       , CAST(NULL AS VARCHAR(1)) AS SCHOOLCD");
            stb.append("       , CAST(NULL AS VARCHAR(1)) AS VALID_FLG ");
            stb.append("       , T1.SHOWORDERCLASS");
            stb.append("       , T1.SHOWORDERSUBCLASS");
            stb.append("       , T1.SPECIALDIV");
            stb.append(" FROM   CHAIR_STD_SUBCLASS T1 ");
            stb.append(" WHERE  T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("     OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' OR T1.CLASSCD = '94' ");

            return stb.toString();
        }

        /**
         * @return 留学単位のＳＱＬ文を戻します。
         * @see 年度別の単位。(留年の仕様に対応)
         */
        protected static String sqlAbroadCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if (param._is133m) {
                stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4))))");
                stb.append(" SELECT YEAR, SUM(ABROAD_CREDITS) AS CREDIT ");
                stb.append(" FROM(");
                stb.append("      SELECT  ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                stb.append("      FROM    SCHREG_TRANSFER_DAT W1 ");
                stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
                stb.append("          AND TRANSFERCD = '1' ");
                stb.append("     )ST1,");
                stb.append("     (");
                stb.append("      SELECT  YEAR ");
                stb.append("      FROM    SCHREG_REGD_DAT W1 ");
                stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO AND W1.YEAR <= W2.YEAR) ");
                stb.append("      GROUP BY YEAR ");
                stb.append("     )ST2 ");
                stb.append(" WHERE  INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                stb.append("GROUP BY YEAR ");
            } else {
                stb.append(" WITH TRANSFER AS (");
                stb.append("   SELECT W1.SCHREGNO, FISCALYEAR(W1.TRANSFER_SDATE) AS TRANSFER_YEAR, W1.TRANSFER_SDATE, W1.ABROAD_CREDITS ");
                if (param._hasSCHREG_TRANSFER_DAT_REMARK1) {
                    stb.append(" , W1.REMARK1 ");
                }
                stb.append("   FROM SCHREG_TRANSFER_DAT W1 ");
                stb.append("   WHERE ");
                stb.append("     W1.SCHREGNO = ? ");
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
                stb.append("      INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR ");
                stb.append("                                      AND GDAT.GRADE = W1.GRADE ");
                stb.append("                                      AND GDAT.SCHOOL_KIND = '" + param.SCHOOL_KIND + "' ");
                stb.append("      WHERE W1.YEAR <= '" + param._year + "' ");
                stb.append("      GROUP BY W1.YEAR, W1.SCHREGNO ");
                stb.append("   ) ST2 ON ST2.SCHREGNO = W1.SCHREGNO ");
                stb.append("        AND ST2.YEAR = W1.TRANSFER_YEAR ");
                stb.append("ORDER BY W1.TRANSFER_SDATE ");
            }
            return stb.toString();
        }

        public static boolean isNotPrint(final Param param, final StudyRec studyrec, final YOSHIKI yoshiki) {
            return SCHOOLCD1.equals(studyrec._schoolcd) && (param._z010.in(Z010.kyoto) || param._z010.in(Z010.sagaken)) && yoshiki == YOSHIKI._2_OMOTE;
        }

        // 133M

        /**
         * 未修得か
         * @return 未修得ならtrue
         */
        public boolean isMishutoku(final Param param) {
            // 修得単位数が0かnull または 履修単位数が0がnull
            return 0 == intVal(_credit, 0) || 0 == intVal(_compCredit, 0);
        }

        public static List<BigDecimal> getTanniList(final int TANNI_DIV, final List<StudyRec> studyrecList, final Param param, final YOSHIKI yoshiki) {
            List<BigDecimal> list = new ArrayList<BigDecimal>();
            for (final StudyRec sr : studyrecList) {
                if (TANNI_DIV == TANNI_DIV_CREDIT) {
                    if (StudyRec.isNotPrint(param, sr, yoshiki)) {
                        continue;
                    }
                    if (null != sr._credit) {
                        list.add(sr._credit);
                    }
                } else if (TANNI_DIV == TANNI_DIV_SATEI) {
                    if (null != sr._sateiCredit) {
                        list.add(sr._sateiCredit);
                    }
                }
            }
            return list;
        }

        protected boolean sateiNasi(final Param param, final YOSHIKI yoshiki) {
            return SCHOOLCD1.equals(_schoolcd) && null == subclassMst(param, yoshiki)._classcd && null == subclassMst(param, yoshiki)._subclasscd && ("1".equals(param._useCurriculumcd) && null == subclassMst(param, yoshiki)._schoolKind && null == subclassMst(param, yoshiki)._curriculumCd || !"1".equals(param._useCurriculumcd));
        }

        /**
         * 学習記録データクラスを作成し、リストに加えます。
         * @param db2
         */
        static List<StudyRec> loadReplace(final KNJA130C.Student student, final DB2UDB db2, final Param param) {
            if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherStudyrec))) {
                log.debug("前籍校の成績読み込み無し");
                return Collections.emptyList();
            }
            if (!param._useStudyrecReplaceDat) {
                return Collections.emptyList();
            }

            final String psKey = "PS_KEY_STUDYREC_REPLACE";
            if (null == param.getPs(psKey)) {
                final String sql = sqlStudyrecReplace(param);
                if (param._isOutputDebugQuery) {
                    log.info(" studyrec replace sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }

            final List<StudyRec> replace = new ArrayList<StudyRec>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {student._schregno})) {

                final String schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                final String year;
                final String annual;
                if (param._z010.in(Z010.tokyoto)) { // 東京都はSCHOOLCD = '1'（前籍高）を「入学前」として扱う
                    year = ANOTHER_YEAR;
                    annual = "00";
                } else {
                    year = KnjDbUtils.getString(row, "YEAR");
                    annual = KnjDbUtils.getString(row, "ANNUAL");
                }
                final String anotherElectdiv = KnjDbUtils.getString(row, "ELECTDIV");
                final String anotherSpecialdiv = KnjDbUtils.getString(row, "SPECIALDIV");
                final String anotherClasscd = KnjDbUtils.getString(row, "CLASSCD");
                final String anotherSchoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String anotherCurriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String anotherSubclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                final String anotherClassname = KnjDbUtils.getString(row, "ANOTHER_CLASSNAME");
                final String anotherSubclassname = KnjDbUtils.getString(row, "ANOTHER_SUBCLASSNAME");
                final Integer anotherShoworderClass = Integer.valueOf(KnjDbUtils.getString(row, "ANOTHER_SHOWORDER_CLASS"));
                final Integer anotherShoworderSubclass = Integer.valueOf(KnjDbUtils.getString(row, "ANOTHER_SHOWORDER_SUBCLASS"));
                final BigDecimal credit = KnjDbUtils.getBigDecimal(row, "CREDIT", null);
                final Integer valuation = null == KnjDbUtils.getString(row, "VALUATION") ? null : new Integer(Math.round(Double.valueOf(KnjDbUtils.getString(row, "VALUATION")).floatValue()));

                String classname;
                if (null == anotherClassname) {
                    log.info("class name null:" + anotherClasscd + "-" + anotherSchoolKind);
                    classname = "";
                } else {
                    classname = anotherClassname;
                }
                final ClassMst classMst = new ClassMst(anotherClasscd, anotherSchoolKind, classname, anotherElectdiv, anotherSpecialdiv, anotherShoworderClass);
                final String subclassname;
                if (null == anotherSubclassname) {
                    log.info("subclass name null:" + classMst._classcd + "-" + classMst._schoolKind + "-" + anotherCurriculumCd + "-" + anotherSubclasscd);
                    subclassname = "";
                } else {
                    subclassname = anotherSubclassname;
                }
                final SubclassMst subclassMst = new SubclassMst(anotherClasscd, anotherSchoolKind, anotherCurriculumCd, anotherSubclasscd, subclassname, null, anotherShoworderSubclass, null);

                final StudyRec sr = new StudyRec(schoolcd, year, annual, classMst, subclassMst, credit, null, null, valuation, null, null);
                replace.add(sr);

                sr._classMstSaki = new ClassMst(KnjDbUtils.getString(row, "CLASSCD_SAKI"), KnjDbUtils.getString(row, "SCHOOL_KIND_SAKI"), KnjDbUtils.getString(row, "CLASSNAME_SAKI"), KnjDbUtils.getString(row, "ELECTDIV_SAKI"), KnjDbUtils.getString(row, "SPECIALDIV_SAKI"), Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDER_CLASS")));
                sr._subclassMstSaki = new SubclassMst(KnjDbUtils.getString(row, "CLASSCD_SAKI"), KnjDbUtils.getString(row, "SCHOOL_KIND_SAKI"), KnjDbUtils.getString(row, "CURRICULUM_CD_SAKI"), KnjDbUtils.getString(row, "SUBCLASSCD_SAKI"), KnjDbUtils.getString(row, "SUBCLASSNAME_SAKI"), null, Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDER_SUBCLASS")), null);
                sr._compCredit = KnjDbUtils.getBigDecimal(row, "COMP_CREDIT", null);
                sr._sateiCredit = KnjDbUtils.getBigDecimal(row, "SATEI_CREDIT", null);

                if (null != KnjDbUtils.getString(row, "FORMER_REG_SCHOOLCD")) {
                    student._formerRegSchoolcd = KnjDbUtils.getString(row, "FORMER_REG_SCHOOLCD");
                    if (null != KnjDbUtils.getString(row, "FINSCHOOL_NAME")) {
                        student._formerRegSchoolFinschoolname = KnjDbUtils.getString(row, "FINSCHOOL_NAME");
                    }
                }
            }

            return replace;
        }

        public static StudyRec union(final Param param, final String year, final List<StudyRec> yearStudyrecList) {
            List<Integer> valuationList = new ArrayList<Integer>();
            List<BigDecimal> creditList = new ArrayList<BigDecimal>();
            List<BigDecimal> compCreditList = new ArrayList<BigDecimal>();
            List<BigDecimal> creditMstCreditList = new ArrayList<BigDecimal>();
            String validFlg = null;

            List<Integer> valuationZenkiList = new ArrayList<Integer>();
            List<BigDecimal> creditZenkiList = new ArrayList<BigDecimal>();
            List<BigDecimal> compCreditZenkiList = new ArrayList<BigDecimal>();
            List<Integer> valuationZenkiIgaiList = new ArrayList<Integer>();
            List<BigDecimal> creditZenkiIgaiList = new ArrayList<BigDecimal>();
            List<BigDecimal> compCreditZenkiIgaiList = new ArrayList<BigDecimal>();

            StudyRec head = null;
            for (final StudyRec studyrec : yearStudyrecList) {
                if (null == head) {
                    head = studyrec;
                }
                addNotNull(valuationList, studyrec._valuation);
                addNotNull(creditList, studyrec._credit);
                addNotNull(compCreditList, studyrec._compCredit);
                addNotNull(creditMstCreditList, studyrec._creditMstCredits);
                addNotNull(valuationZenkiList, studyrec._valuationZenki);
                addNotNull(creditZenkiList, studyrec._creditZenki);
                addNotNull(compCreditZenkiList, studyrec._compCreditZenki);
                addNotNull(valuationZenkiIgaiList, studyrec._valuationZenkiIgai);
                addNotNull(creditZenkiIgaiList, studyrec._creditZenkiIgai);
                addNotNull(compCreditZenkiIgaiList, studyrec._compCreditZenkiIgai);
                if (null != studyrec._validFlg) {
                    validFlg = studyrec._validFlg;
                }
            }
            Integer valuation = max(valuationList);
            BigDecimal credit = Util.bdSum(creditList);
            BigDecimal compCredit = Util.bdSum(compCreditList);
            Integer valuationZenki = max(valuationZenkiList);
            BigDecimal creditMstCredits = Util.bdSum(creditMstCreditList);
            BigDecimal creditZenki = Util.bdSum(creditZenkiList);
            BigDecimal compCreditZenki = Util.bdSum(compCreditZenkiList);
            Integer valuationZenkiIgai = max(valuationZenkiIgaiList);
            BigDecimal creditZenkiIgai = Util.bdSum(creditZenkiIgaiList);
            BigDecimal compCreditZenkiIgai = Util.bdSum(compCreditZenkiIgaiList);

            StudyRec s = new StudyRec(null, year, null == head ? null : head._annual, null == head ? null : head._classMst, null == head ? null : head._subclassMst,
                    credit,
                    compCredit, creditMstCredits,
                    valuation,
                    null, validFlg);
            s._valuationZenki = valuationZenki;
            s._creditZenki = creditZenki;
            s._compCreditZenki = compCreditZenki;
            s._valuationZenkiIgai = valuationZenkiIgai;
            s._creditZenkiIgai = creditZenkiIgai;
            s._compCreditZenkiIgai = compCreditZenkiIgai;
            return s;
        }

        private static <T> void addNotNull(final List<T> list, final T o) {
            if (null != o) {
                list.add(o);
            }
        }
    }

    /**
     * <<学習記録データ科目別単位数のクラス>>。 学習記録データクラスを科目別に集計しました。
     */
    private static class StudyRecSubclassTotal {

        private static class Comparator implements java.util.Comparator<StudyRecSubclassTotal> {
            private final Param _param;
            private YOSHIKI _yoshiki;
            Comparator(final Param param, final YOSHIKI yoshiki) {
                _yoshiki = yoshiki;
                _param = param;
            }
            /**
             * {@inheritDoc}
             */
            public int compare(final StudyRecSubclassTotal t1, final StudyRecSubclassTotal t2) {
                int rtn = 0;
                rtn = ClassMst.compareOrder(_param, t1.classMst(_param, _yoshiki), t2.classMst(_param, _yoshiki));
                if (0 != rtn) { return rtn; }
                rtn = SubclassMst.compareOrder(_param, t1.subclassMst(_param, _yoshiki), t2.subclassMst(_param, _yoshiki));
                return rtn;
            }
        }

        final Param _param;
        final PersonalInfo _pInfo;
        final List<StudyRec> _studyrecList;
        final Collection<String> _dropYears;
        final Collection<String> _enabledDropYears;
        final Collection<String> _printDropRegdYears;

        /**
         * コンストラクタ。
         *
         * @param rs
         */
        private StudyRecSubclassTotal(final List<StudyRec> studyrecList, final Collection<String> dropYears, final Collection<String> enabledDropYears, final Collection<String> printDropRegdYears, final Param param, final PersonalInfo pInfo) {
            _param = param;
            _pInfo = pInfo;
            _studyrecList = studyrecList;
            _dropYears = dropYears;
            _enabledDropYears = enabledDropYears;
            _printDropRegdYears = printDropRegdYears;
        }

        /**
         * 年度とその年度の単位のマップを返す
         * @param kind <br><code>StudyRec.KIND_CREDIT</code>: 修得単位数<br><code>StudyRec.KIND_COMP_CREDIT</code>: 履修単位数<br><code>StudyRec.KIND_CREDIT_MSTCREDIT</code>: 単位マスタの単位数<br>
         * @param checkDropYears
         * @return
         */
        private List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> creditKindYearMapList(final Param param, final StudyRec.CreditKind kind, final Collection<String> enabledDropYears, final int checkDropYears, final boolean isDebug) {
            //final List<StudyRec> targetStudyRecList = Util.filter(StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _printDropRegdYears, enabledDropYears, checkDropYears), new StudyRecCreditTargetPredicate(param, _pInfo));
            final List<StudyRec> targetStudyRecList = Util.filter(StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _printDropRegdYears, enabledDropYears, checkDropYears), null);
            final List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> rtn = creditForTotalMapList(param, kind, targetStudyRecList);
            if (param._isOutputDebugSeiseki) {
                if (isDebug) {
                    log.info(kind + " " + studyrec() + ", targetStudyRecList = " + targetStudyRecList + ", rtn = " + rtn);
                }
            }
            return rtn;
        }

        private List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> creditForTotalMapList(final Param param, final StudyRec.CreditKind kind, List<StudyRec> studyRecList) {
            final List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> rtn = new ArrayList<Map<String, Map<StudyRec.CreditKind, BigDecimal>>>();
            for (final StudyRec sr : studyRecList) {
                final Map<StudyRec.CreditKind, BigDecimal> creditForTotal = sr.creditForTotal(param, kind);
                if (!creditForTotal.isEmpty()) {
                    final Map<String, Map<StudyRec.CreditKind, BigDecimal>> m = new HashMap<String, Map<StudyRec.CreditKind, BigDecimal>>();
                    m.put(sr._year, creditForTotal);
                    rtn.add(m);
                }
            }
            return rtn;
        }

        public <K, V> List<V> mapValues(final List<Map<K, V>> list) {
            final ArrayList<V> vals = new ArrayList<V>();
            for (final Map<K, V> m : list) {
                vals.addAll(m.values());
            }
            return vals;
        }

        public List<BigDecimal> kindListForTotal(final Param param, final StudyRec.CreditKind kind, final Collection<String> enabledDropYears) {
            return mapValues(mapValues(creditKindYearMapList(param, kind, enabledDropYears, 1, false)));
        }

        public List<BigDecimal> compCreditListForTotal(final Param param, final Collection<String> enabledDropYears) {
            return kindListForTotal(param, StudyRec.CreditKind.COMP_CREDIT, enabledDropYears);
        }

        public List<BigDecimal> creditMstCreditListForTotal(final Param param, final Collection<String> enabledDropYears) {
            return kindListForTotal(param, StudyRec.CreditKind.CREDIT_MSTCREDIT, enabledDropYears);
        }

        public List<Map<StudyRec.CreditKind, BigDecimal>> bunkatuRishuCreditMapListForTotal(final Param param) {
            return mapValues(creditKindYearMapList(param, StudyRec.CreditKind.BUNKATSU_RISHU_CREDIT, null, 1, true));
        }

        /**
         * デバッグ用
         * @return
         */
        private List<String> getCreditInfoList() {
            final List<String> rtn = new ArrayList<String>();
            for (final StudyRec sr : _studyrecList) {
                final String dropped = _dropYears.contains(sr._year) && !_printDropRegdYears.contains(sr._year) ? ",dropped" + (null != sr._validFlg ? ",valid" : "") : "";
                rtn.add("[" + sr._year + "," + sr._credit + "" + dropped + "]");
            }
            return rtn;
        }

        /**
         * デバッグ用
         * @return
         */
        private List<String> compCreditInfoList() {
            final List<String> rtn = new ArrayList<String>();
            for (final StudyRec sr : _studyrecList) {
                final String dropped = _dropYears.contains(sr._year) && !_printDropRegdYears.contains(sr._year) ? ",dropped" + (null != sr._validFlg ? ",valid" : "") : "";
                rtn.add("[" + sr._year + "," + sr._compCredit + "" + dropped + "]");
            }
            return rtn;
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        private boolean isRishuNomi(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _studyrecList) {
                if (!sr.isRishuNomi(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        public boolean isMirishu(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _studyrecList) {
                if (!sr.isMirishu(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        public String creditInfo(final Param param) {
            final List creditList = getCreditInfoList();
            final List compCreditList = compCreditInfoList();
            final String compCreditInfo = " " + (Util.bdSum(compCreditListForTotal(param, _enabledDropYears)) != null ? (" compcredit = " + compCreditList.toString()) : "");
            return (Util.bdSum(kindListForTotal(param, StudyRec.CreditKind.CREDIT, _enabledDropYears)) != null && Util.bdSum(kindListForTotal(param, StudyRec.CreditKind.CREDIT, _enabledDropYears)).intValue() == 0) ? compCreditInfo : (" credit = " + creditList.toString());
        }

        public StudyRec studyrec() {
            List<StudyRec> tgtStudyrecList = StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _printDropRegdYears, _enabledDropYears, 1);
            if (tgtStudyrecList.size() == 0) {
                tgtStudyrecList = _studyrecList;
            }
            return tgtStudyrecList.get(tgtStudyrecList.size() - 1);
        }

        public ClassMst classMst(final Param param, final YOSHIKI yoshiki) {
            return studyrec().classMst(param, yoshiki);
        }

        public SubclassMst subclassMst(final Param param, final YOSHIKI yoshiki) {
            return studyrec().subclassMst(param, yoshiki);
        }

        /**
         * データが全て非対象か
         * @return
         *   全てのレコードが原級留置の年度に含まれるが、有効のフラグがある場合、false <br>
         *   全てのレコードが原級留置の年度に含まれ、有効のフラグもない場合、true<br>
         *   それ以外の場合、false (前提条件:レコードは1つ以上存在する)<br>
         */
        public boolean isAllNotTarget() {
            return StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _printDropRegdYears, _enabledDropYears, 1).size() == 0 ? true : false;
        }

        private static boolean isEnabledJiritsu(final Param param, final StudyRecSubclassTotal sst) {
            if (param._isOutputDebug) {
                log.info(" StudyRecSubclassTotal " + sst + " isMirisyu? " + !param._isPrintMirisyu + ", " + sst.isMirishu(param) + ", isRisyuNomi? " + !param._isPrintRisyuNomi + ", " + sst.isRishuNomi(param));
            }
            final boolean isNotTarget = !param._isPrintMirisyu && sst.isMirishu(param) || !param._isPrintRisyuNomi && sst.isRishuNomi(param);
            return !isNotTarget;
        }

//        /**
//         * {@inheritDoc}
//         */
//        public String toString() {
//            return "[" + classcd() + ":" + subClasscd() + " " + creditInfo() + " (" + className() + ":" + subClassName() + ") ]";
//        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            if (_param._is133m) {
                return "[" + classMst(_param, YOSHIKI.NONE).getKey(_param) + ":" + subclassMst(_param, YOSHIKI.NONE).getKey(_param) + " " + creditInfoM() + " (" + classMst(_param, YOSHIKI.NONE)._classname + ":" + subclassMst(_param, YOSHIKI.NONE).subclassname() + ") ]";
            }
            return "StudyRecSubclassTotal(" + classMst(_param, YOSHIKI.NONE).getKey(_param) + ":" + subclassMst(_param, YOSHIKI.NONE).getKey(_param) + " " + creditInfo(_param) + ")";
        }

        /// KNJA 133M
        public List<BigDecimal> kindCreditsM(final StudyRec.CreditKind kind, final YOSHIKI yoshiki) {
            final List<BigDecimal> credits = new ArrayList<BigDecimal>();
            final List<BigDecimal> creditsSagaA = new ArrayList<BigDecimal>();
            final List<BigDecimal> creditsSagaB = new ArrayList<BigDecimal>();
            final List<BigDecimal> sateiCredits = new ArrayList<BigDecimal>();
            final List<BigDecimal> compCredits = new ArrayList<BigDecimal>();
            for (final StudyRec sr : _studyrecList) {
                if (!_dropYears.contains(sr._year)) {
                    if (_param._useStudyrecReplaceDat) {
                        if (yoshiki == YOSHIKI._1_URA) {
                            if (null != sr._credit) {
                                credits.add(sr._credit);
                                if (SCHOOLCD1.equals(sr._schoolcd)) {
                                    creditsSagaA.add(sr._credit);
                                } else {
                                    creditsSagaB.add(sr._credit);
                                }
                            }
                        } else if (yoshiki == YOSHIKI._2_OMOTE) {
                            if (SCHOOLCD1.equals(sr._schoolcd)) {
                                if (null != sr._sateiCredit) {
                                    sateiCredits.add(sr._sateiCredit);
                                    credits.add(sr._sateiCredit);
                                }
                            } else {
                                if (null != sr._credit) {
                                    credits.add(sr._credit);
                                }
                            }
                        }
                    } else {
                        if (null != sr._credit) {
                            credits.add(sr._credit);
                            if (SCHOOLCD1.equals(sr._schoolcd)) {
                                creditsSagaA.add(sr._credit);
                            } else {
                                creditsSagaB.add(sr._credit);
                            }
                        }
                    }
                }
            }
            if (kind == StudyRec.CreditKind.CREDIT) {
                return credits;
            } else if (kind == StudyRec.CreditKind.COMP_CREDIT) {
                return compCredits;
            } else if (kind == StudyRec.CreditKind.SATEI_CREDIT) {
                return sateiCredits;
            } else if (kind == StudyRec.CreditKind.CREDIT_SAGA_A) {
                return creditsSagaA;
            } else if (kind == StudyRec.CreditKind.CREDIT_SAGA_B) {
                return creditsSagaB;
            }
            return Collections.emptyList();
        }

        private List<String> creditListM() {
            final List<String> rtn = new ArrayList<String>();
            for (final StudyRec sr : _studyrecList) {
                final String dropped = _dropYears.contains(sr._year) && !_printDropRegdYears.contains(sr._year) ? ",dropped" : "";
                rtn.add("[" + sr._year + "," + sr._credit + "" + dropped + "]");
            }
            return rtn;
        }

        private List<String> compCreditListM() {
            final List<String> rtn = new ArrayList<String>();
            for (final StudyRec sr : _studyrecList) {
                final String dropped = _dropYears.contains(sr._year) && !_printDropRegdYears.contains(sr._year) ? ",dropped" : "";
                rtn.add("[" + sr._year + "," + sr._compCredit + "" + dropped + "]");
            }
            return rtn;
        }

        /**
         * 未修得か
         * @return 未修得ならtrue
         */
        private boolean isMishutokuM(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _studyrecList) {
                if (!sr.isMishutoku(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        public String creditInfoM() {
            final List creditList = creditListM();
            final List compCreditList = compCreditListM();
            final String compCreditInfo = " " + (!kindCreditsM(StudyRec.CreditKind.COMP_CREDIT, YOSHIKI.NONE).isEmpty() ? (" compcredit = " + compCreditList.toString()) : "");
            return kindCreditsM(StudyRec.CreditKind.CREDIT, YOSHIKI.NONE).isEmpty() ? compCreditInfo : (" credit = " + creditList.toString());
        }

        public boolean isAllDroppedM() {
            boolean isAllDropped = true;
            for (final StudyRec sr : _studyrecList) {
                if (!_dropYears.contains(sr._year) || _printDropRegdYears.contains(sr._year)) {
                    isAllDropped = false;
                }
            }
            return isAllDropped;
        }
    }

    private static class StudyRecCreditTargetPredicate implements Predicate<StudyRec> {
        final Param _param;
        final PersonalInfo _pInfo;
        final int _targetFlg; // 0:年度、1:科目
        public StudyRecCreditTargetPredicate(final Param param, final PersonalInfo pInfo, final int targetFlg) {
            _param = param;
            _pInfo = pInfo;
            _targetFlg = targetFlg;
        }
        public boolean test(final StudyRec s) {
            if (_param._z010.in(Z010.aoyama)) {
                final AoyamaGakunenSeiseki.Judge[] judges = _pInfo._aoyamaGakunenSeiseki.getJudges(s);
                if (0 == _targetFlg) {
                    return AoyamaGakunenSeiseki.Judge.isCreditNendoTotalTarget(judges);
                } else if (1 == _targetFlg) {
                    return AoyamaGakunenSeiseki.Judge.isCreditSubclassTotalTarget(judges);
                } else {
                    throw new IllegalArgumentException(" _targetFlg = " + _targetFlg);
                }
            }
            return true;
        }
    }

    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRecSubstitution extends StudyRec implements RemarkContainer {

        private final GakushuBiko.DAITAI_TYPE _daitaiType;

        private List<SubstitutionAttendSubclass> _attendSubclasses = new ArrayList<SubstitutionAttendSubclass>();

        /**
         * コンストラクタ。
         */
        private StudyRecSubstitution(final GakushuBiko.DAITAI_TYPE daitaiType, final String schoolcd, final String year, final String annual, final ClassMst classMst, final SubclassMst subclassMst,
                final BigDecimal credit, final Integer valuation, final String studyFlg, final Set<String> dropYears) {
            super(schoolcd, year, annual, classMst, subclassMst, credit, null, null, valuation, studyFlg, null);
            _daitaiType = daitaiType;
            if (null != dropYears) {
                _isDrop = dropYears.contains(year);
            }
        }

        private String daitaiName(final boolean isFullname) {
            return GakushuBiko.DAITAI_TYPE.ICHIBU.equals(_daitaiType) ? "一部代替" : (isFullname ? "全部代替" : "代替");
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考データを得る。
         * @return
         */
        private Tuple<List<SubstitutionAttendSubclass>, BigDecimal> getBikoSubstitutionInfo(final KNJA130CCommon.Param param, final String year) {
            final Set<String> addedKey = new HashSet<String>();

            final List<BigDecimal> totalAttendCredit = new ArrayList<BigDecimal>();
            final List<SubstitutionAttendSubclass> attendSubclasses = new ArrayList<SubstitutionAttendSubclass>();

            for (final SubstitutionAttendSubclass attendSubclass : _attendSubclasses) {

                final String keyCd = attendSubclass._attendSubclassMst.getKey(param);
                if (!addedKey.contains(keyCd) && (year == null || year.equals(attendSubclass._attendyear))) {
                    attendSubclasses.add(attendSubclass);
                    addedKey.add(keyCd);
                }
                if (null != attendSubclass._attendCreditMstCredit) {
                    totalAttendCredit.add(attendSubclass._attendCreditMstCredit);
                }
            }
            return Tuple.of(attendSubclasses, Util.bdSum(totalAttendCredit));
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考文字列を得る。
         * @return
         */
        public Map<String, String> getBikoSubstitutionTemplateDataMap(final KNJA130CCommon.Param param) {
            final Tuple<List<SubstitutionAttendSubclass>, BigDecimal> bikoCredits = getBikoSubstitutionInfo(param, null);
            final List<String> subclassnames = new ArrayList<String>();
            for (final SubstitutionAttendSubclass s : bikoCredits._first) {
                subclassnames.add(s._attendSubclassMst.subclassname());
            }
            final BigDecimal totalAttendCredit = bikoCredits._second;
            final Map<String, String> map = new HashMap<String, String>();
            map.put("{代替先科目}", defstr(_subclassMst.subclassname()));
            map.put("{代替元科目}", Util.mkString(subclassnames, "、").toString());
            map.put("{代替単位}", defstr(totalAttendCredit, " "));
            map.put("{代替名}", daitaiName(false));
            map.put("{代替名2}", daitaiName(true));
            return map;
        }

        @Override
        public String getRemark(final KNJA130CCommon.Param param) {
            // 「修得単位の記録」「学習の記録」の代替科目備考文字列を得る。
            return param._substRemarkNot90Template.format(getBikoSubstitutionTemplateDataMap(param));
        }

        /**
         * 「活動の記録用」の代替科目備考文字列を得る。
         * @return
         */
        public Map<String, String> getBikoSubstitution90TemplateDataMap(final String year, final KNJA130CCommon.Param param) {
            final Tuple<List<SubstitutionAttendSubclass>, BigDecimal> bikoCredits = getBikoSubstitutionInfo(param, year);
            final List<String> subclassnames = new ArrayList<String>();
            for (final SubstitutionAttendSubclass s : bikoCredits._first) {
                subclassnames.add(defstr(s._attendClassMst._classname) + "・" + defstr(s._attendSubclassMst.subclassname()));
            }

            Map<String, String> rtn = new HashMap<String, String>();
            if (subclassnames.size() != 0) {
                rtn.put("{代替先科目}", defstr(_subclassMst.subclassname()));
                rtn.put("{代替元科目}", Util.mkString(subclassnames, "、").toString());
                rtn.put("{代替名}", daitaiName(false));
            }
            return rtn;
        }

        /**
         * 履修科目の最大年度を得る
         * @return 履修科目の最大年度
         */
        public String getMaxAttendSubclassYear() {
            return getMaxAttendSubclassYear(_attendSubclasses);
        }


        /**
         * 履修科目の最大年度を得る
         * @return 履修科目の最大年度
         */
        public static String getMaxAttendSubclassYear(final List<SubstitutionAttendSubclass> attendSubclasses) {
            String maxyear = null;
            for (final SubstitutionAttendSubclass array : attendSubclasses) {
                if (maxyear == null || array._attendyear != null && maxyear.compareTo(array._attendyear) < 0) {
                    maxyear = array._attendyear;
                }
            }
            return maxyear;
        }

        public String toString() {
            return "StudyRecSubstitution(" + super.toString() + ", attendSubclasses = \n" + Util.listString(_attendSubclasses, 0) + ")";
        }

        protected static class SubstitutionAttendSubclass {
            final String _attendyear;
            final BigDecimal _substitutionCreditMstCredit;
            final ClassMst _attendClassMst;
            final SubclassMst _attendSubclassMst;
            final BigDecimal _attendCreditMstCredit;
            final BigDecimal _attendCredit;
            final BigDecimal _attendCompCredit;
            final Integer _attendValuation;
            public SubstitutionAttendSubclass(final String attendyear, final BigDecimal substitutionCreditMstCredit, final ClassMst attendClassMst, final SubclassMst attendSubclassMst, final BigDecimal attendCredit, final BigDecimal attendCompCredit, final Integer attendValuation, final BigDecimal attendCreditMstCredit) {
                _attendyear = attendyear;
                _substitutionCreditMstCredit = substitutionCreditMstCredit;
                _attendClassMst = attendClassMst;
                _attendSubclassMst = attendSubclassMst;
                _attendCredit = attendCredit;
                _attendCompCredit = attendCompCredit;
                _attendValuation = attendValuation;
                _attendCreditMstCredit = attendCreditMstCredit;
            }
            public String toString() {
                return "SubstitutionAttendSubclass(year=" + _attendyear + ", class = " + _attendClassMst + ", subclass = " + _attendSubclassMst + ", credit = " + _attendCredit + ", substitutionCreditMstCredit = " + _substitutionCreditMstCredit + ") ";
            }
        }
    }

    /**
     * 青山学院 成績判定
     */
    private static class AoyamaGakunenSeiseki {

        private static enum Judge {
            _1_学年成績3かつ進級卒業できた,
            _2_学年成績3かつ進級卒業できなかった,
            _3_学年成績3かつ学年途中,
            _4_学年成績3かつ1学期退学,
            _5_評定未入力,
            _6_欠課時数3分の1超成績3超,
            _6_欠課時数3分の1超成績3以下,
            _7_欠課時数2分の1超
            ;

            public String toString() {
                return super.name();
            }
            private static BigDecimal credit(final Judge[] judges, final StudyRec s) {
                if (containsAny(judges, new Judge[] {
                        _2_学年成績3かつ進級卒業できなかった
                      , _3_学年成績3かつ学年途中
                      , _4_学年成績3かつ1学期退学
                      , _5_評定未入力
                      , _6_欠課時数3分の1超成績3超
                      , _6_欠課時数3分の1超成績3以下
                      , _7_欠課時数2分の1超
                })) {
                    return s._compCredit;
                }
                return s._credit;
            }
            /**
             * 単位数欄表示
             * @param judges
             * @param s
             * @return
             */
            public static String getPrintCredit(final Judge[] judges, final StudyRec s) {
                if (containsAny(judges, new Judge[] { _7_欠課時数2分の1超 })) {
                    return "-/" + defstr(credit(judges, s));
                }
                if (containsAny(judges, new Judge[] { _5_評定未入力 })) {
                    return "(" + defstr(credit(judges, s), " ") + ")";
                }
                if (containsAny(judges, new Judge[] { _3_学年成績3かつ学年途中, _4_学年成績3かつ1学期退学, _6_欠課時数3分の1超成績3超, _6_欠課時数3分の1超成績3以下 })) {
                    return "(" + defstr(credit(judges, s), " ") + ")";
                }
                if (containsAny(judges, new Judge[] { _2_学年成績3かつ進級卒業できなかった })) {
                    return "0/" + defstr(credit(judges, s));
                }
                return defstr(credit(judges, s));
            }
            /**
             * 横計に含めるか
             * @param judges
             * @return
             */
            public static boolean isCreditSubclassTotalTarget(final Judge[] judges) {
                if (containsAny(judges, new Judge[] {
                        _2_学年成績3かつ進級卒業できなかった
                      , _3_学年成績3かつ学年途中
                      , _4_学年成績3かつ1学期退学
                      , _5_評定未入力
                      , _6_欠課時数3分の1超成績3超
                      , _6_欠課時数3分の1超成績3以下
                      , _7_欠課時数2分の1超
                })) {
                    return false;
                }
                return true;
            }
            /**
             * 縦計に含めるか
             * @param judges
             * @return
             */
            public static boolean isCreditNendoTotalTarget(final Judge[] judges) {
                if (containsAny(judges, new Judge[] {
                        _2_学年成績3かつ進級卒業できなかった
                      , _7_欠課時数2分の1超
                })) {
                    return false;
                }
                return true;
            }
            /**
             * 赤字表示か
             * @param judges
             * @return
             */
            public static boolean isHyoteiColorRed(final Judge[] judges) {
                return containsAny(judges, new Judge[] {
                        _1_学年成績3かつ進級卒業できた
                      , _2_学年成績3かつ進級卒業できなかった
                      , _3_学年成績3かつ学年途中
                });
            }
            private static boolean containsAny(final Judge[] judges, final Judge[] tgt) {
                for (final Judge j : judges) {
                    if (ArrayUtils.contains(tgt, j)) {
                        return true;
                    }
                }
                return false;
            }
        }
        final Param _param;
        final PersonalInfo _pInfo;
        Map<SubclassMst, TreeMap<Year, Integer>> _gakunenSeisekiMap;
        Map<SubclassMst, TreeMap<Year, Tuple<BigDecimal, BigDecimal>>> _subclassYearKekkaOverMap;
        String _remainGradeFlg;

        AoyamaGakunenSeiseki(final Param param, final PersonalInfo pInfo) {
            _param = param;
            _pInfo = pInfo;
        }

        /**
         * 評定欄表示
         * @param judges
         * @param s
         * @return
         */
        public String hyotei(final Judge[] judges, final StudyRec s) {
            if (Judge.containsAny(judges, new Judge[] { Judge._7_欠課時数2分の1超 })) {
                return "×";
            }
            final StringBuffer stb = new StringBuffer();
            if (Judge.containsAny(judges, new Judge[] { Judge._6_欠課時数3分の1超成績3超, Judge._6_欠課時数3分の1超成績3以下 })) {
                stb.append("*");
                if (Judge.containsAny(judges, new Judge[] { Judge._6_欠課時数3分の1超成績3超 })) {
                    stb.append("保");
                } else if (Judge.containsAny(judges, new Judge[] { Judge._6_欠課時数3分の1超成績3以下 })) {
                    stb.append(defstr(getGakunenSeisekiString(s._subclassMst, Year.of(s._year)))); // 学年成績の値を表示する
                }
            } else {
                if (Judge.containsAny(judges, new Judge[] { Judge._5_評定未入力 })) {
                    stb.append("0");
                } else if (Judge.containsAny(judges, new Judge[] { Judge._4_学年成績3かつ1学期退学 })) {
                    stb.append("");
                } else if (Judge.containsAny(judges, new Judge[] { Judge._3_学年成績3かつ学年途中 })) {
                    stb.append("1+");
                } else if (Judge.containsAny(judges, new Judge[] { Judge._1_学年成績3かつ進級卒業できた })) {
                    stb.append("2-");
                } else if (Judge.containsAny(judges, new Judge[] { Judge._2_学年成績3かつ進級卒業できなかった })) {
                    stb.append("1+");
                } else {
                    stb.append(defstr(s._valuation));
                }
            }
            return stb.toString();
        }

        public boolean is進級卒業できなかった() {
            return "1".equals(_remainGradeFlg); // 0:できた null:学年途中
        }

        private boolean isHyoteiTarget(final StudyRec s) {
            return !Util.containsAny(s.kindList(_param, _pInfo), Arrays.asList(StudyRec.KIND.SOGO90, StudyRec.KIND.ABROAD, StudyRec.KIND.CREDIT_ONLY, StudyRec.KIND.JIRITSU, StudyRec.KIND.SOGO94));
        }
        public Judge[] getJudges(final StudyRec s) {
            final List<Judge> rtn = new ArrayList<Judge>();
            final boolean kekkaJisu1of2Over = isKekkaJisu1of2Over(s);
            final boolean kekkaJisu1of3Over = !kekkaJisu1of2Over && isKekkaJisu1of3Over(s);
            if (kekkaJisu1of2Over) {
                rtn.add(Judge._7_欠課時数2分の1超);
            } else {
                if (isHyoteiTarget(s) && null == s._valuation) {
                    rtn.add(Judge._5_評定未入力);
                } else if (kekkaJisu1of3Over && isGakunenSeisekiGreaterThan3(s._subclassMst, Year.of(s._year))) {
                    rtn.add(Judge._6_欠課時数3分の1超成績3超);
                } else if (kekkaJisu1of3Over && isGakunenSeisekiLessEqual3(s._subclassMst, Year.of(s._year))) {
                    rtn.add(Judge._6_欠課時数3分の1超成績3以下);
                } else if (isGakunenSeisekiEquals3(s._subclassMst, Year.of(s._year)) && !"90".equals(s._classMst._classcd)) { // 10段階評価が3
                    if (is1gakkiTaigaku()) {
                        // 1学期退学
                        rtn.add(Judge._4_学年成績3かつ1学期退学);
                    } else if (_pInfo.isSotsugyo() || "0".equals(_remainGradeFlg)) {
                        // 進級卒業できた
                        rtn.add(Judge._1_学年成績3かつ進級卒業できた);
                    } else if ("1".equals(_remainGradeFlg)) {
                        // 進級卒業できなかった
                        rtn.add(Judge._2_学年成績3かつ進級卒業できなかった);
                    } else {
                        // 学年途中
                        rtn.add(Judge._3_学年成績3かつ学年途中);
                    }
                }
            }

            return rtn.toArray(new Judge[rtn.size()]);
        }

        public boolean isKekkaJisu1of3Over(final StudyRec s) {
            return isKekkaOver(s._subclassMst, Year.of(s._year), 3);
        }

        public boolean isKekkaJisu1of2Over(final StudyRec s) {
            return isKekkaOver(s._subclassMst, Year.of(s._year), 2);
        }

        public static AoyamaGakunenSeiseki load(final DB2UDB db2, final Param param, final PersonalInfo pInfo) {
            final AoyamaGakunenSeiseki c = new AoyamaGakunenSeiseki(param, pInfo);

            final String psKey = "PS_GAKUNENSEISEKI";
            if (null == param.getPs(psKey)) {
                final String sql = sqlGakunenSeiseki(param);
                param.setPs(psKey, db2, sql);
            }
            Map<SubclassMst, TreeMap<Year, Integer>> subclassYearGakunenSeisekiMap = new TreeMap<SubclassMst, TreeMap<Year, Integer>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { param._year, pInfo._schregno})) {
                final SubclassMst mst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"), KnjDbUtils.getString(row, "SUBCLASSCD")));
                if (null == mst) {
                    continue;
                }
                final String year = KnjDbUtils.getString(row, "YEAR");
                final Integer score = Integer.valueOf(KnjDbUtils.getString(row, "SCORE"));
                Util.getMappedMap(subclassYearGakunenSeisekiMap, mst).put(Year.of(year), score);
            }
            c._gakunenSeisekiMap = subclassYearGakunenSeisekiMap;

            final String attendYear = param._year;
            final SchregRegdDat regd = SchregRegdDat.getMaxSemesterRegd(pInfo._student._regdList, attendYear);
            String grade = null;
            if (null != regd) {
                grade = regd._grade;
            }
            final String psKeyKekka = "PS_KEKKA" + grade;
            if (null == param.getPs(psKeyKekka)) {
                final String sdate = param._year + "-04-01";
                final String edate = String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31";
                param._attendParamMap.put("grade", grade);
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(attendYear, "9", sdate, edate, param._attendParamMap);
                param.setPs(psKeyKekka, db2, sql);
            }
            Map<SubclassMst, TreeMap<Year, Tuple<BigDecimal, BigDecimal>>> subclassYearKekkaMap = new TreeMap<SubclassMst, TreeMap<Year, Tuple<BigDecimal, BigDecimal>>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKeyKekka), new Object[] { pInfo._schregno})) {
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                if (!"9".equals(semester)) {
                    continue;
                }
                final String[] split = StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-");
                final SubclassMst mst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, split[0], split[1], split[2], split[3]));
                if (null == mst) {
                    continue;
                }
                final String year = attendYear;
                final BigDecimal kekka = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "LESSON", null);
                Util.getMappedMap(subclassYearKekkaMap, mst).put(Year.of(year), Tuple.of(kekka, lesson));
            }
            c._subclassYearKekkaOverMap = subclassYearKekkaMap;

            final String psKeySinkyu = "PS_GAKUNENSEISEKI_SINKYU";
            if (null == param.getPs(psKeySinkyu)) {
                final String sql = sqlSinkyu(param);
                param.setPs(psKeySinkyu, db2, sql);
            }
            c._remainGradeFlg = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKeySinkyu), new Object[] { String.valueOf(Integer.parseInt(param._year) + 1), pInfo._schregno, param._year }));
            return c;
        }

        /**
         * 1学期退学か
         * @return
         */
        public boolean is1gakkiTaigaku() {
            return _pInfo.isTaigaku() && null != _pInfo._grdSemester && "1".equals(_pInfo._grdSemester._semester);
        }

        /**
         * 10段階評価が3より大きいか
         * @param mst
         * @param year
         * @return
         */
        public boolean isGakunenSeisekiGreaterThan3(final SubclassMst mst, final Year year) {
            return 3 < Util.toInt(getGakunenSeisekiString(mst, year), -1);
        }

        /**
         * 10段階評価が3以下か
         * @param mst
         * @param year
         * @return
         */
        public boolean isGakunenSeisekiLessEqual3(final SubclassMst mst, final Year year) {
            final int score = Util.toInt(getGakunenSeisekiString(mst, year), -1);
            return 1 <= score && score <= 3;
        }

        /**
         * 10段階評価が3か
         * @param mst
         * @param year
         * @return
         */
        public boolean isGakunenSeisekiEquals3(final SubclassMst mst, final Year year) {
            return 3 == Util.toInt(getGakunenSeisekiString(mst, year), -1);
        }

        public String getGakunenSeisekiString(final SubclassMst mst, final Year year) {
            if (!_gakunenSeisekiMap.containsKey(mst) || !_gakunenSeisekiMap.get(mst).containsKey(year)) {
                return null;
            }
            return _gakunenSeisekiMap.get(mst).get(year).toString();
        }

        private boolean isKekkaOver(final SubclassMst mst, final Year year, final int div) {
            if (!_subclassYearKekkaOverMap.containsKey(mst) || !_subclassYearKekkaOverMap.get(mst).containsKey(year)) {
                return false;
            }
            final Tuple<BigDecimal, BigDecimal> kekkaAndLesson = _subclassYearKekkaOverMap.get(mst).get(year);
            final BigDecimal kekka = kekkaAndLesson._first;
            final BigDecimal lesson = kekkaAndLesson._second;
            if (null == kekka || kekka.doubleValue() == 0.0 || null == lesson || lesson.doubleValue() == 0.0) {
                return false;
            }
            final BigDecimal lessonDiv = lesson.divide(new BigDecimal(div), 2, BigDecimal.ROUND_HALF_UP);
            final boolean isOver = kekka.compareTo(lessonDiv) > 0;
            if (_param._isOutputDebugData) {
                _param.logOnce(" " + _pInfo._schregno + " over ? " + (isOver ? "1" : " ") + " : " + mst.getKey(_param) + ":" + mst.subclassname() + ", kekka = " + kekka + ", " + div + " (lessonDiv : " + lessonDiv + ")");
            }
            return isOver;
        }

        private static String sqlSinkyu(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T2.REMAINGRADE_FLG ");
            sql.append(" FROM ");
            sql.append("     SCHREG_REGD_DAT T1 ");
            sql.append("     INNER JOIN CLASS_FORMATION_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("         AND T2.YEAR = ? ");
            sql.append("         AND T2.SEMESTER = '1' ");
            sql.append(" WHERE ");
            sql.append("     T1.SCHREGNO = ? ");
            sql.append("     AND T1.YEAR = ? ");

            return sql.toString();
        }

        private static String sqlGakunenSeiseki(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     YEAR ");
            sql.append("   , CLASSCD ");
            sql.append("   , SCHOOL_KIND ");
            sql.append("   , CURRICULUM_CD ");
            sql.append("   , SUBCLASSCD ");
            sql.append("   , SCORE ");
            sql.append(" FROM ");
            sql.append("     RECORD_SCORE_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("     YEAR <= ? ");
            sql.append("     AND SEMESTER = '9' ");
            sql.append("     AND TESTKINDCD = '99' ");
            sql.append("     AND TESTITEMCD = '00' ");
            sql.append("     AND SCORE_DIV = '08' ");
            sql.append("     AND SCHREGNO = ? ");
            sql.append("     AND SCORE IS NOT NULL ");
            return sql.toString();
        }
    }

    /**
     * <<学習備考>>。
     */
    private static class GakushuBiko {

        private static String STUDY = "STUDY";
        private static String RISHU = "RISHU";
        private static String SUBST = "SUBST";
        private static String SUBST_INPUT = "SUBST_INPUT";
        final Param _param;
        private Map _biko = new HashMap();

        private enum DAITAI_TYPE {
            ZENBU("1")  // 代替フラグ:全部
            ,ICHIBU("2")  // 代替フラグ:一部
            ,NO_TYPE_FLG("") // 指定なし
            ;

            final String _typeFlg;
            DAITAI_TYPE(final String typeFlg) {
                _typeFlg = typeFlg;
            }

            public static DAITAI_TYPE valueOfFlg(final String typeFlg) {
                for (DAITAI_TYPE t : values()) {
                    if (t._typeFlg.equals(typeFlg)) {
                        return t;
                    }
                }
                return null;
            }
        }

        static final List<DAITAI_TYPE> TYPE_FLG_LIST = Arrays.asList(DAITAI_TYPE.ZENBU, DAITAI_TYPE.ICHIBU);

        public GakushuBiko(final Param param) {
            _param = param;
        }

        public Map<SubclassMst, StudyRecSubstitution> getInputSubclassStudyrecSubstitutionMap(final DAITAI_TYPE daitaiType) {
            return Util.getMappedMap(Util.getMappedMap(_biko, SUBST_INPUT), daitaiType);
        }

        public boolean hasDaitai90(final DAITAI_TYPE daitaiType) {
            final Map<SubclassMst, StudyRecSubstitution> map = getInputSubclassStudyrecSubstitutionMap(daitaiType);
            StudyRecSubstitution subst = null;
            for (final Map.Entry<SubclassMst, StudyRecSubstitution> e : map.entrySet()) {
                if (_90.equals(e.getKey()._classcd)) {
                    subst = e.getValue();
                    break;
                }
            }
            if (_param._isOutputDebug) {
                log.info(" hasDaitai90? " + daitaiType + " : subst = " + subst);
            }
            return null != subst;
        }

        /**
         * 科目の年度の学習記録履修単位備考をセットする。
         * @param subclasscd 科目コード
         * @param year 年度
         * @param rishuTanniBiko 履修単位備考
         */
        public void putRishuTanniBiko(final String subclasscd, final String year, final String rishuTanniBiko) {
            Util.getMappedMap(Util.getMappedMap(_biko, RISHU), subclasscd).put(year, rishuTanniBiko);
        }

        /**
         * 科目の年度の学習記録備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param studyrecBiko 学習記録備考
         */
        public void putStudyrecBiko(final String subclassCd, final String year, final String studyrecBiko) {
            Util.getMappedMap(Util.getMappedMap(_biko, STUDY), subclassCd).put(year, studyrecBiko);
        }


        private Map<String, RemarkContainer> getYearStudyrecSubstitutionBikoMap(final String subclasscd, final DAITAI_TYPE daitaiType) {
            return Util.getMappedMap(Util.getMappedMap(Util.getMappedMap(_biko, SUBST), subclasscd), daitaiType);
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録代替科目備考を得る。
         * @param subclasscd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecSubstitutionBiko(final String subclasscd, final DAITAI_TYPE daitaiType, final String yearMin, final String yearMax) {
            return Util.mkString(StringRemarkContainer.toStringList(_param, getBikoList2(getYearStudyrecSubstitutionBikoMap(subclasscd, daitaiType), yearMin, yearMax)), "、");
        }

        /**
         * 最小年度から最大年度までの備考の連結文字列を得る。
         * @param map 年度をキーとする備考のマップ
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @return 最小年度から最大年度までの備考のリスト
         */
        protected <T> List<T> getBikoList2(final Map<String, T> map, final String yearMin, final String yearMax) {
            final List<T> list = new ArrayList<T>();
            for (final String year : map.keySet()) {
                final T biko = map.get(year);
                if (inArea(year, yearMin, yearMax)) {
                    list.add(biko);
                }
            }
            return list;
        }

        /**
         * 最小年度から最大年度までの備考の連結文字列を得る。
         * @param map 年度をキーとする備考のマップ
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @return 最小年度から最大年度までの備考のリスト
         */
        protected List<String> getBikoList(final Map<String, String> map, final String yearMin, final String yearMax) {
            final List<String> list = new ArrayList<String>();
            for (final String year : map.keySet()) {
                final String biko = map.get(year);
                if (inArea(year, yearMin, yearMax) && biko.length() != 0) {
                    list.add(biko);
                }
            }
            return list;
        }

        /**
         * 科目コードのyearMinからyearMaxまでの履修単位備考をコンマ連結で得る。
         * @param subclasscd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getRishuTanniBiko(final String subclasscd, final String yearMin, final String yearMax) {
            return Util.mkString(getBikoList(Util.getMappedMap(Util.getMappedMap(_biko, RISHU), subclasscd), yearMin, yearMax), "、");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録備考を得る。
         * @param subclasscd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecBiko(final String subclasscd, final String yearMin, final String yearMax) {
            return Util.mkString(getBikoList(Util.getMappedMap(Util.getMappedMap(_biko, STUDY), subclasscd), yearMin, yearMax), "、");
        }

//        /**
//         * 科目コードの学習記録代替科目の単位を得る。
//         * @param subclassCd 科目コード
//         * @return
//         */
//        public Integer getStudyrecSubstitutionCredit(final String subclassCd, final DAITAI_TYPE daitaiType, final Param param) {
//            if (null == subclasscd) {
//                return null;
//            }
//            final Map<String, StudyRecSubstitution> subclassStudyRecSubstitutionMap = getSubclassStudyrecSubstitutionMap(daitaiType);
//            final List<Tuple<StringBuffer, String>> list = new ArrayList<Tuple<StringBuffer, String>>();
//            for (final String substitutionSubclasscd : subclassStudyRecSubstitutionMap.keySet()) {
//                if (!subclassCd.equals(substitutionSubclasscd)) {
//                    continue;
//                }
//                final StudyRecSubstitution studyRecSubstitution = subclassStudyRecSubstitutionMap.get(substitutionSubclasscd);
//                final Tuple<StringBuffer, String> info = StudyRecSubstitution.getBikoSubstitutionInfo(param, studyRecSubstitution.attendSubclasses);
//                list.add(info);
//            }
//            if (list.isEmpty()) {
//                return null;
//            }
//            int total = 0;
//            for (final Tuple<StringBuffer, String> info : list) {
//                final String cred = info._second;
//                if (NumberUtils.isDigits(cred)) {
//                    total += Integer.parseInt(cred);
//                }
//            }
//            return new Integer(total);
//        }

        /**
         * 学習記録備考クラスを作成し、マップに加えます。
         */
        protected static void createStudyRecBiko(final DB2UDB db2, final String schregno, final Param param, final GakushuBiko gakushuBiko) {

            final String psKey = "PS_STUDYREC_BIKO";
            if (null == param.getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                } else {
                    stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                    stb.append("     '' AS SCHOOL_KIND, ");
                    stb.append("     '' AS CURRICULUM_CD, ");
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
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD, T1.YEAR");
                final String sql = stb.toString();

                param.setPs(psKey, db2, sql);
            }
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno, param._year })) {

                if (KnjDbUtils.getString(row, "REMARK") == null) {
                    continue;
                }
                final String key;
                if (_90.equals(KnjDbUtils.getString(row, "CLASSCD"))) {
                    key = _90;
                } else if (param._is133m) {
                    key = getSubclasscdM(row, param);
                } else {
                    key = getSubclasscd(row, param);
                }
                gakushuBiko.putStudyrecBiko(key, KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "REMARK"));
            }
        }

        private static String subclassKeycd(final SubclassMst subclass, final Param param) {
            return _90.equals(subclass._classcd) ? _90 : subclass.getKey(param);
        }

        /**
         * 代替科目の学習記録備考を作成し、マップに加えます。
         */
        private static void createStudyRecBikoSubstitution(final Param param, final GakushuBiko gakushuBiko) {
            final Collection<DAITAI_TYPE> typeList;
            if (param._is133m) {
                typeList = Collections.singleton(GakushuBiko.DAITAI_TYPE.NO_TYPE_FLG);
            } else {
                typeList = GakushuBiko.TYPE_FLG_LIST;
            }

            for (final DAITAI_TYPE daitaiType : typeList) {

                final boolean isPrintDaitaiMotoBiko = param._z010.in(Z010.mieken);
                final Map<String, Map<String, List<String>>> subclasscdDaitaiMotoListMap = new HashMap<String, Map<String, List<String>>>();

                // 代替科目備考追加処理
                for (final Map.Entry<SubclassMst, StudyRecSubstitution> e : gakushuBiko.getInputSubclassStudyrecSubstitutionMap(daitaiType).entrySet()) {

                    final SubclassMst substitutionSubclass = e.getKey();
                    final StudyRecSubstitution studyRecSubstitution = e.getValue();

                    gakushuBiko.getYearStudyrecSubstitutionBikoMap(subclassKeycd(substitutionSubclass, param), daitaiType).put(studyRecSubstitution.getMaxAttendSubclassYear(), studyRecSubstitution);

                    if (isPrintDaitaiMotoBiko) {
                        // 代替元にも備考を印字する
                        for (final StudyRecSubstitution.SubstitutionAttendSubclass att : studyRecSubstitution._attendSubclasses) {

                            getMappedList(Util.getMappedHashMap(subclasscdDaitaiMotoListMap, subclassKeycd(att._attendSubclassMst, param)), att._attendyear).add("「" + defstr(studyRecSubstitution._subclassMst.subclassname()) + "」" + Util.prepend("の", Util.append(att._substitutionCreditMstCredit, "単位")));
                        }
                    }
                }

                if (isPrintDaitaiMotoBiko) {
                    for (final Map.Entry<String, Map<String, List<String>>> de : subclasscdDaitaiMotoListMap.entrySet()) {
                        final String subclasscd = de.getKey();
                        for (final Map.Entry<String, List<String>> ye : de.getValue().entrySet()) {
                            final String attendyear = ye.getKey();
                            final List<String> daitaiMotoList = ye.getValue();
                            gakushuBiko.getYearStudyrecSubstitutionBikoMap(subclasscd, daitaiType).put(attendyear, new StringRemarkContainer(Util.mkString(daitaiMotoList, "、") + "に代替"));
                        }
                    }
                }
            }
        }

        /**
         * 代替科目の元科目を取得。
         */
        private List<StudyRecSubstitution.SubstitutionAttendSubclass> getStudyRecBikoSubstitutionAttendSubclass(final String substitutionSubclassBikoKey) {
            final List<StudyRecSubstitution.SubstitutionAttendSubclass> list = new ArrayList<StudyRecSubstitution.SubstitutionAttendSubclass>();
            for (final DAITAI_TYPE daitaiType : TYPE_FLG_LIST) {

                // 代替科目
                for (final Map.Entry<SubclassMst, StudyRecSubstitution> e : getInputSubclassStudyrecSubstitutionMap(daitaiType).entrySet()) {

                    final SubclassMst substitutionSubclass = e.getKey();
                    final StudyRecSubstitution studyRecSubstitution = e.getValue();

                    if (substitutionSubclassBikoKey.equals(subclassKeycd(substitutionSubclass, _param))) {
                        list.addAll(studyRecSubstitution._attendSubclasses);
                    }
                }
            }
            return list;
        }

        /**
         * 総合的な学習の時間の代替科目の学習記録備考を作成し、マップに加えます。
         */
        private Map<String, List<String>> getYearStudyRecBikoSubstitution90ListMap(final DAITAI_TYPE daitaiType, final List<Gakuseki> gakusekiList, final String keyAll, final Param param) {

            //rtn = defstr(_subclassMst.subclassname()) + "は" + Util.mkString(subclassnames, "、").toString() + "で" + daitaiName(false)

            final Map<String, List<String>> yearBikoListMap = new HashMap<String, List<String>>();

            if (param._isOutputDebugSeiseki) {
                log.info(" subclassStudyRecSubstitutionMap " + daitaiType + " = " + getInputSubclassStudyrecSubstitutionMap(daitaiType));
            }
            for (final Map.Entry<SubclassMst, StudyRecSubstitution> e : getInputSubclassStudyrecSubstitutionMap(daitaiType).entrySet()) {

                final SubclassMst substitutionSubclass = e.getKey();
                if (!_90.equals(substitutionSubclass._classcd)) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = e.getValue();

                for (final Gakuseki gakuseki : gakusekiList) {
                    if (null == gakuseki._year) {
                        continue;
                    }
                    getMappedList(yearBikoListMap, gakuseki._year).add(param._substRemark90Template.format(studyRecSubstitution.getBikoSubstitution90TemplateDataMap(gakuseki._year, param)));
                }

                getMappedList(yearBikoListMap, keyAll).add(param._substRemark90Template.format(studyRecSubstitution.getBikoSubstitution90TemplateDataMap(null, param)));
            }
            return yearBikoListMap;
        }

        // GakushuBikoM
        private static final String sikakuYear = "YEAR";
        private static final String sikakuAnnual = "ANNUAL";
        private static final String sikakuConditionDiv = "CONDITION_DIV";
        private static final String sikakuName1 = "NAME1";
        private static final String sikakuRemark = "REMARK";
        private static final String sikakuCredits = "CREDITS";

        private Map<String, List<Map<String, Object>>> _sikakubiko = new HashMap<String, List<Map<String, Object>>>();

        private List<Map<String, Object>> getSikakuBikoList(final String subclassCd) {
            return getMappedList(_sikakubiko, subclassCd);
        }

        public void addSikakuBiko(final String subclassCd, final Map m) {
            getSikakuBikoList(subclassCd).add(m);
        }

        /**
         * 最小年度から最大年度までの備考のリストを得る。
         * @param map 年度をもつ備考のリスト
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @return 最小年度から最大年度までの備考のリスト
         */
        private List<Map<String, Object>> getBikoListInYear(final List<Map<String, Object>> list, final String yearMin, final String yearMax) {
            final List<Map<String, Object>> rtn = new ArrayList();
            for (final Map<String, Object> map : list) {
                final String year = (String) map.get(sikakuYear);
                if (inArea(year, yearMin, yearMax)) {
                    rtn.add(map);
                }
            }
            return rtn;
        }

        private <T extends Comparable<T>> boolean inArea(final T o, final T a, final T b) {
            return (a == null || a.compareTo(o) <= 0) && (b == null || o.compareTo(b) <= 0);
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録備考を得る。
         * @param subclassCd 科目コード
         * @param subclassName 科目名
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public List<String> getStudyrecBikoList(final String subclasscd, final String subclassname, final String yearMin, final String yearMax) {
            final List<String> rtn = new ArrayList<String>();
            final String studyrecBiko = Util.mkString(getBikoList(Util.getMappedMap(Util.getMappedMap(_biko, STUDY), subclasscd), yearMin, yearMax), "").toString();
            if (!StringUtils.isBlank(studyrecBiko)) {
                rtn.add(studyrecBiko);
            }
            final List<Map<String, Object>> sikakuBikoList = getBikoListInYear(getSikakuBikoList(subclasscd), yearMin, yearMax);
            if (sikakuBikoList.size() == 0) {
            } else if (sikakuBikoList.size() >= 2) {
                rtn.add("※下記参照のこと");
            } else {
                rtn.add(createSikakuBiko(subclassname, sikakuBikoList.get(0)));
            }
            return rtn;
        }

        public String createSikakuBiko(final String subClassName, final Map<String, Object> m) {
            final String sikakuBiko;
            if ("3".equals(Util.str(m.get(sikakuConditionDiv)))) {
                sikakuBiko = defstr(Util.str(m.get(sikakuRemark)));
            } else {
                final String sikakuName = Util.str(m.get(sikakuName1));
                final Integer credits = (Integer) m.get(sikakuCredits);
                final String annual = Util.str(m.get(sikakuAnnual));
                String annualStr = "";
                if (NumberUtils.isDigits(annual)) {
                    annualStr = String.valueOf(Integer.parseInt(annual)) + "年次の";
                }
                sikakuBiko = annualStr + subClassName + "の" + credits + "単位分は" + sikakuName + "取得による";
            }
            return sikakuBiko;
        }


        /**
         * yearMinからyearMaxまでの資格備考を得る。
         * @param yearMin year最小
         * @param yearMax year最大
         * @param studyRecSubclassMap 生徒の科目ごとの成績
         * @return
         */
        public List<String> getSikakuBikoList(final String yearMin, final String yearMax, final Map<String, StudyRecSubclassTotal> studyRecSubclassMap, final Param param) {
            final List<String> rtn = new ArrayList();
            for (final String subclassCd : _sikakubiko.keySet()) {
                final List<Map<String, Object>> subclassSikakuBikoList = getBikoListInYear(getSikakuBikoList(subclassCd), yearMin, yearMax);
                final StudyRecSubclassTotal total = studyRecSubclassMap.get(subclassCd);
                if (subclassSikakuBikoList.size() < 2 || null == total) {
                } else {
                    final String subClassName = total.subclassMst(param, YOSHIKI._2_OMOTE).subclassname();
                    for (final Map<String, Object> m : subclassSikakuBikoList) {
                        rtn.add(createSikakuBiko(subClassName, m));
                    }
                }
            }
            return rtn;
        }

        /**
         * 学習記録資格備考を作成し、備考データに加えます。
         */
        private static void createStudyRecQualifiedBiko(final DB2UDB db2, final String schregno, final GakushuBiko gakushuBiko, final Param param) {
            if (!"1".equals(param._useStudyrecRemarkQualifiedDat)) {
                // 使用しない場合は読み込みしない
                return;
            }
            final String psKey = "PS_SIKAKU_BIKO";
            if (null == param.getPs(psKey)) {
                final String sql = sqlStudyrecQualifiedBiko(param);

                param.setPs(psKey, db2, sql);
            }
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno})) {

                final String conditionDiv = KnjDbUtils.getString(row, sikakuConditionDiv);
                if ("3".equals(conditionDiv)) {
                } else {
                    if (KnjDbUtils.getString(row, "NAME1") == null) {
                        continue;
                    }
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD").startsWith(_90) ? _90 : KNJA130C.getSubclasscd(row, param);
                final Map m = new HashMap();
                m.put(sikakuYear, KnjDbUtils.getString(row, "YEAR"));
                m.put(sikakuConditionDiv, conditionDiv);
                m.put(sikakuAnnual, KnjDbUtils.getString(row, "ANNUAL"));
                m.put(sikakuCredits, KnjDbUtils.getInt(row, "CREDITS", null));
                if ("3".equals(conditionDiv)) {
                    m.put(sikakuRemark, KnjDbUtils.getString(row, "REMARK"));
                } else {
                    m.put(sikakuName1, KnjDbUtils.getString(row, "NAME1"));
                }
                log.debug(" sikaku biko = " + m);
                gakushuBiko.addSikakuBiko(subclasscd, m);

            }
        }

        /**
         * @return 学習の記録備考のＳＱＬ文を戻します。
         */
        private static String sqlStudyrecQualifiedBiko(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH YEAR_ANNUAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.YEAR, ");
            stb.append("         MAX(ANNUAL) AS ANNUAL ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         SCHREGNO = ? ");
            stb.append("         AND YEAR <= '" + param._year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, T1.YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CONDITION_DIV, ");
            stb.append("     T4.ANNUAL, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            } else {
                stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("     '' AS SCHOOL_KIND, ");
                stb.append("     '' AS CURRICULUM_CD, ");
            }
            stb.append("     VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, T1.CONTENTS, T2.NAME1, T1.CREDITS, T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_QUALIFIED_DAT T1 ");
            stb.append("     INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'H305' ");
            stb.append("         AND T2.NAMECD2 = T1.CONTENTS ");
            stb.append("     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN YEAR_ANNUAL T4 ON T4.YEAR = T1.YEAR AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.CONDITION_DIV IN ('1', '2') ");
            stb.append("     AND T1.CREDITS IS NOT NULL ");
            stb.append("     AND T2.NAME1 IS NOT NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CONDITION_DIV, ");
            stb.append("     T4.ANNUAL, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            } else {
                stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("     '' AS SCHOOL_KIND, ");
                stb.append("     '' AS CURRICULUM_CD, ");
            }
            stb.append("     VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CONTENTS, CAST(NULL AS VARCHAR(1)) AS NAME1, CAST(NULL AS SMALLINT) AS CREDITS, T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_QUALIFIED_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     INNER JOIN YEAR_ANNUAL T4 ON T4.YEAR = T1.YEAR AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.CONDITION_DIV = '3' ");
            stb.append("     AND T1.REMARK IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     YEAR, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD ");
            return stb.toString();
        }

        public GakushuBiko copy() {
            final GakushuBiko gakushuBiko = new GakushuBiko(_param);

            gakushuBiko._biko = new HashMap(gakushuBiko._biko);

            return gakushuBiko;
        }
    }

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
    private static class StudyRecYearTotalM {

//        private final static byte GET = 0;
//        private final static byte DROP = 1;
//        private final static byte DROP_SHOW = 2;

        private final String _year;
        private final Map<StudyRec.TotalM, List<StudyRec>> _creditMap = new HashMap<StudyRec.TotalM, List<StudyRec>>();

        /**
         * コンストラクタ。
         */
        private StudyRecYearTotalM(final Param param, final String year) {
            _year = year;
            getMappedList(_creditMap, StudyRec.TotalM.SUBJECT90);
            getMappedList(_creditMap, StudyRec.TotalM.ABROAD);
            getMappedList(_creditMap, StudyRec.TotalM.SUBJECT);
            getMappedList(_creditMap, StudyRec.TotalM.KATEIGAI);
            getMappedList(_creditMap, StudyRec.TotalM.TOTAL);
            if (param._z010.in(Z010.sagaken)) {
                getMappedList(_creditMap, StudyRec.TotalM.SUBJECT90_SAGA_A);
                getMappedList(_creditMap, StudyRec.TotalM.SUBJECT90_SAGA_B);
                getMappedList(_creditMap, StudyRec.TotalM.SUBJECT_SAGA_A);
                getMappedList(_creditMap, StudyRec.TotalM.SUBJECT_SAGA_B);
                getMappedList(_creditMap, StudyRec.TotalM.TOTAL_SAGA_A);
                getMappedList(_creditMap, StudyRec.TotalM.TOTAL_SAGA_B);
            }
        }

        private List<StudyRec> list(final StudyRec.TotalM totalKind) {
            return _creditMap.get(totalKind);
        }
    }

    private interface Page {
        public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines);
    }

    private static abstract class KNJA130_0 extends KNJA130CCommon.KNJA130_0 {

        private final Param _param;
        protected boolean nonedata; // データ有りフラグ

        protected int _gradeLineMax;
        protected boolean _isPrintEduDiv2CharsPerLine; // 様式2表の教科専門区分文言は1行2文字ずつ表示

        KNJA130_0(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _param = param;
        }

        protected Param param() {
            return _param;
        }

        protected String modifyForm0(final String form, final PersonalInfo pInfo, final PrintGakuseki pg, final Map<String, String> flgMap) {

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

        protected int getSvfFormFieldLength(final String fieldname, final int def) {
            if (!_form._createSvfForms.containsKey(_form._formInfo._formname)) {
                final String path = _form._svf.getPath(_form._formInfo._formname);
                final SvfForm svfForm = new SvfForm(new File(path));
                _form._createSvfForms.put(_form._formInfo._formname, svfForm.readFile() ? svfForm : null);
            }

            final SvfForm svfForm = _form._createSvfForms.get(_form._formInfo._formname);
            if (null == svfForm) {
                log.info(" no svfForm for " + _form._formInfo._formname + " / " + _form._createSvfForms.keySet());
                return def;
            }
            final SvfForm.Field field = svfForm.getField(fieldname);
            if (null == field) {
                log.info("no svfForm.field : " + _form._formInfo._formname + ", " + fieldname);
                return def;
            }
            return field._fieldLength;
        }

        public abstract void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines);

        public enum GakusekiColumn {
            NORMAL,
            INCLUDE_ZAISEKI_MAE,
            SEQ
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
            } else if ((param._schoolDiv.isGakunenSei(gakuseki._year, pInfo, pInfo._student) || pInfo._student.certifSchool(param)._isGenkyuRyuchi) && !gakuseki._isDroppedAbroad) {
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

        protected String[] getNameLines(final PersonalInfo pInfo) {
            if (pInfo._isPrintRealName &&
                    pInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(pInfo._studentRealName + pInfo._studentName) &&
                    !pInfo._studentRealName.equals(pInfo._studentName)
            ) {
                final String printName1 = pInfo._studentRealName;
                final String printName2 = pInfo._studentName;
                return new String[] {printName1, printName2};
            }
            final String printName = pInfo._isPrintRealName ? pInfo._studentRealName : pInfo._studentName;
            return new String[] {printName};
        }

        protected void printName(final PersonalInfo pInfo, final String field, final String field1, final String field2, final KNJSvfFieldInfo _name) {
            if (!hasField(_name._field) && !hasField(field)) {
                log.info(" has no field : " + _name._field);
                return;
            }
            final int width = _name._x2 - _name._x1;
            if (pInfo._isPrintRealName &&
                    pInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(pInfo._studentRealName + pInfo._studentName) &&
                    !pInfo._studentRealName.equals(pInfo._studentName) &&
                    null != field1 && null != field2
            ) {
                final String printName1 = pInfo._studentRealName;
                final String printName2 = pInfo._studentName;
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
                final String printName = pInfo._isPrintRealName ? pInfo._studentRealName : pInfo._studentName;
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

        /**
         * 様式1用名前出力
         */
        protected void printName1(final DB2UDB db2, final PersonalInfo.HistVal nameHistVal, final String date, final KNJSvfFieldInfo fi) {
            if (null != fi._field1 && null != fi._field2) {
                if (fi._field1Status.isEmpty() || fi._field2Status.isEmpty()) {
                    log.warn("履歴表示フィールドなし:" + fi._field1 + " or " + fi._field2);
                }
            }

            final String nameHistFirst = nameHistVal._histFirstVal;
            String name = nameHistVal._val;
            if (param()._isOutputDebug) {
                log.info(" namehistFirst = " + nameHistFirst + ", name = " + name);
            }
            final boolean hasField23 = !fi._field1Status.isEmpty() && !fi._field2Status.isEmpty() && fi._ystart1 != -1 && fi._ystart2 != -1;
            if (hasField23 && (!StringUtils.isBlank(nameHistFirst) && !nameHistFirst.equals(name))) {
                final int keta = Math.min(getMS932ByteLength(nameHistFirst), fi._maxnum);
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(fi._field1, fi._x2 - fi._x1, fi._height, fi._ystart1, fi._minnum, fi._maxnum);
                modify1._param = param();
                final double charSize1 = modify1.getCharSize(nameHistFirst);
                svfVrAttribute(fi._field1, "Size=" + charSize1);
                svfVrAttribute(fi._field1, "Y=" + (int) modify1.getYjiku(0, charSize1));
                svfVrsOut(fi._field1, nameHistFirst);
                svfUchikeshi(fi._field1, keta, null);

                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(fi._field2, fi._x2 - fi._x1, fi._height, fi._ystart2, fi._minnum, fi._maxnum);
                modify2._param = param();
                if (null != date) {
                    name = defstr(name) + henkouHiduke(db2, date);
                }
                final double charSize2 = modify2.getCharSize(name);
                svfVrAttribute(fi._field2, "Size=" + charSize2);
                svfVrAttribute(fi._field2, "Y=" + (int) modify2.getYjiku(0, charSize2));
                svfVrsOut(fi._field2, name);
            } else if (hasField23 && getMS932ByteLength(name) > _form._formInfo.getFieldLength(fi._field, 999)) {
                svfVrsOutSplit(new String[] {fi._field1, fi._field2}, name);
            } else {
                // 履歴なしもしくは最も古い履歴の名前が現データの名称と同一
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(fi._field, fi._x2 - fi._x1, fi._height, fi._ystart, fi._minnum, fi._maxnum);
                modify._param = param();
                final double charSize = modify.getCharSize(name);
                svfVrAttribute(fi._field, "Size=" + charSize);
                svfVrsOut(fi._field, name);
            }
        }

        protected String henkouHiduke(final DB2UDB db2, final String date) {
            return kakko(defstr(formatDate(db2, date, param()) + "変更"));
        }

        protected void printSchoolName(final Student student) {
            final CertifSchoolDat certifSchoolDat = student.certifSchool(param());
            if ("1".equals(param().property(Property.seitoSidoYorokuPrintGappeimaeSchoolname)) && !StringUtils.isBlank(student._gappeimaeSchoolname)) {
                svfVrsOut("SCHOOLNAME2", student._gappeimaeSchoolname);
                svfUchikeshi("SCHOOLNAME2", getMS932ByteLength(student._gappeimaeSchoolname), null);
                svfVrsOut("SCHOOLNAME3", certifSchoolDat._schoolName1);
            } else if (param()._z010.in(Z010.nishiyama)) {
                svfVrsOut("SCHOOLNAME2", certifSchoolDat._certifSchoolDatRemark1);
                svfVrsOut("SCHOOLNAME3", certifSchoolDat._schoolName1);
            } else if (!StringUtils.isBlank(certifSchoolDat._bunkouSchoolName)) {
                svfVrsOut("SCHOOLNAME2", certifSchoolDat._schoolName1);
                svfVrsOutForData(Arrays.asList("SCHOOLNAME3", "SCHOOLNAME3_2"), "（" + certifSchoolDat._bunkouSchoolName + "）");
            } else if (null != certifSchoolDat._schoolName1) {
                final int keta1 = _form._formInfo.getFieldLength("SCHOOLNAME1", 0);
                final int keta2 = _form._formInfo.getFieldLength("SCHOOLNAME2", 0);
                final SvfField fieldSCHOOLNAME2 = _form._formInfo.getSvfField("SCHOOLNAME2", false);
                boolean linkField = false;
                if (null != fieldSCHOOLNAME2) {
                    linkField = "SCHOOLNAME3".equals(fieldSCHOOLNAME2.getAttributeMap().get(SvfField.AttributeLinkField));
                }
                final int keta3 = _form._formInfo.getFieldLength("SCHOOLNAME3", 0);
                if (keta1 < getMS932ByteLength(certifSchoolDat._schoolName1)) {
                    if (0 < keta1 && linkField && keta1 < keta2) {
                        svfVrsOut("SCHOOLNAME2", certifSchoolDat._schoolName1);
                    } else if (0 < keta1 && !linkField && keta1 <= keta2 + keta3) {
                        final List<String> arr = Util.getTokenList(param(), certifSchoolDat._schoolName1, keta2, 2);
                        for (int i = 0; i < arr.size(); i++) {
                            svfVrsOut("SCHOOLNAME" + (2 + i), arr.get(i));
                        }
                    } else {
                        // 収まらない
                        svfVrsOut("SCHOOLNAME1", certifSchoolDat._schoolName1);
                    }
                } else {
                    svfVrsOut("SCHOOLNAME1", certifSchoolDat._schoolName1);
                }
            }
        }

        protected static boolean isNewForm(final Param param, final PersonalInfo pInfo) {
            boolean rtn = false;
            if (param._useNewForm && null != pInfo) {
                final int checkYear = 2013; // 切替年度
                if (NumberUtils.isDigits(pInfo._curriculumYear)) {
                    // 教育課程年度が入力されている場合
                    if (checkYear > Integer.parseInt(pInfo._curriculumYear)) {
                        rtn = false;
                    } else {
                        rtn = true;
                    }
                } else {
                    rtn = PersonalInfo.entYearStart(param, pInfo, checkYear);
                }
            }
            if (param._isOutputDebug) {
                log.info(" isNewForm = " + rtn + ", useNewForm = " + param._useNewForm + ", student curYear = " + (null == pInfo ? "" : pInfo._curriculumYear) + ", entYear =" + (null == pInfo ? "" : pInfo._entYear) + ", entYearGradeCd = " + (null == pInfo ? "" : pInfo._entYearGradeCd));
            }
            return rtn;
        }


        protected void printFooterRemark(final PersonalInfo pInfo, final YOSHIKI yoshiki) {
            final int length = _form._formInfo.getFieldLength("FOOTER1", 110);
            if (null != param()._outputDebugFieldList) {
                log.info(" footerl = " + length);
            }
            if (pInfo._isFuhakkou && (yoshiki == YOSHIKI._2_OMOTE || yoshiki == YOSHIKI._2_URA)) {
                if (length > 110) {
                    final String text = "教科・科目の評定等、指導要録における指導に関する記録については、法令で定められた保存期間（卒業後" + param()._seitoSidoYorokuHozonkikan + "年）が経過しているため、証明できません。";
                    final List<String> tokenList = Util.getTokenList(text, length);
                    for (int i = 0; i < tokenList.size(); i++) {
                        final String token = tokenList.get(i);
                        svfVrsOut("FOOTER" + String.valueOf(i + 1), token);
                    }
                } else {
                    final String FUHAKKOU_TEXT1 = "　　教科・科目の評定等、指導要録における指導に関する記録については、法令で定められた保存期間（卒業後";
                    final String FUHAKKOU_TEXT2 = "　　" + param()._seitoSidoYorokuHozonkikan + "年）が経過しているため、証明できません。";
                    svfVrsOut("FOOTER1", FUHAKKOU_TEXT1);
                    svfVrsOut("FOOTER2", FUHAKKOU_TEXT2);
                }
            } else if (param()._z010.in(Z010.meikei) && (yoshiki == YOSHIKI._1_URA && !"1".equals(param()._ibCourse) || yoshiki == YOSHIKI._2_OMOTE)) {

                final String text = "※学校設定科目／〇スーパーサイエンスハイスクール指定による特例措置設定科目";
                final List<String> tokenList = Util.getTokenList(text, length);
                for (int i = 0; i < tokenList.size(); i++) {
                    final String token = tokenList.get(i);
                    svfVrsOut("FOOTER" + String.valueOf(i + 1), token);
                }
            }
        }

        /**
         * <pre>
         *  学年・年度表示欄の印字位置（番号）を戻します。
         *  ・学年制の場合は学年による固定位置
         *  ・単位制の場合は連番
         * </pre>
         *
         * @param i：連番
         * @param gakuseki
         * @return
         */
        protected static int getGradeColumnNumM1(final Student student, final PersonalInfo pInfo, final int i, final Gakuseki gakuseki, final Param param, final int max) {
            if (param._schoolDiv.isGakunenSei(gakuseki._year, pInfo, student)) {
                final int j = Integer.parseInt(gakuseki._grade);
                return (0 == j % max) ? max : j % max;
            } else {
                return i;
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

        private static TargetGakusekiFlag getTargetGakuseki(final YOSHIKI yoshiki, final PersonalInfo pInfo, final Gakuseki gakuseki, final Param param) {
            TargetGakusekiFlag isTarget = TargetGakusekiFlag.Normal;
            if (yoshiki == YOSHIKI._2_OMOTE) {
                if (gakuseki.isNotPrint(param)) {
                    isTarget = TargetGakusekiFlag.NotPrint2Omote;
                }
                if (!param._is133m) {
                    if (!pInfo.isTargetYear("isTargetGakuseki : ", gakuseki._year, param)) {
                        isTarget = TargetGakusekiFlag.NotTarget2Omote;
                    }
                }
                if (Gakuseki.GAKUSEKI_DATA_FLG1.equals(gakuseki._dataflg)) {
                    if (!param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        isTarget = TargetGakusekiFlag.NotGdatH2Omote;
                    }
                } else if (Gakuseki.GAKUSEKI_DATA_FLG2_ZAISEKIMAE.equals(gakuseki._dataflg) || Gakuseki.GAKUSEKI_DATA_FLG3_ZAISEKIMAE_NENDOARI.equals(gakuseki._dataflg)) {
                    if (!param._is133m) {
                        if (param._seitoSidoYorokuZaisekiMae) { // TODO: ???
                            if (Gakuseki.nyugakuMaeHaMigi(pInfo._student, pInfo, param)) {
                                isTarget = TargetGakusekiFlag.PrintZaisekiMae2Omote; // 在籍前を印字する
                            }
                        } else {
                            isTarget = TargetGakusekiFlag.NotPrintZaisekiMae2Omote;
                        }
                    }
                }
            } else if (yoshiki == YOSHIKI._2_URA) {
                if (gakuseki.isNotPrint(param)) {
                    isTarget = TargetGakusekiFlag.NotPrint2Ura;
                }
                if (param._is133m) {
//                  if (!param().isGakunenSei() && isAnotherSchoolYear(gakuseki._year)) {
//                      isTarget = 0;
//                  }
                } else {
                    if (!(param._schoolDiv.isGakunenSei(gakuseki._year, null, pInfo._student) || pInfo._student.certifSchool(param)._isGenkyuRyuchi) && gakuseki.isNyugakumae() || !param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        isTarget = TargetGakusekiFlag.NotPrintTargetEtc2Ura;
                    }
                    if (!pInfo.isTargetYear("isTargetGakuseki : ", gakuseki._year, param)) {
                        isTarget = TargetGakusekiFlag.NotPrintTargetYear2Ura;
                    }
                }
            }
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

        private static Map<Integer, PrintGakuseki> getPagePrintGakusekiMap(final DB2UDB db2, final YOSHIKI yoshiki, final PersonalInfo pInfo, final Param param, int max) {
            final List<Gakuseki> gakusekiAllList = KNJA130_0.getPageGakusekiList(db2, yoshiki, pInfo, param);

            if (max == 3 && param._z010.in(Z010.kyoto)) {
                if (Gakuseki.containsDroppedAbroad(gakusekiAllList)) {
                    max = 4;
                }
            }

            final Map<Integer, PrintGakuseki> pageGakusekiListMap = new TreeMap<Integer, PrintGakuseki>();

            final int zaisekimaePos = max; // param._isMieken ? 1 : max;
            final List<Gakuseki> targetGakusekiList = new ArrayList<Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiAllList) {
                if (getTargetGakuseki(yoshiki, pInfo, gakuseki, param).isTarget()) {
                    targetGakusekiList.add(gakuseki);
                }
            }
            final Gakuseki lastGakuseki = (targetGakusekiList.size() == 0 ? null : targetGakusekiList.get(targetGakusekiList.size() - 1));

            if (param._isOutputDebug) {
                log.info(" PrintGakuseki src = " + Util.listString(gakusekiAllList, 0));
            }

            Map<YOSHIKI2_OMOTE_VER, List<Gakuseki>> yoshikiVerYearListMap = null;
            final List<List<Gakuseki>> gakusekiListList;
            if (yoshiki == YOSHIKI._2_OMOTE) {
                yoshikiVerYearListMap = YOSHIKI2_OMOTE_VER.getYoshikiVerYearListMap(param, gakusekiAllList);
                if (param._isOutputDebug) {
                    log.info(" yoshikiVerYearListMap size = " + yoshikiVerYearListMap.size());
                    for (final Map.Entry<YOSHIKI2_OMOTE_VER, List<Gakuseki>> e : yoshikiVerYearListMap.entrySet()) {
                        log.info(" yoshikiVerYearListMap " + e.getKey() + " = " + Util.listString(e.getValue(), 1));
                    }
                }
                gakusekiListList = new ArrayList<List<Gakuseki>>(yoshikiVerYearListMap.values());
            } else {
                gakusekiListList = Collections.singletonList(gakusekiAllList);
            }

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
                    final TargetGakusekiFlag targetGakuseki = getTargetGakuseki(yoshiki, pInfo, gakuseki, param);
                    if (!targetGakuseki.isTarget()) {
                        continue;
                    }
                    PrintGakuseki printGakuseki = getPagedPrintGakuseki(pageGakusekiListMap, new Integer(ipage));
                    printGakuseki._isLastPrintGakuseki = printGakuseki._isLastPrintGakuseki || lastGakuseki == gakuseki;
                    if (yoshiki == YOSHIKI._2_OMOTE) {
                        printGakuseki._isYoshiki2omote3KantenForm = yoshikiVerYearListMap.get(YOSHIKI2_OMOTE_VER._3KANTEN) == gakusekiList;
                    }

                    int newpos = -1;
                    boolean zaisekimae = false;
                    if (isAbroadPrintDrop) {
                        newpos = pos;
                    } else if (param._is133m) {
                        newpos = getGradeColumnNumM1(pInfo._student, pInfo, pos, gakuseki, param, max);
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
                    } else if (gakuseki._isDrop && pInfo._abroadPrintDropRegdYears.contains(gakuseki._year)) {
                        isAbroadPrintDrop = true;
                        pos++;
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
                    log.info(" yoshiki " + yoshiki + " printgakuseki (page " + n + ") = " + Util.listString(new ArrayList(printgakuseki._yearGakusekiMap.entrySet()), 0));
                }
            }
            return pageGakusekiListMap;
        }

        private static List<Gakuseki> getPageGakusekiList(final DB2UDB db2, final YOSHIKI yoshiki, final PersonalInfo pInfo, final Param param) {
            List<Gakuseki> gakusekiList = Collections.emptyList();
            if (yoshiki == YOSHIKI._2_OMOTE) {
                final List<StudyRec> studyRecListInPersonalInfo = new ArrayList<StudyRec>();
                if (param._is133m) {
                    studyRecListInPersonalInfo.addAll(pInfo._studyRecList);
                    studyRecListInPersonalInfo.addAll(pInfo.getStudyRecReplaceSateiAriList(param, YOSHIKI._2_OMOTE));
                } else {
                    studyRecListInPersonalInfo.addAll(pInfo._studyRecList);
                }

                gakusekiList = Student.createGakusekiStudyRec(db2, pInfo, pInfo._gakusekiList, studyRecListInPersonalInfo, param);

            } else if (yoshiki == YOSHIKI._2_URA || yoshiki == YOSHIKI.SHUKKETSUNOKIROKU) {

                gakusekiList = Student.createGakusekiAttendRec(db2, pInfo, pInfo._gakusekiList, pInfo._student._attendRecMap, param);

            } else if (yoshiki == YOSHIKI.HOSOKU) {

                gakusekiList = new ArrayList<Gakuseki>(pInfo._gakusekiList);

            }
            return gakusekiList;
        }

        protected static boolean isZennnichisei(final PersonalInfo pInfo) {
            return null != pInfo._checkCourseName && -1 != pInfo._checkCourseName.indexOf("全日制");
        }

        protected static boolean isTeijisei(final PersonalInfo pInfo) {
            return null != pInfo._checkCourseName && -1 != pInfo._checkCourseName.indexOf("定時制");
        }

        protected boolean is3nenYou(final PersonalInfo pInfo) {
            if (null != pInfo && param()._z010.in(Z010.fukuiken)) {
                return isZennnichisei(pInfo); // 全日制は3年生用。それ以外は4年生用。
            }
            return param()._is3nenYou;
        }

        protected List<List<String>> getCsvGakusekiLines(final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            final List<List<String>> gakusekiLines = new ArrayList<List<String>>();
            final List<String> gradeLines = CsvUtils.newLine(gakusekiLines);
            gradeLines.add("区分＼" + pInfo._title);
            final List<String> hrLines = CsvUtils.newLine(gakusekiLines);
            hrLines.add("ﾎｰﾑﾙｰﾑ");
            final List<String> attendnoLines = CsvUtils.newLine(gakusekiLines);
            attendnoLines.add("整理番号");
            for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                if (null == gakuseki) {
                    log.warn(" no gakuseki : year = " + year  + " / years = " + printGakuseki._yearGakusekiMap.keySet());
                    continue;
                }

                if (gakuseki._isKoumokuGakunen) {
                    gradeLines.add(gakuseki._gdat._gakunenSimple);
                } else {
                    gradeLines.add(gakuseki._nendo);
                }

                if (param()._is133m && param()._z010.in(Z010.kumamoto) && gakuseki._isStudyrecGakusekiM) {
                } else {
                    // ホームルーム
                    hrLines.add(gakuseki._hdat._hrname);
                    if (param()._is133m) {
                        attendnoLines.add(pInfo._student._schregno);
                    } else {
                        attendnoLines.add(gakuseki._attendno);
                    }
                }
            }
            return gakusekiLines;
        }
    }

    /*
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 学籍の記録 2004/08/18
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * SCHREG_BASE_MST ) --NO001 ・編入において学年が出力されない不具合を修正 --NO001 ・印鑑の出力処理を追加
     * --NO002 ・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加 --NO003 =>
     * 無い場合は従来通りHR_CLASSを出力 ・日付および年度の出力仕様を変更 --NO004 ・学年をすべて出力 --NO005
     * ・生徒名および保護者名の出力仕様変更 --NO006 ・学籍異動履歴出力仕様変更 --NO007 2006/05/02
     * yamashiro・入学前学歴の学校名出力仕様変更 --NO008
     */

    /**
     * 生徒指導要録(学籍の記録)。
     */
    private static class KNJA130_1 extends KNJA130_0 implements Page {

        final int charSize8 = (int) KNJSvfFieldModify.charHeightPixel("", 8.0);
        final int charSize13 = (int) KNJSvfFieldModify.charHeightPixel("", 13.0);

        private KNJSvfFieldInfo _kana = new KNJSvfFieldInfo(-1, -1, charSize8, -1, -1, -1, 12, 100);
        private KNJSvfFieldInfo _gKana = new KNJSvfFieldInfo(-1, -1, charSize8, -1, -1, -1, 12, 100);
        private KNJSvfFieldInfo _name = new KNJSvfFieldInfo(-1, -1, charSize13, -1, -1, -1, 24, 48);
        private KNJSvfFieldInfo _gName = new KNJSvfFieldInfo(-1, -1, charSize13, -1, -1, -1, 24, 48);

        private final String fieldLine1 = "LINE1";
        private final String fieldLine2 = "LINE2";
        private final String fieldENTERDIV1 = "ENTERDIV1";
        private final String fieldENTERDIV2 = "ENTERDIV2";

        private final String HOSOKU_ARI = "補足あり";

        int _addressMax = -1;
        int _grdaddressMax133m = -1;

        KNJA130_1(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public void init() {
            if (csv.equals(param()._cmd)) {
                _gradeLineMax = 99;
            } else {
                getForm1(null, null);
            }
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {

            if (param()._isOutputDebug) {
                log.info(" 学籍List all = " + pInfo._gakusekiList);
            }

            final Map<Integer, PrintGakuseki> printGakusekiMap = getPrintGakusekiPageMap(pInfo, _gradeLineMax);
            final PrintGakuseki lastPg = printGakusekiMap.get(max(printGakusekiMap.keySet()));
            if (param()._isOutputDebug) {
                log.info(" lastPrintGakuseki = " + lastPg);
            }
            for (final Integer page : printGakusekiMap.keySet()) {

                final PrintGakuseki pg = printGakusekiMap.get(page);
                pg._isLastPrintGakuseki = pg.equals(lastPg);

                printPage1(db2, student, pInfo, pg, csvLines);
            }
            nonedata = true;
        }


        public Map<Integer, PrintGakuseki> getPrintGakusekiPageMap(final PersonalInfo pInfo, final int gradeLineMax) {
            return PrintGakuseki.getPrintGakusekiPageMap(getGakusekiPageList(pInfo, gradeLineMax));
        }

        @Override
        public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
            printPage1(db2, student, pInfo, pg, csvLines);
        }

        public void printPage1(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki pg, final List<List<String>> csvLines) {
            if (null == csvLines) {
                final String form1 = getForm1(student, pInfo);

                final String form = modifyForm1(form1, student, pInfo, pg);

                svfVrSetForm(form, 1);
            }

            printStudent1(db2, student, pInfo, pg, csvLines);

            svfVrEndPage();
        }

        private void printStudent1(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {

            int yearMin = Integer.parseInt(printGakuseki.minYear());
            int yearMax = Integer.parseInt(printGakuseki.maxYear());

            if (param()._isOutputDebug) {
                log.info(" page " + printGakuseki._pageIdx + ": yearMin = " + yearMin + ", max = " + yearMax + ", 学籍List = " + Util.listString(printGakuseki._gakusekiList, 0));
            }
            if (null != csvLines) {

                final List<List<String>> nameLines = new ArrayList<List<String>>();
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines).addAll(Arrays.asList("", "", "高　等　学　校　生　徒　指　導　要　録", "", "", "", "", ""));
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines);

                csvLines.addAll(CsvUtils.horizontalUnionLines(nameLines, getCsvGakusekiLines(pInfo, printGakuseki)));

                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("", "", "学籍の記録", "", "", "", "", ""));
                CsvUtils.newLine(csvLines);

                List<List<String>> left = new ArrayList<List<String>>();
                List<List<String>> seito = new ArrayList<List<String>>();
                seito = CsvUtils.horizontalUnionLines(seito, Util.listToListList(Arrays.asList("生徒")));
                List<List<String>> seito2 = new ArrayList<List<String>>();
                seito2.add(Arrays.asList("ふりがな", pInfo._studentKana._val));
                seito2.add(Arrays.asList("氏名", pInfo._studentName, "性別", pInfo._sex));
                seito2.add(Arrays.asList("生年月日", pInfo._birthdayStr));
                final List<Address> printAddressList = getIndexedList(pInfo._addressList, Address.getPrintAddressIndex(param(), "address", pInfo._addressList, _addressMax));
                for (int i = 0; i < 3; i++) {
                    Address a = i < printAddressList.size() ? printAddressList.get(i) : new Address(-1, new HashMap());
                    seito2.add(Arrays.asList(i == 0 ? "住所" : ""));
                    seito2.add(Arrays.asList("", a.getAddr1()));
                    seito2.add(Arrays.asList("", a.getAddr2()));
                }
                seito = CsvUtils.horizontalUnionLines(seito, seito2);

                List<List<String>> hogosha = new ArrayList<List<String>>();
                hogosha = CsvUtils.horizontalUnionLines(hogosha, Util.listToListList(Arrays.asList("保護者")));
                List<List<String>> hogosha2 = new ArrayList<List<String>>();
                hogosha2.add(Arrays.asList("ふりがな", pInfo._guardianOrGuarantorKana._val));
                hogosha2.add(Arrays.asList("氏名", pInfo._guardianOrGuarantorName._val));
                final List<Address> printGAddressList = getIndexedList(pInfo._addressList, Address.getPrintAddressIndex(param(), "address", pInfo._addressList, _addressMax));
                for (int i = 0; i < 3; i++) {
                    Address a = i < printGAddressList.size() ? printGAddressList.get(i) : new Address(-1, new HashMap());
                    hogosha2.add(Arrays.asList(i == 0 ? "住所" : ""));
                    hogosha2.add(Arrays.asList("", a.getAddr1()));
                    hogosha2.add(Arrays.asList("", a.getAddr2()));
                }
                hogosha = CsvUtils.horizontalUnionLines(hogosha, hogosha2);

                List<List<String>> nyugakumae = new ArrayList<List<String>>();
                nyugakumae = CsvUtils.horizontalUnionLines(nyugakumae, Util.listToListList(Arrays.asList("", "入学前の経歴", "")));
                nyugakumae = CsvUtils.horizontalUnionLines(nyugakumae, Util.listToListList(Arrays.asList(pInfo._finishDate, defstr(KnjDbUtils.getString(pInfo._regRow, "J_NAME")) + "卒業")));

                left.addAll(seito);
                left.addAll(hogosha);
                left.addAll(nyugakumae);

                List<List<String>> right = new ArrayList<List<String>>();
                final String entDateStr = setDateFormatInsertBlank(db2, formatDate1(db2, pInfo._entDate, param()), param(), param()._formatDateDefaultYear);
                right.add(Arrays.asList("入学・編入学", !pInfo.isTennyu() ? entDateStr : ""));
                if (!pInfo.isTennyu()) {
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entReason));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entSchool));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entAddr));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entAddr2));
                } else {
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                }
                right.add(Arrays.asList("転入学", pInfo.isTennyu() ? entDateStr : ""));
                if (pInfo.isTennyu()) {
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entReason));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entSchool));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entAddr));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._entAddr2));
                } else {
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                }
                final String grdDateStr = setDateFormatInsertBlank(db2, formatDate1(db2, pInfo._grdDate, param()), param(), param()._formatDateDefaultYear);
                right.add(Arrays.asList("転学・退学等", pInfo.isTengaku() || pInfo.isTaigaku() ? grdDateStr : ""));
                if (pInfo.isTengaku() || pInfo.isTaigaku()) {
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdReason));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdSchool));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdAddr));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdAddr2));
                } else {
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                }
                right.add(Arrays.asList("留学・休学等", ""));
                right.add(Arrays.asList("卒業", pInfo.isSotsugyo() ? grdDateStr : ""));
                if (pInfo.isSotsugyo()) {
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdReason));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdSchool));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdAddr));
                    CsvUtils.newLine(right).addAll(Arrays.asList("", pInfo._grdAddr2));
                } else {
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                    CsvUtils.newLine(right);
                }
                right.add(Arrays.asList("進学先・就職先等", ""));

                left = CsvUtils.horizontalUnionLines(left, right);

                List<List<String>> bottomLeft = new ArrayList<List<String>>();
                final CertifSchoolDat certifSchool = student.certifSchool(param());
                bottomLeft.add(Arrays.asList("学校名", certifSchool._schoolName1, "", "", ""));
                bottomLeft.add(Arrays.asList("所在地", defstr(param()._schoolAddress1) + defstr(param()._schoolAddress2)));
                bottomLeft.add(Arrays.asList("", Util.kakko(certifSchool._bunkouSchoolName)));
                bottomLeft.add(Arrays.asList("課程名", pInfo._courseName));
                bottomLeft.add(Arrays.asList("学科名", pInfo._majorName));

                List<List<String>> bottomRight = new ArrayList<List<String>>();
                bottomRight.add(Arrays.asList("年度", "学年", "校長氏名印", "ホームルーム担任者氏名印"));
                final List<Gakuseki> gakusekiList = new ArrayList<Gakuseki>(printGakuseki._gakusekiList);
                Collections.sort(gakusekiList, new Gakuseki.GakusekiComparator(student, pInfo, param()));
                for (int j = 0; j < gakusekiList.size(); j++) {
                    final Gakuseki gakuseki = gakusekiList.get(j);
                    final String nendo = setNendoFormatInsertBlank(db2, gakuseki._nendo, param(), param()._formatDateDefaultYear);
                    final String gakunen = gakuseki._isKoumokuGakunen || "1".equals(param().property(Property.seitoSidoYorokuPrintForm1StaffGrade)) ? gakuseki._gdat._gakunenSimple : "";

                    final Tuple<Staff, Tuple<Staff, Staff>> principal012 = getPrincipal012(gakuseki, student, pInfo);
                    final Staff _principal1 = principal012._second._first;
                    final List<Staff> studentStaff1HistList = param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV1, gakuseki._year);
                    final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
                    bottomRight.add(Arrays.asList(nendo, gakunen, _principal1._staffMst._name, staff1Last._staffMst._name));
                }


                final List<List<String>> bottom = CsvUtils.horizontalUnionLines(bottomLeft, bottomRight);
                left.addAll(bottom);
                csvLines.addAll(left);

                return;
            }
            printDefault1(db2, param(), student, pInfo); // デフォルト印刷
            printSchoolInfo1(student); // 学校情報印刷
            printCourseMajor(pInfo);
            printPersonalInfo1(db2, pInfo); // 個人情報印刷
            printAddress(db2, pInfo); // 住所履歴印刷
            printGuardianAddress(db2, pInfo); // 保護者住所履歴印刷
            printNyugaku(db2, student, pInfo, yearMin, yearMax); // 入学・編入学・転入学
            if (printGakuseki._isLastPrintGakuseki) {
                printTengakuTaigakuSotsugyo(db2, pInfo, yearMin, yearMax); // 転学・退学・卒業
            }
            printRyugakuKyugaku(db2, student, pInfo, printGakuseki); // 異動履歴印刷
            printAfterGraduate(student, pInfo); // 進学先・就職先等印刷
            printGakuseki(db2, student, pInfo, printGakuseki);
        }

        private Map<Gakuseki, Integer> getGakusekiPosisionMap(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final Collection<Gakuseki> gakusekiList) {
            int i = 0;
            final Map<Gakuseki, Integer> map = new HashMap<Gakuseki, Integer>();
            for (final Gakuseki gakuseki : gakusekiList) {
                int i1;
                if (param()._is133m) {
                    if (param()._schoolDiv.isGakunenSei(gakuseki._year, pInfo, student)) {
                        i1 = Integer.parseInt(gakuseki._grade);
                    } else {
                        i1 = i + 1;
                    }
                } else {
                    if ((param()._schoolDiv.isGakunenSei(gakuseki._year, pInfo, student) || student.certifSchool(param())._isGenkyuRyuchi) && !param()._z010.in(Z010.kyoto) && pInfo.isDropBefore(gakuseki)) {
                        if (null == gakuseki._gdat._gradeCd) {
                            i1 = -1;
                        } else {
                            i1 = Integer.parseInt(gakuseki._gdat._gradeCd);
                        }
                    } else {
                        i1 = i + 1;
                    }
                    if (param()._isOutputDebug) {
                        log.info(" _gradeLineMax = " + _gradeLineMax);
                    }
                    if (0 < _gradeLineMax && _gradeLineMax < i1) {
                        i1 = i1 - _gradeLineMax;
                    }
                }
                final int newi = i1;
                map.put(gakuseki, newi);
                i = newi;
            }
            return map;
        }

        private void printGakuseki(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            final Map<Gakuseki, Integer> gakusekiPositionMap = getGakusekiPosisionMap(student, pInfo, printGakuseki, printGakuseki._gakusekiList);
            for (int j = 0; j < printGakuseki._gakusekiList.size(); j++) {
                final Gakuseki gakuseki = printGakuseki._gakusekiList.get(j);
                final int pos = gakusekiPositionMap.get(gakuseki);
                if (param()._isOutputDebug) {
                    log.info(" print gakuseki " + gakuseki._year + " in " + pos);
                }
                printGakuseki1(db2, student, pInfo, String.valueOf(pos), gakuseki);
            }
        }

        private List<List<Gakuseki>> getGakusekiPageList(final PersonalInfo pInfo, final int gradeLineMax) {
            final List<List<Gakuseki>> gakusekiListList = new ArrayList<List<Gakuseki>>();
            List<Gakuseki> current = null;
            final boolean useNewPageByDrop = !param()._z010.in(Z010.kyoto);
            boolean newPageByDrop = false;
            for (final Gakuseki gakuseki : pInfo._gakusekiList) {
                if (gakuseki.isNotPrint(param())) {
                    continue;
                }
                if (!param()._is133m) {
                    if (!pInfo.isTargetYear("getGakusekiPageList : ", gakuseki._year, param())) {
                        continue;
                    }
                }

                if (null == current || current.size() >= gradeLineMax || newPageByDrop) {
                    current = new ArrayList<Gakuseki>();
                    gakusekiListList.add(current);
                }
                if (useNewPageByDrop) {
                    newPageByDrop = gakuseki._isDrop && !pInfo._abroadPrintDropRegdYears.contains(gakuseki._year);
                }
                current.add(gakuseki);
            }
            if (param()._is133m && gakusekiListList.isEmpty()) {
                // 対象外のクラスのみの場合も表示する
                gakusekiListList.add(new ArrayList<Gakuseki>());
            }
            return gakusekiListList;
        }

        private String getForm1(final Student student, final PersonalInfo pInfo) {
            final String form;

            int stdX2 = -1;
            int guardX2 = -1;
            int kanaminnum = 12;
            int kanaDefCharSize = charSize8;
            final boolean is3 = is3nenYou(pInfo);

            final int addressMax;
            _grdaddressMax133m = -1;
            if (param()._z010.in(Z010.kyoto)) {
                if (param()._is133m) {
                    form = "KNJA133M_1KYOTO.frm";
                    _gradeLineMax = 8;
                    guardX2 = 1689;
                } else {
                    form = "KNJA130C_1KYOTO.frm";
                    _gradeLineMax = 6;
                    guardX2 = 1689;
                }
                addressMax = 2;
            } else if (param()._z010.in(Z010.miyagiken)) {
                if (param()._is133m) {
                    form = "KNJA133M_1MIYA.frm";
                    _gradeLineMax = 6;
                    guardX2 = 1691;
                    kanaminnum = 17;
                    _grdaddressMax133m = 1;
                } else {
                    form = "KNJA130C_1MIYA.frm";
                    _gradeLineMax = 4;
                    guardX2 = 1689;
                }
                addressMax = 2;
            } else if (param()._z010.in(Z010.mieken)) {
                if (param()._is133m) {
                    form = "KNJA133M_1MIE.frm";
                    _gradeLineMax = 6;
                } else {
                    form = "KNJA130C_1MIE.frm";
                    _gradeLineMax = 4;
                }
                addressMax = 2;
                guardX2 = 1712;
                kanaDefCharSize = (int) KNJSvfFieldModify.charHeightPixel("", 6.5);
            } else if (param()._z010.in(Z010.fukuiken)) {
//                if (param()._is133m) {
//              TODO: フォーム作成
//                } else {
                form =  is3 ? "KNJA130C_11FUKUI.frm" : "KNJA130C_1FUKUI.frm";
                _gradeLineMax = is3 ? 3 : 4;
                guardX2 = 2045;
                addressMax = 2;
//                }
            } else if (param()._z010.in(Z010.naraken)) {
                if (param()._is133m) {
                    form = "KNJA133M_1NARA.frm";
                    _gradeLineMax = 8;
                } else {
                    form = "KNJA130C_1NARA.frm";
                    _gradeLineMax = 4;
                }
                addressMax = 2;
                kanaDefCharSize = (int) KNJSvfFieldModify.charHeightPixel("", 7.0);
            } else if (param()._z010.in(Z010.tosa)) {
                form = "KNJA130C_11TOSA.frm";
                _gradeLineMax = 3;
                guardX2 = 2045;
                addressMax = 2;
            } else if (param()._z010.in(Z010.tokiwa)) {
                _gradeLineMax = 3;
                if (isNewForm(param(), pInfo)) {
                    form =  is3 ? "KNJA130C_11_2TOKIWA.frm" : null;
                    guardX2 = 1689;
                } else {
                    form =  is3 ? "KNJA130C_11TOKIWA.frm" : null;
                    stdX2 = 1752;
                    guardX2 = 1752;
                }
                addressMax = 3;
            } else if (param()._z010.in(Z010.tottori) || param()._z010.in(Z010.kyoai)) {
                form = (is3) ? "KNJA130C_11TORI.frm" : "KNJA130C_1TORI.frm";
                _gradeLineMax = (is3) ? 3 : 4;
                guardX2 = 1689;
                addressMax = 2;
            } else if (param()._z010.in(Z010.musashinohigashi)) {
                form = "KNJA130C_1_MUSAHIGA.frm";
                _gradeLineMax = 4;
                guardX2 = 1712;
                addressMax = 2;
            } else if (param()._z010.in(Z010.nishiyama)) {
                form = "KNJA130C_11NISHIYAMA.frm";
                _gradeLineMax = 3;
                guardX2 = 1689;
                addressMax = 3;
            } else if (param()._z010.in(Z010.sundaikoufu)) {
                form = "KNJA130C_11SUNDAIKOUFU.frm";
                _gradeLineMax = 3;
                guardX2 = 1689;
                addressMax = 3;
            } else if (param()._z010.in(Z010.chiyodaKudan)) {
                form = "KNJA130_1KUDAN.frm";
                _gradeLineMax = 4;
                guardX2 = 1689;
                addressMax = 2;
            } else if (param()._z010.in(Z010.tokiwagi)) {
                form = "KNJA130C_11TOKIWAGI.frm";
                _gradeLineMax = 3;
                guardX2 = 1689;
                addressMax = 3;
            } else if (param()._is133m) {
                _gradeLineMax = 6;
                if (param()._z010.in(Z010.tokyoto)) {
                    form = "KNJA133M_1TOKYO.frm";
                    guardX2 = 1691;
                } else if (param()._z010.in(Z010.sagaken)) {
                    form = "KNJA133M_1SAGA.frm";
                    guardX2 = 1691;
                } else if (KNJA130_0.isNewForm(param(), pInfo)) {
                    form = "KNJA133M_1KUMA.frm";
                    guardX2 = 1712;
                } else {
                    form = "KNJA133M_1.frm";
                    guardX2 = 1712;
                }
                addressMax = 2;
            } else {
                form = (is3) ? "KNJA130C_11.frm" : "KNJA130C_1.frm";
                _gradeLineMax = (is3) ? 3 : 4;
                guardX2 = 1689;
                addressMax = 3;
            }
            _addressMax = addressMax;
            if (param()._is133m) {
                if (-1 == _grdaddressMax133m) {
                    _grdaddressMax133m = _addressMax;
                }
            }
            svfVrSetForm(form, 1);
            if (param()._isOutputDebug) {
                log.info(" addressMax = " + _addressMax);
            }

            _kana = _form._formInfo.getFieldInfo("KANA", "KANA1_1", "KANA2_1", kanaDefCharSize, kanaminnum);
            _gKana = _form._formInfo.getFieldInfo("GUARD_KANA", "GUARD_KANA1_1", "GUARD_KANA2_1", kanaDefCharSize, kanaminnum);

            if (-1 != stdX2) _kana._x2 = stdX2;
            if (-1 != guardX2) _gKana._x2 = guardX2;

            _name = _form._formInfo.getFieldInfo("NAME1", "NAME1_1", "NAME2_1", charSize13, 24);
            _gName = _form._formInfo.getFieldInfo("GUARD_NAME1", "GUARD_NAME1_1", "GUARD_NAME2_1", charSize13, 24);

            if (-1 != stdX2) _name._x2 = stdX2;
            if (-1 != guardX2) _gName._x2 = guardX2;

            return form;
        }

        final String FLG_INKAN_SIZE_OSAKATOIN = "INKAN_SIZE_OSAKATOIN";
        final String FLG_INKAN_SIZE_MIEKEN = "INKAN_SIZE_MIEKEN";

        private String modifyForm1(final String form, final Student student, final PersonalInfo pInfo, final PrintGakuseki pg) {
            final Map<String, String> flgMap = new TreeMap<String, String>();

            final Map<Gakuseki, Integer> gakusekiPosMap = getGakusekiPosisionMap(student, pInfo, pg, pg._gakusekiList);

            if (param()._z010.in(Z010.osakatoin)) {

                final List<String> addTanninInkan = new ArrayList<String>();
                for (int j = 0; j < pg._gakusekiList.size(); j++) {
                    final Gakuseki gakuseki = pg._gakusekiList.get(j);
                    final int pos = gakusekiPosMap.get(gakuseki);

                    final List<Staff> studentStaff1HistList = param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV1, gakuseki._year);
                    final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
                    final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);
                    final Staff staff2Last = Util.last(param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV2, gakuseki._year), Staff.Null);
                    final Staff staff3Last = Util.last(param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV3, gakuseki._year), Staff.Null);

                    final List<Staff> yearStaffList = Util.flatten(Arrays.asList(Staff.getUniqueStaffList(staff1First, staff1Last), Staff.getUniqueStaffList(staff2Last), Staff.getUniqueStaffList(staff3Last)));
                    if (yearStaffList.size() > 2) {
                        param().logOnce(" add tannin inkan " + gakuseki._year + " / " + Util.listString(yearStaffList, 0));
                        addTanninInkan.add(String.valueOf(pos));
                    }
                }

                flgMap.put(FLG_INKAN_SIZE_OSAKATOIN, Util.mkString(addTanninInkan, "_").toString());
            } else if (param()._z010.in(Z010.mieken)) {

                final List<String> addTanninInkan = new ArrayList<String>();

                for (int j = 0; j < pg._gakusekiList.size(); j++) {
                    final Gakuseki gakuseki = pg._gakusekiList.get(j);
                    final int pos = gakusekiPosMap.get(gakuseki);

                    final List<Staff> studentStaff1HistList = param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV1, gakuseki._year);
                    final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
                    final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);
                    final Staff staff2Last = Util.last(param().getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV2, gakuseki._year), Staff.Null);

                    final List<Staff> yearStaffList = Util.flatten(Arrays.asList(Staff.getUniqueStaffList(staff1First, staff1Last), Staff.getUniqueStaffList(staff2Last)));
                    if (yearStaffList.size() > 1) {
                        param().logOnce(" add tannin inkan " + gakuseki._year + " / " + Util.listString(yearStaffList, 0));
                        addTanninInkan.add(String.valueOf(pos));
                    }
                }
                flgMap.put(FLG_INKAN_SIZE_MIEKEN, Util.mkString(addTanninInkan, "_").toString());
            }
            return modifyForm0(form, pInfo, pg, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_INKAN_SIZE_OSAKATOIN)) {

                final List<String> addTanninInkan = Arrays.asList(flgMap.get(FLG_INKAN_SIZE_OSAKATOIN).split("_"));
                final int scale = 2;
                final int ygap = -70;
                for (int g = 1, maxGrade = is3nenYou(pInfo) ? 3 : 4; g <= maxGrade; g++) {
                    final String sg = String.valueOf(g);

                    for (final SvfForm.ImageField imageField : svfForm.getElementList(SvfForm.ImageField.class)) {
                        SvfForm.ImageField modified = null;
                        if (imageField._fieldname.startsWith("STAFFBTM_1_" + sg)) {
                            // サイズ変更
                            final int xgap = 100;
                            final int y = svfForm.getField("STAFFNAME_2_" + sg + "_3")._position._y;
                            modified = imageField.setY(y + ygap).setHeight(imageField._height / scale).addX(xgap).setEndX(xgap + imageField._point._x + (imageField._endX - imageField._point._x) / scale);
                        } else if (imageField._fieldname.startsWith("STAFFBTM_2_" + String.valueOf(g))) {
                            // サイズ変更
                            final String n = imageField._fieldname.endsWith("C") ? imageField._fieldname.substring(imageField._fieldname.length() - 2, imageField._fieldname.length() - 1) : imageField._fieldname.substring(imageField._fieldname.length() - 1);
                            final int xgap = -200 + 120 * Integer.parseInt(n);
                            final int y = svfForm.getField("STAFFNAME_2_" + sg + "_3")._position._y + ygap;
                            modified = imageField.addX(xgap).setY(y).setHeight(imageField._height / scale).setEndX(xgap + imageField._point._x + (imageField._endX - imageField._point._x) / scale);
                        }
                        if (null != modified) {
                            svfForm.move(imageField, modified);
                        }
                    }
                }

                for (final String g : addTanninInkan) {
                    final SvfForm.ImageField imageField = svfForm.getImageField("STAFFBTM_2_" + g + "_1");
                    if (null != imageField) {
                        final int xgap = -200 + 120 * Integer.parseInt("3");
                        final int y = svfForm.getField("STAFFNAME_2_" + g + "_3")._position._y + ygap;
                        final SvfForm.ImageField mono = imageField.setFieldname("STAFFBTM_2_" + g + "_3").addX(xgap).setY(y).setHeight(imageField._height / scale).setEndX(xgap + imageField._point._x + (imageField._endX - imageField._point._x) / scale);
                        svfForm.addImageField(mono);
                        final SvfForm.ImageField color = mono.setFieldname(mono._fieldname + "C").setColor("9");
                        svfForm.addImageField(color);
                    }
                }
            }
            if (flgMap.containsKey(FLG_INKAN_SIZE_MIEKEN)) {
                final List<String> addTanninInkan = Arrays.asList(flgMap.get(FLG_INKAN_SIZE_MIEKEN).split("_"));

                final int tanninYgap = 50;
                for (final String g : addTanninInkan) {
                    final SvfForm.ImageField imageField = svfForm.getImageField("STAFFBTM_2_" + g);
                    if (null != imageField) {
                        final SvfForm.ImageField i1 = imageField.setFieldname("STAFFBTM_2_" + g + "_1C").setY(imageField._point._y - tanninYgap).setHeight(imageField._height).setEndX(imageField._endX).setColor("9");
                        svfForm.addImageField(i1);
                        final SvfForm.ImageField i2 = i1.setFieldname("STAFFBTM_2_" + g + "_2C").setY(imageField._point._y + tanninYgap);
                        svfForm.addImageField(i2);
                    }
                }
            }
            return true;
        }

        // 進学先・就職先等印刷
        private void printAfterGraduate(final Student student, final PersonalInfo pInfo) {
            final List<String> textList = pInfo._afterGraduatedCourseTextList;
            for (int i = 0, max = textList.size(); i < max; i++) {
                final String line = textList.get(i);
                final String field = "AFTER_GRADUATION" + String.valueOf(i + 1) + (getMS932ByteLength(line) > 50 && !param()._is133m ? "_2" : "") ;
                svfVrsOut(field, line);
            }

            if (param()._z010.in(Z010.kyoto)) { // 京都府のみ
                final String name;
                final String[] fields;
                if (isNewForm(param(), pInfo)) {
                    name = "進学・就職・進学就職・その他";
                    fields = new String[] {null, "1", "2", "3", null, null, "6_2"};
                } else {
                    name = "進学・就職・進学就職・家事・家業・その他";
                    fields = new String[] {null, "1", "2", "3", "4", "5", "6"};
                }
                svfVrsOut("AFTER_GRADUATION_NAME", name);
                if (StringUtils.isNumeric(pInfo._afterGraduatedCourseSenkouKindSub)) {
                    final int n = Integer.parseInt(pInfo._afterGraduatedCourseSenkouKindSub);
                    if (n < fields.length) {
                        svfVrsOut("AFTER_GRADUATION_LINE" + fields[n], "○");
                    }
                }
            }
        }

        // デフォルト印刷
        private void printDefault1(final DB2UDB db2, final Param param, final Student student, final PersonalInfo pInfo) {
            final String setDateFormat = setDateFormatInsertBlank(db2, null, param, param._formatDateDefaultYear);
            svfVrsOut("BIRTHDAY", setDateFormat2(null) + "生");
            svfVrsOut("J_GRADUATEDDATE_YE", setDateFormat); // 133M
            svfVrsOut("J_GRADUATEDDATE_Y", setDateFormat);
            svfVrsOut("ENTERDATE1", setDateFormat);
            svfVrsOut("TRANSFER_DATE_1", setDateFormat);
            svfVrsOut("TRANSFER_DATE_2", setDateFormat);
            svfVrsOut("TRANSFER_DATE3_1", setDateFormat + MARK_FROM_TO + setDateFormat);
            svfVrsOut("TRANSFER_DATE_4", setDateFormat);
            final String format = setNendoFormatInsertBlank(db2, null, param, param._formatDateDefaultYear);
            for (int i = 0; i < _gradeLineMax; i++) {
                svfVrsOut("YEAR_" + (i + 1), format);
            }
            if (param._schoolDiv.isGakunenSei(null, pInfo, student)) {
                if (pInfo._gakusekiList.size() > 0) {
                    final Gakuseki gakuseki = pInfo._gakusekiList.get(0);
                    final String gradeCd = gakuseki._gdat._gradeCd;
                    if (NumberUtils.isDigits(gradeCd)) {
                        for (int g = 1; g <= Integer.parseInt(gradeCd); g++) {
                            svfVrsOut("YEAR_" + String.valueOf(g), setNendoFormatInsertBlank(db2, null, param, gakuseki._year));
                        }
                    }
                }
            }

            svfVrsOut("GRADENAME1", pInfo._title);

            if (param._schoolDiv.containsGakunenSei(pInfo, student) || "1".equals(param.property(Property.seitoSidoYorokuPrintForm1StaffGrade))) {
                svfVrsOut("GRADENAME2", "学年");
            } else if (param()._z010.in(Z010.miyagiken) && param._schoolDiv.isTanniSei(null, pInfo, student)) {
                svfVrsOut("GRADENAME2", "年次");
            }

            if (param._z010.in(Z010.risshisha)) {
                final SvfField field = _form._formInfo.getSvfField("YEAR_1", true);
                if (null != field) {
                    svfVrAttribute("FOOTER", "X=" + field.x());
                }
                svfVrsOut("FOOTER", "指導要録電子化に伴い、校長印・担任印の押印は省略");
            }
        }

        // 学校情報印刷
        private void printSchoolInfo1(final Student student) {
            final CertifSchoolDat certifSchool = student.certifSchool(param());
            final List<String> names = new ArrayList();
            final List<String> attributes = new ArrayList();
            if (!param()._z010.in(Z010.naraken) && !StringUtils.isEmpty(certifSchool._certifSchoolDatRemark1)) { // 京都西山、栄
                names.add(certifSchool._certifSchoolDatRemark1);
                attributes.add("");
            }
            names.add(certifSchool._schoolName1);
            if (param()._z010.in(Z010.tosa)) {
                attributes.add(ATTR_CENTERING);
            } else {
                attributes.add("");
            }
            if (!StringUtils.isBlank(certifSchool._bunkouSchoolName)) {
                names.add("（" + certifSchool._bunkouSchoolName + "）");
                attributes.add("");
            }
            final String[] fields = {"NAME_gakko1", "NAME_gakko2"};
            for (int i = 0; i < Math.min(fields.length, names.size()); i++) {
                svfVrsOut(fields[i], names.get(i));
                final String attr = attributes.get(i);
                if (!StringUtils.isEmpty(attr)) {
                    svfVrAttribute(fields[i], attr);
                }
            }

            svfVrsOutForData(Arrays.asList("ADDRESS_gakko1",  "ADDRESS_gakko1_2", "ADDRESS_gakko1_3"), defstr(param()._schoolAddress1) + defstr(param()._schoolAddress2));
            if (param()._z010.in(Z010.tosa)) {
                svfVrAttribute("ADDRESS_gakko1", ATTR_CENTERING);
            }
            final String addrBunkouSrc = certifSchool._bunkouSchoolAddress1 + certifSchool._bunkouSchoolAddress2;
            if (!StringUtils.isBlank(addrBunkouSrc)) {
                svfVrsOutForData(Arrays.asList("ADDRESS_gakko2",  "ADDRESS_gakko2_2", "ADDRESS_gakko2_3"), "（" + addrBunkouSrc + "）");
            }
            if (param()._printSchoolZipcd) {
                svfVrsOut("ZIPCODE", Util.prepend("〒", param()._schoolZipcode));
            }
        }

        /**
         * 異動情報を印刷します。
         * @param param
         * @param student
         */
        private void printRyugakuKyugaku(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {

            final List<Gakuseki> gakusekiList = printGakuseki._gakusekiList;

            List<TransferRec> transferRecList = TransferRec.getGradeOnlyTransferList(param(), pInfo._isFirst, student._transferRecList, gakusekiList);
            if (!param()._is133m) {
                final List<Integer> indexList = TransferRec.getPrintTransferRecIndexList(transferRecList, param());
                transferRecList = getIndexedList(transferRecList, indexList);
            }
            for (final Iterator<TransferRec> it = transferRecList.iterator(); it.hasNext();) {
                final TransferRec tr = it.next();
                if (!(TransferRec.A004_NAMECD2_RYUGAKU.equals(tr._transfercd) || TransferRec.A004_NAMECD2_KYUGAKU.equals(tr._transfercd))) {
                    it.remove();
                }
            }
            final int abroadMax = param()._z010.in(Z010.kyoto) ? 1 : 99;  // 京都府は留学詳細は1件のみ表示可能

            final String slash = "／";
            int printAbroadCount = 0;
            int printCount = 1; // 休学・留学回数
            int printCount2 = 1; // 休学・留学回数 京都以外はiaと同じ
            for (final TransferRec tr : transferRecList) {
                if (param()._is133m) {
                    final int max = param()._z010.in(Z010.miyagiken) ? 1 : 2;
                    if (max < printCount) {
                        continue;
                    }
                }
                final String dateFromTo;
                if (param()._z010.in(Z010.kyoto)) {
                    dateFromTo = tr._sDateStr + MARK_FROM_TO + tr._eDateStr + " " + tr._name;
                } else {
                    dateFromTo = tr._sDateStr + MARK_FROM_TO + tr._eDateStr ;
                }
                svfVrsOut("TRANSFER_DATE3_" + printCount, dateFromTo);
                printCount += 1;
                if (TransferRec.A004_NAMECD2_RYUGAKU.equals(tr._transfercd)) { // 留学
                    if (abroadMax <= printAbroadCount) {
                        continue;
                    }
                    String reason;
                    if (param()._is133m) {
                        reason = "";
                        if (param()._z010.in(Z010.kyoto)) {
                            reason = defstr(tr._reason);
                        } else {
                            reason = tr._name + Util.prepend(slash, reason);
                        }
                    } else {
                        reason = defstr(tr._reason);
                        if (param()._z010.in(Z010.kyoto)) {
                        } else {
                            reason = tr._name + Util.prepend(slash, reason);
                        }
                    }
                    svfVrsOut("TRANSFERREASON3_" + printCount2 + "_1", reason);
                    svfVrsOut("TRANSFERREASON3_" + printCount2 + "_2", defstr(tr._place));
                    if (param()._z010.in(Z010.mieken)) {
                        // 三重県は3段目に住所を表示
                        svfVrsOut("TRANSFERREASON3_" + printCount2 + "_3", tr._address);
                    }
                    printCount2 += 1;
                    printAbroadCount += 1;
                    if (param()._z010.in(Z010.kyoto)) {
                        // 京都府は3段目に学年を表示
                        svfVrsOut("TRANSFERREASON3_" + printCount2 + "_1", tr._address);
                    }
                } else if (TransferRec.A004_NAMECD2_KYUGAKU.equals(tr._transfercd)) { // 休学
                    if (param()._z010.in(Z010.kyoto)) {
                    } else {
                        final boolean isPrintReason = !param()._is133m && param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.meikei) || param()._z010.in(Z010.naraken) || !"1".equals(param().property(Property.useAddrField2));
                        svfVrsOut("TRANSFERREASON3_" + printCount2 + "_1", tr._name + (isPrintReason ? Util.prepend(slash, tr._reason) : ""));
                        svfVrsOut("TRANSFERREASON3_" + printCount2 + "_2", defstr(tr._place));
                        printCount2 += 1;
                    }
                }
            }
            final Gakuseki dropGakuseki = printGakuseki._dropGakuseki;
            if (null != dropGakuseki) {
                if (param()._z010.in(Z010.mieken)) {
                    if (printCount == 1) {
                        printCount += 1; // デフォルト日付の次の行に表示
                    }
                    svfVrsOut("TRANSFER_DATE3_" + printCount, dropGakuseki.getGradeOrNendo(param()) + "原級留置");
                    printCount += 1;
                } else if (param()._z010.in(Z010.hibarigaoka)) {
                    if (!printGakuseki._isLastPrintGakuseki) {
                        // 雲雀丘は留年した年度を留学・休学欄に表記
                        if (null != dropGakuseki && NumberUtils.isDigits(dropGakuseki._year)) {
                            svfVrsOut("TRANSFER_DATE3_" + printCount, KNJ_EditDate.h_format_JP(db2, String.valueOf(Integer.parseInt(dropGakuseki._year) + 1) + "-03-31") + "　原級留置");
                            printCount += 1;
                        }
                    }
                }
            }

            if (param()._isPrintHosoku && pInfo._hosokuTransferRecIndexList.size() > 1) {
                svfVrsOut("HOSOKU_TRANSFER", "※" + HOSOKU_ARI);
            }
        }

        // 入学・編入学・転入学
        private void printNyugaku(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final int yearMin, final int yearMax) {
            final String NYUGAKU = "入　学";
            final String HENNYUGAKU = "編入学";
            final String TENNYUGAKU = "転入学";
            final String mongonDaiXgakunen = "第　学年";
            final boolean isPrintGakunen = param()._schoolDiv.isGakunenSei(null, pInfo, student) || student.certifSchool(param())._isGenkyuRyuchi || param()._z010.in(Z010.kyoto) && param()._is133m || param()._z010.in(Z010.mieken) && !param()._is133m;
            if (param()._isOutputDebug) {
                log.info(" isPrintGakunen = " + isPrintGakunen);
            }
            final boolean isUchikeshiGakunen = !param()._schoolDiv.isGakunenSei(null, pInfo, student) && isPrintGakunen && param()._z010.in(Z010.mieken);
            final int uchiKeshiGakunenKeta = getMS932ByteLength(mongonDaiXgakunen);
            final String torikeshisen = isPrintGakunen ? "＝＝＝＝＝＝＝" : "＝＝＝";
            final int keta = getMS932ByteLength(torikeshisen);

            final int x;
            if (param()._is133m) {
                final int charWidth = 55;
                x = (param()._z010.in(Z010.tokyoto) || param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.sagaken) ? 2775 : 2735) + (isPrintGakunen ? 0 : charWidth * 4);
            } else {
                int x0;
                int charWidth;
                if (param()._z010.in(Z010.tokiwa)) {
                    x0 = isNewForm(param(), pInfo) ? 2737 : 2697;
                    charWidth = 55;
                } else {
                    x0 = _form._formInfo.getFieldX(fieldENTERDIV1, 10000);
                    final double charPoint = _form._formInfo.getFieldCharSize(fieldENTERDIV1, 999);
                    charWidth = (int) KNJSvfFieldModify.charPointToPixel("", charPoint, 1);
                    final int defaultKeta = getMS932ByteLength(NYUGAKU) + uchiKeshiGakunenKeta; // 14桁
                    final int fieldKeta = _form._formInfo.getFieldLength(fieldENTERDIV1, defaultKeta);
                    if (fieldKeta > defaultKeta) {
                        x0 = x0 + (fieldKeta - defaultKeta) * charWidth / 2;
                    }
                    if (param()._isOutputDebug) {
                        log.info(" ENTERDIV1 x0 = " + x0 + ", charPoint = " + charPoint + ", charWidth = " + charWidth);
                    }
                }
                x = x0 + (isPrintGakunen ? 0 : charWidth * 4);
            }
            if (param()._z010.in(Z010.tosa)) {
                svfUchikeshi(fieldLine1, keta, null); // 打ち消し線
                svfUchikeshi(fieldLine2, keta, null); // 打ち消し線
            } else {
                svfUchikeshi(fieldLine1, keta, "X=" + x + ","); // 打ち消し線
                svfUchikeshi(fieldLine2, keta, "X=" + x + ","); // 打ち消し線
            }

            if (isPrintGakunen) {
                svfVrsOut(fieldENTERDIV1, "第" + (param()._z010.in(Z010.mieken) ? " 1" : "  ") + "学年" + NYUGAKU);
                svfVrsOut(fieldENTERDIV2, mongonDaiXgakunen + HENNYUGAKU);
                svfVrsOut("TENNYU", mongonDaiXgakunen + TENNYUGAKU);
            } else {
                svfVrsOut(fieldENTERDIV1, NYUGAKU);
                svfVrsOut(fieldENTERDIV2, HENNYUGAKU);
            }

            boolean isTennyuran = false;
            String title = null;
            String tenDate = null;
            if (pInfo.isEntDivTenseki(param(), yearMin, yearMax)) {
                title = "転籍";
                isTennyuran = true;
                tenDate = pInfo._entDate;

//          } else if (useTenkaTenseki && pInfo.isTenseki(param(), yearMin, yearMax)) { // -> 入学区分を転籍に変更して扱うため上の処理に流れるはず。不要なのでコメントにする
//              title = "転籍";
//              isTennyuran = true;
//              tenDate = pInfo._tenseki.date();

            } else if (pInfo.isEntDivTenka(param(), yearMin, yearMax)) {
                title = "転科";
                isTennyuran = true;
                tenDate = pInfo._entDate;

            } else if (!param()._z010.in(Z010.miyagiken) && pInfo.isTenka(param(), yearMin, yearMax)) {
                title = "転科";
                isTennyuran = true;
                tenDate = addDay(pInfo._tenka.date(), 1);

            } else if (pInfo.isTennyu()) {
                // 転入学
                title = defstr(pInfo._entDivName2, TENNYUGAKU);
                isTennyuran = true;
                tenDate = pInfo._entDate;
            }
            final String gradeWhenNull;
            if (isTennyuran) {
                gradeWhenNull = " ";
            } else {
                if (pInfo.isHennyu()) {
                    // 編入学を印字
                    gradeWhenNull = " ";
                } else {
                    // 入学を印字
                    gradeWhenNull = "1";
                }
            }
            final boolean useEnterdiv2 = !(param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.mieken));
            final String entGradeName = getGradeName(isUchikeshiGakunen ? null : (param()._replacePrintGradeCdWithGrade ? pInfo._entYearGrade : pInfo._entYearGradeCd), gradeWhenNull);
            final String fldEnterDate;
            final String fldLine;
            final String fldEnterdiv;
            if (isTennyuran) {
                fldEnterdiv = "TENNYU";
                fldEnterDate = "TRANSFER_DATE_1";
                fldLine = hasField("TENNYU_LINE") ? "TENNYU_LINE" : "";
            } else if (pInfo.isHennyu()) {
                if (useEnterdiv2) {
                    fldEnterDate = "ENTERDATE2";
                } else {
                    // 1行目に表示
                    fldEnterDate = "ENTERDATE1";
                }
                fldEnterdiv = fieldENTERDIV2;
                fldLine = fieldLine2;
            } else {
                fldEnterDate = "ENTERDATE1";
                fldLine = fieldLine1;
                fldEnterdiv = fieldENTERDIV1;
            }

            String enterDiv = null;
            String entDivName = null;
            if (isTennyuran) {
                // 転入
                if (param()._isOutputDebug) {
                    log.info(" tennnyu " + pInfo._schregno + " entDiv = " + pInfo._entDiv);
                }
                if (param()._is133m && !param()._z010.in(Z010.mieken)) {
                    if (isPrintGakunen) {
                        enterDiv = entGradeName + title;
                    } else if (null != pInfo._entDivName2) {
                        enterDiv = pInfo._entDivName2;
                    }
                } else {
                    String title2 = title;
                    if (null != title && title.length() == 2) {
                        final char ch0 = title.charAt(0);
                        final char ch1 = title.charAt(1);
                        title = ch0 + "　" + ch1;
                        title2 = ch0 + "　　" + ch1;
                    }
                    if (isUchikeshiGakunen) {
                        svfUchikeshi(fldLine, uchiKeshiGakunenKeta, ""); // 打ち消し線
                    }
                    if (param()._z010.in(Z010.miyagiken) && param()._schoolDiv.isTanniSei(null, pInfo, student)) {
                        if (TENNYUGAKU.equals(title2)) {
                            enterDiv = dateNendoWareki(db2, pInfo._entDate) + title;
                        } else {
                            enterDiv = dateNendoWareki(db2, pInfo._entDate);
                        }
                    } else if (isPrintGakunen) {
                        if (TENNYUGAKU.equals(title2)) {
                            enterDiv = entGradeName + title;
                        } else {
                            svfVrAttribute(fldEnterdiv, ATTR_LEFT);
                            enterDiv = entGradeName + (param()._z010.in(Z010.mieken) ? defstr(title) : "　　");
                        }
                    }
                    if (!TENNYUGAKU.equals(title2)) {
                        if (hasField("LINE_TRANSFER")) {
                            svfUchikeshi("LINE_TRANSFER", 10, null);
                            svfVrsOut("TRANSFER_TITLE1", title2);
                        } else if (null == enterDiv) {
                            enterDiv = defstr(pInfo._entDivName2, pInfo._entDivName);
                        }
                    }
                }
                svfVrsOut(fldEnterDate, setDateFormatInsertBlank(db2, formatDate1(db2, tenDate, param()), param(), param()._formatDateDefaultYear));
            } else {
                // 入学・編入
                svfVrsOut(fldEnterDate, setDateFormatInsertBlank(db2, formatDate1(db2, pInfo._entDate, param()), param(), param()._formatDateDefaultYear));
                if (isUchikeshiGakunen) {
                    svfUchikeshi(fldLine, uchiKeshiGakunenKeta, ""); // 打ち消し線
                } else {
                    svfVrAttribute(fldLine, "X=10000"); // 打ち消し線消去
                }
                if (pInfo.isHennyu()) {
                    // 編入学を印字
                    entDivName = defstr(pInfo._entDivName2, HENNYUGAKU);
                } else {
                    // 入学を印字
                    entDivName = defstr(pInfo._entDivName2, NYUGAKU);
                }
                if (param()._is133m) {
                    if (isPrintGakunen) {
                        enterDiv = entGradeName + entDivName;
                    } else if (null != pInfo._entDivName2) {
                        enterDiv = pInfo._entDivName2;
                    } else if (null != entDivName) {
                        enterDiv = entDivName;
                    }
                } else {
                    final boolean isPrintZaigakusubekiKikan = null != pInfo._zaigakusubekiKikan;
                    if (param()._isOutputDebug) {
                        log.info(" lastAnotherSchoolHistDat = " + pInfo._lastAnotherSchoolHistDat);
                    }
                    if (isPrintZaigakusubekiKikan) {
                        enterDiv = dateNendoWareki(db2, pInfo._entDate) + entDivName;
                    } else if (isPrintGakunen) {
                        enterDiv = entGradeName + entDivName;
                    } else if (null != entDivName) {
                        enterDiv = entDivName;
                    }
                }
            }
            if (null != enterDiv) {
                svfVrsOut(fldEnterdiv, enterDiv);
            }

            // 事由
            if (isTennyuran) {
                // 転入
                final boolean useField2 = getMS932ByteLength(pInfo._entAddr) > 50 || getMS932ByteLength(pInfo._entAddr2) > 50;
                if (param()._is133m && !(param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.mieken))) {
                    if (null != pInfo._entSchool) {
                        svfVrsOut("TRANSFERREASON1_1", pInfo._entSchool);
                    }
                    if ("1".equals(param().property(Property.useAddrField2))) {
                        if (null != pInfo._entAddr2) {
                            svfVrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), pInfo._entAddr);
                            svfVrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), pInfo._entAddr2);
                        } else {
                            svfVrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), pInfo._entAddr);
                        }
                    } else {
                        svfVrsOutNotNull("TRANSFERREASON1_2", pInfo._entAddr);
                    }
                } else {
                    final boolean isInputAnotherSchoolHistDat = !(param()._is133m && param()._z010.in(Z010.kyoto));
                    if (isInputAnotherSchoolHistDat && null != pInfo._zaigakusubekiKikan && !"1".equals(KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "KAIGAI")) && !pInfo._lastAnotherSchoolHistDat.isEmpty()) {
                        // 国内
                        svfVrsOut("TRANSFERREASON1_1", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_NAME"));
                        svfVrsOut("TRANSFERREASON1_2", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "COURSE_MAJOR"));
                        svfVrsOut("TRANSFERREASON1_3", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_ADDR1"));
                        svfVrsOut("TRANSFERREASON1_4", pInfo._zaigakusubekiKikan);
                    } else {
                        final boolean isPrintTennyuEntReason = param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.mieken) || param()._z010.in(Z010.naraken) || "1".equals(param().property(Property.seitoSidoYorokuPrintEntGrdReason));
                        if (isPrintTennyuEntReason) {
                            svfVrsOut("TRANSFERREASON1_1", pInfo._entReason);
                        } else if ("1".equals(param().property(Property.useAddrField2)) || param()._z010.in(Z010.kyoto)) {
                        } else {
                            svfVrsOut("TRANSFERREASON1_1", kakko(pInfo._entReason));
                        }
                        if (param()._is133m && param()._z010.in(Z010.kyoto)) {
                            svfVrsOut("TRANSFERREASON0_1", pInfo._zaigakusubekiKikan);
                        }

                        svfVrsOutNotNull("TRANSFERREASON1_2", pInfo._entSchool);
                        if ("1".equals(param().property(Property.useAddrField2))) {
                            if (null != pInfo._entTensekiReason) {
                                final boolean isPrintTenkaReason = !param()._z010.in(Z010.miyagiken);
                                if (pInfo.isEntDivTenseki(param(), yearMin, yearMax)) {
                                    pInfo._entAddr2 = pInfo._entTensekiReason;
                                } else if (pInfo.isEntDivTenka(param(), yearMin, yearMax)) {
                                    pInfo._entAddr2 = pInfo._entTensekiReason;
                                } else if (isPrintTenkaReason && pInfo.isTenka(param(), yearMin, yearMax)) {
                                    pInfo._entAddr2 = pInfo._entTensekiReason;
                                }
                            }

                            if (null != pInfo._entAddr2) {
                                if (param()._z010.in(Z010.kyoto)) {
                                    svfVrsOut("TRANSFERREASON1_4" + (useField2 ? "_2" : ""), pInfo._entAddr);
//                            svfVrsOut("TRANSFERREASON1_5" + (useField2 ? "_2" : ""), pInfo._entAddr2);
                                    svfVrsOut("TRANSFERREASON1_6" + (useField2 ? "_2" : ""), pInfo._entAddr2);
                                } else {
                                    svfVrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), pInfo._entAddr);
                                    svfVrsOut("TRANSFERREASON1_4" + (useField2 ? "_2" : ""), pInfo._entAddr2);
                                }
                            } else {
                                svfVrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), pInfo._entAddr);
                            }
                        } else {
                            svfVrsOutNotNull("TRANSFERREASON1_3", pInfo._entAddr);
                        }
                    }
                }
            } else {
                // 入学・編入
                if (param()._is133m) {
                    final List<String> cand = new ArrayList<String>();
                    cand.add(pInfo._zaigakusubekiKikan);
                    if ("1".equals(param().property(Property.seitoSidoYorokuPrintEntGrdReason))) {
                        cand.add(pInfo._entReason);
                    }
                    cand.addAll(Arrays.asList(pInfo._entSchool, pInfo._entAddr, pInfo._entAddr2));
                    final List<String> vals = new ArrayList<String>();
                    for (final String val : cand) {
                        if (!StringUtils.isEmpty(val)) {
                            vals.add(val);
                        }
                    }

                    final List<String> fieldnames = new ArrayList<String>();
                    for (final String fieldname : Arrays.asList("ENTERRESONS0", "ENTERRESONS1", "ENTERRESONS2", "ENTERRESONS3", "ENTERRESONS4")) {
                        if (hasField(fieldname)) {
                            fieldnames.add(fieldname);
                        }
                    }
                    for (int i = 0; i < Math.min(fieldnames.size(), vals.size()); i++) {
                        svfVrsOutForData(Arrays.asList(fieldnames.get(i), fieldnames.get(i) + "_2"), vals.get(i));
                    }

                } else {
                    boolean printData = false;
                    if (!"1".equals(param().property(Property.useAddrField2)) && !param()._z010.in(Z010.kyoto) && !StringUtils.isEmpty(pInfo._entReason)) {
                        svfVrsOut("ENTERRESONS3", kakko(pInfo._entReason));
                        printData = true;
                    }
                    final boolean isPrintZaigakusubekiKikan = null != pInfo._zaigakusubekiKikan;
                    if (isPrintZaigakusubekiKikan) {
                        if (!"1".equals(KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "KAIGAI"))) { // 国内
                            svfVrsOut("ENTERRESONS0", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_NAME"));
                            svfVrsOut("ENTERRESONS1", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "COURSE_MAJOR"));
                            svfVrsOut("ENTERRESONS2", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_ADDR1"));
                        }
                        svfVrsOut("ENTERRESONS3", pInfo._zaigakusubekiKikan);
                        printData = true;
                    }
                    boolean isPrintEnterText = false;
                    if (isPrintZaigakusubekiKikan) {
                    } else if (isPrintGakunen) {
                        isPrintEnterText = true;
                    } else if (null != entDivName) {
                        isPrintEnterText = true;
                    }

                    if (isPrintEnterText) {
                        if (isPrintHenyuEntTextToKeireki(pInfo)) {
                            // 入学前経歴に印字
                            printData = true;
                        } else if (param()._z010.in(Z010.miyagiken)) {
                            if (!pInfo._lastAnotherSchoolHistDat.isEmpty()) {
                                svfVrsOut("ENTERRESONS0", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_NAME"));
                                svfVrsOut("ENTERRESONS1", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "COURSE_MAJOR"));
                                svfVrsOut("ENTERRESONS2", KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "ANOTHER_SCHOOL_ADDR1"));
                                printData = true;
                            }
                        }
                    }
                    if (!printData) {
                        final List<String> fieldnames = new ArrayList<String>();
                        for (final String fieldname : Arrays.asList("ENTERRESONS0", "ENTERRESONS1", "ENTERRESONS2", "ENTERRESONS3", "ENTERRESONS4")) {
                            if (hasField(fieldname)) {
                                fieldnames.add(fieldname);
                            }
                        }
                        final List<String> cand = new ArrayList<String>();
                        final boolean isPrintReason = param()._z010.in(Z010.miyagiken) || pInfo.isHennyu() && param()._z010.in(Z010.naraken) || "1".equals(param().property(Property.seitoSidoYorokuPrintEntGrdReason));
                        if (isPrintReason) {
                            cand.add(pInfo._entReason);
                        }
                        cand.addAll(Arrays.asList(pInfo._entSchool, pInfo._entAddr, pInfo._entAddr2));
                        final List<String> vals = new ArrayList<String>();
                        for (final String val : cand) {
                            if (!StringUtils.isEmpty(val)) {
                                vals.add(val);
                            }
                        }
                        for (int i = 0; i < Math.min(fieldnames.size(), vals.size()); i++) {
                            svfVrsOutForData(Arrays.asList(fieldnames.get(i), fieldnames.get(i) + "_2"), vals.get(i));
                        }
                    }
                }
            }
        }

        private String grdDivName(final PersonalInfo pInfo, final int yearMin, final int yearMax) {
            String kubun = "";
            if (pInfo.isTengaku()) {
                kubun = "転学";
            } else if (pInfo.isTaigaku()){
                kubun = "退学";
            } else if (pInfo.isGrdDivTenseki(param(), yearMin, yearMax)) {
                kubun = "転籍";
            } else if (pInfo.isGrdDivTenka(param(), yearMin, yearMax)) {
                kubun = "転科";
            } else if (pInfo.isJoseki()) {
                kubun = "除籍";
            }
            return kubun;
        }

        // 転学・退学・卒業
        private void printTengakuTaigakuSotsugyo(final DB2UDB db2, final PersonalInfo pInfo, final int yearMin, final int yearMax) {
            final boolean isGrdDivTensekiTenka = pInfo.isGrdDivTenseki(param(), yearMin, yearMax) || pInfo.isGrdDivTenka(param(), yearMin, yearMax);
            final boolean isTengakuTaigakuran = pInfo.isTengaku() || pInfo.isTaigaku() || isGrdDivTensekiTenka || pInfo.isJoseki();
            if (isTengakuTaigakuran) {
                if (param()._z010.in(Z010.fukuiken)) {
                    if (!Util.dateBetweenYear(param(), pInfo._grdDate, yearMin, yearMax, "転学日付チェック ")) {
                        return;
                    }
                }

                if (param()._isOutputDebug) {
                    log.info(" tengakutaigaku : " + pInfo._schregno + ", grdDiv = " + pInfo._grdDiv + " (school = " + pInfo._grdSchool + ", reason = " + pInfo._grdReason + ", addr = " + pInfo._grdAddr + ", addr2 = " + pInfo._grdAddr2 + ")");
                }
                // 転学・退学日付
                svfVrsOut("TRANSFER_DATE_2", setDateFormatInsertBlank(db2, formatDate1(db2, pInfo._grdDate, param()), param(), param()._formatDateDefaultYear));

                // 転学学年
                final boolean isNotPrintTengakuGrade = param()._z010.in(Z010.miyagiken, Z010.mieken, Z010.osakashinnai) || param()._is133m && param()._z010.in(Z010.hirokoudai);
                String tengakuGrade = null;
                if (!isNotPrintTengakuGrade) {
                    final String grdYearGrade = param()._replacePrintGradeCdWithGrade ? pInfo._grdYearGrade : pInfo._grdYearGradeCd;
                    final boolean isTsushinsei = param()._is133m && !param()._z010.in(Z010.kyoto);
                    if (isTsushinsei) {
                        tengakuGrade = getGradeName(grdYearGrade, " ");
                    } else {
                        // tengaku_GRADEに編集式が設定されている
//                      if (namecd2 == 3) {
//                          svfVrsOut("tengaku_GRADE", gradecdPlus1When0331(tr._gradeCd, tr._sDate));
//                      } else {
//                          svfVrsOut("tengaku_GRADE", tr._gradeCd);
//                      }
                        tengakuGrade = StringUtils.isNumeric(grdYearGrade) ? " " + String.valueOf(Integer.parseInt(grdYearGrade)) : grdYearGrade;
                    }
                }
                if (null != tengakuGrade) {
                    svfVrsOut("tengaku_GRADE", tengakuGrade);
                }

                // 転学・退学名称
                String grdDivName = grdDivName(pInfo, yearMin, yearMax);
                if (!param()._is133m && param()._z010.in(Z010.miyagiken) && isGrdDivTensekiTenka) {
                    if (null != grdDivName && grdDivName.length() == 2) {
                        final char ch0 = grdDivName.charAt(0);
                        final char ch1 = grdDivName.charAt(1);
                        grdDivName = ch0 + "　　" + ch1;
                    }
                    svfUchikeshi("LINE_TRANSFER2", 11, null);
                    svfVrsOut("TRANSFER_TITLE2", grdDivName);
                } else {
                    svfVrsOut("KUBUN", grdDivName);
                }

                // 事由等
                final String TRANSFERREASON2_1 = "TRANSFERREASON2_1";
                final String TRANSFERREASON2_2 = "TRANSFERREASON2_2";
                final String TRANSFERREASON2_3 = "TRANSFERREASON2_3";
                final String TRANSFERREASON2_4 = "TRANSFERREASON2_4";
                final boolean is130cFormat = param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.mieken) || param()._z010.in(Z010.naraken);
                if (param()._is133m && !is130cFormat) {
                    svfVrsOut(TRANSFERREASON2_1, pInfo._grdSchool);
                    if ("1".equals(param().property(Property.useAddrField2))) {
                        final boolean useField2 = getMS932ByteLength(pInfo._grdAddr) > 50 || getMS932ByteLength(pInfo._grdAddr2) > 50;
                        svfVrsOut(TRANSFERREASON2_2 + (useField2 ? "_2" : ""), pInfo._grdAddr);
                        svfVrsOut(TRANSFERREASON2_3 + (useField2 ? "_2" : ""), pInfo._grdAddr2);
                    } else {
                        svfVrsOut(TRANSFERREASON2_2, pInfo._grdAddr);
                    }
                } else {

                    String reason = "";
                    if (null != pInfo._grdTensekiReason && isGrdDivTensekiTenka) {
                        pInfo._grdAddr2 = pInfo._grdTensekiReason;
                    }
                    if (param()._z010.in(Z010.kyoto)) {
                        final String school = Util.insertBetween(pInfo._grdSchool, pInfo._tengakuSakiGrade, "　");
                        if (!StringUtils.isBlank(school)) {
                            svfVrsOut(TRANSFERREASON2_2, school);
                        }
                        if (pInfo.isJoseki()) {
                            // 京都は除籍は転学学校名欄に除籍事由を表示する
                            svfVrsOut(TRANSFERREASON2_2, pInfo._grdReason);
                        }

                        final boolean useField2 = getMS932ByteLength(pInfo._grdAddr) > 50 || getMS932ByteLength(pInfo._grdAddr2) > 50;
                        if (null != pInfo._grdAddr2) {
                            svfVrsOut(TRANSFERREASON2_4 + (useField2 ? "_2" : ""), pInfo._grdAddr);
//                          svfVrsOut("TRANSFERREASON2_5" + (useField2 ? "_2" : ""), pInfo._grdAddr2);
                            svfVrsOut("TRANSFERREASON2_6" + (useField2 ? "_2" : ""), pInfo._grdAddr2);
                        } else {
                            svfVrsOut(TRANSFERREASON2_3 + (useField2 ? "_2" : ""), pInfo._grdAddr);
                        }
                    } else if (param()._z010.in(Z010.naraken)) {

                        svfVrsOut(TRANSFERREASON2_1, pInfo._grdSchool);
                        final String addr = pInfo._grdAddr;
                        final String addr2 = Util.insertBetween(pInfo._grdAddr2, pInfo._tengakuSakiGrade, "　");
                        final boolean useField2 = getMS932ByteLength(addr) > 50 || getMS932ByteLength(addr2) > 50;
                        svfVrsOut(useField2 ? TRANSFERREASON2_2 + "_2" : TRANSFERREASON2_2, addr);
                        svfVrsOut(useField2 ? TRANSFERREASON2_3 + "_2" : TRANSFERREASON2_3, addr2);

                    } else {
                        final boolean isNotAddTengakuSakiGradeToLine3 = param()._z010.in(Z010.mieken);
                        String school = "";
                        if (isNotAddTengakuSakiGradeToLine3) {
                            school = defstr(pInfo._grdSchool);
                        } else {
                            school = Util.insertBetween(pInfo._grdSchool, pInfo._tengakuSakiGrade, "　");
                        }
                        final boolean isPrintGrdReason = param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.mieken) && pInfo.isJoseki() || "1".equals(param().property(Property.seitoSidoYorokuPrintEntGrdReason));
                        if (isPrintGrdReason) {
                            reason = pInfo._grdReason;
                        } else if ("1".equals(param().property(Property.useAddrField2))) {
                        } else {
                            reason = kakko(pInfo._grdReason);
                        }
                        final boolean useField2 = (getMS932ByteLength(pInfo._grdAddr) > 50 || getMS932ByteLength(pInfo._grdAddr2) > 50) && hasField(TRANSFERREASON2_3 + "_2") && (!"1".equals(param().property(Property.useAddrField2)) || "1".equals(param().property(Property.useAddrField2)) && hasField(TRANSFERREASON2_4 + "_2"));
                        if (useField2) {
                            if (!StringUtils.isBlank(reason)) {
                                svfVrsOut(TRANSFERREASON2_1, reason);
                            }
                            if (!StringUtils.isBlank(school)) {
                                svfVrsOut(TRANSFERREASON2_2, school);
                            }
                            svfVrsOut(TRANSFERREASON2_3 + "_2", pInfo._grdAddr);
                            svfVrsOut(TRANSFERREASON2_4 + "_2", pInfo._grdAddr2);
                        } else {
                            final List<String> remarks = new ArrayList<String>();
                            for (final String item : Arrays.asList(reason, school, pInfo._grdAddr2, pInfo._grdAddr)) {
                                if (!StringUtils.isBlank(item)) {
                                    remarks.add(item);
                                }
                            }
                            if (param()._isOutputDebug) {
                                log.info(" grd remarks = " + remarks);
                            }
                            final List<String> fields;
                            if (remarks.size() > 3 && hasField(TRANSFERREASON2_4)) {
                                fields = Arrays.asList(TRANSFERREASON2_1, TRANSFERREASON2_2, TRANSFERREASON2_3, TRANSFERREASON2_4);
                            } else if (hasField(TRANSFERREASON2_4)) {
                                fields = Arrays.asList(TRANSFERREASON2_2, TRANSFERREASON2_3, TRANSFERREASON2_4);
                            } else {
                                fields = Arrays.asList(TRANSFERREASON2_1, TRANSFERREASON2_2, TRANSFERREASON2_3);
                            }
                            for (int i = 0; i < Math.min(remarks.size(), fields.size()); i++) {
                                svfVrsOut(fields.get(i), remarks.get(i));
                            }
                        }
                    }
                }
            } else if (pInfo.isSotsugyo()) { // 卒業
                final String defaultGengoYear;
                if (null != pInfo._grdDate) {
                    defaultGengoYear = null;
                } else if (null == pInfo._entDate) {
                    defaultGengoYear = param()._formatDateDefaultYear;
                } else {
                    defaultGengoYear = String.valueOf(getNendo(getCalendarOfDate(pInfo._entDate)) + 3);
                }
                svfVrsOut("TRANSFER_DATE_4", setDateFormatInsertBlank(db2, formatDate1(db2, pInfo._grdDate, param()), param(), defaultGengoYear));
                if (!param()._is133m && pInfo._grdNo != null) {
                    svfVrsOut("FIELD1", pInfo._grdNo); // 卒業台帳番号
                }
            }
        }

        /**
         * 3/31転学の場合学年コード+1、それ以外は学年コードを得る。
         * @param gradecd 学年コード
         * @param date 転学日付
         * @return 学年コード
         */
        private static String gradecdPlus1When0331(final String gradecd, final String date) {
            if (!StringUtils.isNumeric(gradecd) || !isDayOfMonth(date, 3, 31)) {
                return gradecd;
            }
            return String.valueOf(1 + Integer.parseInt(gradecd));

        }

        private static String getGradeName(final String gradeCd, final String nullGradeCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("第 ");
            if (StringUtils.isNumeric(gradeCd)) {
                stb.append(String.valueOf(Integer.parseInt(gradeCd)));
            } else {
                stb.append(nullGradeCd);
            }
            stb.append("学年");
            return stb.toString();
        }

        /**
         * 生徒住所履歴を印刷します。
         * @param student
         */
        private void printAddress(final DB2UDB db2, final PersonalInfo pInfo) {
            final List<Address> printAddressList;
            if (param()._is133m) {
                printAddressList = getPrintAddressRecListM(pInfo._addressList, _addressMax);
            } else {
                printAddressList = getIndexedList(pInfo._addressList, Address.getPrintAddressIndex(param(), "address", pInfo._addressList, _addressMax));
            }
            for (int i = 1, num = printAddressList.size(); i <= num; i++) {
                final Address address = printAddressList.get(i - 1);
                final boolean islast = i == num;
                printZipCode("ZIPCODE", i, address.getZipCode(), islast, "ZIPCODELINE");

                printAddr12(db2, "ADDRESS", "ADDRESS", i, address, islast, "ADDRESSLINE");
            }
            if (param()._isPrintHosoku && pInfo._hosokuAddressIndexList.size() > 0) {
                svfVrsOut("HOSOKU_ADDR", "※" + HOSOKU_ARI);
            }
        }

        private void printAddr12(final DB2UDB db2, final String field, final String sundaiField, final int i, final Address address, final boolean islast, final String linefield) {

            final String addr1 = defstr(address.getAddr1());
            String addr2 = defstr(address.getAddr2());
            boolean isPrintAddr2 = address.isPrintAddr2();
            final String addr = addr1 + (isPrintAddr2 ? addr2 : "");
            if (param()._z010.in(Z010.sundaikoufu) && getMS932ByteLength(addr) <= 38) {
                final String nf = sundaiField + "_B" + String.valueOf(i);
                final String linef = sundaiField + "_B" + String.valueOf(i) + "_LINE";

                svfVrsOut(nf, addr);
                printAddressLine(addr, islast, linef);
            } else {
                final int keta = getMS932ByteLength(addr1);
                printAddr1(field, i, addr1, keta, islast, linefield);

                if (param()._isPrintRirekiDate && i != 1 && null != address.getIssuedate()) {
                    addr2 += henkouHiduke(db2, address.getIssuedate());
                    isPrintAddr2 = true;
                }

                if (isPrintAddr2) {
                    printAddr2(field, i, addr2, keta, islast, linefield);
                }
            }
        }

        private void printAddr1(final String field, int i, final String addr1, final int keta1, final boolean islast, final String linefield) {
            String p = null;
            if ("1".equals(param().property(Property.useAddrField2)) && 50 < keta1) {
                p = "_1_3";
            } else if (40 < keta1) {
                p = "_1_2";
            } else if (0 < keta1) {
                p = "_1_1";
            }
            if (p != null) {
                svfVrsOut(field + i + p, addr1);
                printAddressLine(addr1, islast, linefield + i + p);
            }
        }

        private void printAddr2(final String field, int i, final String addr2, final int keta1, final boolean islast, final String linefield) {
            final int keta2 = getMS932ByteLength(addr2);
            String p = null;
            if ("1".equals(param().property(Property.useAddrField2)) && (50 < keta2 || 50 < keta1)) {
                p = "_2_3";
            } else if (40 < keta2 || 40 < keta1) {
                p = "_2_2";
            } else if (0 < keta2) {
                p = "_2_1";
            }
            if (p != null) {
                svfVrsOut(field + i + p, addr2);
                printAddressLine(addr2, islast, linefield + i + p);
            }
        }

        private static Address getSameLineSchregAddress(final List<Address> printAddressList, final int i) {
            Address rtn = null;
            if (printAddressList.size() > i) {
                rtn = printAddressList.get(i);
            }
            return rtn;
        }

        /**
         * 保護者住所履歴を印刷します。
         * @param student
         */
        private void printGuardianAddress(final DB2UDB db2, final PersonalInfo pInfo) {
            svfVrsOut("GRD_HEADER", pInfo._addressGrdHeader);
            final String SAME_TEXT = "生徒の欄に同じ";
            final List<Address> printAddressList;
            final List<Address> guardPrintAddressList;
            if (param()._is133m) {
                printAddressList = getPrintAddressRecListM(pInfo._addressList, _grdaddressMax133m);
                guardPrintAddressList = getPrintAddressRecListM(pInfo._guardianAddressList, _grdaddressMax133m);
            } else {
                printAddressList = getIndexedList(pInfo._addressList, Address.getPrintAddressIndex(param(), "address", pInfo._addressList, _addressMax));
                guardPrintAddressList = getIndexedList(pInfo._guardianAddressList, Address.getPrintAddressIndex(param(), "guardianAddress", pInfo._guardianAddressList, _addressMax));
                if (param()._isPrintHosoku && pInfo._hosokuGuardAddressIndexList.size() > 0) {
                    svfVrsOut("HOSOKU_GUR_ADDR", "※" + HOSOKU_ARI);
                }
            }
            if (Address.isSameAddressList(printAddressList, guardPrintAddressList)) {
                // 住所が生徒と同一
                svfVrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            }
            for (int i = 1, num = guardPrintAddressList.size(); i <= num; i++) {
                final Address guardianAddress = guardPrintAddressList.get(i - 1);
                final boolean islast = i == num;
//                final String guardianAddress1 = defstr(guardianAddress.getAddr1());
//                final String guardianAddress2 = defstr(guardianAddress.getAddr2());
                final Address schregAddress = getSameLineSchregAddress(printAddressList, i - 1);

                boolean isSameAddress = false;
                if (islast) {
                    //isSameAddress = Address.isSameAddress(schregAddress, guardianAddress); // このコメントをはずすと、生徒住所A -> B、保護者住所Aの場合に「同一」
                    if (!isSameAddress) {
                        // 最新の生徒住所とチェック
                        isSameAddress = Address.isSameAddress(pInfo.getStudentAddressMax(), guardianAddress);
                    }
                } else {
                    isSameAddress = Address.isSameAddress(schregAddress, guardianAddress);
                }
                if (isSameAddress) {
                    // 内容が生徒と同一
                    svfVrsOut("GUARDIANADD" + i + "_1_1", SAME_TEXT);
                    printAddressLine(SAME_TEXT, islast, "GUARDIANADDLINE" + i + "_1_1");
                } else {
                    printZipCode("GUARDZIP", i, guardianAddress.getZipCode(), islast, "GUARDZIPLINE");

                    printAddr12(db2, "GUARDIANADD", "GUARDADDRESS", i, guardianAddress, islast, "GUARDIANADDLINE");
                }
            }
        }

        private void printZipCode(final String field, int i, final String zipCode, final boolean islast, final String linefield) {
            if (param()._printZipcd) {
                svfVrsOut(field + i, Util.prepend("〒", zipCode));
                printAddressLine(zipCode, islast, linefield + i);
            }
        }

        // 半角スペースか全角スペースで分割
        private static String[] splitBySpace(final String s) {
            String v1 = s;
            String v2 = null;
            int spaceIndex = v1 != null ? v1.indexOf(' ') : -1; // 全角スペースまたは半角スペースがあるか
            if (spaceIndex == -1) {
                spaceIndex = v1 != null ? v1.indexOf('　') : -1;
            }
            if (spaceIndex != -1 && spaceIndex != v1.length() - 1) {  // スペースがあれば
                v2 = v1.substring(spaceIndex + 1);
                v1 = v1.substring(0, spaceIndex);
            }
            return new String[] {v1, v2};
        }

        // 編入の各文言を右上編入学欄ではなく左下経歴欄に印字する
        private boolean isPrintHenyuEntTextToKeireki(final PersonalInfo pInfo) {
            if ("1".equals(param().property(Property.seitoSidoYorokuPrintHennyuEntReasonToKeireki))) {
                return true;
            }
            if (!pInfo.isHennyu()) {
                return false;
            }
            if (param()._is133m) {
                return param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.tokyoto);
            }
            if (param()._z010.in(Z010.miyagiken)) {
                return "1".equals(KnjDbUtils.getString(pInfo._lastAnotherSchoolHistDat, "KAIGAI")); // 海外
            }
            return false;
        }

        /**
         * 生徒情報を印刷します。
         * @param personalinfo
         */
        private void printPersonalInfo1(final DB2UDB db2, final PersonalInfo pInfo) {

            if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto) || param()._z010.in(Z010.miyagiken)) {
                    svfVrsOut("ATTENDNO_1", pInfo._schregno);
                } else if (param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("SCHREGNO", pInfo._schregno);
                }

                if (param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.mieken) || param()._z010.in(Z010.fukuiken)) {
                    // かな履歴表示
                    printName1(db2, pInfo._studentKana, null, _kana);

                    printName1(db2, pInfo._guardianOrGuarantorKana, null, _gKana);

                } else {
                    printKanaM("KANA", pInfo._studentKana._val, _kana);

                    printKanaM("GUARD_KANA", pInfo._guardianOrGuarantorKana._val, _gKana);
                }
            } else {
                printName1(db2, pInfo._studentKana, null, _kana);

                printName1(db2, pInfo._guardianOrGuarantorKana, null, _gKana);
            }
            if (param()._simei != null) { // 漢字名指定あり？
                printName1(db2, PersonalInfo.HistVal.of(pInfo.getPrintName1(), pInfo._studentNameHistFirst), param()._isPrintRirekiDate ? pInfo._studentNameNextDateOfHistLastDate : null, _name);

                printName1(db2, pInfo._guardianOrGuarantorName, param()._isPrintRirekiDate ? pInfo._guardianOrGuarantorNameNextDateOfHistLastDate : null, _gName);
            }
            if (param()._isPrintHosoku && pInfo._schregBaseHistList.size() > 0) {
                svfVrsOut("HOSOKU_NAME", "※" + HOSOKU_ARI);
            }
            if (param()._isPrintHosoku && pInfo._guardianHistOrGuarantorHistList.size() > 0) {
                svfVrsOut("HOSOKU_GUR_NAME", "※" + HOSOKU_ARI);
            }
            svfVrsOut("BIRTHDAY", Util.append(pInfo._birthdayStr, "生"));
            svfVrsOut("SEX", pInfo._sex);
            svfVrsOut("J_GRADUATEDDATE_Y", pInfo._finishDate);

            printFinschool(pInfo);

            if (param()._z010.in(Z010.sundaikoufu)) {
                svfVrsOut("GRD_TERM", defstr(pInfo._grdTerm) + "期生");
                svfVrsOut("SCHREGNO", "学籍番号 " + pInfo._schregno);
            }

            // 編入学の場合事由、学校名、学校住所を表示
            if (isPrintHenyuEntTextToKeireki(pInfo)) {
                final int keta;
                if (param()._is133m) {
                    keta = param()._z010.in(Z010.miyagiken) ? 40 : 50;
                    // 編入学の場合事由、学校名、学校住所を表示
                    final StringBuffer stb = new StringBuffer();
                    stb.append(Util.setKeta(pInfo._entReason, keta));
                    stb.append(Util.setKeta(pInfo._entSchool, keta));
                    stb.append(Util.setKeta(pInfo._entAddr, keta));
                    svfVrsOut("keireki11", stb.toString());
                } else {
                    // 海外
                    keta = 50;
                    final StringBuffer stb = new StringBuffer();
                    stb.append(Util.setKeta(pInfo._entReason, keta));
                    stb.append(Util.setKeta(pInfo._entSchool, keta));
                    stb.append(Util.setKeta(pInfo._entAddr, keta));
                    svfVrsOut("keireki1", stb.toString());
                }
            }
        }

        private void printCourseMajor(final PersonalInfo pInfo) {
            if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("COURSENAME", pInfo._courseName);
                    svfVrsOut("MAJORNAME", pInfo._majorName);
                } else {
                    if (param()._z010.in(Z010.miyagiken)) {
                        svfVrsOut("COURSENAME", Util.append(pInfo._courseName, "課程"));
                        final int keta = getMS932ByteLength("＝＝＝＝＝＝");
                        if (!"全日制".equals(pInfo._courseName)) {
                            svfUchikeshi("LINE4", keta, null); // 打ち消し線
                        }
                        if (!"通信制".equals(pInfo._courseName)) {
                            svfUchikeshi("LINE5", keta, null); // 打ち消し線
                        }
                    } else {
                        svfVrsOut("COURSENAME", pInfo._courseName);
                    }

                    final String[] split = splitBySpace(pInfo._majorName);
                    svfVrsOut("MAJORNAME", split[0]);
                    svfVrsOut("COURSECODENAME", split[1]);
                }
            } else {
                if (param()._z010.in(Z010.miyagiken)) {
                    final int keta = getMS932ByteLength("＝＝＝＝＝＝＝");
                    if (StringUtils.isBlank(pInfo._checkCourseName)) {
                        svfUchikeshi("LINE4", keta, null);
                        svfUchikeshi("LINE5", keta, null);
                    } else if (isZennnichisei(pInfo)) {
                        svfUchikeshi("LINE5", keta, null); // 定時制を打ち消し
                    } else if (isTeijisei(pInfo)) {
                        svfUchikeshi("LINE4", keta, null); // 全日制を打ち消し
                    }
                } else if (param()._z010.in(Z010.fukuiken)) {
                    svfVrsOut("TITLE_COURSE", pInfo._checkCourseName);
                } else {
                    final int keta = getMS932ByteLength(param()._z010.in(Z010.mieken) || param()._z010.in(Z010.naraken) ? "＝＝＝＝＝＝" : "＝＝＝＝＝");
                    if (!isZennnichisei(pInfo)) {
                        svfUchikeshi("LINE4", keta, null);
                    }
                    if (!isTeijisei(pInfo)) {
                        svfUchikeshi("LINE5", keta, null);
                    }
                }
                if (param()._z010.in(Z010.miyagiken)) {
                    final String[] split = splitBySpace(pInfo._majorName);
                    svfVrsOut("MAJORNAME", defstr(pInfo._courseName) + "課程・" + defstr(split[0]));
                    svfVrsOut("COURSECODENAME", split[1]);
                } else if (param()._z010.in(Z010.mieken)) {

                    svfVrsOut("COURSENAME", defstr(pInfo._courseName) + "課程・");
                    String majorNameString = defstr(pInfo._majorName);
                    int nakaguroIdx = majorNameString.indexOf("・");
                    if (nakaguroIdx == -1) {
                        nakaguroIdx = majorNameString.indexOf("・");
                    }
                    if (0 <= nakaguroIdx) {
                        majorNameString = majorNameString.substring(0, nakaguroIdx) + "(" + majorNameString.substring(nakaguroIdx + 1) + ")";
                    }
                    svfVrsOut("MAJORNAME", majorNameString);
                } else if (param()._z010.in(Z010.tosa)) {
                    svfVrsOut("MAJORNAME", Util.append(pInfo._courseName, "課程・") + defstr(pInfo._majorName));

                    final int leftx = 884;
                    final int rightx = 3780;
                    final Map<String, String> fieldStatusMap = _form._formInfo.getFieldStatusMap("MAJORNAME", true);
                    final String xstr = fieldStatusMap.get("X");
                    final String sizestr = fieldStatusMap.get("Size");
                    final String ketastr = fieldStatusMap.get("Keta");
                    final int x = NumberUtils.isDigits(xstr) ? Integer.parseInt(xstr) : 0;
                    final int keta = NumberUtils.isDigits(ketastr) ? Integer.parseInt(ketastr) : 0;
                    final float size = NumberUtils.isNumber(sizestr) ? Float.parseFloat(sizestr) : 0;
                    final double width = SvfFieldAreaInfo.KNJSvfFieldModify.fieldWidth("MAJORNAME", size, 0, keta);
                    final int movex = x + (int) (leftx + (rightx - leftx) / 2 - (x + width / 2));
                    svfVrAttribute("MAJORNAME", ATTR_CENTERING + ",X=" + String.valueOf(movex)); // 移動、センタリング

                } else {
                    svfVrsOut("COURSENAME", pInfo._courseName);

                    final boolean hasFieldCOURSECODENAME = hasField("COURSECODENAME");
                    if ("1".equals(param().property(Property.seitoSidoYorokuPrintCoursecodename))) {
                        if (hasFieldCOURSECODENAME) {
                            final String[] split = splitBySpace(pInfo._majorName);
                            svfVrsOut("MAJORNAME", split[0]);
                            svfVrsOut("COURSECODENAME", defstr(split[1]) + Util.prepend("　", pInfo._coursecodeName));
                        } else {
                            svfVrsOut("MAJORNAME", defstr(pInfo._majorName) + Util.prepend("　", pInfo._coursecodeName));
                        }
                    } else {
                        if (hasFieldCOURSECODENAME) {
                            final String[] split = splitBySpace(pInfo._majorName);
                            svfVrsOut("MAJORNAME", split[0]);
                            svfVrsOut("COURSECODENAME", split[1]);
                        } else {
                            svfVrsOut("MAJORNAME", pInfo._majorName);
                        }
                    }
                }
            }
        }

        private void printFinschool(final PersonalInfo pInfo) {
            final boolean isPrintInstallationDiv = !(param()._z010.in(Z010.KINDAI) || param()._z010.in(Z010.kumamoto) || param()._isDefinecodeSchoolMarkHiro || param()._z010.in(Z010.rakunan) || "1".equals(param().property(Property.notPrintFinschooldistcdName)));
            if (isPrintInstallationDiv) {
                if (param()._is133m) {
                    svfVrsOut("INSTALLATION_DIV", pInfo._installationDiv);
                } else {
                    if (param()._z010.in(Z010.HOUSEI)) {
                        final String ritu = pInfo._installationDiv;
                        if (null != ritu) {
                            svfVrsOut("INSTALLATION_DIV",  ritu + "立");
                        }
                    } else {
                        svfVrsOut("INSTALLATION_DIV", pInfo._installationDiv);
                    }
                }
            }

            // 入学前学歴の学校名編集
            final String kotei;
            final String juniorSchoolName = KnjDbUtils.getString(pInfo._regRow, "J_NAME");
            final boolean juniorSchoolNameContainsSchoolKindName = param()._z010.in(Z010.tokiwa, Z010.miyagiken, Z010.nishiyama, Z010.kaijyo, Z010.mieken) || "1".equals(param().property(Property.notPrintFinschooltypeName));
            final String SOTSUGYO = "1".equals(KnjDbUtils.getString(pInfo._regRow, "FINSCHOOL_NOT_PRINT_SOTSUGYO")) ? "" : defstr(pInfo._jSchoolKindGrdDivName, "卒業");
            if (param()._z010.in(Z010.KINDAI)) {
                kotei = "中学校" + SOTSUGYO;
            } else if (StringUtils.isBlank(juniorSchoolName) && "1".equals(param().property(Property.seitoSidoYorokuNotPrintFinschoolGrdDivDefaultName))) {
                kotei = "";
            } else if (param()._is133m && param()._z010.in(Z010.miyagiken)) {
                kotei = StringUtils.isBlank(juniorSchoolName) ? "" : SOTSUGYO;
            } else if (juniorSchoolNameContainsSchoolKindName) {
                kotei = SOTSUGYO;
            } else {
                final String finschoolTypeName = KnjDbUtils.getString(pInfo._regRow, "FINSCHOOL_TYPE_NAME");
                kotei = defstr(finschoolTypeName) + SOTSUGYO;
            }

            final String finschool1 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL1_HIRO" : "FINSCHOOL1";
            final String finschool2 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL2_HIRO" : "FINSCHOOL2";
            final String finschool3 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL3_HIRO" : "FINSCHOOL3";
            if (param()._z010.in(Z010.rakunan)) {
                svfVrAttribute(finschool1, ATTR_LEFT);
                svfVrAttribute(finschool2, ATTR_LEFT);
                svfVrAttribute(finschool3, ATTR_LEFT);
            }
            if (StringUtils.isBlank(juniorSchoolName)) {
                svfVrsOut(finschool1, kotei);
            } else {
                final String schoolName;
                if (param()._z010.in(Z010.kumamoto)) {
                    schoolName = defstr(pInfo._installationDiv) + defstr(juniorSchoolName);
                } else {
                    final int i = juniorSchoolName.indexOf('　');  // 全角スペース
                    if (-1 < i && 5 >= i) {
                        final String ritu = juniorSchoolName.substring(0, i);
                        if (null != ritu) {
                            if (param()._z010.in(Z010.rakunan)) {
                                svfVrsOut("INSTALLATION_DIV",  ritu);
                            } else {
                                svfVrsOut("INSTALLATION_DIV",  ritu + "立");
                            }
                        }
                        schoolName = juniorSchoolName.substring(i + 1);
                    } else {
                        schoolName = juniorSchoolName;
                    }
                }
                final int totallen = getMS932ByteLength(schoolName) + getMS932ByteLength(kotei);
                if (totallen <= 40) {
                    svfVrsOut(finschool1, schoolName + kotei);
                } else if(totallen <= 50) {
                    svfVrsOut(finschool2, schoolName + kotei);
                } else {
                    svfVrsOut(finschool2, schoolName);
                    svfVrsOut(finschool3, kotei);
                }
            }
        }

        /**
         * 住所の取り消し線印刷
         * @param svf
         * @param i
         */
        private void printAddressLine(final String val, final boolean islast, final String field) {
            if (null == val || islast) {
                return;
            }
            svfUchikeshi(field, getMS932ByteLength(val), null);
        }

        /**
         * 学籍履歴を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGakuseki1(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final String i, final Gakuseki gakuseki) {
            final String printSeiribango;
            final Param param = param();
            if (param._is133m) {
                if (param._z010.in(Z010.tokyoto)) {
                    // 学年
                    if (param._schoolDiv.isGakunenSei(gakuseki._year, null, student)) {
                        svfVrsOut("GRADE2_" + i, gakuseki._gdat._gakunenSimple);
                    } else {
                        svfVrsOut("GRADE1_" + i, gakuseki._arNendoM[0]);
                        svfVrsOut("GRADE2_" + i, gakuseki._arNendoM[1]);
                        svfVrsOut("GRADE3_" + i, gakuseki._arNendoM[2]);
                    }
                } else if (param._z010.in(Z010.kyoto)) {
                    svfVrsOut("GRADE1_" + i, gakuseki._arNendoM[0]);
                    svfVrsOut("GRADE2_" + i, gakuseki._arNendoM[1]);
                    svfVrsOut("GRADE3_" + i, gakuseki._arNendoM[2]);
                } else if (param._z010.in(Z010.sagaken)) {
                    svfVrsOut("HGRADE1_" + i, gakuseki._nendo);
                } else {
                    // 学年
                    if (param._schoolDiv.isGakunenSei(gakuseki._year, null, student)) {
                        svfVrsOut("GRADE" + i, gakuseki._gdat._gakunenSimple);
                    } else {
                        svfVrsOut("GRADE" + i, gakuseki._arNendoM[0] + gakuseki._arNendoM[1] + gakuseki._arNendoM[2]);
                    }
                }

                printSeiribango = student._schregno;

            } else {
                svfVrsOut("SCHOOLNAME1", student.certifSchool(param)._schoolName1);

                // 学年
                if (gakuseki._isKoumokuGakunen) {
                    if (param._z010.in(Z010.tokiwa)) {
                        svfVrsOut("GRADE1_" + i, gakuseki._gdat._gradeName2);
                    } else {
                        svfVrsOut("GRADE2_" + i, gakuseki._gdat._gakunenSimple);
                    }
                } else {
                    if (param._z010.in(Z010.mieken) || param._z010.in(Z010.fukuiken) || param._z010.in(Z010.naraken)) {
                        svfVrsOut("GRADE2_" + i, gakuseki._nendo);
                    } else {
                        final String[] nendoArray = gakuseki.nendoArray(param);
                        svfVrsOut("GRADE1_" + i, nendoArray[0]);
                        svfVrsOut("GRADE2_" + i, nendoArray[1]);
                        svfVrsOut("GRADE3_" + i, nendoArray[2]);
                    }
                }

                printSeiribango = gakuseki._attendno;

                if (param._z010.in(Z010.tokiwa)) {
                    svfVrsOut("GRADE_" + i, gakuseki._gdat._gradeName2);
                } else if (param._z010.in(Z010.bunkyo)) {
                    svfVrsOut("GRADE_" + i, gakuseki._gdat._gakunenSimple);
                } else {
                    if (gakuseki._isKoumokuGakunen || "1".equals(param.property(Property.seitoSidoYorokuPrintForm1StaffGrade))) {
                        svfVrsOut("GRADE_" + i, gakuseki._gdat._gakunenSimple);
                    } else if (param._z010.in(Z010.miyagiken) && param._schoolDiv.isTanniSei(gakuseki._year, null, student)) {
                        svfVrsOut("GRADE_" + i, toDigit(gakuseki._annual, gakuseki._annual));
                    }
                }
            }

            // ホームルーム
            svfVrsOutForData(Arrays.asList("HR_CLASS1_" + i, "HR_CLASS2_" + i), gakuseki._hdat._hrname);

            // 整理番号
            svfVrsOut("ATTENDNO_" + i, printSeiribango);

            //
            svfVrsOut("YEAR_" + i, setNendoFormatInsertBlank(db2, gakuseki._nendo, param, param._formatDateDefaultYear));

            final Tuple<Staff, Tuple<Staff, Staff>> principal012 = getPrincipal012(gakuseki, student, pInfo);
            final Staff _principal = principal012._first;
            final Staff _principal1 = principal012._second._first;
            final Staff _principal2 = principal012._second._second;

            final List<Staff> studentStaff1HistList = param.getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV1, gakuseki._year);
            final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
            final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);
            final Staff staff2Last = Util.last(param.getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV2, gakuseki._year), Staff.Null);
            Staff staff3Last = Staff.Null;
            if (param._z010.in(Z010.osakatoin)) {
                staff3Last = Util.last(param.getStudentStaffHistList(student, pInfo, StaffInfo.TR_DIV3, gakuseki._year), Staff.Null);
            }

            if (param._is133m) {

                // 校長氏名
                printStaffNameM("1", i, true, _principal, _principal1, Staff.Null, _principal2);

                // 担任氏名
                printStaffNameM("2", i, false, Staff.Null, staff1Last, staff2Last, staff1First);

            } else {
                // 校長氏名
                printStaffName("1", i, true, _principal, _principal1, _principal2);

                // 担任氏名
                if (param._z010.in(Z010.osakatoin, Z010.rakunan) || param._z010.in(Z010.mieken) && Staff.getUniqueStaffList(staff1First, staff1Last).size() == 1) {
                    printStaffName2("2", i, Arrays.asList(Staff.getUniqueStaffList(staff1First, staff1Last), Staff.getUniqueStaffList(staff2Last), Staff.getUniqueStaffList(staff3Last)));
                } else {
                    printStaffName("2", i, false, Staff.Null, staff1Last, staff1First);
                }

                if (param._isPrintHosoku && (!pInfo._hosokuYearPrincipalMap.isEmpty() || !pInfo._hosokuYearStaffMap.isEmpty())) {
                    List<String> items = new ArrayList<String>();
                    if (!pInfo._hosokuYearPrincipalMap.isEmpty()) {
                        items.add("校長氏名");
                    } else if (!pInfo._hosokuYearStaffMap.isEmpty()) {
                        items.add("担任者氏名");
                    }
                    svfVrsOut("HOSOKU_STAFF", "※" + Util.mkString(items, "、") + "の" + HOSOKU_ARI);
                }
                if (!pInfo.isTargetYearLast(gakuseki._year, student, param, true)) {
                    return;
                }
            }
            //印影
            if (param._isPrintInei) {
                if (param._isOutputDebugStaff) {
                    log.info("印影：" + param._inei + ", 改竄：" + gakuseki._kaizanFlg + ", 署名（校長）：" + gakuseki._principalSeq + ", 署名（担任）：" + gakuseki._staffSeq);
                }
                //改竄されていないか？
                if (null == gakuseki._kaizanFlg) {
                    final boolean isColor = true;
                    //署名（校長）しているか？
                    if (null != gakuseki._principalSeq || "4".equals(param._inei)) {
                        final Staff stampPrincipalStaff;
                        if (param._is133m) {
                            if (null == _principal1._staffMst._staffcd) {
                                stampPrincipalStaff = _principal;
                            } else if (StaffMst.Null == _principal2._staffMst || _principal2._staffMst == _principal1._staffMst) {
                                stampPrincipalStaff = _principal1;
                            } else {
                                stampPrincipalStaff = _principal2;
                            }
                        } else {
                            if (null == _principal1._staffMst._staffcd) {
                                stampPrincipalStaff = _principal;
                            } else {
                                stampPrincipalStaff = _principal1;
                            }
                        }
                        final String principalStampPath = param.getImageFilePath(stampPrincipalStaff._stampNo);
                        if (null == principalStampPath) {
                            log.warn("署名印影なし:" + stampPrincipalStaff._staffMst._staffcd + " / stampNo = " + stampPrincipalStaff._stampNo);
                        } else {
                            svfVrImageOut(isColor ? "STAFFBTM_1_" + i + "C" : "STAFFBTM_1_" + i, principalStampPath); // 担任印
                        }
                    }
                    //署名（担任）しているか？
                    if (null != gakuseki._staffSeq || "3".equals(param._inei) || "4".equals(param._inei)) {
                        final Staff staff = staff1Last;
                        final String hrStaffStamp = param.getImageFilePath(staff._stampNo);
                        if (param._z010.in(Z010.osakatoin) && staff2Last != Staff.Null) {
                            if (param._isOutputDebugStaff) {
                                log.info("印影表示:" + staff._staffMst._staffcd + " " + staff._staffMst._name + " / stampNo = " + staff._stampNo);
                            }

                            svfVrImageOut("STAFFBTM_2_" + i + "_1C", hrStaffStamp); // 担任印
                            svfVrImageOut("STAFFBTM_2_" + i + "_2C", param.getImageFilePath(staff2Last._stampNo)); // 担任印
                            svfVrImageOut("STAFFBTM_2_" + i + "_3C", param.getImageFilePath(staff3Last._stampNo)); // 担任印
                        } else if (param._z010.in(Z010.mieken) && staff2Last != Staff.Null) {
                            if (param._isOutputDebugStaff) {
                                log.info("mieken 印影表示:" + staff._staffMst._staffcd + " " + staff._staffMst._name + " / stampNo = " + staff._stampNo);
                            }

                            svfVrImageOut("STAFFBTM_2_" + i + "_1C", hrStaffStamp); // 担任印
                            svfVrImageOut("STAFFBTM_2_" + i + "_2C", param.getImageFilePath(staff2Last._stampNo)); // 担任印

                        } else if (null == hrStaffStamp) {
                            log.warn("署名印影なし:" + staff._staffMst._staffcd + " / stampNo = " + staff._stampNo);
                        } else {
                            if (param._isOutputDebugStaff) {
                                log.info("印影表示:" + staff._staffMst._staffcd + " " + staff._staffMst._name + " / stampNo = " + staff._stampNo);
                            }
                            svfVrImageOut(isColor ? "STAFFBTM_2_" + i + "C" : "STAFFBTM_2_" + i, hrStaffStamp); // 担任印
                        }
                    }
                }
            }
        }

        private Tuple<Staff, Tuple<Staff, Staff>> getPrincipal012(final Gakuseki gakuseki, final Student student, final PersonalInfo pInfo) {
            Staff _principal = Staff.Null;
            Staff _principal1 = Staff.Null;
            Staff _principal2 = Staff.Null;
            final Param param = param();
            if (Gakuseki.GAKUSEKI_DATA_FLG1.equals(gakuseki._dataflg)) {
                final Map<String, String> certifSchoolMap = Util.getMappedHashMap(student._yearCertifSchoolMap, gakuseki._year);

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
                    if (null != pInfo._grdDate) {
                        // 生徒の卒業日付以降の校長は対象外
                        for (int j = 0; j < principalList.size(); j++) {
                            final Map principal = principalList.get(j);
                            final String fromDate = KnjDbUtils.getString(principal, "FROM_DATE");
                            if (fromDate.compareTo(pInfo._grdDate) > 0) {
                                break;
                            }
                            last = principal;
                        }
                    }
                    if (null != KnjDbUtils.getString(last, "FROM_DATE") && !KnjDbUtils.getString(last, "FROM_DATE").equals(KnjDbUtils.getString(listLast, "FROM_DATE"))) {
                        log.info(" principal last date = " + KnjDbUtils.getString(last, "FROM_DATE") + " instead of " + KnjDbUtils.getString(listLast, "FROM_DATE") + " (student grddate = " + pInfo._grdDate + ")");
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
                }

                _principal = new Staff(gakuseki._year, new StaffMst(null, principalName, null, null, null), null, null, param._staffInfo.getStampNo(KnjDbUtils.getString(certifSchoolMap, "PRINCIPALSTAFFCD"), gakuseki._year));
                _principal1 = new Staff(gakuseki._year, StaffMst.get(param._staffInfo._staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, param._staffInfo.getStampNo(principalStaffcd1, gakuseki._year));
                _principal2 = new Staff(gakuseki._year, StaffMst.get(param._staffInfo._staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);
            }
            return Tuple.of(_principal, Tuple.of(_principal1, _principal2));
        }

        private void printStaffName(final String j, final String i, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            final int keta = 26;
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString(param(), keta);
                svfVrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList<String>();
                line.addAll(staff1._staffMst.getNameLine(staff1._year, param(), keta));
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    final String name = staff1.getNameString(param(), keta);
                    svfVrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
                }
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List<String> line = new ArrayList<String>();
                line.addAll(staff2.getNameBetweenLine(param(), keta));
                line.addAll(staff1.getNameBetweenLine(param(), keta));
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        svfVrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), line.get(k));
                    }
                }
            }
        }

        // 担任2人印字
        private void printStaffName2(final String j, final String i, final List<List<Staff>> staffListList) {
            final int keta = 26;
            final List<String> lines = new ArrayList();
            for (final List<Staff> staff1List : staffListList) {
                if (staff1List.size() == 1) {
                    lines.add(staff1List.get(0).getNameString(param(), keta));
                } else if (staff1List.size() > 1) {
                    for (final Staff staff : staff1List) {
                        lines.addAll(staff._staffMst.getNameLine(staff._year, param(), keta));
                    }
                }
            }
            if (param()._isOutputDebugStaff) {
                if  (lines.size() > 1) {
                    log.info(" staffname " + j + " [" + i + "] lines = " + lines);
                }
            }
            if (lines.size() == 1) {
                // 1人表示。
                svfVrsOutForData(Arrays.asList("STAFFNAME_" + j + "_" + i,  "STAFFNAME_" + j + "_" + i + "_1"), lines.get(0));
            } else if (lines.size() == 2) {
                // 2人表示。最小2行（中央）。
                svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", lines.get(0));
                svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", lines.get(1));
            } else if (lines.size() >= 3) {
                // 上から順に表示。
                for (int k = 0; k < 4 && k < lines.size(); k++) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), lines.get(k));
                }
            }
        }

        private static List<Address> getPrintAddressRecListM(final List<Address> addressRecList, final int max) {
            final LinkedList<Address> rtn = new LinkedList<Address>();
            if (addressRecList.isEmpty()) {
                return rtn;
            }
            if (1 == max) {
                rtn.addAll(Util.reverse(Util.take(max - rtn.size(), Util.reverse(addressRecList))));
            } else {
                rtn.add(addressRecList.get(0));
                rtn.addAll(Util.reverse(Util.take(max - rtn.size(), Util.reverse(Util.drop(1, addressRecList)))));
            }
            return rtn;
        }

        /**
         * かなを表示する
         */
        private void printKanaM(final String fieldKana, final String schKana, final KNJSvfFieldInfo kana) {

            final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldKana, kana._x2 - kana._x1, kana._height, kana._ystart, kana._minnum, kana._maxnum);
            final double charSize = modify.getCharSize(schKana);
            svfVrAttribute(fieldKana, "Size=" + charSize);
            svfVrAttribute(fieldKana, "Y=" + (int) modify.getYjiku(0, charSize));
            svfVrsOut(fieldKana, schKana);
        }

        private void printStaffNameM(final String j, final String i, final boolean isPrincipal, final Staff staff0, final Staff staff1Last, final Staff staff2Last, final Staff staff1First) {
            final int keta = 26;
            final boolean isCheckStaff0 = isPrincipal;
            if (isCheckStaff0 && null == staff1Last._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString(param(), keta);
                svfVrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
            } else if ((StaffMst.Null == staff1First._staffMst || staff1First._staffMst == staff1Last._staffMst) && (!param()._z010.in(Z010.tokyoto) || param()._z010.in(Z010.tokyoto) && StaffMst.Null == staff2Last._staffMst || staff2Last._staffMst == staff1Last._staffMst)) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList<String>();
                line.addAll(staff1Last._staffMst.getNameLine(staff1Last._year, param(), keta));
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    final String name = staff1Last.getNameString(param(), keta);
                    svfVrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
                }
            } else {
                final List<String> line = new ArrayList<String>();
                if (param()._z010.in(Z010.tokyoto) && StaffMst.Null != staff2Last._staffMst && staff2Last._staffMst != staff1Last._staffMst) {
                    // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                    line.addAll(staff1Last._staffMst.getNameLine(staff1Last._year, param(), keta));
                    line.addAll(staff2Last._staffMst.getNameLine(staff2Last._year, param(), keta));
                } else {
                    // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                    line.addAll(staff1First.getNameBetweenLine(param(), keta));
                    line.addAll(staff1Last.getNameBetweenLine(param(), keta));
                }
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        svfVrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), line.get(k));
                    }
                }
            }
        }
    }

    /**
     * 修得単位の記録。
     */
    private static class KNJA130_2 extends KNJA130_0 implements Page {

        private servletpack.KNJZ.detail.KNJSvfFieldModify svfobj = new servletpack.KNJZ.detail.KNJSvfFieldModify(); // フォームのフィールド属性変更
        /** 1列目までの行数 */
        private int MAX_LINE1;
        /** 2列目までの行数 */
        private int MAX_LINE2;
        /** 1ページの最大行数 (3列目までの行数) */
        private int MAX_LINE_PER_PAGE ;

        private String SLASH = "/";

        private KNJSvfFieldInfo _name = new KNJSvfFieldInfo();

        private int _pageMaxGrade;

        private final YOSHIKI _yoshiki = YOSHIKI._1_URA;

        private boolean _formHasHrHeader;
        private boolean _isPrintBiko;

        private final Map<String, Map<String, Integer>> _formnameMaxlinesMap = new HashMap<String, Map<String, Integer>>();

        private boolean _isLastsRecord;
        private boolean _isKenja;

        KNJA130_2(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm2(final Student student, final PersonalInfo pInfo) {
            _formHasHrHeader = true;
            _isPrintBiko = false;
            // TODO: 各固定指定修正する
            MAX_LINE1 = -1;
            MAX_LINE2 = -1;
            MAX_LINE_PER_PAGE = -1;
            final String form;
            final boolean is3 = is3nenYou(pInfo);
            _isLastsRecord = true;
            _isKenja = false;
            if (param()._z010.in(Z010.kyoto)) {
                if (param()._is133m) {
                    form = "KNJA133M_2KYOTO.frm";
                    _pageMaxGrade = -1;
                } else {
                    form = "KNJA130C_2KYOTO.frm";
                    _pageMaxGrade = 8;
                }
                _formHasHrHeader = false;
            } else if (param()._z010.in(Z010.mieken)) {
                if (param()._is133m) {
                    form = "KNJA133M_2MIE.frm";
                    _pageMaxGrade = -1;
                } else {
                    form = "KNJA130C_2MIE.frm";
                    _pageMaxGrade = 4;
                    _isPrintEduDiv2CharsPerLine = true;
                }
                _formHasHrHeader = false;
            } else if (param()._z010.in(Z010.tokiwa)) {
                if (isNewForm(param(), pInfo)) {
                    form =  is3 ? "KNJA130C_12_2TOKIWA.frm" : null;
                    MAX_LINE1 = 37;
                    MAX_LINE2 = 67;
                    MAX_LINE_PER_PAGE = MAX_LINE2;
                } else {
                    form =  is3 ? "KNJA130C_12TOKIWA.frm" : null;
                    MAX_LINE1 = 35;
                    MAX_LINE2 = 64;
                    MAX_LINE_PER_PAGE = MAX_LINE2;
                }
                _pageMaxGrade = 3;
                _isLastsRecord = false;
            } else if (param()._z010.in(Z010.miyagiken)) {
                if (param()._is133m) {
                    form = "KNJA133M_2MIYA.frm";
                    _pageMaxGrade = -1;
                } else {
                    //form = (is3) ? null : "KNJA130C_2MIYA.frm";
                    form = "KNJA130C_2MIYA.frm";
                    _pageMaxGrade = 4;
                    _isPrintEduDiv2CharsPerLine = true;
                    _formHasHrHeader = true;
                }
            } else if (param()._z010.in(Z010.meiji)) {
                MAX_LINE1 = 35;
                MAX_LINE2 = 70;
                MAX_LINE_PER_PAGE = 101;
                form = is3 ? "KNJA130C_12.frm" : "KNJA130C_2MEIJI.frm";
                if (is3) {
                    _pageMaxGrade = 3;
                } else {
                    _pageMaxGrade = 4;
                }
                _isLastsRecord = false;
            } else if (param()._z010.in(Z010.fukuiken)) {
                form = "KNJA130C_2FUKUI.frm";
                _pageMaxGrade = 3;
                _formHasHrHeader = false;
            } else if (param()._z010.in(Z010.naraken)) {
                if (param()._is133m) {
                    form = "KNJA133M_2NARA.frm";
                    _pageMaxGrade = 8;
                } else {
                    form = "KNJA130C_2NARA.frm";
                    _pageMaxGrade = 4;
                }
                _formHasHrHeader = false;
            } else if (param()._z010.in(Z010.tosa)) {
                form = "KNJA130C_12TOSA.frm";
                MAX_LINE1 = 32;
                MAX_LINE2 = 64;
                MAX_LINE_PER_PAGE = 93;
                _pageMaxGrade = 3;
                _isLastsRecord = false;
            } else if (param()._z010.in(Z010.chiyodaKudan)) {
                form = "KNJA130_2KUDAN.frm";
                _pageMaxGrade = 4;
                _isLastsRecord = false;
            } else if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    form = "KNJA133M_2TOKYO.frm";
                } else if (param()._z010.in(Z010.sagaken)) {
                    form = "KNJA133M_2SAGA.frm";
                } else if (KNJA130_0.isNewForm(param(), pInfo)) {
                    form = "KNJA133M_2KUMA.frm";
                } else {
                    form = "KNJA133M_2.frm";
                }
                _pageMaxGrade = -1;
                _isLastsRecord = false;
            } else if (param()._z010.in(Z010.meikei) && "1".equals(param()._ibCourse)) {
                MAX_LINE1 = 50;
                MAX_LINE2 = MAX_LINE1;
                MAX_LINE_PER_PAGE = MAX_LINE1;
                form = "KNJA130C_2MEIKEI_IB.frm";
                _pageMaxGrade = -1;
                _formHasHrHeader = false;
                _isPrintBiko = true;
                _isLastsRecord = false;
            } else {
                form = (is3) ? "KNJA130C_12.frm" : "KNJA130C_2.frm";
                if (is3) {
                    _pageMaxGrade = 3;
                } else {
                    _pageMaxGrade = 4;
                }
                _formHasHrHeader = true;
                _isKenja = true;
            }
            return form;
        }

        private void getRecordCount(final String form) {
            if (!_formnameMaxlinesMap.containsKey(form)) {
                final Map<String, Integer> maxlinesMap = Util.getMappedHashMap(_formnameMaxlinesMap, form);
                try {

                    final String formFilePath = _form._svf.getPath(form);
                    if (null != formFilePath) {
                        final File formFile = new File(formFilePath);
                        SvfForm svfForm = new SvfForm(formFile);
                        if (svfForm.readFile()) {
                            SvfForm.Record record = svfForm.getRecord("RECORD");
                            int count1 = -1, count2 = -1, count3 = -1;
                            if (null != record) {
                                SvfForm.SubForm subform1 = svfForm.getSubForm("SUBFORM1");
                                if (null != subform1) {
                                    count1 = subform1.getHeight() / record.getHeight();
                                }
                                SvfForm.SubForm subform2 = svfForm.getSubForm("SUBFORM2");
                                if (null != subform2) {
                                    count2 = subform2.getHeight() / record.getHeight();
                                }
                                SvfForm.SubForm subform3 = svfForm.getSubForm("SUBFORM3");
                                if (null != subform3) {
                                    count3 = subform3.getHeight() / record.getHeight();
                                }
                            }
                            if (count1 > 0 && count2 >= 0 && count3 >= 0) {
                                log.info("form = " + form + ", count1 = " + count1 + ", count2 = " + count2 + ", count3 = " + count3);
                                maxlinesMap.put("MAX_LINE1", count1);
                                maxlinesMap.put("MAX_LINE2", count1 + count2);
                                maxlinesMap.put("MAX_LINE_PER_PAGE", count1 + count2 + count3);
                            }
                        }
                    }
                } catch (Throwable t) {
                    if (param()._isOutputDebug) {
                        log.warn("exception!", t);
                    }
                    maxlinesMap.put("MAX_LINE1", 35);
                    maxlinesMap.put("MAX_LINE2", 70);
                    maxlinesMap.put("MAX_LINE_PER_PAGE", 101);
                }
            }
            final Map<String, Integer> maxlinesMap = Util.getMappedHashMap(_formnameMaxlinesMap, form);
            MAX_LINE1 = maxlinesMap.get("MAX_LINE1");
            MAX_LINE2 = maxlinesMap.get("MAX_LINE2");
            MAX_LINE_PER_PAGE = maxlinesMap.get("MAX_LINE_PER_PAGE");
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            setDetail2(db2, student, pInfo, csvLines);
        }
        public void setForm(final Student student, final PersonalInfo pInfo) {
            if (csv.equals(param()._cmd)) {
                _gradeLineMax = 99;
                _formHasHrHeader = true;
                MAX_LINE1 = 9999;
                _isKenja = true;
                _isLastsRecord = true;
                return;
            }
            final String frm = getForm2(student, pInfo);
            svfVrSetForm(frm, 4);
            final String mform = modifyForm2(frm, student, pInfo);
            if (!mform.equals(frm)) {
                svfVrSetForm(mform, 4);
            }
            if (MAX_LINE1 < 0 || MAX_LINE2 < 0 || MAX_LINE_PER_PAGE < 0) {
                getRecordCount(mform);
            }
            if (_form._formInfo.hasField("NAME1")) {
                final int minnum = "KNJA133M_2MIYA.frm".equals(frm) ? 16 : 24;
                _name = _form._formInfo.getFieldInfo("NAME1", "NAME2", "NAME3", charSize11, minnum);
            }
        }
        private void setDetail2(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            if (null != csvLines) {
                final Map<Integer, PrintGakuseki> pageGakusekiListMap = getPageGakusekiListMap2(param(), pInfo, 999);
                for (final Integer page : pageGakusekiListMap.keySet()) {
                    final PrintGakuseki printGakuseki = pageGakusekiListMap.get(page);
                    printPage2(student, pInfo, page, printGakuseki, csvLines);
                }
            } else {
                setForm(student, pInfo);
                final Map<Integer, PrintGakuseki> pageGakusekiListMap = getPageGakusekiListMap2(param(), pInfo, _pageMaxGrade);
                for (final Integer page : pageGakusekiListMap.keySet()) {
                    final PrintGakuseki printGakuseki = pageGakusekiListMap.get(page);
                    printPage2(student, pInfo, page, printGakuseki, null);
                }
            }
        }

        final String FLG_TEIHACHI_RISHUZUMI_REMARK = "FLG_TEIHACHI_RISHUZUMI_REMARK";
        private String modifyForm2(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if (param()._z010.in(Z010.teihachi)) {
                flgMap.put(FLG_TEIHACHI_RISHUZUMI_REMARK, "1");
            }
            return modifyForm0(form, pInfo, null, flgMap);
        }
        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_TEIHACHI_RISHUZUMI_REMARK)) {
                final SvfForm.Record record = svfForm.getRecord("RECORD");
                final int addY = - record.getHeight() * 4;
                int bottomY = -1;
                for (final String subformname : Arrays.asList("SUBFORM1", "SUBFORM2", "SUBFORM3")) {
                    final SvfForm.SubForm subForm = svfForm.getSubForm(subformname);
                    final SvfForm.SubForm newSubForm = subForm.setHeight(subForm.getHeight() + addY);

                    for (final SvfForm.Record recordInFormCand : svfForm.getElementList(SvfForm.Record.class)) {
                        if (subForm == recordInFormCand.getSubForm()) {
                            recordInFormCand.setSubForm(newSubForm);
                        }
                    }
                    svfForm.removeSubForm(subForm);
                    svfForm.addSubForm(newSubForm);
                    bottomY = subForm._point2._y;
                }
                if (bottomY >= 0) {
                    for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                        if (bottomY < field._position._y) {
                            svfForm.removeField(field);
                            svfForm.addField(field.addY(addY));
                        }
                    }
                }
                final int boxX1 = 309, boxY1 = 4116, boxX2 = 3347, boxY2 = 4400;
                svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THIN, new SvfForm.Point(boxX1, boxY1), new SvfForm.Point(boxX2, boxY2)));
                final SvfForm.Field.RepeatConfig rcBiko = new SvfForm.Field.RepeatConfig("900", 5, 1, -1, 3).setRepeatPitchPoint(3.3);
                svfForm.addRepeat(new Repeat(rcBiko._repeatNo, boxX1, boxY1 + 10, boxX2, boxY2, 0, rcBiko._repeatCount, rcBiko._repeatPitch, 0, "1"));
                svfForm.addField(new SvfForm.Field(null, "RISHUZUMI", SvfForm.Font.Mincho, 59 * 2, boxX2 - 10, false, new SvfForm.Point(boxX1 + 10, boxY1 + 10), 90, "履修済み備考").setRepeatConfig(rcBiko));
            }
            return true;
        }
        @Override
        public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
            printPage2(student, pInfo, page, pg, csvLines);
        }

        private void printPage2(final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            if (null != csvLines) {
                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).add("様式１（裏面）");
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("", "", "各教科・科目等の修得単位数の記録"));

                final List<List<String>> nameLines = new ArrayList<List<String>>();
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines).addAll(Arrays.asList("生徒氏名", pInfo.getPrintName1(), "", "", "", "", "", ""));
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines);

                csvLines.addAll(CsvUtils.horizontalUnionLines(nameLines, getCsvGakusekiLines(pInfo, printGakuseki)));
            } else {
                setForm(student, pInfo);
                if (param()._isOutputDebug) {
                    log.info(" page " + page + ", " + printGakuseki);
                }

                if (param()._is133m) {
                    printName2M(student, pInfo);
                    if (isPrintGakuseki(param())) {
                        printGakuseki2(student, pInfo, printGakuseki);
                    }
                } else {
                    if (_formHasHrHeader) {
                        printGakuseki2(student, pInfo, printGakuseki);
                    }
                }
                if (param()._z010.in(Z010.teihachi)) {
                    printSvfRenban("RISHUZUMI", student._htrainRemarkDetail2Hdat002Remark1, KNJPropertiesShokenSize.getShokenSize(null, 59, 5));
                }
            }
            boolean containsDropped = false;
            for (final Gakuseki gakuseki : pInfo._gakusekiList) {
                if (gakuseki._isDrop) {
                    containsDropped = true;
                }
            }
            final List<Gakuseki> gakusekiList;
            if (containsDropped) {
                gakusekiList = printGakuseki._gakusekiList;
            } else {
                gakusekiList = pInfo._gakusekiList;
            }
            setStudyDetail2(student, pInfo, page, gakusekiList, csvLines);
        }

        private String eduDivFieldname() {
            if (_isPrintEduDiv2CharsPerLine) {
                return "EDU_DIV_2";
            }
            return "EDU_DIV";
        }

        private String subclassnameFieldname() {
            return "SUBCLASSNAME";
        }

        private String creditFieldname() {
            return "CREDIT";
        }

        private void setStudyDetail2(final Student student, final PersonalInfo pInfo, final Integer page, final List<Gakuseki> gakusekiList, final List<List<String>> csvLines) {
            String remarkText = null;
            if (param()._is133m) {
                if (param()._z010.in(Z010.kyoto)) {
                    if (isNewForm(param(), pInfo)) {
                        remarkText = "※( )内は履修のみ認められた単位数を表す。";
                    } else {
                    }
                }
            } else {
                final String compCreditRemark = "※( )内は履修だけが認められた単位数を表す。";
                if (param()._z010.in(Z010.kyoto)) {
                    if (isNewForm(param(), pInfo)) {
                        remarkText = compCreditRemark;
                    } else {
                    }
                } else if (param()._isNotUseCompCreditYoshiki1Ura) {
                } else {
                    remarkText = compCreditRemark;
                }
            }
            if (!StringUtils.isBlank(remarkText)) {
                svfVrsOut("REMARK", remarkText); // 備考
            }

            if (param()._is133m) {
                final List<StudyRec.TotalM> lastList = new ArrayList<StudyRec.TotalM>();
                final Map<StudyRec.TotalM, List<BigDecimal>> totalMCreditListMap = new TreeMap<StudyRec.TotalM, List<BigDecimal>>();
                if (param()._z010.in(Z010.sagaken)) {
                    lastList.addAll(Arrays.asList(StudyRec.TotalM.SUBJECT90, StudyRec.TotalM.ABROAD, StudyRec.TotalM.TOTAL));
                    final List<StudyRec.TotalM> totalMs = Arrays.asList(StudyRec.TotalM.SUBJECT90, StudyRec.TotalM.SUBJECT90_SAGA_A, StudyRec.TotalM.SUBJECT90_SAGA_B, StudyRec.TotalM.ABROAD, StudyRec.TotalM.TOTAL, StudyRec.TotalM.TOTAL_SAGA_A, StudyRec.TotalM.TOTAL_SAGA_B);
                    for (final StudyRecYearTotalM yearTotal : pInfo.getStudyRecYearM(param(), YOSHIKI._1_URA).values()) {
                        for (final StudyRec.TotalM totalM : totalMs) {
                            getMappedList(totalMCreditListMap, totalM).addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yearTotal.list(totalM), param(), YOSHIKI._1_URA));
                        }
                    }
                } else {
                    svfVrsOut("FOOTER_SUBCLASSNAME", pInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(gakusekiList)));
                }

                String specialDiv = "00";
                int linex = 0;
                boolean isFirstPage = true;
                final Map<String, StudyRecSubclassTotal> studyRecSubclassTotalMap = PersonalInfo.createStudyRecTotalMap(param(), pInfo, PersonalInfo.getSubclassStudyrecListMap(param(), isFirstPage, student, pInfo, pInfo._gakusekiList, YOSHIKI._1_URA));

                final List<StudyRecSubclassTotal> studyRecSubclassList = new ArrayList<StudyRecSubclassTotal>(studyRecSubclassTotalMap.values());
                Collections.sort(studyRecSubclassList, new StudyRecSubclassTotal.Comparator(param(), YOSHIKI._1_URA));

                final List<StudyrecTotalSpecialDiv> studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList2(param(), student, pInfo, studyRecSubclassList);
                final String fieldEduDiv1 = param()._z010.in(Z010.kumamoto) || param()._z010.in(Z010.naraken) ? "EDU_DIV_1" : param()._z010.in(Z010.tokyoto) ? "EDU_DIV1" : eduDivFieldname();
                final String fieldEduDiv_2 = param()._z010.in(Z010.kumamoto) || param()._z010.in(Z010.naraken) ? "EDU_DIV_2" : null;
                final String fieldEduDiv2 = "EDU_DIV2";
                final String fieldClassname = param()._z010.in(Z010.tokyoto) ? "CLASSNAME1" : "CLASSNAME";
                final String fieldClassname2 = "CLASSNAME2";
                final String fieldSubclassname = param()._z010.in(Z010.tokyoto) ? "SUBCLASSNAME1" : "SUBCLASSNAME";
                final String fieldClasscd = "CLASSCD";
                final String fieldCredit = param()._z010.in(Z010.tokyoto) ? "CREDIT1" : "CREDIT";

                for (final StudyrecTotalSpecialDiv studyrectotalSpecialDiv : studyrecTotalSpecialDivList) {
                    if (studyrectotalSpecialDiv.isAllNotTarget(param())) {
                        continue;
                    }

                    specialDiv = studyrectotalSpecialDiv.first().classMst(param(), YOSHIKI._1_URA)._specialDiv;
                    final String s_specialname = param().getSpecialDivName(KNJA130_0.isNewForm(param(), pInfo), specialDiv);
                    final List<String> list_specialname = Util.toCharList(s_specialname); // 普通・専門名のリスト
                    int idxSpecialname = 0;

                    for (final StudyrecTotalClass studyrectotalClass : studyrectotalSpecialDiv._classes) {
                        // 総合的な学習の時間・留学は回避します。
                        if (_90.equals(studyrectotalClass.first().classMst(param(), YOSHIKI._1_URA)._classcd)) {
                            continue;
                        } else if (_ABROAD.equals(studyrectotalClass.first().classMst(param(), YOSHIKI._1_URA)._classname)) {
                            continue;
                        } else if (studyrectotalClass.isAllNotTarget(param())) {
                            continue;
                        }

                        final String classcd = studyrectotalClass.first().classMst(param(), YOSHIKI._1_URA)._classcd; // 教科コードの保存
                        final List<String> list_classname = Util.toCharList(studyrectotalClass.first().classMst(param(), YOSHIKI._1_URA)._classname); // 教科名のリスト

                        final List<Tuple<String, Tuple<String, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, List<BigDecimal>>>>>>> list_subclass = new LinkedList<Tuple<String, Tuple<String, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, List<BigDecimal>>>>>>>();
                        for (final StudyrecTotalSubclass studyrectotalSubclass : studyrectotalClass._subclasses) {
                            if (studyrectotalSubclass.isAllNotTarget(param())) {
                                continue;
                            }

                            for (final StudyRecSubclassTotal sst : studyrectotalSubclass._totals) {
                                final String subclasscd = getSubclasscdM(sst.studyrec().subclassMst(param(), YOSHIKI._1_URA), param());
                                final String subclassname = sst.subclassMst(param(), YOSHIKI._1_URA).subclassname();
                                final List<BigDecimal> creditList = sst.kindCreditsM(StudyRec.CreditKind.CREDIT, YOSHIKI._1_URA);
                                final List<BigDecimal> compCreditList = sst.kindCreditsM(StudyRec.CreditKind.COMP_CREDIT, YOSHIKI._1_URA);
                                final List<BigDecimal> creditSagaAlist = sst.kindCreditsM(StudyRec.CreditKind.CREDIT_SAGA_A, YOSHIKI._1_URA);
                                final List<BigDecimal> creditSagaBlist = sst.kindCreditsM(StudyRec.CreditKind.CREDIT_SAGA_B, YOSHIKI._1_URA);
                                list_subclass.add(Tuple.of(subclasscd, Tuple.of(subclassname, Tuple.of(creditList, Tuple.of(compCreditList, Tuple.of(creditSagaAlist, creditSagaBlist))))));
                            }
                        }

                        final int nameline;
                        if (param()._z010.in(Z010.kumamoto)) {
                            nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 0) : list_classname.size();
                        } else {
                            // 教科名文字数と科目数で多い方を教科の行数にする。教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
                            nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 1) : list_classname.size();

                            // 教科が次列に跨らないために、空行を出力する
                            if ((linex < MAX_LINE1 && MAX_LINE1 < linex + nameline) ||
                                (idxin(MAX_LINE1, linex, MAX_LINE2) && MAX_LINE2 < linex + nameline) ||
                                (idxin(MAX_LINE2, linex, MAX_LINE_PER_PAGE) && MAX_LINE_PER_PAGE < linex + nameline)) {
                                final int max = (linex < MAX_LINE1) ? MAX_LINE1 : (linex < MAX_LINE2) ? MAX_LINE2 : MAX_LINE_PER_PAGE;
                                for (int j = linex; j < max; j++) {
                                    if (linex == MAX_LINE_PER_PAGE) {
                                        linex = 0;
                                    }
                                    svfVrEndRecordM(fieldEduDiv2, specialDiv, student, pInfo);
                                    linex++;
                                }
                            }
                        }

                        for (int i = 0; i < nameline; i++) {
                            if (linex == MAX_LINE_PER_PAGE) {
                                linex = 0;
                            }
                            if (idxSpecialname < list_specialname.size()) {
                                svfVrsOut(fieldEduDiv1, list_specialname.get(idxSpecialname)); // 普通・専門名
                                idxSpecialname += 1;
                                if (null != fieldEduDiv_2 && idxSpecialname < list_specialname.size()) {
                                    svfVrsOut(fieldEduDiv_2, list_specialname.get(idxSpecialname)); // 普通・専門名
                                    idxSpecialname += 1;
                                }
                            }
                            if (i < list_classname.size()) {
                                svfVrsOut(fieldClassname, Util.str(list_classname.get(i))); // 教科名
                            }
                            if (i < list_subclass.size()) {
                                final Tuple<String, Tuple<String, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, Tuple<List<BigDecimal>, List<BigDecimal>>>>>> subclass = list_subclass.get(i);
//                                final String subclasscd = subclass._first;
                                final String subclassname = subclass._second._first;
                                final List<BigDecimal> credits = subclass._second._second._first;
//                                final List<BigDecimal> compCredits = subclass._second._second._second._first;
                                final List<BigDecimal> creditSagaAlist = subclass._second._second._second._second._first;
                                final List<BigDecimal> creditSagaBlist = subclass._second._second._second._second._second;

                                if (param()._isOutputDebug) {
                                    log.info(" subclass = " + subclass);
                                }
//                                final String subclasscd = (String) subclass.get("SUBCLASSCD");
//                                log.debug(subclass[0] + " = " + subclassname);
//                                final List compCredits = (List) subclass[3];

                                svfFieldAttribute2(fieldSubclassname, subclassname, linex, pInfo); // SVF-FIELD属性変更のメソッド
                                svfVrsOut(fieldSubclassname, subclassname); // 科目名

                                String creVal = "";
                                if (!credits.isEmpty()) {
//                                    if (credit.intValue() == 0) {
//                                        if (null != compCredit && compCredit.intValue() > 0) {
//                                            // 履修単位数
//                                            creVal = "(" + compCredit.intValue() + ")";
//                                        }
//                                    } else {
                                        // 修得単位数
                                        creVal = Util.bdSum(credits).toString();
//                                    }
                                }

//                                boolean isOutputCredit = false;
//
//                                if (null != subclasscd) {
//                                    final String substBikoZenbu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ZENBU, null, null).toString();
//                                    final String substBikoIchibu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ICHIBU, null, null).toString();
//                                    if (!StringUtils.isBlank(substBikoZenbu)) {
//                                        final List biko = getTokenList(substBikoZenbu, 10, 2); // 全部代替科目備考
//                                        for (int j = 0; j < biko.size(); j++) {
//                                            svfVrsOut("CREDIT" + (2 + j), str(biko.get(j)));
//                                        }
//                                        isOutputCredit = true;   // 全部代替科目備考を表示する場合、修得単位数は表示しない
//                                    } else if (!StringUtils.isBlank(substBikoIchibu)) {
//                                        svfVrsOut("CREDIT4_1", creVal);
//                                        final List biko = getTokenList(substBikoIchibu, 14, 2); // 一部代替科目備考
//                                        for (int j = 0; j < biko.size(); j++) {
//                                            svfVrsOut("CREDIT4_" + (2 + j), str(biko.get(j)));
//                                        }
//                                        isOutputCredit = true;
//                                    }
//                                }
//                                if (!isOutputCredit) {
                                    svfVrsOut(fieldCredit, creVal);
//                                }
                                  if (param()._z010.in(Z010.sagaken)) {
                                      svfVrsOut("CREDIT_A", defstr(Util.bdSum(creditSagaAlist), null));
                                      svfVrsOut("CREDIT_B", defstr(Util.bdSum(creditSagaBlist), null));
                                  }
                            }

                            svfVrsOut(fieldClassname2, classcd); // 教科コード
                            svfVrEndRecordM(fieldEduDiv2, specialDiv, student, pInfo);
                            linex++;
                        }
                    }

                    // 普通・専門名文字数
                    if (idxSpecialname < list_specialname.size()) {
                        final int nameline;
                        if (null != fieldEduDiv_2) {
                            nameline = (list_specialname.size() - idxSpecialname) / 2 + ((list_specialname.size() - idxSpecialname) > 0 && (list_specialname.size() - idxSpecialname) % 2 > 0 ? 1 : 0);
                        } else {
                            nameline = (list_specialname.size() - idxSpecialname);
                        }
//                        // 教科が次列に跨らないために、空行を出力する
//                        if ((idxin(0, linex, L1) && L1 < linex + nameline) ||
//                            (idxin(L1, linex, L2) && L2 < linex + nameline) ||
//                            (idxin(L2, linex, L3) && L3 < linex + nameline)) {
//                            final int k = (linex < L1) ? L1 : (linex < L2) ? L2 : L3;
//                            for (int j = linex; j < k; j++) {
//                                if (linex == L3) {
//                                    linex = 0;
//                                }
//                                svfVrEndRecord(fieldEduDiv2, specialDiv, student);
//                                linex++;
//                            }
//                        }
                        for (int i = 0; i < nameline; i++) {
                            if (linex == MAX_LINE_PER_PAGE) {
                                linex = 0;
                            }
                            svfVrsOut(fieldEduDiv1, list_specialname.get(idxSpecialname)); // 普通・専門名
                            idxSpecialname += 1;
                            if (null != fieldEduDiv_2 && idxSpecialname < list_specialname.size()) {
                                svfVrsOut(fieldEduDiv_2, list_specialname.get(idxSpecialname)); // 普通・専門名
                                idxSpecialname += 1;
                            }
                            svfVrEndRecordM(fieldEduDiv2, specialDiv, student, pInfo);
                            linex++;
                        }
                    }
                }

                for (int i = linex; i < MAX_LINE_PER_PAGE - lastList.size(); i++) {
                    svfVrsOut(fieldClasscd, ""); // 教科コード
                    svfVrEndRecordM(fieldEduDiv2, specialDiv, student, pInfo);
                }
                if (param()._z010.in(Z010.sagaken)) {
                    for (final StudyRec.TotalM totalM : lastList) {
                        if (totalM == StudyRec.TotalM.SUBJECT90) {
                            svfVrsOut("SUBCLASSNAME2", pInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(pInfo._gakusekiList)));
                            svfVrsOut("CREDIT_A2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.SUBJECT90_SAGA_A)), null));
                            svfVrsOut("CREDIT_B2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.SUBJECT90_SAGA_B)), null));
                            svfVrsOut("CREDIT2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.SUBJECT90)), null));
                        } else if (totalM == StudyRec.TotalM.ABROAD) {
                            svfVrsOut("SUBCLASSNAME2", "留　学");
                            svfVrsOut("CREDIT_A2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.ABROAD)), null));
                            svfVrsOut("CREDIT2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.ABROAD)), null));
                        } else if (totalM == StudyRec.TotalM.TOTAL) {
                            svfVrsOut("SUBCLASSNAME2", "総　計");
                            svfVrsOut("CREDIT_A2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.TOTAL_SAGA_A)), null));
                            svfVrsOut("CREDIT_B2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.TOTAL_SAGA_B)), null));
                            svfVrsOut("CREDIT2", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.TOTAL)), null));
                        }
                        svfVrEndRecord();
                    }
                }
            } else {
                final boolean isTokiwaForm2 = param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo);
                String specialDiv = "00";
                String classcd = "00";
                int linex = 0;
                final boolean isFirst = page.intValue() == 1;
                final Map<String, List<StudyRec>> subclassStudyrecListMap = PersonalInfo.getSubclassStudyrecListMap(param(), isFirst, student, pInfo, gakusekiList, _yoshiki);
                final Map<String, StudyRecSubclassTotal> studyRecSubclassTotalMap = PersonalInfo.createStudyRecTotalMap(param(), pInfo, subclassStudyrecListMap);
                final TreeSet<String> yearSet = new TreeSet<String>();
                for (final List<StudyRec> studyrecList : subclassStudyrecListMap.values()) {
                    for (final StudyRec sr : studyrecList) {
                        if (null != sr._year) {
                            yearSet.add(sr._year);
                        }
                    }
                }

                final List<StudyRecSubclassTotal> studyRecSubclassTotalList = new ArrayList(studyRecSubclassTotalMap.values());
                Collections.sort(studyRecSubclassTotalList, new StudyRecSubclassTotal.Comparator(param(), YOSHIKI.NONE));

                final List<PrintLine> printLineList = new ArrayList<PrintLine>();
                final List<StudyrecTotalClass> creditOnlyStudyrecTotalClassList = new ArrayList<StudyrecTotalClass>();
                final List<StudyrecTotalClass> e065StudyrecTotalClassList = new ArrayList<StudyrecTotalClass>();
                final List<StudyrecTotalSpecialDiv> studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList2(param(), student, pInfo, studyRecSubclassTotalList);
                for (final StudyrecTotalSpecialDiv studyrectotalSpecialDiv : studyrecTotalSpecialDivList) {
                    if (studyrectotalSpecialDiv.isAllNotTarget(param())) {
                        if (param()._isOutputDebugSeiseki) {
                            log.info(" all not target specialdiv : first = " + studyrectotalSpecialDiv.first());
                        }
                        continue;
                    }

                    specialDiv = studyrectotalSpecialDiv.first().classMst(param(), _yoshiki)._specialDiv;
                    final String s_specialname = param().getSpecialDivName(isNewForm(param(), pInfo), specialDiv);
                    int lineSpecialDiv = 0;

                    for (final StudyrecTotalClass studyrectotalClass : studyrectotalSpecialDiv._classes) {
                        // 総合的な学習の時間・留学は回避します。
                        final List<StudyRec.KIND> kindList = studyrectotalClass.first().studyrec().kindList(param(), pInfo);
                        if (kindList.contains(StudyRec.KIND.SOGO90) ||
                                kindList.contains(StudyRec.KIND.ABROAD) ||
                                studyrectotalClass.isAllNotTarget(param())) {
                            if (param()._isOutputDebugSeiseki) {
                                log.info(" all not target subclass : first = " + studyrectotalClass.first());
                            }
                            continue;
                        }
                        if (kindList.contains(StudyRec.KIND.CREDIT_ONLY)) {
                            creditOnlyStudyrecTotalClassList.add(studyrectotalClass);
                            continue;
                        }
                        if (kindList.contains(StudyRec.KIND.JIRITSU)) {
                            boolean hasData = false;
                            searchTargetJIRITSU:
                                for (final StudyrecTotalSubclass sts : studyrectotalClass._subclasses) {
                                    for (final StudyRecSubclassTotal sst : sts._totals) {
                                        if (StudyRecSubclassTotal.isEnabledJiritsu(param(), sst)) {
                                            hasData = true;
                                            break searchTargetJIRITSU;
                                        }
                                    }
                                }
                            if (hasData) {
                                e065StudyrecTotalClassList.add(studyrectotalClass);
                            }
                            continue;
                        }

                        classcd = studyrectotalClass.first().studyrec()._classMst._classcd; // 教科コードの保存
                        final List<String> list_classname = Util.toCharList(studyrectotalClass.first().studyrec()._classMst._classname); // 教科名のリスト

                        final Collection<String> enabledDropYears = pInfo.getEnabledDropYears(param());
                        final List<Tuple<String, Tuple<String, Tuple<BigDecimal, Tuple<BigDecimal, Tuple<BigDecimal, List<Map<StudyRec.CreditKind, BigDecimal>>>>>>>> list_subclass = new LinkedList<Tuple<String, Tuple<String, Tuple<BigDecimal, Tuple<BigDecimal, Tuple<BigDecimal, List<Map<StudyRec.CreditKind, BigDecimal>>>>>>>>();
                        for (final StudyrecTotalSubclass studyrectotalSubclass : studyrectotalClass._subclasses) {
                            // log.debug(" subclass = " + studyrectotalSubclass._subclasscd + ":" + studyrectotalSubclass._subclassname);
                            if (studyrectotalSubclass.isAllNotTarget(param())) {
                                if (param()._isOutputDebugSeiseki) {
                                    log.info(" all not target subclass : first = " + studyrectotalSubclass.first());
                                }
                                continue;
                            }

                            for (final StudyRecSubclassTotal sst : studyrectotalSubclass._totals) {
                                list_subclass.add(
                                        Tuple.of(sst.studyrec().getKeySubclasscd(param()),
                                                Tuple.of(sst.subclassMst(param(), _yoshiki).subclassname(),
                                                        Tuple.of(Util.bdSum(sst.kindListForTotal(param(), StudyRec.CreditKind.CREDIT, enabledDropYears)),
                                                                Tuple.of(Util.bdSum(sst.compCreditListForTotal(param(), enabledDropYears)),
                                                                        Tuple.of(Util.bdSum(sst.creditMstCreditListForTotal(param(), enabledDropYears)),
                                                                                sst.bunkatuRishuCreditMapListForTotal(param())))))));
                            }
                        }

                        if (param()._isOutputDebugSeiseki) {
                            for (final Tuple<String, Tuple<String, Tuple<BigDecimal, Tuple<BigDecimal, Tuple<BigDecimal, List<Map<StudyRec.CreditKind, BigDecimal>>>>>>> t : list_subclass) {
                                log.info(" output line subclass " + t);
                            }
                        }

                        if (isTokiwaForm2) {
                            svfVrsOut("CLASSNAME", studyrectotalClass.first().studyrec()._classMst._classname); // 教科名
                            for (int i = 0; i < list_subclass.size(); i++) {
                                final Tuple<String, Tuple<String, Tuple<BigDecimal, Tuple<BigDecimal, Tuple<BigDecimal, List<Map<StudyRec.CreditKind, BigDecimal>>>>>>> subclass = list_subclass.get(i);
                                // final String subclasscd = (String) subclass[0];
                                final String subclassname = subclass._second._first;
                                final BigDecimal credit = subclass._second._second._first;
                                final BigDecimal compCredit = subclass._second._second._second._first;
                                final BigDecimal creditMstCredits = subclass._second._second._second._second._first;

                                final String sfx = i == 0 ? "1" : "2";

                                svfVrsOut("GRP" + sfx, classcd); // 教科コード
                                svfVrsOut("SUBCLASSNAME" + sfx + "_" + (getMS932ByteLength(subclassname) > 16 ? "2" : "1"), subclassname); // 科目名
                                String creVal = "";
                                String compCreVal = "";
                                if (pInfo.isTaigaku() || pInfo.isTengaku()) {
                                    creVal = null == credit ? "0" : credit.toString();
                                    final String creVal1 = null == compCredit ? "0" : compCredit.toString(); // 修得単位数
                                    final String creVal2 = null == creditMstCredits ? " " : creditMstCredits.toString(); // 単位マスタの単位
                                    compCreVal = creVal1 + SLASH + creVal2;
                                } else {
                                    creVal = Util.str(credit);
                                    compCreVal = Util.str(compCredit);
                                }
                                svfVrsOut("COMP_CREDIT" + sfx, compCreVal);
                                svfVrsOut("GET_CREDIT" + sfx, creVal);
                                svfVrEndRecord();
                                linex++;
                            }
                        } else {

                            // 教科名文字数と科目数で多い方を教科の行数にする。教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
                            final int nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 1) : list_classname.size();

                            // 教科が次列に跨らないために、空行を出力する
                            if ((linex < MAX_LINE1 && MAX_LINE1 < linex + nameline) ||
                                    (idxin(MAX_LINE1, linex, MAX_LINE2) && MAX_LINE2 < linex + nameline) ||
                                    (idxin(MAX_LINE2, linex, MAX_LINE_PER_PAGE) && MAX_LINE_PER_PAGE < linex + nameline)) {
                                final int max = (linex < MAX_LINE1) ? MAX_LINE1 : (linex < MAX_LINE2) ? MAX_LINE2 : MAX_LINE_PER_PAGE;
                                for (int j = linex; j < max; j++) {
                                    final PrintLine line = newLine(linex, printLineList);
                                    line._eduDiv2 = specialDiv;
                                    line._debugcomment = "column blank";
                                    printLine2(line, student, pInfo, csvLines);
                                    svfVrEndRecord();
                                    linex++;
                                }
                            }

                            for (int i = 0; i < nameline; i++) {
                                final PrintLine l = newLine(linex, printLineList);

                                final String eduDiv = eduDiv(param(), s_specialname, lineSpecialDiv);
                                if (!StringUtils.isBlank(eduDiv)) {
                                    svfVrsOut(eduDivFieldname(), eduDiv); // 普通・専門名
                                    l._eduDiv = eduDiv;
                                }
                                if (i < list_classname.size()) {
                                    final String cn = Util.str(list_classname.get(i));
                                    svfVrsOut("CLASSNAME", cn); // 教科名
                                    l._classname = cn;
                                }
                                Tuple<String, Tuple<String, Tuple<BigDecimal, Tuple<BigDecimal, Tuple<BigDecimal, List<Map<StudyRec.CreditKind, BigDecimal>>>>>>> subclass = null;
                                final StringBuffer gakushuubiko = new StringBuffer();
                                final StringBuffer rishuTanniBiko = new StringBuffer();
                                if (i < list_subclass.size()) {
                                    subclass = list_subclass.get(i);
                                    final String subclasscd = subclass._first;
                                    final String subclassname = subclass._second._first;
                                    final BigDecimal credit = subclass._second._second._first;
                                    final BigDecimal compCredit = subclass._second._second._second._first;
                                    final BigDecimal creditMstCredits = subclass._second._second._second._second._first;

                                    final List<Map<StudyRec.CreditKind, BigDecimal>> bunkatuRishuCreditMapList = subclass._second._second._second._second._second;
                                    if (param()._isOutputDebugSeiseki) {
                                        log.info(" subclasscd = " + subclasscd + ", subclassname = " + subclassname + ", credit = "+ credit + ", compCredit = " + compCredit + ", creditMstCredits = " + creditMstCredits + ", bunkatuRishu = " + bunkatuRishuCreditMapList);
                                    }

                                    svfFieldAttribute2(subclassnameFieldname(), subclassname, linex, pInfo); // SVF-FIELD属性変更のメソッド
                                    svfVrsOut(subclassnameFieldname(), subclassname); // 科目名
                                    l._subclassname = subclassname;

                                    String creVal = "";
                                    String compCreVal = null;
                                    if (param()._z010.in(Z010.tokiwa) && isNewForm(param(), pInfo)) {
                                        if (pInfo.isTaigaku() || pInfo.isTengaku()) {
                                            creVal = defstr(credit, "0");
                                            final String creVal1 = defstr(compCredit, "0"); // 修得単位数
                                            final String creVal2 = defstr(creditMstCredits, " "); // 単位マスタの単位
                                            compCreVal = creVal1 + SLASH + creVal2;
                                        } else {
                                            creVal = Util.str(credit);
                                            compCreVal = Util.str(compCredit);
                                        }
                                    } else {
                                        creVal = getCreVal(credit, compCredit, bunkatuRishuCreditMapList);
                                    }

                                    boolean isOutputCredit = false;

                                    if (null != subclasscd && !param().isNotPrintDaitai("1ura")) {
                                        final String substBikoZenbu = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, GakushuBiko.DAITAI_TYPE.ZENBU, null, null).toString();
                                        final String substBikoIchibu = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, GakushuBiko.DAITAI_TYPE.ICHIBU, null, null).toString();
                                        if (!StringUtils.isBlank(substBikoZenbu)) {
                                            // 全部代替科目備考
                                            svfVrsOutGroupForData(new String[][] {{"CREDIT2", "CREDIT3"}, {"CREDIT4_1", "CREDIT4_2", "CREDIT4_3"}, {"CREDIT5_1", "CREDIT5_2", "CREDIT5_3"}}, substBikoZenbu);
                                            l._biko = substBikoZenbu;
                                            isOutputCredit = true;   // 全部代替科目備考を表示する場合、修得単位数は表示しない
                                        } else if (!StringUtils.isBlank(substBikoIchibu)) {
                                            // 一部代替科目備考
                                            svfVrsOut("CREDIT4_1", creVal);
                                            svfVrsOutSplit(new String[] {"CREDIT4_2", "CREDIT4_3"}, substBikoIchibu);
                                            l._biko = substBikoIchibu;
                                            isOutputCredit = true;
                                        }
                                    }
                                    if (!isOutputCredit) {
                                        l._credit = creVal;
                                        svfVrsOut(creditFieldname(), creVal);
                                        if (null != compCreVal) {
                                            svfVrsOut("COMP_CREDIT", compCreVal);
                                            l._compCredit = compCreVal;
                                        }
                                    }
                                    if (_isPrintBiko) {
                                        if (!yearSet.isEmpty()) {
                                            final String minYear = yearSet.first();
                                            final String maxYear = yearSet.last();
                                            gakushuubiko.append(pInfo._gakushuBiko.getStudyrecBiko(subclasscd, minYear, maxYear));
                                            rishuTanniBiko.append(pInfo._gakushuBiko.getRishuTanniBiko(subclasscd, minYear, maxYear));
                                        }
                                    }
                                }

                                l._eduDiv2 = specialDiv;
                                l._classname2 = classcd;
                                if (_isPrintBiko) {
                                    l._biko = Util.mkString(Arrays.asList(gakushuubiko.toString(), rishuTanniBiko.toString()), "、").toString();
                                }
                                l._debugcomment = null == subclass ? "" : ArrayUtils.toString(subclass);
                                printLine2(l, student, pInfo, csvLines);
                                svfVrEndRecord();
                                linex++;
                                lineSpecialDiv++;
                            }

                            if (linex == MAX_LINE_PER_PAGE) {
                                linex = 0;
                            }
                        }
                    }

                    classcd = "00";
                    // 普通・専門名文字数
                    if (isTokiwaForm2) {
                    } else {
                        String eduDiv;
                        if (!StringUtils.isBlank(eduDiv = eduDiv(param(), s_specialname, lineSpecialDiv))) {
//                        final int nameline = list_specialname.size();
//                        // 教科が次列に跨らないために、空行を出力する
//                        if ((idxin(0, linex, MAX_LINE1) && MAX_LINE1 < linex + nameline) ||
//                            (idxin(MAX_LINE1, linex, MAX_LINE2) && MAX_LINE2 < linex + nameline) ||
//                            (idxin(MAX_LINE2, linex, MAX_LINE_PER_PAGE) && MAX_LINE_PER_PAGE < linex + nameline)) {
//                            final int k = (linex < MAX_LINE1) ? MAX_LINE1 : (linex < MAX_LINE2) ? MAX_LINE2 : MAX_LINE_PER_PAGE;
//                            for (int j = linex; j < k; j++) {
//                                final PrintLine l = newLine(linex, printLineList);
//                                l._eduDiv2 = specialDiv;
//                                l._classname2 = classcd;
//                                printLine(l, student, pInfo);
//                                linex++;
//                                lineSpecialDiv++;
//                            }
//                        }
                            while (!StringUtils.isBlank(eduDiv = eduDiv(param(), s_specialname, lineSpecialDiv))) {
                                final PrintLine l = newLine(linex, printLineList);
                                l._eduDiv = eduDiv;
                                l._eduDiv2 = specialDiv;
                                l._classname2 = classcd;
                                l._debugcomment = "rest classname";
                                printLine2(l, student, pInfo, csvLines);
                                svfVrEndRecord();
                                linex++;
                                lineSpecialDiv++;
                            }
                            // 普通・専門名のリストを削除する
                            if (linex == MAX_LINE_PER_PAGE) {
                                linex = 0;
                            }
                        }
                    }
                }

                final PrintLine blankLine = new PrintLine();
                blankLine._eduDiv2 = specialDiv;
                blankLine._classname2 = classcd;
                boolean isfirst = true;
                int printlinex = linex;
                final int l;
                if (_isLastsRecord) {
                    if (param()._z010.in(Z010.kyoto) || _isKenja) {
                        l = 1 /* 総合的な学習の時間 */ + getLastCreditList(pInfo, setCreditsMap(pInfo, gakusekiList, studyRecSubclassTotalList)._first).size();
                    } else if (param()._z010.in(Z010.miyagiken)) {
                        l = 1 /* 留学 */ + 2 /* 総学 */ + getLastCreditList(pInfo, setCreditsMap(pInfo, gakusekiList, studyRecSubclassTotalList)._first).size() * 2 /* E065 * 2 + 合計 2 */;
                    } else if (param()._z010.in(Z010.naraken)) {
                        l = 2 /* 総学 */ + getLastCreditList(pInfo, setCreditsMap(pInfo, gakusekiList, studyRecSubclassTotalList)._first).size() * 2 /* E065 * 2 + 合計 2 */;
                    } else if (param()._z010.in(Z010.mieken)) {
                        l = e065StudyrecTotalClassList.size() + 4 /* 総合的な学習の時間 1 + 小計 1 + 留学 1 + 合計 1 */;
                    } else {
                        // 福井県
                        l = (e065StudyrecTotalClassList.size() > 0 ? 2 : 0) + 3 /* 総学 1.5 + 留学 1.5 */;
                    }
                } else {
                    l = 0;
                }
                final List<StudyrecTotalSubclass> hanasuSubclassList = new ArrayList<StudyrecTotalSubclass>();
                for (final StudyrecTotalClass sc : creditOnlyStudyrecTotalClassList) {
                    hanasuSubclassList.addAll(sc._subclasses);
                }
                if (hanasuSubclassList.size() > 0) {
                    if (param()._isOutputDebug) {
                        log.info(" hanasu subclass list (size = " + hanasuSubclassList.size() + ") : " + hanasuSubclassList);
                    }
                }

                final int blankCount;
                if (_isLastsRecord) {
                    blankCount = MAX_LINE_PER_PAGE - hanasuSubclassList.size();
                } else {
                    if (hanasuSubclassList.size() > 1) {
                        blankCount = MAX_LINE_PER_PAGE - (hanasuSubclassList.size() - 1) - 1;
                    } else {
                        blankCount = MAX_LINE_PER_PAGE - 1;
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" MAX_LINE_PER_PAGE = " + MAX_LINE_PER_PAGE + ", blank count = " + blankCount+ ", lasts = " + l);
                }
                for (;printlinex < blankCount - l; printlinex++) {
                    blankLine._linex = linex;
                    if (isTokiwaForm2) {
                        blankLine._classname2Field = "GRP" + (isfirst ? "1" : "2");
                    }
                    blankLine._debugcomment = "last blank1";
                    printLine2(blankLine, student, pInfo, csvLines);
                    svfVrEndRecord();
                    linex++;
                    isfirst = false;
                }
                if (!_isLastsRecord) {
                    for (int i = 0; i < hanasuSubclassList.size() - 1; i++) {
                        final StudyrecTotalSubclass total = hanasuSubclassList.get(i);
                        if (param()._isOutputDebugSvfOut) {
                            log.info(" hanasu total (" + i + " / " + hanasuSubclassList.size() + ") = " + total);
                        }
                        final PrintLine line = setLineSubclass(pInfo, isTokiwaForm2, specialDiv, linex, total, isfirst);
                        line._debugcomment = "last blank2";
                        printLine2(line, student, pInfo, csvLines);
                        svfVrEndRecord();
                        linex++;
                    }
                }
                printFooterRemark(pInfo, YOSHIKI._1_URA);
                if (_isLastsRecord) {
                    for (final StudyrecTotalSubclass total : hanasuSubclassList) {
                        final PrintLine line = setLineSubclass(pInfo, isTokiwaForm2, specialDiv, linex, total, isfirst);
                        line._debugcomment = "last";
                        printLine2(line, student, pInfo, csvLines);
                        svfVrEndRecord();
                        linex++;
                    }
                    // 単位数計はレコード
                    printTotalCredits2(student, pInfo, gakusekiList, studyRecSubclassTotalList, csvLines);
                } else {
                    // 単位数計は固定
                    printTotalCredits2(student, pInfo, gakusekiList, studyRecSubclassTotalList, csvLines);
                    if (isTokiwaForm2) {
                        blankLine._classname2Field = "GRP" + (isfirst ? "1" : "2");
                    }
                    if (!isTokiwaForm2 && !hanasuSubclassList.isEmpty()) {
                        final StudyrecTotalSubclass total = hanasuSubclassList.get(hanasuSubclassList.size() - 1);
                        if (param()._isOutputDebugSvfOut) {
                            log.info(" hanasu total last = " + total);
                        }
                        final PrintLine line = setLineSubclass(pInfo, isTokiwaForm2, specialDiv, linex, total, isfirst);
                        line._debugcomment = "last";
                        printLine2(line, student, pInfo, csvLines);
                        svfVrEndRecord();
                        linex++;
                    } else {
                        blankLine._debugcomment = "last";
                        printLine2(blankLine, student, pInfo, csvLines);
                        svfVrEndRecord();
                        linex++;
                    }
                }
            }
            nonedata = true;
        }

        private PrintLine setLineSubclass(final PersonalInfo pInfo, final boolean isForm2, String specialDiv, int linex, StudyrecTotalSubclass total, boolean isfirst) {
            final PrintLine line = new PrintLine();
            line._linex = linex;

            svfFieldAttribute2(subclassnameFieldname(), total.first().studyrec()._subclassMst._subclassname, linex, pInfo); // SVF-FIELD属性変更のメソッド
            svfVrsOut(subclassnameFieldname(), total.first().studyrec()._subclassMst._subclassname); // 科目名

            BigDecimal credit = null, compCredit = null;
            for (final StudyRecSubclassTotal srst : total._totals) {
                for (final StudyRec sr : srst._studyrecList) {
                    credit = addNumber(credit, sr._credit);
                    compCredit = addNumber(compCredit, sr._compCredit);
                }
            }

            svfVrsOut(creditFieldname(), getCreVal(credit, compCredit, null));

            line._eduDiv2 = specialDiv;
            line._classname2 = total.first().studyrec()._classMst._classcd;
            if (isForm2) {
                line._classname2Field = "GRP" + (isfirst ? "1" : "2");
            }
            return line;
        }

        private String getCreVal(final BigDecimal credit, final BigDecimal compCredit, final List<Map<StudyRec.CreditKind, BigDecimal>> bunkatuRishuCreditMapList) {
            String creVal = "";
            if (param()._isPrintYoshiki1UraBunkatuRishu && null != bunkatuRishuCreditMapList) {
                creVal = getBunkatuRishuCreditStr(bunkatuRishuCreditMapList, null);
            } else if (credit != null) {
                if (credit.doubleValue() == 0) {
//                                            if (param()._optionCreditOutput == Param.OPTION_CREDIT2) {
//                                                // 履修単位数があれば0、なければ空欄
//                                                if (null != compCredit && compCredit.intValue() > 0) {
//                                                    creVal = "0";
//                                                } else {
//                                                    creVal = "";
//                                                }
//                                            } else {
                    if (param()._z010.in(Z010.miyagiken)) {
                        creVal = null;
                    } else if (param()._isNotUseCompCreditYoshiki1Ura) {
                        creVal = credit.toString();
                    } else {
                        // 履修単位数があればカッコつきで表示
                        if (null != compCredit && compCredit.doubleValue() > 0) {
                            creVal = "(" + compCredit + ")";
                        }
                    }
//                                            }
                } else {
                    // 修得単位数
                    creVal = credit.toString();
                }
            }
            //log.info(" creVal = " + creVal + "( credit = " + credit + ", compCredit = " + compCredit + ")");
            return creVal;
        }

        private void printLine2(final PrintLine printLine, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            if (null != csvLines) {
                final List<String> csvLine = CsvUtils.newLine(csvLines);
                final String data = defstr(printLine._biko, printLine._compCredit, printLine._credit);
                csvLine.addAll(Arrays.asList(printLine._eduDiv, printLine._classname, printLine._subclassname, data));
                return;
            }
            printPersonalInfo2(student, pInfo);
            svfVrsOut(eduDivFieldname(), printLine._eduDiv);
            svfVrsOut("EDU_DIV2", printLine._eduDiv2);
            svfVrsOut(printLine._classname2Field, printLine._classname2); // 教科コード
            if (_isPrintBiko) {
                svfVrsOutForData(Arrays.asList("BIKOU",  "BIKOU2", "BIKOU3_1"), printLine._biko); // 教科コード
            }
            if (param()._isOutputDebugInner) {
                log.info(" line " + printLine._linex + " classname2 " + printLine._classname2Field + " = " + printLine._classname2 + " (" + printLine._debugcomment + ")");
            }
        }

        private static boolean idxin(final int minidx, final int i, final int length) {
            return minidx <= i && i < length;
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル・学籍履歴）を印刷します。
         * @param svf
         * @param student
         */
        private void printGakuseki2(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            final String lastyear = pInfo.getLastYear();
            final int max = is3nenYou(pInfo) ? 3 : 4;
            for (int i = 1; i <= 6; i++) {
                svfVrsOut("GRADE1_" + i, "");
                svfVrsOut("GRADE2_" + i, "");
                svfVrsOut("GRADE3_" + i, "");
                if (param()._is133m && param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("HGRADE1_" + i, "");
                }
                // ホームルーム
                svfVrsOut("HR_CLASS1_" + i, "");
                svfVrsOut("HR_CLASS2_" + i, "");
                svfVrsOut("ATTENDNO_" + i, "");
            }
            int pos = 1;
            for (final Gakuseki gakuseki : printGakuseki._gakusekiList) {
                printPersonalInfo2(student, pInfo);
                final int newpos = getGradeColumnNum(pInfo, pos, gakuseki, param()._z010.in(Z010.kyoto) ? GakusekiColumn.SEQ : GakusekiColumn.NORMAL, param(), max);

                if (param()._is133m && param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("HGRADE1_" + newpos, gakuseki._nendo);
                } else if (gakuseki._isKoumokuGakunen) {
                    svfVrsOut("GRADE1_" + newpos, gakuseki._gdat._gakunenSimple);
                } else {
                    if (param()._z010.in(Z010.miyagiken)) {
                        svfVrsOut("GRADE3_" + newpos, gakuseki._nendo);
                    } else {
                        svfVrsOut("GRADE2_" + newpos, gakuseki._nendo);
                    }
                }
                // ホームルーム
                svfVrsOutForData(Arrays.asList("HR_CLASS1_" + newpos,  "HR_CLASS2_" + newpos), gakuseki._hdat._hrname);
                svfVrsOut("ATTENDNO_" + newpos, gakuseki._attendno);

                final boolean islastyear = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (!islastyear && gakuseki._isDrop && !param()._z010.in(Z010.kyoto)) {
                    //printTurnOverThePages(svf, student);
                } else if (!islastyear && _pageMaxGrade == pos) {
                    //printTurnOverThePages(svf, student);
                    pos = 1;
                } else {
                    pos++;
                }
            }
        }

        private static boolean isPrintGakuseki(final Param param) {
            return !param._is133m || param._is133m && param._z010.in(Z010.sagaken);
        }

        /**
         * ページごとの年度リストのマップを得る。
         * @param student
         */
        private static Map<Integer, PrintGakuseki> getPageGakusekiListMap2(final Param param, final PersonalInfo pInfo, final int pageMaxGrade) {
            final boolean isPrintGakuseki = isPrintGakuseki(param);
            final boolean isDropNewPage = !param._z010.in(Z010.kyoto);
            final Map<Integer, PrintGakuseki> rtn = new TreeMap<Integer, PrintGakuseki>();
            if (!isPrintGakuseki) {
                rtn.put(1, PrintGakuseki.getPrintGakuseki(pInfo._gakusekiList));
            } else {
                final Map<Integer, List<Gakuseki>> pageGakusekiListMap = new TreeMap<Integer, List<Gakuseki>>();
                int page = 1;
                for (final Gakuseki gakuseki : pInfo._gakusekiList) {
                    if (!pInfo.isTargetYear("getPageGakusekiListMap2 : ", gakuseki._year, param)) {
                        continue;
                    }
                    List<Gakuseki> gakusekiList = getMappedList(pageGakusekiListMap, page);
                    if (isDropNewPage && gakuseki._isDrop && !pInfo._abroadPrintDropRegdYears.contains(gakuseki._year) || pageMaxGrade > 0 && gakusekiList.size() >= pageMaxGrade) {
                        page += 1; // 改ページ
                        gakusekiList = getMappedList(pageGakusekiListMap, page);
                    }
                    gakusekiList.add(gakuseki);
                }
                for (final Map.Entry<Integer, List<Gakuseki>> e : pageGakusekiListMap.entrySet()) {
                    rtn.put(e.getKey(), PrintGakuseki.getPrintGakuseki(e.getValue()));
                }
            }
            return rtn;
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param student
         */
        private void printPersonalInfo2(final Student student, final PersonalInfo pInfo) {
            if (param()._z010.in(Z010.miyagiken)) {
                svfVrsOut("GRADENAME1", pInfo._title);
            } else {
                svfVrsOut("GRADENAME", pInfo._title);
            }

            if (_name._maxnum > 0) {
                if (param()._z010.in(Z010.naraken)) {
                    printName1(null, PersonalInfo.HistVal.of(pInfo.getPrintName1(), pInfo._studentNameHistFirst), null, _name);
                } else {
                    printName(pInfo, _name);
                }
            }

            printSchoolName(student);

            if (param()._z010.in(Z010.meiji)) {
                svfVrsOut("TOTAL_STUDY_NAME", "Catholic Spirit");
            }
        }

        private Map<String, String> sogaku(final String defaultCredit, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final Map<StudyRec.CreditKind, List<BigDecimal>> subject90s, final List<Map<StudyRec.CreditKind, BigDecimal>> subject90sBunkatsu) {
            final TreeSet<String> yearSet = Gakuseki.gakusekiYearSet(gakusekiList);
//          final int minYear = PersonalInfo.gakusekiMinYear(gakusekiList);
            final String yearMin;
            final String yearMax;
            if (yearSet.isEmpty()) {
                yearMin = null;
                yearMax = null;
            } else {
                yearMin = yearSet.first();
                yearMax = yearSet.last();
            }

            String substitutionZenbuBiko = param().isNotPrintDaitai("1ura") ? "" : pInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, GakushuBiko.DAITAI_TYPE.ZENBU, yearMin, yearMax).toString();
            String substitutionIchibuBiko = param().isNotPrintDaitai("1ura") ? "" : pInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, GakushuBiko.DAITAI_TYPE.ICHIBU, yearMin, yearMax).toString();
            String creditstime = null;
            final boolean subject90sHasCompCredit = !getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT).isEmpty() && Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT)).intValue() > 0;
            boolean setCreditstime = false;
            if (param()._optionCreditOutputYoshiki1Ura == OptionCredit.YOSHIKI1_URA_2) {
                final List<StudyRecSubstitution.SubstitutionAttendSubclass> attendSubclassList = pInfo._gakushuBiko.getStudyRecBikoSubstitutionAttendSubclass(_90);
                boolean attendMirishu = false;
                boolean attendRishuNomi = false;
                for (final StudyRecSubstitution.SubstitutionAttendSubclass sas : attendSubclassList) {
                    log.debug(" attend = " + sas._attendSubclassMst.getKey(param()) + " : " + sas._attendValuation + ", " + sas._attendCredit + ", " + sas._attendCompCredit);
                    if (null == sas._attendValuation && null != sas._attendCredit && sas._attendCredit.intValue() == 0 && null != sas._attendCompCredit && sas._attendCompCredit.intValue() == 0) {
                        // 未履修
                        attendMirishu = true;
                    } else if (null != sas._attendValuation && null != sas._attendCredit && sas._attendCredit.intValue() == 0 && null != sas._attendCompCredit && sas._attendCompCredit.intValue() > 0) {
                        // 履修のみ
                        attendRishuNomi = true;
                    }
                }
                if (attendMirishu) {
                    creditstime = null; // 空欄（備考無し）
                    substitutionZenbuBiko = null;
                    substitutionIchibuBiko = null;
                    setCreditstime = true;
                } else if (attendRishuNomi) {
                    creditstime = "0"; // 0表示（備考無し）
                    substitutionZenbuBiko = null;
                    substitutionIchibuBiko = null;
                    setCreditstime = true;
                }
            }
            if (false == setCreditstime) {
                if (param()._isPrintYoshiki1UraBunkatuRishu) {
                    creditstime = getBunkatuRishuCreditStr(subject90sBunkatsu, defaultCredit);
                } else if (!getMappedList(subject90s, StudyRec.CreditKind.CREDIT).isEmpty()) {
                    if (Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.CREDIT)).intValue() == 0 && subject90sHasCompCredit && !param()._isNotUseCompCreditYoshiki1Ura) {
                        creditstime = kakko(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT)));
                    } else {
                        creditstime = String.valueOf(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.CREDIT)));
                    }
                } else { // if (getMappedList(subject90s, CREDIT).isEmpty())) {
                    if (subject90sHasCompCredit && !param()._isNotUseCompCreditYoshiki1Ura) {
                        creditstime = kakko(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT)));
                    } else {
                        creditstime = defaultCredit;
                    }
                }
            }

            final Map<String, String> rtn = new HashMap<String, String>();
            rtn.put("creditstime", creditstime);
            rtn.put("substitutionZenbuBiko", substitutionZenbuBiko);
            rtn.put("substitutionIchibuBiko", substitutionIchibuBiko);


            return rtn;
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits2(final Student student, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final List<StudyRecSubclassTotal> studyRecSubclassTotalList, final List<List<String>> csvLines) {

            final Tuple<Map<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>>, List<Map<StudyRec.CreditKind, BigDecimal>>> creditsSubject90sBunkatsu = setCreditsMap(pInfo, gakusekiList, studyRecSubclassTotalList);
            final Map<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>> credits = creditsSubject90sBunkatsu._first;

            final String fieldabroad = param()._z010.in(Z010.tokiwa) ? "GET_ABROAD" : "ABROAD";
            if (param()._z010.in(Z010.miyagiken)) {
                final String creditsabroad = defstr(Util.bdSum(getMappedList(Util.getMappedHashMap(credits, StudyRec.KIND.ABROAD), StudyRec.CreditKind.CREDIT)), param()._creditsDefaultAbroad);
                if (null != csvLines) {
                    final List<String> line = CsvUtils.newLine(csvLines);
                    line.addAll(Arrays.asList("", "留学", "", creditsabroad));
                } else {
                    svfVrsOut("ABROAD_NAME", "留学");
                    svfVrsOut(fieldabroad, creditsabroad);
                    svfVrEndRecord();
                }
            }

            final Map<StudyRec.CreditKind, List<BigDecimal>> subject90s = Util.getMappedHashMap(credits, StudyRec.KIND.SOGO90);

            final boolean isShowCredit0 = student.isShowCredit0(param(), pInfo, pInfo.getLastYear());

            {
                final List<Map<StudyRec.CreditKind, BigDecimal>> subject90sBunkatsu = creditsSubject90sBunkatsu._second;

                final String creditsTime = printSogakuCredit(pInfo, gakusekiList, subject90sBunkatsu, subject90s, isShowCredit0);
                if (null != csvLines) {
                    final List<String> line = CsvUtils.newLine(csvLines);
                    line.addAll(Arrays.asList("", pInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(gakusekiList)), "", creditsTime));
                } else {
                    svfVrsOut("FOOTER_SUBCLASSNAME", pInfo.getSogoSubclassname(param(), Gakuseki.getYearGakusekiMap(gakusekiList)));
                    if (_isLastsRecord) {
                        svfVrEndRecord();
                    }
                }
            }

            // 総学以外
            if (_isLastsRecord) {
                final String field2 = "footer_credit";
                final String subclassnameField = "FOOTER_SUBCLASSNAME";
                for (final Tuple<String, String> titleCredit : getLastCreditList(pInfo, credits)) {
                    final String title = titleCredit._first;
                    final String credit = titleCredit._second;
                    if (null != csvLines) {
                        final List<String> line = CsvUtils.newLine(csvLines);
                        line.addAll(Arrays.asList("", title, "", credit));
                    } else {
                        svfVrsOut(subclassnameField, title);
                        svfVrsOut(field2, credit);
                        svfVrEndRecord();
                    }
                }
            } else {

                final String fieldsubtotal     = param()._z010.in(Z010.tokiwa) ? "GET_SUBTOTAL" : "SUBTOTAL";
                final String fieldtotal        = param()._z010.in(Z010.tokiwa) ? "GET_TOTAL" :"TOTAL";
                svfVrsOut(fieldsubtotal, "");
                svfVrsOut(fieldabroad, "");
                svfVrsOut(fieldtotal, "");
                final String fieldhr           = param()._z010.in(Z010.tokiwa) ? "GET_HR" : null;
                final String fieldtimeComp     = param()._z010.in(Z010.tokiwa) ? "COMP_time" : null;
                final String fieldsubtotalComp = param()._z010.in(Z010.tokiwa) ? "COMP_SUBTOTAL" : null;
                final String fieldtotalComp    = param()._z010.in(Z010.tokiwa) ? "COMP_TOTAL" : null;
                final String fieldhrComp       = param()._z010.in(Z010.tokiwa) ? "COMP_HR" : null;
                svfVrsOut(fieldhr, "");
                svfVrsOut(fieldtimeComp, "");
                svfVrsOut(fieldsubtotalComp, "");
                svfVrsOut(fieldtotalComp, "");
                svfVrsOut(fieldhrComp, "");

                final Map<StudyRec.CreditKind, List<BigDecimal>> subjects = Util.getMappedHashMap(credits, StudyRec.KIND.SYOKEI);
                svfVrsOut(fieldsubtotal, defstr(Util.bdSum(getMappedList(subjects, StudyRec.CreditKind.CREDIT)), isShowCredit0 ? "0" : null));

                final String creditsabroad = defstr(Util.bdSum(getMappedList(Util.getMappedHashMap(credits, StudyRec.KIND.ABROAD), StudyRec.CreditKind.CREDIT)), param()._creditsDefaultAbroad);
                svfVrsOut(fieldabroad, creditsabroad);

                final Map<StudyRec.CreditKind, List<BigDecimal>> totals = Util.getMappedHashMap(credits, StudyRec.KIND.TOTAL);
                svfVrsOut(fieldtotal, defstr(Util.bdSum(getMappedList(totals, StudyRec.CreditKind.CREDIT)), isShowCredit0 ? "0" : param()._creditsDefaultTotal));

                final Map<StudyRec.CreditKind, List<BigDecimal>> subject94s = Util.getMappedHashMap(credits, StudyRec.KIND.SOGO94);
                svfVrsOut(fieldhr, defstr(Util.bdSum(getMappedList(subject94s, StudyRec.CreditKind.CREDIT)), isShowCredit0 ? "0" : null));

                if (param()._z010.in(Z010.tokiwa) && (pInfo.isTaigaku() || pInfo.isTengaku())) {
                    svfVrsOut(fieldtimeComp,     nullzero(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT))) + SLASH + nullzero(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.CREDIT_MSTCREDIT))));
                    svfVrsOut(fieldsubtotalComp, nullzero(Util.bdSum(getMappedList(subjects,   StudyRec.CreditKind.COMP_CREDIT))) + SLASH + nullzero(Util.bdSum(getMappedList(subjects,   StudyRec.CreditKind.CREDIT_MSTCREDIT))));
                    svfVrsOut(fieldtotalComp,    nullzero(Util.bdSum(getMappedList(totals,     StudyRec.CreditKind.COMP_CREDIT))) + SLASH + nullzero(Util.bdSum(getMappedList(totals,     StudyRec.CreditKind.CREDIT_MSTCREDIT))));
                    svfVrsOut(fieldhrComp,       nullzero(Util.bdSum(getMappedList(subject94s, StudyRec.CreditKind.COMP_CREDIT))) + SLASH + nullzero(Util.bdSum(getMappedList(subject94s, StudyRec.CreditKind.CREDIT_MSTCREDIT))));
                } else if (!param()._isNotUseCompCreditYoshiki1Ura) {
                    svfVrsOut(fieldtimeComp,     defstr(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT)), null));
                    svfVrsOut(fieldsubtotalComp, defstr(Util.bdSum(getMappedList(subjects,   StudyRec.CreditKind.COMP_CREDIT)), null));
                    svfVrsOut(fieldtotalComp,    defstr(Util.bdSum(getMappedList(totals,     StudyRec.CreditKind.COMP_CREDIT)), null));
                    svfVrsOut(fieldhrComp,       defstr(Util.bdSum(getMappedList(subject94s, StudyRec.CreditKind.COMP_CREDIT)), null));
                }
            }
        }

        private String printSogakuCredit(final PersonalInfo pInfo, final List<Gakuseki> gakusekiList,
                final List<Map<StudyRec.CreditKind, BigDecimal>> subject90sBunkatsu,
                final Map<StudyRec.CreditKind, List<BigDecimal>> subject90s, final boolean isShowCredit0) {
            final Map<String, String> sogaku = sogaku(isShowCredit0 ? "0" : param()._creditsDefaultSogaku, pInfo, gakusekiList, subject90s, subject90sBunkatsu);
            final String creditstime = KnjDbUtils.getString(sogaku, "creditstime");
            final String substitutionZenbuBiko = KnjDbUtils.getString(sogaku, "substitutionZenbuBiko");
            final String substitutionIchibuBiko = KnjDbUtils.getString(sogaku, "substitutionIchibuBiko");

            final boolean hasZenbuDaitaiBikoField = hasField("CREDIT4") && hasField("CREDIT5");
            if (hasZenbuDaitaiBikoField) {
                svfVrsOut("CREDIT4", "");
                svfVrsOut("CREDIT5", "");
            }

            final boolean hasIchibuDaitaiBikoField = hasField("CREDIT6_1") && hasField("CREDIT6_2") && hasField("CREDIT6_3");
            if (hasIchibuDaitaiBikoField) {
                svfVrsOut("CREDIT6_1", "");
                svfVrsOut("CREDIT6_2", "");
                svfVrsOut("CREDIT6_3", "");
            }

            String data = null;
            boolean print = false;
            if (hasZenbuDaitaiBikoField && !StringUtils.isBlank(substitutionZenbuBiko)) {
                svfVrsOutSplit(new String[] {"CREDIT4", "CREDIT5"}, substitutionZenbuBiko);
                data = substitutionZenbuBiko;
                print = true;
            } else if (hasIchibuDaitaiBikoField && !StringUtils.isBlank(substitutionIchibuBiko)) {
                svfVrsOut("CREDIT6_1", defstr(Util.bdSum(getMappedList(subject90s, StudyRec.CreditKind.COMP_CREDIT)), ""));
                svfVrsOutSplit(new String[] {"CREDIT6_2", "CREDIT6_3"}, substitutionIchibuBiko);
                data = substitutionIchibuBiko;
                print = true;
            }

            final String fieldtime;
            if (_isLastsRecord) {
                fieldtime = "footer_credit";
            } else {
                fieldtime = param()._z010.in(Z010.tokiwa) ? "GET_time" : "time";
                svfVrsOut(fieldtime, "");
            }
            if (!print) {
                data = creditstime;
                if (!NumberUtils.isDigits(creditstime)) {
                    // ()付の場合等は編集式カット、中央割付
                    svfVrAttribute(fieldtime, "Edit=,Hensyu=3");
                }
                svfVrsOut(fieldtime, creditstime);
            }
            return data;
        }

        private List<Tuple<String, String>> getLastCreditList(final PersonalInfo pInfo, final Map<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>> credits) {
            final List<Tuple<String, String>> creditsList = new ArrayList<Tuple<String, String>>();
            final Map<StudyRec.CreditKind, List<BigDecimal>> jiritsus = Util.getMappedHashMap(credits, StudyRec.KIND.JIRITSU);
            if (!jiritsus.isEmpty() || pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                creditsList.add(Tuple.of("自   立   活   動", defstr(Util.bdSum(getMappedList(jiritsus, StudyRec.CreditKind.CREDIT)))));
            }
            if (param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.mieken) || _isKenja) {
                creditsList.add(Tuple.of("小　　　　　　　計", defstr(Util.bdSum(getMappedList(Util.getMappedHashMap(credits, StudyRec.KIND.SYOKEI), StudyRec.CreditKind.CREDIT)), null)));
            }

            if (param()._z010.in(Z010.miyagiken)) {
                // 宮城県は留学は専用のフィールドはなし
            } else {
                creditsList.add(Tuple.of("留　　　　　　　学", defstr(Util.bdSum(getMappedList(Util.getMappedHashMap(credits, StudyRec.KIND.ABROAD), StudyRec.CreditKind.CREDIT)), param()._creditsDefaultAbroad)));
            }

            if (param()._z010.in(Z010.kyoto) || param()._z010.in(Z010.miyagiken) || param()._z010.in(Z010.mieken) || _isKenja) {
                creditsList.add(Tuple.of("修得単位数の合計", defstr(Util.bdSum(getMappedList(Util.getMappedHashMap(credits, StudyRec.KIND.TOTAL), StudyRec.CreditKind.CREDIT)), param()._creditsDefaultTotal)));
            }
            return creditsList;
        }

        private Tuple<Map<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>>, List<Map<StudyRec.CreditKind, BigDecimal>>> setCreditsMap(final PersonalInfo pInfo, final List<Gakuseki> gakusekiList, final List<StudyRecSubclassTotal> studyRecSubclassTotalList) {
            final Map<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>> credits = new HashMap<StudyRec.KIND, Map<StudyRec.CreditKind, List<BigDecimal>>>();
            final List<Map<StudyRec.CreditKind, BigDecimal>> subject90sBunkatsu = new ArrayList<Map<StudyRec.CreditKind, BigDecimal>>();
            for (final StudyRecSubclassTotal sst : studyRecSubclassTotalList) {

                final List<BigDecimal> creditList = sst.kindListForTotal(param(), StudyRec.CreditKind.CREDIT, null);
                final List<BigDecimal> compCreditList = sst.compCreditListForTotal(param(), null);
                final List<BigDecimal> creditMstCreditList = sst.creditMstCreditListForTotal(param(), null);
                final List<Map<StudyRec.CreditKind, BigDecimal>> bunkatuRishuCreditList = sst.bunkatuRishuCreditMapListForTotal(param());
                final List<StudyRec.KIND> kindList = sst.studyrec().kindList(param(), pInfo);
                StudyRec.KIND kind = null;
                kind = StudyRec.KIND.SOGO90;
                if (kindList.contains(kind)) {
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.COMP_CREDIT).addAll(compCreditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT_MSTCREDIT).addAll(creditMstCreditList);
                    subject90sBunkatsu.addAll(bunkatuRishuCreditList);
                }
                kind = StudyRec.KIND.JIRITSU;
                if (kindList.contains(kind)) {
                    if (StudyRecSubclassTotal.isEnabledJiritsu(param(), sst)) {
                        getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList);
                        getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.COMP_CREDIT).addAll(compCreditList);
                        getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT_MSTCREDIT).addAll(creditMstCreditList);
                    }
                }
                kind = StudyRec.KIND.SYOKEI;
                if (kindList.contains(kind)) {
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.COMP_CREDIT).addAll(compCreditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT_MSTCREDIT).addAll(creditMstCreditList);
                }
                kind = StudyRec.KIND.ABROAD;
                if (kindList.contains(kind)) {
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList); // 留学は修得単位のみ
                }
                kind = StudyRec.KIND.TOTAL;
                if (kindList.contains(kind)) {
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.COMP_CREDIT).addAll(compCreditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT_MSTCREDIT).addAll(creditMstCreditList);
                }
                kind = StudyRec.KIND.SOGO94;
                if (kindList.contains(kind)) {
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT).addAll(creditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.COMP_CREDIT).addAll(compCreditList);
                    getMappedList(Util.getMappedHashMap(credits, kind), StudyRec.CreditKind.CREDIT_MSTCREDIT).addAll(creditMstCreditList);
                }
            }
            return Tuple.of(credits, subject90sBunkatsu);
        }

        private String getBunkatuRishuCreditStr(final List<Map<StudyRec.CreditKind, BigDecimal>> bunkatuRishuCreditMapList, final String defaultCredit) {
            final Map<StudyRec.CreditKind, List<BigDecimal>> total = new HashMap<StudyRec.CreditKind, List<BigDecimal>>();
            for (final Map<StudyRec.CreditKind, BigDecimal> map : bunkatuRishuCreditMapList) {
                for (final StudyRec.CreditKind key : map.keySet()) {
                    final BigDecimal value = map.get(key);
                    getMappedList(total, key).add(value);
                }
            }
            if (param()._isOutputDebug || param()._isOutputDebugSeiseki) {
                if (total.size() > 1) {
                    log.info(" bunkatu " + bunkatuRishuCreditMapList + " => " + total);
                }
            }
            final BigDecimal credit = Util.bdSum(getMappedList(total, StudyRec.CreditKind.CREDIT));
            final BigDecimal compCredit = Util.bdSum(getMappedList(total, StudyRec.CreditKind.COMP_CREDIT));
            final String creditstime;
            if (null != credit && null != compCredit) {
                creditstime = credit.toString() + kakko(compCredit);
            } else if (null != credit) {
                creditstime = credit.toString();
            } else if (null != compCredit) {
                creditstime = kakko(compCredit);
            } else { // null == credit && null == compCredit
                creditstime = defaultCredit;
            }
            return creditstime;
        }

        /**
         * 科目名の文字数により文字ピッチ及びＹ軸を変更します。（SVF-FORMのフィールド属性変更）
         *
         * @param subclassname:科目名
         * @param line:出力行(通算)
         */
        private void svfFieldAttribute2(final String fieldname, final String subclassname, final int line, final PersonalInfo pInfo) {
            if (param()._isCsv) {
                return;
            }
            int ln = line + 1;
            final int width;
            final int height;
            final int ystart;
            final int x;
            float size;
            final int jiku;
            if (param()._is133m) {
                final int gap1 = 11;
                final int x1, y1;
                int x2 = 1082;
                if (param()._z010.in(Z010.tokyoto)) {
                    x1 = 550;
                    y1 = 1022;
                } else if (param()._z010.in(Z010.mieken)) {
                    x1 = 482;
                    y1 = 1042;
                } else if (param()._z010.in(Z010.kyoto)) {
                    x1 = 472;
                    y1 = 1042;
                } else if (param()._z010.in(Z010.sagaken)) {
                    x1 = _form._formInfo.getFieldX(fieldname, 314);
                    x2 = 970;
                    y1 = 1114;
                } else {
                    x1 = _form._formInfo.getFieldX(fieldname, 482);
                    y1 = 1092;
                }
                svfobj.width = x2 - x1 - gap1 - 10; // フィールドの幅(ドット)
                svfobj.height = y1 - 944; // フィールドの高さ(ドット)
                svfobj.ystart = 944 - svfobj.height; // 開始位置(ドット)
                svfobj.minnum = 20; // 最小設定文字数
                svfobj.maxnum = _form._formInfo.getFieldLength(fieldname, 40); // 最大設定文字数
                final int gap = x1 - 314 + gap1;

                final int linex;
                if (ln <= MAX_LINE1) {
                    // 左列の開始Ｘ軸
                    linex = 314;
                } else if (ln <= MAX_LINE2) {
                    // 中列の開始Ｘ軸
                    linex = 1338;
                } else {
                    // 右列の開始Ｘ軸
                    linex = 2362;
                }
                x = linex + gap;
            } else {
                final int namegap = 21;
                if (param()._z010.in(Z010.tokiwa) && isNewForm(param(), pInfo)) {
                    width = 574;
                    height = 92;
                    ystart = 852;
                } else if (param()._z010.in(Z010.tosa) || param()._z010.in(Z010.fukuiken)) {
                    width = 1062 - 392 - namegap;
                    height = 136;
                    if (param()._z010.in(Z010.fukuiken)) {
                        ystart = 632;
                    } else {
                        ystart = 848;
                    }
                } else if (param()._z010.in(Z010.chiyodaKudan)) {
                    width = 1082 - 550;
                    height = 78;
                    ystart = 866;
                } else if (param()._z010.in(Z010.meikei) && "1".equals(param()._ibCourse)) {
                    width = 1702 - 472;
                    height = 69;
                    ystart = 766 - height;
                } else {
                    width = 576;
                    height = 98;
                    ystart = 846;
                }
                svfobj.width = width; // フィールドの幅(ドット)
                svfobj.height = height; // フィールドの高さ(ドット)
                svfobj.ystart = ystart; // 開始位置(ドット)
                svfobj.minnum = 20; // 最小設定文字数
                svfobj.maxnum = 40; // 最大設定文字数

                int linex;
                if (ln <= MAX_LINE1 || param()._z010.in(Z010.meikei) && "1".equals(param()._ibCourse)) {
                    final int x1;
                    // 左列の開始Ｘ軸
                    if (param()._z010.in(Z010.chiyodaKudan)) {
                        x1 = 550;
                    } else if (param()._z010.in(Z010.tosa) || param()._z010.in(Z010.fukuiken)) {
                        x1 = 392;
                    } else {
                        x1 = 472;
                    }
                    linex = x1;
                } else if (ln <= MAX_LINE2) {
                    final int x2;
                    // 中列の開始Ｘ軸
                    if (param()._z010.in(Z010.tokiwa) && isNewForm(param(), pInfo)) {
                        x2 = 2002;
                    } else if (param()._z010.in(Z010.tosa, Z010.fukuiken)) {
                        x2= 392 + 1160;
                    } else if (param()._z010.in(Z010.chiyodaKudan)) {
                        x2 = 1574;
                    } else {
                        x2 = 472 + 1024;
                    }
                    linex = x2;
                } else {
                    final int x3;
                    // 右列の開始Ｘ軸
                    if (param()._z010.in(Z010.tosa, Z010.fukuiken)) {
                        x3 = 392 + 1160 * 2;
                    } else if (param()._z010.in(Z010.chiyodaKudan)) {
                        x3 = 2598;
                    } else {
                        x3 = 472 + 1024 * 2;
                    }
                    linex = x3;
                }
                x = linex + namegap;
            }
            final int hnum = (ln % MAX_LINE1 == 0) ? MAX_LINE1 : ln % MAX_LINE1;
            int num = Math.min(svfobj.maxnum, Math.max(svfobj.minnum, getMS932ByteLength(subclassname)));          //文字数(BYTE)
            num += (num % 2 != 0) ? 1 : 0; // 半角等を含んでバイト数が奇数の場合、+1しておく。
            if (param()._is133m) {
                size = (float) KNJSvfFieldModify.retFieldPoint(svfobj.width, num);                  //文字サイズ
                jiku = (int) KNJSvfFieldModify.retFieldY(svfobj.height, size) + svfobj.ystart + svfobj.height * hnum;  //出力位置＋Ｙ軸の移動幅
            } else {
                size = (float) Math.floor(KNJSvfFieldModify.pixelToCharPoint(Math.min((int) ((float) svfobj.width / (num / 2)), svfobj.height)));
                jiku = (int) Math.round(((double) svfobj.height - (KNJSvfFieldModify.charPointToPixel("fieldAttribute " + fieldname, size, 0))) / 2) + svfobj.ystart + svfobj.height * hnum;  //出力位置＋Ｙ軸の移動幅
            }
            //log.info(" x = " + x + ", y = " + jiku + ", size = " + size);
            svfVrAttribute(fieldname, "X=" + x);
            svfVrAttribute(fieldname, "Y=" + jiku); // 開始Ｙ軸
            svfVrAttribute(fieldname, "Size=" + size); // 文字サイズ
        }

        private static PrintLine newLine(final int linex, final List<PrintLine> printLineList) {
            final PrintLine printLine = new PrintLine();
            printLine._linex = linex;
            printLineList.add(printLine);
            return printLine;
        }

        private static class PrintLine {
            int _linex;
            String _eduDiv;
            String _eduDiv2;
            String _classname;
            String _classname2;
            String _classname2Field;
            String _subclassname;
            String _credit;
            String _compCredit;
            String _biko;
            String _debugcomment;
            PrintLine() {
                _classname2Field = "CLASSNAME2";
            }
        }

        private List<StudyrecTotalSpecialDiv> getStudyrecTotalSpecialDivList2(final Param param, final Student student, final PersonalInfo pInfo, final List<StudyRecSubclassTotal> studyRecSubclassTotalList) {
            final List<StudyrecTotalSpecialDiv> rtn = new ArrayList<StudyrecTotalSpecialDiv>();
            for (final StudyRecSubclassTotal studyrectotal : studyRecSubclassTotalList) {
                if (param._is133m) {
                    if (param()._z010.in(Z010.miyagiken) && studyrectotal.isMishutokuM(param())) {
                        // 宮城県は未修得の場合様式1裏に表示しない
                        continue;
                    }
                } else {
                    if (param()._optionCreditOutputYoshiki1Ura == OptionCredit.YOSHIKI1_URA_1) {
                        if (!isNewForm(param(), pInfo) && studyrectotal.isRishuNomi(param())) {
                            // 京都府は平成24年度以前で履修のみの場合様式1裏に表示しない
                            continue;
                        } else if (studyrectotal.isMirishu(param())) {
                            // 京都府は単位不認定（未履修）の場合様式1裏に表示しない
                            continue;
                        }
                    }
                }
                StudyrecTotalSpecialDiv stsd = getStudyrecTotalSpecialDiv(param, _yoshiki, studyrectotal.classMst(param(), _yoshiki)._specialDiv, rtn);
                StudyrecTotalClass stc = getStudyrecTotalClass(param(), studyrectotal.classMst(param(), _yoshiki), stsd._classes);
                StudyrecTotalSubclass sts = getStudyrecTotalSubclass(param(), studyrectotal.subclassMst(param(), _yoshiki), stc._subclasses);
                sts._totals.add(studyrectotal);
            }
            if (param._isOutputDebugSeiseki) {
                log.info(" output seiseki count = " + rtn.size());
                for (int i = 0; i < rtn.size(); i++) {
                    final StudyrecTotalSpecialDiv stsd = rtn.get(i);
                    log.info(" spcialDiv " + i + " = " + stsd.first().studyrec()._classMst._specialDiv);
                    for (int j = 0; j < stsd._classes.size(); j++) {
                        final StudyrecTotalClass stc = stsd._classes.get(j);
                        log.info("  class " + j + " = " + stc.first().studyrec()._classMst);
                        for (int k = 0; k < stc._subclasses.size(); k++) {
                            final StudyrecTotalSubclass sts = stc._subclasses.get(k);
                            log.info("   subclass " + k + " = " + sts.first().studyrec()._subclassMst + ", credit = " + sts._totals);
                        }
                    }
                }
            }
            return rtn;
        }

        private static StudyrecTotalSubclass getStudyrecTotalSubclass(final Param param, final SubclassMst subclassMst, final List<StudyrecTotalSubclass> studyRecTotalSubclassList) {
            StudyrecTotalSubclass rtn = null;
            for (final StudyrecTotalSubclass sts : studyRecTotalSubclassList) {
                if (SubclassMst.isSameKey(param, sts.first().studyrec()._subclassMst, subclassMst)) {
                    rtn = sts;
                    break;
                }
            }
            if (null == rtn) {
                rtn = new StudyrecTotalSubclass();
                studyRecTotalSubclassList.add(rtn);
            }
            return rtn;
        }

        private static StudyrecTotalClass getStudyrecTotalClass(final Param param, final ClassMst classMst2, final List<StudyrecTotalClass> studyRecTotalClassList) {
            StudyrecTotalClass rtn = null;
            for (final StudyrecTotalClass stc : studyRecTotalClassList) {
                if (ClassMst.isSameKey(param, stc.first().studyrec()._classMst, classMst2)) {
                    rtn = stc;
                    break;
                }
            }
            if (null == rtn) {
                rtn = new StudyrecTotalClass();
                studyRecTotalClassList.add(rtn);
            }
            return rtn;
        }

        private static StudyrecTotalSpecialDiv getStudyrecTotalSpecialDiv(final Param param, final YOSHIKI yoshiki, final String specialDiv, final List<StudyrecTotalSpecialDiv> studyRecTotalSpecialDivList) {
            StudyrecTotalSpecialDiv rtn = null;
            for (final StudyrecTotalSpecialDiv stc : studyRecTotalSpecialDivList) {
                if (param._notUseClassMstSpecialDiv || stc.first().classMst(param, yoshiki)._specialDiv.equals(specialDiv)) {
                    rtn = stc;
                    break;
                }
            }
            if (null == rtn) {
                rtn = new StudyrecTotalSpecialDiv();
                studyRecTotalSpecialDivList.add(rtn);
            }
            return rtn;
        }

        private static class StudyrecTotalSubclass {
            final List<StudyRecSubclassTotal> _totals = new ArrayList<StudyRecSubclassTotal>();
            /** データがすべて非対象か */
            public boolean isAllNotTarget(final Param param) {
                if (param._is133m) {
                    return false;
                }
                boolean isAllDropped = true;
                for (final StudyRecSubclassTotal studyrecSubclassTotal : _totals) {
                    if (!studyrecSubclassTotal.isAllNotTarget()) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
            private StudyRecSubclassTotal first() {
                return _totals.get(0);
            }
            public String toString() {
                return "TotalSubclass(" + _totals + ")";
            }
        }

        private static class StudyrecTotalClass {
            final List<StudyrecTotalSubclass> _subclasses = new ArrayList<StudyrecTotalSubclass>();
            private StudyRecSubclassTotal first() {
                return _subclasses.get(0).first();
            }
            /** データがすべて非対象か */
            public boolean isAllNotTarget(final Param param) {
                if (param._is133m) {
                    return false;
                }
                boolean isAllDropped = true;
                for (final StudyrecTotalSubclass studyrectotalSubclass : _subclasses) {
                    if (!studyrectotalSubclass.isAllNotTarget(param)) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
        }

        private static class StudyrecTotalSpecialDiv {
            final List<StudyrecTotalClass> _classes = new ArrayList<StudyrecTotalClass>();
            /** データがすべて非対象か */
            public boolean isAllNotTarget(final Param param) {
                if (param._is133m) {
                    return false;
                }
                boolean isAllDropped = true;
                for (final StudyrecTotalClass studyrectotalClass : _classes) {
                    if (!studyrectotalClass.isAllNotTarget(param)) {
                        isAllDropped = false;
                    }
                }
                return isAllDropped;
            }
            private StudyRecSubclassTotal first() {
                return _classes.get(0).first();
            }
        }

        private void svfVrEndRecordM(final String fieldEduDiv2, final String specialDiv, final Student student, final PersonalInfo pInfo) {
            if (!param()._z010.in(Z010.sagaken)) {
                printTotalCredits2M(student, pInfo, pInfo._gakusekiList);
            }
            svfVrsOut(fieldEduDiv2, specialDiv);
            svfVrEndRecord();
        }

        private void printName2M(final Student student, final PersonalInfo pInfo) {
            svfVrsOut("GRADENAME", pInfo._title);
            if (param()._z010.in(Z010.miyagiken)) {
                svfVrsOut("ATTENDNO_1", student._schregno);
            } else if (param()._z010.in(Z010.sagaken)) {
                svfVrsOut("SCHREGNO", student._schregno);
            }
            _name._param = param();
            printName(pInfo, _name);
            if (param()._z010.in(Z010.tokyoto)) {
                final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(student._htrainRemarkHdat_2, "CREDITREMARK"), 88, 5);
                if (null != token) {
                    for (int j = 0; j < token.length; j++) {
                        svfVrsOutn("REMARK", j + 1, token[j]);
                    }
                }
            } else if (param()._z010.in(Z010.sagaken)) {
                svfVrsOut("FOOTER1", "【備考】");
                svfVrsOut("FOOTER2", "　　１　Ａ欄は、転編入学生が前籍校で取得した単位数又は留学で取得した単位数。");
                svfVrsOut("FOOTER3", "　　２　Ｂ欄は、本校の教育課程に沿って取得した単位数（技能連携及び高等学校卒業程度認定試験により修得を認められた単位を含む）。");
            }
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits2M(final Student student, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList) {
            final TreeSet<String> yearSet = Gakuseki.gakusekiYearSet(gakusekiList);
            if (yearSet.isEmpty()) {
                return;
            }
            final String yearMin = yearSet.first();
            final String yearMax = yearSet.last();

            final List<StudyRec.TotalM> totalMs = Arrays.asList(StudyRec.TotalM.SUBJECT90, StudyRec.TotalM.SUBJECT, StudyRec.TotalM.ABROAD, StudyRec.TotalM.TOTAL);
            final Map<StudyRec.TotalM, List<BigDecimal>> totalMCreditListMap = new TreeMap<StudyRec.TotalM, List<BigDecimal>>();
            for (final StudyRecYearTotalM yearTotal : pInfo.getStudyRecYearM(param(), YOSHIKI._1_URA).values()) {
//              if (yearTotal._isDrop == StudyRecYearTotal.DROP || yearTotal._isDrop == StudyRecYearTotal.DROP_SHOW || !yearSet.contains(yearTotal._year)) {
//              continue;
//          }
                for (final StudyRec.TotalM totalM : totalMs) {
                    getMappedList(totalMCreditListMap, totalM).addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yearTotal.list(totalM), param(), YOSHIKI._1_URA));
                }
            }

            final String substitutionCredit = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, GakushuBiko.DAITAI_TYPE.NO_TYPE_FLG, yearMin, yearMax).toString();
            if (!StringUtils.isBlank(substitutionCredit)) {
                if (param()._z010.in(Z010.tokyoto)) {
                    if (getMS932ByteLength(substitutionCredit) <= 10 * 2) {
                        final String[] fields = {"CREDIT7_1", "CREDIT7_2"};
                        final String[] tokens = Util.get_token(param(), substitutionCredit, 10, 2);
                        for (int i = 0; i < tokens.length; i++) {
                            svfVrsOut(fields[i], tokens[i]);
                        }
                    } else { // if (getMS932ByteLength(substitutionCredit) <= 14 * 3) {
                        final String[] fields = {"CREDIT8_1", "CREDIT8_2", "CREDIT8_3"};
                        final String[] tokens = Util.get_token(param(), substitutionCredit, 14, 3);
                        for (int i = 0; i < tokens.length; i++) {
                            svfVrsOut(fields[i], tokens[i]);
                        }
                    }
                } else {
                    svfVrsOut("CREDIT4", substitutionCredit);
                }
            } else {
                svfVrsOut("time", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.SUBJECT90)), "0"));
            }
            svfVrsOut("SUBTOTAL", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.SUBJECT)), "0"));
            svfVrsOut("ABROAD", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.ABROAD)), param()._creditsDefaultAbroad));
            svfVrsOut(param()._z010.in(Z010.tokyoto) ? "GET" : "TOTAL", defstr(Util.bdSum(getMappedList(totalMCreditListMap, StudyRec.TotalM.TOTAL)), "0"));
        }
    }

    /**
     * 学習の記録。
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001 ・データがない場合の不具合修正 --NO001
     * ・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加 --NO003 => 無い場合は従来通りHR_CLASSを出力
     * ・学年をすべて出力 --NO005
     */
    private static class KNJA130_3 extends KNJA130_0 implements Page {

        private static String FORM_KNJA133M_3KUMA = "KNJA133M_3KUMA.frm";
        private static String FORM_KNJA133M_3 = "KNJA133M_3.frm";

        private int[] MAX_LINE = {65};

        private KNJSvfFieldInfo _name;

        private final YOSHIKI _yoshiki = YOSHIKI._2_OMOTE;

        // 複数行
        private static List<String> _remarkFukusugyoFormList = Arrays.asList("KNJA130C_3.frm");

        // ヘッダとフッタがレコード
        private static List<String> _headerFooterRecordFormList = Arrays.asList("KNJA130C_13FUKUI.frm", "KNJA130C_3FUKUI.frm", "KNJA130C_13.frm", "KNJA130C_3.frm");

        // フッタのみレコード
        private static List<String> _footerOnlyRecordFormList = Arrays.asList("KNJA130C_3MIYA.frm", "KNJA130C_3NARA.frm", "KNJA130C_3MIE.frm");

        private boolean _isHeaderInRecord3;
        private boolean _isFooterInRecord3;

        enum CreditTotalKey {
            yearSubject90sContainsRyunen,
            yearSubject90sValid,
            yearJiritsu,
            yearSubjects,
            yearAbroads,
            yearTotals,
            yearSubject94s,
            yearAny
        }

        KNJA130_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm3(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            boolean is3 = is3nenYou(pInfo);
            _name = null;
            final String form;
            if (null != printGakuseki && printGakuseki._isYoshiki2omote3KantenForm) {
                if (is3) {
                    form = "KNJA130C_13.frm";
                    _gradeLineMax = 3;
                } else {
                    form = "KNJA130C_3.frm";
                    _gradeLineMax = 4;
                }
            } else if (param()._z010.in(Z010.miyagiken)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_3MIYA.frm";
                } else {
                    //form =  is3 ? null : "KNJA130C_3MIYA.frm";
                    form =  "KNJA130C_3MIYA.frm";
                    _name = new KNJSvfFieldInfo(493, 1170, charSize11, 325, 295, 355, 24, 48);
                    //_gradeLineMax = is3 ? 3 : 4;
                    _gradeLineMax = 4;
                    _isPrintEduDiv2CharsPerLine = true;
                }
            } else if (param()._z010.in(Z010.mieken)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_3MIE.frm";
                } else {
                    form = "KNJA130C_3MIE.frm";
                    _gradeLineMax = 4;
                    _isPrintEduDiv2CharsPerLine = true;
                }
            } else if (param()._z010.in(Z010.kyoto) && param()._is133m) {
                _gradeLineMax = 6;
                form = "KNJA133M_3KYOTO.frm";
                _gradeLineMax = 4;
            } else if (param()._z010.in(Z010.tokiwa)) {
                if (isNewForm(param(), pInfo)) {
                    form =  is3 ? "KNJA130C_13_2TOKIWA.frm" : null;
                    MAX_LINE = new int[] {64};
                    _name = new KNJSvfFieldInfo(275, 1120, charSize11, 366, 338, 393, 24, 48);
                } else {
                    form =  is3 ? "KNJA130C_13TOKIWA.frm"  : null;
                    MAX_LINE = new int[] {38};
                    _name = new KNJSvfFieldInfo(520, 1830, charSize11, 406, -1, -1, 24, 48);
                }
                _gradeLineMax = 3;
            } else if (param()._z010.in(Z010.meiji)) {
                _name = new KNJSvfFieldInfo(421, 1155, charSize11, 325, 295, 355, 24, 48);
                form = is3 ? "KNJA130C_13.frm" : "KNJA130C_3MEIJI.frm";
                _gradeLineMax = is3 ? 3 : 4;
            } else if (param()._z010.in(Z010.tosa)) {
                form = "KNJA130C_13TOSA.frm";
                _gradeLineMax = 3;
            } else if (param()._z010.in(Z010.naraken)) {
                if (param()._is133m) {
                    form = "KNJA133M_3NARA.frm";
                    _gradeLineMax = 6;
                } else {
                    form = "KNJA130C_3NARA.frm";
                    _gradeLineMax = 4;
                }
            } else if (param()._z010.in(Z010.chiyodaKudan)) {
                form = "KNJA130_4KUDAN.frm";
                _gradeLineMax = 4;
                MAX_LINE = new int[] {55, 50};
            } else if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4TOKYO.frm";
                } else if (param()._z010.in(Z010.sagaken)) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_3SAGA.frm";
                    MAX_LINE = new int[] {60};
                } else if (KNJA130_0.isNewForm(param(), pInfo)) {
                    _gradeLineMax = 6;
                    form = FORM_KNJA133M_3KUMA;
                } else {
                    _gradeLineMax = 6;
                    form = FORM_KNJA133M_3;
                }
            } else if (param()._z010.in(Z010.fukuiken)) {
                form = is3 ? "KNJA130C_13FUKUI.frm" : "KNJA130C_3FUKUI.frm";
                _gradeLineMax = is3 ? 3 : 4;
            } else {
                if (is3 && param()._z010.in(Z010.kyoto)) {
                    if (null != printGakuseki && Gakuseki.containsDroppedAbroad(printGakuseki._yearGakusekiMap.values())) {
                        is3 = false;
                    }
                }
                form = is3 ? "KNJA130C_13.frm" : "KNJA130C_3.frm";
                _gradeLineMax = is3 ? 3 : 4;
            }
            return form;
        }

        final String FLG_3KANTEN_FORM = "FLG_3KANTEN_FORM";
        final String FLG_GRADE2_10KETA = "FLG_GRADE2_10KETA";
        final String FLG_AOYAMA_GRADE_HYOTEI = "FLG_AOYAMA_GRADE_HYOTEI";
        final String FLG_KWANSEI_HYOTEI = "FLG_KWANSEI_HYOTEI";
        private String modifyForm3(final String form, final Student student, final PersonalInfo pInfo, final PrintGakuseki pg) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if (pg._isYoshiki2omote3KantenForm) {
                flgMap.put(FLG_3KANTEN_FORM, "1");
            }
            for (int i = 1; i <= 8; i++) {
                if (_form._formInfo.hasField("GRADE2_" + String.valueOf(i))) {
                    if (!_form._formInfo.hasField("GRADE2_" + String.valueOf(i) + "_2")) {
                        flgMap.put(FLG_GRADE2_10KETA, "1");
                        break;
                    }
                }
            }
            if (param()._z010.in(Z010.aoyama)) {
                flgMap.put(FLG_AOYAMA_GRADE_HYOTEI, "1");
            }
            if ("print100".equals(param()._hyotei)) {
                flgMap.put(FLG_KWANSEI_HYOTEI, "1");
            }
            return modifyForm0(form, pInfo, pg, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_GRADE2_10KETA)) {
                final Map<String, SvfForm.Field> fieldNameMap = svfForm.getFieldNameMap();
                for (int i = 1; i <= 8; i++) {
                    final SvfForm.Field grade2 = fieldNameMap.get("GRADE2_" + String.valueOf(i));
                    if (null == grade2) {
                        break;
                    }
                    final String field10keta = "GRADE2_" + String.valueOf(i) + "_2";
                    final SvfForm.Field grade2_2 = fieldNameMap.get(field10keta);
                    if (null == grade2_2) {
                        final SvfForm.Field field = grade2.copyTo(field10keta).setFieldLength(10).setCharPoint10(30).setYokobai(2, 1).setTatebai(3, 1).setEndX(grade2._endX - 20).setX(grade2._position._x + 5);
                        if (param()._isOutputDebug) {
                            log.info(" add field : " + field);
                        }
                        svfForm.addField(field);
                    }
                }
            }
            if (flgMap.containsKey(FLG_AOYAMA_GRADE_HYOTEI)) {
                svfForm.setColor(true);
                final Map<String, SvfForm.Field> fieldNameMap = svfForm.getFieldNameMap();
                for (int i = 1; i <= 4; i++) {
                    for (final String name : Arrays.asList("GRADES", "CREDIT")) {
                        final String fieldname = name + String.valueOf(i);
                        final SvfForm.Field f = fieldNameMap.get(fieldname);
                        if (null != f) {
                            svfForm.addField(f.copyTo(fieldname + "_2").addX(-25).addY(6).setFieldLength(5).setCharPoint10(60));
                        }
                    }
                }
                for (int i = 1; i <= 5; i++) {
                    final String fieldname = "tani_" + String.valueOf(i);
                    final SvfForm.Field f = fieldNameMap.get(fieldname);
                    if (null != f) {
                        svfForm.addField(f.copyTo(fieldname + "_2").addX(i == 5 ? -5 : -20).addY(6).setFieldLength(5).setCharPoint10(60).setHenshuShiki("").setPrintMethod(SvfForm.Field.PrintMethod.MIGITSUME).setDataNumType(false));
                    }
                }
            }
            if (flgMap.containsKey(FLG_KWANSEI_HYOTEI)) {
                final Map<String, SvfForm.Field> fieldNameMap = svfForm.getFieldNameMap();
                for (int i = 1; i <= 4; i++) {
                    final String fieldname = "GRADES" + String.valueOf(i);
                    final SvfForm.Field f = fieldNameMap.get(fieldname);
                    if (null != f) {
                        svfForm.removeField(f);
                        svfForm.addField(f.addX(-15).setFieldLength(3));
                    }
                }
            }
            if (flgMap.containsKey(FLG_3KANTEN_FORM)) {
                for (int n = 1; n <= _gradeLineMax; n++) {
                    final SvfForm.Field fieldGrade2 = svfForm.getField("GRADE2_" + String.valueOf(n) + "_2");
                    if (null == fieldGrade2) {
                        continue;
                    }
                    final SvfForm.Line gradeLeftLine = svfForm.getNearestLeftLine(fieldGrade2.getPoint());
                    final SvfForm.Line gradeRightLine = svfForm.getNearestRightLine(fieldGrade2.getPoint());
                    final int width = gradeRightLine.getPoint()._x - gradeLeftLine.getPoint()._x;

                    final int rateKanten = 6;
                    final int rateHyotei = 5;
                    final int rateTannisu = 5;
                    final int x1Kanten = 0, x2Kanten = x1Kanten + (int) (width * rateKanten / (double) (rateKanten + rateHyotei + rateTannisu));
                    final int x1Hyotei = x2Kanten, x2Hyotei = x1Hyotei + (int) (width * rateHyotei /  (double) (rateKanten + rateHyotei + rateTannisu));
                    final int x1Tannisu = x2Hyotei, x2Tannisu = x1Tannisu + (int) (width * rateTannisu /  (double) (rateKanten + rateHyotei + rateTannisu));

                    final int gap7p = (int) KNJSvfFieldModify.charPointToPixel("", 7.0, 0) / 2;
                    final int xKanten = gradeLeftLine.getPoint()._x + x1Kanten + (x2Kanten - x1Kanten) / 2 - gap7p;
                    final int xHyotei = gradeLeftLine.getPoint()._x + x1Hyotei + (x2Hyotei - x1Hyotei) / 2 - gap7p;
                    final int xTannisu = gradeLeftLine.getPoint()._x + x1Tannisu + (x2Tannisu - x1Tannisu) / 2 - gap7p;
                    final int mojipoint = 70; // 7.0
                    final int addY = -20;
                    final int addY2 = 20;

                    //log.info(" left = " + gradeLeftLine.getPoint()._x + ", right = " + gradeRightLine.getPoint()._x + ", width = " + width + " / xKanten = " + xKanten + ", xHyotei = " + xHyotei + ", xTannisu = " + xTannisu);

                    // タイトル
                    final String titleKanten = "観点別学習状況";
                    final String titleHyotei = "評(\\s|　)*定";
                    final String titleShutokuTannisu = "修得単位数";
                    SvfForm.KoteiMoji moto = null;
                    for (final SvfForm.KoteiMoji shutokuTannisu : svfForm.getKoteiMojiListWithText(titleShutokuTannisu)) {
                        if (shutokuTannisu.getPoint().xBetween(gradeLeftLine.getPoint(), gradeRightLine.getPoint())) {
                            moto = shutokuTannisu;
                            svfForm.move(shutokuTannisu, shutokuTannisu.setX(xTannisu).addY(addY).setMojiPoint(mojipoint).setEndX(moto._endX + addY2));

                            final SvfForm.Line leftLine = svfForm.getNearestLeftLine(shutokuTannisu.getPoint());
                            svfForm.move(leftLine, leftLine.setX(gradeLeftLine.getPoint()._x + x2Hyotei));
                            svfForm.addLine(leftLine.setX(gradeLeftLine.getPoint()._x + x2Kanten));
                            break;
                        }
                    }
                    for (final SvfForm.KoteiMoji hyotei : svfForm.getKoteiMojiListWithRegex(titleHyotei)) {
                        if (hyotei.getPoint().xBetween(gradeLeftLine.getPoint(), gradeRightLine.getPoint())) {
                            svfForm.move(hyotei, hyotei.setX(xHyotei).addY(addY).setMojiPoint(mojipoint).setEndX(moto._endX + addY2));
                            break;
                        }
                    }
                    if (null == moto) {
                        log.warn(" no moji : " + titleHyotei + ", " + titleShutokuTannisu);
                    } else {
                        svfForm.addKoteiMoji(moto.setX(xKanten).replaceMojiWith(titleKanten).addY(addY).setMojiPoint(mojipoint).setEndX(moto._endX + addY2));
                    }

                    // データ
                    {
                        final SvfForm.Field grades = svfForm.getField("GRADES" + String.valueOf(n));
                        final SvfForm.Field credit = svfForm.getField("CREDIT" + String.valueOf(n));
                        final SvfForm.Line creditLeftLine = svfForm.getNearestLeftLine(credit.getPoint());
                        svfForm.move(creditLeftLine, creditLeftLine.setX(gradeLeftLine.getPoint()._x + x1Tannisu));
                        svfForm.addLine(creditLeftLine.setX(gradeLeftLine.getPoint()._x + x2Kanten));

                        svfForm.addField(grades.copyTo("KANTEN" + String.valueOf(n)).setFieldLength(3).setX(gradeLeftLine.getPoint()._x + x1Kanten + 3).setPrintMethod(SvfForm.Field.PrintMethod.MUHENSHU)); // 観点出力欄を追加

                        svfForm.removeField(grades);
                        svfForm.addField(grades.setX(xHyotei + 3));

                        svfForm.removeField(credit);
                        svfForm.addField(credit.setX(xTannisu + 3));
                    }

                    // フッタ
                    {
                        final SvfForm.Field tani = svfForm.getField("tani_" + String.valueOf(n));
                        final SvfForm.Record record = svfForm.getRecordOfField(tani);
                        for (SvfForm.Line line : svfForm.getElementList(SvfForm.Line.class)) {

                            final int lineY1 = record.getAbsPoint1()._y, lineY2 = record.getAbsPoint2()._y;
                            final int line1x1 = gradeLeftLine.getPoint()._x + x1Kanten, line1x2 = gradeLeftLine.getPoint()._x + x2Kanten;
                            final int line2x1 = gradeLeftLine.getPoint()._x + x1Hyotei, line2x2 = gradeLeftLine.getPoint()._x + x2Hyotei;

                            if (line._start._x == line._end._x || !line._start.yBetween(record.getAbsPoint1(), record.getAbsPoint2())) {
                                continue;
                            }
                            svfForm.removeLine(line);
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, new SvfForm.Point(line1x1, lineY2), new SvfForm.Point(line1x2, lineY1)));
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, new SvfForm.Point(line2x1, lineY2), new SvfForm.Point(line2x2, lineY1)));
                        }

                        final SvfForm.Line taniLeftLine = svfForm.getNearestLeftLine(tani.getPoint());
                        svfForm.move(taniLeftLine, taniLeftLine.setX(gradeLeftLine.getPoint()._x + x1Tannisu));
                        svfForm.addLine(taniLeftLine.setX(gradeLeftLine.getPoint()._x + x2Kanten));

                        svfForm.removeField(tani);
                        svfForm.addField(tani.setX(xTannisu + 5));
                    }
                }
            }
            return true;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            setDetail3(db2, student, pInfo, csvLines);
        }

        public void setDetail3(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            getForm3(student, pInfo, null); // _gradeLineMaxセット
            final Map<Integer, PrintGakuseki> pagePringGakusekiMap = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_OMOTE, pInfo, param(), _gradeLineMax);
            final List<Integer> pageList = new ArrayList(pagePringGakusekiMap.keySet());

            for (int pageIdx = 0; pageIdx < pageList.size(); pageIdx++) {
                final Integer page = pageList.get(pageIdx);
                final PrintGakuseki printGakuseki = pagePringGakusekiMap.get(page);
                printGakuseki._pageIdx = pageIdx;
                printPage3(student, pInfo, printGakuseki, csvLines);
            }
            nonedata = true;
        }


        @Override
        public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
            printPage3(student, pInfo, pg, csvLines);
        }

        private void printPage3(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            String form = null;
            if (null != csvLines) {
                _isHeaderInRecord3 = true;
                _isFooterInRecord3 = true;
                MAX_LINE = new int[] {};
            } else {
                form = getForm3(student, pInfo, printGakuseki);
                svfVrSetForm(form, 4);
                final String mform = modifyForm3(form, student, pInfo, printGakuseki);
                if (!mform.equals(form)) {
                    svfVrSetForm(mform, 4);
                }
                _isHeaderInRecord3 = _headerFooterRecordFormList.contains(form);
                _isFooterInRecord3 = _headerFooterRecordFormList.contains(form) || _footerOnlyRecordFormList.contains(form);
                if (null == _name) {
                    _name = _form._formInfo.getFieldInfo("NAME1", "NAME2", "NAME3", charSize11, 24);
                    if (param()._isOutputDebug) {
                        log.info(" name3 = " + _name);
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" printGakuseki " + printGakuseki);
                }
            }
            printStudyDetail3(student, pInfo, printGakuseki, csvLines);
        }

        /**
         * 学習の記録明細を印刷します。
         */
        private void printStudyDetail3(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || printGakuseki._isLastPrintGakuseki;
            final boolean isFirst;
            if (param()._is133m) {
                isFirst = 0 == printGakuseki._pageIdx;
            } else {
                isFirst = true;
            }
            final Map<String, StudyRecSubclassTotal> studyRecSubclassTotalMap = PersonalInfo.createStudyRecTotalMap(param(), pInfo, PersonalInfo.getSubclassStudyrecListMap(param(), isFirst, student, pInfo, pInfo._gakusekiList, _yoshiki));
            if (param()._is133m) {
                final TreeSet<String> yearSet = printGakuseki.yearSet();
                if (yearSet.isEmpty()) {
                    return;
                }
                final int XMAX = param()._z010.in(Z010.tokyoto) ? 55 : param()._z010.in(Z010.sagaken) ? 58 : 65; // 東京都の行数がページによって異なる => MAX_LINE
                List<PrintLine> printLineList = getPrintLineList3M(isPrintTotalCredits, student, pInfo, XMAX, studyRecSubclassTotalMap, yearSet);

                final PrintLine finalLine = printLineList.get(printLineList.size() - 1);
                printLineList = printLineList.subList(0, printLineList.size() - 1);
                int tokyoSetPage = 0;
                final List<List<PrintLine>> pageList = getPageList(printLineList, XMAX);
                for (int i = 0; i < pageList.size(); i++) {
                    final List<PrintLine> currentOutputLineList = pageList.get(i);
                    tokyoSetPage = 1;

                    if (param()._z010.in(Z010.tokyoto) && i != 0 && i == pageList.size() - 1) {
                        svfVrSetForm("KNJA133M_5TOKYO.frm", 4);
                        tokyoSetPage = 2;
                    }

                    printGakuseki3(student, pInfo, printGakuseki, csvLines);

                    svfVrsOut("TOTAL_SUBCLASSNAME", pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap));

                    for (final PrintLine outputLine : currentOutputLineList) {
                        printLine3M(student, pInfo, printGakuseki, outputLine);
                        svfVrEndRecord();
                    }
                }

                if (param()._z010.in(Z010.tokyoto) && 2 != tokyoSetPage) {
                    log.debug(" tokyoto page 2.");
                    final PrintLine emptyLine = new PrintLine(999);
                    emptyLine._specialDiv = finalLine._specialDiv;
                    emptyLine._classcd = "";
                    for (int i = 0; i < 1; i++) {
                        printLine3M(student, pInfo, printGakuseki, emptyLine);
                        svfVrEndRecord();
                    }
                    svfVrSetForm("KNJA133M_5TOKYO.frm", 4);
                    for (int i = 0; i < 50 - 1; i++) {
                        printLine3M(student, pInfo, printGakuseki, emptyLine);
                        svfVrEndRecord();
                    }
                }

                printTotalCredits3M(isPrintTotalCredits, student, pInfo, printGakuseki);

                printLine3M(student, pInfo, printGakuseki, finalLine);
                svfVrEndRecord();

            } else {
                final Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>> creditMap = getCreditMap(pInfo, studyRecSubclassTotalMap)._first;
                final boolean hasJiritsu = !Util.getMappedMap(Util.getMappedMap(creditMap, CreditTotalKey.yearJiritsu), StudyRec.CreditKind.CREDIT).isEmpty();
                final boolean isPrintJiritsu = hasJiritsu || pInfo._student.isShimanekenPrintJiritsuKatudou(param());
                List<PrintLine> printLineList = createPrintLineList3(isPrintTotalCredits, student, pInfo, printGakuseki, 0 == printGakuseki._pageIdx, MAX_LINE, isPrintJiritsu);
                if (printLineList.size() > 0) {
                    nonedata = true;
                }
                if (param()._isOutputDebug) {
                    log.info(" 2maime omote print line count = " + printLineList.size());
                }

                final PrintLine lastLine = printLineList.get(printLineList.size() - 1);
                final List<List<PrintLine>> pageList;
                if (MAX_LINE.length > 1) {
                    pageList = new ArrayList<List<PrintLine>>();
                    while (true) {
                        if (printLineList.size() <= MAX_LINE[MAX_LINE.length - 1]) {
                            pageList.add(printLineList);
                            break;
                        }

                        if (MAX_LINE[0] < printLineList.size()) {
                            final List<PrintLine> subList = printLineList.subList(0, MAX_LINE[0]);
                            pageList.add(subList);
                            printLineList = printLineList.subList(MAX_LINE[0], printLineList.size());
                            continue;
                        }

                        // MAX_LINE[MAX_LINE.length - 1] < printLineList.size() <= MAX_LINE[0]
                        pageList.add(printLineList);
                        final List<PrintLine> noneContentsPage = new ArrayList<PrintLine>();
                        pageList.add(noneContentsPage);
                        int linex = lastLine._linex; // なんでもよい
                        for (int l = 0; l < MAX_LINE[0]; l++) {
                            noneContentsPage.add(PrintLine.create(linex++, lastLine._specialDiv));
                        }
                        break;
                    }
                } else {
                    int maxLine = MAX_LINE.length == 0 ? 9999 : MAX_LINE[MAX_LINE.length - 1];
                    if (param()._z010.in(Z010.mieken)) {
                        if (isPrintJiritsu) {
                            maxLine -= 2;
                        }
                    }
                    pageList = getPageList(printLineList.subList(0, printLineList.size() - 1), maxLine);
                }
                if (param()._isOutputDebug) {
                    log.info(" pageList size = " + pageList.size());
                    for  (int i = 0; i < pageList.size(); i++) {
                        final List<PrintLine> pagePrintLine = pageList.get(i);
                        log.info(" pi = " + i + ", line size = " + pagePrintLine.size());
                    }
                }

                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List<PrintLine> pagePrintLineList = pageList.get(pi);
                    if (param()._z010.in(Z010.chiyodaKudan)) {
                        if (pi == pageList.size() - 1) {
                            svfVrSetForm("KNJA130_5KUDAN.frm", 4);
                        }
                    }

                    if (null != csvLines) {
                        printGakuseki3(student, pInfo, printGakuseki, csvLines);
                        for (int i = 0; i < pagePrintLineList.size(); i++) {
                            final PrintLine printLine = pagePrintLineList.get(i);
                            printLine3(printLine, student, pInfo, printGakuseki, csvLines);
                        }

                    } else {
                        printGakuseki3(student, pInfo, printGakuseki, null);
                        if (_isHeaderInRecord3) {
                            svfVrEndRecord();
                        }

                        for (int i = 0; i < pagePrintLineList.size(); i++) {
                            final PrintLine printLine = pagePrintLineList.get(i);
                            printLine3(printLine, student, pInfo, printGakuseki, null);
                            svfVrEndRecord();
                        }
                        printFooterRemark(pInfo, YOSHIKI._2_OMOTE);

                        if (_isFooterInRecord3) {
                            if (pi != pageList.size() - 1) {
                                printTotalCredits3(isPrintTotalCredits, student, pInfo, printGakuseki, studyRecSubclassTotalMap, isPrintJiritsu, null);
                            }
                        }
                    }
                }

                if (null != csvLines) {
                    // 最後のレコード
                    printLine3(lastLine, student, pInfo, printGakuseki, csvLines);
                    printTotalCredits3(isPrintTotalCredits, student, pInfo, printGakuseki, studyRecSubclassTotalMap, isPrintJiritsu, csvLines);
                } else {
                    // 最後のレコード
                    printLine3(lastLine, student, pInfo, printGakuseki, null);
                    if (_isFooterInRecord3) {
                        svfVrEndRecord();
                    }
                    printTotalCredits3(isPrintTotalCredits, student, pInfo, printGakuseki, studyRecSubclassTotalMap, isPrintJiritsu, null);
                    if (!_isFooterInRecord3) {
                        svfVrEndRecord();
                    }
                }
                nonedata = true;
            }
        }

        private String eduDivFieldname() {
            if (_isPrintEduDiv2CharsPerLine) {
                if (param()._isHankiNinteiForm) {
                    return "EDU_DIV_2HANKI";
                }
                return "EDU_DIV_2";
            }
            if (param()._isHankiNinteiForm) {
                return "EDU_DIV_HANKI";
            }
            return "EDU_DIV";
        }


        private String eduDiv2Fieldname() {
            if (param()._isHankiNinteiForm) {
                return "EDU_DIV2_HANKI";
            }
            return "EDU_DIV2";
        }

        private String classnameFieldname() {
            if (param()._isHankiNinteiForm) {
                return "CLASSNAME_HANKI";
            }
            return "CLASSNAME";
        }

        private String classname2Fieldname() {
            if (param()._isHankiNinteiForm) {
                return "CLASSNAME2_HANKI";
            }
            return "CLASSNAME2";
        }

        private String subclassnameFieldname(final PrintLine printLine) {
            if (param()._isHankiNinteiForm) {
                return "SUBCLASSNAME_HANKI";
            }
            return "SUBCLASSNAME";
        }

        private String yearCreditValuationField(final PrintLine printLine, final String head, final String col, final String sfx) {
            return head + defstr(col) + sfx;
        }

        private String totalCreditField(final PrintLine printLine, final String head, final String sfx) {
            return head + sfx;
        }

        private void printLine3(final PrintLine printLine, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            if (null != csvLines) {
                final List<String> line = CsvUtils.newLine(csvLines);
                line.addAll(Arrays.asList(printLine._edudiv, printLine._classname, printLine._subclassname));

                for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                    final Integer column = printGakuseki._yearGakusekiPositionMap.get(year);
                    // 評定
                    line.add(printLine._valuationMap.get(column));
                    line.add(printLine._creditsMap.get(column));
                }
                line.add(printLine._totalCredits); // 科目別修得単位数
                line.add(printLine._biko);
                return;
            }
            svfVrsOut(eduDivFieldname(), printLine._edudiv);
            svfVrsOut(eduDiv2Fieldname(), printLine._specialDiv);
            svfVrsOut(classnameFieldname(), printLine._classname);

            if (hasField("SUBCLASSNAME_GRP")) {
                // グループ化処理
                final String slinex = String.valueOf(printLine._linex);
                svfVrsOut("SUBCLASSNAME_GRP", slinex);
                for (int j = 0; j < 4; j++) {
                    svfVrsOut("GRADES" + String.valueOf(j + 1) + "_GRP", slinex);
                    svfVrsOut("CREDIT" + String.valueOf(j + 1) + "_GRP", slinex);
                }
                svfVrsOut("biko_grp", slinex);
            }

            String sfx;
            if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                final String sfx2 = printLine._isClassFirstLine ? "1" : "2";
                svfVrsOut("SUBCLASSNAME" + sfx2 + "_" + (getMS932ByteLength(printLine._subclassname) > 20 ? "2" : "1"), printLine._subclassname);
                svfVrsOut("GRP" + sfx2, printLine._classcd); // 教科コード
                sfx = "_" + sfx2;
            } else {
                if (printLine._bikoi <= 0) {
                    svfVrsOut(subclassnameFieldname(printLine), printLine._subclassname);
                }
                svfVrsOut(classname2Fieldname(), printLine._classcd); // 教科コード
                if (param()._isHankiNinteiForm) {
                    sfx = "_HANKI";
                } else {
                    sfx = "";
                }
            }
            if (printLine._bikoi <= 0) {
                if (pInfo._isFuhakkou) {
                    // 評定を印刷しない
                } else {

                    if (printGakuseki._isYoshiki2omote3KantenForm) {
                        for (final Integer col : printLine._kantenMap.keySet()) {
                            svfVrsOut("KANTEN" + col.toString(), printLine._kantenMap.get(col));
                        }
                    }

                    // 評定
                    for (final Integer col : printLine._valuationMap.keySet()) {
                        String valutionField = yearCreditValuationField(printLine, "GRADES", col.toString(), sfx);
                        if (param()._isHankiNinteiForm) {
                            // 前期単位
                            final String valuation1 = printLine.getZenkiValuation(col.toString());
                            if (null != valuation1 && NumberUtils.isDigits(valuation1)) {
                                final String field1 = valutionField + "1"; // 前期
                                svfVrAttribute(field1, ATTR_CENTERING); // 中央割付
                                svfVrsOut(field1, valuation1); // 評定
                            }
                            // 後期単位
                            final String valuation2 = printLine.getZenkiIgaiValuation(col.toString());
                            if (null != valuation2 && NumberUtils.isDigits(valuation2)) {
                                final String field2 = valutionField + "2"; // 後期
                                svfVrAttribute(field2, ATTR_CENTERING); // 中央割付
                                svfVrsOut(field2, valuation2); // 評定
                            }
                        } else {
                            // 評定
                            final String fieldname = _form._formInfo.getFieldForData(Arrays.asList(valutionField, valutionField + "_2"), printLine._valuationMap.get(col));
                            svfVrsOut(fieldname, printLine._valuationMap.get(col));
                            if (null != printLine._valuationAttributeMap.get(col)) {
                                svfVrAttribute(fieldname, printLine._valuationAttributeMap.get(col));
                            }
                        }
                    }
                }
                // 科目ごと年度ごとの単位数
                for (final Integer col : printLine._creditsMap.keySet()) {
                    final String creditsField = yearCreditValuationField(printLine, "CREDIT", col.toString(), sfx);
                    if (param()._isHankiNinteiForm) {
                        svfVrsOut(creditsField + "1", printLine.getZenkiCredit(col.toString())); // 前期単位
                        svfVrsOut(creditsField + "2", printLine.getZenkiIgaiCredit(col.toString())); // 後期単位
                    } else {
                        svfVrsOutForData(Arrays.asList(creditsField, creditsField + "_2"), printLine._creditsMap.get(col)); // 単位
                    }
                }
                // 科目ごとの合計単位数
                if (null != printLine._totalCredits) {
                    final String fieldname = totalCreditField(printLine, "CREDIT", sfx);
                    svfVrsOutForData(Arrays.asList(fieldname, fieldname + "_2"), printLine._totalCredits); // 科目別修得単位数
                }
            }
            if (printLine._biko != null && printLine._biko.length() != 0) {
                printBiko(printLine, pInfo);
            }
        }

        private String getBiko90(final PersonalInfo pInfo, final PrintGakuseki printgakuseki) {
            final TreeSet<String> yearSet = new TreeSet<String>(printgakuseki._yearGakusekiPositionMap.keySet());
            final String minYear;
            final String maxYear;
            if (yearSet.isEmpty()) {
                minYear = null;
                maxYear = null;
            } else {
                minYear = yearSet.first();
                maxYear = yearSet.last();
            }

            final String gakushuBiko90 = pInfo._gakushuBiko.getStudyrecBiko(_90, minYear, maxYear).toString();
            final String rishuTaniBiko90 = pInfo._gakushuBiko.getRishuTanniBiko(_90, minYear, maxYear).toString();
            final String studyrecSubstitutionBiko90Zenbu = param().isNotPrintDaitai("2omote") ? "" : pInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, GakushuBiko.DAITAI_TYPE.ZENBU, minYear, maxYear).toString();
            final String studyrecSubstitutionBiko90Ichibu = param().isNotPrintDaitai("2omote") ? "" : pInfo._gakushuBiko.getStudyrecSubstitutionBiko(_90, GakushuBiko.DAITAI_TYPE.ICHIBU, minYear, maxYear).toString();
            final String studyrecSubstitutionBiko90 = studyrecSubstitutionBiko90Zenbu + studyrecSubstitutionBiko90Ichibu;
            final String[] bikoArray = {rishuTaniBiko90, gakushuBiko90, studyrecSubstitutionBiko90};
            return Util.mkString(Arrays.asList(bikoArray), "、").toString();
        }

        private String yoshiki2omoteBikoField(final Param param, final String n) {
            if (param._isHankiNinteiForm) {
                return "biko" + n + "_HANKI";
            }
            return "biko" + n;
        }

        private void printBiko(final PrintLine printLine, final PersonalInfo pInfo) {
            final int length = getMS932ByteLength(printLine._biko);
            final String[] bikoField = new String[5];
            for (int j = 0; j < bikoField.length; j++) {
                bikoField[j] = yoshiki2omoteBikoField(param(), String.valueOf(j + 1));
            }
            if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                final String sfx2 = printLine._isClassFirstLine ? "1" : "2";
                if (length <= 50) {
                    svfVrsOut("biko1_" + sfx2, printLine._biko);
                } else {
                    final List<String> token = Util.getTokenList(param(), printLine._biko, 80, 2);
                    for (int i = 0; i < token.size(); i++) {
                        svfVrsOut("biko" + String.valueOf(i + 2) + "_" + sfx2, token.get(i));
                    }
                }
            } else if (printLine._bikoi == -1) {
                for (int j = 0; j < bikoField.length; j++) {
                    svfVrsOut(bikoField[j], ""); // クリア処理
                }
                final String i;
                if (length <= 40) {
                    i = "1"; // 40桁フィールド
                } else if (length <= 60) {
                    i = "2"; // 60桁フィールド
                } else if (length <= 80) {
                    i = "3"; // 80桁フィールド
                } else if (length <= 100) {
                    i = "4"; // 100桁フィールド
                } else {
                    i = "5"; // 240桁フィールド
                }
                if (param()._isOutputDebug) {
                    log.info("biko" + i + " = " + printLine._biko);
                }
                svfVrsOut(yoshiki2omoteBikoField(param(), i), printLine._biko);
            } else {
                for (int j = 0; j < bikoField.length; j++) {
                    svfVrsOut(bikoField[j], ""); // クリア処理
                }
                svfVrsOut(yoshiki2omoteBikoField(param(), printLine._bikoField), printLine._biko);
            }
        }

        private static boolean yearBetween(final String year, final String min, final String max) {
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

        private List<PrintLine> createPrintLineList3(
                final boolean isPrintTotalCredits,
                final Student student,
                final PersonalInfo pInfo,
                final PrintGakuseki printGakuseki,
                final boolean isPage0,
                final int[] MAX_LINE,
                final boolean isPrintJiritsu) {

            final Param param = param();
            final int iMinYear = Integer.parseInt(printGakuseki.minYear());
            int linex = 0; // 行数
            final List<PrintLine> printLineList = new ArrayList<PrintLine>();

            final TreeSet<String> targetYearSet = printGakuseki.yearSet();
            if (param._isOutputDebug) {
                log.info(" targetYearSet = " + targetYearSet);
            }
            final List<StudyRec> studyrecList = new ArrayList();
            for (final StudyRec studyrec : pInfo._studyRecList) {
                final boolean isPrintAnotherStudyrec3 = param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_1
                                                     || param._printAnotherStudyrec3 == Param._printAnotherStudyrec3_2 && param._schoolDiv.isTanniSei(null, pInfo, student);
                if (isPrintAnotherStudyrec3 && isPage0 && NumberUtils.isDigits(studyrec._year) && Integer.parseInt(studyrec._year) < iMinYear) {
                    // 前籍校の成績を表示する
                } else if (!targetYearSet.contains(studyrec._year)) {
                    continue;
                }
                if (param._isOutputDebugSeiseki) {
                    log.info(" add " + studyrec);
                }
                studyrecList.add(studyrec);
            }
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = getStudyrecSpecialDivList3(studyrecList, param);

            List<StudyrecSubClass> creditOnlyClasscdStudyrecSubClass = new ArrayList<StudyrecSubClass>();

            final Map<SubclassMst, TreeMap<Year, String>> subclassYearViewsMap = student.getSubclassYearViewMap(printGakuseki._yearList);

            String specialDiv = "";
            for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                specialDiv = studyrecSpecialDiv.studyrec()._classMst._specialDiv;
                final String specialName = param.getSpecialDivName(isNewForm(param, pInfo), studyrecSpecialDiv.studyrec()._classMst._specialDiv);

                int lineSpecialDiv = 0; // 普通・専門毎の行数
                // 教科毎の表示
                for (final StudyrecClass studyrecClass : studyrecSpecialDiv._studyrecClassList) {

                    // 総合的な学習の時間・留学は回避します。
                    final List<StudyRec.KIND> kindList = studyrecClass.studyrec().kindList(param, pInfo);
                    if (kindList.contains(StudyRec.KIND.SOGO90)) {
                        final Map<String, TreeMap<RishuStatus, List<StudyRec>>> bikoStatusStudyrecListMap = new TreeMap<String, TreeMap<RishuStatus, List<StudyRec>>>();
                        for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {
                            final Map<String, List<StudyRec>> yearStudyrecListMap = studyrecSubClass.getYearStudyrecListMap();
                            for (final String year : yearStudyrecListMap.keySet()) {
                                final List<StudyRec> yearStudyrecList = yearStudyrecListMap.get(year);

                                for (final StudyRec studyrec : yearStudyrecList) {
                                    final RishuStatus stat = getRishuStatus(param, student, pInfo, studyrec);
                                    if (stat == RishuStatus.NONE) {
                                        if (null != studyrec._yomikaeMotoStudyrecList) {
                                            for (final StudyRec rst : studyrec._yomikaeMotoStudyrecList) {
                                                final RishuStatus rstat = getRishuStatus(param, student, pInfo, rst);
                                                if (rstat != RishuStatus.NONE) {
                                                    getMappedList(Util.getMappedMap(bikoStatusStudyrecListMap, rst._year), rstat).add(rst);
                                                }
                                            }
                                        }
                                    } else {
                                        getMappedList(Util.getMappedMap(bikoStatusStudyrecListMap, studyrec._year), stat).add(studyrec);
                                    }
                                }
                            }
                        }

                        for (final String year : bikoStatusStudyrecListMap.keySet()) {
                            for (final Map.Entry<RishuStatus, List<StudyRec>> e : Util.getMappedMap(bikoStatusStudyrecListMap, year).entrySet()) {
                                final RishuStatus stat = e.getKey();
                                final List<StudyRec> rStudyrecList = e.getValue();
                                final List<BigDecimal> compCreditList = new ArrayList<BigDecimal>();
                                for (final StudyRec st : rStudyrecList) {
                                    if (null != st._compCredit) {
                                        compCreditList.add(st._compCredit);
                                    }
                                }
                                final String compVal = getRishuTanniBiko(param, printGakuseki, year, Util.bdSum(compCreditList), stat);
                                if (!StringUtils.isBlank(compVal)) {
                                    pInfo._gakushuBiko.putRishuTanniBiko(_90, year, compVal);
                                }
                            }
                        }

                        continue;
                    } else if (kindList.contains(StudyRec.KIND.ABROAD)) {
                        continue;
                    } else if (kindList.contains(StudyRec.KIND.JIRITSU)) {
                        continue;
                    } else if (kindList.contains(StudyRec.KIND.CREDIT_ONLY)) {
                        if (!param._z010.in(Z010.tokiwa)) {
                            creditOnlyClasscdStudyrecSubClass.addAll(studyrecClass._studyrecSubclassList);
                        }
                        continue;
                    }
                    int lineClasscd = 0;
                    // 科目毎の表示
                    for (int subi = 0; subi < studyrecClass._studyrecSubclassList.size(); subi++) {
                        final StudyrecSubClass studyrecSubClass = studyrecClass._studyrecSubclassList.get(subi);
                        final PrintLine l1 = new PrintLine(linex);
                        setPrintLineCredit3(param, isPrintTotalCredits, student, pInfo, printGakuseki, studyrecSubClass, subclassYearViewsMap.get(studyrecSubClass.studyrec().subclassMst(param, YOSHIKI._2_OMOTE)), l1);

                        final List<PrintLine> sameSubclassPrintLine = new ArrayList<PrintLine>();
                        setPrintLineBiko(param, student, pInfo, printGakuseki, studyrecSubClass, l1, sameSubclassPrintLine);
                        for (int bi = 0; bi < sameSubclassPrintLine.size(); bi++) {
                            final PrintLine l = sameSubclassPrintLine.get(bi);
                            if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo)) {
                                if (lineClasscd == 0) {
                                    l._classname = studyrecClass.studyrec()._classMst._classname;
                                }
                            } else {
                                if (lineClasscd < defstr(studyrecClass.studyrec()._classMst._classname).length()) {
                                    l._classname = studyrecClass.studyrec()._classMst._classname.substring(lineClasscd, lineClasscd + 1);
                                }
                            }
                            l._classcd = studyrecClass.studyrec()._classMst._classcd;
                            lineClasscd++;
                            l._edudiv = eduDiv(param, specialName, lineSpecialDiv);
                            l._specialDiv = specialDiv;
                            l._isClassFirstLine = subi == 0 && bi == 0;
                            lineSpecialDiv++;
                            printLineList.add(l);
                            linex++;
                        }
                    }

                    if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo)) {
                    } else {
                        boolean outputNokori = false;
                        while (lineClasscd < defstr(studyrecClass.studyrec()._classMst._classname).length()) {
                            final PrintLine l = new PrintLine(linex);
                            l._edudiv = eduDiv(param, specialName, lineSpecialDiv);
                            l._specialDiv = specialDiv;
                            lineSpecialDiv++;
                            l._classcd = studyrecClass.studyrec()._classMst._classcd;
                            l._classname = studyrecClass.studyrec()._classMst._classname.substring(lineClasscd, lineClasscd + 1);
                            lineClasscd++;
                            printLineList.add(l);
                            linex++;
                            outputNokori = true;
                        }
                        if (!outputNokori) {
                            final PrintLine l = new PrintLine(linex);
                            boolean outputName = false;
                            l._edudiv = eduDiv(param, specialName, lineSpecialDiv);
                            outputName = !StringUtils.isBlank(l._edudiv);
                            lineSpecialDiv++;
                            if (outputName || MAX_LINE.length == 0 || linex != MAX_LINE[MAX_LINE.length - 1]) {
                                l._specialDiv = specialDiv;
                                l._classcd = studyrecClass.studyrec()._classMst._classcd;
                                printLineList.add(l);
                                linex++;
                            }
                        }
                    }
                }

                if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo)) {
                } else {
                    while (true) {
                        final String eduDiv = eduDiv(param, specialName, lineSpecialDiv);
                        if (StringUtils.isBlank(eduDiv)) {
                            break;
                        } else {
                            final PrintLine l = new PrintLine(linex);
                            l._edudiv = eduDiv;
                            l._specialDiv = specialDiv;
                            l._classcd = "00";
                            printLineList.add(l);
                            lineSpecialDiv++;
                            linex++;
                        }
                    }
                }
                if (param._isOutputDebugData) {
                    log.fatal(" rishu tanni    biko = " + pInfo._gakushuBiko._biko.get(GakushuBiko.RISHU));
                    log.fatal(" studyrec       biko = " + pInfo._gakushuBiko._biko.get(GakushuBiko.STUDY));
                    log.fatal(" studyrec subst biko = " + pInfo._gakushuBiko._biko.get(GakushuBiko.SUBST));
                }
            }
            int maxLine = 0;
            for (int i = 0; i < MAX_LINE.length; i++) {
                maxLine += MAX_LINE[i];
            }
            if (_isFooterInRecord3) {
                if (isPrintJiritsu) {
                    maxLine -= 2;
                }
            }
            final int max = maxLine - creditOnlyClasscdStudyrecSubClass.size();
            if (param._isOutputDebug) {
                log.info(" 2maime omote print line count1 = " + printLineList.size() + ", max line = " + maxLine);
            }
            boolean isfirst = true;
            if (!param._isCsv) {
                for (int i = linex == 0 ? 0 : linex % maxLine == 0 ? maxLine : linex % maxLine; i < max; i++) {
                    final PrintLine l = new PrintLine(linex);
                    l._specialDiv = specialDiv;
                    l._classcd = "00";
                    if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo) && isfirst) {
                        l._isClassFirstLine = true;
                    }
                    printLineList.add(l);
                    linex++;
                    isfirst = false;
                }
            }
            for (int i = 0; i < creditOnlyClasscdStudyrecSubClass.size(); i++) {
                final StudyrecSubClass studyrecSubClass = creditOnlyClasscdStudyrecSubClass.get(i);
                final PrintLine l = new PrintLine(linex);
                setPrintLineCredit3(param, isPrintTotalCredits, student, pInfo, printGakuseki, studyrecSubClass, subclassYearViewsMap.get(studyrecSubClass.studyrec().subclassMst(param, YOSHIKI._2_OMOTE)), l);

                final List<PrintLine> sameSubclassPrintLine = new ArrayList<PrintLine>();
                setPrintLineBiko(param, student, pInfo, printGakuseki, studyrecSubClass, l, sameSubclassPrintLine);
                l._specialDiv = specialDiv;
                l._classcd = studyrecSubClass.studyrec()._classMst._classcd;
                if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo) && isfirst) {
                    l._isClassFirstLine = true;
                }
                printLineList.add(l);
                linex++;
                isfirst = false;
            }
            return printLineList;
        }

        private void setPrintLineBiko(final Param param,
                final Student student,
                final PersonalInfo pInfo,
                final PrintGakuseki printGakuseki,
                final StudyrecSubClass studyrecSubClass,
                final PrintLine printLine,
                final List<PrintLine> sameSubclassPrintLine) {
            final Map<String, List<StudyRec>> yearStudyrecListMap = studyrecSubClass.getYearStudyrecListMap();
            String biko = null;
            for (final String year : yearStudyrecListMap.keySet()) {
                if (!pInfo.isTargetYearLast(year, student, param, false)) {
                    continue;
                }
                if (!printGakuseki.yearSet().contains(year)) {
                    continue;
                }
                final List<StudyRec> yearStudyrecList = yearStudyrecListMap.get(year);

                for (final StudyRec studyrec : yearStudyrecList) {

                    final String keysubclasscd = studyrecSubClass.studyrec().getKeySubclasscd(param);

                    biko = getBiko(param, student, pInfo, printGakuseki, studyrec, keysubclasscd);
                }
            }

            if (param._z010.in(Z010.tokiwa) && !isNewForm(param, pInfo)) {
                printLine._biko = biko;
                sameSubclassPrintLine.add(printLine);
            } else {
                final FormInfo formInfo = _form._formInfo;
                final List<String> bikoLines;
                if (!_remarkFukusugyoFormList.contains(formInfo._formname)) {
                    bikoLines = Arrays.asList(biko);
                    final PrintLine l = (PrintLine) printLine.clone();
                    l._bikoi = -1;
                    l._biko = bikoLines.get(0);
                    sameSubclassPrintLine.add(l);
                } else if (param._z010.in(Z010.fukuiken)) {
                    final int keta = getMS932ByteLength(biko);
                    final List<String> fieldList = new ArrayList<String>();
                    for (int n = 1; n <= 5; n++) {
                        fieldList.add(yoshiki2omoteBikoField(param, String.valueOf(n)));
                    }
                    final Integer idx = formInfo.getPrintFieldIndex(keta, fieldList);
                    if (null != idx) {
                        final String n = String.valueOf(idx.intValue() + 1);
                        printLine._bikoField = n;
                        printLine._biko = biko;
                        sameSubclassPrintLine.add(printLine);
                    }
                } else {
                    final int keta = getMS932ByteLength(biko);
                    if (keta <= 40) {
                        printLine._bikoField = "1"; // 40桁
                        bikoLines = Arrays.asList(biko);
                    } else if (keta <= 60) {
                        printLine._bikoField = "2"; // 60桁
                        bikoLines = Arrays.asList(biko);
                    } else {
                        printLine._bikoField = "2"; // 60桁
                        bikoLines = Util.getTokenList(param, biko, 60);
                    }
                    for (int bi = 0; bi < bikoLines.size(); bi++) {
                        final PrintLine l = (PrintLine) printLine.clone();
                        l._bikoi = bi;
                        l._biko = bikoLines.get(bi);
                        sameSubclassPrintLine.add(l);
                    }
                }
            }
        }

        private static void setPrintLineCredit3(final Param param,
                final boolean isPrintTotalCredits,
                final Student student,
                final PersonalInfo pInfo,
                final PrintGakuseki printGakuseki,
                final StudyrecSubClass studyrecSubClass,
                final TreeMap<Year, String> yearViewsMap,
                final PrintLine printLine) {
            BigDecimal pageSubclassCredit = null;

            int validFlgMinYear = 9999;
            boolean hasDropValid = false;
            // 留年時の有効フラグのレコードがあるか確認
            for (final StudyRec studyRec : pInfo._studyRecList) {
                if (studyRec.getKeySubclasscdForSubclassTotal(param).equals(studyrecSubClass.studyrec().getKeySubclasscdForSubclassTotal(param))) {
                    if (pInfo.getDropYears(param).contains(studyRec._year) && null != studyRec._validFlg) {
                        hasDropValid = true;
                        validFlgMinYear = Math.min(validFlgMinYear, Integer.parseInt(studyRec._year));
                    }
                }
            }

            final Map<String, List<StudyRec>> yearStudyrecListMap = studyrecSubClass.getYearStudyrecListMap();
            if (printGakuseki._isYoshiki2omote3KantenForm) {
                if (param._isOutputDebug) {
                    log.info(" yearViewsMap (" + studyrecSubClass.studyrec().subclassMst(param, YOSHIKI._2_OMOTE) + ") = " + yearViewsMap);
                }
                if (null != yearViewsMap) {
                    for (final Map.Entry<Year, String> e : yearViewsMap.entrySet()) {
                        if (!yearStudyrecListMap.containsKey(e.getKey()._get)) {
                            yearStudyrecListMap.put(e.getKey()._get, null); // 観点のみの科目を表示対象とするためのダミー
                        }
                    }
                }
            }
            for (final String year : yearStudyrecListMap.keySet()) {
                if (!pInfo.isTargetYearLast(year, student, param, false)) {
                    continue;
                }
                if (!printGakuseki.yearSet().contains(year)) {
                    continue;
                }
                final List<StudyRec> yearStudyrecList = yearStudyrecListMap.get(year);

                final StudyRec union = null == yearStudyrecList ? null : StudyRec.union(param, year, yearStudyrecList);

                // 学年ごとの出力
                final Integer column = printGakuseki._yearGakusekiPositionMap.get(year);
                if (null != column && column.intValue() != 0) {

                    if (printGakuseki._isYoshiki2omote3KantenForm) {
                        printLine.setKanten(column, null == yearViewsMap ? null : yearViewsMap.get(Year.of(year)));
                    }

                    if (null == union) {
                        continue;
                    }

                    if (param._optionCreditOutputYoshiki2Omote == OptionCredit.YOSHIKI2_OMOTE_1 && union.isMirishu(param)) {
                    } else {
                        // 評定
                        if (param._z010.in(Z010.aoyama)) {
                            String v = "";
                            String attr = null;
                            final AoyamaGakunenSeiseki.Judge[] judges = pInfo._aoyamaGakunenSeiseki.getJudges(union);
                            v = pInfo._aoyamaGakunenSeiseki.hyotei(judges, union);
                            if (AoyamaGakunenSeiseki.Judge.isHyoteiColorRed(judges)) {
                                attr = ATTR_COLOR_RED;
                            }
                            printLine.setValuation(column, v);
                            if (null != attr) {
                                printLine._valuationAttributeMap.put(column, attr);
                            }
                        } else if ("print100".equals(param._hyotei)) {
                            printLine.setValuation(column, pInfo._gakunenSeiseki.getGakunenSeisekiString(union._subclassMst, Year.of(year)));
                        } else if (null == union._valuation) {
                            if (param._z010.in(Z010.tottori) && null != union._credit && null == union._valuation) {
                                printLine.setValuation(column, "-");
                            }
                        } else {
                            if ("1".equals(param.property(Property.seitoSidoYorokuHyotei0ToBlank)) && union._valuation.intValue() == 0) {
                            } else {
                                printLine.setValuation(column, union._valuation.toString());
                            }
                        }

                        // 前期評定
                        if (null != union._valuationZenki) {
                            if ("1".equals(param.property(Property.seitoSidoYorokuHyotei0ToBlank)) && union._valuationZenki.intValue() == 0) {
                            } else {
                                printLine.setZenkiValuation(column.toString(), union._valuationZenki.toString());
                            }
                        }

                        // 後期評定
                        if (null != union._valuationZenkiIgai) {
                            if ("1".equals(param.property(Property.seitoSidoYorokuHyotei0ToBlank)) && union._valuationZenkiIgai.intValue() == 0) {
                            } else {
                                printLine.setZenkiIgaiValuation(column.toString(), union._valuationZenkiIgai.toString());
                            }
                        }
                        // 単位
                        boolean isCreditTotalTarget = true;
                        {
                            final BigDecimal setCredit;
                            String printCredit = null;
                            String attr = null;
                            if (param._z010.in(Z010.aoyama)) {
                                final AoyamaGakunenSeiseki.Judge[] judge = pInfo._aoyamaGakunenSeiseki.getJudges(union);
                                setCredit = AoyamaGakunenSeiseki.Judge.credit(judge, union);
                                printCredit = AoyamaGakunenSeiseki.Judge.getPrintCredit(judge, union);
                                isCreditTotalTarget = AoyamaGakunenSeiseki.Judge.isCreditSubclassTotalTarget(judge);
                                if (param._isOutputDebug) {
                                    if (judge.length != 0) {
                                        log.info(" studyrec = " + union + ", judge = " + ArrayUtils.toString(judge) + ", gakunenseiseki = " + pInfo._aoyamaGakunenSeiseki.getGakunenSeisekiString(union._subclassMst, Year.of(union._year)));
                                    }
                                }
                                if (AoyamaGakunenSeiseki.Judge.isHyoteiColorRed(judge)) {
                                    attr = ATTR_COLOR_RED;
                                }
                            } else if (null != union._credit) {
                                if (param._optionCreditOutputYoshiki2Omote == OptionCredit.YOSHIKI2_OMOTE_2) {
                                    final boolean compCreditIsNullOr0 = null == union._compCredit || null != union._compCredit && union._compCredit.intValue() == 0;
                                    if (union._credit.intValue() == 0 && compCreditIsNullOr0) {
                                        // 空欄
                                        setCredit = null;
                                    } else {
                                        setCredit = union._credit;
                                    }
                                } else {
                                    setCredit = union._credit;
                                }
                            } else {
                                setCredit = union._credit; // null
                            }
                            if (null == printCredit) {
                                if (null != setCredit) {
                                    printCredit = setCredit.toString();
                                }
                            }
                            printLine.setCredit(column, printCredit);
                            if (null != attr) {
                                printLine._valuationAttributeMap.put(column, attr);
                            }
                            if (null != setCredit) {
                                if (yearBetween(year, printGakuseki.minYear(), printGakuseki.maxYear()) && (!pInfo.getDropYears(param).contains(year) && (!hasDropValid || hasDropValid && (validFlgMinYear == 9999 || Integer.parseInt(year) < validFlgMinYear)) || hasDropValid && null != union._validFlg)) {
                                    if (isCreditTotalTarget) {
                                        pageSubclassCredit = addNumber(pageSubclassCredit, setCredit);
                                    }
                                }
                            }
                        }
                        if (null != union._credit) {
                            {
                                // 前期単位
                                BigDecimal setCreditZenki = null;
                                if (param._optionCreditOutputYoshiki2Omote == OptionCredit.YOSHIKI2_OMOTE_2) {
                                    final boolean compCreditIsNullOr0 = null == union._compCreditZenki || null != union._compCreditZenki && union._compCreditZenki.intValue() == 0;
                                    if (union._creditZenki.intValue() == 0 && compCreditIsNullOr0) {
                                        // 空欄
                                        setCreditZenki = null;
                                    } else {
                                        setCreditZenki = union._creditZenki;
                                    }
                                } else {
                                    setCreditZenki = union._creditZenki;
                                }
                                if (null != setCreditZenki) {
                                    printLine.setZenkiCredit(column.toString(), setCreditZenki.toString());
                                }
                            }
                            {
                                // 後期単位
                                BigDecimal setCreditZenkiIgai = null;
                                if (param._optionCreditOutputYoshiki2Omote == OptionCredit.YOSHIKI2_OMOTE_2) {
                                    final boolean compCreditIsNullOr0 = null == union._compCreditZenkiIgai || null != union._compCreditZenkiIgai && union._compCreditZenkiIgai.intValue() == 0;
                                    if (union._creditZenkiIgai.intValue() == 0 && compCreditIsNullOr0) {
                                        // 空欄
                                        setCreditZenkiIgai = null;
                                    } else {
                                        setCreditZenkiIgai = union._creditZenkiIgai;
                                    }
                                } else {
                                    setCreditZenkiIgai = union._creditZenkiIgai;
                                }
                                if (null != setCreditZenkiIgai) {
                                    printLine.setZenkiIgaiCredit(column.toString(), setCreditZenkiIgai.toString());
                                }
                            }
                        }
                    }
                }
            }
            printLine._subclassname = studyrecSubClass.studyrec()._subclassMst.subclassname();

            if (isPrintTotalCredits) {
                if (param._isPrintYoshiki2OmoteTotalCreditByPage) {
                    if (null != pageSubclassCredit) {
                        printLine._totalCredits = pageSubclassCredit.toString();
                    }
                } else {

//                                final StudyRecSubclassTotal studytotal = (StudyRecSubclassTotal) studyRecSubclassMap.get(studyrecSubClass.studyrec().getKeySubclasscdForSubclassTotal(param()));
////                              final Integer substitutionIchibuCredit = student._gakushuBiko.getStudyrecSubstitutionCredit(subclasscd, StudyrecSubstitutionContainer.ICHIBU);
//                                final boolean creditHasValue = null != studytotal && null != sum(studytotal.creditListForTotal()) && sum(studytotal.creditListForTotal()).intValue() != 0;
////                              final boolean substitutionIchibuCreditHasValue = null != substitutionIchibuCredit && substitutionIchibuCredit.intValue() != 0;
////                              if (creditHasValue || substitutionIchibuCreditHasValue) {
//                                if (creditHasValue) {
////                                  final int credit = (creditHasValue ? studyobj._credit.intValue() : 0) + (substitutionIchibuCreditHasValue ? substitutionIchibuCredit.intValue() : 0);
//                                    final String credit = (creditHasValue ? String.valueOf(sum(studytotal.creditListForTotal()).intValue()) : null);
//                                    printLine._totalCredits = credit;
//                                }
                }
            }
        }

        private static String getBiko(final Param param, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final StudyRec studyrec, final String keysubclasscd0) {
            final String minYear = printGakuseki.minYear();
            final String maxYear = printGakuseki.maxYear();


            final String compVal = getRishuTanniBiko(param, printGakuseki, studyrec._year, studyrec._compCredit, getRishuStatus(param, student, pInfo, studyrec));
            if (StringUtils.isBlank(compVal)) {
                if (null != studyrec._yomikaeMotoStudyrecList) {
                    final StringBuffer stb = new StringBuffer();
                    for (final StudyRec rsr : studyrec._yomikaeMotoStudyrecList) {
                        final RishuStatus rishuStatus = getRishuStatus(param, student, pInfo, rsr);
                        if (param._isOutputDebugSeiseki) {
                            log.info(" *** " + rsr + " => " + rishuStatus);
                        }
                        final String compVal2 = getRishuTanniBiko(param, printGakuseki, rsr._year, rsr._compCredit, rishuStatus);
                        if (!StringUtils.isBlank(compVal2)) {
                            if (!StringUtils.isBlank(stb.toString())) {
                                stb.append(" ");
                            }
                            stb.append(compVal2);
                        }
                    }
                    pInfo._gakushuBiko.putRishuTanniBiko(keysubclasscd0, studyrec._year, stb.toString());
                }
            } else {
                pInfo._gakushuBiko.putRishuTanniBiko(keysubclasscd0, studyrec._year, compVal);
            }
            final List<String> keysubclasscds = new ArrayList<String>();
            if (param._isSubclassOrderNotContainCurriculumcd && "1".equals(param._useCurriculumcd)) {
                // 異なる教育課程コードの科目に設定されている備考も含む
                final String[] split = StringUtils.split(keysubclasscd0, "-");
                final String classcdschoolKind = split[0] + "-" + split[1];
                final String subclasscd = split[3];
                final Set<String> keysubclasscdAllSet = new TreeSet<String>();
                for (final Iterator it = pInfo._gakushuBiko._biko.values().iterator(); it.hasNext();) {
                    final Map<Object, Object> bikos = (Map) it.next();
                    for (final Object key : bikos.keySet()) {
                        if (key instanceof String) {
                            final String subcd = (String) key;
                            if (null != subcd && subcd.startsWith(classcdschoolKind) && subcd.endsWith(subclasscd)) {
                                keysubclasscdAllSet.add(subcd);
                            }
                        }
                    }
                }
                keysubclasscds.addAll(keysubclasscdAllSet);
            } else {
                keysubclasscds.add(keysubclasscd0);
            }

            final StringBuffer gakushuubiko = new StringBuffer();
            final StringBuffer substitutionBikoZenbu = new StringBuffer();
            final StringBuffer substitutionBikoIchibu = new StringBuffer();
            final StringBuffer rishuTanniBiko = new StringBuffer();
            for (int i = 0; i < keysubclasscds.size(); i++) {
                final String keysubclasscd = keysubclasscds.get(i);
                gakushuubiko.append(pInfo._gakushuBiko.getStudyrecBiko(keysubclasscd, minYear, maxYear));
                if (!param.isNotPrintDaitai("2omote")) {
                    substitutionBikoZenbu.append(pInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, GakushuBiko.DAITAI_TYPE.ZENBU, minYear, maxYear));
                    substitutionBikoIchibu.append(pInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, GakushuBiko.DAITAI_TYPE.ICHIBU, minYear, maxYear));
                }
                rishuTanniBiko.append(pInfo._gakushuBiko.getRishuTanniBiko(keysubclasscd, minYear, maxYear));
            }
            final StringBuffer biko = Util.mkString(Arrays.asList(rishuTanniBiko.toString(), gakushuubiko.toString(), substitutionBikoZenbu.toString() + substitutionBikoIchibu.toString()), "、");
            return biko.toString();
        }

        private static RishuStatus getRishuStatus(final Param param, final Student student, final PersonalInfo pInfo, final StudyRec studyrec) {
            final RishuStatus rtn;
            if (studyrec.isMirishu(param) && null != param._mirishuRemarkFormat) {
                // 未履修の場合の備考処理
                rtn = RishuStatus.MIRISHU;
            } else if (studyrec.isRishuNomi(param) && null != param._rishunomiRemarkFormat) {
                // 履修のみの場合の備考処理
                rtn = RishuStatus.RISHUNOMI;
            } else {
                rtn = RishuStatus.NONE;
            }
            return rtn;
        }

        private static String getRishuTanniBiko(final Param param, final PrintGakuseki printGakuseki, final String year, final Object compCredit, final RishuStatus rishuStatus) {
            final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
            final String head = null == gakuseki ? "" : gakuseki.getGradeOrNendo(param);
            final String rtn = formatRemark(rishuStatus.formatString(param), head, compCredit);
            return rtn;
        }

        private static String formatRemark(final String format, final String gakunenNendo, final Object compCre) {
            String tmp = format;
            tmp = StringUtils.replace(tmp, "x", gakunenNendo);
            if (null != compCre) {
                tmp = StringUtils.replace(tmp, "y", compCre.toString());
            }
            return tmp;
        }

        private int svfVrsOutNullKaihi(final String field, final boolean chkNull, final String data) {
            if (!chkNull || chkNull && !StringUtils.isBlank(data)) {
                return svfVrsOut(field, data);
            }
            return -1;
        }

        private static List<BigDecimal> creditList(final Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>> creditMap, final CreditTotalKey creKey, final StudyRec.CreditKind creditKind, final String year) {
            return getMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, creKey), creditKind), year);
        }

        /**
         * 年度・学年別修得単位数を印字します。（総合的な学習の時間・小計・留学・合計）
         * @param studyrecyear
         */
        private void printTotalCredits3(final boolean isPrintTotalCredits, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final Map<String, StudyRecSubclassTotal> studyRecSubclassTotalMap, final boolean isPrintJiritsu, final List<List<String>> csvLines) {
            if (param()._isOutputDebug) {
                log.info(" -- " + "printYearCreditsTotal3" + " -- ( _isFooterInRecord3 = " + _isFooterInRecord3 + ")");
            }
//            final Map studyrecyear = student.getStudyRecYear(param(), isNewForm(param(), student, pInfo), pInfo);
            final Param param = param();
            final boolean hanki = param._isHankiNinteiForm;

            final Tuple<Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>>, Map<CreditTotalKey, TreeMap<Year, List<StudyRec>>>> creditMapAndKakkoYears = getCreditMap(pInfo, studyRecSubclassTotalMap);
            final Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>> creditMap = creditMapAndKakkoYears._first;
            final Map<CreditTotalKey, TreeMap<Year, List<StudyRec>>> kakkoYears = creditMapAndKakkoYears._second;
            if (!kakkoYears.isEmpty()) {
                for (final Map.Entry<CreditTotalKey, TreeMap<Year, List<StudyRec>>> e : kakkoYears.entrySet()) {
                    log.info(" kakkoYears = " + e.getKey() + " = " + e.getValue());
                }
            }
            final Set<String> yearSet = new HashSet<String>();
            for (final TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>> m : creditMap.values()) {
                for (final TreeMap<String, List<BigDecimal>> m2 : m.values()) {
                    yearSet.addAll(m2.keySet());
                }
            }

            if (param._isOutputDebugData) {
                log.info(" nendo Credits yearSet = " + yearSet);
            }

            // 総合的な学習の時間の備考
            String biko90 = null;
            String biko90Field = null;
            if (!param._z010.in(Z010.meiji)) {
                biko90 = getBiko90(pInfo, printGakuseki);
            }
            final int bikoKeta = getMS932ByteLength(biko90);
            if (bikoKeta > 0) {
                final List<String> biko90FieldList = new ArrayList<String>();
                if (_isFooterInRecord3) {
                    for (int n = 1; n <= 5; n++) {
                        biko90FieldList.add(hanki ? "total_biko" + String.valueOf(n) + "_HANKI" : "total_biko" + String.valueOf(n));
                    }
                } else {
                    for (int n = 1; n <= 5; n++) {
                        if (n == 1) {
                            biko90FieldList.add(hanki ? "biko_sgj_HANKI" : "biko_sgj");
                        } else {
                            biko90FieldList.add(hanki ? "biko_sgj" + String.valueOf(n) + "_HANKI" : "biko_sgj" + String.valueOf(n));
                        }
                    }
                }
                final Integer biko90FieldIdx = _form._formInfo.getPrintFieldIndex(bikoKeta, biko90FieldList);
                if (null != biko90FieldIdx) {
                    biko90Field = biko90FieldList.get(biko90FieldIdx.intValue());
                }
            }

            if (_isFooterInRecord3) {

                final List<StudyRec.KIND> lineKinds = new ArrayList<StudyRec.KIND>();
                lineKinds.add(StudyRec.KIND.SOGO90);
                if (isPrintJiritsu) {
                    lineKinds.add(StudyRec.KIND.JIRITSU);
                }
                lineKinds.add(StudyRec.KIND.SYOKEI);
                lineKinds.add(StudyRec.KIND.ABROAD);
                lineKinds.add(StudyRec.KIND.TOTAL);
                //lineKinds.add(StudyRec.KIND.SOGO94);

                // 種別ごとの単位
                for (final StudyRec.KIND lineKind : lineKinds) {
                    String title = "";
                    if (StudyRec.KIND.SOGO90 == lineKind) {
                        title = pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap);
                    } else if (StudyRec.KIND.JIRITSU == lineKind) {
                        title = "自 立 活 動";
                    } else if (StudyRec.KIND.SYOKEI == lineKind) {
                        title = "小　 　　計";
                    } else if (StudyRec.KIND.ABROAD == lineKind) {
                        title = "留　　　 学";
                    } else if (StudyRec.KIND.TOTAL == lineKind) {
                        title = "合　　　 計";
                    }
                    List<String> csvLine = null;
                    if (null != csvLines) {
                        csvLine = CsvUtils.newLine(csvLines);
                        csvLine.addAll(Arrays.asList("", title, ""));
                    } else {
                        final String titleField = "TOTAL_SUBCLASSNAME" + (param._isHankiNinteiForm ? "_HANKI" : "");
                        svfVrsOut(titleField, title);
                    }

                    final CreditTotalKey creKey;
                    if (StudyRec.KIND.SOGO90 == lineKind) {
                        creKey = CreditTotalKey.yearSubject90sContainsRyunen;
                    } else if (StudyRec.KIND.JIRITSU == lineKind) {
                        creKey = CreditTotalKey.yearJiritsu;
                    } else if (StudyRec.KIND.SYOKEI == lineKind) {
                        creKey = CreditTotalKey.yearSubjects;
                    } else if (StudyRec.KIND.ABROAD == lineKind) {
                        creKey = CreditTotalKey.yearAbroads;
                    } else if (StudyRec.KIND.SOGO94 == lineKind) {
                        creKey = CreditTotalKey.yearSubject94s;
                    } else { // if (StudyRec.KIND.TOTAL == lineKind) {
                        creKey = CreditTotalKey.yearTotals;
                    }

                    // 年度ごとの単位
                    for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                        if (!pInfo.isTargetYearLast(year, student, param, false)) {
                            continue;
                        }
                        final int col = printGakuseki._yearGakusekiPositionMap.get(year).intValue();
                        final boolean hasYear = yearSet.contains(year);
                        final boolean chkNull = true; // hasYear && (dropShowYears.contains(year) || pInfo.getDropYears(param).contains(year));
                        final String def;
                        if (StudyRec.KIND.SOGO90 == lineKind) {
                            def = param._creditsDefaultSogaku;
                        } else if (StudyRec.KIND.ABROAD == lineKind) {
                            def = param._creditsDefaultAbroad;
                        } else if (StudyRec.KIND.TOTAL == lineKind) {
                            def = student.isShowCredit0(param, pInfo, year) ? "0" : param._creditsDefaultTotal;
                        } else {
                            def = student.isShowCredit0(param, pInfo, year) ? "0" : null;
                        }

                        if (hanki) {
                            StudyRec.CreditKind creditKind;
                            String hankiSeme;

                            creditKind = StudyRec.CreditKind.CREDIT_ZENKI;
                            hankiSeme = "1"; // 前期
                            final boolean hasAnyCre1 = hasYear && !creditList(creditMap, CreditTotalKey.yearAny, creditKind, year).isEmpty();
                            if (StudyRec.KIND.SOGO90 == lineKind || Arrays.asList(StudyRec.KIND.JIRITSU, StudyRec.KIND.SYOKEI, StudyRec.KIND.ABROAD, StudyRec.KIND.TOTAL, StudyRec.KIND.SOGO94).contains(lineKind) && hasAnyCre1) {
                                svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col, chkNull, defstr(Util.bdSum(creditList(creditMap, creKey, creditKind, year)), def));
                            }

                            creditKind = StudyRec.CreditKind.CREDIT_ZENKI_IGAI;
                            hankiSeme = "2"; // 後期
                            final boolean hasAnyCre2 = hasYear && !creditList(creditMap, CreditTotalKey.yearAny, creditKind, year).isEmpty();
                            if (StudyRec.KIND.SOGO90 == lineKind || Arrays.asList(StudyRec.KIND.JIRITSU, StudyRec.KIND.SYOKEI, StudyRec.KIND.ABROAD, StudyRec.KIND.TOTAL, StudyRec.KIND.SOGO94).contains(lineKind) && hasAnyCre2) {
                                svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col, chkNull, defstr(Util.bdSum(creditList(creditMap, creKey, creditKind, year)), def));
                            }
                        } else {
                            final StudyRec.CreditKind kind = StudyRec.CreditKind.CREDIT;
                            final List<BigDecimal> creds = creditList(creditMap, creKey, kind, year);
                            if (param._isOutputDebug) {
                                log.info(" nendo Credits year = " + year + ", col = " + col + ", " + lineKind + " (" + defstr(Util.bdSum(creds), "") + ") = " + creds);
                            }

                            String cre;
                            if (StudyRec.KIND.SOGO90 == lineKind) {
                                cre = defstr(Util.bdSum(creds), param._creditsDefaultSogaku);
                            } else {
                                cre = hasYear ? defstr(Util.bdSum(creds), def) : null;
                            }
                            if (Util.getMappedMap(kakkoYears, creKey).keySet().contains(Year.of(year))) {
                                cre = kakko(cre);
                            }
                            if (null != csvLines) {
                                csvLine.addAll(Arrays.asList("/", cre));
                            } else {
                                final String field;
                                if (param._setSogakuKoteiTanni) {
                                    field = _form._formInfo.getFieldForData(Arrays.asList("tani_" + col + "_3k", "tani_" + col), cre);
                                } else {
                                    field = _form._formInfo.getFieldForData(Arrays.asList("tani_" + col, "tani_" + col + "_2"), cre);
                                }
                                svfVrsOutNullKaihi(field, chkNull, cre);
                            }
                        }
                    }

                    // 総単位数
                    if (isPrintTotalCredits) {
                        final int col = 5;

                        final Set<String> tYearSet = new HashSet<String>();
                        tYearSet.addAll(yearSet);
                        if (param._isOutputDebugData) {
                            log.info(" totalCredits yearSet = " + tYearSet);
                        }

                        final CreditTotalKey tcreKey = StudyRec.KIND.SOGO90 == lineKind ? CreditTotalKey.yearSubject90sValid : creKey;

                        final List<BigDecimal> creditList = new ArrayList<BigDecimal>();
                        for (final String year : tYearSet) {
                            if (param._isPrintYoshiki2OmoteTotalCreditByPage) {
                                if (!printGakuseki._yearGakusekiPositionMap.keySet().contains(year)) {
                                    continue;
                                }
                            }
                            if (!pInfo.isTargetYearLast(year, student, param, false)) {
                                continue;
                            }
                            final StudyRec.CreditKind kind = StudyRec.CreditKind.CREDIT;
                            creditList.addAll(creditList(creditMap, tcreKey, kind, year));
                        }

                        String def = null;
                        final boolean isShowCredit0L = student.isShowCredit0(param, pInfo, pInfo.getLastYear());
                        if (param._isOutputDebug) {
                            param.logOnce(" " + student._schregno + " isShowCredit0L = " + isShowCredit0L);
                        }

                        if (StudyRec.KIND.SOGO90 == lineKind) {
                            def = param._creditsDefaultSogaku;
                        } else if (StudyRec.KIND.JIRITSU == lineKind) {
                            def = isShowCredit0L ? "0" : null;
                        } else if (StudyRec.KIND.SYOKEI == lineKind) {
                            def = isShowCredit0L ? "0" : null;
                        } else if (StudyRec.KIND.ABROAD == lineKind) {
                            def = param._creditsDefaultAbroad;
                        } else if (StudyRec.KIND.TOTAL == lineKind) {
                            def = isShowCredit0L ? "0" : param._creditsDefaultTotal;
                        } else if (StudyRec.KIND.SOGO94 == lineKind) {
                            def = isShowCredit0L ? "0" : null;
                        }

                        final CreditTotalKey totalCreKey;
                        if (StudyRec.KIND.SOGO90 == lineKind) {
                            totalCreKey = CreditTotalKey.yearSubject90sContainsRyunen;
                        } else if (StudyRec.KIND.JIRITSU == lineKind) {
                            totalCreKey = CreditTotalKey.yearJiritsu;
                        } else if (StudyRec.KIND.SYOKEI == lineKind) {
                            totalCreKey = CreditTotalKey.yearSubjects;
                        } else if (StudyRec.KIND.ABROAD == lineKind) {
                            totalCreKey = CreditTotalKey.yearAbroads;
                        } else if (StudyRec.KIND.SOGO94 == lineKind) {
                            totalCreKey = CreditTotalKey.yearSubject94s;
                        } else { // if (StudyRec.KIND.TOTAL == lineKind) {
                            totalCreKey = CreditTotalKey.yearTotals;
                        }

                        String totalCredits = defstr(Util.bdSum(creditList), def);
                        if (!Util.getMappedMap(kakkoYears, totalCreKey).isEmpty()) {
                            totalCredits = kakko(totalCredits);
                        }
                        if (param._z010.in(Z010.aoyama) && pInfo._aoyamaGakunenSeiseki.is進級卒業できなかった() && Arrays.asList(StudyRec.KIND.SYOKEI, StudyRec.KIND.TOTAL).contains(lineKind)) {
                            totalCredits = null;
                        }
                        if (param._isOutputDebug) {
                            log.info(" credit " + lineKind + " = " + totalCredits);
                        }
                        if (null != csvLines) {
                            csvLine.add(totalCredits);
                        } else {
                            final String field;
                            if (hanki && hasField("tani_HANKI_" + col)) {
                                field = "tani_HANKI_" + col;
                            } else if (param._setSogakuKoteiTanni) {
                                field = _form._formInfo.getFieldForData(Arrays.asList("tani_" + col + "_3k", "tani_" + col), totalCredits);
                            } else {
                                svfVrAttribute("tani_" + col + "_3k", "X=10000"); // TODO: 値がなぜか二重で表示されるバグ対応
                                if (param._z010.in(Z010.aoyama)) {
                                    if (NumberUtils.isDigits(totalCredits)) {
                                        field = _form._formInfo.getFieldForData(Arrays.asList("tani_" + col, "tani_" + col + "_2"), totalCredits);
                                    } else {
                                        field = "tani_" + col + "_2";
                                    }
                                } else {
                                    field = "tani_" + col;
                                }
                            }
                            svfVrsOutNotNull(field, totalCredits);
                        }
                    }

                    // 備考
                    final String bikoSlashField = hanki ? "bikoSlash_HANKI" : "bikoSlash";
                    if (StudyRec.KIND.SOGO90 == lineKind) {
                        if (null != csvLines) {
                            csvLine.add(biko90);
                        } else if (null != biko90Field) {
                            svfVrsOut(biko90Field, biko90);
                        }
                    } else if (StudyRec.KIND.ABROAD == lineKind) {
                        if (param._isOutputDebug) {
                            log.info(" isPrintYoshiki2OmoteDropAbroadBiko = " + param._isPrintYoshiki2OmoteDropAbroadBiko + " | dropGakuseki = " + printGakuseki._dropGakuseki);
                        }
                        boolean print = false;
                        if (param._isPrintYoshiki2OmoteDropAbroadBiko) {
                            final StudyRec.AbroadStudyRec abroadStudyrec = printGakuseki.getAbroadStudyRec(pInfo._studyRecList);
                            if (null != csvLines) {
                                csvLine.add(abroadStudyrec._remark1);
                            } else if (null != abroadStudyrec) {
                                svfVrsOut("total_biko1", abroadStudyrec._remark1);
                                print = true;
                            }
                        }
                        if (!print) {
                            if (null != csvLines) {
                                csvLine.add("/");
                            } else {
                                svfVrImageOut(bikoSlashField, param._slashImagePath);
                            }
                        }
                    } else if (StudyRec.KIND.TOTAL == lineKind) {
                        boolean print = false;
                        if (param._isPrintYoshiki2OmoteDropAbroadBiko) {
                            final Gakuseki dropGakuseki = printGakuseki._dropGakuseki;
                            if (null != dropGakuseki) {
                                final String comment;
                                if (param._schoolDiv.isTanniSei(dropGakuseki._year, null, student)) {
                                    comment = dropGakuseki._nendo + "　" + Util.append(toDigit(dropGakuseki._annual, ""), "年次") + "　再学年";
                                } else {
                                    comment = dropGakuseki._nendo + "　" + dropGakuseki._gdat._gradeName2 + "原級留置";
                                }
                                if (null != csvLines) {
                                    csvLine.add(comment);
                                } else {
                                    svfVrsOut("total_biko1", comment);
                                }
                                print = true;
                            }
                        }
                        if (!print) {
                            if (null != csvLines) {
                                csvLine.add("/");
                            } else {
                                svfVrImageOut(bikoSlashField, param._slashImagePath);
                            }
                        }
                    } else {
                        if (null != csvLines) {
                            csvLine.add("/");
                        } else {
                            svfVrImageOut(bikoSlashField, param._slashImagePath);
                        }
                    }
                    svfVrEndRecord();
                }

            } else {
                svfVrsOut("TOTAL_SUBCLASSNAME", pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap));
                for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                    if (!pInfo.isTargetYearLast(year, student, param, false)) {
                        continue;
                    }
                    final int col = printGakuseki._yearGakusekiPositionMap.get(year).intValue();
                    final boolean hasYear = yearSet.contains(year);
                    final boolean chkNull = true; // hasYear && (dropShowYears.contains(year) || pInfo._dropYears.contains(year));
                    final boolean isShowCredit0 = student.isShowCredit0(param, pInfo, year);
                    final String def = isShowCredit0 ? "0" : null;
                    if (hanki) {
                        StudyRec.CreditKind kind;
                        String hankiSeme;

                        kind = StudyRec.CreditKind.CREDIT_ZENKI;
                        hankiSeme = "1"; // 前期
                        final boolean hasAnyCre1 = hasYear && !getMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, CreditTotalKey.yearAny), kind), year).isEmpty();
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_sgj", chkNull,          defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubject90sContainsRyunen, kind, year)), param._creditsDefaultSogaku));
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_rg", chkNull, hasAnyCre1 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearAbroads, kind, year)), param._creditsDefaultAbroad) : null);
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_sk", chkNull, hasAnyCre1 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubjects, kind, year)), def) : null);
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_gk", chkNull, hasAnyCre1 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearTotals, kind, year)), isShowCredit0 ? "0" : param._creditsDefaultTotal) : null);
                        if (hasField("tani_HANKI" + hankiSeme + "_" + col + "_hr")) {
                            svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_hr", chkNull, hasYear ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubject94s, kind, year)), def) : null);
                        }

                        kind = StudyRec.CreditKind.CREDIT_ZENKI_IGAI;
                        hankiSeme = "2"; // 後期
                        final boolean hasAnyCre2 = hasYear && !creditList(creditMap, CreditTotalKey.yearAny, kind, year).isEmpty();
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_sgj", chkNull,          defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubject90sContainsRyunen, kind, year)), param._creditsDefaultSogaku));
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_rg", chkNull, hasAnyCre2 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearAbroads, kind, year)), param._creditsDefaultAbroad) : null);
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_sk", chkNull, hasAnyCre2 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubjects, kind, year)), def) : null);
                        svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_gk", chkNull, hasAnyCre2 ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearTotals, kind, year)), def) : isShowCredit0 ? "0" : param._creditsDefaultTotal);
                        if (hasField("tani_HANKI" + hankiSeme + "_" + col + "_hr")) {
                            svfVrsOutNullKaihi("tani_HANKI" + hankiSeme + "_" + col + "_hr", chkNull, hasYear ? defstr(Util.bdSum(creditList(creditMap, CreditTotalKey.yearSubject94s, kind, year)), def) : null);
                        }
                    } else {
                        final StudyRec.CreditKind kind = StudyRec.CreditKind.CREDIT;
                        final List<BigDecimal> subject90 = creditList(creditMap, CreditTotalKey.yearSubject90sContainsRyunen, kind, year);
                        final List<BigDecimal> subjects = creditList(creditMap, CreditTotalKey.yearSubjects, kind, year);
                        final List<BigDecimal> abroads = creditList(creditMap, CreditTotalKey.yearAbroads, kind, year);
                        final List<BigDecimal> totals = creditList(creditMap, CreditTotalKey.yearTotals, kind, year);
                        final List<BigDecimal> subject94 = creditList(creditMap, CreditTotalKey.yearSubject94s, kind, year);
                        if (param._isOutputDebug) {
                            log.info(" nendo Credits year = " + year + ", col = " + col + ", credits = " + subject90 + ", " + abroads + ", " + subjects + ", " + totals + ", " + subject94);
                        }

                        svfVrsOutNullKaihi("tani_" + col + "_sgj", chkNull, defstr(Util.bdSum(subject90), param._creditsDefaultSogaku));
                        svfVrsOutNullKaihi("tani_" + col + "_rg", chkNull, hasYear ? defstr(Util.bdSum(abroads), param._creditsDefaultAbroad) : null);
                        svfVrsOutNullKaihi("tani_" + col + "_sk", chkNull, hasYear ? defstr(Util.bdSum(subjects), def) : null);
                        svfVrsOutNullKaihi("tani_" + col + "_gk", chkNull, hasYear ? defstr(Util.bdSum(totals), isShowCredit0 ? "0" : param._creditsDefaultTotal) : null);
                        if (hasField("tani_" + col + "_hr")) {
                            svfVrsOutNullKaihi("tani_" + col + "_hr", chkNull, hasYear ? defstr(Util.bdSum(subject94), def) : null);
                        }
                    }
                }
                if (null != biko90Field) {
                    svfVrsOut(biko90Field, biko90);
                }
                if (isPrintTotalCredits) {
                    final List<BigDecimal> tSubject90s = new ArrayList<BigDecimal>();
                    final List<BigDecimal> tSubjects = new ArrayList<BigDecimal>();
                    final List<BigDecimal> tAbroads = new ArrayList<BigDecimal>();
                    final List<BigDecimal> tTotals = new ArrayList<BigDecimal>();
                    final List<BigDecimal> tSubject94s = new ArrayList<BigDecimal>();

                    final Set<String> tYearSet = new HashSet<String>();
                    tYearSet.addAll(yearSet);
                    if (param._isOutputDebugData) {
                        log.info(" totalCredits yearSet = " + tYearSet);
                    }

                    for (final String year : tYearSet) {
                        if (param._isPrintYoshiki2OmoteTotalCreditByPage) {
                            if (!printGakuseki._yearGakusekiPositionMap.keySet().contains(year)) {
                                continue;
                            }
                        }
                        if (!pInfo.isTargetYearLast(year, student, param, false)) {
                            continue;
                        }
                        final StudyRec.CreditKind kind = StudyRec.CreditKind.CREDIT;
                        tSubject90s.addAll(creditList(creditMap, CreditTotalKey.yearSubject90sValid, kind, year));
                        tSubjects.addAll(creditList(creditMap, CreditTotalKey.yearSubjects, kind, year));
                        tAbroads.addAll(creditList(creditMap, CreditTotalKey.yearAbroads, kind, year));
                        tTotals.addAll(creditList(creditMap, CreditTotalKey.yearTotals, kind, year));
                        tSubject94s.addAll(creditList(creditMap, CreditTotalKey.yearSubject94s, kind, year));
                    }

                    final int col = 5;
                    final String fieldSgj = hanki ? "tani_HANKI_" + col + "_sgj" : "tani_" + col + "_sgj";
                    final String fieldSk = hanki ? "tani_HANKI_" + col + "_sk" : "tani_" + col + "_sk";
                    final String fieldRg = hanki ? "tani_HANKI_" + col + "_rg" : "tani_" + col + "_rg";
                    final String fieldGk = hanki ? "tani_HANKI_" + col + "_gk" : "tani_" + col + "_gk";
                    final String fieldHr = hanki ? "tani_HANKI_" + col + "_hr" : "tani_" + col + "_hr";
                    final String dummy = hanki ? "DUMMY_CRE_HANKI" : "DUMMY_CRE";
                    svfVrsOutNotNull(fieldSgj, defstr(Util.bdSum(tSubject90s), param._creditsDefaultSogaku));
                    final boolean isShowCredit0L = student.isShowCredit0(param, pInfo, pInfo.getLastYear());
                    svfVrsOutNotNull(fieldSk, defstr(Util.bdSum(tSubjects), isShowCredit0L ? "0" : null));
                    svfVrsOutNotNull(fieldRg, defstr(Util.bdSum(tAbroads), param._creditsDefaultAbroad));
                    svfVrsOutNotNull(fieldGk, defstr(Util.bdSum(tTotals), isShowCredit0L ? "0" : param._creditsDefaultTotal));
                    if (hasField(fieldHr)) {
                        svfVrsOutNotNull(fieldHr, defstr(Util.bdSum(tSubject94s), isShowCredit0L ? "0" : null));
                    }
                    svfVrsOut(dummy, "1");
                }
            }
        }

        private Tuple<Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>>, Map<CreditTotalKey, TreeMap<Year, List<StudyRec>>>> getCreditMap(final PersonalInfo pInfo, final Map<String, StudyRecSubclassTotal> studyRecSubclassTotalMap) {

            final List<StudyRec.CreditKind> printKindList = new ArrayList<StudyRec.CreditKind>();
            printKindList.add(StudyRec.CreditKind.CREDIT); // 修得単位数
            final Param param = param();
            final boolean hanki = param._isHankiNinteiForm;
            if (hanki) {
                printKindList.add(StudyRec.CreditKind.CREDIT_ZENKI);
                printKindList.add(StudyRec.CreditKind.CREDIT_ZENKI_IGAI);
            }

            final List<StudyRecSubclassTotal> studyRecSubclassTotalList = new ArrayList<StudyRecSubclassTotal>(studyRecSubclassTotalMap.values());
            Collections.sort(studyRecSubclassTotalList, new StudyRecSubclassTotal.Comparator(param(), YOSHIKI.NONE));

            final Map<CreditTotalKey, TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>>> creditMap = new HashMap();
            Util.getMappedMap(creditMap, CreditTotalKey.yearSubject90sContainsRyunen); // 年度ごとの総合的な学習の時間は留年していても表示
            Util.getMappedMap(creditMap, CreditTotalKey.yearSubject90sValid);
            Util.getMappedMap(creditMap, CreditTotalKey.yearJiritsu);
            Util.getMappedMap(creditMap, CreditTotalKey.yearSubjects);
            Util.getMappedMap(creditMap, CreditTotalKey.yearAbroads);
            Util.getMappedMap(creditMap, CreditTotalKey.yearTotals);
            Util.getMappedMap(creditMap, CreditTotalKey.yearSubject94s);
            for (final TreeMap<StudyRec.CreditKind, TreeMap<String, List<BigDecimal>>> m : creditMap.values()) {
                for (final StudyRec.CreditKind printKind : printKindList) {
                    Util.getMappedMap(m, printKind);
                }
            }

            //final Collection dropShowYears = Student.getDropShowYears(param, pInfo);

            final Map<CreditTotalKey, TreeMap<Year, List<StudyRec>>> kakkoYearsStudyrecListMap = new TreeMap<CreditTotalKey, TreeMap<Year, List<StudyRec>>>();
            for (final StudyRecSubclassTotal sst : studyRecSubclassTotalList) {

                final StudyRecCreditTargetPredicate nendoTotalPredicate = new StudyRecCreditTargetPredicate(param, pInfo, 0);
                final StudyRecCreditTargetPredicate subclassTotalPredicate = new StudyRecCreditTargetPredicate(param, pInfo, 1);
                for (final StudyRec.CreditKind printKind : printKindList) {
                    final List<StudyRec> targetStudyRecList0 = StudyRec.getTargetStudyRecList(sst._studyrecList, sst._dropYears, sst._printDropRegdYears, null, 1);
                    final List<StudyRec> targetStudyRecList = Util.filter(targetStudyRecList0, nendoTotalPredicate);
                    final List<StudyRec> kakko = Util.filter(targetStudyRecList0, new NotPredicate<StudyRec>(subclassTotalPredicate));
                    final List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> creditKindYearMapList = sst.creditForTotalMapList(param, printKind, targetStudyRecList);
                    final List<StudyRec.KIND> kindList = sst.studyrec().kindList(param, pInfo);
                    if (kindList.contains(StudyRec.KIND.SOGO90)) {
                        final int checkDropYears90s = param._z010.in(Z010.musashinohigashi) ? 0 : 2;
                        CreditTotalKey key;
                        key = CreditTotalKey.yearSubject90sContainsRyunen;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), sst.creditKindYearMapList(param, printKind, null, checkDropYears90s, false));
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                        key = CreditTotalKey.yearSubject90sValid;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.JIRITSU)) {
                        final CreditTotalKey key = CreditTotalKey.yearJiritsu;
                        if (StudyRecSubclassTotal.isEnabledJiritsu(param(), sst)) {
                            putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        }
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.SYOKEI)) {
                        final CreditTotalKey key = CreditTotalKey.yearSubjects;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.ABROAD)) {
                        final CreditTotalKey key = CreditTotalKey.yearAbroads;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.TOTAL)) {
                        final CreditTotalKey key = CreditTotalKey.yearTotals;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.SOGO94)) {
                        final CreditTotalKey key = CreditTotalKey.yearSubject94s;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                    if (kindList.contains(StudyRec.KIND.SOGO90) || kindList.contains(StudyRec.KIND.JIRITSU) || kindList.contains(StudyRec.KIND.SYOKEI) || kindList.contains(StudyRec.KIND.ABROAD) || kindList.contains(StudyRec.KIND.TOTAL) || kindList.contains(StudyRec.KIND.SOGO94)) {
                        final CreditTotalKey key = CreditTotalKey.yearAny;
                        putAllToMappedList(Util.getMappedMap(Util.getMappedMap(creditMap, key), printKind), creditKindYearMapList);
                        for (final StudyRec s : kakko) {
                            getMappedList(Util.getMappedMap(kakkoYearsStudyrecListMap, key), Year.of(s._year)).add(s);
                        }
                    }
                }
            }
            return Tuple.of(creditMap, kakkoYearsStudyrecListMap);
        }

        private static void putAllToMappedList(final Map<String, List<BigDecimal>> yearCreditListMap, final List<Map<String, Map<StudyRec.CreditKind, BigDecimal>>> yearCreditMapList) {
            for (final Map<String, Map<StudyRec.CreditKind, BigDecimal>> m : yearCreditMapList) {
                for (final Map.Entry<String, Map<StudyRec.CreditKind, BigDecimal>> e : m.entrySet()) {
                    final String year = e.getKey();
                    final Map<StudyRec.CreditKind, BigDecimal> keyCreditMap = e.getValue();
                    getMappedList(yearCreditListMap, year).addAll(keyCreditMap.values());
                }
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGakuseki3(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            if (null != csvLines) {
                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).add("様式２（指導に関する記録）");

                final CertifSchoolDat certifSchoolDat = student.certifSchool(param());
                final List<List<String>> nameLines = new ArrayList<List<String>>();
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines).addAll(Arrays.asList("生徒氏名", pInfo.getPrintName1(), "", "学校名", certifSchoolDat._schoolName1, "", "", ""));
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines);

                csvLines.addAll(CsvUtils.horizontalUnionLines(nameLines, getCsvGakusekiLines(pInfo, printGakuseki)));

                CsvUtils.newLine(csvLines).addAll(Arrays.asList("", "各教科・科目等の学習の記録"));

                final List<String> line1 = CsvUtils.newLine(csvLines);
                line1.addAll(Arrays.asList("", "教科名", "科目名"));

                for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                    if (null == gakuseki) {
                        log.warn(" no gakuseki : year = " + year  + " / years = " + printGakuseki._yearGakusekiMap.keySet());
                        continue;
                    }
                    String val = null;
                    if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                        val = gakuseki._gdat._gradeName2;
                    } else if (gakuseki._isKoumokuGakunen) {
                        if (param()._is133m) {
                            val = gakuseki._gakunenDaitukiM;
                        } else {
                            val = gakuseki._gdat._gradeName2;
                        }
                    } else {
                        val = gakuseki.getNendo2(param());
                    }
                    line1.add("　　　　" + val);
                    line1.add("");
                }

                final List<String> line2 = CsvUtils.newLine(csvLines);
                line2.addAll(Arrays.asList("", "", ""));

                for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                    if (null == gakuseki) {
                        log.warn(" no gakuseki : year = " + year  + " / years = " + printGakuseki._yearGakusekiMap.keySet());
                        continue;
                    }
                    line2.add("評定");
                    line2.add("修得単位数");
                }

                line2.add("修得単位数の計"); // 科目別修得単位数
                line2.add("備考");

                return;
            }
            svfVrsOut("GRADENAME1", pInfo._title);
            svfVrsOut("GRADENAME2", pInfo._title);
            if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                printName(pInfo, "NAME1", null, null, _name);
            } else if (param()._z010.in(Z010.naraken)) {
                printName1(null, PersonalInfo.HistVal.of(pInfo.getPrintName1(), pInfo._studentNameHistFirst), null, _name);
            } else {
                printName(pInfo, "NAME1", "NAME2", "NAME3", _name);
            }

            if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("ATTENDNO_1", student._schregno);
                } else if (param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("SCHREGNO", student._schregno);
                }

                if (param()._useStudyrecReplaceDat) {
                    svfVrsOutForData(Arrays.asList("FINSCHOOL1",  "FINSCHOOL2"), student._formerRegSchoolFinschoolname);
                }
                svfVrsOutForData(Arrays.asList("SCHOOLNAME1",  "SCHOOLNAME2"), student.certifSchool(param())._schoolName1);

            } else {
                printSchoolName(student);

                if (param()._z010.in(Z010.meiji)) {
                    svfVrsOut("TOTAL_STUDY_NAME", "Catholic Spirit");
                    final String text = "教育課程の特例として文科省より認可。総合的な学習の時間と特別活動を削減して、これに充てる。";
                    final List<String> token = Util.getTokenList(param(), text, 52, 2);
                    for (int i = 0; i < token.size(); i++) {
                        svfVrsOut("biko_sgj" + (i + 2), token.get(i));
                    }
                }
            }

            for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                final Integer ii = printGakuseki._yearGakusekiPositionMap.get(year);
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                if (null == gakuseki) {
                    log.warn(" no gakuseki : year = " + year  + " / years = " + printGakuseki._yearGakusekiMap.keySet());
                    continue;
                }

                final String i = ii.toString();
                if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                    svfVrsOut("GRADE1_" + i, gakuseki._gdat._gradeName2);
                } else if (param()._is133m && param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("HGRADE1_" + i, gakuseki._nendo);
                } else if (gakuseki._isKoumokuGakunen) {
                    svfVrsOut("GRADE1_" + i, gakuseki._gdat._gakunenSimple);
                } else {
                    svfVrsOut("GRADE3_" + i, gakuseki._nendo);
                }

                if (param()._is133m && param()._z010.in(Z010.kumamoto) && gakuseki._isStudyrecGakusekiM) {
                } else {
                    // ホームルーム
                    svfVrsOutForData(Arrays.asList("HR_CLASS1_" + i,  "HR_CLASS2_" + i), gakuseki._hdat._hrname);
                    if (param()._is133m) {
                        svfVrsOut("ATTENDNO_" + i, student._schregno);
                    } else {
                        svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
                    }
                }

                String val = null;
                if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                    val = gakuseki._gdat._gradeName2;
                } else if (gakuseki._isKoumokuGakunen) {
                    if (param()._is133m) {
                        val = gakuseki._gakunenDaitukiM;
                    } else {
                        val = gakuseki._gdat._gradeName2;
                    }
                } else {
                    val = gakuseki.getNendo2(param());
                }
                final List<String> field;
                if (param()._isHankiNinteiForm) {
                    field = Arrays.asList("GRADE2_HANKI_" + i);
                } else {
                    field = Arrays.asList("GRADE2_" + i, "GRADE2_" + i + "_2");
                }
                svfVrsOutForData(field, val);
            }
            if (param()._isHankiNinteiForm) {
                for (final String year : printGakuseki._yearGakusekiPositionMap.keySet()) {
                    final Integer ii = printGakuseki._yearGakusekiPositionMap.get(year);

                    for (int n = 1; n <= 2; n++) {
                        final String seme = String.valueOf(n);
                        String semesterName = param().getSemesterName(year, seme);
                        if (null == semesterName) {
                            if (n == 1) {
                                semesterName = "前期";
                            } else if (n == 2) {
                                semesterName = "後期";
                            }
                        }
                        svfVrsOut("SEMESTERNAME_HANKI_" + ii.toString() + "_" + seme, semesterName);
                    }
                }
            }
        }

        private static List<StudyrecSpecialDiv> getStudyrecSpecialDivList3(final List<StudyRec> studyrecList, final Param param) {
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = new ArrayList<StudyrecSpecialDiv>();
            for (final StudyRec studyrec : studyrecList) {
                if (null == studyrec._classMst._classcd || null == studyrec._subclassMst._subclasscd) {
                    continue;
                }
                final StudyrecSpecialDiv ssd = getStudyrecSpecialDiv3(studyrecSpecialDivList, studyrec._classMst._specialDiv);
                final StudyrecClass sc = getStudyrecClass3(ssd._studyrecClassList, studyrec._classMst, param);
                final StudyrecSubClass ssc = getStudyrecSubClass3(sc._studyrecSubclassList, studyrec._subclassMst, param);
                ssc._studyrecList.add(studyrec);
            }
            return studyrecSpecialDivList;
        }

        private static StudyrecSpecialDiv getStudyrecSpecialDiv3(final List<StudyrecSpecialDiv> list, final String specialDiv) {
            StudyrecSpecialDiv studyrecSpecialDiv = null;
            for (final StudyrecSpecialDiv studyrecSpecialDiv0 : list) {
                if (specialDiv.equals(studyrecSpecialDiv0.studyrec()._classMst._specialDiv)) {
                    studyrecSpecialDiv = studyrecSpecialDiv0;
                    break;
                }
            }
            if (null == studyrecSpecialDiv) {
                studyrecSpecialDiv = new StudyrecSpecialDiv();
                list.add(studyrecSpecialDiv);
            }
            return studyrecSpecialDiv;
        }

        private static StudyrecClass getStudyrecClass3(final List<StudyrecClass> list, final ClassMst classMst, final Param param) {
            StudyrecClass studyrecClass = null;
            for (final StudyrecClass studyrecClass0 : list) {
                if (ClassMst.isSameKey(param, classMst, studyrecClass0.studyrec()._classMst)) {
                    studyrecClass = studyrecClass0;
                    break;
                }
            }
            if (null == studyrecClass) {
                studyrecClass = new StudyrecClass(param);
                list.add(studyrecClass);
            }
            return studyrecClass;
        }

        private static StudyrecSubClass getStudyrecSubClass3(final List<StudyrecSubClass> list, final SubclassMst subclassMst, final Param param) {
            StudyrecSubClass studyrecSubClass = null;
            for (final StudyrecSubClass studyrecSubClass0 : list) {
                if (SubclassMst.isSameKey(param, subclassMst, studyrecSubClass0.studyrec()._subclassMst)) {
                    studyrecSubClass = studyrecSubClass0;
                    break;
                }
            }
            if (null == studyrecSubClass) {
                studyrecSubClass = new StudyrecSubClass(param);
                list.add(studyrecSubClass);
            }
            return studyrecSubClass;
        }

        private static class PrintLine {
            final int _linex;
            String _edudiv;
            String _specialDiv;
            String _classcd;
            String _classname;
            String _subclasscd;
            String _subclassname;
            String _biko;
            String _totalCredits;
            boolean _isClassFirstLine;
            final Map<Integer, String> _kantenMap = new HashMap<Integer, String>();
            final Map<Integer, String> _creditsMap = new HashMap<Integer, String>();
            final Map<Integer, String> _valuationMap = new HashMap();
            final Map<Integer, String> _valuationAttributeMap = new HashMap();
            final Map<String, String> _creditsHankiMap = new HashMap<String, String>();
            final Map<String, String> _valuationHankiMap = new HashMap<String, String>();
            int _bikoi;
            String _bikoField;

            List<StudyRec> _studyrecList = Collections.emptyList();

            public static PrintLine create(final int linex, final String specialDiv) {
                PrintLine printLine = new PrintLine(linex);
                printLine._specialDiv = specialDiv;
                return printLine;
            }

            private PrintLine(final int linex) {
                _linex = linex;
            }

            public void setKanten(final int colno, final String kanten) {
                _kantenMap.put(colno, kanten);
            }

            public String getKanten(final int colno) {
                return _kantenMap.get(colno);
            }

            public void setCredit(final int colno, final String credits) {
                _creditsMap.put(colno, credits);
            }

            public void setZenkiCredit(final String colno, final String credits) {
                _creditsHankiMap.put(colno + "_1", credits);
            }

            public String getZenkiCredit(final String colno) {
                return _creditsHankiMap.get(colno + "_1");
            }

            public void setZenkiIgaiCredit(final String colno, final String credits) {
                _creditsHankiMap.put(colno + "_2", credits);
            }

            public String getZenkiIgaiCredit(final String colno) {
                return _creditsHankiMap.get(colno + "_2");
            }

            public void setValuation(final int colno, final String valuation) {
                _valuationMap.put(colno, valuation);
            }

            public void setZenkiValuation(final String colno, final String valuation) {
                _valuationHankiMap.put(colno + "_1", valuation);
            }

            public String getZenkiValuation(final String colno) {
                return _valuationHankiMap.get(colno + "_1");
            }

            public void setZenkiIgaiValuation(final String colno, final String valuation) {
                _valuationHankiMap.put(colno + "_2", valuation);
            }

            public String getZenkiIgaiValuation(final String colno) {
                return _valuationHankiMap.get(colno + "_2");
            }

            public Object clone() {
                PrintLine l = new PrintLine(_linex);
                l._edudiv = _edudiv;
                l._specialDiv = _specialDiv;
                l._classcd = _classcd;
                l._classname = _classname;
                l._subclasscd = _subclasscd;
                l._subclassname = _subclassname;
                l._bikoi = _bikoi;
                l._totalCredits = _totalCredits;
                l._isClassFirstLine = _isClassFirstLine;
                l._creditsMap.putAll(_creditsMap);
                l._creditsHankiMap.putAll(_creditsHankiMap);
                l._valuationMap.putAll(_valuationMap);
                l._valuationAttributeMap.putAll(_valuationAttributeMap);
                l._valuationHankiMap.putAll(_valuationHankiMap);
                l._kantenMap.putAll(_kantenMap);
                l._biko = _biko;
                l._bikoField = _bikoField;
                l._studyrecList = Collections.EMPTY_LIST == _studyrecList ? Collections.EMPTY_LIST : new ArrayList(_studyrecList);
                return l;
            }

            public String toString() {
                return "[" + String.valueOf(_linex) + " : " + _classcd + ":" + _classname + ":" + _subclassname + "]";
            }
        }

        private static class StudyrecSpecialDiv {
            final List<StudyrecClass> _studyrecClassList = new ArrayList<StudyrecClass>();
            private StudyRec studyrec() {
                return (_studyrecClassList.get(0)).studyrec();
            }
        }

        private static class StudyrecClass implements Comparable<StudyrecClass> {
            final List<StudyrecSubClass> _studyrecSubclassList = new ArrayList<StudyrecSubClass>();
            final Param _param;
            StudyrecClass(final Param param) {
                _param = param;
            }
            private StudyRec studyrec() {
                return (_studyrecSubclassList.get(0)).studyrec();
            }
            public int compareTo(final StudyrecClass that) {
                return ClassMst.compareOrder(_param, studyrec()._classMst, that.studyrec()._classMst);
            }
        }

        private static class StudyrecSubClass implements Comparable<StudyrecSubClass> {
            final Param _param;
            final List<StudyRec> _studyrecList = new ArrayList<StudyRec>();
            StudyrecSubClass(final Param param) {
                _param = param;
            }
            private Map<String, List<StudyRec>> getYearStudyrecListMap() {
                final Map<String, List<StudyRec>> map = new HashMap<String, List<StudyRec>>();
                for (final StudyRec studyrec : _studyrecList) {
                    getMappedList(map, studyrec._year).add(studyrec);
                }
                return map;
            }
            private StudyRec studyrec() {
                return _studyrecList.get(_studyrecList.size() - 1);
            }
            public int compareTo(final StudyrecSubClass that) {
                return SubclassMst.compareOrder(_param, studyrec()._subclassMst, that.studyrec()._subclassMst);
            }
        }

        private static enum RishuStatus {

            RISHUNOMI, MIRISHU, NONE;

            public String formatString(Param param) {
                switch (this) {
                case RISHUNOMI:
                    return param._rishunomiRemarkFormat;
                case MIRISHU:
                    return param._mirishuRemarkFormat;
                case NONE:
                default:
                    return "";
                }
            }
        }

        private List<PrintLine> getPrintLineList3M(
                final boolean isPrintTotalCredits,
                final Student student,
                final PersonalInfo pInfo,
                final int XMAX,
                final Map<String, StudyRecSubclassTotal> studyRecSubclassMap,
                final TreeSet<String> yearSet) {
            final List<StudyRec> studyrecList = new ArrayList<StudyRec>();

            final List<StudyRec> list = new ArrayList();
            list.addAll(pInfo._studyRecList);
            list.addAll(pInfo.getStudyRecReplaceSateiAriList(param(), _yoshiki));
            Collections.sort(list, new StudyRec.StudyrecComparator(param(), _yoshiki));
            for (final StudyRec studyrec : list) {
                if (!SCHOOLCD1.equals(studyrec._schoolcd) && !yearSet.contains(studyrec._year)) {
                    continue;
                }
                if (StudyRec.isNotPrint(param(), studyrec, _yoshiki)) {
                    continue;
                }
                studyrecList.add(studyrec);
                if (param()._isOutputDebugInner) {
                    log.info(" linelist3m add " + studyrec.toString(param(), _yoshiki));
                }
            }

            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            final List<StudyrecSpecialDiv> studyrecSpecialDivList = getStudyrecSpecialDivList3M(studyrecList, param());

            List<PrintLine> outputLineList = new ArrayList<PrintLine>();
            int linex = 0; // 行数

            String specialDiv = "";
            for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                specialDiv = studyrecSpecialDiv.studyrec()._classMst._specialDiv;
                final String specialName = param().getSpecialDivName(KNJA130_0.isNewForm(param(), pInfo), studyrecSpecialDiv.studyrec()._classMst._specialDiv);

                int lineSpecialDiv = 0; // 普通・専門毎の行数
                // 教科毎の表示
                for (final StudyrecClass studyrecClass : studyrecSpecialDiv._studyrecClassList) {

                    // 総合的な学習の時間・留学は回避します。
                    if (_90.equals(studyrecClass.studyrec()._classMst._classcd) || _ABROAD.equals(studyrecClass.studyrec()._classMst._classname)) {
                        continue;
                    }
                    int lineClasscd = 0;
                    // 科目毎の表示
                    for (final StudyrecSubClass studyrecSubclass : studyrecClass._studyrecSubclassList) {

                        final PrintLine outputLine = new PrintLine(linex);
                        outputLineList.add(outputLine);
                        outputLine._studyrecList = studyrecSubclass._studyrecList;
                        final StudyRec studyrec0 = outputLine._studyrecList.get(0);
                        if (isPrintTotalCredits) {
                            if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                                BigDecimal pageSubclassCredit = null;
                                for (final StudyRec studyrec : outputLine._studyrecList) {
                                    if (yearBetween(studyrec._year, minYear, maxYear)) {
                                        pageSubclassCredit = addNumber(pageSubclassCredit, studyrec._credit);
                                    }
                                }
                                outputLine._totalCredits = null == pageSubclassCredit ? null : pageSubclassCredit.toString();
                            } else { // if (lastyearflg) {
                                final String keysubclasscd = getSubclasscdM(studyrec0.subclassMst(param(), _yoshiki), param());
                                final StudyRecSubclassTotal studyobj = studyRecSubclassMap.get(keysubclasscd);
//                              final Integer substitutionIchibuCredit = student._gakushuBiko.getStudyrecSubstitutionCredit(subclasscd, StudyrecSubstitutionContainer.ICHIBU);
                                final boolean creditHasValue = null != studyobj && !studyobj.kindCreditsM(StudyRec.CreditKind.CREDIT, _yoshiki).isEmpty();
//                              final boolean substitutionIchibuCreditHasValue = null != substitutionIchibuCredit && substitutionIchibuCredit.intValue() != 0;
//                              if (creditHasValue || substitutionIchibuCreditHasValue) {
                                if (creditHasValue) {
//                                  final int credit = (creditHasValue ? studyobj._credit.intValue() : 0) + (substitutionIchibuCreditHasValue ? substitutionIchibuCredit.intValue() : 0);
                                    final int credit = (creditHasValue ? Util.bdSum(studyobj.kindCreditsM(StudyRec.CreditKind.CREDIT, YOSHIKI._2_OMOTE)).intValue() : 0);
                                    outputLine._totalCredits = String.valueOf(credit); // 科目別修得単位数
                                }
                            }
                            if (param()._isOutputDebug) {
                                log.info(" yoshiki2 (" + studyrec0.isSaki(param(), _yoshiki) + ") subclass = " + getSubclasscdM(studyrec0.subclassMst(param(), _yoshiki), param()));
                            }
                        }

                        for (final StudyRec studyrec : outputLine._studyrecList) {
                            final List<String> biko = new ArrayList<String>();
                            biko.addAll(getBikoM(student, pInfo, minYear, maxYear, getSubclasscdM(studyrec.subclassMst(param(), _yoshiki), param()), studyrec._subclassMstSaki.subclassname()));
                            if (studyrec.isSaki(param(), _yoshiki) && !studyrec._subclassMst.getKey(param()).equals(studyrec._subclassMstSaki.getKey(param()))) {
                                // KNJE066で入力するとこちらに入力される
                                biko.addAll(getBikoM(student, pInfo, minYear, maxYear, getSubclasscdM(studyrec._subclassMst, param()), studyrec._subclassMst.subclassname()));
                            }
                            outputLine._biko = Util.mkString(biko, "、").toString();
                            // 学年ごとの出力
                            outputLine._classcd = studyrecClass.studyrec()._classMst._classcd;
                            outputLine._subclassname = studyrec.subclassMst(param(), _yoshiki).subclassname();
                        }
                        if (lineClasscd < studyrecClass.studyrec()._classMst._classname.length()) {
                            outputLine._classname = studyrecClass.studyrec()._classMst._classname.substring(lineClasscd, lineClasscd + 1);
                        }
                        outputLine._classcd = studyrecClass.studyrec()._classMst._classcd;
                        lineClasscd++;
                        if (lineSpecialDiv < specialName.length()) {
                            outputLine._edudiv = specialName.substring(lineSpecialDiv, lineSpecialDiv + 1);
                        }
                        outputLine._specialDiv = specialDiv;
                        lineSpecialDiv++;
                        linex++;
                    }

                    boolean outputNokori = false;
                    while (lineClasscd < studyrecClass.studyrec()._classMst._classname.length()) {
                        final PrintLine outputLine = new PrintLine(linex);
                        outputLineList.add(outputLine);
                        if (lineSpecialDiv < specialName.length()) {
                            outputLine._edudiv = specialName.substring(lineSpecialDiv, lineSpecialDiv + 1);
                        }
                        outputLine._specialDiv = specialDiv;
                        lineSpecialDiv++;
                        outputLine._classcd = studyrecClass.studyrec()._classMst._classcd;
                        outputLine._classname = studyrecClass.studyrec()._classMst._classname.substring(lineClasscd, lineClasscd + 1);
                        lineClasscd++;
                        nonedata = true;
                        linex++;
                        if (linex == XMAX) {
                            linex = 0;
                        } // 行のオーバーフロー
                        outputNokori = true;
                    }
                    if (!outputNokori) {
                        final PrintLine outputLine = new PrintLine(linex);
                        outputLineList.add(outputLine);
                        boolean outputName = false;
                        if (lineSpecialDiv < specialName.length()) {
                            outputLine._edudiv = specialName.substring(lineSpecialDiv, lineSpecialDiv + 1);
                            outputName = true;
                        }
                        lineSpecialDiv++;
                        if (outputName || linex != XMAX) {
                            outputLine._specialDiv = specialDiv;
                            outputLine._classcd = studyrecClass.studyrec()._classMst._classcd;
                            nonedata = true;
                            linex++;
                            if (linex == XMAX) {
                                linex = 0;
                            } // 行のオーバーフロー
                        }
                    }
                }
                while (lineSpecialDiv < specialName.length()) {
                    final PrintLine outputLine = new PrintLine(linex);
                    outputLineList.add(outputLine);
                    outputLine._edudiv = specialName.substring(lineSpecialDiv, lineSpecialDiv + 1);
                    outputLine._specialDiv = specialDiv;
                    lineSpecialDiv++;
                    linex++;
                }
            }

            if (linex == 0 || linex > 0 && linex % XMAX != 0) {
                for (int i = linex % XMAX; i < XMAX; i++) { // --NO001
                    final PrintLine outputLine = new PrintLine(linex);
                    outputLineList.add(outputLine);
                    outputLine._specialDiv = specialDiv;
                    outputLine._classname = "";
                    nonedata = true;
                    linex++;
                }
            }

            setSikakuBikoToOutputLineM(outputLineList, pInfo._gakushuBiko.getSikakuBikoList(minYear, maxYear, studyRecSubclassMap, param()));
            return outputLineList;
        }

        private void setSikakuBikoToOutputLineM(final List<PrintLine> outputLineList, final List<String> sikakuBikoList) {
            // 下のラインにつめて出力
            final ListIterator<PrintLine> oit = outputLineList.listIterator(outputLineList.size());
            final ListIterator<String> it = sikakuBikoList.listIterator(sikakuBikoList.size());
            while (it.hasPrevious()) {
                final String sikakuBiko = it.previous();

                while (oit.hasPrevious()) {
                    final PrintLine oline = oit.previous();
                    if (null != oline._subclasscd && oline._subclasscd.startsWith(_90)) {
                        continue;
                    }
                    if (oline._biko == null) {
                        oline._biko = sikakuBiko;
                        break;
                    }
                }
            }
        }

        private void printLine3M(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final PrintLine outputLine) {

            final String EDU_DIV = param()._z010.in(Z010.tokyoto) ? "EDU_DIV1" : eduDivFieldname();
            final String EDU_DIV2 = "EDU_DIV2";
            final String CLASSNAME = param()._z010.in(Z010.tokyoto) ? "CLASSNAME1" : "CLASSNAME";
            final String CLASSNAME2 = "CLASSNAME2";
            final String SUBCLASSNAME = param()._z010.in(Z010.tokyoto) ? "SUBCLASSNAME1" : "SUBCLASSNAME";

            printGakuseki3(student, pInfo, printGakuseki, null);

            svfVrsOut(EDU_DIV, outputLine._edudiv);
            svfVrsOut(EDU_DIV2, outputLine._specialDiv);
            svfVrsOut(CLASSNAME, outputLine._classname);
            svfVrsOut(CLASSNAME2, outputLine._classcd); // 教科コード
            svfVrsOut(SUBCLASSNAME, outputLine._subclassname); // 科目名

            for (final StudyRec studyrec : outputLine._studyrecList) {
                printHyoteiTanniM(printGakuseki, outputLine, studyrec);
            }
            if (param()._z010.in(Z010.kyoto)) {
                // グループ化処理
                final String slinex = String.valueOf(outputLine._linex);
                svfVrsOut("SUBCLASSNAME_GRP", slinex);
                for (int j = 0; j < 4; j++) {
                    svfVrsOut("GRADES" + String.valueOf(j + 1) + "_GRP", slinex);
                    svfVrsOut("CREDIT" + String.valueOf(j + 1) + "_GRP", slinex);
                }
                svfVrsOut("biko_grp", slinex);
            } else if (param()._useStudyrecReplaceDat) {
                final BigDecimal satei = Util.bdSum(StudyRec.getTanniList(StudyRec.TANNI_DIV_SATEI, outputLine._studyrecList, param(), _yoshiki));
                if (null != satei) {
                    svfVrsOut("CREDIT0", satei.toString()); // 査定単位
                }
            }
            for (int j = 1; j <= 5; j++) {
                svfVrsOut("biko" + j, ""); // クリア処理
            }
            svfVrsOutForData(Arrays.asList("biko",  "biko1", "biko2", "biko3", "biko4", "biko5"), outputLine._biko);
        }

        private void printHyoteiTanniM(final PrintGakuseki printGakuseki, final PrintLine outputLine, final StudyRec studyrec) {
            final String SLASH = param()._z010.in(Z010.tokyoto) ? "SLASH1_" : null;
            final String GRADES = "GRADES";
            final String CREDIT = "CREDIT";

            final Integer column = printGakuseki._yearGakusekiPositionMap.get(studyrec._year);
            if (null == column || column.intValue() == 0) {
                log.warn(" null column " + studyrec._year + " in " + printGakuseki._yearGakusekiPositionMap.keySet() + " (" + studyrec + ")");
            } else {
                final int intColumn = column.intValue();
//                if (param()._z010.in(Z010.kyoto) && studyrec.isMirishu(param())) {
//                    nonedata = true;
//                } else {
                if (param()._z010.in(Z010.tokyoto) && (studyrec.isRishuNomi(param()))) {
                    svfVrsOut(SLASH + intColumn, "／"); // 履修のみ（修得単位なしかつ履修単位あり）は斜線
                    nonedata = true;
                } else if (null != studyrec._valuation) {
                    if ("1".equals(param().property(Property.seitoSidoYorokuHyotei0ToBlank)) && studyrec._valuation.intValue() == 0) {
                    } else {
                        svfVrsOut(GRADES + intColumn, studyrec._valuation.toString()); // 評定
                    }
                    nonedata = true;
                }
                if (null != studyrec._credit) {
                    if (param()._useStudyrecReplaceDat && SCHOOLCD1.equals(studyrec._schoolcd)) {
                        // 宮城県は前籍校は単位数を表示しない
                        // ・査定あり => 査定単位を査定単位の欄に表示
                        // ・査定なし => 前籍校単位を下部合計欄に表示
                    } else {
                        svfVrsOut(CREDIT + intColumn, studyrec._credit.toString()); // 単位
                        nonedata = true;
                    }

                }
//            }
            }
            if (null != outputLine._totalCredits) {
                svfVrsOut(CREDIT, outputLine._totalCredits); // 科目別修得単位数
            }
        }

        private List<String> getBikoM(final Student student, final PersonalInfo pInfo, final String minYear, final String maxYear, final String keysubclasscd0, final String subclassName) {
//          final String compVal = getRishuTanniBiko(student, studyrec);
//          if (!"".equals(compVal)) {
//              student._gakushuBiko.putRishuTanniBiko(keysubclasscd, studyrec._year, compVal);
//          }
            final List<String> rtn = new ArrayList<String>();

            final List<String> keysubclasscds = new ArrayList<String>();
            if (param()._isSubclassOrderNotContainCurriculumcd && "1".equals(param()._useCurriculumcd) && StringUtils.split(keysubclasscd0, "-").length >= 4) {
                // 異なる教育課程コードの科目に設定されている備考も含む
                final String[] split = StringUtils.split(keysubclasscd0, "-");
                final String classcdschoolKind = split[0] + "-" + split[1];
                final String subclasscd = split[3];
                final Set keysubclasscdAllSet = new TreeSet();
                for (final Iterator it = pInfo._gakushuBiko._biko.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry<String, Map> e = (Map.Entry<String, Map>) it.next();
                    final String key = e.getKey();
                    if (Arrays.asList(GakushuBiko.STUDY, GakushuBiko.RISHU).contains(key)) {
                        final Map bikos = e.getValue();
                        for (final Iterator its = bikos.keySet().iterator(); its.hasNext();) {
                            final String subcd = (String) its.next();
                            if (null != subcd && subcd.startsWith(classcdschoolKind) && subcd.endsWith(subclasscd)) {
                                keysubclasscdAllSet.add(subcd);
                            }
                        }
                    } else {
                        // 保留
                        if (param()._isOutputDebug) {
                            log.info(" skip subclasscd " + e.getKey() + " / " + e.getValue());
                        }
                    }
                }
                keysubclasscds.addAll(keysubclasscdAllSet);
            } else {
                keysubclasscds.add(keysubclasscd0);
            }
            for (final String keysubclasscd : keysubclasscds) {
                final List<String> gakushuBikoList = pInfo._gakushuBiko.getStudyrecBikoList(keysubclasscd, subclassName, minYear, maxYear);
                final String gakushuBiko = gakushuBikoList.size() > 0 ? gakushuBikoList.get(0) : "";
                final String substitutionBikoZenbu = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, GakushuBiko.DAITAI_TYPE.NO_TYPE_FLG, minYear, maxYear).toString();
//              final String substitutionBikoZenbu = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, GakushuBiko.DAITAI_TYPE.ZENBU, minYear, maxYear).toString();
//              final String substitutionBikoIchibu = pInfo._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, GakushuBiko.DAITAI_TYPE.ICHIBU, minYear, maxYear).toString();
                final String rishuTanniBiko = pInfo._gakushuBiko.getRishuTanniBiko(keysubclasscd, minYear, maxYear).toString();
//              final String biko = rishuTanniBiko.toString() + gakushuBiko  + substitutionBikoZenbu + substitutionBikoIchibu;
                final String biko = rishuTanniBiko.toString() + gakushuBiko  + substitutionBikoZenbu;
                if (!StringUtils.isBlank(biko)) {
                    rtn.add(biko);
                }
                for (int i = 1; i < gakushuBikoList.size(); i++) {
                    rtn.add(gakushuBikoList.get(i));
                }
            }
            return rtn;
        }

        private String getRishuTanniBikoM(final Student student, final StudyRec studyrec) {
            final String rtn;
//            if (studyrec.isMirishu(param())) {
//                // 未履修の場合の備考処理
//                if (param()._z010.in(Z010.kyoto)) {
//                    rtn = getRishuTanniBikoHead(student, studyrec) + "履修不認定";
//                } else {
//                    rtn = "";
//                }
//            } else if (studyrec.isRishuNomi(param())) {
//                // 履修のみの場合の備考処理
//                if (param()._z010.in(Z010.kyoto)) {
//                    rtn = studyrec._compCredit.toString() + "単位履修認定";
//                } else {
//                    rtn = getRishuTanniBikoHead(student, studyrec) + "履修単位数(" + studyrec._compCredit.toString() + ")";
//                }
//            } else {
                rtn = "";
//            }
            return rtn;
        }

        private static String getTanniListSumString(final StudyRecYearTotalM yearTotal, final StudyRec.TotalM totalKind, final int TANNI_DIV, final Param param, final YOSHIKI yoshiki, final String def) {
            String tani = null;
            if (null != yearTotal && !yearTotal.list(totalKind).isEmpty()) {
                tani = defstr(Util.bdSum(StudyRec.getTanniList(TANNI_DIV, yearTotal.list(totalKind), param, yoshiki)), null);
            } else {
                tani = def;
            }
            return tani;
        }

        /**
         * 年度・学年別修得単位数を印字します。（総合的な学習の時間・小計・留学・合計）
         * @param substitutionbiko90
         * @param lastyearflg
         * @param studyrecyear
         */
        private void printTotalCredits3M(final boolean isPrintTotalCredits, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            final TreeSet<String> yearSet = new TreeSet<String>(printGakuseki._yearGakusekiPositionMap.keySet());
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();
            final List<String> bikoList = getBikoM(student, pInfo, minYear, maxYear, _90, "");

            final Map<String, StudyRecYearTotalM> studyrecyear = pInfo.getStudyRecYearM(param(), _yoshiki);
            final Set<String> keySet = new HashSet<String>();
            keySet.addAll(printGakuseki._yearGakusekiPositionMap.keySet());
            keySet.addAll(studyrecyear.keySet());

            // 単位が無い場合のデフォルト表示
            final String sgjDefault = param()._z010.in(Z010.kumamoto) ? "0" : null; // 総合的な学習の時間
            final String skDefault = param()._z010.in(Z010.kumamoto) || param()._z010.in(Z010.miyagiken) ? "0" : null; // 小計
            final String gkDefault = param()._z010.in(Z010.kumamoto) || param()._z010.in(Z010.miyagiken) ? "0" : param()._creditsDefaultTotal; // 合計

            for (final String year : keySet) {
                final StudyRecYearTotalM yt = studyrecyear.get(year);
                if (null != yt && StudyRec.SATEI.equals(year)) {
                    final int intColumn = 0;
                    svfVrsOutNotNull("tani_" + intColumn + "_sgj", getTanniListSumString(yt, StudyRec.TotalM.SUBJECT90, StudyRec.TANNI_DIV_SATEI, param(), _yoshiki, sgjDefault));
                    svfVrsOutNotNull("tani_" + intColumn + "_rg", getTanniListSumString(yt, StudyRec.TotalM.ABROAD, StudyRec.TANNI_DIV_SATEI, param(), _yoshiki, param()._creditsDefaultAbroad));
                    svfVrsOutNotNull("tani_" + intColumn + "_sk", getTanniListSumString(yt, StudyRec.TotalM.SUBJECT, StudyRec.TANNI_DIV_SATEI, param(), _yoshiki, skDefault));

                    final String kateigaiTanni = getTanniListSumString(yt, StudyRec.TotalM.KATEIGAI, StudyRec.TANNI_DIV_CREDIT, param(), _yoshiki, param()._z010.in(Z010.kumamoto) || param()._z010.in(Z010.miyagiken) ? "0" : null);
                    svfVrsOutNotNull("tani_" + intColumn + "_oth", kateigaiTanni);

                    final String totalTanni = getTanniListSumString(yt, StudyRec.TotalM.TOTAL, StudyRec.TANNI_DIV_SATEI, param(), _yoshiki, gkDefault);
                    svfVrsOutNotNull("tani_" + intColumn + "_gk", addNumber(totalTanni, kateigaiTanni));
                } else if (null != printGakuseki._yearGakusekiPositionMap.get(year)){
                    final int intColumn = printGakuseki._yearGakusekiPositionMap.get(year);
                    svfVrsOutNotNull("tani_" + intColumn + "_sgj", getTanniListSumString(yt, StudyRec.TotalM.SUBJECT90, StudyRec.TANNI_DIV_CREDIT, param(), _yoshiki, sgjDefault));
                    svfVrsOutNotNull("tani_" + intColumn + "_rg", getTanniListSumString(yt, StudyRec.TotalM.ABROAD, StudyRec.TANNI_DIV_CREDIT, param(), _yoshiki, param()._creditsDefaultAbroad));
                    svfVrsOutNotNull("tani_" + intColumn + "_sk", getTanniListSumString(yt, StudyRec.TotalM.SUBJECT, StudyRec.TANNI_DIV_CREDIT, param(), _yoshiki, skDefault));
                    svfVrsOutNotNull("tani_" + intColumn + "_gk", getTanniListSumString(yt, StudyRec.TotalM.TOTAL, StudyRec.TANNI_DIV_CREDIT, param(), _yoshiki, gkDefault));
                }
            }
            if (bikoList.size() > 0) {
                svfVrsOut("biko_sgj", ""); // クリア処理
                for (int j = 1; j <= 5; j++) {
                    svfVrsOut("biko_sgj" + j, ""); // クリア処理
                }
                svfVrsOutForData(Arrays.asList("biko_sgj", "biko_sgj1", "biko_sgj2", "biko_sgj3", "biko_sgj4", "biko_sgj5"), Util.mkString(bikoList, "、").toString());
            }
            if (isPrintTotalCredits) {
                final List<BigDecimal> subject90s = new ArrayList<BigDecimal>();
                final List<BigDecimal> subjects = new ArrayList<BigDecimal>();
                final List<BigDecimal> abroads = new ArrayList<BigDecimal>();
                final List<BigDecimal> totals = new ArrayList<BigDecimal>();
                final List<BigDecimal> kateigais = new ArrayList<BigDecimal>();

                for (final StudyRecYearTotalM yt : studyrecyear.values()) {
//                    if (yt._isDrop == StudyRecYearTotal.DROP || yt._isDrop == StudyRecYearTotal.DROP_SHOW) {
//                        continue;
//                    }
                    if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                        if (!printGakuseki._yearGakusekiPositionMap.keySet().contains(yt._year)) {
                            continue;
                        }
                    }
                    if (StudyRec.SATEI.equals(yt._year)) {
                        subject90s.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_SATEI, yt.list(StudyRec.TotalM.SUBJECT90), param(), _yoshiki));
                        subjects.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_SATEI, yt.list(StudyRec.TotalM.SUBJECT), param(), _yoshiki));
                        abroads.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_SATEI, yt.list(StudyRec.TotalM.ABROAD), param(), _yoshiki));
                        totals.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_SATEI, yt.list(StudyRec.TotalM.TOTAL), param(), _yoshiki));
                        final List kateigaiTanniList = StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yt.list(StudyRec.TotalM.KATEIGAI), param(), _yoshiki);
                        kateigais.addAll(kateigaiTanniList);
                        totals.addAll(kateigaiTanniList);
                    } else {
                        subject90s.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yt.list(StudyRec.TotalM.SUBJECT90), param(), _yoshiki));
                        subjects.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yt.list(StudyRec.TotalM.SUBJECT), param(), _yoshiki));
                        abroads.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yt.list(StudyRec.TotalM.ABROAD), param(), _yoshiki));
                        totals.addAll(StudyRec.getTanniList(StudyRec.TANNI_DIV_CREDIT, yt.list(StudyRec.TotalM.TOTAL), param(), _yoshiki));
                    }
                }

                final int intColumn = _gradeLineMax + 1;
                svfVrsOutNotNull("tani_" + intColumn + "_sgj", defstr(Util.bdSum(subject90s), sgjDefault));
                svfVrsOutNotNull("tani_" + intColumn + "_sk", defstr(Util.bdSum(subjects), skDefault));
                svfVrsOutNotNull("tani_" + intColumn + "_rg", defstr(Util.bdSum(abroads), param()._creditsDefaultAbroad));
                svfVrsOutNotNull("tani_" + intColumn + "_gk", defstr(Util.bdSum(totals), gkDefault));

                if (param()._z010.in(Z010.miyagiken)) {
                    svfVrsOut("tani_" + intColumn + "_oth", defstr(Util.bdSum(kateigais), "0"));
                    final List<StudyRec> kateigaiStudyrecList = pInfo.getStudyRecReplaceSateiNasiList(param(), _yoshiki);
                    final List<String> kateigaiBikoList = new ArrayList<String>();
                    for (final StudyRec sr : kateigaiStudyrecList) {
                        kateigaiBikoList.add(defstr(sr.subclassMst(param(), YOSHIKI._1_URA).subclassname()) + "(" + defstr(Util.str(sr._credit)) + ")");
                    }
                    svfVrsOutForData(Arrays.asList("biko_oth1", "biko_oth2"), Util.mkString(kateigaiBikoList, "、").toString());
                }
            }
        }

        private List<StudyrecSpecialDiv> getStudyrecSpecialDivList3M(final List<StudyRec> studyrecList, final Param param) {
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = new ArrayList<StudyrecSpecialDiv>();
            for (final StudyRec studyrec : studyrecList) {
                if (null == studyrec._classMst._classcd || null == studyrec._subclassMst._subclasscd) {
                    continue;
                }
                if (param._z010.in(Z010.miyagiken) && studyrec.isMishutoku(param)) { // 宮城県は未修得を表示しない
                    continue;
                }
                final StudyrecSpecialDiv ssd = getStudyrecSpecialDiv3(studyrecSpecialDivList, studyrec._classMst._specialDiv);
                final StudyrecClass sc = getStudyrecClass3(ssd._studyrecClassList, studyrec.classMst(param, _yoshiki), param);
                final StudyrecSubClass ssc = getStudyrecSubClass3(sc._studyrecSubclassList, studyrec.subclassMst(param, _yoshiki), param);
                ssc._studyrecList.add(studyrec);
            }
            for (final StudyrecSpecialDiv ssd : studyrecSpecialDivList) {
                Collections.sort(ssd._studyrecClassList);
                for (final StudyrecClass sc : ssd._studyrecClassList) {
                    Collections.sort(sc._studyrecSubclassList);
                }
            }
            return studyrecSpecialDivList;
        }
    }

    /**
     * 活動の記録。
     * 高校生徒指導要録の指導に関する記録を印刷します。
     * ２.総合的な学習の時間の記録
     * ３.特別活動の記録
     * ４.総合所見及び指導上参考となる諸事項
     * ５.出欠の記録
     */
    private static class KNJA130_4 extends KNJA130_0 implements Page {

        private final String USE_FIELD_AREA = "USE_FIELD_AREA";

        private final String FORM_KNJA130C_4A = "KNJA130C_4A.frm";
        private final String FORM_KNJA130C_4 = "KNJA130C_4.frm";
        private final String FORM_KNJA130_6KUDAN = "KNJA130_6KUDAN.frm";
        private final String FORM_KNJA130C_4NARA = "KNJA130C_4NARA.frm";
        private final String FORM_KNJA130C_4ANARA = "KNJA130C_4ANARA.frm";
        private boolean _isKenjaForm;

        private static final String Sougaku = "Sougaku";
        private static final String TokubetsuKatsudou = "TokubetsuKatsudou";
        private static final String SougouShoken = "SougouShoken";
        private static final String ShukektsuBikou = "ShukektsuBikou";
        private static final String All = "All";
        private static final Integer PreferPointThanKeta = 1;
        private static final Integer PreferKeta = 2;
        private final Map<String, Integer> _printMethodMap = new HashMap<String, Integer>();
        private double _shokenUsingFieldAreaPreferPointThanKeta;

        private String _seitoSidoYoroku_dat_TotalstudyactSizeForPrint;
        private String _seitoSidoYoroku_dat_TotalstudyvalSizeForPrint;
        private String _seitoSidoYoroku_dat_TokubetsuKatudouForPrint;
        private String _seitoSidoYoroku_dat_TrainRef1ForPrint;
        private String _seitoSidoYoroku_dat_TrainRef2ForPrint;
        private String _seitoSidoYoroku_dat_TrainRef3ForPrint;
        private String _seitoSidoYoroku_dat_TotalremarkSizeForPrint;
        private String _seitoSidoYoroku_dat_ShukketsuBikouForPrint;
        private int _formdiv;

        SvfFieldAreaInfo _areaInfo;

        private KNJSvfFieldInfo _nameFieldInfo;

        KNJA130_4(final Vrw32alp svf, final Param param) {
            super(svf, param);

            if (!param._isCsv) {
                if (param._z010.in(Z010.tosa)) {
                    _printMethodMap.put(All, PreferKeta);
                } else if (param._z010.in(Z010.mieken)) {
                    _printMethodMap.put(All, PreferPointThanKeta);

                    if (param._is133m) {
                        _seitoSidoYoroku_dat_TotalremarkSizeForPrint = "28 * 8";
                    } else {
                        _seitoSidoYoroku_dat_TotalstudyactSizeForPrint = "44 * 4";
                        _seitoSidoYoroku_dat_TotalstudyvalSizeForPrint = "44 * 4";
                        _seitoSidoYoroku_dat_TotalremarkSizeForPrint = "44 * 4";
                    }
                    _shokenUsingFieldAreaPreferPointThanKeta = 10.1;
                } else if (param._z010.in(Z010.aoyama)) {
                    _printMethodMap.put(Sougaku, null);
                    _printMethodMap.put(All, PreferPointThanKeta);

                    _seitoSidoYoroku_dat_TokubetsuKatudouForPrint = "12 * 6";
                    _seitoSidoYoroku_dat_TrainRef1ForPrint = "18 * 8";
                    _seitoSidoYoroku_dat_TrainRef2ForPrint = "14 * 8";
                    _seitoSidoYoroku_dat_TrainRef3ForPrint = "10 * 8";
                    _shokenUsingFieldAreaPreferPointThanKeta = 9.1;
                }
            }
        }

        private String getForm4(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
            String form = "";
            boolean is3 = is3nenYou(pInfo);
            _nameFieldInfo = null;
            _isKenjaForm = false;
            _formdiv = 1;
            if (param()._z010.in(Z010.miyagiken)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4MIYA.frm";
                } else {
                    //_gradeLineMax = is3 ? 3 : 4;
                    _gradeLineMax = 4;
                    //form = is3 ? null : "1".equals(param()._seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg) ? "KNJA130C_4_2MIYA.frm" : "KNJA130C_4MIYA.frm";
                    form = "1".equals(param().property(Property.seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg)) ? "KNJA130C_4_2MIYA.frm" : "KNJA130C_4MIYA.frm";
                }
            } else if (param()._z010.in(Z010.hirokoudai)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4HIROKOUDAI.frm";
                } else {
                    _gradeLineMax = 4;
                    form = "KNJA130C_4AHIROKOUDAI.frm";
                }
            } else if (param()._z010.in(Z010.mieken)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4MIE.frm";
                } else {
                    _gradeLineMax = 4;
                    form = "KNJA130C_4MIE.frm";
                }
            } else if (param()._z010.in(Z010.naraken)) {
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4NARA.frm";
                } else {
                    _gradeLineMax = 4;
                    if (param()._isNendogoto) {
                        form = FORM_KNJA130C_4ANARA;
                    } else {
                        form = FORM_KNJA130C_4NARA;
                    }
                }
            } else if (param()._z010.in(Z010.kyoto)) {
                if (param()._is133m) {
                    form = "KNJA133M_4KYOTO.frm";
                    _gradeLineMax = 4;
                } else {
                    if (is3 && param()._z010.in(Z010.kyoto)) {
                        if (null != printGakuseki && Gakuseki.containsDroppedAbroad(printGakuseki._yearGakusekiMap.values())) {
                            is3 = false;
                        }
                    }
                    _gradeLineMax = is3 ? 3 : 4;
//                    if (param()._isNendogoto) {
//                        if ("1".equals(param()._seitoSidoYorokuFieldSize)) {
//                        } else {
//                            if (param()._isSeitoSidoYorokuKinsokuForm) {
//                                form = is3 ? "KNJA130C_14ACKYOTO.frm" : "KNJA130C_4ACKYOTO.frm"; // 禁則
//                            } else {
                                form = is3 ? "KNJA130C_14AKYOTO.frm" : "KNJA130C_4AKYOTO.frm";
//                                form = is3 ? "KNJA130C_14ATORI.frm" : "KNJA130C_4ATORI.frm";
//                            }
//                        }
//                    }
                }
            } else if (param()._z010.in(Z010.ktsushin) && param()._is133m) {
                _gradeLineMax = 6;
                form = "KNJA133M_4KAICHI.frm";
            } else if (param()._isKaichi && pInfo._isSogoShoken3Bunkatsu) {
                _gradeLineMax = 4;
                if (param()._z010.in(Z010.kikan)) {
                    form = "KNJA130C_4AD2KAICHIIKKAN.frm"; // 3分割非均等
                } else {
                    form = "KNJA130C_4AD2KAICHIMIRAI.frm"; // 3分割非均等
                }
            } else if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    _gradeLineMax = 6;
                    form = StringUtils.isBlank(KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYVAL2")) ? "KNJA133M_6_2TOKYO.frm" : "KNJA133M_6TOKYO.frm";
                } else if (param()._z010.in(Z010.sagaken)) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4SAGA.frm";
                } else if (KNJA130_0.isNewForm(param(), pInfo)) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4KUMA.frm";
                } else {
                    _gradeLineMax = 6;
                    form = "KNJA133M_4.frm";
                }
                // KNJA133Mコールは以上
            } else if (param()._z010.in(Z010.kyoai)) {
                _gradeLineMax = is3 ? 3 : 4;
//                if (param()._isNendogoto) {
//                } else {
                    form = is3 ? "KNJA130C_14TORI.frm" : "KNJA130C_4TORI.frm";
//                }
            } else if (param()._z010.in(Z010.hagoromo)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4AHAGOROMO.frm";
            } else if (param()._z010.in(Z010.TamagawaSei)) {
                form = "KNJA130C_14TAMAGAWASEI.frm";
                _gradeLineMax = 3;
            } else if (param()._z010.in(Z010.seijyo)) {
                form = "KNJA130C_14SEIJO.frm";
                _gradeLineMax = 3;
            } else if (param()._z010.in(Z010.tokiwa)) {
                _gradeLineMax = 3;
                form =  is3 ? (isNewForm(param(), pInfo) ? "KNJA130C_14_2TOKIWA.frm" : "KNJA130C_14TOKIWA.frm") : null;
            } else if (param()._z010.in(Z010.meiji) && param()._isNendogoto) {
                _gradeLineMax = is3 ? 3 : 4;
                form = is3 ? "KNJA130C_14A.frm" : "KNJA130C_4AMEIJI.frm";
            } else if (param()._z010.in(Z010.nishiyama)) {
                _gradeLineMax = 3;
                form = "KNJA130C_14NISHIYAMA.frm"; // "KNJA130C_14A.frm"をコピー
            } else if (param()._z010.in(Z010.rakunan)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4D2RAKUNAN.frm"; // KNJA130C_4D2.frmをコピー
            } else if (param()._z010.in(Z010.tosa)) {
                _gradeLineMax = 3;
                form = "KNJA130C_14TOSA.frm";
            } else if (param()._z010.in(Z010.fukuiken)) {
                _gradeLineMax = is3 ? 3 : 4;
                form = is3 ? "KNJA130C_14FUKUI.frm" : "KNJA130C_4FUKUI.frm";
            } else if (param()._z010.in(Z010.chiyodaKudan)) {
                _gradeLineMax = 4;
                form = FORM_KNJA130_6KUDAN;
            } else if (param()._isHigashiosaka) {
                _gradeLineMax = 4; // is3 ? 3 : 4;
                form = pInfo._isSogoShoken3Bunkatsu ? "KNJA130C_4ADHIGASHIOSAKA.frm" : "KNJA130C_4AHIGASHIOSAKA.frm";
            } else if (param()._z010.in(Z010.tokiwagi)) {
                _gradeLineMax = 3; // is3 ? 3 : 4;
                form = "KNJA130C_14TOKIWAGI.frm";
            } else if (param()._z010.in(Z010.yamamura)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4YAMAMURA.frm";
            } else if (param()._z010.in(Z010.tosajoshi)) {
                _gradeLineMax = 3;
                form = "KNJA130C_14AD2TOSAJOSHI.frm"; // 3分割非均等 [18,24,18]x7
            } else if (param()._z010.in(Z010.osakatoin)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4ATOIN.frm";
            } else if (param()._isSanonihonHs) {
                _gradeLineMax = 4;
                form = "KNJA130C_4SANONIHONHS.frm";
            } else if (param()._isSanonihonSs) {
                _gradeLineMax = 4;
                form = "KNJA130C_4SANONIHONSS.frm";
            } else if (param()._z010.in(Z010.nagisa)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4NAGISA.frm";
            } else if (param()._z010.in(Z010.naganoSeisen)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4NAGANOSEISEN.frm";
            } else if (param()._z010.in(Z010.komazawa)) {
                _gradeLineMax = 3;
                form = "KNJA130C_14KOMAZAWA.frm";
            } else if (param()._z010.in(Z010.reitaku)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4REITAKU.frm";
            } else if (param()._z010.in(Z010.matsudo)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4D2MATSUDO.frm";
            } else if (param()._z010.in(Z010.ryukei)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4RYUKEI.frm";
            } else if (param()._z010.in(Z010.shimaneken)) {
                // 年度毎
                final boolean isJiritsuForm = pInfo._student.isShimanekenPrintJiritsuKatudou(param());
                if (is3) {
                    // 3年用 総合所見3分割均等割
                    _gradeLineMax = 3;
                    if (pInfo.useSeitoSidoYorokuSenmonGakkaForm(param())) {
                        if (isJiritsuForm) {
                            form = "KNJA130C_14AD2_SHIMANEKEN_JIRITSU_SENMON.frm";
                        } else {
                            form = "KNJA130C_14AD2_SHIMANEKEN_SENMON.frm";
                        }
                    } else {
                        if (isJiritsuForm) {
                            form = "KNJA130C_14AD2_SHIMANEKEN_JIRITSU.frm";
                        } else {
                            form = "KNJA130C_14AD2_SHIMANEKEN.frm";
                        }
                    }
                } else {
                    // 4年用 総合所見3分割均等割
                    _gradeLineMax = 4;
                    if (pInfo.useSeitoSidoYorokuSenmonGakkaForm(param())) {
                        if (isJiritsuForm) {
                            form = "KNJA130C_4AD2_SHIMANEKEN_JIRITSU_SENMON.frm";
                        } else {
                            form = "KNJA130C_4AD2_SHIMANEKEN_SENMON.frm";
                        }
                    } else {
                        if (isJiritsuForm) {
                            form = "KNJA130C_4AD2_SHIMANEKEN_JIRITSU.frm";
                        } else {
                            form = "KNJA130C_4AD2_SHIMANEKEN.frm";
                        }
                    }
                }
            } else if (param()._z010.in(Z010.aoyama)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4D_AOYAMA.frm";
            } else if (param()._z010.in(Z010.doshisha)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4_DOSHISHA.frm";
            } else if (param()._z010.in(Z010.jogakkan)) {
                _gradeLineMax = 4;
                form = "KNJA130C_4_JOGAKKAN.frm";
            } else if (param()._z010.in(Z010.jyoto)) {
                if (pInfo._isSogoShoken3Bunkatsu) {
                    _gradeLineMax = 4;
                    form = "KNJA130C_4D_JYOTO.frm";
                } else if (pInfo._isSogoShoken6Bunkatsu) {
                    _gradeLineMax = 4;
                    form = "KNJA130C_4_RECORD_JYOTO.frm";
                    _formdiv = 4;
                } else {
                    form = null;
                }
            } else {
                _gradeLineMax = is3 ? 3 : 4;
                if (pInfo._isSogoShoken3Bunkatsu) {
                    if (pInfo._isSogoShoken3BunkatsuHikintou) {
                        if (param()._isNendogoto) {
                            form = is3 ? "KNJA130C_14AD.frm" : "KNJA130C_4AD.frm";
                        } else {
                            form = is3 ? "KNJA130C_14D.frm" : "KNJA130C_4D.frm";
                        }
                    } else {
                        if (param()._isNendogoto) {
                            form = is3 ? "KNJA130C_14AD2.frm" : "KNJA130C_4AD2.frm";
                        } else {
                            form = is3 ? "KNJA130C_14D2.frm" : "KNJA130C_4D2.frm";
                        }
                    }
                } else {
                    if (param()._isNendogoto) {
                        form = is3 ? "KNJA130C_14A.frm" : FORM_KNJA130C_4A;
                    } else {
                        form = is3 ? "KNJA130C_14.frm" : FORM_KNJA130C_4;
                    }
                }
                _isKenjaForm = true;
            }
            return form;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            setDetail4(db2, student, pInfo, csvLines);
        }

        private void setDetail4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            getForm4(student, pInfo, null); // _gradeLineMaxセット
            final Map<Integer, PrintGakuseki> pagePrintGakusekiMap = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);

            for (final Integer page : pagePrintGakusekiMap.keySet()) {
                final PrintGakuseki printGakuseki = pagePrintGakusekiMap.get(page);
                printPage4(db2, student, pInfo, printGakuseki, csvLines);
            }
        }

        @Override
        public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
            printPage4(db2, student, pInfo, pg, csvLines);
        }

        private void printPage4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
            String form = null;
            if (null != csvLines) {
                _gradeLineMax = 999;
            } else {
                form = getForm4(student, pInfo, printGakuseki);
                svfVrSetForm(form, 1);
            }
            if (null == _nameFieldInfo) {
                _nameFieldInfo = _form._formInfo.getFieldInfo("NAME1", "NAME2", "NAME3", charSize11, 24);
                if (param()._isOutputDebug) {
                    log.info(" ## setNameField " + _nameFieldInfo);
                }
            }
            if (param()._isOutputDebug) {
                log.info(" _isFuhakkou = " + pInfo._isFuhakkou);
            }

            final TreeMap<String, Integer> pageYearPosMap = new TreeMap<String, Integer>();
            for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                final Integer i = printGakuseki._yearGakusekiPositionMap.get(year);
                final int pos;
                if (param()._is133m) {
                    pos = getGradeColumnNumM1(student, pInfo, i.intValue(), gakuseki, param(), _gradeLineMax);
                } else {
                    pos = getGradeColumnNum(pInfo, i.intValue(), gakuseki, pInfo.isDropBefore(gakuseki) ? GakusekiColumn.NORMAL : GakusekiColumn.SEQ, param(), _gradeLineMax);
                }
                pageYearPosMap.put(year, pos);
            }
            final Param param = param();
            if (null != csvLines) {

                final List<List<String>> nameLines = new ArrayList<List<String>>();
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines).addAll(Arrays.asList("生徒氏名", pInfo.getPrintName1(), "", "", "", "", "", ""));
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(nameLines);
                CsvUtils.newLine(csvLines);
                csvLines.addAll(CsvUtils.horizontalUnionLines(nameLines, getCsvGakusekiLines(pInfo, printGakuseki)));

                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).addAll(Arrays.asList(pInfo.getSogoSubclassname(param, printGakuseki._yearGakusekiMap)));

                List<List<String>> sogakuAct = new ArrayList<List<String>>();
                List<List<String>> sogakuVal = new ArrayList<List<String>>();

                final List<String> studyRecSubstitution90AllYear = daitaibiko90AllYear(student, pInfo, printGakuseki, param);
                if (param._isOutputDebug) {
                    log.info(" _seitoSidoYorokuTotalStudyCombineHtrainremarkDat = " + param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat);
                }
                if (param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                    // 総合的な学習の時間
                    printSogoMatome(db2, student, pInfo, studyRecSubstitution90AllYear, printGakuseki);
                }

                if (param._isNendogoto || param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                    for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                        final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                        final Integer pos = pageYearPosMap.get(year);
                        if (param._isOutputDebugData) {
                            log.info(" gakuseki = " + gakuseki  + " / " + year + " -> " + pos);
                        }

                        // 所見データを印刷
                        final HtrainRemark remark = student.getHtrainremark(year);

                        final List<String> studyRecSubstitution90Tannen;
                        if (student._isShowStudyRecBikoSubstitution90 && !param().isNotPrintDaitai("2ura")) {
                            studyRecSubstitution90Tannen = pInfo.getArraySubstitutionBiko90(year, param());
                        } else {
                            studyRecSubstitution90Tannen = Collections.emptyList();
                        }
                        final String title;
                        if (gakuseki._isKoumokuGakunen) {
                            title = gakuseki._gdat._gradeName2;
                        } else {
                            title = gakuseki._nendo;
                        }
                        final String totalStudyAct = Util.mkString(cons(remark.totalstudyact(), studyRecSubstitution90Tannen), "\n").toString();
                        final String totalStudyVal = remark.totalstudyval();
                        final KNJPropertiesShokenSize sizeAct = KNJPropertiesShokenSize.getShokenSize(defstr(_seitoSidoYoroku_dat_TotalstudyactSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize)), 11, 4);
                        final KNJPropertiesShokenSize sizeVal = KNJPropertiesShokenSize.getShokenSize(defstr(_seitoSidoYoroku_dat_TotalstudyvalSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize)), 11, 4);

                        if (param._isNendogoto) {
                            final List<String> nenAct = new ArrayList<String>();
                            final List<String> nenVal = new ArrayList<String>();
                            nenAct.add(title);
                            nenAct.addAll(Util.getTokenList(totalStudyAct, sizeAct.getKeta()));
                            nenVal.add(title);
                            nenVal.addAll(Util.getTokenList(totalStudyVal, sizeVal.getKeta()));

                            sogakuAct = CsvUtils.horizontalUnionLines(sogakuAct, Util.listToListList(nenAct));
                            sogakuVal = CsvUtils.horizontalUnionLines(sogakuVal, Util.listToListList(nenVal));
                        } else {
                            sogakuAct.add(Util.getTokenList(totalStudyAct, sizeAct.getKeta()));
                            sogakuVal.add(Util.getTokenList(totalStudyAct, sizeAct.getKeta()));
                        }
                    }
                } else {
                    final String totalstudyact0 = KnjDbUtils.getString(student._htrainRemarkHdat, "TOTALSTUDYACT");
                    // 「総合的な学習の時間の記録」を印字
                    final String totalStudyAct = student._isShowStudyRecBikoSubstitution90 ? Util.mkString(cons(totalstudyact0, studyRecSubstitution90AllYear), "\n").toString() : totalstudyact0;
                    final String totalStudyVal = KnjDbUtils.getString(student._htrainRemarkHdat, "TOTALSTUDYVAL");
                    final int moji = param()._z010.in(Z010.ryukei) ? 45 : 44;

                    sogakuAct.add(Util.getTokenList(param(), totalStudyAct, moji * 2, 4));
                    sogakuVal.add(Util.getTokenList(param(), totalStudyVal, moji * 2, 6));
                }

                csvLines.addAll(CsvUtils.horizontalUnionLines(Arrays.asList(Arrays.asList("学習活動")), sogakuAct));
                csvLines.addAll(CsvUtils.horizontalUnionLines(Arrays.asList(Arrays.asList("評価")), sogakuVal));

                List<List<String>> spAct = new ArrayList<List<String>>();
                List<List<String>> sogoShoken = new ArrayList<List<String>>();
                List<List<String>> attend = new ArrayList<List<String>>();

                for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                    final String title;
                    if (gakuseki._isKoumokuGakunen) {
                        title = gakuseki._gdat._gradeName2;
                    } else {
                        title = gakuseki._nendo;
                    }

                    final HtrainRemark remark = student.getHtrainremark(gakuseki._year);

                    // 特別活動
                    final KNJPropertiesShokenSize spSize = KNJPropertiesShokenSize.getShokenSize(defstr(_seitoSidoYoroku_dat_TokubetsuKatudouForPrint, param().property(Property.seitoSidoYoroku_dat_SpecialactremarkSize)), 12, 8).createAddKeta(1);
                    final String specialactremark = remark.specialactremark();
                    final List<List<String>> spActNen = new ArrayList<List<String>>();
                    spActNen.add(Arrays.asList(title));
                    spActNen.addAll(Util.listToListList(Util.getTokenList(specialactremark, spSize.getKeta())));
                    spAct = CsvUtils.horizontalUnionLines(spAct, spActNen);

                    // 総合所見
                    List<List<String>> sogoShokenNen = new ArrayList<List<String>>();
                    CsvUtils.newLine(sogoShokenNen).add(title);
                    final HtrainRemarkDetail remarkDetail = null == student._htrainRemarkDetailMap.get(gakuseki._year) ? new HtrainRemarkDetail(new HashMap()) : student._htrainRemarkDetailMap.get(gakuseki._year);
                    if (pInfo._isSogoShoken3Bunkatsu) {
                        final int keta1 = pInfo._train_ref_1_2_3_field_sizeInt[0] * 2;
                        final int keta2 = pInfo._train_ref_1_2_3_field_sizeInt[1] * 2;
                        final int keta3 = pInfo._train_ref_1_2_3_field_sizeInt[2] * 2;
                        for (final Tuple<String, Integer> shokenAndKeta : Arrays.asList(Tuple.of(remarkDetail._trainRef1, keta1), Tuple.of(remarkDetail._trainRef2, keta2), Tuple.of(remarkDetail._trainRef3, keta3))) {
                            final String shoken = shokenAndKeta._first;
                            final int keta = shokenAndKeta._second;
                            sogoShokenNen = CsvUtils.horizontalUnionLines(sogoShokenNen, Util.listToListList(Util.getTokenList(shoken, keta)));
                        }
                    }
                    sogoShoken.addAll(sogoShokenNen);

                    // 出欠
                    final AttendRec attendrec = null == student._attendRecMap.get(gakuseki._year) ? new AttendRec(gakuseki._year) : student._attendRecMap.get(gakuseki._year);
                    List<List<String>> attendNen = new ArrayList<List<String>>();
                    attendNen.add(Arrays.asList(title, attendrec._attend_1, addNumber(attendrec._mourning, attendrec._suspend), attendrec._abroad, attendrec._requirepresent, attendrec._attend_6, attendrec._present));
                    final KNJPropertiesShokenSize attendrecRemarkSize = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize), 20, 2).createAddKeta(1);
                    attendNen = CsvUtils.horizontalUnionLines(attendNen, Util.listToListList(Util.getTokenList(remark.attendrecRemark(), attendrecRemarkSize.getKeta())));
                    attend.addAll(attendNen);
                }

                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("特別活動の記録"));
                csvLines.addAll(spAct);
                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("総合所見及び指導上参考となる諸事項"));
                if (pInfo._isSogoShoken3Bunkatsu) {
                    CsvUtils.newLine(csvLines).addAll(Arrays.asList("", "（１）学習における特徴等", "（３）部活動、ボランティア活動等", "（５）その他"));
                    CsvUtils.newLine(csvLines).addAll(Arrays.asList("", "（２）行動の特徴等、特技等", "（４）取得資格、検定等", ""));
                }
                csvLines.addAll(sogoShoken);
                CsvUtils.newLine(csvLines);
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("出欠の記録"));
                CsvUtils.newLine(csvLines).addAll(Arrays.asList(pInfo._title + "＼区分", "授業日数", "出席停止・忌引等の日数", "留学中の授業日数", "出席しなければならない日数", "欠席日数", "出席日数", "備考"));
                csvLines.addAll(attend);

                return;
            }
            final String form4 = modifyForm4(form, student, pInfo, printGakuseki, pageYearPosMap);
            svfVrSetForm(form4, _formdiv);

            final boolean isJyotoRecordForm = isJyotoRecordForm(pInfo);
            final boolean isRecordForm = isJyotoRecordForm;

            printPersonalInfo4(student, pInfo, printGakuseki);

            svfVrsOut("GRADENAME2", pInfo._title);
            for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                final Integer pos = pageYearPosMap.get(year);
                if (param._isOutputDebugData) {
                    log.info(" gakuseki = " + gakuseki  + " / " + year + " -> " + pos);
                }

                printGakuseki4(pInfo, pos, gakuseki);
            }

            svfVrsOut("SOGO_TITLE", pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap) + "の記録");

            // 総合的な学習の時間
            printSogoteknaGakushunoJikan(db2, student, pInfo, printGakuseki, pageYearPosMap, param);

            if (param()._isOutputDebugData) {
                for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                    final HtrainRemark remark = student.getHtrainremark(gakuseki._year);

                    log.info(" " + gakuseki._year + " : " + remark);
                }
            }

            // 特別活動の記録
            if (!pInfo._isFuhakkou) {
                for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                    final Integer pos = pageYearPosMap.get(year);

                    final HtrainRemark remark = student.getHtrainremark(gakuseki._year);
                    // 特別活動
                    printSpecialActRemark(pInfo, pos, remark);
                }
            }
            if (isRecordForm) {
                svfVrEndRecord();
            }

            // 総合所見
            if (isJyotoRecordForm) {

                svfVrsOut("SOGOSHOKEN_TITLE_FLG", "1");
                svfVrEndRecord();
                // 城東は6分割調査書と同様に枠拡張で出力する
                final int moji = 16; // 1行あたり文字数
                final int gyo = 2 + 4; // タイトル2行と所見5行
                final List<String> years = Util.setMinSize(new ArrayList<String>(printGakuseki._yearGakusekiMap.keySet()), _gradeLineMax); // 学年枠分 (gradeLineMax)

                for (final Indexed<String> idxYear : Indexed.of(years)) {
                    final String year = idxYear._val;
                    final LinkedList<FormRecord> yearRecordList = new LinkedList<FormRecord>();

                    final HtrainRemarkTrainref remarkTrainref = def(null == year ? null : student._htrainRemarkTrainrefMap.get(Year.of(year)), HtrainRemarkTrainref.NULL);

                    for (int dan = 1; dan <= 2; dan++) {
                        final List<Tuple<String, String>> shokenAndTitleList;
                        if (dan == 1) {
                            shokenAndTitleList = Arrays.asList(
                                    Tuple.of(remarkTrainref._trainRef1, "(1)学習における特徴等")
                                  , Tuple.of(remarkTrainref._trainRef2, "(2)行動の特徴，特技等")
                                  , Tuple.of(remarkTrainref._trainRef3, "(3)部活動，ボランティア活動，\n 留学・海外経験等")
                                    );
                        } else {
                            shokenAndTitleList = Arrays.asList(
                                    Tuple.of(remarkTrainref._trainRef4, "(4)取得資格，検定等")
                                  , Tuple.of(remarkTrainref._trainRef5, "(5)表彰・顕彰等の記録")
                                  , Tuple.of(remarkTrainref._trainRef6, "(6)その他")
                                    );
                        }
                        final List<List<String>> cols = new ArrayList<List<String>>();
                        final List<Integer> gyos = new ArrayList<Integer>();
                        for (final Tuple<String, String> shokenAndTitle : shokenAndTitleList) {
                            final String shoken = shokenAndTitle._first;
                            final String title = shokenAndTitle._second;
                            final List<String> tokenList = Util.getTokenList(param, shoken, moji * 2);
                            cols.add(Util.concat(Util.setMinSize(Arrays.asList(StringUtils.split(title)), 2), tokenList));
                            gyos.add(tokenList.size());
                        }
                        final int gyoMax = Math.max(gyo, max(gyos));
                        for (int gyoidx = 0; gyoidx < gyoMax; gyoidx++) {
                            final FormRecord rec = FormRecord.nextRecord(yearRecordList);
                            for (int colidx = 0; colidx < cols.size(); colidx++) {
                                final String field = "field8_1_" + String.valueOf(colidx + 1);
                                final String data;
                                if (gyoidx < cols.get(colidx).size()) {
                                    data = cols.get(colidx).get(gyoidx);
                                } else {
                                    data = "DUMMY";
                                    rec.setAttribute(field, "X=10000");
                                }
                                rec.setData(field, data);
                                rec.setData("LINEGROUP_SHOKEN" + String.valueOf(colidx + 1), String.valueOf(dan));
                            }
                        }
                    }

                    final String gakunenNendoTitle = defstr(null == year ? null : getGakunenNendoTitle(pInfo, printGakuseki._yearGakusekiMap.get(year)));
                    if (idxYear._idx != 0) {
                        final int currentPageConsumed = getCurrentPageConsumed("field8_1_1");
                        final double currentPageRestRecordCount = getCurrentPageRestRecordCount("field8_1_1");
                        final int currentPageRestRecordCounti = (int) currentPageRestRecordCount;
                        if (param()._isOutputDebugFormRecordInfo) {
                            log.info(" " + gakunenNendoTitle + " 残り " + new BigDecimal(currentPageRestRecordCount).setScale(2, BigDecimal.ROUND_FLOOR) +  "行 / 出力行 " + yearRecordList.size());
                        }
                        if (0 < currentPageConsumed && currentPageRestRecordCounti < yearRecordList.size()) {
                            // 空行追加
                            final List<FormRecord> blankLines = new ArrayList<FormRecord>();
                            if (param()._isOutputDebugFormRecordInfo) {
                                log.info(" 空行追加 " + currentPageRestRecordCounti);
                            }
                            for (int i = 0; i < currentPageRestRecordCounti; i++) {
                                final FormRecord rec = FormRecord.nextRecord(blankLines);
                                rec.setData("LINEGROUP_EMPTY", "1");
                                rec.setData("BLANK_N", String.valueOf(i));
                            }
                            svfOutRecordList(pInfo, blankLines);
                        }
                    }

                    final int currentPageRestRecordCounti = (int) getCurrentPageRestRecordCount("field8_1_1");
                    if (currentPageRestRecordCounti < yearRecordList.size()) {
                        List<FormRecord> processYearRecordList = new ArrayList<FormRecord>(yearRecordList);
                        // 複数ページにまたがる
                        int loop = 0;
                        while (0 < processYearRecordList.size()) {

                            int restRecordCount = (int) getCurrentPageRestRecordCount("field8_1_1");
                            if (restRecordCount == 0) {
                                recordInfoNewPage();
                                restRecordCount = (int) getCurrentPageRestRecordCount("field8_1_1");
                            }


                            final List<FormRecord> currentPageYearRecordList = processYearRecordList.subList(0, Math.min(processYearRecordList.size(), restRecordCount));

                            final String gakunenTitle = defstr(Util.centering(gakunenNendoTitle, currentPageYearRecordList.size()));
                            // 1ページに収まる
                            for (int ti = 0; ti < currentPageYearRecordList.size(); ti++) {
                                if (ti < gakunenTitle.length()) {
                                    currentPageYearRecordList.get(ti).setData("GRADE2_1", String.valueOf(gakunenTitle.charAt(ti)));
                                }
                                currentPageYearRecordList.get(ti).setData("LINEGROUP_GRADE", String.valueOf(idxYear._idx));
                            }

                            svfOutRecordList(pInfo, currentPageYearRecordList);

                            processYearRecordList = Util.drop(currentPageYearRecordList.size(), processYearRecordList);
                            loop += 1;
                            if (loop > 100) {
                                break;
                            }
                        }

                    } else {
                        final String gakunenTitle = defstr(Util.centering(gakunenNendoTitle, yearRecordList.size()));
                        // 1ページに収まる
                        for (int ti = 0; ti < yearRecordList.size(); ti++) {
                            if (ti < gakunenTitle.length()) {
                                yearRecordList.get(ti).setData("GRADE2_1", String.valueOf(gakunenTitle.charAt(ti)));
                            }
                            yearRecordList.get(ti).setData("LINEGROUP_GRADE", String.valueOf(idxYear._idx));
                        }
                        svfOutRecordList(pInfo, yearRecordList);
                    }
                }

            } else {
                if (!pInfo._isFuhakkou) {
                    for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                        final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                        final Integer pos = pageYearPosMap.get(year);

                        final HtrainRemark remark = student.getHtrainremark(gakuseki._year);
                        // 総合所見
                        final HtrainRemarkDetail remarkDetail = student._htrainRemarkDetailMap.get(gakuseki._year);
                        final HtrainRemarkTrainref remarkTrainref = student._htrainRemarkTrainrefMap.get(Year.of(gakuseki._year));
                        printSogoShoken(pInfo, pos, remark, remarkDetail, remarkTrainref);
                    }
                }
            }

            // 出欠
            svfVrsOut("GRADENAME1", pInfo._title);
            for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                final Integer pos = pageYearPosMap.get(year);

                final boolean enableYear = Util.toInt(gakuseki._year, 0) != 0;
                if (param()._is133m) {
                    if (param()._z010.in(Z010.sagaken)) {
                        svfVrsOut("GRADE3_" + pos + "_2", gakuseki._nendo);
                    } else if (param()._schoolDiv.isGakunenSei(gakuseki._year, null, student)) {
                        if (enableYear) {
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._gdat._gakunenSimple);
                        } else {
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._nendofM);
                        }
                    } else {
                        if (enableYear) {
                            svfVrsOut("GRADE3_" + pos, gakuseki._nendofM);
                            svfVrsOut("GRADE3_" + pos + "_1", gakuseki._arNendoM[0]);
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._arNendoM[1]);
                            svfVrsOut("GRADE3_" + pos + "_3", gakuseki._arNendoM[2]);
                        } else {
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._nendofM);
                        }
                    }
                } else {
                    if (gakuseki._isKoumokuGakunen) {
                        if (enableYear) {
                            svfVrsOut("GRADE3_" + pos + "_2", param()._z010.in(Z010.tokiwa) ? gakuseki._gdat._gradeName2 : gakuseki._gdat._gakunenSimple);
                        } else {
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._nendo);
                        }
                    } else {
                        if (enableYear) {
                            svfVrsOut("GRADE3_" + pos, gakuseki._nendo);
                            final String[] nendoArray = gakuseki.nendoArray(param());
                            svfVrsOut("GRADE3_" + pos + "_1", nendoArray[0]);
                            svfVrsOut("GRADE3_" + pos + "_2", nendoArray[1]);
                            svfVrsOut("GRADE3_" + pos + "_3", nendoArray[2]);
                        } else {
                            svfVrsOut("GRADE3_" + pos + "_2", gakuseki._nendo);
                        }
                    }
                }

                final HtrainRemark remark = student.getHtrainremark(gakuseki._year);
                if (!pInfo._isFuhakkou) {
                    // 出欠備考
                    final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
                    printAttendrecRemark(pos, remark, attendrec);
                }

                // 出欠データを印刷
                printAttendRec(student, pInfo, gakuseki._year, pos);
            }
            if (isRecordForm) {
                svfVrEndRecord();
            }

            if (isJyotoRecordForm) {
                // 調査書備考欄の挿入
                final List<FormRecord> bikoRecordList = new ArrayList<FormRecord>();
                FormRecord.nextRecord(bikoRecordList).setData("LINEGROUP_BIKO_HEAD", "1");
                final List<String> tokenList = Util.getTokenList(student._hexamEntremarkRemarkHdatRemark, getSvfFormFieldLength("field9", 10));
                for (final String token : tokenList) {
                    final FormRecord record = FormRecord.nextRecord(bikoRecordList);
                    record.setData("field9", token);
                    record.setData("LINEGROUP_BIKO_TTILE", "1");
                    record.setData("LINEGROUP_BIKO", "1");
                }
                final List<String> title = Arrays.asList("", "８", "", "備", "", "考", "", "");
                for (int i = 0; i < title.size(); i++) {
                    if (i >= tokenList.size()) {
                        final FormRecord record = FormRecord.nextRecord(bikoRecordList);
                        record.setData("LINEGROUP_BIKO_TTILE", "1");
                        record.setData("LINEGROUP_BIKO", "1");
                    }
                    final FormRecord record = bikoRecordList.get(i);
                    String chr = title.get(i);
                    if ("８".equals(chr)) {
                        record.setData("field9_TITLE", chr);
                        record.setData("field9_TITLE2", ".");
                    } else {
                        record.setData("field9_TITLE", chr);
                    }
                }

                printName(pInfo, _nameFieldInfo);

                svfOutRecordList(pInfo, bikoRecordList);
            }

            if (hasField("TEXT1")) {
                // SVF9以上で○番号が文字化けする対応
                final String[] sogoText = {
                        "①各教科・科目や" + pInfo.getSogoSubclassname(param, printGakuseki._yearGakusekiMap) + "の学習",
                        "  に関する所見",
                        "②行動に関する所見",
                        "③進路指導に関する事項",
                        "④取得資格",
                        "⑤生徒が就職している場合の事業所",
                        "⑥生徒の特徴・特技，部活動，学校内外におけるボランティア活動，",
                        "  表彰を受けた行為や活動，標準検査に関する記録など指導上参考と",
                        "  なる諸事項",
                        "⑦生徒の成長の状況にかかわる総合的な所見",
                };
                for (int i = 0; i < sogoText.length; i++) {
                    svfVrsOut("TEXT" + String.valueOf(i + 1), sogoText[i]);
                }
            }

            if (isRecordForm) {
                printName(pInfo, _nameFieldInfo);
                svfVrEndRecord();
            } else {
                svfVrEndPage();
            }
            nonedata = true;
        }

        private void svfOutRecordList(final PersonalInfo pInfo, final List<FormRecord> recordList) {
            // 出力
            for (final FormRecord record : recordList) {
                for (final String field : record.fieldSet()) {
                    String data = record.data(field);
                    String attribute = record.attribute(field);
                    if (record.isNotBlank() == false) {
                        data = "DUMMY";
                        attribute = "X=10000";
                    }
                    svfVrsOut(field, data);
                    svfVrAttribute(field, attribute);
                }
                printName(pInfo, _nameFieldInfo);
                svfVrEndRecord();
            }
        }

        private boolean isJyotoRecordForm(final PersonalInfo pInfo) {
            return param()._z010.in(Z010.jyoto) && pInfo._isSogoShoken6Bunkatsu;
        }

        private void printSogoteknaGakushunoJikan(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final TreeMap<String, Integer> pageYearPosMap, final Param param) {
            if (param._is133m) {

                printSogoMatome(db2, student, pInfo, Collections.EMPTY_LIST, printGakuseki);
            } else {
                final List<String> studyRecSubstitution90AllYear = daitaibiko90AllYear(student, pInfo, printGakuseki, param);
                if (param._isOutputDebug) {
                    log.info(" _seitoSidoYorokuTotalStudyCombineHtrainremarkDat = " + param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat);
                }
                if (pInfo._isFuhakkou) {
                    printFooterRemark(pInfo, YOSHIKI._2_URA);
                } else {
                    if (param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                        // 総合的な学習の時間
                        printSogoMatome(db2, student, pInfo, studyRecSubstitution90AllYear, printGakuseki);
                    }
                }
                if (!param._isNendogoto && !param._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                    if (param()._isOutputDebug) {
                        log.info("sogo hdat ");
                    }
                    final String totalstudyact0 = KnjDbUtils.getString(student._htrainRemarkHdat, "TOTALSTUDYACT");
                    // 「総合的な学習の時間の記録」を印字
                    final String totalStudyAct = student._isShowStudyRecBikoSubstitution90 ? Util.mkString(cons(totalstudyact0, studyRecSubstitution90AllYear), "\n").toString() : totalstudyact0;
                    final String totalStudyVal = KnjDbUtils.getString(student._htrainRemarkHdat, "TOTALSTUDYVAL");
                    if (usePrintMethod(Sougaku, PreferKeta)) {
                        printSvfRenbanUseFieldArePreferKeta("rec_1", totalStudyAct, 44 * 2);
                        printSvfRenbanUseFieldArePreferKeta("rec_2", totalStudyVal, 44 * 2);
                    } else if (usePrintMethod(Sougaku, PreferPointThanKeta)) {
                        printSvfRenbanUseFieldArePreferPointThanKeta("rec_1", totalStudyAct, 10.1);
                        printSvfRenbanUseFieldArePreferPointThanKeta("rec_2", totalStudyVal, 10.1);
                    } else {
                        final int moji = param()._z010.in(Z010.ryukei) ? 45 : 44;
                        printSvfRenban("REC_1", totalStudyAct, moji * 2, 4);    // 「学習活動」の欄
                        printSvfRenban("REC_2", totalStudyVal, moji * 2, 6);    // 「評価」の欄
                    }
                }
            }
            if (param._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    final int keta = 88;
                    VrsOutnToken("rec_3", keta, 2, KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYACT2"));
                    VrsOutnToken("rec_4", keta, 2, KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYVAL2"));
                }
            } else {
                if ("2".equals(param().property(Property.seitoSidoYorokuHoushiNentani))) {
                    // 奉仕
                    VrsOutnToken("rec_3", 44 * 2, 4, KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYACT2"));
                    VrsOutnToken("rec_4", 44 * 2, 6, KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYVAL2"));
                }
            }

            if (param._isNendogoto) {
                for (final String year : printGakuseki._yearGakusekiMap.keySet()) {
                    final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                    final Integer pos = pageYearPosMap.get(year);

                    if (param()._isOutputDebug) {
                        log.info("sogo nendogoto " + gakuseki._year);
                    }
                    if (!pInfo.isTargetYearLast(gakuseki._year, student, param(), true)) {
                        log.warn("所見表示無し:" + gakuseki._year);
                        continue;
                    }
                    if (pInfo._isFuhakkou) {
                        log.warn("所見表示無し:不発行");
                        continue;
                    }
                    // 総合的な学習の時間 年度ごと
                    printSogoNendogoto(student, pInfo, gakuseki._year, pos);
                }
            }
        }

        private boolean usePrintMethod(final String s, final Integer i) {
            if (_printMethodMap.containsKey(s)) {
                return i.equals(_printMethodMap.get(s));
            }
            return i.equals(_printMethodMap.get(All));
        }

        private SvfForm.Line wakuShasen(final SvfForm svfForm, final SvfField field) {
            final Map<String, String> attributes = field.getAttributeMap();
            final SvfForm.Point topleft = new SvfForm.Point(Integer.parseInt(attributes.get("X")), Integer.parseInt(attributes.get("Y")));
            final SvfForm.Line upperLine = svfForm.getNearestUpperLine(topleft);
            final SvfForm.Line bottomLine = svfForm.getNearestLowerLine(topleft);
            final SvfForm.Line leftLine = svfForm.getNearestLeftLine(topleft);
            final SvfForm.Line rightLine = svfForm.getNearestRightLine(topleft);

            final SvfForm.Line shasen = new SvfForm.Line(new SvfForm.Point(rightLine._start._x, upperLine._end._y), new SvfForm.Point(leftLine._start._x, bottomLine._end._y)); // 右上から左下
            return shasen;
        }

        final String HYOKA = "HYOKA";
        final String SLASH = "SLASH";
        final String KUDAN_SHASEN = "KUDAN_SHASEN"; // 九段の斜線
        final String TEXT_SOGO = "TEXT_SOGO"; // 総合の時間の文言
        final String TEXT_SHUKKOU = "TEXT_SHUKKOU"; // 「出校の記録」
        final String SPACT_EXTENDS = "SPACT_EXTENDS"; // 特別活動の記録所見
        final String SOGOSHOKEN_6BUNKATSU = "SOGOSHOKEN_6BUNKATSU"; // 総合所見6分割
        final String SHIMANEKEN_SOGOSHOKEN_SECOND_FIELD = "SHIMANEKEN_SOGOSHOKEN_SECOND_FIELD"; // 総合所見フィールド切替
        final String SHIMANEKEN_SOGAKU_SECOND_FIELD = "SHIMANEKEN_SOGAKU_SECOND_FIELD"; // 総学フィールド切替
        final String SHIMANEKEN_TOKUBETSUKATUDOU_SECOND_FIELD = "SHIMANEKEN_TOKUBETSUKATUDOU_SECOND_FIELD"; // 特別活動フィールド切替
        final String SHIMANEKEN_ATTENDREMARK_SECOND_FIELD = "SHIMANEKEN_ATTENDREMARK_SECOND_FIELD"; // 出欠備考フィールド切替
        final String NAGANOSEISEN_SOGOSHOKEN = "NAGANOSEISEN_SOGOSHOKEN"; // 長野清泉総合所見
        private String modifyForm4(final String form, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final TreeMap<String, Integer> pageYearPosMap) {
            final Map<String, String> keys = new TreeMap<String, String>();

            if (_formdiv == 1 && pInfo._isSogoShoken6Bunkatsu) {
                keys.put(SOGOSHOKEN_6BUNKATSU, "1");
            }

            if (FORM_KNJA130_6KUDAN.equals(_form._formInfo._formname)) {
                if (StringUtils.isBlank(KnjDbUtils.getString(student._htrainRemarkHdat_2, "TOTALSTUDYVAL2"))) {
                    // 斜線
                    keys.put(KUDAN_SHASEN, "1");
                }
            }

            final Set<String> slashs = new TreeSet<String>();
            final Set<String> hyokas = new TreeSet<String>();
            if (Arrays.asList(FORM_KNJA130C_4NARA, FORM_KNJA130C_4ANARA).contains(_form._formInfo._formname)) {
                // 留学欄斜線
                final StringBuffer keyStb = new StringBuffer();
                for (final String year : pageYearPosMap.keySet()) {
                    final Integer pos = pageYearPosMap.get(year);
                    // 出欠備考
                    final AttendRec attendrec = student._attendRecMap.get(year);
                    if (null != attendrec && (!NumberUtils.isDigits(attendrec._abroad) || NumberUtils.isDigits(attendrec._abroad) && Integer.parseInt(attendrec._abroad) == 0)) {
                        keyStb.append(pos);
                        slashs.add(String.valueOf(pos));
                    }
                }
                if (!slashs.isEmpty()) {
                    keys.put(SLASH, Util.mkString(slashs, "||").toString());
                }
                if (pInfo._gakushuBiko.hasDaitai90(GakushuBiko.DAITAI_TYPE.ZENBU)) {
                    // 全部代替があれば評価欄は斜線
                    if (FORM_KNJA130C_4NARA.equals(_form._formInfo._formname)) {
                        keyStb.append("HYOKA");
                        hyokas.add("_0");
                    } else if (FORM_KNJA130C_4ANARA.equals(_form._formInfo._formname)) {
                        for (final String year : pageYearPosMap.keySet()) {
                            final Integer pos = pageYearPosMap.get(year);
                            keyStb.append("HYOKA" + pos);
                            hyokas.add("_" + pos.toString());
                        }
                    }
                }
                if (!hyokas.isEmpty()) {
                    keys.put(HYOKA, Util.mkString(hyokas, "||").toString());
                }
            }

            if (param()._z010.in(Z010.ktsushin) && param()._is133m) {
                keys.put(TEXT_SHUKKOU, "1");
            }

            final String sogoSubclassname = pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap);
            if (!PersonalInfo.SOGOTEKI_NA_GAKUSHU_NO_JIKAN.equals(sogoSubclassname)) {
                keys.put(TEXT_SOGO, "1");
            }

            if (param()._z010.in(Z010.shimaneken)) {
                keys.put(SHIMANEKEN_SOGOSHOKEN_SECOND_FIELD, "1");
                keys.put(SHIMANEKEN_SOGAKU_SECOND_FIELD, "1");
                keys.put(SHIMANEKEN_TOKUBETSUKATUDOU_SECOND_FIELD, "1");
                keys.put(SHIMANEKEN_ATTENDREMARK_SECOND_FIELD, "1");
            }

            if (param()._z010.in(Z010.naganoSeisen)) {
                keys.put(NAGANOSEISEN_SOGOSHOKEN, "1");
            }

            if (_isKenjaForm) {
                // 特別活動の記録のフィールド桁数が11文字(22桁)でプロパティーのサイズが12文字の場合、フィールドを24桁に変更する
                final KNJPropertiesShokenSize spSize = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_SpecialactremarkSize), -1, -1);
                log.info(" spSize " + spSize._mojisu);
                if (spSize._mojisu == 12) {
                    SvfField spActField = _form._formInfo.getSvfField("SPECIALACTREMARK_1", false);
                    if (null != spActField && spActField._fieldLength == 11 * 2) {
                        keys.put(SPACT_EXTENDS, "1");
                    }
                }
            }

            if (param()._isOutputDebug) {
                log.info(" modifyKey = " + keys);
            }

            return modifyForm0(_form._formInfo._formname, pInfo, printGakuseki, keys);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final PrintGakuseki printGakuseki, final Map<String, String> flgMap) {

            boolean modified = false;
            if (flgMap.containsKey(SOGOSHOKEN_6BUNKATSU)) {
                final List<SvfForm.KoteiMoji> sogoTitle = svfForm.getKoteiMojiListWithText("総合所見及び指導上参考となる諸事項");
                final List<SvfForm.KoteiMoji> shukketsuTitle = svfForm.getKoteiMojiListWithText("出欠の記録");
                if (sogoTitle.isEmpty() || shukketsuTitle.isEmpty()) {
                    log.warn(" no field " + sogoTitle + " or " + shukketsuTitle);
                } else {
                    final SvfForm.Line upper = svfForm.getNearestLowerLine(sogoTitle.get(0).getPoint());
                    final SvfForm.Line lower = svfForm.getNearestUpperLine(shukketsuTitle.get(0).getPoint());
                    final SvfForm.Point p1 = upper._start;
                    final SvfForm.Point p2 = lower._end;
                    final int gradeWidth = 100;

                    final Map<String, List<SvfForm.Repeat>> repeats = new HashMap<String, List<SvfForm.Repeat>>();
                    for (final SvfForm.Element e : svfForm.getAllElementList()) {
                        if (e instanceof SvfForm.Positioned) {
                            SvfForm.Positioned p = (SvfForm.Positioned) e;
                            final int x = p.getPoint()._x;
                            final int y = p.getPoint()._y;
                            if (p1._x < x && x < p2._x && p1._y < y && y < p2._y) {
                                svfForm.removeElement(e);

                                if (e instanceof SvfForm.Field) {
                                    final SvfForm.Field f = (SvfForm.Field) e;
                                    if (f._repeatConfig._repeatCount != 0) {
                                        getMappedList(repeats, f._repeatConfig._repeatNo).add(svfForm.getRepeat(f._repeatConfig._repeatNo));
                                    }
                                }
                            }
                        }
                    }
                    for (final Map.Entry<String, List<SvfForm.Repeat>> e : repeats.entrySet()) {
                        for (final SvfForm.Repeat r : e.getValue()) {
                            svfForm.removeRepeat(r);
                        }
                    }

                    svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, upper._start, lower._start).addX(gradeWidth)); // 学年縦線

                    final int[] xs = new int[3];
                    for (int i = 0; i < 3; i++) {
                        xs[i] = i * (upper._end._x - upper._start._x - gradeWidth) / 3 + upper._start._x + gradeWidth;
                        if (i != 0) {
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, upper._start, lower._start).setX(xs[i]));
                        }
                    }

                    final int height = (p2._y - p1._y);
                    final int gradeCount = 4;
                    final int gh = height / gradeCount;
                    for (int gi = 0; gi < gradeCount; gi++) {
                        svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, upper._start.addX(gradeWidth), upper._end).addY(gh * gi + gh / 2)); // 横線
                        final int g = gi + 1;
                        if (gi != gradeCount - 1) {
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THINEST, upper._start, upper._end).addY(gh * g)); // 横線
                        }
                        final int gradeStarty = (int) (1.5 * gh / 4 + p1._y + gh * gi);
                        final int gradeEndy = (int) (2.5 * gh / 4 + p1._y + gh * gi);
                        svfForm.addField(new SvfForm.Field("", "GRADE2_" + String.valueOf(g), SvfForm.Font.Mincho, 6, gradeEndy, true, new SvfForm.Point(upper._start._x + 25, gradeStarty), 100, null).setPrintMethod(SvfForm.Field.PrintMethod.CENTERING).setZenkaku(true));

                        final int repeatCount = 4;
                        final double repeatPitchPoint = 2.5;
                        final SvfForm.Field.RepeatConfig rc = new SvfForm.Field.RepeatConfig(String.valueOf(180 + gi), repeatCount, 1, -1, 0).setRepeatPitchPoint(repeatPitchPoint);
                        svfForm.addRepeat(new SvfForm.Repeat(String.valueOf(180 + gi), upper._start.addX(gradeWidth)._x, upper._start._y + gh * gi, upper._end._x, upper._start._y + gh * (gi + 1), 0, repeatCount, rc._repeatPitch, 1, "1"));

                        for (int seqi = 1; seqi <= 6; seqi++) {
                            String title = "";
                            switch (seqi) {
                            case 1 : title = "(1)学習における特徴等"; break;
                            case 2 : title = "(2)行動の特徴，特技等"; break;
                            case 3 : title = "(3)部活動，ボランティア活動，留学・海外経験等"; break;
                            case 4 : title = "(4)取得資格，検定等"; break;
                            case 5 : title = "(5)表彰・顕彰等の記録"; break;
                            case 6 : title = "(6)その他"; break;
                            }
                            final boolean is2danme = seqi > 3;
                            final int width = xs[1] - xs[0] - 20;
                            final int itemX = xs[seqi - (is2danme ? 3 : 0) - 1] + 4;
                            final int itemY = upper._start._y + gh * gi + (is2danme ? gh / 2 : 0);
                            svfForm.addKoteiMoji(new SvfForm.KoteiMoji(title, new SvfForm.Point(itemX, itemY), 60));

                            svfForm.addField(new SvfForm.Field("", "field8_" + String.valueOf(seqi) + "_" + String.valueOf(g), SvfForm.Font.Mincho, 53, itemX + width, false, new SvfForm.Point(itemX, itemY + 35), 60, "").setRepeatConfig(rc));
                        }
                    }
                }
            }

            if (flgMap.containsKey(SHIMANEKEN_SOGOSHOKEN_SECOND_FIELD)) {
                final int charpoint3p = 30; // 3.0
                if (pInfo.useSeitoSidoYorokuSenmonGakkaForm(param())) {
                    final double pitchPoint12;
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            pitchPoint12 = 1.15;
                        } else {
                            pitchPoint12 = 1.6;
                        }
                    } else {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            pitchPoint12 = 1.02;
                        } else {
                            pitchPoint12 = 1.02;
                        }
                    }
                    final int moji12 = 80;
                    final int gyosu12 = 16;
                    final SvfForm.Field.RepeatConfig rc12 = new SvfForm.Field.RepeatConfig("1", gyosu12 - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint12);

                    for (int g = 1; g <= 4; g++) {
                        for (int k = 1; k <= 2; k++) {
                            final String fieldname = "field8_" + k + "_" + g;
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null == field) {
                                continue;
                            }
                            final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").setFieldLength(moji12 * 2 + 1).setEndX(field._endX).setCharPoint10(charpoint3p).setRepeatConfig(rc12);
                            svfForm.addField(field2);
                        }
                    }

                    final int moji3 = 41;
                    final int gyosu3 = 17;
                    int addy = 0;
                    final double pitchPoint3;
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            pitchPoint3 = 1.3;
                        } else {
                            addy = - 10;
                            pitchPoint3 = 1.5;
                        }
                    } else {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            pitchPoint3 = 1.05;
                        } else {
                            pitchPoint3 = 1.25;
                        }
                    }
                    final SvfForm.Field.RepeatConfig rc3 = new SvfForm.Field.RepeatConfig("1", gyosu3 - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint3);
                    for (int g = 1; g <= 4; g++) {
                        for (int k = 3; k <= 3; k++) {
                            final String fieldname = "field8_" + k + "_" + g;
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null == field) {
                                continue;
                            }
                            final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").addY(addy).setFieldLength(moji3 * 2 + 1).setEndX(field._endX).setCharPoint10(charpoint3p).setRepeatConfig(rc3);
                            svfForm.addField(field2);
                        }
                    }
                } else {
                    final double pitchPoint;
                    int addy;
                    int charpoint = 52; // 5.2
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy = 0;
                            pitchPoint = 1.74;
                        } else {
                            addy = -15;
                            pitchPoint = 2.25;
                        }
                    } else {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy = 0;
                            charpoint = 44;
                            pitchPoint = 1.44;
                        } else {
                            addy = -15;
                            pitchPoint = 1.74;
                        }
                    }
                    final int gyosu = 16;
                    final SvfForm.Field.RepeatConfig rc6p = new SvfForm.Field.RepeatConfig("1", gyosu - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint);

                    for (int g = 1; g <= 4; g++) {
                        for (int k = 1; k <= 3; k++) {
                            final String fieldname = "field8_" + k + "_" + g;
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null == field) {
                                continue;
                            }
                            final SvfForm.Field field2 = field.copyTo(fieldname+ "_6P").addY(addy).setFieldLength(27 * 2 + 1).setCharPoint10(charpoint).setRepeatConfig(rc6p);
                            svfForm.addField(field2);
                        }
                    }

                    final double pitchPoint2;
                    int addy2 = -15;
                    int charpoint2 = 30; // 3.0
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy2 = 0;
                            pitchPoint2 = 1.55;
                        } else {
                            pitchPoint2 = 2.0;
                        }
                    } else {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy2 = 0;
                            pitchPoint2 = 1.27;
                        } else {
                            pitchPoint2 = 1.55;
                        }
                    }
                    final int gyosu2 = 18;
                    final SvfForm.Field.RepeatConfig rc3 = new SvfForm.Field.RepeatConfig("1", gyosu2 - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint2);

                    for (int g = 1; g <= 4; g++) {
                        for (int k = 1; k <= 3; k++) {
                            final String fieldname = "field8_" + k + "_" + g;
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null == field) {
                                continue;
                            }
                            final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").addY(addy2).setFieldLength(52 * 2 + 1).setCharPoint10(charpoint2).setRepeatConfig(rc3);
                            svfForm.addField(field2);
                        }
                    }
                }
            }

            if (flgMap.containsKey(SHIMANEKEN_SOGAKU_SECOND_FIELD)) {
                final double pitchPoint6p;
                final int charpoint6p = 60; // 6.0
                if (is3nenYou(pInfo)) {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint6p = 2.1;
                    } else {
                        pitchPoint6p = 2.4;
                    }
                } else {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint6p = 2.1;
                    } else {
                        pitchPoint6p = 2.4;
                    }
                }
                final int mojisu6p = 17;
                final int gyosu6p = 7;
                final SvfForm.Field.RepeatConfig rc6p = new SvfForm.Field.RepeatConfig("1", gyosu6p - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint6p);

                for (int k = 1; k <= 2; k++) {
                    for (int g = 1; g <= 4; g++) {
                        final String fieldname = "REC_" + String.valueOf(k) + "_" + g;
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null == field) {
                            continue;
                        }
                        final SvfForm.Field field2 = field.copyTo(fieldname+ "_6P").setFieldLength(mojisu6p * 2 + 1).setEndX(field._endX).setCharPoint10(charpoint6p).setRepeatConfig(rc6p);
                        svfForm.addField(field2);
                    }
                }

                final double pitchPoint3p;
                final int charpoint3p = 30; // 3.0
                int addy = 0;
                if (is3nenYou(pInfo)) {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        addy = -10;
                        pitchPoint3p = 1.53;
                    } else {
                        addy = -10;
                        pitchPoint3p = 1.59;
                    }
                } else {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        addy = -5;
                        pitchPoint3p = 1.46;
                    } else {
                        addy = -10;
                        pitchPoint3p = 1.78;
                    }
                }
                final int mojisu3p = 40;
                final int gyosu3p = 11;
                final SvfForm.Field.RepeatConfig rc3p = new SvfForm.Field.RepeatConfig("1", gyosu3p - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint3p);

                for (int k = 1; k <= 2; k++) {
                    for (int g = 1; g <= 4; g++) {
                        final String fieldname = "REC_" + String.valueOf(k) + "_" + g;
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null == field) {
                            continue;
                        }
                        final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").setFieldLength(mojisu3p * 2 + 1).addX(-10).addY(addy).setEndX(field._endX).setCharPoint10(charpoint3p).setRepeatConfig(rc3p);
                        svfForm.addField(field2);
                    }
                }
            }

            if (flgMap.containsKey(SHIMANEKEN_TOKUBETSUKATUDOU_SECOND_FIELD)) {
                final double pitchPoint6p;
                final int charpoint6p = 60; // 6.0
                if (is3nenYou(pInfo)) {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint6p = 2.6;
                    } else {
                        pitchPoint6p = 3.0;
                    }
                } else {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint6p = 2.2;
                    } else {
                        pitchPoint6p = 2.93;
                    }
                }
                final int mojisu6p = 18;
                final int gyosu6p = 11;
                final SvfForm.Field.RepeatConfig rc6p = new SvfForm.Field.RepeatConfig("1", gyosu6p - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint6p);

                for (int g = 1; g <= 4; g++) {
                    final String fieldname = "SPECIALACTREMARK_" + g;
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null == field) {
                        continue;
                    }
                    final SvfForm.Field field2 = field.copyTo(fieldname+ "_6P").setFieldLength(mojisu6p * 2).setEndX(field._endX).setCharPoint10(charpoint6p).setRepeatConfig(rc6p);
                    svfForm.addField(field2);
                }

                final double pitchPoint3p;
                final int charpoint3p = 30; // 3.0
                if (is3nenYou(pInfo)) {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint3p = 1.44;
                    } else {
                        pitchPoint3p = 1.64;
                    }
                } else {
                    if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                        pitchPoint3p = 1.21;
                    } else {
                        pitchPoint3p = 1.59;
                    }
                }
                final int mojisu3p = 31;
                final int gyosu3p = 20;
                final SvfForm.Field.RepeatConfig rc3p = new SvfForm.Field.RepeatConfig("1", gyosu3p - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint3p);

                for (int g = 1; g <= 4; g++) {
                    final String fieldname = "SPECIALACTREMARK_" + g;
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null == field) {
                        continue;
                    }
                    final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").setFieldLength(mojisu3p * 2).setEndX(field._endX).setCharPoint10(charpoint3p).setRepeatConfig(rc3p);
                    svfForm.addField(field2);
                }
            }

            if (flgMap.containsKey(SHIMANEKEN_ATTENDREMARK_SECOND_FIELD)) {
                // #SHIMANEKEN_ATTENDREMARK_SECOND_FIELD
                {
                    final int charpoint6p = 60; // 6.0
                    int addy = -5;
                    double pitchPoint;
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            pitchPoint = 2.25;
                        } else {
                            pitchPoint = 2.6;
                        }
                    } else {
                        pitchPoint = 2.25;
                    }
                    final int moji = 30;
                    final int gyosu = 3;
                    final SvfForm.Field.RepeatConfig rc6p = new SvfForm.Field.RepeatConfig("1", gyosu - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint);

                    for (int g = 1; g <= 4; g++) {
                        final String fieldname = "syuketu_8_" + String.valueOf(g);
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null == field) {
                            continue;
                        }
                        final SvfForm.Field field2 = field.copyTo(fieldname+ "_6P").addY(addy).setFieldLength(moji * 2).setCharPoint10(charpoint6p).setRepeatConfig(rc6p);
                        svfForm.addField(field2);
//                        log.info(" add field2 " + field2);
                    }
                }
                {
                    final int charpoint3p = 30; // 3.0
                    int addy2 = -5;
                    final double pitchPoint;
                    if (is3nenYou(pInfo)) {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy2 = 0;
                            pitchPoint = 1.3;
                        } else {
                            pitchPoint = 1.8;
                        }
                    } else {
                        if (pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                            addy2 = 0;
                            pitchPoint = 1.27;
                        } else {
                            pitchPoint = 1.4;
                        }
                    }
                    final int moji = 59;
                    final int gyosu = 5;
                    final SvfForm.Field.RepeatConfig rc3p = new SvfForm.Field.RepeatConfig("1", gyosu - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint);

                    for (int g = 1; g <= 4; g++) {
                        final String fieldname = "syuketu_8_" + String.valueOf(g);
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null == field) {
                            continue;
                        }
                        final SvfForm.Field field2 = field.copyTo(fieldname+ "_3P").addY(addy2).setFieldLength(moji * 2).setCharPoint10(charpoint3p).setRepeatConfig(rc3p);
                        svfForm.addField(field2);
//                        log.info(" add field2 " + field2);
                    }
                }

            }

            if (flgMap.containsKey(KUDAN_SHASEN)) {
                SvfField field_rec_4 = _form._formInfo.getSvfField("rec_4", true);
                if (null != field_rec_4) {
                    final SvfForm.Line rec_4_shasen = wakuShasen(svfForm, field_rec_4);
                    svfForm.addLine(rec_4_shasen);
                    modified = true;
                }
            }


            if (flgMap.containsKey(HYOKA)) {
                final String[] hyokas = StringUtils.split(flgMap.get(HYOKA), "||");
                for (final String rec_2_pos : hyokas) {
                    final SvfField field_rec_2 = _form._formInfo.getSvfField("REC_2" + ("_0".equals(rec_2_pos) ? "" : rec_2_pos), true);
                    svfForm.addLine(wakuShasen(svfForm, field_rec_2));
                    modified = true;
                }
            }

            if (flgMap.containsKey(SLASH)) {
                final String[] slashs = StringUtils.split(flgMap.get(SLASH), "||");
                for (final String pos : slashs) {
                    final SvfField field_syuketu_4 = _form._formInfo.getSvfField("syuketu_4_" + pos, true);
                    if (null != field_syuketu_4) {
                        final SvfForm.Line rec_4_shasen = wakuShasen(svfForm, field_syuketu_4);
                        svfForm.addLine(rec_4_shasen);
                        modified = true;
                    }
                }
            }

            if (flgMap.containsKey(TEXT_SOGO)) {
                final List<SvfForm.KoteiMoji> koteiMojiList = svfForm.getElementList(SvfForm.KoteiMoji.class);
                for (final SvfForm.KoteiMoji moji : koteiMojiList) {
                    if (moji._moji.contains(PersonalInfo.SOGOTEKI_NA_GAKUSHU_NO_JIKAN)) {
                        if (param()._isOutputDebug) {
                            log.info(" replace " + moji._moji + "");
                        }
                        final String sogoSubclassname_ = pInfo.getSogoSubclassname(param(), printGakuseki._yearGakusekiMap);
                        svfForm.move(moji, moji.replaceMojiWith(moji._moji.replaceAll(PersonalInfo.SOGOTEKI_NA_GAKUSHU_NO_JIKAN, sogoSubclassname_)));
                        modified = true;
                    }
                }
            }

            if (flgMap.containsKey(SPACT_EXTENDS)) {
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    if (!field._fieldname.matches("SPECIALACTREMARK_[0-9]+")) {
                        continue;
                    }
                    svfForm.removeField(field);
                    final SvfForm.Field newField = field.setX(field._position._x - 30).setFieldLength(12 * 2);
                    svfForm.addField(newField);
                    if (param()._isOutputDebug) {
                        log.info(" move field " + field + " to " + newField);
                    }
                    modified = true;
                }
            }

            if (flgMap.containsKey(NAGANOSEISEN_SOGOSHOKEN)) {
                final int charpoint95p = 75; // 7.5
                final int gyosu = 9;
                final double pitchPoint = 2.7;
                final int addy = -15;

                final SvfForm.Field.RepeatConfig rc = new SvfForm.Field.RepeatConfig("1", gyosu - 1, 1, -1, 0).setRepeatPitchPoint(pitchPoint);

                for (int g = 1; g <= 4; g++) {
                    final String fieldname = "rec_3_" + String.valueOf(g);
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    svfForm.addField(field.copyTo(fieldname+ "_9GYO").addY(addy).setCharPoint10(charpoint95p).setRepeatConfig(rc));
                }
            }

            if (flgMap.containsKey(TEXT_SHUKKOU)) {
                final List<SvfForm.KoteiMoji> koteiMojiList = svfForm.getKoteiMojiListWithText("出欠の記録");
                log.info(" koteiMo = " + koteiMojiList);
                for (final SvfForm.KoteiMoji moji : koteiMojiList) {
                    svfForm.move(moji, moji.replaceMojiWith("出校の記録"));
                    modified = true;
                }
            }

            return modified;
        }

        private List<String> daitaibiko90AllYear(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final Param param) {
            final List<String> studyRecSubstitution90AllYear = new ArrayList();
            if (!param().isNotPrintDaitai("2ura")) {
                for (final String year0 : printGakuseki._yearList) {
                    if (!pInfo.isTargetYearLast(year0, student, param(), true)) {
                        continue;
                    }
                    final List<String> yearBikoList = pInfo.getArraySubstitutionBiko90(year0, param);
                    if (param._isOutputDebugSeiseki) {
                        log.info(" studyRecSubstitution90All(" + year0 + ") = " + yearBikoList);
                    }
                    for (final String yearBiko : yearBikoList) {
                        if (!studyRecSubstitution90AllYear.contains(yearBiko)) {
                            studyRecSubstitution90AllYear.add(yearBiko);
                        }
                    }
                }
                if (param._isOutputDebugSeiseki) {
                    log.info(" studyRecSubstitution90AllYear = " + studyRecSubstitution90AllYear);
                }
            }
            return studyRecSubstitution90AllYear;
        }

        private boolean hasHtrainremarkCarrerPlanVal(Student student, final PrintGakuseki printGakuseki) {
            return true;
//            for (final Iterator itg = printGakuseki._yearGakusekiMap.keySet().iterator(); itg.hasNext();) {
//
//                final String year = (String) itg.next();
//                final Gakuseki gakuseki = (Gakuseki) printGakuseki._yearGakusekiMap.get(year);
//
//                // 所見データを印刷
//                final HtrainRemark remark = (HtrainRemark) student._htrainRemarkMap.get(gakuseki._year);
//                if (null != remark && null != remark.careerPlanVal()) {
//                    return true;
//                }
//            }
//            return false;
        }

        private void printSogoMatome(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<String> studyRecSubstitution90AllYear, final PrintGakuseki printGakuseki) {
            if (param()._isOutputDebug) {
                log.info("sogo matome");
            }
            final TreeSet<String> gakusekiYearSet = new TreeSet(printGakuseki._yearGakusekiMap.keySet());
            if (param()._is133m) {

                final List<Gakuseki> gakusekiMapGakusekiList = new ArrayList(printGakuseki._yearGakusekiMap.values());
                Collections.sort(gakusekiMapGakusekiList, new Gakuseki.GakusekiComparator(student, pInfo, param()));

                // 総合的な学習の時間の記録の所見を印刷
                final List<String> actLines = new ArrayList<String>();
                final List<String> valLines = new ArrayList<String>();
                int keta = 88;
                if (param()._z010.in(Z010.ktsushin)) {
                    keta += 6 * 2; // 年度ヘッダ分拡張
                }
                for (final Gakuseki gakuseki : gakusekiMapGakusekiList) {

//                    if (!param().isGakunenSei() && isAnotherSchoolYear(gakuseki._year)) {
//                        continue;
//                    }

                    final HtrainRemark remark = student._htrainRemarkMap.get(gakuseki._year);
                    if (null == remark) {
                        continue;
                    }
                    if (gakuseki.isNotPrint(param())) {
                        continue;
                    }

                    String totalStudyAct = defstr(remark.totalstudyact());
                    if (param()._z010.in(Z010.ktsushin) && !StringUtils.isBlank(totalStudyAct)) {
                        totalStudyAct = Util.prepend(Util.append(nendoWareki(db2, remark.year()), "："), totalStudyAct);
                    }
                    actLines.addAll(Util.getTokenList(param(), totalStudyAct, keta));           // 総合的な学習の時間学習活動
                    if (!param().isNotPrintDaitai("2ura")) {
                        final String substBiko = Util.mkString(pInfo.getArraySubstitutionBiko90M(gakuseki._year), "、").toString();
                        actLines.addAll(Util.getTokenList(param(), substBiko, keta));               // 総合的な学習の時間の代替科目備考
                    }


                    String totalStudyVal = remark.totalstudyval();
                    if (param()._z010.in(Z010.ktsushin) && !StringUtils.isBlank(totalStudyVal)) {
                        totalStudyVal = Util.prepend(Util.append(nendoWareki(db2, remark.year()), "："), totalStudyVal);
                    }
                    valLines.addAll(Util.getTokenList(param(), totalStudyVal, keta)); // 総合的な学習の時間評価
                }

                final String fieldAct = "rec_1";
                final String fieldVal = "rec_2";

                printSvfRenban(fieldAct, actLines);    // 学習活動：44文字×6行
                printSvfRenban(fieldVal, valLines);    // 評価：44文字×6行
                return;
            }

            final List<String> totalStudyActList = new ArrayList<String>();
            final List<String> totalStudyValList = new ArrayList<String>();
            final List<String> careerPlanActList = new ArrayList<String>();
            final List<String> careerPlanValList = new ArrayList<String>();

            String fieldAct = "rec_1";
            String fieldVal = "rec_2";
            final int defKeta;
            if (param()._z010.in(Z010.tokiwa)) {
                defKeta = 45 * 2;
            } else {
                final String prop = defstr(_seitoSidoYoroku_dat_TotalstudyactSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize));
                defKeta = KNJPropertiesShokenSize.getShokenSize(prop, pInfo._isSogoShoken3Bunkatsu ? 66 : 44, 99).getKeta();
            }
            if (pInfo._isSogoShoken3Bunkatsu && defKeta == 132) {
                fieldAct = "rec_1_2";
                fieldVal = "rec_2_2";
            }
            final int keta = Math.min(_form._formInfo.getFieldLength(fieldAct, defKeta), _form._formInfo.getFieldLength(fieldVal, defKeta));
            int printGradecdInt = 1;
            final String empty = "";
            if (param()._isOutputDebug) {
                log.info(" gakusekiYearSet = " + gakusekiYearSet);
            }
            for (final String year : gakusekiYearSet) {

                final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);

                // 所見データを印刷
                final HtrainRemark remark = null == student._htrainRemarkMap.get(gakuseki._year) ? HtrainRemark.Null : student._htrainRemarkMap.get(gakuseki._year);

                if (param()._z010.in(Z010.tokiwa, Z010.yamamura)) {
                    final int actLineMax = param()._z010.in(Z010.yamamura) ? 1 : 2;
                    final int valLineMax = 2;
                    if (NumberUtils.isDigits(gakuseki._gdat._gradeCd)) {
                        final int gradecdInt = Integer.parseInt(gakuseki._gdat._gradeCd);
                        for (int tg = printGradecdInt; tg < gradecdInt - 1; tg++) {
                            totalStudyActList.addAll(setMaxSize(actLineMax, empty, Util.getTokenList(param(), gradeNameHead(tg), keta)));
                        }
                        totalStudyActList.addAll(setMaxSize(actLineMax, empty, Util.getTokenList(param(), gradeNameHead(gradecdInt) + defstr(remark.totalstudyact()), keta)));
                        for (int tg = printGradecdInt; tg < gradecdInt - 1; tg++) {
                            totalStudyValList.addAll(setMaxSize(valLineMax, empty, Util.getTokenList(param(), gradeNameHead(tg), keta)));
                        }
                        totalStudyValList.addAll(setMaxSize(valLineMax, empty, Util.getTokenList(param(), gradeNameHead(gradecdInt) + defstr(remark.totalstudyval()), keta)));
                        printGradecdInt = gradecdInt;
                    }
                } else {
                    totalStudyActList.add(remark.totalstudyact());
                    totalStudyValList.add(remark.totalstudyval());
                    careerPlanActList.add(remark.careerPlanAct());
                    careerPlanValList.add(remark.careerPlanVal());
                }
            }

            final int actLines = _form.getFieldRepeatCount(fieldAct, pInfo._isSogoShoken3Bunkatsu || param()._z010.in(Z010.tokiwa, Z010.miyagiken, Z010.mieken) ? 6 : 4);
            final int valLines = _form.getFieldRepeatCount(fieldVal, 6);

            // 「総合的な学習の時間の記録」を印字
            if (student._isShowStudyRecBikoSubstitution90) {
                totalStudyActList.addAll(studyRecSubstitution90AllYear);
            }
            final String act1 = Util.mkString(totalStudyActList, "\n").toString();
            final String val1 = Util.mkString(totalStudyValList, "\n").toString();
            if (param()._isOutputDebugInner) {
                log.info(" 活動内容:" + act1);
                log.info(" 評価:" + val1);
            }
            if (usePrintMethod(Sougaku, PreferKeta)) {
                printSvfRenbanUseFieldArePreferKeta(fieldAct, act1, defKeta);    // 「学習活動」の欄
                printSvfRenbanUseFieldArePreferKeta(fieldVal, val1, defKeta);    // 「評価」の欄
            } else if (usePrintMethod(Sougaku, PreferPointThanKeta)) {
                printSvfRenbanUseFieldArePreferPointThanKeta(fieldAct, act1, 10.1);    // 「学習活動」の欄
                printSvfRenbanUseFieldArePreferPointThanKeta(fieldVal, val1, 10.1);    // 「評価」の欄
            } else {
                printSvfRenban(fieldAct, act1, keta, actLines);    // 「学習活動」の欄
                printSvfRenban(fieldVal, val1, keta, valLines);    // 「評価」の欄
            }

            if ("1".equals(param().property(Property.seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg))) {
                svfVrsOut("TOTAL_STUDYACT_NAME", defstr(param()._careerPlanItemName, "キャリアプラン"));
                printSvfRenban("rec_3", Util.mkString(careerPlanActList, "\n").toString(), keta, actLines);    // 「学習活動」の欄
                printSvfRenban("rec_4", Util.mkString(careerPlanValList, "\n").toString(), keta, valLines);    // 「評価」の欄
            }
        }

        private String gradeNameHead(final int gradeCdInt) {
            if (param()._z010.in(Z010.yamamura)) {
                return String.valueOf(gradeCdInt) + "年：";
            }
            return "第" + String.valueOf(gradeCdInt) + "学年：";
        }

        private static <T> List<T> setMaxSize(final int size, final T o, final List<T> list) {
            while (list.size() < size) {
                list.add(o);
            }
            return list;
        }

        private void printSogoNendogoto(final Student student, final PersonalInfo pInfo, final String year, final int pos) {
            // 所見データを印刷
            final HtrainRemark remark = null == student._htrainRemarkMap.get(year) ? HtrainRemark.Null : student._htrainRemarkMap.get(year);

            final List<String> studyRecSubstitution90Tannen;
            if (student._isShowStudyRecBikoSubstitution90 && !param().isNotPrintDaitai("2ura")) {
                studyRecSubstitution90Tannen = pInfo.getArraySubstitutionBiko90(year, param());
            } else {
                studyRecSubstitution90Tannen = Collections.emptyList();
            }

            final String spos = String.valueOf(pos);
            if (param()._z010.in(Z010.meiji)) {
//                if (param()._isSeitoSidoYorokuKinsokuForm) {
//                    svfVrsOut("rec_" + spos, remark.totalstudyval());   // 総合的な学習の時間評価
//                } else {
                    final List<String> arrstr = Util.getTokenList(param(), remark.totalstudyval(), 42 * 2, 2);
                    for (int l = 0; l < arrstr.size(); l++) {
                        if (null == arrstr.get(l)) {
                            continue;
                        }
                        svfVrsOutn("rec_1_" + (l + 1), pos, arrstr.get(l));
                    }
//                }
            } else {
                final String totalStudyAct = Util.mkString(cons(remark.totalstudyact(), studyRecSubstitution90Tannen), "\n").toString();
                final String totalStudyVal = remark.totalstudyval();
                final String propAct = defstr(_seitoSidoYoroku_dat_TotalstudyactSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize));
                final String propVal = defstr(_seitoSidoYoroku_dat_TotalstudyvalSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize));
                String fieldAct, fieldVal;
                KNJPropertiesShokenSize sizeAct;
                KNJPropertiesShokenSize sizeVal;
                if (param()._z010.in(Z010.nishiyama)) {
                    fieldAct = "rec_1_" + spos;
                    fieldVal = "rec_2_" + spos;
                    sizeAct = KNJPropertiesShokenSize.getShokenSize(propAct, 20, 5);
                    sizeVal = KNJPropertiesShokenSize.getShokenSize(propVal, 20, 6);
                } else if (param()._z010.in(Z010.seijyo) || param()._z010.in(Z010.chiyodaKudan)) {
                    fieldAct = "rec_1_" + spos;
                    fieldVal = "rec_2_" + spos;
                    sizeAct = KNJPropertiesShokenSize.getShokenSize(propAct, 22, 5);
                    sizeVal = KNJPropertiesShokenSize.getShokenSize(propVal, 22, 5);
                } else if (param()._z010.in(Z010.shimaneken)) {
                    // SHIMANEKEN_SOGAKU_SECOND_FIELD
                    // 学習内容
                    fieldAct = "rec_1_" + spos;
                    sizeAct = KNJPropertiesShokenSize.getShokenSize(null, 11, 4);
                    if (Util.getTokenList(param(), totalStudyAct, sizeAct.getKeta()).size() > sizeAct._gyo) {
                        fieldAct = "rec_1_" + spos + "_6P";
                        sizeAct = KNJPropertiesShokenSize.getShokenSize(null, 17, 7);
                    }
                    if (Util.getTokenList(param(), totalStudyAct, sizeAct.getKeta()).size() > sizeAct._gyo) {
                        fieldAct = "rec_1_" + spos + "_3P";
                        sizeAct = KNJPropertiesShokenSize.getShokenSize(null, 40, 11);
                    }

                    // 評価
                    fieldVal = "rec_2_" + spos;
                    sizeVal = KNJPropertiesShokenSize.getShokenSize(null, 11, 4);
                    if (Util.getTokenList(param(), totalStudyVal, sizeVal.getKeta()).size() > sizeVal._gyo) {
                        fieldVal = "rec_2_" + spos + "_6P";
                        sizeVal = KNJPropertiesShokenSize.getShokenSize(null, 17, 7);
                    }
                    if (Util.getTokenList(param(), totalStudyVal, sizeVal.getKeta()).size() > sizeVal._gyo) {
                        fieldVal = "rec_2_" + spos + "_3P";
                        sizeVal = KNJPropertiesShokenSize.getShokenSize(null, 40, 11);
                    }
                } else if ("0".equals(param().property(Property.seitoSidoYorokuFieldSize))) {
                    //学習活動：11文字×4行, 評価：11文字×6行
                    int addKeta = 0;
                    if (param()._z010.in(Z010.kyoto)) { // && !param()._isSeitoSidoYorokuKinsokuForm) {
                        addKeta = 1;
                    }
                    sizeAct = KNJPropertiesShokenSize.getShokenSize(propAct, 11, 4).createAddKeta(addKeta);
                    sizeVal = KNJPropertiesShokenSize.getShokenSize(propVal, 11, 6).createAddKeta(addKeta);
                    if (param()._z010.in(Z010.naraken) && Util.getTokenList(totalStudyAct, sizeAct.getKeta()).size() > sizeAct._gyo) {
                        fieldAct = "rec_1_1" + spos;
                        sizeAct = KNJPropertiesShokenSize.getShokenSize(null, 22, 12).createAddKeta(addKeta);
                    } else if (param()._z010.in(Z010.tottori) || param()._z010.in(Z010.kyoai) || param()._z010.in(Z010.kyoto)) {
                        fieldAct = "rec_1_1" + spos;
                        sizeAct = KNJPropertiesShokenSize.getShokenSize(propAct, 11, 4).createAddKeta(addKeta);
                    } else {
                        fieldAct = "rec_1_" + spos;
                    }
                    if (param()._z010.in(Z010.naraken) && Util.getTokenList(totalStudyVal, sizeVal.getKeta()).size() > sizeVal._gyo) {
                        fieldVal = "rec_2_1" + spos;
                        sizeVal = KNJPropertiesShokenSize.getShokenSize(null, 22, 12).createAddKeta(addKeta);
                    } else if (param()._z010.in(Z010.tottori) || param()._z010.in(Z010.kyoai) || param()._z010.in(Z010.kyoto)) {
                        fieldVal = "rec_2_1" + spos;
                        sizeVal = KNJPropertiesShokenSize.getShokenSize(propVal, 11, 6).createAddKeta(addKeta);
                    } else {
                        fieldVal = "rec_2_" + spos;
                    }
                } else {
                    //学習活動：22文字×8行, 評価：22文字×8行
                    if (FORM_KNJA130C_4A.equals(_form._formInfo._formname)) {
                        fieldAct = "rec_1_1" + spos;
                        fieldVal = "rec_2_1" + spos;
                    } else {
                        fieldAct = "rec_1_" + spos;
                        fieldVal = "rec_2_" + spos;
                    }
                    sizeAct = KNJPropertiesShokenSize.getShokenSize(propAct, 22, 8);
                    sizeVal = KNJPropertiesShokenSize.getShokenSize(propVal, 22, 8);
                }
                printSvfRenban(fieldAct, totalStudyAct, sizeAct);   // 総合的な学習の時間学習活動
                printSvfRenban(fieldVal, totalStudyVal, sizeVal);   // 総合的な学習の時間評価
            }
        }

        private static List<String> cons(final String s, final List<String> list) {
            if (StringUtils.isBlank(s)) {
                return list;
            }
            if (list.isEmpty()) {
                return Collections.singletonList(s);
            }
            final List<String> rtn = new ArrayList<String>();
            rtn.add(s);
            rtn.addAll(list);
            return rtn;
        }

        // 総合所見
        private void printSogoShoken(final PersonalInfo pInfo, final int i, final HtrainRemark remark, final HtrainRemarkDetail remarkDetail, final HtrainRemarkTrainref remarkTrainref) {
            final String si =  String.valueOf(i);
            // 所見
            if (pInfo._isSogoShoken6BunkatsuTo3Bunkatsu) {
                printTrainRef123456To123(pInfo, remark, remarkTrainref, i);
            } else if (pInfo._isSogoShoken6Bunkatsu) {
                printTrainRef123456(pInfo, remark, remarkTrainref, i);
            } else if (pInfo._isSogoShoken3Bunkatsu || param()._z010.in(Z010.tokiwa, Z010.seijyo)) {
                printTrainRef123(pInfo, remark, remarkDetail, i);
            } else if (param()._is133m) {
                final String prop = defstr(_seitoSidoYoroku_dat_TotalremarkSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalremarkSize));
                KNJPropertiesShokenSize sogoShokenSize = KNJPropertiesShokenSize.getShokenSize(prop, -1, -1);
                if (sogoShokenSize._mojisu == -1 || sogoShokenSize._gyo == -1) {
                    if (param()._z010.in(Z010.tokyoto, Z010.miyagiken)) {
                        sogoShokenSize = new KNJPropertiesShokenSize(44, 3);
                    } else {
                        sogoShokenSize = new KNJPropertiesShokenSize(22, 6);
                    }
                }
                final String field = "rec_3_" + si;
                if (param()._z010.in(Z010.mieken)) {
                    printSvfRenbanUseFieldArePreferKeta(field, remark.totalremark(), sogoShokenSize.getKeta());
                } else {
                    printDetailM(field, Util.get_token(param(), remark.totalremark(), sogoShokenSize));
                }
            } else {
                final String field;
                final int moji;
                final int gyo;
                if (param()._seitoSidoYorokuSougouFieldSize) {
                    field = "rec_4_" + si;
                    moji = 66;
                    gyo = 8;
                } else if ("1".equals(param().property(Property.seitoSidoYorokuFieldSize))) {
                    field = "rec_4_" + si;
                    moji = 66;
                    gyo = 8;
                } else if (param()._z010.in(Z010.kyoto) && kinsokuOver(remark.totalremark(), 44 * 2, 6)) {
                    field = "rec_5_" + si;
                    moji = 44;
                    gyo = 8;
                } else if (param()._z010.in(Z010.naganoSeisen) && kinsokuOver(remark.totalremark(), 44 * 2, 6)) {
                    field = "rec_3_" + si + "_9GYO";
                    moji = 44;
                    gyo = 9;
                } else {
                    field = "rec_3_" + si;
                    moji = 44;
                    gyo = 6;
                }
                final String prop = defstr(_seitoSidoYoroku_dat_TotalremarkSizeForPrint, param().property(Property.seitoSidoYoroku_dat_TotalremarkSize));
                int addKeta = 0;
                if (param()._z010.in(Z010.kyoto)) { // && !param()._isSeitoSidoYorokuKinsokuForm) {
                    addKeta = 1;
                }
                final KNJPropertiesShokenSize totSize = KNJPropertiesShokenSize.getShokenSize(prop, moji, gyo).createAddKeta(addKeta);
                if (usePrintMethod(SougouShoken, PreferKeta)) {
                    printSvfRenbanUseFieldArePreferKeta(field, remark.totalremark(), totSize.getKeta());
                } else if (usePrintMethod(SougouShoken, PreferPointThanKeta)) {
                    printSvfRenbanUseFieldArePreferPointThanKeta(field, remark.totalremark(), 10.1);
                } else {
                    printSvfRenban(field, remark.totalremark(), totSize);
                }
            }
            if (param()._z010.in(Z010.naraken)) {
                // 奈良Time
                KNJPropertiesShokenSize size;
                if (param()._is133m) {
                    size = KNJPropertiesShokenSize.getShokenSize(null, 18, 6);
                } else {
                    size = KNJPropertiesShokenSize.getShokenSize(null, 25, 10);
                }
                printSvfRenban("rec_4_" + si, remark.naraTime(), size);
            }
            if (param()._z010.in(Z010.shimaneken) && pInfo._student.isShimanekenPrintJiritsuKatudou(param())) {
                final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(null, 12, 8);
                printSvfRenban("JIRITSU_REMARK_" + si, remark.shimaneJiritsuKatsudou(), size);
            }
        }

        private void printTrainRef123(final PersonalInfo pInfo, final HtrainRemark remark, final HtrainRemarkDetail remarkDetail, final int i) {
            final String si =  String.valueOf(i);
            if (param()._z010.in(Z010.seijyo)) {
                // 成城用出力
                // ---------------------------------------
                // |                         |           |
                // |     所見1               |           |
                // |                         |           |
                // --------------------------|   所見3   |
                // |                         |           |
                // |     所見2               |           |
                // |                         |           |
                // ---------------------------------------
                if (null != remarkDetail) {
                    final int sourceKeta1 = 18 * 2;
                    final int sourceGyo1 = 7;
                    final int sourceKeta2 = 24 * 2;
                    final int sourceGyo2 = 7;
                    final int gyo = 7;
                    final int keta1 = (sourceKeta1 * sourceGyo1 + sourceKeta2 * sourceGyo2) / gyo;
                    final int keta2 = keta1;
                    final int keta3 = 18 * 2;
                    final int spaceKeta = 2;

                    final List<String> leftList = new ArrayList<String>();
                    leftList.addAll(Util.getTokenList(remarkDetail._trainRef1, Math.max(keta1, keta2)));
                    leftList.addAll(Util.getTokenList(remarkDetail._trainRef2, Math.max(keta1, keta2)));
                    final List<String> rightList = Util.getTokenList(remarkDetail._trainRef3, keta3);
                    final List<String> total = new ArrayList();
                    final int maxGyo = Math.max(leftList.size(), rightList.size());
                    for (int j = 0; j < maxGyo; j++) {
                        total.add(null);
                    }
                    for (int j = 0; j < maxGyo; j++) {
                        String left = "";
                        String right = "";
                        if (j < leftList.size()) {
                            left = Util.str(leftList.get(j));
                        }
                        if (j < rightList.size()) {
                            right = Util.str(rightList.get(j));
                        }
                        total.set(j, Util.rightPadding(left, Math.max(keta1, keta2) + spaceKeta) + right);
                    }
                    log.info(" total = " + Util.mkString(total, "\n").toString());
                    printSvfRenban("rec_3_" + si, total);
                }

            } else if (pInfo._isSogoShoken3Bunkatsu) {
                if (null != remarkDetail) {
                    final int keta1 = pInfo._train_ref_1_2_3_field_sizeInt[0] * 2;
                    final int keta2 = pInfo._train_ref_1_2_3_field_sizeInt[1] * 2;
                    final int keta3 = pInfo._train_ref_1_2_3_field_sizeInt[2] * 2;
                    if (hasField(USE_FIELD_AREA)) {
                        if (param()._isOutputDebug) {
                            param().logOnce(" trainref123 keta = " + ArrayUtils.toString(new int[] {keta1, keta2, keta3}));
                        }

                        printSvfRenbanUseFieldArePreferKeta("field8_1_" + si, remarkDetail._trainRef1, keta1);
                        printSvfRenbanUseFieldArePreferKeta("field8_2_" + si, remarkDetail._trainRef2, keta2);
                        printSvfRenbanUseFieldArePreferKeta("field8_3_" + si, remarkDetail._trainRef3, keta3);
                    } else {
                        printSvfRenban("field8_1_" + si, remarkDetail._trainRef1, keta1, pInfo._train_ref_1_2_3_gyo_sizeInt);
                        printSvfRenban("field8_2_" + si, remarkDetail._trainRef2, keta2, pInfo._train_ref_1_2_3_gyo_sizeInt);
                        printSvfRenban("field8_3_" + si, remarkDetail._trainRef3, keta3, pInfo._train_ref_1_2_3_gyo_sizeInt);
                    }
                }
            } else if (param()._z010.in(Z010.tokiwa)) {
                if (isNewForm(param(), pInfo)) {
                    if (null != remarkDetail) {
                        final int keta1;
                        final int keta2;
                        final int keta3;
                        if ("2".equals(pInfo._train_ref_1_2_3_field_size)) {
                            keta1 = 42 + 1;
                            keta2 = 42 + 1;
                            keta3 = 14 + 1;
                        } else {
                            keta1 = 28;
                            keta2 = 42;
                            keta3 = 14;
                        }
                        printSvfRenban("field8_1_" + si, remarkDetail._trainRef1, keta1, pInfo._train_ref_1_2_3_gyo_sizeInt);
                        printSvfRenban("field8_2_" + si, remarkDetail._trainRef2, keta2, pInfo._train_ref_1_2_3_gyo_sizeInt);
                        printSvfRenban("field8_3_" + si, remarkDetail._trainRef3, keta3, pInfo._train_ref_1_2_3_gyo_sizeInt);
                    }
                } else {
                    printSvfRenban("rec_3_1" + si, remark.totalremark(), 90, 4);
                }
            }
        }

        private void printTrainRef123456To123(final PersonalInfo pInfo, final HtrainRemark remark, final HtrainRemarkTrainref remarkTrainref, final int i) {
            final String si =  String.valueOf(i);
            if (null != remarkTrainref) {
                if (pInfo.useSeitoSidoYorokuSenmonGakkaForm(param())) {
                    for (final Tuple<String, List<String>> kindShokens : Arrays.asList(
                              Tuple.of("1", Arrays.asList(remarkTrainref._trainRef1, remarkTrainref._trainRef2))
                            , Tuple.of("2", Arrays.asList(remarkTrainref._trainRef3, remarkTrainref._trainRef5, remarkTrainref._trainRef6))
                            , Tuple.of("3", Arrays.asList(remarkTrainref._trainRef4)))) {
                        final String kind = kindShokens._first;
                        final List<String> shokens = kindShokens._second;

                        int gyoMax = 8;
                        int mojisu;
                        String fieldname;
                        if ("3".equals(kind)) {
                            mojisu = 21;
                        } else {
                            mojisu = 40;
                        }
                        fieldname = "field8_" + kind + "_" + si;
                        List<String> shoken = Util.getTokenList(param(), Util.mkString(shokens, "\n").toString(), mojisu * 2);
                        if (shoken.size() > gyoMax) {
                            if ("3".equals(kind)) {
                                mojisu = 41;
                                gyoMax = 17;
                            } else {
                                mojisu = 80;
                                gyoMax = 16;
                            }
                            shoken = Util.getTokenList(param(), Util.mkString(shokens, "\n").toString(), mojisu * 2);
                            fieldname = "field8_" + kind + "_" + si + "_3P";
                        }
                        printSvfRenban(fieldname, shoken);
                    }

                } else {
                    for (final Tuple<String, List<String>> kindShoken : Arrays.asList(
                              Tuple.of("1", Arrays.asList(remarkTrainref._trainRef1, remarkTrainref._trainRef2))
                            , Tuple.of("2", Arrays.asList(remarkTrainref._trainRef3, remarkTrainref._trainRef4))
                            , Tuple.of("3", Arrays.asList(remarkTrainref._trainRef5, remarkTrainref._trainRef6)))) {
                        final String kind = kindShoken._first;
                        final List<String> shokens = kindShoken._second;

                        int gyoMax = 9;
                        int keta;
                        String fieldname;
                        keta = 20 * 2 + 1;
                        fieldname = "field8_" + kind + "_" + si;
                        List<String> shoken = Util.getTokenList(param(), Util.mkString(shokens, "\n").toString(), keta);
                        if (shoken.size() > gyoMax) {
                            keta = 27 * 2 + 1;
                            gyoMax = 16;
                            shoken = Util.getTokenList(param(), Util.mkString(shokens, "\n").toString(), keta);
                            fieldname = "field8_" + kind + "_" + si + "_6P";
                            if (shoken.size() > gyoMax) {
                                keta = 52 * 2 + 1;
                                gyoMax = 18;
                                shoken = Util.getTokenList(param(), Util.mkString(shokens, "\n").toString(), keta);
                                fieldname = "field8_" + kind + "_" + si + "_3P";
                            }
                        }
                        printSvfRenban(fieldname, shoken);
                    }
                }
            }
        }

        private void printTrainRef123456(final PersonalInfo pInfo, final HtrainRemark remark, final HtrainRemarkTrainref remarkTrainref, final int i) {
            final String si =  String.valueOf(i);
            if (null != remarkTrainref) {
                final int keta = 53;
                final int gyo = 4;

                printSvfRenban("field8_1_" + si, Util.getTokenList(param(), remarkTrainref._trainRef1, keta, gyo));
                printSvfRenban("field8_2_" + si, Util.getTokenList(param(), remarkTrainref._trainRef2, keta, gyo));
                printSvfRenban("field8_3_" + si, Util.getTokenList(param(), remarkTrainref._trainRef3, keta, gyo));
                printSvfRenban("field8_4_" + si, Util.getTokenList(param(), remarkTrainref._trainRef4, keta, gyo));
                printSvfRenban("field8_5_" + si, Util.getTokenList(param(), remarkTrainref._trainRef5, keta, gyo));
                printSvfRenban("field8_6_" + si, Util.getTokenList(param(), remarkTrainref._trainRef6, keta, gyo));
            }
        }

        private void printSpecialActRemark(final PersonalInfo pInfo, final int i, final HtrainRemark remark) {
            final String si = String.valueOf(i);
            final String specialactremark = remark.specialactremark();
            if (param()._is133m) {
                if (param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("SPTIME" + String.valueOf(i), "出席時数（" + StringUtils.leftPad(defstr(remark.sagaSpTime()), 3) + "）");
                }

                printDetailM("SPECIALACTREMARK_" + si, Util.get_token(param(), specialactremark, KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_SpecialactremarkSize), 11, 6)));
                return;
            }
            // 特別活動
            String fieldSpact;
            int spActMoji;
            int spActGyo;
            if ((param()._z010.in(Z010.tottori, Z010.kyoai)) && ("1".equals(param().property(Property.seitoSidoYorokuFieldSize)) || param()._seitoSidoYorokuSpecialactremarkFieldSize)) {
                fieldSpact = "SPECIALACTREMARK_1" + si;
                spActMoji = 22;
                spActGyo = 10;
            } else if (param()._z010.in(Z010.tokiwa)) {
                if (isNewForm(param(), pInfo)) {
                    fieldSpact = "SPECIALACTREMARK_" + si + "_2";
                } else {
                    fieldSpact = "SPECIALACTREMARK_1" + si;
                }
                spActMoji = 12;
                spActGyo = 8;
            } else {
                fieldSpact = "SPECIALACTREMARK_" + si;
                spActMoji = 11;
                spActGyo = 6;
            }
            int addKeta = 0;
            if (param()._z010.in(Z010.kyoto)) { // && !param()._isSeitoSidoYorokuKinsokuForm) {
                addKeta = 1;
            }
            KNJPropertiesShokenSize spSize = KNJPropertiesShokenSize.getShokenSize(defstr(_seitoSidoYoroku_dat_TokubetsuKatudouForPrint, param().property(Property.seitoSidoYoroku_dat_SpecialactremarkSize)), spActMoji, spActGyo).createAddKeta(addKeta);
            if (param()._z010.in(Z010.shimaneken)) {
                //spSize = KNJPropertiesShokenSize.getShokenSize(null, spActMoji, spActGyo);
                if (true) { // Util.getTokenList(param(), specialactremark, spSize.getKeta()).size() > spSize._gyo) {
                    fieldSpact = "SPECIALACTREMARK_" + si + "_6P";
                    spSize = KNJPropertiesShokenSize.getShokenSize(null, 18, 11);
                }
                if (Util.getTokenList(param(), specialactremark, spSize.getKeta()).size() > spSize._gyo) {
                    fieldSpact = "SPECIALACTREMARK_" + si + "_3P";
                    spSize = KNJPropertiesShokenSize.getShokenSize(null, 31, 20);
                }
            }
            if (usePrintMethod(TokubetsuKatsudou, PreferKeta)) {
                printSvfRenbanUseFieldArePreferKeta(fieldSpact, specialactremark, spSize.getKeta());
            } else if (usePrintMethod(TokubetsuKatsudou, PreferPointThanKeta)) {
                printSvfRenbanUseFieldArePreferPointThanKeta(fieldSpact, specialactremark, 10.1);
            } else {
                printSvfRenban(fieldSpact, specialactremark, spSize.getKeta() + (param()._z010.in(Z010.tokiwa) ? 1 : 0), spSize._gyo);
            }
        }

        private static String keta(String intString, final int keta) {
            if (NumberUtils.isDigits(intString)) {
                intString = String.valueOf(Integer.parseInt(intString));
            } else {
                intString = "";
            }
            return StringUtils.repeat(" ", keta - intString.length()) + intString;
        }

        // 出欠備考
        public void printAttendrecRemark(final int i, final HtrainRemark remark, final AttendRec attendrec) {
            // 出欠備考
            if (param()._is133m) {
                final String si =  String.valueOf(i);
                if (param()._z010.in(Z010.tokyoto)) {
                } else if (param()._z010.in(Z010.miyagiken)) {
                    final KNJPropertiesShokenSize attSize = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize), 20, 2);
                    VrsOutnToken("syuketu_8_" + si, attSize.getKeta(), attSize._gyo, remark.attendrecRemark());
                } else {
                    svfVrsOut("syuketu_8_" + si, remark.attendrecRemark());
                }
            } else {
                if (param()._hasShukketsuForm) {
                    // 別フォーム出力
                    return;
                }
                StringBuffer text = new StringBuffer();
                if (param()._z010.in(Z010.musashinohigashi)) {
                    String late = null;
                    String early = null;
                    String suspend = null;
                    String mourning = null;
                    // 出欠の記録
                    if (null != attendrec) {
                        late = attendrec._late;
                        early = attendrec._early;
                        mourning = attendrec._mourning;
                        suspend = attendrec._suspend;
                    }

                    text.append("遅刻 " + defstr(late) + "回　");
                    text.append("早退 " + defstr(early) +"回　");
                    text.append("忌引 " + defstr(mourning) + "日　");
                    text.append("出停 " + defstr(suspend) + "日");
                }
                if (null != remark && null != remark.attendrecRemark()) {
                    if (text.length() != 0) {
                        text.append("\n");
                    }
                    text.append(remark.attendrecRemark());
                }
                if (param()._isOutputDebug) {
                    if (0 != text.length()) {
                        log.info(" attendremark (" + remark.year() + ") = " + text);
                    }
                }
                // 出欠備考
                final String si = String.valueOf(i);
                if (hasField(USE_FIELD_AREA)) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(null, 20, 2);
                    printSvfRenbanUseFieldArePreferKeta("syuketu_8_" + si, text.toString(), size.getKeta());

                } else if (param()._z010.in(Z010.tokiwa)) {
                    svfVrsOut("syuketu_8_" + i + "_2", text.toString());
                } else {
                    int addKeta = 0;
                    if (param()._z010.in(Z010.kyoto)) { // && !param()._isSeitoSidoYorokuKinsokuForm) {
                        addKeta = 1;
                    }
                    KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize), 20, 2).createAddKeta(addKeta);
                    if (usePrintMethod(ShukektsuBikou, PreferKeta) || usePrintMethod(ShukektsuBikou, PreferPointThanKeta)) {
                        printSvfRenbanUseFieldArePreferKeta("syuketu_8_" + si, text.toString(), size.getKeta());
                    } else {
                        String field = "syuketu_8_" + si;
                        if (param()._z010.in(Z010.shimaneken)) {
                            // #SHIMANEKEN_ATTENDREMARK_SECOND_FIELD
                            if (Util.getTokenList(param(), text.toString(), size.getKeta()).size() > size._gyo) {
                                field = "syuketu_8_" + si + "_6P";
                                size = KNJPropertiesShokenSize.getShokenSize(null, 30, 3);
                                if (Util.getTokenList(param(), text.toString(), size.getKeta()).size() > size._gyo) {
                                    field = "syuketu_8_" + si + "_3P";
                                    size = KNJPropertiesShokenSize.getShokenSize(null, 59, 5);
                                }
                            }
                        }
                        printSvfRenban(field, text.toString(), size);
                    }
                }
            }
        }

        private void printSvfRenbanUseFieldArePreferKeta(final String fieldname, final String data, final int defKeta) {
            if (StringUtils.isBlank(data)) {
                return;
            }
            if (param().isOutputDebugField(fieldname)) {
                log.info(" printSvfRenbanUseFieldAreaPreferKeta(" + fieldname + ", " + Util.take(20, data) + "..., " + defKeta + ")");
            }

            final int repeatCount = _form.getFieldRepeatCount(fieldname, 0);
            final boolean isRepeat = repeatCount > 0;

            if (null == _areaInfo) {
                _areaInfo = new SvfFieldAreaInfo();
                _areaInfo._param._setKinsoku = param()._useEditKinsoku;
                _areaInfo._param._isOutputDebugKinsoku = param()._isOutputDebugKinsoku;
            }
            _areaInfo._param._isOutputDebug = param().isOutputDebugField(fieldname);

            final ModifyParam modifyParam = new ModifyParam();
            modifyParam._repeatCount = repeatCount;
            modifyParam._usePreferPoint = false;
            modifyParam._preferKeta = defKeta;
            final Map modifyFieldInfoMap = _areaInfo.getModifyFieldInfoMap(_form._formInfo._fieldInfoMap, _form._formInfo._formname, fieldname, modifyParam, data);

            if (param().isOutputDebugField(fieldname)) {
                log.info(" !!! modify field " + fieldname + " = " + Util.listString(modifyFieldInfoMap.entrySet(), 0));
            }

            if (isRepeat) {
                int ketai = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_KETA", new Integer(0)).intValue();
                final int lines = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_LINE", new Integer(0)).intValue();

                final List attrMapRepeatList = getMappedList(modifyFieldInfoMap, "REPEAT");

                for (int ri = 0; ri < attrMapRepeatList.size(); ri++) {
                    final Map attrMap = (Map) attrMapRepeatList.get(ri);
                    svfVrAttributen(fieldname, ri + 1, KnjDbUtils.getString(attrMap, "FIELD_ATTR"));
                }
                if (defKeta < ketai && Util.getTokenList(param(), data, defKeta).size() <= lines) {
                    // 余裕があるのでデフォルトの桁で表示
                    ketai = defKeta;
                }
                printSvfRenban(fieldname, Util.getTokenList(param(), data, ketai, lines));
            } else {
                // 未実装
            }
        }


        private void printSvfRenbanUseFieldArePreferPointThanKeta(final String fieldname, final String data, final double pointThanKeta) {
            if (StringUtils.isBlank(data)) {
                return;
            }
            if (param().isOutputDebugField(fieldname)) {
                log.info(" printSvfRenbanUseFieldAreaPreferPoint(" + fieldname + ", " + Util.take(20, data) + "..., " + pointThanKeta + ")");
            }

            final int repeatCount = _form.getFieldRepeatCount(fieldname, 0);
            final boolean isRepeat = repeatCount > 0;

            if (null == _areaInfo) {
                _areaInfo = new SvfFieldAreaInfo();
                _areaInfo._param._setKinsoku = param()._useEditKinsoku;
                _areaInfo._param._isOutputDebugKinsoku = param()._isOutputDebugKinsoku;
            }
            _areaInfo._param._isOutputDebug = param().isOutputDebugField(fieldname);

            final ModifyParam modifyParam = new ModifyParam();
            modifyParam._repeatCount = repeatCount;
            modifyParam._usePreferPointThanKeta = true;
            modifyParam._preferPointThanKeta = pointThanKeta;
            final Map modifyFieldInfoMap = _areaInfo.getModifyFieldInfoMap(_form._formInfo._fieldInfoMap, _form._formInfo._formname, fieldname, modifyParam, data);

            if (param().isOutputDebugField(fieldname)) {
                log.info(" !!! modify field " + fieldname + " = " + Util.listString(modifyFieldInfoMap.entrySet(), 0));
            }

            if (isRepeat) {
                int ketai = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_KETA", new Integer(0)).intValue();
                final int lines = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_LINE", new Integer(0)).intValue();

                final List attrMapRepeatList = getMappedList(modifyFieldInfoMap, "REPEAT");

                for (int ri = 0; ri < attrMapRepeatList.size(); ri++) {
                    final Map attrMap = (Map) attrMapRepeatList.get(ri);
                    svfVrAttributen(fieldname, ri + 1, KnjDbUtils.getString(attrMap, "FIELD_ATTR"));
                }
                printSvfRenban(fieldname, Util.getTokenList(param(), data, ketai, lines));

            } else {
                // 未実装
            }
        }

        private static boolean kinsokuOver(final String strData, final int keta, final int lines) {
            final List hyphendTokenList = KNJ_EditKinsoku.getTokenList(strData, keta);
            final boolean rtn = hyphendTokenList.size() > lines;
            return rtn;
        }

        private void printAttendRec(final Student student, final PersonalInfo pInfo, final String year, int j) {
            if (param()._hasShukketsuForm) {
                // 別フォーム出力
                return;
            }
            if (param()._is133m) {
                final AttendRec attendrec = student._attendRecMap.get(year);
                final Integer count = null == attendrec || null == attendrec._executedateCount ? new Integer(0) : attendrec._executedateCount;
                svfVrsOut("syuketu_7_" + j, count.toString());
                if (param()._z010.in(Z010.miyagiken)) {
                    final BigDecimal ct93 = null == attendrec || null == attendrec._creditTime93 ? new BigDecimal(0) : attendrec._creditTime93;
                    final BigDecimal ct94 = null == attendrec || null == attendrec._creditTime94 ? new BigDecimal(0) : attendrec._creditTime94;
                    final BigDecimal cttotal = ct93.add(ct94);
                    svfVrsOut("SP1_" + j, getDispNumM(ct93));
                    svfVrsOut("SP2_" + j, getDispNumM(ct94));
                    svfVrsOut("SP3_" + j, getDispNumM(cttotal));
                }

            } else {
                final AttendRec attendrec = student._attendRecMap.get(year);
                if (null == attendrec) {
                    return;
                }
                svfVrsOut("syuketu_1_" + j, attendrec._attend_1);// 授業日数
                if (param()._z010.in(Z010.KINDAI)) {
                    svfVrsOut("syuketu_2_" + j, attendrec._suspend);// 出停 + 忌引日数
                    svfVrsOut("syuketu_3_" + j, attendrec._mourning);// 出停日数
                } else {
                    // 忌引日数
                    final String field;
                    if (param()._z010.in(Z010.tokiwa) && isNewForm(param(), pInfo) || param()._z010.in(Z010.mieken) || param()._z010.in(Z010.naraken)) {
                        field = "syuketu_2_" + j;
                    } else {
                        field = "SUSPEND"    + j;
                    }
                    svfVrsOut(field, addNumber(attendrec._mourning, attendrec._suspend));
                }
                if (param()._z010.in(Z010.naraken)) {
                    if (NumberUtils.isDigits(attendrec._abroad) && Integer.parseInt(attendrec._abroad) > 0) {
                        svfVrsOut("syuketu_4_" + j, attendrec._abroad);// 留学日数
                    }
                } else {
                    svfVrsOut("syuketu_4_" + j, attendrec._abroad);// 留学日数
                }
                svfVrsOut("syuketu_5_" + j, attendrec._requirepresent);// 要出席日数
                svfVrsOut("syuketu_6_" + j, attendrec._attend_6);// 欠席日数
                svfVrsOut("syuketu_7_" + j, attendrec._present);// 出席日数
                svfVrsOut("LATE" + j, attendrec._late);// 遅刻回数
                svfVrsOut("EARLY" + j, attendrec._early);// 早退回数
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printPersonalInfo4(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {

            if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                svfVrsOut("NAME1", pInfo._isPrintRealName ? pInfo._studentRealName : pInfo._studentName);
            } else if (param()._z010.in(Z010.naraken)) {
                printName1(null, PersonalInfo.HistVal.of(pInfo.getPrintName1(), pInfo._studentNameHistFirst), null, _nameFieldInfo);
            } else {
                printName(pInfo, _nameFieldInfo);
            }

            if (param()._is133m) {
                if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("ATTENDNO_1", student._schregno);
                } else if (param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("SCHREGNO", student._schregno);
                }

                if (param()._z010.in(Z010.sagaken)) {
                    for (int g = 1; g <= 6; g++) {
                        svfVrsOut("SPTIME" + String.valueOf(g), "出席時数（   ）");
                    }
                }
            } else {

                printSchoolName(student);

                if (param()._z010.in(Z010.meiji)) {
                    svfVrsOut("TOTAL_STUDY_NAME", "Catholic Spiritの評価");
                }
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGakuseki4(final PersonalInfo pInfo, final int i, final Gakuseki gakuseki) {
            final boolean enableYear = Util.toInt(gakuseki._year, 0) != 0;

            if (param()._is133m) {
                if (param()._z010.in(Z010.sagaken)) {
                    svfVrsOut("HGRADE1_" + i, gakuseki._nendo);
                } else if (param()._schoolDiv.isGakunenSei(gakuseki._year, null, pInfo._student)) {
                    if (enableYear) {
                        svfVrsOut("GRADE4_" + i, gakuseki._gdat._gakunenSimple);
                    }
                }
            } else {
                if (gakuseki._isKoumokuGakunen) {
                    if (enableYear) {
                        svfVrsOut("GRADE4_" + i, gakuseki._gdat._gakunenSimple);
                    }
                }
            }

            // ホームルーム
            svfVrsOutForData(Arrays.asList("HR_CLASS1_" + i, "HR_CLASS2_" + i), gakuseki._hdat._hrname);
            svfVrsOut("ATTENDNO_" + i, param()._is133m ? pInfo._schregno : gakuseki._attendno);

            final String title = getGakunenNendoTitle(pInfo, gakuseki);
            if (param()._z010.in(Z010.meiji)) {
                svfVrsOutn("SP_GRADE", i, title);    // 明治専用 Catholic Spiritの評価
            }
            // 総合的な学習の時間の記録
            svfVrsOut("GRADE5_" + i, title);
            // 特別活動の記録
            svfVrsOut("GRADE1_" + i, title);

            if (!isJyotoRecordForm(pInfo)) {
                // 総合所見及び指導上参考となる諸事項
                if (param()._is133m && param()._z010.in(Z010.miyagiken)) {
                    // 数値を横書き
                    svfVrsOut("GRADE2_" + i, gakuseki._arNendoM[0] + " " + gakuseki._arNendoM[2]);
                    svfVrsOut("GRADE2_" + i + "_2", gakuseki._arNendoM[1]);
                } else {
                    svfVrsOut("GRADE2_" + i, title);
                }
            }
        }

        private String getGakunenNendoTitle(final PersonalInfo pInfo, final Gakuseki gakuseki) {
            if (null == gakuseki) {
                return null;
            }
            String title;
            if (param()._is133m) {
                if (param()._z010.in(Z010.sagaken)) {
                    title = gakuseki._nendo;
                } else if (param()._schoolDiv.isGakunenSei(gakuseki._year, null, pInfo._student)) {
                    title = gakuseki._gakunenDaitukiM;
                } else {
                    title = gakuseki._nendo;
                }
            } else {
                if (gakuseki._isKoumokuGakunen) {
                    title = gakuseki._gdat._gradeName2;
                } else {
                    title = gakuseki._nendo;
                }
            }
            return title;
        }

        /**
         * 所見等データを印刷します。
         * @param svf
         * @param strField SVFフィールド名
         * @param strData 編集元の文字列
         * @param i
         * @param size 所見サイズ
         */
        private void printDetailM(final String strField, final String[] arrstr) {
            for (int j = 0; j < arrstr.length; j++) {
                if (null == arrstr[j]) {
                    continue;
                }
                svfVrsOutn(strField, j + 1, arrstr[j]);
            }
        }

        private static String getDispNumM(final BigDecimal bd) {
            if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
                // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
                return bd.setScale(0).toString();
            }
            return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        /**
         * 出欠の記録（東京都用）
         */
        private static class KNJA130_4T extends KNJA130_0 implements Page {

            private KNJSvfFieldInfo _name;

            KNJA130_4T(final Vrw32alp svf, final Param param) {
                super(svf, param);
            }

            public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
                setDetail4T(db2, student, pInfo);
            }

            private void setDetail4T(final DB2UDB db2, final Student student, final PersonalInfo pInfo) {
                setForm();

                final Map<Integer, PrintGakuseki> pagePrintGakusekiMap = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);
                for (final Integer page : pagePrintGakusekiMap.keySet()) {
                    final PrintGakuseki printGakuseki = pagePrintGakusekiMap.get(page);

                    printPage4t(student, pInfo, printGakuseki);
                    nonedata = true;
                }
            }

            public void setForm() {
                final String form;
                if (param()._is133m) {
                    _gradeLineMax = 6;
                    form = "KNJA133M_3TOKYO.frm";
                    _name = new KNJSvfFieldInfo(433, 1268, charSize11, 320, 287, 353, 24, 48);
                } else {
                    if (param()._z010.in(Z010.chiyodaKudan)) {
                        _gradeLineMax = 4;
                        form = "KNJA130_3KUDAN.frm";
                    } else {
                        _gradeLineMax = 6;
                        form = "KNJA130_3TOKYO.frm";
                    }
                }
                svfVrSetForm(form, 1);
            }

            @Override
            public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
                printPage4t(student, pInfo, pg);
            }

            private void printPage4t(final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki) {
                final Param param = param();
                setForm();
                svfVrsOut("GRADENAME1", pInfo._title);
                svfVrsOut("GRADENAME2", pInfo._title);
                svfVrsOutGroupForData(new String[][] {{"SCHOOLNAME1"}, {"SCHOOLNAME2", "SCHOOLNAME3"}}, student.certifSchool(param())._schoolName1);

                final String courseMajor;
                if (param._is133m) {
                    courseMajor = defstr(pInfo._courseName) + "　" + defstr(pInfo._majorName) + ("1".equals(param().property(Property.seitoSidoYorokuPrintCoursecodename)) && !param()._seitoSidoYorokuNotPrintCoursecodes.contains(pInfo._coursecode) ? "　" + defstr(pInfo._coursecodeName) : "");
                } else {
                    courseMajor = defstr(pInfo._courseName) + "　" + defstr(pInfo._majorName);
                }
                svfVrsOutGroupForData(new String[][] {{"COURSENAME1"}, {"COURSENAME2", "COURSENAME3"}}, courseMajor);

                if (param._is133m) {

                    svfVrsOut("ATTENDNO_1", student._schregno);

                    printName(pInfo, "NAME1", "NAME2", "NAME3", _name);

                    for (final Gakuseki gakuseki : printGakuseki._gakusekiList) {

                        final int i = printGakuseki._yearGakusekiPositionMap.get(gakuseki._year);

                        printGakuseki4MT(gakuseki, i);

                        // 所見データを印刷
                        printHtrainRemark4MT(student, gakuseki, i);

                        // 出欠データを印刷
                        printAttendRec4MT(student, gakuseki, i);
                    }
                } else {
                    printName(pInfo, _form._formInfo.getFieldInfo("NAME1", "NAME2", "NAME3", (int) KNJSvfFieldModify.charPointToPixel("", 12.0, 0), 24));

                    for (final String year :  printGakuseki._yearList) {

                        final Gakuseki gakuseki = printGakuseki._yearGakusekiMap.get(year);
                        if (param()._schoolDiv.isTanniSei(gakuseki._year, pInfo, student) && gakuseki.isNyugakumae() || !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                            continue;
                        }

                        final Integer i = printGakuseki._yearGakusekiPositionMap.get(year);
                        final int j = getGradeColumnNum(pInfo, i.intValue(), gakuseki, pInfo.isDropBefore(gakuseki) ? GakusekiColumn.NORMAL : GakusekiColumn.SEQ, param(), _gradeLineMax); // 不要?

                        printGakuseki4t(student, j, gakuseki, pInfo);

                        if (pInfo._isFuhakkou) {
                            printFooterRemark(pInfo, YOSHIKI._2_URA);
                        } else {
                            printShokenData(j, gakuseki._year, student);
                        }
                    }
                }
                svfVrEndPage();
            }

            private void printShokenData(final int j, final String year, Student student) {
                final AttendRec attendrec = student._attendRecMap.get(year);

                // 所見データを印刷
                final HtrainRemark remark = student._htrainRemarkMap.get(year);
                final String fieldname = "syuketu_8_" + String.valueOf(j);
                if (param()._z010.in(Z010.chiyodaKudan)) {

                    final int length = _form._formInfo.getFieldLength(fieldname, 20);
                    final int repeatCount = _form.getFieldRepeatCount(fieldname, 2);

                    String late = "";
                    String early ="";
                    if (null != attendrec) {
                        late = attendrec._late;
                        early = attendrec._early;
                    }
                    String remarkText = null;
                    if (null != remark) {
                        remarkText = remark.attendrecRemark();
                    }

                    final String data = "遅刻：" + defstr(late) + " 早退：" + defstr(early) + defstr(Util.prepend("\n", remarkText));

                    printDetail(fieldname, Util.getTokenList(param(), data, length, repeatCount));

                } else {
                    if (null != remark) {
                        // 出欠備考
                        printDetail(fieldname, Util.getTokenList(param(), remark.attendrecRemark(), 20, 4));
                    }
                }

                // 出欠データを印刷
                if (null != attendrec) {
                    svfVrsOut("syuketu_1_" + j, attendrec._attend_1); // 授業日数
                    if (param()._z010.in(Z010.KINDAI)) {
                        svfVrsOut("syuketu_2_" + j, attendrec._suspend); // 出停 + 忌引日数
                        svfVrsOut("syuketu_3_" + j, attendrec._mourning); // 出停日数
                    } else if (param()._z010.in(Z010.tokyoto) || param()._z010.in(Z010.chiyodaKudan)) {
                        svfVrsOut("syuketu_2_" + j, addNumber(attendrec._suspend, attendrec._mourning)); // 出停 + 忌引日数
                    } else {
                        svfVrsOut("SUSPEND"    + j, addNumber(attendrec._suspend, attendrec._mourning)); // 忌引日数
                    }
                    svfVrsOut("syuketu_4_" + j, attendrec._abroad); // 留学日数
                    svfVrsOut("syuketu_5_" + j, attendrec._requirepresent); // 要出席日数
                    svfVrsOut("syuketu_6_" + j, attendrec._attend_6); // 欠席日数
                    svfVrsOut("syuketu_7_" + j, attendrec._present); // 出席日数
                }
            }

            protected void printDetail(final String strField, final List<String> tokenList) {
                for (int j = 0; j < tokenList.size(); j++) {
                    if (null == tokenList.get(j)) {
                        continue;
                    }
                    svfVrsOutn(strField, j + 1, tokenList.get(j));
                }
            }

            /**
             * 学籍履歴を印刷します。
             * @param i
             * @param gakuseki
             */
            private void printGakuseki4t(final Student student, final int i, final Gakuseki gakuseki, final PersonalInfo pInfo) {
                // 学年
                if (gakuseki._isKoumokuGakunen) {
                    svfVrsOut("GRADE2_" + i, gakuseki._gdat._gakunenSimple);
                    svfVrsOut("GRADE2_" + i + "_2", gakuseki._gdat._gakunenSimple);
                } else {
                    final String[] nendoArray = gakuseki.nendoArray(param());
                    svfVrsOut("GRADE1_" + i, nendoArray[0]);
                    svfVrsOut("GRADE2_" + i, nendoArray[1]);
                    svfVrsOut("GRADE3_" + i, nendoArray[2]);

                    svfVrsOut("GRADE2_" + i + "_1", nendoArray[0]);
                    svfVrsOut("GRADE2_" + i + "_2", nendoArray[1]);
                    svfVrsOut("GRADE2_" + i + "_3", nendoArray[2]);
                }

                // ホームルーム
                svfVrsOutForData(Arrays.asList("HR_CLASS_" + String.valueOf(i), "HR_CLASS_" + String.valueOf(i) + "_2"), gakuseki._hdat._hrname);
                // 整理番号
                svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
            }

            private void printHtrainRemark4MT(final Student student, final Gakuseki gakuseki, int j) {
                final HtrainRemark remark = student._htrainRemarkMap.get(gakuseki._year);
                if (null != remark) {
                    if (param()._z010.in(Z010.tokyoto)) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize), 20, 2);
                        VrsOutnToken("syuketu_8_" + String.valueOf(j), size.getKeta(), size._gyo, remark.attendrecRemark());
                    } else {
                        // 出欠備考
                        svfVrsOut("syuketu_8_" + String.valueOf(j), remark.attendrecRemark());
                    }
                }
            }

            private void printAttendRec4MT(final Student student, final Gakuseki gakuseki, int j) {
                final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
                final Integer count = null == attendrec || null == attendrec._executedateCount ? new Integer(0) : attendrec._executedateCount;
                if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("syuketu_6_" + j, count.toString());
                } else {
                    svfVrsOut("syuketu_7_" + j, count.toString());
                }
            }

            /**
             * ヘッダー（学籍履歴）を印刷します。
             * @param gakuseki
             * @param i
             */
            private void printGakuseki4MT(final Gakuseki gakuseki, final int i) {
                // 学年
                if (gakuseki._isKoumokuGakunen) {
                    svfVrsOut("GRADE2_" + i, gakuseki._gdat._gakunenSimple);
                    svfVrsOut("GRADE2_" + i + "_2", gakuseki._gdat._gakunenSimple);
                } else {
                    final String[] nendoArray = gakuseki._arNendoM;
                    svfVrsOut("GRADE1_" + i, nendoArray[0]);
                    svfVrsOut("GRADE2_" + i, nendoArray[1]);
                    svfVrsOut("GRADE3_" + i, nendoArray[2]);

                    svfVrsOut("GRADE2_" + i + "_1", nendoArray[0]);
                    svfVrsOut("GRADE2_" + i + "_2", nendoArray[1]);
                    svfVrsOut("GRADE2_" + i + "_3", nendoArray[2]);
                }

                // ホームルーム
                svfVrsOutForData(Arrays.asList("HR_CLASS_" + String.valueOf(i), "HR_CLASS_" + String.valueOf(i) + "_2"), gakuseki._hdat._hrname);
            }
        }

        /**
         * 特例の授業等の記録
         */
        private static class KNJA129Delegate extends KNJA130_0 implements Page {

            KNJA129 _knja129;
            KNJA129.Param _knja129param;

            KNJA129Delegate(final Vrw32alp svf, final Param param, final HttpServletRequest request, final DB2UDB db2) {
                super(svf, param);
                _knja129 = new KNJA129();
                _knja129param = _knja129.getParam(request, db2, param._year, param._gakki, param.SCHOOL_KIND);
                _gradeLineMax = 6;
            }

            @Override
            public void printPage(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final Integer page, final PrintGakuseki pg, final List<List<String>> csvLines) {
                printPage4(db2, student, pInfo, pg, csvLines);
            }

            @Override
            public void setDetail(DB2UDB db2, Student student, PersonalInfo pInfo, List<List<String>> csvLines) {
                setDetail4(db2, student, pInfo, csvLines);
            }

            private void setDetail4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
                final Map<Integer, PrintGakuseki> pagePrintGakusekiMap = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);

                for (final Integer page : pagePrintGakusekiMap.keySet()) {
                    final PrintGakuseki printGakuseki = pagePrintGakusekiMap.get(page);
                    printPage4(db2, student, pInfo, printGakuseki, csvLines);
                }
            }

            private void printPage4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final PrintGakuseki printGakuseki, final List<List<String>> csvLines) {
                _knja129.printSvf(_knja129param, db2, super._form._svf, pInfo._schregno, getNameLines(pInfo), param().SCHOOL_KIND, new ArrayList<String>(printGakuseki._yearList), csvLines);
                if (_knja129.hasData()) {
                    nonedata = true;
                }
            }

            public void close() {
                super.close();
                _knja129param.close();
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class KNJA130_34 extends KNJA130_0 {

        KNJA130_34(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            if (param()._is133m) {
                if (param()._z010.in(Z010.kyoto)) {
                    _gradeLineMax = 4;
                } else {
                    _gradeLineMax = 6;
                }
            } else {
                _gradeLineMax = is3nenYou(pInfo) ? 3 : 4;
            }
            final TreeSet<Integer> pageSet = new TreeSet<Integer>();
            Map<Integer, PrintGakuseki> pagePrintGakusekiMap3 = null;
            if (null != param()._knja130_3) {
                pagePrintGakusekiMap3 = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_OMOTE, pInfo, param(), _gradeLineMax);
                pageSet.addAll(pagePrintGakusekiMap3.keySet());
            }
            Map<Integer, PrintGakuseki> pagePrintGakusekiMap4 = null;
            if (null != param()._knja130_4) {
                pagePrintGakusekiMap4 = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);
                pageSet.addAll(pagePrintGakusekiMap4.keySet());
            }
            final List<Integer> pageList = new ArrayList<Integer>(pageSet);

            for (int printGakusekiIdx = 0; printGakusekiIdx < pageList.size(); printGakusekiIdx++) {
                final Integer page = pageList.get(printGakusekiIdx);

                if (null != pagePrintGakusekiMap3) {
                    final PrintGakuseki pg3 = pagePrintGakusekiMap3.get(page);
                    pg3._pageIdx = printGakusekiIdx;
                    if (null != pg3) {
                        param()._knja130_3.printPage3(student, pInfo, pg3, csvLines);
                    }
                }

                if (null != pagePrintGakusekiMap4) {
                    final PrintGakuseki pg4 = pagePrintGakusekiMap4.get(page);
                    if (null != pg4) {
                        param()._knja130_4.printPage4(db2, student, pInfo, pg4, csvLines);
                    }
                }
            }
            nonedata = true;
        }
    }


    //--- 内部クラス -------------------------------------------------------
    private static class KNJA130_1234 extends KNJA130_0 {

        KNJA130_1234(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            if (param()._is133m) {
                _gradeLineMax = 6;
            } else {
                _gradeLineMax = is3nenYou(pInfo) ? 3 : 4;
            }

            final List<Tuple<Map<Integer, PrintGakuseki>, Page>> printPages = new ArrayList<Tuple<Map<Integer, PrintGakuseki>, Page>>();
            if (null != param()._knja130_1) {
                param()._knja130_1._gradeLineMax = _gradeLineMax;
                final Map<Integer, PrintGakuseki> pm = param()._knja130_1.getPrintGakusekiPageMap(pInfo, _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja130_1));
                if (param()._isOutputDebug) {
                    log.info(" key1 = " + pm.keySet());
                }

                final PrintGakuseki lastPrintGakuseki = pm.get(max(pm.keySet()));
                if (null != lastPrintGakuseki) {
                    lastPrintGakuseki._isLastPrintGakuseki = true;
                }
            }
            if (null != param()._knja130_2) {
                param()._knja130_2.setForm(student, pInfo);
                final Map<Integer, PrintGakuseki> pm = KNJA130_2.getPageGakusekiListMap2(param(), pInfo, _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja130_2));
                if (param()._isOutputDebug) {
                    log.info(" key2 = " + pm.keySet());
                }
            }
            if (null != param()._knja130_4t) {
                final Map<Integer, PrintGakuseki> pm = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja130_4t));
                if (param()._isOutputDebug) {
                    log.info(" key shukketsu = " + pm.keySet());
                }
            }

            if (null != param()._knja130_3) {
                final Map<Integer, PrintGakuseki> pm = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_OMOTE, pInfo, param(), _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja130_3));
                if (param()._isOutputDebug) {
                    log.info(" key3 = " + pm.keySet());
                }
            }
            if (null != param()._knja130_4) {
                final Map<Integer, PrintGakuseki> pm = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja130_4));
                if (param()._isOutputDebug) {
                    log.info(" key4 = " + pm.keySet());
                }
            }
            if (null != param()._knja129) {
                final Map<Integer, PrintGakuseki> pm = KNJA130_0.getPagePrintGakusekiMap(db2, YOSHIKI._2_URA, pInfo, param(), _gradeLineMax);
                printPages.add(Tuple.of(pm, (Page) param()._knja129));
                if (param()._isOutputDebug) {
                    log.info(" key online = " + pm.keySet());
                }
            }

            final TreeSet<Integer> pageSet = new TreeSet<Integer>();
            for (final Tuple<Map<Integer, PrintGakuseki>, Page> pageMapAndPage : printPages) {
                final Map<Integer, PrintGakuseki> pageMap = pageMapAndPage._first;
                pageSet.addAll(pageMap.keySet());
            }
            final List<Integer> pageList = new ArrayList<Integer>(pageSet);

            for (int printGakusekiIdx = 0; printGakusekiIdx < pageList.size(); printGakusekiIdx++) {
                final Integer page = pageList.get(printGakusekiIdx);

                for (final Tuple<Map<Integer, PrintGakuseki>, Page> pageMapAndPage : printPages) {
                    final Map<Integer, PrintGakuseki> pageMap = pageMapAndPage._first;
                    final Page printPage = pageMapAndPage._second;

                    final PrintGakuseki pg = pageMap.get(page);
                    if (null != pg) {
                        pg._pageIdx = printGakusekiIdx;
                        printPage.printPage(db2, student, pInfo, page, pg, csvLines);
                    }
                }
            }
            nonedata = true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 補足資料。
     */
    private static class KNJA130_Hosoku extends KNJA130_0 {

        private KNJSvfFieldInfo _nameHosoku;

        KNJA130_Hosoku(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<List<String>> csvLines) {
            final List<String> allPrintLines = getHosokuData(db2, student, pInfo, pInfo._isFirst, param());
            if (allPrintLines.isEmpty()) {
                return;
            }
            // 補足出力
            _nameHosoku = new KNJSvfFieldInfo(421, 1180, charSize11, 325, 295, 355, 24, 48);

            boolean is3 = is3nenYou(pInfo);
            if (is3 && param()._z010.in(Z010.kyoto)) {
                if (Gakuseki.containsDroppedAbroad(pInfo._gakusekiList)) {
                    is3 = false;
                 }
            }
            final String hosokuForm = is3 ? "KNJA130C_15.frm" : "KNJA130C_5.frm";
            final List<List<String>> printLinesPageList = getPageList(allPrintLines, 60);
            for (int pi = 0; pi < printLinesPageList.size(); pi++) {
                final List<String> printLines = printLinesPageList.get(pi);
                svfVrSetForm(hosokuForm, 1);

                int ii = 1;
                for (final Gakuseki gakuseki : pInfo._gakusekiList) {
                    printGakusekiHosoku(student, pInfo, String.valueOf(ii), gakuseki);
                    ii += 1;
                }

                for (int li = 0; li < printLines.size(); li++) {
                    svfVrsOutn("REMARK", li + 1, printLines.get(li));
                }
                svfVrEndPage();
            }
            nonedata = true;
        }

        private static String hosokuFormatDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "　　　　　　　　";
            }
            final Calendar cal = getCalendarOfDate(date);
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String gengou = KNJ_EditDate.dateGengou(db2, year, month, dayOfMonth);

            return gengou + "年" + (month < 10 ? " " : "") + String.valueOf(month) + "月" + (dayOfMonth < 10 ? " " : "") + String.valueOf(dayOfMonth) + "日";
        }

        private static List<String> getAddressHosokuLines(final DB2UDB db2, final List<Address> hosokuAddressList) {
            final List<String> printLines = new ArrayList<String>();
            printLines.add("　　　 有効開始日　　　　有効終了日　　住所");
            for (int i = 0; i < hosokuAddressList.size(); i++) {
                final Address address = hosokuAddressList.get(i);
                final StringBuffer line = new StringBuffer();
                line.append("　　");
                line.append(hosokuFormatDate(db2, address.getIssuedate()));
                line.append("　");
                line.append(defstr(hosokuFormatDate(db2, address.getExpiredate()), "　　　　　　　　"));
                line.append("　");
                final int keta = getMS932ByteLength(line.toString());
                line.append(defstr(address.getAddr1()));
                printLines.add(line.toString());
                if (address.isPrintAddr2() && !StringUtils.isBlank(address.getAddr2())) {
                    printLines.add(StringUtils.repeat("　", keta / 2) + StringUtils.repeat(" ", keta % 2) + defstr(address.getAddr2()));
                }
            }
            return printLines;
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGakusekiHosoku(final Student student, final PersonalInfo pInfo, final String i, final Gakuseki gakuseki) {
            svfVrsOut("GRADENAME1", pInfo._title);
            svfVrsOut("GRADENAME2", pInfo._title);

            printName(pInfo, "NAME1", "NAME2", "NAME3", _nameHosoku);

            printSchoolName(student);

            if (param()._z010.in(Z010.tokiwa) && !isNewForm(param(), pInfo)) {
                svfVrsOut("GRADE1_" + i, gakuseki._gdat._gradeName2);
            } else if (gakuseki._isKoumokuGakunen) {
                svfVrsOut("GRADE1_" + i, gakuseki._gdat._gakunenSimple);
            } else {
                svfVrsOut("GRADE3_" + i, gakuseki._nendo);
            }

            // ホームルーム
            svfVrsOutForData(Arrays.asList("HR_CLASS1_" + i, "HR_CLASS2_" + i), gakuseki._hdat._hrname);
            svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        private static void addLineNotNull(final List<String> lines, final String header, final String data) {
            if (null != data) {
                lines.add(header + data);
            }
        }

        private List<String> getHosokuData(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final boolean isFirst, final Param param) {
            final List<String> printLines = new ArrayList<String>();

            if (pInfo._schregBaseHistList.size() > 0) {
                final int realNameOrNotDiv;
                if (pInfo._isPrintNameAndRealName) {
                    realNameOrNotDiv = 2;
                } else if (pInfo._isPrintRealName) {
                    realNameOrNotDiv = 1;
                } else {
                    realNameOrNotDiv = 0;
                }

                printLines.add("（生徒氏名）");
                printLines.add("　　　　 開始日　　　　　　終了日　　　生徒氏名（かな）");
                addHosokuNameLines(db2, pInfo, pInfo._schregBaseHistList, printLines, realNameOrNotDiv);
            }

            if (!student._secondKokuseki.isEmpty()) {
                final List<String> lines = new ArrayList<String>();
                addLineNotNull(lines, "　　　第二国籍　", KnjDbUtils.getString(student._secondKokuseki, "NATIONALITY_NAME"));
                addLineNotNull(lines, "　　　　　氏名　", KnjDbUtils.getString(student._secondKokuseki, "SECOND_NAME"));
                addLineNotNull(lines, "　　　氏名かな　", KnjDbUtils.getString(student._secondKokuseki, "SECOND_NAME_KANA"));
                addLineNotNull(lines, "　　　英字氏名　", KnjDbUtils.getString(student._secondKokuseki, "SECOND_NAME_ENG"));
                addLineNotNull(lines, "　　　戸籍氏名　", KnjDbUtils.getString(student._secondKokuseki, "SECOND_REAL_NAME"));
                addLineNotNull(lines, "　戸籍氏名かな　", KnjDbUtils.getString(student._secondKokuseki, "SECOND_REAL_NAME_KANA"));
                if (!lines.isEmpty()) {
                    printLines.add("（生徒第二国籍）");
                    printLines.addAll(lines);
                }
            }

            if (pInfo._hosokuAddressIndexList.size() > 0) {
//                log.debug(" hosokuAddressIndexList = " + hosokuAddressIndexList);
                final List<Address> hosokuAddressList = getIndexedList(pInfo._addressList, pInfo._hosokuAddressIndexList);
                if (null != hosokuAddressList) {
                    printLines.add("（生徒住所）");
                    printLines.addAll(getAddressHosokuLines(db2, hosokuAddressList));
                    printLines.add("");
                }
            }

            if (pInfo._guardianHistOrGuarantorHistList.size() > 0) {
                final int realNameOrNotDiv;
                realNameOrNotDiv = 2;

                printLines.add("（" + pInfo._addressGrdHeader + "氏名）");
                printLines.add("　　　　 開始日　　　　　　終了日　　　" + pInfo._addressGrdHeader + "氏名（かな）");
                addHosokuNameLines(db2, pInfo, pInfo._guardianHistOrGuarantorHistList, printLines, realNameOrNotDiv);
            }

            if (pInfo._hosokuGuardAddressIndexList.size() > 0) {
//                log.debug(" guardHosokuAddressIndexList = " + guardHosokuAddressIndexList);
                final List<Address> guardHosokuAddressList = getIndexedList(pInfo._guardianAddressList, pInfo._hosokuGuardAddressIndexList);
                if (null != guardHosokuAddressList) {
                    printLines.add("（" + pInfo._addressGrdHeader + "住所）");
                    printLines.addAll(getAddressHosokuLines(db2, guardHosokuAddressList));
                    printLines.add("");
                }
            }
            if (pInfo._hosokuTransferRecIndexList.size() > 0) {
//                log.debug(" hosokuTransferRecIndexList = " + hosokuTransferRecIndexList);
                final List<TransferRec> hosokuTransferrecList = getIndexedList(TransferRec.getGradeOnlyTransferList(param(), isFirst, student._transferRecList, pInfo._gakusekiList), pInfo._hosokuTransferRecIndexList);
                if (null != hosokuTransferrecList) {
                    final List<TransferRec> ryugaku = new ArrayList<TransferRec>();
                    final List<TransferRec> kyugaku = new ArrayList<TransferRec>();
                    for (final TransferRec tr : hosokuTransferrecList) {
                        if (TransferRec.A004_NAMECD2_RYUGAKU.equals(tr._transfercd)) {
                            ryugaku.add(tr);
                        } else if (TransferRec.A004_NAMECD2_KYUGAKU.equals(tr._transfercd)) {
                            kyugaku.add(tr);
                        }
                    }
                    if (ryugaku.size() > 0) {
                        printLines.add("（留学）");
                        printLines.add("　　　　 開始日　　　　　　終了日　　　" + (param()._z010.in(Z010.kyoto) ? "国名/学校名/学年" : "事由/異動先名称/異動先住所"));
                        for (int i = 0; i < ryugaku.size(); i++) {
                            final TransferRec tr = (TransferRec) ryugaku.get(i);
                            final String reason = defstr(tr._reason);
                            final String place = defstr(tr._place);
                            final String addr = defstr(tr._address);

                            final StringBuffer line = new StringBuffer();
                            line.append("　　");
                            line.append(hosokuFormatDate(db2, tr._sDate));
                            line.append("　");
                            line.append(hosokuFormatDate(db2, tr._eDate));
                            line.append("　");
                            if (!StringUtils.isBlank(Util.mkString(Arrays.asList(reason, place, addr), "").toString())) {
                                line.append(Util.mkString(Arrays.asList(reason, place, addr), "/").toString());
                            }
                            printLines.add(line.toString());
                        }
                        printLines.add("");
                    }
                    if (kyugaku.size() > 0) {
                        printLines.add("（休学）");
                        printLines.add("　　　　 開始日　　　　　　終了日　　　事由");
                        for (int i = 0; i < kyugaku.size(); i++) {
                            final TransferRec tr = kyugaku.get(i);
                            final StringBuffer line = new StringBuffer();
                            line.append("　　");
                            line.append(hosokuFormatDate(db2, tr._sDate));
                            line.append("　");
                            line.append(hosokuFormatDate(db2, tr._eDate));
                            line.append("　");
                            line.append(defstr(tr._reason));
                            printLines.add(line.toString());
                        }
                        printLines.add("");
                    }
                }
            }
            if (!pInfo._hosokuYearPrincipalMap.isEmpty()) {
                printLines.add("（校長履歴）");

                for (final Map.Entry<String, List<Map<String, String>>> e : pInfo._hosokuYearPrincipalMap.entrySet()) {
                    final String year = e.getKey();
                    final List<Map<String, String>> principalHistList = e.getValue();
                    if (principalHistList.size() >= 3) {
                        printLines.add("　　" + nendoWareki(db2, year));
                        printLines.add("　　　　 開始日　　　　　　終了日　　　校長氏名");

                        for (int idx = 1; idx < principalHistList.size() - 1; idx++) { // 最初と最後以外
                            final Map<String, String> hist = principalHistList.get(idx);
                            final StringBuffer line = new StringBuffer();
                            line.append("　　");
                            line.append(hosokuFormatDate(db2, KnjDbUtils.getString(hist, "FROM_DATE")));
                            line.append("　");
                            line.append(hosokuFormatDate(db2, KnjDbUtils.getString(hist, "TO_DATE")));
                            line.append("　");
                            final StaffMst staffMst = StaffMst.get(param._staffInfo._staffMstMap, KnjDbUtils.getString(hist, "STAFFCD"));
                            line.append(defstr(staffMst._name));
                            printLines.add(line.toString());
                        }
                        printLines.add("");
                    }
                }
            }

            if (!pInfo._hosokuYearStaffMap.isEmpty()) {
                printLines.add("（担任履歴）");

                for (final Map.Entry<String, List<Staff>> e : pInfo._hosokuYearStaffMap.entrySet()) {
                    final String year = e.getKey();
                    final List<Staff> minStaffcdStaffList = e.getValue();

                    printLines.add("　　" + nendoWareki(db2, year));
                    printLines.add("　　　　 開始日　　　　　　終了日　　　担任氏名");

                    for (int idx = 0; idx < minStaffcdStaffList.size(); idx++) { // 最初と最後以外
                        final Staff staff = minStaffcdStaffList.get(idx);
                        final StringBuffer line = new StringBuffer();
                        line.append("　　");
                        line.append(hosokuFormatDate(db2, staff._dateFrom));
                        line.append("　");
                        line.append(hosokuFormatDate(db2, staff._dateTo));
                        line.append("　");
                        line.append(defstr(staff._staffMst._name));
                        printLines.add(line.toString());
                    }
                    printLines.add("");
                }
            }

            return printLines;

        }

        private static void addHosokuNameLines(final DB2UDB db2, final PersonalInfo pInfo, final List<Map<String, String>> histList, final List<String> printLines, final int realNameOrNotDiv) {
            for (int i = 0; i < histList.size(); i++) {
                final Map<String, String> nameHist = histList.get(i);
                final StringBuffer line = new StringBuffer();
                line.append("　　");
                line.append(hosokuFormatDate(db2, KnjDbUtils.getString(nameHist, "ISSUEDATE")));
                line.append("　");
                line.append(hosokuFormatDate(db2, KnjDbUtils.getString(nameHist, "EXPIREDATE")));
                line.append("　");
                final int keta = getMS932ByteLength(line.toString());
                final String realName = defstr(KnjDbUtils.getString(nameHist, "REAL_NAME"));
                final String realKana = defstr(kakko(KnjDbUtils.getString(nameHist, "REAL_KANA")));
                final String name = defstr(KnjDbUtils.getString(nameHist, "NAME"));
                final String kana = defstr(kakko(KnjDbUtils.getString(nameHist, "KANA")));
                if (realNameOrNotDiv == 2 && !StringUtils.isBlank(realName + realKana)) {
                    line.append(defstr(realName + "　" + realKana));
                    printLines.add(line.toString());
                    line.delete(0, line.length());
                    line.append(StringUtils.repeat("　", keta / 2)).append(StringUtils.repeat(" ", keta % 2));
                    line.append(defstr(name + "　" + kana));
                    printLines.add(line.toString());

                } else if ((realNameOrNotDiv == 1 || realNameOrNotDiv == 2) && !StringUtils.isBlank(realName + realKana)) {
                    line.append(defstr(realName + "　" + realKana));
                    printLines.add(line.toString());
                } else {
                    line.append(defstr(name + "　" + kana));
                    printLines.add(line.toString());
                }
            }
            printLines.add("");
        }

        /**
         * 補足に表示するインデクスのリストを得る
         * @param allList
         * @param printIndexList
         * @return 補足に表示するインデクスのリスト
         */
        private static List<Integer> getHosokuIndex(final Param param, final String debug, final List<?> allList, final List<?> printIndexList) {
            final List<Integer> rtn = new LinkedList();
            if (allList.size() - printIndexList.size() > 0) {
                final List<Integer> indexList = getIndexList(param, debug, allList);
//            for (final Iterator it = indexList.iterator(); it.hasNext();) {
//                final Integer index = it.next();
//                if (!printIndexList.contains(index)) { // allListの要素のうち、printIndexListのインデクスに含まれない要素のリストを得る
//                    rtn.add(index);
//                }
//            }
                rtn.addAll(indexList); // すべて表示
            }
            if (param._isOutputDebugData) {
                log.info(" hosoku " + debug + " idx = " + rtn);
            }
            return rtn;
        }
    }

    private static class Param extends KNJA130CCommon.Param {

        final String _output;
        final List<String> _schregnoList;

        /** 生徒指導要録 */
        final String _seito;
        /** 修得単位の記録 */
        final String _tani;
        /** 学習の記録 */
        final String _gakushu;
        /** 活動の記録 */
        final String _katsudo;
        /** 特例の授業等の記録 */
        final String _online;
        /** 生徒・保護者氏名出力(生徒指導要録に関係する) */
        final String _simei;
        /** 陰影出力(生徒指導要録に関係する) */
        final String _inei;
        final boolean _isPrintInei;
        /** 現住所の郵便番号を出力 */
        final boolean _printZipcd;
        /** 学校所在地の郵便番号を出力 */
        final boolean _printSchoolZipcd;
        /** 半期認定フォーム */
        final boolean _isHankiNinteiForm;

        /** プロパティ hyoteiYomikaeRadio
         *  0 : しない
         *  notPrint1 : 評定1を表示しない
         *  print100 : 100段階評価を表示
         */
        String _hyotei;

        /** 「活動の記録」にて「総合的な学習の時間の記録」欄は年度毎か? */
        final boolean _isNendogoto;

        /** ３年用フォームならtrue */
        final boolean _is3nenYou;

        KNJA130_1 _knja130_1;
        KNJA130_2 _knja130_2;
        KNJA130_3 _knja130_3;
        KNJA130_4 _knja130_4;
        KNJA130_4.KNJA130_4T _knja130_4t;
        KNJA130_4.KNJA129Delegate _knja129;
        KNJA130_34 _knja130_34;
        KNJA130_1234 _knja130_1234;
        KNJA130_Hosoku _knja130_Hosoku;

        final String _train_ref_1_2_3_gyo_size;

        /** 留学単位数デフォルト表示 */
        final String _seitoSidoYorokuTaniPrintAbroad;
        final String _creditsDefaultAbroad;
        /** 総合的な学習の時間単位数デフォルト表示 */
        final String _seitoSidoYorokuTaniPrintSogaku;
        final String _creditsDefaultSogaku;
        /** 合計単位数デフォルト表示 */
        final String _seitoSidoYorokuTaniPrintTotal;
        final String _creditsDefaultTotal;

        String _useProvFlg;

        boolean _setSogakuKoteiTanni;
        final Map<Integer, BigDecimal> _sogakuKoteiTanniMap = new TreeMap<Integer, BigDecimal>();

        /** 学校区分名称 */
        private Map _z001name1Map;

        /** 履修のみ科目出力 */
        final boolean _isPrintRisyuNomi;
        /** 未履修科目出力 */
        final boolean _isPrintMirisyu;
        /** 履修登録のみ科目出力 */
        final boolean _isPrintRisyuTourokuNomi;

        private OptionCredit _optionCreditOutputYoshiki1Ura;
        private OptionCredit _optionCreditOutputYoshiki2Omote;

        protected StaffInfo _staffInfo;
        protected String _careerPlanItemName = null;

        final boolean _isPrintYoshiki1UraBunkatuRishu;
        final boolean _isPrintYoshiki2OmoteTotalCreditByPage;
        final boolean _isPrintYoshiki2OmoteDropAbroadBiko; // 様式2表の下の余白に原級留置備考を表示もしくは留学欄に備考を表示
        final boolean _isPrintAnotherStudyrec2; // 2枚目（様式1裏）に前籍校の成績を含める
        private static final int _printAnotherStudyrec3_0 = 0;
        private static final int _printAnotherStudyrec3_1 = 1;
        private static final int _printAnotherStudyrec3_2 = 2;
        final int _printAnotherStudyrec3; // 3枚目（様式2表）に前籍校の成績を含めるか 0:含めない 1:含める 2:単位制は含める。学年制は含めない。

        List<KNJA130_0> _printForm;

        boolean _useNewForm;

        final boolean _hasShukketsuForm;
        /** 出欠の記録（東京都のみ） */
        String _shukketsu;
        /** プロパティーファイルのseitoSidoYorokuNotPrintAnotherAttendrecが1なら指導要録のSCHOOLCD='1'のSCHREG_ATTENDREC_DATを読み込みしない */
        // String _seitoSidoYorokuNotPrintAnotherAttendrec;
        /** プロパティーファイルのuseStudyrecRemarkQualifiedDatが1ならを読み込み */
        String _useStudyrecRemarkQualifiedDat;

        // 総合的な学習の時間の記録は１つの欄に年度ごとHTRAIN_REMARK_DATのデータを連結して出力する
        private boolean _seitoSidoYorokuTotalStudyCombineHtrainremarkDat;
        // 様式2で休学の年度は表示しない KNJA133Mのみ
        String _notPrintKyugakuNendoInYoshiki2;
        boolean _useStudyrecReplaceDat = false;
        final boolean _isPrintRirekiDate;
        final boolean _useGengoInApr01;
        private Map _attendParamMap;

        final boolean _isPrintHosoku;

        final boolean _isNotUseCompCreditYoshiki1Ura;

        final SchoolDiv _schoolDiv;
        final String _a023Name3GraduatableGrade;
        final List<Map<String, String>> _a045CourseCertifKindChangeList;
        private List<String> _a055SenmonGakkaMajorKeyList = Collections.emptyList(); // 島根県 専門学科の学科コード
        final Map<String, String> _schoolMstYearGvalCalcMap; // GVAL_CALCは0:平均値、1:単位による重み付け、2:最大値
        final List<String> _e065Name1JiritsuKatsudouSubclasscdList;
        final String _slashImagePath;

        Param(final HttpServletRequest request, final DB2UDB db2, final DB2UDB db2_2, final String prgid, final String year, final String semester, final List<String> schregnoList, final Map<String, String> otherParamMap) {
            super(request, db2, prgid, year, semester, otherParamMap);
            _schregnoList = schregnoList;
            if ("1".equals(request.getParameter("inei_print"))) {
                _inei = "3";
            } else if ("1".equals(request.getParameter("inei_print2"))) {
                _inei = "4";
            } else {
                _inei = "".equals(request.getParameter("INEI")) ? null : request.getParameter("INEI"); // 陰影出力
            }

            _is3nenYou = ("3".equals(property(Property.seitoSidoYorokuFormType)) || _z010.in(Z010.tokiwa)) && !_z010.in(Z010.mieken);
            if (_isOutputDebug) {
                log.info("３年用フォームを使用するか?⇒" + _is3nenYou);
            }

            final String propSeitoSidoYorokuTaniPrintAbroad = property(Property.seitoSidoYorokuTaniPrintAbroad);
            if ("0".equals(propSeitoSidoYorokuTaniPrintAbroad) || _z010.in(Z010.tokiwa)) {
                _seitoSidoYorokuTaniPrintAbroad = null;
            } else {
                _seitoSidoYorokuTaniPrintAbroad = _z010.in(Z010.kyoto) /* 通信制含む */ ? null : _is133m ? "1" : StringUtils.isBlank(propSeitoSidoYorokuTaniPrintAbroad) ? "1" : propSeitoSidoYorokuTaniPrintAbroad; // 設定なしは表示する
            }
            _creditsDefaultAbroad = "1".equals(_seitoSidoYorokuTaniPrintAbroad) ? "0" : null;
            _seitoSidoYorokuTaniPrintSogaku = _z010.in(Z010.bunkyo) ? "1" : property(Property.seitoSidoYorokuTaniPrintSogaku);
            _creditsDefaultSogaku = "1".equals(_seitoSidoYorokuTaniPrintSogaku) ? "0" : null;
            _seitoSidoYorokuTaniPrintTotal = property(Property.seitoSidoYorokuTaniPrintTotal);
            _creditsDefaultTotal = "1".equals(_seitoSidoYorokuTaniPrintTotal) ? "0" : null;

            if (_is130) {
                _isNendogoto = "KNJA130A".equals(prgid) || _z010.in(Z010.KINDAI) && "KNJI050".equals(prgid);
            } else {
                _isNendogoto = _z010.in(Z010.mieken) ? false : "KNJA130D".equals(request.getParameter("PRGID")) || "1".equals(property(Property.seitoSidoYorokuSougouHyoukaNentani)) || _z010.in(Z010.chiyodaKudan);
            }
            if (_isOutputDebug) {
                log.info("「活動の記録」にて「総合的な学習の時間の記録」欄を年度毎にするか?⇒" + _isNendogoto);
            }

            _useProvFlg = property(Property.useProvFlg);

            _train_ref_1_2_3_gyo_size = property(Property.train_ref_1_2_3_gyo_size);

            for (int g = 1; g <= 12; g++) {
                final String sogakuKoteiTanni = property("sogakuKoteiTanni_" + String.valueOf(g));
                if (NumberUtils.isNumber(sogakuKoteiTanni)) {
                    _setSogakuKoteiTanni = true;
                    _sogakuKoteiTanniMap.put(g, new BigDecimal(sogakuKoteiTanni));
                }
            }
            if (_setSogakuKoteiTanni) {
                log.info(" _sogakuKoteiTanni = " + _sogakuKoteiTanniMap);
            }

            _isPrintYoshiki2OmoteDropAbroadBiko = _z010.in(Z010.kyoto);
            if (!StringUtils.isBlank(property(Property.seitoSidoYorokuNotPrintAnotherStudyrec)) && property(Property.seitoSidoYorokuNotPrintAnotherStudyrec).indexOf("yoshiki") >= 0) {
                final String[] splits = StringUtils.split(property(Property.seitoSidoYorokuNotPrintAnotherStudyrec), ",");
                String _1ura = "1";
                String _2omote = "1";
                for (int i = 0; i < splits.length; i++) {
                    final String split = StringUtils.trim(splits[i]);
                    if (split.startsWith("yoshiki1ura:")) {
                        _1ura = StringUtils.trim(StringUtils.replace(split, "yoshiki1ura:", ""));
                    } else if (split.startsWith("yoshiki2omote:")) {
                        _2omote = StringUtils.trim(StringUtils.replace(split, "yoshiki2omote:", ""));
                    }
                }
                _isPrintAnotherStudyrec2 = Integer.parseInt(_1ura) == 1;
                _printAnotherStudyrec3 = Integer.parseInt(_2omote);
            } else if (_z010.in(Z010.kyoto)) {
                _isPrintAnotherStudyrec2 = !"1".equals(property(Property.seitoSidoYorokuNotPrintAnotherStudyrec)); // プロパティが設定されていなければ前籍校を表示する
                _printAnotherStudyrec3 = _printAnotherStudyrec3_2; // 様式2表に前籍校を表示するかは学校制度次第
            } else if (_z010.in(Z010.miyagiken)) {
                _isPrintAnotherStudyrec2 = true; //  様式1裏に前籍校を表示する
                _printAnotherStudyrec3 = _printAnotherStudyrec3_0; // 表示しない
            } else if (_z010.in(Z010.mieken)) {
                _isPrintAnotherStudyrec2 = true; //  様式1裏に前籍校を表示する
                _printAnotherStudyrec3 = _printAnotherStudyrec3_1; // 様式2表に前籍校を表示する
            } else {
                // 賢者
                _isPrintAnotherStudyrec2 = false; // 様式1裏に前籍校を表示しない
                _printAnotherStudyrec3 = _printAnotherStudyrec3_1; // 様式2表に前籍校を表示する
            }
            if (_isOutputDebug) {
                log.info(" isPrintAnotherStudyrec2 = " + _isPrintAnotherStudyrec2);
                log.info(" printAnotherStudyrec3 = " + _printAnotherStudyrec3);
            }
            _isPrintRisyuNomi = null == request.getParameter("RISYU") || "1".equals(request.getParameter("RISYU"));
            _isPrintMirisyu = null == request.getParameter("MIRISYU") || "1".equals(request.getParameter("MIRISYU"));
            _isPrintRisyuTourokuNomi = null == request.getParameter("RISYUTOUROKU") || "1".equals(request.getParameter("RISYUTOUROKU"));

            if ("1".equals(property(Property.seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg))) {
                _careerPlanItemName = getCareerPlanItemName(db2, _year);
            }

            _hasShukketsuForm = _z010.in(Z010.chiyodaKudan);

            if (_is133m) {

                _simei = request.getParameter("SIMEI"); // 漢字名出力

                _seito = request.getParameter("SEITO");
                _tani = request.getParameter("TANI");
                _shukketsu  =request.getParameter("SHUKKETSU");
                _gakushu = request.getParameter("GAKUSHU");
                _katsudo = request.getParameter("KATSUDO");
                _online = request.getParameter("online");

                _output = "1"; // request.getParameter("OUTPUT");    // 1=個人, 2=クラス

                _useStudyrecRemarkQualifiedDat = property(Property.useStudyrecRemarkQualifiedDat);

                _printZipcd = "1".equals(request.getParameter("SCHZIP"));
                _printSchoolZipcd = "1".equals(request.getParameter("SCHOOLZIP"));
                _useNewForm = _z010.in(Z010.kumamoto, Z010.kyoto, Z010.miyagiken, Z010.mieken);
                _useProvFlg = null;
                _notPrintKyugakuNendoInYoshiki2 = request.getParameter("notPrintKyugakuNendoInYoshiki2");
                _isPrintYoshiki2OmoteTotalCreditByPage = _z010.in(Z010.kyoto);
                _useStudyrecReplaceDat = _z010.in(Z010.miyagiken);
                _isPrintInei = null != _inei;
            } else { // if ("KNJA130C".equals(flg)) {
                _simei = request.getParameter("simei"); // 漢字名出力

                _seito = request.getParameter("seito");
                _tani = request.getParameter("tani");
                if (_hasShukketsuForm) {
                    _shukketsu  =request.getParameter("shukketsu");
                }
                _gakushu = request.getParameter("gakushu");
                _katsudo = request.getParameter("katsudo");
                _online = request.getParameter("online");

                _output = request.getParameter("OUTPUT");    // 1=個人, 2=クラス

                _hyotei = request.getParameter("HYOTEI");

                _printZipcd = "1".equals(request.getParameter("schzip"));
                _printSchoolZipcd = "1".equals(request.getParameter("schoolzip"));

                _useNewForm = _z010.in(Z010.tottori, Z010.kyoto, Z010.tokiwa, Z010.miyagiken, Z010.mieken, Z010.tosa, Z010.fukuiken);
                _isPrintYoshiki2OmoteTotalCreditByPage = true;

                _isPrintInei = null != _inei || (_z010.in(Z010.kyoto) && null != _staffGroupcd && _staffGroupcd.startsWith("999"));
                if (!_isNendogoto) {
                    _seitoSidoYorokuTotalStudyCombineHtrainremarkDat = _z010.in(Z010.kumamoto, Z010.tokiwa, Z010.miyagiken, Z010.kaijyo, Z010.bunkyo, Z010.mieken, Z010.rakunan, Z010.tosa) || "1".equals(property(Property.seitoSidoYorokuTotalStudyCombineHtrainremarkDat)) || _z010.in(Z010.tokiwagi);
                    if (_seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                        log.info("総合的な学習の時間の記録は１つの欄に年度ごとHTRAIN_REMARK_DATのデータを連結して出力する");
                    } else {
                        log.info("総合的な学習の時間の記録はHTRAIN_REMARK_HDATから出力する");
                    }
                }
            }
            _isHankiNinteiForm = "1".equals(request.getParameter("hanki"));
            if (_isHankiNinteiForm) {
                _semesterNameMap = getSemesterNameMap(db2);
            }
            if (null == _gakki) {
                log.error("パラメーターがnull: gakki");
            }
            _isPrintHosoku = _z010.in(Z010.kyoto) || "1".equals(property(Property.seitoSidoYorokuPrintHosoku));
            _isPrintRirekiDate = _z010.in(Z010.mieken);
            _isPrintYoshiki1UraBunkatuRishu = _z010.in(Z010.kyoto) || "1".equals(property(Property.seitoSidoYorokuYoshiki1UraBunkatsuRishu));
            _isNotUseCompCreditYoshiki1Ura = _z010.in(Z010.sundaikoufu, Z010.mieken, Z010.meikei) || ArrayUtils.contains(Util.csvToArray(property(Property.seitoSidoYorokuNotUseCompCredit)), "1ura"); // 福井県
            _useGengoInApr01 = _z010.in(Z010.miyagiken) || _z010.in(Z010.chiyodaKudan);
            if (_z010.in(Z010.aoyama)) {
                _attendParamMap = new HashMap();
                _attendParamMap.put("DB2UDB", db2);
                _attendParamMap.put("HttpServletRequest", request);
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            }

            _staffInfo = new StaffInfo(SCHOOL_KIND);
            _staffInfo.load(db2, _z010.in(Z010.mieken) ? db2_2 : db2, _year);
            _schoolDiv = new SchoolDiv(this, db2);
            setOptionCreditOutput(db2);

            setSemester(db2);
            setHdat(db2);
            setClassMst(db2);
            setSubclassMst(db2);
            setD015Namespare1(db2);

            _z001name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT T1.NAMECD2, T1.NAME1 FROM NAME_MST T1 WHERE T1.NAMECD1 = 'Z001' "), "NAMECD2", "NAME1");
            loadSchool(db2, _year);
            _a023Name3GraduatableGrade = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT VALUE(NAMESPARE2, NAME3) FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + SCHOOL_KIND + "' "));
            _a045CourseCertifKindChangeList = KnjDbUtils.query(db2, "SELECT NAME1, NAME2, NAME3, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A045' AND ABBV1 = '" + CERTIF_KINDCD + "' ORDER BY NAMECD2 ");
            if (_z010.in(Z010.shimaneken)) {
                _a055SenmonGakkaMajorKeyList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 || '-' || NAME2 AS MAJOR_KEY FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A055' ORDER BY NAMECD2 "), "MAJOR_KEY");
            }
            if (_z010.in(Z010.shimaneken)) {
                _e065Name1JiritsuKatsudouSubclasscdList = Collections.emptyList();
            } else {
                _e065Name1JiritsuKatsudouSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'E065' ORDER BY NAMECD2 "), "NAME1");
            }
            _slashImagePath = getImageFilePath("image", "slash", "jpg");

            final StringBuffer schoolMstSql = new StringBuffer();
            schoolMstSql.append(" SELECT YEAR, GVAL_CALC FROM SCHOOL_MST ");
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                schoolMstSql.append(" WHERE SCHOOL_KIND = '" + SCHOOL_KIND + "' ");
            }
            _schoolMstYearGvalCalcMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, schoolMstSql.toString()), "YEAR", "GVAL_CALC");
        }

        public List<Staff> getStudentStaffHistList(final Student student, final PersonalInfo pInfo, final String trDiv, final String year) {
            return _staffInfo.getStudentStaffHistList(_isOutputDebugStaff, _semesterMap, student._regdList, pInfo._entSemester, pInfo._entDate, pInfo._grdSemester, pInfo._grdDate, trDiv, year);
        }

        public void closeForm() {
            for (final KNJA130_0 f : Arrays.asList(_knja130_1, _knja130_2, _knja130_3, _knja130_4, _knja130_4t, _knja130_Hosoku, _knja129)) {
                if (null != f) {
                    f.close();
                }
            }
        }

        private boolean isNotPrintDaitai(final String div) {
            return ArrayUtils.contains(_seitoSidoYorokuNotUseSubclassSubstitution, "1") || ArrayUtils.contains(_seitoSidoYorokuNotUseSubclassSubstitution, div);
        }

        /**
         * 学校クラスを作成ます。
         */
        private void loadSchool(final DB2UDB db2, final String year) {
            final KNJ_SchoolinfoSql obj = new KNJ_SchoolinfoSql("10000");

            final Map paramMap = new HashMap();
            paramMap.put("schoolMstSchoolKind", _hasSCHOOL_MST_SCHOOL_KIND ? "H" : null);

            final String sql = obj.pre_sql(paramMap);

            final Map schoolRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql, new String[] { year, year }));

            _schoolAddress1 = defstr(KnjDbUtils.getString(schoolRow, "SCHOOLADDR1"));
            _schoolAddress2 = defstr(KnjDbUtils.getString(schoolRow, "SCHOOLADDR2"));
            _schoolZipcode = KnjDbUtils.getString(schoolRow, "SCHOOLZIPCD");
            _schoolMstSchoolName1 = KnjDbUtils.getString(schoolRow, "SCHOOLNAME1");
        }

        public boolean isPrintZaigakusubekiKikan(final Student student, final PersonalInfo pInfo) {
            if ("print".equals(property(Property.seitoSidoYorokuNotPrintZaisekiSubekiKikan))) {
                return true;
            }
            if ("1".equals(property(Property.seitoSidoYorokuNotPrintZaisekiSubekiKikan))) {
                return false;
            }
            if (_z010.in(Z010.kyoto)) {
                return _is133m;
            } else if (_z010.in(Z010.miyagiken)) {
                // 単位制のみ対象
//                // 転入生と編入生のみ対象
//                if (!(pInfo.isTennyu() || pInfo.isHennyu())) {
//                    return false;
//                }
                return !_is133m && _schoolDiv.isTanniSei(null, pInfo, student) && null != pInfo._entDate;
            } else if (_z010.in(Z010.fukuiken)) {
                return !_is133m && _schoolDiv.isTanniSei(null, pInfo, student);
            }
            return false;
        }

        public void setPrintForm(final Vrw32alp svf, final HttpServletRequest request, final DB2UDB db2) {
            _printForm = new ArrayList<KNJA130_0>();
            // 様式１（学籍に関する記録）
            if ((null != _seito || null != _tani || null != _shukketsu || null != _gakushu || null != _katsudo) && "1".equals(property(Property.seitoSidoYorokuPrintOrder))) {
                if (null == _knja130_1234) {
                    _knja130_1234 = new KNJA130_1234(svf, this);
                }
                _printForm.add(_knja130_1234);
            }
            if (null != _seito) {
                if (null == _knja130_1) {
                    _knja130_1 = new KNJA130_1(svf, this);
                }
                _knja130_1.init();
                if (null == _knja130_1234) {
                    _printForm.add(_knja130_1);
                }
            }
            // 様式１の裏（修得単位の記録）
            if (null != _tani) {
                if (null == _knja130_2) {
                    _knja130_2 = new KNJA130_2(svf, this);
                }
                if (null == _knja130_1234) {
                    _printForm.add(_knja130_2);
                }
            }
            if (null != _shukketsu) {
                if (null == _knja130_4t) {
                    _knja130_4t = new KNJA130_4.KNJA130_4T(svf, this);
                }
                if (null == _knja130_1234) {
                    _printForm.add(_knja130_4t);
                }
            }
            if (null == _knja130_1234) {
                if ((null != _gakushu || null != _katsudo) && "1".equals(property(Property.seitoSidoYorokuYoshiki2PrintOrder))) {
                    // 様式２
                    if (null == _knja130_34) {
                        _knja130_34 = new KNJA130_34(svf, this);
                    }
                    _printForm.add(_knja130_34);
                }
            }
            // 様式２（指導に関する記録）
            if (null != _gakushu) {
                if (null == _knja130_3) {
                    _knja130_3 = new KNJA130_3(svf, this);
                }
                if (null == _knja130_1234 && null == _knja130_34) {
                    _printForm.add(_knja130_3);
                }
            }

            // 様式２の裏（所見等）
            if (null != _katsudo) {
                if (null == _knja130_4) {
                    _knja130_4 = new KNJA130_4(svf, this);
                }
                if (null == _knja130_1234 && null == _knja130_34) {
                    _printForm.add(_knja130_4);
                }
            }

            // 特例の授業等の記録
            if (null != _online) {
                if (null == _knja129) {
                    _knja129 = new KNJA130_4.KNJA129Delegate(svf, this, request, db2);
                }
                if (null == _knja130_1234) {
                    _printForm.add(_knja129);
                }
            }
            if (_isPrintHosoku) {
                if (null != _knja130_1) {
                    if (null == _knja130_Hosoku) {
                        _knja130_Hosoku = new KNJA130_Hosoku(svf, this);
                    }
                    _printForm.add(_knja130_Hosoku);
                }
            }
            if (_isOutputDebug) {
                log.info(" printForm = " + Util.listString(_printForm, 0));
            }
        }

        private void setOptionCreditOutput(final DB2UDB db2) {
            int opt = -1;
            final String a031Name1 = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'A031' AND NAMECD2 = '00' ")), "NAME1");
            if (NumberUtils.isDigits(a031Name1)) {
                opt = Integer.parseInt(a031Name1);
            }
            final int min = 0;
            final int max = 2;
            if (opt < min || max < opt) {
                if (_z010.in(Z010.kyoto)) {
                    _optionCreditOutputYoshiki1Ura = OptionCredit.YOSHIKI1_URA_1;
                    _optionCreditOutputYoshiki2Omote = OptionCredit.YOSHIKI2_OMOTE_1;
                } else if (_z010.in(Z010.tottori)) {
                    _optionCreditOutputYoshiki1Ura = OptionCredit.YOSHIKI1_URA_2;
                    _optionCreditOutputYoshiki2Omote = OptionCredit.YOSHIKI2_OMOTE_2;
                } else {
                    _optionCreditOutputYoshiki1Ura = OptionCredit.YOSHIKI1_URA_0;
                    _optionCreditOutputYoshiki2Omote = OptionCredit.YOSHIKI2_OMOTE_0;
                }
            }
            if (_isOutputDebug) {
                log.info(" optionCreditOutput = " + _optionCreditOutputYoshiki1Ura + ", " + _optionCreditOutputYoshiki2Omote);
            }
        }

        // D015に設定された名称予備1の年度
        private List<String> getD015Namespare1YearList() {
            return getMappedList(_sessionCache, "V_NAME_MST_MAP_D015");
        }
        private void setD015Namespare1(final DB2UDB db2) {
            // 名称マスタ「D015」名称予備1が「Y」なら評定平均の下限を0とする
            _sessionCache.put("V_NAME_MST_MAP_D015", KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT YEAR FROM V_NAME_MST WHERE NAMECD1 = 'D015' AND YEAR <= '" + _year + "' AND NAMESPARE1 = 'Y' "), "YEAR"));
        }
    }

    /**
    *
    *  学校教育システム 賢者 [学籍管理]  生徒指導要録  通信制高校用
    *
    */
    public static class KNJA133M extends KNJA130C { // 外部呼び出しのためアクセス指定子はpublic

        protected Param getParam(final HttpServletRequest request, final DB2UDB db2, final DB2UDB db2_2) {
            final String prgid = "KNJA133M";
            final Param param = new Param(request, db2, db2_2, prgid, request.getParameter("YEAR"), request.getParameter("SEMESTER"), Param.getSchregnoList(db2, prgid, request), new HashMap<String, String>());
            return param;
        }
    }
}

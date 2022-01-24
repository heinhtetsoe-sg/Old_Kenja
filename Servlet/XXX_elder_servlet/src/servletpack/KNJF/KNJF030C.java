// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2009/10/06 11:39:11 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJF.detail.MedexamDetDat;
import servletpack.KNJF.detail.MedexamToothDat;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id$
 */
public class KNJF030C {

    private static final Log log = LogFactory.getLog("KNJF030C.class");

    private static final String SELECT_HR = "1";
    private List _printStudents;
    private List _printNendoBunStudents;
    private boolean _hasData;

    Param _param;
    KNJF030CAbstract _printKNJF030A;

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
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        _printStudents = getPrintStudent(db2, true);

        KNJF030CAbstract[] printClass = new KNJF030CAbstract[5];
        if (_param._printGankaKensin) {
            printClass[0] = new KNJF030C_GankaKensin(_param, db2, svf);
        }
        if (_param._printKekkaHa) {
            printClass[1] = new KNJF030C_KekkaHa(_param, db2, svf);
        }
        if (_param._printTeikiKensin) {
            printClass[2] = new KNJF030C_TeikiKensin(_param, db2, svf);
        }
        if (_param._printTeikiKensinItiran) {
            printClass[3] = new KNJF030C_TeikiKensinItiran(_param, db2, svf);
        }
        if (_param._printKekkaHa2) {
            printClass[4] = new KNJF030C_KekkaHa2(_param, db2, svf);
        }
        //印字処理
        for (int i = 0; i < printClass.length; i++) {
            _hasData = null != printClass[i] && printClass[i].printMain(_printStudents) ? true : _hasData;
        }
    }

    /**
     * 他のクラスからコールされる
     * @param request リクエスト
     * @param response レスポンス
     */
    public void makeData(
            final HttpServletRequest request,
            final DB2UDB db2,
            final boolean mekeMedexamFlg
    ) throws Exception {

        try {
            _param = createParam(db2, request);

            _printStudents = getPrintStudent(db2, mekeMedexamFlg);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            db2.commit();
        }

    }

    public boolean printKenkouSindanIppanA(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        _printKNJF030A = new KNJF030A_KenkouSindanIppan(_param, db2, svf);
        boolean hasData = false;
        final List schregnoList = new ArrayList();
        for (final Iterator it = _printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            schregnoList.add(student._schregno);
        }
        if (printKenkouSindanIppanAschregnoList(db2, svf, schregnoList)) {
            hasData = true;
        }
        //印字処理
        return hasData;
    }

    public boolean printKenkouSindanIppanAschregnoList(final DB2UDB db2, final Vrw32alp svf, final List schregnoList) throws SQLException {
        if (null == _printKNJF030A) {
            _printKNJF030A = new KNJF030A_KenkouSindanIppan(_param, db2, svf);
        }
        final List printStudentsCopy = new ArrayList(_printStudents);
        final List printStudents = new ArrayList();
        for (final Iterator it = schregnoList.iterator(); it.hasNext();) {
            final String schregno = (String) it.next();
            for (final Iterator itc = printStudentsCopy.iterator(); itc.hasNext();) {
                final Student student = (Student) itc.next();
                if (schregno.equals(student._schregno)) {
                    printStudents.add(student);
                    itc.remove();
                    break;
                }
            }
        }
        //印字処理
        return _printKNJF030A.printMain(printStudents);
    }

    private List getPrintStudent(final DB2UDB db2, final boolean mekeMedexamFlg) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement psSt = null;
        ResultSet rsSt = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            // 01001(年組) OR 20051015-02001004(学籍-年組番)
            final String classSelected = _param._classSelected[i];
            final String selected = _param._kubun.equals(SELECT_HR) ? classSelected : classSelected.substring(0,(classSelected).indexOf("-"));
            final String studentSql = getStudentSql(selected);
            try {
                psSt = db2.prepareStatement(studentSql);
                rsSt = psSt.executeQuery();
                while (rsSt.next()) {
                    final String schregno = rsSt.getString("SCHREGNO");
                    final String grade = rsSt.getString("GRADE");
                    final String hrClass = rsSt.getString("HR_CLASS");
                    final String hrName = rsSt.getString("HR_NAME");
                    final String attendno = rsSt.getString("ATTENDNO");
                    final String annual = rsSt.getString("ANNUAL");
                    final String name = rsSt.getString("NAME");
                    final String sexCd = rsSt.getString("SEX_CD");
                    final String sex = rsSt.getString("SEX");
                    final String birthDay = rsSt.getString("BIRTHDAY");
                    final String coursecd = rsSt.getString("COURSECD");
                    final String majorcd = rsSt.getString("MAJORCD");
                    final String coursecode = rsSt.getString("COURSECODE");
                    final String coursename = rsSt.getString("COURSENAME");
                    final String majorname = rsSt.getString("MAJORNAME");
                    final String coursecodename = rsSt.getString("COURSECODENAME");
                    final String schoolKind = rsSt.getString("SCHOOL_KIND");
                    final Student student = new Student(schregno,
                                                        grade,
                                                        hrClass,
                                                        hrName,
                                                        attendno,
                                                        annual,
                                                        name,
                                                        sexCd,
                                                        sex,
                                                        birthDay,
                                                        coursecd,
                                                        majorcd,
                                                        coursecode,
                                                        coursename,
                                                        majorname,
                                                        coursecodename,
                                                        schoolKind);
                    if (mekeMedexamFlg) {
                        student.setMedexamDet(db2);
                        student.setMedexamTooth(db2);
                    }
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, psSt, rsSt);
                db2.commit();
            }
        }
        return rtnList;
    }

    private String getStudentSql(final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     HR.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.ANNUAL, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SEX AS SEX_CD, ");
        stb.append("     N1.NAME2 AS SEX, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     COURSE.COURSENAME, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSEC.COURSECODENAME, ");
        stb.append("     GDAT.SCHOOL_KIND ");
        stb.append(" FROM ");
        if ("KNJF030E".equals(_param._prgid) && !"1".equals(_param._not_useFi_Hrclass)) {
            stb.append("     SCHREG_REGD_FI_DAT T1 ");
        } else {
            stb.append("     SCHREG_REGD_DAT T1 ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ");
        stb.append("          AND T1.GRADE = GDAT.GRADE ");
        if ("KNJF030E".equals(_param._prgid) && !"1".equals(_param._not_useFi_Hrclass)) {
            stb.append("     LEFT JOIN SCHREG_REGD_FI_HDAT AS HR ON T1.YEAR = HR.YEAR ");
            stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
            stb.append("          AND T1.GRADE = HR.GRADE ");
            stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
        } else {
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
            stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
            stb.append("          AND T1.GRADE = HR.GRADE ");
            stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
        }
        stb.append("     INNER JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
        stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._kubun.equals(SELECT_HR)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        } else {
            stb.append("     AND T1.SCHREGNO = '" + selected + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _annual;
        final String _name;
        final String _sexCd;
        final String _sex;
        final String _birthDay;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursename;
        final String _majorname;
        final String _coursecodename;
        final String _schoolKind;
        MedexamDetDat _medexamDetDat = null;
        MedexamToothDat _medexamToothDat = null;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String annual,
                final String name,
                final String sexCd,
                final String sex,
                final String birthDay,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String coursename,
                final String majorname,
                final String coursecodename,
                final String schoolKind
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _annual = annual;
            _name = name;
            _sexCd = sexCd;
            _sex = sex;
            _birthDay = birthDay;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _schoolKind = schoolKind;
        }

        public void setMedexamDet(final DB2UDB db2) throws SQLException {
            try {
                _medexamDetDat = new MedexamDetDat(db2, _param._year, _schregno, _param._printKenkouSindanIppan);
            } finally {
                db2.commit();
            }
        }

        public void setMedexamTooth(final DB2UDB db2) throws SQLException {
            try {
                _medexamToothDat = new MedexamToothDat(db2, _param._year, _schregno);
            } finally {
                db2.commit();
            }
        }
    }

    static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76834 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        static final String SCHOOL_NAME1 = "SCHOOL_NAME1";
        static final String SCHOOL_NAME2 = "SCHOOL_NAME2";
        static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";
        static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

        final boolean _printGankaKensin;
        final boolean _printKekkaHa;
        final boolean _printKekkaHaCard;
        final boolean _printTeikiKensin;
        final boolean _heartMedexamPrint;
        final boolean _tbPrint;
        final boolean _printTeikiKensinItiran;
        final boolean _anemiaPrint;
        final boolean _printKekkaHa2;
        final String _knjf030PrintVisionNumber;
        final String _not_useFi_Hrclass; // KNJF030E
        final String _kekkaHa2Date;
        final String _kubun;
        final String _gradeHrClass;
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _time;
        final String _staffCd;
        final String _staffName;
        final String _teikiKensinDate;
        final String _teikiKensinDatePhp;
        final KNJ_Schoolinfo _schoolinfo;
        private final KNJ_Schoolinfo.ReturnVal _schoolInfoVal;
        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatJobName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark1;
        private String _certifSchoolDatRemark2;
        private String _certifSchoolDatRemark3;
        private String _certifSchoolDatRemark4;
        private String _certifSchoolDatRemark5;
        private String _certifSchoolDatRemark6;
        final String _prgid;
        final String _schoolJudge;
        final String[] _classSelected;
        private boolean _seirekiFlg;
        final Map _otherInjiParam;
        final String _namemstZ010Name1;
        final boolean _isChukyo;
        final boolean _isKumamoto;
        final boolean _isMiyagiken;
        final boolean _isMusashinoHigashi;
        final boolean _isTokiwa;
        final String _printKenkouSindanIppan;
        final String _useKnjf030AHeartBiko;
        final String _printStamp;
        final String _printSchregNo1;
        final String _printSchregNo2;
        final String _useParasite_P;
        final String _useParasite_J;
        final String _useParasite_H;
        final String _useForm9_PJ_Ippan;
        final String _useForm7_JH_Ippan;
        final String _useForm5_H_Ippan;
        private Map _yearKouiStampNo; // 学校医印鑑
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        private final String _stampImageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String[] _selectSchoolKind;
        /** 名称マスタのコンボで名称予備2=1は表示しない */
        final String _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1;

        Map _physAvgMap = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _printGankaKensin = null != request.getParameter("GANKA_KENSIN") ? true : false;
            _printKekkaHa = null != request.getParameter("KEKKA_HA") ? true : false;
            _printKekkaHaCard = null != request.getParameter("KEKKA_HA_CARD") ? true : false;
            _printTeikiKensin = null != request.getParameter("TEIKI_KENSIN") ? true : false;
            _heartMedexamPrint = null != request.getParameter("HEART_MEDEXAM_PRINT") ? true : false;
            _tbPrint = null != request.getParameter("TB_PRINT") ? true : false;
            _printTeikiKensinItiran = null != request.getParameter("TEIKI_KENSIN_ITIRAN") ? true : false;
            _anemiaPrint = null != request.getParameter("ANEMIA_PRINT") ? true : false;
            _printKekkaHa2 = null != request.getParameter("KEKKA_HA2") ? true : false;
            _kekkaHa2Date = request.getParameter("KEKKA_HA2_DATE");
            _kubun = request.getParameter("KUBUN");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _staffCd = request.getParameter("STAFFCD");
            _prgid = request.getParameter("PRGID");
            _schoolJudge = request.getParameter("SCHOOL_JUDGE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = StringUtils.split(request.getParameter("selectSchoolKind"), ":");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _otherInjiParam = new HashMap();
            _staffName = getStaffName(db2);
            _namemstZ010Name1 = getNamemstZ010(db2);
            _isChukyo = "chukyo".equals(_namemstZ010Name1);
            _isKumamoto = "kumamoto".equals(_namemstZ010Name1);
            _isMiyagiken = "miyagiken".equals(_namemstZ010Name1);
            _isMusashinoHigashi = "musashinohigashi".equals(_namemstZ010Name1);
            _isTokiwa = "tokiwa".equals(_namemstZ010Name1);
            _printKenkouSindanIppan = request.getParameter("printKenkouSindanIppan"); // 1:Aパターン使用
            _useKnjf030AHeartBiko = request.getParameter("useKnjf030AHeartBiko");
            _knjf030PrintVisionNumber = request.getParameter("knjf030PrintVisionNumber");
            _not_useFi_Hrclass = request.getParameter("not_useFi_Hrclass");
            _useParasite_P = request.getParameter("useParasite_P");
            _useParasite_J = request.getParameter("useParasite_J"); // 中学校で寄生虫卵を使用するか
            _useParasite_H = request.getParameter("useParasite_H"); // 高校で寄生虫卵を使用するか
            _useForm5_H_Ippan = request.getParameter("useForm5_H_Ippan");
            _printStamp = request.getParameter("PRINT_STAMP");
            _printSchregNo1 = request.getParameter("PRINT_SCHREGNO1"); // 学籍番号印字
            _printSchregNo2 = request.getParameter("PRINT_SCHREGNO2"); // 学籍番号印字
            _useForm9_PJ_Ippan = request.getParameter("useForm9_PJ_Ippan"); // 健康診断票・小中学校で9年用フォームを使用するか
            _useForm7_JH_Ippan = request.getParameter("useForm7_JH_Ippan"); // 健康診断票・中学高校で7年用フォームを使用するか
            _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1 = request.getParameter("kenkouSindanIppanNotPrintNameMstComboNamespare2Is1");
            _teikiKensinDate = request.getParameter("TEIKI_KENSIN_DATE");
            _teikiKensinDatePhp = hasPropertyName(request, "TEIKI_KENSIN_DATE") ? "1" : null;
            try {
                _schoolinfo = new KNJ_Schoolinfo(_year); //取得クラスのインスタンス作成
                _schoolInfoVal = _schoolinfo.get_info(db2);
            } finally {
                db2.commit();
            }
            setSeirekiFlg(db2);

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            DecimalFormat df = new DecimalFormat("00");
            _time = df.format(hour)+"時"+df.format(minute)+"分";

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _imageDir = "image";
            _stampImageDir = "image/stamp";
            _imageExt = "bmp";
            setCertifSchoolDat(db2);
        }

        String schoolInfoVal(final String schoolKind, final String field) {
            final Map map = new HashMap();
            if ("H".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatSchoolName); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatSchoolName); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatPrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark1); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark1); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark4); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark4); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
            }
            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _schoolInfoVal.SCHOOL_NAME1); //学校名１
            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _schoolInfoVal.SCHOOL_NAME2); //学校名２
            if (null == map.get(PRINCIPAL_NAME)) map.put(PRINCIPAL_NAME, _schoolInfoVal.PRINCIPAL_NAME); //校長名
            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _schoolInfoVal.PRINCIPAL_JOBNAME);
            return (String) map.get(field);
        }

        private boolean hasPropertyName(final HttpServletRequest request, final String name) {
            for (final Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                final String parameterName = (String) e.nextElement();
                if (parameterName.equals(name)) {
                    return true;
                }
            }
            return false;
        }

        private String getStaffName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _staffCd + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getNamemstZ010(final DB2UDB db2) {
            String namemstZ010Name1 = "";
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    namemstZ010Name1 = rs.getString("NAME1");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return namemstZ010Name1;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            if (_seirekiFlg) {
                return year + "年";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年";
            }
        }

        public void setOtherInjiParam(final String key, final String val) {
            _otherInjiParam.put(key, val);
        }


        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _yearKouiStampNo = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, T1.REMARK5, T2.STAMP_NO ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK5 ");
                stb.append(" WHERE CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _yearKouiStampNo.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
                    log.fatal(" year = " + rs.getString("YEAR") + ", stampNo = " + rs.getString("STAMP_NO"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolDatSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatJobName = rs.getString("JOB_NAME");
                    _certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolDatRemark1 = rs.getString("REMARK1");
                    _certifSchoolDatRemark2 = rs.getString("REMARK2");
                    _certifSchoolDatRemark3 = rs.getString("REMARK3");
                    _certifSchoolDatRemark4 = rs.getString("REMARK4");
                    _certifSchoolDatRemark5 = rs.getString("REMARK5");
                    _certifSchoolDatRemark6 = rs.getString("REMARK6");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 写真データファイルの取得
         */
        public String getStampImageFile(final String year) {
            if (null == year) {
                return null;
            }
            final String filename = (String) _yearKouiStampNo.get(year);
            if (null == filename) {
                return null;
            }
            return getImageFile(_stampImageDir, filename + "." + _imageExt);
        }


        /**
         * 写真データファイルの取得
         */
        public String getImageFile(final String filename) {
            return getImageFile(_imageDir, filename);
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String imageDir, final String filename) {
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            File file1 = new File(stb.toString());
            log.debug(" filename = " + file1.toString() + ", exists = " + file1.exists());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }
}

// eof

// kanji=漢字
/*
 * $Id: 12556e521fe464e39f005d484f38469ec174e328 $
 *
 * 作成日: 2007/11/08 09:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 合格通知書／入学検定料受領書
 *
 * @author nakada
 * @version $Id: 12556e521fe464e39f005d484f38469ec174e328 $
 */
public class KNJWP101 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWP101.class);

    private static final String FORM_FILE = "KNJWP101.frm";

    /*
     * 校長のスタッフコード
     */
    /** 校長のスタッフコード */
    private static final String STAFF_CD = "0001";

    /*
     * 本文テンプレート
     */
    /** 本文テンプレート */
    private static final String TEXT1 = "上記の者、";
    private static final String TEXT2 = "への入学選考に合格したことをここに通知いたします。";
    private static final String TEXT3 = "の科目履修生として許可いたしましたことをここに通知いたします。";

    /*
     * 印刷指示（ＯＵＴＰＵＴ）
     */
    /** 印刷 */
    private static final String PRINT_ON = "1";
    /** 受領書 */
    private static final String PRINT_TITLE1 = "受領書";
    /** 入学検定料 */
    private static final String PRINT_TITLE2 = "入学検定料";
    /** 登録料 */
    private static final String PRINT_TITLE3 = "登録料";

    /** 合格通知書 */
    private static final String PRINT_TITLE4 = "合格通知書";

    /** 受講許可証 */
    private static final String PRINT_TITLE5 = "受講許可証";

    /** 様 */
    private static final String PRINT_STATE = "　様";

    /*
     * 送り先
     */
    /** 生徒 */
    private static final String DESTINATION_STUDENT = "1";
    /** 保護者 */
    private static final String DESTINATION_PROTECT = "2";
    /** 負担者 */
    private static final String DESTINATION_GUARANT = "3";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _page;

    public boolean svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response,
            Vrw32alp svf,
            DB2UDB pDb2
    ) throws Exception {
        _hasData = false;
        dumpParam(request);
        _param = createParam(request);

        _svf = svf;
        _form = new Form(FORM_FILE, response, _svf);
        db2 = pDb2;

        try {

            _param.load(db2);

            log.debug(">>志願者番号=" + _param._applicantNo);

            final ApplicantBaseMst applicant = createApplicant(db2, _param._applicantNo);
            printMain(applicant);

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final ApplicantBaseMst applicant) throws SQLException {
        printApplicant(applicant);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void printApplicant(ApplicantBaseMst applicant) throws SQLException {
        /* 送付先住所印刷 */
        _form.printShipAdd(applicant);

        /* 送付先氏名印刷 */
        if (!_param._checkTouroku2.equals(PRINT_ON)) {
            _form.printShipName(applicant);
        }

        if (_param._checkTouroku2.equals(PRINT_ON) && applicant.isUnder20()) {
            _form.printApplicantName(applicant._gname + PRINT_STATE);
        }

        /* 志願者氏名 */
        _form.printName(applicant);

        /* 合格通知書タイトル */
        _form.printGokakuTitle();


        /* 志願者番号 */
        _form._svf.VrsOut("APPLICANTNO", _param._applicantNo.toString());
        /* 本文 */
        _form.printText(applicant);
        /* 作成日 */
        _form._svf.VrsOut("DATE1", getJDate(_param._date));
        _form._svf.VrsOut("DATE2", getJDate(_param._date));
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME1", _param._staffSchoolMst._schoolName1);
        _form._svf.VrsOut("SCHOOLNAME2", _param._staffSchoolMst._schoolName1);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME1", _param._jobMst._jobName + "　");
        _form._svf.VrsOut("JOBNAME2", _param._jobMst._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME1", _param._staffMst._name);
        _form._svf.VrsOut("STAFFNAME2", _param._staffMst._name);
        /* 受領書タイトル */
        _form.printTitle();
        /* 本文・受領内容 */
        _form.printTextTitle();
    }

    private static String getJDate(String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);

            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _applicantNo;
        private final String _date;                 // 請求日付／作成日
        private final String _checkSenkou1;         // 1:選考科受領書印刷
        private final String _checkTouroku2;        // 1:登録科受領書印刷
        private final String _checkGoukakuK1;       // 1:合格通知書(個人)印刷
        private final String _select;       // 送り先
        private final String _schregno;

        private Map _prefMap;                       // 都道府県
        private School _staffSchoolMst;            // 学校名
        private Staff _staffMst;                   // 校長名
        private JobMst _jobMst;                     // 学校代表者職名

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String date,
                final String checkSenkou1,
                final String checkTouroku2,
                final String checkGoukakuK1,
                final String select,
                final String schregno
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _date = date;
            _checkSenkou1 = checkSenkou1;
            _checkTouroku2 = checkTouroku2;
            _checkGoukakuK1 = checkGoukakuK1;
            _select = select;
            _schregno = schregno;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();

            _staffSchoolMst = createSchool(db2, _param._year);

            _staffMst = createStaff(db2);

            _jobMst = createJobMst(db2, STAFF_CD);

            return;
        }

        private Map getPrefMst() throws SQLException {
            final String sql = sqlPrefMst();
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlPrefMst() {
            return " select"
            + "    PREF_CD as code,"
            + "    PREF_NAME as name"
            + " from"
            + "    PREF_MST"
            + " order by PREF_CD";
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
        final String applicantNo = request.getParameter("APPLICANTNO");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        final String checkSenkou1 = request.getParameter("CHECK_SENKOU1") != null ?
                                     request.getParameter("CHECK_SENKOU1") : "";
        final String checkTouroku2 = request.getParameter("CHECK_TOUROKU2") != null?
                                      request.getParameter("CHECK_TOUROKU2") : "";
        final String checkGoukakuK1 = request.getParameter("CHECK_GOUKAKU_K1") != null ?
                                       request.getParameter("CHECK_GOUKAKU_K1") : "";
        final String select = request.getParameter("SEND");  // 保護者固定とする。
        final String schregno = request.getParameter("SCHREGNO");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                date,
                checkSenkou1,
                checkTouroku2,
                checkGoukakuK1,
                select,
                schregno
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response,
            final Vrw32alp svf) throws IOException {

            _svf = svf;
            _svf.VrSetForm(file, 1);
        }

        public void printName(ApplicantBaseMst applicant) {
            if (applicant._schregBaseMst._name.length() == 0) {
                _form._svf.VrsOut("NAME1", applicant._name);
                _form._svf.VrsOut("NAME2", applicant._name);
                _form._svf.VrsOut("NAME3", applicant._name);
            } else {
                _form._svf.VrsOut("NAME1", applicant._schregBaseMst._name);
                _form._svf.VrsOut("NAME2", applicant._schregBaseMst._name);
                _form._svf.VrsOut("NAME3", applicant._schregBaseMst._name);
            }
        }

        public void printGokakuTitle() {
            if (_param._checkGoukakuK1.equals(PRINT_ON) ||
                    _param._checkSenkou1.equals(PRINT_ON)) {
                _form._svf.VrsOut("TITLE2", PRINT_TITLE4);
            } else {
                _form._svf.VrsOut("TITLE2", PRINT_TITLE5);
            }
        }

        public void printTextTitle() {
            _form._svf.VrsOut("COURSE", getTestTitle());
        }
        private String getTestTitle() {
            String str = null;
            if (_param._checkSenkou1.equals(PRINT_ON)) {
                str = PRINT_TITLE2;
            } else if (_param._checkTouroku2.equals(PRINT_ON)) {
                str = PRINT_TITLE3;
            }
            return str;
        }

        public void printTitle() {
            _form._svf.VrsOut("TITLE", getTitle());
        }

        private String getTitle() {
            String str = null;
            if (_param._checkSenkou1.equals(PRINT_ON)) {
                str = PRINT_TITLE2 + PRINT_TITLE1;
            } else if (_param._checkTouroku2.equals(PRINT_ON)) {
                str = PRINT_TITLE3 + PRINT_TITLE1;
            }
            return str;
        }

        public void printText(ApplicantBaseMst applicant) {
            if (_param._checkGoukakuK1.equals(PRINT_ON) ||
                    _param._checkSenkou1.equals(PRINT_ON)) {

                String str = TEXT1 + _param._staffSchoolMst._schoolName1 + "　" + applicant._major._majorName
                + applicant._courseCodeMst._courseCodeName + TEXT2;
                _form._svf.VrsOut("TEXT1_1", str);
            } else {
                String str = TEXT1 + _param._staffSchoolMst._schoolName1 + "　" + TEXT3;
                _form._svf.VrsOut("TEXT1_1", str);
            }
        }

        /** 送付先印刷 */
        public void printShipAdd(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
                printShipAddApplicant(applicant);
            } else {
                if (_param._select.equals(DESTINATION_STUDENT)) {
                    prtDestinationStudent(applicant);
                } else if (_param._select.equals(DESTINATION_PROTECT)) {
                    prtDestinationProtect(applicant);
                } else {
                    prtDestinationGuarant(applicant);
                }
            }
        }

        public void printShipAddApplicant(ApplicantBaseMst applicant) {
            if (_param._select.equals(DESTINATION_STUDENT)) {
                prtDestinationApplicant(applicant);
            } else {
                prtDestinationApplicantProtect(applicant);
            }
        }

        private void prtDestinationApplicant(ApplicantBaseMst applicant) {
            printApplicantZip(applicant._zipcd);
            printAddr1(applicant._prefCd, applicant._addr1, applicant._addr2);
            printAddr2(applicant._addr3);
        }

        private void prtDestinationApplicantProtect(ApplicantBaseMst applicant) {
            if (applicant._gaddr1 != null) {
                printApplicantZip(applicant._gzipCd);
                printAddr1(applicant._gprefCd, applicant._gaddr1, applicant._gaddr2);
                printAddr2(applicant._gaddr3);
            } else {
                prtDestinationApplicant(applicant);
            }
        }

        private void prtDestinationStudent(ApplicantBaseMst applicant) {
            if (applicant._SchregAddressDat._addr1.length() != 0) {
                printApplicantZip(applicant._SchregAddressDat._zipcd);
                printAddr1(applicant._SchregAddressDat._prefCd, applicant._SchregAddressDat._addr1, applicant._SchregAddressDat._addr2);
                printAddr2(applicant._SchregAddressDat._addr3);
            } else {
                printShipAddApplicant(applicant);
            }
        }

        private void prtDestinationProtect(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardAddr1.length() != 0) {
                printApplicantZip(applicant._guardianDat._guardZipcd);
                printAddr1(applicant._guardianDat._guardPrefCd, applicant._guardianDat._guardAddr1, applicant._guardianDat._guardAddr2);
                printAddr2(applicant._guardianDat._guardAddr3);
            } else {
                prtDestinationStudent(applicant);
            }
        }

        private void prtDestinationGuarant(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorAddr1.length() != 0) {
                printApplicantZip(applicant._guardianDat._guarantorZipcd);
                printAddr1(applicant._guardianDat._guarantorPrefCd, applicant._guardianDat._guarantorAddr1, applicant._guardianDat._guarantorAddr2);
                printAddr2(applicant._guardianDat._guarantorAddr3);
            } else {
                prtDestinationProtect(applicant);
            }
        }

        public void printAddr1(String pPrefCd, String pAdd1, String pAdd2) {
            String addres = _param._prefMapString(pPrefCd)
                                + (pAdd1 != null ? pAdd1 : "")
                                + (pAdd2 != null ? pAdd2 : "");

            printApplicantAddr1_1(addres);
        }

        public void printAddr2(String pAdd3) {
            printApplicantAddr1_2(pAdd3 != null ? pAdd3 : "");
        }

        /** 郵便版号 */
        public void printApplicantZip(String pZip) {
            _form._svf.VrsOut("GZIPCD", pZip);
        }

        /** 送付先住所１ */
        public void printApplicantAddr1_1(String pAddres) {
            _form._svf.VrsOut("G_ADDRESS1_1", pAddres);
        }

        /** 送付先住所２ */
        public void printApplicantAddr1_2(String pAddres) {
            _form._svf.VrsOut("G_ADDRESS1_2", pAddres);
        }

        /** 送付先氏名 */
        public void printApplicantName(String pName) {
            _form._svf.VrsOut("G_NAME", pName);
        }

        /** 送付先氏名印刷 */
        public void printShipName(ApplicantBaseMst applicant) {
            if (_param._schregno.length() == 0) {
                printShipNameApplicant(applicant);
            } else {
                if (_param._select.equals(DESTINATION_PROTECT) || _param._select.equals(DESTINATION_STUDENT)) {
                    prtDestinationProtectName(applicant);
                } else if (_param._select.equals(DESTINATION_GUARANT)){
                    prtDestinationGuarantName(applicant);
                }
            }
        }

        public void printShipNameApplicant(ApplicantBaseMst applicant) {
            if (!_param._select.equals(DESTINATION_STUDENT)) {
                prtDestinationApplicantProtectName(applicant);
            }
        }

        private void prtDestinationApplicantProtectName(ApplicantBaseMst applicant) {
            if (applicant._gname != null) {
                printApplicantName(applicant._gname + PRINT_STATE);
            }
        }

        private void prtDestinationProtectName(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guardName.length() != 0) {
                printApplicantName(applicant._guardianDat._guardName + PRINT_STATE);
            } else {
                printShipNameApplicant(applicant);
            }
        }

        private void prtDestinationGuarantName(ApplicantBaseMst applicant) {
            if (applicant._guardianDat._guarantorName.length() != 0) {
                printApplicantName(applicant._guardianDat._guarantorName + PRINT_STATE);
            } else {
                prtDestinationProtectName(applicant);
            }
        }
    }

    // ======================================================================
    /**
     * 志願者基礎マスタ。
     */
    private class ApplicantBaseMst {
        private final String _schregNo;     // 学籍番号
        private final String _courseCd;     // 課程コード
        private final String _majorCd;      // 学科コード
        private final String _courseCode;   // コースコード
        private final String _name;         // 氏名
        private final String _birthDay;     //生年月日
        private final int _age;             //年齢
        private final String _zipcd;        // 郵便番号
        private final String _prefCd;       // 都道府県コード
        private final String _addr1;        // 住所1
        private final String _addr2;        // 住所2
        private final String _addr3;        // 住所3
        private final String _gname;        // 保護者氏名
        private final String _gzipCd;       // 保護者郵便番号
        private final String _gprefCd;      // 保護者都道府県コード
        private final String _gaddr1;       // 保護者住所1
        private final String _gaddr2;       // 保護者住所2
        private final String _gaddr3;       // 保護者住所3
        private final String _claimSend;    // 請求先

        private Major _major;                               // 学科データ
        private CourseCodeMst _courseCodeMst;               // コースコード　データ
        private SchregAddressDat _SchregAddressDat;     // 学籍住所データ
        private GuardianDat _guardianDat;
        private SchregBaseMst _schregBaseMst;

        ApplicantBaseMst(
                final String schregNo,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name,
                final String birthDay,
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String gname,
                final String gzipCd,
                final String gprefCd,
                final String gaddr1,
                final String gaddr2,
                final String gaddr3,
                final String claimSend
        ) {
            _schregNo = schregNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _birthDay = birthDay;
            _age = getAge(_param._loginDate, _birthDay);
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _gname = gname;
            _gzipCd = gzipCd;
            _gprefCd = gprefCd;
            _gaddr1 = gaddr1;
            _gaddr2 = gaddr2;
            _gaddr3 = gaddr3;
            _claimSend = claimSend;
        }

        public void load(DB2UDB db2) throws SQLException {
            _major = createMajorDat(db2, _courseCd, _majorCd);
            _courseCodeMst = createCourseCodeDat(db2, _courseCode);
            _SchregAddressDat = createStudentSchregAddressDat(db2, _schregNo);
            _guardianDat = createGuardianDat(db2, _schregNo);
            _schregBaseMst = createSCHREG_BASE_MST(db2);
        }

        public int getAge(String date, String birthday) {
            final Calendar nowCal;
            try {
                nowCal = parseDate(date);
                int nowYear = nowCal.get(Calendar.YEAR);
                int nowMonth = nowCal.get(Calendar.MONTH) + 1;
                int nowDay = nowCal.get(Calendar.DATE);

                if (birthday == null) {
                    return 0;
                }

                final Calendar cal = KNJServletUtils.parseDate(birthday);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DATE);

                 return Integer.parseInt(""+(nowYear-year -
                  ((month > nowMonth || (month == nowMonth &&
                          day > nowDay)) ? 1:0)));
            } catch (ParseException e) {
                return 0;
            }
        }

        public Calendar parseDate(final String dateStr) throws ParseException {
            return parseDate(dateStr, "yyyy/MM/dd");
        }

        private Calendar parseDate(final String dateStr, final String pattern) throws ParseException {
            // 文字列を Date型に
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            final Date date = format.parse(dateStr);

            // Date型を Calendar に
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        }

        private boolean isUnder20() {
            return _age < 20;
        }
    }

    public ApplicantBaseMst createApplicant(DB2UDB db2, String applicantNo)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlApplicantBaseMst(applicantNo));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String courseCd = rs.getString("courseCd");
                final String majorCd = rs.getString("majorCd");
                final String courseCode = rs.getString("courseCode");
                final String name = rs.getString("name");
                final String birthDay = rs.getString("birthDay");
                final String zipcd = rs.getString("zipcd");
                final String prefCd = rs.getString("prefCd");
                final String addr1 = rs.getString("addr1");
                final String addr2 = rs.getString("addr2");
                final String addr3 = rs.getString("addr3");
                final String gname = rs.getString("gname");
                final String gzipCd = rs.getString("gzipCd");
                final String gprefCd = rs.getString("gprefCd");
                final String gaddr1 = rs.getString("gaddr1");
                final String gaddr2 = rs.getString("gaddr2");
                final String gaddr3 = rs.getString("gaddr3");
                final String claimSend = rs.getString("claimSend");

                final ApplicantBaseMst applicantBaseMst = new ApplicantBaseMst(
                        schregNo,
                        courseCd,
                        majorCd,
                        courseCode,
                        name,
                        birthDay,
                        zipcd,
                        prefCd,
                        addr1,
                        addr2,
                        addr3,
                        gname,
                        gzipCd,
                        gprefCd,
                        gaddr1,
                        gaddr2,
                        gaddr3,
                        claimSend
                );

                applicantBaseMst.load(db2);
                return applicantBaseMst;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>APPLICANT_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlApplicantBaseMst(String applicantNo) {
        return " select"
        + "    SCHREGNO as schregNo,"
        + "    COURSECD as courseCd,"
        + "    MAJORCD as majorCd,"
        + "    COURSECODE as courseCode,"
        + "    NAME as name,"
        + "    BIRTHDAY as birthDay,"
        + "    ZIPCD as zipcd,"
        + "    PREF_CD as prefCd,"
        + "    ADDR1 as addr1,"
        + "    ADDR2 as addr2,"
        + "    ADDR3 as addr3,"
        + "    GNAME as gname,"
        + "    GZIPCD as gzipCd,"
        + "    GPREF_CD as gprefCd,"
        + "    GADDR1 as gaddr1,"
        + "    GADDR2 as gaddr2,"
        + "    GADDR3 as gaddr3,"
        + "    CLAIM_SEND as claimSend"
        + " from"
        + "    APPLICANT_BASE_MST"
        + " where"
        + "    APPLICANTNO = '" + applicantNo + "'"
        ;
    }

    // ======================================================================
    /**
     * 学科データ。
     */
    private class Major {
        private final String _majorName;

        Major(
                final String majorName
        ) {
            _majorName = majorName;
        }

        public Major() {
            _majorName = "";
        }
    }

    public Major createMajorDat(DB2UDB db2, String courseCd, String majorCd)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlMajorDat(courseCd, majorCd));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");

                final Major major = new Major(
                        name
                );
                return major;
            }
            return new Major();
    }

    private String sqlMajorDat(String coursecd, String majorCd) {
        return " select"
                + "    MAJORNAME as name"
                + " from"
                + "    MAJOR_MST"
                + " where"
                + "    COURSECD = '" + coursecd + "' and"
                + "    MAJORCD = '" + majorCd + "'"
                ;
    }

    // ======================================================================
    /**
     * コースコード　データ。
     */
    private class CourseCodeMst {
        private final String _courseCodeName;

        CourseCodeMst(
                final String courseCodeName
        ) {
            _courseCodeName = courseCodeName;
        }

        public CourseCodeMst() {
            _courseCodeName = "";
        }
    }

    public CourseCodeMst createCourseCodeDat(DB2UDB db2, String courseCode)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlCourseCodeDat(courseCode));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");

                final CourseCodeMst courseCodeMst = new CourseCodeMst(
                        name
                );
                return courseCodeMst;
            }
            return new CourseCodeMst();
    }

    private String sqlCourseCodeDat(String courseCode) {
        return " select"
                + "    COURSECODENAME as name"
                + " from"
                + "    COURSECODE_MST"
                + " where"
                + "    COURSECODE = '" + courseCode + "'"
                ;
    }

    // ======================================================================
    /**
     * 職員。
     */
    private class Staff {
        private final String _name; // 職員氏名

        Staff(final String name) {
            _name = name;
        }

        public Staff() {
            _name = "";
        }
    }

    private Staff createStaff(DB2UDB db2) throws SQLException {
        final String sql = sqlStaff();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String staffname = rs.getString("staffName");

            final Staff staff = new Staff(staffname);
            return staff;
        }

        return new Staff();
    }

    private String sqlStaff() {
        return " select"
                + "    T2.STAFFNAME as  staffName"
                + " from"
                + "    STAFF_YDAT T1, STAFF_MST T2"
                + " where"
                + "    T1.YEAR = '" + _param._year + "'"
                + "    and T2.STAFFCD = T1.STAFFCD"
                + "    and T2.JOBCD = '" + STAFF_CD + "'"
        ;
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */
    private class School {
        private final String _schoolName1; // 学校名1

        School(final String schoolName1) {
            _schoolName1 = schoolName1;
        }

        public School() {
            _schoolName1 = "";
        }
    }

    private School createSchool(DB2UDB db2, String year) throws SQLException {
        final String sql = sqlSchool(year);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schoolName1 = rs.getString("schoolName1");

            final School school = new School(schoolName1);
            return school;
        }

        return new School();
    }

    private String sqlSchool(String year) {
        return " select"
                + "    SCHOOLNAME1 as schoolName1"
                + " from"
                + "    SCHOOL_MST"
                + " where"
                + "    YEAR = '" + year + "'";
    }

    // ======================================================================
    /**
     * 職名マスタ。
     */
    private class JobMst {
        private final String _jobName; // 職名

        JobMst(final String jobName) {
            _jobName = jobName;
        }

        public JobMst() {
            _jobName = "";
        }
    }

    private JobMst createJobMst(DB2UDB db2, String jobCd) throws SQLException {
        final String sql = sqlJobMst(jobCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String jobName = rs.getString("jobName");

            final JobMst jobMst = new JobMst(jobName);
            return jobMst;
        }

        return new JobMst();
    }

    private String sqlJobMst(String jobCd) {
        return " select"
                + "    JOBNAME as  jobName"
                + " from"
                + "    JOB_MST"
                + " where"
                + "    JOBCD = '" + jobCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍住所データ。
     */
    private class SchregAddressDat {
        private final String _zipcd; // 郵便番号
        private final String _prefCd; // 都道府県
        private final String _addr1; // 住所１
        private final String _addr2; // 住所２
        private final String _addr3; // 住所３

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
        }

        public SchregAddressDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
        }
    }

    private SchregAddressDat createStudentSchregAddressDat(DB2UDB db2, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlSchregAddressDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String zipcd = rs.getString("zipcd");
            final String prefCd = rs.getString("prefCd");
            final String addr1 = rs.getString("addr1");
            final String addr2 = rs.getString("addr2");
            final String addr3 = rs.getString("addr3");

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3
            );
            return studentSchregAddressDat;
        }
        return new SchregAddressDat();
    }

    private String sqlSchregAddressDat(String schregno) {
        return " select"
                + "    ZIPCD as zipcd,"
                + "    PREF_CD as prefCd,"
                + "    ADDR1 as addr1,"
                + "    ADDR2 as addr2,"
                + "    ADDR3 as addr3"
                + " from"
                + "    SCHREG_ADDRESS_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by ISSUEDATE DESC";
    }

    // ======================================================================
    /**
     * 生徒。学籍保護者データ。
     */
    private class GuardianDat {
        private final String _guardName;        // 保護者氏名
        private final String _guardZipcd;       // 郵便番号
        private final String _guardPrefCd;      // 都道府県コード
        private final String _guardAddr1;       // 住所1
        private final String _guardAddr2;       // 住所2
        private final String _guardAddr3;       // 住所3
        private final String _guarantorName;    // 保証人氏名
        private final String _guarantorZipcd;   // 保証人郵便番号
        private final String _guarantorPrefCd;  // 保証人都道府県コード
        private final String _guarantorAddr1;   // 保証人住所1
        private final String _guarantorAddr2;   // 保証人住所2
        private final String _guarantorAddr3;   // 保証人住所3

        GuardianDat(
                final String guardName,
                final String guardZipcd,
                final String guardPrefCd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardAddr3,
                final String guarantorName,
                final String guarantorZipcd,
                final String guarantorPrefCd,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorAddr3
        ) {
            _guardName = guardName;
            _guardZipcd = guardZipcd;
            _guardPrefCd = guardPrefCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardAddr3 = guardAddr3;
            _guarantorName = guarantorName;
            _guarantorZipcd = guarantorZipcd;
            _guarantorPrefCd = guarantorPrefCd;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorAddr3 = guarantorAddr3;
        }

        public GuardianDat() {
            _guardName = "";
            _guardZipcd = "";
            _guardPrefCd = "";
            _guardAddr1 = "";
            _guardAddr2 = "";
            _guardAddr3 = "";
            _guarantorName = "";
            _guarantorZipcd = "";
            _guarantorPrefCd = "";
            _guarantorAddr1 = "";
            _guarantorAddr2 = "";
            _guarantorAddr3 = "";
        }
    }

    private GuardianDat createGuardianDat(DB2UDB db2, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlGuardianDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String guardName = rs.getString("guardName");
            final String guardZipcd = rs.getString("guardZipcd");
            final String guardPrefCd = rs.getString("guardPrefCd");
            final String guardAddr1 = rs.getString("guardAddr1");
            final String guardAddr2 = rs.getString("guardAddr2");
            final String guardAddr3 = rs.getString("guardAddr3");
            final String guarantorName = rs.getString("guarantorName");
            final String guarantorZipcd = rs.getString("guarantorZipcd");
            final String guarantorPrefCd = rs.getString("guarantorPrefCd");
            final String guarantorAddr1 = rs.getString("guarantorAddr1");
            final String guarantorAddr2 = rs.getString("guarantorAddr2");
            final String guarantorAddr3 = rs.getString("guarantorAddr3");

            final GuardianDat guardianDat = new GuardianDat(
                    guardName,
                    guardZipcd,
                    guardPrefCd,
                    guardAddr1,
                    guardAddr2,
                    guardAddr3,
                    guarantorName,
                    guarantorZipcd,
                    guarantorPrefCd,
                    guarantorAddr1,
                    guarantorAddr2,
                    guarantorAddr3
            );
            return guardianDat;
        }
        return new GuardianDat();
    }

    private String sqlGuardianDat(String schregno) {
        return " select"
                + "    GUARD_NAME as guardName,"
                + "    GUARD_ZIPCD as guardZipcd,"
                + "    GUARD_PREF_CD as guardPrefCd,"
                + "    GUARD_ADDR1 as guardAddr1,"
                + "    GUARD_ADDR2 as guardAddr2,"
                + "    GUARD_ADDR3 as guardAddr3,"
                + "    GUARANTOR_NAME as guarantorName,"
                + "    GUARANTOR_ZIPCD as guarantorZipcd,"
                + "    GUARANTOR_PREF_CD as guarantorPrefCd,"
                + "    GUARANTOR_ADDR1 as guarantorAddr1,"
                + "    GUARANTOR_ADDR2 as guarantorAddr2,"
                + "    GUARANTOR_ADDR3 as guarantorAddr3"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍基礎マスタ。
     */
    private class SchregBaseMst {
        private final String _name;          // 氏名

        SchregBaseMst(final String name) {
            _name = name;
        }

        public SchregBaseMst() {
            _name = "";
        }
    }

    public SchregBaseMst createSCHREG_BASE_MST(DB2UDB db2)
        throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregBaseMst(_param._schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String name = rs.getString("name");

            final SchregBaseMst schregBaseMst = new SchregBaseMst(name);

            return schregBaseMst;
        }

        return new SchregBaseMst();
    }

    private String sqlSchregBaseMst(String schregNo) {
        return " select"
                + "    value(NAME, '') as name"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where"
                + "    SCHREGNO = '" + schregNo + "'"
                ;
    }
} // KNJWP101

// eof

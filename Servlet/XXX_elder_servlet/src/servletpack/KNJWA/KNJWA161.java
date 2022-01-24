// kanji=漢字
/*
 * $Id: a02182a548ec91867a902c31624825f1111fb3d8 $
 *
 * 作成日: 2007/10/25 10:53:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
 * 生徒一覧
 * 
 * @author nakada
 * @version $Id: a02182a548ec91867a902c31624825f1111fb3d8 $
 */
public class KNJWA161 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA161.class);

    private static final String FORM_FILE = "KNJWA161.frm";

    /** 印字件数/ページ */
    private static final int StudentNumMax = 50;

    /*
     * 在学状態
     */
    /** 在学 */
    private static final String PRAM_STATE_ATTENDANCE  = "0";
    /** 卒業 */
    private static final String PRAM_STATE_GRADUATION = "1";
    /** 退学 */
    private static final String PRAM_STATE_WITHDRAWAL  = "2";
    /** 転学 */
    private static final String PRAM_STATE_CHANGE  = "3";
    /** 全て */
    private static final String PRAM_STATE_ALL  = "9";

    // 除籍(卒業)区分
    private static final String GRD_DIV_GRADUATION = "1";       // 卒業
    private static final String GRD_DIV_WITHDRAWAL  = "2";      // 退学
    private static final String GRD_DIV_CHANGE  = "3";          // 転学
    private static final String GRD_DIV_GRADUATION_POSSI = "4"; // 卒業見込
    private static final String GRD_DIV_REMOVAL = "9";          // 除籍(死亡等)

    // 文字数による出力項目切り分け基準
    private static final int NAME1_LENG = 10;           
    private static final int NAME2_LENG = 15;
    private static final int ADDRESS1_LENG = 20;
    private static final int ADDRESS2_LENG = 25;
    private static final int PERSON1_LENG = 10;
    private static final int PERSON2_LENG = 15;
    private static final int FIN_SCHOOL1_LENG = 10;
    private static final int FIN_SCHOOL2_LENG = 20;
    private static final int ANOTHER_SCHOOL1_LENG = 10;
    private static final int ANOTHER_SCHOOL2_LENG = 20;

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_ENT_DIV = "A002";
    private static final String NAME_MST_GRD_DIV = "A003";
    private static final String NAME_MST_SEX = "Z002";

    private Form _form;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _page;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);
        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);
            for (int i = 0; i < _param._categorySelected.length; i++) {
                _page = 0;

                final String hrClass = _param._categorySelected[i];
                _param.debugPrint(hrClass);

                Staff staff = createStaff(db2, hrClass);
                log.debug(">>>>>職員情報=" + staff);

                final List students = createStudents(db2, hrClass);
                printMain(staff, students);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(Staff staff, final List students) throws SQLException {
        int i = 0; // １ページあたり件数
        int no = 0; // 所属＋組あたり件数

        String newAnnual = null;
        String oldAnnual = null;

        if (!students.isEmpty()) {
            final Student studentKey = (Student) students.get(0);
            newAnnual = studentKey._annual;
            oldAnnual = studentKey._annual;
        }

        for (Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            i++;
            no++;

            newAnnual = student._annual;

            if (newAnnual.equals(oldAnnual)) {
                printStudent(i, no, student);
            } else {                
                printDate();
                printHeader(staff, oldAnnual);
                
                _form._svf.VrEndPage();
                _hasData = true;

                i = 1;
                printStudent(i, no, student);

                oldAnnual = newAnnual;
            }

            if (i >= StudentNumMax) {
                printDate();
                printHeader(staff, oldAnnual);
                
                _form._svf.VrEndPage();
                _hasData = true;

                i = 0;
                oldAnnual = newAnnual;
            }
        }
        if (i > 0) {
            printDate();
            printHeader(staff, newAnnual);

            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader(Staff staff, String annual) {
        // 所属
        _form._svf.VrsOut("BELONGING_NAME", _param._belongingName);

        // 担任
        _form._svf.VrsOut("STAFFNAME", staff._name);
            
        // 年次
        _form._svf.VrsOut("ANNUAL", Integer.valueOf(annual).toString());    // ゼロサプレス

        // 学生状態
        _form._svf.VrsOut("TRANSFER", _param.getTransferName());

        // ページ
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
    }

    private void printStudent(int j, int k, Student student)  throws SQLException {
        _form._svf.VrsOutn("NUMBER", j, String.valueOf(k));
        _form._svf.VrsOutn("SCHREGNO", j, student._schregno);
        _form.printName(student, j);
        _form._svf.VrsOutn("SEX", j, _param._sexString(student._sex));
        _form._svf.VrsOutn("IN_DIV", j, _param._entString(student._entDiv));
        _form._svf.VrsOutn("IN_DATE", j, getJDate(student._entDate));
        _form._svf.VrsOutn("GRD_DATE", j, getJDate(student._grdScheduleDate));
        _form._svf.VrsOutn("BIRTHDAY", j, getJDate(student._birthday));
        _form._svf.VrsOutn("ZIP", j, student._schregAddressDat._zipcd);
        _form._svf.VrsOutn("PREF_NAME", j, _param._prefMapString(student._schregAddressDat._prefCd));
        _form.printAddr(student, j);
        _form._svf.VrsOutn("TEL1", j, student._schregAddressDat._telno);
        _form._svf.VrsOutn("MOBILE", j, student._mobilePhoneNo);
        _form._svf.VrsOutn("EMAIL", j, student._schregAddressDat._email);
        _form._svf.VrsOutn("TEL2", j, student._emergencyTelNo);
        _form.printEmergencyName(student, j);
        _form.printFinSchoolCd(student, j);
        _form.printAnotherSchool(student, j);
    }

    private static String getJDate(String entDate) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(entDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }

    private String sqlName(String nameCd1, String nameCd2) {
        return " select"
                + "    T1.NAME1 as name"
                + " from"
                + "    NAME_MST T1"
                + " where"
                + "    T1.nameCd1 = '" + nameCd1 + "' and"
                + "    T1.nameCd2 = '" + nameCd2 + "'";
    }

    private Staff createStaff(DB2UDB db2, String hr_class) throws SQLException {
        final String sql = sqlStaff(hr_class);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String staffcd = rs.getString("tr_cd1");
                final String staffname = rs.getString("staffname");

                final Staff staff = new Staff(staffcd, staffname);
                return staff;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return null;
    }

    private String sqlStaff(String tr_cd1) {
        return " select"
                + "    SCHREG_REGD_HDAT.TR_CD1  as  tr_cd1,"
                + "    STAFF_MST.STAFFNAME as  staffname"
                + " from"
                + "    SCHREG_REGD_HDAT left join STAFF_MST on SCHREG_REGD_HDAT.TR_CD1 = STAFF_MST.STAFFCD"
                + " where" + "    SCHREG_REGD_HDAT.YEAR = '"
                + _param._year + "' and"
                + "    SCHREG_REGD_HDAT.SEMESTER = '" + _param._semester
                + "' and" + "    SCHREG_REGD_HDAT.GRADE = '"
                + _param._grade + "' and"
                + "    SCHREG_REGD_HDAT.HR_CLASS = '" + tr_cd1 + "'";
    }

    private void printDate() {
        _form._svf.VrsOut("DATE", getJDate(_param._date));
    }

    private List createStudents(final DB2UDB db2, String hrClass) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlStudents(hrClass));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String annual = rs.getString("annual");
                final String name = rs.getString("name");
                final String birthday = rs.getString("birthday");
                final String sex = rs.getString("sex");
                final String applicantNo = rs.getString("applicantNo");
                final String finSchoolCd = rs.getString("finSchoolCd");
                final String entDate = rs.getString("entDate");
                final String entDiv = rs.getString("entDiv");
                final String grdDiv = rs.getString("grdDiv");
                final String grdScheduleDate = rs.getString("grdScheduleDate");
                final String emergencyName = rs.getString("emergencyName");
                final String emergencyTelNo = rs.getString("emergencyTelNo");
                final String mobilePhoneNo = rs.getString("mobilePhoneNo");

                final Student schregRegdDat = new Student(schregno,
                        annual,
                        name,
                        sex,
                        entDiv,
                        entDate,
                        grdScheduleDate,
                        birthday,
                        emergencyTelNo,
                        emergencyName,
                        finSchoolCd,
                        applicantNo,
                        grdDiv,
                        mobilePhoneNo
                );

                // 学籍状態条件判定
                if (judgStudent(schregRegdDat)) {
                    schregRegdDat.load(db2);

                    rtn.add(schregRegdDat);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /**
     * 前籍校履歴データ取得
     * @param db22
     * @param applicantNo
     */
    private AnotherSchoolHistDat createStudentAnotherSchoolHistDat(DB2UDB db2, String applicantNo)
        throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlAnotherSchoolHistDat(applicantNo));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String anoName = rs.getString("anoName");
            
            final AnotherSchoolHistDat studentAnotherSchoolHistDat = new AnotherSchoolHistDat(
                    anoName
            );
            return studentAnotherSchoolHistDat;
        }                    
        return new AnotherSchoolHistDat();
    }

    /**
     * 学籍住所データ取得
     * @param db2
     * @param schregno
     */
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
            final String telno = rs.getString("telno");
            final String email = rs.getString("email");

            final String addr = (addr1 != null ? addr1 : "")
                                    + (addr2 != null ? addr2 : "")
                                    + "　"
                                    + (addr3 != null ? addr3 : "");

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr,
                    telno,
                    email
            );
            return studentSchregAddressDat;
        }                    
        return new SchregAddressDat();
    }

    private String sqlAnotherSchoolHistDat(String applicantNo) {
        return " select"
                + "    T2.NAME as anoName"
                + " from"
                + "    ANOTHER_SCHOOL_HIST_DAT T1 left join FIN_HIGH_SCHOOL_MST T2 on T1.FORMER_REG_SCHOOLCD = T2.SCHOOL_CD"
                + " where"
                + "    T1.APPLICANTNO = '" + applicantNo + "'"
                + " order by T1.REGD_S_DATE DESC";
    }

    private String sqlSchregAddressDat(String schregno) {
        return " select"
                + "    T1.ZIPCD as zipcd,"
                + "    T1.PREF_CD as prefCd,"
                + "    T1.ADDR1 as addr1,"
                + "    T1.ADDR2 as addr2,"
                + "    T1.ADDR3 as addr3,"
                + "    T1.TELNO as telno,"
                + "    T1.EMAIL as email"
                + " from"
                + "    SCHREG_ADDRESS_DAT T1"
                + " where"
                + "    T1.SCHREGNO = '" + schregno + "'"
                + " order by T1.ISSUEDATE DESC";
    }

    private boolean judgStudent(Student schregRegdDat) {
        if (_param._grdDiv.equals(PRAM_STATE_ATTENDANCE)) {
            if ((schregRegdDat._grdDiv == null) ||
                    (schregRegdDat._grdDiv.equals(GRD_DIV_GRADUATION_POSSI))) {
                return true;
            }
        } else if ((_param._grdDiv.equals(PRAM_STATE_GRADUATION)) ||
                (_param._grdDiv.equals(PRAM_STATE_WITHDRAWAL)) ||
                (_param._grdDiv.equals(PRAM_STATE_CHANGE))) {
          if (schregRegdDat._grdDiv.equals(_param._grdDiv)){
              return true;
          }
        } else {
            return true;
        }
        return false;
    }

    private String sqlStudents(String hrClass) {
        return " select"
                + "    T2.SCHREGNO as schregno,"
                + "    T2.ANNUAL as annual,"
                + "    T3.NAME as name,"
                + "    T3.BIRTHDAY as birthday,"
                + "    T3.SEX as sex,"
                + "    T3.APPLICANTNO as applicantNo,"
                + "    T3.FINSCHOOLCD as finSchoolCd,"
                + "    T3.ENT_DATE as entDate,"
                + "    T3.ENT_DIV as entDiv,"
                + "    T3.GRD_DIV as grdDiv,"
                + "    T3.GRD_SCHEDULE_DATE as grdScheduleDate,"
                + "    T3.EMERGENCYNAME as emergencyName,"
                + "    T3.EMERGENCYTELNO as emergencyTelNo,"
                + "    T3.MOBILE_PHONE_NO as mobilePhoneNo"
                + " from"
                + "    SCHREG_REGD_HDAT T1 left join SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and"
                + "    T1.SEMESTER = T2.SEMESTER and"
                + "    T1.GRADE = T2.GRADE and"
                + "    T1.HR_CLASS = T2.HR_CLASS"
                + "    left join SCHREG_BASE_MST T3 on T2.SCHREGNO = T3.SCHREGNO"
                + " where" + "    T1.YEAR = '" + _param._year + "' and"
                + "    T1.SEMESTER = '" + _param._semester + "' and"
                + "    T1.GRADE = '" + _param._grade + "' and"
                + "    T1.HR_CLASS = '" + hrClass + "'" + " order by"
                + "    T2.ANNUAL," + "T2.SCHREGNO";
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programid = request.getParameter("PROGRAMID");
        final String dbname = request.getParameter("DBNAME");
        final String grade = request.getParameter("GRADE");
        final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        final String transfer = request.getParameter("GRD_DIV");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));

        final Param param = new Param(
                year,
                semester,
                programid,
                dbname,
                grade,
                categorySelected,
                transfer,
                date
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

        public Form(final String file, final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 1);
        }

        public void printAnotherSchool(Student student, int j) {
            String another = student._anotherSchoolHistDat._anoName;
            _form._svf.VrsOutn("ANOTHER_SCHOOL", j, another);

            if (another != null && another.length() <= ANOTHER_SCHOOL1_LENG) {
                _form._svf.VrsOutn("ANOTHER_SCHOOL1", j, another);
            } else {
                _form._svf.VrsOutn("ANOTHER_SCHOOL2", j, another);
            }
        }

        public void printFinSchoolCd(Student student, int j) throws SQLException {
            String name = getFJHSchName(student._finSchoolCd);

            final String label;
            if (name != null && name.length() <= FIN_SCHOOL1_LENG) {
               label = "FIN_SCHOOL1";
            } else {
                label = "FIN_SCHOOL2";
            }
            _form._svf.VrsOutn(label, j, name);
        }

        private String getFJHSchName(String finSchoolCd) throws SQLException {
            final String sql = sqlFinJName(finSchoolCd);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    return rs.getString("name");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return null;
        }

        private String sqlFinJName(String schoolCd) {
            return " select"
                    + "    T1.NAME as name"
                    + " from"
                    + "    FIN_JUNIOR_HIGHSCHOOL_MST T1"
                    + " where"
                    + "    T1.SCHOOL_CD = '" + schoolCd + "'";
        }

        public void printEmergencyName(Student student, int j) {
            String emergencyName = student._emergencyName;
            if (emergencyName != null && emergencyName.length() <= PERSON1_LENG) {
                _form._svf.VrsOutn("PERSON1", j, emergencyName);
            } else {
                _form._svf.VrsOutn("PERSON2", j, emergencyName);
            }
        }

        public void printAddr(Student student, int j) {
            String addr = student._schregAddressDat._addr;
            if (addr != null && addr.length() <= ADDRESS1_LENG) {
                _form._svf.VrsOutn("ADDRESS1", j, addr);
            } else {
                _form._svf.VrsOutn("ADDRESS2", j, addr);
            }
        }

        public void printName(Student student, int j) {
            String name = student._name;

            final String label;
            if (name != null && name.length() <= NAME1_LENG) {
               label = "NAME1";
            } else {
                label = "NAME2";
            }
            _form._svf.VrsOutn(label, j, name);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    // ======================================================================
    /**
     * 職員。
     */
    private class Staff {
        private final String _trCd1; // 担任コード１

        private final String _name; // 職員氏名

        Staff(final String trCd1, final String name) {
            _trCd1 = trCd1;
            _name = name;
        }

        public String toString() {
            return _trCd1 + ":" + _name;
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregno; // 学籍番号
        private final String _annual; // 年次
        private final String _name; // 氏名
        private final String _sex; // 性別
        private final String _entDiv; // 入学区分
        private final String _entDate; // 入学日付
        private final String _grdScheduleDate; // 卒業予定日付
        private final String _birthday; // 生年月日
        private final String _emergencyTelNo; // 急用電話番号
        private final String _emergencyName; // 急用連絡氏名
        private final String _finSchoolCd; // 出身中学校コード
        private final String _applicantNo; // 志願者番号
        private final String _grdDiv; // 除籍(卒業)区分
        private final String _mobilePhoneNo;

        private SchregAddressDat _schregAddressDat;         // 学籍住所データ
        private AnotherSchoolHistDat _anotherSchoolHistDat; // 前籍校履歴データ

        Student(final String schregno, 
                final String annual,
                final String name,
                final String sex,
                final String entDiv, 
                final String entDate, 
                final String grdScheduleDate, 
                final String birthday,
                final String emergencyTelNo,
                final String emergencyName,
                final String finSchoolCd,
                final String applicantNo, 
                final String grdDiv,
                final String mobilePhoneNo
        ) {
            _schregno = schregno;
            _annual = annual;
            _name = name;
            _sex = sex;
            _entDiv = entDiv;
            _entDate = entDate;
            _grdScheduleDate = grdScheduleDate;
            _birthday = birthday;
            _emergencyTelNo = emergencyTelNo;
            _emergencyName = emergencyName;
            _finSchoolCd = finSchoolCd;
            _applicantNo = applicantNo;
            _grdDiv = grdDiv;
            _mobilePhoneNo = mobilePhoneNo;
        }

        public void load(DB2UDB db2) throws SQLException {
            _schregAddressDat = createStudentSchregAddressDat(db2, _schregno);
            _anotherSchoolHistDat = createStudentAnotherSchoolHistDat(db2, _applicantNo);
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍住所データ。
     */
    private class SchregAddressDat {
        private final String _zipcd; // 郵便番号
        private final String _prefCd; // 都道府県
        private final String _addr; // 住所
        private final String _telno; // 電話番号
        private final String _email; // e-mail

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr,
                final String telno,
                final String email
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr = addr;
            _telno = telno;
            _email = email;
        }

        public SchregAddressDat() {
            super();
            _zipcd = null;
            _prefCd = null;
            _addr = null;
            _telno = null;
            _email = null;
        }
    }

    // ======================================================================
    /**
     * 生徒。前籍校履歴データ。
     */
    private class AnotherSchoolHistDat {
        private final String _anoName; // 漢字学校名

        AnotherSchoolHistDat(
                final String anoName
        ) {
            _anoName = anoName;
        }

        public AnotherSchoolHistDat() {
            super();
            _anoName = null;
        }
    }
    
    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programid;
        private final String _dbname;
        private final String _grade;                // 所属
        private final String[] _categorySelected;   // 組
        private final String _grdDiv;             // 学籍状態
        private final String _date;
        private Map _prefMap;

        private String _belongingName;
        private Map _transferMap = new HashMap();
        private Map _sexMap;
        private Map _entMap;

        public Param(
                final String year,
                final String semester,
                final String programid,
                final String dbname,
                final String grade,
                final String[] categorySelected,
                final String grdDiv,
                final String date
        ) {
            _year = year;
            _semester = semester;
            _programid = programid;
            _dbname = dbname;
            _grade = grade;
            _categorySelected = categorySelected;
            _grdDiv = grdDiv;
            _date = date;
        }

        public String _entString(String entDiv) {
            return (String) _entMap.get(entDiv);
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String getTransferName() {
            final String code = (String) _transferMap.get(_grdDiv);
            if (null == code) {
                log.warn("該当する異動区分が見つからない:" + _grdDiv);
                return "???";
            }
            return code;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref);
        }

        public void debugPrint(String hrClass) {
            log.debug(">>>>>所属=" + _grade + ":" + _belongingName + ", 組=" + hrClass);
        }

        public void load(DB2UDB db2) throws SQLException {
            setBelongingName(db2);

            setTransferMap(db2);

            _sexMap = getNameMst(NAME_MST_SEX);
            log.debug("性別の一覧=" + _sexMap);

            _entMap = getNameMst(NAME_MST_ENT_DIV);
            log.debug("入学形態の一覧=" + _entMap);

            _prefMap = getPrefMst();
            log.debug("都道府県名の一覧=" + _prefMap);
            
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

        private void setTransferMap(DB2UDB db2) throws SQLException {
            _transferMap.put(PRAM_STATE_ATTENDANCE, "在学");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlTransfer());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    _transferMap.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            _transferMap.put(PRAM_STATE_ALL, "全て");

            log.debug("異動区分=" + _transferMap);
        }

        private String sqlTransfer() {
            return " select"
                    + " NAMECD2,"
                    + " NAME1"
                    + " from"
                    + "     V_NAME_MST"
                    + " where"
                    + "     NAMECD1 = '" + NAME_MST_GRD_DIV + "' and"
                    + "     YEAR = '" + _year + "'"
                    ;
        }

        private void setBelongingName(DB2UDB db2) throws SQLException {
            final String sql = sqlBelongingName();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _belongingName = rs.getString("Name");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlBelongingName() {
            return " select"
                    + "    t1.SCHOOLNAME1 as  name"
                    + " from"
                    + "    SCHREG_REGD_HDAT t0 left join BELONGING_MST t1 on t0.GRADE = t1.BELONGING_DIV"
                    + " where" + "    t0.YEAR = '" + _param._year + "' and"
                    + "    t0.SEMESTER = '" + _param._semester + "' and"
                    + "    t0.GRADE = '" + _param._grade + "'";
        }

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String name = rs.getString("name");
                    final String code = rs.getString("code");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAME1 as name,"
                    + "    NAMECD2 as code"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }
    }
} // KNJWA161

// eof

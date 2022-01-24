// kanji=漢字
/*
 * $Id: 0c9cc115c2d1b449957e33d4d9341725e7618558 $
 *
 * 作成日: 2007/11/31 08:56:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWL;

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

/**
 * 志願者一覧
 * 
 * @author nakada
 * @version $Id: 0c9cc115c2d1b449957e33d4d9341725e7618558 $
 */
public class KNJWL120 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWL120.class);

    private static final String FORM_FILE = "KNJWL120.frm";

    /** 印字件数/ページ */
    private static final int APPLICANT_MAX = 50;

    /* 
     * ソート条件 
     */
    /** 志願者番号 */
    private static final String APPLICANT_NO = "2";
    /** 出願日 */
    private static final String APPLICATION_DATE = "1";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 生徒氏名 */
    private static final int NAME_LENG = 20;
    /** 所属(拠点) */
    private static final int BELONGING1_LENG = 10;
    /** 志望コース名 */
    private static final int COURSE1_LENG = 10;
    /** 出身中学名 */
    private static final int FIN_SCHOOL1_LENG = 10;
    /** 前籍高校名 */
    private static final int ANOTHER_SCHOOL1_LENG = 10;

    /*
     * 名称マスタキー（NAMECD1）
     */
    /** 性別 */
    private static final String NAME_MST_SEX = "Z002";
    /** 受験区分 */
    private static final String APPLICANT_DIV = "W005";
    /** 手続区分 */
    private static final String PROCEDURE_DIV = "W008";

    /*
     *検定料
     */
    /** 受験区分："済" */
    private static final String KENTEI_NAME = "済";

    /*
     *合否
     */
    private static final String COMP_ENT_PASS_CD = "1";
    /** 合格 */
    private static final String COMP_ENT_PASS_NAME = "合";
    /** 否合格 */
    private static final String COMP_ENT_FAILURE_NAME = "否";

    /*
     *辞退
     */
    /** 志願辞退 */
    private static final String APPLICATION_REFUSAL = "2";
    /** 入学辞退 */
    private static final String ENT_SCH_REFUSAL = "4";

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

            final List applicants = createApplicants(db2);
            printMain(applicants);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List applicants) throws SQLException {
        int i = 0; // １ページあたり件数
        int no = 0; // №

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();

            AnotherSchoolHistDat anotherSchoolHistDat = createStudentAnotherSchoolHistDat(db2, applicant._applicantNo);
            
            i++;
            no++;

            printApplicant(i, no, applicant, anotherSchoolHistDat);

            if (i >= APPLICANT_MAX) {
                printDate();
                printHeader();

                _form._svf.VrEndPage();
                _hasData = true;

                i = 0;
            }
        }
        if (i > 0) {
            printDate();
            printHeader();

            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader() {
        // ページ
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
    }

    private void printApplicant(int i, int no, Applicant applicant, AnotherSchoolHistDat anotherSchoolHistDat) throws SQLException {
        _form._svf.VrsOutn("NUMBER", i, Integer.toString(no));
        _form._svf.VrsOutn("APPLICATION_DATE", i, getJDate(applicant._applicationDate));
        _form._svf.VrsOutn("APPLICANTNO", i, applicant._applicantNo);
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._sex));
        _form.printName(applicant, i);
        _form._svf.VrsOutn("KENTEI", i, KENTEI_NAME);
        _form.printcompEntName(applicant, i);
        _form.printRefusalName(applicant, i);
        _form._svf.VrsOutn("PREF_NAME", i, _param._prefMapString(applicant._prefCd));
        _form.printBelongingName(applicant, i);
        _form._svf.VrsOutn("ENT_SCHEDULE_DATE", i, getJDate(applicant._entScheduleDate));
        _form._svf.VrsOutn("IN_DIV", i, _param._applicantString(applicant._applicantDiv));

        _form.printAntAnnual(i, applicant);

        _form.printCourseName(applicant, i);
        _form.printFinSchoolCd(applicant, i);
        _form.printAnotherSchool(applicant, i, anotherSchoolHistDat);
        _form._svf.VrsOutn("TELNO", i, applicant._telno);
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

    private String sqlName(String nameCd1, String nameCd2) {
        return " select"
                + "    T1.NAME1 as name"
                + " from"
                + "    NAME_MST T1"
                + " where"
                + "    T1.nameCd1 = '" + nameCd1 + "' and"
                + "    T1.nameCd2 = '" + nameCd2 + "'";
    }

    private void printDate() {
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
    }

    private List createApplicants(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlApplicants(_param._year, _param._output));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String applicantNo = rs.getString("applicantNo");
                final String applicantDiv = rs.getString("applicantDiv");
                final String belongingDiv = rs.getString("belongingDiv");
                final String applicationDate = rs.getString("applicationDate");
                final String procedureDiv = rs.getString("procedureDiv");
                final String entScheduleDate = rs.getString("entScheduleDate");
                final String entAnnual = rs.getString("entAnnual");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String name = rs.getString("name");
                final String sex = rs.getString("sex");
                final String prefCd = rs.getString("prefCd");
                final String telno = rs.getString("telno");
                final String fsCd = rs.getString("fsCd");

                final Applicant applicantDat = new Applicant(
                        applicantNo,
                        applicantDiv,
                        belongingDiv,
                        applicationDate,
                        procedureDiv,
                        entScheduleDate,
                        entAnnual,
                        coursecd,
                        majorcd,
                        coursecode,
                        name,
                        sex,
                        prefCd,
                        telno,
                        fsCd
                );

                rtn.add(applicantDat);
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

        try{
            ps = db2.prepareStatement(sqlAnotherSchoolHistDat(applicantNo));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String anoName = rs.getString("anoName");
                
                final AnotherSchoolHistDat studentAnotherSchoolHistDat = new AnotherSchoolHistDat(
                        anoName
                );
                return studentAnotherSchoolHistDat;
            }             
        }finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return new AnotherSchoolHistDat();
    }

    private String sqlAnotherSchoolHistDat(String applicantNo) {
        final String sql;
        sql = " select"
                + "    T2.NAME as anoName"
                + " from"
                + "    ANOTHER_SCHOOL_HIST_DAT T1 left join FIN_HIGH_SCHOOL_MST T2 on T1.FORMER_REG_SCHOOLCD = T2.SCHOOL_CD"
                + " where"
                + "    T1.APPLICANTNO = '" + applicantNo + "'"
                + " order by T1.REGD_S_DATE DESC";
        return sql;
    }

    private String sqlApplicants(String year, String output) {

        StringBuffer stb = new StringBuffer();

        stb.append(" select"
                + "    T1.APPLICANTNO as applicantNo,"
                + "    T1.APPLICANT_DIV as applicantDiv,"
                + "    T1.BELONGING_DIV as belongingDiv,"
                + "    T1.APPLICATION_DATE as applicationDate,"
                + "    T1.PROCEDURE_DIV as procedureDiv,"
                + "    T1.ENT_SCHEDULE_DATE as entScheduleDate,"
                + "    T1.ENT_ANNUAL as entAnnual,"
                + "    T1.COURSECD as coursecd,"
                + "    T1.MAJORCD as majorcd,"
                + "    T1.COURSECODE as coursecode,"
                + "    T1.NAME as name,"
                + "    T1.SEX as sex,"
                + "    T1.PREF_CD as prefCd,"
                + "    T1.TELNO as telno,"
                + "    T1.FS_CD as fsCd "
                + " from"
                + "    APPLICANT_BASE_MST T1"
                + " where "
                + "    T1.YEAR = '"+year+"' "
                + " order by");

        if (_param._output.equals(APPLICANT_NO)) {
            stb.append("    T1.APPLICANTNO");
        } else {
            stb.append("    T1.APPLICATION_DATE");
        }

        return stb.toString();
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
        final String programId = request.getParameter("PROGRAMID");
        final String dbName = request.getParameter("DBNAME");
        final String output = request.getParameter("OUTPUT");
        final String loginDate = request.getParameter("LOGIN_DATE");

        final Param param = new Param(
                year,
                semester,
                programId,
                dbName,
                output,
                loginDate
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

        public void printRefusalName(Applicant applicant, int i) {
            if (applicant._procedureDiv != null) {
                if (applicant._procedureDiv.equals(APPLICATION_REFUSAL) ||
                        applicant._procedureDiv.equals(ENT_SCH_REFUSAL)) {
                    _form._svf.VrsOutn("REFUSAL", i, _param._procedureDivMapString(applicant._procedureDiv));
                }
            }
        }

        public void printcompEntName(Applicant applicant, int i) {
            String name =_param._claimDatMapString(applicant._applicantNo);

            if (name != null) {
                if (name.equals(COMP_ENT_PASS_CD)) {
                    _form._svf.VrsOutn("COMP_ENT", i, COMP_ENT_PASS_NAME);
                } else {
                    _form._svf.VrsOutn("COMP_ENT", i, COMP_ENT_FAILURE_NAME);
                }
            }
        }

        public void printAnotherSchool(Applicant applicant, int i, AnotherSchoolHistDat anotherSchoolHistDat) {
            String name = anotherSchoolHistDat._anoName;

            if (name != null) {
                final String label;
                if (name.length() <= ANOTHER_SCHOOL1_LENG) {
                   label = "ANOTHER_SCHOOL1";
                } else {
                    label = "ANOTHER_SCHOOL2";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printFinSchoolCd(Applicant applicant, int i) throws SQLException {
            final String name = (String) _param._fjhSchList.get(applicant._fsCd);

            if (name != null) {
                final String label;
                if (name.length() <= FIN_SCHOOL1_LENG) {
                   label = "FIN_SCHOOL1";
                } else {
                    label = "FIN_SCHOOL2";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printName(Applicant applicant, int j) {
            String name = applicant._name;

            if (name != null) {
                final String label;
                    label = "NAME";
                _form._svf.VrsOutn(label, j, name);
            }
        }

        public void printBelongingName(Applicant applicant, int j) {
            String name = _param._belongingMapString(applicant._belongingDiv);

            if (name != null) {
                final String label;
                if (name.length() <= BELONGING1_LENG) {
                    label = "BELONGING_NAME";
                } else {
                    label = "BELONGING_NAME2";
                }
                _form._svf.VrsOutn(label, j, name);
            }
        }

        public void printCourseName(Applicant applicant, int j) {
            String name = getCourseName(applicant);

            if (name != null) {
                final String label;
                if (name.length() <= COURSE1_LENG) {
                    label = "COURSE1";
                } else {
                    label = "COURSE2";
                }
                _form._svf.VrsOutn(label, j, name);
            }
        }

        private String getCourseName(Applicant applicant) {
            String name;
            String name1 = _param._courseMapString(applicant._coursecd);
            String name2 = _param._majorMapString(applicant._coursecd + applicant._majorcd);
            String name3 = _param._courseCodeMstMapString(applicant._coursecode);

            if (name1 == "" && name2 == "") {
                name = name3;
            } else {
                name = name1 + name2 + "・" + name3;
            }
            return name;
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

        void printAntAnnual(int i, Applicant applicant) {
            if (applicant._entAnnual != null) {
                if (Integer.valueOf(applicant._entAnnual).intValue() != 0) {
                    _svf.VrsOutn("ANNUAL", i,  Integer.valueOf(applicant._entAnnual).toString());    // ゼロサプレス
                }
            }
        }
    }

    // ======================================================================
    class Applicant {
        private final String _applicantNo;      // 志願者番号
        private final String _applicantDiv;     // 受験区分
        private final String _belongingDiv;     // 所属(拠点)
        private final String _applicationDate;  // 出願日
        private final String _procedureDiv;     // 手続区分
        private final String _entScheduleDate;  // 入学予定日
        private final String _entAnnual;        // 年次
        private final String _coursecd;         // 課程コード
        private final String _majorcd;          // 学科コード
        private final String _coursecode;       // コースコード
        private final String _name;             // 氏名
        private final String _sex;              // 性別コード
        private final String _prefCd;           // 都道府県コード
        private final String _telno;            // 電話番号
        private final String _fsCd;             // 出身中学校コード

        Applicant(final String applicantNo,
                final String applicantDiv,
                final String belongingDiv,
                final String applicationDate,
                final String procedureDiv,
                final String entScheduleDate,
                final String entAnnual,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String name,
                final String sex,
                final String prefCd,
                final String telno,
                final String fsCd
        ) {
            _applicantNo = applicantNo;
            _applicantDiv = applicantDiv;
            _belongingDiv = belongingDiv;
            _applicationDate = applicationDate;
            _procedureDiv = procedureDiv;
            _entScheduleDate = entScheduleDate;
            _entAnnual = entAnnual;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
            _sex = sex;
            _prefCd = prefCd;
            _telno = telno;
            _fsCd = fsCd;
        }

        public String toString() {
            return _applicantNo + ":" + _name;
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
            _anoName = "";
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _output;       // ソート条件
        private final String _loginDate;

        private Map _sexMap;
        private Map _applicantMap;
        private Map _prefMap;
        private Map _procedureDivMap;
        private Map _belongingMap;
        private Map _courseMap;        // 課程
        private Map _majorMap;         // 学科
        private Map _courseCodeMstMap; // コースコード
        /** 出身中学 */
        private Map _fjhSchList;
        private Map _claimDatMap;
       
        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String output,
                final String loginDate
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _output = output;
            _loginDate = loginDate;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _applicantString(String applicant) {
            return (String) _applicantMap.get(applicant);
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref);
        }

        public String _procedureDivMapString(String procedureDiv) {
            return (String) _procedureDivMap.get(procedureDiv);
        }

        public String _belongingMapString(String code) {
            return (String) nvlT((String)_belongingMap.get(code));
        }

        public String _courseMapString(String code) {
            return (String) nvlT((String)_courseMap.get(code));
        }

        public String _majorMapString(String code) {
            return (String) nvlT((String)_majorMap.get(code));
        }

        public String _courseCodeMstMapString(String code) {
            return (String) nvlT((String)_courseCodeMstMap.get(code));
        }

        public String _claimDatMapString(String code) {
            return (String) nvlT((String)_claimDatMap.get(code));
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            _applicantMap = getNameMst(APPLICANT_DIV);
            _prefMap = getPrefMst();
            _procedureDivMap = getNameMst(PROCEDURE_DIV);
            _belongingMap = createBelongingDat(db2);
            _courseMap = createCourseDat(db2);
            _majorMap = createMajorDat(db2);
            _courseCodeMstMap = createCourseCodeDat(db2);
            _fjhSchList = getFJHSchName();
            _claimDatMap = createClaimDat();

            return;
        }

        private Map getFJHSchName() throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlFinJName());
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

        private String sqlFinJName() {
            return " select"
                    + "    T1.SCHOOL_CD as code,"
                    + "    T1.NAME as name"
                    + " from"
                    + "    FIN_JUNIOR_HIGHSCHOOL_MST T1";
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
                    final String code = rs.getString("CODE");
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

        public Map createClaimDat()
            throws SQLException {

            final Map rtn = new HashMap();
            
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlClaimDat());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String applicantNo = rs.getString("applicantNo");
                final String compEntFlg = rs.getString("compEntFlg");

                rtn.put(applicantNo, compEntFlg);
            }
            return rtn;
        }

        private String sqlClaimDat() {
            final String sql;

            sql = " select"
                + "        T1.APPLICANTNO as applicantno,"
                + "        T1.COMP_ENT_FLG as compEntFlg"
                + " from"
                + "        CLAIM_DAT T1,"
                + "        (select"
                + "                APPLICANTNO,"
                + "                min(SLIP_NO) sno"
                + "           from CLAIM_DAT"
                + "          where"
                + "                SLIP_NO like'" + _param._year.substring(2, 4) + "%'"
                + "          group by APPLICANTNO) T2"
                + " where"
                + "        T2.APPLICANTNO = T1.APPLICANTNO"
                + "    and T2.sno = T1.SLIP_NO"
                + " order by T1.APPLICANTNO , T1.SLIP_NO";

            return sql;
        }
    }

    private int getPaymentMoney(ResultSet rs) throws SQLException {
        String str = rs.getString("paymentMoney");
        if (str != null) {
            return Integer.parseInt(str);
        }
        return 0;
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    public Map createBelongingDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlBelongingDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("belonging_div");
            final String name = rs.getString("schoolname1");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlBelongingDat() {
        return " select"
                + "    BELONGING_DIV as belonging_div,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    BELONGING_MST"
                ;
    }

    // ======================================================================
    /**
     * 課程データ。
     */
    public Map createCourseDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
    
        ps = db2.prepareStatement(sqlCourseDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = nvlT(rs.getString("clubcd"));
            final String name = nvlT(rs.getString("name"));
    
            rtn.put(code, name);
        }
    
        return rtn;
    }

    private String sqlCourseDat() {
        return " select"
                + "    COURSECD as clubcd,"
                + "    COURSENAME as name"
                + " from"
                + "    COURSE_MST"
                ;
    }

    // ======================================================================
    /**
     * 学科データ。
     */
    public Map createMajorDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlMajorDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code1 = rs.getString("coursecd");
            final String code2 = rs.getString("majorcd");
            final String name = rs.getString("name");
            
            rtn.put(code1 + code2, name);
        }

        return rtn;
    }

    private String sqlMajorDat() {
        return " select"
                + "    COURSECD as coursecd,"
                + "    MAJORCD as majorcd,"
                + "    MAJORNAME as name"
                + " from"
                + "    MAJOR_MST"
                ;
    }

    // ======================================================================
    /**
     * コースコード　データ。
     */
    public Map createCourseCodeDat(DB2UDB db2)
        throws SQLException {

    final Map rtn = new HashMap();

    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = db2.prepareStatement(sqlCourseCodeDat());
    rs = ps.executeQuery();
    while (rs.next()) {
        final String code = rs.getString("coursecode");
        final String name = rs.getString("name");
        
        rtn.put(code, name);
    }

        return rtn;
    }

    private String sqlCourseCodeDat() {
        return " select"
                + "    COURSECODE as coursecode,"
                + "    COURSECODENAME as name"
                + " from"
                + "    COURSECODE_MST"
                ;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }
} // KNJWL120

// eof

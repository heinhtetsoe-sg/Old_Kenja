// kanji=漢字
/*
 * $Id: b36dd769080489e7623d3f971718d82fad51e847 $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;
 
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 生徒証
 * @author nakada
 * @version $Id: b36dd769080489e7623d3f971718d82fad51e847 $
 */
public class KNJWA171 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA171.class);

    private static final String FORM_FILE = "KNJWA171.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 名前 */
    private static final int NAME1_LENG = 15;       // TODO:    暫定
    /** 住所 */
    private static final int ADD1_LENG = 25;        // TODO:    暫定
    private static final int ADD2_LENG = 25;        // TODO:    暫定
    private static final int ADD3_LENG = 25;        // TODO:    暫定
    /** 学習センター住所 */
    private static final int CENTER_ADD1_LENG = 25; // TODO:    暫定
    /** 発行者住所 */
    private static final int SCH_ADDR_LENG = 25;    // TODO:    暫定
    /** 発行者名称 */
    private static final int SCH_NAME_LENG = 25;    // TODO:    暫定

    /*
     * イメージファイル名
     */
    /** マーク */
    private static final String PIC_NAME1 = "MARK";
    /** 学校名 */
    private static final String PIC_NAME2 = "SCHOOLNAME";
    /** 生徒証バー */
    private static final String PIC_NAME3 = "STUDENTBAR";
    /** 学校印 */
    private static final String PIC_NAME4 = "SCHOOLSTAMP";
    /** 帯 */
    private static final String PIC_NAME5 = "BAR";
    /** 写真 */
    private static final String PIC_NAME6 = "P";

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

            for (int i = 0; i < _param._schregno.length; i++) {
                final String schregno = _param._schregno[i];
                log.debug(">>学籍番号=" + schregno);

                final Student student = createStudent(db2, schregno, i);
                printMain(student, i);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        }
        return _hasData;
    }

    private void printMain(final Student student, int i) throws SQLException {
            printApplicant(student, i);
            _form._svf.VrEndPage();
            _hasData = true;
    }
 
    private void printApplicant(Student student, int i) throws SQLException {
        /* № */
        _form._svf.VrsOut("SCHREGNO", _param._schregno[i]);
        /* 所属 */
        _form.printPosition(student);

        /* 氏名 */
        _form.printName(student);
        /* 生年月日 */
        _form._svf.VrsOut("BIRTHDAY", getJDate(student._birthday) + "生");
        /* 住所 */
        _form.printAddr(student);
        /* 発行日付 */
        _form._svf.VrsOut("DATE", getJDate(KNJ_EditDate.H_Format_Haifun(_param._public_date)));
        /* 有効期限 */
        if (null != student._certifIssueDat._endDate) {
            _form._svf.VrsOut("END_DATE", getJDate(student._certifIssueDat._endDate));
        }
        /* 学習センター住所 */
        _form.printCenterAddr(student);
        /* 発行者住所 */
        _form.printSchAddr(student);
        /* 発行者名称 */
        _form.printSchName(student);

        /* マーク */
        if (_param._imgFlg1) {
            _form._svf.VrsOut("MARK", _param._imgFIleName1);
        }

        /* 学校名 */
        if (_param._imgFlg2) {
            _form._svf.VrsOut("SCHOOLNAME", _param._imgFIleName2);
        }

        /* 生徒証バー */
        if (_param._imgFlg3) {
            _form._svf.VrsOut("STUDENTBAR", _param._imgFIleName3);
        }

        /* 学校印 */
        if (_param._imgFlg4) {
            _form._svf.VrsOut("SCHOOLSTAMP", _param._imgFIleName4);
        }

        /* 帯 */
        if (_param._imgFlg5) {
            _form._svf.VrsOut("BAR", _param._imgFIleName5);
        }

        /* 写真 */
        _form.printPic(i);
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
        private final String[] _schregno;
        private final String[] _certif;            // 通学証明書番号
        private final String _docBase;
        private final String _public_date;          // 発効日
        private final String _effect_date;          // 有効日
        private final String _exeYear;              // 指示画面検索年度

        private Map _prefMap;                       // 都道府県
        private Map _courseMap;                     // 課程
        private Map _majorMap;                      // 学科

        private String _imgFIle_pass;
        private String _imgFIle_extension;
        private boolean _imgFlg1;                // 存在有無フラグ
        private String _imgFIleName1;
        private boolean _imgFlg2;                // 存在有無フラグ
        private String _imgFIleName2;
        private boolean _imgFlg3;                // 存在有無フラグ
        private String _imgFIleName3;
        private boolean _imgFlg4;                // 存在有無フラグ
        private String _imgFIleName4;
        private boolean _imgFlg5;                // 存在有無フラグ
        private String _imgFIleName5;

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] schregno,
                final String[] certif,
                final String docBase,
                final String public_date,
                final String effect_date,
                final String exeYear
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _certif = certif;
            _docBase = docBase;
            _public_date = public_date;
            _effect_date = effect_date;
            _exeYear = exeYear;
        }

        public String _prefMapString(String pref) {
            return nvlT((String) _prefMap.get(pref));
        }

        public String _courseMapString(String code) {
            return nvlT((String) _courseMap.get(code));
        }

        public String _majorMapString(String code) {
            return nvlT((String) _majorMap.get(code));
        }

        public void load(DB2UDB db2) throws SQLException {
            _prefMap = getPrefMst();
            _courseMap = getCourseMst(db2);
            _majorMap = getMajorMst(db2);
            setImage();

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

        public Map getCourseMst(DB2UDB db2)
            throws SQLException {

            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlCourseDat());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = nvlT(rs.getString("code"));
                    final String name = nvlT(rs.getString("name"));
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlCourseDat() {
            return " select"
                    + "    COURSECD as code,"
                    + "    COURSENAME as name"
                    + " from"
                    + "    COURSE_MST"
                    ;
        }
    
        public Map getMajorMst(DB2UDB db2)
            throws SQLException {

            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlMajorDat());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code1 = rs.getString("code1");
                    final String code2 = rs.getString("code2");
                    final String name = rs.getString("name");
                    rtn.put(code1 + code2, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlMajorDat() {
            return " select"
                    + "    COURSECD as code1,"
                    + "    MAJORCD as code2,"
                    + "    MAJORNAME as name"
                    + " from"
                    + "    MAJOR_MST"
                    ;
        }

        public String setImage() {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            String folder = null;
            String extension = null;

            returnval = getinfo.Control(db2);
            folder = returnval.val4;            //格納フォルダ
            extension = returnval.val5;         //拡張子

            String image_pass = _param._docBase + "/" + folder + "/";   //イメージパス
            _imgFIle_pass = image_pass;
            _imgFIle_extension = extension;

            _imgFIleName1 = image_pass + PIC_NAME1 + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + _imgFIleName1);

            File f1 = new File(_imgFIleName1);

            if (!f1.exists()) {
                _imgFlg1 = false;
                log.debug(">>>イメージファイルがありません。：" + _imgFIleName1);
            } else {
                _imgFlg1 = true;
            }

            _imgFIleName2 = image_pass + PIC_NAME2 + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + _imgFIleName2);

            f1 = new File(_imgFIleName2);

            if (!f1.exists()) {
                _imgFlg2 = false;
                log.debug(">>>イメージファイルがありません。：" + _imgFIleName2);
            } else {
                _imgFlg2 = true;
            }

            _imgFIleName3 = image_pass + PIC_NAME3 + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + _imgFIleName3);

            f1 = new File(_imgFIleName3);

            if (!f1.exists()) {
                _imgFlg3 = false;
                log.debug(">>>イメージファイルがありません。：" + _imgFIleName3);
            } else {
                _imgFlg3 = true;
            }

            _imgFIleName4 = image_pass + PIC_NAME4 + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + _imgFIleName4);

            f1 = new File(_imgFIleName4);

            if (!f1.exists()) {
                _imgFlg4 = false;
                log.debug(">>>イメージファイルがありません。：" + _imgFIleName4);
            } else {
                _imgFlg4 = true;
            }

            _imgFIleName5 = image_pass + PIC_NAME5 + "." + extension;
            log.debug(">>>ドキュメントフルパス=" + _imgFIleName5);

            f1 = new File(_imgFIleName5);

            if (!f1.exists()) {
                _imgFlg5 = false;
                log.debug(">>>イメージファイルがありません。：" + _imgFIleName5);
            } else {
                _imgFlg5 = true;
            }

            return image_pass;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
        final String[] schregno = request.getParameterValues("SCHREGNO[]");
        final String[] certif = request.getParameterValues("SEITO[]");
        final String docBase = request.getParameter("DOCUMENTROOT");
        final String public_date = request.getParameter("DATE");
        final String effect_date = request.getParameter("END_DATE");
        final String exeYear = request.getParameter("EXE_YEAR");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                certif,
                docBase,
                public_date,
                effect_date,
                exeYear
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

        public void printPic(final int i) {
            String str = _param._imgFIle_pass + PIC_NAME6 + _param._schregno[i] + "." + _param._imgFIle_extension;
            log.debug(">>>ドキュメントフルパス=" + str);

            File f1 = new File(str);

            if (!f1.exists()) {
                log.debug(">>>イメージファイルがありません。：" + str);
            } else {
                _form._svf.VrsOut("PHOTO", str);
            }
        }

        public void printSchAddr(Student student) {
            String name = nvlT(student._certifIssueDat._certifschooldat._remark1);

            if (name != null) {
                final String label;
                if (name.length() <= SCH_ADDR_LENG) {
                    label = "REMARK";
                } else {
                    label = "REMARK";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printSchName(Student student) {
            String name = nvlT(student._certifIssueDat._certifschooldat._schoolName);

            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "SCHOOL_NAME";
                } else {
                    label = "SCHOOL_NAME";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printCenterAddr(Student student) {
            String name = nvlT(_param._prefMapString(nvlT(student._belonging._schoolprefCd)))
            + nvlT(student._belonging._schoolAddr1)
            + nvlT(student._belonging._schoolAddr2);

            if (name != null) {
                final String label;
                if (name.length() <= CENTER_ADD1_LENG) {
                    label = "SCHOOL_ADDRESS";
                } else {
                    label = "SCHOOL_ADDRESS";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printPosition(Student student) {
            _form._svf.VrsOut("COURSENAME",
                    nvlT(_param._courseMapString(student._schregRegdDat._courseCd))
                    + "課程"
                    + nvlT(_param._majorMapString(student._schregRegdDat._courseCd
                            + student._schregRegdDat._majorCd)));
        }

        public void printName(Student student) {
            String name = student._name;
            
            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME";
                } else {
                    label = "NAME";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAddr(Student student) {
            String name = nvlT(_param._prefMapString(nvlT(student._schregAddressDat._prefCd)));

            if (name != null) {
                final String label;
                if (name.length() <= ADD1_LENG) {
                    label = "PREF_NAME";
                } else {
                    label = "PREF_NAME";
                }
                _form._svf.VrsOut(label, name);
            }

            name = nvlT(student._schregAddressDat._addr1) + nvlT(student._schregAddressDat._addr2);

            if (name != null) {
                final String label;
                if (name.length() <= ADD2_LENG) {
                    label = "ADDRESS1";
                } else {
                    label = "ADDRESS1";
                }
                _form._svf.VrsOut(label, name);
            }
            
            name = nvlT(student._schregAddressDat._addr3);

            if (name != null) {
                final String label;
                if (name.length() <= ADD3_LENG) {
                    label = "ADDRESS2";
                } else {
                    label = "ADDRESS2";
                }
                _form._svf.VrsOut(label, name);
            }
        }
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private SchregAddressDat _schregAddressDat; // 生徒。学籍住所データ
        private CertifIssueDat _certifIssueDat;
        private Belonging _belonging;

        Student(final String schregNo,
                final String name,
                final String birthday
        ) {
            _schregNo = schregNo;
            _name = name;
            _birthday = birthday;
        }

        public void load(DB2UDB db2, int i) throws SQLException {
            _schregRegdDat = createSourseCodeDat(db2, _schregNo);
            _schregAddressDat = createSchregAddressDat(db2, _schregNo);
            _certifIssueDat = createCertifIssueDat(db2, i);
            _belonging = createBelongingDat(db2, _schregRegdDat._grade);
        }
    }

    private Student createStudent(final DB2UDB db2, String schregno, final int i)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String birthday = rs.getString("birthday");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        birthday
                );

                studentDat.load(db2, i);
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    BIRTHDAY as birthday"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }
    
    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _grade;         // 所属
        private final String _courseCd;     // 課程コード
        private final String _majorCd;      // 学科コード

        public SchregRegdDat() {
            _grade = "";
            _courseCd = "";
            _majorCd = "";
        }

        SchregRegdDat(
                final String grade,
                final String courseCd,
                final String majorCd
        ) {
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String SCHREGNO)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlSchregRegdDat(SCHREGNO));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("grade");
                final String courseCd = rs.getString("courseCd");
                final String majorCd = rs.getString("majorCd");

                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        grade,
                        courseCd,
                        majorCd
                );
                return schregRegdDat;
            }
            return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String schregNo) {
        return " select"
                + "    GRADE as grade,"
                + "    value(COURSECD, '') as courseCd,"
                + "    value(MAJORCD, '') as majorCd"
                + " from"
                + "    SCHREG_REGD_DAT"
                + " where"
                + "    SCHREGNO = '" + schregNo + "' and"
                + "    YEAR = '" + _param._exeYear + "' and"
                + "    SEMESTER = '" + _param._semester + "'"
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
        private final String _telno; // 電話番号

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String telno
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _telno = telno;
        }

        public SchregAddressDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
            _telno = "";
        }
    }

    private SchregAddressDat createSchregAddressDat(DB2UDB db2, String schregno)
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

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3,
                    telno
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
                + "    ADDR3 as addr3,"
                + "    TELNO as telno"
                + " from"
                + "    SCHREG_ADDRESS_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by ISSUEDATE DESC";
    }

    // ======================================================================
    /**
     * 証明書種別別発行データ。
     */
    private class CertifIssueDat {
        private final String _certifKindCd; // 証明書種類コード
        private final String _strDate;      // 開始日
        private final String _endDate;      // 終了日
        
        private CertifSchoolDat _certifschooldat;

        CertifIssueDat() {
            _certifKindCd = "";
            _certifschooldat = new CertifSchoolDat();
            _strDate = "";
            _endDate = "";
        }

        CertifIssueDat(
                final String certifKindCd,
                final String strDate,
                final String endDate
        ) {
            _certifKindCd = certifKindCd;
            _strDate = strDate;
            _endDate = endDate;
        }

        public void load(DB2UDB db2, String certifIndex) throws SQLException {
            _certifschooldat = createSchoolDat(db2, _certifKindCd);
        }
    }

    private CertifIssueDat createCertifIssueDat(DB2UDB db2, int i)
        throws SQLException {

        final String sql = sqlCertifIssueDat(_param._certif[i]);

        PreparedStatement ps = null;
        ResultSet rs = null;

        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.debug(sql);
        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String certifKindCd = rs.getString("certifKindCd");
            final String strDate = rs.getString("STR_DATE");
            final String endDate = rs.getString("END_DATE");

            final CertifIssueDat certifIssueDat = new CertifIssueDat(
                    certifKindCd,
                    strDate,
                    endDate
            );
            certifIssueDat.load(db2, certifKindCd);
            return certifIssueDat;
        }

        log.debug(">>>CERTIF_ISSUE_DAT に該当するものがありません。");

        return new CertifIssueDat();
    }

    private String sqlCertifIssueDat(String certifIndex) {
        return " select"
                + "    T1.CERTIF_KINDCD as certifKindCd,"
                + "    L1.REMARK4 as STR_DATE,"
                + "    L1.REMARK5 as END_DATE"
                + " from"
                + "    CERTIF_ISSUE_DAT T1 "
                + "    LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT L1 ON T1.YEAR = L1.YEAR "
                + "         AND T1.CERTIF_INDEX = L1.CERTIF_INDEX "
                + " where"
                + "    T1.YEAR = '" + _param._year + "' and"
                + "    T1.CERTIF_INDEX = '" + certifIndex + "'"
                ;
    }

    // ======================================================================
    /**
     * 証明書学校データ。
     */
    private class CertifSchoolDat {
        private final String _remark1;      // 学校所在地
        private final String _schoolName;       // 学校名

        CertifSchoolDat(
                final String remark1,
                final String schoolName
        ) {
            _remark1 = remark1;
            _schoolName = schoolName;
        }

        public CertifSchoolDat() {
            _remark1 = "";
            _schoolName = "";
        }
    }

    private CertifSchoolDat createSchoolDat(DB2UDB db2, String certifKind)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlCertifSchoolDat(certifKind));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String remark1 = nvlT(rs.getString("remark1"));
            final String schoolName = nvlT(rs.getString("schoolName"));

            final CertifSchoolDat certifSchoolDat = new CertifSchoolDat(
                    remark1,
                    schoolName
            );
            return certifSchoolDat;
        }                    
        return new CertifSchoolDat();
    }

    private String sqlCertifSchoolDat(String certifKind) {
        return " select"
                + "    REMARK1 as remark1,"
                + "    SCHOOL_NAME as schoolName"
                + " from"
                + "    CERTIF_SCHOOL_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "' and"
                + "    CERTIF_KINDCD = '" + certifKind + "'"
                ;
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    private class Belonging {
        private final String _schoolprefCd;
        private final String _schoolAddr1;
        private final String _schoolAddr2;
        private final String _schoolAddr3;
        private final String _schoolname1;

        Belonging() {
            _schoolprefCd = "";
            _schoolAddr1 =  "";
            _schoolAddr2 =  "";
            _schoolAddr3 =  "";
            _schoolname1 =  "";
        }

        Belonging(
                final String schoolprefCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String schoolAddr3,
                final String schoolname1
        ) {
            _schoolprefCd = schoolprefCd;
            _schoolAddr1 =  schoolAddr1;
            _schoolAddr2 =  schoolAddr2;
            _schoolAddr3 =  schoolAddr3;
            _schoolname1 =  schoolname1;
        }
    }

    public Belonging createBelongingDat(DB2UDB db2, String belongingDiv)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            if ((belongingDiv != null) && (belongingDiv.length() != 0)) {

                if (belongingDiv.length() >= 4) {
                    ps = db2.prepareStatement(sqlBelongingDat(belongingDiv.substring(0, 4)));
                } else {
                    ps = db2.prepareStatement(sqlBelongingDat(belongingDiv));
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolprefCd = rs.getString("schoolprefCd");
                    final String schoolAddr1 = rs.getString("schoolAddr1");
                    final String schoolAddr2 = rs.getString("schoolAddr2");
                    final String schoolAddr3 = rs.getString("schoolAddr3");
                    final String schoolname1 = rs.getString("schoolname1");
                    
                    final Belonging belonging = new Belonging(
                            schoolprefCd,
                            schoolAddr1,
                            schoolAddr2,
                            schoolAddr3,
                            schoolname1
                    );
                    return belonging;
                }
            }

            return new Belonging();
    }

    private String sqlBelongingDat(String belongingDiv) {
        return " select"
                + "    SCHOOLPREF_CD as schoolprefCd,"
                + "    SCHOOLADDR1 as schoolAddr1,"
                + "    SCHOOLADDR2 as schoolAddr2,"
                + "    SCHOOLADDR3 as schoolAddr3,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    BELONGING_MST"
                + " where"
                + "    BELONGING_DIV = '" + belongingDiv + "'"
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
    
} // KNJWA171

// eof

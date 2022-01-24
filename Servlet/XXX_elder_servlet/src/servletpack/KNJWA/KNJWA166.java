// kanji=漢字
/*
 * $Id: 5d4c53606b28961a848c2e273c4e14a8bbd04b81 $
 *
 * 作成日: 2007/11/02 11:33:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 生徒個人原簿
 * 
 * @author nakada
 * @version $Id: 5d4c53606b28961a848c2e273c4e14a8bbd04b81 $
 */
public class KNJWA166 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA166.class);

    private static final String FORM_FILE = "KNJWA166.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 所属(拠点) */
    private static final int BELONGING1_LENG = 26;
    /** ふりがな */
    private static final int NAME_KANA1_LENG = 40;
    /** 生徒氏名 */
    private static final int NAME1_LENG = 34;
    /** 家族氏名 */
    private static final int RELANAME1_LENG = 26;

    /*
     * 名称マスタキー（NAMECD1）
     */
    /** 性別 */
    private static final String NAME_MST_SEX = "Z002";
    /** 血液型 */
    private static final String BLOOD_TYPE = "????";    // TODO: 保留
    /** 血液RH型 */
    private static final String BLOOD_RH = "????";      // TODO: 保留
    /** 続柄 */
    private static final String RELATION_SHIP = "H201";
    /** 異動区分 */
    private static final String TRANSFER_CD = "A004";

    /* 
     * 件数ＭＡＸ 
     */
    /** 家族構成件数ＭＡＸ */
    private static final int SCHREG_RELA_MAX = 7;
    /** 異動件数ＭＡＸ */
    private static final int SCHREG_TRANSFER_MAX = 10;

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
                final String schregno = _param._categorySelected[i];
                log.debug(">>学籍番号=" + schregno);

                final List students = createStudents(db2, schregno);
                printMain(students, i);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List student, int i) throws SQLException {

        for (Iterator it = student.iterator(); it.hasNext();) {
            final Student sudent = (Student) it.next();

            printStudent(sudent, i);

            printHeader();

            _form._svf.VrEndPage();
            _hasData = true;

        }
    }

    private void printHeader() {
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date));
    }

    private void printStudent(Student student, int i) throws SQLException {

        /* 所属 */
        _form.printBelongingName(student);
        /* 写真 */
        _form.printPicture(student);    // TODO: 仕様保留
        /* かな氏名 */
        _form.printNameKana(student);
        /* 氏名漢字 */
        _form.printName(student);
        /* 志願者番号 */
        _form._svf.VrsOut("APPLICANTNO", student._applicantNo);
        /* 学籍番号 */
        _form._svf.VrsOut("SCHREGNO", _param._categorySelected[i]);
        /* 学科／専攻コース */
        _form.printCourseName(student);
        /* 性別 */
        _form._svf.VrsOut("SEX", _param._sexString(student._sex));
        /* 生年月日 */
        _form._svf.VrsOut("BIRTHDAY", getJDate(student._birthday));
        /* 入学日 */
        _form._svf.VrsOut("END_DATE", getJDate(student._entDate));
        /* 卒業予定日 */
        _form._svf.VrsOut("GRD_DATE", getJDate(student._grdScheduleDate));
        /* 血液型 */   // TODO: テーブル項目未登録
        /* 携帯電話番号 */
        _form._svf.VrsOut("MOBILE_PHONE_NO", student._mobilePhoneNo);
        /* 携帯Ｅメール */ // TODO:　テーブル項目未定義
        /* 出身中学校 */
        _form.printFinSchoolCd(student);
        /* 前籍高校名 */
        _form.printAnotherSchool(student);
        /* 電話番号 */
        _form._svf.VrsOut("TELNO", student._schregAddressDat._telno);
        /* Eメール */
        _form._svf.VrsOut("EMAIL", student._schregAddressDat._email);
        /* 郵便番号 */
        _form._svf.VrsOut("ZIP", student._schregAddressDat._zipcd);
        /* 住所１ */
        _form.printAddr1(student);
        /* 住所２ */
        _form.printAddr2(student);
        /* 生徒勤務先 */ // TODO:　テーブル項目未定義
        /* 生徒勤務先（電話番号） */ // TODO:　テーブル項目未定義
        /* 緊急連絡先 */
        _form.printEmergencyName(student);
        /* 緊急連絡先(電話番号) */
        _form._svf.VrsOut("EMERGENCYTELNO", student._emergencyTelno);
        /* 保護者かな氏名 */
        _form._svf.VrsOut("GUARD_KANA", student._guardianDat._guardKana);
        /* 保護者名 */
        _form._svf.VrsOut("GUARD_NAME", student._guardianDat._guardName);
        /* 保護者電話番号 */
        _form._svf.VrsOut("GUARD_TELNO", student._guardianDat._guardTelno);
        /* 保護者携帯電話番号 */ // TODO: テーブル項目未定義
        /* 保護者郵便番号 */
        _form._svf.VrsOut("GUARD_ZIPCD", student._guardianDat._guardZipcd);
        /* 保護者住所１ */
        _form.printGuardAddr1(student);
        /* 保護者住所２ */
        _form.printGuardAddr2(student);
        /* 進路希望（第一志望） */ // TODO: 空白
        /* 進路希望（第一志望） */ // TODO: 空白
        /* 視力・矯正・右 */
        _form._svf.VrsOut("R_BAREVISION", student._medexam_det_dat._rBarevision);
        /* 視力・裸眼・右 */
        _form._svf.VrsOut("R_VISION", student._medexam_det_dat._rVision);
        /* 視力・矯正・左 */
        _form._svf.VrsOut("L_BAREVISION", student._medexam_det_dat._lBarevision);
        /* 視力・裸眼・左 */
        _form._svf.VrsOut("L_VISION", student._medexam_det_dat._lVision);
        /* ＜本人以外の家族構成＞ */
        _form.printRelaDat(student);
        /* ＜在学中の異動または特記事項＞ */
        _form.printTransferDat(student);
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

    private static  int getAge(String date, String birthday) {
        final Calendar nowCal;
        try {
            nowCal = KNJServletUtils.parseDate(date);
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

    private String sqlName(String nameCd1, String nameCd2) {
        return " select"
                + "    NAME1 as name"
                + " from"
                + "    NAME_MST"
                + " where"
                + "    nameCd1 = '" + nameCd1 + "' and"
                + "    nameCd2 = '" + nameCd2 + "'";
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

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _grade;
        private final String[] _categorySelected;   // 学籍番号
        private final String _date;                 // 作成日
        private final String _docBase;              // ドキュメントベースパス

        private Map _sexMap;
        private Map _bloodTypeMap;      // 血液型
        private Map _bloodRhMap;       // 血液RH型
        private Map _relationShipMap;   // 続柄
        private Map _transferCdMap;     // 異動区分
        private Map _prefMap;           // 都道府県

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String grade,
                final String[] categorySelected,
                final String date,
                final String docBase
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _grade = grade;
            _categorySelected = categorySelected;
            _date = date;
            _docBase = docBase;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex) != null ?
                    (String) _sexMap.get(sex) : "";
        }

        public String _bloodTypeString(String bloodType) {
            return (String) _bloodTypeMap.get(bloodType) != null ?
                    (String) _bloodTypeMap.get(bloodType) : "";
        }

        public String _bloodRhMapString(String bloodRh) {
            return (String) _bloodRhMap.get(bloodRh) != null ?
                    (String) _bloodRhMap.get(bloodRh) : "";
        }

        public String _relationShipString(String relationShip) {
            return (String) _relationShipMap.get(relationShip) != null ?
                    (String) _relationShipMap.get(relationShip) : "";
        }

        public String _transferCdString(String transferCd) {
            return (String) _transferCdMap.get(transferCd) != null ?
                    (String) _transferCdMap.get(transferCd) : "";
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ?
                    (String) _prefMap.get(pref) : "";
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            log.debug("性別の一覧=" + _sexMap);

            _bloodTypeMap = getNameMst(BLOOD_TYPE);
            log.debug("血液型の一覧=" + _bloodTypeMap);

            _bloodRhMap = getNameMst(BLOOD_RH);
            log.debug("血液RH型の一覧=" + _bloodRhMap);

            _relationShipMap = getNameMst(RELATION_SHIP);
            log.debug("続柄の一覧=" + _relationShipMap);

            _transferCdMap = getNameMst(TRANSFER_CD);
            log.debug("異動区分の一覧=" + _transferCdMap);

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

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
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

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAMECD2 as code,"
                    + "    NAME1 as name"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PROGRAMID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String grade = request.getParameter("GRADE");
        final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
        final String docBase = request.getParameter("DOCBASE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                grade,
                categorySelected,
                date,
                docBase
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

        public void printPicture(Student student) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            String folder = null;
            String extension = null;

            returnval = getinfo.Control(db2);
            folder = returnval.val4;            //格納フォルダ
            extension = returnval.val5;         //拡張子

            String image_pass = _param._docBase + "/" + folder + "/";   //イメージパス
            String photo_check = image_pass + "P" + student._schregNo + "." + extension;
            File f1 = new File(photo_check);

            if (f1.exists()) {
//                _form._svf.VrsOut("PHOTO", photo_check );   // TODO: 項目名保留
            }
        }

        public void printTransferDat(Student student) {
            int i = 0;

            for (Iterator it = student._schregTransferDats.iterator(); it.hasNext();) {
                i++;
                
                final SchregTransferDat schregTransferDat = (SchregTransferDat) it.next();
                /* 区分 */
                _form._svf.VrsOutn("DIVISION", i, _param._transferCdString(schregTransferDat._transferCd));
                /* 異動年月日 */
                String str = getJDate(schregTransferDat._transferSDate) + "〜"
                    + getJDate(schregTransferDat._transferEDate);
                _form._svf.VrsOutn("RESHUFFLE_DATE",i , str);

                if (i >= SCHREG_TRANSFER_MAX) {
                    return;
                }
            }
        }

        public void printRelaDat(Student student) {
            int i = 0;

            for (Iterator it = student._schregRelaDats.iterator(); it.hasNext();) {
                i++;
                
                final SchregRelaDat schregRelaDat = (SchregRelaDat) it.next();

                /* 氏名 */
                _form.printRelaName(i, schregRelaDat);
                /* 続柄 */
                _form._svf.VrsOutn("RELATIONSHIP",i , _param._relationShipString(schregRelaDat._relationShip));
                /* 年齢 */
                _form._svf.VrsOutn("AGE",i , Integer.toString(getAge(_param._date, schregRelaDat._relaBirthday)));
                /* 所属 */
                _form._svf.VrsOutn("REMARK",i , schregRelaDat._remark);

                if (i >= SCHREG_RELA_MAX) {
                    return;
                }
            }
        }

        public void printAddr1(Student student) {
            String name = _param._prefMapString(student._schregAddressDat._prefCd)
                + (student._schregAddressDat._addr1 != null ?
                        student._schregAddressDat._addr1 : "")
                + (student._schregAddressDat._addr2 != null ?
                        student._schregAddressDat._addr2 : "");
            _form._svf.VrsOut("ADDRESS1_1", name);
        }

        public void printAddr2(Student student) {
            String name = student._schregAddressDat._addr3 != null ?
                    student._schregAddressDat._addr3 : "";
            _form._svf.VrsOut("ADDRESS1_2", name);
        }

        public void printGuardAddr1(Student student) {
            String name = _param._prefMapString(student._guardianDat._guardPrefCd)
                + (student._guardianDat._guardAddr1 != null ?
                        student._guardianDat._guardAddr1 : "")
                + (student._guardianDat._guardAddr2 != null ?
                        student._guardianDat._guardAddr2 : "");
            _form._svf.VrsOut("ADDRESS2_1", name);
        }

        public void printGuardAddr2(Student student) {
            String name = student._guardianDat._guardAddr3 != null ?
                    student._guardianDat._guardAddr3 : "";
            _form._svf.VrsOut("ADDRESS2_2", name);
        }

        public void printEmergencyName(Student student) {
            String name = student._emergencyCall;
            if (name != null) {
                final String label;
                label = "EMERGENCYCALL";
                _form._svf.VrsOut(label, name);
            }
        }

        public void printAnotherSchool(Student student) {
            String name = student._anotherSchoolHistDat._anoName;

            if (name != null) {
                final String label;
                label = "ANOTHER_SCHOOL";
                _form._svf.VrsOut(label, name);
            }
        }

        public void printFinSchoolCd(Student student) throws SQLException {
            String name = getFJHSchName(student._finSchoolCd);

            if (name != null) {
                final String label;
                label = "FIN_SCHOOL";
                _form._svf.VrsOut(label, name);
            }
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
                    + "    NAME as name"
                    + " from"
                    + "    FIN_JUNIOR_HIGHSCHOOL_MST"
                    + " where"
                    + "    SCHOOL_CD = '" + schoolCd + "'";
        }

        public void printName(Student student) {
            String name = student._name;
            
            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printNameKana(Student student) {
            String name = student._nameKana;
            
            if (name != null) {
                final String label;
                if (name.length() <= NAME_KANA1_LENG) {
                    label = "NAME_KANA1";
                } else {
                    label = "NAME_KANA2";
                }
                _form._svf.VrsOut(label, name);
            }
        }


        public void printRelaName(int i, SchregRelaDat schregRelaDat) {
            String name = schregRelaDat._relaName;

            if (name != null) {
                final String label;
                if (name.length() <= RELANAME1_LENG) {
                    label = "RELANAME1";
                } else {
                    label = "RELANAME2";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printBelongingName(Student student) {
            String name = student._belonging._schoolName1;

            if (name != null) {
                final String label;
                if (name.length() <= BELONGING1_LENG) {
                    label = "BELONGING_NAME1";
                } else {
                    label = "BELONGING_NAME2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        public void printCourseName(Student student) {
            String name = getCourseName(student);

            if (name != null) {
                final String label;
                label = "COURSE";
                _form._svf.VrsOut(label, name);
            }
        }

        private String getCourseName(Student student) {
            String name;
            String name2 = student._major._majorName;
            String name3 = student._courseCodeMst._courseCodeName;
            if (name2 == null) {
                name = name3;
            } else {
                name = name2 +  name3;
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
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _nameKana;         // 氏名かな
        private final String _birthday;         // 生年月日
        private final String _sex;              // 性別
        private final String _bloodType;        // 血液型
        private final String _bloodRh;          // 血液rh型
        private final String _applicantNo;      // 志願者番号
        private final String _mobilePhoneNo;    // 携帯電話番号
        private final String _finSchoolCd;      // 出身中学校コード
        private final String _entDate;          // 入学日付
        private final String _grdScheduleDate;  // 卒業予定日付
        private final String _emergencyCall;    // 急用連絡先
        private final String _emergencyTelno;   // 急用電話番号
        // 携帯ｅメール //TODO:　テーブル項目未定義
        
        private SchregRegdDat _schregRegdDat;                // 生徒。学籍在籍データ
        private Belonging _belonging;                       // 所属データ
        private Major _major;                               // 学科データ
        private CourseCodeMst _courseCodeMst;               // コースコード　データ
        private SchregAddressDat _schregAddressDat;         // 生徒。学籍住所データ
        private AnotherSchoolHistDat _anotherSchoolHistDat; // 生徒。前籍校履歴データ
        private List _schregTransferDats;                   // 生徒。学籍異動データ
        private GuardianDat _guardianDat;                   // 生徒。学籍保護者データ
        private List _schregRelaDats;                        // 生徒。学籍親族データ
        private MedexamDetDat _medexam_det_dat;             // 生徒。健康診断詳細データ

        Student(final String schregNo,
                final String name,
                final String nameKana,
                final String birthday,
                final String sex,
                final String bloodType,
                final String bloodRh,
                final String applicantNo,
                final String mobilePhoneNo,
                final String finSchoolCd,
                final String entDate,
                final String grdScheduleDate,
                final String emergencyCall,
                final String emergencyTelno
        ) {
            _schregNo = schregNo;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _sex = sex;
            _bloodType = bloodType;
            _bloodRh = bloodRh;
            _applicantNo = applicantNo;
            _mobilePhoneNo = mobilePhoneNo;
            _finSchoolCd = finSchoolCd;
            _entDate = entDate;
            _grdScheduleDate = grdScheduleDate;
            _emergencyCall = emergencyCall;
            _emergencyTelno = emergencyTelno;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdDat = createSourseCodeDat(db2, _param._year, _param._semester, _schregNo);
            _belonging = createBelongingDat(db2, _schregRegdDat._grade);
            _major = createMajorDat(db2, _schregRegdDat._courseCd, _schregRegdDat._majorCd);
            _courseCodeMst = createCourseCodeDat(db2, _schregRegdDat._courseCode);
            _schregAddressDat = createStudentSchregAddressDat(db2, _schregNo);
            _anotherSchoolHistDat = createStudentAnotherSchoolHistDat(db2, _applicantNo);
            _schregTransferDats = createSchregTransferDats(db2, _schregNo);
            _guardianDat = createGuardianDat(db2, _schregNo);
            _schregRelaDats = createSchregRelaDats(db2, _schregNo);
            _medexam_det_dat = createMedexamDetDat(db2, _param._year, _schregNo);
        }

        public String toString() {
            return _applicantNo + ":" + _name;
        }
    }

    private List createStudents(final DB2UDB db2, String schregno)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String nameKana = rs.getString("nameKana");
                final String birthday = rs.getString("birthday");
                final String sex = rs.getString("sex");
                final String bloodType = rs.getString("bloodType");
                final String bloodRh = rs.getString("bloodRh");
                final String applicantNo = rs.getString("applicantNo");
                final String mobilePhoneNo = rs.getString("mobilePhoneNo");
                final String finSchoolCd = rs.getString("finSchoolCd");
                final String entDate = rs.getString("entDate");
                final String grdScheduleDate = rs.getString("grdScheduleDate");
                final String emergencyCall = rs.getString("emergencyCall");
                final String emergencyTelno = rs.getString("emergencyTelno");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        nameKana,
                        birthday,
                        sex,
                        bloodType,
                        bloodRh,
                        applicantNo,
                        mobilePhoneNo,
                        finSchoolCd,
                        entDate,
                        grdScheduleDate,
                        emergencyCall,
                        emergencyTelno
                );

                studentDat.load(db2);
                rtn.add(studentDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    NAME_KANA as nameKana,"
                + "    BIRTHDAY as birthday,"
                + "    SEX as sex,"
                + "    BLOODTYPE as bloodType,"
                + "    BLOOD_RH as bloodRh,"
                + "    APPLICANTNO as applicantNo,"
                + "    MOBILE_PHONE_NO as mobilePhoneNo,"
                + "    FINSCHOOLCD as finSchoolCd,"
                + "    ENT_DATE as entDate,"
                + "    GRD_SCHEDULE_DATE as grdScheduleDate,"
                + "    EMERGENCYCALL as emergencyCall,"
                + "    EMERGENCYTELNO as emergencyTelno"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
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

    /**
     * 前籍校履歴データ取得
     * @param db2
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

    private String sqlAnotherSchoolHistDat(String applicantNo) {
        return " select"
                + "    T2.NAME as anoName"
                + " from"
                + "    ANOTHER_SCHOOL_HIST_DAT T1 left join FIN_HIGH_SCHOOL_MST T2 on T1.FORMER_REG_SCHOOLCD = T2.SCHOOL_CD"
                + " where"
                + "    T1.APPLICANTNO = '" + applicantNo + "'"
                + " order by T1.REGD_S_DATE DESC";
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    private class Belonging {
        private final String _schoolName1;

        Belonging(
                final String schoolName1
        ) {
            _schoolName1 = schoolName1;
        }

        Belonging() {
            _schoolName1 = "";
        }
    }

    public Belonging createBelongingDat(DB2UDB db2, String belongingDiv)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlBelongingDat(belongingDiv));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                
                final Belonging belonging = new Belonging(
                        name
                );
                return belonging;
            }
            return null;
    }

    private String sqlBelongingDat(String belongingDiv) {
        return " select"
                + "    SCHOOLNAME1 as name"
                + " from"
                + "    BELONGING_MST"
                + " where"
                + "    BELONGING_DIV = '" + belongingDiv + "'"
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
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _grade;         // 課程コード
        private final String _courseCd;         // 課程コード
        private final String _majorCd;          // 学科コード
        private final String _courseCode;       // コースコード
        // 生徒勤務先
        // 生徒勤務j先（電話番号）

        SchregRegdDat(
                final String grade,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String courseCd = rs.getString("courseCd");
            final String majorCd = rs.getString("majorCd");
            final String courseCode = rs.getString("courseCode");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    grade,
                    courseCd,
                    majorCd,
                    courseCode
            );
            return schregRegdDat;
        }

        log.debug(">>>SCHREG_REGD_DAT に該当するものがありません。");
        throw new Exception();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo) {
        return " select"
                + "    GRADE as grade,"
                + "    COURSECD as courseCd,"
                + "    MAJORCD as majorCd,"
                + "    COURSECODE as courseCode"
                + " from"
                + "    SCHREG_REGD_DAT"
                + " where"
                + "    SCHREGNO = '" + schregNo + "' and"
                + "    YEAR = '" + year + "' and"
                + "    SEMESTER = '" + semester + "'"
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
        private final String _email; // e-mail

        SchregAddressDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String telno,
                final String email
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _telno = telno;
            _email = email;
        }

        public SchregAddressDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
            _telno = "";
            _email = "";
        }
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

            final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3,
                    telno,
                    email
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
                + "    TELNO as telno,"
                + "    EMAIL as email"
                + " from"
                + "    SCHREG_ADDRESS_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by ISSUEDATE DESC";
    }

    // ======================================================================
    /**
     * 生徒。学籍異動データ。
     */
    private class SchregTransferDat {
        private final String _transferCd;    // 異動区分
        private final String _transferSDate; // 異動期間開始日付
        private final String _transferEDate; // 異動期間終了日付

        SchregTransferDat(
                final String transferCd,
                final String transferSDate,
                final String transferEDate
        ) {
            _transferCd = transferCd;
            _transferSDate = transferSDate;
            _transferEDate = transferEDate;
        }

        public SchregTransferDat() {
            _transferCd = "";
            _transferSDate = "";
            _transferEDate = "";
        }
    }

    /**
     * 学籍異動データ取得
     * @param db2
     * @param schregno
     */
    private List createSchregTransferDats(DB2UDB db2, String schregno)
        throws SQLException {

        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlSchregTransferDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String transferCd = rs.getString("transferCd");
            final String transferSDate = rs.getString("transferSDate");
            final String transferEDate = rs.getString("transferEDate");

            final SchregTransferDat schregTransferDat = new SchregTransferDat(
                    transferCd,
                    transferSDate,
                    transferEDate
            );

            rtn.add(schregTransferDat);
        }                    
        return rtn;
    }

    private String sqlSchregTransferDat(String schregno) {
        return " select"
                + "    TRANSFERCD as transferCd,"
                + "    TRANSFER_SDATE as transferSDate,"
                + "    TRANSFER_EDATE as transferEDate"
                + " from"
                + "    SCHREG_TRANSFER_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by TRANSFER_SDATE";
    }

    // ======================================================================
    /**
     * 生徒。学籍保護者データ。
     */
    private class GuardianDat {
        private final String _guardName;    // 保護者氏名
        private final String _guardKana; // 保護者氏名かな
        private final String _guardZipcd; // 郵便番号
        private final String _guardPrefCd; // 都道府県コード
        private final String _guardAddr1; // 住所1
        private final String _guardAddr2; // 住所2
        private final String _guardAddr3; // 住所3
        private final String _guardTelno; // 電話番号
        // 保護者携帯電話番号 // TODO: テーブル項目未定義

        GuardianDat(
                final String guardName,
                final String guardKana,
                final String guardZipcd,
                final String guardPrefCd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardAddr3,
                final String guardTelno
        ) {
            _guardName = guardName;
            _guardKana = guardKana;
            _guardZipcd = guardZipcd;
            _guardPrefCd = guardPrefCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardAddr3 = guardAddr3;
            _guardTelno = guardTelno;
        }

        public GuardianDat() {
            _guardName = "";
            _guardKana = "";
            _guardZipcd = "";
            _guardPrefCd = "";
            _guardAddr1 = "";
            _guardAddr2 = "";
            _guardAddr3 = "";
            _guardTelno = "";
        }
    }

    /**
     * 学籍保護者データ取得
     * @param db2
     * @param schregno
     */
    private GuardianDat createGuardianDat(DB2UDB db2, String schregno)
        throws SQLException {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlGuardianDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String guardName = rs.getString("guardName");
            final String guardKana = rs.getString("guardKana");
            final String guardZipcd = rs.getString("guardZipcd");
            final String guardPrefCd = rs.getString("guardPrefCd");
            final String guardAddr1 = rs.getString("guardAddr1");
            final String guardAddr2 = rs.getString("guardAddr2");
            final String guardAddr3 = rs.getString("guardAddr3");
            final String guardTelno = rs.getString("guardTelno");

            final GuardianDat guardianDat = new GuardianDat(
                    guardName,
                    guardKana,
                    guardZipcd,
                    guardPrefCd,
                    guardAddr1,
                    guardAddr2,
                    guardAddr3,
                    guardTelno
            );
            return guardianDat;
        }                    
        return new GuardianDat();
    }

    private String sqlGuardianDat(String schregno) {
        return " select"
                + "    GUARD_NAME as guardName,"
                + "    GUARD_KANA as guardKana,"
                + "    GUARD_ZIPCD as guardZipcd,"
                + "    GUARD_PREF_CD as guardPrefCd,"
                + "    GUARD_ADDR1 as guardAddr1,"
                + "    GUARD_ADDR2 as guardAddr2,"
                + "    GUARD_ADDR3 as guardAddr3,"
                + "    GUARD_TELNO as guardTelno"
                + " from"
                + "    GUARDIAN_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。学籍親族データ。
     */
    private class SchregRelaDat {
        private final String _relaName;         // 親族氏名
        private final String _relaBirthday;     // 親族生年月日
        private final String _relationShip;     // 親族続柄
        private final String _remark;           // 備考

        SchregRelaDat(
                final String relaName,
                final String relaBirthday,
                final String relationShip,
                final String remark
        ) {
            _relaName = relaName;
            _relaBirthday = relaBirthday;
            _relationShip = relationShip;
            _remark = remark;
        }

        public SchregRelaDat() {
            _relaName = "";
            _relaBirthday = "";
            _relationShip = "";
            _remark = "";
        }
    }

    /**
     * 学籍親族データ取得
     * @param db2
     * @param schregno
     */
    private List createSchregRelaDats(DB2UDB db2, String schregno)
        throws SQLException {

        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlSchregRelaDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String relaName = rs.getString("relaName");
            final String relaBirthday = rs.getString("relaBirthday");
            final String relationShip = rs.getString("relationShip");
            final String remark = rs.getString("remark");

            final SchregRelaDat schregRelaDat = new SchregRelaDat(
                    relaName,
                    relaBirthday,
                    relationShip,
                    remark
            );
            rtn.add(schregRelaDat);
        }                    
        return rtn;
    }

    private String sqlSchregRelaDat(String schregno) {
        return " select"
                + "    RELANAME as relaName,"
                + "    RELABIRTHDAY as relaBirthday,"
                + "    RELATIONSHIP as relationShip,"
                + "    REMARK as remark"
                + " from"
                + "    SCHREG_RELA_DAT"
                + " where"
                + "    SCHREGNO = '" + schregno + "'"
                + " order by RELANO"
                ;
    }

    // ======================================================================
    /**
     * 生徒。健康診断詳細データ
     */
    private class MedexamDetDat {
        private final String _rBarevision;
        private final String _lBarevision;
        private final String _rVision;
        private final String _lVision;

        MedexamDetDat(
                final String rBarevision,
                final String lBarevision,
                final String rVision,
                final String lVision
        ) {
            _rBarevision = rBarevision;
            _lBarevision = lBarevision;
            _rVision = rVision;
            _lVision = lVision;
        }

        public MedexamDetDat() {
            _rBarevision = "";
            _lBarevision = "";
            _rVision = "";
            _lVision = "";
        }
    }

    /**
     * 健康診断詳細データ取得
     * @param db2
     * @param year
     * @param schregno
     */
    private MedexamDetDat createMedexamDetDat(DB2UDB db2, String year, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlMedexamDetDat(year, schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String rBarevision = rs.getString("rBarevision");
            final String lBarevision = rs.getString("lBarevision");
            final String rVision = rs.getString("rVision");
            final String lVision = rs.getString("lVision");

            final MedexamDetDat medexamDetDat = new MedexamDetDat(
                    rBarevision,
                    lBarevision,
                    rVision,
                    lVision
            );
            return medexamDetDat;
        }                    
        return new MedexamDetDat();
    }

    private String sqlMedexamDetDat(String year, String schregno) {
        return " select"
                + "    R_BAREVISION as rBarevision,"
                + "    L_BAREVISION as lBarevision,"
                + "    R_VISION as rVision,"
                + "    L_VISION as lVision"
                + " from"
                + "    MEDEXAM_DET_DAT"
                + " where"
                + "    YEAR = '" + year + "' and"
                + "    SCHREGNO = '" + schregno + "'"
                ;
    }
} // KNJWA166

// eof

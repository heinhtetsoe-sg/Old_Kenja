// kanji=漢字
/*
 * $Id: cc7a372a725308c988973980d4b19e98e0755111 $
 *
 * 作成日: 2018/10/24 10:00:00 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 通学証明書
 * @author nakada
 * @version $Id: cc7a372a725308c988973980d4b19e98e0755111 $
 */
public class KNJG060A {
    /* pkg */static final Log log = LogFactory.getLog(KNJG060A.class);

    private static final String SCHOOL_KIND_P = "P";
    private static final String SCHOOL_KIND_J = "J";
    private static final String SCHOOL_KIND_H = "H";

    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 70447 $ $Date: 2019-10-30 17:05:55 +0900 (水, 30 10 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        response.setContentType("application/pdf");
        _svf = new Vrw32alp();
        if (_svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        _svf.VrSetSpoolFileStream(response.getOutputStream());

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch (final Exception ex) {
                log.error("db open error!", ex);
                return;
            }
            _param = new Param(db2, request);

            final String FORM_FILE = "KNJG060A_1.frm";

            _hasData = false;
            for (int i = 0; i < _param._certif.length; i++) {
                log.debug(">> 証明書連番 =" + _param._certif[i]);

                _svf.VrSetForm(FORM_FILE, 1);

                final Student student = Student.createStudent(db2, _param, _param._certif[i]);
                printMain(db2, student);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final DB2UDB db2, final Student student) {

        String schoolType = "";
        if (SCHOOL_KIND_P.equals(student._schregRegdDat._schoolKind)) {
            schoolType = "小学校";
        } else if (SCHOOL_KIND_J.equals(student._schregRegdDat._schoolKind)) {
            schoolType = "中学校";
        } else if (SCHOOL_KIND_H.equals(student._schregRegdDat._schoolKind)) {
            schoolType = "高等学校";
        }
        _svf.VrsOut("SCHOOL_TYPE", schoolType);
        _svf.VrsOut("SCHOOL_DIV", student._schregRegdDat._courseName);
        //SCHOOL_KIND
        _svf.VrsOut("SCHOOL_KIND", schoolType);
        //DIV
        _svf.VrsOut("DIV", student._schregRegdDat._courseName);

        // 氏名
        if (student._name != null) {
            _svf.VrsOut(KNJ_EditEdit.getMS932ByteLength(student._name) <= 25 ? "NAME1" : "NAME2", student._name);
            //KANA1/2
            final int kanalen = KNJ_EditEdit.getMS932ByteLength(student._kana);
            _svf.VrsOut(kanalen > 26 ? "KANA2" : "KANA1", student._kana);
            //NAME2_1/2
            final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
            _svf.VrsOut(namelen > 16 ? "NAME2_2" : "NAME2_1", student._name);
        }

        // 年齢
        _svf.VrsOut("AGE", Integer.toString(getAge(_param._loginDate, student._birthday)));

        // 性別
        _svf.VrsOut("SEX", student._sex);

        // 住所
        printAddr(student);
        // 電話番号
        _svf.VrsOut("TELNO", student._schregAddressDat._telno);

        _svf.VrsOut("SCHOOL_KIND2", student._schregRegdDat._schoolKind);
        // 学年
        final String gradeCd = student._schregRegdDat._grade;
        if (gradeCd != "") {
        	_svf.VrsOut("ANNUAL", Integer.toString(Integer.parseInt(gradeCd)));
        	_svf.VrsOut("GRADE", "第" + Integer.toString(Integer.parseInt(gradeCd)) + "学年");
        }

        _svf.VrsOut("SCHREG_NO", student._disp_schregno);
        if (null != student._birthday) {
        	final String s = KNJ_EditDate.h_format_SeirekiJP(student._birthday);
        	if (null != s) {
        		_svf.VrsOut("BIRTHDAY2", s + "生");
        	}
        }
        //BIRTHDAY
        try {
            final Calendar cal = KNJServletUtils.parseDate(student._birthday);
            final String year = String.valueOf(cal.get(Calendar.YEAR));
            final String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            final String day = String.valueOf(cal.get(Calendar.DATE));
            _svf.VrsOut("BIRTHDAY", year + "/" + month + "/" + day);
        } catch (ParseException e) {
            //return 0;
        }
        //START_DATE ※月日は固定
        _svf.VrsOut("START_DATE", _param._year + "/4/1");

        //LIMIT_DATE ※月日は固定
        _svf.VrsOut("LIMIT_DATE", String.valueOf((Integer.parseInt(_param._year) + 1)) + "/3/31");

        if (!StringUtils.isBlank(_param._schoolStampPath)) {
        	//印鑑
        	_svf.VrsOut("SCHOOLSTAMP", _param._schoolStampPath);
        }
        if (student._schregEnvirDat != null) {
        	final List<String> rosenList = new ArrayList<String>(); // 路線名リスト
        	final List<List<String>> rosenEkiList = new ArrayList<List<String>>(); // 路線名ごとの駅リスト
            for (int ii = 7;ii >= 1;ii--) {
            	final String rosen = StringUtils.defaultString(student._schregEnvirDat.getRosen(_param, ii));
            	final String gesya = student._schregEnvirDat.getGesya(_param, ii);
            	final String josya = student._schregEnvirDat.getJosya(_param, ii);

            	if (student._schregEnvirDat.chkFlg(_param, ii) && (!StringUtils.isBlank(josya) || !StringUtils.isBlank(gesya))) {
            		if (rosenList.isEmpty() || !rosenList.get(rosenList.size() - 1).equals(rosen)) {
            			rosenList.add(rosen);
            			rosenEkiList.add(new ArrayList<String>());
            		}
            		final List<String> ekiList = rosenEkiList.get(rosenEkiList.size() - 1);
            		if (!StringUtils.isBlank(gesya) && !ekiList.contains(gesya)) {
            			ekiList.add(gesya);
            		}
            		if (!StringUtils.isBlank(josya) && !ekiList.contains(josya)) {
            			ekiList.add(josya);
            		}
            	}
            }
            for (int i = 0; i < rosenList.size(); i++) {
            	final int line = i + 1;
            	final String rosen = rosenList.get(i);
            	final List<String> ekiList = rosenEkiList.get(i);
            	final String josya = ekiList.size() > 0 ? ekiList.get(0) : null; // 最初の駅
            	final String gesya = ekiList.size() > 1 ? ekiList.get(ekiList.size() - 1) : null; // 最後の駅
            	final String keiyu = ekiList.size() > 2 ? ekiList.get(1) : null; // 乗車駅と下車駅以外にあれば2番目の駅

                // 路線
                final int rosenLen = KNJ_EditEdit.getMS932ByteLength(rosen);
                _svf.VrsOutn("TRANSPORT" + (rosenLen > 12 ? "_2" : ""), line, rosen);
                // 乗車
                final int josyaLen = KNJ_EditEdit.getMS932ByteLength(josya);
                _svf.VrsOutn("START_STATION" + (josyaLen > 12 ? "_2" : ""), line, josya);
                // 下車
                final int gesyaLen = KNJ_EditEdit.getMS932ByteLength(gesya);
                _svf.VrsOutn("END_STATION" + (gesyaLen > 12 ? "_2" : ""), line, gesya);
                // 経由
                final int keiyuLen = KNJ_EditEdit.getMS932ByteLength(keiyu);
                _svf.VrsOutn("BY_WAY_STATION" + (keiyuLen > 12 ? "_2" : ""), line, keiyu);

                _svf.VrsOutn("TRANSPORT2", line, rosen);
                _svf.VrsOutn("START_STATION2" + (josyaLen > 12 ? "_2" : ""), line, josya);
                _svf.VrsOutn("END_STATION2" + (gesyaLen > 12 ? "_2" : ""), line, gesya);
                _svf.VrsOutn("BY_WAY_STATION2", line, keiyu);
            }
        }

         //通学証明書用
        //SCHOOLNAME2
        _svf.VrsOut("SCHOOLNAME2", student._certifSchoolDat._schoolName);
        //SCHOOLADDR1/2
        final int schaddrlen = KNJ_EditEdit.getMS932ByteLength(student._certifSchoolDat._remark1);
        _svf.VrsOut(schaddrlen > 26 ? "SCHOOLADDR2" : "SCHOOLADDR1", student._certifSchoolDat._remark1);
        //SCHOOLTELNO
        _svf.VrsOut("SCHOOLTELNO", student._certifSchoolDat._remark5);
        //STAFFNAME2
        _svf.VrsOut("STAFFNAME2", student._certifSchoolDat._principalName);

        _svf.VrEndPage();
        _hasData = true;
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _certif;            // 通学証明書番号
        private final String _type;                 // 証明書種別コード
        private final String _exeYear;              // 指示画面検索年度 (賢者では指示画面検索は無いのでyearを使用する)
        private final String _useAddrField2;
        private List _findnameflg;
        private String _findnameinstr;
        private final String _documentRoot;
        private String _imagePath;
        private final String _schoolStampPath;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            final String year = request.getParameter("YEAR");
            //final String exeYear = request.getParameter("EXE_YEAR");

            _year = year;
            _semester = request.getParameter("SEMESTER");
            _programId = request.getParameter("PRGID");
            _dbName = request.getParameter("DBNAME");
            _loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
            _certif = request.getParameterValues("TUGAKU");
            _type = request.getParameter("TYPE");
            _exeYear = year;
            _useAddrField2 = request.getParameter("useAddrField2");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_JH.bmp");

            _findnameflg = new ArrayList();
            //_findnameflg = getNameMstH100(db2);  //登録分全てを出力
            _findnameflg.add("1");//knjh010a_disasterSubForm2.php内部で定義された値(通学手段)
            _findnameflg.add("2");
            _findnameflg.add("4");

            String delm = "";
            _findnameinstr = "";
            for (int ii = 0;ii < _findnameflg.size();ii++) {
            	_findnameinstr += delm + "'" + _findnameflg.get(ii) + "'";
            	delm = ", ";
            }
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }
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

             return Integer.parseInt(""+(nowYear-year - ((month > nowMonth || (month == nowMonth && day > nowDay)) ? 1 : 0)));
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

    private void printAddr(Student student) {
        /** 住所 */
        final int ADD1_LENG = 50;
        final int ADD2_LENG = 40;

        final String addr1 = StringUtils.defaultString(student._schregAddressDat._addr1);
        final String addr2 = StringUtils.defaultString(student._schregAddressDat._addr2);
        final int len1 = KNJ_EditEdit.getMS932ByteLength(addr1);
        final int len2 = KNJ_EditEdit.getMS932ByteLength(addr2);

        final String addr = addr1 + addr2;

        if (len1 + len2 < ADD1_LENG) {
            // 住所1＋住所2＝が25文字以内なら、中央表示
            _svf.VrsOut("ADDRESS1_1", addr);
        } else if (addr2 == null) {
            // 住所2がnullのとき
            if (len1 <= ADD1_LENG) {
                // 住所1が25文字以内で中央表示
                _svf.VrsOut("ADDRESS1_1", addr1);
            } else {
                // 住所1が25文字超えるとき、住所1＋住所2で、上下で表示
            	String a1 = null;
            	String a2 = null;
            	final String[] token = KNJ_EditEdit.get_token(addr, ADD1_LENG, 2);
            	if (null != token) {
            		if (token.length > 0) {
            			a1 = token[0];
            		}
            		if (token.length > 1) {
            			a2 = token[1];
            		}
            	}

                _svf.VrsOut("ADDRESS1_2", a1);
                _svf.VrsOut("ADDRESS1_3", a2);
            }
        } else {
            // 上記以外のときは、 住所1は上段、住所2は下段（リンクではない）
            if ("1".equals(_param._useAddrField2) &&
                    (len1 > 50 || len2 > 50)
                    ) {
                _svf.VrsOut("ADDRESS3_1", addr1);
                _svf.VrsOut("ADDRESS3_2", addr2);
            } else {
                _svf.VrsOut("ADDRESS1_2", addr1);
                _svf.VrsOut("ADDRESS1_3", addr2);
            }
        }
        //通学証明書
        if (len1 + len2 > ADD2_LENG) {
            _svf.VrsOut("ADDR2", addr1 + addr2);
        } else {
            _svf.VrsOut("ADDR1", addr1 + addr2);
        }
        _svf.VrsOut("ADDRESS2", "");
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private static class Student {
        private final String _schregno;         // 学籍番号
        private final String _disp_schregno;    // 学籍番号(表示用)
        private final String _kana;             // 氏名かな
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日
        private final String _sex;              // 性別

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private SchregAddressDat _schregAddressDat; // 生徒。学籍住所データ
        private CertifSchoolDat _certifSchoolDat;   // 証明書学校データ
        private CertifIssueDat _certifIssueDat;
        private EnvirDat _schregEnvirDat;   // 証明書学校データ

        Student(final String schregno,
        		final String disp_schregno,
                final String kana,
                final String name,
                final String birthday,
                final String sex
        ) {
            _schregno = schregno;
            _disp_schregno = disp_schregno;
            _kana = kana;
            _name = name;
            _birthday = birthday;
            _sex = sex;
        }

        public void load(DB2UDB db2, final Param param, CertifIssueDat cid) throws SQLException, Exception {
            _schregRegdDat = SchregRegdDat.createSourseCodeDat(db2, param._exeYear, param._semester, _schregno);
            _schregAddressDat = SchregAddressDat.createSchregAddressDat(db2, _schregno);
            _certifIssueDat = cid;
            _certifSchoolDat = CertifSchoolDat.createSchoolDat(db2, param, param._exeYear, _certifIssueDat._certifKindCd);
            _schregEnvirDat = createSchregEnvirDat(db2, param, param._exeYear, _schregno);
        }


        private static Student createStudent(final DB2UDB db2, final Param param, final String certifIndex)
            throws SQLException, Exception {

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                CertifIssueDat cid = CertifIssueDat.createCertifIssueDat(db2, param, certifIndex);

                ps = db2.prepareStatement(sqlStudents(cid._schregNo));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String disp_schregno = rs.getString("DISP_SCHREGNO");
                    final String kana = rs.getString("NAME_KANA");
                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String sex = rs.getString("SEX");

                    final Student studentDat = new Student(
                            schregNo,
                            disp_schregno,
                            kana,
                            name,
                            birthday,
                            sex
                    );

                    studentDat.load(db2, param, cid);
                    return studentDat;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
            return new Student(null, null, "", "", null, "");
        }

        private static String sqlStudents(String schregno) {
            return " select"
                    + "    T1.SCHREGNO ,"
            		+ "    CASE WHEN T2.SCHREGNO_OLD IS NOT NULL THEN T2.SCHREGNO_OLD ELSE T1.SCHREGNO END AS DISP_SCHREGNO, "
                    + "    T1.NAME_KANA ,"
                    + "    T1.NAME ,"
                    + "    T1.REAL_NAME ,"
                    + "    (CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME,"
                    + "    T1.BIRTHDAY ,"
                    + "    T1.SEX "
                    + " from"
                    + "    SCHREG_BASE_MST T1"
                    + "    LEFT JOIN SCHREGNO_MAPPING_DAT T2 "
                    + "      ON T2.SCHREGNO = T1.SCHREGNO "
                    + " left join "
                    + "    SCHREG_NAME_SETUP_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.DIV = '01' "
                    + " where"
                    + "    T1.SCHREGNO = '" + schregno + "'";
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private static class SchregRegdDat {
        private final String _grade;        // 年次
        private final String _schoolKind;        // 年次
        private final String _gradeCd;        // 年次
        private final String _courseCd;      // 課程コード
        private final String _majorCd;       // 学科コード
        private final String _courseName;       // 課程名

        SchregRegdDat(
                final String grade,
                final String schoolKind,
                final String gradeCd,
                final String courseCd,
                final String majorCd,
                final String courseName
        ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _gradeCd = gradeCd;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseName = courseName;
        }


        public static SchregRegdDat createSourseCodeDat(DB2UDB db2, String YEAR, String SEMESTER, String SCHREGNO)
            throws SQLException {
                PreparedStatement ps = null;
                ResultSet rs = null;

    			try {
    				ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
    				rs = ps.executeQuery();
    				while (rs.next()) {
    					final String grade = rs.getString("GRADE");
    					final String schoolKind = rs.getString("SCHOOL_KIND");
    					final String gradeCd = rs.getString("GRADE_CD");
    					final String courseCd = rs.getString("COURSECD");
    					final String majorCd = rs.getString("MAJORCD");
    					final String coursename = rs.getString("COURSENAME");

    					final SchregRegdDat schregRegdDat = new SchregRegdDat(
    							grade,
    							schoolKind,
    							gradeCd,
    							courseCd,
    							majorCd,
    							coursename
    							);
    					return schregRegdDat;
    				}
    	        } finally {
    	        	DbUtils.closeQuietly(null, ps, rs);
    	        	db2.commit();
    	        }
                return new SchregRegdDat(null, null, null, null, null, null);
        }

        private static String sqlSchregRegdDat(String year, String semester, String schregNo) {
            return " select"
                    + "    L1.GRADE_CD AS GRADE,"
                    + "    L1.SCHOOL_KIND,"
                    + "    L1.GRADE_CD,"
                    + "    T1.COURSECD,"
                    + "    CM.COURSENAME,"
                    + "    T1.MAJORCD"
                    + " from"
                    + "    SCHREG_REGD_DAT T1 "
                    + "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR "
                    + "         AND L1.GRADE = T1.GRADE "
                    + "    LEFT JOIN COURSE_MST CM ON CM.COURSECD = T1.COURSECD "
                    + " where"
                    + "    T1.SCHREGNO = '" + schregNo + "' and"
                    + "    T1.YEAR = '" + year + "' and"
                    + "    T1.SEMESTER = '" + semester + "'"
                    ;
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍住所データ。
     */
    private static class SchregAddressDat {
        private final String _zipcd; // 郵便番号
        private final String _addr1; // 住所１
        private final String _addr2; // 住所２
        private final String _telno; // 電話番号

        SchregAddressDat(
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno
        ) {
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
        }

        private static SchregAddressDat createSchregAddressDat(DB2UDB db2, String schregno)
                throws SQLException {

            PreparedStatement ps = null;
            ResultSet rs = null;
			try {
				ps = db2.prepareStatement(sqlSchregAddressDat(schregno));
				rs = ps.executeQuery();
				while (rs.next()) {
					final String zipcd = rs.getString("ZIPCD");
					final String addr1 = rs.getString("ADDR1");
					final String addr2 = rs.getString("ADDR2");
					final String telno = rs.getString("TELNO");

					final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
							zipcd,
							addr1,
							addr2,
							telno
							);
					return studentSchregAddressDat;
				}
	        } finally {
	        	DbUtils.closeQuietly(null, ps, rs);
	        	db2.commit();
	        }
            return new SchregAddressDat("", "", "", "");
        }

        private static String sqlSchregAddressDat(String schregno) {
            return " select"
                    + "    ZIPCD ,"
                    + "    ADDR1 ,"
                    + "    ADDR2 ,"
                    + "    TELNO "
                    + " from"
                    + "    SCHREG_ADDRESS_DAT"
                    + " where"
                    + "    SCHREGNO = '" + schregno + "'"
                    + " order by ISSUEDATE DESC";
        }
    }


    // ======================================================================
    /**
     * 証明書学校データ。
     */
    private static class CertifSchoolDat {
        private final String _remark1;      // 学校所在地
        private final String _remark5;      // 電話番号
        private final String _schoolName;       // 学校名
        private final String _jobName;          // 役職名
        private final String _principalName;    // 校長名

        CertifSchoolDat(
                final String remark1,
                final String schoolName,
                final String jobName,
                final String remark5,
                final String principalName
        ) {
            _remark1 = remark1;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark5 = remark5;
        }

        private static CertifSchoolDat createSchoolDat(DB2UDB db2, final Param param, String year, String certifKind)
            throws SQLException {

            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sqlCertifSchoolDat(year, certifKind);
            log.debug("certif sql = "+sql);
			try {
				ps = db2.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs.next()) {
					final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
					final String schoolName = StringUtils.defaultString(rs.getString("SCHOOL_NAME"));
					final String jobName = StringUtils.defaultString(rs.getString("JOB_NAME"));
					final String remark5 = StringUtils.defaultString(rs.getString("REMARK5"));
					final String principalName = StringUtils.defaultString(rs.getString("PRINCIPAL_NAME"));

					final CertifSchoolDat certifSchoolDat = new CertifSchoolDat(
							remark1,
							schoolName,
							jobName,
							remark5,
							principalName
							);
					return certifSchoolDat;
				}
	        } finally {
	        	DbUtils.closeQuietly(null, ps, rs);
	        	db2.commit();
	        }
            return new CertifSchoolDat(null, null, null, null, null);
        }

        private static String sqlCertifSchoolDat(String year, String certifKind) {
            return " select"
                    + "    REMARK1 ,"
                    + "    REMARK5 ,"
                    + "    SCHOOL_NAME,"
                    + "    JOB_NAME ,"
                    + "    PRINCIPAL_NAME "
                    + " from"
                    + "    CERTIF_SCHOOL_DAT"
                    + " where"
                    + "    YEAR = '" + year + "' and"
                    + "    CERTIF_KINDCD = '" + certifKind + "'"
                    ;
        }
    }

    // ======================================================================
    /**
     * 証明書種別別発行データ。
     */
    private static class CertifIssueDat {
        private final String _schregNo;     // 学籍番号
        private final String _certifKindCd; // 証明書種類コード
        private final String _issueDate;    // 証明書発行日付
        private final String _certifNo;     // 証明書番号

        private CertifDetailEachtypeDat _certifDetailEachtypeDat;

        CertifIssueDat(
                final String schregNo,
                final String certifKindCd,
                final String issueDate,
                final String certifNo
        ) {
            _schregNo = schregNo;
            _certifKindCd = certifKindCd;
            _issueDate = issueDate;
            _certifNo = certifNo;
        }

        public void load(DB2UDB db2, final Param param, final String certifIndex) throws SQLException {
            _certifDetailEachtypeDat = CertifDetailEachtypeDat.createCertifDetailEachtypeDat(db2, param, certifIndex);
        }

        private static CertifIssueDat createCertifIssueDat(DB2UDB db2, final Param param, String pCertifIndex)
            throws SQLException {

            final String sql = sqlCertifIssueDat(param, pCertifIndex);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String certifKindCd = rs.getString("CERTIF_KINDCD");
                    final String issueDate = rs.getString("ISSUEDATE");
                    final String certifNo = rs.getString("CERTIF_NO");

                    final CertifIssueDat certifIssueDat = new CertifIssueDat(
                            schregno,
                            certifKindCd,
                            issueDate,
                            certifNo
                    );
                    certifIssueDat.load(db2, param, pCertifIndex);
                    return certifIssueDat;
                }
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }

            log.debug(">>>CERTIF_ISSUE_DAT に該当するものがありません。");
            return new CertifIssueDat("", "", "", "");
        }

        private static String sqlCertifIssueDat(final Param param, String certifIndex) {
            return " select"
                    + "    T1.SCHREGNO,"
                    + "    T1.CERTIF_KINDCD,"
                    + "    T1.ISSUEDATE,"
                    + "    T1.CERTIF_NO  "
                    + " from"
                    + "    CERTIF_ISSUE_DAT T1 "
                    + " where"
                    + "    T1.YEAR = '" + param._exeYear + "' and"
                    + "    T1.CERTIF_INDEX = '" + certifIndex + "'"
                    ;
        }
    }

    // ======================================================================
    /**
     * 証明書種別別詳細データ。
     */
    private static class CertifDetailEachtypeDat {
        private final String _schregno;     // 学籍番号
        private final String _type;         // 証明書種別
        private final String _remark1;      // 備考1
        private final String _remark2;      // 備考2
        private final String _remark3;      // 備考3
        private final String _remark4;      // 備考4
        private final String _remark5;      // 備考5
        private final String _remark6;      // 備考6
        private final String _remark7;      // 備考7

        CertifDetailEachtypeDat(
                final String schregno,
                final String type,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7
        ) {
            _schregno = schregno;
            _type = type;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
        }


        private static CertifDetailEachtypeDat createCertifDetailEachtypeDat(DB2UDB db2, final Param param, String pCertifIndex) throws SQLException {

            final String sql = sqlCertifDetailEachtypeDat(param, pCertifIndex);

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
				ps = db2.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs.next()) {
					final String schregno = rs.getString("SCHREGNO");
					final String type = rs.getString("TYPE");
					final String remark1 = rs.getString("REMARK1");
					final String remark2 = rs.getString("REMARK2");
					final String remark3 = rs.getString("REMARK3");
					final String remark4 = rs.getString("REMARK4");
					final String remark5 = rs.getString("REMARK5");
					final String remark6 = rs.getString("REMARK6");
					final String remark7 = rs.getString("REMARK7");

					final CertifDetailEachtypeDat certifDetailEachtypeDat = new CertifDetailEachtypeDat(schregno, type,
							remark1, remark2, remark3, remark4, remark5, remark6, remark7);
					return certifDetailEachtypeDat;
				}
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }

            log.debug(">>>CERTIF_DETAIL_EACHTYPE_DAT に該当するものがありません。");
            return new CertifDetailEachtypeDat("", "", "", "", "", "", "", "", "");
        }

        private static String sqlCertifDetailEachtypeDat(final Param param, String certifIndex) {
            return " select"
                    + "    SCHREGNO ,"
                    + "    TYPE,"
                    + "    REMARK1 ,"
                    + "    REMARK2 ,"
                    + "    REMARK3 ,"
                    + "    REMARK4 ,"
                    + "    REMARK5 ,"
                    + "    REMARK6 ,"
                    + "    REMARK7 "
                    + " from"
                    + "    CERTIF_DETAIL_EACHTYPE_DAT"
                    + " where"
                    + "    YEAR = '" + param._exeYear + "' and"
                    + "    CERTIF_INDEX = '" + certifIndex + "'"
                    ;
        }
    }

    // ======================================================================
    /**
     * 通学路線データ。
     */
    private static class EnvirDat{
    	private final String _josya_1;
    	private final String _rosen_1;
    	private final String _gesya_1;
    	private final String _josya_1_name;
    	private final String _rosen_1_name;
    	private final String _gesya_1_name;
    	private final String _flg_1;
    	private final String _josya_2;
    	private final String _rosen_2;
    	private final String _gesya_2;
    	private final String _josya_2_name;
    	private final String _rosen_2_name;
    	private final String _gesya_2_name;
    	private final String _flg_2;
    	private final String _josya_3;
    	private final String _rosen_3;
    	private final String _gesya_3;
    	private final String _josya_3_name;
    	private final String _rosen_3_name;
    	private final String _gesya_3_name;
    	private final String _flg_3;
    	private final String _josya_4;
    	private final String _rosen_4;
    	private final String _gesya_4;
    	private final String _josya_4_name;
    	private final String _rosen_4_name;
    	private final String _gesya_4_name;
    	private final String _flg_4;
    	private final String _josya_5;
    	private final String _rosen_5;
    	private final String _gesya_5;
    	private final String _josya_5_name;
    	private final String _rosen_5_name;
    	private final String _gesya_5_name;
    	private final String _flg_5;
    	private final String _josya_6;
    	private final String _rosen_6;
    	private final String _gesya_6;
    	private final String _josya_6_name;
    	private final String _rosen_6_name;
    	private final String _gesya_6_name;
    	private final String _flg_6;
    	private final String _josya_7;
    	private final String _rosen_7;
    	private final String _gesya_7;
    	private final String _josya_7_name;
    	private final String _rosen_7_name;
    	private final String _gesya_7_name;
    	private final String _flg_7;

        EnvirDat(final String josya_1, final String rosen_1, final String gesya_1, final String flg_1,
                final String josya_1_name, final String rosen_1_name, final String gesya_1_name,
                final String josya_2, final String rosen_2, final String gesya_2, final String flg_2,
                final String josya_2_name, final String rosen_2_name, final String gesya_2_name,
                final String josya_3, final String rosen_3, final String gesya_3, final String flg_3,
                final String josya_3_name, final String rosen_3_name, final String gesya_3_name,
                final String josya_4, final String rosen_4, final String gesya_4, final String flg_4,
                final String josya_4_name, final String rosen_4_name, final String gesya_4_name,
                final String josya_5, final String rosen_5, final String gesya_5, final String flg_5,
                final String josya_5_name, final String rosen_5_name, final String gesya_5_name,
                final String josya_6, final String rosen_6, final String gesya_6, final String flg_6,
                final String josya_6_name, final String rosen_6_name, final String gesya_6_name,
                final String josya_7, final String rosen_7, final String gesya_7, final String flg_7,
                final String josya_7_name, final String rosen_7_name, final String gesya_7_name
    			){
            _josya_1 = josya_1;
            _rosen_1 = rosen_1;
            _gesya_1 = gesya_1;
            _josya_1_name = josya_1_name;
            _rosen_1_name = rosen_1_name;
            _gesya_1_name = gesya_1_name;
            _flg_1 = flg_1;
            _josya_2 = josya_2;
            _rosen_2 = rosen_2;
            _gesya_2 = gesya_2;
            _josya_2_name = josya_2_name;
            _rosen_2_name = rosen_2_name;
            _gesya_2_name = gesya_2_name;
            _flg_2 = flg_2;
            _josya_3 = josya_3;
            _rosen_3 = rosen_3;
            _gesya_3 = gesya_3;
            _josya_3_name = josya_3_name;
            _rosen_3_name = rosen_3_name;
            _gesya_3_name = gesya_3_name;
            _flg_3 = flg_3;
            _josya_4 = josya_4;
            _rosen_4 = rosen_4;
            _gesya_4 = gesya_4;
            _josya_4_name = josya_4_name;
            _rosen_4_name = rosen_4_name;
            _gesya_4_name = gesya_4_name;
            _flg_4 = flg_4;
            _josya_5 = josya_5;
            _rosen_5 = rosen_5;
            _gesya_5 = gesya_5;
            _josya_5_name = josya_5_name;
            _rosen_5_name = rosen_5_name;
            _gesya_5_name = gesya_5_name;
            _flg_5 = flg_5;
            _josya_6 = josya_6;
            _rosen_6 = rosen_6;
            _gesya_6 = gesya_6;
            _josya_6_name = josya_6_name;
            _rosen_6_name = rosen_6_name;
            _gesya_6_name = gesya_6_name;
            _flg_6 = flg_6;
            _josya_7 = josya_7;
            _rosen_7 = rosen_7;
            _gesya_7 = gesya_7;
            _josya_7_name = josya_7_name;
            _rosen_7_name = rosen_7_name;
            _gesya_7_name = gesya_7_name;
            _flg_7 = flg_7;
    	}
        private boolean chkFlg(final Param param, int idx) {
        	boolean retbln = false;
        	final String chkflgstr = getFlg(idx);
        	if (!"".equals(chkflgstr) && param._findnameflg.contains(chkflgstr)) {
        		retbln = true;
        	}
        	return retbln;
        }
    	private String getFlg(int idx) {
    		String retstr = "";
    		if (idx == 1) {
    			retstr = StringUtils.defaultString(_flg_1, "");
    		} else if (idx == 2)  {
    			retstr = StringUtils.defaultString(_flg_2, "");
    		} else if (idx == 3)  {
    			retstr = StringUtils.defaultString(_flg_3, "");
    		} else if (idx == 4)  {
    			retstr = StringUtils.defaultString(_flg_4, "");
    		} else if (idx == 5)  {
    			retstr = StringUtils.defaultString(_flg_5, "");
    		} else if (idx == 6)  {
    			retstr = StringUtils.defaultString(_flg_6, "");
    		} else if (idx == 7)  {
    			retstr = StringUtils.defaultString(_flg_7, "");
    		}
    		return retstr;
    	}
    	private String getJosya(final Param param, int idx) {
    		boolean chkbln = chkFlg(param, idx);
    		String retstr = "";
    		if (idx == 1) {
    			retstr = chkbln ? StringUtils.defaultString(_josya_1_name, _josya_1) : _josya_1;
    		} else if (idx == 2)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_2_name, _josya_2) : _josya_2;
    		} else if (idx == 3)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_3_name, _josya_3) : _josya_3;
    		} else if (idx == 4)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_4_name, _josya_4) : _josya_4;
    		} else if (idx == 5)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_5_name, _josya_5) : _josya_5;
    		} else if (idx == 6)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_6_name, _josya_6) : _josya_6;
    		} else if (idx == 7)  {
    			retstr = chkbln ? StringUtils.defaultString(_josya_7_name, _josya_7) : _josya_7;
    		}
    		return retstr;
    	}
    	private String getGesya(final Param param, int idx) {
    		boolean chkbln = chkFlg(param, idx);
    		String retstr = "";
    		if (idx == 1) {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_1_name, _gesya_1) : _gesya_1;
    		} else if (idx == 2)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_2_name, _gesya_2) : _gesya_2;
    		} else if (idx == 3)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_3_name, _gesya_3) : _gesya_3;
    		} else if (idx == 4)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_4_name, _gesya_4) : _gesya_4;
    		} else if (idx == 5)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_5_name, _gesya_5) : _gesya_5;
    		} else if (idx == 6)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_6_name, _gesya_6) : _gesya_6;
    		} else if (idx == 7)  {
    			retstr = chkbln ? StringUtils.defaultString(_gesya_7_name, _gesya_7) : _gesya_7;
    		}
    		return retstr;
    	}
    	private String getRosen(final Param param, int idx) {
    		boolean chkbln = chkFlg(param, idx);
    		String retstr = "";
    		if (idx == 1) {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_1_name, _rosen_1) : _rosen_1;
    		} else if (idx == 2)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_2_name, _rosen_2) : _rosen_2;
    		} else if (idx == 3)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_3_name, _rosen_3) : _rosen_3;
    		} else if (idx == 4)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_4_name, _rosen_4) : _rosen_4;
    		} else if (idx == 5)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_5_name, _rosen_5) : _rosen_5;
    		} else if (idx == 6)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_6_name, _rosen_6) : _rosen_6;
    		} else if (idx == 7)  {
    			retstr = chkbln ? StringUtils.defaultString(_rosen_7_name, _rosen_7) : _rosen_7;
    		}
    		return retstr;
    	}
    }
    private static EnvirDat createSchregEnvirDat(DB2UDB db2, final Param param, String year, String schregno)
            throws SQLException {

    	EnvirDat envirDat = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlSchregEnvirDat(param, year, schregno);
        log.debug("sql = " + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String josya_1 = rs.getString("JOSYA_1");
            	final String rosen_1 = rs.getString("ROSEN_1");
            	final String gesya_1 = rs.getString("GESYA_1");
            	final String flg_1 = rs.getString("FLG_1");
            	final String josya_2 = rs.getString("JOSYA_2");
            	final String rosen_2 = rs.getString("ROSEN_2");
            	final String gesya_2 = rs.getString("GESYA_2");
            	final String flg_2 = rs.getString("FLG_2");
            	final String josya_3 = rs.getString("JOSYA_3");
            	final String rosen_3 = rs.getString("ROSEN_3");
            	final String gesya_3 = rs.getString("GESYA_3");
            	final String flg_3 = rs.getString("FLG_3");
            	final String josya_4 = rs.getString("JOSYA_4");
            	final String rosen_4 = rs.getString("ROSEN_4");
            	final String gesya_4 = rs.getString("GESYA_4");
            	final String flg_4 = rs.getString("FLG_4");
            	final String josya_5 = rs.getString("JOSYA_5");
            	final String rosen_5 = rs.getString("ROSEN_5");
            	final String gesya_5 = rs.getString("GESYA_5");
            	final String flg_5 = rs.getString("FLG_5");
            	final String josya_6 = rs.getString("JOSYA_6");
            	final String rosen_6 = rs.getString("ROSEN_6");
            	final String gesya_6 = rs.getString("GESYA_6");
            	final String flg_6 = rs.getString("FLG_6");
            	final String josya_7 = rs.getString("JOSYA_7");
            	final String rosen_7 = rs.getString("ROSEN_7");
            	final String gesya_7 = rs.getString("GESYA_7");
            	final String flg_7 = rs.getString("FLG_7");

            	final String josya_1_name = rs.getString("JOSYA_1_NAME");
            	final String rosen_1_name = rs.getString("ROSEN_1_NAME");
            	final String gesya_1_name = rs.getString("GESYA_1_NAME");
            	final String josya_2_name = rs.getString("JOSYA_2_NAME");
            	final String rosen_2_name = rs.getString("ROSEN_2_NAME");
            	final String gesya_2_name = rs.getString("GESYA_2_NAME");
            	final String josya_3_name = rs.getString("JOSYA_3_NAME");
            	final String rosen_3_name = rs.getString("ROSEN_3_NAME");
            	final String gesya_3_name = rs.getString("GESYA_3_NAME");
            	final String josya_4_name = rs.getString("JOSYA_4_NAME");
            	final String rosen_4_name = rs.getString("ROSEN_4_NAME");
            	final String gesya_4_name = rs.getString("GESYA_4_NAME");
            	final String josya_5_name = rs.getString("JOSYA_5_NAME");
            	final String rosen_5_name = rs.getString("ROSEN_5_NAME");
            	final String gesya_5_name = rs.getString("GESYA_5_NAME");
            	final String josya_6_name = rs.getString("JOSYA_6_NAME");
            	final String rosen_6_name = rs.getString("ROSEN_6_NAME");
            	final String gesya_6_name = rs.getString("GESYA_6_NAME");
            	final String josya_7_name = rs.getString("JOSYA_7_NAME");
            	final String rosen_7_name = rs.getString("ROSEN_7_NAME");
            	final String gesya_7_name = rs.getString("GESYA_7_NAME");

                envirDat = new EnvirDat(
                		josya_1, rosen_1, gesya_1, flg_1,
                		josya_1_name, rosen_1_name, gesya_1_name,
                		josya_2, rosen_2, gesya_2, flg_2,
                		josya_2_name, rosen_2_name, gesya_2_name,
                		josya_3, rosen_3, gesya_3, flg_3,
                		josya_3_name, rosen_3_name, gesya_3_name,
                		josya_4, rosen_4, gesya_4, flg_4,
                		josya_4_name, rosen_4_name, gesya_4_name,
                		josya_5, rosen_5, gesya_5, flg_5,
                		josya_5_name, rosen_5_name, gesya_5_name,
                		josya_6, rosen_6, gesya_6, flg_6,
                		josya_6_name, rosen_6_name, gesya_6_name,
                		josya_7, rosen_7, gesya_7, flg_7,
                		josya_7_name, rosen_7_name, gesya_7_name
                );
            }
        } finally {
        	DbUtils.closeQuietly(null, ps, rs);
        	db2.commit();
        }
        return envirDat;
    }
    private static String sqlSchregEnvirDat(final Param param, String year, String schregno) {
        return " select "
                + "    T1.*, "
        		+ "    CASE WHEN T1.FLG_1 IN (" + param._findnameinstr + ") THEN NJ1.STATION_NAME ELSE '' END AS JOSYA_1_NAME, "
        		+ "    CASE WHEN T1.FLG_1 IN (" + param._findnameinstr + ") THEN NJ1.LINE_NAME ELSE '' END AS ROSEN_1_NAME, "
        		+ "    CASE WHEN T1.FLG_1 IN (" + param._findnameinstr + ") THEN NG1.STATION_NAME ELSE '' END AS GESYA_1_NAME, "
        		+ "    CASE WHEN T1.FLG_2 IN (" + param._findnameinstr + ") THEN NJ2.STATION_NAME ELSE '' END AS JOSYA_2_NAME, "
        		+ "    CASE WHEN T1.FLG_2 IN (" + param._findnameinstr + ") THEN NJ2.LINE_NAME ELSE '' END AS ROSEN_2_NAME, "
        		+ "    CASE WHEN T1.FLG_2 IN (" + param._findnameinstr + ") THEN NG2.STATION_NAME ELSE '' END AS GESYA_2_NAME, "
        		+ "    CASE WHEN T1.FLG_3 IN (" + param._findnameinstr + ") THEN NJ3.STATION_NAME ELSE '' END AS JOSYA_3_NAME, "
        		+ "    CASE WHEN T1.FLG_3 IN (" + param._findnameinstr + ") THEN NJ3.LINE_NAME ELSE '' END AS ROSEN_3_NAME, "
        		+ "    CASE WHEN T1.FLG_3 IN (" + param._findnameinstr + ") THEN NG3.STATION_NAME ELSE '' END AS GESYA_3_NAME, "
        		+ "    CASE WHEN T1.FLG_4 IN (" + param._findnameinstr + ") THEN NJ4.STATION_NAME ELSE '' END AS JOSYA_4_NAME, "
        		+ "    CASE WHEN T1.FLG_4 IN (" + param._findnameinstr + ") THEN NJ4.LINE_NAME ELSE '' END AS ROSEN_4_NAME, "
        		+ "    CASE WHEN T1.FLG_4 IN (" + param._findnameinstr + ") THEN NG4.STATION_NAME ELSE '' END AS GESYA_4_NAME, "
        		+ "    CASE WHEN T1.FLG_5 IN (" + param._findnameinstr + ") THEN NJ5.STATION_NAME ELSE '' END AS JOSYA_5_NAME, "
        		+ "    CASE WHEN T1.FLG_5 IN (" + param._findnameinstr + ") THEN NJ5.LINE_NAME ELSE '' END AS ROSEN_5_NAME, "
        		+ "    CASE WHEN T1.FLG_5 IN (" + param._findnameinstr + ") THEN NG5.STATION_NAME ELSE '' END AS GESYA_5_NAME, "
        		+ "    CASE WHEN T1.FLG_6 IN (" + param._findnameinstr + ") THEN NJ6.STATION_NAME ELSE '' END AS JOSYA_6_NAME, "
        		+ "    CASE WHEN T1.FLG_6 IN (" + param._findnameinstr + ") THEN NJ6.LINE_NAME ELSE '' END AS ROSEN_6_NAME, "
        		+ "    CASE WHEN T1.FLG_6 IN (" + param._findnameinstr + ") THEN NG6.STATION_NAME ELSE '' END AS GESYA_6_NAME, "
        		+ "    CASE WHEN T1.FLG_7 IN (" + param._findnameinstr + ") THEN NJ7.STATION_NAME ELSE '' END AS JOSYA_7_NAME, "
        		+ "    CASE WHEN T1.FLG_7 IN (" + param._findnameinstr + ") THEN NJ7.LINE_NAME ELSE '' END AS ROSEN_7_NAME, "
        		+ "    CASE WHEN T1.FLG_7 IN (" + param._findnameinstr + ") THEN NG7.STATION_NAME ELSE '' END AS GESYA_7_NAME "
                + "     "
                + " from "
                + "    SCHREG_ENVIR_DAT T1 "
                + "    LEFT JOIN STATION_NETMST NJ1 ON NJ1.LINE_CD = T1.ROSEN_1 AND NJ1.STATION_CD = T1.JOSYA_1 "
                + "    LEFT JOIN STATION_NETMST NG1 ON NG1.LINE_CD = T1.ROSEN_1 AND NG1.STATION_CD = T1.GESYA_1 "
                + "    LEFT JOIN STATION_NETMST NJ2 ON NJ2.LINE_CD = T1.ROSEN_2 AND NJ2.STATION_CD = T1.JOSYA_2 "
                + "    LEFT JOIN STATION_NETMST NG2 ON NG2.LINE_CD = T1.ROSEN_2 AND NG2.STATION_CD = T1.GESYA_2 "
                + "    LEFT JOIN STATION_NETMST NJ3 ON NJ3.LINE_CD = T1.ROSEN_3 AND NJ3.STATION_CD = T1.JOSYA_3 "
                + "    LEFT JOIN STATION_NETMST NG3 ON NG3.LINE_CD = T1.ROSEN_3 AND NG3.STATION_CD = T1.GESYA_3 "
                + "    LEFT JOIN STATION_NETMST NJ4 ON NJ4.LINE_CD = T1.ROSEN_4 AND NJ4.STATION_CD = T1.JOSYA_4 "
                + "    LEFT JOIN STATION_NETMST NG4 ON NG4.LINE_CD = T1.ROSEN_4 AND NG4.STATION_CD = T1.GESYA_4 "
                + "    LEFT JOIN STATION_NETMST NJ5 ON NJ5.LINE_CD = T1.ROSEN_5 AND NJ5.STATION_CD = T1.JOSYA_5 "
                + "    LEFT JOIN STATION_NETMST NG5 ON NG5.LINE_CD = T1.ROSEN_5 AND NG5.STATION_CD = T1.GESYA_5 "
                + "    LEFT JOIN STATION_NETMST NJ6 ON NJ6.LINE_CD = T1.ROSEN_6 AND NJ6.STATION_CD = T1.JOSYA_6 "
                + "    LEFT JOIN STATION_NETMST NG6 ON NG6.LINE_CD = T1.ROSEN_6 AND NG6.STATION_CD = T1.GESYA_6 "
                + "    LEFT JOIN STATION_NETMST NJ7 ON NJ7.LINE_CD = T1.ROSEN_7 AND NJ7.STATION_CD = T1.JOSYA_7 "
                + "    LEFT JOIN STATION_NETMST NG7 ON NG7.LINE_CD = T1.ROSEN_7 AND NG7.STATION_CD = T1.GESYA_7 "
                + " where "
                + "    T1.SCHREGNO = '" + schregno + "'"
                ;
    }

} // KNJG060A

// eof

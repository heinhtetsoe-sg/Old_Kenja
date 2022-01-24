package servletpack.KNJI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;


/**
 * 卒業生名簿
 */
public class KNJI093 {

    private static final Log log = LogFactory.getLog(KNJI093.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            final List studentList = new ArrayList();
            for (int i = 0; i < _param._classSelected.length; i++) {
                // 生徒データを取得
                studentList.addAll(createStudentInfoData(db2, _param._classSelected[i]));
            }
            if (printMain(svf, db2, studentList)) { // 生徒出力のメソッド
                _hasData = true;
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private boolean printMain(final Vrw32alp svf, final DB2UDB db2, final List studentList) throws Exception {
        boolean hasData = false;

        if (null == _param._useFormNameI093 || "".equals(_param._useFormNameI093)) {
            svf.VrSetForm("KNJI093_2.frm", 4);
        } else {
            svf.VrSetForm(_param._useFormNameI093 + ".frm", 1);
        }
        int gyo = 0;
        int renban = 1;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            gyo++;
            if (null == _param._useFormNameI093 || "".equals(_param._useFormNameI093)) {
                printStudent(svf, db2, gyo, student);
                svf.VrEndRecord();
            } else {
                if (gyo > 10) {
                    svf.VrEndPage();
                    gyo = 1;
                }
                printStudent2(svf, db2, gyo, student, renban);
                renban++;
            }
            hasData = true;
        }
        if (renban > 1) {
            svf.VrEndPage();
        }

        return  hasData;
    }

    private void printStudent(final Vrw32alp svf, final DB2UDB db2, final int gyo, final Student student) {
        final String setTitle = "K".equals(student._schoolKind) ? "　卒園生名簿" : "　卒業生名簿";
        if (_param._isSeireki) {
            svf.VrsOut("TITLE", StringUtils.defaultString(_param._year) + "年度" + setTitle);
        } else {
            svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._gengoBaseDate) + "度" + setTitle);
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolname2);
        final String setGradName = "K".equals(student._schoolKind) ? "卒園生番号" : "卒業生番号";
        svf.VrsOut("GRAD_NAME", setGradName);
        final String setNameTitle = "K".equals(student._schoolKind) ? "園　児" : "氏　名";
        svf.VrsOut("NAME_TITLE", setNameTitle);
        svf.VrsOut("CERTIFNO", student._grdNo);
        final String name;
        if ("1".equals(student._useRealName) && "1".equals(student._nameOutputFlg) && student._realName != null && student._name != null) {
            name = student._realName + "（" + student._name + "）";
        } else if ("1".equals(student._useRealName) && student._realName != null) {
            name = student._realName;
        } else {
            name = student._name;
        }
        final int namelen = KNJ_EditEdit.getMS932ByteLength(name);
        final int checkNamelen = 20;
        if (checkNamelen * 3 < namelen) {
            svf.VrsOut("NAME4", name);
        } else if (checkNamelen * 2 < namelen) {
            svf.VrsOut("NAME3", name);
        } else if (checkNamelen < namelen) {
            svf.VrsOut("NAME2", name);
        } else {
            svf.VrsOut("NAME1", name);
        }
        final String guardname = student._guardname;
        final int guardnamelen = KNJ_EditEdit.getMS932ByteLength(guardname);
        if (checkNamelen * 3 < guardnamelen) {
            svf.VrsOut("GUARD_NAME4", guardname);
        } else if (checkNamelen * 2 < guardnamelen) {
            svf.VrsOut("GUARD_NAME3", guardname);
        } else if (checkNamelen < guardnamelen) {
            svf.VrsOut("GUARD_NAME2", guardname);
        } else {
            svf.VrsOut("GUARD_NAME1", guardname);
        }
        if (_param._isSeireki) {
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthday));
        } else {
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday));
        }
        svf.VrsOut("ZIP_NO", student._zipcd);
        if (student._addr2 != null) {
            if (50 < KNJ_EditEdit.getMS932ByteLength(student._addr1) || 50 < KNJ_EditEdit.getMS932ByteLength(student._addr2)) {
                svf.VrsOut("ADDR4_1", student._addr1);
                svf.VrsOut("ADDR4_2", student._addr2);
            } else {
                svf.VrsOut("ADDR3_1", student._addr1);
                svf.VrsOut("ADDR3_2", student._addr2);
            }
        } else {
            if (50 < KNJ_EditEdit.getMS932ByteLength(student._addr1)) {
                svf.VrsOut("ADDR2", student._addr1);
            } else {
                svf.VrsOut("ADDR1", student._addr1);
            }
        }
    }

    private void printStudent2(final Vrw32alp svf, final DB2UDB db2, final int gyo, final Student student, final int renban) {
        final String setTitle = "K".equals(student._schoolKind) ? "　卒園生名簿" : "　卒業生名簿";
        if (_param._isSeireki) {
            svf.VrsOut("TITLE", StringUtils.defaultString(_param._year) + "年度" + setTitle);
        } else {
            svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._gengoBaseDate) + "度" + setTitle);
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolname2);

        final String setNameTitle = "K".equals(student._schoolKind) ? "園　児" : "氏　名";
        svf.VrsOut("NAME_TITLE", setNameTitle);

        svf.VrsOut("GRAD_NAME", "学籍番号");

        final String renban0 = "00" + String.valueOf(renban);
        final String setNo = renban0.substring(renban0.length() -3);
        svf.VrsOutn("NO", gyo, setNo);

        final String kanaField = KNJ_EditEdit.getMS932ByteLength(student._kana) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._kana) > 20 ? "2" : "1";
        svf.VrsOutn("KANA" + kanaField, gyo, student._kana);

        if (_param._isSeireki) {
            svf.VrsOutn("BIRTHDAY", gyo, KNJ_EditDate.h_format_SeirekiJP(student._birthday));
        } else {
            svf.VrsOutn("BIRTHDAY", gyo, KNJ_EditDate.h_format_JP(db2, student._birthday));
        }
        final String name;
        if ("1".equals(student._useRealName) && "1".equals(student._nameOutputFlg) && student._realName != null && student._name != null) {
            name = student._realName + "（" + student._name + "）";
        } else if ("1".equals(student._useRealName) && student._realName != null) {
            name = student._realName;
        } else {
            name = student._name;
        }
        final int namelen = KNJ_EditEdit.getMS932ByteLength(name);
        final int checkNamelen = 20;
        if (checkNamelen * 2 < namelen) {
            svf.VrsOutn("NAME3", gyo, name);
        } else if (checkNamelen < namelen) {
            svf.VrsOutn("NAME2", gyo, name);
        } else {
            svf.VrsOutn("NAME1", gyo, name);
        }

        if (student._unknownAddr) {
            svf.VrsOutn("ADDR3_1", gyo, "住所不明");
        } else {
            svf.VrsOutn("ZIP_NO", gyo, student._zipcd);
            if (student._addr2 != null) {
                if (50 < KNJ_EditEdit.getMS932ByteLength(student._addr1)
                        || 50 < KNJ_EditEdit.getMS932ByteLength(student._addr2)) {
                    svf.VrsOutn("ADDR4_1", gyo, student._addr1);
                    svf.VrsOutn("ADDR4_2", gyo, student._addr2);
                } else {
                    svf.VrsOutn("ADDR3_1", gyo, student._addr1);
                    svf.VrsOutn("ADDR3_2", gyo, student._addr2);
                }
            } else {
                if (50 < KNJ_EditEdit.getMS932ByteLength(student._addr1)) {
                    svf.VrsOutn("ADDR2", gyo, student._addr1);
                } else {
                    svf.VrsOutn("ADDR1", gyo, student._addr1);
                }
            }
        }
        svf.VrsOutn("CERTIFNO", gyo, student._schregno);
        svf.VrsOutn("TELNO", gyo, student._telno);
        if (_param._isSeireki) {
            svf.VrsOutn("ENT_DATE", gyo, KNJ_EditDate.h_format_SeirekiJP(student._entDate));
        } else {
            svf.VrsOutn("ENT_DATE", gyo, KNJ_EditDate.h_format_JP(db2, student._entDate));
        }

        final String remarkField = KNJ_EditEdit.getMS932ByteLength(student._remark) > 60 ? "4" : KNJ_EditEdit.getMS932ByteLength(student._remark) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._remark) > 20 ? "2" : "1";
        svf.VrsOutn("REMARK" + remarkField, gyo, student._remark);
    }

    private List createStudentInfoData(final DB2UDB db2, final String selectCd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(selectCd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("SCHREGNO"),
                        rs.getString("GRADE"),
                        rs.getString("SCHOOL_KIND"),
                        rs.getString("HR_CLASS"),
                        rs.getString("ATTENDNO"),
                        rs.getString("GRD_NO"),
                        rs.getString("NAME"),
                        rs.getString("REAL_NAME"),
                        rs.getString("USE_REAL_NAME"),
                        rs.getString("NAME_OUTPUT_FLG"),
                        rs.getString("NAME_KANA"),
                        rs.getString("GUARD_NAME"),
                        rs.getString("BIRTHDAY"),
                        rs.getString("ENT_DATE"),
                        rs.getString("ZIPCD"),
                        rs.getString("ADDR1"),
                        rs.getString("ADDR2"),
                        rs.getString("TELNO"),
                        rs.getString("UNKNOWN_ADDR"),
                        rs.getString("REMARK")
                );
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(final String selectCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ZIPCD, ");
        stb.append("         T1.ADDR1, ");
        stb.append("         T1.ADDR2, ");
        stb.append("         T1.TELNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     RTRIM(T3.GRD_NO) AS GRD_NO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.REAL_NAME, ");
        stb.append("     (CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("     L1.NAME_OUTPUT_FLG, ");
        stb.append("     T3.NAME_KANA, ");
        stb.append("     T4.GUARD_NAME, ");
        stb.append("     (CASE WHEN GRD_MST.BIRTHDAY IS NOT NULL THEN GRD_MST.BIRTHDAY ELSE T3.BIRTHDAY END) AS BIRTHDAY, ");
        stb.append("     (CASE WHEN GRD_MST.ENT_DATE IS NOT NULL THEN GRD_MST.ENT_DATE ELSE T3.ENT_DATE END) AS ENT_DATE, ");
        stb.append("     (CASE WHEN GRD_MST.CUR_ADDR1 IS NOT NULL THEN GRD_MST.CUR_ZIPCD ELSE T5.ZIPCD END) AS ZIPCD, ");
        stb.append("     (CASE WHEN GRD_MST.CUR_ADDR1 IS NOT NULL THEN GRD_MST.CUR_ADDR1 ELSE T5.ADDR1 END) AS ADDR1, ");
        stb.append("     (CASE WHEN GRD_MST.CUR_ADDR1 IS NOT NULL THEN GRD_MST.CUR_ADDR2 ELSE T5.ADDR2 END) AS ADDR2, ");
        stb.append("     (CASE WHEN GRD_MST.CUR_ADDR1 IS NOT NULL THEN GRD_MST.CUR_TELNO ELSE T5.TELNO END) AS TELNO, ");
        stb.append("     GRD_DETAIL.REMARK1 AS UNKNOWN_ADDR, ");
        stb.append("     GRD_DETAIL.REMARK2 AS REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.DIV = '06' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("          AND GDAT.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A023' AND N1.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append("     LEFT JOIN GRD_BASE_MST GRD_MST ON GRD_MST.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_BASE_DETAIL_MST GRD_DETAIL ON GRD_DETAIL.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND GRD_DETAIL.SEQ = '001' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._isPrintMajor) {
        	stb.append("     AND T1.COURSECD || T1.MAJORCD = '" + selectCd + "' ");
        	stb.append("     AND T1.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ");
        } else {
        	stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selectCd + "' ");
        }
        if (_param._mikomi == null) {
            stb.append("     AND VALUE(T3.GRD_DIV,'0') = '1' ");
        } else {
            stb.append("     AND T3.GRD_DIV IS NULL ");
        }
        stb.append(" ORDER BY ");
        if (_param._isPrintMajor) {
            stb.append("     TRANSLATE_KANA(CASE WHEN L1.SCHREGNO IS NOT NULL THEN VALUE(T3.REAL_NAME_KANA, '') ELSE VALUE(T3.NAME_KANA, '') END), ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN VALUE(T3.REAL_NAME_KANA, '') ELSE VALUE(T3.NAME_KANA, '') END, ");
        }
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒 */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _schoolKind;
        final String _hrclass;
        final String _attendno;
        final String _grdNo;
        final String _name;
        final String _realName;
        final String _useRealName;
        final String _nameOutputFlg;
        final String _kana;
        final String _guardname;
        final String _birthday;
        final String _entDate;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final boolean _unknownAddr;
        final String _remark;

        Student(
                final String schregno,
                final String grade,
                final String schoolKind,
                final String hrclass,
                final String attendno,
                final String grdNo,
                final String name,
                final String realName,
                final String useRealName,
                final String nameOutputFlg,
                final String kana,
                final String guardname,
                final String birthday,
                final String entDate,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String unknownAddr,
                final String remark
        ) {
            _schregno = schregno;
            _grade = grade;
            _schoolKind = schoolKind;
            _hrclass = hrclass;
            _attendno = attendno;
            _grdNo = grdNo;
            _name = name;
            _realName = realName;
            _useRealName = useRealName;
            _nameOutputFlg = nameOutputFlg;
            _kana = kana;
            _guardname = guardname;
            _birthday = birthday;
            _entDate = entDate;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _unknownAddr = "1".equals(unknownAddr);
            _remark = remark;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 70908 $ $Date: 2019-11-27 21:05:42 +0900 (水, 27 11 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _selectedSchKind;
        final String[] _classSelected;
        final String _ctrlYear;
        final String _ctrlDate;
        final String _mikomi;
        final boolean _isPrintMajor;
        final String _schoolname2;
        final boolean _isSeireki;
        final boolean _useSchoolKindField;
        final String _useFormNameI093;
        final String _gengoBaseDate;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _selectedSchKind = request.getParameter("selectedSchKind");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _mikomi = request.getParameter("MIKOMI");
            _useFormNameI093 = request.getParameter("useFormNameI093");
            _useSchoolKindField = "1".equals(request.getParameter("useSchool_KindField"));
            _isPrintMajor = "2".equals(request.getParameter("CLASS_MAJOR"));
            _schoolname2 = getSchoolMstSchoolname2(db2);
            _isSeireki = "2".equals(KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'")), "NAME1"));
            _gengoBaseDate = getGengouBaseDate(_year, _ctrlDate);
        }

        private String getSchoolMstSchoolname2(final DB2UDB db2) {
            String rtn = null;
            
            String sql = "";
            if (_useSchoolKindField) {
            	sql = " SELECT SCHOOLNAME2 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _selectedSchKind + "' ";
            } else {
            	sql = " SELECT SCHOOLNAME2 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            }
            log.fatal(sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME2");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
       //ログイン年度が2019年かつ5/1以降であれば"2019-05-01"を返す、それ以外は04-01 (元号変換の基準日)
       private static String getGengouBaseDate (final String targetYear ,final String loginDate) {

    	   final String yearStr = loginDate.substring(0, 4);
    	   String rtnStr = targetYear + "-" + "04-01";
    	   
    	   if (targetYear.equals("2019") && yearStr.equals("2019") && compareDateString(loginDate, "2019-05-01") >= 0) {
    		   rtnStr = "2019-05-01";
    	   } 
    	   log.fatal("(loginDate)"+loginDate+"(yearStr)"+yearStr+"(base)"+rtnStr+"(targetYear)"+targetYear);
    	   return rtnStr;
       }
        
        //YYYY-MM-DDの形式の文字列2を引数にとる。日付として大きい方(後の方)の文字列を返す
        private static Integer compareDateString (String dateString1, String dateString2) {
        	Date date1 = toDate(dateString1);
           	Date date2 = toDate(dateString2);
           	
           	return date1.compareTo(date2);

        }
        
        // 日付文字列指定でDate型を生成
        private static Date toDate(String str) {
            // 日付フォーマットを作成
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Date型へ変換
            try {
                return dateFormat.parse(str);
            } catch ( ParseException e ) {
                return null;
            }
        }
    }

}// クラスの括り

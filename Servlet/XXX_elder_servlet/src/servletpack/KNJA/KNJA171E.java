package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;


/**
 * 学籍簿
 */
public class KNJA171E {

    private static final Log log = LogFactory.getLog(KNJA171E.class);
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

            for (int i = 0; i < _param._classSelected.length; i++) {
                // 生徒データを取得
                final String schoolKind = getSchoolKind(db2, _param._classSelected[i]);
                final List studentList = createStudentInfoData(db2, _param, _param._classSelected[i]);
                if (printMain(db2, svf, schoolKind, studentList)) { // 生徒出力のメソッド
                    _hasData = true;
                }
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final String schoolKind, final List studentList) throws Exception {
        boolean hasData = false;

        svf.VrSetForm("KNJA171E.frm", 4);
        int gyo = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            gyo++;
            printStudent(db2, svf, gyo, schoolKind, student);
            svf.VrEndRecord();
            hasData = true;
        }

        return  hasData;
    }
    
    private static String formatDate(final DB2UDB db2, final Param param, final String date) {
    	if (null == date) {
    		return null;
    	}
    	if (param._isSeireki) {
    		return KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(date, "/", "-"));
    	}
    	return KNJ_EditDate.h_format_JP(db2, date);
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final int gyo, final String schoolKind, final Student student) {
        svf.VrsOut("NENDO", _param._nendo + "　クラス名簿");
        svf.VrsOut("YMD", _param._ctrlDateFormat);

        if ("K".equals(schoolKind)) {
            svf.VrsOut("STAFF", "担任：" + StringUtils.defaultString(student._trname1));
        } else {
            svf.VrsOut("STAFF", "担任：" + StringUtils.defaultString(student._trname1) + "　　副担任：" + StringUtils.defaultString(student._subtrname1) + (StringUtils.isBlank(student._subtrname2) ? "" : "　　" + student._subtrname2));
        }

        svf.VrsOut("SCHREG_NO", student._schregno);
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("ATTEND_NO", (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "");
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("NAME_KANA", student._namekana);
        svf.VrsOut("SEX", student._sexname);
        if (!"1".equals(student._h_no_name)) {
            svf.VrsOut("GUARD_NAME", student._guardname);
            svf.VrsOut("GUARD_NAME_KANA", student._guardkana);
        }
        if (!"1".equals(student._s_no_addr)) {
            svf.VrsOut("ZIP_NO", student._zipcd);
            svf.VrsOut("PREF_NAME", student._prefname);
            if (student._addr2 != null) {
                if (50 < getMS932ByteLength(student._addr1) || 50 < getMS932ByteLength(student._addr2)) {
                    svf.VrsOut("ADDR2_2", student._addr1);
                    svf.VrsOut("ADDR2_3", student._addr2);
                } else {
                    svf.VrsOut("ADDR1_2", student._addr1);
                    svf.VrsOut("ADDR1_3", student._addr2);
                }
            } else {
                if (50 < getMS932ByteLength(student._addr1)) {
                    svf.VrsOut("ADDR2_1", student._addr1);
                } else {
                    svf.VrsOut("ADDR1_1", student._addr1);
                }
            }
        }
        if (!"1".equals(student._s_no_tel)) {
            svf.VrsOut("TEL_NO", student._telno);
        }
        if (!"1".equals(student._s_no_birth)) {
            svf.VrsOut("BIRTHDAY", student._birthdayFormat);
        }
    }

    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }
    
    private String getSchoolKind(final DB2UDB db2, final String selectCd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String schoolKind = null;
        try {
            final String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _param._year + "' AND GRADE = '" + selectCd.substring(0, 2) + "'";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                schoolKind = rs.getString("SCHOOL_KIND");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return schoolKind;
    }

    private List createStudentInfoData(final DB2UDB db2, final Param param, final String selectCd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(selectCd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("PREF_CD"),
                        rs.getString("PREF_NAME"),
                        rs.getString("AREACD"),
                        rs.getString("AREA_NAME"),
                        rs.getString("SCHREGNO"),
                        rs.getString("ATTENDNO"),
                        rs.getString("NAME"),
                        rs.getString("NAME_KANA"),
                        rs.getString("KYODAKU_FLG"),
                        rs.getString("SEX"),
                        rs.getString("SEX_NAME"),
                        rs.getString("BIRTHDAY"),
                        rs.getString("GUARD_NAME"),
                        rs.getString("GUARD_KANA"),
                        rs.getString("ZIPCD"),
                        rs.getString("ADDR1"),
                        rs.getString("ADDR2"),
                        rs.getString("TELNO"),
                        rs.getString("TR_CD1"),
                        rs.getString("TR_NAME1"),
                        rs.getString("SUBTR_CD1"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_CD2"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("S_NO_NAME"),
                        rs.getString("S_NO_ADDR"),
                        rs.getString("S_NO_TEL"),
                        rs.getString("S_NO_BIRTH"),
                        rs.getString("H_NO_NAME"),
                        rs.getString("H_NO_ADDR"),
                        rs.getString("H_NO_TEL"),
                        rs.getString("H_NO_BIRTH")
                );
                rtnList.add(studentInfo);
            }
            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
            	final Student studentInfo = (Student) it.next();
                studentInfo._birthdayFormat = formatDate(db2, param, studentInfo._birthday);
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
        stb.append("     ) ");
        stb.append(" , SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ZIPCD, ");
        stb.append("         P1.PREF_CD, ");
        stb.append("         P1.PREF_NAME, ");
        stb.append("         T1.AREACD, ");
        stb.append("         N1.NAME1 AS AREA_NAME, ");
        stb.append("         T1.ADDR1, ");
        stb.append("         T1.ADDR2, ");
        stb.append("         T1.TELNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
        stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.NAME_KANA, ");
        stb.append("     T6.BASE_REMARK1 AS KYODAKU_FLG, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T4.GUARD_NAME, ");
        stb.append("     T4.GUARD_KANA, ");
        stb.append("     T5.ZIPCD, ");
        stb.append("     T5.PREF_CD, ");
        stb.append("     T5.PREF_NAME, ");
        stb.append("     T5.AREACD, ");
        stb.append("     T5.AREA_NAME, ");
        stb.append("     T5.ADDR1, ");
        stb.append("     T5.ADDR2, ");
        stb.append("     T5.TELNO, ");
        stb.append("     T3.BIRTHDAY, ");
        stb.append("     T2.TR_CD1, ");
        stb.append("     S1.STAFFNAME AS TR_NAME1, ");
        stb.append("     T2.SUBTR_CD1, ");
        stb.append("     S2.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     T2.SUBTR_CD2, ");
        stb.append("     S3.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     LICENSE_GS1.REMARK1 AS S_NO_NAME, ");
        stb.append("     LICENSE_GS1.REMARK2 AS S_NO_ADDR, ");
        stb.append("     LICENSE_GS1.REMARK3 AS S_NO_TEL, ");
        stb.append("     LICENSE_GS1.REMARK4 AS S_NO_BIRTH, ");
        stb.append("     LICENSE_GS2.REMARK1 AS H_NO_NAME, ");
        stb.append("     LICENSE_GS2.REMARK2 AS H_NO_ADDR, ");
        stb.append("     LICENSE_GS2.REMARK3 AS H_NO_TEL, ");
        stb.append("     LICENSE_GS2.REMARK4 AS H_NO_BIRTH ");
        stb.append(" FROM ");
        stb.append("     " + _param._tableRegdDat + " T1 ");
        stb.append("     INNER JOIN " + _param._tableRegdHDat + " T2 ");
        stb.append("         ON  T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.GRADE = T1.GRADE ");
        stb.append("         AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T2.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST S2 ON S2.STAFFCD = T2.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST S3 ON S3.STAFFCD = T2.SUBTR_CD2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.BASE_SEQ = '006' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_PRGID_DAT LICENSE_GM ON LICENSE_GM.PRGID = 'KNJA171E' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_STD_DAT LICENSE_GS1 ON LICENSE_GM.GROUP_DIV = LICENSE_GS1.GROUP_DIV ");
        stb.append("          AND LICENSE_GS1.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND LICENSE_GS1.SELECT_DIV = '1' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_STD_DAT LICENSE_GS2 ON LICENSE_GM.GROUP_DIV = LICENSE_GS2.GROUP_DIV ");
        stb.append("          AND LICENSE_GS2.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND LICENSE_GS2.SELECT_DIV = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selectCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒 */
    private class Student {
		final String _grade;
        final String _hrclass;
        final String _hrName;
        final String _prefcd;
        final String _prefname;
        final String _areacd;
        final String _areaname;
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _namekana;
        final String _kyodakuFlg;
        final String _sex;
        final String _sexname;
        final String _birthday;
        final String _guardname;
        final String _guardkana;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final String _trcd1;
        final String _trname1;
        final String _subtrcd1;
        final String _subtrname1;
        final String _subtrcd2;
        final String _subtrname2;
        final String _s_no_name;
        final String _s_no_addr;
        final String _s_no_tel;
        final String _s_no_birth;
        final String _h_no_name;
        final String _h_no_addr;
        final String _h_no_tel;
        final String _h_no_birth;
        private String _birthdayFormat;

        Student(
                final String grade,
                final String hrclass,
                final String hrName,
                final String prefcd,
                final String prefname,
                final String areacd,
                final String areaname,
                final String schregno,
                final String attendno,
                final String name,
                final String namekana,
                final String kyodakuFlg,
                final String sex,
                final String sexname,
                final String birthday,
                final String guardname,
                final String guardkana,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String trcd1,
                final String trname1,
                final String subtrcd1,
                final String subtrname1,
                final String subtrcd2,
                final String subtrname2,
                final String s_no_name,
                final String s_no_addr,
                final String s_no_tel,
                final String s_no_birth,
                final String h_no_name,
                final String h_no_addr,
                final String h_no_tel,
                final String h_no_birth
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _hrName = hrName;
            _prefcd = prefcd;
            _prefname = prefname;
            _areacd = areacd;
            _areaname = areaname;
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _namekana = namekana;
            _kyodakuFlg = kyodakuFlg;
            _sex = sex;
            _sexname = sexname;
            _birthday = birthday;
            _guardname = guardname;
            _guardkana = guardkana;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _trcd1 = trcd1;
            _trname1 = trname1;
            _subtrcd1 = subtrcd1;
            _subtrname1 = subtrname1;
            _subtrcd2 = subtrcd2;
            _subtrname2 = subtrname2;
            _s_no_name = s_no_name;
            _s_no_addr = s_no_addr;
            _s_no_tel = s_no_tel;
            _s_no_birth = s_no_birth;
            _h_no_name = h_no_name;
            _h_no_addr = h_no_addr;
            _h_no_tel = h_no_tel;
            _h_no_birth = h_no_birth;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 73002 $ $Date: 2020-03-16 17:37:04 +0900 (月, 16 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String[] _classSelected;
        final String _ctrlDate;
        final String _printClass;
        final String _tableRegdDat;
        final String _tableRegdHDat;
        final boolean _isSeireki;
        final String _ctrlDateFormat;
        final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            //1:法定クラス 2:複式クラス
            _printClass = request.getParameter("HR_CLASS_TYPE");
            _isSeireki = KNJ_EditDate.isSeireki(db2);
            _ctrlDateFormat = formatDate(db2, this, _ctrlDate);
            if ("2".equals(_printClass)) {
                _tableRegdDat = "SCHREG_REGD_FI_DAT";
                _tableRegdHDat = "SCHREG_REGD_FI_HDAT";
            } else {
                _tableRegdDat = "SCHREG_REGD_DAT";
                _tableRegdHDat = "SCHREG_REGD_HDAT";
            }
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
        }
    }

}// クラスの括り

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
public class KNJA172 {

    private static final Log log = LogFactory.getLog(KNJA172.class);
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
                final List studentList = createStudentInfoData(db2, _param._classSelected[i]);
                if (printMain(svf, studentList)) { // 生徒出力のメソッド
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

    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
        boolean hasData = false;

        svf.VrSetForm("KNJA172.frm", 4);
        int gyo = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            gyo++;
            printStudent(svf, gyo, student);
            svf.VrEndRecord();
            hasData = true;
        }

        return  hasData;
    }
    
    private void printStudent(final Vrw32alp svf, final int gyo, final Student student) {
        svf.VrsOut("NENDO", _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolDatSchoolName) + " 学籍簿");
        svf.VrsOut("YMD", _param._ctrlDateFormat);
        //1:クラス 2:都道府県 3:市町村
        if ("1".equals(_param._printRadio)) {
            svf.VrsOut("SELECT_DIV", student._hrName);
        }
        if ("2".equals(_param._printRadio)) {
            svf.VrsOut("SELECT_DIV", student._prefname);
        }
        if ("3".equals(_param._printRadio)) {
            svf.VrsOut("SELECT_DIV", student._areaname);
        }
        
        svf.VrsOut("SCHREG_NO", student._schregno);
        svf.VrsOut("ATTEND_NO", student._hrName);
        final int nameKeta = getMS932ByteLength(student._name);
        svf.VrsOut("NAME" + (nameKeta <= 24 ? "" : nameKeta <= 30 ? "2" : "3"), student._name);
        final int nameKanaKeta = getMS932ByteLength(student._namekana);
        svf.VrsOut("seito_kana" + (nameKanaKeta <= 32 ? "" : nameKanaKeta <= 40 ? "2" : "3"), student._namekana);
        svf.VrsOut("SEX", student._sexname);
        svf.VrsOut("BIRTHDAY", student._birthdayFormat);
        svf.VrsOut("GUARD_NAME", student._guardname);
        svf.VrsOut("ENT_DIV", student._entname);
        svf.VrsOut("ENT_DATE", student._entdateFormat);
        svf.VrsOut("GRD_DIV", student._grdname);
        svf.VrsOut("GRD_DATE", student._grddateFormat);
    }
    
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
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
                        rs.getString("SEX"),
                        rs.getString("SEX_NAME"),
                        rs.getString("BIRTHDAY"),
                        rs.getString("GUARD_NAME"),
                        rs.getString("ENT_DIV"),
                        rs.getString("ENT_NAME"),
                        rs.getString("ENT_DATE"),
                        rs.getString("GRD_DIV"),
                        rs.getString("GRD_NAME"),
                        rs.getString("GRD_DATE")
                );
                rtnList.add(studentInfo);
            }
            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
            	final Student student = (Student) it.next();
            	student._birthdayFormat = KNJ_EditDate.getAutoFormatDate(db2, student._birthday);
            	student._entdateFormat = KNJ_EditDate.getAutoFormatDate(db2, student._entdate);
            	student._grddateFormat = KNJ_EditDate.getAutoFormatDate(db2, student._grddate);
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
        stb.append("         P1.PREF_CD, ");
        stb.append("         P1.PREF_NAME, ");
        stb.append("         T1.AREACD, ");
        stb.append("         N1.NAME1 AS AREA_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
        stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
        stb.append("     ) ");
        stb.append(" , NO_REGD_PARAM_SEMES AS ( "); // 指定学期前に転退学して指定学期に在籍データが無い生徒の最大学期
        stb.append("     SELECT ");
        stb.append("         T2.* ");
        stb.append("     FROM ");
        stb.append("     (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
        stb.append("      FROM " + _param._tableRegdDat + " ");
        stb.append("      GROUP BY SCHREGNO, YEAR) T1 ");
        stb.append("      INNER JOIN " + _param._tableRegdDat + " T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND T2.YEAR = T1.YEAR ");
        stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("          AND GDAT.GRADE = T2.GRADE ");
        stb.append("      INNER JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append("      INNER JOIN SEMESTER_MST GRDDATE_SEME ON GRDDATE_SEME.YEAR = T1.YEAR ");
        stb.append("          AND GRDDATE_SEME.SEMESTER <> '9' ");
        stb.append("          AND ENTGRD.GRD_DATE BETWEEN GRDDATE_SEME.SDATE AND GRDDATE_SEME.EDATE ");
        stb.append("      LEFT JOIN " + _param._tableRegdDat + " L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND L1.SCHREGNO IS NULL ");
        stb.append("         AND GRDDATE_SEME.SEMESTER < '" + _param._semester + "' ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T5.PREF_CD, ");
        stb.append("     T5.PREF_NAME, ");
        stb.append("     T5.AREACD, ");
        stb.append("     T5.AREA_NAME, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.NAME_KANA, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T3.BIRTHDAY, ");
        stb.append("     T4.GUARD_NAME, ");
        stb.append("     T3.ENT_DIV, ");
        stb.append("     N2.NAME1 AS ENT_NAME, ");
        stb.append("     T3.ENT_DATE, ");
        stb.append("     T3.GRD_DIV, ");
        stb.append("     N3.NAME1 AS GRD_NAME, ");
        stb.append("     T3.GRD_DATE ");
        stb.append(" FROM ");
        stb.append("     (SELECT * FROM " + _param._tableRegdDat + " T1 ");
        stb.append("         WHERE T1.YEAR = '" + _param._year + "' ");
        stb.append("           AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("      UNION ALL ");
        stb.append("      SELECT * FROM NO_REGD_PARAM_SEMES ");
        stb.append("      ) T1 ");
        stb.append("     INNER JOIN " + _param._tableRegdHDat + " T2 ");
        stb.append("         ON  T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.GRADE = T1.GRADE ");
        stb.append("         AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("          AND GDAT.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' AND N2.NAMECD2 = ENTGRD.ENT_DIV ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'A003' AND N3.NAMECD2 = ENTGRD.GRD_DIV ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        //1:クラス 2:都道府県 3:市町村
        if ("1".equals(_param._printRadio)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selectCd + "' ");
        }
        if ("2".equals(_param._printRadio)) {
            stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
            stb.append("     AND T5.PREF_CD = '" + selectCd + "' ");
        }
        if ("3".equals(_param._printRadio)) {
            stb.append("     AND T5.AREACD = '" + selectCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO ");
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
        final String _sex;
        final String _sexname;
        final String _birthday;
        final String _guardname;
        final String _entdiv;
        final String _entname;
        final String _entdate;
        final String _grddiv;
        final String _grdname;
        final String _grddate;
        String _birthdayFormat;
        String _entdateFormat;
        String _grddateFormat;
        
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
                final String sex,
                final String sexname,
                final String birthday,
                final String guardname,
                final String entdiv,
                final String entname,
                final String entdate,
                final String grddiv,
                final String grdname,
                final String grddate
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
            _sex = sex;
            _sexname = sexname;
            _birthday = birthday;
            _guardname = guardname;
            _entdiv = entdiv;
            _entname = entname;
            _entdate = entdate;
            _grddiv = grddiv;
            _grdname = grdname;
            _grddate = grddate;
        }
    }
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 67431 $ $Date: 2019-05-13 21:05:15 +0900 (月, 13 5 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _printRadio;
        final String _grade;
        final String[] _classSelected;
        final String _ctrlDate;
        final String _printClass;
        final String _tableRegdDat;
        final String _tableRegdHDat;
        final String _certifSchoolDatSchoolName;
        final String _nendo;
        final String _ctrlDateFormat;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _printRadio = request.getParameter("PRINT_RADIO");
            _grade  = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            //1:法定クラス 2:複式クラス
            _printClass = request.getParameter("HR_CLASS_TYPE");
            if ("2".equals(_printClass)) {
                _tableRegdDat = "SCHREG_REGD_FI_DAT";
                _tableRegdHDat = "SCHREG_REGD_FI_HDAT";
            } else {
                _tableRegdDat = "SCHREG_REGD_DAT";
                _tableRegdHDat = "SCHREG_REGD_HDAT";
            }
            _certifSchoolDatSchoolName = getCertifSchoolDatSchoolName(db2);
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _ctrlDateFormat = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate);
        }
        
        private String getCertifSchoolDatSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String certifKindCd = "131";
            String rtn = null;
            try {
                final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT"
                    + " WHERE YEAR='" + _year + "'"
                    + " AND CERTIF_KINDCD='" + certifKindCd + "'";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (final SQLException e) {
                log.error("学校名称取得エラー:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
    
}// クラスの括り

package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB247 {

    private static final Log log = LogFactory.getLog(KNJB247.class);
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

            for (int i = 0; i < _param._categorySelected.length; i++) {

                // 生徒データを取得
                final List hrClassList = createHrClassInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, hrClassList)) { // 生徒出力のメソッド
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

    /**
     * 生徒の出力
     */
    private boolean printMain(final Vrw32alp svf, final List hrClassList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            if (hrClass._studentList.size() > 0) {
                if (printStudent(svf, hrClass)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    private boolean printStudent(final Vrw32alp svf, final HrClass hrClass) {
        boolean hasData = false;

        for (final Iterator it = hrClass._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            svf.VrSetForm("KNJB247.frm", 4);
            svf.VrsOut("TITLE", "生徒別　欠席状況一覧");
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            svf.VrsOut("DATE", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
            svf.VrsOut("NENDO", _param._year + "年度");
            svf.VrsOut("TR_NAME1" + ((30 < getMS932ByteLength(hrClass.getTrName())) ? "_2" : ""), hrClass.getTrName());
            svf.VrsOut("SUBTR_NAME1" + ((30 < getMS932ByteLength(hrClass._subtrName1)) ? "_2" : ""), hrClass._subtrName1);
            svf.VrsOut("SUBTR_NAME2" + ((30 < getMS932ByteLength(hrClass._subtrName2)) ? "_2" : ""), hrClass._subtrName2);
            svf.VrsOut("HR_NAME", hrClass._hrName);
            final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
            svf.VrsOut("ATTENDNO", attendno);
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME1" + ((30 < getMS932ByteLength(student._name)) ? "_2" : ""), student._name);
            int sickCnt = 0;
            String dicdname = "";
            String remark = "";
            String seqC = "";
            String seqR = "";
            String attenddateKeep = "";
            for (final Iterator it2 = student._attendDayDatList.iterator(); it2.hasNext();) {
                final AttendDayDat attendDayDat = (AttendDayDat) it2.next();
                if (!attenddateKeep.equals(attendDayDat._attenddate) && !"".equals(attenddateKeep)) {
                    svf.VrEndRecord();
                    hasData = true;
                    dicdname = "";
                    remark = "";
                    seqC = "";
                    seqR = "";
                }
                svf.VrsOut("MONTH", attendDayDat._month + "月");
                svf.VrsOut("DAY", attendDayDat._day);
                svf.VrsOut("WEEK", KNJ_EditDate.h_format_W(attendDayDat._attenddate));
                if (attendDayDat._dicdname != null) {
                    dicdname += seqC + attendDayDat._dicdname;
                    seqC = "・";
                }
                if (attendDayDat._diremark != null) {
                    remark += seqR + attendDayDat._diremark;
                    seqR = "・";
                }
                svf.VrsOut("REASON", dicdname);
                svf.VrsOut("REMARK", remark);
                attenddateKeep = attendDayDat._attenddate;
                if ("4".equals(attendDayDat._dicd) || "5".equals(attendDayDat._dicd) || "6".equals(attendDayDat._dicd) || "11".equals(attendDayDat._dicd) || "12".equals(attendDayDat._dicd) || "13".equals(attendDayDat._dicd)) {
                    sickCnt++;
                }
            }
            if (!"".equals(attenddateKeep)) {
                svf.VrsOut("SICK", String.valueOf(sickCnt));
                svf.VrEndRecord();
                hasData = true;
            }
        }

        return hasData;
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private List createHrClassInfoData(final DB2UDB db2, final String gradehrclass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getHrClassInfoSql(gradehrclass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final HrClass hrClassInfo = new HrClass(
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("HR_NAMEABBV"),
                        rs.getString("TR_NAME1"),
                        rs.getString("TR_NAME2"),
                        rs.getString("TR_NAME3"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("SUBTR_NAME3")
                );
                rtnList.add(hrClassInfo);
                hrClassInfo._studentList = hrClassInfo.createStudentInfoData(db2, hrClassInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getHrClassInfoSql(final String gradehrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     L1.STAFFNAME AS TR_NAME1, ");
        stb.append("     L2.STAFFNAME AS TR_NAME2, ");
        stb.append("     L3.STAFFNAME AS TR_NAME3, ");
        stb.append("     L4.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     L5.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     L6.STAFFNAME AS SUBTR_NAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L5 ON L5.STAFFCD = T1.SUBTR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L6 ON L6.STAFFCD = T1.SUBTR_CD3 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradehrclass + "' ");
        return stb.toString();
    }

    /** 年組 */
    private class HrClass {
        final String _grade;
        final String _hrclass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _trName1;
        final String _trName2;
        final String _trName3;
        final String _subtrName1;
        final String _subtrName2;
        final String _subtrName3;
        List _studentList = new ArrayList();

        HrClass(
                final String grade,
                final String hrclass,
                final String hrName,
                final String hrNameAbbv,
                final String trName1,
                final String trName2,
                final String trName3,
                final String subtrName1,
                final String subtrName2,
                final String subtrName3
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _trName1 = trName1;
            _trName2 = trName2;
            _trName3 = trName3;
            _subtrName1 = subtrName1;
            _subtrName2 = subtrName2;
            _subtrName3 = subtrName3;
        }

        private String getTrName() {
            String rtnName = "";
            String seq = "";
            if (_trName1 != null) {
                rtnName += seq + _trName1;
                seq = ",";
            }
            if (_trName2 != null) {
                rtnName += seq + _trName2;
                seq = ",";
            }
            if (_trName3 != null) {
                rtnName += seq + _trName3;
                seq = ",";
            }
            return rtnName;
        }

        private String getSubTrName() {
            String rtnName = "";
            String seq = "";
            if (_subtrName1 != null) {
                rtnName += seq + _subtrName1;
                seq = ",";
            }
            if (_subtrName2 != null) {
                rtnName += seq + _subtrName2;
                seq = ",";
            }
            if (_subtrName3 != null) {
                rtnName += seq + _subtrName3;
                seq = ",";
            }
            return rtnName;
        }

        private List createStudentInfoData(final DB2UDB db2, final HrClass hrClass) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStudentInfoSql(hrClass);
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student studentInfo = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("SEX_NAME"),
                            rs.getString("ATTENDNO")
                    );
                    rtnList.add(studentInfo);
                    studentInfo._attendDayDatList = studentInfo.createAttendDayDatInfoData(db2, studentInfo._schregno);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }

    private String getStudentInfoSql(final HrClass hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T2.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + hrClass._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass._hrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _attendno;
        List _attendDayDatList = new ArrayList();

        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String attendno
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _attendno = attendno;
        }

        private List createAttendDayDatInfoData(final DB2UDB db2, final String schregno) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getAttendDayDatInfoSql(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final AttendDayDat attendDayDatInfo = new AttendDayDat(
                            rs.getString("SEMESTER"),
                            rs.getString("SEMESTERNAME"),
                            rs.getString("MONTH"),
                            rs.getString("DAY"),
                            rs.getString("SCHREGNO"),
                            rs.getString("ATTENDDATE"),
                            rs.getString("DI_CD"),
                            rs.getString("DI_CD_NAME"),
                            rs.getString("DI_CD_ABBV"),
                            rs.getString("DI_REMARK")
                            );
                    rtnList.add(attendDayDatInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }

    private String getAttendDayDatInfoSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     S1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.ATTENDDATE) AS MONTH, ");
        stb.append("     DAY(T1.ATTENDDATE) AS DAY, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.DI_CD, ");
        stb.append("     N1.NAME1 AS DI_CD_NAME, ");
        stb.append("     N1.ABBV1 AS DI_CD_ABBV, ");
        stb.append("     T1.DI_REMARK ");
        stb.append(" FROM ");
        stb.append("     ATTEND_DAY_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'C001' AND N1.NAMECD2 = T1.DI_CD ");
        stb.append("     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER != '9' AND T1.ATTENDDATE BETWEEN S1.SDATE AND S1.EDATE ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = S1.YEAR ");
        stb.append("         AND REGD.SEMESTER = S1.SEMESTER ");
        stb.append("         AND REGD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
        stb.append("         AND GDAT.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.ATTENDDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND (EGHIST.ENT_DATE IS NULL OR EGHIST.ENT_DATE <= T1.ATTENDDATE)");
        stb.append("     AND (EGHIST.GRD_DATE IS NULL OR EGHIST.GRD_DIV <> '4' AND T1.ATTENDDATE <= EGHIST.GRD_DATE) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDDATE DESC, ");
        stb.append("     T1.DI_CD ");
        return stb.toString();
    }

    /** 出欠 */
    private class AttendDayDat {
        final String _semester;
        final String _semestername;
        final String _month;
        final String _day;
        final String _schregno;
        final String _attenddate;
        final String _dicd;
        final String _dicdname;
        final String _dicdabbv;
        final String _diremark;

        AttendDayDat(
                final String semester,
                final String semestername,
                final String month,
                final String day,
                final String schregno,
                final String attenddate,
                final String dicd,
                final String dicdname,
                final String dicdabbv,
                final String diremark
        ) {
            _semester = semester;
            _semestername = semestername;
            _month = month;
            _day = day;
            _schregno = schregno;
            _attenddate = attenddate;
            _dicd = dicd;
            _dicdname = dicdname;
            _dicdabbv = dicdabbv;
            _diremark = diremark;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _grade;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _grade  = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }
    }

}// クラスの括り

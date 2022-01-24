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
public class KNJB248 {

    private static final Log log = LogFactory.getLog(KNJB248.class);
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
            if (hrClass._attendList.size() > 0) {
                if (printAttend(svf, hrClass)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    private boolean printAttend(final Vrw32alp svf, final HrClass hrClass) {
        boolean hasData = false;

        String monthKeep = "";
        List monthList = new ArrayList();
        List curentMonthAttendList = new ArrayList();
        for (final Iterator it = hrClass._attendList.iterator(); it.hasNext();) {
            final Attend attend = (Attend) it.next();
            if (!monthKeep.equals(attend._semester + attend._month)) {
                curentMonthAttendList = new ArrayList();
                monthList.add(curentMonthAttendList);
                monthKeep = attend._semester + attend._month;
            }
            curentMonthAttendList.add(attend);
        }

        for (final Iterator it = monthList.iterator(); it.hasNext();) {
            final List monthAttendList = (List) it.next();
            svf.VrSetForm("KNJB248.frm", 4);
            svf.VrsOut("TITLE", "クラス別　月単位　欠席状況一覧");
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            svf.VrsOut("NENDO", _param._year + "年度");
            svf.VrsOut("TR_NAME1" + ((30 < getMS932ByteLength(hrClass.getTrName())) ? "_2" : ""), hrClass.getTrName());
            svf.VrsOut("SUBTR_NAME1" + ((30 < getMS932ByteLength(hrClass._subtrName1)) ? "_2" : ""), hrClass._subtrName1);
            svf.VrsOut("SUBTR_NAME2" + ((30 < getMS932ByteLength(hrClass._subtrName2)) ? "_2" : ""), hrClass._subtrName2);
            svf.VrsOut("HR_NAME", hrClass._hrName);
            svf.VrsOut("TOTAL_CNT", hrClass._cnt);
            String dicdname = "";
            String remark = "";
            String seqC = "";
            String seqR = "";
            String attenddateKeep = "";
            for (final Iterator it2 = monthAttendList.iterator(); it2.hasNext();) {
                final Attend attend = (Attend) it2.next();
                if (!attenddateKeep.equals(attend._attenddate + attend._schregno) && !"".equals(attenddateKeep)) {
                    svf.VrEndRecord();
                    hasData = true;
                    dicdname = "";
                    remark = "";
                    seqC = "";
                    seqR = "";
                }
                final String attendno = (NumberUtils.isDigits(attend._attendno)) ? String.valueOf(Integer.parseInt(attend._attendno)) : "";
                svf.VrsOut("ATTENDNO", attendno);
                svf.VrsOut("SCHREGNO", attend._schregno);
                svf.VrsOut("NAME1" + ((30 < getMS932ByteLength(attend._name)) ? "_2" : ""), attend._name);

                svf.VrsOut("SEMESTER", attend._semestername);
                svf.VrsOut("MONTH", attend._month + "月");
                svf.VrsOut("DAY", attend._day);
                for (int i = 1; i <= 5; i++) {
                    svf.VrsOut("DAY_SHIRO" + i, attend._day);
                }
                svf.VrsOut("WEEK", "(" + KNJ_EditDate.h_format_W(attend._attenddate) + ")");

                if (attend._dicdname != null) {
                    dicdname += seqC + attend._dicdname;
                    seqC = "・";
                }
                if (attend._diremark != null) {
                    remark += seqR + attend._diremark;
                    seqR = "・";
                }
                svf.VrsOut("REASON", dicdname);
                svf.VrsOut("FIELD1", remark);

                attenddateKeep = attend._attenddate + attend._schregno;
            }
            if (!"".equals(attenddateKeep)) {
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
                        rs.getString("CNT"),
                        rs.getString("TR_NAME1"),
                        rs.getString("TR_NAME2"),
                        rs.getString("TR_NAME3"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("SUBTR_NAME3")
                );
                rtnList.add(hrClassInfo);
                hrClassInfo._attendList = hrClassInfo.createAttendInfoData(db2, hrClassInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getHrClassInfoSql(final String gradehrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         COUNT(T1.SCHREGNO) AS CNT ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradehrclass + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     T2.CNT, ");
        stb.append("     L1.STAFFNAME AS TR_NAME1, ");
        stb.append("     L2.STAFFNAME AS TR_NAME2, ");
        stb.append("     L3.STAFFNAME AS TR_NAME3, ");
        stb.append("     L4.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     L5.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     L6.STAFFNAME AS SUBTR_NAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN T_CNT T2 ON T2.GRADE = T2.GRADE AND T2.HR_CLASS = T2.HR_CLASS ");
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
        final String _cnt;
        final String _trName1;
        final String _trName2;
        final String _trName3;
        final String _subtrName1;
        final String _subtrName2;
        final String _subtrName3;
        List _attendList = new ArrayList();

        HrClass(
                final String grade,
                final String hrclass,
                final String hrName,
                final String hrNameAbbv,
                final String cnt,
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
            _cnt = cnt;
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

        private List createAttendInfoData(final DB2UDB db2, final HrClass hrClass) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getAttendInfoSql(hrClass);
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Attend attendInfo = new Attend(
                            rs.getString("SCHREGNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("SEX_NAME"),
                            rs.getString("ATTENDNO"),
                            rs.getString("SEMESTER"),
                            rs.getString("SEMESTERNAME"),
                            rs.getString("MONTH"),
                            rs.getString("DAY"),
                            rs.getString("ATTENDDATE"),
                            rs.getString("DI_CD"),
                            rs.getString("DI_CD_NAME"),
                            rs.getString("DI_CD_ABBV"),
                            rs.getString("DI_REMARK")
                    );
                    rtnList.add(attendInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }

    private String getAttendInfoSql(final HrClass hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     S1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T3.ATTENDDATE) AS MONTH, ");
        stb.append("     DAY(T3.ATTENDDATE) AS DAY, ");
        stb.append("     T3.ATTENDDATE, ");
        stb.append("     T3.DI_CD, ");
        stb.append("     N2.NAME1 AS DI_CD_NAME, ");
        stb.append("     N2.ABBV1 AS DI_CD_ABBV, ");
        stb.append("     T3.DI_REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T2.SEX ");
        stb.append("     INNER JOIN ATTEND_DAY_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T3.ATTENDDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("             AND T3.YEAR = '" + _param._year + "' ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'C001' AND N2.NAMECD2 = T3.DI_CD ");
        stb.append("     INNER JOIN SEMESTER_MST S1 ON S1.YEAR = T3.YEAR AND S1.SEMESTER != '9' AND T3.ATTENDDATE BETWEEN S1.SDATE AND S1.EDATE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + hrClass._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass._hrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T3.ATTENDDATE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.DI_CD ");
        return stb.toString();
    }

    /** 出欠・生徒 */
    private class Attend {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _attendno;
        final String _semester;
        final String _semestername;
        final String _month;
        final String _day;
        final String _attenddate;
        final String _dicd;
        final String _dicdname;
        final String _dicdabbv;
        final String _diremark;

        Attend(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String attendno,
                final String semester,
                final String semestername,
                final String month,
                final String day,
                final String attenddate,
                final String dicd,
                final String dicdname,
                final String dicdabbv,
                final String diremark
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _attendno = attendno;
            _semester = semester;
            _semestername = semestername;
            _month = month;
            _day = day;
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

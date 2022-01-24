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
public class KNJB236 {

    private static final Log log = LogFactory.getLog(KNJB236.class);
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

            printMain(db2, svf);

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
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List classMstList = createClassMstInfoData(db2);
        for (final Iterator it = classMstList.iterator(); it.hasNext();) {
            final ClassMst classMst = (ClassMst) it.next();
            final List kouzaList = createKouzaInfoData(db2, classMst._classcd);
            if (kouzaList.size() > 0) {
                printDataList(svf, kouzaList, classMst);
            }
        }
    }

    private void printDataList(final Vrw32alp svf, final List kouzaList, final ClassMst classMst) {
        svf.VrSetForm("KNJB236.frm", 4);

        String subclasscdKeep = "";
        int grpNo = 0;
        for (final Iterator it2 = kouzaList.iterator(); it2.hasNext();) {
            final Kouza kouza = (Kouza) it2.next();
            svf.VrsOut("TITLE", "教科別　開講講座情報一覧");
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            svf.VrsOut("NENDO", _param._year + "年度　" + _param._semestername);
            svf.VrsOut("CLASS_CD", classMst._classcd);
            svf.VrsOut("CLASS_NAME", classMst._classname);
            final int kensuu = kouzaList.size();
            svf.VrsOut("UNREGIST", "開講講座数：　" + String.valueOf(kensuu) + "講座");
            final String fieldLen1 = (26 < getMS932ByteLength(kouza._subclassname)) ? "2" : "1";
            final String fieldLen2 = (26 < getMS932ByteLength(kouza._chairname)) ? "2" : "1";
            if (!subclasscdKeep.equals(kouza._subclasscd)) {
                subclasscdKeep = kouza._subclasscd;
                grpNo++;
                svf.VrsOut("SUBCLASS_NAME" + fieldLen1, kouza._subclassname);
            }
            svf.VrsOut("GRP1", String.valueOf(grpNo));
            svf.VrsOut("SUBCLASS_CD", kouza._subclasscd);
            svf.VrsOut("CHAIRCD", kouza._chaircd);
            svf.VrsOut("CHAIR_NAME" + fieldLen2, kouza._chairname);
            if ("J".equals(kouza._schoolKind)) {
                svf.VrsOut("CREDIT", "なし");
            }
            svf.VrsOut("COMP", kouza._takesemesName);
            for (final Iterator it = kouza._chairStdList.iterator(); it.hasNext();) {
                final ChairStd chairStd = (ChairStd) it.next();
                if ("J".equals(kouza._schoolKind)) {
                    svf.VrsOut("CREDIT", "なし");
                } else {
                    svf.VrsOut("CREDIT", (NumberUtils.isDigits(chairStd._credits)) ? chairStd._credits : "");
                }
                svf.VrsOut("STUDENT_NUM", (NumberUtils.isDigits(chairStd._cnt)) ? chairStd._cnt : "");
            }
            String sch_ptrn = "";
            String seqP = "";
            for (final Iterator it = kouza._schPtrnList.iterator(); it.hasNext();) {
                final SchPtrn schPtrn = (SchPtrn) it.next();
                sch_ptrn += seqP + schPtrn._daycdname + schPtrn._periodabbv;
                seqP = ",";
            }
            svf.VrsOut("SCH_CHAIR", sch_ptrn);
            String staffM = "";
            String seqM = "";
            int cntStaffS = 0;
            for (final Iterator it = kouza._staffList.iterator(); it.hasNext();) {
                final Staff staff = (Staff) it.next();
                if ("1".equals(staff._chargediv)) {
                    staffM += seqM + staff._staffname;
                    seqM = ",";
                } else {
                    cntStaffS++;
                }
            }
            final String fieldLenM = (30 < getMS932ByteLength(staffM)) ? "2" : "1";
            svf.VrsOut("TEACHER_NAME" + fieldLenM, staffM);
            svf.VrsOut("SUB_TEACHER", (0 < cntStaffS) ? String.valueOf(cntStaffS) : "");
            String hrNameAbbv = "";
            String seq = "";
            for (final Iterator it = kouza._chairClsList.iterator(); it.hasNext();) {
                final ChairCls chairCls = (ChairCls) it.next();
                hrNameAbbv += seq + chairCls._hrNameAbbv;
                seq = ",";
            }
            svf.VrsOut("ATTEND", hrNameAbbv);

            svf.VrEndRecord();
            _hasData = true;
        }
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

    private List createClassMstInfoData(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getClassMstInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final ClassMst classMst = new ClassMst(
                        rs.getString("CLASSCD"),
                        rs.getString("CLASSNAME")
                        );
                rtnList.add(classMst);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getClassMstInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" SCHOOL_KIND, ");
            stb.append(" CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ");
        } else {
            stb.append(" CLASSCD AS CLASSCD, ");
        }
        stb.append("     CLASSNAME ");
        stb.append(" FROM ");
        stb.append("     V_CLASS_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" SCHOOL_KIND, ");
        }
        stb.append("     CLASSCD ");
        return stb.toString();
    }

    /** 教科マスタ */
    private class ClassMst {
        final String _classcd;
        final String _classname;

        ClassMst(
                final String classcd,
                final String classname
        ) {
            _classcd = classcd;
            _classname = classname;
        }
    }

    private List createKouzaInfoData(final DB2UDB db2, final String classcd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getKouzaInfoSql(classcd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Kouza kouzaInfo = new Kouza(
                        rs.getString("SCHOOL_KIND"),
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CHAIRCD"),
                        rs.getString("CHAIRNAME"),
                        rs.getString("GROUPCD"),
                        rs.getString("TAKESEMES_NAME")
                        );
                rtnList.add(kouzaInfo);
                kouzaInfo._schPtrnList = kouzaInfo.createSchPtrnInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._staffList = kouzaInfo.createStaffInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._chairClsList = kouzaInfo.createChairClsInfoData(db2, kouzaInfo._chaircd, kouzaInfo._groupcd);
                kouzaInfo._chairStdList = kouzaInfo.createChairStdInfoData(db2, kouzaInfo._chaircd, kouzaInfo._subclasscd);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getKouzaInfoSql(final String classcd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     '' AS SCHOOL_KIND, ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST T2 ");
        stb.append("             ON  T2.SUBCLASSCD = T1.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.TAKESEMES ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '" + classcd + "' ");
        } else {
            stb.append("     AND SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + classcd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    /** 講座データ */
    private class Kouza {
        final String _schoolKind;
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _groupcd;
        final String _takesemesName;
        List _schPtrnList = new ArrayList();
        List _staffList = new ArrayList();
        List _chairClsList = new ArrayList();
        List _chairStdList = new ArrayList();

        Kouza(
                final String schoolKind,
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String groupcd,
                final String takesemesName
        ) {
            _schoolKind = schoolKind;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _groupcd = groupcd;
            _takesemesName = takesemesName;
        }

        private List createSchPtrnInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            final String[] name = {null, "日", "月", "火", "水", "木", "金", "土"};
            try {
                final String sql = getSchPtrnInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String daycd = rs.getString("DAYCD");
                    final String daycdname = name[Integer.parseInt(daycd)];
                    final SchPtrn schPtrnInfo = new SchPtrn(
                            daycd,
                            daycdname,
                            rs.getString("PERIODCD"),
                            rs.getString("PERIOD_NAME"),
                            rs.getString("PERIOD_ABBV")
                            );
                    rtnList.add(schPtrnInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private List createStaffInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStaffInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Staff staffInfo = new Staff(
                            rs.getString("STAFFCD"),
                            rs.getString("STAFFNAME"),
                            rs.getString("CHARGEDIV")
                            );
                    rtnList.add(staffInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private List createChairClsInfoData(final DB2UDB db2, final String chaircd, final String groupcd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getChairClsInfoSql(chaircd, groupcd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final ChairCls chairClsInfo = new ChairCls(
                            rs.getString("CHAIRCD"),
                            rs.getString("GROUPCD"),
                            rs.getString("HR_CLASS"),
                            rs.getString("HR_NAME"),
                            rs.getString("HR_NAMEABBV")
                            );
                    rtnList.add(chairClsInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private List createChairStdInfoData(final DB2UDB db2, final String chaircd, final String subclasscd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getChairStdInfoSql(chaircd, subclasscd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final ChairStd chairStdInfo = new ChairStd(
                            rs.getString("CHAIRCD"),
                            rs.getString("CNT"),
                            rs.getString("CREDITS")
                            );
                    rtnList.add(chairStdInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }

    private String getSchPtrnInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     N1.NAME1 AS PERIOD_NAME, ");
        stb.append("     N1.ABBV1 AS PERIOD_ABBV ");
        stb.append(" FROM ");
        stb.append("     SCH_PTRN_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' AND N1.NAMECD2 = T1.PERIODCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.BSCSEQ = " + _param._bscseq + " ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    /** 基本時間割 */
    private class SchPtrn {
        final String _daycd;
        final String _daycdname;
        final String _periodcd;
        final String _periodname;
        final String _periodabbv;

        SchPtrn(
                final String daycd,
                final String daycdname,
                final String periodcd,
                final String periodname,
                final String periodabbv
        ) {
            _daycd = daycd;
            _daycdname = daycdname;
            _periodcd = periodcd;
            _periodname = periodname;
            _periodabbv = periodabbv;
        }
    }

    private String getStaffInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     T1.CHARGEDIV, ");
        stb.append("     T2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.STAFFCD ");
        return stb.toString();
    }

    /** 講座職員 */
    private class Staff {
        final String _staffcd;
        final String _staffname;
        final String _chargediv;

        Staff(
                final String staffcd,
                final String staffname,
                final String chargediv
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
            _chargediv = chargediv;
        }
    }

    private String getChairClsInfoSql(final String chaircd, final String groupcd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T2.GRADE || T2.HR_CLASS AS HR_CLASS, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     CHAIR_CLS_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ");
        stb.append("             ON  T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.GRADE = T1.TRGTGRADE ");
        stb.append("             AND T2.HR_CLASS = T1.TRGTCLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND (T1.CHAIRCD = '" + chaircd + "' OR T1.CHAIRCD = '0000000') ");
        stb.append("     AND T1.GROUPCD = '" + groupcd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HR_CLASS ");
        return stb.toString();
    }

    /** 講座受講クラス */
    private class ChairCls {
        final String _chaircd;
        final String _groupcd;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;

        ChairCls(
                final String chaircd,
                final String groupcd,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv
        ) {
            _chaircd = chaircd;
            _groupcd = groupcd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
        }
    }

    private String getChairStdInfoSql(final String chaircd, final String subclasscd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     COUNT(T1.SCHREGNO) AS CNT, ");
        stb.append("     MAX(L1.CREDITS) AS CREDITS ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN CREDIT_MST L1  ");
        stb.append("             ON  L1.YEAR = T2.YEAR ");
        stb.append("             AND L1.GRADE = T2.GRADE ");
        stb.append("             AND L1.COURSECD = T2.COURSECD ");
        stb.append("             AND L1.MAJORCD = T2.MAJORCD ");
        stb.append("             AND L1.COURSECODE = T2.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '" + subclasscd + "' ");
        } else {
            stb.append("         AND L1.SUBCLASSCD = '" + subclasscd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND DATE('" + _param._date + "') BETWEEN T1.APPDATE AND T1.APPENDDATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    /** 講座生徒名簿 */
    private class ChairStd {
        final String _chaircd;
        final String _cnt;
        final String _credits;

        ChairStd(
                final String chaircd,
                final String cnt,
                final String credits
        ) {
            _chaircd = chaircd;
            _cnt = cnt;
            _credits = credits;
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
        private String _semestername;
        final String _bscseq;
        final String _date;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _bscseq = request.getParameter("PTRN_TITLE");
            _date = request.getParameter("DATE") == null ? null : request.getParameter("DATE").replace('/', '-');
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            try {
                _semestername = setSemesterName(db2);
            } catch (Exception e) {
                log.debug("setSemesterName exception", e);
            }
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }

        private String setSemesterName(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }
    }

}// クラスの括り

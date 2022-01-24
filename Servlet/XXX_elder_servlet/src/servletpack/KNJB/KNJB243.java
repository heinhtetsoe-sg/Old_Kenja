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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB243 {

    private static final Log log = LogFactory.getLog(KNJB243.class);
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
                final List kouzaList = createKouzaInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, kouzaList)) { // 生徒出力のメソッド
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
    private boolean printMain(final Vrw32alp svf, final List kouzaList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = kouzaList.iterator(); it.hasNext();) {
            final Kouza kouza = (Kouza) it.next();
            if (kouza._schChrDatList.size() > 0) {
                if (printStudent(svf, kouza)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }
    
    private boolean printStudent(final Vrw32alp svf, final Kouza kouza) {
        boolean hasData = false;
        svf.VrSetForm("KNJB243.frm", 4);

        svf.VrsOut("TITLE", "講座別実施状況");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        final String fieldLen1 = (30 < getMS932ByteLength(kouza._subclassname)) ? "_2" : "";
        final String fieldLen2 = (30 < getMS932ByteLength(kouza._chairname)) ? "_2" : "";
        svf.VrsOut("SUBCLASSCD", kouza._subclasscd);
        svf.VrsOut("SUBCLASSNAME" + fieldLen1, kouza._subclassname);
        svf.VrsOut("CHAIRCD", kouza._chaircd);
        svf.VrsOut("CHAIRNAME" + fieldLen2, kouza._chairname);
        svf.VrsOut("HOLD_SEMESTER", kouza._takesemesName);
        svf.VrsOut("DATE", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
        String staffM = "";
        String staffS = "";
        String seqM = "";
        String seqS = "";
        for (final Iterator it = kouza._staffList.iterator(); it.hasNext();) {
            final Staff staff = (Staff) it.next();
            if ("1".equals(staff._chargediv)) {
                staffM += seqM + staff._staffname;
                seqM = ",";
            } else {
                staffS += seqS + staff._staffname;
                seqS = ",";
            }
        }
        final String fieldLenM = (30 < getMS932ByteLength(staffM)) ? "_2" : "";
        final String fieldLenS = (30 < getMS932ByteLength(staffS)) ? "_2" : "";
        svf.VrsOut("MAIN_STAFF" + fieldLenM, staffM);
        svf.VrsOut("SUB_STAFF"  + fieldLenS, staffS);

        String monthKeep = "";
        List monthList = new ArrayList();
        List curentMonthSchChrDatList = new ArrayList();
        for (final Iterator it = kouza._schChrDatList.iterator(); it.hasNext();) {
            final SchChrDat schChrDat = (SchChrDat) it.next();
            if (!monthKeep.equals(schChrDat._semester + schChrDat._month)) {
                curentMonthSchChrDatList = new ArrayList();
                monthList.add(curentMonthSchChrDatList);
                monthKeep = schChrDat._semester + schChrDat._month;
            }
            curentMonthSchChrDatList.add(schChrDat);
        }

        final int maxLen = 6;
        for (final Iterator it = monthList.iterator(); it.hasNext();) {
            final List monthSchChrDatList = (List) it.next();
            final int monthMaxLine = (monthSchChrDatList.size() % maxLen == 0) ? monthSchChrDatList.size() / maxLen : monthSchChrDatList.size() / maxLen + 1;
            int cnt = 0;
            for (final Iterator it2 = monthSchChrDatList.iterator(); it2.hasNext();) {
                final SchChrDat schChrDat = (SchChrDat) it2.next();
                cnt++;
                final int len  = (cnt % maxLen == 0) ? maxLen       : cnt % maxLen;
                final int line = (cnt % maxLen == 0) ? cnt / maxLen : cnt / maxLen + 1;
                final String field = (line == monthMaxLine) ? "JISSEN_" : "HASEN_"; //最終行は実践。それ以外は破線
                //最初に月を出力
                if (cnt == 1) {
                    svf.VrsOut("HASEN_SEMESTER", schChrDat._semestername);
                    svf.VrsOut("HASEN_MONTH", schChrDat._month + "月");
                    svf.VrEndRecord();
                }
                svf.VrsOut(field + "DAY" + len, schChrDat._day);
                svf.VrsOut(field + "WEEK" + len, "(" + KNJ_EditDate.h_format_W(schChrDat._executedate) + ")");
                svf.VrsOut(field + "PERIOD" + len, schChrDat._periodabbv);
                svf.VrsOut(field + "DIV" + len, schChrDat._executedivabbv);
                //改行と最後に１行出力
                if (cnt % maxLen == 0 || cnt == monthSchChrDatList.size()) {
                    svf.VrEndRecord();
                    hasData = true;
                }
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
    
    private List createKouzaInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getKouzaInfoSql(chaircd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Kouza kouzaInfo = new Kouza(
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CHAIRCD"),
                        rs.getString("CHAIRNAME"),
                        rs.getString("TAKESEMES_NAME")
                        );
                rtnList.add(kouzaInfo);
                kouzaInfo._staffList = kouzaInfo.createStaffInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._schChrDatList = kouzaInfo.createSchChrDatInfoData(db2, kouzaInfo._chaircd);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private String getKouzaInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T1.SUBCLASSCD AS SUBCLASSCD, ");
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
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        return stb.toString();
    }
    
    /** 講座データクラス */
    private class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _takesemesName;
        List _staffList = new ArrayList();
        List _schChrDatList = new ArrayList();
        
        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String takesemesName
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _takesemesName = takesemesName;
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
        
        private List createSchChrDatInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getSchChrDatInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SchChrDat schChrDatInfo = new SchChrDat(
                            rs.getString("SEMESTER"),
                            rs.getString("SEMESTERNAME"),
                            rs.getString("MONTH"),
                            rs.getString("DAY"),
                            rs.getString("EXECUTEDATE"),
                            rs.getString("PERIODCD"),
                            rs.getString("PERIOD_NAME"),
                            rs.getString("PERIOD_ABBV"),
                            rs.getString("EXECUTEDIV"),
                            rs.getString("EXECUTEDIV_NAME"),
                            rs.getString("EXECUTEDIV_ABBV")
                            );
                    rtnList.add(schChrDatInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
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
    
    /** 講座職員データクラス */
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
    
    private String getSchChrDatInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
        stb.append("     DAY(T1.EXECUTEDATE) AS DAY, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.EXECUTEDIV, ");
        stb.append("     N1.NAME1 AS PERIOD_NAME, ");
        stb.append("     N1.ABBV1 AS PERIOD_ABBV, ");
        stb.append("     N2.NAME1 AS EXECUTEDIV_NAME, ");
        stb.append("     N2.ABBV1 AS EXECUTEDIV_ABBV ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' AND N1.NAMECD2 = T1.PERIODCD ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'C009' AND N2.NAMECD2 = T1.EXECUTEDIV ");
        stb.append("     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }
    
    /** 通常時間割クラス */
    private class SchChrDat {
        final String _semester;
        final String _semestername;
        final String _month;
        final String _day;
        final String _executedate;
        final String _periodcd;
        final String _periodname;
        final String _periodabbv;
        final String _executediv;
        final String _executedivname;
        final String _executedivabbv;
        
        SchChrDat(
                final String semester,
                final String semestername,
                final String month,
                final String day,
                final String executedate,
                final String periodcd,
                final String periodname,
                final String periodabbv,
                final String executediv,
                final String executedivname,
                final String executedivabbv
        ) {
            _semester = semester;
            _semestername = semestername;
            _month = month;
            _day = day;
            _executedate = executedate;
            _periodcd = periodcd;
            _periodname = periodname;
            _periodabbv = periodabbv;
            _executediv = executediv;
            _executedivname = executedivname;
            _executedivabbv = executedivabbv;
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
        final String _subclasscd;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;
        final String _useCurriculumcd;

//        private boolean _seirekiFlg;
//        private String _nendo;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _subclasscd  = request.getParameter("SUBCLASSCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }
    }
    
}// クラスの括り

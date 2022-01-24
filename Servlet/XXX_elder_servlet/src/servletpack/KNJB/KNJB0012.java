package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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


/**
 * 一覧
 */
public class KNJB0012 {

    private static final Log log = LogFactory.getLog(KNJB0012.class);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List periodList = createPeriodInfo(db2);

        svf.VrSetForm("KNJB0012.frm", 4);

        for (final Iterator it = periodList.iterator(); it.hasNext();) {
            final Period periodInfo = (Period) it.next();

            //ヘッダ
//            svf.VrsOut("ymd", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
//            svf.VrsOut("nendo", _param._year + "年度　生徒別履修時間割表");
            svf.VrsOut("ymd", _param.getDate(_param._ctrlDate));
            svf.VrsOut("nendo", _param.getNendo() + "　生徒別履修時間割表");
            svf.VrsOut("HR_NAME", _param._schregInfo);
            svf.VrsOut("NAME", _param._schregInfoName);
            svf.VrsOut("TITLE", "学期(" + _param._semestername + ")　履修登録日(" + _param._rirekiName + ")　基本時間割(" + _param._schPtrnHInfo + ")");

            if (periodInfo._maxCnt > 0) {
                for (int line = 1; line <= periodInfo._maxCnt; line++) {
                    for (Iterator it2 = periodInfo._weekMap.keySet().iterator(); it2.hasNext();) {
                        final String dayCd = (String) it2.next();
                        final Map chairMap = (Map) periodInfo._weekMap.get(dayCd);
                        if (chairMap.containsKey(String.valueOf(line))) {
                            final ChairDat chairDatInfo = (ChairDat) chairMap.get(String.valueOf(line));
                            final String mojiNo = getMS932ByteLength(chairDatInfo._chairname) <= 12 ? "" : "_2";
                            svf.VrsOut("CLASSNAME" + getDaycdReplace(dayCd) + mojiNo, chairDatInfo._chairname);
                        }
                    }
                    //校時
                    svf.VrsOut("PERIOD", periodInfo._periodname);
                    for (int i = 1; i <= 7; i++) {
                        svf.VrsOut("CLASSCD" + i, periodInfo._periodcd);
                    }
                    svf.VrEndRecord();
                }
            } else {
                //校時
                svf.VrsOut("PERIOD", periodInfo._periodname);
                for (int i = 1; i <= 7; i++) {
                    svf.VrsOut("CLASSCD" + i, periodInfo._periodcd);
                }
                svf.VrEndRecord();
            }

            _hasData = true;
        }
    }

    private String getDaycdReplace(final String dayCd) throws Exception {
        if ("2".equals(dayCd)) return "1"; //月
        if ("3".equals(dayCd)) return "2"; //火
        if ("4".equals(dayCd)) return "3"; //水
        if ("5".equals(dayCd)) return "4"; //木
        if ("6".equals(dayCd)) return "5"; //金
        if ("7".equals(dayCd)) return "6"; //土
        if ("1".equals(dayCd)) return "7"; //日

        return "";
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

    private List createPeriodInfo(final DB2UDB db2) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getPeriodInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Period periodInfo = new Period(
                        rs.getString("PERIODCD"),
                        rs.getString("PERIOD_NAME")
                        );
                periodInfo.createChairDat(db2);
                rtnList.add(periodInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPeriodInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     N1.NAME1 AS PERIOD_NAME ");
        stb.append(" FROM ");
        stb.append("     SCH_PTRN_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' AND N1.NAMECD2 = T1.PERIODCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.BSCSEQ = " + _param._bscseq + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    private class Period {
        final String _periodcd;
        final String _periodname;
        final Map _weekMap = new TreeMap();
        int _maxCnt = 0;

        Period(
                final String periodcd,
                final String periodname
        ) {
            _periodcd = periodcd;
            _periodname = periodname;
        }

        private void createChairDat(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChairDatSql(_periodcd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int chairCnt = 0;
                String befDay = "";
                Map chairMap = new TreeMap();
                while (rs.next()) {
                    final String dayCd = rs.getString("DAYCD");
                    if (!befDay.equals(dayCd) && !"".equals(befDay)) {
                        _weekMap.put(befDay, chairMap);
                        _maxCnt = chairCnt > _maxCnt ? chairCnt : _maxCnt;
                        chairCnt = 0;
                        chairMap = new TreeMap();
                    }
                    chairCnt++;
                    final ChairDat chairDatInfo = new ChairDat(
                            rs.getString("CHAIRCD"),
                            rs.getString("CHAIRNAME")
                            );
                    chairMap.put(String.valueOf(chairCnt), chairDatInfo);
                    befDay = dayCd;
                }
                if (!"".equals(befDay)) {
                    _weekMap.put(befDay, chairMap);
                    _maxCnt = chairCnt > _maxCnt ? chairCnt : _maxCnt;
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

    }

    private String getChairDatSql(final String periodcd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_C AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.RIREKI_CODE, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.GROUPCD ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("     AND T1.SCHREGNO = '" + _param._schregno + "' ");
        stb.append(" ), SUBCLASS_G AS ( ");
        stb.append(" SELECT ");
        stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T2.GROUPCD ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT T2, ");
        stb.append("     SUBCLASS_C T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.RIREKI_CODE = T2.RIREKI_CODE ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.SUBCLASSCD <> T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("     AND T1.GROUPCD = T2.GROUPCD ");
        stb.append(" ), MAIN_CHAIR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     SUBCLASS_C T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     SUBCLASS_G T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" ), CHAIR_STD AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + _param._schregno + "' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L1.CHAIRNAME ");
        stb.append(" FROM ");
        stb.append("     SCH_PTRN_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.BSCSEQ = " + _param._bscseq + " ");
        stb.append("     AND T1.PERIODCD = '" + periodcd + "' ");
        stb.append("     AND T1.CHAIRCD IN (SELECT CHAIRCD FROM MAIN_CHAIR) ");
        stb.append("     AND T1.CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_STD) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    private class ChairDat {
        final String _chaircd;
        final String _chairname;
        
        ChairDat(
                final String chaircd,
                final String chairname
        ) {
            _chaircd = chaircd;
            _chairname = chairname;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _ctrlDate;
        final String _schregno;
        final String _year;
        final String _semester;
        final String _rirekiCode;
        final String _bscseq;
        private boolean _seirekiFlg;

        private String _semestername;
        private String _rirekiName;
        private String _schPtrnHInfo;
        private String _schregInfo;
        private String _schregInfoHrName;
        private String _schregInfoName;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schregno = request.getParameter("PRINT_SCHREGNO");
            _year = request.getParameter("PRINT_YEAR");
            _semester = request.getParameter("SEMESTER");
            _rirekiCode = request.getParameter("RIREKI_CODE");
            _bscseq = StringUtils.split(request.getParameter("SCH_PTRN"), ",")[1];

            try {
                _semestername = setSemesterName(db2);
            } catch (Exception e) {
                log.debug("setSemesterName exception", e);
            }
            try {
                _rirekiName = setRirekiName(db2);
            } catch (Exception e) {
                log.debug("setRirekiName exception", e);
            }
            try {
                _schregInfo = setSchregInfo(db2);
            } catch (Exception e) {
                log.debug("setSchregInfo exception", e);
            }
            try {
                _schPtrnHInfo = setSchPtrnHInfo(db2);
            } catch (Exception e) {
                log.debug("setSchPtrnHInfo exception", e);
            }
            try {
                _seirekiFlg = getSeirekiFlg(db2);
            } catch (Exception e) {
                log.debug("getSeirekiFlg exception", e);
            }
        }

        private String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return _seirekiFlg ? date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) : KNJ_EditDate.h_format_JP(date) ;
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
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
                log.debug("setSemesterName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String setRirekiName(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SELECT_NAME || ' ' || RTRIM(CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/'))) AS RIREKI_NAME FROM STUDY_SELECT_DATE_YMST WHERE YEAR = '" + _year + "' AND RIREKI_CODE = '" + _rirekiCode + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("RIREKI_NAME");
                }
            } catch (SQLException ex) {
                log.debug("setRirekiName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String setSchregInfo(DB2UDB db2) {
            _schregInfoHrName = null;
            _schregInfoName = null;
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = setSchregInfoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("HR_NAME") + "　" + rs.getString("ATTENDNO") + "番　" + rs.getString("NAME") + "(" + rs.getString("SCHREGNO") + ")";
                    _schregInfoHrName = rs.getString("HR_NAME") + "　" + rs.getString("ATTENDNO") + "番";
                    _schregInfoName = rs.getString("NAME") + "(" + rs.getString("SCHREGNO") + ")";
                }
            } catch (SQLException ex) {
                log.debug("setSchregInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String setSchregInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD_H.HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST BASE ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("          AND REGD.YEAR = '" + _year + "' ");
            stb.append("          AND REGD.SEMESTER = '" + _semester + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGD_H.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGD_H.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGD_H.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     BASE.SCHREGNO = '" + _schregno + "' ");
            return stb.toString();
        }

        private String setSchPtrnHInfo(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = setSchPtrnHInfoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("SEMESTERNAME") + " Seq" + rs.getString("BSCSEQ") + ":" + rs.getString("TITLE");
                }
            } catch (SQLException ex) {
                log.debug("setSchPtrnHInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String setSchPtrnHInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTERNAME, ");
            stb.append("     T1.BSCSEQ, ");
            stb.append("     T1.TITLE, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SCH_PTRN_HDAT T1, ");
            stb.append("     SEMESTER_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
            stb.append("     AND T1.BSCSEQ = " + _bscseq + " ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            return stb.toString();
        }
    }

}// クラスの括り

// kanji=漢字
/*
 * $Id: fe9807632e561b544647bcd8ee133d484abda40b $
 *
 * 作成日: 2009/12/19 19:20:01 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: fe9807632e561b544647bcd8ee133d484abda40b $
 */
public class KNJL352S {

    private static final Log log = LogFactory.getLog("KNJL352S.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL352S.frm";

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            for (final Iterator itAppDiv = _param._applicantDivList.iterator(); itAppDiv.hasNext();) {
                final ApplicantDivData applicantDivData = (ApplicantDivData) itAppDiv.next();
                printMain(db2, svf, applicantDivData);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final ApplicantDivData applicantDivData) throws SQLException {

        svf.VrSetForm(FORMNAME, 4);
        final List printData = getPrintData(db2, applicantDivData);
        String befDistCd = "";
        String befPrefCd = "";
        Map totalTiikiMap = new HashMap();
        Map totalKenMap = new HashMap();
        Map totalMap = new HashMap();
        ManWomanCnt tiikiCnt = null;
        ManWomanCnt kenCnt = null;
        ManWomanCnt totalCnt = null;
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            setHead(svf, applicantDivData);
            final FsData fsData = (FsData) itPrint.next();

            if (!"".equals(befDistCd) && befDistCd != null && !befDistCd.equals(fsData._districtCd)) {
                printKakuTotal(svf, totalTiikiMap, "地区計");
                totalTiikiMap = new HashMap();
            }

            if (!"".equals(befPrefCd) && befPrefCd != null && !befPrefCd.equals(fsData._prefCd)) {
                printKakuTotal(svf, totalKenMap, "都道府県計");
                totalKenMap = new HashMap();
            }

            svf.VrsOut("PREF", fsData._prefName);
            svf.VrsOut("DISTRICT_NO", fsData._districtCd);
            svf.VrsOut("DISTRICT_NAME", fsData._districtName);
            svf.VrsOut("JHSCHOOL_NO", fsData._fsCd);
            svf.VrsOut("JHSCHOOL_NAME", fsData._fsName + "中学校");

            ManWomanCnt fsTotalManWoman = null;
            for (final Iterator itCnt = fsData._majorList.iterator(); itCnt.hasNext();) {
                final MajorData majorData = (MajorData) itCnt.next();
                final String setField = String.valueOf(majorData._setField);
                svf.VrsOut("SUBJECT_M" + setField, majorData._setManCnt == 0 ? "" : String.valueOf(majorData._setManCnt));
                svf.VrsOut("SUBJECT_F" + setField, majorData._setWomanCnt == 0 ? "" : String.valueOf(majorData._setWomanCnt));

                tiikiCnt = addKakuTotalCnt(totalTiikiMap, majorData, setField);

                kenCnt = addKakuTotalCnt(totalKenMap, majorData, setField);

                totalCnt = addKakuTotalCnt(totalMap, majorData, setField);

                if (fsTotalManWoman == null) {
                    fsTotalManWoman = new ManWomanCnt(majorData._setManCnt, majorData._setWomanCnt);
                } else {
                    fsTotalManWoman.addCnt(majorData._setManCnt, majorData._setWomanCnt);
                }

                totalTiikiMap.put(setField, tiikiCnt);
                totalKenMap.put(setField, kenCnt);
                totalMap.put(setField, totalCnt);
                svf.VrsOut("SUBJECT_M7", fsTotalManWoman._manCnt == 0 ? "" : String.valueOf(fsTotalManWoman._manCnt));
                svf.VrsOut("SUBJECT_F7", fsTotalManWoman._womanCnt == 0 ? "" : String.valueOf(fsTotalManWoman._womanCnt));
                svf.VrsOut("TOTAL_SUBJECT", String.valueOf(fsTotalManWoman._manCnt + fsTotalManWoman._womanCnt));
            }
            svf.VrEndRecord();

            befDistCd = fsData._districtCd;
            befPrefCd = fsData._prefCd;
            _hasData = true;
        }

        if (totalTiikiMap.size() > 0) {
            printKakuTotal(svf, totalTiikiMap, "地区計");
            totalTiikiMap = new HashMap();
        }

        if (totalKenMap.size() > 0) {
            printKakuTotal(svf, totalKenMap, "都道府県計");
            totalKenMap = new HashMap();
        }
        printKakuTotal(svf, totalMap, "合計");
    }

    private ManWomanCnt addKakuTotalCnt(final Map totalTiikiMap, final MajorData majorData, final String setField) {
        ManWomanCnt tiikiCnt;
        if (totalTiikiMap.containsKey(setField)) {
            tiikiCnt = (ManWomanCnt) totalTiikiMap.get(setField);
            tiikiCnt.addCnt(majorData._setManCnt, majorData._setWomanCnt);
        } else {
            tiikiCnt = new ManWomanCnt(majorData._setManCnt, majorData._setWomanCnt);
        }
        return tiikiCnt;
    }

    private void printKakuTotal(final Vrw32alp svf, final Map totalKenMap, final String districtName) {
        int manTotal = 0;
        int womanTotal = 0;
        svf.VrsOut("TOTAL_DISTRICT_NAME", districtName);
        for (final Iterator itTotal = totalKenMap.keySet().iterator(); itTotal.hasNext();) {
            final String setField = (String) itTotal.next();
            final ManWomanCnt manWomanCnt = (ManWomanCnt) totalKenMap.get(setField);
            svf.VrsOut("TOTAL_SUBJECT_M" + setField, manWomanCnt._manCnt == 0 ? "" : String.valueOf(manWomanCnt._manCnt));
            svf.VrsOut("TOTAL_SUBJECT_F" + setField, manWomanCnt._womanCnt == 0 ? "" : String.valueOf(manWomanCnt._womanCnt));
            manTotal += manWomanCnt._manCnt;
            womanTotal += manWomanCnt._womanCnt;
        }
        svf.VrsOut("TOTAL_SUBJECT_M" + 7, String.valueOf(manTotal));
        svf.VrsOut("TOTAL_SUBJECT_F" + 7, String.valueOf(womanTotal));
        svf.VrsOut("ALL_TOTAL_SUBJECT", String.valueOf(manTotal + womanTotal));
        svf.VrEndRecord();
    }

    private void setHead(final Vrw32alp svf, final ApplicantDivData applicantDivData) {
        final String year = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("YEAR", year);
        svf.VrsOut("EXAM_DIV", applicantDivData._applicantName);
        final String setDate = KNJ_EditDate.h_format_JP(_param._ctrlDate.replace('/', '-'));
        final String setWeek = KNJ_EditDate.h_format_W(_param._ctrlDate);
        svf.VrsOut("DATE1", setDate + "(" + setWeek + ")");
        for (final Iterator itTitle = _param._titleMap.keySet().iterator(); itTitle.hasNext();) {
            final String key = (String) itTitle.next();
            final MajorData majorData = (MajorData) _param._titleMap.get(key);
            svf.VrsOut("SUBJECT" + majorData._setField, majorData.getMajorAbbv());
            svf.VrsOut("SUBJECT1" + majorData._setField, majorData.getMajorAbbv());
        }
        svf.VrsOut("SUBJECT7", "小計");
        svf.VrsOut("SUBJECT17", "小計");
    }

    private List getPrintData(final DB2UDB db2, final ApplicantDivData applicantDivData) throws SQLException {
        final List retList = new ArrayList();
        final String finScoolSql = getFinSchoolSql(applicantDivData);
        PreparedStatement psPrint = null;
        ResultSet rsPrint = null;
        try {
            psPrint = db2.prepareStatement(finScoolSql);
            rsPrint = psPrint.executeQuery();
            String befFsCd = "";
            FsData fsData = null;
            while (rsPrint.next()) {
                final String majorCd = rsPrint.getString("MAJORCD");
                final String prefCd = rsPrint.getString("FINSCHOOL_PREF_CD");
                final String prefName = rsPrint.getString("PREF_NAME");
                final String districtCd = rsPrint.getString("DISTRICTCD");
                final String districtName = rsPrint.getString("DISTRICT_NAME");
                final String fsCd = rsPrint.getString("FS_CD");
                final String fsName = rsPrint.getString("FINSCHOOL_NAME");
                final int manCnt = rsPrint.getInt("MAN_CNT");
                final int womanCnt = rsPrint.getInt("WOMAN_CNT");
                final String keyFsCd = fsCd;
                if (!"".equals(befFsCd) && !keyFsCd.equals(befFsCd)) {
                    retList.add(fsData);
                }
                if ("".equals(befFsCd) || !keyFsCd.equals(befFsCd)) {
                    fsData = new FsData(prefCd, prefName, districtCd, districtName, fsCd, fsName);
                }
                fsData.setMajor(majorCd, manCnt, womanCnt);
                befFsCd = keyFsCd;
            }
            if (null != fsData) {
                retList.add(fsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psPrint, rsPrint);
            db2.commit();
        }
        
        return retList;
    }

    private String getFinSchoolSql(final ApplicantDivData applicantDivData) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L1.MAJORLCD || L1.MAJORSCD AS MAJORCD, ");
        stb.append("     L2.FINSCHOOL_PREF_CD, ");
        stb.append("     L3.PREF_NAME, ");
        stb.append("     L2.DISTRICTCD, ");
        stb.append("     N1.NAME1 AS DISTRICT_NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     L2.FINSCHOOL_NAME, ");
        stb.append("     SUM(CASE WHEN T1.SEX = '1' THEN 1 ElSE 0 END) AS MAN_CNT, ");
        stb.append("     SUM(CASE WHEN T1.SEX = '2' THEN 1 ElSE 0 END) AS WOMAN_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTWISH_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append("          AND L1.WISHNO = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L2 ON T1.FS_CD = L2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN PREF_MST L3 ON L2.FINSCHOOL_PREF_CD = L3.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z003' ");
        stb.append("          AND L2.DISTRICTCD = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + applicantDivData._applicantDiv + "' ");
        stb.append("     AND VALUE(T1.INTERVIEW_ATTEND_FLG, '0') <> '1' ");
        stb.append("     AND T1.FS_CD IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     L1.MAJORLCD || L1.MAJORSCD, ");
        stb.append("     L2.FINSCHOOL_PREF_CD, ");
        stb.append("     L3.PREF_NAME, ");
        stb.append("     L2.DISTRICTCD, ");
        stb.append("     N1.NAME1, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     L2.FINSCHOOL_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     L2.FINSCHOOL_PREF_CD, ");
        stb.append("     L2.DISTRICTCD, ");
        stb.append("     L1.MAJORLCD || L1.MAJORSCD, ");
        stb.append("     T1.FS_CD ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class FsData {
        final String _prefCd;
        final String _prefName;
        final String _districtCd;
        final String _districtName;
        final String _fsCd;
        final String _fsName;
        final List _majorList;

        public FsData(
                final String prefCd,
                final String prefName,
                final String districtCd,
                final String districtName,
                final String fsCd,
                final String fsName
        ) {
            _prefCd = prefCd;
            _prefName = prefName;
            _districtCd = districtCd;
            _districtName = districtName;
            _fsCd = fsCd;
            _fsName = fsName;
            _majorList = new ArrayList();
        }

        public void setMajor(
                final String majorCd,
                final int manCnt,
                final int womanCnt
        ) {
            final MajorData majorData = new MajorData(majorCd, "", "", "", "", _param.getFieldCnt(majorCd));
            majorData.setCnt(manCnt, womanCnt);
            _majorList.add(majorData);
        }

        public String toString() {
            return _prefName + " " + _districtName + " " + _fsName;
        }
    }

    private class ManWomanCnt {
        int _manCnt = 0;
        int _womanCnt = 0;

        public ManWomanCnt(final int manCnt, final int womanCnt) {
            _manCnt = manCnt;
            _womanCnt = womanCnt;
        }

        public void addCnt(final int manCnt, final int womanCnt) {
            _manCnt += manCnt;
            _womanCnt += womanCnt;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final Map _titleMap;
        private final List _applicantDivList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _titleMap = getTitleMajor(db2);
            _applicantDivList = getApplicantDiv(db2);
        }

        public int getFieldCnt(final String majorCd) {
            final int ret = _titleMap.containsKey(majorCd) ? ((MajorData) _titleMap.get(majorCd))._setField : 0;
            return ret;
        }

        private Map getTitleMajor(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final String titleMajorSql = getTitleMajorSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int setField = 1;
                ps = db2.prepareStatement(titleMajorSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String majorCd = rs.getString("MAJORCD");
                    final String majorLName = rs.getString("MAJORLNAME");
                    final String majorLAbbv = rs.getString("MAJORLABBV");
                    final String majorSName = rs.getString("MAJORSNAME");
                    final String majorSAbbv = rs.getString("MAJORSABBV");
                    final MajorData majorData = new MajorData(majorCd, majorLName, majorLAbbv, majorSName, majorSAbbv, setField);
                    setField++;
                    retMap.put(majorCd, majorData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

        private String getTitleMajorSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_MAJOR_MST ");
            stb.append(" ORDER BY ");
            stb.append("     MAJORCD ");

            return stb.toString();
        }

        private List getApplicantDiv(DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String applicantDivSql = getApplicantDivSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(applicantDivSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String applicantDiv = rs.getString("APPLICANTDIV");
                    final String applicantName = rs.getString("APPLICANTNAME");
                    final ApplicantDivData applicantDivData = new ApplicantDivData(applicantDiv, applicantName); 
                    retList.add(applicantDivData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retList;
        }

        private String getApplicantDivSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     L1.NAME1 AS APPLICANTNAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'L003' ");
            stb.append("           AND L1.NAMECD2 = T1.APPLICANTDIV ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _year + "' ");
            if (!"99".equals(_applicantDiv)) {
                stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.APPLICANTDIV ");

            return stb.toString();
        }
    }

    private class ApplicantDivData {
        final String _applicantDiv;
        final String _applicantName;

        public ApplicantDivData(
                final String applicantDiv,
                final String applicantName
        ) {
            _applicantDiv = applicantDiv;
            _applicantName = applicantName;
        }
    }

    private class MajorData {
        final String _majorCd;
        final String _majorLName;
        final String _majorLAbbv;
        final String _majorSName;
        final String _majorSAbbv;
        int _setField;
        int _setManCnt;
        int _setWomanCnt;

        public MajorData(
                final String majorCd,
                final String majorLName,
                final String majorLAbbv,
                final String majorSName,
                final String majorSAbbv,
                final int setField
        ) {
            _majorCd = majorCd;
            _majorLName = majorLName;
            _majorLAbbv = majorLAbbv;
            _majorSName = majorSName;
            _majorSAbbv = majorSAbbv;
            _setField = setField > 6 ? 99 : setField;
        }

        public void setCnt(final int manCnt, final int womanCnt) {
            _setManCnt = manCnt;
            _setWomanCnt = womanCnt;
        }

        public String getMajorAbbv() {
            if (_majorCd != null && _majorCd.endsWith("0")) {
                return _majorLAbbv;
            } else {
                return _majorSAbbv;
            }
        }
    }
}

// eof

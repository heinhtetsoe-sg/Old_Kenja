/*
 * 作成日: 2021/03/19
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF323A extends HttpServlet {
    private static final Log log = LogFactory.getLog(KNJF323A.class);

    private boolean _hasData;

    private Param _param;

    private static final int LINE_MAX = 40;
    private static final String KUBUN_CLASS_BETSU = "1"; // 1:クラス別

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJF323A.frm", 1);
        final Map<String, PageItem> pageItemMap = getMedicalCertificateMap(db2);

        int lineCnt = 1;

        for (PageItem pageItem : pageItemMap.values()) {
            printTitle(svf, pageItem._pageItemName);

            for (MedicalCertification medicalCertification : pageItem._medicalCertificateMap.values()) {
                if (LINE_MAX < lineCnt) {
                    lineCnt = 1;
                    svf.VrEndPage();
                    printTitle(svf, pageItem._pageItemName);
                }

                svf.VrsOutn("HR_NAME_NO", lineCnt, medicalCertification._attendno);
                svf.VrsOutn("NAME1", lineCnt, medicalCertification._name);
                svf.VrsOutn("SEX", lineCnt, medicalCertification._sex);
                svf.VrsOutn("TEL_NO", lineCnt, medicalCertification._guardTelno);
                svf.VrsOutn("MEDICAL_HISTORY1", lineCnt, medicalCertification._mediccalHistory1);
                svf.VrsOutn("MEDICAL_HISTORY2", lineCnt, medicalCertification._mediccalHistory2);
                svf.VrsOutn("MEDICAL_HISTORY3", lineCnt, medicalCertification._mediccalHistory3);
                svf.VrsOutn("HEARTDISEASE", lineCnt, medicalCertification._heartDiseasecd);
                svf.VrsOutn("REMARK", lineCnt, medicalCertification._remark);

                lineCnt++;
                _hasData = true;
            }

            lineCnt = 1;
            svf.VrEndPage();
        }
    }

    private void printTitle(final Vrw32alp svf, final String pageItemName) {
        final String kubunName = KUBUN_CLASS_BETSU.equals(_param._kubun) ? "クラス別" : "部活動別";
        svf.VrsOut("TITLE",  _param._ctrlYear + "年度　健康リスト（" + kubunName + "）");
        svf.VrsOut("DATE", "作成日：" + KNJ_EditDate.h_format_SeirekiJP(_param._ctrlDate));
        svf.VrsOut("BELONG", pageItemName);
    }

    private Map<String, PageItem> getMedicalCertificateMap(final DB2UDB db2) {
        final Map<String, PageItem> pageItemMap = new LinkedHashMap<String, PageItem>();
        PageItem pageItem = null;
        MedicalCertification medicalCertification = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String medicalCertificateSql = getMedicalCertificateSql();
        log.debug(" medical certificate sql =" + medicalCertificateSql);

        try {
            ps = db2.prepareStatement(medicalCertificateSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String pageItemCd = rs.getString("PAGE_ITEM_CD");
                String pageItemName = rs.getString("PAGE_ITEM_NAME");
                String attendno = rs.getString("GRADE_HR_CLASS_ATTENDNO");
                String name = rs.getString("NAME");
                String sex = rs.getString("SEX");
                String guardTelno = rs.getString("GUARD_TELNO");
                String medicalHistory1 = rs.getString("MEDICAL_HISTORY1");
                String medicalHistory2 = rs.getString("MEDICAL_HISTORY2");
                String medicalHistory3 = rs.getString("MEDICAL_HISTORY3");
                String heartDiseasse = rs.getString("HEARTDISEASE");
                String remark = rs.getString("REMARK");
                if (pageItemMap.containsKey(pageItemCd)) {
                    pageItem = pageItemMap.get(pageItemCd);
                } else {
                    pageItem = new PageItem(pageItemCd, pageItemName);
                    pageItemMap.put(pageItemCd, pageItem);
                }

                medicalCertification = new MedicalCertification(attendno, name, sex, guardTelno, medicalHistory1, medicalHistory2, medicalHistory3, heartDiseasse, remark);
                pageItem._medicalCertificateMap.put(attendno, medicalCertification);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return pageItemMap;
    }

    private class PageItem {
        final String _pageItemCd;
        final String _pageItemName;
        final Map<String, MedicalCertification> _medicalCertificateMap;

        PageItem(final String pageItemCd, final String pageItemName) {
            _pageItemCd            = pageItemCd;
            _pageItemName          = pageItemName;
            _medicalCertificateMap = new LinkedHashMap<String, MedicalCertification>();
        }
    }

    private class MedicalCertification {
        final String _attendno;
        final String _name;
        final String _sex;
        final String _guardTelno;
        final String _mediccalHistory1;
        final String _mediccalHistory2;
        final String _mediccalHistory3;
        final String _heartDiseasecd;
        final String _remark;

        MedicalCertification(
            final String attendno,
            final String name,
            final String sex,
            final String guardTelno,
            final String mediccalHistory1,
            final String mediccalHistory2,
            final String mediccalHistory3,
            final String heartDiseasecd,
            final String remark
        ) {
            _attendno         = attendno;
            _name             = name;
            _sex              = sex;
            _guardTelno       = guardTelno;
            _mediccalHistory1 = mediccalHistory1;
            _mediccalHistory2 = mediccalHistory2;
            _mediccalHistory3 = mediccalHistory3;
            _heartDiseasecd   = heartDiseasecd;
            _remark           = remark;
        }

    }
    private String getMedicalCertificateSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG AS ( ");
        if ("1".equals(_param._kubun)) {
            stb.append("     SELECT ");
            stb.append("         DAT.YEAR, ");
            stb.append("         DAT.SEMESTER, ");
            stb.append("         DAT.SCHREGNO, ");
            stb.append("         DAT.GRADE || DAT.HR_CLASS AS PAGE_ITEM_CD, ");
            stb.append("         HDAT.HR_NAME AS PAGE_ITEM_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT DAT ");
            stb.append("         INNER JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("                 ON HDAT.YEAR     = DAT.YEAR ");
            stb.append("                AND HDAT.SEMESTER = DAT.SEMESTER ");
            stb.append("                AND HDAT.GRADE    = DAT.GRADE ");
            stb.append("                AND HDAT.HR_CLASS = DAT.HR_CLASS ");
            stb.append("     WHERE ");
            stb.append("         DAT.YEAR     = '" + _param._ctrlYear     + "' ");
            stb.append("     AND DAT.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND ");
            stb.append(SQLUtils.whereIn(true, "DAT.GRADE || DAT.HR_CLASS", _param._categorySelected));
        } else {
            stb.append("     SELECT ");
            stb.append("         '" + _param._ctrlYear     + "' AS YEAR, ");
            stb.append("         '" + _param._ctrlSemester + "' AS SEMESTER, ");
            stb.append("         HIST.SCHREGNO, ");
            stb.append("         CLUB.SCHOOL_KIND || '-' || CLUB.CLUBCD AS PAGE_ITEM_CD, ");
            stb.append("         CLUB.CLUBNAME AS PAGE_ITEM_NAME ");
            stb.append("     FROM ");
            stb.append("         CLUB_MST CLUB ");
            stb.append("         INNER JOIN SCHREG_CLUB_HIST_DAT HIST ");
            stb.append("                 ON HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
            stb.append("                AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
            stb.append("                AND HIST.CLUBCD      = CLUB.CLUBCD ");
            stb.append("                AND (HIST.EDATE IS NULL OR (TO_DATE('" + _param._ctrlDate + "', 'YYYY/MM/DD') BETWEEN HIST.SDATE AND HIST.EDATE)) ");
            stb.append("     WHERE ");
            stb.append("         CLUB.SCHOOLCD    = '" + _param._schoolcd   + "' ");
            stb.append("     AND ");
            stb.append(SQLUtils.whereIn(true, "CLUB.SCHOOL_KIND || '-' || CLUB.CLUBCD", _param._categorySelected));
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG.PAGE_ITEM_CD, ");
        stb.append("     SCHREG.PAGE_ITEM_NAME, ");
        stb.append("     VALUE(DAT.GRADE, 0) || '年' || VALUE(DAT.HR_CLASS, 0) || '組' || VALUE(DAT.ATTENDNO, 0) || '番' AS GRADE_HR_CLASS_ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.ABBV1 AS SEX, ");
        stb.append("     GUARDIAN.GUARD_TELNO, ");
        stb.append("     F143_1.NAME1 AS MEDICAL_HISTORY1, ");
        stb.append("     F143_2.NAME1 AS MEDICAL_HISTORY2, ");
        stb.append("     F143_3.NAME1 AS MEDICAL_HISTORY3, ");
        stb.append("     F090.NAME1 AS HEARTDISEASE, ");
        stb.append("     MEDEXAM.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT DAT ");
        stb.append("             ON DAT.YEAR     = SCHREG.YEAR ");
        stb.append("            AND DAT.SEMESTER = SCHREG.SEMESTER ");
        stb.append("            AND DAT.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ");
        stb.append("            ON BASE.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");
        stb.append("            ON Z002.YEAR    = DAT.YEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     LEFT JOIN MEDEXAM_DET_DAT MEDEXAM ");
        stb.append("            ON MEDEXAM.YEAR     = DAT.YEAR ");
        stb.append("           AND MEDEXAM.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     LEFT JOIN V_NAME_MST F143_1 ");
        stb.append("            ON F143_1.YEAR    = DAT.YEAR ");
        stb.append("           AND F143_1.NAMECD1 = 'F143' ");
        stb.append("           AND F143_1.NAMECD2 = MEDEXAM.MEDICAL_HISTORY1 ");
        stb.append("     LEFT JOIN V_NAME_MST F143_2 ");
        stb.append("            ON F143_2.YEAR    = DAT.YEAR ");
        stb.append("           AND F143_2.NAMECD1 = 'F143' ");
        stb.append("           AND F143_2.NAMECD2 = MEDEXAM.MEDICAL_HISTORY2 ");
        stb.append("     LEFT JOIN V_NAME_MST F143_3 ");
        stb.append("            ON F143_3.YEAR    = DAT.YEAR ");
        stb.append("           AND F143_3.NAMECD1 = 'F143' ");
        stb.append("           AND F143_3.NAMECD2 = MEDEXAM.MEDICAL_HISTORY3 ");
        stb.append("     LEFT JOIN V_NAME_MST F090 ");
        stb.append("            ON F090.YEAR    = DAT.YEAR ");
        stb.append("           AND F090.NAMECD1 = 'F090' ");
        stb.append("           AND F090.NAMECD2 = MEDEXAM.HEARTDISEASECD ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREG.PAGE_ITEM_CD, ");
        stb.append("     DAT.GRADE, ");
        stb.append("     DAT.HR_CLASS, ");
        stb.append("     DAT.ATTENDNO ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolcd;
        private final String _kubun;
        private final String[] _categorySelected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear         = request.getParameter("CTRL_YEAR");
            _ctrlDate         = request.getParameter("CTRL_DATE");
            _ctrlSemester     = request.getParameter("CTRL_SEMESTER");
            _schoolcd         = request.getParameter("SCHOOLCD");
            _kubun            = request.getParameter("KUBUN");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        }
    }
}

/*
 *
 * 作成日: 2017/09/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
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

public class KNJA143S {

    private static final Log log = LogFactory.getLog(KNJA143S.class);

    private boolean _hasData;

    private Param _param;

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA143S.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        PreparedStatement psFamily = null;
        ResultSet rsFamily = null;
        final String sql = sql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final StringBuffer familySql = new StringBuffer();
            familySql.append(" SELECT ");
            familySql.append("     FAMILY.RELANAME, ");
            familySql.append("     VALUE(REGDH.HR_NAME, '') HR_NAME ");
            familySql.append(" FROM ");
            familySql.append("     FAMILY_DAT FAMILY ");
            familySql.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '2006' ");
            familySql.append("          AND REGD.SEMESTER = '1' ");
            familySql.append("          AND FAMILY.RELA_SCHREGNO = REGD.SCHREGNO ");
            familySql.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            familySql.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
            familySql.append("          AND REGD.GRADE = REGDH.GRADE ");
            familySql.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            familySql.append(" WHERE ");
            familySql.append("     FAMILY.FAMILY_NO = ? ");
            familySql.append("     AND FAMILY.RELA_SCHREGNO != ? ");
            familySql.append("     AND VALUE(FAMILY.REGD_GRD_FLG, '0') = '1' ");

            while (rs.next()) {
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
                svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
                svf.VrsOut("TEACHER_NAME", _param._certifSchoolJobName + "　" + _param._certifSchoolPrincipalName);
                svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._year + "-04-01") + "　防災引渡し確認票");

                int textCnt = 1;
                for (Iterator itTitle = _param._titleList.iterator(); itTitle.hasNext();) {
                    final String titleText = (String) itTitle.next();
                    svf.VrsOutn("TEXT1", textCnt, null != titleText && !"null".equals(titleText) ? titleText : "");
                    textCnt++;
                }
                svf.VrsOut("SCHREG_NO", rs.getString("SCHREGNO"));
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME") + "　" + rs.getString("ATTENDNO") + "番");
                svf.VrsOut("KANA", rs.getString("NAME_KANA"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, rs.getString("BIRTHDAY")));
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));
                svf.VrsOut("BLOOD", rs.getString("BLOODTYPE") + rs.getString("BLOOD_RH"));
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                svf.VrsOut("HOME_TEL_NO", rs.getString("TELNO"));
                svf.VrsOut("ADDR1", rs.getString("ADDR1"));
                svf.VrsOut("ADDR2", rs.getString("ADDR2"));

                //保護者1
                svf.VrsOut("RELA1", rs.getString("RELA_1"));
                svf.VrsOut("GUARD_NAME1", rs.getString("GUARD_NAME_1"));
                svf.VrsOut("GUARD_MOBILE_TEL_NO1", rs.getString("KEITAI_1"));
                svf.VrsOut("GUARD_COMPANY1", rs.getString("GUARD_WORK_NAME_1"));
                svf.VrsOut("GUARD_COMPANY_TEL_NO1", rs.getString("GUARD_WORK_TELNO_1"));

                //保護者2
                svf.VrsOut("RELA2", rs.getString("RELA_2"));
                svf.VrsOut("GUARD_NAME2", rs.getString("GUARD_NAME_2"));
                svf.VrsOut("GUARD_MOBILE_TEL_NO2", rs.getString("KEITAI_2"));
                svf.VrsOut("GUARD_COMPANY2", rs.getString("GUARD_WORK_NAME_2"));
                svf.VrsOut("GUARD_COMPANY_TEL_NO2", rs.getString("GUARD_WORK_TELNO_2"));

                //兄弟姉妹
                psFamily = db2.prepareStatement(familySql.toString());
                psFamily.setString(1, rs.getString("FAMILY_NO"));
                psFamily.setString(2, rs.getString("SCHREGNO"));
                rsFamily = psFamily.executeQuery();
                int brosysCnt = 1;
                while (rsFamily.next()) {
                    svf.VrsOut("BROSYS_HR_NAME" + brosysCnt, rsFamily.getString("HR_NAME"));
                    svf.VrsOut("BROSYS_NAME" + brosysCnt, rsFamily.getString("RELANAME"));
                    brosysCnt++;
                }

                //引き取り者
                svf.VrsOut("TAKEBACK_NAME1", rs.getString("TAKEBACK_NAME1"));
                svf.VrsOut("TAKEBACK_RELATION1", rs.getString("TAKEBACK_RELATION1"));
                svf.VrsOut("TAKEBACK_TEL_NO1", rs.getString("TAKEBACK_TEL_NO1"));

                svf.VrsOut("TAKEBACK_NAME2", rs.getString("TAKEBACK_NAME2"));
                svf.VrsOut("TAKEBACK_RELATION2", rs.getString("TAKEBACK_RELATION2"));
                svf.VrsOut("TAKEBACK_TEL_NO2", rs.getString("TAKEBACK_TEL_NO2"));

                svf.VrsOut("TAKEBACK_NAME3", rs.getString("TAKEBACK_NAME3"));
                svf.VrsOut("TAKEBACK_RELATION3", rs.getString("TAKEBACK_RELATION3"));
                svf.VrsOut("TAKEBACK_TEL_N3", rs.getString("TAKEBACK_TEL_NO3")); //フィールド誤り

                svf.VrsOut("REFUGE_NAME1", rs.getString("REFUGE_NAME1"));
                svf.VrsOut("REFUGE_ZIPNO1", rs.getString("REFUGE_ZIPNO1") != null ? "〒" + rs.getString("REFUGE_ZIPNO1") : "");
                printTelNo(svf, "REFUGE_TEL_NO1", rs.getString("REFUGE_TEL_NO1_1"), rs.getString("REFUGE_TEL_NO1_2"));
                svf.VrsOut("REFUGE_ADDR1_1", rs.getString("REFUGE_ADDR1_1"));
                svf.VrsOut("REFUGE_ADDR1_2", rs.getString("REFUGE_ADDR1_2"));

                svf.VrsOut("REFUGE_NAME2", rs.getString("REFUGE_NAME2"));
                svf.VrsOut("REFUGE_ZIPNO2", rs.getString("REFUGE_ZIPNO2") != null ? "〒" + rs.getString("REFUGE_ZIPNO2") : "");
                printTelNo(svf, "REFUGE_TEL_NO2", rs.getString("REFUGE_TEL_NO2_1"), rs.getString("REFUGE_TEL_NO2_2"));
                svf.VrsOut("REFUGE_ADDR2_1", rs.getString("REFUGE_ADDR2_1"));
                svf.VrsOut("REFUGE_ADDR2_2", rs.getString("REFUGE_ADDR2_2"));

                textCnt = 1;
                for (Iterator itFootTitle = _param._footerList.iterator(); itFootTitle.hasNext();) {
                    final String titleText = (String) itFootTitle.next();
                    svf.VrsOutn("TEXT2", textCnt, null != titleText && !"null".equals(titleText) ? titleText : "");
                    textCnt++;
                }

                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, psFamily, rsFamily);
        }
    }

    //電話番号が2つ登録あれば2つ印字する
    private void printTelNo(final Vrw32alp svf, final String field, final String tel1, final String tel2) {
        if (tel1 != null && tel2 == null) {
            svf.VrsOut(field, tel1);
        } else if (tel1 == null && tel2 != null) {
            svf.VrsOut(field, tel2);
        } else if (tel1 != null && tel2 != null){
            svf.VrsOut(field + "_2", tel1 + " " + tel2);
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     VALUE(BASE_009.BASE_REMARK1, '') AS FAMILY_NO, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.SEX, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     VALUE(BASE.BLOODTYPE, '') AS BLOODTYPE, ");
        stb.append("     VALUE(BASE.BLOOD_RH, '') AS BLOOD_RH, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.TELNO, ");
        stb.append("     ADDR.ADDR1, ");
        stb.append("     ADDR.ADDR2, ");
        stb.append("     H201_1.NAME1 AS RELA_1, ");
        stb.append("     GUARD1.GUARD_NAME AS GUARD_NAME_1, ");
        stb.append("     GUARD1.GUARD_TELNO2 AS KEITAI_1, ");
        stb.append("     GUARD1.GUARD_WORK_TELNO AS GUARD_WORK_TELNO_1, ");
        stb.append("     GUARD1.GUARD_WORK_NAME AS GUARD_WORK_NAME_1, ");
        stb.append("     H201_2.NAME1 AS RELA_2, ");
        stb.append("     GUARD2.GUARD_NAME AS GUARD_NAME_2, ");
        stb.append("     GUARD2.GUARD_TELNO2 AS KEITAI_2, ");
        stb.append("     GUARD2.GUARD_WORK_TELNO AS GUARD_WORK_TELNO_2, ");
        stb.append("     GUARD2.GUARD_WORK_NAME AS GUARD_WORK_NAME_2, ");
        stb.append("     ENV001.REMARK1 AS TAKEBACK_NAME1, ");
        stb.append("     ENV_H201_1.NAME1 AS TAKEBACK_RELATION1, ");
        stb.append("     ENV001.REMARK3 AS TAKEBACK_TEL_NO1, ");
        stb.append("     ENV002.REMARK1 AS TAKEBACK_NAME2, ");
        stb.append("     ENV_H201_2.NAME1 AS TAKEBACK_RELATION2, ");
        stb.append("     ENV002.REMARK3 AS TAKEBACK_TEL_NO2, ");
        stb.append("     ENV003.REMARK1 AS TAKEBACK_NAME3, ");
        stb.append("     ENV_H201_3.NAME1 AS TAKEBACK_RELATION3, ");
        stb.append("     ENV003.REMARK3 AS TAKEBACK_TEL_NO3, ");
        stb.append("     ENV004.REMARK1 AS REFUGE_NAME1, ");
        stb.append("     ENV004.REMARK2 AS REFUGE_ZIPNO1, ");
        stb.append("     ENV004.REMARK3 AS REFUGE_ADDR1_1, ");
        stb.append("     ENV004.REMARK4 AS REFUGE_ADDR1_2, ");
        stb.append("     ENV004.REMARK5 AS REFUGE_TEL_NO1_1, ");
        stb.append("     ENV004.REMARK6 AS REFUGE_TEL_NO1_2, ");
        stb.append("     ENV005.REMARK1 AS REFUGE_NAME2, ");
        stb.append("     ENV005.REMARK2 AS REFUGE_ZIPNO2, ");
        stb.append("     ENV005.REMARK3 AS REFUGE_ADDR2_1, ");
        stb.append("     ENV005.REMARK4 AS REFUGE_ADDR2_2, ");
        stb.append("     ENV005.REMARK5 AS REFUGE_TEL_NO2_1, ");
        stb.append("     ENV005.REMARK6 AS REFUGE_TEL_NO2_2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("         AND REGDH.GRADE = REGD.GRADE ");
        stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("                   FROM SCHREG_ADDRESS_DAT  ");
        stb.append("                   GROUP BY SCHREGNO) ADDR_MAX ON ADDR_MAX.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
        stb.append("         AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("         AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD1 ON BASE.SCHREGNO = GUARD1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST H201_1 ON H201_1.NAMECD1 = 'H201' ");
        stb.append("          AND GUARD1.RELATIONSHIP = H201_1.NAMECD2 ");
        stb.append("     LEFT JOIN GUARDIAN2_DAT GUARD2 ON BASE.SCHREGNO = GUARD2.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST H201_2 ON H201_2.NAMECD1 = 'H201' ");
        stb.append("          AND GUARD2.RELATIONSHIP = H201_2.NAMECD2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_009 ON BASE.SCHREGNO = BASE_009.SCHREGNO ");
        stb.append("          AND BASE_009.BASE_SEQ = '009' ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENV001 ON ENV001.SCHREGNO = BASE.SCHREGNO  ");
        stb.append("          AND ENV001.SEQ = '001' ");
        stb.append("     LEFT JOIN NAME_MST ENV_H201_1 ON ENV_H201_1.NAMECD1 = 'H201' ");
        stb.append("          AND ENV_H201_1.NAMECD2 = ENV001.REMARK2 ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENV002 ON ENV002.SCHREGNO = BASE.SCHREGNO ");
        stb.append("          AND ENV002.SEQ = '002' ");
        stb.append("     LEFT JOIN NAME_MST ENV_H201_2 ON ENV_H201_2.NAMECD1 = 'H201' ");
        stb.append("          AND ENV_H201_2.NAMECD2 = ENV002.REMARK2 ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENV003 ON ENV003.SCHREGNO = BASE.SCHREGNO ");
        stb.append("          AND ENV003.SEQ = '003' ");
        stb.append("     LEFT JOIN NAME_MST ENV_H201_3 ON ENV_H201_3.NAMECD1 = 'H201' ");
        stb.append("          AND ENV_H201_3.NAMECD2 = ENV003.REMARK2 ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENV004 ON ENV004.SCHREGNO = BASE.SCHREGNO ");
        stb.append("          AND ENV004.SEQ = '004' ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENV005 ON ENV005.SCHREGNO = BASE.SCHREGNO ");
        stb.append("          AND ENV005.SEQ = '005' ");
        stb.append(" WHERE ");
        if ("1".equals(_param._disp)) {
            stb.append("     BASE.SCHREGNO IN (" + _param._sqlInSentence + ") ");
        } else {
            stb.append("     REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInSentence + ") ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _disp; // 1:個人,2:クラス
        private final String[] _category_selected;
        private final String _sqlInSentence;
        private final String _printDate;
        private final String _limitDate;
        private final String _documentroot;
        private final String _useFamilyDat;
        private String _certifSchoolSchoolName;
        private String _certifSchoolSchoolAddress;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark3;
        private final List _titleList;
        private final List _footerList;

        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _printDate = StringUtils.defaultString(request.getParameter("PRINT_DATE")).replace('/', '-');
            _limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE")).replace('/', '-');
            _category_selected = request.getParameterValues("category_selected");
            String setSentence = "";
            String sep = "";
            for (int i = 0; i < _category_selected.length; i++) {
                final String schregOrGradeHr = StringUtils.split(_category_selected[i], "-")[0];
                setSentence += sep + "'" + schregOrGradeHr + "'";
                sep = ",";
            }
            _sqlInSentence = setSentence;
            _documentroot = request.getParameter("DOCUMENTROOT");
            _useFamilyDat = request.getParameter("useFamilyDat");

            setCertifSchool(db2);
            _useAddrField2 = request.getParameter("useAddrField2");

            _titleList = new ArrayList();
            getDocumentMst(db2, _titleList, "", "B1", 92, 9);
            final String limitWeekJp = KNJ_EditDate.h_format_W(_limitDate);
            final String setPlusText = "　※" + KNJ_EditDate.h_format_JP_MD(_limitDate) + "(" + limitWeekJp + ")";
            getDocumentMst(db2, _titleList, setPlusText, "B2", 92, 2);
            _footerList = new ArrayList();
            getDocumentMst(db2, _footerList, "", "B3", 80, 2);

        }

        private void setCertifSchool(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _certifSchoolSchoolName = "";
            _certifSchoolSchoolAddress = "";
            _certifSchoolJobName = "";
            _certifSchoolPrincipalName = "";
            String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1, REMARK3 " +
                         "FROM CERTIF_SCHOOL_DAT " +
                         "WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '141' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolSchoolName = null != rs.getString("SCHOOL_NAME") ? rs.getString("SCHOOL_NAME") : "";
                    _certifSchoolSchoolAddress = null != rs.getString("REMARK1") ? rs.getString("REMARK1") : "";
                    _certifSchoolJobName = null != rs.getString("JOB_NAME") ? rs.getString("JOB_NAME") : "";
                    _certifSchoolPrincipalName = null != rs.getString("PRINCIPAL_NAME") ? rs.getString("PRINCIPAL_NAME") : "";
                    _certifSchoolRemark3 = null != rs.getString("REMARK3") ? rs.getString("REMARK3") : "";
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void getDocumentMst(final DB2UDB db2, List setList, final String plusText, final String documentCd, final int fLen, final int fCnt) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT * FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + documentCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String setPlusText = plusText;
                while (rs.next()) {
                    final String getText = null != rs.getString("TEXT") ? rs.getString("TEXT") : "";
                    final String[] getTextArray = KNJ_EditEdit.get_token(getText, fLen, fCnt);
                    for (int i = 0; i < getTextArray.length; i++) {
                        final String setText = getTextArray[i];
                        setList.add(setPlusText + setText);
                        setPlusText = "";
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}

// eof


/*
 * $Id: a17bb0393def33ad8c2f9e9138961147e0ae9d13 $
 *
 * 作成日: 2016/09/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

public class KNJG020A {

    private static final Log log = LogFactory.getLog(KNJG020A.class);

    private boolean _hasData;
    static final int MAX_LINE = 20;

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

    private Map getList(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            List printList = new ArrayList();
            int cnt = 1;
            int pageCnt = 1;
            while (rs.next()) {
                if (cnt > MAX_LINE) {
                    retMap.put(String.valueOf(pageCnt), printList);
                    pageCnt++;
                    cnt = 1;
                    printList = new ArrayList();
                }
                final String printNo = rs.getString("PRINT_NO");
                final String issuedate = rs.getString("ISSUEDATE");
                final String remark1 = rs.getString("REMARK1");
                final String certifNo = rs.getString("CERTIF_NO");
                final String certifIndex = rs.getString("CERTIF_INDEX");
                final String gradeHrAtte = rs.getString("GRADE_HR_ATTE");
                final String name = rs.getString("NAME");
                final String realName = rs.getString("REAL_NAME");
                final String printRealName = rs.getString("PRINT_REAL_NAME");
                final String nameOutputFlg = rs.getString("NAME_OUTPUT_FLG");
                final String schregNo = rs.getString("SCHREGNO");
                final String kindName = rs.getString("KINDNAME");
                final String cancel = rs.getString("CANCEL");
                final PrintData printData = new PrintData(printNo, issuedate, remark1, certifNo, certifIndex, gradeHrAtte, name, realName, printRealName, nameOutputFlg, schregNo, kindName, cancel);
                printList.add(printData);
                cnt++;
            }
            if (cnt > 1) {
                retMap.put(String.valueOf(pageCnt), printList);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJG020A.frm", 1);
        final Map printMap = getList(db2);
        final String[] printPages = StringUtils.split(_param._printPage, ',');
        for (int i = 0; i < printPages.length; i++) {
        	final List PrintList = (List) printMap.get(printPages[i]);
        	if (null == PrintList) {
        		continue;
        	}
            svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("scollname", _param._schoolname);
            int gyo = 1;
            for (Iterator itPrint = PrintList.iterator(); itPrint.hasNext();) {
                final PrintData printData = (PrintData) itPrint.next();
                svf.VrsOutn("PRINT_NO", gyo, printData._printNo);
                svf.VrsOutn("date", gyo, StringUtils.replace(printData._issuedate, "-", "/"));
                svf.VrsOutn("bango", gyo, printData.getBango());
                svf.VrsOutn("HR_NAME", gyo, printData._gradeHrAtte);
                final String nameField = getMS932ByteLength(printData._name) > 20 ? "2_1" : "";
                svf.VrsOutn("name" + nameField, gyo, printData._name);
                svf.VrsOutn("REMARK1", gyo, printData._kindName);
                svf.VrsOutn("REMARK2", gyo, printData._cancel);
                gyo++;
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" CERTIF_DATA AS( ");
        stb.append("     SELECT  T1.CERTIF_INDEX ");
        stb.append("     ,T2.REMARK1 ");
        stb.append("     ,T1.CERTIF_NO ");
        stb.append("     ,T1.CERTIF_KINDCD ");
        stb.append("     ,T1.ISSUEDATE ");
        stb.append("     ,T1.SCHREGNO ");
        stb.append("     ,CASE WHEN T2.REMARK15 = '1' ");
        stb.append("           THEN 'キャンセル' ");
        stb.append("           ELSE '' ");
        stb.append("      END AS CANCEL ");
        stb.append("     ,VALUE(T1.GRADUATE_FLG, '0') AS GRADUATE_FLG ");
        stb.append(" FROM ");
        stb.append("     CERTIF_ISSUE_DAT T1 ");
        stb.append("     LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ");
        stb.append("          ON T2.YEAR = T1.YEAR ");
        stb.append("          AND T2.CERTIF_INDEX = T1.CERTIF_INDEX ");
        stb.append("          AND T2.TYPE = '1' ");
        stb.append(" WHERE   T1.ISSUECD = '1' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND (T2.REMARK1 IS NOT NULL ");
        stb.append("          OR T1.CERTIF_NO IS NOT NULL) ");
        if ("2".equals(_param._stdDiv)) {
            stb.append("     AND T1.GRADUATE_FLG = '0' ");
        } else if ("1".equals(_param._stdDiv)) {
            stb.append("     AND T1.GRADUATE_FLG = '1' ");
        }
        stb.append(" ) ");

        stb.append("     ,SCHREG_DATA AS( ");
        stb.append("         SELECT ");
        stb.append("             '0' AS GRAD_KUBUN, SCHREGNO, NAME, REAL_NAME, GRD_DATE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_BASE_MST W1 ");
        stb.append("         WHERE ");
        stb.append("             EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
        if (!"2".equals(_param._stdDiv)) {
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         '1' AS GRAD_KUBUN, SCHREGNO, NAME, REAL_NAME, GRD_DATE ");
            stb.append("     FROM ");
            stb.append("         GRD_BASE_MST W1 ");
            stb.append("     WHERE ");
            stb.append("         EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         '1' AS GRAD_KUBUN, SCHREGNO, NAME, REAL_NAME, GRD_DATE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_BASE_MST W1 ");
            stb.append("     WHERE ");
            stb.append("         EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
            stb.append("         AND NOT EXISTS(SELECT 'X' FROM GRD_BASE_MST W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
        }
        stb.append(" ), REGD_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.HR_NAME, ");
        stb.append("     L1.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolKindCmb + "' ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.GRADE = L1.GRADE ");
        stb.append("          AND T1.HR_CLASS = L1.HR_CLASS, ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(SEMESTER) AS MAX_SEME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("         AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_DATA) ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.MAX_SEME ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ), GREGD_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.HR_NAME, ");
        stb.append("     L1.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     GRD_REGD_DAT T1 ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolKindCmb + "' ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        }
        stb.append("     LEFT JOIN GRD_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.GRADE = L1.GRADE ");
        stb.append("          AND T1.HR_CLASS = L1.HR_CLASS, ");
        stb.append("     (SELECT ");
        stb.append("         W1.YEAR, ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         MAX(W1.SEMESTER) AS MAX_SEME ");
        stb.append("     FROM ");
        stb.append("         GRD_REGD_DAT W1, ");
        stb.append("        (SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(YEAR) AS MAX_YEAR ");
        stb.append("         FROM ");
        stb.append("             GRD_REGD_DAT ");
        stb.append("         WHERE ");
        stb.append("             SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_DATA) ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("        ) W2 ");
        stb.append("     WHERE ");
        stb.append("         W1.SCHREGNO = W2.SCHREGNO ");
        stb.append("         AND W1.SCHREGNO = W2.MAX_YEAR ");
        stb.append("     GROUP BY ");
        stb.append("         W1.YEAR, ");
        stb.append("         W1.SCHREGNO ");
        stb.append("     ) T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.MAX_SEME ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" ,DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("     W1.CERTIF_INDEX ");
        stb.append("     ,W1.REMARK1 ");
        stb.append("     ,W1.CERTIF_NO ");
        stb.append("     ,W1.CERTIF_KINDCD ");
        stb.append("     ,W1.ISSUEDATE ");
        stb.append("     ,W1.SCHREGNO ");
        stb.append("     ,W1.CANCEL ");
        stb.append("     ,W1.GRADUATE_FLG ");
        stb.append("     FROM    CERTIF_DATA W1 ");
        stb.append("     WHERE   EXISTS(SELECT 'X' FROM SCHREG_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     ORDER.PRINT_NO ");
        stb.append("     ,T1.ISSUEDATE ");
        stb.append("     ,T1.CERTIF_NO ");
        stb.append("     ,T1.REMARK1 ");
        stb.append("     ,T1.CERTIF_INDEX ");
        stb.append("     ,CASE WHEN VALUE(T1.GRADUATE_FLG,'0') = '0' OR GREGD_T.SCHREGNO IS NULL ");
        stb.append("           THEN REGD_T.HR_NAME || REGD_T.ATTENDNO || '番' ");
        stb.append("           ELSE GREGD_T.HR_NAME || GREGD_T.ATTENDNO || '番' ");
        stb.append("      END AS GRADE_HR_ATTE ");
        stb.append("     ,T2.NAME ");
        stb.append("     ,T2.REAL_NAME ");
        stb.append("     ,T4.SCHREGNO AS PRINT_REAL_NAME ");
        stb.append("     ,T4.NAME_OUTPUT_FLG ");
        stb.append("     ,T1.SCHREGNO ");
        stb.append("     ,T3.KINDNAME ");
        stb.append("     ,T1.CANCEL ");
        stb.append(" FROM ");
        stb.append("     DATA T1 ");
        stb.append("     INNER JOIN SCHREG_DATA T2 ON T2.GRAD_KUBUN = T1.GRADUATE_FLG ");
        stb.append("           AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT  JOIN CERTIF_KIND_MST T3 ON T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
        stb.append("     LEFT  JOIN SCHREG_NAME_SETUP_DAT T4 ON T4.DIV = '07' AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN REGD_T ON REGD_T.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GREGD_T ON GREGD_T.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN CERTIF_ISSUE_PRINT_ORDER_DAT ORDER ON ORDER.YEAR = '" + _param._year + "' ");
        stb.append("          AND T1.CERTIF_INDEX = ORDER.CERTIF_INDEX ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append(" WHERE ");
            stb.append("     REGD_T.SCHREGNO IS NOT NULL ");
            stb.append("     OR GREGD_T.SCHREGNO IS NOT NULL ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolkind)) {
            stb.append(" WHERE ");
            stb.append("     REGD_T.SCHREGNO IS NOT NULL ");
            stb.append("     OR GREGD_T.SCHREGNO IS NOT NULL ");
        }
        stb.append(" ORDER BY VALUE(ORDER.PRINT_NO, 999999), T1.ISSUEDATE, CAST(T1.REMARK1 AS INT), CAST(T1.CERTIF_NO AS INT) ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 59121 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private class PrintData {
        private final String _printNo;
        private final String _issuedate;
        private final String _remark1;
        private final String _certifNo;
        private final String _certifIndex;
        private final String _gradeHrAtte;
        private final String _name;
        private final String _realName;
        private final String _printRealName;
        private final String _nameOutputFlg;
        private final String _schregNo;
        private final String _kindName;
        private final String _cancel;

        public PrintData(
                final String printNo,
                final String issuedate,
                final String remark1,
                final String certifNo,
                final String certifIndex,
                final String gradeHrAtte,
                final String name,
                final String realName,
                final String printRealName,
                final String nameOutputFlg,
                final String schregNo,
                final String kindName,
                final String cancel
        ) {
            _printNo        = printNo;
            _issuedate      = issuedate;
            _remark1        = remark1;
            _certifNo       = certifNo;
            _certifIndex    = certifIndex;
            _gradeHrAtte    = gradeHrAtte;
            _name           = name;
            _realName       = realName;
            _printRealName  = printRealName;
            _nameOutputFlg  = nameOutputFlg;
            _schregNo       = schregNo;
            _kindName       = kindName;
            _cancel         = cancel;
        }

        public String getBango() {
            String retStr = _certifNo;
            if (null != _remark1 && !"".equals(_remark1)) {
                retStr = _remark1;
            }
            return retStr;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _stdDiv;
        private final String _pageCnt;
        private final String _prgid;
        private final String _certifNoSyudou;
        private final String _schoolkind;
        private final String _useSchool_KindField;
        private final String _certif_no_8keta;
        private final String _printPage;
        private final String _foundedyear;
        private final String _schoolname;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String _schoolKindCmb;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _stdDiv = request.getParameter("STD_DIV");
            _pageCnt = request.getParameter("PAGE_CNT");
            _prgid = request.getParameter("PRGID");
            _certifNoSyudou = request.getParameter("certifNoSyudou");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _certif_no_8keta = request.getParameter("certif_no_8keta");
            _printPage = request.getParameter("PrintPage");
            String[] getSchool = getFoundedyear(db2);
            _foundedyear = getSchool[0];
            _schoolname = getSchool[1];
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _schoolKindCmb = request.getParameter("SCHOOL_KIND");
        }

        /**
         *  SVF-FORM 見出し設定
         */
        private String[] getFoundedyear(final DB2UDB db2)
        {
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            String[] retStr = new String[2];
            try {
                KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("10000");
                final Map paramMap = new HashMap();
                if ("1".equals(_use_prg_schoolkind)) {
                    paramMap.put("schoolMstSchoolKind", _schoolKindCmb);
                } else if (!"SCHOOLKIND".equals(_schoolkind)) {
                    paramMap.put("schoolMstSchoolKind", _schoolkind);
                }
                ps1 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql(paramMap));
                int p = 0;
                ps1.setString(++p, _year);
                ps1.setString(++p, _year);
                rs = ps1.executeQuery();
                retStr[0] = "0";
                retStr[1] = "";
                if (rs.next()) {
                    if (rs.getString("FOUNDEDYEAR") != null) { retStr[0] = rs.getString("FOUNDEDYEAR"); }
                    if (rs.getString("SCHOOLNAME1") != null) { retStr[1] = rs.getString("SCHOOLNAME1"); }
                }
            } catch (Exception ex) {
                log.warn("now date-get error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof


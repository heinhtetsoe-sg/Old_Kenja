// kanji=漢字
/*
 * $Id: 3e952d9895ed13fc4fe5ee68dbce37bdb54bd924 $
 *
 * 作成日: 2013/07/08 15:05:52 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 3e952d9895ed13fc4fe5ee68dbce37bdb54bd924 $
 */
public class KNJJ130 {

    private static final Log log = LogFactory.getLog("KNJJ130.class");

    private boolean _hasData;

    private Param _param;
    private String _useSchool_KindField;
    private String _SCHOOLCD;
    private String _SCHOOLKIND;
    private String use_prg_schoolkind;
    private String useClubMultiSchoolKind;
    private String selectSchoolKind;
    private String selectSchoolKindSql;
    private String SCHKIND;

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
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
            SCHKIND = request.getParameter("SCHKIND");

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final String mainSql = getMainSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(mainSql);
            rs = ps.executeQuery();
            String befClubCd = "";
            svf.VrSetForm("KNJJ130.frm", 4);
            while (rs.next()) {
                if (!"".equals(befClubCd) && !befClubCd.equals(rs.getString("CLUBCD"))) {
                    svf.VrSetForm("KNJJ130.frm", 4);
                }
                final String title = KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　大会結果一覧";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    if ("H".equals(_SCHOOLKIND)) {
                        svf.VrsOut("FINSCHOOL_TITLE", "中学校名");
                    } else if ("J".equals(_SCHOOLKIND)) {
                        svf.VrsOut("FINSCHOOL_TITLE", "小学校名");
                    } else if ("P".equals(_SCHOOLKIND)) {
                        svf.VrsOut("FINSCHOOL_TITLE", "幼稚園名");
                    }
                }
                svf.VrsOut("TITLE", title);
                svf.VrsOut("CLUB_NAME", rs.getString("CLUBCD") + "：" + rs.getString("CLUBNAME"));
                final String printDate = KNJ_EditDate.h_format_JP(_param._ctrlDate);
                svf.VrsOut("PRINT_DATE", printDate);
                svf.VrsOut("HOST_NAME", rs.getString("HOSTNAME"));
                svf.VrsOut("MEET_NAME", rs.getString("MEET_NAME"));
                svf.VrsOut("DETAIL_DATE", rs.getString("DETAIL_DATE").replace('-', '/'));
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));
                if (null != rs.getString("KINDNAME")) {
                    final String fieldNo = (10 < rs.getString("KINDNAME").length()) ? "3" : (5 < rs.getString("KINDNAME").length()) ? "2" : "";
                    svf.VrsOut("CLUB_ITEM" + fieldNo, rs.getString("KINDNAME"));
                }
                svf.VrsOut("DIV", rs.getString("DIV_NAME"));
                if (null != rs.getString("RECORDNAME")) {
                    final String fieldNo = (10 < rs.getString("RECORDNAME").length()) ? "3_1" : (5 < rs.getString("RECORDNAME").length()) ? "2" : "";
                    svf.VrsOut("RECORD_NAME" + fieldNo, rs.getString("RECORDNAME"));
                }
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                svf.VrsOut("NO", String.valueOf(rs.getInt("ATTENDNO")));
                if (null != rs.getString("NAME")) {
                    final String fieldNo = (15 < rs.getString("NAME").length()) ? "3" : (10 < rs.getString("NAME").length()) ? "2" : "1";
                    svf.VrsOut("NAME" + fieldNo, rs.getString("NAME"));
                }
                if (null != rs.getString("FINSCHOOL_NAME")) {
                    final String fieldNo = (10 < rs.getString("FINSCHOOL_NAME").length()) ? "3" : (5 < rs.getString("FINSCHOOL_NAME").length()) ? "2" : "1";
                    svf.VrsOut("JHSCHOOL_NAME" + fieldNo, rs.getString("FINSCHOOL_NAME"));
                }
                if (null != rs.getString("REMARK")) {
                    final String fieldNo = (15 < rs.getString("REMARK").length()) ? "2" : "1";
                    svf.VrsOut("REMARK" + fieldNo, rs.getString("REMARK"));
                }
                befClubCd = rs.getString("CLUBCD");
                svf.VrEndRecord();
                _hasData = true;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * @return
     */
    private String getMainSql() {
    	final String sDate = _param._ctrlYear + "-04-01";
    	final String eDate =  (Integer.parseInt((_param._ctrlYear)) + 1) + "-03-31";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_REGD AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append(" 	 							AND GDAT.GRADE = T1.GRADE ");
        //部活動複数校種設定の場合はメインのSELECT文のWHERE句で校種を絞るようにしました。
        if (!"1".equals(useClubMultiSchoolKind)) {
	        if ("1".equals(use_prg_schoolkind)) {
	            if (!StringUtils.isBlank(selectSchoolKindSql)) {
	                stb.append("        AND GDAT.SCHOOL_KIND IN " + selectSchoolKindSql + "  ");
	            }
	            if (null != SCHKIND) {
	                stb.append("        AND GDAT.SCHOOL_KIND = '" + SCHKIND + "'  ");
	            }
	        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
	            stb.append("        AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
	        }
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.CLUBCD, ");
        stb.append("     L0.CLUBNAME, ");
        stb.append("     T1.HOSTCD, ");
        stb.append("     L1.HOSTNAME, ");
        stb.append("     T1.MEET_NAME, ");
        stb.append("     T1.DETAIL_DATE, ");
        stb.append("     L2.SEX, ");
        stb.append("     L3.NAME1 AS SEX_NAME, ");
        stb.append("     T1.ITEMCD, ");
        stb.append("     T1.KINDCD, ");
        stb.append("     L4.KINDNAME, ");
        stb.append("     T1.DIV, ");
        stb.append("     CASE WHEN T1.DIV = '1' ");
        stb.append("          THEN '個人' ");
        stb.append("          ELSE '団体' ");
        stb.append("     END AS DIV_NAME, ");
        stb.append("     T1.RECORDCD, ");
        stb.append("     L5.RECORDNAME, ");
        stb.append("     L7.HR_NAME, ");
        stb.append("     L6.ATTENDNO, ");
        stb.append("     L2.NAME, ");
        stb.append("     L2.FINSCHOOLCD, ");
        stb.append("     L8.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN T1.DOCUMENT IS NOT NULL AND T1.DETAIL_REMARK IS NOT NULL ");
        stb.append("          THEN T1.DOCUMENT || '/' || T1.DETAIL_REMARK ");
        stb.append("          ELSE CASE WHEN T1.DOCUMENT IS NOT NULL ");
        stb.append("                    THEN T1.DOCUMENT ");
        stb.append("                    ELSE T1.DETAIL_REMARK ");
        stb.append("               END ");
        stb.append("     END AS REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HDETAIL_DAT T1 ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   LEFT JOIN CLUB_DETAIL_DAT CDET001 ON CDET001.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND CDET001.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND CDET001.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND CDET001.CLUBCD = T1.CLUBCD ");
            stb.append("   AND CDET001.SEQ = '001' ");
        }
        stb.append("     LEFT JOIN CLUB_MST L0 ON T1.CLUBCD = L0.CLUBCD ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND L0.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND L0.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND L0.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        } else if ("1".equals(use_prg_schoolkind)) {
            stb.append("   AND L0.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND L0.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND L0.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
            if (null != SCHKIND) {
                stb.append("        AND L0.SCHOOL_KIND = '" + SCHKIND + "'  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND L0.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND L0.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND L0.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("     LEFT JOIN CLUB_HOST_MST L1 ON T1.HOSTCD = L1.HOSTCD ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND L1.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L1.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ");
            stb.append("   AND L1.SCHOOLCD = '" + _SCHOOLCD + "' ");
        } else if ("1".equals(use_prg_schoolkind)) {
            stb.append("   AND L1.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND L1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND L1.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
            if (null != SCHKIND) {
                stb.append("        AND L1.SCHOOL_KIND = '" + SCHKIND + "'  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND L1.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND L1.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND L1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' ");
        stb.append("          AND L2.SEX = L3.NAMECD2 ");
        stb.append("     LEFT JOIN CLUB_ITEM_KIND_MST L4 ON T1.ITEMCD = L4.ITEMCD ");
        stb.append("          AND T1.KINDCD = L4.KINDCD ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND L4.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L4.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ");
            stb.append("   AND L4.SCHOOLCD = '" + _SCHOOLCD + "' ");
        } else if ("1".equals(use_prg_schoolkind)) {
            stb.append("   AND L4.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND L4.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND L4.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
            if (null != SCHKIND) {
                stb.append("        AND L4.SCHOOL_KIND = '" + SCHKIND + "'  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND L4.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND L4.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND L4.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("     LEFT JOIN CLUB_RECORD_MST L5 ON T1.RECORDCD = L5.RECORDCD ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND L5.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L5.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ");
            stb.append("   AND L5.SCHOOLCD = '" + _SCHOOLCD + "' ");
        } else if ("1".equals(use_prg_schoolkind)) {
            stb.append("   AND L5.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND L5.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND L5.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
            if (null != SCHKIND) {
                stb.append("        AND L5.SCHOOL_KIND = '" + SCHKIND + "'  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND L5.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND L5.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND L5.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("     INNER JOIN (SELECT ");
        stb.append("                    R1.* ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_REGD_DAT R1, ");
        stb.append("                    MAX_REGD R2 ");
        stb.append("                WHERE ");
        stb.append("                    R1.YEAR = R2.YEAR ");
        stb.append("                    AND R1.SEMESTER = R2.SEMESTER ");
        stb.append("                    AND R1.SCHREGNO = R2.SCHREGNO ");
        stb.append("     ) L6 ON T1.SCHREGNO = L6.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGD ON L6.YEAR = REGD.YEAR ");
        stb.append("          AND L6.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L7 ON L6.YEAR = L7.YEAR ");
        stb.append("          AND L6.SEMESTER = L7.SEMESTER ");
        stb.append("          AND L6.GRADE = L7.GRADE ");
        stb.append("          AND L6.HR_CLASS = L7.HR_CLASS ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L8 ON L2.FINSCHOOLCD = L8.FINSCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     T1.CLUBCD " + _param._clubInState + " ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            stb.append("   AND T1.DETAIL_SCHOOL_KIND = REGD.SCHOOL_KIND ");
            stb.append("   AND CDET001.REMARK1 LIKE CONCAT(CONCAT('%', T1.DETAIL_SCHOOL_KIND), '%') ");
        } else if ("1".equals(use_prg_schoolkind)) {
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
            if (null != SCHKIND) {
                stb.append("        AND T1.SCHOOL_KIND = '" + SCHKIND + "'  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("   AND T1.DETAIL_DATE BETWEEN '"+ sDate +"' AND '"+ eDate +"' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLUBCD, ");
        if (!_param._sortInState.equals("")) {
            stb.append(" " + _param._sortInState + " ");
        } else {
            stb.append("     T1.HOSTCD, ");
            stb.append("     T1.DETAIL_DATE, ");
            stb.append("     T1.ITEMCD, ");
            stb.append("     T1.KINDCD, ");
            stb.append("     T1.RECORDCD, ");
            stb.append("     L5.RECORDNAME, ");
            stb.append("     L6.GRADE DESC, ");
            stb.append("     L6.HR_CLASS, ");
            stb.append("     L6.ATTENDNO ");
        }
        log.fatal("sql="+stb);
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75937 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        final String[] _clubSelected;
        String _clubInState = "";
        final String[] _sortSelected;
        String _sortInState = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _clubSelected = request.getParameterValues("CLUB_SELECTED");
            _clubInState = "IN ('";
            String sep = "";
            for (int ia = 0; ia < _clubSelected.length; ia++) {
                _clubInState += sep + _clubSelected[ia];
                sep = "','";
            }
            _clubInState += "')";

            if (request.getParameterValues("SORT_SELECTED") != null) {
                _sortSelected = request.getParameterValues("SORT_SELECTED");
            } else {
                _sortSelected = new String[0];
            }
            _sortInState = "";
            String sep2 = "";
            if (request.getParameterValues("SORT_SELECTED") != null) {
                for (int ia = 0; ia < _sortSelected.length; ia++) {
                    if (_sortSelected[ia].equals("01")) {
                        _sortInState += sep2 + "T1.HOSTCD";
                    }
                    if (_sortSelected[ia].equals("02")) {
                        _sortInState += sep2 + "T1.DETAIL_DATE";
                    }
                    if (_sortSelected[ia].equals("03")) {
                        _sortInState += sep2 + "T1.ITEMCD, T1.KINDCD";
                    }
                    if (_sortSelected[ia].equals("04")) {
                        _sortInState += sep2 + "T1.RECORDCD";
                    }
                    if (_sortSelected[ia].equals("05")) {
                        _sortInState += sep2 + "L6.GRADE DESC, L6.HR_CLASS, L6.ATTENDNO";
                    }
                    sep2 = ",";
                }
            }
        }

    }
}

// eof

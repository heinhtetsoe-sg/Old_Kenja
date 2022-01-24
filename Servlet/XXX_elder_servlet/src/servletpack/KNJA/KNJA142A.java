// kanji=漢字
/*
 * $Id: eea705be55d0764f07dec01d0efa612ce6c28162 $
 *
 * 作成日: 2010/03/24 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: eea705be55d0764f07dec01d0efa612ce6c28162 $
 */
public class KNJA142A {

    private static final Log log = LogFactory.getLog("KNJA142A.class");

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
    	final int maxLine = 5;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String form = "KNJA142A.frm";
            svf.VrSetForm(form, 1);
            int line = 1;
            int no = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line  = 1;
                }

                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String grade = rs.getString("GRADE_CD");
                final String hrclass = rs.getString("HR_CLASS");
                final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO");
                final String rosen = rs.getString("ROSEN_1");

                svf.VrsOutn("NENDO" + (no % 2 == 1 ? "1" : "2"), line, _param._year + "年度");
                svf.VrsOutn("SCHREGNO" + (no % 2 == 1 ? "1" : "2"), line, schregno);

                final String printgrade = seikei(grade);
                final String printhrclass = seikei(hrclass);
                final String printattendo = seikei(attendno2);

                final String syozoku = printgrade + "年 " + printhrclass + "組 " + printattendo + "番";
                svf.VrsOutn("ENT_SCHOOL"+ (no % 2 == 1 ? "1" : "2"), line, syozoku);

                final int namelen = getMS932ByteLength(name);
                String nameFld = namelen <= 26 ? "_1" : namelen <= 30 ? "_2" : "_3";
                svf.VrsOutn("NAME"+ (no % 2 == 1 ? "1" : "2") + nameFld, line, name);

                svf.VrsOutn("SCHOOLNAME"+ (no % 2 == 1 ? "1" : "2"), line, _param._schoolName + "　スクールバス対策協議会");

                svf.VrsOutn("ROSEN"+ (no % 2 == 1 ? "1" : "2"), line, rosen);
                svf.VrsOutn("LIMIT"+ (no % 2 == 1 ? "1" : "2"), line, _param._limit);

                if(no % 2 == 0) {
                	line++;
                }
                no++;

                _hasData = true;
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private String seikei(final String str) {
    	Pattern p = Pattern.compile("^0+([0-9]+.*)");
        Matcher m = p.matcher(str);

        String printstr= null;
        if (m.matches()) {
        	printstr = m.group(1);
        }

    	return printstr;

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75174 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _disp; // 1:個人,2:クラス
        final String _inState;
        private final String _schoolName;
        private boolean _isSeireki;
        private Map _certifSchoolDat = Collections.EMPTY_MAP;
        final String _documentroot;
        String _limit;
        final String _rosenName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _disp = request.getParameter("DISP");

             try {
            	 _limit = new String(StringUtils.defaultString(request.getParameter("TERM")).getBytes("ISO8859-1"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

            _schoolName =loadCertifSchoolDat(db2, _year);

            final String[] category_selected = request.getParameterValues("category_selected");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < category_selected.length; i++) {
                String rtnSt = "";
                if ("2".equals(_disp)) {
                    rtnSt = "'" + category_selected[i] + "'";
                } else if ("1".equals(_disp)) {
                    rtnSt = "'" + StringUtils.split(category_selected[i], "-")[0] + "'";
                }
                stb.append(sep).append(rtnSt);
                sep = ",";
            }
            stb.append(")");
            _inState = stb.toString();
            setSeirekiFlg(db2);
            setSchoolInfo(db2);
            _documentroot = request.getParameter("DOCUMENTROOT");

            final String rosenCd = request.getParameter("ROSEN");
            final Map rosenMap = getRosenMap(db2);
            _rosenName = (String) rosenMap.get(rosenCd);
        }

        public String loadCertifSchoolDat(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String schoolname = "";

            String certifKindcd = "102";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolname = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return schoolname;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final String str = rs.getString("NAME1");
                    if ("2".equals(str)) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String printDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(db2, date);
            }
        }

        private String printDateFormat(final DB2UDB db2, final String date) {
            if (_isSeireki) {
                final String wdate = (null == date) ? date : date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                return KNJ_EditDate.setDateFormat2(wdate);
            } else {
                final String wdate = (null == date) ? date : KNJ_EditDate.h_format_JP(db2, date);
                return KNJ_EditDate.setDateFormat(db2, wdate, _year);
            }
        }

        private void setSchoolInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _certifSchoolDat = new HashMap();
            try {
                String sql = "SELECT CERTIF_KINDCD, SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND (CERTIF_KINDCD = '101' OR CERTIF_KINDCD = '102') ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map certifKindMap = getMappedMap(_certifSchoolDat, rs.getString("CERTIF_KINDCD"));
                    certifKindMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    certifKindMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    certifKindMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                    certifKindMap.put("REMARK1", rs.getString("REMARK1"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getRosenMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRosenSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cd = 0;
                while (rs.next()) {
                    cd++;
                    final String rosenCd = String.valueOf(cd);
                    final String rosenName = rs.getString("ROSEN_1");

                    retMap.put(rosenCd, rosenName);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getRosenSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     ENVIR.ROSEN_1 AS ROSEN_1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND ENVIR.ROSEN_1 IS NOT NULL ");
            stb.append("         AND ENVIR.FLG_1 = '2' ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = REGD.YEAR AND T3.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND T3.GRADE = REGD.GRADE AND T3.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     ENVIR.ROSEN_1 ");
            return stb.toString();
        }
    }

    /**生徒又は職員情報**/
    private String sql()
    {
        final StringBuffer stb = new StringBuffer();
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       T2.NAME, ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO, ");
            stb.append("       GDAT.GRADE_CD, ");
            stb.append("       CASE WHEN T3.FLG_1 = '2' THEN T3.ROSEN_1 END AS ROSEN_1 ");
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("         ON GDAT.YEAR = T1.YEAR ");
            stb.append("        AND GDAT.GRADE = T1.GRADE ");
            stb.append("WHERE  T1.YEAR = '" + _param._year + "' AND ");
            stb.append("       T1.SEMESTER = '" + _param._semester + "' AND ");
            if ("1".equals(_param._disp)) {
                stb.append("   T1.SCHREGNO IN " + _param._inState + " ");
            }
            if ("2".equals(_param._disp)) {
                stb.append("   T1.GRADE || T1.HR_CLASS IN " + _param._inState + " ");
            }
            stb.append("       AND T3.ROSEN_1 = '" + _param._rosenName + "' ");
            stb.append("ORDER BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO ");

        return stb.toString();
    }
}

// eof

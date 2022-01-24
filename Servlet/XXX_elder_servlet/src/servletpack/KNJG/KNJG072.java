/*
 * $Id: ebf78ea20bec5869107060643be21b8757b8dc5c $
 *
 * 作成日: 2016/11/30
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJG072 {

    private static final Log log = LogFactory.getLog(KNJG072.class);

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

        final String sql = sql();
        log.debug(" sql =" + sql);

    	if (_param._isMeikei) {
    		
    		final KNJG010_1T g010 = new KNJG010_1T(db2, svf, new KNJDefineSchool());
    		g010.pre_stat(null, new HashMap());
    		final String[] arrayParam = {
    				null, // 0: 学籍番号
    				null, // 1: 証明書種別
    				_param._ctrlYear, // 2: 年度
    				_param._ctrlSemester, // 3: 学期
    				null, // 4:
    				null, // 5:
    				null, // 6:
    				null, // 7:
    				_param._printDate, // 8: 証明日付
    				null, // 9: 証明書番号 (使用しない)
    				null, // 10:
    				_param._ctrlYear, // 11: 年度
    				null, // 12: DOCUMENTROOT
    				null, // 13:
    				null, // 14:
    				null, // 15:
    				null, // 16: プロパティuseAddrField
    				null, // 17: entGrdDateFormat (使用しない)
    				null, // 18: printStamp (使用しない)
    				null, // 19: プロパティcertifPrintRealName
    				null, // 20: useShuryoShoumeisho(使用しない)
    				null, // 21: プロパティchutouKyoikuGakkoUFlg
    				null, // 22: プロパティknjg010PrintGradeCdAsGrade
    		};
    		
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
            	arrayParam[0] = KnjDbUtils.getString(row, "SCHREGNO");
            	final String gdatSchoolKind = KnjDbUtils.getString(row, "GDAT_SCHOOL_KIND");
            	if ("H".equals(gdatSchoolKind)) {
            		arrayParam[1] = "056";
            	} else if ("J".equals(gdatSchoolKind)) {
            		arrayParam[1] = "057";
            	}
            	if (g010.printSvfMain(arrayParam, _param._ctrlYear)) {
            		_hasData = true;
            	}
            }
            
            g010.pre_stat_f();

    	} else {
            final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度 ";

        	svf.VrSetForm("KNJG072.frm", 1);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
        		svf.VrsOut("SCHOOL_NAME1", KnjDbUtils.getString(row, "SEND_TO1"));
        		svf.VrsOut("PRINCIPAL_NAME1", KnjDbUtils.getString(row, "SEND_TO2"));
        		final String stdName = KnjDbUtils.getString(row, "NAME");
        		final String stdBirthDay = "(" + KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(row, "BIRTHDAY")) + "生)";
        		svf.VrsOut("STUDENT_NAME", "照会者　"  + stdName + stdBirthDay);
        		svf.VrsOut("NENDO", nendo);
        		svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(row, "SEND_DATE")));
        		svf.VrsOut("PREF", _param._certifSchoolDatRemark1);
        		svf.VrsOut("SCHOOL_NAME2", _param._schoolName);
        		svf.VrsOut("PRINCIPAL_NAME2", _param._principalName);
        		if (null != _param._schoolStampPath) {
        			svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
        		}

                svf.VrEndPage();
                _hasData = true;
            }
    	}
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_PRINT AS ( ");
        stb.append(" SELECT ");
        stb.append("    PRINT_HIST.YEAR, ");
        stb.append("    PRINT_HIST.SCHOOLCD, ");
        stb.append("    PRINT_HIST.SCHOOL_KIND, ");
        stb.append("    PRINT_HIST.SCHREGNO, ");
        stb.append("    GDAT.SCHOOL_KIND AS GDAT_SCHOOL_KIND, ");
        stb.append("    MAX(PRINT_HIST.SEQ) AS SEQ ");
        stb.append(" FROM ");
        stb.append("     HEALTH_SPORT_PRINT_HIST_DAT PRINT_HIST ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON PRINT_HIST.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON PRINT_HIST.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         AND PRINT_HIST.YEAR = REGD.YEAR ");
        stb.append("         AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("         AND REGD.GRADE = GDAT.GRADE ");
        stb.append(" WHERE ");
        stb.append("     PRINT_HIST.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND PRINT_HIST.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND PRINT_HIST.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("     AND PRINT_HIST.SCHREGNO = '" + _param._schregNo + "' ");
        stb.append(" GROUP BY ");
        stb.append("    PRINT_HIST.YEAR, ");
        stb.append("    PRINT_HIST.SCHOOLCD, ");
        stb.append("    PRINT_HIST.SCHOOL_KIND, ");
        stb.append("    PRINT_HIST.SCHREGNO, ");
        stb.append("    GDAT.SCHOOL_KIND ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    PRINT_HIST.SEND_DATE, ");
        stb.append("    BASE.NAME, ");
        stb.append("    BASE.BIRTHDAY, ");
        stb.append("    PRINT_HIST.SEND_TO1, ");
        stb.append("    PRINT_HIST.SEND_TO2, ");
        stb.append("    PRINT_HIST.SCHREGNO, ");
        stb.append("    MAX_PRINT.GDAT_SCHOOL_KIND ");
        stb.append(" FROM ");
        stb.append("     MAX_PRINT ");
        stb.append("     INNER JOIN HEALTH_SPORT_PRINT_HIST_DAT PRINT_HIST ON MAX_PRINT.YEAR = PRINT_HIST.YEAR ");
        stb.append("           AND MAX_PRINT.SCHOOLCD = PRINT_HIST.SCHOOLCD ");
        stb.append("           AND MAX_PRINT.SCHOOL_KIND = PRINT_HIST.SCHOOL_KIND ");
        stb.append("           AND MAX_PRINT.SCHREGNO = PRINT_HIST.SCHREGNO ");
        stb.append("           AND MAX_PRINT.SEQ = PRINT_HIST.SEQ ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON MAX_PRINT.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("    PRINT_HIST.SEND_DATE, ");
        stb.append("    PRINT_HIST.SCHREGNO, ");
        stb.append("    PRINT_HIST.SEQ ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70133 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printDate;
        private final String _documentroot;
        private final String _schoolCd;
        private final String _schoolkind;
        private final String _schregNo;
        private final String _z010;
        private final boolean _isMeikei;

        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";
        private String _schoolNamePath;
        private String _schoolStampPath;
        private String _schoolLogoPath;
        private String _imagePath;
        private String _documentRoot;
        private String _certifSchoolDatRemark1 = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schregNo = request.getParameter("SCHREGNO");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printDate = request.getParameter("PRINT_DATE");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isMeikei = "meikei".equals(_z010);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + _schoolkind + ".bmp");
            _schoolLogoPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLLOGO_" + _schoolkind + ".jpg");
            _schoolNamePath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLNAME_" + _schoolkind + ".jpg");
            setCertifSchoolDat(db2);
        }

        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
        	if ("J".equals(_schoolkind)) {
        		certifKindCd = "135";
        	} else {
        		certifKindCd = "136";
        	}

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' "));
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _certifSchoolDatRemark1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK1"));
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}

// eof


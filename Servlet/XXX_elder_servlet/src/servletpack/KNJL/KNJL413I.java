/*
 * $Id: e9a80052b3df6279648a3511efe5ca806b674681 $
 *
 * 作成日: 2020/09/18
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJL413I {

    private static final Log log = LogFactory.getLog(KNJL413I.class);

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
        svf.VrSetForm("KNJL413I.frm", 1);
        final List<PrintData> bodyList = getBodyList(db2);
        final PrintSchoolData schoolData = getSchoolData(db2);
        final String wYear = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear));

        for (PrintData bodyData : bodyList) {
            svf.VrsOut("NAME", StringUtils.defaultString(bodyData._name) + "  （整理番号" + StringUtils.defaultString(bodyData._examNo) + "）");
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(db2, bodyData._birthday));
            svf.VrsOut("NOTE", "　上記のものは、" + wYear + "年度　" + StringUtils.defaultString(schoolData._schoolName).trim() + "　第１学年度に");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._limitDate));
            svf.VrsOut("SCHOOL_ADDR", schoolData._schoolAddr);
            svf.VrsOut("SCHOOL_NAME", schoolData._schoolName);
            String principalName = StringUtils.defaultString(schoolData._jobName) + StringUtils.defaultString(schoolData._principalName);
            svf.VrsOut("PRINCIPAL_NAME", principalName);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private PrintSchoolData getSchoolData(final DB2UDB db2) {
        PrintSchoolData psd = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String schoolDataSql = getSchoolDataSql();
            log.debug(" sql =" + schoolDataSql);

        	ps = db2.prepareStatement(schoolDataSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolAddr = rs.getString("ADDR");
                final String schoolName = rs.getString("SCHOOL_NAME");
                final String jobName = rs.getString("JOB_NAME");
                final String principalName = rs.getString("PRINCIPAL_NAME");

                psd = new PrintSchoolData(schoolAddr, schoolName, jobName, principalName);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return psd;
    }

    private String getSchoolDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REMARK3 || REMARK4 AS ADDR, ");
        stb.append("     SCHOOL_NAME, ");
        stb.append("     JOB_NAME, ");
        stb.append("     PRINCIPAL_NAME ");
        stb.append(" FROM ");
        stb.append("     CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._entexamyear + "' ");
        // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
        if ("1".equals(_param._applicantDiv)) {
        	stb.append("     AND CERTIF_KINDCD = '105' ");
        } else if ("2".equals(_param._applicantDiv)) {
        	stb.append("     AND CERTIF_KINDCD = '106' ");
        }
        return stb.toString();
    }

    private List<PrintData> getBodyList(final DB2UDB db2) {
        List<PrintData> bodyList = new ArrayList<PrintData>();
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	try {
    		final String bodySql = getBodySql();
    		log.debug(" sql =" + bodySql);
    		ps = db2.prepareStatement(bodySql);
    		rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String examNo = rs.getString("EXAMNO");
                final String birthday = rs.getString("BIRTHDAY");

                final PrintData bodyData = new PrintData(name, examNo, birthday);
                bodyList.add(bodyData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

    	return bodyList;
    }

    private String getBodySql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.BIRTHDAY ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND DETAIL.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND DETAIL.SEQ = '022' ");
        stb.append("          AND DETAIL.REMARK1 = '1' ");
        stb.append("          AND DETAIL.REMARK3 = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.JUDGEMENT = '1' ");
        stb.append("     AND BASE.PROCEDUREDIV = '1' ");
        stb.append("     AND BASE.ENTDIV <> '2' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76981 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintSchoolData {
        final String _schoolAddr;
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        public PrintSchoolData(
                final String schoolAddr,
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolAddr = schoolAddr;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }
    }

    private class PrintData {
        final String _name;
        final String _examNo;
        final String _birthday;

        public PrintData(
                final String name,
                final String examNo,
                final String birthday
        ) {
            _name = name;
            _examNo = examNo;
            _birthday = birthday;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _limitDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _limitDate = request.getParameter("LIMIT_DATE");
        }
    }
}

// eof


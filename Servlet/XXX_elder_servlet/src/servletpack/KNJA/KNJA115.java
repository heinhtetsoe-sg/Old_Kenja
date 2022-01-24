/*
 * $Id: 7ff49761478dd4121cda1c58158da294049db18b $
 *
 * 作成日: 2018/01/10
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJA115 {

    private static final Log log = LogFactory.getLog(KNJA115.class);

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            final List schJohoList = getList(db2);

            if (_param._isCsv) {
                outputCsv(response, schJohoList);
            } else {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if (!schJohoList.isEmpty()) {
                    printMain(svf,  schJohoList);
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (null != _param && _param._isCsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void outputCsv(final HttpServletResponse response, final List schJohoList) {
        final List lines = getCsvOutputLines(schJohoList);

        CsvUtils.outputLines(log, response, getTitle() + ".csv" , lines);
    }

    private List getCsvOutputLines(final List schJohoList) {
        final List lines = new ArrayList();

        String befSchregNo = "";
        newLine(lines).addAll(Arrays.asList(new String[] {getTitle(), "", getPrintDate()}));//タイトル、印刷日
        newLine(lines).addAll(Arrays.asList(new String[] {""}));//空行

        final List header1 = newLine(lines);
        header1.addAll(Arrays.asList(new String[] {"学籍番号", "氏名", "システム名", "ID", "パスワード"}));

        for (Iterator iterator = schJohoList.iterator(); iterator.hasNext();) {
        	final SchData printData = (SchData) iterator.next();

            if (!"".equals(befSchregNo) && !befSchregNo.equals(printData._schregNo)) {
                newLine(lines).addAll(Arrays.asList(new String[] {""})); //学籍番号が変わったら改行
            }
            final List line = newLine(lines);

            if (!befSchregNo.equals(printData._schregNo)) {
            	line.add(printData._schregNo);        // 学籍番号
            	line.add(printData._name);            // 氏名
            } else {
            	line.add("");
            	line.add("");
            }
            line.add(printData._systemName);      // システム名
            line.add(printData._loginId);         // ID
            line.add(printData._passWord);        // パスワード

            befSchregNo = printData._schregNo;
        }

        _hasData = true;

        return lines;
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private String getTitle() {
        final String title = "パスワード連絡票";
        return title;
    }

    private String getPrintDate() {
        final String printDate = KNJ_EditDate.h_format_JP(_param._ctrlDate);
        return printDate;
    }

    private void printMain(final Vrw32alp svf, final List schJohoList) {
        svf.VrSetForm("KNJA115.frm", 4);
        String befSchregNo = "";

        for (Iterator iterator = schJohoList.iterator(); iterator.hasNext();) {
        	final SchData printData = (SchData) iterator.next();

        	if (!"".equals(befSchregNo) && !befSchregNo.equals(printData._schregNo)) {
        		svf.VrSetForm("KNJA115.frm", 4);
        	}

            setTitle(svf);
            svf.VrsOut("SCHREG_NO", printData._schregNo);     // 学籍番号
            svf.VrsOut("NAME", printData._name);              // 氏名
            svf.VrsOut("SYSTEM_NAME", printData._systemName); // システム名
            svf.VrsOut("ID", printData._loginId);             // ID
            svf.VrsOut("PASSWORD", printData._passWord);      // パスワード

            svf.VrEndRecord();

            befSchregNo = printData._schregNo;
            _hasData = true;
        }
    }

	private void setTitle(final Vrw32alp svf) {
		svf.VrsOut("TITLE", getTitle());
        svf.VrsOut("DATE", getPrintDate());
	}

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String systemName = rs.getString("SYSTEM_NAME");
                final String loginId = rs.getString("LOGINID");
                final String passWord = rs.getString("PASSWORD");

                final SchData schData = new SchData(schregNo, name, systemName, loginId, passWord);
                retList.add(schData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SYST.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     SYS_M.SYSTEM_NAME, ");
        stb.append("     SYST.LOGINID, ");
        stb.append("     SYST.PASSWORD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_OTHER_SYSTEM_USER_DAT SYST  ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON SYST.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN OTHER_SYSTEM_MST SYS_M ON SYST.SYSTEMID = SYS_M.SYSTEMID ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON SYST.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                   AND REGD.YEAR      = '"+ _param._year + "' ");
        stb.append("                                   AND REGD.SEMESTER  = '"+ _param._semester + "' ");
        stb.append(" WHERE ");
        if ("1".equals(_param._taisyou)) {
            stb.append("     REGD.GRADE || REGD.HR_CLASS IN "+ _param._categorySelectedIn +" ");
        } else {
            stb.append("     REGD.SCHREGNO IN "+ _param._categorySelectedIn +" ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SYST.SCHREGNO, ");
        stb.append("     SYS_M.SHOWORDER ");

        return stb.toString();
    }

    private class SchData {
        final String _schregNo;
        final String _name;
        final String _systemName;
        final String _loginId;
        final String _passWord;
        public SchData(
                final String schregNo,
                final String name,
                final String systemName,
                final String loginId,
                final String passWord
        ) {
            _schregNo = schregNo;
            _name     = name;
            _systemName = systemName;
            _loginId  = loginId;
            _passWord = passWord;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57934 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _cmd;
        private final boolean _isCsv;
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _taisyou;
        private final String _categorySelectedIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd          = request.getParameter("cmd");
            _isCsv        = "csv".equals(_cmd);
            _year         = request.getParameter("CTRL_YEAR");
            _semester     = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("LOGIN_DATE");
            _taisyou      = request.getParameter("TAISYOU");
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(categorySelected);
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + categorySelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }
    }
}

// eof

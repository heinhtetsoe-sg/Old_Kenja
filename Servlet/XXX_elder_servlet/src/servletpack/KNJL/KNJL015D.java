/*
 * $Id: f2272ec7470e6addcdf0c737d98e1a18f2815e52 $
 *
 * 作成日: 2018/01/18
 * 作成者: kawata
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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

public class KNJL015D {

    private static final Log log = LogFactory.getLog(KNJL015D.class);

    private boolean _hasData;

    private Param _param;

	private String bithdayField;

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
        String fschoolname = "";
    	if ("print_all".equals(_param._printtype)) {
            svf.VrSetForm("KNJL015D.frm", 1);
    	}else {
    		fschoolname = getJHName(db2);
            svf.VrSetForm("KNJL015D_2.frm", 1);
    	}
        final List printList = getList(db2);
        final int maxLine = 50;
        int printLine = 1;

        setTitle(db2, svf, fschoolname);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(db2, svf, fschoolname);//ヘッダ
                printLine = 1;
            }

            //データ
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, printData._examNo);
            //氏名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME1" , printLine, printData._name);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME2" , printLine, printData._name);
            }else {
                svf.VrsOutn("NAME3" , printLine, printData._name);
            }
            //性別
            svf.VrsOutn("SEX" , printLine, printData._sex);
            //出身学校(全て印刷時のみ)
        	if ("print_all".equals(_param._printtype)) {
                svf.VrsOutn("FINSCHOOL_NAME" , printLine, printData._finschool_name);
        	}
            //選抜Ⅰ
            if ("print_all".equals(_param._printtype) || 30 >= KNJ_EditEdit.getMS932ByteLength(printData._remark1)) {
                svf.VrsOutn("SELECTION1_1" , printLine, printData._remark1);
            }else {
                svf.VrsOutn("SELECTION1_2" , printLine, printData._remark1);
            }
            //選抜Ⅱ
            if ("print_all".equals(_param._printtype) || 30 >= KNJ_EditEdit.getMS932ByteLength(printData._remark2)) {
                svf.VrsOutn("SELECTION2_1", printLine, printData._remark2);
            }else {
                svf.VrsOutn("SELECTION2_2", printLine, printData._remark2);
            }

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf ,final String finschool_name) {
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + "　併願校一覧" + "(" + _param._testdivName + ")");
    	if (!"print_all".equals(_param._printtype)) {
            svf.VrsOut("FINSCHOOL_NAME", finschool_name);
    	}
    }

    private String getJHName(final DB2UDB db2) {
        PreparedStatement ps_finschoolName = null;
        ResultSet rs_finschoolName = null;
        String finschool_name = "";

        try {
            final String finschoolNamesql = getFinShoolSql();
            log.debug(" finschoolNamesql =" + finschoolNamesql);
            ps_finschoolName = db2.prepareStatement(finschoolNamesql);
            rs_finschoolName = ps_finschoolName.executeQuery();

            while (rs_finschoolName.next()) {
            	finschool_name = rs_finschoolName.getString("FINSCHOOL_NAME_ABBV");
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps_finschoolName, rs_finschoolName);
            db2.commit();
        }
        return finschool_name;
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
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");

                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");

            	if ("print_all".equals(_param._printtype)) {
                    final String fs_cd = rs.getString("FS_CD");
                    final String finschool_name = rs.getString("FINSCHOOL_NAME_ABBV");
                    final PrintData printData = new PrintData(examNo, name, sex, fs_cd, finschool_name, remark1, remark2);
                    retList.add(printData);
            	} else {
                    final PrintData printData = new PrintData(examNo, name, sex, "", "", remark1, remark2);
                    retList.add(printData);
            	}

            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getFinShoolSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FSM.FINSCHOOL_NAME_ABBV ");
        stb.append(" FROM ");
        stb.append("     FINSCHOOL_MST FSM ");
        stb.append(" WHERE ");
        stb.append("         FSM.FINSCHOOLCD = '" + _param._finschoolcd + "' ");
        return stb.toString();
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     NM_SEX.NAME1 AS SEX, ");
    	if ("print_all".equals(_param._printtype)) {
            stb.append("     T1.FS_CD, ");
            stb.append("     FSM.FINSCHOOL_NAME_ABBV, ");
    	}
        stb.append("     TD1.REMARK1, ");
        stb.append("     TD1.REMARK2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TD1 ON T1.EXAMNO = TD1.EXAMNO AND ");
        stb.append("       T1.ENTEXAMYEAR = TD1.ENTEXAMYEAR AND ");
        stb.append("       T1.APPLICANTDIV = TD1.APPLICANTDIV AND ");
        stb.append("       TD1.SEQ         = '005' ");
        stb.append("    LEFT JOIN NAME_MST NM_SEX ON NM_SEX.NAMECD1 = 'Z002' AND NM_SEX.NAMECD2 = T1.SEX");
        stb.append("    LEFT JOIN FINSCHOOL_MST FSM ON T1.FS_CD = FSM.FINSCHOOLCD");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
    	if (!"print_all".equals(_param._printtype)) {
            stb.append("     AND T1.FS_CD        = '" + _param._finschoolcd + "' ");
    	}
        stb.append(" ORDER BY SUBSTR(CHAR(DECIMAL(T1.EXAMNO, 10, 0)),1,10)");

        return stb.toString();
    }

    private class PrintData {
        final String _examNo;
        final String _name;
        final String _sex;
        final String _fs_cd;
        final String _finschool_name;
        final String _remark1;
        final String _remark2;

        public PrintData(
                final String examNo,
                final String name,
                final String sex,
                final String fs_cd,
                final String finschool_name,
                final String remark1,
                final String remark2
        ) {
            _examNo = examNo;
            _name = name;
            _sex = sex;
            _fs_cd = fs_cd;
            _finschool_name = finschool_name;
            _remark1 = remark1;
            _remark2 = remark2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final String _finschoolcd;
        private final String _printtype;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _finschoolcd  = request.getParameter("FINSCHOOLCD");
            _printtype  = request.getParameter("PRINTTYPE");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

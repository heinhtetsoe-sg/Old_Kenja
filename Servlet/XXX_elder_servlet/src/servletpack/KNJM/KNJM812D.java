// kanji=漢字
/*
 * $Id: 2581f05fa1a0c6793c56022fe4735c54788f9ba6 $
 *
 * 作成日: 2013/02/20 18:41:30 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2581f05fa1a0c6793c56022fe4735c54788f9ba6 $
 */
public class KNJM812D {

    private static final Log log = LogFactory.getLog("KNJM812D.class");

    private boolean _hasData;

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

        svf.VrSetForm("KNJM812D.frm", 1);
        final List studentList = getStudentList(db2);
        final int rowMax = 8;
        final int colMax = 3;
        int col = 1;
        int row = 1;
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            _hasData = true;
            if(col > colMax) {
                col = 1;
                row++;
                if (row > rowMax) {
                    if (_hasData) {
                        svf.VrEndPage();
                    }
                    row = 1;
                }
            }
            //郵便番号
            if (!StringUtils.isBlank(student._zip)) {
                svf.VrsOutn("ZIPCODE"  + col, row, "〒" + student._zip);
            }
            //住所
            if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(student._addr1) > 50 || getMS932ByteLength(student._addr2) > 50)) {
                svf.VrsOutn("ADDRESS"  + col + "_1_3", row, student._addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_3", row, student._addr2);
            } else if (getMS932ByteLength(student._addr1) > 40 || getMS932ByteLength(student._addr2) > 40){
                svf.VrsOutn("ADDRESS"  + col + "_1_2", row, student._addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_2", row, student._addr2);
            } else if (getMS932ByteLength(student._addr1) > 0 || getMS932ByteLength(student._addr2) > 0) {
                svf.VrsOutn("ADDRESS"  + col + "_1_1", row, student._addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_1", row, student._addr2);
            }
            final String name = StringUtils.defaultString(student._name);
            if (!StringUtils.isBlank(name)) {
                if (getMS932ByteLength(name) > 20) {
                    svf.VrsOutn("NAME" + col + "_1", row, name);
                } else {
                    svf.VrsOutn("NAME" + col + "_1", row, name + "　様");
                }
            }
            svf.VrsOutn("NAME" + col + "_3", row, "保護者（保証人）　様");
            svf.VrsOutn("NAME" + col + "_2", row, student._schregno);
            col++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private List getStudentList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        // 履修科目をセット
        try {
            final String sql = getStdSelectSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("NAME_KANA"), rs.getString("SEND_ZIPCD"), rs.getString("SEND_ADDR1"), rs.getString("SEND_ADDR2"), rs.getString("SEND_NAME"));
                list.add(student);
            }
        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private String getStdSelectSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREGNOS AS (");
        stb.append("   SELECT  ");
        stb.append("     T1.SCHREGNO, T4.NAME, T4.NAME_KANA ");
        stb.append("   FROM SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("       T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("       AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._output)) {
            stb.append("             AND T1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
        }
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("     UNION ");
            stb.append("     SELECT T2.SCHREGNO, T2.NAME, T2.NAME_KANA ");
            stb.append("     FROM FRESHMAN_DAT T2 ");
            stb.append("   WHERE ");
            stb.append("       T2.ENTERYEAR = '" + _param._year + "' ");
            if ("1".equals(_param._output)) {
                stb.append("           AND T2.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
            }
        }
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC,");
        stb.append("     SUBSTR(T1.SCHREGNO, 5, 4),");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T3.SEND_ZIPCD, ");
        stb.append("     T3.SEND_ADDR1, ");
        stb.append("     T3.SEND_ADDR2, ");
        stb.append("     T3.SEND_NAME ");
        stb.append(" FROM SCHREGNOS T1 ");
        //if ("2".equals(_param._output)) {
            stb.append(" INNER JOIN SUBCLASS_STD_SELECT_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
            if (!_param._year.equals(_param._ctrlYear)) {
                stb.append("     AND T2.SEMESTER = '1' ");
            } else {
                stb.append("     AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            }
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        //}
        stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T3.DIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4) ");
        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _nameKana;
        final String _zip;
        final String _addr1;
        final String _addr2;
        final String _sendName;
        Student(final String schregno, final String name, final String nameKana, final String zip, final String addr1, final String addr2, final String sendName) {
            _schregno = schregno;
            _name = name;
            _nameKana = nameKana;
            _zip = zip;
            _addr1 = addr1;
            _addr2 = addr2;
            _sendName = sendName;
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
        final String _year; // ログイン年度 もしくは ログイン年度 + 1
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _output;
        final String _sSchregno;
        final String _eSchregno;
        final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _sSchregno = request.getParameter("SSCHREGNO");
            _eSchregno = request.getParameter("ESCHREGNO");
            _useAddrField2 = request.getParameter("useAddrField2");
        }

    }
}

// eof

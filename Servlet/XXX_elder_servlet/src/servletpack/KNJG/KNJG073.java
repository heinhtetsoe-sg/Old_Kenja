/*
 * $Id: d282ff4af7e05f504b479179c487d85d8f5fdb10 $
 *
 * 作成日: 2017/09/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJG073 {

    private static final Log log = LogFactory.getLog(KNJG073.class);

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
        svf.VrSetForm("KNJG073.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 50;
            final int maxCol = 2;
            int lineCnt = 1;
            int colCnt = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    colCnt++;
                }
                if (colCnt > maxCol) {
                    lineCnt = 1;
                    colCnt = 1;
                    svf.VrEndPage();
                }
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("TITLE", "スポーツ振興センター未加入者一覧");
                svf.VrsOut("NAME_HEADER1", "生徒氏名");
                svf.VrsOut("NAME_HEADER2", "生徒氏名");

                svf.VrsOutn("HR_NAME" + colCnt, lineCnt, rs.getString("HR_NAME") + "　" + rs.getString("ATTENDNO") + "番");
                final String nameField = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME")) > 40 ? "_3" : KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME")) > 30 ? "_2" : "_1";
                svf.VrsOutn("NAME" + colCnt + nameField, lineCnt, rs.getString("NAME"));

                lineCnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("           AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("         AND REGDH.GRADE = REGD.GRADE ");
        stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_008 ON BASE.SCHREGNO = BASE_008.SCHREGNO ");
        stb.append("          AND BASE_008.BASE_SEQ = '008' ");
        stb.append(" WHERE ");
        stb.append("     REGD.GRADE IN (" + _param._sqlInSentence + ") ");
        stb.append("     AND BASE_008.BASE_REMARK1 IS NULL ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
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
        private final String[] _grade;
        private final String _sqlInSentence;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useschoolKindfield;
        private final String _schoolkind;
        private final String _usePrgSchoolkind;
        private final String _selectschoolkind;
        private final String _printLogStaffcd;
        private final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameterValues("GRADE");
            String setSentence = "";
            String sep = "";
            for (int i = 0; i < _grade.length; i++) {
                setSentence += sep + "'" + _grade[i] + "'";
                sep = ",";
            }
            _sqlInSentence = setSentence;
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
        }

    }
}

// eof


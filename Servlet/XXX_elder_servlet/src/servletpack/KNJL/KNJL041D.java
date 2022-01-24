/*
 * $Id: 40a3d4edb1f66c0ca3ce305684a6f97c343f1d09 $
 *
 * 作成日: 2018/01/19
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL041D {

    private static final Log log = LogFactory.getLog(KNJL041D.class);

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

        newLine(lines).addAll(Arrays.asList(new String[] {getTitle(), "",  "",  "", "作成日：" + _param._printDateStr}));//タイトル、印刷日
        newLine(lines).addAll(Arrays.asList(new String[] {""}));//空行

        final List header1 = newLine(lines);
        header1.addAll(Arrays.asList(new String[] {"受験番号", "氏名", "性別", "出身中学", "会場"}));

        for (Iterator iterator = schJohoList.iterator(); iterator.hasNext();) {
            final SchData printData = (SchData) iterator.next();
            final List line = newLine(lines);

            line.add(printData._examNo);        // 受験番号
            line.add(printData._name);          // 氏名
            line.add(printData._sex);           // 性別
            line.add(printData._finSchoolName); // 出身中学
            line.add(printData._hall);          // 会場
        }

        _hasData = true;

        return lines;
    }

    private void printMain(final Vrw32alp svf, final List schJohoList) {
        svf.VrSetForm("KNJL041D.frm", 1);

        int printLine = 1;
        final int maxLine = 50;

        for (Iterator iterator = schJohoList.iterator(); iterator.hasNext();) {
            final SchData printData = (SchData) iterator.next();

            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(svf);
                printLine = 1;
            }
            setTitle(svf);
            svf.VrsOutn("EXAM_NO", printLine, printData._examNo);//受験番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "2" : "1";
            svf.VrsOutn("NAME"+ nameField, printLine, printData._name);//氏名
            svf.VrsOutn("SEX", printLine, printData._sex);//性別
            svf.VrsOutn("FINSCHOOL_NAME", printLine, printData._finSchoolName);//出身中学
            svf.VrsOutn("HALL", printLine, printData._hall);//会場

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private String getTitle() {
        final String title = "欠席リスト";
        return title;
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", getTitle());
        svf.VrsOut("DATE", _param._printDateStr);
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
                final String examNo        = rs.getString("EXAMNO");
                final String name          = rs.getString("NAME");
                final String sex           = rs.getString("SEX");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME");
                final String hall          = rs.getString("HALL");

                final SchData schData = new SchData(examNo, name, sex, finSchoolName, hall);
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
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.ABBV1 AS SEX, ");
        stb.append("     VALUE(FINS.FINSCHOOL_NAME_ABBV, '') AS FINSCHOOL_NAME, ");
        stb.append("     VALUE((SELECT ");
        stb.append("                EXAMHALL_NAME ");
        stb.append("            FROM  ");
        stb.append("                ENTEXAM_HALL_YDAT ");
        stb.append("            WHERE  ");
        stb.append("                    ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                AND APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                AND EXAM_TYPE    = '1' ");
        stb.append("                AND BASE.EXAMNO BETWEEN S_RECEPTNO AND E_RECEPTNO ");
        stb.append("            ), '') AS HALL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("                              AND Z002.NAMECD1 = 'Z002' ");
        stb.append("                              AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '"+ _param._entExamYear +"' ");
        stb.append("     AND BASE.APPLICANTDIV = '"+ _param._applicantDiv +"' ");
        stb.append("     AND BASE.JUDGEMENT    = '4' ");//欠席
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private class SchData {
        final String _examNo;
        final String _name;
        final String _sex;
        final String _finSchoolName;
        final String _hall;
        public SchData(
                final String examNo,
                final String name,
                final String sex,
                final String finSchoolName,
                final String hall
        ) {
            _examNo        = examNo;
            _name          = name;
            _sex           = sex;
            _finSchoolName = finSchoolName;
            _hall          = hall;
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
        private final String _cmd;
        private final boolean _isCsv;
        private final String _loginYear;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _printDateStr; 

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd            = request.getParameter("cmd");
            _isCsv          = "csv".equals(_cmd);
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _printDateStr = KNJ_EditDate.h_format_JP(db2, _loginDate);
        }

    }
}

// eof

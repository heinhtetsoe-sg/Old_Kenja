// kanji=漢字
/*
 * $Id: e19dbad254bcea3c8f0b6502ca3460f756676e1e $
 *
 * 作成日: 2009/12/22 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: e19dbad254bcea3c8f0b6502ca3460f756676e1e $
 */
public class KNJL355M {

    private static final Log log = LogFactory.getLog("KNJL355M.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL355M.frm";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

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
        svf.VrSetForm(FORMNAME, 4);
        svf.VrsOut("TITLE", "新入生保護者入学前説明会");
        final List printData = getPrintData(db2);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final String date = KNJ_EditDate.h_format_JP(_param._ctrlDate);
            svf.VrsOut("DATE", date);
            final Student student = (Student) itPrint.next();
            final int nameField = student._name.length() > 8 ? 2 : 1;
            svf.VrsOut("NAME" + nameField, student._name);
            final int kanaField = student._nameKana.length() > 12 ? 2 : 1;
            svf.VrsOut("KANA" + kanaField, student._nameKana);
            svf.VrsOut("EXAMNO", student._examNo);
            _hasData = true;
            svf.VrEndRecord();
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examNo = rsStudent.getString("EXAMNO");
                final String name = rsStudent.getString("NAME");
                final String nameKana = rsStudent.getString("NAME_KANA");
                final String birthDay = rsStudent.getString("BIRTHDAY");
                final String addr1 = rsStudent.getString("ADDRESS1");
                final String addr2 = rsStudent.getString("ADDRESS2");
                final Student student = new Student(examNo, name, nameKana, birthDay, addr1, addr2);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     L1.ADDRESS1, ");
        stb.append("     L1.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        stb.append("     AND T1.JUDGEMENT IN (SELECT I1.NAMECD2 FROM NAME_MST I1 WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        stb.append("     AND T1.PROCEDUREDIV = '1' ");
        stb.append("     AND T1.ENTDIV = '1' ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _addr1;
        final String _addr2;

        public Student(
                final String examNo,
                final String name,
                final String nameKana,
                final String birthDay,
                final String addr1,
                final String addr2
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _addr1 = addr1;
            _addr2 = addr2;
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
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
    }
}

// eof

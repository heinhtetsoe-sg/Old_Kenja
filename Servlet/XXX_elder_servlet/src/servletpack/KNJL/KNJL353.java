// kanji=漢字
/*
 * $Id: e772fe50247c79607c4a38f5048842a3d6c3f9ca $
 *
 * 作成日: 2014/01/15 11:06:07 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: e772fe50247c79607c4a38f5048842a3d6c3f9ca $
 */
public class KNJL353 {

    private static final Log log = LogFactory.getLog("KNJL353.class");

    private boolean _hasData;
    private static final String EXAM_TYPE4 = "2";
    private static final int MAX_LINE = 25;

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

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printList = getPrintList(db2);
        int linCnt = 0;
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (linCnt == 0) {
                linCnt = 1;
                svf.VrSetForm("KNJL353.frm", 1);
                setHead(svf);
            } else if (linCnt > MAX_LINE) {
                linCnt = 1;
                svf.VrEndPage();
                setHead(svf);
            }

            svf.VrsOutn("EXAMNO", linCnt, student._examno);

            if (student._name != null) {
                final String fieldNo = (30 < getMS932ByteLength(student._name)) ? "3" : (20 < getMS932ByteLength(student._name)) ? "2" : "";
                svf.VrsOutn("NAME" + fieldNo, linCnt, student._name);
            }
            svf.VrsOutn("CRAM_NAME1", linCnt, student._prischoolName);
            svf.VrsOutn("CLASSROOM_NAME1", linCnt, student._kyousituName);
            svf.VrsOutn("CRAM_NAME2", linCnt, student._prischoolName2);
            svf.VrsOutn("CLASSROOM_NAME2", linCnt, student._kyousituName2);

            linCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　大宮開成中学　入学試験　塾チェックリスト");
        svf.VrsOut("PRINT_DAYTIME", _param._dateStr); // 作成日
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examno = rsStudent.getString("EXAMNO");
                final String name = rsStudent.getString("NAME");
                final String prischoolCd = rsStudent.getString("PRISCHOOLCD");
                final String prischoolName = rsStudent.getString("PRISCHOOL_NAME");
                final String kyousituName = rsStudent.getString("REMARK2");
                final String prischoolCd2 = rsStudent.getString("PRISCHOOLCD2");
                final String prischoolName2 = rsStudent.getString("PRISCHOOL_NAME2");
                final String kyousituName2 = rsStudent.getString("REMARK4");
                final Student student = new Student(db2, examno, name, prischoolCd, prischoolName, kyousituName, prischoolCd2, prischoolName2, kyousituName2);
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
        stb.append("     BASE_D.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     PRI.PRISCHOOLCD, ");
        stb.append("     PRI.PRISCHOOL_NAME, ");
        stb.append("     BASE_D.REMARK2, ");
        stb.append("     PRI2.PRISCHOOLCD AS PRISCHOOLCD2, ");
        stb.append("     PRI2.PRISCHOOL_NAME AS PRISCHOOL_NAME2, ");
        stb.append("     BASE_D.REMARK4 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN PRISCHOOL_MST PRI  ON BASE_D.REMARK1 = PRI.PRISCHOOLCD ");
        stb.append("     LEFT JOIN PRISCHOOL_MST PRI2 ON BASE_D.REMARK3 = PRI2.PRISCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     BASE_D.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND BASE_D.SEQ = '008' ");
        stb.append("     AND (PRI.PRISCHOOLCD IS NOT NULL OR PRI2.PRISCHOOLCD IS NOT NULL) ");
        stb.append(" ORDER BY ");
        stb.append("     BASE_D.EXAMNO ");
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
        private final String _year;
        private final String _ctrlDate;
        private final String _applicantDiv;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_ctrlDate);
        }

        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

    }

    /** 生徒 */
    private class Student {
        private final String _examno;
        private final String _name;
        private final String _prischoolCd;
        private final String _prischoolName;
        private final String _kyousituName;
        private final String _prischoolCd2;
        private final String _prischoolName2;
        private final String _kyousituName2;

        Student(final DB2UDB db2,
                final String examno,
                final String name,
                final String prischoolCd,
                final String prischoolName,
                final String kyousituName,
                final String prischoolCd2,
                final String prischoolName2,
                final String kyousituName2
        ) throws SQLException {
            _examno = examno;
            _name = name;
            _prischoolCd = prischoolCd;
            _prischoolName = prischoolName;
            _kyousituName = kyousituName;
            _prischoolCd2 = prischoolCd2;
            _prischoolName2 = prischoolName2;
            _kyousituName2 = kyousituName2;
        }

    }
}

// eof

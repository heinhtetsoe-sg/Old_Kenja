/*
 * $Id: bfbf0bfc5548d75682a43e03444e61f9a5f9e2f4 $
 *
 * 作成日: 2015/01/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJI;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJI080A {

    private static final Log log = LogFactory.getLog(KNJI080A.class);

    private boolean _hasData;
    private final int MAXLINE = 50;

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

    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

    /** 作成日のメソッド **/
    private void Set_Head(final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param.changePrintYear(_param._graduate_year) + "度　卒業生一覧");
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJI080A.frm", 1);
        Set_Head(svf);
        svf.VrsOut("DATE", "作成日：" + _param._ctrlDate.replace('-', '/'));      //作成日

        final List studentList = getList(db2);
        svf.VrsOut("NUM", "卒業生：" + String.valueOf(studentList.size()) + "人");

        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            if (MAXLINE < line) {
                line = 1;
                svf.VrEndPage();
                Set_Head(svf);
            }
            Student student = (Student) iterator.next();
            svf.VrsOutn("SCHREGNO", line, student._schregNo);
            String setField = getMS932ByteLength(student._name) > 40 ? "2_2" : getMS932ByteLength(student._name) > 30 ? "2_1" : "";
            svf.VrsOutn("NAME" + setField, line, student._name);
            svf.VrsOutn("SEX", line, student._sex);
            svf.VrsOutn("GRD_DATE", line, student._grdDate);
            svf.VrsOutn("ENT_DATE", line, student._entDate);
            line++;
        }
        if (1 < line) {
            svf.VrEndPage();
        }

        db2.commit();
    }

    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
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
                final String sex = rs.getString("SEX");
                final String grdDate = rs.getString("GRD_DATE");
                final String entDate = rs.getString("ENT_DATE");
                final Student student = new Student(schregNo, name, sex, grdDate, entDate);
                list.add(student);
                _hasData = true;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     N1.NAME2 AS SEX, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.ENT_DATE ");
        stb.append(" FROM ");
        stb.append("     GRD_BASE_MST T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     Fiscalyear(T1.GRD_DATE) = '" + _param._graduate_year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _sex;
        private final String _grdDate;
        private final String _entDate;
        public Student(
                final String schregNo,
                final String name,
                final String sex,
                final String grdDate,
                final String entDate
                ) {
            _schregNo = schregNo;
            _name = name;
            _sex = sex;
            _grdDate = grdDate != null ? grdDate.replace('-', '/') : "";
            _entDate = entDate != null ? entDate.replace('-', '/') : "";
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _graduate_year;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _graduate_year = request.getParameter("GRADUATE_YEAR");
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                return KNJ_EditDate.h_format_JP(date);
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年";
        }

    }
}

// eof


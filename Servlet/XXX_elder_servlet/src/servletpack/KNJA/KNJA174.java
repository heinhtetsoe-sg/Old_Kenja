/*
 * $Id: b89c6534866284df294d0b2efecdbf04940997a7 $
 *
 * 作成日: 2019/08/29
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJA174 {

    private static final Log log = LogFactory.getLog(KNJA174.class);

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

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String sql = hrSql(_param._categorySelected[i]);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String majorname = rs.getString("MAJORNAME");
                    final String cd = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");

                    final HrClass hrClass = new HrClass(coursecd, majorcd, majorname, cd, hrName, staffName);
                    hrClass.setStudent(db2);
                    retList.add(hrClass);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA174.frm", 1);
        final List hrList = getList(db2);
        int maxCnt = 25;
//      int page = 1;
        for (Iterator itHr = hrList.iterator(); itHr.hasNext();) {
            HrClass hrClass = (HrClass) itHr.next();
            int lineCnt = 1;
            svf.VrsOut("COURSE_NAME1", hrClass._majorName); //学科名
            svf.VrsOut("HR_NAME1", hrClass._name); //年組名
            int trNameLen = KNJ_EditEdit.getMS932ByteLength(hrClass._staffName);
            final String trNamefield = trNameLen > 18 ? "_2" : "_1";
            svf.VrsOut("TR_NAME1" + trNamefield, hrClass._staffName); //担任名
            if (hrClass._studentList.size() > maxCnt) {
                svf.VrsOut("COURSE_NAME2", hrClass._majorName); //学科名
                svf.VrsOut("HR_NAME2", hrClass._name); //年組名
                svf.VrsOut("TR_NAME2" + trNamefield, hrClass._staffName); //担任名
            }

            int colCnt = 1;
            svf.VrsOut("BELONGING_NAME", "クラス：" + hrClass._name + "　担任：" + (null != hrClass._staffName ? hrClass._staffName : ""));
            for (Iterator itStudent = hrClass._studentList.iterator(); itStudent.hasNext();) {
                Student student = (Student) itStudent.next();
                if (lineCnt > maxCnt) {
                	if (colCnt == 2) {
                        colCnt = 1;
                	} else {
                        colCnt = 2;
                	}
                    lineCnt = 1;
//                    page++;
                }
//                svf.VrsOut("PAGE", String.valueOf(page));
                svf.VrsOutn("NO" + colCnt, lineCnt, String.valueOf(Integer.parseInt(student._attendNo)));
                final String nameField = getMS932ByteLength(student._name) > 30 ? "_3" : getMS932ByteLength(student._name) > 20 ? "_2" : "_1";
                svf.VrsOutn("NAME"+colCnt + nameField, lineCnt, student._name);

                lineCnt++;
            }
            if (lineCnt > 1) {
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
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

    private String hrSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     M1.MAJORNAME, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     L2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON REGH.TR_CD1 = L2.STAFFCD ");
        stb.append("     LEFT JOIN MAJOR_MST M1 ON M1.COURSECD = REGD.COURSECD AND M1.MAJORCD = REGD.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._year + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGD.HR_CLASS = '" + hrClass + "' ");
        return stb.toString();
    }

    private class HrClass {
    	final String _coursecd;
    	final String _majorcd;
    	final String _majorName;
        final String _cd;
        final String _name;
        final String _staffName;
        final List _studentList;

        /**
         * コンストラクタ。
         */
        HrClass(
        		final String coursecd,
        		final String majorcd,
        		final String majorname,
                final String cd,
                final String name,
                final String staffName
        ) {
        	_coursecd = coursecd;
        	_majorcd = majorcd;
        	_majorName = majorname;
            _cd = cd;
            _name = name;
            _staffName = staffName;
            _studentList = new ArrayList();
        }
        /**
         * @param db2
         */
        public void setStudent(final DB2UDB db2) throws SQLException {
            final String studentSql = getStudentSql();
            log.info(" studentSql sql = " + studentSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo       = rs.getString("SCHREGNO");
                    final String name           = rs.getString("NAME");
                    final String nameKana       = rs.getString("NAME_KANA");
                    final String sexName        = rs.getString("SEX_NAME");
                    final String entDivName     = rs.getString("ENT_DIV_NAME");
                    final String grade          = rs.getString("GRADE");
                    final String hrClass        = rs.getString("HR_CLASS");
                    final String hrName         = rs.getString("HR_NAME");
                    final String hrNameabbv     = rs.getString("HR_NAMEABBV");
                    final String attendNo       = rs.getString("ATTENDNO");

                    final Student student = new Student(schregNo, name, nameKana, sexName, entDivName, grade, hrClass, hrName, hrNameabbv, attendNo, db2);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * @return
         */
        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     N1.NAME2 AS SEX_NAME, ");
            stb.append("     N2.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGH.HR_NAMEABBV, ");
            stb.append("     REGD.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = N1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' ");
            stb.append("          AND BASE.ENT_DIV = N2.NAMECD2 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
            stb.append("     AND REGD.HR_CLASS = '" + _cd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

    }

    private class Student {
        final String _schregNo;
        final String _name;
        final String _nameKana;
        final String _sexName;
        final String _entDivName;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _attendNo;

        /**
         * コンストラクタ。
         * @throws SQLException
         */
        Student(
                final String schregNo,
                final String name,
                final String nameKana,
                final String sexName,
                final String entDivName,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String attendNo,
                final DB2UDB db2
        ) throws SQLException {
            _schregNo       = schregNo;
            _name           = name;
            _nameKana       = nameKana;
            _sexName        = sexName;
            _entDivName     = entDivName;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _hrNameabbv     = hrNameabbv;
            _attendNo       = attendNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69464 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String[] _categorySelected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CLASS_SELECTED");
        }

    }
}

// eof


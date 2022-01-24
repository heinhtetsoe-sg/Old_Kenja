/*
 * $Id: f6396920395c5f9e1f09880d5b92278f1cbc28a1 $
 *
 * 作成日: 2019/03/25
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJH132A {

    private static final Log log = LogFactory.getLog(KNJH132A.class);

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

            printMain(db2, svf, response);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final HttpServletResponse response) {
        svf.VrSetForm("KNJH132A.frm", 4);
        final int maxLine = 40;
        final Map printAreaMap = getPrintAreaList(db2);
        int no = 1;
        int pageCnt = 1;
        int lineCnt = 1;

        setTitle(db2, svf, pageCnt);

        for (Iterator itArea = printAreaMap.keySet().iterator(); itArea.hasNext();) {
            final String areaCd = (String) itArea.next();
            final Area area = (Area) printAreaMap.get(areaCd);

            if (lineCnt >= maxLine) {
                svf.VrEndPage();
                pageCnt++;
                lineCnt = 1;
                setTitle(db2, svf, pageCnt);
            }

            svf.VrsOut("AREA_NAME", area._areaName + "　地区　　"); //地区名
            svf.VrsOut("NUM", area._studentList.size() + "名"); //人数
            lineCnt++;

            for (Iterator itStudent = area._studentList.iterator(); itStudent.hasNext();) {

                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    pageCnt++;
                    lineCnt = 1;
                    setTitle(db2, svf, pageCnt);
                }

                final Student student = (Student) itStudent.next();

                svf.VrsOut("NO", String.valueOf(no)); //番号
                svf.VrsOut("GRADE", student._gradeName); //学年
                final String hrNameField = getMS932ByteLength(student._hrName) > 4 ? "2" : "1";
                svf.VrsOut("HR_NAME" + hrNameField, student._hrName); //年組
                final String nameField = getMS932ByteLength(student._name) > 30 ? "4" : getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 14 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, student._name); //氏名
                final String finschoolNameField = getMS932ByteLength(student._finSchoolName) > 30 ? "4" : getMS932ByteLength(student._finSchoolName) > 20 ? "3" : getMS932ByteLength(student._finSchoolName) > 14 ? "2" : "1";
                svf.VrsOut("FINSCHOOL_NAME" + finschoolNameField, student._finSchoolName); //出身校名
                final String gNameField = getMS932ByteLength(student._guardName) > 30 ? "4" : getMS932ByteLength(student._guardName) > 20 ? "3" : getMS932ByteLength(student._guardName) > 14 ? "2" : "1";
                svf.VrsOut("GUARD_NAME" + gNameField, student._guardName); //保護者名
                svf.VrsOut("ZIP_NO", student._zipcd); //郵便番号
                final String addr1Field = getMS932ByteLength(student._addr1) > 80 ? "4" : getMS932ByteLength(student._addr1) > 60 ? "3" : getMS932ByteLength(student._addr1) > 40 ? "2" : "1";
                final String addr2Field = getMS932ByteLength(student._addr2) > 80 ? "4" : getMS932ByteLength(student._addr2) > 60 ? "3" : getMS932ByteLength(student._addr2) > 40 ? "2" : "1";
                svf.VrsOut("ADDR1_" + addr1Field, student._addr1); //住所1
                svf.VrsOut("ADDR2_" + addr2Field, student._addr2); //住所2
                svf.VrsOut("TEL_NO", student._telno); //電話番号

                no++;
                lineCnt++;
                _hasData = true;
                svf.VrEndRecord();
            }
        }
        svf.VrEndPage();
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, int pageCnt) {
        svf.VrsOut("PAGE", "-" + pageCnt + "-"); //ページ
        String[] nendo = KNJ_EditDate.tate_format4(db2, _param._ctrlYear + "-04-01");
        svf.VrsOut("TITLE", nendo[0] + nendo[1] + "年度　地区別名簿");
    }

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private Map getPrintAreaList(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = studentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSchoolKind = "";
            while (rs.next()) {
                final String areaCd = StringUtils.defaultString(rs.getString("AREACD"));
                final String areaName = StringUtils.defaultString(rs.getString("AREA_NAME"));
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeName = rs.getString("GRADE_NAME3");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_CLASS_NAME2");
                final String name = rs.getString("NAME");
                final String finSchoolName = rs.getString("REMARK2");
                final String guardName = rs.getString("GUARD_NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String telno = rs.getString("TELNO");

                Area area = null;
                if (retMap.containsKey(areaCd)) {
                    area = (Area) retMap.get(areaCd);
                    if (!befSchoolKind.equals(schoolKind)) {
                        area._haveSchoolKinds = true;
                    }
                } else {
                    area = new Area(areaCd, areaName);
                    befSchoolKind = "";
                }
                area._schoolKind = schoolKind;
                final Student student = new Student(areaCd,areaName,grade,schoolKind,gradeName,hrClass,hrName,name,finSchoolName,guardName,zipcd,addr1,addr2,telno);
                area._studentList.add(student);
                retMap.put(areaCd, area);
                befSchoolKind = schoolKind;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String studentSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     CASE WHEN VALUE(ADDR.AREACD, '999') = '999' ");
        stb.append("          THEN '999' ");
        stb.append("          ELSE ADDR.AREACD ");
        stb.append("     END AS AREACD, ");
        stb.append("     CASE WHEN VALUE(ADDR.AREACD, '999') = '999' ");
        stb.append("          THEN '未登録' ");
        stb.append("          ELSE A020.NAME1 ");
        stb.append("     END AS AREA_NAME, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GDAT.GRADE_NAME3, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_CLASS_NAME2, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.REMARK2, ");
        stb.append("     L2.FINSCHOOL_NAME_ABBV, ");
        stb.append("     L1.GUARD_NAME, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDR1, ");
        stb.append("     ADDR.ADDR2, ");
        stb.append("     ADDR.TELNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
        stb.append("        ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("       ON REGD.YEAR     = REGDH.YEAR ");
        stb.append("      AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("      AND REGD.GRADE    = REGDH.GRADE ");
        stb.append("      AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("       ON REGD.YEAR  = GDAT.YEAR ");
        stb.append("      AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN (");
        stb.append("       SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.AREACD, ");
        stb.append("         W1.ZIPCD, ");
        stb.append("         W1.ADDR1, ");
        stb.append("         W1.ADDR2, ");
        stb.append("         W1.TELNO ");
        stb.append("       FROM ");
        stb.append("         SCHREG_ADDRESS_DAT W1");
        stb.append("       WHERE");
        stb.append("         (W1.SCHREGNO,W1.ISSUEDATE) IN ");
        stb.append("         ( SELECT SCHREGNO,");
        stb.append("                      MAX(ISSUEDATE)");
        stb.append("           FROM   SCHREG_ADDRESS_DAT W2");
        stb.append("           WHERE  W2.ISSUEDATE <= '" + _param._eDate + "'");
        stb.append("             AND (W2.EXPIREDATE IS NULL " + " OR W2.EXPIREDATE >= '" + _param._sDate + "')");
        stb.append("           GROUP BY SCHREGNO ");
        stb.append("         )" );
        stb.append("     )ADDR ON REGD.SCHREGNO = ADDR.SCHREGNO");
        stb.append("     LEFT JOIN NAME_MST A020 ");
        stb.append("       ON A020.NAMECD1 = 'A020' ");
        stb.append("      AND ADDR.AREACD  = A020.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L2 ");
        stb.append("       ON BASE.FINSCHOOLCD = L2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L1 ");
        stb.append("       ON REGD.SCHREGNO = L1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     ADDR.AREACD   ASC, ");
        stb.append("     REGD.GRADE    DESC, ");
        stb.append("     REGD.HR_CLASS ASC, ");
        stb.append("     REGD.ATTENDNO ASC ");
        return stb.toString();
    }

    private class Area {
        private final String _areaCd;
        private final String _areaName;
        private String _schoolKind;
        private boolean _haveSchoolKinds;
        private final List _studentList;
        public Area(
                final String areaCd,
                final String areaName
        ) {
            _areaCd = areaCd;
            _areaName = areaName;
            _studentList = new ArrayList();
            _haveSchoolKinds = false;
        }

    }

    private class Student {
        final String _areaCd;
        final String _areaName;
        final String _grade;
        final String _schoolKind;
        final String _gradeName;
        final String _hrClass;
        final String _hrName;
        final String _name;
        final String _finSchoolName;
        final String _guardName;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        public Student(
                final String areaCd,
                final String areaName,
                final String grade,
                final String schoolKind,
                final String gradeName,
                final String hrClass,
                final String hrName,
                final String name,
                final String finSchoolName,
                final String guardName,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno
        ) {
            _areaCd = areaCd;
            _areaName = areaName;
            _grade = grade;
            _schoolKind = schoolKind;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrName = hrName;
            _name = name;
            _finSchoolName = finSchoolName;
            _guardName = guardName;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;

        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74191 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _loginDate;
        private final String _cmd;
        private final String _sDate;
        private final String _eDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _cmd = request.getParameter("cmd");
            _sDate = getSemesterMst(db2, "SDATE");
            _eDate = getSemesterMst(db2, "EDATE");

        }

        private String getSemesterMst(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "' ");
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


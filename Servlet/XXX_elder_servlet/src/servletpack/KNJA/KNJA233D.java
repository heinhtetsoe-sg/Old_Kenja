/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 6cee5c1910bfcbe77ec3480510815721154c6ec4 $
 *
 * 作成日: 2018/08/24
 * 作成者: yamashiro
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJA233D {

    private static final Log log = LogFactory.getLog(KNJA233D.class);

    private boolean _hasData;
    private static final String FRM_HR_NAME = "1";
    private static final String FRM_HR_NAME_FIN = "2";
    private static final String FRM_HR_NAME_ENG = "3";

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
        final String frmName = getFrm();
        svf.VrSetForm(frmName + ".frm", 1);

        final List chairlist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final int max = Math.max(_param._appdate.length, _param._attendclasscd.length);
        for (int i = 0; i < max; i++) {
            final String chaircd = i >= _param._attendclasscd.length ? null : _param._attendclasscd[i];
            final String appdate = i >= _param._appdate.length ? null : _param._appdate[i];
            final String staffcd = i >= _param._nameshow.length ? null : _param._nameshow[i];
            try {
                ps = db2.prepareStatement(getSql(chaircd, appdate));
                rs = ps.executeQuery();
                final Chair chair = new Chair(chaircd, staffcd);
                chairlist.add(chair);
                while (rs.next()) {
                    final Integer attendno = null == rs.getString("attendno") ? null : Integer.valueOf(rs.getString("attendno"));
                    final String setAttendNo = attendno.intValue() > 9 ? attendno.toString() : "0" + attendno.toString();
                    final Student student = new Student(rs.getString("SCHREGNO"), setAttendNo, rs.getString("NAME"), rs.getString("NAME_KANA"), rs.getString("NAME_ENG"), rs.getString("FINSCHOOL_NAME"), rs.getString("SEX"), rs.getString("HR_NAMEABBV"));
                    chair._studentList.add(student);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        for (final Iterator it = chairlist.iterator(); it.hasNext();) {

            final Chair chair = (Chair) it.next();

            for (int exeCnt = 0; exeCnt < _param._kensuu; exeCnt++) {
                int lineCnt = 1;
                for (final Iterator its = chair._studentList.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    final String fullName = null == student._name ? "" : student._name;
                    final int spaceIndex = fullName.indexOf("　"); // 空白文字の位置
                    int spaceIndex2 = -1; // 空白文字2個目
                    String firstName = "";
                    String lastName = "";

                    int ketaFirst = 0;
                    int ketaLast = 0;
                    if (spaceIndex != -1) {
                        spaceIndex2 = fullName.indexOf("　", spaceIndex + 1);
                        firstName = fullName.substring(0, spaceIndex); // 姓
                        lastName = fullName.substring(spaceIndex + 1); // 名
                        ketaFirst = KNJ_EditEdit.getMS932ByteLength(firstName);
                        ketaLast = KNJ_EditEdit.getMS932ByteLength(lastName);
                    }

                    final int rowCnt = getRowCnt();
                    for (int rowNo = 1; rowNo <= rowCnt; rowNo++) {
                        svf.VrsOut("CHAIR_NAME" + rowNo, getOne(db2, getCair(chair._chaircd)));
                        svf.VrsOut("STAFFNAME" + rowNo, getOne(db2, getStaff(chair._staffcd)));

                        svf.VrsOutn("ATTENDNO" + rowNo, lineCnt, student._hrabbv + "-" + student._attendno);

                        final int kanaAddr = KNJ_EditEdit.getMS932ByteLength(student._namekana);
                        svf.VrsOutn("KANA" + rowNo + ( kanaAddr >= 23 ? "_2": ""),lineCnt, student._namekana);

                        svf.VrsOutn("FINSCHOOL_NAME" + rowNo, lineCnt, student._finschoolName);
                        svf.VrsOutn("MARK" + rowNo, lineCnt, student._sex);

                        if (FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
                            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._nameEng) > 22 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._nameEng) > 18 ? "_2" : "_1";
                            svf.VrsOutn("NAME" + rowNo + nameField, lineCnt, student._nameEng);
                        } else {
                            if (spaceIndex < 0 || (ketaFirst > 8 || ketaLast > 8) || 0 <= spaceIndex && spaceIndex < spaceIndex2) {
                                svf.VrsOutn("NAME" + rowNo, lineCnt, fullName);
                            } else {
                                String nameField;
                                if (firstName.length() == 1) {
                                    nameField = "_2"; // 姓１文字
                                } else {
                                    nameField = "_1"; // 姓２文字以上
                                }
                                final String firstNameField1 = "LNAME" + rowNo + nameField;
                                svf.VrsOutn(firstNameField1, lineCnt, firstName);
                                if (lastName.length() == 1) {
                                    nameField = "_2"; // 名１文字
                                } else {
                                    nameField = "_1"; // 名２文字以上
                                }
                                final String lastNameField = "FNAME" + rowNo + nameField;
                                svf.VrsOutn(lastNameField, lineCnt, lastName);
                            }
                        }
                    }
                    _hasData = true;
                    lineCnt++;
                }
                svf.VrEndPage();
            }
        }

    }

    private String getFrm() {
        if (FRM_HR_NAME.equals(_param._frmPatern)) {
            if ("1".equals(_param._kanaPrint)) {
                return "KNJA233D_1_2";
            } else {
                return "KNJA233D_1_1";
            }
        } else if (FRM_HR_NAME_FIN.equals(_param._frmPatern)) {
            if ("1".equals(_param._kanaPrint)) {
                return "KNJA233D_2_2";
            } else {
                return "KNJA233D_2_1";
            }
        } else {
            return "KNJA233D_3";
        }
    }

    private int getRowCnt() {
        if (FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
            return 3;
        } else {
            return 2;
        }
    }

    private static String getOne(final DB2UDB db2, final String sql) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String s = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                s = rs.getString(1);
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return s;
    }

    private String getCair(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    CHAIRNAME ");
        stb.append("FROM ");
        stb.append("    CHAIR_DAT ");
        stb.append("WHERE ");
        stb.append("    YEAR = '" + _param._year + "' ");
        stb.append("    AND SEMESTER = '" + _param._semester+ "' ");
        stb.append("    AND CHAIRCD = '" + chaircd + "' ");
        return stb.toString();
    }

    private String getStaff(final String staffcd){
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    STAFFNAME ");
        stb.append("FROM ");
        stb.append("    STAFF_MST ");
        stb.append("WHERE ");
        stb.append("    STAFFCD = '" + staffcd + "' ");
        return stb.toString();
    }

    private String getSql(final String chaircd, final String appdate) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    BASE.SCHREGNO, ");
        stb.append("    CASE WHEN BASE.SEX = '2' THEN '*' ELSE ' ' END AS SEX, ");
        if ("1".equals(_param._grdNameNasi)) {
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE value(BASE.NAME,'') END AS NAME, ");
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE value(BASE.NAME_KANA,'') END AS NAME_KANA, ");
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE value(BASE.NAME_ENG,'') END AS NAME_ENG, ");
        } else {
            stb.append("    value(BASE.NAME,'') AS NAME, ");
            stb.append("    value(BASE.NAME_KANA,'') AS NAME_KANA, ");
            stb.append("    value(BASE.NAME_ENG,'') AS NAME_ENG, ");
        }
        stb.append("    value(REGDH.HR_NAMEABBV,'') AS HR_NAMEABBV, ");
        stb.append("    value(FINM.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");
        stb.append("    value(REGD.GRADE,'') AS GRADE, ");
        stb.append("    value(REGD.HR_CLASS,'') AS HR_CLASS, ");
        stb.append("    value(REGD.ATTENDNO,'') AS ATTENDNO ");
        stb.append("FROM ");
        stb.append("    CHAIR_STD_DAT CSTD, ");
        stb.append("    SCHREG_BASE_MST BASE ");
        stb.append("    LEFT JOIN FINSCHOOL_MST FINM ON BASE.FINSCHOOLCD = FINM.FINSCHOOLCD, ");
        stb.append("    SCHREG_REGD_DAT REGD, ");
        stb.append("    SCHREG_REGD_HDAT REGDH ");
        stb.append("WHERE ");
        stb.append("    CSTD.YEAR = '" + _param._year + "' AND ");
        stb.append("    CSTD.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    CSTD.CHAIRCD ='" + chaircd + "' AND ");
        stb.append("    CSTD.APPDATE ='" + appdate + "' AND ");
        stb.append("    BASE.SCHREGNO = CSTD.SCHREGNO AND ");
        stb.append("    REGD.SCHREGNO = CSTD.SCHREGNO AND ");
        stb.append("    REGD.YEAR = CSTD.YEAR AND ");
        stb.append("    REGD.SEMESTER = CSTD.SEMESTER AND ");
        stb.append("    REGDH.YEAR = REGD.YEAR AND ");
        stb.append("    REGDH.SEMESTER = REGD.SEMESTER AND ");
        stb.append("    REGDH.GRADE = REGD.GRADE AND ");
        stb.append("    REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("ORDER BY ");
        stb.append("    REGD.GRADE, ");
        stb.append("    REGD.HR_CLASS, ");
        stb.append("    REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Chair {
        final String _chaircd;
        final String _staffcd;
        final List _studentList;
        public Chair(final String chaircd, final String staffcd) {
            _chaircd = chaircd;
            _staffcd = staffcd;
            _studentList = new ArrayList();
        }
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _namekana;
        final String _nameEng;
        final String _finschoolName;
        final String _sex;
        final String _hrabbv;
        public Student(final String schregno, final String attendno, final String name, final String namekana, final String nameEng, final String finschoolName, final String sex, final String hrabbv) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _namekana = namekana;
            _nameEng = nameEng;
            _finschoolName = finschoolName;
            _sex = sex;
            _hrabbv = hrabbv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73504 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _frmPatern;
        private final String _templatePath;
        private final int _kensuu;
        private final String _kanaPrint;
        private final String _grdNameNasi;
        private final String _classcd;
        private final String _subclasscd;
        private final String _excelOutput;
        private final String[] _nameshow;
        private final String[] _appdate;
        private final String[] _attendclasscd;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _kensuu = null == request.getParameter("KENSUU") ? 1 : Integer.parseInt(request.getParameter("KENSUU"));
            _frmPatern = request.getParameter("FRM_PATERN");
            _kanaPrint = request.getParameter("KANA_PRINT");
            _grdNameNasi = request.getParameter("GRD_NAME_NASI");
            _classcd = request.getParameter("CLASSCD");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _excelOutput = request.getParameter("EXCEL_OUTPUT");
            _nameshow = StringUtils.split(request.getParameter("NAME_SHOW"), ",");
            _appdate = null == request.getParameter("APPDATE") ? new String[]{} : StringUtils.split(request.getParameter("APPDATE"), ",");
            _attendclasscd = null == request.getParameter("ATTENDCLASSCD") ? new String[]{} : StringUtils.split(request.getParameter("ATTENDCLASSCD"), ",");
        }
    }
}

// eof

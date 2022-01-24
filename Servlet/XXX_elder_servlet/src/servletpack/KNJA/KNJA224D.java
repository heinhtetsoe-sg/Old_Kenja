/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 81e54d80fb13eb664a68c18a159352c3c11a916b $
 *
 * 作成日: 2018/01/23
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

public class KNJA224D {

    private static final Log log = LogFactory.getLog(KNJA224D.class);

    private boolean _hasData;
    private static final String FRM_HR_NAME = "1";
    private static final String FRM_HR_NAME_FIN = "2";
    private static final String FRM_SCHREG_NAME = "3";
    private static final String FRM_HR_NAME_ENG = "4";

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
        for (int i = 0; i < _param._classSelected.length; i++) {
            final String gradeHrClass = _param._classSelected[i];
            final List printList = getList(db2, gradeHrClass);

            for (int exeCnt = 0; exeCnt < Integer.parseInt(_param._kensuu); exeCnt++) {
                int lineCnt = 1;
                for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                    final StudentInfo studentInfo = (StudentInfo) iterator.next();

                    final String fullName = null == studentInfo._name ? "" : studentInfo._name;
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
                        svf.VrsOut("HR_NAME" + rowNo, studentInfo._hrName);
                        svf.VrsOut("STAFFNAME" + rowNo, studentInfo._staffname);

                        svf.VrsOutn("SCHREGNO" + rowNo, lineCnt, studentInfo._schregno);
                        svf.VrsOutn("ATTENDNO" + rowNo, lineCnt, studentInfo._attendno);

                        final int kanaAddr = KNJ_EditEdit.getMS932ByteLength(studentInfo._name_kana);
                        svf.VrsOutn("KANA" + rowNo + ( kanaAddr >= 23 ? "_2": ""),lineCnt, studentInfo._name_kana);

                        svf.VrsOutn("FINSCHOOL_NAME" + rowNo, lineCnt, studentInfo._finschoolName);
                        svf.VrsOutn("MARK" + rowNo, lineCnt, studentInfo._sex);

                        if (FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
                            final String nameField = KNJ_EditEdit.getMS932ByteLength(studentInfo._nameEng) > 22 ? "_3" : KNJ_EditEdit.getMS932ByteLength(studentInfo._nameEng) > 18 ? "_2" : "_1";
                            svf.VrsOutn("NAME" + rowNo + nameField, lineCnt, studentInfo._nameEng);
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
                    lineCnt++;
                    _hasData = true;
                }
                svf.VrEndPage();
            }
        }

    }

    private String getFrm() {
        if (FRM_HR_NAME.equals(_param._frmPatern)) {
            if ("1".equals(_param._kanaPrint)) {
                return "KNJA224D_1_2";
            } else {
                return "KNJA224D_1_1";
            }
        } else if (FRM_HR_NAME_FIN.equals(_param._frmPatern)) {
            if ("1".equals(_param._kanaPrint)) {
                return "KNJA224D_2_2";
            } else {
                return "KNJA224D_2_1";
            }
        } else if (FRM_SCHREG_NAME.equals(_param._frmPatern)) {
            if ("1".equals(_param._kanaPrint)) {
                return "KNJA224D_3_2";
            } else {
                return "KNJA224D_3_1";
            }
        } else {
            return "KNJA224D_4";
        }
    }

    private int getRowCnt() {
        if (FRM_HR_NAME_FIN.equals(_param._frmPatern)) {
            return 2;
        } else {
            return 3;
        }
    }

    private List getList(final DB2UDB db2, final String gradeHrClass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(gradeHrClass);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String name_kana = rs.getString("NAME_KANA");
                final String name_eng = rs.getString("NAME_ENG");
                final String sex = rs.getString("SEX");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                final String staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));

                final StudentInfo studentInfo = new StudentInfo(grade, hrClass, hrName, hrNameabbv, attendno, schregno, name, name_kana, name_eng, sex, finschoolName, staffname, staffname2);
                retList.add(studentInfo);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql(final String gradeHrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     CASE WHEN BASE.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
        if ("1".equals(_param._grdNameNasi)) {
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE BASE.NAME END AS NAME, ");
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE BASE.NAME_KANA END AS NAME_KANA, ");
            stb.append("     CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE BASE.NAME_ENG END AS NAME_ENG, ");
        } else {
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.NAME_ENG, ");
        }
        stb.append("     VALUE(FINM.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");
        stb.append("     VALUE(STAFF1.STAFFNAME,'') AS STAFFNAME, ");
        stb.append("     VALUE(STAFF2.STAFFNAME,'') AS STAFFNAME2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINM ON BASE.FINSCHOOLCD = FINM.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("           AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("           AND REGD.GRADE = REGH.GRADE ");
        stb.append("           AND REGD.HR_CLASS = REGH.HR_CLASS ");
		stb.append("     LEFT JOIN STAFF_MST STAFF1 ON REGH.TR_CD1 = STAFF1.STAFFCD ");
		stb.append("     LEFT JOIN STAFF_MST STAFF2 ON REGH.TR_CD2 = STAFF2.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + gradeHrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class StudentInfo {
    	final String _grade;
    	final String _hrClass;
    	final String _hrName;
    	final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _name_kana;
        final String _nameEng;
        final String _sex;
        final String _finschoolName;
        final String _staffname;
        final String _staffname2;

        StudentInfo(
        		final String grade,
        		final String hrClass,
        		final String hrName,
        		final String hrNameabbv,
        		final String attendno,
                final String schregno,
                final String name,
                final String name_kana,
                final String name_eng,
                final String sex,
                final String finschoolName,
                final String staffname,
                final String staffname2
        ) {
        	_grade = grade;
        	_hrClass = hrClass;
        	_hrName = hrName;
        	_hrNameabbv = hrNameabbv;
        	_attendno = attendno;
            _schregno = schregno;
            _name = name;
            _name_kana = name_kana;
            _nameEng = name_eng;
            _sex = sex;
            _finschoolName = finschoolName;
            _staffname = staffname;
            _staffname2 = staffname2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73503 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String[] _classSelected;
        private final String _frmPatern;
        private final String _kanaPrint;
        private final String _kensuu;
        private final String _grdNameNasi;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _frmPatern = request.getParameter("FRM_PATERN");
            _kanaPrint = request.getParameter("KANA_PRINT");
            _kensuu = request.getParameter("KENSUU");
            _grdNameNasi = request.getParameter("GRD_NAME_NASI");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof

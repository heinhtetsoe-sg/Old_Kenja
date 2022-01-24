/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 56507b7b39362f06a40aaefa018cdc57fd887f60 $
 *
 * 作成日: 2018/08/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 56507b7b39362f06a40aaefa018cdc57fd887f60 $
 */
public class KNJA224D_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA224sD_XLS.class");

    private boolean _hasData;
    private static final String FRM_HR_NAME = "1";
    private static final String FRM_HR_NAME_FIN = "2";
    private static final String FRM_SCHREG_NAME = "3";
    private static final String FRM_HR_NAME_ENG = "4";

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        outPutXls(response);
    }


    /** XLSデータ出力 */
    protected void outPutXls(final HttpServletResponse response) throws IOException {
        //出力用のシート
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(0);

        final List xlsDataList = getXlsDataList(_db2);
        for (int row = 0; row < xlsDataList.size(); row++) {
            final List xlsData = (List) xlsDataList.get(row);
            for (int col = 0; col < xlsData.size(); col++) {
                final String setData = (String) xlsData.get(col);
                setCellData(outPutSheet, row, col, setData);
            }
        }

        //送信
        response.setHeader("Content-Disposition", "inline;filename=noufu_0.xls");
        response.setContentType("application/vnd.ms-excel");
        _tmpBook.write(response.getOutputStream());
    }

    private List getXlsDataList(final DB2UDB db2) {
        final List xlsDataList = new ArrayList();
        for (int i = 0; i < _param._classSelected.length; i++) {
            final String gradeHrClass = _param._classSelected[i];
            final List printList = getList(db2, gradeHrClass);
            boolean firstFlg = true;
            List row1;
            for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final StudentInfo studentInfo = (StudentInfo) iterator.next();

                if (firstFlg) {
                    row1 = nextLine(xlsDataList);
                    row1.add("年組名称");
                    row1.add(studentInfo._hrName);
                    row1.add("担任名");
                    row1.add(studentInfo._staffname);

                    row1 = nextLine(xlsDataList);

                    if (FRM_SCHREG_NAME.equals(_param._frmPatern)) {
                        row1.add("学籍番号");
                    } else {
                        row1.add("出席番号");
                    }
                    row1.add("性別");
                    row1.add("氏名");
                    if ("1".equals(_param._kanaPrint) && !FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
                        row1.add("ふりがな");
                    }
                    if (FRM_HR_NAME_FIN.equals(_param._frmPatern)) {
                        row1.add("出身校");
                    }

                    firstFlg = false;
                }

                row1 = nextLine(xlsDataList);
                if (FRM_SCHREG_NAME.equals(_param._frmPatern)) {
                    row1.add(studentInfo._schregno);
                } else {
                    row1.add(studentInfo._attendno);
                }
                row1.add(studentInfo._sex);
                if (FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
                    row1.add(studentInfo._nameEng);
                } else {
                    row1.add(studentInfo._name);
                }
                if ("1".equals(_param._kanaPrint) && !FRM_HR_NAME_ENG.equals(_param._frmPatern)) {
                    row1.add(studentInfo._nameKana);
                }
                if (FRM_HR_NAME_FIN.equals(_param._frmPatern)) {
                    row1.add(studentInfo._finschoolName);
                }
            }
            row1 = nextLine(xlsDataList);
        }

        return xlsDataList;
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
                final Integer attendno = null == rs.getString("ATTENDNO") ? null : Integer.valueOf(rs.getString("ATTENDNO"));
                final String setAttendNo = attendno.intValue() > 9 ? attendno.toString() : "0" + attendno.toString();
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String name_kana = rs.getString("NAME_KANA");
                final String name_eng = rs.getString("NAME_ENG");
                final String sex = rs.getString("SEX");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                final String staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));

                final StudentInfo studentInfo = new StudentInfo(grade, hrClass, hrName, hrNameabbv, setAttendNo, schregno, name, name_kana, name_eng, sex, finschoolName, staffname, staffname2);
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
        final String _nameKana;
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
            _nameKana = name_kana;
            _nameEng = name_eng;
            _sex = sex;
            _finschoolName = finschoolName;
            _staffname = staffname;
            _staffname2 = staffname2;
        }
    }

    private List nextLine(final List xlsDataList) {
        final List row = new ArrayList();
        xlsDataList.add(row);
        return row;
    }

    protected void setCellData(final HSSFSheet outPutSheet, int row, int col, final String setData) {
        HSSFCell cell;
        HSSFRow firstRow = outPutSheet.getRow(2);
        if (firstRow == null) {
            firstRow = outPutSheet.createRow(2);
        }
        cell = firstRow.getCell((short) 0);
        if (cell == null) {
            cell = firstRow.createCell((short) 0);
        }
        HSSFRow setRow = outPutSheet.getRow(row);
        if (setRow == null) {
            setRow = outPutSheet.createRow(row);
        }
        cell = setRow.getCell((short) col);
        if (cell == null) {
            cell = setRow.createCell((short) (col));
        }
        //書式をコピー
        cell.setCellStyle(firstRow.getCell((short) 0).getCellStyle());
        if (null != setData) {
            cell.setCellValue(setData);
        }
    }

    protected List getXlsDataList() throws SQLException {
        return null;
    }

    protected List getHeadData() {
        return null;
    }

    protected String[] getCols() {
        return null;
    }

    protected String getSql() {
        return null;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61947 $");
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
        private final String _templatePath;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _frmPatern = request.getParameter("FRM_PATERN");
            _kanaPrint = request.getParameter("KANA_PRINT");
            _kensuu = request.getParameter("KENSUU");
            _grdNameNasi = request.getParameter("GRD_NAME_NASI");
            _templatePath = request.getParameter("TEMPLATE_PATH");
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

// kanji=漢字
/*
 * $Id: 48bf61ab33b4ffe3651383710851052655671cbe $
 *
 * 作成日: 2011/04/01 16:46:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
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
 * @version $Id: 48bf61ab33b4ffe3651383710851052655671cbe $
 */
public class KNJA233A_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA233A_XLS.class");

    private boolean _hasData;

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
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(1);

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

    private String data_get(final String chaircd, final String appdate)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
        stb.append("    value(T1.NAME,'') NAME, ");
        stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
        stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
        stb.append("    value(T2.GRADE,'') GRADE, ");
        stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
        stb.append("    value(T2.ATTENDNO,'') ATTENDNO ");
        stb.append("FROM ");
        stb.append("    CHAIR_STD_DAT T7, ");
        stb.append("    SCHREG_BASE_MST T1, ");
        stb.append("    SCHREG_REGD_DAT T2, ");
        stb.append("    SCHREG_REGD_HDAT T6 ");
        stb.append("WHERE ");
        stb.append("    T7.YEAR = '" + _param._year + "' AND ");
        stb.append("    T7.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    T7.CHAIRCD ='" + chaircd + "' AND ");
        stb.append("    T7.APPDATE ='" + appdate + "' AND ");
        stb.append("    T1.SCHREGNO = T7.SCHREGNO AND ");
        stb.append("    T2.SCHREGNO = T7.SCHREGNO AND ");
        stb.append("    T2.YEAR = T7.YEAR AND ");
        stb.append("    T2.SEMESTER = T7.SEMESTER AND ");
        stb.append("    T6.YEAR = T2.YEAR AND ");
        stb.append("    T6.SEMESTER = T2.SEMESTER AND ");
        stb.append("    T6.GRADE = T2.GRADE AND ");
        stb.append("    T6.HR_CLASS = T2.HR_CLASS ");
        stb.append("ORDER BY ");
        if ("1".equals(_param._output) || "4".equals(_param._output) || "musashi".equals(_param._output)){
            stb.append("    T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");
        }else {
            stb.append("    T1.SCHREGNO ");
        }
        return stb.toString();
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
    
    private List getXlsDataList(final DB2UDB db2) {
        final List chairlist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final int max = Math.max(_param._appdate.length, _param._attendclasscd.length);
        for (int i = 0; i < max; i++) {
            final String chaircd = i >= _param._attendclasscd.length ? null : _param._attendclasscd[i];
            final String appdate = i >= _param._appdate.length ? null : _param._appdate[i];
            final String staffcd = i >= _param._nameshow.length ? null : _param._nameshow[i];
            try {
                ps = db2.prepareStatement(data_get(chaircd, appdate));
                rs = ps.executeQuery();
                final Chair chair = new Chair(chaircd, staffcd);
                chairlist.add(chair);
                while (rs.next()) {
                    final Integer attendno = null == rs.getString("attendno") ? null : Integer.valueOf(rs.getString("attendno"));
                    final Student student = new Student(rs.getString("SCHREGNO"), attendno, rs.getString("name"), rs.getString("name_kana"), rs.getString("SEX"), rs.getString("HR_NAMEABBV"));
                    chair._studentList.add(student);
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        final List xlsDataList = new ArrayList();
        for (final Iterator it = chairlist.iterator(); it.hasNext();) {
            
            final Chair chair = (Chair) it.next();
            List row1;
            for (int ken = 0; ken < _param._kensuu; ken++) {
                row1 = nextLine(xlsDataList);
                if ("1".equals(_param._output) || "musashi".equals(_param._output)) {
                    row1.add("講座名");
                    row1.add(getOne(db2, getCair(chair._chaircd)));
                    row1.add("担任名");
                    row1.add(getOne(db2, getStaff(chair._staffcd)));
                } else {
                    row1.add("講座コード");
                    row1.add(chair._chaircd);
                    row1.add("講座名");
                    row1.add(getOne(db2, getCair(chair._chaircd)));
                    row1.add("担任名");
                    row1.add(getOne(db2, getStaff(chair._staffcd)));
                }

                row1 = nextLine(xlsDataList);
                if ("1".equals(_param._output) || "musashi".equals(_param._output)) {
                    row1.add("連番");
                    row1.add("性別");
                    row1.add("氏名");
                    row1.add("かな");
                    row1.add("年組");
                    row1.add("出席番号");
                } else {
                    row1.add("学籍番号");
                    row1.add("性別");
                    row1.add("氏名");
                    row1.add("かな");
                    row1.add("年組");
                    row1.add("出席番号");
                }
                int renban = 1;
                for (final Iterator its = chair._studentList.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    row1 = nextLine(xlsDataList);
                    if ("1".equals(_param._output) || "musashi".equals(_param._output)) {
                        row1.add(String.valueOf(renban));
                    } else {
                        row1.add(student._schregno);
                    }
                    row1.add(student._sex);
                    row1.add(student._name);
                    row1.add(student._namekana);
                    row1.add(student._hrabbv);
                    row1.add(null == student._attendno ? null : student._attendno.toString());
                    renban++;
                }
                row1 = nextLine(xlsDataList);
            }
        }
        return xlsDataList;
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
    
    private List nextLine(final List xlsDataList) {
        final List row = new ArrayList();
        xlsDataList.add(row);
        return row;
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
        final Integer _attendno;
        final String _name;
        final String _namekana;
        final String _sex;
        final String _hrabbv;
        public Student(final String schregno, final Integer attendno, final String name, final String namekana, final String sex, final String hrabbv) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _namekana = namekana;
            _sex = sex;
            _hrabbv = hrabbv;
        }
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
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final int _kensuu;
        private final String _output;
//        private final String _height;
//        private final String _width;
//        private final String _huriganaOutput;
//        private final String[] _chk;
        private final String _classcd;
        private final String _subclasscd;
//        private final String _testkindcd;
//        private final String _testitemcd;
        private final String _excelOutput;
//        private final String[] _groupcd;
        private final String[] _nameshow;
//        private final String[] _chargediv;
        private final String[] _appdate;
        private final String[] _attendclasscd;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _kensuu = null == request.getParameter("KENSUU") ? 1 : Integer.parseInt(request.getParameter("KENSUU"));
//            _height = request.getParameter("HEIGHT");
//            _width = request.getParameter("WIDTH");
//            _huriganaOutput = request.getParameter("HURIGANA_OUTPUT");
//            _chk = request.getParameterValues("chk");
            _classcd = request.getParameter("CLASSCD");
            _subclasscd = request.getParameter("SUBCLASSCD");
//            _testkindcd = request.getParameter("TESTKINDCD");
//            _testitemcd = request.getParameter("TESTITEMCD");
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _excelOutput = request.getParameter("EXCEL_OUTPUT");
//            _groupcd = StringUtils.split(request.getParameter("GROUPCD"), ",");
            _nameshow = StringUtils.split(request.getParameter("NAME_SHOW"), ",");
//            _chargediv = StringUtils.split(request.getParameter("CHARGEDIV"), ",");
            _appdate = null == request.getParameter("APPDATE") ? new String[]{} : StringUtils.split(request.getParameter("APPDATE"), ",");
            _attendclasscd = null == request.getParameter("ATTENDCLASSCD") ? new String[]{} : StringUtils.split(request.getParameter("ATTENDCLASSCD"), ",");
            _output = request.getParameter("OUTPUT");
        }
    }
}

// eof

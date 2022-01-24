// kanji=漢字
/*
 * $Id: c723eacf47b162d350346bf3a224ae9098db22a5 $
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

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
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
 * @version $Id: c723eacf47b162d350346bf3a224ae9098db22a5 $
 */
public class KNJA223_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA223_XLS.class");

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

        //出力データ取得
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

    private String selectQuery()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    w3.grade || w3.hr_class as grade_hr_class,");
        stb.append("    smallint(w3.attendno) as attendno,");
        stb.append("    CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
        stb.append("    w4.name,");
        stb.append("    w4.name_kana,");
        stb.append("    w1.hr_nameabbv,");
        stb.append("    w2.staffname ");
        stb.append("FROM ");
        stb.append("    schreg_base_mst w4,");
        stb.append("    schreg_regd_dat w3,");
        stb.append("    schreg_regd_hdat w1 ");
        stb.append("    left join staff_mst w2 on w1.tr_cd1 = w2.staffcd ");
        stb.append("WHERE ");
        stb.append("    w1.year = '" + _param._year + "' AND ");
        stb.append("    w1.semester = '" + _param._gakki + "' AND ");
        stb.append("    w1.grade || w1.hr_class in " + SQLUtils.whereIn(true, _param._classselected) + " AND ");
        stb.append("    w1.year = w3.year AND ");
        stb.append("    w1.semester = w3.semester AND ");
        stb.append("    w1.grade = w3.grade AND ");
        stb.append("    w1.hr_class = w3.hr_class AND ");
        stb.append("    w3.schregno = w4.schregno ");
        stb.append("order by w3.grade, w3.hr_class, w3.attendno");

        return stb.toString();
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
    
    private List getXlsDataList(final DB2UDB db2) {
        final List hrclasslist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(selectQuery());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String gradehrclass = rs.getString("grade_hr_class");
                Hrclass hrclass = null;
                for (final Iterator it = hrclasslist.iterator(); it.hasNext();) {
                    final Hrclass hc = (Hrclass) it.next();
                    if (hc._gradehrclass != null && hc._gradehrclass.equals(gradehrclass)) {
                        hrclass = hc;
                    }
                }
                if (null == hrclass) {
                    hrclass = new Hrclass(gradehrclass, rs.getString("hr_nameabbv"), rs.getString("staffname"));
                    hrclasslist.add(hrclass);
                }
                final Integer attendno = null == rs.getString("attendno") ? null : Integer.valueOf(rs.getString("attendno"));
                final Student student = new Student(attendno, rs.getString("name"), rs.getString("name_kana"), rs.getString("SEX"));
                hrclass._studentList.add(student);
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        final List xlsDataList = new ArrayList();
        for (final Iterator it = hrclasslist.iterator(); it.hasNext();) {
            
            final Hrclass hrclass = (Hrclass) it.next();
            List row1;
            
            for (int ken = 0; ken < _param._kensuu; ken++) {
                row1 = nextLine(xlsDataList);
                row1.add("年組：" + (null == hrclass._hrnameabbv ? "" : hrclass._hrnameabbv));
                row1.add(null);
                row1.add("担任名：" + (null == hrclass._staffname ? "" : hrclass._staffname));

                row1 = nextLine(xlsDataList);
                row1.add("出席番号");
                row1.add("性別");
                row1.add("氏名");
                if (!"2".equals(_param._output)) {
                    row1.add("かな");
                }
                int prevAttendno = 0;
                for (final Iterator its = hrclass._studentList.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    if (null == _param._output2) {
                        for (int i = prevAttendno + 1; i < student._attendno.intValue(); i++) {
                            row1 = nextLine(xlsDataList);
                        }
                    }
                    row1 = nextLine(xlsDataList);
                    row1.add(null == student._attendno ? null : student._attendno.toString());
                    row1.add(student._sex);
                    row1.add(student._name);
                    if (!"2".equals(_param._output)) {
                        row1.add(student._namekana);
                    }
                    prevAttendno = null == student._attendno ? prevAttendno + 1 : student._attendno.intValue();
                }
                row1 = nextLine(xlsDataList);
            }
        }
        return xlsDataList;
    }
    
    private List nextLine(final List xlsDataList) {
        final List row = new ArrayList();
        xlsDataList.add(row);
        return row;
    }
    
    private class Hrclass {
        final String _gradehrclass;
        final String _hrnameabbv;
        final String _staffname;
        final List _studentList;
        public Hrclass(final String gradehrclass, final String hrnameabbv, final String staffname) {
            _gradehrclass = gradehrclass;
            _hrnameabbv = hrnameabbv;
            _staffname = staffname;
            _studentList = new ArrayList();
        }
    }
    private class Student {
        final Integer _attendno;
        final String _name;
        final String _namekana;
        final String _sex;
        public Student(final Integer attendno, final String name, final String namekana, final String sex) {
            _attendno = attendno;
            _name = name;
            _namekana = namekana;
            _sex = sex;
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
        private final String[] _classselected;
        private final String _year;
        private final String _gakki;
        private final String _output;
        private final int _kensuu;
        private final String _output2;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classselected = request.getParameterValues("CLASS_SELECTED");
            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _output = request.getParameter("OUTPUT"); // 名票種類
            _kensuu = null == request.getParameter("KENSUU") ? 1 : Integer.parseInt(request.getParameter("KENSUU")); // 出力件数
            _output2 = request.getParameter("OUTPUT2"); // 空白行なし
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }
    }
}

// eof

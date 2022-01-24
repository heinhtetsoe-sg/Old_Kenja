// kanji=漢字
/*
 * $Id: a8c4cc8b86648064a410498201444af8b250620a $
 *
 * 作成日: 2011/02/18 13:14:38 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.io.FileNotFoundException;
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a8c4cc8b86648064a410498201444af8b250620a $
 */
public abstract class AbstractXls {

    private static final Log log = LogFactory.getLog("AbstractXls.class");

    protected HSSFWorkbook _tmpBook = null;
    protected HSSFSheet _sheet0 = null;
    protected List _dataList = null;
    protected List _headList = null;
    protected DB2UDB _db2;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        _db2.open();
        try {
            xls_out(request, response);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(_db2);
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public abstract void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception;

    protected void setIniTmpBook(final String tempPass) throws FileNotFoundException, IOException {
        java.io.InputStream excelTempFileSt = new java.io.FileInputStream(tempPass);
        POIFSFileSystem excelTempFile = new POIFSFileSystem(excelTempFileSt);
        excelTempFileSt.close();
        _tmpBook = new HSSFWorkbook(excelTempFile);

        /* シート0は、パスワード設定用      */
        /* 普段は、非表示の為見えないです。 */
        _sheet0 = _tmpBook.getSheetAt(0);

        passClear();
    }

    protected void passClear() {
        /* シート0のL34にAlp Corporationとあると(デフォルトで入ってる) */
        /* パスワード登録済みとみなされる。パスワード入力をさせる為に  */
        /* L34をクリアしてる。                                         */
        HSSFRow row0 = _sheet0.getRow(33);
        if (row0 != null) {
            HSSFCell cell0 = row0.getCell((short) 11);
            if (cell0 != null) {
                System.out.println("{"+cell0.getStringCellValue()+"}");
                cell0.setCellValue("");
            }
        }
    }

    /** XLSデータ出力 */
    protected void outPutXls(final HttpServletResponse response, final boolean header) throws IOException {
        //出力用のシート
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(1);
        HSSFRow setRow;

        //ヘッダの行の書式を使用する為
        HSSFRow headerRow = outPutSheet.getRow(0);
        setRow = outPutSheet.getRow(0);
        int hedCol = 0;
        for (final Iterator itHead = _headList.iterator(); itHead.hasNext();) {
            final String setXlsHedData = (String) itHead.next();
            setCellData(setRow, headerRow, hedCol++, setXlsHedData);
        }
        setCellData(setRow, headerRow, hedCol++, "DUMMY");

        //最初の行の書式を使用する為
        HSSFRow firstRow = null;
        int line = 0;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 1 : line;
            setRow = outPutSheet.getRow(rowLine);
            firstRow = line == 0 ? outPutSheet.getRow(line + 1) : firstRow;
            if (setRow == null) {
                setRow = outPutSheet.createRow(rowLine);
            }
            int col = 0;
            for (final Iterator itXlsData = xlsData.iterator(); itXlsData.hasNext();) {
                final String setXlsData = (String) itXlsData.next();
                setCellData(setRow, firstRow, col++, setXlsData);
            }
            line++;
        }
        //送信
        response.setHeader("Content-Disposition", "inline;filename=noufu_0.xls");
        response.setContentType("application/vnd.ms-excel");
        _tmpBook.write(response.getOutputStream());
    }

    protected void setCellData(final HSSFRow setRow, final HSSFRow firstRow, int col, final String setData) {
        try {
            HSSFCell cell;
            cell = setRow.getCell((short) col);
            if (cell == null) {
                cell = setRow.createCell((short) (col));
            }
            //書式をコピー
            cell.setCellStyle(firstRow.getCell((short) 0).getCellStyle());
            cell.setCellValue(setData);
        } catch (Exception e) {
            log.error("setCellData colum = " + col + " data = " + setData, e);
        }
    }

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        final String[] cols = getCols();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                for (int i = 0; i < cols.length; i++) {
                    xlsData.add(rsXls.getString(cols[i]));
                }
                xlsData.add("DUMMY");
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    protected abstract String[] getCols();
    protected abstract String getSql();
    protected abstract List getHeadData();
}
// eof

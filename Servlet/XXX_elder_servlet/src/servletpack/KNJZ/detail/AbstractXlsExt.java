// kanji=漢字
/*
 * $Id: 2a834764f266b45366b6f5069a06e94cbd78f2e5 $
 *
 * 作成日: 2017/01/26 13:14:38 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import nao_package.db.DB2UDB;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2a834764f266b45366b6f5069a06e94cbd78f2e5 $
 */
public abstract class AbstractXlsExt {

    private static final Log log = LogFactory.getLog("AbstractXlsExt.class");

    protected Workbook _tmpBook = null;
    protected Sheet _sheet0 = null;
    protected List _dataList = null;
    //protected List _headList = null;
    protected DB2UDB _db2;
    protected int _strtline = 0;
    protected int _strtcol = 0;
    protected MultiHashMap _headList = null;


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
        Workbook wb = new XSSFWorkbook(new FileInputStream(tempPass));
        _tmpBook = wb;

        log.debug(" tempPass = " + tempPass);
        /* シート0は、パスワード設定用      */
        /* 普段は、非表示の為見えないです。 */
        _sheet0 = _tmpBook.getSheetAt(0);

        passClear();
    }

    protected void passClear() {
        /* シート0のL34にAlp Corporationとあると(デフォルトで入ってる) */
        /* パスワード登録済みとみなされる。パスワード入力をさせる為に  */
        /* L34をクリアしてる。                                         */
        Row row0 = _sheet0.getRow(33);
        if (row0 != null) {
            Cell cell0 = row0.getCell((short) 11);
            if (cell0 != null) {
                System.out.println("{"+cell0.getStringCellValue()+"}");
                cell0.setCellValue("");
            }
        }
    }

    /** XLSデータ出力 */
    protected void outPutSht(final int sheet_no, final String sheet_name, final boolean header) throws IOException {
        if (sheet_name.getBytes().length > 31) {
            return;
        }
        //出力用のシート
        log.debug(" sheet_no = " + sheet_no);
        Sheet outPutSheet = _tmpBook.getSheetAt(sheet_no);
        _tmpBook.setSheetName(sheet_no, sheet_name);
        Row setRow = outPutSheet.getRow(2);

        //ヘッダの行の書式を使用する為
        Row headerRow = setRow;
        if (null != _headList) {
            int hedCol = 0;
            for (int idx = 0; idx < _headList.size(); idx++) {
            	Collection iteList = (Collection) _headList.get(String.valueOf(idx));
            	String[] iteList2 = new String[5];
            	for (final Iterator it2 = iteList.iterator(); it2.hasNext();) {
            		iteList2 = (String[])it2.next();
            	}
            	final int set_row = Integer.parseInt(iteList2[0]);
            	final int set_col = Integer.parseInt(iteList2[1]);
            	final String setval = iteList2[2];

            	setRow = outPutSheet.getRow(2);
                if (null == setRow) {
                	setRow = outPutSheet.createRow(set_row);
                }
                headerRow = setRow;
                if (setRow.getCell((short) 0) == null) {
                	setRow.createCell((short) 0);
                }

                log.debug(" __sethead_02__:" + set_row + ":" + set_col);
                setCellData(setRow, headerRow, set_col, setval);
            }
        }

        //最初の行の書式を使用する為
        Row firstRow = null;
        int line = _strtline;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 1 : line;
            firstRow = line == 0 ? outPutSheet.getRow(line + 1) : firstRow;
            if (firstRow == null) {
            	firstRow = outPutSheet.createRow(line);
            	firstRow.createCell((short) 0);
            }
            setRow = outPutSheet.createRow(rowLine);
            int col = _strtcol;
            for (final Iterator itXlsData = xlsData.iterator(); itXlsData.hasNext();) {
                final String setXlsData = (String) itXlsData.next();
                if (setXlsData != null) {
                    setCellData(setRow, firstRow, col++, setXlsData);
                } else {
                	col++;
                }
            }
            line++;
        }
    }

    protected void sendXlsFile(final HttpServletResponse response, final String sendfilename) throws IOException {
        //送信
        String sendfname = "noufu_0.xlsx";
        if (!"".equals(sendfilename)) {
            sendfname = sendfilename;
        }
        
        final String filenameEncoding = isIE(null) ? "MS932" : "UTF-8";
        //log.info(" filenameEncoding = " + filenameEncoding);
        final String encodedFilename = new String(sendfname.getBytes(filenameEncoding), "ISO8859-1");
        response.setHeader("Content-Disposition", "inline;filename=" + encodedFilename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        _tmpBook.write(response.getOutputStream());
    }

    private static boolean isIE(final Map parameter) {
        boolean isIE = true; // デフォルトはtrue
        if (null != parameter && null != parameter.get("HttpServletRequest")) {
            final HttpServletRequest req = (HttpServletRequest) parameter.get("HttpServletRequest");
            isIE = -1 != StringUtils.defaultString(req.getHeader("User-Agent")).indexOf("Trident");
        }
        return isIE;
    }
    
    protected void setCellData(final Row setRow, final Row firstRow, int col, final String setData) {
        try {
            Cell cell;
            cell = setRow.getCell((short) col);
            if (cell == null) {
                cell = setRow.createCell((short) (col));
            }
//            //書式をコピー
//            cell.setCellStyle(firstRow.getCell((short) 0).getCellStyle()); // XmlValueDisconnectedExceptionが発生するためコメント... ooxmlの仕様らしい
            cell.setCellValue(setData);
        } catch (Exception e) {
//            log.error("setCellData colum = " + col + " data = " + setData + " / " + firstRow + " / " + (null == firstRow ? null : firstRow.getCell((short) 0)), e);
            log.error("setCellData colum = " + col + " data = " + setData, e);
        }
    }

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        log.debug(" sql = " + sql);
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
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    protected void setWarning(final HttpServletResponse response, final String msgCd, final String message) throws SQLException, IOException {
        //送信
    	final String content = getMessageMstContent(msgCd);
    	
    	final StringBuffer html = new StringBuffer();
    	html.append("<!DOCTYPE html>");
    	html.append("<html>");
    	html.append("<head>");
    	html.append("</head>");
    	html.append("<body>");
    	html.append("<script type='text/javascript'>");
    	html.append("alert(\"" + StringUtils.defaultString(msgCd) + "\\n\\n" + StringUtils.defaultString(content) + "\");");
    	html.append("window.open('about:blank', '_self').close();");
    	html.append("</script>");
    	html.append("</body>");
    	html.append("</html>");
    	
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(html.toString().getBytes().length);
        PrintStream ps = new PrintStream(response.getOutputStream());
        log.info(" html = " + html);
        ps.println(html.toString());
    }
    
    protected String getMessageMstContent(final String msgCd) throws SQLException {
        final String sql = " SELECT * FROM MESSAGE_MST WHERE MSG_CD = '" + msgCd + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String content = null;
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                content = rs.getString("MSG_CONTENT");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return content;
    }

    protected abstract String[] getCols();
    protected abstract String getSql();
    protected abstract MultiHashMap getHeadData();
}
// eof

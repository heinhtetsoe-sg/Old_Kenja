// kanji=漢字
/*
 * $Id: 060e3acc2fef051bf4cb19a89699e63666f6ba4b $
 *
 * 作成日: 2011/03/04 14:05:25 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 060e3acc2fef051bf4cb19a89699e63666f6ba4b $
 */
public class KNJX180 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX180.class");

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

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
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
                for (int i = 0; i < _param._fieldName.length; i++) {
                    xlsData.add(rsXls.getString(_param._fieldName[i]));
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

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("学年");
        retList.add("クラス");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※年度");
        retList.add("※学籍番号");
        retList.add("※年次");
        for (int i = 0; i < _param._fieldName.length; i++) {
            final String setXlsHedData = (String) _param._headerMap.get(_param._fieldName[i]);
            retList.add(setXlsHedData);
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "YEAR",
                "SCHREGNO",
                "ANNUAL",
                };
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.ANNUAL ");
        for (int i = 0; i < _param._fieldName.length; i++) {
        	if ("TRAIN_REF1".equals(_param._fieldName[i]) || "TRAIN_REF2".equals(_param._fieldName[i]) ||"TRAIN_REF3".equals(_param._fieldName[i])) {
        		stb.append("    ,L1." + _param._fieldName[i]);
        	} else {
        		stb.append("    ,T3." + _param._fieldName[i]);        		
        	}
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN HTRAINREMARK_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SCHREGNO = T1.SCHREGNO ");        
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
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
        private final String _yearSem;
        private final String _gradeHrClass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String[] _fieldName;
        private final Map _headerMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _fieldName = request.getParameterValues("XLS_FIELDNAME");
            _headerMap.put("TOTALSTUDYACT"    , "総合学習活動");
            _headerMap.put("TOTALSTUDYVAL"    , "総合学習評価");
            _headerMap.put("SPECIALACTREMARK" , "特別活動所見");
            _headerMap.put("TOTALREMARK"      , "総合所見");
            _headerMap.put("TRAIN_REF1"       , "学習／行動の特技・特徴等");            
            _headerMap.put("TRAIN_REF2"       , "部活動・資格取得等");            
            _headerMap.put("TRAIN_REF3"       , "その他");            
            _headerMap.put("ATTENDREC_REMARK" , "出欠の記録備考");
            _headerMap.put("VIEWREMARK"       , "観点");
            _headerMap.put("BEHAVEREC_REMARK" , "行動の記録備考");
            _headerMap.put("CLASSACT"         , "学級活動");
            _headerMap.put("STUDENTACT"       , "生徒会活動");
            _headerMap.put("CLUBACT"          , "クラブ活動");
            _headerMap.put("SCHOOLEVENT"      , "学校行事");
        }

    }
}

// eof

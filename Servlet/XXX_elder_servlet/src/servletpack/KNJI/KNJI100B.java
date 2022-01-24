// kanji=漢字
/*
 * $Id: 5b28aa1d0205ef64ec07f4caa4f7512ba95ed108 $
 *
 * 作成日: 2011/03/14 0:49:03 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJI;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5b28aa1d0205ef64ec07f4caa4f7512ba95ed108 $
 */
public class KNJI100B extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJI100B.class");

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
        final String[] cols = getCols();
        int hedCol = 0;
        for (int selected = 0; selected < _param._selectDatas.length; selected++) {
            final String setField = _param._selectDatas[selected];
            for (int i = 0; i < cols.length; i++) {
                if (setField.equals(cols[i])) {
                    final String setXlsHedData = (String) _param._headerMap.get(cols[i]);
                    if ("".equals(_param._outPut) && _param._codeMap.containsKey(cols[i])) {
                        setCellData(setRow, headerRow, hedCol++, (String) _param._codeMap.get(cols[i]));
                    }
                    setCellData(setRow, headerRow, hedCol++, setXlsHedData);
                }
            }
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
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                for (int selected = 0; selected < _param._selectDatas.length; selected++) {
                    final String setField = _param._selectDatas[selected];
                    final String setData = rsXls.getString(setField);
                    if ("".equals(_param._outPut) && _param._codeMap.containsKey(setField)) {
                        final String[] setDataArray = StringUtils.split(setData, ',');
                        xlsData.add(setDataArray.length <= 1 ? "" : setDataArray[0]);
                        xlsData.add(setDataArray.length <= 1 ? "" : setDataArray[1]);
                    } else {
                        xlsData.add(setData);
                    }
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
        return null;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "INOUTCD",
                "NAME",
                "NAME_SHOW",
                "NAME_KANA",
                "NAME_ENG",
                "BIRTHDAY",
                "SEX",
                "BLOODTYPE",
                "BLOOD_RH",
                "FINSCHOOLCD",
                "FINISH_DATE",
                "PRISCHOOLCD",
                "ENT_DATE",
                "ENT_DIV",
                "ENT_REASON",
                "ENT_SCHOOL",
                "ENT_ADDR",
                "GRD_DATE",
                "GRD_DIV",
                "GRD_REASON",
                "GRD_SCHOOL",
                "GRD_ADDR",
                "GRD_NO",
                "GRD_TERM",
                "REMARK1",
                "REMARK2",
                "REMARK3",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "ANNUAL",
                "COURSECD",
                "MAJORCD",
                "COURSECODE",
                "STAFFNAME",
                "ZIPCD",
                "AREACD",
                "ADDR1",
                "ADDR2",
                "ADDR1_ENG",
                "ADDR2_ENG",
                "TELNO",
                "FAXNO",
                "EMAIL",
                "EMERGENCYCALL",
                "EMERGENCYNAME",
                "EMERGENCYRELA_NAME",
                "EMERGENCYTELNO",
                "EMERGENCYCALL2",
                "EMERGENCYNAME2",
                "EMERGENCYRELA_NAME2",
                "EMERGENCYTELNO2",
                "RELATIONSHIP",
                "GUARD_NAME",
                "GUARD_KANA",
                "GUARD_SEX",
                "GUARD_BIRTHDAY",
                "GUARD_ZIPCD",
                "GUARD_ADDR1",
                "GUARD_ADDR2",
                "GUARD_TELNO",
                "GUARD_FAXNO",
                "GUARD_E_MAIL",
                "GUARD_JOBCD",
                "GUARD_WORK_NAME",
                "GUARD_WORK_TELNO",
                "GUARANTOR_RELATIONSHIP",
                "GUARANTOR_NAME",
                "GUARANTOR_KANA",
                "GUARANTOR_SEX",
                "GUARANTOR_ZIPCD",
                "GUARANTOR_ADDR1",
                "GUARANTOR_ADDR2",
                "GUARANTOR_TELNO",
                "GUARANTOR_JOBCD",
                "PUBLIC_OFFICE",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        if ("ungrd".equals(_param._mode)) {
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.INOUTCD,'') || ',' || ");
                stb.append("   VALUE(N1.NAME1,'') INOUTCD, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.INOUTCD INOUTCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N1.NAME1 INOUTCD, ");
            }
            stb.append("   T1.NAME, ");
            stb.append("   T1.NAME_SHOW, ");
            stb.append("   T1.NAME_KANA, ");
            stb.append("   T1.NAME_ENG, ");
            stb.append("   T1.BIRTHDAY, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.SEX,'') || ',' || ");
                stb.append("   VALUE(N2.NAME1,'') SEX,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.SEX SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N2.NAME1 SEX,");
            }
            stb.append("   T1.BLOODTYPE, ");
            stb.append("   T1.BLOOD_RH, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.FINSCHOOLCD FINSCHOOLCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   F1.FINSCHOOL_NAME FINSCHOOLCD,");
            }
            stb.append("   T1.FINISH_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.PRISCHOOLCD PRISCHOOLCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   P1.PRISCHOOL_NAME PRISCHOOLCD,");
            }
            stb.append("   T1.ENT_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.ENT_DIV,'') || ',' || ");
                stb.append("   VALUE(N6.NAME1,'') ENT_DIV, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.ENT_DIV ENT_DIV, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N6.NAME1 ENT_DIV, ");
            }
            stb.append("   T1.ENT_REASON, ");
            stb.append("   T1.ENT_SCHOOL, ");
            stb.append("   T1.ENT_ADDR, ");
            stb.append("   T1.GRD_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.GRD_DIV,'') || ',' || ");
                stb.append("   VALUE(N7.NAME1,'') GRD_DIV, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.GRD_DIV GRD_DIV, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N7.NAME1 GRD_DIV, ");
            }
            stb.append("   T1.GRD_REASON, ");
            stb.append("   T1.GRD_SCHOOL, ");
            stb.append("   T1.GRD_ADDR, ");
            stb.append("   T1.GRD_NO, ");
            stb.append("   T1.GRD_TERM, ");
            stb.append("   T1.REMARK1, ");
            stb.append("   T1.REMARK2, ");
            stb.append("   T1.REMARK3, ");
            stb.append("   T5.GRADE, ");
            stb.append("   T5.HR_CLASS, ");
            stb.append("   T5.ATTENDNO, ");
            stb.append("   T5.ANNUAL, ");
            stb.append("   T5.COURSECD, ");
            stb.append("   T5.MAJORCD, ");
            stb.append("   T5.COURSECODE, ");
            stb.append("   T7.STAFFNAME, ");
            stb.append("   T2.ZIPCD, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T2.AREACD,'') || ',' || ");
                stb.append("   VALUE(N8.NAME1,'') AREACD, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T2.AREACD AREACD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N8.NAME1 AREACD, ");
            }
            stb.append("   T2.ADDR1, ");
            stb.append("   T2.ADDR2, ");
            stb.append("   T2.ADDR1_ENG, ");
            stb.append("   T2.ADDR2_ENG, ");
            stb.append("   T2.TELNO, ");
            stb.append("   T2.FAXNO, ");
            stb.append("   T2.EMAIL, ");
            stb.append("   T1.EMERGENCYCALL, ");
            stb.append("   T1.EMERGENCYNAME, ");
            stb.append("   T1.EMERGENCYRELA_NAME, ");
            stb.append("   T1.EMERGENCYTELNO, ");
            stb.append("   T1.EMERGENCYCALL2, ");
            stb.append("   T1.EMERGENCYNAME2, ");
            stb.append("   T1.EMERGENCYRELA_NAME2, ");
            stb.append("   T1.EMERGENCYTELNO2, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.RELATIONSHIP,'') || ',' || ");
                stb.append("   VALUE(N3.NAME1,'') RELATIONSHIP,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.RELATIONSHIP RELATIONSHIP, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N3.NAME1 RELATIONSHIP,");
            }
            stb.append("   T3.GUARD_NAME, ");
            stb.append("   T3.GUARD_KANA, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARD_SEX,'') || ',' || ");
                stb.append("   VALUE(N4.NAME1,'') GUARD_SEX, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARD_SEX GUARD_SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N4.NAME1 GUARD_SEX, ");
            }
            stb.append("   T3.GUARD_BIRTHDAY, ");
            stb.append("   T3.GUARD_ZIPCD, ");
            stb.append("   T3.GUARD_ADDR1, ");
            stb.append("   T3.GUARD_ADDR2, ");
            stb.append("   T3.GUARD_TELNO, ");
            stb.append("   T3.GUARD_FAXNO, ");
            stb.append("   T3.GUARD_E_MAIL, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARD_JOBCD,'') || ',' || ");
                stb.append("   VALUE(N5.NAME1,'') GUARD_JOBCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARD_JOBCD GUARD_JOBCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N5.NAME1 GUARD_JOBCD,");
            }
            stb.append("   T3.GUARD_WORK_NAME, ");
            stb.append("   T3.GUARD_WORK_TELNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_RELATIONSHIP,'') || ',' || ");
                stb.append("   VALUE(N9.NAME1,'') GUARANTOR_RELATIONSHIP,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_RELATIONSHIP GUARANTOR_RELATIONSHIP, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N9.NAME1 GUARANTOR_RELATIONSHIP,");
            }
            stb.append("   T3.GUARANTOR_NAME, ");
            stb.append("   T3.GUARANTOR_KANA, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_SEX,'') || ',' || ");
                stb.append("   VALUE(N10.NAME1,'') GUARANTOR_SEX, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_SEX GUARANTOR_SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N10.NAME1 GUARANTOR_SEX, ");
            }
            stb.append("   T3.GUARANTOR_ZIPCD, ");
            stb.append("   T3.GUARANTOR_ADDR1, ");
            stb.append("   T3.GUARANTOR_ADDR2, ");
            stb.append("   T3.GUARANTOR_TELNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_JOBCD,'') || ',' || ");
                stb.append("   VALUE(N11.NAME1,'') GUARANTOR_JOBCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_JOBCD GUARANTOR_JOBCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N11.NAME1 GUARANTOR_JOBCD,");
            }
            stb.append("   T3.PUBLIC_OFFICE ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST T1 ");
            stb.append("   LEFT JOIN SCHREG_ADDRESS_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ");
            stb.append("   LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("   LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.INOUTCD AND N1.NAMECD1='A001' ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD2 = T1.SEX AND N2.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N6 ON N6.NAMECD2 = T1.ENT_DIV AND N6.NAMECD1='A002' ");
            stb.append("   LEFT JOIN NAME_MST N7 ON N7.NAMECD2 = T1.GRD_DIV AND N7.NAMECD1='A003' ");
            stb.append("   LEFT JOIN NAME_MST N8 ON N8.NAMECD2 = T2.AREACD AND N8.NAMECD1='A020' ");
            stb.append("   LEFT JOIN GUARDIAN_DAT T3 ON T2.SCHREGNO = T3.SCHREGNO ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD2 = T3.RELATIONSHIP AND N3.NAMECD1='H201' ");
            stb.append("   LEFT JOIN NAME_MST N4 ON N4.NAMECD2 = T3.GUARD_SEX AND N4.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N5 ON N5.NAMECD2 = T3.GUARD_JOBCD AND N5.NAMECD1='H202' ");
            stb.append("   LEFT JOIN NAME_MST N9 ON N9.NAMECD2 = T3.GUARANTOR_RELATIONSHIP AND N9.NAMECD1='H201' ");
            stb.append("   LEFT JOIN NAME_MST N10 ON N10.NAMECD2 = T3.GUARANTOR_SEX AND N10.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1='H202', ");
            stb.append("   SCHREG_REGD_DAT T5, ");
            stb.append("   SCHREG_REGD_HDAT T6, ");
            stb.append("   STAFF_MST T7 ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO IN (" + _param.getInState() + ") AND ");
            stb.append("   T1.SCHREGNO = T5.SCHREGNO AND ");
            stb.append("   T5.YEAR = '" + _param._ctrlYear + "' AND ");
            stb.append("   T5.SEMESTER = '" + _param._ctrlSemester + "' AND ");
            stb.append("   T5.YEAR = T6.YEAR AND   ");
            stb.append("   T5.GRADE = T6.GRADE AND   ");
            stb.append("   T5.HR_CLASS = T6.HR_CLASS AND ");
            stb.append("   T6.TR_CD1 = T7.STAFFCD AND   ");
            stb.append("   T6.SEMESTER = (SELECT ");
            stb.append("                   MAX(ST1.SEMESTER) ");
            stb.append("                 FROM ");
            stb.append("                   SCHREG_REGD_HDAT ST1 ");
            stb.append("                 WHERE ");
            stb.append("                   ST1.YEAR = T5.YEAR AND ");
            stb.append("                   ST1.GRADE = T5.GRADE AND ");
            stb.append("                   ST1.HR_CLASS = T5.HR_CLASS ");
            stb.append("                 ) AND ");
            stb.append("   T2.ISSUEDATE = (SELECT ");
            stb.append("                   MAX(ST2.ISSUEDATE) ");
            stb.append("                 FROM ");
            stb.append("                   SCHREG_ADDRESS_DAT ST2 ");
            stb.append("                 WHERE ");
            stb.append("                   T2.SCHREGNO = ST2.SCHREGNO ");
            stb.append("                 ) ");
            stb.append(" ORDER BY ");
            stb.append("   T5.GRADE, ");
            stb.append("   T5.HR_CLASS, ");
            stb.append("   T5.ATTENDNO ");
        } else {
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.INOUTCD,'') || ',' || ");
                stb.append("   VALUE(N1.NAME1,'') INOUTCD, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.INOUTCD INOUTCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N1.NAME1 INOUTCD, ");
            }
            stb.append("   T1.NAME, ");
            stb.append("   T1.NAME_SHOW, ");
            stb.append("   T1.NAME_KANA, ");
            stb.append("   T1.NAME_ENG, ");
            stb.append("   T1.BIRTHDAY, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.SEX,'') || ',' || ");
                stb.append("   VALUE(N2.NAME1,'') SEX,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.SEX SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N2.NAME1 SEX,");
            }
            stb.append("   T1.BLOODTYPE, ");
            stb.append("   T1.BLOOD_RH, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.FINSCHOOLCD FINSCHOOLCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   F1.FINSCHOOL_NAME FINSCHOOLCD,");
            }
            stb.append("   T1.FINISH_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.PRISCHOOLCD PRISCHOOLCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   P1.PRISCHOOL_NAME PRISCHOOLCD,");
            }
            stb.append("   T1.ENT_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.ENT_DIV,'') || ',' || ");
                stb.append("   VALUE(N6.NAME1,'') ENT_DIV, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.ENT_DIV ENT_DIV, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N6.NAME1 ENT_DIV, ");
            }
            stb.append("   T1.ENT_REASON, ");
            stb.append("   T1.ENT_SCHOOL, ");
            stb.append("   T1.ENT_ADDR, ");
            stb.append("   T1.GRD_DATE, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.GRD_DIV,'') || ',' || ");
                stb.append("   VALUE(N7.NAME1,'') GRD_DIV, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.GRD_DIV GRD_DIV, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N7.NAME1 GRD_DIV, ");
            }
            stb.append("   T1.GRD_REASON, ");
            stb.append("   T1.GRD_SCHOOL, ");
            stb.append("   T1.GRD_ADDR, ");
            stb.append("   T1.GRD_NO, ");
            stb.append("   T1.GRD_TERM, ");
            stb.append("   T1.REMARK1, ");
            stb.append("   T1.REMARK2, ");
            stb.append("   T1.REMARK3, ");
            stb.append("   T5.GRADE, ");
            stb.append("   T5.HR_CLASS, ");
            stb.append("   T5.ATTENDNO, ");
            stb.append("   T5.ANNUAL, ");
            stb.append("   T5.COURSECD, ");
            stb.append("   T5.MAJORCD, ");
            stb.append("   T5.COURSECODE, ");
            stb.append("   T7.STAFFNAME, ");
            stb.append("   T1.CUR_ZIPCD ZIPCD, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T1.CUR_AREACD,'') || ',' || ");
                stb.append("   VALUE(N8.NAME1,'') AREACD, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T1.CUR_AREACD AREACD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N8.NAME1 AREACD, ");
            }
            stb.append("   T1.CUR_ADDR1 ADDR1, ");
            stb.append("   T1.CUR_ADDR2 ADDR2, ");
            stb.append("   T1.CUR_ADDR1_ENG ADDR1_ENG, ");
            stb.append("   T1.CUR_ADDR2_ENG  ADDR2_ENG, ");
            stb.append("   T1.CUR_TELNO TELNO, ");
            stb.append("   T1.CUR_FAXNO FAXNO, ");
            stb.append("   T1.CUR_EMAIL EMAIL, ");
            stb.append("   T1.CUR_EMERGENCYCALL EMERGENCYCALL, ");
            stb.append("   T1.CUR_EMERGENCYNAME EMERGENCYNAME, ");
            stb.append("   T1.CUR_EMERGENCYRELA_NAME EMERGENCYRELA_NAME, ");
            stb.append("   T1.CUR_EMERGENCYTELNO EMERGENCYTELNO, ");
            stb.append("   T1.CUR_EMERGENCYCALL2 EMERGENCYCALL2, ");
            stb.append("   T1.CUR_EMERGENCYNAME2 EMERGENCYNAME2, ");
            stb.append("   T1.CUR_EMERGENCYRELA_NAME2 EMERGENCYRELA_NAME2, ");
            stb.append("   T1.CUR_EMERGENCYTELNO2 EMERGENCYTELNO2, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.RELATIONSHIP,'') || ',' || ");
                stb.append("   VALUE(N3.NAME1,'') RELATIONSHIP,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.RELATIONSHIP RELATIONSHIP, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N3.NAME1 RELATIONSHIP,");
            }
            stb.append("   T3.GUARD_NAME, ");
            stb.append("   T3.GUARD_KANA, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARD_SEX,'') || ',' || ");
                stb.append("   VALUE(N4.NAME1,'') GUARD_SEX, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARD_SEX GUARD_SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N4.NAME1 GUARD_SEX, ");
            }
            stb.append("   T3.GUARD_BIRTHDAY, ");
            stb.append("   T3.GUARD_ZIPCD, ");
            stb.append("   T3.GUARD_ADDR1, ");
            stb.append("   T3.GUARD_ADDR2, ");
            stb.append("   T3.GUARD_TELNO, ");
            stb.append("   T3.GUARD_FAXNO, ");
            stb.append("   T3.GUARD_E_MAIL, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARD_JOBCD,'') || ',' || ");
                stb.append("   VALUE(N5.NAME1,'') GUARD_JOBCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARD_JOBCD GUARD_JOBCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N5.NAME1 GUARD_JOBCD,");
            }
            stb.append("   T3.GUARD_WORK_NAME, ");
            stb.append("   T3.GUARD_WORK_TELNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_RELATIONSHIP,'') || ',' || ");
                stb.append("   VALUE(N9.NAME1,'') GUARANTOR_RELATIONSHIP,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_RELATIONSHIP GUARANTOR_RELATIONSHIP, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N9.NAME1 GUARANTOR_RELATIONSHIP,");
            }
            stb.append("   T3.GUARANTOR_NAME, ");
            stb.append("   T3.GUARANTOR_KANA, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_SEX,'') || ',' || ");
                stb.append("   VALUE(N10.NAME1,'') GUARANTOR_SEX, ");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_SEX GUARANTOR_SEX, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N10.NAME1 GUARANTOR_SEX, ");
            }
            stb.append("   T3.GUARANTOR_ZIPCD, ");
            stb.append("   T3.GUARANTOR_ADDR1, ");
            stb.append("   T3.GUARANTOR_ADDR2, ");
            stb.append("   T3.GUARANTOR_TELNO, ");
            if ("".equals(_param._outPut)){       //コード＋名称
                stb.append("   VALUE(T3.GUARANTOR_JOBCD,'') || ',' || ");
                stb.append("   VALUE(N11.NAME1,'') GUARANTOR_JOBCD,");
            } else if ("1".equals(_param._outPut)){ //コードのみ
                stb.append("   T3.GUARANTOR_JOBCD GUARANTOR_JOBCD, ");
            } else if ("2".equals(_param._outPut)){ //名称のみ
                stb.append("   N11.NAME1 GUARANTOR_JOBCD,");
            }
            stb.append("   T3.PUBLIC_OFFICE ");
            stb.append(" FROM ");
            stb.append("   GRD_BASE_MST T1 "); 
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ");
            stb.append("   LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("   LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.INOUTCD AND N1.NAMECD1='A001' ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD2 = T1.SEX AND N2.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N6 ON N6.NAMECD2 = T1.ENT_DIV AND N6.NAMECD1='A002' ");
            stb.append("   LEFT JOIN NAME_MST N7 ON N7.NAMECD2 = T1.GRD_DIV AND N7.NAMECD1='A003' ");
            stb.append("   LEFT JOIN NAME_MST N8 ON N8.NAMECD2 = T1.CUR_AREACD AND N8.NAMECD1='A020' ");
            stb.append("   LEFT OUTER JOIN GRD_GUARDIAN_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD2 = T3.RELATIONSHIP AND N3.NAMECD1='H201' ");
            stb.append("   LEFT JOIN NAME_MST N4 ON N4.NAMECD2 = T3.GUARD_SEX AND N4.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N5 ON N5.NAMECD2 = T3.GUARD_JOBCD AND N5.NAMECD1='H202' ");
            stb.append("   LEFT JOIN NAME_MST N9 ON N9.NAMECD2 = T3.GUARANTOR_RELATIONSHIP AND N9.NAMECD1='H201' ");
            stb.append("   LEFT JOIN NAME_MST N10 ON N10.NAMECD2 = T3.GUARANTOR_SEX AND N10.NAMECD1='Z002' ");
            stb.append("   LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1='H202', ");
            stb.append("   GRD_REGD_DAT T5, ");
            stb.append("   GRD_REGD_HDAT T6, ");
            stb.append("   STAFF_MST T7 ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO IN (" + _param.getInState() + ") AND ");
            stb.append("   T1.SCHREGNO = T5.SCHREGNO AND ");
            stb.append("   T5.YEAR = FISCALYEAR(T1.GRD_DATE) AND ");
            stb.append("   T5.SEMESTER = T1.GRD_SEMESTER AND ");
            stb.append("   T5.HR_CLASS = T1.GRD_HR_CLASS AND ");
            stb.append("   T5.GRADE = T1.GRD_GRADE AND ");
            stb.append("   T5.ATTENDNO = T1.GRD_ATTENDNO AND ");
            stb.append("   T5.YEAR = T6.YEAR AND   ");
            stb.append("   T5.GRADE = T6.GRADE AND   ");
            stb.append("   T5.HR_CLASS = T6.HR_CLASS AND ");
            stb.append("   T6.TR_CD1 = T7.STAFFCD AND   ");
            stb.append("   T6.SEMESTER = (SELECT ");
            stb.append("                   MAX(ST1.SEMESTER) ");
            stb.append("                 FROM ");
            stb.append("                   SCHREG_REGD_HDAT ST1 ");
            stb.append("                 WHERE ");
            stb.append("                   ST1.YEAR = T5.YEAR AND ");
            stb.append("                   ST1.GRADE = T5.GRADE AND ");
            stb.append("                   ST1.HR_CLASS = T5.HR_CLASS ");
            stb.append("                 ) ");
            stb.append(" ORDER BY ");
            stb.append("   T5.YEAR, ");
            stb.append("   T5.GRADE, ");
            stb.append("   T5.HR_CLASS, ");
            stb.append("   T5.ATTENDNO ");
        }
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
        private final String _outPut;
        private final String _selectData;
        private final String[] _selectDatas;
        private final String _schregNo;
        private final String[] _schregNos;
        private final String _mode;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final Map _headerMap = new HashMap();
        private final Map _codeMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _outPut = request.getParameter("OUTPUT");
            _selectData = request.getParameter("selectdata");
            _selectDatas = StringUtils.split(_selectData, ",");
            _schregNo = request.getParameter("SCHREGNO");
            _schregNos = StringUtils.split(_schregNo, ",");
            _mode = request.getParameter("mode");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _headerMap.put("SCHREGNO", "学籍番号");
            _headerMap.put("INOUTCD", "内外区分(*)");
            _headerMap.put("NAME", "生徒氏名");
            _headerMap.put("NAME_SHOW", "生徒氏名表示用");
            _headerMap.put("NAME_KANA", "生徒氏名かな");
            _headerMap.put("NAME_ENG", "生徒氏名英字");
            _headerMap.put("BIRTHDAY", "生年月日");
            _headerMap.put("SEX", "性別区分(*)");
            _headerMap.put("BLOODTYPE", "血液型");
            _headerMap.put("BLOOD_RH", "血液型(RH)");
            _headerMap.put("FINSCHOOLCD", "出身学校(*)");
            _headerMap.put("FINISH_DATE", "出身校卒業日付");
            _headerMap.put("PRISCHOOLCD", "塾(*)");
            _headerMap.put("ENT_DATE", "入学日付");
            _headerMap.put("ENT_DIV", "入学区分(*)");
            _headerMap.put("ENT_REASON", "入学事由");
            _headerMap.put("ENT_SCHOOL", "入学学校名");
            _headerMap.put("ENT_ADDR", "入学学校住所");
            _headerMap.put("GRD_DATE", "卒業(除籍)日付");
            _headerMap.put("GRD_DIV", "卒業(除籍)区分(*)");
            _headerMap.put("GRD_REASON", "卒業(除籍)事由");
            _headerMap.put("GRD_SCHOOL", "卒業(除籍)学校名");
            _headerMap.put("GRD_ADDR", "卒業(除籍)学校住所");
            _headerMap.put("GRD_NO", "卒業生台帳番号");
            _headerMap.put("GRD_TERM", "卒業期");
            _headerMap.put("REMARK1", "備考１");
            _headerMap.put("REMARK2", "備考２");
            _headerMap.put("REMARK3", "備考３");
            _headerMap.put("GRADE", "学年");
            _headerMap.put("HR_CLASS", "組");
            _headerMap.put("ATTENDNO", "卒業時出席番号");
            _headerMap.put("ANNUAL", "年次");
            _headerMap.put("COURSECD", "課程");
            _headerMap.put("MAJORCD", "学科");
            _headerMap.put("COURSECODE", "コース");
            _headerMap.put("STAFFNAME", "担任");
            _headerMap.put("ZIPCD", "現住所郵便番号");
            _headerMap.put("AREACD", "地区(*)");
            _headerMap.put("ADDR1", "現住所１");
            _headerMap.put("ADDR2", "現住所２");
            _headerMap.put("ADDR1_ENG", "現住所１(英字)");
            _headerMap.put("ADDR2_ENG", "現住所２(英字)");
            _headerMap.put("TELNO", "現住所電話番号");
            _headerMap.put("FAXNO", "現住所FAX番号");
            _headerMap.put("EMAIL", "現住所E-MAILアドレス");
            _headerMap.put("EMERGENCYCALL", "急用連絡先");
            _headerMap.put("EMERGENCYNAME", "急用連絡先名");
            _headerMap.put("EMERGENCYRELA_NAME", "急用連絡先続柄");
            _headerMap.put("EMERGENCYTELNO", "急用電話番号");
            _headerMap.put("EMERGENCYCALL2", "急用連絡先２");
            _headerMap.put("EMERGENCYNAME2", "急用連絡先名２");
            _headerMap.put("EMERGENCYRELA_NAME2", "急用連絡先続柄２");
            _headerMap.put("EMERGENCYTELNO2", "急用電話番号２");
            _headerMap.put("RELATIONSHIP", "保護者との続柄(*)");
            _headerMap.put("GUARD_NAME", "保護者氏名");
            _headerMap.put("GUARD_KANA", "保護者氏名かな");
            _headerMap.put("GUARD_SEX", "保護者性別区分(*)");
            _headerMap.put("GUARD_BIRTHDAY", "保護者生年月日");
            _headerMap.put("GUARD_ZIPCD", "保護者郵便番号");
            _headerMap.put("GUARD_ADDR1", "保護者住所１");
            _headerMap.put("GUARD_ADDR2", "保護者住所２");
            _headerMap.put("GUARD_TELNO", "保護者電話番号");
            _headerMap.put("GUARD_FAXNO", "保護者FAX番号");
            _headerMap.put("GUARD_E_MAIL", "保護者E-MAILアドレス");
            _headerMap.put("GUARD_JOBCD", "保護者職種(*)");
            _headerMap.put("GUARD_WORK_NAME", "保護者勤務先名称");
            _headerMap.put("GUARD_WORK_TELNO", "保護者勤務先電話番号");
            _headerMap.put("GUARANTOR_RELATIONSHIP", "保証人との続柄(*)");
            _headerMap.put("GUARANTOR_NAME", "保証人氏名");
            _headerMap.put("GUARANTOR_KANA", "保証人氏名かな");
            _headerMap.put("GUARANTOR_SEX", "保証人性別区分(*)");
            _headerMap.put("GUARANTOR_ZIPCD", "保証人郵便番号");
            _headerMap.put("GUARANTOR_ADDR1", "保証人住所１");
            _headerMap.put("GUARANTOR_ADDR2", "保証人住所２");
            _headerMap.put("GUARANTOR_TELNO", "保証人電話番号");
            _headerMap.put("GUARANTOR_JOBCD", "保証人職種(*)");
            _headerMap.put("PUBLIC_OFFICE", "兼ねている公職");

            _codeMap.put("INOUTCD", "コード");
            _codeMap.put("SEX", "コード");
            _codeMap.put("FINSCHOOLCD", "コード");
            _codeMap.put("PRISCHOOLCD", "コード");
            _codeMap.put("ENT_DIV", "コード");
            _codeMap.put("GRD_DIV", "コード");
            _codeMap.put("AREACD", "コード");
            _codeMap.put("RELATIONSHIP", "コード");
            _codeMap.put("GUARD_SEX", "コード");
            _codeMap.put("GUARD_JOBCD", "コード");
            _codeMap.put("GUARANTOR_RELATIONSHIP", "コード");
            _codeMap.put("GUARANTOR_SEX", "コード");
            _codeMap.put("GUARANTOR_JOBCD", "コード");
        }

        public String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _schregNos.length; i++) {
                stb.append(sep + "'" + _schregNos[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

    }
}

// eof

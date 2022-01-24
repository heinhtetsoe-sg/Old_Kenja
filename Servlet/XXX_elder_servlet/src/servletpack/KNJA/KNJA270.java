// kanji=漢字
/*
 * $Id: 7ceec403e7415d26ee62f4fcd82d22d1b90863f9 $
 *
 * 作成日: 2011/03/11 14:29:16 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 7ceec403e7415d26ee62f4fcd82d22d1b90863f9 $
 */
public class KNJA270 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA270.class");

    private boolean _hasData;
    private KNJSchoolMst _knjSchoolMst;
    private static final SimpleDateFormat dateSlashFm = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat dateHyphenFm = new SimpleDateFormat("yyyy-MM-dd");

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
        _knjSchoolMst = new KNJSchoolMst(_db2, _param._year);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXlsSakusei(response, _param._header);
    }

    /** XLSデータ出力 */
    protected void outPutXlsSakusei(final HttpServletResponse response, final boolean header) throws IOException, SQLException {
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

    private String getStaffSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(T3.STAFFCD) AS STAFFCNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2, ");
        stb.append("     CHAIR_STF_DAT T3, ");
        stb.append("     ATTEND_DAT T4 ");
        stb.append("     LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = T2.YEAR AND ");
        stb.append("     T1.YEAR = T3.YEAR AND ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("     T1.SEMESTER = T2.SEMESTER AND ");
        stb.append("     T1.SEMESTER = T3.SEMESTER AND ");
        stb.append("     T1.SEMESTER = '" + _param._dateGakki + "' AND ");
        stb.append("     T1.GRADE || T1.HR_CLASS IN (" + _param.getInState() + ") AND ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("     T1.SCHREGNO = T4.SCHREGNO AND ");
        stb.append("     T2.CHAIRCD = T3.CHAIRCD AND ");
        stb.append("     T2.CHAIRCD = T4.CHAIRCD AND ");
        stb.append("     T3.CHARGEDIV = 1 AND ");
        stb.append("     L1.REP_DI_CD <> '0' AND ");
        stb.append("     T4.PERIODCD <> '0' AND ");
        stb.append("     T4.ATTENDDATE BETWEEN '" + _param._date.replace('/', '-') + "' AND '" + _param._date2.replace('/', '-') + "' AND ");
        stb.append("     T4.ATTENDDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T4.ATTENDDATE, ");
        stb.append("     T4.PERIODCD, ");
        stb.append("     T2.CHAIRCD ");
        stb.append(" ORDER BY ");
        stb.append("     STAFFCNT DESC ");

        return stb.toString();
    }

    protected List getXlsDataList() throws SQLException {
        //テスト名の取得
        final Map attendMap = getAttendMap();
        final String sql = getSql(attendMap);
        final String[] cols = getCols();
        List dataList = new ArrayList();
        try {
            if ("1".equals(_param._output)) {
                dataList = getTujouDataList(cols, sql);
            } else if ("3".equals(_param._output)) {
                dataList = getGakkiBetuDataList(cols, sql);
            } else if ("4".equals(_param._output) && "1".equals(_param._ruikei)) {
                dataList = getKamokuBetuRuikeiDataList(cols, sql);
            } else if ("4".equals(_param._output) && "2".equals(_param._ruikei)) {
                dataList = getKamokuBetuGakkiDataList(cols, sql);
            }
        } finally {
            _db2.commit();
        }

        return dataList;
    }

    private List getKamokuBetuGakkiDataList(final String[] cols, final String sql) throws SQLException {
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(_param._year);
                xlsData.add(_param.getSemesterName(_param._semester));
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

    private List getKamokuBetuRuikeiDataList(final String[] cols, final String sql) throws SQLException {
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(_param._year);
                xlsData.add(_param.getSemesterName(_param._semester));
                for (int i = 0; i < cols.length; i++) {
                    if (_param._isKinH && "MAXDAY".equals(cols[i])) {
                        final String days = rsXls.getString("MAXDAY");
                        final String[] dayArray = StringUtils.split(days, '/');
                        if ("01".equals(dayArray[2])) {
                            if ("12".equals(dayArray[1])) {
                                dayArray[1] = "01";
                                dayArray[0] = String.valueOf(Integer.parseInt(dayArray[0]) + 1);
                            } else {
                                dayArray[1] = String.valueOf(Integer.parseInt(dayArray[1]) + 1);
                            }
                        }
                        final String setDay = dayArray[0] + "/" + StringUtils.leftPad(dayArray[1], 2, '0') + "/" + StringUtils.leftPad(dayArray[2], 2, '0');
                        xlsData.add(setDay);
                    } else {
                        xlsData.add(rsXls.getString(cols[i]));
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

    private List getGakkiBetuDataList(final String[] cols, final String sql) throws SQLException {
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(_param._year);
                xlsData.add(_param.getSemesterName(_param._semester));
                for (int i = 0; i < cols.length; i++) {
                    if (_param._isKinH && "MAXDAY".equals(cols[i])) {
                        final String days = rsXls.getString("MAXDAY");
                        final String[] dayArray = StringUtils.split(days, '/');
                        if ("01".equals(dayArray[2])) {
                            if ("12".equals(dayArray[1])) {
                                dayArray[1] = "01";
                                dayArray[0] = String.valueOf(Integer.parseInt(dayArray[0]) + 1);
                            } else {
                                dayArray[1] = String.valueOf(Integer.parseInt(dayArray[1]) + 1);
                            }
                        }
                        final String setDay = dayArray[0] + "/" + StringUtils.leftPad(dayArray[1], 2, '0') + "/" + StringUtils.leftPad(dayArray[2], 2, '0');
                        xlsData.add(setDay);
                    } else {
                        xlsData.add(rsXls.getString(cols[i]));
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

    private List getTujouDataList(final String[] cols, final String sql) throws SQLException {
        final String staffSql = getStaffSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        int staffCnt = 1;
        try {
            ps = _db2.prepareStatement(staffSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                staffCnt = rs.getInt("STAFFCNT");
                break;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }

        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            String rowTmp = "";
            List xlsData = new ArrayList();
            int dataCnt = 1;
            while (rsXls.next()) {
                final String tmpKey = rsXls.getString("SCHREGNO") + rsXls.getString("ATTENDDATE") + rsXls.getString("PERIODCD");
                if (!rowTmp.equals(tmpKey)) {
                    if (!"".equals(rowTmp)) {
                        for (int i = dataCnt; i <= staffCnt; i++) {
                            xlsData.add("");
                        }
                        xlsData.add("DUMMY");
                        dataList.add(xlsData);
                        xlsData = new ArrayList();
                        dataCnt = 1;
                    }
                    xlsData.add(_param._ctrlYear);
                    xlsData.add(_param.getSemesterName(_param._dateGakki));
                    for (int i = 0; i < cols.length; i++) {
                        xlsData.add(rsXls.getString(cols[i]));
                    }
                    dataCnt++;
                } else {
                    xlsData.add(rsXls.getString("STAFFNAME"));
                    dataCnt++;
                }
                rowTmp = tmpKey;
            }
            for (int i = dataCnt; i <= staffCnt; i++) {
                xlsData.add("");
            }
            xlsData.add("DUMMY");
            dataList.add(xlsData);
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }

        return dataList;
    }

    private String getSql(final Map attendMap) {
        String retSql = "";

        if ("1".equals(_param._output)) {
            retSql = getTujou();
        } else if ("3".equals(_param._output)) {
            retSql = getGakkiBetuSql();
        } else if ("4".equals(_param._output) && "1".equals(_param._ruikei)) {
            retSql = getKamokuBetuRuikeiSql();
        } else if ("4".equals(_param._output) && "2".equals(_param._ruikei)) {
            retSql = getKamokuBetuGakkiSql(attendMap);
        }
        return retSql;
    }

    private String getKamokuBetuGakkiSql(final Map attendMap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT SCHREGNO,COURSECD,MAJORCD,COURSECODE,GRADE,HR_CLASS,ATTENDNO ");
        stb.append("     FROM   SCHREG_REGD_DAT ");
        stb.append("     WHERE  YEAR = '" + _param._year + "' ");
        stb.append("       AND  SEMESTER = '" + _param._semester + "' ");
        stb.append("       AND  GRADE || HR_CLASS IN (" + _param.getInState() + ") ");
        stb.append("     ) ");
        stb.append(" ,ATTEND_SUBCLASS AS ( ");
        stb.append("     SELECT * ");
        stb.append("     FROM   ATTEND_SUBCLASS_DAT ");
        stb.append("     WHERE  YEAR='" + _param._year + "' ");
        stb.append("       AND  SEMESTER <= '" + (String) attendMap.get("attendSeme") + "' ");
        stb.append("       AND  MONTH IN (" + (String) attendMap.get("attendMonth") + ") ");
        stb.append("     ) ");
        stb.append(" ,SCHEDULE AS( ");
        stb.append("     SELECT EXECUTEDATE, PERIODCD, CHAIRCD, DATADIV ");
        stb.append("     FROM   SCH_CHR_DAT  ");
        stb.append("     WHERE  EXECUTEDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("       AND  PERIODCD != '0'  ");
        if (_param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append("     ) ");
        stb.append(" ,T_attend_dat AS( ");
        stb.append("      SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ");
        stb.append("             CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ");
        stb.append("             L1.MULTIPLY  ");
        stb.append("      FROM   ATTEND_DAT W1  ");
        stb.append("             LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ");
        stb.append("      WHERE  W1.ATTENDDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ");
        stb.append("                            WHERE  T7.SCHREGNO = W1.SCHREGNO  ");
        stb.append("                              AND  T7.TRANSFERCD IN('1','2')  ");
        stb.append("                              AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ");
        stb.append("     ) ");
        //テスト項目マスタの集計フラグの表
        stb.append("  , TEST_COUNTFLG AS ( ");
        stb.append("      SELECT ");
        stb.append("          T1.EXECUTEDATE, ");
        stb.append("          T1.PERIODCD, ");
        stb.append("          T1.CHAIRCD, ");
        stb.append("          '2' AS DATADIV ");
        stb.append("      FROM ");
        stb.append("          SCH_CHR_TEST T1, ");
        stb.append("          TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("      WHERE ");
        stb.append("              T2.YEAR       = T1.YEAR ");
        stb.append("          AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("          AND T2.COUNTFLG   = '0' "); //0：集計しない 0以外：集計する
        stb.append("      ) ");
        stb.append(" ,T_attend AS( ");
        stb.append(" SELECT S1.SCHREGNO  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         ,S1.CLASSCD ");
            stb.append("         ,S1.SCHOOL_KIND ");
            stb.append("         ,S1.CURRICULUM_CD ");
        }
        stb.append("       ,S1.SUBCLASSCD  ");
        stb.append("       ,S1.SEMESTER  ");
        stb.append("       ,COUNT(*)  AS LESSON  ");
        stb.append("       ,SUM(CASE WHEN S3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  OFFDAYS  ");
        stb.append("       ,SUM(CASE WHEN S4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  ABROAD  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ");
        stb.append("       ,SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ");
        stb.append(" FROM  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, ");
            stb.append("          T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER ");
        } else {
            stb.append("  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD, T2.SEMESTER ");
        }
        stb.append("   FROM   SCHEDULE T1  ");
        stb.append("         ,CHAIR_STD_DAT T2  ");
        stb.append("         ,CHAIR_DAT T3  ");
        stb.append("         ,SCHNO T4  ");
        stb.append("   WHERE  T1.CHAIRCD  = T3.CHAIRCD  ");
        stb.append("     AND  T3.YEAR     = '" + _param._year + "'  ");
        stb.append("     AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ");
        stb.append("     AND  T2.YEAR     = '" + _param._year + "'  ");
        stb.append("     AND  T2.SEMESTER = T3.SEMESTER  ");
        stb.append("     AND  T2.CHAIRCD  = T1.CHAIRCD  ");
        stb.append("     AND  T4.SCHREGNO = T2.SCHREGNO  ");
        stb.append("     AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ");
        stb.append("                     WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ");
        stb.append("                             T5.PERIODCD = T1.PERIODCD AND  ");
        stb.append("                             T5.CHAIRCD = T1.CHAIRCD AND  ");
        stb.append("                             T5.GRADE = T4.GRADE AND  ");
        stb.append("                             T5.HR_CLASS = T4.HR_CLASS AND  ");
        stb.append("                             T1.DATADIV IN ('0','1') AND "); //テスト(DATADIV=2)以外
        stb.append("                             T5.COUNTFLG = '0') ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT 1T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        stb.append("     AND     NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
        stb.append("                         WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("                           AND TEST.PERIODCD    = T1.PERIODCD ");
        stb.append("                           AND TEST.CHAIRCD     = T1.CHAIRCD ");
        stb.append("                           AND TEST.DATADIV     = T1.DATADIV) "); //テスト(DATADIV=2)
        stb.append("     AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ");
        stb.append("                     WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ");
        stb.append("                           ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ");
        stb.append("                            (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER)S1  ");
        } else {
            stb.append("   GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD, T2.SEMESTER)S1  ");
        }
        stb.append("  LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ");
        stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ");
        stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" GROUP BY S1.SCHREGNO, S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD, S1.SEMESTER ");
        } else {
            stb.append(" GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ");
        }
        stb.append("     ) ");
        stb.append(" ,ATTEND_SUM AS( ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SELECT W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER, ");
        } else {
            stb.append("     SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ");
        }
        stb.append("            SUM(W1.LESSON) LESSON, ");
        stb.append("            SUM(W1.OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(W1.ABROAD) ABROAD, ");
        stb.append("            SUM(W1.ABSENT) ABSENT, ");
        stb.append("            SUM(W1.SUSPEND) SUSPEND, ");
        stb.append("            SUM(W1.MOURNING) MOURNING, ");
        stb.append("            SUM(W1.SICK) SICK, ");
        if (_param._useVirus) {
            stb.append("            SUM(W1.VIRUS) VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            SUM(W1.KOUDOME) KOUDOME, ");
        }
        stb.append("            SUM(W1.NOTICE) NOTICE, ");
        stb.append("            SUM(W1.NONOTICE) NONOTICE, ");
        stb.append("            SUM(W1.NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(W1.LATE) LATE, ");
        stb.append("            SUM(W1.EARLY) EARLY ");
        stb.append("     FROM   ATTEND_SUBCLASS W1, SCHNO W0 ");
        stb.append("     WHERE  W1.SCHREGNO = W0.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     GROUP BY W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER ");
            stb.append("      UNION ALL ");
            stb.append("     SELECT W2.SCHREGNO,W2.CLASSCD,W2.SCHOOL_KIND,W2.CURRICULUM_CD,W2.SUBCLASSCD,W2.SEMESTER, ");
        } else {
            stb.append("     GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ");
            stb.append("      UNION ALL ");
            stb.append("     SELECT W2.SCHREGNO,W2.SUBCLASSCD,W2.SEMESTER, ");
        }
        stb.append("            W2.LESSON, ");
        stb.append("            W2.OFFDAYS, ");
        stb.append("            W2.ABROAD, ");
        stb.append("            W2.ABSENT, ");
        stb.append("            W2.SUSPEND, ");
        stb.append("            W2.MOURNING, ");
        stb.append("            W2.SICK, ");
        if (_param._useVirus) {
            stb.append("            W2.VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            W2.KOUDOME, ");
        }
        stb.append("            W2.NOTICE, ");
        stb.append("            W2.NONOTICE, ");
        stb.append("            W2.NURSEOFF, ");
        stb.append("            W2.LATE, ");
        stb.append("            W2.EARLY ");
        stb.append("      FROM   T_attend W2 ");
        stb.append("     ) ");
        //学期毎に清算
        stb.append(" ,ATTEND_SUM2 AS ( ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER, ");
        } else {
            stb.append("     SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ");
        }
        stb.append("            SUM(LESSON) LESSON, ");
        stb.append("            SUM(OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(ABROAD) ABROAD, ");
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        stb.append("            SUM(SICK) SICK, ");
        if (_param._useVirus) {
            stb.append("            SUM(VIRUS) VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            SUM(KOUDOME) KOUDOME, ");
        }
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        if (("3".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ), 4, 1) as NOTICE_LATE ");
        } else if (("1".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ) as NOTICE_LATE ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       as NOTICE_LATE ");
        }
        stb.append("     FROM   ATTEND_SUM ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER ");
        } else {
            stb.append("     GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ");
        }
        stb.append("     ) ");
        //年間で清算
        stb.append(" ,ATTEND_SUM3 AS ( ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD, ");
        } else {
            stb.append("     SELECT SCHREGNO,SUBCLASSCD, ");
        }
        stb.append("            SUM(LESSON) LESSON, ");
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        stb.append("            SUM(OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(ABROAD) ABROAD, ");
        stb.append("            SUM(SICK) SICK, ");
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        stb.append("           ,SUM(NOTICE_LATE) NOTICE_LATE1 ");
        if (("4".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ), 4, 1) as NOTICE_LATE2 ");
        } else if (("2".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ) as NOTICE_LATE2 ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       as NOTICE_LATE2 ");
        }
        stb.append("     FROM   ATTEND_SUM2 ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ");
        } else {
            stb.append("     GROUP BY SCHREGNO,SUBCLASSCD ");
        }
        stb.append("     ) ");

        //メイン
        stb.append(" SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ");
        stb.append("        T2.NAME_SHOW, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        T4.SUBCLASSNAME, ");
        } else {
            stb.append("        T4.SUBCLASSCD,T4.SUBCLASSNAME, ");
        }
        stb.append("       (SELECT CRE.CREDITS ");
        stb.append("          FROM CREDIT_MST CRE ");
        stb.append("         WHERE CRE.YEAR='" + _param._year + "' ");
        stb.append("           AND CRE.COURSECD=T1.COURSECD ");
        stb.append("           AND CRE.MAJORCD=T1.MAJORCD ");
        stb.append("           AND CRE.GRADE=T1.GRADE ");
        stb.append("           AND CRE.COURSECODE=T1.COURSECODE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND CRE.CLASSCD=T3.CLASSCD ");
            stb.append("           AND CRE.SCHOOL_KIND=T3.SCHOOL_KIND ");
            stb.append("           AND CRE.CURRICULUM_CD=T3.CURRICULUM_CD ");
        }
        stb.append("           AND CRE.SUBCLASSCD=T3.SUBCLASSCD) as CREDITS, ");
        stb.append("        T3.LESSON,'" + _param._attendDate.replace('/', '-') + "' AS MAXDAY, ");
        stb.append("        ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE,NURSEOFF,LATE,EARLY ");
        if (("1".equals(_knjSchoolMst._absentCov) || "3".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("   ,NOTICE_LATE1 AS NOTICE_LATE ");
        } else {
            stb.append("   ,NOTICE_LATE2 AS NOTICE_LATE ");
        }
        stb.append(" FROM   SCHNO T1, ");
        stb.append("        SCHREG_BASE_MST T2, ");
        stb.append("        ATTEND_SUM3 T3 ");
        stb.append("       ,SUBCLASS_MST T4 ");
        stb.append(" WHERE  T1.SCHREGNO=T2.SCHREGNO ");
        stb.append("   AND  T1.SCHREGNO=T3.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD=T4.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND=T4.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD=T4.CURRICULUM_CD ");
        }
        stb.append("   AND  T3.SUBCLASSCD=T4.SUBCLASSCD ");
        if ("on".equals(_param._outDiv)){
            stb.append(" and ( ");
            stb.append("        ABSENT>0 ");
            stb.append("     or SUSPEND>0 ");
            stb.append("     or MOURNING>0 ");
            stb.append("     or SICK>0 ");
            stb.append("     or OFFDAYS>0 ");
            stb.append("     or ABROAD>0 ");
            stb.append("     or NONOTICE>0 ");
            stb.append("     or NOTICE>0 ");
            stb.append("     or NURSEOFF>0 ");
            stb.append("     or LATE>0 ");
            stb.append("     or EARLY>0 ");
            stb.append("     ) ");
        }
        stb.append(" ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T4.SUBCLASSCD ");
        return stb.toString();
    }

    private String getKamokuBetuRuikeiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO,COURSECD,MAJORCD,COURSECODE,GRADE,HR_CLASS,ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND GRADE || HR_CLASS IN (" + _param.getInState() + ") ");
        stb.append(" ) ");
        stb.append(" ,ATTEND AS ( ");
        stb.append("     SELECT SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("            SUBCLASSCD, ");
        stb.append("            SUM(LESSON) LESSON, ");
        stb.append("            MAX(CASE WHEN MONTH BETWEEN '01' AND '03' ");
        stb.append("                     THEN RTRIM(CHAR(INT(YEAR)+1)) || '/' || MONTH || '/' || APPOINTED_DAY ");
        stb.append("                     ELSE YEAR || '/' || MONTH || '/' || APPOINTED_DAY END) AS MAXDAY, ");
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        stb.append("            SUM(SICK) SICK, ");
        stb.append("            SUM(OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(ABROAD) ABROAD, ");
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        if (("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) && !"0".equals(_knjSchoolMst._absentCovLate)) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ),4,1) as NOTICE_LATE ");
        } else if (("1".equals(_knjSchoolMst._absentCov) || "2".equals(_knjSchoolMst._absentCov)) && !"0".equals(_knjSchoolMst._absentCovLate)) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       ) as NOTICE_LATE ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("             + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("             + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("             + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("             + sum(VIRUS) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("             + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("             + sum(ABSENT) ");
            }
            stb.append("       as NOTICE_LATE ");
        }
        stb.append("      FROM  ATTEND_SUBCLASS_DAT ");
        stb.append("     WHERE  YEAR = '" + _param._year + "' ");
        stb.append("       AND  SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ");
        } else {
            stb.append("     GROUP BY SCHREGNO,SUBCLASSCD ");
        }
        stb.append("     ) ");

        //メイン
        stb.append(" SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ");
        stb.append("        T2.NAME_SHOW, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        T4.SUBCLASSNAME, ");
        } else {
            stb.append("        T4.SUBCLASSCD,T4.SUBCLASSNAME, ");
        }
        stb.append("       (SELECT CRE.CREDITS ");
        stb.append("          FROM CREDIT_MST CRE ");
        stb.append("         WHERE CRE.YEAR = '" + _param._year + "' ");
        stb.append("           AND CRE.COURSECD = T1.COURSECD ");
        stb.append("           AND CRE.MAJORCD = T1.MAJORCD ");
        stb.append("           AND CRE.GRADE = T1.GRADE ");
        stb.append("           AND CRE.COURSECODE = T1.COURSECODE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CRE.CLASSCD = T3.CLASSCD ");
            stb.append("     AND CRE.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND CRE.CURRICULUM_CD = T3.CURRICULUM_CD ");
        }
        stb.append("           AND CRE.SUBCLASSCD = T3.SUBCLASSCD) as CREDITS, ");
        stb.append("        LESSON,MAXDAY,ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE, ");
        stb.append("        NURSEOFF, ");
        stb.append("        LATE,EARLY ");
        stb.append("       ,NOTICE_LATE ");
        stb.append(" FROM   SCHNO T1, ");
        stb.append("        SCHREG_BASE_MST T2, ");
        stb.append("        ATTEND T3 ");
        stb.append("       ,SUBCLASS_MST T4 ");
        stb.append(" WHERE  T1.SCHREGNO=T2.SCHREGNO ");
        stb.append("   AND  T1.SCHREGNO=T3.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T3.CLASSCD = T4.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T4.CURRICULUM_CD ");
        }
        stb.append("   AND  T3.SUBCLASSCD=T4.SUBCLASSCD ");
        if ("on".equals(_param._outDiv)){
            stb.append(" and ( ");
            stb.append("        ABSENT>0 ");
            stb.append("     or SUSPEND>0 ");
            stb.append("     or MOURNING>0 ");
            stb.append("     or SICK>0 ");
            stb.append("     or OFFDAYS>0 ");
            stb.append("     or ABROAD>0 ");
            stb.append("     or NONOTICE>0 ");
            stb.append("     or NOTICE>0 ");
            stb.append("     or NURSEOFF>0 ");
            stb.append("     or LATE>0 ");
            stb.append("     or EARLY>0 ");
            stb.append("     ) ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,SUBCLASSCD ");
        } else {
            stb.append(" ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T4.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private String getGakkiBetuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND GRADE || HR_CLASS IN (" + _param.getInState() + ") ");
        stb.append(" ) ");
        stb.append(" ,ATTEND AS ( ");
        stb.append("     SELECT SCHREGNO, ");
        stb.append("            SUM(LESSON) LESSON, ");
        stb.append("            MAX(CASE WHEN MONTH BETWEEN '01' AND '03' ");
        stb.append("                     THEN RTRIM(CHAR(INT(YEAR)+1)) || '/' || MONTH || '/' || APPOINTED_DAY ");
        stb.append("                     ELSE YEAR || '/' || MONTH || '/' || APPOINTED_DAY END) AS MAXDAY, ");
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        stb.append("            SUM(SICK) SICK, ");
        stb.append("            SUM(OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(ABROAD) ABROAD, ");
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        stb.append("  FROM  ATTEND_SEMES_DAT ");
        stb.append("     WHERE  YEAR = '" + _param._year + "' ");
        stb.append("       AND  SEMESTER = '" + _param._semester + "' ");
        stb.append("     GROUP BY SCHREGNO ");
        stb.append("     ) ");

        //メイン
        stb.append(" SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ");
        stb.append("        T2.NAME_SHOW, ");
        stb.append("        LESSON,MAXDAY,ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE, ");
        stb.append("        LATE,EARLY ");
        stb.append(" FROM   SCHNO T1, ");
        stb.append("        SCHREG_BASE_MST T2, ");
        stb.append("        ATTEND T3 ");
        stb.append(" WHERE  T1.SCHREGNO=T2.SCHREGNO ");
        stb.append("   AND  T1.SCHREGNO=T3.SCHREGNO ");
        if ("on".equals(_param._outDiv)){
            stb.append(" and ( ");
            stb.append("        ABSENT>0 ");
            stb.append("     or SUSPEND>0 ");
            stb.append("     or MOURNING>0 ");
            stb.append("     or SICK>0 ");
            stb.append("     or OFFDAYS>0 ");
            stb.append("     or ABROAD>0 ");
            stb.append("     or NONOTICE>0 ");
            stb.append("     or NOTICE>0 ");
            stb.append("     or LATE>0 ");
            stb.append("     or EARLY>0 ");
            stb.append("     ) ");
        }
        stb.append(" ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ");
        return stb.toString();
    }

    private String getTujou() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME_SHOW ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1, ");
        stb.append("         SCHREG_BASE_MST T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._dateGakki + "' ");
        stb.append("         AND T1.GRADE || T1.HR_CLASS IN (" + _param.getInState() + ") ");
        stb.append("         AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ), ATTEND AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.NAME_SHOW, ");
        stb.append("         T2.ATTENDDATE, ");
        stb.append("         T2.PERIODCD, ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         ATTEND_DI.REP_DI_CD AS DI_CD, ");
        stb.append("         ATTEND_DI.DI_NAME1 AS DI_NAME, ");
        stb.append("         VALUE(L1.NAME1, T2.DI_REMARK) AS DI_REMARK ");
        stb.append("     FROM ");
        stb.append("         SCHINFO T1, ");
        stb.append("         ATTEND_DAT T2 ");
        stb.append("         INNER JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T2.YEAR AND ATTEND_DI.DI_CD = T2.DI_CD ");
        stb.append("         LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C901' AND L1.NAMECD2 = T2.DI_REMARK_CD ");
        stb.append("     WHERE ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("         T2.ATTENDDATE BETWEEN '" + _param._date.replace('/', '-') + "' AND '" + _param._date2.replace('/', '-') + "' AND ");
        stb.append("         T2.DI_CD <> '0' AND ");
        stb.append("         T2.PERIODCD <> '0' AND ");
        stb.append("         T2.YEAR = '" + _param._ctrlYear + "' AND ");
        //テスト項目マスタの集計フラグの表
        stb.append(" ), TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T2.COUNTFLG "); //0：集計しない 0以外：集計する
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("     WHERE ");
        stb.append("             T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append(" ),ATTEND_FLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         CASE WHEN DAT.DATADIV='2' THEN TEST.COUNTFLG ELSE T2.COUNTFLG END AS COUNTFLG ");
        stb.append("     FROM ");
        stb.append("         ATTEND T1 ");
        stb.append("         INNER JOIN SCH_CHR_DAT DAT ");
        stb.append("             ON  DAT.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("             AND DAT.PERIODCD    = T1.PERIODCD ");
        stb.append("             AND DAT.CHAIRCD     = T1.CHAIRCD ");
        if (_param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append("         LEFT JOIN TEST_COUNTFLG TEST ");
        stb.append("             ON  TEST.EXECUTEDATE = T1.ATTENDDATE ");
        stb.append("             AND TEST.PERIODCD    = T1.PERIODCD ");
        stb.append("             AND TEST.CHAIRCD     = T1.CHAIRCD ");
        stb.append("     LEFT JOIN SCH_CHR_COUNTFLG T2 ON ");
        stb.append("             T2.EXECUTEDATE = T1.ATTENDDATE AND  ");
        stb.append("             T2.PERIODCD = T1.PERIODCD AND  ");
        stb.append("             T2.CHAIRCD = T1.CHAIRCD AND  ");
        stb.append("             T2.GRADE = T1.GRADE  AND  ");
        stb.append("             T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ),CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         T3.CHAIRNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.CLASSCD, ");
            stb.append("         T3.SCHOOL_KIND, ");
            stb.append("         T3.CURRICULUM_CD, ");
        }
        stb.append("         T3.SUBCLASSCD, ");
        stb.append("         T4.STAFFCD ");
        stb.append("     FROM ");
        stb.append("         SCHINFO T1, ");
        stb.append("         CHAIR_STD_DAT T2, ");
        stb.append("         CHAIR_DAT T3, ");
        stb.append("         CHAIR_STF_DAT T4 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR = T3.YEAR AND ");
        stb.append("         T2.YEAR = T4.YEAR AND ");
        stb.append("         T2.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("         T2.SEMESTER = T3.SEMESTER AND ");
        stb.append("         T2.SEMESTER = T4.SEMESTER AND ");
        stb.append("         T2.SEMESTER = '" + _param._dateGakki + "' AND ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("         T2.CHAIRCD = T3.CHAIRCD AND ");
        stb.append("         T2.CHAIRCD = T4.CHAIRCD AND ");
        stb.append("         T4.CHARGEDIV = 1 ");
        stb.append("     ORDER BY ");
        stb.append("         T4.STAFFCD ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME_SHOW, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.DI_CD, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T3.SUBCLASSNAME, ");
        stb.append("     T1.DI_NAME, ");
        stb.append("     T1.DI_REMARK, ");
        stb.append("     T1.COUNTFLG, ");
        stb.append("     T2.CHAIRNAME, ");
        stb.append("     T4.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_FLG T1, ");
        stb.append("     CHAIR T2, ");
        stb.append("     SUBCLASS_MST T3, ");
        stb.append("     STAFF_MST T4 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("     T1.CHAIRCD = T2.CHAIRCD AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD = T3.CLASSCD AND ");
            stb.append("     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ");
            stb.append("     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ");
        }
        stb.append("     T2.SUBCLASSCD = T3.SUBCLASSCD AND ");
        stb.append("     T2.STAFFCD = T4.STAFFCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.DI_CD, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    private Object getAttendData(final Map attendMap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" ,ATTEND_SUBCLASS AS ( ");
        stb.append("     SELECT * ");
        stb.append("     FROM   ATTEND_SUBCLASS_DAT ");
        stb.append("     WHERE  YEAR='" + _param._year + "' ");
        stb.append("       AND  SEMESTER <= '" + _param._semester + "' ");
        stb.append("       AND  MONTH IN (" + (String) attendMap.get("attendMonth") + ") ");
        stb.append("     ) ");
        stb.append(" ,SCHEDULE AS( ");
        stb.append("     SELECT EXECUTEDATE, PERIODCD, CHAIRCD, DATADIV ");
        stb.append("     FROM   SCH_CHR_DAT  ");
        stb.append("     WHERE  EXECUTEDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("       AND  PERIODCD != '0'  ");
        if (_param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append("     ) ");
        stb.append(" ,T_attend_dat AS( ");
        stb.append("      SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD,L1.REP_DI_CD AS DI_CD,L1.MULTIPLY  ");
        stb.append("      FROM   ATTEND_DAT W1  ");
        stb.append("             LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ");
        stb.append("      WHERE  W1.ATTENDDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ");
        stb.append("                            WHERE  T7.SCHREGNO = W1.SCHREGNO  ");
        stb.append("                              AND  T7.TRANSFERCD IN('1','2')  ");
        stb.append("                              AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ");
        stb.append("     ) ");
        //テスト項目マスタの集計フラグの表
        stb.append("  , TEST_COUNTFLG AS ( ");
        stb.append("      SELECT ");
        stb.append("          T1.EXECUTEDATE, ");
        stb.append("          T1.PERIODCD, ");
        stb.append("          T1.CHAIRCD, ");
        stb.append("          '2' AS DATADIV ");
        stb.append("      FROM ");
        stb.append("          SCH_CHR_TEST T1, ");
        stb.append("          TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("      WHERE ");
        stb.append("              T2.YEAR       = T1.YEAR ");
        stb.append("          AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("          AND T2.COUNTFLG   = '0' "); //0：集計しない 0以外：集計する
        stb.append("      ) ");
        stb.append(" ,T_attend AS( ");
        stb.append(" SELECT S1.SCHREGNO  ");
        stb.append("       ,S1.SUBCLASSCD  ");
        stb.append("       ,S1.SEMESTER  ");
        stb.append("       ,COUNT(*)  AS LESSON  ");
        stb.append("       ,SUM(CASE WHEN S3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  OFFDAYS  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ");
        stb.append("       ,SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ");
        stb.append(" FROM  ");
        stb.append("  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD, T2.SEMESTER ");
        stb.append("   FROM   SCHEDULE T1  ");
        stb.append("         ,CHAIR_STD_DAT T2  ");
        stb.append("         ,CHAIR_DAT T3  ");
        stb.append("         ,SCH_T T4  ");
        stb.append("   WHERE  T1.CHAIRCD  = T3.CHAIRCD  ");
        stb.append("     AND  T3.YEAR     = '" + _param._year + "'  ");
        stb.append("     AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ");
        stb.append("     AND  T2.YEAR     = '" + _param._year + "'  ");
        stb.append("     AND  T2.SEMESTER = T3.SEMESTER  ");
        stb.append("     AND  T2.CHAIRCD  = T1.CHAIRCD  ");
        stb.append("     AND  T4.SCHREGNO = T2.SCHREGNO  ");
        stb.append("     AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ");
        stb.append("                     WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ");
        stb.append("                             T5.PERIODCD = T1.PERIODCD AND  ");
        stb.append("                             T5.CHAIRCD = T1.CHAIRCD AND  ");
        stb.append("                             T5.GRADE = T4.GRADE AND  ");
        stb.append("                             T5.HR_CLASS = T4.HR_CLASS AND  ");
        stb.append("                             T1.DATADIV IN ('0','1') AND "); //テスト(DATADIV=2)以外
        stb.append("                             T5.COUNTFLG = '0') ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        stb.append("     AND     NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
        stb.append("                         WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("                           AND TEST.PERIODCD    = T1.PERIODCD ");
        stb.append("                           AND TEST.CHAIRCD     = T1.CHAIRCD ");
        stb.append("                           AND TEST.DATADIV     = T1.DATADIV) "); //テスト(DATADIV=2)
        stb.append("     AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ");
        stb.append("                     WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ");
        stb.append("                           ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ");
        stb.append("                            (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ");
        stb.append("   GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD, T2.SEMESTER)S1  ");
        stb.append("  LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ");
        stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ");
        stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ");
        stb.append(" GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ");
        stb.append("     ) ");
        stb.append(" ,ATTEND_SUM AS( ");
        stb.append("     SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ");
        stb.append("            SUM(W1.LESSON) LESSON, ");
        stb.append("            SUM(W1.OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(W1.ABSENT) ABSENT, ");
        stb.append("            SUM(W1.SUSPEND) SUSPEND, ");
        stb.append("            SUM(W1.MOURNING) MOURNING, ");
        if (_param._useVirus) {
            stb.append("            SUM(W1.VIRUS) VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            SUM(W1.KOUDOME) KOUDOME, ");
        }
        stb.append("            SUM(W1.SICK) SICK, ");
        stb.append("            SUM(W1.NOTICE) NOTICE, ");
        stb.append("            SUM(W1.NONOTICE) NONOTICE, ");
        stb.append("            SUM(W1.NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(W1.LATE) LATE, ");
        stb.append("            SUM(W1.EARLY) EARLY ");
        stb.append("     FROM   ATTEND_SUBCLASS W1, SCH_T W0 ");
        stb.append("     WHERE  W1.SCHREGNO = W0.SCHREGNO ");
        stb.append("     GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ");
        stb.append("      UNION ALL ");
        stb.append("     SELECT W2.SCHREGNO,W2.SUBCLASSCD,W2.SEMESTER, ");
        stb.append("            W2.LESSON, ");
        stb.append("            W2.OFFDAYS, ");
        stb.append("            W2.ABSENT, ");
        stb.append("            W2.SUSPEND, ");
        stb.append("            W2.MOURNING, ");
        if (_param._useVirus) {
            stb.append("            W2.VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            W2.KOUDOME, ");
        }
        stb.append("            W2.SICK, ");
        stb.append("            W2.NOTICE, ");
        stb.append("            W2.NONOTICE, ");
        stb.append("            W2.NURSEOFF, ");
        stb.append("            W2.LATE, ");
        stb.append("            W2.EARLY ");
        stb.append("      FROM   T_attend W2 ");
        stb.append("     ) ");
        //学期毎に清算
        stb.append(" ,ATTEND_SUM2 AS ( ");
        stb.append("     SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ");
        stb.append("            SUM(LESSON) LESSON, ");
        stb.append("            SUM(OFFDAYS) OFFDAYS, ");
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        if (_param._useVirus) {
            stb.append("            SUM(VIRUS) VIRUS, ");
        }
        if (_param._useKoudome) {
            stb.append("            SUM(KOUDOME) KOUDOME, ");
        }
        stb.append("            SUM(SICK) SICK, ");
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        if (("3".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCov))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") ");
            stb.append("                + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("                  ), 4, 1) as NOTICE_LATE ");
        } else if (("1".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCov))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") ");
            stb.append("         + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("           ) as NOTICE_LATE ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("       as NOTICE_LATE ");
        }
        stb.append("     FROM   ATTEND_SUM ");
        stb.append("     GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ");
        stb.append("     ) ");
        //年間で清算
        stb.append(" ,ATTEND_SUM3 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         SUBCLASSCD, ");
        stb.append("         SUM(NOTICE_LATE) NOTICE_LATE1 ");
        if (("4".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCov))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") ");
            stb.append("                + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("                  ), 4, 1) as NOTICE_LATE2 ");
        } else if (("2".equals(_knjSchoolMst._absentCov)) && (!"0".equals(_knjSchoolMst._absentCov))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _knjSchoolMst._absentCovLate + ") ");
            stb.append("        + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("          ) as NOTICE_LATE2 ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("                   + sum(OFFDAYS) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append("                   + sum(ABSENT) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append("                   + sum(SUSPEND) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append("                   + sum(MOURNING) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append("                   + sum(KOUDOME) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append("                   + sum(VIRUS) ");
            }
            stb.append("       as NOTICE_LATE2 ");
        }
        stb.append("     FROM ");
        stb.append("         ATTEND_SUM2 ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO,SUBCLASSCD ");
        stb.append("     ) ");

        return stb.toString();
    }

    /**
     * @return
     */
    private Object GetAttendData2() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.SUBCLASSCD, ");
        if (("1".equals(_knjSchoolMst._absentCov) || "3".equals(_knjSchoolMst._absentCov)) &&
            (!"0".equals(_knjSchoolMst._absentCovLate))) {
            stb.append("     NOTICE_LATE1 AS NOTICE_LATE ");
        } else {
            stb.append("     NOTICE_LATE2 AS NOTICE_LATE ");
        }
        stb.append(" FROM ");
        stb.append("     SCH_T T1, ");
        stb.append("     ATTEND_SUM3 T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
        return stb.toString();
    }

    private Map getAttendMap() throws SQLException {
        Map retMap = new HashMap();
        if ("4".equals(_param._output) && "2".equals(_param._ruikei)) {
            final String attendDateSql = getAttendDate();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(attendDateSql);
                rs = ps.executeQuery();
                java.util.Date attendDate = dateSlashFm.parse(_param._attendDate);
                while (rs.next()) {
                    String dateStr = rs.getString("MAX_YEAR") + "/" + rs.getString("MONTH") + "/" + rs.getString("MAX_APP");
                    if (_param._isKinH) {
                        final String[] dayArray = StringUtils.split(dateStr, '/');
                        if ("01".equals(dayArray[2])) {
                            if ("12".equals(dayArray[1])) {
                                dayArray[1] = "01";
                                dayArray[0] = String.valueOf(Integer.parseInt(dayArray[0]) + 1);
                            } else {
                                dayArray[1] = String.valueOf(Integer.parseInt(dayArray[1]) + 1);
                            }
                        }
                        dateStr = dayArray[0] + "/" + StringUtils.leftPad(dayArray[1], 2, '0') + "/" + StringUtils.leftPad(dayArray[2], 2, '0');
                    }
                    java.util.Date getDateData = dateSlashFm.parse(dateStr);
                    if (attendDate.before(getDateData)) {
                        break;
                    }
                    String setMonth = "'" + rs.getString("MONTH") + "'";
                    if (retMap.containsKey("attendMonth")) {
                        setMonth = (String) retMap.get("attendMonth") + "," + setMonth;
                    }
                    retMap.put("attendMonth", setMonth);
                    retMap.put("attendSdate", dateStr.replace('/', '-'));
                    retMap.put("attendSeme", rs.getString("SEMESTER"));
                }
                final String attendMonth = (String) retMap.get("attendMonth");
                if (null == attendMonth || attendMonth.length() == 0) {
                    retMap.put("attendMonth", "''");
                }

                String attendSdate = (String) retMap.get("attendSdate");
                if (null != attendSdate && !"".equals(attendSdate)) {
                    //次の日
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateSlashFm.parse(attendSdate.replace('-', '/')));
                    cal.add(Calendar.DATE, 1);
                    attendSdate = dateHyphenFm.format(cal.getTime());
                } else {
                    //学期開始日
                    final String sdateSql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER = '1'";
                    PreparedStatement psSdate = null;
                    ResultSet rsSdate = null;
                    try {
                        psSdate = _db2.prepareStatement(sdateSql);
                        rsSdate = psSdate.executeQuery();
                        rsSdate.next();
                        attendSdate = rsSdate.getString("SDATE");
                    } finally {
                        DbUtils.closeQuietly(null, psSdate, rsSdate);
                        _db2.commit();
                    }
                }
                retMap.put("attendSdate", attendSdate);
            } catch (ParseException e) {
                log.error("ParseException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        }
        return retMap;
    }

    private String getAttendDate() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ");
        stb.append("     MONTH, ");
        stb.append("     MAX(APPOINTED_DAY) AS MAX_APP ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SEMES_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER <= '" + _param._semester + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SEMESTER,MONTH ");
        stb.append(" ORDER BY ");
        stb.append("     MAX_YEAR, ");
        stb.append("     MONTH ");

        return stb.toString();
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        if ("1".equals(_param._output)) {
            retList.add("年度");
            retList.add("学期");
            retList.add("学籍番号");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("生徒氏名");
            retList.add("出欠日付");
            retList.add("勤怠コード");
            retList.add("校時");
            retList.add("科目");
            retList.add("出欠");
            retList.add("備考");
            retList.add("集計フラグ");
            retList.add("講座");

            final String staffSql = getStaffSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(staffSql);
                rs = ps.executeQuery();
                int staffCnt = 1;
                while (rs.next()) {
                    staffCnt = rs.getInt("STAFFCNT");
                    break;
                }
                for (int i = 1; i <= staffCnt; i++) {
                    retList.add("講座担当職員" + i);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        } else if ("3".equals(_param._output)) {
            retList.add("年度");
            retList.add("学期");
            retList.add("学籍番号");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("生徒氏名");
            retList.add("授業日数");
            retList.add("集計日付");
            retList.add("公欠");
            retList.add("出停");
            retList.add("忌引");
            retList.add("休学");
            retList.add("留学");
            for (final Iterator iter = _param._nameMstC001List.iterator(); iter.hasNext();) {
                final nameMstData nameMstData = (nameMstData) iter.next();
                retList.add(nameMstData._name);
            }
            retList.add("遅刻");
            retList.add("早退");
        } else if ("4".equals(_param._output)) {
            retList.add("年度");
            retList.add("学期");
            retList.add("学籍番号");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("生徒氏名");
            retList.add("科目コード");
            retList.add("科目名");
            retList.add("単位数");
            retList.add("時数");
            retList.add("集計日付");
            retList.add("公欠");
            retList.add("出停");
            retList.add("忌引");
            retList.add("休学");
            retList.add("留学");
            for (final Iterator iter = _param._nameMstC001List.iterator(); iter.hasNext();) {
                final nameMstData nameMstData = (nameMstData) iter.next();
                retList.add(nameMstData._name);
            }
            retList.add("保健室欠課");
            retList.add("遅刻（欠課数換算前の数字）");
            retList.add("早退（欠課数換算前の数字）");
            retList.add("欠課数");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols1 = {"SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "ATTENDDATE",
                "DI_CD",
                "PERIODCD",
                "SUBCLASSNAME",
                "DI_NAME",
                "DI_REMARK",
                "COUNTFLG",
                "CHAIRNAME",
                "STAFFNAME",};
        final String[] cols3 = {"SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "LESSON",
                "MAXDAY",
                "ABSENT",
                "SUSPEND",
                "MOURNING",
                "OFFDAYS",
                "ABROAD",
                "SICK",
                "NOTICE",
                "NONOTICE",
                "LATE",
                "EARLY",};
        final String[] cols4 = {"SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "SUBCLASSCD",
                "SUBCLASSNAME",
                "CREDITS",
                "LESSON",
                "MAXDAY",
                "ABSENT",
                "SUSPEND",
                "MOURNING",
                "OFFDAYS",
                "ABROAD",
                "SICK",
                "NOTICE",
                "NONOTICE",
                "NURSEOFF",
                "LATE",
                "EARLY",
                "NOTICE_LATE",};
        final String[] cols5 = {"SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "SUBCLASSCD",
                "SUBCLASSNAME",
                "CREDITS",
                "LESSON",
                "MAXDAY",
                "ABSENT",
                "SUSPEND",
                "MOURNING",
                "OFFDAYS",
                "ABROAD",
                "SICK",
                "NOTICE",
                "NONOTICE",
                "NURSEOFF",
                "LATE",
                "EARLY",
                "NOTICE_LATE",};

        return "1".equals(_param._output) ? cols1 : "3".equals(_param._output) ? cols3 :  "4".equals(_param._output) && "1".equals(_param._ruikei) ? cols4 : cols5;
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
        private final String _attendDate;
        private final String _outDiv;
        private final String _ruikei;
        private final String _selectData;
        private final String[] _selectDatas;
        private final String _output;
        private final String _date;
        private final String _date2;
        private final boolean _useVirus;
        private final boolean _useKoudome;
        private final List _nameMstC001List;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _dateGakki;
        private final boolean _header;
        private final String _templatePath;
        private final Map _semesterName;
        private boolean _isKinH;
        private String _useCurriculumcd;
        private final boolean _hasSchChrDatExecutediv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _attendDate = request.getParameter("ATTENDDATE");
            _outDiv = request.getParameter("OUTDIV");
            _ruikei = request.getParameter("RUIKEI");
            _selectData = request.getParameter("selectdata");
            _output = request.getParameter("OUTPUT");
            _date = request.getParameter("DATE");
            _date2 = request.getParameter("DATE2");
            _useVirus = "true".equals(request.getParameter("useVirus"));
            _useKoudome = "true".equals(request.getParameter("useKoudome"));
            _selectDatas = StringUtils.split(_selectData, ",");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _dateGakki = request.getParameter("DATE_SEME");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _nameMstC001List = getNameMstC001List();
            _semesterName = setSemesterName();
            _isKinH = getKinH();
            _useCurriculumcd =request.getParameter("useCurriculumcd");
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
        }

        private boolean getKinH() throws SQLException {
            boolean retB = false;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS K_HIGHSCHOOL ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOLNAME1 like '%近畿大学%' ");
            stb.append("     AND SCHOOLNAME1 not like '%中学%' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                retB = rs.getInt("K_HIGHSCHOOL") == 0 ? false : true;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retB;
        }

        private Map setSemesterName() throws SQLException {
            final Map ret = new HashMap();
            final String semeSql = "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' ORDER BY SEMESTER ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                rs.next();
                ret.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return ret;
        }

        private List getNameMstC001List() throws SQLException {
            final List retList = new ArrayList();
            final String nameMstC001Sql = getNameMstC001Sql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(nameMstC001Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final nameMstData data = new nameMstData(rs.getString("NAMECD2"), rs.getString("NAME1"));
                    retList.add(data);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retList;
        }

        private String getNameMstC001Sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'C001' ");
            stb.append("     AND NAMECD2 IN ('4', '5', '6') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");

            return stb.toString();
        }

        public String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _selectDatas.length; i++) {
                stb.append(sep + "'" + _selectDatas[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

        private String getSemesterName(final String semester) {
            final String semeName = _semesterName.containsKey(semester) ? (String) _semesterName.get(semester) : "";
            return semeName;
        }

        private boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }
    }

    private class nameMstData {
        final String _cd;
        final String _name;

        public nameMstData(final String cd, final String name) {
            _cd = cd;
            _name = name;
        }
    }
}

// eof

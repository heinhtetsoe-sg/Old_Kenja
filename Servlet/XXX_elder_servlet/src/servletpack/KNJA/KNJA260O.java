// kanji=漢字
/*
 * $Id: f6612928e810a7ce14ea923e415e666bace7993f $
 *
 * 作成日: 2011/04/07 22:26:38 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
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
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f6612928e810a7ce14ea923e415e666bace7993f $
 */
public class KNJA260O extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA260O.class");

    private boolean _hasData;
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

        //2行目の出力
        HSSFRow line2Row = outPutSheet.getRow(1);
        setRow = outPutSheet.getRow(1);
        int line2Col = 0;
        setCellData(setRow, line2Row, line2Col++, "出欠集計日付　" + _param._attendDate);
        //空データ
        for (int i = 0; i < 7; i++) {
            setCellData(setRow, line2Row, line2Col++, "");
        }
        for (final Iterator iter = _param._subClassList.iterator(); iter.hasNext();) {
            final SubclassData subclassData = (SubclassData) iter.next();
            setCellData(setRow, line2Row, line2Col++, subclassData._cd);
            setCellData(setRow, line2Row, line2Col++, "");
        }
        setCellData(setRow, line2Row, line2Col++, "");

        //最初の行の書式を使用する為
        HSSFRow firstRow = null;
        int line = 0;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 2 : line;
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
        //テスト名の取得
        final String testname = getTestName();
        final Map attendMap = getAttendMap();
        final String sql = getSql(attendMap);
        final String[] cols = getCols();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(_param._year);
                xlsData.add(_param._semester);
                for (int i = 0; i < cols.length; i++) {
                    xlsData.add(rsXls.getString(cols[i]));
                }
                xlsData.add(testname);
                for (int cnt = 0; cnt < _param._subClassList.size(); cnt++) {
                    final String setTokuten = rsXls.getString("TOKUTEN" + cnt);
                    xlsData.add("9999".equals(setTokuten) ? "-" : "8888".equals(setTokuten) ? "=" : setTokuten);
                    xlsData.add(rsXls.getString("DI" + cnt));
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

    private String getSql(final Map attendMap) {
        //科目コードのIN文作成
        final StringBuffer subclassInstate = new StringBuffer();
        String sep = "";
        for (final Iterator iter = _param._subClassList.iterator(); iter.hasNext();) {
            final SubclassData subclassData = (SubclassData) iter.next();
            subclassInstate.append(sep + "'" + subclassData._cd + "'");
            sep = ",";
        }
        if (subclassInstate.length() == 0) {
            subclassInstate.append("''");
        }

        //成績データ取得のフィールド指定
        String intrTerm = "";
        if("9".equals(_param._semester)){
            if("9".equals(_param._testKindCd)){
                intrTerm = "GRAD_VALUE";
            }
        } else {
            if("01".equals(_param._testKindCd)){
                intrTerm = "SEM" + _param._semester + "_INTR_SCORE";
            } else if ("02".equals(_param._testKindCd)) {
                intrTerm = "SEM" + _param._semester + "_INTR_VALUE";
            } else if ("03".equals(_param._testKindCd)) {
                intrTerm = "SEM" + _param._semester + "_TERM_SCORE";
            } else if ("04".equals(_param._testKindCd)) {
                intrTerm = "SEM" + _param._semester + "_TERM_VALUE";
            } else if ("05".equals(_param._testKindCd)) {
                intrTerm = "SEM" + _param._semester + "_TERM2_SCORE";
            } else if ("06".equals(_param._testKindCd)) {
                intrTerm = "SEM" + _param._semester + "_TERM2_VALUE";
            } else {
                intrTerm = "SEM" + _param._semester + "_VALUE";
            }
        }

        final StringBuffer stb = new StringBuffer();
        //対象生徒
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     SEMESTER = '" + _param.getSetSeme() + "' AND ");
        stb.append("     GRADE || HR_CLASS IN (" + _param.getInState() + ") ");
        stb.append(" ) ");

        //出欠データ
        stb.append(getAttendData(attendMap));
        stb.append(" , ATTEND_T AS ( ");
        stb.append(GetAttendData2());
        stb.append(" ) ");

        //メインデータ
        stb.append(" SELECT ");
        stb.append("     M1.SCHREGNO, ");
        stb.append("     M2.GRADE, ");
        stb.append("     M2.HR_CLASS, ");
        stb.append("     M2.ATTENDNO, ");
        stb.append("     M3.NAME_SHOW ");

        int cnt = 0;
        if ("01".equals(_param._testKindCd) || "03".equals(_param._testKindCd) || "05".equals(_param._testKindCd)) {
            for (final Iterator iter = _param._subClassList.iterator(); iter.hasNext();) {
                final SubclassData subclassData = (SubclassData) iter.next();
                stb.append("     ,SUM( CASE M1.SUBCLASSCD WHEN '" + subclassData._cd + "' THEN M1." + intrTerm + " ELSE NULL END ) AS TOKUTEN" + cnt + " ");
                stb.append("     ,SUM( CASE M1.SUBCLASSCD WHEN '" + subclassData._cd + "' THEN L1.NOTICE_LATE ELSE NULL END) AS DI" + cnt + " ");
                cnt++;
            }
        } else {
            for (final Iterator iter = _param._subClassList.iterator(); iter.hasNext();) {
                final SubclassData subclassData = (SubclassData) iter.next();
                stb.append("     ,SUM( CASE M1.SUBCLASSCD WHEN '" + subclassData._cd + "' THEN CASE M1." + intrTerm + "_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE M1." + intrTerm + " END ELSE NULL END ) AS TOKUTEN" + cnt + " ");
                stb.append("     ,SUM( CASE M1.SUBCLASSCD WHEN '" + subclassData._cd + "' THEN L1.NOTICE_LATE ELSE NULL END) as DI" + cnt + " ");
                cnt++;
            }
        }
        stb.append(" FROM ");
        stb.append("     RECORD_DAT M1 ");
        stb.append("     LEFT JOIN ATTEND_T L1 ON L1.SCHREGNO = M1.SCHREGNO ");
        stb.append("          AND L1.SUBCLASSCD = M1.SUBCLASSCD, ");
        stb.append("     SCH_T M2, ");
        stb.append("     SCHREG_BASE_MST M3 ");
        stb.append(" WHERE ");
        stb.append("     M2.YEAR = M1.YEAR AND ");
        stb.append("     M2.SCHREGNO = M1.SCHREGNO AND ");
        stb.append("     M3.SCHREGNO = M2.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     M1.SCHREGNO, ");
        stb.append("     M2.GRADE, ");
        stb.append("     M2.HR_CLASS, ");
        stb.append("     M2.ATTENDNO, ");
        stb.append("     M3.NAME_SHOW ");
        stb.append(" ORDER BY ");
        stb.append("     M2.GRADE, ");
        stb.append("     M2.HR_CLASS, ");
        stb.append("     M2.ATTENDNO ");

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
        stb.append("     SELECT EXECUTEDATE, PERIODCD, CHAIRCD ");
        stb.append("     FROM   SCH_CHR_DAT  ");
        stb.append("     WHERE  EXECUTEDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("       AND  PERIODCD != '0'  ");
        if (_param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append("     GROUP BY EXECUTEDATE, PERIODCD, CHAIRCD ");
        stb.append("     ) ");
        stb.append(" ,T_attend_dat AS( ");
        stb.append("      SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD,L1.REP_DI_CD  ");
        stb.append("      FROM   ATTEND_DAT W1  ");
        stb.append("             LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ");
        stb.append("      WHERE  W1.ATTENDDATE BETWEEN DATE('" + (String) attendMap.get("attendSdate") + "') AND DATE('" + _param._attendDate.replace('/', '-') + "')  ");
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ");
        stb.append("                            WHERE  T7.SCHREGNO = W1.SCHREGNO  ");
        stb.append("                              AND  T7.TRANSFERCD IN('1','2')  ");
        stb.append("                              AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ");
        stb.append("     ) ");
        stb.append(" ,T_attend AS( ");
        stb.append(" SELECT S1.SCHREGNO  ");
        stb.append("       ,S1.SUBCLASSCD  ");
        stb.append("       ,S1.SEMESTER  ");
        stb.append("       ,COUNT(*)  AS LESSON  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ");
        stb.append("       ,SUM(CASE S2.DI_CD WHEN '15' THEN 1 ELSE 0 END)  AS  LATE  ");
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
        stb.append("            SUM(W1.ABSENT) ABSENT, ");
        stb.append("            SUM(W1.SUSPEND) SUSPEND, ");
        stb.append("            SUM(W1.MOURNING) MOURNING, ");
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
        stb.append("            W2.ABSENT, ");
        stb.append("            W2.SUSPEND, ");
        stb.append("            W2.MOURNING, ");
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
        stb.append("            SUM(ABSENT) ABSENT, ");
        stb.append("            SUM(SUSPEND) SUSPEND, ");
        stb.append("            SUM(MOURNING) MOURNING, ");
        stb.append("            SUM(SICK) SICK, ");
        stb.append("            SUM(NOTICE) NOTICE, ");
        stb.append("            SUM(NONOTICE) NONOTICE, ");
        stb.append("            SUM(NURSEOFF) NURSEOFF, ");
        stb.append("            SUM(LATE) LATE, ");
        stb.append("            SUM(EARLY) EARLY ");
        if (("3".equals(_param._absentCov)) && (!"0".equals(_param._absentCovLate))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _param._absentCovLate + ") ");
            stb.append("                + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            stb.append("                  ), 4, 1) as NOTICE_LATE ");
        } else if (("1".equals(_param._absentCov)) && (!"0".equals(_param._absentCovLate))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _param._absentCovLate + ") ");
            stb.append("         + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            stb.append("           ) as NOTICE_LATE ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
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
        if (("4".equals(_param._absentCov)) && (!"0".equals(_param._absentCovLate))) {
            stb.append("       ,decimal((float(sum(LATE) + sum(EARLY)) / " + _param._absentCovLate + ") ");
            stb.append("                + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            stb.append("                  ), 4, 1) as NOTICE_LATE2 ");
        } else if (("2".equals(_param._absentCov)) && (!"0".equals(_param._absentCovLate))) {
            stb.append("       ,((sum(LATE) + sum(EARLY)) / " + _param._absentCovLate + ") ");
            stb.append("        + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
            stb.append("          ) as NOTICE_LATE2 ");
        } else {
            stb.append("       ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ");
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
        if (("1".equals(_param._absentCov) || "3".equals(_param._absentCov)) &&
            (!"0".equals(_param._absentCovLate))) {
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

    private String getTestName() throws SQLException {
        String testname;
        if ("01".equals(_param._testKindCd)) {
            testname = "中間試験";
        } else if ("02".equals(_param._testKindCd)) {
            testname = "中間成績";
        } else if ("03".equals(_param._testKindCd)) {
            testname = "期末試験";
        } else if ("04".equals(_param._testKindCd)) {
            testname = "期末成績";
        } else if ("05".equals(_param._testKindCd)) {
            testname = "期末2試験";
        } else if ("06".equals(_param._testKindCd)) {
            testname = "期末2成績";
        } else if ("0".equals(_param._testKindCd)) {
            testname = "学期成績";
        } else {
            testname = "学年成績";
        }
        return testname;
    }

    private Map getAttendMap() throws SQLException {
        Map retMap = new HashMap();
        final String attendDateSql = getAttendDate();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = _db2.prepareStatement(attendDateSql);
            rs = ps.executeQuery();
            java.util.Date attendDate = dateSlashFm.parse(_param._attendDate);
            while (rs.next()) {
                final String dateStr = rs.getString("MAX_YEAR") + "/" + rs.getString("MONTH") + "/" + rs.getString("MAX_APP");
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
        if (!"9".equals(_param._semester)){
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        }else {
            stb.append("     AND SEMESTER <= '" + _param._ctrlSemester + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     SEMESTER, ");
        stb.append("     MONTH ");
        stb.append(" ORDER BY ");
        stb.append("     MAX_YEAR, ");
        stb.append("     MONTH ");

        return stb.toString();
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("年度");
        retList.add("学期");
        retList.add("学籍番号");
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("生徒氏名");
        retList.add("テスト種別名");
        for (final Iterator iter = _param._subClassList.iterator(); iter.hasNext();) {
            final SubclassData subclassData = (SubclassData) iter.next();
            retList.add(subclassData._name);
            retList.add("欠課時数");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",};
        return cols;
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
        private final String _testKindCd;
        private final String _selectData;
        private final String[] _selectDatas;
        private final List _subClassList;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _semesterName;
        private String _absentCov;
        private String _absentCovLate;
        private final boolean _hasSchChrDatExecutediv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _attendDate = request.getParameter("ATTENDDATE");
            _testKindCd = request.getParameter("TESTKINDCD");
            _selectData = request.getParameter("selectdata");
            _selectDatas = StringUtils.split(_selectData, ",");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _subClassList = getSubclassList();
            _semesterName = getSemesterName();
            setAbsentCov();
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
        }

        private void setAbsentCov() throws SQLException {
            _absentCov = null;
            _absentCovLate = "0";
            final String sql = "SELECT ABSENT_COV, value(ABSENT_COV_LATE,0) AS ABSENT_COV_LATE FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _absentCov = rs.getString("ABSENT_COV");
                    _absentCovLate = rs.getString("ABSENT_COV_LATE");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        }

        private String getSemesterName() throws SQLException {
            String retStr = "";
            final String semeSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                rs.next();
                retStr = rs.getString("SEMESTERNAME");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retStr;
        }

        private String getSetSeme() {
            return !"9".equals(_semester) ? _semester : _ctrlSemester;
        }

        private List getSubclassList() throws SQLException {
            final List retList = new ArrayList();
            final String subclassSql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SubclassData data = new SubclassData(rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"));
                    retList.add(data);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retList;
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     RECORD_DAT T1, ");
            stb.append("     SUBCLASS_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     AND T1.SCHREGNO IN ( ");
            stb.append("                     SELECT ");
            stb.append("                         SCHREGNO ");
            stb.append("                     FROM ");
            stb.append("                         SCHREG_REGD_DAT ");
            stb.append("                     WHERE ");
            stb.append("                         YEAR = '" + _year + "' ");
            stb.append("                         AND SEMESTER = '" + getSetSeme() + "' ");
            stb.append("                         AND GRADE || HR_CLASS IN (" + getInState() + ") ");
            stb.append("                     ) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");

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

    private class SubclassData {
        final String _cd;
        final String _name;

        public SubclassData(final String cd, final String name) {
            _cd = cd;
            _name = name;
        }
    }
}

// eof

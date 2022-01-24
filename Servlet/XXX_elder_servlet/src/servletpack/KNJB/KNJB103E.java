/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/02/15
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJB103E {

    private static final Log log = LogFactory.getLog(KNJB103E.class);

    private static final int MAX_LINE = 40;

    private boolean _hasData;

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

    private List getPageMapList(final Map map, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final PrintData printData = (PrintData) map.get(key);
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(printData);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map printMap = getPrintMap(db2);
        final List pageList = getPageMapList(printMap, MAX_LINE);

        for (Iterator itPage = pageList.iterator(); itPage.hasNext();) {
            final List printDataList = (List) itPage.next();

            svf.VrSetForm("KNJB103E.frm", 4);

            // タイトルを印字
            printTitle(db2, svf);

            // 明細部を印字
            String preGrpcd = null;
            int cntGrpcd = 0;
            for (Iterator itData = printDataList.iterator(); itData.hasNext();) {
                final PrintData printData = (PrintData) itData.next();

                final String grpcd = printData._grade + "-" + printData._hrClass + "-" + printData._staffcd;
                if (!grpcd.equals(preGrpcd)) {
                    cntGrpcd++;
                    preGrpcd = grpcd;

                    svf.VrsOut("HR_NAME1", printData._hrNameabbv); // HR
                    svf.VrsOut("NAME1", printData._staffname); // 氏名
                    svf.VrsOut("BELONG", printData._sectionabbv); // 所属
                    svf.VrsOut("TIME", printData._kouzaTotal); // 時間数
                    svf.VrsOut("NAME2", printData._staffname); // 氏名
                }

                svf.VrsOut("GRPCDA", String.valueOf(cntGrpcd)); // グループコード
                svf.VrsOut("GRPCDB", String.valueOf(cntGrpcd)); // グループコード
                svf.VrsOut("GRPCDC", String.valueOf(cntGrpcd)); // グループコード
                svf.VrsOut("GRPCDD", String.valueOf(cntGrpcd)); // グループコード
                svf.VrsOut("GRPCDE", String.valueOf(cntGrpcd)); // グループコード

                // 月1～金7を印字
                int retsu = 0;
                for (int day = 2; day <= 6; day++) {
                    for (int period = 2; period <= 8; period++) {
                        retsu++;
                        svf.VrsOut("GRPCD" + String.valueOf(retsu), String.valueOf(cntGrpcd)); // グループコード

                        final String key2 = String.valueOf(day) + "-" + String.valueOf(period);
                        final PrintData2 printData2 = (PrintData2) printData._dayPeriodMap.get(key2);
                        if (printData2 != null) {
                            final int subLen = KNJ_EditEdit.getMS932ByteLength(printData2._chairname + printData2._smallcls);
                            final int facLen = KNJ_EditEdit.getMS932ByteLength(printData2._facilityabbv + "(" + printData2._trgtgrade + ")");
                            final String subField = subLen > 22 ? "5" : subLen > 18 ? "4" : subLen > 14 ? "3" : subLen > 10 ? "2" : "1";
                            final String facField = facLen > 22 ? "5" : facLen > 18 ? "4" : facLen > 14 ? "3" : facLen > 10 ? "2" : "1";
                            svf.VrsOutn("SUBCLASS_NAME" + subField, retsu, printData2._chairname + printData2._smallcls); // 上段　講座名称＋スモールクラス名称
                            svf.VrsOutn("FACILITY_NAME" + facField, retsu, printData2._facilityabbv + "(" + printData2._trgtgrade + ")"); // 下段　施設略称＋"("＋受講クラスの学年＋")"
                        }
                    }
                }

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf) {
        //明細部以外を印字
        final String title = _param._nendo + "　時間割　" + _param._termDateStr;
        svf.VrsOut("TITLE", title); // タイトル
        svf.VrsOut("PRINT_DATE", _param._ctrlDateStr); // 印刷日
    }

    private Map getPrintMap(final DB2UDB db2) {
        final Map rtnMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String hrNameabbv = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                final String staffcd = StringUtils.defaultString(rs.getString("STAFFCD"));
                final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                final String sectioncd = StringUtils.defaultString(rs.getString("SECTIONCD"));
                final String sectionabbv = StringUtils.defaultString(rs.getString("SECTIONABBV"));
                final String kouzaTotal = StringUtils.defaultString(rs.getString("KOUZA_TOTAL"));
                final String lineNo = StringUtils.defaultString(rs.getString("LINE_NO"));

                final String daycd = StringUtils.defaultString(rs.getString("DAYCD"));
                final String periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                final String chaircd = StringUtils.defaultString(rs.getString("CHAIRCD"));
                final String chairname = StringUtils.defaultString(rs.getString("CHAIRNAME"));
                final String smallcls = StringUtils.defaultString(rs.getString("SMALLCLS"));
                final String faccd = StringUtils.defaultString(rs.getString("FACCD"));
                final String facilityabbv = StringUtils.defaultString(rs.getString("FACILITYABBV"));
                final String trgtgrade = StringUtils.defaultString(rs.getString("TRGTGRADE"));

                final String key = grade + "-" + hrClass + "-" + staffcd + "-" + lineNo;
                final PrintData printData;
                if (rtnMap.containsKey(key)) {
                    printData = (PrintData) rtnMap.get(key);
                } else {
                    printData = new PrintData(grade, hrClass, hrNameabbv, staffcd, staffname, sectioncd, sectionabbv, kouzaTotal, lineNo);
                }
                rtnMap.put(key, printData);

                final String key2 = daycd + "-" + periodcd;
                final PrintData2 printData2 = new PrintData2(daycd, periodcd, chaircd, chairname, smallcls, faccd, facilityabbv, trgtgrade);
                printData._dayPeriodMap.put(key2, printData2);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        //（資料記述なし）出力対象者と時間割を結合する参照テーブル（SCH_STF_DAT、CHAIR_STF_DAT）
        stb.append(" WITH SCH_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         DAYOFWEEK(T1.EXECUTEDATE) AS DAYCD, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T4.CHAIRNAME, ");
        stb.append("         T4.CHAIRABBV, ");
        stb.append("         T5.REMARK2 AS SMALLCLS, ");
        stb.append("         CASE WHEN T2.STAFFCD IS NOT NULL THEN T2.STAFFCD ELSE T3.STAFFCD END AS STAFFCD, ");
        stb.append("         CASE WHEN T6.FACCD IS NOT NULL THEN T6.FACCD ELSE T7.FACCD END AS FACCD, ");
        stb.append("         T8.TRGTGRADE ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_DAT T1 ");
        stb.append("         LEFT JOIN SCH_STF_DAT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN CHAIR_STF_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN CHAIR_DAT T4 ON T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER AND T4.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN CHAIR_DETAIL_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER AND T5.CHAIRCD = T1.CHAIRCD AND T5.SEQ = '004' ");
        stb.append("         LEFT JOIN SCH_FAC_DAT T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE AND T6.PERIODCD = T1.PERIODCD AND T6.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN CHAIR_FAC_DAT T7 ON T7.YEAR = T1.YEAR AND T7.SEMESTER = T1.SEMESTER AND T7.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN CHAIR_CLS_DAT T8 ON T8.YEAR = T1.YEAR AND T8.SEMESTER = T1.SEMESTER AND T8.GROUPCD = T4.GROUPCD AND (T8.CHAIRCD = T4.CHAIRCD OR T8.CHAIRCD = '0000000') ");
        stb.append("     WHERE ");
        stb.append("         T1.EXECUTEDATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
        stb.append("         AND T1.PERIODCD BETWEEN '2' AND '8' ");
        stb.append("         AND T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND (T2.STAFFCD IS NOT NULL OR T3.STAFFCD IS NOT NULL) ");
        stb.append(" ) ");
        stb.append(" , SCH_MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.STAFFCD, ");
        stb.append("         T1.DAYCD, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CHAIRNAME, ");
        stb.append("         T1.SMALLCLS, ");
        stb.append("         MIN(T1.FACCD) AS FACCD, ");
        stb.append("         MIN(T1.TRGTGRADE) AS TRGTGRADE ");
        stb.append("     FROM ");
        stb.append("         SCH_DATA T1 ");
        stb.append("         INNER JOIN V_STAFF_MST M1 ON M1.YEAR = T1.YEAR AND M1.STAFFCD = T1.STAFFCD ");
        stb.append("     WHERE ");
        stb.append("         T1.DAYCD BETWEEN '2' AND '6' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.STAFFCD, ");
        stb.append("         T1.DAYCD, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CHAIRNAME, ");
        stb.append("         T1.SMALLCLS ");
        stb.append(" ) ");
        stb.append(" , JIKANSUU AS ( ");
        stb.append("     SELECT ");
        stb.append("         STAFFCD, ");
        stb.append("         SUM(CNT) AS KOUZA_TOTAL ");
        stb.append("     FROM ");
        stb.append("         (SELECT ");
        stb.append("             STAFFCD, ");
        stb.append("             DAYCD, ");
        stb.append("             PERIODCD, ");
        stb.append("             COUNT(CHAIRCD) AS CNT ");
        stb.append("         FROM ");
        stb.append("             SCH_MAIN ");
        stb.append("         GROUP BY ");
        stb.append("             STAFFCD, ");
        stb.append("             DAYCD, ");
        stb.append("             PERIODCD) ");
        stb.append("     GROUP BY ");
        stb.append("         STAFFCD ");
        stb.append(" ) ");
        stb.append(" , REGD_H AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.HR_NAMEABBV, ");
        stb.append("         T1.TR_CD1 AS STAFFCD ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT T1 ");
        stb.append("         INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR AND T2.STAFFCD = T1.TR_CD1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ) ");
        //クラス担任（正担任1）
        //クラス担任（正担任1）以外
        stb.append(" , STAFF_LIST AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         HR_NAMEABBV, ");
        stb.append("         STAFFCD ");
        stb.append("     FROM ");
        stb.append("         REGD_H ");
        stb.append("     WHERE ");
        stb.append("         STAFFCD IN (SELECT STAFFCD FROM SCH_MAIN GROUP BY STAFFCD) ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         NULL AS GRADE, ");
        stb.append("         NULL AS HR_CLASS, ");
        stb.append("         NULL AS HR_NAMEABBV, ");
        stb.append("         STAFFCD ");
        stb.append("     FROM ");
        stb.append("         SCH_MAIN ");
        stb.append("     WHERE ");
        stb.append("         STAFFCD NOT IN (SELECT STAFFCD FROM REGD_H GROUP BY STAFFCD) ");
        stb.append("     GROUP BY ");
        stb.append("         STAFFCD ");
        stb.append(" ) ");
        //部長（校長）
        stb.append(" , PRINCIPAL AS ( ");
        stb.append("     SELECT ");
        stb.append("         STAFFCD ");
        stb.append("     FROM ");
        stb.append("         STAFF_PRINCIPAL_HIST_DAT ");
        stb.append("     WHERE ");
        stb.append("         '" + _param._tergetDate + "' BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-03-31') ");
        stb.append("     GROUP BY ");
        stb.append("         STAFFCD ");
        stb.append(" ) ");
        //メイン
        stb.append(" SELECT ");
        stb.append("     T0.GRADE, ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     CASE WHEN P1.STAFFCD IS NOT NULL THEN '部長' ELSE T0.HR_NAMEABBV END AS HR_NAMEABBV, ");
        stb.append("     T0.STAFFCD, ");
        stb.append("     T2.STAFFNAME, ");
        stb.append("     S3.FIELD1 AS SECTIONCD, ");
        stb.append("     S4.SECTIONABBV, ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.SMALLCLS, ");
        stb.append("     T1.FACCD, ");
        stb.append("     F1.FACILITYABBV, ");
        stb.append("     smallint(T1.TRGTGRADE) AS TRGTGRADE, ");
        //同一時限に複数講座が存在する場合は講座コード順に表示するために行番号（LINE_NO）を割り振る
        stb.append("     RANK() OVER(PARTITION BY T0.GRADE, T0.HR_CLASS, T1.STAFFCD, T1.DAYCD, T1.PERIODCD ORDER BY T1.CHAIRCD) AS LINE_NO, ");
        stb.append("     J1.KOUZA_TOTAL ");
        stb.append(" FROM ");
        stb.append("     STAFF_LIST T0 ");
        stb.append("     INNER JOIN SCH_MAIN T1 ON T1.STAFFCD = T0.STAFFCD ");
        stb.append("     INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR AND T2.STAFFCD = T1.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_DETAIL_MST S3 ON S3.YEAR = T1.YEAR AND S3.STAFFCD = T1.STAFFCD AND S3.STAFF_SEQ = '002' ");
        stb.append("     LEFT JOIN SECTION_MST S4 ON S4.SECTIONCD = S3.FIELD1 ");
        stb.append("     LEFT JOIN JIKANSUU J1 ON J1.STAFFCD = T1.STAFFCD ");
        stb.append("     LEFT JOIN V_FACILITY_MST F1 ON F1.YEAR = T1.YEAR AND F1.FACCD = T1.FACCD ");
        stb.append("     LEFT JOIN PRINCIPAL P1 ON P1.STAFFCD = T1.STAFFCD ");
        //クラス担任（正担任1）はクラス順
        //クラス担任（正担任1）以外は五十音順
        //同一時限に複数講座が存在する場合は講座コード順
        stb.append(" ORDER BY ");
        stb.append("     T0.GRADE, ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     T2.STAFFNAME_KANA, ");
        stb.append("     T0.STAFFCD, ");
        stb.append("     LINE_NO, ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD ");

        return stb.toString();
    }

    private class PrintData {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final String _staffcd;
        final String _staffname;
        final String _sectioncd;
        final String _sectionabbv;
        final String _kouzaTotal;
        final String _lineNo;
        final Map _dayPeriodMap;

        private PrintData(
                final String grade,
                final String hrClass,
                final String hrNameabbv,
                final String staffcd,
                final String staffname,
                final String sectioncd,
                final String sectionabbv,
                final String kouzaTotal,
                final String lineNo
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _staffcd = staffcd;
            _staffname = staffname;
            _sectioncd = sectioncd;
            _sectionabbv = sectionabbv;
            _kouzaTotal = kouzaTotal;
            _lineNo = lineNo;
            _dayPeriodMap = new HashMap();
        }
    }

    private class PrintData2 {
        final String _daycd;
        final String _periodcd;
        final String _chaircd;
        final String _chairname;
        final String _smallcls;
        final String _faccd;
        final String _facilityabbv;
        final String _trgtgrade;

        private PrintData2(
                final String daycd,
                final String periodcd,
                final String chaircd,
                final String chairname,
                final String smallcls,
                final String faccd,
                final String facilityabbv,
                final String trgtgrade
        ) {
            _daycd = daycd;
            _periodcd = periodcd;
            _chaircd = chaircd;
            _chairname = chairname;
            _smallcls = smallcls;
            _faccd = faccd;
            _facilityabbv = facilityabbv;
            _trgtgrade = trgtgrade;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75874 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _tergetDate;

        final String _nendo;
        final String _fromDate;
        final String _toDate;
        final String _ctrlDateStr;
        final String _termDateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _tergetDate = request.getParameter("TERGET_DATE").replace('/', '-');

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_ctrlYear)) + "年度";
            _fromDate = getFromDate();
            _toDate = getToDate();
            _ctrlDateStr = getCtrlDate(db2);
            _termDateStr = getTermDate();
        }

        private String getTermDate() {
            return KNJ_EditDate.h_format_JP_MD(_fromDate) + "(" + KNJ_EditDate.h_format_W(_fromDate) + ")" + " ～ " + KNJ_EditDate.h_format_JP_MD(_toDate) + "(" + KNJ_EditDate.h_format_W(_toDate) + ")";
        }

        private String getCtrlDate(final DB2UDB db2) {
            return KNJ_EditDate.h_format_JP(db2, _ctrlDate);
        }

        private String getFromDate() {
            final String[] date = _tergetDate.split("-");
            int year  = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day   = Integer.parseInt(date[2]);

            Calendar cals = Calendar.getInstance();
            cals.set(year, month - 1, day);

            //指定週の開始日(月曜日)を取得
            int addDay = 2 - cals.get(Calendar.DAY_OF_WEEK);
            if (addDay != 0) {
                cals.add(Calendar.DATE, addDay);
            }

            year  = cals.get(Calendar.YEAR);
            month = cals.get(Calendar.MONTH);
            month++;
            day   = cals.get(Calendar.DATE);

            return checkDate(String.valueOf(year) + "-" + maeZero(month) + "-" + maeZero(day));
        }

        private String getToDate() {
            final String[] date = _tergetDate.split("-");
            int year  = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day   = Integer.parseInt(date[2]);

            Calendar cals = Calendar.getInstance();
            cals.set(year, month - 1, day);

            //指定週の終了日(金曜日)を取得
            int addDay = 6 - cals.get(Calendar.DAY_OF_WEEK);
            if (addDay != 0) {
                cals.add(Calendar.DATE, addDay);
            }

            year  = cals.get(Calendar.YEAR);
            month = cals.get(Calendar.MONTH);
            month++;
            day   = cals.get(Calendar.DATE);

            return checkDate(String.valueOf(year) + "-" + maeZero(month) + "-" + maeZero(day));
        }

        private String checkDate(final String date) {
            final String sDate = _ctrlYear + "-04-01";
            final String eDate = String.valueOf(Integer.parseInt(_ctrlYear) + 1) + "-03-31";

            //指定週が年度をまたぐ場合
            Date d = Date.valueOf(date);
            Date s = Date.valueOf(sDate);
            Date e = Date.valueOf(eDate);
            if (d.compareTo(s) < 0) {
                return sDate;
            } else if (d.compareTo(e) > 0) {
                return eDate;
            } else {
                return date;
            }
        }

        private String maeZero(final int str) {
            String strx = "00" + String.valueOf(str);
            return strx.substring(strx.length() - 2);
        }
    }
}
// eof

/*
 * $Id: 87aecca447b1fc27006e2bf78608ed83aaf2cdc7 $
 *
 * 作成日: 2015/12/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;




import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF302 {

    private static final Log log = LogFactory.getLog(KNJF302.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJF302.frm", 4); // TODO: フォーム名
        printHead(svf);
        String befGrade = "";
        final List printList = getList(db2);
        for (Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            PrintData printData = (PrintData) itPrint.next();

            if (!"".equals(befGrade) && !befGrade.equals(printData._grade)) {
                final PrintData printDataTotal = getTotalData(printList, befGrade);
                printData(svf, printDataTotal, "_2");
                svf.VrEndRecord();
            }
            printData(svf, printData, "");
            befGrade = printData._grade;
            svf.VrEndRecord();
            _hasData = true;
        }
        if (!"".equals(befGrade)) {
            final PrintData printDataTotal = getTotalData(printList, befGrade);
            printData(svf, printDataTotal, "_2");
            svf.VrEndRecord();

            final PrintData printDataSougou = getTotalData(printList, "ALL");
            printData(svf, printDataSougou, "_2");
            svf.VrEndRecord();
        }
    }

    private void printHead(final Vrw32alp svf) {
        svf.VrsOut("TITLE",  nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　感染症発生状況");
        svf.VrsOut("TIMESTAMP",  "作成日：" + KNJ_EditDate.h_format_JP(_param._executeDate));
        svf.VrsOut("GRADE_TITLE",  "年組");

        svf.VrsOut("DISEASE_NAME1", "発熱");
        svf.VrsOut("DISEASE_NAME2", "頭痛");
        svf.VrsOut("DISEASE_NAME3", "急性呼吸器症状");
        svf.VrsOut("DISEASE_NAME4", "下痢・腹痛");
        svf.VrsOut("DISEASE_NAME5", "嘔吐・嘔気");
        svf.VrsOut("DISEASE_NAME6", "発疹");
        svf.VrsOut("DISEASE_NAME7", "インフルエンザ様症状");
        svf.VrsOut("DISEASE_NAME8", "その他");
        svf.VrsOut("DISEASE_NAME9", "インフルエンザ");
        svf.VrsOut("DISEASE_NAME10", "感染性胃腸炎");
        svf.VrsOut("DISEASE_NAME11", "溶連菌感染症");
        svf.VrsOut("DISEASE_NAME12", "おたふくかぜ");
        svf.VrsOut("DISEASE_NAME13", "水ぼうそう");
        svf.VrsOut("DISEASE_NAME14", "マイコプラズマ感染症");
        svf.VrsOut("DISEASE_NAME15", "伝染性紅斑");
        svf.VrsOut("DISEASE_NAME16", "手足口病");
        svf.VrsOut("DISEASE_NAME17", "咽頭結膜熱");
        svf.VrsOut("DISEASE_NAME18", "流行性角結膜炎");
        svf.VrsOut("DISEASE_NAME19", "その他");
        svf.VrsOut("TOTAL_NAME1",  "罹患出席者数");
        svf.VrsOut("TOTAL_NAME2",  "欠席者数");
        svf.VrsOut("TOTAL_NAME3",  "出席停止者数");
        svf.VrsOut("TOTAL_NAME4",  "総患者数(人数)");
        svf.VrsOut("TOTAL_NAME5",  "総患者数(割合)");
        svf.VrsOut("TOTAL_NAME6",  "総欠席者数(人数)");
        svf.VrsOut("TOTAL_NAME7",  "総欠席者数(割合)");
        svf.VrsOut("TOTAL_NAME8",  "在籍者数");
    }

    private void printData(final Vrw32alp svf, final PrintData printData, final String soeji) {
        svf.VrsOut("HR_NAME" + soeji,  printData._hrName);
        svf.VrsOut("DISEASE1" + soeji,  printData._absence01);
        svf.VrsOut("DISEASE2" + soeji,  printData._absence02);
        svf.VrsOut("DISEASE3" + soeji,  printData._absence03);
        svf.VrsOut("DISEASE4" + soeji,  printData._absence04);
        svf.VrsOut("DISEASE5" + soeji,  printData._absence05);
        svf.VrsOut("DISEASE6" + soeji,  printData._absence06);
        svf.VrsOut("DISEASE7" + soeji,  printData._absence07);
        svf.VrsOut("DISEASE8" + soeji,  printData._absence08);
        svf.VrsOut("DISEASE9" + soeji,  printData._attendsuspend01);
        svf.VrsOut("DISEASE10" + soeji, printData._attendsuspend02);
        svf.VrsOut("DISEASE11" + soeji, printData._attendsuspend03);
        svf.VrsOut("DISEASE12" + soeji, printData._attendsuspend04);
        svf.VrsOut("DISEASE13" + soeji, printData._attendsuspend05);
        svf.VrsOut("DISEASE14" + soeji, printData._attendsuspend06);
        svf.VrsOut("DISEASE15" + soeji, printData._attendsuspend07);
        svf.VrsOut("DISEASE16" + soeji, printData._attendsuspend08);
        svf.VrsOut("DISEASE17" + soeji, printData._attendsuspend09);
        svf.VrsOut("DISEASE18" + soeji, printData._attendsuspend10);
        svf.VrsOut("DISEASE19" + soeji, printData._attendsuspend11);
        svf.VrsOut("TOTAL1" + soeji,    printData._totalsum01);
        svf.VrsOut("TOTAL2" + soeji,    printData._totalsum02);
        svf.VrsOut("TOTAL3" + soeji,    printData._totalsum03);
        svf.VrsOut("TOTAL4" + soeji,    printData._totalsum04);
        svf.VrsOut("TOTAL5" + soeji,    printData._totalsum04Percent);
        svf.VrsOut("TOTAL6" + soeji,    printData._totalsum05);
        svf.VrsOut("TOTAL7" + soeji,    printData._totalsum05Percent);
        svf.VrsOut("TOTAL8" + soeji,    printData._totalsum06);
    }

    private PrintData getTotalData(final List printList, final String grade) {
        int absence01 = 0;
        int absence02 = 0;
        int absence03 = 0;
        int absence04 = 0;
        int absence05 = 0;
        int absence06 = 0;
        int absence07 = 0;
        int absence08 = 0;
        int attendsuspend01 = 0;
        int attendsuspend02 = 0;
        int attendsuspend03 = 0;
        int attendsuspend04 = 0;
        int attendsuspend05 = 0;
        int attendsuspend06 = 0;
        int attendsuspend07 = 0;
        int attendsuspend08 = 0;
        int attendsuspend09 = 0;
        int attendsuspend10 = 0;
        int attendsuspend11 = 0;
        int totalsum01 = 0;
        int totalsum02 = 0;
        int totalsum03 = 0;
        int totalsum04 = 0;
        double totalsum04Percent = 0;
        int totalsum05 = 0;
        double totalsum05Percent = 0;
        int totalsum06 = 0;
        for (Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            PrintData printData = (PrintData) itPrint.next();
            if (grade.equals(printData._grade) || "ALL".equals(grade)) {
                absence01           += Integer.parseInt(null != printData._absence01 ?          printData._absence01 : "0");
                absence02           += Integer.parseInt(null != printData._absence02 ?          printData._absence02 : "0");
                absence03           += Integer.parseInt(null != printData._absence03 ?          printData._absence03 : "0");
                absence04           += Integer.parseInt(null != printData._absence04 ?          printData._absence04 : "0");
                absence05           += Integer.parseInt(null != printData._absence05 ?          printData._absence05 : "0");
                absence06           += Integer.parseInt(null != printData._absence06 ?          printData._absence06 : "0");
                absence07           += Integer.parseInt(null != printData._absence07 ?          printData._absence07 : "0");
                absence08           += Integer.parseInt(null != printData._absence08 ?          printData._absence08 : "0");
                attendsuspend01     += Integer.parseInt(null != printData._attendsuspend01 ?    printData._attendsuspend01 : "0");
                attendsuspend02     += Integer.parseInt(null != printData._attendsuspend02 ?    printData._attendsuspend02 : "0");
                attendsuspend03     += Integer.parseInt(null != printData._attendsuspend03 ?    printData._attendsuspend03 : "0");
                attendsuspend04     += Integer.parseInt(null != printData._attendsuspend04 ?    printData._attendsuspend04 : "0");
                attendsuspend05     += Integer.parseInt(null != printData._attendsuspend05 ?    printData._attendsuspend05 : "0");
                attendsuspend06     += Integer.parseInt(null != printData._attendsuspend06 ?    printData._attendsuspend06 : "0");
                attendsuspend07     += Integer.parseInt(null != printData._attendsuspend07 ?    printData._attendsuspend07 : "0");
                attendsuspend08     += Integer.parseInt(null != printData._attendsuspend08 ?    printData._attendsuspend08 : "0");
                attendsuspend09     += Integer.parseInt(null != printData._attendsuspend09 ?    printData._attendsuspend09 : "0");
                attendsuspend10     += Integer.parseInt(null != printData._attendsuspend10 ?    printData._attendsuspend10 : "0");
                attendsuspend11     += Integer.parseInt(null != printData._attendsuspend11 ?    printData._attendsuspend11 : "0");
                totalsum01          += Integer.parseInt(null != printData._totalsum01 ?         printData._totalsum01 : "0");
                totalsum02          += Integer.parseInt(null != printData._totalsum02 ?         printData._totalsum02 : "0");
                totalsum03          += Integer.parseInt(null != printData._totalsum03 ?         printData._totalsum03 : "0");
                totalsum04          += Integer.parseInt(null != printData._totalsum04 ?         printData._totalsum04 : "0");
                totalsum04Percent   += Double.parseDouble(null != printData._totalsum04Percent ?  printData._totalsum04Percent : "0.0");
                totalsum05          += Integer.parseInt(null != printData._totalsum05 ?         printData._totalsum05 : "0");
                totalsum05Percent   += Double.parseDouble(null != printData._totalsum05Percent ?  printData._totalsum05Percent : "0.0");
                totalsum06          += Integer.parseInt(null != printData._totalsum06 ?         printData._totalsum06 : "0");
            }
        }
        final String setGrade = "ALL".equals(grade) ? "総合計" : String.valueOf(Integer.parseInt(grade) + "年　合計");
        final PrintData printData = new PrintData(grade, "", setGrade, String.valueOf(absence01), String.valueOf(absence02), String.valueOf(absence03), String.valueOf(absence04), String.valueOf(absence05), String.valueOf(absence06), String.valueOf(absence07), String.valueOf(absence08), String.valueOf(attendsuspend01), String.valueOf(attendsuspend02), String.valueOf(attendsuspend03), String.valueOf(attendsuspend04), String.valueOf(attendsuspend05), String.valueOf(attendsuspend06), String.valueOf(attendsuspend07), String.valueOf(attendsuspend08), String.valueOf(attendsuspend09), String.valueOf(attendsuspend10), String.valueOf(attendsuspend11), String.valueOf(totalsum01), String.valueOf(totalsum02), String.valueOf(totalsum03), String.valueOf(totalsum04), String.valueOf(totalsum04Percent), String.valueOf(totalsum05), String.valueOf(totalsum05Percent), String.valueOf(totalsum06));
        return printData;
    }
    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = dataSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade              = rs.getString("GRADE");
                final String hrClass            = rs.getString("HR_CLASS");
                final String hrName             = rs.getString("HR_NAME");
                final String absence01          = rs.getString("ABSENCE01");
                final String absence02          = rs.getString("ABSENCE02");
                final String absence03          = rs.getString("ABSENCE03");
                final String absence04          = rs.getString("ABSENCE04");
                final String absence05          = rs.getString("ABSENCE05");
                final String absence06          = rs.getString("ABSENCE06");
                final String absence07          = rs.getString("ABSENCE07");
                final String absence08          = rs.getString("ABSENCE08");
                final String attendsuspend01    = rs.getString("ATTENDSUSPEND01");
                final String attendsuspend02    = rs.getString("ATTENDSUSPEND02");
                final String attendsuspend03    = rs.getString("ATTENDSUSPEND03");
                final String attendsuspend04    = rs.getString("ATTENDSUSPEND04");
                final String attendsuspend05    = rs.getString("ATTENDSUSPEND05");
                final String attendsuspend06    = rs.getString("ATTENDSUSPEND06");
                final String attendsuspend07    = rs.getString("ATTENDSUSPEND07");
                final String attendsuspend08    = rs.getString("ATTENDSUSPEND08");
                final String attendsuspend09    = rs.getString("ATTENDSUSPEND09");
                final String attendsuspend10    = rs.getString("ATTENDSUSPEND10");
                final String attendsuspend11    = rs.getString("ATTENDSUSPEND11");
                final String totalsum01         = rs.getString("TOTALSUM01");
                final String totalsum02         = rs.getString("TOTALSUM02");
                final String totalsum03         = rs.getString("TOTALSUM03");
                final String totalsum04         = rs.getString("TOTALSUM04");
                final String totalsum04Percent  = rs.getString("TOTALSUM04_PERCENT");
                final String totalsum05         = rs.getString("TOTALSUM05");
                final String totalsum05Percent  = rs.getString("TOTALSUM05_PERCENT");
                final String totalsum06         = rs.getString("TOTALSUM06");
                final PrintData printData = new PrintData(grade, hrClass, hrName, absence01, absence02, absence03, absence04, absence05, absence06, absence07, absence08, attendsuspend01, attendsuspend02, attendsuspend03, attendsuspend04, attendsuspend05, attendsuspend06, attendsuspend07, attendsuspend08, attendsuspend09, attendsuspend10, attendsuspend11, totalsum01, totalsum02, totalsum03, totalsum04, totalsum04Percent, totalsum05, totalsum05Percent, totalsum06);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String dataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_SCHREG (GRADE, HR_CLASS, SCHREGNO) AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ");
            stb.append("         AND T1.GRADE = GDAT.GRADE ");
            stb.append("         AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append("     WHERE ");
        stb.append("             T1.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        if (!"".equals(_param._grade)) {
            stb.append("         AND T1.GRADE = '" + _param._grade + "' ");
        }
        stb.append("         AND NOT EXISTS( ");
        stb.append("                        SELECT ");
        stb.append("                            'x' ");
        stb.append("                        FROM ");
        stb.append("                            SCHREG_TRANSFER_DAT E1 ");
        stb.append("                        WHERE ");
        stb.append("                                E1.SCHREGNO     = T1.SCHREGNO ");
        stb.append("                            AND E1.TRANSFERCD IN ('1', '2') ");
        stb.append("                            AND '" + _param._additionDate.replace('/', '-') + "' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
        stb.append("                       ) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , T_GRADE (GRADE, HR_CLASS, HR_NAME, TOTALSUM06) AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T3.HR_NAME, ");
        stb.append("         COUNT(*) ");
        stb.append("     FROM ");
        stb.append("         T_SCHREG T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("             ON  T3.YEAR     = '" + _param._year + "' ");
        stb.append("             AND T3.SEMESTER = '" + _param._semester + "' ");
        stb.append("             AND T3.GRADE    = T1.GRADE ");
        stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     GROUP BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T3.HR_NAME ");
        stb.append("     ) ");
        stb.append(" , T_ADDITION1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         MEDEXAM_DISEASE_ADDITION2_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("         AND YEAR = '" + _param._year + "' ");
        stb.append("         AND ADDITION_DATE = '" + _param._additionDate.replace('/', '-') + "' ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     L1.ABSENCE01, ");
        stb.append("     L1.ABSENCE02, ");
        stb.append("     L1.ABSENCE03, ");
        stb.append("     L1.ABSENCE04, ");
        stb.append("     L1.ABSENCE05, ");
        stb.append("     L1.ABSENCE06, ");
        stb.append("     L1.ABSENCE07, ");
        stb.append("     L1.ABSENCE08, ");
        stb.append("     L1.ATTENDSUSPEND01, ");
        stb.append("     L1.ATTENDSUSPEND02, ");
        stb.append("     L1.ATTENDSUSPEND03, ");
        stb.append("     L1.ATTENDSUSPEND04, ");
        stb.append("     L1.ATTENDSUSPEND05, ");
        stb.append("     L1.ATTENDSUSPEND06, ");
        stb.append("     L1.ATTENDSUSPEND07, ");
        stb.append("     L1.ATTENDSUSPEND08, ");
        stb.append("     L1.ATTENDSUSPEND09, ");
        stb.append("     L1.ATTENDSUSPEND10, ");
        stb.append("     L1.ATTENDSUSPEND11, ");
        stb.append("     L1.TOTALSUM01, ");
        stb.append("     L1.TOTALSUM02, ");
        stb.append("     L1.TOTALSUM03, ");
        stb.append("     L1.TOTALSUM04, ");
        stb.append("     L1.TOTALSUM04_PERCENT, ");
        stb.append("     L1.TOTALSUM05, ");
        stb.append("     L1.TOTALSUM05_PERCENT, ");
        stb.append("     CASE WHEN L1.GRADE IS NOT NULL ");
        stb.append("          THEN L1.TOTALSUM06 ");
        stb.append("          ELSE T1.TOTALSUM06 ");
        stb.append("     END AS TOTALSUM06 ");
        stb.append(" FROM ");
        stb.append("     T_GRADE T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 L1 ON L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        return stb.toString();
    }

    private class PrintData {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _absence01;
        final String _absence02;
        final String _absence03;
        final String _absence04;
        final String _absence05;
        final String _absence06;
        final String _absence07;
        final String _absence08;
        final String _attendsuspend01;
        final String _attendsuspend02;
        final String _attendsuspend03;
        final String _attendsuspend04;
        final String _attendsuspend05;
        final String _attendsuspend06;
        final String _attendsuspend07;
        final String _attendsuspend08;
        final String _attendsuspend09;
        final String _attendsuspend10;
        final String _attendsuspend11;
        final String _totalsum01;
        final String _totalsum02;
        final String _totalsum03;
        final String _totalsum04;
        final String _totalsum04Percent;
        final String _totalsum05;
        final String _totalsum05Percent;
        final String _totalsum06;
        public PrintData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String absence01,
                final String absence02,
                final String absence03,
                final String absence04,
                final String absence05,
                final String absence06,
                final String absence07,
                final String absence08,
                final String attendsuspend01,
                final String attendsuspend02,
                final String attendsuspend03,
                final String attendsuspend04,
                final String attendsuspend05,
                final String attendsuspend06,
                final String attendsuspend07,
                final String attendsuspend08,
                final String attendsuspend09,
                final String attendsuspend10,
                final String attendsuspend11,
                final String totalsum01,
                final String totalsum02,
                final String totalsum03,
                final String totalsum04,
                final String totalsum04Percent,
                final String totalsum05,
                final String totalsum05Percent,
                final String totalsum06
        ) {
            _grade              = grade;
            _hrClass            = hrClass;
            _hrName             = hrName;
            _absence01          = null != absence01 ? absence01 : "0";
            _absence02          = null != absence02 ? absence02 : "0";
            _absence03          = null != absence03 ? absence03 : "0";
            _absence04          = null != absence04 ? absence04 : "0";
            _absence05          = null != absence05 ? absence05 : "0";
            _absence06          = null != absence06 ? absence06 : "0";
            _absence07          = null != absence07 ? absence07 : "0";
            _absence08          = null != absence08 ? absence08 : "0";
            _attendsuspend01    = null != attendsuspend01 ? attendsuspend01 : "0";
            _attendsuspend02    = null != attendsuspend02 ? attendsuspend02 : "0";
            _attendsuspend03    = null != attendsuspend03 ? attendsuspend03 : "0";
            _attendsuspend04    = null != attendsuspend04 ? attendsuspend04 : "0";
            _attendsuspend05    = null != attendsuspend05 ? attendsuspend05 : "0";
            _attendsuspend06    = null != attendsuspend06 ? attendsuspend06 : "0";
            _attendsuspend07    = null != attendsuspend07 ? attendsuspend07 : "0";
            _attendsuspend08    = null != attendsuspend08 ? attendsuspend08 : "0";
            _attendsuspend09    = null != attendsuspend09 ? attendsuspend09 : "0";
            _attendsuspend10    = null != attendsuspend10 ? attendsuspend10 : "0";
            _attendsuspend11    = null != attendsuspend11 ? attendsuspend11 : "0";
            _totalsum01         = null != totalsum01 ? totalsum01 : "0";
            _totalsum02         = null != totalsum02 ? totalsum02 : "0";
            _totalsum03         = null != totalsum03 ? totalsum03 : "0";
            _totalsum04         = null != totalsum04 ? totalsum04 : "0";
            _totalsum04Percent  = null != totalsum04Percent ? totalsum04Percent : "0";
            _totalsum05         = null != totalsum05 ? totalsum05 : "0";
            _totalsum05Percent  = null != totalsum05Percent ? totalsum05Percent : "0";
            _totalsum06         = null != totalsum06 ? totalsum06 : "0";
        }
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
        private final String _grade;
        private final String _additionDate;
        private final String _executeDate;
        private final String _report;
        private final String _schoolcd;
        final String _schoolKind;
        final String _useSchool_KindField;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _additionDate = request.getParameter("ADDITION_DATE");
            _executeDate = request.getParameter("EXECUTE_DATE");
            _report = request.getParameter("REPORT");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
        }

    }
}

// eof
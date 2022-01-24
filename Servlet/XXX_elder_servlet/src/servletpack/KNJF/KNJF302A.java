/*
 * $Id: 2c7f17396c566e0264c92b8a50dd3c45b9c60996 $
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

public class KNJF302A {

    private static final Log log = LogFactory.getLog(KNJF302A.class);

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
        printCover(svf);

        if ("1".equals(_param._dataDiv)) {
            svf.VrSetForm("KNJF302.frm", 4);
            printHead(svf);
            printOut(db2, svf, "_2", null, 0);
        } else {
            svf.VrSetForm("KNJF302A.frm", 4);
            printHead(svf);
            int frmGrpCd = 0;
            for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
                EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
                printOut(db2, svf, "", edboardSchool, frmGrpCd);
                frmGrpCd++;
            }
        }
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final String soeji, final EdboardSchool edboardSchool, final int frmGrpCd) {
        String befGrade = "";
        final List printList = getList(db2, edboardSchool);
        for (Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            PrintData printData = (PrintData) itPrint.next();

            printData(svf, printData, soeji, edboardSchool, frmGrpCd);
            befGrade = printData._grade;
            svf.VrEndRecord();
            _hasData = true;
        }
        if (!"".equals(befGrade)) {
            final PrintData printDataSougou = getTotalData(printList, "ALL");
            printData(svf, printDataSougou, soeji, edboardSchool, frmGrpCd);
            svf.VrEndRecord();
        }
    }

    private void printCover(final Vrw32alp svf) {
        svf.VrSetForm("KNJE_COVER.frm", 1);
        svf.VrsOut("TITLE",  nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　感染症発生状況");
        svf.VrsOut("DATE",  "作成日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate));
        int fieldName = 1;
        int hyousiGyo = 1;
        for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
            if (hyousiGyo > 30) {
                hyousiGyo = 1;
                fieldName++;
            }
            EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
            svf.VrsOutn("SCHOOL_NAME" + fieldName, hyousiGyo, edboardSchool._schoolName);
            svf.VrsOutn("DATE" + fieldName, hyousiGyo, edboardSchool._executeDate);
            hyousiGyo++;
        }
        svf.VrEndPage();
    }

    private void printHead(final Vrw32alp svf) {
        svf.VrsOut("TITLE",  nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　感染症発生状況");
        svf.VrsOut("TIMESTAMP",  "作成日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("GRADE_TITLE",  "学年");

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

    private void printData(final Vrw32alp svf, final PrintData printData, final String soeji, final EdboardSchool edboardSchool, final int frmGrpCd) {
        svf.VrsOut("GRP",  String.valueOf(frmGrpCd));
        if (null != edboardSchool) {
            final String schoolField = getMS932ByteLength(edboardSchool._schoolName) > 20 ? "SCHOOL_NAME1_2" : "SCHOOL_NAME1_1";
            svf.VrsOut(schoolField,  edboardSchool._schoolName);
        }
        svf.VrsOut("HR_NAME" + soeji,  printData._gradeName);
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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
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
        final PrintData printData = new PrintData(grade, setGrade, String.valueOf(absence01), String.valueOf(absence02), String.valueOf(absence03), String.valueOf(absence04), String.valueOf(absence05), String.valueOf(absence06), String.valueOf(absence07), String.valueOf(absence08), String.valueOf(attendsuspend01), String.valueOf(attendsuspend02), String.valueOf(attendsuspend03), String.valueOf(attendsuspend04), String.valueOf(attendsuspend05), String.valueOf(attendsuspend06), String.valueOf(attendsuspend07), String.valueOf(attendsuspend08), String.valueOf(attendsuspend09), String.valueOf(attendsuspend10), String.valueOf(attendsuspend11), String.valueOf(totalsum01), String.valueOf(totalsum02), String.valueOf(totalsum03), String.valueOf(totalsum04), String.valueOf(totalsum04Percent), String.valueOf(totalsum05), String.valueOf(totalsum05Percent), String.valueOf(totalsum06));
        return printData;
    }
    private List getList(final DB2UDB db2, final EdboardSchool edboardSchool) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = dataSql(edboardSchool);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade              = rs.getString("GRADE");
                final String gradeName          = String.valueOf(Integer.parseInt(grade)) + "年　合計";
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
                final PrintData printData = new PrintData(grade, gradeName, absence01, absence02, absence03, absence04, absence05, absence06, absence07, absence08, attendsuspend01, attendsuspend02, attendsuspend03, attendsuspend04, attendsuspend05, attendsuspend06, attendsuspend07, attendsuspend08, attendsuspend09, attendsuspend10, attendsuspend11, totalsum01, totalsum02, totalsum03, totalsum04, totalsum04Percent, totalsum05, totalsum05Percent, totalsum06);
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

    private String dataSql(final EdboardSchool edboardSchool) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_DATE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EDBOARD_SCHOOLCD, ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.FIXED_DATE ");
        stb.append("     FROM ");
        stb.append("         REPORT_DISEASE_ADDITION2_DAT T1, ");
        stb.append("         (SELECT ");
        stb.append("             EDBOARD_SCHOOLCD, ");
        stb.append("             YEAR, ");
        stb.append("             MAX(EXECUTE_DATE) AS EXECUTE_DATE ");
        stb.append("         FROM ");
        stb.append("             REPORT_DISEASE_ADDITION2_DAT ");
        stb.append("         WHERE ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("             EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") AND ");
        } else {
            stb.append("             EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' AND ");
        }
        stb.append("             YEAR = '" + _param._year + "' ");
        stb.append("         GROUP BY ");
        stb.append("             EDBOARD_SCHOOLCD, ");
        stb.append("             YEAR ");
        stb.append("         ) T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND ");
        stb.append("         T1.YEAR         = T2.YEAR AND ");
        stb.append("         T1.EXECUTE_DATE = T2.EXECUTE_DATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     SUM(T1.ABSENCE01) AS ABSENCE01, ");
        stb.append("     SUM(T1.ABSENCE02) AS ABSENCE02, ");
        stb.append("     SUM(T1.ABSENCE03) AS ABSENCE03, ");
        stb.append("     SUM(T1.ABSENCE04) AS ABSENCE04, ");
        stb.append("     SUM(T1.ABSENCE05) AS ABSENCE05, ");
        stb.append("     SUM(T1.ABSENCE06) AS ABSENCE06, ");
        stb.append("     SUM(T1.ABSENCE07) AS ABSENCE07, ");
        stb.append("     SUM(T1.ABSENCE08) AS ABSENCE08, ");
        stb.append("     SUM(T1.ATTENDSUSPEND01) AS ATTENDSUSPEND01, ");
        stb.append("     SUM(T1.ATTENDSUSPEND02) AS ATTENDSUSPEND02, ");
        stb.append("     SUM(T1.ATTENDSUSPEND03) AS ATTENDSUSPEND03, ");
        stb.append("     SUM(T1.ATTENDSUSPEND04) AS ATTENDSUSPEND04, ");
        stb.append("     SUM(T1.ATTENDSUSPEND05) AS ATTENDSUSPEND05, ");
        stb.append("     SUM(T1.ATTENDSUSPEND06) AS ATTENDSUSPEND06, ");
        stb.append("     SUM(T1.ATTENDSUSPEND07) AS ATTENDSUSPEND07, ");
        stb.append("     SUM(T1.ATTENDSUSPEND08) AS ATTENDSUSPEND08, ");
        stb.append("     SUM(T1.ATTENDSUSPEND09) AS ATTENDSUSPEND09, ");
        stb.append("     SUM(T1.ATTENDSUSPEND10) AS ATTENDSUSPEND10, ");
        stb.append("     SUM(T1.ATTENDSUSPEND11) AS ATTENDSUSPEND11, ");
        stb.append("     SUM(T1.TOTALSUM01) AS TOTALSUM01, ");
        stb.append("     SUM(T1.TOTALSUM02) AS TOTALSUM02, ");
        stb.append("     SUM(T1.TOTALSUM03) AS TOTALSUM03, ");
        stb.append("     SUM(T1.TOTALSUM04) AS TOTALSUM04, ");
        stb.append("     SUM(T1.TOTALSUM04_PERCENT) AS TOTALSUM04_PERCENT, ");
        stb.append("     SUM(T1.TOTALSUM05) AS TOTALSUM05, ");
        stb.append("     SUM(T1.TOTALSUM05_PERCENT) AS TOTALSUM05_PERCENT, ");
        stb.append("     SUM(T1.TOTALSUM06) AS TOTALSUM06 ");
        stb.append(" FROM ");
        stb.append("     MEDEXAM_DISEASE_ADDITION2_FIXED_DAT T1 ");
        stb.append("     INNER JOIN MAX_DATE T2 ");
        stb.append("          ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ");
        stb.append("         AND T1.YEAR         = T2.YEAR ");
        stb.append("         AND T1.FIXED_DATE   = T2.FIXED_DATE ");
        stb.append(" WHERE ");
        stb.append("     T1.EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GRADE NOT IN ('99') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.GRADE ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE ");
        return stb.toString();
    }

    private class PrintData {
        final String _grade;
        final String _gradeName;
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
                final String gradeName,
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
            _gradeName          = gradeName;
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _dataDiv;
        private final List _schoolList;
        private String _schoolInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            String schoolCd[] = request.getParameterValues("CATEGORY_SELECTED");
            _dataDiv = request.getParameter("DATA_DIV");
            _schoolList = getSchoolList(db2, schoolCd);
        }

        private List getSchoolList(final DB2UDB db2, final String[] schoolCd) throws SQLException {
            final List retList = new ArrayList();
            _schoolInState = "";
            String sep = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            for (int ia = 0 ; ia < schoolCd.length ; ia++) {
                final String setSchoolCd = schoolCd[ia].substring(1);
                final String schoolSql = getSchoolSql(setSchoolCd);
                try {
                    _schoolInState += sep + "'" + setSchoolCd + "'";
                    sep = ",";
                    ps = db2.prepareStatement(schoolSql);
                    rs = ps.executeQuery();
                    String schoolName = "";
                    String executeDate = "";
                    while (rs.next()) {
                        schoolName = rs.getString("EDBOARD_SCHOOLNAME");
                        executeDate = rs.getString("EXECUTE_DATE");
                    }
                    final EdboardSchool edboardSchool = new EdboardSchool(setSchoolCd, schoolName, executeDate);
                    retList.add(edboardSchool);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                _schoolInState = "".equals(_schoolInState) ? "''" : _schoolInState;
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLNAME, ");
            stb.append("     MAX(L1.EXECUTE_DATE) AS EXECUTE_DATE ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1 ");
            stb.append("     LEFT JOIN REPORT_DISEASE_ADDITION2_DAT L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ");
            stb.append("          AND L1.YEAR = '" + _year + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EDBOARD_SCHOOLNAME ");

            return stb.toString();
        }
    }

    private class EdboardSchool {
        private final String _schoolCd;
        private final String _schoolName;
        private final String _executeDate;
        public EdboardSchool(
            final String schoolCd,
            final String schoolName,
            final String executeDate
        ) {
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _executeDate = null != executeDate && !"".equals(executeDate) ? executeDate.replace('-', '/') : "";
        }
    }
}

// eof
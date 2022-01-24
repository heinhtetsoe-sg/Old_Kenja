/*
 * 作成日: 2021/04/15
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditDate.L007Class;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL692I {

    private static final Log log = LogFactory.getLog(KNJL692I.class);

    private boolean _hasData;

    private Param _param;

    private static final int APPLICATION_LINE_MAX = 25;
    private static final int APPLICATION_FINSCHOOL_LINE_MAX = 35;
    private static final String NORMAL_DIV = "1";
    private static final String TECHNICAL_DIV = "2";

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
        if ("1".equals(_param._output)) {
            printApplicationList(db2, svf);
        } else {
            printApplicationFinschoolList(db2, svf);
        }
    }

    private void printApplicationList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL692I_1.frm", 1);

        List<List<PrintData1>> applicationList = getApplicationList(db2);

        final int pageMaxCnt = applicationList.size();
        int pageCnt = 1;

        for(List<PrintData1> printList : applicationList) {
            int lineCnt = 1;
            svf.VrsOut("TITLE", "願書リスト");
            svf.VrsOut("PAGE", pageCnt + "/" + pageMaxCnt);
            svf.VrsOut("DATE", _param._ctrlDate);

            for(PrintData1 printData : printList) {
                svf.VrsOutn("EXAM_NO", lineCnt, printData._examno);

                final String name = StringUtils.defaultString(printData._name);
                final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                final String nameFieldName = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldName, lineCnt, name);

                final String nameKana = StringUtils.defaultString(printData._nameKana);
                final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(name);
                final String nameKanaFieldName = nameKanaByte > 30 ? "3" : nameKanaByte > 20 ? "2" : "1";
                svf.VrsOutn("KANA" + nameKanaFieldName, lineCnt, nameKana);

                svf.VrsOutn("SEX", lineCnt, printData._sex);
                svf.VrsOutn("HOPE_COURSE", lineCnt, printData._desireCourseCD);
                svf.VrsOutn("CONSUL_COURSE", lineCnt, printData._consulCourseCD);
                svf.VrsOutn("FINSCHOOL_CD", lineCnt, printData._fscd);
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, printData._finschoolName);
                svf.VrsOutn("COMMON_TEST", lineCnt, printData._commonTest);
                svf.VrsOutn("ABSENT", lineCnt, printData._absenceDays);
                svf.VrsOutn("SP_MARK", lineCnt, printData._spReason);

                final String remark = StringUtils.defaultString(printData._remark);
                final int remarkByte = KNJ_EditEdit.getMS932ByteLength(remark);
                final String remarFieldName = remarkByte > 100 ? "4" : remarkByte > 50 ? "3" : remarkByte > 36 ? "2" : "1";
                if ("1".equals(remarFieldName) || "2".equals(remarFieldName)) {
                    svf.VrsOutn("REMARK" + remarFieldName, lineCnt, remark);
                } else if ("3".equals(remarFieldName) || "4".equals(remarFieldName)) {
                    final int separateLength = "3".equals(remarFieldName) ? 50 : 100;
                    String[] remarkArray = KNJ_EditEdit.get_token(remark, separateLength, 2);
                    for(int i = 0; i < remarkArray.length; i++) {
                        svf.VrsOutn("REMARK" + remarFieldName + "_" + (i + 1), lineCnt, remarkArray[i]);
                    }
                }

                lineCnt++;
                _hasData = true;
            }

            pageCnt++;
            svf.VrEndPage();
        }
    }

    private void printApplicationFinschoolList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL692I_2.frm", 1);

        final Map<String, String> genralCourseMap = getConsulCourseMap(db2, NORMAL_DIV);
        final Map<String, String> technicalCourseMap = getConsulCourseMap(db2, TECHNICAL_DIV);

        List<List<PrintData2>> applicationFinschoolList = getApplicationFinschoolList(db2, genralCourseMap, technicalCourseMap);

        for(List<PrintData2> printList : applicationFinschoolList) {
            int lineCnt = 1;
            svf.VrsOut("TITLE", _param._warekiAbbv + "　" + _param._testDivName +  "　出願中学校リスト");
            svf.VrsOut("MAJOR_NAME1", "普通科");
            svf.VrsOut("MAJOR_NAME2", "工業科");

            int colCnt = 1;
            for (String generalName : genralCourseMap.values()) {
                svf.VrsOut("COURSE_NAME1_" + colCnt, generalName);
                colCnt++;
            }

            colCnt = 1;
            for (String technicalName : technicalCourseMap.values()) {
                svf.VrsOut("COURSE_NAME2_" + colCnt, technicalName);
                colCnt++;
            }

            for(PrintData2 printData : printList) {
                svf.VrsOutn("NO", lineCnt, printData._no);
                svf.VrsOutn("FINSCHOOL_CD", lineCnt, printData._fscd);
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, printData._finschoolName);

                colCnt = 1;
                for (String generalCD : genralCourseMap.keySet()) {
                    String generalCnt = printData._generalMap.get("GENERAL_COURSE" + generalCD);
                    svf.VrsOutn("NUM1_" + colCnt, lineCnt, generalCnt);
                    colCnt++;
                }

                colCnt = 1;
                for (String technicalCD : technicalCourseMap.keySet()) {
                    String technicalCnt = printData._technicalMap.get("TECHNICAL_COURSE" + technicalCD);
                    svf.VrsOutn("NUM2_" + colCnt, lineCnt, technicalCnt);
                    colCnt++;
                }

                svf.VrsOutn("TOTAL", lineCnt, printData._total);

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    private Map<String, String> getConsulCourseMap(final DB2UDB db2, final String gakka) {
        final Map<String, String> consuCourseMap = new LinkedHashMap<String, String>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getConsulCourseSql(gakka);
        log.debug(" consul course(" + gakka + ") =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String cd = rs.getString("GENERAL_CD");
                final String name = rs.getString("GENERAL_NAME");

                consuCourseMap.put(cd, name);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return consuCourseMap;
    }

    private List<List<PrintData1>> getApplicationList(final DB2UDB db2) {
        final List<List<PrintData1>> applicationList = new ArrayList<List<PrintData1>>();
        List<PrintData1> printList = new ArrayList<PrintData1>();
        PrintData1 printData = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getApplicantListSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String desireCourseCD = rs.getString("DESIRE_COURSE_CD");
                final String consulCourseCD = rs.getString("CONSUL_COURSE_CD");
                final String fscd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String commonTest = rs.getString("COMMON_TEST");
                final String absenceDays = rs.getString("ABSENCE_DAYS");
                final String spReason = rs.getString("SP_REASON");
                final String remark = rs.getString("REMARK");

                if (applicationList.size() == 0) {
                    applicationList.add(printList);
                }

                if (APPLICATION_LINE_MAX <= printList.size()) {
                    printList = new ArrayList<PrintData1>();
                    applicationList.add(printList);
                }

                printData = new PrintData1(examno, name, nameKana, sex, desireCourseCD, consulCourseCD, fscd, finschoolName, commonTest, absenceDays, spReason, remark);
                printList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return applicationList;
    }

    private List<List<PrintData2>> getApplicationFinschoolList(final DB2UDB db2, final Map<String, String> generalCourseMap, final Map<String, String> technicalCourseMap) {
        final List<List<PrintData2>> applicationFinschoolList = new ArrayList<List<PrintData2>>();
        List<PrintData2> printList = new ArrayList<PrintData2>();
        PrintData2 printData = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getApplicantFinschoolListSql(generalCourseMap, technicalCourseMap);
        log.debug(" applicant finschool list sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String fscd = rs.getString("FS_CD");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String total = rs.getString("TOTAL");

                if (applicationFinschoolList.size() == 0) {
                    applicationFinschoolList.add(printList);
                }

                if (APPLICATION_FINSCHOOL_LINE_MAX <= printList.size()) {
                    printList = new ArrayList<PrintData2>();
                    applicationFinschoolList.add(printList);
                }

                printData = new PrintData2(no, fscd, finschoolNameAbbv, total);
                for (String generalCD : generalCourseMap.keySet()) {
                    final String generalKey = "GENERAL_COURSE" + generalCD;
                    final String generalCnt = rs.getString(generalKey);
                    printData._generalMap.put(generalKey, generalCnt);
                }
                for (String technicalCD : technicalCourseMap.keySet()) {
                    final String technicalKey = "TECHNICAL_COURSE" + technicalCD;
                    final String technicalCnt = rs.getString(technicalKey);
                    printData._technicalMap.put(technicalKey, technicalCnt);
                }
                printList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return applicationFinschoolList;
    }

    private String getConsulCourseSql(final String gakka) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     G2.GENERAL_CD, ");
        stb.append("     G2.GENERAL_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_GENERAL_MST G1 ");
        stb.append("     INNER JOIN ENTEXAM_GENERAL_MST G2 ");
        stb.append("             ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ");
        stb.append("            AND G2.APPLICANTDIV = G1.APPLICANTDIV ");
        stb.append("            AND G2.GENERAL_DIV  = '02' ");
        stb.append("            AND G2.TESTDIV      = G1.TESTDIV ");
        stb.append("            AND G2.REMARK1      = G1.GENERAL_CD ");
        stb.append(" WHERE ");
        stb.append("     G1.ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("     AND G1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND G1.GENERAL_DIV  = '01' ");
        stb.append("     AND G1.TESTDIV      = '0' "); //固定
        stb.append("     AND G1.REMARK1      = '" + gakka + "' ");
        stb.append(" ORDER BY ");
        stb.append("     G2.GENERAL_CD ");
        return stb.toString();
    }

    private String getApplicantListSql() {
        final StringBuffer stb = new StringBuffer();
        // 指示画面の類別コースで絞込むためのテーブル
        stb.append(" WITH GENERAL_D2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         G2.ENTEXAMYEAR, ");
        stb.append("         G2.GENERAL_CD, ");
        stb.append("         G2.GENERAL_NAME, ");
        stb.append("         G2.REMARK1 AS RUIBETSU_COURSE, ");
        stb.append("         G1.REMARK1 AS GAKKA ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_GENERAL_MST G1 ");
        stb.append("         INNER JOIN ENTEXAM_GENERAL_MST G2 ");
        stb.append("                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ");
        stb.append("                AND G2.APPLICANTDIV = G1.APPLICANTDIV ");
        stb.append("                AND G2.TESTDIV      = G1.TESTDIV ");
        stb.append("                AND G2.GENERAL_DIV  = '02' ");
        stb.append("                AND G2.REMARK1      = G1.GENERAL_CD ");
        stb.append("     WHERE ");
        stb.append("         G1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND G1.TESTDIV      = '0' ");
        stb.append("     AND G1.GENERAL_DIV  = '01' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     CASE WHEN BASE.SEX = '1' THEN 'M' WHEN BASE.SEX = '2' THEN 'F' ELSE '' END AS SEX, ");
        stb.append("     DESIRE.GENERAL_CD AS DESIRE_COURSE_CD, ");
        stb.append("     DESIRE.GENERAL_NAME AS DESIRE_COURSE_NAME, ");
        stb.append("     CONSUL.GENERAL_CD AS CONSUL_COURSE_CD, ");
        stb.append("     CONSUL.GENERAL_NAME AS CONSUL_COURSE_NAME, ");
        stb.append("     ADV.FS_CD, ");
        stb.append("     FIN_MST.FINSCHOOL_NAME, ");
        stb.append("     ADV_D3.REMARK1 AS COMMON_TEST, ");
        stb.append("     ADV_D8.REMARK1 AS ABSENCE_DAYS, ");
        stb.append("     GENE05.GENERAL_NAME AS SP_REASON, ");
        stb.append("     ADV_D6.REMARK1 AS REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DAT ADV ");
        stb.append("            ON ADV.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND ADV.EXAMNO       = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D31 ");
        stb.append("            ON BASE_D31.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D31.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BASE_D31.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BASE_D31.SEQ          = '031' ");
        stb.append("     LEFT JOIN GENERAL_D2 DESIRE ");
        stb.append("            ON DESIRE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ");
        stb.append("           AND DESIRE.GENERAL_CD   = BASE_D31.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D1 ");
        stb.append("            ON ADV_D1.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D1.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D1.SEQ         = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D2 ");
        stb.append("            ON ADV_D2.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D2.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D2.SEQ         = '002' ");
        stb.append("     LEFT JOIN GENERAL_D2 CONSUL ");
        stb.append("            ON CONSUL.ENTEXAMYEAR  = ADV_D2.ENTEXAMYEAR ");
        stb.append("           AND CONSUL.GENERAL_CD   = ADV_D2.REMARK1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_MST ");
        stb.append("            ON FIN_MST.FINSCHOOLCD = ADV.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D3 ");
        stb.append("            ON ADV_D3.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D3.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D3.SEQ         = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D8 ");
        stb.append("            ON ADV_D8.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D8.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D8.SEQ         = '008' ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D4 ");
        stb.append("            ON ADV_D4.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D4.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D4.SEQ         = '004' ");
        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST GENE05 ");
        stb.append("            ON GENE05.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ");
        stb.append("           AND GENE05.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("           AND GENE05.TESTDIV      = '0' ");
        stb.append("           AND GENE05.GENERAL_DIV  = '05' ");
        stb.append("           AND GENE05.GENERAL_CD   = ADV_D4.REMARK2 ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D6 ");
        stb.append("            ON ADV_D6.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D6.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D6.SEQ         = '006' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        if (!"ALL".equals(_param._gakka)) {
            stb.append("     AND CONSUL.GAKKA = '" + _param._gakka + "' ");
        }
        if (!"ALL".equals(_param._ruibetsu)) {
            stb.append("     AND CONSUL.RUIBETSU_COURSE = '" + _param._ruibetsu + "' ");
        }
        if (!"".equals(_param._examnoFrom)) {
            if (!"".equals(_param._examnoTo)) {
                stb.append("     AND (ADV.EXAMNO BETWEEN '" + _param._examnoFrom + "' AND '" + _param._examnoTo + "') ");
            } else {
                stb.append("     AND ADV.EXAMNO >= '" + _param._examnoFrom + "' ");
            }
        } else {
            if (!"".equals(_param._examnoTo)) {
                stb.append("     AND ADV.EXAMNO <= '" + _param._examnoTo + "' ");
            }
        }
        // 3:全て 以外のときに条件に加える
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        if ("2".equals(_param._outputDiv)) {
            stb.append("     AND ADV_D4.REMARK2 = '01' "); // 01:学業
        } else if ("3".equals(_param._outputDiv)) {
            stb.append("     AND ADV_D4.REMARK2 <> '01' "); // 01:学業 以外
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._order)) {
            stb.append("     BASE.NAME, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private String getApplicantFinschoolListSql(final Map<String, String> generalCourseMap, final Map<String, String> technicalCourseMap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH GENE_COSE AS ( ");
        stb.append("     SELECT ");
        stb.append("         G2.ENTEXAMYEAR, ");
        stb.append("         G2.GENERAL_CD ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_GENERAL_MST G1 ");
        stb.append("         INNER JOIN ENTEXAM_GENERAL_MST G2 ");
        stb.append("                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ");
        stb.append("                AND G2.APPLICANTDIV = G1.APPLICANTDIV ");
        stb.append("                AND G2.TESTDIV      = G1.TESTDIV ");
        stb.append("                AND G2.GENERAL_DIV  = '02' ");
        stb.append("                AND G2.REMARK1      = G1.GENERAL_CD ");
        stb.append("     WHERE ");
        stb.append("         G1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND G1.TESTDIV      = '0' ");
        stb.append("     AND G1.GENERAL_DIV  = '01' ");
        stb.append("     AND G1.REMARK1      = '1' "); // 1:普通科
        stb.append(" ), ");
        stb.append(" TECH_COSE AS ( ");
        stb.append("     SELECT ");
        stb.append("         G2.ENTEXAMYEAR, ");
        stb.append("         G2.GENERAL_CD ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_GENERAL_MST G1 ");
        stb.append("         INNER JOIN ENTEXAM_GENERAL_MST G2 ");
        stb.append("                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ");
        stb.append("                AND G2.APPLICANTDIV = G1.APPLICANTDIV ");
        stb.append("                AND G2.TESTDIV      = G1.TESTDIV ");
        stb.append("                AND G2.GENERAL_DIV  = '02' ");
        stb.append("                AND G2.REMARK1      = G1.GENERAL_CD ");
        stb.append("     WHERE ");
        stb.append("         G1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND G1.TESTDIV      = '0' ");
        stb.append("     AND G1.GENERAL_DIV  = '01' ");
        stb.append("     AND G1.REMARK1      = '2' "); // 2:工業科
        stb.append(" ), ");
        // 指示画面の類別コースで絞込むためのテーブル
        stb.append(" GENERAL_D2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         G2.ENTEXAMYEAR, ");
        stb.append("         G2.GENERAL_CD, ");
        stb.append("         G2.REMARK1 AS RUIBETSU_COURSE, ");
        stb.append("         G1.REMARK1 AS GAKKA ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_GENERAL_MST G1 ");
        stb.append("         INNER JOIN ENTEXAM_GENERAL_MST G2 ");
        stb.append("                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ");
        stb.append("                AND G2.APPLICANTDIV = G1.APPLICANTDIV ");
        stb.append("                AND G2.TESTDIV      = G1.TESTDIV ");
        stb.append("                AND G2.GENERAL_DIV  = '02' ");
        stb.append("                AND G2.REMARK1      = G1.GENERAL_CD ");
        stb.append("     WHERE ");
        stb.append("         G1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND G1.TESTDIV      = '0' ");
        stb.append("     AND G1.GENERAL_DIV  = '01' ");
        stb.append(" ), ");
        stb.append(" MAIN AS ( ");
        stb.append(" SELECT ");
        for (String generalCD : generalCourseMap.keySet()) {
            stb.append("     SUM(CASE WHEN GENE_COSE.GENERAL_CD = '" + generalCD + "' THEN 1 ELSE 0 END) AS GENERAL_COURSE" + generalCD + ", ");
        }
        for (String technicalCD : technicalCourseMap.keySet()) {
            stb.append("     SUM(CASE WHEN TECH_COSE.GENERAL_CD = '" + technicalCD + "' THEN 1 ELSE 0 END) AS TECHNICAL_COURSE" + technicalCD + ", ");
        }
        stb.append("     ROW_NUMBER () OVER () AS NO, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FIN_MST.FINSCHOOL_NAME_ABBV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D31 ");
        stb.append("            ON BASE_D31.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D31.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BASE_D31.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BASE_D31.SEQ          = '031' ");
        stb.append("     LEFT JOIN GENE_COSE ");
        stb.append("            ON GENE_COSE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ");
        stb.append("           AND GENE_COSE.GENERAL_CD   = BASE_D31.REMARK1 ");
        stb.append("     LEFT JOIN TECH_COSE ");
        stb.append("            ON TECH_COSE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ");
        stb.append("           AND TECH_COSE.GENERAL_CD   = BASE_D31.REMARK1 ");
        stb.append("     LEFT JOIN GENERAL_D2 ");
        stb.append("            ON GENERAL_D2.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ");
        stb.append("           AND GENERAL_D2.GENERAL_CD   = BASE_D31.REMARK1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_MST ");
        stb.append("            ON FIN_MST.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        if (!"ALL".equals(_param._gakka)) {
            stb.append("     AND GENERAL_D2.GAKKA = '" + _param._gakka + "' ");
        }
        if (!"ALL".equals(_param._ruibetsu)) {
            stb.append("     AND GENERAL_D2.RUIBETSU_COURSE = '" + _param._ruibetsu + "' ");
        }
        if (!"".equals(_param._examnoFrom)) {
            if (!"".equals(_param._examnoTo)) {
                stb.append("     AND (BASE.EXAMNO BETWEEN '" + _param._examnoFrom + "' AND '" + _param._examnoTo + "') ");
            } else {
                stb.append("     AND BASE.EXAMNO >= '" + _param._examnoFrom + "' ");
            }
        } else {
            if (!"".equals(_param._examnoTo)) {
                stb.append("     AND BASE.EXAMNO <= '" + _param._examnoTo + "' ");
            }
        }
        // 3:全て 以外のときに条件に加える
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FIN_MST.FINSCHOOL_NAME_ABBV ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.FS_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        for (String generalCD : generalCourseMap.keySet()) {
            stb.append("     GENERAL_COURSE" + generalCD + ", ");
        }
        for (String technicalCD : technicalCourseMap.keySet()) {
            stb.append("     TECHNICAL_COURSE" + technicalCD + ", ");
        }
        String plusMark = "";
        for (String generalCD : generalCourseMap.keySet()) {
            stb.append(plusMark + "GENERAL_COURSE" + generalCD);
            plusMark = " + ";
        }
        for (String technicalCD : technicalCourseMap.keySet()) {
            stb.append(" + TECHNICAL_COURSE" + technicalCD);
        }
        stb.append("     AS TOTAL, ");
        stb.append("     NO, ");
        stb.append("     FS_CD, ");
        stb.append("     FINSCHOOL_NAME_ABBV ");
        stb.append(" FROM ");
        stb.append("     MAIN ");
        stb.append(" ORDER BY ");
        stb.append("     FS_CD ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData1 {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _desireCourseCD;
        final String _consulCourseCD;
        final String _fscd;
        final String _finschoolName;
        final String _commonTest;
        final String _absenceDays;
        final String _spReason;
        final String _remark;

        PrintData1(
            final String examno,
            final String name,
            final String nameKana,
            final String sex,
            final String desireCourseCD,
            final String consulCourseCD,
            final String fscd,
            final String finschoolName,
            final String commonTest,
            final String absenceDays,
            final String spReason,
            final String remark
        ) {
            _examno          = examno;
            _name            = name;
            _nameKana        = nameKana;
            _sex             = sex;
            _desireCourseCD  = desireCourseCD;
            _consulCourseCD  = consulCourseCD;
            _fscd            = fscd;
            _finschoolName   = finschoolName;
            _commonTest      = commonTest;
            _absenceDays     = absenceDays;
            _spReason        = spReason;
            _remark          = remark;
        }
    }

    private class PrintData2 {
        final String _no;
        final String _fscd;
        final String _finschoolName;
        final String _total;
        final Map<String, String> _generalMap;
        final Map<String, String> _technicalMap;

        PrintData2(
            final String no,
            final String fscd,
            final String finschoolName,
            final String total
        ) {
            _no              = no;
            _fscd            = fscd;
            _finschoolName   = finschoolName;
            _total           = total;
            _generalMap      = new LinkedHashMap<String, String>();
            _technicalMap    = new LinkedHashMap<String, String>();
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _gakka;
        private final String _testDiv;
        private final String _ruibetsu;
        private final String _examnoFrom;
        private final String _examnoTo;
        private final String _sex;
        private final String _order;
        private final String _output;
        private final String _outputDiv;
        private final String _testDivName;
        private final String _warekiAbbv;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _ctrlDate     = request.getParameter("CTRL_DATE").replace("-", "/");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _gakka        = request.getParameter("GAKKA");
            _testDiv      = request.getParameter("TESTDIV");
            _ruibetsu     = request.getParameter("RUIBETSU");
            _examnoFrom   = request.getParameter("EXAMNO_FROM");
            _examnoTo     = request.getParameter("EXAMNO_TO");
            _sex          = request.getParameter("SEX");
            _order        = request.getParameter("ORDER");
            _output       = request.getParameter("OUTPUT");
            _outputDiv    = request.getParameter("OUTPUT_DIV");
            _testDivName  = getTestDivName(db2);
            _warekiAbbv   = getWarekiAbbv(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV='" + _applicantDiv + "' AND TESTDIV='" + _testDiv + "' ";
            log.debug(" testdiv name sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String getWarekiAbbv(final DB2UDB db2) {
            L007Class l007 = KNJ_EditDate.getL007ofYear(db2, _entexamyear);
            String abbv = l007.getAbbv1();
            int year = l007.calcNen();
            String rtn =  abbv + String.format("%02d", year);

            return rtn;
        }
    }
}

// eof


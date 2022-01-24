/*
 * 作成日: 2021/04/09
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL678I {

    private static final Log log = LogFactory.getLog(KNJL678I.class);

    private boolean _hasData;

    private Param _param;

    private static final int LINE_MAX = 25;
    private static final String SCHOOL_WORK = "01";
    private static final Map<String, String> OUTPUT_MAP = new LinkedHashMap<String, String>();

    static {
        OUTPUT_MAP.put("1", "中学校別");
        OUTPUT_MAP.put("2", "地区別");
        OUTPUT_MAP.put("3", "特待学業");
        OUTPUT_MAP.put("4", "特待部活動");
        OUTPUT_MAP.put("5", "欠席");
    }

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
        svf.VrSetForm("KNJL678I.frm", 1);

        int lineCnt = 1; // 書き込み行数
        int pageCnt = 1;

        Map<String, List<List<PrintData>>> separaterMap = getSeparaterMap(db2);

        int totalPageCnt = 0;
        int totalCount = 0;
        final Map<String, Integer> countMap = new HashMap<String, Integer>();
        for(String separater : separaterMap.keySet()) {
            List<List<PrintData>> printDataLists = separaterMap.get(separater);
            totalPageCnt += printDataLists.size();

            int count = 0;
            for(List<PrintData> printDataList : printDataLists) {
                totalCount += printDataList.size();
                count += printDataList.size();
            }
            countMap.put(separater, count);
        }

        for(String separater : separaterMap.keySet()) {
            List<List<PrintData>> printDataLists = separaterMap.get(separater);

            for(List<PrintData> printDataList : printDataLists) {
                final String titleStr = StringUtils.defaultString(OUTPUT_MAP.get(_param._output));
                final int count = "3".equals(_param._output) ? totalPageCnt : countMap.get(separater);

                svf.VrsOut("TITLE", "中学生リスト（" + titleStr + "）");
                svf.VrsOut("PAGE", pageCnt + "/" + totalPageCnt);
                svf.VrsOut("COUNT", String.valueOf(count));
                svf.VrsOut("TOTAL_COUNT", String.valueOf(totalCount));
                svf.VrsOut("DATE", _param._ctrlDate);

                for(PrintData printData : printDataList) {
                    svf.VrsOutn("NO", lineCnt, printData._no);
                    svf.VrsOutn("DISTRICT_NAME", lineCnt, printData._districtName);
                    svf.VrsOutn("FINSCHOOL_NAME", lineCnt, printData._finschoolName);

                    final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData._name);
                    final String nameFieldName = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                    svf.VrsOutn("NAME" + nameFieldName, lineCnt, printData._name);

                    svf.VrsOutn("EXAM_DIV", lineCnt, printData._examDiv);

                    final int consulCourseByte = KNJ_EditEdit.getMS932ByteLength(printData._consulCourse);
                    final String consulCourseFieldName = consulCourseByte > 18 ? "3" : consulCourseByte > 12 ? "2" : "1";
                    svf.VrsOutn("CONSUL_COURSE" + consulCourseFieldName, lineCnt, printData._consulCourse);

                    svf.VrsOutn("COMMON_TEST", lineCnt, printData._commonTest);
                    svf.VrsOutn("SP_DIV", lineCnt, printData._spDiv);
                    svf.VrsOutn("SP_MARK", lineCnt, printData._spMark);

                    String[] changeLogArray = KNJ_EditEdit.get_token(printData._changeLog, 50, 5);
                    if (changeLogArray != null) {
                        for (int i = 0; i < changeLogArray.length; i++) {
                            svf.VrsOutn("CHANGE_LOG" + (i + 1), lineCnt, changeLogArray[i]);
                        }
                    }

                    String[] remarkArray = KNJ_EditEdit.get_token(printData._remark, 50, 4);
                    if (remarkArray != null) {
                        for (int i = 0; i < remarkArray.length; i++) {
                            svf.VrsOutn("REMARK" + (i + 1), lineCnt, remarkArray[i]);
                        }
                    }
                    svf.VrsOutn("EXCLUSION", lineCnt, printData._exclusion);

                    lineCnt++;
                    _hasData = true;
                }

                lineCnt = 1;
                pageCnt++;
                svf.VrEndPage();
            }
        }
    }

    private Map<String, List<List<PrintData>>> getSeparaterMap(final DB2UDB db2) {
        final Map<String, List<List<PrintData>>> separateMap = new LinkedHashMap<String, List<List<PrintData>>>();
        List<List<PrintData>> printLists = null;
        List<PrintData> printList = null;
        PrintData printData = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getApplicantListSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String separateCD = rs.getString("SEPARATE_CD");
                final String examno = rs.getString("EXAMNO");
                final String fsAreaName = rs.getString("FS_AREA_NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String name = rs.getString("NAME");
                final String testDivName = rs.getString("TESTDIV_NAME");
                final String consulCourse = rs.getString("CONSUL_COURSE");
                final String commonTest = rs.getString("COMMON_TEST");
                final String spCD = rs.getString("SP_CD");
                final String spReason = rs.getString("SP_REASON");
                final String changeText = rs.getString("CHANGE_TEXT");
                final String remark = rs.getString("REMARK");
                final String exclusion = rs.getString("EXCLUSION");

                if (separateMap.containsKey(separateCD)) {
                    printLists = separateMap.get(separateCD);
                    printList = printLists.get(printLists.size() - 1);
                } else {
                    printLists = new ArrayList<List<PrintData>>();
                    printList = new ArrayList<PrintData>();
                    printLists.add(printList);
                    separateMap.put(separateCD, printLists);
                }

                if (LINE_MAX <= printList.size()) {
                    printList = new ArrayList<PrintData>();
                    printLists.add(printList);
                }

                printData = new PrintData(examno, fsAreaName, finschoolName, name, testDivName, consulCourse, commonTest, spReason, spCD, changeText, remark, exclusion);
                printList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return separateMap;
    }

    private String getApplicantListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ADV_HIST_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         EXAMNO, ");
        stb.append("         MAX(CHANGE_DATE) AS CHANGE_DATE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_HIST_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         EXAMNO ");
        stb.append(" ), ");
        stb.append(" ADV_HIST AS ( ");
        stb.append("     SELECT ");
        stb.append("         HIST.ENTEXAMYEAR, ");
        stb.append("         HIST.EXAMNO, ");
        stb.append("         HIST.CHANGE_TEXT ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_HIST_DAT HIST ");
        stb.append("         INNER JOIN ADV_HIST_MAX ");
        stb.append("                 ON ADV_HIST_MAX.ENTEXAMYEAR = HIST.ENTEXAMYEAR ");
        stb.append("                AND ADV_HIST_MAX.EXAMNO      = HIST.EXAMNO ");
        stb.append("                AND ADV_HIST_MAX.CHANGE_DATE = HIST.CHANGE_DATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("1".equals(_param._output) || "3".equals(_param._output)) {
            // 1:中学校別 か 3:特待学業が選択された場合は、中学校を改頁制御用の項目として扱う
            stb.append("    ADV.FS_CD AS SEPARATE_CD, ");
        } else if ("2".equals(_param._output)) {
            // 2:地区別が選択された場合は、地区を改頁制御用の項目として扱う
            stb.append("    ADV.FS_AREA_CD AS SEPARATE_CD, ");
        } else if ("4".equals(_param._output)) {
            // 4:特待部活動が選択された場合は、特待理由を改頁制御用の項目として扱う
            stb.append("    VALUE(ADV_D4.REMARK1, '') AS SEPARATE_CD, ");
        } else if ("5".equals(_param._output)) {
            // 5:欠席が選択された場合は、改頁制御用の項目は無しとして扱う
            stb.append("    '' AS SEPARATE_CD, ");
        }
        stb.append("     ADV.EXAMNO, ");
        if ("3".equals(_param._output)) {
            stb.append("     VALUE(L001.NAME1, 'その他') AS FS_AREA_NAME, ");
        } else {
            stb.append("     VALUE(L001.NAME1, '') AS FS_AREA_NAME, ");
        }
        stb.append("     FIN_MST.FINSCHOOL_NAME, ");
        stb.append("     ADV.NAME, ");
        stb.append("     TESTDIV_T.TESTDIV_NAME, ");
        stb.append("     GENE02.GENERAL_NAME AS CONSUL_COURSE, ");
        stb.append("     ADV_D3.REMARK1 AS COMMON_TEST, ");
        stb.append("     GENE04.GENERAL_NAME AS SP_CD, ");
        stb.append("     GENE05.GENERAL_NAME AS SP_REASON, ");
        stb.append("     ADV_HIST.CHANGE_TEXT, ");
        stb.append("     ADV_D6.REMARK1 AS REMARK, ");
        stb.append("     CASE WHEN ADV.EXCLUSION = '1' THEN '除外' ELSE '' END AS EXCLUSION ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECRUIT_ADVICE_DAT ADV ");
        stb.append("     LEFT JOIN V_NAME_MST L001 ");
        stb.append("            ON L001.YEAR    = ADV.ENTEXAMYEAR ");
        stb.append("           AND L001.NAMECD1 = 'L001' ");
        stb.append("           AND L001.NAMECD2 = ADV.FS_AREA_CD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_MST ");
        stb.append("            ON FIN_MST.FINSCHOOLCD = ADV.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D1 ");
        stb.append("            ON ADV_D1.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D1.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D1.SEQ         = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST TESTDIV_T ");
        stb.append("           ON TESTDIV_T.ENTEXAMYEAR  = ADV_D1.ENTEXAMYEAR ");
        stb.append("          AND TESTDIV_T.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("          AND TESTDIV_T.TESTDIV      = ADV_D1.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D2 ");
        stb.append("            ON ADV_D2.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D2.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D2.SEQ         = '002' ");
        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST GENE02 ");
        stb.append("            ON GENE02.ENTEXAMYEAR  = ADV_D2.ENTEXAMYEAR ");
        stb.append("           AND GENE02.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("           AND GENE02.TESTDIV      = '0' ");
        stb.append("           AND GENE02.GENERAL_DIV  = '02' ");
        stb.append("           AND GENE02.GENERAL_CD   = ADV_D2.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D3 ");
        stb.append("            ON ADV_D3.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D3.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D3.SEQ         = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D4 ");
        stb.append("            ON ADV_D4.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D4.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D4.SEQ         = '004' ");
        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST GENE04 ");
        stb.append("            ON GENE04.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ");
        stb.append("           AND GENE04.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("           AND GENE04.TESTDIV      = '0' ");
        stb.append("           AND GENE04.GENERAL_DIV  = '04' ");
        stb.append("           AND GENE04.GENERAL_CD   = ADV_D4.REMARK2 ");
        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST GENE05 ");
        stb.append("            ON GENE05.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ");
        stb.append("           AND GENE05.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("           AND GENE05.TESTDIV      = '0' ");
        stb.append("           AND GENE05.GENERAL_DIV  = '05' ");
        stb.append("           AND GENE05.GENERAL_CD   = ADV_D4.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D6 ");
        stb.append("            ON ADV_D6.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D6.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D6.SEQ         = '006' ");
        stb.append("     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D8 ");
        stb.append("            ON ADV_D8.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_D8.EXAMNO      = ADV.EXAMNO ");
        stb.append("           AND ADV_D8.SEQ         = '008' ");
        stb.append("     LEFT JOIN ADV_HIST ");
        stb.append("            ON ADV_HIST.ENTEXAMYEAR = ADV.ENTEXAMYEAR ");
        stb.append("           AND ADV_HIST.EXAMNO      = ADV.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     ADV.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        if ("1".equals(_param._output) && !"ALL".equals(_param._finschoolCD)) {
            stb.append(" AND ADV.FS_CD = '" + _param._finschoolCD + "' ");
        } else  if ("2".equals(_param._output)) {
            if ("OTHER".equals(_param._fsAreaCD)) {
                stb.append(" AND ADV.FS_AREA_CD IS NULL ");
            } else if ("2".equals(_param._output) && !"ALL".equals(_param._fsAreaCD)) {
                stb.append(" AND ADV.FS_AREA_CD = '" + _param._fsAreaCD + "' ");
            }
        } else if ("3".equals(_param._output)) {
            stb.append(" AND ADV_D4.REMARK1 = '" + SCHOOL_WORK + "' "); // 01:学業
        } else if ("4".equals(_param._output)) {
            stb.append(" AND ADV_D4.REMARK1 <> '" + SCHOOL_WORK + "' "); // 01:学業 以外
        } else if ("5".equals(_param._output)) {
            stb.append(" AND ADV_D8.REMARK1 IS NOT NULL "); // 01:学業 以外
        }
        stb.append(" ORDER BY ");
        if ("4".equals(_param._output)) {
            stb.append("     VALUE(ADV_D4.REMARK1, 'ZZZ'), ");
        }
        stb.append("     VALUE(ADV.FS_AREA_CD, 'ZZZZZZZZZZZZZ'), ");
        stb.append("     VALUE(ADV.FS_CD, 'ZZZZZZZZZZZZZ'), ");
        if ("3".equals(_param._output) || "4".equals(_param._output)) {
            stb.append("     VALUE(ADV_D4.REMARK2, 'ZZ'), ");
        }
        stb.append("     ADV.NAME ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData {
        final String _no;
        final String _districtName;
        final String _finschoolName;
        final String _name;
        final String _examDiv;
        final String _consulCourse;
        final String _commonTest;
        final String _spDiv;
        final String _spMark;
        final String _changeLog;
        final String _remark;
        final String _exclusion;

        PrintData(
            final String no,
            final String districtName,
            final String finschoolName,
            final String name,
            final String examDiv,
            final String consulCourse,
            final String commonTest,
            final String spDiv,
            final String spMark,
            final String changeLog,
            final String remark,
            final String exclusion
        ) {
            _no            = no;
            _districtName  = districtName;
            _finschoolName = finschoolName;
            _name          = name;
            _examDiv       = examDiv;
            _consulCourse  = consulCourse;
            _commonTest    = commonTest;
            _spDiv         = spDiv;
            _spMark        = spMark;
            _changeLog     = changeLog;
            _remark        = remark;
            _exclusion     = exclusion;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _output;
        private final String _finschoolCD;
        private final String _fsAreaCD;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _ctrlDate     = request.getParameter("CTRL_DATE").replace("-", "/");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _output       = request.getParameter("OUTPUT");
            _finschoolCD  = request.getParameter("FINSCHOOLCD");
            _fsAreaCD     = request.getParameter("FS_AREA_CD");
        }
    }
}

// eof


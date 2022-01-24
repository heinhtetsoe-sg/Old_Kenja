/*
 * $Id: d78dd96b8b50b60afbec811b6c9d3a3c47b68e38 $
 *
 * 作成日: 2017/11/21
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL362W {

    private static final Log log = LogFactory.getLog(KNJL362W.class);

    private boolean _hasData;

    private final String SOUGOUKEI = "99999999";

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

            for (Iterator itSchool = _param._schoolList.iterator(); itSchool.hasNext();) {
                final SchoolData schoolData = (SchoolData) itSchool.next();
                if ("1".equals(_param._csvDiv)) {
                    final String testDiv = "1','2','3','4";
                    printMain(db2, svf, schoolData, testDiv, "前期選抜", "");
                    printMain(db2, svf, schoolData, testDiv, "前期選抜", "1");
                } else {
                    String testDiv = "5";
                    printMain(db2, svf, schoolData, testDiv, "後期選抜", "");
                    printMain(db2, svf, schoolData, testDiv, "後期選抜", "1");
                    testDiv = "6";
                    printMain(db2, svf, schoolData, testDiv, "再募集", "");
                }
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final SchoolData schoolData, final String testDiv, final String divName, final String testDiv2) {
        svf.VrSetForm("KNJL362W.frm", 4);
        final Map printMap = getMap(db2, schoolData, testDiv, testDiv2);
        int printLine = 1;

        setTitle(svf, schoolData, divName, testDiv2);//タイトル
        for (Iterator itKoumoku = _param._koumokuList.iterator(); itKoumoku.hasNext();) {
            final KoumokuClass koumokuClass = (KoumokuClass) itKoumoku.next();
            if ("9".equals(koumokuClass._setFieldCd)) {
                for (Iterator itCmc = printMap.keySet().iterator(); itCmc.hasNext();) {
                    final String cmcCd = (String) itCmc.next();
                    final Map cmcData = (Map) printMap.get(cmcCd);
                    if (!SOUGOUKEI.equals(cmcCd)) {
                        svf.VrsOutn("MAJOR_NAME1", printLine, (String) cmcData.get(koumokuClass._sqlField));//学科・コース名称
                    }
                    printLine++;
                }
            } else if ("1".equals(koumokuClass._setFieldCd)) {
                svf.VrsOut("NUM_NAME", koumokuClass._scoreName);//合計者数名称
                int colNo = 1;
                for (Iterator itCmc = printMap.keySet().iterator(); itCmc.hasNext();) {
                    final String cmcCd = (String) itCmc.next();
                    final Map cmcData = (Map) printMap.get(cmcCd);
                    if (!SOUGOUKEI.equals(cmcCd)) {
                        svf.VrsOut("NUM" + colNo, (String) cmcData.get(koumokuClass._sqlField));//人数
                    } else {
                        svf.VrsOut("NUM9", (String) cmcData.get(koumokuClass._sqlField));//人数（総合計）
                    }
                    colNo++;
                }
            } else {
                svf.VrsOut("GRP2", koumokuClass._setFieldCd);
                svf.VrsOut("SCORE_TITLE", koumokuClass._titleName);
                svf.VrsOut("CLASS_NAME", koumokuClass._subclassName);
                svf.VrsOut("SCORE_NAME", koumokuClass._scoreName);//得点名称
                int colNo = 1;
                for (Iterator itCmc = printMap.keySet().iterator(); itCmc.hasNext();) {
                    final String cmcCd = (String) itCmc.next();
                    final Map cmcData = (Map) printMap.get(cmcCd);
                    if (!SOUGOUKEI.equals(cmcCd)) {//得点（総合計以外）
                        if ("2".equals(koumokuClass._roundFlg) && !"".equals((String) cmcData.get(koumokuClass._sqlField))) {
                            final BigDecimal setVal = new BigDecimal((String) cmcData.get(koumokuClass._sqlField)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOut("SCORE" + colNo, String.valueOf(setVal));
                        } else {
                            svf.VrsOut("SCORE" + colNo, (String) cmcData.get(koumokuClass._sqlField));
                        }
                    } else {//総合計
                        if ("2".equals(koumokuClass._roundFlg) && !"".equals((String) cmcData.get(koumokuClass._sqlField))) {
                            final BigDecimal setVal = new BigDecimal((String) cmcData.get(koumokuClass._sqlField)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOut("SCORE9", String.valueOf(setVal));
                        } else {
                            svf.VrsOut("SCORE9", (String) cmcData.get(koumokuClass._sqlField));
                        }
                    }
                    colNo++;
                }
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf, final SchoolData schoolData, final String divName, final String testDiv2) {
        String setYear = KNJ_EditDate.h_format_JP_N(_param._entExamYear + "/04/01");
        svf.VrsOut("TITLE", setYear + "度　三重県立高等学校入学志願者学力検査結果集計表");
        final String setTsuikensa = "1".equals(testDiv2) ? "(追検査)" : "";
        svf.VrsOut("EXAM_NAME" , divName + setTsuikensa);
        svf.VrsOut("AREA", schoolData._distName);
        svf.VrsOut("SCHOOL_NAME", schoolData._schoolName);
        svf.VrsOut("COURSE", schoolData._courseName);
    }

    private Map getMap(final DB2UDB db2, final SchoolData schoolData, final String testDiv, final String testDiv2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schoolData._schoolCd, testDiv, testDiv2);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Map cmDataMap = new HashMap();
                for (Iterator itKoumoku = _param._koumokuList.iterator(); itKoumoku.hasNext();) {
                    final KoumokuClass koumokuClass = (KoumokuClass) itKoumoku.next();
                    cmDataMap.put(koumokuClass._sqlField, StringUtils.defaultString(rs.getString(koumokuClass._sqlField)));
                }
                retMap.put(rs.getString("CMCD"), cmDataMap);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private String getSql(final String schoolCd, final String testDiv, final String testDiv2) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH BASE_ALL(YEAR, CMCD, CMNAME) AS ( ");
        stb.append("                 VALUES('" + _param._entExamYear + "', '99999999', '総　合　計') ");
        stb.append(" ), BASE AS ( ");
        stb.append("                 SELECT DISTINCT ");
        stb.append("                     V1.ENTEXAMYEAR AS YEAR, ");
        stb.append("                     V1.COURSECD || V1.MAJORCD || V1.EXAMCOURSECD AS CMCD, ");
        stb.append("                     V2.MAJORNAME || '・' || V1.EXAMCOURSE_NAME AS CMNAME ");
        stb.append("                 FROM ");
        stb.append("                     EDBOARD_ENTEXAM_COURSE_MST V1 ");
        stb.append("                     INNER JOIN V_EDBOARD_COURSE_MAJOR_MST V2 ");
        stb.append("                         ON V1.EDBOARD_SCHOOLCD = V2.EDBOARD_SCHOOLCD ");
        stb.append("                        AND V1.ENTEXAMYEAR      = V2.YEAR ");
        stb.append("                        AND V1.COURSECD || V1.MAJORCD = V2.COURSECD || V2.MAJORCD ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR       = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV    IN ('" + testDiv + "') ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     * ");
        stb.append("                 FROM ");
        stb.append("                     BASE_ALL ");
        stb.append("                 ORDER BY ");
        stb.append("                     CMCD ");
        stb.append(" ), DAI1_INT AS ( ");//試験受験者数
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS DAI1_CMCD, ");
        stb.append("                     COUNT(*) AS DAI1_CM_INT ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS DAI1_CMCD, ");
        stb.append("                     COUNT(*) AS DAI1_CM_INT ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR ");
        stb.append(" ), SUC_INT AS ( ");//試験合格者数
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS SUC_CMCD, ");
        stb.append("                     COUNT(*) AS SUC_CM_INT ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND V1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS SUC_CMCD, ");
        stb.append("                     COUNT(*) AS SUC_CM_INT ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND V1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR ");
        stb.append(" ), SUB_99 AS ( ");
        stb.append("                  SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS CMCD, ");
        stb.append("                     SUM(E1.SCORE) AS TOTAL99 ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD  = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                     AND E1.TESTSUBCLASSCD   IN ('1', '2', '3', '4', '5') ");
        } else {
            stb.append("                     AND E1.TESTSUBCLASSCD   IN ('1', '3', '5') ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE, ");
        stb.append("                     V1.EXAMNO  ");
        stb.append(" ), SUC_SUB_99 AS ( ");
        stb.append("                  SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS CMCD, ");
        stb.append("                     SUM(E1.SCORE) AS SUC_TOTAL99 ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND V1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                     AND E1.TESTSUBCLASSCD   IN ('1', '2', '3', '4', '5') ");
        } else {
            stb.append("                     AND E1.TESTSUBCLASSCD   IN ('1', '3', '5') ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE, ");
        stb.append("                     V1.EXAMNO  ");
        stb.append(" ), DAI1_DATA AS ( ");//全教科（受験者総得点、総平均）
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.CMCD AS DAI1_CMCD, ");
        stb.append("                     SUM(V1.TOTAL99) AS DAI1_TOTAL_3, ");
        stb.append("                     DECIMAL(ROUND(AVG(V1.TOTAL99 * 1.0),1),4,1) AS DAI1_AVG_3 ");
        stb.append("                 FROM ");
        stb.append("                     SUB_99 V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.CMCD ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS DAI1_CMCD, ");
        stb.append("                     SUM(V1.TOTAL99) AS DAI1_TOTAL_3, ");
        stb.append("                     DECIMAL(ROUND(AVG(V1.TOTAL99 * 1.0),1),4,1) AS DAI1_AVG_3 ");
        stb.append("                 FROM ");
        stb.append("                     SUB_99 V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR ");
        stb.append(" ), SUC_DATA AS ( ");//全教科（合格者総得点、総平均）
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.CMCD AS SUC_CMCD, ");
        stb.append("                     SUM(V1.SUC_TOTAL99) AS SUC_TOTAL_3, ");
        stb.append("                     DECIMAL(ROUND(AVG(V1.SUC_TOTAL99 * 1.0),1),4,1) AS SUC_AVG_3 ");
        stb.append("                 FROM ");
        stb.append("                     SUC_SUB_99 V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.CMCD ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS SUC_CMCD, ");
        stb.append("                     SUM(V1.SUC_TOTAL99) AS SUC_TOTAL_3, ");
        stb.append("                     DECIMAL(ROUND(AVG(V1.SUC_TOTAL99 * 1.0),1),4,1) AS SUC_AVG_3 ");
        stb.append("                 FROM ");
        stb.append("                     SUC_SUB_99 V1 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR ");
        stb.append(" ), DAI1_SUB_DATA AS ( ");//各教科（受験者総得点、総平均）
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS DAI1_CMCD, ");
        stb.append("                     E1.TESTSUBCLASSCD, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ");
        }
        stb.append("                     END AS DAI1_TOTAL_SUB, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        }
        stb.append("                     END AS DAI1_AVG_SUB ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE, ");
        stb.append("                     E1.TESTSUBCLASSCD ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS DAI1_CMCD, ");
        stb.append("                     E1.TESTSUBCLASSCD, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ");
        }
        stb.append("                     END AS DAI1_TOTAL_SUB, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        }
        stb.append("                     END AS DAI1_AVG_SUB ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     E1.TESTSUBCLASSCD ");
        stb.append(" ), SUC_SUB_DATA AS ( ");//各教科（合格者総得点、総平均）
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS SUC_CMCD, ");
        stb.append("                     E1.TESTSUBCLASSCD, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ");
        }
        stb.append("                     END AS SUC_TOTAL_SUB, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        }
        stb.append("                     END AS SUC_AVG_SUB ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND V1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE, ");
        stb.append("                     E1.TESTSUBCLASSCD ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     '99999999' AS SUC_CMCD, ");
        stb.append("                     E1.TESTSUBCLASSCD, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ");
        }
        stb.append("                     END AS SUC_TOTAL_SUB, ");
        stb.append("                     CASE  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        stb.append("                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
            stb.append("                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ");
        }
        stb.append("                     END AS SUC_AVG_SUB ");
        stb.append("                 FROM ");
        stb.append("                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ");
        stb.append("                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ");
        stb.append("                                              AND V1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("                                              AND V1.TESTDIV     = E1.TESTDIV ");
        stb.append("                                              AND V1.EXAMNO      = E1.RECEPTNO ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND V1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         V1.EDBOARD_SCHOOLCD  = '" + schoolCd + "' ");
        stb.append("                     AND V1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND V1.TESTDIV     IN ('" + testDiv + "') ");
        stb.append("                     AND VALUE(V1.TESTDIV2, '') = '" + testDiv2 + "' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     V1.ENTEXAMYEAR, ");
        stb.append("                     E1.TESTSUBCLASSCD ");
        stb.append(" ) ");
        //メイン
        stb.append(" SELECT ");
        stb.append("     B1.CMCD, ");
        stb.append("     B1.CMNAME, ");
        stb.append("     VALUE(DINT.DAI1_CM_INT, 0) AS DAI1_CM_INT, ");
        stb.append("     VALUE(SINT.SUC_CM_INT, 0) AS SUC_CM_INT, ");
        stb.append("     VALUE(DD.DAI1_TOTAL_3, 0) AS DAI1_TOTAL_3, ");
        stb.append("     VALUE(DD.DAI1_AVG_3, 0) AS DAI1_AVG_3, ");
        stb.append("     VALUE(SD.SUC_TOTAL_3, 0) AS SUC_TOTAL_3, ");
        stb.append("     VALUE(SD.SUC_AVG_3, 0) AS SUC_AVG_3, ");
        stb.append("     VALUE(DSD1.DAI1_TOTAL_SUB, 0) AS JAP_D1_TOTAL, ");
        stb.append("     VALUE(DSD1.DAI1_AVG_SUB  , 0) AS JAP_D1_AVG, ");
        stb.append("     VALUE(SSD1.SUC_TOTAL_SUB , 0) AS JAP_SUC_TOTAL, ");
        stb.append("     VALUE(SSD1.SUC_AVG_SUB   , 0) AS JAP_SUC_AVG, ");
        stb.append("     VALUE(DSD3.DAI1_TOTAL_SUB, 0) AS MATH_D1_TOTAL, ");
        stb.append("     VALUE(DSD3.DAI1_AVG_SUB  , 0) AS MATH_D1_AVG, ");
        stb.append("     VALUE(SSD3.SUC_TOTAL_SUB , 0) AS MATH_SUC_TOTAL, ");
        stb.append("     VALUE(SSD3.SUC_AVG_SUB   , 0) AS MATH_SUC_AVG, ");
        stb.append("     VALUE(DSD5.DAI1_TOTAL_SUB, 0) AS ENG_D1_TOTAL, ");
        stb.append("     VALUE(DSD5.DAI1_AVG_SUB  , 0) AS ENG_D1_AVG, ");
        stb.append("     VALUE(SSD5.SUC_TOTAL_SUB , 0) AS ENG_SUC_TOTAL, ");
        stb.append("     VALUE(SSD5.SUC_AVG_SUB   , 0) AS ENG_SUC_AVG ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("    ,VALUE(DSD2.DAI1_TOTAL_SUB, 0) AS SOC_D1_TOTAL, ");
            stb.append("     VALUE(DSD2.DAI1_AVG_SUB  , 0) AS SOC_D1_AVG, ");
            stb.append("     VALUE(SSD2.SUC_TOTAL_SUB , 0) AS SOC_SUC_TOTAL, ");
            stb.append("     VALUE(SSD2.SUC_AVG_SUB   , 0) AS SOC_SUC_AVG, ");
            stb.append("     VALUE(DSD4.DAI1_TOTAL_SUB, 0) AS SCI_D1_TOTAL, ");
            stb.append("     VALUE(DSD4.DAI1_AVG_SUB  , 0) AS SCI_D1_AVG, ");
            stb.append("     VALUE(SSD4.SUC_TOTAL_SUB , 0) AS SCI_SUC_TOTAL, ");
            stb.append("     VALUE(SSD4.SUC_AVG_SUB   , 0) AS SCI_SUC_AVG, ");
            stb.append("     VALUE(DSD6.DAI1_TOTAL_SUB, 0) AS PRA_D1_TOTAL, ");
            stb.append("     VALUE(DSD6.DAI1_AVG_SUB  , 0) AS PRA_D1_AVG, ");
            stb.append("     VALUE(SSD6.SUC_TOTAL_SUB , 0) AS PRA_SUC_TOTAL, ");
            stb.append("     VALUE(SSD6.SUC_AVG_SUB   , 0) AS PRA_SUC_AVG ");
        }
        stb.append(" FROM ");
        stb.append("     BASE B1 ");
        stb.append("     LEFT JOIN DAI1_INT DINT ON B1.YEAR = DINT.ENTEXAMYEAR ");
        stb.append("                            AND B1.CMCD = DINT.DAI1_CMCD ");
        stb.append("     LEFT JOIN SUC_INT SINT  ON B1.YEAR = SINT.ENTEXAMYEAR ");
        stb.append("                            AND B1.CMCD = SINT.SUC_CMCD ");
        stb.append("     LEFT JOIN DAI1_DATA DD  ON B1.YEAR = DD.ENTEXAMYEAR ");
        stb.append("                            AND B1.CMCD = DD.DAI1_CMCD ");
        stb.append("     LEFT JOIN SUC_DATA SD   ON B1.YEAR = SD.ENTEXAMYEAR ");
        stb.append("                            AND B1.CMCD = SD.SUC_CMCD ");
        stb.append("     LEFT JOIN DAI1_SUB_DATA DSD1 ON B1.YEAR = DSD1.ENTEXAMYEAR ");
        stb.append("                                 AND B1.CMCD = DSD1.DAI1_CMCD ");
        stb.append("                                 AND DSD1.TESTSUBCLASSCD = '1' ");
        stb.append("     LEFT JOIN DAI1_SUB_DATA DSD3 ON B1.YEAR = DSD3.ENTEXAMYEAR ");
        stb.append("                                 AND B1.CMCD = DSD3.DAI1_CMCD ");
        stb.append("                                 AND DSD3.TESTSUBCLASSCD = '3' ");
        stb.append("     LEFT JOIN DAI1_SUB_DATA DSD5 ON B1.YEAR = DSD5.ENTEXAMYEAR ");
        stb.append("                                 AND B1.CMCD = DSD5.DAI1_CMCD ");
        stb.append("                                 AND DSD5.TESTSUBCLASSCD = '5' ");
        stb.append("     LEFT JOIN SUC_SUB_DATA SSD1 ON B1.YEAR = SSD1.ENTEXAMYEAR ");
        stb.append("                                AND B1.CMCD = SSD1.SUC_CMCD ");
        stb.append("                                AND SSD1.TESTSUBCLASSCD = '1' ");
        stb.append("     LEFT JOIN SUC_SUB_DATA SSD3 ON B1.YEAR = SSD3.ENTEXAMYEAR ");
        stb.append("                                AND B1.CMCD = SSD3.SUC_CMCD ");
        stb.append("                                AND SSD3.TESTSUBCLASSCD = '3' ");
        stb.append("     LEFT JOIN SUC_SUB_DATA SSD5 ON B1.YEAR = SSD5.ENTEXAMYEAR ");
        stb.append("                                AND B1.CMCD = SSD5.SUC_CMCD ");
        stb.append("                                AND SSD5.TESTSUBCLASSCD = '5' ");
        if ("2".equals(_param._csvDiv)) {
            stb.append("     LEFT JOIN DAI1_SUB_DATA DSD2 ON B1.YEAR = DSD2.ENTEXAMYEAR ");
            stb.append("                                 AND B1.CMCD = DSD2.DAI1_CMCD ");
            stb.append("                                 AND DSD2.TESTSUBCLASSCD = '2' ");
            stb.append("     LEFT JOIN DAI1_SUB_DATA DSD4 ON B1.YEAR = DSD4.ENTEXAMYEAR ");
            stb.append("                                 AND B1.CMCD = DSD4.DAI1_CMCD ");
            stb.append("                                 AND DSD4.TESTSUBCLASSCD = '4' ");
            stb.append("     LEFT JOIN DAI1_SUB_DATA DSD6 ON B1.YEAR = DSD6.ENTEXAMYEAR ");
            stb.append("                                 AND B1.CMCD = DSD6.DAI1_CMCD ");
            stb.append("                                 AND DSD6.TESTSUBCLASSCD = '6' ");
            stb.append("     LEFT JOIN SUC_SUB_DATA SSD2 ON B1.YEAR = SSD2.ENTEXAMYEAR ");
            stb.append("                                AND B1.CMCD = SSD2.SUC_CMCD ");
            stb.append("                                AND SSD2.TESTSUBCLASSCD = '2' ");
            stb.append("     LEFT JOIN SUC_SUB_DATA SSD4 ON B1.YEAR = SSD4.ENTEXAMYEAR ");
            stb.append("                                AND B1.CMCD = SSD4.SUC_CMCD ");
            stb.append("                                AND SSD4.TESTSUBCLASSCD = '4' ");
            stb.append("     LEFT JOIN SUC_SUB_DATA SSD6 ON B1.YEAR = SSD6.ENTEXAMYEAR ");
            stb.append("                                AND B1.CMCD = SSD6.SUC_CMCD ");
            stb.append("                                AND SSD6.TESTSUBCLASSCD = '6' ");
        }
        stb.append(" WHERE  ");
        stb.append("     B1.YEAR = '" + _param._entExamYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     B1.CMCD ");
        return stb.toString();
    }

    private class KoumokuClass {
        final String _titleName;
        final String _roundFlg;
        final String _setFieldCd;
        final String _subclassName;
        final String _scoreName;
        final String _sqlField;
        public KoumokuClass(
                final String titleName,
                final String roundFlg,
                final String setFieldCd,
                final String subclassName,
                final String scoreName,
                final String sqlField
        ) {
            _titleName    = titleName;
            _roundFlg     = roundFlg;
            _setFieldCd   = setFieldCd;
            _subclassName = subclassName;
            _scoreName    = scoreName;
            _sqlField     = sqlField;
        }
    }

    private class SchoolData {
        final String _schoolCd;
        final String _schoolName;
        final String _courseCd;
        final String _courseName;
        final String _distName;
        public SchoolData(
                final String schoolCd,
                final String schoolName,
                final String courseCd,
                final String courseName,
                final String distName
        ) {
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _courseCd = courseCd;
            _courseName = courseName;
            _distName = distName;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 60549 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _entExamYear;
        private final String _csvDiv;
        private final String[] _selected;
        private final List _koumokuList;
        private final List _schoolList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {
            _year         = request.getParameter("YEAR");
            _ctrlYear     = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("CTRL_DATE");
            _entExamYear  = request.getParameter("ENTEXAMYEAR");
            _csvDiv       = request.getParameter("CSVDIV");
            _selected     = request.getParameterValues("CATEGORY_SELECTED");
            _koumokuList  = getKoumokuList();
            _schoolList   = getSchoolList(db2);

        }

        private List getKoumokuList() {
            final List retList = new ArrayList();

            retList.add(new KoumokuClass("",   "",   "9", "", "","CMNAME"));
            retList.add(new KoumokuClass("",   "",   "1", "","学力検査等による受検者数","DAI1_CM_INT"));
            retList.add(new KoumokuClass("",   "",   "1", "","学力検査等による合格者数","SUC_CM_INT"));
            if ("2".equals(_csvDiv)) {
                retList.add(new KoumokuClass("学", "",   "2", "５","受検者総得点","DAI1_TOTAL_3"));
                retList.add(new KoumokuClass("力",  "2", "2", "教","総平均点","DAI1_AVG_3"));
                retList.add(new KoumokuClass("検", "",   "2", "科","合格者総得点","SUC_TOTAL_3"));
                retList.add(new KoumokuClass("査",  "2", "2", "",  "合格者平均点","SUC_AVG_3"));
            } else {
                retList.add(new KoumokuClass("学", "",   "2", "全","受検者総得点","DAI1_TOTAL_3"));
                retList.add(new KoumokuClass("力", "2",  "2", "教","総平均点","DAI1_AVG_3"));
                retList.add(new KoumokuClass("検", "",   "2", "科","合格者総得点","SUC_TOTAL_3"));
                retList.add(new KoumokuClass("査", "2",  "2", "",  "合格者平均点","SUC_AVG_3"));
            }
            retList.add(new KoumokuClass("得", "",   "3", "国","受検者総得点","JAP_D1_TOTAL"));
            retList.add(new KoumokuClass("点", "2",  "3", "語","総平均点","JAP_D1_AVG"));
            retList.add(new KoumokuClass("",   "",   "3", "",  "合格者総得点","JAP_SUC_TOTAL"));
            retList.add(new KoumokuClass("",    "2", "3", "",  "合格者平均点","JAP_SUC_AVG"));
            retList.add(new KoumokuClass("",   "",   "4", "数","受検者総得点","MATH_D1_TOTAL"));
            retList.add(new KoumokuClass("",    "2", "4", "学","総平均点","MATH_D1_AVG"));
            retList.add(new KoumokuClass("",   "",   "4", "",  "合格者総得点","MATH_SUC_TOTAL"));
            retList.add(new KoumokuClass("",    "2", "4", "",  "合格者平均点","MATH_SUC_AVG"));
            if ("2".equals(_csvDiv)) {
                retList.add(new KoumokuClass("",   "",   "5", "社","受検者総得点","SOC_D1_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "5", "会","総平均点","SOC_D1_AVG"));
                retList.add(new KoumokuClass("",   "",   "5", "",  "合格者総得点","SOC_SUC_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "5", "",  "合格者平均点","SOC_SUC_AVG"));
            }
            retList.add(new KoumokuClass("",   "",   "6", "英","受検者総得点","ENG_D1_TOTAL"));
            retList.add(new KoumokuClass("",    "2", "6", "語","総平均点","ENG_D1_AVG"));
            retList.add(new KoumokuClass("",   "",   "6", "",  "合格者総得点","ENG_SUC_TOTAL"));
            retList.add(new KoumokuClass("",    "2", "6", "",  "合格者平均点","ENG_SUC_AVG"));
            if ("2".equals(_csvDiv)) {
                retList.add(new KoumokuClass("",   "",   "7", "理","受検者総得点","SCI_D1_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "7", "科","総平均点","SCI_D1_AVG"));
                retList.add(new KoumokuClass("",   "",   "7", "",  "合格者総得点","SCI_SUC_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "7", "",  "合格者平均点","SCI_SUC_AVG"));
                retList.add(new KoumokuClass("",   "",   "8", "実","受検者総得点","PRA_D1_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "8", "技","総平均点","PRA_D1_AVG"));
                retList.add(new KoumokuClass("",   "",   "8", "",  "合格者総得点","PRA_SUC_TOTAL"));
                retList.add(new KoumokuClass("",    "2", "8", "",  "合格者平均点","PRA_SUC_AVG"));
            }

            return retList;
        }

        private List getSchoolList(final DB2UDB db2) throws SQLException, ParseException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            for (int i = 0; i < _selected.length; i++) {
                final String schoolCd = _selected[i];
                try {
                    final String titleSql = getSchoolSql(schoolCd);
                    ps = db2.prepareStatement(titleSql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schoolName = rs.getString("FINSCHOOL_NAME");
                        final String courseCd = rs.getString("COURSECD");
                        final String courseName = rs.getString("COURSENAME");
                        final String distName = rs.getString("DIST_NAME");
                        final SchoolData schoolData = new SchoolData(schoolCd, schoolName, courseCd, courseName, distName);
                        retList.add(schoolData);
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH COURSE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         EDBOARD_COURSE_MST T1, ");
            stb.append("         (SELECT ");
            stb.append("              MIN(COURSECD) AS COURSECD ");
            stb.append("          FROM ");
            stb.append("              EDBOARD_COURSE_MST ");
            stb.append("          WHERE ");
            stb.append("              EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("         ) T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("         AND T1.COURSECD = T2.COURSECD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLCD AS KYOUIKU_IINKAI_SCHOOLCD, ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     COURSE.COURSECD, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     L1.NAME1 AS DIST_NAME ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1, ");
            stb.append("     FINSCHOOL_MST T2 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2, ");
            stb.append("     COURSE ");
            stb.append(" WHERE ");
            stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("     AND T1.EDBOARD_SCHOOLCD = T2.FINSCHOOLCD ");

            return stb.toString();
        }
    }
}

// eof

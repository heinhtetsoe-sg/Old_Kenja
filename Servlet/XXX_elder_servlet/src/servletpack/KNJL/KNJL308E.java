/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 898c9c2e886546f6b05ad2ec0821e05c2bc81264 $
 *
 * 作成日: 2019/01/11
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL308E {

    private static final Log log = LogFactory.getLog(KNJL308E.class);

    private boolean _hasData;
    private final String KANENDO_SOTSU = "1";
    private final String KANENDO_SOTSU_TITLE = "過年度卒者";

    private final String GIMON_TYUUI = "2";
    private final String GIMON_TYUUI_TITLE = "疑問・注意を要する";

    private final String KOUDOU_NASHI = "3";
    private final String KOUDOU_NASHI_TITLE = "行動記録チェックなし";

    private final String KESSEKI = "4";
    private final String KESSEKI_TITLE = "欠席16以上";
    private final int KESSEKI_JOUGEN = 16;

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
        svf.VrSetForm("KNJL308E.frm", 4);
        setTitle(db2, svf);
        final Map printMap = getHanteiMap(db2);

        for (Iterator itPrintMap = printMap.keySet().iterator(); itPrintMap.hasNext();) {
            final String printCd = (String) itPrintMap.next();
            final List printList = (List) printMap.get(printCd);
            if (_hasData) {
                //空行
                svf.VrsOut("BLANK", printCd);
                svf.VrEndRecord();
            }
            //審議タイトル
            svf.VrsOut("DISCUSSION", getDiscussionName(printCd));
            svf.VrEndRecord();

            //科目名
            if (_param._subclsNameList.size() > 0) {
                svf.VrsOut("SUBCLASS_NAME1", StringUtils.defaultString((String)_param._subclsNameList.get(0)));
            }
            if (_param._subclsNameList.size() > 1) {
                svf.VrsOut("SUBCLASS_NAME2", StringUtils.defaultString((String)_param._subclsNameList.get(1)));
            }
            if (_param._subclsNameList.size() > 2) {
                svf.VrsOut("SUBCLASS_NAME3", StringUtils.defaultString((String)_param._subclsNameList.get(2)));
            }
            svf.VrEndRecord();
            
            for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final PrintData printData = (PrintData) iterator.next();

                //データ
                //受験番号
                svf.VrsOut("EXAM_NO" , StringUtils.defaultString(printData._examNo));
                //氏名
                svf.VrsOut("NAME1" , StringUtils.defaultString(printData._name));
                //中学校
                svf.VrsOut("FINSCHOOL_NAME" , StringUtils.defaultString(printData._finSchoolNameAbbv));
                //成績1
                svf.VrsOut("SCORE1", StringUtils.defaultString(printData._score1));
                //成績2
                svf.VrsOut("SCORE2", StringUtils.defaultString(printData._score2));
                //成績3
                svf.VrsOut("SCORE3", StringUtils.defaultString(printData._score3));
                //合計
                svf.VrsOut("TOTAL4", StringUtils.defaultString(printData._total4));
                //順位
                svf.VrsOut("RANK4", StringUtils.defaultString(printData._total_Rank4));
                //面接
                svf.VrsOut("INTERVIEW_A", StringUtils.defaultString(printData._interview1));
                svf.VrsOut("INTERVIEW_B", StringUtils.defaultString(printData._interview2));
                //卒業年月
                svf.VrsOut("GRD_YM" , KNJ_EditDate.h_format_JP_M(db2, StringUtils.defaultString(printData._fsDay)));
                //欠席
                final String setAttend = printData._attend >= KESSEKI_JOUGEN ? String.valueOf(printData._attend) : "";
                svf.VrsOut("ATTEND", setAttend);
                //備考
                final String[] printRemark = KNJ_EditEdit.get_token(StringUtils.defaultString(printData._remark), 40, 2);
                if (null != printRemark) {
                    for (int i = 0; i < printRemark.length; i++) {
                        svf.VrsOut("REMARK" + (i + 1), printRemark[i]);
                    }
                }

                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private String getDiscussionName(final String printCd) {
        if (KANENDO_SOTSU.equals(printCd)) {
            return KANENDO_SOTSU_TITLE;
        } else if (GIMON_TYUUI.equals(printCd)) {
            return GIMON_TYUUI_TITLE;
        } else if (KOUDOU_NASHI.equals(printCd)) {
            return KOUDOU_NASHI_TITLE;
        } else if (KESSEKI.equals(printCd)) {
            return KESSEKI_TITLE;
        }
        return "";
    }

    private Map getHanteiMap(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        final List kanendoList = getList(db2, KANENDO_SOTSU);
        retMap.put(KANENDO_SOTSU, kanendoList);
        final List gimonList = getList(db2, GIMON_TYUUI);
        retMap.put(GIMON_TYUUI, gimonList);
        final List koudouList = getList(db2, KOUDOU_NASHI);
        retMap.put(KOUDOU_NASHI, koudouList);
        final List kessekiList = getList(db2, KESSEKI);
        retMap.put(KESSEKI, kessekiList);
        return retMap;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        String setYear = KNJ_EditDate.getAutoFormatYearNen(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" +  _param._testdivName + "　" + "入試判定資料");
    }

    private List getList(final DB2UDB db2, final String whereDiv) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(whereDiv);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String judgement = rs.getString("JUDGEMENT");
                final String judge = rs.getString("JUDGE");
                final String examNo = rs.getString("EXAMNO");
                final String examHallCd = rs.getString("EXAMHALLCD");
                final String examHallName = rs.getString("EXAMHALL_NAME");
                final String name = rs.getString("NAME");
                final String fs_Cd = rs.getString("FS_CD");
                final String finSchoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String fsDay = rs.getString("FS_DAY");
                final String testDiv = rs.getString("TESTDIV");
                final String tDivName = rs.getString("TDIVNAME");
                final String heiganAbbv = rs.getString("HEIGAN_ABBV");
                final String desirediv = rs.getString("DESIREDIV");
                final String desiredivname = rs.getString("DESIREDIVNAME");
                //final String avgAll = rs.getString("AVERAGE_ALL");
                String avgAll = "";
                if (rs.getString("AVERAGE_ALL") != null && 0.0 != rs.getDouble("AVERAGE_ALL")) {
                    avgAll = new DecimalFormat("0.0").format(rs.getBigDecimal("AVERAGE_ALL"));
                }
                final String maruCnt = rs.getString("MARU_CNT");
                final String interview1 = rs.getString("INTERVIEW1");
                final String interview2 = rs.getString("INTERVIEW2");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String total2 = rs.getString("TOTAL2");
                final String total_Rank2 = rs.getString("TOTAL_RANK2");
                final String total4 = rs.getString("TOTAL4");
                final String total_Rank4 = rs.getString("TOTAL_RANK4");
                final String remark = rs.getString("REMARK");
                final int attend = rs.getInt("ATTEND");

                final PrintData printData = new PrintData(judgement, judge, examNo, examHallCd, examHallName, name, fs_Cd, finSchoolNameAbbv, fsDay, testDiv, tDivName, desirediv, desiredivname, heiganAbbv, avgAll, maruCnt, interview1, interview2, score1, score2, score3, total2, total_Rank2, total4, total_Rank4, remark, attend);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql(final String whereDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLSCNT AS ( ");
        stb.append("  select count(*) AS CNT from V_NAME_MST WHERE NAMECD1 = 'L008' AND YEAR = '" +_param._entExamYear  + "' ");
        stb.append(" ), SUM_SUBCLSSCORE AS ( ");
        stb.append(" select ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  (VALUE(INT(S001.REMARK1), 0) + VALUE(INT(S002.REMARK1), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT01), 0)) AS RPT01, ");
        stb.append("  (VALUE(INT(S001.REMARK2), 0) + VALUE(INT(S002.REMARK2), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT02), 0)) AS RPT02, ");
        stb.append("  (VALUE(INT(S001.REMARK3), 0) + VALUE(INT(S002.REMARK3), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT03), 0)) AS RPT03, ");
        stb.append("  (VALUE(INT(S001.REMARK4), 0) + VALUE(INT(S002.REMARK4), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT04), 0)) AS RPT04, ");
        stb.append("  (VALUE(INT(S001.REMARK5), 0) + VALUE(INT(S002.REMARK5), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT05), 0)) AS RPT05, ");
        stb.append("  (VALUE(INT(S001.REMARK6), 0) + VALUE(INT(S002.REMARK6), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT06), 0)) AS RPT06, ");
        stb.append("  (VALUE(INT(S001.REMARK7), 0) + VALUE(INT(S002.REMARK7), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT07), 0)) AS RPT07, ");
        stb.append("  (VALUE(INT(S001.REMARK8), 0) + VALUE(INT(S002.REMARK8), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT08), 0)) AS RPT08, ");
        stb.append("  (VALUE(INT(S001.REMARK9), 0) + VALUE(INT(S002.REMARK9), 0) + VALUE(INT(S003.CONFIDENTIAL_RPT09), 0)) AS RPT09 ");
        stb.append(" from ");
        stb.append(" ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S001 ");
        stb.append("   ON S001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S001.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S001.EXAMNO = T1.EXAMNO ");
        stb.append("  AND S001.SEQ = '001' ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S002 ");
        stb.append("   ON S002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S002.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S002.EXAMNO = T1.EXAMNO ");
        stb.append("  AND S002.SEQ = '002' ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT S003 ");
        stb.append("   ON S003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S003.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S003.EXAMNO = T1.EXAMNO ");
        stb.append(" ), AVERAGE_TOTAL AS ( ");
        stb.append(" SELECT ");
        stb.append("   S_DAT.ENTEXAMYEAR, ");
        stb.append("   S_DAT.APPLICANTDIV, ");
        stb.append("   S_DAT.EXAMNO, ");
        stb.append("   R003.REMARK1 AS MARU_CNT, ");
        stb.append("   ( (S_DAT.RPT01 + S_DAT.RPT02 + S_DAT.RPT03 + S_DAT.RPT04 ");
        stb.append("    + S_DAT.RPT05 + S_DAT.RPT06 + S_DAT.RPT07 + S_DAT.RPT08 + S_DAT.RPT09) / (3.0 * SCNT.CNT) ) AS AVERAGE_ALL ");
        stb.append(" FROM ");
        stb.append("   SUM_SUBCLSSCORE S_DAT ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT R003 ");
        stb.append("   ON R003.ENTEXAMYEAR = S_DAT.ENTEXAMYEAR ");
        stb.append("   AND R003.APPLICANTDIV = S_DAT.APPLICANTDIV ");
        stb.append("   AND R003.EXAMNO = S_DAT.EXAMNO ");
        stb.append("   AND R003.SEQ = '003' ");
        stb.append("   ,SUBCLSCNT SCNT ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.JUDGEMENT, ");
        stb.append("   L013.NAME1 AS JUDGE, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   H1.EXAMHALLCD, ");
        stb.append("   H2.EXAMHALL_NAME, ");
        stb.append("   T1.NAME, ");
        stb.append("   T1.FS_CD, ");
        stb.append("   FM1.FINSCHOOL_NAME_ABBV, ");
        stb.append("   T1.FS_DAY, ");
        stb.append("   T1.TESTDIV, ");
        stb.append("   L045.NAME1 AS TDIVNAME, ");
        stb.append("   CASE WHEN VALUE(L004_2.NAMESPARE3, '') = '2' THEN L061.ABBV1 END AS HEIGAN_ABBV, "); // 音楽家の併願略称
        stb.append("   T1.DESIREDIV, ");
        stb.append("   L058.NAME1 AS DESIREDIVNAME, ");
        stb.append("   L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("   L027_2.NAME1 AS INTERVIEW2, ");
        stb.append("   ROUND(ATOTAL.AVERAGE_ALL, 1) AS AVERAGE_ALL, ");
        stb.append("   ATOTAL.MARU_CNT, ");
        stb.append("   S1_1.SCORE AS SCORE1, ");
        stb.append("   S1_2.SCORE AS SCORE2, ");
        stb.append("   S1_3.SCORE AS SCORE3, ");
        stb.append("   S2.TOTAL2, ");
        stb.append("   S2.TOTAL_RANK2, ");
        stb.append("   S2.TOTAL4, ");
        stb.append("   S2.TOTAL_RANK4, ");
        stb.append("   VALUE(CONFRPT.REMARK1, '') AS REMARK, ");
        stb.append("   VALUE(CONFRPT.ABSENCE_DAYS3, 0) AS ATTEND ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("      ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND H1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND H1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND H1.EXAMNO = T1.EXAMNO ");
        stb.append("     AND H1.EXAMHALL_TYPE = '2' ");  //2(固定で指定)
        stb.append("    LEFT JOIN ENTEXAM_HALL_YDAT H2 ");
        stb.append("      ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("     AND H2.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("     AND H2.TESTDIV = H1.TESTDIV ");
        stb.append("     AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ");
        stb.append("     AND H2.EXAMHALLCD = H1.EXAMHALLCD ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT S2 ");
        stb.append("     ON S2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND S2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND S2.TESTDIV = T1.TESTDIV ");
        stb.append("    AND S2.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ");
        stb.append("     ON CONFRPT.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND CONFRPT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND CONFRPT.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT003 ");
        stb.append("     ON CONFRPT003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND CONFRPT003.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND CONFRPT003.EXAMNO = T1.EXAMNO ");
        stb.append("    AND CONFRPT003.SEQ = '003' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_1 ");
        stb.append("     ON S1_1.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_1.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_1.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_1.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_1.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_2 ");
        stb.append("     ON S1_2.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_2.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_2.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_2.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_2.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_3 ");
        stb.append("     ON S1_3.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_3.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_3.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_3.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_3.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("     ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("    AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("     ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("   LEFT JOIN NAME_MST L013 ");
        stb.append("     ON L013.NAMECD1 = 'L013' ");
        stb.append("    AND L013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("   LEFT JOIN NAME_MST L045 ");
        stb.append("     ON L045.NAMECD1 = 'L045' ");
        stb.append("    AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("   LEFT JOIN NAME_MST L027_1 ");
        stb.append("     ON L027_1.NAMECD1 = 'L027' ");
        stb.append("    AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("   LEFT JOIN NAME_MST L027_2 ");
        stb.append("     ON L027_2.NAMECD1 = 'L027' ");
        stb.append("    AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append("   LEFT JOIN AVERAGE_TOTAL ATOTAL ");
        stb.append("     ON ATOTAL.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND ATOTAL.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND ATOTAL.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST L058 ");
        stb.append("     ON L058.NAMECD1 = 'L058' ");
        stb.append("    AND L058.NAMECD2 = T1.DESIREDIV ");
        stb.append("   INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ");
        stb.append("                              AND L004.NAMECD1    = 'L004' ");
        stb.append("                              AND L004.NAMECD2    = T1.TESTDIV ");
        stb.append("                              AND L004.NAMESPARE1 = '" + _param._testDiv + "' ");
        stb.append("   LEFT JOIN V_NAME_MST L004_2 ");
        stb.append("     ON L004_2.YEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND L004_2.NAMECD1 = 'L004' ");
        stb.append("    AND L004_2.NAMECD2 = T1.TESTDIV ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ");
        stb.append("     ON BD33.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD33.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD33.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD33.SEQ = '033' ");
        stb.append("   LEFT JOIN V_NAME_MST L061 ");
        stb.append("     ON L061.YEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND L061.NAMECD1 = 'L061' ");
        stb.append("    AND L061.NAMECD2 = BD33.REMARK3 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entExamYear + "'   AND ");
        stb.append("   T1.APPLICANTDIV = '" + _param._applicantDiv + "'   AND ");
        stb.append("   VALUE(T1.JUDGEMENT, '') <> '3' ");
        stb.append("   AND NOT (VALUE(L004.NAMESPARE3, '') = '2' AND VALUE(L061.NAMESPARE1, '') <> '1') "); // 音楽家の単願（2科目合計のみ）を除く
        if (KANENDO_SOTSU.equals(whereDiv)) {
            stb.append("   AND T1.FS_GRDDIV = '2' ");
        }
        if (GIMON_TYUUI.equals(whereDiv)) {
            stb.append("   AND VALUE(CONFRPT.REMARK2, '0') = '1' ");
        }
        if (KOUDOU_NASHI.equals(whereDiv)) {
            stb.append("   AND VALUE(CONFRPT003.REMARK1, '0') = '0' ");
        }
        if (KESSEKI.equals(whereDiv)) {
            stb.append("   AND VALUE(CONFRPT.ABSENCE_DAYS3, 0) >= " + KESSEKI_JOUGEN + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _judgement;
        final String _judge;
        final String _examNo;
        final String _examHallCd;
        final String _examHallName;
        final String _name;
        final String _fs_Cd;
        final String _finSchoolNameAbbv;
        final String _fsDay;
        final String _testDiv;
        final String _tDivName;
        final String _desirediv;
        final String _desiredivname;
        final String _heiganAbbv;
        final String _avgAll;
        final String _maruCnt;
        final String _interview1;
        final String _interview2;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _total2;
        final String _total_Rank2;
        final String _total4;
        final String _total_Rank4;
        final String _remark;
        final int _attend;

        public PrintData(
                final String judgement,
                final String judge,
                final String examNo,
                final String examHallCd,
                final String examHallName,
                final String name,
                final String fs_Cd,
                final String finSchoolNameAbbv,
                final String fsDay,
                final String testDiv,
                final String tDivName,
                final String desirediv,
                final String desiredivname,
                final String heiganAbbv,
                final String avgAll,
                final String maruCnt,
                final String interview1,
                final String interview2,
                final String score1,
                final String score2,
                final String score3,
                final String total2,
                final String total_Rank2,
                final String total4,
                final String total_Rank4,
                final String remark,
                final int attend
        ) {
            _judgement = judgement;
            _judge = judge;
            _examNo = examNo;
            _examHallCd = examHallCd;
            _examHallName = examHallName;
            _name = name;
            _fs_Cd = fs_Cd;
            _finSchoolNameAbbv = finSchoolNameAbbv;
            _fsDay = fsDay;
            _testDiv = testDiv;
            _tDivName = tDivName;
            _desirediv = desirediv;
            _desiredivname = desiredivname;
            _heiganAbbv = heiganAbbv;
            _avgAll = avgAll;
            _maruCnt = maruCnt;
            _interview1 = interview1;
            _interview2 = interview2;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _total2 = total2;
            _total_Rank2 = total_Rank2;
            _total4 = total4;
            _total_Rank4 = total_Rank4;
            _remark = remark;
            _attend = attend;
          }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66152 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final List _subclsNameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = getNameMst(db2, "NAME1", "L065", _testDiv);
            _subclsNameList = getNameMstList(db2, "NAME1", "L009");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if ("".equals(namecd2)) {
                    ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                } else {
                    ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private List getNameMstList(final DB2UDB db2, final String field, final String namecd1) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnList.add(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnList;
        }
    }
}

// eof

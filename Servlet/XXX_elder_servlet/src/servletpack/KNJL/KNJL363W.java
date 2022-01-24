/*
 * $Id: 9bd7f47b6759f61178e504eb2013f0593b977041 $
 *
 * 作成日: 2017/11/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL363W {

    private static final Log log = LogFactory.getLog(KNJL363W.class);

    private boolean _hasData;
    private final String KOKUGO = "1";
    private final String SANSUU = "2";
    private final String RIKA = "3";
    private final String SYAKAI = "4";
    private final String GOUKEI = "B";

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            svf = new Vrw32alp();
            response.setContentType("application/pdf");
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            _hasData = false;
            for (Iterator itSchool = _param._schoolList.iterator(); itSchool.hasNext();) {
                final SchoolData schoolData = (SchoolData) itSchool.next();
                printMain(db2, svf, schoolData);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final SchoolData schoolData) {
        final List printList = getList(db2, schoolData);

        String kaiPageKey = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            //改ページ
            if (!kaiPageKey.equals(printData._kaiPageKey)) {
                kaiPageKey = printData._kaiPageKey;

                //ヘッダー部
                svf.VrSetForm("KNJL363W.frm", 4);
                svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "/04/01") + "度" + "三重県立高等学校入学志願者検査個人別・出身中学校別一覧表");
                svf.VrsOut("SCHOOL_NAME", schoolData._schoolName);
                svf.VrsOut("COURSE", schoolData._courseName);
                svf.VrsOut("EXAM_NAME", printData._testdivName);
                svf.VrsOut("MAJOR_NAME", printData._dai1MajorName);
                svf.VrsOut("COURSE_NAME", printData._dai1CourseName);
                svf.VrsOut("CLASS_NAME1_1", "国語");
                svf.VrsOut("CLASS_NAME1_2", "社会");
                svf.VrsOut("CLASS_NAME1_3", "数学");
                svf.VrsOut("CLASS_NAME1_4", "理科");
                svf.VrsOut("CLASS_NAME1_5", "英語");
                svf.VrsOut("CLASS_NAME1_6", "実技");
                svf.VrsOut("CLASS_NAME2_1", "国語");
                svf.VrsOut("CLASS_NAME2_2", "社会");
                svf.VrsOut("CLASS_NAME2_3", "数学");
                svf.VrsOut("CLASS_NAME2_4", "理科");
                svf.VrsOut("CLASS_NAME2_5", "音楽");
                svf.VrsOut("CLASS_NAME2_6", "美術");
                svf.VrsOut("CLASS_NAME2_7", "保健体育");
                svf.VrsOut("CLASS_NAME2_8", "技術・家庭");
                svf.VrsOut("CLASS_NAME2_9", "外国語");
                svf.VrsOut("PER1", "100");
                svf.VrsOut("PER2", "80");
            }


            //データ部
            //校外願変
            if ("5".equals(printData._judgement)) {
                svf.VrsOut("EXAM_NO", printData._examno);
                svf.VrsOut("REMARK1", "校外願変");
            } else {
                svf.VrsOut("EXAM_NO", printData._examno);
                final String fsnameField = KNJ_EditEdit.getMS932ByteLength(printData._finschoolName) <= 10 ? "1" : "2";
                svf.VrsOut("FINSCHOOL_NAME" + fsnameField, printData._finschoolName);
                svf.VrsOut("SEX", printData._sexName);
                svf.VrsOut("SCORE1_1", printData._score1);
                svf.VrsOut("SCORE1_2", printData._score2);
                svf.VrsOut("SCORE1_3", printData._score3);
                svf.VrsOut("SCORE1_4", printData._score4);
                svf.VrsOut("SCORE1_5", printData._score5);
                svf.VrsOut("SCORE1_6", printData._score6);
                svf.VrsOut("SCORE1_7", printData._scoreT);
                svf.VrsOut("SCORE2_1", printData._conf3Rpt01);
                svf.VrsOut("SCORE2_2", printData._conf3Rpt02);
                svf.VrsOut("SCORE2_3", printData._conf3Rpt03);
                svf.VrsOut("SCORE2_4", printData._conf3Rpt04);
                svf.VrsOut("SCORE2_5", printData._conf3Rpt05);
                svf.VrsOut("SCORE2_6", printData._conf3Rpt06);
                svf.VrsOut("SCORE2_7", printData._conf3Rpt07);
                svf.VrsOut("SCORE2_8", printData._conf3Rpt08);
                svf.VrsOut("SCORE2_9", printData._conf3Rpt09);
                svf.VrsOut("SCORE2_10", printData._conf3Total);
                svf.VrsOut("INVEST_PER", printData._conf3Total100);
                svf.VrsOut("SCORE_PER", printData._scoreT80);
                svf.VrsOut("JUDGE", printData._judge);
                svf.VrsOut("SECOND_PASS", printData._dai2SucMajorName);
                svf.VrsOut("VAL1", printData._conf1Total);
                svf.VrsOut("VAL2", printData._conf2Total);
                svf.VrsOut("REMARK1", printData._remark);
            }

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2, final SchoolData schoolData) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getListSql(schoolData._schoolCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String kaiPageKey = rs.getString("KAI_PAGE_KEY");
                final String testdivName = rs.getString("TESTDIV_NAME");
                final String dai1MajorName = rs.getString("DAI1_MAJOR_NAME");
                final String dai1CourseName = rs.getString("DAI1_COURSE_NAME");
                final String examno = rs.getString("EXAMNO");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String sexName = rs.getString("SEX_NAME");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String score4 = rs.getString("SCORE4");
                final String score5 = rs.getString("SCORE5");
                final String score6 = rs.getString("SCORE6");
                final String scoreT = rs.getString("SCORET");
                final String conf3Rpt01 = rs.getString("CONF3_RPT01");
                final String conf3Rpt02 = rs.getString("CONF3_RPT02");
                final String conf3Rpt03 = rs.getString("CONF3_RPT03");
                final String conf3Rpt04 = rs.getString("CONF3_RPT04");
                final String conf3Rpt05 = rs.getString("CONF3_RPT05");
                final String conf3Rpt06 = rs.getString("CONF3_RPT06");
                final String conf3Rpt07 = rs.getString("CONF3_RPT07");
                final String conf3Rpt08 = rs.getString("CONF3_RPT08");
                final String conf3Rpt09 = rs.getString("CONF3_RPT09");
                final String conf3Total = rs.getString("CONF3_TOTAL");
                final String conf3Total100 = rs.getString("CONF3_TOTAL100");
                final String scoreT80 = rs.getString("SCORET80");
                final String judge = rs.getString("JUDGE");
                final String dai2SucMajorName = rs.getString("DAI2_SUC_MAJOR_NAME");
                final String conf1Total = rs.getString("CONF1_TOTAL");
                final String conf2Total = rs.getString("CONF2_TOTAL");
                final String judgement = rs.getString("JUDGEMENT");
                String remarkYousiki3 = null != rs.getString("YOUSIKI3_REMARK") ? rs.getString("YOUSIKI3_REMARK") : "";
                String remarkSotugyou = "1".equals(rs.getString("REMARK_SOTUGYOU")) ? "過年度卒業" : "";
                String remarkJitai = "1".equals(rs.getString("REMARK_JITAI")) ? "入学辞退" : "";
                String remarkTokuWaku = "1".equals(rs.getString("REMARK_TOKU_WAKU")) ? "特別枠選抜" : "";
                String remarkOverAge = "1".equals(rs.getString("REMARK_OVER_AGE")) ? "成人" : "";
                String remarkKesseki = "1".equals(rs.getString("REMARK_KESSEKI")) ? "欠席" : "";
                String remark = "";
                String seq = "";
                if (!"".equals(remarkYousiki3)) {
                    remark += seq + remarkYousiki3;
                    seq = "、";
                }
                if (!"".equals(remarkSotugyou)) {
                    remark += seq + remarkSotugyou;
                    seq = "、";
                }
                if (!"".equals(remarkJitai)) {
                    remark += seq + remarkJitai;
                    seq = "、";
                }
                if (!"".equals(remarkTokuWaku)) {
                    remark += seq + remarkTokuWaku;
                    seq = "、";
                }
                if (!"".equals(remarkOverAge)) {
                    remark += seq + remarkOverAge;
                    seq = "、";
                }
                if (!"".equals(remarkKesseki)) {
                    remark += seq + remarkKesseki;
                    seq = "、";
                }

                final PrintData printData = new PrintData(kaiPageKey, testdivName, dai1MajorName, dai1CourseName, examno, finschoolName, sexName, score1, score2, score3, score4, score5, score6, scoreT, conf3Rpt01, conf3Rpt02, conf3Rpt03, conf3Rpt04, conf3Rpt05, conf3Rpt06, conf3Rpt07, conf3Rpt08, conf3Rpt09, conf3Total, conf3Total100, scoreT80, judge, dai2SucMajorName, conf1Total, conf2Total, judgement, remark);
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

    private String getListSql(final String schoolCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.TESTDIV2, ");
        stb.append("     VALUE(T1.TESTDIV2, '0') || T1.LAST_DAI1_COURSECD || T1.LAST_DAI1_MAJORCD || T1.LAST_DAI1_COURSECODE AS KAI_PAGE_KEY, ");
        stb.append("     CASE WHEN T1.TESTDIV2 = '1' ");
        stb.append("          THEN N2.NAME1 || '追検査' ");
        stb.append("          ELSE N2.NAME1 ");
        stb.append("     END AS TESTDIV_NAME, ");
        stb.append("     M1.MAJORNAME AS DAI1_MAJOR_NAME, ");
        stb.append("     C1.EXAMCOURSE_NAME AS DAI1_COURSE_NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     FIN.FINSCHOOL_NAME, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        //得点
        stb.append("     S1.SCORE AS SCORE1, ");
        stb.append("     S2.SCORE AS SCORE2, ");
        stb.append("     S3.SCORE AS SCORE3, ");
        stb.append("     S4.SCORE AS SCORE4, ");
        stb.append("     S5.SCORE AS SCORE5, ");
        stb.append("     S6.SCORE AS SCORE6, ");
        stb.append("     ST.TOTAL AS SCORET, ");
        //評定
        stb.append("     CF0.CONFIDENTIAL_RPT01 AS CONF3_RPT01, ");
        stb.append("     CF0.CONFIDENTIAL_RPT02 AS CONF3_RPT02, ");
        stb.append("     CF0.CONFIDENTIAL_RPT03 AS CONF3_RPT03, ");
        stb.append("     CF0.CONFIDENTIAL_RPT04 AS CONF3_RPT04, ");
        stb.append("     CF0.CONFIDENTIAL_RPT05 AS CONF3_RPT05, ");
        stb.append("     CF0.CONFIDENTIAL_RPT06 AS CONF3_RPT06, ");
        stb.append("     CF0.CONFIDENTIAL_RPT07 AS CONF3_RPT07, ");
        stb.append("     CF0.CONFIDENTIAL_RPT08 AS CONF3_RPT08, ");
        stb.append("     CF0.CONFIDENTIAL_RPT09 AS CONF3_RPT09, ");
        stb.append("     CF0.TOTAL_ALL AS CONF3_TOTAL, ");
        //調査書諸記録の100%
        stb.append("     CASE WHEN VALUE(S100.SCORE, 0) >= 1 ");
        stb.append("          THEN '○' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS CONF3_TOTAL100, ");
        //学力検査等得点合計の80%
        stb.append("     CASE WHEN VALUE(S80.SCORE, 0) >= 1 ");
        stb.append("          THEN '○' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS SCORET80, ");
        //合否
        stb.append("     CASE WHEN T1.TESTDIV = '4' AND T1.JUDGEMENT = '3' THEN '否' "); //スポーツ特別選抜で不合格、前期選抜で合格
        stb.append("          WHEN N3.NAMESPARE1 = '1' THEN '合' ");
        stb.append("          WHEN T1.JUDGEMENT = '2' THEN '否' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS JUDGE, ");
        //第２志望合格者（合格学科名）
        stb.append("     CASE WHEN T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE ");
        stb.append("          THEN M2.MAJORNAME ");
        stb.append("          ELSE '' ");
        stb.append("     END AS DAI2_SUC_MAJOR_NAME, ");
        //１年の評定合計
        //２年の評定合計
        stb.append("     CF1.REMARK12 AS CONF1_TOTAL, ");
        stb.append("     CF2.REMARK12 AS CONF2_TOTAL, ");
        //校外願変
        stb.append("     T1.JUDGEMENT, ");
        //別紙様式３の備考//
        stb.append("     BD032.REMARK1 AS YOUSIKI3_REMARK, ");
        //各内容をつなげて備考にセット
        stb.append("     CASE WHEN T1.FS_GRDDIV='2' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK_SOTUGYOU, ");
        stb.append("     CASE WHEN T1.ENTDIV='2' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK_JITAI, ");
        stb.append("     CASE WHEN T1.KAIGAI_KIKOKUSEI_NADO='1' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK_TOKU_WAKU, ");
        stb.append("     CASE WHEN T1.BIRTHDAY IS NULL ");
        stb.append("          THEN '' ");
        stb.append("          WHEN YEAR('" + _param._entexamYear + "-04-01' - T1.BIRTHDAY) >= 20 ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK_OVER_AGE, ");
        stb.append("     CASE WHEN T1.JUDGEMENT = '4' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK_KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = T1.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L013' AND N3.NAMECD2 = T1.JUDGEMENT ");
        stb.append("     LEFT JOIN EDBOARD_MAJOR_MST M1 ");
        stb.append("          ON M1.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND M1.COURSECD = T1.LAST_DAI1_COURSECD ");
        stb.append("         AND M1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("     LEFT JOIN EDBOARD_MAJOR_MST M2 ");
        stb.append("          ON M2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND M2.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND M2.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_COURSE_MST C1 ");
        stb.append("          ON C1.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C1.COURSECD = T1.LAST_DAI1_COURSECD ");
        stb.append("         AND C1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("         AND C1.EXAMCOURSECD = T1.LAST_DAI1_COURSECODE ");
        for (int i = 1; i <= 6; i++) {
            String strI = String.valueOf(i);
            stb.append("     LEFT JOIN EDBOARD_ENTEXAM_SCORE_DAT S" + strI + " ");
            stb.append("          ON S" + strI + ".EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
            stb.append("         AND S" + strI + ".ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND S" + strI + ".APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND S" + strI + ".TESTDIV = T1.TESTDIV ");
            stb.append("         AND S" + strI + ".RECEPTNO = T1.EXAMNO ");
            stb.append("         AND S" + strI + ".TESTSUBCLASSCD = '" + strI + "' ");
        }

        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_SCORE_DAT S100 ");
        stb.append("          ON S100.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND S100.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND S100.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND S100.TESTDIV = T1.TESTDIV ");
        stb.append("         AND S100.RECEPTNO = T1.EXAMNO ");
        stb.append("         AND S100.TESTSUBCLASSCD = '7' ");

        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_SCORE_DAT S80 ");
        stb.append("          ON S80.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND S80.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND S80.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND S80.TESTDIV = T1.TESTDIV ");
        stb.append("         AND S80.RECEPTNO = T1.EXAMNO ");
        stb.append("         AND S80.TESTSUBCLASSCD = '8' ");

        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             ENTEXAMYEAR, ");
        stb.append("             APPLICANTDIV, ");
        stb.append("             TESTDIV, ");
        stb.append("             RECEPTNO, ");
        stb.append("             SUM(SCORE) AS TOTAL ");
        stb.append("         FROM ");
        stb.append("             EDBOARD_ENTEXAM_SCORE_DAT ");
        stb.append("         WHERE ");
        stb.append("             EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("             AND ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("             AND TESTSUBCLASSCD <= '6' ");
        stb.append("         GROUP BY ");
        stb.append("             ENTEXAMYEAR, ");
        stb.append("             APPLICANTDIV, ");
        stb.append("             TESTDIV, ");
        stb.append("             RECEPTNO ");
        stb.append("     ) ST ON ST.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND ST.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND ST.TESTDIV = T1.TESTDIV ");
        stb.append("         AND ST.RECEPTNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_APPLICANTCONFRPT_DAT CF0 ");
        stb.append("          ON CF0.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND CF0.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CF0.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CF0.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CF1 ");
        stb.append("          ON CF1.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND CF1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CF1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CF1.EXAMNO = T1.EXAMNO ");
        stb.append("         AND CF1.SEQ = '001' ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CF2 ");
        stb.append("          ON CF2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND CF2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CF2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CF2.EXAMNO = T1.EXAMNO ");
        stb.append("         AND CF2.SEQ = '002' ");
        stb.append("     LEFT JOIN EDBOARD_ENTEXAM_APPLICANTBASE_DETAIL_DAT BD032 ");
        stb.append("          ON BD032.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND BD032.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND BD032.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND BD032.EXAMNO = T1.EXAMNO ");
        stb.append("         AND BD032.SEQ = '032' ");
        stb.append(" WHERE ");
        stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
        stb.append("     AND T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append(" ) ");

        //MAIN
        stb.append(" SELECT ");
        stb.append("     T1.TESTDIV || T1.KAI_PAGE_KEY AS KAI_PAGE_KEY, ");
        stb.append("     T1.TESTDIV_NAME, ");
        stb.append("     T1.DAI1_MAJOR_NAME, ");
        stb.append("     T1.DAI1_COURSE_NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.FINSCHOOL_NAME, ");
        stb.append("     T1.SEX_NAME, ");
        //得点
        stb.append("     T1.SCORE1, ");
        stb.append("     T1.SCORE2, ");
        stb.append("     T1.SCORE3, ");
        stb.append("     T1.SCORE4, ");
        stb.append("     T1.SCORE5, ");
        stb.append("     T1.SCORE6, ");
        stb.append("     T1.SCORET, ");
        //評定
        stb.append("     T1.CONF3_RPT01, ");
        stb.append("     T1.CONF3_RPT02, ");
        stb.append("     T1.CONF3_RPT03, ");
        stb.append("     T1.CONF3_RPT04, ");
        stb.append("     T1.CONF3_RPT05, ");
        stb.append("     T1.CONF3_RPT06, ");
        stb.append("     T1.CONF3_RPT07, ");
        stb.append("     T1.CONF3_RPT08, ");
        stb.append("     T1.CONF3_RPT09, ");
        stb.append("     T1.CONF3_TOTAL, ");
        //調査書諸記録の100%
        stb.append("     T1.CONF3_TOTAL100, ");
        //学力検査等得点合計の80%
        stb.append("     T1.SCORET80, ");
        //合否
        stb.append("     T1.JUDGE, ");
        //第２志望合格者（合格学科名）
        stb.append("     T1.DAI2_SUC_MAJOR_NAME, ");
        //１年の評定合計
        //２年の評定合計
        stb.append("     T1.CONF1_TOTAL, ");
        stb.append("     T1.CONF2_TOTAL, ");
        //校外願変
        stb.append("     T1.JUDGEMENT, ");
        //別紙様式３の備考//
        stb.append("     T1.YOUSIKI3_REMARK, ");
        //各内容をつなげて備考にセット
        stb.append("     T1.REMARK_SOTUGYOU, ");
        stb.append("     T1.REMARK_JITAI, ");
        stb.append("     T1.REMARK_TOKU_WAKU, ");
        stb.append("     T1.REMARK_OVER_AGE, ");
        stb.append("     T1.REMARK_KESSEKI ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");

        //スポーツ特別選抜（TESTDIV='4'）の受検者で、前期選抜合格者（JUDGEMENT='3'）と不合格者（JUDGEMENT='2'）は、前期選抜（TESTDIV='1'）にも印字する
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '1' || T1.KAI_PAGE_KEY AS KAI_PAGE_KEY, ");
        stb.append("     CASE WHEN T1.TESTDIV2 = '1' ");
        stb.append("          THEN N2.NAME1 || '追検査' ");
        stb.append("          ELSE N2.NAME1 ");
        stb.append("     END AS TESTDIV_NAME, ");
        stb.append("     T1.DAI1_MAJOR_NAME, ");
        stb.append("     T1.DAI1_COURSE_NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.FINSCHOOL_NAME, ");
        stb.append("     T1.SEX_NAME, ");
        //得点
        stb.append("     T1.SCORE1, ");
        stb.append("     T1.SCORE2, ");
        stb.append("     T1.SCORE3, ");
        stb.append("     T1.SCORE4, ");
        stb.append("     T1.SCORE5, ");
        stb.append("     T1.SCORE6, ");
        stb.append("     T1.SCORET, ");
        //評定
        stb.append("     T1.CONF3_RPT01, ");
        stb.append("     T1.CONF3_RPT02, ");
        stb.append("     T1.CONF3_RPT03, ");
        stb.append("     T1.CONF3_RPT04, ");
        stb.append("     T1.CONF3_RPT05, ");
        stb.append("     T1.CONF3_RPT06, ");
        stb.append("     T1.CONF3_RPT07, ");
        stb.append("     T1.CONF3_RPT08, ");
        stb.append("     T1.CONF3_RPT09, ");
        stb.append("     T1.CONF3_TOTAL, ");
        //調査書諸記録の100%
        stb.append("     T1.CONF3_TOTAL100, ");
        //学力検査等得点合計の80%
        stb.append("     T1.SCORET80, ");
        //合否
        stb.append("     CASE WHEN T1.JUDGEMENT = '2' THEN '否' "); //スポーツ特別選抜で不合格、前期選抜で不合格
        stb.append("          WHEN T1.JUDGEMENT = '3' THEN '合' "); //スポーツ特別選抜で不合格、前期選抜で合格
        stb.append("          ELSE '' ");
        stb.append("     END AS JUDGE, ");
        //第２志望合格者（合格学科名）
        stb.append("     T1.DAI2_SUC_MAJOR_NAME, ");
        //１年の評定合計
        //２年の評定合計
        stb.append("     T1.CONF1_TOTAL, ");
        stb.append("     T1.CONF2_TOTAL, ");
        //校外願変
        stb.append("     T1.JUDGEMENT, ");
        //別紙様式３の備考//
        stb.append("     T1.YOUSIKI3_REMARK, ");
        //各内容をつなげて備考にセット
        stb.append("     T1.REMARK_SOTUGYOU, ");
        stb.append("     T1.REMARK_JITAI, ");
        stb.append("     T1.REMARK_TOKU_WAKU, ");
        stb.append("     T1.REMARK_OVER_AGE, ");
        stb.append("     T1.REMARK_KESSEKI ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = '1' ");
        stb.append(" WHERE ");
        stb.append("     T1.TESTDIV = '4' ");
        stb.append("     AND T1.JUDGEMENT IN ('2','3') ");

        stb.append(" ORDER BY ");
        stb.append("     KAI_PAGE_KEY, ");
        stb.append("     EXAMNO ");
        return stb.toString();
    }

    private class PrintData {
        final String _kaiPageKey;
        final String _testdivName;
        final String _dai1MajorName;
        final String _dai1CourseName;
        final String _examno;
        final String _finschoolName;
        final String _sexName;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;
        final String _score6;
        final String _scoreT;
        final String _conf3Rpt01;
        final String _conf3Rpt02;
        final String _conf3Rpt03;
        final String _conf3Rpt04;
        final String _conf3Rpt05;
        final String _conf3Rpt06;
        final String _conf3Rpt07;
        final String _conf3Rpt08;
        final String _conf3Rpt09;
        final String _conf3Total;
        final String _conf3Total100;
        final String _scoreT80;
        final String _judge;
        final String _dai2SucMajorName;
        final String _conf1Total;
        final String _conf2Total;
        final String _judgement;
        final String _remark;
        public PrintData(
                final String kaiPageKey,
                final String testdivName,
                final String dai1MajorName,
                final String dai1CourseName,
                final String examno,
                final String finschoolName,
                final String sexName,
                final String score1,
                final String score2,
                final String score3,
                final String score4,
                final String score5,
                final String score6,
                final String scoreT,
                final String conf3Rpt01,
                final String conf3Rpt02,
                final String conf3Rpt03,
                final String conf3Rpt04,
                final String conf3Rpt05,
                final String conf3Rpt06,
                final String conf3Rpt07,
                final String conf3Rpt08,
                final String conf3Rpt09,
                final String conf3Total,
                final String conf3Total100,
                final String scoreT80,
                final String judge,
                final String dai2SucMajorName,
                final String conf1Total,
                final String conf2Total,
                final String judgement,
                final String remark
        ) {
            _kaiPageKey = kaiPageKey;
            _testdivName = testdivName;
            _dai1MajorName = dai1MajorName;
            _dai1CourseName = dai1CourseName;
            _examno = examno;
            _finschoolName = finschoolName;
            _sexName = sexName;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _scoreT = scoreT;
            _conf3Rpt01 = conf3Rpt01;
            _conf3Rpt02 = conf3Rpt02;
            _conf3Rpt03 = conf3Rpt03;
            _conf3Rpt04 = conf3Rpt04;
            _conf3Rpt05 = conf3Rpt05;
            _conf3Rpt06 = conf3Rpt06;
            _conf3Rpt07 = conf3Rpt07;
            _conf3Rpt08 = conf3Rpt08;
            _conf3Rpt09 = conf3Rpt09;
            _conf3Total = conf3Total;
            _conf3Total100 = conf3Total100;
            _scoreT80 = scoreT80;
            _judge = judge;
            _dai2SucMajorName = dai2SucMajorName;
            _conf1Total = conf1Total;
            _conf2Total = conf2Total;
            _judgement = judgement;
            _remark = remark;
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
        log.fatal("$Revision: 59532 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamYear;
        private final String[] _selected;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useSchoolKindField;
        private final String _schoolkind;
        private final String _schoolCd;
        private final List _schoolList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _selected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useSchoolKindField = StringUtils.defaultString(request.getParameter("useSchool_KindField"));
            _schoolkind = StringUtils.defaultString(request.getParameter("SCHOOLKIND"));
            _schoolCd = StringUtils.defaultString(request.getParameter("SCHOOLCD"));
            _schoolList = getSchoolList(db2);
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

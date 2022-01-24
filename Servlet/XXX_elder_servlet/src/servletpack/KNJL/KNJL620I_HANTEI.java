/*
 * 作成日: 2020/12/14
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class KNJL620I_HANTEI {

    private static final Log log = LogFactory.getLog(KNJL620I_HANTEI.class);

    private boolean _hasData;

    private Param _param;

    private static final int HANTEI_SHIRYOU_LINE_MAX = 50;

    private static final int SUBTOTAL_LINE_MAX = 5;

    private static final Map<String, String> SUBJECT_MAP;

    /**
     * 左下に印字する学業の特待コードの取り揃え
     */
    private static final Map<String, String> GAKU_MAP;

    /**
     * 左下に印字する部の特待コードの取り揃え
     */
    private static final Map<String, String> BU_MAP;

    /**
     * 右下に印字する判別マークの取り揃え
     */
    private static final Map<String, String> JUDGE_MAP;

    static {
        SUBJECT_MAP = new HashMap<String, String>();
        SUBJECT_MAP.put("ALL", "全学科");
        SUBJECT_MAP.put("1", "普通科");
        SUBJECT_MAP.put("2", "工業科");

        GAKU_MAP = new LinkedHashMap<String, String>();
        GAKU_MAP.put("3", "SA");
        GAKU_MAP.put("4", "SB");
        GAKU_MAP.put("1", "学A");
        GAKU_MAP.put("2", "学B");

        BU_MAP = new LinkedHashMap<String, String>();
        BU_MAP.put("1", "部A");
        BU_MAP.put("2", "部B");
        BU_MAP.put("5", "部C");
        BU_MAP.put("6", "部P");

        JUDGE_MAP = new LinkedHashMap<String, String>();
        JUDGE_MAP.put("1", "Ⅰ特");
        JUDGE_MAP.put("2", "Ⅰ");
        JUDGE_MAP.put("3", "Ⅱ特");
        JUDGE_MAP.put("4", "Ⅱ");
        JUDGE_MAP.put("9", "否");
        JUDGE_MAP.put("5", "気SP");
        JUDGE_MAP.put("6", "気");
        JUDGE_MAP.put("7", "子SP");
        JUDGE_MAP.put("8", "子");
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String outputDate = sdf.format(new Date());

        List<String> testSubClassList = getTestSubClassList(db2);
        HanteiShiryou hanteiShiryou = getHanteiShiryou(db2);

        int pageCnt = 1;

        for (int i = 0; i < hanteiShiryou._hanteiShiryouPageList.size(); i++) {
            svf.VrSetForm("KNJL620I_1.frm", 4);

            HanteiShiryouPage hanteiShiryouPage = hanteiShiryou._hanteiShiryouPageList.get(i);

            setTitle(svf, pageCnt, outputDate);
            setHeader(svf, testSubClassList);
            setBody(svf, hanteiShiryouPage._applicantList);
            setSubTotal(svf, hanteiShiryouPage._gakuBu, hanteiShiryouPage._judgeMark, "");

            /**
             * 最終頁の処理
             */
            if (i == hanteiShiryou._hanteiShiryouPageList.size() - 1) {
                svf.VrEndRecord();
                setSubTotal(svf, hanteiShiryou._gakuBuTotal, hanteiShiryou._judgeTotal, "総合計");
            }
            svf.VrEndRecord();
            pageCnt++;
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("COURSE_NAME", _param._courseName);
        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　（" + SUBJECT_MAP.get(_param._majorcd) + "　" + _param._testDivName + "）　入　試　判　定　資　料");
    }

    private void setHeader(final Vrw32alp svf, final List<String> testSubClassList) {
        for (int i = 0; i < testSubClassList.size(); i++) {
            String testSubClass = testSubClassList.get(i);
            svf.VrsOut("CLASS_NAME" + (i + 1), testSubClass);
        }
    }

    private void setBody(final Vrw32alp svf, final List<Applicant> applicantList) {
        // 一覧の表示
        int lineCnt = 1;
        for (Applicant applicant : applicantList) {
            svf.VrsOutn("NO",      lineCnt, applicant._no);
            String receptno = applicant._receptno;
            if ("1".equals(applicant._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(applicant._name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, applicant._name);

            svf.VrsOutn("SEX", lineCnt, applicant._sex);

            final int finSchoolNameByte = KNJ_EditEdit.getMS932ByteLength(applicant._finschoolNameAbbv);
            final String finSchoolNameFieldStr = finSchoolNameByte > 24 ? "3" : finSchoolNameByte > 16 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL_NAME" + finSchoolNameFieldStr, lineCnt, applicant._finschoolNameAbbv);

            svf.VrsOutn("HOPE_DEPARTMENT",   lineCnt, applicant._hopeCource);
            svf.VrsOutn("CONSUL_DEPARTMENT", lineCnt, applicant._consultationCource);
            svf.VrsOutn("SCHOLAR",           lineCnt, applicant._honorName);
            svf.VrsOutn("HOPE_SCHOLAR",      lineCnt, applicant._scholarshipsDesire);
            svf.VrsOutn("FINSCHOOL_DIV",     lineCnt, applicant._averageAll);
            svf.VrsOutn("COMMON_TEST",       lineCnt, applicant._commonTest);
            svf.VrsOutn("ABSENCE",           lineCnt, applicant._absenceDays);
            svf.VrsOutn("PAST",              lineCnt, applicant._past);
            svf.VrsOutn("SCORE1",            lineCnt, applicant._score1);
            svf.VrsOutn("SCORE2",            lineCnt, applicant._score2);
            svf.VrsOutn("SCORE3",            lineCnt, applicant._score3);
            svf.VrsOutn("SCORE4",            lineCnt, applicant._score4);
            svf.VrsOutn("SCORE5",            lineCnt, applicant._score5);
            svf.VrsOutn("TOTAL_SCORE",       lineCnt, applicant._total4);
            svf.VrsOutn("DEPARTMENT_RANK",   lineCnt, applicant._totalRank1);
            svf.VrsOutn("COURSE_RANK",       lineCnt, applicant._totalRank3);
            svf.VrsOutn("PRE_JUDGE",         lineCnt, applicant._provisionalJudgeName);
            svf.VrsOutn("JUDGE_MARK",        lineCnt, applicant._judgeName);

            lineCnt++;
            _hasData = true;
        }
    }

    private void setSubTotal(final Vrw32alp svf, final GakuBu gakuBu, final Judge judge, String totalName) {
        svf.VrsOut("TOTAL_NAME", totalName);

        // 左下の「学」の表示
        int lineCnt = 1;
        for (String gakuCd : GAKU_MAP.keySet()) {
            String gakuName = GAKU_MAP.get(gakuCd);
            final int gakuNameByte = KNJ_EditEdit.getMS932ByteLength(gakuName);
            final String gakuNameFieldStr = gakuNameByte > 4 ? "_2" : "";
            svf.VrsOutn("TOTAL_NAME1" + gakuNameFieldStr, lineCnt, gakuName);

            Integer gakuCount = gakuBu._gakuCountMap.get(gakuCd);
            if (gakuCount == null) {
                gakuCount = 0;
            }
            svf.VriOutn("TOTAL_NUM1", lineCnt, gakuCount);

            lineCnt++;
          }

        // 左下の「その他」の表示
        svf.VrsOutn("TOTAL_NAME1_2", 5, gakuBu._otherName);
        svf.VrsOutn("TOTAL_NUM1",    5, gakuBu._otherCount.toString());

        // 左下の「部」の表示
        lineCnt = 1;
        for (String buCd : BU_MAP.keySet()) {
            String buName = BU_MAP.get(buCd);
            final int buNameByte = KNJ_EditEdit.getMS932ByteLength(buName);
            final String buNameFieldStr = buNameByte > 4 ? "_2" : "";
            svf.VrsOutn("TOTAL_NAME2" + buNameFieldStr, lineCnt, buName);

            Integer buCount = gakuBu._buCountMap.get(buCd);
            if (buCount == null) {
                buCount = 0;
            }
            svf.VriOutn("TOTAL_NUM2", lineCnt, buCount);

            lineCnt++;
        }

        // 右下の判定マークの表示
        lineCnt = 1;
        for (String judgeCd : JUDGE_MAP.keySet()) {
            int judgeLineCnt = lineCnt <= 5 ? lineCnt : lineCnt - SUBTOTAL_LINE_MAX;
            int judgeColCnt = lineCnt <= 5 ? 1 : 2;
            String judgeMark = JUDGE_MAP.get(judgeCd);
            svf.VrsOutn("TOTAL_MARK" + judgeColCnt, judgeLineCnt, judgeMark);

            Integer judgeMarkCount = judge._judgeCountMap.get(judgeCd);
            if (judgeMarkCount == null) {
                judgeMarkCount = 0;
            }
            svf.VriOutn("TOTAL_MARK_NUM" + judgeColCnt, judgeLineCnt, judgeMarkCount);

            lineCnt++;
        }
    }

    private List<String> getTestSubClassList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<String> testSubClassList = new ArrayList<String>();

        try {
            final String testSubClassSal = getTestSubClassSql();
            log.debug(" sql =" + testSubClassSal);
            ps = db2.prepareStatement(testSubClassSal);
            rs = ps.executeQuery();

            int lineCnt = 1;

            while (rs.next()) {
                if (lineCnt > 5) {
                    break;
                }

                String abbv1 = rs.getString("ABBV1");
                testSubClassList.add(abbv1);
                lineCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return testSubClassList;
    }

    private HanteiShiryou getHanteiShiryou(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        HanteiShiryou hanteiShiryou = new HanteiShiryou(new ArrayList<HanteiShiryouPage>(), new GakuBu(), new Judge());

        HanteiShiryouPage hanteiShiryouPage = null;
        List<Applicant> applicantList = new ArrayList<Applicant>();
        GakuBu gakubu = new GakuBu();
        Judge judge = new Judge();

        try {
            final String hanteiShiryouSql = getHanteiShiryouSql();
            log.debug(" sql =" + hanteiShiryouSql);
            ps = db2.prepareStatement(hanteiShiryouSql);
            rs = ps.executeQuery();

            int lineCnt = 1;

            while (rs.next()) {
                String no = rs.getString("ROW_NUMBER");
                String receptno = rs.getString("RECEPTNO");
                String name = rs.getString("NAME");
                String sex = rs.getString("SEX");
                String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                String hopeCource = rs.getString("HOPE_COURCE");
                String consultaionCource = rs.getString("CONSULTATION_COURCE");
                String honorName = rs.getString("HONOR_NAME");
                String scholarshipsDesire = rs.getString("SCHOLARSHIPS_DESIRE");
                String averageAll = rs.getString("AVERAGE_ALL");
                String commonTest = rs.getString("COMMON_TEST");
                String absenceDays = rs.getString("ABSENCE_DAYS");
                String past = rs.getString("PAST");
                String score1 = rs.getString("SCORE1");
                String score2 = rs.getString("SCORE2");
                String score3 = rs.getString("SCORE3");
                String score4 = rs.getString("SCORE4");
                String score5 = rs.getString("SCORE5");
                String total4 = rs.getString("TOTAL4");
                String totalRank1 = rs.getString("TOTAL_RANK1");
                String totalRank3 = rs.getString("TOTAL_RANK3");
                String judgeCource = rs.getString("JUDGE_COURCE");
                String honordiv = rs.getString("HONORDIV");
                String honorReasondiv = rs.getString("HONOR_REASONDIV");
                String provisionalJudgeName = rs.getString("PROVISIONAL_JUDGE_NAME");
                String judgeName = rs.getString("JUDGE_NAME");
                String hopeMajorcd = rs.getString("HOPE_MAJORCD");
                String duplicateFlg = rs.getString("DUPLICATE_FLG");

                Applicant applicant = new Applicant(no, receptno, name, sex, finschoolNameAbbv, hopeCource, consultaionCource, honorName, scholarshipsDesire, averageAll, commonTest, absenceDays, past, score1, score2, score3, score4, score5, total4, totalRank1, totalRank3, judgeCource, honordiv, honorReasondiv, provisionalJudgeName, judgeName, hopeMajorcd, duplicateFlg);
                applicantList.add(applicant);
                hanteiShiryou._gakuBuTotal.put(honorReasondiv, honordiv);
                hanteiShiryou._judgeTotal.put(judgeCource, provisionalJudgeName);
                gakubu.put(honorReasondiv, honordiv);
                judge.put(judgeCource, provisionalJudgeName);

                if (lineCnt == 1) {
                    hanteiShiryouPage = new HanteiShiryouPage(applicantList, gakubu, judge);
                    hanteiShiryou._hanteiShiryouPageList.add(hanteiShiryouPage);
                    lineCnt++;

                } else if (lineCnt >= HANTEI_SHIRYOU_LINE_MAX) {
                    applicantList = new ArrayList<Applicant>();
                    gakubu = new GakuBu();
                    judge = new Judge();
                    lineCnt = 1;
                } else {
                    lineCnt++;
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hanteiShiryou;
    }

    private String getHanteiShiryouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH L009 AS ( ");
        stb.append("     SELECT ");
        stb.append("         ROWNUMBER() OVER() AS ROW_NUMBER, ");
        stb.append("         SEQ ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SETTING_MST ");
        stb.append("     WHERE ");
        stb.append("         ENTEXAMYEAR  = '" + _param._entexamyear + "' AND ");
        stb.append("         APPLICANTDIV = '" + _param._applicantDiv + "'    AND ");
        stb.append("         SETTING_CD   = 'L009' ");
        stb.append("     ORDER BY ");
        stb.append("         VALUE(SEQ, 0) ");
        stb.append(" ), ");
        stb.append(" SCORE1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE.ENTEXAMYEAR, ");
        stb.append("         SCORE.APPLICANTDIV, ");
        stb.append("         SCORE.TESTDIV, ");
        stb.append("         SCORE.EXAM_TYPE, ");
        stb.append("         SCORE.RECEPTNO, ");
        stb.append("         SCORE.SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT AS SCORE ");
        stb.append("         INNER JOIN L009 ON ");
        stb.append("                    L009.ROW_NUMBER = 1 ");
        stb.append("                AND SEQ             = SCORE.TESTSUBCLASSCD ");
        stb.append(" ), ");
        stb.append(" SCORE2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE.ENTEXAMYEAR, ");
        stb.append("         SCORE.APPLICANTDIV, ");
        stb.append("         SCORE.TESTDIV, ");
        stb.append("         SCORE.EXAM_TYPE, ");
        stb.append("         SCORE.RECEPTNO, ");
        stb.append("         SCORE.SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT AS SCORE ");
        stb.append("         INNER JOIN L009 ON ");
        stb.append("                    L009.ROW_NUMBER = 2 ");
        stb.append("                AND SEQ             = SCORE.TESTSUBCLASSCD ");
        stb.append(" ), ");
        stb.append(" SCORE3 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE.ENTEXAMYEAR, ");
        stb.append("         SCORE.APPLICANTDIV, ");
        stb.append("         SCORE.TESTDIV, ");
        stb.append("         SCORE.EXAM_TYPE, ");
        stb.append("         SCORE.RECEPTNO, ");
        stb.append("         SCORE.SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT AS SCORE ");
        stb.append("         INNER JOIN L009 ON ");
        stb.append("                    L009.ROW_NUMBER = 3 ");
        stb.append("                AND SEQ             = SCORE.TESTSUBCLASSCD ");
        stb.append(" ), ");
        stb.append(" SCORE4 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE.ENTEXAMYEAR, ");
        stb.append("         SCORE.APPLICANTDIV, ");
        stb.append("         SCORE.TESTDIV, ");
        stb.append("         SCORE.EXAM_TYPE, ");
        stb.append("         SCORE.RECEPTNO, ");
        stb.append("         SCORE.SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT AS SCORE ");
        stb.append("         INNER JOIN L009 ON ");
        stb.append("                    L009.ROW_NUMBER = 4 ");
        stb.append("                AND SEQ             = SCORE.TESTSUBCLASSCD ");
        stb.append(" ), ");
        stb.append(" SCORE5 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE.ENTEXAMYEAR, ");
        stb.append("         SCORE.APPLICANTDIV, ");
        stb.append("         SCORE.TESTDIV, ");
        stb.append("         SCORE.EXAM_TYPE, ");
        stb.append("         SCORE.RECEPTNO, ");
        stb.append("         SCORE.SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT AS SCORE ");
        stb.append("         INNER JOIN L009 ON ");
        stb.append("                    L009.ROW_NUMBER = 5 ");
        stb.append("                AND SEQ             = SCORE.TESTSUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        /**
         * 一覧の情報を取得
         */
        stb.append("     ROWNUMBER() OVER() AS ROW_NUMBER, ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     FS.FINSCHOOL_NAME_ABBV, ");
        stb.append("     VALUE(BASE_D031.REMARK1, '0') || VALUE(BASE_D031.REMARK2, '0') || VALUE(BASE_D031.REMARK3, '0') || VALUE(BASE_D031.REMARK4, '0') AS HOPE_COURCE, ");
        stb.append("     CONSULTATION_COURCE.GENERAL_ABBV AS CONSULTATION_COURCE, ");
        stb.append("     VALUE(HONOR1.GENERAL_MARK, '') || VALUE(HONOR2.GENERAL_MARK, '') AS HONOR_NAME, ");
        stb.append("     CASE BASE_D031.REMARK6 WHEN '1' THEN '●' ELSE NULL END AS SCHOLARSHIPS_DESIRE, ");
        if (_param._3yearsKyoukaNum > 0) {
            stb.append("     ROUND((VALUE(CONFD001.REMARK10, 0.0) + VALUE(CONFD002.REMARK10, 0.0) + VALUE(CON.TOTAL_ALL, 0.0)) / " + _param._3yearsKyoukaNum + ", 1) AS AVERAGE_ALL, ");
        } else {
            stb.append("     NULL AS AVERAGE_ALL, ");
        }
        stb.append("     CON_D009.REMARK4 AS COMMON_TEST, ");
        stb.append("     VALUE(CON.ABSENCE_DAYS, 0) + VALUE(CON.ABSENCE_DAYS2, 0) + VALUE(CON.ABSENCE_DAYS3, 0) AS ABSENCE_DAYS, ");
        stb.append("     CASE WHEN BASE_D031.REMARK5 = '1' THEN '1' ELSE '' END PAST, ");
        stb.append("     SCORE1.SCORE AS SCORE1, ");
        stb.append("     SCORE2.SCORE AS SCORE2, ");
        stb.append("     SCORE3.SCORE AS SCORE3, ");
        stb.append("     SCORE4.SCORE AS SCORE4, ");
        stb.append("     SCORE5.SCORE AS SCORE5, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL_RANK1, ");
        stb.append("     RECEPT.TOTAL_RANK3, ");
        stb.append("     RECEPT_D015.REMARK1 AS JUDGE_COURCE, ");
        stb.append("     PROVISIONAL_JUDGE.GENERAL_NAME AS PROVISIONAL_JUDGE_NAME, ");
        stb.append("     JUDGE.GENERAL_NAME AS JUDGE_NAME, ");
        /**
         * フッタの集計用の情報を取得
         */
        stb.append("     HONOR1.GENERAL_CD AS HONOR_REASONDIV, ");
        stb.append("     HONOR2.GENERAL_CD AS HONORDIV, ");
        /**
         * その他
         */
        stb.append("     BASE.TESTDIV0 AS HOPE_MAJORCD, ");
        stb.append("     CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("          ELSE NULL ");
        stb.append("     END AS DUPLICATE_FLG "); //他入試区分を受験しているかのフラグ

        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT AS RECEPT ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT AS BASE ON ");
        stb.append("               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");

        stb.append("     LEFT JOIN V_NAME_MST AS Z002 ON ");
        stb.append("               Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN FINSCHOOL_MST AS FS ON ");
        stb.append("               FS.FINSCHOOLCD = BASE.FS_CD ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AS BASE_D012 ON ");
        stb.append("               BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BASE_D012.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BASE_D012.SEQ          = '012' ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AS BASE_D031 ON ");
        stb.append("               BASE_D031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BASE_D031.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BASE_D031.SEQ          = '031' ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT AS CON ON ");
        stb.append("               CON.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND CON.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND CON.EXAMNO       = BASE.EXAMNO ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT AS CONFD001 ON ");
        stb.append("               CONFD001.ENTEXAMYEAR  = CON.ENTEXAMYEAR ");
        stb.append("           AND CONFD001.APPLICANTDIV = CON.APPLICANTDIV ");
        stb.append("           AND CONFD001.EXAMNO       = CON.EXAMNO ");
        stb.append("           AND CONFD001.SEQ          = '001' ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT AS CONFD002 ON ");
        stb.append("               CONFD002.ENTEXAMYEAR  = CON.ENTEXAMYEAR ");
        stb.append("           AND CONFD002.APPLICANTDIV = CON.APPLICANTDIV ");
        stb.append("           AND CONFD002.EXAMNO       = CON.EXAMNO ");
        stb.append("           AND CONFD002.SEQ          = '002' ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT AS CON_D009 ON ");
        stb.append("               CON_D009.ENTEXAMYEAR  = CON.ENTEXAMYEAR ");
        stb.append("           AND CON_D009.APPLICANTDIV = CON.APPLICANTDIV ");
        stb.append("           AND CON_D009.EXAMNO       = CON.EXAMNO ");
        stb.append("           AND CON_D009.SEQ          = '009' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST AS CONSULTATION_COURCE ON ");
        stb.append("               CONSULTATION_COURCE.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ");
        stb.append("           AND CONSULTATION_COURCE.APPLICANTDIV = CON_D009.APPLICANTDIV ");
        stb.append("           AND CONSULTATION_COURCE.TESTDIV      = '0' ");  // '0' 固定
        stb.append("           AND CONSULTATION_COURCE.GENERAL_DIV  = '02' "); // '02':コース 固定
        stb.append("           AND CONSULTATION_COURCE.GENERAL_CD   = CON_D009.REMARK1 ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST AS HONOR1 ON ");
        stb.append("               HONOR1.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ");
        stb.append("           AND HONOR1.APPLICANTDIV = CON_D009.APPLICANTDIV ");
        stb.append("           AND HONOR1.TESTDIV      = '0' ");  // '0' 固定
        stb.append("           AND HONOR1.GENERAL_DIV  = '05' "); // '05':特待理由コード 固定
        stb.append("           AND HONOR1.GENERAL_CD   = CON_D009.REMARK3 ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST AS HONOR2 ON ");
        stb.append("               HONOR2.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ");
        stb.append("           AND HONOR2.APPLICANTDIV = CON_D009.APPLICANTDIV ");
        stb.append("           AND HONOR2.TESTDIV      = '0' ");  // '0' 固定
        stb.append("           AND HONOR2.GENERAL_DIV  = '04' "); // '04':特待コード 固定
        stb.append("           AND HONOR2.GENERAL_CD   = CON_D009.REMARK2 ");

        stb.append("     LEFT JOIN SCORE1 ON ");
        stb.append("               SCORE1.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE1.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND SCORE1.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE1.RECEPTNO     = RECEPT.RECEPTNO ");

        stb.append("     LEFT JOIN SCORE2 ON ");
        stb.append("               SCORE2.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE2.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE2.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND SCORE2.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE2.RECEPTNO     = RECEPT.RECEPTNO ");

        stb.append("     LEFT JOIN SCORE3 ON ");
        stb.append("               SCORE3.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE3.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE3.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND SCORE3.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE3.RECEPTNO     = RECEPT.RECEPTNO ");

        stb.append("     LEFT JOIN SCORE4 ON ");
        stb.append("               SCORE4.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE4.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE4.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND SCORE4.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE4.RECEPTNO     = RECEPT.RECEPTNO ");

        stb.append("     LEFT JOIN SCORE5 ON ");
        stb.append("               SCORE5.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE5.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE5.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND SCORE5.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE5.RECEPTNO     = RECEPT.RECEPTNO ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT AS RECEPT_D015 ON ");
        stb.append("               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D015.SEQ          = '015' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST PROVISIONAL_JUDGE ON ");
        stb.append("               PROVISIONAL_JUDGE.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND PROVISIONAL_JUDGE.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND PROVISIONAL_JUDGE.TESTDIV      = '0' ");  // '0' 固定
        stb.append("           AND PROVISIONAL_JUDGE.GENERAL_DIV  = '03' "); // '03'：判定マーク 固定
        stb.append("           AND PROVISIONAL_JUDGE.GENERAL_CD   = RECEPT_D015.REMARK1 ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST JUDGE ON ");
        stb.append("               JUDGE.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND JUDGE.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND JUDGE.TESTDIV      = '0' ");  // '0' 固定
        stb.append("           AND JUDGE.GENERAL_DIV  = '03' "); // '03'：判定マーク 固定
        stb.append("           AND JUDGE.GENERAL_CD   = RECEPT_D015.REMARK3 ");

        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" AND RECEPT.EXAM_TYPE    = '1' ");
        if (!"ALL".equals(_param._majorcd)) {
            stb.append(" AND BASE.TESTDIV0       = '" + _param._majorcd + "' ");
        }
        stb.append(" AND BASE_D031.REMARK1  IN " + _param._whereInChkCoursesStr + " ");

        stb.append(" ORDER BY ");
        if ("2".equals(_param._outputSort)) {
            stb.append("     VALUE(RECEPT.TOTAL4, -1) DESC, ");
        }
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getTestSubClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ABBV1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SETTING_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND SETTING_CD   = 'L009' ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(SEQ, 0) ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class HanteiShiryou {
        private List<HanteiShiryouPage> _hanteiShiryouPageList;
        private GakuBu _gakuBuTotal;
        private Judge _judgeTotal;

        HanteiShiryou (List<HanteiShiryouPage> hanteiShiryouPageList, GakuBu gakuBuTotal, Judge judgeTotal) {
            _hanteiShiryouPageList = hanteiShiryouPageList;
            _gakuBuTotal = gakuBuTotal;
            _judgeTotal = judgeTotal;
        }
    }

    private class HanteiShiryouPage {
        private List<Applicant> _applicantList;
        private GakuBu _gakuBu;
        private Judge _judgeMark;

        HanteiShiryouPage (List<Applicant> applicantList, GakuBu gakuBu, Judge judgeMark) {
            _applicantList = applicantList;
            _gakuBu = gakuBu;
            _judgeMark = judgeMark;
        }
    }

    private class Applicant {
        private final String _no;
        private final String _receptno;
        private final String _name;
        private final String _sex;
        private final String _finschoolNameAbbv;
        private final String _hopeCource;
        private final String _consultationCource;
        private final String _honorName;
        private final String _scholarshipsDesire;
        private final String _averageAll;
        private final String _commonTest;
        private final String _absenceDays;
        private final String _past;
        private final String _score1;
        private final String _score2;
        private final String _score3;
        private final String _score4;
        private final String _score5;
        private final String _total4;
        private final String _totalRank1;
        private final String _totalRank3;
        private final String _judgeCource;
        private final String _honordiv;
        private final String _honorReasondiv;
        private final String _provisionalJudgeName;
        private final String _judgeName;
        private final String _hopeMajorcd;
        private final String _duplicateFlg;

        Applicant(
            String no,
            String receptno,
            String name,
            String sex,
            String finschoolName,
            String hopeCource,
            String consultationCource,
            String honorName,
            String scholarshipsDesire,
            String averageAll,
            String commonTest,
            String absenceDays,
            String past,
            String score1,
            String score2,
            String score3,
            String score4,
            String score5,
            String total4,
            String totalRank1,
            String totalRank3,
            String judgeCource,
            String honordiv,
            String honorReasondiv,
            String provisionalJudgeName,
            String judgeName,
            String hopeMajorcd,
            String duplicateFlg
        ) {
            _no = no;
            _receptno = receptno;
            _name = name;
            _sex = sex;
            _finschoolNameAbbv = finschoolName;
            _hopeCource = hopeCource;
            _consultationCource = consultationCource;
            _honorName = honorName;
            _scholarshipsDesire = scholarshipsDesire;
            _averageAll = averageAll;
            _commonTest = commonTest;
            _absenceDays = absenceDays;
            _past = past;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _total4 = total4;
            _totalRank1 = totalRank1;
            _totalRank3 = totalRank3;
            _judgeCource = judgeCource;
            _honordiv = honordiv;
            _honorReasondiv = honorReasondiv;
            _provisionalJudgeName = provisionalJudgeName;
            _judgeName = judgeName;
            _hopeMajorcd = hopeMajorcd;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class GakuBu {
        private Map<String, String> _gakuNameMap;
        private Map<String, Integer> _gakuCountMap;

        private Map<String, String> _buNameMap;
        private Map<String, Integer> _buCountMap;

        private String _otherName;
        private Integer _otherCount;

        GakuBu() {
            _gakuNameMap = new LinkedHashMap<String, String>();
            _gakuCountMap = new LinkedHashMap<String, Integer>();
            _buNameMap = new LinkedHashMap<String, String>();
            _buCountMap = new LinkedHashMap<String, Integer>();

            _otherName = "その他";
            _otherCount = 0;
        }

        void put(String honorReasondiv, String honordiv) {
            Map<String, Integer> gakuBuCountMap = null;

            if (honorReasondiv != null) {
                if (honorReasondiv.equals("01")) {
                    if (GAKU_MAP.containsKey(honordiv)) {
                        gakuBuCountMap = _gakuCountMap;
                    }
                } else {
                    if ("3".equals(honordiv) || "4".equals(honordiv)) {
                        // 3:SA、4:SBは学業として扱う
                        gakuBuCountMap = _gakuCountMap;
                    } else {
                        if (BU_MAP.containsKey(honordiv)) {
                            gakuBuCountMap = _buCountMap;
                        }
                    }
                }

                if (gakuBuCountMap != null) {
                    if (gakuBuCountMap.containsKey(honordiv)) {
                        /**
                         *  既に登録済みなら人数をカウントアップする
                         *  注：出力領域は4個しかないが、実運用では4個以下しかないとのことなので、
                         *  　　4個以上の場合の制御は行わない。
                         */
                        int cnt = gakuBuCountMap.get(honordiv) + 1;
                        gakuBuCountMap.put(honordiv, cnt);
                    } else {
                        gakuBuCountMap.put(honordiv, 1);
                    }
                } else {
                    // 決められたコードの組み合わせ以外は”その他”の人数でカウントする
                    _otherCount++;
                }
            } else {
                // 特待以外は”その他”の人数でカウントする
                _otherCount++;
            }
        }
    }

    private class Judge {
        private Map<String, String> _judgeMarkMap;
        private Map<String, Integer> _judgeCountMap;

        Judge() {
            _judgeMarkMap = new LinkedHashMap<String, String>();
            _judgeCountMap = new LinkedHashMap<String, Integer>();
        }

        void put(String judgecd, String judgeMark) {
            if (judgecd != null) {
                if (_judgeCountMap.containsKey(judgecd)) {
                    /**
                     *  既に登録済みなら人数をカウントアップする
                     *  注：出力領域は10個しかないが、実運用では10個以下しかないとのことなので、
                     *  　　11個以上の場合の制御は行わない。
                     */
                    int cnt = _judgeCountMap.get(judgecd) + 1;
                    _judgeCountMap.put(judgecd, cnt);
                } else {
                    _judgeMarkMap.put(judgecd, judgeMark);
                    _judgeCountMap.put(judgecd, 1);
                }
            }
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _majorcd;
        private final String _testDiv;
        private final String _outputSort;
        private final String _testDivName;
        private String _courseName;
        private final String[] _chkCourses;
        private String _whereInChkCoursesStr;
        private Map _confrptKyoukaMap;
        private final int _3yearsKyoukaNum;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = "2"; // 2:高校 固定
            _majorcd = request.getParameter("MAJORCD");
            _testDiv = request.getParameter("TESTDIV");
            _outputSort = request.getParameter("OUTPUT_SORT");
            _testDivName = getTestDivName(db2);
            _chkCourses = request.getParameterValues("OUTPUT_CHK[]");
            _whereInChkCoursesStr = "";
            if (_chkCourses.length > 0) {
                _whereInChkCoursesStr = "('" + StringUtils.join(_chkCourses, "','") + "')";
            }

            //指定したコースが1つのみの場合コース名を取得
            _courseName = "";
            if (_chkCourses.length == 1) {
                _courseName = getCourseName(db2, _chkCourses[0]);
            }

            _confrptKyoukaMap = getConfrptKyouka(db2);
            _3yearsKyoukaNum = _confrptKyoukaMap.size() * 3;
        }

        private String getTestDivName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV='" + _applicantDiv + "' AND TESTDIV='" + _testDiv + "' ";
            log.debug(" sql =" + sql);

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

        private String getCourseName(final DB2UDB db2, final String courseCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("       GENERAL_NAME AS COURSE_NAME");
            stb.append("   FROM ");
            stb.append("       ENTEXAM_GENERAL_MST ");
            stb.append("   WHERE ");
            stb.append("       ENTEXAMYEAR      = '" + _entexamyear + "' ");
            stb.append("       AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("       AND TESTDIV 		= '0' ");
            stb.append("       AND GENERAL_DIV 	= '02' ");
            stb.append("       AND GENERAL_CD 	= '" + courseCd  + "' ");

            String sql = stb.toString();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("COURSE_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        //内申の教科取得
        private Map getConfrptKyouka(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtn = new LinkedHashMap();

            StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("       SEQ, ");
            stb.append("       NAME1 ");
            stb.append("   FROM ");
            stb.append("       ENTEXAM_SETTING_MST ");
            stb.append("   WHERE ");
            stb.append("       ENTEXAMYEAR      = '" + _entexamyear + "' ");
            stb.append("       AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("       AND SETTING_CD 	= 'L008' ");
            stb.append("   ORDER BY ");
            stb.append("       SEQ ");

            String sql = stb.toString();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEQ"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof


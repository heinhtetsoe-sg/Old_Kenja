/*
 * $Id: 3899d1bdb1831469ca2948b456311511599b9763 $
 *
 * 作成日: 2020/09/17
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL434I {

    private static final Log log = LogFactory.getLog(KNJL434I.class);

    private boolean _hasData;

    private Param _param;

    private static final int _maxLine = 25;

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
        svf.VrSetForm("KNJL434I.frm", 1);
        final Map<String, PrintHeaderData> headerMap = getHeaderData(db2);
        final List<Map<String, Map<String, PrintBodyData>>> bodyList = getBodyList(db2);

        int pageCnt = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String outputDate = sdf.format(new Date());

        // 一ページ以内に表示する受験者情報の単位（２５人／頁）で繰り返す。
        for (Map<String, Map<String, PrintBodyData>> refMap : bodyList) {
            setTitle(svf, pageCnt, outputDate);
            setHeader(svf, headerMap);
            setBody(svf, refMap, headerMap, pageCnt);

            svf.VrEndPage();
            pageCnt++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        final String sex = "3".equals(_param._sex) ? "男女共" : "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "";
        svf.VrsOut("TITLE", _param._entexamyear + "年度  入学試験  成績原簿  （" + _param._testDivName + "）  （" + sex + "）");

        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
    }

    private void setHeader(final Vrw32alp svf, final Map<String, PrintHeaderData> headerMap) {
        for (PrintHeaderData headerData : headerMap.values()) {
            svf.VrsOut("CLASS_NAME" + headerData._colNumber, headerData._testClassName);
        }
    }

    private void setBody(final Vrw32alp svf, final Map<String, Map<String, PrintBodyData>> refMap, Map<String, PrintHeaderData> headerMap, final int pageCnt) {
        int lineCnt = 1;
        int noCnt = ((pageCnt - 1) * _maxLine) + 1;

        for (Map<String, PrintBodyData> subMap : refMap.values()) {
            svf.VrsOutn("NO", lineCnt, String.valueOf(noCnt));

            String preReceptNo = "";
            for (PrintBodyData printBody : subMap.values()) {
                svf.VrsOutn("EXAM_NO1", lineCnt, printBody._receptNo);
                svf.VrsOutn("SEX", lineCnt, printBody._sex);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(printBody._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printBody._name);

                if (headerMap.containsKey(printBody._testsubclassCd)) {
                    svf.VrsOutn("SCORE" + headerMap.get(printBody._testsubclassCd)._colNumber, lineCnt, printBody._score);
                }

                svf.VrsOutn("SUBTOTAL", lineCnt, printBody._total4);

                if (!"2".equals(printBody._remark1) && !preReceptNo.equals(printBody._receptNo)) {
                    svf.VrsOutn("PLUS", lineCnt, printBody._addScore);
                }

                svf.VrsOutn("TOTAL", lineCnt, printBody._total1);
                svf.VrsOutn("COUNT", lineCnt, printBody._hopeCnt);
                svf.VrsOutn("RANK", lineCnt, printBody._totalRank1);

                final String interview = "4".equals(printBody._judgeDiv) ? "欠席" : StringUtils.defaultString(printBody._interviewA) + StringUtils.defaultString(printBody._interviewB) + StringUtils.defaultString(printBody._interviewC);
                svf.VrsOutn("INTERVIEW", lineCnt, interview);

                svf.VrsOutn("DIV1", lineCnt, NumberUtils.isNumber(printBody._plus) && Double.parseDouble(printBody._plus) == 0.0 ? "" : printBody._plus);
                final String minusDiv = (NumberUtils.isNumber(printBody._minus) && Double.parseDouble(printBody._minus) > 0.0) ? "●" : (NumberUtils.isNumber(printBody._plus) && Double.parseDouble(printBody._plus) > 0.0) ? "〇" : ""; // ●が優先
                svf.VrsOutn("DIV2", lineCnt, minusDiv);
                svf.VrsOutn("DIV3", lineCnt, (!NumberUtils.isNumber(printBody._minus) || Double.parseDouble(printBody._minus) == 0.0 ? "" : "-" + StringUtils.defaultString(printBody._minus)));

                String judgeStr = StringUtils.defaultString(printBody._judgeDivValue) + StringUtils.defaultString(printBody._judgeKindValue);
                svf.VrsOutn("JUDGE", lineCnt, judgeStr);

                if ("2".equals(printBody._remark1)) {
                    svf.VrsOutn("HOPE", lineCnt, printBody._remark1);
                }

                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(printBody._finschoolName);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + finschoolNameFieldStr, lineCnt, printBody._finschoolName);

                svf.VrsOutn("EXAM_NO2", lineCnt, printBody._recomExamNo);

                preReceptNo = printBody._receptNo;
            }

            noCnt++;
            lineCnt++;
            _hasData = true;
        }
    }

    private Map<String, PrintHeaderData> getHeaderData(final DB2UDB db2) {
        Map<String, PrintHeaderData> headerData = new LinkedHashMap<String, PrintHeaderData>();
        int colCnt = 1;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String headerSql = getHeaderSql();
            log.debug(" sql =" + headerSql);
            ps = db2.prepareStatement(headerSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testSubClassCd = rs.getString("TESTSUBCLASSCD");
                final String testSubClassName = rs.getString("TESTSUBCLASSNAME");

                final PrintHeaderData printHeaderData = new PrintHeaderData(testSubClassName, colCnt);
                headerData.put(testSubClassCd, printHeaderData);
                colCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return headerData;
    }

    private List<Map<String, Map<String, PrintBodyData>>> getBodyList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Map<String, Map<String, PrintBodyData>>> bodyList = new ArrayList<Map<String, Map<String, PrintBodyData>>>();
        Map<String, Map<String, PrintBodyData>> retMap = new LinkedHashMap<String, Map<String, PrintBodyData>>();
        Map<String, PrintBodyData> subMap = null;

        try {
            final String bodySql = getBodySql();
            log.debug(" sql =" + bodySql);
            ps = db2.prepareStatement(bodySql);
            rs = ps.executeQuery();

            bodyList.add(retMap);
            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String sex = rs.getString("SEX");
                final String name = rs.getString("NAME");
                final String testsubclassCd = rs.getString("TESTSUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String total4 = rs.getString("TOTAL4");
                final String addScore = rs.getString("ADD_SCORE");
                final String total1 = rs.getString("TOTAL1");
                final String hopeCnt = rs.getString("HOPE_CNT");
                final String totalRank1 = rs.getString("TOTAL_RANK1");
                final String remark1 = rs.getString("REMARK1");
                final String interviewA = rs.getString("INTERVIEW_A");
                final String interviewB = rs.getString("INTERVIEW_B");
                final String interviewC = rs.getString("INTERVIEW_C");
                final String plus = rs.getString("PLUS");
                final String minus = rs.getString("MINUS");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String judgeDivValue = rs.getString("JUDGEDIV_VALUE");
                final String judgeKindValue = rs.getString("JUDGE_KIND_VALUE");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                String recomExamNo = rs.getString("RECOM_EXAMNO");
                if ("1".equals(_param._testDiv)) {
                    if (null == recomExamNo) {
                        recomExamNo = rs.getString("EXAMNO2");
                    }
                }

                final PrintBodyData bodyData = new PrintBodyData(receptNo, sex, name, testsubclassCd, score, total4, addScore, total1, hopeCnt, totalRank1, remark1, interviewA, interviewB, interviewC, plus, minus, judgeDiv, judgeDivValue, judgeKindValue, finschoolName, recomExamNo);
                if (!retMap.containsKey(receptNo)) {
                    if (retMap.size() > _maxLine) {
                        retMap = new LinkedHashMap<String, Map<String, PrintBodyData>>();
                        bodyList.add(retMap);
                    }
                    subMap = new LinkedHashMap<String, PrintBodyData>();
                    retMap.put(receptNo, subMap);
                } else {
                    subMap = retMap.get(receptNo);
                }
                subMap.put(testsubclassCd, bodyData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return bodyList;
    }

    private String getHeaderSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUBCLASS.TESTSUBCLASSCD, ");
        stb.append("    L009.NAME1 AS TESTSUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT SUBCLASS ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L009 ON L009.SETTING_CD = 'L009' ");
        stb.append("          AND L009.ENTEXAMYEAR = SUBCLASS.ENTEXAMYEAR ");
        stb.append("          AND L009.APPLICANTDIV = SUBCLASS.APPLICANTDIV ");
        stb.append("          AND L009.SEQ = SUBCLASS.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     SUBCLASS.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND SUBCLASS.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND SUBCLASS.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND SUBCLASS.EXAM_TYPE = '1' ");
        stb.append("     AND L009.NAME1 IS NOT NULL ");
        stb.append("     AND L009.NAMESPARE2 IS NULL "); // 子科目は対象外
        stb.append(" ORDER BY ");
        stb.append("     VALUE(SUBCLASS.TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    private String getBodySql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HOPE AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.ENTEXAMYEAR, ");
        stb.append("     BASE.APPLICANTDIV, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     ROWNUMBER() OVER(ORDER BY BASE.EXAMNO) AS HOPE_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE_D.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND BASE_D.SEQ = '005' ");
        stb.append("          AND VALUE(BASE_D.REMARK1, '1') = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        /**
         * 抽出区分(OUTPUT_DIV)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     BASE.NAME, ");
        stb.append("     SCORE.TESTSUBCLASSCD, ");
        stb.append("     SCORE.SCORE, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     L014.NAME1 AS ADD_SCORE, ");
        stb.append("     RECEPT.TOTAL1, ");
        stb.append("     HOPE.HOPE_CNT, ");
        stb.append("     RECEPT.TOTAL_RANK1, ");
        stb.append("     BASE_D.REMARK1, ");
        stb.append("     LH27_A.NAME1 AS INTERVIEW_A, ");
        stb.append("     LH27_B.NAME1 AS INTERVIEW_B, ");
        stb.append("     LH27_C.NAME1 AS INTERVIEW_C, ");
        stb.append("     CONFR_D.REMARK1 AS PLUS, ");
        stb.append("     CONFR_D.REMARK2 AS MINUS, ");
        stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     L013.NAME1 AS JUDGEDIV_VALUE, ");
        stb.append("     L025.ABBV1 AS JUDGE_KIND_VALUE, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        if ("1".equals(_param._testDiv)) {
            stb.append("     L2.EXAMNO2, ");
        }
        stb.append("     BASE.RECOM_EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        if ("1".equals(_param._testDiv) || "3".equals(_param._testDiv)) {
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    RECOM_EXAMNO AS RECOM_EXAMNO2 ");
            stb.append("                  , MAX(EXAMNO) AS EXAMNO2 ");
            stb.append("                FROM ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                WHERE B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("                  AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("                  AND B1.TESTDIV = '2' ");
            stb.append("                  AND B1.RECOM_EXAMNO IS NOT NULL ");
            stb.append("                GROUP BY ");
            stb.append("                  B1.RECOM_EXAMNO ");
            stb.append("     ) L2 ON L2.RECOM_EXAMNO2 = BASE.EXAMNO ");
        }
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ON INTERVIEW.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND INTERVIEW.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND INTERVIEW.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ON SCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND SCORE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND SCORE.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("          AND SCORE.EXAM_TYPE = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L014 ON L014.SETTING_CD = 'L014' ");
        stb.append("          AND L014.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND L014.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND L014.SEQ = '01' ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_A ON LH27_A.SETTING_CD = 'LH27' ");
        stb.append("          AND LH27_A.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ");
        stb.append("          AND LH27_A.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("          AND LH27_A.SEQ = INTERVIEW.INTERVIEW_A ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_B ON LH27_B.SETTING_CD = 'LH27' ");
        stb.append("          AND LH27_B.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ");
        stb.append("          AND LH27_B.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("          AND LH27_B.SEQ = INTERVIEW.INTERVIEW_B ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_C ON LH27_C.SETTING_CD = 'LH27' ");
        stb.append("          AND LH27_C.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ");
        stb.append("          AND LH27_C.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("          AND LH27_C.SEQ = INTERVIEW.INTERVIEW_C ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFR_D ON CONFR_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND CONFR_D.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND CONFR_D.EXAMNO = RECEPT.EXAMNO ");
        stb.append("          AND CONFR_D.SEQ = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("          AND L013.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND L013.SEQ = RECEPT.JUDGEDIV ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L025 ON L025.SETTING_CD = 'L025' ");
        stb.append("          AND L025.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND L025.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND L025.SEQ = BASE.JUDGE_KIND ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE_D.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND BASE_D.SEQ = '005' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN HOPE ON HOPE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND HOPE.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND HOPE.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        /**
         * 抽出区分(OUTPUT_DIV)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77040 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintHeaderData {
        final String _testClassName;
        final int _colNumber;

        public PrintHeaderData(
                final String testClassName,
                final int colNumber
        ) {
            _testClassName = testClassName;
            _colNumber = colNumber;
        }
    }

    private class PrintBodyData {
        final String _receptNo;
        final String _sex;
        final String _name;
        final String _testsubclassCd;
        final String _score;
        final String _total4;
        final String _addScore;
        final String _total1;
        final String _hopeCnt;
        final String _totalRank1;
        final String _remark1;
        final String _interviewA;
        final String _interviewB;
        final String _interviewC;
        final String _plus;
        final String _minus;
        final String _judgeDiv;
        final String _judgeDivValue;
        final String _judgeKindValue;
        final String _finschoolName;
        final String _recomExamNo;

        public PrintBodyData(
                final String receptNo,
                final String sex,
                final String name,
                final String testsubclassCd,
                final String score,
                final String total4,
                final String addScore,
                final String total1,
                final String hopeCnt,
                final String totalRank1,
                final String remark1,
                final String interviewA,
                final String interviewB,
                final String interviewC,
                final String plus,
                final String minus,
                final String judgeDiv,
                final String judgeDivValue,
                final String judgeKindValue,
                final String finschoolName,
                final String recomExamNo
        ) {
            _receptNo = receptNo;
            _sex = sex;
            _name = name;
            _testsubclassCd = testsubclassCd;
            _score = score;
            _total4 = total4;
            _addScore = addScore;
            _total1 = total1;
            _hopeCnt = hopeCnt;
            _totalRank1 = totalRank1;
            _remark1 = remark1;
            _interviewA = interviewA;
            _interviewB = interviewB;
            _interviewC = interviewC;
            _plus = plus;
            _minus = minus;
            _judgeDiv = judgeDiv;
            _judgeDivValue = judgeDivValue;
            _judgeKindValue = judgeKindValue;
            _finschoolName = finschoolName;
            _recomExamNo = recomExamNo;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _sex;
        private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _sex = request.getParameter("SEX");
            _schoolName = getSchoolName(db2);
            _testDivName = getTestDivName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
            String sqlwk = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "'";
            String sql = "1".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '105' ") : "2".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '106' ") : "";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_ABBV");
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


/*
 * $Id: 39d0907ac0af73ea39421003bf770dec84484bf6 $
 *
 * 作成日: 2020/10/02
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL820H {

    private static final Log log = LogFactory.getLog(KNJL820H.class);

    private boolean _hasData;

    private Param _param;

    private static final String ABSENT = "5"; // 5:欠席

    private static final String ABSENTSUBCLASS = "1"; // 1:欠席

    private static final String WRITTEN_EXAMINATION = "1"; // 1:筆記

    private static final String INTERVIEW = "2"; // 2:面接

    private static final String COMPOSITION = "3"; // 3作文

    private static final int KESEKISYA_ICHIRANHYOU_LINE_MAX = 45;
    private static final int KESEKISYA_ICHIRANHYOU_COL_MAX = 2;

    private static final int SCORE_CHECK_LIST_LINE_MAX = 45;
    private static final int SCORE_CHECK_LIST_COL_MAX = 2;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String outputDate = sdf.format(new Date());

        if ("1".equals(_param._output)) {
            printKesekisyaIchiranhyou(db2, svf, outputDate);
        } else if ("2".equals(_param._output)) {
            printScoreCheckList(db2, svf, outputDate);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 欠席者一覧表を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日時
     */
    private void printKesekisyaIchiranhyou(final DB2UDB db2, final Vrw32alp svf, final String outputDate) {
        svf.VrSetForm("KNJL820H_1.frm", 1);

        List<KesekisyaData> kesekisyaList = getKesekisyaList(db2);

        int lineCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;

        setKesekisyaTitle(svf, pageCnt, outputDate);

        for (KesekisyaData kesekisyaData : kesekisyaList) {
            if (lineCnt > KESEKISYA_ICHIRANHYOU_LINE_MAX) {
                lineCnt = 1;

                // 改ページの制御
                if (colCnt >= KESEKISYA_ICHIRANHYOU_COL_MAX) {
                    svf.VrEndPage();
                    colCnt = 1;
                    pageCnt++;
                    setKesekisyaTitle(svf, pageCnt, outputDate);
                } else {
                    colCnt++;
                }
            }

            svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, kesekisyaData._receptNo);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(kesekisyaData._name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + colCnt + "_" + nameFieldStr, lineCnt, kesekisyaData._name);

            svf.VrsOutn("SEX" + colCnt, lineCnt, kesekisyaData._sex);

            lineCnt++;
            _hasData = true;
        }
    }

    /**
     * 得点チェックリストを出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日時
     */
    private void printScoreCheckList(final DB2UDB db2, final Vrw32alp svf, final String outputDate) {
        svf.VrSetForm("KNJL820H_2.frm", 1);

        List<TestSubclassData> testSubclassList = getTestSubclassList(db2);
        List<HallData> hallList = getHallList(db2);
        Map<String, List<HallReceptData>> hallReceptList = getHallReceptList(db2);
        Map<String, Map<String, ScoreCheckData>> scoreCheckReceptMap = getScoreCheckMap(db2);
        Map<String, ScoreCheckData> scoreCheckSubClassMap = null;
        ScoreCheckData scoreCheckData = null;

        int lineCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;

        for (TestSubclassData testSubclassData : testSubclassList) {
            for (HallData hallData : hallList) {
                setScoreCheckListTitle(svf, testSubclassData._testClassName, hallData._hallName, pageCnt, outputDate);

                if (hallReceptList.containsKey(hallData._hallCd)) {
                    // 会場に割り振られた人は、得点未入力（TESTSUBCLASS_DATにデータなし）でも表示はする。
                    // 一頁以内に表示する受験者情報の単位（９０人／頁）で繰り返す。
                    List<HallReceptData> receptList = hallReceptList.get(hallData._hallCd);
                    for (HallReceptData hallReceptData : receptList) {
                        if (lineCnt > SCORE_CHECK_LIST_LINE_MAX) {
                            lineCnt = 1;

                            // 改ページの制御
                            if (colCnt >= SCORE_CHECK_LIST_COL_MAX) {
                                svf.VrEndPage();
                                colCnt = 1;
                                pageCnt++;
                                setScoreCheckListTitle(svf, testSubclassData._testClassName, hallData._hallName, pageCnt, outputDate);
                            } else {
                                colCnt++;
                            }
                        }

                        svf.VrsOutn("EXAM_NO" + String.valueOf(colCnt), lineCnt, hallReceptData._receptNo);

                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(hallReceptData._name);
                        final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                        svf.VrsOutn("NAME" + String.valueOf(colCnt) + "_" + nameFieldStr, lineCnt, hallReceptData._name);

                        svf.VrsOutn("SEX" + String.valueOf(colCnt), lineCnt, hallReceptData._sex);

                        // 得点未入力（TESTSUBCLASS_DATにデータなし）は、得点を表示しない。
                        if (scoreCheckReceptMap.containsKey(hallReceptData._receptNo)) {
                            scoreCheckSubClassMap = scoreCheckReceptMap.get(hallReceptData._receptNo);

                            if (scoreCheckSubClassMap.containsKey(testSubclassData._testClassCd)) {
                                scoreCheckData = scoreCheckSubClassMap.get(testSubclassData._testClassCd);

                                String score = ABSENTSUBCLASS.equals(scoreCheckData._attendFlg) || ABSENT.equals(hallReceptData._judgeDiv) ? "欠席" : scoreCheckData._score;
                                svf.VrsOutn("SCORE" + String.valueOf(colCnt), lineCnt, score);
                            }
                        }

                        lineCnt++;
                        _hasData = true;
                    }
                }

                svf.VrEndPage();
                lineCnt = 1;
                colCnt = 1;
                pageCnt++;
            }
        }
    }

    private void setKesekisyaTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　欠席者一覧表");
        svf.VrsOut("EXAM_DIV", _param._testDivName);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
    }

    private void setScoreCheckListTitle(final Vrw32alp svf, final String testClassName, final String examhallName, final int pageNo, final String outputDate) {
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　得点チェックリスト");
        svf.VrsOut("EXAM_DIV", _param._testDivName);
        svf.VrsOut("EXAM_SUBCLASS", testClassName);
        svf.VrsOut("EXAM_HALL", examhallName);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
    }

    private List<KesekisyaData> getKesekisyaList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<KesekisyaData> kesekisyaList = new ArrayList<KesekisyaData>();

        try {
            final String kesekisyaIchiranhyouSql = getKesekisyaIchiranhyouSql();
            log.debug(" sql =" + kesekisyaIchiranhyouSql);
            ps = db2.prepareStatement(kesekisyaIchiranhyouSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");

                final KesekisyaData kesekisyaData = new KesekisyaData(receptNo, name, sex);

                kesekisyaList.add(kesekisyaData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return kesekisyaList;
    }

    private Map<String, Map<String, ScoreCheckData>> getScoreCheckMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, ScoreCheckData>> scoreCheckReceptMap = new LinkedHashMap<String, Map<String, ScoreCheckData>>();
        Map<String, ScoreCheckData> scoreCheckSubClassMap = new LinkedHashMap<String, ScoreCheckData>();

        try {
            final String scoreCheckListSql = getScoreCheckListSql();
            log.debug(" sql =" + scoreCheckListSql);
            ps = db2.prepareStatement(scoreCheckListSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String testsubclassCd = rs.getString("TESTSUBCLASSCD");
                final String attendFlg = rs.getString("ATTEND_FLG");
                final String score = rs.getString("SCORE");

                final ScoreCheckData scoreCheckData = new ScoreCheckData(receptNo, testsubclassCd, attendFlg, score);
                if (scoreCheckReceptMap.containsKey(receptNo)) {
                    scoreCheckSubClassMap = scoreCheckReceptMap.get(receptNo);
                } else {
                    scoreCheckSubClassMap = new LinkedHashMap<String, ScoreCheckData>();
                    scoreCheckReceptMap.put(receptNo, scoreCheckSubClassMap);
                }

                scoreCheckSubClassMap.put(testsubclassCd, scoreCheckData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return scoreCheckReceptMap;
    }

    private Map<String, List<HallReceptData>> getHallReceptList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, List<HallReceptData>> hallReceptMap = new LinkedHashMap<String, List<HallReceptData>>();
        List<HallReceptData> receptList = new ArrayList<HallReceptData>();

        try {
            final String hallReceptListSql = getHallReceptListSql();
            log.debug(" sql =" + hallReceptListSql);
            ps = db2.prepareStatement(hallReceptListSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhallCd = rs.getString("EXAMHALLCD");
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String judgeDiv = rs.getString("JUDGEDIV");

                final HallReceptData hallReceptData = new HallReceptData(examhallCd, receptNo, name, sex, judgeDiv);
                if (hallReceptMap.containsKey(examhallCd)) {
                    receptList = hallReceptMap.get(examhallCd);
                } else {
                    receptList = new ArrayList<HallReceptData>();
                    hallReceptMap.put(examhallCd, receptList);
                }
                receptList.add(hallReceptData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hallReceptMap;
    }

    private List<HallData> getHallList(final DB2UDB db2) {
        List<HallData> hallList = new ArrayList<HallData>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String hallSql = getHallSql();
            log.debug(" sql =" + hallSql);
            ps = db2.prepareStatement(hallSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String hallCd = rs.getString("EXAMHALLCD");
                final String hallName = rs.getString("EXAMHALL_NAME");

                final HallData hallData = new HallData(hallCd, hallName);
                hallList.add(hallData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hallList;
    }

    private List<TestSubclassData> getTestSubclassList(final DB2UDB db2) {
        List<TestSubclassData> testSubclassList = new ArrayList<TestSubclassData>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String testSubclassSql = getTestSubclassSql();
            log.debug(" sql =" + testSubclassSql);
            ps = db2.prepareStatement(testSubclassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testSubClassCd = rs.getString("TESTSUBCLASSCD");
                final String testSubClassName = rs.getString("TESTSUBCLASS_NAME");

                final TestSubclassData testSubclassData = new TestSubclassData(testSubClassCd, testSubClassName);
                testSubclassList.add(testSubclassData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return testSubclassList;
    }

    private String getKesekisyaIchiranhyouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("      RECEPT.RECEPTNO, ");
        stb.append("      BASE.NAME, ");
        stb.append("      Z002.NAME2 AS SEX ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND RECEPT.JUDGEDIV = '5' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getScoreCheckListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPTNO, ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     ATTEND_FLG, ");
        stb.append("     SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND EXAM_TYPE = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO, ");
        stb.append("     TESTSUBCLASSCD ");
        return stb.toString();
    }


    private String getHallReceptListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     CASE WHEN RECEPT.JUDGEDIV = '5' THEN 0 ELSE RECEPT.RECEPTNO END AS ORDER_RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     RECEPT.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_LIST_YDAT HALLLIST ON HALLLIST.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND HALLLIST.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND HALLLIST.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND HALLLIST.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = HALLLIST.ENTEXAMYEAR ");
        stb.append("          AND HALL.APPLICANTDIV = HALLLIST.APPLICANTDIV ");
        stb.append("          AND HALL.TESTDIV = HALLLIST.TESTDIV ");
        stb.append("          AND HALL.EXAM_TYPE = HALLLIST.EXAM_TYPE ");
        stb.append("          AND HALL.EXAMHALLCD = HALLLIST.EXAMHALLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        // 会場を 1:筆記、2:面接、3:作文 で切り分けるために、ENTEXAM_HALL_LIST_YDAT(HALLLIST) の EXAM_TYPE を流用して使っている。
        // 本来の用途とは違う使い方のため、他のテーブルの EXAM_TYPE と結合させない。
        stb.append("     AND HALLLIST.EXAM_TYPE = '" + _param._syukessekiHanteiHou + "' ");
        if (!"ALL".equals(_param._hallcd1)) {
            stb.append("     AND HALLLIST.EXAMHALLCD = '" + _param._hallcd1 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     ORDER_RECEPTNO, ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getHallSql() {
        final StringBuffer stb = new StringBuffer();
        // 人が割り当てられた会場のみ取得する
        stb.append(" WITH HALLLIST AS ( ");
        stb.append("     SELECT ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         APPLICANTDIV, ");
        stb.append("         TESTDIV, ");
        stb.append("         EXAM_TYPE, ");
        stb.append("         EXAMHALLCD ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_HALL_LIST_YDAT ");
        stb.append("     GROUP BY ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         APPLICANTDIV, ");
        stb.append("         TESTDIV, ");
        stb.append("         EXAM_TYPE, ");
        stb.append("         EXAMHALLCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT HALL ");
        stb.append("     INNER JOIN HALLLIST ");
        stb.append("             ON HALLLIST.ENTEXAMYEAR    = HALL.ENTEXAMYEAR ");
        stb.append("            AND HALLLIST.APPLICANTDIV   = HALL.APPLICANTDIV ");
        stb.append("            AND HALLLIST.TESTDIV        = HALL.TESTDIV ");
        stb.append("            AND HALLLIST.EXAM_TYPE      = HALL.EXAM_TYPE ");
        stb.append("            AND HALLLIST.EXAMHALLCD     = HALL.EXAMHALLCD ");
        stb.append(" WHERE ");
        stb.append("     HALL.ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("     AND HALL.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND HALL.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND HALL.EXAM_TYPE    = '" + _param._syukessekiHanteiHou + "' ");
        if (!"ALL".equals(_param._hallcd1)) {
            stb.append("     AND HALL.EXAMHALLCD   = '" + _param._hallcd1 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     EXAMHALLCD ");
        return stb.toString();
    }

    private String getTestSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("      TESTSUBCLASSCD, ");
        stb.append("      TESTSUBCLASS_NAME ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_TESTSUBCLASSCD_DAT ");
        stb.append("  WHERE ");
        stb.append("      ENTEXAMYEAR  = '" + _param._entexamyear + "' AND ");
        stb.append("      APPLICANTDIV = '" + _param._applicantDiv + "'    AND ");
        stb.append("      TESTDIV      = '" + _param._testDiv + "'   AND ");
        stb.append("      EXAM_TYPE    = '1' AND ");
        stb.append("      TESTSUBCLASSCD IS NOT NULL AND ");
        if (WRITTEN_EXAMINATION.equals(_param._syukessekiHanteiHou)) {
            // 試験会場に筆記が選ばれた場合は、科目を筆記（REMARK2 IS NULL）で絞り込む
            stb.append("      REMARK2      IS NULL ");
        } else if (INTERVIEW.equals(_param._syukessekiHanteiHou)) {
            // 試験会場に面接が選ばれた場合は、科目を面接（REMARK2=1）で絞り込む
            stb.append("      REMARK2      = '1' ");
        } else if (COMPOSITION.equals(_param._syukessekiHanteiHou)) {
            // 試験会場に作文が選ばれた場合は、科目を作文（REMARK2=2）で絞り込む
            stb.append("      REMARK2      = '2' ");
        }
        stb.append("  ORDER BY ");
        stb.append("      VALUE(TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77310 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class KesekisyaData {
        final String _receptNo;
        final String _name;
        final String _sex;

        public KesekisyaData(
                final String receptNo,
                final String name,
                final String sex
        ) {
            _receptNo = receptNo;
            _name = name;
            _sex = sex;
        }
    }

    private class ScoreCheckData {
        final String _receptNo;
        final String _testsubclassCd;
        final String _attendFlg;
        final String _score;

        public ScoreCheckData(
                final String receptNo,
                final String testsubclassCd,
                final String attendFlg,
                final String score
        ) {
            _receptNo = receptNo;
            _testsubclassCd = testsubclassCd;
            _attendFlg = attendFlg;
            _score = score;
        }
    }

    private class HallReceptData {
        final String _hallCd;
        final String _receptNo;
        final String _name;
        final String _sex;
        final String _judgeDiv;

        public HallReceptData(
                final String hallCd,
                final String receptNo,
                final String name,
                final String sex,
                final String judgeDiv
        ) {
        	_hallCd = hallCd;
            _receptNo = receptNo;
            _name = name;
            _sex = sex;
            _judgeDiv = judgeDiv;
        }
    }

    private class HallData {
        final String _hallCd;
        final String _hallName;

        public HallData(
                final String hallCd,
                final String hallName
        ) {
            _hallCd = hallCd;
            _hallName = hallName;
        }
    }

    private class TestSubclassData {
        final String _testClassCd;
        final String _testClassName;

        public TestSubclassData(
                final String testClassCd,
                final String testClassName
        ) {
            _testClassCd = testClassCd;
            _testClassName = testClassName;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _output;
        private final String _syukessekiHanteiHou;
        private final String _hallcd1;
        private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _output = request.getParameter("OUTPUT");
            _syukessekiHanteiHou = request.getParameter("SYUKESSEKI_HANTEI_HOU");
            _hallcd1 = request.getParameter("HALLCD1");
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


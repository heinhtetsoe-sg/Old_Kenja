/*
 * $Id: 33d50f4cf3fe71acd62f678a91bf8a54cbac8ef6 $
 *
 * 作成日: 2020/10/26
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL693H {

    private static final Log log = LogFactory.getLog(KNJL693H.class);

    private boolean _hasData;

    private Param _param;

    private static final String ABSENT = "3";

    private static final String PASS = "1";

    private static final String ENTRANCE = "1";

    private static final String TESTDIV_IPPAN = "02"; // 入試区分(TESTDIV)　02:一般

    private static final String TESTDIV1_IPPAN = "06"; // 類別(TESTDIV1)　06:一般

    private static final String TESTDIV1_SHIZYUKU = "08"; // 類別(TESTDIV1)　08:私塾

    private static final String TESTDIV1_HEIGAN = "07"; // 類別(TESTDIV1)　併願

    private static final int NYUSHIYOU_ZYUKENBANGOUHYOU_LINE_MAX = 12;
    private static final int NYUSHIYOU_ZYUKENBANGOUHYOU_COL_MAX = 3;

    private static final int GOUKAKUSYORUI_HUTOYO_LABEL_LINE_MAX = 12;
    private static final int GOUKAKUSYORUI_HUTOYO_LABEL_COL_MAX = 2;

    private static final String TOKYO = "13";

    private static final int NYUGAKUSYA_GAKURYOKUTESTYO_ZYUKENBANGOUHYO_LINE_MAX = 12;
    private static final int NYUGAKUSYA_GAKURYOKUTESTYO_ZYUKENBANGOUHYO_COL_MAX = 3;

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
            //入試用受験番号票の印刷
            printNyushiyouZyukenbangouhyou(db2, svf);
        } else if ("2".equals(_param._output)) {
            //合格書類封筒用ラベルの印刷
            printGoukakusyoryuiHutouyouLabel(db2, svf);
        } else if ("3".equals(_param._output)) {
            //学力テスト用受験番号票の印刷
            printGakuryokuTestyouZyukenbangouhyou(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 入試用受験番号票を印刷する。
     *
     * @param db2
     * @param svf
     */
    private void printNyushiyouZyukenbangouhyou(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL693H_1_1.frm", 1);

        List<String> receptNoList = getNyushiyouZyukenbangouhyou(db2);

        int lineCnt = 1; // 書き込み行数
        int colCnt = 1; // 書き込み列数
        int colLineCnt = 0; // 書き込み行列
        int pageCnt = 0; // 書き込んだページ数
        int listNo = 0; // 読み取り要素番号

        int lineMax = receptNoList.size();
        int pageMax = (int) Math.ceil((double) lineMax / NYUSHIYOU_ZYUKENBANGOUHYOU_LINE_MAX);

        for (int i = 0; i < lineMax; i++) {
            if (colCnt > NYUSHIYOU_ZYUKENBANGOUHYOU_COL_MAX) {
                // 改ページの制御
                if (colLineCnt >= NYUSHIYOU_ZYUKENBANGOUHYOU_LINE_MAX) {
                    lineCnt = 1;
                    colCnt = 1;
                    colLineCnt = 0;
                    pageCnt++;

                    svf.VrEndPage();
                    svf.VrSetForm("KNJL693H_1_2.frm", 1);
                } else {
                    lineCnt++;
                    colCnt = 1;
                }
            }

            listNo =  pageCnt + (colLineCnt * pageMax); // 初項が頁数（0スタート）の増分値が最大頁数の数列の計算
            if (listNo < lineMax) {
                String receptNo = receptNoList.get(listNo);

                svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, receptNo);
            } else {
                i--; // 何も出力しない場合はカウントを進めない
            }

            colCnt++;
            colLineCnt++;
            _hasData = true;
        }
    }

    /**
     * 合格書類封筒用ラベルを印刷する。
     *
     * @param db2
     * @param svf
     */
    private void printGoukakusyoryuiHutouyouLabel(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL693H_2.frm", 1);

        List<GoukakusyoruiHutoyoLabel> ghlList = getGoukakusyoruiHutoyoLabel(db2, "");

        int lineCnt = 1; // 書き込み行数
        int colCnt = 1; // 書き込み列数
        int colLineCnt = 1; // 書き込み行列数

        for (GoukakusyoruiHutoyoLabel ghl : ghlList) {
            if (colCnt > GOUKAKUSYORUI_HUTOYO_LABEL_COL_MAX) {
                // 改ページの制御
                if (colLineCnt > GOUKAKUSYORUI_HUTOYO_LABEL_LINE_MAX) {
                    lineCnt = 1;
                    colCnt = 1;
                    colLineCnt = 1;

                    svf.VrEndPage();
                } else {
                    lineCnt++;
                    colCnt = 1;
                }
            }

            String gokaku = "合格";
            if (TESTDIV_IPPAN.equals(ghl._testdiv) && "1".equals(ghl._heigan)) {
                gokaku = "<<" + gokaku + ">>";
            } else {
                if (TESTDIV1_IPPAN.equals(ghl._testdiv1) || TESTDIV1_SHIZYUKU.equals(ghl._testdiv1)) {
                    gokaku = "[" + gokaku + "]";
                } else if (TESTDIV1_HEIGAN.equals(ghl._testdiv1)) {
                    gokaku = "<" + gokaku + ">";
                }
            }
            svf.VrsOutn("JUDGE" + colCnt, lineCnt, gokaku);
            svf.VrsOutn("SCHOOL_NAME" + colCnt, lineCnt, _param._schoolName);
            svf.VrsOutn("EXAM_YEAR" + colCnt, lineCnt, _param._entexamyear + "年度入試");
            svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, ghl._receptNo);
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(ghl._name);
            final String nameFieldStr = nameByte > 28 ? "2" : "1";
            svf.VrsOutn("NAME" + colCnt + "_" + nameFieldStr, lineCnt, ghl._name);

            colCnt++;
            colLineCnt++;
            _hasData = true;
        }
    }

    /**
     * 学力テスト用受験番号票を印刷する。
     *
     * @param db2
     * @param svf
     */
    private void printGakuryokuTestyouZyukenbangouhyou(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL693H_3_1.frm", 1);

        List<GoukakusyoruiHutoyoLabel> ghlList = getGoukakusyoruiHutoyoLabel(db2, ENTRANCE);

        int lineCnt = 1; // 書き込み行数
        int colCnt = 1; // 書き込み列数
        int colLineCnt = 0; // 書き込み行列
        int pageCnt = 0; // 書き込んだページ数
        int listNo = 0; // 読み取り要素番号

        int lineMax = ghlList.size();
        int pageMax = (int) Math.ceil((double) lineMax / NYUGAKUSYA_GAKURYOKUTESTYO_ZYUKENBANGOUHYO_LINE_MAX);

        for (int i = 0; i < lineMax; i++) {
            if (colCnt > NYUGAKUSYA_GAKURYOKUTESTYO_ZYUKENBANGOUHYO_COL_MAX) {
                // 改ページの制御
                if (colLineCnt >= NYUGAKUSYA_GAKURYOKUTESTYO_ZYUKENBANGOUHYO_LINE_MAX) {
                    lineCnt = 1;
                    colCnt = 1;
                    colLineCnt = 0;
                    pageCnt++;

                    svf.VrEndPage();
                    svf.VrSetForm("KNJL693H_3_2.frm", 1);
                } else {
                    lineCnt++;
                    colCnt = 1;
                }
            }

            listNo =  pageCnt + (colLineCnt * pageMax); // 初項が頁数（0スタート）の増分値が最大頁数の数列の計算
            if (listNo < lineMax) {
            	GoukakusyoruiHutoyoLabel ghl = ghlList.get(listNo);

                svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, ghl._receptNo);
                final int nameByte = KNJ_EditEdit.getMS932ByteLength(ghl._name);
                final String nameFieldStr = nameByte > 38 ? "2" : "1";
                svf.VrsOutn("NAME" + colCnt + "_" + nameFieldStr, lineCnt, ghl._name);
            } else {
                i--; // 何も出力しない場合はカウントを進めない
            }

            colCnt++;
            colLineCnt++;
            _hasData = true;
        }
    }

    private List<String> getNyushiyouZyukenbangouhyou(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<String> receptNoList = new ArrayList<String>();

        try {
            final String receptNoSql = getReceptnoSql();
            log.debug(" sql =" + receptNoSql);
            ps = db2.prepareStatement(receptNoSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");

                receptNoList.add(receptNo);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return receptNoList;
    }

    private List<GoukakusyoruiHutoyoLabel> getGoukakusyoruiHutoyoLabel(final DB2UDB db2, String entrance) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<GoukakusyoruiHutoyoLabel> ghlList = new ArrayList<GoukakusyoruiHutoyoLabel>();

        try {
            final String entReceptNoSql = getEntReceptnoSql(entrance);
            log.debug(" sql =" + entReceptNoSql);
            ps = db2.prepareStatement(entReceptNoSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String hei = rs.getString("HEIGAN");
                final String testdiv = rs.getString("TESTDIV");
                final String testdiv1 = rs.getString("TESTDIV1");
                final String finSchoolPrefCd = rs.getString("FINSCHOOL_PREF_CD");

                GoukakusyoruiHutoyoLabel ghl = new GoukakusyoruiHutoyoLabel(receptNo, name, hei, testdiv, testdiv1, finSchoolPrefCd);
                ghlList.add(ghl);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return ghlList;
    }

    private String getReceptnoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("       ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BASE.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("      AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("       RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("   AND RECEPT.APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("   AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("   AND RECEPT.EXAM_TYPE    = '1' ");
        if (!"".equals(_param._receptNoStart)) {
            stb.append("   AND RECEPT.RECEPTNO >= '" + _param._receptNoStart + "' ");
        }
        if (!"".equals(_param._receptNoEnd)) {
            stb.append("   AND RECEPT.RECEPTNO <= '" + _param._receptNoEnd + "' ");
        }
        // 類別が 00:全て 以外の場合に条件に加える
        if (!"00".equals(_param._testdiv1)) {
            stb.append("   AND BASE.TESTDIV1       = '" + _param._testdiv1 + "' ");
        }
        // 男女別が 3:全員 以外の場合に条件に加える
        if (!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append("   AND (BASE.JUDGEMENT <> '" + ABSENT + "' OR BASE.JUDGEMENT IS NULL) "); // 3:欠席 は除く
        stb.append(" ORDER BY ");
        // 出力順が 2:氏名(50音順) の場合にソートに 氏名カナ を加える
        if ("2".equals(_param._order)) {
            stb.append("     BASE.NAME_KANA, ");
        }
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getEntReceptnoSql(String entrance) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     CASE WHEN BASED.REMARK9 IS NULL THEN 0 ELSE 1 END AS HEIGAN, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.TESTDIV1, ");
        stb.append("     FSMST.FINSCHOOL_PREF_CD ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("       ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BASE.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("      AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASED ");
        stb.append("       ON BASED.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("      AND BASED.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND BASED.EXAMNO       = BASE.EXAMNO ");
        stb.append("      AND BASED.SEQ          = '031' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSMST ");
        stb.append("       ON FSMST.FINSCHOOLCD  = BASE.FS_CD ");
        stb.append(" WHERE ");
        stb.append("       RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("   AND RECEPT.APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("   AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("   AND RECEPT.EXAM_TYPE    = '1' ");
        if (!"".equals(_param._receptNoStart)) {
            stb.append("   AND RECEPT.RECEPTNO >= '" + _param._receptNoStart + "' ");
        }
        if (!"".equals(_param._receptNoEnd)) {
            stb.append("   AND RECEPT.RECEPTNO <= '" + _param._receptNoEnd + "' ");
        }
        // 類別が 00:全て 以外の場合に条件に加える
        if (!"00".equals(_param._testdiv1)) {
            stb.append("   AND BASE.TESTDIV1       = '" + _param._testdiv1 + "' ");
        }
        // 男女別が 3:全員 以外の場合に条件に加える
        if (!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX            = '" + _param._sex + "' ");
        }
        stb.append("   AND BASE.JUDGEMENT      = '" + PASS + "' "); // 1:合格
        if (!"".equals(entrance)) {
            stb.append("   AND BASE.ENTDIV         = '" + entrance + "' "); // 1:入学
        }
        stb.append(" ORDER BY ");
        // 出力順が 2:氏名(50音順) の場合にソートに 氏名カナ を加える
        if ("2".equals(_param._order)) {
            stb.append("     BASE.NAME_KANA, ");
        }
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77377 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class GoukakusyoruiHutoyoLabel {
        private final String _receptNo;
        private final String _name;
        private final String _heigan;
        private final String _testdiv;
        private final String _testdiv1;
        private final String _finSchoolPrefCd;

        GoukakusyoruiHutoyoLabel(String receptNo, String name, String heigan, String testdiv, String testdiv1, String finSchoolPrefCd) {
            _receptNo = receptNo;
            _name = name;
            _heigan = heigan;
            _testdiv = testdiv;
            _testdiv1 = testdiv1;
            _finSchoolPrefCd = finSchoolPrefCd;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _testDiv;
        private final String _receptNoStart;
        private final String _receptNoEnd;
        private final String _testdiv1;
        private final String _sex;
        private final String _order;
        private final String _output;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _testDiv = request.getParameter("TESTDIV");
            String receptNoStart = request.getParameter("RECEPTNO_START");
            if ("".equals(receptNoStart)) {
                _receptNoStart = "";
            } else {
                _receptNoStart = String.format("%04d", Integer.parseInt(receptNoStart));
            }
            String receptNoEnd = request.getParameter("RECEPTNO_END");
            if ("".equals(receptNoEnd)) {
                _receptNoEnd = "";
            } else {
                _receptNoEnd = String.format("%04d", Integer.parseInt(receptNoEnd));
            }
            _testdiv1 = request.getParameter("TESTDIV1");
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _output = request.getParameter("OUTPUT");
            _schoolName = getSchoolName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ";
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
    }
}

// eof


/*
 * $Id$
 *
 * 作成日: 2020/09/10
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

public class KNJL388I {

    private static final Log log = LogFactory.getLog(KNJL388I.class);

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
        svf.VrSetForm("KNJL388I.frm", 1);
        final List<Map<String, PrintHeaderData>> headerList = getHeaderList(db2);
        final List<Map<String, Map<String, PrintBodyData>>> bodyList = getBodyList(db2);

        int pageCnt = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String outputDate = sdf.format(new Date());

        // 一ページ以内に表示する受験者情報の単位（５０人／頁）で繰り返す。
        for (Map<String, Map<String, PrintBodyData>> refMap : bodyList) {
            // 一ページ以内に表示する科目の単位（１３科目／頁）で繰り返す。
            for (Map<String, PrintHeaderData> headerMap : headerList) {
                setTitle(svf, pageCnt, outputDate);
                setHeader(svf, headerMap);
                setBody(svf, refMap, headerMap);

                svf.VrEndPage();
                pageCnt++;
            }
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        final String titleStr = "3".equals(_param._sex) ? "男女共" : "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "";
        svf.VrsOut("TITLE", _param._entexamyear + "年度 入学試験 " + _param._testDivName + " 科目別欠席確認票 （" + titleStr + "）");

        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", "作成日時：" + outputDate);
    }

    private void setHeader(final Vrw32alp svf, final Map<String, PrintHeaderData> headerMap) {
        for (PrintHeaderData headerData : headerMap.values()) {
            final int testSubClassNameByte = KNJ_EditEdit.getMS932ByteLength(headerData._testClassName);
            final String testSubClassNameFieldStr = testSubClassNameByte > 4 ? "2" : "1";
            svf.VrsOut("CLASS_NAME" + headerData._colNumber + "_" + testSubClassNameFieldStr, headerData._testClassName);
        }
    }

    private void setBody(final Vrw32alp svf, final Map<String, Map<String, PrintBodyData>> refMap, Map<String, PrintHeaderData> headerMap) {
        int lineCnt = 1;

        for (Map<String, PrintBodyData> subMap : refMap.values()) {
            for (PrintBodyData printBody : subMap.values()) {
                svf.VrsOutn("EXAM_NO1", lineCnt, printBody._examNo);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(printBody._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printBody._name);

                svf.VrsOutn("SEX", lineCnt, printBody._sex);

                // JUDGEMENTが 4:欠席 は 欠席を印字する。
                if ("4".equals(printBody._judgement)) {
                    for (PrintHeaderData printHeader : headerMap.values()) {
                        svf.VrsOutn("SCORE" + printHeader._colNumber, lineCnt, "欠席");
                    }
                } else if ("1".equals(printBody._attendFlg)) {
                    // ATTEND_FLGが 1:欠席 の場合は、欠席を印字する。
                    if (headerMap.containsKey(printBody._testSubClassCd)) {
                        svf.VrsOutn("SCORE" + headerMap.get(printBody._testSubClassCd)._colNumber, lineCnt, "欠席");
                    }
                }
            }

            lineCnt++;
            _hasData = true;
        }
    }

    private List<Map<String, PrintHeaderData>> getHeaderList(final DB2UDB db2) {
        final List<Map<String, PrintHeaderData>> headerList = new ArrayList<Map<String, PrintHeaderData>>();
        Map<String, PrintHeaderData> headerMap = new LinkedHashMap<String, PrintHeaderData>();
        final int maxCol = 13;
        int colCnt = 1;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String headerSql = getHeaderSql();
            log.debug(" sql =" + headerSql);
            ps = db2.prepareStatement(headerSql);
            rs = ps.executeQuery();

            headerList.add(headerMap);
            while (rs.next()) {
                if (colCnt > maxCol) {
                    colCnt = 1;
                    headerMap = new LinkedHashMap<String, PrintHeaderData>();
                    headerList.add(headerMap);
                }
                final String testSubClassCd = rs.getString("TESTSUBCLASSCD");
                final String testSubClassName = rs.getString("TESTSUBCLASSNAME");

                final PrintHeaderData printHeaderData = new PrintHeaderData(testSubClassName, colCnt);
                headerMap.put(testSubClassCd, printHeaderData);
                colCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return headerList;
    }

    private List<Map<String, Map<String, PrintBodyData>>> getBodyList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final int maxLine = 50;
        int lineCnt = 1;

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
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    retMap = new LinkedHashMap<String, Map<String, PrintBodyData>>();
                    bodyList.add(retMap);
                }

                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String judgement = rs.getString("JUDGEMENT");
                final String testSubClassCd = rs.getString("TESTSUBCLASSCD");
                final String attendFlg = rs.getString("ATTEND_FLG");

                final PrintBodyData bodyData = new PrintBodyData(examNo, name, sex, judgement, testSubClassCd, attendFlg);
                if (!retMap.containsKey(examNo)) {
                    subMap = new LinkedHashMap<String, PrintBodyData>();
                    retMap.put(examNo, subMap);
                    lineCnt++;
                } else {
                    subMap = retMap.get(examNo);
                }
                subMap.put(testSubClassCd, bodyData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return bodyList;
    }

    private String getHeaderSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUBCLASS.TESTSUBCLASSCD, ");
        stb.append("     L009.NAME1 AS TESTSUBCLASSNAME ");
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
        stb.append(" ORDER BY ");
        stb.append("     VALUE(SUBCLASS.TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    private String getBodySql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, "); // TODO SCORE.RECEPTNO かもしれない
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     BASE.JUDGEMENT, ");
        stb.append("     SCORE.TESTSUBCLASSCD, ");
        stb.append("     SCORE.ATTEND_FLG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ON SCORE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND SCORE.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND SCORE.TESTDIV = BASE.TESTDIV ");
        stb.append("          AND SCORE.RECEPTNO = BASE.EXAMNO ");
        stb.append("          AND SCORE.EXAM_TYPE = '1' ");
        stb.append(" WHERE  ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '') <> '5' "); //5:未受験  B日程志願者のうちA日程または帰国生入試で既に合格している場合は除外
        /**
         * 抽出区分(SEX)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     VALUE(SCORE.TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77032 $");
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
        final String _examNo;
        final String _name;
        final String _sex;
        final String _judgement;
        final String _testSubClassCd;
        final String _attendFlg;

        public PrintBodyData(
                final String examNo,
                final String name,
                final String sex,
                final String judgement,
                final String testSubClassCd,
                final String attendFlg
        ) {
            _examNo = examNo;
            _name = name;
            _sex = sex;
            _judgement = judgement;
            _testSubClassCd = testSubClassCd;
            _attendFlg = attendFlg;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _schoolName;
        private final String _sex;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolName = getSchoolName(db2);
            _sex = request.getParameter("SEX");
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


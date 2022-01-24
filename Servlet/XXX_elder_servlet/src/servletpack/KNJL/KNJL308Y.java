/*
 * $Id: def4864bc8e7f31e68f81bbfa1cdcaefa68ff897 $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０８Ｙ＞  得点記入用紙
 **/
public class KNJL308Y {
    
    private static final Log log = LogFactory.getLog(KNJL308Y.class);
    
    private boolean _hasData;
    
    Param _param;
    
    private final String TEST_SUBCLASSCD2 = "2";
    
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
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final int startSeq = NumberUtils.isDigits(_param._seqStart) ? Integer.parseInt(_param._seqStart) : -1;
        final List subclasses = getSubclasses(db2);
        for (final Iterator itSch = subclasses.iterator(); itSch.hasNext(); ) {
            final Subclass subclass = (Subclass) itSch.next();
            int seq = startSeq;
            for (final Iterator itH = subclass._halls.iterator(); itH.hasNext();) {
                final Hall hall = (Hall) itH.next();
                
                for (final Iterator itHd = hall._detailNos.iterator(); itHd.hasNext();) {
                    final HallDetail hallDetail = (HallDetail) itHd.next();
                    final boolean useForm2 = "1".equals(_param._applicantDiv) && TEST_SUBCLASSCD2.equals(subclass._testSubclassCd);
                    svf.VrSetForm(useForm2 ? "KNJL308Y_2.frm" : "KNJL308Y.frm", 4);
                    
                    svf.VrsOut("NENDO", _param._entexamYear + "年度");
                    svf.VrsOut("TITLE", _param._title);
                    svf.VrsOut("SUBTITLE", _param._subTitle);
                    // svf.VrsOut("SUBTITLE2", "得点記入用紙");
                    svf.VrsOut("SUBJECT", subclass._testSubclassName);
                    svf.VrsOut("HALLNAME", hall._examHallName);
                    svf.VrsOut("HALLNO", hallDetail._detailNo);
                    if (seq != -1) {
                        svf.VrsOut("HALLNO1", String.valueOf(seq));
                        seq += 1;
                    }
                    
                    for (final Iterator it = hallDetail._scores.iterator(); it.hasNext();) {
                        final Score score = (Score) it.next();
                        svf.VrsOut("EXAMNO", score._examno);
//                        if (!"1".equals(_param._form)) {
//                            if (useForm2) {
//                                svf.VrsOut("ARITHMETIV", score._score);
//                                svf.VrsOut("CALC", score._score3);
//                            } else {
//                                svf.VrsOut("SCORE", score._score);
//                            }
//                        }
                        svf.VrEndRecord();
                    }
                    _hasData = true;
                }
            }
        }
    }
    
    private Subclass getSubclass(final List subclasses, final String testSubclassCd) {
        if (null != testSubclassCd) {
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (testSubclassCd.equals(subclass._testSubclassCd)) {
                    return subclass;
                }
            }
        }
        return null;
    }
    
    private Hall getHall(final List halls, final String examHallCd) {
        if (null != examHallCd) {
            for (final Iterator it = halls.iterator(); it.hasNext();) {
                final Hall hall = (Hall) it.next();
                if (examHallCd.equals(hall._examHallCd)) {
                    return hall;
                }
            }
        }
        return null;
    }
    
    private List getSubclasses(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List subclasses = new ArrayList();
        try {
            final String sql = getScoreSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                Subclass subclass = getSubclass(subclasses, testSubclassCd);
                if (null == subclass) {
                    if (null == testSubclassCd) {
                        continue;
                    }
                    final String testSubclassName = "1".equals(_param._applicantDiv) ? rs.getString("TESTSUBCLASSNAME1") : rs.getString("TESTSUBCLASSNAME2");
                    subclass = new Subclass(testSubclassCd, testSubclassName);
                    subclasses.add(subclass);
                }
                
                Hall hall = getHall(subclass._halls, rs.getString("EXAMHALLCD"));
                if (null == hall) {
                    if (null == rs.getString("EXAMHALLCD")) {
                        continue;
                    }
                    hall = new Hall(rs.getString("EXAMHALLCD"), rs.getString("EXAMHALL_NAME"));
                    subclass._halls.add(hall);
                }
                hall.addExamno(rs.getString("DETAIL_NO"), rs.getString("EXAMNO"), rs.getString("SCORE"), rs.getString("SCORE3"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return subclasses;
    }
    
    private static class Subclass {
        final String _testSubclassCd;
        final String _testSubclassName;
        final List _halls = new ArrayList();
        public Subclass(final String testSubclassCd, final String testSubclassName) {
            _testSubclassCd = testSubclassCd;
            _testSubclassName = testSubclassName;
        }
    }
    
    private static class Hall {
        final String _examHallCd;
        final String _examHallName;
        final List _detailNos = new ArrayList();
        
        public Hall(String examHallCd, String examHallName) {
            _examHallCd = examHallCd;
            _examHallName = examHallName;
        }
        
        public void addExamno(final String detailNo, final String examno, final String score, final String score3) {
            final HallDetail hallDetail = getDetail(_detailNos, null == detailNo ? "" : detailNo);
            hallDetail._scores.add(new Score(examno, score, score3));
        }
        
        private static HallDetail getDetail(final List details, final String detailNo) {
            for (final Iterator it = details.iterator(); it.hasNext();) {
                final HallDetail hallDetail = (HallDetail) it.next();
                if (detailNo.equals(hallDetail._detailNo)) {
                    return hallDetail;
                }
            }
            details.add(new HallDetail(detailNo));
            return getDetail(details, detailNo);
        }
    }
    
    private static class HallDetail {
        final String _detailNo;
        final List _scores = new ArrayList();
        public HallDetail(String detailNo) {
            _detailNo = detailNo;
        }
    }
    
    private static class Score {
        final String _examno;
        final String _score;
        final String _score3;
        public Score(final String examno, final String score, final String score3) {
            _examno = examno;
            _score = score;
            _score3 = score3;
        }
    }
    
    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HALL_EXAMNO AS (");
        stb.append(" SELECT");
        stb.append("     T1.ENTEXAMYEAR,");
        stb.append("     T1.APPLICANTDIV,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T1.EXAM_TYPE,");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T2.DETAIL_NO,");
        stb.append("     T3.RECEPTNO,");
        stb.append("     T3.EXAMNO");
        stb.append(" FROM");
        stb.append("     ENTEXAM_HALL_YDAT T1");
        stb.append("     INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV");
        stb.append("         AND T2.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("         AND T2.EXAMHALLCD = T1.EXAMHALLCD");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV");
        stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("         AND T3.RECEPTNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO");
        stb.append(" WHERE");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear  +"'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.EXAM_TYPE = '1'");
        if ("2".equals(_param._hallDiv)) {
            stb.append("     AND T1.EXAMHALLCD || T2.DETAIL_NO IN ").append(_param._examHallCds);
        } else {
            stb.append("     AND T1.EXAMHALLCD IN ").append(_param._examHallCds);
        }
        stb.append(" ORDER BY");
        stb.append("     T3.RECEPTNO");
        stb.append(" )");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T1.DETAIL_NO,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T7.PATTERN_NO,");
        stb.append("     T7.TESTSUBCLASSCD,");
        stb.append("     NM1.NAME1 AS TESTSUBCLASSNAME1,");
        stb.append("     NM1.NAME2 AS TESTSUBCLASSNAME2,");
        stb.append("     T4.COURSECD,");
        stb.append("     T4.MAJORCD,");
        stb.append("     T4.EXAMCOURSECD,");
        stb.append("     T1.EXAMNO,");
        stb.append("     T1.RECEPTNO,");
        stb.append("     T5.RECOM_KIND,");
        stb.append("     T8.SCORE,");
        stb.append("     T8.SCORE3");
        stb.append(" FROM");
        stb.append("     HALL_EXAMNO T1");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T5.TESTDIV = T1.TESTDIV");
        stb.append("         AND T5.EXAMNO = T3.EXAMNO");
        stb.append("     INNER JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV");
        stb.append("         AND T4.DESIREDIV = T5.DESIREDIV");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     INNER JOIN ENTEXAM_SCH_PTRN_SUB_DAT T7 ON T7.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T7.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T7.TESTDIV = T1.TESTDIV");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T8 ON T8.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T8.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T8.TESTDIV = T1.TESTDIV");
        stb.append("         AND T8.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("         AND T8.RECEPTNO = T1.RECEPTNO");
        stb.append("         AND T8.TESTSUBCLASSCD = T7.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'L009' AND NM1.NAMECD2 = T7.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T7.PATTERN_NO = '" + _param._schPtrn + "' ");
        stb.append(" ORDER BY T7.TESTSUBCLASSCD, T1.RECEPTNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _hallDiv;
        final String _examHallCds;
//        final String _form;
        final String _schPtrn;
        final String _seqStart;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _hallDiv = request.getParameter("HALL_DIV");
            _examHallCds = SQLUtils.whereIn(true, request.getParameterValues("CATEGORY_SELECTED"));
//            _form = request.getParameter("FORM");
            _schPtrn = request.getParameter("SCH_PTRN");
            _seqStart = request.getParameter("RENBAN");
        }
        
        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
